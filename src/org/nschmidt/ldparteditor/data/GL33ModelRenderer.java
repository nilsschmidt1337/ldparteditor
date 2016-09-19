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
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.data.colour.GCChrome;
import org.nschmidt.ldparteditor.data.colour.GCMatteMetal;
import org.nschmidt.ldparteditor.data.colour.GCMetal;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.composite3d.ViewIdleManager;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.logger.NLogger;
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
    
    private volatile Lock lock = new ReentrantLock();
    private static volatile Lock static_lock = new ReentrantLock();
    private static volatile AtomicInteger idGen = new AtomicInteger(0);
    private static volatile AtomicInteger idCount = new AtomicInteger(0);
    
    private static volatile CopyOnWriteArrayList<Integer> idList = new CopyOnWriteArrayList<>();
    
    private volatile float[] data = null;
    private volatile int solidSize = 0;
    private volatile int transparentOffset = 0;
    private volatile int transparentSize = 0;
    
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
                final Vector4f Av = new Vector4f(0, 0, 0, 1f);
                final Vector4f Bv = new Vector4f(0, 0, 0, 1f);
                final Vector4f Cv = new Vector4f(0, 0, 0, 1f);
                final Vector4f Dv = new Vector4f(0, 0, 0, 1f);

                final Vector4f Nv = new Vector4f(0, 0, 0, 1f);
                final Matrix4f Mm = new Matrix4f();
                
                final ArrayList<GDataAndWinding> dataInOrder = new ArrayList<>();
                final HashMap<GData, Vertex[]> vertexMap = new HashMap<>();
                final HashMap<GData, Boolean> condlineMap = new HashMap<>();
                final HashMap<GData1, Matrix4f> CACHE_viewByProjection = new HashMap<GData1, Matrix4f>(1000);
                final HashMap<GData1, Matrix4f> matrixMap = new HashMap<>();
                final Integer myID = idGen.getAndIncrement();
                matrixMap.put(View.DUMMY_REFERENCE, View.ID);
                idList.add(myID);
                while (isRunning.get()) {
                    
                    boolean myTurn;
                    try {
                        myTurn = myID == idList.get(idCount.get()); 
                    } catch (IndexOutOfBoundsException iob) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {}
                        continue;
                    }
                    
                    if (!myTurn) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {}
                        continue;
                    }
                    
                    try {                        
                            static_lock.lock();
                            final long start = System.currentTimeMillis();

                            // First we have to get links to the sets from the model
                            final DatFile df = c3d.getLockableDatFileReference();
                            // Just to speed up things in some cases...                           
                            if (!df.isDrawSelection()) {
                                continue; // static_lock.lock(); on finally
                            }
                            final VertexManager vm = df.getVertexManager();
                            // For the vertices, we have to create a copy, since we have to iterate the set
                            final Set<Vertex> vertices = new TreeSet<Vertex>(vm.vertexLinkedToPositionInFile.keySet());
                            final Set<Vertex> selectedVertices = new TreeSet<Vertex>(vm.selectedVertices);
                            // The links are sufficient
                            final Set<GData> selectedData = vm.selectedData;
                            final ThreadsafeHashMap<GData2, Vertex[]> lines = vm.lines;
                            final ThreadsafeHashMap<GData3, Vertex[]> triangles = vm.triangles;
                            final ThreadsafeHashMap<GData4, Vertex[]> quads = vm.quads;
                            final ThreadsafeHashMap<GData5, Vertex[]> condlines = vm.condlines;                            

                            boolean hasTEXMAP = false;
                            boolean hasPNG = false;

                            // Build the list of the data from the datfile
                            dataInOrder.clear();
                            vertexMap.clear();
                            condlineMap.clear();
                            CACHE_viewByProjection.clear();
                            {
                                Stack<GData> stack = new Stack<>();
                                Stack<Byte> tempWinding = new Stack<>();
                                Stack<Boolean> tempInvertNext = new Stack<>();
                                Stack<Boolean> tempInvertNextFound = new Stack<>();
                                Stack<Boolean> tempNegativeDeterminant = new Stack<>();
                                boolean isCertified = true; 
                                
                                GData gd = df.getDrawChainStart();

                                byte localWinding = BFC.NOCERTIFY;
                                int accumClip = 0;
                                boolean globalInvertNext = false;
                                boolean globalInvertNextFound = false;
                                boolean globalNegativeDeterminant = false;

                                // The BFC logic/state machine is not correct yet? (for BFC no-certify).
                                while ((gd = gd.next) != null || !stack.isEmpty()) {                                
                                    if (gd == null) {
                                        if (accumClip > 0) {
                                            accumClip--;
                                        }
                                        gd = stack.pop();
                                        localWinding = tempWinding.pop();
                                        isCertified = localWinding != BFC.NOCERTIFY;
                                        globalInvertNext = tempInvertNext.pop();
                                        globalInvertNextFound = tempInvertNextFound.pop();
                                        globalNegativeDeterminant = tempNegativeDeterminant.pop();
                                        continue;
                                    }
                                    final int type = gd.type();
                                    boolean addData = false;
                                    Vertex[] verts;
                                    switch (type) {
                                    case 1:
                                        break;
                                    case 2:
                                        verts = lines.get(gd);
                                        if (verts != null) {
                                            vertexMap.put(gd, verts);
                                            addData = true;
                                        }
                                        break;
                                    case 3:
                                        verts = triangles.get(gd);
                                        if (verts != null) {
                                            vertexMap.put(gd, verts);
                                            addData = true;
                                        }
                                        break;
                                    case 4:
                                        verts = quads.get(gd);
                                        if (verts != null) {
                                            vertexMap.put(gd, verts);
                                            addData = true;
                                        }
                                        break;
                                    case 5:
                                        verts = condlines.get(gd);
                                        if (verts != null) {
                                            vertexMap.put(gd, verts);
                                            addData = true;
                                        }
                                        break;
                                    case 6:
                                        break;
                                    case 9:
                                        hasTEXMAP = true;
                                        break;
                                    case 10:
                                        hasPNG = true;
                                        break;
                                    default:
                                        continue;
                                    }

                                    if (addData) {
                                        dataInOrder.add(new GDataAndWinding(gd, localWinding, globalNegativeDeterminant, globalInvertNext));
                                    }

                                    switch (type) {
                                    case 1:
                                        final GData1 gd1 = ((GData1) gd);
                                        matrixMap.put(gd1, gd1.productMatrix);
                                        stack.push(gd);
                                        isCertified = localWinding != BFC.NOCERTIFY;
                                        tempWinding.push(localWinding);
                                        tempInvertNext.push(globalInvertNext);
                                        tempInvertNextFound.push(globalInvertNextFound);
                                        tempNegativeDeterminant.push(globalNegativeDeterminant);
                                        if (accumClip > 0) {
                                            accumClip++;
                                        }
                                        globalInvertNextFound = false;
                                        localWinding = BFC.NOCERTIFY;
                                        globalNegativeDeterminant = globalNegativeDeterminant ^ gd1.negativeDeterminant;
                                        gd = gd1.myGData;
                                        break;
                                    case 6:
                                        if (!isCertified) {
                                            break;
                                        }
                                        if (accumClip > 0) {
                                            switch (type) {
                                            case BFC.CCW_CLIP:
                                                if (accumClip == 1)
                                                    accumClip = 0;
                                                localWinding = BFC.CCW;
                                                break;
                                            case BFC.CLIP:
                                                if (accumClip == 1)
                                                    accumClip = 0;
                                                break;
                                            case BFC.CW_CLIP:
                                                if (accumClip == 1)
                                                    accumClip = 0;
                                                localWinding = BFC.CW;
                                                break;
                                            default:
                                                break;
                                            }
                                        } else {
                                            switch (((GDataBFC) gd).type) {
                                            case BFC.CCW:
                                                localWinding = BFC.CCW;
                                                break;
                                            case BFC.CCW_CLIP:
                                                localWinding = BFC.CCW;
                                                break;
                                            case BFC.CW:
                                                localWinding = BFC.CW;
                                                break;
                                            case BFC.CW_CLIP:
                                                localWinding = BFC.CW;
                                                break;
                                            case BFC.INVERTNEXT:
                                                boolean validState = false;
                                                GData g = gd.next;
                                                while (g != null && g.type() < 2) {
                                                    if (g.type() == 1) {
                                                        if (g.visible) validState = true;
                                                        break;
                                                    } else if (!g.toString().trim().isEmpty()) {
                                                        break;
                                                    }
                                                    g = g.next;
                                                }
                                                if (validState) {
                                                    globalInvertNext = !globalInvertNext;
                                                    globalInvertNextFound = true;
                                                }
                                                break;
                                            case BFC.NOCERTIFY:
                                                localWinding = BFC.NOCERTIFY;
                                                break;
                                            case BFC.NOCLIP:
                                                if (accumClip == 0)
                                                    accumClip = 1;
                                                break;
                                            default:
                                                break;
                                            }
                                        }
                                        break;
                                    default:
                                        break;
                                    }
                                }
                            }

                            final boolean openGL_lines = View.edge_threshold == 5e6f;
                            final int renderMode = c3d.getRenderMode();
                            final int lineMode = c3d.getLineMode();
                            final boolean showAllLines = lineMode == 1;
                            final boolean condlineMode = renderMode == 6;
                            final float zoom = c3d.getZoom();
                            final Matrix4f viewport = c3d.getViewport();

                            int size2 = 0;
                            int size3 = 0;
                            
                            int solidVertexCount = 0;

                            // Calculate the buffer sizes (and condline visibility)
                            for (Iterator<GDataAndWinding> it = dataInOrder.iterator(); it.hasNext();) {
                                final GDataAndWinding gw = (GDataAndWinding) it.next();

                                final GData gd = gw.data;
                                switch (gd.type()) {
                                case 2:
                                    // If "OpenGL lines" is ON, I have to use another buffer for it
                                    if (openGL_lines) {
                                        size3 += 17;
                                    } else {

                                    }
                                    break;
                                case 3:
                                    switch (renderMode) {
                                    case 0:
                                        size2 += 60;
                                        solidVertexCount += 6;
                                        break;
                                    default:
                                        break;
                                    }
                                    break;
                                case 4:
                                    switch (renderMode) {
                                    case 0:
                                        size2 += 120;
                                        solidVertexCount += 12;
                                        break;
                                    default:
                                        break;
                                    }
                                    break;
                                case 5:
                                    // Condlines are tricky, since I have to calculate their visibility
                                    // and save the result for the special condline mode
                                    GData5 gd5 = (GData5) gd;
                                    final boolean visible = gd5.isShown(viewport, CACHE_viewByProjection, zoom, Av, Bv, Cv, Dv, Nv, Mm);
                                    if (showAllLines || condlineMode || visible) {
                                        condlineMap.put(gd, visible);
                                        // If "OpenGL lines" is ON, I have to use another buffer for it 
                                        if (openGL_lines) {
                                            size3 += 17;
                                        } else {

                                        }
                                    } else {
                                        it.remove();
                                    }
                                    break;
                                default:
                                    break;
                                }
                            }

                            float[] vertexData = new float[size2];
                            Vertex[] v;
                            int i = 0;
                            
                            // Iterate the objects and generate the buffer data
                            // TEXMAP and Real Backface Culling are quite "the same", but they need different vertex normals / materials
                            for (GDataAndWinding gw : dataInOrder) {                                
                                final GData gd = gw.data;
                                switch (gd.type()) {
                                case 2:
                                    // If "OpenGL lines" is ON, I have to use another buffer for it
                                    if (openGL_lines) {

                                    } else {

                                    }
                                    break;
                                case 3:
                                    switch (renderMode) {
                                    case 0:
                                    {                                        
                                        v = vertexMap.get(gd);
                                        GData3 gd3 = (GData3) gd;
                                        Nv.x = gd3.xn;
                                        Nv.y = gd3.yn;
                                        Nv.z = gd3.zn;
                                        Nv.w = 1f;
                                        Matrix4f loc = matrixMap.get(gd3.parent);
                                        Matrix4f.transform(loc, Nv, Nv);
                                        float xn = Nv.x - loc.m30; 
                                        float yn = Nv.y - loc.m31;
                                        float zn = Nv.z - loc.m32;
                                        pointAt(0, v[0].x, v[0].y, v[0].z, vertexData, i);
                                        pointAt(1, v[1].x, v[1].y, v[1].z, vertexData, i);
                                        pointAt(2, v[2].x, v[2].y, v[2].z, vertexData, i);
                                        pointAt(3, v[0].x, v[0].y, v[0].z, vertexData, i);
                                        pointAt(4, v[2].x, v[2].y, v[2].z, vertexData, i);
                                        pointAt(5, v[1].x, v[1].y, v[1].z, vertexData, i);
                                        colourise(0, 6, gd3.r, gd3.g, gd3.b, gd3.a, vertexData, i);
                                        if (gw.negativeDeterminant) {                                            
                                            normal(0, 3, xn, yn, zn, vertexData, i);
                                            normal(3, 3, -xn, -yn, -zn, vertexData, i);
                                        } else {
                                            normal(0, 3, -xn, -yn, -zn, vertexData, i);
                                            normal(3, 3, xn, yn, zn, vertexData, i);
                                        }
                                    }
                                    i += 6;
                                        break;
                                    default:
                                        break;
                                    }
                                    
                                    break;
                                case 4:
                                    switch (renderMode) {
                                    case 0:
                                        v = vertexMap.get(gd);
                                        GData4 gd4 = (GData4) gd;
                                        Nv.x = gd4.xn;
                                        Nv.y = gd4.yn;
                                        Nv.z = gd4.zn;
                                        Nv.w = 1f;
                                        Matrix4f loc = matrixMap.get(gd4.parent);
                                        Matrix4f.transform(loc, Nv, Nv);
                                        Nv.x = Nv.x - loc.m30; 
                                        Nv.y = Nv.y - loc.m31;
                                        Nv.z = Nv.z - loc.m32;
                                        float xn = Nv.x;
                                        float yn = Nv.y;
                                        float zn = Nv.z;
                                        pointAt(0, v[0].x, v[0].y, v[0].z, vertexData, i);
                                        pointAt(1, v[1].x, v[1].y, v[1].z, vertexData, i);
                                        pointAt(2, v[2].x, v[2].y, v[2].z, vertexData, i);
                                        pointAt(3, v[2].x, v[2].y, v[2].z, vertexData, i);
                                        pointAt(4, v[3].x, v[3].y, v[3].z, vertexData, i);
                                        pointAt(5, v[0].x, v[0].y, v[0].z, vertexData, i);
                                        
                                        pointAt(6, v[0].x, v[0].y, v[0].z, vertexData, i);
                                        pointAt(7, v[3].x, v[3].y, v[3].z, vertexData, i);
                                        pointAt(8, v[2].x, v[2].y, v[2].z, vertexData, i);
                                        pointAt(9, v[2].x, v[2].y, v[2].z, vertexData, i);
                                        pointAt(10, v[1].x, v[1].y, v[1].z, vertexData, i);
                                        pointAt(11, v[0].x, v[0].y, v[0].z, vertexData, i);
                                        
                                        colourise(0, 12, gd4.r, gd4.g, gd4.b, gd4.a, vertexData, i);
                                        if (gw.negativeDeterminant) {                                            
                                            normal(0, 6, xn, yn, zn, vertexData, i);
                                            normal(6, 6, -xn, -yn, -zn, vertexData, i);
                                        } else {
                                            normal(0, 6, -xn, -yn, -zn, vertexData, i);
                                            normal(6, 6, xn, yn, zn, vertexData, i);
                                        }
                                        i += 12;
                                        break;
                                    default:
                                        break;
                                    }
                                    break;
                                case 5:
                                    // If "OpenGL lines" is ON, I have to use another buffer for it
                                    if (openGL_lines) {

                                    } else {

                                    }
                                    break;
                                default:
                                    break;
                                }
                            }

                            lock.lock();
                            data = vertexData;
                            solidSize = solidVertexCount;
                            transparentSize = 0;
                            transparentOffset = 0;
                            lock.unlock();

                            NLogger.debug(getClass(), "Processing time: " + (System.currentTimeMillis() - start)); //$NON-NLS-1$
                    } catch (Exception ex) {
                        NLogger.debug(getClass(), "Exception: " + ex.getMessage()); //$NON-NLS-1$
                    } finally {
                        static_lock.unlock();
                    }
                    if (idCount.incrementAndGet() >= idList.size()) {
                        idCount.set(0);
                    }
                }
                idCount.set(0);
                idList.remove(myID);
                idCount.set(0);
            }
            
            private void normal(int offset, int times, float xn, float yn, float zn,
                    float[] vertexData, int i) {
                for (int j = 0; j < times; j++) {
                    int pos = (offset + i + j) * 10;
                    vertexData[pos + 3] = xn;
                    vertexData[pos + 4] = yn;
                    vertexData[pos + 5] = zn;
                }
            }

            private void colourise(int offset, int times, float r, float g, float b,
                    float a, float[] vertexData, int i) {
                for (int j = 0; j < times; j++) {
                    int pos = (offset + i + j) * 10;
                    vertexData[pos + 6] = r;
                    vertexData[pos + 7] = g;
                    vertexData[pos + 8] = b;
                    vertexData[pos + 9] = a;
                }
            }

            private void pointAt(int offset, float x, float y, float z,
                    float[] vertexData, int i) {
                int pos = (offset + i) * 10;
                vertexData[pos] = x;
                vertexData[pos + 1] = y;
                vertexData[pos + 2] = z;
            }

            class GDataAndWinding {
                final GData data;
                final byte winding;
                final boolean negativeDeterminant;
                final boolean invertNext;
                public GDataAndWinding(GData gd, byte bfc, boolean negDet, boolean iNext) {
                    data = gd;
                    winding = bfc;
                    negativeDeterminant = negDet;
                    invertNext = iNext;
                }
            }
        }).start();
    }
    
    public void dispose() {
        isRunning.set(false);
        GL30.glDeleteVertexArrays(vao);
        GL15.glDeleteBuffers(vbo);
    }

    private int ts, ss, to, so;
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
                ss = solidSize;
                ts = transparentSize;
                to = transparentOffset;
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
                GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, solidSize);
            } else {
                GL11.glDrawArrays(GL11.GL_TRIANGLES, transparentOffset, transparentSize);
            }
            GL30.glBindVertexArray(0);
            
            // TODO Draw !LPE PNG VAOs here
            
            
            // TODO Draw !TEXMAP VAOs here
    }
}
