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


/**
 * @author nils
 *
 */
public final class PGDataProxy extends PGData implements Serializable {

    private static final long serialVersionUID = 1L;

    private transient PGData data;
    private transient boolean initialised = false;

    public PGDataProxy(PGData proxy) {
        data = proxy;
    }

    @Override
    public void drawBFCprimitiveGL20(int drawOnlyMode) {
        if (initialised) {
            data.drawBFCprimitiveGL20(drawOnlyMode);
        } else {
            initialised = true;
            switch (data.type()) {
            case 2:
                data = PGData2.clone((PGData2) data);
                break;
            case 3:
                data = PGData3.clone((PGData3) data);
                break;
            case 4:
                data = PGData4.clone((PGData4) data);
                break;
            case 5:
                data = PGData5.clone((PGData5) data);
                break;
            case 6:
                data = PGDataBFC.clone((PGDataBFC) data);
                break;
            default:
                initialised = false;
            }
            if (initialised) {
                data.drawBFCprimitiveGL20(drawOnlyMode);
            }
        }
    }

    @Override
    public void drawBFCprimitiveGL33(GLMatrixStack stack, int drawOnlyMode) {
        if (initialised) {
            data.drawBFCprimitiveGL33(stack, drawOnlyMode);
        } else {
            initialised = true;
            switch (data.type()) {
            case 2:
                data = PGData2.clone((PGData2) data);
                break;
            case 3:
                data = PGData3.clone((PGData3) data);
                break;
            case 4:
                data = PGData4.clone((PGData4) data);
                break;
            case 5:
                data = PGData5.clone((PGData5) data);
                break;
            case 6:
                data = PGDataBFC.clone((PGDataBFC) data);
                break;
            default:
                initialised = false;
            }
            if (initialised) {
                data.drawBFCprimitiveGL33(stack, drawOnlyMode);
            }
        }
    }

    @Override
    public int type() {
        return data.type();
    }

    @Override
    public PGData data() {
        return data;
    }
}
