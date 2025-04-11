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
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.lwjgl.util.vector.Matrix4f;
import org.nschmidt.ldparteditor.composite.Composite3D;
import org.nschmidt.ldparteditor.helper.math.MathHelper;
import org.nschmidt.ldparteditor.helper.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.helper.math.ThreadsafeSortedMap;

/**
 * Lightweight graphical data class
 */
public abstract class GData implements Comparable<GData> {

    abstract String getNiceString();

    @Override
    public String toString() {
        return getNiceString();
    }

    protected GData next;
    protected GData before;
    public final GData1 parent;

    protected boolean visible = true;

    protected String text = null;
    static BFC localWinding = BFC.NOCERTIFY;
    static int accumClip = 0;
    static boolean globalInvertNext = false;
    static boolean globalInvertNextFound = false;
    static boolean globalNegativeDeterminant = false;
    static boolean globalDrawObjects = true;
    static boolean globalFoundTEXMAPNEXT = false;

    static Deque<GTexture> globalTextureStack = new ArrayDeque<>();
    static Deque<Boolean> globalFoundTEXMAPStack = new ArrayDeque<>();

    // Cleared before viewport change
    public static final Map<GData1, Matrix4f> CACHE_viewByProjection = new HashMap<>(1000);

    // Cleared before parse
    public static final Map<String, List<String>> CACHE_parsedFilesSource = new HashMap<>(1000);
    public static final Map<String, GData> parsedLines = new HashMap<>(1000);

    public static final Map<GData, List<ParsingResult>> CACHE_warningsAndErrors = new HashMap<>(1000); // Cleared
    public static final ThreadsafeHashMap<GData, ParsingResult> CACHE_duplicates = new ThreadsafeHashMap<>(1000); // Cleared

    public abstract void drawGL20(Composite3D c3d);

    public abstract void drawGL20RandomColours(Composite3D c3d);

    public abstract void drawGL20BFC(Composite3D c3d);

    public abstract void drawGL20BFCuncertified(Composite3D c3d);

    public abstract void drawGL20BFCbackOnly(Composite3D c3d);

    public abstract void drawGL20BFCcolour(Composite3D c3d);

    public abstract void drawGL20BFCtextured(Composite3D c3d);

    public abstract void drawGL20WhileAddCondlines(Composite3D c3d);

    public abstract void drawGL20CoplanarityHeatmap(Composite3D c3d);

    public abstract void drawGL20ConvexityHeatmap(Composite3D c3d);

    public abstract void drawGL20Wireframe(Composite3D c3d);

    public abstract void getBFCorientationMap(Map<GData,BFC> map);
    public abstract void getBFCorientationMapNOCERTIFY(Map<GData, BFC> map);
    public abstract void getBFCorientationMapNOCLIP(Map<GData, BFC> map);

    public abstract void getVertexNormalMap(GDataState state, ThreadsafeSortedMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm);
    public abstract void getVertexNormalMapNOCERTIFY(GDataState state, ThreadsafeSortedMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm);
    public abstract void getVertexNormalMapNOCLIP(GDataState state, ThreadsafeSortedMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm);

    public abstract String inlinedString(BFC bfcStatusTarget, GColour colour);

    public abstract String transformAndColourReplace(String colour, Matrix matrix);

    public abstract int type();

    public GData getNext() {
        return next;
    }

    public GData getBefore() {
        return before;
    }

    void derefer() {
        CACHE_warningsAndErrors.remove(this);
        next = null;
        before = null;
    }

    void show() {
        visible = true;
    }

    void hide() {
        visible = false;
    }

    public void setText(String text) {
        this.text = text;
    }

    private static final AtomicInteger id_counter = new AtomicInteger(0);
    protected final int id;

    GData(GData1 parent) {
        // NOTE: A possible overflow is irrelevant since equals() will return distinct results!!
        id = id_counter.getAndIncrement();
        this.parent = parent;
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
        return id;
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

    @Override
    public int compareTo(GData o) {
        if (equals(o)) {
            return 0;
        }
        return Integer.compare(id, o.id);
    }

    static int getLastID() {
        return id_counter.get();
    }

    protected String bigDecimalToString(BigDecimal bd) {
        return MathHelper.roundBigDecimalToString(bd);
    }

    protected String bigDecimalToStringRoundAlways(BigDecimal bd) {
        return MathHelper.roundBigDecimalToStringAlways(bd);
    }

    public boolean isVisible() {
        return visible;
    }
}
