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
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.nschmidt.ldparteditor.opengl.GLMatrixStack;
import org.nschmidt.ldparteditor.opengl.GLShader;

/**
 * @author nils
 *
 */
public class GearGL33 {

    private final FloatBuffer bvertices;
    private final IntBuffer bindices;

    public GearGL33(float radius1, int segments, float width, float toothHeight) {

        final boolean invert = radius1 < 0f;
        final float radius2 = radius1 + width;
        final float radius3 = radius2 + toothHeight;

        final List<Float> vertices = new ArrayList<>();
        final List<Integer> indices = new ArrayList<>();

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
                indices.add(i++);
                indices.add(2 + i++);
                indices.add(i);
                indices.add(i++);
                indices.add(i++ - 2);
                indices.add(i - 4);
            } else {
                indices.add(i++);
                indices.add(i++);
                indices.add(i);
                indices.add(i++);
                indices.add(i);
                indices.add(i++ - 3);
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
                    indices.add(i++);
                    indices.add(2 + i++);
                    indices.add(i);
                    indices.add(i++);
                    indices.add(i++ - 2);
                    indices.add(i - 4);
                } else {
                    indices.add(i++);
                    indices.add(i++);
                    indices.add(i);
                    indices.add(i++);
                    indices.add(i);
                    indices.add(i++ - 3);
                }
            }

        }

        bvertices = BufferUtils.createFloatBuffer(vertices.size());
        bindices = BufferUtils.createIntBuffer(indices.size());

        for (Float f : vertices) {
            bvertices.put(f);
        }
        int vertexCount = vertices.size() / 3;

        for (Integer in : indices) {
            bindices.put(in % vertexCount);
        }

        bvertices.flip();
        bindices.flip();

    }

    public GearGL33() {
        bvertices = BufferUtils.createFloatBuffer(3);
        bvertices.put(0f);
        bvertices.put(0f);
        bvertices.put(0f);
        bindices = BufferUtils.createIntBuffer(3);
        bindices.put((short) 0);
        bindices.put((short) 0);
        bindices.put((short) 0);
        bvertices.flip();
        bindices.flip();
    }

    public void draw(GLMatrixStack stack, GLShader shader, float x, float y, float z, float r, float g, float b) {

        final GLShader backup = stack.getShader();

        stack.setShader(shader);
        shader.use();

        stack.glPushMatrix();
        stack.glLoadIdentity();
        stack.glTranslatef(x, y, z);

        final int colour = shader.getUniformLocation("color"); //$NON-NLS-1$
        GL20.glUniform3f(colour, r, g, b);
        final int VBO = GL15.glGenBuffers();
        final int EBO = GL15.glGenBuffers();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, bvertices, GL15.GL_STREAM_DRAW);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, EBO);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, bindices, GL15.GL_STREAM_DRAW);

        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 12, 0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        GL11.glDrawElements(GL11.GL_TRIANGLES, bindices.capacity(), GL11.GL_UNSIGNED_INT, 0);

        GL15.glDeleteBuffers(VBO);
        GL15.glDeleteBuffers(EBO);

        stack.setShader(backup);
        backup.use();

        stack.glPopMatrix();
    }
}
