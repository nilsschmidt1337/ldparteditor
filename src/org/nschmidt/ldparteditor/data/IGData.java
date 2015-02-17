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

/**
 * @author nils
 *
 */
interface IGData {

    public void draw(Composite3D c3d);

    public void drawRandomColours(Composite3D c3d);

    public void drawBFC(Composite3D c3d);

    public void drawBFCuncertified(Composite3D c3d);

    public void drawBFC_backOnly(Composite3D c3d);

    public void drawBFC_Colour(Composite3D c3d);

    public void drawBFC_Textured(Composite3D c3d);

    public void drawWhileAddCondlines(Composite3D c3d);

    public void getBFCorientationMap(HashMap<GData, Byte> map);
    public void getBFCorientationMapNOCERTIFY(HashMap<GData, Byte> map);
    public void getBFCorientationMapNOCLIP(HashMap<GData, Byte> map);

    public void getVertexNormalMap(HashMap<Vertex, float[]> vertexLinkedToNormalCACHE, HashMap<GData, float[]> dataLinkedToNormalCACHE, VertexManager vm);
    public void getVertexNormalMapNOCERTIFY(HashMap<Vertex, float[]> vertexLinkedToNormalCACHE, HashMap<GData, float[]> dataLinkedToNormalCACHE, VertexManager vm);
    public void getVertexNormalMapNOCLIP(HashMap<Vertex, float[]> vertexLinkedToNormalCACHE, HashMap<GData, float[]> dataLinkedToNormalCACHE, VertexManager vm);


    public String inlinedString(byte bfcStatusTarget, GColour colour);

    public String transformAndColourReplace(String colour, Matrix matrix);

    public int type();

}
