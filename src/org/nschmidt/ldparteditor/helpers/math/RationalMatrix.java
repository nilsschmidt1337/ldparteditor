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
package org.nschmidt.ldparteditor.helpers.math;

import java.math.BigDecimal;

import org.lwjgl.util.vector.Matrix4f;

/**
 * Immutable, absolute precision matrix class
 *
 * @author nils
 *
 */
public class RationalMatrix {

    public final Rational M00;
    public final Rational M01;
    public final Rational M02;
    public final Rational M03;
    public final Rational M10;
    public final Rational M11;
    public final Rational M12;
    public final Rational M13;
    public final Rational M20;
    public final Rational M21;
    public final Rational M22;
    public final Rational M23;
    public final Rational M30;
    public final Rational M31;
    public final Rational M32;
    public final Rational M33;

    private final Rational[][] M = new Rational[4][4];

    public RationalMatrix(Matrix4f m) {
        Rational M00 = new Rational(new BigDecimal(Float.toString(m.m00)));
        Rational M01 = new Rational(new BigDecimal(Float.toString(m.m01)));
        Rational M02 = new Rational(new BigDecimal(Float.toString(m.m02)));
        Rational M03 = new Rational(new BigDecimal(Float.toString(m.m03)));
        Rational M10 = new Rational(new BigDecimal(Float.toString(m.m10)));
        Rational M11 = new Rational(new BigDecimal(Float.toString(m.m11)));
        Rational M12 = new Rational(new BigDecimal(Float.toString(m.m12)));
        Rational M13 = new Rational(new BigDecimal(Float.toString(m.m13)));
        Rational M20 = new Rational(new BigDecimal(Float.toString(m.m20)));
        Rational M21 = new Rational(new BigDecimal(Float.toString(m.m21)));
        Rational M22 = new Rational(new BigDecimal(Float.toString(m.m22)));
        Rational M23 = new Rational(new BigDecimal(Float.toString(m.m23)));
        Rational M30 = new Rational(new BigDecimal(Float.toString(m.m30)));
        Rational M31 = new Rational(new BigDecimal(Float.toString(m.m31)));
        Rational M32 = new Rational(new BigDecimal(Float.toString(m.m32)));
        Rational M33 = new Rational(new BigDecimal(Float.toString(m.m33)));
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

    private RationalMatrix(Rational[][] M) {
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

    public RationalMatrix invert() {
        final Rational[][] Mn = new Rational[4][4];

        Rational s0 = M[0][0].multiply(M[1][1]).subtract(M[1][0].multiply(M[0][1]));
        Rational s1 = M[0][0].multiply(M[1][2]).subtract(M[1][0].multiply(M[0][2]));
        Rational s2 = M[0][0].multiply(M[1][3]).subtract(M[1][0].multiply(M[0][3]));
        Rational s3 = M[0][1].multiply(M[1][2]).subtract(M[1][1].multiply(M[0][2]));
        Rational s4 = M[0][1].multiply(M[1][3]).subtract(M[1][1].multiply(M[0][3]));
        Rational s5 = M[0][2].multiply(M[1][3]).subtract(M[1][2].multiply(M[0][3]));

        Rational c5 = M[2][2].multiply(M[3][3]).subtract(M[3][2].multiply(M[2][3]));
        Rational c4 = M[2][1].multiply(M[3][3]).subtract(M[3][1].multiply(M[2][3]));
        Rational c3 = M[2][1].multiply(M[3][2]).subtract(M[3][1].multiply(M[2][2]));
        Rational c2 = M[2][0].multiply(M[3][3]).subtract(M[3][0].multiply(M[2][3]));
        Rational c1 = M[2][0].multiply(M[3][2]).subtract(M[3][0].multiply(M[2][2]));
        Rational c0 = M[2][0].multiply(M[3][1]).subtract(M[3][0].multiply(M[2][1]));

        // TODO Should check for 0 determinant

        Rational invdet = Rational.ONE.divide(s0.multiply(c5).subtract(s1.multiply(c4)).add(s2.multiply(c3)).add(s3.multiply(c2)).subtract(s4.multiply(c1)).add(s5.multiply(c0)));

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

        return new RationalMatrix(Mn);
    }

    public Vector3r transform(Vector3r relPos) {
        final Rational[] result = new Rational[4];
        for (int row = 0; row < 4; row++) {
            final Rational P1 = this.M[0][row].multiply(relPos.X);
            final Rational P2 = this.M[1][row].multiply(relPos.Y);
            final Rational P3 = this.M[2][row].multiply(relPos.Z);
            final Rational P4 = this.M[3][row];
            result[row] = P1.add(P2).add(P3).add(P4);
        }
        return new Vector3r(result[0], result[1], result[2]);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result.append(this.M[j][i].doubleValue());
                result.append(" "); //$NON-NLS-1$
            }
            result.append("\n"); //$NON-NLS-1$
        }
        return result.toString();
    }
}
