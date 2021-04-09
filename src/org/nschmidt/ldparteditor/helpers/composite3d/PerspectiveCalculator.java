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
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer20;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;

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
    private float zoomExponent;
    /** The view offset [NOT PUBLIC YET] */
    private Vector4f offset = new Vector4f(0, 0, 0, 1f);
    /** The effective clipping value [NOT PUBLIC YET] */
    private float zEff;

    /** Magic number for numerical stability [NOT PUBLIC YET] */
    private final Rational tx = new Rational(new BigDecimal("0.146432")); //$NON-NLS-1$
    /** Magic number for numerical stability [NOT PUBLIC YET] */
    private final Rational ty = new Rational(new BigDecimal("0.392734")); //$NON-NLS-1$

    public PerspectiveCalculator(Composite3D c3d) {
        this.c3d = c3d;
        this.zoomExponent = -20; // Start with 1.0% zoom
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
                    -0.7071f, 0.5f, 0.5f, 0,
                    0, 0.7071f, -0.7071f, 0,
                    -0.7071f, -0.5f, -0.5f, 0,
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
        Vector4f xAxis4fViewportGenerator = new Vector4f(1.0f, 0, 0, 1.0f);
        Vector4f yAxis4fViewportGenerator = new Vector4f(0, 1.0f, 0, 1.0f);
        Vector4f zAxis4fViewportGenerator = new Vector4f(0, 0, 1.0f, 1.0f);
        Matrix4f viewportRotation = c3d.getRotation();
        Matrix4f vrInverse = Matrix4f.invert(viewportRotation, null);
        Matrix4f.transform(vrInverse, xAxis4fViewportGenerator, xAxis4fViewportGenerator);
        Matrix4f.transform(vrInverse, yAxis4fViewportGenerator, yAxis4fViewportGenerator);
        Matrix4f.transform(vrInverse, zAxis4fViewportGenerator, zAxis4fViewportGenerator);
        if (c3d.hasNegDeterminant()) {
            zAxis4fViewportGenerator = new Vector4f(-zAxis4fViewportGenerator.x, -zAxis4fViewportGenerator.y, -zAxis4fViewportGenerator.z, 1f);
        }
        Vector4f[] viewportGenerator = c3d.getGenerator();
        viewportGenerator[0] = xAxis4fViewportGenerator;
        viewportGenerator[1] = yAxis4fViewportGenerator;
        viewportGenerator[2] = zAxis4fViewportGenerator;
    }

    /**
     * Calculates the origin and grid for the actual viewport perspective
     */
    public void calculateOriginData() {
        Matrix4f realViewport = getRealViewport();
        calculateOrigin(realViewport);
        calculateGrid();
        c3d.setNegDeterminant(realViewport.determinant() < 0f);
        calculateViewGenerator();
    }

    /**
     * Calculates the origin axis coordinates for the actual viewport
     * perspective
     */
    private void calculateOrigin(Matrix4f realViewport) {
        Rectangle bounds = c3d.getBounds();
        final float width = (float) bounds.width / (float) bounds.height;
        zEff = (float) ((c3d.getzFar() + c3d.getzNear()) / -2.0 * c3d.getZoom());
        Vector3f[] axes = c3d.getViewportOriginAxis();
        offset.set(0, 0, 0, 1f);
        Matrix4f.transform(realViewport, offset, offset);
        axes[0].set(-width, offset.y, zEff);
        axes[1].set(width, offset.y, zEff);
        axes[2].set(offset.x, -1f, zEff);
        axes[3].set(offset.x, 1f, zEff);
    }

    /**
     * Calculates the grid data for the actual viewport perspective
     */
    private void calculateGrid() {

        float gridSize = c3d.getZoom() * View.PIXEL_PER_LDU;
        while (gridSize > 10f) {
            gridSize = gridSize / 10f;
        }
        while (gridSize < 10f) {
            gridSize = gridSize * 10f;
        }
        if (gridSize > 10f) {
            gridSize = gridSize / 2f;
        }
        gridSize = gridSize * 10f * c3d.getGridScale();
        int mx = (int) (c3d.getBounds().width / gridSize + 4) / 2;
        int my = (int) (c3d.getBounds().height / gridSize + 4) / 2;
        gridSize = gridSize / 1000f;

        float restX = offset.x % gridSize;
        float restY = offset.y % gridSize;
        Vector4f[] grid = c3d.getGrid();
        grid[0].set(restX, restY, zEff + 0.1f);
        grid[1].set(-gridSize, 0f);
        grid[2].set(0f, -gridSize);
        // Multiplicants
        grid[3].set(mx, my);
        gridSize = gridSize * 10f;
        int mx10 = (int) (c3d.getBounds().width / gridSize + 4) / 2;
        int my10 = (int) (c3d.getBounds().height / gridSize + 4) / 2;
        float restX10 = offset.x % gridSize;
        float restY10 = offset.y % gridSize;
        grid[4].set(restX10, restY10, zEff);
        grid[5].set(-gridSize, 0f);
        grid[6].set(0f, -gridSize);
        // Multiplicants
        grid[7].set(mx10, my10);
    }

    /**
     * @return The real transformation matrix of the viewport
     */
    public Matrix4f getRealViewport() {
        Matrix4f viewportTransform = new Matrix4f();
        Matrix4f.setIdentity(viewportTransform);
        float zoom = c3d.getZoom();
        Matrix4f.scale(new Vector3f(zoom, zoom, zoom), viewportTransform, viewportTransform);
        Matrix4f viewportRotation = c3d.getRotation();
        Matrix4f.mul(viewportRotation, viewportTransform, viewportTransform);
        Matrix4f viewportTranslation = c3d.getTranslation();
        Matrix4f.mul(viewportTransform, viewportTranslation, viewportTransform);
        return viewportTransform;
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
        Matrix4f vInverse = c3d.getViewportInverse();
        Matrix4f.transform(vInverse, relPos, relPos);
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
        float cursorX = 0.5f * cSize.x - relPos.x * View.PIXEL_PER_LDU;
        float cursorY = 0.5f * cSize.y + relPos.y * View.PIXEL_PER_LDU;
        relPos.set(cursorX, cursorY, 0f, 1f);
        return relPos;
    }

    Vector3d getScreenCoordinatesFrom3D(BigDecimal x, BigDecimal y, BigDecimal z) {
        BigDecimal thousand = new BigDecimal(1000);
        BigDecimal zero = new BigDecimal(0);
        Matrix vm = new Matrix(c3d.getViewport());
        Matrix vm2 = new Matrix(thousand, zero, zero, zero, zero, thousand, zero, zero, zero, zero, thousand, zero, zero, zero, zero, thousand);
        vm = Matrix.mul(vm, vm2);
        Vector3d result = new Vector3d(x, y, z);
        result = vm.transform(result);
        return result;
    }

    /**
     * Transforms screen coordinates to 3D space coordinates
     * (needs viewport inverse)
     * @return vector position in 3D space
     */
    public Vector3r get3DCoordinatesFromScreen(Vector3r vector3r, RationalMatrix m) {
        Vector3r relPos = new Vector3r(vector3r.x.subtract(tx), vector3r.y.subtract(ty), vector3r.z);
        relPos = m.transform(relPos);
        return relPos;
    }

    /**
     * Transforms 3D space coordinates to screen coordinates
     *
     *
     *
     * (needs viewport matrix)
     * @return vector position on screen
     */
    public Vector3r getScreenCoordinatesFrom3D(Vector3r vector3r, RationalMatrix m) {
        Vector3r relPos = m.transform(vector3r);
        relPos = new Vector3r(relPos.x.add(tx), relPos.y.add(ty), relPos.z);
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
     * Initializes the viewport and perspective
     */
    public void initializeViewportPerspective() {
        if (c3d.getRenderer() instanceof OpenGLRenderer20) {
            // MARK OpenGL Viewport and Perspective
            Rectangle bounds = c3d.getBounds();
            Rectangle scaledBounds = c3d.getScaledBounds();
            GL11.glViewport(0, 0, scaledBounds.width, scaledBounds.height);
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();
            float viewportWidth = bounds.width / View.PIXEL_PER_LDU / 2.0f;
            float viewportHeight = bounds.height / View.PIXEL_PER_LDU / 2.0f;
            GL11.glOrtho(-viewportWidth, viewportWidth, -viewportHeight, viewportHeight, c3d.getzNear() * c3d.getZoom(), c3d.getzFar() * c3d.getZoom());
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadIdentity();
        }
        calculateOriginData();
    }

    /**
     * Zooming in
     */
    public void zoomIn() {
        if (c3d.isDoingSelection())
            return;
        zoomExponent++;
        if (zoomExponent > 20) {
            zoomExponent = 20;
        }
        c3d.setZoom((float) Math.pow(10.0d, zoomExponent / 10 - 3));
        c3d.setViewportPixelPerLDU(c3d.getZoom() * View.PIXEL_PER_LDU);
        GuiStatusManager.updateStatus(c3d);
        ((ScalableComposite) c3d.getParent()).redrawScales();
        initializeViewportPerspective();
        syncZoom();
    }

    /**
     * Zooming out
     */
    public void zoomOut() {
        if (c3d.isDoingSelection())
            return;
        zoomExponent--;
        if (zoomExponent < -40) {
            zoomExponent = -40;
        }
        c3d.setZoom((float) Math.pow(10.0d, zoomExponent / 10 - 3));
        c3d.setViewportPixelPerLDU(c3d.getZoom() * View.PIXEL_PER_LDU);
        GuiStatusManager.updateStatus(c3d);
        ((ScalableComposite) c3d.getParent()).redrawScales();
        initializeViewportPerspective();
        syncZoom();
    }

    public void zoomReset() {
        if (c3d.isDoingSelection())
            return;
        zoomExponent = -20;
        c3d.setZoom((float) Math.pow(10.0d, zoomExponent / 10 - 3));
        c3d.setViewportPixelPerLDU(c3d.getZoom() * View.PIXEL_PER_LDU);
        c3d.getTranslation().setIdentity();
        GuiStatusManager.updateStatus(c3d);
        ((ScalableComposite) c3d.getParent()).redrawScales();
        initializeViewportPerspective();
        syncZoom();
    }

    /**
     * Gets the translated String for the perspective value
     *
     * @param perspective
     *            the perspective value from {@linkplain Perspective}.
     * @return (e.g. Perspective.FRONT returns Front)
     */
    String getPerspectiveString(Perspective perspective) {
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

    public float getZoomExponent() {
        return zoomExponent;
    }

    public void setZoomExponent(float zoomExponent) {
        this.zoomExponent = zoomExponent;
    }

    public Vector4f getOffset() {
        return new Vector4f(offset.x, offset.y, offset.z, offset.w);
    }

    public void setOffset(Vector4f off) {
        offset.x = off.x;
        offset.y = off.y;
        offset.z = off.z;
        offset.w = off.w;
    }

    private void syncZoom() {
        if (c3d.isSyncZoom()) {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d2 = renderer.getC3D();
                if (c3d != c3d2 && c3d.getLockableDatFileReference().equals(c3d2.getLockableDatFileReference())) {
                    c3d2.setZoom(c3d.getZoom());
                    c3d2.getPerspectiveCalculator().setZoomExponent(c3d.getPerspectiveCalculator().getZoomExponent());
                    c3d2.setViewportPixelPerLDU(c3d.getZoom() * View.PIXEL_PER_LDU);
                    ((ScalableComposite) c3d2.getParent()).redrawScales();
                    c3d2.getPerspectiveCalculator().initializeViewportPerspective();
                }
            }
        }
    }
}
