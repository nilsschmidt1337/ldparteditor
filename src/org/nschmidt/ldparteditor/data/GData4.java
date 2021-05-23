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

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.composite.Composite3D;
import org.nschmidt.ldparteditor.enumtype.Colour;
import org.nschmidt.ldparteditor.enumtype.LDConfig;
import org.nschmidt.ldparteditor.enumtype.Threshold;
import org.nschmidt.ldparteditor.helper.math.MathHelper;
import org.nschmidt.ldparteditor.helper.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.helper.math.ThreadsafeSortedMap;
import org.nschmidt.ldparteditor.helper.math.Vector3d;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer20;

/**
 * @author nils
 *
 */
public final class GData4 extends GData {

    public final int colourNumber;

    public final float r;
    public final float g;
    public final float b;
    public final float a;

    public final float x1;
    public final float y1;
    public final float z1;

    public final float x2;
    public final float y2;
    public final float z2;

    public final float x3;
    public final float y3;
    public final float z3;

    public final float x4;
    public final float y4;
    public final float z4;

    final BigDecimal x1p;
    final BigDecimal y1p;
    final BigDecimal z1p;
    final BigDecimal x2p;
    final BigDecimal y2p;
    final BigDecimal z2p;
    final BigDecimal x3p;
    final BigDecimal y3p;
    final BigDecimal z3p;
    final BigDecimal x4p;
    final BigDecimal y4p;
    final BigDecimal z4p;

    final float xn;
    final float yn;
    final float zn;

    private double angle = -1;

    public GData4(final int colourNumber, float r, float g, float b, float a, BigDecimal x1, BigDecimal y1, BigDecimal z1, BigDecimal x2, BigDecimal y2, BigDecimal z2, BigDecimal x3, BigDecimal y3,
            BigDecimal z3, BigDecimal x4, BigDecimal y4, BigDecimal z4, Vector3d normal, GData1 parent, DatFile datFile) {

        super(parent);
        this.colourNumber = colourNumber;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.x1p = x1;
        this.y1p = y1;
        this.z1p = z1;
        this.x2p = x2;
        this.y2p = y2;
        this.z2p = z2;
        this.x3p = x3;
        this.y3p = y3;
        this.z3p = z3;
        this.x4p = x4;
        this.y4p = y4;
        this.z4p = z4;
        this.x1 = x1p.floatValue() * 1000f;
        this.y1 = y1p.floatValue() * 1000f;
        this.z1 = z1p.floatValue() * 1000f;
        this.x2 = x2p.floatValue() * 1000f;
        this.y2 = y2p.floatValue() * 1000f;
        this.z2 = z2p.floatValue() * 1000f;
        this.x3 = x3p.floatValue() * 1000f;
        this.y3 = y3p.floatValue() * 1000f;
        this.z3 = z3p.floatValue() * 1000f;
        this.x4 = x4p.floatValue() * 1000f;
        this.y4 = y4p.floatValue() * 1000f;
        this.z4 = z4p.floatValue() * 1000f;
        float txn = -normal.getXf();
        float tyn = -normal.getYf();
        float tzn = -normal.getZf();
        this.xn = txn;
        this.yn = tyn;
        this.zn = tzn;
        datFile.getVertexManager().add(this);
    }

    GData4(final int colourNumber, float r, float g, float b, float a, BigDecimal x1, BigDecimal y1, BigDecimal z1, BigDecimal x2, BigDecimal y2, BigDecimal z2, BigDecimal x3, BigDecimal y3,
            BigDecimal z3, BigDecimal x4, BigDecimal y4, BigDecimal z4, float x12, float y12, float z12, float x22, float y22, float z22, float x32, float y32, float z32, float x42, float y42,
            float z42, float xn, float yn, float zn, GData1 parent, DatFile datFile) {

        super(parent);
        this.colourNumber = colourNumber;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.x1p = x1;
        this.y1p = y1;
        this.z1p = z1;
        this.x2p = x2;
        this.y2p = y2;
        this.z2p = z2;
        this.x3p = x3;
        this.y3p = y3;
        this.z3p = z3;
        this.x4p = x4;
        this.y4p = y4;
        this.z4p = z4;
        this.x1 = x12;
        this.y1 = y12;
        this.z1 = z12;
        this.x2 = x22;
        this.y2 = y22;
        this.z2 = z22;
        this.x3 = x32;
        this.y3 = y32;
        this.z3 = z32;
        this.x4 = x42;
        this.y4 = y42;
        this.z4 = z42;
        this.xn = xn;
        this.yn = yn;
        this.zn = zn;
        datFile.getVertexManager().add(this);
    }

    /**
     * FOR Cut, Copy, Paste and TEXMAP ONLY!
     *
     * @param v1
     * @param v2
     * @param v3
     * @param v4
     * @param xn
     * @param yn
     * @param zn
     * @param parent
     * @param c
     */
    public GData4(Vertex v1, Vertex v2, Vertex v3, Vertex v4, GData1 parent, GColour c) {

        super(parent);
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
        this.x4 = v4.x;
        this.y4 = v4.y;
        this.z4 = v4.z;
        this.x1p = v1.xp;
        this.y1p = v1.yp;
        this.z1p = v1.zp;
        this.x2p = v2.xp;
        this.y2p = v2.yp;
        this.z2p = v2.zp;
        this.x3p = v3.xp;
        this.y3p = v3.yp;
        this.z3p = v3.zp;
        this.x4p = v4.xp;
        this.y4p = v4.yp;
        this.z4p = v4.zp;
        final Vector3f[] normals = new Vector3f[] { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
        {
            final Vector3f[] lineVectors = new Vector3f[] { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
            Vector3f.sub(new Vector3f(v2.x, v2.y, v2.z), new Vector3f(v1.x, v1.y, v1.z), lineVectors[0]);
            Vector3f.sub(new Vector3f(v3.x, v3.y, v3.z), new Vector3f(v2.x, v2.y, v2.z), lineVectors[1]);
            Vector3f.sub(new Vector3f(v4.x, v4.y, v4.z), new Vector3f(v3.x, v3.y, v3.z), lineVectors[2]);
            Vector3f.sub(new Vector3f(v1.x, v1.y, v1.z), new Vector3f(v4.x, v4.y, v4.z), lineVectors[3]);
            Vector3f.cross(lineVectors[0], lineVectors[1], normals[0]);
            Vector3f.cross(lineVectors[1], lineVectors[2], normals[1]);
            Vector3f.cross(lineVectors[2], lineVectors[3], normals[2]);
            Vector3f.cross(lineVectors[3], lineVectors[0], normals[3]);
        }
        Vector3f normal = new Vector3f();
        for (int i = 0; i < 4; i++) {
            Vector3f.add(normals[i], normal, normal);
        }
        this.xn = -normal.x;
        this.yn = -normal.y;
        this.zn = -normal.z;

    }

    GData4(final int colourNumber, float r, float g, float b, float a, Vertex v1, Vertex v2, Vertex v3, Vertex v4, GData1 parent, DatFile datFile) {

        super(parent);
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
        this.x4 = v4.x;
        this.y4 = v4.y;
        this.z4 = v4.z;
        this.x1p = v1.xp;
        this.y1p = v1.yp;
        this.z1p = v1.zp;
        this.x2p = v2.xp;
        this.y2p = v2.yp;
        this.z2p = v2.zp;
        this.x3p = v3.xp;
        this.y3p = v3.yp;
        this.z3p = v3.zp;
        this.x4p = v4.xp;
        this.y4p = v4.yp;
        this.z4p = v4.zp;
        final Vector3f[] normals = new Vector3f[] { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
        {
            final Vector3f[] lineVectors = new Vector3f[] { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
            Vector3f.sub(new Vector3f(v2.x, v2.y, v2.z), new Vector3f(v1.x, v1.y, v1.z), lineVectors[0]);
            Vector3f.sub(new Vector3f(v3.x, v3.y, v3.z), new Vector3f(v2.x, v2.y, v2.z), lineVectors[1]);
            Vector3f.sub(new Vector3f(v4.x, v4.y, v4.z), new Vector3f(v3.x, v3.y, v3.z), lineVectors[2]);
            Vector3f.sub(new Vector3f(v1.x, v1.y, v1.z), new Vector3f(v4.x, v4.y, v4.z), lineVectors[3]);
            Vector3f.cross(lineVectors[0], lineVectors[1], normals[0]);
            Vector3f.cross(lineVectors[1], lineVectors[2], normals[1]);
            Vector3f.cross(lineVectors[2], lineVectors[3], normals[2]);
            Vector3f.cross(lineVectors[3], lineVectors[0], normals[3]);
        }
        Vector3f normal = new Vector3f();
        for (int i = 0; i < 4; i++) {
            Vector3f.add(normals[i], normal, normal);
        }
        this.xn = -normal.x;
        this.yn = -normal.y;
        this.zn = -normal.z;
        datFile.getVertexManager().add(this);
    }

    @Override
    public void drawGL20(Composite3D c3d) {
        if (!visible)
            return;
        if (a < 1f && c3d.isDrawingSolidMaterials() || !c3d.isDrawingSolidMaterials() && a == 1f)
            return;
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor4f(r, g, b, a);
        if (GData.globalNegativeDeterminant) {
            GL11.glNormal3f(xn, yn, zn);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x4, y4, z4);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glNormal3f(-xn, -yn, -zn);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glVertex3f(x4, y4, z4);
        } else {
            GL11.glNormal3f(-xn, -yn, -zn);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x4, y4, z4);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glNormal3f(xn, yn, zn);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glVertex3f(x4, y4, z4);
        }
        GL11.glEnd();
    }

    @Override
    public void drawGL20RandomColours(Composite3D c3d) {
        if (!visible)
            return;
        if (a < 1f && c3d.isDrawingSolidMaterials() || !c3d.isDrawingSolidMaterials() && a == 1f)
            return;
        final float rndRed = MathHelper.randomFloat(id, 0);
        final float rndGreen = MathHelper.randomFloat(id, 1);
        final float rndBlue = MathHelper.randomFloat(id, 2);
        GL11.glBegin(GL11.GL_QUADS);
        if (GData.globalNegativeDeterminant) {
            GL11.glColor4f(rndRed, rndGreen, rndBlue, a);
            GL11.glNormal3f(xn, yn, zn);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x4, y4, z4);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glNormal3f(-xn, -yn, -zn);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glVertex3f(x4, y4, z4);
        } else {
            GL11.glColor4f(rndRed, rndGreen, rndBlue, a);
            GL11.glNormal3f(-xn, -yn, -zn);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x4, y4, z4);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glNormal3f(xn, yn, zn);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glVertex3f(x4, y4, z4);
        }
        GL11.glEnd();
    }

    @Override
    public void drawGL20BFC(Composite3D c3d) {
        if (!visible)
            return;
        if (a < 1f && c3d.isDrawingSolidMaterials() || !c3d.isDrawingSolidMaterials() && a == 1f)
            return;
        GL11.glBegin(GL11.GL_QUADS);
        switch (GData.localWinding) {
        case CCW:
            if (GData.globalNegativeDeterminant) {
                if (GData.globalInvertNext) {
                    GL11.glColor4f( // 111
                            Colour.bfcFrontColourR, Colour.bfcFrontColourG, Colour.bfcFrontColourB, a);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glColor4f(Colour.bfcBackColourR, Colour.bfcBackColourG, Colour.bfcBackColourB, a);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x4, y4, z4);
                } else {
                    GL11.glColor4f( // 110
                            Colour.bfcFrontColourR, Colour.bfcFrontColourG, Colour.bfcFrontColourB, a);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glColor4f(Colour.bfcBackColourR, Colour.bfcBackColourG, Colour.bfcBackColourB, a);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                }
            } else {
                if (GData.globalInvertNext) {
                    GL11.glColor4f( // 101
                            Colour.bfcFrontColourR, Colour.bfcFrontColourG, Colour.bfcFrontColourB, a);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glColor4f(Colour.bfcBackColourR, Colour.bfcBackColourG, Colour.bfcBackColourB, a);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                } else {
                    GL11.glColor4f( // 100
                            Colour.bfcFrontColourR, Colour.bfcFrontColourG, Colour.bfcFrontColourB, a);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glColor4f(Colour.bfcBackColourR, Colour.bfcBackColourG, Colour.bfcBackColourB, a);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x4, y4, z4);
                }
            }
            break;
        case CW:
            if (GData.globalNegativeDeterminant) {
                if (GData.globalInvertNext) {
                    GL11.glColor4f( // 011
                            Colour.bfcFrontColourR, Colour.bfcFrontColourG, Colour.bfcFrontColourB, a);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glColor4f(Colour.bfcBackColourR, Colour.bfcBackColourG, Colour.bfcBackColourB, a);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                } else {
                    GL11.glColor4f( // 010
                            Colour.bfcFrontColourR, Colour.bfcFrontColourG, Colour.bfcFrontColourB, a);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glColor4f(Colour.bfcBackColourR, Colour.bfcBackColourG, Colour.bfcBackColourB, a);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x4, y4, z4);
                }
            } else {
                if (GData.globalInvertNext) {
                    GL11.glColor4f( // 001
                            Colour.bfcFrontColourR, Colour.bfcFrontColourG, Colour.bfcFrontColourB, a);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glColor4f(Colour.bfcBackColourR, Colour.bfcBackColourG, Colour.bfcBackColourB, a);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x4, y4, z4);
                } else {
                    GL11.glColor4f( // 000
                            Colour.bfcFrontColourR, Colour.bfcFrontColourG, Colour.bfcFrontColourB, a);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glColor4f(Colour.bfcBackColourR, Colour.bfcBackColourG, Colour.bfcBackColourB, a);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                }
            }
            break;
        case NOCERTIFY:
            if (GData.globalNegativeDeterminant) {
                GL11.glColor4f(Colour.bfcUncertifiedColourR, Colour.bfcUncertifiedColourG, Colour.bfcUncertifiedColourB, a);
                GL11.glNormal3f(xn, yn, zn);
                GL11.glVertex3f(x1, y1, z1);
                GL11.glVertex3f(x4, y4, z4);
                GL11.glVertex3f(x3, y3, z3);
                GL11.glVertex3f(x2, y2, z2);
                GL11.glNormal3f(-xn, -yn, -zn);
                GL11.glVertex3f(x1, y1, z1);
                GL11.glVertex3f(x2, y2, z2);
                GL11.glVertex3f(x3, y3, z3);
                GL11.glVertex3f(x4, y4, z4);
            } else {
                GL11.glColor4f(Colour.bfcUncertifiedColourR, Colour.bfcUncertifiedColourG, Colour.bfcUncertifiedColourB, a);
                GL11.glNormal3f(-xn, -yn, -zn);
                GL11.glVertex3f(x1, y1, z1);
                GL11.glVertex3f(x4, y4, z4);
                GL11.glVertex3f(x3, y3, z3);
                GL11.glVertex3f(x2, y2, z2);
                GL11.glNormal3f(xn, yn, zn);
                GL11.glVertex3f(x1, y1, z1);
                GL11.glVertex3f(x2, y2, z2);
                GL11.glVertex3f(x3, y3, z3);
                GL11.glVertex3f(x4, y4, z4);
            }
            break;
        default:
            break;
        }
        GL11.glEnd();
    }

    @Override
    public void drawGL20BFCuncertified(Composite3D c3d) {
        if (!visible)
            return;
        if (a < 1f && c3d.isDrawingSolidMaterials() || !c3d.isDrawingSolidMaterials() && a == 1f)
            return;
        GL11.glBegin(GL11.GL_QUADS);
        if (GData.globalNegativeDeterminant) {
            GL11.glColor4f(Colour.bfcUncertifiedColourR, Colour.bfcUncertifiedColourG, Colour.bfcUncertifiedColourB, a);
            GL11.glNormal3f(xn, yn, zn);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x4, y4, z4);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glNormal3f(-xn, -yn, -zn);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glVertex3f(x4, y4, z4);
        } else {
            GL11.glColor4f(Colour.bfcUncertifiedColourR, Colour.bfcUncertifiedColourG, Colour.bfcUncertifiedColourB, a);
            GL11.glNormal3f(-xn, -yn, -zn);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x4, y4, z4);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glNormal3f(xn, yn, zn);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glVertex3f(x4, y4, z4);
        }
        GL11.glEnd();
    }

    @Override
    public void drawGL20BFCbackOnly(Composite3D c3d) {
        if (!visible)
            return;
        if (a < 1f && c3d.isDrawingSolidMaterials() || !c3d.isDrawingSolidMaterials() && a == 1f)
            return;
        GL11.glBegin(GL11.GL_QUADS);
        switch (GData.localWinding) {
        case CCW:
            if (GData.globalNegativeDeterminant) {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glColor4f(Colour.bfcBackColourR, Colour.bfcBackColourG, Colour.bfcBackColourB, a);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x4, y4, z4);
                } else {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glColor4f(Colour.bfcBackColourR, Colour.bfcBackColourG, Colour.bfcBackColourB, a);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                }
            } else {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glColor4f(Colour.bfcBackColourR, Colour.bfcBackColourG, Colour.bfcBackColourB, a);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                } else {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glColor4f(Colour.bfcBackColourR, Colour.bfcBackColourG, Colour.bfcBackColourB, a);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x4, y4, z4);
                }
            }
            break;
        case CW:
            if (GData.globalNegativeDeterminant) {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glColor4f(Colour.bfcBackColourR, Colour.bfcBackColourG, Colour.bfcBackColourB, a);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                } else {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glColor4f(Colour.bfcBackColourR, Colour.bfcBackColourG, Colour.bfcBackColourB, a);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x4, y4, z4);
                }
            } else {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glColor4f(Colour.bfcBackColourR, Colour.bfcBackColourG, Colour.bfcBackColourB, a);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x4, y4, z4);
                } else {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glColor4f(Colour.bfcBackColourR, Colour.bfcBackColourG, Colour.bfcBackColourB, a);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                }
            }
            break;
        case NOCERTIFY:
            if (GData.globalNegativeDeterminant) {
                GL11.glColor4f(Colour.bfcUncertifiedColourR, Colour.bfcUncertifiedColourG, Colour.bfcUncertifiedColourB, a);
                GL11.glNormal3f(xn, yn, zn);
                GL11.glVertex3f(x1, y1, z1);
                GL11.glVertex3f(x4, y4, z4);
                GL11.glVertex3f(x3, y3, z3);
                GL11.glVertex3f(x2, y2, z2);
                GL11.glNormal3f(-xn, -yn, -zn);
                GL11.glVertex3f(x1, y1, z1);
                GL11.glVertex3f(x2, y2, z2);
                GL11.glVertex3f(x3, y3, z3);
                GL11.glVertex3f(x4, y4, z4);
            } else {
                GL11.glColor4f(Colour.bfcUncertifiedColourR, Colour.bfcUncertifiedColourG, Colour.bfcUncertifiedColourB, a);
                GL11.glNormal3f(-xn, -yn, -zn);
                GL11.glVertex3f(x1, y1, z1);
                GL11.glVertex3f(x4, y4, z4);
                GL11.glVertex3f(x3, y3, z3);
                GL11.glVertex3f(x2, y2, z2);
                GL11.glNormal3f(xn, yn, zn);
                GL11.glVertex3f(x1, y1, z1);
                GL11.glVertex3f(x2, y2, z2);
                GL11.glVertex3f(x3, y3, z3);
                GL11.glVertex3f(x4, y4, z4);
            }
            break;
        default:
            break;
        }
        GL11.glEnd();
    }

    @Override
    public void drawGL20BFCcolour(Composite3D c3d) {
        if (!visible)
            return;
        if (a < 1f && c3d.isDrawingSolidMaterials() || !c3d.isDrawingSolidMaterials() && a == 1f)
            return;
        GL11.glBegin(GL11.GL_QUADS);
        switch (a < 1f ? BFC.NOCERTIFY : GData.localWinding) {
        case CCW:
            if (GData.globalNegativeDeterminant) {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                } else {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x4, y4, z4);
                }
            } else {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x4, y4, z4);
                } else {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                }
            }
            break;
        case CW:
            if (GData.globalNegativeDeterminant) {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x4, y4, z4);
                } else {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                }
            } else {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glNormal3f(-xn, -yn, -zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x4, y4, z4);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x2, y2, z2);
                } else {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glNormal3f(xn, yn, zn);
                    GL11.glVertex3f(x1, y1, z1);
                    GL11.glVertex3f(x2, y2, z2);
                    GL11.glVertex3f(x3, y3, z3);
                    GL11.glVertex3f(x4, y4, z4);
                }
            }
            break;
        case NOCERTIFY:
            if (GData.globalNegativeDeterminant) {
                GL11.glColor4f(r, g, b, a);
                GL11.glNormal3f(xn, yn, zn);
                GL11.glVertex3f(x1, y1, z1);
                GL11.glVertex3f(x4, y4, z4);
                GL11.glVertex3f(x3, y3, z3);
                GL11.glVertex3f(x2, y2, z2);
                GL11.glNormal3f(-xn, -yn, -zn);
                GL11.glVertex3f(x1, y1, z1);
                GL11.glVertex3f(x2, y2, z2);
                GL11.glVertex3f(x3, y3, z3);
                GL11.glVertex3f(x4, y4, z4);
            } else {
                GL11.glColor4f(r, g, b, a);
                GL11.glNormal3f(-xn, -yn, -zn);
                GL11.glVertex3f(x1, y1, z1);
                GL11.glVertex3f(x4, y4, z4);
                GL11.glVertex3f(x3, y3, z3);
                GL11.glVertex3f(x2, y2, z2);
                GL11.glNormal3f(xn, yn, zn);
                GL11.glVertex3f(x1, y1, z1);
                GL11.glVertex3f(x2, y2, z2);
                GL11.glVertex3f(x3, y3, z3);
                GL11.glVertex3f(x4, y4, z4);
            }
            break;
        default:
            break;
        }
        GL11.glEnd();
    }

    private void drawBFCcolour2(Composite3D c3d, float r, float g, float b, float a, int useCubeMap) {
        if (!visible)
            return;
        if (a < 1f && c3d.isDrawingSolidMaterials() || !c3d.isDrawingSolidMaterials() && a == 1f)
            return;
        switch (a < 1f ? BFC.NOCERTIFY : GData.localWinding) {
        case CCW:
            if (GData.globalNegativeDeterminant) {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_QUADS);
                    c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x4, y4, z4, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, false, this, useCubeMap);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_QUADS);
                    c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x4, y4, z4, true, this, useCubeMap);
                    GL11.glEnd();
                }
            } else {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_QUADS);
                    c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x4, y4, z4, false, this, useCubeMap);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_QUADS);
                    c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x4, y4, z4, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, true, this, useCubeMap);
                    GL11.glEnd();
                }
            }
            break;
        case CW:
            if (GData.globalNegativeDeterminant) {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_QUADS);
                    c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x4, y4, z4, false, this, useCubeMap);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_QUADS);
                    c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x4, y4, z4, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, false, this, useCubeMap);
                    GL11.glEnd();
                }
            } else {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_QUADS);
                    c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x4, y4, z4, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, false, this, useCubeMap);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_QUADS);
                    c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x4, y4, z4, false, this, useCubeMap);
                    GL11.glEnd();
                }
            }
            break;
        case NOCERTIFY:
            if (GData.globalNegativeDeterminant) {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_QUADS);
                    c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x4, y4, z4, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x4, y4, z4, false, this, useCubeMap);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_QUADS);
                    c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x4, y4, z4, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x4, y4, z4, true, this, useCubeMap);
                    GL11.glEnd();
                }
            } else {
                if (GData.globalInvertNext) {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_QUADS);
                    c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x4, y4, z4, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x4, y4, z4, true, this, useCubeMap);
                    GL11.glEnd();
                } else {
                    GL11.glColor4f(r, g, b, a);
                    GL11.glBegin(GL11.GL_QUADS);
                    c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x4, y4, z4, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, true, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, false, this, useCubeMap);
                    c3d.getVertexManager().setVertexAndNormal(x4, y4, z4, false, this, useCubeMap);
                    GL11.glEnd();
                }
            }
            break;
        default:
            break;
        }
    }

    @Override
    public void drawGL20BFCtextured(Composite3D c3d) {
        if (GData.globalDrawObjects) {
            GColour c = LDConfig.getColour(LDConfig.getIndex(r, g, b));
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
            float tR = this.r;
            float tG = this.g;
            float tB = this.b;
            float tA = this.a;
            if (hasColourType && useCubeMap < 1) {
                tA = 0.99f;
            }
            final OpenGLRenderer20 ren = (OpenGLRenderer20) c3d.getRenderer();
            if (GData.globalTextureStack.isEmpty()) {
                GL20.glUniform1f(ren.getAlphaSwitchLoc(), c3d.isDrawingSolidMaterials() ? 1f : 0f); // Draw transparent
                GL20.glUniform1f(ren.getNormalSwitchLoc(), GData.globalNegativeDeterminant ^ GData.globalInvertNext ? 1f : 0f);
                GL20.glUniform1f(ren.getNoTextureSwitch(), 1f);
                GL20.glUniform1f(ren.getNoLightSwitch(), c3d.isLightOn() && matLight ? 0f : 1f);
                GL20.glUniform1f(ren.getCubeMapSwitch(), useCubeMap);
                if (GData.accumClip == 0) {
                    drawBFCcolour2(c3d, tR, tG, tB, tA, useCubeMap);
                } else {
                    BFC tmp = GData.localWinding;
                    GData.localWinding = BFC.NOCERTIFY;
                    drawBFCcolour2(c3d, tR, tG, tB, tA, useCubeMap);
                    GData.localWinding = tmp;
                }
            } else { // Draw the textured face
                if (!visible)
                    return;
                GTexture tex = GData.globalTextureStack.peek();
                tex.bind(c3d.isDrawingSolidMaterials(), GData.globalNegativeDeterminant ^ GData.globalInvertNext, c3d.isLightOn() && matLight, ren, useCubeMap);
                float[] uv;
                switch (tA < 1f || GData.accumClip > 0 ? BFC.NOCERTIFY : GData.localWinding) {
                case CCW:
                    if (GData.globalNegativeDeterminant) {
                        if (GData.globalInvertNext) {
                            GL11.glColor4f(tR, tG, tB, tA);
                            GL11.glBegin(GL11.GL_QUADS);
                            tex.calcUVcoords1(x1, y1, z1, parent, this);
                            tex.calcUVcoords2(x4, y4, z4, parent);
                            tex.calcUVcoords3(x3, y3, z3, parent);
                            tex.calcUVcoords4(x2, y2, z2, parent);
                            uv = tex.getUVcoords(false, this);
                            GL11.glTexCoord2f(uv[0], uv[1]);
                            c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, false, this, useCubeMap);
                            GL11.glTexCoord2f(uv[2], uv[3]);
                            c3d.getVertexManager().setVertexAndNormal(x4, y4, z4, false, this, useCubeMap);
                            GL11.glTexCoord2f(uv[4], uv[5]);
                            c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, false, this, useCubeMap);
                            GL11.glTexCoord2f(uv[6], uv[7]);
                            c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, false, this, useCubeMap);
                            GL11.glEnd();
                        } else {
                            GL11.glColor4f(tR, tG, tB, tA);
                            GL11.glBegin(GL11.GL_QUADS);
                            tex.calcUVcoords1(x1, y1, z1, parent, this);
                            tex.calcUVcoords2(x2, y2, z2, parent);
                            tex.calcUVcoords3(x3, y3, z3, parent);
                            tex.calcUVcoords4(x4, y4, z4, parent);
                            uv = tex.getUVcoords(false, this);
                            GL11.glTexCoord2f(uv[0], uv[1]);
                            c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, true, this, useCubeMap);
                            GL11.glTexCoord2f(uv[2], uv[3]);
                            c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, true, this, useCubeMap);
                            GL11.glTexCoord2f(uv[4], uv[5]);
                            c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, true, this, useCubeMap);
                            GL11.glTexCoord2f(uv[6], uv[7]);
                            c3d.getVertexManager().setVertexAndNormal(x4, y4, z4, true, this, useCubeMap);
                            GL11.glEnd();
                        }
                    } else {
                        if (GData.globalInvertNext) {
                            GL11.glColor4f(tR, tG, tB, tA);
                            GL11.glBegin(GL11.GL_QUADS);
                            tex.calcUVcoords1(x1, y1, z1, parent, this);
                            tex.calcUVcoords2(x2, y2, z2, parent);
                            tex.calcUVcoords3(x3, y3, z3, parent);
                            tex.calcUVcoords4(x4, y4, z4, parent);
                            uv = tex.getUVcoords(false, this);
                            GL11.glTexCoord2f(uv[0], uv[1]);
                            c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, false, this, useCubeMap);
                            GL11.glTexCoord2f(uv[2], uv[3]);
                            c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, false, this, useCubeMap);
                            GL11.glTexCoord2f(uv[4], uv[5]);
                            c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, false, this, useCubeMap);
                            GL11.glTexCoord2f(uv[6], uv[7]);
                            c3d.getVertexManager().setVertexAndNormal(x4, y4, z4, false, this, useCubeMap);
                            GL11.glEnd();
                        } else {
                            GL11.glColor4f(tR, tG, tB, tA);
                            GL11.glBegin(GL11.GL_QUADS);
                            tex.calcUVcoords1(x1, y1, z1, parent, this);
                            tex.calcUVcoords2(x4, y4, z4, parent);
                            tex.calcUVcoords3(x3, y3, z3, parent);
                            tex.calcUVcoords4(x2, y2, z2, parent);
                            uv = tex.getUVcoords(false, this);
                            GL11.glTexCoord2f(uv[0], uv[1]);
                            c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, true, this, useCubeMap);
                            GL11.glTexCoord2f(uv[2], uv[3]);
                            c3d.getVertexManager().setVertexAndNormal(x4, y4, z4, true, this, useCubeMap);
                            GL11.glTexCoord2f(uv[4], uv[5]);
                            c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, true, this, useCubeMap);
                            GL11.glTexCoord2f(uv[6], uv[7]);
                            c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, true, this, useCubeMap);
                            GL11.glEnd();
                        }
                    }
                    break;
                case CW:
                    if (GData.globalNegativeDeterminant) {
                        if (GData.globalInvertNext) {
                            GL11.glColor4f(tR, tG, tB, tA);
                            GL11.glBegin(GL11.GL_QUADS);
                            tex.calcUVcoords1(x1, y1, z1, parent, this);
                            tex.calcUVcoords2(x2, y2, z2, parent);
                            tex.calcUVcoords3(x3, y3, z3, parent);
                            tex.calcUVcoords4(x4, y4, z4, parent);
                            uv = tex.getUVcoords(false, this);
                            GL11.glTexCoord2f(uv[0], uv[1]);
                            c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, true, this, useCubeMap);
                            GL11.glTexCoord2f(uv[2], uv[3]);
                            c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, true, this, useCubeMap);
                            GL11.glTexCoord2f(uv[4], uv[5]);
                            c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, true, this, useCubeMap);
                            GL11.glTexCoord2f(uv[6], uv[7]);
                            c3d.getVertexManager().setVertexAndNormal(x4, y4, z4, true, this, useCubeMap);
                            GL11.glEnd();
                        } else {
                            GL11.glColor4f(tR, tG, tB, tA);
                            GL11.glBegin(GL11.GL_QUADS);
                            tex.calcUVcoords1(x1, y1, z1, parent, this);
                            tex.calcUVcoords2(x4, y4, z4, parent);
                            tex.calcUVcoords3(x3, y3, z3, parent);
                            tex.calcUVcoords4(x2, y2, z2, parent);
                            uv = tex.getUVcoords(false, this);
                            GL11.glTexCoord2f(uv[0], uv[1]);
                            c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, false, this, useCubeMap);
                            GL11.glTexCoord2f(uv[2], uv[3]);
                            c3d.getVertexManager().setVertexAndNormal(x4, y4, z4, false, this, useCubeMap);
                            GL11.glTexCoord2f(uv[4], uv[5]);
                            c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, false, this, useCubeMap);
                            GL11.glTexCoord2f(uv[6], uv[7]);
                            c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, false, this, useCubeMap);
                            GL11.glEnd();
                        }
                    } else {
                        if (GData.globalInvertNext) {
                            GL11.glColor4f(tR, tG, tB, tA);
                            GL11.glBegin(GL11.GL_QUADS);
                            tex.calcUVcoords1(x1, y1, z1, parent, this);
                            tex.calcUVcoords2(x4, y4, z4, parent);
                            tex.calcUVcoords3(x3, y3, z3, parent);
                            tex.calcUVcoords4(x2, y2, z2, parent);
                            uv = tex.getUVcoords(false, this);
                            GL11.glTexCoord2f(uv[0], uv[1]);
                            c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, true, this, useCubeMap);
                            GL11.glTexCoord2f(uv[2], uv[3]);
                            c3d.getVertexManager().setVertexAndNormal(x4, y4, z4, true, this, useCubeMap);
                            GL11.glTexCoord2f(uv[4], uv[5]);
                            c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, true, this, useCubeMap);
                            GL11.glTexCoord2f(uv[6], uv[7]);
                            c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, true, this, useCubeMap);
                            GL11.glEnd();
                        } else {
                            GL11.glColor4f(tR, tG, tB, tA);
                            GL11.glBegin(GL11.GL_QUADS);
                            tex.calcUVcoords1(x1, y1, z1, parent, this);
                            tex.calcUVcoords2(x2, y2, z2, parent);
                            tex.calcUVcoords3(x3, y3, z3, parent);
                            tex.calcUVcoords4(x4, y4, z4, parent);
                            uv = tex.getUVcoords(false, this);
                            GL11.glTexCoord2f(uv[0], uv[1]);
                            c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, false, this, useCubeMap);
                            GL11.glTexCoord2f(uv[2], uv[3]);
                            c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, false, this, useCubeMap);
                            GL11.glTexCoord2f(uv[4], uv[5]);
                            c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, false, this, useCubeMap);
                            GL11.glTexCoord2f(uv[6], uv[7]);
                            c3d.getVertexManager().setVertexAndNormal(x4, y4, z4, false, this, useCubeMap);
                            GL11.glEnd();
                        }
                    }
                    break;
                case NOCERTIFY:
                    if (GData.globalNegativeDeterminant) {
                        GL11.glColor4f(tR, tG, tB, tA);
                        GL11.glBegin(GL11.GL_QUADS);
                        tex.calcUVcoords1(x1, y1, z1, parent, null);
                        tex.calcUVcoords2(x4, y4, z4, parent);
                        tex.calcUVcoords3(x3, y3, z3, parent);
                        tex.calcUVcoords4(x2, y2, z2, parent);
                        uv = tex.getUVcoords(false, null);
                        GL11.glTexCoord2f(uv[0], uv[1]);
                        c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, false, this, useCubeMap);
                        GL11.glTexCoord2f(uv[2], uv[3]);
                        c3d.getVertexManager().setVertexAndNormal(x4, y4, z4, false, this, useCubeMap);
                        GL11.glTexCoord2f(uv[4], uv[5]);
                        c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, false, this, useCubeMap);
                        GL11.glTexCoord2f(uv[6], uv[7]);
                        c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, false, this, useCubeMap);
                        GL11.glEnd();
                        GL11.glBegin(GL11.GL_QUADS);
                        tex.calcUVcoords1(x1, y1, z1, parent, null);
                        tex.calcUVcoords2(x2, y2, z2, parent);
                        tex.calcUVcoords3(x3, y3, z3, parent);
                        tex.calcUVcoords4(x4, y4, z4, parent);
                        uv = tex.getUVcoords(false, null);
                        GL11.glTexCoord2f(uv[0], uv[1]);
                        c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, true, this, useCubeMap);
                        GL11.glTexCoord2f(uv[2], uv[3]);
                        c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, true, this, useCubeMap);
                        GL11.glTexCoord2f(uv[4], uv[5]);
                        c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, true, this, useCubeMap);
                        GL11.glTexCoord2f(uv[6], uv[7]);
                        c3d.getVertexManager().setVertexAndNormal(x4, y4, z4, true, this, useCubeMap);
                        GL11.glEnd();
                    } else {
                        GL11.glColor4f(tR, tG, tB, tA);
                        GL11.glBegin(GL11.GL_QUADS);
                        tex.calcUVcoords1(x1, y1, z1, parent, null);
                        tex.calcUVcoords2(x4, y4, z4, parent);
                        tex.calcUVcoords3(x3, y3, z3, parent);
                        tex.calcUVcoords4(x2, y2, z2, parent);
                        uv = tex.getUVcoords(false, null);
                        GL11.glTexCoord2f(uv[0], uv[1]);
                        c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, true, this, useCubeMap);
                        GL11.glTexCoord2f(uv[2], uv[3]);
                        c3d.getVertexManager().setVertexAndNormal(x4, y4, z4, true, this, useCubeMap);
                        GL11.glTexCoord2f(uv[4], uv[5]);
                        c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, true, this, useCubeMap);
                        GL11.glTexCoord2f(uv[6], uv[7]);
                        c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, true, this, useCubeMap);
                        GL11.glEnd();
                        GL11.glBegin(GL11.GL_QUADS);
                        tex.calcUVcoords1(x1, y1, z1, parent, null);
                        tex.calcUVcoords2(x2, y2, z2, parent);
                        tex.calcUVcoords3(x3, y3, z3, parent);
                        tex.calcUVcoords4(x4, y4, z4, parent);
                        uv = tex.getUVcoords(false, null);
                        GL11.glTexCoord2f(uv[0], uv[1]);
                        c3d.getVertexManager().setVertexAndNormal(x1, y1, z1, false, this, useCubeMap);
                        GL11.glTexCoord2f(uv[2], uv[3]);
                        c3d.getVertexManager().setVertexAndNormal(x2, y2, z2, false, this, useCubeMap);
                        GL11.glTexCoord2f(uv[4], uv[5]);
                        c3d.getVertexManager().setVertexAndNormal(x3, y3, z3, false, this, useCubeMap);
                        GL11.glTexCoord2f(uv[6], uv[7]);
                        c3d.getVertexManager().setVertexAndNormal(x4, y4, z4, false, this, useCubeMap);
                        GL11.glEnd();
                    }
                    break;
                default:
                    break;
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
    public void drawGL20WhileAddCondlines(Composite3D c3d) {
        drawGL20BFC(c3d);
    }

    @Override
    public void drawGL20CoplanarityHeatmap(Composite3D c3d) {
        calculateAngle();
        float f = (float) Math.min(1.0, Math.max(0, angle - Threshold.coplanarityAngleWarning) / Threshold.coplanarityAngleError);

        float red = 0f;
        float green;
        float blue = 0f;

        if (f < .5) {
            green = f / .5f;
            blue = (1f - green);
        } else {
            red = (f - .5f) / .5f;
            green = (1f - red);
        }

        if (!visible)
            return;
        if (a < 1f && c3d.isDrawingSolidMaterials() || !c3d.isDrawingSolidMaterials() && a == 1f)
            return;
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor4f(red, green, blue, a);
        if (GData.globalNegativeDeterminant) {
            GL11.glNormal3f(xn, yn, zn);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x4, y4, z4);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glNormal3f(-xn, -yn, -zn);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glVertex3f(x4, y4, z4);
        } else {
            GL11.glNormal3f(-xn, -yn, -zn);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x4, y4, z4);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glNormal3f(xn, yn, zn);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glVertex3f(x3, y3, z3);
            GL11.glVertex3f(x4, y4, z4);
        }
        GL11.glEnd();
    }

    @Override
    public void drawGL20Wireframe(Composite3D c3d) {
        // Implementation is not required.
    }

    @Override
    public int type() {
        return 4;
    }

    @Override
    String getNiceString() {
        if (text != null)
            return text;
        StringBuilder lineBuilder = new StringBuilder();
        lineBuilder.append("4 "); //$NON-NLS-1$
        if (colourNumber == -1) {
            lineBuilder.append("0x2"); //$NON-NLS-1$
            lineBuilder.append(MathHelper.toHex((int) (255f * r)).toUpperCase());
            lineBuilder.append(MathHelper.toHex((int) (255f * g)).toUpperCase());
            lineBuilder.append(MathHelper.toHex((int) (255f * b)).toUpperCase());
        } else {
            lineBuilder.append(colourNumber);
        }
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(x1p));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(y1p));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(z1p));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(x2p));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(y2p));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(z2p));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(x3p));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(y3p));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(z3p));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(x4p));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(y4p));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(z4p));
        text = lineBuilder.toString();
        return text;
    }

    @Override
    public String inlinedString(BFC bfc, GColour colour) {
        return getNiceString();
    }

    @Override
    @SuppressWarnings("java:S2111")
    public String transformAndColourReplace(String colour, Matrix matrix) {
        BigDecimal[] v1;
        BigDecimal[] v2;
        BigDecimal[] v3;
        BigDecimal[] v4;
        if (x1p == null) {
            v1 = matrix.transform(new BigDecimal(x1 / 1000f), new BigDecimal(y1 / 1000f), new BigDecimal(z1 / 1000f));
            v2 = matrix.transform(new BigDecimal(x2 / 1000f), new BigDecimal(y2 / 1000f), new BigDecimal(z2 / 1000f));
            v3 = matrix.transform(new BigDecimal(x3 / 1000f), new BigDecimal(y3 / 1000f), new BigDecimal(z3 / 1000f));
            v4 = matrix.transform(new BigDecimal(x4 / 1000f), new BigDecimal(y4 / 1000f), new BigDecimal(z4 / 1000f));
        } else {
            v1 = matrix.transform(x1p, y1p, z1p);
            v2 = matrix.transform(x2p, y2p, z2p);
            v3 = matrix.transform(x3p, y3p, z3p);
            v4 = matrix.transform(x4p, y4p, z4p);
        }
        StringBuilder lineBuilder = new StringBuilder();
        lineBuilder.append(4);
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
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(v4[0]));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(v4[1]));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(v4[2]));
        return lineBuilder.toString();
    }

    @Override
    public void getBFCorientationMap(Map<GData,BFC> map) {
        switch (GData.localWinding) {
        case CCW:
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
        case CW:
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
        case NOCERTIFY:
            // Don't get it for NOCERTIFY.
            break;
        default:
            break;
        }
    }

    @Override
    public void getBFCorientationMapNOCERTIFY(Map<GData, BFC> map) {
        // Don't get it for NOCERTIFY.
    }

    @Override
    public void getBFCorientationMapNOCLIP(Map<GData, BFC> map) {
        map.put(this, BFC.NOCLIP);
    }

    @Override
    public void getVertexNormalMap(GDataState state, ThreadsafeSortedMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {
        if (state.globalDrawObjects) {
            Vertex[] verts = vm.getQuadsNoClone().get(this);
            if (verts == null) {
                verts = new Vertex[4];
                Vector4f v1 = new Vector4f(x1, y1, z1, 1f);
                Vector4f v2 = new Vector4f(x2, y2, z2, 1f);
                Vector4f v3 = new Vector4f(x3, y3, z3, 1f);
                Vector4f v4 = new Vector4f(x4, y4, z4, 1f);
                Matrix4f.transform(this.parent.productMatrix, v1, v1);
                Matrix4f.transform(this.parent.productMatrix, v2, v2);
                Matrix4f.transform(this.parent.productMatrix, v3, v3);
                Matrix4f.transform(this.parent.productMatrix, v4, v4);
                verts[0] = new Vertex(v1.x , v1.y , v1.z, true);
                verts[1] = new Vertex(v2.x , v2.y , v2.z, true);
                verts[2] = new Vertex(v3.x , v3.y , v3.z, true);
                verts[3] = new Vertex(v4.x , v4.y , v4.z, true);
            }

            final Vector3f[] normals = new Vector3f[] { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
            {
                final Vector3f[] lineVectors = new Vector3f[] { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
                Vector3f.sub(new Vector3f(verts[1].x, verts[1].y, verts[1].z), new Vector3f(verts[0].x, verts[0].y, verts[0].z), lineVectors[0]);
                Vector3f.sub(new Vector3f(verts[2].x, verts[2].y, verts[2].z), new Vector3f(verts[1].x, verts[1].y, verts[1].z), lineVectors[1]);
                Vector3f.sub(new Vector3f(verts[3].x, verts[3].y, verts[3].z), new Vector3f(verts[2].x, verts[2].y, verts[2].z), lineVectors[2]);
                Vector3f.sub(new Vector3f(verts[0].x, verts[0].y, verts[0].z), new Vector3f(verts[3].x, verts[3].y, verts[3].z), lineVectors[3]);
                Vector3f.cross(lineVectors[0], lineVectors[1], normals[0]);
                Vector3f.cross(lineVectors[1], lineVectors[2], normals[1]);
                Vector3f.cross(lineVectors[2], lineVectors[3], normals[2]);
                Vector3f.cross(lineVectors[3], lineVectors[0], normals[3]);
            }
            Vector3f normal = new Vector3f();
            for (int i = 0; i < 4; i++) {
                Vector3f.add(normals[i], normal, normal);
            }
            float txn = normal.x;
            float tyn = normal.y;
            float tzn = normal.z;

            final float length = (float) Math.sqrt(txn * txn + tyn * tyn + tzn *tzn);
            if (length > 0) {
                txn = txn / length;
                tyn = tyn / length;
                tzn = tzn / length;
            }

            for (Vertex vertex : verts) {
                float[] result = new float[3];
                switch (state.localWinding) {
                case NOCLIP:
                    result[0] = txn;
                    result[1] = tyn;
                    result[2] = tzn;
                    break;
                case CCW:
                    if (state.globalInvertNext) {
                        if (state.globalNegativeDeterminant) {
                            result[0] = -txn;
                            result[1] = -tyn;
                            result[2] = -tzn;
                        } else {
                            result[0] = txn;
                            result[1] = tyn;
                            result[2] = tzn;
                        }
                    } else {
                        if (state.globalNegativeDeterminant) {
                            result[0] = txn;
                            result[1] = tyn;
                            result[2] = tzn;
                        } else {
                            result[0] = -txn;
                            result[1] = -tyn;
                            result[2] = -tzn;
                        }
                    }
                    break;
                case CW:
                    if (state.globalNegativeDeterminant) {
                        result[0] = txn;
                        result[1] = tyn;
                        result[2] = tzn;
                    } else {
                        result[0] = -txn;
                        result[1] = -tyn;
                        result[2] = -tzn;
                    }
                    break;
                case NOCERTIFY:
                    break;
                default:
                    break;
                }
                if (state.globalInvertNext) {
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
        if (state.globalFoundTEXMAPNEXT) {
            state.globalFoundTEXMAPStack.pop();
            state.globalTextureStack.pop();
            state.globalFoundTEXMAPStack.push(false);
            state.globalFoundTEXMAPNEXT = false;
        }
    }

    @Override
    public void getVertexNormalMapNOCERTIFY(GDataState state, ThreadsafeSortedMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {
        // Implementation is not required.
    }

    @Override
    public void getVertexNormalMapNOCLIP(GDataState state, ThreadsafeSortedMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {
        getVertexNormalMap(state, vertexLinkedToNormalCACHE, dataLinkedToNormalCACHE, vm);
    }

    String colourReplace(String col) {
        StringBuilder lineBuilder = new StringBuilder();
        lineBuilder.append(4);
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(col);
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(x1p));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(y1p));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(z1p));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(x2p));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(y2p));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(z2p));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(x3p));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(y3p));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(z3p));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(x4p));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(y4p));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(z4p));
        return lineBuilder.toString();
    }

    double calculateAngle() {
        if (angle < 0) {
            Vector3d[] normals = new Vector3d[4];
            Vector3d[] lineVectors = new Vector3d[4];
            Vector3d vertexA = new Vector3d(x1p, y1p, z1p);
            Vector3d vertexB = new Vector3d(x2p, y2p, z2p);
            Vector3d vertexC = new Vector3d(x3p, y3p, z3p);
            Vector3d vertexD = new Vector3d(x4p, y4p, z4p);
            lineVectors[0] = Vector3d.sub(vertexB, vertexA);
            lineVectors[1] = Vector3d.sub(vertexC, vertexB);
            lineVectors[2] = Vector3d.sub(vertexD, vertexC);
            lineVectors[3] = Vector3d.sub(vertexA, vertexD);
            normals[0] = Vector3d.cross(lineVectors[0], lineVectors[1]);
            normals[1] = Vector3d.cross(lineVectors[1], lineVectors[2]);
            normals[2] = Vector3d.cross(lineVectors[2], lineVectors[3]);
            normals[3] = Vector3d.cross(lineVectors[3], lineVectors[0]);

            angle = Math.max(Vector3d.angle(normals[0], normals[2]), Vector3d.angle(normals[1], normals[3]));
        }
        return angle;
    }

    public boolean isCollinear() {
        Vector3d vertexA = new Vector3d(x1p, y1p, z1p);
        Vector3d vertexB = new Vector3d(x2p, y2p, z2p);
        Vector3d vertexC = new Vector3d(x3p, y3p, z3p);
        Vector3d vertexD = new Vector3d(x4p, y4p, z4p);
        Vector3d sa = Vector3d.sub(vertexB, vertexA);
        Vector3d sb = Vector3d.sub(vertexB, vertexC);
        Vector3d sc = Vector3d.sub(vertexD, vertexC);
        Vector3d sd = Vector3d.sub(vertexD, vertexA);

        double collinearityAngle = Vector3d.angle(sa, sd);
        double sumAngle = collinearityAngle;
        if (collinearityAngle < Threshold.COLLINEAR_ANGLE_MINIMUM || collinearityAngle > Threshold.COLLINEAR_ANGLE_MAXIMUM) {
            return true;
        }

        collinearityAngle = Vector3d.angle(sb, sc);
        sumAngle = sumAngle + collinearityAngle;
        if (collinearityAngle < Threshold.COLLINEAR_ANGLE_MINIMUM || collinearityAngle > Threshold.COLLINEAR_ANGLE_MAXIMUM) {
            return true;
        }

        sa.negate();
        sb.negate();
        collinearityAngle = Vector3d.angle(sa, sb);
        sumAngle = sumAngle + collinearityAngle;
        if (collinearityAngle < Threshold.COLLINEAR_ANGLE_MINIMUM || collinearityAngle > Threshold.COLLINEAR_ANGLE_MAXIMUM) {
            return true;
        }

        collinearityAngle = 360.0 - sumAngle;
        if (collinearityAngle < Threshold.COLLINEAR_ANGLE_MINIMUM || collinearityAngle > Threshold.COLLINEAR_ANGLE_MAXIMUM) {
            return true;
        }

        return false;
    }

    public int getHourglassConfiguration() {

        Vector3d vertexA = new Vector3d(x1p, y1p, z1p);
        Vector3d vertexB = new Vector3d(x2p, y2p, z2p);
        Vector3d vertexC = new Vector3d(x3p, y3p, z3p);
        Vector3d vertexD = new Vector3d(x4p, y4p, z4p);

        Vector3d[] normals = new Vector3d[4];
        float[] normalDirections = new float[4];
        Vector3d[] lineVectors = new Vector3d[4];
        int cnc = 0;
        int fcc = 0;
        lineVectors[0] = Vector3d.sub(vertexB, vertexA);
        lineVectors[1] = Vector3d.sub(vertexC, vertexB);
        lineVectors[2] = Vector3d.sub(vertexD, vertexC);
        lineVectors[3] = Vector3d.sub(vertexA, vertexD);
        normals[0] = Vector3d.cross(lineVectors[0], lineVectors[1]);
        normals[1] = Vector3d.cross(lineVectors[1], lineVectors[2]);
        normals[2] = Vector3d.cross(lineVectors[2], lineVectors[3]);
        normals[3] = Vector3d.cross(lineVectors[3], lineVectors[0]);

        for (int i = 0; i < 4; i++) {
            normalDirections[i] = MathHelper.directionOfVectors(normals[0], normals[i]);
            if (normalDirections[i] < 0) {
                if (cnc < 1) fcc = i;
                cnc++;
            }
        }
        if (cnc == 2) {
            // Hourglass
            if (fcc == 1) {
                return 1;
            } else {
                // fcc is 2, probably return fcc directly?
                return 2;
            }
        }
        return 0;
    }
}
