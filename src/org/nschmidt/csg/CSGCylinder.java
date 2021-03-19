/**
 * CSGCylinder.java
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
import java.util.List;

import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GColourIndex;

/**
 * A solid cylinder.
 *
 * The tessellation can be controlled via the {@link #numSlices} parameter.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class CSGCylinder extends CSGPrimitive implements Primitive {

    public final int ID = id_counter.getAndIncrement();

    private VectorCSGd start;
    private VectorCSGd end;
    private double radius;
    private int numSlices;

    /**
     * Constructor. Creates a new cylinder with center {@code [0,0,0]} and
     * ranging from {@code [0,-0.5,0]} to {@code [0,0.5,0]}, i.e.
     * {@code size = 1}.
     */
    public CSGCylinder() {
        this.start = new VectorCSGd(0, 0d, 0);
        this.end = new VectorCSGd(0, 1000d, 0);
        this.radius = 1000d;
        this.numSlices = 16;
    }

    /**
     * Constructor. Creates a cylinder . The resolution of the tessellation can
     * be controlled with {@code numSlices}.
     *
     * @param numSlices
     *            number of slices (used for tessellation)
     */
    public CSGCylinder(int numSlices) {
        this.start = new VectorCSGd(0, 0d, 0);
        this.end = new VectorCSGd(0, 1000d, 0);
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
        VectorCSGd startV = s.clone();
        VectorCSGd endV = e.clone();
        List<Polygon> polygons = new ArrayList<>();

        for (int i = 0; i < numSlices; i++) {
            double t0 = i / (double) numSlices, t1 = (i + 1) / (double) numSlices;
            {
                polygons.add(new Polygon(df, Arrays.asList(startV, cylPoint(axisX, axisY, axisZ, ray, s, radius, 0, t0, -1), cylPoint(axisX, axisY, axisZ, ray, s, radius, 0, t1, -1)), new GColourIndex(colour, ID)));
            }
            {
                polygons.add(new Polygon(df, Arrays.asList(cylPoint(axisX, axisY, axisZ, ray, s, radius, 0, t1, 0), cylPoint(axisX, axisY, axisZ, ray, s, radius, 0, t0, 0),
                                                cylPoint(axisX, axisY, axisZ, ray, s, radius, 1, t0, 0), cylPoint(axisX, axisY, axisZ, ray, s, radius, 1, t1, 0)), new GColourIndex(colour, ID)));
            }
            {
                polygons.add(new Polygon(df, Arrays.asList(endV, cylPoint(axisX, axisY, axisZ, ray, s, radius, 1, t1, 1),
                                                cylPoint(axisX, axisY, axisZ, ray, s, radius, 1, t0, 1)), new GColourIndex(colour, ID)));
            }
        }

        return polygons;
    }

    private VectorCSGd cylPoint(VectorCSGd axisX, VectorCSGd axisY, VectorCSGd axisZ, VectorCSGd ray, VectorCSGd s, double r, double stack, double slice, double normalBlend) {
        double angle = slice * Math.PI * 2;
        VectorCSGd out = axisX.times(Math.cos(angle)).plus(axisY.times(Math.sin(angle)));
        VectorCSGd pos = s.plus(ray.times(stack)).plus(out.times(r));
        return pos;
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
