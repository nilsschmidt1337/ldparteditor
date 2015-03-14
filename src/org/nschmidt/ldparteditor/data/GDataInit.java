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
import java.util.TreeMap;

import org.nschmidt.ldparteditor.composites.Composite3D;

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
    public void draw(Composite3D c3d) {
        GData.localWinding = BFC.NOCERTIFY;
        GData.accumClip = 0;
        GData.globalInvertNext = false;
        GData.globalInvertNextFound = false;
        GData.globalNegativeDeterminant = false;
    }

    @Override
    public void drawRandomColours(Composite3D c3d) {
        draw(c3d);
    }

    @Override
    public void drawBFC(Composite3D c3d) {
        draw(c3d);
    }

    @Override
    public void drawBFCprimitive() {
        draw(null);
    }

    @Override
    public void drawBFCuncertified(Composite3D c3d) {
        draw(c3d);
    }

    @Override
    public void drawBFC_backOnly(Composite3D c3d) {
        draw(c3d);
    }

    @Override
    public void drawBFC_Colour(Composite3D c3d) {
        draw(c3d);
    }

    @Override
    public void drawWhileAddCondlines(Composite3D c3d) {
        draw(c3d);
    }

    @Override
    public void drawBFC_Textured(Composite3D c3d) {
        draw(c3d);
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
    public void getVertexNormalMap(TreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, HashMap<GData, float[]> dataLinkedToNormalCACHE, VertexManager vm) {
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
    @Override
    public void getVertexNormalMapNOCERTIFY(TreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, HashMap<GData, float[]> dataLinkedToNormalCACHE, VertexManager vm) {
        getVertexNormalMap(vertexLinkedToNormalCACHE, null, null);
    }
    @Override
    public void getVertexNormalMapNOCLIP(TreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, HashMap<GData, float[]> dataLinkedToNormalCACHE, VertexManager vm) {
        getVertexNormalMap(vertexLinkedToNormalCACHE, null, null);
    }

    public GData1 getParent() {
        return parent;
    }
}
