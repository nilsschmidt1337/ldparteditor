/*
 * Copyright 2014 Gary W. Lucas., modified by Nils Schmidt (removed not required methods)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinfour.constrained.delaunay;

/**
 * Provides elements and methods to support geometric operations using "double
 * double" precision where necessary. The double-double precision calculations
 * use extended precision arithmetic to provide 108 bits of mantissa or about 30
 * decimal digits of precision.
 */
class GeometricOperations {

    /* Reusable extended precision data objects */
    private final DoubleDouble q11 = new DoubleDouble();
    private final DoubleDouble q21 = new DoubleDouble();
    private final DoubleDouble q31 = new DoubleDouble();
    private final DoubleDouble q12 = new DoubleDouble();
    private final DoubleDouble q22 = new DoubleDouble();
    private final DoubleDouble q32 = new DoubleDouble();
    private final DoubleDouble q11s = new DoubleDouble();
    private final DoubleDouble q12s = new DoubleDouble();
    private final DoubleDouble q21s = new DoubleDouble();
    private final DoubleDouble q22s = new DoubleDouble();
    private final DoubleDouble q31s = new DoubleDouble();
    private final DoubleDouble q32s = new DoubleDouble();
    private final DoubleDouble q21m32 = new DoubleDouble();
    private final DoubleDouble q31m22 = new DoubleDouble();
    private final DoubleDouble q31m12 = new DoubleDouble();
    private final DoubleDouble q11m32 = new DoubleDouble();
    private final DoubleDouble q11m22 = new DoubleDouble();
    private final DoubleDouble q21m12 = new DoubleDouble();

    /* Parameters related to magnitude of numeric values */
    private final Thresholds thresholds;
    private final double inCircleThreshold;

    /**
     * Construct an instance based on the specified threshold values.
     *
     * @param thresholds a valid instance
     */
    GeometricOperations(Thresholds thresholds) {
        this.thresholds = thresholds;
        this.inCircleThreshold = thresholds.getInCircleThreshold();
    }

    /**
     * Determines if vertex d lies within the circumcircle of triangle a,b,c, using
     * extended-precision arithmetic when required by small magnitude results.
     *
     * @param a a valid vertex
     * @param b a valid vertex
     * @param c a valid vertex
     * @param d a valid vertex
     * @return positive if d is inside the circumcircle; negative if it is outside;
     *         zero if it is on the edge.
     */
    double inCircle(Pnt a, Pnt b, Pnt c, Pnt d) {
        return inCircle(a.x, a.y, b.x, b.y, c.x, c.y, d.x, d.y);
    }

    /**
     * Determines if vertex d lies within the circumcircle of triangle a,b,c, using
     * extended-precision arithmetic when required by small magnitude results.
     *
     * @param ax the x coordinate of vertex a
     * @param ay the y coordinate of vertex a
     * @param bx the x coordinate of vertex b
     * @param by the y coordinate of vertex b
     * @param cx the x coordinate of vertex c
     * @param cy the y coordinate of vertex c
     * @param dx the x coordinate of vertex d
     * @param dy the y coordinate of vertex d
     * @return positive if d is inside the circumcircle; negative if it is outside;
     *         zero if it is on the edge.
     */
    private double inCircle(double ax, double ay, double bx, double by, double cx, double cy, double dx, double dy) {
        // Shewchuk presents versions of the determinant calculations
        // in which all the terms are expressed as differences.
        // So, for example term a11 becomes ax - dx, etc. This has the
        // advantage with map coordinates which may be quite large in
        // magnitude for a particular data set even though the range of
        // values is relatively small. For example, coordinates for a
        // Lidar sample in Northern Connecticut in UTM Zone 18N coordinates
        // might range from (640000,4000000) to (640500, 4000500).
        // by taking the differences, we get smaller magnitude values
        // this is significant when we start multiplying terms together and
        // taking differences... we want to reduce the loss of precision in
        // lower-order digits.
        // column 1
        double a11 = ax - dx;
        double a21 = bx - dx;
        double a31 = cx - dx;

        // column 2
        double a12 = ay - dy;
        double a22 = by - dy;
        double a32 = cy - dy;

        // column 3 (folded into code below)
        // double a13 = a11 * a11 + a12 * a12
        // double a23 = a21 * a21 + a22 * a22
        // double a33 = a31 * a31 + a32 * a32
        // the following is organized so that terms of like-magnitude are
        // grouped together when difference are taken. the column 3 terms
        // involve squared terms. We do not want to take a difference between
        // once of these and a non-squared term because we do not want to
        // lose precision in the low-order digits.
        double inCircle = (a11 * a11 + a12 * a12) * (a21 * a32 - a31 * a22)
                + (a21 * a21 + a22 * a22) * (a31 * a12 - a11 * a32) + (a31 * a31 + a32 * a32) * (a11 * a22 - a21 * a12);

        if (-inCircleThreshold < inCircle && inCircle < inCircleThreshold) {
            double inCircle2 = this.inCircleQuadPrecision(ax, ay, bx, by, cx, cy, dx, dy);
            inCircle = inCircle2;
        }

        return inCircle;
    }

    /**
     * Uses quad-precision methods to determines if vertex d lies within the
     * circumcircle of triangle a,b,c. Similar to inCircle() but requires more
     * processing and delivers higher accuracy.
     *
     * @param ax the x coordinate of vertex a
     * @param ay the y coordinate of vertex a
     * @param bx the x coordinate of vertex b
     * @param by the y coordinate of vertex b
     * @param cx the x coordinate of vertex c
     * @param cy the y coordinate of vertex c
     * @param dx the x coordinate of vertex d
     * @param dy the y coordinate of vertex d
     * @return positive if d is inside the circumcircle; negative if it is outside;
     *         zero if it is on the edge.
     */
    double inCircleQuadPrecision(double ax, double ay, double bx, double by, double cx, double cy, double dx,
            double dy) {

        // (a11 * a11 + a12 * a12) * (a21 * a32 - a31 * a22)
        // + (a21 * a21 + a22 * a22) * (a31 * a12 - a11 * a32)
        // + (a31 * a31 + a32 * a32) * (a11 * a22 - a21 * a12)

        q11.setValue(ax).selfSubtract(dx);
        q21.setValue(bx).selfSubtract(dx);
        q31.setValue(cx).selfSubtract(dx);

        q12.setValue(ay).selfSubtract(dy);
        q22.setValue(by).selfSubtract(dy);
        q32.setValue(cy).selfSubtract(dy);

        q11s.setValue(q11).selfMultiply(q11);
        q12s.setValue(q12).selfMultiply(q12);
        q21s.setValue(q21).selfMultiply(q21);
        q22s.setValue(q22).selfMultiply(q22);
        q31s.setValue(q31).selfMultiply(q31);
        q32s.setValue(q32).selfMultiply(q32);

        q11m22.setValue(q11).selfMultiply(q22);
        q11m32.setValue(q11).selfMultiply(q32);
        q21m12.setValue(q21).selfMultiply(q12);
        q21m32.setValue(q21).selfMultiply(q32);
        q31m22.setValue(q31).selfMultiply(q22);
        q31m12.setValue(q31).selfMultiply(q12);

        // the following lines are destructive of values computed above
        DoubleDouble s1 = q11s.selfAdd(q12s);
        DoubleDouble s2 = q21s.selfAdd(q22s);
        DoubleDouble s3 = q31s.selfAdd(q32s);

        DoubleDouble t1 = q21m32.selfSubtract(q31m22);
        DoubleDouble t2 = q31m12.selfSubtract(q11m32);
        DoubleDouble t3 = q11m22.selfSubtract(q21m12);

        s1.selfMultiply(t1);
        s2.selfMultiply(t2);
        s3.selfMultiply(t3);
        s1.selfAdd(s2).selfAdd(s3);

        return s1.doubleValue();
    }

    /**
     * Uses extended arithmetic to find the side on which a point lies with respect
     * to a directed edge.
     *
     * @param ax the x coordinate of the first vertex in the segment
     * @param ay the y coordinate of the first vertex in the segment
     * @param bx the x coordinate of the second vertex in the segment
     * @param by the y coordinate of the second vertex in the segment
     * @param cx the x coordinate of the point of interest
     * @param cy the y coordinate of the point of interest
     * @return positive if the point is to the left of the edge, negative if it is
     *         to the right, or zero if it lies on the ray coincident with the edge.
     */
    double halfPlane(double ax, double ay, double bx, double by, double cx, double cy) {
        q11.setValue(cx).selfSubtract(ax);
        q12.setValue(ay).selfSubtract(by);
        q21.setValue(cy).selfSubtract(ay);
        q22.setValue(bx).selfSubtract(ax);
        q11.selfMultiply(q12);
        q21.selfMultiply(q22);
        q11.selfAdd(q21);
        return q11.doubleValue();
    }

    /**
     * Uses extended arithmetic to find the direction of a point with coordinates
     * (cx, cy) compared to a directed edge from vertex A to B. This value is given
     * by the dot product (cx-ax, cy-ay) dot (bx-ax, by-ay).
     *
     * @param ax the x coordinate of the initial point on the edge
     * @param ay the y coordinate of the initial point on the edge
     * @param bx the x coordinate of the second point on the edge
     * @param by the y coordinate of the second point on the edge
     * @param cx the coordinate of interest
     * @param cy the coordinate of interest
     * @return a valid, signed floating point number, potentially zero.
     */
    double direction(double ax, double ay, double bx, double by, double cx, double cy) {
        q11.setValue(bx).selfSubtract(ax);
        q12.setValue(by).selfSubtract(ay);
        q21.setValue(cx).selfSubtract(ax);
        q22.setValue(cy).selfSubtract(ay);
        q11.selfMultiply(q21);
        q12.selfMultiply(q22);
        q11.selfAdd(q12);
        return q11.doubleValue();
    }

    /**
     * Determines the signed area of triangle ABC. If necessary, uses extended
     * arithmetic to compute the area of a nearly degenerate triangle.
     *
     * @param a the initial vertex
     * @param b the second vertex
     * @param c the third vertex
     * @return a positive value if the triangle is oriented counterclockwise,
     *         negative if it is oriented clockwise, or zero if it is degenerate.
     */
    double area(Pnt a, Pnt b, Pnt c) {
        // the computation used here and the one used in the
        // halfPlane() method are the same. However, to save operations
        // in the halfPlane() calculation, we swap a couple of variables
        // so as to avoid a subtraction. So the h calculation could be written
        // h = (c.x - a.x) * (a.y - b.y) + (c.y - a.y) * (b.x - a.x)
        double h = (c.y - a.y) * (b.x - a.x) - (c.x - a.x) * (b.y - a.y);
        if (-inCircleThreshold < h && h < inCircleThreshold) {
            h = halfPlane(a.x, a.y, b.x, b.y, c.x, c.y);
        }
        return h / 2.0;
    }

    /**
     * Gets the threshold values associated with this instance.
     *
     * @return a valid instance of Thresholds.
     */
    public Thresholds getThresholds() {
        return thresholds;
    }
}
