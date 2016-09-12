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
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.opengl.GLCanvas;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.nschmidt.ldparteditor.composites.primitive.CompositePrimitive;
import org.nschmidt.ldparteditor.enums.View;

public class OpenGLRendererPrimitives33 extends OpenGLRendererPrimitives {

    /** The Primitive Composite */
    private final CompositePrimitive cp;
    
    private volatile AtomicBoolean isRendering = new AtomicBoolean(true);
    
    private volatile Matrix4f viewport = new Matrix4f();
    
    public OpenGLRendererPrimitives33(CompositePrimitive compositePrimitive) {
        this.cp = compositePrimitive;
    }
    
    private GLShader shaderProgram = new GLShader();
    private int VAO;
    private int VBO;
    
    private final int POSITION_SHADER_LOCATION = 0;
    private final int COLOUR_SHADER_LOCATION = 1;
    
    @Override
    public void init() {
        
        Matrix4f.setIdentity(viewport);
        shaderProgram = new GLShader("primitive.vert", "primitive.frag"); //$NON-NLS-1$ //$NON-NLS-2$
        shaderProgram.use();
        
        GL11.glClearDepth(1.0f);
        GL11.glClearColor(View.primitive_background_Colour_r[0], View.primitive_background_Colour_g[0], View.primitive_background_Colour_b[0], 1.0f);
        
        new Thread(new Runnable() {
            
            @Override
            public void run() {

                while (isRendering.get()) {
                    
                    final float zoom = cp.getZoom();
                    final Matrix4f viewport_translation = cp.getTranslation();
                    final float STEP = 22f * zoom * View.PIXEL_PER_LDU;
                    cp.setRotationWidth(STEP);
                    
                    Matrix4f viewport_transform = new Matrix4f();
                    Matrix4f.setIdentity(viewport_transform);
                    Matrix4f.scale(new Vector3f(zoom, zoom, zoom), viewport_transform, viewport_transform);
                    Matrix4f.mul(viewport_transform, viewport_translation, viewport_transform);
                    cp.setViewport(viewport_transform);
                    viewport = viewport_transform;
                    
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        
        
        // Set up vertex data (and buffer(s)) and attribute pointers
        float[] vertices = new float[]{
                0.1f,  0.1f, 0.0f,  // Top Right
                1.0f, 0.0f, 0.0f, // Colour
                
                0.1f, -0.1f, 0.0f,  // Bottom Right
                0.0f, 1.0f, 0.0f, // Colour
                
               -0.1f, -0.1f, 0.0f,  // Bottom Left
               0.0f, 0.0f, 1.0f, // Colour
           };
        
        VAO = GL30.glGenVertexArrays();
        VBO = GL15.glGenBuffers();
        // Bind the Vertex Array Object first, then bind and set vertex buffer(s) and attribute pointer(s).
        GL30.glBindVertexArray(VAO);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW);

        GL20.glEnableVertexAttribArray(POSITION_SHADER_LOCATION);
        GL20.glVertexAttribPointer(POSITION_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, (3 + 3) * 4, 0);
        
        GL20.glEnableVertexAttribArray(COLOUR_SHADER_LOCATION);
        GL20.glVertexAttribPointer(COLOUR_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, (3 + 3) * 4, 3 * 4);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); // Note that this is allowed, the call to glVertexAttribPointer registered VBO as the currently bound vertex buffer object so afterwards we can safely unbind

        GL30.glBindVertexArray(0); // Unbind VAO (it's always a good thing to unbind any buffer/array to prevent strange bugs), remember: do NOT unbind the EBO, keep it bound to this VAO

    }

    @Override
    public void drawScene(float mouseX, float mouseY) {
        final GLCanvas canvas = cp.getCanvas();

        if (!canvas.isCurrent()) {
            canvas.setCurrent();
            GL.setCapabilities(cp.getCapabilities());
        }
        
        GL11.glColorMask(true, true, true, true);

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
        
        final Rectangle bounds = cp.getBounds();
        GL11.glViewport(0, 0, bounds.width, bounds.height);
        
        shaderProgram.use();
        
        Matrix4f tmp = new Matrix4f();
        Matrix4f.setIdentity(tmp);
        tmp = tmp.scale(new Vector3f(2f, 2f, 2f));
        
        final FloatBuffer view_buf = BufferUtils.createFloatBuffer(16);
        viewport.store(view_buf);
        view_buf.position(0);
        
        final Matrix4f ID = new Matrix4f();
        Matrix4f.setIdentity(ID);
        
        final FloatBuffer ID_buf = BufferUtils.createFloatBuffer(16);
        ID.store(ID_buf);
        ID_buf.position(0);
                
        final FloatBuffer projection_buf = BufferUtils.createFloatBuffer(16);        
        final float viewport_width = bounds.width / View.PIXEL_PER_LDU;
        final float viewport_height = bounds.height / View.PIXEL_PER_LDU;
        GLMatrixStack.glOrtho(0f, viewport_width, viewport_height, 0f, -1000000f * cp.getZoom(), 1000001f * cp.getZoom()).store(projection_buf);
        projection_buf.position(0);        
                
        int model = shaderProgram.getUniformLocation("model" ); //$NON-NLS-1$
        GL20.glUniformMatrix4fv(model, false, ID_buf);

        int view = shaderProgram.getUniformLocation("view" ); //$NON-NLS-1$
        GL20.glUniformMatrix4fv(view, true, view_buf);

        int projection = shaderProgram.getUniformLocation("projection" ); //$NON-NLS-1$
        GL20.glUniformMatrix4fv(projection, false, projection_buf);
        
        GL30.glBindVertexArray(VAO);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
        GL30.glBindVertexArray(0);
        
        canvas.swapBuffers();
    }

    @Override
    public void dispose() {
        // Properly de-allocate all resources once they've outlived their purpose
        shaderProgram.dispose();
        
        GL30.glDeleteVertexArrays(VAO);
        GL15.glDeleteBuffers(VBO);
        
        isRendering.set(false);
    }
}
