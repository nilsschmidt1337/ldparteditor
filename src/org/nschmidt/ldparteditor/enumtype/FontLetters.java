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

import java.util.Set;

import org.nschmidt.ldparteditor.data.PGData3;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.text.TextTriangulator;

public enum FontLetters {
    INSTANCE;

    public static final Set<PGData3> FRONT = TextTriangulator.triangulateGLText(Font.MONOSPACE_1, I18n.PERSPECTIVE_FRONT, 0.07, .012f * (1f + IconSize.getIconsize() / 4f));
    public static final Set<PGData3> BACK = TextTriangulator.triangulateGLText(Font.MONOSPACE_1, I18n.PERSPECTIVE_BACK, 0.07, .012f * (1f + IconSize.getIconsize() / 4f));
    public static final Set<PGData3> BOTTOM = TextTriangulator.triangulateGLText(Font.MONOSPACE_1, I18n.PERSPECTIVE_BOTTOM, 0.07, .012f * (1f + IconSize.getIconsize() / 4f));
    public static final Set<PGData3> LEFT = TextTriangulator.triangulateGLText(Font.MONOSPACE_1, I18n.PERSPECTIVE_LEFT, 0.07, .012f * (1f + IconSize.getIconsize() / 4f));
    public static final Set<PGData3> RIGHT = TextTriangulator.triangulateGLText(Font.MONOSPACE_1, I18n.PERSPECTIVE_RIGHT, 0.07, .012f * (1f + IconSize.getIconsize() / 4f));
    public static final Set<PGData3> TOP = TextTriangulator.triangulateGLText(Font.MONOSPACE_1, I18n.PERSPECTIVE_TOP, 0.07, .012f * (1f + IconSize.getIconsize() / 4f));

    public static final Set<PGData3> S = TextTriangulator.triangulateGLText(Font.MONOSPACE_1, "*", 0.07); //$NON-NLS-1$
    public static final Set<PGData3> D0 = TextTriangulator.triangulateGLText(Font.MONOSPACE_1, "0", 0.07); //$NON-NLS-1$
    public static final Set<PGData3> D1 = TextTriangulator.triangulateGLText(Font.MONOSPACE_1, "1", 0.07); //$NON-NLS-1$
    public static final Set<PGData3> D2 = TextTriangulator.triangulateGLText(Font.MONOSPACE_1, "2", 0.07); //$NON-NLS-1$
    public static final Set<PGData3> D3 = TextTriangulator.triangulateGLText(Font.MONOSPACE_1, "3", 0.07); //$NON-NLS-1$
    public static final Set<PGData3> D4 = TextTriangulator.triangulateGLText(Font.MONOSPACE_1, "4", 0.07); //$NON-NLS-1$
    public static final Set<PGData3> D5 = TextTriangulator.triangulateGLText(Font.MONOSPACE_1, "5", 0.07); //$NON-NLS-1$
    public static final Set<PGData3> D6 = TextTriangulator.triangulateGLText(Font.MONOSPACE_1, "6", 0.07); //$NON-NLS-1$
    public static final Set<PGData3> D7 = TextTriangulator.triangulateGLText(Font.MONOSPACE_1, "7", 0.07); //$NON-NLS-1$
    public static final Set<PGData3> D8 = TextTriangulator.triangulateGLText(Font.MONOSPACE_1, "8", 0.07); //$NON-NLS-1$
    public static final Set<PGData3> D9 = TextTriangulator.triangulateGLText(Font.MONOSPACE_1, "9", 0.07); //$NON-NLS-1$
    public static final Set<PGData3> DDot = TextTriangulator.triangulateGLText(Font.MONOSPACE_1, ".", 0.07); //$NON-NLS-1$
    public static final Set<PGData3> DComma = TextTriangulator.triangulateGLText(Font.MONOSPACE_1, ",", 0.07); //$NON-NLS-1$
    public static final Set<PGData3> DDegree = TextTriangulator.triangulateGLText(Font.MONOSPACE_1, "°", 0.07); //$NON-NLS-1$
    public static final Set<PGData3> DX = TextTriangulator.triangulateGLText(Font.MONOSPACE_1, "dX =", 0.07); //$NON-NLS-1$
    public static final Set<PGData3> DY = TextTriangulator.triangulateGLText(Font.MONOSPACE_1, "dY =", 0.07); //$NON-NLS-1$
    public static final Set<PGData3> DZ = TextTriangulator.triangulateGLText(Font.MONOSPACE_1, "dZ =", 0.07); //$NON-NLS-1$
    public static final Set<PGData3> DA = TextTriangulator.triangulateGLText(Font.MONOSPACE_1, "D  =", 0.07); //$NON-NLS-1$
    public static final Set<PGData3> Dmm = TextTriangulator.triangulateGLText(Font.MONOSPACE_1, "Dmm=", 0.07); //$NON-NLS-1$
    public static final Set<PGData3> Dst = TextTriangulator.triangulateGLText(Font.MONOSPACE_1, "Dst=", 0.07); //$NON-NLS-1$
    public static final Set<PGData3> DM = TextTriangulator.triangulateGLText(Font.MONOSPACE_1, "-", 0.07); //$NON-NLS-1$
    public static final Set<PGData3> X = TextTriangulator.triangulateGLText(Font.MONOSPACE_1, "x", 0.07); //$NON-NLS-1$
    public static final Set<PGData3> Y = TextTriangulator.triangulateGLText(Font.MONOSPACE_1, "y", 0.07); //$NON-NLS-1$
    public static final Set<PGData3> Z = TextTriangulator.triangulateGLText(Font.MONOSPACE_1, "z", 0.07); //$NON-NLS-1$
}
