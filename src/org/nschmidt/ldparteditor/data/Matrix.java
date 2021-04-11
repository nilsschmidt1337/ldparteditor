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
package org.nschmidt.ldparteditor.data;

import java.math.BigDecimal;
import java.math.MathContext;

import org.lwjgl.util.vector.Matrix4f;
import org.nschmidt.ldparteditor.enums.RotationSnap;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;

/**
 * Immutable, high precision matrix class
 *
 * @author nils
 *
 */
public final class Matrix {

    public final BigDecimal m00;
    public final BigDecimal m01;
    public final BigDecimal m02;
    public final BigDecimal m03;
    public final BigDecimal m10;
    public final BigDecimal m11;
    public final BigDecimal m12;
    public final BigDecimal m13;
    public final BigDecimal m20;
    public final BigDecimal m21;
    public final BigDecimal m22;
    public final BigDecimal m23;
    public final BigDecimal m30;
    public final BigDecimal m31;
    public final BigDecimal m32;
    public final BigDecimal m33;

    private final BigDecimal[][] m = new BigDecimal[4][4];

    /**
     *
     */
    public Matrix(BigDecimal m00, BigDecimal m01, BigDecimal m02, BigDecimal m03, BigDecimal m10, BigDecimal m11, BigDecimal m12, BigDecimal m13, BigDecimal m20, BigDecimal m21, BigDecimal m22,
            BigDecimal m23, BigDecimal m30, BigDecimal m31, BigDecimal m32, BigDecimal m33) {
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m03 = m03;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;
        this.m30 = m30;
        this.m31 = m31;
        this.m32 = m32;
        this.m33 = m33;
        this.m[0][0] = m00;
        this.m[1][0] = m10;
        this.m[2][0] = m20;
        this.m[3][0] = m30;
        this.m[0][1] = m01;
        this.m[1][1] = m11;
        this.m[2][1] = m21;
        this.m[3][1] = m31;
        this.m[0][2] = m02;
        this.m[1][2] = m12;
        this.m[2][2] = m22;
        this.m[3][2] = m32;
        this.m[0][3] = m03;
        this.m[1][3] = m13;
        this.m[2][3] = m23;
        this.m[3][3] = m33;
    }

    public Matrix(Matrix4f m) {
        BigDecimal tm00 = new BigDecimal(m.m00);
        BigDecimal tm01 = new BigDecimal(m.m01);
        BigDecimal tm02 = new BigDecimal(m.m02);
        BigDecimal tm03 = new BigDecimal(m.m03);
        BigDecimal tm10 = new BigDecimal(m.m10);
        BigDecimal tm11 = new BigDecimal(m.m11);
        BigDecimal tm12 = new BigDecimal(m.m12);
        BigDecimal tm13 = new BigDecimal(m.m13);
        BigDecimal tm20 = new BigDecimal(m.m20);
        BigDecimal tm21 = new BigDecimal(m.m21);
        BigDecimal tm22 = new BigDecimal(m.m22);
        BigDecimal tm23 = new BigDecimal(m.m23);
        BigDecimal tm30 = new BigDecimal(m.m30);
        BigDecimal tm31 = new BigDecimal(m.m31);
        BigDecimal tm32 = new BigDecimal(m.m32);
        BigDecimal tm33 = new BigDecimal(m.m33);
        this.m00 = tm00;
        this.m01 = tm01;
        this.m02 = tm02;
        this.m03 = tm03;
        this.m10 = tm10;
        this.m11 = tm11;
        this.m12 = tm12;
        this.m13 = tm13;
        this.m20 = tm20;
        this.m21 = tm21;
        this.m22 = tm22;
        this.m23 = tm23;
        this.m30 = tm30;
        this.m31 = tm31;
        this.m32 = tm32;
        this.m33 = tm33;
        this.m[0][0] = tm00;
        this.m[1][0] = tm10;
        this.m[2][0] = tm20;
        this.m[3][0] = tm30;
        this.m[0][1] = tm01;
        this.m[1][1] = tm11;
        this.m[2][1] = tm21;
        this.m[3][1] = tm31;
        this.m[0][2] = tm02;
        this.m[1][2] = tm12;
        this.m[2][2] = tm22;
        this.m[3][2] = tm32;
        this.m[0][3] = tm03;
        this.m[1][3] = tm13;
        this.m[2][3] = tm23;
        this.m[3][3] = tm33;
    }

    public Matrix(BigDecimal[][] m) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                this.m[j][i] = m[j][i];
            }
        }
        this.m00 = this.m[0][0];
        this.m01 = this.m[0][1];
        this.m02 = this.m[0][2];
        this.m03 = this.m[0][3];
        this.m10 = this.m[1][0];
        this.m11 = this.m[1][1];
        this.m12 = this.m[1][2];
        this.m13 = this.m[1][3];
        this.m20 = this.m[2][0];
        this.m21 = this.m[2][1];
        this.m22 = this.m[2][2];
        this.m23 = this.m[2][3];
        this.m30 = this.m[3][0];
        this.m31 = this.m[3][1];
        this.m32 = this.m[3][2];
        this.m33 = this.m[3][3];
    }

    Matrix set(BigDecimal value, int col, int row) {
        BigDecimal[][] n = new BigDecimal[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i == row && j == col) {
                    n[j][i] = value;
                } else {
                    n[j][i] = m[j][i];
                }
            }
        }
        return new Matrix(n);
    }

    public Matrix(Matrix matrix) {
        this(matrix.m);
    }

    public static Matrix mul(Matrix left, Matrix right) {
        final MathContext mc = Threshold.MC;
        final BigDecimal[][] m = new BigDecimal[4][4];
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 4; column++) {
                final BigDecimal p1 = left.m[0][row].multiply(right.m[column][0], mc);
                final BigDecimal p2 = left.m[1][row].multiply(right.m[column][1], mc);
                final BigDecimal p3 = left.m[2][row].multiply(right.m[column][2], mc);
                final BigDecimal p4 = left.m[3][row].multiply(right.m[column][3], mc);
                m[column][row] = p1.add(p2, mc).add(p3, mc).add(p4, mc);
            }
        }
        return new Matrix(m);

    }

    Matrix transpose() {
        final BigDecimal[][] mn = new BigDecimal[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                mn[i][j] = this.m[j][i];
            }
        }
        return new Matrix(mn);
    }

    Matrix transposeXYZ() {
        final BigDecimal[][] mn = new BigDecimal[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                mn[i][j] = this.m[i][j];
            }
        }
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i == 3 || j == 3)
                    mn[j][i] = this.m[i][j];
            }
        }
        return new Matrix(mn);
    }

    public Matrix invert() {
        final BigDecimal[][] mn = new BigDecimal[4][4];

        BigDecimal s0 = m[0][0].multiply(m[1][1]).subtract(m[1][0].multiply(m[0][1]));
        BigDecimal s1 = m[0][0].multiply(m[1][2]).subtract(m[1][0].multiply(m[0][2]));
        BigDecimal s2 = m[0][0].multiply(m[1][3]).subtract(m[1][0].multiply(m[0][3]));
        BigDecimal s3 = m[0][1].multiply(m[1][2]).subtract(m[1][1].multiply(m[0][2]));
        BigDecimal s4 = m[0][1].multiply(m[1][3]).subtract(m[1][1].multiply(m[0][3]));
        BigDecimal s5 = m[0][2].multiply(m[1][3]).subtract(m[1][2].multiply(m[0][3]));

        BigDecimal c5 = m[2][2].multiply(m[3][3]).subtract(m[3][2].multiply(m[2][3]));
        BigDecimal c4 = m[2][1].multiply(m[3][3]).subtract(m[3][1].multiply(m[2][3]));
        BigDecimal c3 = m[2][1].multiply(m[3][2]).subtract(m[3][1].multiply(m[2][2]));
        BigDecimal c2 = m[2][0].multiply(m[3][3]).subtract(m[3][0].multiply(m[2][3]));
        BigDecimal c1 = m[2][0].multiply(m[3][2]).subtract(m[3][0].multiply(m[2][2]));
        BigDecimal c0 = m[2][0].multiply(m[3][1]).subtract(m[3][0].multiply(m[2][1]));

        // TODO Should check for 0 determinant

        BigDecimal invdet = BigDecimal.ONE.divide(s0.multiply(c5).subtract(s1.multiply(c4)).add(s2.multiply(c3)).add(s3.multiply(c2)).subtract(s4.multiply(c1)).add(s5.multiply(c0)), Threshold.MC);

        mn[0][0] = m[1][1].multiply(c5).subtract(m[1][2].multiply(c4)).add(m[1][3].multiply(c3)).multiply(invdet);
        mn[0][1] = m[0][1].negate().multiply(c5).add(m[0][2].multiply(c4)).subtract(m[0][3].multiply(c3)).multiply(invdet);
        mn[0][2] = m[3][1].multiply(s5).subtract(m[3][2].multiply(s4)).add(m[3][3].multiply(s3)).multiply(invdet);
        mn[0][3] = m[2][1].negate().multiply(s5).add(m[2][2].multiply(s4)).subtract(m[2][3].multiply(s3)).multiply(invdet);

        mn[1][0] = m[1][0].negate().multiply(c5).add(m[1][2].multiply(c2)).subtract(m[1][3].multiply(c1)).multiply(invdet);
        mn[1][1] = m[0][0].multiply(c5).subtract(m[0][2].multiply(c2)).add(m[0][3].multiply(c1)).multiply(invdet);
        mn[1][2] = m[3][0].negate().multiply(s5).add(m[3][2].multiply(s2)).subtract(m[3][3].multiply(s1)).multiply(invdet);
        mn[1][3] = m[2][0].multiply(s5).subtract(m[2][2].multiply(s2)).add(m[2][3].multiply(s1)).multiply(invdet);

        mn[2][0] = m[1][0].multiply(c4).subtract(m[1][1].multiply(c2)).add(m[1][3].multiply(c0)).multiply(invdet);
        mn[2][1] = m[0][0].negate().multiply(c4).add(m[0][1].multiply(c2)).subtract(m[0][3].multiply(c0)).multiply(invdet);
        mn[2][2] = m[3][0].multiply(s4).subtract(m[3][1].multiply(s2)).add(m[3][3].multiply(s0)).multiply(invdet);
        mn[2][3] = m[2][0].negate().multiply(s4).add(m[2][1].multiply(s2)).subtract(m[2][3].multiply(s0)).multiply(invdet);

        mn[3][0] = m[1][0].negate().multiply(c3).add(m[1][1].multiply(c1)).subtract(m[1][2].multiply(c0)).multiply(invdet);
        mn[3][1] = m[0][0].multiply(c3).subtract(m[0][1].multiply(c1)).add(m[0][2].multiply(c0)).multiply(invdet);
        mn[3][2] = m[3][0].negate().multiply(s3).add(m[3][1].multiply(s1)).subtract(m[3][2].multiply(s0)).multiply(invdet);
        mn[3][3] = m[2][0].multiply(s3).subtract(m[2][1].multiply(s1)).add(m[2][2].multiply(s0)).multiply(invdet);

        return new Matrix(mn);
    }

    public BigDecimal[] transform(BigDecimal x, BigDecimal y, BigDecimal z) {
        final MathContext mc = Threshold.MC;
        final BigDecimal[] result = new BigDecimal[4];
        for (int row = 0; row < 4; row++) {
            final BigDecimal p1 = this.m[0][row].multiply(x, mc);
            final BigDecimal p2 = this.m[1][row].multiply(y, mc);
            final BigDecimal p3 = this.m[2][row].multiply(z, mc);
            final BigDecimal p4 = this.m[3][row];
            result[row] = p1.add(p2, mc).add(p3, mc).add(p4, mc);
        }
        return result;
    }


    public Vector3d transform(Vector3d v) {
        final BigDecimal[] result = transform(v.x, v.y, v.z);
        return new Vector3d(result[0], result[1], result[2]);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result.append(this.m[j][i].floatValue());
                result.append(" "); //$NON-NLS-1$
            }
            result.append("\n"); //$NON-NLS-1$
        }
        return result.toString();
    }

    public String toLDrawString() {
        StringBuilder result = new StringBuilder();
        result.append(" "); //$NON-NLS-1$
        result.append(bigDecimalToString(this.m30));
        result.append(" "); //$NON-NLS-1$
        result.append(bigDecimalToString(this.m31));
        result.append(" "); //$NON-NLS-1$
        result.append(bigDecimalToString(this.m32));
        result.append(" "); //$NON-NLS-1$
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result.append(bigDecimalToString(this.m[j][i]));
                result.append(" "); //$NON-NLS-1$
            }
        }
        return result.toString();
    }

    private String bigDecimalToString(BigDecimal bd) {
        String result;
        if (bd.compareTo(BigDecimal.ZERO) == 0)
            return "0"; //$NON-NLS-1$
        BigDecimal bd2 = bd.stripTrailingZeros();
        result = bd2.toPlainString();
        if (result.startsWith("-0."))return "-" + result.substring(2); //$NON-NLS-1$ //$NON-NLS-2$
        if (result.startsWith("0."))return result.substring(1); //$NON-NLS-1$
        return result;
    }

    public Matrix4f getMatrix4f() {

        Matrix4f result = new Matrix4f();

        result.m00 = m00.floatValue();
        result.m01 = m01.floatValue();
        result.m02 = m02.floatValue();
        result.m03 = m03.floatValue();

        result.m10 = m10.floatValue();
        result.m11 = m11.floatValue();
        result.m12 = m12.floatValue();
        result.m13 = m13.floatValue();

        result.m20 = m20.floatValue();
        result.m21 = m21.floatValue();
        result.m22 = m22.floatValue();
        result.m23 = m23.floatValue();

        result.m30 = m30.floatValue();
        result.m31 = m31.floatValue();
        result.m32 = m32.floatValue();
        result.m33 = m33.floatValue();

        return result;
    }

    public BigDecimal determinant() {

        BigDecimal s0 = m[0][0].multiply(m[1][1]).subtract(m[1][0].multiply(m[0][1]));
        BigDecimal s1 = m[0][0].multiply(m[1][2]).subtract(m[1][0].multiply(m[0][2]));
        BigDecimal s2 = m[0][0].multiply(m[1][3]).subtract(m[1][0].multiply(m[0][3]));
        BigDecimal s3 = m[0][1].multiply(m[1][2]).subtract(m[1][1].multiply(m[0][2]));
        BigDecimal s4 = m[0][1].multiply(m[1][3]).subtract(m[1][1].multiply(m[0][3]));
        BigDecimal s5 = m[0][2].multiply(m[1][3]).subtract(m[1][2].multiply(m[0][3]));

        BigDecimal c5 = m[2][2].multiply(m[3][3]).subtract(m[3][2].multiply(m[2][3]));
        BigDecimal c4 = m[2][1].multiply(m[3][3]).subtract(m[3][1].multiply(m[2][3]));
        BigDecimal c3 = m[2][1].multiply(m[3][2]).subtract(m[3][1].multiply(m[2][2]));
        BigDecimal c2 = m[2][0].multiply(m[3][3]).subtract(m[3][0].multiply(m[2][3]));
        BigDecimal c1 = m[2][0].multiply(m[3][2]).subtract(m[3][0].multiply(m[2][2]));
        BigDecimal c0 = m[2][0].multiply(m[3][1]).subtract(m[3][0].multiply(m[2][1]));

        return s0.multiply(c5).subtract(s1.multiply(c4)).add(s2.multiply(c3)).add(s3.multiply(c2)).subtract(s4.multiply(c1)).add(s5.multiply(c0));
    }

    public Matrix rotate(BigDecimal angle, RotationSnap rs, BigDecimal[] axis) {

        final BigDecimal f00;
        final BigDecimal f01;
        final BigDecimal f02;
        final BigDecimal f10;
        final BigDecimal f11;
        final BigDecimal f12;
        final BigDecimal f20;
        final BigDecimal f21;
        final BigDecimal f22;

        int ac = 0;
        int ax = 0;
        boolean neg = false;
        if (axis[0].abs().compareTo(BigDecimal.ONE) == 0) {
            ax = 1;
            ac++;
            neg = axis[0].compareTo(BigDecimal.ZERO) < 0;
        } else if (axis[0].compareTo(BigDecimal.ZERO) == 0) ac++;
        if (axis[1].abs().compareTo(BigDecimal.ONE) == 0) {
            if (ax != 0) ac = 0;
            ax = 2;
            ac++;
            neg = axis[1].compareTo(BigDecimal.ZERO) < 0;
        } else if (axis[1].compareTo(BigDecimal.ZERO) == 0) ac++;
        if (axis[2].abs().compareTo(BigDecimal.ONE) == 0) {
            if (ax != 0) ac = 0;
            ax = 3;
            ac++;
            neg = axis[2].compareTo(BigDecimal.ZERO) < 0;
        } else if (axis[2].compareTo(BigDecimal.ZERO) == 0) ac++;
        if (ac != 3 || ax < 1) {
            rs = RotationSnap.COMPLEX;
        } else if (angle.compareTo(BigDecimal.ZERO) < 0 ^ !neg) {
            if (rs == RotationSnap.DEG270) {
                rs = RotationSnap.DEG90;
            } else if (rs == RotationSnap.DEG90) {
                rs = RotationSnap.DEG270;
            }
        }
        switch (rs) {
        case DEG180:
            switch (ax) {
            case 1:
                f00 = BigDecimal.ONE;
                f01 = BigDecimal.ZERO;
                f02 = BigDecimal.ZERO;
                f10 = BigDecimal.ZERO;
                f11 = BigDecimal.ONE.negate();
                f12 = BigDecimal.ZERO;
                f20 = BigDecimal.ZERO;
                f21 = BigDecimal.ZERO;
                f22 = BigDecimal.ONE.negate();
                break;
            case 2:
                f00 = BigDecimal.ONE.negate();
                f01 = BigDecimal.ZERO;
                f02 = BigDecimal.ZERO;
                f10 = BigDecimal.ZERO;
                f11 = BigDecimal.ONE;
                f12 = BigDecimal.ZERO;
                f20 = BigDecimal.ZERO;
                f21 = BigDecimal.ZERO;
                f22 = BigDecimal.ONE.negate();
                break;
            default:
                f00 = BigDecimal.ONE.negate();
                f01 = BigDecimal.ZERO;
                f02 = BigDecimal.ZERO;
                f10 = BigDecimal.ZERO;
                f11 = BigDecimal.ONE.negate();
                f12 = BigDecimal.ZERO;
                f20 = BigDecimal.ZERO;
                f21 = BigDecimal.ZERO;
                f22 = BigDecimal.ONE;
                break;
            }
            break;
        case DEG90:
            switch (ax) {
            case 1:
                f00 = BigDecimal.ONE;
                f01 = BigDecimal.ZERO;
                f02 = BigDecimal.ZERO;
                f10 = BigDecimal.ZERO;
                f11 = BigDecimal.ZERO;
                f12 = BigDecimal.ONE.negate();
                f20 = BigDecimal.ZERO;
                f21 = BigDecimal.ONE;
                f22 = BigDecimal.ZERO;
                break;
            case 2:
                f00 = BigDecimal.ZERO;
                f01 = BigDecimal.ZERO;
                f02 = BigDecimal.ONE;
                f10 = BigDecimal.ZERO;
                f11 = BigDecimal.ONE;
                f12 = BigDecimal.ZERO;
                f20 = BigDecimal.ONE.negate();
                f21 = BigDecimal.ZERO;
                f22 = BigDecimal.ZERO;
                break;
            default:
                f00 = BigDecimal.ZERO;
                f01 = BigDecimal.ONE.negate();
                f02 = BigDecimal.ZERO;
                f10 = BigDecimal.ONE;
                f11 = BigDecimal.ZERO;
                f12 = BigDecimal.ZERO;
                f20 = BigDecimal.ZERO;
                f21 = BigDecimal.ZERO;
                f22 = BigDecimal.ONE;
                break;
            }
            break;
        case DEG360:
            f00 = BigDecimal.ONE;
            f01 = BigDecimal.ZERO;
            f02 = BigDecimal.ZERO;
            f10 = BigDecimal.ZERO;
            f11 = BigDecimal.ONE;
            f12 = BigDecimal.ZERO;
            f20 = BigDecimal.ZERO;
            f21 = BigDecimal.ZERO;
            f22 = BigDecimal.ONE;
            break;
        case DEG270:
            switch (ax) {
            case 1:
                f00 = BigDecimal.ONE;
                f01 = BigDecimal.ZERO;
                f02 = BigDecimal.ZERO;
                f10 = BigDecimal.ZERO;
                f11 = BigDecimal.ZERO;
                f12 = BigDecimal.ONE;
                f20 = BigDecimal.ZERO;
                f21 = BigDecimal.ONE.negate();
                f22 = BigDecimal.ZERO;
                break;
            case 2:
                f00 = BigDecimal.ZERO;
                f01 = BigDecimal.ZERO;
                f02 = BigDecimal.ONE.negate();
                f10 = BigDecimal.ZERO;
                f11 = BigDecimal.ONE;
                f12 = BigDecimal.ZERO;
                f20 = BigDecimal.ONE;
                f21 = BigDecimal.ZERO;
                f22 = BigDecimal.ZERO;
                break;
            default:
                f00 = BigDecimal.ZERO;
                f01 = BigDecimal.ONE;
                f02 = BigDecimal.ZERO;
                f10 = BigDecimal.ONE.negate();
                f11 = BigDecimal.ZERO;
                f12 = BigDecimal.ZERO;
                f20 = BigDecimal.ZERO;
                f21 = BigDecimal.ZERO;
                f22 = BigDecimal.ONE;
                break;
            }
            break;
        case COMPLEX:
        default:
            BigDecimal c = MathHelper.cos(angle);
            BigDecimal s = MathHelper.sin(angle);
            BigDecimal oneminusc = BigDecimal.ONE.subtract(c);
            BigDecimal xy = axis[0].multiply(axis[1]);
            BigDecimal yz = axis[1].multiply(axis[2]);
            BigDecimal xz = axis[0].multiply(axis[2]);
            BigDecimal xs = axis[0].multiply(s);
            BigDecimal ys = axis[1].multiply(s);
            BigDecimal zs = axis[2].multiply(s);
            f00 = axis[0].multiply(axis[0]).multiply(oneminusc).add(c);
            f01 = xy.multiply(oneminusc).add(zs);
            f02 = xz.multiply(oneminusc).subtract(ys);
            f10 = xy.multiply(oneminusc).subtract(zs);
            f11 = axis[1].multiply(axis[1]).multiply(oneminusc).add(c);
            f12 = yz.multiply(oneminusc).add(xs);
            f20 = xz.multiply(oneminusc).add(ys);
            f21 = yz.multiply(oneminusc).subtract(xs);
            f22 = axis[2].multiply(axis[2]).multiply(oneminusc).add(c);
            break;
        }


        BigDecimal t00 = m00.multiply(f00).add(m10.multiply(f01)).add(m20.multiply(f02));
        BigDecimal t01 = m01.multiply(f00).add(m11.multiply(f01)).add(m21.multiply(f02));
        BigDecimal t02 = m02.multiply(f00).add(m12.multiply(f01)).add(m22.multiply(f02));
        BigDecimal t03 = m03.multiply(f00).add(m13.multiply(f01)).add(m23.multiply(f02));
        BigDecimal t10 = m00.multiply(f10).add(m10.multiply(f11)).add(m20.multiply(f12));
        BigDecimal t11 = m01.multiply(f10).add(m11.multiply(f11)).add(m21.multiply(f12));
        BigDecimal t12 = m02.multiply(f10).add(m12.multiply(f11)).add(m22.multiply(f12));
        BigDecimal t13 = m03.multiply(f10).add(m13.multiply(f11)).add(m23.multiply(f12));
        BigDecimal tm20 = m00.multiply(f20).add(m10.multiply(f21)).add(m20.multiply(f22));
        BigDecimal tm21 = m01.multiply(f20).add(m11.multiply(f21)).add(m21.multiply(f22));
        BigDecimal tm22 = m02.multiply(f20).add(m12.multiply(f21)).add(m22.multiply(f22));
        BigDecimal tm23 = m03.multiply(f20).add(m13.multiply(f21)).add(m23.multiply(f22));
        BigDecimal tm00 = t00;
        BigDecimal tm01 = t01;
        BigDecimal tm02 = t02;
        BigDecimal tm03 = t03;
        BigDecimal tm10 = t10;
        BigDecimal tm11 = t11;
        BigDecimal tm12 = t12;
        BigDecimal tm13 = t13;

        return new Matrix(tm00, tm01, tm02, tm03, tm10, tm11, tm12, tm13, tm20, tm21, tm22, tm23, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE);
    }

    public Matrix translate(BigDecimal[] vec) {

        BigDecimal tm30 = m30.add(m00.multiply(vec[0]).add(m10.multiply(vec[1])).add(m20.multiply(vec[2])));
        BigDecimal tm31 = m31.add(m01.multiply(vec[0]).add(m11.multiply(vec[1])).add(m21.multiply(vec[2])));
        BigDecimal tm32 = m32.add(m02.multiply(vec[0]).add(m12.multiply(vec[1])).add(m22.multiply(vec[2])));

        return new Matrix(m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, tm30, tm31, tm32, BigDecimal.ONE);
    }

    Matrix reduceAccuracy() {
        final BigDecimal[][] rounded = new BigDecimal[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                rounded[i][j] = this.m[i][j].round(Threshold.MC);
            }
        }
        return new Matrix(rounded);
    }
}
