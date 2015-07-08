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
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeSet;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.GData2;
import org.nschmidt.ldparteditor.data.GData3;
import org.nschmidt.ldparteditor.data.GData5;
import org.nschmidt.ldparteditor.data.Matrix;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.enums.View;

/**
 * Math Helper Class
 *
 * @author nils
 *
 */
public enum MathHelper {
    INSTANCE;

    private static Random randomizer = new Random(183630263548l);

    public static final BigDecimal THREE = new BigDecimal(3);
    public static final BigDecimal FOUR = new BigDecimal(4);

    public static final BigDecimal R1 = new BigDecimal(".432"); //$NON-NLS-1$
    public static final BigDecimal R2 = new BigDecimal(".256"); //$NON-NLS-1$
    public static final BigDecimal R3 = new BigDecimal(".312"); //$NON-NLS-1$

    /**
     * @return a random float between 0f and 1f
     */
    public static float randomFloat(int ID, int argNum) {
        randomizer.setSeed(18363 * Math.abs(ID) + argNum * 192732);
        return randomizer.nextFloat();
    }

    /**
     * Performs an UNCHECKED cast
     *
     * @param x
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object x) {
        return (T) x;
    }

    /**
     * Snaps a value to a grid
     *
     * @param value
     *            the value
     * @param gridWidth
     *            the width of the grid
     * @return the rounded value
     */
    public static float snapToGrid(float value, float gridWidth) {
        return Math.round((double) value / gridWidth) * gridWidth;
    }

    /**
     * Rounds a vectors coordinates to
     *
     * @param vector
     *            the vector
     * @param digits
     *            the number of digits
     * @return the rounded value
     */
    public static Vector4f round(Vector4f value, float digits) {
        float factor = (float) Math.pow(10.0, digits);
        value.set(Math.round(value.x * factor) / factor, Math.round(value.y * factor) / factor, Math.round(value.z * factor) / factor, 1f);
        return value;
    }

    /**
     * Return the three component vectors v1, v2, v3 of the {@code target}, with
     * v1 + v2 + v3 = target.
     *
     * @param basis_1
     * @param basis_2
     * @param basis_3
     * @param target
     * @return array of component vectors
     */
    public static Vector4f[] linearComponents4f(Vector4f basis_1, Vector4f basis_2, Vector4f basis_3, Vector4f target) {
        float[][] A = new float[][] { { basis_1.x, basis_2.x, basis_3.x }, { basis_1.y, basis_2.y, basis_3.y }, { basis_1.z, basis_2.z, basis_3.z } };
        float[] b = new float[] { target.x, target.y, target.z };
        float[] factors = gaussianElimination(A, b);
        Vector4f[] result = new Vector4f[] { new Vector4f(basis_1.x * factors[0], basis_1.y * factors[0], basis_1.z * factors[0], 1f),
                new Vector4f(basis_2.x * factors[1], basis_2.y * factors[1], basis_2.z * factors[1], 1f), new Vector4f(basis_3.x * factors[2], basis_3.y * factors[2], basis_3.z * factors[2], 1f) };
        return result;
    }

    public static float[][] getScaleFactorArray(Matrix4f pMatrix) {

        Vector3f col1 = new Vector3f(pMatrix.m00, pMatrix.m01, pMatrix.m02);
        Vector3f col2 = new Vector3f(pMatrix.m10, pMatrix.m11, pMatrix.m12);
        Vector3f col3 = new Vector3f(pMatrix.m20, pMatrix.m21, pMatrix.m22);

        float[][] result = new float[2][3];
        result[0][0] = col1.length();
        result[0][1] = col2.length();
        result[0][2] = col3.length();

        result[1][0] = 1f / result[0][0];
        result[1][1] = 1f / result[0][1];
        result[1][2] = 1f / result[0][2];
        return result;
    }

    public static float[][] getLineVertices(Vector4f p1, Vector4f p2, Matrix4f pMatrix) {

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

        float[][] result = new float[20][3];

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

        float r1 = View.lineWidth[0];
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

        result[18][0] = sx;
        result[18][1] = sy;
        result[18][2] = sz;
        result[19][0] = 1f / sx;
        result[19][1] = 1f / sy;
        result[19][2] = 1f / sz;

        return result;
    }

    public static float[][] getLineVertices1000(Vector4f p1, Vector4f p2, Matrix4f pMatrix) {

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

        float[][] result = new float[20][3];

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

        float r1 = View.lineWidth1000[0];
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

        result[18][0] = sx;
        result[18][1] = sy;
        result[18][2] = sz;
        result[19][0] = 1f / sx;
        result[19][1] = 1f / sy;
        result[19][2] = 1f / sz;

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
        final BigDecimal TWO = new BigDecimal(2, Threshold.mc);
        BigDecimal result = value.add(BigDecimal.ONE, Threshold.mc).divide(TWO, Threshold.mc);
        for (int i = 0; i < 20; i++) { // Ten iterations should be sufficent
            result = result.add(value.divide(result, Threshold.mc), Threshold.mc).divide(TWO, Threshold.mc);
        }
        return result;
    }

    /**
     * @param cartesian
     *            coordinates
     * @return polar coordinates with x=r y=theta z=phi
     */
    public static Vector4f getPolarCoordinates(Vector4f cartesian) {
        Vector4f result = new Vector4f();
        result.setX((float) Math.sqrt(cartesian.x * cartesian.x + cartesian.y * cartesian.y + cartesian.z * cartesian.z));
        if (result.getX() > 0.0001f) {
            result.setY((float) Math.acos(cartesian.z / result.getX()));
            result.setZ((float) Math.atan2(cartesian.y, cartesian.x));
            return result;
        } else {
            result.setX(0f);
            return result;
        }
    }

    /**
     * @param polar
     *            coordinates
     * @return cartesian coordinates
     */
    public static Vector4f getCartesianCoordinates(Vector4f polar) {
        return new Vector4f((float) (polar.x * Math.sin(polar.y) * Math.cos(polar.z)), (float) (polar.x * Math.sin(polar.y) * Math.sin(polar.z)), (float) (polar.x * Math.cos(polar.y)), 1f);
    }

    public static Vector4f getNearestPointToLine(Vertex line1, Vertex line2, Vertex point) {
        return getNearestPointToLine(line1.x, line1.y, line1.z, line2.x, line2.y, line2.z, point.x, point.y, point.z);
    }

    public static Vector4f getNearestPointToLine(float line_x1, float line_y1, float line_z1, float line_x2, float line_y2, float line_z2, Vertex point) {
        return getNearestPointToLine(line_x1, line_y1, line_z1, line_x2, line_y2, line_z2, point.x, point.y, point.z);
    }

    public static Vector4f getNearestPointToLine(float line_x1, float line_y1, float line_z1, float line_x2, float line_y2, float line_z2, float point_x, float point_y, float point_z) {
        float Ax = point_x - line_x1;
        float Ay = point_y - line_y1;
        float Az = point_z - line_z1;
        float ux = line_x2 - line_x1;
        float uy = line_y2 - line_y1;
        float uz = line_z2 - line_z1;
        float lu = (float) Math.sqrt(ux * ux + uy * uy + uz * uz);
        ux = ux / lu;
        uy = uy / lu;
        uz = uz / lu;
        float dotAu = Ax * ux + Ay * uy + Az * uz;
        // pt1 + (A.dotProduct(u)) * u;
        return new Vector4f(line_x1 + dotAu * ux, line_y1 + dotAu * uy, line_z1 + dotAu * uz, 1f);
    }

    public static Vector4f getNearestPointToLineSegment(Vertex line1, Vertex line2, Vertex point) {
        return getNearestPointToLineSegment(line1.x, line1.y, line1.z, line2.x, line2.y, line2.z, point.x, point.y, point.z);
    }

    public static Vector4f getNearestPointToLineSegment(float line_x1, float line_y1, float line_z1, float line_x2, float line_y2, float line_z2, Vertex point) {
        return getNearestPointToLineSegment(line_x1, line_y1, line_z1, line_x2, line_y2, line_z2, point.x, point.y, point.z);
    }

    public static Vector4f getNearestPointToLineSegment(float lx1, float ly1, float lz1, float lx2, float ly2, float lz2, float px, float py, float pz) {

        // Fastest iterative approach without objects

        // 0th Iteration
        float ax = (lx1 + lx2) / 2f;
        float ay = (ly1 + ly2) / 2f;
        float az = (lz1 + lz2) / 2f;

        // 1st to n-th Iteration
        float ux = lx1;
        float uy = ly1;
        float uz = lz1;
        float vx = lx2;
        float vy = ly2;
        float vz = lz2;

        float dup = 0f;
        float dvp = 1f;

        float dap = 0f;
        float odap = 1f;

        while (Math.abs(dap - odap) > .001f) {
            float dxup = ux - px;
            float dyup = uy - py;
            float dzup = uz - pz;
            dup = dxup * dxup + dyup * dyup + dzup * dzup;
            float dxvp = vx - px;
            float dyvp = vy - py;
            float dzvp = vz - pz;
            dvp = dxvp * dxvp + dyvp * dyvp + dzvp * dzvp;

            if (dup < dvp) {
                vx = ax;
                vy = ay;
                vz = az;
                ax = (ax + ux) / 2f;
                ay = (ay + uy) / 2f;
                az = (az + uz) / 2f;
            } else {
                ux = ax;
                uy = ay;
                uz = az;
                ax = (ax + vx) / 2f;
                ay = (ay + vy) / 2f;
                az = (az + vz) / 2f;
            }
            odap = dap;
            float dxap = ax - px;
            float dyap = ay - py;
            float dzap = az - pz;
            dap = dxap * dxap + dyap * dyap + dzap * dzap;

        }
        return new Vector4f(ax, ay, az, 1f);
    }

    public static Vector4f getNearestPointToLineSegment2(float lx1, float ly1, float lz1, float lx2, float ly2, float lz2, float px, float py, float pz) {

        // Fastest iterative approach without objects

        // 0th Iteration
        float ax = (lx1 + lx2) / 2f;
        float ay = (ly1 + ly2) / 2f;
        float az = (lz1 + lz2) / 2f;

        // 1st to n-th Iteration
        float ux = lx1;
        float uy = ly1;
        float uz = lz1;
        float vx = lx2;
        float vy = ly2;
        float vz = lz2;

        float dup = 0f;
        float dvp = 1f;

        float dap = 0f;
        float odap = 1f;

        while (Math.abs(dap - odap) > .0000001f) {
            float dxup = ux - px;
            float dyup = uy - py;
            float dzup = uz - pz;
            dup = dxup * dxup + dyup * dyup + dzup * dzup;
            float dxvp = vx - px;
            float dyvp = vy - py;
            float dzvp = vz - pz;
            dvp = dxvp * dxvp + dyvp * dyvp + dzvp * dzvp;

            if (dup < dvp) {
                vx = ax;
                vy = ay;
                vz = az;
                ax = (ax + ux) / 2f;
                ay = (ay + uy) / 2f;
                az = (az + uz) / 2f;
            } else {
                ux = ax;
                uy = ay;
                uz = az;
                ax = (ax + vx) / 2f;
                ay = (ay + vy) / 2f;
                az = (az + vz) / 2f;
            }
            odap = dap;
            float dxap = ax - px;
            float dyap = ay - py;
            float dzap = az - pz;
            dap = dxap * dxap + dyap * dyap + dzap * dzap;

        }
        return new Vector4f(ax, ay, az, 1f);
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
        double[] solution = new PowerRay().BARYCENTRIC(new double[] { xl, yl, zl }, // point
                new double[] { xn, yn, zn }, // normal
                new double[] { x1, y1, z1 }, // v1
                new double[] { x2, y2, z2 }, // v2
                new double[] { x3, y3, z3 }, // v3
                closestPoint);
        if (solution != null) {
            return new Vector4f(closestPoint.x, closestPoint.y, closestPoint.z, 1f);
        }

        float dist2 = 0f;
        float dist3 = 0f;
        float dist4 = 0f;

        float dist5 = 0f;
        float dist6 = 0f;
        float dist7 = 0f;

        float ex1 = 0f;
        float ey1 = 0f;
        float ez1 = 0f;

        float ex2 = 0f;
        float ey2 = 0f;
        float ez2 = 0f;

        float ex3 = 0f;
        float ey3 = 0f;
        float ez3 = 0f;

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
        TreeSet<Float> ts = new TreeSet<Float>();
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

        float s1_x, s1_y, s2_x, s2_y;
        s1_x = v12.x - v11.x;
        s1_y = v12.y - v11.y;

        s2_x = v22.x - v21.x;
        s2_y = v22.y - v21.y;

        float s, t;
        s = (-s1_y * (v11.x - v21.x) + s1_x * (v11.y - v21.y)) / (-s2_x * s1_y + s1_x * s2_y);
        t = (s2_x * (v11.y - v21.y) - s2_y * (v11.x - v21.x)) / (-s2_x * s1_y + s1_x * s2_y);

        if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {
            return new Vector4f(v11.x + t * s1_x, v11.y + t * s1_y, 0f, 1f);
        }
        return null;
    }

    /**
     * Multiplies the target vector with a scalar
     *
     * @param value
     * @param target
     */
    public static void multiply(float value, Vector4f target) {
        target.set(target.x * value, target.y * value, target.z * value, 1f);
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
     * @param A
     *            coefficient matrix
     * @param b
     *            value vector
     * @return solution
     */
    public static float[] gaussianElimination(float[][] A, float[] b) throws RuntimeException {
        int N = b.length; // Very compact gaussian elimination
        for (int p = 0; p < N; p++) {
            int max = p;
            for (int i = p + 1; i < N; i++)
                if (Math.abs(A[i][p]) > Math.abs(A[max][p]))
                    max = i;
            float[] temp = A[p];
            A[p] = A[max];
            A[max] = temp;
            float t = b[p];
            b[p] = b[max];
            b[max] = t;
            if (Math.abs(A[p][p]) < 0.000001f)
                throw new RuntimeException("Matrix is singular or nearly singular"); //$NON-NLS-1$
            for (int i = p + 1; i < N; i++) {
                float alpha = A[i][p] / A[p][p];
                b[i] -= alpha * b[p];
                for (int j = p; j < N; j++)
                    A[i][j] -= alpha * A[p][j];
            }
        }
        float[] x = new float[N];
        for (int i = N - 1; i >= 0; i--) {
            float sum = 0f;
            for (int j = i + 1; j < N; j++)
                sum += A[i][j] * x[j];
            x[i] = (b[i] - sum) / A[i][i];
        }
        return x;
    }

    public static Matrix4f matrixFromStrings(String s30, String s31, String s32, String s00, String s05, String s06, String s07, String s08, String s09, String s10, String s11, String s12) {
        ArrayList<String> as = new ArrayList<String>();
        as.add(s30);
        as.add(s31);
        as.add(s32);
        as.add(s00);
        as.add(s05);
        as.add(s06);
        as.add(s07);
        as.add(s08);
        as.add(s09);
        as.add(s10);
        as.add(s11);
        as.add(s12);
        ArrayList<Float> af = new ArrayList<Float>();
        try {
            for (String s : as) {
                af.add(Float.parseFloat(s));
            }
        } catch (Exception e) {
            return null;
        }
        Matrix4f tMatrix = new Matrix4f();
        tMatrix.m30 = af.get(0) * 1000f;
        tMatrix.m31 = af.get(1) * 1000f;
        tMatrix.m32 = af.get(2) * 1000f;
        tMatrix.m00 = af.get(3);
        tMatrix.m10 = af.get(4);
        tMatrix.m20 = af.get(5);
        tMatrix.m01 = af.get(6);
        tMatrix.m11 = af.get(7);
        tMatrix.m21 = af.get(8);
        tMatrix.m02 = af.get(9);
        tMatrix.m12 = af.get(10);
        tMatrix.m22 = af.get(11);
        tMatrix.m33 = 1f;

        float det = tMatrix.determinant();
        if (Math.abs(det) < Threshold.singularity_determinant) {
            return null;
        }
        return tMatrix;
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
            if (result.equals("0.0"))result = "0"; //$NON-NLS-1$ //$NON-NLS-2$
        }
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

    public static Matrix matrixFromStringsPrecise(String s30, String s31, String s32, String s00, String s05, String s06, String s07, String s08, String s09, String s10, String s11, String s12) {

        final BigDecimal[][] Mn = new BigDecimal[4][4];

        ArrayList<String> as = new ArrayList<String>();
        as.add(s30);
        as.add(s31);
        as.add(s32);
        as.add(s00);
        as.add(s05);
        as.add(s06);
        as.add(s07);
        as.add(s08);
        as.add(s09);
        as.add(s10);
        as.add(s11);
        as.add(s12);
        ArrayList<BigDecimal> ab = new ArrayList<BigDecimal>();
        try {
            for (String s : as) {
                ab.add(new BigDecimal(s));
            }
        } catch (NumberFormatException e) {
            return null;
        }
        Mn[3][0] = ab.get(0);
        Mn[3][1] = ab.get(1);
        Mn[3][2] = ab.get(2);
        Mn[0][0] = ab.get(3);
        Mn[1][0] = ab.get(4);
        Mn[2][0] = ab.get(5);
        Mn[0][1] = ab.get(6);
        Mn[1][1] = ab.get(7);
        Mn[2][1] = ab.get(8);
        Mn[0][2] = ab.get(9);
        Mn[1][2] = ab.get(10);
        Mn[2][2] = ab.get(11);
        Mn[0][3] = BigDecimal.ZERO;
        Mn[1][3] = BigDecimal.ZERO;
        Mn[2][3] = BigDecimal.ZERO;
        Mn[3][3] = BigDecimal.ONE;

        final Matrix result = new Matrix(Mn);
        double det = result.determinant().doubleValue();
        if (Math.abs(det) < Threshold.singularity_determinant) {
            return null;
        }

        return result;
    }

    public static String matrixToStringPrecise(Matrix matrix) {
        StringBuilder lineBuilder = new StringBuilder();
        lineBuilder.append(bigDecimalToString(matrix.M30));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(matrix.M31));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(matrix.M32));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(matrix.M00));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(matrix.M10));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(matrix.M20));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(matrix.M01));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(matrix.M11));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(matrix.M21));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(matrix.M02));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(matrix.M12));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(matrix.M22));
        return lineBuilder.toString();
    }

    public static BigDecimal max(BigDecimal a, BigDecimal b) {
        if (a.compareTo(b) < 0) {
            return b;
        } else {
            return a;
        }
    }

    public static BigDecimal min(BigDecimal a, BigDecimal b) {
        if (a.compareTo(b) < 0) {
            return a;
        } else {
            return b;
        }
    }

    static BigDecimal PI = new BigDecimal("3.14159265358979323846264338327950288419716939937510582097494459230781640628620" //$NON-NLS-1$
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
    static public BigDecimal pi(final MathContext mc) {
        /* look it up if possible */
        if (mc.getPrecision() < PI.precision()) {
            return PI.round(mc);
        } else {
            /*
             * Broadhurst \protect\vrule
             * width0pt\protect\href{http://arxiv.org/abs
             * /math/9803067}{arXiv:math/9803067}
             */
            int[] a = { 1, 0, 0, -1, -1, -1, 0, 0 };
            BigDecimal S = broadhurstBBP(1, 1, a, mc);
            return multiplyRound(S, 8);
        }
    } /* BigDecimalMath.pi */

    /**
     * The integer root.
     *
     * @param n
     *            the positive argument.
     * @param x
     *            the non-negative argument.
     * @return The n-th root of the BigDecimal rounded to the precision implied
     *         by x, x^(1/n).
     */
    static public BigDecimal root(final int n, final BigDecimal x) {
        if (x.compareTo(BigDecimal.ZERO) < 0) {
            throw new ArithmeticException("negative argument " + x.toString() + " of root"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (n <= 0) {
            throw new ArithmeticException("negative power " + n + " of root"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (n == 1) {
            return x;
        }
        /* start the computation from a double precision estimate */
        BigDecimal s = new BigDecimal(Math.pow(x.doubleValue(), 1.0 / n));
        /*
         * this creates nth with nominal precision of 1 digit
         */
        final BigDecimal nth = new BigDecimal(n);
        /*
         * Specify an internal accuracy within the loop which is slightly larger
         * than what is demanded by ’eps’ below.
         */
        final BigDecimal xhighpr = scalePrec(x, 2);
        MathContext mc = new MathContext(2 + x.precision());
        /*
         * Relative accuracy of the result is eps.
         */
        final double eps = x.ulp().doubleValue() / (2 * n * x.doubleValue());
        for (;;) {
            /*
             * s = s -(s/n-x/n/s^(n-1)) = s-(s-x/s^(n-1))/n; test correction
             * s/n-x/s for being smaller than the precision requested. The
             * relative correction is (1-x/s^n)/n,
             */
            BigDecimal c = xhighpr.divide(s.pow(n - 1), mc);
            c = s.subtract(c);
            MathContext locmc = new MathContext(c.precision());
            c = c.divide(nth, locmc);
            s = s.subtract(c);
            if (Math.abs(c.doubleValue() / s.doubleValue()) < eps) {
                break;
            }
        }
        return s.round(new MathContext(err2prec(eps)));
    } /* BigDecimalMath.root */

    /**
     * Trigonometric sine.
     *
     * @param x
     *            The argument in radians.
     * @return sin(x) in the range -1 to 1.
     */
    static public BigDecimal sin(final BigDecimal x) {
        if (x.compareTo(BigDecimal.ZERO) < 0) {
            return sin(x.negate()).negate();
        } else if (x.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        } else {
            /*
             * reduce modulo 2pi
             */
            BigDecimal res = mod2pi(x);
            double errpi = 0.5 * Math.abs(x.ulp().doubleValue());
            MathContext mc = new MathContext(2 + err2prec(3.1415926535897932, errpi));
            BigDecimal p = pi(mc);
            mc = Threshold.mc; // new MathContext(x.precision());
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
                    /*
                     * Simple Taylor expansion, sum_{i=1..infinity}
                     * (-1)^(..)res^(2i+1)/(2i+1)!
                     */
                    BigDecimal resul = res;
                    /* x^i */
                    BigDecimal xpowi = res;
                    /* 2i+1 factorial */
                    BigInteger ifac = BigInteger.ONE;
                    /*
                     * The error in the result is set by the error in x itself.
                     */
                    double xUlpDbl = res.ulp().doubleValue();
                    /*
                     * The error in the result is set by the error in x itself.
                     * We need at most k terms to squeeze x^(2k+1)/(2k+1)! below
                     * this value. x^(2k+1) < x.ulp; (2k+1)*log10(x) <
                     * -x.precision; 2k*log10(x)< -x.precision; 2k*(-log10(x)) >
                     * x.precision; 2k*log10(1/x) > x.precision
                     */
                    int k = (int) (res.precision() / Math.log10(1.0 / res.doubleValue())) / 2;
                    MathContext mcTay = new MathContext(err2prec(res.doubleValue(), xUlpDbl / k));
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
                    // mc = new MathContext(res.precision());
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
    static public BigDecimal cos(final BigDecimal x) {
        if (x.compareTo(BigDecimal.ZERO) < 0) {
            return cos(x.negate());
        } else if (x.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ONE;
        } else {
            /*
             * reduce modulo 2pi
             */
            BigDecimal res = mod2pi(x);
            double errpi = 0.5 * Math.abs(x.ulp().doubleValue());
            MathContext mc = new MathContext(2 + err2prec(3.1415926535897932, errpi));
            BigDecimal p = pi(mc);
            mc = new MathContext(x.precision());
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
                 * higher up. throw new
                 * ProviderException("Unimplemented cosine ") ;
                 */
                if (res.multiply(new BigDecimal("4")).compareTo(p) > 0) { //$NON-NLS-1$
                    /*
                     * x>pi/4: cos(x) = sin(pi/2-x)
                     */
                    return sin(subtractRound(p.divide(new BigDecimal("2")), res)); //$NON-NLS-1$
                } else {
                    /*
                     * Simple Taylor expansion, sum_{i=0..infinity}
                     * (-1)^(..)res^(2i)/(2i)!
                     */
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
                    /*
                     * The error in the result is set by the error in x^2/2
                     * itself, xUlpDbl. We need at most k terms to push
                     * x^(2k+1)/(2k+1)! below this value. x^(2k) < xUlpDbl;
                     * (2k)*log(x) < log(xUlpDbl);
                     */
                    int k = (int) (Math.log(xUlpDbl) / Math.log(res.doubleValue())) / 2;
                    MathContext mcTay = new MathContext(err2prec(1., xUlpDbl / k));
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
                    mc = new MathContext(err2prec(resul.doubleValue(), xUlpDbl));
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
    static private BigDecimal mod2pi(BigDecimal x) {
        /*
         * write x= 2*pi*k+r with the precision in r defined by the precision of
         * x and not compromised by the precision of 2*pi, so the ulp of 2*pi*k
         * should match the ulp of x. First get a guess of k to figure out how
         * many digits of 2*pi are needed.
         */
        int k = (int) (0.5 * x.doubleValue() / Math.PI);
        /*
         * want to have err(2*pi*k)< err(x)=0.5*x.ulp, so err(pi) = err(x)/(4k)
         * with two safety digits
         */

        double err2pi;

        if (k != 0) {
            err2pi = 0.25 * Math.abs(x.ulp().doubleValue() / k);
        } else {
            err2pi = 0.5 * Math.abs(x.ulp().doubleValue());
        }
        MathContext mc = new MathContext(2 + err2prec(6.283, err2pi));
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
        mc = new MathContext(err2prec(res.doubleValue(), x.ulp().doubleValue() / 2.));

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
    static private BigDecimal broadhurstBBP(final int n, final int p, final int a[], MathContext mc) {
        /*
         * Explore the actual magnitude of the result first with a quick
         * estimate.
         */
        double x = 0.0;

        for (int k = 1; k < 10; k++) {
            x += a[(k - 1) % 8] / Math.pow(2., p * (k + 1) / 2) / Math.pow(k, n);
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
            MathContext mcloc = new MathContext(1 + err2prec(r.doubleValue(), eps));
            res = res.add(r.BigDecimalValue(mcloc));

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
    static private BigDecimal subtractRound(final BigDecimal x, final BigDecimal y) {
        BigDecimal resul = x.subtract(y);
        /*
         * The estimation of the absolute error in the result is
         * |err(y)|+|err(x)|
         */

        double errR = Math.abs(y.ulp().doubleValue() / 2.) + Math.abs(x.ulp().doubleValue() / 2.);
        MathContext mc = new MathContext(err2prec(resul.doubleValue(), errR));

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
    static private BigDecimal multiplyRound(final BigDecimal x, final int n) {
        BigDecimal resul = x.multiply(new BigDecimal(n));
        /*
         * The estimation of the absolute error in the result is |n*err(x)|
         */
        MathContext mc = new MathContext(n != 0 ? x.precision() : 0);

        return resul.round(mc);

    }

    /**
     * Append decimal zeros to the value. This returns a value which appears to
     * have a higher precision than the input.
     *
     * @param x
     *            The input value
     * @param d
     *            The (positive) value of zeros to be added as least significant
     *            digits.
     * @return The same value as the input but with increased (pseudo)
     *         precision.
     */
    static private BigDecimal scalePrec(final BigDecimal x, int d) {
        return x.setScale(d + x.scale());

    }

    /**
     * Convert an absolute error to a precision.
     *
     * @param x
     *            The value of the variable The value returned depends only on
     *            the absolute value, not on the sign.
     * @param xerr
     *            The absolute error in the variable The value returned depends
     *            only on the absolute value, not on the sign.
     * @return The number of valid digits in x. Derived from the representation
     *         x+- xerr, as if the error was represented 38 in a "half width"
     *         (half of the error bar) form. The value is rounded down, and on
     *         the pessimistic side for that reason.
     */
    static private int err2prec(double x, double xerr) {
        /*
         * Example: an error of xerr=+-0.5 at x=100 represents 100+-0.5 with a
         * precision = 3 (digits).
         */
        return 32; // 10 + (int) Math.log10(Math.abs(0.5 * x / xerr));

    }

    /**
     * Convert a relative error to a precision.
     *
     * @param xerr
     *            The relative error in the variable. The value returned depends
     *            only on the absolute value, not on the sign.
     * @return The number of valid digits in x. The value is rounded down, and
     *         on the pessimistic side for that reason.
     */
    static private int err2prec(double xerr) {
        /*
         * Example: an error of xerr=+-0.5 a precision of 1 (digit), an error of
         * +-0.05 a precision of 2 (digits)
         */
        return 32; // 10 + (int) Math.log10(Math.abs(0.5 / xerr));

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
    static private double prec2err(final double x, final int prec) {
        return 5. * Math.abs(x) * Math.pow(10., -prec);

    }

    static public ArrayList<GData3> triangulateFourPoints(int colourNumber, float r, float g, float b, float a, Vertex vertex, Vertex vertex2, Vertex vertex3, Vertex vertex4, GData1 dummyReference, DatFile df) {
        ArrayList<GData3> result = new ArrayList<GData3>();
        result.add(new GData3(colourNumber, r, g, b, a,
                vertex,
                vertex2,
                vertex3,
                dummyReference, df));
        result.add(new GData3(colourNumber, r, g, b, a,
                vertex,
                vertex3,
                vertex4,
                dummyReference, df));
        return result;
    }

    static public ArrayList<GData3> triangulateFivePoints(int colourNumber, float r, float g, float b, float a, Vertex vertex, Vertex vertex2, Vertex vertex3, Vertex vertex4, Vertex vertex5, GData1 dummyReference, DatFile df) {
        ArrayList<GData3> result = new ArrayList<GData3>();
        result.add(new GData3(colourNumber, r, g, b, a,
                vertex,
                vertex2,
                vertex3,
                dummyReference, df));
        result.add(new GData3(colourNumber, r, g, b, a,
                vertex,
                vertex3,
                vertex4,
                dummyReference, df));
        result.add(new GData3(colourNumber, r, g, b, a,
                vertex,
                vertex4,
                vertex5,
                dummyReference, df));
        return result;
    }

    static public ArrayList<GData3> triangulateSixPoints(int colourNumber, float r, float g, float b, float a, Vertex vertex, Vertex vertex2, Vertex vertex3, Vertex vertex4, Vertex vertex5, Vertex vertex6, GData1 dummyReference, DatFile df) {
        ArrayList<GData3> result = new ArrayList<GData3>();
        result.add(new GData3(colourNumber, r, g, b, a,
                vertex,
                vertex2,
                vertex3,
                dummyReference, df));
        result.add(new GData3(colourNumber, r, g, b, a,
                vertex,
                vertex3,
                vertex4,
                dummyReference, df));
        result.add(new GData3(colourNumber, r, g, b, a,
                vertex,
                vertex4,
                vertex5,
                dummyReference, df));
        result.add(new GData3(colourNumber, r, g, b, a,
                vertex,
                vertex5,
                vertex6,
                dummyReference, df));
        return result;
    }

    static public ArrayList<GData3> triangulateSevenPoints(int colourNumber, float r, float g, float b, float a, Vertex vertex, Vertex vertex2, Vertex vertex3, Vertex vertex4, Vertex vertex5, Vertex vertex6, Vertex vertex7, GData1 dummyReference, DatFile df) {
        ArrayList<GData3> result = new ArrayList<GData3>();

        result.add(new GData3(colourNumber, r, g, b, a,
                vertex,
                vertex3,
                vertex4,
                dummyReference, df));
        result.add(new GData3(colourNumber, r, g, b, a,
                vertex,
                vertex4,
                vertex5,
                dummyReference, df));
        result.add(new GData3(colourNumber, r, g, b, a,
                vertex,
                vertex5,
                vertex6,
                dummyReference, df));
        result.add(new GData3(colourNumber, r, g, b, a,
                vertex,
                vertex6,
                vertex7,
                dummyReference, df));
        return result;
    }

    public static ArrayList<GData3> triangulatePointGroups(ArrayList<GColour> cols, ArrayList<Vector3dd> av, ArrayList<Integer> types, GData1 dummyReference,
            DatFile df) {
        ArrayList<GData3> result = new ArrayList<GData3>();
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
                        v1.X,
                        v1.Y,
                        v1.Z,
                        v2.X,
                        v2.Y,
                        v2.Z,
                        v3.X,
                        v3.Y,
                        v3.Z,
                        dummyReference, df));
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

    public static ArrayList<GData2> triangulatePointGroups2(ArrayList<GColour> cols, ArrayList<Vector3dd> av, ArrayList<Integer> types, GData1 dummyReference,
            DatFile df) {
        ArrayList<GData2> result = new ArrayList<GData2>();
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
                        v1.X,
                        v1.Y,
                        v1.Z,
                        v2.X,
                        v2.Y,
                        v2.Z,
                        dummyReference, df));
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

    public static ArrayList<GData5> triangulatePointGroups5(ArrayList<GColour> cols, ArrayList<Vector3dd> av, ArrayList<Integer> types, GData1 dummyReference,
            DatFile df) {
        ArrayList<GData5> result = new ArrayList<GData5>();
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
                        v1.X,
                        v1.Y,
                        v1.Z,
                        v2.X,
                        v2.Y,
                        v2.Z,
                        v3.X,
                        v3.Y,
                        v3.Z,
                        v4.X,
                        v4.Y,
                        v4.Z,
                        dummyReference, df));
                break;
            default:
                break;
            }
            counter2 += 1;
        }
        return result;
    }

    public static ArrayList<GData3> triangulateNPoints(int colourNumber, float r, float g, float b, float a, int pointsToTriangulate, ArrayList<Vector3d> av, GData1 dummyReference,
            DatFile df) {
        ArrayList<GData3> result = new ArrayList<GData3>(pointsToTriangulate);
        final Vector3d origin = av.get(0);
        final BigDecimal originX = origin.X;
        final BigDecimal originY = origin.Y;
        final BigDecimal originZ = origin.Z;

        pointsToTriangulate--;

        Vector3d p1 = av.get(1);
        Vector3d p2 = av.get(2);
        BigDecimal x1 = p1.X;
        BigDecimal y1 = p1.Y;
        BigDecimal z1 = p1.Z;

        BigDecimal x2 = p2.X;
        BigDecimal y2 = p2.Y;
        BigDecimal z2 = p2.Z;

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
                dummyReference, df));

        for (int i = 2; i < pointsToTriangulate; i++) {

            p1 = av.get(i + 1);

            x1 = x2;
            y1 = y2;
            z1 = z2;

            x2 = p1.X;
            y2 = p1.Y;
            z2 = p1.Z;

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
                    dummyReference, df));
        }
        return result;
    }


}
