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

import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.data.Vertex;

/**
 * Ported from VB.net / LDPatternCreator to Java, the original code was 6 years
 * old...
 *
 * @author nils
 *
 */
public final class PowerRay {

    private static final double TOLERANCE = 0.00001d;

    private static final float[] EMPTY_FLOAT_ARRAY = new float[0];
    private static final double[] EMPTY_DOUBLE_ARRAY = new double[0];

    private double t;
    private double u;
    private double v;

    private final double[] corner1 = new double[3];
    private final double[] corner2 = new double[3];
    private final double[] tvec = new double[3];
    private final double[] pvec = new double[3];
    private final double[] qvec = new double[3];

    private boolean triangleIntersect(double[] orig, double[] dir, double[] vert0, double[] vert1, double[] vert2) {
        double diskr = 0;
        double invDiskr = 0;
        corner1[0] = vert1[0] - vert0[0];
        corner1[1] = vert1[1] - vert0[1];
        corner1[2] = vert1[2] - vert0[2];
        corner2[0] = vert2[0] - vert0[0];
        corner2[1] = vert2[1] - vert0[1];
        corner2[2] = vert2[2] - vert0[2];
        pvec[0] = dir[1] * corner2[2] - dir[2] * corner2[1];
        pvec[1] = dir[2] * corner2[0] - dir[0] * corner2[2];
        pvec[2] = dir[0] * corner2[1] - dir[1] * corner2[0];
        diskr = corner1[0] * pvec[0] + corner1[1] * pvec[1] + corner1[2] * pvec[2];
        if (diskr > -TOLERANCE && diskr < TOLERANCE)
            return false;
        invDiskr = 1d / diskr;
        tvec[0] = orig[0] - vert0[0];
        tvec[1] = orig[1] - vert0[1];
        tvec[2] = orig[2] - vert0[2];
        u = (tvec[0] * pvec[0] + tvec[1] * pvec[1] + tvec[2] * pvec[2]) * invDiskr;
        if (u < 0 || u > 1)
            return false;
        qvec[0] = tvec[1] * corner1[2] - tvec[2] * corner1[1];
        qvec[1] = tvec[2] * corner1[0] - tvec[0] * corner1[2];
        qvec[2] = tvec[0] * corner1[1] - tvec[1] * corner1[0];
        v = (dir[0] * qvec[0] + dir[1] * qvec[1] + dir[2] * qvec[2]) * invDiskr;
        if (v < 0 || u + v > 1)
            return false;
        t = (corner2[0] * qvec[0] + corner2[1] * qvec[1] + corner2[2] * qvec[2]) * invDiskr;
        if (t < 0)
            return false;
        return true;
    }

    private boolean triangleIntersect2(double[] orig, double[] dir, double[] vert0, double[] vert1, double[] vert2) {
        double diskr = 0;
        double invDiskr = 0;
        corner1[0] = vert1[0] - vert0[0];
        corner1[1] = vert1[1] - vert0[1];
        corner1[2] = vert1[2] - vert0[2];
        corner2[0] = vert2[0] - vert0[0];
        corner2[1] = vert2[1] - vert0[1];
        corner2[2] = vert2[2] - vert0[2];
        pvec[0] = dir[1] * corner2[2] - dir[2] * corner2[1];
        pvec[1] = dir[2] * corner2[0] - dir[0] * corner2[2];
        pvec[2] = dir[0] * corner2[1] - dir[1] * corner2[0];
        diskr = corner1[0] * pvec[0] + corner1[1] * pvec[1] + corner1[2] * pvec[2];
        if (diskr > -TOLERANCE && diskr < TOLERANCE)
            return false;
        invDiskr = 1d / diskr;
        tvec[0] = orig[0] - vert0[0];
        tvec[1] = orig[1] - vert0[1];
        tvec[2] = orig[2] - vert0[2];
        u = (tvec[0] * pvec[0] + tvec[1] * pvec[1] + tvec[2] * pvec[2]) * invDiskr;
        if (u < 0 || u > 1)
            return false;
        qvec[0] = tvec[1] * corner1[2] - tvec[2] * corner1[1];
        qvec[1] = tvec[2] * corner1[0] - tvec[0] * corner1[2];
        qvec[2] = tvec[0] * corner1[1] - tvec[1] * corner1[0];
        v = (dir[0] * qvec[0] + dir[1] * qvec[1] + dir[2] * qvec[2]) * invDiskr;
        if (v < 0 || u + v > 1)
            return false;
        t = (corner2[0] * qvec[0] + corner2[1] * qvec[1] + corner2[2] * qvec[2]) * invDiskr;
        return true;
    }

    public boolean triangleIntersect(Vector4f orig, Vector4f dir, Vertex vert0, Vertex vert1, Vertex vert2) {
        double[] origArr = new double[] { orig.x + dir.x * 100f, orig.y + dir.y * 100f, orig.z + dir.z * 100f };
        double[] dirArr = new double[] { dir.x, dir.y, dir.z };
        double[] vert0Arr = new double[] { vert0.x, vert0.y, vert0.z };
        double[] vert1Arr = new double[] { vert1.x, vert1.y, vert1.z };
        double[] vert2Arr = new double[] { vert2.x, vert2.y, vert2.z };
        return triangleIntersect(origArr, dirArr, vert0Arr, vert1Arr, vert2Arr);
    }

    public boolean triangleIntersect(Vector4f orig, Vector4f dir, Vertex vert0, Vertex vert1, Vertex vert2, Vector4f intersectionPoint, double[] dist) {
        double[] origArr = new double[] { orig.x, orig.y, orig.z };
        double[] dirArr = new double[] { dir.x, dir.y, dir.z };
        double[] vert0Arr = new double[] { vert0.x, vert0.y, vert0.z };
        double[] vert1Arr = new double[] { vert1.x, vert1.y, vert1.z };
        double[] vert2Arr = new double[] { vert2.x, vert2.y, vert2.z };
        if (triangleIntersect2(origArr, dirArr, vert0Arr, vert1Arr, vert2Arr)) {
            intersectionPoint.set((float) (origArr[0] + dirArr[0] * t), (float) (origArr[1] + dirArr[1] * t), (float) (origArr[2] + dirArr[2] * t), 1f);
            dist[0] = t;
            return true;
        }
        return false;
    }

    double[] barycentric(double[] point, double[] normal, double[] vert0, double[] vert1, double[] vert2, Vector4f intersectionPoint) {
        double[] orig = new double[] { 100.0 * normal[0] + point[0], 100.0 * normal[1] + point[1], 100.0 * normal[2] + point[2] };
        double diskr = 0;
        double invDiskr = 0;
        corner1[0] = vert1[0] - vert0[0];
        corner1[1] = vert1[1] - vert0[1];
        corner1[2] = vert1[2] - vert0[2];
        corner2[0] = vert2[0] - vert0[0];
        corner2[1] = vert2[1] - vert0[1];
        corner2[2] = vert2[2] - vert0[2];
        pvec[0] = normal[1] * corner2[2] - normal[2] * corner2[1];
        pvec[1] = normal[2] * corner2[0] - normal[0] * corner2[2];
        pvec[2] = normal[0] * corner2[1] - normal[1] * corner2[0];
        diskr = corner1[0] * pvec[0] + corner1[1] * pvec[1] + corner1[2] * pvec[2];
        if (diskr > -TOLERANCE && diskr < TOLERANCE)
            return EMPTY_DOUBLE_ARRAY;
        invDiskr = 1d / diskr;
        tvec[0] = orig[0] - vert0[0];
        tvec[1] = orig[1] - vert0[1];
        tvec[2] = orig[2] - vert0[2];
        u = (tvec[0] * pvec[0] + tvec[1] * pvec[1] + tvec[2] * pvec[2]) * invDiskr;
        if (u < 0 || u > 1d)
            return EMPTY_DOUBLE_ARRAY;
        qvec[0] = tvec[1] * corner1[2] - tvec[2] * corner1[1];
        qvec[1] = tvec[2] * corner1[0] - tvec[0] * corner1[2];
        qvec[2] = tvec[0] * corner1[1] - tvec[1] * corner1[0];
        v = (normal[0] * qvec[0] + normal[1] * qvec[1] + normal[2] * qvec[2]) * invDiskr;
        if (v < 0 || u + v > 1d)
            return EMPTY_DOUBLE_ARRAY;
        double w = 1.0 - u - v;
        intersectionPoint.set((float) (vert0[0] * w + vert1[0] * u + vert2[0] * v), (float) (vert0[1] * w + vert1[1] * u + vert2[1] * v), (float) (vert0[2] * w + vert1[2] * u + vert2[2] * v), 1f);
        return new double[] { u, v, w };
    }

    public float[] triangleIntersect(Vector4f orig, float[] dir, float[] tri) {
        double diskr = 0;
        double invDiskr = 0;
        corner1[0] = tri[3] - tri[0];
        corner1[1] = tri[4] - tri[1];
        corner1[2] = tri[5] - tri[2];
        corner2[0] = tri[6] - tri[0];
        corner2[1] = tri[7] - tri[1];
        corner2[2] = tri[8] - tri[2];
        pvec[0] = dir[1] * corner2[2] - dir[2] * corner2[1];
        pvec[1] = dir[2] * corner2[0] - dir[0] * corner2[2];
        pvec[2] = dir[0] * corner2[1] - dir[1] * corner2[0];
        diskr = corner1[0] * pvec[0] + corner1[1] * pvec[1] + corner1[2] * pvec[2];
        if (diskr > -TOLERANCE && diskr < TOLERANCE)
            return EMPTY_FLOAT_ARRAY;
        invDiskr = 1d / diskr;
        tvec[0] = orig.x - tri[0];
        tvec[1] = orig.y - tri[1];
        tvec[2] = orig.z - tri[2];
        u = (tvec[0] * pvec[0] + tvec[1] * pvec[1] + tvec[2] * pvec[2]) * invDiskr;
        if (u < 0 || u > 1)
            return EMPTY_FLOAT_ARRAY;
        qvec[0] = tvec[1] * corner1[2] - tvec[2] * corner1[1];
        qvec[1] = tvec[2] * corner1[0] - tvec[0] * corner1[2];
        qvec[2] = tvec[0] * corner1[1] - tvec[1] * corner1[0];
        v = (dir[0] * qvec[0] + dir[1] * qvec[1] + dir[2] * qvec[2]) * invDiskr;
        if (v < 0 || u + v > 1)
            return EMPTY_FLOAT_ARRAY;
        t = (corner2[0] * qvec[0] + corner2[1] * qvec[1] + corner2[2] * qvec[2]) * invDiskr;
        return new float[]{(float) (orig.x + dir[0] * t), (float) (orig.y + dir[1] * t), (float) (orig.z + dir[2] * t), (float) u, (float) v};
    }
}
