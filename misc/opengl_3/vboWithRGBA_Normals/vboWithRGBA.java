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
    
    public OpenGLRendererPrimitives33(CompositePrimitive compositePrimitive) {
        this.cp = compositePrimitive;
    }
    
    private GLShader shaderProgram = new GLShader();
    private int VAO;
    private int VBO;
    
    private final int POSITION_SHADER_LOCATION = 0;
    private final int NORMAL_SHADER_LOCATION = 1;
    private final int COLOUR_SHADER_LOCATION = 2;
    
    @Override
    public void init() {
        
        Matrix4f.setIdentity(viewport);
        shaderProgram = new GLShader("primitive.vert", "primitive.frag"); //$NON-NLS-1$ //$NON-NLS-2$
        shaderProgram.use();
        
        GL11.glClearDepth(1.0f);
        GL11.glClearColor(View.primitive_background_Colour_r[0], View.primitive_background_Colour_g[0], View.primitive_background_Colour_b[0], 1.0f);
        
        // Set up vertex data (and buffer(s)) and attribute pointers
        float[] vertices = new float[]{
             0.5f,  0.5f, 0.0f,  // Top Right
             0.0f,  0.0f, 1.0f,  // Normal
             1.0f, 0.0f, 0.0f, 1.0f, // Colour
             
             0.5f, -0.5f, 0.0f,  // Bottom Right
             0.0f,  0.0f, 1.0f,  // Normal
             0.0f, 1.0f, 0.0f, 1.0f, // Colour
             
            -0.5f, -0.5f, 0.0f,  // Bottom Left
            0.0f,  0.0f, 1.0f,  // Normal
            0.0f, 0.0f, 1.0f, 1.0f, // Colour
        };
        
        VAO = GL30.glGenVertexArrays();
        VBO = GL15.glGenBuffers();
        // Bind the Vertex Array Object first, then bind and set vertex buffer(s) and attribute pointer(s).
        GL30.glBindVertexArray(VAO);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW);

        GL20.glEnableVertexAttribArray(POSITION_SHADER_LOCATION);
        GL20.glVertexAttribPointer(POSITION_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, (3 + 3 + 4) * 4, 0);
        
        GL20.glEnableVertexAttribArray(NORMAL_SHADER_LOCATION);
        GL20.glVertexAttribPointer(NORMAL_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, (3 + 3 + 4) * 4, 3 * 4);
        
        GL20.glEnableVertexAttribArray(COLOUR_SHADER_LOCATION);
        GL20.glVertexAttribPointer(COLOUR_SHADER_LOCATION, 4, GL11.GL_FLOAT, false, (3 + 3 + 4) * 4, (3 + 3) * 4);

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
    }
}
