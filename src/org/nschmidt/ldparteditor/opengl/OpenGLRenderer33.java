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

import org.nschmidt.ldparteditor.composites.Composite3D;

public class OpenGLRenderer33 extends OpenGLRenderer {

    /** The 3D Composite */
    private final Composite3D c3d;
    
    public OpenGLRenderer33(Composite3D c3d) {
        this.c3d = c3d;
    }
    
    
    @Override
    public Composite3D getC3D() {
        return c3d;
    }

    @Override
    public void disposeAllTextures() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void disposeOldTextures() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void init() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void drawScene() {
        // TODO Auto-generated method stub
        
    }
}
