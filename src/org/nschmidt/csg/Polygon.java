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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GColourIndex;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.GData3;
import org.nschmidt.ldparteditor.enums.View;

/**
 * Represents a convex polygon.
 *
 * Each convex polygon has a {@code shared} property, which is shared between
 * all polygons that are clones of each other or where split from the same
 * polygon. This can be used to define per-polygon properties (such as surface
 * color).
 */
public final class Polygon {

    /**
     * Polygon vertices
     */
    public final List<Vertex> vertices;
    /**
     * Shared property (can be used for shared color etc.).
     */
    private final PropertyStorage shared;

    public PropertyStorage getShared() {
        return shared;
    }

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
     *
     * @param vertices
     *            polygon vertices
     * @param shared
     *            shared property
     */
    public Polygon(List<Vertex> vertices, PropertyStorage shared) {
        this.vertices = vertices;
        this.shared = shared;
        this.plane = Plane.createFromPoints(vertices.get(0).pos, vertices.get(1).pos, vertices.get(2).pos);
    }

    /**
     * Constructor. Creates a new polygon that consists of the specified
     * vertices.
     *
     * <b>Note:</b> the vertices used to initialize a polygon must be coplanar
     * and form a convex loop.
     *
     * @param vertices
     *            polygon vertices
     */
    public Polygon(List<Vertex> vertices) {
        this.vertices = vertices;
        this.shared = new PropertyStorage();
        this.plane = Plane.createFromPoints(vertices.get(0).pos, vertices.get(1).pos, vertices.get(2).pos);
    }

    /**
     * Constructor. Creates a new polygon that consists of the specified
     * vertices.
     *
     * <b>Note:</b> the vertices used to initialize a polygon must be coplanar
     * and form a convex loop.
     *
     * @param vertices
     *            polygon vertices
     *
     */
    public Polygon(Vertex... vertices) {
        this(Arrays.asList(vertices));
    }

    @Override
    public Polygon clone() {
        List<Vertex> newVertices = new ArrayList<Vertex>();
        for (Vertex vertex : vertices) {
            newVertices.add(vertex.clone());
        }
        ;
        return new Polygon(newVertices, new PropertyStorage(shared));
    }

    /**
     * Flips this polygon.
     *
     * @return this polygon
     */
    public Polygon flip() {
        for (Vertex vertex : vertices) {
            vertex.flip();
        }
        ;
        Collections.reverse(vertices);

        plane.flip();

        return this;
    }

    /**
     * Returns a flipped copy of this polygon.
     *
     * <b>Note:</b> this polygon is not modified.
     *
     * @return a flipped copy of this polygon
     */
    public Polygon flipped() {
        return clone().flip();
    }

    /**
     * Returns this polygon in STL string format.
     *
     * @return this polygon in STL string format
     */
    public String toStlString() {
        return toStlString(new StringBuilder()).toString();
    }

    /**
     * Returns this polygon in STL string format.
     *
     * @param sb
     *            string builder
     *
     * @return the specified string builder
     */
    public StringBuilder toStlString(StringBuilder sb) {

        if (this.vertices.size() >= 3) {

            // TODO: improve the triangulation?
            //
            // STL requires triangular polygons.
            // If our polygon has more vertices, create
            // multiple triangles:
            String firstVertexStl = this.vertices.get(0).toStlString();
            for (int i = 0; i < this.vertices.size() - 2; i++) {
                sb.append("  facet normal ").append( //$NON-NLS-1$
                        this.plane.normal.toStlString()).append("\n"). //$NON-NLS-1$
                        append("    outer loop\n"). //$NON-NLS-1$
                        append("      ").append(firstVertexStl).append("\n"). //$NON-NLS-1$ //$NON-NLS-2$
                        append("      "); //$NON-NLS-1$
                this.vertices.get(i + 1).toStlString(sb).append("\n"). //$NON-NLS-1$
                append("      "); //$NON-NLS-1$
                this.vertices.get(i + 2).toStlString(sb).append("\n"). //$NON-NLS-1$
                append("    endloop\n"). //$NON-NLS-1$
                append("  endfacet\n"); //$NON-NLS-1$
            }
        }

        return sb;
    }

    public HashMap<GData3, Integer> toLDrawTriangles(GData1 parent) {
        HashMap<GData3, Integer> result = new HashMap<GData3, Integer>();
        if (this.vertices.size() >= 3) {
            int dID = CSGPrimitive.id_counter.getAndIncrement();
            final GColour c16 = View.getLDConfigColour(16);
            for (int i = 0; i < this.vertices.size() - 2; i++) {
                org.nschmidt.ldparteditor.data.Vertex v1 = new org.nschmidt.ldparteditor.data.Vertex((float) this.vertices.get(0).pos.x, (float) this.vertices.get(0).pos.y,
                        (float) this.vertices.get(0).pos.z);
                org.nschmidt.ldparteditor.data.Vertex v2 = new org.nschmidt.ldparteditor.data.Vertex((float) this.vertices.get(i + 1).pos.x, (float) this.vertices.get(i + 1).pos.y,
                        (float) this.vertices.get(i + 1).pos.z);
                org.nschmidt.ldparteditor.data.Vertex v3 = new org.nschmidt.ldparteditor.data.Vertex((float) this.vertices.get(i + 2).pos.x, (float) this.vertices.get(i + 2).pos.y,
                        (float) this.vertices.get(i + 2).pos.z);
                GColourIndex colour = null;
                try {
                    colour = (GColourIndex) this.shared.getFirstValue();
                } catch (NoSuchElementException nse) {
                    colour = null;
                }
                if (colour == null) {
                    result.put(new GData3(v1, v2, v3, parent, c16), dID);
                } else {
                    result.put(new GData3(v1, v2, v3, parent, View.getLDConfigColour(colour.getIndex() % 16)), colour.getIndex());
                    // result.put(new GData3(v1, v2, v3, parent, colour.getColour()), colour.getIndex());
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
    public Polygon translate(Vector3d v) {
        for (Vertex vertex : vertices) {
            vertex.pos = vertex.pos.plus(v);
        }
        ;
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
    public Polygon translated(Vector3d v) {
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

        for (Vertex v : vertices) {
            v.transform(transform);
        }
        ;

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

    /**
     * Creates a polygon from the specified point list.
     *
     * @param points
     *            the points that define the polygon
     * @param shared
     *            shared property storage
     * @return a polygon defined by the specified point list
     */
    public static Polygon fromPoints(List<Vector3d> points, PropertyStorage shared) {
        return fromPoints(points, shared, null);
    }

    /**
     * Creates a polygon from the specified point list.
     *
     * @param points
     *            the points that define the polygon
     * @return a polygon defined by the specified point list
     */
    public static Polygon fromPoints(List<Vector3d> points) {
        return fromPoints(points, new PropertyStorage(), null);
    }

    /**
     * Creates a polygon from the specified points.
     *
     * @param points
     *            the points that define the polygon
     * @return a polygon defined by the specified point list
     */
    public static Polygon fromPoints(Vector3d... points) {
        return fromPoints(Arrays.asList(points), new PropertyStorage(), null);
    }

    /**
     * Creates a polygon from the specified point list.
     *
     * @param points
     *            the points that define the polygon
     * @param shared
     * @param plane
     *            may be null
     * @return a polygon defined by the specified point list
     */
    private static Polygon fromPoints(List<Vector3d> points, PropertyStorage shared, Plane plane) {

        Vector3d normal = plane != null ? plane.normal.clone() : new Vector3d(0, 0, 0);

        List<Vertex> vertices = new ArrayList<Vertex>();

        for (Vector3d p : points) {
            Vector3d vec = p.clone();
            Vertex vertex = new Vertex(vec, normal);
            vertices.add(vertex);
        }

        return new Polygon(vertices, shared);
    }

    /**
     * Returns the bounds of this polygon.
     *
     * @return bouds of this polygon
     */
    public Bounds getBounds() {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;

        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < vertices.size(); i++) {

            Vertex vert = vertices.get(i);

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

        return new Bounds(new Vector3d(minX, minY, minZ), new Vector3d(maxX, maxY, maxZ));
    }
}
