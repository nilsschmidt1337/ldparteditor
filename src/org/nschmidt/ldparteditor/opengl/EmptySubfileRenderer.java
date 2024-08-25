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
package org.nschmidt.ldparteditor.opengl;

import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.enumtype.Colour;
import org.nschmidt.ldparteditor.helper.Manipulator;

public enum EmptySubfileRenderer {
    INSTANCE;

    static float[] calculateEmptySubfileBoxes(DatFile df, Manipulator mani) {
        final VertexManager vm = df.getVertexManager();
        List<GData1> subs = vm.subfilesWithOneVertex();
        final float[] result = new float[subs.size() * 216];
        int i = 0;
        float boxScale = 1000f;
        // Prefer an in-line solution over extracted methods
        for (GData1 subfile : subs) {
            final boolean selected = vm.getSelectedSubfiles().contains(subfile);
            final float r = selected ? Colour.vertexSelectedTmpColourR : Colour.originColourR;
            final float g = selected ? Colour.vertexSelectedTmpColourG : Colour.originColourG;
            final float b = selected ? Colour.vertexSelectedTmpColourB : Colour.originColourB;

            Matrix4f m = subfile.getProductMatrix();
            if (mani.isModified() && selected) {
                m = Matrix4f.mul(mani.getTempTransformation4f(), m, null);
            }

            Vector4f topUpperLeft = Matrix4f.transform(m, new Vector4f(-boxScale, boxScale, -boxScale, 1f), null);
            Vector4f topUpperRight = Matrix4f.transform(m, new Vector4f(boxScale, boxScale, -boxScale, 1f), null);
            Vector4f topLowerRight = Matrix4f.transform(m, new Vector4f(boxScale, boxScale, boxScale, 1f), null);
            Vector4f topLowerLeft = Matrix4f.transform(m, new Vector4f(-boxScale, boxScale, boxScale, 1f), null);

            Vector4f bottomUpperLeft = Matrix4f.transform(m, new Vector4f(-boxScale, -boxScale, -boxScale, 1f), null);
            Vector4f bottomUpperRight = Matrix4f.transform(m, new Vector4f(boxScale, -boxScale, -boxScale, 1f), null);
            Vector4f bottomLowerRight = Matrix4f.transform(m, new Vector4f(boxScale, -boxScale, boxScale, 1f), null);
            Vector4f bottomLowerLeft = Matrix4f.transform(m, new Vector4f(-boxScale, -boxScale, boxScale, 1f), null);

            result[i +  0] = topUpperLeft.x;
            result[i +  1] = topUpperLeft.y;
            result[i +  2] = topUpperLeft.z;
            result[i +  3] = r;
            result[i +  4] = g;
            result[i +  5] = b;
            result[i +  6] = topUpperRight.x;
            result[i +  7] = topUpperRight.y;
            result[i +  8] = topUpperRight.z;
            result[i +  9] = r;
            result[i + 10] = g;
            result[i + 11] = b;

            result[i + 12] = topUpperRight.x;
            result[i + 13] = topUpperRight.y;
            result[i + 14] = topUpperRight.z;
            result[i + 15] = r;
            result[i + 16] = g;
            result[i + 17] = b;
            result[i + 18] = topLowerRight.x;
            result[i + 19] = topLowerRight.y;
            result[i + 20] = topLowerRight.z;
            result[i + 21] = r;
            result[i + 22] = g;
            result[i + 23] = b;

            result[i + 24] = topLowerRight.x;
            result[i + 25] = topLowerRight.y;
            result[i + 26] = topLowerRight.z;
            result[i + 27] = r;
            result[i + 28] = g;
            result[i + 29] = b;
            result[i + 30] = topLowerLeft.x;
            result[i + 31] = topLowerLeft.y;
            result[i + 32] = topLowerLeft.z;
            result[i + 33] = r;
            result[i + 34] = g;
            result[i + 35] = b;

            result[i + 36] = topLowerLeft.x;
            result[i + 37] = topLowerLeft.y;
            result[i + 38] = topLowerLeft.z;
            result[i + 39] = r;
            result[i + 40] = g;
            result[i + 41] = b;
            result[i + 42] = topUpperLeft.x;
            result[i + 43] = topUpperLeft.y;
            result[i + 44] = topUpperLeft.z;
            result[i + 45] = r;
            result[i + 46] = g;
            result[i + 47] = b;
            i += 48;

            result[i +  0] = bottomUpperLeft.x;
            result[i +  1] = bottomUpperLeft.y;
            result[i +  2] = bottomUpperLeft.z;
            result[i +  3] = r;
            result[i +  4] = g;
            result[i +  5] = b;
            result[i +  6] = bottomUpperRight.x;
            result[i +  7] = bottomUpperRight.y;
            result[i +  8] = bottomUpperRight.z;
            result[i +  9] = r;
            result[i + 10] = g;
            result[i + 11] = b;

            result[i + 12] = bottomUpperRight.x;
            result[i + 13] = bottomUpperRight.y;
            result[i + 14] = bottomUpperRight.z;
            result[i + 15] = r;
            result[i + 16] = g;
            result[i + 17] = b;
            result[i + 18] = bottomLowerRight.x;
            result[i + 19] = bottomLowerRight.y;
            result[i + 20] = bottomLowerRight.z;
            result[i + 21] = r;
            result[i + 22] = g;
            result[i + 23] = b;

            result[i + 24] = bottomLowerRight.x;
            result[i + 25] = bottomLowerRight.y;
            result[i + 26] = bottomLowerRight.z;
            result[i + 27] = r;
            result[i + 28] = g;
            result[i + 29] = b;
            result[i + 30] = bottomLowerLeft.x;
            result[i + 31] = bottomLowerLeft.y;
            result[i + 32] = bottomLowerLeft.z;
            result[i + 33] = r;
            result[i + 34] = g;
            result[i + 35] = b;

            result[i + 36] = bottomLowerLeft.x;
            result[i + 37] = bottomLowerLeft.y;
            result[i + 38] = bottomLowerLeft.z;
            result[i + 39] = r;
            result[i + 40] = g;
            result[i + 41] = b;
            result[i + 42] = bottomUpperLeft.x;
            result[i + 43] = bottomUpperLeft.y;
            result[i + 44] = bottomUpperLeft.z;
            result[i + 45] = r;
            result[i + 46] = g;
            result[i + 47] = b;
            i += 48;

            result[i +  0] = bottomUpperLeft.x;
            result[i +  1] = bottomUpperLeft.y;
            result[i +  2] = bottomUpperLeft.z;
            result[i +  3] = r;
            result[i +  4] = g;
            result[i +  5] = b;
            result[i +  6] = topUpperLeft.x;
            result[i +  7] = topUpperLeft.y;
            result[i +  8] = topUpperLeft.z;
            result[i +  9] = r;
            result[i + 10] = g;
            result[i + 11] = b;

            result[i + 12] = bottomUpperRight.x;
            result[i + 13] = bottomUpperRight.y;
            result[i + 14] = bottomUpperRight.z;
            result[i + 15] = r;
            result[i + 16] = g;
            result[i + 17] = b;
            result[i + 18] = topUpperRight.x;
            result[i + 19] = topUpperRight.y;
            result[i + 20] = topUpperRight.z;
            result[i + 21] = r;
            result[i + 22] = g;
            result[i + 23] = b;

            result[i + 24] = bottomLowerRight.x;
            result[i + 25] = bottomLowerRight.y;
            result[i + 26] = bottomLowerRight.z;
            result[i + 27] = r;
            result[i + 28] = g;
            result[i + 29] = b;
            result[i + 30] = topLowerRight.x;
            result[i + 31] = topLowerRight.y;
            result[i + 32] = topLowerRight.z;
            result[i + 33] = r;
            result[i + 34] = g;
            result[i + 35] = b;

            result[i + 36] = bottomLowerLeft.x;
            result[i + 37] = bottomLowerLeft.y;
            result[i + 38] = bottomLowerLeft.z;
            result[i + 39] = r;
            result[i + 40] = g;
            result[i + 41] = b;
            result[i + 42] = topLowerLeft.x;
            result[i + 43] = topLowerLeft.y;
            result[i + 44] = topLowerLeft.z;
            result[i + 45] = r;
            result[i + 46] = g;
            result[i + 47] = b;
            i += 48;

            result[i +  0] = bottomLowerLeft.x;
            result[i +  1] = bottomLowerLeft.y;
            result[i +  2] = bottomLowerLeft.z;
            result[i +  3] = Colour.manipulatorZAxisColourR;
            result[i +  4] = Colour.manipulatorZAxisColourG;
            result[i +  5] = Colour.manipulatorZAxisColourB;
            result[i +  6] = topLowerRight.x;
            result[i +  7] = topLowerRight.y;
            result[i +  8] = topLowerRight.z;
            result[i +  9] = Colour.manipulatorZAxisColourR;
            result[i + 10] = Colour.manipulatorZAxisColourG;
            result[i + 11] = Colour.manipulatorZAxisColourB;

            result[i + 12] = topLowerLeft.x;
            result[i + 13] = topLowerLeft.y;
            result[i + 14] = topLowerLeft.z;
            result[i + 15] = Colour.manipulatorZAxisColourR;
            result[i + 16] = Colour.manipulatorZAxisColourG;
            result[i + 17] = Colour.manipulatorZAxisColourB;
            result[i + 18] = bottomLowerRight.x;
            result[i + 19] = bottomLowerRight.y;
            result[i + 20] = bottomLowerRight.z;
            result[i + 21] = Colour.manipulatorZAxisColourR;
            result[i + 22] = Colour.manipulatorZAxisColourG;
            result[i + 23] = Colour.manipulatorZAxisColourB;
            i += 24;

            result[i +  0] = topLowerRight.x;
            result[i +  1] = topLowerRight.y;
            result[i +  2] = topLowerRight.z;
            result[i +  3] = Colour.manipulatorYAxisColourR;
            result[i +  4] = Colour.manipulatorYAxisColourG;
            result[i +  5] = Colour.manipulatorYAxisColourB;
            result[i +  6] = topUpperLeft.x;
            result[i +  7] = topUpperLeft.y;
            result[i +  8] = topUpperLeft.z;
            result[i +  9] = Colour.manipulatorYAxisColourR;
            result[i + 10] = Colour.manipulatorYAxisColourG;
            result[i + 11] = Colour.manipulatorYAxisColourB;

            result[i + 12] = topUpperRight.x;
            result[i + 13] = topUpperRight.y;
            result[i + 14] = topUpperRight.z;
            result[i + 15] = Colour.manipulatorYAxisColourR;
            result[i + 16] = Colour.manipulatorYAxisColourG;
            result[i + 17] = Colour.manipulatorYAxisColourB;
            result[i + 18] = topLowerLeft.x;
            result[i + 19] = topLowerLeft.y;
            result[i + 20] = topLowerLeft.z;
            result[i + 21] = Colour.manipulatorYAxisColourR;
            result[i + 22] = Colour.manipulatorYAxisColourG;
            result[i + 23] = Colour.manipulatorYAxisColourB;
            i += 24;

            result[i +  0] = bottomUpperRight.x;
            result[i +  1] = bottomUpperRight.y;
            result[i +  2] = bottomUpperRight.z;
            result[i +  3] = Colour.manipulatorXAxisColourR;
            result[i +  4] = Colour.manipulatorXAxisColourG;
            result[i +  5] = Colour.manipulatorXAxisColourB;
            result[i +  6] = topLowerRight.x;
            result[i +  7] = topLowerRight.y;
            result[i +  8] = topLowerRight.z;
            result[i +  9] = Colour.manipulatorXAxisColourR;
            result[i + 10] = Colour.manipulatorXAxisColourG;
            result[i + 11] = Colour.manipulatorXAxisColourB;

            result[i + 12] = topUpperRight.x;
            result[i + 13] = topUpperRight.y;
            result[i + 14] = topUpperRight.z;
            result[i + 15] = Colour.manipulatorXAxisColourR;
            result[i + 16] = Colour.manipulatorXAxisColourG;
            result[i + 17] = Colour.manipulatorXAxisColourB;
            result[i + 18] = bottomLowerRight.x;
            result[i + 19] = bottomLowerRight.y;
            result[i + 20] = bottomLowerRight.z;
            result[i + 21] = Colour.manipulatorXAxisColourR;
            result[i + 22] = Colour.manipulatorXAxisColourG;
            result[i + 23] = Colour.manipulatorXAxisColourB;
            i += 24;
        }
        return result;
    }

    static void drawEmptySubfileBoxes20(DatFile df, Manipulator mani) {
        float[] emptySubfileBoxes = calculateEmptySubfileBoxes(df, mani);
        final int length = emptySubfileBoxes.length;
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glLineWidth(2f);
        GL11.glBegin(GL11.GL_LINES);
        for (int i = 0; i < length; i += 12) {
            GL11.glColor3f(emptySubfileBoxes[i + 3], emptySubfileBoxes[i + 4], emptySubfileBoxes[i + 5]);
            GL11.glVertex3f(emptySubfileBoxes[i + 0], emptySubfileBoxes[i + 1], emptySubfileBoxes[i + 2]);
            GL11.glVertex3f(emptySubfileBoxes[i + 6], emptySubfileBoxes[i + 7], emptySubfileBoxes[i + 8]);
        }

        GL11.glEnd();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }
}
