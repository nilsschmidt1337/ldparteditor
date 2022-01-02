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
import java.math.RoundingMode;

import org.nschmidt.ldparteditor.data.Vertex;

public class Vector3dd extends Vector3d implements Comparable<Vector3dd> {

    public Vector3dd(Vertex tmp) {
        super(tmp);
    }
    public Vector3dd() {
        super();
    }

    public Vector3dd(Vector3d tmp) {
        super(tmp.x, tmp.y, tmp.z);
    }
    @Override
    public int hashCode() {
        return 1337;
    }

    private static final BigDecimal MIN_DIST = new BigDecimal(".0001"); //$NON-NLS-1$
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vector3dd)) {
            return false;
        }

        Vector3dd other = (Vector3dd) obj;
        return other == this || this.x.subtract(other.x).abs().compareTo(MIN_DIST) < 0 && this.y.subtract(other.y).abs().compareTo(MIN_DIST) < 0 && this.z.subtract(other.z).abs().compareTo(MIN_DIST) < 0;
    }

    @Override
    public String toString() {
        return x + " | " + y + " | " + z; //$NON-NLS-1$//$NON-NLS-2$
    }

    private static final BigDecimal EPSILON = new BigDecimal("0.000000001"); //$NON-NLS-1$
    public Vector3dd round() {
        this.x = x.round(new MathContext(7, RoundingMode.HALF_UP));
        this.y = y.round(new MathContext(7, RoundingMode.HALF_UP));
        this.z = z.round(new MathContext(7, RoundingMode.HALF_UP));
        if (this.x.abs().compareTo(EPSILON) <= 0) this.x = BigDecimal.ZERO;
        if (this.y.abs().compareTo(EPSILON) <= 0) this.y = BigDecimal.ZERO;
        if (this.z.abs().compareTo(EPSILON) <= 0) this.z = BigDecimal.ZERO;
        return this;
    }
    @Override
    public int compareTo(Vector3dd o) {
        {
            BigDecimal d1 = x.subtract(o.x);
            int c1 = d1.abs().compareTo(EPSILON);
            switch (c1) {
            case 0, 1:
                return d1.compareTo(BigDecimal.ZERO) < 0 ? -1 : 1;
            default:
                break;
            }
        }
        {
            BigDecimal d1 = y.subtract(o.y);
            int c1 = d1.abs().compareTo(EPSILON);
            switch (c1) {
            case 0, 1:
                return d1.compareTo(BigDecimal.ZERO) < 0 ? -1 : 1;
            default:
                break;
            }
        }
        {
            BigDecimal d1 = z.subtract(o.z);
            int c1 = d1.abs().compareTo(EPSILON);
            switch (c1) {
            case 0, 1:
                return d1.compareTo(BigDecimal.ZERO) < 0 ? -1 : 1;
            default:
                break;
            }
        }
        return 0;
    }
}