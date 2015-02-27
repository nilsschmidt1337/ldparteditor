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
package org.nschmidt.ldparteditor.helpers.composite3d;

import java.math.BigDecimal;

/**
 * @author nils
 *
 */
public class UnificatorSettings {

    private BigDecimal offset = BigDecimal.ZERO;
    private BigDecimal precision = BigDecimal.ZERO;
    private int splitPlane = 0;
    private int scope = 0;
    public BigDecimal getVertexThreshold() {
        return offset;
    }
    public void setVertexThreshold(BigDecimal offset) {
        this.offset = offset;
    }
    public int getScope() {
        return scope;
    }
    public void setScope(int scope) {
        this.scope = scope;
    }
    public int getSnapOn() {
        return splitPlane;
    }
    public void setSnapOn(int plane) {
        this.splitPlane = plane;
    }
    public BigDecimal getSubvertexThreshold() {
        return precision;
    }
    public void setSubvertexThreshold(BigDecimal precision) {
        this.precision = precision;
    }
}
