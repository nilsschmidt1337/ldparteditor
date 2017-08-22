package org.nschmidt.csgn;

public class Plane {

    /**
     * EPSILON is the tolerance used by
     * {@link #splitPolygon(org.nschmidt.csg.Polygon, java.util.List, java.util.List, java.util.List, java.util.List)
     * }
     * to decide if a point is on the plane.
     */
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
