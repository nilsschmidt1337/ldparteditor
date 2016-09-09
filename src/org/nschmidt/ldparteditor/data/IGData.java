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

    public void drawGL20_RandomColours(Composite3D c3d);

    public void drawGL20_BFC(Composite3D c3d);

    public void drawGL20_BFCuncertified(Composite3D c3d);

    public void drawGL20_BFC_backOnly(Composite3D c3d);

    public void drawGL20_BFC_Colour(Composite3D c3d);

    public void drawGL20_BFC_Textured(Composite3D c3d);

    public void drawGL20_WhileAddCondlines(Composite3D c3d);

    public void getBFCorientationMap(HashMap<GData, Byte> map);
    public void getBFCorientationMapNOCERTIFY(HashMap<GData, Byte> map);
    public void getBFCorientationMapNOCLIP(HashMap<GData, Byte> map);

    public void getVertexNormalMap(GDataState state, ThreadsafeTreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm);
    public void getVertexNormalMapNOCERTIFY(GDataState state, ThreadsafeTreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm);
    public void getVertexNormalMapNOCLIP(GDataState state, ThreadsafeTreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm);


    public String inlinedString(byte bfcStatusTarget, GColour colour);

    public String transformAndColourReplace(String colour, Matrix matrix);

    public int type();

}
