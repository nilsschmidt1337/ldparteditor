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
package org.nschmidt.ldparteditor.data;

/**
 * @author nils
 *
 */
public enum BFC {
    INSTANCE;

    public static final byte INVERTNEXT = 0;
    public static final byte CCW = 1;
    public static final byte CW = 2;
    public static final byte CLIP = 3;
    public static final byte NOCLIP = 4;
    public static final byte CCW_CLIP = 5;
    public static final byte CW_CLIP = 6;
    public static final byte NOCERTIFY = 7;
    public static final byte UNKNOWN = 8;
    public static final byte PLACEHOLDER = 127; // Constant for empty primitive lines (cache)

    private static boolean invertNext = false;

    public static boolean isInvertNext() {
        return invertNext;
    }

    public static void setInvertNext(boolean invertNext) {
        BFC.invertNext = invertNext;
    }
}
