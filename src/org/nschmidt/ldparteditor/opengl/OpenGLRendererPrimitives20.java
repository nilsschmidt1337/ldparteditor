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
import org.eclipse.swt.opengl.GLCanvas;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.nschmidt.ldparteditor.composites.primitive.CompositePrimitive;
import org.nschmidt.ldparteditor.data.Primitive;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.Arrow;

public class OpenGLRendererPrimitives20 extends OpenGLRendererPrimitives {

    /** The Primitive Composite */
    private final CompositePrimitive cp;

    private final FloatBuffer rotation = BufferUtils.createFloatBuffer(16);
    /** The transformation matrix buffer of the view [NOT PUBLIC YET] */
    private final FloatBuffer viewport = BufferUtils.createFloatBuffer(16);

    public FloatBuffer getViewport() {
        return viewport;
    }

    public OpenGLRendererPrimitives20(CompositePrimitive compositePrimitive) {
        this.cp = compositePrimitive;
    }

    /**
     * Initializes the Scene and gives OpenGL-Hints
     */
    @Override
    public void init() {
        // MARK OpenGL Hints and Initialization
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glDepthFunc(GL11.GL_LESS);
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glClearDepth(1.0f);
        GL11.glClearColor(View.primitive_background_Colour_r[0], View.primitive_background_Colour_g[0], View.primitive_background_Colour_b[0], 1.0f);

        GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
        GL11.glPointSize(4);

        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glEnable(GL11.GL_NORMALIZE);

        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
    }

    /**
     * Draws the scene
     */
    @Override
    public void drawScene(float mouseX, float mouseY) {

        final GLCanvas canvas = cp.getCanvas();

        if (!canvas.isCurrent()) {
            canvas.setCurrent();
            GL.setCapabilities(cp.getCapabilities());
        }

        // MARK OpenGL Draw Scene
        GL20.glUseProgram(0);

        GL11.glColorMask(true, true, true, true);

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);

        if (cp.stopDraw()) {
            canvas.swapBuffers();
            return;
        }

        Rectangle bounds = cp.getBounds();
        Rectangle scaledBounds = cp.getScaledBounds();
        GL11.glViewport(0, 0, scaledBounds.width, scaledBounds.height);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        float viewportWidth = bounds.width / View.PIXEL_PER_LDU;
        float viewportHeight = bounds.height / View.PIXEL_PER_LDU;
        GL11.glOrtho(0f, viewportWidth, viewportHeight, 0f, -1000000f * cp.getZoom(), 1000001f * cp.getZoom());

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();

        Matrix4f viewportTransform = new Matrix4f();
        Matrix4f.setIdentity(viewportTransform);

        float zoom = cp.getZoom();
        Matrix4f.scale(new Vector3f(zoom, zoom, zoom), viewportTransform, viewportTransform);
        Matrix4f viewportTranslation = cp.getTranslation();
        Matrix4f.mul(viewportTransform, viewportTranslation, viewportTransform);
        viewportTransform.store(viewport);
        cp.setViewport(viewportTransform);
        viewport.flip();
        GL11.glLoadMatrixf(viewport);

        // Draw all visible primitives / highlight selection

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        float lastX;

        float x = 2f;
        float y = 2f;

        float mx = mouseX + (viewportTransform.m30 - 2f) * zoom * View.PIXEL_PER_LDU;
        float my = mouseY + (viewportTransform.m31 - 2f) * zoom * View.PIXEL_PER_LDU;

        final float STEP = 22f * zoom * View.PIXEL_PER_LDU;
        cp.setRotationWidth(STEP);
        final Matrix4f rotation2 = cp.getRotation();
        rotation2.store(rotation);
        rotation.position(0);
        float minY = viewportTransform.m31 - 22f;
        float maxY = viewportTransform.m31 + canvas.getBounds().height / (zoom * View.PIXEL_PER_LDU);
        boolean wasFocused = false;
        float sx = STEP;
        float width = canvas.getBounds().width;
        final Primitive sp = cp.getSelectedPrimitive();
        final boolean hasSearchResults = !cp.getSearchResults().isEmpty();

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
                        p.drawGL20(x, y, rotation);
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
                        p.drawGL20(x, y, rotation);
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

        GL11.glLoadIdentity();
        GL11.glPushMatrix();
        GL11.glTranslatef(viewportWidth - .05f, viewportHeight - .05f, 0f);
        GL11.glMultMatrixf(rotation);
        new Arrow(View.x_axis_Colour_r[0], View.x_axis_Colour_g[0], View.x_axis_Colour_b[0], -.5f,0f, 0f, .00015f, .00004f, 2f).drawGL20(0f, 0f, 0f, .01f);
        new Arrow(View.y_axis_Colour_r[0], View.y_axis_Colour_g[0], View.y_axis_Colour_b[0], 0f, .5f,0f, .00015f, .00004f, 2f).drawGL20(0f, 0f, 0f, .01f);
        new Arrow(View.z_axis_Colour_r[0], View.z_axis_Colour_g[0], View.z_axis_Colour_b[0], 0f, 0f, .5f,.00015f, .00004f, 2f).drawGL20(0f, 0f, 0f, .01f);
        GL11.glPopMatrix();

        cp.setMaxY(y - 22f);

        if (!wasFocused && mouseX != -1) {
            cp.setFocusedPrimitive(null);
            cp.setSelectedPrimitive(null);
        }
        canvas.swapBuffers();
    }


    private void drawCell(float x, float y, boolean selected, boolean category, boolean focused) {
        if (selected) {
            drawRoundRectangle(x, y, 20f, 20f, 5f, View.primitive_selectedCell_Colour_r[0], View.primitive_selectedCell_Colour_g[0], View.primitive_selectedCell_Colour_b[0]);
        } else if (focused) {
            drawRoundRectangle(x, y, 20f, 20f, 5f, View.primitive_focusedCell_Colour_r[0], View.primitive_focusedCell_Colour_g[0], View.primitive_focusedCell_Colour_b[0]);
        } else {
            drawRoundRectangle(x, y, 20f, 20f, 5f, View.primitive_normalCell_Colour_r[0], View.primitive_normalCell_Colour_g[0], View.primitive_normalCell_Colour_b[0]);
        }
        if (category) {
            drawRoundRectangle(x + .5f, y + .5f, 19f, 19f, 5f, View.primitive_categoryCell_1_Colour_r[0], View.primitive_categoryCell_1_Colour_g[0], View.primitive_categoryCell_1_Colour_b[0]);
            drawRoundRectangle(x + 1f, y + 1f, 18f, 18f, 5f, View.primitive_categoryCell_2_Colour_r[0], View.primitive_categoryCell_2_Colour_g[0], View.primitive_categoryCell_2_Colour_b[0]);
        } else {
            drawRoundRectangle(x + .5f, y + .5f, 19f, 19f, 5f, View.primitive_cell_1_Colour_r[0], View.primitive_cell_1_Colour_g[0], View.primitive_cell_1_Colour_b[0]);
            drawRoundRectangle(x + 1f, y + 1f, 18f, 18f, 5f, View.primitive_cell_2_Colour_r[0], View.primitive_cell_2_Colour_g[0], View.primitive_cell_2_Colour_b[0]);
        }
    }

    private void drawPlus(float x, float y) {
        drawSignBackground(x, y);
        GL11.glColor4f(View.primitive_plusNminus_Colour_r[0], View.primitive_plusNminus_Colour_g[0], View.primitive_plusNminus_Colour_b[0], 1f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glNormal3f(0f, 0f, 1f);
        GL11.glVertex3f(x + 14f, y + 15.75f, 0f);
        GL11.glVertex3f(x + 14f, y + 15.25f, 0f);
        GL11.glVertex3f(x + 16f, y + 15.25f, 0f);
        GL11.glVertex3f(x + 16f, y + 15.75f, 0f);
        GL11.glVertex3f(x + 14.75f, y + 16.4f, 0f);
        GL11.glVertex3f(x + 14.75f, y + 14.6f, 0f);
        GL11.glVertex3f(x + 15.25f, y + 14.6f, 0f);
        GL11.glVertex3f(x + 15.25f, y + 16.4f, 0f);
        GL11.glEnd();
    }

    private void drawMinus(float x, float y) {
        drawSignBackground(x, y);
        GL11.glColor4f(View.primitive_plusNminus_Colour_r[0], View.primitive_plusNminus_Colour_g[0], View.primitive_plusNminus_Colour_b[0], 1f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glNormal3f(0f, 0f, 1f);
        GL11.glVertex3f(x + 14f, y + 15.75f, 0f);
        GL11.glVertex3f(x + 14f, y + 15.25f, 0f);
        GL11.glVertex3f(x + 16f, y + 15.25f, 0f);
        GL11.glVertex3f(x + 16f, y + 15.75f, 0f);
        GL11.glEnd();
    }

    private void drawSignBackground(float x, float y) {
        drawRoundRectangle(x + 13.2f, y + 13.6f, 4f, 4f, .5f, View.primitive_signBG_Colour_r[0], View.primitive_signBG_Colour_g[0], View.primitive_signBG_Colour_b[0]);
        drawRoundRectangle(x + 13.3f, y + 13.7f, 3.6f, 3.6f, .5f, View.primitive_signFG_Colour_r[0], View.primitive_signFG_Colour_g[0], View.primitive_signFG_Colour_b[0]);
    }
    private void drawRoundRectangle(float x, float y,float width, float height, float radius, float r, float g, float b) {

        final float widthMinusRadius = width - radius;
        final float heightMinusRadius = height - radius;

        GL11.glColor4f(r, g, b, 1f);

        // Middle
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glNormal3f(0f, 0f, 1f);
        GL11.glVertex3f(x + radius, y, 0f);
        GL11.glVertex3f(x + radius, y + height, 0f);
        GL11.glVertex3f(x + widthMinusRadius, y + height, 0f);
        GL11.glVertex3f(x + widthMinusRadius, y, 0f);
        GL11.glEnd();

        // Left
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glNormal3f(0f, 0f, 1f);
        GL11.glVertex3f(x, y + radius, 0f);
        GL11.glVertex3f(x, y + heightMinusRadius, 0f);
        GL11.glVertex3f(x + radius, y + heightMinusRadius, 0f);
        GL11.glVertex3f(x + radius, y + radius, 0f);
        GL11.glEnd();

        // Right
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glNormal3f(0f, 0f, 1f);
        GL11.glVertex3f(x + width, y + radius, 0f);
        GL11.glVertex3f(x + width, y + heightMinusRadius, 0f);
        GL11.glVertex3f(x + widthMinusRadius, y + heightMinusRadius, 0f);
        GL11.glVertex3f(x + widthMinusRadius, y + radius, 0f);
        GL11.glEnd();

        // Upper-Left
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex3f(x + radius, y + radius, 0f);
        for(float angle = 0f; angle < 100f; angle += 10f) {
            float anglerad = (float) (Math.PI * angle / 180.0);
            float ax = (float) (Math.cos(anglerad) * radius);
            float ay = (float) (Math.sin(anglerad) * radius);
            GL11.glVertex3f(x + radius - ax, y + radius - ay, 0f);
        }
        GL11.glEnd();
        // Upper-Right
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex3f(x + widthMinusRadius, y + radius, 0f);
        for(float angle = 0f; angle < 100f; angle += 10f) {
            float anglerad = (float) (Math.PI * angle / 180.0);
            float ax = (float) (Math.cos(anglerad) * radius);
            float ay = (float) (Math.sin(anglerad) * radius);
            GL11.glVertex3f(x + widthMinusRadius + ax, y + radius - ay, 0f);
        }
        GL11.glEnd();
        // Lower-Left
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex3f(x + radius, y + heightMinusRadius, 0f);
        for(float angle = 0f; angle < 100f; angle += 10f) {
            float anglerad = (float) (Math.PI * angle / 180.0);
            float ax = (float) (Math.cos(anglerad) * radius);
            float ay = (float) (Math.sin(anglerad) * radius);
            GL11.glVertex3f(x + radius - ax, y + heightMinusRadius + ay, 0f);
        }
        GL11.glEnd();
        // Lower-Right
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex3f(x + widthMinusRadius, y + heightMinusRadius, 0f);
        for(float angle = 0f; angle < 100f; angle += 10f) {
            float anglerad = (float) (Math.PI * angle / 180.0);
            float ax = (float) (Math.cos(anglerad) * radius);
            float ay = (float) (Math.sin(anglerad) * radius);
            GL11.glVertex3f(x + widthMinusRadius + ax, y + heightMinusRadius + ay, 0f);
        }
        GL11.glEnd();
    }

    @Override
    public void dispose() {

    }
}
