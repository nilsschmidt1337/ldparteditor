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
package org.nschmidt.ldparteditor.helpers;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.nschmidt.ldparteditor.opengl.GL33Helper;
import org.nschmidt.ldparteditor.opengl.GLMatrixStack;

/**
 * @author nils
 *
 */
public class ArrowBlunt {

    private final FloatBuffer matrix;
    private final Matrix4f rotation;

    private static final float EPSILON = 0.0000001f;

    private final float r;
    private final float g;
    private final float b;

    private final float lineEnd;
    private final float lineWidth;

    private final float[] cubeX = new float[8];
    private final float[] cubeY = new float[8];
    private final float[] cubeZ = new float[8];

    public ArrowBlunt(float r, float g, float b, float dirX, float dirY, float dirZ, float edgeLength, float lineWidth) {
        dirX = dirX / 1000f;
        dirY = dirY / 1000f;
        dirZ = dirZ / 1000f;
        this.r = r;
        this.g = g;
        this.b = b;
        this.lineWidth = lineWidth;
        final float length = (float) Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        final float cube_start = length - edgeLength;
        lineEnd = length - edgeLength / 3f;
        rotation = makeRotationDir(new Vector3f(dirX, dirY, dirZ));
        matrix = BufferUtils.createFloatBuffer(16);
        rotation.store(matrix);
        matrix.position(0);

        final float half_edge_length = edgeLength / 2f;
        final float half_edge_length_neg = -half_edge_length;
        cubeX[0] = half_edge_length;
        cubeY[0] = cube_start;
        cubeZ[0] = half_edge_length;
        cubeX[1] = half_edge_length;
        cubeY[1] = cube_start;
        cubeZ[1] = half_edge_length_neg;
        cubeX[2] = half_edge_length_neg;
        cubeY[2] = cube_start;
        cubeZ[2] = half_edge_length_neg;
        cubeX[3] = half_edge_length_neg;
        cubeY[3] = cube_start;
        cubeZ[3] = half_edge_length;

        cubeX[4] = half_edge_length;
        cubeY[4] = length;
        cubeZ[4] = half_edge_length;
        cubeX[5] = half_edge_length;
        cubeY[5] = length;
        cubeZ[5] = half_edge_length_neg;
        cubeX[6] = half_edge_length_neg;
        cubeY[6] = length;
        cubeZ[6] = half_edge_length_neg;
        cubeX[7] = half_edge_length_neg;
        cubeY[7] = length;
        cubeZ[7] = half_edge_length;
    }

    private Matrix4f makeRotationDir(Vector3f direction) {
        final Vector3f direction2 = new Vector3f();
        Matrix4f arrowRotation = new Matrix4f();
        direction.normalise();

        // Calculate point from hesse normal plane

        int rank = 0;
        if (Math.abs(direction.x) < EPSILON)
            rank++;
        if (Math.abs(direction.y) < EPSILON)
            rank++;
        if (Math.abs(direction.z) < EPSILON)
            rank++;

        if (rank == 1) {
            if (Math.abs(direction.x) < EPSILON)
                direction2.set(1f, 0f, 0f);
            else if (Math.abs(direction.y) < EPSILON)
                direction2.set(0f, 1f, 0f);
            else if (Math.abs(direction.z) < EPSILON)
                direction2.set(0f, 0f, 1f);
        } else if (rank == 2) {
            if (Math.abs(direction.x) < EPSILON && Math.abs(direction.y) < EPSILON)
                direction2.set(1f, 0f, 0f);
            else if (Math.abs(direction.x) < EPSILON && Math.abs(direction.z) < EPSILON)
                direction2.set(1f, 0f, 0f);
            else if (Math.abs(direction.y) < EPSILON && Math.abs(direction.z) < EPSILON)
                direction2.set(0f, 1f, 0f);
        } else {
            direction2.setX(0f);
            direction2.setY(direction.y * 10f);
            direction2.setZ(-direction.y * 10f / direction.z);
            ;
        }

        final Vector3f xbase;
        final Vector3f ybase = new Vector3f(direction);
        final Vector3f zbase;

        xbase = Vector3f.cross(direction2, direction, null);
        zbase = Vector3f.cross(direction, xbase, null);

        xbase.normalise();
        zbase.normalise();

        arrowRotation.m00 = xbase.x;
        arrowRotation.m10 = ybase.x;
        arrowRotation.m20 = zbase.x;

        arrowRotation.m01 = xbase.y;
        arrowRotation.m11 = ybase.y;
        arrowRotation.m21 = zbase.y;

        arrowRotation.m02 = xbase.z;
        arrowRotation.m12 = ybase.z;
        arrowRotation.m22 = zbase.z;

        arrowRotation.m33 = 1f;

        return arrowRotation;
    }

    public void draw(float x, float y, float z, float zoom) {
        final float zoom_inv = 1f / zoom;
        GL11.glPushMatrix();

        GL11.glTranslatef(x, y, z);
        GL11.glMultMatrixf(matrix);
        GL11.glScalef(zoom_inv, zoom_inv, zoom_inv);

        GL11.glLineWidth(lineWidth);
        GL11.glColor3f(r, g, b);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3f(0f, 0f, 0f);
        GL11.glVertex3f(0f, lineEnd, 0f);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_QUADS);

        GL11.glVertex3f(cubeX[3], cubeY[3], cubeZ[3]);
        GL11.glVertex3f(cubeX[2], cubeY[2], cubeZ[2]);
        GL11.glVertex3f(cubeX[1], cubeY[1], cubeZ[1]);
        GL11.glVertex3f(cubeX[0], cubeY[0], cubeZ[0]);

        GL11.glVertex3f(cubeX[4], cubeY[4], cubeZ[4]);
        GL11.glVertex3f(cubeX[5], cubeY[5], cubeZ[5]);
        GL11.glVertex3f(cubeX[6], cubeY[6], cubeZ[6]);
        GL11.glVertex3f(cubeX[7], cubeY[7], cubeZ[7]);

        GL11.glEnd();

        GL11.glBegin(GL11.GL_QUAD_STRIP);
        GL11.glVertex3f(cubeX[0], cubeY[0], cubeZ[0]);
        GL11.glVertex3f(cubeX[4], cubeY[4], cubeZ[4]);
        GL11.glVertex3f(cubeX[3], cubeY[3], cubeZ[3]);
        GL11.glVertex3f(cubeX[7], cubeY[7], cubeZ[7]);
        GL11.glVertex3f(cubeX[2], cubeY[2], cubeZ[2]);
        GL11.glVertex3f(cubeX[6], cubeY[6], cubeZ[6]);
        GL11.glVertex3f(cubeX[1], cubeY[1], cubeZ[1]);
        GL11.glVertex3f(cubeX[5], cubeY[5], cubeZ[5]);
        GL11.glVertex3f(cubeX[0], cubeY[0], cubeZ[0]);
        GL11.glVertex3f(cubeX[4], cubeY[4], cubeZ[4]);
        GL11.glEnd();

        GL11.glPopMatrix();
    }

    public void drawGL33_RGB(GLMatrixStack stack, float x, float y, float z, float zoom) {
        final float zoom_inv = 1f / zoom;
        stack.glPushMatrix();

        stack.glTranslatef(x, y, z);
        stack.glMultMatrixf(rotation);
        stack.glScalef(zoom_inv, zoom_inv, zoom_inv);

        GL11.glLineWidth(lineWidth);

        {
            float[] vertexData = new float[]{
                    0f, 0f, 0f,
                    r, g, b,
                    0f, lineEnd, 0f,
                    r, g, b
            };
            GL33Helper.drawLinesRGB_GeneralSlow(vertexData);
        }

        float[] vertexData = new float[]{
                cubeX[3], cubeY[3], cubeZ[3], r, g, b,
                cubeX[2], cubeY[2], cubeZ[2], r, g, b,
                cubeX[1], cubeY[1], cubeZ[1], r, g, b,
                cubeX[0], cubeY[0], cubeZ[0], r, g, b,

                cubeX[4], cubeY[4], cubeZ[4], r, g, b,
                cubeX[5], cubeY[5], cubeZ[5], r, g, b,
                cubeX[6], cubeY[6], cubeZ[6], r, g, b,
                cubeX[7], cubeY[7], cubeZ[7], r, g, b,

                cubeX[0], cubeY[0], cubeZ[0], r, g, b,
                cubeX[4], cubeY[4], cubeZ[4], r, g, b,
                cubeX[3], cubeY[3], cubeZ[3], r, g, b,
                cubeX[7], cubeY[7], cubeZ[7], r, g, b,
                cubeX[2], cubeY[2], cubeZ[2], r, g, b,
                cubeX[6], cubeY[6], cubeZ[6], r, g, b,
                cubeX[1], cubeY[1], cubeZ[1], r, g, b,
                cubeX[5], cubeY[5], cubeZ[5], r, g, b,
                cubeX[0], cubeY[0], cubeZ[0], r, g, b,
                cubeX[4], cubeY[4], cubeZ[4], r, g, b
        };

        int[] indices = new int[]{
                0, 1, 2,
                2, 3, 0,

                4, 5, 6,
                6, 7, 4,

                8, 9, 10,
                10, 9, 11,

                10, 11, 12,
                12, 11, 13,

                12, 13, 14,
                14, 13, 15,

                14, 15, 16,
                16, 15, 17
        };

        GL33Helper.drawTrianglesIndexedRGB_GeneralSlow(vertexData, indices);

        stack.glPopMatrix();
    }
}
