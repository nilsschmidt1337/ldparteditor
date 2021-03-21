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

    private double t;
    private double u;
    private double v;

    private double[] corner1 = new double[3];
    private double[] corner2 = new double[3];
    private double[] tvec = new double[3];
    private double[] pvec = new double[3];
    private double[] qvec = new double[3];

    private final double TOLERANCE = 0.00001d;

    private boolean TRIANGLE_INTERSECT(double[] orig, double[] dir, double[] vert0, double[] vert1, double[] vert2) {
        double diskr = 0;
        double inv_diskr = 0;
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
        inv_diskr = 1d / diskr;
        tvec[0] = orig[0] - vert0[0];
        tvec[1] = orig[1] - vert0[1];
        tvec[2] = orig[2] - vert0[2];
        u = (tvec[0] * pvec[0] + tvec[1] * pvec[1] + tvec[2] * pvec[2]) * inv_diskr;
        if (u < 0 || u > 1)
            return false;
        qvec[0] = tvec[1] * corner1[2] - tvec[2] * corner1[1];
        qvec[1] = tvec[2] * corner1[0] - tvec[0] * corner1[2];
        qvec[2] = tvec[0] * corner1[1] - tvec[1] * corner1[0];
        v = (dir[0] * qvec[0] + dir[1] * qvec[1] + dir[2] * qvec[2]) * inv_diskr;
        if (v < 0 || u + v > 1)
            return false;
        t = (corner2[0] * qvec[0] + corner2[1] * qvec[1] + corner2[2] * qvec[2]) * inv_diskr;
        if (t < 0)
            return false;
        return true;
    }

    private boolean TRIANGLE_INTERSECT2(double[] orig, double[] dir, double[] vert0, double[] vert1, double[] vert2) {
        double diskr = 0;
        double inv_diskr = 0;
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
        inv_diskr = 1d / diskr;
        tvec[0] = orig[0] - vert0[0];
        tvec[1] = orig[1] - vert0[1];
        tvec[2] = orig[2] - vert0[2];
        u = (tvec[0] * pvec[0] + tvec[1] * pvec[1] + tvec[2] * pvec[2]) * inv_diskr;
        if (u < 0 || u > 1)
            return false;
        qvec[0] = tvec[1] * corner1[2] - tvec[2] * corner1[1];
        qvec[1] = tvec[2] * corner1[0] - tvec[0] * corner1[2];
        qvec[2] = tvec[0] * corner1[1] - tvec[1] * corner1[0];
        v = (dir[0] * qvec[0] + dir[1] * qvec[1] + dir[2] * qvec[2]) * inv_diskr;
        if (v < 0 || u + v > 1)
            return false;
        t = (corner2[0] * qvec[0] + corner2[1] * qvec[1] + corner2[2] * qvec[2]) * inv_diskr;
        return true;
    }

    public boolean TRIANGLE_INTERSECT(Vector4f orig, Vector4f dir, Vertex vert0, Vertex vert1, Vertex vert2) {
        double[] orig_arr = new double[] { orig.x + dir.x * 100f, orig.y + dir.y * 100f, orig.z + dir.z * 100f };
        double[] dir_arr = new double[] { dir.x, dir.y, dir.z };
        double[] vert0_arr = new double[] { vert0.x, vert0.y, vert0.z };
        double[] vert1_arr = new double[] { vert1.x, vert1.y, vert1.z };
        double[] vert2_arr = new double[] { vert2.x, vert2.y, vert2.z };
        return TRIANGLE_INTERSECT(orig_arr, dir_arr, vert0_arr, vert1_arr, vert2_arr);
    }

    public boolean TRIANGLE_INTERSECT(Vector4f orig, Vector4f dir, Vertex vert0, Vertex vert1, Vertex vert2, Vector4f intersection_point, double[] dist) {
        double[] orig_arr = new double[] { orig.x, orig.y, orig.z };
        double[] dir_arr = new double[] { dir.x, dir.y, dir.z };
        double[] vert0_arr = new double[] { vert0.x, vert0.y, vert0.z };
        double[] vert1_arr = new double[] { vert1.x, vert1.y, vert1.z };
        double[] vert2_arr = new double[] { vert2.x, vert2.y, vert2.z };
        if (TRIANGLE_INTERSECT2(orig_arr, dir_arr, vert0_arr, vert1_arr, vert2_arr)) {
            intersection_point.set((float) (orig_arr[0] + dir_arr[0] * t), (float) (orig_arr[1] + dir_arr[1] * t), (float) (orig_arr[2] + dir_arr[2] * t), 1f);
            dist[0] = t;
            return true;
        }
        return false;
    }

    double[] BARYCENTRIC(double[] point, double[] normal, double[] vert0, double[] vert1, double[] vert2, Vector4f intersection_point) {
        double[] orig = new double[] { 100.0 * normal[0] + point[0], 100.0 * normal[1] + point[1], 100.0 * normal[2] + point[2] };
        double diskr = 0;
        double inv_diskr = 0;
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
            return null;
        inv_diskr = 1d / diskr;
        tvec[0] = orig[0] - vert0[0];
        tvec[1] = orig[1] - vert0[1];
        tvec[2] = orig[2] - vert0[2];
        u = (tvec[0] * pvec[0] + tvec[1] * pvec[1] + tvec[2] * pvec[2]) * inv_diskr;
        if (u < 0 || u > 1d)
            return null;
        qvec[0] = tvec[1] * corner1[2] - tvec[2] * corner1[1];
        qvec[1] = tvec[2] * corner1[0] - tvec[0] * corner1[2];
        qvec[2] = tvec[0] * corner1[1] - tvec[1] * corner1[0];
        v = (normal[0] * qvec[0] + normal[1] * qvec[1] + normal[2] * qvec[2]) * inv_diskr;
        if (v < 0 || u + v > 1d)
            return null;
        double w = 1.0 - u - v;
        intersection_point.set((float) (vert0[0] * w + vert1[0] * u + vert2[0] * v), (float) (vert0[1] * w + vert1[1] * u + vert2[1] * v), (float) (vert0[2] * w + vert1[2] * u + vert2[2] * v), 1f);
        return new double[] { u, v, w };
    }

    public float[] TRIANGLE_INTERSECT(Vector4f orig, float[] dir, float[] tri) {
        double diskr = 0;
        double inv_diskr = 0;
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
            return null;
        inv_diskr = 1d / diskr;
        tvec[0] = orig.x - tri[0];
        tvec[1] = orig.y - tri[1];
        tvec[2] = orig.z - tri[2];
        u = (tvec[0] * pvec[0] + tvec[1] * pvec[1] + tvec[2] * pvec[2]) * inv_diskr;
        if (u < 0 || u > 1)
            return null;
        qvec[0] = tvec[1] * corner1[2] - tvec[2] * corner1[1];
        qvec[1] = tvec[2] * corner1[0] - tvec[0] * corner1[2];
        qvec[2] = tvec[0] * corner1[1] - tvec[1] * corner1[0];
        v = (dir[0] * qvec[0] + dir[1] * qvec[1] + dir[2] * qvec[2]) * inv_diskr;
        if (v < 0 || u + v > 1)
            return null;
        t = (corner2[0] * qvec[0] + corner2[1] * qvec[1] + corner2[2] * qvec[2]) * inv_diskr;
        return new float[]{(float) (orig.x + dir[0] * t), (float) (orig.y + dir[1] * t), (float) (orig.z + dir[2] * t), (float) u, (float) v};
    }
}
