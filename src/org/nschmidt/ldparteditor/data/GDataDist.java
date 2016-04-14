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
public final class GDataDist extends GData {

    final GData2 line;
    // FIXME Needs implementation!

    public GDataDist(final GData2 line) {
        this.line = line;
    }


    @Override
    String getNiceString() {
        if (text != null)
            return text;
        StringBuilder lineBuilder = new StringBuilder();
        lineBuilder.append("0 !LPE DISTANCE "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(line.X1));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(line.Y1));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(line.Z1));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(line.X2));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(line.Y2));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(line.Z2));
        text = lineBuilder.toString();
        return text;
    }

    @Override
    public String inlinedString(byte bfc, GColour colour) {
        return getNiceString();
    }


    @Override
    public void draw(Composite3D c3d) {
        // TODO Auto-generated method stub

    }


    @Override
    public void drawRandomColours(Composite3D c3d) {
        // TODO Auto-generated method stub

    }


    @Override
    public void drawBFC(Composite3D c3d) {
        // TODO Auto-generated method stub

    }


    @Override
    public void drawBFCuncertified(Composite3D c3d) {
        // TODO Auto-generated method stub

    }


    @Override
    public void drawBFC_backOnly(Composite3D c3d) {
        // TODO Auto-generated method stub

    }


    @Override
    public void drawBFC_Colour(Composite3D c3d) {
        // TODO Auto-generated method stub

    }


    @Override
    public void drawBFC_Textured(Composite3D c3d) {
        // TODO Auto-generated method stub

    }


    @Override
    public void drawWhileAddCondlines(Composite3D c3d) {
        // TODO Auto-generated method stub

    }


    @Override
    public void getBFCorientationMap(HashMap<GData, Byte> map) {}


    @Override
    public void getBFCorientationMapNOCERTIFY(HashMap<GData, Byte> map) {}


    @Override
    public void getBFCorientationMapNOCLIP(HashMap<GData, Byte> map) {}


    @Override
    public void getVertexNormalMap(TreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, HashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {}


    @Override
    public void getVertexNormalMapNOCERTIFY(TreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, HashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {}


    @Override
    public void getVertexNormalMapNOCLIP(TreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, HashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {}


    @Override
    public String transformAndColourReplace(String colour, Matrix matrix) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public int type() {
        return 42;
    }

}
