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

import java.io.Serializable;

import org.lwjgl.opengl.GL11;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.opengl.GL33HelperPrimitives;
import org.nschmidt.ldparteditor.opengl.GLMatrixStack;

/**
 * @author nils
 *
 */
public final class PGData4 extends PGData implements Serializable {

    private static final long serialVersionUID = 1L;

    final float x1;
    final float y1;
    final float z1;
    final float x2;
    final float y2;
    final float z2;
    final float x3;
    final float y3;
    final float z3;
    final float x4;
    final float y4;
    final float z4;
    
    private final transient int[] indices = new int[12];
    private final transient float[] vertices = new float[48]; 
    
    public PGData4(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4) {
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
        this.x3 = x3;
        this.y3 = y3;
        this.z3 = z3;
        this.x4 = x4;
        this.y4 = y4;
        this.z4 = z4;
        
        vertices[0] = x1;
        vertices[1] = y1;
        vertices[2] = z1;
        vertices[6] = x2;
        vertices[7] = y2;
        vertices[8] = z2;
        vertices[12] = x3;
        vertices[13] = y3;
        vertices[14] = z3;
        vertices[18] = x4;
        vertices[19] = y4;
        vertices[20] = z4;
        vertices[24] = x1;
        vertices[25] = y1;
        vertices[26] = z1;
        vertices[30] = x2;
        vertices[31] = y2;
        vertices[32] = z2;
        vertices[36] = x3;
        vertices[37] = y3;
        vertices[38] = z3;
        vertices[42] = x4;
        vertices[43] = y4;
        vertices[44] = z4;
    }
    @Override
    public int type() {
        return 4;
    }
    @Override
    public void drawBFCprimitive_GL20(int drawOnlyMode) {
        if (drawOnlyMode == 2) return;
        switch (PGData.accumClip > 0 ? BFC.NOCLIP : PGData.localWinding) {
        case BFC.CCW:
            if (PGData.globalNegativeDeterminant) {
                if (PGData.globalInvertNext) {
                    GL11.glColor4f( // 111
                            View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], 1f);
                    GL11.glBegin(GL11.GL_QUADS);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glColor4f(View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], 1f);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f( // 110
                            View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], 1f);
                    GL11.glBegin(GL11.GL_QUADS);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glColor4f(View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], 1f);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glEnd();
                }
            } else {
                if (PGData.globalInvertNext) {
                    GL11.glColor4f( // 101
                            View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], 1f);
                    GL11.glBegin(GL11.GL_QUADS);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glColor4f(View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], 1f);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f( // 100
                            View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], 1f);
                    GL11.glBegin(GL11.GL_QUADS);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glColor4f(View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], 1f);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glEnd();
                }
            }
            break;
        case BFC.CW:
            if (PGData.globalNegativeDeterminant) {
                if (PGData.globalInvertNext) {
                    GL11.glColor4f( // 011
                            View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], 1f);
                    GL11.glBegin(GL11.GL_QUADS);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glColor4f(View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], 1f);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f( // 010
                            View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], 1f);
                    GL11.glBegin(GL11.GL_QUADS);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glColor4f(View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], 1f);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glEnd();
                }
            } else {
                if (PGData.globalInvertNext) {
                    GL11.glColor4f( // 001
                            View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], 1f);
                    GL11.glBegin(GL11.GL_QUADS);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glColor4f(View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], 1f);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f( // 000
                            View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], 1f);
                    GL11.glBegin(GL11.GL_QUADS);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glColor4f(View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], 1f);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glEnd();
                }
            }
            break;
        case BFC.NOCERTIFY:
            GL11.glColor4f(View.BFC_uncertified_Colour_r[0], View.BFC_uncertified_Colour_g[0], View.BFC_uncertified_Colour_b[0], 1f);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x4, y4, z4);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glVertex3f(x4, y4, z4);
            GL11.glEnd();
            break;
        case BFC.NOCLIP:
            GL11.glColor4f(View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], 1f);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x4, y4, z4);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glVertex3f(x4, y4, z4);
            GL11.glEnd();
            break;
        }
    }
    
    @Override
    public void drawBFCprimitiveGL33(GLMatrixStack stack, int drawOnlyMode) {
        if (drawOnlyMode == 2) return;
        switch (PGData.accumClip > 0 ? BFC.NOCLIP : PGData.localWinding) {
        case BFC.CCW:
            if (PGData.globalNegativeDeterminant) {
                if (PGData.globalInvertNext) {                    
                    indices[0] = 0;
                    indices[1] = 3;
                    indices[2] = 2;
                    indices[3] = 2;
                    indices[4] = 1;
                    indices[5] = 0;
                    indices[6] = 4;
                    indices[7] = 5;
                    indices[8] = 6;
                    indices[9] = 6;
                    indices[10] = 7;
                    indices[11] = 4;
                } else {
                    indices[0] = 0;
                    indices[1] = 1;
                    indices[2] = 2;
                    indices[3] = 2;
                    indices[4] = 3;
                    indices[5] = 0;
                    indices[6] = 4;
                    indices[7] = 7;
                    indices[8] = 6;
                    indices[9] = 6;
                    indices[10] = 5;
                    indices[11] = 4;
                }
            } else {
                if (PGData.globalInvertNext) {
                    indices[0] = 0;
                    indices[1] = 1;
                    indices[2] = 2;
                    indices[3] = 2;
                    indices[4] = 3;
                    indices[5] = 0;
                    indices[6] = 4;
                    indices[7] = 7;
                    indices[8] = 6;
                    indices[9] = 6;
                    indices[10] = 5;
                    indices[11] = 4;
                } else {
                    indices[0] = 0;
                    indices[1] = 3;
                    indices[2] = 2;
                    indices[3] = 2;
                    indices[4] = 1;
                    indices[5] = 0;
                    indices[6] = 4;
                    indices[7] = 5;
                    indices[8] = 6;
                    indices[9] = 6;
                    indices[10] = 7;
                    indices[11] = 4;
                }
            }
        case BFC.CW:
            if (indices[1] == 0) {
                if (PGData.globalNegativeDeterminant) {
                    if (PGData.globalInvertNext) {
                        indices[0] = 0;
                        indices[1] = 3;
                        indices[2] = 2;
                        indices[3] = 2;
                        indices[4] = 1;
                        indices[5] = 0;
                        indices[6] = 4;
                        indices[7] = 5;
                        indices[8] = 6;
                        indices[9] = 6;
                        indices[10] = 7;
                        indices[11] = 4;
                    } else {
                        indices[0] = 0;
                        indices[1] = 3;
                        indices[2] = 2;
                        indices[3] = 2;
                        indices[4] = 1;
                        indices[5] = 0;
                        indices[6] = 4;
                        indices[7] = 5;
                        indices[8] = 6;
                        indices[9] = 6;
                        indices[10] = 7;
                        indices[11] = 4;
                    }
                } else {
                    if (PGData.globalInvertNext) {                    
                        indices[0] = 0;
                        indices[1] = 3;
                        indices[2] = 2;
                        indices[3] = 2;
                        indices[4] = 1;
                        indices[5] = 0;
                        indices[6] = 4;
                        indices[7] = 5;
                        indices[8] = 6;
                        indices[9] = 6;
                        indices[10] = 7;
                        indices[11] = 4;
                    } else {
                        indices[0] = 0;
                        indices[1] = 1;
                        indices[2] = 2;
                        indices[3] = 2;
                        indices[4] = 3;
                        indices[5] = 0;
                        indices[6] = 4;
                        indices[7] = 7;
                        indices[8] = 6;
                        indices[9] = 6;
                        indices[10] = 5;
                        indices[11] = 4;
                    }
                }
            }
            vertices[3] = View.BFC_front_Colour_r[0];
            vertices[4] = View.BFC_front_Colour_g[0];
            vertices[5] = View.BFC_front_Colour_b[0];
            vertices[9] = vertices[3];
            vertices[10] = vertices[4];
            vertices[11] = vertices[5];
            vertices[15] = vertices[3];
            vertices[16] = vertices[4];
            vertices[17] = vertices[5];                                        
            vertices[21] = vertices[3];
            vertices[22] = vertices[4];
            vertices[23] = vertices[5];     
            vertices[27] = View.BFC_back__Colour_r[0];
            vertices[28] = View.BFC_back__Colour_g[0];
            vertices[29] = View.BFC_back__Colour_b[0];               
            vertices[33] = vertices[27];
            vertices[34] = vertices[28];
            vertices[35] = vertices[29];
            vertices[39] = vertices[27];
            vertices[40] = vertices[28];
            vertices[41] = vertices[29];
            vertices[45] = vertices[27];
            vertices[46] = vertices[28];
            vertices[47] = vertices[29];
            GL33HelperPrimitives.drawTrianglesIndexedRGB_Quad(vertices, indices);
            break;
        case BFC.NOCERTIFY:
            indices[0] = 0;
            indices[1] = 2;
            indices[2] = 1;
            indices[3] = 3;
            indices[4] = 4;
            indices[5] = 5;
            indices[6] = 0;
            indices[7] = 2;
            indices[8] = 1;
            indices[9] = 3;
            indices[10] = 4;
            indices[11] = 5;
            vertices[3] = View.BFC_uncertified_Colour_r[0];
            vertices[4] = View.BFC_uncertified_Colour_g[0];
            vertices[5] = View.BFC_uncertified_Colour_b[0];
            vertices[9] = vertices[3];
            vertices[10] = vertices[4];
            vertices[11] = vertices[5];
            vertices[15] = vertices[3];
            vertices[16] = vertices[4];
            vertices[17] = vertices[5];                                        
            vertices[21] = vertices[3];
            vertices[22] = vertices[4];
            vertices[23] = vertices[5];                    
            vertices[27] = vertices[3];
            vertices[28] = vertices[4];
            vertices[29] = vertices[5];                    
            vertices[33] = vertices[3];
            vertices[34] = vertices[4];
            vertices[35] = vertices[5];
            vertices[39] = vertices[3];
            vertices[40] = vertices[4];
            vertices[41] = vertices[5];
            vertices[45] = vertices[3];
            vertices[46] = vertices[4];
            vertices[47] = vertices[5];
            GL33HelperPrimitives.drawTrianglesIndexedRGB_Quad(vertices, indices);
            break;
        case BFC.NOCLIP:
            indices[0] = 0;
            indices[1] = 2;
            indices[2] = 1;
            indices[3] = 3;
            indices[4] = 4;
            indices[5] = 5;
            indices[6] = 0;
            indices[7] = 2;
            indices[8] = 1;
            indices[9] = 3;
            indices[10] = 4;
            indices[11] = 5;
            vertices[3] = View.BFC_front_Colour_r[0];
            vertices[4] = View.BFC_front_Colour_g[0];
            vertices[5] = View.BFC_front_Colour_b[0];
            vertices[9] = vertices[3];
            vertices[10] = vertices[4];
            vertices[11] = vertices[5];
            vertices[15] = vertices[3];
            vertices[16] = vertices[4];
            vertices[17] = vertices[5];                                        
            vertices[21] = vertices[3];
            vertices[22] = vertices[4];
            vertices[23] = vertices[5];                    
            vertices[27] = vertices[3];
            vertices[28] = vertices[4];
            vertices[29] = vertices[5];                    
            vertices[33] = vertices[3];            
            vertices[34] = vertices[4];
            vertices[35] = vertices[5];
            vertices[39] = vertices[3];
            vertices[40] = vertices[4];
            vertices[41] = vertices[5];
            vertices[45] = vertices[3];
            vertices[46] = vertices[4];
            vertices[47] = vertices[5];
            GL33HelperPrimitives.drawTrianglesIndexedRGB_Quad(vertices, indices);
        }

    }
    public static PGData4 clone(PGData4 o) {
        return new PGData4(o.x1, o.y1, o.z1, o.x2, o.y2, o.z2, o.x3, o.y3, o.z3, o.x4, o.y4, o.z4);
    }

    @Override
    public PGData data() {
        return this;
    }
}