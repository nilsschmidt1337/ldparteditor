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

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class GearGL20 {

    private final FloatBuffer bvertices;
    private final ShortBuffer bindices;

    public GearGL20(float radius1, int segments, float width, float toothHeight) {

        final boolean invert = radius1 < 0f;
        final float radius2 = radius1 + width;
        final float radius3 = radius2 + toothHeight;

        final List<Float> vertices = new ArrayList<>();
        final List<Short> indices = new ArrayList<>();

        final double R = 2 * Math.PI / segments;
        double r;
        int i = 0;

        for (r = 0; r < segments; ++r) {

            final float y1 = (float) Math.sin(r * R);
            final float x1 = (float) Math.cos(r * R);
            final float y2 = (float) Math.sin((r + 1) * R);
            final float x2 = (float) Math.cos((r + 1) * R);

            vertices.add(x1 * radius1);
            vertices.add(y1 * radius1);
            vertices.add(0f);

            vertices.add(x2 * radius1);
            vertices.add(y2 * radius1);
            vertices.add(0f);

            vertices.add(x2 * radius2);
            vertices.add(y2 * radius2);
            vertices.add(0f);

            vertices.add(x1 * radius2);
            vertices.add(y1 * radius2);
            vertices.add(0f);

            if (invert) {
                indices.add((short) i++);
                indices.add((short) (2 + i++));
                indices.add((short) i++);
                indices.add((short) (i++ - 2));
            } else {
                indices.add((short) i++);
                indices.add((short) i++);
                indices.add((short) i++);
                indices.add((short) i++);
            }

            if (r % 2 == 0) {

                final float y1n = (float) Math.sin((r + .2f) * R);
                final float x1n = (float) Math.cos((r + .2f) * R);
                final float y2n = (float) Math.sin((r + .8f) * R);
                final float x2n = (float) Math.cos((r + .8f) * R);

                vertices.add(x1 * radius2);
                vertices.add(y1 * radius2);
                vertices.add(0f);

                vertices.add(x2 * radius2);
                vertices.add(y2 * radius2);
                vertices.add(0f);

                vertices.add(x2n * radius3);
                vertices.add(y2n * radius3);
                vertices.add(0f);

                vertices.add(x1n * radius3);
                vertices.add(y1n * radius3);
                vertices.add(0f);

                if (invert) {
                    indices.add((short) i++);
                    indices.add((short) (2 + i++));
                    indices.add((short) i++);
                    indices.add((short) (i++ - 2));
                } else {
                    indices.add((short) i++);
                    indices.add((short) i++);
                    indices.add((short) i++);
                    indices.add((short) i++);
                }
            }

        }

        bvertices = BufferUtils.createFloatBuffer(vertices.size());
        bindices = BufferUtils.createShortBuffer(indices.size());

        for (Float f : vertices) {
            bvertices.put(f);
        }
        int vertexCount = vertices.size() / 3;

        for (Short sh : indices) {
            bindices.put((short) (sh % vertexCount));
        }

        bvertices.flip();
        bindices.flip();

    }

    public GearGL20() {
        bvertices = BufferUtils.createFloatBuffer(3);
        bvertices.put(0f);
        bvertices.put(0f);
        bvertices.put(0f);
        bindices = BufferUtils.createShortBuffer(3);
        bindices.put((short) 0);
        bindices.put((short) 0);
        bindices.put((short) 0);
        bvertices.flip();
        bindices.flip();
    }

    public void draw(float x, float y, float z) {
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, z);

        GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, bvertices);

        GL11.glDrawElements(GL11.GL_QUADS, bindices);

        GL11.glPopMatrix();
    }
}
