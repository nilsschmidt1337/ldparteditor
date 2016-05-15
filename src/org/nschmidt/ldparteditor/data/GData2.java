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
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.enums.GLPrimitives;
import org.nschmidt.ldparteditor.enums.ManipulatorScope;
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeTreeMap;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;

/**
 * @author nils
 *
 */
public final class GData2 extends GData {

    final int colourNumber;
    public final boolean isLine;

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

    final BigDecimal X1;
    final BigDecimal Y1;
    final BigDecimal Z1;
    final BigDecimal X2;
    final BigDecimal Y2;
    final BigDecimal Z2;

    final float[][] lGeom;

    final GData1 parent;

    private BigDecimal length = null;
    private int state = 0;

    public GData2(int colourNumber, float r, float g, float b, float a, BigDecimal x1, BigDecimal y1, BigDecimal z1, BigDecimal x2, BigDecimal y2, BigDecimal z2, GData1 parent, DatFile datFile, boolean isLine) {
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
        this.x1 = X1.floatValue() * 1000f;
        this.y1 = Y1.floatValue() * 1000f;
        this.z1 = Z1.floatValue() * 1000f;
        this.x2 = X2.floatValue() * 1000f;
        this.y2 = Y2.floatValue() * 1000f;
        this.z2 = Z2.floatValue() * 1000f;
        this.parent = parent;
        this.isLine = isLine;
        datFile.getVertexManager().add(this);
    }

    public GData2(GData1 parent, int colourNumber, float r, float g, float b, float a, BigDecimal x1, BigDecimal y1, BigDecimal z1, BigDecimal x2, BigDecimal y2, BigDecimal z2, float x12, float y12,
            float z12, float x22, float y22, float z22, DatFile datFile, boolean isLine) {
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
        this.x1 = x12;
        this.y1 = y12;
        this.z1 = z12;
        this.x2 = x22;
        this.y2 = y22;
        this.z2 = z22;
        this.lGeom = MathHelper.getLineVertices1000(new Vector4f(this.x1, this.y1, this.z1, 1f), new Vector4f(this.x2, this.y2, this.z2, 1f), parent.productMatrix);
        this.parent = parent;
        this.isLine = isLine;
        datFile.getVertexManager().add(this);
    }

    public GData2(final int colourNumber, float r, float g, float b, float a, Vertex v1, Vertex v2, GData1 parent, DatFile datFile, boolean isLine) {
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
        this.X1 = v1.X;
        this.Y1 = v1.Y;
        this.Z1 = v1.Z;
        this.X2 = v2.X;
        this.Y2 = v2.Y;
        this.Z2 = v2.Z;
        this.parent = parent;
        this.isLine = isLine;
        datFile.getVertexManager().add(this);
        this.lGeom = MathHelper.getLineVertices1000(new Vector4f(x1, y1, z1, 1f), new Vector4f(x2, y2, z2, 1f), parent.productMatrix);
    }

    /**
     * FOR Cut, Copy, Paste ONLY!
     *
     * @param v1
     * @param v2
     * @param parent
     * @param c
     * @param isLine
     */
    GData2(Vertex v1, Vertex v2, GData1 parent, GColour c, boolean isLine) {
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
        this.X1 = v1.X;
        this.Y1 = v1.Y;
        this.Z1 = v1.Z;
        this.X2 = v2.X;
        this.Y2 = v2.Y;
        this.Z2 = v2.Z;
        this.parent = parent;
        this.isLine = isLine;
        this.lGeom = null;
    }

    public GData2 unboundCopy(int index) {
        return new GData2(new Vertex(x1, y1, z1), new Vertex(x2, y2, z2), parent, new GColour(colourNumber, r, g, b, index), isLine);
    }

    /**
     * FOR TEXMAP ONLY!
     *
     * @param v1
     * @param v2
     * @param parent
     * @param c
     */
    public GData2(Vertex v1, Vertex v2, GColour c, GData1 parent) {
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
        this.parent = parent;
        this.isLine = true;
        this.lGeom = MathHelper.getLineVertices1000(new Vector4f(x1, y1, z1, 1f), new Vector4f(x2, y2, z2, 1f), parent.productMatrix);
        this.X1 = null;
        this.Y1 = null;
        this.Z1 = null;
        this.X2 = null;
        this.Y2 = null;
        this.Z2 = null;
    }

    @Override
    public void draw(Composite3D c3d) {
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
        switch (c3d.getLineMode()) {
        case 3:
        case 4:
            if (c3d.isLightOn() && (next == null || next.type() != 2 && next.type() != 5))
                GL11.glEnable(GL11.GL_LIGHTING);
            return;
        default:
            break;
        }

        if (GL11.glGetBoolean(GL11.GL_LIGHTING)) GL11.glDisable(GL11.GL_LIGHTING);

        if (!isLine) {
            drawDistance(c3d, X1, Y1, Z1, X2, Y2, Z2);
            if (c3d.isLightOn() && (next == null || next.type() != 2 && next.type() != 5))
                GL11.glEnable(GL11.GL_LIGHTING);
            return;
        }

        if (c3d.getZoom() > View.edge_threshold) {

            GL11.glPushMatrix();

            GL11.glColor4f(r, g, b, a);
            GL11.glScalef(lGeom[19][0], lGeom[19][1], lGeom[19][2]);

            float lx1 = x1 * lGeom[18][0];
            float ly1 = y1 * lGeom[18][1];
            float lz1 = z1 * lGeom[18][2];

            float lx2 = x2 * lGeom[18][0];
            float ly2 = y2 * lGeom[18][1];
            float lz2 = z2 * lGeom[18][2];

            if (GData.globalNegativeDeterminant) {

                GLPrimitives.SPHERE_INV.draw(lx1, ly1, lz1);

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

                GLPrimitives.SPHERE_INV.draw(lx2, ly2, lz2);

            } else {

                GLPrimitives.SPHERE.draw(lx1, ly1, lz1);

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

                GLPrimitives.SPHERE.draw(lx2, ly2, lz2);

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

        if (c3d.isLightOn() && (next == null || next.type() != 2 && next.type() != 5))
            GL11.glEnable(GL11.GL_LIGHTING);
    }

    @Override
    public void drawRandomColours(Composite3D c3d) {
        if (!visible) {
            return;
        }
        if (a < 1f && c3d.isDrawingSolidMaterials() || !c3d.isDrawingSolidMaterials() && a == 1f) {
            return;
        }
        switch (c3d.getLineMode()) {
        case 3:
        case 4:

            return;
        default:
            break;
        }

        if (GL11.glGetBoolean(GL11.GL_LIGHTING)) GL11.glDisable(GL11.GL_LIGHTING);

        if (!isLine) {
            drawDistance(c3d, X1, Y1, Z1, X2, Y2, Z2);
            if (c3d.isLightOn() && (next == null || next.type() != 2 && next.type() != 5))
                GL11.glEnable(GL11.GL_LIGHTING);
            return;
        }

        final float r = MathHelper.randomFloat(ID, 0);
        final float g = MathHelper.randomFloat(ID, 1);
        final float b = MathHelper.randomFloat(ID, 2);



        if (c3d.getZoom() > 5e-6) {

            GL11.glPushMatrix();

            GL11.glColor4f(r, g, b, a);
            GL11.glScalef(lGeom[19][0], lGeom[19][1], lGeom[19][2]);

            float lx1 = x1 * lGeom[18][0];
            float ly1 = y1 * lGeom[18][1];
            float lz1 = z1 * lGeom[18][2];

            float lx2 = x2 * lGeom[18][0];
            float ly2 = y2 * lGeom[18][1];
            float lz2 = z2 * lGeom[18][2];

            if (GData.globalNegativeDeterminant) {

                GLPrimitives.SPHERE_INV.draw(lx1, ly1, lz1);

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

                GLPrimitives.SPHERE_INV.draw(lx2, ly2, lz2);

            } else {

                GLPrimitives.SPHERE.draw(lx1, ly1, lz1);

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

                GLPrimitives.SPHERE.draw(lx2, ly2, lz2);

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

        if (c3d.isLightOn() && (next == null || next.type() != 2 && next.type() != 5))
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
    public void drawWhileAddCondlines(Composite3D c3d) {
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
            switch (c3d.getLineMode()) {
            case 3:
            case 4:
                return;
            default:
                break;
            }
            if (!isLine)
                return;

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
            GraphicalDataTools.setVertex(x1, y1, z1, this, true);
            GraphicalDataTools.setVertex(x2, y2, z2, this, true);
            GL11.glEnd();
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
        return 2;
    }

    @Override
    String getNiceString() {
        if (text != null)
            return text;
        StringBuilder lineBuilder = new StringBuilder();
        if (isLine) {
            lineBuilder.append(2);
            lineBuilder.append(" "); //$NON-NLS-1$
        } else {
            lineBuilder.append("0 !LPE DISTANCE "); //$NON-NLS-1$
        }
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
        if (X1 == null) {
            v1 = matrix.transform(new BigDecimal(x1 / 1000f), new BigDecimal(y1 / 1000f), new BigDecimal(z1 / 1000f));
            v2 = matrix.transform(new BigDecimal(x2 / 1000f), new BigDecimal(y2 / 1000f), new BigDecimal(z2 / 1000f));
        } else {
            v1 = matrix.transform(X1, Y1, Z1);
            v2 = matrix.transform(X2, Y2, Z2);
        }
        StringBuilder lineBuilder = new StringBuilder();
        if (isLine) {
            lineBuilder.append(2);
            lineBuilder.append(" "); //$NON-NLS-1$
        } else {
            lineBuilder.append("0 !LPE DISTANCE "); //$NON-NLS-1$
        }
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
    public void getVertexNormalMap(GDataState state, ThreadsafeTreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {}

    @Override
    public void getVertexNormalMapNOCERTIFY(GDataState state, ThreadsafeTreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {}

    @Override
    public void getVertexNormalMapNOCLIP(GDataState state, ThreadsafeTreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {}

    public String colourReplace(String col) {
        StringBuilder lineBuilder = new StringBuilder();
        if (isLine) {
            lineBuilder.append(2);
            lineBuilder.append(" "); //$NON-NLS-1$
        } else {
            lineBuilder.append("0 !LPE DISTANCE "); //$NON-NLS-1$
        }
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
        return lineBuilder.toString();
    }

    public void drawDistance(Composite3D c3d, BigDecimal x1c, BigDecimal y1c, BigDecimal z1c, BigDecimal x2c, BigDecimal y2c, BigDecimal z2c) {
        final java.text.DecimalFormat NUMBER_FORMAT4F = new java.text.DecimalFormat(View.NUMBER_FORMAT4F, new DecimalFormatSymbols(MyLanguage.LOCALE));
        final OpenGLRenderer renderer = c3d.getRenderer();
        final float zoom = 1f / c3d.getZoom();
        GL11.glLineWidth(View.lineWidthGL[0]);
        GL11.glColor4f(r, g, b, 1f);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3f(x1, y1, z1);
        GL11.glVertex3f(x2, y2, z2);
        GL11.glEnd();
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glMultMatrix(renderer.getRotationInverse());
        PGData3.beginDrawText();
        GL11.glColor4f(r, g, b, 1f);
        final Vector4f textOrigin = new Vector4f((x1 + x2) / 2f, (y1 + y2) / 2f, (z1 + z2) / 2f, 1f);
        Matrix4f.transform(c3d.getRotation(), textOrigin, textOrigin);
        BigDecimal dx = x2c.subtract(x1c);
        BigDecimal dy = y2c.subtract(y1c);
        BigDecimal dz = z2c.subtract(z1c);
        if (Editor3DWindow.getWindow().getTransformationMode() == ManipulatorScope.GLOBAL) {
            if (state != -1) {
                length = new Vector3d(dx, dy, dz).length();
                state = -1;
            }
        } else {
            Vector3d tr = c3d.getManipulator().getAccurateRotation().transform(new Vector3d(dx, dy, dz));
            dx = tr.X;
            dy = tr.Y;
            dz = tr.Z;
            if (state < 1) {
                length = new Vector3d(dx, dy, dz).length();
                state = 11;
            }
            state--;
        }
        BigDecimal dA = length;

        String dx_s = NUMBER_FORMAT4F.format(dx);
        String dy_s = NUMBER_FORMAT4F.format(dy);
        String dz_s = NUMBER_FORMAT4F.format(dz);
        String dA_s = NUMBER_FORMAT4F.format(dA);
        final float oy1 = .015f * zoom;
        final float oy2 = .03f * zoom;
        final float oy3 = .045f * zoom;
        final float ox1 = -.045f * zoom;
        for (PGData3 tri : View.DA) {
            tri.drawText(textOrigin.x, textOrigin.y, textOrigin.z + 100000f, zoom);
        }
        for (PGData3 tri : View.DX) {
            tri.drawText(textOrigin.x, textOrigin.y + oy1, textOrigin.z + 100000f, zoom);
        }
        for (PGData3 tri : View.DY) {
            tri.drawText(textOrigin.x, textOrigin.y + oy2, textOrigin.z + 100000f, zoom);
        }
        for (PGData3 tri : View.DZ) {
            tri.drawText(textOrigin.x, textOrigin.y + oy3, textOrigin.z + 100000f, zoom);
        }
        drawNumber(dA_s, textOrigin.x + ox1, textOrigin.y, textOrigin.z, zoom);
        drawNumber(dx_s, textOrigin.x + ox1, textOrigin.y + oy1, textOrigin.z, zoom);
        drawNumber(dy_s, textOrigin.x + ox1, textOrigin.y + oy2, textOrigin.z, zoom);
        drawNumber(dz_s, textOrigin.x + ox1, textOrigin.y + oy3, textOrigin.z, zoom);
        PGData3.endDrawText();
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();
    }

    private void drawNumber(String number, float ox, float oy, float oz, float zoom) {
        final int length =  number.length();
        float ox2 = 0f;
        for (int i = 0; i < length; i++) {
            Set<PGData3> tris = new HashSet<PGData3>();
            final char c = number.charAt(i);
            switch (c) {
            case '0':
                tris = View.D0;
                break;
            case '1':
                tris = View.D1;
                break;
            case '2':
                tris = View.D2;
                break;
            case '3':
                tris = View.D3;
                break;
            case '4':
                tris = View.D4;
                break;
            case '5':
                tris = View.D5;
                break;
            case '6':
                tris = View.D6;
                break;
            case '7':
                tris = View.D7;
                break;
            case '8':
                tris = View.D8;
                break;
            case '9':
                tris = View.D9;
                break;
            case '.':
                tris = View.Dd;
                break;
            case ',':
                tris = View.Dc;
                break;
            case '-':
                tris = View.DM;
                break;
            default:
                break;
            }
            for (PGData3 tri : tris) {
                tri.drawText(ox + ox2, oy, oz + 100000f, zoom);
            }
            ox2 = ox2 - .01f * zoom;
        }
    }

    /*
     * LOW QUALITY LINES: GL11.glDisable(GL11.GL_LIGHTING); GL11.glColor4f(r, g,
     * b, a); GL11.glBegin(GL11.GL_LINES); GL11.glVertex3f(x1, y1, z1);
     * GL11.glVertex3f(x2, y2, z2); GL11.glEnd();
     * GL11.glEnable(GL11.GL_LIGHTING);
     */
}
