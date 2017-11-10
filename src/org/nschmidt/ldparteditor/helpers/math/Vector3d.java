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
import java.math.MathContext;

import org.nschmidt.csg.VectorCSGd;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.enums.Threshold;

/**
 * Holds a 3-tuple BigDecimal vector for maximum numeric precision.
 *
 * @author nils Note: It is not intended to write something like
 *         {@code vectorA.X = vectorB.X;} use {@code vectorA.setX(...)} instead.
 */
public class Vector3d {

    /** A thread safe static reference to the math context constant */
    private static MathContext mc = Threshold.mc;
    /** The x component of the vector. */
    public BigDecimal X;
    /** The y component of the vector. */
    public BigDecimal Y;
    /** The z component of the vector. */
    public BigDecimal Z;

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
        this.X = x;
        this.Y = y;
        this.Z = z;
    }

    public Vector3d(Vector3d vertex) {
        this.X = vertex.X;
        this.Y = vertex.Y;
        this.Z = vertex.Z;
    }

    public Vector3d(Vertex vertex) {
        this.X = vertex.X;
        this.Y = vertex.Y;
        this.Z = vertex.Z;
        // this.X = new BigDecimal(vertex.x / 1000f, mc);
        // this.Y = new BigDecimal(vertex.y / 1000f, mc);
        // this.Z = new BigDecimal(vertex.z / 1000f, mc);
    }

    public Vector3d(Vector3r vertex) {
        this(vertex.X.BigDecimalValue(), vertex.Y.BigDecimalValue(), vertex.Z.BigDecimalValue());
    }

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
        this.X = x;
    }

    /**
     * Sets the y component of the vector.
     *
     * @param y
     *            the component value to set
     */
    public void setY(BigDecimal y) {
        this.Y = y;
    }

    /**
     * Sets the z component of the vector.
     *
     * @param z
     *            the component value to set
     */
    public void setZ(BigDecimal z) {
        this.Z = z;
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
        Vector3d result = new Vector3d(left.X.subtract(right.X, mc), left.Y.subtract(right.Y, mc), left.Z.subtract(right.Z, mc));
        if (target != null) {
            target.setX(result.X);
            target.setY(result.Y);
            target.setZ(result.Z);
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
        return new Vector3d(left.X.subtract(right.X, mc), left.Y.subtract(right.Y, mc), left.Z.subtract(right.Z, mc));
    }

    public static BigDecimal manhattan(Vector3d left, Vector3d right) {
        BigDecimal dx = left.X.subtract(right.X, mc);
        BigDecimal dy = left.Y.subtract(right.Y, mc);
        BigDecimal dz = left.Z.subtract(right.Z, mc);
        return dx.abs().add(dy.abs()).add(dz.abs());
    }

    public static BigDecimal distSquare(Vector3d left, Vector3d right) {
        BigDecimal dx = left.X.subtract(right.X, mc);
        BigDecimal dy = left.Y.subtract(right.Y, mc);
        BigDecimal dz = left.Z.subtract(right.Z, mc);
        return dx.multiply(dx).add(dy.multiply(dy)).add(dz.multiply(dz));
    }

    public static Vector3d absSub(Vector3d left, Vector3d right) {
        Vector3d s = sub(left, right);
        return new Vector3d(s.X.compareTo(BigDecimal.ZERO) < 0 ? s.X.negate() : s.X, s.Y.compareTo(BigDecimal.ZERO) < 0 ? s.Y.negate() : s.Y, s.Z.compareTo(BigDecimal.ZERO) < 0 ? s.Z.negate() : s.Z);
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
        BigDecimal x = left.Y.multiply(right.Z).subtract(left.Z.multiply(right.Y));
        BigDecimal y = left.Z.multiply(right.X).subtract(left.X.multiply(right.Z));
        BigDecimal z = left.X.multiply(right.Y).subtract(left.Y.multiply(right.X));
        return new Vector3d(x, y, z);
    }

    /**
     * @return calculates the length (Euklidian Norm) from the vector.
     */
    public BigDecimal length() {
        return MathHelper.sqrt(X.pow(2, mc).add(Y.pow(2, mc), mc).add(Z.pow(2, mc)));
    }

    /**
     * @return calculates the 2-Norm from the vector.
     */
    public BigDecimal norm2() {
        return X.pow(2, mc).add(Y.pow(2, mc), mc).add(Z.pow(2, mc));
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
        Vector3d vectorA = new Vector3d();
        Vector3d vectorB = new Vector3d();
        vectorA1.normalise(vectorA);
        vectorB1.normalise(vectorB);
        double cosinus = vectorA.X.multiply(vectorB.X, mc).add(vectorA.Y.multiply(vectorB.Y, mc), mc).add(vectorA.Z.multiply(vectorB.Z, mc), mc)
                .divide(vectorA.length().multiply(vectorB.length(), mc), mc).doubleValue();
        return Math.acos(cosinus) * 180d / Math.PI;
    }

    public static double fastAngle(Vector3d vectorA1, Vector3d vectorB1) {
        Vector3d vectorA = new Vector3d();
        Vector3d vectorB = new Vector3d();
        vectorA1.normalise(vectorA);
        vectorB1.normalise(vectorB);
        double cosinus = vectorA.X.multiply(vectorB.X, mc).add(vectorA.Y.multiply(vectorB.Y, mc), mc).add(vectorA.Z.multiply(vectorB.Z, mc), mc).doubleValue();
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
        normal1.setX(this.X.divide(length, mc));
        normal1.setY(this.Y.divide(length, mc));
        normal1.setZ(this.Z.divide(length, mc));
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
    public static double dot(Vector3d vectorA, Vector3d vectorB) {
        return vectorA.X.multiply(vectorB.X, mc).add(vectorA.Y.multiply(vectorB.Y, mc), mc).add(vectorA.Z.multiply(vectorB.Z, mc), mc).doubleValue();
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
        return vectorA.X.multiply(vectorB.X).add(vectorA.Y.multiply(vectorB.Y)).add(vectorA.Z.multiply(vectorB.Z));
    }

    public float getXf() {
        return this.X.floatValue();
    }

    public float getYf() {
        return this.Y.floatValue();
    }

    public float getZf() {
        return this.Z.floatValue();
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
        Vector3d result = new Vector3d(left.X.add(right.X, mc), left.Y.add(right.Y, mc), left.Z.add(right.Z, mc));
        if (target != null) {
            target.setX(result.X);
            target.setY(result.Y);
            target.setZ(result.Z);
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
        return new Vector3d(left.X.add(right.X, mc), left.Y.add(right.Y, mc), left.Z.add(right.Z, mc));
    }

    @Override
    public String toString() {
        return getXf() + " | " + getYf() + " | " + getZf(); //$NON-NLS-1$//$NON-NLS-2$
    }

    public void negate() {
        X = X.negate();
        Y = Y.negate();
        Z = Z.negate();
    }

    public static Vector3d getNormal(Vector3d v1, Vector3d v2, Vector3d v3) {
        //      g3.Y3.subtract(g3.Y1).multiply(g3.Z2.subtract(g3.Z1)).subtract(g3.Z3.subtract(g3.Z1).multiply(g3.Y2.subtract(g3.Y1))),
        //      g3.Z3.subtract(g3.Z1).multiply(g3.X2.subtract(g3.X1)).subtract(g3.X3.subtract(g3.X1).multiply(g3.Z2.subtract(g3.Z1))),
        //      g3.X3.subtract(g3.X1).multiply(g3.Y2.subtract(g3.Y1)).subtract(g3.Y3.subtract(g3.Y1).multiply(g3.X2.subtract(g3.X1)))
        BigDecimal X = v1.Y.subtract(v2.Y).multiply(v3.Z.subtract(v2.Z)).subtract(v1.Z.subtract(v2.Z).multiply(v3.Y.subtract(v2.Y)));
        BigDecimal Y = v1.Z.subtract(v2.Z).multiply(v3.X.subtract(v2.X)).subtract(v1.X.subtract(v2.X).multiply(v3.Z.subtract(v2.Z)));
        BigDecimal Z = v1.X.subtract(v2.X).multiply(v3.Y.subtract(v2.Y)).subtract(v1.Y.subtract(v2.Y).multiply(v3.X.subtract(v2.X)));
        return new Vector3d(X, Y, Z);
    }

    private static BigDecimal HALF = new BigDecimal(".5"); //$NON-NLS-1$
    public Vector3d scaledByHalf() {
        return new Vector3d(X.multiply(HALF), Y.multiply(HALF), Z.multiply(HALF));
    }

    public Vector3d scale(BigDecimal scale) {
        return new Vector3d(X.multiply(scale), Y.multiply(scale), Z.multiply(scale));
    }

}
