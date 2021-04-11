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
import org.nschmidt.ldparteditor.enums.ManipulatorAxisMode;
import org.nschmidt.ldparteditor.enums.ManipulatorScope;
import org.nschmidt.ldparteditor.enums.RotationSnap;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.enums.WorkingMode;
import org.nschmidt.ldparteditor.helpers.composite3d.PerspectiveCalculator;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.logger.NLogger;
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
    private final FloatBuffer matrixInv = BufferUtils.createFloatBuffer(16);
    private volatile boolean modified = false;

    private static float translateSize = 140f;
    private static float rotateSize = 100f;
    private static float rotateOuterSize = 120f;
    private static float scaleSize = 60f;

    private Vector4f xAxis = new Vector4f(1f, 0f, 0f, 1f);
    private Vector4f yAxis = new Vector4f(0f, 1f, 0f, 1f);
    private Vector4f zAxis = new Vector4f(0f, 0f, 1f, 1f);

    private BigDecimal[] accurateXaxis = new BigDecimal[] { BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO };
    private BigDecimal[] accurateYaxis = new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO };
    private BigDecimal[] accurateZaxis = new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE };

    private Vector4f xRotateArrow = new Vector4f(0f, 0f, 1f, 1f);
    private Vector4f yRotateArrow = new Vector4f(0f, 0f, 1f, 1f);
    private Vector4f zRotateArrow = new Vector4f(0f, 0f, 1f, 1f);
    private Vector4f vRotateArrow = new Vector4f(0f, 0f, 1f, 1f);

    private static BigDecimal snapXtranslate = new BigDecimal("100"); //$NON-NLS-1$
    private static BigDecimal snapZtranslate = new BigDecimal("100"); //$NON-NLS-1$
    private static BigDecimal snapYtranslate = new BigDecimal("100"); //$NON-NLS-1$

    private static final float SNAP_X_SCALE = 400f;
    private static final float SNAP_Z_SCALE = 400f;
    private static final float SNAP_Y_SCALE = 400f;

    private static BigDecimal factorScale = new BigDecimal("1.1"); //$NON-NLS-1$

    private static BigDecimal snapXrotate = new BigDecimal(Math.PI).divide(new BigDecimal(8), Threshold.MC);
    private static BigDecimal snapYrotate = new BigDecimal(Math.PI).divide(new BigDecimal(8), Threshold.MC);
    private static BigDecimal snapZrotate = new BigDecimal(Math.PI).divide(new BigDecimal(8), Threshold.MC);
    private static BigDecimal snapVrotate = new BigDecimal(Math.PI).divide(new BigDecimal(8), Threshold.MC);


    private static RotationSnap snapXrotateFlag = RotationSnap.COMPLEX;
    private static RotationSnap snapYrotateFlag = RotationSnap.COMPLEX;
    private static RotationSnap snapZrotateFlag = RotationSnap.COMPLEX;
    private static RotationSnap snapVrotateFlag = RotationSnap.COMPLEX;

    private static BigDecimal initialScaleOld = BigDecimal.ZERO;
    private static BigDecimal initialScaleNew = BigDecimal.ZERO;

    public static void setInitialScale(BigDecimal length) {
        initialScaleNew = length;
    }

    public static void setSnap(BigDecimal trans, BigDecimal rot, BigDecimal scale) {

        try {
            rot.intValueExact();
            switch (rot.intValue()) {
            case 90:
                snapXrotateFlag = RotationSnap.DEG90;
                break;
            case 180:
                snapXrotateFlag = RotationSnap.DEG180;
                break;
            case 270:
                snapXrotateFlag = RotationSnap.DEG270;
                break;
            case 360:
                snapXrotateFlag = RotationSnap.DEG360;
                break;
            default:
                snapXrotateFlag = RotationSnap.COMPLEX;
                break;
            }

            switch (rot.intValue()) {
            case 90:
                snapYrotateFlag = RotationSnap.DEG90;
                break;
            case 180:
                snapYrotateFlag = RotationSnap.DEG180;
                break;
            case 270:
                snapYrotateFlag = RotationSnap.DEG270;
                break;
            case 360:
                snapYrotateFlag = RotationSnap.DEG360;
                break;
            default:
                snapYrotateFlag = RotationSnap.COMPLEX;
                break;
            }

            switch (rot.intValue()) {
            case 90:
                snapZrotateFlag = RotationSnap.DEG90;
                break;
            case 180:
                snapZrotateFlag = RotationSnap.DEG180;
                break;
            case 270:
                snapZrotateFlag = RotationSnap.DEG270;
                break;
            case 360:
                snapZrotateFlag = RotationSnap.DEG360;
                break;
            default:
                snapZrotateFlag = RotationSnap.COMPLEX;
                break;
            }

            switch (rot.intValue()) {
            case 90:
                snapVrotateFlag = RotationSnap.DEG90;
                break;
            case 180:
                snapVrotateFlag = RotationSnap.DEG180;
                break;
            case 270:
                snapVrotateFlag = RotationSnap.DEG270;
                break;
            case 360:
                snapVrotateFlag = RotationSnap.DEG360;
                break;
            default:
                snapVrotateFlag = RotationSnap.COMPLEX;
                break;
            }
        } catch (ArithmeticException ae) {
            snapXrotateFlag = RotationSnap.COMPLEX;
            snapYrotateFlag = RotationSnap.COMPLEX;
            snapZrotateFlag = RotationSnap.COMPLEX;
            snapVrotateFlag = RotationSnap.COMPLEX;
        }

        rot = rot.divide(new BigDecimal(180), Threshold.MC).multiply(new BigDecimal(Math.PI));
        snapXtranslate = trans;
        snapZtranslate = trans;
        snapYtranslate = trans;

        factorScale = scale;

        snapXrotate = rot;
        snapYrotate = rot;
        snapZrotate = rot;
        snapVrotate = rot;

    }

    public static BigDecimal[] getSnap() {
        return new BigDecimal[] { snapXtranslate, snapXrotate, factorScale };
    }

    private boolean lock = false;

    private boolean xTranslate;
    private boolean yTranslate;
    private boolean zTranslate;

    private boolean xRotate;
    private boolean yRotate;
    private boolean zRotate;
    private boolean vRotate;

    private boolean xScale;
    private boolean yScale;
    private boolean zScale;

    private boolean xRotatingForwards;
    private boolean xRotatingBackwards;

    private boolean yRotatingForwards;
    private boolean yRotatingBackwards;

    private boolean zRotatingForwards;
    private boolean zRotatingBackwards;

    private boolean vRotatingForwards;
    private boolean vRotatingBackwards;

    private int calmDownCounter = 1;

    private static final float PI16TH = (float) (Math.PI / 16d);

    private Vector4f xRotateStart = new Vector4f(1f, 0f, 0f, 1f);
    private Vector4f yRotateStart = new Vector4f(1f, 0f, 0f, 1f);
    private Vector4f zRotateStart = new Vector4f(1f, 0f, 0f, 1f);
    private Vector4f vRotateStart = new Vector4f(1f, 0f, 0f, 1f);

    private static final float ACTIVATION_TRESHOLD = 200f;

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
        resultinv.store(matrixInv);
        matrixInv.position(0);
        return matrixInv;
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

    public Vector4f getXrotateArrow() {
        return xRotateArrow;
    }

    public Vector4f getYrotateArrow() {
        return yRotateArrow;
    }

    public Vector4f getZrotateArrow() {
        return zRotateArrow;
    }

    public Vector4f getVrotateArrow() {
        return vRotateArrow;
    }

    public Vector4f getPosition() {
        return position;
    }

    public boolean isXtranslate() {
        return xTranslate;
    }

    public void setXtranslate(boolean xTranslate) {
        this.xTranslate = xTranslate;
    }

    public boolean isYtranslate() {
        return yTranslate;
    }

    public void setYtranslate(boolean yTranslate) {
        this.yTranslate = yTranslate;
    }

    public boolean isZtranslate() {
        return zTranslate;
    }

    public void setZtranslate(boolean zTranslate) {
        this.zTranslate = zTranslate;
    }

    public boolean isXrotate() {
        return xRotate;
    }

    public void setXrotate(boolean xRotate) {
        this.xRotate = xRotate;
    }

    public boolean isYrotate() {
        return yRotate;
    }

    public void setYrotate(boolean yRotate) {
        this.yRotate = yRotate;
    }

    public boolean isZrotate() {
        return zRotate;
    }

    public void setZrotate(boolean zRotate) {
        this.zRotate = zRotate;
    }

    public boolean isVrotate() {
        return vRotate;
    }

    public void setVrotate(boolean vRotate) {
        this.vRotate = vRotate;
    }

    public boolean isXscale() {
        return xScale;
    }

    public void setXscale(boolean xScale) {
        this.xScale = xScale;
    }

    public boolean isYscale() {
        return yScale;
    }

    public void setYscale(boolean yScale) {
        this.yScale = yScale;
    }

    public boolean isZscale() {
        return zScale;
    }

    public void setZscale(boolean zScale) {
        this.zScale = zScale;
    }

    public boolean isXrotatingForwards() {
        return xRotatingForwards;
    }

    public void setXrotatingForwards(boolean xRotatingForwards) {
        this.xRotatingForwards = xRotatingForwards;
    }

    public boolean isXrotatingBackwards() {
        return xRotatingBackwards;
    }

    public void setXrotatingBackwards(boolean xRotatingBackwards) {
        this.xRotatingBackwards = xRotatingBackwards;
    }

    public boolean isYrotatingForwards() {
        return yRotatingForwards;
    }

    public void setYrotatingForwards(boolean yRotatingForwards) {
        this.yRotatingForwards = yRotatingForwards;
    }

    public boolean isYrotatingBackwards() {
        return yRotatingBackwards;
    }

    public void setYrotatingBackwards(boolean yRotatingBackwards) {
        this.yRotatingBackwards = yRotatingBackwards;
    }

    public boolean isZrotatingForwards() {
        return zRotatingForwards;
    }

    public void setZrotatingForwards(boolean zRotatingForwards) {
        this.zRotatingForwards = zRotatingForwards;
    }

    public boolean isZrotatingBackwards() {
        return zRotatingBackwards;
    }

    public void setZrotatingBackwards(boolean zRotatingBackwards) {
        this.zRotatingBackwards = zRotatingBackwards;
    }

    public boolean isVrotatingForwards() {
        return vRotatingForwards;
    }

    public void setVrotatingForwards(boolean vRotatingForwards) {
        this.vRotatingForwards = vRotatingForwards;
    }

    public boolean isVrotatingBackwards() {
        return vRotatingBackwards;
    }

    public void setVrotatingBackwards(boolean vRotatingBackwards) {
        this.vRotatingBackwards = vRotatingBackwards;
    }

    public GColour checkManipulatorStatus(float r, float g, float b, int type, Composite3D c3d, float zoom) {
        GColour resultColour = manipulatorStatusHelper(r, g, b, type, c3d, zoom);
        if (View.MANIPULATOR_SELECTED_COLOUR_R[0] == resultColour.getR() && View.MANIPULATOR_SELECTED_COLOUR_G[0] == resultColour.getG() && View.MANIPULATOR_SELECTED_COLOUR_B[0] == resultColour.getB()) {
            switch (Editor3DWindow.getWindow().getWorkingLayer()) {
            case NONE:
            case TEMP_X:
            case TEMP_Y:
            case TEMP_Z:
                switch (type) {
                case X_TRANSLATE:
                case X_SCALE:
                case X_ROTATE:
                    Editor3DWindow.getWindow().setWorkingLayer(ManipulatorAxisMode.TEMP_X);
                    break;
                case Y_TRANSLATE:
                case Y_SCALE:
                case Y_ROTATE:
                    Editor3DWindow.getWindow().setWorkingLayer(ManipulatorAxisMode.TEMP_Y);
                    break;
                case Z_TRANSLATE:
                case Z_SCALE:
                case Z_ROTATE:
                    Editor3DWindow.getWindow().setWorkingLayer(ManipulatorAxisMode.TEMP_Z);
                    break;
                default:
                }
                return resultColour;
            default:
                return new GColour(-1, View.MANIPULATOR_OUTERCIRCLE_COLOUR_R[0], View.MANIPULATOR_OUTERCIRCLE_COLOUR_G[0], View.MANIPULATOR_OUTERCIRCLE_COLOUR_B[0], 1f);
            }
        }
        switch (Editor3DWindow.getWindow().getWorkingLayer()) {
        case X:
            switch (type) {
            case X_TRANSLATE:
            case X_SCALE:
            case X_ROTATE:
                return new GColour(-1, View.MANIPULATOR_SELECTED_COLOUR_R[0], View.MANIPULATOR_SELECTED_COLOUR_G[0], View.MANIPULATOR_SELECTED_COLOUR_B[0], 1f);
            default:
            }
            break;
        case Y:
            switch (type) {
            case Y_TRANSLATE:
            case Y_SCALE:
            case Y_ROTATE:
                return new GColour(-1, View.MANIPULATOR_SELECTED_COLOUR_R[0], View.MANIPULATOR_SELECTED_COLOUR_G[0], View.MANIPULATOR_SELECTED_COLOUR_B[0], 1f);
            default:
            }
            break;
        case Z:
            switch (type) {
            case Z_TRANSLATE:
            case Z_SCALE:
            case Z_ROTATE:
                return new GColour(-1, View.MANIPULATOR_SELECTED_COLOUR_R[0], View.MANIPULATOR_SELECTED_COLOUR_G[0], View.MANIPULATOR_SELECTED_COLOUR_B[0], 1f);
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
                return new GColour(-1, View.MANIPULATOR_SELECTED_COLOUR_R[0], View.MANIPULATOR_SELECTED_COLOUR_G[0], View.MANIPULATOR_SELECTED_COLOUR_B[0], 1f);
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
                return new GColour(-1, View.MANIPULATOR_SELECTED_COLOUR_R[0], View.MANIPULATOR_SELECTED_COLOUR_G[0], View.MANIPULATOR_SELECTED_COLOUR_B[0], 1f);
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
                return new GColour(-1, View.MANIPULATOR_SELECTED_COLOUR_R[0], View.MANIPULATOR_SELECTED_COLOUR_G[0], View.MANIPULATOR_SELECTED_COLOUR_B[0], 1f);
            default:
            }
            break;
        case XYZ:
            return new GColour(-1, View.MANIPULATOR_SELECTED_COLOUR_R[0], View.MANIPULATOR_SELECTED_COLOUR_G[0], View.MANIPULATOR_SELECTED_COLOUR_B[0], 1f);
        case NONE:
        default:
            break;
        }
        return resultColour;
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
        default:
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
                    if (xRotate) {
                        return new GColour(-1, View.MANIPULATOR_SELECTED_COLOUR_R[0], View.MANIPULATOR_SELECTED_COLOUR_G[0], View.MANIPULATOR_SELECTED_COLOUR_B[0], 1f);
                    } else {
                        return new GColour(-1, r, g, b, 1f);
                    }
                }
                xRotate = false;
                vector = new Vector4f(yAxis);
                axis = new Vector3f(xAxis.x, xAxis.y, xAxis.z);
                break;
            case Y_ROTATE:
                if (lock) {
                    if (yRotate) {
                        return new GColour(-1, View.MANIPULATOR_SELECTED_COLOUR_R[0], View.MANIPULATOR_SELECTED_COLOUR_G[0], View.MANIPULATOR_SELECTED_COLOUR_B[0], 1f);
                    } else {
                        return new GColour(-1, r, g, b, 1f);
                    }
                }
                yRotate = false;
                vector = new Vector4f(zAxis);
                axis = new Vector3f(yAxis.x, yAxis.y, yAxis.z);
                break;
            case Z_ROTATE:
                if (lock) {
                    if (zRotate) {
                        return new GColour(-1, View.MANIPULATOR_SELECTED_COLOUR_R[0], View.MANIPULATOR_SELECTED_COLOUR_G[0], View.MANIPULATOR_SELECTED_COLOUR_B[0], 1f);
                    } else {
                        return new GColour(-1, r, g, b, 1f);
                    }
                }
                zRotate = false;
                vector = new Vector4f(xAxis);
                axis = new Vector3f(zAxis.x, zAxis.y, zAxis.z);
                break;
            case V_ROTATE:
                if (lock) {
                    if (vRotate) {
                        return new GColour(-1, View.MANIPULATOR_SELECTED_COLOUR_R[0], View.MANIPULATOR_SELECTED_COLOUR_G[0], View.MANIPULATOR_SELECTED_COLOUR_B[0], 1f);
                    } else {
                        return new GColour(-1, r, g, b, 1f);
                    }
                }
                vRotate = false;
                vector = new Vector4f(gen[1].x, gen[1].y, gen[1].z, 1f);
                axis = new Vector3f(gen[2].x, gen[2].y, gen[2].z);
                break;
            default:
                break;
            }
            if (type == V_ROTATE) {
                vector.scale(rotateOuterSize / zoom / 1000f);
            } else {
                vector.scale(rotateSize / zoom / 1000f);
            }

            float sumangle = 0f;
            float angle = (float) (2d * Math.asin(ACTIVATION_TRESHOLD / (20d * rotateSize)));

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
                    if (dists < ACTIVATION_TRESHOLD) {
                        switch (type) {
                        case X_ROTATE:
                            xRotateStart.set(vector);
                            xRotate = true;
                            break;
                        case Y_ROTATE:
                            yRotateStart.set(vector);
                            yRotate = true;
                            break;
                        case Z_ROTATE:
                            zRotateStart.set(vector);
                            zRotate = true;
                            break;
                        case V_ROTATE:
                            vRotateStart.set(vector);
                            vRotate = true;
                            break;
                        default:
                            break;
                        }
                        return new GColour(-1, View.MANIPULATOR_SELECTED_COLOUR_R[0], View.MANIPULATOR_SELECTED_COLOUR_G[0], View.MANIPULATOR_SELECTED_COLOUR_B[0], 1f);
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
                    if (xScale) {
                        return new GColour(-1, View.MANIPULATOR_SELECTED_COLOUR_R[0], View.MANIPULATOR_SELECTED_COLOUR_G[0], View.MANIPULATOR_SELECTED_COLOUR_B[0], 1f);
                    } else {
                        return new GColour(-1, r, g, b, 1f);
                    }
                }
                if (!(c3d.getKeys().isCtrlPressed() || (Cocoa.IS_COCOA && c3d.getKeys().isCmdPressed())))
                    xScale = false;
            case Y_SCALE:
                if (lock) {
                    if (yScale) {
                        return new GColour(-1, View.MANIPULATOR_SELECTED_COLOUR_R[0], View.MANIPULATOR_SELECTED_COLOUR_G[0], View.MANIPULATOR_SELECTED_COLOUR_B[0], 1f);
                    } else {
                        return new GColour(-1, r, g, b, 1f);
                    }
                }
                if (!(c3d.getKeys().isCtrlPressed() || (Cocoa.IS_COCOA && c3d.getKeys().isCmdPressed())))
                    yScale = false;
            case Z_SCALE:
                if (lock) {
                    if (zScale) {
                        return new GColour(-1, View.MANIPULATOR_SELECTED_COLOUR_R[0], View.MANIPULATOR_SELECTED_COLOUR_G[0], View.MANIPULATOR_SELECTED_COLOUR_B[0], 1f);
                    } else {
                        return new GColour(-1, r, g, b, 1f);
                    }
                }
                if (!(c3d.getKeys().isCtrlPressed() || (Cocoa.IS_COCOA && c3d.getKeys().isCmdPressed())))
                    zScale = false;
                size = scaleSize;
                break;
            case X_TRANSLATE:
                if (lock) {
                    if (xTranslate) {
                        return new GColour(-1, View.MANIPULATOR_SELECTED_COLOUR_R[0], View.MANIPULATOR_SELECTED_COLOUR_G[0], View.MANIPULATOR_SELECTED_COLOUR_B[0], 1f);
                    } else {
                        return new GColour(-1, r, g, b, 1f);
                    }
                }
                if (!(c3d.getKeys().isCtrlPressed() || (Cocoa.IS_COCOA && c3d.getKeys().isCmdPressed())))
                    xTranslate = false;
            case Y_TRANSLATE:
                if (lock) {
                    if (yTranslate) {
                        return new GColour(-1, View.MANIPULATOR_SELECTED_COLOUR_R[0], View.MANIPULATOR_SELECTED_COLOUR_G[0], View.MANIPULATOR_SELECTED_COLOUR_B[0], 1f);
                    } else {
                        return new GColour(-1, r, g, b, 1f);
                    }
                }
                if (!(c3d.getKeys().isCtrlPressed() || (Cocoa.IS_COCOA && c3d.getKeys().isCmdPressed())))
                    yTranslate = false;
            case Z_TRANSLATE:
                if (lock) {
                    if (zTranslate) {
                        return new GColour(-1, View.MANIPULATOR_SELECTED_COLOUR_R[0], View.MANIPULATOR_SELECTED_COLOUR_G[0], View.MANIPULATOR_SELECTED_COLOUR_B[0], 1f);
                    } else {
                        return new GColour(-1, r, g, b, 1f);
                    }
                }
                if (!(c3d.getKeys().isCtrlPressed() || (Cocoa.IS_COCOA && c3d.getKeys().isCmdPressed())))
                    zTranslate = false;
                size = translateSize;
                break;
            case X_ROTATE_ARROW:
                size = rotateSize;
                break;
            case Y_ROTATE_ARROW:
                size = rotateSize;
                break;
            case Z_ROTATE_ARROW:
                size = rotateSize;
                break;
            case V_ROTATE_ARROW:
                size = rotateOuterSize;
                break;
            default:
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
                vector = new Vector4f(xRotateStart);
                vector.setW(0f);
                vector.normalise();
                vector.setW(1f);
                xRotateArrow.set(vector);
                break;
            case Y_ROTATE_ARROW:
                vector = new Vector4f(yRotateStart);
                vector.setW(0f);
                vector.normalise();
                vector.setW(1f);
                yRotateArrow.set(vector);
                break;
            case Z_ROTATE_ARROW:
                vector = new Vector4f(zRotateStart);
                vector.setW(0f);
                vector.normalise();
                vector.setW(1f);
                zRotateArrow.set(vector);
                break;
            case V_ROTATE_ARROW:
                vector = new Vector4f(vRotateStart);
                vector.setW(0f);
                vector.normalise();
                vector.setW(1f);
                vRotateArrow.set(vector);
                break;
            default:
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
                float dists3;
                int position2 = 0;
                {
                    Vector4f vector2 = new Vector4f(vector);
                    vector2.scale(.25f);
                    Matrix4f m = new Matrix4f();
                    Matrix4f.setIdentity(m);
                    switch (type) {
                    case X_ROTATE_ARROW:
                        m.rotate(Math.max(snapXrotate.floatValue(), PI16TH), new Vector3f(axis.x, axis.y, axis.z));
                        break;
                    case Y_ROTATE_ARROW:
                        m.rotate(Math.max(snapYrotate.floatValue(), PI16TH), new Vector3f(axis.x, axis.y, axis.z));
                        break;
                    case Z_ROTATE_ARROW:
                        m.rotate(Math.max(snapZrotate.floatValue(), PI16TH), new Vector3f(axis.x, axis.y, axis.z));
                        break;
                    case V_ROTATE_ARROW:
                        m.rotate(Math.max(snapVrotate.floatValue(), PI16TH), new Vector3f(axis.x, axis.y, axis.z));
                        break;
                    default:
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
                        m.rotate(-Math.max(snapXrotate.floatValue(), PI16TH), new Vector3f(axis.x, axis.y, axis.z));
                        break;
                    case Y_ROTATE_ARROW:
                        m.rotate(-Math.max(snapYrotate.floatValue(), PI16TH), new Vector3f(axis.x, axis.y, axis.z));
                        break;
                    case Z_ROTATE_ARROW:
                        m.rotate(-Math.max(snapZrotate.floatValue(), PI16TH), new Vector3f(axis.x, axis.y, axis.z));
                        break;
                    case V_ROTATE_ARROW:
                        m.rotate(-Math.max(snapVrotate.floatValue(), PI16TH), new Vector3f(axis.x, axis.y, axis.z));
                        break;
                    default:
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
                if (dists < ACTIVATION_TRESHOLD || position2 != 0) {
                    switch (type) {
                    case X_ROTATE_ARROW:
                        if (position2 < 0) {
                            if (calmDownCounter > 0) {
                                calmDownCounter -= 1;
                                break;
                            } else {
                                calmDownCounter = -2;
                                xRotatingForwards = true;
                            }
                        }else {
                            if (calmDownCounter < 0) {
                                calmDownCounter += 1;
                                break;
                            } else {
                                calmDownCounter = 2;
                                xRotatingBackwards = true;
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
                                yRotatingForwards = true;
                            }
                        }else {
                            if (calmDownCounter < 0) {
                                calmDownCounter += 1;
                                break;
                            } else {
                                calmDownCounter = 2;
                                yRotatingBackwards = true;
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
                                zRotatingForwards = true;
                            }
                        }else {
                            if (calmDownCounter < 0) {
                                calmDownCounter += 1;
                                break;
                            } else {
                                calmDownCounter = 2;
                                zRotatingBackwards = true;
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
                                vRotatingForwards = true;
                            }
                        }else {
                            if (calmDownCounter < 0) {
                                calmDownCounter += 1;
                                break;
                            } else {
                                calmDownCounter = 2;
                                vRotatingBackwards = true;
                            }
                        }
                        break;
                    default:
                        break;
                    }
                    return new GColour(-1, View.MANIPULATOR_SELECTED_COLOUR_R[0], View.MANIPULATOR_SELECTED_COLOUR_G[0], View.MANIPULATOR_SELECTED_COLOUR_B[0], 1f);
                } else {
                    return new GColour(-1, r, g, b, 1f);
                }
            } else {
                Vector4f virtpos = new Vector4f(Vector4f.add(vector, position, null));
                Vector4f screenpos = c3d.getPerspectiveCalculator().getScreenCoordinatesFrom3D(virtpos.x, virtpos.y, virtpos.z);
                float dists = (float) (Math.pow(c3d.getMousePosition().x - screenpos.x, 2) + Math.pow(c3d.getMousePosition().y - screenpos.y, 2));
                if (dists < ACTIVATION_TRESHOLD) {
                    switch (type) {
                    case X_TRANSLATE:
                        xTranslate = true;
                        break;
                    case X_SCALE:
                        xScale = true;
                        break;
                    case Y_TRANSLATE:
                        yTranslate = true;
                        break;
                    case Y_SCALE:
                        yScale = true;
                        break;
                    case Z_TRANSLATE:
                        zTranslate = true;
                        break;
                    case Z_SCALE:
                        zScale = true;
                        break;
                    default:
                        break;
                    }
                    return new GColour(-1, View.MANIPULATOR_SELECTED_COLOUR_R[0], View.MANIPULATOR_SELECTED_COLOUR_G[0], View.MANIPULATOR_SELECTED_COLOUR_B[0], 1f);
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
        initialScaleOld = initialScaleNew;
        c3d.getLockableDatFileReference().getVertexManager().backupHideShowState();
        modified = false;
        Matrix4f.setIdentity(result);
        Matrix4f.setIdentity(scale);
        accurateResult = View.ACCURATE_ID;
        accurateScale = View.ACCURATE_ID;
        accurateRotationX = 0.0;
        accurateRotationY = 0.0;
        accurateRotationZ = 0.0;
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
        xTranslate = true;
        yTranslate = true;
    }

    public void applyTranslation(Composite3D c3d) {
        if (modified) {
            c3d.getLockableDatFileReference().getVertexManager().transformSelection(accurateResult, null, Editor3DWindow.getWindow().isMovingAdjacentData());
            initialScaleOld = initialScaleNew;
            Editor3DWindow.getWindow().updateInitialScale(initialScaleNew, factorScale, false);
        }
        resetTranslation();
    }

    public void applyTranslationAtSelect(Composite3D c3d) {
        if (modified) {
            c3d.getLockableDatFileReference().getVertexManager().transformSelection(accurateResult, null, Editor3DWindow.getWindow().isMovingAdjacentData());
        }
        resetTranslation();
    }

    public void resetTranslation() {
        accurateResult = View.ACCURATE_ID;
        accurateScale = View.ACCURATE_ID;
        accurateRotationX = 0.0;
        accurateRotationY = 0.0;
        accurateRotationZ = 0.0;
        Matrix4f.setIdentity(result);
        Matrix4f.setIdentity(scale);
        xTranslate = false;
        yTranslate = false;
        zTranslate = false;
        xRotate = false;
        yRotate = false;
        zRotate = false;
        xScale = false;
        yScale = false;
        zScale = false;

        xRotatingForwards = false;
        xRotatingBackwards = false;
        yRotatingForwards = false;
        yRotatingBackwards = false;
        zRotatingForwards = false;
        zRotatingBackwards = false;
        modified = false;
    }

    public Vector4f transform(Vector2f oldMousePosition, int newX, int newY, Composite3D c3d) {

        Vector4f temp = new Vector4f(this.position);
        boolean isGlobal = Editor3DWindow.getWindow().getTransformationMode() == ManipulatorScope.GLOBAL;
        if (isGlobal) {
            position = new Vector4f(0f, 0f, 0f, 1f);
            accuratePosition = new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO };
        }

        Vector2f d = new Vector2f(newX - oldMousePosition.x, newY - oldMousePosition.y);

        if (d.lengthSquared() == 0f)
            return temp;

        d.normalise();

        Vector4f pos3d1 = c3d.getPerspectiveCalculator().get3DCoordinatesFromScreen(newX, newY);
        Vector4f pos3d2 = c3d.getPerspectiveCalculator().get3DCoordinatesFromScreen((int) oldMousePosition.x, (int) oldMousePosition.y);
        Vector4f d3d = Vector4f.sub(pos3d1, pos3d2, null);
        float l = d3d.length();
        BigDecimal lPrecise = new BigDecimal(l / 1000f);

        //        NLogger.debug(getClass(), "Transforming... dx= " + d.x + " dy= " + d.y + " length= " + l); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        Matrix4f transformation = new Matrix4f();
        Matrix accurateTransformation;
        Matrix4f.setIdentity(transformation);

        if (xTranslate) {
            if (l < snapXtranslate.floatValue() * 1000f)
                return temp;
            Vector4f vector = new Vector4f(xAxis);
            Vector4f virtpos = new Vector4f(Vector4f.add(vector, position, null));
            Vector4f screenpos = c3d.getPerspectiveCalculator().getScreenCoordinatesFrom3D(virtpos.x, virtpos.y, virtpos.z);
            Vector4f screenpos2 = c3d.getPerspectiveCalculator().getScreenCoordinatesFrom3D(position.x, position.y, position.z);
            Vector2f dA = new Vector2f(screenpos.x - screenpos2.x, screenpos.y - screenpos2.y);

            if (dA.lengthSquared() != 0f)
                dA.normalise();

            float factor = Math.max(l - l % (snapXtranslate.floatValue() * 1000f), snapXtranslate.floatValue() * 1000f);
            BigDecimal factorPrecise = MathHelper.max(lPrecise.subtract(lPrecise.remainder(snapXtranslate, Threshold.MC)), snapXtranslate);

            if (Math.acos(dA.x * d.x + dA.y * d.y) < Math.PI / 2d) {
                transformation.m30 = xAxis.x * factor;
                transformation.m31 = xAxis.y * factor;
                transformation.m32 = xAxis.z * factor;
                accurateTransformation = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, accurateXaxis[0].multiply(factorPrecise), accurateXaxis[1].multiply(factorPrecise), accurateXaxis[2].multiply(factorPrecise),
                        BigDecimal.ONE);
            } else {
                transformation.m30 = -xAxis.x * factor;
                transformation.m31 = -xAxis.y * factor;
                transformation.m32 = -xAxis.z * factor;
                accurateTransformation = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, accurateXaxis[0].multiply(factorPrecise).negate(), accurateXaxis[1].multiply(factorPrecise).negate(), accurateXaxis[2]
                                .multiply(factorPrecise).negate(), BigDecimal.ONE);
            }
            accuratePosition = accurateTransformation.transform(accuratePosition[0], accuratePosition[1], accuratePosition[2]);
            Matrix4f.transform(transformation, position, position);
            accurateResult = Matrix.mul(accurateTransformation, accurateResult);
            Matrix4f.mul(transformation, result, result);
            Matrix4f.setIdentity(transformation);

            modified = true;
        }

        if (yTranslate) {
            if (l < snapZtranslate.floatValue() * 1000f)
                return temp;
            Vector4f vector = new Vector4f(yAxis);
            Vector4f virtpos = new Vector4f(Vector4f.add(vector, position, null));
            Vector4f screenpos = c3d.getPerspectiveCalculator().getScreenCoordinatesFrom3D(virtpos.x, virtpos.y, virtpos.z);
            Vector4f screenpos2 = c3d.getPerspectiveCalculator().getScreenCoordinatesFrom3D(position.x, position.y, position.z);
            Vector2f dA = new Vector2f(screenpos.x - screenpos2.x, screenpos.y - screenpos2.y);

            if (dA.lengthSquared() != 0f)
                dA.normalise();

            float factor = Math.max(l - l % (snapZtranslate.floatValue() * 1000f), snapZtranslate.floatValue() * 1000f);
            BigDecimal factorPrecise = MathHelper.max(lPrecise.subtract(lPrecise.remainder(snapZtranslate, Threshold.MC)), snapZtranslate);

            if (Math.acos(dA.x * d.x + dA.y * d.y) < Math.PI / 2d) {
                transformation.m30 = yAxis.x * factor;
                transformation.m31 = yAxis.y * factor;
                transformation.m32 = yAxis.z * factor;
                accurateTransformation = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, accurateYaxis[0].multiply(factorPrecise), accurateYaxis[1].multiply(factorPrecise), accurateYaxis[2].multiply(factorPrecise),
                        BigDecimal.ONE);
            } else {
                transformation.m30 = -yAxis.x * factor;
                transformation.m31 = -yAxis.y * factor;
                transformation.m32 = -yAxis.z * factor;
                accurateTransformation = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, accurateYaxis[0].multiply(factorPrecise).negate(), accurateYaxis[1].multiply(factorPrecise).negate(), accurateYaxis[2]
                                .multiply(factorPrecise).negate(), BigDecimal.ONE);
            }
            accuratePosition = accurateTransformation.transform(accuratePosition[0], accuratePosition[1], accuratePosition[2]);
            Matrix4f.transform(transformation, position, position);
            accurateResult = Matrix.mul(accurateTransformation, accurateResult);
            Matrix4f.mul(transformation, result, result);
            Matrix4f.setIdentity(transformation);

            modified = true;
        }

        if (zTranslate) {
            if (l < snapYtranslate.floatValue() * 1000f)
                return temp;
            Vector4f vector = new Vector4f(zAxis);
            Vector4f virtpos = new Vector4f(Vector4f.add(vector, position, null));
            Vector4f screenpos = c3d.getPerspectiveCalculator().getScreenCoordinatesFrom3D(virtpos.x, virtpos.y, virtpos.z);
            Vector4f screenpos2 = c3d.getPerspectiveCalculator().getScreenCoordinatesFrom3D(position.x, position.y, position.z);
            Vector2f dA = new Vector2f(screenpos.x - screenpos2.x, screenpos.y - screenpos2.y);

            if (dA.lengthSquared() != 0f)
                dA.normalise();

            float factor = Math.max(l - l % (snapYtranslate.floatValue() * 1000f), snapYtranslate.floatValue() * 1000f);
            BigDecimal factorPrecise = MathHelper.max(lPrecise.subtract(lPrecise.remainder(snapYtranslate, Threshold.MC)), snapYtranslate);

            if (Math.acos(dA.x * d.x + dA.y * d.y) < Math.PI / 2d) {
                transformation.m30 = zAxis.x * factor;
                transformation.m31 = zAxis.y * factor;
                transformation.m32 = zAxis.z * factor;
                accurateTransformation = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, accurateZaxis[0].multiply(factorPrecise), accurateZaxis[1].multiply(factorPrecise), accurateZaxis[2].multiply(factorPrecise),
                        BigDecimal.ONE);
            } else {
                transformation.m30 = -zAxis.x * factor;
                transformation.m31 = -zAxis.y * factor;
                transformation.m32 = -zAxis.z * factor;
                accurateTransformation = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, accurateZaxis[0].multiply(factorPrecise).negate(), accurateZaxis[1].multiply(factorPrecise).negate(), accurateZaxis[2]
                                .multiply(factorPrecise).negate(), BigDecimal.ONE);
            }
            accuratePosition = accurateTransformation.transform(accuratePosition[0], accuratePosition[1], accuratePosition[2]);
            Matrix4f.transform(transformation, position, position);
            accurateResult = Matrix.mul(accurateTransformation, accurateResult);
            Matrix4f.mul(transformation, result, result);
            Matrix4f.setIdentity(transformation);

            modified = true;
        }

        if (xRotate || yRotate || zRotate || vRotate) {
            Matrix4f forward = new Matrix4f();
            Matrix4f.setIdentity(forward);
            Matrix4f.translate(new Vector3f(-position.x, -position.y, -position.z), forward, forward);
            Matrix4f.mul(forward, result, result);
            accurateResult = Matrix.mul(View.ACCURATE_ID.translate(new BigDecimal[] { accuratePosition[0].negate(), accuratePosition[1].negate(), accuratePosition[2].negate() }), accurateResult);
        }

        if (xRotate) {
            while (true) {
                if (xRotatingForwards) {
                    transformation.rotate(snapXrotate.floatValue(), new Vector3f(xAxis.x, xAxis.y, xAxis.z));
                    accurateTransformation = View.ACCURATE_ID.rotate(snapXrotate, snapXrotateFlag, accurateXaxis);
                    accurateRotationX = accurateRotationX + snapXrotate.doubleValue();
                    Vector4f vector = new Vector4f(xRotateArrow);
                    Matrix4f m = new Matrix4f();
                    Matrix4f.setIdentity(m);
                    m.rotate(Math.max(snapXrotate.floatValue(), PI16TH), new Vector3f(xAxis.x, xAxis.y, xAxis.z));
                    Matrix4f.transform(m, vector, vector);
                    vector.setW(0f);
                    vector.normalise();
                    vector.setW(1f);
                    xRotateArrow.set(vector);
                    xRotateStart.set(xRotateArrow);
                } else if (xRotatingBackwards) {
                    transformation.rotate(-snapXrotate.floatValue(), new Vector3f(xAxis.x, xAxis.y, xAxis.z));
                    accurateTransformation = View.ACCURATE_ID.rotate(snapXrotate.negate(), snapXrotateFlag, accurateXaxis);
                    accurateRotationX = accurateRotationX - snapXrotate.doubleValue();
                    Vector4f vector = new Vector4f(xRotateArrow);
                    Matrix4f m = new Matrix4f();
                    Matrix4f.setIdentity(m);
                    m.rotate(-Math.max(snapXrotate.floatValue(), PI16TH), new Vector3f(xAxis.x, xAxis.y, xAxis.z));
                    Matrix4f.transform(m, vector, vector);
                    vector.setW(0f);
                    vector.normalise();
                    vector.setW(1f);
                    xRotateArrow.set(vector);
                    xRotateStart.set(xRotateArrow);
                } else {
                    break;
                }
                xRotatingForwards = false;
                xRotatingBackwards = false;
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

        if (yRotate) {
            while (true) {
                if (yRotatingForwards) {
                    transformation.rotate(snapYrotate.floatValue(), new Vector3f(yAxis.x, yAxis.y, yAxis.z));
                    accurateTransformation = View.ACCURATE_ID.rotate(snapYrotate, snapYrotateFlag, accurateYaxis);
                    accurateRotationY = accurateRotationY + snapYrotate.doubleValue();
                    Vector4f vector = new Vector4f(yRotateArrow);
                    Matrix4f m = new Matrix4f();
                    Matrix4f.setIdentity(m);
                    m.rotate(Math.max(snapYrotate.floatValue(), PI16TH), new Vector3f(yAxis.x, yAxis.y, yAxis.z));
                    Matrix4f.transform(m, vector, vector);
                    vector.setW(0f);
                    vector.normalise();
                    vector.setW(1f);
                    yRotateArrow.set(vector);
                    yRotateStart.set(yRotateArrow);
                } else if (yRotatingBackwards) {
                    transformation.rotate(-snapYrotate.floatValue(), new Vector3f(yAxis.x, yAxis.y, yAxis.z));
                    accurateTransformation = View.ACCURATE_ID.rotate(snapYrotate.negate(), snapYrotateFlag, accurateYaxis);
                    accurateRotationY = accurateRotationY - snapYrotate.doubleValue();
                    Vector4f vector = new Vector4f(yRotateArrow);
                    Matrix4f m = new Matrix4f();
                    Matrix4f.setIdentity(m);
                    m.rotate(-Math.max(snapYrotate.floatValue(), PI16TH), new Vector3f(yAxis.x, yAxis.y, yAxis.z));
                    Matrix4f.transform(m, vector, vector);
                    vector.setW(0f);
                    vector.normalise();
                    vector.setW(1f);
                    yRotateArrow.set(vector);
                    yRotateStart.set(yRotateArrow);
                } else {
                    break;
                }
                yRotatingForwards = false;
                yRotatingBackwards = false;
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

        if (zRotate) {
            while (true) {
                if (zRotatingForwards) {
                    transformation.rotate(snapZrotate.floatValue(), new Vector3f(zAxis.x, zAxis.y, zAxis.z));
                    accurateTransformation = View.ACCURATE_ID.rotate(snapZrotate, snapZrotateFlag, accurateZaxis);
                    accurateRotationZ = accurateRotationZ + snapZrotate.doubleValue();
                    Vector4f vector = new Vector4f(zRotateArrow);
                    Matrix4f m = new Matrix4f();
                    Matrix4f.setIdentity(m);
                    m.rotate(Math.max(snapZrotate.floatValue(), PI16TH), new Vector3f(zAxis.x, zAxis.y, zAxis.z));
                    Matrix4f.transform(m, vector, vector);
                    vector.setW(0f);
                    vector.normalise();
                    vector.setW(1f);
                    zRotateArrow.set(vector);
                    zRotateStart.set(zRotateArrow);
                } else if (zRotatingBackwards) {
                    transformation.rotate(-snapZrotate.floatValue(), new Vector3f(zAxis.x, zAxis.y, zAxis.z));
                    accurateTransformation = View.ACCURATE_ID.rotate(snapZrotate.negate(), snapZrotateFlag, accurateZaxis);
                    accurateRotationZ = accurateRotationZ - snapZrotate.doubleValue();
                    Vector4f vector = new Vector4f(zRotateArrow);
                    Matrix4f m = new Matrix4f();
                    Matrix4f.setIdentity(m);
                    m.rotate(-Math.max(snapZrotate.floatValue(), PI16TH), new Vector3f(zAxis.x, zAxis.y, zAxis.z));
                    Matrix4f.transform(m, vector, vector);
                    vector.setW(0f);
                    vector.normalise();
                    vector.setW(1f);
                    zRotateArrow.set(vector);
                    zRotateStart.set(zRotateArrow);
                } else {
                    break;
                }
                zRotatingForwards = false;
                zRotatingBackwards = false;
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

        if (vRotate) {
            while (true) {
                Vector4f[] gen = c3d.getGenerator();
                if (vRotatingForwards) {
                    transformation.rotate(snapVrotate.floatValue(), new Vector3f(gen[2].x, gen[2].y, gen[2].z));
                    accurateTransformation = View.ACCURATE_ID.rotate(snapVrotate, snapVrotateFlag, new BigDecimal[] { new BigDecimal(gen[2].x), new BigDecimal(gen[2].y), new BigDecimal(gen[2].z) });
                    Vector4f vector = new Vector4f(vRotateArrow);
                    Matrix4f m = new Matrix4f();
                    Matrix4f.setIdentity(m);
                    m.rotate(Math.max(snapVrotate.floatValue(), PI16TH), new Vector3f(gen[2].x, gen[2].y, gen[2].z));
                    Matrix4f.transform(m, vector, vector);
                    vector.setW(0f);
                    vector.normalise();
                    vector.setW(1f);
                    vRotateArrow.set(vector);
                    vRotateStart.set(vRotateArrow);
                } else if (vRotatingBackwards) {
                    transformation.rotate(-snapVrotate.floatValue(), new Vector3f(gen[2].x, gen[2].y, gen[2].z));
                    accurateTransformation = View.ACCURATE_ID.rotate(snapVrotate.negate(), snapVrotateFlag, new BigDecimal[] { new BigDecimal(gen[2].x), new BigDecimal(gen[2].y), new BigDecimal(gen[2].z) });
                    Vector4f vector = new Vector4f(vRotateArrow);
                    Matrix4f m = new Matrix4f();
                    Matrix4f.setIdentity(m);
                    m.rotate(-Math.max(snapVrotate.floatValue(), PI16TH), new Vector3f(gen[2].x, gen[2].y, gen[2].z));
                    Matrix4f.transform(m, vector, vector);
                    vector.setW(0f);
                    vector.normalise();
                    vector.setW(1f);
                    vRotateArrow.set(vector);
                    vRotateStart.set(vRotateArrow);
                } else {
                    break;
                }
                vRotatingForwards = false;
                vRotatingBackwards = false;
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

        if (xRotate || yRotate || zRotate || vRotate) {
            Matrix4f backward = new Matrix4f();
            Matrix4f.setIdentity(backward);
            Matrix4f.translate(new Vector3f(position.x, position.y, position.z), backward, backward);
            Matrix4f.mul(backward, result, result);
            accurateResult = Matrix.mul(View.ACCURATE_ID.translate(new BigDecimal[] { accuratePosition[0], accuratePosition[1], accuratePosition[2] }), accurateResult);
        }

        if (xScale || yScale || zScale) {
            Matrix4f.setIdentity(scale);
            accurateScale = View.ACCURATE_ID;
        }

        boolean isScaling = false;
        BigDecimal oldScaleFactor = BigDecimal.ONE;
        float oldFactor = 1f;
        boolean newScaleFactor = initialScaleOld.compareTo(BigDecimal.ZERO) > 0;
        if (newScaleFactor) {
            initialScaleOld = initialScaleNew;
        }

        boolean scaleThreshold = newScaleFactor && snapXtranslate.add(snapXtranslate.divide(new BigDecimal(1000))).compareTo(initialScaleOld) < 0;

        if (xScale) {
            if (l < SNAP_X_SCALE)
                return temp;
            Vector4f vector = new Vector4f(xAxis);
            Vector4f virtpos = new Vector4f(Vector4f.add(vector, position, null));
            Vector4f screenpos = c3d.getPerspectiveCalculator().getScreenCoordinatesFrom3D(virtpos.x, virtpos.y, virtpos.z);
            Vector4f screenpos2 = c3d.getPerspectiveCalculator().getScreenCoordinatesFrom3D(position.x, position.y, position.z);
            Vector2f dA = new Vector2f(screenpos.x - screenpos2.x, screenpos.y - screenpos2.y);

            if (dA.lengthSquared() != 0f)
                dA.normalise();

            float factor;
            BigDecimal factorPrecise;
            if (newScaleFactor) {
                if (Math.acos(dA.x * d.x + dA.y * d.y) < Math.PI / 2d) {
                    initialScaleNew = initialScaleOld.add(snapXtranslate, Threshold.MC);
                    factorScale = initialScaleNew.divide(initialScaleOld, Threshold.MC);
                } else if (scaleThreshold) {
                    initialScaleNew = initialScaleOld.subtract(snapXtranslate, Threshold.MC);
                    factorScale = initialScaleNew.divide(initialScaleOld, Threshold.MC);
                } else {
                    initialScaleNew = initialScaleOld;
                    factorScale = BigDecimal.ONE;
                }
                factor = factorScale.floatValue();
                factorPrecise = factorScale;
            } else {
                if (Math.acos(dA.x * d.x + dA.y * d.y) < Math.PI / 2d) {
                    factor = factorScale.floatValue();
                    factorPrecise = factorScale;
                } else {
                    factor = 1f / factorScale.floatValue();
                    factorPrecise = BigDecimal.ONE.divide(factorScale, Threshold.MC);
                }
            }
            isScaling = true;
            oldFactor = factor;
            oldScaleFactor = factorPrecise;

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
            accurateTransformation = new Matrix(accurateXaxis[0].multiply(factorPrecise), accurateXaxis[1].multiply(factorPrecise), accurateXaxis[2].multiply(factorPrecise), BigDecimal.ZERO, accurateYaxis[0],
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

        if (yScale) {
            if (l < SNAP_Z_SCALE)
                return temp;
            Vector4f vector = new Vector4f(yAxis);
            Vector4f virtpos = new Vector4f(Vector4f.add(vector, position, null));
            Vector4f screenpos = c3d.getPerspectiveCalculator().getScreenCoordinatesFrom3D(virtpos.x, virtpos.y, virtpos.z);
            Vector4f screenpos2 = c3d.getPerspectiveCalculator().getScreenCoordinatesFrom3D(position.x, position.y, position.z);
            Vector2f dA = new Vector2f(screenpos.x - screenpos2.x, screenpos.y - screenpos2.y);

            if (dA.lengthSquared() != 0f)
                dA.normalise();

            float factor;
            BigDecimal factorPrecise;
            if (isScaling) {
                factor = oldFactor;
                factorPrecise = oldScaleFactor;
            } else {
                if (newScaleFactor) {
                    initialScaleOld = initialScaleNew;
                    if (Math.acos(dA.x * d.x + dA.y * d.y) < Math.PI / 2d) {
                        initialScaleNew = initialScaleOld.add(snapXtranslate, Threshold.MC);
                        factorScale = initialScaleNew.divide(initialScaleOld, Threshold.MC);
                    } else if (scaleThreshold) {
                        initialScaleNew = initialScaleOld.subtract(snapXtranslate, Threshold.MC);
                        factorScale = initialScaleNew.divide(initialScaleOld, Threshold.MC);
                    } else {
                        initialScaleNew = initialScaleOld;
                        factorScale = BigDecimal.ONE;
                    }
                    factor = factorScale.floatValue();
                    factorPrecise = factorScale;
                } else {
                    if (Math.acos(dA.x * d.x + dA.y * d.y) < Math.PI / 2d) {
                        factor = factorScale.floatValue();
                        factorPrecise = factorScale;
                    } else {
                        factor = 1f / factorScale.floatValue();
                        factorPrecise = BigDecimal.ONE.divide(factorScale, Threshold.MC);
                    }
                }
                isScaling = true;
                oldFactor = factor;
                oldScaleFactor = factorPrecise;
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
            accurateTransformation = new Matrix(accurateXaxis[0], accurateXaxis[1], accurateXaxis[2], BigDecimal.ZERO, accurateYaxis[0].multiply(factorPrecise), accurateYaxis[1].multiply(factorPrecise),
                    accurateYaxis[2].multiply(factorPrecise), BigDecimal.ZERO, accurateZaxis[0], accurateZaxis[1], accurateZaxis[2], BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
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

        if (zScale) {
            if (l < SNAP_Y_SCALE)
                return temp;
            Vector4f vector = new Vector4f(zAxis);
            Vector4f virtpos = new Vector4f(Vector4f.add(vector, position, null));
            Vector4f screenpos = c3d.getPerspectiveCalculator().getScreenCoordinatesFrom3D(virtpos.x, virtpos.y, virtpos.z);
            Vector4f screenpos2 = c3d.getPerspectiveCalculator().getScreenCoordinatesFrom3D(position.x, position.y, position.z);
            Vector2f dA = new Vector2f(screenpos.x - screenpos2.x, screenpos.y - screenpos2.y);

            if (dA.lengthSquared() != 0f)
                dA.normalise();

            float factor;
            BigDecimal factorPrecise;
            if (isScaling) {
                factor = oldFactor;
                factorPrecise = oldScaleFactor;
            } else {
                if (newScaleFactor) {
                    initialScaleOld = initialScaleNew;
                    if (Math.acos(dA.x * d.x + dA.y * d.y) < Math.PI / 2d) {
                        initialScaleNew = initialScaleOld.add(snapXtranslate, Threshold.MC);
                        factorScale = initialScaleNew.divide(initialScaleOld, Threshold.MC);
                    } else if (scaleThreshold) {
                        initialScaleNew = initialScaleOld.subtract(snapXtranslate, Threshold.MC);
                        factorScale = initialScaleNew.divide(initialScaleOld, Threshold.MC);
                    } else {
                        initialScaleNew = initialScaleOld;
                        factorScale = BigDecimal.ONE;
                    }
                    factor = factorScale.floatValue();
                    factorPrecise = factorScale;
                } else {
                    if (Math.acos(dA.x * d.x + dA.y * d.y) < Math.PI / 2d) {
                        factor = factorScale.floatValue();
                        factorPrecise = factorScale;
                    } else {
                        factor = 1f / factorScale.floatValue();
                        factorPrecise = BigDecimal.ONE.divide(factorScale, Threshold.MC);
                    }
                }
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
                    accurateZaxis[0].multiply(factorPrecise), accurateZaxis[1].multiply(factorPrecise), accurateZaxis[2].multiply(factorPrecise), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
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

        if (xScale || yScale || zScale) {
            Matrix4f.mul(scale, result, result);
            accurateResult = Matrix.mul(accurateScale, accurateResult);
        }

        oldMousePosition.set(newX, newY);

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

    public Vector4f transformAtSelect(Vector2f oldMousePosition, int newX, int newY, Composite3D c3d) {

        Vector4f temp = new Vector4f(this.position);
        boolean isGlobal = Editor3DWindow.getWindow().getTransformationMode() == ManipulatorScope.GLOBAL;
        if (isGlobal) {
            position = new Vector4f(0f, 0f, 0f, 1f);
            accuratePosition = new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO };
        }

        Vector2f d = new Vector2f(newX - oldMousePosition.x, newY - oldMousePosition.y);

        if (d.lengthSquared() == 0f)
            return temp;

        d.normalise();

        Vector4f pos3d1 = c3d.getPerspectiveCalculator().get3DCoordinatesFromScreen(newX, newY);
        Vector4f pos3d2 = c3d.getPerspectiveCalculator().get3DCoordinatesFromScreen((int) oldMousePosition.x, (int) oldMousePosition.y);
        Vector4f d3d = Vector4f.sub(pos3d1, pos3d2, null);
        float l = d3d.length();
        BigDecimal lPrecise = new BigDecimal(l / 1000f);

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

            float factor = Math.max(l - l % (snapXtranslate.floatValue() * 1000f), snapXtranslate.floatValue() * 1000f);
            BigDecimal factorPrecise = MathHelper.max(lPrecise.subtract(lPrecise.remainder(snapXtranslate, Threshold.MC)), snapXtranslate);

            if (Math.acos(dA.x * d.x + dA.y * d.y) < Math.PI / 2d) {
                transformation.m30 = xAxis.x * factor;
                transformation.m31 = xAxis.y * factor;
                transformation.m32 = xAxis.z * factor;
                accurateTransformation = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, accurateXaxis[0].multiply(factorPrecise), accurateXaxis[1].multiply(factorPrecise), accurateXaxis[2].multiply(factorPrecise),
                        BigDecimal.ONE);
            } else {
                transformation.m30 = -xAxis.x * factor;
                transformation.m31 = -xAxis.y * factor;
                transformation.m32 = -xAxis.z * factor;
                accurateTransformation = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, accurateXaxis[0].multiply(factorPrecise).negate(), accurateXaxis[1].multiply(factorPrecise).negate(), accurateXaxis[2]
                                .multiply(factorPrecise).negate(), BigDecimal.ONE);
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

            float factor = Math.max(l - l % (snapZtranslate.floatValue() * 1000f), snapZtranslate.floatValue() * 1000f);
            BigDecimal factorPrecise = MathHelper.max(lPrecise.subtract(lPrecise.remainder(snapZtranslate, Threshold.MC)), snapZtranslate);

            if (Math.acos(dA.x * d.x + dA.y * d.y) < Math.PI / 2d) {
                transformation.m30 = yAxis.x * factor;
                transformation.m31 = yAxis.y * factor;
                transformation.m32 = yAxis.z * factor;
                accurateTransformation = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, accurateYaxis[0].multiply(factorPrecise), accurateYaxis[1].multiply(factorPrecise), accurateYaxis[2].multiply(factorPrecise),
                        BigDecimal.ONE);
            } else {
                transformation.m30 = -yAxis.x * factor;
                transformation.m31 = -yAxis.y * factor;
                transformation.m32 = -yAxis.z * factor;
                accurateTransformation = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, accurateYaxis[0].multiply(factorPrecise).negate(), accurateYaxis[1].multiply(factorPrecise).negate(), accurateYaxis[2]
                                .multiply(factorPrecise).negate(), BigDecimal.ONE);
            }
            accuratePosition = accurateTransformation.transform(accuratePosition[0], accuratePosition[1], accuratePosition[2]);
            Matrix4f.transform(transformation, position, position);
            accurateResult = Matrix.mul(accurateTransformation, accurateResult);
            Matrix4f.mul(transformation, result, result);
            Matrix4f.setIdentity(transformation);

            modified = true;
        }

        oldMousePosition.set(newX, newY);

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
        Vector3d x = new Vector3d(accurateXaxis[0], accurateXaxis[1], accurateXaxis[2]);
        Vector3d y = new Vector3d(accurateYaxis[0], accurateYaxis[1], accurateYaxis[2]);
        Vector3d z = new Vector3d(accurateZaxis[0], accurateZaxis[1], accurateZaxis[2]);
        x.normalise(x);
        y.normalise(y);
        z.normalise(z);
        return new Matrix(x.x, x.y, x.z, BigDecimal.ZERO, y.x, y.y, y.z, BigDecimal.ZERO, z.x, z.y, z.z, BigDecimal.ZERO, accuratePosition[0], accuratePosition[1], accuratePosition[2], BigDecimal.ONE);
    }

    public Matrix getAccurateRotation() {
        Vector3d x = new Vector3d(accurateXaxis[0], accurateXaxis[1], accurateXaxis[2]);
        Vector3d y = new Vector3d(accurateYaxis[0], accurateYaxis[1], accurateYaxis[2]);
        Vector3d z = new Vector3d(accurateZaxis[0], accurateZaxis[1], accurateZaxis[2]);
        x.normalise(x);
        y.normalise(y);
        z.normalise(z);
        return new Matrix(
                x.x, y.x, z.x, BigDecimal.ZERO,
                x.y, y.y, z.y, BigDecimal.ZERO,
                x.z, y.z, z.z, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE);
    }

    public boolean isLocked() {
        return lock;
    }

    public static float getTranslateSize() {
        return translateSize;
    }

    public static float getRotateSize() {
        return rotateSize;
    }

    public static float getRotateOuterSize() {
        return rotateOuterSize;
    }

    public static float getScaleSize() {
        return scaleSize;
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

    public void smallIncrement(WorkingMode action, ManipulatorAxisMode layer, ManipulatorScope scope, Composite3D c3d) {
        smallStep(action, layer, scope, BigDecimal.ONE, c3d);
    }

    public void smallDecrement(WorkingMode action, ManipulatorAxisMode layer, ManipulatorScope scope, Composite3D c3d) {
        smallStep(action, layer, scope, BigDecimal.ONE.negate(), c3d);
    }

    private void smallStep(WorkingMode action, ManipulatorAxisMode layer, ManipulatorScope scope, BigDecimal ddir, Composite3D c3d) {
        initialScaleOld = initialScaleNew;
        resetTranslation();
        switch (layer) {
        case X:
        case XY:
        case TEMP_X:
            if (action == WorkingMode.MOVE || action == WorkingMode.COMBINED) xTranslate = true;
            if (action == WorkingMode.ROTATE) xRotate = true;
            if (action == WorkingMode.SCALE) xScale = true;
            if (layer != ManipulatorAxisMode.XY) break;
        case Y:
        case YZ:
        case TEMP_Y:
            if (action == WorkingMode.MOVE || action == WorkingMode.COMBINED) yTranslate = true;
            if (action == WorkingMode.ROTATE) yRotate = true;
            if (action == WorkingMode.SCALE) yScale = true;
            if (layer != ManipulatorAxisMode.YZ) break;
        case Z:
        case XZ:
        case TEMP_Z:
            if (action == WorkingMode.MOVE || action == WorkingMode.COMBINED) zTranslate = true;
            if (action == WorkingMode.ROTATE) zRotate = true;
            if (action == WorkingMode.SCALE) zScale = true;
            if (layer != ManipulatorAxisMode.XZ) break;
            if (action == WorkingMode.MOVE || action == WorkingMode.COMBINED) xTranslate = true;
            if (action == WorkingMode.ROTATE) xRotate = true;
            if (action == WorkingMode.SCALE) xScale = true;
            break;
        case XYZ:
            if (action == WorkingMode.MOVE || action == WorkingMode.COMBINED) xTranslate = true;
            if (action == WorkingMode.MOVE || action == WorkingMode.COMBINED) yTranslate = true;
            if (action == WorkingMode.MOVE || action == WorkingMode.COMBINED) zTranslate = true;
            if (action == WorkingMode.SCALE) xScale = true;
            if (action == WorkingMode.SCALE) yScale = true;
            if (action == WorkingMode.SCALE) zScale = true;
            if (action == WorkingMode.ROTATE) vRotate = true;
            break;
        case NONE:
        default:
            // Can't happen...
            NLogger.error(getClass(), "Invalid call to Manipulator.smallStep()"); //$NON-NLS-1$
            return;
        }

        boolean newScaleFactor = initialScaleOld.compareTo(BigDecimal.ZERO) > 0;
        boolean scaleThreshold = newScaleFactor && snapXtranslate.add(snapXtranslate.divide(new BigDecimal(1000))).compareTo(initialScaleOld) < 0;
        float fdir = ddir.floatValue();
        modified = true;

        if (scope == ManipulatorScope.GLOBAL) {
            Vector4f t = new Vector4f(getPosition());
            BigDecimal[] tPrecise = getAccuratePosition();
            c3d.getManipulator().reset();
            c3d.getManipulator().getPosition().set(t);
            c3d.getManipulator().setAccuratePosition(tPrecise[0], tPrecise[1], tPrecise[2]);
        }

        Matrix4f transformation = new Matrix4f();
        Matrix accurateTransformation;
        Matrix4f.setIdentity(transformation);

        if (xTranslate) {
            float factor = snapXtranslate.floatValue() * 1000f * fdir;
            BigDecimal factorPrecise = snapXtranslate.multiply(ddir);
            transformation.m30 = xAxis.x * factor;
            transformation.m31 = xAxis.y * factor;
            transformation.m32 = xAxis.z * factor;
            accurateTransformation = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, accurateXaxis[0].multiply(factorPrecise), accurateXaxis[1].multiply(factorPrecise), accurateXaxis[2].multiply(factorPrecise),
                    BigDecimal.ONE);
            accuratePosition = accurateTransformation.transform(accuratePosition[0], accuratePosition[1], accuratePosition[2]);
            Matrix4f.transform(transformation, position, position);
            accurateResult = Matrix.mul(accurateTransformation, accurateResult);
            Matrix4f.mul(transformation, result, result);
            Matrix4f.setIdentity(transformation);
        }

        if (yTranslate) {
            float factor = snapZtranslate.floatValue() * 1000f * fdir;
            BigDecimal factorPrecise = snapZtranslate.multiply(ddir);
            transformation.m30 = yAxis.x * factor;
            transformation.m31 = yAxis.y * factor;
            transformation.m32 = yAxis.z * factor;
            accurateTransformation = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, accurateYaxis[0].multiply(factorPrecise), accurateYaxis[1].multiply(factorPrecise), accurateYaxis[2].multiply(factorPrecise),
                    BigDecimal.ONE);
            accuratePosition = accurateTransformation.transform(accuratePosition[0], accuratePosition[1], accuratePosition[2]);
            Matrix4f.transform(transformation, position, position);
            accurateResult = Matrix.mul(accurateTransformation, accurateResult);
            Matrix4f.mul(transformation, result, result);
            Matrix4f.setIdentity(transformation);
        }

        if (zTranslate) {
            float factor = snapYtranslate.floatValue() * 1000f * fdir;
            BigDecimal factorPrecise = snapYtranslate.multiply(ddir);
            transformation.m30 = zAxis.x * factor;
            transformation.m31 = zAxis.y * factor;
            transformation.m32 = zAxis.z * factor;
            accurateTransformation = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, accurateZaxis[0].multiply(factorPrecise), accurateZaxis[1].multiply(factorPrecise), accurateZaxis[2].multiply(factorPrecise),
                    BigDecimal.ONE);
            accuratePosition = accurateTransformation.transform(accuratePosition[0], accuratePosition[1], accuratePosition[2]);
            Matrix4f.transform(transformation, position, position);
            accurateResult = Matrix.mul(accurateTransformation, accurateResult);
            Matrix4f.mul(transformation, result, result);
            Matrix4f.setIdentity(transformation);
        }

        if (xRotate || yRotate || zRotate || vRotate) {
            Matrix4f forward = new Matrix4f();
            Matrix4f.setIdentity(forward);
            Matrix4f.translate(new Vector3f(-position.x, -position.y, -position.z), forward, forward);
            Matrix4f.mul(forward, result, result);
            accurateResult = Matrix.mul(View.ACCURATE_ID.translate(new BigDecimal[] { accuratePosition[0].negate(), accuratePosition[1].negate(), accuratePosition[2].negate() }), accurateResult);
        }

        if (xRotate) {
            BigDecimal factorPrecise = snapXrotate.multiply(ddir);
            transformation.rotate(factorPrecise.floatValue(), new Vector3f(xAxis.x, xAxis.y, xAxis.z));
            accurateTransformation = View.ACCURATE_ID.rotate(factorPrecise, snapXrotateFlag, accurateXaxis);
            accurateRotationX = accurateRotationX + factorPrecise.doubleValue();
            Vector4f vector = new Vector4f(xRotateArrow);
            Matrix4f m = new Matrix4f();
            Matrix4f.setIdentity(m);
            m.rotate(Math.max(factorPrecise.floatValue(), PI16TH), new Vector3f(xAxis.x, xAxis.y, xAxis.z));
            Matrix4f.transform(m, vector, vector);
            vector.setW(0f);
            vector.normalise();
            vector.setW(1f);
            xRotateArrow.set(vector);
            xRotateStart.set(xRotateArrow);
            Matrix4f.transform(transformation, yAxis, yAxis);
            Matrix4f.transform(transformation, zAxis, zAxis);
            accurateYaxis = accurateTransformation.transform(accurateYaxis[0], accurateYaxis[1], accurateYaxis[2]);
            accurateZaxis = accurateTransformation.transform(accurateZaxis[0], accurateZaxis[1], accurateZaxis[2]);
            accurateResult = Matrix.mul(accurateTransformation, accurateResult);
            Matrix4f.mul(transformation, result, result);
            Matrix4f.setIdentity(transformation);
        }

        if (yRotate) {
            BigDecimal factorPrecise = snapYrotate.multiply(ddir);
            transformation.rotate(factorPrecise.floatValue(), new Vector3f(yAxis.x, yAxis.y, yAxis.z));
            accurateTransformation = View.ACCURATE_ID.rotate(factorPrecise, snapYrotateFlag, accurateYaxis);
            accurateRotationY = accurateRotationY + factorPrecise.doubleValue();
            Vector4f vector = new Vector4f(yRotateArrow);
            Matrix4f m = new Matrix4f();
            Matrix4f.setIdentity(m);
            m.rotate(Math.max(factorPrecise.floatValue(), PI16TH), new Vector3f(yAxis.x, yAxis.y, yAxis.z));
            Matrix4f.transform(m, vector, vector);
            vector.setW(0f);
            vector.normalise();
            vector.setW(1f);
            yRotateArrow.set(vector);
            yRotateStart.set(yRotateArrow);
            Matrix4f.transform(transformation, xAxis, xAxis);
            Matrix4f.transform(transformation, zAxis, zAxis);
            accurateXaxis = accurateTransformation.transform(accurateXaxis[0], accurateXaxis[1], accurateXaxis[2]);
            accurateZaxis = accurateTransformation.transform(accurateZaxis[0], accurateZaxis[1], accurateZaxis[2]);
            accurateResult = Matrix.mul(accurateTransformation, accurateResult);
            Matrix4f.mul(transformation, result, result);
            Matrix4f.setIdentity(transformation);
        }

        if (zRotate) {
            BigDecimal factorPrecise = snapZrotate.multiply(ddir);
            transformation.rotate(factorPrecise.floatValue(), new Vector3f(zAxis.x, zAxis.y, zAxis.z));
            accurateTransformation = View.ACCURATE_ID.rotate(factorPrecise, snapZrotateFlag, accurateZaxis);
            accurateRotationZ = accurateRotationZ + factorPrecise.doubleValue();
            Vector4f vector = new Vector4f(zRotateArrow);
            Matrix4f m = new Matrix4f();
            Matrix4f.setIdentity(m);
            m.rotate(Math.max(factorPrecise.floatValue(), PI16TH), new Vector3f(zAxis.x, zAxis.y, zAxis.z));
            Matrix4f.transform(m, vector, vector);
            vector.setW(0f);
            vector.normalise();
            vector.setW(1f);
            zRotateArrow.set(vector);
            zRotateStart.set(zRotateArrow);
            Matrix4f.transform(transformation, yAxis, yAxis);
            Matrix4f.transform(transformation, xAxis, xAxis);
            accurateYaxis = accurateTransformation.transform(accurateYaxis[0], accurateYaxis[1], accurateYaxis[2]);
            accurateXaxis = accurateTransformation.transform(accurateXaxis[0], accurateXaxis[1], accurateXaxis[2]);
            accurateResult = Matrix.mul(accurateTransformation, accurateResult);
            Matrix4f.mul(transformation, result, result);
            Matrix4f.setIdentity(transformation);
        }

        if (vRotate) {
            Vector4f[] gen = c3d.getGenerator();
            BigDecimal factorPrecise = snapVrotate.multiply(ddir);
            transformation.rotate(factorPrecise.floatValue(), new Vector3f(gen[2].x, gen[2].y, gen[2].z));
            accurateTransformation = View.ACCURATE_ID.rotate(factorPrecise, snapVrotateFlag, new BigDecimal[] { new BigDecimal(gen[2].x), new BigDecimal(gen[2].y), new BigDecimal(gen[2].z) });
            Vector4f vector = new Vector4f(vRotateArrow);
            Matrix4f m = new Matrix4f();
            Matrix4f.setIdentity(m);
            m.rotate(Math.max(factorPrecise.floatValue(), PI16TH), new Vector3f(gen[2].x, gen[2].y, gen[2].z));
            Matrix4f.transform(m, vector, vector);
            vector.setW(0f);
            vector.normalise();
            vector.setW(1f);
            vRotateArrow.set(vector);
            vRotateStart.set(vRotateArrow);
            Matrix4f.transform(transformation, zAxis, zAxis);
            Matrix4f.transform(transformation, yAxis, yAxis);
            Matrix4f.transform(transformation, xAxis, xAxis);
            accurateXaxis = accurateTransformation.transform(accurateXaxis[0], accurateXaxis[1], accurateXaxis[2]);
            accurateYaxis = accurateTransformation.transform(accurateYaxis[0], accurateYaxis[1], accurateYaxis[2]);
            accurateZaxis = accurateTransformation.transform(accurateZaxis[0], accurateZaxis[1], accurateZaxis[2]);
            accurateResult = Matrix.mul(accurateTransformation, accurateResult);
            Matrix4f.mul(transformation, result, result);
            Matrix4f.setIdentity(transformation);
        }

        if (xRotate || yRotate || zRotate || vRotate) {
            Matrix4f backward = new Matrix4f();
            Matrix4f.setIdentity(backward);
            Matrix4f.translate(new Vector3f(position.x, position.y, position.z), backward, backward);
            Matrix4f.mul(backward, result, result);
            accurateResult = Matrix.mul(View.ACCURATE_ID.translate(new BigDecimal[] { accuratePosition[0], accuratePosition[1], accuratePosition[2] }), accurateResult);
        }

        if (xScale || yScale || zScale) {
            Matrix4f.setIdentity(scale);
            accurateScale = View.ACCURATE_ID;
        }

        if (xScale) {
            float factor;
            BigDecimal factorPrecise;
            if (newScaleFactor) {
                if (BigDecimal.ONE.compareTo(ddir) == 0) {
                    initialScaleNew = initialScaleOld.add(snapXtranslate, Threshold.MC);
                    factorScale = initialScaleNew.divide(initialScaleOld, Threshold.MC);
                } else if (scaleThreshold) {
                    initialScaleNew = initialScaleOld.subtract(snapXtranslate, Threshold.MC);
                    factorScale = initialScaleNew.divide(initialScaleOld, Threshold.MC);
                } else {
                    initialScaleNew = initialScaleOld;
                    factorScale = BigDecimal.ONE;
                }
                factor = factorScale.floatValue();
                factorPrecise = factorScale;
            } else {
                if (BigDecimal.ONE.compareTo(ddir) == 0) {
                    factor = factorScale.floatValue();
                    factorPrecise = factorScale;
                } else {
                    factor = 1f / factorScale.floatValue();
                    factorPrecise = BigDecimal.ONE.divide(factorScale, Threshold.MC);
                }
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
            transformation.m00 = factor * xAxis.x;
            transformation.m01 = factor * xAxis.y;
            transformation.m02 = factor * xAxis.z;
            transformation.m10 = yAxis.x;
            transformation.m11 = yAxis.y;
            transformation.m12 = yAxis.z;
            transformation.m20 = zAxis.x;
            transformation.m21 = zAxis.y;
            transformation.m22 = zAxis.z;
            accurateTransformation = new Matrix(accurateXaxis[0].multiply(factorPrecise), accurateXaxis[1].multiply(factorPrecise), accurateXaxis[2].multiply(factorPrecise), BigDecimal.ZERO, accurateYaxis[0],
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
            Matrix4f.setIdentity(transformation);
        }

        if (yScale) {
            float factor;
            BigDecimal factorPrecise;
            if (newScaleFactor) {
                if (BigDecimal.ONE.compareTo(ddir) == 0) {
                    initialScaleNew = initialScaleOld.add(snapXtranslate, Threshold.MC);
                    factorScale = initialScaleNew.divide(initialScaleOld, Threshold.MC);
                } else if (scaleThreshold) {
                    initialScaleNew = initialScaleOld.subtract(snapXtranslate, Threshold.MC);
                    factorScale = initialScaleNew.divide(initialScaleOld, Threshold.MC);
                } else {
                    initialScaleNew = initialScaleOld;
                    factorScale = BigDecimal.ONE;
                }
                factor = factorScale.floatValue();
                factorPrecise = factorScale;
            } else {
                if (BigDecimal.ONE.compareTo(ddir) == 0) {
                    factor = factorScale.floatValue();
                    factorPrecise = factorScale;
                } else {
                    factor = 1f / factorScale.floatValue();
                    factorPrecise = BigDecimal.ONE.divide(factorScale, Threshold.MC);
                }
            }
            transformation.m30 = -position.x;
            transformation.m31 = -position.y;
            transformation.m32 = -position.z;
            accurateTransformation = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, accuratePosition[0].negate(), accuratePosition[1].negate(), accuratePosition[2].negate(), BigDecimal.ONE);
            accurateScale = Matrix.mul(accurateTransformation, accurateScale);
            Matrix4f.mul(transformation, scale, scale);
            Matrix4f.setIdentity(transformation);
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
            accurateTransformation = new Matrix(accurateXaxis[0], accurateXaxis[1], accurateXaxis[2], BigDecimal.ZERO, accurateYaxis[0].multiply(factorPrecise), accurateYaxis[1].multiply(factorPrecise),
                    accurateYaxis[2].multiply(factorPrecise), BigDecimal.ZERO, accurateZaxis[0], accurateZaxis[1], accurateZaxis[2], BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
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
            Matrix4f.setIdentity(transformation);
        }

        if (zScale) {
            float factor;
            BigDecimal factorPrecise;
            if (newScaleFactor) {
                if (BigDecimal.ONE.compareTo(ddir) == 0) {
                    initialScaleNew = initialScaleOld.add(snapXtranslate, Threshold.MC);
                    factorScale = initialScaleNew.divide(initialScaleOld, Threshold.MC);
                } else if (scaleThreshold) {
                    initialScaleNew = initialScaleOld.subtract(snapXtranslate, Threshold.MC);
                    factorScale = initialScaleNew.divide(initialScaleOld, Threshold.MC);
                } else {
                    initialScaleNew = initialScaleOld;
                    factorScale = BigDecimal.ONE;
                }
                factor = factorScale.floatValue();
                factorPrecise = factorScale;
            } else {
                if (BigDecimal.ONE.compareTo(ddir) == 0) {
                    factor = factorScale.floatValue();
                    factorPrecise = factorScale;
                } else {
                    factor = 1f / factorScale.floatValue();
                    factorPrecise = BigDecimal.ONE.divide(factorScale, Threshold.MC);
                }
            }
            transformation.m30 = -position.x;
            transformation.m31 = -position.y;
            transformation.m32 = -position.z;
            accurateTransformation = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, accuratePosition[0].negate(), accuratePosition[1].negate(), accuratePosition[2].negate(), BigDecimal.ONE);
            accurateScale = Matrix.mul(accurateTransformation, accurateScale);
            Matrix4f.mul(transformation, scale, scale);
            Matrix4f.setIdentity(transformation);
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
                    accurateZaxis[0].multiply(factorPrecise), accurateZaxis[1].multiply(factorPrecise), accurateZaxis[2].multiply(factorPrecise), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
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
            Matrix4f.setIdentity(transformation);
        }

        if (xScale || yScale || zScale) {
            Matrix4f.mul(scale, result, result);
            accurateResult = Matrix.mul(accurateScale, accurateResult);
        }

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
    }

}
