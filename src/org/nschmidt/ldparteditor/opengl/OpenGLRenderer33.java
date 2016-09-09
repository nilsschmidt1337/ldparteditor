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
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.logger.NLogger;

/**
 * This class draws the 3D view (OpenGL 3.3 compliant)
 *
 * @author nils
 *
 */
public class OpenGLRenderer33 extends OpenGLRenderer {

    /** The 3D Composite */
    private final Composite3D c3d;
    
    private volatile AtomicBoolean isRendering = new AtomicBoolean(true);
    
    private volatile Matrix4f viewport = new Matrix4f();
    
    
    public OpenGLRenderer33(Composite3D c3d) {
        this.c3d = c3d;
    }
    
    @Override
    public Composite3D getC3D() {
        return c3d;
    }

    @Override
    public void init() {
        // TODO Auto-generated method stub
        
        Matrix4f.setIdentity(viewport);
        
        new Thread(new Runnable() {
            
            @Override
            public void run() {

                while (isRendering.get()) {
                    final float zoom = c3d.getZoom();
                    final Matrix4f viewport_rotation = c3d.getRotation();
                    final Matrix4f viewport_translation = c3d.getTranslation();
                    
                    Matrix4f viewport_transform = new Matrix4f();
                    Matrix4f.setIdentity(viewport_transform);
                    Matrix4f.scale(new Vector3f(zoom, zoom, zoom), viewport_transform, viewport_transform);
                    Matrix4f.mul(viewport_rotation, viewport_transform, viewport_transform);
                    Matrix4f.mul(viewport_transform, viewport_translation, viewport_transform);
                    c3d.setViewport(viewport_transform);
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
    public void drawScene() {
        
        final long start = System.currentTimeMillis();
        
        final GLCanvas canvas = c3d.getCanvas();
        
        if (!canvas.isCurrent()) {
            canvas.setCurrent();
            GL.setCapabilities(c3d.getCapabilities());
        }
        
        // FIXME Needs implementation!
        
        
        canvas.swapBuffers();
        
        NLogger.debug(getClass(), "Frametime: " + (System.currentTimeMillis() - start)); //$NON-NLS-1$
    }
    
    @Override
    public void dispose() {
        // TODO Auto-generated method stub
        
        isRendering.set(false);
    }
    
    @Override
    public void disposeOldTextures() {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void disposeAllTextures() {
        // TODO Auto-generated method stub
        
    }
}
