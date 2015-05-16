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
import java.util.HashMap;
import java.util.TreeMap;

import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;

/**
 * @author nils
 *
 */
public final class GData0 extends GData {

    private final boolean isSTEP;

    public GData0(String comment) {
        text = comment;
        isSTEP = false;
    }

    public GData0(String comment, boolean isSTEP) {
        text = comment;
        this.isSTEP = isSTEP;
    }

    @Override
    public void draw(Composite3D c3d) {
    }

    @Override
    public void drawRandomColours(Composite3D c3d) {
    }

    @Override
    public void drawBFC(Composite3D c3d) {
    }

    @Override
    public void drawBFCuncertified(Composite3D c3d) {
    }

    @Override
    public void drawBFC_backOnly(Composite3D c3d) {
    }

    @Override
    public void drawBFC_Colour(Composite3D c3d) {
    }

    @Override
    public void drawWhileAddCondlines(Composite3D c3d) {
    }

    @Override
    public void drawBFC_Textured(Composite3D c3d) {
        // done :)
        if (GData.globalFoundTEXMAPNEXT && !text.equals("")) { //$NON-NLS-1$
            GData.globalFoundTEXMAPStack.pop();
            GData.globalTextureStack.pop();
            GData.globalFoundTEXMAPStack.push(false);
            GData.globalFoundTEXMAPNEXT = false;
        } else if (isSTEP && !GData.globalFoundTEXMAPStack.isEmpty() && GData.globalFoundTEXMAPStack.peek()) {
            GData.globalFoundTEXMAPStack.pop();
            GData.globalTextureStack.pop();
            GData.globalFoundTEXMAPStack.push(false);
            GData.globalDrawObjects = true;
        }
    }

    @Override
    public int type() {
        return 0;
    }

    @Override
    String getNiceString() {
        return text;
    }

    @Override
    public String inlinedString(byte bfc, GColour colour) {
        return text;
    }

    @Override
    public String transformAndColourReplace(String colour, Matrix matrix) {
        // Check for special VERTEX meta command
        if (text.startsWith("0 !LPE")) { //$NON-NLS-1$
            if (text.startsWith("VERTEX ", 7)) { //$NON-NLS-1$
                boolean numberError = false;
                final Vector3d start = new Vector3d();
                String[] data_segments = text.trim().split("\\s+"); //$NON-NLS-1$
                if (data_segments.length == 6) {

                    try {
                        start.setX(new BigDecimal(data_segments[3], Threshold.mc));
                    } catch (NumberFormatException nfe) {
                        numberError = true;
                    }
                    try {
                        start.setY(new BigDecimal(data_segments[4], Threshold.mc));
                    } catch (NumberFormatException nfe) {
                        numberError = true;
                    }
                    try {
                        start.setZ(new BigDecimal(data_segments[5], Threshold.mc));
                    } catch (NumberFormatException nfe) {
                        numberError = true;
                    }
                } else {
                    numberError = true;
                }
                if (!numberError) {
                    BigDecimal[] vert = matrix.transform(start.X, start.Y, start.Z);
                    StringBuilder lineBuilder = new StringBuilder();
                    lineBuilder.append("0 !LPE VERTEX "); //$NON-NLS-1$
                    lineBuilder.append(bigDecimalToString(vert[0]));
                    lineBuilder.append(" "); //$NON-NLS-1$
                    lineBuilder.append(bigDecimalToString(vert[1]));
                    lineBuilder.append(" "); //$NON-NLS-1$
                    lineBuilder.append(bigDecimalToString(vert[2]));
                    return  lineBuilder.toString();
                }
            }
        }
        return text;
    }

    /**
     * Parses the !LPE VERTEX statement and returns the vertex
     *
     * @return the vertex or {@code null} if the line does not refer to a valid
     *         vertex
     */
    public Vertex getVertex() {
        // Check for special VERTEX meta command
        if (text.startsWith("0 !LPE")) { //$NON-NLS-1$
            if (text.startsWith("VERTEX ", 7)) { //$NON-NLS-1$
                boolean numberError = false;
                final Vector3d start = new Vector3d();
                String[] data_segments = text.trim().split("\\s+"); //$NON-NLS-1$
                if (data_segments.length == 6) {
                    try {
                        start.setX(new BigDecimal(data_segments[3], Threshold.mc));
                    } catch (NumberFormatException nfe) {
                        numberError = true;
                    }
                    try {
                        start.setY(new BigDecimal(data_segments[4], Threshold.mc));
                    } catch (NumberFormatException nfe) {
                        numberError = true;
                    }
                    try {
                        start.setZ(new BigDecimal(data_segments[5], Threshold.mc));
                    } catch (NumberFormatException nfe) {
                        numberError = true;
                    }
                } else {
                    numberError = true;
                }
                if (!numberError) {
                    return new Vertex(start.X, start.Y, start.Z);
                }
            }
        }
        return null;
    }

    @Override
    public void getBFCorientationMap(HashMap<GData, Byte> map) {}
    @Override
    public void getBFCorientationMapNOCERTIFY(HashMap<GData, Byte> map) {}
    @Override
    public void getBFCorientationMapNOCLIP(HashMap<GData, Byte> map) {}
    @Override
    public void getVertexNormalMap(TreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, HashMap<GData, float[]> dataLinkedToNormalCACHE, VertexManager vm) {
        drawBFC_Textured(null);
    }
    @Override
    public void getVertexNormalMapNOCERTIFY(TreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, HashMap<GData, float[]> dataLinkedToNormalCACHE, VertexManager vm) {
        drawBFC_Textured(null);
    }
    @Override
    public void getVertexNormalMapNOCLIP(TreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, HashMap<GData, float[]> dataLinkedToNormalCACHE, VertexManager vm) {
        drawBFC_Textured(null);
    }
}
