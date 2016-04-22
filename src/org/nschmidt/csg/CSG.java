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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.lwjgl.util.vector.Matrix4f;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.GData3;
import org.nschmidt.ldparteditor.enums.View;

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

    HashMap<GData3, Integer> result = new HashMap<GData3, Integer>();

    private List<Polygon> polygons;

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
    public CSG union(CSG csg) {

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
    }

    private CSG _unionNoOpt(CSG csg) {
        Node a = new Node(this.clone().polygons);
        Node b = new Node(csg.clone().polygons);
        a.clipTo(b);
        b.clipTo(a);
        b.invert();
        b.clipTo(a);
        b.invert();
        a.build(b.allPolygons());
        return CSG.fromPolygons(a.allPolygons());
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
        a.build(b.allPolygons());
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
        a.build(b.allPolygons());
        a.invert();
        return CSG.fromPolygons(a.allPolygons());
    }

    /**
     * Returns this csg in STL string format.
     *
     * @return this csg in STL string format
     */
    public String toStlString() {
        StringBuilder sb = new StringBuilder();
        toStlString(sb);
        return sb.toString();
    }

    /**
     * Returns this csg in STL string format.
     *
     * @param sb
     *            string builder
     *
     * @return the specified string builder
     */
    public StringBuilder toStlString(StringBuilder sb) {
        sb.append("solid v3d.csg\n"); //$NON-NLS-1$
        for (Polygon p : this.polygons) {
            p.toStlString(sb);
        }
        sb.append("endsolid v3d.csg\n"); //$NON-NLS-1$
        return sb;
    }

    /**
     * Returns this csg as list of LDraw triangles
     *
     * @return this csg as list of LDraw triangles
     */
    public HashMap<GData3, Integer> toLDrawTriangles(GData1 parent) {
        HashMap<GData3, Integer> result = new HashMap<GData3, Integer>();
        for (Polygon p : this.polygons) {
            result.putAll(p.toLDrawTriangles(parent));
        }
        return result;
    }

    public GData1 compile() {
        Matrix4f id = new Matrix4f();
        Matrix4f.setIdentity(id);
        GData1 g1 = new GData1(-1, .5f, .5f, .5f, 1f, id, View.ACCURATE_ID, new ArrayList<String>(), null, null, 1, false, id, View.ACCURATE_ID, null, View.DUMMY_REFERENCE, true, false,
                new HashSet<String>(), View.DUMMY_REFERENCE);
        this.result = toLDrawTriangles(g1);
        return g1;
    }

    public void draw(Composite3D c3d) {
        for (GData3 tri : result.keySet()) {
            tri.draw(c3d);
        }
    }

    public void draw_textured(Composite3D c3d) {
        for (GData3 tri : result.keySet()) {
            tri.drawBFC_Textured(c3d);
        }
    }

    public HashMap<GData3, Integer> getResult() {
        return result;
    }

    /**
     * Returns this csg in OBJ string format.
     *
     * @param sb
     *            string builder
     * @return the specified string builder
     */
    public StringBuilder toObjString(StringBuilder sb) {
        sb.append("# Group").append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
        sb.append("g v3d.csg\n"); //$NON-NLS-1$

        List<Vertex> vertices = new ArrayList<Vertex>();
        List<List<Integer>> indices = new ArrayList<List<Integer>>();

        sb.append("\n# Vertices\n"); //$NON-NLS-1$

        for (Polygon p : polygons) {
            List<Integer> polyIndices = new ArrayList<Integer>();
            for (Vertex v : p.vertices) {

                if (!vertices.contains(v)) {
                    vertices.add(v);
                    v.toObjString(sb);
                    polyIndices.add(vertices.size());
                } else {
                    polyIndices.add(vertices.indexOf(v) + 1);
                }
            }

            indices.add(polyIndices);
        }

        sb.append("\n# Faces").append("\n"); //$NON-NLS-1$ //$NON-NLS-2$

        for (List<Integer> pVerts : indices) {

            // we triangulate the polygon to ensure
            // compatibility with 3d printer software
            int index1 = pVerts.get(0);
            for (int i = 0; i < pVerts.size() - 2; i++) {
                int index2 = pVerts.get(i + 1);
                int index3 = pVerts.get(i + 2);

                sb.append("f "). //$NON-NLS-1$
                append(index1).append(" "). //$NON-NLS-1$
                append(index2).append(" "). //$NON-NLS-1$
                append(index3).append("\n"); //$NON-NLS-1$
            }
        }

        sb.append("\n# End Group v3d.csg").append("\n"); //$NON-NLS-1$ //$NON-NLS-2$

        return sb;
    }

    /**
     * Returns this csg in OBJ string format.
     *
     * @return this csg in OBJ string format
     */
    public String toObjString() {
        StringBuilder sb = new StringBuilder();
        return toObjString(sb).toString();
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
     *
     * @return a transformed copy of this CSG
     */
    public CSG transformed(Matrix4f transform) {
        return transformed(new Transform().apply(transform));
    }

    /**
     * Returns the bounds of this csg.
     *
     * @return bouds of this csg
     */
    public Bounds getBounds() {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;

        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;

        for (Polygon p : getPolygons()) {

            for (int i = 0; i < p.vertices.size(); i++) {

                Vertex vert = p.vertices.get(i);

                if (vert.pos.x < minX) {
                    minX = vert.pos.x;
                }
                if (vert.pos.y < minY) {
                    minY = vert.pos.y;
                }
                if (vert.pos.z < minZ) {
                    minZ = vert.pos.z;
                }

                if (vert.pos.x > maxX) {
                    maxX = vert.pos.x;
                }
                if (vert.pos.y > maxY) {
                    maxY = vert.pos.y;
                }
                if (vert.pos.z > maxZ) {
                    maxZ = vert.pos.z;
                }

            } // end for vertices

        } // end for polygon

        return new Bounds(new Vector3d(minX, minY, minZ), new Vector3d(maxX, maxY, maxZ));
    }
}
