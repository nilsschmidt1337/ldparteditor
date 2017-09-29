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
import java.util.List;

import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GColourIndex;

public class CSGSphere extends CSGPrimitive implements Primitive {

    public final int ID = id_counter.getAndIncrement();

    private final int numSlices;
    private final int numStacks;

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
        this.numSlices = numSlices;
        this.numStacks = numStacks;
    }

    private Vector3d sphereVertex(double theta, double phi) {
        theta *= Math.PI * 2;
        phi *= Math.PI;
        Vector3d dir = new Vector3d(Math.cos(theta) * Math.sin(phi), Math.cos(phi), Math.sin(theta) * Math.sin(phi));
        return dir.times(1000d);
    }

    @Override
    public List<Triangle> toTriangles(DatFile df, GColour colour) {
        List<Triangle> polygons = new ArrayList<Triangle>();
        for (int i = 0; i < numSlices; i++) {
            for (int j = 0; j < numStacks; j++) {
                final List<Vector3d> vertices = new ArrayList<Vector3d>();

                vertices.add(sphereVertex(i / (double) numSlices, j / (double) numStacks));
                if (j > 0) {
                    vertices.add(sphereVertex((i + 1) / (double) numSlices, j / (double) numStacks));
                }
                if (j < numStacks - 1) {
                    vertices.add(sphereVertex((i + 1) / (double) numSlices, (j + 1) / (double) numStacks));
                }
                vertices.add(sphereVertex(i / (double) numSlices, (j + 1) / (double) numStacks));

                final int polysize = vertices.size();
                if (polysize == 4) {
                    polygons.add(new Triangle(df, vertices.get(0), vertices.get(1), vertices.get(2), new GColourIndex(colour, ID)));
                    polygons.add(new Triangle(df, vertices.get(2), vertices.get(3), vertices.get(0), new GColourIndex(colour, ID)));
                } else if (polysize == 3) {
                    polygons.add(new Triangle(df, vertices.get(0), vertices.get(1), vertices.get(2), new GColourIndex(colour, ID)));
                }
            }
        }
        return polygons;
    }

    @Override
    public CSG toCSG(DatFile df, GColour colour) {
        return CSG.fromTriangles(toTriangles(df, colour));
    }
}
