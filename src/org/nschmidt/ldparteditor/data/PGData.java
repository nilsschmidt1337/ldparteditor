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
import java.util.concurrent.atomic.AtomicInteger;

import org.nschmidt.ldparteditor.opengl.GLMatrixStack;

/**
 * Lightweight graphical data class
 */
public abstract class PGData implements Serializable {
    // Do not rename fields. It will break backwards compatibility!

    private static final long serialVersionUID = 1L;

    private transient PGData next;

    static BFC localWinding = BFC.NOCERTIFY;
    static int accumClip = 0;
    static boolean globalInvertNext = false;
    static boolean globalInvertNextFound = false;
    static boolean globalNegativeDeterminant = false;

    public abstract void drawBFCprimitiveGL20(int drawOnlyMode);
    public abstract void drawBFCprimitiveGL33(GLMatrixStack stack, int drawOnlyMode);
    public abstract int type();
    public abstract PGData data();

    public PGData getNext() {
        return next;
    }

    private static final AtomicInteger id_counter = new AtomicInteger(0);
    private final transient int id;

    PGData() {
        // NOTE: A possible overflow is irrelevant since equals() will return distinct results!!
        id = id_counter.getAndIncrement();
    }

    // anchor is the next data to render
    public void setNext(PGData next) {
        this.next = next;
    }

    @Override
    public int hashCode() {
        return id;
    }

    /**
     * EVERY PGData object is unique!
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        return this == obj;
    }
}
