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
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.composite.Composite3D;
import org.nschmidt.ldparteditor.enumtype.LDConfig;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.helper.LDPartEditorException;
import org.nschmidt.ldparteditor.helper.StudLogo;
import org.nschmidt.ldparteditor.helper.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.opengl.GLMatrixStack;
import org.nschmidt.ldparteditor.opengl.GLShader;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer33;

/**
 * New OpenGL 3.3 high performance render function for the LDraw standard render mode
 */
public class GL33ModelRendererLDrawStandard {

    private static final GTexture CUBEMAP_TEXTURE = new GTexture(TexType.PLANAR, "cmap.png", null, 1, new Vector3f(1,0,0), new Vector3f(1,1,0), new Vector3f(1,1,1), 0, 0); //$NON-NLS-1$
    private static final GTexture CUBEMAP_MATTE_TEXTURE = new GTexture(TexType.PLANAR, "matte_metal.png", null, 2, new Vector3f(1,0,0), new Vector3f(1,1,0), new Vector3f(1,1,1), 0, 0); //$NON-NLS-1$
    private static final GTexture CUBEMAP_METAL_TEXTURE = new GTexture(TexType.PLANAR, "metal.png", null, 3, new Vector3f(1,0,0), new Vector3f(1,1,0), new Vector3f(1,1,1), 0, 0); //$NON-NLS-1$
    private static final GTexture CUBEMAP_PEARL_TEXTURE = new GTexture(TexType.PLANAR, "pearl.png", null, 4, new Vector3f(1,0,0), new Vector3f(1,1,0), new Vector3f(1,1,1), 0, 0); //$NON-NLS-1$

    private static Set<String> filesWithLogo1 = new HashSet<>();
    private static Set<String> filesWithLogo2 = new HashSet<>();

    static {
        filesWithLogo1.add("STUD.DAT"); //$NON-NLS-1$
        filesWithLogo1.add("STUD.dat"); //$NON-NLS-1$
        filesWithLogo1.add("stud.dat"); //$NON-NLS-1$
        filesWithLogo2.add("STUD2.DAT"); //$NON-NLS-1$
        filesWithLogo2.add("STUD2.dat"); //$NON-NLS-1$
        filesWithLogo2.add("stud2.dat"); //$NON-NLS-1$
    }

    private final Composite3D c3d;
    private final OpenGLRenderer33 renderer;

    public GL33ModelRendererLDrawStandard(Composite3D c3d, OpenGLRenderer33 renderer) {
        this.c3d = c3d;
        this.renderer = renderer;
    }

    private int vao;
    private int vbo;

    private int vaoLines;
    private int vboLines;

    private int vaoCondlines;
    private int vboCondlines;

    private int vaoStudLogo1;
    private int vboStudLogo1;

    private int vaoStudLogo2;
    private int vboStudLogo2;

    private volatile Lock lock = new ReentrantLock();
    private static volatile Lock staticLock = new ReentrantLock();
    private static volatile AtomicInteger idGen = new AtomicInteger(0);
    private static volatile AtomicInteger idCount = new AtomicInteger(0);

    private static volatile CopyOnWriteArrayList<Integer> idList = new CopyOnWriteArrayList<>();

    private volatile float[] dataTriangles = null;
    private volatile float[] dataLines = new float[]{0f};
    private volatile float[] dataCondlines = new float[]{0f};
    private volatile int solidTriangleSize = 0;
    private volatile int transparentTriangleOffset = 0;
    private volatile int transparentTriangleSize = 0;
    private volatile int lineSize = 0;
    private volatile int condlineSize = 0;

    private volatile List<GDataAndTexture> texmapData = new ArrayList<>();
    private volatile Map<GData, Vertex[]> sharedVertexMap = new HashMap<>();
    private volatile Map<GData, Vector3f[]> sharedTEXMAPnormalMap = new HashMap<>();

    private volatile boolean usesTEXMAP = false;

    private volatile List<Matrix4f> stud1MatricesResult = new ArrayList<>();
    private volatile List<Matrix4f> stud2MatricesResult = new ArrayList<>();

    private volatile AtomicBoolean isRunning = new AtomicBoolean(true);

    public void init() {
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);

        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, (3 + 3 + 4 + 2) * 4, 0);

        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, (3 + 3 + 4 + 2) * 4, 3 * 4l);

        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, (3 + 3 + 4 + 2) * 4, (3 + 3) * 4l);
        
        GL20.glEnableVertexAttribArray(3);
        GL20.glVertexAttribPointer(3, 2, GL11.GL_FLOAT, false, (3 + 3 + 4 + 2) * 4, (3 + 3 + 4) * 4l);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        vaoLines = GL30.glGenVertexArrays();
        vboLines = GL15.glGenBuffers();
        GL30.glBindVertexArray(vaoLines);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboLines);

        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, (3 + 4) * 4, 0);

        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, (3 + 4) * 4, 3 * 4l);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        vaoStudLogo1 = GL30.glGenVertexArrays();
        vboStudLogo1 = GL15.glGenBuffers();
        GL30.glBindVertexArray(vaoStudLogo1);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboStudLogo1);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, StudLogo.getStudLogoData1(), GL15.GL_STATIC_DRAW);

        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, (3 + 4) * 4, 0);

        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, (3 + 4) * 4, 3 * 4l);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        vaoStudLogo2 = GL30.glGenVertexArrays();
        vboStudLogo2 = GL15.glGenBuffers();
        GL30.glBindVertexArray(vaoStudLogo2);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboStudLogo2);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, StudLogo.getStudLogoData2(), GL15.GL_STATIC_DRAW);

        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, (3 + 4) * 4, 0);

        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, (3 + 4) * 4, 3 * 4l);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        vaoCondlines = GL30.glGenVertexArrays();
        vboCondlines = GL15.glGenBuffers();
        GL30.glBindVertexArray(vaoCondlines);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboCondlines);

        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 15 * 4, 0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 15 * 4, 3 * 4l);
        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 15 * 4, 6 * 4l);
        GL20.glEnableVertexAttribArray(3);
        GL20.glVertexAttribPointer(3, 3, GL11.GL_FLOAT, false, 15 * 4, 9 * 4l);
        GL20.glEnableVertexAttribArray(4);
        GL20.glVertexAttribPointer(4, 3, GL11.GL_FLOAT, false, 15 * 4, 12 * 4l);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        new Thread(this::renderThread).start();
    }

    private void renderThread() {

        float[] normal;

        final Vector4f nv = new Vector4f(0, 0, 0, 1f);
        final Matrix4f mm = new Matrix4f();
        Matrix4f.setIdentity(mm);

        final List<GDataAndWinding> dataInOrder = new ArrayList<>();
        final List<GDataAndTexture> texmapDataInOrder = new ArrayList<>();
        final Map<GData, Vertex[]> vertexMap = new HashMap<>();
        final Map<GData, Vertex[]> vertexMap2 = new HashMap<>();
        final Map<GData, float[]> normalMap = new HashMap<>();
        final ThreadsafeHashMap<GData1, Matrix4f> cacheViewByProjection = new ThreadsafeHashMap<>(1000);
        final Map<GData1, Matrix4f> matrixMap = new HashMap<>();
        final Integer myID = idGen.getAndIncrement();
        matrixMap.put(View.DUMMY_REFERENCE, View.ID);
        idList.add(myID);
        while (isRunning.get()) {

            boolean myTurn;
            try {
                myTurn = myID.equals(idList.get(idCount.get()));
            } catch (IndexOutOfBoundsException iob) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new LDPartEditorException(ie);
                }
                continue;
            }

            if (!myTurn) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new LDPartEditorException(ie);
                }
                continue;
            }

            final int renderMode = c3d.getRenderMode();

            // Only process render mode 5
            if (renderMode == 5) try {
                staticLock.lock();

                // First we have to get links to the sets from the model
                final DatFile df = c3d.getLockableDatFileReference();
                // Just to speed up things in some cases...
                if (df == null || !df.isDrawSelection()) {
                    continue; // static_lock.unlock(); on finally
                }
                final VertexManager vm = df.getVertexManager();

                // The links are sufficient
                final ThreadsafeHashMap<GData2, Vertex[]> lines = vm.lines;
                final ThreadsafeHashMap<GData3, Vertex[]> triangles = vm.triangles;
                final ThreadsafeHashMap<GData4, Vertex[]> quads = vm.quads;
                final ThreadsafeHashMap<GData5, Vertex[]> condlines = vm.condlines;
                final boolean drawStudLogo = c3d.isShowingLogo();
                final Set<GData> dataToRemove = new HashSet<>(vertexMap.keySet());
                final Matrix4f viewport = new Matrix4f();
                viewport.load(c3d.getViewport());

                // Build the list of the data from the datfile
                dataInOrder.clear();
                texmapDataInOrder.clear();
                normalMap.clear();
                cacheViewByProjection.clear();

                {
                    usesTEXMAP = loadBFCandTEXMAPinfo(
                            dataInOrder, texmapDataInOrder, vertexMap, matrixMap, df, lines,
                            triangles, quads, condlines, drawStudLogo);
                    Set<GData> allData = new HashSet<>();
                    if (usesTEXMAP) {

                        GTexture lastTexture = null;

                        for (GDataAndTexture gw : texmapDataInOrder) {
                            allData.add(gw.data);
                            dataToRemove.remove(gw.data);

                            if (gw.texture == null && lastTexture != null) {
                                gw.texture = lastTexture;
                            }

                            if (gw.data instanceof GDataTEX tex && (tex.meta == TexMeta.START || tex.meta == TexMeta.NEXT)) {
                                lastTexture = tex.linkedTexture;
                            }
                        }

                        Collections.sort(texmapDataInOrder, (a, b) -> {
                            final GData ga = a.data;
                            final GData gb = b.data;
                            final boolean aIsTransparentSurface =
                                    ga instanceof GData3 gd3 && gd3.a < 1f
                                    || ga instanceof GData4 gd4 && gd4.a < 1f;
                            final boolean bIsTransparentSurface =
                                    gb instanceof GData3 gd3 && gd3.a < 1f
                                    || gb instanceof GData4 gd4 && gd4.a < 1f;

                            if (aIsTransparentSurface && bIsTransparentSurface) {
                                final Vertex[] saVerts =  vertexMap.containsKey(ga) ? vertexMap.get(ga) : ga.type() == 3 ? triangles.get(ga) : quads.get(ga);
                                final Vertex[] sbVerts =  vertexMap.containsKey(gb) ? vertexMap.get(gb) : gb.type() == 3 ? triangles.get(gb) : quads.get(gb);
                                if (saVerts == null || sbVerts == null) {
                                    return 0;
                                }

                                final Vector4f vf = new Vector4f();
                                final Vector4f vt = new Vector4f();

                                float aTopmostZ = -Float.MAX_VALUE;
                                for (Vertex v : saVerts) {
                                    vf.set(v.x, v.y, v.z, 1f);
                                    Matrix4f.transform(viewport, vf, vt);
                                    aTopmostZ = Math.max(aTopmostZ, vt.z);
                                }

                                float bTopmostZ = -Float.MAX_VALUE;
                                for (Vertex v : sbVerts) {
                                    vf.set(v.x, v.y, v.z, 1f);
                                    Matrix4f.transform(viewport, vf, vt);
                                    bTopmostZ = Math.max(bTopmostZ, vt.z);
                                }

                                return Float.compare(aTopmostZ, bTopmostZ);
                            } else if (aIsTransparentSurface) {
                                return 1;
                            } else if (bIsTransparentSurface) {
                                return -1;
                            } else {
                                return 0;
                            }
                        });
                        texmapData = new ArrayList<>(texmapDataInOrder);
                    } else {
                        Collections.sort(dataInOrder, (a, b) -> {
                            final GData ga = a.data;
                            final GData gb = b.data;
                            final boolean aIsTransparentSurface =
                                    ga instanceof GData3 gd3 && gd3.a < 1f
                                    || ga instanceof GData4 gd4 && gd4.a < 1f;
                            final boolean bIsTransparentSurface =
                                    gb instanceof GData3 gd3 && gd3.a < 1f
                                    || gb instanceof GData4 gd4 && gd4.a < 1f;

                            if (aIsTransparentSurface && bIsTransparentSurface) {
                                final Vertex[] saVerts =  ga.type() == 3 ? triangles.get(ga) : quads.get(ga);
                                final Vertex[] sbVerts =  gb.type() == 3 ? triangles.get(gb) : quads.get(gb);
                                if (saVerts == null || sbVerts == null) {
                                    return 0;
                                }

                                final Vector4f vf = new Vector4f();
                                final Vector4f vt = new Vector4f();

                                float aTopmostZ = -Float.MAX_VALUE;
                                for (Vertex v : saVerts) {
                                    vf.set(v.x, v.y, v.z, 1f);
                                    Matrix4f.transform(viewport, vf, vt);
                                    aTopmostZ = Math.max(aTopmostZ, vt.z);
                                }

                                float bTopmostZ = -Float.MAX_VALUE;
                                for (Vertex v : sbVerts) {
                                    vf.set(v.x, v.y, v.z, 1f);
                                    Matrix4f.transform(viewport, vf, vt);
                                    bTopmostZ = Math.max(bTopmostZ, vt.z);
                                }

                                return Float.compare(aTopmostZ, bTopmostZ);
                            } else if (aIsTransparentSurface) {
                                return 1;
                            } else if (bIsTransparentSurface) {
                                return -1;
                            } else {
                                return 0;
                            }
                        });
                    }
                    Iterator<Entry<GData, Vector3f[]>> iter = sharedTEXMAPnormalMap.entrySet().iterator();
                    while (iter.hasNext()) {
                        if(!allData.contains(iter.next().getKey())){
                            iter.remove();
                        }
                    }
                }

                final boolean smoothShading = c3d.isSmoothShading();

                final Map<GData, Vector3f[]> vertexNormals;
                if (smoothShading) {
                    // MARK Calculate normals here...
                    vertexNormals = new HashMap<>();
                    final List<GDataAndWinding> data;
                    if (usesTEXMAP) {
                        data = new ArrayList<>(dataInOrder);
                        data.addAll(texmapDataInOrder);
                    } else {
                        data = dataInOrder;
                    }
                    final Map<GData, Vector3f> surfaceNormals = new HashMap<>();
                    for (GDataAndWinding gw : data) {
                        final GData gd = gw.data;
                        Vector3f normalv = null;
                        if (gd.type() == 3) {
                            GData3 gd3 = (GData3) gd;
                            nv.x = gd3.xn;
                            nv.y = gd3.yn;
                            nv.z = gd3.zn;
                            nv.w = 1f;
                            Matrix4f loc = matrixMap.get(gd3.parent);
                            Matrix4f.transform(loc, nv, nv);
                            normalv = new Vector3f(nv.x, nv.y, nv.z);
                        } else if (gd.type() == 4) {
                            GData4 gd4 = (GData4) gd;
                            nv.x = gd4.xn;
                            nv.y = gd4.yn;
                            nv.z = gd4.zn;
                            nv.w = 1f;
                            Matrix4f loc = matrixMap.get(gd4.parent);
                            Matrix4f.transform(loc, nv, nv);
                            normalv = new Vector3f(nv.x, nv.y, nv.z);
                        }
                        if (normalv != null) {
                            // Flip the normal in case of determinant or INVERTNEXT
                            if (!(gw.winding == BFC.CCW ^ gw.invertNext)) {
                                normalv.negate();
                            }
                            surfaceNormals.put(gd, normalv);
                        }
                    }
                    surfaceNormals.values().parallelStream().forEach(n -> {
                        if (n.lengthSquared() > 0f) n.normalise();
                    });
                    // Now calculate vertex normals (based on adjacent condlines)...
                    for (GDataAndWinding gw : data) {
                        final GData gd = gw.data;
                        final int t = gd.type();
                        if (t == 3) {
                            final Vector3f norm = surfaceNormals.get(gd);
                            final Vector3f[] normals = new Vector3f[]{new Vector3f(norm), new Vector3f(norm), new Vector3f(norm)};
                            vertexNormals.put(gd, normals);
                        } else if (t == 4) {
                            final Vector3f norm = surfaceNormals.get(gd);
                            final Vector3f[] normals = new Vector3f[]{new Vector3f(norm), new Vector3f(norm), new Vector3f(norm), new Vector3f(norm)};
                            vertexNormals.put(gd, normals);
                        }
                    }
                    for (GDataAndWinding gw : data) {
                        final GData gd = gw.data;
                        final int t = gd.type();
                        if (t > 2 && t < 5) {
                            final Vector3f[] normals = vertexNormals.get(gd);
                            final Vertex[] v = vertexMap.get(gd);
                            for (int i = 0; i < t; i++) {
                                final int j = (i + 1) % t;
                                if (vm.hasCondlineAndNoEdge(v[i], v[j])) {
                                    for (GData s : vm.linkedCommonFaces(v[i], v[j])) {
                                        if (s != gd) {
                                            if (vertexMap.get(s) != null && surfaceNormals.get(s) != null) for (Vertex w : vertexMap.get(s)) {
                                                if (w.equals(v[i])) {
                                                    Vector3f.add(normals[i], surfaceNormals.get(s), normals[i]);
                                                } else if (w.equals(v[j])) {
                                                    Vector3f.add(normals[j], surfaceNormals.get(s), normals[j]);
                                                }
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                            vertexNormals.put(gd, normals);
                        }
                    }
                    vertexNormals.values().parallelStream().forEach(normals -> {
                        for (Vector3f n : normals) if (n.lengthSquared() > 0f) n.normalise();
                    });
                    // Put the new TEXMAP normals to the shared map
                    if (usesTEXMAP) {
                        for (GDataAndWinding gw : texmapDataInOrder) {
                            final GData key = gw.data;
                            sharedTEXMAPnormalMap.put(key, vertexNormals.get(key));
                        }
                    }
                } else {
                    vertexNormals = null;
                }

                final int lineMode = c3d.getLineMode();
                final boolean hideCondlines = lineMode > 1;
                final boolean hideLines = lineMode > 2;

                final List<Matrix4f> stud1Matrices;
                final List<Matrix4f> stud2Matrices;

                if (drawStudLogo) {
                    stud1Matrices = new ArrayList<>();
                    stud2Matrices = new ArrayList<>();
                } else {
                    stud1Matrices = null;
                    stud2Matrices = null;
                }

                int localTriangleSize = 0;
                int localLineSize = 0;
                int localCondlineSize = 0;

                int triangleVertexCount = 0;
                int transparentTriangleVertexCount = 0;
                int lineVertexCount = 0;
                int condlineVertexCount = 0;

                // Calculate the buffer sizes
                // Lines are never transparent!
                for (GDataAndWinding gw : dataInOrder) {

                    final GData gd = gw.data;
                    final int type = gd.type();

                    dataToRemove.remove(gd);

                    if (!gd.visible) {
                        continue;
                    }

                    switch (type) {
                    case 1:
                        // Collect stud matrices here...
                        if (drawStudLogo) {
                            GData1 gd1 = (GData1) gd;
                            // Well, it is better to use one VAO for each logo and
                            // iterate with different matrices over the VAO!
                            if (filesWithLogo1.contains(gd1.shortName)) {
                                stud1Matrices.add(gd1.productMatrix);
                            } else if (filesWithLogo2.contains(gd1.shortName)) {
                                stud2Matrices.add(gd1.productMatrix);
                            }
                        }
                        continue;
                    case 2:
                        if (hideLines) {
                            continue;
                        }
                        final GData2 gd2 = (GData2) gd;
                        if (gd2.isLine) {
                            localLineSize += 14;
                            lineVertexCount += 2;
                        }
                        continue;
                    case 3:
                        final GData3 gd3 = (GData3) gd;
                        if (gd3.isTriangle) {
                            if (gw.noclip) {
                                localTriangleSize += 72;
                                if (gd3.a < 1f) {
                                    transparentTriangleVertexCount += 6;
                                } else {
                                    triangleVertexCount += 6;
                                }
                            } else {
                                localTriangleSize += 36;
                                if (gd3.a < 1f) {
                                    transparentTriangleVertexCount += 3;
                                } else {
                                    triangleVertexCount += 3;
                                }
                            }
                        }
                        continue;
                    case 4:
                        final GData4 gd4 = (GData4) gd;
                        if (gw.noclip) {
                            localTriangleSize += 144;
                            if (gd4.a < 1f) {
                                transparentTriangleVertexCount += 12;
                            } else {
                                triangleVertexCount += 12;
                            }
                        } else {
                            localTriangleSize += 72;
                            if (gd4.a < 1f) {
                                transparentTriangleVertexCount += 6;
                            } else {
                                triangleVertexCount += 6;
                            }
                        }
                        continue;
                    case 5:
                        if (hideCondlines) {
                            continue;
                        }
                        // Condlines are tricky, since I have to calculate their visibility
                        localCondlineSize += 30;
                        condlineVertexCount += 2;
                        continue;
                    default:
                        continue;
                    }
                }

                // for GL_TRIANGLES
                float[] triangleData = new float[localTriangleSize];
                // for GL_LINES
                float[] lineData = new float[localLineSize];
                float[] condlineData = new float[localCondlineSize];

                Vertex[] v;
                int triangleIndex = 0;
                int transparentTriangleIndex = triangleVertexCount;
                int lineIndex = 0;
                int condlineIndex = 0;

                float xn = 0f;
                float yn = 0f;
                float zn = 0f;
                float xn1 = 0f;
                float yn1 = 0f;
                float zn1 = 0f;
                float xn2 = 0f;
                float yn2 = 0f;
                float zn2 = 0f;
                float xn3 = 0f;
                float yn3 = 0f;
                float zn3 = 0f;
                float xn4 = 0f;
                float yn4 = 0f;
                float zn4 = 0f;

                // Iterate the objects and generate the buffer data
                // TEXMAP and Real Backface Culling are quite "the same", but they need different vertex normals / materials
                for (GDataAndWinding gw : dataInOrder) {
                    final GData gd = gw.data;

                    final int type = gd.type();

                    if (!gd.visible) {
                        continue;
                    }

                    switch (type) {
                    case 2:
                        if (hideLines) {
                            continue;
                        }
                        GData2 gd2 = (GData2) gd;
                        v = vertexMap.get(gd);
                        if (gd2.isLine) {
                            pointAt7(0, v[0].x, v[0].y, v[0].z, lineData, lineIndex);
                            pointAt7(1, v[1].x, v[1].y, v[1].z, lineData, lineIndex);
                            colourise7(0, 2, gd2.r, gd2.g, gd2.b, 7f, lineData, lineIndex);
                            lineIndex += 2;
                        }
                        continue;
                    case 3:
                        GData3 gd3 = (GData3) gd;
                        v = vertexMap.get(gd);
                        if (gd3.isTriangle) {
                            final boolean transparent = gd3.a < 1f;
                            int tempIndex = triangleIndex;
                            if (transparent) {
                                tempIndex = transparentTriangleIndex;
                            }

                            if (smoothShading) {
                                if ((normal = normalMap.get(gd)) != null) {
                                    xn1 = normal[0];
                                    yn1 = normal[1];
                                    zn1 = normal[2];
                                    xn2 = xn1; yn2 = yn1; zn2 = zn1;
                                    xn3 = xn1; yn3 = yn1; zn3 = zn1;
                                } else {
                                    final Vector3f[] normals = vertexNormals.get(gd);
                                    {
                                        final Vector3f n = normals[0];
                                        xn1 = n.x;
                                        yn1 = n.y;
                                        zn1 = n.z;
                                    }
                                    {
                                        final Vector3f n = normals[1];
                                        xn2 = n.x;
                                        yn2 = n.y;
                                        zn2 = n.z;
                                    }
                                    {
                                        final Vector3f n = normals[2];
                                        xn3 = n.x;
                                        yn3 = n.y;
                                        zn3 = n.z;
                                    }
                                }
                            } else {
                                if ((normal = normalMap.get(gd)) != null) {
                                    xn = normal[0];
                                    yn = normal[1];
                                    zn = normal[2];
                                } else {
                                    nv.x = gd3.xn;
                                    nv.y = gd3.yn;
                                    nv.z = gd3.zn;
                                    nv.w = 1f;
                                    Matrix4f loc = matrixMap.get(gd3.parent);
                                    Matrix4f.transform(loc, nv, nv);
                                    xn = nv.x;
                                    yn = nv.y;
                                    zn = nv.z;
                                }
                            }
                            if (gw.noclip) {
                                pointAt(0, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                pointAt(1, v[1].x, v[1].y, v[1].z, triangleData, tempIndex);
                                pointAt(2, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                pointAt(3, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                pointAt(4, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                pointAt(5, v[1].x, v[1].y, v[1].z, triangleData, tempIndex);
                                float d1 = (float) Math.sqrt(Math.pow(v[0].x - v[2].x, 2.0) + Math.pow(v[0].y - v[2].y, 2.0) +  Math.pow(v[0].z - v[2].z, 2.0)) / 4096f;
                                float d2 = (float) Math.sqrt(Math.pow(v[0].x - v[1].x, 2.0) + Math.pow(v[0].y - v[1].y, 2.0) +  Math.pow(v[0].z - v[1].z, 2.0)) / 4096f;
                                uv(0, 0f, 0f, triangleData, tempIndex);
                                uv(1, 0f, d1, triangleData, tempIndex);
                                uv(2, d2, d1, triangleData, tempIndex);
                                uv(3, 0f, 0f, triangleData, tempIndex);
                                uv(4, d2, d1, triangleData, tempIndex);
                                uv(5, 0f, d1, triangleData, tempIndex);
                                colourise(0, 6, gd3.r, gd3.g, gd3.b, gd3.a, triangleData, tempIndex);
                                if (smoothShading) {
                                    normal(0, 1, xn1, yn1, zn1, triangleData, tempIndex);
                                    normal(1, 1, xn2, yn2, zn2, triangleData, tempIndex);
                                    normal(2, 1, xn3, yn3, zn3, triangleData, tempIndex);
                                    normal(3, 1, -xn1, -yn1, -zn1, triangleData, tempIndex);
                                    normal(4, 1, -xn3, -yn3, -zn3, triangleData, tempIndex);
                                    normal(5, 1, -xn2, -yn2, -zn2, triangleData, tempIndex);
                                } else {
                                    if (gw.negativeDeterminant) {
                                        normal(0, 3, xn, yn, zn, triangleData, tempIndex);
                                        normal(3, 3, -xn, -yn, -zn, triangleData, tempIndex);
                                    } else {
                                        normal(0, 3, -xn, -yn, -zn, triangleData, tempIndex);
                                        normal(3, 3, xn, yn, zn, triangleData, tempIndex);
                                    }
                                }

                                if (transparent) {
                                    transparentTriangleIndex += 6;
                                } else {
                                    triangleIndex += 6;
                                }
                            } else {
                                GColour c = LDConfig.getColour(LDConfig.getIndex(gd3.r, gd3.g, gd3.b));
                                GColourType ct = c.getType();
                                boolean hasColourType = ct != null;
                                if (hasColourType) {
                                    switch (ct.type()) {
                                    case CHROME:
                                        colourise(0, 3, gd3.r, gd3.g, gd3.b, 2f, triangleData, tempIndex);
                                        break;
                                    case MATTE_METALLIC:
                                        colourise(0, 3, gd3.r, gd3.g, gd3.b, 4.2f, triangleData, tempIndex);
                                        break;
                                    case METAL:
                                        colourise(0, 3, gd3.r, gd3.g, gd3.b, 3.2f, triangleData, tempIndex);
                                        break;
                                    case RUBBER:
                                        colourise(0, 3, gd3.r, gd3.g, gd3.b, 5.2f, triangleData, tempIndex);
                                        break;
                                    case PEARL:
                                        colourise(0, 3, gd3.r, gd3.g, gd3.b, 6.2f, triangleData, tempIndex);
                                        break;
                                    default:
                                        colourise(0, 3, gd3.r, gd3.g, gd3.b, gd3.a, triangleData, tempIndex);
                                        break;
                                    }
                                } else {
                                    colourise(0, 3, gd3.r, gd3.g, gd3.b, gd3.a, triangleData, tempIndex);
                                }

                                switch (gw.winding) {
                                case CW:
                                    if (smoothShading) {
                                        if (gw.negativeDeterminant ^ gw.invertNext) {
                                            normal(0, 1, xn1, yn1, zn1, triangleData, tempIndex);
                                            normal(1, 1, xn3, yn3, zn3, triangleData, tempIndex);
                                            normal(2, 1, xn2, yn2, zn2, triangleData, tempIndex);
                                        } else {
                                            normal(0, 1, xn1, yn1, zn1, triangleData, tempIndex);
                                            normal(1, 1, xn2, yn2, zn2, triangleData, tempIndex);
                                            normal(2, 1, xn3, yn3, zn3, triangleData, tempIndex);
                                        }
                                    } else {
                                        if (gw.invertNext) {
                                            normal(0, 3, xn, yn, zn, triangleData, tempIndex);
                                        } else {
                                            normal(0, 3, -xn, -yn, -zn, triangleData, tempIndex);
                                        }
                                    }
                                    if (gw.negativeDeterminant ^ gw.invertNext) {
                                        pointAt(0, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                        pointAt(1, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                        pointAt(2, v[1].x, v[1].y, v[1].z, triangleData, tempIndex);
                                        float d1 = (float) Math.sqrt(Math.pow(v[0].x - v[2].x, 2.0) + Math.pow(v[0].y - v[2].y, 2.0) +  Math.pow(v[0].z - v[2].z, 2.0)) / 4096f;
                                        float d2 = (float) Math.sqrt(Math.pow(v[0].x - v[1].x, 2.0) + Math.pow(v[0].y - v[1].y, 2.0) +  Math.pow(v[0].z - v[1].z, 2.0)) / 4096f;
                                        uv(0, 0f, 0f, triangleData, tempIndex);
                                        uv(1, 0f, d1, triangleData, tempIndex);
                                        uv(2, d2, d1, triangleData, tempIndex);
                                    } else {
                                        pointAt(0, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                        pointAt(1, v[1].x, v[1].y, v[1].z, triangleData, tempIndex);
                                        pointAt(2, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                        float d1 = (float) Math.sqrt(Math.pow(v[0].x - v[2].x, 2.0) + Math.pow(v[0].y - v[2].y, 2.0) +  Math.pow(v[0].z - v[2].z, 2.0)) / 4096f;
                                        float d2 = (float) Math.sqrt(Math.pow(v[0].x - v[1].x, 2.0) + Math.pow(v[0].y - v[1].y, 2.0) +  Math.pow(v[0].z - v[1].z, 2.0)) / 4096f;
                                        uv(0, 0f, 0f, triangleData, tempIndex);
                                        uv(1, 0f, d1, triangleData, tempIndex);
                                        uv(2, d2, d1, triangleData, tempIndex);
                                    }
                                    break;
                                case CCW:
                                    if (smoothShading) {
                                        if (gw.negativeDeterminant ^ gw.invertNext) {
                                            normal(0, 1, xn1, yn1, zn1, triangleData, tempIndex);
                                            normal(1, 1, xn2, yn2, zn2, triangleData, tempIndex);
                                            normal(2, 1, xn3, yn3, zn3, triangleData, tempIndex);
                                        } else {
                                            normal(0, 1, xn1, yn1, zn1, triangleData, tempIndex);
                                            normal(1, 1, xn3, yn3, zn3, triangleData, tempIndex);
                                            normal(2, 1, xn2, yn2, zn2, triangleData, tempIndex);
                                        }
                                    } else {
                                        if (gw.invertNext) {
                                            normal(0, 3, -xn, -yn, -zn, triangleData, tempIndex);
                                        } else {
                                            normal(0, 3, xn, yn, zn, triangleData, tempIndex);
                                        }
                                    }
                                    if (gw.negativeDeterminant ^ gw.invertNext) {
                                        pointAt(0, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                        pointAt(1, v[1].x, v[1].y, v[1].z, triangleData, tempIndex);
                                        pointAt(2, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                        float d1 = (float) Math.sqrt(Math.pow(v[0].x - v[1].x, 2.0) + Math.pow(v[0].y - v[1].y, 2.0) +  Math.pow(v[0].z - v[1].z, 2.0)) / 4096f;
                                        float d2 = (float) Math.sqrt(Math.pow(v[0].x - v[2].x, 2.0) + Math.pow(v[0].y - v[2].y, 2.0) +  Math.pow(v[0].z - v[2].z, 2.0)) / 4096f;
                                        uv(0, 0f, 0f, triangleData, tempIndex);
                                        uv(1, 0f, d1, triangleData, tempIndex);
                                        uv(2, d2, d1, triangleData, tempIndex);
                                    } else {
                                        pointAt(0, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                        pointAt(1, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                        pointAt(2, v[1].x, v[1].y, v[1].z, triangleData, tempIndex);
                                        float d1 = (float) Math.sqrt(Math.pow(v[0].x - v[2].x, 2.0) + Math.pow(v[0].y - v[2].y, 2.0) +  Math.pow(v[0].z - v[2].z, 2.0)) / 4096f;
                                        float d2 = (float) Math.sqrt(Math.pow(v[0].x - v[1].x, 2.0) + Math.pow(v[0].y - v[1].y, 2.0) +  Math.pow(v[0].z - v[1].z, 2.0)) / 4096f;
                                        uv(0, 0f, 0f, triangleData, tempIndex);
                                        uv(1, 0f, d1, triangleData, tempIndex);
                                        uv(2, d2, d1, triangleData, tempIndex);
                                    }
                                    break;
                                case NOCERTIFY:
                                    pointAt(0, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                    pointAt(1, v[1].x, v[1].y, v[1].z, triangleData, tempIndex);
                                    pointAt(2, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                    colourise(0, 3, 0f, 0f, 0f, 0f, triangleData, tempIndex);
                                    float d1 = (float) Math.sqrt(Math.pow(v[0].x - v[1].x, 2.0) + Math.pow(v[0].y - v[1].y, 2.0) +  Math.pow(v[0].z - v[1].z, 2.0)) / 4096f;
                                    float d2 = (float) Math.sqrt(Math.pow(v[0].x - v[2].x, 2.0) + Math.pow(v[0].y - v[2].y, 2.0) +  Math.pow(v[0].z - v[2].z, 2.0)) / 4096f;
                                    uv(0, 0f, 0f, triangleData, tempIndex);
                                    uv(1, 0f, d1, triangleData, tempIndex);
                                    uv(2, d2, d1, triangleData, tempIndex);
                                    break;
                                default:
                                    break;
                                }
                                if (transparent) {
                                    transparentTriangleIndex += 3;
                                } else {
                                    triangleIndex += 3;
                                }
                            }
                        }
                        continue;
                    case 4:
                        v = vertexMap.get(gd);
                        GData4 gd4 = (GData4) gd;
                        final boolean transparent = gd4.a < 1f;
                        int tempIndex = triangleIndex;
                        if (transparent) {
                            tempIndex = transparentTriangleIndex;
                        }

                        if (smoothShading) {
                            if ((normal = normalMap.get(gd)) != null) {
                                xn1 = normal[0];
                                yn1 = normal[1];
                                zn1 = normal[2];
                                xn2 = xn1; yn2 = yn1; zn2 = zn1;
                                xn3 = xn1; yn3 = yn1; zn3 = zn1;
                                xn4 = xn1; yn4 = yn1; zn4 = zn1;
                            } else {
                                final Vector3f[] normals = vertexNormals.get(gd);
                                {
                                    final Vector3f n = normals[0];
                                    xn1 = n.x;
                                    yn1 = n.y;
                                    zn1 = n.z;
                                }
                                {
                                    final Vector3f n = normals[1];
                                    xn2 = n.x;
                                    yn2 = n.y;
                                    zn2 = n.z;
                                }
                                {
                                    final Vector3f n = normals[2];
                                    xn3 = n.x;
                                    yn3 = n.y;
                                    zn3 = n.z;
                                }
                                {
                                    final Vector3f n = normals[3];
                                    xn4 = n.x;
                                    yn4 = n.y;
                                    zn4 = n.z;
                                }
                            }
                        } else {
                            if ((normal = normalMap.get(gd)) != null) {
                                xn = normal[0];
                                yn = normal[1];
                                zn = normal[2];
                            } else {
                                nv.x = gd4.xn;
                                nv.y = gd4.yn;
                                nv.z = gd4.zn;
                                nv.w = 1f;
                                Matrix4f loc = matrixMap.get(gd4.parent);
                                Matrix4f.transform(loc, nv, nv);
                                xn = nv.x;
                                yn = nv.y;
                                zn = nv.z;
                            }
                        }

                        if (gw.noclip) {
                            pointAt(0, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                            pointAt(1, v[1].x, v[1].y, v[1].z, triangleData, tempIndex);
                            pointAt(2, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                            pointAt(3, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                            pointAt(4, v[3].x, v[3].y, v[3].z, triangleData, tempIndex);
                            pointAt(5, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);

                            pointAt(6, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                            pointAt(7, v[3].x, v[3].y, v[3].z, triangleData, tempIndex);
                            pointAt(8, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                            pointAt(9, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                            pointAt(10, v[1].x, v[1].y, v[1].z, triangleData, tempIndex);
                            pointAt(11, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                            
                            float d1 = (float) Math.sqrt(Math.pow(v[0].x - v[1].x, 2.0) + Math.pow(v[0].y - v[1].y, 2.0) +  Math.pow(v[0].z - v[1].z, 2.0)) / 4096f;
                            float d2 = (float) Math.sqrt(Math.pow(v[0].x - v[2].x, 2.0) + Math.pow(v[0].y - v[2].y, 2.0) +  Math.pow(v[0].z - v[2].z, 2.0)) / 4096f;
                            
                            uv(0, 0f, 0f, triangleData, tempIndex);
                            uv(1, 0f, d1, triangleData, tempIndex);
                            uv(2, d2, d1, triangleData, tempIndex);
                            uv(3, 0f, 0f, triangleData, tempIndex);
                            uv(4, 0f, d1, triangleData, tempIndex);
                            uv(5, d2, d1, triangleData, tempIndex);
                            
                            uv(6, 0f, 0f, triangleData, tempIndex);
                            uv(7, 0f, d1, triangleData, tempIndex);
                            uv(8, d2, d1, triangleData, tempIndex);
                            uv(9, 0f, 0f, triangleData, tempIndex);
                            uv(10, 0f, d1, triangleData, tempIndex);
                            uv(11, d2, d1, triangleData, tempIndex);

                            colourise(0, 12, gd4.r, gd4.g, gd4.b, gd4.a, triangleData, tempIndex);
                            if (smoothShading) {
                                normal(0, 1, xn1, yn1, zn1, triangleData, tempIndex);
                                normal(1, 1, xn2, yn2, zn2, triangleData, tempIndex);
                                normal(2, 2, xn3, yn3, zn3, triangleData, tempIndex);
                                normal(4, 1, xn4, yn4, zn4, triangleData, tempIndex);
                                normal(5, 1, xn1, yn1, zn1, triangleData, tempIndex);
                                normal(6, 1, -xn1, -yn1, -zn1, triangleData, tempIndex);
                                normal(7, 1, -xn4, -yn4, -zn4, triangleData, tempIndex);
                                normal(8, 2, -xn3, -yn3, -zn3, triangleData, tempIndex);
                                normal(10, 1, -xn2, -yn2, -zn2, triangleData, tempIndex);
                                normal(11, 1, -xn1, -yn1, -zn1, triangleData, tempIndex);
                            } else {
                                if (gw.negativeDeterminant) {
                                    normal(0, 6, xn, yn, zn, triangleData, tempIndex);
                                    normal(6, 6, -xn, -yn, -zn, triangleData, tempIndex);
                                } else {
                                    normal(0, 6, -xn, -yn, -zn, triangleData, tempIndex);
                                    normal(6, 6, xn, yn, zn, triangleData, tempIndex);
                                }
                            }

                            if (transparent) {
                                transparentTriangleIndex += 12;
                            } else {
                                triangleIndex += 12;
                            }
                        } else {
                            GColour c = LDConfig.getColour(LDConfig.getIndex(gd4.r, gd4.g, gd4.b));
                            GColourType ct = c.getType();
                            boolean hasColourType = ct != null;
                            if (hasColourType) {
                                switch (ct.type()) {
                                case CHROME:
                                    colourise(0, 6, gd4.r, gd4.g, gd4.b, 2f, triangleData, tempIndex);
                                    break;
                                case MATTE_METALLIC:
                                    colourise(0, 6, gd4.r, gd4.g, gd4.b, 4.2f, triangleData, tempIndex);
                                    break;
                                case METAL:
                                    colourise(0, 6, gd4.r, gd4.g, gd4.b, 3.2f, triangleData, tempIndex);
                                    break;
                                case RUBBER:
                                    colourise(0, 6, gd4.r, gd4.g, gd4.b, 5.2f, triangleData, tempIndex);
                                    break;
                                case PEARL:
                                    colourise(0, 6, gd4.r, gd4.g, gd4.b, 6.2f, triangleData, tempIndex);
                                    break;
                                default:
                                    colourise(0, 6, gd4.r, gd4.g, gd4.b, gd4.a, triangleData, tempIndex);
                                    break;
                                }
                            } else {
                                colourise(0, 6, gd4.r, gd4.g, gd4.b, gd4.a, triangleData, tempIndex);
                            }
                            switch (gw.winding) {
                            case CW:
                                if (smoothShading) {
                                    if (gw.invertNext ^ gw.negativeDeterminant) {
                                        normal(0, 1, xn1, yn1, zn1, triangleData, tempIndex);
                                        normal(1, 1, xn4, yn4, zn4, triangleData, tempIndex);
                                        normal(2, 2, xn3, yn3, zn3, triangleData, tempIndex);
                                        normal(4, 1, xn2, yn2, zn2, triangleData, tempIndex);
                                        normal(5, 1, xn1, yn1, zn1, triangleData, tempIndex);
                                    } else {
                                        normal(0, 1, xn1, yn1, zn1, triangleData, tempIndex);
                                        normal(1, 1, xn2, yn2, zn2, triangleData, tempIndex);
                                        normal(2, 2, xn3, yn3, zn3, triangleData, tempIndex);
                                        normal(4, 1, xn4, yn4, zn4, triangleData, tempIndex);
                                        normal(5, 1, xn1, yn1, zn1, triangleData, tempIndex);
                                    }
                                } else {
                                    if (gw.invertNext) {
                                        normal(0, 6, xn, yn, zn, triangleData, tempIndex);
                                    } else {
                                        normal(0, 6, -xn, -yn, -zn, triangleData, tempIndex);
                                    }
                                }
                                if (gw.invertNext ^ gw.negativeDeterminant) {
                                    pointAt(0, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                    pointAt(1, v[3].x, v[3].y, v[3].z, triangleData, tempIndex);
                                    pointAt(2, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                    pointAt(3, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                    pointAt(4, v[1].x, v[1].y, v[1].z, triangleData, tempIndex);
                                    pointAt(5, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                    float d1 = (float) Math.sqrt(Math.pow(v[0].x - v[2].x, 2.0) + Math.pow(v[0].y - v[2].y, 2.0) +  Math.pow(v[0].z - v[2].z, 2.0)) / 4096f;
                                    float d2 = (float) Math.sqrt(Math.pow(v[0].x - v[1].x, 2.0) + Math.pow(v[0].y - v[1].y, 2.0) +  Math.pow(v[0].z - v[1].z, 2.0)) / 4096f;
                                    uv(0, 0f, 0f, triangleData, tempIndex);
                                    uv(1, 0f, d1, triangleData, tempIndex);
                                    uv(2, d2, d1, triangleData, tempIndex);
                                    uv(3, 0f, 0f, triangleData, tempIndex);
                                    uv(4, 0f, d1, triangleData, tempIndex);
                                    uv(5, d2, d1, triangleData, tempIndex);
                                } else {
                                    pointAt(0, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                    pointAt(1, v[1].x, v[1].y, v[1].z, triangleData, tempIndex);
                                    pointAt(2, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                    pointAt(3, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                    pointAt(4, v[3].x, v[3].y, v[3].z, triangleData, tempIndex);
                                    pointAt(5, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                    float d1 = (float) Math.sqrt(Math.pow(v[0].x - v[1].x, 2.0) + Math.pow(v[0].y - v[1].y, 2.0) +  Math.pow(v[0].z - v[1].z, 2.0)) / 4096f;
                                    float d2 = (float) Math.sqrt(Math.pow(v[0].x - v[2].x, 2.0) + Math.pow(v[0].y - v[2].y, 2.0) +  Math.pow(v[0].z - v[2].z, 2.0)) / 4096f;
                                    uv(0, 0f, 0f, triangleData, tempIndex);
                                    uv(1, 0f, d1, triangleData, tempIndex);
                                    uv(2, d2, d1, triangleData, tempIndex);
                                    uv(3, 0f, 0f, triangleData, tempIndex);
                                    uv(4, 0f, d1, triangleData, tempIndex);
                                    uv(5, d2, d1, triangleData, tempIndex);
                                }
                                break;
                            case CCW:
                                if (smoothShading) {
                                    if (gw.invertNext ^ gw.negativeDeterminant) {
                                        normal(0, 1, xn1, yn1, zn1, triangleData, tempIndex);
                                        normal(1, 1, xn2, yn2, zn2, triangleData, tempIndex);
                                        normal(2, 2, xn3, yn3, zn3, triangleData, tempIndex);
                                        normal(4, 1, xn4, yn4, zn4, triangleData, tempIndex);
                                        normal(5, 1, xn1, yn1, zn1, triangleData, tempIndex);
                                    } else {
                                        normal(0, 1, xn1, yn1, zn1, triangleData, tempIndex);
                                        normal(1, 1, xn4, yn4, zn4, triangleData, tempIndex);
                                        normal(2, 2, xn3, yn3, zn3, triangleData, tempIndex);
                                        normal(4, 1, xn2, yn2, zn2, triangleData, tempIndex);
                                        normal(5, 1, xn1, yn1, zn1, triangleData, tempIndex);
                                    }
                                } else {
                                    if (gw.invertNext) {
                                        normal(0, 6, -xn, -yn, -zn, triangleData, tempIndex);
                                    } else {
                                        normal(0, 6, xn, yn, zn, triangleData, tempIndex);
                                    }
                                }
                                if (gw.invertNext ^ gw.negativeDeterminant) {
                                    pointAt(0, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                    pointAt(1, v[1].x, v[1].y, v[1].z, triangleData, tempIndex);
                                    pointAt(2, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                    pointAt(3, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                    pointAt(4, v[3].x, v[3].y, v[3].z, triangleData, tempIndex);
                                    pointAt(5, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                    float d1 = (float) Math.sqrt(Math.pow(v[0].x - v[1].x, 2.0) + Math.pow(v[0].y - v[1].y, 2.0) +  Math.pow(v[0].z - v[1].z, 2.0)) / 4096f;
                                    float d2 = (float) Math.sqrt(Math.pow(v[0].x - v[2].x, 2.0) + Math.pow(v[0].y - v[2].y, 2.0) +  Math.pow(v[0].z - v[2].z, 2.0)) / 4096f;
                                    uv(0, 0f, 0f, triangleData, tempIndex);
                                    uv(1, 0f, d1, triangleData, tempIndex);
                                    uv(2, d2, d1, triangleData, tempIndex);
                                    uv(3, 0f, 0f, triangleData, tempIndex);
                                    uv(4, 0f, d1, triangleData, tempIndex);
                                    uv(5, d2, d1, triangleData, tempIndex);
                                } else {
                                    pointAt(0, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                    pointAt(1, v[3].x, v[3].y, v[3].z, triangleData, tempIndex);
                                    pointAt(2, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                    pointAt(3, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                    pointAt(4, v[1].x, v[1].y, v[1].z, triangleData, tempIndex);
                                    pointAt(5, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                    float d1 = (float) Math.sqrt(Math.pow(v[0].x - v[2].x, 2.0) + Math.pow(v[0].y - v[2].y, 2.0) +  Math.pow(v[0].z - v[2].z, 2.0)) / 4096f;
                                    float d2 = (float) Math.sqrt(Math.pow(v[0].x - v[1].x, 2.0) + Math.pow(v[0].y - v[1].y, 2.0) +  Math.pow(v[0].z - v[1].z, 2.0)) / 4096f;
                                    uv(0, 0f, 0f, triangleData, tempIndex);
                                    uv(1, 0f, d1, triangleData, tempIndex);
                                    uv(2, d2, d1, triangleData, tempIndex);
                                    uv(3, 0f, 0f, triangleData, tempIndex);
                                    uv(4, 0f, d1, triangleData, tempIndex);
                                    uv(5, d2, d1, triangleData, tempIndex);
                                }
                                break;
                            case NOCERTIFY:
                                colourise(0, 6, 0f, 0f, 0f, 0f, triangleData, tempIndex);
                                pointAt(0, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                pointAt(1, v[1].x, v[1].y, v[1].z, triangleData, tempIndex);
                                pointAt(2, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                pointAt(3, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                pointAt(4, v[3].x, v[3].y, v[3].z, triangleData, tempIndex);
                                pointAt(5, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                float d1 = (float) Math.sqrt(Math.pow(v[0].x - v[1].x, 2.0) + Math.pow(v[0].y - v[1].y, 2.0) +  Math.pow(v[0].z - v[1].z, 2.0)) / 4096f;
                                float d2 = (float) Math.sqrt(Math.pow(v[0].x - v[2].x, 2.0) + Math.pow(v[0].y - v[2].y, 2.0) +  Math.pow(v[0].z - v[2].z, 2.0)) / 4096f;
                                uv(0, 0f, 0f, triangleData, tempIndex);
                                uv(1, 0f, d1, triangleData, tempIndex);
                                uv(2, d2, d1, triangleData, tempIndex);
                                uv(3, 0f, 0f, triangleData, tempIndex);
                                uv(4, 0f, d1, triangleData, tempIndex);
                                uv(5, d2, d1, triangleData, tempIndex);
                                break;
                            default:
                                break;
                            }
                            if (transparent) {
                                transparentTriangleIndex += 6;
                            } else {
                                triangleIndex += 6;
                            }
                        }
                        continue;
                    case 5:
                        if (hideCondlines) {
                            continue;
                        }
                        GData5 gd5 = (GData5) gd;
                        v = vertexMap.get(gd);
                        pointAt15(0, v[0].x, v[0].y, v[0].z, condlineData, condlineIndex);
                        pointAt15(1, v[1].x, v[1].y, v[1].z, condlineData, condlineIndex);
                        controlPointAt15(0, 0, v[1].x, v[1].y, v[1].z, condlineData, condlineIndex);
                        controlPointAt15(0, 1, v[2].x, v[2].y, v[2].z, condlineData, condlineIndex);
                        controlPointAt15(0, 2, v[3].x, v[3].y, v[3].z, condlineData, condlineIndex);
                        controlPointAt15(1, 0, v[0].x, v[0].y, v[0].z, condlineData, condlineIndex);
                        controlPointAt15(1, 1, v[2].x, v[2].y, v[2].z, condlineData, condlineIndex);
                        controlPointAt15(1, 2, v[3].x, v[3].y, v[3].z, condlineData, condlineIndex);
                        colourise15(0, 2, gd5.r, gd5.g, gd5.b, condlineData, condlineIndex);
                        condlineIndex += 2;
                        continue;
                    default:
                        continue;
                    }
                }

                if (drawStudLogo) {
                    lock.lock();
                    stud1MatricesResult = stud1Matrices;
                    stud2MatricesResult = stud2Matrices;
                    lock.unlock();
                }

                if (vertexMap2.size() != vertexMap.size() || !dataToRemove.isEmpty()) {
                    for (GData gd : dataToRemove) {
                        vertexMap.remove(gd);
                        vertexMap2.remove(gd);
                    }
                    vertexMap2.putAll(vertexMap);
                    sharedVertexMap = vertexMap2;
                }
                lock.lock();
                dataTriangles = triangleData;
                solidTriangleSize = triangleVertexCount;
                transparentTriangleSize = transparentTriangleVertexCount;
                transparentTriangleOffset = triangleVertexCount;
                lineSize = lineVertexCount;
                dataLines = lineData;
                condlineSize = condlineVertexCount;
                dataCondlines = condlineData;
                lock.unlock();

            } catch (Exception ex) {
                if (NLogger.debugging) {
                    NLogger.debug(GL33ModelRendererLDrawStandard.class, "Exception: " + ex.getMessage()); //$NON-NLS-1$
                }
            } finally {
                staticLock.unlock();
            }
            if (idCount.incrementAndGet() >= idList.size()) {
                idCount.set(0);
            }
        }
        idCount.set(0);
        idList.remove(myID);
        idCount.set(0);
    }

    public void dispose() {
        isRunning.set(false);
        GL30.glDeleteVertexArrays(vao);
        GL15.glDeleteBuffers(vbo);
        GL30.glDeleteVertexArrays(vaoLines);
        GL15.glDeleteBuffers(vboLines);
        GL30.glDeleteVertexArrays(vaoCondlines);
        GL15.glDeleteBuffers(vboCondlines);
        GL30.glDeleteVertexArrays(vaoStudLogo1);
        GL15.glDeleteBuffers(vboStudLogo1);
        GL30.glDeleteVertexArrays(vaoStudLogo2);
        GL15.glDeleteBuffers(vboStudLogo2);
    }

    private int ts;
    private int ss;
    private int to;
    private int ls;

    public void draw(GLMatrixStack stack, GLShader mainShader, GLShader condlineShader, boolean drawSolidMaterials) {

        Matrix4f vm = c3d.getViewport();
        Matrix4f ivm = c3d.getViewportInverse();

        if (dataTriangles == null || dataLines == null) {
            return;
        }

        final float zoom = c3d.getZoom();
        final boolean drawLines = View.lineWidthGL > 0.01f;
        final boolean studlogo = c3d.isShowingLogo();

        CUBEMAP_TEXTURE.bindGL33(renderer, mainShader);
        CUBEMAP_MATTE_TEXTURE.bindGL33(renderer, mainShader);
        CUBEMAP_METAL_TEXTURE.bindGL33(renderer, mainShader);
        CUBEMAP_PEARL_TEXTURE.bindGL33(renderer, mainShader);

        if (c3d.isLightOn()) {
            mainShader.setFactor(.9f);
        } else {
            mainShader.setFactor(1f);
        }

        // Draw !TEXMAP VAOs here (slow)
        if (usesTEXMAP) {
            mainShader.texmapOn();
            if (drawSolidMaterials) {
                mainShader.transparentOff();
            } else {
                mainShader.transparentOn();
            }
            GL33TexmapRenderer.render(texmapData, mainShader, renderer, sharedTEXMAPnormalMap, sharedVertexMap, c3d.isSmoothShading(), drawSolidMaterials);
            mainShader.texmapOff();
        }

        if (drawSolidMaterials) {

            GL30.glBindVertexArray(vao);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
            lock.lock();
            // I can't use glBufferSubData() it creates a memory leak!!!
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, dataTriangles, GL15.GL_STATIC_DRAW);
            ss = solidTriangleSize;
            to = transparentTriangleOffset;
            ts = transparentTriangleSize;
            ls = lineSize;
            lock.unlock();

            GL20.glEnableVertexAttribArray(0);
            GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, (3 + 3 + 4 + 2) * 4, 0);

            GL20.glEnableVertexAttribArray(1);
            GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, (3 + 3 + 4 + 2) * 4, 3 * 4l);

            GL20.glEnableVertexAttribArray(2);
            GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, (3 + 3 + 4 + 2) * 4, (3 + 3) * 4l);
            
            GL20.glEnableVertexAttribArray(3);
            GL20.glVertexAttribPointer(3, 2, GL11.GL_FLOAT, false, (3 + 3 + 4 + 2) * 4, (3 + 3 + 4) * 4l);

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        }

        GL30.glBindVertexArray(vao);

        // Transparent and solid parts are at a different location in the buffer
        if (drawSolidMaterials) {

            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, ss);
            mainShader.lightsOff();

            if (ls > 0 && drawLines) {
                GL30.glBindVertexArray(vaoLines);
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboLines);
                lock.lock();
                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, dataLines, GL15.GL_STATIC_DRAW);
                ls = lineSize;
                lock.unlock();

                GL20.glEnableVertexAttribArray(0);
                GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, (3 + 4) * 4, 0);

                GL20.glEnableVertexAttribArray(2);
                GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, (3 + 4) * 4, 3 * 4l);

                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
                GL11.glLineWidth(View.lineWidthGL);

                Vector4f tr = new Vector4f(vm.m30, vm.m31, vm.m32 + 330f * zoom, 1f);
                Matrix4f.transform(ivm, tr, tr);
                stack.glPushMatrix();
                stack.glTranslatef(tr.x, tr.y, tr.z);
                GL11.glDrawArrays(GL11.GL_LINES, 0, ls);
                stack.glPopMatrix();
            }

            if (studlogo) {

                GL11.glLineWidth(3f);

                stack.glPushMatrix();

                {
                    lock.lock();
                    List<Matrix4f> stud1Matrices = stud1MatricesResult;
                    List<Matrix4f> stud2Matrices = stud2MatricesResult;
                    lock.unlock();
                    GL30.glBindVertexArray(vaoStudLogo1);
                    for (Matrix4f m : stud1Matrices) {
                        stack.glLoadMatrix(m);
                        GL11.glDrawArrays(GL11.GL_LINES, 0, 320);
                    }
                    GL30.glBindVertexArray(vaoStudLogo2);
                    for (Matrix4f m : stud2Matrices) {
                        stack.glLoadMatrix(m);
                        GL11.glDrawArrays(GL11.GL_LINES, 0, 320);
                    }
                }

                stack.glPopMatrix();

                GL11.glLineWidth(1f);
            }

            mainShader.setFactor(1f);

        } else {

            GL11.glDrawArrays(GL11.GL_TRIANGLES, to, ts);
            mainShader.lightsOff();
            mainShader.setFactor(1f);
        }

        // Draw condlines here
        if (drawSolidMaterials && drawLines) {
            condlineShader.use();
            stack.setShader(condlineShader);

            GL20.glUniform1f(condlineShader.getUniformLocation("showAll"), c3d.getLineMode() == 1 ? 1f : 0f); //$NON-NLS-1$
            GL20.glUniform1f(condlineShader.getUniformLocation("condlineMode"), c3d.getRenderMode() == 6 ? 1f : 0f); //$NON-NLS-1$

            GL30.glBindVertexArray(vaoCondlines);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboCondlines);
            lock.lock();
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, dataCondlines, GL15.GL_STATIC_DRAW);
            final int cls = condlineSize;
            lock.unlock();

            GL20.glEnableVertexAttribArray(0);
            GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 15 * 4, 0);
            GL20.glEnableVertexAttribArray(1);
            GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 15 * 4, 3 * 4l);
            GL20.glEnableVertexAttribArray(2);
            GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 15 * 4, 6 * 4l);
            GL20.glEnableVertexAttribArray(3);
            GL20.glVertexAttribPointer(3, 3, GL11.GL_FLOAT, false, 15 * 4, 9 * 4l);
            GL20.glEnableVertexAttribArray(4);
            GL20.glVertexAttribPointer(4, 3, GL11.GL_FLOAT, false, 15 * 4, 12 * 4l);

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

            Vector4f tr = new Vector4f(vm.m30, vm.m31, vm.m32 + 330f * zoom, 1f);
            Matrix4f.transform(ivm, tr, tr);
            stack.glPushMatrix();
            stack.glTranslatef(tr.x, tr.y, tr.z);
            GL11.glLineWidth(View.lineWidthGL);
            GL11.glDrawArrays(GL11.GL_LINES, 0, cls);
            stack.glPopMatrix();
            mainShader.use();
            stack.setShader(mainShader);
        }

        if (c3d.isLightOn()) {
            mainShader.lightsOn();
        }
        GL30.glBindVertexArray(0);
    }

    private boolean loadBFCandTEXMAPinfo(
            final List<GDataAndWinding> dataInOrder,
            final List<GDataAndTexture> texmapDataInOrder,
            final Map<GData, Vertex[]> vertexMap,
            final Map<GData1, Matrix4f> matrixMap,
            final DatFile df, final ThreadsafeHashMap<GData2, Vertex[]> lines,
            final ThreadsafeHashMap<GData3, Vertex[]> triangles,
            final ThreadsafeHashMap<GData4, Vertex[]> quads,
            final ThreadsafeHashMap<GData5, Vertex[]> condlines,
            final boolean drawStudLogo) {

        final boolean parseTexmap = true;

        boolean hasTEXMAP = false;
        Deque<GData> stack = new LinkedList<>();
        Deque<BFC> tempWinding = new LinkedList<>();
        Deque<Boolean> tempInvertNext = new LinkedList<>();
        Deque<Boolean> tempInvertNextFound = new LinkedList<>();
        Deque<Boolean> tempNegativeDeterminant = new LinkedList<>();

        GData gd = df.getDrawChainStart();
        GData backup = gd;

        BFC localWinding = BFC.NOCERTIFY;
        int accumClip = 0;
        boolean globalInvertNext = false;
        boolean globalInvertNextFound = false;
        boolean globalNegativeDeterminant = false;
        boolean texmap = false;
        boolean texmapNext = false;
        GData firstTexmapObject = null;

        Vector4f v1 = new Vector4f(0f, 0f, 0f, 1f);
        Vector4f v2 = new Vector4f(0f, 0f, 0f, 1f);
        Vector4f v3 = new Vector4f(0f, 0f, 0f, 1f);
        Vector4f v4 = new Vector4f(0f, 0f, 0f, 1f);

        // The BFC logic/state machine is not correct yet? (for BFC INVERTNEXT).
        while ((gd = backup.next) != null || !stack.isEmpty()) {
            final boolean parseTexmapSubfile = firstTexmapObject != null;
            if (gd == null) {
                if (accumClip > 0) {
                    accumClip--;
                }
                backup = stack.pop();
                if (backup == firstTexmapObject) {
                    firstTexmapObject = null;
                    if (texmapNext) {
                        texmap = false;
                        texmapNext = false;
                    }
                }
                localWinding = tempWinding.pop();
                globalInvertNextFound = tempInvertNextFound.pop();
                if (globalInvertNextFound) {
                    globalInvertNext = !tempInvertNext.pop();
                } else {
                    tempInvertNext.pop();
                }
                globalInvertNextFound = false;
                globalNegativeDeterminant = tempNegativeDeterminant.pop();
                continue;
            }
            Vertex[] verts = null;
            backup = gd;
            if (parseTexmap && gd.type() == 9) {
                final GDataTEX tex = (GDataTEX) gd;
                if (tex.linkedData != null) {
                    gd = tex.linkedData;
                }
            }
            switch (gd.type()) {
            case 1:
                if (texmap && !parseTexmapSubfile) {
                    firstTexmapObject = backup;
                }
                final GData1 gd1 = (GData1) gd;
                final Matrix4f rotation = new Matrix4f(gd1.productMatrix);
                rotation.m30 = 0f;
                rotation.m31 = 0f;
                rotation.m32 = 0f;
                rotation.invert();
                rotation.transpose();
                matrixMap.put(gd1, rotation);
                stack.push(backup);
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
                if (drawStudLogo) {
                    dataInOrder.add(new GDataAndWinding(gd, localWinding, globalNegativeDeterminant, globalInvertNext, accumClip));
                }
                backup = gd1.myGData;
                continue;
            case 6:
                if (!tempWinding.isEmpty() && tempWinding.peek() == BFC.NOCERTIFY) {
                    continue;
                }
                if (accumClip > 0) {
                    switch (((GDataBFC) gd).type) {
                    case CCW_CLIP:
                        if (accumClip == 1)
                            accumClip = 0;
                        localWinding = BFC.CCW;
                        continue;
                    case CLIP:
                        if (accumClip == 1)
                            accumClip = 0;
                        continue;
                    case CW_CLIP:
                        if (accumClip == 1)
                            accumClip = 0;
                        localWinding = BFC.CW;
                        continue;
                    default:
                        continue;
                    }
                } else {
                    switch (((GDataBFC) gd).type) {
                    case CCW, CCW_CLIP:
                        localWinding = BFC.CCW;
                        continue;
                    case CW, CW_CLIP:
                        localWinding = BFC.CW;
                        continue;
                    case INVERTNEXT:
                        boolean validState = false;
                        GData g = gd.next;
                        while (g != null && g.type() < 2) {
                            validState = g.type() == 1 && g.visible;
                            if (validState || !g.toString().trim().isEmpty()) {
                                break;
                            }
                            g = g.next;
                        }
                        if (validState) {
                            globalInvertNext = !globalInvertNext;
                            globalInvertNextFound = true;
                        }
                        continue;
                    case NOCERTIFY:
                        localWinding = BFC.NOCERTIFY;
                        continue;
                    case NOCLIP:
                        if (accumClip == 0)
                            accumClip = 1;
                        continue;
                    default:
                        continue;
                    }
                }
            case 2:
                if (texmapNext && !parseTexmapSubfile) {
                    texmap = false;
                    texmapNext = false;
                }
                GData2 gd2 = (GData2) gd;
                verts = lines.get(gd2);
                if (verts != null) {
                    vertexMap.put(gd, verts);
                    dataInOrder.add(new GDataAndWinding(gd, localWinding, globalNegativeDeterminant, globalInvertNext, accumClip));
                }
                continue;
            case 3:
                GData3 gd3 = (GData3) gd;
                if (texmap) {
                    if (gd3.isTriangle) {
                        verts = triangles.get(gd3);
                        if (verts == null) {
                            v1.set(gd3.x1, gd3.y1, gd3.z1, 1f);
                            v2.set(gd3.x2, gd3.y2, gd3.z2, 1f);
                            v3.set(gd3.x3, gd3.y3, gd3.z3, 1f);
                            Matrix4f m = gd3.parent.productMatrix;
                            Matrix4f.transform(m, v1, v1);
                            Matrix4f.transform(m, v2, v2);
                            Matrix4f.transform(m, v3, v3);
                            verts = new Vertex[]{new Vertex(v1.x, v1.y, v1.z, true), new Vertex(v2.x, v2.y, v2.z, true), new Vertex(v3.x, v3.y, v3.z, true)};
                        }
                        vertexMap.put(gd, verts);
                        texmapDataInOrder.add(new GDataAndTexture(gd, localWinding, globalNegativeDeterminant, globalInvertNext, accumClip));
                        if (texmapNext && !parseTexmapSubfile) {
                            texmap = false;
                            texmapNext = false;
                        }
                    }
                } else {
                    verts = triangles.get(gd3);
                    if (verts != null) {
                        vertexMap.put(gd, verts);
                        dataInOrder.add(new GDataAndWinding(gd, localWinding, globalNegativeDeterminant, globalInvertNext, accumClip));
                    }
                }
                continue;
            case 4:
                if (texmap) {
                    GData4 gd4 = (GData4) gd;
                    verts = quads.get(gd4);
                    if (verts == null) {
                        v1.set(gd4.x1, gd4.y1, gd4.z1, 1f);
                        v2.set(gd4.x2, gd4.y2, gd4.z2, 1f);
                        v3.set(gd4.x3, gd4.y3, gd4.z3, 1f);
                        v4.set(gd4.x4, gd4.y4, gd4.z4, 1f);
                        Matrix4f m = gd4.parent.productMatrix;
                        Matrix4f.transform(m, v1, v1);
                        Matrix4f.transform(m, v2, v2);
                        Matrix4f.transform(m, v3, v3);
                        Matrix4f.transform(m, v4, v4);
                        verts = new Vertex[]{new Vertex(v1.x, v1.y, v1.z, true), new Vertex(v2.x, v2.y, v2.z, true), new Vertex(v3.x, v3.y, v3.z, true), new Vertex(v4.x, v4.y, v4.z, true)};
                    }
                    vertexMap.put(gd, verts);
                    texmapDataInOrder.add(new GDataAndTexture(gd, localWinding, globalNegativeDeterminant, globalInvertNext, accumClip));
                    if (texmapNext && !parseTexmapSubfile) {
                        texmap = false;
                        texmapNext = false;
                    }
                } else {
                    verts = quads.get(gd);
                    if (verts != null) {
                        vertexMap.put(gd, verts);
                        dataInOrder.add(new GDataAndWinding(gd, localWinding, globalNegativeDeterminant, globalInvertNext, accumClip));
                    }
                }
                continue;
            case 5:
                if (texmapNext && !parseTexmapSubfile) {
                    texmap = false;
                    texmapNext = false;
                }
                verts = condlines.get(gd);
                if (verts != null) {
                    vertexMap.put(gd, verts);
                    dataInOrder.add(new GDataAndWinding(gd, localWinding, globalNegativeDeterminant, globalInvertNext, accumClip));
                }
                continue;
            case 9:
                if (!parseTexmap || parseTexmapSubfile) {
                    continue;
                }
                GDataTEX tex = (GDataTEX) gd;
                hasTEXMAP = true;
                switch (tex.meta) {
                case START:
                    texmap = true;
                    texmapDataInOrder.add(new GDataAndTexture(gd, localWinding, globalNegativeDeterminant, globalInvertNext, accumClip));
                    break;
                case NEXT:
                    texmapNext = true;
                    texmap = true;
                    texmapDataInOrder.add(new GDataAndTexture(gd, localWinding, globalNegativeDeterminant, globalInvertNext, accumClip));
                    break;
                case GEOMETRY:
                    // Shouldn't happen...
                    break;
                case FALLBACK:
                    while ((backup = backup.next) != null && !(backup instanceof GDataTEX gdtex && gdtex.meta == TexMeta.END));
                    texmap = false;
                    break;
                case END:
                    // Won't happen...
                    break;
                }
                continue;
            default:
                continue;
            }
        }

        // If there is TEXMAP then render all transparent surfaces with the TEXMAP renderer
        // for most accurate OIT rendering.
        if (hasTEXMAP) {
            for (Iterator<GDataAndWinding> iterator = dataInOrder.iterator(); iterator.hasNext();) {
                GDataAndWinding gaw = iterator.next();
                if (gaw.data instanceof GData3 gd3 && gd3.a < 1f
                 || gaw.data instanceof GData4 gd4 && gd4.a < 1f) {
                    texmapDataInOrder.add(new GDataAndTexture(gaw));
                    iterator.remove();
                }
            }
        }

        return hasTEXMAP;
    }

    private void normal(int offset, int times, float xn, float yn, float zn,
            float[] vertexData, int i) {
        for (int j = 0; j < times; j++) {
            int pos = (offset + i + j) * 12;
            vertexData[pos + 3] = xn;
            vertexData[pos + 4] = yn;
            vertexData[pos + 5] = zn;
        }
    }

    private void colourise(int offset, int times, float r, float g, float b,
            float a, float[] vertexData, int i) {
        for (int j = 0; j < times; j++) {
            int pos = (offset + i + j) * 12;
            vertexData[pos + 6] = r;
            vertexData[pos + 7] = g;
            vertexData[pos + 8] = b;
            vertexData[pos + 9] = a;
        }
    }
    
    private void uv(int offset, float u, float v, float[] vertexData, int i) {
        int pos = (offset + i) * 12;
        vertexData[pos + 10] = u;
        vertexData[pos + 11] = v;
    }

    private void colourise7(int offset, int times, float r, float g, float b,
            float a, float[] vertexData, int i) {
        for (int j = 0; j < times; j++) {
            int pos = (offset + i + j) * 7;
            vertexData[pos + 3] = r;
            vertexData[pos + 4] = g;
            vertexData[pos + 5] = b;
            vertexData[pos + 6] = a;
        }
    }

    private void colourise15(int offset, int times, float r, float g, float b,
            float[] vertexData, int i) {
        for (int j = 0; j < times; j++) {
            int pos = (offset + i + j) * 15;
            vertexData[pos + 12] = r;
            vertexData[pos + 13] = g;
            vertexData[pos + 14] = b;
        }
    }

    private void pointAt(int offset, float x, float y, float z,
            float[] vertexData, int i) {
        int pos = (offset + i) * 12;
        vertexData[pos] = x;
        vertexData[pos + 1] = y;
        vertexData[pos + 2] = z;
    }

    private void pointAt7(int offset, float x, float y, float z,
            float[] vertexData, int i) {
        int pos = (offset + i) * 7;
        vertexData[pos] = x;
        vertexData[pos + 1] = y;
        vertexData[pos + 2] = z;
    }

    private void pointAt15(int offset, float x, float y, float z,
            float[] vertexData, int i) {
        int pos = (offset + i) * 15;
        vertexData[pos] = x;
        vertexData[pos + 1] = y;
        vertexData[pos + 2] = z;
    }

    private void controlPointAt15(int offset, int offset2, float x, float y, float z,
            float[] vertexData, int i) {
        int pos = (offset + i) * 15 + 3 * offset2;
        vertexData[pos + 3] = x;
        vertexData[pos + 4] = y;
        vertexData[pos + 5] = z;
    }
}
