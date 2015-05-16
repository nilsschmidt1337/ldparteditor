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

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.lwjgl.util.vector.Matrix4f;

/**
 * Lightweight graphical data class
 *
 * @author nils
 *
 */
public abstract class GData implements IGData {

    abstract String getNiceString();

    @Override
    public String toString() {
        return getNiceString();
    }

    private GData next;
    private GData before;

    protected boolean visible = true;

    protected String text = null;
    static byte localWinding = BFC.NOCERTIFY;
    static int accumClip = 0;
    static boolean globalInvertNext = false;
    static boolean globalInvertNextFound = false;
    static boolean globalNegativeDeterminant = false;
    static boolean globalDrawObjects = true;
    static boolean globalFoundTEXMAPNEXT = false;

    static boolean globalFoundTEXMAPDEF = false;
    static Deque<GTexture> globalTextureStack = new ArrayDeque<GTexture>();
    static Deque<Boolean> globalFoundTEXMAPStack = new ArrayDeque<Boolean>();

    static TexMeta globalLastTextureType = null;

    // Cleared before viewport change
    public static final HashMap<GData1, Matrix4f> CACHE_viewByProjection = new HashMap<GData1, Matrix4f>(1000);

    // Cleared before parse
    public static final HashMap<String, ArrayList<String>> CACHE_parsedFilesSource = new HashMap<String, ArrayList<String>>(1000);
    public static final HashMap<String, GData> parsedLines = new HashMap<String, GData>(1000);

    public static final HashMap<GData, ArrayList<ParsingResult>> CACHE_warningsAndErrors = new HashMap<GData, ArrayList<ParsingResult>>(1000); // Cleared

    public GData getNext() {
        return next;
    }

    public GData getBefore() {
        return before;
    }

    public void derefer() {
        CACHE_warningsAndErrors.remove(this);
        next = null;
        before = null;
    }

    public String getText() {
        return text;
    }

    public void show() {
        visible = true;
    }

    public void hide() {
        visible = false;
    }

    public void setText(String text) {
        this.text = text;
    }

    private static final AtomicInteger id_counter = new AtomicInteger(0); // Integer.MIN_VALUE);
    protected final int ID;

    GData() {
        // NOTE: A possible overflow is irrelevant since equals() will return distinct results!!
        ID = id_counter.getAndIncrement();
    }

    // anchor is the next data to render
    public void setNext(GData next) {
        this.next = next;
        if (next != null) {
            next.before = this;
        }
    }

    @Override
    public int hashCode() {
        return ID;
    }


    /**
     * EVERY GData object is unique!
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

    protected String bigDecimalToString(BigDecimal bd) {
        String result;
        if (bd.compareTo(BigDecimal.ZERO) == 0)
            return "0"; //$NON-NLS-1$
        BigDecimal bd2 = bd.stripTrailingZeros();
        result = bd2.toPlainString();
        if (result.startsWith("-0."))return "-" + result.substring(2); //$NON-NLS-1$ //$NON-NLS-2$
        if (result.startsWith("0."))return result.substring(1); //$NON-NLS-1$
        return result;
    }

    /*
     * MARK Only for GC debugging!
     *
     * @Override protected void finalize() throws Throwable {
     * NLogger.debug(getClass(), "Disposing: " + this.toString()); //$NON-NLS-1$
     * super.finalize(); }
     */

}
