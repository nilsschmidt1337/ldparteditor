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

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GData3;
import org.nschmidt.ldparteditor.data.GData4;
import org.nschmidt.ldparteditor.data.Vertex;

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

    public float areaHalf() {
        // Calculate the 1/2 area of this box
        final float dX = maxX - minX;
        final float dY = maxY - minY;
        final float dZ = maxZ - minZ;
        return dX * dY + dX * dZ + dY * dZ;
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
            && y >= minY && y <= maxY
            && z >= minZ && z <= maxZ;
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

    public void insert(GData geometry, Map<GData3, Vertex[]> triangles, Map<GData4, Vertex[]> quads) {
        if (geometry instanceof GData3 triangle) {
            insert(triangle, triangles);
        } else if (geometry instanceof GData4 quad) {
            insert(quad, quads);
        } else {
            throw new IllegalArgumentException("Type " + geometry.type() + " is not supported!"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public BoundingBox copy() {
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public boolean isIntersecting(Vector4f rayOrigin, float[] rayDirection) {
        // Is This normalisation really necessary?
        final Vector4f rayDir = new Vector4f(rayDirection[0], rayDirection[1], rayDirection[2], 0f);

        if (rayDir.lengthSquared() == 0f) {
            return false;
        }

        rayDir.normalise();

        return isIntersecting2(rayOrigin, new float[] {rayDir.x, rayDir.y, rayDir.z}) || isIntersecting2(rayOrigin, new float[] {-rayDir.x, -rayDir.y, -rayDir.z});
    }


    private boolean isIntersecting2(Vector4f rayOrigin, float[] rayDirection) {
        float t1 = 0f;
        float t2 = 0f;
        float tnear = Float.NEGATIVE_INFINITY;
        float tfar = Float.POSITIVE_INFINITY;
        float temp = 0f;

        final float rox = rayOrigin.x;
        final float rdx = rayDirection[0];
        if(rdx == 0){
            if(rox < minX || rox > maxX)
                return false;
        } else {
            t1 = (minX - rox) / rdx;
            t2 = (maxX - rox) / rdx;
            if (t1 > t2) {
                temp = t1;
                t1 = t2;
                t2 = temp;
            }

            if (t1 > tnear)
                tnear = t1;
            if (t2 < tfar)
                tfar = t2;
            if (tnear > tfar)
                return false;
            if (tfar < 0)
                return false;
        }

        final float roy = rayOrigin.y;
        final float rdy = rayDirection[1];
        if(rdy == 0){
            if(roy < minY || roy > maxY)
                return false;
        } else {
            t1 = (minY - roy) / rdy;
            t2 = (maxY - roy) / rdy;
            if (t1 > t2) {
                temp = t1;
                t1 = t2;
                t2 = temp;
            }

            if (t1 > tnear)
                tnear = t1;
            if (t2 < tfar)
                tfar = t2;
            if (tnear > tfar)
                return false;
            if (tfar < 0)
                return false;
        }

        final float roz = rayOrigin.z;
        final float rdz = rayDirection[2];
        if(rdz == 0){
            if(roz < minZ || roz > maxZ)
                return false;
        } else {
            t1 = (minZ - roz) / rdz;
            t2 = (maxZ - roz) / rdz;
            if (t1 > t2) {
                temp = t1;
                t1 = t2;
                t2 = temp;
            }

            if (t1 > tnear)
                tnear = t1;
            if (t2 < tfar)
                tfar = t2;
            if (tnear > tfar)
                return false;
            if (tfar < 0)
                return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "BoundingBox [min=" + List.of(minX, minY, minZ) + ", max=" + List.of(maxX, maxY, maxZ) + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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

    private void insert(GData3 triangle, Map<GData3, Vertex[]> triangles) {
        for (Vertex v : triangles.get(triangle)) {
            insertX(v.x);
            insertY(v.y);
            insertZ(v.z);
        }
    }

    private void insert(GData4 quad, Map<GData4, Vertex[]> quads) {
        for (Vertex v : quads.get(quad)) {
            insertX(v.x);
            insertY(v.y);
            insertZ(v.z);
        }
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
