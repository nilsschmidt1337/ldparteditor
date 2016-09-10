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

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.opengl.GLCanvas;
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
    private int EBO;
    
    private final int POSITION_SHADER_LOCATION = 0;
    
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
             0.5f,  0.5f, 0.0f,  // Top Right
             0.5f, -0.5f, 0.0f,  // Bottom Right
            -0.5f, -0.5f, 0.0f,  // Bottom Left
            -0.5f,  0.5f, 0.0f   // Top Left 
        };
        int[] indices = new int[]{  // Note that we start from 0!
            0, 1, 3,  // First Triangle
            1, 2, 3   // Second Triangle
        };
        
        VAO = GL30.glGenVertexArrays();
        VBO = GL15.glGenBuffers();
        EBO = GL15.glGenBuffers();
        // Bind the Vertex Array Object first, then bind and set vertex buffer(s) and attribute pointer(s).
        GL30.glBindVertexArray(VAO);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, EBO);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_STATIC_DRAW);

        GL20.glEnableVertexAttribArray(POSITION_SHADER_LOCATION);
        GL20.glVertexAttribPointer(POSITION_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, 3 * 4, 0);

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
        
        Rectangle bounds = cp.getBounds();
        GL11.glViewport(0, 0, bounds.width, bounds.height);
        
        shaderProgram.use();
        GL30.glBindVertexArray(VAO);
        // GL20.glEnableVertexAttribArray(POSITION_SHADER_LOCATION); // <-- Not necessary!
        GL11.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_INT, 0);
        // GL20.glDisableVertexAttribArray(POSITION_SHADER_LOCATION); // <-- Not necessary!
        GL30.glBindVertexArray(0);
        
        canvas.swapBuffers();
    }

    @Override
    public void dispose() {
        // Properly de-allocate all resources once they've outlived their purpose
        shaderProgram.dispose();
        
        GL30.glDeleteVertexArrays(VAO);
        GL15.glDeleteBuffers(VBO);
        GL15.glDeleteBuffers(EBO);
        
        isRendering.set(false);
    }
}
