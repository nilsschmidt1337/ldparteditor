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

public class Plane {

    public static double EPSILON = 1e-3;

    /**
     * Normal vector.
     */
    public Vector3d normal;
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
    Plane(Vector3d normal, double dist) {
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
    public static Plane createFromPoints(Vector3d a, Vector3d b, Vector3d c) {
        Vector3d n = b.minus(a).cross(c.minus(a)).unit();
        return new Plane(n, n.dot(a));
    }

    public void recreateFromPoints(Vector3d a, Vector3d b, Vector3d c) {
        Vector3d n = b.minus(a).cross(c.minus(a)).unit();
        normal = n;
        dist = n.dot(a);
    }

    public Location getPointLocation(Vector3d point) {
        double t = normal.dot(point) - dist;
        return t < -Plane.EPSILON ? Location.BACK : t > Plane.EPSILON ? Location.FRONT : Location.COPLANAR;
    }

    public double getInterpolationFactorFromPoints(Vector3d start, Vector3d end) {
        return (this.dist - this.normal.dot(start)) / this.normal.dot(end.minus(start));
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
}
