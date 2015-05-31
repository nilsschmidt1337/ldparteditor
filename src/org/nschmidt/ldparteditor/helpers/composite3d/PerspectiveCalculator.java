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
package org.nschmidt.ldparteditor.helpers.composite3d;

import java.math.BigDecimal;
import java.nio.FloatBuffer;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.composites.ScalableComposite;
import org.nschmidt.ldparteditor.data.Matrix;
import org.nschmidt.ldparteditor.enums.Perspective;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.math.Rational;
import org.nschmidt.ldparteditor.helpers.math.RationalMatrix;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.helpers.math.Vector3r;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;

/**
 * Provides functions to set the perspective of the {@linkplain Composite3D}
 *
 * @author nils
 *
 */
public class PerspectiveCalculator {

    // Non-public properties
    /** The 3D Composite [NOT PUBLIC YET] */
    private final Composite3D c3d;
    /** The view zoom exponent [NOT PUBLIC YET] */
    private float zoom_exponent;
    /** The view offset [NOT PUBLIC YET] */
    Vector4f offset = new Vector4f(0, 0, 0, 1f);
    /** The effective clipping value [NOT PUBLIC YET] */
    private float z_eff;
    /** The left half width in the viewport [NOT PUBLIC YET] */
    private float width;


    /** Magic number for numerical stability [NOT PUBLIC YET] */
    private final Rational TX = new Rational(new BigDecimal("0.146432")); //$NON-NLS-1$
    /** Magic number for numerical stability [NOT PUBLIC YET] */
    private final Rational TY = new Rational(new BigDecimal("0.392734")); //$NON-NLS-1$

    public PerspectiveCalculator(Composite3D c3d) {
        this.c3d = c3d;
        this.zoom_exponent = -20; // Start with 1.0% zoom
    }

    /**
     * Sets the specific perspective
     *
     * @param perspective
     *            the perspective value from {@linkplain Perspective}.
     */
    public void setPerspective(Perspective perspective) {
        Matrix4f id = new Matrix4f();
        Matrix4f.setIdentity(id);
        c3d.getTranslation().load(id);
        c3d.setClassicPerspective(true);
        c3d.setPerspectiveIndex(perspective);
        switch (perspective) {
        case FRONT: {
            NLogger.debug(PerspectiveCalculator.class, "[Front view]"); //$NON-NLS-1$
            float[] rpf = new float[] { -1, 0, 0, 0, 0, 1, 0, 0, 0, 0, -1, 0, 0, 0, 0, 1 };
            FloatBuffer fb = BufferUtils.createFloatBuffer(16);
            fb.put(rpf);
            fb.flip();
            c3d.getRotation().load(fb);
        }
        break;
        case BACK:
            NLogger.debug(PerspectiveCalculator.class, "[Back view]"); //$NON-NLS-1$
            c3d.getRotation().load(id);
            break;
        case LEFT: {
            NLogger.debug(PerspectiveCalculator.class, "[Left view]"); //$NON-NLS-1$
            float[] rpf = new float[] { 0, 0, -1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1 };
            FloatBuffer fb = BufferUtils.createFloatBuffer(16);
            fb.put(rpf);
            fb.flip();
            c3d.getRotation().load(fb);
        }
        break;
        case RIGHT: {
            NLogger.debug(PerspectiveCalculator.class, "[Right view]"); //$NON-NLS-1$
            float[] rpf = new float[] { 0, 0, 1, 0, 0, 1, 0, 0, -1, 0, 0, 0, 0, 0, 0, 1 };
            FloatBuffer fb = BufferUtils.createFloatBuffer(16);
            fb.put(rpf);
            fb.flip();
            c3d.getRotation().load(fb);
        }
        break;
        case TOP: {
            NLogger.debug(PerspectiveCalculator.class, "[Top view]"); //$NON-NLS-1$
            float[] rpf = new float[] { -1, 0, 0, 0, 0, 0, -1, 0, 0, -1, 0, 0, 0, 0, 0, 1 };
            FloatBuffer fb = BufferUtils.createFloatBuffer(16);
            fb.put(rpf);
            fb.flip();
            c3d.getRotation().load(fb);
        }
        break;
        case BOTTOM: {
            NLogger.debug(PerspectiveCalculator.class, "[Bottom view]"); //$NON-NLS-1$
            float[] rpf = new float[] { -1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1 };
            FloatBuffer fb = BufferUtils.createFloatBuffer(16);
            fb.put(rpf);
            fb.flip();
            c3d.getRotation().load(fb);
        }
        break;
        case TWO_THIRDS: {
            NLogger.debug(PerspectiveCalculator.class, "[Two thirds view]"); //$NON-NLS-1$
            float[] rpf = new float[] {
                    -0.7071f, -0.5f, -0.5f, 0,
                    0, 0.7071f, -0.7071f, 0,
                    0.7071f, -0.5f, -0.5f, 0,
                    0, 0, 0, 1 };
            FloatBuffer fb = BufferUtils.createFloatBuffer(16);
            fb.put(rpf);
            fb.flip();
            c3d.getRotation().load(fb);
            c3d.setClassicPerspective(false); // Two-thirds has no axes!
        }
        break;
        }
        calculateOriginData();
    }

    /**
     * Calculates the view generator from the actual rotation matrix
     */
    private void calculateViewGenerator() {
        Vector4f xAxis4f_viewport_generator = new Vector4f(1.0f, 0, 0, 1.0f);
        Vector4f yAxis4f_viewport_generator = new Vector4f(0, 1.0f, 0, 1.0f);
        Vector4f zAxis4f_viewport_generator = new Vector4f(0, 0, 1.0f, 1.0f);
        Matrix4f viewport_rotation = c3d.getRotation();
        Matrix4f vr_inverse = Matrix4f.invert(viewport_rotation, null);
        Matrix4f.transform(vr_inverse, xAxis4f_viewport_generator, xAxis4f_viewport_generator);
        Matrix4f.transform(vr_inverse, yAxis4f_viewport_generator, yAxis4f_viewport_generator);
        Matrix4f.transform(vr_inverse, zAxis4f_viewport_generator, zAxis4f_viewport_generator);
        if (c3d.hasNegDeterminant()) {
            zAxis4f_viewport_generator = new Vector4f(-zAxis4f_viewport_generator.x, -zAxis4f_viewport_generator.y, -zAxis4f_viewport_generator.z, 1f);
        }
        Vector4f[] viewport_generator = c3d.getGenerator();
        viewport_generator[0] = xAxis4f_viewport_generator;
        viewport_generator[1] = yAxis4f_viewport_generator;
        viewport_generator[2] = zAxis4f_viewport_generator;
    }

    /**
     * Calculates the origin and grid for the actual viewport perspective
     */
    public void calculateOriginData() {
        Matrix4f realViewport = getRealViewport();
        calculateOrigin(realViewport);
        calculateGrid(realViewport);
        c3d.setNegDeterminant(realViewport.determinant() < 0f);
        calculateViewGenerator();
    }

    /**
     * Calculates the origin axis coordinates for the actual viewport
     * perspective
     */
    private void calculateOrigin(Matrix4f realViewport) {
        Rectangle bounds = c3d.getBounds();
        z_eff = (float) ((c3d.getzFar() + c3d.getzNear()) / -2.0 * c3d.getZoom());
        width = (float) bounds.width / (float) bounds.height * .5f;
        Vector3f[] axes = c3d.getViewportOriginAxis();
        offset.set(0, 0, 0, 1f);
        Matrix4f.transform(realViewport, offset, offset);
        axes[0].set(-width, offset.y, z_eff);
        axes[1].set(width, offset.y, z_eff);
        axes[2].set(offset.x, -.5f, z_eff);
        axes[3].set(offset.x, .5f, z_eff);
    }

    /**
     * Calculates the grid data for the actual viewport perspective
     */
    private void calculateGrid(Matrix4f realViewport) {

        float grid_size = c3d.getZoom() * View.PIXEL_PER_LDU;
        while (grid_size > 10f) {
            grid_size = grid_size / 10f;
        }
        while (grid_size < 10f) {
            grid_size = grid_size * 10f;
        }
        if (grid_size > 10f) {
            grid_size = grid_size / 2f;
        }
        grid_size = grid_size * 10f * c3d.getGrid_scale();
        int mx = (int) (c3d.getBounds().width / grid_size + 4) / 2;
        int my = (int) (c3d.getBounds().height / grid_size + 4) / 2;
        grid_size = grid_size / 1000f;

        float rest_x = offset.x % grid_size;
        float rest_y = offset.y % grid_size;
        Vector4f[] grid = c3d.getGrid();
        grid[0].set(rest_x, rest_y, z_eff + 0.1f);
        grid[1].set(-grid_size, 0f);
        grid[2].set(0f, -grid_size);
        // Multiplicants
        grid[3].set(mx, my);
        grid_size = grid_size * 10f;
        int mx10 = (int) (c3d.getBounds().width / grid_size + 4) / 2;
        int my10 = (int) (c3d.getBounds().height / grid_size + 4) / 2;
        float rest_x10 = offset.x % grid_size;
        float rest_y10 = offset.y % grid_size;
        grid[4].set(rest_x10, rest_y10, z_eff);
        grid[5].set(-grid_size, 0f);
        grid[6].set(0f, -grid_size);
        // Multiplicants
        grid[7].set(mx10, my10);
    }

    /**
     * @return The real transformation matrix of the viewport
     */
    public Matrix4f getRealViewport() {
        Matrix4f viewport_transform = new Matrix4f();
        Matrix4f.setIdentity(viewport_transform);
        float zoom = c3d.getZoom();
        Matrix4f.scale(new Vector3f(zoom, zoom, zoom), viewport_transform, viewport_transform);
        Matrix4f viewport_rotation = c3d.getRotation();
        Matrix4f.mul(viewport_rotation, viewport_transform, viewport_transform);
        Matrix4f viewport_translation = c3d.getTranslation();
        Matrix4f.mul(viewport_transform, viewport_translation, viewport_transform);
        return viewport_transform;
    }

    /**
     * Transforms screen coordinates to 3D space coordinates
     *
     * @param x
     *            x-screen coordinate
     * @param y
     *            y-screen coordinate
     * @return vector position in 3D space
     */
    public Vector4f get3DCoordinatesFromScreen(int x, int y) {
        Point cSize = c3d.getSize();
        Vector4f relPos = new Vector4f();
        relPos.x = (0.5f * cSize.x - x) / View.PIXEL_PER_LDU;
        relPos.y = (y - 0.5f * cSize.y) / View.PIXEL_PER_LDU;
        relPos.w = 1.0f;
        Matrix4f v_inverse = c3d.getViewport_Inverse();
        Matrix4f.transform(v_inverse, relPos, relPos);
        return relPos;
    }

    /**
     * Transforms 3D space coordinates to screen coordinates
     *
     * @param x
     *            x-screen coordinate
     * @param y
     *            y-screen coordinate
     * @param z
     *            z-screen coordinate
     * @return vector position on screen
     */
    public Vector4f getScreenCoordinatesFrom3D(float x, float y, float z) {
        Point cSize = c3d.getSize();
        Vector4f relPos = new Vector4f(x, y, z, 1f);
        Matrix4f.transform(c3d.getViewport(), relPos, relPos);
        float cursor_x = 0.5f * cSize.x - relPos.x * View.PIXEL_PER_LDU;
        float cursor_y = 0.5f * cSize.y + relPos.y * View.PIXEL_PER_LDU;
        relPos.set(cursor_x, cursor_y, 0f, 1f);
        return relPos;
    }

    public Vector3d getScreenCoordinatesFrom3D(BigDecimal X, BigDecimal Y, BigDecimal Z) {
        BigDecimal THOUSAND = new BigDecimal(1000);
        BigDecimal ZERO = new BigDecimal(0);
        Matrix vm = new Matrix(c3d.getViewport());
        Matrix vm2 = new Matrix(THOUSAND, ZERO, ZERO, ZERO, ZERO, THOUSAND, ZERO, ZERO, ZERO, ZERO, THOUSAND, ZERO, ZERO, ZERO, ZERO, THOUSAND);
        vm = Matrix.mul(vm, vm2);
        Vector3d result = new Vector3d(X, Y, Z);
        result = vm.transform(result);
        return result;
    }


    /**
     * Transforms screen coordinates to 3D space coordinates
     * (needs viewport inverse)
     * @param x
     *            x-screen coordinate
     * @param y
     *            y-screen coordinate
     * @return vector position in 3D space
     */
    public Vector3r get3DCoordinatesFromScreen(Vector3r vector3r, RationalMatrix m) {
        Vector3r relPos = new Vector3r(vector3r.X.subtract(TX), vector3r.Y.subtract(TY), vector3r.Z);
        relPos = m.transform(relPos);
        return relPos;
    }

    /**
     * Transforms 3D space coordinates to screen coordinates
     *
     *
     *
     * (needs viewport matrix)
     * @param x
     *            x-screen coordinate
     * @param y
     *            y-screen coordinate
     * @param z
     *            z-screen coordinate
     * @return vector position on screen
     */
    public Vector3r getScreenCoordinatesFrom3D(Vector3r vector3r, RationalMatrix m) {
        Vector3r relPos = m.transform(vector3r);
        relPos = new Vector3r(relPos.X.add(TX), relPos.Y.add(TY), relPos.Z);
        return relPos;
    }

    /**
     * Transforms 3D space coordinates to screen coordinates with Z axis
     *
     * @param x
     *            x-screen coordinate
     * @param y
     *            y-screen coordinate
     * @param z
     *            z-screen coordinate
     * @return vector position on screen
     */
    public Vector4f getScreenCoordinatesFrom3DonlyZ(float x, float y, float z) {
        Vector4f relPos = new Vector4f(x, y, z, 1f);
        Matrix4f.transform(c3d.getViewport(), relPos, relPos);
        return relPos;
    }

    /**
     * Transforms screen coordinates to 3D space coordinates
     *
     * @param x
     *            x-screen coordinate
     * @param y
     *            y-screen coordinate
     * @param z
     *            z-screen coordinate
     * @return vector position in 3D space
     */
    public Vector4f get3DCoordinatesFromScreen(int x, int y, int z) {
        Point cSize = c3d.getSize();
        Vector4f relPos = new Vector4f();
        relPos.x = (0.5f * cSize.x - x) / View.PIXEL_PER_LDU;
        relPos.y = (y - 0.5f * cSize.y) / View.PIXEL_PER_LDU;
        relPos.z = z;
        relPos.w = 1.0f;
        Matrix4f v_inverse = c3d.getViewport_Inverse();
        Matrix4f.transform(v_inverse, relPos, relPos);
        return relPos;
    }

    /**
     * Initializes the viewport and perspective
     */
    public void initializeViewportPerspective() {
        Rectangle bounds = c3d.getBounds();
        // MARK OpenGL Viewport and Perspective
        GL11.glViewport(0, 0, bounds.width, bounds.height);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        float viewport_width = bounds.width / View.PIXEL_PER_LDU / 2.0f;
        float viewport_height = bounds.height / View.PIXEL_PER_LDU / 2.0f;
        GL11.glOrtho(-viewport_width, viewport_width, -viewport_height, viewport_height, c3d.getzNear() * c3d.getZoom(), c3d.getzFar() * c3d.getZoom());
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        calculateOriginData();
    }

    /**
     * Zooming in
     */
    public void zoomIn() {
        if (c3d.isDoingSelection())
            return;
        zoom_exponent++;
        if (zoom_exponent > 20) {
            zoom_exponent = 20;
        }
        c3d.setZoom((float) Math.pow(10.0d, zoom_exponent / 10 - 3));
        c3d.setViewportPixelPerLDU(c3d.getZoom() * View.PIXEL_PER_LDU);
        GuiManager.updateStatus(c3d);
        ((ScalableComposite) c3d.getParent()).redrawScales();
        initializeViewportPerspective();
    }

    /**
     * Zooming out
     */
    public void zoomOut() {
        if (c3d.isDoingSelection())
            return;
        zoom_exponent--;
        if (zoom_exponent < -40) {
            zoom_exponent = -40;
        }
        c3d.setZoom((float) Math.pow(10.0d, zoom_exponent / 10 - 3));
        c3d.setViewportPixelPerLDU(c3d.getZoom() * View.PIXEL_PER_LDU);
        GuiManager.updateStatus(c3d);
        ((ScalableComposite) c3d.getParent()).redrawScales();
        initializeViewportPerspective();
    }

    public void zoomReset() {
        if (c3d.isDoingSelection())
            return;
        zoom_exponent = -20;
        c3d.setZoom((float) Math.pow(10.0d, zoom_exponent / 10 - 3));
        c3d.setViewportPixelPerLDU(c3d.getZoom() * View.PIXEL_PER_LDU);
        c3d.getTranslation().setIdentity();
        GuiManager.updateStatus(c3d);
        ((ScalableComposite) c3d.getParent()).redrawScales();
        initializeViewportPerspective();
    }

    /**
     * Gets the translated String for the perspective value
     *
     * @param perspective
     *            the perspective value from {@linkplain Perspective}.
     * @return (e.g. Perspective.FRONT returns Front)
     */
    public String getPerspectiveString(Perspective perspective) {
        switch (perspective) {
        case FRONT:
            return I18n.PERSPECTIVE_FRONT;
        case BACK:
            return I18n.PERSPECTIVE_BACK;
        case LEFT:
            return I18n.PERSPECTIVE_LEFT;
        case RIGHT:
            return I18n.PERSPECTIVE_RIGHT;
        case TOP:
            return I18n.PERSPECTIVE_TOP;
        case BOTTOM:
            return I18n.PERSPECTIVE_BOTTOM;
        default:
            return ""; //$NON-NLS-1$
        }
    }

    public float getZoom_exponent() {
        return zoom_exponent;
    }

    public void setZoom_exponent(float zoom_exponent) {
        this.zoom_exponent = zoom_exponent;
    }

}
