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

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.enums.GL20Primitives;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeTreeMap;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer20;

/**
 * @author nils
 *
 */
public final class GData5 extends GData {

    final int colourNumber;

    final float r;
    final float g;
    final float b;
    final float a;

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

    private final Vector4f sA = new Vector4f(0, 0, 0, 1f);
    private final Vector4f sB = new Vector4f(0, 0, 0, 1f);
    private final Vector4f sC = new Vector4f(0, 0, 0, 1f);
    private final Vector4f sD = new Vector4f(0, 0, 0, 1f);

    private final Vector4f sA2 = new Vector4f(0, 0, 0, 1f);
    private final Vector4f sB2 = new Vector4f(0, 0, 0, 1f);
    private final Vector4f sC2 = new Vector4f(0, 0, 0, 1f);
    private final Vector4f sD2 = new Vector4f(0, 0, 0, 1f);

    private final Vector4f n = new Vector4f(0, 0, 0, 1f);
    private Matrix4f m = new Matrix4f();

    private final float[][] lGeom;

    private boolean wasShown = false;

    public GData5(final int colourNumber, float r, float g, float b, float a, BigDecimal x1, BigDecimal y1, BigDecimal z1, BigDecimal x2, BigDecimal y2, BigDecimal z2, BigDecimal x3, BigDecimal y3,
            BigDecimal z3, BigDecimal x4, BigDecimal y4, BigDecimal z4, GData1 parentFileRef, DatFile datFile) {

        super(parentFileRef);
        this.colourNumber = colourNumber;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.lGeom = MathHelper.getLineVertices(new Vector3f(x1.floatValue(), y1.floatValue(), z1.floatValue()), new Vector3f(x2.floatValue(), y2.floatValue(), z2.floatValue()),
                parent.productMatrix);
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
        datFile.getVertexManager().add(this);
        sA2.x = this.x1;
        sB2.x = this.x2;
        sC2.x = this.x3;
        sD2.x = this.x4;
        sA2.y = this.y1;
        sB2.y = this.y2;
        sC2.y = this.y3;
        sD2.y = this.y4;
        sA2.z = this.z1;
        sB2.z = this.z2;
        sC2.z = this.z3;
        sD2.z = this.z4;

    }

    GData5(final int colourNumber, float r, float g, float b, float a, BigDecimal x1, BigDecimal y1, BigDecimal z1, BigDecimal x2, BigDecimal y2, BigDecimal z2, BigDecimal x3,
            BigDecimal y3, BigDecimal z3, BigDecimal x4, BigDecimal y4, BigDecimal z4, float x12, float y12, float z12, float x22, float y22, float z22, float x32, float y32, float z32,
            float x42, float y42, float z42, GData1 parentFileRef, DatFile datFile) {

        super(parentFileRef);
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
        this.lGeom = MathHelper.getLineVertices1000(new Vector3f(this.x1, this.y1, this.z1), new Vector3f(this.x2, this.y2, this.z2), parent.productMatrix);
        datFile.getVertexManager().add(this);
        sA2.x = this.x1;
        sB2.x = this.x2;
        sC2.x = this.x3;
        sD2.x = this.x4;
        sA2.y = this.y1;
        sB2.y = this.y2;
        sC2.y = this.y3;
        sD2.y = this.y4;
        sA2.z = this.z1;
        sB2.z = this.z2;
        sC2.z = this.z3;
        sD2.z = this.z4;

    }

    /**
     * FOR Cut, Copy, Paste ONLY!
     *
     * @param v1
     * @param v2
     * @param v3
     * @param v4
     * @param parent
     * @param c
     */
    GData5(Vertex v1, Vertex v2, Vertex v3, Vertex v4, GData1 parent, GColour c) {
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
        this.lGeom = null;
    }

    /**
     * FOR TEXMAP ONLY
     *
     * @param v1
     * @param v2
     * @param v3
     * @param v4
     * @param c
     * @param parent
     */
    public GData5(Vertex v1, Vertex v2, Vertex v3, Vertex v4, GColour c, GData1 parent) {
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
        this.lGeom = MathHelper.getLineVertices1000(new Vector3f(x1, y1, z1), new Vector3f(x2, y2, z2), parent.productMatrix);
        sA2.x = this.x1;
        sB2.x = this.x2;
        sC2.x = this.x3;
        sD2.x = this.x4;
        sA2.y = this.y1;
        sB2.y = this.y2;
        sC2.y = this.y3;
        sD2.y = this.y4;
        sA2.z = this.z1;
        sB2.z = this.z2;
        sC2.z = this.z3;
        sD2.z = this.z4;
        this.x1p = null;
        this.y1p = null;
        this.z1p = null;
        this.x2p = null;
        this.y2p = null;
        this.z2p = null;
        this.x3p = null;
        this.y3p = null;
        this.z3p = null;
        this.x4p = null;
        this.y4p = null;
        this.z4p = null;
    }

    GData5(final int colourNumber, float r, float g, float b, float a, Vertex v1, Vertex v2, Vertex v3, Vertex v4, GData1 parent, DatFile datFile) {
        super(parent);
        this.colourNumber = colourNumber;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
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
        this.lGeom = MathHelper.getLineVertices1000(new Vector3f(x1, y1, z1), new Vector3f(x2, y2, z2), parent.productMatrix);
        datFile.getVertexManager().add(this);
        sA2.x = this.x1;
        sB2.x = this.x2;
        sC2.x = this.x3;
        sD2.x = this.x4;
        sA2.y = this.y1;
        sB2.y = this.y2;
        sC2.y = this.y3;
        sD2.y = this.y4;
        sA2.z = this.z1;
        sB2.z = this.z2;
        sC2.z = this.z3;
        sD2.z = this.z4;
    }

    @Override
    public void drawGL20(Composite3D c3d) {
        if (!visible) {
            if (c3d.isLightOn() && (next == null || next.type() != 2 && next.type() != 5))
                GL11.glEnable(GL11.GL_LIGHTING);
            return;
        }
        if (a < 1f && c3d.isDrawingSolidMaterials() || !c3d.isDrawingSolidMaterials() && a == 1f) {
            if (c3d.isLightOn() && (next == null || next.type() != 2 && next.type() != 5))
                GL11.glEnable(GL11.GL_LIGHTING);
            return;
        }

        float result;
        float zoom = c3d.getZoom();
        switch (c3d.getLineMode()) {
        case 1:
            result = 1f;
            break;
        case 2:
        case 4:
            if (c3d.isLightOn() && (next == null || next.type() != 2 && next.type() != 5))
                GL11.glEnable(GL11.GL_LIGHTING);
            return;
        default:
            final Matrix4f m2 = GData.CACHE_viewByProjection.get(parent);
            if (m2 == null) {
                Matrix4f.mul(c3d.getViewport(), parent.productMatrix, m);
                GData.CACHE_viewByProjection.put(parent, m);
            } else {
                m = m2;
            }
            // Calculate the real coordinates
            Matrix4f.transform(m, sA2, sA);
            Matrix4f.transform(m, sB2, sB);
            Matrix4f.transform(m, sC2, sC);
            Matrix4f.transform(m, sD2, sD);

            n.x = sA.y - sB.y;
            n.y = sB.x - sA.x;

            result = zoom / Vector4f.dot(n, Vector4f.sub(sC, sA, null)) * Vector4f.dot(n, Vector4f.sub(sD, sA, null));
            break;
        }

        if (result > -1e-20f) {

            if (GL11.glGetBoolean(GL11.GL_LIGHTING) == 1) GL11.glDisable(GL11.GL_LIGHTING);

            if (zoom > View.edgeThreshold) {

                GL11.glColor4f(r, g, b, a);

                GL11.glPushMatrix();

                GL11.glScalef(lGeom[20][0], lGeom[20][1], lGeom[20][2]);

                if (GData.globalNegativeDeterminant) {

                    GL20Primitives.sphereInv.draw(lGeom[18][0], lGeom[18][1], lGeom[18][2]);

                    GL11.glBegin(GL11.GL_QUAD_STRIP);
                    GL11.glVertex3f(lGeom[1][0], lGeom[1][1], lGeom[1][2]);
                    GL11.glVertex3f(lGeom[0][0], lGeom[0][1], lGeom[0][2]);
                    GL11.glVertex3f(lGeom[3][0], lGeom[3][1], lGeom[3][2]);
                    GL11.glVertex3f(lGeom[2][0], lGeom[2][1], lGeom[2][2]);
                    GL11.glVertex3f(lGeom[5][0], lGeom[5][1], lGeom[5][2]);
                    GL11.glVertex3f(lGeom[4][0], lGeom[4][1], lGeom[4][2]);
                    GL11.glVertex3f(lGeom[7][0], lGeom[7][1], lGeom[7][2]);
                    GL11.glVertex3f(lGeom[6][0], lGeom[6][1], lGeom[6][2]);
                    GL11.glVertex3f(lGeom[9][0], lGeom[9][1], lGeom[9][2]);
                    GL11.glVertex3f(lGeom[8][0], lGeom[8][1], lGeom[8][2]);
                    GL11.glVertex3f(lGeom[11][0], lGeom[11][1], lGeom[11][2]);
                    GL11.glVertex3f(lGeom[10][0], lGeom[10][1], lGeom[10][2]);
                    GL11.glVertex3f(lGeom[13][0], lGeom[13][1], lGeom[13][2]);
                    GL11.glVertex3f(lGeom[12][0], lGeom[12][1], lGeom[12][2]);
                    GL11.glVertex3f(lGeom[15][0], lGeom[15][1], lGeom[15][2]);
                    GL11.glVertex3f(lGeom[14][0], lGeom[14][1], lGeom[14][2]);
                    GL11.glVertex3f(lGeom[17][0], lGeom[17][1], lGeom[17][2]);
                    GL11.glVertex3f(lGeom[16][0], lGeom[16][1], lGeom[16][2]);
                    GL11.glEnd();

                    GL20Primitives.sphereInv.draw(lGeom[19][0], lGeom[19][1], lGeom[19][2]);

                } else {

                    GL20Primitives.sphere.draw(lGeom[18][0], lGeom[18][1], lGeom[18][2]);

                    GL11.glBegin(GL11.GL_QUAD_STRIP);
                    GL11.glVertex3f(lGeom[0][0], lGeom[0][1], lGeom[0][2]);
                    GL11.glVertex3f(lGeom[1][0], lGeom[1][1], lGeom[1][2]);
                    GL11.glVertex3f(lGeom[2][0], lGeom[2][1], lGeom[2][2]);
                    GL11.glVertex3f(lGeom[3][0], lGeom[3][1], lGeom[3][2]);
                    GL11.glVertex3f(lGeom[4][0], lGeom[4][1], lGeom[4][2]);
                    GL11.glVertex3f(lGeom[5][0], lGeom[5][1], lGeom[5][2]);
                    GL11.glVertex3f(lGeom[6][0], lGeom[6][1], lGeom[6][2]);
                    GL11.glVertex3f(lGeom[7][0], lGeom[7][1], lGeom[7][2]);
                    GL11.glVertex3f(lGeom[8][0], lGeom[8][1], lGeom[8][2]);
                    GL11.glVertex3f(lGeom[9][0], lGeom[9][1], lGeom[9][2]);
                    GL11.glVertex3f(lGeom[10][0], lGeom[10][1], lGeom[10][2]);
                    GL11.glVertex3f(lGeom[11][0], lGeom[11][1], lGeom[11][2]);
                    GL11.glVertex3f(lGeom[12][0], lGeom[12][1], lGeom[12][2]);
                    GL11.glVertex3f(lGeom[13][0], lGeom[13][1], lGeom[13][2]);
                    GL11.glVertex3f(lGeom[14][0], lGeom[14][1], lGeom[14][2]);
                    GL11.glVertex3f(lGeom[15][0], lGeom[15][1], lGeom[15][2]);
                    GL11.glVertex3f(lGeom[16][0], lGeom[16][1], lGeom[16][2]);
                    GL11.glVertex3f(lGeom[17][0], lGeom[17][1], lGeom[17][2]);
                    GL11.glEnd();

                    GL20Primitives.sphere.draw(lGeom[19][0], lGeom[19][1], lGeom[19][2]);

                }

                GL11.glPopMatrix();

            } else {
                GL11.glLineWidth(View.LINE_WIDTH_GL[0]);
                GL11.glColor4f(r, g, b, a);
                GL11.glBegin(GL11.GL_LINES);
                GL11.glVertex3f(x1, y1, z1);
                GL11.glVertex3f(x2, y2, z2);
                GL11.glEnd();
            }
        }

        if (c3d.isLightOn() && (next == null || next.type() != 2 && next.type() != 5))
            GL11.glEnable(GL11.GL_LIGHTING);
    }

    @Override
    public void drawGL20RandomColours(Composite3D c3d) {
        if (!visible) {
            if (c3d.isLightOn() && (next == null || next.type() != 2 && next.type() != 5))
                GL11.glEnable(GL11.GL_LIGHTING);
            return;
        }
        if (a < 1f && c3d.isDrawingSolidMaterials() || !c3d.isDrawingSolidMaterials() && a == 1f) {
            if (c3d.isLightOn() && (next == null || next.type() != 2 && next.type() != 5))
                GL11.glEnable(GL11.GL_LIGHTING);
            return;
        }

        final Matrix4f m2 = GData.CACHE_viewByProjection.get(parent);
        if (m2 == null) {
            Matrix4f.mul(c3d.getViewport(), parent.productMatrix, m);
            GData.CACHE_viewByProjection.put(parent, m);
        } else {
            m = m2;
        }

        float result;
        int lineMode = c3d.getLineMode();
        float zoom = c3d.getZoom();

        switch (lineMode) {
        case 1:
            result = 1f;
            break;
        case 2:
        case 4:
            if (c3d.isLightOn() && (next == null || next.type() != 2 && next.type() != 5))
                GL11.glEnable(GL11.GL_LIGHTING);
            return;
        default:
            // Calculate the real coordinates
            Matrix4f.transform(m, sA2, sA);
            Matrix4f.transform(m, sB2, sB);
            Matrix4f.transform(m, sC2, sC);
            Matrix4f.transform(m, sD2, sD);

            n.x = sA.y - sB.y;
            n.y = sB.x - sA.x;

            result = zoom / Vector4f.dot(n, Vector4f.sub(sC, sA, null)) * Vector4f.dot(n, Vector4f.sub(sD, sA, null));
            break;
        }

        if (result > -1e-20f) {

            final float r = MathHelper.randomFloat(id, 0);
            final float g = MathHelper.randomFloat(id, 1);
            final float b = MathHelper.randomFloat(id, 2);

            if (GL11.glGetBoolean(GL11.GL_LIGHTING) == 1) GL11.glDisable(GL11.GL_LIGHTING);

            if (zoom > View.edgeThreshold) {

                GL11.glColor4f(r, g, b, a);

                GL11.glPushMatrix();

                GL11.glScalef(lGeom[20][0], lGeom[20][1], lGeom[20][2]);

                if (GData.globalNegativeDeterminant) {

                    GL20Primitives.sphereInv.draw(lGeom[18][0], lGeom[18][1], lGeom[18][2]);

                    GL11.glBegin(GL11.GL_QUAD_STRIP);
                    GL11.glVertex3f(lGeom[1][0], lGeom[1][1], lGeom[1][2]);
                    GL11.glVertex3f(lGeom[0][0], lGeom[0][1], lGeom[0][2]);
                    GL11.glVertex3f(lGeom[3][0], lGeom[3][1], lGeom[3][2]);
                    GL11.glVertex3f(lGeom[2][0], lGeom[2][1], lGeom[2][2]);
                    GL11.glVertex3f(lGeom[5][0], lGeom[5][1], lGeom[5][2]);
                    GL11.glVertex3f(lGeom[4][0], lGeom[4][1], lGeom[4][2]);
                    GL11.glVertex3f(lGeom[7][0], lGeom[7][1], lGeom[7][2]);
                    GL11.glVertex3f(lGeom[6][0], lGeom[6][1], lGeom[6][2]);
                    GL11.glVertex3f(lGeom[9][0], lGeom[9][1], lGeom[9][2]);
                    GL11.glVertex3f(lGeom[8][0], lGeom[8][1], lGeom[8][2]);
                    GL11.glVertex3f(lGeom[11][0], lGeom[11][1], lGeom[11][2]);
                    GL11.glVertex3f(lGeom[10][0], lGeom[10][1], lGeom[10][2]);
                    GL11.glVertex3f(lGeom[13][0], lGeom[13][1], lGeom[13][2]);
                    GL11.glVertex3f(lGeom[12][0], lGeom[12][1], lGeom[12][2]);
                    GL11.glVertex3f(lGeom[15][0], lGeom[15][1], lGeom[15][2]);
                    GL11.glVertex3f(lGeom[14][0], lGeom[14][1], lGeom[14][2]);
                    GL11.glVertex3f(lGeom[17][0], lGeom[17][1], lGeom[17][2]);
                    GL11.glVertex3f(lGeom[16][0], lGeom[16][1], lGeom[16][2]);
                    GL11.glEnd();

                    GL20Primitives.sphereInv.draw(lGeom[19][0], lGeom[19][1], lGeom[19][2]);

                } else {

                    GL20Primitives.sphere.draw(lGeom[18][0], lGeom[18][1], lGeom[18][2]);

                    GL11.glBegin(GL11.GL_QUAD_STRIP);
                    GL11.glVertex3f(lGeom[0][0], lGeom[0][1], lGeom[0][2]);
                    GL11.glVertex3f(lGeom[1][0], lGeom[1][1], lGeom[1][2]);
                    GL11.glVertex3f(lGeom[2][0], lGeom[2][1], lGeom[2][2]);
                    GL11.glVertex3f(lGeom[3][0], lGeom[3][1], lGeom[3][2]);
                    GL11.glVertex3f(lGeom[4][0], lGeom[4][1], lGeom[4][2]);
                    GL11.glVertex3f(lGeom[5][0], lGeom[5][1], lGeom[5][2]);
                    GL11.glVertex3f(lGeom[6][0], lGeom[6][1], lGeom[6][2]);
                    GL11.glVertex3f(lGeom[7][0], lGeom[7][1], lGeom[7][2]);
                    GL11.glVertex3f(lGeom[8][0], lGeom[8][1], lGeom[8][2]);
                    GL11.glVertex3f(lGeom[9][0], lGeom[9][1], lGeom[9][2]);
                    GL11.glVertex3f(lGeom[10][0], lGeom[10][1], lGeom[10][2]);
                    GL11.glVertex3f(lGeom[11][0], lGeom[11][1], lGeom[11][2]);
                    GL11.glVertex3f(lGeom[12][0], lGeom[12][1], lGeom[12][2]);
                    GL11.glVertex3f(lGeom[13][0], lGeom[13][1], lGeom[13][2]);
                    GL11.glVertex3f(lGeom[14][0], lGeom[14][1], lGeom[14][2]);
                    GL11.glVertex3f(lGeom[15][0], lGeom[15][1], lGeom[15][2]);
                    GL11.glVertex3f(lGeom[16][0], lGeom[16][1], lGeom[16][2]);
                    GL11.glVertex3f(lGeom[17][0], lGeom[17][1], lGeom[17][2]);
                    GL11.glEnd();

                    GL20Primitives.sphere.draw(lGeom[19][0], lGeom[19][1], lGeom[19][2]);

                }

                GL11.glPopMatrix();

            } else {
                GL11.glLineWidth(View.LINE_WIDTH_GL[0]);
                GL11.glColor4f(r, g, b, a);
                GL11.glBegin(GL11.GL_LINES);
                GL11.glVertex3f(x1, y1, z1);
                GL11.glVertex3f(x2, y2, z2);
                GL11.glEnd();
            }
        }
        if (c3d.isLightOn() && (next == null || next.type() != 2 && next.type() != 5))
            GL11.glEnable(GL11.GL_LIGHTING);
    }

    @Override
    public void drawGL20WhileAddCondlines(Composite3D c3d) {
        if (!visible) {
            if (c3d.isLightOn() && (next == null || next.type() != 2 && next.type() != 5))
                GL11.glEnable(GL11.GL_LIGHTING);
            return;
        }
        if (!c3d.isDrawingSolidMaterials()) {
            if (c3d.isLightOn() && (next == null || next.type() != 2 && next.type() != 5))
                GL11.glEnable(GL11.GL_LIGHTING);
            return;
        }

        final float zoom = c3d.getZoom();

        final Matrix4f m2 = GData.CACHE_viewByProjection.get(parent);
        if (m2 == null) {
            Matrix4f.mul(c3d.getViewport(), parent.productMatrix, m);
            GData.CACHE_viewByProjection.put(parent, m);
        } else {
            m = m2;
        }
        // Calculate the real coordinates
        Matrix4f.transform(m, sA2, sA);
        Matrix4f.transform(m, sB2, sB);
        Matrix4f.transform(m, sC2, sC);
        Matrix4f.transform(m, sD2, sD);

        n.x = sA.y - sB.y;
        n.y = sB.x - sA.x;

        final float result = zoom / Vector4f.dot(n, Vector4f.sub(sC, sA, null)) * Vector4f.dot(n, Vector4f.sub(sD, sA, null));

        if (GL11.glGetBoolean(GL11.GL_LIGHTING) == 1) GL11.glDisable(GL11.GL_LIGHTING);

        if (zoom > View.edgeThreshold) {

            if (result > -1e-20f) {
                GL11.glColor4f(0f, 0f, 0f, 1f);
                wasShown = true;
            } else {
                if (wasShown) {
                    GL11.glColor4f(View.CONDLINE_SHOWN_COLOUR_R[0], View.CONDLINE_SHOWN_COLOUR_G[0], View.CONDLINE_SHOWN_COLOUR_B[0], 1f);
                } else {
                    GL11.glColor4f(View.CONDLINE_HIDDEN_COLOUR_R[0], View.CONDLINE_HIDDEN_COLOUR_G[0], View.CONDLINE_HIDDEN_COLOUR_B[0], 1f);
                }
            }

            GL11.glPushMatrix();

            GL11.glScalef(lGeom[20][0], lGeom[20][1], lGeom[20][2]);

            if (GData.globalNegativeDeterminant) {

                GL20Primitives.sphereInv.draw(lGeom[18][0], lGeom[18][1], lGeom[18][2]);

                GL11.glBegin(GL11.GL_QUAD_STRIP);
                GL11.glVertex3f(lGeom[1][0], lGeom[1][1], lGeom[1][2]);
                GL11.glVertex3f(lGeom[0][0], lGeom[0][1], lGeom[0][2]);
                GL11.glVertex3f(lGeom[3][0], lGeom[3][1], lGeom[3][2]);
                GL11.glVertex3f(lGeom[2][0], lGeom[2][1], lGeom[2][2]);
                GL11.glVertex3f(lGeom[5][0], lGeom[5][1], lGeom[5][2]);
                GL11.glVertex3f(lGeom[4][0], lGeom[4][1], lGeom[4][2]);
                GL11.glVertex3f(lGeom[7][0], lGeom[7][1], lGeom[7][2]);
                GL11.glVertex3f(lGeom[6][0], lGeom[6][1], lGeom[6][2]);
                GL11.glVertex3f(lGeom[9][0], lGeom[9][1], lGeom[9][2]);
                GL11.glVertex3f(lGeom[8][0], lGeom[8][1], lGeom[8][2]);
                GL11.glVertex3f(lGeom[11][0], lGeom[11][1], lGeom[11][2]);
                GL11.glVertex3f(lGeom[10][0], lGeom[10][1], lGeom[10][2]);
                GL11.glVertex3f(lGeom[13][0], lGeom[13][1], lGeom[13][2]);
                GL11.glVertex3f(lGeom[12][0], lGeom[12][1], lGeom[12][2]);
                GL11.glVertex3f(lGeom[15][0], lGeom[15][1], lGeom[15][2]);
                GL11.glVertex3f(lGeom[14][0], lGeom[14][1], lGeom[14][2]);
                GL11.glVertex3f(lGeom[17][0], lGeom[17][1], lGeom[17][2]);
                GL11.glVertex3f(lGeom[16][0], lGeom[16][1], lGeom[16][2]);
                GL11.glEnd();

                GL20Primitives.sphereInv.draw(lGeom[19][0], lGeom[19][1], lGeom[19][2]);

            } else {

                GL20Primitives.sphere.draw(lGeom[18][0], lGeom[18][1], lGeom[18][2]);

                GL11.glBegin(GL11.GL_QUAD_STRIP);
                GL11.glVertex3f(lGeom[0][0], lGeom[0][1], lGeom[0][2]);
                GL11.glVertex3f(lGeom[1][0], lGeom[1][1], lGeom[1][2]);
                GL11.glVertex3f(lGeom[2][0], lGeom[2][1], lGeom[2][2]);
                GL11.glVertex3f(lGeom[3][0], lGeom[3][1], lGeom[3][2]);
                GL11.glVertex3f(lGeom[4][0], lGeom[4][1], lGeom[4][2]);
                GL11.glVertex3f(lGeom[5][0], lGeom[5][1], lGeom[5][2]);
                GL11.glVertex3f(lGeom[6][0], lGeom[6][1], lGeom[6][2]);
                GL11.glVertex3f(lGeom[7][0], lGeom[7][1], lGeom[7][2]);
                GL11.glVertex3f(lGeom[8][0], lGeom[8][1], lGeom[8][2]);
                GL11.glVertex3f(lGeom[9][0], lGeom[9][1], lGeom[9][2]);
                GL11.glVertex3f(lGeom[10][0], lGeom[10][1], lGeom[10][2]);
                GL11.glVertex3f(lGeom[11][0], lGeom[11][1], lGeom[11][2]);
                GL11.glVertex3f(lGeom[12][0], lGeom[12][1], lGeom[12][2]);
                GL11.glVertex3f(lGeom[13][0], lGeom[13][1], lGeom[13][2]);
                GL11.glVertex3f(lGeom[14][0], lGeom[14][1], lGeom[14][2]);
                GL11.glVertex3f(lGeom[15][0], lGeom[15][1], lGeom[15][2]);
                GL11.glVertex3f(lGeom[16][0], lGeom[16][1], lGeom[16][2]);
                GL11.glVertex3f(lGeom[17][0], lGeom[17][1], lGeom[17][2]);
                GL11.glEnd();

                GL20Primitives.sphere.draw(lGeom[19][0], lGeom[19][1], lGeom[19][2]);

            }

            GL11.glPopMatrix();

        } else {
            GL11.glLineWidth(View.LINE_WIDTH_GL[0]);
            if (result > -1e-20f) {
                GL11.glColor4f(0f, 0f, 0f, 1f);
                wasShown = true;
            } else {
                if (wasShown) {
                    GL11.glColor4f(View.CONDLINE_SHOWN_COLOUR_R[0], View.CONDLINE_SHOWN_COLOUR_G[0], View.CONDLINE_SHOWN_COLOUR_B[0], 1f);
                } else {
                    GL11.glColor4f(View.CONDLINE_HIDDEN_COLOUR_R[0], View.CONDLINE_HIDDEN_COLOUR_G[0], View.CONDLINE_HIDDEN_COLOUR_B[0], 1f);
                }
            }
            GL11.glBegin(GL11.GL_LINES);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glEnd();
        }
        if (c3d.isLightOn() && (next == null || next.type() != 2 && next.type() != 5))
            GL11.glEnable(GL11.GL_LIGHTING);
    }

    @Override
    public void drawGL20BFC(Composite3D c3d) {
        drawGL20(c3d);
    }

    @Override
    public void drawGL20BFCuncertified(Composite3D c3d) {
        drawGL20(c3d);
    }

    @Override
    public void drawGL20BFCbackOnly(Composite3D c3d) {
        drawGL20(c3d);
    }

    @Override
    public void drawGL20BFCcolour(Composite3D c3d) {
        drawGL20(c3d);
    }

    @Override
    public void drawGL20BFCtextured(Composite3D c3d) {
        // done :)
        if (GData.globalDrawObjects) {
            final OpenGLRenderer20 r = (OpenGLRenderer20) c3d.getRenderer();
            GL20.glUniform1f(r.getNormalSwitchLoc(), GData.globalNegativeDeterminant ^ GData.globalInvertNext ? 1f : 0f);
            GL20.glUniform1f(r.getNoTextureSwitch(), 1f);
            GL20.glUniform1f(r.getNoLightSwitch(), 1f);
            GL20.glUniform1f(r.getCubeMapSwitch(), 0f);

            if (!visible)
                return;
            if (!c3d.isDrawingSolidMaterials())
                return;

            float result;
            float zoom = c3d.getZoom();
            switch (c3d.getLineMode()) {
            case 1:
                result = 1f;
                break;
            case 2:
            case 4:
                return;
            default:
                final Matrix4f m2 = GData.CACHE_viewByProjection.get(parent);
                if (m2 == null) {
                    Matrix4f.mul(c3d.getViewport(), parent.productMatrix, m);
                    GData.CACHE_viewByProjection.put(parent, m);
                } else {
                    m = m2;
                }
                // Calculate the real coordinates
                Matrix4f.transform(m, sA2, sA);
                Matrix4f.transform(m, sB2, sB);
                Matrix4f.transform(m, sC2, sC);
                Matrix4f.transform(m, sD2, sD);

                n.x = sA.y - sB.y;
                n.y = sB.x - sA.x;

                result = zoom / Vector4f.dot(n, Vector4f.sub(sC, sA, null)) * Vector4f.dot(n, Vector4f.sub(sD, sA, null));
                break;
            }

            if (result > -1e-20f) {

                float r2;
                float g2;
                float b2;
                int cn;
                if (colourNumber == 24 && (cn = parent.r == .5f && parent.g == .5f && parent.b == .5f && (parent.a == 1.1f || parent.a == -1)  ? 16 : View.getLDConfigIndex(parent.r,  parent.g,  parent.b)) != 16) {
                    GColour c = View.getLDConfigEdgeColour(cn, c3d);
                    r2 = c.getR();
                    g2 = c.getG();
                    b2 = c.getB();
                } else {
                    r2 = this.r;
                    g2 = this.g;
                    b2 = this.b;
                }

                GL11.glLineWidth(View.LINE_WIDTH_GL[0]);
                GL11.glColor4f(r2, g2, b2, 1f);
                GL11.glBegin(GL11.GL_LINES);
                GraphicalDataTools.setVertex(x1, y1, z1, this, true);
                GraphicalDataTools.setVertex(x2, y2, z2, this, true);
                GL11.glEnd();
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
    public void drawGL20CoplanarityHeatmap(Composite3D c3d) {
        drawGL20(c3d);
    }

    @Override
    public void drawGL20Wireframe(Composite3D c3d) {}

    @Override
    public int type() {
        return 5;
    }

    @Override
    String getNiceString() {
        if (text != null)
            return text;
        StringBuilder lineBuilder = new StringBuilder();
        lineBuilder.append("5 "); //$NON-NLS-1$
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
        lineBuilder.append(5);
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
    public void getBFCorientationMap(HashMap<GData, BFC> map) {
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
    public void getBFCorientationMapNOCERTIFY(HashMap<GData, BFC> map) {
        // Don't get it for NOCERTIFY.
    }

    @Override
    public void getBFCorientationMapNOCLIP(HashMap<GData, BFC> map) {
        map.put(this, BFC.NOCLIP);
    }

    @Override
    public void getVertexNormalMap(GDataState state, ThreadsafeTreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {}

    @Override
    public void getVertexNormalMapNOCERTIFY(GDataState state, ThreadsafeTreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {}

    @Override
    public void getVertexNormalMapNOCLIP(GDataState state, ThreadsafeTreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {}

    String colourReplace(String col) {
        StringBuilder lineBuilder = new StringBuilder();
        lineBuilder.append(5);
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

    void isShown(Matrix4f viewport, ThreadsafeHashMap<GData1, Matrix4f> cacheViewByProjection, float zoom) {

        if (wasShown) {
            return;
        }

        final Matrix4f m2 = cacheViewByProjection.get(parent);
        if (m2 == null) {
            Matrix4f.mul(viewport, parent.productMatrix, m);
            cacheViewByProjection.put(parent, m);
        } else {
            m = m2;
        }

        // Calculate the real coordinates
        Matrix4f.transform(m, sA2, sA);
        Matrix4f.transform(m, sB2, sB);
        Matrix4f.transform(m, sC2, sC);
        Matrix4f.transform(m, sD2, sD);

        n.x = sA.y - sB.y;
        n.y = sB.x - sA.x;
        n.z = 0f;
        n.w = 1f;
        wasShown = zoom / Vector4f.dot(n, Vector4f.sub(sC, sA, null)) * Vector4f.dot(n, Vector4f.sub(sD, sA, null)) > -1e-20f;
    }

    boolean wasShown() {
        return wasShown;
    }
}
