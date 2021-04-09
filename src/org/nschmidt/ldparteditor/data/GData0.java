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

import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeTreeMap;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;

/**
 * @author nils
 *
 */
public final class GData0 extends GData {

    private final boolean isSTEP;

    public GData0(String comment, GData1 parent) {
        super(parent);
        text = comment;
        isSTEP = false;
    }

    public GData0(String comment, boolean isSTEP, GData1 parent) {
        super(parent);
        text = comment;
        this.isSTEP = isSTEP;
    }

    @Override
    public void drawGL20(Composite3D c3d) {
    }

    @Override
    public void drawGL20_RandomColours(Composite3D c3d) {
    }

    @Override
    public void drawGL20_BFC(Composite3D c3d) {
    }

    @Override
    public void drawGL20_BFCuncertified(Composite3D c3d) {
    }

    @Override
    public void drawGL20_BFC_backOnly(Composite3D c3d) {
    }

    @Override
    public void drawGL20_BFC_Colour(Composite3D c3d) {
    }

    @Override
    public void drawGL20_WhileAddCondlines(Composite3D c3d) {
    }

    @Override
    public void drawGL20_CoplanarityHeatmap(Composite3D c3d) {
    }

    @Override
    public void drawGL20_Wireframe(Composite3D c3d) {
    }

    @Override
    public void drawGL20_BFC_Textured(Composite3D c3d) {
        // done :)
        if (GData.globalFoundTEXMAPNEXT && !text.equals("")) { //$NON-NLS-1$
            GData.globalFoundTEXMAPStack.pop();
            GData.globalTextureStack.pop();
            GData.globalFoundTEXMAPStack.push(false);
            GData.globalFoundTEXMAPNEXT = false;
        } else if (isSTEP && !GData.globalFoundTEXMAPStack.isEmpty() && Boolean.TRUE.equals(GData.globalFoundTEXMAPStack.peek())) {
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
    public String inlinedString(BFC bfc, GColour colour) {
        return text;
    }

    @Override
    public String transformAndColourReplace(String colour, Matrix matrix) {
        // Check for special VERTEX meta command
        if (text.startsWith("0 !LPE")) { //$NON-NLS-1$
            if (text.startsWith("VERTEX ", 7)) { //$NON-NLS-1$
                boolean numberError = false;
                final Vector3d start = new Vector3d();
                String[] dataSegments = text.trim().split("\\s+"); //$NON-NLS-1$
                if (dataSegments.length == 6) {
                    try {
                        start.setX(new BigDecimal(dataSegments[3], Threshold.MC));
                        start.setY(new BigDecimal(dataSegments[4], Threshold.MC));
                        start.setZ(new BigDecimal(dataSegments[5], Threshold.MC));
                    } catch (NumberFormatException nfe) {
                        numberError = true;
                    }
                } else {
                    numberError = true;
                }
                if (!numberError) {
                    BigDecimal[] vert = matrix.transform(start.x, start.y, start.z);
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
                String[] dataSegments = text.trim().split("\\s+"); //$NON-NLS-1$
                if (dataSegments.length == 6) {
                    try {
                        start.setX(new BigDecimal(dataSegments[3], Threshold.MC));
                        start.setY(new BigDecimal(dataSegments[4], Threshold.MC));
                        start.setZ(new BigDecimal(dataSegments[5], Threshold.MC));
                    } catch (NumberFormatException nfe) {
                        numberError = true;
                    }
                } else {
                    numberError = true;
                }
                if (!numberError) {
                    return new Vertex(start.x, start.y, start.z);
                }
            }
        }
        return null;
    }

    @Override
    public void getBFCorientationMap(HashMap<GData, BFC> map) {}
    @Override
    public void getBFCorientationMapNOCERTIFY(HashMap<GData, BFC> map) {}
    @Override
    public void getBFCorientationMapNOCLIP(HashMap<GData, BFC> map) {}
    @Override
    public void getVertexNormalMap(GDataState state, ThreadsafeTreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {
        normalMapHelper(state);
    }
    @Override
    public void getVertexNormalMapNOCERTIFY(GDataState state, ThreadsafeTreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {
        normalMapHelper(state);
    }
    @Override
    public void getVertexNormalMapNOCLIP(GDataState state, ThreadsafeTreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {
        normalMapHelper(state);
    }

    private void normalMapHelper(GDataState state) {
        if (state.globalFoundTEXMAPNEXT && !text.equals("")) { //$NON-NLS-1$
            state.globalFoundTEXMAPStack.pop();
            state.globalTextureStack.pop();
            state.globalFoundTEXMAPStack.push(false);
            state.globalFoundTEXMAPNEXT = false;
        } else if (isSTEP && !state.globalFoundTEXMAPStack.isEmpty() && Boolean.TRUE.equals(state.globalFoundTEXMAPStack.peek())) {
            state.globalFoundTEXMAPStack.pop();
            state.globalTextureStack.pop();
            state.globalFoundTEXMAPStack.push(false);
            state.globalDrawObjects = true;
        }
    }
}
