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

/**
 * @author nils
 *
 */
public final class GDataTEX extends GData {

    final TexMeta meta;
    final GData linkedData;
    final GTexture linkedTexture;

    public GDataTEX(GData linkedData, String text, TexMeta meta, GTexture linkedTexture) {
        this.linkedTexture = linkedTexture;
        this.linkedData = linkedData;
        this.text = text;
        this.meta = meta;
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
        boolean foundLocalTEXMAP = GData.globalFoundTEXMAPStack.peek();
        if (GData.globalFoundTEXMAPNEXT) {
            GData.globalFoundTEXMAPStack.pop();
            GData.globalTextureStack.pop();
            GData.globalFoundTEXMAPStack.push(false);
            GData.globalFoundTEXMAPNEXT = false;
            foundLocalTEXMAP = false;
        }
        if (linkedData != null && foundLocalTEXMAP) {
            linkedData.drawBFC_Textured(c3d);
        } else {
            if (meta == TexMeta.FALLBACK && foundLocalTEXMAP && GData.globalDrawObjects) {
                GData.globalDrawObjects = false;
            } else if (meta == TexMeta.END && foundLocalTEXMAP) {
                GData.globalFoundTEXMAPStack.pop();
                GData.globalTextureStack.pop();
                GData.globalFoundTEXMAPStack.push(false);
                GData.globalDrawObjects = true;
            } else if (!foundLocalTEXMAP) {
                if (meta == TexMeta.START || meta == TexMeta.NEXT) {
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
    public String inlinedString(byte bfc, GColour colour) {
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
            // TODO Needs implementation!
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
                    String[] data_segments = text.trim().split("\\s+"); //$NON-NLS-1$
                    if (texType == TexType.CYLINDRICAL) prefix = prefix + "CYLINDRICAL "; //$NON-NLS-1$
                    else if (texType == TexType.PLANAR) prefix = prefix + "PLANAR "; //$NON-NLS-1$
                    else if (texType == TexType.SPHERICAL) prefix = prefix + "SPHERICAL "; //$NON-NLS-1$
                    if (data_segments.length > 12) {
                        try {
                            BigDecimal X1 = new BigDecimal(data_segments[4]);
                            BigDecimal Y1 = new BigDecimal(data_segments[5]);
                            BigDecimal Z1 = new BigDecimal(data_segments[6]);
                            BigDecimal X2 = new BigDecimal(data_segments[7]);
                            BigDecimal Y2 = new BigDecimal(data_segments[8]);
                            BigDecimal Z2 = new BigDecimal(data_segments[9]);
                            BigDecimal X3 = new BigDecimal(data_segments[10]);
                            BigDecimal Y3 = new BigDecimal(data_segments[11]);
                            BigDecimal Z3 = new BigDecimal(data_segments[12]);
                            BigDecimal[] v1 = matrix.transform(X1, Y1, Z1);
                            BigDecimal[] v2 = matrix.transform(X2, Y2, Z2);
                            BigDecimal[] v3 = matrix.transform(X3, Y3, Z3);
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
                        if (texType == TexType.PLANAR && data_segments.length == 12) return prefix;
                        else if (texType == TexType.CYLINDRICAL && data_segments.length == 14) return prefix + " " + data_segments[13]; //$NON-NLS-1$
                        else if (texType == TexType.SPHERICAL && data_segments.length == 15) return prefix + " " + data_segments[13] + " " + data_segments[14]; //$NON-NLS-1$ //$NON-NLS-2$
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
    public void getBFCorientationMap(HashMap<GData, Byte> map) {}
    @Override
    public void getBFCorientationMapNOCERTIFY(HashMap<GData, Byte> map) {}
    @Override
    public void getBFCorientationMapNOCLIP(HashMap<GData, Byte> map) {}

    @Override
    public void getVertexNormalMap(TreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, HashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {
        boolean foundLocalTEXMAP = GData.globalFoundTEXMAPStack.peek();
        if (GData.globalFoundTEXMAPNEXT) {
            GData.globalFoundTEXMAPStack.pop();
            GData.globalTextureStack.pop();
            GData.globalFoundTEXMAPStack.push(false);
            GData.globalFoundTEXMAPNEXT = false;
            foundLocalTEXMAP = false;
        }
        if (linkedData != null && foundLocalTEXMAP) {
            linkedData.getVertexNormalMap(vertexLinkedToNormalCACHE, dataLinkedToNormalCACHE, vm);
        } else {
            if (meta == TexMeta.FALLBACK && foundLocalTEXMAP && GData.globalDrawObjects) {
                GData.globalDrawObjects = false;
            } else if (meta == TexMeta.END && foundLocalTEXMAP) {
                GData.globalFoundTEXMAPStack.pop();
                GData.globalTextureStack.pop();
                GData.globalFoundTEXMAPStack.push(false);
                GData.globalDrawObjects = true;
            } else if (!foundLocalTEXMAP) {
                if (meta == TexMeta.START || meta == TexMeta.NEXT) {
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
    }

    @Override
    public void getVertexNormalMapNOCERTIFY(TreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, HashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {
        boolean foundLocalTEXMAP = GData.globalFoundTEXMAPStack.peek();
        if (GData.globalFoundTEXMAPNEXT) {
            GData.globalFoundTEXMAPStack.pop();
            GData.globalTextureStack.pop();
            GData.globalFoundTEXMAPStack.push(false);
            GData.globalFoundTEXMAPNEXT = false;
            foundLocalTEXMAP = false;
        }
        if (linkedData != null && foundLocalTEXMAP) {
            linkedData.getVertexNormalMapNOCERTIFY(vertexLinkedToNormalCACHE, dataLinkedToNormalCACHE, vm);
        } else {
            if (meta == TexMeta.FALLBACK && foundLocalTEXMAP && GData.globalDrawObjects) {
                GData.globalDrawObjects = false;
            } else if (meta == TexMeta.END && foundLocalTEXMAP) {
                GData.globalFoundTEXMAPStack.pop();
                GData.globalTextureStack.pop();
                GData.globalFoundTEXMAPStack.push(false);
                GData.globalDrawObjects = true;
            } else if (!foundLocalTEXMAP) {
                if (meta == TexMeta.START || meta == TexMeta.NEXT) {
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
    }

    @Override
    public void getVertexNormalMapNOCLIP(TreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, HashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {
        boolean foundLocalTEXMAP = GData.globalFoundTEXMAPStack.peek();
        if (GData.globalFoundTEXMAPNEXT) {
            GData.globalFoundTEXMAPStack.pop();
            GData.globalTextureStack.pop();
            GData.globalFoundTEXMAPStack.push(false);
            GData.globalFoundTEXMAPNEXT = false;
            foundLocalTEXMAP = false;
        }
        if (linkedData != null && foundLocalTEXMAP) {
            linkedData.getVertexNormalMapNOCLIP(vertexLinkedToNormalCACHE, dataLinkedToNormalCACHE, vm);
        } else {
            if (meta == TexMeta.FALLBACK && foundLocalTEXMAP && GData.globalDrawObjects) {
                GData.globalDrawObjects = false;
            } else if (meta == TexMeta.END && foundLocalTEXMAP) {
                GData.globalFoundTEXMAPStack.pop();
                GData.globalTextureStack.pop();
                GData.globalFoundTEXMAPStack.push(false);
                GData.globalDrawObjects = true;
            } else if (!foundLocalTEXMAP) {
                if (meta == TexMeta.START || meta == TexMeta.NEXT) {
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
    }

    public GData getLinkedData() {
        return linkedData;
    }
}