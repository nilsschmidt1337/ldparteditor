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

/**
 * @author nils
 *
 */
public class Arrow {

    final FloatBuffer matrix;

    final float EPSILON = 0.0000001f;

    final float r;
    final float g;
    final float b;
    final float a;

    final float length;
    final float cone_start;
    final float line_end;

    final float[] cone = new float[34];

    public Arrow(float r, float g, float b, float a, float dir_x, float dir_y, float dir_z, float cone_height, float cone_width) {
        dir_x = dir_x / 1000f;
        dir_y = dir_y / 1000f;
        dir_z = dir_z / 1000f;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        length = (float) Math.sqrt(dir_x * dir_x + dir_y * dir_y + dir_z * dir_z);
        cone_start = length - cone_height;
        line_end = length - cone_height / 3f;
        Matrix4f rotation = makeRotationDir(new Vector3f(dir_x, dir_y, dir_z));
        matrix = BufferUtils.createFloatBuffer(16);
        rotation.store(matrix);
        matrix.position(0);
        float cone_radius = cone_width;
        float step = (float) (Math.PI / 8d);
        float angle = 0f;
        for (int i = 0; i < 34; i += 2) {
            cone[i] = (float) (cone_radius * Math.cos(angle));
            cone[i + 1] = (float) (cone_radius * Math.sin(angle));
            angle = angle + step;
        }

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
        draw(x, y, z, zoom, 2f);
    }

    public void draw(float x, float y, float z, float zoom, float lineWidth) {
        final float zoom_inv = 1f / zoom;
        GL11.glPushMatrix();

        GL11.glTranslatef(x, y, z);
        GL11.glMultMatrix(matrix);
        GL11.glScalef(zoom_inv, zoom_inv, zoom_inv);

        GL11.glLineWidth(lineWidth);
        GL11.glColor4f(r, g, b, a);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3f(0f, 0f, 0f);
        GL11.glVertex3f(0f, line_end, 0f);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex3f(0f, length, 0f);

        GL11.glVertex3f(cone[0], cone_start, cone[1]);
        GL11.glVertex3f(cone[2], cone_start, cone[3]);
        GL11.glVertex3f(cone[4], cone_start, cone[5]);
        GL11.glVertex3f(cone[6], cone_start, cone[7]);

        GL11.glVertex3f(cone[8], cone_start, cone[9]);
        GL11.glVertex3f(cone[10], cone_start, cone[11]);
        GL11.glVertex3f(cone[12], cone_start, cone[13]);
        GL11.glVertex3f(cone[14], cone_start, cone[15]);

        GL11.glVertex3f(cone[16], cone_start, cone[17]);
        GL11.glVertex3f(cone[18], cone_start, cone[19]);
        GL11.glVertex3f(cone[20], cone_start, cone[21]);
        GL11.glVertex3f(cone[22], cone_start, cone[23]);

        GL11.glVertex3f(cone[24], cone_start, cone[25]);
        GL11.glVertex3f(cone[26], cone_start, cone[27]);
        GL11.glVertex3f(cone[28], cone_start, cone[29]);
        GL11.glVertex3f(cone[30], cone_start, cone[31]);

        GL11.glVertex3f(cone[32], cone_start, cone[33]);

        GL11.glEnd();

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex3f(0f, cone_start, 0f);

        GL11.glVertex3f(cone[32], cone_start, cone[33]);

        GL11.glVertex3f(cone[30], cone_start, cone[31]);
        GL11.glVertex3f(cone[28], cone_start, cone[29]);
        GL11.glVertex3f(cone[26], cone_start, cone[27]);
        GL11.glVertex3f(cone[24], cone_start, cone[25]);

        GL11.glVertex3f(cone[22], cone_start, cone[23]);
        GL11.glVertex3f(cone[20], cone_start, cone[21]);
        GL11.glVertex3f(cone[18], cone_start, cone[19]);
        GL11.glVertex3f(cone[16], cone_start, cone[17]);

        GL11.glVertex3f(cone[14], cone_start, cone[15]);
        GL11.glVertex3f(cone[12], cone_start, cone[13]);
        GL11.glVertex3f(cone[10], cone_start, cone[11]);
        GL11.glVertex3f(cone[8], cone_start, cone[9]);

        GL11.glVertex3f(cone[6], cone_start, cone[7]);
        GL11.glVertex3f(cone[4], cone_start, cone[5]);
        GL11.glVertex3f(cone[2], cone_start, cone[3]);
        GL11.glVertex3f(cone[0], cone_start, cone[1]);

        GL11.glEnd();

        GL11.glPopMatrix();
    }
}
