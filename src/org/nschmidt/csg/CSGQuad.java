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
import java.util.List;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GColourIndex;

/**
 * An axis-aligned solid quad defined by {@code center} and {@code dimensions}.
 *
 * @author nils
 */
public class CSGQuad extends CSGPrimitive implements Primitive {

    public final int ID = id_counter.getAndIncrement();

    /**
     * Center of this quad.
     */
    private VectorCSGd center;
    /**
     * Cube dimensions.
     */
    private VectorCSGd dimensions;

    private boolean centered = true;

    /**
     * Constructor. Creates a new quad with center {@code [0,0,0]} and
     * dimensions {@code [1,1,1]}.
     */
    public CSGQuad() {
        center = new VectorCSGd(0d, 0d, 0d);
        dimensions = new VectorCSGd(2000d, 1d, 2000d);
    }

    /**
     * Constructor. Creates a new quad with center {@code [0,0,0]} and
     * dimensions {@code [size,0,size]}.
     *
     * @param size
     *            size
     */
    public CSGQuad(double size) {
        center = new VectorCSGd(0d, 0d, 0d);
        dimensions = new VectorCSGd(size, 1d, size);
    }

    @Override
    public List<Polygon> toPolygons(DatFile df, GColour colour) {

        int[][][] a = {
                // position // normal
                { { 0, 4, 6, 2 }, { -1, 0, 0 } }, { { 1, 3, 7, 5 }, { +1, 0, 0 } }, { { 0, 1, 5, 4 }, { 0, -1, 0 } }, { { 2, 6, 7, 3 }, { 0, +1, 0 } }, { { 0, 2, 3, 1 }, { 0, 0, -1 } },
                { { 4, 5, 7, 6 }, { 0, 0, +1 } } };
        List<Polygon> polygons = new ArrayList<Polygon>();
        for (int[][] info : a) {
            List<VectorCSGd> vertices = new ArrayList<VectorCSGd>();
            for (int i : info[0]) {
                VectorCSGd pos = new VectorCSGd(center.x + dimensions.x * (1 * Math.min(1, i & 1) - 0.5), center.y + dimensions.y * (1 * Math.min(1, i & 2) - 0.5), center.z + dimensions.z
                        * (1 * Math.min(1, i & 4) - 0.5));
                vertices.add(pos);
            }
            polygons.add(new Polygon(df, vertices, new GColourIndex(colour, ID)));
        }

        if (!centered) {

            Transform centerTransform = Transform.unity().apply(
                    Matrix4f.setIdentity(new Matrix4f()).translate(new Vector3f((float) dimensions.x / 2f, (float) dimensions.y / 2f, (float) dimensions.z / 2f)));

            for (Polygon p : polygons) {
                p.transform(centerTransform);
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
     * @return the dimensions
     */
    public VectorCSGd getDimensions() {
        return dimensions;
    }

    /**
     * @param dimensions
     *            the dimensions to set
     */
    public void setDimensions(VectorCSGd dimensions) {
        this.dimensions = dimensions;
    }

    public CSGQuad noCenter() {
        centered = false;
        return this;
    }

    @Override
    public CSG toCSG(DatFile df, GColour colour) {
        return CSG.fromPolygons(toPolygons(df, colour));
    }

}
