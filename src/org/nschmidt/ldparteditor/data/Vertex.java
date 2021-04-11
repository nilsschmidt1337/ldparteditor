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
package org.nschmidt.ldparteditor.data;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.helpers.math.Vector3r;

/**
 * Full immutable vertex class
 *
 * @author nils
 *
 */
public class Vertex implements Comparable<Vertex> {

    private static final float EPSILON = 0.0001f;

    public final BigDecimal xp;
    public final BigDecimal yp;
    public final BigDecimal zp;
    public final float x;
    public final float y;
    public final float z;
    private final Vector4f vector4f;
    private final float roundedX;
    private final float roundedY;
    private final float roundedZ;

    // Lowest accuracy version (simple float to BigDecimal cast)
    public Vertex(Vector4f vertex) {

        this.x = vertex.x;
        this.y = vertex.y;
        this.z = vertex.z;

        BigDecimal rx = new BigDecimal(this.x);
        xp = rx.scaleByPowerOfTen(-3);
        rx = rx.setScale(2, RoundingMode.HALF_UP);

        BigDecimal ry = new BigDecimal(this.y);
        yp = ry.scaleByPowerOfTen(-3);
        ry = ry.setScale(2, RoundingMode.HALF_UP);

        BigDecimal rz = new BigDecimal(this.z);
        zp = rz.scaleByPowerOfTen(-3);
        rz = rz.setScale(2, RoundingMode.HALF_UP);

        this.roundedX = rx.floatValue();
        this.roundedY = ry.floatValue();
        this.roundedZ = rz.floatValue();

        this.vector4f = new Vector4f(vertex);

        // NLogger.error(getClass(), "Standard accuracy on vertex."); //$NON-NLS-1$
    }

    public Vertex(Vector3d v3d) {
        this(v3d.x, v3d.y, v3d.z);
    }

    // High accuracy version
    public Vertex(BigDecimal bx, BigDecimal by, BigDecimal bz) {

        this.x = bx.floatValue() * 1000f;
        this.y = by.floatValue() * 1000f;
        this.z = bz.floatValue() * 1000f;

        this.vector4f = new Vector4f(this.x, this.y, this.z, 1f);

        this.xp = bx;
        this.yp = by;
        this.zp = bz;

        BigDecimal rx = new BigDecimal(this.x);
        rx = rx.setScale(2, RoundingMode.HALF_UP);

        BigDecimal ry = new BigDecimal(this.y);
        ry = ry.setScale(2, RoundingMode.HALF_UP);

        BigDecimal rz = new BigDecimal(this.z);
        rz = rz.setScale(2, RoundingMode.HALF_UP);

        this.roundedX = rx.floatValue();
        this.roundedY = ry.floatValue();
        this.roundedZ = rz.floatValue();
    }

    // High performance version / only for texture rendering, primitive preview
    public Vertex(float vx, float vy, float vz, boolean hp) {

        this.x = vx;
        this.y = vy;
        this.z = vz;

        this.vector4f = null;
        this.xp = null;
        this.yp = null;
        this.zp = null;

        BigDecimal rx = new BigDecimal(this.x);
        rx = rx.setScale(2, RoundingMode.HALF_UP);

        BigDecimal ry = new BigDecimal(this.y);
        ry = ry.setScale(2, RoundingMode.HALF_UP);

        BigDecimal rz = new BigDecimal(this.z);
        rz = rz.setScale(2, RoundingMode.HALF_UP);

        this.roundedX = rx.floatValue();
        this.roundedY = ry.floatValue();
        this.roundedZ = rz.floatValue();
    }

    // High accuracy version (better performance)
    Vertex(BigDecimal bx, BigDecimal by, BigDecimal bz, Vector4f vertex) {

        this.x = vertex.x;
        this.y = vertex.y;
        this.z = vertex.z;

        this.vector4f = new Vector4f(vertex);

        this.xp = bx;
        this.yp = by;
        this.zp = bz;

        BigDecimal rx = new BigDecimal(this.x);
        rx = rx.setScale(2, RoundingMode.HALF_UP);

        BigDecimal ry = new BigDecimal(this.y);
        ry = ry.setScale(2, RoundingMode.HALF_UP);

        BigDecimal rz = new BigDecimal(this.z);
        rz = rz.setScale(2, RoundingMode.HALF_UP);

        this.roundedX = rx.floatValue();
        this.roundedY = ry.floatValue();
        this.roundedZ = rz.floatValue();
    }

    public Vertex(float x, float y, float z) {
        this(new Vector4f(x, y, z, 1f));
    }

    public Vertex(BigDecimal[] bdArray) {
        this(bdArray[0], bdArray[1], bdArray[2]);
    }

    Vertex(Vector3r vector3r) {
        this(vector3r.x.bigDecimalValue(), vector3r.y.bigDecimalValue(), vector3r.z.bigDecimalValue());
    }

    public final Vector4f toVector4f() {
        return new Vector4f(vector4f);
    }

    final Vector4f toVector4fm() {
        return vector4f;
    }

    @Override
    public int hashCode() {
        return 1337;
    }

    @Override
    public boolean equals(Object obj) {
        Vertex other = (Vertex) obj;
        return Math.abs(roundedX - other.roundedX) < EPSILON && Math.abs(roundedY - other.roundedY) < EPSILON
                && Math.abs(roundedZ - other.roundedZ) < EPSILON;
    }

    @Override
    public String toString() {
        return Math.round(x / 10f) / 100f + "|" + Math.round(y / 10f) / 100f + "|" + Math.round(z / 10f) / 100f; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public int compareTo(Vertex o) {
        {
            float d1 = roundedX - o.roundedX;
            int c1 = Float.compare(Math.abs(d1), EPSILON);
            switch (c1) {
            case 0:
            case 1:
                return d1 < 0f ? -1 : 1;
            default:
                break;
            }
        }
        {
            float d1 = roundedY - o.roundedY;
            int c1 = Float.compare(Math.abs(d1), EPSILON);
            switch (c1) {
            case 0:
            case 1:
                return d1 < 0f ? -1 : 1;
            default:
                break;
            }
        }
        {
            float d1 = roundedZ - o.roundedZ;
            int c1 = Float.compare(Math.abs(d1), EPSILON);
            switch (c1) {
            case 0:
            case 1:
                return d1 < 0f ? -1 : 1;
            default:
                break;
            }
        }
        return 0;
    }

}
