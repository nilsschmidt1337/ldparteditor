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
import org.nschmidt.ldparteditor.opengl.GLMatrixStack;

/**
 * @author nils
 *
 */
public class ArrowBlunt {

    final FloatBuffer matrix;

    final float EPSILON = 0.0000001f;

    final float r;
    final float g;
    final float b;
    final float a;

    final float line_end;
    final float line_width;

    final float[] cube_x = new float[8];
    final float[] cube_y = new float[8];
    final float[] cube_z = new float[8];

    public ArrowBlunt(float r, float g, float b, float a, float dir_x, float dir_y, float dir_z, float edge_length, float line_width) {
        dir_x = dir_x / 1000f;
        dir_y = dir_y / 1000f;
        dir_z = dir_z / 1000f;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.line_width = line_width;
        final float length = (float) Math.sqrt(dir_x * dir_x + dir_y * dir_y + dir_z * dir_z);
        final float cube_start = length - edge_length;
        line_end = length - edge_length / 3f;
        Matrix4f rotation = makeRotationDir(new Vector3f(dir_x, dir_y, dir_z));
        matrix = BufferUtils.createFloatBuffer(16);
        rotation.store(matrix);
        matrix.position(0);

        final float half_edge_length = edge_length / 2f;
        final float half_edge_length_neg = -half_edge_length;
        cube_x[0] = half_edge_length;
        cube_y[0] = cube_start;
        cube_z[0] = half_edge_length;
        cube_x[1] = half_edge_length;
        cube_y[1] = cube_start;
        cube_z[1] = half_edge_length_neg;
        cube_x[2] = half_edge_length_neg;
        cube_y[2] = cube_start;
        cube_z[2] = half_edge_length_neg;
        cube_x[3] = half_edge_length_neg;
        cube_y[3] = cube_start;
        cube_z[3] = half_edge_length;

        cube_x[4] = half_edge_length;
        cube_y[4] = length;
        cube_z[4] = half_edge_length;
        cube_x[5] = half_edge_length;
        cube_y[5] = length;
        cube_z[5] = half_edge_length_neg;
        cube_x[6] = half_edge_length_neg;
        cube_y[6] = length;
        cube_z[6] = half_edge_length_neg;
        cube_x[7] = half_edge_length_neg;
        cube_y[7] = length;
        cube_z[7] = half_edge_length;
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

        GL11.glLineWidth(line_width);
        GL11.glColor4f(r, g, b, a);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3f(0f, 0f, 0f);
        GL11.glVertex3f(0f, line_end, 0f);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_QUADS);

        GL11.glVertex3f(cube_x[3], cube_y[3], cube_z[3]);
        GL11.glVertex3f(cube_x[2], cube_y[2], cube_z[2]);
        GL11.glVertex3f(cube_x[1], cube_y[1], cube_z[1]);
        GL11.glVertex3f(cube_x[0], cube_y[0], cube_z[0]);

        GL11.glVertex3f(cube_x[4], cube_y[4], cube_z[4]);
        GL11.glVertex3f(cube_x[5], cube_y[5], cube_z[5]);
        GL11.glVertex3f(cube_x[6], cube_y[6], cube_z[6]);
        GL11.glVertex3f(cube_x[7], cube_y[7], cube_z[7]);

        GL11.glEnd();

        GL11.glBegin(GL11.GL_QUAD_STRIP);
        GL11.glVertex3f(cube_x[0], cube_y[0], cube_z[0]);
        GL11.glVertex3f(cube_x[4], cube_y[4], cube_z[4]);
        GL11.glVertex3f(cube_x[3], cube_y[3], cube_z[3]);
        GL11.glVertex3f(cube_x[7], cube_y[7], cube_z[7]);
        GL11.glVertex3f(cube_x[2], cube_y[2], cube_z[2]);
        GL11.glVertex3f(cube_x[6], cube_y[6], cube_z[6]);
        GL11.glVertex3f(cube_x[1], cube_y[1], cube_z[1]);
        GL11.glVertex3f(cube_x[5], cube_y[5], cube_z[5]);
        GL11.glVertex3f(cube_x[0], cube_y[0], cube_z[0]);
        GL11.glVertex3f(cube_x[4], cube_y[4], cube_z[4]);
        GL11.glEnd();

        GL11.glPopMatrix();
    }
    
    public void drawGL33_RGB(GLMatrixStack stack, float x, float y, float z, float zoom) {
        
    }
}
