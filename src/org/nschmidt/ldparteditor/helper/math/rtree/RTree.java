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

import org.nschmidt.ldparteditor.data.GData;

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

    public void add(GData geometry) {
        // Trivial case for an empty tree.
        if (root.isClear()) {
            root.insertGeometry(geometry);
            return;
        }
    }
}
