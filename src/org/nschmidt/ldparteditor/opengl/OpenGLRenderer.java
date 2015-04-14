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
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.opengl.GLCanvas;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.composites.Composite3D;
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
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.enums.WorkingMode;
import org.nschmidt.ldparteditor.helpers.Arc;
import org.nschmidt.ldparteditor.helpers.Arrow;
import org.nschmidt.ldparteditor.helpers.ArrowBlunt;
import org.nschmidt.ldparteditor.helpers.BufferFactory;
import org.nschmidt.ldparteditor.helpers.Circle;
import org.nschmidt.ldparteditor.helpers.Manipulator;
import org.nschmidt.ldparteditor.helpers.composite3d.ViewIdleManager;
import org.nschmidt.ldparteditor.helpers.math.PowerRay;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * This class draws all 3D models
 *
 * @author nils
 *
 */
public class OpenGLRenderer {

    /** The 3D Composite */
    private final Composite3D c3d;

    /** The transformation matrix buffer of the view [NOT PUBLIC YET] */
    private final FloatBuffer viewport = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer rotation = BufferUtils.createFloatBuffer(16);

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

    public FloatBuffer getViewport() {
        return viewport;
    }

    public OpenGLRenderer(Composite3D c3d) {
        this.c3d = c3d;
    }

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

    /** The set, which stores already loaded textures in-memory. */
    private Set<GTexture> textureSet = new HashSet<GTexture>();

    private int skipFrame;

    /**
     * Registers a texture with a given ID
     *
     * @param ID
     *            The ID of the texture
     */
    public void registerTexture(GTexture tex) {
        textureSet.add(tex);
    }

    /**
     * Initializes the Scene and gives OpenGL-Hints
     */
    public void init() {
        // MARK OpenGL Hints and Initialization
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glDepthFunc(GL11.GL_LESS);
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glClearDepth(1.0f);
        GL11.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
        GL11.glPointSize(4);
        // GL11.glLineWidth(2);

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glShadeModel(GL11.GL_FLAT);

        GL11.glEnable(GL11.GL_NORMALIZE);

        GL11.glEnable(GL11.GL_LIGHT0);
        GL11.glEnable(GL11.GL_LIGHT1);
        GL11.glEnable(GL11.GL_LIGHT2);
        GL11.glEnable(GL11.GL_LIGHT3);

        GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, BufferFactory.floatBuffer(new float[] { 0.09f, 0.09f, 0.09f, 1f }));

        GL11.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, BufferFactory.floatBuffer(new float[] { 0.8f, 0.8f, 0.8f, 1f }));
        GL11.glLight(GL11.GL_LIGHT0, GL11.GL_SPECULAR, BufferFactory.floatBuffer(new float[] { 0.5f, 0.5f, 0.5f, 1f }));
        GL11.glLightf(GL11.GL_LIGHT0, GL11.GL_LINEAR_ATTENUATION, .001f);

        GL11.glLight(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, BufferFactory.floatBuffer(new float[] { 0.25f, 0.25f, 0.25f, 1f }));
        GL11.glLight(GL11.GL_LIGHT1, GL11.GL_SPECULAR, BufferFactory.floatBuffer(new float[] { 0.0f, 0.0f, 0.0f, 1f }));
        GL11.glLightf(GL11.GL_LIGHT1, GL11.GL_LINEAR_ATTENUATION, .001f);

        GL11.glLight(GL11.GL_LIGHT2, GL11.GL_DIFFUSE, BufferFactory.floatBuffer(new float[] { 0.25f, 0.25f, 0.25f, 1f }));
        GL11.glLight(GL11.GL_LIGHT2, GL11.GL_SPECULAR, BufferFactory.floatBuffer(new float[] { 0.0f, 0.0f, 0.0f, 1f }));
        GL11.glLightf(GL11.GL_LIGHT2, GL11.GL_LINEAR_ATTENUATION, .001f);

        GL11.glLight(GL11.GL_LIGHT3, GL11.GL_DIFFUSE, BufferFactory.floatBuffer(new float[] { 0.25f, 0.25f, 0.25f, 1f }));
        GL11.glLight(GL11.GL_LIGHT3, GL11.GL_SPECULAR, BufferFactory.floatBuffer(new float[] { 0.0f, 0.0f, 0.0f, 1f }));
        GL11.glLightf(GL11.GL_LIGHT3, GL11.GL_LINEAR_ATTENUATION, .001f);

        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glColorMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT_AND_DIFFUSE);

        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SPECULAR, BufferFactory.floatBuffer(new float[] { 1.0f, 1.0f, 1.0f, 1.0f }));
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
    public void drawScene() {

        final int negDet = c3d.hasNegDeterminant();
        final boolean raytraceMode = c3d.getRenderMode() == 5;

        final GLCanvas canvas = c3d.getCanvas();

        if (!canvas.isCurrent()) {
            canvas.setCurrent();
            try {
                GLContext.useContext(canvas);
            } catch (LWJGLException e) {
                NLogger.error(OpenGLRenderer.class, e);
            }
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
            GL20.glUseProgram(pGlossId);
        } else {
            GL20.glUseProgram(0);
        }

        int state3d = 0;
        if (c3d.isAnaglyph3d() && !raytraceMode) {
            GL11.glColorMask(true, false, false, true);
        } else {
            GL11.glColorMask(true, true, true, true);
        }
        while (true) {

            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);

            Rectangle bounds = c3d.getBounds();
            GL11.glViewport(0, 0, bounds.width, bounds.height);
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();
            float viewport_width = bounds.width / View.PIXEL_PER_LDU / 2.0f;
            float viewport_height = bounds.height / View.PIXEL_PER_LDU / 2.0f;
            GL11.glOrtho(viewport_width, -viewport_width, viewport_height, -viewport_height, -c3d.getzNear() * c3d.getZoom(), c3d.getzFar() * c3d.getZoom());

            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadIdentity();

            Matrix4f viewport_transform = new Matrix4f();
            Matrix4f.setIdentity(viewport_transform);

            final float zoom = c3d.getZoom();
            Matrix4f.scale(new Vector3f(zoom, zoom, zoom), viewport_transform, viewport_transform);
            Matrix4f viewport_rotation = c3d.getRotation();
            viewport_rotation.store(rotation);
            rotation.flip();
            Matrix4f.mul(viewport_rotation, viewport_transform, viewport_transform);
            Matrix4f viewport_translation = c3d.getTranslation();
            Matrix4f.mul(viewport_transform, viewport_translation, viewport_transform);
            viewport_transform.store(viewport);
            c3d.setViewport(viewport_transform);
            viewport.flip();
            GL11.glLoadMatrix(viewport);

            if (c3d.isAnaglyph3d() && !raytraceMode) {

                Matrix4f viewport_rotation2 = new Matrix4f();

                float rx = 0;
                double val = 0d;
                if (zoom <= 1.0E-6)
                    val = Math.PI / zoom / 2000000000f; // TODO 3D Constants
                // need to be excluded /
                // customisable
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

                Vector4f yAxis4f_rotation = new Vector4f(0f, 1.0f, 0f, 1f);
                Vector4f xAxis4f_translation = new Vector4f(0f, 0f, 0f, 1f);
                Matrix4f ovr_inverse = Matrix4f.invert(viewport_rotation, null);
                Matrix4f.transform(ovr_inverse, yAxis4f_rotation, yAxis4f_rotation);
                Matrix4f.transform(ovr_inverse, xAxis4f_translation, xAxis4f_translation);
                Vector3f yAxis3f_rotation = new Vector3f(yAxis4f_rotation.x, yAxis4f_rotation.y, yAxis4f_rotation.z);
                Vector3f xAxis3f_translation = new Vector3f(xAxis4f_translation.x, xAxis4f_translation.y, xAxis4f_translation.z);
                Matrix4f.rotate(rx, yAxis3f_rotation, viewport_rotation, viewport_rotation2);
                Matrix4f.translate(xAxis3f_translation, viewport_rotation2, viewport_rotation2);

                Matrix4f viewport_transform2 = new Matrix4f();
                Matrix4f.setIdentity(viewport_transform2);
                Matrix4f.scale(new Vector3f(zoom, zoom, zoom), viewport_transform2, viewport_transform2);

                Matrix4f.mul(viewport_rotation2, viewport_transform2, viewport_transform2);

                viewport_translation = c3d.getTranslation();
                Matrix4f.mul(viewport_transform2, viewport_translation, viewport_transform2);
                viewport_transform2.store(viewport);
                viewport.flip();
                GL11.glLoadMatrix(viewport);

            }

            GL11.glEnable(GL11.GL_CULL_FACE);
            switch (negDet) {
            case 0:
                GL11.glFrontFace(GL11.GL_CW);
                break;
            case 1:
                GL11.glFrontFace(GL11.GL_CCW);
                break;
            }

            GL11.glCullFace(GL11.GL_BACK);

            // GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);

            if (c3d.isLightOn())
                GL11.glEnable(GL11.GL_LIGHTING);

            c3d.setDrawingSolidMaterials(true);
            c3d.getLockableDatFileReference().draw(c3d);
            if (raytraceMode) {
                Rectangle b = c3d.getCanvas().getBounds();
                final int w =  b.width;
                final int h =  b.height;
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
                Rectangle b = c3d.getCanvas().getBounds();
                final float w =  b.width;
                final float h =  b.height;
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
                            final float xf = 2f * viewport_width;
                            final float yf = 2f * viewport_height;
                            GL11.glTranslatef(-viewport_width, -viewport_height, 0f);
                            GL11.glScalef(1f / w * xf, 1f / h * yf, 1f);
                            // FIXME Needs adjutments for negative determinants!
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
                    raytracer = new Thread(new Runnable() {
                        @Override
                        public void run() {
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
                                    } catch (InterruptedException e) {}
                                }
                                counter++;

                                final int cs = solidColours[0].length;
                                final float[] sc = Arrays.copyOf(solidColours[0], cs);
                                final float[] tc = Arrays.copyOf(transparentColours[0], cs);
                                final int w = (int) cWidth[0];
                                final int h = (int) cHeight[0];

                                final float[] ray;
                                final Vector3f ray3f;
                                final Vector3f ray3f2 = new Vector3f(-20f, -20f, 100f);
                                ray3f2.normalise();
                                final Matrix4f vInverse = c3d.getViewport_Inverse();
                                final Matrix4f vM = c3d.getViewport();
                                final float z = 100f;
                                {
                                    // FIXME Negative Determinant check is needed somewhere???
                                    Vector4f zAxis4f = new Vector4f(0, 0, 1f, 1f);
                                    Matrix4f ovr_inverse2 = Matrix4f.invert(c3d.getRotation(), null);
                                    Matrix4f.transform(ovr_inverse2, zAxis4f, zAxis4f);
                                    Vector4f ray2 = (Vector4f) new Vector4f(zAxis4f.x, zAxis4f.y, zAxis4f.z, 0f).normalise();
                                    ray = new float[]{ray2.x, ray2.y, ray2.z};
                                    ray3f = new Vector3f(ray[0], ray[1], ray[2]);
                                }

                                needData.decrementAndGet();
                                needData.decrementAndGet();

                                NLogger.debug(getClass(), "Initialised raytracer."); //$NON-NLS-1$
                                final boolean lights = c3d.isLightOn();
                                // Read triangles and quads
                                final ArrayList<float[]> tris = new ArrayList<float[]>();
                                {
                                    HashMap<GData4, Vertex[]> quads = c3d.getLockableDatFileReference().getVertexManager().getQuads();
                                    HashMap<GData3, Vertex[]> tris2 = c3d.getLockableDatFileReference().getVertexManager().getTriangles();
                                    for (GData3 g : tris2.keySet()) {
                                        Vertex[] v = tris2.get(g);
                                        Vector4f[] nv = new Vector4f[3];
                                        {
                                            boolean notShown = true;
                                            float max_x = -Float.MAX_VALUE;
                                            float min_x = Float.MAX_VALUE;
                                            float max_y = -Float.MAX_VALUE;
                                            float min_y = Float.MAX_VALUE;
                                            for(int i = 0; i < 3; i++) {
                                                Vector4f sz = getScreenZFrom3D(v[i].x, v[i].y, v[i].z, w, h, vM);
                                                nv[i] = sz;
                                                max_x = Math.max(max_x, sz.x);
                                                min_x = Math.min(min_x, sz.x);
                                                max_y = Math.max(max_y, sz.y);
                                                min_y = Math.min(min_y, sz.y);
                                            }

                                            Rectangle bounds = new Rectangle(0, 0, w, h);
                                            Rectangle boundingBox = new Rectangle((int) min_x, (int) min_y, (int) (max_x - min_x), (int) (max_y - min_y));

                                            if (boundingBox.intersects(bounds) || boundingBox.contains(0, 0) || boundingBox.contains(bounds.width, bounds.height) || boundingBox.contains(bounds.width, 0)
                                                    || boundingBox.contains(0, bounds.height) || bounds.contains(boundingBox.x, boundingBox.y) || bounds.contains(boundingBox.x, boundingBox.y + boundingBox.height)
                                                    || bounds.contains(boundingBox.x + boundingBox.width, boundingBox.y) || bounds.contains(boundingBox.x + boundingBox.width, boundingBox.y + boundingBox.height)) {
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
                                        normal.normalise();
                                        normal.negate();
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
                                    for (GData4 g : quads.keySet()) {
                                        Vertex[] v = quads.get(g);
                                        Vector4f[] nv = new Vector4f[4];
                                        {
                                            boolean notShown = true;
                                            float max_x = -Float.MAX_VALUE;
                                            float min_x = Float.MAX_VALUE;
                                            float max_y = -Float.MAX_VALUE;
                                            float min_y = Float.MAX_VALUE;
                                            for(int i = 0; i < 4; i++) {
                                                Vector4f sz = getScreenZFrom3D(v[i].x, v[i].y, v[i].z, w, h, vM);
                                                nv[i] = sz;
                                                max_x = Math.max(max_x, sz.x);
                                                min_x = Math.min(min_x, sz.x);
                                                max_y = Math.max(max_y, sz.y);
                                                min_y = Math.min(min_y, sz.y);
                                            }

                                            Rectangle bounds = new Rectangle(0, 0, w, h);
                                            Rectangle boundingBox = new Rectangle((int) min_x, (int) min_y, (int) (max_x - min_x), (int) (max_y - min_y));

                                            if (boundingBox.intersects(bounds) || boundingBox.contains(0, 0) || boundingBox.contains(bounds.width, bounds.height) || boundingBox.contains(bounds.width, 0)
                                                    || boundingBox.contains(0, bounds.height) || bounds.contains(boundingBox.x, boundingBox.y) || bounds.contains(boundingBox.x, boundingBox.y + boundingBox.height)
                                                    || bounds.contains(boundingBox.x + boundingBox.width, boundingBox.y) || bounds.contains(boundingBox.x + boundingBox.width, boundingBox.y + boundingBox.height)) {
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
                                        normal.normalise();

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

                                final ArrayList<float[]> points = new ArrayList<float[]>(10000);


                                final LinkedList<float[]> speckles = new LinkedList<float[]>();
                                final LinkedList<float[]> glitters = new LinkedList<float[]>();
                                final HashMap<float[], Long> specklesCreation = new HashMap<float[], Long>();
                                final HashMap<float[], Long> glittersCreation = new HashMap<float[], Long>();

                                // FIXME Needs implementation

                                // Light positions
                                final Vector3f lp1 = new Vector3f(-2.0f, -2.0f, 2.0f);
                                final Vector3f lp2 = new Vector3f(-2.0f, 2.0f, 2.0f);
                                final Vector3f lp3 = new Vector3f(2.0f, -2.0f, 2.0f);
                                final Vector3f lp4 = new Vector3f(2.0f, 2.0f, 2.0f);

                                {

                                    final Lock lock = new ReentrantLock();

                                    final int chunks = Math.max(View.NUM_CORES - 1, 1);
                                    final Thread[] threads = new Thread[chunks];
                                    for (int j = 0; j < chunks; ++j) {
                                        final int[] start = new int[] { j };
                                        final int[] ti = new int[] { start[0] * 4 * w};
                                        threads[j] = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                final Random tRnd = new Random(12348729642643L * start[0]);
                                                ArrayList<float[]> points2 = new ArrayList<float[]>(10000 / chunks);
                                                final PowerRay pr = new PowerRay();
                                                final int s = start[0];
                                                int i = ti[0];
                                                int skip = 0;
                                                for (int y = s; y < h; y += chunks) {
                                                    final int sy = h - y - 1;
                                                    for (int x = 0; x < w; x++) {
                                                        final int sx = w - x - 1;
                                                        float rS = sc[i];
                                                        float gS = sc[i + 1];
                                                        float bS = sc[i + 2];
                                                        float rT = tc[i];
                                                        float gT = tc[i + 1];
                                                        float bT = tc[i + 2];
                                                        if (rS != rT || gS != gT || bS != bT) {
                                                            TreeMap<Float, float[]>  zSort = new TreeMap<Float, float[]>();
                                                            TreeMap<Float, Vector4f>  hitSort = new TreeMap<Float, Vector4f>();

                                                            for (float[] tri : tris) {
                                                                float[] zHit = pr.TRIANGLE_INTERSECT(get3DCoordinatesFromScreen(x, y, z, w, h, vInverse), ray, tri);
                                                                if (zHit != null) {
                                                                    Vector4f sz = getScreenZFrom3D(zHit[0], zHit[1], zHit[2], w, h, vM);
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
                                                                point[6] = sy + 1;
                                                                point[7] = sx + 1;
                                                                point[8] = sy + 1;
                                                                point[9] = sx + 1;
                                                                point[10] = sy;
                                                                points2.add(point);
                                                                break;
                                                            }
                                                            case 1:
                                                            {
                                                                float[] ze = zSort.get(zSort.firstKey());
                                                                GColour c = View.getLDConfigColour((int) ze[16]);
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
                                                                    point[6] = sy + 1;
                                                                    point[7] = sx + 1;
                                                                    point[8] = sy + 1;
                                                                    point[9] = sx + 1;
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
                                                                    float r = ze[12];
                                                                    float g = ze[13];
                                                                    float b = ze[14];
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
                                                                        Random rnd = new Random((long) (129642643f * (1f + r) * (1f + g) * (1f + b)));
                                                                        r = r + Math.abs(sp + v.x * spI) * rnd.nextFloat() / 4f;
                                                                        g = g + Math.abs(sp + v.y * spI) * rnd.nextFloat() / 4f;
                                                                        b = b + Math.abs(sp + v.z * spI) * rnd.nextFloat() / 4f;
                                                                        if (lights) {
                                                                            r = (light + lightSpecular) / 4f + r;
                                                                            g = (light + lightSpecular) / 4f + g;
                                                                            b = (light + lightSpecular) / 4f + b;
                                                                        }
                                                                        break;
                                                                    }
                                                                    case GLITTER:
                                                                    {
                                                                        // FIXME @Needs implementation!
                                                                        float a = ze[15];
                                                                        GCGlitter type = (GCGlitter) ct;
                                                                        final int sSize = glitters.size();
                                                                        float radi = 1000f * type.getMinSize() + (type.getMaxSize() - type.getMinSize()) * tRnd.nextFloat();
                                                                        boolean hit = false;
                                                                        final int glitterCount = 30000;
                                                                        boolean buildable = sSize < glitterCount;
                                                                        Vector4f v = get3DCoordinatesFromScreen(pos.x, pos.y, pos.z, w, h, vInverse);
                                                                        final float px = v.x;
                                                                        final float py = v.y;
                                                                        final float pz = v.z;
                                                                        float[] newGlitter = new float[]{px + tRnd.nextFloat() * radi, py + tRnd.nextFloat() * radi, pz + tRnd.nextFloat() * radi, radi};
                                                                        try {
                                                                            lockGlitter.lock();
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
                                                                                    if (buildable) {
                                                                                        if (dist * 0.5 < radi) {
                                                                                            buildable = false;
                                                                                        }
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
                                                                            r = type.getR();
                                                                            g = type.getG();
                                                                            b = type.getB();
                                                                            a = 1f;
                                                                        } else {
                                                                            r = r * .8f + vari * .2f * r;
                                                                            g = g * .8f + vari * .2f * g;
                                                                            b = b * .8f + vari * .2f * b;
                                                                        }
                                                                        float oneMinusAlpha = 1f - a;
                                                                        if (lights) {
                                                                            float resLight = light + lightSpecular * 2f;
                                                                            r = resLight + r;
                                                                            g = resLight + g;
                                                                            b = resLight + b;
                                                                        }
                                                                        r = r * a + rS * oneMinusAlpha;
                                                                        g = g * a + gS * oneMinusAlpha;
                                                                        b = b * a + bS * oneMinusAlpha;
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
                                                                        Vector4f v = get3DCoordinatesFromScreen(pos.x, pos.y, pos.z, w, h, vInverse);
                                                                        final float px = v.x;
                                                                        final float py = v.y;
                                                                        final float pz = v.z;
                                                                        float[] newSpeckle = new float[]{px + tRnd.nextFloat() * radi, py + tRnd.nextFloat() * radi, pz + tRnd.nextFloat() * radi, radi};
                                                                        try {
                                                                            lockSpeckle.lock();
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
                                                                                    if (buildable) {
                                                                                        if (dist * 0.5 < radi) {
                                                                                            buildable = false;
                                                                                        }
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
                                                                            r = (type.getR() * .8f + vari * .2f * type.getR()) * .5f + r * .5f;
                                                                            g = (type.getG() * .8f + vari * .2f * type.getG()) * .5f + g * .5f;
                                                                            b = (type.getB() * .8f + vari * .2f * type.getB()) * .5f + b * .5f;
                                                                            a = 1f;
                                                                        } else {
                                                                            r = r * .8f + vari * .2f * r;
                                                                            g = g * .8f + vari * .2f * g;
                                                                            b = b * .8f + vari * .2f * b;
                                                                        }
                                                                        float oneMinusAlpha = 1f - a;
                                                                        if (lights) {
                                                                            float resLight = light + lightSpecular * 2f;
                                                                            r = resLight + r;
                                                                            g = resLight + g;
                                                                            b = resLight + b;
                                                                        }
                                                                        r = r * a + rS * oneMinusAlpha;
                                                                        g = g * a + gS * oneMinusAlpha;
                                                                        b = b * a + bS * oneMinusAlpha;
                                                                        break;
                                                                    }
                                                                    default:
                                                                        break;
                                                                    }

                                                                    float[] point = new float[11];
                                                                    point[0] = r;
                                                                    point[1] = g;
                                                                    point[2] = b;
                                                                    point[3] = sx;
                                                                    point[4] = sy;
                                                                    point[5] = sx;
                                                                    point[6] = sy + 1;
                                                                    point[7] = sx + 1;
                                                                    point[8] = sy + 1;
                                                                    point[9] = sx + 1;
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

                                                                for (Float f : zSort.keySet()) {
                                                                    k++;
                                                                    float[] ze = zSort.get(f);
                                                                    float a = ze[15];
                                                                    float r = ze[12];
                                                                    float g = ze[13];
                                                                    float b = ze[14];

                                                                    GColour c = View.getLDConfigColour((int) ze[16]);
                                                                    GColourType ct = c.getType();
                                                                    if (ct == null || GCType.hasCubeMap(ct.type())) {
                                                                        float oneMinusAlpha = 1f - a;
                                                                        if (a == 1f) {
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
                                                                                r = r + light;
                                                                                g = g + light;
                                                                                b = b + light;
                                                                            }

                                                                            point[0] = (r + lightSpecular) * a + point[0] * oneMinusAlpha;
                                                                            point[1] = (g + lightSpecular)  * a + point[1] * oneMinusAlpha;
                                                                            point[2] = (b + lightSpecular) * a + point[2] * oneMinusAlpha;

                                                                        } else {
                                                                            float lightSpecular = 0f;
                                                                            Vector3f normal = new Vector3f(ze[9], ze[10], ze[11]);
                                                                            lightSpecular += Math.pow(Math.max(Vector3f.dot(normal, ray3f2), 0.0), 128f);
                                                                            point[0] = (rT + lightSpecular) * a + point[0] * oneMinusAlpha;
                                                                            point[1] = (gT + lightSpecular) * a + point[1] * oneMinusAlpha;
                                                                            point[2] = (bT + lightSpecular) * a + point[2] * oneMinusAlpha;
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
                                                                            Random rnd = new Random((long) (129642643f * (1f + r) * (1f + g) * (1f + b)));
                                                                            r = r + Math.abs(sp + v.x * spI) * rnd.nextFloat() / 4f;
                                                                            g = g + Math.abs(sp + v.y * spI) * rnd.nextFloat() / 4f;
                                                                            b = b + Math.abs(sp + v.z * spI) * rnd.nextFloat() / 4f;
                                                                            if (lights) {
                                                                                r = (light + lightSpecular) / 4f + r;
                                                                                g = (light + lightSpecular) / 4f + g;
                                                                                b = (light + lightSpecular) / 4f + b;
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
                                                                            Vector4f v = get3DCoordinatesFromScreen(pos.x, pos.y, pos.z, w, h, vInverse);
                                                                            final float px = v.x;
                                                                            final float py = v.y;
                                                                            final float pz = v.z;
                                                                            float[] newGlitter = new float[]{px + tRnd.nextFloat() * radi, py + tRnd.nextFloat() * radi, pz + tRnd.nextFloat() * radi, radi};
                                                                            try {
                                                                                lockGlitter.lock();
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
                                                                                        if (buildable) {
                                                                                            if (dist * 0.5 < radi) {
                                                                                                buildable = false;
                                                                                            }
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
                                                                                r = type.getR();
                                                                                g = type.getG();
                                                                                b = type.getB();
                                                                                a = 1f;
                                                                            } else {
                                                                                r = r * .8f + vari * .2f * r;
                                                                                g = g * .8f + vari * .2f * g;
                                                                                b = b * .8f + vari * .2f * b;
                                                                            }
                                                                            float oneMinusAlpha = 1f - a;
                                                                            if (lights) {
                                                                                float resLight = light + lightSpecular * 2f;
                                                                                r = resLight + r;
                                                                                g = resLight + g;
                                                                                b = resLight + b;
                                                                            }
                                                                            r = r * a + point[0] * oneMinusAlpha;
                                                                            g = g * a + point[1] * oneMinusAlpha;
                                                                            b = b * a + point[2] * oneMinusAlpha;
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
                                                                            Vector4f v = get3DCoordinatesFromScreen(pos.x, pos.y, pos.z, w, h, vInverse);
                                                                            final float px = v.x;
                                                                            final float py = v.y;
                                                                            final float pz = v.z;
                                                                            float[] newSpeckle = new float[]{px + tRnd.nextFloat() * radi, py + tRnd.nextFloat() * radi, pz + tRnd.nextFloat() * radi, radi};
                                                                            try {
                                                                                lockSpeckle.lock();
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
                                                                                        if (buildable) {
                                                                                            if (dist * 0.5 < radi) {
                                                                                                buildable = false;
                                                                                            }
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
                                                                                r = (type.getR() * .8f + vari * .2f * type.getR()) * .5f + r * .5f;
                                                                                g = (type.getG() * .8f + vari * .2f * type.getG()) * .5f + g * .5f;
                                                                                b = (type.getB() * .8f + vari * .2f * type.getB()) * .5f + b * .5f;
                                                                                a = 1f;
                                                                            } else {
                                                                                r = r * .8f + vari * .2f * r;
                                                                                g = g * .8f + vari * .2f * g;
                                                                                b = b * .8f + vari * .2f * b;
                                                                            }
                                                                            float oneMinusAlpha = 1f - a;
                                                                            if (lights) {
                                                                                float resLight = light + lightSpecular * 2f;
                                                                                r = resLight + r;
                                                                                g = resLight + g;
                                                                                b = resLight + b;
                                                                            }
                                                                            r = r * a + point[0] * oneMinusAlpha;
                                                                            g = g * a + point[1] * oneMinusAlpha;
                                                                            b = b * a + point[2] * oneMinusAlpha;
                                                                            break;
                                                                        }
                                                                        default:
                                                                            break;
                                                                        }
                                                                        point[0] = r;
                                                                        point[1] = g;
                                                                        point[2] = b;
                                                                    }
                                                                }
                                                                point[3] = sx;
                                                                point[4] = sy;
                                                                point[5] = sx;
                                                                point[6] = sy + 1;
                                                                point[7] = sx + 1;
                                                                point[8] = sy + 1;
                                                                point[9] = sx + 1;
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
                                                        for (int j = skip; j < size; j++) {
                                                            r[j] = points2.get(j);
                                                        }
                                                        skip = size;
                                                        try {
                                                            lock.lock();
                                                            // Update renderedPoints here!
                                                            renderedPoints[0] = r;
                                                        } finally {
                                                            lock.unlock();
                                                        }
                                                    }
                                                    i += 4 * w * (chunks - 1);
                                                }
                                                try {
                                                    lock.lock();
                                                    points.addAll(points2);
                                                } finally {
                                                    lock.unlock();
                                                }
                                            }


                                        });
                                        threads[j].start();
                                    }
                                    boolean isRunning = true;
                                    while (isRunning) {
                                        try {
                                            Thread.sleep(100);
                                        } catch (InterruptedException e) {
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
                                try {
                                    lock.lock();
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
                                    } catch (InterruptedException e) {}
                                }
                            }
                        }

                        private Vector4f getScreenZFrom3D(float x, float y, float z, int w, int h, Matrix4f v) {
                            Vector4f relPos = new Vector4f(x, y, z, 1f);
                            Matrix4f.transform(v, relPos, relPos);
                            float cursor_x = 0.5f * w - relPos.x * View.PIXEL_PER_LDU / w;
                            float cursor_y = 0.5f * h - relPos.y * View.PIXEL_PER_LDU / h;
                            relPos.x = cursor_x;
                            relPos.y = cursor_y;
                            return relPos;
                        }

                        private Vector4f get3DCoordinatesFromScreen(int x, int y, float z, int w, int h, Matrix4f v_inverse) {
                            Vector4f relPos = new Vector4f();
                            relPos.x = (0.5f * w - x) / View.PIXEL_PER_LDU;
                            relPos.y = (0.5f * h - y) / View.PIXEL_PER_LDU;
                            relPos.z = z;
                            relPos.w = 1.0f;
                            Matrix4f.transform(v_inverse, relPos, relPos);
                            return relPos;
                        }

                        private Vector4f get3DCoordinatesFromScreen(float x, float y, float z, int w, int h, Matrix4f v_inverse) {
                            Vector4f relPos = new Vector4f();
                            relPos.x = x;
                            relPos.y = y;
                            relPos.z = z;
                            relPos.w = 1.0f;
                            Matrix4f.transform(v_inverse, relPos, relPos);
                            return relPos;
                        }
                    });
                    raytracer.start();
                } else {
                    if (needData.get() > 0) {
                        needData.incrementAndGet();
                        while(needData.get() > 0) {
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {}
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
            if (window.getWorkingAction() != WorkingMode.SELECT) {
                final float lineWidth;
                final float cone_height;
                final float cone_width;
                final float circleWidth;
                final float arcWidth;
                final float bluntSize;
                // The size will be set on application start!
                final float moveSize = Manipulator.getTranslate_size();
                final float rotateSize = Manipulator.getRotate_size();
                final float rotateOuterSize = Manipulator.getRotate_outer_size();
                final float scaleSize = Manipulator.getScale_size();
                // mSize has normally a length of 11
                // (lineWidth, cone_height, cone_width, bluntSize, circleWidth, arcWidth,
                // moveSizeFactor, rotateSizeFactor, rotateOuterSizeFactor, scaleSizeFactor
                // and activationTreshold)
                float[] mSize = WorkbenchManager.getUserSettingState().getManipulatorSize();
                if (mSize == null) {
                    // We have no custom manipulator settings yet => create a fake array
                    mSize = new float[]{1f, 1f, 1f, 1f, 1f, 1f};
                }
                switch (Editor3DWindow.getIconsize()) {
                case 2:
                    lineWidth = 4f * mSize[0];
                    cone_height = .030f * mSize[1];
                    cone_width = .008f * mSize[2];
                    bluntSize = .02f * mSize[3];
                    circleWidth = 0.02f * mSize[4];
                    arcWidth = 0.004f * mSize[5];
                    break;
                case 3:
                    lineWidth = 6f * mSize[0];
                    cone_height = .045f * mSize[1];
                    cone_width = .012f * mSize[2];
                    bluntSize = .03f * mSize[3];
                    circleWidth = 0.03f * mSize[4];
                    arcWidth = 0.006f * mSize[5];
                    break;
                case 4:
                    lineWidth = 8f * mSize[0];
                    cone_height = .060f * mSize[1];
                    cone_width = .016f * mSize[2];
                    bluntSize = .04f * mSize[3];
                    circleWidth = 0.04f * mSize[4];
                    arcWidth = 0.008f * mSize[5];
                    break;
                case 5:
                    lineWidth = 9f * mSize[0];
                    cone_height = .075f * mSize[1];
                    cone_width = .018f * mSize[2];
                    bluntSize = .045f * mSize[3];
                    circleWidth = 0.045f * mSize[4];
                    arcWidth = 0.009f * mSize[5];
                    break;
                case 0:
                case 1:
                default:
                    lineWidth = 2f * mSize[0];
                    cone_height = .015f * mSize[1];
                    cone_width = .004f * mSize[2];
                    bluntSize = .01f * mSize[3];
                    circleWidth = 0.01f * mSize[4];
                    arcWidth = 0.002f * mSize[5];
                }
                switch (window.getWorkingAction()) {
                case COMBINED:
                    singleMode = false;
                case ROTATE:
                    c = manipulator.checkManipulatorStatus(1f, 0f, 0f, Manipulator.X_ROTATE, c3d, zoom);
                    new Arc(c.getR(), c.getG(), c.getB(), 1f, manipulator.getXaxis().x, manipulator.getXaxis().y, manipulator.getXaxis().z, rotateSize, arcWidth).draw(mx, my, mz, zoom);

                    if (manipulator.isX_Rotate()) {
                        c = manipulator.checkManipulatorStatus(.5f, 0f, 0f, Manipulator.X_ROTATE_ARROW, c3d, zoom);
                        new Arrow(c.getR(), c.getG(), c.getB(), 1f, rotateSize * manipulator.getX_RotateArrow().x, rotateSize * manipulator.getX_RotateArrow().y, rotateSize * manipulator.getX_RotateArrow().z, cone_height, cone_width, lineWidth)
                        .draw(mx, my, mz, zoom);
                    }

                    c = manipulator.checkManipulatorStatus(0f, 1f, 0f, Manipulator.Y_ROTATE, c3d, zoom);
                    new Arc(c.getR(), c.getG(), c.getB(), 1f, manipulator.getYaxis().x, manipulator.getYaxis().y, manipulator.getYaxis().z, rotateSize, arcWidth).draw(mx, my, mz, zoom);

                    if (manipulator.isY_Rotate()) {
                        c = manipulator.checkManipulatorStatus(0f, .5f, 0f, Manipulator.Y_ROTATE_ARROW, c3d, zoom);
                        new Arrow(c.getR(), c.getG(), c.getB(), 1f, rotateSize * manipulator.getY_RotateArrow().x, rotateSize * manipulator.getY_RotateArrow().y, rotateSize * manipulator.getY_RotateArrow().z, cone_height, cone_width, lineWidth)
                        .draw(mx, my, mz, zoom);
                    }

                    c = manipulator.checkManipulatorStatus(0f, 0f, 1f, Manipulator.Z_ROTATE, c3d, zoom);
                    new Arc(c.getR(), c.getG(), c.getB(), 1f, manipulator.getZaxis().x, manipulator.getZaxis().y, manipulator.getZaxis().z, rotateSize, arcWidth).draw(mx, my, mz, zoom);

                    if (manipulator.isZ_Rotate()) {
                        c = manipulator.checkManipulatorStatus(0f, 0f, .5f, Manipulator.Z_ROTATE_ARROW, c3d, zoom);
                        new Arrow(c.getR(), c.getG(), c.getB(), 1f, rotateSize * manipulator.getZ_RotateArrow().x, rotateSize * manipulator.getZ_RotateArrow().y, rotateSize * manipulator.getZ_RotateArrow().z, cone_height, cone_width, lineWidth)
                        .draw(mx, my, mz, zoom);
                    }

                    Vector4f[] gen = c3d.getGenerator();
                    new Circle(.3f, .3f, .3f, 1f, gen[2].x, gen[2].y, gen[2].z, rotateSize, circleWidth).draw(mx, my, mz, zoom);
                    c = manipulator.checkManipulatorStatus(.85f, .85f, .85f, Manipulator.V_ROTATE, c3d, zoom);
                    new Circle(c.getR(), c.getG(), c.getB(), 1f, gen[2].x, gen[2].y, gen[2].z, rotateOuterSize, circleWidth).draw(mx, my, mz, zoom);

                    if (manipulator.isV_Rotate()) {
                        c = manipulator.checkManipulatorStatus(.7f, .7f, .7f, Manipulator.V_ROTATE_ARROW, c3d, zoom);
                        new Arrow(c.getR(), c.getG(), c.getB(), 1f, rotateOuterSize * manipulator.getV_RotateArrow().x, rotateOuterSize * manipulator.getV_RotateArrow().y, rotateOuterSize * manipulator.getV_RotateArrow().z, cone_height, cone_width, lineWidth)
                        .draw(mx, my, mz, zoom);
                    }
                    if (singleMode)
                        break;
                case SCALE:
                    c = manipulator.checkManipulatorStatus(1f, 0f, 0f, Manipulator.X_SCALE, c3d, zoom);
                    new ArrowBlunt(c.getR(), c.getG(), c.getB(), 1f, scaleSize * manipulator.getXaxis().x, scaleSize * manipulator.getXaxis().y, scaleSize * manipulator.getXaxis().z, bluntSize, lineWidth).draw(mx, my, mz, zoom);
                    c = manipulator.checkManipulatorStatus(0f, 1f, 0f, Manipulator.Y_SCALE, c3d, zoom);
                    new ArrowBlunt(c.getR(), c.getG(), c.getB(), 1f, scaleSize * manipulator.getYaxis().x, scaleSize * manipulator.getYaxis().y, scaleSize * manipulator.getYaxis().z, bluntSize, lineWidth).draw(mx, my, mz, zoom);
                    c = manipulator.checkManipulatorStatus(0f, 0f, 1f, Manipulator.Z_SCALE, c3d, zoom);
                    new ArrowBlunt(c.getR(), c.getG(), c.getB(), 1f, scaleSize * manipulator.getZaxis().x, scaleSize * manipulator.getZaxis().y, scaleSize * manipulator.getZaxis().z, bluntSize, lineWidth).draw(mx, my, mz, zoom);
                    if (singleMode)
                        break;
                case MOVE:
                    c = manipulator.checkManipulatorStatus(1f, 0f, 0f, Manipulator.X_TRANSLATE, c3d, zoom);
                    new Arrow(c.getR(), c.getG(), c.getB(), 1f, moveSize * manipulator.getXaxis().x, moveSize * manipulator.getXaxis().y, moveSize * manipulator.getXaxis().z, cone_height, cone_width, lineWidth).draw(mx, my, mz, zoom);
                    c = manipulator.checkManipulatorStatus(0f, 1f, 0f, Manipulator.Y_TRANSLATE, c3d, zoom);
                    new Arrow(c.getR(), c.getG(), c.getB(), 1f, moveSize * manipulator.getYaxis().x, moveSize * manipulator.getYaxis().y, moveSize * manipulator.getYaxis().z, cone_height, cone_width, lineWidth).draw(mx, my, mz, zoom);
                    c = manipulator.checkManipulatorStatus(0f, 0f, 1f, Manipulator.Z_TRANSLATE, c3d, zoom);
                    new Arrow(c.getR(), c.getG(), c.getB(), 1f, moveSize * manipulator.getZaxis().x, moveSize * manipulator.getZaxis().y, moveSize * manipulator.getZaxis().z, cone_height, cone_width, lineWidth).draw(mx, my, mz, zoom);
                    break;
                default:
                    break;
                }
            }

            // MARK Draw temporary objects for all "Add..." functions here
            if (window.isAddingSomething() && c3d.getLockableDatFileReference().getLastSelectedComposite() != null && c3d.getLockableDatFileReference().getLastSelectedComposite().equals(c3d)) {
                if (window.isAddingVertices()) {
                    // Point for add vertex
                    GL11.glColor3f(0.9f, 0.9f, 0.0f); // TODO Needs custom
                    // colour!
                    GL11.glBegin(GL11.GL_POINTS);
                    Vector4f cursor3D = c3d.getCursorSnapped3D();
                    GL11.glVertex3f(cursor3D.x, cursor3D.y, cursor3D.z);
                    GL11.glEnd();
                } else if (window.isAddingLines()) {
                    Vector4f cur = c3d.getCursorSnapped3D();
                    DatFile dat = c3d.getLockableDatFileReference();
                    Vertex v = dat.getNearestObjVertex1();
                    if (v != null) {
                        GL11.glLineWidth(4f);
                        GL11.glBegin(GL11.GL_LINES);
                        GL11.glColor3f(View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0]);
                        GL11.glVertex3f(v.x, v.y, v.z);
                        GL11.glVertex3f(cur.x, cur.y, cur.z);
                        GL11.glEnd();
                    }
                } else if (window.isAddingTriangles()) {
                    Vector4f cur = c3d.getCursorSnapped3D();
                    DatFile dat = c3d.getLockableDatFileReference();
                    Vertex v = dat.getNearestObjVertex1();
                    if (v != null) {
                        Vertex v2 = dat.getNearestObjVertex2();
                        if (v2 != null) {
                            GL11.glLineWidth(4f);
                            GL11.glBegin(GL11.GL_LINES);
                            GL11.glColor3f(View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0]);
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
                            GL11.glColor3f(View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0]);
                            GL11.glVertex3f(v.x, v.y, v.z);
                            GL11.glVertex3f(cur.x, cur.y, cur.z);
                            GL11.glEnd();
                        }
                    }
                } else if (window.isAddingQuads()) {
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
                                    GL11.glColor3f(View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0]);
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
                                    GL11.glColor3f(View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0]);
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
                                GL11.glColor3f(View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0]);
                                GL11.glVertex3f(v2.x, v2.y, v2.z);
                                GL11.glVertex3f(cur.x, cur.y, cur.z);
                                GL11.glVertex3f(v2.x, v2.y, v2.z);
                                GL11.glVertex3f(v.x, v.y, v.z);
                                GL11.glEnd();
                            }
                        } else {
                            GL11.glLineWidth(4f);
                            GL11.glBegin(GL11.GL_LINES);
                            GL11.glColor3f(View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0]);
                            GL11.glVertex3f(v.x, v.y, v.z);
                            GL11.glVertex3f(cur.x, cur.y, cur.z);
                            GL11.glEnd();
                        }
                    }
                } else if (window.isAddingCondlines()) {
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
                                    GL11.glColor3f(View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0]);
                                    GL11.glVertex3f(cur.x, cur.y, cur.z);
                                    GL11.glVertex3f(v.x, v.y, v.z);
                                    GL11.glEnd();
                                } else {
                                    v = dat.getObjVertex1();
                                    v2 = dat.getObjVertex2();
                                    GL11.glLineWidth(4f);
                                    GL11.glBegin(GL11.GL_LINES);
                                    GL11.glColor3f(View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0]);
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
                                GL11.glColor3f(View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0]);
                                GL11.glVertex3f(v2.x, v2.y, v2.z);
                                GL11.glVertex3f(cur.x, cur.y, cur.z);
                                GL11.glVertex3f(v2.x, v2.y, v2.z);
                                GL11.glVertex3f(v.x, v.y, v.z);
                                GL11.glEnd();
                            }
                        } else {
                            GL11.glLineWidth(4f);
                            GL11.glBegin(GL11.GL_LINES);
                            GL11.glColor3f(View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0]);
                            GL11.glVertex3f(v.x, v.y, v.z);
                            GL11.glVertex3f(cur.x, cur.y, cur.z);
                            GL11.glEnd();
                        }
                    }
                }
            }

            {
                Vector4f selectionEnd_MODELVIEW = new Vector4f(c3d.getCursor3D());
                float viewport_pixel_per_ldu = c3d.getViewportPixelPerLDU();
                float dx = 0;
                float dy = 0;
                dx = 100f / viewport_pixel_per_ldu;
                dy = 100f / viewport_pixel_per_ldu;
                Vector4f xAxis4f_translation = new Vector4f(dx, 0, 0, 1.0f);
                Vector4f yAxis4f_translation = new Vector4f(0, dy, 0, 1.0f);
                Matrix4f ovr_inverse2 = Matrix4f.invert(viewport_rotation, null);
                Matrix4f.transform(ovr_inverse2, xAxis4f_translation, xAxis4f_translation);
                Matrix4f.transform(ovr_inverse2, yAxis4f_translation, yAxis4f_translation);
                Vector4f width = new Vector4f(xAxis4f_translation.x / 2f, xAxis4f_translation.y / 2f, xAxis4f_translation.z / 2f, 1f);
                Vector4f height = new Vector4f(yAxis4f_translation.x / 2f, yAxis4f_translation.y / 2f, yAxis4f_translation.z / 2f, 1f);

                Vector4f selectionCorner1 = new Vector4f(selectionEnd_MODELVIEW.x + width.x, selectionEnd_MODELVIEW.y + width.y, selectionEnd_MODELVIEW.z + width.z, 1f);
                Vector4f selectionCorner2 = new Vector4f(selectionEnd_MODELVIEW.x + height.x, selectionEnd_MODELVIEW.y + height.y, selectionEnd_MODELVIEW.z + height.z, 1f);
                Vector4f selectionCorner3 = new Vector4f(selectionEnd_MODELVIEW.x - width.x, selectionEnd_MODELVIEW.y - width.y, selectionEnd_MODELVIEW.z - width.z, 1f);
                Vector4f selectionCorner4 = new Vector4f(selectionEnd_MODELVIEW.x - height.x, selectionEnd_MODELVIEW.y - height.y, selectionEnd_MODELVIEW.z - height.z, 1f);

                GL11.glLineWidth(2f);
                GL11.glBegin(GL11.GL_LINES);
                GL11.glColor3f(1f, 0f, 0f);
                GL11.glVertex3f(selectionCorner3.x, selectionCorner3.y, selectionCorner3.z);
                GL11.glVertex3f(selectionCorner1.x, selectionCorner1.y, selectionCorner1.z);
                GL11.glColor3f(0f, 0f, 1f);
                GL11.glVertex3f(selectionCorner4.x, selectionCorner4.y, selectionCorner4.z);
                GL11.glVertex3f(selectionCorner2.x, selectionCorner2.y, selectionCorner2.z);

                GL11.glEnd();
            }

            GL11.glEnable(GL11.GL_DEPTH_TEST);

            // MARK Drawing the selection Rectangle
            if (c3d.isDoingSelection()) {
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                Vector4f selectionStart_MODELVIEW = new Vector4f(c3d.getSelectionStart());
                Vector4f selectionEnd_MODELVIEW = new Vector4f(c3d.getCursor3D());
                float viewport_pixel_per_ldu = c3d.getViewportPixelPerLDU();
                float dx = 0;
                float dy = 0;
                dx = (c3d.getOldMousePosition().x - c3d.getMousePosition().x) / viewport_pixel_per_ldu;
                dy = (c3d.getMousePosition().y - c3d.getOldMousePosition().y) / viewport_pixel_per_ldu;
                Vector4f xAxis4f_translation = new Vector4f(dx, 0, 0, 1.0f);
                Vector4f yAxis4f_translation = new Vector4f(0, dy, 0, 1.0f);
                Matrix4f ovr_inverse2 = Matrix4f.invert(viewport_rotation, null);
                Matrix4f.transform(ovr_inverse2, xAxis4f_translation, xAxis4f_translation);
                Matrix4f.transform(ovr_inverse2, yAxis4f_translation, yAxis4f_translation);
                Vector4f width = new Vector4f(xAxis4f_translation.x, xAxis4f_translation.y, xAxis4f_translation.z, 1f);
                Vector4f height = new Vector4f(yAxis4f_translation.x, yAxis4f_translation.y, yAxis4f_translation.z, 1f);

                c3d.getSelectionWidth().set(width);
                c3d.getSelectionHeight().set(height);

                Vector4f selectionCorner1 = new Vector4f(selectionStart_MODELVIEW.x + width.x, selectionStart_MODELVIEW.y + width.y, selectionStart_MODELVIEW.z + width.z, 1f);
                Vector4f selectionCorner2 = new Vector4f(selectionStart_MODELVIEW.x + height.x, selectionStart_MODELVIEW.y + height.y, selectionStart_MODELVIEW.z + height.z, 1f);

                GL11.glLineWidth(3f);

                GL11.glColor3f(1f, 0f, 0f);
                GL11.glBegin(GL11.GL_LINES);

                GL11.glVertex3f(selectionStart_MODELVIEW.x, selectionStart_MODELVIEW.y, selectionStart_MODELVIEW.z);
                GL11.glVertex3f(selectionCorner1.x, selectionCorner1.y, selectionCorner1.z);

                GL11.glVertex3f(selectionStart_MODELVIEW.x, selectionStart_MODELVIEW.y, selectionStart_MODELVIEW.z);
                GL11.glVertex3f(selectionCorner2.x, selectionCorner2.y, selectionCorner2.z);

                GL11.glVertex3f(selectionEnd_MODELVIEW.x, selectionEnd_MODELVIEW.y, selectionEnd_MODELVIEW.z);
                GL11.glVertex3f(selectionCorner1.x, selectionCorner1.y, selectionCorner1.z);

                GL11.glVertex3f(selectionEnd_MODELVIEW.x, selectionEnd_MODELVIEW.y, selectionEnd_MODELVIEW.z);
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
            Vector3f[] viewport_origin_axis = c3d.getViewportOriginAxis();
            float z_offset = 0;
            if (c3d.isGridShown()) {
                // Grid-1 and 10
                for (int r = 0; r < 5; r += 4) {
                    if (r == 4) {
                        GL11.glColor3f(.5f, .5f, .5f);
                        z_offset = 1f;
                        GL11.glLineWidth(2f);
                    } else {
                        GL11.glColor3f(0.15f, 0.15f, 0.15f);
                        z_offset = 0;
                        GL11.glLineWidth(1f);
                    }
                    GL11.glBegin(GL11.GL_LINES);
                    Vector4f grid_center1 = new Vector4f();
                    Vector4f grid_center2 = new Vector4f();
                    grid_center1.set(c3d.getGrid()[r]);
                    grid_center2.set(grid_center1);
                    for (float i = 0f; i < c3d.getGrid()[3 + r].y; i += 1f) {
                        Vector4f.sub(grid_center2, c3d.getGrid()[2 + r], grid_center2);
                        GL11.glVertex3f(viewport_origin_axis[0].x, grid_center1.y, viewport_origin_axis[0].z + z_offset);
                        GL11.glVertex3f(viewport_origin_axis[1].x, grid_center1.y, viewport_origin_axis[1].z + z_offset);
                        GL11.glVertex3f(viewport_origin_axis[0].x, grid_center2.y, viewport_origin_axis[0].z + z_offset);
                        GL11.glVertex3f(viewport_origin_axis[1].x, grid_center2.y, viewport_origin_axis[1].z + z_offset);
                        Vector4f.add(grid_center1, c3d.getGrid()[2 + r], grid_center1);
                    }
                    grid_center1.set(c3d.getGrid()[r]);
                    grid_center2.set(grid_center1);
                    for (float i = 0f; i < c3d.getGrid()[3 + r].x; i += 1f) {
                        Vector4f.sub(grid_center2, c3d.getGrid()[1 + r], grid_center2);
                        GL11.glVertex3f(grid_center2.x, viewport_origin_axis[2].y, viewport_origin_axis[2].z + z_offset);
                        GL11.glVertex3f(grid_center2.x, viewport_origin_axis[3].y, viewport_origin_axis[3].z + z_offset);
                        GL11.glVertex3f(grid_center1.x, viewport_origin_axis[2].y, viewport_origin_axis[2].z + z_offset);
                        GL11.glVertex3f(grid_center1.x, viewport_origin_axis[3].y, viewport_origin_axis[3].z + z_offset);
                        Vector4f.add(grid_center1, c3d.getGrid()[1 + r], grid_center1);
                    }
                    GL11.glEnd();
                }
                z_offset = 2f; // z_offset + 5 + 1f * c3d.getZoom();
            }

            if (c3d.isOriginShown()) {
                // Origin
                GL11.glLineWidth(2f);
                GL11.glBegin(GL11.GL_LINES);
                GL11.glColor3f(0f, 0f, 0f);
                GL11.glVertex3f(viewport_origin_axis[0].x, viewport_origin_axis[0].y, viewport_origin_axis[0].z + z_offset);
                GL11.glVertex3f(viewport_origin_axis[1].x, viewport_origin_axis[1].y, viewport_origin_axis[1].z + z_offset);
                GL11.glVertex3f(viewport_origin_axis[2].x, viewport_origin_axis[2].y, viewport_origin_axis[2].z + z_offset);
                GL11.glVertex3f(viewport_origin_axis[3].x, viewport_origin_axis[3].y, viewport_origin_axis[3].z + z_offset);
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
                    switch (Editor3DWindow.getIconsize()) {
                    case 4:
                    case 5:
                        l = 1f;
                        ox = .1f;
                        oy = .11f;
                        cone_height = .00030f;
                        cone_width = .00008f;
                        line_width = 6f;
                        break;
                    case 2:
                    case 3:
                        l = .75f;
                        ox = .075f;
                        oy = .085f;
                        cone_height = .00023f;
                        cone_width = .00006f;
                        line_width = 4f;
                        break;
                    case 0:
                    case 1:
                    default:
                        l = .5f;
                        ox = .05f;
                        oy = .06f;
                        cone_height = .00015f;
                        cone_width = .00004f;
                        line_width = 2f;
                    }
                    GL11.glPushMatrix();
                    GL11.glTranslatef(ox - viewport_width, viewport_height - oy, 0f);
                    GL11.glMultMatrix(rotation);
                    new Arrow(1f, 0f, 0f, 1f,l, 0f, 0f, cone_height, cone_width, line_width).draw(0f, 0f, 0f, .01f);
                    new Arrow(0f, 1f, 0f, 1f, 0f,l, 0f, cone_height, cone_width, line_width).draw(0f, 0f, 0f, .01f);
                    new Arrow(0f, 0f, 1f, 1f, 0f, 0f,l, cone_height, cone_width, line_width).draw(0f, 0f, 0f, .01f);
                    GL11.glPopMatrix();
                }
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                if (c3d.isShowingLabels() && c3d.isClassicPerspective()) {
                    switch (c3d.getPerspectiveIndex()) {
                    case FRONT:
                        for (PGData3 tri : View.FRONT) {
                            tri.drawText(viewport_width, viewport_height, viewport_origin_axis[0].z);
                        }
                        break;
                    case BACK:
                        for (PGData3 tri : View.BACK) {
                            tri.drawText(viewport_width, viewport_height, viewport_origin_axis[0].z);
                        }
                        break;
                    case TOP:
                        for (PGData3 tri : View.TOP) {
                            tri.drawText(viewport_width, viewport_height, viewport_origin_axis[0].z);
                        }
                        break;
                    case BOTTOM:
                        for (PGData3 tri : View.BOTTOM) {
                            tri.drawText(viewport_width, viewport_height, viewport_origin_axis[0].z);
                        }
                        break;
                    case LEFT:
                        for (PGData3 tri : View.LEFT) {
                            tri.drawText(viewport_width, viewport_height, viewport_origin_axis[0].z);
                        }
                        break;
                    case RIGHT:
                        for (PGData3 tri : View.RIGHT) {
                            tri.drawText(viewport_width, viewport_height, viewport_origin_axis[0].z);
                        }
                        break;
                    case TWO_THIRDS:
                    default:
                        break;
                    }
                }
                if (Project.getFileToEdit().equals(c3d.getLockableDatFileReference())) {
                    if (Project.getFileToEdit().isReadOnly()) {
                        GL11.glColor3f(0f, 0f, 0f);
                    } else {
                        GL11.glColor3f(View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0]);
                    }
                    GL11.glLineWidth(7f);
                    GL11.glBegin(GL11.GL_LINES);
                    GL11.glVertex3f(viewport_width, viewport_height, viewport_origin_axis[3].z);
                    GL11.glVertex3f(viewport_width, -viewport_height, viewport_origin_axis[3].z);
                    GL11.glEnd();
                    GL11.glLineWidth(10f);
                    GL11.glBegin(GL11.GL_LINES);
                    GL11.glVertex3f(-viewport_width, -viewport_height, viewport_origin_axis[3].z);
                    GL11.glVertex3f(-viewport_width, viewport_height, viewport_origin_axis[3].z);
                    GL11.glEnd();
                    GL11.glLineWidth(5f);
                    GL11.glBegin(GL11.GL_LINES);
                    GL11.glVertex3f(-viewport_width, viewport_height, viewport_origin_axis[3].z);
                    GL11.glVertex3f(viewport_width, viewport_height, viewport_origin_axis[3].z);
                    GL11.glEnd();
                    GL11.glLineWidth(10f);
                    GL11.glBegin(GL11.GL_LINES);
                    GL11.glVertex3f(-viewport_width, -viewport_height, viewport_origin_axis[3].z);
                    GL11.glVertex3f(viewport_width, -viewport_height, viewport_origin_axis[3].z);
                    GL11.glEnd();
                }
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                break;
            }
        }

        // Lights
        GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, BufferFactory.floatBuffer(new float[] { 2.0f, 2.0f, 2.0f, 1f}));
        GL11.glLight(GL11.GL_LIGHT1, GL11.GL_POSITION, BufferFactory.floatBuffer(new float[] { -2.0f, 2.0f, 2.0f, 1f}));
        GL11.glLight(GL11.GL_LIGHT2, GL11.GL_POSITION, BufferFactory.floatBuffer(new float[] { 2.0f, -2.0f, 2.0f, 1f}));
        GL11.glLight(GL11.GL_LIGHT3, GL11.GL_POSITION, BufferFactory.floatBuffer(new float[] { -2.0f, -2.0f, 2.0f, 1f}));

        c3d.getCanvas().swapBuffers();
    }

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
            NLogger.debug(getClass(), "Dispose texture: " + tex); //$NON-NLS-1$
            tex.dispose(this);
            it.remove();
        }
    }

    /**
     * Disposes all textures
     */
    public void disposeAllTextures() {
        final GLCanvas canvas = c3d.getCanvas();
        if (!canvas.isCurrent()) {
            canvas.setCurrent();
            try {
                GLContext.useContext(canvas);
            } catch (LWJGLException e) {
                NLogger.error(OpenGLRenderer.class, e);
            }
        }
        for (Iterator<GTexture> it = textureSet.iterator() ; it.hasNext();) {
            GTexture tex = it.next();
            NLogger.debug(getClass(), "Dispose texture: " + tex); //$NON-NLS-1$
            tex.dispose(this);
            it.remove();
        }
    }

    /**
     * Disposes old textures
     */
    public synchronized void disposeOldTextures() {
        final GLCanvas canvas = c3d.getCanvas();
        if (!canvas.isCurrent()) {
            canvas.setCurrent();
            try {
                GLContext.useContext(canvas);
            } catch (LWJGLException e) {
                NLogger.error(OpenGLRenderer.class, e);
            }
        }
        Iterator<GTexture> ti = textureSet.iterator();
        for (GTexture tex = null; ti.hasNext() && (tex = ti.next()) != null;) {
            if (tex.isTooOld()) {
                NLogger.debug(getClass(), "Dispose old texture: " + tex); //$NON-NLS-1$
                tex.dispose(this);
                ti.remove();
            }
        }
    }

    private int loadGlossFragmentShader() {
        StringBuilder shaderSource = new StringBuilder();
        int shaderID = 0;

        try {
            BufferedReader reader = new BufferedReader(new FileReader("gloss.frag")); //$NON-NLS-1$
            String line;
            while ((line = reader.readLine()) != null) {
                shaderSource.append(line).append("\n"); //$NON-NLS-1$
            }
            reader.close();
        } catch (IOException e) {
            NLogger.error(OpenGLRenderer.class, e);
            return -1;
        }

        shaderID = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(shaderID, shaderSource);
        GL20.glCompileShader(shaderID);

        if (GL20.glGetShader(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            NLogger.debug(OpenGLRenderer.class, new Exception("Could not compile shader.")); //$NON-NLS-1$;
            return -1;
        }

        return shaderID;
    }

    private int loadGlossVertexShader() {
        StringBuilder shaderSource = new StringBuilder();
        int shaderID = 0;

        try {
            BufferedReader reader = new BufferedReader(new FileReader("gloss.vert")); //$NON-NLS-1$
            String line;
            while ((line = reader.readLine()) != null) {
                shaderSource.append(line).append("\n"); //$NON-NLS-1$
            }
            reader.close();
        } catch (IOException e) {
            NLogger.error(OpenGLRenderer.class, e);
            return -1;
        }

        shaderID = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(shaderID, shaderSource);
        GL20.glCompileShader(shaderID);

        if (GL20.glGetShader(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            NLogger.debug(OpenGLRenderer.class, new Exception("Could not compile shader.")); //$NON-NLS-1$;
            return -1;
        }

        return shaderID;
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

    public boolean containsOnlyCubeMaps() {
        int counter = 0;
        for (GTexture tex : textureSet) {
            if (tex.getCubeMapIndex() > 0) {
                counter++;
            }
        }
        return textureSet.size() == counter;
    }

    public int getCubeMapMatteLoc() {
        return cubeMapMatteLoc;
    }

    public int getCubeMapMetalLoc() {
        return cubeMapMetalLoc;
    }
}
