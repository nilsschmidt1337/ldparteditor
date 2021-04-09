/**
 * CSGSphere.java
 *
 * Copyright 2014-2014 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of Michael Hoffer <info@michaelhoffer.de>.
 */

package org.nschmidt.csg;

import java.util.ArrayList;
import java.util.List;

import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GColourIndex;

/**
 * A solid sphere.
 *
 * The tessellation along the longitude and latitude directions can be
 * controlled via the {@link #numSlices} and {@link #numStacks} parameters.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class CSGSphere extends CSGPrimitive implements Primitive {

    public final int id = id_counter.getAndIncrement();

    private VectorCSGd center;
    private double radius;
    private int numSlices;
    private int numStacks;

    /**
     * Constructor. Creates a sphere with radius 1, 16 slices and 8 stacks and
     * center [0,0,0].
     *
     */
    public CSGSphere() {
        init();
    }

    /**
     * Constructor. Creates a sphere with the specified number of slices and
     * stacks.
     *
     * @param numSlices
     *            number of slices
     * @param numStacks
     *            number of stacks
     */
    public CSGSphere(int numSlices, int numStacks) {
        this.center = new VectorCSGd(0, 0, 0);
        this.radius = 1000d;
        this.numSlices = numSlices;
        this.numStacks = numStacks;
    }

    private void init() {
        center = new VectorCSGd(0, 0, 0);
        radius = 1000d;
        numSlices = 16;
        numStacks = 8;
    }

    private VectorCSGd sphereVertex(VectorCSGd c, double r, double theta, double phi) {
        theta *= Math.PI * 2;
        phi *= Math.PI;
        VectorCSGd dir = new VectorCSGd(Math.cos(theta) * Math.sin(phi), Math.cos(phi), Math.sin(theta) * Math.sin(phi));
        return c.plus(dir.times(r));
    }

    @Override
    public List<Polygon> toPolygons(DatFile df, GColour colour) {
        List<Polygon> polygons = new ArrayList<>();
        for (int i = 0; i < numSlices; i++) {
            for (int j = 0; j < numStacks; j++) {
                final List<VectorCSGd> vertices = new ArrayList<>();

                vertices.add(sphereVertex(center, radius, i / (double) numSlices, j / (double) numStacks));
                if (j > 0) {
                    vertices.add(sphereVertex(center, radius, (i + 1) / (double) numSlices, j / (double) numStacks));
                }
                if (j < numStacks - 1) {
                    vertices.add(sphereVertex(center, radius, (i + 1) / (double) numSlices, (j + 1) / (double) numStacks));
                }
                vertices.add(sphereVertex(center, radius, i / (double) numSlices, (j + 1) / (double) numStacks));
                polygons.add(new Polygon(df, vertices, new GColourIndex(colour, id)));
            }
        }
        return polygons;
    }

    /**
     * @return the center
     */
    public VectorCSGd getCenter() {
        return center;
    }

    /**
     * @param center
     *            the center to set
     */
    public void setCenter(VectorCSGd center) {
        this.center = center;
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
     * @return the numSlices
     */
    public int getNumSlices() {
        return numSlices;
    }

    /**
     * @param numSlices
     *            the numSlices to set
     */
    public void setNumSlices(int numSlices) {
        this.numSlices = numSlices;
    }

    /**
     * @return the numStacks
     */
    public int getNumStacks() {
        return numStacks;
    }

    /**
     * @param numStacks
     *            the numStacks to set
     */
    public void setNumStacks(int numStacks) {
        this.numStacks = numStacks;
    }

    @Override
    public CSG toCSG(DatFile df, GColour colour) {
        return CSG.fromPolygons(toPolygons(df, colour));
    }
}
