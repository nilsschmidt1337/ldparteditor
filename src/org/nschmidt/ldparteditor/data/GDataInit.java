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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeTreeMap;
import org.nschmidt.ldparteditor.opengl.GLMatrixStack;

/**
 * @author nils
 *
 */
public final class GDataInit extends GData {

    private final GData1 parent;

    public GDataInit(GData1 parent) {
        this.parent = parent;
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
    public void drawGL20_RandomColours(Composite3D c3d) {
        drawGL20(c3d);
    }

    @Override
    public void drawGL20_BFC(Composite3D c3d) {
        drawGL20(c3d);
    }

    @Override
    public void drawGL20_BFCuncertified(Composite3D c3d) {
        drawGL20(c3d);
    }

    @Override
    public void drawGL20_BFC_backOnly(Composite3D c3d) {
        drawGL20(c3d);
    }

    @Override
    public void drawGL20_BFC_Colour(Composite3D c3d) {
        drawGL20(c3d);
    }

    @Override
    public void drawGL20_WhileAddCondlines(Composite3D c3d) {
        drawGL20(c3d);
    }

    @Override
    public void drawGL20_BFC_Textured(Composite3D c3d) {
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
    public String inlinedString(byte bfc, GColour colour) {
        return ""; //$NON-NLS-1$
    }

    @Override
    public String transformAndColourReplace(String colour, Matrix matrix) {
        return ""; //$NON-NLS-1$
    }

    @Override
    public void getBFCorientationMap(HashMap<GData, Byte> map) {
        GData.localWinding = BFC.NOCERTIFY;
        GData.accumClip = 0;
        GData.globalInvertNext = false;
        GData.globalInvertNextFound = false;
        GData.globalNegativeDeterminant = false;
    }
    @Override
    public void getBFCorientationMapNOCERTIFY(HashMap<GData, Byte> map) {
        getBFCorientationMap(map);
    }
    @Override
    public void getBFCorientationMapNOCLIP(HashMap<GData, Byte> map) {
        getBFCorientationMap(map);
    }
    @Override
    public void getVertexNormalMap(GDataState state, ThreadsafeTreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {
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
    public void getVertexNormalMapNOCERTIFY(GDataState state, ThreadsafeTreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {
        getVertexNormalMap(state, null, null, null);
    }
    @Override
    public void getVertexNormalMapNOCLIP(GDataState state, ThreadsafeTreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {
        getVertexNormalMap(state, null, null, null);
    }

    public static void resetBfcState() {
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

    public GData1 getParent() {
        return parent;
    }

    @Override
    public void drawGL33(Composite3D c3d, GLMatrixStack stack, boolean drawSolidMaterials, Set<Integer> sourceVAO, Set<Integer> targetVAO, Set<Integer> sourceBUF, Set<Integer> targetBUF, Set<String> sourceID, Set<String> targetID, Map<String, Integer[]> mapGLO) {
       drawGL20(null);
    }

    @Override
    public void drawGL33_RandomColours(Composite3D c3d, GLMatrixStack stack, boolean drawSolidMaterials, Set<Integer> sourceVAO, Set<Integer> targetVAO, Set<Integer> sourceBUF, Set<Integer> targetBUF, Set<String> sourceID, Set<String> targetID, Map<String, Integer[]> mapGLO) {
        drawGL20(c3d);
    }

    @Override
    public void drawGL33_BFC(Composite3D c3d, GLMatrixStack stack, boolean drawSolidMaterials, Set<Integer> sourceVAO, Set<Integer> targetVAO, Set<Integer> sourceBUF, Set<Integer> targetBUF, Set<String> sourceID, Set<String> targetID, Map<String, Integer[]> mapGLO) {
        drawGL20(c3d);
    }

    @Override
    public void drawGL33_BFCuncertified(Composite3D c3d, GLMatrixStack stack, boolean drawSolidMaterials, Set<Integer> sourceVAO, Set<Integer> targetVAO, Set<Integer> sourceBUF, Set<Integer> targetBUF, Set<String> sourceID, Set<String> targetID, Map<String, Integer[]> mapGLO) {
        drawGL20(c3d);
    }

    @Override
    public void drawGL33_BFC_backOnly(Composite3D c3d, GLMatrixStack stack, boolean drawSolidMaterials, Set<Integer> sourceVAO, Set<Integer> targetVAO, Set<Integer> sourceBUF, Set<Integer> targetBUF, Set<String> sourceID, Set<String> targetID, Map<String, Integer[]> mapGLO) {
        drawGL20(c3d);
    }

    @Override
    public void drawGL33_BFC_Colour(Composite3D c3d, GLMatrixStack stack, boolean drawSolidMaterials, Set<Integer> sourceVAO, Set<Integer> targetVAO, Set<Integer> sourceBUF, Set<Integer> targetBUF, Set<String> sourceID, Set<String> targetID, Map<String, Integer[]> mapGLO) {
        drawGL20(c3d);
    }
}
