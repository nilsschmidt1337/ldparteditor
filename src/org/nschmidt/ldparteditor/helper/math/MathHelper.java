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
package org.nschmidt.ldparteditor.helper.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.csg.VectorCSGd;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.GData2;
import org.nschmidt.ldparteditor.data.GData3;
import org.nschmidt.ldparteditor.data.GData5;
import org.nschmidt.ldparteditor.data.Matrix;
import org.nschmidt.ldparteditor.enumtype.Threshold;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.helper.LDPartEditorException;
import org.nschmidt.ldparteditor.logger.NLogger;

/**
 * Math Helper Class
 *
 * @author nils
 *
 */
public enum MathHelper {
    INSTANCE;

    private static Random randomizer = new Random(183630263548l);

    public static final BigDecimal R1 = new BigDecimal(".432"); //$NON-NLS-1$
    public static final BigDecimal R2 = new BigDecimal(".256"); //$NON-NLS-1$
    public static final BigDecimal R3 = new BigDecimal(".312"); //$NON-NLS-1$

    /**
     * @return a random float between 0f and 1f
     */
    public static float randomFloat(int id, int argNum) {
        randomizer.setSeed(18363l * Math.abs(id) + argNum * 192732l);
        return randomizer.nextFloat();
    }

    public static float[][] getLineVertices(Vector3f p1, Vector3f p2, Matrix4f pMatrix) {

        Vector3f col1 = new Vector3f(pMatrix.m00, pMatrix.m01, pMatrix.m02);
        Vector3f col2 = new Vector3f(pMatrix.m10, pMatrix.m11, pMatrix.m12);
        Vector3f col3 = new Vector3f(pMatrix.m20, pMatrix.m21, pMatrix.m22);

        float sx = col1.length();
        float sy = col2.length();
        float sz = col3.length();

        if (sx == 0f)
            sx = 1f;
        if (sy == 0f)
            sy = 1f;
        if (sz == 0f)
            sz = 1f;

        float[][] result = new float[21][3];

        result[18][0] = p1.x * 1000f;
        result[18][1] = p1.y * 1000f;
        result[18][2] = p1.z * 1000f;
        result[19][0] = p2.x * 1000f;
        result[19][1] = p2.y * 1000f;
        result[19][2] = p2.z * 1000f;

        Vector4f n = new Vector4f();
        Vector4f p = new Vector4f();
        Vector4f q = new Vector4f();
        Vector4f perp = new Vector4f();

        int j = 0;
        float theta;

        /* Normal pointing from p1 to p2 */
        n.x = p1.x - p2.x;
        n.y = p1.y - p2.y;
        n.z = p1.z - p2.z;

        /*
         * Create two perpendicular vectors perp and q on the plane of the disk
         */

        perp.x = n.x;
        perp.y = n.y;
        perp.z = n.z;
        if (n.x == 0 && n.z == 0)
            perp.x += 1;
        else
            perp.y += 1;
        crossProduct(perp, n, q);
        crossProduct(n, q, perp);
        try {
            perp.normalise();
            q.normalise();
        } catch (IllegalStateException ise) {
            perp.x = 0f;
            perp.y = 0f;
            perp.z = 0f;
            q.x = 0f;
            q.y = 0f;
            q.z = 0f;
        }

        float r1 = View.lineWidth;
        float r2 = r1;

        float twoPI = (float) Math.PI / 4f;
        for (int i = 0; i <= 8; i++) {
            theta = i * twoPI;

            n.x = (float) (Math.cos(theta) * perp.x + Math.sin(theta) * q.x);
            n.y = (float) (Math.cos(theta) * perp.y + Math.sin(theta) * q.y);
            n.z = (float) (Math.cos(theta) * perp.z + Math.sin(theta) * q.z);
            try {
                n.normalise();
            } catch (IllegalStateException ise) {
                n.x = 0f;
                n.y = 0f;
                n.z = 0f;
            }

            p.x = (p1.x * sx + r1 * n.x) * 1000f;
            p.y = (p1.y * sy + r1 * n.y) * 1000f;
            p.z = (p1.z * sz + r1 * n.z) * 1000f;
            result[j][0] = p.x;
            result[j][1] = p.y;
            result[j][2] = p.z;
            j++;

            p.x = (p2.x * sx + r2 * n.x) * 1000f;
            p.y = (p2.y * sy + r2 * n.y) * 1000f;
            p.z = (p2.z * sz + r2 * n.z) * 1000f;
            result[j][0] = p.x;
            result[j][1] = p.y;
            result[j][2] = p.z;
            j++;

        }

        result[18][0] = result[18][0] * sx;
        result[18][1] = result[18][1] * sy;
        result[18][2] = result[18][2] * sz;
        result[19][0] = result[19][0] * sx;
        result[19][1] = result[19][1] * sy;
        result[19][2] = result[19][2] * sz;
        result[20][0] = 1f / sx;
        result[20][1] = 1f / sy;
        result[20][2] = 1f / sz;

        return result;
    }

    public static float[][] getLineVertices1000(Vector3f p1, Vector3f p2, Matrix4f pMatrix) {

        Vector3f col1 = new Vector3f(pMatrix.m00, pMatrix.m01, pMatrix.m02);
        Vector3f col2 = new Vector3f(pMatrix.m10, pMatrix.m11, pMatrix.m12);
        Vector3f col3 = new Vector3f(pMatrix.m20, pMatrix.m21, pMatrix.m22);

        float sx = col1.length();
        float sy = col2.length();
        float sz = col3.length();

        if (sx == 0f)
            sx = 1f;
        if (sy == 0f)
            sy = 1f;
        if (sz == 0f)
            sz = 1f;

        float[][] result = new float[21][3];

        result[18][0] = p1.x;
        result[18][1] = p1.y;
        result[18][2] = p1.z;
        result[19][0] = p2.x;
        result[19][1] = p2.y;
        result[19][2] = p2.z;

        Vector4f n = new Vector4f();
        Vector4f p = new Vector4f();
        Vector4f q = new Vector4f();
        Vector4f perp = new Vector4f();

        int j = 0;
        float theta;

        /* Normal pointing from p1 to p2 */
        n.x = p1.x - p2.x;
        n.y = p1.y - p2.y;
        n.z = p1.z - p2.z;

        /*
         * Create two perpendicular vectors perp and q on the plane of the disk
         */

        perp.x = n.x;
        perp.y = n.y;
        perp.z = n.z;
        if (n.x == 0 && n.z == 0)
            perp.x += 1;
        else
            perp.y += 1;
        crossProduct(perp, n, q);
        crossProduct(n, q, perp);
        try {
            perp.normalise();
            q.normalise();
        } catch (IllegalStateException ise) {
            perp.x = 0f;
            perp.y = 0f;
            perp.z = 0f;
            q.x = 0f;
            q.y = 0f;
            q.z = 0f;
        }

        float r1 = View.lineWidth1000;
        float r2 = r1;

        float twoPI = (float) Math.PI / 4f;
        for (int i = 0; i <= 8; i++) {
            theta = i * twoPI;

            n.x = (float) (Math.cos(theta) * perp.x + Math.sin(theta) * q.x);
            n.y = (float) (Math.cos(theta) * perp.y + Math.sin(theta) * q.y);
            n.z = (float) (Math.cos(theta) * perp.z + Math.sin(theta) * q.z);
            try {
                n.normalise();
            } catch (IllegalStateException ise) {
                n.x = 0f;
                n.y = 0f;
                n.z = 0f;
            }


            p.x = p1.x * sx + r1 * n.x;
            p.y = p1.y * sy + r1 * n.y;
            p.z = p1.z * sz + r1 * n.z;
            result[j][0] = p.x;
            result[j][1] = p.y;
            result[j][2] = p.z;
            j++;

            p.x = p2.x * sx + r2 * n.x;
            p.y = p2.y * sy + r2 * n.y;
            p.z = p2.z * sz + r2 * n.z;
            result[j][0] = p.x;
            result[j][1] = p.y;
            result[j][2] = p.z;
            j++;

        }

        result[18][0] = result[18][0] * sx;
        result[18][1] = result[18][1] * sy;
        result[18][2] = result[18][2] * sz;
        result[19][0] = result[19][0] * sx;
        result[19][1] = result[19][1] * sy;
        result[19][2] = result[19][2] * sz;
        result[20][0] = 1f / sx;
        result[20][1] = 1f / sy;
        result[20][2] = 1f / sz;

        return result;
    }

    /**
     * Calculates the square root of a BigDecimal
     *
     * @param value
     *            the value
     * @return the value's square root
     */
    public static BigDecimal sqrt(BigDecimal value) {
        final BigDecimal two = new BigDecimal(2, Threshold.MC);
        BigDecimal result = value.add(BigDecimal.ONE, Threshold.MC).divide(two, Threshold.MC);
        for (int i = 0; i < 20; i++) { // Ten iterations should be sufficent
            result = result.add(value.divide(result, Threshold.MC), Threshold.MC).divide(two, Threshold.MC);
        }
        return result;
    }

    public static Vector4f getNearestPointToLine(float lineX1, float lineY1, float lineZ1, float lineX2, float lineY2, float lineZ2, float pointX, float pointY, float pointZ) {
        float aX = pointX - lineX1;
        float aY = pointY - lineY1;
        float aZ = pointZ - lineZ1;
        float ux = lineX2 - lineX1;
        float uy = lineY2 - lineY1;
        float uz = lineZ2 - lineZ1;
        float lu = (float) Math.sqrt(ux * ux + uy * uy + uz * uz);
        ux = ux / lu;
        uy = uy / lu;
        uz = uz / lu;
        float dotAu = aX * ux + aY * uy + aZ * uz;
        return new Vector4f(lineX1 + dotAu * ux, lineY1 + dotAu * uy, lineZ1 + dotAu * uz, 1f);
    }

    public static Vector4f getNearestPointToLineSegment(float lx1, float ly1, float lz1, float lx2, float ly2, float lz2, float px, float py, float pz) {

        final Vector4f nearestPointToLine = getNearestPointToLine(lx1, ly1, lz1, lx2, ly2, lz2, px, py, pz);

        final Vector4f dirToL1 = new Vector4f();
        Vector4f.sub(new Vector4f(lx1, ly1, lz1, 1f), nearestPointToLine, dirToL1);
        if (dirToL1.lengthSquared() > 0f) {
            dirToL1.normalise();
        } else {
            return nearestPointToLine;
        }

        final Vector4f dirToL2 = new Vector4f();
        Vector4f.sub(new Vector4f(lx2, ly2, lz2, 1f), nearestPointToLine, dirToL2);
        if (dirToL2.lengthSquared() > 0f) {
            dirToL2.normalise();
        } else {
            return nearestPointToLine;
        }

        final float dotp = Vector4f.dot(dirToL1, dirToL2);

        if (dotp < 0f) {
            return nearestPointToLine;
        }

        final Vector4f pToL1 = new Vector4f();
        Vector4f.sub(new Vector4f(lx1, ly1, lz1, 1f), new Vector4f(px, py, pz, 1f), pToL1);

        final Vector4f pToL2 = new Vector4f();
        Vector4f.sub(new Vector4f(lx2, ly2, lz2, 1f), new Vector4f(px, py, pz, 1f), pToL2);

        if (pToL1.lengthSquared() < pToL2.lengthSquared()) {
            return new Vector4f(lx1, ly1, lz1, 1f);
        } else {
            return new Vector4f(lx2, ly2, lz2, 1f);
        }
    }

    public static boolean canBeProjectedToLineSegmentCSG(VectorCSGd p1, VectorCSGd p2, VectorCSGd p) {
        VectorCSGd v = p2.minus(p1);
        VectorCSGd w = p.minus(p1);
        double t = w.dot(v) / v.dot(v);
        return t > 0.0 && t < 1.0;
    }

    public static double getNearestPointDistanceToLineSegmentCSG(VectorCSGd p1, VectorCSGd p2, VectorCSGd p, double epsilon) {

        VectorCSGd v = p2.minus(p1);
        VectorCSGd w = p.minus(p1);

        double t = w.dot(v) / v.dot(v);
        if (t < 0.0) {
            return 2.0 * epsilon;
        }
        if (t > 1.0) {
            return 2.0 * epsilon;
        }

        VectorCSGd pb = p1.plus(v.times(t));
        return p.minus(pb).magnitude();
    }

    public static Vector4f getNearestPointToLinePoints(float lx1, float ly1, float lz1, float lx2, float ly2, float lz2, float px, float py, float pz) {
        final float dxup = lx1 - px;
        final float dyup = ly1 - py;
        final float dzup = lz1 - pz;
        final float dup = dxup * dxup + dyup * dyup + dzup * dzup;
        final float dxvp = lx2 - px;
        final float dyvp = ly2 - py;
        final float dzvp = lz2 - pz;
        final float dvp = dxvp * dxvp + dyvp * dyvp + dzvp * dzvp;
        if (dup < dvp) {
            return new Vector4f(lx1, ly1, lz1, 1f);
        } else {
            return new Vector4f(lx2, ly2, lz2, 1f);
        }
    }

    public static Vector4f getNearestPointToTriangle(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float px, float py, float pz) {

        // Accurate approach without Eigenvalues

        float xn = (y3 - y1) * (z2 - z1) - (z3 - z1) * (y2 - y1);
        float yn = (z3 - z1) * (x2 - x1) - (x3 - x1) * (z2 - z1);
        float zn = (x3 - x1) * (y2 - y1) - (y3 - y1) * (x2 - x1);

        float nl = (float) Math.sqrt(xn * xn + yn * yn + zn * zn);
        if (nl < .0001f)
            return null;
        xn = xn / nl;
        yn = yn / nl;
        zn = zn / nl;

        float d = (x1 - px) * xn + (y1 - py) * yn + (z1 - pz) * zn;

        float xl = px + d * xn;
        float yl = py + d * yn;
        float zl = pz + d * zn;

        Vector4f closestPoint = new Vector4f();
        double[] solution = new PowerRay().barycentric(new double[] { xl, yl, zl }, // point
                new double[] { xn, yn, zn }, // normal
                new double[] { x1, y1, z1 }, // v1
                new double[] { x2, y2, z2 }, // v2
                new double[] { x3, y3, z3 }, // v3
                closestPoint);
        if (solution.length != 0) {
            return new Vector4f(closestPoint.x, closestPoint.y, closestPoint.z, 1f);
        }

        float dist2;
        float dist3;
        float dist4;

        float dist5;
        float dist6;
        float dist7;

        float ex1;
        float ey1;
        float ez1;

        float ex2;
        float ey2;
        float ez2;

        float ex3;
        float ey3;
        float ez3;

        {
            float dxup = x1 - px;
            float dyup = y1 - py;
            float dzup = z1 - pz;
            dist2 = dxup * dxup + dyup * dyup + dzup * dzup;
        }
        {
            float dxup = x2 - px;
            float dyup = y2 - py;
            float dzup = z2 - pz;
            dist3 = dxup * dxup + dyup * dyup + dzup * dzup;
        }
        {
            float dxup = x3 - px;
            float dyup = y3 - py;
            float dzup = z3 - pz;
            dist4 = dxup * dxup + dyup * dyup + dzup * dzup;
        }

        {
            Vector4f p = MathHelper.getNearestPointToLineSegment(x1, y1, z1, x2, y2, z2, px, py, pz);
            ex1 = p.x;
            ey1 = p.y;
            ez1 = p.z;
            float dxup = p.x - px;
            float dyup = p.y - py;
            float dzup = p.z - pz;
            dist5 = dxup * dxup + dyup * dyup + dzup * dzup;
        }
        {
            Vector4f p = MathHelper.getNearestPointToLineSegment(x2, y2, z2, x3, y3, z3, px, py, pz);
            ex2 = p.x;
            ey2 = p.y;
            ez2 = p.z;
            float dxup = p.x - px;
            float dyup = p.y - py;
            float dzup = p.z - pz;
            dist6 = dxup * dxup + dyup * dyup + dzup * dzup;
        }
        {
            Vector4f p = MathHelper.getNearestPointToLineSegment(x3, y3, z3, x1, y1, z1, px, py, pz);
            ex3 = p.x;
            ey3 = p.y;
            ez3 = p.z;
            float dxup = p.x - px;
            float dyup = p.y - py;
            float dzup = p.z - pz;
            dist7 = dxup * dxup + dyup * dyup + dzup * dzup;
        }
        SortedSet<Float> ts = new TreeSet<>();
        ts.add(dist2);
        ts.add(dist3);
        ts.add(dist4);
        ts.add(dist5);
        ts.add(dist6);
        ts.add(dist7);
        float first = ts.first();
        if (first == dist5) {
            return new Vector4f(ex1, ey1, ez1, 1f);
        } else if (first == dist6) {
            return new Vector4f(ex2, ey2, ez2, 1f);
        } else if (first == dist7) {
            return new Vector4f(ex3, ey3, ez3, 1f);
        } else if (first == dist2) {
            return new Vector4f(x1, y1, z1, 1f);
        } else if (first == dist3) {
            return new Vector4f(x2, y2, z2, 1f);
        }
        return new Vector4f(x3, y3, z3, 1f);
    }

    public static Vector4f intersectionBetweenTwoLinesStrict2D(Vector4f v11, Vector4f v12, Vector4f v21, Vector4f v22) {
        if (v11.x == v21.x && v11.y == v21.y || v12.x == v22.x && v12.y == v22.y)
            return null;
        if (v11.x == v22.x && v11.y == v22.y || v12.x == v21.x && v12.y == v21.y)
            return null;

        float s1X;
        float s1Y;
        float s2X;
        float s2Y;
        s1X = v12.x - v11.x;
        s1Y = v12.y - v11.y;

        s2X = v22.x - v21.x;
        s2Y = v22.y - v21.y;

        float s;
        float t;
        s = (-s1Y * (v11.x - v21.x) + s1X * (v11.y - v21.y)) / (-s2X * s1Y + s1X * s2Y);
        t = (s2X * (v11.y - v21.y) - s2Y * (v11.x - v21.x)) / (-s2X * s1Y + s1X * s2Y);

        if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {
            return new Vector4f(v11.x + t * s1X, v11.y + t * s1Y, 0f, 1f);
        }
        return null;
    }

    /**
     * Multiplies the target vector with a scalar
     *
     * @param value
     * @param target
     */
    public static void crossProduct(Vector4f left, Vector4f right, Vector4f target) {
        target.set(left.y * right.z - left.z * right.y, left.z * right.x - left.x * right.z, left.x * right.y - left.y * right.x, 1f);
    }

    /**
     * Returns a "direction" indicator from a pair of vectors [sgn(v1*v2)]
     *
     * @param vector1
     * @param vector2
     * @return -1 if vector1 * vector2 is less than 0, 1 otherwise.
     */
    public static float directionOfVectors(Vector3d vector1, Vector3d vector2) {
        Vector3d normal1 = new Vector3d();
        Vector3d normal2 = new Vector3d();
        vector1.normalise(normal1);
        vector2.normalise(normal2);
        if (Vector3d.dot(normal1, normal2) < 0) {
            return -1;
        }
        return 1;
    }

    public static float directionOfVectors(Vector3f vector1, Vector3f vector2) {
        Vector3f normal1 = new Vector3f(vector1);
        Vector3f normal2 = new Vector3f(vector2);
        if (normal1.lengthSquared() > 0f)
            normal1.normalise();
        if (normal2.lengthSquared() > 0f)
            normal2.normalise();
        if (Vector3f.dot(normal1, normal2) < 0) {
            return -1;
        }
        return 1;
    }

    /**
     * Does the gaussian elimination
     *
     * @param a
     *            coefficient matrix
     * @param b
     *            value vector
     * @return solution
     */
    public static float[] gaussianElimination(float[][] a, float[] b) {
        int n = b.length; // Very compact gaussian elimination
        for (int p = 0; p < n; p++) {
            int max = p;
            for (int i = p + 1; i < n; i++)
                if (Math.abs(a[i][p]) > Math.abs(a[max][p]))
                    max = i;
            float[] temp = a[p];
            a[p] = a[max];
            a[max] = temp;
            float t = b[p];
            b[p] = b[max];
            b[max] = t;
            if (Math.abs(a[p][p]) < 0.000001f) {
                try {
                    // Throw this exception and catch it immediately to track where the error occurred.
                    throw new LDPartEditorException(new RuntimeException("Matrix is singular or nearly singular")); //$NON-NLS-1$
                } catch (LDPartEditorException ex) {
                    NLogger.debug(MathHelper.class, ex);
                }
                return new float[0];
            }
            for (int i = p + 1; i < n; i++) {
                float alpha = a[i][p] / a[p][p];
                b[i] -= alpha * b[p];
                for (int j = p; j < n; j++)
                    a[i][j] -= alpha * a[p][j];
            }
        }
        float[] x = new float[n];
        for (int i = n - 1; i >= 0; i--) {
            float sum = 0f;
            for (int j = i + 1; j < n; j++)
                sum += a[i][j] * x[j];
            x[i] = (b[i] - sum) / a[i][i];
        }
        return x;
    }

    @SuppressWarnings("java:S2111")
    public static String matrixToString(Matrix4f matrix, int coordsDecimalPlaces, int matrixDecimalPlaces, final boolean onX,  final boolean onY,  final boolean onZ) {
        StringBuilder lineBuilder = new StringBuilder();
        lineBuilder.append(bigDecimalToString(onX ? new BigDecimal(matrix.m30 / 1000f).setScale(coordsDecimalPlaces, RoundingMode.HALF_UP) : new BigDecimal(matrix.m30 / 1000f)));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(onY ? new BigDecimal(matrix.m31 / 1000f).setScale(coordsDecimalPlaces, RoundingMode.HALF_UP) : new BigDecimal(matrix.m31 / 1000f)));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(onZ ? new BigDecimal(matrix.m32 / 1000f).setScale(coordsDecimalPlaces, RoundingMode.HALF_UP) : new BigDecimal(matrix.m32 / 1000f)));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(new BigDecimal(matrix.m00).setScale(matrixDecimalPlaces, RoundingMode.HALF_UP)));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(new BigDecimal(matrix.m10).setScale(matrixDecimalPlaces, RoundingMode.HALF_UP)));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(new BigDecimal(matrix.m20).setScale(matrixDecimalPlaces, RoundingMode.HALF_UP)));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(new BigDecimal(matrix.m01).setScale(matrixDecimalPlaces, RoundingMode.HALF_UP)));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(new BigDecimal(matrix.m11).setScale(matrixDecimalPlaces, RoundingMode.HALF_UP)));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(new BigDecimal(matrix.m21).setScale(matrixDecimalPlaces, RoundingMode.HALF_UP)));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(new BigDecimal(matrix.m02).setScale(matrixDecimalPlaces, RoundingMode.HALF_UP)));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(new BigDecimal(matrix.m12).setScale(matrixDecimalPlaces, RoundingMode.HALF_UP)));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(new BigDecimal(matrix.m22).setScale(matrixDecimalPlaces, RoundingMode.HALF_UP)));
        return lineBuilder.toString();
    }

    public static String matrixToString(Matrix4f matrix) {
        StringBuilder lineBuilder = new StringBuilder();
        lineBuilder.append(floatToString(matrix.m30 / 1000f));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(floatToString(matrix.m31 / 1000f));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(floatToString(matrix.m32 / 1000f));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(floatToString(matrix.m00));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(floatToString(matrix.m10));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(floatToString(matrix.m20));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(floatToString(matrix.m01));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(floatToString(matrix.m11));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(floatToString(matrix.m21));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(floatToString(matrix.m02));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(floatToString(matrix.m12));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(floatToString(matrix.m22));
        return lineBuilder.toString();
    }

    private static String floatToString(float flt) {
        String result;
        if (flt == (int) flt) {
            result = String.format("%d", (int) flt); //$NON-NLS-1$
        } else {
            result = String.format("%s", flt); //$NON-NLS-1$
        }
        if (result.equals("0.0"))result = "0"; //$NON-NLS-1$ //$NON-NLS-2$
        if (result.startsWith("-0."))return "-" + result.substring(2); //$NON-NLS-1$ //$NON-NLS-2$
        if (result.startsWith("0."))return result.substring(1); //$NON-NLS-1$
        return result;
    }

    public static String bigDecimalToString(BigDecimal bd) {
        String result;
        if (bd.compareTo(BigDecimal.ZERO) == 0)
            return "0"; //$NON-NLS-1$
        BigDecimal bd2 = bd.stripTrailingZeros();
        result = bd2.toPlainString();
        if (result.startsWith("-0."))return "-" + result.substring(2); //$NON-NLS-1$ //$NON-NLS-2$
        if (result.startsWith("0."))return result.substring(1); //$NON-NLS-1$
        return result;
    }

    public static String toHex(int decimal) {
        String result = Integer.toHexString(decimal);
        if (result.length() == 1)
            result = "0" + result; //$NON-NLS-1$
        return result;
    }

    public static String csgMatrixMult(Matrix4f csg, Matrix4f loc) {
        Matrix4f tmp = new Matrix4f();
        Matrix4f.setIdentity(tmp);

        tmp.m30 = csg.m30;
        tmp.m31 = csg.m31;
        tmp.m32 = csg.m32;

        tmp.m00 = csg.m00;
        tmp.m10 = csg.m01;
        tmp.m20 = csg.m02;

        tmp.m01 = csg.m10;
        tmp.m11 = csg.m11;
        tmp.m21 = csg.m12;

        tmp.m02 = csg.m20;
        tmp.m12 = csg.m21;
        tmp.m22 = csg.m22;

        return matrixToString(Matrix4f.mul(loc, tmp, null));
    }

    public static String matrixToStringPrecise(Matrix matrix) {
        StringBuilder lineBuilder = new StringBuilder();
        lineBuilder.append(bigDecimalToString(matrix.m30));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(matrix.m31));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(matrix.m32));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(matrix.m00));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(matrix.m10));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(matrix.m20));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(matrix.m01));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(matrix.m11));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(matrix.m21));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(matrix.m02));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(matrix.m12));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(matrix.m22));
        return lineBuilder.toString();
    }

    public static BigDecimal max(BigDecimal a, BigDecimal b) {
        if (a.compareTo(b) < 0) {
            return b;
        } else {
            return a;
        }
    }

    static BigDecimal pi = new BigDecimal("3.14159265358979323846264338327950288419716939937510582097494459230781640628620" //$NON-NLS-1$
            + "899862803482534211706798214808651328230664709384460955058223172535940812848111" //$NON-NLS-1$
            + "745028410270193852110555964462294895493038196442881097566593344612847564823378" //$NON-NLS-1$
            + "678316527120190914564856692346034861045432664821339360726024914127372458700660" //$NON-NLS-1$
            + "631558817488152092096282925409171536436789259036001133053054882046652138414695" //$NON-NLS-1$
            + "194151160943305727036575959195309218611738193261179310511854807446237996274956" //$NON-NLS-1$
            + "735188575272489122793818301194912983367336244065664308602139494639522473719070" //$NON-NLS-1$
            + "217986094370277053921717629317675238467481846766940513200056812714526356082778" //$NON-NLS-1$
            + "577134275778960917363717872146844090122495343014654958537105079227968925892354" //$NON-NLS-1$
            + "201995611212902196086403441815981362977477130996051870721134999999837297804995" //$NON-NLS-1$
            + "105973173281609631859502445945534690830264252230825334468503526193118817101000" //$NON-NLS-1$
            + "313783875288658753320838142061717766914730359825349042875546873115956286388235" //$NON-NLS-1$
            + "378759375195778185778053217122680661300192787661119590921642019893809525720106" //$NON-NLS-1$
            + "548586327886593615338182796823030195203530185296899577362259941389124972177528" //$NON-NLS-1$
            + "347913151557485724245415069595082953311686172785588907509838175463746493931925" //$NON-NLS-1$
            + "506040092770167113900984882401285836160356370766010471018194295559619894676783" //$NON-NLS-1$
            + "744944825537977472684710404753464620804668425906949129331367702898915210475216" //$NON-NLS-1$
            + "205696602405803815019351125338243003558764024749647326391419927260426992279678" //$NON-NLS-1$
            + "235478163600934172164121992458631503028618297455570674983850549458858692699569" //$NON-NLS-1$
            + "092721079750930295532116534498720275596023648066549911988183479775356636980742" //$NON-NLS-1$
            + "654252786255181841757467289097777279380008164706001614524919217321721477235014"); //$NON-NLS-1$

    /**
     * PI.
     *
     * @param mc
     *            The required precision of the result.
     * @return 3.14159...
     */
    public static BigDecimal pi(final MathContext mc) {
        // look it up if possible
        if (mc.getPrecision() < pi.precision()) {
            return pi.round(mc);
        } else {
            // Broadhurst
            int[] a = { 1, 0, 0, -1, -1, -1, 0, 0 };
            BigDecimal s = broadhurstBBP(1, 1, a, mc);
            return multiplyRound(s, 8);
        }
    } /* BigDecimalMath.pi */

    /**
     * Trigonometric sine.
     *
     * @param x
     *            The argument in radians.
     * @return sin(x) in the range -1 to 1.
     */
    public static BigDecimal sin(final BigDecimal x) {
        if (x.compareTo(BigDecimal.ZERO) < 0) {
            return sin(x.negate()).negate();
        } else if (x.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        } else {
            /*
             * reduce modulo 2pi
             */
            BigDecimal res = mod2pi(x);
            MathContext mc = new MathContext(2 + err2prec());
            BigDecimal p = pi(mc);
            mc = Threshold.MC;
            if (res.compareTo(p) > 0) {
                /*
                 * pi<x<=2pi: sin(x)= - sin(x-pi)
                 */
                return sin(subtractRound(res, p)).negate();
            } else if (res.multiply(new BigDecimal("2")).compareTo(p) > 0) { //$NON-NLS-1$
                /*
                 * pi/2<x<=pi: sin(x)= sin(pi-x)
                 */
                return sin(subtractRound(p, res));
            } else {
                /*
                 * for the range 0<=x<Pi/2 one could use sin(2x)=2sin(x)cos(x)
                 * to split this further. Here, use the sine up to pi/4 and the
                 * cosine higher up.
                 */
                if (res.multiply(new BigDecimal("4")).compareTo(p) > 0) { //$NON-NLS-1$
                    /*
                     * x>pi/4: sin(x) = cos(pi/2-x)
                     */
                    return cos(subtractRound(p.divide(new BigDecimal("2")), res)); //$NON-NLS-1$
                } else {
                    // Simple Taylor expansion
                    BigDecimal resul = res;
                    /* x^i */
                    BigDecimal xpowi = res;
                    /* 2i+1 factorial */
                    BigInteger ifac = BigInteger.ONE;
                    /*
                     * The error in the result is set by the error in x itself.
                     */
                    double xUlpDbl = res.ulp().doubleValue();
                    MathContext mcTay = new MathContext(err2prec());
                    for (int i = 1;; i++) {
                        /*
                         * TBD: at which precision will 2*i or 2*i+1 overflow?
                         */
                        ifac = ifac.multiply(new BigInteger("" + 2 * i)); //$NON-NLS-1$
                        ifac = ifac.multiply(new BigInteger("" + (2 * i + 1))); //$NON-NLS-1$
                        xpowi = xpowi.multiply(res).multiply(res).negate();
                        BigDecimal corr = xpowi.divide(new BigDecimal(ifac), mcTay);
                        resul = resul.add(corr);
                        if (corr.abs().doubleValue() < 0.05 * xUlpDbl) {
                            break;
                        }
                    }
                    /*
                     * The error in the result is set by the error in x itself.
                     */
                    return resul.round(mc);
                }
            }
        } /* sin */
    }

    /**
     * Trigonometric cosine.
     *
     * @param x
     *            The argument in radians.
     * @return cos(x) in the range -1 to 1.
     */
    public static BigDecimal cos(final BigDecimal x) {
        if (x.compareTo(BigDecimal.ZERO) < 0) {
            return cos(x.negate());
        } else if (x.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ONE;
        } else {
            /*
             * reduce modulo 2pi
             */
            BigDecimal res = mod2pi(x);
            MathContext mc = new MathContext(2 + err2prec());
            BigDecimal p = pi(mc);
            if (res.compareTo(p) > 0) {
                /*
                 * pi<x<=2pi: cos(x)= - cos(x-pi)
                 */
                return cos(subtractRound(res, p)).negate();
            } else if (res.multiply(new BigDecimal("2")).compareTo(p) > 0) { //$NON-NLS-1$
                /*
                 * pi/2<x<=pi: cos(x)= -cos(pi-x)
                 */
                return cos(subtractRound(p, res)).negate();
            } else {
                /*
                 * for the range 0<=x<Pi/2 one could use cos(2x)= 1-2*sin^2(x)
                 * to split this further, or use the cos up to pi/4 and the sine
                 * higher up.
                 */
                if (res.multiply(new BigDecimal("4")).compareTo(p) > 0) { //$NON-NLS-1$
                    /*
                     * x>pi/4: cos(x) = sin(pi/2-x)
                     */
                    return sin(subtractRound(p.divide(new BigDecimal("2")), res)); //$NON-NLS-1$
                } else {
                    // Simple Taylor expansion
                    BigDecimal resul = BigDecimal.ONE;
                    /* x^i */
                    BigDecimal xpowi = BigDecimal.ONE;
                    /* 2i factorial */
                    BigInteger ifac = BigInteger.ONE;
                    /*
                     * The absolute error in the result is the error in x^2/2
                     * which is x times the error in x.
                     */
                    double xUlpDbl = 0.5 * res.ulp().doubleValue() * res.doubleValue();
                    MathContext mcTay = new MathContext(err2prec());
                    for (int i = 1;; i++) {
                        /*
                         * TBD: at which precision will 2*i-1 or 2*i overflow?
                         */
                        ifac = ifac.multiply(new BigInteger("" + (2 * i - 1))); //$NON-NLS-1$
                        ifac = ifac.multiply(new BigInteger("" + 2 * i)); //$NON-NLS-1$
                        xpowi = xpowi.multiply(res).multiply(res).negate();
                        BigDecimal corr = xpowi.divide(new BigDecimal(ifac), mcTay);
                        resul = resul.add(corr);
                        if (corr.abs().doubleValue() < 0.05 * xUlpDbl) {
                            break;
                        }
                    }
                    /*
                     * The error in the result is governed by the error in x
                     * itself.
                     */
                    mc = new MathContext(err2prec());
                    return resul.round(mc);
                }
            }
        }
    } /* BigDecimalMath.cos */

    /**
     * Reduce value to the interval [0,2*Pi].
     *
     * @param x
     *            the original value
     * @return the value modulo 2*pi in the interval from 0 to 2*pi.
     */
    private static BigDecimal mod2pi(BigDecimal x) {
        MathContext mc = new MathContext(2 + err2prec());
        BigDecimal twopi = pi(mc).multiply(new BigDecimal(2));
        /*
         * Delegate the actual operation to the BigDecimal class, which may
         * return a negative value of x was negative .
         */
        BigDecimal res = x.remainder(twopi);

        if (res.compareTo(BigDecimal.ZERO) < 0) {
            res = res.add(twopi);
        }
        /*
         * The actual precision is set by the input value, its absolute value of
         * x.ulp()/2.
         */
        mc = new MathContext(err2prec());

        return res.round(mc);

    } /* mod2pi */

    /**
     * Broadhurst ladder sequence.
     *
     * @param a
     *            The vector of 8 integer arguments
     * @param mc
     *            Specification of the accuracy of the result
     * @return S_(n,p)(a)
     * @see \protect\vrule
     *      width0pt\protect\href{http://arxiv.org/abs/math/9803067
     *      }{arXiv:math/9803067}
     */
    private static BigDecimal broadhurstBBP(final int n, final int p, final int[] a, MathContext mc) {
        /*
         * Explore the actual magnitude of the result first with a quick
         * estimate.
         */
        double x = 0.0;

        for (int k = 1; k < 10; k++) {
            x += a[(k - 1) % 8] / Math.pow(2d, p * (k + 1) / 2d) / Math.pow(k, n);
        }
        /*
         * Convert the relative precision and estimate of the result into an
         * absolute precision.
         */

        double eps = prec2err(x, mc.getPrecision());
        /*
         * Divide this through the number of terms in the sum to account for
         * error accumulation The divisor 2^(p(k+1)/2) means that on the average
         * each 8th term in k has shrunk by relative to the 8th predecessor by
         * 1/2^(4p). 1/2^(4pc) = 10^(-precision) with c the 8term cycles yields
         * c=log_2( 10^precision)/4p = 3.3*precision/4p with k=8c
         */

        int kmax = (int) (6.6 * mc.getPrecision() / p);
        /* Now eps is the absolute error in each term */
        eps /= kmax;
        BigDecimal res = BigDecimal.ZERO;

        for (int c = 0;; c++) {
            Rational r = new Rational();

            for (int k = 0; k < 8; k++) {
                Rational tmp = new Rational(new BigInteger("" + a[k]), new BigInteger("" + (1 + 8 * c + k)).pow(n)); //$NON-NLS-1$ //$NON-NLS-2$
                /*
                 * floor( (pk+p)/2)
                 */

                int pk1h = p * (2 + 8 * c + k) / 2;
                tmp = tmp.divide(BigInteger.ONE.shiftLeft(pk1h));
                r = r.add(tmp);

            }
            if (Math.abs(r.doubleValue()) < eps) {
                break;
            }
            MathContext mcloc = new MathContext(1 + err2prec());
            res = res.add(r.bigDecimalValue(mcloc));

        }
        return res.round(mc);

    } /* broadhurstBBP */

    /**
     * Subtract and round according to the larger of the two ulp’s.
     *
     * @param x
     *            The left term.
     * @param y
     *            The right term.
     * @return The difference x-y.
     */
    private static BigDecimal subtractRound(final BigDecimal x, final BigDecimal y) {
        BigDecimal resul = x.subtract(y);
        MathContext mc = new MathContext(err2prec());
        return resul.round(mc);

    } /* subtractRound */

    /**
     * Multiply and round.
     *
     * @param x
     *            The left factor.
     * @param n
     *            The right factor.
     * @return The product x*n.
     */
    private static BigDecimal multiplyRound(final BigDecimal x, final int n) {
        BigDecimal resul = x.multiply(new BigDecimal(n));
        /*
         * The estimation of the absolute error in the result is |n*err(x)|
         */
        MathContext mc = new MathContext(n != 0 ? x.precision() : 0);

        return resul.round(mc);

    }

    /**
     * Convert a relative error to a precision.
     *
     * @return The number of valid digits in x.
     */
    private static int err2prec() {
        /*
         * Example: an error of xerr=+-0.5 a precision of 1 (digit), an error of
         * +-0.05 a precision of 2 (digits)
         */
        return Threshold.SIGNIFICANT_PLACES;
    }

    /**
     * Convert a precision (relative error) to an absolute error. The is the
     * inverse functionality of err2prec().
     *
     * @param x
     *            The value of the variable The value returned depends only on
     *            the absolute value, not on the sign.
     * @param prec
     *            The number of valid digits of the variable.
     * @return the absolute error in x. Derived from the an accuracy of one half
     *         of the ulp.
     */
    private static double prec2err(final double x, final int prec) {
        return 5. * Math.abs(x) * Math.pow(10., -prec);

    }

    public static List<GData3> triangulatePointGroups(List<GColour> cols, List<Vector3dd> av, List<Integer> types, GData1 dummyReference,
            DatFile df) {
        List<GData3> result = new ArrayList<>();
        int counter = 0;
        int counter2 = 0;
        for (GColour c : cols) {
            switch (types.get(counter2)) {
            case 0:
                Vector3d v1 = av.get(counter);
                Vector3d v2 = av.get(counter + 1);
                Vector3d v3 = av.get(counter + 2);
                counter += 3;

                result.add(new GData3(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(),
                        v1.x,
                        v1.y,
                        v1.z,
                        v2.x,
                        v2.y,
                        v2.z,
                        v3.x,
                        v3.y,
                        v3.z,
                        dummyReference, df, true));
                break;
            case 1:
                counter += 2;
                break;
            case 2:
                counter += 4;
                break;
            default:
                break;
            }
            counter2 += 1;
        }
        return result;
    }

    public static List<GData2> triangulatePointGroups2(List<GColour> cols, List<Vector3dd> av, List<Integer> types, GData1 dummyReference,
            DatFile df) {
        List<GData2> result = new ArrayList<>();
        int counter = 0;
        int counter2 = 0;
        for (GColour c : cols) {
            switch (types.get(counter2)) {
            case 0:
                counter += 3;
                break;
            case 1:
                Vector3d v1 = av.get(counter);
                Vector3d v2 = av.get(counter + 1);
                counter += 2;
                result.add(new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(),
                        v1.x,
                        v1.y,
                        v1.z,
                        v2.x,
                        v2.y,
                        v2.z,
                        dummyReference, df, true));
                break;
            case 2:
                counter += 4;
                break;
            default:
                break;
            }
            counter2 += 1;
        }
        return result;
    }

    public static List<GData5> triangulatePointGroups5(List<GColour> cols, List<Vector3dd> av, List<Integer> types, GData1 dummyReference,
            DatFile df) {
        List<GData5> result = new ArrayList<>();
        int counter = 0;
        int counter2 = 0;
        for (GColour c : cols) {
            switch (types.get(counter2)) {
            case 0:
                counter += 3;
                break;
            case 1:
                counter += 2;
                break;
            case 2:
                Vector3d v1 = av.get(counter);
                Vector3d v2 = av.get(counter + 1);
                Vector3d v3 = av.get(counter + 2);
                Vector3d v4 = av.get(counter + 3);
                counter += 4;
                result.add(new GData5(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(),
                        v1.x,
                        v1.y,
                        v1.z,
                        v2.x,
                        v2.y,
                        v2.z,
                        v3.x,
                        v3.y,
                        v3.z,
                        v4.x,
                        v4.y,
                        v4.z,
                        dummyReference, df));
                break;
            default:
                break;
            }
            counter2 += 1;
        }
        return result;
    }

    public static List<GData3> triangulateNPoints(int colourNumber, float r, float g, float b, float a, int pointsToTriangulate, List<Vector3d> av, GData1 dummyReference,
            DatFile df) {
        List<GData3> result = new ArrayList<>(pointsToTriangulate);
        final Vector3d origin = av.get(0);
        final BigDecimal originX = origin.x;
        final BigDecimal originY = origin.y;
        final BigDecimal originZ = origin.z;

        pointsToTriangulate--;

        Vector3d p1 = av.get(1);
        Vector3d p2 = av.get(2);
        BigDecimal x1 = p1.x;
        BigDecimal y1 = p1.y;
        BigDecimal z1 = p1.z;

        BigDecimal x2 = p2.x;
        BigDecimal y2 = p2.y;
        BigDecimal z2 = p2.z;

        result.add(new GData3(colourNumber, r, g, b, a,
                originX,
                originY,
                originZ,
                x1,
                y1,
                z1,
                x2,
                y2,
                z2,
                dummyReference, df, true));

        for (int i = 2; i < pointsToTriangulate; i++) {

            p1 = av.get(i + 1);

            x1 = x2;
            y1 = y2;
            z1 = z2;

            x2 = p1.x;
            y2 = p1.y;
            z2 = p1.z;

            result.add(new GData3(colourNumber, r, g, b, a,
                    originX,
                    originY,
                    originZ,
                    x1,
                    y1,
                    z1,
                    x2,
                    y2,
                    z2,
                    dummyReference, df, true));
        }
        return result;
    }

    public static boolean hasNarrowAngleDistribution(
            VectorCSGd ta, VectorCSGd tb, VectorCSGd tc,
            VectorCSGd oa, VectorCSGd ob, VectorCSGd oc,
            VectorCSGd na, VectorCSGd nb, VectorCSGd nc,
            VectorCSGd noa, VectorCSGd nob, VectorCSGd noc) {

        double[] tangles = getTriangleAngles(ta, tb, tc);
        double[] oangles = getTriangleAngles(oa, ob, oc);

        double[] nangles = getTriangleAngles(na, nb, nc);
        double[] noangles = getTriangleAngles(noa, nob, noc);

        double tmax = -Double.MAX_VALUE;
        double tmin = Double.MAX_VALUE;

        double omax = -Double.MAX_VALUE;
        double omin = Double.MAX_VALUE;

        for (int i = 0; i < 3; i++) {
            tmax = Math.max(tmax, tangles[i]);
            tmax = Math.max(tmax, oangles[i]);
            tmin = Math.min(tmin, tangles[i]);
            tmin = Math.min(tmin, oangles[i]);
            omax = Math.max(omax, nangles[i]);
            omax = Math.max(omax, noangles[i]);
            omin = Math.min(omin, nangles[i]);
            omin = Math.min(omin, noangles[i]);
        }

        return omax - omin < tmax - tmin;
    }

    @SuppressWarnings("java:S2111")
    private static double[] getTriangleAngles(VectorCSGd pa, VectorCSGd pb, VectorCSGd pc) {
        double[] result = new double[3];
        Vector3d a = new Vector3d(new BigDecimal(pa.x), new BigDecimal(pa.y), new BigDecimal(pa.z));
        Vector3d b = new Vector3d(new BigDecimal(pb.x), new BigDecimal(pb.y), new BigDecimal(pb.z));
        Vector3d c = new Vector3d(new BigDecimal(pc.x), new BigDecimal(pc.y), new BigDecimal(pc.z));
        Vector3d ab = Vector3d.sub(b, a);
        Vector3d bc = Vector3d.sub(c, b);
        Vector3d ac = Vector3d.sub(c, a);

        result[0] = Vector3d.fastAngle(ab, ac);
        ab.negate();
        result[1] = Vector3d.fastAngle(bc, ab);
        result[2] = 180.0 - result[0] - result[1];

        return result;
    }
}
