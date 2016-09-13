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
package org.nschmidt.ldparteditor.data;

import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.helpers.composite3d.ViewIdleManager;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.opengl.GLMatrixStack;
import org.nschmidt.ldparteditor.opengl.GLShader;

/**
 * New OpenGL 3.3 high performance render function for the model (VAO accelerated)
 * @author nils
 *
 */
public class GL33ModelRenderer {

    private VertexManager vm = null;
    private ThreadsafeHashMap<GData3, Vertex[]> triangles;
    private ThreadsafeHashMap<GData4, Vertex[]> quads;
    
    // FIXME Renderer needs implementation!
    public void draw(GLMatrixStack stack, GLShader shaderProgram, boolean drawSolidMaterials, DatFile df, Composite3D c3d) {
        if (vm != df.getVertexManager()) {
            vm = df.getVertexManager();
            triangles = vm.triangles;
            quads = vm.quads;
        }
        GDataCSG.resetCSG(df, c3d.getManipulator().isModified());
        GData data2draw = df.getDrawChainStart();
        final int renderMode = c3d.getRenderMode();
        // data2draw.drawGL33(c3d);
        while ((data2draw = data2draw.next) != null && !ViewIdleManager.pause[0].get()) {                        
            // data2draw.drawGL33(c3d);
        }
        GDataCSG.finishCacheCleanup(c3d.getLockableDatFileReference());
        if (c3d.isDrawingSolidMaterials() && renderMode != 5)
            vm.showHidden();
    }
}
