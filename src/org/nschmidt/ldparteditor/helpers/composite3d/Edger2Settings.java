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
public class Edger2Settings {

    private BigDecimal equalDistance = new BigDecimal("0.0010"); //$NON-NLS-1$
    private BigDecimal af = new BigDecimal("0.1"); //$NON-NLS-1$
    private BigDecimal ac = new BigDecimal("60"); //$NON-NLS-1$
    private BigDecimal ae = new BigDecimal("60"); //$NON-NLS-1$
    private boolean extendedRange = false;
    private int scope = 0;
    private int unmatchedMode = 0;
    public BigDecimal getEqualDistance() {
        return equalDistance;
    }
    public void setEqualDistance(BigDecimal equalDistance) {
        this.equalDistance = equalDistance;
    }
    public BigDecimal getAf() {
        return af;
    }
    public void setAf(BigDecimal af) {
        this.af = af;
    }
    public BigDecimal getAc() {
        return ac;
    }
    public void setAc(BigDecimal ac) {
        this.ac = ac;
    }
    public BigDecimal getAe() {
        return ae;
    }
    public void setAe(BigDecimal ae) {
        this.ae = ae;
    }
    public boolean isExtendedRange() {
        return extendedRange;
    }
    public void setExtendedRange(boolean extendedRange) {
        this.extendedRange = extendedRange;
    }
    public int getScope() {
        return scope;
    }
    public void setScope(int scope) {
        this.scope = scope;
    }
    public int getUnmatchedMode() {
        return unmatchedMode;
    }
    public void setUnmatchedMode(int unmatchedMode) {
        this.unmatchedMode = unmatchedMode;
    }
}
