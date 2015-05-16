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

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;

/**
 * @author nils
 *
 */
public final class GData3 extends GData {

    public final int colourNumber;

    public final float r;
    public final float g;
    public final float b;
    public final float a;

    final float x1;
    final float y1;
    final float z1;

    final float x2;
    final float y2;
    final float z2;

    final float x3;
    final float y3;
    final float z3;

    final BigDecimal X1;
    final BigDecimal Y1;
    final BigDecimal Z1;
    final BigDecimal X2;
    final BigDecimal Y2;
    final BigDecimal Z2;
    final BigDecimal X3;
    final BigDecimal Y3;
    final BigDecimal Z3;

    public final float xn;
    public final float yn;
    public final float zn;
    public final GData1 parent;

    public GData3(final int colourNumber, float r, float g, float b, float a, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, GData1 parent, DatFile datFile) {
        this.colourNumber = colourNumber;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.x1 = x1 * 1000f;
        this.y1 = y1 * 1000f;
        this.z1 = z1 * 1000f;
        this.x2 = x2 * 1000f;
        this.y2 = y2 * 1000f;
        this.z2 = z2 * 1000f;
        this.x3 = x3 * 1000f;
        this.y3 = y3 * 1000f;
        this.z3 = z3 * 1000f;
        this.xn = (y3 - y1) * (z2 - z1) - (z3 - z1) * (y2 - y1);
        this.yn = (z3 - z1) * (x2 - x1) - (x3 - x1) * (z2 - z1);
        this.zn = (x3 - x1) * (y2 - y1) - (y3 - y1) * (x2 - x1);
        this.parent = parent;
        datFile.getVertexManager().add(this);
        this.X1 = new BigDecimal(x1);
        this.Y1 = new BigDecimal(y1);
        this.Z1 = new BigDecimal(z1);
        this.X2 = new BigDecimal(x2);
        this.Y2 = new BigDecimal(y2);
        this.Z2 = new BigDecimal(z2);
        this.X3 = new BigDecimal(x3);
        this.Y3 = new BigDecimal(y3);
        this.Z3 = new BigDecimal(z3);
    }

    public GData3(final int colourNumber, float r, float g, float b, float a, BigDecimal x1, BigDecimal y1, BigDecimal z1, BigDecimal x2, BigDecimal y2, BigDecimal z2, BigDecimal x3, BigDecimal y3,
            BigDecimal z3, GData1 parent, DatFile datFile) {

        this.colourNumber = colourNumber;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        final float tx1 = x1.floatValue();
        final float ty1 = y1.floatValue();
        final float tz1 = z1.floatValue();
        final float tx2 = x2.floatValue();
        final float ty2 = y2.floatValue();
        final float tz2 = z2.floatValue();
        final float tx3 = x3.floatValue();
        final float ty3 = y3.floatValue();
        final float tz3 = z3.floatValue();
        this.xn = (ty3 - ty1) * (tz2 - tz1) - (tz3 - tz1) * (ty2 - ty1);
        this.yn = (tz3 - tz1) * (tx2 - tx1) - (tx3 - tx1) * (tz2 - tz1);
        this.zn = (tx3 - tx1) * (ty2 - ty1) - (ty3 - ty1) * (tx2 - tx1);
        this.X1 = x1;
        this.Y1 = y1;
        this.Z1 = z1;
        this.X2 = x2;
        this.Y2 = y2;
        this.Z2 = z2;
        this.X3 = x3;
        this.Y3 = y3;
        this.Z3 = z3;
        this.x1 = X1.floatValue() * 1000f;
        this.y1 = Y1.floatValue() * 1000f;
        this.z1 = Z1.floatValue() * 1000f;
        this.x2 = X2.floatValue() * 1000f;
        this.y2 = Y2.floatValue() * 1000f;
        this.z2 = Z2.floatValue() * 1000f;
        this.x3 = X3.floatValue() * 1000f;
        this.y3 = Y3.floatValue() * 1000f;
        this.z3 = Z3.floatValue() * 1000f;
        this.parent = parent;
        datFile.getVertexManager().add(this);

    }

    public GData3(final int colourNumber, float r, float g, float b, float a, Vertex v1, Vertex v2, Vertex v3, GData1 parent, DatFile datFile) {

        this.colourNumber = colourNumber;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.x1 = v1.x;
        this.y1 = v1.y;
        this.z1 = v1.z;
        this.x2 = v2.x;
        this.y2 = v2.y;
        this.z2 = v2.z;
        this.x3 = v3.x;
        this.y3 = v3.y;
        this.z3 = v3.z;
        this.X1 = v1.X;
        this.Y1 = v1.Y;
        this.Z1 = v1.Z;
        this.X2 = v2.X;
        this.Y2 = v2.Y;
        this.Z2 = v2.Z;
        this.X3 = v3.X;
        this.Y3 = v3.Y;
        this.Z3 = v3.Z;
        this.xn = (y3 - y1) * (z2 - z1) - (z3 - z1) * (y2 - y1);
        this.yn = (z3 - z1) * (x2 - x1) - (x3 - x1) * (z2 - z1);
        this.zn = (x3 - x1) * (y2 - y1) - (y3 - y1) * (x2 - x1);
        this.parent = parent;
        datFile.getVertexManager().add(this);
    }

    /**
     * FOR CSG, TEXMAP AND Cut, Copy, Paste ONLY!
     *
     * @param v1
     * @param v2
     * @param v3
     * @param parent
     * @param c
     */
    public GData3(Vertex v1, Vertex v2, Vertex v3, GData1 parent, GColour c) {

        this.colourNumber = c.getColourNumber();
        this.r = c.getR();
        this.g = c.getG();
        this.b = c.getB();
        this.a = c.getA();
        this.x1 = v1.x;
        this.y1 = v1.y;
        this.z1 = v1.z;
        this.x2 = v2.x;
        this.y2 = v2.y;
        this.z2 = v2.z;
        this.x3 = v3.x;
        this.y3 = v3.y;
        this.z3 = v3.z;
        this.X1 = v1.X;
        this.Y1 = v1.Y;
        this.Z1 = v1.Z;
        this.X2 = v2.X;
        this.Y2 = v2.Y;
        this.Z2 = v2.Z;
        this.X3 = v3.X;
        this.Y3 = v3.Y;
        this.Z3 = v3.Z;
        this.xn = (y3 - y1) * (z2 - z1) - (z3 - z1) * (y2 - y1);
        this.yn = (z3 - z1) * (x2 - x1) - (x3 - x1) * (z2 - z1);
        this.zn = (x3 - x1) * (y2 - y1) - (y3 - y1) * (x2 - x1);
        this.parent = parent;
    }

    public GData3(final int colourNumber, float r, float g, float b, float a, BigDecimal x1, BigDecimal y1, BigDecimal z1, BigDecimal x2, BigDecimal y2, BigDecimal z2, BigDecimal x3, BigDecimal y3,
            BigDecimal z3, float x12, float y12, float z12, float x22, float y22, float z22, float x32, float y32, float z32, float xn, float yn, float zn, GData1 parent, DatFile datFile) {

        this.colourNumber = colourNumber;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.X1 = x1;
        this.Y1 = y1;
        this.Z1 = z1;
        this.X2 = x2;
        this.Y2 = y2;
        this.Z2 = z2;
        this.X3 = x3;
        this.Y3 = y3;
        this.Z3 = z3;
        this.x1 = x12;
        this.y1 = y12;
        this.z1 = z12;
        this.x2 = x22;
        this.y2 = y22;
        this.z2 = z22;
        this.x3 = x32;
        this.y3 = y32;
        this.z3 = z32;
        this.xn = xn;
        this.yn = yn;
        this.zn = zn;
        this.parent = parent;
        datFile.getVertexManager().add(this);

    }

    public Vector4f getNormal() {
        Vector4f vn = new Vector4f(xn, yn, zn, 0f);
        vn.normalise();
        vn.setW(1f);
        return vn;
    }

    @Override
    public void draw(Composite3D c3d) {
        if (!visible)
            return;
        if (a < 1f && c3d.isDrawingSolidMaterials() || !c3d.isDrawingSolidMaterials() && a == 1f)
            return;
        if (GData.globalNegativeDeterminant) {
            GL11.glColor4f(r, g, b, a);
            GL11.glBegin(GL11.GL_TRIANGLES);
            GL11.glNormal3f(xn, yn, zn);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glNormal3f(-xn, -yn, -zn);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glEnd();
        } else {
            GL11.glColor4f(r, g, b, a);
            GL11.glBegin(GL11.GL_TRIANGLES);
            GL11.glNormal3f(xn, yn, zn);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glNormal3f(-xn, -yn, -zn);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glEnd();
        }
    }

    @Override
    public void drawRandomColours(Composite3D c3d) {
        if (!visible)
            return;
        if (a < 1f && c3d.isDrawingSolidMaterials() || !c3d.isDrawingSolidMaterials() && a == 1f)
            return;
        final float r = MathHelper.randomFloat(ID, 0);
        final float g = MathHelper.randomFloat(ID, 1);
        final float b = MathHelper.randomFloat(ID, 2);
        if (GData.globalNegativeDeterminant) {
            GL11.glColor4f(r, g, b, a);
            GL11.glBegin(GL11.GL_TRIANGLES);
            GL11.glNormal3f(xn, yn, zn);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glNormal3f(-xn, -yn, -zn);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glEnd();
        } else {
            GL11.glColor4f(r, g, b, a);
            GL11.glBegin(GL11.GL_TRIANGLES);
            GL11.glNormal3f(xn, yn, zn);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glNormal3f(-xn, -yn, -zn);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glEnd();
        }
    }

    @Override
    public void drawBFC(Composite3D c3d) {
        if (!visible)
            return;
        if (a < 1f && c3d.isDrawingSolidMaterials() || !c3d.isDrawingSolidMaterials() && a == 1f)
            return;
        switch (GData.localWinding) {
        case BFC.CCW:
            if (GData.globalNegativeDeterminant) {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glColor4f(View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], a);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glColor4f(View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], a);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glEnd();
                }
            } else {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glColor4f(View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], a);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glColor4f(View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], a);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glEnd();
                }
            }
            break;
        case BFC.CW:
            if (GData.globalNegativeDeterminant) {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glColor4f(View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], a);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glColor4f(View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], a);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glEnd();
                }
            } else {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glColor4f(View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], a);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glColor4f(View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], a);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glEnd();
                }
            }
            break;
        case BFC.NOCERTIFY:
            if (GData.globalNegativeDeterminant) {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(View.BFC_uncertified__Colour_r[0], View.BFC_uncertified__Colour_g[0], View.BFC_uncertified__Colour_b[0], a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(View.BFC_uncertified__Colour_r[0], View.BFC_uncertified__Colour_g[0], View.BFC_uncertified__Colour_b[0], a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glEnd();
                }
            } else {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(View.BFC_uncertified__Colour_r[0], View.BFC_uncertified__Colour_g[0], View.BFC_uncertified__Colour_b[0], a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(View.BFC_uncertified__Colour_r[0], View.BFC_uncertified__Colour_g[0], View.BFC_uncertified__Colour_b[0], a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glEnd();
                }
            }
        }
    }

    @Override
    public void drawBFCuncertified(Composite3D c3d) {
        if (!visible)
            return;
        if (a < 1f && c3d.isDrawingSolidMaterials() || !c3d.isDrawingSolidMaterials() && a == 1f)
            return;
        if (GData.globalNegativeDeterminant) {
            GL11.glColor4f(View.BFC_uncertified__Colour_r[0], View.BFC_uncertified__Colour_g[0], View.BFC_uncertified__Colour_b[0], a);
            GL11.glBegin(GL11.GL_TRIANGLES);
            GL11.glNormal3f(xn, yn, zn);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glNormal3f(-xn, -yn, -zn);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glEnd();
        } else {
            GL11.glColor4f(View.BFC_uncertified__Colour_r[0], View.BFC_uncertified__Colour_g[0], View.BFC_uncertified__Colour_b[0], a);
            GL11.glBegin(GL11.GL_TRIANGLES);
            GL11.glNormal3f(xn, yn, zn);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glNormal3f(-xn, -yn, -zn);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glEnd();
        }
    }

    @Override
    public void drawBFC_backOnly(Composite3D c3d) {
        if (!visible)
            return;
        if (a < 1f && c3d.isDrawingSolidMaterials() || !c3d.isDrawingSolidMaterials() && a == 1f)
            return;
        switch (GData.localWinding) {
        case BFC.CCW:
            if (GData.globalNegativeDeterminant) {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glColor4f(View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], a);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glColor4f(View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], a);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glEnd();
                }
            } else {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glColor4f(View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], a);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glColor4f(View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], a);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glEnd();
                }
            }
            break;
        case BFC.CW:
            if (GData.globalNegativeDeterminant) {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glColor4f(View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], a);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glColor4f(View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], a);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glEnd();
                }
            } else {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glColor4f(View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], a);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glColor4f(View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], a);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glEnd();
                }
            }
            break;
        case BFC.NOCERTIFY:
            if (GData.globalNegativeDeterminant) {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(View.BFC_uncertified__Colour_r[0], View.BFC_uncertified__Colour_g[0], View.BFC_uncertified__Colour_b[0], a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(View.BFC_uncertified__Colour_r[0], View.BFC_uncertified__Colour_g[0], View.BFC_uncertified__Colour_b[0], a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glEnd();
                }
            } else {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(View.BFC_uncertified__Colour_r[0], View.BFC_uncertified__Colour_g[0], View.BFC_uncertified__Colour_b[0], a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(View.BFC_uncertified__Colour_r[0], View.BFC_uncertified__Colour_g[0], View.BFC_uncertified__Colour_b[0], a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glEnd();
                }
            }
        }
    }

    @Override
    public void drawBFC_Colour(Composite3D c3d) {
        if (!visible)
            return;
        if (a < 1f && c3d.isDrawingSolidMaterials() || !c3d.isDrawingSolidMaterials() && a == 1f)
            return;
        switch (a < 1f ? BFC.NOCERTIFY : GData.localWinding) {
        case BFC.CCW:
            if (GData.globalNegativeDeterminant) {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glEnd();
                }
            } else {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glEnd();
                }
            }
            break;
        case BFC.CW:
            if (GData.globalNegativeDeterminant) {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glEnd();
                }
            } else {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glEnd();
                }
            }
            break;
        case BFC.NOCERTIFY:
            if (GData.globalNegativeDeterminant) {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glEnd();
                }
            } else {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glEnd();
                }
            }
        }
    }

    private void drawBFC_Colour2(Composite3D c3d, float r, float g, float b, float a, int useCubeMap) {
        if (!visible)
            return;
        if (a < 1f && c3d.isDrawingSolidMaterials() || !c3d.isDrawingSolidMaterials() && a == 1f)
            return;
        switch (a < 1f ? BFC.NOCERTIFY : GData.localWinding) {
        case BFC.CCW:
            if (GData.globalNegativeDeterminant) {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, true, this, useCubeMap);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, true, this, useCubeMap);
                    GL11.glEnd();
                }
            } else {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, true, this, useCubeMap);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, true, this, useCubeMap);
                    GL11.glEnd();
                }
            }
            break;
        case BFC.CW:
            if (GData.globalNegativeDeterminant) {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, false, this, useCubeMap);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    GL11.glNormal3f(xn, yn, zn);
                    c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, false, this, useCubeMap);
                    GL11.glEnd();
                }
            } else {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, false, this, useCubeMap);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, false, this, useCubeMap);
                    GL11.glEnd();
                }
            }
            break;
        case BFC.NOCERTIFY:
            if (GData.globalNegativeDeterminant) {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, false, this, useCubeMap);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, true, this, useCubeMap);
                    GL11.glEnd();
                }
            } else {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, false, this, useCubeMap);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, true, this, useCubeMap);
                    GL11.glEnd();
                }
            }
        }
    }

    @Override
    public void drawBFC_Textured(Composite3D c3d) {
        if (GData.globalDrawObjects) {
            GColour c = View.getLDConfigColour(View.getLDConfigIndex(r, g, b));
            GColourType ct = c.getType();
            boolean hasColourType = ct != null;
            boolean matLight = true;
            int useCubeMap = 0;
            if (hasColourType) {
                switch (ct.type()) {
                case CHROME:
                    useCubeMap = 1;
                    break;
                case MATTE_METALLIC:
                    useCubeMap = 2;
                    break;
                case METAL:
                    useCubeMap = 3;
                    break;
                case RUBBER:
                    useCubeMap = 2;
                    matLight = false;
                    break;
                default:
                    break;
                }
            }
            float r = this.r;
            float g = this.g;
            float b = this.b;
            float a = this.a;
            if (hasColourType && useCubeMap < 1) {
                a = 0.99f;
            }
            final OpenGLRenderer ren = c3d.getRenderer();
            if (GData.globalTextureStack.isEmpty()) {
                GL20.glUniform1f(ren.getAlphaSwitchLoc(), c3d.isDrawingSolidMaterials() ? 1f : 0f); // Draw transparent
                GL20.glUniform1f(ren.getNormalSwitchLoc(), GData.globalNegativeDeterminant ^ GData.globalInvertNext ? 1f : 0f);
                GL20.glUniform1f(ren.getNoTextureSwitch(), 1f);
                GL20.glUniform1f(ren.getNoLightSwitch(), c3d.isLightOn() && matLight ? 0f : 1f);
                GL20.glUniform1f(ren.getCubeMapSwitch(), useCubeMap);
                switch (GData.accumClip) {
                case 0:
                    drawBFC_Colour2(c3d, r, g, b, a, useCubeMap);
                    break;
                default:
                    byte tmp = GData.localWinding;
                    GData.localWinding = BFC.NOCERTIFY;
                    drawBFC_Colour2(c3d, r, g, b, a, useCubeMap);
                    GData.localWinding = tmp;
                    break;
                }
            } else { // Draw the textured face
                if (!visible)
                    return;
                GTexture tex = GData.globalTextureStack.peek();
                tex.bind(c3d.isDrawingSolidMaterials(), GData.globalNegativeDeterminant ^ GData.globalInvertNext, c3d.isLightOn() && matLight, ren, useCubeMap);
                float[] uv;
                switch (a < 1f || GData.accumClip > 0 ? BFC.NOCERTIFY : GData.localWinding) {
                case BFC.CCW:
                    if (GData.globalNegativeDeterminant) {
                        if (GData.globalInvertNext) {
                            GL11.glColor4f(r, g, b, a);
                            GL11.glBegin(GL11.GL_TRIANGLES);
                            tex.calcUVcoords1(x1, y1, z1, parent, this);
                            tex.calcUVcoords2(x3, y3, z3, parent);
                            tex.calcUVcoords3(x2, y2, z2, parent);
                            uv = tex.getUVcoords(true, this);
                            GL11.glTexCoord2f(uv[0], uv[1]);
                            c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, false, this, useCubeMap);
                            GL11.glTexCoord2f(uv[2], uv[3]);
                            c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, false, this, useCubeMap);
                            GL11.glTexCoord2f(uv[4], uv[5]);
                            c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, false, this, useCubeMap);
                            GL11.glEnd();
                        } else {
                            GL11.glColor4f(r, g, b, a);
                            GL11.glBegin(GL11.GL_TRIANGLES);
                            tex.calcUVcoords1(x1, y1, z1, parent, this);
                            tex.calcUVcoords2(x2, y2, z2, parent);
                            tex.calcUVcoords3(x3, y3, z3, parent);
                            uv = tex.getUVcoords(true, this);
                            GL11.glTexCoord2f(uv[0], uv[1]);
                            c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, true, this, useCubeMap);
                            GL11.glTexCoord2f(uv[2], uv[3]);
                            c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, true, this, useCubeMap);
                            GL11.glTexCoord2f(uv[4], uv[5]);
                            c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, true, this, useCubeMap);
                            GL11.glEnd();
                        }
                    } else {
                        if (GData.globalInvertNext) {
                            GL11.glColor4f(r, g, b, a);
                            GL11.glBegin(GL11.GL_TRIANGLES);
                            tex.calcUVcoords1(x1, y1, z1, parent, this);
                            tex.calcUVcoords2(x2, y2, z2, parent);
                            tex.calcUVcoords3(x3, y3, z3, parent);
                            uv = tex.getUVcoords(true, this);
                            GL11.glTexCoord2f(uv[0], uv[1]);
                            c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, false, this, useCubeMap);
                            GL11.glTexCoord2f(uv[2], uv[3]);
                            c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, false, this, useCubeMap);
                            GL11.glTexCoord2f(uv[4], uv[5]);
                            c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, false, this, useCubeMap);
                            GL11.glEnd();
                        } else {
                            GL11.glColor4f(r, g, b, a);
                            GL11.glBegin(GL11.GL_TRIANGLES);
                            tex.calcUVcoords1(x1, y1, z1, parent, this);
                            tex.calcUVcoords2(x3, y3, z3, parent);
                            tex.calcUVcoords3(x2, y2, z2, parent);
                            uv = tex.getUVcoords(true, this);
                            GL11.glTexCoord2f(uv[0], uv[1]);
                            c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, true, this, useCubeMap);
                            GL11.glTexCoord2f(uv[2], uv[3]);
                            c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, true, this, useCubeMap);
                            GL11.glTexCoord2f(uv[4], uv[5]);
                            c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, true, this, useCubeMap);
                            GL11.glEnd();
                        }
                    }
                    break;
                case BFC.CW:
                    if (GData.globalNegativeDeterminant) {
                        if (GData.globalInvertNext) {
                            GL11.glColor4f(r, g, b, a);
                            GL11.glBegin(GL11.GL_TRIANGLES);
                            tex.calcUVcoords1(x1, y1, z1, parent, this);
                            tex.calcUVcoords2(x2, y2, z2, parent);
                            tex.calcUVcoords3(x3, y3, z3, parent);
                            uv = tex.getUVcoords(true, this);
                            GL11.glTexCoord2f(uv[0], uv[1]);
                            c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, true, this, useCubeMap);
                            GL11.glTexCoord2f(uv[2], uv[3]);
                            c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, true, this, useCubeMap);
                            GL11.glTexCoord2f(uv[4], uv[5]);
                            c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, true, this, useCubeMap);
                            GL11.glEnd();
                        } else {
                            GL11.glColor4f(r, g, b, a);
                            GL11.glBegin(GL11.GL_TRIANGLES);
                            tex.calcUVcoords1(x1, y1, z1, parent, this);
                            tex.calcUVcoords2(x3, y3, z3, parent);
                            tex.calcUVcoords3(x2, y2, z2, parent);
                            uv = tex.getUVcoords(true, this);
                            GL11.glTexCoord2f(uv[0], uv[1]);
                            c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, false, this, useCubeMap);
                            GL11.glTexCoord2f(uv[2], uv[3]);
                            c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, false, this, useCubeMap);
                            GL11.glTexCoord2f(uv[4], uv[5]);
                            c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, false, this, useCubeMap);
                            GL11.glEnd();
                        }
                    } else {
                        if (GData.globalInvertNext) {
                            GL11.glColor4f(r, g, b, a);
                            GL11.glBegin(GL11.GL_TRIANGLES);
                            tex.calcUVcoords1(x1, y1, z1, parent, this);
                            tex.calcUVcoords2(x3, y3, z3, parent);
                            tex.calcUVcoords3(x2, y2, z2, parent);
                            uv = tex.getUVcoords(true, this);
                            GL11.glTexCoord2f(uv[0], uv[1]);
                            c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, true, this, useCubeMap);
                            GL11.glTexCoord2f(uv[2], uv[3]);
                            c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, true, this, useCubeMap);
                            GL11.glTexCoord2f(uv[4], uv[5]);
                            c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, true, this, useCubeMap);
                            GL11.glEnd();
                        } else {
                            GL11.glColor4f(r, g, b, a);
                            GL11.glBegin(GL11.GL_TRIANGLES);
                            tex.calcUVcoords1(x1, y1, z1, parent, this);
                            tex.calcUVcoords2(x2, y2, z2, parent);
                            tex.calcUVcoords3(x3, y3, z3, parent);
                            uv = tex.getUVcoords(true, this);
                            GL11.glTexCoord2f(uv[0], uv[1]);
                            c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, false, this, useCubeMap);
                            GL11.glTexCoord2f(uv[2], uv[3]);
                            c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, false, this, useCubeMap);
                            GL11.glTexCoord2f(uv[4], uv[5]);
                            c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, false, this, useCubeMap);
                            GL11.glEnd();
                        }
                    }
                    break;
                case BFC.NOCERTIFY:
                    if (GData.globalNegativeDeterminant) {
                        GL11.glColor4f(r, g, b, a);
                        GL11.glBegin(GL11.GL_TRIANGLES);
                        tex.calcUVcoords1(x1, y1, z1, parent, null);
                        tex.calcUVcoords2(x3, y3, z3, parent);
                        tex.calcUVcoords3(x2, y2, z2, parent);
                        uv = tex.getUVcoords(true, null);
                        GL11.glTexCoord2f(uv[0], uv[1]);
                        c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, false, this, useCubeMap);
                        GL11.glTexCoord2f(uv[2], uv[3]);
                        c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, false, this, useCubeMap);
                        GL11.glTexCoord2f(uv[4], uv[5]);
                        c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, false, this, useCubeMap);
                        GL11.glEnd();
                        GL11.glBegin(GL11.GL_TRIANGLES);
                        tex.calcUVcoords1(x1, y1, z1, parent, null);
                        tex.calcUVcoords2(x2, y2, z2, parent);
                        tex.calcUVcoords3(x3, y3, z3, parent);
                        uv = tex.getUVcoords(true, null);
                        GL11.glTexCoord2f(uv[0], uv[1]);
                        c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, true, this, useCubeMap);
                        GL11.glTexCoord2f(uv[2], uv[3]);
                        c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, true, this, useCubeMap);
                        GL11.glTexCoord2f(uv[4], uv[5]);
                        c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, true, this, useCubeMap);
                        GL11.glEnd();
                    } else {
                        GL11.glColor4f(r, g, b, a);
                        GL11.glBegin(GL11.GL_TRIANGLES);
                        tex.calcUVcoords1(x1, y1, z1, parent, null);
                        tex.calcUVcoords2(x2, y2, z2, parent);
                        tex.calcUVcoords3(x3, y3, z3, parent);
                        uv = tex.getUVcoords(true, null);
                        GL11.glTexCoord2f(uv[0], uv[1]);
                        c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, false, this, useCubeMap);
                        GL11.glTexCoord2f(uv[2], uv[3]);
                        c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, false, this, useCubeMap);
                        GL11.glTexCoord2f(uv[4], uv[5]);
                        c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, false, this, useCubeMap);
                        GL11.glEnd();
                        GL11.glBegin(GL11.GL_TRIANGLES);
                        tex.calcUVcoords1(x1, y1, z1, parent, null);
                        tex.calcUVcoords2(x3, y3, z3, parent);
                        tex.calcUVcoords3(x2, y2, z2, parent);
                        uv = tex.getUVcoords(true, null);
                        GL11.glTexCoord2f(uv[0], uv[1]);
                        c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, true, this, useCubeMap);
                        GL11.glTexCoord2f(uv[2], uv[3]);
                        c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, true, this, useCubeMap);
                        GL11.glTexCoord2f(uv[4], uv[5]);
                        c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, true, this, useCubeMap);
                        GL11.glEnd();
                    }
                }
            }
        }
        if (GData.globalFoundTEXMAPNEXT) {
            GData.globalFoundTEXMAPStack.pop();
            GData.globalTextureStack.pop();
            GData.globalFoundTEXMAPStack.push(false);
            GData.globalFoundTEXMAPNEXT = false;
        }
    }

    @Override
    public void drawWhileAddCondlines(Composite3D c3d) {
        drawBFC(c3d);
    }

    @Override
    public int type() {
        return 3;
    }

    @Override
    String getNiceString() {
        if (text != null)
            return text;
        StringBuilder lineBuilder = new StringBuilder();
        lineBuilder.append(3);
        lineBuilder.append(" "); //$NON-NLS-1$
        if (colourNumber == -1) {
            lineBuilder.append("0x2"); //$NON-NLS-1$
            lineBuilder.append(MathHelper.toHex((int) (255f * r)).toUpperCase());
            lineBuilder.append(MathHelper.toHex((int) (255f * g)).toUpperCase());
            lineBuilder.append(MathHelper.toHex((int) (255f * b)).toUpperCase());
        } else {
            lineBuilder.append(colourNumber);
        }
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(X1));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(Y1));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(Z1));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(X2));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(Y2));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(Z2));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(X3));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(Y3));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(Z3));
        text = lineBuilder.toString();
        return text;
    }

    @Override
    public String inlinedString(byte bfc, GColour colour) {
        return getNiceString();
    }

    @Override
    public String transformAndColourReplace(String colour, Matrix matrix) {
        BigDecimal[] v1;
        BigDecimal[] v2;
        BigDecimal[] v3;
        if (X1 == null) {
            v1 = matrix.transform(new BigDecimal(x1 / 1000f), new BigDecimal(y1 / 1000f), new BigDecimal(z1 / 1000f));
            v2 = matrix.transform(new BigDecimal(x2 / 1000f), new BigDecimal(y2 / 1000f), new BigDecimal(z2 / 1000f));
            v3 = matrix.transform(new BigDecimal(x3 / 1000f), new BigDecimal(y3 / 1000f), new BigDecimal(z3 / 1000f));
        } else {
            v1 = matrix.transform(X1, Y1, Z1);
            v2 = matrix.transform(X2, Y2, Z2);
            v3 = matrix.transform(X3, Y3, Z3);
        }
        StringBuilder lineBuilder = new StringBuilder();
        lineBuilder.append(3);
        lineBuilder.append(" "); //$NON-NLS-1$
        StringBuilder colourBuilder = new StringBuilder();
        if (colourNumber == -1) {
            colourBuilder.append("0x2"); //$NON-NLS-1$
            colourBuilder.append(MathHelper.toHex((int) (255f * r)).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * g)).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * b)).toUpperCase());
        } else {
            colourBuilder.append(colourNumber);
        }
        String col = colourBuilder.toString();
        if (col.equals(colour))
            col = "16"; //$NON-NLS-1$
        lineBuilder.append(col);
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(v1[0]));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(v1[1]));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(v1[2]));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(v2[0]));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(v2[1]));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(v2[2]));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(v3[0]));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(v3[1]));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(v3[2]));
        return lineBuilder.toString();
    }

    @Override
    public void getBFCorientationMap(HashMap<GData, Byte> map) {
        switch (GData.localWinding) {
        case BFC.CCW:
            if (GData.globalNegativeDeterminant) {
                if (GData.globalInvertNext) {
                    map.put(this, BFC.CCW);
                } else {
                    map.put(this, BFC.CW);
                }
            } else {
                if (GData.globalInvertNext) {
                    map.put(this, BFC.CW);
                } else {
                    map.put(this, BFC.CCW);
                }
            }
            break;
        case BFC.CW:
            if (GData.globalNegativeDeterminant) {
                if (GData.globalInvertNext) {
                    map.put(this, BFC.CW);
                } else {
                    map.put(this, BFC.CCW);
                }
            } else {
                if (GData.globalInvertNext) {
                    map.put(this, BFC.CCW);
                } else {
                    map.put(this, BFC.CW);
                }
            }
            break;
        case BFC.NOCERTIFY:
            // map.put(this, BFC.NOCERTIFY);
            break;
        }
    }

    @Override
    public void getBFCorientationMapNOCERTIFY(HashMap<GData, Byte> map) {
        // map.put(this, BFC.NOCERTIFY);
    }

    @Override
    public void getBFCorientationMapNOCLIP(HashMap<GData, Byte> map) {
        map.put(this, BFC.NOCLIP);
    }

    @Override
    public void getVertexNormalMap(TreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, HashMap<GData, float[]> dataLinkedToNormalCACHE, VertexManager vm) {
        if (GData.globalDrawObjects) {
            float[] result = new float[3];
            Vertex[] verts = vm.getTriangles_NOCLONE().get(this);
            if (verts == null) {
                verts = new Vertex[3];
                Vector4f v1 = new Vector4f(x1, y1, z1, 1f);
                Vector4f v2 = new Vector4f(x2, y2, z2, 1f);
                Vector4f v3 = new Vector4f(x3, y3, z3, 1f);
                Matrix4f.transform(this.parent.productMatrix, v1, v1);
                Matrix4f.transform(this.parent.productMatrix, v2, v2);
                Matrix4f.transform(this.parent.productMatrix, v3, v3);
                verts[0] = new Vertex(v1.x , v1.y , v1.z, true);
                verts[1] = new Vertex(v2.x , v2.y , v2.z, true);
                verts[2] = new Vertex(v3.x , v3.y , v3.z, true);
            }

            float xn = (verts[2].y - verts[0].y) * (verts[1].z - verts[0].z) - (verts[2].z - verts[0].z) * (verts[1].y - verts[0].y);
            float yn = (verts[2].z - verts[0].z) * (verts[1].x - verts[0].x) - (verts[2].x - verts[0].x) * (verts[1].z - verts[0].z);
            float zn = (verts[2].x - verts[0].x) * (verts[1].y - verts[0].y) - (verts[2].y - verts[0].y) * (verts[1].x - verts[0].x);

            for (Vertex vertex : verts) {
                switch (GData.localWinding) {
                case BFC.NOCLIP:
                    result[0] = xn;
                    result[1] = yn;
                    result[2] = zn;
                    break;
                case BFC.CCW:
                    if (GData.globalInvertNext) {
                        if (GData.globalNegativeDeterminant) {
                            result[0] = -xn;
                            result[1] = -yn;
                            result[2] = -zn;
                        } else {
                            result[0] = xn;
                            result[1] = yn;
                            result[2] = zn;
                        }
                    } else {
                        if (GData.globalNegativeDeterminant) {
                            result[0] = -xn;
                            result[1] = -yn;
                            result[2] = -zn;
                        } else {
                            result[0] = xn;
                            result[1] = yn;
                            result[2] = zn;
                        }
                    }
                    break;
                case BFC.CW:
                    if (GData.globalInvertNext) {
                        if (GData.globalNegativeDeterminant) {
                            result[0] = -xn;
                            result[1] = -yn;
                            result[2] = -zn;
                        } else {
                            result[0] = xn;
                            result[1] = yn;
                            result[2] = zn;
                        }
                    } else {
                        if (GData.globalNegativeDeterminant) {
                            result[0] = -xn;
                            result[1] = -yn;
                            result[2] = -zn;
                        } else {
                            result[0] = xn;
                            result[1] = yn;
                            result[2] = zn;
                        }
                    }
                    break;
                case BFC.NOCERTIFY:
                    break;
                }
                if (GData.globalInvertNext) {
                    dataLinkedToNormalCACHE.put(this, new float[]{-result[0], -result[1], -result[2]});
                } else {
                    dataLinkedToNormalCACHE.put(this, new float[]{result[0], result[1], result[2]});
                }
                if (!vertexLinkedToNormalCACHE.containsKey(vertex)) {
                    vertexLinkedToNormalCACHE.put(vertex, result);
                } else {
                    float[] n = vertexLinkedToNormalCACHE.get(vertex);
                    n[0] = n[0] + result[0];
                    n[1] = n[1] + result[1];
                    n[2] = n[2] + result[2];
                }
            }
        }
        if (GData.globalFoundTEXMAPNEXT) {
            GData.globalFoundTEXMAPStack.pop();
            GData.globalTextureStack.pop();
            GData.globalFoundTEXMAPStack.push(false);
            GData.globalFoundTEXMAPNEXT = false;
        }
    }

    @Override
    public void getVertexNormalMapNOCERTIFY(TreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, HashMap<GData, float[]> dataLinkedToNormalCACHE, VertexManager vm) {}

    @Override
    public void getVertexNormalMapNOCLIP(TreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, HashMap<GData, float[]> dataLinkedToNormalCACHE, VertexManager vm) {
        getVertexNormalMap(vertexLinkedToNormalCACHE, dataLinkedToNormalCACHE, vm);
    }

    public boolean isCollinear() {
        Vector3d vertexA = new Vector3d(X1, Y1, Z1);
        Vector3d vertexB = new Vector3d(X2, Y2, Z2);
        Vector3d vertexC = new Vector3d(X3, Y3, Z3);
        Vector3d vertexA2 = new Vector3d();
        Vector3d vertexB2 = new Vector3d();
        Vector3d.sub(vertexA, vertexC, vertexA2);
        Vector3d.sub(vertexB, vertexC, vertexB2);
        return Vector3d.angle(vertexA2, vertexB2) < Threshold.collinear_angle_minimum;
    }

    public String colourReplace(String col) {
        StringBuilder lineBuilder = new StringBuilder();
        lineBuilder.append(3);
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(col);
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(X1));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(Y1));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(Z1));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(X2));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(Y2));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(Z2));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(X3));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(Y3));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(Z3));
        return lineBuilder.toString();
    }
}
