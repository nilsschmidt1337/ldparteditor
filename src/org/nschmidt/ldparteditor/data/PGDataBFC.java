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

import java.io.Serializable;

import org.nschmidt.ldparteditor.opengl.GLMatrixStack;

public final class PGDataBFC extends PGData implements Serializable {

    private static final long serialVersionUID = 1L;

    private final BFC type;

    public BFC getType() {
        return type;
    }

    public PGDataBFC(BFC type) {
        this.type = type;
    }

    @Override
    public int type() {
        return 6;
    }

    @Override
    public void drawBFCprimitiveGL20(int drawOnlyMode) {
        drawBFCprimitiveGL33(null, drawOnlyMode);
    }

    @Override
    public void drawBFCprimitiveGL33(GLMatrixStack stack, int drawOnlyMode) {
        switch (type) {
        case CCW:
            PGData.localWinding = BFC.CCW;
            break;
        case CCW_CLIP:
            PGData.localWinding = BFC.CCW;
            break;
        case CW:
            PGData.localWinding = BFC.CW;
            break;
        case CW_CLIP:
            PGData.localWinding = BFC.CW;
            break;
        case INVERTNEXT:
            PGData.globalInvertNext = !PGData.globalInvertNext;
            PGData.globalInvertNextFound = true;
            break;
        case NOCERTIFY:
            PGData.localWinding = BFC.NOCERTIFY;
            break;
        case NOCLIP:
            if (PGData.accumClip == 0)
                PGData.accumClip = 1;
            break;
        default:
            break;
        }
    }

    static PGDataBFC clone(PGDataBFC o) {
        return new PGDataBFC(o.type);
    }

    @Override
    public PGData data() {
        return this;
    }
}
