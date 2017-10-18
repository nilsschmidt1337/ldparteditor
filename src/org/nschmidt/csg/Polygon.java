/**
 * Polygon.java
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

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GColourIndex;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.GData3;
import org.nschmidt.ldparteditor.data.GDataCSG;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;

/**
 * Represents a convex polygon.
 *
 * Each convex polygon has a {@code shared} property, which is shared between
 * all polygons that are clones of each other or where split from the same
 * polygon. This can be used to define per-polygon properties (such as surface
 * color).
 */
public final class Polygon {


    // FIXME Remove this ID after the implementation is done
    private static int pseudo_id_counter = 0;
    private final int PSEUDO_ID;

    /**
     * Polygon vertices
     */
    public List<VectorCSGd> vertices;
    /**
     * The linked DatFile
     */
    final DatFile df;

    private GColourIndex colour = new GColourIndex(new GColour(-1, 0f, 0f, 0f, 1f), 0);

    /**
     * Plane defined by this polygon.
     *
     * <b>Note:</b> uses first three vertices to define the plane.
     */
    public final Plane plane;

    /**
     * Constructor. Creates a new polygon that consists of the specified
     * vertices.
     *
     * <b>Note:</b> the vertices used to initialize a polygon must be coplanar
     * and form a convex loop.
     * @param vertices
     *            polygon vertices
     */
    private Polygon(DatFile df, List<VectorCSGd> vertices) {
        // NOTE: A possible overflow is irrelevant since equals() will return distinct results!!
        PSEUDO_ID = pseudo_id_counter++;
        this.df = df;
        this.plane = Plane.createFromPoints(vertices.get(0), vertices.get(1), vertices.get(2));
        this.vertices = vertices;
    }

    @Override
    public int hashCode() {
        return PSEUDO_ID;
    }

    /**
     * EVERY Polygon object is unique!
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        return this == obj;
    }

    /**
     * Constructor. Creates a new polygon that consists of the specified
     * vertices.
     *
     * <b>Note:</b> the vertices used to initialize a polygon must be coplanar
     * and form a convex loop.
     * @param vertices
     *            polygon vertices
     */
    public Polygon(DatFile df, List<VectorCSGd> vertices, GColourIndex colour) {
        this(df, vertices);
        this.colour = colour;
    }

    /**
     * Constructor. Creates a new polygon that consists of the specified
     * vertices.
     *
     * <b>Note:</b> the vertices used to initialize a polygon must be coplanar
     * and form a convex loop.
     * @param vertices
     *            polygon vertices
     *
     */
    public Polygon(DatFile df, VectorCSGd... vertices) {
        this(df, new ArrayList<VectorCSGd>(Arrays.asList(vertices)));
    }

    public Polygon(DatFile df, List<VectorCSGd> vertices, Polygon o) {
        PSEUDO_ID = pseudo_id_counter++;
        this.df = df;
        this.plane = o.plane.clone();
        this.vertices = vertices;
        this.colour = o.colour;
    }

    @Override
    public Polygon clone() {
        List<VectorCSGd> newVertices = new ArrayList<VectorCSGd>(vertices.size());
        for (VectorCSGd vertex : vertices) {
            newVertices.add(vertex.clone());
        }
        return new Polygon(df, newVertices, new GColourIndex(colour.getColour(), colour.getIndex()));
    }

    /**
     * Flips this polygon.
     *
     * @return this polygon
     */
    public Polygon flip() {

        Collections.reverse(vertices);
        plane.flip();

        return this;
    }

    public HashMap<GData3, Integer> toLDrawTriangles(GData1 parent) {
        HashMap<GData3, Integer> result = new HashMap<GData3, Integer>();
        if (this.vertices.size() >= 3) {
            final GColour c16 = View.getLDConfigColour(16);
            VectorCSGd dv1 = this.vertices.get(0);
            for (int i = 0; i < this.vertices.size() - 2; i++) {
                VectorCSGd dv2 = this.vertices.get(i + 1);
                VectorCSGd dv3 = this.vertices.get(i + 2);
                Vertex v1 = new Vertex((float) dv1.x, (float) dv1.y, (float) dv1.z);
                Vertex v2 = new Vertex((float) dv2.x, (float) dv2.y, (float) dv2.z);
                Vertex v3 = new Vertex((float) dv3.x, (float) dv3.y, (float) dv3.z);
                GColourIndex colour = null;
                if ((colour = this.colour) == null) {
                    int dID = CSGPrimitive.id_counter.getAndIncrement();
                    result.put(new GData3(v1, v2, v3, parent, c16, true), dID);
                } else {
                    result.put(new GData3(v1, v2, v3, parent, View.getLDConfigColour(PSEUDO_ID % 16), true), colour.getIndex());
                    // FIXME result.put(new GData3(v1, v2, v3, parent, colour.getColour(), true), colour.getIndex());
                }
            }
        }
        return result;
    }

    public HashMap<GData3, Integer> toLDrawTriangles2(GData1 parent) {
        HashMap<GData3, Integer> result = new HashMap<GData3, Integer>();
        final int size = this.vertices.size();
        if (size >= 3) {
            final GColour c16 = View.getLDConfigColour(16);
            final BigDecimal identical_vertex_distance = new BigDecimal("0.1", MathContext.DECIMAL128); //$NON-NLS-1$

            boolean isCollinear = false;

            // Collinearity check
            for (int i = 0; i < this.vertices.size() - 2; i++) {
                Vector3d vertexA = new Vector3d(new BigDecimal(this.vertices.get(0).x), new BigDecimal(this.vertices.get(0).y),
                        new BigDecimal(this.vertices.get(0).z));
                Vector3d vertexB = new Vector3d(new BigDecimal(this.vertices.get(i + 1).x), new BigDecimal(this.vertices.get(i + 1).y),
                        new BigDecimal(this.vertices.get(i + 1).z));
                Vector3d vertexC = new Vector3d(new BigDecimal(this.vertices.get(i + 2).x), new BigDecimal(this.vertices.get(i + 2).y),
                        new BigDecimal(this.vertices.get(i + 2).z));

                Vector3d.sub(vertexA, vertexC, vertexA);
                Vector3d.sub(vertexB, vertexC, vertexB);
                boolean parseError = Vector3d.angle(vertexA, vertexB) < Threshold.collinear_angle_minimum;

                parseError = parseError || vertexA.length().compareTo(identical_vertex_distance) < 0;
                parseError = parseError || vertexB.length().compareTo(identical_vertex_distance) < 0;
                parseError = parseError || Vector3d.sub(vertexA, vertexB).length().compareTo(identical_vertex_distance) < 0;

                if (parseError) {
                    isCollinear = true;
                    break;
                }
            }


            if (isCollinear) {
                // Fix the collinearity by adding a center vertex
                double mx = 0.0;
                double my = 0.0;
                double mz = 0.0;
                for (int i = 0; i < size; i++) {
                    mx = mx + this.vertices.get(i).x;
                    my = my + this.vertices.get(i).y;
                    mz = mz + this.vertices.get(i).z;
                }

                mx = mx / size;
                my = my / size;
                mz = mz / size;

                Vertex v1 = new Vertex((float) mx, (float) my, (float) mz);
                for (int i = 0; i < size; i++) {
                    VectorCSGd dv2 = this.vertices.get(i);
                    VectorCSGd dv3 = this.vertices.get((i + 1) % size);
                    Vertex v2 = new Vertex((float) dv2.x, (float) dv2.y, (float) dv2.z);
                    Vertex v3 = new Vertex((float) dv3.x, (float) dv3.y, (float) dv3.z);

                    Vector3d vertexA = new Vector3d(new BigDecimal(mx), new BigDecimal(my), new BigDecimal(mz));
                    Vector3d vertexB = new Vector3d(new BigDecimal(this.vertices.get(i).x), new BigDecimal(this.vertices.get(i).y),
                            new BigDecimal(this.vertices.get(i).z));
                    Vector3d vertexC = new Vector3d(new BigDecimal(this.vertices.get((i + 1) % size).x), new BigDecimal(this.vertices.get((i + 1) % size).y),
                            new BigDecimal(this.vertices.get((i + 1) % size).z));

                    Vector3d.sub(vertexA, vertexC, vertexA);
                    Vector3d.sub(vertexB, vertexC, vertexB);

                    boolean parseError = vertexA.length().compareTo(identical_vertex_distance) < 0;
                    parseError = parseError || vertexB.length().compareTo(identical_vertex_distance) < 0;
                    parseError = parseError || Vector3d.sub(vertexA, vertexB).length().compareTo(identical_vertex_distance) < 0;

                    if (parseError) {
                        continue;
                    }

                    GColourIndex colour = null;
                    if ((colour = this.colour) == null) {
                        int dID = CSGPrimitive.id_counter.getAndIncrement();
                        result.put(new GData3(v1, v2, v3, parent, c16, true), dID);
                    } else {
                        // result.put(new GData3(v1, v2, v3, parent, View.getLDConfigColour(dID % 16), true), colour.getIndex());
                        result.put(new GData3(v1, v2, v3, parent, colour.getColour(), true), colour.getIndex());
                    }
                }

            } else {
                // No collinearity, save one triangle
                VectorCSGd dv1 = this.vertices.get(0);
                for (int i = 0; i < this.vertices.size() - 2; i++) {
                    VectorCSGd dv2 = this.vertices.get(i + 1);
                    VectorCSGd dv3 = this.vertices.get(i + 2);
                    Vertex v1 = new Vertex((float) dv1.x, (float) dv1.y, (float) dv1.z);
                    Vertex v2 = new Vertex((float) dv2.x, (float) dv2.y, (float) dv2.z);
                    Vertex v3 = new Vertex((float) dv3.x, (float) dv3.y, (float) dv3.z);

                    Vector3d vertexA = new Vector3d(new BigDecimal(dv1.x), new BigDecimal(dv1.y), new BigDecimal(dv1.z));
                    Vector3d vertexB = new Vector3d(new BigDecimal(dv2.x), new BigDecimal(dv2.y), new BigDecimal(dv2.z));
                    Vector3d vertexC = new Vector3d(new BigDecimal(dv3.x), new BigDecimal(dv3.y), new BigDecimal(dv3.z));

                    Vector3d.sub(vertexA, vertexC, vertexA);
                    Vector3d.sub(vertexB, vertexC, vertexB);

                    boolean parseError = vertexA.length().compareTo(identical_vertex_distance) < 0;
                    parseError = parseError || vertexB.length().compareTo(identical_vertex_distance) < 0;
                    parseError = parseError || Vector3d.sub(vertexA, vertexB).length().compareTo(identical_vertex_distance) < 0;

                    if (parseError) {
                        continue;
                    }

                    GColourIndex colour = null;
                    if ((colour = this.colour) == null) {
                        int dID = CSGPrimitive.id_counter.getAndIncrement();
                        result.put(new GData3(v1, v2, v3, parent, c16, true), dID);
                    } else {
                        // result.put(new GData3(v1, v2, v3, parent, View.getLDConfigColour(dID % 16), true), colour.getIndex());
                        result.put(new GData3(v1, v2, v3, parent, colour.getColour(), true), colour.getIndex());
                    }
                }
            }
        }
        return result;
    }

    /**
     * Translates this polygon.
     *
     * @param v
     *            the vector that defines the translation
     * @return this polygon
     */
    public Polygon translate(VectorCSGd v) {
        for (VectorCSGd vertex : vertices) {
            vertex = vertex.plus(v);
        }
        return this;
    }

    /**
     * Returns a translated copy of this polygon.
     *
     * <b>Note:</b> this polygon is not modified
     *
     * @param v
     *            the vector that defines the translation
     *
     * @return a translated copy of this polygon
     */
    public Polygon translated(VectorCSGd v) {
        return clone().translate(v);
    }

    /**
     * Applies the specified transformation to this polygon.
     *
     * <b>Note:</b> if the applied transformation performs a mirror operation
     * the vertex order of this polygon is reversed.
     *
     * @param transform
     *            the transformation to apply
     *
     * @return this polygon
     */
    public Polygon transform(Transform transform) {

        for (VectorCSGd v : vertices) {
            transform.transform(v);
        }

        if (transform.isMirror()) {
            // the transformation includes mirroring. flip polygon
            flip();
        }
        return this;
    }

    /**
     * Returns a transformed copy of this polygon.
     *
     * <b>Note:</b> if the applied transformation performs a mirror operation
     * the vertex order of this polygon is reversed.
     *
     * <b>Note:</b> this polygon is not modified
     *
     * @param transform
     *            the transformation to apply
     * @return a transformed copy of this polygon
     */
    public Polygon transformed(Transform transform) {
        return clone().transform(transform);
    }

    public Polygon transformed(Transform transform, GColour c, int ID) {
        Polygon result = clone().transform(transform);
        GColourIndex colour = null;
        if ((colour = this.getColour()) != null) {
            GColour c2;
            if ((c2 = colour.getColour()) != null) {
                if (c2.getColourNumber() == 16) {
                    result.setColour(new GColourIndex(c.clone(), ID));
                } else {
                    result.setColour(new GColourIndex(c2.clone(), ID));
                }
            }
        }
        return result;
    }

    /**
     * Creates a polygon from the specified point list.
     * @param points
     *            the points that define the polygon
     *
     * @return a polygon defined by the specified point list
     */
    public static Polygon fromPoints(GDataCSG csg, DatFile df, List<VectorCSGd> points, GColourIndex colour) {
        return fromPoints(csg, df, points, null, colour);
    }

    /**
     * Creates a polygon from the specified point list.
     * @param points
     *            the points that define the polygon
     *
     * @return a polygon defined by the specified point list
     */
    public static Polygon fromPoints(GDataCSG csg, DatFile df, List<VectorCSGd> points) {
        return fromPoints(csg, df, points, null, new GColourIndex(new GColour(-1, 0f, 0f, 0f, 1f), 0));
    }

    /**
     * Creates a polygon from the specified points.
     * @param points
     *            the points that define the polygon
     *
     * @return a polygon defined by the specified point list
     */
    public static Polygon fromPoints(GDataCSG csg, DatFile df, VectorCSGd... points) {
        return fromPoints(csg, df, new ArrayList<VectorCSGd>(Arrays.asList(points)), null, new GColourIndex(new GColour(-1, 0f, 0f, 0f, 1f), 0));
    }

    /**
     * Creates a polygon from the specified point list.
     * @param points
     *            the points that define the polygon
     * @param shared
     * @param plane
     *            may be null
     *
     * @return a polygon defined by the specified point list
     */
    private static Polygon fromPoints(GDataCSG csg, DatFile df, List<VectorCSGd> points, Plane plane, GColourIndex colour) {

        List<VectorCSGd> vertices = new ArrayList<VectorCSGd>();

        for (VectorCSGd p : points) {
            vertices.add(p.clone());
        }

        return new Polygon(df, vertices, colour);
    }

    /**
     * Returns the bounds of this polygon.
     *
     * @return bounds of this polygon
     */
    public Bounds getBounds() {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;

        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < vertices.size(); i++) {

            VectorCSGd vert = vertices.get(i);

            if (vert.x < minX) {
                minX = vert.x;
            }
            if (vert.y < minY) {
                minY = vert.y;
            }
            if (vert.z < minZ) {
                minZ = vert.z;
            }

            if (vert.x > maxX) {
                maxX = vert.x;
            }
            if (vert.y > maxY) {
                maxY = vert.y;
            }
            if (vert.z > maxZ) {
                maxZ = vert.z;
            }

        } // end for vertices

        return new Bounds(new VectorCSGd(minX, minY, minZ), new VectorCSGd(maxX, maxY, maxZ));
    }

    public GColourIndex getColour() {
        return colour;
    }

    public void setColour(GColourIndex colour) {
        this.colour = colour;
    }

    /**
     * Unifies this convex polygon with another polygon if
     * - they are on the same plane
     * - they share two vertices
     * - the resulting shape is convex
     *
     * @param other the other polygon
     * @return {@code null} if the two polygons can't be unified to a new convex shape
     */
    public Polygon unify(Polygon other) {

        // (optional) Check if they share the same colour

        /*
        if (!colour.getColour().equals(other.colour.getColour())) {
            return null;
        }
        */

        // Check if they are on the same plane

        /*
        final Plane op = other.plane;
        if (plane.normal.compareTo(op.normal) != 0 || Math.abs(plane.dist - op.dist) > 0.001) {
            return null;
        }
        */

        // Check if they share two vertices

        int vs = vertices.size();
        int ovs = other.vertices.size();

        int common = 0;

        final Set<VectorCSGd> ov = new TreeSet<>(other.vertices);
        final int[] this_indexes = new int[2];

        for (int i = 0; i < vs; i++) {
            if (ov.contains(vertices.get(i))) {
                if (common < 2) {
                    this_indexes[common] = i;
                    common++;
                } else {
                    return null;
                }
            }
        }

        if (common != 2) {
            return null;
        }
        common = 0;

        final Set<VectorCSGd> tv = new TreeSet<>(vertices);
        final int[] other_indexes = new int[2];

        for (int i = 0; i < ovs; i++) {
            if (tv.contains(other.vertices.get(i))) {
                other_indexes[common] = i;
                common++;
                if (common == 2) {
                    break;
                }
            }
        }

        rotate(this_indexes, vertices, vs);
        rotate(other_indexes, other.vertices, ovs);

        // Check if the resulting shape is "convex"
        // and the common vertices are superflous.

        final VectorCSGd dtv_1 = vertices.get(1).minus(vertices.get(0)).unit();
        final VectorCSGd dov_1 = other.vertices.get(ovs - 1).minus(other.vertices.get(ovs - 2)).unit();
        final VectorCSGd dtv_2 = vertices.get(vs - 1).minus(vertices.get(vs - 2)).unit();
        final VectorCSGd dov_2 = other.vertices.get(1).minus(other.vertices.get(0)).unit();

        final boolean linearDependent1 = Math.abs(dtv_1.dot(dov_1) - 1.0) <= 0.001;
        final boolean linearDependent2 = Math.abs(dtv_2.dot(dov_2) - 1.0) <= 0.001;
        final boolean linearMerge = linearDependent1 && linearDependent2;
        final boolean convexMerge = !linearDependent1 && !linearDependent2;
        // final VectorCSGd cornerv1 = linearMerge ? null : dtv_1.cross(dov_1);
        // final VectorCSGd cornerv2 = linearMerge ? null : dtv_2.cross(dov_2);

        // Create the new shape

        final List<VectorCSGd> newVertices = new ArrayList<>();

        if (linearMerge) {
            vs--;
            for (int i = 1; i < vs; i++) {
                newVertices.add(vertices.get(i));
            }

            ovs--;
            for (int i = 1; i < ovs; i++) {
                newVertices.add(other.vertices.get(i));
            }
            return new Polygon(df, newVertices, this);
        } else if (convexMerge) {
            // FIXME Needs implementation?!
            return null;
        } else if (linearDependent1) {
            for (int i = 1; i < vs; i++) {
                newVertices.add(vertices.get(i));
            }
            ovs--;
            for (int i = 1; i < ovs; i++) {
                newVertices.add(other.vertices.get(i));
            }
            return new Polygon(df, newVertices, this);
        } else if (linearDependent2) {
            vs--;
            for (int i = 0; i < vs; i++) {
                newVertices.add(vertices.get(i));
            }
            ovs--;
            for (int i = 1; i < ovs; i++) {
                newVertices.add(other.vertices.get(i));
            }
            return new Polygon(df, newVertices, this);
        } else {
            return null;
        }
    }

    public Polygon[] consumeCommonInterpolatedVertex(Polygon other) {

        // Check if they share one vertex

        int vs = vertices.size();
        int ovs = other.vertices.size();

        int common = 0;

        final Set<VectorCSGd> ov = new TreeSet<>(other.vertices);
        int this_index = -1;

        for (int i = 0; i < vs; i++) {
            if (ov.contains(vertices.get(i))) {
                if (common < 1) {
                    this_index = i;
                    common++;
                } else {
                    return null;
                }
            }
        }

        if (common != 1) {
            return null;
        }
        common = 0;

        final Set<VectorCSGd> tv = new TreeSet<>(vertices);
        int other_index = -1;

        for (int i = 0; i < ovs; i++) {
            if (tv.contains(other.vertices.get(i))) {
                other_index = i;
                break;
            }
        }

        rotate(this_index, vertices, vs);
        rotate(other_index, other.vertices, ovs);

        final VectorCSGd dtv_1 = vertices.get(1).minus(vertices.get(0)).unit();
        final VectorCSGd dov_1 = other.vertices.get(ovs - 1).minus(other.vertices.get(ovs - 2)).unit();
        final VectorCSGd dtv_2 = vertices.get(vs - 1).minus(vertices.get(vs - 2)).unit();
        final VectorCSGd dov_2 = other.vertices.get(1).minus(other.vertices.get(0)).unit();

        final boolean linearDependent1 = Math.abs(dtv_1.dot(dov_1) - 1.0) <= 0.001;
        final boolean linearDependent2 = Math.abs(dtv_2.dot(dov_2) - 1.0) <= 0.001;

        return null;
    }

    private void rotate(final int ci, final List<VectorCSGd> list, final int size) {
        final int rot_dist = ci;
        final int target = rot_dist + size;
        final List<VectorCSGd> copy = new ArrayList<>(list);
        list.clear();
        for (int i = rot_dist; i < target; i++) {
            list.add(copy.get(i % size));
        }
    }

    private void rotate(final int[] i_arr, final List<VectorCSGd> list, final int size) {
        if (Math.abs(i_arr[0] - i_arr[1]) != 1) return;
        final int rot_dist = i_arr[1]; // remember: i_arr[1] > i_arr[0]
        final int target = rot_dist + size;
        final List<VectorCSGd> copy = new ArrayList<>(list);
        list.clear();
        for (int i = rot_dist; i < target; i++) {
            list.add(copy.get(i % size));
        }
    }
}
