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
package org.nschmidt.ldparteditor.helper.composite3d;

import java.math.BigDecimal;

/**
 * @author nils
 *
 */
public class SymSplitterSettings {

    private BigDecimal offset = BigDecimal.ZERO;
    private BigDecimal precision = BigDecimal.ZERO;
    private int splitPlane = 0;
    private int hideLevel = 0;
    private boolean colourise = false;
    private boolean validate = false;
    private boolean cutAcross = false;
    private int scope = 0;
    public static final int Z_PLUS = 0;
    public static final int Y_PLUS = 1;
    public static final int X_PLUS = 2;
    public static final int Z_MINUS = 3;
    public static final int Y_MINUS = 4;
    public static final int X_MINUS = 5;
    public BigDecimal getOffset() {
        return offset;
    }
    public void setOffset(BigDecimal offset) {
        this.offset = offset;
    }
    public int getScope() {
        return scope;
    }
    public void setScope(int scope) {
        this.scope = scope;
    }
    public int getSplitPlane() {
        return splitPlane;
    }
    public void setSplitPlane(int plane) {
        this.splitPlane = plane;
    }
    public boolean isColourise() {
        return colourise;
    }
    public void setColourise(boolean colourise) {
        this.colourise = colourise;
    }
    public int getHideLevel() {
        return hideLevel;
    }
    public void setHideLevel(int hideLevel) {
        this.hideLevel = hideLevel;
    }
    public boolean isValidate() {
        return validate;
    }
    public void setValidate(boolean validate) {
        this.validate = validate;
    }
    public boolean isCutAcross() {
        return cutAcross;
    }
    public void setCutAcross(boolean cutAcross) {
        this.cutAcross = cutAcross;
    }
    public BigDecimal getPrecision() {
        return precision;
    }
    public void setPrecision(BigDecimal precision) {
        this.precision = precision;
    }
}
