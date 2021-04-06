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

import java.nio.FloatBuffer;
import java.util.Iterator;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.widgets.Menu;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GL33ModelRenderer;
import org.nschmidt.ldparteditor.data.GL33ModelRendererLDrawStandard;
import org.nschmidt.ldparteditor.data.GTexture;
import org.nschmidt.ldparteditor.data.PGData3;
import org.nschmidt.ldparteditor.data.Primitive;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.enums.GL33Primitives;
import org.nschmidt.ldparteditor.enums.IconSize;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.enums.WorkingMode;
import org.nschmidt.ldparteditor.helpers.Arc;
import org.nschmidt.ldparteditor.helpers.Arrow;
import org.nschmidt.ldparteditor.helpers.ArrowBlunt;
import org.nschmidt.ldparteditor.helpers.Circle;
import org.nschmidt.ldparteditor.helpers.Manipulator;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.workbench.UserSettingState;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * This class draws the 3D view (OpenGL 3.3 compliant)
 *
 * @author nils
 *
 */
public class OpenGLRenderer33 extends OpenGLRenderer {

    private GLShader shaderProgram = new GLShader();
    private GLShader shaderProgram2 = new GLShader();
    private GLShader shaderProgram2D = new GLShader();
    private GLShader shaderProgramCondline = new GLShader();
    private final GLMatrixStack stack = new GLMatrixStack();
    private final GL33Helper helper = new GL33Helper();
    private final GL33ModelRenderer modelRenderer = new GL33ModelRenderer(c3d, this);
    private final GL33ModelRendererLDrawStandard modelRendererLDrawStandard = new GL33ModelRendererLDrawStandard(c3d, this);

    /** The transformation matrix buffer of the view [NOT PUBLIC YET] */
    private final FloatBuffer view_buf = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer rot_buf = BufferUtils.createFloatBuffer(16);
    private final Matrix4f rotation_inv4f = new Matrix4f();

    private static long hoverSettingsTime = System.currentTimeMillis();

    public OpenGLRenderer33(Composite3D c3d) {
        super(c3d);
    }

    @Override
    public Composite3D getC3D() {
        return c3d;
    }

    @Override
    public void init() {

        if (shaderProgram.isDefault()) shaderProgram = new GLShader("renderer.vert", "renderer.frag"); //$NON-NLS-1$ //$NON-NLS-2$
        if (shaderProgram2.isDefault()) shaderProgram2 = new GLShader("primitive.vert", "primitive.frag"); //$NON-NLS-1$ //$NON-NLS-2$
        if (shaderProgram2D.isDefault()) shaderProgram2D = new GLShader("2D.vert", "2D.frag"); //$NON-NLS-1$ //$NON-NLS-2$
        if (shaderProgramCondline.isDefault()) shaderProgramCondline = new GLShader("condline.vert", "condline.frag"); //$NON-NLS-1$ //$NON-NLS-2$

        shaderProgramCondline.use();

        GL20.glUniform1f(shaderProgramCondline.getUniformLocation("showAll"), c3d.getLineMode() == 1 ? 1f : 0f); //$NON-NLS-1$
        GL20.glUniform1f(shaderProgramCondline.getUniformLocation("condlineMode"), c3d.getRenderMode() == 6 ? 1f : 0f); //$NON-NLS-1$

        stack.setShader(shaderProgram);
        shaderProgram.use();
        shaderProgram.texmapOff();

        {
            GL20.glUniform1f(shaderProgram.getUniformLocation("l0_r"), View.light1_Colour_r[0]); //$NON-NLS-1$
            GL20.glUniform1f(shaderProgram.getUniformLocation("l0_g"), View.light1_Colour_g[0]); //$NON-NLS-1$
            GL20.glUniform1f(shaderProgram.getUniformLocation("l0_b"), View.light1_Colour_b[0]); //$NON-NLS-1$
            GL20.glUniform1f(shaderProgram.getUniformLocation("l0s_r"), View.light1_specular_Colour_r[0]); //$NON-NLS-1$
            GL20.glUniform1f(shaderProgram.getUniformLocation("l0s_g"), View.light1_specular_Colour_g[0]); //$NON-NLS-1$
            GL20.glUniform1f(shaderProgram.getUniformLocation("l0s_b"), View.light1_specular_Colour_b[0]); //$NON-NLS-1$

            GL20.glUniform1f(shaderProgram.getUniformLocation("l1_r"), View.light2_Colour_r[0]); //$NON-NLS-1$
            GL20.glUniform1f(shaderProgram.getUniformLocation("l1_g"), View.light2_Colour_g[0]); //$NON-NLS-1$
            GL20.glUniform1f(shaderProgram.getUniformLocation("l1_b"), View.light2_Colour_b[0]); //$NON-NLS-1$
            GL20.glUniform1f(shaderProgram.getUniformLocation("l1s_r"), View.light2_specular_Colour_r[0]); //$NON-NLS-1$
            GL20.glUniform1f(shaderProgram.getUniformLocation("l1s_g"), View.light2_specular_Colour_g[0]); //$NON-NLS-1$
            GL20.glUniform1f(shaderProgram.getUniformLocation("l1s_b"), View.light2_specular_Colour_b[0]); //$NON-NLS-1$

            GL20.glUniform1f(shaderProgram.getUniformLocation("l2_r"), View.light3_Colour_r[0]); //$NON-NLS-1$
            GL20.glUniform1f(shaderProgram.getUniformLocation("l2_g"), View.light3_Colour_g[0]); //$NON-NLS-1$
            GL20.glUniform1f(shaderProgram.getUniformLocation("l2_b"), View.light3_Colour_b[0]); //$NON-NLS-1$
            GL20.glUniform1f(shaderProgram.getUniformLocation("l2s_r"), View.light3_specular_Colour_r[0]); //$NON-NLS-1$
            GL20.glUniform1f(shaderProgram.getUniformLocation("l2s_g"), View.light3_specular_Colour_g[0]); //$NON-NLS-1$
            GL20.glUniform1f(shaderProgram.getUniformLocation("l2s_b"), View.light3_specular_Colour_b[0]); //$NON-NLS-1$

            GL20.glUniform1f(shaderProgram.getUniformLocation("l3_r"), View.light4_Colour_r[0]); //$NON-NLS-1$
            GL20.glUniform1f(shaderProgram.getUniformLocation("l3_g"), View.light4_Colour_g[0]); //$NON-NLS-1$
            GL20.glUniform1f(shaderProgram.getUniformLocation("l3_b"), View.light4_Colour_b[0]); //$NON-NLS-1$
            GL20.glUniform1f(shaderProgram.getUniformLocation("l3s_r"), View.light4_specular_Colour_r[0]); //$NON-NLS-1$
            GL20.glUniform1f(shaderProgram.getUniformLocation("l3s_g"), View.light4_specular_Colour_g[0]); //$NON-NLS-1$
            GL20.glUniform1f(shaderProgram.getUniformLocation("l3s_b"), View.light4_specular_Colour_b[0]); //$NON-NLS-1$

            shaderProgram.setFactor(1f);
        }

        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glDepthFunc(GL11.GL_LESS);
        GL11.glClearDepth(1.0f);
        GL11.glClearColor(View.background_Colour_r[0], View.background_Colour_g[0], View.background_Colour_b[0], 1.0f);

        GL11.glPointSize(5);

        modelRenderer.init();
        modelRendererLDrawStandard.init();
    }

    @Override
    public void drawScene() {

        final long start = System.currentTimeMillis();
        final UserSettingState userSettings = WorkbenchManager.getUserSettingState();

        final boolean negDet = c3d.hasNegDeterminant();
        final boolean ldrawStandardMode = c3d.getRenderMode() == 5;

        final GLCanvas canvas = c3d.getCanvas();

        if (!canvas.isCurrent()) {
            canvas.setCurrent();
            GL.setCapabilities(c3d.getCapabilities());
        }

        final Editor3DWindow window = Editor3DWindow.getWindow();

        // MARK OpenGL Draw Scene

        stack.clear();
        helper.createVBO();

        int state3d = 0;
        if (c3d.isAnaglyph3d() && !ldrawStandardMode) {
            GL11.glColorMask(true, false, false, true);
        } else {
            GL11.glColorMask(true, true, true, true);
        }
        while (true) {

            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);

            Rectangle bounds = c3d.getBounds();
            Rectangle scaledBounds = c3d.getScaledBounds();
            GL11.glViewport(0, 0, scaledBounds.width, scaledBounds.height);

            shaderProgram.use();
            stack.setShader(shaderProgram);
            stack.glLoadIdentity();

            float viewportWidth = bounds.width / View.PIXEL_PER_LDU / 2.0f;
            float viewportHeight = bounds.height / View.PIXEL_PER_LDU / 2.0f;
            {
                final FloatBuffer projectionBuf = BufferUtils.createFloatBuffer(16);
                GLMatrixStack.glOrtho(viewportWidth, -viewportWidth, viewportHeight, -viewportHeight, -c3d.getzNear() * c3d.getZoom(), c3d.getzFar() * c3d.getZoom()).store(projectionBuf);
                projectionBuf.position(0);
                shaderProgram2D.use();
                int projection = shaderProgram2D.getUniformLocation("projection" ); //$NON-NLS-1$
                GL20.glUniformMatrix4fv(projection, false, projectionBuf);
                shaderProgram2.use();
                projection = shaderProgram2.getUniformLocation("projection" ); //$NON-NLS-1$
                GL20.glUniformMatrix4fv(projection, false, projectionBuf);
                shaderProgramCondline.use();
                projection = shaderProgramCondline.getUniformLocation("projection" ); //$NON-NLS-1$
                GL20.glUniformMatrix4fv(projection, false, projectionBuf);
                shaderProgram.use();
                projection = shaderProgram.getUniformLocation("projection" ); //$NON-NLS-1$
                GL20.glUniformMatrix4fv(projection, false, projectionBuf);
            }

            Matrix4f viewportTransform = new Matrix4f();
            Matrix4f.setIdentity(viewportTransform);

            final float zoom = c3d.getZoom();
            Matrix4f.scale(new Vector3f(zoom, zoom, zoom), viewportTransform, viewportTransform);
            Matrix4f viewportRotation = c3d.getRotation();
            viewportRotation.store(rot_buf);
            rot_buf.flip();
            Matrix4f.load(viewportRotation, rotation_inv4f);
            rotation_inv4f.invert();
            Matrix4f.mul(viewportRotation, viewportTransform, viewportTransform);
            Matrix4f viewportTranslation = c3d.getTranslation();
            Matrix4f.mul(viewportTransform, viewportTranslation, viewportTransform);
            viewportTransform.store(view_buf);
            view_buf.flip();
            c3d.setViewport(viewportTransform);

            {
                shaderProgram2D.use();
                int view = shaderProgram2D.getUniformLocation("view" ); //$NON-NLS-1$
                GL20.glUniformMatrix4fv(view, false, view_buf);
                shaderProgram2.use();
                view = shaderProgram2.getUniformLocation("view" ); //$NON-NLS-1$
                GL20.glUniformMatrix4fv(view, false, view_buf);
                shaderProgramCondline.use();
                view = shaderProgramCondline.getUniformLocation("view" ); //$NON-NLS-1$
                GL20.glUniformMatrix4fv(view, false, view_buf);
                view = shaderProgramCondline.getUniformLocation("zoom" ); //$NON-NLS-1$
                GL20.glUniform1f(view, zoom);
                shaderProgram.use();
                view = shaderProgram.getUniformLocation("view" ); //$NON-NLS-1$
                GL20.glUniformMatrix4fv(view, false, view_buf);
                view = shaderProgram.getUniformLocation("rotation" ); //$NON-NLS-1$
                GL20.glUniformMatrix4fv(view, false, rot_buf);
            }

            if (c3d.isAnaglyph3d() && !ldrawStandardMode) {

                Matrix4f viewportRotation2 = new Matrix4f();

                float rx = 0;
                double val = 0d;
                if (zoom <= 1.0E-6)
                    val = Math.PI / zoom / 2000000000f; // TODO 3D Constants need to be excluded / customisable
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
                viewportTransform2.store(view_buf);
                view_buf.flip();
                {
                    shaderProgram2D.use();
                    int view = shaderProgram2D.getUniformLocation("view" ); //$NON-NLS-1$
                    GL20.glUniformMatrix4fv(view, false, view_buf);
                    shaderProgram2.use();
                    view = shaderProgram2.getUniformLocation("view" ); //$NON-NLS-1$
                    GL20.glUniformMatrix4fv(view, false, view_buf);
                    shaderProgramCondline.use();
                    view = shaderProgramCondline.getUniformLocation("view" ); //$NON-NLS-1$
                    GL20.glUniformMatrix4fv(view, false, view_buf);
                    shaderProgram.use();
                    view = shaderProgram.getUniformLocation("view" ); //$NON-NLS-1$
                    GL20.glUniformMatrix4fv(view, false, view_buf);
                }
            }

            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_CULL_FACE);
            if (negDet) {
                GL11.glFrontFace(GL11.GL_CCW);
            } else {
                GL11.glFrontFace(GL11.GL_CW);
            }

            GL11.glCullFace(GL11.GL_BACK);

            if (c3d.isLightOn()) {
                shaderProgram.lightsOn();
            } else {
                shaderProgram.lightsOff();
            }

            if (ldrawStandardMode) {
                modelRendererLDrawStandard.draw(stack, shaderProgram, shaderProgramCondline, shaderProgram2D, true, c3d.getLockableDatFileReference());
            } else {
                modelRenderer.draw(stack, shaderProgram, shaderProgramCondline, shaderProgram2D, true);
            }

            if (window.getCompositePrimitive().isDoingDND()) {
                final Primitive p = c3d.getDraggedPrimitive();
                if (p != null) {
                    Vector4f cur = c3d.getCursorSnapped3D();
                    GL33HelperPrimitives.backupVBO_PrimitiveArea();
                    GL33HelperPrimitives.createVBO_PrimitiveArea();
                    stack.setShader(shaderProgram2);
                    shaderProgram2.use();
                    p.drawGL33(stack, cur.x, cur.y, cur.z);
                    stack.setShader(shaderProgram);
                    shaderProgram.use();
                    GL33HelperPrimitives.destroyVBO_PrimitiveArea();
                    GL33HelperPrimitives.restoreVBO_PrimitiveArea();
                }
            } else {
                c3d.setDraggedPrimitive(null);
            }

            if (ldrawStandardMode) {
                modelRendererLDrawStandard.draw(stack, shaderProgram, shaderProgramCondline, shaderProgram2D, false, c3d.getLockableDatFileReference());
            } else {
                modelRenderer.draw(stack, shaderProgram, shaderProgramCondline, shaderProgram2D, false);
            }

            stack.setShader(shaderProgram2);
            shaderProgram2.use();

            GL11.glDisable(GL11.GL_DEPTH_TEST);

            Manipulator manipulator = c3d.getManipulator();
            final float mx = manipulator.getPosition().x;
            final float my = manipulator.getPosition().y;
            final float mz = manipulator.getPosition().z;

            // MARK Manipulator
            boolean singleMode = true;
            GColour c;
            if (!ldrawStandardMode && window.getWorkingAction() != WorkingMode.SELECT) {
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
                case 4:
                case 5:
                    lineWidth = 4f * mSize[0];
                    cone_height = .030f * mSize[1];
                    cone_width = .008f * mSize[2];
                    bluntSize = .02f * mSize[3];
                    circleWidth = (negDet ? -1f : 1f) * 0.02f * mSize[4];
                    arcWidth = 0.004f * mSize[5];
                    break;
                case -1:
                case 0:
                default:
                    lineWidth = 2f * mSize[0];
                    cone_height = .015f * mSize[1];
                    cone_width = .004f * mSize[2];
                    bluntSize = .01f * mSize[3];
                    circleWidth = (negDet ? -1f : 1f) * 0.01f * mSize[4];
                    arcWidth = 0.002f * mSize[5];
                }
                switch (window.getWorkingAction()) {
                case COMBINED:
                    singleMode = false;
                case ROTATE:

                    c = manipulator.checkManipulatorStatus(View.x_axis_Colour_r[0], View.x_axis_Colour_g[0], View.x_axis_Colour_b[0], Manipulator.X_ROTATE, c3d, zoom);
                    new Arc(c.getR(), c.getG(), c.getB(), manipulator.getXaxis().x, manipulator.getXaxis().y, manipulator.getXaxis().z, rotateSize, arcWidth).drawGL33(stack, mx, my, mz, zoom);

                    if (manipulator.isX_Rotate()) {
                        c = manipulator.checkManipulatorStatus(View.manipulator_x_axis_Colour_r[0], View.manipulator_x_axis_Colour_g[0], View.manipulator_x_axis_Colour_b[0], Manipulator.X_ROTATE_ARROW, c3d, zoom);
                        new Arrow(c.getR(), c.getG(), c.getB(), rotateSize * manipulator.getX_RotateArrow().x, rotateSize * manipulator.getX_RotateArrow().y, rotateSize * manipulator.getX_RotateArrow().z, cone_height, cone_width, lineWidth)
                        .drawGL33_RGB(stack, mx, my, mz, zoom);
                    }

                    c = manipulator.checkManipulatorStatus(View.y_axis_Colour_r[0], View.y_axis_Colour_g[0], View.y_axis_Colour_b[0], Manipulator.Y_ROTATE, c3d, zoom);
                    new Arc(c.getR(), c.getG(), c.getB(), manipulator.getYaxis().x, manipulator.getYaxis().y, manipulator.getYaxis().z, rotateSize, arcWidth).drawGL33(stack, mx, my, mz, zoom);

                    if (manipulator.isY_Rotate()) {
                        c = manipulator.checkManipulatorStatus(View.manipulator_y_axis_Colour_r[0], View.manipulator_y_axis_Colour_g[0], View.manipulator_y_axis_Colour_b[0], Manipulator.Y_ROTATE_ARROW, c3d, zoom);
                        new Arrow(c.getR(), c.getG(), c.getB(), rotateSize * manipulator.getY_RotateArrow().x, rotateSize * manipulator.getY_RotateArrow().y, rotateSize * manipulator.getY_RotateArrow().z, cone_height, cone_width, lineWidth)
                        .drawGL33_RGB(stack, mx, my, mz, zoom);
                    }

                    c = manipulator.checkManipulatorStatus(View.z_axis_Colour_r[0], View.z_axis_Colour_g[0], View.z_axis_Colour_b[0], Manipulator.Z_ROTATE, c3d, zoom);
                    new Arc(c.getR(), c.getG(), c.getB(), manipulator.getZaxis().x, manipulator.getZaxis().y, manipulator.getZaxis().z, rotateSize, arcWidth).drawGL33(stack, mx, my, mz, zoom);

                    if (manipulator.isZ_Rotate()) {
                        c = manipulator.checkManipulatorStatus(View.manipulator_z_axis_Colour_r[0], View.manipulator_z_axis_Colour_g[0], View.manipulator_z_axis_Colour_b[0], Manipulator.Z_ROTATE_ARROW, c3d, zoom);
                        new Arrow(c.getR(), c.getG(), c.getB(), rotateSize * manipulator.getZ_RotateArrow().x, rotateSize * manipulator.getZ_RotateArrow().y, rotateSize * manipulator.getZ_RotateArrow().z, cone_height, cone_width, lineWidth)
                        .drawGL33_RGB(stack, mx, my, mz, zoom);
                    }

                    Vector4f[] gen = c3d.getGenerator();
                    new Circle(View.manipulator_innerCircle_Colour_r[0], View.manipulator_innerCircle_Colour_g[0], View.manipulator_innerCircle_Colour_b[0], gen[2].x, gen[2].y, gen[2].z, rotateSize, circleWidth).drawGL33(stack, mx, my, mz, zoom);
                    c = manipulator.checkManipulatorStatus(View.manipulator_outerCircle_Colour_r[0], View.manipulator_outerCircle_Colour_g[0], View.manipulator_outerCircle_Colour_b[0], Manipulator.V_ROTATE, c3d, zoom);
                    new Circle(c.getR(), c.getG(), c.getB(), gen[2].x, gen[2].y, gen[2].z, rotateOuterSize, circleWidth).drawGL33(stack, mx, my, mz, zoom);

                    if (manipulator.isV_Rotate()) {
                        c = manipulator.checkManipulatorStatus(View.manipulator_outerCircle_Colour_r[0], View.manipulator_outerCircle_Colour_g[0], View.manipulator_outerCircle_Colour_b[0], Manipulator.V_ROTATE_ARROW, c3d, zoom);
                        new Arrow(c.getR(), c.getG(), c.getB(), rotateOuterSize * manipulator.getV_RotateArrow().x, rotateOuterSize * manipulator.getV_RotateArrow().y, rotateOuterSize * manipulator.getV_RotateArrow().z, cone_height, cone_width, lineWidth)
                        .drawGL33_RGB(stack, mx, my, mz, zoom);
                    }
                    if (singleMode)
                        break;
                case SCALE:
                    c = manipulator.checkManipulatorStatus(View.x_axis_Colour_r[0], View.x_axis_Colour_g[0], View.x_axis_Colour_b[0], Manipulator.X_SCALE, c3d, zoom);
                    new ArrowBlunt(c.getR(), c.getG(), c.getB(), scaleSize * manipulator.getXaxis().x, scaleSize * manipulator.getXaxis().y, scaleSize * manipulator.getXaxis().z, bluntSize, lineWidth).drawGL33_RGB(stack, mx, my, mz, zoom);
                    c = manipulator.checkManipulatorStatus(View.y_axis_Colour_r[0], View.y_axis_Colour_g[0], View.y_axis_Colour_b[0], Manipulator.Y_SCALE, c3d, zoom);
                    new ArrowBlunt(c.getR(), c.getG(), c.getB(), scaleSize * manipulator.getYaxis().x, scaleSize * manipulator.getYaxis().y, scaleSize * manipulator.getYaxis().z, bluntSize, lineWidth).drawGL33_RGB(stack, mx, my, mz, zoom);
                    c = manipulator.checkManipulatorStatus(View.z_axis_Colour_r[0], View.z_axis_Colour_g[0], View.z_axis_Colour_b[0], Manipulator.Z_SCALE, c3d, zoom);
                    new ArrowBlunt(c.getR(), c.getG(), c.getB(), scaleSize * manipulator.getZaxis().x, scaleSize * manipulator.getZaxis().y, scaleSize * manipulator.getZaxis().z, bluntSize, lineWidth).drawGL33_RGB(stack, mx, my, mz, zoom);
                    if (singleMode)
                        break;
                case MOVE:
                    c = manipulator.checkManipulatorStatus(View.x_axis_Colour_r[0], View.x_axis_Colour_g[0], View.x_axis_Colour_b[0], Manipulator.X_TRANSLATE, c3d, zoom);
                    new Arrow(c.getR(), c.getG(), c.getB(), moveSize * manipulator.getXaxis().x, moveSize * manipulator.getXaxis().y, moveSize * manipulator.getXaxis().z, cone_height, cone_width, lineWidth).drawGL33_RGB(stack, mx, my, mz, zoom);
                    c = manipulator.checkManipulatorStatus(View.y_axis_Colour_r[0], View.y_axis_Colour_g[0], View.y_axis_Colour_b[0], Manipulator.Y_TRANSLATE, c3d, zoom);
                    new Arrow(c.getR(), c.getG(), c.getB(), moveSize * manipulator.getYaxis().x, moveSize * manipulator.getYaxis().y, moveSize * manipulator.getYaxis().z, cone_height, cone_width, lineWidth).drawGL33_RGB(stack, mx, my, mz, zoom);
                    c = manipulator.checkManipulatorStatus(View.z_axis_Colour_r[0], View.z_axis_Colour_g[0], View.z_axis_Colour_b[0], Manipulator.Z_TRANSLATE, c3d, zoom);
                    new Arrow(c.getR(), c.getG(), c.getB(), moveSize * manipulator.getZaxis().x, moveSize * manipulator.getZaxis().y, moveSize * manipulator.getZaxis().z, cone_height, cone_width, lineWidth).drawGL33_RGB(stack, mx, my, mz, zoom);
                    break;
                case MOVE_GLOBAL:
                    c = manipulator.checkManipulatorStatus(View.x_axis_Colour_r[0], View.x_axis_Colour_g[0], View.x_axis_Colour_b[0], Manipulator.X_TRANSLATE, c3d, zoom);
                    new Arrow(c.getR(), c.getG(), c.getB(), moveSize, 0f, 0f, cone_height, cone_width, lineWidth).drawGL33_RGB(stack, mx, my, mz, zoom);
                    c = manipulator.checkManipulatorStatus(View.y_axis_Colour_r[0], View.y_axis_Colour_g[0], View.y_axis_Colour_b[0], Manipulator.Y_TRANSLATE, c3d, zoom);
                    new Arrow(c.getR(), c.getG(), c.getB(), 0f, moveSize, 0f, cone_height, cone_width, lineWidth).drawGL33_RGB(stack, mx, my, mz, zoom);
                    c = manipulator.checkManipulatorStatus(View.z_axis_Colour_r[0], View.z_axis_Colour_g[0], View.z_axis_Colour_b[0], Manipulator.Z_TRANSLATE, c3d, zoom);
                    new Arrow(c.getR(), c.getG(), c.getB(), 0f, 0f, moveSize, cone_height, cone_width, lineWidth).drawGL33_RGB(stack, mx, my, mz, zoom);
                    break;
                default:
                    break;
                }
            }

            // MARK Draw temporary objects for all "Add..." functions here
            if (window.isAddingSomething() && c3d.getLockableDatFileReference().getLastSelectedComposite() != null && c3d.getLockableDatFileReference().getLastSelectedComposite().equals(c3d)) {
                if (window.isAddingVertices()) {
                    // Point for add vertex
                    final Vector4f cursor3D = c3d.getCursorSnapped3D();
                    final int VBO = GL15.glGenBuffers();
                    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
                    GL15.glBufferData(GL15.GL_ARRAY_BUFFER,
                            new float[] {
                                    cursor3D.x, cursor3D.y, cursor3D.z,
                                    View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0]}
                    , GL15.GL_STREAM_DRAW);
                    GL20.glEnableVertexAttribArray(0);
                    GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 24, 0);
                    GL20.glEnableVertexAttribArray(1);
                    GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 24, 12);
                    GL11.glDrawArrays(GL11.GL_POINTS, 0, 1);
                    GL15.glDeleteBuffers(VBO);
                } else if (window.isAddingLines() || window.isAddingDistance()) {
                    Vector4f cur = c3d.getCursorSnapped3D();
                    DatFile dat = c3d.getLockableDatFileReference();
                    Vertex v = dat.getNearestObjVertex1();
                    if (v != null) {
                        GL11.glLineWidth(4f);
                        final int VBO = GL15.glGenBuffers();
                        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
                        GL15.glBufferData(GL15.GL_ARRAY_BUFFER,
                                new float[] {
                                        v.x, v.y, v.z,
                                        View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0],
                                        cur.x, cur.y, cur.z,
                                        View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0]}
                        , GL15.GL_STREAM_DRAW);
                        GL20.glEnableVertexAttribArray(0);
                        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 24, 0);
                        GL20.glEnableVertexAttribArray(1);
                        GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 24, 12);
                        GL11.glDrawArrays(GL11.GL_LINES, 0, 2);
                        GL15.glDeleteBuffers(VBO);
                    }
                } else if (window.isAddingTriangles() || window.isAddingProtractor()) {
                    Vector4f cur = c3d.getCursorSnapped3D();
                    DatFile dat = c3d.getLockableDatFileReference();
                    Vertex v = dat.getNearestObjVertex1();
                    if (v != null) {
                        Vertex v2 = dat.getNearestObjVertex2();
                        if (v2 != null) {
                            GL11.glLineWidth(4f);
                            final int VBO = GL15.glGenBuffers();
                            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
                            GL15.glBufferData(GL15.GL_ARRAY_BUFFER,
                                    new float[] {
                                            v.x, v.y, v.z,
                                            View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0],
                                            cur.x, cur.y, cur.z,
                                            View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0],
                                            v2.x, v2.y, v2.z,
                                            View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0],
                                            cur.x, cur.y, cur.z,
                                            View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0],
                                            v2.x, v2.y, v2.z,
                                            View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0],
                                            v.x, v.y, v.z,
                                            View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0]}
                            , GL15.GL_STREAM_DRAW);
                            GL20.glEnableVertexAttribArray(0);
                            GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 24, 0);
                            GL20.glEnableVertexAttribArray(1);
                            GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 24, 12);
                            GL11.glDrawArrays(GL11.GL_LINES, 0, 6);
                            GL15.glDeleteBuffers(VBO);
                        } else {
                            GL11.glLineWidth(4f);
                            final int VBO = GL15.glGenBuffers();
                            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
                            GL15.glBufferData(GL15.GL_ARRAY_BUFFER,
                                    new float[] {
                                            v.x, v.y, v.z,
                                            View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0],
                                            cur.x, cur.y, cur.z,
                                            View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0]}
                            , GL15.GL_STREAM_DRAW);
                            GL20.glEnableVertexAttribArray(0);
                            GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 24, 0);
                            GL20.glEnableVertexAttribArray(1);
                            GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 24, 12);
                            GL11.glDrawArrays(GL11.GL_LINES, 0, 2);
                            GL15.glDeleteBuffers(VBO);
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
                                    final int VBO = GL15.glGenBuffers();
                                    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
                                    GL15.glBufferData(GL15.GL_ARRAY_BUFFER,
                                            new float[] {
                                                    v2.x, v2.y, v2.z,
                                                    View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0],
                                                    cur.x, cur.y, cur.z,
                                                    View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0],
                                                    v2.x, v2.y, v2.z,
                                                    View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0],
                                                    v.x, v.y, v.z,
                                                    View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0]}
                                    , GL15.GL_STREAM_DRAW);
                                    GL20.glEnableVertexAttribArray(0);
                                    GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 24, 0);
                                    GL20.glEnableVertexAttribArray(1);
                                    GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 24, 12);
                                    GL11.glDrawArrays(GL11.GL_LINES, 0, 4);
                                    GL15.glDeleteBuffers(VBO);
                                } else {
                                    v = dat.getObjVertex1();
                                    v2 = dat.getObjVertex2();
                                    GL11.glLineWidth(4f);
                                    final int VBO = GL15.glGenBuffers();
                                    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
                                    GL15.glBufferData(GL15.GL_ARRAY_BUFFER,
                                            new float[] {
                                                    v.x, v.y, v.z,
                                                    View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0],
                                                    v2.x, v2.y, v2.z,
                                                    View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0],
                                                    v2.x, v2.y, v2.z,
                                                    View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0],
                                                    v3.x, v3.y, v3.z,
                                                    View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0],
                                                    v3.x, v3.y, v3.z,
                                                    View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0],
                                                    cur.x, cur.y, cur.z,
                                                    View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0],
                                                    cur.x, cur.y, cur.z,
                                                    View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0],
                                                    v.x, v.y, v.z,
                                                    View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0]}
                                    , GL15.GL_STREAM_DRAW);
                                    GL20.glEnableVertexAttribArray(0);
                                    GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 24, 0);
                                    GL20.glEnableVertexAttribArray(1);
                                    GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 24, 12);
                                    GL11.glDrawArrays(GL11.GL_LINES, 0, 8);
                                    GL15.glDeleteBuffers(VBO);
                                }
                            } else {
                                GL11.glLineWidth(4f);
                                final int VBO = GL15.glGenBuffers();
                                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
                                GL15.glBufferData(GL15.GL_ARRAY_BUFFER,
                                        new float[] {
                                                v2.x, v2.y, v2.z,
                                                View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0],
                                                cur.x, cur.y, cur.z,
                                                View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0],
                                                v2.x, v2.y, v2.z,
                                                View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0],
                                                v.x, v.y, v.z,
                                                View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0]}
                                , GL15.GL_STREAM_DRAW);
                                GL20.glEnableVertexAttribArray(0);
                                GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 24, 0);
                                GL20.glEnableVertexAttribArray(1);
                                GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 24, 12);
                                GL11.glDrawArrays(GL11.GL_LINES, 0, 4);
                                GL15.glDeleteBuffers(VBO);
                            }
                        } else {
                            GL11.glLineWidth(4f);
                            final int VBO = GL15.glGenBuffers();
                            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
                            GL15.glBufferData(GL15.GL_ARRAY_BUFFER,
                                    new float[] {
                                            v.x, v.y, v.z,
                                            View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0],
                                            cur.x, cur.y, cur.z,
                                            View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0]}
                            , GL15.GL_STREAM_DRAW);
                            GL20.glEnableVertexAttribArray(0);
                            GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 24, 0);
                            GL20.glEnableVertexAttribArray(1);
                            GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 24, 12);
                            GL11.glDrawArrays(GL11.GL_LINES, 0, 2);
                            GL15.glDeleteBuffers(VBO);
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
                                    final int VBO = GL15.glGenBuffers();
                                    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
                                    GL15.glBufferData(GL15.GL_ARRAY_BUFFER,
                                            new float[] {
                                                    v.x, v.y, v.z,
                                                    View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0],
                                                    cur.x, cur.y, cur.z,
                                                    View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0]}
                                    , GL15.GL_STREAM_DRAW);
                                    GL20.glEnableVertexAttribArray(0);
                                    GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 24, 0);
                                    GL20.glEnableVertexAttribArray(1);
                                    GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 24, 12);
                                    GL11.glDrawArrays(GL11.GL_LINES, 0, 2);
                                    GL15.glDeleteBuffers(VBO);
                                } else {
                                    v = dat.getObjVertex1();
                                    v2 = dat.getObjVertex2();
                                    GL11.glLineWidth(4f);
                                    final int VBO = GL15.glGenBuffers();
                                    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
                                    GL15.glBufferData(GL15.GL_ARRAY_BUFFER,
                                            new float[] {
                                                    v.x, v.y, v.z,
                                                    View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0],
                                                    v2.x, v2.y, v2.z,
                                                    View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0],
                                                    v2.x, v2.y, v2.z,
                                                    View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0],
                                                    v3.x, v3.y, v3.z,
                                                    View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0],
                                                    v2.x, v2.y, v2.z,
                                                    View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0],
                                                    cur.x, cur.y, cur.z,
                                                    View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0]}
                                    , GL15.GL_STREAM_DRAW);
                                    GL20.glEnableVertexAttribArray(0);
                                    GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 24, 0);
                                    GL20.glEnableVertexAttribArray(1);
                                    GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 24, 12);
                                    GL11.glDrawArrays(GL11.GL_LINES, 0, 8);
                                    GL15.glDeleteBuffers(VBO);
                                }
                            } else {
                                GL11.glLineWidth(4f);
                                final int VBO = GL15.glGenBuffers();
                                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
                                GL15.glBufferData(GL15.GL_ARRAY_BUFFER,
                                        new float[] {
                                                v2.x, v2.y, v2.z,
                                                View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0],
                                                cur.x, cur.y, cur.z,
                                                View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0],
                                                v2.x, v2.y, v2.z,
                                                View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0],
                                                v.x, v.y, v.z,
                                                View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0]}
                                , GL15.GL_STREAM_DRAW);
                                GL20.glEnableVertexAttribArray(0);
                                GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 24, 0);
                                GL20.glEnableVertexAttribArray(1);
                                GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 24, 12);
                                GL11.glDrawArrays(GL11.GL_LINES, 0, 4);
                                GL15.glDeleteBuffers(VBO);
                            }
                        } else {
                            GL11.glLineWidth(4f);
                            final int VBO = GL15.glGenBuffers();
                            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
                            GL15.glBufferData(GL15.GL_ARRAY_BUFFER,
                                    new float[] {
                                            v.x, v.y, v.z,
                                            View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0],
                                            cur.x, cur.y, cur.z,
                                            View.add_Object_Colour_r[0], View.add_Object_Colour_g[0], View.add_Object_Colour_b[0]}
                            , GL15.GL_STREAM_DRAW);
                            GL20.glEnableVertexAttribArray(0);
                            GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 24, 0);
                            GL20.glEnableVertexAttribArray(1);
                            GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 24, 12);
                            GL11.glDrawArrays(GL11.GL_LINES, 0, 2);
                            GL15.glDeleteBuffers(VBO);
                        }
                    }
                }
            }

            if (!ldrawStandardMode && DatFile.getLastHoveredComposite() == c3d) {
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

                helper.drawLinesRGB_General(new float[]{
                        selectionCorner3.x, selectionCorner3.y, selectionCorner3.z,
                        View.cursor1_Colour_r[0], View.cursor1_Colour_g[0], View.cursor1_Colour_b[0],
                        selectionCorner1.x, selectionCorner1.y, selectionCorner1.z,
                        View.cursor1_Colour_r[0], View.cursor1_Colour_g[0], View.cursor1_Colour_b[0],
                        selectionCorner4.x, selectionCorner4.y, selectionCorner4.z,
                        View.cursor2_Colour_r[0], View.cursor2_Colour_g[0], View.cursor2_Colour_b[0],
                        selectionCorner2.x, selectionCorner2.y, selectionCorner2.z,
                        View.cursor2_Colour_r[0], View.cursor2_Colour_g[0], View.cursor2_Colour_b[0]
                });
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

                helper.drawLinesRGB_General(new float[]{

                        selectionStartMODELVIEW.x, selectionStartMODELVIEW.y, selectionStartMODELVIEW.z,
                        View.rubberBand_Colour_r[0], View.rubberBand_Colour_g[0], View.rubberBand_Colour_b[0],
                        selectionCorner1.x, selectionCorner1.y, selectionCorner1.z,
                        View.rubberBand_Colour_r[0], View.rubberBand_Colour_g[0], View.rubberBand_Colour_b[0],
                        selectionStartMODELVIEW.x, selectionStartMODELVIEW.y, selectionStartMODELVIEW.z,
                        View.rubberBand_Colour_r[0], View.rubberBand_Colour_g[0], View.rubberBand_Colour_b[0],
                        selectionCorner2.x, selectionCorner2.y, selectionCorner2.z,
                        View.rubberBand_Colour_r[0], View.rubberBand_Colour_g[0], View.rubberBand_Colour_b[0],

                        selectionEndMODELVIEW.x, selectionEndMODELVIEW.y, selectionEndMODELVIEW.z,
                        View.rubberBand_Colour_r[0], View.rubberBand_Colour_g[0], View.rubberBand_Colour_b[0],
                        selectionCorner1.x, selectionCorner1.y, selectionCorner1.z,
                        View.rubberBand_Colour_r[0], View.rubberBand_Colour_g[0], View.rubberBand_Colour_b[0],

                        selectionEndMODELVIEW.x, selectionEndMODELVIEW.y, selectionEndMODELVIEW.z,
                        View.rubberBand_Colour_r[0], View.rubberBand_Colour_g[0], View.rubberBand_Colour_b[0],
                        selectionCorner2.x, selectionCorner2.y, selectionCorner2.z,
                        View.rubberBand_Colour_r[0], View.rubberBand_Colour_g[0], View.rubberBand_Colour_b[0]});

                GL11.glEnable(GL11.GL_DEPTH_TEST);

            } else {
                c3d.getSelectionWidth().set(0.0001f, 0.0001f, 0.0001f);
                c3d.getSelectionHeight().set(0.0001f, 0.0001f, 0.0001f);
            }

            // To make it easier to draw and calculate the grid and the origin,
            // reset the transformation matrix ;)
            stack.glLoadIdentity();
            Vector3f[] viewportOriginAxis = c3d.getViewportOriginAxis();
            float zOffset = 0;
            if (c3d.isGridShown()) {
                // Grid-1 and 10
                float gR;
                float gG;
                float gB;
                for (int r = 0; r < 5; r += 4) {
                    if (r == 4) {
                        gR = View.grid10_Colour_r[0];
                        gG = View.grid10_Colour_g[0];
                        gB = View.grid10_Colour_b[0];
                        zOffset = 1f;
                        GL11.glLineWidth(2f);
                    } else {
                        gR = View.grid_Colour_r[0];
                        gG = View.grid_Colour_g[0];
                        gB = View.grid_Colour_b[0];
                        zOffset = 0;
                        GL11.glLineWidth(1f);
                    }
                    Vector4f gridCenter1 = new Vector4f();
                    Vector4f gridCenter2 = new Vector4f();
                    gridCenter1.set(c3d.getGrid()[r]);
                    gridCenter2.set(gridCenter1);

                    int size = 0;
                    float limit = c3d.getGrid()[3 + r].y;
                    for (float i = 0f; i < limit; i += 1f) {
                        size = size + 24;
                    }
                    limit = c3d.getGrid()[3 + r].x;
                    for (float i = 0f; i < limit; i += 1f) {
                        size = size + 24;
                    }

                    final float[] vertices = new float[size];
                    int j = 0;

                    limit = c3d.getGrid()[3 + r].y;
                    for (float i = 0f; i < limit; i += 1f) {
                        Vector4f.sub(gridCenter2, c3d.getGrid()[2 + r], gridCenter2);
                        vertices[j] = viewportOriginAxis[0].x; j++;
                        vertices[j] = gridCenter1.y; j++;
                        vertices[j] = viewportOriginAxis[0].z + zOffset; j++;
                        vertices[j] = gR; j++;
                        vertices[j] = gG; j++;
                        vertices[j] = gB; j++;
                        vertices[j] = viewportOriginAxis[1].x; j++;
                        vertices[j] = gridCenter1.y; j++;
                        vertices[j] = viewportOriginAxis[1].z + zOffset; j++;
                        vertices[j] = gR; j++;
                        vertices[j] = gG; j++;
                        vertices[j] = gB; j++;
                        vertices[j] = viewportOriginAxis[0].x; j++;
                        vertices[j] = gridCenter2.y; j++;
                        vertices[j] = viewportOriginAxis[0].z + zOffset; j++;
                        vertices[j] = gR; j++;
                        vertices[j] = gG; j++;
                        vertices[j] = gB; j++;
                        vertices[j] = viewportOriginAxis[1].x; j++;
                        vertices[j] = gridCenter2.y; j++;
                        vertices[j] = viewportOriginAxis[1].z + zOffset; j++;
                        vertices[j] = gR; j++;
                        vertices[j] = gG; j++;
                        vertices[j] = gB; j++;
                        Vector4f.add(gridCenter1, c3d.getGrid()[2 + r], gridCenter1);
                    }
                    gridCenter1.set(c3d.getGrid()[r]);
                    gridCenter2.set(gridCenter1);
                    limit = c3d.getGrid()[3 + r].x;
                    for (float i = 0f; i < limit; i += 1f) {
                        Vector4f.sub(gridCenter2, c3d.getGrid()[1 + r], gridCenter2);
                        vertices[j] = gridCenter2.x; j++;
                        vertices[j] = viewportOriginAxis[2].y; j++;
                        vertices[j] = viewportOriginAxis[2].z + zOffset; j++;
                        vertices[j] = gR; j++;
                        vertices[j] = gG; j++;
                        vertices[j] = gB; j++;
                        vertices[j] = gridCenter2.x; j++;
                        vertices[j] = viewportOriginAxis[3].y; j++;
                        vertices[j] = viewportOriginAxis[3].z + zOffset; j++;
                        vertices[j] = gR; j++;
                        vertices[j] = gG; j++;
                        vertices[j] = gB; j++;
                        vertices[j] = gridCenter1.x; j++;
                        vertices[j] = viewportOriginAxis[2].y; j++;
                        vertices[j] = viewportOriginAxis[2].z + zOffset; j++;
                        vertices[j] = gR; j++;
                        vertices[j] = gG; j++;
                        vertices[j] = gB; j++;
                        vertices[j] = gridCenter1.x; j++;
                        vertices[j] = viewportOriginAxis[3].y; j++;
                        vertices[j] = viewportOriginAxis[3].z + zOffset; j++;
                        vertices[j] = gR; j++;
                        vertices[j] = gG; j++;
                        vertices[j] = gB; j++;
                        Vector4f.add(gridCenter1, c3d.getGrid()[1 + r], gridCenter1);
                    }
                    helper.drawLinesRGB_General(vertices);
                }
                zOffset = 2f;
            }

            if (c3d.isOriginShown()) {
                // Origin
                GL11.glLineWidth(2f);
                helper.drawLinesRGB_General(new float[]{
                        viewportOriginAxis[0].x, viewportOriginAxis[0].y, viewportOriginAxis[0].z + zOffset,
                        View.origin_Colour_r[0], View.origin_Colour_g[0], View.origin_Colour_b[0],
                        viewportOriginAxis[1].x, viewportOriginAxis[1].y, viewportOriginAxis[1].z + zOffset,
                        View.origin_Colour_r[0], View.origin_Colour_g[0], View.origin_Colour_b[0],
                        viewportOriginAxis[2].x, viewportOriginAxis[2].y, viewportOriginAxis[2].z + zOffset,
                        View.origin_Colour_r[0], View.origin_Colour_g[0], View.origin_Colour_b[0],
                        viewportOriginAxis[3].x, viewportOriginAxis[3].y, viewportOriginAxis[3].z + zOffset,
                        View.origin_Colour_r[0], View.origin_Colour_g[0], View.origin_Colour_b[0]});
            }

            if (c3d.isAnaglyph3d() && !ldrawStandardMode && state3d == 0) {
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
                    stack.glPushMatrix();
                    stack.glTranslatef(ox - viewportWidth, viewportHeight - oy, 0f);
                    stack.glMultMatrixf(viewportRotation);
                    new Arrow(View.x_axis_Colour_r[0], View.x_axis_Colour_g[0], View.x_axis_Colour_b[0], l,  0f, 0f, cone_height, cone_width, line_width).drawGL33_RGB(stack, 0f, 0f, 0f, .01f);
                    new Arrow(View.y_axis_Colour_r[0], View.y_axis_Colour_g[0], View.y_axis_Colour_b[0], 0f, l,  0f, cone_height, cone_width, line_width).drawGL33_RGB(stack, 0f, 0f, 0f, .01f);
                    new Arrow(View.z_axis_Colour_r[0], View.z_axis_Colour_g[0], View.z_axis_Colour_b[0], 0f, 0f, l,  cone_height, cone_width, line_width).drawGL33_RGB(stack, 0f, 0f, 0f, .01f);
                    stack.glPopMatrix();
                }
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                if (c3d.isShowingLabels() && c3d.isClassicPerspective()) {
                    PGData3.beginDrawTextGL33(shaderProgram2D);
                    stack.setShader(shaderProgram2D);
                    stack.glLoadIdentity();
                    switch (c3d.getPerspectiveIndex()) {
                    case FRONT:
                        for (PGData3 tri : View.FRONT) {
                            tri.drawTextGL33(viewportWidth, viewportHeight, viewportOriginAxis[0].z);
                        }
                        break;
                    case BACK:
                        for (PGData3 tri : View.BACK) {
                            tri.drawTextGL33(viewportWidth, viewportHeight, viewportOriginAxis[0].z);
                        }
                        break;
                    case TOP:
                        for (PGData3 tri : View.TOP) {
                            tri.drawTextGL33(viewportWidth, viewportHeight, viewportOriginAxis[0].z);
                        }
                        break;
                    case BOTTOM:
                        for (PGData3 tri : View.BOTTOM) {
                            tri.drawTextGL33(viewportWidth, viewportHeight, viewportOriginAxis[0].z);
                        }
                        break;
                    case LEFT:
                        for (PGData3 tri : View.LEFT) {
                            tri.drawTextGL33(viewportWidth, viewportHeight, viewportOriginAxis[0].z);
                        }
                        break;
                    case RIGHT:
                        for (PGData3 tri : View.RIGHT) {
                            tri.drawTextGL33(viewportWidth, viewportHeight, viewportOriginAxis[0].z);
                        }
                        break;
                    case TWO_THIRDS:
                    default:
                        break;
                    }
                    PGData3.endDrawTextGL33(shaderProgram2);
                    stack.setShader(shaderProgram2);
                }
                if (Project.getFileToEdit().equals(c3d.getLockableDatFileReference())) {
                    final float r;
                    final float g;
                    final float b;

                    if (Project.getFileToEdit().isReadOnly()) {
                        r = View.text_Colour_r[0];
                        g = View.text_Colour_g[0];
                        b = View.text_Colour_b[0];
                    } else if (c3d.equals(Project.getFileToEdit().getLastSelectedComposite())) {
                        r = 1f - View.vertex_selected_Colour_r[0];
                        g = 1f - View.vertex_selected_Colour_g[0];
                        b = 1f - View.vertex_selected_Colour_b[0];
                    } else {
                        r = View.vertex_selected_Colour_r[0];
                        g = View.vertex_selected_Colour_g[0];
                        b = View.vertex_selected_Colour_b[0];
                    }
                    GL11.glLineWidth(7f);
                    helper.drawLinesRGB_General(new float[]{
                            viewportWidth, viewportHeight, viewportOriginAxis[3].z,
                            r, g, b,
                            viewportWidth, -viewportHeight, viewportOriginAxis[3].z,
                            r, g, b
                    });
                    GL11.glLineWidth(10f);
                    helper.drawLinesRGB_General(new float[]{
                            -viewportWidth, -viewportHeight, viewportOriginAxis[3].z,
                            r, g, b,
                            -viewportWidth, viewportHeight, viewportOriginAxis[3].z,
                            r, g, b,
                    });
                    GL11.glLineWidth(5f);
                    helper.drawLinesRGB_General(new float[]{
                            -viewportWidth, viewportHeight, viewportOriginAxis[3].z,
                            r, g, b,
                            viewportWidth, viewportHeight, viewportOriginAxis[3].z,
                            r, g, b
                    });
                    GL11.glLineWidth(10f);
                    helper.drawLinesRGB_General(new float[]{
                            -viewportWidth, -viewportHeight, viewportOriginAxis[3].z,
                            r, g, b,
                            viewportWidth, -viewportHeight, viewportOriginAxis[3].z,
                            r, g, b
                    });
                }

                if (!c3d.isDoingSelection() && !manipulator.isLocked() && !window.isAddingSomething() && c3d.getDraggedPrimitive() == null) {
                    float r;
                    float g;
                    float b;
                    Vector2f mp = c3d.getMousePosition();
                    r = View.text_Colour_r[0];
                    g = View.text_Colour_g[0];
                    b = View.text_Colour_b[0];
                    if (mp.x > 50f || mp.y > 50f) {
                        if (DatFile.getLastHoveredComposite() == c3d) {
                            hoverSettingsTime = System.currentTimeMillis();
                        }
                    } else if (mp.x > 0f && mp.y > 0f) {
                        r = View.vertex_selected_Colour_r[0];
                        g = View.vertex_selected_Colour_g[0];
                        b = View.vertex_selected_Colour_b[0];
                        if (System.currentTimeMillis() - hoverSettingsTime > 600 && DatFile.getLastHoveredComposite() == c3d) {

                            hoverSettingsTime = System.currentTimeMillis();

                            if (c3d.hasMouse()) {
                                java.awt.Point p = java.awt.MouseInfo.getPointerInfo().getLocation();
                                final int x = (int) p.getX();
                                final int y = (int) p.getY();

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
                    GL33Primitives.GEAR_MENU.draw(stack, shaderProgram2D, gx, gy, viewportOriginAxis[0].z, r, g, b);
                    GL33Primitives.GEAR_MENU_INV.draw(stack, shaderProgram2D, gx, gy, viewportOriginAxis[0].z, r, g, b);

                    // Draw arrows for cursor-on-border-scrolling
                    if (userSettings.isTranslatingViewByCursor() && c3d.hasMouse() && c3d.equals(Project.getFileToEdit().getLastSelectedComposite())) {

                        final float duration = Math.max(10f, Math.min(1000f, System.currentTimeMillis() - start));
                        final float speed = 0.05f / duration / zoom;
                        final int[] indices = new int[] { 0, 1, 2, 0, 2, 1};
                        final float[] vertices = new float[18];

                        // TOP
                        for (int i = 0; i < 18; i += 6) {
                            vertices[i + 3] = View.text_Colour_r[0];
                            vertices[i + 4] = View.text_Colour_g[0];
                            vertices[i + 5] = View.text_Colour_b[0];
                        }
                        if (Math.abs(bounds.width / 2 - mp.x) > 75f || mp.y > 25f) {
                            for (int i = 0; i < 18; i += 6) {
                                vertices[i + 3] = View.text_Colour_r[0];
                                vertices[i + 4] = View.text_Colour_g[0];
                                vertices[i + 5] = View.text_Colour_b[0];
                            }
                        } else if (mp.y > 0f && Math.abs(bounds.width / 2 - mp.x) <= 75f) {
                            for (int i = 0; i < 18; i += 6) {
                                vertices[i + 3] = View.vertex_selected_Colour_r[0];
                                vertices[i + 4] = View.vertex_selected_Colour_g[0];
                                vertices[i + 5] = View.vertex_selected_Colour_b[0];
                            }
                            if (DatFile.getLastHoveredComposite() == c3d) {
                                c3d.getMouse().prepareTranslateViewport();
                                c3d.getMouse().translateViewport(0f, speed, viewportTranslation, viewportRotation, c3d.getPerspectiveCalculator());
                            }
                        }

                        vertices[0] = -0.018f;
                        vertices[1] = -viewportHeight + 0.018f;
                        vertices[2] = viewportOriginAxis[0].z;
                        vertices[6] = 0.018f;
                        vertices[7] = -viewportHeight + 0.018f;
                        vertices[8] = viewportOriginAxis[0].z;
                        vertices[12] = 0;
                        vertices[13] = -viewportHeight + 0.009f;
                        vertices[14] = viewportOriginAxis[0].z;

                        helper.drawTrianglesIndexedRGB_General(vertices, indices);

                        // BOTTOM
                        for (int i = 0; i < 18; i += 6) {
                            vertices[i + 3] = View.text_Colour_r[0];
                            vertices[i + 4] = View.text_Colour_g[0];
                            vertices[i + 5] = View.text_Colour_b[0];
                        }
                        if (Math.abs(bounds.width / 2 - mp.x) > 75f || mp.y <= (bounds.height - 25)) {
                            for (int i = 0; i < 18; i += 6) {
                                vertices[i + 3] = View.text_Colour_r[0];
                                vertices[i + 4] = View.text_Colour_g[0];
                                vertices[i + 5] = View.text_Colour_b[0];
                            }
                        } else if (mp.y > (bounds.height - 25) && Math.abs(bounds.width / 2 - mp.x) <= 75f) {
                            for (int i = 0; i < 18; i += 6) {
                                vertices[i + 3] = View.vertex_selected_Colour_r[0];
                                vertices[i + 4] = View.vertex_selected_Colour_g[0];
                                vertices[i + 5] = View.vertex_selected_Colour_b[0];
                            }
                            c3d.getMouse().prepareTranslateViewport();
                            c3d.getMouse().translateViewport(0f, -speed, viewportTranslation, viewportRotation, c3d.getPerspectiveCalculator());
                        }

                        vertices[0] = -0.018f;
                        vertices[1] = viewportHeight - 0.018f;
                        vertices[2] = viewportOriginAxis[0].z;
                        vertices[6] = 0.018f;
                        vertices[7] = viewportHeight - 0.018f;
                        vertices[8] = viewportOriginAxis[0].z;
                        vertices[12] = 0;
                        vertices[13] = viewportHeight - 0.009f;
                        vertices[14] = viewportOriginAxis[0].z;

                        helper.drawTrianglesIndexedRGB_General(vertices, indices);

                        // LEFT
                        for (int i = 0; i < 18; i += 6) {
                            vertices[i + 3] = View.text_Colour_r[0];
                            vertices[i + 4] = View.text_Colour_g[0];
                            vertices[i + 5] = View.text_Colour_b[0];
                        }
                        if (Math.abs(bounds.height / 2 - mp.y) > 75f || mp.x >= 25) {
                            for (int i = 0; i < 18; i += 6) {
                                vertices[i + 3] = View.text_Colour_r[0];
                                vertices[i + 4] = View.text_Colour_g[0];
                                vertices[i + 5] = View.text_Colour_b[0];
                            }
                        } else if (mp.x < 25 && Math.abs(bounds.height / 2 - mp.y) <= 75f) {
                            for (int i = 0; i < 18; i += 6) {
                                vertices[i + 3] = View.vertex_selected_Colour_r[0];
                                vertices[i + 4] = View.vertex_selected_Colour_g[0];
                                vertices[i + 5] = View.vertex_selected_Colour_b[0];
                            }
                            c3d.getMouse().prepareTranslateViewport();
                            c3d.getMouse().translateViewport(-speed, 0f, viewportTranslation, viewportRotation, c3d.getPerspectiveCalculator());
                        }

                        vertices[0] = viewportWidth - 0.018f;
                        vertices[1] = -0.018f;
                        vertices[2] = viewportOriginAxis[0].z;
                        vertices[6] = viewportWidth - 0.018f;
                        vertices[7] = 0.018f;
                        vertices[8] = viewportOriginAxis[0].z;
                        vertices[12] = viewportWidth - 0.009f;
                        vertices[13] = 0;
                        vertices[14] = viewportOriginAxis[0].z;

                        helper.drawTrianglesIndexedRGB_General(vertices, indices);

                        // RIGHT
                        for (int i = 0; i < 18; i += 6) {
                            vertices[i + 3] = View.text_Colour_r[0];
                            vertices[i + 4] = View.text_Colour_g[0];
                            vertices[i + 5] = View.text_Colour_b[0];
                        }
                        if (Math.abs(bounds.height / 2 - mp.y) > 75f || mp.x <= (bounds.width - 25)) {
                            for (int i = 0; i < 18; i += 6) {
                                vertices[i + 3] = View.text_Colour_r[0];
                                vertices[i + 4] = View.text_Colour_g[0];
                                vertices[i + 5] = View.text_Colour_b[0];
                            }
                        } else if (mp.x > (bounds.width - 25) && Math.abs(bounds.height / 2 - mp.y) <= 75f) {
                            for (int i = 0; i < 18; i += 6) {
                                vertices[i + 3] = View.vertex_selected_Colour_r[0];
                                vertices[i + 4] = View.vertex_selected_Colour_g[0];
                                vertices[i + 5] = View.vertex_selected_Colour_b[0];
                            }
                            c3d.getMouse().prepareTranslateViewport();
                            c3d.getMouse().translateViewport(speed, 0f, viewportTranslation, viewportRotation, c3d.getPerspectiveCalculator());
                        }

                        vertices[0] = -viewportWidth + 0.018f;
                        vertices[1] = -0.018f;
                        vertices[2] = viewportOriginAxis[0].z;
                        vertices[6] = -viewportWidth + 0.018f;
                        vertices[7] = 0.018f;
                        vertices[8] = viewportOriginAxis[0].z;
                        vertices[12] = -viewportWidth + 0.009f;
                        vertices[13] = 0;
                        vertices[14] = viewportOriginAxis[0].z;

                        helper.drawTrianglesIndexedRGB_General(vertices, indices);
                    }
                }

                GL11.glEnable(GL11.GL_DEPTH_TEST);
                break;
            }
        }

        helper.destroyVBO();

        canvas.swapBuffers();

        // NLogger.debug(getClass(), "Frametime: " + (System.currentTimeMillis() - start)); //$NON-NLS-1$
    }

    @Override
    public void dispose() {
        // Properly de-allocate all resources once they've outlived their purpose
        modelRenderer.dispose();
        modelRendererLDrawStandard.dispose();
        shaderProgram.dispose();
        shaderProgram2.dispose();
        shaderProgram2D.dispose();
        shaderProgramCondline.dispose();
        // Dispose all textures
        for (Iterator<GTexture> it = textureSet.iterator() ; it.hasNext();) {
            GTexture tex = it.next();
            NLogger.debug(getClass(), "Dispose texture: {0}", tex); //$NON-NLS-1$
            tex.dispose(this);
            it.remove();
        }
    }

    public Matrix4f getRotationInverse() {
        return rotation_inv4f;
    }
}
