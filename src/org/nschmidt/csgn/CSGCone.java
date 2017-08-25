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

public class CSGCone extends CSGPrimitive implements Primitive {

    public final int ID = id_counter.getAndIncrement();

    private final int numSlices;

    /**
     * Constructor. Creates a cone . The resolution of the tessellation can
     * be controlled with {@code numSlices}.
     *
     * @param numSlices
     *            number of slices (used for tessellation)
     */
    public CSGCone(int numSlices) {
        this.numSlices = numSlices;
    }

    @Override
    public List<Triangle> toTriangles(DatFile df, GColour colour) {
        final Vector3d axisZ = new Vector3d(0, 1d, 0);
        final Vector3d axisX = new Vector3d(1, 0, 0).cross(axisZ).unit();
        final Vector3d axisY = axisX.cross(axisZ).unit();
        List<Triangle> polygons = new ArrayList<>();

        for (int i = 0; i < numSlices; i++) {
            double t0 = i / (double) numSlices, t1 = (i + 1) / (double) numSlices;
            {
                Vector3d v1 = new Vector3d(0, 0, 0);
                Vector3d v2 = cylPoint(axisX, axisY, 1000d, t0, -1);
                Vector3d v3 = cylPoint(axisX, axisY, 1000d, t1, -1);
                polygons.add(new Triangle(df, v1, v2, v3, new GColourIndex(colour, ID)));
            }
            {
                Vector3d v1 = new Vector3d(0, 1000d, 0);
                Vector3d v2 = cylPoint(axisX, axisY, 1000d, t1, 1);
                Vector3d v3 =  cylPoint(axisX, axisY, 1000d, t0, 1);
                polygons.add(new Triangle(df, v1, v2, v3, new GColourIndex(colour, ID)));
            }
        }

        return polygons;
    }

    private Vector3d cylPoint(Vector3d axisX, Vector3d axisY, double stack, double slice, double normalBlend) {
        double angle = slice * Math.PI * 2;
        Vector3d out = axisX.times(Math.cos(angle)).plus(axisY.times(Math.sin(angle)));
        Vector3d pos = new Vector3d(0, stack, 0).plus(out.times(1000d));
        return pos;
    }

    @Override
    public CSG toCSG(DatFile df, GColour colour) {
        return CSG.fromTriangles(toTriangles(df, colour));
    }
}
