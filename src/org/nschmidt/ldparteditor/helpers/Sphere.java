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
import java.nio.ShortBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/**
 * @author nils
 *
 */
public class Sphere {

    private final FloatBuffer bvertices;
    private final ShortBuffer bindices;

    final ArrayList<Short> indices = new ArrayList<Short>();

    public Sphere(float radius, int segments) {

        final ArrayList<Float> vertices = new ArrayList<Float>();

        final double R = 1d / (segments - 1);
        final double S = 1d / (segments - 1);
        double r, s;

        for (r = 0; r < segments; ++r) {
            for (s = 0; s < segments; ++s) {

                final float y = (float) Math.sin(-0.5d * Math.PI + Math.PI * r * R);
                final float x = (float) (Math.cos(2d * Math.PI * s * S) * Math.sin(Math.PI * r * R));
                final float z = (float) (Math.sin(2d * Math.PI * s * S) * Math.sin(Math.PI * r * R));

                vertices.add(x * radius);
                vertices.add(y * radius);
                vertices.add(z * radius);

                short curRow = (short) (r * segments);
                short nextRow = (short) ((r + 1) * segments);

                if (s != segments - 1) {
                    indices.add((short) (curRow + s));
                    indices.add((short) (curRow + (s + 1)));
                    indices.add((short) (nextRow + (s + 1)));
                    indices.add((short) (nextRow + s));
                }
            }
        }
        for (r = 1; r < segments; ++r) {
            indices.remove(indices.size() - 1);
            indices.remove(indices.size() - 1);
            indices.remove(indices.size() - 1);
            indices.remove(indices.size() - 1);
        }

        bvertices = BufferUtils.createFloatBuffer(vertices.size());
        bindices = BufferUtils.createShortBuffer(indices.size());

        for (Float f : vertices) {
            bvertices.put(f);
        }
        int vertex_count = vertices.size() / 3;

        for (Short sh : indices) {
            bindices.put((short) (sh % vertex_count));
        }

        bvertices.flip();
        // bnormals.flip();
        bindices.flip();

    }

    public Sphere() {
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

        GL11.glVertexPointer(3, 0, bvertices);

        GL11.glDrawElements(GL11.GL_QUADS, bindices);

        GL11.glPopMatrix();
    }
}
