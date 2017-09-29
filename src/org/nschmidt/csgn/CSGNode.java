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
package org.nschmidt.csgn;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class CSGNode implements Comparable<CSGNode> {

    private CSGNode frontNode = null;
    private CSGNode backNode = null;

    private List<Triangle> front = null;
    private List<Triangle> back = null;
    private Triangle triangle;
    private static int lastSortResult;
    private static List<Triangle> lastSide = null;
    public static CSGNode oldNode = null;
    private final boolean isRoot;

    public CSGNode(List<Triangle> intersectOther) {
        isRoot = true;
        front = intersectOther;
        triangle = new Triangle(null,
                new Vector3d(0.0, -1E6, 0.0),
                new Vector3d(0.0, -1E6, 1.0),
                new Vector3d(1.0, -1E6, 0.0),
                null);
    }

    public CSGNode(Triangle t) {
        isRoot = false;
        triangle = t;
    }

    public List<Triangle> getFront() {
        return front;
    }

    public List<Triangle> getBack() {
        return back;
    }

    @Override
    public int compareTo(CSGNode o) {

        final Vector3d[] verts = triangle.vertices;
        final Plane tp = o.triangle.plane;

        final Location loc1 = tp.getPointLocation(verts[0]);
        final Location loc2 = tp.getPointLocation(verts[1]);
        final Location loc3 = tp.getPointLocation(verts[2]);

        int frontCount = loc1 == Location.FRONT ? 1 : 0;
        frontCount += loc2 == Location.FRONT ? 1 : 0;
        frontCount += loc3 == Location.FRONT ? 1 : 0;

        int backCount = loc1 == Location.BACK ? 1 : 0;
        backCount += loc2 == Location.BACK ? 1 : 0;
        backCount += loc3 == Location.BACK ? 1 : 0;

        if (o.isRoot || (frontCount > 0 && backCount == 0)) {
            lastSide = o.front;
            lastSortResult = 1;
        } else if (backCount > 0 && frontCount == 0) {
            lastSide = o.back;
            lastSortResult = -1;
        } else if (frontCount == 0 && backCount == 0) {
            if (tp.normal.dot(o.triangle.plane.normal) > 0) {
                lastSide = o.front;
                lastSortResult = 1;
            } else {
                lastSide = o.back;
                lastSortResult = -1;
            }
        } else {
            lastSortResult = 0;
            oldNode = o;
        }

        return lastSortResult;
    }

    public Object[] splitOther(Triangle t) {
        return t.split(triangle.plane);
    }

    @SuppressWarnings("unchecked")
    public void split() {
        // FIXME CSG needs implementation!
        final Plane p = triangle.plane;
        if (lastSortResult == 0) {
            throw new AssertionError(""); //$NON-NLS-1$
        } else {
            final List<Triangle> newFront = new ArrayList<>();
            back = new ArrayList<>();
            for (Iterator<Triangle> it = lastSide.iterator(); it.hasNext();) {
                Triangle t = it.next();
                Object[] splitResult = t.split(p);
                newFront.addAll((List<Triangle>) splitResult[0]);
                back.addAll((List<Triangle>) splitResult[1]);
                it.remove();
            }
            front = newFront;
        }
    }

    @SuppressWarnings("unchecked")
    public void splitOnExisting(CSGNode node) {
        final Plane p = node.triangle.plane;
        final List<Triangle> newBack = new ArrayList<>();
        final List<Triangle> newFront = new ArrayList<>();
        for (Triangle t : front) {
            Object[] splitResult = t.split(p);
            newFront.addAll((List<Triangle>) splitResult[0]);
            newFront.addAll((List<Triangle>) splitResult[1]);
        }
        for (Triangle t : back) {
            Object[] splitResult = t.split(p);
            newBack.addAll((List<Triangle>) splitResult[0]);
            newBack.addAll((List<Triangle>) splitResult[1]);
        }
        front.clear();
        back.clear();
        back = newBack;
        front = newFront;
    }

    public boolean add(CSGNode newNode) {
        // FIXME Remove recursion!

        final int result = newNode.compareTo(this);
        switch (result) {
        case -1: // back
            if (backNode == null) {
                backNode = newNode;
                return true;
            } else {
                return backNode.add(newNode);
            }
        case 1: // front
            if (frontNode == null) {
                frontNode = newNode;
                return true;
            } else {
                return frontNode.add(newNode);
            }
        default:
            break;
        }
        return false;
    }

}
