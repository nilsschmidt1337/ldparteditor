/*Copyright (c) 2017 Travis Cobbs, Peter Bartfai

Ported from C++ to Java by Nils Schmidt, 2024

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.*/
package org.nschmidt.ldparteditor.helper.math;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

public enum EightSphereGenerator {
    INSTANCE;

    public static List<String> addEightSphere(int numSegments, boolean shouldLoadConditionals) {
        final List<String> result = new ArrayList<>();
        float radius = 1f;
        TCVector[] zeroXPoints;
        TCVector[] zeroYPoints;
        TCVector[] zeroZPoints;
        int usedSegments = numSegments / 4;
        TCVector p1;
        TCVector p2;
        TCVector p3;
        TCVector[] spherePoints = null;
        int numMainPoints = (usedSegments + 1) * (usedSegments + 1) - 1;
        int mainSpot = 0;

        if (shouldLoadConditionals) {
            spherePoints = init(new TCVector[numMainPoints]);
        }

        int loopCount = usedSegments + 1;
        zeroXPoints = init(new TCVector[loopCount > 0 ? loopCount : 0]);
        zeroYPoints = init(new TCVector[loopCount > 0 ? loopCount : 0]);
        zeroZPoints = init(new TCVector[loopCount > 0 ? loopCount : 0]);
        for (int i = 0; i < loopCount; i++) {
            float angle = (float) (2.0f * Math.PI / numSegments * i);

            zeroYPoints[i].setX((float) (1.0f / (Math.tan(angle) + 1)));
            zeroYPoints[i].setY(0.0f);
            zeroYPoints[i].setZ(1.0f - zeroYPoints[i].getX());
            zeroZPoints[i] = zeroYPoints[i].rearrange(2, 0, 1);
            zeroXPoints[i] = zeroYPoints[i].rearrange(1, 2, 0);
        }

        for (int j = 0; j < usedSegments; j++) {
            int stripCount = usedSegments - j;
            int stripSpot = 0;
            TCVector[] points = init(new TCVector[stripCount * 2 + 1]);

            for (int i = 0; i < stripCount; i++) {
                if (i == 0) {
                    p1 = calcIntersection(i, j, usedSegments, zeroXPoints,
                            zeroYPoints, zeroZPoints).copy();
                    p1 = p1.scale(radius / p1.length());
                    points[stripSpot++] = p1;
                    if (shouldLoadConditionals) {
                        spherePoints[mainSpot++] = p1;
                    }
                }

                p2 = calcIntersection(i, j + 1, usedSegments, zeroXPoints,
                        zeroYPoints, zeroZPoints).copy();
                p2 = p2.scale(radius / p2.length());
                p3 = calcIntersection(i + 1, j, usedSegments, zeroXPoints,
                        zeroYPoints, zeroZPoints).copy();
                p3 = p3.scale(radius / p3.length());
                points[stripSpot++] = p2;
                points[stripSpot++] = p3;
                if (shouldLoadConditionals) {
                    spherePoints[mainSpot++] = p2;
                    spherePoints[mainSpot++] = p3;
                }
            }

            addTriangleStrip(points, stripSpot, result);
        }

        if (shouldLoadConditionals) {
            addEighthSphereConditionals(spherePoints, numSegments, result);
        }

        return result;
    }

    private static TCVector[] init(TCVector[] tcVectors) {
        for (int i = 0; i < tcVectors.length; i++) {
            tcVectors[i] = new TCVector(new Vector3f());
        }

        return tcVectors;
    }

    private static void addEighthSphereConditionals(TCVector[] points, int numSegments, List<String> result) {
        int usedSegments = numSegments / 4;
        TCVector p1;
        TCVector p2;
        TCVector p3;
        TCVector p4;

        int mainSpot = 0;

        for (int j = 0; j < usedSegments; j++) {
            int stripCount = usedSegments - j;

            for (int i = 0; i < stripCount; i++) {
                if (i > 0) {
                    p3 = points[mainSpot - 1].copy();
                } else {
                    p3 = points[mainSpot].copy();
                    p3.setZ(p3.getZ() - 1f);
                }
                p4 = points[mainSpot + 2].copy();
                p1 = points[mainSpot].copy();
                p2 = points[mainSpot + 1].copy();
                addConditionalLine(p1, p2, p3, p4, result);
                p3 = p1;
                p1 = p2;
                p2 = p4;
                if (i < stripCount - 1) {
                    p4 = points[mainSpot + 3].copy();
                } else {
                    p4 = points[mainSpot + 1].copy();
                    p4.setX(p4.getX() - 1f);
                }

                addConditionalLine(p1, p2, p3, p4, result);
                p1 = points[mainSpot].copy();
                p2 = points[mainSpot + 2].copy();
                p3 = points[mainSpot + 1].copy();
                if (j == 0) {
                    p4 = points[mainSpot].copy();
                    p4.setY(p4.getY() - 1f);
                } else {
                    p4 = points[sphereIndex(i * 2 + 2, j - 1, usedSegments)].copy();
                }

                addConditionalLine(p1, p2, p3, p4, result);
                mainSpot += 2;
            }

            mainSpot++;
        }
    }

    private static int sphereIndex(int i, int j, int usedSegments) {
        int retVal = 0;
        for (int k = 0; k < j; k++) {
            int rowSize = usedSegments - k;
            retVal += rowSize * 2 + 1;
        }

        return retVal + i;
    }

    private static void addConditionalLine(TCVector p1, TCVector p2, TCVector p3, TCVector p4, List<String> result) {
        final StringBuilder sb = new StringBuilder();
        sb.append("5 24 "); //$NON-NLS-1$
        sb.append(p1.toString());
        sb.append(" "); //$NON-NLS-1$
        sb.append(p2.toString());
        sb.append(" "); //$NON-NLS-1$
        sb.append(p3.toString());
        sb.append(" "); //$NON-NLS-1$
        sb.append(p4.toString());
        result.add(sb.toString());
    }

    private static void addTriangleStrip(TCVector[] points, int count, List<String> result) {
        if (points.length < 3) return;
        boolean flip = false;
        for (int i = 0; i < points.length - 2 && i < count; i++) {
            TCVector a = points[i];
            TCVector b = points[i + 1];
            TCVector c = points[i + 2];
            if (flip) {
                TCVector t = b;
                b = c;
                c = t;
            }

            final StringBuilder sb = new StringBuilder();
            sb.append("3 16 "); //$NON-NLS-1$
            sb.append(a.toString());
            sb.append(" "); //$NON-NLS-1$
            sb.append(b.toString());
            sb.append(" "); //$NON-NLS-1$
            sb.append(c.toString());
            result.add(sb.toString());
            flip = !flip;
        }
    }

    private static TCVector calcIntersection(int i, int j, int num,
            TCVector[] zeroXPoints,
            TCVector[] zeroYPoints,
            TCVector[] zeroZPoints) {

        if (i + j == num) {
            return zeroXPoints[j];
        } else if (i == 0) {
            return zeroZPoints[num - j];
        } else if (j == 0) {
            return zeroYPoints[i];
        }

        TCVector temp1 = zeroYPoints[i];
        TCVector temp2 = zeroXPoints[num - i];
        TCVector temp3 = zeroZPoints[num - j];
        TCVector temp4 = zeroXPoints[j];
        TCVector temp5 = zeroYPoints[i + j];
        TCVector temp6 = zeroZPoints[num - i - j];

        return temp1.add(temp2).add(temp3).add(temp4).add(temp5).add(temp6)
                .sub(zeroXPoints[0]).sub(zeroYPoints[0]).sub(zeroZPoints[0]).scale(1 / 9.0f);
    }


    private record TCVector(Vector3f v) {

        public TCVector copy() {
            return new TCVector(new Vector3f(v));
        }

        public float length() {
            return v.length();
        }

        public TCVector sub(TCVector v) {
            return new TCVector(Vector3f.sub(this.v, v.v, null));
        }

        public TCVector add(TCVector v) {
            return new TCVector(Vector3f.add(this.v, v.v, null));
        }

        public float getX() {
            return v.x;
        }

        public float getY() {
            return v.y;
        }

        public float getZ() {
            return v.z;
        }

        public void setX(float x) {
            v.setX(x);
        }

        public void setY(float y) {
            v.setY(y);
        }

        public void setZ(float z) {
            v.setZ(z);
        }

        public TCVector rearrange(int xi, int yi, int zi) {
            final float x = getCoord(xi);
            final float y = getCoord(yi);
            final float z = getCoord(zi);
            return new TCVector(new Vector3f(x, y, z));
        }

        private float getCoord(int i) {
            if (i == 0) return v.x;
            if (i == 1) return v.y;
            return v.z;
        }

        public TCVector scale(float factor) {
            return new TCVector((Vector3f) v.scale(factor));
        }

        @Override
        public String toString() {
            return v.x + " " + v.y + " " + v.z; //$NON-NLS-1$ //$NON-NLS-2$
        }}
}
