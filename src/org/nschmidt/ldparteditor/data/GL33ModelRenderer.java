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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
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
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.Manipulator;
import org.nschmidt.ldparteditor.helpers.StudLogo;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.opengl.GLMatrixStack;
import org.nschmidt.ldparteditor.opengl.GLShader;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer33;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;

/**
 * New OpenGL 3.3 high performance render function for the model (VAO accelerated)
 */
public class GL33ModelRenderer {

    private static Set<String> filesWithLogo1 = new HashSet<String>();
    private static Set<String> filesWithLogo2 = new HashSet<String>();

    static {
        filesWithLogo1.add("STUD.DAT"); //$NON-NLS-1$
        filesWithLogo1.add("STUD.dat"); //$NON-NLS-1$
        filesWithLogo1.add("stud.dat"); //$NON-NLS-1$
        filesWithLogo2.add("STUD2.DAT"); //$NON-NLS-1$
        filesWithLogo2.add("STUD2.dat"); //$NON-NLS-1$
        filesWithLogo2.add("stud2.dat"); //$NON-NLS-1$
    }

    boolean isPaused = false;

    private final Composite3D c3d;
    private final OpenGLRenderer33 renderer;

    public GL33ModelRenderer(Composite3D c3d, OpenGLRenderer33 renderer) {
        this.c3d = c3d;
        this.renderer = renderer;
    }

    // FIXME needs concept implementation!
    // |
    // --v Here I try to use only one(!) VAO for the price of letting an asynchronous thread doing the buffer data generation!

    // This is super-fast!
    // However, TEXMAP/!LPE PNG will require a multi-VAO solution (all non-TEXMAP/PNG stuff can still be rendered with one VAO).

    private int vao;
    private int vbo;

    private int vaoLines;
    private int vboLines;

    private int vaoTempLines;
    private int vboTempLines;

    private int vaoVertices;
    private int vboVertices;

    private int vaoSelectionLines;
    private int vboSelectionLines;

    private int vaoCondlines;
    private int vboCondlines;

    private int vaoCSG;
    private int vboCSG;

    private int vaoStudLogo1;
    private int vboStudLogo1;

    private int vaoStudLogo2;
    private int vboStudLogo2;

    private volatile Lock lock = new ReentrantLock();
    private static volatile Lock static_lock = new ReentrantLock();
    private static volatile AtomicInteger idGen = new AtomicInteger(0);
    private static volatile AtomicInteger idCount = new AtomicInteger(0);

    private static volatile CopyOnWriteArrayList<Integer> idList = new CopyOnWriteArrayList<>();
    private volatile AtomicBoolean calculateCSG = new AtomicBoolean(true);

    private volatile AtomicBoolean calculateCondlineControlPoints = new AtomicBoolean(true);
    private volatile TreeSet<Vertex> pureCondlineControlPoints = new TreeSet<>();
    private volatile float[] dataTriangles = null;
    private volatile float[] dataLines = new float[]{0f};
    private volatile float[] dataTempLines = new float[]{0f};
    private volatile float[] dataVertices = null;
    private volatile float[] dataCondlines = new float[]{0f};
    private volatile float[] dataSelectionLines = new float[]{0f};
    private volatile float[] dataCSG = new float[]{0f};
    private volatile float[] dataSelectionCSG = new float[]{0f};
    private volatile int solidTriangleSize = 0;
    private volatile int transparentTriangleOffset = 0;
    private volatile int transparentTriangleSize = 0;
    private volatile int solidCSGsize = 0;
    private volatile int selectionCSGsize = 0;
    private volatile int transparentCSGoffset = 0;
    private volatile int transparentCSGsize = 0;
    private volatile int lineSize = 0;
    private volatile int tempLineSize = 0;
    private volatile int vertexSize = 0;
    private volatile int condlineSize = 0;
    private volatile int selectionSize = 0;

    private volatile ArrayList<GDataPNG> images = new ArrayList<>();
    private volatile ArrayList<GData2> distanceMeters = new ArrayList<>();
    private volatile ArrayList<GData3> protractors = new ArrayList<>();
    private volatile HashMap<GData, Vertex[]> sharedVertexMap = new HashMap<>();

    private volatile boolean usesCSG = false;

    private volatile ArrayList<Matrix4f> stud1_Matrices = new ArrayList<>();
    private volatile ArrayList<Matrix4f> stud2_Matrices = new ArrayList<>();

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

        vaoCSG = GL30.glGenVertexArrays();
        vboCSG = GL15.glGenBuffers();
        GL30.glBindVertexArray(vaoCSG);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboCSG);

        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, (3 + 3 + 4) * 4, 0);

        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, (3 + 3 + 4) * 4, 3 * 4);

        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, (3 + 3 + 4) * 4, (3 + 3) * 4);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        vaoLines = GL30.glGenVertexArrays();
        vboLines = GL15.glGenBuffers();
        GL30.glBindVertexArray(vaoLines);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboLines);

        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, (3 + 4) * 4, 0);

        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, (3 + 4) * 4, 3 * 4);

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
        GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, (3 + 4) * 4, 3 * 4);

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
        GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, (3 + 4) * 4, 3 * 4);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        vaoTempLines = GL30.glGenVertexArrays();
        vboTempLines = GL15.glGenBuffers();
        GL30.glBindVertexArray(vaoTempLines);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboTempLines);

        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, (3 + 4) * 4, 0);

        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, (3 + 4) * 4, 3 * 4);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        vaoVertices = GL30.glGenVertexArrays();
        vboVertices = GL15.glGenBuffers();
        GL30.glBindVertexArray(vaoVertices);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboVertices);

        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, (3 + 4) * 4, 0);

        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, (3 + 4) * 4, 3 * 4);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        vaoSelectionLines = GL30.glGenVertexArrays();
        vboSelectionLines = GL15.glGenBuffers();
        GL30.glBindVertexArray(vaoSelectionLines);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboSelectionLines);

        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, (3 + 4) * 4, 0);

        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, (3 + 4) * 4, 3 * 4);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        vaoCondlines = GL30.glGenVertexArrays();
        vboCondlines = GL15.glGenBuffers();
        GL30.glBindVertexArray(vaoCondlines);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboCondlines);

        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 15 * 4, 0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 15 * 4, 3 * 4);
        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 15 * 4, 6 * 4);
        GL20.glEnableVertexAttribArray(3);
        GL20.glVertexAttribPointer(3, 3, GL11.GL_FLOAT, false, 15 * 4, 9 * 4);
        GL20.glEnableVertexAttribArray(4);
        GL20.glVertexAttribPointer(4, 3, GL11.GL_FLOAT, false, 15 * 4, 12 * 4);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        new Thread(new Runnable() {
            @Override
            public void run() {

                float[] normal;

                final Vector4f Nv = new Vector4f(0, 0, 0, 1f);
                final Matrix4f Mm = new Matrix4f();
                Matrix4f.setIdentity(Mm);

                final Set<GDataCSG> oldCsgData = new HashSet<>();
                final Set<GData> selectionSet = new HashSet<GData>();
                final Set<GData> hiddenSet = new HashSet<GData>();
                final ArrayList<GDataAndWinding> dataInOrder = new ArrayList<>();
                final HashMap<GData, Vertex[]> vertexMap = new HashMap<>();
                final HashMap<GData, Vertex[]> vertexMap2 = new HashMap<>();
                final HashMap<GData, float[]> normalMap = new HashMap<>();
                final ThreadsafeHashMap<GData1, Matrix4f> CACHE_viewByProjection = new ThreadsafeHashMap<GData1, Matrix4f>(1000);
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

                    final int renderMode = c3d.getRenderMode();

                    // Skip render mode 5
                    if (renderMode != 5) try {
                        static_lock.lock();
                        // final long start = System.currentTimeMillis();

                        // First we have to get links to the sets from the model
                        final DatFile df = c3d.getLockableDatFileReference();
                        // Just to speed up things in some cases...
                        if (df == null || !df.isDrawSelection()) {
                            continue; // static_lock.unlock(); on finally
                        }
                        final VertexManager vm = df.getVertexManager();
                        final Lock maniLock = vm.getManifestationLock();
                        final Set<GData> mainFileContent = vm.lineLinkedToVertices.keySet();
                        // For the declared vertices, we have to use shallow copy
                        maniLock.lock();
                        final List<Vertex> vertices = new ArrayList<>(vm.vertexLinkedToPositionInFile.size());
                        vertices.addAll(vm.vertexLinkedToPositionInFile.keySet());
                        maniLock.unlock();

                        if (calculateCondlineControlPoints.compareAndSet(true, false)) {
                            CompletableFuture.runAsync( () -> {
                                final TreeSet<Vertex> tmpPureCondlineControlPoints = new TreeSet<>();
                                for (Vertex v : vertices) {
                                    Set<VertexManifestation> manis = vm.vertexLinkedToPositionInFile.get(v);
                                    if (manis != null) {
                                        boolean pureControlPoint = true;
                                        maniLock.lock();
                                        for (VertexManifestation m : manis) {
                                            if (m.getPosition() < 2 || m.getGdata().type() != 5) {
                                                pureControlPoint = false;
                                                break;
                                            }
                                        }
                                        maniLock.unlock();
                                        if (pureControlPoint) {
                                            tmpPureCondlineControlPoints.add(v);
                                        }
                                    }
                                }
                                pureCondlineControlPoints = tmpPureCondlineControlPoints;
                                calculateCondlineControlPoints.set(true);
                            });
                        }

                        // The links are sufficient
                        final Set<GData> selectedData = vm.selectedData;
                        final Set<Vertex> selectedVertices = vm.selectedVertices;
                        final ThreadsafeHashMap<GData, Set<VertexInfo>> ltv = vm.lineLinkedToVertices;
                        Set<Vertex> tmpSelectedVertices = null;
                        TreeMap<Integer, ArrayList<Integer>> smoothVertexAdjacency = null;
                        TreeMap<Vertex, Integer> smoothVertexIndmap = null;
                        final Set<Vertex> hiddenVertices = vm.hiddenVertices;
                        final ThreadsafeHashMap<GData2, Vertex[]> lines = vm.lines;
                        final ThreadsafeHashMap<GData3, Vertex[]> triangles = vm.triangles;
                        final ThreadsafeHashMap<GData4, Vertex[]> quads = vm.quads;
                        final ThreadsafeHashMap<GData5, Vertex[]> condlines = vm.condlines;
                        final ArrayList<GDataCSG> csgData = new ArrayList<>();
                        final boolean drawStudLogo = c3d.isShowingLogo();
                        final ArrayList<GDataPNG> pngImages = new ArrayList<>();
                        final ArrayList<GData2> tmpDistanceMeters = new ArrayList<>();
                        final ArrayList<GData3> tmpProtractors = new ArrayList<>();
                        final HashSet<GData> dataToRemove = new HashSet<>(vertexMap.keySet());
                        final boolean drawWireframe = renderMode == -1;

                        // Build the list of the data from the datfile
                        dataInOrder.clear();
                        normalMap.clear();
                        selectionSet.clear();
                        hiddenSet.clear();
                        CACHE_viewByProjection.clear();

                        usesCSG = load_BFC_info(
                                dataInOrder, csgData, vertexMap, matrixMap, df,
                                lines, triangles, quads, condlines, drawStudLogo,
                                pngImages, tmpDistanceMeters, tmpProtractors);

                        final boolean smoothShading = c3d.isSmoothShading() && !drawWireframe;
                        final HashMap<GData, Vector3f[]> vertexNormals;
                        if (smoothShading) {
                            // MARK Calculate normals here...
                            vertexNormals = new HashMap<>();
                            final ArrayList<GDataAndWinding> data;
                            data = dataInOrder;
                            final HashMap<GData, Vector3f> surfaceNormals = new HashMap<>();
                            for (GDataAndWinding gw : data) {
                                final GData gd = gw.data;
                                Vector3f normalv = null;
                                if (gd.type() == 3) {
                                    GData3 gd3 = (GData3) gd;
                                    Nv.x = gd3.xn;
                                    Nv.y = gd3.yn;
                                    Nv.z = gd3.zn;
                                    Nv.w = 1f;
                                    Matrix4f loc = matrixMap.get(gd3.parent);
                                    Matrix4f.transform(loc, Nv, Nv);
                                    normalv = new Vector3f(Nv.x, Nv.y, Nv.z);
                                } else if (gd.type() == 4) {
                                    GData4 gd4 = (GData4) gd;
                                    Nv.x = gd4.xn;
                                    Nv.y = gd4.yn;
                                    Nv.z = gd4.zn;
                                    Nv.w = 1f;
                                    Matrix4f loc = matrixMap.get(gd4.parent);
                                    Matrix4f.transform(loc, Nv, Nv);
                                    normalv = new Vector3f(Nv.x, Nv.y, Nv.z);
                                }
                                if (normalv != null) {
                                    // Flip the normal in case of determinant or INVERTNEXT
                                    if (renderMode > 3 && renderMode < 6) {
                                        if (!(gw.winding == BFC.CCW ^ gw.invertNext)) {
                                            normalv.negate();
                                        }
                                    } else if (!gw.negativeDeterminant) {
                                        normalv.negate();
                                    }
                                    surfaceNormals.put(gd, normalv);
                                }
                            }
                            surfaceNormals.values().parallelStream().forEach((n) -> {
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
                            vertexNormals.values().parallelStream().forEach((normals) -> {
                                for (Vector3f n : normals) if (n.lengthSquared() > 0f) n.normalise();
                            });
                        } else {
                            vertexNormals = null;
                        }

                        // CSG Selection
                        int csgSelectionVertexSize = 0;
                        final float[] tmpCsgSelectionData;
                        {
                            int csgSelectionIndex = 0;
                            HashSet<GData3> selection = GDataCSG.getSelectionData(df);
                            csgSelectionVertexSize += selection.size() * 6;
                            tmpCsgSelectionData = new float[csgSelectionVertexSize * 7];
                            for (GData3 gd3 : selection) {
                                pointAt7(0, gd3.x1, gd3.y1, gd3.z1, tmpCsgSelectionData, csgSelectionIndex);
                                pointAt7(1, gd3.x2, gd3.y2, gd3.z2, tmpCsgSelectionData, csgSelectionIndex);
                                pointAt7(2, gd3.x3, gd3.y3, gd3.z3, tmpCsgSelectionData, csgSelectionIndex);
                                pointAt7(3, gd3.x1, gd3.y1, gd3.z1, tmpCsgSelectionData, csgSelectionIndex);
                                pointAt7(4, gd3.x3, gd3.y3, gd3.z3, tmpCsgSelectionData, csgSelectionIndex);
                                pointAt7(5, gd3.x2, gd3.y2, gd3.z2, tmpCsgSelectionData, csgSelectionIndex);
                                colourise7(0, 6, View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0], 7f, tmpCsgSelectionData, csgSelectionIndex);
                                csgSelectionIndex += 6;
                            }
                            if (csgSelectionIndex > 0) {
                                csgSelectionIndex -= 6;
                            }
                        }

                        if (calculateCSG.compareAndSet(true, false)) {
                            boolean modified2 = true;
                            if (!c3d.getManipulator().isModified()) {
                                modified2 = false;
                                for (GDataCSG csg : csgData) {
                                    if (!oldCsgData.contains(csg)) {
                                        modified2 = true;
                                        oldCsgData.clear();
                                        oldCsgData.addAll(csgData);
                                    }
                                }
                            }
                            final boolean modified = modified2;
                            final ArrayList<GDataCSG> csgData2 = csgData;
                            CompletableFuture.runAsync( () -> {
                                // Do asynchronous CSG calculations here...
                                int csgDataSize = 0;
                                int csgSolidVertexCount = 0;
                                int csgTransVertexCount = 0;

                                try {
                                    GDataCSG.static_lock.lock();
                                    if (modified) GDataCSG.resetCSG(df, true);
                                    // GDataCSG.forceRecompile(df); // <- Check twice if this is really necessary!
                                    for (GDataCSG csg : csgData2) {
                                        if (modified) csg.drawAndParse(c3d, df, false);
                                        csg.cacheResult(df);
                                        final int[] size = csg.getDataSize();
                                        csgDataSize += size[0];
                                        csgSolidVertexCount += size[1];
                                        csgTransVertexCount += size[2];
                                    }
                                    GDataCSG.rebuildSelection(df);
                                    int csgIndex = 0;
                                    int transparentCSGindex = csgSolidVertexCount;
                                    float[] tmpCsgData = new float[csgDataSize];
                                    // Fill array here!
                                    final Vector4f[] v = new Vector4f[]{
                                            new Vector4f(0f, 0f, 0f, 1f),
                                            new Vector4f(0f, 0f, 0f, 1f),
                                            new Vector4f(0f, 0f, 0f, 1f),
                                            new Vector4f(0f, 0f, 0f, 1f)
                                    };

                                    for (GDataCSG csg : csgData2) {
                                        for (GData3 gd3 : csg.getSurfaces()) {
                                            final boolean transparent = gd3.a < 1f;
                                            int tempIndex = csgIndex;
                                            if (transparent) {
                                                tempIndex = transparentCSGindex;
                                            }
                                            if (csg.parent == View.DUMMY_REFERENCE) {
                                                final float xn = gd3.xn;
                                                final float yn = gd3.yn;
                                                final float zn = gd3.zn;
                                                pointAt(0, gd3.x1, gd3.y1, gd3.z1, tmpCsgData, tempIndex);
                                                pointAt(1, gd3.x2, gd3.y2, gd3.z2, tmpCsgData, tempIndex);
                                                pointAt(2, gd3.x3, gd3.y3, gd3.z3, tmpCsgData, tempIndex);
                                                pointAt(3, gd3.x1, gd3.y1, gd3.z1, tmpCsgData, tempIndex);
                                                pointAt(4, gd3.x3, gd3.y3, gd3.z3, tmpCsgData, tempIndex);
                                                pointAt(5, gd3.x2, gd3.y2, gd3.z2, tmpCsgData, tempIndex);
                                                normal(0, 3, -xn, -yn, -zn, tmpCsgData, tempIndex);
                                                normal(3, 3, xn, yn, zn, tmpCsgData, tempIndex);
                                            } else {
                                                final Matrix4f m = csg.parent.productMatrix;

                                                v[0].x = gd3.x1;
                                                v[0].y = gd3.y1;
                                                v[0].z = gd3.z1;
                                                v[0].w = 1f;
                                                v[1].x = gd3.x2;
                                                v[1].y = gd3.y2;
                                                v[1].z = gd3.z2;
                                                v[1].w = 1f;
                                                v[2].x = gd3.x3;
                                                v[2].y = gd3.y3;
                                                v[2].z = gd3.z3;
                                                v[2].w = 1f;
                                                v[3].x = gd3.xn;
                                                v[3].y = gd3.yn;
                                                v[3].z = gd3.zn;
                                                v[3].w = 1f;
                                                Matrix4f.transform(m, v[0], v[0]);
                                                Matrix4f.transform(m, v[1], v[1]);
                                                Matrix4f.transform(m, v[2], v[2]);
                                                Matrix4f.transform(m, v[3], v[3]);
                                                // No normalization necessary (the shader does the job)!
                                                final float xn = v[3].x;
                                                final float yn = v[3].y;
                                                final float zn = v[3].z;
                                                pointAt(0, v[0].x, v[0].y, v[0].z, tmpCsgData, tempIndex);
                                                pointAt(1, v[1].x, v[1].y, v[1].z, tmpCsgData, tempIndex);
                                                pointAt(2, v[2].x, v[2].y, v[2].z, tmpCsgData, tempIndex);
                                                pointAt(3, v[0].x, v[0].y, v[0].z, tmpCsgData, tempIndex);
                                                pointAt(4, v[2].x, v[2].y, v[2].z, tmpCsgData, tempIndex);
                                                pointAt(5, v[1].x, v[1].y, v[1].z, tmpCsgData, tempIndex);
                                                if (m.determinant() < 0f) {
                                                    normal(0, 3, xn, yn, zn, tmpCsgData, tempIndex);
                                                    normal(3, 3, -xn, -yn, -zn, tmpCsgData, tempIndex);
                                                } else {
                                                    normal(0, 3, -xn, -yn, -zn, tmpCsgData, tempIndex);
                                                    normal(3, 3, xn, yn, zn, tmpCsgData, tempIndex);
                                                }
                                            }
                                            colourise(0, 6, gd3.r, gd3.g, gd3.b, gd3.a, tmpCsgData, tempIndex);
                                            if (transparent) {
                                                transparentCSGindex += 6;
                                            } else {
                                                csgIndex += 6;
                                            }
                                        }
                                    }
                                    lock.lock();
                                    dataCSG = tmpCsgData;
                                    solidCSGsize= csgIndex;
                                    transparentCSGoffset = csgIndex;
                                    transparentCSGsize = csgTransVertexCount;
                                    lock.unlock();
                                } catch (Exception ex) {
                                    NLogger.error(getClass(), ex);
                                } finally {
                                    GDataCSG.static_lock.unlock();
                                    calculateCSG.set(true);
                                }
                            });
                        }

                        final boolean smoothVertices = OpenGLRenderer.getSmoothing().get();
                        if (smoothVertices) {
                            tmpSelectedVertices = new TreeSet<Vertex>();
                            for (Vertex vertex : vertices) {
                                if (selectedVertices.contains(vertex)) {
                                    tmpSelectedVertices.add(vertex);
                                }
                            }
                        }
                        final Object[] smoothObj = smoothVertices ? vm.getSmoothedVertices(tmpSelectedVertices) : null;
                        final int lineMode = c3d.getLineMode();
                        final boolean meshLines = c3d.isMeshLines();
                        final boolean subfileMeshLines = c3d.isSubMeshLines();
                        final boolean condlineMode = renderMode == 6;
                        final boolean hideCondlines = !condlineMode && lineMode > 1;
                        final boolean hideLines = !condlineMode && lineMode > 2;
                        final float zoom = c3d.getZoom();
                        final Matrix4f viewport = c3d.getViewport();
                        final Manipulator manipulator = c3d.getManipulator();
                        final Matrix4f transform = manipulator.getTempTransformation4f();
                        final boolean isTransforming = manipulator.isModified();
                        final boolean moveAdjacentData = Editor3DWindow.getWindow().isMovingAdjacentData();

                        final ArrayList<Matrix4f> stud1Matrices;
                        final ArrayList<Matrix4f> stud2Matrices;

                        if (drawStudLogo) {
                            stud1Matrices = new ArrayList<>();
                            stud2Matrices = new ArrayList<>();
                        } else {
                            stud1Matrices = null;
                            stud2Matrices = null;
                        }

                        int local_triangleSize = 0;
                        int local_lineSize = 0;
                        int local_condlineSize = 0;
                        int local_tempLineSize = 0;
                        @SuppressWarnings("unchecked")
                        int local_verticesSize = vertices.size() + (smoothVertices ? ((ArrayList<Vertex>) smoothObj[0]).size() : 0);
                        int local_selectionLineSize = 0;

                        int triangleVertexCount = 0;
                        int transparentTriangleVertexCount = 0;
                        int lineVertexCount = 0;
                        int condlineVertexCount = 0;
                        int tempLineVertexCount = 0;
                        int selectionLineVertexCount = 0;

                        if (smoothVertices) {
                            @SuppressWarnings("unchecked")
                            TreeMap<Integer, ArrayList<Integer>> tmpAdjacency = (TreeMap<Integer, ArrayList<Integer>>) smoothObj[2];
                            @SuppressWarnings("unchecked")
                            TreeMap<Vertex, Integer> tmpIndex = (TreeMap<Vertex, Integer>) smoothObj[1];
                            smoothVertexAdjacency = tmpAdjacency;
                            smoothVertexIndmap = tmpIndex;
                            for (ArrayList<Integer> lst : smoothVertexAdjacency.values()) {
                                final int size = lst.size();
                                local_selectionLineSize += 14 * size;
                                selectionLineVertexCount += 2 * size;
                            }
                        }

                        // Pre-compute vertex transformations for Move Adjacent Data
                        TreeMap<Vertex, Vector4f> transformedVerts = null;
                        ArrayList<Vertex> transformedVertices = null;
                        if (isTransforming && moveAdjacentData) {
                            transformedVerts = new TreeMap<>();
                            transformedVertices = new ArrayList<>();
                            for (Vertex v : vertices) {
                                if (selectedVertices.contains(v)) {
                                    if (!transformedVerts.containsKey(v)) {
                                        Vector4f tv = Matrix4f.transform(transform, v.toVector4f(), new Vector4f());
                                        transformedVerts.put(v, tv);
                                        transformedVertices.add(new Vertex(tv.x, tv.y, tv.z, true));
                                    }
                                }
                            }
                            for (GDataAndWinding gw : dataInOrder) {
                                final GData gd = gw.data;
                                if (selectedData.contains(gd)) {
                                    Vertex[] verts = vertexMap.get(gd);
                                    if (verts != null) {
                                        for (Vertex v : verts) {
                                            if (!transformedVerts.containsKey(v)) {
                                                Vector4f tv = Matrix4f.transform(transform, v.toVector4f(), new Vector4f());
                                                transformedVerts.put(v, tv);
                                                transformedVertices.add(new Vertex(tv.x, tv.y, tv.z, true));
                                            }
                                        }
                                    }
                                }
                            }
                            local_verticesSize += transformedVertices.size();
                        }

                        // Only do "heavy" CPU condline computing with the special condline mode
                        // (if the condline was not shown before)
                        if (condlineMode) {
                            dataInOrder.parallelStream().forEach((GDataAndWinding gw) -> {
                                GData gd = gw.data;
                                if (gd.type() == 5) {
                                    ((GData5) gd).isShown(viewport, CACHE_viewByProjection, zoom);
                                }
                            });
                        }

                        // Calculate the buffer sizes
                        // Lines are never transparent!
                        for (GDataAndWinding gw : dataInOrder) {

                            final GData gd = gw.data;
                            final boolean selected = selectedData.contains(gd);
                            final int type = gd.type();

                            dataToRemove.remove(gd);

                            // If anything is transformed, transform it here
                            // and update the vertex positions (vertexMap) and normals for it (normalMap)
                            if (isTransforming && type > 1) {
                                if (moveAdjacentData) {
                                    if (selected || ltv.containsKey(gd)) {
                                        boolean needNormal = false;
                                        Vertex[] verts = vertexMap.get(gd);
                                        Vertex[] nverts = new Vertex[verts.length];
                                        for (int i = 0; i < verts.length; i++) {
                                            Vector4f v = transformedVerts.getOrDefault(verts[i], verts[i].toVector4fm());
                                            needNormal = needNormal || verts[i].toVector4fm() != v;
                                            nverts[i] = new Vertex(v.x, v.y, v.z, true);
                                        }
                                        vertexMap.put(gd, nverts);
                                        if (needNormal) {
                                            switch (type) {
                                            case 3:
                                            {
                                                float xn = (nverts[2].y - nverts[0].y) * (nverts[1].z - nverts[0].z) - (nverts[2].z - nverts[0].z) * (nverts[1].y - nverts[0].y);
                                                float yn = (nverts[2].z - nverts[0].z) * (nverts[1].x - nverts[0].x) - (nverts[2].x - nverts[0].x) * (nverts[1].z - nverts[0].z);
                                                float zn = (nverts[2].x - nverts[0].x) * (nverts[1].y - nverts[0].y) - (nverts[2].y - nverts[0].y) * (nverts[1].x - nverts[0].x);
                                                normalMap.put(gd, new float[]{
                                                        xn, yn, zn,
                                                        xn, yn, zn,
                                                        xn, yn, zn});
                                            }
                                            break;
                                            case 4:
                                            {
                                                final Vector3f[] normals = new Vector3f[] { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
                                                {
                                                    final Vector3f[] lineVectors = new Vector3f[] { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
                                                    Vector3f.sub(new Vector3f(nverts[1].x, nverts[1].y, nverts[1].z), new Vector3f(nverts[0].x, nverts[0].y, nverts[0].z), lineVectors[0]);
                                                    Vector3f.sub(new Vector3f(nverts[2].x, nverts[2].y, nverts[2].z), new Vector3f(nverts[1].x, nverts[1].y, nverts[1].z), lineVectors[1]);
                                                    Vector3f.sub(new Vector3f(nverts[3].x, nverts[3].y, nverts[3].z), new Vector3f(nverts[2].x, nverts[2].y, nverts[2].z), lineVectors[2]);
                                                    Vector3f.sub(new Vector3f(nverts[0].x, nverts[0].y, nverts[0].z), new Vector3f(nverts[3].x, nverts[3].y, nverts[3].z), lineVectors[3]);
                                                    Vector3f.cross(lineVectors[0], lineVectors[1], normals[0]);
                                                    Vector3f.cross(lineVectors[1], lineVectors[2], normals[1]);
                                                    Vector3f.cross(lineVectors[2], lineVectors[3], normals[2]);
                                                    Vector3f.cross(lineVectors[3], lineVectors[0], normals[3]);
                                                }
                                                Vector3f quadNormal = new Vector3f();
                                                for (int i = 0; i < 4; i++) {
                                                    Vector3f.add(normals[i], quadNormal, quadNormal);
                                                }
                                                quadNormal.negate();
                                                normalMap.put(gd, new float[]{
                                                        quadNormal.x, quadNormal.y, quadNormal.z,
                                                        quadNormal.x, quadNormal.y, quadNormal.z,
                                                        quadNormal.x, quadNormal.y, quadNormal.z,
                                                        quadNormal.x, quadNormal.y, quadNormal.z});
                                            }
                                            break;
                                            default:
                                                break;
                                            }
                                        }
                                    }
                                } else if (selected) {
                                    Vertex[] verts = vertexMap.get(gd);
                                    Vertex[] nverts = new Vertex[verts.length];
                                    for (int i = 0; i < verts.length; i++) {
                                        Vector4f v = Matrix4f.transform(transform, verts[i].toVector4f(), new Vector4f());
                                        nverts[i] = new Vertex(v.x, v.y, v.z, true);
                                    }
                                    vertexMap.put(gd, nverts);
                                    switch (type) {
                                    case 3:
                                    {
                                        float xn = (nverts[2].y - nverts[0].y) * (nverts[1].z - nverts[0].z) - (nverts[2].z - nverts[0].z) * (nverts[1].y - nverts[0].y);
                                        float yn = (nverts[2].z - nverts[0].z) * (nverts[1].x - nverts[0].x) - (nverts[2].x - nverts[0].x) * (nverts[1].z - nverts[0].z);
                                        float zn = (nverts[2].x - nverts[0].x) * (nverts[1].y - nverts[0].y) - (nverts[2].y - nverts[0].y) * (nverts[1].x - nverts[0].x);
                                        normalMap.put(gd, new float[]{
                                                xn, yn, zn,
                                                xn, yn, zn,
                                                xn, yn, zn});
                                    }
                                    break;
                                    case 4:
                                    {
                                        final Vector3f[] normals = new Vector3f[] { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
                                        {
                                            final Vector3f[] lineVectors = new Vector3f[] { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
                                            Vector3f.sub(new Vector3f(nverts[1].x, nverts[1].y, nverts[1].z), new Vector3f(nverts[0].x, nverts[0].y, nverts[0].z), lineVectors[0]);
                                            Vector3f.sub(new Vector3f(nverts[2].x, nverts[2].y, nverts[2].z), new Vector3f(nverts[1].x, nverts[1].y, nverts[1].z), lineVectors[1]);
                                            Vector3f.sub(new Vector3f(nverts[3].x, nverts[3].y, nverts[3].z), new Vector3f(nverts[2].x, nverts[2].y, nverts[2].z), lineVectors[2]);
                                            Vector3f.sub(new Vector3f(nverts[0].x, nverts[0].y, nverts[0].z), new Vector3f(nverts[3].x, nverts[3].y, nverts[3].z), lineVectors[3]);
                                            Vector3f.cross(lineVectors[0], lineVectors[1], normals[0]);
                                            Vector3f.cross(lineVectors[1], lineVectors[2], normals[1]);
                                            Vector3f.cross(lineVectors[2], lineVectors[3], normals[2]);
                                            Vector3f.cross(lineVectors[3], lineVectors[0], normals[3]);
                                        }
                                        Vector3f quadNormal = new Vector3f();
                                        for (int i = 0; i < 4; i++) {
                                            Vector3f.add(normals[i], quadNormal, quadNormal);
                                        }
                                        quadNormal.negate();
                                        normalMap.put(gd, new float[]{
                                                quadNormal.x, quadNormal.y, quadNormal.z,
                                                quadNormal.x, quadNormal.y, quadNormal.z,
                                                quadNormal.x, quadNormal.y, quadNormal.z,
                                                quadNormal.x, quadNormal.y, quadNormal.z});
                                    }
                                    break;
                                    default:
                                        break;
                                    }
                                }
                            }

                            // Calculate the buffer size for selected objects
                            if (selected) {
                                selectionSet.add(gd);

                                switch (type) {
                                case 2:
                                    local_selectionLineSize += 14;
                                    selectionLineVertexCount += 2;
                                    break;
                                case 3:
                                    local_selectionLineSize += 42;
                                    selectionLineVertexCount += 6;
                                    break;
                                case 4:
                                    local_selectionLineSize += 56;
                                    selectionLineVertexCount += 8;
                                    break;
                                case 5:
                                    local_selectionLineSize += 42;
                                    selectionLineVertexCount += 6;
                                    break;
                                default:
                                    break;
                                }
                            }


                            if (!gd.visible) {
                                hiddenSet.add(gd);
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
                                    local_lineSize += 14;
                                    lineVertexCount += 2;
                                } else {
                                    local_lineSize += 28;
                                    lineVertexCount += 4;
                                }
                                continue;
                            case 3:
                                final GData3 gd3 = (GData3) gd;
                                if (gd3.isTriangle) {
                                    if (drawWireframe || meshLines && (subfileMeshLines || mainFileContent.contains(gw.data))) {
                                        local_tempLineSize += 42;
                                        tempLineVertexCount += 6;
                                    }
                                    switch (renderMode) {
                                    case -1:
                                        continue;
                                    case 0:
                                    case 1:
                                    case 2:
                                    case 3:
                                    case 6:
                                    case 7:
                                        local_triangleSize += 60;
                                        if (gd3.a < 1f) {
                                            transparentTriangleVertexCount += 6;
                                        } else {
                                            triangleVertexCount += 6;
                                        }
                                        continue;
                                    case 4:
                                        if (gw.noclip) {
                                            local_triangleSize += 60;
                                            if (gd3.a < 1f) {
                                                transparentTriangleVertexCount += 6;
                                            } else {
                                                triangleVertexCount += 6;
                                            }
                                        } else {
                                            local_triangleSize += 30;
                                            if (gd3.a < 1f) {
                                                transparentTriangleVertexCount += 3;
                                            } else {
                                                triangleVertexCount += 3;
                                            }
                                        }
                                        continue;
                                    default:
                                        continue;
                                    }
                                } else {
                                    local_lineSize += 168;
                                    lineVertexCount += 24;
                                }
                                continue;
                            case 4:
                                final GData4 gd4 = (GData4) gd;
                                if (drawWireframe || meshLines && (subfileMeshLines || mainFileContent.contains(gw.data))) {
                                    local_tempLineSize += 56;
                                    tempLineVertexCount += 8;
                                }
                                switch (renderMode) {
                                case -1:
                                    continue;
                                case 0:
                                case 1:
                                case 2:
                                case 3:
                                case 6:
                                case 7:
                                    local_triangleSize += 120;
                                    if (gd4.a < 1f) {
                                        transparentTriangleVertexCount += 12;
                                    } else {
                                        triangleVertexCount += 12;
                                    }
                                    continue;
                                case 4:
                                    if (gw.noclip) {
                                        local_triangleSize += 120;
                                        if (gd4.a < 1f) {
                                            transparentTriangleVertexCount += 12;
                                        } else {
                                            triangleVertexCount += 12;
                                        }
                                    } else {
                                        local_triangleSize += 60;
                                        if (gd4.a < 1f) {
                                            transparentTriangleVertexCount += 6;
                                        } else {
                                            triangleVertexCount += 6;
                                        }
                                    }
                                    continue;
                                default:
                                    continue;
                                }
                            case 5:
                                if (hideCondlines) {
                                    continue;
                                }
                                // Condlines are tricky, since I have to calculate their visibility
                                local_condlineSize += 30;
                                condlineVertexCount += 2;
                                continue;
                            default:
                                continue;
                            }
                        }

                        // for GL_TRIANGLES
                        float[] triangleData = new float[local_triangleSize];
                        // for GL_LINES
                        float[] lineData = new float[local_lineSize];
                        float[] condlineData = new float[local_condlineSize];
                        float[] tempLineData = new float[local_tempLineSize];
                        float[] selectionLineData = new float[local_selectionLineSize];

                        // for GL_POINTS
                        float[] vertexData = new float[local_verticesSize * 7];

                        // Build the vertex array
                        {
                            final float r = View.vertex_Colour_r[0];
                            final float g = View.vertex_Colour_g[0];
                            final float b = View.vertex_Colour_b[0];
                            final float r2 = View.vertex_selected_Colour_r[0];
                            final float g2 = View.vertex_selected_Colour_g[0];
                            final float b2 = View.vertex_selected_Colour_b[0];
                            int i = 0;

                            if (isTransforming && moveAdjacentData) {
                                for(Vertex v : transformedVertices) {
                                    vertexData[i] = v.x;
                                    vertexData[i + 1] = v.y;
                                    vertexData[i + 2] = v.z;
                                    vertexData[i + 3] = r2;
                                    vertexData[i + 4] = g2;
                                    vertexData[i + 5] = b2;
                                    vertexData[i + 6] = 7f;
                                    i += 7;
                                }
                            }
                            if (smoothVertices) {

                                for(Vertex v : vertices) {
                                    vertexData[i] = v.x;
                                    vertexData[i + 1] = v.y;
                                    vertexData[i + 2] = v.z;
                                    vertexData[i + 3] = r;
                                    vertexData[i + 4] = g;
                                    vertexData[i + 5] = b;
                                    if (c3d.isShowingCondlineControlPoints()) {
                                        vertexData[i + 6] = hiddenVertices.contains(v) ? 0f : 7f;
                                    } else {
                                        vertexData[i + 6] = hiddenVertices.contains(v) || pureCondlineControlPoints.contains(v) ? 0f : 7f;
                                    }
                                    i += 7;
                                }

                                @SuppressWarnings("unchecked")
                                ArrayList<Vertex> verts = (ArrayList<Vertex>) smoothObj[0];
                                for(Vertex v : verts) {
                                    vertexData[i] = v.x;
                                    vertexData[i + 1] = v.y;
                                    vertexData[i + 2] = v.z;
                                    vertexData[i + 3] = r2;
                                    vertexData[i + 4] = g2;
                                    vertexData[i + 5] = b2;
                                    vertexData[i + 6] = 7f;
                                    i += 7;
                                }
                            } else {
                                for(Vertex v : vertices) {
                                    vertexData[i] = v.x;
                                    vertexData[i + 1] = v.y;
                                    vertexData[i + 2] = v.z;

                                    if (selectedVertices.contains(v)) {
                                        vertexData[i + 3] = r2;
                                        vertexData[i + 4] = g2;
                                        vertexData[i + 5] = b2;
                                        vertexData[i + 6] = 7f;
                                    } else {
                                        vertexData[i + 3] = r;
                                        vertexData[i + 4] = g;
                                        vertexData[i + 5] = b;

                                        if (c3d.isShowingCondlineControlPoints()) {
                                            vertexData[i + 6] = hiddenVertices.contains(v) ? 0f : 7f;
                                        } else {
                                            vertexData[i + 6] = hiddenVertices.contains(v) || pureCondlineControlPoints.contains(v) ? 0f : 7f;
                                        }
                                    }
                                    i += 7;
                                }

                            }
                        }


                        Vertex[] v;
                        int triangleIndex = 0;
                        int transparentTriangleIndex = triangleVertexCount;
                        int lineIndex = 0;
                        int condlineIndex = 0;
                        int tempLineIndex = 0;
                        int selectionLineIndex = 0;

                        if (smoothVertices) {
                            @SuppressWarnings("unchecked")
                            ArrayList<Vertex> verts = (ArrayList<Vertex>) smoothObj[0];
                            for (Vertex v1 : verts) {
                                if (smoothVertexAdjacency.containsKey(smoothVertexIndmap.get(v1))) {
                                    for (Integer i : smoothVertexAdjacency.get(smoothVertexIndmap.get(v1))) {
                                        Vertex v2 = verts.get(i);
                                        pointAt7(0, v1.x, v1.y, v1.z, selectionLineData, selectionLineIndex);
                                        pointAt7(1, v2.x, v2.y, v2.z, selectionLineData, selectionLineIndex);
                                        colourise7(0, 2, View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0], 7f, selectionLineData, selectionLineIndex);
                                        selectionLineIndex += 2;
                                    }
                                }
                            }
                        }

                        float xn = 0f, yn = 0f, zn = 0f;
                        float xn1 = 0f, yn1 = 0f, zn1 = 0f;
                        float xn2 = 0f, yn2 = 0f, zn2 = 0f;
                        float xn3 = 0f, yn3 = 0f, zn3 = 0f;
                        float xn4 = 0f, yn4 = 0f, zn4 = 0f;

                        // Iterate the objects and generate the buffer data
                        // TEXMAP and Real Backface Culling are quite "the same", but they need different vertex normals / materials
                        for (GDataAndWinding gw : dataInOrder) {
                            final GData gd = gw.data;

                            final int type = gd.type();
                            final boolean selected = selectionSet.contains(gd);

                            if (selected) {
                                v = vertexMap.get(gd);
                                switch (type) {
                                case 2:
                                    pointAt7(0, v[0].x, v[0].y, v[0].z, selectionLineData, selectionLineIndex);
                                    pointAt7(1, v[1].x, v[1].y, v[1].z, selectionLineData, selectionLineIndex);
                                    colourise7(0, 2, View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0], 7f, selectionLineData, selectionLineIndex);
                                    selectionLineIndex += 2;
                                    break;
                                case 3:
                                    pointAt7(0, v[0].x, v[0].y, v[0].z, selectionLineData, selectionLineIndex);
                                    pointAt7(1, v[1].x, v[1].y, v[1].z, selectionLineData, selectionLineIndex);
                                    pointAt7(2, v[1].x, v[1].y, v[1].z, selectionLineData, selectionLineIndex);
                                    pointAt7(3, v[2].x, v[2].y, v[2].z, selectionLineData, selectionLineIndex);
                                    pointAt7(4, v[2].x, v[2].y, v[2].z, selectionLineData, selectionLineIndex);
                                    pointAt7(5, v[0].x, v[0].y, v[0].z, selectionLineData, selectionLineIndex);
                                    if (((GData3) gd).isTriangle) {
                                        colourise7(0, 6, View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0], 7f, selectionLineData, selectionLineIndex);
                                    } else {
                                        colourise7(0, 4, View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0], 7f, selectionLineData, selectionLineIndex);
                                    }
                                    selectionLineIndex += 6;
                                    break;
                                case 4:
                                    pointAt7(0, v[0].x, v[0].y, v[0].z, selectionLineData, selectionLineIndex);
                                    pointAt7(1, v[1].x, v[1].y, v[1].z, selectionLineData, selectionLineIndex);
                                    pointAt7(2, v[1].x, v[1].y, v[1].z, selectionLineData, selectionLineIndex);
                                    pointAt7(3, v[2].x, v[2].y, v[2].z, selectionLineData, selectionLineIndex);
                                    pointAt7(4, v[2].x, v[2].y, v[2].z, selectionLineData, selectionLineIndex);
                                    pointAt7(5, v[3].x, v[3].y, v[3].z, selectionLineData, selectionLineIndex);
                                    pointAt7(6, v[3].x, v[3].y, v[3].z, selectionLineData, selectionLineIndex);
                                    pointAt7(7, v[0].x, v[0].y, v[0].z, selectionLineData, selectionLineIndex);
                                    colourise7(0, 8, View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0], 7f, selectionLineData, selectionLineIndex);
                                    selectionLineIndex += 8;
                                    break;
                                case 5:
                                    pointAt7(0, v[0].x, v[0].y, v[0].z, selectionLineData, selectionLineIndex);
                                    pointAt7(1, v[1].x, v[1].y, v[1].z, selectionLineData, selectionLineIndex);
                                    pointAt7(2, v[0].x, v[0].y, v[0].z, selectionLineData, selectionLineIndex);
                                    pointAt7(3, v[2].x, v[2].y, v[2].z, selectionLineData, selectionLineIndex);
                                    pointAt7(4, v[0].x, v[0].y, v[0].z, selectionLineData, selectionLineIndex);
                                    pointAt7(5, v[3].x, v[3].y, v[3].z, selectionLineData, selectionLineIndex);
                                    colourise7(0, 2, View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0], 7f, selectionLineData, selectionLineIndex);
                                    colourise7(2, 2, View.condline_selected_Colour_r[0], View.condline_selected_Colour_g[0], View.condline_selected_Colour_b[0], 7f, selectionLineData, selectionLineIndex);
                                    colourise7(4, 2, View.condline_selected_Colour_r[0] / 2f, View.condline_selected_Colour_g[0] / 2f, View.condline_selected_Colour_b[0] / 2f, 7f, selectionLineData, selectionLineIndex);
                                    selectionLineIndex += 6;
                                    break;
                                default:
                                    break;
                                }
                            }

                            if (hiddenSet.contains(gd)) {
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
                                    if (renderMode != 1) {
                                        colourise7(0, 2, gd2.r, gd2.g, gd2.b, 7f, lineData, lineIndex);
                                    } else {
                                        final float r = MathHelper.randomFloat(gd2.ID, 0);
                                        final float g = MathHelper.randomFloat(gd2.ID, 1);
                                        final float b = MathHelper.randomFloat(gd2.ID, 2);
                                        colourise7(0, 2, r, g, b, 7f, lineData, lineIndex);
                                    }
                                    lineIndex += 2;
                                } else {
                                    lineIndex += gd2.insertDistanceMeter(v, lineData, lineIndex);
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
                                            Nv.x = gd3.xn;
                                            Nv.y = gd3.yn;
                                            Nv.z = gd3.zn;
                                            Nv.w = 1f;
                                            Matrix4f loc = matrixMap.get(gd3.parent);
                                            Matrix4f.transform(loc, Nv, Nv);
                                            xn = Nv.x;
                                            yn = Nv.y;
                                            zn = Nv.z;
                                        }
                                    }

                                    if (drawWireframe || meshLines && (subfileMeshLines || mainFileContent.contains(gw.data))) {
                                        pointAt7(0, v[0].x, v[0].y, v[0].z, tempLineData, tempLineIndex);
                                        pointAt7(1, v[1].x, v[1].y, v[1].z, tempLineData, tempLineIndex);
                                        pointAt7(2, v[1].x, v[1].y, v[1].z, tempLineData, tempLineIndex);
                                        pointAt7(3, v[2].x, v[2].y, v[2].z, tempLineData, tempLineIndex);
                                        pointAt7(4, v[2].x, v[2].y, v[2].z, tempLineData, tempLineIndex);
                                        pointAt7(5, v[0].x, v[0].y, v[0].z, tempLineData, tempLineIndex);
                                        colourise7(0, 6, View.meshline_Colour_r[0], View.meshline_Colour_g[0], View.meshline_Colour_b[0], 7f, tempLineData, tempLineIndex);
                                        tempLineIndex += 6;
                                    }
                                    int tmpRenderMode = renderMode;
                                    if (tmpRenderMode < 6  && tmpRenderMode > 1 && gw.noclip) {
                                        tmpRenderMode = 0;
                                    }
                                    switch (tmpRenderMode) {
                                    case -1:
                                        continue;
                                    case 0:
                                    {
                                        pointAt(0, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                        pointAt(1, v[1].x, v[1].y, v[1].z, triangleData, tempIndex);
                                        pointAt(2, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                        pointAt(3, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                        pointAt(4, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                        pointAt(5, v[1].x, v[1].y, v[1].z, triangleData, tempIndex);
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
                                        continue;
                                    }
                                    case 1:
                                    {
                                        final float r = MathHelper.randomFloat(gd3.ID, 0);
                                        final float g = MathHelper.randomFloat(gd3.ID, 1);
                                        final float b = MathHelper.randomFloat(gd3.ID, 2);
                                        pointAt(0, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                        pointAt(1, v[1].x, v[1].y, v[1].z, triangleData, tempIndex);
                                        pointAt(2, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                        pointAt(3, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                        pointAt(4, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                        pointAt(5, v[1].x, v[1].y, v[1].z, triangleData, tempIndex);
                                        colourise(0, 6, r, g, b, gd3.a, triangleData, tempIndex);
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
                                        continue;
                                    }
                                    case 2:
                                    case 6:
                                    {
                                        pointAt(0, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                        pointAt(1, v[1].x, v[1].y, v[1].z, triangleData, tempIndex);
                                        pointAt(2, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                        pointAt(3, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                        pointAt(4, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                        pointAt(5, v[1].x, v[1].y, v[1].z, triangleData, tempIndex);


                                        switch (gw.winding) {
                                        case BFC.CW:
                                            if (gw.negativeDeterminant ^ gw.invertNext) {
                                                colourise(0, 3, View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], gd3.a, triangleData, tempIndex);
                                                colourise(3, 3, View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], gd3.a, triangleData, tempIndex);
                                            } else {
                                                colourise(3, 3, View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], gd3.a, triangleData, tempIndex);
                                                colourise(0, 3, View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], gd3.a, triangleData, tempIndex);
                                            }
                                            break;
                                        case BFC.CCW:
                                            if (gw.negativeDeterminant ^ gw.invertNext) {
                                                colourise(3, 3, View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], gd3.a, triangleData, tempIndex);
                                                colourise(0, 3, View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], gd3.a, triangleData, tempIndex);
                                            } else {
                                                colourise(0, 3, View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], gd3.a, triangleData, tempIndex);
                                                colourise(3, 3, View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], gd3.a, triangleData, tempIndex);
                                            }
                                            break;
                                        case BFC.NOCERTIFY:
                                            colourise(0, 6, View.BFC_uncertified_Colour_r[0], View.BFC_uncertified_Colour_g[0], View.BFC_uncertified_Colour_b[0], gd3.a, triangleData, tempIndex);
                                            break;
                                        default:
                                            break;
                                        }
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
                                        continue;
                                    }
                                    case 7:
                                    {
                                        pointAt(0, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                        pointAt(1, v[1].x, v[1].y, v[1].z, triangleData, tempIndex);
                                        pointAt(2, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                        pointAt(3, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                        pointAt(4, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                        pointAt(5, v[1].x, v[1].y, v[1].z, triangleData, tempIndex);

                                        colourise(0, 6, 0f, 0f, 1f, gd3.a, triangleData, tempIndex);

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
                                        continue;
                                    }
                                    case 3:
                                    {
                                        pointAt(0, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                        pointAt(1, v[1].x, v[1].y, v[1].z, triangleData, tempIndex);
                                        pointAt(2, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                        pointAt(3, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                        pointAt(4, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                        pointAt(5, v[1].x, v[1].y, v[1].z, triangleData, tempIndex);


                                        switch (gw.winding) {
                                        case BFC.CW:
                                            if (gw.negativeDeterminant ^ gw.invertNext) {
                                                colourise(0, 3, View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], gd3.a, triangleData, tempIndex);
                                                colourise(3, 3, gd3.r, gd3.g, gd3.b, gd3.a, triangleData, tempIndex);
                                            } else {
                                                colourise(3, 3, View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], gd3.a, triangleData, tempIndex);
                                                colourise(0, 3, gd3.r, gd3.g, gd3.b, gd3.a, triangleData, tempIndex);
                                            }
                                            break;
                                        case BFC.CCW:
                                            if (gw.negativeDeterminant ^ gw.invertNext) {
                                                colourise(3, 3, View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], gd3.a, triangleData, tempIndex);
                                                colourise(0, 3, gd3.r, gd3.g, gd3.b, gd3.a, triangleData, tempIndex);
                                            } else {
                                                colourise(0, 3, View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], gd3.a, triangleData, tempIndex);
                                                colourise(3, 3, gd3.r, gd3.g, gd3.b, gd3.a, triangleData, tempIndex);
                                            }
                                            break;
                                        case BFC.NOCERTIFY:
                                            colourise(0, 6, View.BFC_uncertified_Colour_r[0], View.BFC_uncertified_Colour_g[0], View.BFC_uncertified_Colour_b[0], gd3.a, triangleData, tempIndex);
                                            break;
                                        default:
                                            break;
                                        }
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
                                        continue;
                                    }
                                    case 4:
                                    {
                                        colourise(0, 3, gd3.r, gd3.g, gd3.b, gd3.a, triangleData, tempIndex);

                                        switch (gw.winding) {
                                        case BFC.CW:
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
                                            } else {
                                                pointAt(0, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                                pointAt(1, v[1].x, v[1].y, v[1].z, triangleData, tempIndex);
                                                pointAt(2, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                            }
                                            break;
                                        case BFC.CCW:
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
                                            } else {
                                                pointAt(0, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                                pointAt(1, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                                pointAt(2, v[1].x, v[1].y, v[1].z, triangleData, tempIndex);
                                            }
                                            break;
                                        case BFC.NOCERTIFY:
                                            pointAt(0, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                            pointAt(1, v[1].x, v[1].y, v[1].z, triangleData, tempIndex);
                                            pointAt(2, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                            colourise(0, 3, 0f, 0f, 0f, 0f, triangleData, tempIndex);
                                            break;
                                        default:
                                            break;
                                        }
                                        if (transparent) {
                                            transparentTriangleIndex += 3;
                                        } else {
                                            triangleIndex += 3;
                                        }
                                        continue;
                                    }
                                    default:
                                        continue;
                                    }
                                } else {
                                    lineIndex += gd3.insertProtractor(v, lineData, lineIndex);
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
                                        Nv.x = gd4.xn;
                                        Nv.y = gd4.yn;
                                        Nv.z = gd4.zn;
                                        Nv.w = 1f;
                                        Matrix4f loc = matrixMap.get(gd4.parent);
                                        Matrix4f.transform(loc, Nv, Nv);
                                        xn = Nv.x;
                                        yn = Nv.y;
                                        zn = Nv.z;
                                    }
                                }

                                if (drawWireframe || meshLines && (subfileMeshLines || mainFileContent.contains(gw.data))) {
                                    pointAt7(0, v[0].x, v[0].y, v[0].z, tempLineData, tempLineIndex);
                                    pointAt7(1, v[1].x, v[1].y, v[1].z, tempLineData, tempLineIndex);
                                    pointAt7(2, v[1].x, v[1].y, v[1].z, tempLineData, tempLineIndex);
                                    pointAt7(3, v[2].x, v[2].y, v[2].z, tempLineData, tempLineIndex);
                                    pointAt7(4, v[2].x, v[2].y, v[2].z, tempLineData, tempLineIndex);
                                    pointAt7(5, v[3].x, v[3].y, v[3].z, tempLineData, tempLineIndex);
                                    pointAt7(6, v[3].x, v[3].y, v[3].z, tempLineData, tempLineIndex);
                                    pointAt7(7, v[0].x, v[0].y, v[0].z, tempLineData, tempLineIndex);
                                    colourise7(0, 8, View.meshline_Colour_r[0], View.meshline_Colour_g[0], View.meshline_Colour_b[0], 7f, tempLineData, tempLineIndex);
                                    tempLineIndex += 8;
                                }

                                int tmpRenderMode = renderMode;
                                if (tmpRenderMode < 6  && tmpRenderMode > 1 && gw.noclip) {
                                    tmpRenderMode = 0;
                                }
                                switch (tmpRenderMode) {
                                case -1:
                                    continue;
                                case 0:
                                {
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
                                    continue;
                                }
                                case 1:
                                {
                                    final float r = MathHelper.randomFloat(gd4.ID, 0);
                                    final float g = MathHelper.randomFloat(gd4.ID, 1);
                                    final float b = MathHelper.randomFloat(gd4.ID, 2);
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

                                    colourise(0, 12, r, g, b, gd4.a, triangleData, tempIndex);
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
                                    continue;
                                }
                                case 2:
                                case 6:
                                {
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

                                    switch (gw.winding) {
                                    case BFC.CW:
                                        if (gw.invertNext  ^ gw.negativeDeterminant) {
                                            colourise(6, 6, View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], gd4.a, triangleData, tempIndex);
                                            colourise(0, 6, View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], gd4.a, triangleData, tempIndex);
                                        } else {
                                            colourise(0, 6, View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], gd4.a, triangleData, tempIndex);
                                            colourise(6, 6, View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], gd4.a, triangleData, tempIndex);
                                        }
                                        break;
                                    case BFC.CCW:
                                        if (gw.invertNext  ^ gw.negativeDeterminant) {
                                            colourise(0, 6, View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], gd4.a, triangleData, tempIndex);
                                            colourise(6, 6, View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], gd4.a, triangleData, tempIndex);
                                        } else {
                                            colourise(6, 6, View.BFC_front_Colour_r[0], View.BFC_front_Colour_g[0], View.BFC_front_Colour_b[0], gd4.a, triangleData, tempIndex);
                                            colourise(0, 6, View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], gd4.a, triangleData, tempIndex);
                                        }
                                        break;
                                    case BFC.NOCERTIFY:
                                        colourise(0, 12, View.BFC_uncertified_Colour_r[0], View.BFC_uncertified_Colour_g[0], View.BFC_uncertified_Colour_b[0], gd4.a, triangleData, tempIndex);
                                        break;
                                    default:
                                        break;
                                    }
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
                                    continue;
                                }
                                case 7:
                                {
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

                                    final double angle = gd4.calculateAngle();
                                    float f = (float) Math.min(1.0, Math.max(0, angle - Threshold.coplanarity_angle_warning) / Threshold.coplanarity_angle_error);

                                    float r = 0f;
                                    float g = 0f;
                                    float b = 0f;

                                    if (f < .5) {
                                        g = f / .5f;
                                        b = (1f - g);
                                    } else {
                                        r = (f - .5f) / .5f;
                                        g = (1f - r);
                                    }

                                    colourise(0, 12, r, g, b, gd4.a, triangleData, tempIndex);

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
                                    continue;
                                }
                                case 3:
                                {
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

                                    switch (gw.winding) {
                                    case BFC.CW:
                                        if (gw.invertNext  ^ gw.negativeDeterminant) {
                                            colourise(6, 6, gd4.r, gd4.g, gd4.b, gd4.a, triangleData, tempIndex);
                                            colourise(0, 6, View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], gd4.a, triangleData, tempIndex);
                                        } else {
                                            colourise(0, 6, gd4.r, gd4.g, gd4.b, gd4.a, triangleData, tempIndex);
                                            colourise(6, 6, View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], gd4.a, triangleData, tempIndex);
                                        }
                                        break;
                                    case BFC.CCW:
                                        if (gw.invertNext  ^ gw.negativeDeterminant) {
                                            colourise(0, 6, gd4.r, gd4.g, gd4.b, gd4.a, triangleData, tempIndex);
                                            colourise(6, 6, View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], gd4.a, triangleData, tempIndex);
                                        } else {
                                            colourise(6, 6, gd4.r, gd4.g, gd4.b, gd4.a, triangleData, tempIndex);
                                            colourise(0, 6, View.BFC_back__Colour_r[0], View.BFC_back__Colour_g[0], View.BFC_back__Colour_b[0], gd4.a, triangleData, tempIndex);
                                        }
                                        break;
                                    case BFC.NOCERTIFY:
                                        colourise(0, 12, View.BFC_uncertified_Colour_r[0], View.BFC_uncertified_Colour_g[0], View.BFC_uncertified_Colour_b[0], gd4.a, triangleData, tempIndex);
                                        break;
                                    default:
                                        break;
                                    }
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
                                    continue;
                                }
                                case 4:
                                {
                                    colourise(0, 6, gd4.r, gd4.g, gd4.b, gd4.a, triangleData, tempIndex);
                                    switch (gw.winding) {
                                    case BFC.CW:
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
                                        } else {
                                            pointAt(0, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                            pointAt(1, v[1].x, v[1].y, v[1].z, triangleData, tempIndex);
                                            pointAt(2, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                            pointAt(3, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                            pointAt(4, v[3].x, v[3].y, v[3].z, triangleData, tempIndex);
                                            pointAt(5, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                        }
                                        break;
                                    case BFC.CCW:
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
                                        } else {
                                            pointAt(0, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                            pointAt(1, v[3].x, v[3].y, v[3].z, triangleData, tempIndex);
                                            pointAt(2, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                            pointAt(3, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                            pointAt(4, v[1].x, v[1].y, v[1].z, triangleData, tempIndex);
                                            pointAt(5, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                        }
                                        break;
                                    case BFC.NOCERTIFY:
                                        colourise(0, 6, 0f, 0f, 0f, 0f, triangleData, tempIndex);
                                        pointAt(0, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                        pointAt(1, v[1].x, v[1].y, v[1].z, triangleData, tempIndex);
                                        pointAt(2, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                        pointAt(3, v[2].x, v[2].y, v[2].z, triangleData, tempIndex);
                                        pointAt(4, v[3].x, v[3].y, v[3].z, triangleData, tempIndex);
                                        pointAt(5, v[0].x, v[0].y, v[0].z, triangleData, tempIndex);
                                        break;
                                    default:
                                        break;
                                    }
                                    if (transparent) {
                                        transparentTriangleIndex += 6;
                                    } else {
                                        triangleIndex += 6;
                                    }
                                    continue;
                                }
                                default:
                                    continue;
                                }
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
                                if (condlineMode) {
                                    if (gd5.wasShown()) {
                                        colourise15(0, 2, View.condline_shown_Colour_r[0], View.condline_shown_Colour_g[0], View.condline_shown_Colour_b[0], condlineData, condlineIndex);
                                    } else {
                                        colourise15(0, 2, View.condline_hidden_Colour_r[0], View.condline_hidden_Colour_g[0], View.condline_hidden_Colour_b[0], condlineData, condlineIndex);
                                    }
                                } else {
                                    if (renderMode != 1) {
                                        colourise15(0, 2, gd5.r, gd5.g, gd5.b, condlineData, condlineIndex);
                                    } else {
                                        final float r = MathHelper.randomFloat(gd5.ID, 0);
                                        final float g = MathHelper.randomFloat(gd5.ID, 1);
                                        final float b = MathHelper.randomFloat(gd5.ID, 2);
                                        colourise15(0, 2, r, g, b, condlineData, condlineIndex);
                                    }
                                }
                                condlineIndex += 2;
                                continue;
                            default:
                                continue;
                            }
                        }

                        if (drawStudLogo) {
                            lock.lock();
                            stud1_Matrices = stud1Matrices;
                            stud2_Matrices = stud2Matrices;
                            lock.unlock();
                        }

                        if (vertexMap2.size() != vertexMap.size() || !dataToRemove.isEmpty() || isTransforming) {
                            for (GData gd : dataToRemove) {
                                vertexMap.remove(gd);
                                vertexMap2.remove(gd);
                            }
                            vertexMap2.putAll(vertexMap);
                            sharedVertexMap = vertexMap2;
                        }
                        lock.lock();
                        images = pngImages;
                        distanceMeters = tmpDistanceMeters;
                        protractors = tmpProtractors;
                        dataTriangles = triangleData;
                        solidTriangleSize = triangleVertexCount;
                        transparentTriangleSize = transparentTriangleVertexCount;
                        transparentTriangleOffset = triangleVertexCount;
                        vertexSize = local_verticesSize;
                        dataVertices = vertexData;
                        lineSize = lineVertexCount;
                        dataLines = lineData;
                        condlineSize = condlineVertexCount;
                        dataCondlines = condlineData;
                        tempLineSize = tempLineVertexCount;
                        dataTempLines = tempLineData;
                        selectionSize = selectionLineVertexCount;
                        dataSelectionLines = selectionLineData;
                        dataSelectionCSG = tmpCsgSelectionData;
                        selectionCSGsize = csgSelectionVertexSize;
                        lock.unlock();

                        /* if (NLogger.DEBUG) {
                            System.out.println("Processing time: " + (System.currentTimeMillis() - start)); //$NON-NLS-1$
                        } */
                    } catch (Exception ex) {
                        if (NLogger.DEBUG) {
                            System.out.println("Exception: " + ex.getMessage()); //$NON-NLS-1$
                        }
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
        GL30.glDeleteVertexArrays(vaoVertices);
        GL15.glDeleteBuffers(vboVertices);
        GL30.glDeleteVertexArrays(vaoLines);
        GL15.glDeleteBuffers(vboLines);
        GL30.glDeleteVertexArrays(vaoTempLines);
        GL15.glDeleteBuffers(vboTempLines);
        GL30.glDeleteVertexArrays(vaoSelectionLines);
        GL15.glDeleteBuffers(vboSelectionLines);
        GL30.glDeleteVertexArrays(vaoCondlines);
        GL15.glDeleteBuffers(vboCondlines);
        GL30.glDeleteVertexArrays(vaoCSG);
        GL15.glDeleteBuffers(vboCSG);
        GL30.glDeleteVertexArrays(vaoStudLogo1);
        GL15.glDeleteBuffers(vboStudLogo1);
        GL30.glDeleteVertexArrays(vaoStudLogo2);
        GL15.glDeleteBuffers(vboStudLogo2);
    }

    private int ts, ss, to, vs, ls, tls, sls, cls, ssCSG, toCSG, tsCSG, sCSG;
    public void draw(GLMatrixStack stack, GLShader mainShader, GLShader condlineShader, GLShader glyphShader, boolean drawSolidMaterials, DatFile df) {

        Matrix4f vm = c3d.getViewport();
        Matrix4f ivm = c3d.getViewport_Inverse();

        if (dataTriangles == null || dataLines == null || dataVertices == null) {
            return;
        }

        final float zoom = c3d.getZoom();
        final boolean drawLines = View.lineWidthGL[0] > 0.01f;
        final boolean studlogo = c3d.isShowingLogo();

        if (c3d.isLightOn()) {
            mainShader.setFactor(.9f);
        } else {
            mainShader.setFactor(1f);
        }

        // TODO Draw CSG VAOs here
        if (usesCSG) {
            if (drawSolidMaterials) {

                GL30.glBindVertexArray(vaoCSG);
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboCSG);
                lock.lock();
                // I can't use glBufferSubData() it creates a memory leak!!!
                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, dataCSG, GL15.GL_STATIC_DRAW);
                ssCSG = solidCSGsize;
                toCSG = transparentCSGoffset;
                tsCSG = transparentCSGsize;
                lock.unlock();

                GL20.glEnableVertexAttribArray(0);
                GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, (3 + 3 + 4) * 4, 0);

                GL20.glEnableVertexAttribArray(1);
                GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, (3 + 3 + 4) * 4, 3 * 4);

                GL20.glEnableVertexAttribArray(2);
                GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, (3 + 3 + 4) * 4, (3 + 3) * 4);

                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

                // Transparent and solid parts are at a different location in the buffer
                GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, ssCSG);
            } else {
                GL30.glBindVertexArray(vaoCSG);
                GL11.glDrawArrays(GL11.GL_TRIANGLES, toCSG, tsCSG);
            }
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
            tls = tempLineSize;
            sls = selectionSize;
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
                GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, (3 + 4) * 4, 3 * 4);

                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
                GL11.glLineWidth(View.lineWidthGL[0]);

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
                    ArrayList<Matrix4f> stud1Matrices = stud1_Matrices;
                    ArrayList<Matrix4f> stud2Matrices = stud2_Matrices;
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

            if (tls > 0) {
                GL30.glBindVertexArray(vaoTempLines);
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboTempLines);
                lock.lock();
                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, dataTempLines, GL15.GL_STATIC_DRAW);
                tls = tempLineSize;
                lock.unlock();

                GL20.glEnableVertexAttribArray(0);
                GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, (3 + 4) * 4, 0);

                GL20.glEnableVertexAttribArray(2);
                GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, (3 + 4) * 4, 3 * 4);

                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
                GL11.glLineWidth(1f);

                Vector4f tr = new Vector4f(vm.m30, vm.m31, vm.m32 + 330f * zoom, 1f);
                Matrix4f.transform(ivm, tr, tr);
                stack.glPushMatrix();
                stack.glTranslatef(tr.x, tr.y, tr.z);
                GL11.glDrawArrays(GL11.GL_LINES, 0, tls);
                stack.glPopMatrix();
            }

            mainShader.setFactor(1f);

        } else {

            GL11.glDrawArrays(GL11.GL_TRIANGLES, to, ts);
            mainShader.setFactor(1f);

            if (c3d.isShowingVertices()) {
                GL30.glBindVertexArray(vaoVertices);
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboVertices);
                lock.lock();
                // I can't use glBufferSubData() it creates a memory leak!!!
                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, dataVertices, GL15.GL_STATIC_DRAW);
                vs = vertexSize;
                lock.unlock();

                GL20.glEnableVertexAttribArray(0);
                GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, (3 + 4) * 4, 0);

                GL20.glEnableVertexAttribArray(2);
                GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, (3 + 4) * 4, 3 * 4);

                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

                if (c3d.isShowingHiddenVertices()) {
                    mainShader.setFactor(.5f);
                    GL11.glDisable(GL11.GL_DEPTH_TEST);
                    GL11.glDrawArrays(GL11.GL_POINTS, 0, vs);
                    GL11.glEnable(GL11.GL_DEPTH_TEST);
                    mainShader.setFactor(1f);
                }

                Vector4f tr = new Vector4f(vm.m30, vm.m31, vm.m32 + 330f * zoom, 1f);
                Matrix4f.transform(ivm, tr, tr);
                stack.glPushMatrix();
                stack.glTranslatef(tr.x, tr.y, tr.z);
                GL11.glDrawArrays(GL11.GL_POINTS, 0, vs);
                stack.glPopMatrix();
            }
        }

        // Draw condlines here
        if (drawSolidMaterials) {
            if (drawLines) {
                condlineShader.use();
                stack.setShader(condlineShader);

                GL20.glUniform1f(condlineShader.getUniformLocation("showAll"), c3d.getLineMode() == 1 ? 1f : 0f); //$NON-NLS-1$
                GL20.glUniform1f(condlineShader.getUniformLocation("condlineMode"), c3d.getRenderMode() == 6 ? 1f : 0f); //$NON-NLS-1$

                GL30.glBindVertexArray(vaoCondlines);
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboCondlines);
                lock.lock();
                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, dataCondlines, GL15.GL_STATIC_DRAW);
                cls = condlineSize;
                lock.unlock();

                GL20.glEnableVertexAttribArray(0);
                GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 15 * 4, 0);
                GL20.glEnableVertexAttribArray(1);
                GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 15 * 4, 3 * 4);
                GL20.glEnableVertexAttribArray(2);
                GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 15 * 4, 6 * 4);
                GL20.glEnableVertexAttribArray(3);
                GL20.glVertexAttribPointer(3, 3, GL11.GL_FLOAT, false, 15 * 4, 9 * 4);
                GL20.glEnableVertexAttribArray(4);
                GL20.glVertexAttribPointer(4, 3, GL11.GL_FLOAT, false, 15 * 4, 12 * 4);

                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

                Vector4f tr = new Vector4f(vm.m30, vm.m31, vm.m32 + 330f * zoom, 1f);
                Matrix4f.transform(ivm, tr, tr);
                stack.glPushMatrix();
                stack.glTranslatef(tr.x, tr.y, tr.z);
                GL11.glLineWidth(View.lineWidthGL[0]);
                GL11.glDrawArrays(GL11.GL_LINES, 0, cls);
                stack.glPopMatrix();
                mainShader.use();
                stack.setShader(mainShader);
            }

            // Draw LPE PNG images here
            if (!images.isEmpty()) {
                mainShader.pngModeOn();
                for (GDataPNG png : images) {
                    png.drawGL33(c3d, stack);
                }
                mainShader.pngModeOff();
            }
        } else {

            GL11.glDisable(GL11.GL_DEPTH_TEST);

            // Draw lines from the selection
            if (sls > 0) {
                GL30.glBindVertexArray(vaoSelectionLines);
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboSelectionLines);
                lock.lock();
                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, dataSelectionLines, GL15.GL_STATIC_DRAW);
                sls = selectionSize;
                lock.unlock();

                GL20.glEnableVertexAttribArray(0);
                GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, (3 + 4) * 4, 0);

                GL20.glEnableVertexAttribArray(2);
                GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, (3 + 4) * 4, 3 * 4);

                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

                GL11.glLineWidth(2f);
                GL11.glDrawArrays(GL11.GL_LINES, 0, sls);
            }

            // Draw lines from the CSG selection
            if (usesCSG) {
                GL30.glBindVertexArray(vaoSelectionLines);
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboSelectionLines);
                lock.lock();
                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, dataSelectionCSG, GL15.GL_STATIC_DRAW);
                sCSG = selectionCSGsize;
                lock.unlock();

                GL20.glEnableVertexAttribArray(0);
                GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, (3 + 4) * 4, 0);

                GL20.glEnableVertexAttribArray(2);
                GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, (3 + 4) * 4, 3 * 4);

                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

                GL11.glLineWidth(2f);
                GL11.glDrawArrays(GL11.GL_LINES, 0, sCSG);
            }

            // Draw glyphs here (distance meters, protractors)
            if (!protractors.isEmpty() || !distanceMeters.isEmpty()) {
                glyphShader.use();
                stack.setShader(glyphShader);
                stack.glPushMatrix();
                GL11.glDisable(GL11.GL_CULL_FACE);
                stack.glMultMatrixf(renderer.getRotationInverse());
                for (GData2 dm : distanceMeters) {
                    Vertex[] verts = sharedVertexMap.get(dm);
                    if (verts != null) {
                        if (verts[0].X == null || verts[1].X == null) {
                            dm.drawDistanceGL33(
                                    c3d,
                                    glyphShader,
                                    new BigDecimal(Float.toString(verts[0].x)).scaleByPowerOfTen(-3),
                                    new BigDecimal(Float.toString(verts[0].y)).scaleByPowerOfTen(-3),
                                    new BigDecimal(Float.toString(verts[0].z)).scaleByPowerOfTen(-3),
                                    new BigDecimal(Float.toString(verts[1].x)).scaleByPowerOfTen(-3),
                                    new BigDecimal(Float.toString(verts[1].y)).scaleByPowerOfTen(-3),
                                    new BigDecimal(Float.toString(verts[1].z)).scaleByPowerOfTen(-3),
                                    true
                                    );
                        } else {
                            dm.drawDistanceGL33(
                                    c3d,
                                    glyphShader,
                                    verts[0].X,
                                    verts[0].Y,
                                    verts[0].Z,
                                    verts[1].X,
                                    verts[1].Y,
                                    verts[1].Z,
                                    false
                                    );
                        }
                    }
                }
                for (GData3 pt : protractors) {
                    Vertex[] verts = sharedVertexMap.get(pt);
                    if (verts != null) {
                        if (verts[0].X == null || verts[1].X == null) {
                            pt.drawProtractorGL33(
                                    c3d,
                                    glyphShader,
                                    new BigDecimal(Float.toString(verts[0].x)).scaleByPowerOfTen(-3),
                                    new BigDecimal(Float.toString(verts[0].y)).scaleByPowerOfTen(-3),
                                    new BigDecimal(Float.toString(verts[0].z)).scaleByPowerOfTen(-3),
                                    new BigDecimal(Float.toString(verts[1].x)).scaleByPowerOfTen(-3),
                                    new BigDecimal(Float.toString(verts[1].y)).scaleByPowerOfTen(-3),
                                    new BigDecimal(Float.toString(verts[1].z)).scaleByPowerOfTen(-3),
                                    new BigDecimal(Float.toString(verts[2].x)).scaleByPowerOfTen(-3),
                                    new BigDecimal(Float.toString(verts[2].y)).scaleByPowerOfTen(-3),
                                    new BigDecimal(Float.toString(verts[2].z)).scaleByPowerOfTen(-3)
                                    );
                        } else {
                            pt.drawProtractorGL33(
                                    c3d,
                                    glyphShader,
                                    verts[0].X,
                                    verts[0].Y,
                                    verts[0].Z,
                                    verts[1].X,
                                    verts[1].Y,
                                    verts[1].Z,
                                    verts[2].X,
                                    verts[2].Y,
                                    verts[2].Z
                                    );
                        }
                    }
                }
                GL11.glEnable(GL11.GL_CULL_FACE);
                stack.glPopMatrix();
                mainShader.use();
                stack.setShader(mainShader);
            }

            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }

        GL30.glBindVertexArray(0);
    }

    private boolean load_BFC_info(
            final ArrayList<GDataAndWinding> dataInOrder,
            final ArrayList<GDataCSG> csgData,
            final HashMap<GData, Vertex[]> vertexMap,
            final HashMap<GData1, Matrix4f> matrixMap, final DatFile df,
            final ThreadsafeHashMap<GData2, Vertex[]> lines,
            final ThreadsafeHashMap<GData3, Vertex[]> triangles,
            final ThreadsafeHashMap<GData4, Vertex[]> quads,
            final ThreadsafeHashMap<GData5, Vertex[]> condlines,
            final boolean drawStudLogo,
            ArrayList<GDataPNG> pngImages,
            ArrayList<GData2> tmpDistanceMeters,
            ArrayList<GData3> tmpProtractors) {

        boolean hasCSG = false;
        Stack<GData> stack = new Stack<>();
        Stack<Byte> tempWinding = new Stack<>();
        Stack<Boolean> tempInvertNext = new Stack<>();
        Stack<Boolean> tempInvertNextFound = new Stack<>();
        Stack<Boolean> tempNegativeDeterminant = new Stack<>();

        GData gd = df.getDrawChainStart();
        GData backup = gd;

        byte localWinding = BFC.NOCERTIFY;
        int accumClip = 0;
        boolean globalInvertNext = false;
        boolean globalInvertNextFound = false;
        boolean globalNegativeDeterminant = false;

        // The BFC logic/state machine is not correct yet? (for BFC INVERTNEXT).
        while ((gd = backup.next) != null || !stack.isEmpty()) {
            if (gd == null) {
                if (accumClip > 0) {
                    accumClip--;
                }
                backup = stack.pop();
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
            switch (gd.type()) {
            case 1:
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
                    case BFC.CCW_CLIP:
                        if (accumClip == 1)
                            accumClip = 0;
                        localWinding = BFC.CCW;
                        continue;
                    case BFC.CLIP:
                        if (accumClip == 1)
                            accumClip = 0;
                        continue;
                    case BFC.CW_CLIP:
                        if (accumClip == 1)
                            accumClip = 0;
                        localWinding = BFC.CW;
                        continue;
                    default:
                        continue;
                    }
                } else {
                    switch (((GDataBFC) gd).type) {
                    case BFC.CCW:
                        localWinding = BFC.CCW;
                        continue;
                    case BFC.CCW_CLIP:
                        localWinding = BFC.CCW;
                        continue;
                    case BFC.CW:
                        localWinding = BFC.CW;
                        continue;
                    case BFC.CW_CLIP:
                        localWinding = BFC.CW;
                        continue;
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
                        continue;
                    case BFC.NOCERTIFY:
                        localWinding = BFC.NOCERTIFY;
                        continue;
                    case BFC.NOCLIP:
                        if (accumClip == 0)
                            accumClip = 1;
                        continue;
                    default:
                        continue;
                    }
                }
            case 2:
                GData2 gd2 = (GData2) gd;
                verts = lines.get(gd2);
                if (verts != null) {
                    if (!gd2.isLine) {
                        tmpDistanceMeters.add(gd2);
                    }
                    vertexMap.put(gd, verts);
                    dataInOrder.add(new GDataAndWinding(gd, localWinding, globalNegativeDeterminant, globalInvertNext, accumClip));
                }
                continue;
            case 3:
                GData3 gd3 = (GData3) gd;
                verts = triangles.get(gd3);
                if (verts != null) {
                    if (!gd3.isTriangle) {
                        tmpProtractors.add(gd3);
                    }
                    vertexMap.put(gd, verts);
                    dataInOrder.add(new GDataAndWinding(gd, localWinding, globalNegativeDeterminant, globalInvertNext, accumClip));
                }
                continue;
            case 4:
                verts = quads.get(gd);
                if (verts != null) {
                    vertexMap.put(gd, verts);
                    dataInOrder.add(new GDataAndWinding(gd, localWinding, globalNegativeDeterminant, globalInvertNext, accumClip));
                }
                continue;
            case 5:
                verts = condlines.get(gd);
                if (verts != null) {
                    vertexMap.put(gd, verts);
                    dataInOrder.add(new GDataAndWinding(gd, localWinding, globalNegativeDeterminant, globalInvertNext, accumClip));
                }
                continue;
            case 8:
                csgData.add((GDataCSG) gd);
                hasCSG = true;
                continue;
            case 10:
                pngImages.add((GDataPNG) gd);
                continue;
            default:
                continue;
            }
        }

        return hasCSG;
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
        int pos = (offset + i) * 10;
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
