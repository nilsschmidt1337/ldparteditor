/*
 * Copyright (c) 2016 Martin Davis, modified by Nils Schmidt (removed not required methods)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * The DoubleDouble class included in this package was modified from the
 * JTS 1.13 release.  While the changes were submitted to (and accepted by)
 * the project, they were not yet part of the standard distribution
 * when this package was created.  Source code for JTS can be accessed at
 * the JTS Topology Suite at https://github.com/locationtech/jts .
 * The package name user here is slightly different to avoid name-space
 * conflicts in applications that use the standard JTS API.
 */
package org.tinfour.constrained.delaunay;

import java.io.Serializable;

/**
 * Implements extended-precision floating-point numbers which maintain 106 bits
 * (approximately 30 decimal digits) of precision.
 * <p>
 * A DoubleDouble uses a representation containing two double-precision values.
 * A number x is represented as a pair of doubles, x.hi and x.lo, such that the
 * number represented by x is x.hi + x.lo, where
 * 
 * <pre>
 *    |x.lo| &lt;= 0.5*ulp(x.hi)
 * </pre>
 * 
 * and ulp(y) means "unit in the last place of y". The basic arithmetic
 * operations are implemented using convenient properties of IEEE-754
 * floating-point arithmetic.
 * <p>
 * The range of values which can be represented is the same as in IEEE-754. The
 * precision of the representable numbers is twice as great as IEEE-754 double
 * precision.
 * <p>
 * The correctness of the arithmetic algorithms relies on operations being
 * performed with standard IEEE-754 double precision and rounding. This is the
 * Java standard arithmetic model, but for performance reasons Java
 * implementations are not constrained to using this standard by default. Some
 * processors (notably the Intel Pentium architecure) perform floating point
 * operations in (non-IEEE-754-standard) extended-precision. A JVM
 * implementation may choose to use the non-standard extended-precision as its
 * default arithmetic mode. To prevent this from happening, this code uses the
 * Java {@code strictfp} modifier, which forces all operations to take place in
 * the standard IEEE-754 rounding model.
 * <p>
 * The API provides both a set of value-oriented operations and a set of
 * mutating operations. Value-oriented operations treat DoubleDouble values as
 * immutable; operations on them return new objects carrying the result of the
 * operation. This provides a simple and safe semantics for writing DoubleDouble
 * expressions. However, there is a performance penalty for the object
 * allocations required. The mutable interface updates object values in-place.
 * It provides optimum memory performance, but requires care to ensure that
 * aliasing errors are not created and constant values are not changed.
 * <p>
 * For example, the following code example constructs three DoubleDouble instances: two to
 * hold the input values and one to hold the result of the addition.
 * 
 * <pre>
 * DoubleDouble a = new DoubleDouble(2.0);
 * DoubleDouble b = new DoubleDouble(3.0);
 * DoubleDouble c = a.add(b);
 * </pre>
 * 
 * In contrast, the following approach uses only one object:
 * 
 * <pre>
 * DoubleDouble a = new DoubleDouble(2.0);
 * a.selfAdd(3.0);
 * </pre>
 * <p>
 * This implementation uses algorithms originally designed variously by Knuth,
 * Kahan, Dekker, and Linnainmaa. Douglas Priest developed the first C
 * implementation of these techniques. Other more recent C++ implementation are
 * due to Keith M. Briggs and David Bailey et al.
 *
 * <h3>References</h3>
 * <ul>
 * <li>Priest, D., <i>Algorithms for Arbitrary Precision Floating Point
 * Arithmetic</i>, in P. Kornerup and D. Matula, Eds., Proc. 10th Symposium on
 * Computer Arithmetic, IEEE Computer Society Press, Los Alamitos, Calif., 1991.
 * <li>Yozo Hida, Xiaoye S. Li and David H. Bailey, <i>Quad-Double Arithmetic:
 * Algorithms, Implementation, and Application</i>, manuscript, Oct 2000;
 * Lawrence Berkeley National Laboratory Report BNL-46996.
 * <li>David Bailey, <i>High Precision Software Directory</i>;
 * {@code http://crd.lbl.gov/~dhbailey/mpdist/index.html}
 * </ul>
 *
 *
 * @author Martin Davis
 *
 */
final class DoubleDouble implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The value to split a double-precision value on during multiplication
     */
    private static final double SPLIT = 134217729.0D; // 2^27+1, for IEEE double

    /**
     * The high-order component of the double-double precision value.
     */
    private double hi = 0.0;

    /**
     * The low-order component of the double-double precision value.
     */
    private double lo = 0.0;

    /**
     * Creates a new DoubleDouble with value 0.0.
     */
    DoubleDouble() {
        this.hi = 0.0;
        this.lo = 0.0;
    }

    /**
     * Set the value for the DoubleDouble object. This method supports the mutating operations
     * concept described in the class documentation (see above).
     * 
     * @param value a DoubleDouble instance supplying an extended-precision value.
     * @return a self-reference to the DoubleDouble instance.
     */
    final DoubleDouble setValue(DoubleDouble value) {
        hi = value.hi;
        lo = value.lo;
        return this;
    }

    /**
     * Set the value for the DoubleDouble object. This method supports the mutating operations
     * concept described in the class documentation (see above).
     * 
     * @param value a floating point value to be stored in the instance.
     * @return a self-reference to the DoubleDouble instance.
     */
    final DoubleDouble setValue(double value) {
        this.hi = value;
        this.lo = 0.0;
        return this;
    }

    /**
     * Adds the argument to the value of {@code this}. To prevent altering
     * constants, this method <b>must only</b> be used on values known to be newly
     * created.
     *
     * @param y the addend
     * @return this object, increased by y
     */
    final DoubleDouble selfAdd(DoubleDouble y) {
        return selfAdd(y.hi, y.lo);
    }

    /**
     * Subtracts the argument from the value of {@code this}. To prevent altering
     * constants, this method <b>must only</b> be used on values known to be newly
     * created.
     *
     * @param y the addend
     * @return this object, decreased by y
     */
    final DoubleDouble selfSubtract(DoubleDouble y) {
        if (isNaN())
            return this;
        return selfAdd(-y.hi, -y.lo);
    }

    /**
     * Subtracts the argument from the value of {@code this}. To prevent altering
     * constants, this method <b>must only</b> be used on values known to be newly
     * created.
     *
     * @param y the addend
     * @return this object, decreased by y
     */
    final DoubleDouble selfSubtract(double y) {
        if (isNaN())
            return this;
        return selfAdd(-y, 0.0);
    }

    /**
     * Multiplies this object by the argument, returning {@code this}. To prevent
     * altering constants, this method <b>must only</b> be used on values known to
     * be newly created.
     *
     * @param y the value to multiply by
     * @return this object, multiplied by y
     */
    final DoubleDouble selfMultiply(DoubleDouble y) {
        return selfMultiply(y.hi, y.lo);
    }

    private final DoubleDouble selfMultiply(double yhi, double ylo) {
        double hx;
        double tx;
        double hy;
        double ty;
        double cHi;
        double c;
        cHi = SPLIT * hi;
        hx = cHi - hi;
        c = SPLIT * yhi;
        hx = cHi - hx;
        tx = hi - hx;
        hy = c - yhi;
        cHi = hi * yhi;
        hy = c - hy;
        ty = yhi - hy;
        c = ((((hx * hy - cHi) + hx * ty) + tx * hy) + tx * ty) + (hi * ylo + lo * yhi);
        double zhi = cHi + c;
        hx = cHi - zhi;
        double zlo = c + hx;
        hi = zhi;
        lo = zlo;
        return this;
    }

    /**
     * Converts this value to the nearest double-precision number.
     *
     * @return the nearest double-precision number to this value
     */
    double doubleValue() {
        return hi + lo;
    }
    
    private final DoubleDouble selfAdd(double yhi, double ylo) {
        double hHi;
        double h;
        double tHi;
        double t;
        double sHi;
        double s;
        double e;
        double f;
        sHi = hi + yhi;
        tHi = lo + ylo;
        e = sHi - hi;
        f = tHi - lo;
        s = sHi - e;
        t = tHi - f;
        s = (yhi - e) + (hi - s);
        t = (ylo - f) + (lo - t);
        e = s + tHi;
        hHi = sHi + e;
        h = e + (sHi - hHi);
        e = t + h;

        double zhi = hHi + e;
        double zlo = e + (hHi - zhi);
        hi = zhi;
        lo = zlo;
        return this;
    }

    private boolean isNaN() {
        return Double.isNaN(hi);
    }
}