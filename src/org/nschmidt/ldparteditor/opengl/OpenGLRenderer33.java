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

import org.eclipse.swt.opengl.GLCanvas;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;

/**
 * This class draws the 3D view (OpenGL 3.3 compliant)
 *
 * @author nils
 *
 */
public class OpenGLRenderer33 extends OpenGLRenderer {

    /** The 3D Composite */
    private final Composite3D c3d;
    
    private GLShader shaderProgram = new GLShader();
    private final GLMatrixStack stack = new GLMatrixStack();
    
    public OpenGLRenderer33(Composite3D c3d) {
        this.c3d = c3d;
    }
    
    @Override
    public Composite3D getC3D() {
        return c3d;
    }

    @Override
    public void init() {
        // FIXME Needs implementation!
        
        shaderProgram = new GLShader("primitive.vert", "primitive.frag"); //$NON-NLS-1$ //$NON-NLS-2$
        stack.setShader(shaderProgram);
        shaderProgram.use();
        
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glDepthFunc(GL11.GL_LESS);
        GL11.glClearDepth(1.0f);
        GL11.glClearColor(View.background_Colour_r[0], View.background_Colour_g[0], View.background_Colour_b[0], 1.0f);

        GL11.glPointSize(5);
    }

    @Override
    public void drawScene() {
        
        final long start = System.currentTimeMillis();
        
        final boolean negDet = c3d.hasNegDeterminant();
        final boolean raytraceMode = c3d.getRenderMode() == 5;
        
        final GLCanvas canvas = c3d.getCanvas();
        
        if (!canvas.isCurrent()) {
            canvas.setCurrent();
            GL.setCapabilities(c3d.getCapabilities());
        }
        
        final Editor3DWindow window = Editor3DWindow.getWindow();

        // MARK OpenGL Draw Scene
        
        final float zoom = c3d.getZoom();
        final Matrix4f viewport_rotation = c3d.getRotation();
        final Matrix4f viewport_translation = c3d.getTranslation();
        
        Matrix4f viewport_transform = new Matrix4f();
        Matrix4f.setIdentity(viewport_transform);
        Matrix4f.scale(new Vector3f(zoom, zoom, zoom), viewport_transform, viewport_transform);
        Matrix4f.mul(viewport_rotation, viewport_transform, viewport_transform);
        Matrix4f.mul(viewport_transform, viewport_translation, viewport_transform);
        c3d.setViewport(viewport_transform);
        
        stack.clear();
        
        // FIXME Needs implementation!
        
        GL11.glColorMask(true, true, true, true);

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);

        canvas.swapBuffers();
        
        NLogger.debug(getClass(), "Frametime: " + (System.currentTimeMillis() - start)); //$NON-NLS-1$
    }
    
    @Override
    public void dispose() {
        // TODO Auto-generated method stub
        
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
