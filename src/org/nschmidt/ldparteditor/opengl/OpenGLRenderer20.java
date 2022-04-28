/* MIT - License

Copyright (c) 2012 - this year, Nils Schmidt

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package org.nschmidt.ldparteditor.opengl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.swt.graphics.Rectangle;
import org.lwjgl.opengl.swt.GLCanvas;
import org.eclipse.swt.widgets.Menu;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.composite.Composite3D;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GColourType;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.GData3;
import org.nschmidt.ldparteditor.data.GData4;
import org.nschmidt.ldparteditor.data.GTexture;
import org.nschmidt.ldparteditor.data.PGData3;
import org.nschmidt.ldparteditor.data.Primitive;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.data.colour.GCGlitter;
import org.nschmidt.ldparteditor.data.colour.GCSpeckle;
import org.nschmidt.ldparteditor.data.colour.GCType;
import org.nschmidt.ldparteditor.enumtype.Colour;
import org.nschmidt.ldparteditor.enumtype.GL20Primitives;
import org.nschmidt.ldparteditor.enumtype.IconSize;
import org.nschmidt.ldparteditor.enumtype.LDConfig;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.enumtype.WorkingMode;
import org.nschmidt.ldparteditor.helper.Arc;
import org.nschmidt.ldparteditor.helper.Arrow;
import org.nschmidt.ldparteditor.helper.ArrowBlunt;
import org.nschmidt.ldparteditor.helper.BufferFactory;
import org.nschmidt.ldparteditor.helper.Circle;
import org.nschmidt.ldparteditor.helper.LDPartEditorException;
import org.nschmidt.ldparteditor.helper.Manipulator;
import org.nschmidt.ldparteditor.helper.composite3d.ViewIdleManager;
import org.nschmidt.ldparteditor.helper.math.PowerRay;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.AddToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.TransformationModeToolItem;
import org.nschmidt.ldparteditor.workbench.UserSettingState;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * This class draws the 3D view (OpenGL 2.0 compliant)
 *
 * @author nils
 *
 */
public class OpenGLRenderer20 extends OpenGLRenderer {

    /** The transformation matrix buffer of the view [NOT PUBLIC YET] */
    private final FloatBuffer viewport = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer rotation = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer rotationInv = BufferUtils.createFloatBuffer(16);
    private final Matrix4f rotationInv4f = new Matrix4f();

    private final float[][][] renderedPoints = new float[1][][];
    private final float[][] solidColours = new float[1][];
    private final float[][] transparentColours = new float[1][];
    private final float[] cWidth = new float[1];
    private final float[] cHeight = new float[1];

    private final Lock lock = new ReentrantLock();

    private final Lock lockSpeckle = new ReentrantLock();
    private final Lock lockGlitter = new ReentrantLock();

    private final AtomicBoolean alive = new AtomicBoolean(true);
    private final AtomicInteger needData = new AtomicInteger(0);
    private Thread raytracer = null;

    private static long hoverSettingsTime = System.currentTimeMillis();

    public FloatBuffer getRotationInverse() {
        return rotationInv;
    }

    public OpenGLRenderer20(Composite3D c3d) {
        super(c3d);
    }

    @Override
    public Composite3D getC3D() {
        return c3d;
    }

    private int vsGlossId = -1;
    private int fsGlossId = -1;
    private int pGlossId = -1;

    private int baseImageLoc = -1;
    private int glossMapLoc = -1;
    private int cubeMapLoc = -1;
    private int cubeMapMatteLoc = -1;
    private int cubeMapMetalLoc = -1;
    private int alphaSwitchLoc = -1;
    private int normalSwitchLoc = -1;
    private int noTextureSwitch = -1;
    private int noLightSwitch = -1;
    private int noGlossMapSwitch = -1;
    private int cubeMapSwitch = -1;

    private int skipFrame;

    /**
     * Initializes the Scene and gives OpenGL-Hints
     */
    @Override
    public void init() {
        // MARK OpenGL Hints and Initialization
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glDepthFunc(GL11.GL_LESS);

        GL11.glClearDepth(1.0f);
        GL11.glClearColor(Colour.backgroundColourR, Colour.backgroundColourG, Colour.backgroundColourB, 1.0f);

        GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
        GL11.glPointSize(5);

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glShadeModel(GL11.GL_SMOOTH);

        GL11.glEnable(GL11.GL_NORMALIZE);

        GL11.glEnable(GL11.GL_LIGHT0);
        GL11.glEnable(GL11.GL_LIGHT1);
        GL11.glEnable(GL11.GL_LIGHT2);
        GL11.glEnable(GL11.GL_LIGHT3);

        GL11.glLightModelfv(GL11.GL_LIGHT_MODEL_AMBIENT, BufferFactory.floatBuffer(new float[] { 0.1f, 0.1f, 0.1f, 1f }));

        GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, BufferFactory.floatBuffer(new float[] { Colour.light1ColourR, Colour.light1ColourG, Colour.light1ColourB, 1f }));
        GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_SPECULAR, BufferFactory.floatBuffer(new float[] { Colour.light1SpecularColourR, Colour.light1SpecularColourG, Colour.light1SpecularColourB, 1f }));
        GL11.glLightf(GL11.GL_LIGHT0, GL11.GL_LINEAR_ATTENUATION, .001f);

        GL11.glLightfv(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, BufferFactory.floatBuffer(new float[] { Colour.light2ColourR, Colour.light2ColourG, Colour.light2ColourB, 1f }));
        GL11.glLightfv(GL11.GL_LIGHT1, GL11.GL_SPECULAR, BufferFactory.floatBuffer(new float[] { Colour.light2SpecularColourR, Colour.light2SpecularColourG, Colour.light2SpecularColourB, 1f }));
        GL11.glLightf(GL11.GL_LIGHT1, GL11.GL_LINEAR_ATTENUATION, .001f);

        GL11.glLightfv(GL11.GL_LIGHT2, GL11.GL_DIFFUSE, BufferFactory.floatBuffer(new float[] { Colour.light3ColourR, Colour.light3ColourG, Colour.light3ColourB, 1f }));
        GL11.glLightfv(GL11.GL_LIGHT2, GL11.GL_SPECULAR, BufferFactory.floatBuffer(new float[] { Colour.light3SpecularColourR, Colour.light3SpecularColourG, Colour.light3SpecularColourB, 1f }));
        GL11.glLightf(GL11.GL_LIGHT2, GL11.GL_LINEAR_ATTENUATION, .001f);

        GL11.glLightfv(GL11.GL_LIGHT3, GL11.GL_DIFFUSE, BufferFactory.floatBuffer(new float[] { Colour.light4ColourR, Colour.light4ColourG, Colour.light4ColourB, 1f }));
        GL11.glLightfv(GL11.GL_LIGHT3, GL11.GL_SPECULAR, BufferFactory.floatBuffer(new float[] { Colour.light4SpecularColourR, Colour.light4SpecularColourG, Colour.light4SpecularColourB, 1f }));
        GL11.glLightf(GL11.GL_LIGHT3, GL11.GL_LINEAR_ATTENUATION, .001f);

        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glColorMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT_AND_DIFFUSE);

        GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_SPECULAR, BufferFactory.floatBuffer(new float[] { 1.0f, 1.0f, 1.0f, 1.0f }));
        GL11.glMaterialf(GL11.GL_FRONT, GL11.GL_SHININESS, 128f);


        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);

        if (fsGlossId == -1) {
            vsGlossId = loadGlossVertexShader();
            fsGlossId = loadGlossFragmentShader();
            if (pGlossId == -1 && vsGlossId != -1 && fsGlossId != -1) {
                pGlossId = GL20.glCreateProgram();
                GL20.glAttachShader(pGlossId, vsGlossId);
                GL20.glAttachShader(pGlossId, fsGlossId);
                GL20.glLinkProgram(pGlossId);
                GL20.glValidateProgram(pGlossId);
                baseImageLoc = GL20.glGetUniformLocation(pGlossId, "colorMap"); //$NON-NLS-1$
                glossMapLoc = GL20.glGetUniformLocation(pGlossId, "glossMap"); //$NON-NLS-1$
                cubeMapLoc = GL20.glGetUniformLocation(pGlossId, "cubeMap"); //$NON-NLS-1$
                cubeMapMatteLoc = GL20.glGetUniformLocation(pGlossId, "cubeMapMatte"); //$NON-NLS-1$
                cubeMapMetalLoc = GL20.glGetUniformLocation(pGlossId, "cubeMapMetal"); //$NON-NLS-1$
                alphaSwitchLoc = GL20.glGetUniformLocation(pGlossId, "alphaSwitch"); //$NON-NLS-1$
                normalSwitchLoc = GL20.glGetUniformLocation(pGlossId, "normalSwitch"); //$NON-NLS-1$
                noTextureSwitch = GL20.glGetUniformLocation(pGlossId, "noTextureSwitch"); //$NON-NLS-1$
                noGlossMapSwitch = GL20.glGetUniformLocation(pGlossId, "noGlossMapSwitch"); //$NON-NLS-1$
                cubeMapSwitch = GL20.glGetUniformLocation(pGlossId, "cubeMapSwitch"); //$NON-NLS-1$
                noLightSwitch = GL20.glGetUniformLocation(pGlossId, "noLightSwitch"); //$NON-NLS-1$
            }
        }
    }

    /**
     * Draws the scene
     */
    @Override
    public void drawScene() {

        final long start = System.currentTimeMillis();
        final UserSettingState userSettings = WorkbenchManager.getUserSettingState();

        final boolean negDet = c3d.hasNegDeterminant();
        final boolean raytraceMode = c3d.getRenderMode() == 5;

        final GLCanvas canvas = c3d.getCanvas();

        if (!canvas.isCurrent()) {
            canvas.setCurrent();
            GL.setCapabilities(c3d.getCapabilities());
        }

        final Editor3DWindow window = Editor3DWindow.getWindow();

        // MARK OpenGL Draw Scene

        if (raytraceMode) {
            if (skipFrame < 2) {
                skipFrame++;

            } else {
                skipFrame = 0;
                return;
            }
            c3d.getVertexManager().clearVertexNormalCache();
            c3d.getVertexManager().fillVertexNormalCache(c3d.getLockableDatFileReference().getDrawChainStart());
            GL20.glUseProgram(pGlossId);
        } else {
            GL20.glUseProgram(0);
        }

        int state3d = 0;
        boolean isAnaglyph = c3d.isAnaglyph3d() && !raytraceMode;
        GL11.glColorMask(true, !isAnaglyph, !isAnaglyph, true);
        while (true) {

            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);

            Rectangle bounds = c3d.getBounds();
            Rectangle scaledBounds = c3d.getScaledBounds();
            GL11.glViewport(0, 0, scaledBounds.width, scaledBounds.height);
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();
            float viewportWidth = bounds.width / View.PIXEL_PER_LDU / 2.0f;
            float viewportHeight = bounds.height / View.PIXEL_PER_LDU / 2.0f;
            GL11.glOrtho(viewportWidth, -viewportWidth, viewportHeight, -viewportHeight, -c3d.getzNear() * c3d.getZoom(), c3d.getzFar() * c3d.getZoom());

            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadIdentity();

            Matrix4f viewportTransform = new Matrix4f();
            Matrix4f.setIdentity(viewportTransform);

            final float zoom = c3d.getZoom();
            Matrix4f.scale(new Vector3f(zoom, zoom, zoom), viewportTransform, viewportTransform);
            Matrix4f viewportRotation = c3d.getRotation();
            viewportRotation.store(rotation);
            rotation.flip();
            Matrix4f.load(viewportRotation, rotationInv4f);
            ((Matrix4f) rotationInv4f.invert()).store(rotationInv);
            rotationInv.flip();
            Matrix4f.mul(viewportRotation, viewportTransform, viewportTransform);
            Matrix4f viewportTranslation = c3d.getTranslation();
            Matrix4f.mul(viewportTransform, viewportTranslation, viewportTransform);
            viewportTransform.store(viewport);
            c3d.setViewport(viewportTransform);
            viewport.flip();
            GL11.glLoadMatrixf(viewport);

            if (c3d.isAnaglyph3d() && !raytraceMode) {

                Matrix4f viewportRotation2 = new Matrix4f();

                float rx = 0;
                double val = 0d;
                if (zoom <= 1.0E-6)
                    val = Math.PI / zoom / 2000000000f;
                else if (zoom > 1.0E-6 && zoom <= 5.0E-6)
                    val = Math.PI / zoom / 500000000f;
                else if (zoom > 5.0E-6 && zoom <= 1.0E-5)
                    val = Math.PI / zoom / 100000000f;
                else if (zoom > 1.0E-5 && zoom <= 5.0E-5)
                    val = Math.PI / zoom / 100000000f;
                else if (zoom > 5.0E-5 && zoom <= 1.0E-4)
                    val = Math.PI / zoom / 10000000f;
                else if (zoom > 1.0E-4 && zoom <= 5.0E-4)
                    val = Math.PI / zoom / 7000000f;
                else if (zoom > 5.0E-4 && zoom <= 1.0E-3)
                    val = Math.PI / zoom / 7000000f;
                else if (zoom > 1.0E-3 && zoom <= 5.0E-3)
                    val = Math.PI / zoom / 6000000f;
                else if (zoom > 5.0E-3 && zoom <= 1.0E-2)
                    val = Math.PI / zoom / 300000f;
                else if (zoom > 1.0E-2)
                    val = Math.PI / zoom / 200000f;

                switch (state3d) {
                case 0:
                    rx = (float) -val;
                    break;
                case 1:
                    rx = (float) val;
                    break;
                default:
                    break;
                }

                Vector4f yAxis4fRotation = new Vector4f(0f, 1.0f, 0f, 1f);
                Vector4f xAxis4fTranslation = new Vector4f(0f, 0f, 0f, 1f);
                Matrix4f ovrInverse = Matrix4f.invert(viewportRotation, null);
                Matrix4f.transform(ovrInverse, yAxis4fRotation, yAxis4fRotation);
                Matrix4f.transform(ovrInverse, xAxis4fTranslation, xAxis4fTranslation);
                Vector3f yAxis3fRotation = new Vector3f(yAxis4fRotation.x, yAxis4fRotation.y, yAxis4fRotation.z);
                Vector3f xAxis3fTranslation = new Vector3f(xAxis4fTranslation.x, xAxis4fTranslation.y, xAxis4fTranslation.z);
                Matrix4f.rotate(rx, yAxis3fRotation, viewportRotation, viewportRotation2);
                Matrix4f.translate(xAxis3fTranslation, viewportRotation2, viewportRotation2);

                Matrix4f viewportTransform2 = new Matrix4f();
                Matrix4f.setIdentity(viewportTransform2);
                Matrix4f.scale(new Vector3f(zoom, zoom, zoom), viewportTransform2, viewportTransform2);

                Matrix4f.mul(viewportRotation2, viewportTransform2, viewportTransform2);

                viewportTranslation = c3d.getTranslation();
                Matrix4f.mul(viewportTransform2, viewportTranslation, viewportTransform2);
                viewportTransform2.store(viewport);
                viewport.flip();
                GL11.glLoadMatrixf(viewport);

            }

            GL11.glEnable(GL11.GL_CULL_FACE);
            if (negDet) {
                GL11.glFrontFace(GL11.GL_CCW);
            } else {
                GL11.glFrontFace(GL11.GL_CW);
            }

            GL11.glCullFace(GL11.GL_BACK);

            if (c3d.isLightOn())
                GL11.glEnable(GL11.GL_LIGHTING);

            c3d.setDrawingSolidMaterials(true);
            c3d.getLockableDatFileReference().draw(c3d);

            if (raytraceMode) {
                final float scaleFactor = (float) userSettings.getViewportScaleFactor();
                Rectangle b = c3d.getCanvas().getBounds();
                final int w =  (int) (b.width * scaleFactor);
                final int h =  (int) (b.height * scaleFactor);
                FloatBuffer pixels = BufferUtils.createFloatBuffer(w * h * 4);
                GL11.glReadPixels(0, 0, w, h, GL11.GL_RGBA, GL11.GL_FLOAT, pixels);
                pixels.position(0);
                float[] arr = new float[pixels.capacity()];
                pixels.get(arr);
                solidColours[0] = arr;
                // NLogger.debug(getClass(), arr[(int) (w * 50.5f * 4)] + " " + arr[(int) (w * 50.5f * 4 + 1)] + " " + arr[(int) (w * 50.5f * 4 + 2)] + " " + arr[(int) (w * 50.5f * 4 + 3)]);  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
            }

            if (window.getCompositePrimitive().isDoingDND()) {
                final Primitive p = c3d.getDraggedPrimitive();
                if (p != null) {
                    GL11.glDisable(GL11.GL_LIGHTING);
                    Vector4f cur = c3d.getCursorSnapped3D();
                    p.draw(cur.x, cur.y, cur.z);
                    if (c3d.isLightOn())
                        GL11.glEnable(GL11.GL_LIGHTING);
                }
            } else {
                c3d.setDraggedPrimitive(null);
            }
            c3d.setDrawingSolidMaterials(false);
            c3d.getLockableDatFileReference().draw(c3d);

            if (raytraceMode) {
                final float scaleFactor = (float) userSettings.getViewportScaleFactor();
                Rectangle b = c3d.getCanvas().getBounds();
                final float w =  b.width * scaleFactor;
                final float h =  b.height * scaleFactor;
                FloatBuffer pixels = BufferUtils.createFloatBuffer((int) (w * h * 4));
                GL11.glReadPixels(0, 0, (int) w, (int) h, GL11.GL_RGBA, GL11.GL_FLOAT, pixels);
                pixels.position(0);
                float[] arr = new float[pixels.capacity()];
                pixels.get(arr);
                transparentColours[0] = arr;
                cWidth[0] = w;
                cHeight[0] = h;
                // NLogger.debug(getClass(), "Trans: " + arr[(int) (w * 50.5f * 4)] + " " + arr[(int) (w * 50.5f * 4 + 1)] + " " + arr[(int) (w * 50.5f * 4 + 2)] + " " + arr[(int) (w * 50.5f * 4 + 3)]);  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_DEPTH_TEST);

                GL20.glUseProgram(0);

                if (lock.tryLock()) {
                    try {
                        if (renderedPoints[0] != null) {
                            GL11.glPushMatrix();
                            GL11.glLoadIdentity();
                            final float xf = 2f * viewportWidth;
                            final float yf = 2f * viewportHeight;
                            GL11.glTranslatef(-viewportWidth, -viewportHeight, 0f);
                            GL11.glScalef(1f / w * xf, 1f / h * yf, 1f);
                            // FIXME Needs adjustments for negative determinants!
                            for (float[] p : renderedPoints[0]) {
                                GL11.glBegin(GL11.GL_QUADS);
                                GL11.glColor3f(p[0], p[1], p[2]);
                                GL11.glVertex3f(p[3], p[4], 0f);
                                GL11.glVertex3f(p[5], p[6], 0f);
                                GL11.glVertex3f(p[7], p[8], 0f);
                                GL11.glVertex3f(p[9], p[10], 0f);
                                GL11.glEnd();
                            }
                            GL11.glPopMatrix();
                        }
                    } finally {
                        lock.unlock();
                    }
                }

                if (raytracer == null || !raytracer.isAlive()) {
                    raytracer = new Thread(() -> {
                        while(alive.get()) {

                            needData.set(1);
                            int counter = 0;
                            while (needData.get() < 2) {
                                counter++;
                                if (counter > 100) {
                                    NLogger.debug(getClass(), "Stopped raytracer."); //$NON-NLS-1$
                                    return;
                                }
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException ie) {
                                    Thread.currentThread().interrupt();
                                    throw new LDPartEditorException(ie);
                                }
                            }

                            final int cs = solidColours[0].length;
                            final float[] sc = Arrays.copyOf(solidColours[0], cs);
                            final float[] tc = Arrays.copyOf(transparentColours[0], cs);
                            final int cw = (int) cWidth[0];
                            final int ch = (int) cHeight[0];

                            final float[] ray;
                            final Vector3f ray3f;
                            final Vector3f ray3f2 = new Vector3f(-20f, -20f, 100f);
                            ray3f2.normalise();
                            final Matrix4f vInverse = c3d.getViewportInverse();
                            final Matrix4f vM = c3d.getViewport();
                            final float z = 100f;
                            {
                                // FIXME Negative Determinant check is needed somewhere???
                                Vector4f zAxis4f = new Vector4f(0, 0, 1f, 1f);
                                Matrix4f ovrInverse2 = Matrix4f.invert(c3d.getRotation(), null);
                                Matrix4f.transform(ovrInverse2, zAxis4f, zAxis4f);
                                Vector4f ray2 = (Vector4f) new Vector4f(zAxis4f.x, zAxis4f.y, zAxis4f.z, 0f).normalise();
                                ray = new float[]{ray2.x, ray2.y, ray2.z};
                                ray3f = new Vector3f(ray[0], ray[1], ray[2]);
                            }

                            needData.decrementAndGet();
                            needData.decrementAndGet();

                            NLogger.debug(getClass(), "Initialised raytracer."); //$NON-NLS-1$
                            final boolean lights = c3d.isLightOn();
                            // Read triangles and quads
                            final List<float[]> tris = new ArrayList<>();
                            {
                                Map<GData4, Vertex[]> quads = c3d.getLockableDatFileReference().getVertexManager().getQuads();
                                Map<GData3, Vertex[]> tris2 = c3d.getLockableDatFileReference().getVertexManager().getTriangles();
                                for (Entry<GData3, Vertex[]> entry : tris2.entrySet()) {
                                    GData3 g = entry.getKey();
                                    Vertex[] v = entry.getValue();
                                    Vector4f[] nv = new Vector4f[3];
                                    {
                                        boolean notShown = true;
                                        float maxX = -Float.MAX_VALUE;
                                        float minX = Float.MAX_VALUE;
                                        float maxY = -Float.MAX_VALUE;
                                        float minY = Float.MAX_VALUE;
                                        for(int i = 0; i < 3; i++) {
                                            Vector4f sz = getScreenZFrom3D(v[i].x, v[i].y, v[i].z, cw, ch, vM);
                                            nv[i] = sz;
                                            maxX = Math.max(maxX, sz.x);
                                            minX = Math.min(minX, sz.x);
                                            maxY = Math.max(maxY, sz.y);
                                            minY = Math.min(minY, sz.y);
                                        }

                                        Rectangle cbounds = new Rectangle(0, 0, cw, ch);
                                        Rectangle boundingBox = new Rectangle((int) minX, (int) minY, (int) (maxX - minX), (int) (maxY - minY));

                                        if (boundingBox.intersects(cbounds) || boundingBox.contains(0, 0) || boundingBox.contains(cbounds.width, cbounds.height) || boundingBox.contains(cbounds.width, 0)
                                                || boundingBox.contains(0, cbounds.height) || cbounds.contains(boundingBox.x, boundingBox.y) || cbounds.contains(boundingBox.x, boundingBox.y + boundingBox.height)
                                                || cbounds.contains(boundingBox.x + boundingBox.width, boundingBox.y) || cbounds.contains(boundingBox.x + boundingBox.width, boundingBox.y + boundingBox.height)) {
                                            notShown = false;
                                        }

                                        if (notShown) continue;
                                    }

                                    int c = g.colourNumber;
                                    float rv = g.r;
                                    float gv = g.g;
                                    float bv = g.b;
                                    float av = g.a;
                                    GData1 p = g.parent;
                                    while (c == 16 && !p.equals(View.DUMMY_REFERENCE)) {
                                        c = p.colourNumber;
                                        rv = p.r;
                                        gv = p.g;
                                        bv = p.b;
                                        av = p.a;
                                        p = p.parent;
                                    }

                                    Vector3f normal = new Vector3f(
                                            (nv[2].y - nv[0].y) * (nv[1].z - nv[0].z) - (nv[2].z - nv[0].z) * (nv[1].y - nv[0].y),
                                            (nv[2].z - nv[0].z) * (nv[1].x - nv[0].x) - (nv[2].x - nv[0].x) * (nv[1].z - nv[0].z),
                                            (nv[2].x - nv[0].x) * (nv[1].y - nv[0].y) - (nv[2].y - nv[0].y) * (nv[1].x - nv[0].x));
                                    if (normal.lengthSquared() > 0f) {
                                        normal.normalise();
                                        normal.negate();
                                    } else {
                                        normal.setX(0f);
                                        normal.setY(0f);
                                        normal.setZ(1f);
                                    }
                                    float[] nt = new float[]{
                                            v[0].x, v[0].y, v[0].z,
                                            v[1].x, v[1].y, v[1].z,
                                            v[2].x, v[2].y, v[2].z,
                                            normal.x,
                                            normal.y,
                                            normal.z,
                                            rv, gv, bv, av, c
                                    };
                                    tris.add(nt);



                                }
                                for (Entry<GData4, Vertex[]> entry : quads.entrySet()) {
                                    GData4 g = entry.getKey();
                                    Vertex[] v = entry.getValue();
                                    Vector4f[] nv = new Vector4f[4];
                                    {
                                        boolean notShown = true;
                                        float maxX = -Float.MAX_VALUE;
                                        float minX = Float.MAX_VALUE;
                                        float maxY = -Float.MAX_VALUE;
                                        float minY = Float.MAX_VALUE;
                                        for(int i = 0; i < 4; i++) {
                                            Vector4f sz = getScreenZFrom3D(v[i].x, v[i].y, v[i].z, cw, ch, vM);
                                            nv[i] = sz;
                                            maxX = Math.max(maxX, sz.x);
                                            minX = Math.min(minX, sz.x);
                                            maxY = Math.max(maxY, sz.y);
                                            minY = Math.min(minY, sz.y);
                                        }

                                        Rectangle cbounds = new Rectangle(0, 0, cw, ch);
                                        Rectangle boundingBox = new Rectangle((int) minX, (int) minY, (int) (maxX - minX), (int) (maxY - minY));

                                        if (boundingBox.intersects(cbounds) || boundingBox.contains(0, 0) || boundingBox.contains(cbounds.width, cbounds.height) || boundingBox.contains(cbounds.width, 0)
                                                || boundingBox.contains(0, cbounds.height) || cbounds.contains(boundingBox.x, boundingBox.y) || cbounds.contains(boundingBox.x, boundingBox.y + boundingBox.height)
                                                || cbounds.contains(boundingBox.x + boundingBox.width, boundingBox.y) || cbounds.contains(boundingBox.x + boundingBox.width, boundingBox.y + boundingBox.height)) {
                                            notShown = false;
                                        }

                                        if (notShown) continue;
                                    }

                                    final Vector3f[] normals = new Vector3f[] { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
                                    {
                                        final Vector3f[] lineVectors = new Vector3f[] { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
                                        Vector3f.sub(new Vector3f(nv[1].x, nv[1].y, nv[1].z), new Vector3f(nv[0].x, nv[0].y, nv[0].z), lineVectors[0]);
                                        Vector3f.sub(new Vector3f(nv[2].x, nv[2].y, nv[2].z), new Vector3f(nv[1].x, nv[1].y, nv[1].z), lineVectors[1]);
                                        Vector3f.sub(new Vector3f(nv[3].x, nv[3].y, nv[3].z), new Vector3f(nv[2].x, nv[2].y, nv[2].z), lineVectors[2]);
                                        Vector3f.sub(new Vector3f(nv[0].x, nv[0].y, nv[0].z), new Vector3f(nv[3].x, nv[3].y, nv[3].z), lineVectors[3]);
                                        Vector3f.cross(lineVectors[0], lineVectors[1], normals[0]);
                                        Vector3f.cross(lineVectors[1], lineVectors[2], normals[1]);
                                        Vector3f.cross(lineVectors[2], lineVectors[3], normals[2]);
                                        Vector3f.cross(lineVectors[3], lineVectors[0], normals[3]);
                                    }
                                    Vector3f normal = new Vector3f();
                                    for (int i = 0; i < 4; i++) {
                                        Vector3f.add(normals[i], normal, normal);
                                    }
                                    if (normal.lengthSquared() > 0f) {
                                        normal.normalise();
                                    } else {
                                        normal.setX(0f);
                                        normal.setY(0f);
                                        normal.setZ(1f);
                                    }

                                    int c = g.colourNumber;
                                    float rv = g.r;
                                    float gv = g.g;
                                    float bv = g.b;
                                    float av = g.a;
                                    GData1 p = g.parent;
                                    while (c == 16 && !p.equals(View.DUMMY_REFERENCE)) {
                                        c = p.colourNumber;
                                        rv = p.r;
                                        gv = p.g;
                                        bv = p.b;
                                        av = p.a;
                                        p = p.parent;
                                    }

                                    float[] nt = new float[]{
                                            v[0].x, v[0].y, v[0].z,
                                            v[1].x, v[1].y, v[1].z,
                                            v[2].x, v[2].y, v[2].z,
                                            normal.x,
                                            normal.y,
                                            normal.z,
                                            rv, gv, bv, av, c
                                    };
                                    tris.add(nt);
                                    float[] nt2 = new float[]{
                                            v[2].x, v[2].y, v[2].z,
                                            v[3].x, v[3].y, v[3].z,
                                            v[0].x, v[0].y, v[0].z,
                                            normal.x,
                                            normal.y,
                                            normal.z,
                                            rv, gv, bv, av, c
                                    };
                                    tris.add(nt2);
                                }
                            }

                            NLogger.debug(getClass(), "Started raytracer."); //$NON-NLS-1$

                            final List<float[]> points = new ArrayList<>(10000);


                            final List<float[]> speckles = new LinkedList<>();
                            final List<float[]> glitters = new LinkedList<>();
                            final Map<float[], Long> specklesCreation = new HashMap<>();
                            final Map<float[], Long> glittersCreation = new HashMap<>();

                            // Light positions
                            final Vector3f lp1 = new Vector3f(-2.0f, -2.0f, 2.0f);
                            final Vector3f lp2 = new Vector3f(-2.0f, 2.0f, 2.0f);
                            final Vector3f lp3 = new Vector3f(2.0f, -2.0f, 2.0f);
                            final Vector3f lp4 = new Vector3f(2.0f, 2.0f, 2.0f);

                            {

                                final Lock tmpLock = new ReentrantLock();

                                final int chunks = Math.max(View.NUM_CORES - 1, 1);
                                final Thread[] threads = new Thread[chunks];
                                for (int j = 0; j < chunks; ++j) {
                                    final int[] startIndices = new int[] { j };
                                    final int[] ti = new int[] { startIndices[0] * 4 * cw};
                                    threads[j] = new Thread(() -> {
                                        final SortedMap<Float, float[]>  zSort = new TreeMap<>();
                                        final SortedMap<Float, Vector4f>  hitSort = new TreeMap<>();
                                        final Random tRnd = new Random(12348729642643L * startIndices[0]);
                                        List<float[]> points2 = new ArrayList<>(10000 / chunks);
                                        final PowerRay pr = new PowerRay();
                                        final int s = startIndices[0];
                                        int i = ti[0];
                                        int skip = 0;
                                        for (int y = s; y < ch; y += chunks) {
                                            final int sy = ch - y - 1;
                                            for (int x = 0; x < cw; x++) {
                                                final int sx = cw - x - 1;
                                                float rS = sc[i];
                                                float gS = sc[i + 1];
                                                float bS = sc[i + 2];
                                                float rT = tc[i];
                                                float gT = tc[i + 1];
                                                float bT = tc[i + 2];
                                                if (rS != rT || gS != gT || bS != bT) {
                                                    zSort.clear();
                                                    hitSort.clear();
                                                    final Vector4f posv = get3DCoordinatesFromScreen(x, y, z, cw, ch, vInverse);
                                                    for (float[] tri : tris) {
                                                        float[] zHit = pr.triangleIntersect(posv, ray, tri);
                                                        if (zHit.length != 0) {
                                                            Vector4f sz = getScreenZFrom3D(zHit[0], zHit[1], zHit[2], cw, ch, vM);
                                                            hitSort.put(sz.z, sz);
                                                            zSort.put(sz.z, tri);
                                                        }
                                                    }


                                                    final int size = zSort.size();

                                                    switch(size) {
                                                    case 0:
                                                    {
                                                        float[] point = new float[11];
                                                        point[0] = rT;
                                                        point[1] = gT;
                                                        point[2] = bT;
                                                        point[3] = sx;
                                                        point[4] = sy;
                                                        point[5] = sx;
                                                        point[6] = sy + 1f;
                                                        point[7] = sx + 1f;
                                                        point[8] = sy + 1f;
                                                        point[9] = sx + 1f;
                                                        point[10] = sy;
                                                        points2.add(point);
                                                        break;
                                                    }
                                                    case 1:
                                                    {
                                                        float[] ze = zSort.get(zSort.firstKey());
                                                        GColour c = LDConfig.getColour((int) ze[16]);
                                                        GColourType ct = c.getType();
                                                        if (ct == null) {
                                                            float[] point = new float[11];
                                                            float a = ze[15];
                                                            float oneMinusAlpha = 1f - a;
                                                            point[0] = rT * a + rS * oneMinusAlpha;
                                                            point[1] = gT * a + gS * oneMinusAlpha;
                                                            point[2] = bT * a + bS * oneMinusAlpha;
                                                            point[3] = sx;
                                                            point[4] = sy;
                                                            point[5] = sx;
                                                            point[6] = sy + 1f;
                                                            point[7] = sx + 1f;
                                                            point[8] = sy + 1f;
                                                            point[9] = sx + 1f;
                                                            point[10] = sy;
                                                            points2.add(point);
                                                        } else {
                                                            // Compute light (with specular!)
                                                            float light = 0f;
                                                            float lightSpecular = 0f;
                                                            final Vector3f normal = new Vector3f(ze[9], ze[10], ze[11]);
                                                            final Vector4f pos = hitSort.get(hitSort.firstKey());
                                                            if (lights) {
                                                                Vector3f position = new Vector3f(pos.x, pos.y, pos.z);
                                                                Vector3f lightDir1 = Vector3f.sub(lp1, position, null);
                                                                Vector3f lightDir2 = Vector3f.sub(lp2, position, null);
                                                                Vector3f lightDir3 = Vector3f.sub(lp3, position, null);
                                                                Vector3f lightDir4 = Vector3f.sub(lp4, position, null);
                                                                lightDir1.normalise();
                                                                lightDir2.normalise();
                                                                lightDir3.normalise();
                                                                lightDir4.normalise();
                                                                // attenuation and light direction
                                                                // ambient + diffuse
                                                                light = 0.09f; // Ambient
                                                                light += 0.80f * .6f * Math.max(Vector3f.dot(normal, lightDir1), 0.0);
                                                                light += 0.25f * .6f * Math.max(Vector3f.dot(normal, lightDir2), 0.0);
                                                                light += 0.25f * .6f * Math.max(Vector3f.dot(normal, lightDir3), 0.0);
                                                                light += 0.25f * .6f * Math.max(Vector3f.dot(normal, lightDir4), 0.0);
                                                                lightSpecular += Math.pow(Math.max(Vector3f.dot(normal, ray3f2), 0.0), 128f);
                                                            }
                                                            float colourR = ze[12];
                                                            float colourG = ze[13];
                                                            float colourB = ze[14];
                                                            switch (ct.type()) {
                                                            case PEARL:
                                                            {
                                                                Vector3f normal2 = new Vector3f(
                                                                        normal.x * (.8f + .2f * tRnd.nextFloat()),
                                                                        normal.y * (.8f + .2f * tRnd.nextFloat()),
                                                                        normal.z * (.8f + .2f * tRnd.nextFloat())
                                                                        );
                                                                float sp = Vector3f.dot(normal2, ray3f);
                                                                float spI = 1f - sp;
                                                                Vector3f v = Vector3f.cross(ray3f, normal2, null);
                                                                Random rnd = new Random((long) (129642643f * (1f + colourR) * (1f + colourG) * (1f + colourB)));
                                                                colourR = colourR + Math.abs(sp + v.x * spI) * rnd.nextFloat() / 4f;
                                                                colourG = colourG + Math.abs(sp + v.y * spI) * rnd.nextFloat() / 4f;
                                                                colourB = colourB + Math.abs(sp + v.z * spI) * rnd.nextFloat() / 4f;
                                                                if (lights) {
                                                                    colourR = (light + lightSpecular) / 4f + colourR;
                                                                    colourG = (light + lightSpecular) / 4f + colourG;
                                                                    colourB = (light + lightSpecular) / 4f + colourB;
                                                                }
                                                                break;
                                                            }
                                                            case GLITTER:
                                                            {
                                                                float a = ze[15];
                                                                GCGlitter type = (GCGlitter) ct;
                                                                final int sSize = glitters.size();
                                                                float radi = 1000f * type.getMinSize() + (type.getMaxSize() - type.getMinSize()) * tRnd.nextFloat();
                                                                boolean hit = false;
                                                                final int glitterCount = 30000;
                                                                boolean buildable = sSize < glitterCount;
                                                                Vector4f v = get3DCoordinatesFromScreen(pos.x, pos.y, pos.z, vInverse);
                                                                final float px = v.x;
                                                                final float py = v.y;
                                                                final float pz = v.z;
                                                                float[] newGlitter = new float[]{px + tRnd.nextFloat() * radi, py + tRnd.nextFloat() * radi, pz + tRnd.nextFloat() * radi, radi};
                                                                lockGlitter.lock();
                                                                try {
                                                                    final long now = System.currentTimeMillis();
                                                                    for (Iterator<float[]> iterator = glitters.iterator(); iterator.hasNext();) {
                                                                        float[] glitter = iterator.next();
                                                                        final long age = now - glittersCreation.get(glitter);
                                                                        if (age > 500L) {
                                                                            iterator.remove();
                                                                        } else {
                                                                            float dx = glitter[0] - px;
                                                                            float dy = glitter[1] - py;
                                                                            float dz = glitter[2] - pz;
                                                                            float dist = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
                                                                            if (dist < glitter[3]) {
                                                                                hit = true;
                                                                                break;
                                                                            }
                                                                            if (buildable && dist * 0.5 < radi) {
                                                                                buildable = false;
                                                                            }
                                                                        }
                                                                    }
                                                                    if (buildable && !hit) {
                                                                        glittersCreation.put(newGlitter, System.currentTimeMillis());
                                                                        glitters.add(newGlitter);
                                                                    }
                                                                } finally {
                                                                    lockGlitter.unlock();
                                                                }

                                                                float vari = tRnd.nextFloat();
                                                                if (hit) {
                                                                    lightSpecular *= 1f + vari;
                                                                    colourR = type.getR();
                                                                    colourG = type.getG();
                                                                    colourB = type.getB();
                                                                    a = 1f;
                                                                } else {
                                                                    colourR = colourR * .8f + vari * .2f * colourR;
                                                                    colourG = colourG * .8f + vari * .2f * colourG;
                                                                    colourB = colourB * .8f + vari * .2f * colourB;
                                                                }
                                                                float oneMinusAlpha = 1f - a;
                                                                if (lights) {
                                                                    float resLight = light + lightSpecular * 2f;
                                                                    colourR = resLight + colourR;
                                                                    colourG = resLight + colourG;
                                                                    colourB = resLight + colourB;
                                                                }
                                                                colourR = colourR * a + rS * oneMinusAlpha;
                                                                colourG = colourG * a + gS * oneMinusAlpha;
                                                                colourB = colourB * a + bS * oneMinusAlpha;
                                                                break;
                                                            }
                                                            case SPECKLE:
                                                            {
                                                                float a = ze[15];
                                                                GCSpeckle type = (GCSpeckle) ct;
                                                                final int sSize = speckles.size();
                                                                float radi = 1000f * type.getMinSize() + (type.getMaxSize() - type.getMinSize()) * tRnd.nextFloat();
                                                                boolean hit = false;
                                                                final int speckleCount = 30000;
                                                                boolean buildable = sSize < speckleCount;
                                                                Vector4f v = get3DCoordinatesFromScreen(pos.x, pos.y, pos.z, vInverse);
                                                                final float px = v.x;
                                                                final float py = v.y;
                                                                final float pz = v.z;
                                                                float[] newSpeckle = new float[]{px + tRnd.nextFloat() * radi, py + tRnd.nextFloat() * radi, pz + tRnd.nextFloat() * radi, radi};
                                                                lockSpeckle.lock();
                                                                try {
                                                                    final long now = System.currentTimeMillis();
                                                                    for (Iterator<float[]> iterator = speckles.iterator(); iterator.hasNext();) {
                                                                        float[] speckle = iterator.next();
                                                                        final long age = now - specklesCreation.get(speckle);
                                                                        if (age > 500L) {
                                                                            iterator.remove();
                                                                        } else {
                                                                            double dx = speckle[0] - px;
                                                                            double dy = speckle[1] - py;
                                                                            double dz = speckle[2] - pz;
                                                                            float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                                                                            if (dist < speckle[3]) {
                                                                                hit = true;
                                                                                break;
                                                                            }
                                                                            if (buildable && dist * 0.5 < radi) {
                                                                                buildable = false;
                                                                            }
                                                                        }
                                                                    }
                                                                    if (buildable && !hit) {
                                                                        specklesCreation.put(newSpeckle, System.currentTimeMillis());
                                                                        speckles.add(newSpeckle);
                                                                    }
                                                                } finally {
                                                                    lockSpeckle.unlock();
                                                                }

                                                                float vari = tRnd.nextFloat();
                                                                if (hit) {
                                                                    colourR = (type.getR() * .8f + vari * .2f * type.getR()) * .5f + colourR * .5f;
                                                                    colourG = (type.getG() * .8f + vari * .2f * type.getG()) * .5f + colourG * .5f;
                                                                    colourB = (type.getB() * .8f + vari * .2f * type.getB()) * .5f + colourB * .5f;
                                                                    a = 1f;
                                                                } else {
                                                                    colourR = colourR * .8f + vari * .2f * colourR;
                                                                    colourG = colourG * .8f + vari * .2f * colourG;
                                                                    colourB = colourB * .8f + vari * .2f * colourB;
                                                                }
                                                                float oneMinusAlpha = 1f - a;
                                                                if (lights) {
                                                                    float resLight = light + lightSpecular * 2f;
                                                                    colourR = resLight + colourR;
                                                                    colourG = resLight + colourG;
                                                                    colourB = resLight + colourB;
                                                                }
                                                                colourR = colourR * a + rS * oneMinusAlpha;
                                                                colourG = colourG * a + gS * oneMinusAlpha;
                                                                colourB = colourB * a + bS * oneMinusAlpha;
                                                                break;
                                                            }
                                                            default:
                                                                break;
                                                            }

                                                            float[] point = new float[11];
                                                            point[0] = colourR;
                                                            point[1] = colourG;
                                                            point[2] = colourB;
                                                            point[3] = sx;
                                                            point[4] = sy;
                                                            point[5] = sx;
                                                            point[6] = sy + 1f;
                                                            point[7] = sx + 1f;
                                                            point[8] = sy + 1f;
                                                            point[9] = sx + 1f;
                                                            point[10] = sy;
                                                            points2.add(point);
                                                        }
                                                        break;
                                                    }
                                                    default:
                                                        float[] point = new float[11];
                                                        int k = 0;

                                                        point[0] = 1f;
                                                        point[1] = 1f;
                                                        point[2] = 1f;

                                                        for (Entry<Float, float[]> entry : zSort.entrySet()) {
                                                            Float f = entry.getKey();
                                                            k++;
                                                            float[] ze = entry.getValue();
                                                            float colourA = ze[15];
                                                            float colourR = ze[12];
                                                            float colourG = ze[13];
                                                            float colourB = ze[14];

                                                            GColour c = LDConfig.getColour((int) ze[16]);
                                                            GColourType ct = c.getType();
                                                            if (ct == null || GCType.hasCubeMap(ct.type())) {
                                                                float oneMinusAlpha = 1f - colourA;
                                                                if (colourA == 1f) {
                                                                    point[0] = rS;
                                                                    point[1] = gS;
                                                                    point[2] = bS;
                                                                } else if (k < size) {
                                                                    // Compute light (with specular!)
                                                                    float lightSpecular = 0f;
                                                                    if (lights) {
                                                                        Vector4f pos = hitSort.get(f);
                                                                        Vector3f position = new Vector3f(pos.x, pos.y, pos.z);
                                                                        Vector3f normal = new Vector3f(ze[9], ze[10], ze[11]);
                                                                        Vector3f lightDir1 = Vector3f.sub(lp1, position, null);
                                                                        Vector3f lightDir2 = Vector3f.sub(lp2, position, null);
                                                                        Vector3f lightDir3 = Vector3f.sub(lp3, position, null);
                                                                        Vector3f lightDir4 = Vector3f.sub(lp4, position, null);
                                                                        lightDir1.normalise();
                                                                        lightDir2.normalise();
                                                                        lightDir3.normalise();
                                                                        lightDir4.normalise();
                                                                        // attenuation and light direction
                                                                        // ambient + diffuse
                                                                        float light = 0.09f; // Ambient
                                                                        light += 0.80f * .6f * Math.max(Vector3f.dot(normal, lightDir1), 0.0);
                                                                        light += 0.25f * .6f * Math.max(Vector3f.dot(normal, lightDir2), 0.0);
                                                                        light += 0.25f * .6f * Math.max(Vector3f.dot(normal, lightDir3), 0.0);
                                                                        light += 0.25f * .6f * Math.max(Vector3f.dot(normal, lightDir4), 0.0);
                                                                        lightSpecular += Math.pow(Math.max(Vector3f.dot(normal, ray3f2), 0.0), 128f);
                                                                        // compute final color
                                                                        colourR = colourR + light;
                                                                        colourG = colourG + light;
                                                                        colourB = colourB + light;
                                                                    }

                                                                    point[0] = (colourR + lightSpecular) * colourA + point[0] * oneMinusAlpha;
                                                                    point[1] = (colourG + lightSpecular)  * colourA + point[1] * oneMinusAlpha;
                                                                    point[2] = (colourB + lightSpecular) * colourA + point[2] * oneMinusAlpha;

                                                                } else {
                                                                    float lightSpecular = 0f;
                                                                    Vector3f normal = new Vector3f(ze[9], ze[10], ze[11]);
                                                                    lightSpecular += Math.pow(Math.max(Vector3f.dot(normal, ray3f2), 0.0), 128f);
                                                                    point[0] = (rT + lightSpecular) * colourA + point[0] * oneMinusAlpha;
                                                                    point[1] = (gT + lightSpecular) * colourA + point[1] * oneMinusAlpha;
                                                                    point[2] = (bT + lightSpecular) * colourA + point[2] * oneMinusAlpha;
                                                                }
                                                            } else {
                                                                // Compute light (with specular!)
                                                                float light = 0f;
                                                                float lightSpecular = 0f;
                                                                final Vector3f normal = new Vector3f(ze[9], ze[10], ze[11]);
                                                                final Vector4f pos = hitSort.get(hitSort.firstKey());
                                                                if (lights) {
                                                                    Vector3f position = new Vector3f(pos.x, pos.y, pos.z);
                                                                    Vector3f lightDir1 = Vector3f.sub(lp1, position, null);
                                                                    Vector3f lightDir2 = Vector3f.sub(lp2, position, null);
                                                                    Vector3f lightDir3 = Vector3f.sub(lp3, position, null);
                                                                    Vector3f lightDir4 = Vector3f.sub(lp4, position, null);
                                                                    lightDir1.normalise();
                                                                    lightDir2.normalise();
                                                                    lightDir3.normalise();
                                                                    lightDir4.normalise();
                                                                    // attenuation and light direction
                                                                    // ambient + diffuse
                                                                    light = 0.09f; // Ambient
                                                                    light += 0.80f * .6f * Math.max(Vector3f.dot(normal, lightDir1), 0.0);
                                                                    light += 0.25f * .6f * Math.max(Vector3f.dot(normal, lightDir2), 0.0);
                                                                    light += 0.25f * .6f * Math.max(Vector3f.dot(normal, lightDir3), 0.0);
                                                                    light += 0.25f * .6f * Math.max(Vector3f.dot(normal, lightDir4), 0.0);
                                                                    lightSpecular += Math.pow(Math.max(Vector3f.dot(normal, ray3f2), 0.0), 128f);
                                                                }
                                                                switch (ct.type()) {
                                                                case PEARL:
                                                                {
                                                                    Vector3f normal2 = new Vector3f(
                                                                            normal.x * (.8f + .2f * tRnd.nextFloat()),
                                                                            normal.y * (.8f + .2f * tRnd.nextFloat()),
                                                                            normal.z * (.8f + .2f * tRnd.nextFloat())
                                                                            );
                                                                    float sp = Vector3f.dot(normal2, ray3f);
                                                                    float spI = 1f - sp;
                                                                    Vector3f v = Vector3f.cross(ray3f, normal2, null);
                                                                    Random rnd = new Random((long) (129642643f * (1f + colourR) * (1f + colourG) * (1f + colourB)));
                                                                    colourR = colourR + Math.abs(sp + v.x * spI) * rnd.nextFloat() / 4f;
                                                                    colourG = colourG + Math.abs(sp + v.y * spI) * rnd.nextFloat() / 4f;
                                                                    colourB = colourB + Math.abs(sp + v.z * spI) * rnd.nextFloat() / 4f;
                                                                    if (lights) {
                                                                        colourR = (light + lightSpecular) / 4f + colourR;
                                                                        colourG = (light + lightSpecular) / 4f + colourG;
                                                                        colourB = (light + lightSpecular) / 4f + colourB;
                                                                    }
                                                                    break;
                                                                }
                                                                case GLITTER:
                                                                {
                                                                    GCGlitter type = (GCGlitter) ct;
                                                                    final int sSize = glitters.size();
                                                                    float radi = 1000f * type.getMinSize() + (type.getMaxSize() - type.getMinSize()) * tRnd.nextFloat();
                                                                    boolean hit = false;
                                                                    final int glitterCount = 30000;
                                                                    boolean buildable = sSize < glitterCount;
                                                                    Vector4f v = get3DCoordinatesFromScreen(pos.x, pos.y, pos.z, vInverse);
                                                                    final float px = v.x;
                                                                    final float py = v.y;
                                                                    final float pz = v.z;
                                                                    float[] newGlitter = new float[]{px + tRnd.nextFloat() * radi, py + tRnd.nextFloat() * radi, pz + tRnd.nextFloat() * radi, radi};
                                                                    lockGlitter.lock();
                                                                    try {
                                                                        final long now = System.currentTimeMillis();
                                                                        for (Iterator<float[]> iterator = glitters.iterator(); iterator.hasNext();) {
                                                                            float[] glitter = iterator.next();
                                                                            final long age = now - glittersCreation.get(glitter);
                                                                            if (age > 500L) {
                                                                                iterator.remove();
                                                                            } else {
                                                                                float dx = glitter[0] - px;
                                                                                float dy = glitter[1] - py;
                                                                                float dz = glitter[2] - pz;
                                                                                float dist = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
                                                                                if (dist < glitter[3]) {
                                                                                    hit = true;
                                                                                    break;
                                                                                }
                                                                                if (buildable && dist * 0.5 < radi) {
                                                                                    buildable = false;
                                                                                }
                                                                            }
                                                                        }
                                                                        if (buildable && !hit) {
                                                                            glittersCreation.put(newGlitter, System.currentTimeMillis());
                                                                            glitters.add(newGlitter);
                                                                        }
                                                                    } finally {
                                                                        lockGlitter.unlock();
                                                                    }

                                                                    float vari = tRnd.nextFloat();
                                                                    if (hit) {
                                                                        lightSpecular *= 1f + vari;
                                                                        colourR = type.getR();
                                                                        colourG = type.getG();
                                                                        colourB = type.getB();
                                                                        colourA = 1f;
                                                                    } else {
                                                                        colourR = colourR * .8f + vari * .2f * colourR;
                                                                        colourG = colourG * .8f + vari * .2f * colourG;
                                                                        colourB = colourB * .8f + vari * .2f * colourB;
                                                                    }
                                                                    float oneMinusAlpha = 1f - colourA;
                                                                    if (lights) {
                                                                        float resLight = light + lightSpecular * 2f;
                                                                        colourR = resLight + colourR;
                                                                        colourG = resLight + colourG;
                                                                        colourB = resLight + colourB;
                                                                    }
                                                                    colourR = colourR * colourA + point[0] * oneMinusAlpha;
                                                                    colourG = colourG * colourA + point[1] * oneMinusAlpha;
                                                                    colourB = colourB * colourA + point[2] * oneMinusAlpha;
                                                                    break;
                                                                }
                                                                case SPECKLE:
                                                                {
                                                                    GCSpeckle type = (GCSpeckle) ct;
                                                                    final int sSize = speckles.size();
                                                                    float radi = 1000f * type.getMinSize() + (type.getMaxSize() - type.getMinSize()) * tRnd.nextFloat();
                                                                    boolean hit = false;
                                                                    final int speckleCount = 30000;
                                                                    boolean buildable = sSize < speckleCount;
                                                                    Vector4f v = get3DCoordinatesFromScreen(pos.x, pos.y, pos.z, vInverse);
                                                                    final float px = v.x;
                                                                    final float py = v.y;
                                                                    final float pz = v.z;
                                                                    float[] newSpeckle = new float[]{px + tRnd.nextFloat() * radi, py + tRnd.nextFloat() * radi, pz + tRnd.nextFloat() * radi, radi};
                                                                    lockSpeckle.lock();
                                                                    try {
                                                                        final long now = System.currentTimeMillis();
                                                                        for (Iterator<float[]> iterator = speckles.iterator(); iterator.hasNext();) {
                                                                            float[] speckle = iterator.next();
                                                                            final long age = now - specklesCreation.get(speckle);
                                                                            if (age > 500L) {
                                                                                iterator.remove();
                                                                            } else {
                                                                                double dx = speckle[0] - px;
                                                                                double dy = speckle[1] - py;
                                                                                double dz = speckle[2] - pz;
                                                                                float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                                                                                if (dist < speckle[3]) {
                                                                                    hit = true;
                                                                                    break;
                                                                                }
                                                                                if (buildable && dist * 0.5 < radi) {
                                                                                    buildable = false;
                                                                                }
                                                                            }
                                                                        }
                                                                        if (buildable && !hit) {
                                                                            specklesCreation.put(newSpeckle, System.currentTimeMillis());
                                                                            speckles.add(newSpeckle);
                                                                        }
                                                                    } finally {
                                                                        lockSpeckle.unlock();
                                                                    }

                                                                    float vari = tRnd.nextFloat();
                                                                    if (hit) {
                                                                        colourR = (type.getR() * .8f + vari * .2f * type.getR()) * .5f + colourR * .5f;
                                                                        colourG = (type.getG() * .8f + vari * .2f * type.getG()) * .5f + colourG * .5f;
                                                                        colourB = (type.getB() * .8f + vari * .2f * type.getB()) * .5f + colourB * .5f;
                                                                        colourA = 1f;
                                                                    } else {
                                                                        colourR = colourR * .8f + vari * .2f * colourR;
                                                                        colourG = colourG * .8f + vari * .2f * colourG;
                                                                        colourB = colourB * .8f + vari * .2f * colourB;
                                                                    }
                                                                    float oneMinusAlpha = 1f - colourA;
                                                                    if (lights) {
                                                                        float resLight = light + lightSpecular * 2f;
                                                                        colourR = resLight + colourR;
                                                                        colourG = resLight + colourG;
                                                                        colourB = resLight + colourB;
                                                                    }
                                                                    colourR = colourR * colourA + point[0] * oneMinusAlpha;
                                                                    colourG = colourG * colourA + point[1] * oneMinusAlpha;
                                                                    colourB = colourB * colourA + point[2] * oneMinusAlpha;
                                                                    break;
                                                                }
                                                                default:
                                                                    break;
                                                                }
                                                                point[0] = colourR;
                                                                point[1] = colourG;
                                                                point[2] = colourB;
                                                            }
                                                        }
                                                        point[3] = sx;
                                                        point[4] = sy;
                                                        point[5] = sx;
                                                        point[6] = sy + 1f;
                                                        point[7] = sx + 1f;
                                                        point[8] = sy + 1f;
                                                        point[9] = sx + 1f;
                                                        point[10] = sy;
                                                        points2.add(point);
                                                    }
                                                }
                                                i += 4;
                                            }
                                            if (s < 1) {
                                                int size = points2.size();
                                                int size2 = renderedPoints[0] == null ? -1 : renderedPoints[0].length;
                                                float[][] r;
                                                if (size2 < size) {
                                                    r = new float[size][];
                                                    skip = 0;
                                                } else {
                                                    r = renderedPoints[0];
                                                }
                                                for (int k = skip; k < size; k++) {
                                                    r[k] = points2.get(k);
                                                }
                                                skip = size;
                                                tmpLock.lock();
                                                try {
                                                    // Update renderedPoints here!
                                                    renderedPoints[0] = r;
                                                } finally {
                                                    tmpLock.unlock();
                                                }
                                            }
                                            i += 4 * cw * (chunks - 1);
                                        }

                                        tmpLock.lock();
                                        try {
                                            points.addAll(points2);
                                        } finally {
                                            tmpLock.unlock();
                                        }
                                    });
                                    threads[j].start();
                                }
                                boolean isRunning = true;
                                while (isRunning) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException ie) {
                                        Thread.currentThread().interrupt();
                                        throw new LDPartEditorException(ie);
                                    }
                                    isRunning = false;
                                    for (Thread thread : threads) {
                                        if (thread.isAlive())
                                            isRunning = true;
                                    }
                                }
                            }

                            final int size = points.size();
                            float[][] r = new float[size][];
                            for (int j = 0; j < size; j++) {
                                r[j] = points.get(j);
                            }

                            lock.lock();
                            try {
                                // Update renderedPoints here!
                                renderedPoints[0] = r;
                                ViewIdleManager.renderLDrawStandard[0].set(true);
                            } finally {
                                lock.unlock();
                            }

                            alive.set(false);
                            counter = 0;
                            while(!alive.get()) {
                                counter++;
                                if (counter > 100) {
                                    NLogger.debug(getClass(), "Stopped raytracer."); //$NON-NLS-1$
                                    return;
                                }
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException ie) {
                                    Thread.currentThread().interrupt();
                                    throw new LDPartEditorException(ie);
                                }
                            }
                        }
                    }

                    );
                    raytracer.start();
                } else {
                    if (needData.get() > 0) {
                        needData.incrementAndGet();
                        while(needData.get() > 0) {
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                throw new LDPartEditorException(ie);
                            }
                        }
                    }
                }
                alive.set(true);
            } else {
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
            }

            Manipulator manipulator = c3d.getManipulator();
            final float mx = manipulator.getPosition().x;
            final float my = manipulator.getPosition().y;
            final float mz = manipulator.getPosition().z;

            // MARK Manipulator
            boolean singleMode = true;
            GColour c;
            if (TransformationModeToolItem.getWorkingAction() != WorkingMode.SELECT) {
                final float lineWidth;
                final float cone_height;
                final float cone_width;
                final float circleWidth;
                final float arcWidth;
                final float bluntSize;
                // The size will be set on application start!
                final float moveSize = Manipulator.getTranslateSize();
                final float rotateSize = Manipulator.getRotateSize();
                final float rotateOuterSize = Manipulator.getRotateOuterSize();
                final float scaleSize = Manipulator.getScaleSize();
                // mSize has normally a length of 11
                // (lineWidth, cone_height, cone_width, bluntSize, circleWidth, arcWidth,
                // moveSizeFactor, rotateSizeFactor, rotateOuterSizeFactor, scaleSizeFactor
                // and activationTreshold)
                float[] mSize = userSettings.getManipulatorSize();
                if (mSize == null) {
                    // We have no custom manipulator settings yet => create a fake array
                    mSize = new float[]{1f, 1f, 1f, 1f, 1f, 1f};
                }
                switch (IconSize.getIconsize()) {
                case 1:
                    lineWidth = 2.5f * mSize[0];
                    cone_height = .019f * mSize[1];
                    cone_width = .005f * mSize[2];
                    bluntSize = .0125f * mSize[3];
                    circleWidth = (negDet ? -1f : 1f) * 0.0125f * mSize[4];
                    arcWidth = 0.004f * mSize[5];
                    break;
                case 2:
                    lineWidth = 3f * mSize[0];
                    cone_height = .023f * mSize[1];
                    cone_width = .006f * mSize[2];
                    bluntSize = .015f * mSize[3];
                    circleWidth = (negDet ? -1f : 1f) * 0.015f * mSize[4];
                    arcWidth = 0.004f * mSize[5];
                    break;
                case 3:
                    lineWidth = 3.5f * mSize[0];
                    cone_height = .027f * mSize[1];
                    cone_width = .007f * mSize[2];
                    bluntSize = .0175f * mSize[3];
                    circleWidth = (negDet ? -1f : 1f) * 0.0175f * mSize[4];
                    arcWidth = 0.004f * mSize[5];
                    break;
                case 4, 5:
                    lineWidth = 4f * mSize[0];
                    cone_height = .030f * mSize[1];
                    cone_width = .008f * mSize[2];
                    bluntSize = .02f * mSize[3];
                    circleWidth = (negDet ? -1f : 1f) * 0.02f * mSize[4];
                    arcWidth = 0.004f * mSize[5];
                    break;
                case -1, 0:
                default:
                    lineWidth = 2f * mSize[0];
                    cone_height = .015f * mSize[1];
                    cone_width = .004f * mSize[2];
                    bluntSize = .01f * mSize[3];
                    circleWidth = (negDet ? -1f : 1f) * 0.01f * mSize[4];
                    arcWidth = 0.002f * mSize[5];
                }
                switch (TransformationModeToolItem.getWorkingAction()) {
                case COMBINED:
                    singleMode = false;
                case ROTATE:
                    c = manipulator.checkManipulatorStatus(Colour.xAxisColourR, Colour.xAxisColourG, Colour.xAxisColourB, Manipulator.X_ROTATE, c3d, zoom);
                    new Arc(c.getR(), c.getG(), c.getB(), manipulator.getXaxis().x, manipulator.getXaxis().y, manipulator.getXaxis().z, rotateSize, arcWidth).draw(mx, my, mz, zoom);

                    if (manipulator.isXrotate()) {
                        c = manipulator.checkManipulatorStatus(Colour.manipulatorXAxisColourR, Colour.manipulatorXAxisColourG, Colour.manipulatorXAxisColourB, Manipulator.X_ROTATE_ARROW, c3d, zoom);
                        new Arrow(c.getR(), c.getG(), c.getB(), rotateSize * manipulator.getXrotateArrow().x, rotateSize * manipulator.getXrotateArrow().y, rotateSize * manipulator.getXrotateArrow().z, cone_height, cone_width, lineWidth)
                        .drawGL20(mx, my, mz, zoom);
                    }

                    c = manipulator.checkManipulatorStatus(Colour.yAxisColourR, Colour.yAxisColourG, Colour.yAxisColourB, Manipulator.Y_ROTATE, c3d, zoom);
                    new Arc(c.getR(), c.getG(), c.getB(), manipulator.getYaxis().x, manipulator.getYaxis().y, manipulator.getYaxis().z, rotateSize, arcWidth).draw(mx, my, mz, zoom);

                    if (manipulator.isYrotate()) {
                        c = manipulator.checkManipulatorStatus(Colour.manipulatorYAxisColourR, Colour.manipulatorYAxisColourG, Colour.manipulatorYAxisColourB, Manipulator.Y_ROTATE_ARROW, c3d, zoom);
                        new Arrow(c.getR(), c.getG(), c.getB(), rotateSize * manipulator.getYrotateArrow().x, rotateSize * manipulator.getYrotateArrow().y, rotateSize * manipulator.getYrotateArrow().z, cone_height, cone_width, lineWidth)
                        .drawGL20(mx, my, mz, zoom);
                    }

                    c = manipulator.checkManipulatorStatus(Colour.zAxisColourR, Colour.zAxisColourG, Colour.zAxisColourB, Manipulator.Z_ROTATE, c3d, zoom);
                    new Arc(c.getR(), c.getG(), c.getB(), manipulator.getZaxis().x, manipulator.getZaxis().y, manipulator.getZaxis().z, rotateSize, arcWidth).draw(mx, my, mz, zoom);

                    if (manipulator.isZrotate()) {
                        c = manipulator.checkManipulatorStatus(Colour.manipulatorZAxisColourR, Colour.manipulatorZAxisColourG, Colour.manipulatorZAxisColourB, Manipulator.Z_ROTATE_ARROW, c3d, zoom);
                        new Arrow(c.getR(), c.getG(), c.getB(), rotateSize * manipulator.getZrotateArrow().x, rotateSize * manipulator.getZrotateArrow().y, rotateSize * manipulator.getZrotateArrow().z, cone_height, cone_width, lineWidth)
                        .drawGL20(mx, my, mz, zoom);
                    }

                    Vector4f[] gen = c3d.getGenerator();
                    new Circle(Colour.manipulatorInnerCircleColourR, Colour.manipulatorInnerCircleColourG, Colour.manipulatorInnerCircleColourB, gen[2].x, gen[2].y, gen[2].z, rotateSize, circleWidth).draw(mx, my, mz, zoom);
                    c = manipulator.checkManipulatorStatus(Colour.manipulatorOuterCircleColourR, Colour.manipulatorOuterCircleColourG, Colour.manipulatorOuterCircleColourB, Manipulator.V_ROTATE, c3d, zoom);
                    new Circle(c.getR(), c.getG(), c.getB(), gen[2].x, gen[2].y, gen[2].z, rotateOuterSize, circleWidth).draw(mx, my, mz, zoom);

                    if (manipulator.isVrotate()) {
                        c = manipulator.checkManipulatorStatus(Colour.manipulatorOuterCircleColourR, Colour.manipulatorOuterCircleColourG, Colour.manipulatorOuterCircleColourB, Manipulator.V_ROTATE_ARROW, c3d, zoom);
                        new Arrow(c.getR(), c.getG(), c.getB(), rotateOuterSize * manipulator.getVrotateArrow().x, rotateOuterSize * manipulator.getVrotateArrow().y, rotateOuterSize * manipulator.getVrotateArrow().z, cone_height, cone_width, lineWidth)
                        .drawGL20(mx, my, mz, zoom);
                    }
                    if (singleMode)
                        break;
                case SCALE:
                    c = manipulator.checkManipulatorStatus(Colour.xAxisColourR, Colour.xAxisColourG, Colour.xAxisColourB, Manipulator.X_SCALE, c3d, zoom);
                    new ArrowBlunt(c.getR(), c.getG(), c.getB(), scaleSize * manipulator.getXaxis().x, scaleSize * manipulator.getXaxis().y, scaleSize * manipulator.getXaxis().z, bluntSize, lineWidth).draw(mx, my, mz, zoom);
                    c = manipulator.checkManipulatorStatus(Colour.yAxisColourR, Colour.yAxisColourG, Colour.yAxisColourB, Manipulator.Y_SCALE, c3d, zoom);
                    new ArrowBlunt(c.getR(), c.getG(), c.getB(), scaleSize * manipulator.getYaxis().x, scaleSize * manipulator.getYaxis().y, scaleSize * manipulator.getYaxis().z, bluntSize, lineWidth).draw(mx, my, mz, zoom);
                    c = manipulator.checkManipulatorStatus(Colour.zAxisColourR, Colour.zAxisColourG, Colour.zAxisColourB, Manipulator.Z_SCALE, c3d, zoom);
                    new ArrowBlunt(c.getR(), c.getG(), c.getB(), scaleSize * manipulator.getZaxis().x, scaleSize * manipulator.getZaxis().y, scaleSize * manipulator.getZaxis().z, bluntSize, lineWidth).draw(mx, my, mz, zoom);
                    if (singleMode)
                        break;
                case MOVE:
                    c = manipulator.checkManipulatorStatus(Colour.xAxisColourR, Colour.xAxisColourG, Colour.xAxisColourB, Manipulator.X_TRANSLATE, c3d, zoom);
                    new Arrow(c.getR(), c.getG(), c.getB(), moveSize * manipulator.getXaxis().x, moveSize * manipulator.getXaxis().y, moveSize * manipulator.getXaxis().z, cone_height, cone_width, lineWidth).drawGL20(mx, my, mz, zoom);
                    c = manipulator.checkManipulatorStatus(Colour.yAxisColourR, Colour.yAxisColourG, Colour.yAxisColourB, Manipulator.Y_TRANSLATE, c3d, zoom);
                    new Arrow(c.getR(), c.getG(), c.getB(), moveSize * manipulator.getYaxis().x, moveSize * manipulator.getYaxis().y, moveSize * manipulator.getYaxis().z, cone_height, cone_width, lineWidth).drawGL20(mx, my, mz, zoom);
                    c = manipulator.checkManipulatorStatus(Colour.zAxisColourR, Colour.zAxisColourG, Colour.zAxisColourB, Manipulator.Z_TRANSLATE, c3d, zoom);
                    new Arrow(c.getR(), c.getG(), c.getB(), moveSize * manipulator.getZaxis().x, moveSize * manipulator.getZaxis().y, moveSize * manipulator.getZaxis().z, cone_height, cone_width, lineWidth).drawGL20(mx, my, mz, zoom);
                    break;
                case MOVE_GLOBAL:
                    c = manipulator.checkManipulatorStatus(Colour.xAxisColourR, Colour.xAxisColourG, Colour.xAxisColourB, Manipulator.X_TRANSLATE, c3d, zoom);
                    new Arrow(c.getR(), c.getG(), c.getB(), moveSize, 0f, 0f, cone_height, cone_width, lineWidth).drawGL20(mx, my, mz, zoom);
                    c = manipulator.checkManipulatorStatus(Colour.yAxisColourR, Colour.yAxisColourG, Colour.yAxisColourB, Manipulator.Y_TRANSLATE, c3d, zoom);
                    new Arrow(c.getR(), c.getG(), c.getB(), 0f, moveSize, 0f, cone_height, cone_width, lineWidth).drawGL20(mx, my, mz, zoom);
                    c = manipulator.checkManipulatorStatus(Colour.zAxisColourR, Colour.zAxisColourG, Colour.zAxisColourB, Manipulator.Z_TRANSLATE, c3d, zoom);
                    new Arrow(c.getR(), c.getG(), c.getB(), 0f, 0f, moveSize, cone_height, cone_width, lineWidth).drawGL20(mx, my, mz, zoom);
                    break;
                default:
                    break;
                }
            }

            // MARK Draw temporary objects for all "Add..." functions here
            if (AddToolItem.isAddingSomething() && c3d.getLockableDatFileReference().getLastSelectedComposite() != null && c3d.getLockableDatFileReference().getLastSelectedComposite().equals(c3d)) {
                if (AddToolItem.isAddingVertices()) {
                    // Point for add vertex
                    GL11.glColor3f(Colour.addObjectColourR, Colour.addObjectColourG, Colour.addObjectColourB);
                    GL11.glBegin(GL11.GL_POINTS);
                    Vector4f cursor3D = c3d.getCursorSnapped3D();
                    GL11.glVertex3f(cursor3D.x, cursor3D.y, cursor3D.z);
                    GL11.glEnd();
                } else if (AddToolItem.isAddingLines() || AddToolItem.isAddingDistance()) {
                    Vector4f cur = c3d.getCursorSnapped3D();
                    DatFile dat = c3d.getLockableDatFileReference();
                    Vertex v = dat.getNearestObjVertex1();
                    if (v != null) {
                        GL11.glLineWidth(4f);
                        GL11.glBegin(GL11.GL_LINES);
                        GL11.glColor3f(Colour.addObjectColourR, Colour.addObjectColourG, Colour.addObjectColourB);
                        GL11.glVertex3f(v.x, v.y, v.z);
                        GL11.glVertex3f(cur.x, cur.y, cur.z);
                        GL11.glEnd();
                    }
                } else if (AddToolItem.isAddingTriangles() || AddToolItem.isAddingProtractor()) {
                    Vector4f cur = c3d.getCursorSnapped3D();
                    DatFile dat = c3d.getLockableDatFileReference();
                    Vertex v = dat.getNearestObjVertex1();
                    if (v != null) {
                        Vertex v2 = dat.getNearestObjVertex2();
                        if (v2 != null) {
                            GL11.glLineWidth(4f);
                            GL11.glBegin(GL11.GL_LINES);
                            GL11.glColor3f(Colour.addObjectColourR, Colour.addObjectColourG, Colour.addObjectColourB);
                            GL11.glVertex3f(v.x, v.y, v.z);
                            GL11.glVertex3f(cur.x, cur.y, cur.z);
                            GL11.glVertex3f(v2.x, v2.y, v2.z);
                            GL11.glVertex3f(cur.x, cur.y, cur.z);
                            GL11.glVertex3f(v2.x, v2.y, v2.z);
                            GL11.glVertex3f(v.x, v.y, v.z);
                            GL11.glEnd();
                        } else {
                            GL11.glLineWidth(4f);
                            GL11.glBegin(GL11.GL_LINES);
                            GL11.glColor3f(Colour.addObjectColourR, Colour.addObjectColourG, Colour.addObjectColourB);
                            GL11.glVertex3f(v.x, v.y, v.z);
                            GL11.glVertex3f(cur.x, cur.y, cur.z);
                            GL11.glEnd();
                        }
                    }
                } else if (AddToolItem.isAddingQuads()) {
                    Vector4f cur = c3d.getCursorSnapped3D();
                    DatFile dat = c3d.getLockableDatFileReference();
                    Vertex v = dat.getNearestObjVertex1();
                    if (v != null) {
                        Vertex v2 = dat.getNearestObjVertex2();
                        if (v2 != null) {
                            Vertex v3 = dat.getObjVertex3();
                            if (v3 != null) {
                                Vertex v4 = dat.getObjVertex4();
                                if (v4 != null) {
                                    GL11.glLineWidth(4f);
                                    GL11.glBegin(GL11.GL_LINES);
                                    GL11.glColor3f(Colour.addObjectColourR, Colour.addObjectColourG, Colour.addObjectColourB);
                                    GL11.glVertex3f(v2.x, v2.y, v2.z);
                                    GL11.glVertex3f(cur.x, cur.y, cur.z);
                                    GL11.glVertex3f(v2.x, v2.y, v2.z);
                                    GL11.glVertex3f(v.x, v.y, v.z);
                                    GL11.glEnd();
                                } else {
                                    v = dat.getObjVertex1();
                                    v2 = dat.getObjVertex2();
                                    GL11.glLineWidth(4f);
                                    GL11.glBegin(GL11.GL_LINES);
                                    GL11.glColor3f(Colour.addObjectColourR, Colour.addObjectColourG, Colour.addObjectColourB);
                                    GL11.glVertex3f(v.x, v.y, v.z);
                                    GL11.glVertex3f(v2.x, v2.y, v2.z);
                                    GL11.glVertex3f(v2.x, v2.y, v2.z);
                                    GL11.glVertex3f(v3.x, v3.y, v3.z);
                                    GL11.glVertex3f(v3.x, v3.y, v3.z);
                                    GL11.glVertex3f(cur.x, cur.y, cur.z);
                                    GL11.glVertex3f(cur.x, cur.y, cur.z);
                                    GL11.glVertex3f(v.x, v.y, v.z);
                                    GL11.glEnd();
                                }
                            } else {
                                GL11.glLineWidth(4f);
                                GL11.glBegin(GL11.GL_LINES);
                                GL11.glColor3f(Colour.addObjectColourR, Colour.addObjectColourG, Colour.addObjectColourB);
                                GL11.glVertex3f(v2.x, v2.y, v2.z);
                                GL11.glVertex3f(cur.x, cur.y, cur.z);
                                GL11.glVertex3f(v2.x, v2.y, v2.z);
                                GL11.glVertex3f(v.x, v.y, v.z);
                                GL11.glEnd();
                            }
                        } else {
                            GL11.glLineWidth(4f);
                            GL11.glBegin(GL11.GL_LINES);
                            GL11.glColor3f(Colour.addObjectColourR, Colour.addObjectColourG, Colour.addObjectColourB);
                            GL11.glVertex3f(v.x, v.y, v.z);
                            GL11.glVertex3f(cur.x, cur.y, cur.z);
                            GL11.glEnd();
                        }
                    }
                } else if (AddToolItem.isAddingCondlines()) {
                    Vector4f cur = c3d.getCursorSnapped3D();
                    DatFile dat = c3d.getLockableDatFileReference();
                    Vertex v = dat.getNearestObjVertex1();
                    if (v != null) {
                        Vertex v2 = dat.getNearestObjVertex2();
                        if (v2 != null) {
                            Vertex v3 = dat.getObjVertex3();
                            if (v3 != null) {
                                Vertex v4 = dat.getObjVertex4();
                                if (v4 != null) {
                                    GL11.glLineWidth(4f);
                                    GL11.glBegin(GL11.GL_LINES);
                                    GL11.glColor3f(Colour.addObjectColourR, Colour.addObjectColourG, Colour.addObjectColourB);
                                    GL11.glVertex3f(cur.x, cur.y, cur.z);
                                    GL11.glVertex3f(v.x, v.y, v.z);
                                    GL11.glEnd();
                                } else {
                                    v = dat.getObjVertex1();
                                    v2 = dat.getObjVertex2();
                                    GL11.glLineWidth(4f);
                                    GL11.glBegin(GL11.GL_LINES);
                                    GL11.glColor3f(Colour.addObjectColourR, Colour.addObjectColourG, Colour.addObjectColourB);
                                    GL11.glVertex3f(v.x, v.y, v.z);
                                    GL11.glVertex3f(v2.x, v2.y, v2.z);
                                    GL11.glVertex3f(v2.x, v2.y, v2.z);
                                    GL11.glVertex3f(v3.x, v3.y, v3.z);
                                    GL11.glVertex3f(v2.x, v2.y, v2.z);
                                    GL11.glVertex3f(cur.x, cur.y, cur.z);
                                    GL11.glEnd();
                                }
                            } else {
                                GL11.glLineWidth(4f);
                                GL11.glBegin(GL11.GL_LINES);
                                GL11.glColor3f(Colour.addObjectColourR, Colour.addObjectColourG, Colour.addObjectColourB);
                                GL11.glVertex3f(v2.x, v2.y, v2.z);
                                GL11.glVertex3f(cur.x, cur.y, cur.z);
                                GL11.glVertex3f(v2.x, v2.y, v2.z);
                                GL11.glVertex3f(v.x, v.y, v.z);
                                GL11.glEnd();
                            }
                        } else {
                            GL11.glLineWidth(4f);
                            GL11.glBegin(GL11.GL_LINES);
                            GL11.glColor3f(Colour.addObjectColourR, Colour.addObjectColourG, Colour.addObjectColourB);
                            GL11.glVertex3f(v.x, v.y, v.z);
                            GL11.glVertex3f(cur.x, cur.y, cur.z);
                            GL11.glEnd();
                        }
                    }
                }
            }

            if (!raytraceMode && DatFile.getLastHoveredComposite() == c3d) {
                Vector4f selectionEndMODELVIEW = new Vector4f(c3d.getCursor3D());
                float viewportPixelPerLDU = c3d.getViewportPixelPerLDU();
                float dx = 0;
                float dy = 0;
                dx = 100f / viewportPixelPerLDU;
                dy = 100f / viewportPixelPerLDU;
                Vector4f xAxis4fTranslation = new Vector4f(dx, 0, 0, 1.0f);
                Vector4f yAxis4fTranslation = new Vector4f(0, dy, 0, 1.0f);
                Matrix4f ovrInverse2 = Matrix4f.invert(viewportRotation, null);
                Matrix4f.transform(ovrInverse2, xAxis4fTranslation, xAxis4fTranslation);
                Matrix4f.transform(ovrInverse2, yAxis4fTranslation, yAxis4fTranslation);
                Vector4f width = new Vector4f(xAxis4fTranslation.x / 2f, xAxis4fTranslation.y / 2f, xAxis4fTranslation.z / 2f, 1f);
                Vector4f height = new Vector4f(yAxis4fTranslation.x / 2f, yAxis4fTranslation.y / 2f, yAxis4fTranslation.z / 2f, 1f);

                Vector4f selectionCorner1 = new Vector4f(selectionEndMODELVIEW.x + width.x, selectionEndMODELVIEW.y + width.y, selectionEndMODELVIEW.z + width.z, 1f);
                Vector4f selectionCorner2 = new Vector4f(selectionEndMODELVIEW.x + height.x, selectionEndMODELVIEW.y + height.y, selectionEndMODELVIEW.z + height.z, 1f);
                Vector4f selectionCorner3 = new Vector4f(selectionEndMODELVIEW.x - width.x, selectionEndMODELVIEW.y - width.y, selectionEndMODELVIEW.z - width.z, 1f);
                Vector4f selectionCorner4 = new Vector4f(selectionEndMODELVIEW.x - height.x, selectionEndMODELVIEW.y - height.y, selectionEndMODELVIEW.z - height.z, 1f);

                GL11.glLineWidth(2f);
                GL11.glBegin(GL11.GL_LINES);
                GL11.glColor3f(Colour.cursor1ColourR, Colour.cursor1ColourG, Colour.cursor1ColourB);
                GL11.glVertex3f(selectionCorner3.x, selectionCorner3.y, selectionCorner3.z);
                GL11.glVertex3f(selectionCorner1.x, selectionCorner1.y, selectionCorner1.z);
                GL11.glColor3f(Colour.cursor2ColourR, Colour.cursor2ColourG, Colour.cursor2ColourB);
                GL11.glVertex3f(selectionCorner4.x, selectionCorner4.y, selectionCorner4.z);
                GL11.glVertex3f(selectionCorner2.x, selectionCorner2.y, selectionCorner2.z);

                GL11.glEnd();
            }

            GL11.glEnable(GL11.GL_DEPTH_TEST);

            // MARK Drawing the selection Rectangle
            if (c3d.isDoingSelection()) {
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                Vector4f selectionStartMODELVIEW = new Vector4f(c3d.getSelectionStart());
                Vector4f selectionEndMODELVIEW = new Vector4f(c3d.getCursor3D());
                float viewportPixelPerLDU = c3d.getViewportPixelPerLDU();
                float dx = 0;
                float dy = 0;
                dx = (c3d.getOldMousePosition().x - c3d.getMousePosition().x) / viewportPixelPerLDU;
                dy = (c3d.getMousePosition().y - c3d.getOldMousePosition().y) / viewportPixelPerLDU;
                Vector4f xAxis4fTranslation = new Vector4f(dx, 0, 0, 1.0f);
                Vector4f yAxis4fTranslation = new Vector4f(0, dy, 0, 1.0f);
                Matrix4f ovrInverse2 = Matrix4f.invert(viewportRotation, null);
                Matrix4f.transform(ovrInverse2, xAxis4fTranslation, xAxis4fTranslation);
                Matrix4f.transform(ovrInverse2, yAxis4fTranslation, yAxis4fTranslation);
                Vector4f width = new Vector4f(xAxis4fTranslation.x, xAxis4fTranslation.y, xAxis4fTranslation.z, 1f);
                Vector4f height = new Vector4f(yAxis4fTranslation.x, yAxis4fTranslation.y, yAxis4fTranslation.z, 1f);

                c3d.getSelectionWidth().set(width);
                c3d.getSelectionHeight().set(height);

                Vector4f selectionCorner1 = new Vector4f(selectionStartMODELVIEW.x + width.x, selectionStartMODELVIEW.y + width.y, selectionStartMODELVIEW.z + width.z, 1f);
                Vector4f selectionCorner2 = new Vector4f(selectionStartMODELVIEW.x + height.x, selectionStartMODELVIEW.y + height.y, selectionStartMODELVIEW.z + height.z, 1f);

                GL11.glLineWidth(3f);

                GL11.glColor3f(Colour.rubberBandColourR, Colour.rubberBandColourG, Colour.rubberBandColourB);
                GL11.glBegin(GL11.GL_LINES);

                GL11.glVertex3f(selectionStartMODELVIEW.x, selectionStartMODELVIEW.y, selectionStartMODELVIEW.z);
                GL11.glVertex3f(selectionCorner1.x, selectionCorner1.y, selectionCorner1.z);

                GL11.glVertex3f(selectionStartMODELVIEW.x, selectionStartMODELVIEW.y, selectionStartMODELVIEW.z);
                GL11.glVertex3f(selectionCorner2.x, selectionCorner2.y, selectionCorner2.z);

                GL11.glVertex3f(selectionEndMODELVIEW.x, selectionEndMODELVIEW.y, selectionEndMODELVIEW.z);
                GL11.glVertex3f(selectionCorner1.x, selectionCorner1.y, selectionCorner1.z);

                GL11.glVertex3f(selectionEndMODELVIEW.x, selectionEndMODELVIEW.y, selectionEndMODELVIEW.z);
                GL11.glVertex3f(selectionCorner2.x, selectionCorner2.y, selectionCorner2.z);

                GL11.glEnd();
                GL11.glEnable(GL11.GL_DEPTH_TEST);

            } else {
                c3d.getSelectionWidth().set(0.0001f, 0.0001f, 0.0001f);
                c3d.getSelectionHeight().set(0.0001f, 0.0001f, 0.0001f);
            }

            // To make it easier to draw and calculate the grid and the origin,
            // reset the transformation matrix ;)
            GL11.glLoadIdentity();
            Vector3f[] viewportOriginAxis = c3d.getViewportOriginAxis();
            float zOffset = 0;
            if (c3d.isGridShown()) {
                // Grid-1 and 10
                for (int r = 0; r < 5; r += 4) {
                    if (r == 4) {
                        GL11.glColor3f(Colour.grid10ColourR, Colour.grid10ColourG, Colour.grid10ColourB);
                        zOffset = 1f;
                        GL11.glLineWidth(2f);
                    } else {
                        GL11.glColor3f(Colour.gridColourR, Colour.gridColourG, Colour.gridColourB);
                        zOffset = 0;
                        GL11.glLineWidth(1f);
                    }
                    GL11.glBegin(GL11.GL_LINES);
                    Vector4f gridCenter1 = new Vector4f();
                    Vector4f gridCenter2 = new Vector4f();
                    gridCenter1.set(c3d.getGrid()[r]);
                    gridCenter2.set(gridCenter1);
                    for (float i = 0f; i < c3d.getGrid()[3 + r].y; i += 1f) {
                        Vector4f.sub(gridCenter2, c3d.getGrid()[2 + r], gridCenter2);
                        GL11.glVertex3f(viewportOriginAxis[0].x, gridCenter1.y, viewportOriginAxis[0].z + zOffset);
                        GL11.glVertex3f(viewportOriginAxis[1].x, gridCenter1.y, viewportOriginAxis[1].z + zOffset);
                        GL11.glVertex3f(viewportOriginAxis[0].x, gridCenter2.y, viewportOriginAxis[0].z + zOffset);
                        GL11.glVertex3f(viewportOriginAxis[1].x, gridCenter2.y, viewportOriginAxis[1].z + zOffset);
                        Vector4f.add(gridCenter1, c3d.getGrid()[2 + r], gridCenter1);
                    }
                    gridCenter1.set(c3d.getGrid()[r]);
                    gridCenter2.set(gridCenter1);
                    for (float i = 0f; i < c3d.getGrid()[3 + r].x; i += 1f) {
                        Vector4f.sub(gridCenter2, c3d.getGrid()[1 + r], gridCenter2);
                        GL11.glVertex3f(gridCenter2.x, viewportOriginAxis[2].y, viewportOriginAxis[2].z + zOffset);
                        GL11.glVertex3f(gridCenter2.x, viewportOriginAxis[3].y, viewportOriginAxis[3].z + zOffset);
                        GL11.glVertex3f(gridCenter1.x, viewportOriginAxis[2].y, viewportOriginAxis[2].z + zOffset);
                        GL11.glVertex3f(gridCenter1.x, viewportOriginAxis[3].y, viewportOriginAxis[3].z + zOffset);
                        Vector4f.add(gridCenter1, c3d.getGrid()[1 + r], gridCenter1);
                    }
                    GL11.glEnd();
                }
                zOffset = 2f;
            }

            if (c3d.isOriginShown()) {
                // Origin
                GL11.glLineWidth(2f);
                GL11.glBegin(GL11.GL_LINES);
                GL11.glColor3f(Colour.originColourR, Colour.originColourG, Colour.originColourB);
                GL11.glVertex3f(viewportOriginAxis[0].x, viewportOriginAxis[0].y, viewportOriginAxis[0].z + zOffset);
                GL11.glVertex3f(viewportOriginAxis[1].x, viewportOriginAxis[1].y, viewportOriginAxis[1].z + zOffset);
                GL11.glVertex3f(viewportOriginAxis[2].x, viewportOriginAxis[2].y, viewportOriginAxis[2].z + zOffset);
                GL11.glVertex3f(viewportOriginAxis[3].x, viewportOriginAxis[3].y, viewportOriginAxis[3].z + zOffset);
                GL11.glEnd();
            }

            if (c3d.isAnaglyph3d() && !raytraceMode && state3d == 0) {
                GL11.glColorMask(false, true, true, true);
                state3d++;
            } else {
                if (c3d.isShowingAxis()) {
                    GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
                    final float l;
                    final float ox;
                    final float oy;
                    final float cone_height;
                    final float cone_width;
                    final float line_width;
                    switch (IconSize.getIconsize()) {
                    case 4, 5:
                        l = 1f;
                        ox = .1f;
                        oy = .11f;
                        cone_height = .00030f;
                        cone_width = .00008f;
                        line_width = 6f;
                        break;
                    case 2, 3:
                        l = .75f;
                        ox = .075f;
                        oy = .085f;
                        cone_height = .00023f;
                        cone_width = .00006f;
                        line_width = 4f;
                        break;
                    case 0, 1:
                    default:
                        l = .5f;
                        ox = .05f;
                        oy = .06f;
                        cone_height = .00015f;
                        cone_width = .00004f;
                        line_width = 2f;
                    }
                    GL11.glPushMatrix();
                    GL11.glTranslatef(ox - viewportWidth, viewportHeight - oy, 0f);
                    GL11.glMultMatrixf(rotation);
                    new Arrow(Colour.xAxisColourR, Colour.xAxisColourG, Colour.xAxisColourB, l,  0f, 0f, cone_height, cone_width, line_width).drawGL20(0f, 0f, 0f, .01f);
                    new Arrow(Colour.yAxisColourR, Colour.yAxisColourG, Colour.yAxisColourB, 0f, l,  0f, cone_height, cone_width, line_width).drawGL20(0f, 0f, 0f, .01f);
                    new Arrow(Colour.zAxisColourR, Colour.zAxisColourG, Colour.zAxisColourB, 0f, 0f, l,  cone_height, cone_width, line_width).drawGL20(0f, 0f, 0f, .01f);
                    GL11.glPopMatrix();
                }
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                if (c3d.isShowingLabels() && c3d.isClassicPerspective()) {
                    PGData3.beginDrawText();
                    switch (c3d.getPerspectiveIndex()) {
                    case FRONT:
                        for (PGData3 tri : View.FRONT) {
                            tri.drawText(viewportWidth, viewportHeight, viewportOriginAxis[0].z);
                        }
                        break;
                    case BACK:
                        for (PGData3 tri : View.BACK) {
                            tri.drawText(viewportWidth, viewportHeight, viewportOriginAxis[0].z);
                        }
                        break;
                    case TOP:
                        for (PGData3 tri : View.TOP) {
                            tri.drawText(viewportWidth, viewportHeight, viewportOriginAxis[0].z);
                        }
                        break;
                    case BOTTOM:
                        for (PGData3 tri : View.BOTTOM) {
                            tri.drawText(viewportWidth, viewportHeight, viewportOriginAxis[0].z);
                        }
                        break;
                    case LEFT:
                        for (PGData3 tri : View.LEFT) {
                            tri.drawText(viewportWidth, viewportHeight, viewportOriginAxis[0].z);
                        }
                        break;
                    case RIGHT:
                        for (PGData3 tri : View.RIGHT) {
                            tri.drawText(viewportWidth, viewportHeight, viewportOriginAxis[0].z);
                        }
                        break;
                    case TWO_THIRDS:
                    default:
                        break;
                    }
                    PGData3.endDrawText();
                }
                if (Project.getFileToEdit().equals(c3d.getLockableDatFileReference())) {
                    if (Project.getFileToEdit().isReadOnly()) {
                        GL11.glColor3f(Colour.textColourR, Colour.textColourG, Colour.textColourB);
                    } else if (c3d.equals(Project.getFileToEdit().getLastSelectedComposite())) {
                        GL11.glColor3f(1f - Colour.vertexSelectedColourR, 1f - Colour.vertexSelectedColourG, 1f - Colour.vertexSelectedColourB);
                    } else {
                        GL11.glColor3f(Colour.vertexSelectedColourR, Colour.vertexSelectedColourG, Colour.vertexSelectedColourB);
                    }
                    GL11.glLineWidth(7f);
                    GL11.glBegin(GL11.GL_LINES);
                    GL11.glVertex3f(viewportWidth, viewportHeight, viewportOriginAxis[3].z);
                    GL11.glVertex3f(viewportWidth, -viewportHeight, viewportOriginAxis[3].z);
                    GL11.glEnd();
                    GL11.glLineWidth(10f);
                    GL11.glBegin(GL11.GL_LINES);
                    GL11.glVertex3f(-viewportWidth, -viewportHeight, viewportOriginAxis[3].z);
                    GL11.glVertex3f(-viewportWidth, viewportHeight, viewportOriginAxis[3].z);
                    GL11.glEnd();
                    GL11.glLineWidth(5f);
                    GL11.glBegin(GL11.GL_LINES);
                    GL11.glVertex3f(-viewportWidth, viewportHeight, viewportOriginAxis[3].z);
                    GL11.glVertex3f(viewportWidth, viewportHeight, viewportOriginAxis[3].z);
                    GL11.glEnd();
                    GL11.glLineWidth(10f);
                    GL11.glBegin(GL11.GL_LINES);
                    GL11.glVertex3f(-viewportWidth, -viewportHeight, viewportOriginAxis[3].z);
                    GL11.glVertex3f(viewportWidth, -viewportHeight, viewportOriginAxis[3].z);
                    GL11.glEnd();
                }

                if (!c3d.isDoingSelection() && !manipulator.isLocked() && !AddToolItem.isAddingSomething() && c3d.getDraggedPrimitive() == null) {
                    Vector2f mp = c3d.getMousePosition();
                    GL11.glColor3f(Colour.textColourR, Colour.textColourG, Colour.textColourB);
                    if (mp.x > 50f || mp.y > 50f) {
                        GL11.glColor3f(Colour.textColourR, Colour.textColourG, Colour.textColourB);
                        if (DatFile.getLastHoveredComposite() == c3d) {
                            hoverSettingsTime = System.currentTimeMillis();
                        }
                    } else if (mp.x > 0f && mp.y > 0f) {
                        GL11.glColor3f(Colour.vertexSelectedColourR, Colour.vertexSelectedColourG, Colour.vertexSelectedColourB);
                        if (System.currentTimeMillis() - hoverSettingsTime > 600 && DatFile.getLastHoveredComposite() == c3d) {

                            hoverSettingsTime = System.currentTimeMillis();

                            if (c3d.hasMouse()) {
                                java.awt.Point b = java.awt.MouseInfo.getPointerInfo().getLocation();
                                final int x = (int) b.getX();
                                final int y = (int) b.getY();

                                Menu menu = c3d.getMenu();
                                if (!menu.isDisposed() && !menu.getVisible()) {
                                    menu.setLocation(x, y);
                                    menu.setVisible(true);
                                    mp.setX(51f);
                                    mp.setY(51f);
                                }
                            }
                        }
                    }

                    final float gx = viewportWidth - 0.018f;
                    final float gy = -viewportHeight + 0.018f;
                    GL20Primitives.GEAR_MENU.draw(gx, gy, viewportOriginAxis[0].z);
                    GL20Primitives.GEAR_MENU_INV.draw(gx, gy, viewportOriginAxis[0].z);

                    // Draw arrows for cursor-on-border-scrolling
                    if (userSettings.isTranslatingViewByCursor() && c3d.hasMouse() && c3d.equals(Project.getFileToEdit().getLastSelectedComposite())) {

                    	final float duration = Math.max(10f, Math.min(1000, System.currentTimeMillis() - start));
                    	final float speed = 0.05f / duration / zoom;

                    	// TOP
                        GL11.glColor3f(Colour.textColourR, Colour.textColourG, Colour.textColourB);
                        if (Math.abs(bounds.width / 2f - mp.x) > 75f || mp.y > 25f) {
                            GL11.glColor3f(Colour.textColourR, Colour.textColourG, Colour.textColourB);
                        } else if (mp.y > 0f && Math.abs(bounds.width / 2f - mp.x) <= 75f) {
                            GL11.glColor3f(Colour.vertexSelectedColourR, Colour.vertexSelectedColourG, Colour.vertexSelectedColourB);
                            c3d.getMouse().prepareTranslateViewport();
                            c3d.getMouse().translateViewport(0f, speed, viewportTranslation, viewportRotation, c3d.getPerspectiveCalculator());
                        }

                        GL11.glBegin(GL11.GL_TRIANGLES);
                        GL11.glVertex3f(-0.018f, -viewportHeight + 0.018f, viewportOriginAxis[0].z);
                        GL11.glVertex3f(0.018f, -viewportHeight + 0.018f, viewportOriginAxis[0].z);
                        GL11.glVertex3f(0, -viewportHeight + 0.009f, viewportOriginAxis[0].z);
                        GL11.glVertex3f(-0.018f, -viewportHeight + 0.018f, viewportOriginAxis[0].z);
                        GL11.glVertex3f(0, -viewportHeight + 0.009f, viewportOriginAxis[0].z);
                        GL11.glVertex3f(0.018f, -viewportHeight + 0.018f, viewportOriginAxis[0].z);
                        GL11.glEnd();

                        // BOTTOM
                        GL11.glColor3f(Colour.textColourR, Colour.textColourG, Colour.textColourB);
                        if (Math.abs(bounds.width / 2f - mp.x) > 75f || mp.y <= (bounds.height - 25)) {
                            GL11.glColor3f(Colour.textColourR, Colour.textColourG, Colour.textColourB);
                        } else if (mp.y > (bounds.height - 25) && Math.abs(bounds.width / 2f - mp.x) <= 75f) {
                            GL11.glColor3f(Colour.vertexSelectedColourR, Colour.vertexSelectedColourG, Colour.vertexSelectedColourB);
                            c3d.getMouse().prepareTranslateViewport();
                            c3d.getMouse().translateViewport(0f, -speed, viewportTranslation, viewportRotation, c3d.getPerspectiveCalculator());
                        }

                        GL11.glBegin(GL11.GL_TRIANGLES);
                        GL11.glVertex3f(-0.018f, viewportHeight - 0.018f, viewportOriginAxis[0].z);
                        GL11.glVertex3f(0.018f, viewportHeight - 0.018f, viewportOriginAxis[0].z);
                        GL11.glVertex3f(0, viewportHeight - 0.009f, viewportOriginAxis[0].z);
                        GL11.glVertex3f(-0.018f, viewportHeight - 0.018f, viewportOriginAxis[0].z);
                        GL11.glVertex3f(0, viewportHeight - 0.009f, viewportOriginAxis[0].z);
                        GL11.glVertex3f(0.018f, viewportHeight - 0.018f, viewportOriginAxis[0].z);
                        GL11.glEnd();

                        // LEFT
                        GL11.glColor3f(Colour.textColourR, Colour.textColourG, Colour.textColourB);
                        if (Math.abs(bounds.height / 2f - mp.y) > 75f || mp.x >= 25) {
                            GL11.glColor3f(Colour.textColourR, Colour.textColourG, Colour.textColourB);
                        } else if (mp.x < 25 && Math.abs(bounds.height / 2f - mp.y) <= 75f) {
                            GL11.glColor3f(Colour.vertexSelectedColourR, Colour.vertexSelectedColourG, Colour.vertexSelectedColourB);
                            c3d.getMouse().prepareTranslateViewport();
                            c3d.getMouse().translateViewport(-speed, 0f, viewportTranslation, viewportRotation, c3d.getPerspectiveCalculator());
                        }

                        GL11.glBegin(GL11.GL_TRIANGLES);
                        GL11.glVertex3f(viewportWidth - 0.018f, -0.018f, viewportOriginAxis[0].z);
                        GL11.glVertex3f(viewportWidth - 0.018f, 0.018f, viewportOriginAxis[0].z);
                        GL11.glVertex3f(viewportWidth - 0.009f, 0, viewportOriginAxis[0].z);
                        GL11.glVertex3f(viewportWidth - 0.018f, -0.018f, viewportOriginAxis[0].z);
                        GL11.glVertex3f(viewportWidth - 0.009f, 0, viewportOriginAxis[0].z);
                        GL11.glVertex3f(viewportWidth - 0.018f, 0.018f, viewportOriginAxis[0].z);
                        GL11.glEnd();

                        // RIGHT
                        GL11.glColor3f(Colour.textColourR, Colour.textColourG, Colour.textColourB);
                        if (Math.abs(bounds.height / 2f - mp.y) > 75f || mp.x <= (bounds.width - 25)) {
                            GL11.glColor3f(Colour.textColourR, Colour.textColourG, Colour.textColourB);
                        } else if (mp.x > (bounds.width - 25) && Math.abs(bounds.height / 2f - mp.y) <= 75f) {
                            GL11.glColor3f(Colour.vertexSelectedColourR, Colour.vertexSelectedColourG, Colour.vertexSelectedColourB);
                            c3d.getMouse().prepareTranslateViewport();
                            c3d.getMouse().translateViewport(speed, 0f, viewportTranslation, viewportRotation, c3d.getPerspectiveCalculator());
                        }

                        GL11.glBegin(GL11.GL_TRIANGLES);
                        GL11.glVertex3f(-viewportWidth + 0.018f, -0.018f, viewportOriginAxis[0].z);
                        GL11.glVertex3f(-viewportWidth + 0.018f, 0.018f, viewportOriginAxis[0].z);
                        GL11.glVertex3f(-viewportWidth + 0.009f, 0, viewportOriginAxis[0].z);
                        GL11.glVertex3f(-viewportWidth + 0.018f, -0.018f, viewportOriginAxis[0].z);
                        GL11.glVertex3f(-viewportWidth + 0.009f, 0, viewportOriginAxis[0].z);
                        GL11.glVertex3f(-viewportWidth + 0.018f, 0.018f, viewportOriginAxis[0].z);
                        GL11.glEnd();
                    }
                }

                GL11.glEnable(GL11.GL_DEPTH_TEST);
                break;
            }
        }

        // Lights
        GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_POSITION, BufferFactory.floatBuffer(new float[] { 2.0f, 2.0f, 2.0f, 1f}));
        GL11.glLightfv(GL11.GL_LIGHT1, GL11.GL_POSITION, BufferFactory.floatBuffer(new float[] { -2.0f, 2.0f, 2.0f, 1f}));
        GL11.glLightfv(GL11.GL_LIGHT2, GL11.GL_POSITION, BufferFactory.floatBuffer(new float[] { 2.0f, -2.0f, 2.0f, 1f}));
        GL11.glLightfv(GL11.GL_LIGHT3, GL11.GL_POSITION, BufferFactory.floatBuffer(new float[] { -2.0f, -2.0f, 2.0f, 1f}));

        canvas.swapBuffers();

        // NLogger.debug(getClass(), "Frametime: " + (System.currentTimeMillis() - start)); //$NON-NLS-1$
    }

    @Override
    public void dispose() {
        GL20.glUseProgram(0);
        if (pGlossId != -1) {
            if (vsGlossId != -1)
                GL20.glDetachShader(pGlossId, vsGlossId);
            if (fsGlossId != -1)
                GL20.glDetachShader(pGlossId, fsGlossId);
        }
        if (vsGlossId != -1)
            GL20.glDeleteShader(vsGlossId);
        if (fsGlossId != -1)
            GL20.glDeleteShader(fsGlossId);

        if (pGlossId != -1)
            GL20.glDeleteProgram(pGlossId);
        // Dispose all textures
        for (Iterator<GTexture> it = textureSet.iterator() ; it.hasNext();) {
            GTexture tex = it.next();
            NLogger.debug(getClass(), "Dispose texture: {0}", tex); //$NON-NLS-1$
            tex.dispose(this);
            it.remove();
        }
    }

    private int loadGlossFragmentShader() {
        StringBuilder shaderSource = new StringBuilder();
        int shaderID = 0;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(GLShader.class.getResourceAsStream("gloss.frag"), StandardCharsets.UTF_8)); //$NON-NLS-1$
            String line;
            while ((line = reader.readLine()) != null) {
                shaderSource.append(line).append("\n"); //$NON-NLS-1$
            }
            reader.close();
        } catch (IOException e) {
            NLogger.error(OpenGLRenderer20.class, e);
            return -1;
        }

        shaderID = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(shaderID, shaderSource);
        GL20.glCompileShader(shaderID);

        if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            NLogger.error(OpenGLRenderer20.class, new Exception("Could not compile fragment shader.")); //$NON-NLS-1$
            return -1;
        }

        return shaderID;
    }

    private int loadGlossVertexShader() {
        StringBuilder shaderSource = new StringBuilder();
        int shaderID = 0;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(GLShader.class.getResourceAsStream("gloss.vert"), StandardCharsets.UTF_8)); //$NON-NLS-1$
            String line;
            while ((line = reader.readLine()) != null) {
                shaderSource.append(line).append("\n"); //$NON-NLS-1$
            }
            reader.close();
        } catch (IOException e) {
            NLogger.error(OpenGLRenderer20.class, e);
            return -1;
        }

        shaderID = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(shaderID, shaderSource);
        GL20.glCompileShader(shaderID);

        if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            NLogger.error(OpenGLRenderer20.class, new Exception("Could not compile vertex shader.")); //$NON-NLS-1$
            return -1;
        }

        return shaderID;
    }

    private Vector4f getScreenZFrom3D(float x, float y, float z, int w, int h, Matrix4f v) {
        Vector4f relPos = new Vector4f(x, y, z, 1f);
        Matrix4f.transform(v, relPos, relPos);
        float cursorX = 0.5f * w - relPos.x * View.PIXEL_PER_LDU / w;
        float cursorY = 0.5f * h - relPos.y * View.PIXEL_PER_LDU / h;
        relPos.x = cursorX;
        relPos.y = cursorY;
        return relPos;
    }

    private Vector4f get3DCoordinatesFromScreen(int x, int y, float z, int w, int h, Matrix4f vInverse) {
        Vector4f relPos = new Vector4f();
        relPos.x = (0.5f * w - x) / View.PIXEL_PER_LDU;
        relPos.y = (0.5f * h - y) / View.PIXEL_PER_LDU;
        relPos.z = z;
        relPos.w = 1.0f;
        Matrix4f.transform(vInverse, relPos, relPos);
        return relPos;
    }

    private Vector4f get3DCoordinatesFromScreen(float x, float y, float z, Matrix4f vInverse) {
        Vector4f relPos = new Vector4f();
        relPos.x = x;
        relPos.y = y;
        relPos.z = z;
        relPos.w = 1.0f;
        Matrix4f.transform(vInverse, relPos, relPos);
        return relPos;
    }

    public int getBaseImageLoc() {
        return baseImageLoc;
    }

    public int getGlossMapLoc() {
        return glossMapLoc;
    }

    public int getCubeMapLoc() {
        return cubeMapLoc;
    }

    public int getAlphaSwitchLoc() {
        return alphaSwitchLoc;
    }

    public int getNormalSwitchLoc() {
        return normalSwitchLoc;
    }

    public int getNoTextureSwitch() {
        return noTextureSwitch;
    }

    public int getNoGlossMapSwitch() {
        return noGlossMapSwitch;
    }

    public int getNoLightSwitch() {
        return noLightSwitch;
    }

    public int getCubeMapSwitch() {
        return cubeMapSwitch;
    }

    public int getCubeMapMatteLoc() {
        return cubeMapMatteLoc;
    }

    public int getCubeMapMetalLoc() {
        return cubeMapMetalLoc;
    }
}
