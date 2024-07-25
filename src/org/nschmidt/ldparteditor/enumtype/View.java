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
package org.nschmidt.ldparteditor.enumtype;

import java.io.File;
import java.math.BigDecimal;

import org.lwjgl.util.vector.Matrix4f;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.DatType;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.Matrix;
import org.nschmidt.ldparteditor.i18n.I18n;

/**
 * This class provides constants for the 3D view
 */
@java.lang.SuppressWarnings({"java:S1104", "java:S1444", "java:S2386"})
public enum View {
    INSTANCE;

    /** Resolution of the viewport at 100% zoom */
    public static final float PIXEL_PER_LDU = 1000.0f;
    /** i18n reference string for the current unit */
    public static String unit = "LDU"; //$NON-NLS-1$
    /** The current unit factor */
    public static BigDecimal unitFactor = BigDecimal.ONE;
    /** threshold for "solid" edges (default: 5e-6f) */
    public static float edgeThreshold = 5e-6f;
    /** The standard decimal format for floating point numbers (0 digits) */
    public static final String NUMBER_FORMAT0F = "###,##0;-###,##0"; //$NON-NLS-1$
    /** The standard decimal format for floating point numbers (1 digit) */
    public static final String NUMBER_FORMAT1F = "###,##0.0;-###,##0.0"; //$NON-NLS-1$
    /** The standard decimal format for floating point numbers (2 digits) */
    public static final String NUMBER_FORMAT2F = " ###,##0.00;-###,##0.00"; //$NON-NLS-1$
    /** The standard decimal format for floating point numbers (4 digits) */
    public static final String NUMBER_FORMAT4F = "###,##0.0000;-###,##0.0000"; //$NON-NLS-1$
    /** The standard decimal format for floating point numbers (8 digits) */
    public static final String NUMBER_FORMAT8F = "###,##0.00000000;-###,##0.00000000"; //$NON-NLS-1$
    /** The decimal format for floating point numbers with four leading zeros (4 digits) */
    public static final String NUMBER_FORMATL4F = " ###,##0000.0000;-###,##0000.0000"; //$NON-NLS-1$

    public static float lineWidth1000 = 100f;
    public static float lineWidth = 0.100f;
    public static float lineWidthGL = 1.5f;

    public static final int NUM_CORES = Runtime.getRuntime().availableProcessors();

    public static final Matrix4f ID = Matrix4f.setIdentity(new Matrix4f());
    public static final GData1 DUMMY_REFERENCE = new GData1();
    public static final DatFile DUMMY_DATFILE = new DatFile(File.separator + File.separator + I18n.E3D_NO_FILE_SELECTED, "DUMMY FILE", true, DatType.PART); //$NON-NLS-1$

    public static final Matrix ACCURATE_ID = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE);
}
