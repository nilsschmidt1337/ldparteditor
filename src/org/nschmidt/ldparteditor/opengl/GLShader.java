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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.nschmidt.ldparteditor.logger.NLogger;

public class GLShader {

    private final int program;
    final private HashMap<String, Integer> uniformMap = new HashMap<>();

    public GLShader() {
        program = 0;
    }

    public GLShader(final String vertexPath, final String fragmentPath) {
        final int vertex = createAndCompile(vertexPath, GL20.GL_VERTEX_SHADER);
        final int fragment = createAndCompile(fragmentPath, GL20.GL_FRAGMENT_SHADER);

        program = GL20.glCreateProgram();
        GL20.glAttachShader(program, fragment);
        GL20.glAttachShader(program, vertex);

        GL20.glLinkProgram(program);

        // FIXME Extract parameter locations
        // int baseImageLoc = GL20.glGetUniformLocation(program, "colorMap"); //$NON-NLS-1$

        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            NLogger.error(GLShader.class, "Could not link shader: " + GL20.glGetProgramInfoLog(program, 1024)); //$NON-NLS-1$;
        }

        GL20.glDetachShader(program, fragment);
        GL20.glDetachShader(program, vertex);
        GL20.glDeleteShader(fragment);
        GL20.glDeleteShader(vertex);
    }

    public void use() {
        GL20.glUseProgram(program);
    }

    private int createAndCompile(final String path, final int type) {
        final StringBuilder shaderSource = new StringBuilder();

        try (BufferedReader shaderReader = new BufferedReader(new InputStreamReader(GLShader.class.getResourceAsStream(path), "UTF-8"))) { //$NON-NLS-1$) {
            String line;
            while ((line = shaderReader.readLine()) != null) {
                shaderSource.append(line).append("\n"); //$NON-NLS-1$
            }
        } catch (IOException io) {
            NLogger.error(GLShader.class, io);
            return -1;
        }

        final int shaderID = GL20.glCreateShader(type);
        GL20.glShaderSource(shaderID, shaderSource);
        GL20.glCompileShader(shaderID);

        if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            NLogger.error(GLShader.class, "Could not compile shader " + path + GL20.glGetProgramInfoLog(program, 1024)); //$NON-NLS-1$;
            NLogger.error(GLShader.class, "msg:  " + path + GL20.glGetShaderInfoLog(shaderID)); //$NON-NLS-1$;
            return -1;
        }

        return shaderID;
    }

    void dispose() {
        GL20.glUseProgram(0);
        GL20.glDeleteProgram(program);
        uniformMap.clear();
    }

    public int getUniformLocation(String uniformName) {
        int location = uniformMap.getOrDefault(uniformName, -1);
        if (location == -1) {
            location = GL20.glGetUniformLocation(program, uniformName);
        }
        if (location != -1) {
            uniformMap.put(uniformName, location);
        } else {
            NLogger.error(GLShader.class, "Could not find uniform variable: " + uniformName + "\n" + GL20.glGetProgramInfoLog(program, 1024)); //$NON-NLS-1$ //$NON-NLS-2$;
        }
        return location;
    }

    public void lightsOn() {
        GL20.glUniform1f(getUniformLocation("lightswitch"), 1f); //$NON-NLS-1$
    }

    public void lightsOff() {
        GL20.glUniform1f(getUniformLocation("lightswitch"), 0f); //$NON-NLS-1$
    }

    public void texmapOn() {
        GL20.glUniform1f(getUniformLocation("texmapswitch"), 1f); //$NON-NLS-1$
    }

    public void texmapOff() {
        GL20.glUniform1f(getUniformLocation("texmapswitch"), 0f); //$NON-NLS-1$
    }

    public void transparentOn() {
        GL20.glUniform1f(getUniformLocation("alphaswitch"), 1f); //$NON-NLS-1$
    }

    public void transparentOff() {
        GL20.glUniform1f(getUniformLocation("alphaswitch"), 0f); //$NON-NLS-1$
    }

    public boolean isLightOn() {
        return GL20.glGetUniformf(program, getUniformLocation("lightswitch")) == 1f; //$NON-NLS-1$
    }

    public void setFactor(float f) {
        GL20.glUniform1f(getUniformLocation("factor"), f); //$NON-NLS-1$
    }

    public boolean isDefault() {
        return program == 0;
    }

    public void pngModeOn() {
        GL20.glUniform1f(getUniformLocation("pngswitch"), 1f); //$NON-NLS-1$
    }

    public void pngModeOff() {
        GL20.glUniform1f(getUniformLocation("pngswitch"), 0f); //$NON-NLS-1$
    }
}
