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

import org.eclipse.swt.graphics.Rectangle;
import org.lwjgl.opengl.swt.GLCanvas;
import org.lwjgl.system.Callback;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.composite.primitive.CompositePrimitive;
import org.nschmidt.ldparteditor.data.PGData3;
import org.nschmidt.ldparteditor.data.Primitive;
import org.nschmidt.ldparteditor.enumtype.Colour;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.helper.Arrow;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.workbench.UserSettingState;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

public class OpenGLRendererPrimitives33 implements OpenGLRendererPrimitives {

    /** The Primitive Composite */
    private final CompositePrimitive cp;

    private GLShader shaderProgram = new GLShader();
    private GLShader shaderProgram2D = new GLShader();
    private Callback debugCallback = null;
    private final GLMatrixStack stack = new GLMatrixStack();
    private final GL33Helper helper = new GL33Helper();

    public OpenGLRendererPrimitives33(CompositePrimitive compositePrimitive) {
        this.cp = compositePrimitive;
    }

    @Override
    public void init() {

        if (shaderProgram.isDefault()) shaderProgram = new GLShader("primitive.vert", "primitive.frag"); //$NON-NLS-1$ //$NON-NLS-2$
        if (shaderProgram2D.isDefault()) shaderProgram2D = new GLShader("2D.vert", "2D.frag"); //$NON-NLS-1$ //$NON-NLS-2$

        if (NLogger.debugging && debugCallback == null) debugCallback = GLUtil.setupDebugMessageCallback();

        stack.setShader(shaderProgram);
        shaderProgram.use();

        GL11.glClearDepth(1.0f);
        GL11.glClearColor(Colour.primitiveBackgroundColourR, Colour.primitiveBackgroundColourG, Colour.primitiveBackgroundColourB, 1.0f);
    }

    @Override
    public void drawScene(float mouseX, float mouseY) {

        final UserSettingState userSettings = WorkbenchManager.getUserSettingState();
        final GLCanvas canvas = cp.getCanvas();

        if (canvas.isDisposed()) {
            return;
        }

        if (!canvas.isCurrent()) {
            canvas.setCurrent();
            GL.setCapabilities(cp.getCapabilities());
        }

        GL11.glColorMask(true, true, true, true);

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);

        if (cp.stopDraw()) {
            canvas.swapBuffers();
            return;
        }

        final Rectangle bounds = cp.getBounds();
        final Rectangle scaledBounds = cp.getScaledBounds();
        GL11.glViewport(0, 0, scaledBounds.width, scaledBounds.height);

        shaderProgram.use();

        final FloatBuffer viewBuf = BufferUtils.createFloatBuffer(16);

        Matrix4f viewportTransform = new Matrix4f();
        Matrix4f.setIdentity(viewportTransform);

        float zoom = cp.getZoom();
        Matrix4f.scale(new Vector3f(zoom, zoom, zoom), viewportTransform, viewportTransform);
        Matrix4f viewportTranslation = cp.getTranslation();
        Matrix4f.mul(viewportTransform, viewportTranslation, viewportTransform);
        viewportTransform.store(viewBuf);
        cp.setViewport(viewportTransform);
        viewBuf.flip();

        // Draw all visible primitives / highlight selection

        GL11.glDisable(GL11.GL_DEPTH_TEST);

        final FloatBuffer projectionBuf = BufferUtils.createFloatBuffer(16);
        final float viewport_width = bounds.width / View.PIXEL_PER_LDU;
        final float viewport_height = bounds.height / View.PIXEL_PER_LDU;
        GLMatrixStack.glOrtho(0f, viewport_width, viewport_height, 0f, -1000000f * cp.getZoom(), 1000001f * cp.getZoom()).store(projectionBuf);
        projectionBuf.position(0);
        
        int view = shaderProgram.getUniformLocation("view" ); //$NON-NLS-1$
        GL20.glUniformMatrix4fv(view, false, viewBuf);

        int projection = shaderProgram.getUniformLocation("projection" ); //$NON-NLS-1$
        GL20.glUniformMatrix4fv(projection, false, projectionBuf);
        
        stack.clear();
        GL33HelperPrimitives.createVBOprimitiveArea();
        helper.createVBO();

        GL11.glDisable(GL11.GL_DEPTH_TEST);

        float lastX;

        float x = 2f;
        float y = 2f;

        float mx = mouseX + (viewportTransform.m30 - 2f) * zoom * View.PIXEL_PER_LDU;
        float my = mouseY + (viewportTransform.m31 - 2f) * zoom * View.PIXEL_PER_LDU;

        final float STEP = 22f * zoom * View.PIXEL_PER_LDU;
        cp.setRotationWidth(STEP);
        float minY = viewportTransform.m31 - 22f;
        float maxY = viewportTransform.m31 + canvas.getBounds().height / (zoom * View.PIXEL_PER_LDU);
        boolean wasFocused = false;
        float sx = STEP;
        float width = canvas.getBounds().width;
        final Primitive sp = cp.getSelectedPrimitive();
        final Matrix4f rotation = cp.getRotation();
        final boolean hasSearchResults = !cp.getSearchResults().isEmpty();

        GL11.glLineWidth(1f);

        if (hasSearchResults) {
            if (cp.getSearchResults().get(0) != null) {
                for (Primitive p : cp.getSearchResults()) {
                    lastX = sx;
                    if (minY < y && maxY > y) {
                        float ty = y * zoom * View.PIXEL_PER_LDU;
                        boolean focused = mx > sx - STEP && mx < sx  && my > ty && my < ty + STEP;
                        if (focused) {
                            cp.setFocusedPrimitive(p);
                            wasFocused = true;
                        }
                        drawCell(x, y, p.equals(sp), p.isCategory(), focused);
                        GL11.glEnable(GL11.GL_CULL_FACE);
                        GL11.glFrontFace(GL11.GL_CW);
                        GL11.glCullFace(GL11.GL_BACK);
                        GL11.glEnable(GL11.GL_DEPTH_TEST);
                        p.drawGL33(stack, x, y, rotation);
                        GL11.glDisable(GL11.GL_DEPTH_TEST);
                        GL11.glDisable(GL11.GL_CULL_FACE);
                        if (p.isCategory()) {
                            if (p.isExtended()) {
                                drawMinus(x, y);
                            } else {
                                drawPlus(x, y);
                            }
                        }

                    }
                    sx = (sx + STEP) % width;
                    x += 22f;
                    if (lastX > sx) {
                        sx = 22f * zoom * View.PIXEL_PER_LDU;
                        x = 2f;
                        y += 22f;
                    }
                }
            }
        } else {
            for (Primitive p2 : cp.getPrimitives()) {
                for (Primitive p : p2.getPrimitives()) {
                    lastX = sx;
                    if (minY < y && maxY > y) {
                        float ty = y * zoom * View.PIXEL_PER_LDU;
                        boolean focused = mx > sx - STEP && mx < sx  && my > ty && my < ty + STEP;
                        if (focused) {
                            cp.setFocusedPrimitive(p);
                            wasFocused = true;
                        }
                        drawCell(x, y, p.equals(sp), p.isCategory(), focused);
                        GL11.glEnable(GL11.GL_CULL_FACE);
                        GL11.glFrontFace(GL11.GL_CW);
                        GL11.glCullFace(GL11.GL_BACK);
                        GL11.glEnable(GL11.GL_DEPTH_TEST);
                        p.drawGL33(stack, x, y, rotation);
                        GL11.glDisable(GL11.GL_DEPTH_TEST);
                        GL11.glDisable(GL11.GL_CULL_FACE);
                        if (p.isCategory()) {
                            if (p.isExtended()) {
                                drawMinus(x, y);
                            } else {
                                drawPlus(x, y);
                            }
                        }
                    }
                    sx = (sx + STEP) % width;
                    x += 22f;
                    if (lastX > sx) {
                        sx = 22f * zoom * View.PIXEL_PER_LDU;
                        x = 2f;
                        y += 22f;
                    }
                }
            }
        }

        stack.glLoadIdentity();

        stack.glPushMatrix();
        stack.glTranslatef(viewport_width - .05f, viewport_height - .05f, 0f);
        stack.glMultMatrixf(rotation);

        new Arrow(Colour.xAxisColourR, Colour.xAxisColourG, Colour.xAxisColourB, -.5f,0f, 0f, .00015f, .00004f, 2f).drawGL33rgb(stack, 0f, 0f, 0f, .01f);
        new Arrow(Colour.yAxisColourR, Colour.yAxisColourG, Colour.yAxisColourB, 0f, .5f,0f, .00015f, .00004f, 2f).drawGL33rgb(stack, 0f, 0f, 0f, .01f);
        new Arrow(Colour.zAxisColourR, Colour.zAxisColourG, Colour.zAxisColourB, 0f, 0f, .5f,.00015f, .00004f, 2f).drawGL33rgb(stack, 0f, 0f, 0f, .01f);

        stack.glPopMatrix();

        if (userSettings.isShowingAxisLabels()) {
            final float length = .025f;
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            
            final Vector4f xAxis = new Vector4f(-length, 0f, 0f, 1f);
            final Vector4f yAxis = new Vector4f(0f, length, 0f, 1f);
            final Vector4f zAxis = new Vector4f(0f, 0f, length, 1f);
            
            Matrix4f.transform(rotation, xAxis, xAxis);
            Matrix4f.transform(rotation, yAxis, yAxis);
            Matrix4f.transform(rotation, zAxis, zAxis);

            PGData3.beginDrawTextGL33(shaderProgram2D);
            stack.setShader(shaderProgram2D);
            
            view = shaderProgram2D.getUniformLocation("view" ); //$NON-NLS-1$
            GL20.glUniformMatrix4fv(view, false, viewBuf);
            
            projection = shaderProgram2D.getUniformLocation("projection" ); //$NON-NLS-1$
            GL20.glUniformMatrix4fv(projection, false, projectionBuf);
            
            stack.glLoadIdentity();

            stack.glPushMatrix();
            stack.glTranslatef(viewport_width - .05f, viewport_height - .05f, 0f);
            stack.glScalef(-1f, 1f, 1f);
            
            for (PGData3 tri : View.X) {
                tri.drawTextGL33(-xAxis.x, xAxis.y, 0f);
            }
            
            for (PGData3 tri : View.Y) {
                tri.drawTextGL33(-yAxis.x, yAxis.y, 0f);
            }
            
            for (PGData3 tri : View.Z) {
                tri.drawTextGL33(-zAxis.x, zAxis.y, 0f);
            }
            PGData3.endDrawTextGL33(shaderProgram);
            stack.setShader(shaderProgram);
            stack.glPopMatrix();
        }
        
        cp.setMaxY(y - 22f);
        
        if (!wasFocused && mouseX != -1) {
            cp.setFocusedPrimitive(null);
            cp.setSelectedPrimitive(null);
        }

        GL33HelperPrimitives.destroyVBOprimitiveArea();
        helper.destroyVBO();

        canvas.swapBuffers();
    }

    @Override
    public void dispose() {
        // Properly de-allocate all resources once they've outlived their purpose
        shaderProgram.dispose();
        shaderProgram2D.dispose();
        if (debugCallback != null) debugCallback.free();
    }

    private void drawCell(float x, float y, boolean selected, boolean category, boolean focused) {
        if (selected) {
            drawRoundRectangle(x, y, 20f, 20f, 5f, Colour.primitiveSelectedCellColourR, Colour.primitiveSelectedCellColourG, Colour.primitiveSelectedCellColourB);
        } else if (focused) {
            drawRoundRectangle(x, y, 20f, 20f, 5f, Colour.primitiveFocusedCellColourR, Colour.primitiveFocusedCellColourG, Colour.primitiveFocusedCellColourB);
        } else {
            drawRoundRectangle(x, y, 20f, 20f, 5f, Colour.primitiveNormalCellColourR, Colour.primitiveNormalCellColourG, Colour.primitiveNormalCellColourB);
        }
        if (category) {
            drawRoundRectangle(x + .5f, y + .5f, 19f, 19f, 5f, Colour.primitiveCategoryCell1ColourR, Colour.primitiveCategoryCell1ColourG, Colour.primitiveCategoryCell1ColourB);
            drawRoundRectangle(x + 1f, y + 1f, 18f, 18f, 5f, Colour.primitiveCategoryCell2ColourR, Colour.primitiveCategoryCell2ColourG, Colour.primitiveCategoryCell2ColourB);
        } else {
            drawRoundRectangle(x + .5f, y + .5f, 19f, 19f, 5f, Colour.primitiveCell1ColourR, Colour.primitiveCell1ColourG, Colour.primitiveCell1ColourB);
            drawRoundRectangle(x + 1f, y + 1f, 18f, 18f, 5f, Colour.primitiveCell2ColourR, Colour.primitiveCell2ColourG, Colour.primitiveCell2ColourB);
        }
    }

    private void drawPlus(float x, float y) {
        drawSignBackground(x, y);

        int[] indices = new int[]{
                0, 1, 3,
                1, 2, 3,
                4, 5, 7,
                5, 6, 7};

        float[] vertexData = new float[] {
                x + 14f, y + 15.75f, 0f,
                Colour.primitivePlusNMinusColourR, Colour.primitivePlusNMinusColourG, Colour.primitivePlusNMinusColourB,
                x + 14f, y + 15.25f, 0f,
                Colour.primitivePlusNMinusColourR, Colour.primitivePlusNMinusColourG, Colour.primitivePlusNMinusColourB,
                x + 16f, y + 15.25f, 0f,
                Colour.primitivePlusNMinusColourR, Colour.primitivePlusNMinusColourG, Colour.primitivePlusNMinusColourB,
                x + 16f, y + 15.75f, 0f,
                Colour.primitivePlusNMinusColourR, Colour.primitivePlusNMinusColourG, Colour.primitivePlusNMinusColourB,
                x + 14.75f, y + 16.4f, 0f,
                Colour.primitivePlusNMinusColourR, Colour.primitivePlusNMinusColourG, Colour.primitivePlusNMinusColourB,
                x + 14.75f, y + 14.6f, 0f,
                Colour.primitivePlusNMinusColourR, Colour.primitivePlusNMinusColourG, Colour.primitivePlusNMinusColourB,
                x + 15.25f, y + 14.6f, 0f,
                Colour.primitivePlusNMinusColourR, Colour.primitivePlusNMinusColourG, Colour.primitivePlusNMinusColourB,
                x + 15.25f, y + 16.4f, 0f,
                Colour.primitivePlusNMinusColourR, Colour.primitivePlusNMinusColourG, Colour.primitivePlusNMinusColourB};

        helper.drawTrianglesIndexedRGBgeneral(vertexData, indices);
    }

    private void drawMinus(float x, float y) {
        drawSignBackground(x, y);

        int[] indices = new int[]{
                0, 1, 3,
                1, 2, 3};

        float[] vertexData = new float[] {

                x + 14f, y + 15.75f, 0f,
                Colour.primitivePlusNMinusColourR, Colour.primitivePlusNMinusColourG, Colour.primitivePlusNMinusColourB,
                x + 14f, y + 15.25f, 0f,
                Colour.primitivePlusNMinusColourR, Colour.primitivePlusNMinusColourG, Colour.primitivePlusNMinusColourB,
                x + 16f, y + 15.25f, 0f,
                Colour.primitivePlusNMinusColourR, Colour.primitivePlusNMinusColourG, Colour.primitivePlusNMinusColourB,
                x + 16f, y + 15.75f, 0f,
                Colour.primitivePlusNMinusColourR, Colour.primitivePlusNMinusColourG, Colour.primitivePlusNMinusColourB};

        helper.drawTrianglesIndexedRGBgeneral(vertexData, indices);
    }

    private void drawSignBackground(float x, float y) {
        drawRoundRectangle(x + 13.2f, y + 13.6f, 4f, 4f, .5f, Colour.primitiveSignBgColourR, Colour.primitiveSignBgColourG, Colour.primitiveSignBgColourB);
        drawRoundRectangle(x + 13.3f, y + 13.7f, 3.6f, 3.6f, .5f, Colour.primitiveSignFgColourR, Colour.primitiveSignFgColourG, Colour.primitiveSignFgColourB);
    }
    private void drawRoundRectangle(float x, float y,float width, float height, float radius, float r, float g, float b) {

        final float widthMinusRadius = width - radius;
        final float heightMinusRadius = height - radius;

        {
            float[] vertexData = new float[] {

                    // Middle
                    x + radius, y, 0f,
                    r, g, b,
                    x + radius, y + height, 0f,
                    r, g, b,
                    x + widthMinusRadius, y + height, 0f,
                    r, g, b,
                    x + widthMinusRadius, y, 0f,
                    r, g, b,

                    // Left
                    x, y + radius, 0f,
                    r, g, b,
                    x, y + heightMinusRadius, 0f,
                    r, g, b,
                    x + radius, y + heightMinusRadius, 0f,
                    r, g, b,
                    x + radius, y + radius, 0f,
                    r, g, b,

                    // Right
                    x + width, y + radius, 0f,
                    r, g, b,
                    x + width, y + heightMinusRadius, 0f,
                    r, g, b,
                    x + widthMinusRadius, y + heightMinusRadius, 0f,
                    r, g, b,
                    x + widthMinusRadius, y + radius, 0f,
                    r, g, b,

            };

            int[] indices = new int[]{
                    0, 1, 3,
                    1, 2, 3,
                    4, 5, 7,
                    5, 6, 7,
                    8, 9, 11,
                    9, 10, 11,
            };
            helper.drawTrianglesIndexedRGBgeneral(vertexData, indices);
        }

        int[] indices = new int[27];
        for (int i = 0; i < 9; i++) {
            indices[i * 3] = 0;
            indices[i * 3 + 1] = i + 1;
            indices[i * 3 + 2] = i + 2;
        }

        // Upper-Left
        {
            int i = 6;
            float[] vertexData = new float[66];
            vertexData[0] = x + radius;
            vertexData[1] = y + radius;
            vertexData[2] = 0f;
            vertexData[3] = r;
            vertexData[4] = g;
            vertexData[5] = b;

            for(float angle = 0f; angle < 100f; angle += 10f) {
                float anglerad = (float) (Math.PI * angle / 180.0);
                float ax = (float) (Math.cos(anglerad) * radius);
                float ay = (float) (Math.sin(anglerad) * radius);

                vertexData[i] = x + radius - ax; i++;
                vertexData[i] = y + radius - ay; i++;
                vertexData[i] = 0f; i++;

                vertexData[i] = r; i++;
                vertexData[i] = g; i++;
                vertexData[i] = b; i++;
            }
            helper.drawTrianglesIndexedRGBgeneral(vertexData, indices);
        }


        // Upper-Right
        {
            int i = 6;
            float[] vertexData = new float[66];
            vertexData[0] = x + widthMinusRadius;
            vertexData[1] = y + radius;
            vertexData[2] = 0f;
            vertexData[3] = r;
            vertexData[4] = g;
            vertexData[5] = b;

            for(float angle = 0f; angle < 100f; angle += 10f) {
                float anglerad = (float) (Math.PI * angle / 180.0);
                float ax = (float) (Math.cos(anglerad) * radius);
                float ay = (float) (Math.sin(anglerad) * radius);

                vertexData[i] = x + widthMinusRadius + ax; i++;
                vertexData[i] = y + radius - ay; i++;
                vertexData[i] = 0f; i++;

                vertexData[i] = r; i++;
                vertexData[i] = g; i++;
                vertexData[i] = b; i++;
            }
            helper.drawTrianglesIndexedRGBgeneral(vertexData, indices);
        }


        // Lower-Left
        {
            int i = 6;
            float[] vertexData = new float[66];
            vertexData[0] = x + radius;
            vertexData[1] = y + heightMinusRadius;
            vertexData[2] = 0f;
            vertexData[3] = r;
            vertexData[4] = g;
            vertexData[5] = b;

            for(float angle = 0f; angle < 100f; angle += 10f) {
                float anglerad = (float) (Math.PI * angle / 180.0);
                float ax = (float) (Math.cos(anglerad) * radius);
                float ay = (float) (Math.sin(anglerad) * radius);

                vertexData[i] = x + radius - ax; i++;
                vertexData[i] = y + heightMinusRadius + ay; i++;
                vertexData[i] = 0f; i++;

                vertexData[i] = r; i++;
                vertexData[i] = g; i++;
                vertexData[i] = b; i++;
            }
            helper.drawTrianglesIndexedRGBgeneral(vertexData, indices);
        }


        // Lower-Right
        {
            int i = 6;
            float[] vertexData = new float[66];
            vertexData[0] = x + widthMinusRadius;
            vertexData[1] = y + heightMinusRadius;
            vertexData[2] = 0f;
            vertexData[3] = r;
            vertexData[4] = g;
            vertexData[5] = b;

            for(float angle = 0f; angle < 100f; angle += 10f) {
                float anglerad = (float) (Math.PI * angle / 180.0);
                float ax = (float) (Math.cos(anglerad) * radius);
                float ay = (float) (Math.sin(anglerad) * radius);

                vertexData[i] = x + widthMinusRadius + ax; i++;
                vertexData[i] = y + heightMinusRadius + ay; i++;
                vertexData[i] = 0f; i++;

                vertexData[i] = r; i++;
                vertexData[i] = g; i++;
                vertexData[i] = b; i++;
            }
            helper.drawTrianglesIndexedRGBgeneral(vertexData, indices);
        }
    }
}
