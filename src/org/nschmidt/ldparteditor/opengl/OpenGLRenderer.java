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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.opengl.GLCanvas;
import org.lwjgl.opengl.GL;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.data.GTexture;
import org.nschmidt.ldparteditor.logger.NLogger;

public abstract class OpenGLRenderer implements IRenderer {

    private static final AtomicBoolean smoothing = new AtomicBoolean(false);
    /** The 3D Composite */
    protected final Composite3D c3d;

    protected OpenGLRenderer(Composite3D c3d) {
        this.c3d = c3d;
    }

    /** The set, which stores already loaded textures in-memory. */
    protected Set<GTexture> textureSet = new HashSet<>();

    public static AtomicBoolean getSmoothing() {
        return smoothing;
    }

    /**
     * Registers a texture with a given ID
     *
     * @param ID
     *            The ID of the texture
     */
    public void registerTexture(GTexture tex) {
        textureSet.add(tex);
    }

    /**
     * Disposes all textures
     */
    @Override
    public void disposeAllTextures() {
        final GLCanvas canvas = c3d.getCanvas();
        if (!canvas.isCurrent()) {
            canvas.setCurrent();
            GL.setCapabilities(c3d.getCapabilities());
        }
        for (Iterator<GTexture> it = textureSet.iterator() ; it.hasNext();) {
            GTexture tex = it.next();
            NLogger.debug(getClass(), "Dispose texture: {0}", tex); //$NON-NLS-1$
            tex.dispose(this);
            it.remove();
        }
    }

    /**
     * Disposes old textures
     */
    @Override
    public synchronized void disposeOldTextures() {
        final GLCanvas canvas = c3d.getCanvas();
        if (!canvas.isCurrent()) {
            canvas.setCurrent();
            GL.setCapabilities(c3d.getCapabilities());
        }
        Iterator<GTexture> ti = textureSet.iterator();
        for (GTexture tex = null; ti.hasNext() && (tex = ti.next()) != null;) {
            if (tex.isTooOld()) {
                NLogger.debug(getClass(), "Dispose old texture: {0}", tex); //$NON-NLS-1$
                tex.dispose(this);
                ti.remove();
            }
        }
    }

    public boolean containsOnlyCubeMaps() {
        int counter = 0;
        for (GTexture tex : textureSet) {
            if (tex.getCubeMapIndex() > 0) {
                counter++;
            }
        }
        return textureSet.size() == counter;
    }
}
