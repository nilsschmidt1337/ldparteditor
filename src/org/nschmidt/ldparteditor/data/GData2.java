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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.composite.Composite3D;
import org.nschmidt.ldparteditor.enumtype.Colour;
import org.nschmidt.ldparteditor.enumtype.GL20Primitives;
import org.nschmidt.ldparteditor.enumtype.LDConfig;
import org.nschmidt.ldparteditor.enumtype.ManipulatorScope;
import org.nschmidt.ldparteditor.enumtype.MyLanguage;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.helper.math.MathHelper;
import org.nschmidt.ldparteditor.helper.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.helper.math.ThreadsafeSortedMap;
import org.nschmidt.ldparteditor.helper.math.Vector3d;
import org.nschmidt.ldparteditor.opengl.GL33Helper;
import org.nschmidt.ldparteditor.opengl.GLShader;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer20;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.ManipulatorScopeToolItem;

/**
 * @author nils
 *
 */
public final class GData2 extends GData {

    public final int colourNumber;
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

    final BigDecimal x1p;
    final BigDecimal y1p;
    final BigDecimal z1p;
    final BigDecimal x2p;
    final BigDecimal y2p;
    final BigDecimal z2p;

    private final float[][] lGeom;

    private BigDecimal length = null;
    private int state = 0;

    public GData2(int colourNumber, float r, float g, float b, float a, BigDecimal x1, BigDecimal y1, BigDecimal z1, BigDecimal x2, BigDecimal y2, BigDecimal z2, GData1 parent, DatFile datFile, boolean isLine) {
        super(parent);
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
        this.x1 = x1p.floatValue() * 1000f;
        this.y1 = y1p.floatValue() * 1000f;
        this.z1 = z1p.floatValue() * 1000f;
        this.x2 = x2p.floatValue() * 1000f;
        this.y2 = y2p.floatValue() * 1000f;
        this.z2 = z2p.floatValue() * 1000f;
        this.isLine = isLine;
        datFile.getVertexManager().add(this);
    }

    GData2(GData1 parent, int colourNumber, float r, float g, float b, float a, BigDecimal x1, BigDecimal y1, BigDecimal z1, BigDecimal x2, BigDecimal y2, BigDecimal z2, float x12, float y12,
            float z12, float x22, float y22, float z22, DatFile datFile, boolean isLine) {
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
        this.x1 = x12;
        this.y1 = y12;
        this.z1 = z12;
        this.x2 = x22;
        this.y2 = y22;
        this.z2 = z22;
        this.lGeom = MathHelper.getLineVertices1000(new Vector3f(this.x1, this.y1, this.z1), new Vector3f(this.x2, this.y2, this.z2), parent.productMatrix);
        this.isLine = isLine;
        datFile.getVertexManager().add(this);
    }

    GData2(final int colourNumber, float r, float g, float b, float a, Vertex v1, Vertex v2, GData1 parent, DatFile datFile, boolean isLine) {
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
        this.x1p = v1.xp;
        this.y1p = v1.yp;
        this.z1p = v1.zp;
        this.x2p = v2.xp;
        this.y2p = v2.yp;
        this.z2p = v2.zp;
        this.isLine = isLine;
        datFile.getVertexManager().add(this);
        this.lGeom = MathHelper.getLineVertices1000(new Vector3f(x1, y1, z1), new Vector3f(x2, y2, z2), parent.productMatrix);
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
        this.x1p = v1.xp;
        this.y1p = v1.yp;
        this.z1p = v1.zp;
        this.x2p = v2.xp;
        this.y2p = v2.yp;
        this.z2p = v2.zp;
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
        this.isLine = true;
        this.lGeom = MathHelper.getLineVertices1000(new Vector3f(x1, y1, z1), new Vector3f(x2, y2, z2), parent.productMatrix);
        this.x1p = null;
        this.y1p = null;
        this.z1p = null;
        this.x2p = null;
        this.y2p = null;
        this.z2p = null;
    }

    public BigDecimal getLength() {
        BigDecimal dx = x1p.subtract(x2p);
        BigDecimal dy = y1p.subtract(y2p);
        BigDecimal dz = z1p.subtract(z2p);
        return new Vector3d(dx, dy, dz).length();
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
        switch (c3d.getLineMode()) {
        case 3, 4:
            if (c3d.isLightOn() && (next == null || next.type() != 2 && next.type() != 5))
                GL11.glEnable(GL11.GL_LIGHTING);
            return;
        default:
            break;
        }

        if (GL11.glGetBoolean(GL11.GL_LIGHTING) == 1) GL11.glDisable(GL11.GL_LIGHTING);

        if (!isLine) {
            drawDistanceGL20(c3d, x1p, y1p, z1p, x2p, y2p, z2p);
            if (c3d.isLightOn() && (next == null || next.type() != 2 && next.type() != 5))
                GL11.glEnable(GL11.GL_LIGHTING);
            return;
        }

        if (c3d.getZoom() > View.edgeThreshold) {

            GL11.glPushMatrix();

            GL11.glColor4f(r, g, b, a);
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
            GL11.glLineWidth(View.lineWidthGL);
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
    public void drawGL20RandomColours(Composite3D c3d) {
        if (!visible) {
            return;
        }
        if (a < 1f && c3d.isDrawingSolidMaterials() || !c3d.isDrawingSolidMaterials() && a == 1f) {
            return;
        }
        switch (c3d.getLineMode()) {
        case 3, 4:
            return;
        default:
            break;
        }

        if (GL11.glGetBoolean(GL11.GL_LIGHTING) == 1) GL11.glDisable(GL11.GL_LIGHTING);

        if (!isLine) {
            drawDistanceGL20(c3d, x1p, y1p, z1p, x2p, y2p, z2p);
            if (c3d.isLightOn() && (next == null || next.type() != 2 && next.type() != 5))
                GL11.glEnable(GL11.GL_LIGHTING);
            return;
        }

        final float rndRed = MathHelper.randomFloat(id, 0);
        final float rndGreen = MathHelper.randomFloat(id, 1);
        final float rndBlue = MathHelper.randomFloat(id, 2);

        if (c3d.getZoom() > View.edgeThreshold) {

            GL11.glPushMatrix();

            GL11.glColor4f(rndRed, rndGreen, rndBlue, a);
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
            GL11.glLineWidth(View.lineWidthGL);
            GL11.glColor4f(rndRed, rndGreen, rndBlue, a);
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
    public void drawGL20WhileAddCondlines(Composite3D c3d) {
        drawGL20(c3d);
    }

    @Override
    public void drawGL20CoplanarityHeatmap(Composite3D c3d) {
        drawGL20(c3d);
    }

    @Override
    public void drawGL20Wireframe(Composite3D c3d) {
        // Implementation is not required.
    }

    @Override
    public void drawGL20BFCtextured(Composite3D c3d) {
        // done :)
        if (GData.globalDrawObjects) {
            final OpenGLRenderer20 renderer = (OpenGLRenderer20) c3d.getRenderer();
            GL20.glUniform1f(renderer.getNormalSwitchLoc(), GData.globalNegativeDeterminant ^ GData.globalInvertNext ? 1f : 0f);
            GL20.glUniform1f(renderer.getNoTextureSwitch(), 1f);
            GL20.glUniform1f(renderer.getNoLightSwitch(), 1f);
            GL20.glUniform1f(renderer.getCubeMapSwitch(), 0f);
            if (!visible)
                return;
            if (!c3d.isDrawingSolidMaterials())
                return;
            switch (c3d.getLineMode()) {
            case 3, 4:
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
            if (colourNumber == 24 && (cn = parent.r == .5f && parent.g == .5f && parent.b == .5f && (parent.a == 1.1f || parent.a == -1)  ? 16 : LDConfig.getIndex(parent.r,  parent.g,  parent.b)) != 16) {
                GColour c = LDConfig.getEdgeColour(cn, c3d);
                r2 = c.getR();
                g2 = c.getG();
                b2 = c.getB();
            } else {
                r2 = this.r;
                g2 = this.g;
                b2 = this.b;
            }

            GL11.glLineWidth(View.lineWidthGL);
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
            lineBuilder.append("2 "); //$NON-NLS-1$
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
        if (x1p == null) {
            v1 = matrix.transform(new BigDecimal(x1 / 1000f), new BigDecimal(y1 / 1000f), new BigDecimal(z1 / 1000f));
            v2 = matrix.transform(new BigDecimal(x2 / 1000f), new BigDecimal(y2 / 1000f), new BigDecimal(z2 / 1000f));
        } else {
            v1 = matrix.transform(x1p, y1p, z1p);
            v2 = matrix.transform(x2p, y2p, z2p);
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
        // Implementation is not required.
    }

    @Override
    public void getVertexNormalMapNOCERTIFY(GDataState state, ThreadsafeSortedMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {
        // Implementation is not required.
    }

    @Override
    public void getVertexNormalMapNOCLIP(GDataState state, ThreadsafeSortedMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {
        // Implementation is not required.
    }

    String colourReplace(String col) {
        StringBuilder lineBuilder = new StringBuilder();
        if (isLine) {
            lineBuilder.append(2);
            lineBuilder.append(" "); //$NON-NLS-1$
        } else {
            lineBuilder.append("0 !LPE DISTANCE "); //$NON-NLS-1$
        }
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
        return lineBuilder.toString();
    }

    void drawDistanceGL20(Composite3D c3d, BigDecimal x1c, BigDecimal y1c, BigDecimal z1c, BigDecimal x2c, BigDecimal y2c, BigDecimal z2c) {
        final java.text.DecimalFormat numberFormat4f = new java.text.DecimalFormat(View.NUMBER_FORMAT4F, new DecimalFormatSymbols(MyLanguage.getLocale()));
        final OpenGLRenderer20 renderer = (OpenGLRenderer20) c3d.getRenderer();
        final float zoom = 1f / c3d.getZoom();
        GL11.glLineWidth(View.lineWidthGL);
        GL11.glColor4f(r, g, b, 1f);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3f(x1, y1, z1);
        GL11.glVertex3f(x2, y2, z2);
        GL11.glEnd();
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glMultMatrixf(renderer.getRotationInverse());
        PGData3.beginDrawText();
        GL11.glColor4f(r, g, b, 1f);
        final Vector4f textOrigin = new Vector4f((x1 + x2) / 2f, (y1 + y2) / 2f, (z1 + z2) / 2f, 1f);
        final Vector4f lineOrigin = new Vector4f(x1, y1, z1, 1f);
        Matrix4f.transform(c3d.getRotation(), textOrigin, textOrigin);
        Matrix4f.transform(c3d.getRotation(), lineOrigin, lineOrigin);
        BigDecimal dx = x2c.subtract(x1c);
        BigDecimal dy = y2c.subtract(y1c);
        BigDecimal dz = z2c.subtract(z1c);
        if (ManipulatorScopeToolItem.getTransformationScope() == ManipulatorScope.GLOBAL) {
            if (state != -1) {
                length = new Vector3d(dx, dy, dz).length();
                state = -1;
            }
        } else {
            Vector3d tr = c3d.getManipulator().getAccurateRotation().transform(new Vector3d(dx, dy, dz));
            dx = tr.x;
            dy = tr.y;
            dz = tr.z;
            if (state < 1) {
                length = new Vector3d(dx, dy, dz).length();
                state = 11;
            }
            state--;
        }
        BigDecimal dA = length;

        String dxS = numberFormat4f.format(dx);
        String dyS = numberFormat4f.format(dy);
        String dzS = numberFormat4f.format(dz);
        String dAS = numberFormat4f.format(dA);
        final float oy1 = .015f * zoom;
        final float oy2 = .03f * zoom;
        final float oy3 = .045f * zoom;
        final float ox1 = -.045f * zoom;
        for (PGData3 tri : View.S) {
            tri.drawText(lineOrigin.x, lineOrigin.y, lineOrigin.z + 100000f, zoom);
        }
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
        drawNumber(dAS, textOrigin.x + ox1, textOrigin.y, textOrigin.z, zoom);
        drawNumber(dxS, textOrigin.x + ox1, textOrigin.y + oy1, textOrigin.z, zoom);
        drawNumber(dyS, textOrigin.x + ox1, textOrigin.y + oy2, textOrigin.z, zoom);
        drawNumber(dzS, textOrigin.x + ox1, textOrigin.y + oy3, textOrigin.z, zoom);
        PGData3.endDrawText();
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();
    }

    private void drawNumber(String number, float ox, float oy, float oz, float zoom) {
        final int charCount =  number.length();
        float ox2 = 0f;
        for (int i = 0; i < charCount; i++) {
            Set<PGData3> tris = new HashSet<>();
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
                tris = View.DDot;
                break;
            case ',':
                tris = View.DComma;
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

    private void drawNumberGL33(String number, float ox, float oy, float oz, float zoom) {
        final int charCount =  number.length();
        float ox2 = 0f;
        for (int i = 0; i < charCount; i++) {
            Set<PGData3> tris = new HashSet<>();
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
                tris = View.DDot;
                break;
            case ',':
                tris = View.DComma;
                break;
            case '-':
                tris = View.DM;
                break;
            default:
                break;
            }
            for (PGData3 tri : tris) {
                tri.drawTextGL33vao(ox + ox2, oy, oz + 100000f, zoom);
            }
            ox2 = ox2 - .01f * zoom;
        }
    }

    int insertDistanceMeter(Vertex[] v, float[] lineData, int lineIndex) {
        GL33Helper.pointAt7(0, x1, y1, z1, lineData, lineIndex);
        GL33Helper.pointAt7(1, x2, y2, z2, lineData, lineIndex);
        GL33Helper.colourise7(0, 2, r, g, b, 7f, lineData, lineIndex);

        lineIndex += 2;

        GL33Helper.pointAt7(0, v[0].x, v[0].y, v[0].z, lineData, lineIndex);
        GL33Helper.pointAt7(1, v[1].x, v[1].y, v[1].z, lineData, lineIndex);
        GL33Helper.colourise7(0, 2, Colour.vertexSelectedColourR, Colour.vertexSelectedColourG, Colour.vertexSelectedColourB, 7f, lineData, lineIndex);

        return 4;
    }

    void drawDistanceGL33(Composite3D c3d, GLShader shader, BigDecimal x1c, BigDecimal y1c, BigDecimal z1c, BigDecimal x2c, BigDecimal y2c, BigDecimal z2c, boolean forceLengthCalculation) {
        GL20.glUniform3f(shader.getUniformLocation("color"), r, g, b); //$NON-NLS-1$

        final java.text.DecimalFormat numberFormat4f = new java.text.DecimalFormat(View.NUMBER_FORMAT4F, new DecimalFormatSymbols(MyLanguage.getLocale()));
        final float zoom = 1f / c3d.getZoom();
        final Vector4f textOrigin = new Vector4f((x1 + x2) / 2f, (y1 + y2) / 2f, (z1 + z2) / 2f, 1f);
        final Vector4f lineOrigin = new Vector4f(x1, y1, z1, 1f);
        Matrix4f.transform(c3d.getRotation(), textOrigin, textOrigin);
        Matrix4f.transform(c3d.getRotation(), lineOrigin, lineOrigin);
        BigDecimal dx = x2c.subtract(x1c);
        BigDecimal dy = y2c.subtract(y1c);
        BigDecimal dz = z2c.subtract(z1c);
        if (ManipulatorScopeToolItem.getTransformationScope() == ManipulatorScope.GLOBAL) {
            if (state != -1 || forceLengthCalculation) {
                length = new Vector3d(dx, dy, dz).length();
                state = -1;
            }
        } else {
            Vector3d tr = c3d.getManipulator().getAccurateRotation().transform(new Vector3d(dx, dy, dz));
            dx = tr.x;
            dy = tr.y;
            dz = tr.z;
            if (state < 1 || forceLengthCalculation) {
                length = new Vector3d(dx, dy, dz).length();
                state = 11;
            }
            state--;
        }
        BigDecimal dA = length;

        String dxS = numberFormat4f.format(dx);
        String dyS = numberFormat4f.format(dy);
        String dzS = numberFormat4f.format(dz);
        String dAS = numberFormat4f.format(dA);
        final float oy1 = .015f * zoom;
        final float oy2 = .03f * zoom;
        final float oy3 = .045f * zoom;
        final float ox1 = -.045f * zoom;
        for (PGData3 tri : View.S) {
            tri.drawTextGL33vao(lineOrigin.x, lineOrigin.y, lineOrigin.z + 100000f, zoom);
        }
        for (PGData3 tri : View.DA) {
            tri.drawTextGL33vao(textOrigin.x, textOrigin.y, textOrigin.z + 100000f, zoom);
        }
        for (PGData3 tri : View.DX) {
            tri.drawTextGL33vao(textOrigin.x, textOrigin.y + oy1, textOrigin.z + 100000f, zoom);
        }
        for (PGData3 tri : View.DY) {
            tri.drawTextGL33vao(textOrigin.x, textOrigin.y + oy2, textOrigin.z + 100000f, zoom);
        }
        for (PGData3 tri : View.DZ) {
            tri.drawTextGL33vao(textOrigin.x, textOrigin.y + oy3, textOrigin.z + 100000f, zoom);
        }
        drawNumberGL33(dAS, textOrigin.x + ox1, textOrigin.y, textOrigin.z, zoom);
        drawNumberGL33(dxS, textOrigin.x + ox1, textOrigin.y + oy1, textOrigin.z, zoom);
        drawNumberGL33(dyS, textOrigin.x + ox1, textOrigin.y + oy2, textOrigin.z, zoom);
        drawNumberGL33(dzS, textOrigin.x + ox1, textOrigin.y + oy3, textOrigin.z, zoom);
    }
}
