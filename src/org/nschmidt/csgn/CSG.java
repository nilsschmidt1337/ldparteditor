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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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

            if (isProbablyInside(o)) {
                nonintersectInsideOther.add(o);
            } else {
                nonintersectOutsideOther.add(o);
            }
        }

        for (int i = 0; i < size; i++) {
            if (nonboundsintersect[i] || intersect[i]) continue;
            Triangle t = triangles.get(i);
            if (csg.isProbablyInside(t)) {
                nonintersectInsideThis.add(t);
            } else {
                nonintersectOutsideThis.add(t);
            }
        }

        intersectOtherCopy.addAll(intersectOther);

        // Copy all polygons into the datastructure


        {
            final Set<CSGNode> nodes = new TreeSet<CSGNode>();
            final CSGNode top = new CSGNode(intersectOther);
            nodes.add(top);
            for (Triangle t : intersectThis) {
                final CSGNode newNode = new CSGNode(t);
                if (nodes.add(newNode)) {
                    newNode.split();
                } else {
                    CSGNode.oldNode.splitOnExisting(newNode);
                }
            }
            final List<Triangle> newOther = new ArrayList<>();
            for (CSGNode n : nodes) {
                if (n != top) {
                    newOther.addAll(n.getFront());
                    newOther.addAll(n.getBack());
                }
            }
            intersectOther.clear();
            intersectOther.addAll(newOther);
        }

        {
            final Set<CSGNode> nodes = new TreeSet<CSGNode>();
            final CSGNode top = new CSGNode(intersectThis);
            nodes.add(top);
            for (Triangle t : intersectOtherCopy) {
                final CSGNode newNode = new CSGNode(t);
                if (nodes.add(newNode)) {
                    newNode.split();
                } else {
                    CSGNode.oldNode.splitOnExisting(newNode);
                }
            }
            final List<Triangle> newThis = new ArrayList<>();
            for (CSGNode n : nodes) {
                if (n != top) {
                    newThis.addAll(n.getFront());
                    newThis.addAll(n.getBack());
                }
            }
            intersectThis.clear();
            intersectThis.addAll(newThis);
        }

        for (Triangle t : intersectThis) {
            if (csg.isProbablyInside(t)) {
                nonintersectInsideThis.add(t);
            } else {
                nonintersectOutsideThis.add(t);
            }
        }

        for (Triangle o : intersectOther) {
            if (isProbablyInside(o)) {
                nonintersectInsideOther.add(o);
            } else {
                nonintersectOutsideOther.add(o);
            }
        }
    }

    private boolean isProbablyInside(Triangle test) {
        int counter = 0;
        final Vector3d tv1 = test.vertices[0];
        final Vector3d tv2 = test.vertices[1];
        final Vector3d tv3 = test.vertices[2];
        final double[] orig = new double[3];
        double a = 0.33333333;
        double b = 0.33333333;
        double c = 0.33333333;
        double sum;
        for (int i = 0; i < 10; i++) {
            counter = 0;
            orig[0] = a * tv1.x + b * tv2.x + c * tv3.x;
            orig[1] = a * tv1.y + b * tv2.y + c * tv3.y;
            orig[2] = a * tv1.z + b * tv2.z + c * tv3.z;
            for (int j = 0; j < 20; j++) {
                counter += isInsideHelper(test, orig);
                if (counter > 5 || counter < -5) {
                    return counter > 0;
                }
            }
            a = Math.random();
            b = Math.random();
            c = Math.random();
            sum = a + b + c;
            a = a / sum;
            b = b / sum;
            c = c / sum;
        }
        return counter > 0;
    }

    private int isInsideHelper(Triangle test, double[] orig) {
        double rx = 0.0;
        double ry = 0.0;
        double rz = 0.0;
        do {
            rx = Math.random() - 0.5;
            ry = Math.random() - 0.5;
            rz = Math.random() - 0.5;
        } while (rx == 0.0 || ry == 0.0 || rz == 0.0);
        double len = Math.sqrt(rx * rx + ry * ry + rz * rz);
        final double[] dir1 = new double[]{rx / len, ry / len, rz / len};



        intersections.clear();

        for (final Triangle t : triangles) {
            final Vector3d ov1 = t.vertices[0];
            final Vector3d ov2 = t.vertices[1];
            final Vector3d ov3 = t.vertices[2];
            v1[0] = ov1.x;
            v1[1] = ov1.y;
            v1[2] = ov1.z;
            v2[0] = ov2.x;
            v2[1] = ov2.y;
            v2[2] = ov2.z;
            v3[0] = ov3.x;
            v3[1] = ov3.y;
            v3[2] = ov3.z;
            if (TRIANGLE_INTERSECT(orig, dir1, v1, v2, v3)) {
                intersections.add(intersectionPoint);
            }
        }

        return  2 * (intersections.size() % 2) - 1;
    }

    private double t, u, v;

    final TreeSet<Vector3d> intersections = new TreeSet<>();
    final double[] v1 = new double[3];
    final double[] v2 = new double[3];
    final double[] v3 = new double[3];
    private double[] corner1 = new double[3];
    private double[] corner2 = new double[3];
    private double[] tvec = new double[3];
    private double[] pvec = new double[3];
    private double[] qvec = new double[3];
    private Vector3d intersectionPoint;

    private final double TOLERANCE = 0.00001d;

    public boolean TRIANGLE_INTERSECT(double[] orig, double[] dir, double[] vert0, double[] vert1, double[] vert2) {
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
        if (t < 0) return false;
        intersectionPoint = new Vector3d(
                t * dir[0] + orig[0],
                t * dir[1] + orig[1],
                t * dir[2] + orig[2]);
        return true;
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
