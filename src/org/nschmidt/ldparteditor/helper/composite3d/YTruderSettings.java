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

/**
 * @author nils
 *
 */
public class YTruderSettings {
    public static final int MODE_TRANSLATE_BY_DISTANCE = 1;
    public static final int MODE_SYMMETRY_ACROSS_PLANE = 2;
    public static final int MODE_PROJECTION_ON_PLANE = 3;
    public static final int MODE_EXTRUDE_RADIALLY = 4;
    private int axis = 1;
    private int mode = 1;
    private double condlineAngleThreshold = 30.0;
    private double distance = 0.0;
    public int getAxis() {
        return axis;
    }
    public void setAxis(int axis) {
        this.axis = axis;
    }
    public int getMode() {
        return mode;
    }
    public void setMode(int mode) {
        this.mode = mode;
    }
    public double getCondlineAngleThreshold() {
        return condlineAngleThreshold;
    }
    public void setCondlineAngleThreshold(double condlineAngleThreshold) {
        this.condlineAngleThreshold = condlineAngleThreshold;
    }
    public double getDistance() {
        return distance;
    }
    public void setDistance(double distance) {
        this.distance = distance;
    }
}
