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

    public Rational X;
    public Rational Y;
    public Rational Z;

    public Vector3r(Vector4f tmp) {
        this(new Vertex(tmp));
    }

    public Vector3r(Vector3r tmp) {
        this(tmp.X, tmp.Y, tmp.Z);
    }

    public Vector3r(Vertex tmp) {
        this(tmp.X, tmp.Y, tmp.Z);
    }

    private Vector3r(BigDecimal x, BigDecimal y, BigDecimal z) {
        this.X = new Rational(x);
        this.Y = new Rational(y);
        this.Z = new Rational(z);
    }

    public Vector3r() {
        this.X = new Rational();
        this.Y = new Rational();
        this.Z = new Rational();
    }

    public Vector3r(Rational x, Rational y, Rational z) {
        this.X = x;
        this.Y = y;
        this.Z = z;
    }

    public void negate() {
        this.X = this.X.negate();
        this.Y = this.Y.negate();
        this.Z = this.Z.negate();
    }

    public static Vector3r sub(Vector3r a, Vector3r b) {
        return new Vector3r(a.X.subtract(b.X), a.Y.subtract(b.Y), a.Z.subtract(b.Z));
    }

    public static Vector3r cross(Vector3r a, Vector3r b) {
        Rational x = a.Y.multiply(b.Z).subtract(a.Z.multiply(b.Y));
        Rational y = a.Z.multiply(b.X).subtract(a.X.multiply(b.Z));
        Rational z = a.X.multiply(b.Y).subtract(a.Y.multiply(b.X));
        return new Vector3r(x, y, z);
    }

    public static Rational dot(Vector3r a, Vector3r b) {
        return a.X.multiply(b.X).add(a.Y.multiply(b.Y)).add(a.Z.multiply(b.Z));
    }

    public void setX(Rational tmp) {
        this.X = tmp;
    }

    public void setY(Rational tmp) {
        this.Y = tmp;
    }

    public void setZ(Rational tmp) {
        this.Z = tmp;
    }

    public void set(Vertex v) {
        this.X = new Rational(v.X);
        this.Y = new Rational(v.Y);
        this.Z = new Rational(v.Z);
    }

    @Override
    public String toString() {
        return this.X.toString() + " | " + this.Y.toString() + " | " + this.Z.toString(); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public boolean equals2d(Vector3r other) {
        return this.X.compareTo(other.X) == 0 && this.Y.compareTo(other.Y) == 0;
    }
}
