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
import org.lwjgl.opengl.GL20;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.opengl.GL33Helper;
import org.nschmidt.ldparteditor.opengl.GL33HelperPrimitives;
import org.nschmidt.ldparteditor.opengl.GLMatrixStack;
import org.nschmidt.ldparteditor.opengl.GLShader;

/**
 * @author nils
 *
 */
public final class PGData3 extends PGData implements Serializable {

    private static final long serialVersionUID = 1L;

    public final float x1;
    public final float y1;
    final float z1;
    public final float x2;
    public final float y2;
    final float z2;
    public final float x3;
    public final float y3;
    final float z3;
    
    private final transient int[] indices = new int[6];
    private final transient float[] vertices = new float[36];
    
    public PGData3(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3) {
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
        this.x3 = x3;
        this.y3 = y3;
        this.z3 = z3;
        
        vertices[0] = x1;
        vertices[1] = y1;
        vertices[2] = z1;
        vertices[6] = x2;
        vertices[7] = y2;
        vertices[8] = z2;
        vertices[12] = x3;
        vertices[13] = y3;
        vertices[14] = z3;
        vertices[18] = x1;
        vertices[19] = y1;
        vertices[20] = z1;
        vertices[24] = x2;
        vertices[25] = y2;
        vertices[26] = z2;
        vertices[30] = x3;
        vertices[31] = y3;
        vertices[32] = z3;
    }
    @Override
    public int type() {
        return 3;
    }
    @Override
    public void drawBFCprimitive_GL20(int drawOnlyMode) {
        if (drawOnlyMode == 2) return;
        switch (PGData.accumClip > 0 ? BFC.NOCLIP : PGData.localWinding) {
        case BFC.CCW:
            if (PGData.globalNegativeDeterminant) {
                if (PGData.globalInvertNext) {
                    GL11.glColor4f(View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], 1f);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glColor4f(View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], 1f);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], 1f);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glColor4f(View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], 1f);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glEnd();
                }
            } else {
                if (PGData.globalInvertNext) {
                    GL11.glColor4f(View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], 1f);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glColor4f(View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], 1f);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], 1f);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glColor4f(View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], 1f);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glEnd();
                }
            }
            break;
        case BFC.CW:
            if (PGData.globalNegativeDeterminant) {
                if (PGData.globalInvertNext) {
                    GL11.glColor4f(View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], 1f);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glColor4f(View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], 1f);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], 1f);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glColor4f(View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], 1f);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glEnd();
                }
            } else {
                if (PGData.globalInvertNext) {
                    GL11.glColor4f(View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], 1f);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glColor4f(View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], 1f);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], 1f);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glColor4f(View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], 1f);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glEnd();
                }
            }
            break;
        case BFC.NOCERTIFY:
            GL11.glColor4f(View.BFC_uncertified_Colour_r[0], View.BFC_uncertified_Colour_g[0], View.BFC_uncertified_Colour_b[0], 1f);
            GL11.glBegin(GL11.GL_TRIANGLES);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glEnd();
            break;
        case BFC.NOCLIP:
            GL11.glColor4f(View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], 1f);
            GL11.glBegin(GL11.GL_TRIANGLES);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glEnd();
        }
    }
    
    @Override
    public void drawBFCprimitive_GL33(GLMatrixStack stack, int drawOnlyMode) {
        if (drawOnlyMode == 2) return;
        switch (PGData.accumClip > 0 ? BFC.NOCLIP : PGData.localWinding) {
        case BFC.CCW:
            if (PGData.globalNegativeDeterminant) {
                if (PGData.globalInvertNext) {                    
                    indices[0] = 0;
                    indices[1] = 2;
                    indices[2] = 1;
                    indices[3] = 3;
                    indices[4] = 4;
                    indices[5] = 5;
                } else {
                    indices[0] = 0;
                    indices[1] = 1;
                    indices[2] = 2;
                    indices[3] = 3;
                    indices[4] = 5;
                    indices[5] = 4;
                }
            } else {
                if (PGData.globalInvertNext) {
                    indices[0] = 0;
                    indices[1] = 1;
                    indices[2] = 2;
                    indices[3] = 3;
                    indices[4] = 5;
                    indices[5] = 4;
                } else {
                    indices[0] = 0;
                    indices[1] = 2;
                    indices[2] = 1;
                    indices[3] = 3;
                    indices[4] = 4;
                    indices[5] = 5;
                }
            }
        case BFC.CW:
            if (indices[1] == 0) {
                if (PGData.globalNegativeDeterminant) {
                    if (PGData.globalInvertNext) {
                        indices[0] = 0;
                        indices[1] = 1;
                        indices[2] = 2;
                        indices[3] = 3;
                        indices[4] = 5;
                        indices[5] = 4;
                    } else {
                        indices[0] = 0;
                        indices[1] = 2;
                        indices[2] = 1;
                        indices[3] = 3;
                        indices[4] = 4;
                        indices[5] = 5;
                    }
                } else {
                    if (PGData.globalInvertNext) {                    
                        indices[0] = 0;
                        indices[1] = 2;
                        indices[2] = 1;
                        indices[3] = 3;
                        indices[4] = 4;
                        indices[5] = 5;
                    } else {
                        indices[0] = 0;
                        indices[1] = 1;
                        indices[2] = 2;
                        indices[3] = 3;
                        indices[4] = 5;
                        indices[5] = 4;
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
            vertices[21] = View.BFC_back__Colour_r[0];
            vertices[22] = View.BFC_back__Colour_g[0];
            vertices[23] = View.BFC_back__Colour_b[0];                    
            vertices[27] = vertices[21];
            vertices[28] = vertices[22];
            vertices[29] = vertices[23];                    
            vertices[33] = vertices[21];
            vertices[34] = vertices[22];
            vertices[35] = vertices[23];
            GL33HelperPrimitives.drawTrianglesIndexedRGB_Triangle(vertices, indices);
            break;
        case BFC.NOCERTIFY:
            indices[0] = 0;
            indices[1] = 2;
            indices[2] = 1;
            indices[3] = 3;
            indices[4] = 4;
            indices[5] = 5;
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
            GL33HelperPrimitives.drawTrianglesIndexedRGB_Triangle(vertices, indices);
            break;
        case BFC.NOCLIP:
            indices[0] = 0;
            indices[1] = 1;
            indices[2] = 2;
            indices[3] = 3;
            indices[4] = 5;
            indices[5] = 4;
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
            GL33HelperPrimitives.drawTrianglesIndexedRGB_Triangle(vertices, indices);
        }
    }
    
    
    public static void beginDrawText() {
        GL11.glColor4f(View.text_Colour_r[0], View.text_Colour_g[0], View.text_Colour_b[0], 1f);
        GL11.glBegin(GL11.GL_TRIANGLES);
    }
    public void drawText(float x, float y, float z) {
        GL11.glVertex3f(-x1 + x, y1 + y, z1 + z);
        GL11.glVertex3f(-x3 + x, y3 + y, z3 + z);
        GL11.glVertex3f(-x2 + x, y2 + y, z2 + z);
    }
    public void drawText(float x, float y, float z, float scale) {
        GL11.glVertex3f(-x1 * scale + x, y1 * scale + y, z1 * scale + z);
        GL11.glVertex3f(-x3 * scale + x, y3 * scale + y, z3 * scale + z);
        GL11.glVertex3f(-x2 * scale + x, y2 * scale + y, z2 * scale + z);
    }
    public static void endDrawText() {
        GL11.glEnd();
    }
    
    public static void beginDrawTextGL33(GLShader shader) {
        shader.use();
        final int colour = shader.getUniformLocation("color"); //$NON-NLS-1$
        GL20.glUniform3f(colour, View.text_Colour_r[0], View.text_Colour_g[0], View.text_Colour_b[0]);
    }
    public void drawTextGL33(float x, float y, float z) {
        GL33Helper.drawTriangle_GeneralSlow(new float[]{
            -x1 + x, y1 + y, z1 + z,
            -x3 + x, y3 + y, z3 + z,
            -x2 + x, y2 + y, z2 + z
        });
    }
    public static void endDrawTextGL33(GLShader shader) {
        shader.use();  
    }
    public static PGData3 clone(PGData3 o) {
        return new PGData3(o.x1, o.y1, o.z1, o.x2, o.y2, o.z2, o.x3, o.y3, o.z3);
    }
    @Override
    public PGData data() {
        return this;
    }
}