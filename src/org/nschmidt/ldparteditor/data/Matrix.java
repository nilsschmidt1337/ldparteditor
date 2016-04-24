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

    public final BigDecimal M00;
    public final BigDecimal M01;
    public final BigDecimal M02;
    public final BigDecimal M03;
    public final BigDecimal M10;
    public final BigDecimal M11;
    public final BigDecimal M12;
    public final BigDecimal M13;
    public final BigDecimal M20;
    public final BigDecimal M21;
    public final BigDecimal M22;
    public final BigDecimal M23;
    public final BigDecimal M30;
    public final BigDecimal M31;
    public final BigDecimal M32;
    public final BigDecimal M33;

    private final BigDecimal[][] M = new BigDecimal[4][4];

    /**
     *
     */
    public Matrix(BigDecimal M00, BigDecimal M01, BigDecimal M02, BigDecimal M03, BigDecimal M10, BigDecimal M11, BigDecimal M12, BigDecimal M13, BigDecimal M20, BigDecimal M21, BigDecimal M22,
            BigDecimal M23, BigDecimal M30, BigDecimal M31, BigDecimal M32, BigDecimal M33) {
        this.M00 = M00;
        this.M01 = M01;
        this.M02 = M02;
        this.M03 = M03;
        this.M10 = M10;
        this.M11 = M11;
        this.M12 = M12;
        this.M13 = M13;
        this.M20 = M20;
        this.M21 = M21;
        this.M22 = M22;
        this.M23 = M23;
        this.M30 = M30;
        this.M31 = M31;
        this.M32 = M32;
        this.M33 = M33;
        this.M[0][0] = M00;
        this.M[1][0] = M10;
        this.M[2][0] = M20;
        this.M[3][0] = M30;
        this.M[0][1] = M01;
        this.M[1][1] = M11;
        this.M[2][1] = M21;
        this.M[3][1] = M31;
        this.M[0][2] = M02;
        this.M[1][2] = M12;
        this.M[2][2] = M22;
        this.M[3][2] = M32;
        this.M[0][3] = M03;
        this.M[1][3] = M13;
        this.M[2][3] = M23;
        this.M[3][3] = M33;
    }

    public Matrix(Matrix4f m) {
        BigDecimal M00 = new BigDecimal(m.m00);
        BigDecimal M01 = new BigDecimal(m.m01);
        BigDecimal M02 = new BigDecimal(m.m02);
        BigDecimal M03 = new BigDecimal(m.m03);
        BigDecimal M10 = new BigDecimal(m.m10);
        BigDecimal M11 = new BigDecimal(m.m11);
        BigDecimal M12 = new BigDecimal(m.m12);
        BigDecimal M13 = new BigDecimal(m.m13);
        BigDecimal M20 = new BigDecimal(m.m20);
        BigDecimal M21 = new BigDecimal(m.m21);
        BigDecimal M22 = new BigDecimal(m.m22);
        BigDecimal M23 = new BigDecimal(m.m23);
        BigDecimal M30 = new BigDecimal(m.m30);
        BigDecimal M31 = new BigDecimal(m.m31);
        BigDecimal M32 = new BigDecimal(m.m32);
        BigDecimal M33 = new BigDecimal(m.m33);
        this.M00 = M00;
        this.M01 = M01;
        this.M02 = M02;
        this.M03 = M03;
        this.M10 = M10;
        this.M11 = M11;
        this.M12 = M12;
        this.M13 = M13;
        this.M20 = M20;
        this.M21 = M21;
        this.M22 = M22;
        this.M23 = M23;
        this.M30 = M30;
        this.M31 = M31;
        this.M32 = M32;
        this.M33 = M33;
        this.M[0][0] = M00;
        this.M[1][0] = M10;
        this.M[2][0] = M20;
        this.M[3][0] = M30;
        this.M[0][1] = M01;
        this.M[1][1] = M11;
        this.M[2][1] = M21;
        this.M[3][1] = M31;
        this.M[0][2] = M02;
        this.M[1][2] = M12;
        this.M[2][2] = M22;
        this.M[3][2] = M32;
        this.M[0][3] = M03;
        this.M[1][3] = M13;
        this.M[2][3] = M23;
        this.M[3][3] = M33;
    }

    public Matrix(BigDecimal[][] M) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                this.M[j][i] = M[j][i];
            }
        }
        this.M00 = this.M[0][0];
        this.M01 = this.M[0][1];
        this.M02 = this.M[0][2];
        this.M03 = this.M[0][3];
        this.M10 = this.M[1][0];
        this.M11 = this.M[1][1];
        this.M12 = this.M[1][2];
        this.M13 = this.M[1][3];
        this.M20 = this.M[2][0];
        this.M21 = this.M[2][1];
        this.M22 = this.M[2][2];
        this.M23 = this.M[2][3];
        this.M30 = this.M[3][0];
        this.M31 = this.M[3][1];
        this.M32 = this.M[3][2];
        this.M33 = this.M[3][3];
    }

    public Matrix set(BigDecimal value, int col, int row) {
        BigDecimal[][] N = new BigDecimal[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i == row && j == col) {
                    N[j][i] = value;
                } else {
                    N[j][i] = M[j][i];
                }
            }
        }
        return new Matrix(N);
    }

    public Matrix(Matrix matrix) {
        this(matrix.M);
    }

    public static Matrix mul(Matrix left, Matrix right) {
        final MathContext mc = Threshold.mc;
        final BigDecimal[][] M = new BigDecimal[4][4];
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 4; column++) {
                final BigDecimal P1 = left.M[0][row].multiply(right.M[column][0], mc);
                final BigDecimal P2 = left.M[1][row].multiply(right.M[column][1], mc);
                final BigDecimal P3 = left.M[2][row].multiply(right.M[column][2], mc);
                final BigDecimal P4 = left.M[3][row].multiply(right.M[column][3], mc);
                M[column][row] = P1.add(P2, mc).add(P3, mc).add(P4, mc);
            }
        }
        return new Matrix(M);

    }

    public Matrix transpose() {
        final BigDecimal[][] Mn = new BigDecimal[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                Mn[i][j] = this.M[j][i];
            }
        }
        return new Matrix(Mn);
    }

    public Matrix transposeXYZ() {
        final BigDecimal[][] Mn = new BigDecimal[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                Mn[i][j] = this.M[i][j];
            }
        }
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i == 3 || j == 3)
                    Mn[j][i] = this.M[i][j];
            }
        }
        return new Matrix(Mn);
    }

    public Matrix invert() {
        final BigDecimal[][] Mn = new BigDecimal[4][4];

        BigDecimal s0 = M[0][0].multiply(M[1][1]).subtract(M[1][0].multiply(M[0][1]));
        BigDecimal s1 = M[0][0].multiply(M[1][2]).subtract(M[1][0].multiply(M[0][2]));
        BigDecimal s2 = M[0][0].multiply(M[1][3]).subtract(M[1][0].multiply(M[0][3]));
        BigDecimal s3 = M[0][1].multiply(M[1][2]).subtract(M[1][1].multiply(M[0][2]));
        BigDecimal s4 = M[0][1].multiply(M[1][3]).subtract(M[1][1].multiply(M[0][3]));
        BigDecimal s5 = M[0][2].multiply(M[1][3]).subtract(M[1][2].multiply(M[0][3]));

        BigDecimal c5 = M[2][2].multiply(M[3][3]).subtract(M[3][2].multiply(M[2][3]));
        BigDecimal c4 = M[2][1].multiply(M[3][3]).subtract(M[3][1].multiply(M[2][3]));
        BigDecimal c3 = M[2][1].multiply(M[3][2]).subtract(M[3][1].multiply(M[2][2]));
        BigDecimal c2 = M[2][0].multiply(M[3][3]).subtract(M[3][0].multiply(M[2][3]));
        BigDecimal c1 = M[2][0].multiply(M[3][2]).subtract(M[3][0].multiply(M[2][2]));
        BigDecimal c0 = M[2][0].multiply(M[3][1]).subtract(M[3][0].multiply(M[2][1]));

        // TODO Should check for 0 determinant

        BigDecimal invdet = BigDecimal.ONE.divide(s0.multiply(c5).subtract(s1.multiply(c4)).add(s2.multiply(c3)).add(s3.multiply(c2)).subtract(s4.multiply(c1)).add(s5.multiply(c0)), Threshold.mc);

        Mn[0][0] = M[1][1].multiply(c5).subtract(M[1][2].multiply(c4)).add(M[1][3].multiply(c3)).multiply(invdet);
        Mn[0][1] = M[0][1].negate().multiply(c5).add(M[0][2].multiply(c4)).subtract(M[0][3].multiply(c3)).multiply(invdet);
        Mn[0][2] = M[3][1].multiply(s5).subtract(M[3][2].multiply(s4)).add(M[3][3].multiply(s3)).multiply(invdet);
        Mn[0][3] = M[2][1].negate().multiply(s5).add(M[2][2].multiply(s4)).subtract(M[2][3].multiply(s3)).multiply(invdet);

        Mn[1][0] = M[1][0].negate().multiply(c5).add(M[1][2].multiply(c2)).subtract(M[1][3].multiply(c1)).multiply(invdet);
        Mn[1][1] = M[0][0].multiply(c5).subtract(M[0][2].multiply(c2)).add(M[0][3].multiply(c1)).multiply(invdet);
        Mn[1][2] = M[3][0].negate().multiply(s5).add(M[3][2].multiply(s2)).subtract(M[3][3].multiply(s1)).multiply(invdet);
        Mn[1][3] = M[2][0].multiply(s5).subtract(M[2][2].multiply(s2)).add(M[2][3].multiply(s1)).multiply(invdet);

        Mn[2][0] = M[1][0].multiply(c4).subtract(M[1][1].multiply(c2)).add(M[1][3].multiply(c0)).multiply(invdet);
        Mn[2][1] = M[0][0].negate().multiply(c4).add(M[0][1].multiply(c2)).subtract(M[0][3].multiply(c0)).multiply(invdet);
        Mn[2][2] = M[3][0].multiply(s4).subtract(M[3][1].multiply(s2)).add(M[3][3].multiply(s0)).multiply(invdet);
        Mn[2][3] = M[2][0].negate().multiply(s4).add(M[2][1].multiply(s2)).subtract(M[2][3].multiply(s0)).multiply(invdet);

        Mn[3][0] = M[1][0].negate().multiply(c3).add(M[1][1].multiply(c1)).subtract(M[1][2].multiply(c0)).multiply(invdet);
        Mn[3][1] = M[0][0].multiply(c3).subtract(M[0][1].multiply(c1)).add(M[0][2].multiply(c0)).multiply(invdet);
        Mn[3][2] = M[3][0].negate().multiply(s3).add(M[3][1].multiply(s1)).subtract(M[3][2].multiply(s0)).multiply(invdet);
        Mn[3][3] = M[2][0].multiply(s3).subtract(M[2][1].multiply(s1)).add(M[2][2].multiply(s0)).multiply(invdet);

        return new Matrix(Mn);
    }

    public BigDecimal[] transform(BigDecimal X, BigDecimal Y, BigDecimal Z) {
        final MathContext mc = Threshold.mc;
        final BigDecimal[] result = new BigDecimal[4];
        for (int row = 0; row < 4; row++) {
            final BigDecimal P1 = this.M[0][row].multiply(X, mc);
            final BigDecimal P2 = this.M[1][row].multiply(Y, mc);
            final BigDecimal P3 = this.M[2][row].multiply(Z, mc);
            final BigDecimal P4 = this.M[3][row];
            result[row] = P1.add(P2, mc).add(P3, mc).add(P4, mc);
        }
        return result;
    }


    public Vector3d transform(Vector3d v) {
        final BigDecimal[] result = transform(v.X, v.Y, v.Z);
        return new Vector3d(result[0], result[1], result[2]);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result.append(this.M[j][i].floatValue());
                result.append(" "); //$NON-NLS-1$
            }
            result.append("\n"); //$NON-NLS-1$
        }
        return result.toString();
    }

    public String toLDrawString() {
        StringBuilder result = new StringBuilder();
        result.append(" "); //$NON-NLS-1$
        result.append(this.M30.doubleValue());
        result.append(" "); //$NON-NLS-1$
        result.append(this.M31.doubleValue());
        result.append(" "); //$NON-NLS-1$
        result.append(this.M32.doubleValue());
        result.append(" "); //$NON-NLS-1$
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result.append(this.M[j][i].doubleValue());
                result.append(" "); //$NON-NLS-1$
            }
        }
        return result.toString();
    }

    public Matrix4f getMatrix4f() {

        Matrix4f result = new Matrix4f();

        result.m00 = M00.floatValue();
        result.m01 = M01.floatValue();
        result.m02 = M02.floatValue();
        result.m03 = M03.floatValue();

        result.m10 = M10.floatValue();
        result.m11 = M11.floatValue();
        result.m12 = M12.floatValue();
        result.m13 = M13.floatValue();

        result.m20 = M20.floatValue();
        result.m21 = M21.floatValue();
        result.m22 = M22.floatValue();
        result.m23 = M23.floatValue();

        result.m30 = M30.floatValue();
        result.m31 = M31.floatValue();
        result.m32 = M32.floatValue();
        result.m33 = M33.floatValue();

        return result;
    }

    public BigDecimal determinant() {

        BigDecimal s0 = M[0][0].multiply(M[1][1]).subtract(M[1][0].multiply(M[0][1]));
        BigDecimal s1 = M[0][0].multiply(M[1][2]).subtract(M[1][0].multiply(M[0][2]));
        BigDecimal s2 = M[0][0].multiply(M[1][3]).subtract(M[1][0].multiply(M[0][3]));
        BigDecimal s3 = M[0][1].multiply(M[1][2]).subtract(M[1][1].multiply(M[0][2]));
        BigDecimal s4 = M[0][1].multiply(M[1][3]).subtract(M[1][1].multiply(M[0][3]));
        BigDecimal s5 = M[0][2].multiply(M[1][3]).subtract(M[1][2].multiply(M[0][3]));

        BigDecimal c5 = M[2][2].multiply(M[3][3]).subtract(M[3][2].multiply(M[2][3]));
        BigDecimal c4 = M[2][1].multiply(M[3][3]).subtract(M[3][1].multiply(M[2][3]));
        BigDecimal c3 = M[2][1].multiply(M[3][2]).subtract(M[3][1].multiply(M[2][2]));
        BigDecimal c2 = M[2][0].multiply(M[3][3]).subtract(M[3][0].multiply(M[2][3]));
        BigDecimal c1 = M[2][0].multiply(M[3][2]).subtract(M[3][0].multiply(M[2][2]));
        BigDecimal c0 = M[2][0].multiply(M[3][1]).subtract(M[3][0].multiply(M[2][1]));

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


        BigDecimal t00 = M00.multiply(f00).add(M10.multiply(f01)).add(M20.multiply(f02));
        BigDecimal t01 = M01.multiply(f00).add(M11.multiply(f01)).add(M21.multiply(f02));
        BigDecimal t02 = M02.multiply(f00).add(M12.multiply(f01)).add(M22.multiply(f02));
        BigDecimal t03 = M03.multiply(f00).add(M13.multiply(f01)).add(M23.multiply(f02));
        BigDecimal t10 = M00.multiply(f10).add(M10.multiply(f11)).add(M20.multiply(f12));
        BigDecimal t11 = M01.multiply(f10).add(M11.multiply(f11)).add(M21.multiply(f12));
        BigDecimal t12 = M02.multiply(f10).add(M12.multiply(f11)).add(M22.multiply(f12));
        BigDecimal t13 = M03.multiply(f10).add(M13.multiply(f11)).add(M23.multiply(f12));
        BigDecimal m20 = M00.multiply(f20).add(M10.multiply(f21)).add(M20.multiply(f22));
        BigDecimal m21 = M01.multiply(f20).add(M11.multiply(f21)).add(M21.multiply(f22));
        BigDecimal m22 = M02.multiply(f20).add(M12.multiply(f21)).add(M22.multiply(f22));
        BigDecimal m23 = M03.multiply(f20).add(M13.multiply(f21)).add(M23.multiply(f22));
        BigDecimal m00 = t00;
        BigDecimal m01 = t01;
        BigDecimal m02 = t02;
        BigDecimal m03 = t03;
        BigDecimal m10 = t10;
        BigDecimal m11 = t11;
        BigDecimal m12 = t12;
        BigDecimal m13 = t13;

        return new Matrix(m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE);
    }

    public Matrix translate(BigDecimal[] vec) {

        BigDecimal m30 = M30.add(M00.multiply(vec[0]).add(M10.multiply(vec[1])).add(M20.multiply(vec[2])));
        BigDecimal m31 = M31.add(M01.multiply(vec[0]).add(M11.multiply(vec[1])).add(M21.multiply(vec[2])));
        BigDecimal m32 = M32.add(M02.multiply(vec[0]).add(M12.multiply(vec[1])).add(M22.multiply(vec[2])));

        return new Matrix(M00, M01, M02, M03, M10, M11, M12, M13, M20, M21, M22, M23, m30, m31, m32, BigDecimal.ONE);
    }

}
