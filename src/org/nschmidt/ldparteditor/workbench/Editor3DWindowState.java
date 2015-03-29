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

import org.lwjgl.util.vector.Matrix4f;

/**
 * @author nils
 *
 */
public class Editor3DWindowState implements Serializable {
    // TODO New values, which were not included in the state before, have to be initialized! (@ WorkbenchManager.loadWorkbench())
    /** V1.00 */
    private static final long serialVersionUID = 1L;
    /** The state of the application window */
    private WindowState windowState;
    private int leftSashWidth = 170;
    private float primitiveZoom = (float) Math.pow(10.0d, 3f / 10 - 3);
    private float primitiveZoomExponent = 3f;
    private int[] leftSashWeights = null;
    private Matrix4f[] primitiveViewport = null;

    /**
     * @return The state of the application window
     */
    public WindowState getWindowState() {
        return windowState;
    }

    /**
     * @param windowState
     *            Sets the window state
     */
    public void setWindowState(WindowState windowState) {
        this.windowState = windowState;
    }

    public int getLeftSashWidth() {
        return leftSashWidth;
    }

    public void setLeftSashWidth(int leftSashWidth) {
        this.leftSashWidth = leftSashWidth;
    }

    public float getPrimitiveZoom() {
        return primitiveZoom;
    }

    public void setPrimitiveZoom(float primitiveZoom) {
        this.primitiveZoom = primitiveZoom;
    }

    public float getPrimitiveZoomExponent() {
        return primitiveZoomExponent;
    }

    public void setPrimitiveZoomExponent(float primitiveZoomExponent) {
        this.primitiveZoomExponent = primitiveZoomExponent;
    }

    public int[] getLeftSashWeights() {
        return leftSashWeights;
    }

    public void setLeftSashWeights(int[] leftSashWeights) {
        this.leftSashWeights = leftSashWeights;
    }

    public Matrix4f[] getPrimitiveViewport() {
        return primitiveViewport;
    }

    public void setPrimitiveViewport(Matrix4f[] primitiveViewport) {
        this.primitiveViewport = primitiveViewport;
    }

}
