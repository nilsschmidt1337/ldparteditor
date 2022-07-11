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
import java.util.Map;

import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GColourIndex;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.GData3;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.enumtype.LDConfig;

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
    private static int pseudoIdCounter = 0;
    private final int pseudoId;

    /**
     * Polygon vertices
     */
    List<VectorCSGd> vertices;
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
    final Plane plane;

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
        pseudoId = pseudoIdCounter++;
        this.df = df;
        this.plane = Plane.createFromPoints(vertices.get(0), vertices.get(1), vertices.get(2));
        this.vertices = vertices;
    }

    @Override
    public int hashCode() {
        return pseudoId;
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
    Polygon(DatFile df, List<VectorCSGd> vertices, GColourIndex colour) {
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
    Polygon(DatFile df, VectorCSGd... vertices) {
        this(df, new ArrayList<>(Arrays.asList(vertices)));
    }

    Polygon(DatFile df, List<VectorCSGd> vertices, Polygon o) {
        pseudoId = pseudoIdCounter++;
        this.df = df;
        this.plane = o.plane.createClone();
        this.vertices = vertices;
        this.colour = o.colour;
    }

    Polygon createClone() {
        List<VectorCSGd> newVertices = new ArrayList<>(vertices.size());
        for (VectorCSGd vertex : vertices) {
            newVertices.add(vertex.createClone());
        }
        return new Polygon(df, newVertices, new GColourIndex(colour.colour(), colour.index()));
    }

    /**
     * Flips this polygon.
     *
     * @return this polygon
     */
    Polygon flip() {

        Collections.reverse(vertices);
        plane.flip();

        return this;
    }

    public Map<GData3, IdAndPlane> toLDrawTriangles(GData1 parent) {
        Map<GData3, IdAndPlane> result = new HashMap<>();
        if (this.vertices.size() >= 3) {
            final GColour c16 = LDConfig.getColour16();
            VectorCSGd dv1 = this.vertices.get(0);
            for (int i = 0; i < this.vertices.size() - 2; i++) {
                VectorCSGd dv2 = this.vertices.get(i + 1);
                VectorCSGd dv3 = this.vertices.get(i + 2);
                Vertex v1 = new Vertex((float) dv1.x, (float) dv1.y, (float) dv1.z);
                Vertex v2 = new Vertex((float) dv2.x, (float) dv2.y, (float) dv2.z);
                Vertex v3 = new Vertex((float) dv3.x, (float) dv3.y, (float) dv3.z);
                GColourIndex tmpColour = null;
                if ((tmpColour = this.colour) == null) {
                    int dID = Primitive.idCounter.getAndIncrement();
                    result.put(new GData3(v1, v2, v3, parent, c16, true), new IdAndPlane(plane, dID));
                } else {
                    // Debug-only: result.put(new GData3(v1, v2, v3, parent, View.getLDConfigColour(pseudoId % 16), true), new IdAndPlane(plane, colour.getIndex())); // only for test
                    result.put(new GData3(v1, v2, v3, parent, tmpColour.colour(), true), new IdAndPlane(plane, tmpColour.index()));
                }
            }
        }
        return result;
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
    Polygon transform(Transform transform) {

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
    Polygon transformed(Transform transform) {
        return createClone().transform(transform);
    }

    Polygon transformed(Transform transform, GColour c, int id) {
        Polygon result = createClone().transform(transform);
        GColourIndex tmpColour = null;
        if ((tmpColour = this.getColour()) != null) {
            GColour c2;
            if ((c2 = tmpColour.colour()) != null) {
                if (c2.getColourNumber() == 16) {
                    result.setColour(new GColourIndex(c.createClone(), id));
                } else {
                    result.setColour(new GColourIndex(c2.createClone(), id));
                }
            }
        }
        return result;
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
}
