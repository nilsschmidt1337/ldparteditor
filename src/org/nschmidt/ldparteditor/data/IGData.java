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

import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeTreeMap;

/**
 * @author nils
 *
 */
interface IGData {

    public void drawGL20(Composite3D c3d);

    public void drawGL20RandomColours(Composite3D c3d);

    public void drawGL20BFC(Composite3D c3d);

    public void drawGL20BFCuncertified(Composite3D c3d);

    public void drawGL20BFCbackOnly(Composite3D c3d);

    public void drawGL20BFCcolour(Composite3D c3d);

    public void drawGL20BFCtextured(Composite3D c3d);

    public void drawGL20WhileAddCondlines(Composite3D c3d);

    public void drawGL20CoplanarityHeatmap(Composite3D c3d);

    public void drawGL20Wireframe(Composite3D c3d);

    public void getBFCorientationMap(HashMap<GData, BFC> map);
    public void getBFCorientationMapNOCERTIFY(HashMap<GData, BFC> map);
    public void getBFCorientationMapNOCLIP(HashMap<GData, BFC> map);

    public void getVertexNormalMap(GDataState state, ThreadsafeTreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm);
    public void getVertexNormalMapNOCERTIFY(GDataState state, ThreadsafeTreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm);
    public void getVertexNormalMapNOCLIP(GDataState state, ThreadsafeTreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm);

    public String inlinedString(BFC bfcStatusTarget, GColour colour);

    public String transformAndColourReplace(String colour, Matrix matrix);

    public int type();

}
