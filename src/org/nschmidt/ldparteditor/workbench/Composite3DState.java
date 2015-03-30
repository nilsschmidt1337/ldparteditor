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
package org.nschmidt.ldparteditor.workbench;

import java.io.Serializable;

import org.nschmidt.ldparteditor.enums.Perspective;

/**
 * @author nils
 *
 */
public class Composite3DState implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean scales = false;
    private Perspective perspective = Perspective.TWO_THIRDS;
    private int renderMode = 0;


    private boolean sash = false;
    private boolean vertical = false;
    private int[] weights = null;

    public Composite3DState() {

    }

    public Composite3DState(boolean vertical) {
        this.sash = true;
        this.vertical = vertical;
    }

    public boolean hasScales() {
        return scales;
    }

    public void setScales(boolean scales) {
        this.scales = scales;
    }

    public boolean isSash() {
        return sash;
    }

    public void setSash(boolean sash) {
        this.sash = sash;
    }

    public boolean isVertical() {
        return vertical;
    }

    public void setVertical(boolean vertical) {
        this.vertical = vertical;
    }

    public Perspective getPerspective() {
        return perspective;
    }

    public void setPerspective(Perspective perspective) {
        this.perspective = perspective;
    }

    public int[] getWeights() {
        return weights;
    }

    public void setWeights(int[] weights) {
        this.weights = weights;
    }

    public int getRenderMode() {
        return renderMode;
    }

    public void setRenderMode(int renderMode) {
        this.renderMode = renderMode;
    }

}
