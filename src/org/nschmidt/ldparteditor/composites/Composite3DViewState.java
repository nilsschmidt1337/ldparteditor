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
package org.nschmidt.ldparteditor.composites;

import org.lwjgl.util.vector.Vector4f;

public class Composite3DViewState {

    private int renderMode = 0;
    private float zoom = 0f;
    private float zoom_exponent = 0f;
    private Vector4f offset = new Vector4f(0, 0, 0, 1f);

    int getRenderMode() {
        return renderMode;
    }

    void setRenderMode(int renderMode) {
        this.renderMode = renderMode;
    }

    float getZoom() {
        return zoom;
    }

    void setZoom(float zoom) {
        this.zoom = zoom;
    }

    float getZoom_exponent() {
        return zoom_exponent;
    }

    void setZoom_exponent(float zoom_exponent) {
        this.zoom_exponent = zoom_exponent;
    }

    public Vector4f getOffset() {
        return offset;
    }

    public void setOffset(Vector4f offset) {
        this.offset = offset;
    }
}
