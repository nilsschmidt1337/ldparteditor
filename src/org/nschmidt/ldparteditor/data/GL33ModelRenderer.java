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
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.opengl.GLMatrixStack;
import org.nschmidt.ldparteditor.opengl.GLShader;

/**
 * New OpenGL 3.3 high performance render function for the model (VAO accelerated)
 * @author nils
 *
 */
public class GL33ModelRenderer {

    boolean isPaused = false;

    private final Composite3D c3d;

    public GL33ModelRenderer(Composite3D c3d) {
        this.c3d = c3d;
    }

    // FIXME needs concept implementation!
    // |
    // --v Here I try to use only one(!) VAO for the price of letting an asynchronous thread doing the buffer data generation! 

    // This is super-fast!
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
                            continue; // static_lock.unlock(); on finally
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

                        // Build the list of the data from the datfile
                        dataInOrder.clear();
                        vertexMap.clear();
                        condlineMap.clear();
                        CACHE_viewByProjection.clear();
                        
                        boolean hasTEXMAP = false;
                        boolean hasPNG = false;
                        
                        {
                            boolean[] special = loadBFCinfo(dataInOrder, vertexMap, matrixMap, df,
                                    lines, triangles, quads, condlines);
                            hasPNG = special[0];
                            hasTEXMAP = special[1];                            
                        }

                        final boolean openGL_lines = View.edge_threshold == 5e6f;
                        final int renderMode = c3d.getRenderMode();
                        final int lineMode = c3d.getLineMode();
                        final boolean showAllLines = lineMode == 1;
                        final boolean condlineMode = renderMode == 6;
                        final float zoom = c3d.getZoom();
                        final Matrix4f viewport = c3d.getViewport();

                        int triangleSize = 0;
                        int lineSize = 0;

                        int solidVertexCount = 0;

                        // Calculate the buffer sizes (and condline visibility)
                        // Lines are never transparent!
                        for (Iterator<GDataAndWinding> it = dataInOrder.iterator(); it.hasNext();) {
                            final GDataAndWinding gw = (GDataAndWinding) it.next();

                            final GData gd = gw.data;
                            switch (gd.type()) {
                            case 2:
                                // If "OpenGL lines" is ON, I have to use another buffer for it
                                if (openGL_lines) {
                                    lineSize += 17;
                                } else {

                                }
                                break;
                            case 3:
                                final GData3 gd3 = (GData3) gd;
                                if (gd3.isTriangle) {
                                    switch (renderMode) {
                                    case 0:                                        
                                        triangleSize += 60;
                                        solidVertexCount += 6;
                                        break;
                                    default:
                                        break;
                                    }
                                } else {
                                    int[] protractorSize = gd3.getProtractorDataSize();
                                    triangleSize += protractorSize[0];
                                    solidVertexCount += protractorSize[1];
                                    lineSize += protractorSize[2];
                                }
                                break;
                            case 4:
                                switch (renderMode) {
                                case 0:
                                    triangleSize += 120;
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
                                        lineSize += 17;
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

                        // for GL_TRIANGLES
                        float[] triangleData = new float[triangleSize];
                        // for GL_LINES
                        float[] lineData = new float[lineSize];
                        
                        Vertex[] v;
                        int triangleIndex = 0;
                        int lineIndex = 0;

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
                                GData3 gd3 = (GData3) gd;
                                v = vertexMap.get(gd);
                                if (gd3.isTriangle) {
                                    switch (renderMode) {
                                    case 0:
                                    {                                        
                                        Nv.x = gd3.xn;
                                        Nv.y = gd3.yn;
                                        Nv.z = gd3.zn;
                                        Nv.w = 1f;
                                        Matrix4f loc = matrixMap.get(gd3.parent);
                                        Matrix4f.transform(loc, Nv, Nv);
                                        float xn = Nv.x - loc.m30; 
                                        float yn = Nv.y - loc.m31;
                                        float zn = Nv.z - loc.m32;
                                        pointAt(0, v[0].x, v[0].y, v[0].z, triangleData, triangleIndex);
                                        pointAt(1, v[1].x, v[1].y, v[1].z, triangleData, triangleIndex);
                                        pointAt(2, v[2].x, v[2].y, v[2].z, triangleData, triangleIndex);
                                        pointAt(3, v[0].x, v[0].y, v[0].z, triangleData, triangleIndex);
                                        pointAt(4, v[2].x, v[2].y, v[2].z, triangleData, triangleIndex);
                                        pointAt(5, v[1].x, v[1].y, v[1].z, triangleData, triangleIndex);
                                        colourise(0, 6, gd3.r, gd3.g, gd3.b, gd3.visible ? gd3.a : 0f, triangleData, triangleIndex);
                                        if (gw.negativeDeterminant) {                                            
                                            normal(0, 3, xn, yn, zn, triangleData, triangleIndex);
                                            normal(3, 3, -xn, -yn, -zn, triangleData, triangleIndex);
                                        } else {
                                            normal(0, 3, -xn, -yn, -zn, triangleData, triangleIndex);
                                            normal(3, 3, xn, yn, zn, triangleData, triangleIndex);
                                        }
                                    }
                                    triangleIndex += 6;
                                    break;
                                    default:
                                        break;
                                    }
                                } else {
                                    int[] inc = gd3.insertProtractor(v, triangleData, lineData, triangleIndex, lineIndex);
                                    triangleIndex += inc[0];
                                    lineIndex += inc[1];
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
                                    pointAt(0, v[0].x, v[0].y, v[0].z, triangleData, triangleIndex);
                                    pointAt(1, v[1].x, v[1].y, v[1].z, triangleData, triangleIndex);
                                    pointAt(2, v[2].x, v[2].y, v[2].z, triangleData, triangleIndex);
                                    pointAt(3, v[2].x, v[2].y, v[2].z, triangleData, triangleIndex);
                                    pointAt(4, v[3].x, v[3].y, v[3].z, triangleData, triangleIndex);
                                    pointAt(5, v[0].x, v[0].y, v[0].z, triangleData, triangleIndex);

                                    pointAt(6, v[0].x, v[0].y, v[0].z, triangleData, triangleIndex);
                                    pointAt(7, v[3].x, v[3].y, v[3].z, triangleData, triangleIndex);
                                    pointAt(8, v[2].x, v[2].y, v[2].z, triangleData, triangleIndex);
                                    pointAt(9, v[2].x, v[2].y, v[2].z, triangleData, triangleIndex);
                                    pointAt(10, v[1].x, v[1].y, v[1].z, triangleData, triangleIndex);
                                    pointAt(11, v[0].x, v[0].y, v[0].z, triangleData, triangleIndex);

                                    colourise(0, 12, gd4.r, gd4.g, gd4.b, gd4.visible ? gd4.a : 0f, triangleData, triangleIndex);
                                    if (gw.negativeDeterminant) {                                            
                                        normal(0, 6, xn, yn, zn, triangleData, triangleIndex);
                                        normal(6, 6, -xn, -yn, -zn, triangleData, triangleIndex);
                                    } else {
                                        normal(0, 6, -xn, -yn, -zn, triangleData, triangleIndex);
                                        normal(6, 6, xn, yn, zn, triangleData, triangleIndex);
                                    }
                                    triangleIndex += 12;
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
                        data = triangleData;
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
        }).start();
    }

    public void dispose() {
        isRunning.set(false);
        GL30.glDeleteVertexArrays(vao);
        GL15.glDeleteBuffers(vbo);
    }

    private int ts, ss, to;
    public void draw(GLMatrixStack stack, GLShader shaderProgram, boolean drawSolidMaterials, DatFile df) {

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
            to = transparentOffset;
            ts = transparentSize;
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
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, ss);
        } else {
            GL11.glDrawArrays(GL11.GL_TRIANGLES, to, ts);
        }
        GL30.glBindVertexArray(0);

        // TODO Draw !LPE PNG VAOs here


        // TODO Draw !TEXMAP VAOs here
    }
    
    private boolean[] loadBFCinfo(
            final ArrayList<GDataAndWinding> dataInOrder,
            final HashMap<GData, Vertex[]> vertexMap,
            final HashMap<GData1, Matrix4f> matrixMap, final DatFile df,
            final ThreadsafeHashMap<GData2, Vertex[]> lines,
            final ThreadsafeHashMap<GData3, Vertex[]> triangles,
            final ThreadsafeHashMap<GData4, Vertex[]> quads,
            final ThreadsafeHashMap<GData5, Vertex[]> condlines) {

        final boolean[] result = new boolean[2];
        boolean hasTEXMAP = false;
        boolean hasPNG = false;
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
        result[0] = hasPNG;
        result[1] = hasTEXMAP;
        return result;
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
}
