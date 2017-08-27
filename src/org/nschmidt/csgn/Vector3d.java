/**
 * Vector3d.java
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
package org.nschmidt.csgn;

public class Vector3d implements Comparable<Vector3d> {

    private static double epsilon = 0.0001d;

    public double x;
    public double y;
    public double z;

    public static final Vector3d ZERO = new Vector3d(0, 0, 0);
    public static final Vector3d UNITY = new Vector3d(1, 1, 1);
    public static final Vector3d X_ONE = new Vector3d(1, 0, 0);
    public static final Vector3d Y_ONE = new Vector3d(0, 1, 0);
    public static final Vector3d Z_ONE = new Vector3d(0, 0, 1);

    /**
     * Creates a new vector.
     *
     * @param x
     *            x value
     * @param y
     *            y value
     * @param z
     *            z value
     */
    public Vector3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public Vector3d clone() {
        return new Vector3d(x, y, z);
    }

    /**
     * Returns a negated copy of this vector.
     *
     * <b>Note:</b> this vector is not modified.
     *
     * @return a negated copy of this vector
     */
    public Vector3d negated() {
        return new Vector3d(-x, -y, -z);
    }

    /**
     * Returns the sum of this vector and the specified vector.
     *
     * @param v
     *            the vector to add
     *
     *            <b>Note:</b> this vector is not modified.
     *
     * @return the sum of this vector and the specified vector
     */
    public Vector3d plus(Vector3d v) {
        return new Vector3d(x + v.x, y + v.y, z + v.z);
    }

    /**
     * Returns the difference of this vector and the specified vector.
     *
     * @param v
     *            the vector to subtract
     *
     *            <b>Note:</b> this vector is not modified.
     *
     * @return the difference of this vector and the specified vector
     */
    public Vector3d minus(Vector3d v) {
        return new Vector3d(x - v.x, y - v.y, z - v.z);
    }

    /**
     * Returns the product of this vector and the specified value.
     *
     * @param a
     *            the value
     *
     *            <b>Note:</b> this vector is not modified.
     *
     * @return the product of this vector and the specified value
     */
    public Vector3d times(double a) {
        return new Vector3d(x * a, y * a, z * a);
    }

    /**
     * Returns the product of this vector and the specified vector.
     *
     * @param a
     *            the vector
     *
     *            <b>Note:</b> this vector is not modified.
     *
     * @return the product of this vector and the specified vector
     */
    public Vector3d times(Vector3d a) {
        return new Vector3d(x * a.x, y * a.y, z * a.z);
    }

    /**
     * Returns this vector devided by the specified value.
     *
     * @param a
     *            the value
     *
     *            <b>Note:</b> this vector is not modified.
     *
     * @return this vector devided by the specified value
     */
    public Vector3d dividedBy(double a) {
        return new Vector3d(x / a, y / a, z / a);
    }

    /**
     * Returns the dot product of this vector and the specified vector.
     *
     * <b>Note:</b> this vector is not modified.
     *
     * @param a
     *            the second vector
     *
     * @return the dot product of this vector and the specified vector
     */
    public double dot(Vector3d a) {
        return this.x * a.x + this.y * a.y + this.z * a.z;
    }

    /**
     * Linearly interpolates between this and the specified vector.
     *
     * <b>Note:</b> this vector is not modified.
     *
     * @param a
     *            vector
     * @param t
     *            interpolation value
     *
     * @return copy of this vector if {@code t = 0}; copy of a if {@code t = 1};
     *         the point midway between this and the specified vector if
     *         {@code t = 0.5}
     */
    public Vector3d interpolate(Vector3d a, double t) {
        return this.plus(a.minus(this).times(t));
    }

    /**
     * Returns the magnitude of this vector.
     *
     * <b>Note:</b> this vector is not modified.
     *
     * @return the magnitude of this vector
     */
    public double magnitude() {
        return Math.sqrt(this.dot(this));
    }

    /**
     * Returns a normalized copy of this vector with {@code length}.
     *
     * <b>Note:</b> this vector is not modified.
     *
     * @return a normalized copy of this vector with {@code length}
     */
    public Vector3d unit() {
        return this.dividedBy(this.magnitude());
    }

    /**
     * Returns the cross product of this vector and the specified vector.
     *
     * <b>Note:</b> this vector is not modified.
     *
     * @param a
     *            the vector
     *
     * @return the cross product of this vector and the specified vector.
     */
    public Vector3d cross(Vector3d a) {
        return new Vector3d(this.y * a.z - this.z * a.y, this.z * a.x - this.x * a.z, this.x * a.y - this.y * a.x);
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + z + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Vector3d other = (Vector3d) obj;
        if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x)) {
            return false;
        }
        if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y)) {
            return false;
        }
        if (Double.doubleToLongBits(this.z) != Double.doubleToLongBits(other.z)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.x) ^ Double.doubleToLongBits(this.x) >>> 32);
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.y) ^ Double.doubleToLongBits(this.y) >>> 32);
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.z) ^ Double.doubleToLongBits(this.z) >>> 32);
        return hash;
    }

    @Override
    public int compareTo(Vector3d o) {
        {
            double d1 = x - o.x;
            int c1 = Double.compare(Math.abs(d1), epsilon);
            switch (c1) {
            case 0:
            case 1:
                return d1 < 0f ? -1 : 1;
            default:
                break;
            }
        }
        {
            double d1 = y - o.y;
            int c1 = Double.compare(Math.abs(d1), epsilon);
            switch (c1) {
            case 0:
            case 1:
                return d1 < 0f ? -1 : 1;
            default:
                break;
            }
        }
        {
            double d1 = z - o.z;
            int c1 = Double.compare(Math.abs(d1), epsilon);
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
