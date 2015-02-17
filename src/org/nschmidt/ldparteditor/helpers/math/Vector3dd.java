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
import java.math.RoundingMode;

import org.nschmidt.ldparteditor.data.Vertex;

public class Vector3dd extends Vector3d {

    public Vector3dd(Vertex tmp) {
        super(tmp);
    }
    public Vector3dd() {
        super();
    }

    public Vector3dd(Vector3d tmp) {
        super(tmp.X, tmp.Y, tmp.Z);
    }
    @Override
    public int hashCode() {
        return 1337;
    }

    private static final BigDecimal MIN_DIST = new BigDecimal(".0001"); //$NON-NLS-1$
    @Override
    public boolean equals(Object obj) {
        Vector3dd other = (Vector3dd) obj;
        return other == this || this.X.subtract(other.X).abs().compareTo(MIN_DIST) < 0 && this.Y.subtract(other.Y).abs().compareTo(MIN_DIST) < 0 && this.Z.subtract(other.Z).abs().compareTo(MIN_DIST) < 0;
    }

    @Override
    public String toString() {
        return X + " | " + Y + " | " + Z; //$NON-NLS-1$//$NON-NLS-2$
    }

    private static final BigDecimal EPSILON = new BigDecimal("0.000000001"); //$NON-NLS-1$
    public Vector3dd round() {
        this.X = X.round(new MathContext(7, RoundingMode.HALF_UP));
        this.Y = Y.round(new MathContext(7, RoundingMode.HALF_UP));
        this.Z = Z.round(new MathContext(7, RoundingMode.HALF_UP));
        if (this.X.abs().compareTo(EPSILON) <= 0) this.X = BigDecimal.ZERO;
        if (this.Y.abs().compareTo(EPSILON) <= 0) this.Y = BigDecimal.ZERO;
        if (this.Z.abs().compareTo(EPSILON) <= 0) this.Z = BigDecimal.ZERO;
        return this;
    }
}