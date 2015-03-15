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
public final class GDataBfcP extends GDataP {

    final byte type;

    public byte getType() {
        return type;
    }

    public GDataBfcP(byte type) {
        this.type = type;
    }

    @Override
    public int type() {
        return 6;
    }

    @Override
    public void drawBFCprimitive() {
        switch (type) {
        case BFC.CCW:
            GData.localWinding = BFC.CCW;
            break;
        case BFC.CCW_CLIP:
            GData.localWinding = BFC.CCW;
            break;
        case BFC.CW:
            GData.localWinding = BFC.CW;
            break;
        case BFC.CW_CLIP:
            GData.localWinding = BFC.CW;
            break;
        case BFC.INVERTNEXT:
            GData.globalInvertNext = !GData.globalInvertNext;
            GData.globalInvertNextFound = true;
            break;
        case BFC.NOCERTIFY:
            GData.localWinding = BFC.NOCERTIFY;
            break;
        case BFC.NOCLIP:
            if (GData.accumClip == 0)
                GData.accumClip = 1;
            break;
        default:
            break;
        }
    }
}
