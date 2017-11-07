/**
 * Plane.java
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

// # class Plane
import java.util.ArrayList;
import java.util.List;

import org.nschmidt.ldparteditor.data.DatFile;

/**
 * Represents a plane in 3D space.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class Plane implements Comparable<Plane> {

    /**
     * EPSILON is the tolerance used by
     * {@link #splitPolygon(org.nschmidt.csg.Polygon, java.util.List, java.util.List, java.util.List, java.util.List)
     * }
     * to decide if a point is on the plane.
     */
    public static double EPSILON = 1e-3;

    public static final int COPLANAR = 0;
    public static final int FRONT = 1;
    public static final int BACK = 2;
    public static final int SPANNING = 3;

    /**
     * Normal vector.
     */
    public VectorCSGd normal;
    /**
     * Distance to origin.
     */
    public double dist;

    /**
     * Constructor. Creates a new plane defined by its normal vector and the
     * distance to the origin.
     *
     * @param normal
     *            plane normal
     * @param dist
     *            distance from origin
     */
    public Plane(VectorCSGd normal, double dist) {
        this.normal = normal;
        this.dist = dist;
    }

    /**
     * Creates a nedist plane defined by the the specified points.
     *
     * @param a
     *            first point
     * @param b
     *            second point
     * @param c
     *            third point
     * @return a nedist plane
     */
    public static Plane createFromPoints(VectorCSGd a, VectorCSGd b, VectorCSGd c) {
        VectorCSGd n = b.minus(a).cross(c.minus(a)).unit();
        return new Plane(n, n.dot(a));
    }

    @Override
    public Plane clone() {
        return new Plane(normal.clone(), dist);
    }

    /**
     * Flips this plane.
     */
    public void flip() {
        normal = normal.negated();
        dist = -dist;
    }

    public int[] getTypes(final Polygon polygon) {
        final int size = polygon.vertices.size();
        final int[] types = new int[size + 1];
        int polygonType = 0;
        for (int i = 0; i < size; i++) {
            double t = this.normal.dot(polygon.vertices.get(i)) - this.dist;
            int type = t < -Plane.EPSILON ? BACK : t > Plane.EPSILON ? FRONT : COPLANAR;
            polygonType |= type;
            types[i] = type;
        }
        types[size] = polygonType;
        return types;
    }

    /**
     * Splits a {@link Polygon} by this plane if needed. After that it puts the
     * polygons or the polygon fragments in the appropriate lists ({@code front}
     * , {@code back}). Coplanar polygons go into either {@code coplanarFront},
     * {@code coplanarBack} depending on their orientation with respect to this
     * plane. Polygons in front or back of this plane go into either
     * {@code front} or {@code back}.
     *
     * @param polygon
     *            polygon to split
     * @param coplanarFront
     *            "coplanar front" polygons
     * @param coplanarBack
     *            "coplanar back" polygons
     * @param front
     *            front polygons
     * @param back
     *            back polgons
     */
    public void splitPolygonForClip(final Polygon polygon, final int[] types, List<Polygon> front, List<Polygon> back) {

        // Classify each point as well as the entire polygon into one of the
        // above
        // four classes.
        // Put the polygon in the correct list, splitting it when necessary.

        final int size = polygon.vertices.size();
        switch (types[types.length - 1]) {
        case COPLANAR:
            (this.normal.dot(polygon.plane.normal) > 0 ? front : back).add(polygon);
            return;
        case FRONT:
            front.add(polygon);
            return;
        case BACK:
            back.add(polygon);
            return;
        case SPANNING:

            final DatFile df = polygon.df;

            final List<VectorCSGd> f = new ArrayList<VectorCSGd>(size);
            final List<VectorCSGd> b = new ArrayList<VectorCSGd>(size);

            for (int i = 0; i < size; i++) {
                int j = (i + 1) % size;
                int ti = types[i];
                int tj = types[j];
                final VectorCSGd vi = polygon.vertices.get(i);
                final VectorCSGd vj = polygon.vertices.get(j);
                if (ti != BACK) {
                    f.add(vi);
                }
                if (ti != FRONT) {
                    b.add(ti != BACK ? vi.clone() : vi);
                }
                if ((ti | tj) == SPANNING) {

                    double t = (this.dist - this.normal.dot(vi)) / this.normal.dot(vj.minus(vi));

                    final VectorCSGd v = vi.interpolate(vj, t);

                    f.add(v);
                    b.add(v.clone());
                }
            }

            if (f.size() >= 3) {
                front.add(new Polygon(df, f, polygon));
            }
            if (b.size() >= 3) {
                back.add(new Polygon(df, b, polygon));
            }
            return;
        }
    }

    /**
     * Splits a {@link Polygon} by this plane if needed. After that it puts the
     * polygons or the polygon fragments in the appropriate lists ({@code front}
     * , {@code back}). Coplanar polygons go into either {@code coplanarFront},
     * {@code coplanarBack} depending on their orientation with respect to this
     * plane. Polygons in front or back of this plane go into either
     * {@code front} or {@code back}.
     *
     * @param polygon
     *            polygon to split
     * @param coplanarFront
     *            "coplanar front" polygons
     * @param coplanarBack
     *            "coplanar back" polygons
     * @param front
     *            front polygons
     * @param back
     *            back polgons
     */
    public void splitPolygonForBuild(final Polygon polygon, final int[] types, List<Polygon> coplanarPolys, List<Polygon> front, List<Polygon> back) {

        // Classify each point as well as the entire polygon into one of the
        // above
        // four classes.
        // Put the polygon in the correct list, splitting it when necessary.

        final int size = polygon.vertices.size();
        switch (types[types.length - 1]) {
        case COPLANAR:
            coplanarPolys.add(polygon);
            return;
        case FRONT:
            front.add(polygon);
            return;
        case BACK:
            back.add(polygon);
            return;
        case SPANNING:

            final DatFile df = polygon.df;

            final List<VectorCSGd> f = new ArrayList<VectorCSGd>(size);
            final List<VectorCSGd> b = new ArrayList<VectorCSGd>(size);

            for (int i = 0; i < size; i++) {
                int j = (i + 1) % size;
                int ti = types[i];
                int tj = types[j];
                final VectorCSGd vi = polygon.vertices.get(i);
                final VectorCSGd vj = polygon.vertices.get(j);
                if (ti != BACK) {
                    f.add(vi);
                }
                if (ti != FRONT) {
                    b.add(ti != BACK ? vi.clone() : vi);
                }
                if ((ti | tj) == SPANNING) {

                    double t = (this.dist - this.normal.dot(vi)) / this.normal.dot(vj.minus(vi));

                    final VectorCSGd v = vi.interpolate(vj, t);

                    f.add(v);
                    b.add(v.clone());
                }
            }

            if (f.size() >= 3) {
                front.add(new Polygon(df, f, polygon));
            }
            if (b.size() >= 3) {
                back.add(new Polygon(df, b, polygon));
            }
            return;
        }
    }

    @Override
    public int compareTo(Plane o) {
        final int vc = normal.compareTo(o.normal);
        if (vc == 0) {
            return Double.compare(dist, o.dist);
        }
        return vc;
    }
}
