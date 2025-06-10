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
package org.nschmidt.ldparteditor.helper.math.rtree;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.Objects;

import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GData3;
import org.nschmidt.ldparteditor.data.GData4;

public class BoundingBox {

    // TODO: Check if we need BigDecimal here.
    private float minX = 0f;
    private float minY = 0f;
    private float minZ = 0f;

    private float maxX = 0f;
    private float maxY = 0f;
    private float maxZ = 0f;

    public BoundingBox() {}

    public BoundingBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public float area() {
        // Calculate the area of this box
        final float dX = maxX - minX;
        final float dY = maxY - minY;
        final float dZ = maxZ - minZ;
        return 2f * (dX * dY + dX * dZ + dY * dZ);
    }

    public BoundingBox intersection(BoundingBox o) {
        if (!this.intersects(o)) {
            return new BoundingBox();
        }

        float iMinX = max(minX, o.minX);
        float iMinY = max(minY, o.minY);
        float iMinZ = max(minZ, o.minZ);

        float iMaxX = min(maxX, o.maxX);
        float iMaxY = min(maxY, o.maxY);
        float iMaxZ = min(maxZ, o.maxZ);

        return new BoundingBox(iMinX, iMinY, iMinZ, iMaxX, iMaxY, iMaxZ);
    }

    public boolean contains(float x, float y, float z) {
        return x >= minX && x <= maxX
            && x >= minY && x <= maxY
            && x >= minZ && x <= maxZ;
    }

    public boolean intersects(BoundingBox o) {
        return !((minX > o.maxX || maxX < o.minX) ||
                 (minY > o.maxY || maxY < o.minY) ||
                 (minZ > o.maxZ || maxZ < o.minZ));
    }

    public void insert(BoundingBox o) {
        minX = min(minX, o.minX);
        minY = min(minY, o.minY);
        minZ = min(minZ, o.minZ);

        maxX = max(maxX, o.maxX);
        maxY = max(maxY, o.maxY);
        maxZ = max(maxZ, o.maxZ);
    }

    public void insert(GData geometry) {
        if (geometry instanceof GData3 triangle) {
            insert(triangle);
        } else if (geometry instanceof GData4 quad) {
            insert(quad);
        } else {
            throw new IllegalArgumentException("Type " + geometry.type() + " is not supported!"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxX, maxY, maxZ, minX, minY, minZ);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof BoundingBox))
            return false;
        BoundingBox other = (BoundingBox) obj;
        return Float.floatToIntBits(maxX) == Float.floatToIntBits(other.maxX)
                && Float.floatToIntBits(maxY) == Float.floatToIntBits(other.maxY)
                && Float.floatToIntBits(maxZ) == Float.floatToIntBits(other.maxZ)
                && Float.floatToIntBits(minX) == Float.floatToIntBits(other.minX)
                && Float.floatToIntBits(minY) == Float.floatToIntBits(other.minY)
                && Float.floatToIntBits(minZ) == Float.floatToIntBits(other.minZ);
    }

    private void insert(GData3 triangle) {
        insertX(triangle.x1);
        insertY(triangle.y1);
        insertZ(triangle.z1);
        insertX(triangle.x2);
        insertY(triangle.y2);
        insertZ(triangle.z2);
        insertX(triangle.x3);
        insertY(triangle.y3);
        insertZ(triangle.z3);
    }

    private void insert(GData4 quad) {
        insertX(quad.x1);
        insertY(quad.y1);
        insertZ(quad.z1);
        insertX(quad.x2);
        insertY(quad.y2);
        insertZ(quad.z2);
        insertX(quad.x3);
        insertY(quad.y3);
        insertZ(quad.z3);
        insertX(quad.x4);
        insertY(quad.y4);
        insertZ(quad.z4);
    }

    private void insertX(float x) {
        minX = min(minX, x);
        maxX = max(maxX, x);
    }

    private void insertY(float y) {
        minY = min(minY, y);
        maxY = max(maxY, y);
    }

    private void insertZ(float z) {
        minZ = min(minZ, z);
        maxZ = max(maxZ, z);
    }
}
