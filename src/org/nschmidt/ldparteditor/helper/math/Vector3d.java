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
package org.nschmidt.ldparteditor.helper.math;

import java.math.BigDecimal;
import java.math.MathContext;

import org.nschmidt.csg.VectorCSGd;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.enumtype.Threshold;

/**
 * Holds a 3-tuple BigDecimal vector for maximum numeric precision.
 *
 *         Note: It is not intended to write something like
 *         {@code vectorA.X = vectorB.X;} use {@code vectorA.setX(...)} instead.
 */
public class Vector3d {

    /** A thread safe static reference to the math context constant */
    private static MathContext mc = Threshold.MC;
    /** The x component of the vector. */
    public BigDecimal x;
    /** The y component of the vector. */
    public BigDecimal y;
    /** The z component of the vector. */
    public BigDecimal z;

    /**
     * Creates a new 3-tuple BigDecimal vector with the initial value (0, 0, 0).
     */
    public Vector3d() {
        this(new BigDecimal(0), new BigDecimal(0), new BigDecimal(0));
    }

    /**
     * Creates a new 3-tuple BigDecimal vector with the initial value (x, y, z).
     *
     * @param x
     *            the x component of the vector.
     * @param y
     *            the y component of the vector.
     * @param z
     *            the z component of the vector.
     */
    public Vector3d(BigDecimal x, BigDecimal y, BigDecimal z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3d(Vector3d vertex) {
        this.x = vertex.x;
        this.y = vertex.y;
        this.z = vertex.z;
    }

    public Vector3d(Vertex vertex) {
        this.x = vertex.xp;
        this.y = vertex.yp;
        this.z = vertex.zp;
    }

    @SuppressWarnings("java:S2111")
    public Vector3d(VectorCSGd v) {
        this(new BigDecimal(v.x), new BigDecimal(v.y), new BigDecimal(v.z));
    }

    /**
     * Sets the x component of the vector.
     *
     * @param x
     *            the component value to set
     */
    public void setX(BigDecimal x) {
        this.x = x;
    }

    /**
     * Sets the y component of the vector.
     *
     * @param y
     *            the component value to set
     */
    public void setY(BigDecimal y) {
        this.y = y;
    }

    /**
     * Sets the z component of the vector.
     *
     * @param z
     *            the component value to set
     */
    public void setZ(BigDecimal z) {
        this.z = z;
    }

    /**
     * Calculates the difference between two vectors.
     *
     * @param left
     *            the left argument vector from the operand.
     * @param right
     *            the right argument vector from the operand.
     * @param target
     *            the target vector, which will store the result of the
     *            substraction.
     * @return the difference vector
     */
    public static Vector3d sub(Vector3d left, Vector3d right, Vector3d target) {
        Vector3d result = new Vector3d(left.x.subtract(right.x, mc), left.y.subtract(right.y, mc), left.z.subtract(right.z, mc));
        if (target != null) {
            target.setX(result.x);
            target.setY(result.y);
            target.setZ(result.z);
        }
        return result;
    }

    /**
     * Calculates the difference between two vectors.
     *
     * @param left
     *            the left argument vector from the operand.
     * @param right
     *            the right argument vector from the operand.
     * @return the difference vector
     */
    public static Vector3d sub(Vector3d left, Vector3d right) {
        return new Vector3d(left.x.subtract(right.x, mc), left.y.subtract(right.y, mc), left.z.subtract(right.z, mc));
    }

    public static BigDecimal manhattan(Vector3d left, Vector3d right) {
        BigDecimal dx = left.x.subtract(right.x, mc);
        BigDecimal dy = left.y.subtract(right.y, mc);
        BigDecimal dz = left.z.subtract(right.z, mc);
        return dx.abs().add(dy.abs()).add(dz.abs());
    }

    public static BigDecimal distSquare(Vector3d left, Vector3d right) {
        BigDecimal dx = left.x.subtract(right.x, mc);
        BigDecimal dy = left.y.subtract(right.y, mc);
        BigDecimal dz = left.z.subtract(right.z, mc);
        return dx.multiply(dx).add(dy.multiply(dy)).add(dz.multiply(dz));
    }

    /**
     * Calculates the cross product between two vectors.
     *
     * @param left
     *            the left argument vector from the operand.
     * @param right
     *            the right argument vector from the operand.
     * @return the cross product between two vectors
     */
    public static Vector3d cross(Vector3d left, Vector3d right) {
        BigDecimal cx = left.y.multiply(right.z).subtract(left.z.multiply(right.y));
        BigDecimal cy = left.z.multiply(right.x).subtract(left.x.multiply(right.z));
        BigDecimal cz = left.x.multiply(right.y).subtract(left.y.multiply(right.x));
        return new Vector3d(cx, cy, cz);
    }

    /**
     * @return calculates the length (Euklidian Norm) from the vector.
     */
    public BigDecimal length() {
        return MathHelper.sqrt(x.pow(2, mc).add(y.pow(2, mc), mc).add(z.pow(2, mc)));
    }

    /**
     * Calculates the angle between the two vectors in degree.
     *
     * @param vectorA
     *            a {@linkplain Vector3d}.
     * @param vectorB
     *            a {@linkplain Vector3d}.
     * @return the angle between the two vectors [Degree].
     */
    public static double angle(Vector3d vectorA1, Vector3d vectorB1) {
        return angleRad(vectorA1, vectorB1) * 180d / Math.PI;
    }

    /**
     * Calculates the angle between the two vectors in radians.
     *
     * @param vectorA
     *            a {@linkplain Vector3d}.
     * @param vectorB
     *            a {@linkplain Vector3d}.
     * @return the angle between the two vectors [rad].
     */
    static double angleRad(Vector3d vectorA1, Vector3d vectorB1) {
        Vector3d vectorA = new Vector3d();
        Vector3d vectorB = new Vector3d();
        vectorA1.normalise(vectorA);
        vectorB1.normalise(vectorB);
        double cosinus = vectorA.x.multiply(vectorB.x, mc).add(vectorA.y.multiply(vectorB.y, mc), mc).add(vectorA.z.multiply(vectorB.z, mc), mc)
                .divide(vectorA.length().multiply(vectorB.length(), mc), mc).doubleValue();
        return Math.acos(cosinus);
    }

    static double fastAngle(Vector3d vectorA1, Vector3d vectorB1) {
        Vector3d vectorA = new Vector3d();
        Vector3d vectorB = new Vector3d();
        vectorA1.normalise(vectorA);
        vectorB1.normalise(vectorB);
        double cosinus = vectorA.x.multiply(vectorB.x, mc).add(vectorA.y.multiply(vectorB.y, mc), mc).add(vectorA.z.multiply(vectorB.z, mc), mc).doubleValue();
        return Math.acos(cosinus) * 180d / Math.PI;
    }

    /**
     * Normalises this vector and stores the result in {@code normal1}
     *
     * @param normal1
     *            the target vector, which will store the result of the
     *            normalization.
     */
    public BigDecimal normalise(Vector3d normal1) {
        BigDecimal length = this.length();
        normal1.setX(this.x.divide(length, mc));
        normal1.setY(this.y.divide(length, mc));
        normal1.setZ(this.z.divide(length, mc));
        return length;
    }

    /**
     * Calculates the dot product between two vectors.
     *
     * @param vectorA
     *            a {@linkplain Vector3d}.
     * @param vectorB
     *            a {@linkplain Vector3d}.
     * @return the dot product of {@code vectorA} and {@code vectorB}.
     */
    static double dot(Vector3d vectorA, Vector3d vectorB) {
        return vectorA.x.multiply(vectorB.x, mc).add(vectorA.y.multiply(vectorB.y, mc), mc).add(vectorA.z.multiply(vectorB.z, mc), mc).doubleValue();
    }

    /**
     * Calculates the dot product between two vectors.
     *
     * @param vectorA
     *            a {@linkplain Vector3d}.
     * @param vectorB
     *            a {@linkplain Vector3d}.
     * @return the dot product of {@code vectorA} and {@code vectorB}.
     */
    public static BigDecimal dotP(Vector3d vectorA, Vector3d vectorB) {
        return vectorA.x.multiply(vectorB.x).add(vectorA.y.multiply(vectorB.y)).add(vectorA.z.multiply(vectorB.z));
    }

    public float getXf() {
        return this.x.floatValue();
    }

    public float getYf() {
        return this.y.floatValue();
    }

    public float getZf() {
        return this.z.floatValue();
    }

    /**
     * Calculates the sum of two vectors.
     *
     * @param left
     *            the left argument vector from the operand.
     * @param right
     *            the right argument vector from the operand.
     * @param target
     *            the target vector, which will store the result of the
     *            addition.
     * @return the vector sum
     */
    public static Vector3d add(Vector3d left, Vector3d right, Vector3d target) {
        Vector3d result = new Vector3d(left.x.add(right.x, mc), left.y.add(right.y, mc), left.z.add(right.z, mc));
        if (target != null) {
            target.setX(result.x);
            target.setY(result.y);
            target.setZ(result.z);
        }
        return result;
    }

    /**
     * Calculates the sum of two vectors.
     *
     * @param left
     *            the left argument vector from the operand.
     * @param right
     *            the right argument vector from the operand.
     * @return the vector sum
     */
    public static Vector3d add(Vector3d left, Vector3d right) {
        return new Vector3d(left.x.add(right.x, mc), left.y.add(right.y, mc), left.z.add(right.z, mc));
    }

    @Override
    public String toString() {
        return getXf() + " | " + getYf() + " | " + getZf(); //$NON-NLS-1$//$NON-NLS-2$
    }

    public void negate() {
        x = x.negate();
        y = y.negate();
        z = z.negate();
    }

    public static Vector3d getNormal(Vector3d v1, Vector3d v2, Vector3d v3) {
        //      g3.Y3.subtract(g3.Y1).multiply(g3.Z2.subtract(g3.Z1)).subtract(g3.Z3.subtract(g3.Z1).multiply(g3.Y2.subtract(g3.Y1))),
        //      g3.Z3.subtract(g3.Z1).multiply(g3.X2.subtract(g3.X1)).subtract(g3.X3.subtract(g3.X1).multiply(g3.Z2.subtract(g3.Z1))),
        //      g3.X3.subtract(g3.X1).multiply(g3.Y2.subtract(g3.Y1)).subtract(g3.Y3.subtract(g3.Y1).multiply(g3.X2.subtract(g3.X1)))
        BigDecimal x = v1.y.subtract(v2.y).multiply(v3.z.subtract(v2.z)).subtract(v1.z.subtract(v2.z).multiply(v3.y.subtract(v2.y)));
        BigDecimal y = v1.z.subtract(v2.z).multiply(v3.x.subtract(v2.x)).subtract(v1.x.subtract(v2.x).multiply(v3.z.subtract(v2.z)));
        BigDecimal z = v1.x.subtract(v2.x).multiply(v3.y.subtract(v2.y)).subtract(v1.y.subtract(v2.y).multiply(v3.x.subtract(v2.x)));
        return new Vector3d(x, y, z);
    }

    private static final BigDecimal HALF = new BigDecimal(".5"); //$NON-NLS-1$

    public Vector3d scaledByHalf() {
        return new Vector3d(x.multiply(HALF), y.multiply(HALF), z.multiply(HALF));
    }

    public Vector3d scale(BigDecimal scale) {
        return new Vector3d(x.multiply(scale), y.multiply(scale), z.multiply(scale));
    }

}
