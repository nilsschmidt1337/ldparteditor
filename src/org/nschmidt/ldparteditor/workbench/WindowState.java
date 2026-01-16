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

/**
 * This class represents the state of a window in general
 *
 */
public class WindowState implements Serializable {
    // Do not rename fields. It will break backwards compatibility!

    /** V1.00 */
    private static final long serialVersionUID = 1L;

    /**
     * The x position of the upper left corner from the application
     * window (in pixels)
     */
    private int x;
    /**
     * The y position of the upper left corner from the application
     * window (in pixels)
     */
    private int y;
    /**
     * The width from the application window (in pixels)
     */
    private int width;
    /**
     * The height from the application window (in pixels)
     */
    private int height;

    /** The indicator which is set to true when the window is maximized */
    private boolean maximized;
    /** Centered when open */
    private boolean centered;

    /**
     * @return The size and the current position of the window
     */
    public int getX() {
        return x;
    }

    /**
     * Sets the new window position of the window (x axis)
     *
     * @param sizeAndPosition
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * @return The size and the current position of the window
     */
    public int getY() {
        return y;
    }

    /**
     * Sets the new window position of the window (y axis)
     *
     * @param sizeAndPosition
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * @return The width of the window
     */
    public int getWidth() {
        return width;
    }

    /**
     * Sets the new width of the window
     *
     * @param width
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @return The height of the window
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the new height of the window
     *
     * @param height
     */
    public void setHeight(int height) {
        this.height = height;
    }


    /**
     * @return true if the window is maximized
     */
    public boolean isMaximized() {
        return maximized;
    }

    /**
     * @param maximized
     *            Set this true if the window is maximized
     */
    public void setMaximized(boolean maximized) {
        this.maximized = maximized;
    }

    /**
     * @return true if the window is centered
     */
    public boolean isCentered() {
        return centered;
    }

    /**
     * @param centered
     *            Set this true if the window is centered
     */
    public void setCentered(boolean centered) {
        this.centered = centered;
    }
}