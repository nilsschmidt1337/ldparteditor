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

import org.lwjgl.util.vector.Matrix4f;

public class GLMatrixStack {
    
    public static Matrix4f glOrtho(double l, double r, double b, double t, double n, double f) {
        Matrix4f result = new Matrix4f();
        result.m00 = (float) (2 / (r - l));
        result.m03 = (float) (- (r + l) / (r - l)); 
        result.m11 = (float) (2 / (t - b));
        result.m13 = (float) (- (t + b) / (t - b));
        result.m22 = (float) (- 2 / (f - n));  
        result.m23 = (float) (- (f + n) / (f - n));
        result.m33 = 1f; 
        return result;
    }
}
