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

import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.opengl.GL33Helper;
import org.nschmidt.ldparteditor.opengl.GLShader;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;

public enum GL33TexmapRenderer {
    INSTANCE;

    public static void render(ArrayList<GDataAndTexture> texmapData, GLShader mainShader, OpenGLRenderer renderer,
            HashMap<GData, Vector3f[]> normalMap, HashMap<GData, Vertex[]> vertexMap, boolean smoothShading, boolean drawSolidMaterials) {
        GTexture lastTexture = null;
        float[] uv;
        float[] triVertices = new float[36];
        float[] quadVertices = new float[48];
        final Vector4f Nv = new Vector4f(0, 0, 0, 1f);
        final HashMap<GData1, Matrix4f> matrixMap = new HashMap<>();
        int[] triIndices = new int[]{0, 1, 2};
        int[] quadIndices = new int[]{0, 1, 2, 2, 3, 0};

        int[] triIndicesNOCLIP = new int[]{0, 2, 1};
        int[] quadIndicesNOCLIP = new int[]{0, 3, 2, 2, 1, 0};

        boolean texmap = true;

        for (GDataAndTexture gw : texmapData) {
        	final GTexture tex = gw.texture;
        	final GData gd = gw.data;
        	final boolean isTextured = tex != GTexture.NO_TEXTURE;

        	if (isTextured && tex != lastTexture) {
                lastTexture = tex;
                lastTexture.refreshCache();
                lastTexture.bindGL33(renderer, mainShader);
        	}

            if (texmap && !isTextured) {
                mainShader.texmapOff();
                texmap = false;
            } else if (!texmap && isTextured){
                mainShader.texmapOn();
                texmap = true;
            }
            switch (gd.type()) {
            case 3:
                GData3 gd3 = (GData3) gd;
                if (!isTextured && gd3.a < 1f && drawSolidMaterials) continue;
                Vertex[] v = vertexMap.get(gd);
                Vector3f[] n = normalMap.get(gd);
                if (n == null) {
                    Nv.x = gd3.xn;
                    Nv.y = gd3.yn;
                    Nv.z = gd3.zn;
                    Nv.w = 1f;
                    Matrix4f loc = matrixMap.get(gd3.parent);
                    if (loc == null) {
                        final Matrix4f rotation = new Matrix4f(gd3.parent.productMatrix);
                        rotation.m30 = 0f;
                        rotation.m31 = 0f;
                        rotation.m32 = 0f;
                        rotation.invert();
                        rotation.transpose();
                        matrixMap.put(gd3.parent, rotation);
                        loc = rotation;
                    }
                    Matrix4f.transform(loc, Nv, Nv);
                    n = new Vector3f[3];
                    n[0] = new Vector3f(Nv.x, Nv.y, Nv.z);
                    n[1] = n[0];
                    n[2] = n[0];
                }
                if (lastTexture != null && v != null) {
                    lastTexture.calcUVcoords1(gd3.x1, gd3.y1, gd3.z1, gd3.parent, gd);
                    lastTexture.calcUVcoords2(gd3.x2, gd3.y2, gd3.z2, gd3.parent);
                    lastTexture.calcUVcoords3(gd3.x3, gd3.y3, gd3.z3, gd3.parent);
                    uv = lastTexture.getUVcoords(true, gd);
                    GColour c = View.getLDConfigColour(View.getLDConfigIndex(gd3.r, gd3.g, gd3.b));
                    GColourType ct = c.getType();
                    boolean hasColourType = ct != null;
                    if (hasColourType) {
                        switch (ct.type()) {
                        case CHROME:
                            colourise(3, gd3.r, gd3.g, gd3.b, 2f, triVertices);
                            break;
                        case MATTE_METALLIC:
                            colourise(3, gd3.r, gd3.g, gd3.b, 4.2f, triVertices);
                            break;
                        case METAL:
                            colourise(3, gd3.r, gd3.g, gd3.b, 3.2f, triVertices);
                            break;
                        case RUBBER:
                            colourise(3, gd3.r, gd3.g, gd3.b, 5.2f, triVertices);
                            break;
                        default:
                            colourise(3, gd3.r, gd3.g, gd3.b, gd3.a, triVertices);
                            break;
                        }
                    } else {
                        colourise(3, gd3.r, gd3.g, gd3.b, gd3.a, triVertices);
                    }
                    switch (gw.winding) {
                    case BFC.CW:
                        if (smoothShading) {
                            if (gw.invertNext ^ gw.negativeDeterminant) {
                                normal(0, n[0].x, n[0].y, n[0].z, triVertices);
                                normal(1, n[2].x, n[2].y, n[2].z, triVertices);
                                normal(2, n[1].x, n[1].y, n[1].z, triVertices);
                            } else {
                                normal(0, n[0].x, n[0].y, n[0].z, triVertices);
                                normal(1, n[1].x, n[1].y, n[1].z, triVertices);
                                normal(2, n[2].x, n[2].y, n[2].z, triVertices);
                            }
                        } else {
                            if (gw.invertNext) {
                                normal(0, n[0].x, n[0].y, n[0].z, triVertices);
                                normal(1, n[1].x, n[1].y, n[1].z, triVertices);
                                normal(2, n[2].x, n[2].y, n[2].z, triVertices);
                            } else {
                                normal(0, -n[0].x, -n[0].y, -n[0].z, triVertices);
                                normal(1, -n[1].x, -n[1].y, -n[1].z, triVertices);
                                normal(2, -n[2].x, -n[2].y, -n[2].z, triVertices);
                            }
                        }
                        if (gw.negativeDeterminant ^ gw.invertNext) {
                            pointAt(0, v[0].x, v[0].y, v[0].z, triVertices);
                            pointAt(1, v[2].x, v[2].y, v[2].z, triVertices);
                            pointAt(2, v[1].x, v[1].y, v[1].z, triVertices);
                            uv(0, uv[0], uv[1], triVertices);
                            uv(1, uv[4], uv[5], triVertices);
                            uv(2, uv[2], uv[3], triVertices);
                        } else {
                            pointAt(0, v[0].x, v[0].y, v[0].z, triVertices);
                            pointAt(1, v[1].x, v[1].y, v[1].z, triVertices);
                            pointAt(2, v[2].x, v[2].y, v[2].z, triVertices);
                            uv(0, uv[0], uv[1], triVertices);
                            uv(1, uv[2], uv[3], triVertices);
                            uv(2, uv[4], uv[5], triVertices);
                        }
                        break;
                    case BFC.CCW:
                        if (smoothShading) {
                            if (gw.invertNext ^ gw.negativeDeterminant) {
                                normal(0, n[0].x, n[0].y, n[0].z, triVertices);
                                normal(1, n[1].x, n[1].y, n[1].z, triVertices);
                                normal(2, n[2].x, n[2].y, n[2].z, triVertices);
                            } else {
                                normal(0, n[0].x, n[0].y, n[0].z, triVertices);
                                normal(1, n[2].x, n[2].y, n[2].z, triVertices);
                                normal(2, n[1].x, n[1].y, n[1].z, triVertices);
                            }
                        } else {
                            if (gw.invertNext) {
                                normal(0, -n[0].x, -n[0].y, -n[0].z, triVertices);
                                normal(1, -n[1].x, -n[1].y, -n[1].z, triVertices);
                                normal(2, -n[2].x, -n[2].y, -n[2].z, triVertices);
                            } else {
                                normal(0, n[0].x, n[0].y, n[0].z, triVertices);
                                normal(1, n[1].x, n[1].y, n[1].z, triVertices);
                                normal(2, n[2].x, n[2].y, n[2].z, triVertices);
                            }
                        }
                        if (gw.negativeDeterminant ^ gw.invertNext) {
                            pointAt(0, v[0].x, v[0].y, v[0].z, triVertices);
                            pointAt(1, v[1].x, v[1].y, v[1].z, triVertices);
                            pointAt(2, v[2].x, v[2].y, v[2].z, triVertices);
                            uv(0, uv[0], uv[1], triVertices);
                            uv(1, uv[2], uv[3], triVertices);
                            uv(2, uv[4], uv[5], triVertices);
                        } else {
                            pointAt(0, v[0].x, v[0].y, v[0].z, triVertices);
                            pointAt(1, v[2].x, v[2].y, v[2].z, triVertices);
                            pointAt(2, v[1].x, v[1].y, v[1].z, triVertices);
                            uv(0, uv[0], uv[1], triVertices);
                            uv(1, uv[4], uv[5], triVertices);
                            uv(2, uv[2], uv[3], triVertices);
                        }
                        break;
                    case BFC.NOCERTIFY:
                    default:
                        colourise(3, 0f, 0f, 0f, 0f, triVertices);
                        continue;
                    }
                    GL33Helper.drawTrianglesIndexedTextured_GeneralSlow(triVertices, triIndices);
                    if (gw.noclip || (isTextured && gd3.a < 1f)) {
                        flipnormals(3, triVertices);
                        GL33Helper.drawTrianglesIndexedTextured_GeneralSlow(triVertices, triIndicesNOCLIP);
                    }
                }
                continue;
            case 4:
                GData4 gd4 = (GData4) gd;
                if (!isTextured && gd4.a < 1f && drawSolidMaterials) continue;
                v = vertexMap.get(gd);
                n = normalMap.get(gd);
                if (n == null) {
                    Nv.x = gd4.xn;
                    Nv.y = gd4.yn;
                    Nv.z = gd4.zn;
                    Nv.w = 1f;
                    Matrix4f loc = matrixMap.get(gd4.parent);
                    if (loc == null) {
                        final Matrix4f rotation = new Matrix4f(gd4.parent.productMatrix);
                        rotation.m30 = 0f;
                        rotation.m31 = 0f;
                        rotation.m32 = 0f;
                        rotation.invert();
                        rotation.transpose();
                        matrixMap.put(gd4.parent, rotation);
                        loc = rotation;
                    }
                    Matrix4f.transform(loc, Nv, Nv);
                    n = new Vector3f[4];
                    n[0] = new Vector3f(Nv.x, Nv.y, Nv.z);
                    n[1] = n[0];
                    n[2] = n[0];
                    n[3] = n[0];
                }
                if (lastTexture != null && v != null) {
                    lastTexture.calcUVcoords1(gd4.x1, gd4.y1, gd4.z1, gd4.parent, gd);
                    lastTexture.calcUVcoords2(gd4.x2, gd4.y2, gd4.z2, gd4.parent);
                    lastTexture.calcUVcoords3(gd4.x3, gd4.y3, gd4.z3, gd4.parent);
                    lastTexture.calcUVcoords4(gd4.x4, gd4.y4, gd4.z4, gd4.parent);
                    uv = lastTexture.getUVcoords(false, gd);
                    GColour c = View.getLDConfigColour(View.getLDConfigIndex(gd4.r, gd4.g, gd4.b));
                    GColourType ct = c.getType();
                    boolean hasColourType = ct != null;
                    if (hasColourType) {
                        switch (ct.type()) {
                        case CHROME:
                            colourise(4, gd4.r, gd4.g, gd4.b, 2f, quadVertices);
                            break;
                        case MATTE_METALLIC:
                            colourise(4, gd4.r, gd4.g, gd4.b, 4.2f, quadVertices);
                            break;
                        case METAL:
                            colourise(4, gd4.r, gd4.g, gd4.b, 3.2f, quadVertices);
                            break;
                        case RUBBER:
                            colourise(4, gd4.r, gd4.g, gd4.b, 5.2f, quadVertices);
                            break;
                        default:
                            colourise(4, gd4.r, gd4.g, gd4.b, gd4.a, quadVertices);
                            break;
                        }
                    } else {
                        colourise(4, gd4.r, gd4.g, gd4.b, gd4.a, quadVertices);
                    }
                    switch (gw.winding) {
                    case BFC.CW:
                        if (smoothShading) {
                            if (gw.invertNext ^ gw.negativeDeterminant) {
                                normal(0, n[0].x, n[0].y, n[0].z, quadVertices);
                                normal(1, n[3].x, n[3].y, n[3].z, quadVertices);
                                normal(2, n[2].x, n[2].y, n[2].z, quadVertices);
                                normal(3, n[1].x, n[1].y, n[1].z, quadVertices);
                            } else {
                                normal(0, n[0].x, n[0].y, n[0].z, quadVertices);
                                normal(1, n[1].x, n[1].y, n[1].z, quadVertices);
                                normal(2, n[2].x, n[2].y, n[2].z, quadVertices);
                                normal(3, n[3].x, n[3].y, n[3].z, quadVertices);
                            }
                        } else {
                            if (gw.invertNext) {
                                normal(0, n[0].x, n[0].y, n[0].z, quadVertices);
                                normal(1, n[1].x, n[1].y, n[1].z, quadVertices);
                                normal(2, n[2].x, n[2].y, n[2].z, quadVertices);
                                normal(3, n[3].x, n[3].y, n[3].z, quadVertices);
                            } else {
                                normal(0, -n[0].x, -n[0].y, -n[0].z, quadVertices);
                                normal(1, -n[1].x, -n[1].y, -n[1].z, quadVertices);
                                normal(2, -n[2].x, -n[2].y, -n[2].z, quadVertices);
                                normal(3, -n[3].x, -n[3].y, -n[3].z, quadVertices);
                            }
                        }
                        if (gw.negativeDeterminant ^ gw.invertNext) {
                            pointAt(0, v[0].x, v[0].y, v[0].z, quadVertices);
                            pointAt(1, v[3].x, v[3].y, v[3].z, quadVertices);
                            pointAt(2, v[2].x, v[2].y, v[2].z, quadVertices);
                            pointAt(3, v[1].x, v[1].y, v[1].z, quadVertices);
                            uv(0, uv[0], uv[1], quadVertices);
                            uv(1, uv[6], uv[7], quadVertices);
                            uv(2, uv[4], uv[5], quadVertices);
                            uv(3, uv[2], uv[3], quadVertices);
                        } else {
                            pointAt(0, v[0].x, v[0].y, v[0].z, quadVertices);
                            pointAt(1, v[1].x, v[1].y, v[1].z, quadVertices);
                            pointAt(2, v[2].x, v[2].y, v[2].z, quadVertices);
                            pointAt(3, v[3].x, v[3].y, v[3].z, quadVertices);
                            uv(0, uv[0], uv[1], quadVertices);
                            uv(1, uv[2], uv[3], quadVertices);
                            uv(2, uv[4], uv[5], quadVertices);
                            uv(3, uv[6], uv[7], quadVertices);
                        }
                        break;
                    case BFC.CCW:
                        if (smoothShading) {
                            if (gw.invertNext ^ gw.negativeDeterminant) {
                                normal(0, n[0].x, n[0].y, n[0].z, quadVertices);
                                normal(1, n[1].x, n[1].y, n[1].z, quadVertices);
                                normal(2, n[2].x, n[2].y, n[2].z, quadVertices);
                                normal(3, n[3].x, n[3].y, n[3].z, quadVertices);
                            } else {
                                normal(0, n[0].x, n[0].y, n[0].z, quadVertices);
                                normal(1, n[3].x, n[3].y, n[3].z, quadVertices);
                                normal(2, n[2].x, n[2].y, n[2].z, quadVertices);
                                normal(3, n[1].x, n[1].y, n[1].z, quadVertices);
                            }
                        } else {
                            if (gw.invertNext) {
                                normal(0, -n[0].x, -n[0].y, -n[0].z, quadVertices);
                                normal(1, -n[1].x, -n[1].y, -n[1].z, quadVertices);
                                normal(2, -n[2].x, -n[2].y, -n[2].z, quadVertices);
                                normal(3, -n[3].x, -n[3].y, -n[3].z, quadVertices);
                            } else {
                                normal(0, n[0].x, n[0].y, n[0].z, quadVertices);
                                normal(1, n[1].x, n[1].y, n[1].z, quadVertices);
                                normal(2, n[2].x, n[2].y, n[2].z, quadVertices);
                                normal(3, n[3].x, n[3].y, n[3].z, quadVertices);
                            }
                        }
                        if (gw.negativeDeterminant ^ gw.invertNext) {
                            pointAt(0, v[0].x, v[0].y, v[0].z, quadVertices);
                            pointAt(1, v[1].x, v[1].y, v[1].z, quadVertices);
                            pointAt(2, v[2].x, v[2].y, v[2].z, quadVertices);
                            pointAt(3, v[3].x, v[3].y, v[3].z, quadVertices);
                            uv(0, uv[0], uv[1], quadVertices);
                            uv(1, uv[2], uv[3], quadVertices);
                            uv(2, uv[4], uv[5], quadVertices);
                            uv(3, uv[6], uv[7], quadVertices);
                        } else {
                            pointAt(0, v[0].x, v[0].y, v[0].z, quadVertices);
                            pointAt(1, v[3].x, v[3].y, v[3].z, quadVertices);
                            pointAt(2, v[2].x, v[2].y, v[2].z, quadVertices);
                            pointAt(3, v[1].x, v[1].y, v[1].z, quadVertices);
                            uv(0, uv[0], uv[1], quadVertices);
                            uv(1, uv[6], uv[7], quadVertices);
                            uv(2, uv[4], uv[5], quadVertices);
                            uv(3, uv[2], uv[3], quadVertices);
                        }
                        break;
                    case BFC.NOCERTIFY:
                    default:
                        colourise(4, 0f, 0f, 0f, 0f, quadVertices);
                        continue;
                    }
                    GL33Helper.drawTrianglesIndexedTextured_GeneralSlow(quadVertices, quadIndices);
                    if (gw.noclip || (isTextured && gd4.a < 1f)) {
                        flipnormals(4, quadVertices);
                        GL33Helper.drawTrianglesIndexedTextured_GeneralSlow(quadVertices, quadIndicesNOCLIP);
                    }
                }
                continue;
            default:
            }
        }
    }

    private static void flipnormals(int i, float[] data) {
        for (int j = 0; j < i; j++) {
            int k = j * 12;
            data[k + 3] = -data[k + 3];
            data[k + 4] = -data[k + 4];
            data[k + 5] = -data[k + 5];
        }
    }

    private static void pointAt(int i, float x, float y, float z,
            float[] data) {
        int j = i * 12;
        data[j] = x;
        data[j + 1] = y;
        data[j + 2] = z;
    }

    private static void normal(int i, float x, float y, float z,
            float[] data) {
        int j = i * 12;
        data[j + 3] = x;
        data[j + 4] = y;
        data[j + 5] = z;
    }

    private static void colourise(int i, float r, float g, float b, float a,
            float[] data) {
        for (int j = 0; j < i; j++) {
            int k = j * 12;
            data[k + 6] = r;
            data[k + 7] = g;
            data[k + 8] = b;
            data[k + 9] = a;
        }
    }

    private static void uv(int i, float u, float v, float[] data) {
        int j = i * 12;
        data[j + 10] = u;
        data[j + 11] = v;
    }
}
