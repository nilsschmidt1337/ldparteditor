/**
 * CSG.java
 *
 * Copyright 2014-2014 Michael Hoffer <info@michaelhoffer.de>. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of Michael Hoffer
 * <info@michaelhoffer.de>.
 */
package org.nschmidt.csg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import org.lwjgl.util.vector.Matrix4f;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.GData3;
import org.nschmidt.ldparteditor.data.GDataCSG;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.logger.NLogger;

/**
 * Constructive Solid Geometry (CSG).
 *
 * This implementation is a Java port of <a
 * href="https://github.com/evanw/csg.js/">https://github.com/evanw/csg.js/</a>
 * with some additional features like polygon extrude, transformations etc.
 * Thanks to the author for creating the CSG.js library.<br>
 * <br>
 *
 * <b>Implementation Details</b>
 *
 * All CSG operations are implemented in terms of two functions,
 * {@link Node#clipTo(org.nschmidt.csg.Node)} and {@link Node#invert()}, which
 * remove parts of a BSP tree inside another BSP tree and swap solid and empty
 * space, respectively. To find the union of {@code a} and {@code b}, we want to
 * remove everything in {@code a} inside {@code b} and everything in {@code b}
 * inside {@code a}, then combine polygons from {@code a} and {@code b} into one
 * solid:
 *
 * <blockquote>
 *
 * <pre>
 * a.clipTo(b);
 * b.clipTo(a);
 * a.build(b.allPolygons());
 * </pre>
 *
 * </blockquote>
 *
 * The only tricky part is handling overlapping coplanar polygons in both trees.
 * The code above keeps both copies, but we need to keep them in one tree and
 * remove them in the other tree. To remove them from {@code b} we can clip the
 * inverse of {@code b} against {@code a}. The code for union now looks like
 * this:
 *
 * <blockquote>
 *
 * <pre>
 * a.clipTo(b);
 * b.clipTo(a);
 * b.invert();
 * b.clipTo(a);
 * b.invert();
 * a.build(b.allPolygons());
 * </pre>
 *
 * </blockquote>
 *
 * Subtraction and intersection naturally follow from set operations. If union
 * is {@code A | B}, differenceion is {@code A - B = ~(~A | B)} and intersection
 * is {@code A & B =
 * ~(~A | ~B)} where {@code ~} is the complement operator.
 */
public class CSG {

    TreeMap<GData3, Integer> result = new TreeMap<GData3, Integer>();

    private List<Polygon> polygons;
    private Bounds bounds = null;

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
     * Constructs a CSG from a list of {@link Polygon} instances.
     *
     * @param polygons
     *            polygons
     * @return a CSG instance
     */
    public static CSG fromPolygons(List<Polygon> polygons) {

        CSG csg = new CSG();
        csg.polygons = polygons;
        return csg;
    }

    /**
     * Constructs a CSG from the specified {@link Polygon} instances.
     *
     * @param polygons
     *            polygons
     * @return a CSG instance
     */
    public static CSG fromPolygons(Polygon... polygons) {
        return fromPolygons(Arrays.asList(polygons));
    }

    @Override
    public CSG clone() {
        CSG csg = new CSG();

        csg.polygons = new ArrayList<Polygon>();
        for (Polygon polygon : polygons) {
            csg.polygons.add(polygon.clone());
        }

        return csg;
    }

    /**
     *
     * @return the polygons of this CSG
     */
    public List<Polygon> getPolygons() {
        return polygons;
    }

    /**
     * Return a new CSG solid representing the union of this csg and the
     * specified csg.
     *
     * <b>Note:</b> Neither this csg nor the specified csg are modified.
     *
     * <blockquote>
     *
     * <pre>
     *    A.union(B)
     *
     *    +-------+            +-------+
     *    |       |            |       |
     *    |   A   |            |       |
     *    |    +--+----+   =   |       +----+
     *    +----+--+    |       +----+       |
     *         |   B   |            |       |
     *         |       |            |       |
     *         +-------+            +-------+
     * </pre>
     *
     * </blockquote>
     *
     *
     * @param csg
     *            other csg
     *
     * @return union of this csg and the specified csg
     */
    /*public CSG union(CSG csg) {

        List<Polygon> inner = new ArrayList<Polygon>();
        List<Polygon> outer = new ArrayList<Polygon>();

        Bounds bounds = csg.getBounds();

        for (Polygon p : this.polygons) {
            if (bounds.intersects(p.getBounds())) {
                inner.add(p);
            } else {
                outer.add(p);
            }
        }

        List<Polygon> allPolygons = new ArrayList<Polygon>();

        if (!inner.isEmpty()) {
            CSG innerCSG = CSG.fromPolygons(inner);

            allPolygons.addAll(outer);
            allPolygons.addAll(innerCSG._unionNoOpt(csg).polygons);
        } else {
            allPolygons.addAll(this.polygons);
            allPolygons.addAll(csg.polygons);
        }

        return CSG.fromPolygons(allPolygons);
    }*/

    public CSG union(CSG csg) {

        final List<Polygon> thisPolys = this.clone().polygons;
        final List<Polygon> otherPolys = csg.clone().polygons;
        final Bounds thisBounds = this.getBounds();
        final Bounds otherBounds = csg.getBounds();

        final List<Polygon> nonIntersectingPolys = new ArrayList<>();

        thisPolys.removeIf((poly) -> {
           final boolean result;
           if (result = !otherBounds.intersects(poly.getBounds())) {
               nonIntersectingPolys.add(poly);
           }
           return result;
        });

        otherPolys.removeIf((poly) -> {
            final boolean result;
            if (result = !thisBounds.intersects(poly.getBounds())) {
                nonIntersectingPolys.add(poly);
            }
            return result;
         });

        Node a = new Node(thisPolys);
        Node b = new Node(otherPolys);
        a.clipTo(b);
        b.clipTo(a);
        b.invert();
        b.clipTo(a);
        b.invert();

        Stack<NodePolygon> st = new Stack<>();
        st.push(new NodePolygon(a, b.allPolygons()));
        while (!st.isEmpty()) {
            NodePolygon np = st.pop();
            List<NodePolygon> npr = np.getNode().build(np.getPolygons());
            for (NodePolygon np2 : npr) {
                st.push(np2);
            }
        }

        final List<Polygon> resultPolys = a.allPolygons();
        resultPolys.addAll(nonIntersectingPolys);

        return CSG.fromPolygons(resultPolys);
    }

    /**
     * Return a new CSG solid representing the difference of this csg and the
     * specified csg.
     *
     * <b>Note:</b> Neither this csg nor the specified csg are modified.
     *
     * <blockquote>
     *
     * <pre>
     * A.difference(B)
     *
     * +-------+            +-------+
     * |       |            |       |
     * |   A   |            |       |
     * |    +--+----+   =   |    +--+
     * +----+--+    |       +----+
     *      |   B   |
     *      |       |
     *      +-------+
     * </pre>
     *
     * </blockquote>
     *
     * @param csg
     *            other csg
     * @return difference of this csg and the specified csg
     */
    public CSG difference(CSG csg) {

        List<Polygon> inner = new ArrayList<Polygon>();
        List<Polygon> outer = new ArrayList<Polygon>();

        Bounds bounds = csg.getBounds();

        for (Polygon p : this.polygons) {
            if (bounds.intersects(p.getBounds())) {
                inner.add(p);
            } else {
                outer.add(p);
            }
        }

        CSG innerCSG = CSG.fromPolygons(inner);

        List<Polygon> allPolygons = new ArrayList<Polygon>();
        allPolygons.addAll(outer);
        allPolygons.addAll(innerCSG._differenceNoOpt(csg).polygons);

        return CSG.fromPolygons(allPolygons);
    }

    private CSG _differenceNoOpt(CSG csg) {

        Node a = new Node(this.clone().polygons);
        Node b = new Node(csg.clone().polygons);

        a.invert();
        a.clipTo(b);
        b.clipTo(a);
        b.invert();
        b.clipTo(a);
        b.invert();

        Stack<NodePolygon> st = new Stack<>();
        st.push(new NodePolygon(a, b.allPolygons()));
        while (!st.isEmpty()) {
            NodePolygon np = st.pop();
            List<NodePolygon> npr = np.getNode().build(np.getPolygons());
            for (NodePolygon np2 : npr) {
                st.push(np2);
            }
        }

        a.invert();

        CSG csgA = CSG.fromPolygons(a.allPolygons());
        return csgA;
    }

    /**
     * Return a new CSG solid representing the intersection of this csg and the
     * specified csg.
     *
     * <b>Note:</b> Neither this csg nor the specified csg are modified.
     *
     * <blockquote>
     *
     * <pre>
     *     A.intersect(B)
     *
     *     +-------+
     *     |       |
     *     |   A   |
     *     |    +--+----+   =   +--+
     *     +----+--+    |       +--+
     *          |   B   |
     *          |       |
     *          +-------+
     * }
     * </pre>
     *
     * </blockquote>
     *
     * @param csg
     *            other csg
     * @return intersection of this csg and the specified csg
     */
    public CSG intersect(CSG csg) {
        Node a = new Node(this.clone().polygons);
        Node b = new Node(csg.clone().polygons);
        a.invert();
        b.clipTo(a);
        b.invert();
        a.clipTo(b);
        b.clipTo(a);

        // a.build(b.allPolygons());

        Stack<NodePolygon> st = new Stack<>();
        st.push(new NodePolygon(a, b.allPolygons()));
        while (!st.isEmpty()) {
            NodePolygon np = st.pop();
            List<NodePolygon> npr = np.getNode().build(np.getPolygons());
            for (NodePolygon np2 : npr) {
                st.push(np2);
            }
        }

        a.invert();
        return CSG.fromPolygons(a.allPolygons());
    }

    /**
     * Returns this csg as list of LDraw triangles
     *
     * @return this csg as list of LDraw triangles
     */
    public TreeMap<GData3, Integer> toLDrawTriangles(GData1 parent) {
        TreeMap<GData3, Integer> result = new TreeMap<GData3, Integer>();
        for (Polygon p : this.polygons) {
            result.putAll(p.toLDrawTriangles(parent));
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

    public GData1 compile_without_t_junctions(DatFile df) {

        // Copy... and remove duplicates
        ForkJoinPool forkJoinPool = new ForkJoinPool(View.NUM_CORES);
        try {
            forkJoinPool.submit(() -> {
                this.polygons.parallelStream().forEach((poly) -> {
                    poly.vertices = new ArrayList<>(poly.vertices);
                    ArrayList<Vector3d> vertices = new ArrayList<>();
                    for (Vector3d v1 :  poly.vertices) {
                        for (Vector3d v2 :  poly.vertices) {
                            if (v1 != v2) {
                                if (v1.minus(v2).magnitude() < 0.01) {
                                    if (!vertices.contains(v2)) vertices.add(v1);
                                }
                            }
                        }
                    }
                    poly.vertices.removeAll(vertices);
                });
            }).get();
        } catch (InterruptedException e) {
            NLogger.error(getClass(), e);
        } catch (ExecutionException e) {
            NLogger.error(getClass(), e);
        }


        // 1. The interpolation has to be propagated to other polygons
        // This process can be done in parallel, since the polygons are independent from each other.

        final List<Vector3d[]> splitList = Collections.synchronizedList(GDataCSG.getNewPolyVertices(df));
        try {
            forkJoinPool.submit(() -> {
                this.polygons.parallelStream().forEach((poly) -> {
                    final List<Vector3d> verts = poly.vertices;
                    for (Vector3d[] split : splitList) {
                        final Vector3d vi = split[0];
                        final Vector3d vj = split[1];
                        final Vector3d v = split[2];
                        final int size = verts.size();
                        for (int k = 0; k < size; k++) {
                            int l = (k + 1) % size;
                            if (verts.get(k).equals(vi) && verts.get(l).equals(vj)) {
                                verts.add(l, v.clone());
                                break;
                            } else if (verts.get(l).equals(vi) && verts.get(k).equals(vj)) {
                                verts.add(l, v.clone());
                                break;
                            }
                        }
                    }
                });
            }).get();
        } catch (InterruptedException e) {
            NLogger.error(getClass(), e);
        } catch (ExecutionException e) {
            NLogger.error(getClass(), e);
        }

        // 2. Find and fix T-Junctions
        // This process can be done in parallel, since the polygons are independent from each other.

        final List<Vector3d> allVerts;
        {
            final Set<Vector3d> allVertsSet = new HashSet<Vector3d>();
            this.polygons.stream().forEach((poly) -> {
                allVertsSet.addAll(poly.vertices);
            });
            allVerts = Collections.synchronizedList(new ArrayList<Vector3d>(allVertsSet));
        }

        // Find T-Junctions
        try {
            forkJoinPool.submit(() -> {
                this.polygons.parallelStream().forEach((poly) -> {
                    final List<Vector3d> verts = poly.vertices;
                    double min_dist = Double.MAX_VALUE;
                    for (Vector3d v : allVerts) {
                        final int size = verts.size();
                        for (int k = 0; k < size; k++) {
                            int l = (k + 1) % size;

                            Vector3d a = verts.get(k);
                            Vector3d b = verts.get(l);

                            if (a.minus(v).magnitude() > 0.01 && b.minus(v).magnitude() > 0.01) {
                                double dist = MathHelper.getNearestPointToLineSegmentCSG(a.x, a.y, a.z, b.x, b.y, b.z, v.x, v.y, v.z).minus(v).magnitude();
                                if (dist < min_dist) min_dist = dist;
                                if (dist < 0.1) {
                                    verts.add(l, v.clone());
                                }
                            }
                        }
                    }
                });
            }).get();
        } catch (InterruptedException e) {
            NLogger.error(getClass(), e);
        } catch (ExecutionException e) {
            NLogger.error(getClass(), e);
        }

        Matrix4f id = new Matrix4f();
        Matrix4f.setIdentity(id);
        GColour col = View.getLDConfigColour(16);
        GData1 g1 = new GData1(-1, col.getR(), col.getG(), col.getB(), 1f, id, View.ACCURATE_ID, new ArrayList<String>(), null, null, 1, false, id, View.ACCURATE_ID, null, View.DUMMY_REFERENCE, true, false,
                new HashSet<String>(), View.DUMMY_REFERENCE);
        this.result = toLDrawTriangles2(g1);
        return g1;
    }

    public TreeMap<GData3, Integer> toLDrawTriangles2(GData1 parent) {
        TreeMap<GData3, Integer> result = new TreeMap<GData3, Integer>();
        for (Polygon p : this.polygons) {
            result.putAll(p.toLDrawTriangles2(parent));
        }
        return result;
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

    public TreeMap<GData3, Integer> getResult() {
        return result;
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
        List<Polygon> newpolygons = new ArrayList<Polygon>();
        for (Polygon p : polygons) {
            newpolygons.add(p.transformed(transform));
        }
        CSG result = CSG.fromPolygons(newpolygons);
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
        List<Polygon> newpolygons = new ArrayList<Polygon>();
        for (Polygon p : polygons) {
            newpolygons.add(p.transformed(transform, c, ID));
        }
        CSG result = CSG.fromPolygons(newpolygons);
        return result;
    }

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
     * Returns a transformed coloured, copy of this CSG.
     *
     * @param transform
     *            the transform to apply
     * @param ID
     *
     * @return a transformed copy of this CSG
     */
    public CSG transformed(Matrix4f transform, GColour c, int ID) {
        return transformed(new Transform().apply(transform), c, ID);
    }

    /**
     * Returns the bounds of this csg.
     *
     * @return bouds of this csg
     */
    public Bounds getBounds() {
        Bounds result = bounds;
        if (result == null) {
            if (!polygons.isEmpty()) {
                result = new Bounds();
                for (Polygon t : polygons) {
                    Bounds b = t.getBounds();
                    result.union(b);
                }
            } else {
                result = new Bounds(new Vector3d(0, 0, 0), new Vector3d(0, 0, 0));
            }
            bounds = result;
        }
        return result;
    }
}
