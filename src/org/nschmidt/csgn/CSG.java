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
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

import org.lwjgl.util.vector.Matrix4f;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.GData3;
import org.nschmidt.ldparteditor.enums.View;

public class CSG {

    TreeMap<GData3, Integer> result = new TreeMap<GData3, Integer>();

    private List<Triangle> triangles = new ArrayList<>();
    private List<Triangle> intersectThis = new ArrayList<>();
    private List<Triangle> intersectOther = new ArrayList<>();
    private List<Triangle> intersectOtherCopy = new ArrayList<>();
    private List<Triangle> newTrianglesThis = new ArrayList<>();
    private List<Triangle> newTrianglesOther = new ArrayList<>();
    private List<Triangle> nonintersectInsideThis = new ArrayList<>();
    private List<Triangle> nonintersectInsideOther = new ArrayList<>();
    private List<Triangle> nonintersectOutsideThis = new ArrayList<>();
    private List<Triangle> nonintersectOutsideOther = new ArrayList<>();
    private List<Triangle> nonintersectOther = new ArrayList<>();
    private List<Triangle> nonintersectThis = new ArrayList<>();
    private Bounds boundsCache = null;

    private CSG() {
    }

    public static final byte UNION = 0;
    public static final byte DIFFERENCE = 1;
    public static final byte INTERSECTION = 2;
    public static final byte CUBOID = 3;
    public static final byte ELLIPSOID = 4;
    public static final byte QUAD = 5;
    public static final byte CYLINDER = 6;
    public static final byte CIRCLE = 7;
    public static final byte COMPILE = 8;
    public static final byte QUALITY = 9;
    public static final byte EPSILON = 10;
    public static final byte CONE = 11;
    public static final byte TRANSFORM = 12;
    public static final byte MESH = 13;
    public static final byte EXTRUDE = 14;
    public static final byte EXTRUDE_CFG = 15;


    /**
     * Returns a transformed copy of this CSG.
     *
     * @param transform
     *            the transform to apply
     *
     * @return a transformed copy of this CSG
     */
    public CSG transformed(Matrix4f transform) {
        return transformed(new Transform().apply(transform));
    }

    /**
     * Returns a transformed copy of this CSG.
     *
     * @param transform
     *            the transform to apply
     *
     * @return a transformed copy of this CSG
     */
    public CSG transformed(Transform transform) {
        List<Triangle> newtriangles = new ArrayList<>();
        for (Triangle t : triangles) {
            newtriangles.add(t.transformed(transform));
        }
        CSG result = CSG.fromTriangles(newtriangles);
        return result;
    }

    /**
     * Returns a transformed copy of this CSG.
     *
     * @param transform
     *            the transform to apply
     * @param ID
     *
     * @return a transformed copy of this CSG
     */
    public CSG transformed(Transform transform, GColour c, int ID) {
        List<Triangle> newtriangles = new ArrayList<>();
        for (Triangle t : triangles) {
            newtriangles.add(t.transformed(transform, c, ID));
        }
        CSG result = CSG.fromTriangles(newtriangles);
        return result;
    }

    public TreeMap<GData3, Integer> getResult() {
        return result;
    }

    public List<Triangle> getTriangles() {
        return triangles;
    }

    public void draw(Composite3D c3d) {
        for (GData3 tri : result.keySet()) {
            tri.drawGL20(c3d);
        }
    }

    public void draw_textured(Composite3D c3d) {
        for (GData3 tri : result.keySet()) {
            tri.drawGL20_BFC_Textured(c3d);
        }
    }

    public CSG difference(CSG csg) {
        List<Triangle> newtriangles = new ArrayList<>();
        if (getBounds().intersects(csg.getBounds())) {
            collisionAndIntersectionCheck(csg);
            for (Triangle t : nonintersectInsideOther) {
                newtriangles.add(t.clone().flip());
            }
            newtriangles.addAll(nonintersectOutsideThis);
        } else {
            newtriangles.addAll(triangles);
        }
        return CSG.fromTriangles(newtriangles);
    }

    public CSG intersect(CSG csg) {
        List<Triangle> newtriangles = new ArrayList<>();
        if (getBounds().intersects(csg.getBounds())) {
            collisionAndIntersectionCheck(csg);
            newtriangles.addAll(nonintersectInsideThis);
            newtriangles.addAll(nonintersectInsideOther);
        }
        return CSG.fromTriangles(newtriangles);
    }

    public CSG transformed(Matrix4f transform, GColour c, int ID) {
        return transformed(new Transform().apply(transform), c, ID);
    }

    public CSG union(CSG csg) {
        List<Triangle> newtriangles = new ArrayList<>();
        if (getBounds().intersects(csg.getBounds())) {
            collisionAndIntersectionCheck(csg);
            newtriangles.addAll(nonintersectOutsideThis);
            newtriangles.addAll(nonintersectOutsideOther);
        } else {
            newtriangles.addAll(triangles);
            newtriangles.addAll(csg.triangles);
        }
        return CSG.fromTriangles(newtriangles);
    }

    public Bounds getBounds() {
        Bounds result = boundsCache;
        if (result == null) {
            if (!triangles.isEmpty()) {
                result = new Bounds();
                for (Triangle t : triangles) {
                    Bounds b = t.getBounds();
                    result.union(b);
                }
            } else {
                result = new Bounds(new Vector3d(0, 0, 0), new Vector3d(0, 0, 0));
            }
            boundsCache = result;
        }
        return result;
    }

    private void collisionAndIntersectionCheck(CSG csg) {
        final Bounds tb = getBounds();
        final Bounds ob = csg.getBounds();
        final int size = triangles.size();
        final boolean[] nonboundsintersect = new boolean[size];
        final boolean[] intersect = new boolean[size];

        nonintersectInsideThis.clear();
        nonintersectInsideOther.clear();
        nonintersectOutsideThis.clear();
        nonintersectOutsideOther.clear();
        nonintersectOther.clear();
        nonintersectThis.clear();

        intersectThis.clear();
        intersectOther.clear();
        intersectOtherCopy.clear();
        newTrianglesThis.clear();
        newTrianglesOther.clear();

        for (int i = 0; i < size; i++) {
            Triangle t = triangles.get(i);
            if (!ob.intersects(t.getBounds())) {
                nonintersectOutsideThis.add(t);
                nonboundsintersect[i] = true;
            }
        }

        for (Triangle o : csg.triangles) {
            if (!tb.intersects(o.getBounds())) {
                nonintersectOutsideOther.add(o);
                continue;
            }

            boolean otherIntersects = false;
            for (int i = 0; i < size; i++) {
                if (nonboundsintersect[i]) continue;
                Triangle t = triangles.get(i);
                if (t.intersectsBoundingBox(o)) {
                    if (!otherIntersects) {
                        intersectOther.add(o);
                        otherIntersects = true;
                    }
                    if (!intersect[i]) {
                        intersectThis.add(t);
                        intersect[i] = true;
                    }
                }
            }
            if (otherIntersects) continue;

            intersectOther.add(o);
            nonintersectOther.add(o);
            o.setUnbreakable(true);
        }

        for (int i = 0; i < size; i++) {
            if (nonboundsintersect[i] || intersect[i]) continue;
            Triangle t = triangles.get(i);
            intersectThis.add(t);
            nonintersectThis.add(t);
            t.setUnbreakable(true);
        }

        intersectOtherCopy.addAll(intersectOther);

        // Copy all polygons into the datastructure


        {
            final List<CSGNode> nodes = new ArrayList<CSGNode>(intersectThis.size());
            final CSGNode top = new CSGNode(intersectOther);
            for (Triangle t : intersectThis) {
                final CSGNode newNode = new CSGNode(t);
                if (top.add(newNode)) {
                    newNode.split();
                    nodes.add(newNode);
                } else {
                    CSGNode.oldNode.splitOnExisting(newNode);
                }
            }
            for (CSGNode n : nodes) {
                if (n != top) {
                    nonintersectInsideOther.addAll(n.getBack());
                    nonintersectOutsideOther.addAll(n.getFront());
                }

            }
        }

        {
            final List<CSGNode> nodes = new ArrayList<CSGNode>(intersectOtherCopy.size());
            final CSGNode top = new CSGNode(intersectThis);
            for (Triangle t : intersectOtherCopy) {
                final CSGNode newNode = new CSGNode(t);
                if (top.add(newNode)) {
                    newNode.split();
                    nodes.add(newNode);
                } else {
                    CSGNode.oldNode.splitOnExisting(newNode);
                }
            }
            for (CSGNode n : nodes) {
                if (n != top) {
                    nonintersectInsideThis.addAll(n.getBack());
                    nonintersectOutsideThis.addAll(n.getFront());
                }
            }
        }
    }

    /**
     * Returns this csg as list of LDraw triangles
     *
     * @return this csg as list of LDraw triangles
     */
    public TreeMap<GData3, Integer> toLDrawTriangles(GData1 parent) {
        TreeMap<GData3, Integer> result = new TreeMap<GData3, Integer>();
        for (Triangle p : this.triangles) {
            result.putAll(p.toLDrawTriangle(parent));
        }
        return result;
    }

    public GData1 compile() {
        Matrix4f id = new Matrix4f();
        Matrix4f.setIdentity(id);
        GColour col = View.getLDConfigColour(16);
        GData1 g1 = new GData1(-1, col.getR(), col.getG(), col.getB(), 1f, id, View.ACCURATE_ID, new ArrayList<String>(), null, null, 1, false, id, View.ACCURATE_ID, null, View.DUMMY_REFERENCE, true, false,
                new HashSet<String>(), View.DUMMY_REFERENCE);
        this.result = toLDrawTriangles(g1);
        return g1;
    }

    public void compile_without_t_junctions(DatFile df) {
        // TODO Auto-generated method stub
        compile();
    }

    public static CSG fromTriangles(List<Triangle> triangles) {
        CSG csg = new CSG();
        csg.triangles = triangles;
        return csg;
    }

    @Override
    public String toString() {
        return "triangles: " + triangles.size(); //$NON-NLS-1$
    }
}
