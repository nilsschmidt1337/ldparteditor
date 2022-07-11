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

import java.nio.FloatBuffer;
import java.util.Deque;
import java.util.LinkedList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class GLMatrixStack {

    private final Deque<Matrix4f> stack = new LinkedList<>();
    private GLShader shader;
    private Matrix4f currentMatrix;

    static Matrix4f glOrtho(double l, double r, double b, double t, double n, double f) {
        Matrix4f result = new Matrix4f();
        result.m00 = (float) (2 / (r - l));
        result.m30 = (float) (- (r + l) / (r - l));
        result.m11 = (float) (2 / (t - b));
        result.m31 = (float) (- (t + b) / (t - b));
        result.m22 = (float) (- 2 / (f - n));
        result.m23 = (float) (- (f + n) / (f - n));
        result.m33 = 1f;
        return result;
    }

    public GLShader getShader() {
        return shader;
    }

    public void setShader(GLShader shader) {
        this.shader = shader;
    }

    void clear() {
        stack.clear();
        final Matrix4f id = new Matrix4f();
        Matrix4f.setIdentity(id);
        final FloatBuffer idBuf = BufferUtils.createFloatBuffer(16);
        id.store(idBuf);
        idBuf.position(0);
        currentMatrix = id;
        int model = shader.getUniformLocation("model" ); //$NON-NLS-1$
        GL20.glUniformMatrix4fv(model, false, idBuf);
    }

    public void glPushMatrix() {
        stack.push(new Matrix4f(currentMatrix));
    }

    public void glPopMatrix() {
        if (!stack.isEmpty()) {
            currentMatrix = stack.pop();

            final FloatBuffer buf = BufferUtils.createFloatBuffer(16);
            currentMatrix.store(buf);
            buf.position(0);

            int model = shader.getUniformLocation("model" ); //$NON-NLS-1$
            GL20.glUniformMatrix4fv(model, false, buf);
        }
    }

    public void glLoadIdentity() {
        final Matrix4f id = new Matrix4f();
        Matrix4f.setIdentity(id);
        final FloatBuffer idBuf = BufferUtils.createFloatBuffer(16);
        id.store(idBuf);
        idBuf.position(0);
        currentMatrix = id;

        int model = shader.getUniformLocation("model" ); //$NON-NLS-1$
        GL20.glUniformMatrix4fv(model, false, idBuf);

        int view = shader.getUniformLocation("view" ); //$NON-NLS-1$
        GL20.glUniformMatrix4fv(view, false, idBuf);
    }

    public void glLoadMatrix(Matrix4f m) {
        final FloatBuffer mBuf = BufferUtils.createFloatBuffer(16);
        m.store(mBuf);
        mBuf.position(0);
        currentMatrix = m;

        int model = shader.getUniformLocation("model" ); //$NON-NLS-1$
        GL20.glUniformMatrix4fv(model, false, mBuf);
    }

    public void glMultMatrixf(Matrix4f matrix) {

        Matrix4f.mul(currentMatrix, matrix, currentMatrix);

        final FloatBuffer buf = BufferUtils.createFloatBuffer(16);
        currentMatrix.store(buf);
        buf.position(0);

        int model = shader.getUniformLocation("model" ); //$NON-NLS-1$
        GL20.glUniformMatrix4fv(model, false, buf);
    }

    public void glTranslatef(float x, float y, float z) {

        Matrix4f.translate(new Vector3f(x, y, z), currentMatrix, currentMatrix);

        final FloatBuffer buf = BufferUtils.createFloatBuffer(16);
        currentMatrix.store(buf);
        buf.position(0);

        int model = shader.getUniformLocation("model" ); //$NON-NLS-1$
        GL20.glUniformMatrix4fv(model, false, buf);
    }

    public void glScalef(float x, float y, float z) {

        Matrix4f.scale(new Vector3f(x, y, z), currentMatrix, currentMatrix);

        final FloatBuffer buf = BufferUtils.createFloatBuffer(16);
        currentMatrix.store(buf);
        buf.position(0);

        int model = shader.getUniformLocation("model" ); //$NON-NLS-1$
        GL20.glUniformMatrix4fv(model, false, buf);
    }
}
