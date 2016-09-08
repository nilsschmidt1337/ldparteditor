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
public class Arc {

    final FloatBuffer matrix;

    final float EPSILON = 0.0000001f;

    final float r;
    final float g;
    final float b;
    final float a;

    final float arc_width;
    final float arc_width_neg;

    final float[] circle = new float[66];
    final float[] circle2 = new float[66];

    public Arc(float r, float g, float b, float a, float dir_x, float dir_y, float dir_z, float radius, float arc_width) {
        dir_x = dir_x / 1000f;
        dir_y = dir_y / 1000f;
        dir_z = dir_z / 1000f;
        radius = radius / 1000f;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;

        this.arc_width = arc_width;
        this.arc_width_neg = -arc_width;

        Matrix4f rotation = makeRotationDir(new Vector3f(dir_x, dir_y, dir_z));
        matrix = BufferUtils.createFloatBuffer(16);
        rotation.store(matrix);
        matrix.position(0);

        final float step = (float) (Math.PI / 16d);
        float angle = 0f;
        final float radius2 = radius - arc_width / 6f;
        for (int i = 0; i < 66; i += 2) {
            circle[i] = (float) (radius * Math.cos(angle));
            circle[i + 1] = (float) (radius * Math.sin(angle));
            circle2[i] = (float) (radius2 * Math.cos(angle));
            circle2[i + 1] = (float) (radius2 * Math.sin(angle));
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
        final float zoom_inv = 1f / zoom;
        GL11.glPushMatrix();

        GL11.glTranslatef(x, y, z);
        GL11.glMultMatrixf(matrix);
        GL11.glScalef(zoom_inv, zoom_inv, zoom_inv);

        GL11.glColor4f(r, g, b, a);
        GL11.glBegin(GL11.GL_QUAD_STRIP);

        GL11.glVertex3f(circle[0], 0f, circle[1]);
        GL11.glVertex3f(circle2[0], arc_width, circle2[1]);
        GL11.glVertex3f(circle[2], 0f, circle[3]);
        GL11.glVertex3f(circle2[2], arc_width, circle2[3]);
        GL11.glVertex3f(circle[4], 0f, circle[5]);
        GL11.glVertex3f(circle2[4], arc_width, circle2[5]);
        GL11.glVertex3f(circle[6], 0f, circle[7]);
        GL11.glVertex3f(circle2[6], arc_width, circle2[7]);

        GL11.glVertex3f(circle[8], 0f, circle[9]);
        GL11.glVertex3f(circle2[8], arc_width, circle2[9]);
        GL11.glVertex3f(circle[10], 0f, circle[11]);
        GL11.glVertex3f(circle2[10], arc_width, circle2[11]);
        GL11.glVertex3f(circle[12], 0f, circle[13]);
        GL11.glVertex3f(circle2[12], arc_width, circle2[13]);
        GL11.glVertex3f(circle[14], 0f, circle[15]);
        GL11.glVertex3f(circle2[14], arc_width, circle2[15]);

        GL11.glVertex3f(circle[16], 0f, circle[17]);
        GL11.glVertex3f(circle2[16], arc_width, circle2[17]);
        GL11.glVertex3f(circle[18], 0f, circle[19]);
        GL11.glVertex3f(circle2[18], arc_width, circle2[19]);
        GL11.glVertex3f(circle[20], 0f, circle[21]);
        GL11.glVertex3f(circle2[20], arc_width, circle2[21]);
        GL11.glVertex3f(circle[22], 0f, circle[23]);
        GL11.glVertex3f(circle2[22], arc_width, circle2[23]);

        GL11.glVertex3f(circle[24], 0f, circle[25]);
        GL11.glVertex3f(circle2[24], arc_width, circle2[25]);
        GL11.glVertex3f(circle[26], 0f, circle[27]);
        GL11.glVertex3f(circle2[26], arc_width, circle2[27]);
        GL11.glVertex3f(circle[28], 0f, circle[29]);
        GL11.glVertex3f(circle2[28], arc_width, circle2[29]);
        GL11.glVertex3f(circle[30], 0f, circle[31]);
        GL11.glVertex3f(circle2[30], arc_width, circle2[31]);

        GL11.glVertex3f(circle[32], 0f, circle[33]);
        GL11.glVertex3f(circle2[32], arc_width, circle2[33]);
        GL11.glVertex3f(circle[34], 0f, circle[35]);
        GL11.glVertex3f(circle2[34], arc_width, circle2[35]);
        GL11.glVertex3f(circle[36], 0f, circle[37]);
        GL11.glVertex3f(circle2[36], arc_width, circle2[37]);
        GL11.glVertex3f(circle[38], 0f, circle[39]);
        GL11.glVertex3f(circle2[38], arc_width, circle2[39]);

        GL11.glVertex3f(circle[40], 0f, circle[41]);
        GL11.glVertex3f(circle2[40], arc_width, circle2[41]);
        GL11.glVertex3f(circle[42], 0f, circle[43]);
        GL11.glVertex3f(circle2[42], arc_width, circle2[43]);
        GL11.glVertex3f(circle[44], 0f, circle[45]);
        GL11.glVertex3f(circle2[44], arc_width, circle2[45]);
        GL11.glVertex3f(circle[46], 0f, circle[47]);
        GL11.glVertex3f(circle2[46], arc_width, circle2[47]);

        GL11.glVertex3f(circle[48], 0f, circle[49]);
        GL11.glVertex3f(circle2[48], arc_width, circle2[49]);
        GL11.glVertex3f(circle[50], 0f, circle[51]);
        GL11.glVertex3f(circle2[50], arc_width, circle2[51]);
        GL11.glVertex3f(circle[52], 0f, circle[53]);
        GL11.glVertex3f(circle2[52], arc_width, circle2[53]);
        GL11.glVertex3f(circle[54], 0f, circle[55]);
        GL11.glVertex3f(circle2[54], arc_width, circle2[55]);

        GL11.glVertex3f(circle[56], 0f, circle[57]);
        GL11.glVertex3f(circle2[56], arc_width, circle2[57]);
        GL11.glVertex3f(circle[58], 0f, circle[59]);
        GL11.glVertex3f(circle2[58], arc_width, circle2[59]);
        GL11.glVertex3f(circle[60], 0f, circle[61]);
        GL11.glVertex3f(circle2[60], arc_width, circle2[61]);
        GL11.glVertex3f(circle[62], 0f, circle[63]);
        GL11.glVertex3f(circle2[62], arc_width, circle2[63]);

        GL11.glVertex3f(circle[64], 0f, circle[65]);
        GL11.glVertex3f(circle2[64], arc_width, circle2[65]);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_QUAD_STRIP);

        GL11.glVertex3f(circle2[0], arc_width_neg, circle2[1]);
        GL11.glVertex3f(circle[0], 0f, circle[1]);
        GL11.glVertex3f(circle2[2], arc_width_neg, circle2[3]);
        GL11.glVertex3f(circle[2], 0f, circle[3]);
        GL11.glVertex3f(circle2[4], arc_width_neg, circle2[5]);
        GL11.glVertex3f(circle[4], 0f, circle[5]);
        GL11.glVertex3f(circle2[6], arc_width_neg, circle2[7]);
        GL11.glVertex3f(circle[6], 0f, circle[7]);

        GL11.glVertex3f(circle2[8], arc_width_neg, circle2[9]);
        GL11.glVertex3f(circle[8], 0f, circle[9]);
        GL11.glVertex3f(circle2[10], arc_width_neg, circle2[11]);
        GL11.glVertex3f(circle[10], 0f, circle[11]);
        GL11.glVertex3f(circle2[12], arc_width_neg, circle2[13]);
        GL11.glVertex3f(circle[12], 0f, circle[13]);
        GL11.glVertex3f(circle2[14], arc_width_neg, circle2[15]);
        GL11.glVertex3f(circle[14], 0f, circle[15]);

        GL11.glVertex3f(circle2[16], arc_width_neg, circle2[17]);
        GL11.glVertex3f(circle[16], 0f, circle[17]);
        GL11.glVertex3f(circle2[18], arc_width_neg, circle2[19]);
        GL11.glVertex3f(circle[18], 0f, circle[19]);
        GL11.glVertex3f(circle2[20], arc_width_neg, circle2[21]);
        GL11.glVertex3f(circle[20], 0f, circle[21]);
        GL11.glVertex3f(circle2[22], arc_width_neg, circle2[23]);
        GL11.glVertex3f(circle[22], 0f, circle[23]);

        GL11.glVertex3f(circle2[24], arc_width_neg, circle2[25]);
        GL11.glVertex3f(circle[24], 0f, circle[25]);
        GL11.glVertex3f(circle2[26], arc_width_neg, circle2[27]);
        GL11.glVertex3f(circle[26], 0f, circle[27]);
        GL11.glVertex3f(circle2[28], arc_width_neg, circle2[29]);
        GL11.glVertex3f(circle[28], 0f, circle[29]);
        GL11.glVertex3f(circle2[30], arc_width_neg, circle2[31]);
        GL11.glVertex3f(circle[30], 0f, circle[31]);

        GL11.glVertex3f(circle2[32], arc_width_neg, circle2[33]);
        GL11.glVertex3f(circle[32], 0f, circle[33]);
        GL11.glVertex3f(circle2[34], arc_width_neg, circle2[35]);
        GL11.glVertex3f(circle[34], 0f, circle[35]);
        GL11.glVertex3f(circle2[36], arc_width_neg, circle2[37]);
        GL11.glVertex3f(circle[36], 0f, circle[37]);
        GL11.glVertex3f(circle2[38], arc_width_neg, circle2[39]);
        GL11.glVertex3f(circle[38], 0f, circle[39]);

        GL11.glVertex3f(circle2[40], arc_width_neg, circle2[41]);
        GL11.glVertex3f(circle[40], 0f, circle[41]);
        GL11.glVertex3f(circle2[42], arc_width_neg, circle2[43]);
        GL11.glVertex3f(circle[42], 0f, circle[43]);
        GL11.glVertex3f(circle2[44], arc_width_neg, circle2[45]);
        GL11.glVertex3f(circle[44], 0f, circle[45]);
        GL11.glVertex3f(circle2[46], arc_width_neg, circle2[47]);
        GL11.glVertex3f(circle[46], 0f, circle[47]);

        GL11.glVertex3f(circle2[48], arc_width_neg, circle2[49]);
        GL11.glVertex3f(circle[48], 0f, circle[49]);
        GL11.glVertex3f(circle2[50], arc_width_neg, circle2[51]);
        GL11.glVertex3f(circle[50], 0f, circle[51]);
        GL11.glVertex3f(circle2[52], arc_width_neg, circle2[53]);
        GL11.glVertex3f(circle[52], 0f, circle[53]);
        GL11.glVertex3f(circle2[54], arc_width_neg, circle2[55]);
        GL11.glVertex3f(circle[54], 0f, circle[55]);

        GL11.glVertex3f(circle2[56], arc_width_neg, circle2[57]);
        GL11.glVertex3f(circle[56], 0f, circle[57]);
        GL11.glVertex3f(circle2[58], arc_width_neg, circle2[59]);
        GL11.glVertex3f(circle[58], 0f, circle[59]);
        GL11.glVertex3f(circle2[60], arc_width_neg, circle2[61]);
        GL11.glVertex3f(circle[60], 0f, circle[61]);
        GL11.glVertex3f(circle2[62], arc_width_neg, circle2[63]);
        GL11.glVertex3f(circle[62], 0f, circle[63]);

        GL11.glVertex3f(circle2[64], arc_width_neg, circle2[65]);
        GL11.glVertex3f(circle[64], 0f, circle[65]);

        GL11.glEnd();

        GL11.glPopMatrix();
    }

}
