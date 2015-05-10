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
import org.nschmidt.ldparteditor.enums.GLPrimitives;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;

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

    final BigDecimal X1;
    final BigDecimal Y1;
    final BigDecimal Z1;
    final BigDecimal X2;
    final BigDecimal Y2;
    final BigDecimal Z2;
    final BigDecimal X3;
    final BigDecimal Y3;
    final BigDecimal Z3;
    final BigDecimal X4;
    final BigDecimal Y4;
    final BigDecimal Z4;

    private final Vector4f A = new Vector4f(0, 0, 0, 1f);
    private final Vector4f B = new Vector4f(0, 0, 0, 1f);
    private final Vector4f C = new Vector4f(0, 0, 0, 1f);
    private final Vector4f D = new Vector4f(0, 0, 0, 1f);

    private final Vector4f A2 = new Vector4f(0, 0, 0, 1f);
    private final Vector4f B2 = new Vector4f(0, 0, 0, 1f);
    private final Vector4f C2 = new Vector4f(0, 0, 0, 1f);
    private final Vector4f D2 = new Vector4f(0, 0, 0, 1f);

    private final Vector4f N = new Vector4f(0, 0, 0, 1f);
    private Matrix4f M = new Matrix4f();

    private final float[][] lGeom;
    final GData1 parent;

    private boolean wasShown = false;

    public GData5(final int colourNumber, float r, float g, float b, float a, BigDecimal x1, BigDecimal y1, BigDecimal z1, BigDecimal x2, BigDecimal y2, BigDecimal z2, BigDecimal x3, BigDecimal y3,
            BigDecimal z3, BigDecimal x4, BigDecimal y4, BigDecimal z4, GData1 parentFileRef, DatFile datFile) {

        this.parent = parentFileRef;
        this.colourNumber = colourNumber;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.lGeom = MathHelper.getLineVertices(new Vector4f(x1.floatValue(), y1.floatValue(), z1.floatValue(), 1f), new Vector4f(x2.floatValue(), y2.floatValue(), z2.floatValue(), 1f),
                parent.productMatrix);
        this.X1 = x1;
        this.Y1 = y1;
        this.Z1 = z1;
        this.X2 = x2;
        this.Y2 = y2;
        this.Z2 = z2;
        this.X3 = x3;
        this.Y3 = y3;
        this.Z3 = z3;
        this.X4 = x4;
        this.Y4 = y4;
        this.Z4 = z4;
        this.x1 = X1.floatValue() * 1000f;
        this.y1 = Y1.floatValue() * 1000f;
        this.z1 = Z1.floatValue() * 1000f;
        this.x2 = X2.floatValue() * 1000f;
        this.y2 = Y2.floatValue() * 1000f;
        this.z2 = Z2.floatValue() * 1000f;
        this.x3 = X3.floatValue() * 1000f;
        this.y3 = Y3.floatValue() * 1000f;
        this.z3 = Z3.floatValue() * 1000f;
        this.x4 = X4.floatValue() * 1000f;
        this.y4 = Y4.floatValue() * 1000f;
        this.z4 = Z4.floatValue() * 1000f;
        datFile.getVertexManager().add(this);
        A2.x = this.x1;
        B2.x = this.x2;
        C2.x = this.x3;
        D2.x = this.x4;
        A2.y = this.y1;
        B2.y = this.y2;
        C2.y = this.y3;
        D2.y = this.y4;
        A2.z = this.z1;
        B2.z = this.z2;
        C2.z = this.z3;
        D2.z = this.z4;

    }

    public GData5(boolean superFast, final int colourNumber, float r, float g, float b, float a, BigDecimal x1, BigDecimal y1, BigDecimal z1, BigDecimal x2, BigDecimal y2, BigDecimal z2,
            BigDecimal x3, BigDecimal y3, BigDecimal z3, BigDecimal x4, BigDecimal y4, BigDecimal z4, float x12, float y12, float z12, float x22, float y22, float z22, float x32, float y32,
            float z32, float x42, float y42, float z42, GData1 parentFileRef, DatFile datFile) {

        this.parent = parentFileRef;
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
        this.X4 = x4;
        this.Y4 = y4;
        this.Z4 = z4;
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
        this.lGeom = MathHelper.getLineVertices1000(new Vector4f(this.x1, this.y1, this.z1, 1f), new Vector4f(this.x2, this.y2, this.z2, 1f), parent.productMatrix);
        datFile.getVertexManager().add(this);
        A2.x = this.x1;
        B2.x = this.x2;
        C2.x = this.x3;
        D2.x = this.x4;
        A2.y = this.y1;
        B2.y = this.y2;
        C2.y = this.y3;
        D2.y = this.y4;
        A2.z = this.z1;
        B2.z = this.z2;
        C2.z = this.z3;
        D2.z = this.z4;

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
        this.X1 = v1.X;
        this.Y1 = v1.Y;
        this.Z1 = v1.Z;
        this.X2 = v2.X;
        this.Y2 = v2.Y;
        this.Z2 = v2.Z;
        this.X3 = v3.X;
        this.Y3 = v3.Y;
        this.Z3 = v3.Z;
        this.X4 = v4.X;
        this.Y4 = v4.Y;
        this.Z4 = v4.Z;
        this.parent = parent;
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
        this.lGeom = MathHelper.getLineVertices1000(new Vector4f(x1, y1, z1, 1f), new Vector4f(x2, y2, z2, 1f), parent.productMatrix);
        this.parent = parent;
        A2.x = this.x1;
        B2.x = this.x2;
        C2.x = this.x3;
        D2.x = this.x4;
        A2.y = this.y1;
        B2.y = this.y2;
        C2.y = this.y3;
        D2.y = this.y4;
        A2.z = this.z1;
        B2.z = this.z2;
        C2.z = this.z3;
        D2.z = this.z4;
        this.X1 = null;
        this.Y1 = null;
        this.Z1 = null;
        this.X2 = null;
        this.Y2 = null;
        this.Z2 = null;
        this.X3 = null;
        this.Y3 = null;
        this.Z3 = null;
        this.X4 = null;
        this.Y4 = null;
        this.Z4 = null;
    }

    /**
     * FOR PRIMITIVES ONLY
     */
    public GData5(GColour c, GData1 parent, Vertex v1, Vertex v2, Vertex v3, Vertex v4) {
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
        this.lGeom = null; // MathHelper.getLineVertices1000(new Vector4f(x1, y1, z1, 1f), new Vector4f(x2, y2, z2, 1f), parent.productMatrix);
        this.parent = parent;
        this.X1 = null;
        this.Y1 = null;
        this.Z1 = null;
        this.X2 = null;
        this.Y2 = null;
        this.Z2 = null;
        this.X3 = null;
        this.Y3 = null;
        this.Z3 = null;
        this.X4 = null;
        this.Y4 = null;
        this.Z4 = null;
    }

    public GData5(final int colourNumber, float r, float g, float b, float a, Vertex v1, Vertex v2, Vertex v3, Vertex v4, GData1 parent, DatFile datFile) {

        this.colourNumber = colourNumber;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.X1 = v1.X;
        this.Y1 = v1.Y;
        this.Z1 = v1.Z;
        this.X2 = v2.X;
        this.Y2 = v2.Y;
        this.Z2 = v2.Z;
        this.X3 = v3.X;
        this.Y3 = v3.Y;
        this.Z3 = v3.Z;
        this.X4 = v4.X;
        this.Y4 = v4.Y;
        this.Z4 = v4.Z;
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
        this.lGeom = MathHelper.getLineVertices1000(new Vector4f(x1, y1, z1, 1f), new Vector4f(x2, y2, z2, 1f), parent.productMatrix);
        this.parent = parent;
        datFile.getVertexManager().add(this);
        A2.x = this.x1;
        B2.x = this.x2;
        C2.x = this.x3;
        D2.x = this.x4;
        A2.y = this.y1;
        B2.y = this.y2;
        C2.y = this.y3;
        D2.y = this.y4;
        A2.z = this.z1;
        B2.z = this.z2;
        C2.z = this.z3;
        D2.z = this.z4;
    }

    @Override
    public void draw(Composite3D c3d) {
        if (!visible)
            return;
        if (a < 1f && c3d.isDrawingSolidMaterials() || !c3d.isDrawingSolidMaterials() && a == 1f)
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
            if (GData.CACHE_viewByProjection.containsKey(parent)) {
                M = GData.CACHE_viewByProjection.get(parent);
            } else {
                Matrix4f.mul(c3d.getViewport(), parent.productMatrix, M);
                GData.CACHE_viewByProjection.put(parent, M);
            }
            // Calculate the real coordinates
            Matrix4f.transform(M, A2, A);
            Matrix4f.transform(M, B2, B);
            Matrix4f.transform(M, C2, C);
            Matrix4f.transform(M, D2, D);

            N.x = A.y - B.y;
            N.y = B.x - A.x;

            result = zoom / Vector4f.dot(N, Vector4f.sub(C, A, null)) * Vector4f.dot(N, Vector4f.sub(D, A, null));
            break;
        }

        if (result > -1e-20f) {

            GL11.glDisable(GL11.GL_LIGHTING);

            if (zoom > 5e-6) {

                GL11.glColor4f(r, g, b, a);

                GL11.glPushMatrix();

                GL11.glScalef(lGeom[19][0], lGeom[19][1], lGeom[19][2]);

                if (GData.globalNegativeDeterminant) {

                    GLPrimitives.SPHERE_INV.draw(x1 * lGeom[18][0], y1 * lGeom[18][1], z1 * lGeom[18][2]);

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

                    GLPrimitives.SPHERE_INV.draw(x2 * lGeom[18][0], y2 * lGeom[18][1], z2 * lGeom[18][2]);

                } else {

                    GLPrimitives.SPHERE.draw(x1 * lGeom[18][0], y1 * lGeom[18][1], z1 * lGeom[18][2]);

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

                    GLPrimitives.SPHERE.draw(x2 * lGeom[18][0], y2 * lGeom[18][1], z2 * lGeom[18][2]);

                }

                GL11.glPopMatrix();

            } else {
                GL11.glLineWidth(View.lineWidthGL[0]);
                GL11.glColor4f(r, g, b, a);
                GL11.glBegin(GL11.GL_LINES);
                GL11.glVertex3f(x1, y1, z1);
                GL11.glVertex3f(x2, y2, z2);
                GL11.glEnd();
            }
            if (c3d.isLightOn())
                GL11.glEnable(GL11.GL_LIGHTING);
        }
    }

    @Override
    public void drawRandomColours(Composite3D c3d) {
        if (!visible)
            return;
        if (a < 1f && c3d.isDrawingSolidMaterials() || !c3d.isDrawingSolidMaterials() && a == 1f)
            return;

        if (GData.CACHE_viewByProjection.containsKey(parent)) {
            M = GData.CACHE_viewByProjection.get(parent);
        } else {
            Matrix4f.mul(c3d.getViewport(), parent.productMatrix, M);
            GData.CACHE_viewByProjection.put(parent, M);
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
            return;
        default:
            // Calculate the real coordinates
            Matrix4f.transform(M, A2, A);
            Matrix4f.transform(M, B2, B);
            Matrix4f.transform(M, C2, C);
            Matrix4f.transform(M, D2, D);

            N.x = A.y - B.y;
            N.y = B.x - A.x;

            result = zoom / Vector4f.dot(N, Vector4f.sub(C, A, null)) * Vector4f.dot(N, Vector4f.sub(D, A, null));
            break;
        }

        if (result > -1e-20f) {

            final float r = MathHelper.randomFloat(ID, 0);
            final float g = MathHelper.randomFloat(ID, 1);
            final float b = MathHelper.randomFloat(ID, 2);

            GL11.glDisable(GL11.GL_LIGHTING);

            if (zoom > 5e-6) {

                GL11.glColor4f(r, g, b, a);

                GL11.glPushMatrix();

                GL11.glScalef(lGeom[19][0], lGeom[19][1], lGeom[19][2]);

                if (GData.globalNegativeDeterminant) {

                    GLPrimitives.SPHERE_INV.draw(x1 * lGeom[18][0], y1 * lGeom[18][1], z1 * lGeom[18][2]);

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

                    GLPrimitives.SPHERE_INV.draw(x2 * lGeom[18][0], y2 * lGeom[18][1], z2 * lGeom[18][2]);

                } else {

                    GLPrimitives.SPHERE.draw(x1 * lGeom[18][0], y1 * lGeom[18][1], z1 * lGeom[18][2]);

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

                    GLPrimitives.SPHERE.draw(x2 * lGeom[18][0], y2 * lGeom[18][1], z2 * lGeom[18][2]);

                }

                GL11.glPopMatrix();

            } else {
                GL11.glLineWidth(View.lineWidthGL[0]);
                GL11.glColor4f(r, g, b, a);
                GL11.glBegin(GL11.GL_LINES);
                GL11.glVertex3f(x1, y1, z1);
                GL11.glVertex3f(x2, y2, z2);
                GL11.glEnd();
            }
            if (c3d.isLightOn())
                GL11.glEnable(GL11.GL_LIGHTING);
        }
    }

    @Override
    public void drawWhileAddCondlines(Composite3D c3d) {
        if (!visible)
            return;
        if (!c3d.isDrawingSolidMaterials())
            return;

        final float zoom = c3d.getZoom();

        if (GData.CACHE_viewByProjection.containsKey(parent)) {
            M = GData.CACHE_viewByProjection.get(parent);
        } else {
            Matrix4f.mul(c3d.getViewport(), parent.productMatrix, M);
            GData.CACHE_viewByProjection.put(parent, M);
        }
        // Calculate the real coordinates
        Matrix4f.transform(M, A2, A);
        Matrix4f.transform(M, B2, B);
        Matrix4f.transform(M, C2, C);
        Matrix4f.transform(M, D2, D);

        N.x = A.y - B.y;
        N.y = B.x - A.x;

        final float result = zoom / Vector4f.dot(N, Vector4f.sub(C, A, null)) * Vector4f.dot(N, Vector4f.sub(D, A, null));

        GL11.glDisable(GL11.GL_LIGHTING);

        if (zoom > 5e-6) {

            if (result > -1e-20f) {
                GL11.glColor4f(0f, 0f, 0f, 1f);
                wasShown = true;
            } else {
                if (wasShown) {
                    GL11.glColor4f(.553f, .22f, .789f, 1f);
                } else {
                    GL11.glColor4f(1f, .44f, .1f, 1f);
                }
            }

            GL11.glPushMatrix();

            GL11.glScalef(lGeom[19][0], lGeom[19][1], lGeom[19][2]);

            if (GData.globalNegativeDeterminant) {

                GLPrimitives.SPHERE_INV.draw(x1 * lGeom[18][0], y1 * lGeom[18][1], z1 * lGeom[18][2]);

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

                GLPrimitives.SPHERE_INV.draw(x2 * lGeom[18][0], y2 * lGeom[18][1], z2 * lGeom[18][2]);

            } else {

                GLPrimitives.SPHERE.draw(x1 * lGeom[18][0], y1 * lGeom[18][1], z1 * lGeom[18][2]);

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

                GLPrimitives.SPHERE.draw(x2 * lGeom[18][0], y2 * lGeom[18][1], z2 * lGeom[18][2]);

            }

            GL11.glPopMatrix();

        } else {
            GL11.glLineWidth(View.lineWidthGL[0]);
            if (result > -1e-20f) {
                GL11.glColor4f(0f, 0f, 0f, 1f);
                wasShown = true;
            } else {
                if (wasShown) {
                    GL11.glColor4f(.553f, .22f, .789f, 1f);
                } else {
                    GL11.glColor4f(1f, .44f, .1f, 1f);
                }
            }
            GL11.glBegin(GL11.GL_LINES);
            GL11.glVertex3f(x1, y1, z1);
            GL11.glVertex3f(x2, y2, z2);
            GL11.glEnd();
        }
        if (c3d.isLightOn())
            GL11.glEnable(GL11.GL_LIGHTING);
    }

    @Override
    public void drawBFC(Composite3D c3d) {
        draw(c3d);
    }

    @Override
    public void drawBFCuncertified(Composite3D c3d) {
        draw(c3d);
    }

    @Override
    public void drawBFC_backOnly(Composite3D c3d) {
        draw(c3d);
    }

    @Override
    public void drawBFC_Colour(Composite3D c3d) {
        draw(c3d);
    }

    @Override
    public void drawBFC_Textured(Composite3D c3d) {
        // done :)
        if (GData.globalDrawObjects) {
            final OpenGLRenderer r = c3d.getRenderer();
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
                if (GData.CACHE_viewByProjection.containsKey(parent)) {
                    M = GData.CACHE_viewByProjection.get(parent);
                } else {
                    Matrix4f.mul(c3d.getViewport(), parent.productMatrix, M);
                    GData.CACHE_viewByProjection.put(parent, M);
                }
                // Calculate the real coordinates
                Matrix4f.transform(M, A2, A);
                Matrix4f.transform(M, B2, B);
                Matrix4f.transform(M, C2, C);
                Matrix4f.transform(M, D2, D);

                N.x = A.y - B.y;
                N.y = B.x - A.x;

                result = zoom / Vector4f.dot(N, Vector4f.sub(C, A, null)) * Vector4f.dot(N, Vector4f.sub(D, A, null));
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

                GL11.glLineWidth(View.lineWidthGL[0]);
                GL11.glColor4f(r2, g2, b2, 1f);
                GL11.glBegin(GL11.GL_LINES);
                c3d.getVertexManager().setVertex(x1, y1, z1, this, true);
                c3d.getVertexManager().setVertex(x2, y2, z2, this, true);
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
    public int type() {
        return 5;
    }

    @Override
    String getNiceString() {
        if (text != null)
            return text;
        StringBuilder lineBuilder = useAgain();
        lineBuilder.append(5);
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
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(X4));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(Y4));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(Z4));
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
        BigDecimal[] v4;
        if (X1 == null) {
            v1 = matrix.transform(new BigDecimal(x1 / 1000f), new BigDecimal(y1 / 1000f), new BigDecimal(z1 / 1000f));
            v2 = matrix.transform(new BigDecimal(x2 / 1000f), new BigDecimal(y2 / 1000f), new BigDecimal(z2 / 1000f));
            v3 = matrix.transform(new BigDecimal(x3 / 1000f), new BigDecimal(y3 / 1000f), new BigDecimal(z3 / 1000f));
            v4 = matrix.transform(new BigDecimal(x4 / 1000f), new BigDecimal(y4 / 1000f), new BigDecimal(z4 / 1000f));
        } else {
            v1 = matrix.transform(X1, Y1, Z1);
            v2 = matrix.transform(X2, Y2, Z2);
            v3 = matrix.transform(X3, Y3, Z3);
            v4 = matrix.transform(X4, Y4, Z4);
        }
        StringBuilder lineBuilder = useAgain();
        lineBuilder.append(5);
        lineBuilder.append(" "); //$NON-NLS-1$
        StringBuilder colourBuilder = useAgain2();
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
    public void getVertexNormalMap(TreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, HashMap<GData, float[]> dataLinkedToNormalCACHE, VertexManager vm) {}

    @Override
    public void getVertexNormalMapNOCERTIFY(TreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, HashMap<GData, float[]> dataLinkedToNormalCACHE, VertexManager vm) {}

    @Override
    public void getVertexNormalMapNOCLIP(TreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, HashMap<GData, float[]> dataLinkedToNormalCACHE, VertexManager vm) {}

    public String colourReplace(String col) {
        StringBuilder lineBuilder = useAgain();
        lineBuilder.append(5);
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
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(X4));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(Y4));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(Z4));
        return lineBuilder.toString();
    }
}
