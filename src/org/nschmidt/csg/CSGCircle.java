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
package org.nschmidt.csg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GColourIndex;

/**
 * A solid circle.
 *
 * The tessellation can be controlled via the {@link #numSlices} parameter.
 *
 * @author nils
 */
public class CSGCircle implements Primitive {

    public final int id = idCounter.getAndIncrement();
    private VectorCSGd start;
    private VectorCSGd end;
    private double radius;
    private int numSlices;

    /**
     * Constructor. Creates a new circle with center {@code [0,0,0]} and ranging
     * from {@code [0,-0.5,0]} to {@code [0,0.5,0]}, i.e. {@code size = 1}.
     */
    public CSGCircle() {
        this.start = new VectorCSGd(0, 0d, 0);
        this.end = new VectorCSGd(0, 1, 0);
        this.radius = 1000d;
        this.numSlices = 16;
    }

    /**
     * Constructor. Creates a circle . The resolution of the tessellation can be
     * controlled with {@code numSlices}.
     *
     * @param numSlices
     *            number of slices (used for tessellation)
     */
    public CSGCircle(int numSlices) {
        this.start = new VectorCSGd(0, 0d, 0);
        this.end = new VectorCSGd(0, 1, 0);
        this.radius = 1000d;
        this.numSlices = numSlices;
    }

    @Override
    public List<Polygon> toPolygons(DatFile df, GColour colour) {
        final VectorCSGd s = getStart();
        VectorCSGd e = getEnd();
        final VectorCSGd ray = e.minus(s);
        final VectorCSGd axisZ = ray.unit();
        boolean isY = Math.abs(axisZ.y) > 0.5;
        final VectorCSGd axisX = new VectorCSGd(isY ? 1 : 0, !isY ? 1 : 0, 0).cross(axisZ).unit();
        final VectorCSGd axisY = axisX.cross(axisZ).unit();
        VectorCSGd startV = s.createClone();
        VectorCSGd endV = e.createClone();
        List<Polygon> polygons = new ArrayList<>();

        for (int i = 0; i < numSlices; i++) {
            double t0 = i / (double) numSlices;
            double t1 = (i + 1) / (double) numSlices;

            polygons.add(new Polygon(df, Arrays.asList(startV, cylPoint(axisX, axisY, ray, s, radius, 0, t0), cylPoint(axisX, axisY, ray, s, radius, 0, t1)), new GColourIndex(colour, id)));

            polygons.add(new Polygon(df, Arrays.asList(cylPoint(axisX, axisY, ray, s, radius, 0, t1), cylPoint(axisX, axisY, ray, s, radius, 0, t0),
                                            cylPoint(axisX, axisY, ray, s, radius, 1, t0), cylPoint(axisX, axisY, ray, s, radius, 1, t1)), new GColourIndex(colour, id)));

            polygons.add(new Polygon(df, Arrays.asList(endV, cylPoint(axisX, axisY, ray, s, radius, 1, t1), cylPoint(axisX, axisY, ray, s, radius, 1, t0)), new GColourIndex(colour, id)));
        }

        return polygons;
    }

    private VectorCSGd cylPoint(VectorCSGd axisX, VectorCSGd axisY, VectorCSGd ray, VectorCSGd s, double r, double stack, double slice) {
        double angle = slice * Math.PI * 2;
        VectorCSGd out = axisX.times(Math.cos(angle)).plus(axisY.times(Math.sin(angle)));
        return s.plus(ray.times(stack)).plus(out.times(r));
    }

    /**
     * @return the start
     */
    public VectorCSGd getStart() {
        return start;
    }

    /**
     * @param start
     *            the start to set
     */
    public void setStart(VectorCSGd start) {
        this.start = start;
    }

    /**
     * @return the end
     */
    public VectorCSGd getEnd() {
        return end;
    }

    /**
     * @param end
     *            the end to set
     */
    public void setEnd(VectorCSGd end) {
        this.end = end;
    }

    /**
     * @return the radius
     */
    public double getRadius() {
        return radius;
    }

    /**
     * @param radius
     *            the radius to set
     */
    public void setRadius(double radius) {
        this.radius = radius;
    }

    /**
     * @return the number of slices
     */
    public int getNumSlices() {
        return numSlices;
    }

    /**
     * @param numSlices
     *            the number of slices to set
     */
    public void setNumSlices(int numSlices) {
        this.numSlices = numSlices;
    }

    @Override
    public CSG toCSG(DatFile df, GColour colour) {
        return CSG.fromPolygons(toPolygons(df, colour));
    }

}
