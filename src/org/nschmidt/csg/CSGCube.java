/**
 * CSGCube.java
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
import java.util.List;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GColourIndex;

/**
 * An axis-aligned solid cuboid defined by {@code center} and {@code dimensions}
 * .
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class CSGCube extends CSGPrimitive implements Primitive {

    public final int ID = id_counter.getAndIncrement();

    /**
     * Center of this cube.
     */
    private VectorCSGd center;
    /**
     * Cube dimensions.
     */
    private VectorCSGd dimensions;

    private boolean centered = true;

    /**
     * Constructor. Creates a new cube with center {@code [0,0,0]} and
     * dimensions {@code [1,1,1]}.
     */
    public CSGCube() {
        center = new VectorCSGd(0d, 0d, 0d);
        dimensions = new VectorCSGd(2000d, 2000d, 2000d);
    }

    /**
     * Constructor. Creates a new cuboid with the specified center and
     * dimensions.
     *
     * @param center
     *            center of the cuboid
     * @param dimensions
     *            cube dimensions
     */
    public CSGCube(VectorCSGd center, VectorCSGd dimensions) {
        this.center = center;
        this.dimensions = dimensions;
    }

    @Override
    public List<Polygon> toPolygons(DatFile df, GColour colour) {

        int[][][] a = {
                // position // normal
                { { 0, 4, 6, 2 }, { -1, 0, 0 } }, { { 1, 3, 7, 5 }, { +1, 0, 0 } }, { { 0, 1, 5, 4 }, { 0, -1, 0 } }, { { 2, 6, 7, 3 }, { 0, +1, 0 } }, { { 0, 2, 3, 1 }, { 0, 0, -1 } },
                { { 4, 5, 7, 6 }, { 0, 0, +1 } } };
        List<Polygon> polygons = new ArrayList<>();
        for (int[][] info : a) {
            List<VectorCSGd> vertices = new ArrayList<>();
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

    @Override
    public CSG toCSG(DatFile df, GColour colour) {
        return CSG.fromPolygons(toPolygons(df, colour));
    }

}
