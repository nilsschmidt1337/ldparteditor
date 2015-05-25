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
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.TreeMap;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;

/**
 * @author nils
 *
 */
public final class GDataPNG extends GData {

    private final Vector4f direction;
    private final GTexture texture;
    private final FloatBuffer matrix;
    public final Vertex offset;
    public final Vertex scale;
    public final BigDecimal angleA;
    public final BigDecimal angleB;
    public final BigDecimal angleC;
    public final String texturePath;

    private boolean goingToBeReplaced = false;

    public GDataPNG(String text, Vertex offset, BigDecimal angleA, BigDecimal angleB, BigDecimal angleC, Vertex scale, String texturePath) {
        this.text = text;
        this.texturePath = texturePath;
        this.texture = new GTexture(TexType.PLANAR, texturePath, null, 0, new Vector3f(), new Vector3f(), new Vector3f(), 0, 0);
        this.offset = offset;
        this.scale = scale;
        this.angleA = angleA;
        this.angleB = angleB;
        this.angleC = angleC;

        Matrix4f tMatrix = new Matrix4f();
        tMatrix.setIdentity();

        tMatrix = tMatrix.scale(new Vector3f(scale.x, scale.y, scale.z));

        Matrix4f dMatrix = new Matrix4f();
        dMatrix.setIdentity();

        Vector4f direction = new Vector4f(0f, 0f, -1f, 1f);
        // Matrix4f.rotate((float) (angleC.doubleValue() / 180.0 * Math.PI), new Vector3f(0f, 0f, 1f), dMatrix, dMatrix);
        Matrix4f.rotate((float) (angleB.doubleValue() / 180.0 * Math.PI), new Vector3f(1f, 0f, 0f), dMatrix, dMatrix);
        Matrix4f.rotate((float) (angleA.doubleValue() / 180.0 * Math.PI), new Vector3f(0f, 1f, 0f), dMatrix, dMatrix);

        Matrix4f.transform(dMatrix, direction, direction);
        direction.w = 0f;
        direction.normalise();
        direction.w = 1f;
        this.direction = direction;

        dMatrix.setIdentity();

        Matrix4f.rotate((float) (angleC.doubleValue() / 180.0 * Math.PI), new Vector3f(0f, 0f, 1f), dMatrix, dMatrix);
        Matrix4f.rotate((float) (angleB.doubleValue() / 180.0 * Math.PI), new Vector3f(1f, 0f, 0f), dMatrix, dMatrix);
        Matrix4f.rotate((float) (angleA.doubleValue() / 180.0 * Math.PI), new Vector3f(0f, 1f, 0f), dMatrix, dMatrix);

        Matrix4f.mul(dMatrix, tMatrix, tMatrix);

        Vector4f vx = Matrix4f.transform(dMatrix, new Vector4f(offset.x, 0f, 0f, 1f), null);
        Vector4f vy = Matrix4f.transform(dMatrix, new Vector4f(0f, offset.y, 0f, 1f), null);
        Vector4f vz = Matrix4f.transform(dMatrix, new Vector4f(0f, 0f, offset.z, 1f), null);
        tMatrix.m30 = vx.x;
        tMatrix.m31 = vx.y;
        tMatrix.m32 = vx.z;
        tMatrix.m30 += vy.x;
        tMatrix.m31 += vy.y;
        tMatrix.m32 += vy.z;
        tMatrix.m30 += vz.x;
        tMatrix.m31 += vz.y;
        tMatrix.m32 += vz.z;

        matrix = BufferUtils.createFloatBuffer(16);
        tMatrix.store(matrix);

        matrix.position(0);
    }

    @Override
    public void draw(Composite3D c3d) {

        final boolean selected = this.equals(c3d.getLockableDatFileReference().getVertexManager().getSelectedBgPicture());

        Vector4f[] gen = c3d.getGenerator();
        if (!selected && (Math.abs(direction.x - gen[2].x) > .001 || Math.abs(direction.y - gen[2].y) > .001 || Math.abs(direction.z - gen[2].z) > .001)) return;

        GL11.glPushMatrix();
        GL11.glMultMatrix(matrix);

        final OpenGLRenderer ren = c3d.getRenderer();
        texture.bind(c3d.isDrawingSolidMaterials(), GData.globalNegativeDeterminant ^ GData.globalInvertNext, true, ren, 0);

        float w;
        if (this.texture.getHeight() != 0f) {
            w = this.texture.getWidth() / this.texture.getHeight();
        } else {
            w = 1f;
        }

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);

        if (selected) {

            GL11.glColor4f(1f, 0f, 0f, 1f);
            GL11.glBegin(GL11.GL_QUADS);

            GL11.glVertex3f(w + 0.05f, 1f + 0.05f, 0f);
            GL11.glVertex3f(w, 1f, 0f);
            GL11.glVertex3f(w, -1f, 0f);
            GL11.glVertex3f(w + 0.05f, -1f - 0.05f, 0f);

            GL11.glVertex3f(-w, -1f, 0f);
            GL11.glVertex3f(-w, 1f, 0f);
            GL11.glVertex3f(-w - 0.05f, 1f + 0.05f, 0f);
            GL11.glVertex3f(-w - 0.05f, -1f - 0.05f, 0f);

            GL11.glVertex3f(w + 0.05f, 1f + 0.05f, 0f);
            GL11.glVertex3f(-w - 0.05f, 1f + 0.05f, 0f);
            GL11.glVertex3f(-w, 1f, 0f);
            GL11.glVertex3f(w, 1f, 0f);

            GL11.glVertex3f(-w, -1f, 0f);
            GL11.glVertex3f(-w - 0.05f, -1f - 0.05f, 0f);
            GL11.glVertex3f(w + 0.05f, -1f - 0.05f, 0f);
            GL11.glVertex3f(w, -1f, 0f);
            GL11.glEnd();
            GL11.glEnable(GL11.GL_CULL_FACE);

            GL11.glColor4f(1f, 0.7f, 0.7f, 1f);
            if (scale.x * scale.y < 0f) {
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glBegin(GL11.GL_QUADS);
                // 1 1
                GL11.glTexCoord2f(0f, 0f);
                GL11.glVertex3f(-w, -1f, 0f);
                // 1 0
                GL11.glTexCoord2f(0f, 1f);
                GL11.glVertex3f(-w, 1f, 0f);
                // 0 0
                GL11.glTexCoord2f(1f, 1f);
                GL11.glVertex3f(w, 1f, 0f);
                // 0 1
                GL11.glTexCoord2f(1f, 0f);
                GL11.glVertex3f(w, -1f, 0f);
                GL11.glEnd();
            } else {
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glBegin(GL11.GL_QUADS);
                GL11.glTexCoord2f(1f, 1f);
                GL11.glVertex3f(w, 1f, 0f);
                GL11.glTexCoord2f(0f, 1f);
                GL11.glVertex3f(-w, 1f, 0f);
                GL11.glTexCoord2f(0f, 0f);
                GL11.glVertex3f(-w, -1f, 0f);
                GL11.glTexCoord2f(1f, 0f);
                GL11.glVertex3f(w, -1f, 0f);
                GL11.glEnd();
            }

        } else {
            GL11.glColor4f(1f, 1f, 1f, 1f);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(1f, 1f);
            GL11.glVertex3f(w, 1f, 0f);
            GL11.glTexCoord2f(0f, 1f);
            GL11.glVertex3f(-w, 1f, 0f);
            GL11.glTexCoord2f(0f, 0f);
            GL11.glVertex3f(-w, -1f, 0f);
            GL11.glTexCoord2f(1f, 0f);
            GL11.glVertex3f(w, -1f, 0f);
            GL11.glEnd();
        }


        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_CULL_FACE);
        if (c3d.isLightOn()) GL11.glEnable(GL11.GL_LIGHTING);


        GL11.glPopMatrix();
    }

    @Override
    public void drawRandomColours(Composite3D c3d) {
        draw(c3d);
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
    public void drawBFC_Textured(Composite3D c3d) {}

    @Override
    public int type() {
        return 10;
    }

    @Override
    String getNiceString() {
        return text;
    }

    @Override
    public String inlinedString(byte bfc, GColour colour) {
        return text;
    }

    @Override
    public String transformAndColourReplace(String colour, Matrix matrix) {
        return text;
    }

    @Override
    public void getBFCorientationMap(HashMap<GData, Byte> map) {}
    @Override
    public void getBFCorientationMapNOCERTIFY(HashMap<GData, Byte> map) {}
    @Override
    public void getBFCorientationMapNOCLIP(HashMap<GData, Byte> map) {}
    @Override
    public void getVertexNormalMap(TreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, HashMap<GData, float[]> dataLinkedToNormalCACHE, VertexManager vm) {}
    @Override
    public void getVertexNormalMapNOCERTIFY(TreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, HashMap<GData, float[]> dataLinkedToNormalCACHE, VertexManager vm) {}
    @Override
    public void getVertexNormalMapNOCLIP(TreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, HashMap<GData, float[]> dataLinkedToNormalCACHE, VertexManager vm) {}

    public String getString(Vertex offset, BigDecimal angleA, BigDecimal angleB, BigDecimal angleC, Vertex scale, String texturePath) {
        StringBuilder lineBuilder = new StringBuilder();
        lineBuilder.append("0 !LPE PNG "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(offset.X));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(offset.Y));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(offset.Z));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(angleA));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(angleB));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(angleC));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(scale.X));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(scale.Y));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(texturePath);
        return lineBuilder.toString();
    }

    public boolean isGoingToBeReplaced() {
        return goingToBeReplaced;
    }

    public void setGoingToBeReplaced(boolean goingToBeReplaced) {
        this.goingToBeReplaced = goingToBeReplaced;
    }
}