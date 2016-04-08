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
    }
    @Override
    public int type() {
        return 3;
    }
    @Override
    public void drawBFCprimitive(int drawOnlyMode) {
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
            GL11.glColor4f(View.BFC_uncertified__Colour_r[0], View.BFC_uncertified__Colour_g[0], View.BFC_uncertified__Colour_b[0], 1f);
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
    public static void beginDrawText() {
        GL11.glColor4f(View.text_Colour_r[0], View.text_Colour_g[0], View.text_Colour_b[0], 1f);
        GL11.glBegin(GL11.GL_TRIANGLES);
    }
    public void drawText(float x, float y, float z) {
        GL11.glVertex3f(-x1 + x, y1 + y, z1 + z);
        GL11.glVertex3f(-x3 + x, y3 + y, z3 + z);
        GL11.glVertex3f(-x2 + x, y2 + y, z2 + z);
    }
    public static void endDrawText() {
        GL11.glEnd();
    }
    public static PGData3 clone(PGData3 o) {
        return new PGData3(o.x1, o.y1, o.z1, o.x2, o.y2, o.z2, o.x3, o.y3, o.z3);
    }
    @Override
    public PGData data() {
        return this;
    }
}