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

/**
 * The legendary OpenGL teapot!
 *
 * @author nils
 *
 */
public enum Teapot {
    INSTANCE;

    static final int patchdata[][] = {
        /* rim */
        { 102, 103, 104, 105, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 },
        /* body */
        { 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27 }, { 24, 25, 26, 27, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40 },
        /* lid */
        { 96, 96, 96, 96, 97, 98, 99, 100, 101, 101, 101, 101, 0, 1, 2, 3, }, { 0, 1, 2, 3, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117 },
        /* bottom */
        { 118, 118, 118, 118, 124, 122, 119, 121, 123, 126, 125, 120, 40, 39, 38, 37 },
        /* handle */
        { 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56 }, { 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 28, 65, 66, 67 },
        /* spout */
        { 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83 }, { 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95 } };

    /** Used to make the teapot. **/
    static final float cpdata[][] = { { 0.2f, 0, 2.7f }, { 0.2f, -0.112f, 2.7f }, { 0.112f, -0.2f, 2.7f }, { 0, -0.2f, 2.7f }, { 1.3375f, 0, 2.53125f }, { 1.3375f, -0.749f, 2.53125f },
        { 0.749f, -1.3375f, 2.53125f }, { 0, -1.3375f, 2.53125f }, { 1.4375f, 0, 2.53125f }, { 1.4375f, -0.805f, 2.53125f }, { 0.805f, -1.4375f, 2.53125f }, { 0, -1.4375f, 2.53125f },
        { 1.5f, 0, 2.4f }, { 1.5f, -0.84f, 2.4f }, { 0.84f, -1.5f, 2.4f }, { 0, -1.5f, 2.4f }, { 1.75f, 0, 1.875f }, { 1.75f, -0.98f, 1.875f }, { 0.98f, -1.75f, 1.875f }, { 0, -1.75f, 1.875f },
        { 2f, 0, 1.35f }, { 2f, -1.12f, 1.35f }, { 1.12f, -2f, 1.35f }, { 0, -2f, 1.35f }, { 2f, 0, 0.9f }, { 2f, -1.12f, 0.9f }, { 1.12f, -2f, 0.9f }, { 0, -2f, 0.9f }, { -2f, 0, 0.9f },
        { 2f, 0, 0.45f }, { 2f, -1.12f, 0.45f }, { 1.12f, -2f, 0.45f }, { 0, -2f, 0.45f }, { 1.5f, 0, 0.225f }, { 1.5f, -0.84f, 0.225f }, { 0.84f, -1.5f, 0.225f }, { 0, -1.5f, 0.225f },
        { 1.5f, 0, 0.15f }, { 1.5f, -0.84f, 0.15f }, { 0.84f, -1.5f, 0.15f }, { 0, -1.5f, 0.15f }, { -1.6f, 0, 2.025f }, { -1.6f, -0.3f, 2.025f }, { -1.5f, -0.3f, 2.25f }, { -1.5f, 0, 2.25f },
        { -2.3f, 0, 2.025f }, { -2.3f, -0.3f, 2.025f }, { -2.5f, -0.3f, 2.25f }, { -2.5f, 0, 2.25f }, { -2.7f, 0, 2.025f }, { -2.7f, -0.3f, 2.025f }, { -3f, -0.3f, 2.25f }, { -3f, 0, 2.25f },
        { -2.7f, 0, 1.8f }, { -2.7f, -0.3f, 1.8f }, { -3f, -0.3f, 1.8f }, { -3f, 0, 1.8f }, { -2.7f, 0, 1.575f }, { -2.7f, -0.3f, 1.575f }, { -3f, -0.3f, 1.35f }, { -3f, 0, 1.35f },
        { -2.5f, 0, 1.125f }, { -2.5f, -0.3f, 1.125f }, { -2.65f, -0.3f, 0.9375f }, { -2.65f, 0, 0.9375f }, { -2f, -0.3f, 0.9f }, { -1.9f, -0.3f, 0.6f }, { -1.9f, 0, 0.6f }, { 1.7f, 0, 1.425f },
        { 1.7f, -0.66f, 1.425f }, { 1.7f, -0.66f, 0.6f }, { 1.7f, 0, 0.6f }, { 2.6f, 0, 1.425f }, { 2.6f, -0.66f, 1.425f }, { 3.1f, -0.66f, 0.825f }, { 3.1f, 0, 0.825f }, { 2.3f, 0, 2.1f },
        { 2.3f, -0.25f, 2.1f }, { 2.4f, -0.25f, 2.025f }, { 2.4f, 0, 2.025f }, { 2.7f, 0, 2.4f }, { 2.7f, -0.25f, 2.4f }, { 3.3f, -0.25f, 2.4f }, { 3.3f, 0, 2.4f }, { 2.8f, 0, 2.475f },
        { 2.8f, -0.25f, 2.475f }, { 3.525f, -0.25f, 2.49375f }, { 3.525f, 0, 2.49375f }, { 2.9f, 0, 2.475f }, { 2.9f, -0.15f, 2.475f }, { 3.45f, -0.15f, 2.5125f }, { 3.45f, 0, 2.5125f },
        { 2.8f, 0, 2.4f }, { 2.8f, -0.15f, 2.4f }, { 3.2f, -0.15f, 2.4f }, { 3.2f, 0, 2.4f }, { 0, 0, 3.15f }, { 0.8f, 0, 3.15f }, { 0.8f, -0.45f, 3.15f }, { 0.45f, -0.8f, 3.15f },
        { 0, -0.8f, 3.15f }, { 0, 0, 2.85f }, { 1.4f, 0, 2.4f }, { 1.4f, -0.784f, 2.4f }, { 0.784f, -1.4f, 2.4f }, { 0, -1.4f, 2.4f }, { 0.4f, 0, 2.55f }, { 0.4f, -0.224f, 2.55f },
        { 0.224f, -0.4f, 2.55f }, { 0, -0.4f, 2.55f }, { 1.3f, 0, 2.55f }, { 1.3f, -0.728f, 2.55f }, { 0.728f, -1.3f, 2.55f }, { 0, -1.3f, 2.55f }, { 1.3f, 0, 2.4f }, { 1.3f, -0.728f, 2.4f },
        { 0.728f, -1.3f, 2.4f }, { 0, -1.3f, 2.4f }, { 0, 0, 0 }, { 1.425f, -0.798f, 0 }, { 1.5f, 0, 0.075f }, { 1.425f, 0, 0 }, { 0.798f, -1.425f, 0 }, { 0, -1.5f, 0.075f }, { 0, -1.425f, 0 },
        { 1.5f, -0.84f, 0.075f }, { 0.84f, -1.5f, 0.075f } };

    /**
     * Draws the teapot
     *
     * @param grid
     *            Grid size.
     * @param scale
     *            Scale (e.g. 1.0f).
     */
    public static void fastSolidTeapot(int grid, float scale) {
        float p[][][] = new float[4][4][3];
        float q[][][] = new float[4][4][3];
        float r[][][] = new float[4][4][3];
        float s[][][] = new float[4][4][3];
        int i, j, k, l;
        final FloatBuffer buffer = BufferUtils.createFloatBuffer(4 * 4 * 3);

        GL11.glEnable(GL11.GL_AUTO_NORMAL);
        GL11.glEnable(GL11.GL_MAP2_VERTEX_3);
        GL11.glRotatef(270.0f, 1.0f, 0.0f, 0.0f);
        GL11.glScalef(0.5f * scale, 0.5f * scale, 0.5f * scale);
        GL11.glTranslatef(0.0f, 0.0f, -1.5f);

        for (i = 0; i < 10; i++) {
            for (j = 0; j < 4; j++) {
                for (k = 0; k < 4; k++) {
                    for (l = 0; l < 3; l++) {
                        p[j][k][l] = cpdata[patchdata[i][j * 4 + k]][l];
                        q[j][k][l] = cpdata[patchdata[i][j * 4 + 3 - k]][l];

                        if (l == 1)
                            q[j][k][l] *= -1.0;

                        if (i < 6) {
                            r[j][k][l] = cpdata[patchdata[i][j * 4 + 3 - k]][l];

                            if (l == 0)
                                r[j][k][l] *= -1.0;

                            s[j][k][l] = cpdata[patchdata[i][j * 4 + k]][l];

                            if (l == 0)
                                s[j][k][l] *= -1.0;

                            if (l == 1)
                                s[j][k][l] *= -1.0;
                        }
                    }
                }
            }

            putIntoBuffer(buffer, p);
            GL11.glMap2f(GL11.GL_MAP2_VERTEX_3, 0, 1, 3, 4, 0, 1, 12, 4, buffer);
            GL11.glMapGrid2f(grid, 0.0f, 1.0f, grid, 0.0f, 1.0f);
            GL11.glEvalMesh2(GL11.GL_FILL, 0, grid, 0, grid);
            putIntoBuffer(buffer, q);
            GL11.glMap2f(GL11.GL_MAP2_VERTEX_3, 0, 1, 3, 4, 0, 1, 12, 4, buffer);
            GL11.glEvalMesh2(GL11.GL_FILL, 0, grid, 0, grid);

            if (i < 6) {
                putIntoBuffer(buffer, r);
                GL11.glMap2f(GL11.GL_MAP2_VERTEX_3, 0, 1, 3, 4, 0, 1, 12, 4, buffer);
                GL11.glEvalMesh2(GL11.GL_FILL, 0, grid, 0, grid);
                putIntoBuffer(buffer, s);
                GL11.glMap2f(GL11.GL_MAP2_VERTEX_3, 0, 1, 3, 4, 0, 1, 12, 4, buffer);
                GL11.glEvalMesh2(GL11.GL_FILL, 0, grid, 0, grid);
            }
        }
    }

    private static void putIntoBuffer(FloatBuffer buffer, float array[][][]) {
        buffer.clear();

        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                for (int k = 0; k < 3; k++)
                    buffer.put(array[i][j][k]);

        buffer.rewind();
    }

}
