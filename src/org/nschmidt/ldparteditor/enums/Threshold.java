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
package org.nschmidt.ldparteditor.enums;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Contains important threshold variables
 *
 * @author nils
 *
 */
public enum Threshold implements Serializable {
    INSTANCE;

    /** V1.00 */
    private static final long serialVersionUID = 1L;

    /** The count of the significant numbers for BigDecimal */
    public static int significant_places = 32;
    /** The math context for precision */
    public static MathContext mc = new MathContext(32, RoundingMode.HALF_UP);// MathContext.DECIMAL128;
    /** The threshold for determinats of near singular matrices */
    public static double singularity_determinant = 0.000001d;
    /** The threshold for identical vertices and invisible condlines */
    public static BigDecimal identical_vertex_distance = new BigDecimal("0.0001", MathContext.DECIMAL128); //$NON-NLS-1$
    /** The threshold for the minimum collinear angle */
    public static double collinear_angle_minimum = 0.025d;
    /** The threshold for the maximum collinear angle */
    public static double collinear_angle_maximum = 179.9d;
    /** The threshold for the maximum coplanarity angle for warnings */
    public static double coplanarity_angle_warning = 1d;
    /** The threshold for the maximum coplanarity angle */
    public static double coplanarity_angle_error = 3d;
    /** The threshold for the maximum condline angle */
    public static float condline_angle_maximum = 179.9f;
}
