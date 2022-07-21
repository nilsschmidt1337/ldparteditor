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

import java.util.Map;

import org.nschmidt.ldparteditor.composite.Composite3D;
import org.nschmidt.ldparteditor.helper.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.helper.math.ThreadsafeSortedMap;

final class GDataInit extends GData {

    GDataInit(GData1 parent) {
        super(parent);
    }

    @Override
    public void drawGL20(Composite3D c3d) {
        GData.localWinding = BFC.NOCERTIFY;
        GData.accumClip = 0;
        GData.globalInvertNext = false;
        GData.globalInvertNextFound = false;
        GData.globalNegativeDeterminant = false;
    }

    @Override
    public void drawGL20RandomColours(Composite3D c3d) {
        drawGL20(c3d);
    }

    @Override
    public void drawGL20BFC(Composite3D c3d) {
        drawGL20(c3d);
    }

    @Override
    public void drawGL20BFCuncertified(Composite3D c3d) {
        drawGL20(c3d);
    }

    @Override
    public void drawGL20BFCbackOnly(Composite3D c3d) {
        drawGL20(c3d);
    }

    @Override
    public void drawGL20BFCcolour(Composite3D c3d) {
        drawGL20(c3d);
    }

    @Override
    public void drawGL20WhileAddCondlines(Composite3D c3d) {
        drawGL20(c3d);
    }

    @Override
    public void drawGL20CoplanarityHeatmap(Composite3D c3d) {
        drawGL20(c3d);
    }

    @Override
    public void drawGL20Wireframe(Composite3D c3d) {
        drawGL20(c3d);
    }

    @Override
    public void drawGL20BFCtextured(Composite3D c3d) {
        drawGL20(c3d);
        GData.globalDrawObjects = true;
        GData.globalFoundTEXMAPNEXT = false;
        GData.globalFoundTEXMAPStack.clear();
        GData.globalFoundTEXMAPStack.push(false);
        GData.globalTextureStack.clear();
    }

    @Override
    public int type() {
        return 7;
    }

    @Override
    String getNiceString() {
        return ""; //$NON-NLS-1$
    }

    @Override
    public String inlinedString(BFC bfc, GColour colour) {
        return ""; //$NON-NLS-1$
    }

    @Override
    public String transformAndColourReplace(String colour, Matrix matrix) {
        return ""; //$NON-NLS-1$
    }

    @Override
    public void getBFCorientationMap(Map<GData,BFC> map) {
        GData.localWinding = BFC.NOCERTIFY;
        GData.accumClip = 0;
        GData.globalInvertNext = false;
        GData.globalInvertNextFound = false;
        GData.globalNegativeDeterminant = false;
    }

    @Override
    public void getBFCorientationMapNOCERTIFY(Map<GData, BFC> map) {
        getBFCorientationMap(map);
    }

    @Override
    public void getBFCorientationMapNOCLIP(Map<GData, BFC> map) {
        getBFCorientationMap(map);
    }

    @Override
    public void getVertexNormalMap(GDataState state, ThreadsafeSortedMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {
        state.localWinding = BFC.NOCERTIFY;
        state.accumClip = 0;
        state.globalInvertNext = false;
        state.globalInvertNextFound = false;
        state.globalNegativeDeterminant = false;
        state.globalDrawObjects = true;
        state.globalFoundTEXMAPNEXT = false;
        state.globalFoundTEXMAPStack.clear();
        state.globalFoundTEXMAPStack.push(false);
        state.globalTextureStack.clear();
    }

    @Override
    public void getVertexNormalMapNOCERTIFY(GDataState state, ThreadsafeSortedMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {
        getVertexNormalMap(state, null, null, null);
    }

    @Override
    public void getVertexNormalMapNOCLIP(GDataState state, ThreadsafeSortedMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {
        getVertexNormalMap(state, null, null, null);
    }

    static void resetBfcState() {
        GData.localWinding = BFC.NOCERTIFY;
        GData.accumClip = 0;
        GData.globalInvertNext = false;
        GData.globalInvertNextFound = false;
        GData.globalNegativeDeterminant = false;
        GData.globalDrawObjects = true;
        GData.globalFoundTEXMAPNEXT = false;
        GData.globalFoundTEXMAPStack.clear();
        GData.globalFoundTEXMAPStack.push(false);
        GData.globalTextureStack.clear();
    }
}
