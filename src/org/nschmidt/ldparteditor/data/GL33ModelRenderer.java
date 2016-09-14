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

import java.util.HashSet;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.util.vector.Vector3f;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.data.colour.GCChrome;
import org.nschmidt.ldparteditor.data.colour.GCMatteMetal;
import org.nschmidt.ldparteditor.data.colour.GCMetal;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.composite3d.ViewIdleManager;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.opengl.GLMatrixStack;
import org.nschmidt.ldparteditor.opengl.GLShader;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;

/**
 * New OpenGL 3.3 high performance render function for the model (VAO accelerated)
 * @author nils
 *
 */
public class GL33ModelRenderer {

    private VertexManager vm = null;
    private ThreadsafeHashMap<GData3, Vertex[]> triangles;
    private ThreadsafeHashMap<GData4, Vertex[]> quads;
    
    private HashSet<Integer> vaoPool_1 = new HashSet<>();
    private HashSet<Integer> vaoPool_2 = new HashSet<>();    
    private HashSet<Integer> bufPool_1 = new HashSet<>();
    private HashSet<Integer> bufPool_2 = new HashSet<>();
    
    private static final GTexture CUBEMAP_TEXTURE = new GTexture(TexType.PLANAR, "cmap.png", null, 1, new Vector3f(1,0,0), new Vector3f(1,1,0), new Vector3f(1,1,1), 0, 0); //$NON-NLS-1$
    private static final GDataTEX CUBEMAP = new GDataTEX(null, "", TexMeta.NEXT, CUBEMAP_TEXTURE); //$NON-NLS-1$

    private static final GTexture CUBEMAP_MATTE_TEXTURE = new GTexture(TexType.PLANAR, "matte_metal.png", null, 2, new Vector3f(1,0,0), new Vector3f(1,1,0), new Vector3f(1,1,1), 0, 0); //$NON-NLS-1$
    private static final GDataTEX CUBEMAP_MATTE = new GDataTEX(null, "", TexMeta.NEXT, CUBEMAP_MATTE_TEXTURE); //$NON-NLS-1$

    private static final GTexture CUBEMAP_METAL_TEXTURE = new GTexture(TexType.PLANAR, "metal.png", null, 2, new Vector3f(1,0,0), new Vector3f(1,1,0), new Vector3f(1,1,1), 0, 0); //$NON-NLS-1$
    private static final GDataTEX CUBEMAP_METAL = new GDataTEX(null, "", TexMeta.NEXT, CUBEMAP_METAL_TEXTURE); //$NON-NLS-1$
    
    // FIXME Renderer needs implementation!
    public void draw(GLMatrixStack stack, GLShader shaderProgram, boolean drawSolidMaterials, DatFile df, Composite3D c3d) {
        if (vm != df.getVertexManager()) {
            vm = df.getVertexManager();
            triangles = vm.triangles;
            quads = vm.quads;
        }
        GDataCSG.resetCSG(df, c3d.getManipulator().isModified());

        GData data2draw = df.getDrawChainStart();
        int renderMode = c3d.getRenderMode();

        if (Editor3DWindow.getWindow().isAddingCondlines())
            renderMode = 6;
        switch (renderMode) {
        case -1: // Wireframe
            break;
        case 0: // No BFC
            data2draw.drawGL33(c3d, stack);
            while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                data2draw.drawGL33(c3d, stack);
            }
            break;
        case 1: // Random Colours
            data2draw.drawGL33_RandomColours(c3d, stack);
            while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                data2draw.drawGL33_RandomColours(c3d, stack);
            }
            break;
        case 2: // Front-Backface BFC
            data2draw.drawGL33_BFC(c3d, stack);
            while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                switch (GData.accumClip) {
                case 0:
                    data2draw.drawGL33_BFC(c3d, stack);
                    break;
                default:
                    data2draw.drawGL33(c3d, stack);
                    break;
                }
            }
            break;
        case 3: // Backface only BFC
            data2draw.drawGL33_BFC_backOnly(c3d, stack);
            while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                switch (GData.accumClip) {
                case 0:
                    data2draw.drawGL33_BFC_backOnly(c3d, stack);
                    break;
                default:
                    data2draw.drawGL33(c3d, stack);
                    break;
                }
            }
            break;
        case 4: // Real BFC
            data2draw.drawGL33_BFC_Colour(c3d, stack);
            while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                switch (GData.accumClip) {
                case 0:
                    data2draw.drawGL33_BFC_Colour(c3d, stack);
                    break;
                default:
                    data2draw.drawGL33(c3d, stack);
                    break;
                }
            }
            break;
        case 5: // FIXME Real BFC with texture mapping
            if (vm != null) {
                break;
            }
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            data2draw.drawGL33_BFC_Textured(c3d, stack);
            GDataInit.resetBfcState();
            data2draw.drawGL33_BFC_Textured(c3d, stack);
            CUBEMAP.drawGL33_BFC_Textured(c3d, stack);
            new GData3(new Vertex(0,0,0), new Vertex(1,0,0), new Vertex(1,1,0), View.DUMMY_REFERENCE, new GColour(0, 0, 0, 0, 0, new GCChrome()), true).drawGL33_BFC_Textured(c3d.getComposite3D(), stack);
            CUBEMAP_MATTE.drawGL33_BFC_Textured(c3d, stack);
            new GData3(new Vertex(0,0,0), new Vertex(1,0,0), new Vertex(1,1,0), View.DUMMY_REFERENCE, new GColour(0, 0, 0, 0, 0, new GCMatteMetal()), true).drawGL33_BFC_Textured(c3d.getComposite3D(), stack);
            CUBEMAP_METAL.drawGL33_BFC_Textured(c3d, stack);
            new GData3(new Vertex(0,0,0), new Vertex(1,0,0), new Vertex(1,1,0), View.DUMMY_REFERENCE, new GColour(0, 0, 0, 0, 0, new GCMetal()), true).drawGL33_BFC_Textured(c3d.getComposite3D(), stack);
            while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                data2draw.drawGL33_BFC_Textured(c3d, stack);
            }
            // vertices.clearVertexNormalCache();
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + 0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + 2);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + 4);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + 8);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + 16);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            break;
        case 6: // Special mode for "Add condlines"
            data2draw.drawGL33_WhileAddCondlines(c3d, stack);
            while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                data2draw.drawGL33_WhileAddCondlines(c3d, stack);
            }
            break;
        default:
            break;
        }

        GDataCSG.finishCacheCleanup(c3d.getLockableDatFileReference());

        if (c3d.isDrawingSolidMaterials() && renderMode != 5)
            vm.showHidden();
    }
}
