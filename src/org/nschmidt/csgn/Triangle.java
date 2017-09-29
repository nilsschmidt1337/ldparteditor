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
import java.util.HashMap;
import java.util.List;

import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GColourIndex;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.GData3;
import org.nschmidt.ldparteditor.enums.View;

public class Triangle {

    /**
     * Triangle vertices
     */
    public final Vector3d[] vertices = new Vector3d[3];
    private Bounds boundsCache = null;
    private boolean unbreakable = false;

    /**
     * The linked DatFile
     */
    final DatFile df;

    private GColourIndex colour = new GColourIndex(new GColour(-1, 0f, 0f, 0f, 1f), 0);

    /**
     * Plane defined by this triangle.
     */
    public final Plane plane;

    public Triangle(DatFile df, Vector3d v1, Vector3d v2, Vector3d v3, GColourIndex colour) {
        this.df = df;
        this.vertices[0] = v1;
        this.vertices[1] = v2;
        this.vertices[2] = v3;
        this.colour = colour;
        this.plane = Plane.createFromPoints(v1, v2, v3);
    }

    private Triangle(DatFile df, Vector3d v1, Vector3d v2, Vector3d v3, Plane plane, GColourIndex colour, boolean unbreakable) {
        this.df = df;
        this.vertices[0] = v1;
        this.vertices[1] = v2;
        this.vertices[2] = v3;
        this.plane = plane;
        this.colour = colour;
    }

    @Override
    public Triangle clone() {
        return new Triangle(df,
                this.vertices[0].clone(),
                this.vertices[1].clone(),
                this.vertices[2].clone(),
                this.plane.clone(),
                new GColourIndex(colour.getColour(), colour.getIndex()), this.unbreakable);
    }

    public Triangle transform(Transform transform) {

        for (Vector3d v : vertices) {
            transform.transform(v);
        }

        if (transform.isMirror()) {
            // the transformation includes mirroring. flip polygon
            flip();
        }

        plane.recreateFromPoints(vertices[0], vertices[1], vertices[2]);

        boundsCache = null;
        return this;
    }

    public Triangle transformed(Transform transform) {
        return clone().transform(transform);
    }

    public Triangle transformed(Transform transform, GColour c, int ID) {
        Triangle result = clone().transform(transform);
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

    public Triangle flip() {
        Vector3d tmp;
        tmp = vertices[0];
        vertices[0] = vertices[2];
        vertices[2] = tmp;
        plane.flip();
        return this;
    }

    public Object[] split(Plane p) {
        final List<Triangle> front = new ArrayList<>(4);
        final List<Triangle> back = new ArrayList<>(4);

        final Vector3d v1 = vertices[0];
        final Vector3d v2 = vertices[1];
        final Vector3d v3 = vertices[2];
        final Location p1 = p.getPointLocation(v1);
        final Location p2 = p.getPointLocation(v2);
        final Location p3 = p.getPointLocation(v3);

        double t1;
        double t2;

        Vector3d i1;
        Vector3d i2;

        if (p1 == Location.FRONT) {
            if (p2 == Location.FRONT) {
                if (p3 == Location.FRONT) { // FRONT FRONT FRONT
                    front.add(this);
                } else if (p3 == Location.BACK) { // FRONT FRONT BACK
                    t1 = p.getInterpolationFactorFromPoints(v2, v3);
                    i1 = v2.interpolate(v3, t1);
                    t2 = p.getInterpolationFactorFromPoints(v3, v1);
                    i2 = v3.interpolate(v1, t2);
                    front.add(new Triangle(df, v1.clone(), v2.clone(), i1, plane.clone(), colour, this.unbreakable));
                    front.add(new Triangle(df, i2.clone(), v1.clone(), i1.clone(), plane.clone(), colour, this.unbreakable));
                    back.add(new Triangle(df, i1.clone(), v3.clone(), i2, plane.clone(), colour, this.unbreakable));
                } else { // FRONT FRONT COPLANAR
                    front.add(this);
                }
            } else if (p2 == Location.BACK) {
                if (p3 == Location.FRONT) { // FRONT BACK FRONT
                    t1 = p.getInterpolationFactorFromPoints(v1, v2);
                    i1 = v1.interpolate(v2, t1);
                    t2 = p.getInterpolationFactorFromPoints(v2, v3);
                    i2 = v2.interpolate(v3, t2);
                    front.add(new Triangle(df, v1.clone(), i1, i2, plane.clone(), colour, this.unbreakable));
                    back.add(new Triangle(df, i1.clone(), v2.clone(), i2.clone(), plane.clone(), colour, this.unbreakable));
                    front.add(new Triangle(df, i2.clone(), v3.clone(), v1.clone(), plane.clone(), colour, this.unbreakable));
                } else if (p3 == Location.BACK) { // FRONT BACK BACK
                    t1 = p.getInterpolationFactorFromPoints(v1, v2);
                    i1 = v1.interpolate(v2, t1);
                    t2 = p.getInterpolationFactorFromPoints(v3, v1);
                    i2 = v3.interpolate(v1, t2);
                    front.add(new Triangle(df, v1.clone(), i1, i2, plane.clone(), colour, this.unbreakable));
                    back.add(new Triangle(df, i1.clone(), v2.clone(), v3.clone(), plane.clone(), colour, this.unbreakable));
                    back.add(new Triangle(df, v3.clone(), i2.clone(), i1.clone(), plane.clone(), colour, this.unbreakable));
                } else { // FRONT BACK COPLANAR
                    t1 = p.getInterpolationFactorFromPoints(v1, v2);
                    if (t1 < 0.001 || t1 > .999) {
                        front.add(this);
                    } else {
                        i1 = v1.interpolate(v2, t1);
                        front.add(new Triangle(df, v1.clone(), i1, v3.clone(), plane.clone(), colour, this.unbreakable));
                        back.add(new Triangle(df, i1.clone(), v2.clone(), v3.clone(), plane.clone(), colour, this.unbreakable));
                    }
                }
            } else {
                if (p3 == Location.FRONT) { // FRONT COPLANAR FRONT
                    front.add(this);
                } else if (p3 == Location.BACK) { // FRONT COPLANAR BACK
                    t1 = p.getInterpolationFactorFromPoints(v3, v1);
                    if (t1 < 0.001 || t1 > .999) {
                        front.add(this);
                    } else {
                        i1 = v3.interpolate(v1, t1);
                        front.add(new Triangle(df, v1.clone(), v2.clone(), i1, plane.clone(), colour, this.unbreakable));
                        back.add(new Triangle(df, i1.clone(), v2.clone(), v3.clone(), plane.clone(), colour, this.unbreakable));
                    }
                } else { // FRONT COPLANAR COPLANAR
                    front.add(this);
                }
            }
        } else if (p1 == Location.BACK) {
            if (p2 == Location.FRONT) {
                if (p3 == Location.FRONT) { // BACK FRONT FRONT
                    t1 = p.getInterpolationFactorFromPoints(v1, v2);
                    i1 = v1.interpolate(v2, t1);
                    t2 = p.getInterpolationFactorFromPoints(v3, v1);
                    i2 = v3.interpolate(v1, t2);
                    back.add(new Triangle(df, v1.clone(), i1, i2, plane.clone(), colour, this.unbreakable));
                    front.add(new Triangle(df, i1.clone(), v2.clone(), v3.clone(), plane.clone(), colour, this.unbreakable));
                    front.add(new Triangle(df, v3.clone(), i2.clone(), i1.clone(), plane.clone(), colour, this.unbreakable));
                } else if (p3 == Location.BACK) { // BACK FRONT BACK
                    t1 = p.getInterpolationFactorFromPoints(v1, v2);
                    i1 = v1.interpolate(v2, t1);
                    t2 = p.getInterpolationFactorFromPoints(v2, v3);
                    i2 = v2.interpolate(v3, t2);
                    back.add(new Triangle(df, v1.clone(), i1, i2, plane.clone(), colour, this.unbreakable));
                    front.add(new Triangle(df, i1.clone(), v2.clone(), i2.clone(), plane.clone(), colour, this.unbreakable));
                    back.add(new Triangle(df, i2.clone(), v3.clone(), v1.clone(), plane.clone(), colour, this.unbreakable));
                } else { // BACK FRONT COPLANAR
                    t1 = p.getInterpolationFactorFromPoints(v1, v2);
                    if (t1 < 0.001 || t1 > .999) {
                        front.add(this);
                    } else {
                        i1 = v1.interpolate(v2, t1);
                        back.add(new Triangle(df, v1.clone(), i1, v3.clone(), plane.clone(), colour, this.unbreakable));
                        front.add(new Triangle(df, i1.clone(), v2.clone(), v3.clone(), plane.clone(), colour, this.unbreakable));
                    }
                }
            } else if (p2 == Location.BACK) {
                if (p3 == Location.FRONT) { // BACK BACK FRONT
                    t1 = p.getInterpolationFactorFromPoints(v2, v3);
                    i1 = v2.interpolate(v3, t1);
                    t2 = p.getInterpolationFactorFromPoints(v3, v1);
                    i2 = v3.interpolate(v1, t2);
                    back.add(new Triangle(df, v1.clone(), v2.clone(), i1, plane.clone(), colour, this.unbreakable));
                    back.add(new Triangle(df, i2.clone(), v1.clone(), i1.clone(), plane.clone(), colour, this.unbreakable));
                    front.add(new Triangle(df, i1.clone(), v3.clone(), i2, plane.clone(), colour, this.unbreakable));
                } else if (p3 == Location.BACK) { // BACK BACK BACK
                    back.add(this);
                } else { // BACK BACK COPLANAR
                    back.add(this);
                }
            } else {
                if (p3 == Location.FRONT) { // BACK COPLANAR FRONT
                    t1 = p.getInterpolationFactorFromPoints(v3, v1);
                    if (t1 < 0.001 || t1 > .999) {
                        front.add(this);
                    } else {
                        i1 = v3.interpolate(v1, t1);
                        back.add(new Triangle(df, v1.clone(), v2.clone(), i1, plane.clone(), colour, this.unbreakable));
                        front.add(new Triangle(df, i1.clone(), v2.clone(), v3.clone(), plane.clone(), colour, this.unbreakable));
                    }
                } else if (p3 == Location.BACK) { // BACK COPLANAR BACK
                    back.add(this);
                } else { // BACK COPLANAR COPLANAR
                    back.add(this);
                }
            }
        } else {
            if (p2 == Location.FRONT) {
                if (p3 == Location.FRONT) { // COPLANAR FRONT FRONT
                    front.add(this);
                } else if (p3 == Location.BACK) { // COPLANAR FRONT BACK
                    t1 = p.getInterpolationFactorFromPoints(v2, v3);
                    if (t1 < 0.001 || t1 > .999) {
                        front.add(this);
                    } else {
                        i1 = v2.interpolate(v3, t1);
                        front.add(new Triangle(df, v1.clone(), v2.clone(), i1, plane.clone(), colour, this.unbreakable));
                        back.add(new Triangle(df, i1.clone(), v3.clone(), v1.clone(), plane.clone(), colour, this.unbreakable));
                    }
                } else { // COPLANAR FRONT COPLANAR
                    front.add(this);
                }
            } else if (p2 == Location.BACK) {
                if (p3 == Location.FRONT) { // COPLANAR BACK FRONT
                    t1 = p.getInterpolationFactorFromPoints(v2, v3);
                    if (t1 < 0.001 || t1 > .999) {
                        front.add(this);
                    } else {
                        i1 = v2.interpolate(v3, t1);
                        back.add(new Triangle(df, v1.clone(), v2.clone(), i1, plane.clone(), colour, this.unbreakable));
                        front.add(new Triangle(df, i1.clone(), v3.clone(), v1.clone(), plane.clone(), colour, this.unbreakable));
                    }
                } else if (p3 == Location.BACK) { // COPLANAR BACK BACK
                    back.add(this);
                } else { // COPLANAR BACK COPLANAR
                    back.add(this);
                }
            } else {
                if (p3 == Location.FRONT) { // COPLANAR COPLANAR FRONT
                    front.add(this);
                } else if (p3 == Location.BACK) { // COPLANAR COPLANAR BACK
                    back.add(this);
                } else { // COPLANAR COPLANAR COPLANAR
                    (p.normal.dot(plane.normal) > 0 ? front : back).add(this);
                }
            }
        }

        /*
        front.removeIf(t -> t.isMalformed());
        back.removeIf(t -> t.isMalformed());
         */

        return new Object[]{front, back};
    }

    private boolean isMalformed() {
        final boolean AisB = vertices[0].compareTo(vertices[1]) == 0;
        final boolean BisC = vertices[1].compareTo(vertices[2]) == 0;
        final boolean AisC = vertices[0].compareTo(vertices[2]) == 0;
        return AisB || BisC || AisC;
    }

    public HashMap<GData3, Integer> toLDrawTriangle(GData1 parent) {
        HashMap<GData3, Integer> result = new HashMap<GData3, Integer>();
        int dID = CSGPrimitive.id_counter.getAndIncrement();
        final GColour c16 = View.getLDConfigColour(16);
        final Vector3d tv1 = this.vertices[0];
        final Vector3d tv2 = this.vertices[1];
        final Vector3d tv3 = this.vertices[2];
        org.nschmidt.ldparteditor.data.Vertex v1 = new org.nschmidt.ldparteditor.data.Vertex((float) tv1.x, (float) tv1.y,
                (float) tv1.z);
        org.nschmidt.ldparteditor.data.Vertex v2 = new org.nschmidt.ldparteditor.data.Vertex((float) tv2.x, (float) tv2.y,
                (float) tv2.z);
        org.nschmidt.ldparteditor.data.Vertex v3 = new org.nschmidt.ldparteditor.data.Vertex((float) tv3.x, (float) tv3.y,
                (float) tv3.z);
        GColourIndex colour = null;
        if ((colour = this.colour) == null) {
            result.put(new GData3(v1, v2, v3, parent, c16, true), dID);
        } else {
            result.put(new GData3(v1, v2, v3, parent, colour.getColour(), true), colour.getIndex());
        }
        return result;
    }

    public GColourIndex getColour() {
        return colour;
    }

    public void setColour(GColourIndex colour) {
        this.colour = colour;
    }

    public boolean isUnbreakable() {
        return unbreakable;
    }

    public void setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
    }

    public boolean intersectsBoundingBox(Triangle other) {
        return getBounds().intersects(other.getBounds());
    }

    public Bounds getBounds() {
        Bounds result = boundsCache;
        if (result == null) {
            final Vector3d a = this.vertices[0];
            final Vector3d b = this.vertices[1];
            final Vector3d c = this.vertices[2];
            final double x_min = Math.min(Math.min(a.x, b.x), c.x);
            final double y_min = Math.min(Math.min(a.y, b.y), c.y);
            final double z_min = Math.min(Math.min(a.z, b.z), c.z);
            final double x_max = Math.max(Math.max(a.x, b.x), c.x);
            final double y_max = Math.max(Math.max(a.y, b.y), c.y);
            final double z_max = Math.max(Math.max(a.z, b.z), c.z);
            result = new Bounds(new Vector3d(x_min, y_min, z_min), new Vector3d(x_max, y_max, z_max));
            boundsCache = result;
        }
        return result;
    }

    @Override
    public String toString() {
        return  this.vertices[0].toString() + "_" + this.vertices[1].toString() + "_" +  this.vertices[2] + " colour: " + colour.getColour(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
