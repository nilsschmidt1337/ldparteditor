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

    public final BigDecimal X;
    public final BigDecimal Y;
    public final BigDecimal Z;
    public final float x;
    public final float y;
    public final float z;
    private final Vector4f vertex;
    private final float rounded_x;
    private final float rounded_y;
    private final float rounded_z;

    // Lowest accuracy version (simple float to BigDecimal cast)
    public Vertex(Vector4f vertex) {

        this.x = vertex.x;
        this.y = vertex.y;
        this.z = vertex.z;

        BigDecimal x = new BigDecimal(this.x);
        X = x.scaleByPowerOfTen(-3);
        x = x.setScale(2, RoundingMode.HALF_UP);

        BigDecimal y = new BigDecimal(this.y);
        Y = y.scaleByPowerOfTen(-3);
        y = y.setScale(2, RoundingMode.HALF_UP);

        BigDecimal z = new BigDecimal(this.z);
        Z = z.scaleByPowerOfTen(-3);
        z = z.setScale(2, RoundingMode.HALF_UP);

        this.rounded_x = x.floatValue();
        this.rounded_y = y.floatValue();
        this.rounded_z = z.floatValue();

        this.vertex = new Vector4f(vertex);

        // NLogger.error(getClass(), "Standard accuracy on vertex."); //$NON-NLS-1$
    }

    public Vertex(Vector3d v3d) {
        this(v3d.X, v3d.Y, v3d.Z);
    }

    // High accuracy version
    public Vertex(BigDecimal bx, BigDecimal by, BigDecimal bz) {

        this.x = bx.floatValue() * 1000f;
        this.y = by.floatValue() * 1000f;
        this.z = bz.floatValue() * 1000f;

        this.vertex = new Vector4f(this.x, this.y, this.z, 1f);

        this.X = bx;
        this.Y = by;
        this.Z = bz;

        BigDecimal x = new BigDecimal(this.x);
        x = x.setScale(2, RoundingMode.HALF_UP);

        BigDecimal y = new BigDecimal(this.y);
        y = y.setScale(2, RoundingMode.HALF_UP);

        BigDecimal z = new BigDecimal(this.z);
        z = z.setScale(2, RoundingMode.HALF_UP);

        this.rounded_x = x.floatValue();
        this.rounded_y = y.floatValue();
        this.rounded_z = z.floatValue();
    }

    // High performance version / only for texture rendering, primitive preview
    public Vertex(float vx, float vy, float vz, boolean hp) {

        this.x = vx;
        this.y = vy;
        this.z = vz;

        this.vertex = null;
        this.X = null;
        this.Y = null;
        this.Z = null;

        BigDecimal x = new BigDecimal(this.x);
        x = x.setScale(2, RoundingMode.HALF_UP);

        BigDecimal y = new BigDecimal(this.y);
        y = y.setScale(2, RoundingMode.HALF_UP);

        BigDecimal z = new BigDecimal(this.z);
        z = z.setScale(2, RoundingMode.HALF_UP);

        this.rounded_x = x.floatValue();
        this.rounded_y = y.floatValue();
        this.rounded_z = z.floatValue();
    }

    // High accuracy version (better performance)
    Vertex(BigDecimal bx, BigDecimal by, BigDecimal bz, Vector4f vertex) {

        this.x = vertex.x;
        this.y = vertex.y;
        this.z = vertex.z;

        this.vertex = new Vector4f(vertex);

        this.X = bx;
        this.Y = by;
        this.Z = bz;

        BigDecimal x = new BigDecimal(this.x);
        x = x.setScale(2, RoundingMode.HALF_UP);

        BigDecimal y = new BigDecimal(this.y);
        y = y.setScale(2, RoundingMode.HALF_UP);

        BigDecimal z = new BigDecimal(this.z);
        z = z.setScale(2, RoundingMode.HALF_UP);

        this.rounded_x = x.floatValue();
        this.rounded_y = y.floatValue();
        this.rounded_z = z.floatValue();
    }

    public Vertex(float x, float y, float z) {
        this(new Vector4f(x, y, z, 1f));
    }

    public Vertex(BigDecimal[] bdArray) {
        this(bdArray[0], bdArray[1], bdArray[2]);
    }

    Vertex(Vector3r vector3r) {
        this(vector3r.X.BigDecimalValue(), vector3r.Y.BigDecimalValue(), vector3r.Z.BigDecimalValue());
    }

    public final Vector4f toVector4f() {
        return new Vector4f(vertex);
    };

    final Vector4f toVector4fm() {
        return vertex;
    };

    @Override
    public int hashCode() {
        return 1337;
    }

    @Override
    public boolean equals(Object obj) {
        Vertex other = (Vertex) obj;
        return Math.abs(rounded_x - other.rounded_x) < EPSILON && Math.abs(rounded_y - other.rounded_y) < EPSILON
                && Math.abs(rounded_z - other.rounded_z) < EPSILON;
    }

    @Override
    public String toString() {
        return Math.round(x / 10f) / 100f + "|" + Math.round(y / 10f) / 100f + "|" + Math.round(z / 10f) / 100f; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public int compareTo(Vertex o) {
        {
            float d1 = rounded_x - o.rounded_x;
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
            float d1 = rounded_y - o.rounded_y;
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
            float d1 = rounded_z - o.rounded_z;
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
