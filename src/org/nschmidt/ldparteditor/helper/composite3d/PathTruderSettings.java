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
public class PathTruderSettings {

    private BigDecimal maxPathSegmentLength = new BigDecimal(100000);
    private BigDecimal transitionCurveControl = BigDecimal.TEN;
    private BigDecimal transitionCurveCenter = new BigDecimal("0.5"); //$NON-NLS-1$
    private BigDecimal pathAngleForLine = new BigDecimal(180);
    private BigDecimal rotation = BigDecimal.ZERO;
    private boolean inverted = false;
    private int transitionCount = 1;
    private boolean compensation = false;
    public BigDecimal getMaxPathSegmentLength() {
        return maxPathSegmentLength;
    }
    public void setMaxPathSegmentLength(BigDecimal length) {
        this.maxPathSegmentLength = length;
    }
    public int getTransitionCount() {
        return transitionCount;
    }
    public void setTransitionCount(int transitionCount) {
        this.transitionCount = transitionCount;
    }
    public BigDecimal getTransitionCurveControl() {
        return transitionCurveControl;
    }
    public void setTransitionCurveControl(BigDecimal transitionCurveControl) {
        this.transitionCurveControl = transitionCurveControl;
    }
    public BigDecimal getTransitionCurveCenter() {
        return transitionCurveCenter;
    }
    public void setTransitionCurveCenter(BigDecimal transitionCurveCenter) {
        this.transitionCurveCenter = transitionCurveCenter;
    }
    public BigDecimal getPathAngleForLine() {
        return pathAngleForLine;
    }
    public void setPathAngleForLine(BigDecimal pathAngleForLine) {
        this.pathAngleForLine = pathAngleForLine;
    }
    public BigDecimal getRotation() {
        return rotation;
    }
    public void setRotation(BigDecimal rotation) {
        this.rotation = rotation;
    }
    public boolean isCompensation() {
        return compensation;
    }
    public void setCompensation(boolean compensation) {
        this.compensation = compensation;
    }
    public boolean isInverted() {
        return inverted;
    }
    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }
}
