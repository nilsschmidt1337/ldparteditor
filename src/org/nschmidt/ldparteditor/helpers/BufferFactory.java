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
package org.nschmidt.ldparteditor.helpers;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

/**
 * @author nils
 *
 */
public enum BufferFactory {
    INSTANCE;

    public static FloatBuffer floatBuffer(float[] array) {
        FloatBuffer fb = BufferUtils.createFloatBuffer(array.length);
        fb.put(array);
        fb.flip();
        return fb;
    }

    /*
     * No need for this at the moment public static ShortBuffer
     * shortBuffer(short[] array) { ShortBuffer sb =
     * BufferUtils.createShortBuffer(array.length); sb.put(array); sb.flip();
     * return sb; }
     *
     * public static IntBuffer intBuffer(int[] array) { IntBuffer ib =
     * BufferUtils.createIntBuffer(array.length); ib.put(array); ib.flip();
     * return ib; }
     */

}
