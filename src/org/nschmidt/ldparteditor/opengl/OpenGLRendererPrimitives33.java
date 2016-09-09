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
package org.nschmidt.ldparteditor.opengl;

import org.eclipse.swt.opengl.GLCanvas;
import org.lwjgl.opengl.GL;
import org.nschmidt.ldparteditor.composites.primitive.CompositePrimitive;

public class OpenGLRendererPrimitives33 extends OpenGLRendererPrimitives {

    /** The Primitive Composite */
    private final CompositePrimitive cp;
    
    public OpenGLRendererPrimitives33(CompositePrimitive compositePrimitive) {
        this.cp = compositePrimitive;
    }

    @Override
    public void drawScene(float mouseX, float mouseY) {
        final GLCanvas canvas = cp.getCanvas();

        if (!canvas.isCurrent()) {
            canvas.setCurrent();
            GL.setCapabilities(cp.getCapabilities());
        }
        
        canvas.swapBuffers();
    }

    @Override
    public void init() {
        
    }

}
