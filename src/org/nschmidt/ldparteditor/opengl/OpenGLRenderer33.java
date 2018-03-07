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

        // final long start = System.currentTimeMillis();

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
            GL11.glViewport(0, 0, bounds.width, bounds.height);

            shaderProgram.use();
            stack.setShader(shaderProgram);
            stack.glLoadIdentity();

            float viewport_width = bounds.width / View.PIXEL_PER_LDU / 2.0f;
            float viewport_height = bounds.height / View.PIXEL_PER_LDU / 2.0f;
            {
                final FloatBuffer projection_buf = BufferUtils.createFloatBuffer(16);
                GLMatrixStack.glOrtho(viewport_width, -viewport_width, viewport_height, -viewport_height, -c3d.getzNear() * c3d.getZoom(), c3d.getzFar() * c3d.getZoom()).store(projection_buf);
                projection_buf.position(0);
                shaderProgram2D.use();
                int projection = shaderProgram2D.getUniformLocation("projection" ); //$NON-NLS-1$
                GL20.glUniformMatrix4fv(projection, false, projection_buf);
                shaderProgram2.use();
                projection = shaderProgram2.getUniformLocation("projection" ); //$NON-NLS-1$
                GL20.glUniformMatrix4fv(projection, false, projection_buf);
                shaderProgramCondline.use();
                projection = shaderProgramCondline.getUniformLocation("projection" ); //$NON-NLS-1$
                GL20.glUniformMatrix4fv(projection, false, projection_buf);
                shaderProgram.use();
                projection = shaderProgram.getUniformLocation("projection" ); //$NON-NLS-1$
                GL20.glUniformMatrix4fv(projection, false, projection_buf);
            }

            Matrix4f viewport_transform = new Matrix4f();
            Matrix4f.setIdentity(viewport_transform);

            final float zoom = c3d.getZoom();
            Matrix4f.scale(new Vector3f(zoom, zoom, zoom), viewport_transform, viewport_transform);
            Matrix4f viewport_rotation = c3d.getRotation();
            viewport_rotation.store(rot_buf);
            rot_buf.flip();
            Matrix4f.load(viewport_rotation, rotation_inv4f);
            rotation_inv4f.invert();
            Matrix4f.mul(viewport_rotation, viewport_transform, viewport_transform);
            Matrix4f viewport_translation = c3d.getTranslation();
            Matrix4f.mul(viewport_transform, viewport_translation, viewport_transform);
            viewport_transform.store(view_buf);
            view_buf.flip();
            c3d.setViewport(viewport_transform);

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

                Matrix4f viewport_rotation2 = new Matrix4f();

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
                viewport_transform2.store(view_buf);
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
                modelRenderer.draw(stack, shaderProgram, shaderProgramCondline, shaderProgram2D, true, c3d.getLockableDatFileReference());
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
                modelRenderer.draw(stack, shaderProgram, shaderProgramCondline, shaderProgram2D, false, c3d.getLockableDatFileReference());
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
                float[] mSize = WorkbenchManager.getUserSettingState().getManipulatorSize();
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

                helper.drawLinesRGB_General(new float[]{

                        selectionStart_MODELVIEW.x, selectionStart_MODELVIEW.y, selectionStart_MODELVIEW.z,
                        View.rubberBand_Colour_r[0], View.rubberBand_Colour_g[0], View.rubberBand_Colour_b[0],
                        selectionCorner1.x, selectionCorner1.y, selectionCorner1.z,
                        View.rubberBand_Colour_r[0], View.rubberBand_Colour_g[0], View.rubberBand_Colour_b[0],
                        selectionStart_MODELVIEW.x, selectionStart_MODELVIEW.y, selectionStart_MODELVIEW.z,
                        View.rubberBand_Colour_r[0], View.rubberBand_Colour_g[0], View.rubberBand_Colour_b[0],
                        selectionCorner2.x, selectionCorner2.y, selectionCorner2.z,
                        View.rubberBand_Colour_r[0], View.rubberBand_Colour_g[0], View.rubberBand_Colour_b[0],

                        selectionEnd_MODELVIEW.x, selectionEnd_MODELVIEW.y, selectionEnd_MODELVIEW.z,
                        View.rubberBand_Colour_r[0], View.rubberBand_Colour_g[0], View.rubberBand_Colour_b[0],
                        selectionCorner1.x, selectionCorner1.y, selectionCorner1.z,
                        View.rubberBand_Colour_r[0], View.rubberBand_Colour_g[0], View.rubberBand_Colour_b[0],

                        selectionEnd_MODELVIEW.x, selectionEnd_MODELVIEW.y, selectionEnd_MODELVIEW.z,
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
            Vector3f[] viewport_origin_axis = c3d.getViewportOriginAxis();
            float z_offset = 0;
            if (c3d.isGridShown()) {
                // Grid-1 and 10
                float g_r;
                float g_g;
                float g_b;
                for (int r = 0; r < 5; r += 4) {
                    if (r == 4) {
                        g_r = View.grid10_Colour_r[0];
                        g_g = View.grid10_Colour_g[0];
                        g_b = View.grid10_Colour_b[0];
                        z_offset = 1f;
                        GL11.glLineWidth(2f);
                    } else {
                        g_r = View.grid_Colour_r[0];
                        g_g = View.grid_Colour_g[0];
                        g_b = View.grid_Colour_b[0];
                        z_offset = 0;
                        GL11.glLineWidth(1f);
                    }
                    Vector4f grid_center1 = new Vector4f();
                    Vector4f grid_center2 = new Vector4f();
                    grid_center1.set(c3d.getGrid()[r]);
                    grid_center2.set(grid_center1);

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
                        Vector4f.sub(grid_center2, c3d.getGrid()[2 + r], grid_center2);
                        vertices[j] = viewport_origin_axis[0].x; j++;
                        vertices[j] = grid_center1.y; j++;
                        vertices[j] = viewport_origin_axis[0].z + z_offset; j++;
                        vertices[j] = g_r; j++;
                        vertices[j] = g_g; j++;
                        vertices[j] = g_b; j++;
                        vertices[j] = viewport_origin_axis[1].x; j++;
                        vertices[j] = grid_center1.y; j++;
                        vertices[j] = viewport_origin_axis[1].z + z_offset; j++;
                        vertices[j] = g_r; j++;
                        vertices[j] = g_g; j++;
                        vertices[j] = g_b; j++;
                        vertices[j] = viewport_origin_axis[0].x; j++;
                        vertices[j] = grid_center2.y; j++;
                        vertices[j] = viewport_origin_axis[0].z + z_offset; j++;
                        vertices[j] = g_r; j++;
                        vertices[j] = g_g; j++;
                        vertices[j] = g_b; j++;
                        vertices[j] = viewport_origin_axis[1].x; j++;
                        vertices[j] = grid_center2.y; j++;
                        vertices[j] = viewport_origin_axis[1].z + z_offset; j++;
                        vertices[j] = g_r; j++;
                        vertices[j] = g_g; j++;
                        vertices[j] = g_b; j++;
                        Vector4f.add(grid_center1, c3d.getGrid()[2 + r], grid_center1);
                    }
                    grid_center1.set(c3d.getGrid()[r]);
                    grid_center2.set(grid_center1);
                    limit = c3d.getGrid()[3 + r].x;
                    for (float i = 0f; i < limit; i += 1f) {
                        Vector4f.sub(grid_center2, c3d.getGrid()[1 + r], grid_center2);
                        vertices[j] = grid_center2.x; j++;
                        vertices[j] = viewport_origin_axis[2].y; j++;
                        vertices[j] = viewport_origin_axis[2].z + z_offset; j++;
                        vertices[j] = g_r; j++;
                        vertices[j] = g_g; j++;
                        vertices[j] = g_b; j++;
                        vertices[j] = grid_center2.x; j++;
                        vertices[j] = viewport_origin_axis[3].y; j++;
                        vertices[j] = viewport_origin_axis[3].z + z_offset; j++;
                        vertices[j] = g_r; j++;
                        vertices[j] = g_g; j++;
                        vertices[j] = g_b; j++;
                        vertices[j] = grid_center1.x; j++;
                        vertices[j] = viewport_origin_axis[2].y; j++;
                        vertices[j] = viewport_origin_axis[2].z + z_offset; j++;
                        vertices[j] = g_r; j++;
                        vertices[j] = g_g; j++;
                        vertices[j] = g_b; j++;
                        vertices[j] = grid_center1.x; j++;
                        vertices[j] = viewport_origin_axis[3].y; j++;
                        vertices[j] = viewport_origin_axis[3].z + z_offset; j++;
                        vertices[j] = g_r; j++;
                        vertices[j] = g_g; j++;
                        vertices[j] = g_b; j++;
                        Vector4f.add(grid_center1, c3d.getGrid()[1 + r], grid_center1);
                    }
                    helper.drawLinesRGB_General(vertices);
                }
                z_offset = 2f; // z_offset + 5 + 1f * c3d.getZoom();
            }

            if (c3d.isOriginShown()) {
                // Origin
                GL11.glLineWidth(2f);
                helper.drawLinesRGB_General(new float[]{
                        viewport_origin_axis[0].x, viewport_origin_axis[0].y, viewport_origin_axis[0].z + z_offset,
                        View.origin_Colour_r[0], View.origin_Colour_g[0], View.origin_Colour_b[0],
                        viewport_origin_axis[1].x, viewport_origin_axis[1].y, viewport_origin_axis[1].z + z_offset,
                        View.origin_Colour_r[0], View.origin_Colour_g[0], View.origin_Colour_b[0],
                        viewport_origin_axis[2].x, viewport_origin_axis[2].y, viewport_origin_axis[2].z + z_offset,
                        View.origin_Colour_r[0], View.origin_Colour_g[0], View.origin_Colour_b[0],
                        viewport_origin_axis[3].x, viewport_origin_axis[3].y, viewport_origin_axis[3].z + z_offset,
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
                    stack.glTranslatef(ox - viewport_width, viewport_height - oy, 0f);
                    stack.glMultMatrixf(viewport_rotation);
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
                            tri.drawTextGL33(viewport_width, viewport_height, viewport_origin_axis[0].z);
                        }
                        break;
                    case BACK:
                        for (PGData3 tri : View.BACK) {
                            tri.drawTextGL33(viewport_width, viewport_height, viewport_origin_axis[0].z);
                        }
                        break;
                    case TOP:
                        for (PGData3 tri : View.TOP) {
                            tri.drawTextGL33(viewport_width, viewport_height, viewport_origin_axis[0].z);
                        }
                        break;
                    case BOTTOM:
                        for (PGData3 tri : View.BOTTOM) {
                            tri.drawTextGL33(viewport_width, viewport_height, viewport_origin_axis[0].z);
                        }
                        break;
                    case LEFT:
                        for (PGData3 tri : View.LEFT) {
                            tri.drawTextGL33(viewport_width, viewport_height, viewport_origin_axis[0].z);
                        }
                        break;
                    case RIGHT:
                        for (PGData3 tri : View.RIGHT) {
                            tri.drawTextGL33(viewport_width, viewport_height, viewport_origin_axis[0].z);
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
                            viewport_width, viewport_height, viewport_origin_axis[3].z,
                            r, g, b,
                            viewport_width, -viewport_height, viewport_origin_axis[3].z,
                            r, g, b
                    });
                    GL11.glLineWidth(10f);
                    helper.drawLinesRGB_General(new float[]{
                            -viewport_width, -viewport_height, viewport_origin_axis[3].z,
                            r, g, b,
                            -viewport_width, viewport_height, viewport_origin_axis[3].z,
                            r, g, b,
                    });
                    GL11.glLineWidth(5f);
                    helper.drawLinesRGB_General(new float[]{
                            -viewport_width, viewport_height, viewport_origin_axis[3].z,
                            r, g, b,
                            viewport_width, viewport_height, viewport_origin_axis[3].z,
                            r, g, b
                    });
                    GL11.glLineWidth(10f);
                    helper.drawLinesRGB_General(new float[]{
                            -viewport_width, -viewport_height, viewport_origin_axis[3].z,
                            r, g, b,
                            viewport_width, -viewport_height, viewport_origin_axis[3].z,
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

                    final float gx = viewport_width - 0.018f;
                    final float gy = -viewport_height + 0.018f;
                    GL33Primitives.GEAR_MENU.draw(stack, shaderProgram2D, gx, gy, viewport_origin_axis[0].z, r, g, b);
                    GL33Primitives.GEAR_MENU_INV.draw(stack, shaderProgram2D, gx, gy, viewport_origin_axis[0].z, r, g, b);
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
