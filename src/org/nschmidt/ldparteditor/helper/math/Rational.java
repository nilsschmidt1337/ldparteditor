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
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Objects;

/**
 * Full immutable rational representation with lower and upper part
 */
public class Rational implements Comparable<Rational> {

    public static final Rational ZERO = new Rational();
    public static final Rational ONE = new Rational(BigInteger.ONE, BigInteger.ONE);
    private final BigInteger upper;
    private final BigInteger lower;

    Rational(BigInteger upper, BigInteger lower) {
        this.upper = upper;
        this.lower = lower;
    }

    public Rational() {
        this.upper = BigInteger.ZERO;
        this.lower = BigInteger.ONE;
    }

    public Rational(int x) {
        this(new BigDecimal(x));
    }

    public Rational(BigDecimal x) {
        final int scale = x.scale();
        if (scale > 0) {
            this.upper = x.unscaledValue();
            this.lower = BigInteger.TEN.pow(scale);
        } else {
            this.upper = x.unscaledValue().multiply(BigInteger.TEN.pow(-scale));
            this.lower = BigInteger.ONE;
        }

    }

    Rational divide(BigInteger divisor) {
        return new Rational(upper, lower.multiply(divisor));
    }

    double doubleValue() {
        return new BigDecimal(upper).divide(new BigDecimal(lower), MathContext.DECIMAL128).doubleValue();
    }

    BigDecimal bigDecimalValue(MathContext mcloc) {
        return new BigDecimal(upper).divide(new BigDecimal(lower), mcloc);
    }

    private static BigInteger sd = BigInteger.valueOf(1000000000000000L);
    private static BigDecimal sd2 = new BigDecimal(1000000000000000L);

    public BigDecimal bigDecimalValue() {
        return new BigDecimal(upper.multiply(sd).divide(lower)).divide(sd2);
    }

    public Rational add(Rational tmp) {
        return new Rational(upper.multiply(tmp.lower).add(tmp.upper.multiply(lower)), lower.multiply(tmp.lower));
    }

    public boolean isZero() {
        return upper.compareTo(BigInteger.ZERO) == 0;
    }

    public Rational abs() {
        return new Rational(upper.abs(), lower.abs());
    }

    public Rational divide(Rational x) {
        return new Rational(this.upper.multiply(x.lower), this.lower.multiply(x.upper));
    }

    public Rational multiply(Rational x) {
        return new Rational(this.upper.multiply(x.upper), this.lower.multiply(x.lower));
    }

    @Override
    public int compareTo(Rational other) {
        final int s = upper.signum() * lower.signum();
        final int os = other.upper.signum() * other.lower.signum();
        if (s != os) {
            return Integer.compare(s, os);
        }

        switch (s) {
        case 0:
            return 0;
        case 1:
            return upper.abs().multiply(other.lower.abs()).compareTo(other.upper.abs().multiply(lower.abs()));
        default:
            return other.upper.abs().multiply(lower.abs()).compareTo(upper.abs().multiply(other.lower.abs()));
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(lower, upper);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Rational))
            return false;
        Rational other = (Rational) obj;
        return this.compareTo(other) == 0;
    }

    public Rational subtract(Rational x) {
        return new Rational(upper.multiply(x.lower).subtract(x.upper.multiply(lower)), lower.multiply(x.lower));
    }

    Rational negate() {
        return new Rational(upper.negate(), lower);
    }

    @Override
    public String toString() {
        return Double.toString(this.doubleValue());
    }
}
