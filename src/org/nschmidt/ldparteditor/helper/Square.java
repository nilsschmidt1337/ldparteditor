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
package org.nschmidt.ldparteditor.helper;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;
import org.nschmidt.ldparteditor.opengl.GL33Helper;
import org.nschmidt.ldparteditor.opengl.GLMatrixStack;

public class Square {

    private final float r;
    private final float g;
    private final float b;

    private final float[] squareX = new float[4];
    private final float[] squareY = new float[4];
    private final float[] squareZ = new float[4];

    public Square(float r, float g, float b, float aX, float aY, float aZ, float bX, float bY, float bZ, float edgeLength) {
        aX = aX / 1000f;
        aY = aY / 1000f;
        aZ = aZ / 1000f;
        bX = bX / 1000f;
        bY = bY / 1000f;
        bZ = bZ / 1000f;
        this.r = r;
        this.g = g;
        this.b = b;
        Vector3f va = new Vector3f(aX, aY, aZ);
        va.normalise();
        va.scale(edgeLength);
        Vector3f vb = new Vector3f(bX, bY, bZ);
        vb.normalise();
        vb.scale(edgeLength);

        final float cX = aX + bX;
        final float cY = aY + bY;
        final float cZ = aZ + bZ;

        squareX[0] = cX + va.x;
        squareY[0] = cY + va.y;
        squareZ[0] = cZ + va.z;
        squareX[1] = cX + vb.x + va.x;
        squareY[1] = cY + vb.y + va.y;
        squareZ[1] = cZ + vb.z + va.z;
        squareX[2] = cX + vb.x;
        squareY[2] = cY + vb.y;
        squareZ[2] = cZ + vb.z;
        squareX[3] = cX;
        squareY[3] = cY;
        squareZ[3] = cZ;
    }

    public void draw(float x, float y, float z, float zoom) {
        final float zoom_inv = 1f / zoom;
        GL11.glPushMatrix();

        GL11.glTranslatef(x, y, z);
        GL11.glScalef(zoom_inv, zoom_inv, zoom_inv);

        GL11.glColor3f(r, g, b);

        GL11.glBegin(GL11.GL_QUADS);

        GL11.glVertex3f(squareX[3], squareY[3], squareZ[3]);
        GL11.glVertex3f(squareX[2], squareY[2], squareZ[2]);
        GL11.glVertex3f(squareX[1], squareY[1], squareZ[1]);
        GL11.glVertex3f(squareX[0], squareY[0], squareZ[0]);

        GL11.glVertex3f(squareX[0], squareY[0], squareZ[0]);
        GL11.glVertex3f(squareX[1], squareY[1], squareZ[1]);
        GL11.glVertex3f(squareX[2], squareY[2], squareZ[2]);
        GL11.glVertex3f(squareX[3], squareY[3], squareZ[3]);

        GL11.glEnd();

        GL11.glPopMatrix();
    }

    public void drawGL33rgb(GLMatrixStack stack, float x, float y, float z, float zoom) {
        final float zoom_inv = 1f / zoom;
        stack.glPushMatrix();

        stack.glTranslatef(x, y, z);
        stack.glScalef(zoom_inv, zoom_inv, zoom_inv);

        float[] vertexData = new float[]{
                squareX[3], squareY[3], squareZ[3], r, g, b,
                squareX[2], squareY[2], squareZ[2], r, g, b,
                squareX[1], squareY[1], squareZ[1], r, g, b,
                squareX[0], squareY[0], squareZ[0], r, g, b
        };

        int[] indices = new int[]{
                0, 1, 2,
                2, 3, 0,

                2, 1, 0,
                0, 3, 2
        };

        GL33Helper.drawTrianglesIndexedRGBgeneralSlow(vertexData, indices);

        stack.glPopMatrix();
    }
}
