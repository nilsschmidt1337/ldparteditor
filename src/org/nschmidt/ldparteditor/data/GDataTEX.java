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
import java.util.Map;

import org.nschmidt.ldparteditor.composite.Composite3D;
import org.nschmidt.ldparteditor.helper.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.helper.math.ThreadsafeSortedMap;

/**
 * @author nils
 *
 */
public final class GDataTEX extends GData {

    final TexMeta meta;
    final GData linkedData;
    final GTexture linkedTexture;

    public GDataTEX(GData linkedData, String text, TexMeta meta, GTexture linkedTexture, GData1 parent) {
        super(parent);
        this.linkedTexture = linkedTexture;
        this.linkedData = linkedData;
        this.text = text;
        this.meta = meta;
    }

    @Override
    public void drawGL20(Composite3D c3d) {
        // Implementation is not required.
    }

    @Override
    public void drawGL20RandomColours(Composite3D c3d) {
        // Implementation is not required.
    }

    @Override
    public void drawGL20BFC(Composite3D c3d) {
        // Implementation is not required.
    }

    @Override
    public void drawGL20BFCuncertified(Composite3D c3d) {
        // Implementation is not required.
    }

    @Override
    public void drawGL20BFCbackOnly(Composite3D c3d) {
        // Implementation is not required.
    }

    @Override
    public void drawGL20BFCcolour(Composite3D c3d) {
        // Implementation is not required.
    }

    @Override
    public void drawGL20WhileAddCondlines(Composite3D c3d) {
        // Implementation is not required.
    }

    @Override
    public void drawGL20CoplanarityHeatmap(Composite3D c3d) {
        // Implementation is not required.
    }

    @Override
    public void drawGL20Wireframe(Composite3D c3d) {
        // Implementation is not required.
    }

    @Override
    public void drawGL20BFCtextured(Composite3D c3d) {
        boolean foundLocalTEXMAP = GData.globalFoundTEXMAPStack.peek();
        if (GData.globalFoundTEXMAPNEXT) {
            GData.globalFoundTEXMAPStack.pop();
            GData.globalTextureStack.pop();
            GData.globalFoundTEXMAPStack.push(false);
            GData.globalFoundTEXMAPNEXT = false;
            foundLocalTEXMAP = false;
        }
        if (linkedData != null && foundLocalTEXMAP) {
            linkedData.drawGL20BFCtextured(c3d);
        } else {
            if (meta == TexMeta.FALLBACK && foundLocalTEXMAP && GData.globalDrawObjects) {
                GData.globalDrawObjects = false;
            } else if (meta == TexMeta.END && foundLocalTEXMAP) {
                GData.globalFoundTEXMAPStack.pop();
                GData.globalTextureStack.pop();
                GData.globalFoundTEXMAPStack.push(false);
                GData.globalDrawObjects = true;
            } else if (!foundLocalTEXMAP && (meta == TexMeta.START || meta == TexMeta.NEXT)) {
                GData.globalFoundTEXMAPStack.pop();
                GData.globalFoundTEXMAPStack.push(true);
                GData.globalTextureStack.push(linkedTexture);
                linkedTexture.refreshCache();
                if (meta == TexMeta.NEXT)
                    GData.globalFoundTEXMAPNEXT = true;
                GData.globalDrawObjects = true;
            }
        }
    }

    @Override
    public int type() {
        return 9;
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
        if (meta == null) return text;
        String prefix = "0 !TEXMAP "; //$NON-NLS-1$
        switch (meta) {
        case END:
        case FALLBACK:
            return text;
        case GEOMETRY:
            if (linkedData != null) {
                return "0 !: " + linkedData.transformAndColourReplace(colour, matrix); //$NON-NLS-1$
            } else {
                return text;
            }
        case NEXT:
        case START:
            if (meta == TexMeta.NEXT) {
                prefix = prefix + "NEXT "; //$NON-NLS-1$
            } else {
                prefix = prefix + "START "; //$NON-NLS-1$
            }
            if (linkedTexture != null) {
                TexType texType = linkedTexture.getType();
                switch (texType) {
                case CYLINDRICAL:
                case PLANAR:
                case SPHERICAL:
                    String[] dataSegments = text.trim().split("\\s+"); //$NON-NLS-1$
                    if (texType == TexType.CYLINDRICAL) prefix = prefix + "CYLINDRICAL "; //$NON-NLS-1$
                    else if (texType == TexType.PLANAR) prefix = prefix + "PLANAR "; //$NON-NLS-1$
                    else prefix = prefix + "SPHERICAL "; //$NON-NLS-1$
                    if (dataSegments.length > 12) {
                        try {
                            BigDecimal x1 = new BigDecimal(dataSegments[4]);
                            BigDecimal y1 = new BigDecimal(dataSegments[5]);
                            BigDecimal z1 = new BigDecimal(dataSegments[6]);
                            BigDecimal x2 = new BigDecimal(dataSegments[7]);
                            BigDecimal y2 = new BigDecimal(dataSegments[8]);
                            BigDecimal z2 = new BigDecimal(dataSegments[9]);
                            BigDecimal x3 = new BigDecimal(dataSegments[10]);
                            BigDecimal y3 = new BigDecimal(dataSegments[11]);
                            BigDecimal z3 = new BigDecimal(dataSegments[12]);
                            BigDecimal[] v1 = matrix.transform(x1, y1, z1);
                            BigDecimal[] v2 = matrix.transform(x2, y2, z2);
                            BigDecimal[] v3 = matrix.transform(x3, y3, z3);
                            StringBuilder sb = new StringBuilder(prefix);
                            sb.append(bigDecimalToString(v1[0]));
                            sb.append(" "); //$NON-NLS-1$
                            sb.append(bigDecimalToString(v1[1]));
                            sb.append(" "); //$NON-NLS-1$
                            sb.append(bigDecimalToString(v1[2]));
                            sb.append(" "); //$NON-NLS-1$
                            sb.append(bigDecimalToString(v2[0]));
                            sb.append(" "); //$NON-NLS-1$
                            sb.append(bigDecimalToString(v2[1]));
                            sb.append(" "); //$NON-NLS-1$
                            sb.append(bigDecimalToString(v2[2]));
                            sb.append(" "); //$NON-NLS-1$
                            sb.append(bigDecimalToString(v3[0]));
                            sb.append(" "); //$NON-NLS-1$
                            sb.append(bigDecimalToString(v3[1]));
                            sb.append(" "); //$NON-NLS-1$
                            sb.append(bigDecimalToString(v3[2]));
                            prefix = sb.toString();
                        } catch (NumberFormatException nfe) {
                            return text;
                        }
                        if (texType == TexType.PLANAR && dataSegments.length == 12) return prefix;
                        else if (texType == TexType.CYLINDRICAL && dataSegments.length == 14) return prefix + " " + dataSegments[13]; //$NON-NLS-1$
                        else if (texType == TexType.SPHERICAL && dataSegments.length == 15) return prefix + " " + dataSegments[13] + " " + dataSegments[14]; //$NON-NLS-1$ //$NON-NLS-2$
                        else return text;
                    } else {
                        return text;
                    }
                case NONE:
                default:
                    break;
                }
            } else {
                return text;
            }
            break;
        default:
            break;
        }
        return text;
    }

    @Override
    public void getBFCorientationMap(Map<GData,BFC> map) {
        // Implementation is not required.
    }

    @Override
    public void getBFCorientationMapNOCERTIFY(Map<GData, BFC> map) {
        // Implementation is not required.
    }

    @Override
    public void getBFCorientationMapNOCLIP(Map<GData, BFC> map) {
        // Implementation is not required.
    }

    @Override
    public void getVertexNormalMap(GDataState state, ThreadsafeSortedMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {
        boolean foundLocalTEXMAP = state.globalFoundTEXMAPStack.peek();
        if (state.globalFoundTEXMAPNEXT) {
            state.globalFoundTEXMAPStack.pop();
            state.globalTextureStack.pop();
            state.globalFoundTEXMAPStack.push(false);
            state.globalFoundTEXMAPNEXT = false;
            foundLocalTEXMAP = false;
        }
        if (linkedData != null && foundLocalTEXMAP) {
            linkedData.getVertexNormalMap(state, vertexLinkedToNormalCACHE, dataLinkedToNormalCACHE, vm);
        } else {
            if (meta == TexMeta.FALLBACK && foundLocalTEXMAP && state.globalDrawObjects) {
                state.globalDrawObjects = false;
            } else if (meta == TexMeta.END && foundLocalTEXMAP) {
                state.globalFoundTEXMAPStack.pop();
                state.globalTextureStack.pop();
                state.globalFoundTEXMAPStack.push(false);
                state.globalDrawObjects = true;
            } else if (!foundLocalTEXMAP && (meta == TexMeta.START || meta == TexMeta.NEXT)) {
                state.globalFoundTEXMAPStack.pop();
                state.globalFoundTEXMAPStack.push(true);
                state.globalTextureStack.push(linkedTexture);
                linkedTexture.refreshCache();
                if (meta == TexMeta.NEXT)
                    state.globalFoundTEXMAPNEXT = true;
                state.globalDrawObjects = true;
            }
        }
    }

    @Override
    public void getVertexNormalMapNOCERTIFY(GDataState state, ThreadsafeSortedMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {
        boolean foundLocalTEXMAP = state.globalFoundTEXMAPStack.peek();
        if (state.globalFoundTEXMAPNEXT) {
            state.globalFoundTEXMAPStack.pop();
            state.globalTextureStack.pop();
            state.globalFoundTEXMAPStack.push(false);
            state.globalFoundTEXMAPNEXT = false;
            foundLocalTEXMAP = false;
        }
        if (linkedData != null && foundLocalTEXMAP) {
            linkedData.getVertexNormalMapNOCERTIFY(state, vertexLinkedToNormalCACHE, dataLinkedToNormalCACHE, vm);
        } else {
            if (meta == TexMeta.FALLBACK && foundLocalTEXMAP && state.globalDrawObjects) {
                state.globalDrawObjects = false;
            } else if (meta == TexMeta.END && foundLocalTEXMAP) {
                state.globalFoundTEXMAPStack.pop();
                state.globalTextureStack.pop();
                state.globalFoundTEXMAPStack.push(false);
                state.globalDrawObjects = true;
            } else if (!foundLocalTEXMAP && (meta == TexMeta.START || meta == TexMeta.NEXT)) {
                state.globalFoundTEXMAPStack.pop();
                state.globalFoundTEXMAPStack.push(true);
                state.globalTextureStack.push(linkedTexture);
                linkedTexture.refreshCache();
                if (meta == TexMeta.NEXT)
                    state.globalFoundTEXMAPNEXT = true;
                state.globalDrawObjects = true;
            }
        }
    }

    @Override
    public void getVertexNormalMapNOCLIP(GDataState state, ThreadsafeSortedMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {
        boolean foundLocalTEXMAP = state.globalFoundTEXMAPStack.peek();
        if (state.globalFoundTEXMAPNEXT) {
            state.globalFoundTEXMAPStack.pop();
            state.globalTextureStack.pop();
            state.globalFoundTEXMAPStack.push(false);
            state.globalFoundTEXMAPNEXT = false;
            foundLocalTEXMAP = false;
        }
        if (linkedData != null && foundLocalTEXMAP) {
            linkedData.getVertexNormalMapNOCLIP(state, vertexLinkedToNormalCACHE, dataLinkedToNormalCACHE, vm);
        } else {
            if (meta == TexMeta.FALLBACK && foundLocalTEXMAP && state.globalDrawObjects) {
                state.globalDrawObjects = false;
            } else if (meta == TexMeta.END && foundLocalTEXMAP) {
                state.globalFoundTEXMAPStack.pop();
                state.globalTextureStack.pop();
                state.globalFoundTEXMAPStack.push(false);
                state.globalDrawObjects = true;
            } else if (!foundLocalTEXMAP && (meta == TexMeta.START || meta == TexMeta.NEXT)) {
                state.globalFoundTEXMAPStack.pop();
                state.globalFoundTEXMAPStack.push(true);
                state.globalTextureStack.push(linkedTexture);
                linkedTexture.refreshCache();
                if (meta == TexMeta.NEXT)
                    state.globalFoundTEXMAPNEXT = true;
                state.globalDrawObjects = true;
            }
        }
    }

    public GData getLinkedData() {
        return linkedData;
    }
}