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

import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GData3;
import org.nschmidt.ldparteditor.data.GData4;
import org.nschmidt.ldparteditor.helper.math.PowerRay;



public class RNode {

    private static final PowerRay POWER_RAY = new PowerRay();

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

    public void insertGeometry(GData geometry) {
        this.geometry = geometry;
        bb = new BoundingBox();
        bb.insert(geometry);
    }

    public void split() {
        if (isLeaf()) {
            final RNode node = new RNode();
            children[0] = node;
            node.bb = bb.copy();
            node.geometry = geometry;
            this.geometry = null;
        }
    }

    public void backpropagate(GData geometry) {
        bb.insert(geometry);
        if (parent != null) {
            parent.backpropagate(geometry);
        }
    }

    public boolean pointsToLeaves() {
        return children[0].isLeaf() && children[1].isLeaf();
    }

    public List<GData> retrieveGeometryDataOnRay(Vector4f rayOrigin, float[] rayDirection, List<GData> resultList) {
        if (isLeaf()) {
            if (geometry instanceof GData3 triangle) {
                return testRayTriangle(rayOrigin, rayDirection, triangle, resultList);
            } else if (geometry instanceof GData4 quad) {
                return testRayQuad(rayOrigin, rayDirection, quad, resultList);
            }
        } else if (bb.isIntersecting(rayOrigin, rayDirection)) {
            for (RNode c : children) {
                c.retrieveGeometryDataOnRay(rayOrigin, rayDirection, resultList);
            }
        }

        return resultList;
    }

    private List<GData> testRayTriangle(Vector4f rayOrigin, float[] rayDirection, GData3 triangle, List<GData> resultList) {
        float[] triangleData = new float[] {
                triangle.x1, triangle.y1, triangle.z1,
                triangle.x2, triangle.y2, triangle.z2,
                triangle.x3, triangle.y3, triangle.z3
        };

        float[] result = POWER_RAY.triangleIntersect(rayOrigin, rayDirection, triangleData);

        if (result.length > 5) {
            float t = result[5];
            if (t >= 0 && t <= 1f) {
                resultList.add(geometry);
            }
        }

        return resultList;
    }

    private List<GData> testRayQuad(Vector4f rayOrigin, float[] rayDirection, GData4 quad, List<GData> resultList) {
        // TODO: Check GData intersection!
        resultList.add(geometry);
        return resultList;
    }
}
