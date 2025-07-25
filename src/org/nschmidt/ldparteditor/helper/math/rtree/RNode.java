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
package org.nschmidt.ldparteditor.helper.math.rtree;

import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GData3;
import org.nschmidt.ldparteditor.data.GData4;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.helper.math.MathHelper;
import org.nschmidt.ldparteditor.helper.math.PowerRay;

public class RNode {

    private static final double TOLERANCE = 0.00001d;

    BoundingBox bb;
    RNode parent = null;
    final RNode[] children = new RNode[2];
    private GData geometry;

    public boolean isLeaf() {
        return geometry != null && children[0] == null && children[1] == null;
    }

    public boolean isClear() {
        return geometry == null && children[0] == null && children[1] == null;
    }

    public void insertGeometry(GData geometry, Map<GData3, Vertex[]> triangles, Map<GData4, Vertex[]> quads) {
        this.geometry = geometry;
        bb = new BoundingBox();
        bb.insert(geometry, triangles, quads);
    }

    public void split() {
        if (isLeaf()) {
            final RNode node = new RNode();
            children[0] = node;
            node.bb = bb.copy();
            node.geometry = geometry;
            node.parent = this;
            this.geometry = null;
        }
    }

    public void backpropagate(GData geometry, Map<GData3, Vertex[]> triangles, Map<GData4, Vertex[]> quads) {
        bb.insert(geometry, triangles, quads);
        if (parent != null) {
            parent.backpropagate(geometry, triangles, quads);
        }
    }

    public boolean pointsToLeaves() {
        return children[0].isLeaf() && children[1].isLeaf();
    }

    public List<GData> retrieveGeometryDataOnRay(Vector4f rayOrigin, float[] rayDirection, float[] rayDirectionNormalized, List<GData> resultList, BoundingBox bb, PowerRay powerRay, Map<GData3, Vertex[]> triangles, Map<GData4, Vertex[]> quads) {
        if (isLeaf()) {
            if (geometry instanceof GData3 triangle) {
                final BoundingBox b = new BoundingBox();
                b.insert(triangle, triangles, quads);
                if (b.intersects(bb)) {
                    return testRayTriangle(rayOrigin, rayDirection, triangle, resultList, powerRay, triangles);
                }
            } else if (geometry instanceof GData4 quad) {
                final BoundingBox b = new BoundingBox();
                b.insert(quad, triangles, quads);
                if (b.intersects(bb)) {
                    return testRayQuad(rayOrigin, rayDirection, quad, resultList, powerRay, quads);
                }
            }
        } else if (bb.isIntersecting(rayOrigin, rayDirectionNormalized)) {
            for (RNode c : children) {
                c.retrieveGeometryDataOnRay(rayOrigin, rayDirection, rayDirectionNormalized, resultList, bb, powerRay, triangles, quads);
            }
        }

        return resultList;
    }

    private List<GData> testRayTriangle(Vector4f rayOrigin, float[] rayDirection, GData3 triangle, List<GData> resultList, PowerRay powerRay, Map<GData3, Vertex[]> triangles) {
        final Vertex[] v = triangles.get(triangle);
        final float[] triangleData = new float[] {
                v[0].x, v[0].y, v[0].z,
                v[1].x, v[1].y, v[1].z,
                v[2].x, v[2].y, v[2].z
        };

        final float[] result = powerRay.triangleIntersect(rayOrigin, rayDirection, triangleData);

        if (result.length > 5) {
            float t = result[5];
            if (t >= 0 && t <= 1f) {
                if (intersectionNotOnCorner(result, triangleData)) {
                    resultList.add(geometry);
                    return resultList;
                }
            }
        }

        // Check if the line segment is inside of the triangle
        if (pointInsideTriangle(triangleData, rayOrigin.x, rayOrigin.y, rayOrigin.z)
         || pointInsideTriangle(triangleData, rayOrigin.x + rayDirection[0], rayOrigin.y + rayDirection[1], rayOrigin.z + rayDirection[2])) {
            resultList.add(geometry);
            return resultList;
        }

        if (hasSegmentIntersection(rayOrigin, rayDirection,
                v[0].x, v[0].y, v[0].z,
                v[1].x, v[1].y, v[1].z)
         || hasSegmentIntersection(rayOrigin, rayDirection,
                 v[1].x, v[1].y, v[1].z,
                 v[2].x, v[2].y, v[2].z)
         || hasSegmentIntersection(rayOrigin, rayDirection,
                 v[2].x, v[2].y, v[2].z,
                 v[0].x, v[0].y, v[0].z)) {
            resultList.add(geometry);
        }

        return resultList;
    }

    private boolean pointInsideTriangle(float[] tri, float x, float y, float z) {
        final Vector4f nearestPoint = MathHelper.getNearestPointToTriangle(tri[0], tri[1], tri[2], tri[3], tri[4], tri[5], tri[6], tri[7], tri[8], x, y, z);
        final Vector4f referencePoint = new Vector4f(x, y, z, 1f);
        Vector4f.sub(referencePoint, nearestPoint, referencePoint);
        final float dist = referencePoint.lengthSquared();
        final boolean result = dist < TOLERANCE;
        if (result) {
            // Check if the point is a corner of the triangle
            if ((Math.abs(x - tri[0]) < TOLERANCE && Math.abs(y - tri[1]) < TOLERANCE && Math.abs(z - tri[2]) < TOLERANCE)
             || (Math.abs(x - tri[3]) < TOLERANCE && Math.abs(y - tri[4]) < TOLERANCE && Math.abs(z - tri[5]) < TOLERANCE)
             || (Math.abs(x - tri[6]) < TOLERANCE && Math.abs(y - tri[7]) < TOLERANCE && Math.abs(z - tri[8]) < TOLERANCE)) {
                return false;
            }
        }

        return result;
    }

    private List<GData> testRayQuad(Vector4f rayOrigin, float[] rayDirection, GData4 quad, List<GData> resultList, PowerRay powerRay, Map<GData4, Vertex[]> quads) {
        final Vertex[] v = quads.get(quad);
        final float[] quadDataA = new float[] {
                v[0].x, v[0].y, v[0].z,
                v[1].x, v[1].y, v[1].z,
                v[2].x, v[2].y, v[2].z
        };

        final float[] resultA = powerRay.triangleIntersect(rayOrigin, rayDirection, quadDataA);

        if (resultA.length > 5) {
            float t = resultA[5];
            if (t >= 0 && t <= 1f) {
                if (intersectionNotOnCorner(resultA, quadDataA)) {
                    resultList.add(geometry);
                    return resultList;
                }
            }
        }

        if (pointInsideTriangle(quadDataA, rayOrigin.x, rayOrigin.y, rayOrigin.z)
         || pointInsideTriangle(quadDataA, rayOrigin.x + rayDirection[0], rayOrigin.y + rayDirection[1], rayOrigin.z + rayDirection[2])) {
            resultList.add(geometry);
            return resultList;
        }

        final float[] quadDataB = new float[] {
                v[2].x, v[2].y, v[2].z,
                v[3].x, v[3].y, v[3].z,
                v[0].x, v[0].y, v[0].z
        };

        final float[] resultB = powerRay.triangleIntersect(rayOrigin, rayDirection, quadDataB);

        if (resultB.length > 5) {
            float t = resultB[5];
            if (t >= 0 && t <= 1f) {
                if (intersectionNotOnCorner(resultB, quadDataB)) {
                    resultList.add(geometry);
                    return resultList;
                }
            }
        }

        if (pointInsideTriangle(quadDataB, rayOrigin.x, rayOrigin.y, rayOrigin.z)
         || pointInsideTriangle(quadDataB, rayOrigin.x + rayDirection[0], rayOrigin.y + rayDirection[1], rayOrigin.z + rayDirection[2])) {
            resultList.add(geometry);
            return resultList;
        }

        if (hasSegmentIntersection(rayOrigin, rayDirection,
                v[0].x, v[0].y, v[0].z,
                v[1].x, v[1].y, v[1].z)
         || hasSegmentIntersection(rayOrigin, rayDirection,
                 v[1].x, v[1].y, v[1].z,
                 v[2].x, v[2].y, v[2].z)
         || hasSegmentIntersection(rayOrigin, rayDirection,
                 v[2].x, v[2].y, v[2].z,
                 v[3].x, v[3].y, v[3].z)
         || hasSegmentIntersection(rayOrigin, rayDirection,
                 v[3].x, v[3].y, v[3].z,
                 v[0].x, v[0].y, v[0].z)) {
            resultList.add(geometry);
        }

        return resultList;
    }

    private boolean intersectionNotOnCorner(float[] result, float[] data) {
        final float rx = result[0];
        final float ry = result[1];
        final float rz = result[2];
        for (int i = 0; i < data.length; i += 3) {
            final float x = data[i];
            final float y = data[i + 1];
            final float z = data[i + 2];

            if (Math.abs(x - rx) < TOLERANCE && Math.abs(y - ry) < TOLERANCE && Math.abs(z - rz) < TOLERANCE) {
                return false;
            }
        }

        return true;
    }

    public static boolean hasSegmentIntersection(Vector4f rayOrigin, float[] rayDirection,
            float x1, float y1, float z1,
            float x2, float y2, float z2) {

        Vector3f seg1 = new Vector3f(rayOrigin.x, rayOrigin.y, rayOrigin.z);
        Vector3f seg2 = new Vector3f(rayOrigin.x + rayDirection[0], rayOrigin.y + rayDirection[1], rayOrigin.z + rayDirection[2]);
        Vector3f seg3 = new Vector3f(x1, y1, z1);
        Vector3f seg4 = new Vector3f(x2, y2, z2);

        Vector3f p1 = seg1;
        Vector3f p2 = seg3;
        Vector3f v1 = Vector3f.sub(seg2, seg1, new Vector3f());
        Vector3f v2 = Vector3f.sub(seg4, seg3, new Vector3f());
        Vector3f v21 = Vector3f.sub(p2, p1, new Vector3f());

        float dv22 = Vector3f.dot(v2, v2);
        float dv11 = Vector3f.dot(v1, v1);
        float dv21 = Vector3f.dot(v2, v1);
        float dv21_1 = Vector3f.dot(v21, v1);
        float dv21_2 = Vector3f.dot(v21, v2);
        float diskr = dv21 * dv21 - dv22 * dv11;

        float s;
        float t;

        // Check for parallel segments
        if (Math.abs(diskr) < TOLERANCE) {
            if (Math.abs(dv21) < TOLERANCE) {
                return false;
            }

            s = 0f;
            t = (dv11 * s - dv21_1) / dv21;
        } else {
            s = (dv21_2 * dv21 - dv22 * dv21_1) / diskr;
            t = (-dv21_1 * dv21 + dv11 * dv21_2) / diskr;
        }

        s = Math.clamp(s, 0f, 1f);
        t = Math.clamp(t, 0f, 1f);

        Vector3f pA = lin(v1, s, p1);
        Vector3f pB = lin(v2, t, p2);

        Vector3f delta = Vector3f.sub(pA, pB, new Vector3f());
        boolean result = delta.length() < TOLERANCE;
        if (result && (Math.abs(s) < TOLERANCE || Math.abs(1f - s) < TOLERANCE) && (Math.abs(t) < TOLERANCE || Math.abs(1f - t) < TOLERANCE)) {
            // Segments are overlapping at their start and end points.
            return false;
        }
        return result;
    }

    private static Vector3f lin(Vector3f a, float x, Vector3f b) {
        return new Vector3f(
                a.x * x + b.x,
                a.y * x + b.y,
                a.z * x + b.z);
    }
}
