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

public enum GL33HelperPrimitives {
    INSTANCE;

    private static final int POSITION_SHADER_LOCATION = 0;
    private static final int COLOUR_SHADER_LOCATION = 1;
    private static final int RGB_STRIDE = (3 + 3) * 4;

    private static int vboTriangle = -1;
    private static int eboTriangle = -1;
    private static int vboQuad = -1;
    private static int eboQuad = -1;
    private static int vboLine = -1;

    private static int vboTriangleBackup = -1;
    private static int eboTriangleBackup = -1;
    private static int vboQuadBackup = -1;
    private static int eboQuadBackup = -1;
    private static int vboLineBackup = -1;

    static void backupVBOprimitiveArea() {
        vboTriangleBackup = vboTriangle;
        eboTriangleBackup = eboTriangle;
        vboQuadBackup = vboQuad;
        eboQuadBackup = eboQuad;
        vboLineBackup = vboLine;
    }

    static void restoreVBOprimitiveArea() {
        vboTriangle = vboTriangleBackup;
        eboTriangle = eboTriangleBackup;
        vboQuad = vboQuadBackup;
        eboQuad = eboQuadBackup;
        vboLine = vboLineBackup;
    }

    static void createVBOprimitiveArea() {
        vboTriangle = GL15.glGenBuffers();
        eboTriangle = GL15.glGenBuffers();
        vboQuad = GL15.glGenBuffers();
        eboQuad = GL15.glGenBuffers();
        vboLine = GL15.glGenBuffers();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboQuad);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, new float[48], GL15.GL_DYNAMIC_DRAW);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboQuad);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, new int[12], GL15.GL_DYNAMIC_DRAW);

        GL20.glEnableVertexAttribArray(POSITION_SHADER_LOCATION);
        GL20.glVertexAttribPointer(POSITION_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, RGB_STRIDE, 0);

        GL20.glEnableVertexAttribArray(COLOUR_SHADER_LOCATION);
        GL20.glVertexAttribPointer(COLOUR_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, RGB_STRIDE, 12); // 3 * 4

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboTriangle);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, new float[36], GL15.GL_DYNAMIC_DRAW);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboTriangle);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, new int[6], GL15.GL_DYNAMIC_DRAW);

        GL20.glEnableVertexAttribArray(POSITION_SHADER_LOCATION);
        GL20.glVertexAttribPointer(POSITION_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, RGB_STRIDE, 0);

        GL20.glEnableVertexAttribArray(COLOUR_SHADER_LOCATION);
        GL20.glVertexAttribPointer(COLOUR_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, RGB_STRIDE, 12); // 3 * 4

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboLine);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, new float[12], GL15.GL_DYNAMIC_DRAW);

        GL20.glEnableVertexAttribArray(POSITION_SHADER_LOCATION);
        GL20.glVertexAttribPointer(POSITION_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, RGB_STRIDE, 0);

        GL20.glEnableVertexAttribArray(COLOUR_SHADER_LOCATION);
        GL20.glVertexAttribPointer(COLOUR_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, RGB_STRIDE, 12); // 3 * 4

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    static void destroyVBOprimitiveArea() {
        GL15.glDeleteBuffers(vboTriangle);
        GL15.glDeleteBuffers(eboTriangle);
        GL15.glDeleteBuffers(vboQuad);
        GL15.glDeleteBuffers(eboQuad);
        GL15.glDeleteBuffers(vboLine);
    }

    public static void drawTrianglesIndexedRGBtriangle(float[] vertices, int[] indices) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboTriangle);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, vertices);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboTriangle);
        GL15.glBufferSubData(GL15.GL_ELEMENT_ARRAY_BUFFER, 0, indices);

        GL20.glEnableVertexAttribArray(POSITION_SHADER_LOCATION);
        GL20.glVertexAttribPointer(POSITION_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, RGB_STRIDE, 0);

        GL20.glEnableVertexAttribArray(COLOUR_SHADER_LOCATION);
        GL20.glVertexAttribPointer(COLOUR_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, RGB_STRIDE, 12); // 3 * 4

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        GL11.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_INT, 0);
    }

    public static void drawTrianglesIndexedRGBquad(float[] vertices, int[] indices) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboQuad);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0 , vertices);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboQuad);
        GL15.glBufferSubData(GL15.GL_ELEMENT_ARRAY_BUFFER, 0, indices);

        GL20.glEnableVertexAttribArray(POSITION_SHADER_LOCATION);
        GL20.glVertexAttribPointer(POSITION_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, RGB_STRIDE, 0);

        GL20.glEnableVertexAttribArray(COLOUR_SHADER_LOCATION);
        GL20.glVertexAttribPointer(COLOUR_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, RGB_STRIDE, 12); // 3 * 4

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        GL11.glDrawElements(GL11.GL_TRIANGLES, 12, GL11.GL_UNSIGNED_INT, 0);
    }

    public static void drawLinesRGBline(float[] vertices) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboLine);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, vertices);

        GL20.glEnableVertexAttribArray(POSITION_SHADER_LOCATION);
        GL20.glVertexAttribPointer(POSITION_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, RGB_STRIDE, 0);

        GL20.glEnableVertexAttribArray(COLOUR_SHADER_LOCATION);
        GL20.glVertexAttribPointer(COLOUR_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, RGB_STRIDE, 12); // 3 * 4

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        GL11.glDrawArrays(GL11.GL_LINES, 0, 2);
    }
}
