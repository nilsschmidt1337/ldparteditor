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

import org.eclipse.swt.opengl.GLCanvas;
import org.lwjgl.opengl.GL;
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
    
    @Override
    public void init() {
        
        Matrix4f.setIdentity(viewport);
        shaderProgram = new GLShader("primitive.vert", "primitive.frag"); //$NON-NLS-1$ //$NON-NLS-2$
        
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
    }

    @Override
    public void drawScene(float mouseX, float mouseY) {
        final GLCanvas canvas = cp.getCanvas();

        if (!canvas.isCurrent()) {
            canvas.setCurrent();
            GL.setCapabilities(cp.getCapabilities());
        }
        
        shaderProgram.use();
        
        canvas.swapBuffers();
    }

    @Override
    public void dispose() {
        isRendering.set(false);
    }
}
