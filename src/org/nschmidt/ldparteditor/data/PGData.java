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

/**
 * Lightweight graphical data class
 *
 * @author nils
 *
 */
public abstract class PGData implements IPGData, Serializable {

    private static final long serialVersionUID = 1L;

    private transient PGData next;

    static byte localWinding = BFC.NOCERTIFY;
    static int accumClip = 0;
    static boolean globalInvertNext = false;
    static boolean globalInvertNextFound = false;
    static boolean globalNegativeDeterminant = false;

    public PGData getNext() {
        return next;
    }

    private static final AtomicInteger id_counter = new AtomicInteger(0); // Integer.MIN_VALUE);
    private final transient int ID;

    PGData() {
        // NOTE: A possible overflow is irrelevant since equals() will return distinct results!!
        ID = id_counter.getAndIncrement();
    }

    // anchor is the next data to render
    public void setNext(PGData next) {
        this.next = next;
    }

    @Override
    public int hashCode() {
        return ID;
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

    public static int getLastID() {
        return id_counter.get();
    }
}
