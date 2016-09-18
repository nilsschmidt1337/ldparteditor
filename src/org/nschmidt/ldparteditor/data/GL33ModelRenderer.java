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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector3f;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.data.colour.GCChrome;
import org.nschmidt.ldparteditor.data.colour.GCMatteMetal;
import org.nschmidt.ldparteditor.data.colour.GCMetal;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.composite3d.ViewIdleManager;
import org.nschmidt.ldparteditor.opengl.GLMatrixStack;
import org.nschmidt.ldparteditor.opengl.GLShader;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;

/**
 * New OpenGL 3.3 high performance render function for the model (VAO accelerated)
 * @author nils
 *
 */
public class GL33ModelRenderer {

    boolean isPaused = false;
    
    private HashMap<String, Integer[]> mapGLO = new HashMap<>();
    private HashSet<Integer> sourceVAO = new HashSet<>();
    private HashSet<Integer> targetVAO = new HashSet<>();    
    private HashSet<Integer> sourceBUF = new HashSet<>();
    private HashSet<Integer> targetBUF = new HashSet<>();
    private HashSet<Integer> swapPool = null;
    private HashSet<String> swapPool2 = null;
    private HashSet<String> sourceID = new HashSet<>();
    private HashSet<String> targetID = new HashSet<>();
    private final Composite3D c3d;
    
    private static final GTexture CUBEMAP_TEXTURE = new GTexture(TexType.PLANAR, "cmap.png", null, 1, new Vector3f(1,0,0), new Vector3f(1,1,0), new Vector3f(1,1,1), 0, 0); //$NON-NLS-1$
    private static final GDataTEX CUBEMAP = new GDataTEX(null, "", TexMeta.NEXT, CUBEMAP_TEXTURE); //$NON-NLS-1$

    private static final GTexture CUBEMAP_MATTE_TEXTURE = new GTexture(TexType.PLANAR, "matte_metal.png", null, 2, new Vector3f(1,0,0), new Vector3f(1,1,0), new Vector3f(1,1,1), 0, 0); //$NON-NLS-1$
    private static final GDataTEX CUBEMAP_MATTE = new GDataTEX(null, "", TexMeta.NEXT, CUBEMAP_MATTE_TEXTURE); //$NON-NLS-1$

    private static final GTexture CUBEMAP_METAL_TEXTURE = new GTexture(TexType.PLANAR, "metal.png", null, 2, new Vector3f(1,0,0), new Vector3f(1,1,0), new Vector3f(1,1,1), 0, 0); //$NON-NLS-1$
    private static final GDataTEX CUBEMAP_METAL = new GDataTEX(null, "", TexMeta.NEXT, CUBEMAP_METAL_TEXTURE); //$NON-NLS-1$
    
    public GL33ModelRenderer(Composite3D c3d) {
        this.c3d = c3d;
    }

    // FIXME Renderer needs implementation!
    public void draw(GLMatrixStack stack, GLShader shaderProgram, boolean drawSolidMaterials, DatFile df) {

        GDataCSG.resetCSG(df, c3d.getManipulator().isModified());

        if (drawSolidMaterials) {
            swapPool = sourceVAO;
            sourceVAO = targetVAO;
            targetVAO = swapPool;
            
            swapPool = sourceBUF;
            sourceBUF = targetBUF;
            targetBUF = swapPool;
            
            swapPool2 = sourceID;
            sourceID = targetID;
            targetID = swapPool2;
        }
        
        GData data2draw = df.getDrawChainStart();
        int renderMode = c3d.getRenderMode();

        if (Editor3DWindow.getWindow().isAddingCondlines())
            renderMode = 6;
        switch (renderMode) {
        case -1: // Wireframe
            break;
        case 0: // No BFC
            data2draw.drawGL33(c3d, stack, drawSolidMaterials, sourceVAO, targetVAO, sourceBUF, targetBUF, sourceID, targetID, mapGLO);
            while ((data2draw = data2draw.getNext()) != null && !isPaused) {
                isPaused = ViewIdleManager.pause[0].get();
                data2draw.drawGL33(c3d, stack, drawSolidMaterials, sourceVAO, targetVAO, sourceBUF, targetBUF, sourceID, targetID, mapGLO);
                
            }
            break;
        case 1: // Random Colours
            data2draw.drawGL33_RandomColours(c3d, stack, drawSolidMaterials, sourceVAO, targetVAO, sourceBUF, targetBUF, sourceID, targetID, mapGLO);
            while ((data2draw = data2draw.getNext()) != null && !isPaused) {
                isPaused = ViewIdleManager.pause[0].get();
                data2draw.drawGL33_RandomColours(c3d, stack, drawSolidMaterials, sourceVAO, targetVAO, sourceBUF, targetBUF, sourceID, targetID, mapGLO);
            }
            break;
        case 2: // Front-Backface BFC
            data2draw.drawGL33_BFC(c3d, stack, drawSolidMaterials, sourceVAO, targetVAO, sourceBUF, targetBUF, sourceID, targetID, mapGLO);
            while ((data2draw = data2draw.getNext()) != null && !isPaused) {
                isPaused = ViewIdleManager.pause[0].get();
                switch (GData.accumClip) {
                case 0:
                    data2draw.drawGL33_BFC(c3d, stack, drawSolidMaterials, sourceVAO, targetVAO, sourceBUF, targetBUF, sourceID, targetID, mapGLO);
                    break;
                default:
                    data2draw.drawGL33(c3d, stack, drawSolidMaterials, sourceVAO, targetVAO, sourceBUF, targetBUF, sourceID, targetID, mapGLO);
                    break;
                }
            }
            break;
        case 3: // Backface only BFC
            data2draw.drawGL33_BFC_backOnly(c3d, stack, drawSolidMaterials, sourceVAO, targetVAO, sourceBUF, targetBUF, sourceID, targetID, mapGLO);
            while ((data2draw = data2draw.getNext()) != null && !isPaused) {
                isPaused = ViewIdleManager.pause[0].get();
                switch (GData.accumClip) {
                case 0:
                    data2draw.drawGL33_BFC_backOnly(c3d, stack, drawSolidMaterials, sourceVAO, targetVAO, sourceBUF, targetBUF, sourceID, targetID, mapGLO);
                    break;
                default:
                    data2draw.drawGL33(c3d, stack, drawSolidMaterials, sourceVAO, targetVAO, sourceBUF, targetBUF, sourceID, targetID, mapGLO);
                    break;
                }
            }
            break;
        case 4: // Real BFC
            data2draw.drawGL33_BFC_Colour(c3d, stack, drawSolidMaterials, sourceVAO, targetVAO, sourceBUF, targetBUF, sourceID, targetID, mapGLO);
            while ((data2draw = data2draw.getNext()) != null && !isPaused) {
                isPaused = ViewIdleManager.pause[0].get();
                switch (GData.accumClip) {
                case 0:
                    data2draw.drawGL33_BFC_Colour(c3d, stack, drawSolidMaterials, sourceVAO, targetVAO, sourceBUF, targetBUF, sourceID, targetID, mapGLO);
                    break;
                default:
                    data2draw.drawGL33(c3d, stack, drawSolidMaterials, sourceVAO, targetVAO, sourceBUF, targetBUF, sourceID, targetID, mapGLO);
                    break;
                }
            }
            break;
        case 5: // FIXME Real BFC with texture mapping
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            data2draw.drawGL33_BFC_Textured(c3d, stack, drawSolidMaterials, sourceVAO, targetVAO, sourceBUF, targetBUF, sourceID, targetID, mapGLO);
            GDataInit.resetBfcState();
            data2draw.drawGL33_BFC_Textured(c3d, stack, drawSolidMaterials, sourceVAO, targetVAO, sourceBUF, targetBUF, sourceID, targetID, mapGLO);
            CUBEMAP.drawGL33_BFC_Textured(c3d, stack, drawSolidMaterials, sourceVAO, targetVAO, sourceBUF, targetBUF, sourceID, targetID, mapGLO);
            new GData3(new Vertex(0,0,0), new Vertex(1,0,0), new Vertex(1,1,0), View.DUMMY_REFERENCE, new GColour(0, 0, 0, 0, 0, new GCChrome()), true).drawGL33_BFC_Textured(c3d.getComposite3D(), stack, drawSolidMaterials, sourceVAO, targetVAO, sourceBUF, targetBUF, sourceID, targetID, mapGLO);
            CUBEMAP_MATTE.drawGL33_BFC_Textured(c3d, stack, drawSolidMaterials, sourceVAO, targetVAO, sourceBUF, targetBUF, sourceID, targetID, mapGLO);
            new GData3(new Vertex(0,0,0), new Vertex(1,0,0), new Vertex(1,1,0), View.DUMMY_REFERENCE, new GColour(0, 0, 0, 0, 0, new GCMatteMetal()), true).drawGL33_BFC_Textured(c3d.getComposite3D(), stack, drawSolidMaterials, sourceVAO, targetVAO, sourceBUF, targetBUF, sourceID, targetID, mapGLO);
            CUBEMAP_METAL.drawGL33_BFC_Textured(c3d, stack, drawSolidMaterials, sourceVAO, targetVAO, sourceBUF, targetBUF, sourceID, targetID, mapGLO);
            new GData3(new Vertex(0,0,0), new Vertex(1,0,0), new Vertex(1,1,0), View.DUMMY_REFERENCE, new GColour(0, 0, 0, 0, 0, new GCMetal()), true).drawGL33_BFC_Textured(c3d.getComposite3D(), stack, drawSolidMaterials, sourceVAO, targetVAO, sourceBUF, targetBUF, sourceID, targetID, mapGLO);
            while ((data2draw = data2draw.getNext()) != null && !isPaused) {
                isPaused = ViewIdleManager.pause[0].get();
                data2draw.drawGL33_BFC_Textured(c3d, stack, drawSolidMaterials, sourceVAO, targetVAO, sourceBUF, targetBUF, sourceID, targetID, mapGLO);
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
            data2draw.drawGL33_WhileAddCondlines(c3d, stack, drawSolidMaterials, sourceVAO, targetVAO, sourceBUF, targetBUF, sourceID, targetID, mapGLO);
            while ((data2draw = data2draw.getNext()) != null && !isPaused) {
                isPaused = ViewIdleManager.pause[0].get();
                data2draw.drawGL33_WhileAddCondlines(c3d, stack, drawSolidMaterials, sourceVAO, targetVAO, sourceBUF, targetBUF, sourceID, targetID, mapGLO);
            }
            break;
        default:
            break;
        }
        
        if (!drawSolidMaterials) {
            if (isPaused) {
                targetID.addAll(sourceID);
                targetVAO.addAll(sourceVAO);
                targetBUF.addAll(sourceBUF);
            } else {
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
                GL30.glBindVertexArray(0);
                for (Integer id : sourceVAO) {
                    GL30.glDeleteVertexArrays(id);
                }
                for (Integer id : sourceBUF) {
                    GL15.glDeleteBuffers(id);
                }
                for (String id : sourceID) {
                    mapGLO.remove(id);
                }
            }
            sourceID.clear();
            sourceVAO.clear();
            sourceBUF.clear();
            isPaused = false;
        }

        GDataCSG.finishCacheCleanup(c3d.getLockableDatFileReference());

        if (drawSolidMaterials && renderMode != 5)
            df.getVertexManager().showHidden();
        
    }
    
    // FIXME needs concept implementation!
    // |
    // --v Here I try to use only one(!) VAO for the price of letting an asynchronous thread doing the buffer data generation! 
    
    // However, TEXMAP/!LPE PNG will require a multi-VAO solution (all non-TEXMAP/PNG stuff can still be rendered with one VAO). 
    
    private int vao;
    private int vbo;
    private final int triangle_count = 1000000;
    private final int size = 30 * triangle_count;
    
    private volatile Lock lock = new ReentrantLock();    
    private volatile float[] data = null;
    
    private volatile AtomicBoolean isRunning = new AtomicBoolean(true);
    
    public void init() {
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, (3 + 3 + 4) * 4, 0);

        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, (3 + 3 + 4) * 4, 3 * 4);

        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, (3 + 3 + 4) * 4, (3 + 3) * 4);
        
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (isRunning.get()) {
                    float[] vertexData = new float[size];                    
                    for(int i = 0; i < size; i += 10) {
                        
                            vertexData[i] = (float) (2000f * Math.random()) - 1000f;
                            vertexData[i + 1] = (float) (2000f * Math.random()) - 1000f;
                            vertexData[i + 2] = (float) (2000f * Math.random()) - 1000f;
                            
                            vertexData[i + 3] = 1f;
                            vertexData[i + 4] = 1f;
                            vertexData[i + 5] = 1f;
                            
                            vertexData[i + 6] = (float) Math.random();
                            vertexData[i + 7] = (float) Math.random();
                            vertexData[i + 8] = (float) Math.random();
                            vertexData[i + 9] = 1f;
                            
                    }
                    lock.lock();
                    data = vertexData;
                    lock.unlock();
                }
            }
        }).start();
    }
    
    public void dispose() {
        isRunning.set(false);
        GL30.glDeleteVertexArrays(vao);
        GL15.glDeleteBuffers(vbo);
    }

    public void draw2(GLMatrixStack stack, GLShader shaderProgram, boolean drawSolidMaterials, DatFile df) {
        
            if (data == null) {
                return;
            }
            
            if (drawSolidMaterials) {
                GL30.glBindVertexArray(vao);
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
                lock.lock();
                // I can't use glBufferSubData() it creates a memory leak!!!
                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
                lock.unlock();
                
                GL20.glEnableVertexAttribArray(0);
                GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, (3 + 3 + 4) * 4, 0);
    
                GL20.glEnableVertexAttribArray(1);
                GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, (3 + 3 + 4) * 4, 3 * 4);
    
                GL20.glEnableVertexAttribArray(2);
                GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, (3 + 3 + 4) * 4, (3 + 3) * 4);
                
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            }
        
            GL30.glBindVertexArray(vao);
            
            // Transparent and solid parts are at a different location in the buffer
            if (drawSolidMaterials) {
                GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3 * triangle_count);
            } else {
                GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3 * triangle_count);
            }
            GL30.glBindVertexArray(0);
            
            // TODO Draw !LPE PNG VAOs here
            
            
            // TODO Draw !TEXMAP VAOs here
    }
}
