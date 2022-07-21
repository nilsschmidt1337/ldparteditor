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
package org.nschmidt.ldparteditor.helper.composite3d;

import java.util.concurrent.atomic.AtomicBoolean;

import org.lwjgl.opengl.swt.GLCanvas;
import org.nschmidt.ldparteditor.composite.Composite3D;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;

/**
 * Manages the idle state of {@linkplain Composite3D}
 */
public enum ViewIdleManager {
    INSTANCE;

    public static final GLCanvas[] firstCanvas = new GLCanvas[1];
    public static final OpenGLRenderer[] firstRender = new OpenGLRenderer[1];

    public static final AtomicBoolean[] pause = new AtomicBoolean[] { new AtomicBoolean(false) };
    public static final AtomicBoolean[] renderLDrawStandard = new AtomicBoolean[] { new AtomicBoolean(false) };

    /**
     * Refreshes the {@linkplain Composite3D}
     */
    public static void refresh(GLCanvas canvas, OpenGLRenderer renderer) {
        firstCanvas[0] = canvas;
        firstRender[0] = renderer;
    }

}
