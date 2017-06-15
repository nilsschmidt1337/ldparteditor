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
package org.nschmidt.ldparteditor.helpers;

import java.math.BigDecimal;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.Matrix;
import org.nschmidt.ldparteditor.enums.ManipulatorScope;
import org.nschmidt.ldparteditor.enums.RotationSnap;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.composite3d.PerspectiveCalculator;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;

/**
 * @author nils
 *
 */
public class Manipulator {

    public static final int X_TRANSLATE = 0;
    public static final int Y_TRANSLATE = 1;
    public static final int Z_TRANSLATE = 2;

    public static final int X_ROTATE = 3;
    public static final int Y_ROTATE = 4;
    public static final int Z_ROTATE = 5;

    public static final int X_SCALE = 6;
    public static final int Y_SCALE = 7;
    public static final int Z_SCALE = 8;

    public static final int V_ROTATE = 9;

    public static final int X_ROTATE_ARROW = 11;
    public static final int Y_ROTATE_ARROW = 13;
    public static final int Z_ROTATE_ARROW = 15;
    public static final int V_ROTATE_ARROW = 17;

    private final Matrix4f result = new Matrix4f();
    private final Matrix4f resultinv = new Matrix4f();
    private final Matrix4f scale = new Matrix4f();

    private Matrix accurateResult = View.ACCURATE_ID;
    private Matrix accurateScale = View.ACCURATE_ID;
    private double accurateRotationX = 0.0;
    private double accurateRotationY = 0.0;
    private double accurateRotationZ = 0.0;

    private final FloatBuffer matrix = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer matrix_inv = BufferUtils.createFloatBuffer(16);
    private volatile boolean modified = false;

    private static float translate_size = 140f;
    private static float rotate_size = 100f;
    private static float rotate_outer_size = 120f;
    private static float scale_size = 60f;

    private Vector4f xAxis = new Vector4f(1f, 0f, 0f, 1f);
    private Vector4f yAxis = new Vector4f(0f, 1f, 0f, 1f);
    private Vector4f zAxis = new Vector4f(0f, 0f, 1f, 1f);

    private BigDecimal[] accurateXaxis = new BigDecimal[] { BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO };
    private BigDecimal[] accurateYaxis = new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO };
    private BigDecimal[] accurateZaxis = new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE };

    private Vector4f x_rotateArrow = new Vector4f(0f, 0f, 1f, 1f);
    private Vector4f y_rotateArrow = new Vector4f(0f, 0f, 1f, 1f);
    private Vector4f z_rotateArrow = new Vector4f(0f, 0f, 1f, 1f);
    private Vector4f v_rotateArrow = new Vector4f(0f, 0f, 1f, 1f);

    private static BigDecimal snap_x_Translate = new BigDecimal("100"); //$NON-NLS-1$
    private static BigDecimal snap_y_Translate = new BigDecimal("100"); //$NON-NLS-1$
    private static BigDecimal snap_z_Translate = new BigDecimal("100"); //$NON-NLS-1$

    final float snap_x_Scale = 400f;
    final float snap_y_Scale = 400f;
    final float snap_z_Scale = 400f;

    private static BigDecimal factor_x_Scale = new BigDecimal("1.1"); //$NON-NLS-1$
    private static BigDecimal factor_y_Scale = new BigDecimal("1.1"); //$NON-NLS-1$
    private static BigDecimal factor_z_Scale = new BigDecimal("1.1"); //$NON-NLS-1$

    private static BigDecimal snap_x_Rotate = new BigDecimal(Math.PI).divide(new BigDecimal(8), Threshold.mc);
    private static BigDecimal snap_y_Rotate = new BigDecimal(Math.PI).divide(new BigDecimal(8), Threshold.mc);
    private static BigDecimal snap_z_Rotate = new BigDecimal(Math.PI).divide(new BigDecimal(8), Threshold.mc);
    private static BigDecimal snap_v_Rotate = new BigDecimal(Math.PI).divide(new BigDecimal(8), Threshold.mc);


    private static RotationSnap snap_x_RotateFlag = RotationSnap.COMPLEX;
    private static RotationSnap snap_y_RotateFlag = RotationSnap.COMPLEX;
    private static RotationSnap snap_z_RotateFlag = RotationSnap.COMPLEX;
    private static RotationSnap snap_v_RotateFlag = RotationSnap.COMPLEX;

    public static void setSnap(BigDecimal trans, BigDecimal rot, BigDecimal scale) {

        try {
            rot.intValueExact();
            switch (rot.intValue()) {
            case 90:
                snap_x_RotateFlag = RotationSnap.DEG90;
                break;
            case 180:
                snap_x_RotateFlag = RotationSnap.DEG180;
                break;
            case 270:
                snap_x_RotateFlag = RotationSnap.DEG270;
                break;
            case 360:
                snap_x_RotateFlag = RotationSnap.DEG360;
                break;
            default:
                snap_x_RotateFlag = RotationSnap.COMPLEX;
                break;
            }

            switch (rot.intValue()) {
            case 90:
                snap_y_RotateFlag = RotationSnap.DEG90;
                break;
            case 180:
                snap_y_RotateFlag = RotationSnap.DEG180;
                break;
            case 270:
                snap_y_RotateFlag = RotationSnap.DEG270;
                break;
            case 360:
                snap_y_RotateFlag = RotationSnap.DEG360;
                break;
            default:
                snap_y_RotateFlag = RotationSnap.COMPLEX;
                break;
            }

            switch (rot.intValue()) {
            case 90:
                snap_z_RotateFlag = RotationSnap.DEG90;
                break;
            case 180:
                snap_z_RotateFlag = RotationSnap.DEG180;
                break;
            case 270:
                snap_z_RotateFlag = RotationSnap.DEG270;
                break;
            case 360:
                snap_z_RotateFlag = RotationSnap.DEG360;
                break;
            default:
                snap_z_RotateFlag = RotationSnap.COMPLEX;
                break;
            }

            switch (rot.intValue()) {
            case 90:
                snap_v_RotateFlag = RotationSnap.DEG90;
                break;
            case 180:
                snap_v_RotateFlag = RotationSnap.DEG180;
                break;
            case 270:
                snap_v_RotateFlag = RotationSnap.DEG270;
                break;
            case 360:
                snap_v_RotateFlag = RotationSnap.DEG360;
                break;
            default:
                snap_v_RotateFlag = RotationSnap.COMPLEX;
                break;
            }
        } catch (ArithmeticException ae) {
            snap_x_RotateFlag = RotationSnap.COMPLEX;
            snap_y_RotateFlag = RotationSnap.COMPLEX;
            snap_z_RotateFlag = RotationSnap.COMPLEX;
            snap_v_RotateFlag = RotationSnap.COMPLEX;
        }

        rot = rot.divide(new BigDecimal(180), Threshold.mc).multiply(new BigDecimal(Math.PI));
        snap_x_Translate = trans;
        snap_y_Translate = trans;
        snap_z_Translate = trans;

        factor_x_Scale = scale;
        factor_y_Scale = scale;
        factor_z_Scale = scale;

        snap_x_Rotate = rot;
        snap_y_Rotate = rot;
        snap_z_Rotate = rot;
        snap_v_Rotate = rot;

    }

    public static BigDecimal[] getSnap() {
        return new BigDecimal[] { snap_x_Translate, snap_x_Rotate, factor_x_Scale };
    }

    private boolean lock = false;

    private boolean x_Translate;
    private boolean y_Translate;
    private boolean z_Translate;

    private boolean x_Rotate;
    private boolean y_Rotate;
    private boolean z_Rotate;
    private boolean v_Rotate;

    private boolean x_Scale;
    private boolean y_Scale;
    private boolean z_Scale;

    private boolean x_rotatingForwards;
    private boolean x_rotatingBackwards;

    private boolean y_rotatingForwards;
    private boolean y_rotatingBackwards;

    private boolean z_rotatingForwards;
    private boolean z_rotatingBackwards;

    private boolean v_rotatingForwards;
    private boolean v_rotatingBackwards;


    private int calmDownCounter = 1;

    private final float PI16TH = (float) (Math.PI / 16d);

    private Vector4f x_Rotate_start = new Vector4f(1f, 0f, 0f, 1f);
    private Vector4f y_Rotate_start = new Vector4f(1f, 0f, 0f, 1f);
    private Vector4f z_Rotate_start = new Vector4f(1f, 0f, 0f, 1f);
    private Vector4f v_Rotate_start = new Vector4f(1f, 0f, 0f, 1f);

    private static float activationTreshold = 200f;

    private Vector4f position = new Vector4f(0f, 0f, 0f, 1f);
    private BigDecimal[] accuratePosition = new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO };

    private static volatile long timstampModified = System.nanoTime();

    public Manipulator() {
        Matrix4f.setIdentity(result);
        Matrix4f.setIdentity(scale);
    }

    public void copyState(Manipulator origin) {
        this.accuratePosition = origin.accuratePosition.clone();
        this.position = new Vector4f(origin.position);
        this.xAxis = new Vector4f(origin.xAxis);
        this.yAxis = new Vector4f(origin.yAxis);
        this.zAxis = new Vector4f(origin.zAxis);
        this.accurateXaxis = origin.accurateXaxis.clone();
        this.accurateYaxis = origin.accurateYaxis.clone();
        this.accurateZaxis = origin.accurateZaxis.clone();

        this.result.load(origin.result);
        this.scale.load(origin.scale);
        this.accurateResult = new Matrix(origin.accurateResult);
        this.accurateScale = new Matrix(origin.accurateScale);
        this.accurateRotationX = origin.accurateRotationX;
        this.accurateRotationY = origin.accurateRotationY;
        this.accurateRotationZ = origin.accurateRotationZ;
        this.modified = origin.modified;
    }

    public Matrix4f getTempTransformation4f() {
        return result;
    }

    public Matrix getTempTransformationAccurate() {
        return accurateResult;
    }

    public FloatBuffer getTempTransformation() {
        Matrix4f.invert(result, resultinv);
        result.store(matrix);
        matrix.position(0);
        return matrix;
    }

    public Matrix4f getTempTransformationCSG4f() {
        Matrix4f m = new Matrix4f(result);
        m = (Matrix4f) m.transpose();
        m.m30 = m.m03;
        m.m31 = m.m13;
        m.m32 = m.m23;
        m.m03 = 0f;
        m.m13 = 0f;
        m.m23 = 0f;
        return m;
    }

    public FloatBuffer getTempTransformationInv() {
        resultinv.store(matrix_inv);
        matrix_inv.position(0);
        return matrix_inv;
    }

    public Vector4f getUntransformed(float x, float y, float z) {
        return Matrix4f.transform(resultinv, new Vector4f(x, y, z, 1f), null);
    }

    public boolean isModified() {
        if (modified) {
            timstampModified = System.nanoTime();
            return true;
        } else {
            return System.nanoTime() - timstampModified < 1.5E9;
        }
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public Vector4f getXaxis() {
        return xAxis;
    }

    public Vector4f getYaxis() {
        return yAxis;
    }

    public Vector4f getZaxis() {
        return zAxis;
    }

    public Vector4f getX_RotateArrow() {
        return x_rotateArrow;
    }

    public Vector4f getY_RotateArrow() {
        return y_rotateArrow;
    }

    public Vector4f getZ_RotateArrow() {
        return z_rotateArrow;
    }

    public Vector4f getV_RotateArrow() {
        return v_rotateArrow;
    }

    public Vector4f getPosition() {
        return position;
    }

    public boolean isX_Translate() {
        return x_Translate;
    }

    public void setX_Translate(boolean x_Translate) {
        this.x_Translate = x_Translate;
    }

    public boolean isY_Translate() {
        return y_Translate;
    }

    public void setY_Translate(boolean y_Translate) {
        this.y_Translate = y_Translate;
    }

    public boolean isZ_Translate() {
        return z_Translate;
    }

    public void setZ_Translate(boolean z_Translate) {
        this.z_Translate = z_Translate;
    }

    public boolean isX_Rotate() {
        return x_Rotate;
    }

    public void setX_Rotate(boolean x_Rotate) {
        this.x_Rotate = x_Rotate;
    }

    public boolean isY_Rotate() {
        return y_Rotate;
    }

    public void setY_Rotate(boolean y_Rotate) {
        this.y_Rotate = y_Rotate;
    }

    public boolean isZ_Rotate() {
        return z_Rotate;
    }

    public void setZ_Rotate(boolean z_Rotate) {
        this.z_Rotate = z_Rotate;
    }

    public boolean isV_Rotate() {
        return v_Rotate;
    }

    public void setV_Rotate(boolean v_Rotate) {
        this.v_Rotate = v_Rotate;
    }

    public boolean isX_Scale() {
        return x_Scale;
    }

    public void setX_Scale(boolean x_Scale) {
        this.x_Scale = x_Scale;
    }

    public boolean isY_Scale() {
        return y_Scale;
    }

    public void setY_Scale(boolean y_Scale) {
        this.y_Scale = y_Scale;
    }

    public boolean isZ_Scale() {
        return z_Scale;
    }

    public void setZ_Scale(boolean z_Scale) {
        this.z_Scale = z_Scale;
    }

    public boolean isX_rotatingForwards() {
        return x_rotatingForwards;
    }

    public void setX_rotatingForwards(boolean x_rotatingForwards) {
        this.x_rotatingForwards = x_rotatingForwards;
    }

    public boolean isX_rotatingBackwards() {
        return x_rotatingBackwards;
    }

    public void setX_rotatingBackwards(boolean x_rotatingBackwards) {
        this.x_rotatingBackwards = x_rotatingBackwards;
    }

    public boolean isY_rotatingForwards() {
        return y_rotatingForwards;
    }

    public void setY_rotatingForwards(boolean y_rotatingForwards) {
        this.y_rotatingForwards = y_rotatingForwards;
    }

    public boolean isY_rotatingBackwards() {
        return y_rotatingBackwards;
    }

    public void setY_rotatingBackwards(boolean y_rotatingBackwards) {
        this.y_rotatingBackwards = y_rotatingBackwards;
    }

    public boolean isZ_rotatingForwards() {
        return z_rotatingForwards;
    }

    public void setZ_rotatingForwards(boolean z_rotatingForwards) {
        this.z_rotatingForwards = z_rotatingForwards;
    }

    public boolean isZ_rotatingBackwards() {
        return z_rotatingBackwards;
    }

    public void setZ_rotatingBackwards(boolean z_rotatingBackwards) {
        this.z_rotatingBackwards = z_rotatingBackwards;
    }

    public boolean isV_rotatingForwards() {
        return v_rotatingForwards;
    }

    public void setV_rotatingForwards(boolean v_rotatingForwards) {
        this.v_rotatingForwards = v_rotatingForwards;
    }

    public boolean isV_rotatingBackwards() {
        return v_rotatingBackwards;
    }

    public void setV_rotatingBackwards(boolean v_rotatingBackwards) {
        this.v_rotatingBackwards = v_rotatingBackwards;
    }

    public GColour checkManipulatorStatus(float r, float g, float b, int type, Composite3D c3d, float zoom) {
        GColour result = manipulatorStatusHelper(r, g, b, type, c3d, zoom);
        if (View.manipulator_selected_Colour_r[0] == result.getR() && View.manipulator_selected_Colour_g[0] == result.getG() && View.manipulator_selected_Colour_b[0] == result.getB()) {
            switch (Editor3DWindow.getWindow().getWorkingLayer()) {
            case NONE:
                return result;
            default:
                return new GColour(-1, View.manipulator_outerCircle_Colour_r[0], View.manipulator_outerCircle_Colour_g[0], View.manipulator_outerCircle_Colour_b[0], 1f);
            }
        }
        switch (Editor3DWindow.getWindow().getWorkingLayer()) {
        case X:
            switch (type) {
            case X_TRANSLATE:
            case X_SCALE:
            case X_ROTATE:
                return new GColour(-1, View.manipulator_selected_Colour_r[0], View.manipulator_selected_Colour_g[0], View.manipulator_selected_Colour_b[0], 1f);
            default:
            }
            break;
        case Y:
            switch (type) {
            case Y_TRANSLATE:
            case Y_SCALE:
            case Y_ROTATE:
                return new GColour(-1, View.manipulator_selected_Colour_r[0], View.manipulator_selected_Colour_g[0], View.manipulator_selected_Colour_b[0], 1f);
            default:
            }
            break;
        case Z:
            switch (type) {
            case Z_TRANSLATE:
            case Z_SCALE:
            case Z_ROTATE:
                return new GColour(-1, View.manipulator_selected_Colour_r[0], View.manipulator_selected_Colour_g[0], View.manipulator_selected_Colour_b[0], 1f);
            default:
            }
            break;
        case XY:
            switch (type) {
            case X_TRANSLATE:
            case X_SCALE:
            case X_ROTATE:
            case Y_TRANSLATE:
            case Y_SCALE:
            case Y_ROTATE:
                return new GColour(-1, View.manipulator_selected_Colour_r[0], View.manipulator_selected_Colour_g[0], View.manipulator_selected_Colour_b[0], 1f);
            default:
            }
            break;
        case XZ:
            switch (type) {
            case X_TRANSLATE:
            case X_SCALE:
            case X_ROTATE:
            case Z_TRANSLATE:
            case Z_SCALE:
            case Z_ROTATE:
                return new GColour(-1, View.manipulator_selected_Colour_r[0], View.manipulator_selected_Colour_g[0], View.manipulator_selected_Colour_b[0], 1f);
            default:
            }
            break;
        case YZ:
            switch (type) {
            case Y_TRANSLATE:
            case Y_SCALE:
            case Y_ROTATE:
            case Z_TRANSLATE:
            case Z_SCALE:
            case Z_ROTATE:
                return new GColour(-1, View.manipulator_selected_Colour_r[0], View.manipulator_selected_Colour_g[0], View.manipulator_selected_Colour_b[0], 1f);
            default:
            }
            break;
        case XYZ:
            return new GColour(-1, View.manipulator_selected_Colour_r[0], View.manipulator_selected_Colour_g[0], View.manipulator_selected_Colour_b[0], 1f);
        case NONE:
        default:
            break;
        }
        return result;
    }

    private GColour manipulatorStatusHelper(float r, float g, float b, int type, Composite3D c3d, float zoom) {
        Vector4f[] gen = c3d.getGenerator();
        Vector3f axis = null;
        // Check if its rotation
        switch (type) {
        case X_ROTATE_ARROW:
            axis = new Vector3f(xAxis.x, xAxis.y, xAxis.z);
            break;
        case Y_ROTATE_ARROW:
            axis = new Vector3f(yAxis.x, yAxis.y, yAxis.z);
            break;
        case Z_ROTATE_ARROW:
            axis = new Vector3f(zAxis.x, zAxis.y, zAxis.z);
            break;
        case V_ROTATE_ARROW:
            axis = new Vector3f(gen[2].x, gen[2].y, gen[2].z);
            break;
        }
        switch (type) {
        case X_ROTATE:
        case Y_ROTATE:
        case Z_ROTATE:
        case V_ROTATE:
            // Take the axis
            Vector4f vector = null;
            switch (type) {
            case X_ROTATE:
                if (lock) {
                    if (x_Rotate) {
                        return new GColour(-1, View.manipulator_selected_Colour_r[0], View.manipulator_selected_Colour_g[0], View.manipulator_selected_Colour_b[0], 1f);
                    } else {
                        return new GColour(-1, r, g, b, 1f);
                    }
                }
                x_Rotate = false;
                vector = new Vector4f(yAxis);
                axis = new Vector3f(xAxis.x, xAxis.y, xAxis.z);
                break;
            case Y_ROTATE:
                if (lock) {
                    if (y_Rotate) {
                        return new GColour(-1, View.manipulator_selected_Colour_r[0], View.manipulator_selected_Colour_g[0], View.manipulator_selected_Colour_b[0], 1f);
                    } else {
                        return new GColour(-1, r, g, b, 1f);
                    }
                }
                y_Rotate = false;
                vector = new Vector4f(zAxis);
                axis = new Vector3f(yAxis.x, yAxis.y, yAxis.z);
                break;
            case Z_ROTATE:
                if (lock) {
                    if (z_Rotate) {
                        return new GColour(-1, View.manipulator_selected_Colour_r[0], View.manipulator_selected_Colour_g[0], View.manipulator_selected_Colour_b[0], 1f);
                    } else {
                        return new GColour(-1, r, g, b, 1f);
                    }
                }
                z_Rotate = false;
                vector = new Vector4f(xAxis);
                axis = new Vector3f(zAxis.x, zAxis.y, zAxis.z);
                break;
            case V_ROTATE:
                if (lock) {
                    if (v_Rotate) {
                        return new GColour(-1, View.manipulator_selected_Colour_r[0], View.manipulator_selected_Colour_g[0], View.manipulator_selected_Colour_b[0], 1f);
                    } else {
                        return new GColour(-1, r, g, b, 1f);
                    }
                }
                v_Rotate = false;
                vector = new Vector4f(gen[1].x, gen[1].y, gen[1].z, 1f);
                axis = new Vector3f(gen[2].x, gen[2].y, gen[2].z);
                break;
            }
            if (type == V_ROTATE) {
                vector.scale(rotate_outer_size / zoom / 1000f);
            } else {
                vector.scale(rotate_size / zoom / 1000f);
            }

            float sumangle = 0f;
            float angle = (float) (2d * Math.asin(activationTreshold / (20d * rotate_size)));

            float twoPI = (float) (Math.PI * 2f);

            Matrix4f rotMatrix = new Matrix4f();
            Matrix4f.setIdentity(rotMatrix);
            Matrix4f.rotate(angle, axis, rotMatrix, rotMatrix);

            float tx = c3d.getTranslation().m30;
            float ty = c3d.getTranslation().m31;
            float tz = c3d.getTranslation().m32;

            final PerspectiveCalculator p = c3d.getPerspectiveCalculator();
            final Vector2f mp = c3d.getMousePosition();

            while (sumangle < twoPI) {
                sumangle = sumangle + angle;
                Vector4f virtpos = new Vector4f(Vector4f.add(vector, position, null));
                Vector4f screenpos = p.getScreenCoordinatesFrom3D(virtpos.x, virtpos.y, virtpos.z);
                Vector4f screenpos2 = p.getScreenCoordinatesFrom3DonlyZ(vector.x - tx, vector.y - ty, vector.z - tz);
                if (screenpos2.z > 0f || type == V_ROTATE) {
                    float dists = (float) (Math.pow(mp.x - screenpos.x, 2) + Math.pow(mp.y - screenpos.y, 2));
                    if (dists < activationTreshold) {
                        switch (type) {
                        case X_ROTATE:
                            x_Rotate_start.set(vector);
                            x_Rotate = true;
                            break;
                        case Y_ROTATE:
                            y_Rotate_start.set(vector);
                            y_Rotate = true;
                            break;
                        case Z_ROTATE:
                            z_Rotate_start.set(vector);
                            z_Rotate = true;
                            break;
                        case V_ROTATE:
                            v_Rotate_start.set(vector);
                            v_Rotate = true;
                            break;
                        }
                        return new GColour(-1, View.manipulator_selected_Colour_r[0], View.manipulator_selected_Colour_g[0], View.manipulator_selected_Colour_b[0], 1f);
                    }
                }
                Matrix4f.transform(rotMatrix, vector, vector);
            }
            return new GColour(-1, r, g, b, 1f);
        default:
            // Take the correct size
            float size = 0f;
            switch (type) {
            case X_SCALE:
                if (lock) {
                    if (x_Scale) {
                        return new GColour(-1, View.manipulator_selected_Colour_r[0], View.manipulator_selected_Colour_g[0], View.manipulator_selected_Colour_b[0], 1f);
                    } else {
                        return new GColour(-1, r, g, b, 1f);
                    }
                }
                if (!c3d.getKeys().isCtrlPressed())
                    x_Scale = false;
            case Y_SCALE:
                if (lock) {
                    if (y_Scale) {
                        return new GColour(-1, View.manipulator_selected_Colour_r[0], View.manipulator_selected_Colour_g[0], View.manipulator_selected_Colour_b[0], 1f);
                    } else {
                        return new GColour(-1, r, g, b, 1f);
                    }
                }
                if (!c3d.getKeys().isCtrlPressed())
                    y_Scale = false;
            case Z_SCALE:
                if (lock) {
                    if (z_Scale) {
                        return new GColour(-1, View.manipulator_selected_Colour_r[0], View.manipulator_selected_Colour_g[0], View.manipulator_selected_Colour_b[0], 1f);
                    } else {
                        return new GColour(-1, r, g, b, 1f);
                    }
                }
                if (!c3d.getKeys().isCtrlPressed())
                    z_Scale = false;
                size = scale_size;
                break;
            case X_TRANSLATE:
                if (lock) {
                    if (x_Translate) {
                        return new GColour(-1, View.manipulator_selected_Colour_r[0], View.manipulator_selected_Colour_g[0], View.manipulator_selected_Colour_b[0], 1f);
                    } else {
                        return new GColour(-1, r, g, b, 1f);
                    }
                }
                if (!c3d.getKeys().isCtrlPressed())
                    x_Translate = false;
            case Y_TRANSLATE:
                if (lock) {
                    if (y_Translate) {
                        return new GColour(-1, View.manipulator_selected_Colour_r[0], View.manipulator_selected_Colour_g[0], View.manipulator_selected_Colour_b[0], 1f);
                    } else {
                        return new GColour(-1, r, g, b, 1f);
                    }
                }
                if (!c3d.getKeys().isCtrlPressed())
                    y_Translate = false;
            case Z_TRANSLATE:
                if (lock) {
                    if (z_Translate) {
                        return new GColour(-1, View.manipulator_selected_Colour_r[0], View.manipulator_selected_Colour_g[0], View.manipulator_selected_Colour_b[0], 1f);
                    } else {
                        return new GColour(-1, r, g, b, 1f);
                    }
                }
                if (!c3d.getKeys().isCtrlPressed())
                    z_Translate = false;
                size = translate_size;
                break;
            case X_ROTATE_ARROW:
                size = rotate_size;
                break;
            case Y_ROTATE_ARROW:
                size = rotate_size;
                break;
            case Z_ROTATE_ARROW:
                size = rotate_size;
                break;
            case V_ROTATE_ARROW:
                size = rotate_outer_size;
                break;
            }
            // Take the axis
            vector = null;
            switch (type) {
            case X_TRANSLATE:
            case X_SCALE:
                vector = new Vector4f(xAxis);
                break;
            case Y_TRANSLATE:
            case Y_SCALE:
                vector = new Vector4f(yAxis);
                break;
            case Z_TRANSLATE:
            case Z_SCALE:
                vector = new Vector4f(zAxis);
                break;
            case X_ROTATE_ARROW:
                vector = new Vector4f(x_Rotate_start);
                vector.setW(0f);
                vector.normalise();
                vector.setW(1f);
                x_rotateArrow.set(vector);
                break;
            case Y_ROTATE_ARROW:
                vector = new Vector4f(y_Rotate_start);
                vector.setW(0f);
                vector.normalise();
                vector.setW(1f);
                y_rotateArrow.set(vector);
                break;
            case Z_ROTATE_ARROW:
                vector = new Vector4f(z_Rotate_start);
                vector.setW(0f);
                vector.normalise();
                vector.setW(1f);
                z_rotateArrow.set(vector);
                break;
            case V_ROTATE_ARROW:
                vector = new Vector4f(v_Rotate_start);
                vector.setW(0f);
                vector.normalise();
                vector.setW(1f);
                v_rotateArrow.set(vector);
                break;
            }
            vector.scale(size / zoom / 1000f);

            final boolean rotate;
            switch (type) {
            case X_ROTATE_ARROW:
            case Y_ROTATE_ARROW:
            case Z_ROTATE_ARROW:
            case V_ROTATE_ARROW:
                rotate = true;
                break;
            default:
                rotate = false;
            }

            if (rotate) {
                Vector4f vector3 = new Vector4f(vector);
                vector3.scale(.25f);
                Vector4f virtpos3 = new Vector4f(Vector4f.add(vector3, position, null));
                Vector4f screenpos3 = c3d.getPerspectiveCalculator().getScreenCoordinatesFrom3D(virtpos3.x, virtpos3.y, virtpos3.z);
                float dists = (float) (Math.pow(c3d.getMousePosition().x - screenpos3.x, 2) + Math.pow(c3d.getMousePosition().y - screenpos3.y, 2));
                float dists3 = Float.MAX_VALUE;
                int position2 = 0;
                {
                    Vector4f vector2 = new Vector4f(vector);
                    vector2.scale(.25f);
                    Matrix4f m = new Matrix4f();
                    Matrix4f.setIdentity(m);
                    switch (type) {
                    case X_ROTATE_ARROW:
                        m.rotate(Math.max(snap_x_Rotate.floatValue(), PI16TH), new Vector3f(axis.x, axis.y, axis.z));
                        break;
                    case Y_ROTATE_ARROW:
                        m.rotate(Math.max(snap_y_Rotate.floatValue(), PI16TH), new Vector3f(axis.x, axis.y, axis.z));
                        break;
                    case Z_ROTATE_ARROW:
                        m.rotate(Math.max(snap_z_Rotate.floatValue(), PI16TH), new Vector3f(axis.x, axis.y, axis.z));
                        break;
                    case V_ROTATE_ARROW:
                        m.rotate(Math.max(snap_v_Rotate.floatValue(), PI16TH), new Vector3f(axis.x, axis.y, axis.z));
                        break;
                    }
                    Matrix4f.transform(m, vector2, vector2);
                    Vector4f virtpos2 = new Vector4f(Vector4f.add(vector2, position, null));
                    Vector4f screenpos2 = c3d.getPerspectiveCalculator().getScreenCoordinatesFrom3D(virtpos2.x, virtpos2.y, virtpos2.z);
                    dists3 = (float) (Math.pow(c3d.getMousePosition().x - screenpos2.x, 2) + Math.pow(c3d.getMousePosition().y - screenpos2.y, 2));
                    if (dists3 < dists) {
                        position2 = -1;
                    }
                }
                {
                    Vector4f vector2 = new Vector4f(vector);
                    vector2.scale(.25f);
                    Matrix4f m = new Matrix4f();
                    Matrix4f.setIdentity(m);
                    switch (type) {
                    case X_ROTATE_ARROW:
                        m.rotate(-Math.max(snap_x_Rotate.floatValue(), PI16TH), new Vector3f(axis.x, axis.y, axis.z));
                        break;
                    case Y_ROTATE_ARROW:
                        m.rotate(-Math.max(snap_y_Rotate.floatValue(), PI16TH), new Vector3f(axis.x, axis.y, axis.z));
                        break;
                    case Z_ROTATE_ARROW:
                        m.rotate(-Math.max(snap_z_Rotate.floatValue(), PI16TH), new Vector3f(axis.x, axis.y, axis.z));
                        break;
                    case V_ROTATE_ARROW:
                        m.rotate(-Math.max(snap_v_Rotate.floatValue(), PI16TH), new Vector3f(axis.x, axis.y, axis.z));
                        break;
                    }
                    Matrix4f.transform(m, vector2, vector2);
                    Vector4f virtpos2 = new Vector4f(Vector4f.add(vector2, position, null));
                    Vector4f screenpos2 = c3d.getPerspectiveCalculator().getScreenCoordinatesFrom3D(virtpos2.x, virtpos2.y, virtpos2.z);
                    float dists2 = (float) (Math.pow(c3d.getMousePosition().x - screenpos2.x, 2) + Math.pow(c3d.getMousePosition().y - screenpos2.y, 2));
                    if (dists2 < dists3 && dists2 < dists) {
                        position2 = 1;
                    }
                }
                if (dists < activationTreshold || position2 != 0) {
                    switch (type) {
                    case X_ROTATE_ARROW:
                        if (position2 < 0) {
                            if (calmDownCounter > 0) {
                                calmDownCounter -= 1;
                                break;
                            } else {
                                calmDownCounter = -2;
                                x_rotatingForwards = true;
                            }
                        }else {
                            if (calmDownCounter < 0) {
                                calmDownCounter += 1;
                                break;
                            } else {
                                calmDownCounter = 2;
                                x_rotatingBackwards = true;
                            }
                        }
                        break;
                    case Y_ROTATE_ARROW:
                        if (position2 < 0) {
                            if (calmDownCounter > 0) {
                                calmDownCounter -= 1;
                                break;
                            } else {
                                calmDownCounter = -2;
                                y_rotatingForwards = true;
                            }
                        }else {
                            if (calmDownCounter < 0) {
                                calmDownCounter += 1;
                                break;
                            } else {
                                calmDownCounter = 2;
                                y_rotatingBackwards = true;
                            }
                        }
                        break;
                    case Z_ROTATE_ARROW:
                        if (position2 < 0) {
                            if (calmDownCounter > 0) {
                                calmDownCounter -= 1;
                                break;
                            } else {
                                calmDownCounter = -2;
                                z_rotatingForwards = true;
                            }
                        }else {
                            if (calmDownCounter < 0) {
                                calmDownCounter += 1;
                                break;
                            } else {
                                calmDownCounter = 2;
                                z_rotatingBackwards = true;
                            }
                        }
                        break;
                    case V_ROTATE_ARROW:
                        if (position2 < 0) {
                            if (calmDownCounter > 0) {
                                calmDownCounter -= 1;
                                break;
                            } else {
                                calmDownCounter = -2;
                                v_rotatingForwards = true;
                            }
                        }else {
                            if (calmDownCounter < 0) {
                                calmDownCounter += 1;
                                break;
                            } else {
                                calmDownCounter = 2;
                                v_rotatingBackwards = true;
                            }
                        }
                        break;
                    }
                    return new GColour(-1, View.manipulator_selected_Colour_r[0], View.manipulator_selected_Colour_g[0], View.manipulator_selected_Colour_b[0], 1f);
                } else {
                    return new GColour(-1, r, g, b, 1f);
                }
            } else {
                Vector4f virtpos = new Vector4f(Vector4f.add(vector, position, null));
                Vector4f screenpos = c3d.getPerspectiveCalculator().getScreenCoordinatesFrom3D(virtpos.x, virtpos.y, virtpos.z);
                float dists = (float) (Math.pow(c3d.getMousePosition().x - screenpos.x, 2) + Math.pow(c3d.getMousePosition().y - screenpos.y, 2));
                if (dists < activationTreshold) {
                    switch (type) {
                    case X_TRANSLATE:
                        x_Translate = true;
                        break;
                    case X_SCALE:
                        x_Scale = true;
                        break;
                    case Y_TRANSLATE:
                        y_Translate = true;
                        break;
                    case Y_SCALE:
                        y_Scale = true;
                        break;
                    case Z_TRANSLATE:
                        z_Translate = true;
                        break;
                    case Z_SCALE:
                        z_Scale = true;
                        break;
                    }
                    return new GColour(-1, View.manipulator_selected_Colour_r[0], View.manipulator_selected_Colour_g[0], View.manipulator_selected_Colour_b[0], 1f);
                } else {
                    return new GColour(-1, r, g, b, 1f);
                }
            }
        }
    }

    public void lock() {
        lock = true;
        calmDownCounter = 0;
    }

    public void unlock() {
        lock = false;
    }

    public void startTranslation(Composite3D c3d) {
        c3d.getLockableDatFileReference().getVertexManager().backupHideShowState();
        modified = false;
        Matrix4f.setIdentity(result);
        Matrix4f.setIdentity(scale);
        accurateResult = View.ACCURATE_ID;
        accurateScale = View.ACCURATE_ID;
        accurateRotationX = 0.0;
        accurateRotationY = 0.0;
        accurateRotationZ = 0.0;
        // if (Editor3DWindow.getWindow().isMovingAdjacentData()) c3d.getLockableDatFileReference().getVertexManager().transformSelection(View.ACCURATE_ID, true);
    }

    public void startTranslation2(Composite3D c3d) {
        c3d.getLockableDatFileReference().getVertexManager().backupHideShowState();
        modified = false;
        Matrix4f.setIdentity(result);
        Matrix4f.setIdentity(scale);
        accurateResult = View.ACCURATE_ID;
        accurateScale = View.ACCURATE_ID;
        accurateRotationX = 0.0;
        accurateRotationY = 0.0;
        accurateRotationZ = 0.0;
        x_Translate = true;
        y_Translate = true;
        // if (Editor3DWindow.getWindow().isMovingAdjacentData()) c3d.getLockableDatFileReference().getVertexManager().transformSelection(View.ACCURATE_ID, true);
    }


    public void applyTranslation(Composite3D c3d) {
        if (modified) {
            c3d.getLockableDatFileReference().getVertexManager().transformSelection(accurateResult, null, Editor3DWindow.getWindow().isMovingAdjacentData());
        }
        resetTranslation();
    }

    public void applyTranslationAtSelect(Composite3D c3d) {
        if (modified) {
            c3d.getLockableDatFileReference().getVertexManager().transformSelection(accurateResult, null, Editor3DWindow.getWindow().isMovingAdjacentData());
        }
        resetTranslation();
        // x_Translate = false;
        // y_Translate = false;
    }

    public void resetTranslation() {
        accurateResult = View.ACCURATE_ID;
        accurateScale = View.ACCURATE_ID;
        accurateRotationX = 0.0;
        accurateRotationY = 0.0;
        accurateRotationZ = 0.0;
        Matrix4f.setIdentity(result);
        Matrix4f.setIdentity(scale);
        x_Translate = false;
        y_Translate = false;
        z_Translate = false;
        x_Rotate = false;
        y_Rotate = false;
        z_Rotate = false;
        x_Scale = false;
        y_Scale = false;
        z_Scale = false;

        x_rotatingForwards = false;
        x_rotatingBackwards = false;
        y_rotatingForwards = false;
        y_rotatingBackwards = false;
        z_rotatingForwards = false;
        z_rotatingBackwards = false;
        modified = false;
    }

    public Vector4f transform(Vector2f old_mouse_position, int new_x, int new_y, Composite3D c3d) {

        Vector4f temp = new Vector4f(this.position);
        boolean isGlobal = Editor3DWindow.getWindow().getTransformationMode() == ManipulatorScope.GLOBAL;
        if (isGlobal) {
            position = new Vector4f(0f, 0f, 0f, 1f);
            accuratePosition = new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO };
        }

        Vector2f d = new Vector2f(new_x - old_mouse_position.x, new_y - old_mouse_position.y);

        if (d.lengthSquared() == 0f)
            return temp;

        d.normalise();

        Vector4f pos3d1 = c3d.getPerspectiveCalculator().get3DCoordinatesFromScreen(new_x, new_y);
        Vector4f pos3d2 = c3d.getPerspectiveCalculator().get3DCoordinatesFromScreen((int) old_mouse_position.x, (int) old_mouse_position.y);
        Vector4f d3d = Vector4f.sub(pos3d1, pos3d2, null);
        float l = d3d.length();
        BigDecimal L = new BigDecimal(l / 1000f);

        //        NLogger.debug(getClass(), "Transforming... dx= " + d.x + " dy= " + d.y + " length= " + l); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        Matrix4f transformation = new Matrix4f();
        Matrix accurateTransformation;
        Matrix4f.setIdentity(transformation);

        if (x_Translate) {
            if (l < snap_x_Translate.floatValue() * 1000f)
                return temp;
            Vector4f vector = new Vector4f(xAxis);
            Vector4f virtpos = new Vector4f(Vector4f.add(vector, position, null));
            Vector4f screenpos = c3d.getPerspectiveCalculator().getScreenCoordinatesFrom3D(virtpos.x, virtpos.y, virtpos.z);
            Vector4f screenpos2 = c3d.getPerspectiveCalculator().getScreenCoordinatesFrom3D(position.x, position.y, position.z);
            Vector2f dA = new Vector2f(screenpos.x - screenpos2.x, screenpos.y - screenpos2.y);

            if (dA.lengthSquared() != 0f)
                dA.normalise();

            float factor = Math.max(l - l % (snap_x_Translate.floatValue() * 1000f), snap_x_Translate.floatValue() * 1000f);
            BigDecimal FACTOR = MathHelper.max(L.subtract(L.remainder(snap_x_Translate, Threshold.mc)), snap_x_Translate);

            if (Math.acos(dA.x * d.x + dA.y * d.y) < Math.PI / 2d) {
                transformation.m30 = xAxis.x * factor;
                transformation.m31 = xAxis.y * factor;
                transformation.m32 = xAxis.z * factor;
                accurateTransformation = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, accurateXaxis[0].multiply(FACTOR), accurateXaxis[1].multiply(FACTOR), accurateXaxis[2].multiply(FACTOR),
                        BigDecimal.ONE);
            } else {
                transformation.m30 = -xAxis.x * factor;
                transformation.m31 = -xAxis.y * factor;
                transformation.m32 = -xAxis.z * factor;
                accurateTransformation = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, accurateXaxis[0].multiply(FACTOR).negate(), accurateXaxis[1].multiply(FACTOR).negate(), accurateXaxis[2]
                                .multiply(FACTOR).negate(), BigDecimal.ONE);
            }
            accuratePosition = accurateTransformation.transform(accuratePosition[0], accuratePosition[1], accuratePosition[2]);
            Matrix4f.transform(transformation, position, position);
            accurateResult = Matrix.mul(accurateTransformation, accurateResult);
            Matrix4f.mul(transformation, result, result);
            Matrix4f.setIdentity(transformation);

            modified = true;
        }

        if (y_Translate) {
            if (l < snap_y_Translate.floatValue() * 1000f)
                return temp;
            Vector4f vector = new Vector4f(yAxis);
            Vector4f virtpos = new Vector4f(Vector4f.add(vector, position, null));
            Vector4f screenpos = c3d.getPerspectiveCalculator().getScreenCoordinatesFrom3D(virtpos.x, virtpos.y, virtpos.z);
            Vector4f screenpos2 = c3d.getPerspectiveCalculator().getScreenCoordinatesFrom3D(position.x, position.y, position.z);
            Vector2f dA = new Vector2f(screenpos.x - screenpos2.x, screenpos.y - screenpos2.y);

            if (dA.lengthSquared() != 0f)
                dA.normalise();

            float factor = Math.max(l - l % (snap_y_Translate.floatValue() * 1000f), snap_y_Translate.floatValue() * 1000f);
            BigDecimal FACTOR = MathHelper.max(L.subtract(L.remainder(snap_y_Translate, Threshold.mc)), snap_y_Translate);

            if (Math.acos(dA.x * d.x + dA.y * d.y) < Math.PI / 2d) {
                transformation.m30 = yAxis.x * factor;
                transformation.m31 = yAxis.y * factor;
                transformation.m32 = yAxis.z * factor;
                accurateTransformation = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, accurateYaxis[0].multiply(FACTOR), accurateYaxis[1].multiply(FACTOR), accurateYaxis[2].multiply(FACTOR),
                        BigDecimal.ONE);
            } else {
                transformation.m30 = -yAxis.x * factor;
                transformation.m31 = -yAxis.y * factor;
                transformation.m32 = -yAxis.z * factor;
                accurateTransformation = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, accurateYaxis[0].multiply(FACTOR).negate(), accurateYaxis[1].multiply(FACTOR).negate(), accurateYaxis[2]
                                .multiply(FACTOR).negate(), BigDecimal.ONE);
            }
            accuratePosition = accurateTransformation.transform(accuratePosition[0], accuratePosition[1], accuratePosition[2]);
            Matrix4f.transform(transformation, position, position);
            accurateResult = Matrix.mul(accurateTransformation, accurateResult);
            Matrix4f.mul(transformation, result, result);
            Matrix4f.setIdentity(transformation);

            modified = true;
        }

        if (z_Translate) {
            if (l < snap_z_Translate.floatValue() * 1000f)
                return temp;
            Vector4f vector = new Vector4f(zAxis);
            Vector4f virtpos = new Vector4f(Vector4f.add(vector, position, null));
            Vector4f screenpos = c3d.getPerspectiveCalculator().getScreenCoordinatesFrom3D(virtpos.x, virtpos.y, virtpos.z);
            Vector4f screenpos2 = c3d.getPerspectiveCalculator().getScreenCoordinatesFrom3D(position.x, position.y, position.z);
            Vector2f dA = new Vector2f(screenpos.x - screenpos2.x, screenpos.y - screenpos2.y);

            if (dA.lengthSquared() != 0f)
                dA.normalise();

            float factor = Math.max(l - l % (snap_z_Translate.floatValue() * 1000f), snap_z_Translate.floatValue() * 1000f);
            BigDecimal FACTOR = MathHelper.max(L.subtract(L.remainder(snap_z_Translate, Threshold.mc)), snap_z_Translate);

            if (Math.acos(dA.x * d.x + dA.y * d.y) < Math.PI / 2d) {
                transformation.m30 = zAxis.x * factor;
                transformation.m31 = zAxis.y * factor;
                transformation.m32 = zAxis.z * factor;
                accurateTransformation = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, accurateZaxis[0].multiply(FACTOR), accurateZaxis[1].multiply(FACTOR), accurateZaxis[2].multiply(FACTOR),
                        BigDecimal.ONE);
            } else {
                transformation.m30 = -zAxis.x * factor;
                transformation.m31 = -zAxis.y * factor;
                transformation.m32 = -zAxis.z * factor;
                accurateTransformation = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, accurateZaxis[0].multiply(FACTOR).negate(), accurateZaxis[1].multiply(FACTOR).negate(), accurateZaxis[2]
                                .multiply(FACTOR).negate(), BigDecimal.ONE);
            }
            accuratePosition = accurateTransformation.transform(accuratePosition[0], accuratePosition[1], accuratePosition[2]);
            Matrix4f.transform(transformation, position, position);
            accurateResult = Matrix.mul(accurateTransformation, accurateResult);
            Matrix4f.mul(transformation, result, result);
            Matrix4f.setIdentity(transformation);

            modified = true;
        }

        if (x_Rotate || y_Rotate || z_Rotate || v_Rotate) {
            Matrix4f forward = new Matrix4f();
            Matrix4f.setIdentity(forward);
            Matrix4f.translate(new Vector3f(-position.x, -position.y, -position.z), forward, forward);
            Matrix4f.mul(forward, result, result);
            accurateResult = Matrix.mul(View.ACCURATE_ID.translate(new BigDecimal[] { accuratePosition[0].negate(), accuratePosition[1].negate(), accuratePosition[2].negate() }), accurateResult);
        }

        if (x_Rotate) {
            while (true) {
                if (x_rotatingForwards) {
                    transformation.rotate(snap_x_Rotate.floatValue(), new Vector3f(xAxis.x, xAxis.y, xAxis.z));
                    accurateTransformation = View.ACCURATE_ID.rotate(snap_x_Rotate, snap_x_RotateFlag, accurateXaxis);
                    accurateRotationX = accurateRotationX + snap_x_Rotate.doubleValue();
                    Vector4f vector = new Vector4f(x_rotateArrow);
                    Matrix4f m = new Matrix4f();
                    Matrix4f.setIdentity(m);
                    m.rotate(Math.max(snap_x_Rotate.floatValue(), PI16TH), new Vector3f(xAxis.x, xAxis.y, xAxis.z));
                    Matrix4f.transform(m, vector, vector);
                    vector.setW(0f);
                    vector.normalise();
                    vector.setW(1f);
                    x_rotateArrow.set(vector);
                    x_Rotate_start.set(x_rotateArrow);
                } else if (x_rotatingBackwards) {
                    transformation.rotate(-snap_x_Rotate.floatValue(), new Vector3f(xAxis.x, xAxis.y, xAxis.z));
                    accurateTransformation = View.ACCURATE_ID.rotate(snap_x_Rotate.negate(), snap_x_RotateFlag, accurateXaxis);
                    accurateRotationX = accurateRotationX - snap_x_Rotate.doubleValue();
                    Vector4f vector = new Vector4f(x_rotateArrow);
                    Matrix4f m = new Matrix4f();
                    Matrix4f.setIdentity(m);
                    m.rotate(-Math.max(snap_x_Rotate.floatValue(), PI16TH), new Vector3f(xAxis.x, xAxis.y, xAxis.z));
                    Matrix4f.transform(m, vector, vector);
                    vector.setW(0f);
                    vector.normalise();
                    vector.setW(1f);
                    x_rotateArrow.set(vector);
                    x_Rotate_start.set(x_rotateArrow);
                } else {
                    break;
                }
                x_rotatingForwards = false;
                x_rotatingBackwards = false;
                Matrix4f.transform(transformation, yAxis, yAxis);
                Matrix4f.transform(transformation, zAxis, zAxis);
                accurateYaxis = accurateTransformation.transform(accurateYaxis[0], accurateYaxis[1], accurateYaxis[2]);
                accurateZaxis = accurateTransformation.transform(accurateZaxis[0], accurateZaxis[1], accurateZaxis[2]);

                accurateResult = Matrix.mul(accurateTransformation, accurateResult);
                Matrix4f.mul(transformation, result, result);
                Matrix4f.setIdentity(transformation);

                modified = true;
                break;
            }
        }

        if (y_Rotate) {
            while (true) {
                if (y_rotatingForwards) {
                    transformation.rotate(snap_y_Rotate.floatValue(), new Vector3f(yAxis.x, yAxis.y, yAxis.z));
                    accurateTransformation = View.ACCURATE_ID.rotate(snap_y_Rotate, snap_y_RotateFlag, accurateYaxis);
                    accurateRotationY = accurateRotationY + snap_y_Rotate.doubleValue();
                    Vector4f vector = new Vector4f(y_rotateArrow);
                    Matrix4f m = new Matrix4f();
                    Matrix4f.setIdentity(m);
                    m.rotate(Math.max(snap_y_Rotate.floatValue(), PI16TH), new Vector3f(yAxis.x, yAxis.y, yAxis.z));
                    Matrix4f.transform(m, vector, vector);
                    vector.setW(0f);
                    vector.normalise();
                    vector.setW(1f);
                    y_rotateArrow.set(vector);
                    y_Rotate_start.set(y_rotateArrow);
                } else if (y_rotatingBackwards) {
                    transformation.rotate(-snap_y_Rotate.floatValue(), new Vector3f(yAxis.x, yAxis.y, yAxis.z));
                    accurateTransformation = View.ACCURATE_ID.rotate(snap_y_Rotate.negate(), snap_y_RotateFlag, accurateYaxis);
                    accurateRotationY = accurateRotationY - snap_y_Rotate.doubleValue();
                    Vector4f vector = new Vector4f(y_rotateArrow);
                    Matrix4f m = new Matrix4f();
                    Matrix4f.setIdentity(m);
                    m.rotate(-Math.max(snap_y_Rotate.floatValue(), PI16TH), new Vector3f(yAxis.x, yAxis.y, yAxis.z));
                    Matrix4f.transform(m, vector, vector);
                    vector.setW(0f);
                    vector.normalise();
                    vector.setW(1f);
                    y_rotateArrow.set(vector);
                    y_Rotate_start.set(y_rotateArrow);
                } else {
                    break;
                }
                y_rotatingForwards = false;
                y_rotatingBackwards = false;
                Matrix4f.transform(transformation, xAxis, xAxis);
                Matrix4f.transform(transformation, zAxis, zAxis);
                accurateXaxis = accurateTransformation.transform(accurateXaxis[0], accurateXaxis[1], accurateXaxis[2]);
                accurateZaxis = accurateTransformation.transform(accurateZaxis[0], accurateZaxis[1], accurateZaxis[2]);

                accurateResult = Matrix.mul(accurateTransformation, accurateResult);
                Matrix4f.mul(transformation, result, result);
                Matrix4f.setIdentity(transformation);

                modified = true;
                break;
            }
        }

        if (z_Rotate) {
            while (true) {
                if (z_rotatingForwards) {
                    transformation.rotate(snap_z_Rotate.floatValue(), new Vector3f(zAxis.x, zAxis.y, zAxis.z));
                    accurateTransformation = View.ACCURATE_ID.rotate(snap_z_Rotate, snap_z_RotateFlag, accurateZaxis);
                    accurateRotationZ = accurateRotationZ + snap_z_Rotate.doubleValue();
                    Vector4f vector = new Vector4f(z_rotateArrow);
                    Matrix4f m = new Matrix4f();
                    Matrix4f.setIdentity(m);
                    m.rotate(Math.max(snap_z_Rotate.floatValue(), PI16TH), new Vector3f(zAxis.x, zAxis.y, zAxis.z));
                    Matrix4f.transform(m, vector, vector);
                    vector.setW(0f);
                    vector.normalise();
                    vector.setW(1f);
                    z_rotateArrow.set(vector);
                    z_Rotate_start.set(z_rotateArrow);
                } else if (z_rotatingBackwards) {
                    transformation.rotate(-snap_z_Rotate.floatValue(), new Vector3f(zAxis.x, zAxis.y, zAxis.z));
                    accurateTransformation = View.ACCURATE_ID.rotate(snap_z_Rotate.negate(), snap_z_RotateFlag, accurateZaxis);
                    accurateRotationZ = accurateRotationZ - snap_z_Rotate.doubleValue();
                    Vector4f vector = new Vector4f(z_rotateArrow);
                    Matrix4f m = new Matrix4f();
                    Matrix4f.setIdentity(m);
                    m.rotate(-Math.max(snap_z_Rotate.floatValue(), PI16TH), new Vector3f(zAxis.x, zAxis.y, zAxis.z));
                    Matrix4f.transform(m, vector, vector);
                    vector.setW(0f);
                    vector.normalise();
                    vector.setW(1f);
                    z_rotateArrow.set(vector);
                    z_Rotate_start.set(z_rotateArrow);
                } else {
                    break;
                }
                z_rotatingForwards = false;
                z_rotatingBackwards = false;
                Matrix4f.transform(transformation, yAxis, yAxis);
                Matrix4f.transform(transformation, xAxis, xAxis);
                accurateYaxis = accurateTransformation.transform(accurateYaxis[0], accurateYaxis[1], accurateYaxis[2]);
                accurateXaxis = accurateTransformation.transform(accurateXaxis[0], accurateXaxis[1], accurateXaxis[2]);

                accurateResult = Matrix.mul(accurateTransformation, accurateResult);
                Matrix4f.mul(transformation, result, result);
                Matrix4f.setIdentity(transformation);

                modified = true;
                break;
            }
        }

        if (v_Rotate) {
            while (true) {
                Vector4f[] gen = c3d.getGenerator();
                if (v_rotatingForwards) {
                    transformation.rotate(snap_v_Rotate.floatValue(), new Vector3f(gen[2].x, gen[2].y, gen[2].z));
                    accurateTransformation = View.ACCURATE_ID.rotate(snap_v_Rotate, snap_v_RotateFlag, new BigDecimal[] { new BigDecimal(gen[2].x), new BigDecimal(gen[2].y), new BigDecimal(gen[2].z) });
                    Vector4f vector = new Vector4f(v_rotateArrow);
                    Matrix4f m = new Matrix4f();
                    Matrix4f.setIdentity(m);
                    m.rotate(Math.max(snap_v_Rotate.floatValue(), PI16TH), new Vector3f(gen[2].x, gen[2].y, gen[2].z));
                    Matrix4f.transform(m, vector, vector);
                    vector.setW(0f);
                    vector.normalise();
                    vector.setW(1f);
                    v_rotateArrow.set(vector);
                    v_Rotate_start.set(v_rotateArrow);
                } else if (v_rotatingBackwards) {
                    transformation.rotate(-snap_v_Rotate.floatValue(), new Vector3f(gen[2].x, gen[2].y, gen[2].z));
                    accurateTransformation = View.ACCURATE_ID.rotate(snap_v_Rotate.negate(), snap_v_RotateFlag, new BigDecimal[] { new BigDecimal(gen[2].x), new BigDecimal(gen[2].y), new BigDecimal(gen[2].z) });
                    Vector4f vector = new Vector4f(v_rotateArrow);
                    Matrix4f m = new Matrix4f();
                    Matrix4f.setIdentity(m);
                    m.rotate(-Math.max(snap_v_Rotate.floatValue(), PI16TH), new Vector3f(gen[2].x, gen[2].y, gen[2].z));
                    Matrix4f.transform(m, vector, vector);
                    vector.setW(0f);
                    vector.normalise();
                    vector.setW(1f);
                    v_rotateArrow.set(vector);
                    v_Rotate_start.set(v_rotateArrow);
                } else {
                    break;
                }
                v_rotatingForwards = false;
                v_rotatingBackwards = false;
                Matrix4f.transform(transformation, zAxis, zAxis);
                Matrix4f.transform(transformation, yAxis, yAxis);
                Matrix4f.transform(transformation, xAxis, xAxis);
                accurateXaxis = accurateTransformation.transform(accurateXaxis[0], accurateXaxis[1], accurateXaxis[2]);
                accurateYaxis = accurateTransformation.transform(accurateYaxis[0], accurateYaxis[1], accurateYaxis[2]);
                accurateZaxis = accurateTransformation.transform(accurateZaxis[0], accurateZaxis[1], accurateZaxis[2]);

                accurateResult = Matrix.mul(accurateTransformation, accurateResult);
                Matrix4f.mul(transformation, result, result);
                Matrix4f.setIdentity(transformation);

                modified = true;
                break;
            }
        }

        if (x_Rotate || y_Rotate || z_Rotate || v_Rotate) {
            Matrix4f backward = new Matrix4f();
            Matrix4f.setIdentity(backward);
            Matrix4f.translate(new Vector3f(position.x, position.y, position.z), backward, backward);
            Matrix4f.mul(backward, result, result);
            accurateResult = Matrix.mul(View.ACCURATE_ID.translate(new BigDecimal[] { accuratePosition[0], accuratePosition[1], accuratePosition[2] }), accurateResult);
        }

        if (x_Scale || y_Scale || z_Scale) {
            Matrix4f.setIdentity(scale);
            accurateScale = View.ACCURATE_ID;
        }

        boolean isScaling = false;
        BigDecimal oldScaleFactor = BigDecimal.ONE;
        float oldFactor = 1f;

        if (x_Scale) {
            if (l < snap_x_Scale)
                return temp;
            Vector4f vector = new Vector4f(xAxis);
            Vector4f virtpos = new Vector4f(Vector4f.add(vector, position, null));
            Vector4f screenpos = c3d.getPerspectiveCalculator().getScreenCoordinatesFrom3D(virtpos.x, virtpos.y, virtpos.z);
            Vector4f screenpos2 = c3d.getPerspectiveCalculator().getScreenCoordinatesFrom3D(position.x, position.y, position.z);
            Vector2f dA = new Vector2f(screenpos.x - screenpos2.x, screenpos.y - screenpos2.y);

            if (dA.lengthSquared() != 0f)
                dA.normalise();

            float factor;
            BigDecimal FACTOR;
            if (Math.acos(dA.x * d.x + dA.y * d.y) < Math.PI / 2d) {
                factor = factor_x_Scale.floatValue();
                FACTOR = factor_x_Scale;
            } else {
                factor = 1f / factor_x_Scale.floatValue();
                FACTOR = BigDecimal.ONE.divide(factor_x_Scale, Threshold.mc);
            }
            isScaling = true;
            oldFactor = factor;
            oldScaleFactor = FACTOR;

            transformation.m30 = -position.x;
            transformation.m31 = -position.y;
            transformation.m32 = -position.z;

            accurateTransformation = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, accuratePosition[0].negate(), accuratePosition[1].negate(), accuratePosition[2].negate(), BigDecimal.ONE);
            accurateScale = Matrix.mul(accurateTransformation, accurateScale);

            Matrix4f.mul(transformation, scale, scale);
            transformation.setIdentity();

            transformation.m00 = xAxis.x;
            transformation.m01 = xAxis.y;
            transformation.m02 = xAxis.z;
            transformation.m10 = yAxis.x;
            transformation.m11 = yAxis.y;
            transformation.m12 = yAxis.z;
            transformation.m20 = zAxis.x;
            transformation.m21 = zAxis.y;
            transformation.m22 = zAxis.z;
            accurateTransformation = new Matrix(accurateXaxis[0], accurateXaxis[1], accurateXaxis[2], BigDecimal.ZERO, accurateYaxis[0], accurateYaxis[1], accurateYaxis[2], BigDecimal.ZERO,
                    accurateZaxis[0], accurateZaxis[1], accurateZaxis[2], BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE);

            accurateTransformation = accurateTransformation.invert();
            transformation = Matrix4f.invert(transformation, null);

            accurateScale = Matrix.mul(accurateTransformation, accurateScale);
            Matrix4f.mul(transformation, scale, scale);
            Matrix4f.setIdentity(transformation);

            transformation.m00 = factor * xAxis.x;
            transformation.m01 = factor * xAxis.y;
            transformation.m02 = factor * xAxis.z;
            transformation.m10 = yAxis.x;
            transformation.m11 = yAxis.y;
            transformation.m12 = yAxis.z;
            transformation.m20 = zAxis.x;
            transformation.m21 = zAxis.y;
            transformation.m22 = zAxis.z;
            accurateTransformation = new Matrix(accurateXaxis[0].multiply(FACTOR), accurateXaxis[1].multiply(FACTOR), accurateXaxis[2].multiply(FACTOR), BigDecimal.ZERO, accurateYaxis[0],
                    accurateYaxis[1], accurateYaxis[2], BigDecimal.ZERO, accurateZaxis[0], accurateZaxis[1], accurateZaxis[2], BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ONE);

            accurateScale = Matrix.mul(accurateTransformation, accurateScale);
            Matrix4f.mul(transformation, scale, scale);
            Matrix4f.setIdentity(transformation);

            transformation.m30 = position.x;
            transformation.m31 = position.y;
            transformation.m32 = position.z;

            accurateTransformation = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, accuratePosition[0], accuratePosition[1], accuratePosition[2], BigDecimal.ONE);
            accurateScale = Matrix.mul(accurateTransformation, accurateScale);

            Matrix4f.mul(transformation, scale, scale);
            transformation.setIdentity();

            modified = true;
        }

        if (y_Scale) {
            if (l < snap_y_Scale)
                return temp;
            Vector4f vector = new Vector4f(yAxis);
            Vector4f virtpos = new Vector4f(Vector4f.add(vector, position, null));
            Vector4f screenpos = c3d.getPerspectiveCalculator().getScreenCoordinatesFrom3D(virtpos.x, virtpos.y, virtpos.z);
            Vector4f screenpos2 = c3d.getPerspectiveCalculator().getScreenCoordinatesFrom3D(position.x, position.y, position.z);
            Vector2f dA = new Vector2f(screenpos.x - screenpos2.x, screenpos.y - screenpos2.y);

            if (dA.lengthSquared() != 0f)
                dA.normalise();

            float factor;
            BigDecimal FACTOR;
            if (isScaling) {
                factor = oldFactor;
                FACTOR = oldScaleFactor;
            } else {
                if (Math.acos(dA.x * d.x + dA.y * d.y) < Math.PI / 2d) {
                    factor = factor_y_Scale.floatValue();
                    FACTOR = factor_y_Scale;
                } else {
                    factor = 1f / factor_y_Scale.floatValue();
                    FACTOR = BigDecimal.ONE.divide(factor_y_Scale, Threshold.mc);
                }
                isScaling = true;
                oldFactor = factor;
                oldScaleFactor = FACTOR;
            }

            transformation.m30 = -position.x;
            transformation.m31 = -position.y;
            transformation.m32 = -position.z;

            accurateTransformation = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, accuratePosition[0].negate(), accuratePosition[1].negate(), accuratePosition[2].negate(), BigDecimal.ONE);
            accurateScale = Matrix.mul(accurateTransformation, accurateScale);

            Matrix4f.mul(transformation, scale, scale);
            transformation.setIdentity();

            transformation.m00 = xAxis.x;
            transformation.m01 = xAxis.y;
            transformation.m02 = xAxis.z;
            transformation.m10 = yAxis.x;
            transformation.m11 = yAxis.y;
            transformation.m12 = yAxis.z;
            transformation.m20 = zAxis.x;
            transformation.m21 = zAxis.y;
            transformation.m22 = zAxis.z;
            accurateTransformation = new Matrix(accurateXaxis[0], accurateXaxis[1], accurateXaxis[2], BigDecimal.ZERO, accurateYaxis[0], accurateYaxis[1], accurateYaxis[2], BigDecimal.ZERO,
                    accurateZaxis[0], accurateZaxis[1], accurateZaxis[2], BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE);

            accurateTransformation = accurateTransformation.invert();
            transformation = Matrix4f.invert(transformation, null);

            accurateScale = Matrix.mul(accurateTransformation, accurateScale);
            Matrix4f.mul(transformation, scale, scale);
            Matrix4f.setIdentity(transformation);

            transformation.m00 = xAxis.x;
            transformation.m01 = xAxis.y;
            transformation.m02 = xAxis.z;
            transformation.m10 = factor * yAxis.x;
            transformation.m11 = factor * yAxis.y;
            transformation.m12 = factor * yAxis.z;
            transformation.m20 = zAxis.x;
            transformation.m21 = zAxis.y;
            transformation.m22 = zAxis.z;
            accurateTransformation = new Matrix(accurateXaxis[0], accurateXaxis[1], accurateXaxis[2], BigDecimal.ZERO, accurateYaxis[0].multiply(FACTOR), accurateYaxis[1].multiply(FACTOR),
                    accurateYaxis[2].multiply(FACTOR), BigDecimal.ZERO, accurateZaxis[0], accurateZaxis[1], accurateZaxis[2], BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ONE);

            accurateScale = Matrix.mul(accurateTransformation, accurateScale);
            Matrix4f.mul(transformation, scale, scale);
            Matrix4f.setIdentity(transformation);

            transformation.m30 = position.x;
            transformation.m31 = position.y;
            transformation.m32 = position.z;

            accurateTransformation = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, accuratePosition[0], accuratePosition[1], accuratePosition[2], BigDecimal.ONE);
            accurateScale = Matrix.mul(accurateTransformation, accurateScale);

            Matrix4f.mul(transformation, scale, scale);
            transformation.setIdentity();

            modified = true;
        }

        if (z_Scale) {
            if (l < snap_z_Scale)
                return temp;
            Vector4f vector = new Vector4f(zAxis);
            Vector4f virtpos = new Vector4f(Vector4f.add(vector, position, null));
            Vector4f screenpos = c3d.getPerspectiveCalculator().getScreenCoordinatesFrom3D(virtpos.x, virtpos.y, virtpos.z);
            Vector4f screenpos2 = c3d.getPerspectiveCalculator().getScreenCoordinatesFrom3D(position.x, position.y, position.z);
            Vector2f dA = new Vector2f(screenpos.x - screenpos2.x, screenpos.y - screenpos2.y);

            if (dA.lengthSquared() != 0f)
                dA.normalise();

            float factor;
            BigDecimal FACTOR;
            if (isScaling) {
                factor = oldFactor;
                FACTOR = oldScaleFactor;
            } else {
                if (Math.acos(dA.x * d.x + dA.y * d.y) < Math.PI / 2d) {
                    factor = factor_z_Scale.floatValue();
                    FACTOR = factor_z_Scale;
                } else {
                    factor = 1f / factor_z_Scale.floatValue();
                    FACTOR = BigDecimal.ONE.divide(factor_z_Scale, Threshold.mc);
                }
                isScaling = true;
                oldFactor = factor;
                oldScaleFactor = FACTOR;
            }

            transformation.m30 = -position.x;
            transformation.m31 = -position.y;
            transformation.m32 = -position.z;

            accurateTransformation = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, accuratePosition[0].negate(), accuratePosition[1].negate(), accuratePosition[2].negate(), BigDecimal.ONE);
            accurateScale = Matrix.mul(accurateTransformation, accurateScale);

            Matrix4f.mul(transformation, scale, scale);
            transformation.setIdentity();

            transformation.m00 = xAxis.x;
            transformation.m01 = xAxis.y;
            transformation.m02 = xAxis.z;
            transformation.m10 = yAxis.x;
            transformation.m11 = yAxis.y;
            transformation.m12 = yAxis.z;
            transformation.m20 = zAxis.x;
            transformation.m21 = zAxis.y;
            transformation.m22 = zAxis.z;
            accurateTransformation = new Matrix(accurateXaxis[0], accurateXaxis[1], accurateXaxis[2], BigDecimal.ZERO, accurateYaxis[0], accurateYaxis[1], accurateYaxis[2], BigDecimal.ZERO,
                    accurateZaxis[0], accurateZaxis[1], accurateZaxis[2], BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE);

            accurateTransformation = accurateTransformation.invert();
            transformation = Matrix4f.invert(transformation, null);

            accurateScale = Matrix.mul(accurateTransformation, accurateScale);
            Matrix4f.mul(transformation, scale, scale);
            Matrix4f.setIdentity(transformation);

            transformation.m00 = xAxis.x;
            transformation.m01 = xAxis.y;
            transformation.m02 = xAxis.z;
            transformation.m10 = yAxis.x;
            transformation.m11 = yAxis.y;
            transformation.m12 = yAxis.z;
            transformation.m20 = factor * zAxis.x;
            transformation.m21 = factor * zAxis.y;
            transformation.m22 = factor * zAxis.z;
            accurateTransformation = new Matrix(accurateXaxis[0], accurateXaxis[1], accurateXaxis[2], BigDecimal.ZERO, accurateYaxis[0], accurateYaxis[1], accurateYaxis[2], BigDecimal.ZERO,
                    accurateZaxis[0].multiply(FACTOR), accurateZaxis[1].multiply(FACTOR), accurateZaxis[2].multiply(FACTOR), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ONE);

            accurateScale = Matrix.mul(accurateTransformation, accurateScale);
            Matrix4f.mul(transformation, scale, scale);
            Matrix4f.setIdentity(transformation);

            transformation.m30 = position.x;
            transformation.m31 = position.y;
            transformation.m32 = position.z;

            accurateTransformation = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, accuratePosition[0], accuratePosition[1], accuratePosition[2], BigDecimal.ONE);
            accurateScale = Matrix.mul(accurateTransformation, accurateScale);

            Matrix4f.mul(transformation, scale, scale);
            transformation.setIdentity();

            modified = true;
        }

        if (x_Scale || y_Scale || z_Scale) {
            Matrix4f.mul(scale, result, result);
            accurateResult = Matrix.mul(accurateScale, accurateResult);
        }

        old_mouse_position.set(new_x, new_y);

        xAxis.x = accurateXaxis[0].floatValue();
        xAxis.y = accurateXaxis[1].floatValue();
        xAxis.z = accurateXaxis[2].floatValue();
        yAxis.x = accurateYaxis[0].floatValue();
        yAxis.y = accurateYaxis[1].floatValue();
        yAxis.z = accurateYaxis[2].floatValue();
        zAxis.x = accurateZaxis[0].floatValue();
        zAxis.y = accurateZaxis[1].floatValue();
        zAxis.z = accurateZaxis[2].floatValue();
        xAxis.setW(0f);
        xAxis.normalise();
        xAxis.setW(1f);
        yAxis.setW(0f);
        yAxis.normalise();
        yAxis.setW(1f);
        zAxis.setW(0f);
        zAxis.normalise();
        zAxis.setW(1f);
        return temp;
    }

    public Vector4f transformAtSelect(Vector2f old_mouse_position, int new_x, int new_y, Composite3D c3d) {

        Vector4f temp = new Vector4f(this.position);
        boolean isGlobal = Editor3DWindow.getWindow().getTransformationMode() == ManipulatorScope.GLOBAL;
        if (isGlobal) {
            position = new Vector4f(0f, 0f, 0f, 1f);
            accuratePosition = new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO };
        }

        Vector2f d = new Vector2f(new_x - old_mouse_position.x, new_y - old_mouse_position.y);

        if (d.lengthSquared() == 0f)
            return temp;

        d.normalise();

        Vector4f pos3d1 = c3d.getPerspectiveCalculator().get3DCoordinatesFromScreen(new_x, new_y);
        Vector4f pos3d2 = c3d.getPerspectiveCalculator().get3DCoordinatesFromScreen((int) old_mouse_position.x, (int) old_mouse_position.y);
        Vector4f d3d = Vector4f.sub(pos3d1, pos3d2, null);
        float l = d3d.length();
        BigDecimal L = new BigDecimal(l / 1000f);

        Matrix4f transformation = new Matrix4f();
        Matrix accurateTransformation;
        Matrix4f.setIdentity(transformation);

        if (Math.abs(Math.abs(d.x) - Math.abs(d.y)) < .2f || Math.abs(d.x) > Math.abs(d.y)) {
            Vector4f vector = new Vector4f(xAxis);
            Vector4f virtpos = new Vector4f(Vector4f.add(vector, position, null));
            Vector4f screenpos = c3d.getPerspectiveCalculator().getScreenCoordinatesFrom3D(virtpos.x, virtpos.y, virtpos.z);
            Vector4f screenpos2 = c3d.getPerspectiveCalculator().getScreenCoordinatesFrom3D(position.x, position.y, position.z);
            Vector2f dA = new Vector2f(screenpos.x - screenpos2.x, screenpos.y - screenpos2.y);

            if (dA.lengthSquared() != 0f)
                dA.normalise();

            float factor = Math.max(l - l % (snap_x_Translate.floatValue() * 1000f), snap_x_Translate.floatValue() * 1000f);
            BigDecimal FACTOR = MathHelper.max(L.subtract(L.remainder(snap_x_Translate, Threshold.mc)), snap_x_Translate);

            if (Math.acos(dA.x * d.x + dA.y * d.y) < Math.PI / 2d) {
                transformation.m30 = xAxis.x * factor;
                transformation.m31 = xAxis.y * factor;
                transformation.m32 = xAxis.z * factor;
                accurateTransformation = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, accurateXaxis[0].multiply(FACTOR), accurateXaxis[1].multiply(FACTOR), accurateXaxis[2].multiply(FACTOR),
                        BigDecimal.ONE);
            } else {
                transformation.m30 = -xAxis.x * factor;
                transformation.m31 = -xAxis.y * factor;
                transformation.m32 = -xAxis.z * factor;
                accurateTransformation = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, accurateXaxis[0].multiply(FACTOR).negate(), accurateXaxis[1].multiply(FACTOR).negate(), accurateXaxis[2]
                                .multiply(FACTOR).negate(), BigDecimal.ONE);
            }
            accuratePosition = accurateTransformation.transform(accuratePosition[0], accuratePosition[1], accuratePosition[2]);
            Matrix4f.transform(transformation, position, position);
            accurateResult = Matrix.mul(accurateTransformation, accurateResult);
            Matrix4f.mul(transformation, result, result);
            Matrix4f.setIdentity(transformation);

            modified = true;
        }

        if (Math.abs(Math.abs(d.x) - Math.abs(d.y)) < .2f || Math.abs(d.y) > Math.abs(d.x)) {
            Vector4f vector = new Vector4f(yAxis);
            Vector4f virtpos = new Vector4f(Vector4f.add(vector, position, null));
            Vector4f screenpos = c3d.getPerspectiveCalculator().getScreenCoordinatesFrom3D(virtpos.x, virtpos.y, virtpos.z);
            Vector4f screenpos2 = c3d.getPerspectiveCalculator().getScreenCoordinatesFrom3D(position.x, position.y, position.z);
            Vector2f dA = new Vector2f(screenpos.x - screenpos2.x, screenpos.y - screenpos2.y);

            if (dA.lengthSquared() != 0f)
                dA.normalise();

            float factor = Math.max(l - l % (snap_y_Translate.floatValue() * 1000f), snap_y_Translate.floatValue() * 1000f);
            BigDecimal FACTOR = MathHelper.max(L.subtract(L.remainder(snap_y_Translate, Threshold.mc)), snap_y_Translate);

            if (Math.acos(dA.x * d.x + dA.y * d.y) < Math.PI / 2d) {
                transformation.m30 = yAxis.x * factor;
                transformation.m31 = yAxis.y * factor;
                transformation.m32 = yAxis.z * factor;
                accurateTransformation = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, accurateYaxis[0].multiply(FACTOR), accurateYaxis[1].multiply(FACTOR), accurateYaxis[2].multiply(FACTOR),
                        BigDecimal.ONE);
            } else {
                transformation.m30 = -yAxis.x * factor;
                transformation.m31 = -yAxis.y * factor;
                transformation.m32 = -yAxis.z * factor;
                accurateTransformation = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, accurateYaxis[0].multiply(FACTOR).negate(), accurateYaxis[1].multiply(FACTOR).negate(), accurateYaxis[2]
                                .multiply(FACTOR).negate(), BigDecimal.ONE);
            }
            accuratePosition = accurateTransformation.transform(accuratePosition[0], accuratePosition[1], accuratePosition[2]);
            Matrix4f.transform(transformation, position, position);
            accurateResult = Matrix.mul(accurateTransformation, accurateResult);
            Matrix4f.mul(transformation, result, result);
            Matrix4f.setIdentity(transformation);

            modified = true;
        }

        old_mouse_position.set(new_x, new_y);

        return temp;
    }

    public void reset() {
        accurateXaxis = new BigDecimal[] { BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO };
        accurateYaxis = new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO };
        accurateZaxis = new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE };
        accuratePosition = new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO };
        xAxis = new Vector4f(1f, 0f, 0f, 1f);
        yAxis = new Vector4f(0f, 1f, 0f, 1f);
        zAxis = new Vector4f(0f, 0f, 1f, 1f);
        position = new Vector4f(0f, 0f, 0f, 1f);
    }

    public void positionToOrigin() {
        accuratePosition = new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO };
        position = new Vector4f(0f, 0f, 0f, 1f);
    }

    public void loadIntoMatrix(Matrix4f transformation) {
        transformation.setIdentity();
        transformation.m30 = position.x;
        transformation.m31 = position.y;
        transformation.m32 = position.z;
        transformation.m00 = xAxis.x;
        transformation.m01 = xAxis.y;
        transformation.m02 = xAxis.z;
        transformation.m10 = yAxis.x;
        transformation.m11 = yAxis.y;
        transformation.m12 = yAxis.z;
        transformation.m20 = zAxis.x;
        transformation.m21 = zAxis.y;
        transformation.m22 = zAxis.z;
    }

    public void setAccuratePosition(BigDecimal x, BigDecimal y, BigDecimal z) {
        accuratePosition[0] = x;
        accuratePosition[1] = y;
        accuratePosition[2] = z;
    }

    public void setAccurateXaxis(BigDecimal x, BigDecimal y, BigDecimal z) {
        accurateXaxis[0] = x;
        accurateXaxis[1] = y;
        accurateXaxis[2] = z;
    }

    public void setAccurateYaxis(BigDecimal x, BigDecimal y, BigDecimal z) {
        accurateYaxis[0] = x;
        accurateYaxis[1] = y;
        accurateYaxis[2] = z;
    }

    public void setAccurateZaxis(BigDecimal x, BigDecimal y, BigDecimal z) {
        accurateZaxis[0] = x;
        accurateZaxis[1] = y;
        accurateZaxis[2] = z;
    }

    public BigDecimal[] getAccuratePosition() {
        return accuratePosition;
    }

    public BigDecimal[] getAccurateXaxis() {
        return accurateXaxis;
    }

    public BigDecimal[] getAccurateYaxis() {
        return accurateYaxis;
    }

    public BigDecimal[] getAccurateZaxis() {
        return accurateZaxis;
    }

    public Matrix getAccurateMatrix() {
        Vector3d X = new Vector3d(accurateXaxis[0], accurateXaxis[1], accurateXaxis[2]);
        Vector3d Y = new Vector3d(accurateYaxis[0], accurateYaxis[1], accurateYaxis[2]);
        Vector3d Z = new Vector3d(accurateZaxis[0], accurateZaxis[1], accurateZaxis[2]);
        X.normalise(X);
        Y.normalise(Y);
        Z.normalise(Z);
        return new Matrix(X.X, X.Y, X.Z, BigDecimal.ZERO, Y.X, Y.Y, Y.Z, BigDecimal.ZERO, Z.X, Z.Y, Z.Z, BigDecimal.ZERO, accuratePosition[0], accuratePosition[1], accuratePosition[2], BigDecimal.ONE);
    }

    public Matrix getAccurateRotation() {
        Vector3d X = new Vector3d(accurateXaxis[0], accurateXaxis[1], accurateXaxis[2]);
        Vector3d Y = new Vector3d(accurateYaxis[0], accurateYaxis[1], accurateYaxis[2]);
        Vector3d Z = new Vector3d(accurateZaxis[0], accurateZaxis[1], accurateZaxis[2]);
        X.normalise(X);
        Y.normalise(Y);
        Z.normalise(Z);
        // wrong! -> return new Matrix(X.X, X.Y, X.Z, BigDecimal.ZERO, Y.X, Y.Y, Y.Z, BigDecimal.ZERO, Z.X, Z.Y, Z.Z, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE);
        // Should be transposed:
        return new Matrix(
                X.X, Y.X, Z.X, BigDecimal.ZERO,
                X.Y, Y.Y, Z.Y, BigDecimal.ZERO,
                X.Z, Y.Z, Z.Z, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE);
    }

    public boolean isLocked() {
        return lock;
    }

    public static float getTranslate_size() {
        return translate_size;
    }

    public static void setTranslate_size(float translate_size) {
        Manipulator.translate_size = translate_size;
    }

    public static float getRotate_size() {
        return rotate_size;
    }

    public static void setRotate_size(float rotate_size) {
        Manipulator.rotate_size = rotate_size;
    }

    public static float getRotate_outer_size() {
        return rotate_outer_size;
    }

    public static void setRotate_outer_size(float rotate_outer_size) {
        Manipulator.rotate_outer_size = rotate_outer_size;
    }

    public static float getScale_size() {
        return scale_size;
    }

    public static void setScale_size(float scale_size) {
        Manipulator.scale_size = scale_size;
    }

    public static float getActivationTreshold() {
        return activationTreshold;
    }

    public static void setActivationTreshold(float activationTreshold) {
        Manipulator.activationTreshold = activationTreshold;
    }

    public double getAccurateRotationX() {
        return accurateRotationX;
    }

    public double getAccurateRotationY() {
        return accurateRotationY;
    }

    public double getAccurateRotationZ() {
        return accurateRotationZ;
    }
}
