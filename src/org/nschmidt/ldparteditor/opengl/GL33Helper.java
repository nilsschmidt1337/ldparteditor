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

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class GL33Helper {

    private static final int POSITION_SHADER_LOCATION = 0;
    private static final int COLOUR_SHADER_LOCATION = 1;
    private static final int TEX_NORMAL_SHADER_LOCATION = 1;
    private static final int TEX_COLOUR_SHADER_LOCATION = 2;
    private static final int UV_SHADER_LOCATION = 3;
    private static final int RGB_STRIDE = (3 + 3) * 4;
    private static final int RGB_UV_STRIDE = (3 + 3 + 4 + 2) * 4;

    private int vboGeneral = -1;
    private int eboGeneral = -1;

    void createVBO() {
        vboGeneral = GL15.glGenBuffers();
        eboGeneral = GL15.glGenBuffers();
    }

    void destroyVBO() {
        GL15.glDeleteBuffers(vboGeneral);
        GL15.glDeleteBuffers(eboGeneral);
    }

    void drawTrianglesIndexedRGB_General(float[] vertices, int[] indices) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboGeneral);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STREAM_DRAW);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboGeneral);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_STREAM_DRAW);

        GL20.glEnableVertexAttribArray(POSITION_SHADER_LOCATION);
        GL20.glVertexAttribPointer(POSITION_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, RGB_STRIDE, 0);

        GL20.glEnableVertexAttribArray(COLOUR_SHADER_LOCATION);
        GL20.glVertexAttribPointer(COLOUR_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, RGB_STRIDE, 12); // 3 * 4

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        GL11.glDrawElements(GL11.GL_TRIANGLES, indices.length, GL11.GL_UNSIGNED_INT, 0);
    }

    void drawLinesRGB_General(float[] vertices) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboGeneral);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STREAM_DRAW);

        GL20.glEnableVertexAttribArray(POSITION_SHADER_LOCATION);
        GL20.glVertexAttribPointer(POSITION_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, RGB_STRIDE, 0);

        GL20.glEnableVertexAttribArray(COLOUR_SHADER_LOCATION);
        GL20.glVertexAttribPointer(COLOUR_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, RGB_STRIDE, 12); // 3 * 4

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        GL11.glDrawArrays(GL11.GL_LINES, 0, vertices.length);
    }

    public static void drawLinesRGB_GeneralSlow(float[] vertices) {
        int vboGeneral = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboGeneral);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STREAM_DRAW);

        GL20.glEnableVertexAttribArray(POSITION_SHADER_LOCATION);
        GL20.glVertexAttribPointer(POSITION_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, RGB_STRIDE, 0);

        GL20.glEnableVertexAttribArray(COLOUR_SHADER_LOCATION);
        GL20.glVertexAttribPointer(COLOUR_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, RGB_STRIDE, 12); // 3 * 4

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        GL11.glDrawArrays(GL11.GL_LINES, 0, vertices.length);
        GL15.glDeleteBuffers(vboGeneral);
    }

    public static void drawTrianglesIndexedRGB_GeneralSlow(float[] vertices, int[] indices) {
        int vboGeneral = GL15.glGenBuffers();
        int eboGeneral = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboGeneral);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STREAM_DRAW);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboGeneral);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_STREAM_DRAW);

        GL20.glEnableVertexAttribArray(POSITION_SHADER_LOCATION);
        GL20.glVertexAttribPointer(POSITION_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, RGB_STRIDE, 0);

        GL20.glEnableVertexAttribArray(COLOUR_SHADER_LOCATION);
        GL20.glVertexAttribPointer(COLOUR_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, RGB_STRIDE, 12); // 3 * 4

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        GL11.glDrawElements(GL11.GL_TRIANGLES, indices.length, GL11.GL_UNSIGNED_INT, 0);

        GL15.glDeleteBuffers(vboGeneral);
        GL15.glDeleteBuffers(eboGeneral);
    }

    public static void drawTrianglesIndexedTextured_GeneralSlow(float[] vertices, int[] indices) {
        int vaoGeneral = GL30.glGenVertexArrays();
        int vboGeneral = GL15.glGenBuffers();
        int eboGeneral = GL15.glGenBuffers();
        GL30.glBindVertexArray(vaoGeneral);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboGeneral);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboGeneral);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_STATIC_DRAW);

        GL20.glEnableVertexAttribArray(POSITION_SHADER_LOCATION);
        GL20.glVertexAttribPointer(POSITION_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, RGB_UV_STRIDE, 0);

        GL20.glEnableVertexAttribArray(TEX_NORMAL_SHADER_LOCATION);
        GL20.glVertexAttribPointer(TEX_NORMAL_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, RGB_UV_STRIDE, 12); // 3 * 4

        GL20.glEnableVertexAttribArray(TEX_COLOUR_SHADER_LOCATION);
        GL20.glVertexAttribPointer(TEX_COLOUR_SHADER_LOCATION, 4, GL11.GL_FLOAT, false, RGB_UV_STRIDE, 24);

        GL20.glEnableVertexAttribArray(UV_SHADER_LOCATION);
        GL20.glVertexAttribPointer(UV_SHADER_LOCATION, 2, GL11.GL_FLOAT, false, RGB_UV_STRIDE, 40);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        GL11.glDrawElements(GL11.GL_TRIANGLES, indices.length, GL11.GL_UNSIGNED_INT, 0);

        GL30.glBindVertexArray(0);
        GL30.glDeleteVertexArrays(vaoGeneral);
        GL15.glDeleteBuffers(vboGeneral);
        GL15.glDeleteBuffers(eboGeneral);
    }

    public static void drawTriangle_GeneralSlow(float[] vertices) {
        int vboGeneral = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboGeneral);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STREAM_DRAW);

        GL20.glEnableVertexAttribArray(POSITION_SHADER_LOCATION);
        GL20.glVertexAttribPointer(POSITION_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, 12, 0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
        GL15.glDeleteBuffers(vboGeneral);
    }

    public static void drawTriangleVAO_GeneralSlow(float[] vertices) {
        int vaoGeneral = GL30.glGenVertexArrays();
        int vboGeneral = GL15.glGenBuffers();
        GL30.glBindVertexArray(vaoGeneral);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboGeneral);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STREAM_DRAW);

        GL20.glEnableVertexAttribArray(POSITION_SHADER_LOCATION);
        GL20.glVertexAttribPointer(POSITION_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, 12, 0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);

        GL30.glBindVertexArray(0);
        GL30.glDeleteVertexArrays(vaoGeneral);
        GL15.glDeleteBuffers(vboGeneral);
    }

    public static void colourise7(int offset, int times, float r, float g, float b,
            float a, float[] vertexData, int i) {
        for (int j = 0; j < times; j++) {
            int pos = (offset + i + j) * 7;
            vertexData[pos + 3] = r;
            vertexData[pos + 4] = g;
            vertexData[pos + 5] = b;
            vertexData[pos + 6] = a;
        }
    }

    public static void pointAt7(int offset, float x, float y, float z,
            float[] vertexData, int i) {
        int pos = (offset + i) * 7;
        vertexData[pos] = x;
        vertexData[pos + 1] = y;
        vertexData[pos + 2] = z;
    }
}
