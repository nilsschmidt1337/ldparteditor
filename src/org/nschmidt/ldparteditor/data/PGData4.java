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

import org.lwjgl.opengl.GL11;
import org.nschmidt.ldparteditor.enums.View;

/**
 * @author nils
 *
 */
public final class PGData4 extends PGData {
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
    }
    @Override
    public int type() {
        return 4;
    }
    @Override
    public void drawBFCprimitive() {
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
            GL11.glColor4f(View.BFC_uncertified__Colour_r[0], View.BFC_uncertified__Colour_g[0], View.BFC_uncertified__Colour_b[0], 1f);
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
}