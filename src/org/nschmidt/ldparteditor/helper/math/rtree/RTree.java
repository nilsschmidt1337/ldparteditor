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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GData3;
import org.nschmidt.ldparteditor.data.GData4;

public class RTree {


    /* [1]
     * Beckmann N., Kriegel H.-P., Schneider R., and Seeger B.
     * The r*-tree: an efficient and robust access method for points and rectangles
     * SIGMOD 1990 New York ACM Press 322-331
     */

    /* [1]

        Algorithm
        ChooseSubtree
        CS1: Set N to be the root
        CS2: If N is a leaf,
               return N
             else
               if the child-pointers in N point to leaves
               [determine the minimum overlap cost],
               choose the entry in N whose rectangle needs least
               overlap enlargement to include the new data rectangle.
               Resolve ties by choosing the entry
               whose rectangle needs least area enlargement,
             then
               the entry with the rectangle of smallest area
             if the child-pointers in N do not point to leaves
               [determine the minimum area cost],
               choose the entry in N whose rectangle needs least
               area enlargement to include the new data rectangle
               Resolve ties by choosing the entry
               with the rectangle of smallest area
            end
        CS3: Set N to be the child-node pointed to by the
             child-pointer of the chosen entry and repeat from CS2.
    */

    /*
     * What is area enlargement?
     *
     * given
     * BoundingBox b
     *
     * oldArea = b.area
     * b.insert(...)
     * newArea = b.area
     *
     * areaEnlargement = newArea - oldArea
     */

    /*
     * What is overlap enlargement?
     * given
     * BoundingBox a
     * BoundingBox b
     *
     * overlapEnlargement = a.intersection(b).area
     */

    private RNode root = new RNode();

    private int size = 0;

    public void add(GData geometry) {
        // Trivial case for an empty tree.
        if (root.isClear()) {
            root.insertGeometry(geometry);
            size += 1;
            return;
        }

        final BoundingBox bb = new BoundingBox();
        bb.insert(geometry);

        add(geometry, bb, root);

        size += 1;
    }

    private void add(GData geometry, BoundingBox bb, RNode node) {
        // CS2: Choose N if it is a leaf
        if (node.isLeaf()) {
            // Insert it here
            node.split();
            node.backpropagate(geometry);
            RNode newNode = new RNode();
            newNode.insertGeometry(geometry);
            newNode.parent = node;
            node.children[1] = newNode;
            return;
        }

        // CS2: If the child-pointers in N point to leaves
        if (node.pointsToLeaves()) {
            // choose the entry in N whose rectangle needs least
            // overlap enlargement to include the new data rectangle.
            final float overlapA = node.children[0].bb.intersection(bb).areaHalf();
            final float overlapB = node.children[1].bb.intersection(bb).areaHalf();
            if (overlapA < overlapB) {
                add(geometry, bb, node.children[0]);
            } else {
                add(geometry, bb, node.children[1]);
            }

            return;
        }

        // CS2: If the child-pointers in N do not point to leaves
        // choose the entry in N whose rectangle needs least
        // area enlargement to include the new data rectangle.
        final BoundingBox boundingBoxA = bb.copy();
        final BoundingBox boundingBoxB = bb.copy();
        boundingBoxA.insert(node.children[0].bb);
        boundingBoxB.insert(node.children[1].bb);
        final float areaIncreaseA =  boundingBoxA.areaHalf() - node.children[0].bb.areaHalf();
        final float areaIncreaseB =  boundingBoxB.areaHalf() - node.children[1].bb.areaHalf();
        if (areaIncreaseA < areaIncreaseB) {
            add(geometry, bb, node.children[0]);
        } else {
            add(geometry, bb, node.children[1]);
        }
    }

    public List<GData> searchGeometryDataOnSegment(float startX, float startY, float startZ, float endX, float endY, float endZ) {
        final Vector4f rayOrigin = new Vector4f(startX, startY, startZ, 1f);
        final float[] rayDirection = new float[] {endX - startX, endY - startY, endZ - startZ};

        final List<GData> resultList = new LinkedList<>();
        root.retrieveGeometryDataOnRay(rayOrigin, rayDirection, resultList);
        return resultList;
    }

    public Set<GData> searchForIntersections(GData geometry) {
        if (geometry instanceof GData3 triangle) {
            return searchForIntersections(triangle);
        } else if (geometry instanceof GData4 quad) {
            return searchForIntersections(quad);
        }

        return Set.of();
    }

    private Set<GData> searchForIntersections(GData3 triangle) {
        final Set<GData> result = new HashSet<>();
        final List<GData> resultAB = searchGeometryDataOnSegment(triangle.x1, triangle.y1, triangle.z1, triangle.x2, triangle.y2, triangle.z2);
        final List<GData> resultBC = searchGeometryDataOnSegment(triangle.x2, triangle.y2, triangle.z2, triangle.x3, triangle.y3, triangle.z3);
        final List<GData> resultCA = searchGeometryDataOnSegment(triangle.x3, triangle.y3, triangle.z3, triangle.x1, triangle.y1, triangle.z1);
        result.addAll(resultAB);
        result.addAll(resultBC);
        result.addAll(resultCA);
        result.remove(triangle);
        return result;
    }

    private Set<GData> searchForIntersections(GData4 quad) {
        final Set<GData> result = new HashSet<>();
        final List<GData> resultAB = searchGeometryDataOnSegment(quad.x1, quad.y1, quad.z1, quad.x2, quad.y2, quad.z2);
        final List<GData> resultBC = searchGeometryDataOnSegment(quad.x2, quad.y2, quad.z2, quad.x3, quad.y3, quad.z3);
        final List<GData> resultCD = searchGeometryDataOnSegment(quad.x3, quad.y3, quad.z3, quad.x4, quad.y4, quad.z4);
        final List<GData> resultDA = searchGeometryDataOnSegment(quad.x4, quad.y4, quad.z4, quad.x1, quad.y1, quad.z1);
        result.addAll(resultAB);
        result.addAll(resultBC);
        result.addAll(resultCD);
        result.addAll(resultDA);
        result.remove(quad);
        return result;
    }

    public int size() {
        return size;
    }
}
