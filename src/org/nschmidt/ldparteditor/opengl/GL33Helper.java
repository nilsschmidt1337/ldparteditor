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

    public static void drawTrianglesIndexedRGB(float[] vertices, int[] indices) {
        final int VAO = GL30.glGenVertexArrays();
        final int VBO = GL15.glGenBuffers();
        final int EBO = GL15.glGenBuffers();
        
        // Bind the Vertex Array Object first, then bind and set vertex buffer(s) and attribute pointer(s).
        GL30.glBindVertexArray(VAO);
   
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STREAM_DRAW);
        
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, EBO);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_STREAM_DRAW);
   
        GL20.glEnableVertexAttribArray(POSITION_SHADER_LOCATION);
        GL20.glVertexAttribPointer(POSITION_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, (3 + 3) * 4, 0);
        
        GL20.glEnableVertexAttribArray(COLOUR_SHADER_LOCATION);
        GL20.glVertexAttribPointer(COLOUR_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, (3 + 3) * 4, 3 * 4);
   
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        
        GL11.glDrawElements(GL11.GL_TRIANGLES, indices.length, GL11.GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);
      
        GL30.glDeleteVertexArrays(VAO);
        GL15.glDeleteBuffers(VBO);
        GL15.glDeleteBuffers(EBO);
    }
    
    public static void drawLinesRGB(float[] vertices) {
        final int VAO = GL30.glGenVertexArrays();
        final int VBO = GL15.glGenBuffers();
        
        // Bind the Vertex Array Object first, then bind and set vertex buffer(s) and attribute pointer(s).
        GL30.glBindVertexArray(VAO);
   
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STREAM_DRAW);
        
        GL20.glEnableVertexAttribArray(POSITION_SHADER_LOCATION);
        GL20.glVertexAttribPointer(POSITION_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, (3 + 3) * 4, 0);
        
        GL20.glEnableVertexAttribArray(COLOUR_SHADER_LOCATION);
        GL20.glVertexAttribPointer(COLOUR_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, (3 + 3) * 4, 3 * 4);
   
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        
        GL11.glDrawArrays(GL11.GL_LINES, 0, vertices.length);
        GL30.glBindVertexArray(0);
      
        GL30.glDeleteVertexArrays(VAO);
        GL15.glDeleteBuffers(VBO);
    }
    
    public static void drawLinesRGBA(float[] vertices) {
        final int VAO = GL30.glGenVertexArrays();
        final int VBO = GL15.glGenBuffers();
        
        // Bind the Vertex Array Object first, then bind and set vertex buffer(s) and attribute pointer(s).
        GL30.glBindVertexArray(VAO);
   
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STREAM_DRAW);
        
        GL20.glEnableVertexAttribArray(POSITION_SHADER_LOCATION);
        GL20.glVertexAttribPointer(POSITION_SHADER_LOCATION, 3, GL11.GL_FLOAT, false, (3 + 4) * 4, 0);
        
        GL20.glEnableVertexAttribArray(COLOUR_SHADER_LOCATION);
        GL20.glVertexAttribPointer(COLOUR_SHADER_LOCATION, 4, GL11.GL_FLOAT, false, (3 + 4) * 4, 3 * 4);
   
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        
        GL11.glDrawArrays(GL11.GL_LINES, 0, vertices.length);
        GL30.glBindVertexArray(0);
      
        GL30.glDeleteVertexArrays(VAO);
        GL15.glDeleteBuffers(VBO);
    }
}
