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
package org.nschmidt.ldparteditor.helpers.math;

import java.math.BigDecimal;

import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.data.Vertex;

/**
 * Absolute precise vector
 *
 * Note: Needs no hashCode() and equals()
 * @author nils
 *
 */
public class Vector3r {

    public Rational x;
    public Rational y;
    public Rational z;

    public Vector3r(Vector4f tmp) {
        this(new Vertex(tmp));
    }

    public Vector3r(Vector3r tmp) {
        this(tmp.x, tmp.y, tmp.z);
    }

    public Vector3r(Vertex tmp) {
        this(tmp.xp, tmp.yp, tmp.zp);
    }

    private Vector3r(BigDecimal x, BigDecimal y, BigDecimal z) {
        this.x = new Rational(x);
        this.y = new Rational(y);
        this.z = new Rational(z);
    }

    public Vector3r() {
        this.x = new Rational();
        this.y = new Rational();
        this.z = new Rational();
    }

    public Vector3r(Rational x, Rational y, Rational z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void negate() {
        this.x = this.x.negate();
        this.y = this.y.negate();
        this.z = this.z.negate();
    }

    public static Vector3r sub(Vector3r a, Vector3r b) {
        return new Vector3r(a.x.subtract(b.x), a.y.subtract(b.y), a.z.subtract(b.z));
    }

    public static Vector3r cross(Vector3r a, Vector3r b) {
        Rational x = a.y.multiply(b.z).subtract(a.z.multiply(b.y));
        Rational y = a.z.multiply(b.x).subtract(a.x.multiply(b.z));
        Rational z = a.x.multiply(b.y).subtract(a.y.multiply(b.x));
        return new Vector3r(x, y, z);
    }

    public static Rational dot(Vector3r a, Vector3r b) {
        return a.x.multiply(b.x).add(a.y.multiply(b.y)).add(a.z.multiply(b.z));
    }

    public void setX(Rational tmp) {
        this.x = tmp;
    }

    public void setY(Rational tmp) {
        this.y = tmp;
    }

    public void setZ(Rational tmp) {
        this.z = tmp;
    }

    public void set(Vertex v) {
        this.x = new Rational(v.xp);
        this.y = new Rational(v.yp);
        this.z = new Rational(v.zp);
    }

    @Override
    public String toString() {
        return this.x.toString() + " | " + this.y.toString() + " | " + this.z.toString(); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public boolean equals2d(Vector3r other) {
        return this.x.compareTo(other.x) == 0 && this.y.compareTo(other.y) == 0;
    }
}
