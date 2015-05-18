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

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.composites.ScalableComposite;
import org.nschmidt.ldparteditor.composites.compositetab.CompositeTab;
import org.nschmidt.ldparteditor.data.tools.IdenticalVertexRemover;
import org.nschmidt.ldparteditor.data.tools.Merger;
import org.nschmidt.ldparteditor.enums.MergeTo;
import org.nschmidt.ldparteditor.enums.ObjectMode;
import org.nschmidt.ldparteditor.enums.RotationSnap;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.enums.TransformationMode;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.enums.WorkingMode;
import org.nschmidt.ldparteditor.helpers.Manipulator;
import org.nschmidt.ldparteditor.helpers.composite3d.Edger2Settings;
import org.nschmidt.ldparteditor.helpers.composite3d.GuiManager;
import org.nschmidt.ldparteditor.helpers.composite3d.IntersectorSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.IsecalcSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.PathTruderSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.PerspectiveCalculator;
import org.nschmidt.ldparteditor.helpers.composite3d.RectifierSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.SelectorSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.SlicerProSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.SymSplitterSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.UnificatorSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.ViewIdleManager;
import org.nschmidt.ldparteditor.helpers.compositetext.SubfileCompiler;
import org.nschmidt.ldparteditor.helpers.math.HashBiMap;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.helpers.math.PowerRay;
import org.nschmidt.ldparteditor.helpers.math.Rational;
import org.nschmidt.ldparteditor.helpers.math.RationalMatrix;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeTreeMap;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.helpers.math.Vector3dd;
import org.nschmidt.ldparteditor.helpers.math.Vector3dh;
import org.nschmidt.ldparteditor.helpers.math.Vector3r;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.shells.editortext.EditorTextWindow;
import org.nschmidt.ldparteditor.text.DatParser;
import org.nschmidt.ldparteditor.text.HeaderState;
import org.nschmidt.ldparteditor.text.StringHelper;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * All actions are THREAD safe!! 1. Displays all vertices <br>
 * 2. Provides functions to <br>
 * 2.1 Manipulate.. <br>
 * 2.2 Create.. <br>
 * 2.3 Delete vertices
 *
 * @author nils
 *
 */
public class VertexManager {

    private final ArrayList<MemorySnapshot> snapshots = new ArrayList<MemorySnapshot>();

    // 1 Vertex kann an mehreren Stellen (GData2-5 + position) manifestiert sein
    /**
     * Subfile-Inhalte sind hierbei enthalten. Die Manifestierung gegen
     * {@code lineLinkedToVertices} checken, wenn ausgeschlossen werden soll,
     * dass es sich um Subfile Daten handelt
     */
    private final ThreadsafeTreeMap<Vertex, Set<VertexManifestation>> vertexLinkedToPositionInFile = new ThreadsafeTreeMap<Vertex, Set<VertexManifestation>>();

    // 1 Vertex kann keinem oder mehreren Subfiles angehören
    private final ThreadsafeTreeMap<Vertex, Set<GData1>> vertexLinkedToSubfile = new ThreadsafeTreeMap<Vertex, Set<GData1>>();

    // Auf Dateiebene: 1 Vertex kann an mehreren Stellen (GData1-5 + position)
    // manifestiert sein, ist er auch im Subfile, so gibt VertexInfo dies an
    /** Subfile-Inhalte sind hier nicht als Key refenziert!! */
    private final ThreadsafeHashMap<GData, Set<VertexInfo>> lineLinkedToVertices = new ThreadsafeHashMap<GData, Set<VertexInfo>>();

    public ThreadsafeHashMap<GData, Set<VertexInfo>> getLineLinkedToVertices() {
        return lineLinkedToVertices;
    }

    private final TreeMap<Vertex, float[]> vertexLinkedToNormalCACHE = new TreeMap<Vertex, float[]>();
    private final HashMap<GData, float[]> dataLinkedToNormalCACHE = new HashMap<GData, float[]>();

    private final ThreadsafeHashMap<GData1, Integer> vertexCountInSubfile = new ThreadsafeHashMap<GData1, Integer>();

    private final ThreadsafeHashMap<GData0, Vertex[]> declaredVertices = new ThreadsafeHashMap<GData0, Vertex[]>();
    private final ThreadsafeHashMap<GData2, Vertex[]> lines = new ThreadsafeHashMap<GData2, Vertex[]>();
    private final ThreadsafeHashMap<GData3, Vertex[]> triangles = new ThreadsafeHashMap<GData3, Vertex[]>();
    private final ThreadsafeHashMap<GData4, Vertex[]> quads = new ThreadsafeHashMap<GData4, Vertex[]>();
    private final ThreadsafeHashMap<GData5, Vertex[]> condlines = new ThreadsafeHashMap<GData5, Vertex[]>();

    private final Vertex[] vArray = new Vertex[4];
    private final VertexManifestation[] vdArray = new VertexManifestation[4];

    private final Set<Vertex> selectedVertices = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());

    private final Set<GData> selectedData = Collections.newSetFromMap(new ThreadsafeHashMap<GData, Boolean>());
    private final Set<GData1> selectedSubfiles = Collections.newSetFromMap(new ThreadsafeHashMap<GData1, Boolean>());
    private final Set<GData2> selectedLines = Collections.newSetFromMap(new ThreadsafeHashMap<GData2, Boolean>());
    private final Set<GData3> selectedTriangles = Collections.newSetFromMap(new ThreadsafeHashMap<GData3, Boolean>());
    private final Set<GData4> selectedQuads = Collections.newSetFromMap(new ThreadsafeHashMap<GData4, Boolean>());
    private final Set<GData5> selectedCondlines = Collections.newSetFromMap(new ThreadsafeHashMap<GData5, Boolean>());

    private final Set<Vertex> backupSelectedVertices = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());

    private final Set<GData> backupSelectedData = Collections.newSetFromMap(new ThreadsafeHashMap<GData, Boolean>());
    private final Set<GData1> backupSelectedSubfiles = Collections.newSetFromMap(new ThreadsafeHashMap<GData1, Boolean>());
    private final Set<GData2> backupSelectedLines = Collections.newSetFromMap(new ThreadsafeHashMap<GData2, Boolean>());
    private final Set<GData3> backupSelectedTriangles = Collections.newSetFromMap(new ThreadsafeHashMap<GData3, Boolean>());
    private final Set<GData4> backupSelectedQuads = Collections.newSetFromMap(new ThreadsafeHashMap<GData4, Boolean>());
    private final Set<GData5> backupSelectedCondlines = Collections.newSetFromMap(new ThreadsafeHashMap<GData5, Boolean>());

    private final Set<GData> newSelectedData = Collections.newSetFromMap(new ThreadsafeHashMap<GData, Boolean>());

    private GDataPNG selectedBgPicture = null;
    private int selectedBgPictureIndex = -1;

    private final Set<Vertex> selectedVerticesForSubfile = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());
    private final Set<GData2> selectedLinesForSubfile = Collections.newSetFromMap(new ThreadsafeHashMap<GData2, Boolean>());
    private final Set<GData3> selectedTrianglesForSubfile = Collections.newSetFromMap(new ThreadsafeHashMap<GData3, Boolean>());
    private final Set<GData4> selectedQuadsForSubfile = Collections.newSetFromMap(new ThreadsafeHashMap<GData4, Boolean>());
    private final Set<GData5> selectedCondlinesForSubfile = Collections.newSetFromMap(new ThreadsafeHashMap<GData5, Boolean>());

    private static final List<GData> CLIPBOARD = new ArrayList<GData>();
    private static final Set<GData> CLIPBOARD_InvNext = Collections.newSetFromMap(new ThreadsafeHashMap<GData, Boolean>());

    private final Set<GData> dataToHide = Collections.newSetFromMap(new ThreadsafeHashMap<GData, Boolean>());

    private final PowerRay powerRay = new PowerRay();

    private final DatFile linkedDatFile;

    private Vertex vertexToReplace = null;

    private boolean modified = false;
    private boolean updated = true;

    private AtomicBoolean skipSyncWithTextEditor = new AtomicBoolean(false);

    private int selectedItemIndex = -1;
    private GData selectedLine = null;

    private Vertex lastSelectedVertex = null;

    public VertexManager(DatFile linkedDatFile) {
        this.linkedDatFile = linkedDatFile;
    }

    public synchronized GData0 addVertex(Vertex vertex) {
        if (!vertexLinkedToPositionInFile.containsKey(vertex))
            vertexLinkedToPositionInFile.put(vertex, Collections.newSetFromMap(new ThreadsafeHashMap<VertexManifestation, Boolean>()));
        GData0 vertexTag = new GData0("0 !LPE VERTEX " + bigDecimalToString(vertex.X) + " " + bigDecimalToString(vertex.Y) + " " + bigDecimalToString(vertex.Z)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$)
        vertexLinkedToPositionInFile.get(vertex).add(new VertexManifestation(0, vertexTag));
        lineLinkedToVertices.put(vertexTag, Collections.newSetFromMap(new ThreadsafeHashMap<VertexInfo, Boolean>()));
        lineLinkedToVertices.get(vertexTag).add(new VertexInfo(vertex, 0, vertexTag));
        declaredVertices.put(vertexTag, new Vertex[] { vertex });
        return vertexTag;
    }

    public synchronized GData0 addVertex(Vertex vertex, GData0 vertexTag) {
        if (!vertexLinkedToPositionInFile.containsKey(vertex))
            vertexLinkedToPositionInFile.put(vertex, Collections.newSetFromMap(new ThreadsafeHashMap<VertexManifestation, Boolean>()));
        vertexLinkedToPositionInFile.get(vertex).add(new VertexManifestation(0, vertexTag));
        lineLinkedToVertices.put(vertexTag, Collections.newSetFromMap(new ThreadsafeHashMap<VertexInfo, Boolean>()));
        lineLinkedToVertices.get(vertexTag).add(new VertexInfo(vertex, 0, vertexTag));
        declaredVertices.put(vertexTag, new Vertex[] { vertex });
        return vertexTag;
    }

    public synchronized GData0 addSubfileVertex(Vertex vertex, GData0 vertexTag, GData1 subfile) {
        if (!vertexCountInSubfile.containsKey(subfile)) {
            vertexCountInSubfile.put(subfile, 0);
        }
        int vertexCount = vertexCountInSubfile.get(subfile);
        vertexCount--;
        if (!vertexLinkedToPositionInFile.containsKey(vertex))
            vertexLinkedToPositionInFile.put(vertex, Collections.newSetFromMap(new ThreadsafeHashMap<VertexManifestation, Boolean>()));
        vertexLinkedToPositionInFile.get(vertex).add(new VertexManifestation(0, vertexTag));
        if (!lineLinkedToVertices.containsKey(subfile))
            lineLinkedToVertices.put(subfile, Collections.newSetFromMap(new ThreadsafeHashMap<VertexInfo, Boolean>()));
        lineLinkedToVertices.get(subfile).add(new VertexInfo(vertex, vertexCount, vertexTag));
        declaredVertices.put(vertexTag, new Vertex[] { vertex });
        vertexCountInSubfile.put(subfile, vertexCount);
        if (!vertexLinkedToSubfile.containsKey(vertex)) {
            vertexLinkedToSubfile.put(vertex, Collections.newSetFromMap(new ThreadsafeHashMap<GData1, Boolean>()));
        }
        vertexLinkedToSubfile.get(vertex).add(subfile);
        return vertexTag;
    }

    public synchronized void add(GData gdata) {

        final GData originalData = gdata;
        final boolean subVertex;
        final int max;
        switch (gdata.type()) {
        case 2: // Line
            GData2 gd2 = (GData2) gdata;
            GData1 parent2 = gd2.parent;
            subVertex = parent2.depth > 0;
            Vector4f[] lv = new Vector4f[2];
            lv[0] = new Vector4f(gd2.x1, gd2.y1, gd2.z1, 1f);
            lv[1] = new Vector4f(gd2.x2, gd2.y2, gd2.z2, 1f);
            if (subVertex) {
                gdata = parent2.firstRef;
                Matrix4f matrix = parent2.productMatrix;
                Matrix4f.transform(matrix, lv[0], lv[0]);
                Matrix4f.transform(matrix, lv[1], lv[1]);
                GData1.updateBoundingBox(2, lv[0], lv[1], null, null, parent2);
            }
            vdArray[0] = new VertexManifestation(0, gd2);
            vdArray[1] = new VertexManifestation(1, gd2);
            if (gd2.X1 == null) {
                vArray[0] = new Vertex(lv[0]);
                vArray[1] = new Vertex(lv[1]);
            } else {
                if (subVertex) {
                    Matrix matrix = parent2.accurateProductMatrix;
                    BigDecimal[] P1 = matrix.transform(gd2.X1, gd2.Y1, gd2.Z1);
                    BigDecimal[] P2 = matrix.transform(gd2.X2, gd2.Y2, gd2.Z2);
                    vArray[0] = new Vertex(P1[0], P1[1], P1[2], lv[0]);
                    vArray[1] = new Vertex(P2[0], P2[1], P2[2], lv[1]);
                } else {
                    vArray[0] = new Vertex(gd2.X1, gd2.Y1, gd2.Z1, lv[0]);
                    vArray[1] = new Vertex(gd2.X2, gd2.Y2, gd2.Z2, lv[1]);
                }
            }
            lines.put(gd2, Arrays.copyOf(vArray, 2));
            max = 2;
            break;
        case 3: // Triangle
            GData3 gd3 = (GData3) gdata;
            GData1 parent3 = gd3.parent;
            subVertex = parent3.depth > 0;
            Vector4f[] tv = new Vector4f[3];
            tv[0] = new Vector4f(gd3.x1, gd3.y1, gd3.z1, 1f);
            tv[1] = new Vector4f(gd3.x2, gd3.y2, gd3.z2, 1f);
            tv[2] = new Vector4f(gd3.x3, gd3.y3, gd3.z3, 1f);
            if (subVertex) {
                gdata = parent3.firstRef;
                Matrix4f matrix = parent3.productMatrix;
                Matrix4f.transform(matrix, tv[0], tv[0]);
                Matrix4f.transform(matrix, tv[1], tv[1]);
                Matrix4f.transform(matrix, tv[2], tv[2]);
                GData1.updateBoundingBox(3, tv[0], tv[1], tv[2], null, parent3);
            }
            vdArray[0] = new VertexManifestation(0, gd3);
            vdArray[1] = new VertexManifestation(1, gd3);
            vdArray[2] = new VertexManifestation(2, gd3);
            if (gd3.X1 == null) {
                vArray[0] = new Vertex(tv[0]);
                vArray[1] = new Vertex(tv[1]);
                vArray[2] = new Vertex(tv[2]);
            } else {
                if (subVertex) {
                    Matrix matrix = parent3.accurateProductMatrix;
                    BigDecimal[] P1 = matrix.transform(gd3.X1, gd3.Y1, gd3.Z1);
                    BigDecimal[] P2 = matrix.transform(gd3.X2, gd3.Y2, gd3.Z2);
                    BigDecimal[] P3 = matrix.transform(gd3.X3, gd3.Y3, gd3.Z3);
                    vArray[0] = new Vertex(P1[0], P1[1], P1[2], tv[0]);
                    vArray[1] = new Vertex(P2[0], P2[1], P2[2], tv[1]);
                    vArray[2] = new Vertex(P3[0], P3[1], P3[2], tv[2]);
                } else {
                    vArray[0] = new Vertex(gd3.X1, gd3.Y1, gd3.Z1, tv[0]);
                    vArray[1] = new Vertex(gd3.X2, gd3.Y2, gd3.Z2, tv[1]);
                    vArray[2] = new Vertex(gd3.X3, gd3.Y3, gd3.Z3, tv[2]);
                }
            }
            triangles.put(gd3, Arrays.copyOf(vArray, 3));
            max = 3;
            break;
        case 4: // Quad
            GData4 gd4 = (GData4) gdata;
            GData1 parent4 = gd4.parent;
            subVertex = parent4.depth > 0;
            Vector4f[] qv = new Vector4f[4];
            qv[0] = new Vector4f(gd4.x1, gd4.y1, gd4.z1, 1f);
            qv[1] = new Vector4f(gd4.x2, gd4.y2, gd4.z2, 1f);
            qv[2] = new Vector4f(gd4.x3, gd4.y3, gd4.z3, 1f);
            qv[3] = new Vector4f(gd4.x4, gd4.y4, gd4.z4, 1f);
            if (subVertex) {
                gdata = parent4.firstRef;
                Matrix4f matrix4 = parent4.productMatrix;
                Matrix4f.transform(matrix4, qv[0], qv[0]);
                Matrix4f.transform(matrix4, qv[1], qv[1]);
                Matrix4f.transform(matrix4, qv[2], qv[2]);
                Matrix4f.transform(matrix4, qv[3], qv[3]);
                GData1.updateBoundingBox(4, qv[0], qv[1], qv[2], qv[3], parent4);
            }
            vdArray[0] = new VertexManifestation(0, gd4);
            vdArray[1] = new VertexManifestation(1, gd4);
            vdArray[2] = new VertexManifestation(2, gd4);
            vdArray[3] = new VertexManifestation(3, gd4);
            if (gd4.X1 == null) {
                vArray[0] = new Vertex(qv[0]);
                vArray[1] = new Vertex(qv[1]);
                vArray[2] = new Vertex(qv[2]);
                vArray[3] = new Vertex(qv[3]);
            } else {
                if (subVertex) {
                    Matrix matrix = parent4.accurateProductMatrix;
                    BigDecimal[] P1 = matrix.transform(gd4.X1, gd4.Y1, gd4.Z1);
                    BigDecimal[] P2 = matrix.transform(gd4.X2, gd4.Y2, gd4.Z2);
                    BigDecimal[] P3 = matrix.transform(gd4.X3, gd4.Y3, gd4.Z3);
                    BigDecimal[] P4 = matrix.transform(gd4.X4, gd4.Y4, gd4.Z4);
                    vArray[0] = new Vertex(P1[0], P1[1], P1[2], qv[0]);
                    vArray[1] = new Vertex(P2[0], P2[1], P2[2], qv[1]);
                    vArray[2] = new Vertex(P3[0], P3[1], P3[2], qv[2]);
                    vArray[3] = new Vertex(P4[0], P4[1], P4[2], qv[3]);
                } else {
                    vArray[0] = new Vertex(gd4.X1, gd4.Y1, gd4.Z1, qv[0]);
                    vArray[1] = new Vertex(gd4.X2, gd4.Y2, gd4.Z2, qv[1]);
                    vArray[2] = new Vertex(gd4.X3, gd4.Y3, gd4.Z3, qv[2]);
                    vArray[3] = new Vertex(gd4.X4, gd4.Y4, gd4.Z4, qv[3]);
                }
            }
            quads.put(gd4, Arrays.copyOf(vArray, 4));
            max = 4;
            break;
        case 5: // Optional Line
            GData5 gd5 = (GData5) gdata;
            GData1 parent5 = gd5.parent;
            subVertex = parent5.depth > 0;
            Vector4f[] ov = new Vector4f[4];
            ov[0] = new Vector4f(gd5.x1, gd5.y1, gd5.z1, 1f);
            ov[1] = new Vector4f(gd5.x2, gd5.y2, gd5.z2, 1f);
            ov[2] = new Vector4f(gd5.x3, gd5.y3, gd5.z3, 1f);
            ov[3] = new Vector4f(gd5.x4, gd5.y4, gd5.z4, 1f);
            if (subVertex) {
                Matrix4f matrix5 = parent5.productMatrix;
                Matrix4f.transform(matrix5, ov[0], ov[0]);
                Matrix4f.transform(matrix5, ov[1], ov[1]);
                Matrix4f.transform(matrix5, ov[2], ov[2]);
                Matrix4f.transform(matrix5, ov[3], ov[3]);
                gdata = parent5.firstRef;
                GData1.updateBoundingBox(2, ov[0], ov[1], null, null, parent5);
            }
            vdArray[0] = new VertexManifestation(0, gd5);
            vdArray[1] = new VertexManifestation(1, gd5);
            vdArray[2] = new VertexManifestation(2, gd5);
            vdArray[3] = new VertexManifestation(3, gd5);
            if (gd5.X1 == null) {
                vArray[0] = new Vertex(ov[0]);
                vArray[1] = new Vertex(ov[1]);
                vArray[2] = new Vertex(ov[2]);
                vArray[3] = new Vertex(ov[3]);
            } else {
                if (subVertex) {
                    Matrix matrix = parent5.accurateProductMatrix;
                    BigDecimal[] P1 = matrix.transform(gd5.X1, gd5.Y1, gd5.Z1);
                    BigDecimal[] P2 = matrix.transform(gd5.X2, gd5.Y2, gd5.Z2);
                    BigDecimal[] P3 = matrix.transform(gd5.X3, gd5.Y3, gd5.Z3);
                    BigDecimal[] P4 = matrix.transform(gd5.X4, gd5.Y4, gd5.Z4);
                    vArray[0] = new Vertex(P1[0], P1[1], P1[2], ov[0]);
                    vArray[1] = new Vertex(P2[0], P2[1], P2[2], ov[1]);
                    vArray[2] = new Vertex(P3[0], P3[1], P3[2], ov[2]);
                    vArray[3] = new Vertex(P4[0], P4[1], P4[2], ov[3]);
                } else {
                    vArray[0] = new Vertex(gd5.X1, gd5.Y1, gd5.Z1, ov[0]);
                    vArray[1] = new Vertex(gd5.X2, gd5.Y2, gd5.Z2, ov[1]);
                    vArray[2] = new Vertex(gd5.X3, gd5.Y3, gd5.Z3, ov[2]);
                    vArray[3] = new Vertex(gd5.X4, gd5.Y4, gd5.Z4, ov[3]);
                }
            }
            condlines.put(gd5, Arrays.copyOf(vArray, 4));
            max = 4;
            break;
        default:
            max = 0;
            subVertex = false;
            break;
        }

        if (subVertex) {
            if (!vertexCountInSubfile.containsKey(gdata)) {
                vertexCountInSubfile.put((GData1) gdata, 0);
            }
            int vertexCount = vertexCountInSubfile.get(gdata);
            for (int i = 0; i < max; i++) {
                vertexCount--;

                if (!vertexLinkedToPositionInFile.containsKey(vArray[i])) {
                    vertexLinkedToPositionInFile.put(vArray[i], Collections.newSetFromMap(new ThreadsafeHashMap<VertexManifestation, Boolean>()));
                }

                vertexLinkedToPositionInFile.get(vArray[i]).add(vdArray[i]);

                if (!vertexLinkedToSubfile.containsKey(vArray[i])) {
                    vertexLinkedToSubfile.put(vArray[i], Collections.newSetFromMap(new ThreadsafeHashMap<GData1, Boolean>()));
                }

                vertexLinkedToSubfile.get(vArray[i]).add((GData1) gdata);

                if (!lineLinkedToVertices.containsKey(gdata)) {
                    lineLinkedToVertices.put(gdata, new HashSet<VertexInfo>());
                }

                lineLinkedToVertices.get(gdata).add(new VertexInfo(vArray[i], vertexCount, originalData));
            }
            vertexCountInSubfile.put((GData1) gdata, vertexCount);
        } else {
            for (int i = 0; i < max; i++) {
                if (!vertexLinkedToPositionInFile.containsKey(vArray[i])) {
                    vertexLinkedToPositionInFile.put(vArray[i], Collections.newSetFromMap(new ThreadsafeHashMap<VertexManifestation, Boolean>()));
                }

                vertexLinkedToPositionInFile.get(vArray[i]).add(vdArray[i]);

                if (!lineLinkedToVertices.containsKey(gdata)) {
                    lineLinkedToVertices.put(gdata, Collections.newSetFromMap(new ThreadsafeHashMap<VertexInfo, Boolean>()));
                }

                lineLinkedToVertices.get(gdata).add(new VertexInfo(vArray[i], vdArray[i].getPosition(), gdata));
            }
        }
    }

    /**
     *
     * @param gdata
     * @return {@code true} if the tail was removed
     */
    public synchronized boolean remove(final GData gdata) {
        if (gdata == null)
            return false;
        final Set<VertexInfo> lv = lineLinkedToVertices.get(gdata);
        Set<VertexManifestation> vd;
        switch (gdata.type()) {
        case 0: // Vertex Reference
            declaredVertices.remove(gdata);
            lineLinkedToVertices.remove(gdata);
            if (lv == null)
                break;
            for (VertexInfo vertexInfo : lv) {
                Vertex vertex = vertexInfo.vertex;
                int position = vertexInfo.position;
                vd = vertexLinkedToPositionInFile.get(vertex);
                vd.remove(new VertexManifestation(position, gdata));
                if (vd.isEmpty())
                    vertexLinkedToPositionInFile.remove(vertex);
            }
            break;
        case 1: // Subfile
            lineLinkedToVertices.remove(gdata);
            vertexCountInSubfile.remove(gdata);
            if (lv == null)
                break;
            for (VertexInfo vertexInfo : lv) {
                Vertex vertex = vertexInfo.vertex;
                vd = vertexLinkedToPositionInFile.get(vertex);
                GData linkedData = vertexInfo.linkedData;
                switch (linkedData.type()) {
                case 0:
                    if (vd != null) {
                        declaredVertices.remove(linkedData);
                        vd.remove(new VertexManifestation(0, linkedData));
                        if (vd.isEmpty())
                            vertexLinkedToPositionInFile.remove(vertex);
                    }
                    break;
                case 2:
                    lines.remove(linkedData);
                    if (vd != null) {
                        vd.remove(new VertexManifestation(0, linkedData));
                        vd.remove(new VertexManifestation(1, linkedData));
                        if (vd.isEmpty())
                            vertexLinkedToPositionInFile.remove(vertex);
                    }
                    break;
                case 3:
                    triangles.remove(linkedData);
                    if (vd != null) {
                        vd.remove(new VertexManifestation(0, linkedData));
                        vd.remove(new VertexManifestation(1, linkedData));
                        vd.remove(new VertexManifestation(2, linkedData));
                        if (vd.isEmpty())
                            vertexLinkedToPositionInFile.remove(vertex);
                    }
                    break;
                case 4:
                    quads.remove(linkedData);
                    if (vd != null) {
                        vd.remove(new VertexManifestation(0, linkedData));
                        vd.remove(new VertexManifestation(1, linkedData));
                        vd.remove(new VertexManifestation(2, linkedData));
                        vd.remove(new VertexManifestation(3, linkedData));
                        if (vd.isEmpty())
                            vertexLinkedToPositionInFile.remove(vertex);
                    }
                    break;
                case 5:
                    condlines.remove(linkedData);
                    if (vd != null) {
                        vd.remove(new VertexManifestation(0, linkedData));
                        vd.remove(new VertexManifestation(1, linkedData));
                        vd.remove(new VertexManifestation(2, linkedData));
                        vd.remove(new VertexManifestation(3, linkedData));
                        if (vd.isEmpty())
                            vertexLinkedToPositionInFile.remove(vertex);
                    }
                    break;
                default:
                    throw new AssertionError();
                }
                Set<GData1> vs = vertexLinkedToSubfile.get(vertex);
                if (vs != null) { // The same vertex can be used by different
                    // elements from the subfile
                    vs.remove(gdata);
                    if (vs.isEmpty())
                        vertexLinkedToSubfile.remove(vertex);
                }
            }
            break;
        case 2: // Line
            lines.remove(gdata);
            lineLinkedToVertices.remove(gdata);
            if (lv == null)
                break;
            for (VertexInfo vertexInfo : lv) {
                Vertex vertex = vertexInfo.vertex;
                int position = vertexInfo.position;
                vd = vertexLinkedToPositionInFile.get(vertex);
                vd.remove(new VertexManifestation(position, gdata));
                if (vd.isEmpty())
                    vertexLinkedToPositionInFile.remove(vertex);
            }
            break;
        case 3: // Triangle
            triangles.remove(gdata);
            lineLinkedToVertices.remove(gdata);
            if (lv == null)
                break;
            for (VertexInfo vertexInfo : lv) {
                Vertex vertex = vertexInfo.vertex;
                int position = vertexInfo.position;
                vd = vertexLinkedToPositionInFile.get(vertex);
                vd.remove(new VertexManifestation(position, gdata));
                if (vd.isEmpty())
                    vertexLinkedToPositionInFile.remove(vertex);
            }
            break;
        case 4: // Quad
            quads.remove(gdata);
            lineLinkedToVertices.remove(gdata);
            if (lv == null)
                break;
            for (VertexInfo vertexInfo : lv) {
                Vertex vertex = vertexInfo.vertex;
                int position = vertexInfo.position;
                vd = vertexLinkedToPositionInFile.get(vertex);
                vd.remove(new VertexManifestation(position, gdata));
                if (vd.isEmpty())
                    vertexLinkedToPositionInFile.remove(vertex);
            }
            break;
        case 5: // Optional Line
            condlines.remove(gdata);
            lineLinkedToVertices.remove(gdata);
            if (lv == null)
                break;
            for (VertexInfo vertexInfo : lv) {
                Vertex vertex = vertexInfo.vertex;
                int position = vertexInfo.position;
                vd = vertexLinkedToPositionInFile.get(vertex);
                vd.remove(new VertexManifestation(position, gdata));
                if (vd.isEmpty())
                    vertexLinkedToPositionInFile.remove(vertex);
            }
            break;
        case 10:
            if (gdata.equals(selectedBgPicture)) {
                selectedBgPicture = null;
                if (!((GDataPNG) gdata).isGoingToBeReplaced())  Editor3DWindow.getWindow().updateBgPictureTab();
            }
            break;
        default:
            break;
        }
        gdata.derefer();
        boolean tailRemoved = gdata.equals(linkedDatFile.getDrawChainTail());
        if (tailRemoved) linkedDatFile.setDrawChainTail(null);
        return tailRemoved;
    }

    /**
     * Validates the current data structure against dead references and other
     * inconsistencies. All calls to this method will be "suppressed" in the release version.
     * Except the correction of 'trivial' selection inconsistancies
     */
    public synchronized void validateState() {
        // Validate and auto-correct selection inconsistancies
        if (selectedData.size() != selectedSubfiles.size() + selectedLines.size() + selectedTriangles.size() + selectedQuads.size() + selectedCondlines.size()) {
            // throw new AssertionError("The selected data is not equal to the content of single selection classes, e.g. 'selectedTriangles'."); //$NON-NLS-1$
            selectedData.clear();
            for (Iterator<GData1> gi = selectedSubfiles.iterator(); gi.hasNext();) {
                GData g = gi.next();
                if (!exist(g)) {
                    gi.remove();
                }
            }
            for (Iterator<GData2> gi = selectedLines.iterator(); gi.hasNext();) {
                GData g = gi.next();
                if (!exist(g)) {
                    gi.remove();
                }
            }
            for (Iterator<GData3> gi = selectedTriangles.iterator(); gi.hasNext();) {
                GData g = gi.next();
                if (!exist(g)) {
                    gi.remove();
                }
            }
            for (Iterator<GData4> gi = selectedQuads.iterator(); gi.hasNext();) {
                GData g = gi.next();
                if (!exist(g)) {
                    gi.remove();
                }
            }
            for (Iterator<GData5> gi = selectedCondlines.iterator(); gi.hasNext();) {
                GData g = gi.next();
                if (!exist(g)) {
                    gi.remove();
                }
            }
            selectedData.addAll(selectedSubfiles);
            selectedData.addAll(selectedLines);
            selectedData.addAll(selectedTriangles);
            selectedData.addAll(selectedQuads);
            selectedData.addAll(selectedCondlines);
        }
        cleanupSelection();
        // Do not validate more stuff on release, since it costs a lot performance.
        if (!NLogger.DEBUG) return;

        // TreeMap<Vertex, HashSet<VertexManifestation>>
        // vertexLinkedToPositionInFile
        // TreeMap<Vertex, HashSet<GData1>> vertexLinkedToSubfile
        // HashMap<GData, HashSet<VertexInfo>> lineLinkedToVertices

        // HashMap<GData1, Integer> vertexCountInSubfile

        // HashMap<GData2, Vertex[]> lines
        // HashMap<GData3, Vertex[]> triangles
        // HashMap<GData4, Vertex[]> quads
        // HashMap<GData5, Vertex[]> condlines

        // TreeSet<Vertex> selectedVertices

        Set<Vertex> vertices = vertexLinkedToPositionInFile.keySet();
        Set<Vertex> verticesInUse = new TreeSet<Vertex>();
        for (GData0 line : declaredVertices.keySet()) {
            for (Vertex vertex : declaredVertices.get(line)) {
                verticesInUse.add(vertex);
            }
        }
        for (GData2 line : lines.keySet()) {
            for (Vertex vertex : lines.get(line)) {
                verticesInUse.add(vertex);
            }
        }
        for (GData3 line : triangles.keySet()) {
            for (Vertex vertex : triangles.get(line)) {
                verticesInUse.add(vertex);
            }
        }
        for (GData4 line : quads.keySet()) {
            for (Vertex vertex : quads.get(line)) {
                verticesInUse.add(vertex);
            }
        }
        for (GData5 line : condlines.keySet()) {
            for (Vertex vertex : condlines.get(line)) {
                verticesInUse.add(vertex);
            }
        }

        int vertexCount = vertices.size();
        int vertexUseCount = verticesInUse.size();

        if (vertexCount != vertexUseCount) {
            throw new AssertionError("The number of used vertices is not equal to the number of all available vertices."); //$NON-NLS-1$
        }

        // Validate Render Chain
        HashBiMap<Integer, GData> lineMap = linkedDatFile.getDrawPerLine();

        verticesInUse.clear();

        if (lineMap.getValue(1) == null)
            throw new AssertionError("The first line can't be null."); //$NON-NLS-1$

        GData previousData = lineMap.getValue(1).getBefore();
        TreeSet<Integer> lineNumbers = new TreeSet<Integer>(lineMap.keySet());
        boolean nullReferenceFound = false;
        for (Integer lineNumber : lineNumbers) {
            if (nullReferenceFound)
                throw new AssertionError("The reference to the next data is null but the next data is a real instance."); //$NON-NLS-1$
            GData currentData = lineMap.getValue(lineNumber);
            Set<VertexInfo> vi = lineLinkedToVertices.get(currentData);
            if (vi != null) {
                for (VertexInfo vertexInfo : vi) {
                    verticesInUse.add(vertexInfo.vertex);
                }
            }
            if (currentData.getBefore() == null)
                throw new AssertionError("The reference to the data before can't be null."); //$NON-NLS-1$
            if (!currentData.getBefore().equals(previousData))
                throw new AssertionError("The pointer to previous data directs to the wrong object."); //$NON-NLS-1$
            if (previousData.getNext() == null)
                throw new AssertionError("The reference to this before can't be null."); //$NON-NLS-1$
            if (!previousData.getNext().equals(currentData))
                throw new AssertionError("The pointer to next data directs to the wrong object."); //$NON-NLS-1$
            if (currentData.getNext() == null)
                nullReferenceFound = true;
            previousData = currentData;
        }

        if (!nullReferenceFound) {
            throw new AssertionError("Last pointer is not null."); //$NON-NLS-1$
        }

        vertexUseCount = verticesInUse.size();
        vertexUseCount = verticesInUse.size();

        if (vertexCount != vertexUseCount) {
            throw new AssertionError("The number of vertices displayed is not equal to the number of stored vertices."); //$NON-NLS-1$
        }
    }

    public synchronized void draw(Composite3D c3d) {
        if (!linkedDatFile.isDrawSelection()) return;
        Matrix4f vm = c3d.getViewport();
        Matrix4f ivm = c3d.getViewport_Inverse();

        Manipulator manipulator = c3d.getManipulator();
        FloatBuffer matrix = manipulator.getTempTransformation();

        if (c3d.isShowingVertices()) {
            Vector4f tr = new Vector4f(vm.m30, vm.m31, vm.m32 + 330f * c3d.getZoom(), 1f);
            Matrix4f.transform(ivm, tr, tr);
            GL11.glDisable(GL11.GL_LIGHTING);
            if (c3d.isShowingHiddenVertices()) {
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glColor3f(View.vertex_Colour_r[0] * .5f, View.vertex_Colour_g[0] * .5f, View.vertex_Colour_b[0] * .5f);
                GL11.glBegin(GL11.GL_POINTS);
                for (Vertex vertex : vertexLinkedToPositionInFile.keySet()) {
                    if (hiddenVertices.contains(vertex))
                        continue;
                    GL11.glVertex3f(vertex.x, vertex.y, vertex.z);
                }
                GL11.glEnd();
                GL11.glEnable(GL11.GL_DEPTH_TEST);
            }
            GL11.glColor3f(View.vertex_Colour_r[0], View.vertex_Colour_g[0], View.vertex_Colour_b[0]);
            GL11.glBegin(GL11.GL_POINTS);
            for (Vertex vertex : vertexLinkedToPositionInFile.keySet()) {
                if (hiddenVertices.contains(vertex))
                    continue;
                GL11.glVertex3f(vertex.x + tr.x, vertex.y + tr.y, vertex.z + tr.z);
            }
            GL11.glEnd();
        }
        if (!manipulator.isModified())
            GL11.glDisable(GL11.GL_DEPTH_TEST);

        GL11.glPushMatrix();
        // Matrix4f.transform(ivm, tr, tr);
        GL11.glMultMatrix(matrix);

        if (manipulator.isModified()) {
            Set<GData> alreadyMoved = new HashSet<GData>();

            Set<Vertex> allVertices = new TreeSet<Vertex>(selectedVertices);
            GL11.glLineWidth(2f);
            if (c3d.isShowingVertices()) {
                GL11.glBegin(GL11.GL_POINTS);
                for (Vertex vertex : selectedVertices) {
                    GL11.glColor3f(View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0]);
                    GL11.glVertex3f(vertex.x, vertex.y, vertex.z);
                    GL11.glColor3f(View.vertex_Colour_r[0], View.vertex_Colour_g[0], View.vertex_Colour_b[0]);
                }
                GL11.glEnd();
            }

            if (c3d.isLightOn())
                GL11.glEnable(GL11.GL_LIGHTING);

            Vertex[] verts;
            float nx;
            float ny;
            float nz;
            for (GData2 g2 : selectedLines) {
                if (g2.visible) {
                    dataToHide.add(g2);
                    g2.hide();
                }
                if ((verts = lines.get(g2)) != null) {
                    allVertices.add(verts[0]);
                    allVertices.add(verts[1]);
                }
            }
            for (GData3 g3 : selectedTriangles) {
                if (g3.visible) {
                    dataToHide.add(g3);
                    g3.hide();
                }
                if ((verts = triangles.get(g3)) != null) {
                    allVertices.add(verts[0]);
                    allVertices.add(verts[1]);
                    allVertices.add(verts[2]);
                }
            }
            for (GData4 g4 : selectedQuads) {
                if (g4.visible) {
                    dataToHide.add(g4);
                    g4.hide();
                }
                if ((verts = quads.get(g4)) != null) {
                    allVertices.add(verts[0]);
                    allVertices.add(verts[1]);
                    allVertices.add(verts[2]);
                    allVertices.add(verts[3]);
                }
            }
            for (GData5 g5 : selectedCondlines) {
                if (g5.visible) {
                    dataToHide.add(g5);
                    g5.hide();
                }
                if ((verts = condlines.get(g5)) != null) {
                    allVertices.add(verts[0]);
                    allVertices.add(verts[1]);
                }
            }
            for (Vertex vertex : allVertices) {
                Set<VertexManifestation> vms = vertexLinkedToPositionInFile.get(vertex);
                if (vms == null) continue;
                for (VertexManifestation m : vms) {
                    GData gd = m.getGdata();
                    if (alreadyMoved.contains(gd)) continue;
                    alreadyMoved.add(gd);
                    if (selectedData.contains(gd)) {
                        if (gd.visible) {
                            dataToHide.add(gd);
                            gd.hide();
                        }
                        switch (gd.type()) {
                        case 3:
                            GData3 gd3 = (GData3) gd;
                            Vertex[] triverts = triangles.get(gd3);
                            nx = (triverts[2].y - triverts[0].y) * (triverts[1].z - triverts[0].z) - (triverts[2].z - triverts[0].z) * (triverts[1].y - triverts[0].y);
                            ny = (triverts[2].z - triverts[0].z) * (triverts[1].x - triverts[0].x) - (triverts[2].x - triverts[0].x) * (triverts[1].z - triverts[0].z);
                            nz = (triverts[2].x - triverts[0].x) * (triverts[1].y - triverts[0].y) - (triverts[2].y - triverts[0].y) * (triverts[1].x - triverts[0].x);
                            GL11.glBegin(GL11.GL_TRIANGLES);
                            GL11.glColor3f(gd3.r, gd3.g, gd3.b);
                            GL11.glNormal3f(nx, ny, nz);
                            GL11.glVertex3f(triverts[0].x, triverts[0].y, triverts[0].z);
                            GL11.glVertex3f(triverts[1].x, triverts[1].y, triverts[1].z);
                            GL11.glVertex3f(triverts[2].x, triverts[2].y, triverts[2].z);
                            GL11.glNormal3f(-nx, -ny, -nz);
                            GL11.glVertex3f(triverts[0].x, triverts[0].y, triverts[0].z);
                            GL11.glVertex3f(triverts[2].x, triverts[2].y, triverts[2].z);
                            GL11.glVertex3f(triverts[1].x, triverts[1].y, triverts[1].z);
                            GL11.glEnd();
                            break;
                        case 4:
                            GData4 gd4 = (GData4) gd;
                            Vertex[] quadverts = quads.get(gd4);
                            final Vector3f[] normals = new Vector3f[] { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
                            {
                                final Vector3f[] lineVectors = new Vector3f[] { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
                                Vector3f.sub(new Vector3f(quadverts[1].x, quadverts[1].y, quadverts[1].z), new Vector3f(quadverts[0].x, quadverts[0].y, quadverts[0].z), lineVectors[0]);
                                Vector3f.sub(new Vector3f(quadverts[2].x, quadverts[2].y, quadverts[2].z), new Vector3f(quadverts[1].x, quadverts[1].y, quadverts[1].z), lineVectors[1]);
                                Vector3f.sub(new Vector3f(quadverts[3].x, quadverts[3].y, quadverts[3].z), new Vector3f(quadverts[2].x, quadverts[2].y, quadverts[2].z), lineVectors[2]);
                                Vector3f.sub(new Vector3f(quadverts[0].x, quadverts[0].y, quadverts[0].z), new Vector3f(quadverts[3].x, quadverts[3].y, quadverts[3].z), lineVectors[3]);
                                Vector3f.cross(lineVectors[0], lineVectors[1], normals[0]);
                                Vector3f.cross(lineVectors[1], lineVectors[2], normals[1]);
                                Vector3f.cross(lineVectors[2], lineVectors[3], normals[2]);
                                Vector3f.cross(lineVectors[3], lineVectors[0], normals[3]);
                            }
                            Vector3f normal = new Vector3f();
                            for (int i = 0; i < 4; i++) {
                                Vector3f.add(normals[i], normal, normal);
                            }
                            nx = -normal.x;
                            ny = -normal.y;
                            nz = -normal.z;
                            GL11.glBegin(GL11.GL_QUADS);
                            GL11.glColor3f(gd4.r, gd4.g, gd4.b);
                            GL11.glNormal3f(nx, ny, nz);
                            GL11.glVertex3f(quadverts[0].x, quadverts[0].y, quadverts[0].z);
                            GL11.glVertex3f(quadverts[1].x, quadverts[1].y, quadverts[1].z);
                            GL11.glVertex3f(quadverts[2].x, quadverts[2].y, quadverts[2].z);
                            GL11.glVertex3f(quadverts[3].x, quadverts[3].y, quadverts[3].z);
                            GL11.glNormal3f(-nx, -ny, -nz);
                            GL11.glVertex3f(quadverts[0].x, quadverts[0].y, quadverts[0].z);
                            GL11.glVertex3f(quadverts[3].x, quadverts[3].y, quadverts[3].z);
                            GL11.glVertex3f(quadverts[2].x, quadverts[2].y, quadverts[2].z);
                            GL11.glVertex3f(quadverts[1].x, quadverts[1].y, quadverts[1].z);
                            GL11.glEnd();
                            break;
                        default:
                            break;
                        }
                    } else if (lineLinkedToVertices.containsKey(gd) && Editor3DWindow.getWindow().isMovingAdjacentData()) {
                        if (gd.visible) {
                            dataToHide.add(gd);
                            gd.hide();
                        }
                        switch (gd.type()) {
                        case 2:
                            Vertex[] lineverts2 = lines.get(gd);
                            Vertex[] lineverts = lineverts2.clone();
                            for (int i = 0; i < 2; i++) {
                                Vertex v2 = lineverts[i];
                                if (!v2.equals(vertex)) {
                                    Vector4f res = manipulator.getUntransformed(v2.x, v2.y, v2.z);
                                    lineverts[i] = new Vertex(res.x, res.y, res.z, true);
                                }
                            }
                            GL11.glBegin(GL11.GL_LINES);
                            GL11.glColor3f(View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0]);
                            GL11.glVertex3f(lineverts[0].x, lineverts[0].y, lineverts[0].z);
                            GL11.glVertex3f(lineverts[1].x, lineverts[1].y, lineverts[1].z);
                            GL11.glColor3f(View.vertex_Colour_r[0], View.vertex_Colour_g[0], View.vertex_Colour_b[0]);
                            GL11.glEnd();
                            break;
                        case 3:
                            GData3 gd3 = (GData3) gd;
                            Vertex[] triverts2 = triangles.get(gd3);
                            Vertex[] triverts = triverts2.clone();
                            for (int i = 0; i < 3; i++) {
                                Vertex v3 = triverts[i];
                                if (!allVertices.contains(v3)) {
                                    Vector4f res = manipulator.getUntransformed(v3.x, v3.y, v3.z);
                                    triverts[i] = new Vertex(res.x, res.y, res.z, true);
                                }
                            }
                            nx = (triverts[2].y - triverts[0].y) * (triverts[1].z - triverts[0].z) - (triverts[2].z - triverts[0].z) * (triverts[1].y - triverts[0].y);
                            ny = (triverts[2].z - triverts[0].z) * (triverts[1].x - triverts[0].x) - (triverts[2].x - triverts[0].x) * (triverts[1].z - triverts[0].z);
                            nz = (triverts[2].x - triverts[0].x) * (triverts[1].y - triverts[0].y) - (triverts[2].y - triverts[0].y) * (triverts[1].x - triverts[0].x);
                            GL11.glBegin(GL11.GL_TRIANGLES);
                            GL11.glColor3f(gd3.r, gd3.g, gd3.b);
                            GL11.glNormal3f(nx, ny, nz);
                            GL11.glVertex3f(triverts[0].x, triverts[0].y, triverts[0].z);
                            GL11.glVertex3f(triverts[1].x, triverts[1].y, triverts[1].z);
                            GL11.glVertex3f(triverts[2].x, triverts[2].y, triverts[2].z);
                            GL11.glNormal3f(-nx, -ny, -nz);
                            GL11.glVertex3f(triverts[0].x, triverts[0].y, triverts[0].z);
                            GL11.glVertex3f(triverts[2].x, triverts[2].y, triverts[2].z);
                            GL11.glVertex3f(triverts[1].x, triverts[1].y, triverts[1].z);
                            GL11.glEnd();
                            GL11.glBegin(GL11.GL_LINES);
                            GL11.glColor3f(View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0]);
                            GL11.glVertex3f(triverts[0].x, triverts[0].y, triverts[0].z);
                            GL11.glVertex3f(triverts[1].x, triverts[1].y, triverts[1].z);

                            GL11.glVertex3f(triverts[1].x, triverts[1].y, triverts[1].z);
                            GL11.glVertex3f(triverts[2].x, triverts[2].y, triverts[2].z);

                            GL11.glVertex3f(triverts[2].x, triverts[2].y, triverts[2].z);
                            GL11.glVertex3f(triverts[0].x, triverts[0].y, triverts[0].z);
                            GL11.glColor3f(View.vertex_Colour_r[0], View.vertex_Colour_g[0], View.vertex_Colour_b[0]);
                            GL11.glEnd();
                            break;
                        case 4:
                            GData4 gd4 = (GData4) gd;
                            Vertex[] quadverts2 = quads.get(gd4);
                            Vertex[] quadverts = quadverts2.clone();
                            for (int i = 0; i < 4; i++) {
                                Vertex v4 = quadverts[i];
                                if (!allVertices.contains(v4)) {
                                    Vector4f res = manipulator.getUntransformed(v4.x, v4.y, v4.z);
                                    quadverts[i] = new Vertex(res.x, res.y, res.z, true);
                                }
                            }
                            final Vector3f[] normals = new Vector3f[] { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
                            {
                                final Vector3f[] lineVectors = new Vector3f[] { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
                                Vector3f.sub(new Vector3f(quadverts[1].x, quadverts[1].y, quadverts[1].z), new Vector3f(quadverts[0].x, quadverts[0].y, quadverts[0].z), lineVectors[0]);
                                Vector3f.sub(new Vector3f(quadverts[2].x, quadverts[2].y, quadverts[2].z), new Vector3f(quadverts[1].x, quadverts[1].y, quadverts[1].z), lineVectors[1]);
                                Vector3f.sub(new Vector3f(quadverts[3].x, quadverts[3].y, quadverts[3].z), new Vector3f(quadverts[2].x, quadverts[2].y, quadverts[2].z), lineVectors[2]);
                                Vector3f.sub(new Vector3f(quadverts[0].x, quadverts[0].y, quadverts[0].z), new Vector3f(quadverts[3].x, quadverts[3].y, quadverts[3].z), lineVectors[3]);
                                Vector3f.cross(lineVectors[0], lineVectors[1], normals[0]);
                                Vector3f.cross(lineVectors[1], lineVectors[2], normals[1]);
                                Vector3f.cross(lineVectors[2], lineVectors[3], normals[2]);
                                Vector3f.cross(lineVectors[3], lineVectors[0], normals[3]);
                            }
                            Vector3f normal = new Vector3f();
                            for (int i = 0; i < 4; i++) {
                                Vector3f.add(normals[i], normal, normal);
                            }
                            nx = -normal.x;
                            ny = -normal.y;
                            nz = -normal.z;
                            GL11.glBegin(GL11.GL_QUADS);
                            GL11.glColor3f(gd4.r, gd4.g, gd4.b);
                            GL11.glNormal3f(nx, ny, nz);
                            GL11.glVertex3f(quadverts[0].x, quadverts[0].y, quadverts[0].z);
                            GL11.glVertex3f(quadverts[1].x, quadverts[1].y, quadverts[1].z);
                            GL11.glVertex3f(quadverts[2].x, quadverts[2].y, quadverts[2].z);
                            GL11.glVertex3f(quadverts[3].x, quadverts[3].y, quadverts[3].z);
                            GL11.glNormal3f(-nx, -ny, -nz);
                            GL11.glVertex3f(quadverts[0].x, quadverts[0].y, quadverts[0].z);
                            GL11.glVertex3f(quadverts[3].x, quadverts[3].y, quadverts[3].z);
                            GL11.glVertex3f(quadverts[2].x, quadverts[2].y, quadverts[2].z);
                            GL11.glVertex3f(quadverts[1].x, quadverts[1].y, quadverts[1].z);
                            GL11.glEnd();
                            GL11.glBegin(GL11.GL_LINES);
                            GL11.glColor3f(View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0]);
                            GL11.glVertex3f(quadverts[0].x, quadverts[0].y, quadverts[0].z);
                            GL11.glVertex3f(quadverts[1].x, quadverts[1].y, quadverts[1].z);

                            GL11.glVertex3f(quadverts[1].x, quadverts[1].y, quadverts[1].z);
                            GL11.glVertex3f(quadverts[2].x, quadverts[2].y , quadverts[2].z);

                            GL11.glVertex3f(quadverts[2].x, quadverts[2].y, quadverts[2].z);
                            GL11.glVertex3f(quadverts[3].x, quadverts[3].y, quadverts[3].z);

                            GL11.glVertex3f(quadverts[3].x, quadverts[3].y, quadverts[3].z);
                            GL11.glVertex3f(quadverts[0].x, quadverts[0].y, quadverts[0].z);
                            GL11.glColor3f(View.vertex_Colour_r[0], View.vertex_Colour_g[0], View.vertex_Colour_b[0]);
                            GL11.glEnd();
                            break;
                        case 5:
                            Vertex[] condverts2 = condlines.get(gd);
                            Vertex[] condverts = condverts2.clone();
                            for (int i = 0; i < 2; i++) {
                                Vertex v5 = condverts[i];
                                if (!v5.equals(vertex)) {
                                    Vector4f res = manipulator.getUntransformed(v5.x, v5.y, v5.z);
                                    condverts[i] = new Vertex(res.x, res.y, res.z, true);
                                }
                            }
                            GL11.glBegin(GL11.GL_LINES);
                            GL11.glColor3f(View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0]);
                            GL11.glVertex3f(condverts[0].x, condverts[0].y, condverts[0].z);
                            GL11.glVertex3f(condverts[1].x, condverts[1].y, condverts[1].z);
                            GL11.glColor3f(View.vertex_Colour_r[0], View.vertex_Colour_g[0], View.vertex_Colour_b[0]);
                            GL11.glEnd();
                            break;
                        default:
                            break;
                        }
                    }
                }
            }
        } else if (c3d.isShowingVertices()) {
            GL11.glBegin(GL11.GL_POINTS);
            for (Vertex vertex : selectedVertices) {
                if (hiddenVertices.contains(vertex))
                    continue;
                GL11.glColor3f(View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0]);
                GL11.glVertex3f(vertex.x, vertex.y, vertex.z);
                GL11.glColor3f(View.vertex_Colour_r[0], View.vertex_Colour_g[0], View.vertex_Colour_b[0]);
            }
            GL11.glEnd();
        }

        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        if (c3d.isLightOn())
            GL11.glEnable(GL11.GL_LIGHTING);

        if (c3d.isMeshLines()) {
            Vector4f tr2 = new Vector4f(vm.m30, vm.m31, vm.m32 + 300f * c3d.getZoom(), 1f);
            Matrix4f.transform(ivm, tr2, tr2);

            GL11.glLineWidth(2f);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glColor3f(View.line_Colour_r[0], View.line_Colour_g[0], View.line_Colour_b[0]);

            if (c3d.isSubMeshLines()) {
                Vertex[] quadverts = new Vertex[4];
                for (GData3 gdata : triangles.keySet()) {
                    if (!gdata.visible) // if (hiddenData.contains(gdata))
                        continue;
                    GL11.glBegin(GL11.GL_LINE_LOOP);
                    for (Vertex vertex : triangles.get(gdata)) {
                        GL11.glVertex3f(vertex.x + tr2.x, vertex.y + tr2.y, vertex.z + tr2.z);
                    }
                    GL11.glEnd();
                }
                for (GData4 gdata : quads.keySet()) {
                    if (!gdata.visible)
                        continue;
                    quadverts = quads.get(gdata);
                    GL11.glBegin(GL11.GL_LINES);
                    GL11.glVertex3f(quadverts[0].x + tr2.x, quadverts[0].y + tr2.y, quadverts[0].z + tr2.z);
                    GL11.glVertex3f(quadverts[1].x + tr2.x, quadverts[1].y + tr2.y, quadverts[1].z + tr2.z);

                    GL11.glVertex3f(quadverts[1].x + tr2.x, quadverts[1].y + tr2.y, quadverts[1].z + tr2.z);
                    GL11.glVertex3f(quadverts[2].x + tr2.x, quadverts[2].y + tr2.y, quadverts[2].z + tr2.z);

                    GL11.glVertex3f(quadverts[2].x + tr2.x, quadverts[2].y + tr2.y, quadverts[2].z + tr2.z);
                    GL11.glVertex3f(quadverts[3].x + tr2.x, quadverts[3].y + tr2.y, quadverts[3].z + tr2.z);

                    GL11.glVertex3f(quadverts[3].x + tr2.x, quadverts[3].y + tr2.y, quadverts[3].z + tr2.z);
                    GL11.glVertex3f(quadverts[0].x + tr2.x, quadverts[0].y + tr2.y, quadverts[0].z + tr2.z);
                    GL11.glEnd();
                }
            } else {
                Vertex[] quadverts = new Vertex[4];
                for (GData gdata : lineLinkedToVertices.keySet()) {
                    if (!gdata.visible)
                        continue;
                    switch (gdata.type()) {
                    case 3:
                        GL11.glBegin(GL11.GL_LINE_LOOP);
                        for (VertexInfo info : lineLinkedToVertices.get(gdata)) {
                            Vertex triVertex = info.vertex;
                            GL11.glVertex3f(triVertex.x + tr2.x, triVertex.y + tr2.y, triVertex.z + tr2.z);
                        }
                        GL11.glEnd();
                        break;
                    case 4:
                        for (VertexInfo info : lineLinkedToVertices.get(gdata)) {
                            quadverts[info.position] = info.vertex;
                        }
                        GL11.glBegin(GL11.GL_LINES);
                        GL11.glVertex3f(quadverts[0].x + tr2.x, quadverts[0].y + tr2.y, quadverts[0].z + tr2.z);
                        GL11.glVertex3f(quadverts[1].x + tr2.x, quadverts[1].y + tr2.y, quadverts[1].z + tr2.z);

                        GL11.glVertex3f(quadverts[1].x + tr2.x, quadverts[1].y + tr2.y, quadverts[1].z + tr2.z);
                        GL11.glVertex3f(quadverts[2].x + tr2.x, quadverts[2].y + tr2.y, quadverts[2].z + tr2.z);

                        GL11.glVertex3f(quadverts[2].x + tr2.x, quadverts[2].y + tr2.y, quadverts[2].z + tr2.z);
                        GL11.glVertex3f(quadverts[3].x + tr2.x, quadverts[3].y + tr2.y, quadverts[3].z + tr2.z);

                        GL11.glVertex3f(quadverts[3].x + tr2.x, quadverts[3].y + tr2.y, quadverts[3].z + tr2.z);
                        GL11.glVertex3f(quadverts[0].x + tr2.x, quadverts[0].y + tr2.y, quadverts[0].z + tr2.z);
                        GL11.glEnd();
                        break;
                    default:
                        break;
                    }
                }
            }

            if (c3d.isLightOn())
                GL11.glEnable(GL11.GL_LIGHTING);
        }


        if (!selectedData.isEmpty()) {

            GL11.glPushMatrix();

            Vector4f tr = new Vector4f(vm.m30, vm.m31, vm.m32 + 330f * c3d.getZoom(), 1f);
            Matrix4f.transform(ivm, tr, tr);

            GL11.glTranslatef(tr.x, tr.y, tr.z);
            GL11.glMultMatrix(matrix);

            if (!manipulator.isModified())
                GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glColor3f(View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0]);
            Vertex[] dataVerts = new Vertex[4];
            int i = 0;
            if (!selectedLines.isEmpty()) {
                GL11.glLineWidth(3f);
                GL11.glBegin(GL11.GL_LINES);
                for (GData2 line : selectedLines) {
                    i = 0;
                    if (lines.get(line) != null) {
                        for (Vertex tvertex : lines.get(line)) {
                            dataVerts[i] = tvertex;
                            i++;
                        }
                        GL11.glVertex3f(dataVerts[0].x, dataVerts[0].y, dataVerts[0].z);
                        GL11.glVertex3f(dataVerts[1].x, dataVerts[1].y, dataVerts[1].z);
                    }
                }
                GL11.glEnd();
            }
            if (!selectedCondlines.isEmpty()) {
                GL11.glLineWidth(3f);
                GL11.glBegin(GL11.GL_LINES);
                for (GData5 line : selectedCondlines) {
                    i = 0;
                    if (condlines.get(line) != null) {
                        for (Vertex tvertex : condlines.get(line)) {
                            dataVerts[i] = tvertex;
                            i++;
                        }
                        GL11.glVertex3f(dataVerts[0].x, dataVerts[0].y, dataVerts[0].z);
                        GL11.glVertex3f(dataVerts[1].x, dataVerts[1].y, dataVerts[1].z);
                    }
                }
                GL11.glEnd();
                GL11.glLineWidth(4f);
                GL11.glBegin(GL11.GL_LINES);
                for (GData5 line : selectedCondlines) {
                    i = 0;
                    if (condlines.get(line) != null) {
                        for (Vertex tvertex : condlines.get(line)) {
                            dataVerts[i] = tvertex;
                            i++;
                        }
                        GL11.glColor3f(View.condline_selected_Colour_r[0], View.condline_selected_Colour_g[0], View.condline_selected_Colour_b[0]);
                        GL11.glVertex3f(dataVerts[0].x, dataVerts[0].y, dataVerts[0].z);
                        GL11.glVertex3f(dataVerts[2].x, dataVerts[2].y, dataVerts[2].z);
                        GL11.glColor3f(View.condline_selected_Colour_r[0] / 2f, View.condline_selected_Colour_g[0] / 2f, View.condline_selected_Colour_b[0] / 2f);
                        GL11.glVertex3f(dataVerts[0].x, dataVerts[0].y, dataVerts[0].z);
                        GL11.glVertex3f(dataVerts[3].x, dataVerts[3].y, dataVerts[3].z);
                    }
                }
                GL11.glEnd();
                GL11.glColor3f(View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0]);
            }
            if (c3d.isFillingSelectedFaces()) {
                if (!selectedTriangles.isEmpty()) {
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    for (GData3 tri : selectedTriangles) {
                        i = 0;
                        if (triangles.get(tri) != null) {
                            for (Vertex tvertex : triangles.get(tri)) {
                                dataVerts[i] = tvertex;
                                i++;
                            }
                            GL11.glVertex3f(dataVerts[0].x, dataVerts[0].y, dataVerts[0].z);
                            GL11.glVertex3f(dataVerts[1].x, dataVerts[1].y, dataVerts[1].z);
                            GL11.glVertex3f(dataVerts[2].x, dataVerts[2].y, dataVerts[2].z);
                            GL11.glVertex3f(dataVerts[0].x, dataVerts[0].y, dataVerts[0].z);
                            GL11.glVertex3f(dataVerts[2].x, dataVerts[2].y, dataVerts[2].z);
                            GL11.glVertex3f(dataVerts[1].x, dataVerts[1].y, dataVerts[1].z);
                        }
                    }
                    GL11.glEnd();
                }
                if (!selectedQuads.isEmpty()) {
                    GL11.glBegin(GL11.GL_QUADS);
                    for (GData4 quad : selectedQuads) {
                        i = 0;
                        if (quads.get(quad) != null) {
                            for (Vertex tvertex : quads.get(quad)) {
                                dataVerts[i] = tvertex;
                                i++;
                            }
                            GL11.glVertex3f(dataVerts[0].x, dataVerts[0].y, dataVerts[0].z);
                            GL11.glVertex3f(dataVerts[1].x, dataVerts[1].y, dataVerts[1].z);
                            GL11.glVertex3f(dataVerts[2].x, dataVerts[2].y, dataVerts[2].z);
                            GL11.glVertex3f(dataVerts[3].x, dataVerts[3].y, dataVerts[3].z);
                            GL11.glVertex3f(dataVerts[2].x, dataVerts[2].y, dataVerts[2].z);
                            GL11.glVertex3f(dataVerts[1].x, dataVerts[1].y, dataVerts[1].z);
                            GL11.glVertex3f(dataVerts[0].x, dataVerts[0].y, dataVerts[0].z);
                            GL11.glVertex3f(dataVerts[3].x, dataVerts[3].y, dataVerts[3].z);
                        }
                    }
                    GL11.glEnd();
                }
            }
            if (!selectedTriangles.isEmpty()) {
                GL11.glLineWidth(2f);
                if (c3d.isFillingSelectedFaces())
                    GL11.glColor3f(View.line_Colour_r[0], View.line_Colour_g[0], View.line_Colour_b[0]);
                GL11.glBegin(GL11.GL_LINES);
                for (GData3 tri : selectedTriangles) {
                    i = 0;
                    if (triangles.get(tri) != null) {
                        for (Vertex tvertex : triangles.get(tri)) {
                            dataVerts[i] = tvertex;
                            i++;
                        }
                        GL11.glVertex3f(dataVerts[0].x, dataVerts[0].y, dataVerts[0].z);
                        GL11.glVertex3f(dataVerts[1].x, dataVerts[1].y, dataVerts[1].z);
                        GL11.glVertex3f(dataVerts[1].x, dataVerts[1].y, dataVerts[1].z);
                        GL11.glVertex3f(dataVerts[2].x, dataVerts[2].y, dataVerts[2].z);
                        GL11.glVertex3f(dataVerts[2].x, dataVerts[2].y, dataVerts[2].z);
                        GL11.glVertex3f(dataVerts[0].x, dataVerts[0].y, dataVerts[0].z);
                    }
                }
                GL11.glEnd();
                GL11.glColor3f(View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0]);
            }
            if (!selectedQuads.isEmpty()) {
                GL11.glLineWidth(2f);
                if (c3d.isFillingSelectedFaces())
                    GL11.glColor3f(View.line_Colour_r[0], View.line_Colour_g[0], View.line_Colour_b[0]);
                GL11.glBegin(GL11.GL_LINES);
                for (GData4 quad : selectedQuads) {
                    i = 0;
                    if (quads.get(quad) != null) {
                        for (Vertex tvertex : quads.get(quad)) {
                            dataVerts[i] = tvertex;
                            i++;
                        }
                        GL11.glVertex3f(dataVerts[0].x, dataVerts[0].y, dataVerts[0].z);
                        GL11.glVertex3f(dataVerts[1].x, dataVerts[1].y, dataVerts[1].z);
                        GL11.glVertex3f(dataVerts[1].x, dataVerts[1].y, dataVerts[1].z);
                        GL11.glVertex3f(dataVerts[2].x, dataVerts[2].y, dataVerts[2].z);
                        GL11.glVertex3f(dataVerts[2].x, dataVerts[2].y, dataVerts[2].z);
                        GL11.glVertex3f(dataVerts[3].x, dataVerts[3].y, dataVerts[3].z);
                        GL11.glVertex3f(dataVerts[3].x, dataVerts[3].y, dataVerts[3].z);
                        GL11.glVertex3f(dataVerts[0].x, dataVerts[0].y, dataVerts[0].z);
                    }
                }
                GL11.glEnd();
                GL11.glColor3f(View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0]);
            }
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            if (c3d.isLightOn())
                GL11.glEnable(GL11.GL_LIGHTING);

            GL11.glPopMatrix();
        }
    }

    public synchronized void showHidden() {
        for (GData gd : dataToHide) {
            gd.show();
        }
        dataToHide.clear();
    }

    /**
     * FOR TEXT EDITOR ONLY (Performace should be improved (has currently O(n)
     * runtime n=number of code lines)
     *
     * @param oldVertex
     * @param newVertex
     * @param modifyVertexMetaCommands
     * @return
     */
    public synchronized boolean changeVertexDirect(Vertex oldVertex, Vertex newVertex, boolean modifyVertexMetaCommands) {// ,
        // Set<GData>
        // modifiedData)
        // {
        HashBiMap<Integer, GData> drawPerLine = linkedDatFile.getDrawPerLine_NOCLONE();
        TreeSet<Integer> keys = new TreeSet<Integer>(drawPerLine.keySet());
        HashSet<GData> dataToRemove = new HashSet<GData>();
        boolean foundVertexDuplicate = false;
        for (Integer key : keys) {
            GData vm = linkedDatFile.getDrawPerLine().getValue(key);
            switch (vm.type()) {
            case 0:
                Vertex[] va = declaredVertices.get(vm);
                if (va != null) {
                    if (oldVertex.equals(va[0]))
                        dataToRemove.add(vm);
                    if (newVertex.equals(va[0])) {
                        if (modifyVertexMetaCommands) {
                            foundVertexDuplicate = true;
                        } else {
                            return false;
                        }
                    }
                }

                break;
            case 2:
                va = lines.get(vm);
                if (oldVertex.equals(va[0]) || oldVertex.equals(va[1])) {
                    if (newVertex.equals(va[0]) || newVertex.equals(va[1]))
                        return false;
                    dataToRemove.add(vm);
                }
                break;
            case 3:
                va = triangles.get(vm);
                if (oldVertex.equals(va[0]) || oldVertex.equals(va[1]) || oldVertex.equals(va[2])) {
                    if (newVertex.equals(va[0]) || newVertex.equals(va[1]) || newVertex.equals(va[2]))
                        return false;
                    dataToRemove.add(vm);
                }
                break;
            case 4:
                va = quads.get(vm);
                if (oldVertex.equals(va[0]) || oldVertex.equals(va[1]) || oldVertex.equals(va[2]) || oldVertex.equals(va[3])) {
                    if (newVertex.equals(va[0]) || newVertex.equals(va[1]) || newVertex.equals(va[2]) || newVertex.equals(va[3]))
                        return false;
                    dataToRemove.add(vm);
                }
                break;
            case 5:
                va = condlines.get(vm);
                if (oldVertex.equals(va[0]) || oldVertex.equals(va[1]) || oldVertex.equals(va[2]) || oldVertex.equals(va[3])) {
                    if (newVertex.equals(va[0]) || newVertex.equals(va[1]) || newVertex.equals(va[2]) || newVertex.equals(va[3]))
                        return false;
                    dataToRemove.add(vm);
                }
                break;
            default:
                break;
            }
        }

        boolean updateTail = false;

        for (GData gData : dataToRemove) {

            Integer oldNumber = drawPerLine.getKey(gData);

            switch (gData.type()) {
            case 0:
                if (foundVertexDuplicate)
                    break;
                GData0 gd0 = (GData0) gData;
                Vertex[] v0 = declaredVertices.get(gd0);

                updateTail = remove(gData) | updateTail;
                // modifiedData.remove(gData);

                if (v0[0].equals(oldVertex))
                    v0[0] = newVertex;

                GData0 newGdata0 = addVertex(newVertex);

                // modifiedData.add(newGdata0);
                drawPerLine.put(oldNumber, newGdata0);
                break;
            case 2:

                GData2 gd2 = (GData2) gData;
                Vertex[] v2 = lines.get(gd2);

                updateTail = remove(gData) | updateTail;
                // modifiedData.remove(gData);

                if (v2[0].equals(oldVertex))
                    v2[0] = newVertex;
                if (v2[1].equals(oldVertex))
                    v2[1] = newVertex;

                GData2 newGdata2 = new GData2(gd2.colourNumber, gd2.r, gd2.g, gd2.b, gd2.a, v2[0], v2[1], View.DUMMY_REFERENCE, linkedDatFile);

                // modifiedData.add(newGdata2);
                drawPerLine.put(oldNumber, newGdata2);
                break;
            case 3:

                GData3 gd3 = (GData3) gData;
                Vertex[] v3 = triangles.get(gd3);

                updateTail = remove(gData) | updateTail;
                // modifiedData.remove(gData);

                if (v3[0].equals(oldVertex))
                    v3[0] = newVertex;
                if (v3[1].equals(oldVertex))
                    v3[1] = newVertex;
                if (v3[2].equals(oldVertex))
                    v3[2] = newVertex;

                GData3 newGdata3 = new GData3(gd3.colourNumber, gd3.r, gd3.g, gd3.b, gd3.a, v3[0], v3[1], v3[2], View.DUMMY_REFERENCE, linkedDatFile);

                // modifiedData.add(newGdata3);
                drawPerLine.put(oldNumber, newGdata3);
                break;
            case 4:

                GData4 gd4 = (GData4) gData;
                Vertex[] v4 = quads.get(gd4);

                updateTail = remove(gData) | updateTail;
                // modifiedData.remove(gData);

                if (v4[0].equals(oldVertex))
                    v4[0] = newVertex;
                if (v4[1].equals(oldVertex))
                    v4[1] = newVertex;
                if (v4[2].equals(oldVertex))
                    v4[2] = newVertex;
                if (v4[3].equals(oldVertex))
                    v4[3] = newVertex;

                GData4 newGdata4 = new GData4(gd4.colourNumber, gd4.r, gd4.g, gd4.b, gd4.a, v4[0], v4[1], v4[2], v4[3], View.DUMMY_REFERENCE, linkedDatFile);

                // modifiedData.add(newGdata4);
                drawPerLine.put(oldNumber, newGdata4);
                break;
            case 5:

                GData5 gd5 = (GData5) gData;
                Vertex[] v5 = condlines.get(gd5);

                updateTail = remove(gData) | updateTail;
                // modifiedData.remove(gData);

                if (v5[0].equals(oldVertex))
                    v5[0] = newVertex;
                if (v5[1].equals(oldVertex))
                    v5[1] = newVertex;
                if (v5[2].equals(oldVertex))
                    v5[2] = newVertex;
                if (v5[3].equals(oldVertex))
                    v5[3] = newVertex;

                GData5 newGdata5 = new GData5(gd5.colourNumber, gd5.r, gd5.g, gd5.b, gd5.a, v5[0], v5[1], v5[2], v5[3], View.DUMMY_REFERENCE, linkedDatFile);

                // modifiedData.add(newGdata5);
                drawPerLine.put(oldNumber, newGdata5);
                break;
            }
        }

        // Linking:
        for (Integer key : keys) {
            GData val = drawPerLine.getValue(key);
            if (updateTail)
                linkedDatFile.setDrawChainTail(val);
            int k = key;
            if (k < 2) {
                linkedDatFile.getDrawChainStart().setNext(val);
            } else {
                GData val2 = drawPerLine.getValue(k - 1);
                val2.setNext(val);
            }
        }
        return true;
    }

    public synchronized boolean changeVertexDirectFast(Vertex oldVertex, Vertex newVertex, boolean moveAdjacentData) {

        GData tail = linkedDatFile.getDrawChainTail();

        // Collect the data to modify
        Set<VertexManifestation> manis2 = vertexLinkedToPositionInFile.get(oldVertex);
        if (manis2 == null || manis2.isEmpty())
            return false;
        HashSet<VertexManifestation> manis = new HashSet<VertexManifestation>(manis2);

        HashBiMap<Integer, GData> drawPerLine = linkedDatFile.getDrawPerLine_NOCLONE();

        for (VertexManifestation mani : manis) {
            GData oldData = mani.getGdata();
            if (!lineLinkedToVertices.containsKey(oldData))
                continue;
            GData newData = null;
            switch (oldData.type()) {
            case 0:
                GData0 oldVm = (GData0) oldData;
                GData0 newVm = null;
                Vertex[] va = declaredVertices.get(oldVm);
                if (va == null) {
                    continue;
                } else {
                    if (!moveAdjacentData && !selectedVertices.contains(va[0]))
                        continue;
                    if (va[0].equals(oldVertex))
                        va[0] = newVertex;
                    newVm = addVertex(va[0]);
                    newData = newVm;
                }
                break;
            case 2:
                GData2 oldLin = (GData2) oldData;
                if (!moveAdjacentData && !selectedLines.contains(oldLin))
                    continue;
                GData2 newLin = null;
                switch (mani.getPosition()) {
                case 0:
                    newLin = new GData2(oldLin.colourNumber, oldLin.r, oldLin.g, oldLin.b, oldLin.a, newVertex.X, newVertex.Y, newVertex.Z, oldLin.X2, oldLin.Y2, oldLin.Z2, oldLin.parent,
                            linkedDatFile);
                    break;
                case 1:
                    newLin = new GData2(oldLin.colourNumber, oldLin.r, oldLin.g, oldLin.b, oldLin.a, oldLin.X1, oldLin.Y1, oldLin.Z1, newVertex.X, newVertex.Y, newVertex.Z, oldLin.parent,
                            linkedDatFile);
                    break;
                }
                newData = newLin;
                if (selectedLines.contains(oldLin))
                    selectedLines.add(newLin);
                break;
            case 3:
                GData3 oldTri = (GData3) oldData;
                if (!moveAdjacentData && !selectedTriangles.contains(oldTri))
                    continue;
                GData3 newTri = null;
                switch (mani.getPosition()) {
                case 0:
                    newTri = new GData3(oldTri.colourNumber, oldTri.r, oldTri.g, oldTri.b, oldTri.a, newVertex, new Vertex(oldTri.X2, oldTri.Y2, oldTri.Z2),
                            new Vertex(oldTri.X3, oldTri.Y3, oldTri.Z3), oldTri.parent, linkedDatFile);
                    break;
                case 1:
                    newTri = new GData3(oldTri.colourNumber, oldTri.r, oldTri.g, oldTri.b, oldTri.a, new Vertex(oldTri.X1, oldTri.Y1, oldTri.Z1), newVertex,
                            new Vertex(oldTri.X3, oldTri.Y3, oldTri.Z3), oldTri.parent, linkedDatFile);
                    break;
                case 2:
                    newTri = new GData3(oldTri.colourNumber, oldTri.r, oldTri.g, oldTri.b, oldTri.a, new Vertex(oldTri.X1, oldTri.Y1, oldTri.Z1), new Vertex(oldTri.X2, oldTri.Y2, oldTri.Z2),
                            newVertex, oldTri.parent, linkedDatFile);
                    break;
                }
                newData = newTri;
                if (selectedTriangles.contains(oldTri))
                    selectedTriangles.add(newTri);
                break;
            case 4:
                GData4 oldQuad = (GData4) oldData;
                if (!moveAdjacentData && !selectedQuads.contains(oldQuad))
                    continue;
                GData4 newQuad = null;
                switch (mani.getPosition()) {
                case 0:
                    newQuad = new GData4(oldQuad.colourNumber, oldQuad.r, oldQuad.g, oldQuad.b, oldQuad.a, newVertex, new Vertex(oldQuad.X2, oldQuad.Y2, oldQuad.Z2), new Vertex(oldQuad.X3,
                            oldQuad.Y3, oldQuad.Z3), new Vertex(oldQuad.X4, oldQuad.Y4, oldQuad.Z4), oldQuad.parent, linkedDatFile);
                    break;
                case 1:
                    newQuad = new GData4(oldQuad.colourNumber, oldQuad.r, oldQuad.g, oldQuad.b, oldQuad.a, new Vertex(oldQuad.X1, oldQuad.Y1, oldQuad.Z1), newVertex, new Vertex(oldQuad.X3,
                            oldQuad.Y3, oldQuad.Z3), new Vertex(oldQuad.X4, oldQuad.Y4, oldQuad.Z4), oldQuad.parent, linkedDatFile);
                    break;
                case 2:
                    newQuad = new GData4(oldQuad.colourNumber, oldQuad.r, oldQuad.g, oldQuad.b, oldQuad.a, new Vertex(oldQuad.X1, oldQuad.Y1, oldQuad.Z1), new Vertex(oldQuad.X2, oldQuad.Y2,
                            oldQuad.Z2), newVertex, new Vertex(oldQuad.X4, oldQuad.Y4, oldQuad.Z4), oldQuad.parent, linkedDatFile);
                    break;
                case 3:
                    newQuad = new GData4(oldQuad.colourNumber, oldQuad.r, oldQuad.g, oldQuad.b, oldQuad.a, new Vertex(oldQuad.X1, oldQuad.Y1, oldQuad.Z1), new Vertex(oldQuad.X2, oldQuad.Y2,
                            oldQuad.Z2), new Vertex(oldQuad.X3, oldQuad.Y3, oldQuad.Z3), newVertex, oldQuad.parent, linkedDatFile);
                    break;
                }
                newData = newQuad;
                if (selectedQuads.contains(oldQuad))
                    selectedQuads.add(newQuad);
                break;
            case 5:
                GData5 oldCLin = (GData5) oldData;
                if (!moveAdjacentData && !selectedCondlines.contains(oldCLin))
                    continue;
                GData5 newCLin = null;
                switch (mani.getPosition()) {
                case 0:
                    newCLin = new GData5(oldCLin.colourNumber, oldCLin.r, oldCLin.g, oldCLin.b, oldCLin.a, newVertex, new Vertex(oldCLin.X2, oldCLin.Y2, oldCLin.Z2), new Vertex(oldCLin.X3,
                            oldCLin.Y3, oldCLin.Z3), new Vertex(oldCLin.X4, oldCLin.Y4, oldCLin.Z4), oldCLin.parent, linkedDatFile);
                    break;
                case 1:
                    newCLin = new GData5(oldCLin.colourNumber, oldCLin.r, oldCLin.g, oldCLin.b, oldCLin.a, new Vertex(oldCLin.X1, oldCLin.Y1, oldCLin.Z1), newVertex, new Vertex(oldCLin.X3,
                            oldCLin.Y3, oldCLin.Z3), new Vertex(oldCLin.X4, oldCLin.Y4, oldCLin.Z4), oldCLin.parent, linkedDatFile);
                    break;
                case 2:
                    newCLin = new GData5(oldCLin.colourNumber, oldCLin.r, oldCLin.g, oldCLin.b, oldCLin.a, new Vertex(oldCLin.X1, oldCLin.Y1, oldCLin.Z1), new Vertex(oldCLin.X2, oldCLin.Y2,
                            oldCLin.Z2), newVertex, new Vertex(oldCLin.X4, oldCLin.Y4, oldCLin.Z4), oldCLin.parent, linkedDatFile);
                    break;
                case 3:
                    newCLin = new GData5(oldCLin.colourNumber, oldCLin.r, oldCLin.g, oldCLin.b, oldCLin.a, new Vertex(oldCLin.X1, oldCLin.Y1, oldCLin.Z1), new Vertex(oldCLin.X2, oldCLin.Y2,
                            oldCLin.Z2), new Vertex(oldCLin.X3, oldCLin.Y3, oldCLin.Z3), newVertex, oldCLin.parent, linkedDatFile);
                    break;
                }
                newData = newCLin;
                if (selectedCondlines.contains(oldCLin))
                    selectedCondlines.add(newCLin);
                break;
            }

            if (selectedVertices.contains(oldVertex)) {
                selectedVertices.remove(oldVertex);
                selectedVertices.add(newVertex);
            }

            if (oldData.equals(tail))
                linkedDatFile.setDrawChainTail(newData);

            GData oldNext = oldData.getNext();
            GData oldBefore = oldData.getBefore();
            oldBefore.setNext(newData);
            newData.setNext(oldNext);
            Integer oldNumber = drawPerLine.getKey(oldData);
            if (oldNumber != null)
                drawPerLine.put(oldNumber, newData);
            remove(oldData);
        }

        return true;
    }


    public synchronized GData changeVertexDirectFast(Vertex oldVertex, Vertex newVertex, boolean moveAdjacentData, GData og) {

        GData tail = linkedDatFile.getDrawChainTail();

        // Collect the data to modify
        Set<VertexManifestation> manis2 = vertexLinkedToPositionInFile.get(oldVertex);
        if (manis2 == null || manis2.isEmpty())
            return og;
        HashSet<VertexManifestation> manis = new HashSet<VertexManifestation>(manis2);

        HashBiMap<Integer, GData> drawPerLine = linkedDatFile.getDrawPerLine_NOCLONE();

        for (VertexManifestation mani : manis) {
            GData oldData = mani.getGdata();
            if (!lineLinkedToVertices.containsKey(oldData))
                continue;
            GData newData = null;
            switch (oldData.type()) {
            case 0:
                GData0 oldVm = (GData0) oldData;
                GData0 newVm = null;
                Vertex[] va = declaredVertices.get(oldVm);
                if (va == null) {
                    continue;
                } else {
                    if (!moveAdjacentData && !selectedVertices.contains(va[0]))
                        continue;
                    if (va[0].equals(oldVertex))
                        va[0] = newVertex;
                    newVm = addVertex(va[0]);
                    newData = newVm;
                }
                break;
            case 2:
                GData2 oldLin = (GData2) oldData;
                if (!moveAdjacentData && !selectedLines.contains(oldLin))
                    continue;
                GData2 newLin = null;
                switch (mani.getPosition()) {
                case 0:
                    newLin = new GData2(oldLin.colourNumber, oldLin.r, oldLin.g, oldLin.b, oldLin.a, newVertex.X, newVertex.Y, newVertex.Z, oldLin.X2, oldLin.Y2, oldLin.Z2, oldLin.parent,
                            linkedDatFile);
                    break;
                case 1:
                    newLin = new GData2(oldLin.colourNumber, oldLin.r, oldLin.g, oldLin.b, oldLin.a, oldLin.X1, oldLin.Y1, oldLin.Z1, newVertex.X, newVertex.Y, newVertex.Z, oldLin.parent,
                            linkedDatFile);
                    break;
                }
                newData = newLin;
                if (selectedLines.contains(oldLin))
                    selectedLines.add(newLin);
                break;
            case 3:
                GData3 oldTri = (GData3) oldData;
                if (!moveAdjacentData && !selectedTriangles.contains(oldTri))
                    continue;
                GData3 newTri = null;
                switch (mani.getPosition()) {
                case 0:
                    newTri = new GData3(oldTri.colourNumber, oldTri.r, oldTri.g, oldTri.b, oldTri.a, newVertex, new Vertex(oldTri.X2, oldTri.Y2, oldTri.Z2),
                            new Vertex(oldTri.X3, oldTri.Y3, oldTri.Z3), oldTri.parent, linkedDatFile);
                    break;
                case 1:
                    newTri = new GData3(oldTri.colourNumber, oldTri.r, oldTri.g, oldTri.b, oldTri.a, new Vertex(oldTri.X1, oldTri.Y1, oldTri.Z1), newVertex,
                            new Vertex(oldTri.X3, oldTri.Y3, oldTri.Z3), oldTri.parent, linkedDatFile);
                    break;
                case 2:
                    newTri = new GData3(oldTri.colourNumber, oldTri.r, oldTri.g, oldTri.b, oldTri.a, new Vertex(oldTri.X1, oldTri.Y1, oldTri.Z1), new Vertex(oldTri.X2, oldTri.Y2, oldTri.Z2),
                            newVertex, oldTri.parent, linkedDatFile);
                    break;
                }
                newData = newTri;
                if (selectedTriangles.contains(oldTri))
                    selectedTriangles.add(newTri);
                break;
            case 4:
                GData4 oldQuad = (GData4) oldData;
                if (!moveAdjacentData && !selectedQuads.contains(oldQuad))
                    continue;
                GData4 newQuad = null;
                switch (mani.getPosition()) {
                case 0:
                    newQuad = new GData4(oldQuad.colourNumber, oldQuad.r, oldQuad.g, oldQuad.b, oldQuad.a, newVertex, new Vertex(oldQuad.X2, oldQuad.Y2, oldQuad.Z2), new Vertex(oldQuad.X3,
                            oldQuad.Y3, oldQuad.Z3), new Vertex(oldQuad.X4, oldQuad.Y4, oldQuad.Z4), oldQuad.parent, linkedDatFile);
                    break;
                case 1:
                    newQuad = new GData4(oldQuad.colourNumber, oldQuad.r, oldQuad.g, oldQuad.b, oldQuad.a, new Vertex(oldQuad.X1, oldQuad.Y1, oldQuad.Z1), newVertex, new Vertex(oldQuad.X3,
                            oldQuad.Y3, oldQuad.Z3), new Vertex(oldQuad.X4, oldQuad.Y4, oldQuad.Z4), oldQuad.parent, linkedDatFile);
                    break;
                case 2:
                    newQuad = new GData4(oldQuad.colourNumber, oldQuad.r, oldQuad.g, oldQuad.b, oldQuad.a, new Vertex(oldQuad.X1, oldQuad.Y1, oldQuad.Z1), new Vertex(oldQuad.X2, oldQuad.Y2,
                            oldQuad.Z2), newVertex, new Vertex(oldQuad.X4, oldQuad.Y4, oldQuad.Z4), oldQuad.parent, linkedDatFile);
                    break;
                case 3:
                    newQuad = new GData4(oldQuad.colourNumber, oldQuad.r, oldQuad.g, oldQuad.b, oldQuad.a, new Vertex(oldQuad.X1, oldQuad.Y1, oldQuad.Z1), new Vertex(oldQuad.X2, oldQuad.Y2,
                            oldQuad.Z2), new Vertex(oldQuad.X3, oldQuad.Y3, oldQuad.Z3), newVertex, oldQuad.parent, linkedDatFile);
                    break;
                }
                newData = newQuad;
                if (selectedQuads.contains(oldQuad))
                    selectedQuads.add(newQuad);
                break;
            case 5:
                GData5 oldCLin = (GData5) oldData;
                if (!moveAdjacentData && !selectedCondlines.contains(oldCLin))
                    continue;
                GData5 newCLin = null;
                switch (mani.getPosition()) {
                case 0:
                    newCLin = new GData5(oldCLin.colourNumber, oldCLin.r, oldCLin.g, oldCLin.b, oldCLin.a, newVertex, new Vertex(oldCLin.X2, oldCLin.Y2, oldCLin.Z2), new Vertex(oldCLin.X3,
                            oldCLin.Y3, oldCLin.Z3), new Vertex(oldCLin.X4, oldCLin.Y4, oldCLin.Z4), oldCLin.parent, linkedDatFile);
                    break;
                case 1:
                    newCLin = new GData5(oldCLin.colourNumber, oldCLin.r, oldCLin.g, oldCLin.b, oldCLin.a, new Vertex(oldCLin.X1, oldCLin.Y1, oldCLin.Z1), newVertex, new Vertex(oldCLin.X3,
                            oldCLin.Y3, oldCLin.Z3), new Vertex(oldCLin.X4, oldCLin.Y4, oldCLin.Z4), oldCLin.parent, linkedDatFile);
                    break;
                case 2:
                    newCLin = new GData5(oldCLin.colourNumber, oldCLin.r, oldCLin.g, oldCLin.b, oldCLin.a, new Vertex(oldCLin.X1, oldCLin.Y1, oldCLin.Z1), new Vertex(oldCLin.X2, oldCLin.Y2,
                            oldCLin.Z2), newVertex, new Vertex(oldCLin.X4, oldCLin.Y4, oldCLin.Z4), oldCLin.parent, linkedDatFile);
                    break;
                case 3:
                    newCLin = new GData5(oldCLin.colourNumber, oldCLin.r, oldCLin.g, oldCLin.b, oldCLin.a, new Vertex(oldCLin.X1, oldCLin.Y1, oldCLin.Z1), new Vertex(oldCLin.X2, oldCLin.Y2,
                            oldCLin.Z2), new Vertex(oldCLin.X3, oldCLin.Y3, oldCLin.Z3), newVertex, oldCLin.parent, linkedDatFile);
                    break;
                }
                newData = newCLin;
                if (selectedCondlines.contains(oldCLin))
                    selectedCondlines.add(newCLin);
                break;
            }

            if (selectedVertices.contains(oldVertex)) {
                selectedVertices.remove(oldVertex);
                selectedVertices.add(newVertex);
            }

            if (oldData.equals(tail))
                linkedDatFile.setDrawChainTail(newData);

            GData oldNext = oldData.getNext();
            GData oldBefore = oldData.getBefore();
            oldBefore.setNext(newData);
            newData.setNext(oldNext);
            Integer oldNumber = drawPerLine.getKey(oldData);
            if (oldNumber != null)
                drawPerLine.put(oldNumber, newData);
            if (oldData.equals(og)) {
                og = newData;
            }
            remove(oldData);
        }

        return og;
    }

    // For 2D Mesh Reducer only
    public synchronized void changeVertex(Vertex oldVertex, Vertex newVertex, Set<GData> modifiedData) {
        if (vertexLinkedToSubfile.containsKey(oldVertex))
            return;
        Set<VertexManifestation> vms = vertexLinkedToPositionInFile.get(oldVertex);
        ArrayList<GData> dataToRemove = new ArrayList<GData>();
        if (vms == null) {
            return;
        }
        for (VertexManifestation vm : vms) {
            dataToRemove.add(vm.getGdata());
        }

        for (GData gData : dataToRemove) {

            TreeSet<Vertex> verts = new TreeSet<Vertex>();

            switch (gData.type()) {
            case 3:
                Vertex[] va = triangles.get(gData);
                verts.add(va[0]);
                verts.add(va[1]);
                verts.add(va[2]);
                break;
            }

            if (verts.contains(oldVertex) && verts.contains(newVertex)) {
                GData oldNext = gData.getNext();
                if (gData.getBefore() != null) gData.getBefore().setNext(oldNext);
                remove(gData);
                modifiedData.remove(gData);
            } else {

                switch (gData.type()) {
                case 3:

                    GData3 gd3 = (GData3) gData;
                    Vertex[] v3 = triangles.get(gd3);

                    remove(gData);
                    modifiedData.remove(gData);

                    if (v3[0].equals(oldVertex))
                        v3[0] = newVertex;
                    if (v3[1].equals(oldVertex))
                        v3[1] = newVertex;
                    if (v3[2].equals(oldVertex))
                        v3[2] = newVertex;

                    GData3 newGdata3 = new GData3(gd3.colourNumber, gd3.r, gd3.g, gd3.b, gd3.a, v3[0], v3[1], v3[2], View.DUMMY_REFERENCE, linkedDatFile);

                    GData oldNext = gData.getNext();
                    if (gData.getBefore() != null) gData.getBefore().setNext(newGdata3);
                    newGdata3.setNext(oldNext);
                    modifiedData.add(newGdata3);
                    break;
                }
            }
        }
    }

    public synchronized GData changeColour(int index, float r, float g, float b, float a, GData dataToModify) {
        HashSet<GData> newSet = new HashSet<GData>();
        newSet.add(dataToModify);
        changeColour(index, r, g, b, a, newSet);
        if (newSet.iterator().hasNext()) {
            return newSet.iterator().next();
        } else {
            return null;
        }
    }

    public synchronized void changeColour(int index, float r, float g, float b, float a, Set<GData> dataToModify) {
        HashSet<GData> newData = new HashSet<GData>();
        GColour colour = View.getLDConfigColour(index);
        final float cr = colour.getR();
        final float cg = colour.getG();
        final float cb = colour.getB();
        final float ca = colour.getA();
        for (GData gData : dataToModify) {
            if (index > -1) {
                switch (gData.type()) {
                case 2:
                case 5:
                    if (index == 24) {
                        r = View.line_Colour_r[0];
                        g = View.line_Colour_g[0];
                        b = View.line_Colour_b[0];
                        a = 1f;
                    } else {
                        r = cr;
                        g = cg;
                        b = cb;
                        a = ca;
                    }
                    break;
                default:
                    r = cr;
                    g = cg;
                    b = cb;
                    a = ca;
                }
            }
            GData newGData = null;
            switch (gData.type()) {
            case 2:
                GData2 gd2 = (GData2) gData;
                GData2 newGdata2 = new GData2(gd2.parent, index, r, g, b, a, gd2.X1, gd2.Y1, gd2.Z1, gd2.X2, gd2.Y2, gd2.Z2, gd2.x1, gd2.y1, gd2.z1, gd2.x2, gd2.y2, gd2.z2, linkedDatFile);
                newData.add(newGdata2);
                newGData = newGdata2;
                break;
            case 3:
                GData3 gd3 = (GData3) gData;
                GData3 newGdata3 = new GData3(index, r, g, b, a, gd3.X1, gd3.Y1, gd3.Z1, gd3.X2, gd3.Y2, gd3.Z2, gd3.X3, gd3.Y3, gd3.Z3, gd3.x1, gd3.y1, gd3.z1, gd3.x2, gd3.y2, gd3.z2, gd3.x3,
                        gd3.y3, gd3.z3, gd3.xn, gd3.yn, gd3.zn, gd3.parent, linkedDatFile);
                newData.add(newGdata3);
                newGData = newGdata3;
                break;
            case 4:
                GData4 gd4 = (GData4) gData;
                GData4 newGdata4 = new GData4(index, r, g, b, a, gd4.X1, gd4.Y1, gd4.Z1, gd4.X2, gd4.Y2, gd4.Z2, gd4.X3, gd4.Y3, gd4.Z3, gd4.X4, gd4.Y4, gd4.Z4, gd4.x1, gd4.y1, gd4.z1, gd4.x2,
                        gd4.y2, gd4.z2, gd4.x3, gd4.y3, gd4.z3, gd4.x4, gd4.y4, gd4.z4, gd4.xn, gd4.yn, gd4.zn, gd4.parent, linkedDatFile);
                newData.add(newGdata4);
                newGData = newGdata4;
                break;
            case 5:
                GData5 gd5 = (GData5) gData;
                GData5 newGdata5 = new GData5(true, index, r, g, b, a, gd5.X1, gd5.Y1, gd5.Z1, gd5.X2, gd5.Y2, gd5.Z2, gd5.X3, gd5.Y3, gd5.Z3, gd5.X4, gd5.Y4, gd5.Z4, gd5.x1, gd5.y1, gd5.z1, gd5.x2,
                        gd5.y2, gd5.z2, gd5.x3, gd5.y3, gd5.z3, gd5.x4, gd5.y4, gd5.z4, gd5.parent, linkedDatFile);
                newData.add(newGdata5);
                newGData = newGdata5;
                break;
            }
            linker(gData, newGData);
        }
        dataToModify.clear();
        dataToModify.addAll(newData);
    }

    public synchronized void selectVertices(final Composite3D c3d, boolean addSomething) {
        final boolean noTrans = Editor3DWindow.getWindow().hasNoTransparentSelection();
        if (!c3d.getKeys().isCtrlPressed() && !addSomething || addSomething) {
            selectedVertices.clear();
        }
        final Vector4f selectionStart = new Vector4f(c3d.getSelectionStart());
        final Vector4f selectionWidth = new Vector4f(c3d.getSelectionWidth());
        final Vector4f selectionHeight = new Vector4f(c3d.getSelectionHeight());
        final Vector4f selectionDepth;

        boolean needRayTest = false;
        if (Math.abs(selectionWidth.x) < 0.001f && Math.abs(selectionWidth.y) < 0.001f && Math.abs(selectionWidth.z) < 0.001f)
            needRayTest = true;
        if (Math.abs(selectionHeight.x) < 0.001f && Math.abs(selectionHeight.y) < 0.001f && Math.abs(selectionHeight.z) < 0.001f)
            needRayTest = true;

        if (needRayTest) {

            Vector4f zAxis4f = new Vector4f(0, 0, c3d.hasNegDeterminant() == 1 ? -1f : 1f, 1f);
            Matrix4f ovr_inverse2 = Matrix4f.invert(c3d.getRotation(), null);
            Matrix4f.transform(ovr_inverse2, zAxis4f, zAxis4f);
            selectionDepth = (Vector4f) new Vector4f(zAxis4f.x, zAxis4f.y, zAxis4f.z, 0f).normalise();
            selectionDepth.w = 1f;
            final float discr = 1f / c3d.getZoom();

            final long complexity = c3d.isShowingHiddenVertices() ? vertexLinkedToPositionInFile.size() : vertexLinkedToPositionInFile.size() * ((long) triangles.size() + (long) quads.size());
            if (complexity < View.NUM_CORES * 100L) {
                for (Vertex vertex : vertexLinkedToPositionInFile.keySet()) {
                    if (hiddenVertices.contains(vertex))
                        continue;
                    MathHelper.crossProduct(selectionDepth, Vector4f.sub(vertex.toVector4f(), selectionStart, null), selectionWidth);
                    if (selectionWidth.x * selectionWidth.x + selectionWidth.y * selectionWidth.y + selectionWidth.z * selectionWidth.z < discr) {
                        selectVertices_helper(c3d, vertex, selectionDepth, powerRay, noTrans);
                    }
                }
            } else { // Multithreaded selection for many faces
                backupSelection();
                final int chunks = View.NUM_CORES;
                final Thread[] threads = new Thread[chunks];
                final IProgressMonitor[] monitor = new IProgressMonitor[1];
                final Vertex[] verts = vertexLinkedToPositionInFile.keySet().toArray(new Vertex[0]);
                final int iterations = verts.length;
                int lastend = 0;
                for (int j = 0; j < chunks; ++j) {
                    final int[] i = new int[1];
                    final int[] start = new int[] { lastend };
                    lastend = Math.round(iterations / chunks * (j + 1));
                    final int[] end = new int[] { lastend };
                    if (j == chunks - 1) {
                        end[0] = iterations;
                    }
                    i[0] = j;
                    threads[j] = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final PowerRay powerRay = new PowerRay();
                            int s = start[0];
                            int e = end[0];
                            Vector4f result = new Vector4f();
                            for (int k = s; k < e; k++) {
                                Vertex vertex = verts[k];
                                if (hiddenVertices.contains(vertex))
                                    continue;
                                MathHelper.crossProduct(selectionDepth, Vector4f.sub(vertex.toVector4f(), selectionStart, null), result);
                                if (result.x * result.x + result.y * result.y + result.z * result.z < discr) {
                                    if (monitor[0] != null && monitor[0].isCanceled()) return;
                                    selectVertices_helper(c3d, vertex, selectionDepth, powerRay, noTrans);
                                }
                            }
                        }
                    });
                    threads[j].start();
                }
                boolean isRunning = true;
                int counter = 0;
                while (isRunning) {
                    try {
                        Thread.sleep(100);
                        counter++;
                        if (counter == 50) break;
                    } catch (InterruptedException e) {
                    }
                    isRunning = false;
                    for (Thread thread : threads) {
                        if (thread.isAlive())
                            isRunning = true;
                    }
                }
                if (counter == 50) {
                    linkedDatFile.setDrawSelection(false);
                    try
                    {
                        new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
                        {
                            @Override
                            public void run(final IProgressMonitor m) throws InvocationTargetException, InterruptedException
                            {
                                monitor[0] = m;
                                try
                                {
                                    m.beginTask("Selecting...", IProgressMonitor.UNKNOWN); //$NON-NLS-1$ I18N
                                    boolean isRunning = true;
                                    while (isRunning) {
                                        try {
                                            Thread.sleep(100);
                                        } catch (InterruptedException e) {
                                        }
                                        isRunning = false;
                                        for (Thread thread : threads) {
                                            if (thread.isAlive())
                                                isRunning = true;
                                        }
                                    }
                                }
                                finally
                                {
                                    if (m.isCanceled()) {
                                        restoreSelection();
                                    } else {
                                        backupSelectionClear();
                                    }
                                    m.done();
                                }
                            }
                        });
                    }catch (InvocationTargetException consumed) {
                    } catch (InterruptedException consumed) {
                    }
                    linkedDatFile.setDrawSelection(true);
                }
            }
        } else {
            selectionDepth = new Vector4f();
            MathHelper.crossProduct(selectionHeight, selectionWidth, selectionDepth);
            selectionDepth.w = 0f;
            selectionDepth.normalise();
            switch (c3d.hasNegDeterminant()) {
            case 1:
                selectionDepth.negate();
            default:
            }
            selectionDepth.w = 1f;

            final long complexity = c3d.isShowingHiddenVertices() ? vertexLinkedToPositionInFile.size() : vertexLinkedToPositionInFile.size() * ((long) triangles.size() + (long) quads.size());
            if (complexity < View.NUM_CORES * 100L) {
                float[][] A = new float[3][3];
                float[] b = new float[3];
                for (Vertex vertex : vertexLinkedToPositionInFile.keySet()) {
                    if (hiddenVertices.contains(vertex))
                        continue;
                    A[0][0] = selectionWidth.x;
                    A[1][0] = selectionWidth.y;
                    A[2][0] = selectionWidth.z;

                    A[0][1] = selectionHeight.x;
                    A[1][1] = selectionHeight.y;
                    A[2][1] = selectionHeight.z;

                    A[0][2] = selectionDepth.x;
                    A[1][2] = selectionDepth.y;
                    A[2][2] = selectionDepth.z;

                    b[0] = vertex.x - selectionStart.x;
                    b[1] = vertex.y - selectionStart.y;
                    b[2] = vertex.z - selectionStart.z;
                    b = MathHelper.gaussianElimination(A, b);
                    if (b[0] <= 1f && b[0] >= 0f && b[1] >= 0f && b[1] <= 1f) {
                        selectVertices_helper(c3d, vertex, selectionDepth, powerRay, noTrans);
                    }
                }
            } else {  // Multithreaded selection for many, many faces
                backupSelection();
                final int chunks = View.NUM_CORES;
                final Thread[] threads = new Thread[chunks];
                final IProgressMonitor[] monitor = new IProgressMonitor[1];
                final Vertex[] verts = vertexLinkedToPositionInFile.keySet().toArray(new Vertex[0]);
                final int iterations = verts.length;
                int lastend = 0;
                for (int j = 0; j < chunks; ++j) {
                    final int[] i = new int[1];
                    final int[] start = new int[] { lastend };
                    lastend = Math.round(iterations / chunks * (j + 1));
                    final int[] end = new int[] { lastend };
                    if (j == chunks - 1) {
                        end[0] = iterations;
                    }
                    i[0] = j;
                    threads[j] = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final PowerRay powerRay = new PowerRay();
                            int s = start[0];
                            int e = end[0];
                            float[][] A = new float[3][3];
                            float[] b = new float[3];
                            for (int k = s; k < e; k++) {
                                Vertex vertex = verts[k];
                                if (hiddenVertices.contains(vertex))
                                    continue;
                                A[0][0] = selectionWidth.x;
                                A[1][0] = selectionWidth.y;
                                A[2][0] = selectionWidth.z;

                                A[0][1] = selectionHeight.x;
                                A[1][1] = selectionHeight.y;
                                A[2][1] = selectionHeight.z;

                                A[0][2] = selectionDepth.x;
                                A[1][2] = selectionDepth.y;
                                A[2][2] = selectionDepth.z;

                                b[0] = vertex.x - selectionStart.x;
                                b[1] = vertex.y - selectionStart.y;
                                b[2] = vertex.z - selectionStart.z;
                                b = MathHelper.gaussianElimination(A, b);
                                if (b[0] <= 1f && b[0] >= 0f && b[1] >= 0f && b[1] <= 1f) {
                                    if (monitor[0] != null && monitor[0].isCanceled()) return;
                                    selectVertices_helper(c3d, vertex, selectionDepth, powerRay, noTrans);
                                }
                            }
                        }
                    });
                    threads[j].start();
                }
                boolean isRunning = true;
                int counter = 0;
                while (isRunning) {
                    try {
                        Thread.sleep(100);
                        counter++;
                        if (counter == 50) break;
                    } catch (InterruptedException e) {
                    }
                    isRunning = false;
                    for (Thread thread : threads) {
                        if (thread.isAlive())
                            isRunning = true;
                    }
                }
                if (counter == 50) {
                    linkedDatFile.setDrawSelection(false);
                    try
                    {
                        new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
                        {
                            @Override
                            public void run(final IProgressMonitor m) throws InvocationTargetException, InterruptedException
                            {
                                monitor[0] = m;
                                try
                                {
                                    m.beginTask("Selecting...", IProgressMonitor.UNKNOWN); //$NON-NLS-1$ I18N
                                    boolean isRunning = true;
                                    while (isRunning) {
                                        try {
                                            Thread.sleep(100);
                                        } catch (InterruptedException e) {
                                        }
                                        isRunning = false;
                                        for (Thread thread : threads) {
                                            if (thread.isAlive())
                                                isRunning = true;
                                        }
                                    }
                                }
                                finally
                                {
                                    if (m.isCanceled()) {
                                        restoreSelection();
                                    } else {
                                        backupSelectionClear();
                                    }
                                    m.done();
                                }
                            }
                        });
                    }catch (InvocationTargetException consumed) {
                    } catch (InterruptedException consumed) {
                    }
                    linkedDatFile.setDrawSelection(true);
                }
            }
        }
        if (addSomething) {
            TreeSet<Vertex> nearVertices = new TreeSet<Vertex>();
            TreeSet<Vertex> nearVertices2 = new TreeSet<Vertex>();
            float zoom = c3d.getZoom();
            NLogger.debug(getClass(), zoom);
            BigDecimal EPSILON;
            EPSILON = new BigDecimal(".0005"); //$NON-NLS-1$
            EPSILON = EPSILON.multiply(EPSILON, Threshold.mc).multiply(EPSILON, Threshold.mc).multiply(new BigDecimal(3)).divide(new BigDecimal(zoom), Threshold.mc);
            NLogger.debug(getClass(), EPSILON.toString());
            for (Vertex v : selectedVertices) {
                Vector3d v1 = new Vector3d(v);
                boolean isNear = false;
                for (Vertex key : nearVertices2) {
                    Vector3d v2 = new Vector3d(key);
                    BigDecimal dist = Vector3d.distSquare(v1, v2);
                    if (dist.compareTo(EPSILON) < 0f) {
                        isNear = true;
                        break;
                    }
                }
                nearVertices2.add(v);
                if (!isNear) {
                    nearVertices.add(v);
                }
            }
            selectedVertices.clear();
            selectedVertices.addAll(nearVertices);
        } else if (Editor3DWindow.getWindow().isMovingAdjacentData() && Editor3DWindow.getWindow().getWorkingType() == ObjectMode.VERTICES) {
            {
                HashMap<GData, Integer> occurMap = new HashMap<GData, Integer>();
                for (Vertex vertex : selectedVertices) {
                    Set<VertexManifestation> occurences = vertexLinkedToPositionInFile.get(vertex);
                    if (occurences == null)
                        continue;
                    for (VertexManifestation vm : occurences) {
                        GData g = vm.getGdata();
                        int val = 1;
                        if (occurMap.containsKey(g)) {
                            val = occurMap.get(g);
                            val++;
                        }
                        occurMap.put(g, val);
                        switch (g.type()) {
                        case 2:
                            GData2 line = (GData2) g;
                            if (val == 2) {
                                selectedLines.add(line);
                                selectedData.add(g);
                            }
                            break;
                        case 3:
                            GData3 triangle = (GData3) g;
                            if (val == 3) {
                                selectedTriangles.add(triangle);
                                selectedData.add(g);
                            }
                            break;
                        case 4:
                            GData4 quad = (GData4) g;
                            if (val == 4) {
                                selectedQuads.add(quad);
                                selectedData.add(g);
                            }
                            break;
                        case 5:
                            GData5 condline = (GData5) g;
                            if (val == 4) {
                                selectedCondlines.add(condline);
                                selectedData.add(g);
                            }
                            break;
                        }
                    }
                }
            }
        }
        // MARK For Debug only!
        // if (selectedVertices.size() == 1) {
        //
        // }
    }

    private synchronized void selectVertices2(final Composite3D c3d, boolean addSomething) {
        final boolean noTrans = Editor3DWindow.getWindow().hasNoTransparentSelection();

        final Vector4f selectionStart = new Vector4f(c3d.getSelectionStart());
        final Vector4f selectionWidth = new Vector4f(c3d.getSelectionWidth());
        final Vector4f selectionHeight = new Vector4f(c3d.getSelectionHeight());
        final Vector4f selectionDepth;

        boolean needRayTest = false;
        if (Math.abs(selectionWidth.x) < 0.001f && Math.abs(selectionWidth.y) < 0.001f && Math.abs(selectionWidth.z) < 0.001f)
            needRayTest = true;
        if (Math.abs(selectionHeight.x) < 0.001f && Math.abs(selectionHeight.y) < 0.001f && Math.abs(selectionHeight.z) < 0.001f)
            needRayTest = true;

        if (needRayTest) {

            Vector4f zAxis4f = new Vector4f(0, 0, c3d.hasNegDeterminant() == 1 ? -1f : 1f, 1f);
            Matrix4f ovr_inverse2 = Matrix4f.invert(c3d.getRotation(), null);
            Matrix4f.transform(ovr_inverse2, zAxis4f, zAxis4f);
            selectionDepth = (Vector4f) new Vector4f(zAxis4f.x, zAxis4f.y, zAxis4f.z, 0f).normalise();
            selectionDepth.w = 1f;
            final float discr = 1f / c3d.getZoom();

            final long complexity = c3d.isShowingHiddenVertices() ? vertexLinkedToPositionInFile.size() : vertexLinkedToPositionInFile.size() * ((long) triangles.size() + (long) quads.size());
            if (complexity < View.NUM_CORES * 100L) {
                for (Vertex vertex : vertexLinkedToPositionInFile.keySet()) {
                    if (hiddenVertices.contains(vertex))
                        continue;
                    MathHelper.crossProduct(selectionDepth, Vector4f.sub(vertex.toVector4f(), selectionStart, null), selectionWidth);
                    if (selectionWidth.x * selectionWidth.x + selectionWidth.y * selectionWidth.y + selectionWidth.z * selectionWidth.z < discr) {
                        selectVertices2_helper(c3d, vertex, selectionDepth, powerRay, noTrans);
                    }
                }
            } else { // Multithreaded selection for many faces
                backupSelection();
                final int chunks = View.NUM_CORES;
                final Thread[] threads = new Thread[chunks];
                final IProgressMonitor[] monitor = new IProgressMonitor[1];
                final Vertex[] verts = vertexLinkedToPositionInFile.keySet().toArray(new Vertex[0]);
                final int iterations = verts.length;
                int lastend = 0;
                for (int j = 0; j < chunks; ++j) {
                    final int[] i = new int[1];
                    final int[] start = new int[] { lastend };
                    lastend = Math.round(iterations / chunks * (j + 1));
                    final int[] end = new int[] { lastend };
                    if (j == chunks - 1) {
                        end[0] = iterations;
                    }
                    i[0] = j;
                    threads[j] = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final PowerRay powerRay = new PowerRay();
                            int s = start[0];
                            int e = end[0];
                            Vector4f result = new Vector4f();
                            for (int k = s; k < e; k++) {
                                Vertex vertex = verts[k];
                                if (hiddenVertices.contains(vertex))
                                    continue;
                                MathHelper.crossProduct(selectionDepth, Vector4f.sub(vertex.toVector4f(), selectionStart, null), result);
                                if (result.x * result.x + result.y * result.y + result.z * result.z < discr) {
                                    if (monitor[0] != null && monitor[0].isCanceled()) return;
                                    selectVertices2_helper(c3d, vertex, selectionDepth, powerRay, noTrans);
                                }
                            }
                        }
                    });
                    threads[j].start();
                }
                boolean isRunning = true;
                int counter = 0;
                while (isRunning) {
                    try {
                        Thread.sleep(100);
                        counter++;
                        if (counter == 50) break;
                    } catch (InterruptedException e) {
                    }
                    isRunning = false;
                    for (Thread thread : threads) {
                        if (thread.isAlive())
                            isRunning = true;
                    }
                }
                if (counter == 50) {
                    linkedDatFile.setDrawSelection(false);
                    try
                    {
                        new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
                        {
                            @Override
                            public void run(final IProgressMonitor m) throws InvocationTargetException, InterruptedException
                            {
                                monitor[0] = m;
                                try
                                {
                                    m.beginTask("Selecting...", IProgressMonitor.UNKNOWN); //$NON-NLS-1$ I18N
                                    boolean isRunning = true;
                                    while (isRunning) {
                                        try {
                                            Thread.sleep(100);
                                        } catch (InterruptedException e) {
                                        }
                                        isRunning = false;
                                        for (Thread thread : threads) {
                                            if (thread.isAlive())
                                                isRunning = true;
                                        }
                                    }
                                }
                                finally
                                {
                                    if (m.isCanceled()) {
                                        restoreSelection();
                                    } else {
                                        backupSelectionClear();
                                    }
                                    m.done();
                                }
                            }
                        });
                    }catch (InvocationTargetException consumed) {
                    } catch (InterruptedException consumed) {
                    }
                    linkedDatFile.setDrawSelection(true);
                }
            }
        } else {
            selectionDepth = new Vector4f();
            MathHelper.crossProduct(selectionHeight, selectionWidth, selectionDepth);
            selectionDepth.w = 0f;
            selectionDepth.normalise();
            switch (c3d.hasNegDeterminant()) {
            case 1:
                selectionDepth.negate();
            default:
            }
            selectionDepth.w = 1f;

            final long complexity = c3d.isShowingHiddenVertices() ? vertexLinkedToPositionInFile.size() : vertexLinkedToPositionInFile.size() * ((long) triangles.size() + (long) quads.size());
            if (complexity < View.NUM_CORES * 100L) {
                float[][] A = new float[3][3];
                float[] b = new float[3];
                for (Vertex vertex : vertexLinkedToPositionInFile.keySet()) {
                    if (hiddenVertices.contains(vertex))
                        continue;
                    A[0][0] = selectionWidth.x;
                    A[1][0] = selectionWidth.y;
                    A[2][0] = selectionWidth.z;

                    A[0][1] = selectionHeight.x;
                    A[1][1] = selectionHeight.y;
                    A[2][1] = selectionHeight.z;

                    A[0][2] = selectionDepth.x;
                    A[1][2] = selectionDepth.y;
                    A[2][2] = selectionDepth.z;

                    b[0] = vertex.x - selectionStart.x;
                    b[1] = vertex.y - selectionStart.y;
                    b[2] = vertex.z - selectionStart.z;
                    b = MathHelper.gaussianElimination(A, b);
                    if (b[0] <= 1f && b[0] >= 0f && b[1] >= 0f && b[1] <= 1f) {
                        selectVertices2_helper(c3d, vertex, selectionDepth, powerRay, noTrans);
                    }
                }
            } else { // Multithreaded selection for many faces
                backupSelection();
                final int chunks = View.NUM_CORES;
                final Thread[] threads = new Thread[chunks];
                final IProgressMonitor[] monitor = new IProgressMonitor[1];
                final Vertex[] verts = vertexLinkedToPositionInFile.keySet().toArray(new Vertex[0]);
                final int iterations = verts.length;
                int lastend = 0;
                for (int j = 0; j < chunks; ++j) {
                    final int[] i = new int[1];
                    final int[] start = new int[] { lastend };
                    lastend = Math.round(iterations / chunks * (j + 1));
                    final int[] end = new int[] { lastend };
                    if (j == chunks - 1) {
                        end[0] = iterations;
                    }
                    i[0] = j;
                    threads[j] = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final PowerRay powerRay = new PowerRay();
                            int s = start[0];
                            int e = end[0];
                            float[][] A = new float[3][3];
                            float[] b = new float[3];
                            for (int k = s; k < e; k++) {
                                Vertex vertex = verts[k];
                                if (hiddenVertices.contains(vertex))
                                    continue;
                                A[0][0] = selectionWidth.x;
                                A[1][0] = selectionWidth.y;
                                A[2][0] = selectionWidth.z;

                                A[0][1] = selectionHeight.x;
                                A[1][1] = selectionHeight.y;
                                A[2][1] = selectionHeight.z;

                                A[0][2] = selectionDepth.x;
                                A[1][2] = selectionDepth.y;
                                A[2][2] = selectionDepth.z;

                                b[0] = vertex.x - selectionStart.x;
                                b[1] = vertex.y - selectionStart.y;
                                b[2] = vertex.z - selectionStart.z;
                                b = MathHelper.gaussianElimination(A, b);
                                if (b[0] <= 1f && b[0] >= 0f && b[1] >= 0f && b[1] <= 1f) {
                                    if (monitor[0] != null && monitor[0].isCanceled()) return;
                                    selectVertices2_helper(c3d, vertex, selectionDepth, powerRay, noTrans);
                                }
                            }
                        }
                    });
                    threads[j].start();
                }
                boolean isRunning = true;
                int counter = 0;
                while (isRunning) {
                    try {
                        Thread.sleep(100);
                        counter++;
                        if (counter == 50) break;
                    } catch (InterruptedException e) {
                    }
                    isRunning = false;
                    for (Thread thread : threads) {
                        if (thread.isAlive())
                            isRunning = true;
                    }
                }
                if (counter == 50) {
                    linkedDatFile.setDrawSelection(false);
                    try
                    {
                        new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
                        {
                            @Override
                            public void run(final IProgressMonitor m) throws InvocationTargetException, InterruptedException
                            {
                                monitor[0] = m;
                                try
                                {
                                    m.beginTask("Selecting...", IProgressMonitor.UNKNOWN); //$NON-NLS-1$ I18N
                                    boolean isRunning = true;
                                    while (isRunning) {
                                        try {
                                            Thread.sleep(100);
                                        } catch (InterruptedException e) {
                                        }
                                        isRunning = false;
                                        for (Thread thread : threads) {
                                            if (thread.isAlive())
                                                isRunning = true;
                                        }
                                    }
                                }
                                finally
                                {
                                    if (m.isCanceled()) {
                                        restoreSelection();
                                    } else {
                                        backupSelectionClear();
                                    }
                                    m.done();
                                }
                            }
                        });
                    }catch (InvocationTargetException consumed) {
                    } catch (InterruptedException consumed) {
                    }
                    linkedDatFile.setDrawSelection(true);
                }
            }
        }
        // MARK For Debug only!
        // if (selectedVertices.size() == 1) {
        //
        // }
    }

    public void reSelectSubFiles() {
        // Restore selected subfiles
        for (GData1 subf : selectedSubfiles) {
            Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
            for (VertexInfo vertexInfo : vis) {
                selectedVertices.add(vertexInfo.getVertex());
                GData g = vertexInfo.getLinkedData();
                switch (g.type()) {
                case 2:
                    selectedLines.add((GData2) g);
                    break;
                case 3:
                    selectedTriangles.add((GData3) g);
                    break;
                case 4:
                    selectedQuads.add((GData4) g);
                    break;
                case 5:
                    selectedCondlines.add((GData5) g);
                    break;
                default:
                    break;
                }
            }
        }
    }

    private void selectVertices_helper(final Composite3D c3d, final Vertex vertex, final Vector4f rayDirection, PowerRay powerRay, boolean noTrans) {
        final Set<GData3> tris = triangles.keySet();
        final Set<GData4> qs = quads.keySet();
        if (c3d.isShowingHiddenVertices()) {
            if (selectedVertices.contains(vertex)) {
                selectedVertices.remove(vertex);
            } else {
                selectedVertices.add(vertex);
            }
        } else {
            final Vector4f point = vertex.toVector4f();
            boolean vertexIsShown = true;
            for (GData3 triangle : tris) {
                if (noTrans && triangle.a < 1f || hiddenData.contains(triangle))
                    continue;
                Vertex[] tverts = triangles.get(triangle);
                if (!tverts[0].equals(vertex) && !tverts[1].equals(vertex) && !tverts[2].equals(vertex)) {
                    if (powerRay.TRIANGLE_INTERSECT(point, rayDirection, tverts[0], tverts[1], tverts[2])) {
                        vertexIsShown = false;
                        break;
                    }
                }
            }
            if (vertexIsShown) {
                for (GData4 quad : qs) {
                    if (noTrans && quad.a < 1f || hiddenData.contains(quad))
                        continue;
                    Vertex[] tverts = quads.get(quad);
                    if (!tverts[0].equals(vertex) && !tverts[1].equals(vertex) && !tverts[2].equals(vertex) && !tverts[3].equals(vertex)) {
                        if (powerRay.TRIANGLE_INTERSECT(point, rayDirection, tverts[0], tverts[1], tverts[2])
                                || powerRay.TRIANGLE_INTERSECT(point, rayDirection, tverts[2], tverts[3], tverts[0])) {
                            vertexIsShown = false;
                            break;
                        }
                    }
                }
            }
            if (vertexIsShown) {
                if (selectedVertices.contains(vertex)) {
                    selectedVertices.remove(vertex);
                } else {
                    selectedVertices.add(vertex);
                    if (Editor3DWindow.getWindow().getWorkingType() == ObjectMode.VERTICES) lastSelectedVertex = vertex;
                }
            }
        }
    }

    private void selectVertices2_helper(final Composite3D c3d, final Vertex vertex, final Vector4f rayDirection, PowerRay powerRay, boolean noTrans) {
        final Set<GData3> tris = triangles.keySet();
        final Set<GData4> qs = quads.keySet();
        if (c3d.isShowingHiddenVertices()) {
            selectedVerticesForSubfile.add(vertex);
        } else {
            final Vector4f point = vertex.toVector4f();
            boolean vertexIsShown = true;
            for (GData3 triangle : tris) {
                if (noTrans && triangle.a < 1f || hiddenData.contains(triangle))
                    continue;
                Vertex[] tverts = triangles.get(triangle);
                if (!tverts[0].equals(vertex) && !tverts[1].equals(vertex) && !tverts[2].equals(vertex)) {
                    if (powerRay.TRIANGLE_INTERSECT(point, rayDirection, tverts[0], tverts[1], tverts[2])) {
                        vertexIsShown = false;
                        break;
                    }
                }
            }
            if (vertexIsShown) {
                for (GData4 quad : qs) {
                    if (noTrans && quad.a < 1f || hiddenData.contains(quad))
                        continue;
                    Vertex[] tverts = quads.get(quad);
                    if (!tverts[0].equals(vertex) && !tverts[1].equals(vertex) && !tverts[2].equals(vertex) && !tverts[3].equals(vertex)) {
                        if (powerRay.TRIANGLE_INTERSECT(point, rayDirection, tverts[0], tverts[1], tverts[2])
                                || powerRay.TRIANGLE_INTERSECT(point, rayDirection, tverts[2], tverts[3], tverts[0])) {
                            vertexIsShown = false;
                            break;
                        }
                    }
                }
            }
            if (vertexIsShown) {
                selectedVerticesForSubfile.add(vertex);
            }
        }
    }

    private boolean isVertexVisible(Composite3D c3d, Vertex vertex, Vector4f rayDirection, boolean noTrans) {
        if (!c3d.isShowingHiddenVertices()) {
            final Vector4f point = vertex.toVector4f();
            Vertex[] triQuadVerts = new Vertex[4];
            int i = 0;
            for (GData3 triangle : triangles.keySet()) {
                if (noTrans && triangle.a < 1f || hiddenData.contains(triangle))
                    continue;
                i = 0;
                for (Vertex tvertex : triangles.get(triangle)) {
                    triQuadVerts[i] = tvertex;
                    i++;
                }
                if (!triQuadVerts[0].equals(vertex) && !triQuadVerts[1].equals(vertex) && !triQuadVerts[2].equals(vertex)) {
                    if (powerRay.TRIANGLE_INTERSECT(point, rayDirection, triQuadVerts[0], triQuadVerts[1], triQuadVerts[2])) {
                        return false;
                    }
                }
            }

            for (GData4 quad : quads.keySet()) {
                if (noTrans && quad.a < 1f || hiddenData.contains(quad))
                    continue;
                i = 0;
                for (Vertex tvertex : quads.get(quad)) {
                    triQuadVerts[i] = tvertex;
                    i++;
                }
                if (!triQuadVerts[0].equals(vertex) && !triQuadVerts[1].equals(vertex) && !triQuadVerts[2].equals(vertex) && !triQuadVerts[3].equals(vertex)) {
                    if (powerRay.TRIANGLE_INTERSECT(point, rayDirection, triQuadVerts[0], triQuadVerts[1], triQuadVerts[2])
                            || powerRay.TRIANGLE_INTERSECT(point, rayDirection, triQuadVerts[2], triQuadVerts[3], triQuadVerts[0])) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public synchronized void selectLines(Composite3D c3d) {
        final boolean noTrans = Editor3DWindow.getWindow().hasNoTransparentSelection();
        if (!c3d.getKeys().isCtrlPressed()) {
            selectedData.removeAll(selectedLines);
            selectedData.removeAll(selectedCondlines);
            selectedLines.clear();
            selectedCondlines.clear();
        }
        Set<Vertex> selectedVerticesTemp = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());
        selectedVerticesTemp.addAll(selectedVertices);
        selectedVertices.clear();
        selectVertices(c3d, false);
        boolean allVertsFromLine = false;
        Vector4f selectionWidth = new Vector4f(c3d.getSelectionWidth());
        Vector4f selectionHeight = new Vector4f(c3d.getSelectionHeight());
        boolean needRayTest = false;
        if (Math.abs(selectionWidth.x) < 0.001f && Math.abs(selectionWidth.y) < 0.001f && Math.abs(selectionWidth.z) < 0.001f)
            needRayTest = true;
        if (Math.abs(selectionHeight.x) < 0.001f && Math.abs(selectionHeight.y) < 0.001f && Math.abs(selectionHeight.z) < 0.001f)
            needRayTest = true;

        if (selectedVertices.size() < 2 || needRayTest) {
            if (selectedVertices.size() == 1) {
                Vertex selectedVertex = selectedVertices.iterator().next();
                for (GData2 line : lines.keySet()) {
                    if (hiddenData.contains(line))
                        continue;
                    for (Vertex tvertex : lines.get(line)) {
                        if (selectedVertex.equals(tvertex)) {
                            if (selectedLines.contains(line)) {
                                selectedData.remove(line);
                                selectedLines.remove(line);
                            } else {
                                selectedData.add(line);
                                selectedLines.add(line);
                            }
                        }
                    }
                }
                for (GData5 line : condlines.keySet()) {
                    if (hiddenData.contains(line))
                        continue;
                    for (Vertex tvertex : condlines.get(line)) {
                        if (selectedVertex.equals(tvertex)) {
                            if (selectedCondlines.contains(line)) {
                                selectedData.remove(line);
                                selectedCondlines.remove(line);
                            } else {
                                selectedData.add(line);
                                selectedCondlines.add(line);
                            }
                        }
                    }
                }
            } else {
                Vector4f selectionStart = new Vector4f(c3d.getSelectionStart());
                Vector4f selectionDepth = new Vector4f();

                Vector4f zAxis4f = new Vector4f(0, 0, 1f, 1f);
                Matrix4f ovr_inverse2 = Matrix4f.invert(c3d.getRotation(), null);
                Matrix4f.transform(ovr_inverse2, zAxis4f, zAxis4f);
                selectionDepth = (Vector4f) new Vector4f(zAxis4f.x, zAxis4f.y, zAxis4f.z, 0f).normalise();
                selectionDepth.w = 1f;

                // selectionDepth = ray direction

                // Line from Ray
                // x(t) = s + dt

                float discr = 1f / c3d.getZoom();

                float[] s = new float[3];
                s[0] = selectionStart.x;
                s[1] = selectionStart.y;
                s[2] = selectionStart.z;

                float[] d = new float[3];
                d[0] = selectionDepth.x;
                d[1] = selectionDepth.y;
                d[2] = selectionDepth.z;

                // Segment line
                // x(u) = a + (b - a)u

                // Difference
                // x(t) - x(u) = (s - a) + dt + (a - b)u
                // x(t) - x(u) = e + dt + f u

                float[] a = new float[3];
                float[] e = new float[3];
                float[] f = new float[3];

                float[][] M = new float[2][2];
                float[] b = new float[] { 0f, 0f };
                // NLogger.debug(getClass(), discr);
                for (GData2 line : lines.keySet()) {
                    if (hiddenData.contains(line))
                        continue;
                    allVertsFromLine = false;
                    for (Vertex tvertex : lines.get(line)) {
                        if (allVertsFromLine) { // b
                            f[0] = a[0] - tvertex.x;
                            f[1] = a[1] - tvertex.y;
                            f[2] = a[2] - tvertex.z;
                        } else { // a
                            a[0] = tvertex.x;
                            a[1] = tvertex.y;
                            a[2] = tvertex.z;
                            e[0] = s[0] - a[0];
                            e[1] = s[1] - a[1];
                            e[2] = s[2] - a[2];
                        }
                        allVertsFromLine = true;
                    }
                    M[0][0] = d[0] * d[0] + d[1] * d[1] + d[2] * d[2]; // t
                    M[0][1] = d[0] * f[0] + d[1] * f[1] + d[2] * f[2]; // u

                    M[1][0] = M[0][1]; // t
                    M[1][1] = f[0] * f[0] + f[1] * f[1] + f[2] * f[2]; // u
                    b[0] = -(d[0] * e[0] + d[1] * e[1] + d[2] * e[2]); // constant
                    b[1] = -(e[0] * f[0] + e[1] * f[1] + e[2] * f[2]); // constant
                    try {
                        float[] solution = MathHelper.gaussianElimination(M, b);

                        if (solution[1] >= 0f && solution[1] <= 1f) {
                            float distanceSquared = (float) (Math.pow(e[0] + d[0] * solution[0] + f[0] * solution[1], 2) + Math.pow(e[1] + d[1] * solution[0] + f[1] * solution[1], 2) + Math.pow(
                                    e[2] + d[2] * solution[0] + f[2] * solution[1], 2));
                            if (distanceSquared < discr) {
                                Vertex[] v = lines.get(line);
                                if (!(isVertexVisible(c3d, v[0], selectionDepth, noTrans) && isVertexVisible(c3d, v[1], selectionDepth, noTrans)))
                                    continue;
                                if (selectedLines.contains(line)) {
                                    selectedData.remove(line);
                                    selectedLines.remove(line);
                                } else {
                                    selectedData.add(line);
                                    selectedLines.add(line);
                                }
                            }
                        }
                    } catch (RuntimeException re1) {
                    }
                }
                for (GData5 line : condlines.keySet()) {
                    if (hiddenData.contains(line))
                        continue;
                    allVertsFromLine = false;
                    for (Vertex tvertex : condlines.get(line)) {
                        if (allVertsFromLine) { // b
                            f[0] = a[0] - tvertex.x;
                            f[1] = a[1] - tvertex.y;
                            f[2] = a[2] - tvertex.z;
                            break;
                        } else { // a
                            a[0] = tvertex.x;
                            a[1] = tvertex.y;
                            a[2] = tvertex.z;
                            e[0] = s[0] - a[0];
                            e[1] = s[1] - a[1];
                            e[2] = s[2] - a[2];
                        }
                        allVertsFromLine = true;
                    }
                    M[0][0] = d[0] * d[0] + d[1] * d[1] + d[2] * d[2]; // t
                    M[0][1] = d[0] * f[0] + d[1] * f[1] + d[2] * f[2]; // u

                    M[1][0] = M[0][1]; // t
                    M[1][1] = f[0] * f[0] + f[1] * f[1] + f[2] * f[2]; // u
                    b[0] = -(d[0] * e[0] + d[1] * e[1] + d[2] * e[2]); // constant
                    b[1] = -(e[0] * f[0] + e[1] * f[1] + e[2] * f[2]); // constant
                    try {
                        float[] solution = MathHelper.gaussianElimination(M, b);
                        if (solution[1] >= 0f && solution[1] <= 1f) {
                            float distanceSquared = (float) (Math.pow(e[0] + d[0] * solution[0] + f[0] * solution[1], 2) + Math.pow(e[1] + d[1] * solution[0] + f[1] * solution[1], 2) + Math.pow(
                                    e[2] + d[2] * solution[0] + f[2] * solution[1], 2));
                            if (distanceSquared < discr) {
                                Vertex[] v = condlines.get(line);
                                if (!(isVertexVisible(c3d, v[0], selectionDepth, noTrans) && isVertexVisible(c3d, v[1], selectionDepth, noTrans)))
                                    continue;
                                if (selectedCondlines.contains(line)) {
                                    selectedData.remove(line);
                                    selectedCondlines.remove(line);
                                } else {
                                    selectedData.add(line);
                                    selectedCondlines.add(line);
                                }
                            }
                        }
                    } catch (RuntimeException re2) {
                    }
                }
            }
        } else {
            for (GData2 line : lines.keySet()) {
                if (hiddenData.contains(line))
                    continue;
                allVertsFromLine = false;
                for (Vertex tvertex : lines.get(line)) {
                    if (!selectedVertices.contains(tvertex))
                        break;
                    if (allVertsFromLine) {
                        if (selectedLines.contains(line)) {
                            selectedData.remove(line);
                            selectedLines.remove(line);
                        } else {
                            selectedData.add(line);
                            selectedLines.add(line);
                        }
                    }
                    allVertsFromLine = true;
                }
            }
            for (GData5 line : condlines.keySet()) {
                if (hiddenData.contains(line))
                    continue;
                allVertsFromLine = false;
                for (Vertex tvertex : condlines.get(line)) {
                    if (!selectedVertices.contains(tvertex))
                        break;
                    if (allVertsFromLine) {
                        if (selectedCondlines.contains(line)) {
                            selectedData.remove(line);
                            selectedCondlines.remove(line);
                        } else {
                            selectedData.add(line);
                            selectedCondlines.add(line);
                        }
                    }
                    allVertsFromLine = true;
                }
            }
        }
        selectedVertices.clear();
        selectedVertices.addAll(selectedVerticesTemp);
    }

    private synchronized void selectLines2(Composite3D c3d) {
        final boolean noTrans = Editor3DWindow.getWindow().hasNoTransparentSelection();
        Set<Vertex> tmpVerts = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());
        tmpVerts.addAll(selectedVerticesForSubfile);
        selectedVerticesForSubfile.clear();
        selectVertices2(c3d, false);
        boolean allVertsFromLine = false;
        Vector4f selectionWidth = new Vector4f(c3d.getSelectionWidth());
        Vector4f selectionHeight = new Vector4f(c3d.getSelectionHeight());
        boolean needRayTest = false;
        if (Math.abs(selectionWidth.x) < 0.001f && Math.abs(selectionWidth.y) < 0.001f && Math.abs(selectionWidth.z) < 0.001f)
            needRayTest = true;
        if (Math.abs(selectionHeight.x) < 0.001f && Math.abs(selectionHeight.y) < 0.001f && Math.abs(selectionHeight.z) < 0.001f)
            needRayTest = true;

        if (selectedVerticesForSubfile.size() < 2 || needRayTest) {
            if (selectedVerticesForSubfile.size() == 1) {
                Vertex selectedVertex = selectedVerticesForSubfile.iterator().next();
                for (GData2 line : lines.keySet()) {
                    if (hiddenData.contains(line))
                        continue;
                    for (Vertex tvertex : lines.get(line)) {
                        if (selectedVertex.equals(tvertex)) {
                            selectedLinesForSubfile.add(line);
                        }
                    }
                }
                for (GData5 line : condlines.keySet()) {
                    if (hiddenData.contains(line))
                        continue;
                    for (Vertex tvertex : condlines.get(line)) {
                        if (selectedVertex.equals(tvertex)) {
                            selectedCondlinesForSubfile.add(line);
                        }
                    }
                }
            } else {
                Vector4f selectionStart = new Vector4f(c3d.getSelectionStart());
                Vector4f selectionDepth = new Vector4f();

                Vector4f zAxis4f = new Vector4f(0, 0, 1f, 1f);
                Matrix4f ovr_inverse2 = Matrix4f.invert(c3d.getRotation(), null);
                Matrix4f.transform(ovr_inverse2, zAxis4f, zAxis4f);
                selectionDepth = (Vector4f) new Vector4f(zAxis4f.x, zAxis4f.y, zAxis4f.z, 0f).normalise();
                selectionDepth.w = 1f;

                // selectionDepth = ray direction

                // Line from Ray
                // x(t) = s + dt

                float discr = 1f / c3d.getZoom();

                float[] s = new float[3];
                s[0] = selectionStart.x;
                s[1] = selectionStart.y;
                s[2] = selectionStart.z;

                float[] d = new float[3];
                d[0] = selectionDepth.x;
                d[1] = selectionDepth.y;
                d[2] = selectionDepth.z;

                // Segment line
                // x(u) = a + (b - a)u

                // Difference
                // x(t) - x(u) = (s - a) + dt + (a - b)u
                // x(t) - x(u) = e + dt + f u

                float[] a = new float[3];
                float[] e = new float[3];
                float[] f = new float[3];

                float[][] M = new float[2][2];
                float[] b = new float[] { 0f, 0f };
                // NLogger.debug(getClass(), discr);
                for (GData2 line : lines.keySet()) {
                    if (hiddenData.contains(line))
                        continue;
                    allVertsFromLine = false;
                    for (Vertex tvertex : lines.get(line)) {
                        if (allVertsFromLine) { // b
                            f[0] = a[0] - tvertex.x;
                            f[1] = a[1] - tvertex.y;
                            f[2] = a[2] - tvertex.z;
                        } else { // a
                            a[0] = tvertex.x;
                            a[1] = tvertex.y;
                            a[2] = tvertex.z;
                            e[0] = s[0] - a[0];
                            e[1] = s[1] - a[1];
                            e[2] = s[2] - a[2];
                        }
                        allVertsFromLine = true;
                    }
                    M[0][0] = d[0] * d[0] + d[1] * d[1] + d[2] * d[2]; // t
                    M[0][1] = d[0] * f[0] + d[1] * f[1] + d[2] * f[2]; // u

                    M[1][0] = M[0][1]; // t
                    M[1][1] = f[0] * f[0] + f[1] * f[1] + f[2] * f[2]; // u
                    b[0] = -(d[0] * e[0] + d[1] * e[1] + d[2] * e[2]); // constant
                    b[1] = -(e[0] * f[0] + e[1] * f[1] + e[2] * f[2]); // constant
                    try {
                        float[] solution = MathHelper.gaussianElimination(M, b);

                        if (solution[1] >= 0f && solution[1] <= 1f) {
                            float distanceSquared = (float) (Math.pow(e[0] + d[0] * solution[0] + f[0] * solution[1], 2) + Math.pow(e[1] + d[1] * solution[0] + f[1] * solution[1], 2) + Math.pow(
                                    e[2] + d[2] * solution[0] + f[2] * solution[1], 2));
                            if (distanceSquared < discr) {
                                Vertex[] v = lines.get(line);
                                if (!(isVertexVisible(c3d, v[0], selectionDepth, noTrans) && isVertexVisible(c3d, v[1], selectionDepth, noTrans)))
                                    continue;
                                selectedLinesForSubfile.add(line);
                            }
                        }
                    } catch (RuntimeException re1) {
                    }
                }
                for (GData5 line : condlines.keySet()) {
                    if (hiddenData.contains(line))
                        continue;
                    allVertsFromLine = false;
                    for (Vertex tvertex : condlines.get(line)) {
                        if (allVertsFromLine) { // b
                            f[0] = a[0] - tvertex.x;
                            f[1] = a[1] - tvertex.y;
                            f[2] = a[2] - tvertex.z;
                            break;
                        } else { // a
                            a[0] = tvertex.x;
                            a[1] = tvertex.y;
                            a[2] = tvertex.z;
                            e[0] = s[0] - a[0];
                            e[1] = s[1] - a[1];
                            e[2] = s[2] - a[2];
                        }
                        allVertsFromLine = true;
                    }
                    M[0][0] = d[0] * d[0] + d[1] * d[1] + d[2] * d[2]; // t
                    M[0][1] = d[0] * f[0] + d[1] * f[1] + d[2] * f[2]; // u

                    M[1][0] = M[0][1]; // t
                    M[1][1] = f[0] * f[0] + f[1] * f[1] + f[2] * f[2]; // u
                    b[0] = -(d[0] * e[0] + d[1] * e[1] + d[2] * e[2]); // constant
                    b[1] = -(e[0] * f[0] + e[1] * f[1] + e[2] * f[2]); // constant
                    try {
                        float[] solution = MathHelper.gaussianElimination(M, b);
                        if (solution[1] >= 0f && solution[1] <= 1f) {
                            float distanceSquared = (float) (Math.pow(e[0] + d[0] * solution[0] + f[0] * solution[1], 2) + Math.pow(e[1] + d[1] * solution[0] + f[1] * solution[1], 2) + Math.pow(
                                    e[2] + d[2] * solution[0] + f[2] * solution[1], 2));
                            if (distanceSquared < discr) {
                                Vertex[] v = condlines.get(line);
                                if (!(isVertexVisible(c3d, v[0], selectionDepth, noTrans) && isVertexVisible(c3d, v[1], selectionDepth, noTrans)))
                                    continue;
                                selectedCondlinesForSubfile.add(line);
                            }
                        }
                    } catch (RuntimeException re2) {
                    }
                }
            }
        } else {
            for (GData2 line : lines.keySet()) {
                if (hiddenData.contains(line))
                    continue;
                allVertsFromLine = false;
                for (Vertex tvertex : lines.get(line)) {
                    if (!selectedVerticesForSubfile.contains(tvertex))
                        break;
                    if (allVertsFromLine) {
                        selectedLinesForSubfile.add(line);
                    }
                    allVertsFromLine = true;
                }
            }
            for (GData5 line : condlines.keySet()) {
                if (hiddenData.contains(line))
                    continue;
                allVertsFromLine = false;
                for (Vertex tvertex : condlines.get(line)) {
                    if (!selectedVerticesForSubfile.contains(tvertex))
                        break;
                    if (allVertsFromLine) {
                        selectedCondlinesForSubfile.add(line);
                    }
                    allVertsFromLine = true;
                }
            }
        }
        selectedVerticesForSubfile.clear();
        selectedVerticesForSubfile.addAll(tmpVerts);
    }

    public synchronized void selectFaces(Composite3D c3d, Event event) {
        if (!c3d.getKeys().isCtrlPressed()) {
            selectedData.removeAll(selectedTriangles);
            selectedData.removeAll(selectedQuads);
            selectedTriangles.clear();
            selectedQuads.clear();
        }
        Set<Vertex> selectedVerticesTemp = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());
        selectedVerticesTemp.addAll(selectedVertices);
        selectedVertices.clear();
        selectVertices(c3d, false);
        boolean allVertsFromLine = false;
        Vector4f selectionWidth = new Vector4f(c3d.getSelectionWidth());
        Vector4f selectionHeight = new Vector4f(c3d.getSelectionHeight());
        boolean needRayTest = false;
        if (Math.abs(selectionWidth.x) < 0.001f && Math.abs(selectionWidth.y) < 0.001f && Math.abs(selectionWidth.z) < 0.001f)
            needRayTest = true;
        if (Math.abs(selectionHeight.x) < 0.001f && Math.abs(selectionHeight.y) < 0.001f && Math.abs(selectionHeight.z) < 0.001f)
            needRayTest = true;
        if (selectedVertices.size() < 2 || needRayTest) {
            if (selectedVertices.size() == 1) {
                Vertex selectedVertex = selectedVertices.iterator().next();
                for (GData3 line : triangles.keySet()) {
                    if (hiddenData.contains(line))
                        continue;
                    for (Vertex tvertex : triangles.get(line)) {
                        if (selectedVertex.equals(tvertex)) {
                            if (selectedTriangles.contains(line)) {
                                selectedData.remove(line);
                                selectedTriangles.remove(line);
                            } else {
                                selectedData.add(line);
                                selectedTriangles.add(line);
                            }
                        }
                    }
                }
                for (GData4 line : quads.keySet()) {
                    if (hiddenData.contains(line))
                        continue;
                    for (Vertex tvertex : quads.get(line)) {
                        if (selectedVertex.equals(tvertex)) {
                            if (selectedQuads.contains(line)) {
                                selectedData.remove(line);
                                selectedQuads.remove(line);
                            } else {
                                selectedData.add(line);
                                selectedQuads.add(line);
                            }
                        }
                    }
                }
            } else {

                GData selection = selectFaces_helper(c3d, event);
                if (selection != null) {
                    if (selection.type() == 4) {
                        GData4 gd4 = (GData4) selection;
                        if (selectedQuads.contains(gd4)) {
                            selectedData.remove(gd4);
                            selectedQuads.remove(gd4);
                        } else {
                            selectedData.add(gd4);
                            selectedQuads.add(gd4);
                        }
                    } else {
                        GData3 gd3 = (GData3) selection;
                        if (selectedTriangles.contains(gd3)) {
                            selectedData.remove(gd3);
                            selectedTriangles.remove(gd3);
                        } else {
                            selectedData.add(gd3);
                            selectedTriangles.add(gd3);
                        }
                    }
                }
            }
        } else {
            for (GData3 line : triangles.keySet()) {
                if (hiddenData.contains(line))
                    continue;
                allVertsFromLine = true;
                for (Vertex tvertex : triangles.get(line)) {
                    if (!selectedVertices.contains(tvertex)) {
                        allVertsFromLine = false;
                        break;
                    }
                }
                if (allVertsFromLine) {
                    if (selectedTriangles.contains(line)) {
                        selectedData.remove(line);
                        selectedTriangles.remove(line);
                    } else {
                        selectedData.add(line);
                        selectedTriangles.add(line);
                    }
                }
            }
            for (GData4 line : quads.keySet()) {
                if (hiddenData.contains(line))
                    continue;
                allVertsFromLine = true;
                for (Vertex tvertex : quads.get(line)) {
                    if (!selectedVertices.contains(tvertex)) {
                        allVertsFromLine = false;
                        break;
                    }
                }
                if (allVertsFromLine) {
                    if (selectedQuads.contains(line)) {
                        selectedData.remove(line);
                        selectedQuads.remove(line);
                    } else {
                        selectedData.add(line);
                        selectedQuads.add(line);
                    }
                }
            }
        }
        selectedVertices.clear();
        selectedVertices.addAll(selectedVerticesTemp);
    }

    private synchronized void selectFaces2(Composite3D c3d, Event event) {
        Set<Vertex> selVert4sTemp = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());
        selVert4sTemp.addAll(selectedVerticesForSubfile);
        selectedVerticesForSubfile.clear();
        selectVertices2(c3d, false);
        boolean allVertsFromLine = false;
        Vector4f selectionWidth = new Vector4f(c3d.getSelectionWidth());
        Vector4f selectionHeight = new Vector4f(c3d.getSelectionHeight());
        boolean needRayTest = false;
        if (Math.abs(selectionWidth.x) < 0.001f && Math.abs(selectionWidth.y) < 0.001f && Math.abs(selectionWidth.z) < 0.001f)
            needRayTest = true;
        if (Math.abs(selectionHeight.x) < 0.001f && Math.abs(selectionHeight.y) < 0.001f && Math.abs(selectionHeight.z) < 0.001f)
            needRayTest = true;
        if (selectedVerticesForSubfile.size() < 2 || needRayTest) {
            if (selectedVerticesForSubfile.size() == 1) {
                Vertex selectedVertex = selectedVerticesForSubfile.iterator().next();
                for (GData3 line : triangles.keySet()) {
                    if (hiddenData.contains(line))
                        continue;
                    for (Vertex tvertex : triangles.get(line)) {
                        if (selectedVertex.equals(tvertex)) {
                            selectedTrianglesForSubfile.add(line);
                        }
                    }
                }
                for (GData4 line : quads.keySet()) {
                    if (hiddenData.contains(line))
                        continue;
                    for (Vertex tvertex : quads.get(line)) {
                        if (selectedVertex.equals(tvertex)) {
                            selectedQuadsForSubfile.add(line);
                        }
                    }
                }
            } else {

                GData selection = selectFaces_helper(c3d, event);
                if (selection != null) {
                    if (selection.type() == 4) {
                        GData4 gd4 = (GData4) selection;
                        selectedQuadsForSubfile.add(gd4);
                    } else {
                        GData3 gd3 = (GData3) selection;
                        selectedTrianglesForSubfile.add(gd3);
                    }
                }
            }
        } else {
            for (GData3 line : triangles.keySet()) {
                if (hiddenData.contains(line))
                    continue;
                allVertsFromLine = true;
                for (Vertex tvertex : triangles.get(line)) {
                    if (!selectedVerticesForSubfile.contains(tvertex)) {
                        allVertsFromLine = false;
                        break;
                    }
                }
                if (allVertsFromLine) {
                    selectedTrianglesForSubfile.add(line);
                }
            }
            for (GData4 line : quads.keySet()) {
                if (hiddenData.contains(line))
                    continue;
                allVertsFromLine = true;
                for (Vertex tvertex : quads.get(line)) {
                    if (!selectedVerticesForSubfile.contains(tvertex)) {
                        allVertsFromLine = false;
                        break;
                    }
                }
                if (allVertsFromLine) {
                    selectedQuadsForSubfile.add(line);
                }
            }
        }
        selectedVerticesForSubfile.clear();
        selectedVerticesForSubfile.addAll(selVert4sTemp);
    }

    private synchronized GData selectFaces_helper(Composite3D c3d, Event event) {
        final boolean noTrans = Editor3DWindow.getWindow().hasNoTransparentSelection();
        PerspectiveCalculator perspective = c3d.getPerspectiveCalculator();
        Matrix4f viewport_rotation = c3d.getRotation();
        Vector4f zAxis4f = new Vector4f(0, 0, -1f, 1f);
        Matrix4f ovr_inverse2 = Matrix4f.invert(viewport_rotation, null);
        Matrix4f.transform(ovr_inverse2, zAxis4f, zAxis4f);
        Vector4f rayDirection = (Vector4f) new Vector4f(zAxis4f.x, zAxis4f.y, zAxis4f.z, 0f).normalise();
        rayDirection.w = 1f;

        Vertex[] triQuadVerts = new Vertex[4];
        int i = 0;

        Vector4f orig = perspective.get3DCoordinatesFromScreen(event.x, event.y);
        Vector4f point = new Vector4f(orig);

        double minDist = Double.MAX_VALUE;
        final double[] dist = new double[1];
        GData result = null;
        for (GData3 triangle : triangles.keySet()) {
            if (hiddenData.contains(triangle))
                continue;
            if (noTrans && triangle.a < 1f && !c3d.isShowingHiddenVertices())
                continue;
            i = 0;
            for (Vertex tvertex : triangles.get(triangle)) {
                triQuadVerts[i] = tvertex;
                i++;
            }

            if (powerRay.TRIANGLE_INTERSECT(orig, rayDirection, triQuadVerts[0], triQuadVerts[1], triQuadVerts[2], point, dist)) {
                if (dist[0] < minDist) {
                    minDist = dist[0];
                    result = triangle;
                }
            }
        }
        for (GData4 quad : quads.keySet()) {
            if (hiddenData.contains(quad))
                continue;
            if (noTrans && quad.a < 1f && !c3d.isShowingHiddenVertices())
                continue;
            i = 0;
            for (Vertex tvertex : quads.get(quad)) {
                triQuadVerts[i] = tvertex;
                i++;
            }
            if (powerRay.TRIANGLE_INTERSECT(orig, rayDirection, triQuadVerts[0], triQuadVerts[1], triQuadVerts[2], point, dist)
                    || powerRay.TRIANGLE_INTERSECT(orig, rayDirection, triQuadVerts[2], triQuadVerts[3], triQuadVerts[0], point, dist)) {
                if (dist[0] < minDist) {
                    minDist = dist[0];
                    result = quad;
                }
            }
        }
        return result;
    }

    public synchronized void selectSubfiles(Composite3D c3d, Event event, boolean preSelection) {

        selectedVerticesForSubfile.clear();
        selectedLinesForSubfile.clear();
        selectedTrianglesForSubfile.clear();
        selectedQuadsForSubfile.clear();
        selectedCondlinesForSubfile.clear();

        if (preSelection) {
            selectVertices2(c3d, false);
            selectFaces2(c3d, event);
            selectLines2(c3d);
        }

        if (preSelection && !c3d.getKeys().isCtrlPressed()) {
            for (GData1 subf : selectedSubfiles) {
                Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
                for (VertexInfo vertexInfo : vis) {
                    selectedVertices.remove(vertexInfo.getVertex());
                    GData g = vertexInfo.getLinkedData();
                    selectedData.remove(g);
                    switch (g.type()) {
                    case 2:
                        selectedLines.remove(g);
                        break;
                    case 3:
                        selectedTriangles.remove(g);
                        break;
                    case 4:
                        selectedQuads.remove(g);
                        break;
                    case 5:
                        selectedCondlines.remove(g);
                        break;
                    default:
                        break;
                    }
                }
            }
            selectedData.removeAll(selectedSubfiles);
            selectedSubfiles.clear();
        }

        TreeSet<Vertex> verticesToRemove = new TreeSet<Vertex>();
        HashSet<GData1> subfilesToLink = new HashSet<GData1>();
        HashSet<GData2> linesToRemove = new HashSet<GData2>();
        HashSet<GData3> trianglesToRemove = new HashSet<GData3>();
        HashSet<GData4> quadsToRemove = new HashSet<GData4>();
        HashSet<GData5> condlinesToRemove = new HashSet<GData5>();

        for (Vertex v : selectedVerticesForSubfile) {
            Set<GData1> subfiles = vertexLinkedToSubfile.get(v);
            if (subfiles == null) {
                verticesToRemove.add(v);
            } else {
                subfilesToLink.addAll(subfiles);
            }
        }

        for (GData2 l : selectedLinesForSubfile) {
            Vertex[] verts = lines.get(l);
            if (verts != null) {
                Set<GData1> subfiles1 = vertexLinkedToSubfile.get(verts[0]);
                Set<GData1> subfiles2 = vertexLinkedToSubfile.get(verts[1]);
                if (subfiles1 == null || subfiles2 == null) {
                    linesToRemove.add(l);
                } else {
                    subfiles1 = new HashSet<GData1>(subfiles1);
                    subfiles1.retainAll(subfiles2);
                    if (!subfiles1.isEmpty()) {
                        subfilesToLink.addAll(subfiles1);
                    } else {
                        linesToRemove.add(l);
                    }
                }
            }
        }

        for (GData3 t : selectedTrianglesForSubfile) {
            Vertex[] verts = triangles.get(t);
            if (verts != null) {
                Set<GData1> subfiles1 = vertexLinkedToSubfile.get(verts[0]);
                Set<GData1> subfiles2 = vertexLinkedToSubfile.get(verts[1]);
                Set<GData1> subfiles3 = vertexLinkedToSubfile.get(verts[2]);
                if (subfiles1 == null || subfiles2 == null || subfiles3 == null) {
                    trianglesToRemove.add(t);
                } else {
                    subfiles1 = new HashSet<GData1>(subfiles1);
                    subfiles3 = new HashSet<GData1>(subfiles3);
                    subfiles3.retainAll(subfiles2);
                    subfiles1.retainAll(subfiles3);
                    if (!subfiles1.isEmpty()) {
                        subfilesToLink.addAll(subfiles1);
                    } else {
                        trianglesToRemove.add(t);
                    }
                }
            }
        }

        for (GData4 q : selectedQuadsForSubfile) {
            Vertex[] verts = quads.get(q);
            if (verts != null) {
                Set<GData1> subfiles1 = vertexLinkedToSubfile.get(verts[0]);
                Set<GData1> subfiles2 = vertexLinkedToSubfile.get(verts[1]);
                Set<GData1> subfiles3 = vertexLinkedToSubfile.get(verts[2]);
                Set<GData1> subfiles4 = vertexLinkedToSubfile.get(verts[3]);
                if (subfiles1 == null || subfiles2 == null || subfiles3 == null || subfiles4 == null) {
                    quadsToRemove.add(q);
                } else {
                    subfiles1 = new HashSet<GData1>(subfiles1);
                    subfiles3 = new HashSet<GData1>(subfiles3);
                    subfiles4 = new HashSet<GData1>(subfiles4);
                    subfiles3.retainAll(subfiles2);
                    subfiles4.retainAll(subfiles3);
                    subfiles1.retainAll(subfiles4);
                    if (!subfiles1.isEmpty()) {
                        subfilesToLink.addAll(subfiles1);
                    } else {
                        quadsToRemove.add(q);
                    }
                }
            }
        }

        for (GData5 c : selectedCondlinesForSubfile) {
            Vertex[] verts = condlines.get(c);
            if (verts != null) {
                Set<GData1> subfiles1 = vertexLinkedToSubfile.get(verts[0]);
                Set<GData1> subfiles2 = vertexLinkedToSubfile.get(verts[1]);
                Set<GData1> subfiles3 = vertexLinkedToSubfile.get(verts[2]);
                Set<GData1> subfiles4 = vertexLinkedToSubfile.get(verts[3]);
                if (subfiles1 == null || subfiles2 == null || subfiles3 == null || subfiles4 == null) {
                    condlinesToRemove.add(c);
                } else {
                    subfiles1 = new HashSet<GData1>(subfiles1);
                    subfiles3 = new HashSet<GData1>(subfiles3);
                    subfiles4 = new HashSet<GData1>(subfiles4);
                    subfiles3.retainAll(subfiles2);
                    subfiles4.retainAll(subfiles3);
                    subfiles1.retainAll(subfiles4);
                    if (!subfiles1.isEmpty()) {
                        subfilesToLink.addAll(subfiles1);
                    } else {
                        condlinesToRemove.add(c);
                    }
                }
            }
        }

        subfilesToLink.addAll(selectedSubfiles);
        selectedVerticesForSubfile.clear();
        selectedLinesForSubfile.clear();
        selectedTrianglesForSubfile.clear();
        selectedQuadsForSubfile.clear();
        selectedCondlinesForSubfile.clear();

        for (GData1 subf : subfilesToLink) {
            if (hiddenData.contains(subf))
                continue;
            selectedSubfiles.add(subf);
            Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
            for (VertexInfo vertexInfo : vis) {
                selectedVertices.add(vertexInfo.getVertex());
                GData g = vertexInfo.getLinkedData();
                switch (g.type()) {
                case 2:
                    selectedLines.add((GData2) g);
                    break;
                case 3:
                    selectedTriangles.add((GData3) g);
                    break;
                case 4:
                    selectedQuads.add((GData4) g);
                    break;
                case 5:
                    selectedCondlines.add((GData5) g);
                    break;
                default:
                    break;
                }
            }
        }

        selectedData.addAll(selectedLines);
        selectedData.addAll(selectedTriangles);
        selectedData.addAll(selectedQuads);
        selectedData.addAll(selectedCondlines);
        selectedData.addAll(selectedSubfiles);

    }

    public synchronized void clear() {
        final Editor3DWindow win = Editor3DWindow.getWindow();
        vertexCountInSubfile.clear();
        vertexLinkedToPositionInFile.clear();
        vertexLinkedToSubfile.clear();
        lineLinkedToVertices.clear();
        declaredVertices.clear();
        lines.clear();
        triangles.clear();
        quads.clear();
        condlines.clear();
        selectedItemIndex = -1;
        win.disableSelectionTab();
        selectedData.clear();
        selectedVertices.clear();
        selectedSubfiles.clear();
        selectedLines.clear();
        selectedTriangles.clear();
        selectedQuads.clear();
        selectedCondlines.clear();
        lastSelectedVertex = null;
    }

    public synchronized void clearSelection() {
        final Editor3DWindow win = Editor3DWindow.getWindow();
        selectedItemIndex = -1;
        win.disableSelectionTab();
        selectedData.clear();
        selectedVertices.clear();
        selectedSubfiles.clear();
        selectedLines.clear();
        selectedTriangles.clear();
        selectedQuads.clear();
        selectedCondlines.clear();
        lastSelectedVertex = null;
    }

    public synchronized void clearSelection2() {
        selectedItemIndex = -1;
        selectedData.clear();
        selectedVertices.clear();
        selectedSubfiles.clear();
        selectedLines.clear();
        selectedTriangles.clear();
        selectedQuads.clear();
        selectedCondlines.clear();
        lastSelectedVertex = null;
    }

    private void clearSelection3() {
        selectedItemIndex = -1;
        selectedData.clear();
        selectedVertices.clear();
        selectedSubfiles.clear();
        selectedLines.clear();
        selectedTriangles.clear();
        selectedQuads.clear();
        selectedCondlines.clear();
        lastSelectedVertex = null;
    }

    public synchronized void adjustRotationCenter(Composite3D c3d, Event event) {
        Point cSize = c3d.getSize();
        PerspectiveCalculator perspective = c3d.getPerspectiveCalculator();
        Matrix4f viewport_translation = c3d.getTranslation();
        Matrix4f viewport_rotation = c3d.getRotation();
        float viewport_pixel_per_ldu = c3d.getViewportPixelPerLDU();

        float dx = 0;
        float dy = 0;

        Vector4f zAxis4f = new Vector4f(0, 0, -1f, 1f);
        Matrix4f ovr_inverse2 = Matrix4f.invert(viewport_rotation, null);
        Matrix4f.transform(ovr_inverse2, zAxis4f, zAxis4f);
        Vector4f rayDirection = (Vector4f) new Vector4f(zAxis4f.x, zAxis4f.y, zAxis4f.z, 0f).normalise();
        rayDirection.w = 1f;

        Vertex[] triQuadVerts = new Vertex[4];
        int i = 0;
        boolean objectSelected = false;
        GData selectedObject = null;
        Vector4f orig;
        if (event == null) {
            orig = c3d.getManipulator().getPosition();
        } else {
            orig = perspective.get3DCoordinatesFromScreen(event.x, event.y);
        }
        Vector4f point = new Vector4f(orig);

        Vector4f minPoint = new Vector4f();
        double minDist = Double.MAX_VALUE;
        final double[] dist = new double[1];
        for (GData3 triangle : triangles.keySet()) {
            if (hiddenData.contains(triangle))
                continue;
            i = 0;
            for (Vertex tvertex : triangles.get(triangle)) {
                triQuadVerts[i] = tvertex;
                i++;
            }
            if (powerRay.TRIANGLE_INTERSECT(orig, rayDirection, triQuadVerts[0], triQuadVerts[1], triQuadVerts[2], point, dist)) {
                if (dist[0] < minDist) {
                    minDist = dist[0];
                    minPoint.set(point);
                    objectSelected = true;
                    selectedObject = triangle;
                }
            }
        }
        for (GData4 quad : quads.keySet()) {
            if (hiddenData.contains(quad))
                continue;
            i = 0;
            for (Vertex tvertex : quads.get(quad)) {
                triQuadVerts[i] = tvertex;
                i++;
            }
            if (powerRay.TRIANGLE_INTERSECT(orig, rayDirection, triQuadVerts[0], triQuadVerts[1], triQuadVerts[2], point, dist)
                    || powerRay.TRIANGLE_INTERSECT(orig, rayDirection, triQuadVerts[2], triQuadVerts[3], triQuadVerts[0], point, dist)) {
                if (dist[0] < minDist) {
                    minDist = dist[0];
                    minPoint.set(point);
                    objectSelected = true;
                    selectedObject = quad;
                }
            }
        }

        if (objectSelected) {
            if (Editor3DWindow.getWindow().getWorkingAction() != WorkingMode.SELECT) {
                c3d.getManipulator().getPosition().set(minPoint.x, minPoint.y, minPoint.z, 1f);
                if (selectedObject.type() == 3) {
                    GData3 tri = (GData3) selectedObject;
                    Vertex[] verts = triangles.get(tri);
                    float xn = (verts[2].y - verts[0].y) * (verts[1].z - verts[0].z) - (verts[2].z - verts[0].z) * (verts[1].y - verts[0].y);
                    float yn = (verts[2].z - verts[0].z) * (verts[1].x - verts[0].x) - (verts[2].x - verts[0].x) * (verts[1].z - verts[0].z);
                    float zn = (verts[2].x - verts[0].x) * (verts[1].y - verts[0].y) - (verts[2].y - verts[0].y) * (verts[1].x - verts[0].x);
                    Vector3f n = new Vector3f(-xn, -yn, -zn);
                    n.normalise();
                    c3d.getManipulator().getZaxis().set(n.x, n.y, n.z, 1f);

                    Vector4f v0 = verts[0].toVector4fm();
                    Vector4f v1 = verts[1].toVector4fm();
                    Vector4f v2 = verts[2].toVector4fm();

                    Vector4f d1 = Vector4f.sub(v0, v1, null);
                    Vector4f d2 = Vector4f.sub(v0, v2, null);
                    Vector4f d3 = Vector4f.sub(v1, v2, null);

                    float dist1 = d1.lengthSquared();
                    float dist2 = d2.lengthSquared();
                    float dist3 = d3.lengthSquared();

                    float minD = Math.min(dist1, Math.min(dist2, dist3));
                    if (minD == dist1) {
                        d1.normalise();
                        c3d.getManipulator().getXaxis().set(d1.x, d1.y, d1.z, 1f);
                    } else if (minD == dist2) {
                        d2.normalise();
                        c3d.getManipulator().getXaxis().set(d2.x, d2.y, d2.z, 1f);
                    } else {
                        d3.normalise();
                        c3d.getManipulator().getXaxis().set(d3.x, d3.y, d3.z, 1f);
                    }
                    Vector4f zaxis = c3d.getManipulator().getZaxis();
                    Vector4f xaxis = c3d.getManipulator().getXaxis();
                    Vector3f cross = Vector3f.cross(new Vector3f(xaxis.x, xaxis.y, xaxis.z), new Vector3f(zaxis.x, zaxis.y, zaxis.z), null);
                    c3d.getManipulator().getYaxis().set(cross.x, cross.y, cross.z, 1f);

                } else if (selectedObject.type() == 4) {
                    GData4 quad = (GData4) selectedObject;

                    Vertex[] verts = quads.get(quad);
                    Vector4f v0 = verts[0].toVector4fm();
                    Vector4f v1 = verts[1].toVector4fm();
                    Vector4f v2 = verts[2].toVector4fm();
                    Vector4f v3 = verts[3].toVector4fm();

                    Vector3f[] normals = new Vector3f[4];
                    Vector4f[] lineVectors = new Vector4f[4];
                    lineVectors[0] = Vector4f.sub(v1, v0, null);
                    lineVectors[1] = Vector4f.sub(v2, v1, null);
                    lineVectors[2] = Vector4f.sub(v3, v2, null);
                    lineVectors[3] = Vector4f.sub(v0, v3, null);
                    normals[0] = Vector3f.cross(new Vector3f(lineVectors[0].x, lineVectors[0].y, lineVectors[0].z), new Vector3f(lineVectors[1].x, lineVectors[1].y, lineVectors[1].z), null);
                    normals[1] = Vector3f.cross(new Vector3f(lineVectors[1].x, lineVectors[1].y, lineVectors[1].z), new Vector3f(lineVectors[2].x, lineVectors[2].y, lineVectors[2].z), null);
                    normals[2] = Vector3f.cross(new Vector3f(lineVectors[2].x, lineVectors[2].y, lineVectors[2].z), new Vector3f(lineVectors[3].x, lineVectors[3].y, lineVectors[3].z), null);
                    normals[3] = Vector3f.cross(new Vector3f(lineVectors[3].x, lineVectors[3].y, lineVectors[3].z), new Vector3f(lineVectors[0].x, lineVectors[0].y, lineVectors[0].z), null);

                    Vector3f n = new Vector3f();
                    for (int k = 0; k < 4; k++) {
                        n = Vector3f.add(normals[k], n, null);
                    }
                    n.normalise();

                    c3d.getManipulator().getZaxis().set(n.x, n.y, n.z, 1f);

                    Vector4f d1 = Vector4f.sub(v0, v1, null);
                    Vector4f d2 = Vector4f.sub(v0, v2, null);
                    Vector4f d3 = Vector4f.sub(v1, v2, null);

                    float dist1 = d1.lengthSquared();
                    float dist2 = d2.lengthSquared();
                    float dist3 = d3.lengthSquared();

                    float minD = Math.min(dist1, Math.min(dist2, dist3));
                    if (minD == dist1) {
                        d1.normalise();
                        c3d.getManipulator().getXaxis().set(d1.x, d1.y, d1.z, 1f);
                    } else if (minD == dist2) {
                        d2.normalise();
                        c3d.getManipulator().getXaxis().set(d2.x, d2.y, d2.z, 1f);
                    } else {
                        d3.normalise();
                        c3d.getManipulator().getXaxis().set(d3.x, d3.y, d3.z, 1f);
                    }
                    Vector4f zaxis = c3d.getManipulator().getZaxis();
                    Vector4f xaxis = c3d.getManipulator().getXaxis();
                    Vector3f cross = Vector3f.cross(new Vector3f(xaxis.x, xaxis.y, xaxis.z), new Vector3f(zaxis.x, zaxis.y, zaxis.z), null);
                    c3d.getManipulator().getYaxis().set(cross.x, cross.y, cross.z, 1f);
                }
            }
            Vector4f zAxis4f_translation = new Vector4f(-minPoint.x, -minPoint.y, -minPoint.z, 1.0f);
            Vector3f zAxis3 = new Vector3f(zAxis4f_translation.x, zAxis4f_translation.y, zAxis4f_translation.z);
            viewport_translation.load(Matrix4f.translate(zAxis3, View.ID, null));
        } else {
            if (event == null)
                return;
            dx = (event.x - cSize.x / 2) / viewport_pixel_per_ldu;
            dy = (cSize.y / 2 - event.y) / viewport_pixel_per_ldu;

            Vector4f xAxis4f_translation = new Vector4f(dx, 0, 0, 1.0f);
            Vector4f yAxis4f_translation = new Vector4f(0, dy, 0, 1.0f);

            Matrix4f.transform(ovr_inverse2, xAxis4f_translation, xAxis4f_translation);
            Matrix4f.transform(ovr_inverse2, yAxis4f_translation, yAxis4f_translation);
            Vector3f xAxis3 = new Vector3f(xAxis4f_translation.x, xAxis4f_translation.y, xAxis4f_translation.z);
            Vector3f yAxis3 = new Vector3f(yAxis4f_translation.x, yAxis4f_translation.y, yAxis4f_translation.z);

            Matrix4f.translate(xAxis3, viewport_translation, viewport_translation);
            Matrix4f.translate(yAxis3, viewport_translation, viewport_translation);
        }

        c3d.getTranslation().load(viewport_translation);

        perspective.calculateOriginData();

        c3d.getManipulator().setAccurateXaxis(new BigDecimal(c3d.getManipulator().getXaxis().x), new BigDecimal(c3d.getManipulator().getXaxis().y), new BigDecimal(c3d.getManipulator().getXaxis().z));
        c3d.getManipulator().setAccurateYaxis(new BigDecimal(c3d.getManipulator().getYaxis().x), new BigDecimal(c3d.getManipulator().getYaxis().y), new BigDecimal(c3d.getManipulator().getYaxis().z));
        c3d.getManipulator().setAccurateZaxis(new BigDecimal(c3d.getManipulator().getZaxis().x), new BigDecimal(c3d.getManipulator().getZaxis().y), new BigDecimal(c3d.getManipulator().getZaxis().z));
        c3d.getManipulator().setAccuratePosition(new BigDecimal(c3d.getManipulator().getPosition().x / 1000f), new BigDecimal(c3d.getManipulator().getPosition().y / 1000f),
                new BigDecimal(c3d.getManipulator().getPosition().z / 1000f));

        if (event != null) {
            c3d.getCursor3D().set(perspective.get3DCoordinatesFromScreen(event.x, event.y));
            c3d.getScreenXY().set(event.x, event.y);
            GuiManager.updateStatus(c3d);
            ((ScalableComposite) c3d.getParent()).redrawScales(event.x, event.y);
        }
    }

    /**
     * Returns a double-Array with the corner angles (in degree, 0 - 180)
     *
     * @param center
     *            one vertex from the triangle, indicates the corner with the
     *            alpha value {@code getTriangleAngles(...)[0]}
     * @param target
     *            one vertex from the triangle, indicates the corner with the
     *            beta value {@code getTriangleAngles(...)[1]}
     * @param supply
     *            one vertex from the triangle, indicates the corner with the
     *            gamma value {@code getTriangleAngles(...)[1]}
     * @return the double-Array with the corner angles (in degree)
     */
    public double[] getTriangleAngles(Vertex center, Vertex target, Vertex supply) {

        double[] result = new double[3];
        Vector3d target3d = new Vector3d(target);
        Vector3d temp = new Vector3d(center);

        Vector3d[] tvs3d = new Vector3d[3];
        tvs3d[0] = new Vector3d(center);
        tvs3d[1] = new Vector3d(target);
        tvs3d[2] = new Vector3d(supply);

        for (int i = 0; i < 3; i++) {
            Vector3d.sub(tvs3d[i], temp, tvs3d[i]);
        }
        Vector3d.sub(target3d, temp, target3d);

        Vector3d cat3d = new Vector3d(tvs3d[2]);

        result[0] = Vector3d.angle(target3d, cat3d);

        Vector3d.sub(cat3d, target3d, temp);

        result[1] = 180.0d - Vector3d.angle(target3d, temp);
        result[2] = 180.0d - result[0] - result[1];

        return result;
    }

    /**
     * Returns a double-Array with the corner angles (in degree, 0 - 360 and
     * NaN)
     *
     * @param center
     *            one vertex from the triangle, indicates the corner with the
     *            alpha value {@code getTriangleAngles(...)[0]}, can be greater
     *            than 180 degree
     * @param target
     *            one vertex from the triangle, indicates the corner with the
     *            beta value {@code getTriangleAngles(...)[1]}
     * @param supply
     *            one vertex from the triangle, indicates the corner with the
     *            gamma value {@code getTriangleAngles(...)[1]}
     * @return the double-Array with the corner angles (in degree)
     */
    public double[] getTriangleAngles2D(Vertex center, Vertex target, Vertex supply) {

        final double[] result = new double[3];

        if (target.equals(supply)) {
            result[0] = 0d;
            result[1] = Double.NaN;
            result[2] = Double.NaN;
            return result;
        }

        final double[][] vertex = new double[3][2];

        final Vertex[] tvs = new Vertex[3];
        tvs[1] = target;
        tvs[2] = supply;

        NLogger.debug(VertexManager.class, "getTriangleAngles2D()"); //$NON-NLS-1$

        for (int i = 1; i < 3; i++) {
            vertex[i][0] = tvs[i].x - center.x;
            vertex[i][1] = tvs[i].y - center.y;
            NLogger.debug(VertexManager.class, "vertex[" + i + "].x =" + Math.round(vertex[i][0])); //$NON-NLS-1$ //$NON-NLS-2$
            NLogger.debug(VertexManager.class, "vertex[" + i + "].y =" + Math.round(vertex[i][1])); //$NON-NLS-1$ //$NON-NLS-2$
        }

        final double lenght_b = Math.sqrt(Math.pow(vertex[1][0], 2) + Math.pow(vertex[1][1], 2));
        final double lenght_c = Math.sqrt(Math.pow(vertex[2][0], 2) + Math.pow(vertex[2][1], 2));

        result[0] = Math.acos(vertex[1][0] / lenght_b) * 180d / Math.PI;
        if (Double.isNaN(result[0])) {
            result[0] = 0d;
            result[1] = Double.NaN;
            result[2] = Double.NaN;
            return result;
        }

        if (vertex[1][1] > 0f) {
            result[0] = 360.0 - result[0];
        }
        NLogger.debug(VertexManager.class, "1st angle: " + result[0]); //$NON-NLS-1$

        result[1] = Math.acos(vertex[2][0] / lenght_c) * 180d / Math.PI;
        if (Double.isNaN(result[1])) {
            result[0] = 0d;
            result[1] = Double.NaN;
            result[2] = Double.NaN;
            return result;
        }

        if (vertex[2][1] > 0f) {
            result[1] = 360.0 - result[1];
        }
        NLogger.debug(VertexManager.class, "2nd angle: " + result[1]); //$NON-NLS-1$

        double temp;
        temp = result[1] - result[0];
        if (temp < 0d) {
            temp = 360d + temp;
        }
        if (temp > 180d || temp < -180d) {
            result[0] = temp;
            result[1] = Double.NaN;
            result[2] = Double.NaN;
        } else {
            return getTriangleAngles(center, target, supply);
        }

        return result;
    }

    public double getDistance(Vertex v1, Vertex v2) {
        return Math.sqrt(Math.pow(v1.x - v2.x, 2d) + Math.pow(v1.y - v2.y, 2d) + Math.pow(v1.z - v2.z, 2d));
    }

    double getDistance2D(Vertex v1, Vertex v2) {
        return Math.sqrt(Math.pow(v1.x - v2.x, 2d) + Math.pow(v1.y - v2.y, 2d));
    }

    public synchronized Set<Vertex> getSelectedVertices() {
        return selectedVertices;
    }

    public synchronized Set<GData> getSelectedData() {
        return selectedData;
    }

    public synchronized Set<GData1> getSelectedSubfiles() {
        return selectedSubfiles;
    }

    public synchronized Set<GData2> getSelectedLines() {
        return selectedLines;
    }

    public synchronized Set<GData3> getSelectedTriangles() {
        return selectedTriangles;
    }

    public synchronized Set<GData4> getSelectedQuads() {
        return selectedQuads;
    }

    public synchronized Set<GData5> getSelectedCondlines() {
        return selectedCondlines;
    }

    public synchronized HashMap<GData0, Vertex[]> getDeclaredVertices() {
        return new HashMap<GData0, Vertex[]>(declaredVertices);
    }

    public synchronized HashMap<GData2, Vertex[]> getLines() {
        return new HashMap<GData2, Vertex[]>(lines);
    }

    public synchronized HashMap<GData3, Vertex[]> getTriangles() {
        return new HashMap<GData3, Vertex[]>(triangles);
    }

    public synchronized ThreadsafeHashMap<GData3, Vertex[]> getTriangles_NOCLONE() {
        return triangles;
    }

    public synchronized ThreadsafeHashMap<GData4, Vertex[]> getQuads_NOCLONE() {
        return quads;
    }

    public HashSet<GData3> getLinkedTriangles(Vertex vertex) {
        HashSet<GData3> rval = new HashSet<GData3>();
        Set<VertexManifestation> vm = vertexLinkedToPositionInFile.get(vertex);
        if (vm != null) {
            for (VertexManifestation m : vm) {
                if (m.getGdata().type() == 3)
                    rval.add((GData3) m.getGdata());
            }
        }
        return rval;
    }

    public HashSet<GData> getLinkedSurfaces(Vertex vertex) {
        HashSet<GData> rval = new HashSet<GData>();
        Set<VertexManifestation> vm = vertexLinkedToPositionInFile.get(vertex);
        if (vm != null) {
            for (VertexManifestation m : vm) {
                int type = m.getGdata().type();
                if (type > 2 || type < 5)
                    rval.add(m.getGdata());
            }
        }
        return rval;
    }

    public synchronized HashMap<GData4, Vertex[]> getQuads() {
        return new HashMap<GData4, Vertex[]>(quads);
    }

    public synchronized HashMap<GData5, Vertex[]> getCondlines() {
        return new HashMap<GData5, Vertex[]>(condlines);
    }

    public boolean isModified() {
        return modified;
    }

    public synchronized void setModified(boolean modified, boolean addHistory) {
        if (modified) {
            setUpdated(false);
            syncWithTextEditors(addHistory);
        }
        this.modified = modified;
    }

    public boolean isUpdated() {
        return updated;
    }

    public synchronized void setUpdated(boolean updated) {
        this.updated = updated;
        if (updated) {
            ViewIdleManager.renderLDrawStandard[0].set(true);
        }
    }

    public synchronized void setModified_NoSync() {
        this.modified = true;
        setUpdated(false);
    }

    public Vertex getVertexToReplace() {
        return vertexToReplace;
    }

    public void setVertexToReplace(Vertex vertexToReplace) {
        this.vertexToReplace = vertexToReplace;
    }

    private String bigDecimalToString(BigDecimal bd) {
        String result;
        if (bd.compareTo(BigDecimal.ZERO) == 0)
            return "0"; //$NON-NLS-1$
        BigDecimal bd2 = bd.stripTrailingZeros();
        result = bd2.toPlainString();
        if (result.startsWith("-0."))return "-" + result.substring(2); //$NON-NLS-1$ //$NON-NLS-2$
        if (result.startsWith("0."))return result.substring(1); //$NON-NLS-1$
        return result;
    }

    TreeSet<Vertex> hiddenVertices = new TreeSet<Vertex>();
    HashSet<GData> hiddenData = new HashSet<GData>();

    private HashMap<GData, Byte> bfcMap = new HashMap<GData, Byte>();

    public void hideSelection() {
        for (GData1 data : selectedSubfiles) {
            hide(data);
        }
        for (GData2 data : selectedLines) {
            hide(data);
        }
        for (GData3 data : selectedTriangles) {
            hide(data);
        }
        for (GData4 data : selectedQuads) {
            hide(data);
        }
        for (GData5 data : selectedCondlines) {
            hide(data);
        }
        for (Vertex vert : selectedVertices) {
            Set<VertexManifestation> m = vertexLinkedToPositionInFile.get(vert);
            boolean isHidden = true;
            for (VertexManifestation vm : m) {
                if (vm.getGdata().type() != 0 && vm.getGdata().visible) {
                    isHidden = false;
                    break;
                }
            }
            if (isHidden)
                hiddenVertices.add(vert);
        }
        clearSelection();
    }

    private void hide(GData gdata) {
        gdata.hide();
        hiddenData.add(gdata);
    }

    public void showAll() {
        for (GData ghost : hiddenData) {
            ghost.show();
        }
        hiddenVertices.clear();
        hiddenData.clear();
    }

    /**
     *
     * @param transformation
     *            the transformation matrix
     * @param moveAdjacentData
     *            {@code true} if all data should be transformed which is
     *            adjacent to the current selection
     */
    public synchronized void transformSelection(Matrix transformation, boolean moveAdjacentData) {
        if (linkedDatFile.isReadOnly())
            return;

        final Set<Vertex> singleVertices = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());

        final HashSet<GData0> effSelectedVertices = new HashSet<GData0>();
        final HashSet<GData2> effSelectedLines = new HashSet<GData2>();
        final HashSet<GData3> effSelectedTriangles = new HashSet<GData3>();
        final HashSet<GData4> effSelectedQuads = new HashSet<GData4>();
        final HashSet<GData5> effSelectedCondlines = new HashSet<GData5>();

        selectedData.clear();

        // 0. Deselect selected subfile data (for whole selected subfiles)
        for (GData1 subf : selectedSubfiles) {
            Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
            for (VertexInfo vertexInfo : vis) {
                if (!moveAdjacentData)
                    selectedVertices.remove(vertexInfo.getVertex());
                GData g = vertexInfo.getLinkedData();
                if (lineLinkedToVertices.containsKey(g)) continue;
                switch (g.type()) {
                case 2:
                    selectedLines.remove(g);
                    break;
                case 3:
                    selectedTriangles.remove(g);
                    break;
                case 4:
                    selectedQuads.remove(g);
                    break;
                case 5:
                    selectedCondlines.remove(g);
                    break;
                default:
                    break;
                }
            }
        }

        // 1. Vertex Based Selection
        {
            final Set<Vertex> objectVertices = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());
            if (moveAdjacentData) {
                HashMap<GData, Integer> occurMap = new HashMap<GData, Integer>();
                for (Vertex vertex : selectedVertices) {
                    Set<VertexManifestation> occurences = vertexLinkedToPositionInFile.get(vertex);
                    if (occurences == null)
                        continue;
                    boolean isPureSubfileVertex = true;
                    for (VertexManifestation vm : occurences) {
                        GData g = vm.getGdata();
                        int val = 1;
                        if (occurMap.containsKey(g)) {
                            val = occurMap.get(g);
                            val++;
                        }
                        occurMap.put(g, val);
                        switch (g.type()) {
                        case 0:
                            GData0 meta = (GData0) g;
                            boolean idCheck = !lineLinkedToVertices.containsKey(meta);
                            isPureSubfileVertex = isPureSubfileVertex && idCheck;
                            if (val == 1) {
                                if (!idCheck) {
                                    effSelectedVertices.add(meta);
                                    newSelectedData.add(meta);
                                }
                            }
                            break;
                        case 2:
                            GData2 line = (GData2) g;
                            idCheck = !line.parent.equals(View.DUMMY_REFERENCE);
                            isPureSubfileVertex = isPureSubfileVertex && idCheck;
                            if (val == 2) {
                                if (!idCheck) {
                                    effSelectedLines.add(line);
                                    newSelectedData.add(line);
                                }
                            }
                            break;
                        case 3:
                            GData3 triangle = (GData3) g;
                            idCheck = !triangle.parent.equals(View.DUMMY_REFERENCE);
                            isPureSubfileVertex = isPureSubfileVertex && idCheck;
                            if (val == 3) {
                                if (!idCheck) {
                                    effSelectedTriangles.add(triangle);
                                    newSelectedData.add(triangle);
                                }
                            }
                            break;
                        case 4:
                            GData4 quad = (GData4) g;
                            idCheck = !quad.parent.equals(View.DUMMY_REFERENCE);
                            isPureSubfileVertex = isPureSubfileVertex && idCheck;
                            if (val == 4) {
                                if (!idCheck) {
                                    effSelectedQuads.add(quad);
                                    newSelectedData.add(quad);
                                }
                            }
                            break;
                        case 5:
                            GData5 condline = (GData5) g;
                            idCheck = !condline.parent.equals(View.DUMMY_REFERENCE);
                            isPureSubfileVertex = isPureSubfileVertex && idCheck;
                            if (val == 4) {
                                if (!idCheck) {
                                    effSelectedCondlines.add(condline);
                                    newSelectedData.add(condline);
                                }
                            }
                            break;
                        }
                    }
                    if (isPureSubfileVertex)
                        objectVertices.add(vertex);
                }
            }

            // 2. Object Based Selection

            for (GData2 line : selectedLines) {
                if (line.parent.equals(View.DUMMY_REFERENCE))
                    effSelectedLines.add(line);
                Vertex[] verts = lines.get(line);
                if (verts == null)
                    continue;
                for (Vertex vertex : verts) {
                    objectVertices.add(vertex);
                }
            }
            for (GData3 triangle : selectedTriangles) {
                if (triangle.parent.equals(View.DUMMY_REFERENCE))
                    effSelectedTriangles.add(triangle);
                Vertex[] verts = triangles.get(triangle);
                if (verts == null)
                    continue;
                for (Vertex vertex : verts) {
                    objectVertices.add(vertex);
                }
            }
            for (GData4 quad : selectedQuads) {
                if (quad.parent.equals(View.DUMMY_REFERENCE))
                    effSelectedQuads.add(quad);
                Vertex[] verts = quads.get(quad);
                if (verts == null)
                    continue;
                for (Vertex vertex : verts) {
                    objectVertices.add(vertex);
                }
            }
            for (GData5 condline : selectedCondlines) {
                if (condline.parent.equals(View.DUMMY_REFERENCE))
                    effSelectedCondlines.add(condline);
                Vertex[] verts = condlines.get(condline);
                if (verts == null)
                    continue;
                for (Vertex vertex : verts) {
                    objectVertices.add(vertex);
                }
            }

            Set<GData0> vs = new HashSet<GData0>(effSelectedVertices);
            for (GData0 effvert : vs) {
                Vertex v = effvert.getVertex();
                if (v != null && objectVertices.contains(v)) {
                    effSelectedVertices.remove(effvert);
                }
            }

            singleVertices.addAll(selectedVertices);
            singleVertices.removeAll(objectVertices);

            // Reduce the amount of superflous selected data
            selectedVertices.clear();
            selectedVertices.addAll(singleVertices);

            selectedLines.clear();
            selectedLines.addAll(effSelectedLines);

            selectedTriangles.clear();
            selectedTriangles.addAll(effSelectedTriangles);

            selectedQuads.clear();
            selectedQuads.addAll(effSelectedQuads);

            selectedCondlines.clear();
            selectedCondlines.addAll(effSelectedCondlines);

            // 3. Transformation of the selected data (no whole subfiles!!)
            // + selectedData update!


            HashSet<GData> allData = new HashSet<GData>();
            allData.addAll(selectedLines);
            allData.addAll(selectedTriangles);
            allData.addAll(selectedQuads);
            allData.addAll(selectedCondlines);
            HashSet<Vertex> allVertices = new HashSet<Vertex>();
            allVertices.addAll(selectedVertices);
            transform(allData, allVertices, transformation, true, moveAdjacentData);

            // 4. Subfile Based Transformation & Selection
            if (!selectedSubfiles.isEmpty()) {
                HashBiMap<Integer, GData> drawPerLine = linkedDatFile.getDrawPerLine_NOCLONE();
                HashSet<GData1> newSubfiles = new HashSet<GData1>();
                for (GData1 subf : selectedSubfiles) {
                    String transformedString = subf.getTransformedString(transformation, linkedDatFile, true);
                    GData transformedSubfile = DatParser
                            .parseLine(transformedString, drawPerLine.getKey(subf).intValue(), 0, subf.r, subf.g, subf.b, subf.a, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile,
                                    false, new HashSet<String>(), false).get(0).getGraphicalData();
                    if (subf.equals(linkedDatFile.getDrawChainTail()))
                        linkedDatFile.setDrawChainTail(transformedSubfile);
                    GData oldNext = subf.getNext();
                    GData oldBefore = subf.getBefore();
                    oldBefore.setNext(transformedSubfile);
                    transformedSubfile.setNext(oldNext);
                    Integer oldNumber = drawPerLine.getKey(subf);
                    if (oldNumber != null)
                        drawPerLine.put(oldNumber, transformedSubfile);
                    remove(subf);
                    newSubfiles.add((GData1) transformedSubfile);
                }
                selectedSubfiles.clear();
                selectedSubfiles.addAll(newSubfiles);

                for (GData1 subf : selectedSubfiles) {
                    Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
                    for (VertexInfo vertexInfo : vis) {
                        selectedVertices.add(vertexInfo.getVertex());
                        GData g = vertexInfo.getLinkedData();
                        switch (g.type()) {
                        case 2:
                            selectedLines.add((GData2) g);
                            break;
                        case 3:
                            selectedTriangles.add((GData3) g);
                            break;
                        case 4:
                            selectedQuads.add((GData4) g);
                            break;
                        case 5:
                            selectedCondlines.add((GData5) g);
                            break;
                        default:
                            break;
                        }
                    }
                }
                setModified_NoSync();
            }

            if (isModified()) {
                selectedData.addAll(selectedLines);
                selectedData.addAll(selectedTriangles);
                selectedData.addAll(selectedQuads);
                selectedData.addAll(selectedCondlines);
                selectedData.addAll(selectedSubfiles);

                syncWithTextEditors(true);
                updateUnsavedStatus();

            }
            newSelectedData.clear();
            selectedVertices.retainAll(vertexLinkedToPositionInFile.keySet());
        }
    }

    /**
     * Transforms the selection (vertices or data)
     * @param allData
     * @param allVertices
     * @param transformation
     * @param moveAdjacentData
     */
    private void transform(Set<GData> allData, Set<Vertex> allVertices, Matrix transformation, boolean updateSelection, boolean moveAdjacentData) {
        HashSet<GData> allData2 = new HashSet<GData>(allData);
        TreeSet<Vertex> verticesToTransform = new TreeSet<Vertex>();
        verticesToTransform.addAll(allVertices);
        for (GData gd : allData) {
            Set<VertexInfo> vis = lineLinkedToVertices.get(gd);
            if (vis != null) {
                for (VertexInfo vi : vis) {
                    allVertices.add(vi.vertex);
                }
            }
        }
        TreeMap<Vertex, Vertex> oldToNewVertex = new TreeMap<Vertex, Vertex>();
        // Calculate the new vertex position
        for (Vertex v : allVertices) {
            BigDecimal[] temp = transformation.transform(v.X, v.Y, v.Z);
            oldToNewVertex.put(v, new Vertex(temp[0], temp[1], temp[2]));
        }
        // Evaluate the adjacency
        HashMap<GData, Integer> verticesCountPerGData = new HashMap<GData, Integer>();
        for (Vertex v : allVertices) {
            Set<VertexManifestation> manis = vertexLinkedToPositionInFile.get(v);
            if (manis == null) continue;
            for (VertexManifestation m : manis) {
                GData gd = m.getGdata();
                if (lineLinkedToVertices.containsKey(gd)) {
                    final int type = gd.type();
                    switch (type) {
                    case 0:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        break;
                    default:
                        continue;
                    }
                    if (verticesCountPerGData.containsKey(gd)) {
                        verticesCountPerGData.put(gd, verticesCountPerGData.get(gd) + 1);
                    } else {
                        verticesCountPerGData.put(gd, 1);
                    }
                    allData2.add(gd);
                }
            }
        }
        // Transform the data
        if (updateSelection) {
            selectedData.clear();
            selectedLines.clear();
            selectedTriangles.clear();
            selectedQuads.clear();
            selectedCondlines.clear();
        }
        HashSet<GData> allNewData = new HashSet<GData>();
        for (GData gd : allData2) {
            GData newData = null;
            final int type = gd.type();
            switch (type) {
            case 0:
            {
                Vertex[] verts = declaredVertices.get(gd);
                if (verts != null) {
                    Vertex v1 = oldToNewVertex.get(verts[0]);
                    if (v1 == null) v1 = verts[0];
                    newData = addVertex(v1);
                    if (updateSelection) {
                        selectedVertices.remove(verts[0]);
                        selectedVertices.add(v1);
                    }
                }
            }
            break;
            case 2:
            {
                int avc = 0;
                Vertex[] verts = lines.get(gd);
                if (verts != null) {
                    Vertex v1 = oldToNewVertex.get(verts[0]);
                    Vertex v2 = oldToNewVertex.get(verts[1]);
                    if (v1 == null) v1 = verts[0]; else avc++;
                    if (v2 == null) v2 = verts[1]; else avc++;
                    if (!moveAdjacentData && (avc != 2 || !allData.contains(gd))) continue;
                    if (updateSelection) {
                        if (selectedVertices.contains(verts[0])) {
                            selectedVertices.remove(verts[0]);
                            selectedVertices.add(v1);
                        }
                        if (selectedVertices.contains(verts[1])) {
                            selectedVertices.remove(verts[1]);
                            selectedVertices.add(v2);
                        }
                    }
                    GData2 g2 = (GData2) gd;
                    newData = new GData2(g2.colourNumber, g2.r, g2.g, g2.b, g2.a, v1, v2, g2.parent, linkedDatFile);
                }
            }
            break;
            case 3:
            {
                int avc = 0;
                Vertex[] verts = triangles.get(gd);
                if (verts != null) {
                    Vertex v1 = oldToNewVertex.get(verts[0]);
                    Vertex v2 = oldToNewVertex.get(verts[1]);
                    Vertex v3 = oldToNewVertex.get(verts[2]);
                    if (v1 == null) v1 = verts[0]; else avc++;
                    if (v2 == null) v2 = verts[1]; else avc++;
                    if (v3 == null) v3 = verts[2]; else avc++;
                    if (!moveAdjacentData && (avc != 3 || !allData.contains(gd))) continue;
                    if (updateSelection) {
                        if (selectedVertices.contains(verts[0])) {
                            selectedVertices.remove(verts[0]);
                            selectedVertices.add(v1);
                        }
                        if (selectedVertices.contains(verts[1])) {
                            selectedVertices.remove(verts[1]);
                            selectedVertices.add(v2);
                        }
                        if (selectedVertices.contains(verts[2])) {
                            selectedVertices.remove(verts[2]);
                            selectedVertices.add(v3);
                        }
                    }
                    GData3 g3 = (GData3) gd;
                    newData = new GData3(g3.colourNumber, g3.r, g3.g, g3.b, g3.a, v1, v2, v3, g3.parent, linkedDatFile);
                }
            }
            break;
            case 4:
            {
                int avc = 0;
                Vertex[] verts = quads.get(gd);
                if (verts != null) {
                    Vertex v1 = oldToNewVertex.get(verts[0]);
                    Vertex v2 = oldToNewVertex.get(verts[1]);
                    Vertex v3 = oldToNewVertex.get(verts[2]);
                    Vertex v4 = oldToNewVertex.get(verts[3]);
                    if (v1 == null) v1 = verts[0]; else avc++;
                    if (v2 == null) v2 = verts[1]; else avc++;
                    if (v3 == null) v3 = verts[2]; else avc++;
                    if (v4 == null) v4 = verts[3]; else avc++;
                    if (!moveAdjacentData && (avc != 4 || !allData.contains(gd))) continue;
                    if (updateSelection) {
                        if (selectedVertices.contains(verts[0])) {
                            selectedVertices.remove(verts[0]);
                            selectedVertices.add(v1);
                        }
                        if (selectedVertices.contains(verts[1])) {
                            selectedVertices.remove(verts[1]);
                            selectedVertices.add(v2);
                        }
                        if (selectedVertices.contains(verts[2])) {
                            selectedVertices.remove(verts[2]);
                            selectedVertices.add(v3);
                        }
                        if (selectedVertices.contains(verts[3])) {
                            selectedVertices.remove(verts[3]);
                            selectedVertices.add(v4);
                        }
                    }
                    GData4 g4 = (GData4) gd;
                    newData = new GData4(g4.colourNumber, g4.r, g4.g, g4.b, g4.a, v1, v2, v3, v4, g4.parent, linkedDatFile);
                }
            }
            break;
            case 5:
            {
                int avc = 0;
                Vertex[] verts = condlines.get(gd);
                if (verts != null) {
                    Vertex v1 = oldToNewVertex.get(verts[0]);
                    Vertex v2 = oldToNewVertex.get(verts[1]);
                    Vertex v3 = oldToNewVertex.get(verts[2]);
                    Vertex v4 = oldToNewVertex.get(verts[3]);
                    if (v1 == null) v1 = verts[0]; else avc++;
                    if (v2 == null) v2 = verts[1]; else avc++;
                    if (v3 == null) v3 = verts[2]; else avc++;
                    if (v4 == null) v4 = verts[3]; else avc++;
                    if (!moveAdjacentData && (avc != 4 || !allData.contains(gd))) continue;
                    if (updateSelection) {
                        if (selectedVertices.contains(verts[0])) {
                            selectedVertices.remove(verts[0]);
                            selectedVertices.add(v1);
                        }
                        if (selectedVertices.contains(verts[1])) {
                            selectedVertices.remove(verts[1]);
                            selectedVertices.add(v2);
                        }
                        if (selectedVertices.contains(verts[2])) {
                            selectedVertices.remove(verts[2]);
                            selectedVertices.add(v3);
                        }
                        if (selectedVertices.contains(verts[3])) {
                            selectedVertices.remove(verts[3]);
                            selectedVertices.add(v4);
                        }
                    }
                    GData5 g5 = (GData5) gd;
                    newData = new GData5(g5.colourNumber, g5.r, g5.g, g5.b, g5.a, v1, v2, v3, v4, g5.parent, linkedDatFile);
                }
            }
            break;
            default:
                continue;
            }
            if (newData != null) {
                linker(gd, newData);
                allNewData.add(newData);
                setModified_NoSync();
                if (updateSelection) {
                    switch (newData.type()) {
                    case 2:
                        if (verticesCountPerGData.get(gd) != 2) continue;
                        selectedLines.add((GData2) newData);
                        break;
                    case 3:
                        if (verticesCountPerGData.get(gd) != 3) continue;
                        selectedTriangles.add((GData3) newData);
                        break;
                    case 4:
                        if (verticesCountPerGData.get(gd) != 4) continue;
                        selectedQuads.add((GData4) newData);
                        break;
                    case 5:
                        if (verticesCountPerGData.get(gd) != 4) continue;
                        selectedCondlines.add((GData5) newData);
                        break;
                    default:
                        continue;
                    }
                    selectedData.add(newData);
                }
            }
        }
        if (updateSelection && moveAdjacentData) {
            for (Vertex v : oldToNewVertex.keySet()) {
                Vertex nv = oldToNewVertex.get(v);
                if (nv != null) {
                    if (vertexLinkedToPositionInFile.containsKey(nv)) {
                        selectedVertices.add(nv);
                    }
                }
            }
        }
    }

    private void linker(GData oldData, GData newData) {
        HashBiMap<Integer, GData> drawPerLine = linkedDatFile.getDrawPerLine_NOCLONE();
        if (oldData.equals(linkedDatFile.getDrawChainTail()))
            linkedDatFile.setDrawChainTail(newData);
        GData oldNext = oldData.getNext();
        GData oldBefore = oldData.getBefore();
        oldBefore.setNext(newData);
        newData.setNext(oldNext);
        Integer oldNumber = drawPerLine.getKey(oldData);
        if (oldNumber != null)
            drawPerLine.put(oldNumber, newData);
        remove(oldData);
    }

    public synchronized void windingChangeSelection() {
        if (linkedDatFile.isReadOnly())
            return;

        final HashSet<GData2> effSelectedLines = new HashSet<GData2>();
        final HashSet<GData3> effSelectedTriangles = new HashSet<GData3>();
        final HashSet<GData4> effSelectedQuads = new HashSet<GData4>();
        final HashSet<GData5> effSelectedCondlines = new HashSet<GData5>();

        final HashSet<GData2> subSelectedLines = new HashSet<GData2>();
        final HashSet<GData3> subSelectedTriangles = new HashSet<GData3>();
        final HashSet<GData4> subSelectedQuads = new HashSet<GData4>();
        final HashSet<GData5> subSelectedCondlines = new HashSet<GData5>();

        selectedData.clear();
        selectedVertices.clear();

        // 0. Deselect selected subfile data (for whole selected subfiles)
        for (GData1 subf : selectedSubfiles) {
            Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
            for (VertexInfo vertexInfo : vis) {
                GData gt = vertexInfo.getLinkedData();
                switch (gt.type()) {
                case 2:
                    selectedLines.remove(gt);
                    break;
                case 3:
                    selectedTriangles.remove(gt);
                    break;
                case 4:
                    selectedQuads.remove(gt);
                    break;
                case 5:
                    selectedCondlines.remove(gt);
                    break;
                default:
                    break;
                }
            }
        }

        // 2. Object Based Selection

        for (GData2 line : selectedLines) {
            if (line.parent.equals(View.DUMMY_REFERENCE))
                effSelectedLines.add(line);
            else
                subSelectedLines.add(line);
        }
        for (GData3 triangle : selectedTriangles) {
            if (triangle.parent.equals(View.DUMMY_REFERENCE))
                effSelectedTriangles.add(triangle);
            else
                subSelectedTriangles.add(triangle);
        }
        for (GData4 quad : selectedQuads) {
            if (quad.parent.equals(View.DUMMY_REFERENCE))
                effSelectedQuads.add(quad);
            else
                subSelectedQuads.add(quad);
        }
        for (GData5 condline : selectedCondlines) {
            if (condline.parent.equals(View.DUMMY_REFERENCE))
                effSelectedCondlines.add(condline);
            else
                subSelectedCondlines.add(condline);
        }

        // 3. Winding change of the selected data (no whole subfiles!!)
        // + selectedData update!

        ArrayList<GData> modData = new ArrayList<GData>();

        for (GData3 gd : effSelectedTriangles)
            modData.add(changeWinding(gd));
        for (GData4 gd : effSelectedQuads)
            modData.add(changeWinding(gd));

        modData.addAll(effSelectedLines);
        modData.addAll(effSelectedCondlines);

        selectedLines.clear();
        selectedTriangles.clear();
        selectedQuads.clear();
        selectedCondlines.clear();

        for (GData gData : modData) {
            switch (gData.type()) {
            case 2:
                selectedLines.add((GData2) gData);
                break;
            case 3:
                selectedTriangles.add((GData3) gData);
                break;
            case 4:
                selectedQuads.add((GData4) gData);
                break;
            case 5:
                selectedCondlines.add((GData5) gData);
                break;
            }
        }

        if (!selectedTriangles.isEmpty())
            setModified_NoSync();
        if (!selectedQuads.isEmpty())
            setModified_NoSync();

        // 4. Subfile Based Change & Selection
        if (!selectedSubfiles.isEmpty()) {


            final HashBiMap<Integer, GData> dpl = linkedDatFile.getDrawPerLine_NOCLONE();
            HashSet<GData1> newSubfiles = new HashSet<GData1>();
            for (GData1 subf : selectedSubfiles) {
                GData1 untransformedSubfile;
                StringBuilder colourBuilder = new StringBuilder();
                if (subf.colourNumber == -1) {
                    colourBuilder.append("0x2"); //$NON-NLS-1$
                    colourBuilder.append(MathHelper.toHex((int) (255f * subf.r)).toUpperCase());
                    colourBuilder.append(MathHelper.toHex((int) (255f * subf.g)).toUpperCase());
                    colourBuilder.append(MathHelper.toHex((int) (255f * subf.b)).toUpperCase());
                } else {
                    colourBuilder.append(subf.colourNumber);
                }
                untransformedSubfile = (GData1) DatParser
                        .parseLine("1 " + colourBuilder.toString() + " 0 0 0 1 0 0 0 1 0 0 0 1 " + subf.shortName , 0, 0, 0.5f, 0.5f, 0.5f, 1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile, false, //$NON-NLS-1$ //$NON-NLS-2$
                                new HashSet<String>(), false).get(0).getGraphicalData();
                boolean plainOnX = untransformedSubfile.boundingBoxMin.x - untransformedSubfile.boundingBoxMax.x == 0f;
                boolean plainOnY = untransformedSubfile.boundingBoxMin.y - untransformedSubfile.boundingBoxMax.y == 0f;
                boolean plainOnZ = untransformedSubfile.boundingBoxMin.z - untransformedSubfile.boundingBoxMax.z == 0f;


                boolean hasInvertnext = false;
                GData invertNextData = subf.getBefore();

                // Check if a INVERTNEXT is present
                while (invertNextData != null && invertNextData.type() != 1 && (invertNextData.type() != 6 || ((GDataBFC) invertNextData).type != BFC.INVERTNEXT)) {
                    invertNextData = invertNextData.getBefore();
                }
                if (invertNextData != null && invertNextData.type() == 6) {
                    hasInvertnext = true;
                }

                boolean needsInvertNext = false;

                GData1 newSubfile = subf;

                if (plainOnX ^ plainOnY ^ plainOnZ) {
                    Matrix m;
                    final Matrix t = subf.accurateLocalMatrix;
                    if (plainOnX) {
                        m = new Matrix(
                                t.M00.negate(), t.M01.negate(), t.M02.negate(), t.M03,
                                t.M10, t.M11, t.M12, t.M13,
                                t.M20, t.M21, t.M22, t.M23,
                                t.M30, t.M31, t.M32, t.M33);
                    } else if (plainOnY) {
                        m = new Matrix(
                                t.M00, t.M01, t.M02, t.M03,
                                t.M10.negate(), t.M11.negate(), t.M12.negate(), t.M13,
                                t.M20, t.M21, t.M22, t.M23,
                                t.M30, t.M31, t.M32, t.M33);
                    } else {
                        m = new Matrix(
                                t.M00, t.M01, t.M02, t.M03,
                                t.M10, t.M11, t.M12, t.M13,
                                t.M20.negate(), t.M21.negate(), t.M22.negate(), t.M23,
                                t.M30, t.M31, t.M32, t.M33);
                    }
                    newSubfile = (GData1) DatParser
                            .parseLine(untransformedSubfile.getTransformedString(m, linkedDatFile, false) , dpl.getKey(subf).intValue(), 0, 0.5f, 0.5f, 0.5f, 1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile, false,
                                    new HashSet<String>(), false).get(0).getGraphicalData();
                } else {
                    needsInvertNext = true;
                }
                remove(untransformedSubfile);

                if (hasInvertnext) {
                    // Remove Invert Next
                    GDataBFC gbfc = (GDataBFC) invertNextData;
                    dpl.removeByValue(gbfc);
                    gbfc.getBefore().setNext(gbfc.getNext());
                    remove(gbfc);
                } else if (needsInvertNext && !hasInvertnext) {
                    // Add Invert Next
                    GData before = subf.getBefore();
                    int lineToInsert = dpl.getKey(before);
                    TreeSet<Integer> ts = new TreeSet<Integer>();
                    ts.addAll(dpl.keySet());
                    for (Iterator<Integer> dsi = ts.descendingIterator(); dsi.hasNext();) {
                        Integer k = dsi.next();
                        if (k > lineToInsert) {
                            GData gdata = dpl.getValue(k);
                            dpl.removeByKey(k);
                            dpl.put(k + 1, gdata);
                        }
                    }
                    GDataBFC newInvNext = new GDataBFC(BFC.INVERTNEXT);
                    dpl.put(lineToInsert + 1, newInvNext);
                    before.setNext(newInvNext);
                    newInvNext.setNext(subf);
                }

                if (hasInvertnext || needsInvertNext && !hasInvertnext) {

                    // Update Draw per line

                    TreeSet<Integer> ts = new TreeSet<Integer>();
                    ts.addAll(dpl.keySet());

                    int counter = 1;
                    GData tail = null;
                    for (Integer k : ts) {
                        GData gdata = dpl.getValue(k);
                        dpl.removeByKey(k);
                        dpl.put(counter, gdata);
                        counter++;
                        tail = gdata;
                    }
                    if (tail != null) {
                        linkedDatFile.setDrawChainTail(tail);
                    } else {
                        GData0 blankLine = new GData0(""); //$NON-NLS-1$
                        linkedDatFile.getDrawChainStart().setNext(blankLine);
                        dpl.put(1, blankLine);
                        linkedDatFile.setDrawChainTail(blankLine);
                    }
                }

                if (!subf.equals(newSubfile)) {
                    if (subf.equals(linkedDatFile.getDrawChainTail()))
                        linkedDatFile.setDrawChainTail(newSubfile);
                    GData oldNext = subf.getNext();
                    GData oldBefore = subf.getBefore();
                    oldBefore.setNext(newSubfile);
                    newSubfile.setNext(oldNext);
                    Integer oldNumber = dpl.getKey(subf);
                    if (oldNumber != null)
                        dpl.put(oldNumber, newSubfile);
                    remove(subf);
                }
                newSubfiles.add(newSubfile);

            }
            selectedSubfiles.clear();
            selectedSubfiles.addAll(newSubfiles);

            for (GData1 subf : selectedSubfiles) {
                Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
                for (VertexInfo vertexInfo : vis) {
                    selectedVertices.add(vertexInfo.getVertex());
                    GData gt = vertexInfo.getLinkedData();
                    switch (gt.type()) {
                    case 2:
                        selectedLines.add((GData2) gt);
                        break;
                    case 3:
                        selectedTriangles.add((GData3) gt);
                        break;
                    case 4:
                        selectedQuads.add((GData4) gt);
                        break;
                    case 5:
                        selectedCondlines.add((GData5) gt);
                        break;
                    default:
                        break;
                    }
                }
            }
            setModified_NoSync();
        }

        if (isModified()) {
            selectedLines.addAll(subSelectedLines);
            selectedTriangles.addAll(subSelectedTriangles);
            selectedQuads.addAll(subSelectedQuads);
            selectedCondlines.addAll(subSelectedCondlines);
            selectedData.addAll(selectedLines);
            selectedData.addAll(selectedTriangles);
            selectedData.addAll(selectedQuads);
            selectedData.addAll(selectedCondlines);
            selectedData.addAll(selectedSubfiles);

            syncWithTextEditors(true);
            updateUnsavedStatus();
        }

    }

    public synchronized GData changeWinding(GData dataToModify) {
        HashSet<GData> newSet = new HashSet<GData>();
        newSet.add(dataToModify);
        changeWinding(newSet);
        if (newSet.iterator().hasNext()) {
            return newSet.iterator().next();
        } else {
            return null;
        }
    }

    public synchronized void changeWinding(Set<GData> dataToModify) {
        HashSet<GData> newData = new HashSet<GData>();
        for (GData gData : dataToModify) {
            GData newGData = null;
            switch (gData.type()) {
            case 3:
                GData3 gd3 = (GData3) gData;
                GData3 newGdata3 = new GData3(gd3.colourNumber, gd3.r, gd3.g, gd3.b, gd3.a, gd3.X2, gd3.Y2, gd3.Z2, gd3.X1, gd3.Y1, gd3.Z1, gd3.X3, gd3.Y3, gd3.Z3, gd3.x2, gd3.y2, gd3.z2, gd3.x1, gd3.y1, gd3.z1, gd3.x3,
                        gd3.y3, gd3.z3, -gd3.xn, -gd3.yn, -gd3.zn, gd3.parent, linkedDatFile);
                newData.add(newGdata3);
                newGData = newGdata3;
                break;
            case 4:
                GData4 gd4 = (GData4) gData;
                GData4 newGdata4 = new GData4(gd4.colourNumber, gd4.r, gd4.g, gd4.b, gd4.a, gd4.X3, gd4.Y3, gd4.Z3, gd4.X2, gd4.Y2, gd4.Z2, gd4.X1, gd4.Y1, gd4.Z1, gd4.X4, gd4.Y4, gd4.Z4, gd4.x3, gd4.y3, gd4.z3, gd4.x2,
                        gd4.y2, gd4.z2, gd4.x1, gd4.y1, gd4.z1, gd4.x4, gd4.y4, gd4.z4, -gd4.xn, -gd4.yn, -gd4.zn, gd4.parent, linkedDatFile);
                newData.add(newGdata4);
                newGData = newGdata4;
                break;
            }
            linker(gData, newGData);
        }
        dataToModify.clear();
        dataToModify.addAll(newData);
    }

    public synchronized void colourChangeSelection(int index, float r, float g, float b, float a) {
        if (linkedDatFile.isReadOnly())
            return;

        final HashSet<GData2> effSelectedLines = new HashSet<GData2>();
        final HashSet<GData3> effSelectedTriangles = new HashSet<GData3>();
        final HashSet<GData4> effSelectedQuads = new HashSet<GData4>();
        final HashSet<GData5> effSelectedCondlines = new HashSet<GData5>();

        final HashSet<GData2> subSelectedLines = new HashSet<GData2>();
        final HashSet<GData3> subSelectedTriangles = new HashSet<GData3>();
        final HashSet<GData4> subSelectedQuads = new HashSet<GData4>();
        final HashSet<GData5> subSelectedCondlines = new HashSet<GData5>();

        selectedData.clear();
        selectedVertices.clear();

        // 0. Deselect selected subfile data (for whole selected subfiles)
        for (GData1 subf : selectedSubfiles) {
            Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
            for (VertexInfo vertexInfo : vis) {
                GData gt = vertexInfo.getLinkedData();
                switch (gt.type()) {
                case 2:
                    selectedLines.remove(gt);
                    break;
                case 3:
                    selectedTriangles.remove(gt);
                    break;
                case 4:
                    selectedQuads.remove(gt);
                    break;
                case 5:
                    selectedCondlines.remove(gt);
                    break;
                default:
                    break;
                }
            }
        }



        // 2. Object Based Selection

        for (GData2 line : selectedLines) {
            if (line.parent.equals(View.DUMMY_REFERENCE))
                effSelectedLines.add(line);
            else
                subSelectedLines.add(line);
        }
        for (GData3 triangle : selectedTriangles) {
            if (triangle.parent.equals(View.DUMMY_REFERENCE))
                effSelectedTriangles.add(triangle);
            else
                subSelectedTriangles.add(triangle);
        }
        for (GData4 quad : selectedQuads) {
            if (quad.parent.equals(View.DUMMY_REFERENCE))
                effSelectedQuads.add(quad);
            else
                subSelectedQuads.add(quad);
        }
        for (GData5 condline : selectedCondlines) {
            if (condline.parent.equals(View.DUMMY_REFERENCE))
                effSelectedCondlines.add(condline);
            else
                subSelectedCondlines.add(condline);
        }

        // 3. Transformation of the selected data (no whole subfiles!!)
        // + selectedData update!

        ArrayList<GData> modData = new ArrayList<GData>();
        for (GData2 gd : effSelectedLines)
            modData.add(changeColour(index, r, g, b, a, gd));
        for (GData3 gd : effSelectedTriangles)
            modData.add(changeColour(index, r, g, b, a, gd));
        for (GData4 gd : effSelectedQuads)
            modData.add(changeColour(index, r, g, b, a, gd));
        for (GData5 gd : effSelectedCondlines)
            modData.add(changeColour(index, r, g, b, a, gd));

        selectedLines.clear();
        selectedTriangles.clear();
        selectedQuads.clear();
        selectedCondlines.clear();

        for (GData gData : modData) {
            switch (gData.type()) {
            case 2:
                selectedLines.add((GData2) gData);
                break;
            case 3:
                selectedTriangles.add((GData3) gData);
                break;
            case 4:
                selectedQuads.add((GData4) gData);
                break;
            case 5:
                selectedCondlines.add((GData5) gData);
                break;
            }
        }

        if (!selectedLines.isEmpty())
            setModified_NoSync();
        if (!selectedTriangles.isEmpty())
            setModified_NoSync();
        if (!selectedQuads.isEmpty())
            setModified_NoSync();
        if (!selectedCondlines.isEmpty())
            setModified_NoSync();

        // 4. Subfile Based Change & Selection
        if (!selectedSubfiles.isEmpty()) {

            StringBuilder colourBuilder = new StringBuilder();
            if (index == -1) {
                colourBuilder.append("0x2"); //$NON-NLS-1$
                colourBuilder.append(MathHelper.toHex((int) (255f * r)).toUpperCase());
                colourBuilder.append(MathHelper.toHex((int) (255f * g)).toUpperCase());
                colourBuilder.append(MathHelper.toHex((int) (255f * b)).toUpperCase());
            } else {
                colourBuilder.append(index);
            }
            String col = colourBuilder.toString();

            HashBiMap<Integer, GData> drawPerLine = linkedDatFile.getDrawPerLine_NOCLONE();
            HashSet<GData1> newSubfiles = new HashSet<GData1>();
            for (GData1 subf : selectedSubfiles) {
                String colouredString = subf.getColouredString(col);
                GData oldNext = subf.getNext();
                GData oldBefore = subf.getBefore();
                remove(subf);
                GData colouredSubfile;
                if ("16".equals(col)) { //$NON-NLS-1$
                    colouredSubfile = DatParser
                            .parseLine(colouredString, drawPerLine.getKey(subf).intValue(), 0, 0.5f, 0.5f, 0.5f, 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile, false,
                                    new HashSet<String>(), false).get(0).getGraphicalData();
                } else {
                    colouredSubfile = DatParser
                            .parseLine(colouredString, drawPerLine.getKey(subf).intValue(), 0, subf.r, subf.g, subf.b, subf.a, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile, false,
                                    new HashSet<String>(), false).get(0).getGraphicalData();
                }
                if (subf.equals(linkedDatFile.getDrawChainTail()))
                    linkedDatFile.setDrawChainTail(colouredSubfile);

                oldBefore.setNext(colouredSubfile);
                colouredSubfile.setNext(oldNext);
                Integer oldNumber = drawPerLine.getKey(subf);
                if (oldNumber != null)
                    drawPerLine.put(oldNumber, colouredSubfile);
                newSubfiles.add((GData1) colouredSubfile);
            }
            selectedSubfiles.clear();
            selectedSubfiles.addAll(newSubfiles);

            for (GData1 subf : selectedSubfiles) {
                Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
                for (VertexInfo vertexInfo : vis) {
                    selectedVertices.add(vertexInfo.getVertex());
                    GData gt = vertexInfo.getLinkedData();
                    switch (gt.type()) {
                    case 2:
                        selectedLines.add((GData2) gt);
                        break;
                    case 3:
                        selectedTriangles.add((GData3) gt);
                        break;
                    case 4:
                        selectedQuads.add((GData4) gt);
                        break;
                    case 5:
                        selectedCondlines.add((GData5) gt);
                        break;
                    default:
                        break;
                    }
                }
            }
            setModified_NoSync();
        }

        if (isModified()) {
            selectedLines.addAll(subSelectedLines);
            selectedTriangles.addAll(subSelectedTriangles);
            selectedQuads.addAll(subSelectedQuads);
            selectedCondlines.addAll(subSelectedCondlines);
            selectedData.addAll(selectedLines);
            selectedData.addAll(selectedTriangles);
            selectedData.addAll(selectedQuads);
            selectedData.addAll(selectedCondlines);
            selectedData.addAll(selectedSubfiles);

            syncWithTextEditors(true);
            updateUnsavedStatus();
        }

    }

    public void updateUnsavedStatus() {
        String newText = linkedDatFile.getText();
        linkedDatFile.setText(newText);
        if (newText.equals(linkedDatFile.getOriginalText()) && linkedDatFile.getOldName().equals(linkedDatFile.getNewName())) {
            // Do not remove virtual files from the unsaved file list
            // (they are virtual, because they were not saved at all!)
            if (Project.getUnsavedFiles().contains(linkedDatFile) && !linkedDatFile.isVirtual()) {
                Project.removeUnsavedFile(linkedDatFile);
                Editor3DWindow.getWindow().updateTree_unsavedEntries();
            }
        } else if (!Project.getUnsavedFiles().contains(linkedDatFile)) {
            Project.addUnsavedFile(linkedDatFile);
            Editor3DWindow.getWindow().updateTree_unsavedEntries();
        }
    }

    public Set<Vertex> getVertices() {
        return vertexLinkedToPositionInFile.keySet();
    }

    public Vector4f getSelectionCenter() {

        final Set<Vertex> objectVertices = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());
        objectVertices.addAll(selectedVertices);

        // 1. Object Based Selection

        for (GData2 line : selectedLines) {
            Vertex[] verts = lines.get(line);
            if (verts == null)
                continue;
            for (Vertex vertex : verts) {
                objectVertices.add(vertex);
            }
        }
        for (GData3 triangle : selectedTriangles) {
            Vertex[] verts = triangles.get(triangle);
            if (verts == null)
                continue;
            for (Vertex vertex : verts) {
                objectVertices.add(vertex);
            }
        }
        for (GData4 quad : selectedQuads) {
            Vertex[] verts = quads.get(quad);
            if (verts == null)
                continue;
            for (Vertex vertex : verts) {
                objectVertices.add(vertex);
            }
        }
        for (GData5 condline : selectedCondlines) {
            Vertex[] verts = condlines.get(condline);
            if (verts == null)
                continue;
            for (Vertex vertex : verts) {
                objectVertices.add(vertex);
            }
        }

        // 2. Subfile Based Selection
        if (!selectedSubfiles.isEmpty()) {

            for (GData1 subf : selectedSubfiles) {
                Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
                for (VertexInfo vertexInfo : vis) {
                    objectVertices.add(vertexInfo.getVertex());
                }
            }
        }
        if (!objectVertices.isEmpty()) {
            float x = 0f;
            float y = 0f;
            float z = 0f;
            for (Vertex vertex : objectVertices) {
                x = x + vertex.x;
                y = y + vertex.y;
                z = z + vertex.z;
            }
            float count = objectVertices.size();
            return new Vector4f(x / count, y / count, z / count, 1f);
        } else {
            return new Vector4f(0f, 0f, 0f, 1f);
        }
    }

    public Vertex getMinimalDistanceVertexToSurfaces(Vertex vertex) {
        double minDist = Double.MAX_VALUE;
        Vector4f vp = vertex.toVector4f();
        Vertex targetVertex = new Vertex(vp);
        Set<GData3> ts;
        Set<GData4> qs;
        if (selectedTriangles.isEmpty()) {
            if (selectedQuads.isEmpty()) {
                ts = triangles.keySet();
                qs = quads.keySet();
            } else {
                ts = triangles.keySet();
                qs = selectedQuads;
            }
        } else {
            if (selectedQuads.isEmpty()) {
                ts = selectedTriangles;
                qs = quads.keySet();
            } else {
                ts = selectedTriangles;
                qs = selectedQuads;
            }
        }

        for (GData3 g3 : ts) {
            Vertex[] verts = triangles.get(g3);
            Vector4f v1 = MathHelper
                    .getNearestPointToTriangle(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
            if (v1 == null)
                continue;
            float d1 = Vector4f.sub(v1, vp, null).length();
            if (d1 < minDist) {
                targetVertex = new Vertex(v1);
                minDist = d1;
            }
        }

        for (GData4 g4 : qs) {
            Vertex[] verts = quads.get(g4);
            Vector4f v1 = MathHelper
                    .getNearestPointToTriangle(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
            if (v1 == null)
                continue;
            float d1 = Vector4f.sub(v1, vp, null).length();
            if (d1 < minDist) {
                targetVertex = new Vertex(v1);
                minDist = d1;
            }
            Vector4f v2 = MathHelper
                    .getNearestPointToTriangle(verts[2].x, verts[2].y, verts[2].z, verts[3].x, verts[3].y, verts[3].z, verts[0].x, verts[0].y, verts[0].z, vertex.x, vertex.y, vertex.z);
            if (v2 == null)
                continue;
            float d2 = Vector4f.sub(v2, vp, null).length();
            if (d2 < minDist) {
                targetVertex = new Vertex(v2);
                minDist = d2;
            }
        }

        if (minDist == Double.MAX_VALUE)
            return new Vertex(vp);
        return targetVertex;
    }

    public Vertex getMinimalDistanceVertexToLines(Vertex vertex) {
        double minDist = Double.MAX_VALUE;
        Vector4f vp = vertex.toVector4f();
        Vertex targetVertex = new Vertex(vp);
        Set<GData3> ts;
        Set<GData4> qs;

        Set<GData2> ls;
        Set<GData5> cs;
        if (selectedTriangles.isEmpty()) {
            if (selectedQuads.isEmpty()) {
                ts = triangles.keySet();
                qs = quads.keySet();
            } else {
                ts = triangles.keySet();
                qs = selectedQuads;
            }
        } else {
            if (selectedQuads.isEmpty()) {
                ts = selectedTriangles;
                qs = quads.keySet();
            } else {
                ts = selectedTriangles;
                qs = selectedQuads;
            }
        }

        if (selectedLines.isEmpty()) {
            if (selectedCondlines.isEmpty()) {
                ls = lines.keySet();
                cs = condlines.keySet();
            } else {
                ls = lines.keySet();
                cs = selectedCondlines;
            }
        } else {
            if (selectedCondlines.isEmpty()) {
                ls = selectedLines;
                cs = condlines.keySet();
            } else {
                ls = selectedLines;
                cs = selectedCondlines;
            }
        }

        for (GData2 g2 : ls) {
            Vertex[] verts = lines.get(g2);
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
        }

        for (GData3 g3 : ts) {
            Vertex[] verts = triangles.get(g3);
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[1].x, verts[1].y, verts[1].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
        }

        for (GData4 g4 : qs) {
            Vertex[] verts = quads.get(g4);
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[1].x, verts[1].y, verts[1].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[2].x, verts[2].y, verts[2].z, verts[3].x, verts[3].y, verts[3].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[3].x, verts[3].y, verts[3].z, verts[0].x, verts[0].y, verts[0].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
        }

        for (GData5 g5 : cs) {
            Vertex[] verts = condlines.get(g5);
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[3].x, verts[3].y, verts[3].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
        }

        if (minDist == Double.MAX_VALUE)
            return new Vertex(vp);
        return targetVertex;
    }

    public Vertex[] getMinimalDistanceVerticesToLines(Vertex vertex) {
        double minDist = Double.MAX_VALUE;
        Vector4f vp = vertex.toVector4f();
        Vertex[] result = new Vertex[3];
        Vector4f result2 = new Vector4f();
        Set<GData3> ts;
        Set<GData4> qs;

        Set<GData2> ls;
        Set<GData5> cs;
        if (selectedTriangles.isEmpty()) {
            if (selectedQuads.isEmpty()) {
                ts = triangles.keySet();
                qs = quads.keySet();
            } else {
                ts = triangles.keySet();
                qs = selectedQuads;
            }
        } else {
            if (selectedQuads.isEmpty()) {
                ts = selectedTriangles;
                qs = quads.keySet();
            } else {
                ts = selectedTriangles;
                qs = selectedQuads;
            }
        }

        if (selectedLines.isEmpty()) {
            if (selectedCondlines.isEmpty()) {
                ls = lines.keySet();
                cs = condlines.keySet();
            } else {
                ls = lines.keySet();
                cs = selectedCondlines;
            }
        } else {
            if (selectedCondlines.isEmpty()) {
                ls = selectedLines;
                cs = condlines.keySet();
            } else {
                ls = selectedLines;
                cs = selectedCondlines;
            }
        }

        for (GData2 g2 : ls) {
            Vertex[] verts = lines.get(g2);
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    result[0] = verts[0];
                    result[1] = verts[1];
                    result2 = v1;
                    minDist = d1;
                }
            }
        }

        for (GData3 g3 : ts) {
            Vertex[] verts = triangles.get(g3);
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    result[0] = verts[0];
                    result[1] = verts[1];
                    result2 = v1;
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[1].x, verts[1].y, verts[1].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    result[0] = verts[1];
                    result[1] = verts[2];
                    result2 = v1;
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    result[0] = verts[0];
                    result[1] = verts[2];
                    result2 = v1;
                    minDist = d1;
                }
            }
        }

        for (GData4 g4 : qs) {
            Vertex[] verts = quads.get(g4);
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    result[0] = verts[0];
                    result[1] = verts[1];
                    result2 = v1;
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[1].x, verts[1].y, verts[1].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    result[0] = verts[1];
                    result[1] = verts[2];
                    result2 = v1;
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[2].x, verts[2].y, verts[2].z, verts[3].x, verts[3].y, verts[3].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    result[0] = verts[2];
                    result[1] = verts[3];
                    result2 = v1;
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[3].x, verts[3].y, verts[3].z, verts[0].x, verts[0].y, verts[0].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    result[0] = verts[3];
                    result[1] = verts[0];
                    result2 = v1;
                    minDist = d1;
                }
            }
        }

        for (GData5 g5 : cs) {
            Vertex[] verts = condlines.get(g5);
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    result[0] = verts[0];
                    result[1] = verts[1];
                    result2 = v1;
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    result[0] = verts[0];
                    result[1] = verts[2];
                    result2 = v1;
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[3].x, verts[3].y, verts[3].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    result[0] = verts[0];
                    result[1] = verts[3];
                    result2 = v1;
                    minDist = d1;
                }
            }
        }

        if (minDist == Double.MAX_VALUE) {
            result[0] = new Vertex(vertex.X, vertex.Y, vertex.Z);
            result[1] = new Vertex(vertex.X, vertex.Y, vertex.Z);
            result[2] = new Vertex(vertex.X, vertex.Y, vertex.Z);
            return result;
        }
        result[0] = new Vertex(result[0].X, result[0].Y, result[0].Z);
        result[1] = new Vertex(result[1].X, result[1].Y, result[1].Z);
        result[2] = new Vertex(result2.x, result2.y, result2.z);
        return result;
    }

    public Vertex[] getMinimalDistanceVerticesToTriangleEdges(Vertex vertex, Vertex vertexA, Vertex vertexB, Vertex vertexC, Composite3D c3d) {

        Vector4f triA = new Vector4f(vertexA.toVector4f());
        Vector4f triB = new Vector4f(vertexB.toVector4f());
        Vector4f triC = new Vector4f(vertexC.toVector4f());
        final Matrix4f vport = c3d.getViewport();
        Matrix4f.transform(vport, triA, triA);
        Matrix4f.transform(vport, triB, triB);
        Matrix4f.transform(vport, triC, triC);

        double minDist = Double.MAX_VALUE;
        Vertex[] result = new Vertex[2];
        {
            Vector4f v1 = MathHelper.getNearestPointToLineSegment(vertexA.x, vertexA.y, vertexA.z, vertexB.x, vertexB.y, vertexB.z, vertex.x, vertex.y, vertex.z);
            float d1 = Vector4f.sub(v1, vertex.toVector4f(), null).length();
            if (d1 < minDist) {
                Vector4f vert = vertex.toVector4f();
                Matrix4f.transform(vport, vert, vert);
                Vector4f ip1 = MathHelper.intersectionBetweenTwoLinesStrict2D(vert, triA, triA, triC);
                if (ip1 == null) {
                    Vector4f ip2 = MathHelper.intersectionBetweenTwoLinesStrict2D(vert, triA, triB, triC);
                    if (ip2 == null) {
                        Vector4f ip3 = MathHelper.intersectionBetweenTwoLinesStrict2D(vert, triB, triA, triC);
                        if (ip3 == null) {
                            Vector4f ip4 = MathHelper.intersectionBetweenTwoLinesStrict2D(vert, triB, triB, triC);
                            if (ip4 == null) {
                                result[0] = vertexA;
                                result[1] = vertexB;
                                minDist = d1;
                            }
                        }
                    }
                }
            }
        }
        {
            Vector4f v1 = MathHelper.getNearestPointToLineSegment(vertexB.x, vertexB.y, vertexB.z, vertexC.x, vertexC.y, vertexC.z, vertex.x, vertex.y, vertex.z);
            float d1 = Vector4f.sub(v1, vertex.toVector4f(), null).length();
            if (d1 < minDist) {
                Vector4f vert = vertex.toVector4f();
                Matrix4f.transform(vport, vert, vert);
                Vector4f ip1 = MathHelper.intersectionBetweenTwoLinesStrict2D(vert, triC, triA, triB);
                if (ip1 == null) {
                    Vector4f ip2 = MathHelper.intersectionBetweenTwoLinesStrict2D(vert, triC, triA, triC);
                    if (ip2 == null) {
                        Vector4f ip3 = MathHelper.intersectionBetweenTwoLinesStrict2D(vert, triB, triA, triB);
                        if (ip3 == null) {
                            Vector4f ip4 = MathHelper.intersectionBetweenTwoLinesStrict2D(vert, triB, triA, triC);
                            if (ip4 == null) {
                                result[0] = vertexB;
                                result[1] = vertexC;
                                minDist = d1;
                            }
                        }
                    }
                }
            }
        }
        {
            Vector4f v1 = MathHelper.getNearestPointToLineSegment(vertexA.x, vertexA.y, vertexA.z, vertexC.x, vertexC.y, vertexC.z, vertex.x, vertex.y, vertex.z);
            float d1 = Vector4f.sub(v1, vertex.toVector4f(), null).length();
            if (d1 < minDist) {
                Vector4f vert = new Vector4f(vertex.toVector4f());
                Matrix4f.transform(vport, vert, vert);
                Vector4f ip1 = MathHelper.intersectionBetweenTwoLinesStrict2D(vert, triC, triA, triB);
                if (ip1 == null) {
                    Vector4f ip2 = MathHelper.intersectionBetweenTwoLinesStrict2D(vert, triC, triB, triC);
                    if (ip2 == null) {
                        Vector4f ip3 = MathHelper.intersectionBetweenTwoLinesStrict2D(vert, triA, triA, triB);
                        if (ip3 == null) {
                            Vector4f ip4 = MathHelper.intersectionBetweenTwoLinesStrict2D(vert, triA, triB, triC);
                            if (ip4 == null) {
                                result[0] = vertexA;
                                result[1] = vertexC;
                                minDist = d1;
                            }
                        }
                    }
                }
            }
        }
        if (minDist == Double.MAX_VALUE) {
            result[0] = new Vertex(vertex.X, vertex.Y, vertex.Z);
            result[1] = new Vertex(vertex.X, vertex.Y, vertex.Z);
            return result;
        }
        return result;
    }

    public Vertex getMinimalDistanceVertexToLineEnd(Vertex vertex) {
        double minDist = Double.MAX_VALUE;
        Vector4f vp = vertex.toVector4f();
        Vertex targetVertex = new Vertex(vp);
        Set<GData3> ts;
        Set<GData4> qs;

        Set<GData2> ls;
        Set<GData5> cs;
        if (selectedTriangles.isEmpty()) {
            if (selectedQuads.isEmpty()) {
                ts = triangles.keySet();
                qs = quads.keySet();
            } else {
                ts = triangles.keySet();
                qs = selectedQuads;
            }
        } else {
            if (selectedQuads.isEmpty()) {
                ts = selectedTriangles;
                qs = quads.keySet();
            } else {
                ts = selectedTriangles;
                qs = selectedQuads;
            }
        }

        if (selectedLines.isEmpty()) {
            if (selectedCondlines.isEmpty()) {
                ls = lines.keySet();
                cs = condlines.keySet();
            } else {
                ls = lines.keySet();
                cs = selectedCondlines;
            }
        } else {
            if (selectedCondlines.isEmpty()) {
                ls = selectedLines;
                cs = condlines.keySet();
            } else {
                ls = selectedLines;
                cs = selectedCondlines;
            }
        }

        for (GData2 g2 : ls) {
            Vertex[] verts = lines.get(g2);
            {
                Vector4f v1 = MathHelper.getNearestPointToLinePoints(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
        }

        for (GData3 g3 : ts) {
            Vertex[] verts = triangles.get(g3);
            {
                Vector4f v1 = MathHelper.getNearestPointToLinePoints(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLinePoints(verts[1].x, verts[1].y, verts[1].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLinePoints(verts[0].x, verts[0].y, verts[0].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
        }

        for (GData4 g4 : qs) {
            Vertex[] verts = quads.get(g4);
            {
                Vector4f v1 = MathHelper.getNearestPointToLinePoints(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLinePoints(verts[1].x, verts[1].y, verts[1].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLinePoints(verts[2].x, verts[2].y, verts[2].z, verts[3].x, verts[3].y, verts[3].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLinePoints(verts[3].x, verts[3].y, verts[3].z, verts[0].x, verts[0].y, verts[0].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
        }

        for (GData5 g5 : cs) {
            Vertex[] verts = condlines.get(g5);
            {
                Vector4f v1 = MathHelper.getNearestPointToLinePoints(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLinePoints(verts[0].x, verts[0].y, verts[0].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLinePoints(verts[0].x, verts[0].y, verts[0].z, verts[3].x, verts[3].y, verts[3].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
        }

        if (minDist == Double.MAX_VALUE)
            return new Vertex(vp);
        return targetVertex;
    }

    public Vector4f getVertexNormal(Vertex min) {
        Vector4f result = new Vector4f(0f, 0f, 0f, 0f);
        Set<VertexManifestation> linked = vertexLinkedToPositionInFile.get(min);

        for (VertexManifestation m : linked) {
            GData g = m.getGdata();
            Vector3f n;
            switch (g.type()) {
            case 3:
                GData3 g3 = (GData3) g;
                n = new Vector3f(g3.xn, g3.yn, g3.zn);
                if (n.lengthSquared() != 0) {
                    n.normalise();
                    result.set(n.x + result.x, n.y + result.y, n.z + result.z);
                }
                break;
            case 4:
                GData4 g4 = (GData4) g;
                n = new Vector3f(g4.xn, g4.yn, g4.zn);
                if (n.lengthSquared() != 0) {
                    n.normalise();
                    result.set(n.x + result.x, n.y + result.y, n.z + result.z);
                }
                break;
            }
        }
        if (result.lengthSquared() == 0)
            return new Vector4f(0f, 0f, 1f, 1f);
        result.normalise();
        result.setW(1f);
        return result;
    }

    public Vector4f getMinimalDistanceEdgeNormal(Vertex vertex) {
        Vector4f result = new Vector4f(0f, 0f, 0f, 0f);

        double minDist = Double.MAX_VALUE;
        Vector4f vp = vertex.toVector4f();
        Vertex p1 = new Vertex(vp);
        Vertex p2 = new Vertex(vp);
        Set<GData3> ts;
        Set<GData4> qs;

        Set<GData2> ls;
        Set<GData5> cs;
        if (selectedTriangles.isEmpty()) {
            if (selectedQuads.isEmpty()) {
                ts = triangles.keySet();
                qs = quads.keySet();
            } else {
                ts = triangles.keySet();
                qs = selectedQuads;
            }
        } else {
            if (selectedQuads.isEmpty()) {
                ts = selectedTriangles;
                qs = quads.keySet();
            } else {
                ts = selectedTriangles;
                qs = selectedQuads;
            }
        }

        if (selectedLines.isEmpty()) {
            if (selectedCondlines.isEmpty()) {
                ls = lines.keySet();
                cs = condlines.keySet();
            } else {
                ls = lines.keySet();
                cs = selectedCondlines;
            }
        } else {
            if (selectedCondlines.isEmpty()) {
                ls = selectedLines;
                cs = condlines.keySet();
            } else {
                ls = selectedLines;
                cs = selectedCondlines;
            }
        }

        for (GData2 g2 : ls) {
            Vertex[] verts = lines.get(g2);
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    p1 = verts[0];
                    p2 = verts[1];
                    minDist = d1;
                }
            }
        }

        for (GData3 g3 : ts) {
            Vertex[] verts = triangles.get(g3);
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    p1 = verts[0];
                    p2 = verts[1];
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[1].x, verts[1].y, verts[1].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    p1 = verts[1];
                    p2 = verts[2];
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    p1 = verts[0];
                    p2 = verts[2];
                    minDist = d1;
                }
            }
        }

        for (GData4 g4 : qs) {
            Vertex[] verts = quads.get(g4);
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    p1 = verts[0];
                    p2 = verts[1];
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[1].x, verts[1].y, verts[1].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    p1 = verts[1];
                    p2 = verts[2];
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[2].x, verts[2].y, verts[2].z, verts[3].x, verts[3].y, verts[3].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    p1 = verts[2];
                    p2 = verts[3];
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[3].x, verts[3].y, verts[3].z, verts[0].x, verts[0].y, verts[0].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    p1 = verts[3];
                    p2 = verts[0];
                    minDist = d1;
                }
            }
        }

        for (GData5 g5 : cs) {
            Vertex[] verts = condlines.get(g5);
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    p1 = verts[0];
                    p2 = verts[1];
                    minDist = d1;
                }
            }
        }

        HashSet<GData> commonLinkedGData = new HashSet<GData>();
        {
            Set<VertexManifestation> linked = vertexLinkedToPositionInFile.get(p1);
            Set<VertexManifestation> linked2 = vertexLinkedToPositionInFile.get(p2);
            HashSet<GData> l2 = new HashSet<GData>(linked2.size());
            for (VertexManifestation vm : linked) {
                commonLinkedGData.add(vm.getGdata());
            }
            for (VertexManifestation vm : linked2) {
                l2.add(vm.getGdata());
            }
            commonLinkedGData.retainAll(l2);
        }

        for (GData g : commonLinkedGData) {
            Vector3f n;
            switch (g.type()) {
            case 3:
                GData3 g3 = (GData3) g;
                n = new Vector3f(g3.xn, g3.yn, g3.zn);
                if (n.lengthSquared() != 0) {
                    n.normalise();
                    result.set(n.x + result.x, n.y + result.y, n.z + result.z);
                }
                break;
            case 4:
                GData4 g4 = (GData4) g;
                n = new Vector3f(g4.xn, g4.yn, g4.zn);
                if (n.lengthSquared() != 0) {
                    n.normalise();
                    result.set(n.x + result.x, n.y + result.y, n.z + result.z);
                }
                break;
            }
        }
        if (result.lengthSquared() == 0)
            return new Vector4f(0f, 0f, 1f, 1f);
        result.normalise();
        result.setW(1f);
        return result;
    }

    public Vector4f getMinimalDistanceSurfaceNormal(Vertex vertex) {
        double minDist = Double.MAX_VALUE;
        Vector4f result = new Vector4f(0f, 0f, 0f, 0f);
        Vector4f vp = vertex.toVector4f();
        Set<GData3> ts;
        Set<GData4> qs;
        if (selectedTriangles.isEmpty()) {
            if (selectedQuads.isEmpty()) {
                ts = triangles.keySet();
                qs = quads.keySet();
            } else {
                ts = triangles.keySet();
                qs = selectedQuads;
            }
        } else {
            if (selectedQuads.isEmpty()) {
                ts = selectedTriangles;
                qs = quads.keySet();
            } else {
                ts = selectedTriangles;
                qs = selectedQuads;
            }
        }

        for (GData3 g3 : ts) {
            Vertex[] verts = triangles.get(g3);
            Vector4f v1 = MathHelper
                    .getNearestPointToTriangle(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
            if (v1 == null)
                continue;
            float d1 = Vector4f.sub(v1, vp, null).length();
            if (d1 < minDist) {
                result.set(g3.xn, g3.yn, g3.zn);
                minDist = d1;
            }
        }

        for (GData4 g4 : qs) {
            Vertex[] verts = quads.get(g4);
            Vector4f v1 = MathHelper
                    .getNearestPointToTriangle(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
            if (v1 == null)
                continue;
            float d1 = Vector4f.sub(v1, vp, null).length();
            if (d1 < minDist) {
                result.set(g4.xn, g4.yn, g4.zn);
                minDist = d1;
            }
            Vector4f v2 = MathHelper
                    .getNearestPointToTriangle(verts[2].x, verts[2].y, verts[2].z, verts[3].x, verts[3].y, verts[3].z, verts[0].x, verts[0].y, verts[0].z, vertex.x, vertex.y, vertex.z);
            if (v2 == null)
                continue;
            float d2 = Vector4f.sub(v2, vp, null).length();
            if (d2 < minDist) {
                result.set(g4.xn, g4.yn, g4.zn);
                minDist = d2;
            }
        }

        if (result.lengthSquared() == 0)
            return new Vector4f(0f, 0f, 1f, 1f);

        result.normalise();
        result.setW(1f);
        return result;
    }

    public void delete(boolean moveAdjacentData, boolean setModified) {
        if (linkedDatFile.isReadOnly())
            return;

        if (selectedBgPicture != null && linkedDatFile.getDrawPerLine_NOCLONE().containsValue(selectedBgPicture)) {
            GData before = selectedBgPicture.getBefore();
            GData next = selectedBgPicture.getNext();
            linkedDatFile.getDrawPerLine_NOCLONE().removeByValue(selectedBgPicture);
            before.setNext(next);
            remove(selectedBgPicture);
            selectedBgPicture = null;
            setModified_NoSync();
        }

        final Set<Vertex> singleVertices = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());

        final HashSet<GData0> effSelectedVertices = new HashSet<GData0>();
        final HashSet<GData2> effSelectedLines = new HashSet<GData2>();
        final HashSet<GData3> effSelectedTriangles = new HashSet<GData3>();
        final HashSet<GData4> effSelectedQuads = new HashSet<GData4>();
        final HashSet<GData5> effSelectedCondlines = new HashSet<GData5>();

        selectedData.clear();

        // 0. Deselect selected subfile data (for whole selected subfiles)
        for (GData1 subf : selectedSubfiles) {
            Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
            for (VertexInfo vertexInfo : vis) {
                if (!moveAdjacentData)
                    selectedVertices.remove(vertexInfo.getVertex());
                GData g = vertexInfo.getLinkedData();
                switch (g.type()) {
                case 2:
                    selectedLines.remove(g);
                    break;
                case 3:
                    selectedTriangles.remove(g);
                    break;
                case 4:
                    selectedQuads.remove(g);
                    break;
                case 5:
                    selectedCondlines.remove(g);
                    break;
                default:
                    break;
                }
            }
        }

        // 1. Vertex Based Selection
        {
            final Set<Vertex> objectVertices = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());
            {
                HashMap<GData, Integer> occurMap = new HashMap<GData, Integer>();
                for (Vertex vertex : selectedVertices) {
                    Set<VertexManifestation> occurences = vertexLinkedToPositionInFile.get(vertex);
                    if (occurences == null)
                        continue;
                    boolean isPureSubfileVertex = true;
                    for (VertexManifestation vm : occurences) {
                        GData g = vm.getGdata();
                        int val = 1;
                        if (occurMap.containsKey(g)) {
                            val = occurMap.get(g);
                            val++;
                        }
                        occurMap.put(g, val);
                        switch (g.type()) {
                        case 0:
                            GData0 meta = (GData0) g;
                            boolean idCheck = !lineLinkedToVertices.containsKey(meta);
                            isPureSubfileVertex = isPureSubfileVertex && idCheck;
                            if (moveAdjacentData || val == 1) {
                                if (!idCheck) {
                                    effSelectedVertices.add(meta);
                                }
                            }
                            break;
                        case 2:
                            GData2 line = (GData2) g;
                            idCheck = !line.parent.equals(View.DUMMY_REFERENCE);
                            isPureSubfileVertex = isPureSubfileVertex && idCheck;
                            if (moveAdjacentData || val == 2) {
                                if (!idCheck) {
                                    selectedLines.add(line);
                                }
                            }
                            break;
                        case 3:
                            GData3 triangle = (GData3) g;
                            idCheck = !triangle.parent.equals(View.DUMMY_REFERENCE);
                            isPureSubfileVertex = isPureSubfileVertex && idCheck;
                            if (moveAdjacentData || val == 3) {
                                if (!idCheck) {
                                    selectedTriangles.add(triangle);
                                }
                            }
                            break;
                        case 4:
                            GData4 quad = (GData4) g;
                            idCheck = !quad.parent.equals(View.DUMMY_REFERENCE);
                            isPureSubfileVertex = isPureSubfileVertex && idCheck;
                            if (moveAdjacentData || val == 4) {
                                if (!idCheck) {
                                    selectedQuads.add(quad);
                                }
                            }
                            break;
                        case 5:
                            GData5 condline = (GData5) g;
                            idCheck = !condline.parent.equals(View.DUMMY_REFERENCE);
                            isPureSubfileVertex = isPureSubfileVertex && idCheck;
                            if (moveAdjacentData || val == 4) {
                                if (!idCheck) {
                                    selectedCondlines.add(condline);
                                }
                            }
                            break;
                        }
                    }
                    if (isPureSubfileVertex)
                        objectVertices.add(vertex);
                }
            }

            // 2. Object Based Selection

            for (GData2 line : selectedLines) {
                if (line.parent.equals(View.DUMMY_REFERENCE))
                    effSelectedLines.add(line);
                Vertex[] verts = lines.get(line);
                if (verts == null)
                    continue;
                for (Vertex vertex : verts) {
                    objectVertices.add(vertex);
                }
            }
            for (GData3 triangle : selectedTriangles) {
                if (triangle.parent.equals(View.DUMMY_REFERENCE))
                    effSelectedTriangles.add(triangle);
                Vertex[] verts = triangles.get(triangle);
                if (verts == null)
                    continue;
                for (Vertex vertex : verts) {
                    objectVertices.add(vertex);
                }
            }
            for (GData4 quad : selectedQuads) {
                if (quad.parent.equals(View.DUMMY_REFERENCE))
                    effSelectedQuads.add(quad);
                Vertex[] verts = quads.get(quad);
                if (verts == null)
                    continue;
                for (Vertex vertex : verts) {
                    objectVertices.add(vertex);
                }
            }
            for (GData5 condline : selectedCondlines) {
                if (condline.parent.equals(View.DUMMY_REFERENCE))
                    effSelectedCondlines.add(condline);
                Vertex[] verts = condlines.get(condline);
                if (verts == null)
                    continue;
                for (Vertex vertex : verts) {
                    objectVertices.add(vertex);
                }
            }

            if (moveAdjacentData) {
                singleVertices.addAll(selectedVertices);
                singleVertices.removeAll(objectVertices);
            }

            // 3. Deletion of the selected data (no whole subfiles!!)

            if (!effSelectedLines.isEmpty())
                setModified_NoSync();
            if (!effSelectedTriangles.isEmpty())
                setModified_NoSync();
            if (!effSelectedQuads.isEmpty())
                setModified_NoSync();
            if (!effSelectedCondlines.isEmpty())
                setModified_NoSync();
            final HashBiMap<Integer, GData> dpl = linkedDatFile.getDrawPerLine_NOCLONE();
            for (GData2 gd : effSelectedLines) {
                dpl.removeByValue(gd);
                gd.getBefore().setNext(gd.getNext());
                remove(gd);
            }
            for (GData3 gd : effSelectedTriangles) {
                dpl.removeByValue(gd);
                gd.getBefore().setNext(gd.getNext());
                remove(gd);
            }
            for (GData4 gd : effSelectedQuads) {
                dpl.removeByValue(gd);
                gd.getBefore().setNext(gd.getNext());
                remove(gd);
            }
            for (GData5 gd : effSelectedCondlines) {
                dpl.removeByValue(gd);
                gd.getBefore().setNext(gd.getNext());
                remove(gd);
            }
            for (Vertex v : singleVertices) {
                if (vertexLinkedToPositionInFile.containsKey(v)) {
                    Set<VertexManifestation> tmp = vertexLinkedToPositionInFile.get(v);
                    if (tmp == null)
                        continue;
                    Set<VertexManifestation> occurences = new HashSet<VertexManifestation>(tmp);
                    for (VertexManifestation vm : occurences) {
                        GData g = vm.getGdata();
                        if (lineLinkedToVertices.containsKey(g)) {
                            dpl.removeByValue(g);
                            g.getBefore().setNext(g.getNext());
                            remove(g);
                            setModified_NoSync();
                        }
                    }
                }
            }

            selectedVertices.clear();
            selectedLines.clear();
            selectedTriangles.clear();
            selectedQuads.clear();
            selectedCondlines.clear();

            // 4. Subfile Based Deletion
            if (!selectedSubfiles.isEmpty()) {
                for (GData1 gd : selectedSubfiles) {

                    // Remove a BFC INVERTNEXT if it is present
                    boolean hasInvertnext = false;
                    GData invertNextData = gd.getBefore();
                    while (invertNextData != null && invertNextData.type() != 1 && (invertNextData.type() != 6 || ((GDataBFC) invertNextData).type != BFC.INVERTNEXT)) {
                        invertNextData = invertNextData.getBefore();
                    }
                    if (invertNextData != null && invertNextData.type() == 6) {
                        hasInvertnext = true;
                    }
                    if (hasInvertnext) {
                        // Remove Invert Next
                        GDataBFC gbfc = (GDataBFC) invertNextData;
                        dpl.removeByValue(gbfc);
                        gbfc.getBefore().setNext(gbfc.getNext());
                        remove(gbfc);
                    }

                    dpl.removeByValue(gd);
                    gd.getBefore().setNext(gd.getNext());
                    remove(gd);
                }
                selectedSubfiles.clear();
                setModified_NoSync();
            }

            if (isModified()) {

                // Update Draw per line

                TreeSet<Integer> ts = new TreeSet<Integer>();
                ts.addAll(dpl.keySet());

                int counter = 1;
                GData tail = null;
                for (Integer k : ts) {
                    GData gdata = dpl.getValue(k);
                    dpl.removeByKey(k);
                    dpl.put(counter, gdata);
                    counter++;
                    tail = gdata;
                }
                if (tail != null) {
                    linkedDatFile.setDrawChainTail(tail);
                } else {
                    GData0 blankLine = new GData0(""); //$NON-NLS-1$
                    linkedDatFile.getDrawChainStart().setNext(blankLine);
                    dpl.put(1, blankLine);
                    linkedDatFile.setDrawChainTail(blankLine);
                }

                if (setModified) syncWithTextEditors(true);
                updateUnsavedStatus();
            }
        }
    }

    public void copy() {

        // Has to copy what IS selected, nothing more, nothing less.

        CLIPBOARD.clear();
        CLIPBOARD_InvNext.clear();

        final Set<Vertex> singleVertices = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());

        final HashSet<GData2> effSelectedLines = new HashSet<GData2>();
        final HashSet<GData3> effSelectedTriangles = new HashSet<GData3>();
        final HashSet<GData4> effSelectedQuads = new HashSet<GData4>();
        final HashSet<GData5> effSelectedCondlines = new HashSet<GData5>();

        final TreeSet<Vertex> effSelectedVertices2 = new TreeSet<Vertex>(selectedVertices);
        final HashSet<GData2> effSelectedLines2 = new HashSet<GData2>(selectedLines);
        final HashSet<GData3> effSelectedTriangles2 = new HashSet<GData3>(selectedTriangles);
        final HashSet<GData4> effSelectedQuads2 = new HashSet<GData4>(selectedQuads);
        final HashSet<GData5> effSelectedCondlines2 = new HashSet<GData5>(selectedCondlines);

        selectedData.clear();

        {
            final Set<Vertex> objectVertices = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());
            // 0. Deselect selected subfile data I (for whole selected subfiles)
            for (GData1 subf : selectedSubfiles) {
                Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
                for (VertexInfo vertexInfo : vis) {
                    selectedVertices.remove(vertexInfo.getVertex());
                    GData g = vertexInfo.getLinkedData();
                    switch (g.type()) {
                    case 2:
                        selectedLines.remove(g);
                        {
                            Vertex[] verts = lines.get(g);
                            if (verts == null)
                                continue;
                            for (Vertex vertex : verts) {
                                objectVertices.add(vertex);
                            }
                        }
                        break;
                    case 3:
                        selectedTriangles.remove(g);
                        {
                            Vertex[] verts = triangles.get(g);
                            if (verts == null)
                                continue;
                            for (Vertex vertex : verts) {
                                objectVertices.add(vertex);
                            }
                        }
                        break;
                    case 4:
                        selectedQuads.remove(g);
                        {
                            Vertex[] verts = quads.get(g);
                            if (verts == null)
                                continue;
                            for (Vertex vertex : verts) {
                                objectVertices.add(vertex);
                            }
                        }
                        break;
                    case 5:
                        selectedCondlines.remove(g);
                        {
                            Vertex[] verts = condlines.get(g);
                            if (verts == null)
                                continue;
                            for (Vertex vertex : verts) {
                                objectVertices.add(vertex);
                            }
                        }
                        break;
                    default:
                        break;
                    }
                }
            }
            // 1. Vertex Based Selection

            {
                HashMap<GData, Integer> occurMap = new HashMap<GData, Integer>();
                for (Vertex vertex : selectedVertices) {
                    Set<VertexManifestation> occurences = vertexLinkedToPositionInFile.get(vertex);
                    if (occurences == null)
                        continue;
                    for (VertexManifestation vm : occurences) {
                        GData g = vm.getGdata();
                        int val = 1;
                        int type = g.type();
                        if (occurMap.containsKey(g)) {
                            val = occurMap.get(g);
                            if (type != 5 || vm.getPosition() < 2) {
                                val++;
                                occurMap.put(g, val);
                            }
                        } else if (type != 5 || vm.getPosition() < 2) {
                            occurMap.put(g, val);
                        }
                        switch (type) {
                        case 2:
                            GData2 line = (GData2) g;
                            if (val == 2) {
                                selectedLines.add(line);
                            }
                            break;
                        case 3:
                            GData3 triangle = (GData3) g;
                            if (val == 3) {
                                selectedTriangles.add(triangle);
                            }
                            break;
                        case 4:
                            GData4 quad = (GData4) g;
                            if (val == 4) {
                                selectedQuads.add(quad);
                            }
                            break;
                        case 5:
                            GData5 condline = (GData5) g;
                            if (val == 2) {
                                selectedCondlines.add(condline);
                            }
                            break;
                        }
                    }
                }
            }

            // 2. Deselect selected subfile data II (for whole selected
            // subfiles, remove all from selection, which belongs to a
            // completely selected subfile)
            for (GData1 subf : selectedSubfiles) {
                Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
                for (VertexInfo vertexInfo : vis) {
                    GData g = vertexInfo.getLinkedData();
                    switch (g.type()) {
                    case 2:
                        selectedLines.remove(g);
                        {
                            Vertex[] verts = lines.get(g);
                            if (verts == null)
                                continue;
                            for (Vertex vertex : verts) {
                                objectVertices.add(vertex);
                            }
                        }
                        break;
                    case 3:
                        selectedTriangles.remove(g);
                        {
                            Vertex[] verts = triangles.get(g);
                            if (verts == null)
                                continue;
                            for (Vertex vertex : verts) {
                                objectVertices.add(vertex);
                            }
                        }
                        break;
                    case 4:
                        selectedQuads.remove(g);
                        {
                            Vertex[] verts = quads.get(g);
                            if (verts == null)
                                continue;
                            for (Vertex vertex : verts) {
                                objectVertices.add(vertex);
                            }
                        }
                        break;
                    case 5:
                        selectedCondlines.remove(g);
                        {
                            Vertex[] verts = condlines.get(g);
                            if (verts == null)
                                continue;
                            for (Vertex vertex : verts) {
                                objectVertices.add(vertex);
                            }
                        }
                        break;
                    default:
                        break;
                    }
                }
            }

            // 3. Object Based Selection

            for (GData2 line : selectedLines) {
                if (line.parent.equals(View.DUMMY_REFERENCE)) {
                    effSelectedLines.add(line);
                } else {
                    Vertex[] verts = lines.get(line);
                    if (verts == null)
                        continue;
                    effSelectedLines.add(new GData2(verts[0], verts[1], line.parent, new GColour(line.colourNumber, line.r, line.g, line.b, line.a)));
                }
                Vertex[] verts = lines.get(line);
                if (verts == null)
                    continue;
                for (Vertex vertex : verts) {
                    objectVertices.add(vertex);
                }
            }
            for (GData3 triangle : selectedTriangles) {
                if (triangle.parent.equals(View.DUMMY_REFERENCE)) {
                    effSelectedTriangles.add(triangle);
                } else {
                    Vertex[] verts = triangles.get(triangle);
                    if (verts == null)
                        continue;
                    effSelectedTriangles.add(new GData3(verts[0], verts[1], verts[2], triangle.parent, new GColour(triangle.colourNumber, triangle.r, triangle.g, triangle.b, triangle.a)));
                }
                Vertex[] verts = triangles.get(triangle);
                if (verts == null)
                    continue;
                for (Vertex vertex : verts) {
                    objectVertices.add(vertex);
                }
            }
            for (GData4 quad : selectedQuads) {
                if (quad.parent.equals(View.DUMMY_REFERENCE)) {
                    effSelectedQuads.add(quad);
                } else {
                    Vertex[] verts = quads.get(quad);
                    if (verts == null)
                        continue;
                    effSelectedQuads.add(new GData4(verts[0], verts[1], verts[2], verts[3], quad.parent, new GColour(quad.colourNumber, quad.r, quad.g, quad.b, quad.a)));
                }
                Vertex[] verts = quads.get(quad);
                if (verts == null)
                    continue;
                for (Vertex vertex : verts) {
                    objectVertices.add(vertex);
                }
            }
            for (GData5 condline : selectedCondlines) {
                if (condline.parent.equals(View.DUMMY_REFERENCE)) {
                    effSelectedCondlines.add(condline);
                } else {
                    Vertex[] verts = condlines.get(condline);
                    if (verts == null)
                        continue;
                    effSelectedCondlines.add(new GData5(verts[0], verts[1], verts[2], verts[3], condline.parent,
                            new GColour(condline.colourNumber, condline.r, condline.g, condline.b, condline.a)));
                }
                Vertex[] verts = condlines.get(condline);
                if (verts == null)
                    continue;
                for (Vertex vertex : verts) {
                    objectVertices.add(vertex);
                }
            }

            singleVertices.addAll(selectedVertices);
            singleVertices.removeAll(objectVertices);

            // 4. Copy of the selected data (no whole subfiles!!)

            CLIPBOARD.addAll(effSelectedLines);
            CLIPBOARD.addAll(effSelectedTriangles);
            CLIPBOARD.addAll(effSelectedQuads);
            CLIPBOARD.addAll(effSelectedCondlines);

            // 4. Subfile Based Copy (with INVERTNEXT)
            if (!selectedSubfiles.isEmpty()) {
                for (GData1 subf : selectedSubfiles) {
                    boolean hasInvertnext = false;
                    GData invertNextData = subf.getBefore();
                    while (invertNextData != null && invertNextData.type() != 1 && (invertNextData.type() != 6 || ((GDataBFC) invertNextData).type != BFC.INVERTNEXT)) {
                        invertNextData = invertNextData.getBefore();
                    }
                    if (invertNextData != null && invertNextData.type() == 6) {
                        hasInvertnext = true;
                    }
                    if (hasInvertnext) {
                        CLIPBOARD_InvNext.add(subf);
                    }
                }
                CLIPBOARD.addAll(selectedSubfiles);
            }


            // Sort the clipboard content by linenumber (or ID if the linenumber is the same)

            {
                final HashBiMap<Integer, GData> dpl = linkedDatFile.getDrawPerLine_NOCLONE();
                Collections.sort(CLIPBOARD, new Comparator<GData>(){
                    @Override
                    public int compare(GData o1, GData o2) {
                        if (dpl.containsValue(o1)) {
                            if (dpl.containsValue(o2)) {
                                return dpl.getKey(o1).compareTo(dpl.getKey(o2));
                            } else {
                                switch (o2.type()) {
                                case 1:
                                    return dpl.getKey(o1).compareTo(dpl.getKey(((GData1) o2).firstRef));
                                case 2:
                                    return dpl.getKey(o1).compareTo(dpl.getKey(((GData2) o2).parent.firstRef));
                                case 3:
                                    return dpl.getKey(o1).compareTo(dpl.getKey(((GData3) o2).parent.firstRef));
                                case 4:
                                    return dpl.getKey(o1).compareTo(dpl.getKey(((GData4) o2).parent.firstRef));
                                case 5:
                                    return dpl.getKey(o1).compareTo(dpl.getKey(((GData5) o2).parent.firstRef));
                                default:
                                    GData t = o2.getBefore();
                                    while (t != null && t.getBefore() != null) {
                                        t = t.getBefore();
                                    }
                                    return dpl.getKey(o1).compareTo(dpl.getKey(((GDataInit) t).getParent().firstRef));
                                }
                            }
                        } else {
                            if (dpl.containsValue(o2)) {
                                switch (o1.type()) {
                                case 1:
                                    return dpl.getKey(((GData1) o1).firstRef).compareTo(dpl.getKey(o2));
                                case 2:
                                    return dpl.getKey(((GData2) o1).parent.firstRef).compareTo(dpl.getKey(o2));
                                case 3:
                                    return dpl.getKey(((GData3) o1).parent.firstRef).compareTo(dpl.getKey(o2));
                                case 4:
                                    return dpl.getKey(((GData4) o1).parent.firstRef).compareTo(dpl.getKey(o2));
                                case 5:
                                    return dpl.getKey(((GData5) o1).parent.firstRef).compareTo(dpl.getKey(o2));
                                default:
                                    GData t = o2.getBefore();
                                    while (t != null && t.getBefore() != null) {
                                        t = t.getBefore();
                                    }
                                    return dpl.getKey(((GDataInit) t).getParent().firstRef).compareTo(dpl.getKey(o2));
                                }
                            } else {
                                final Integer co1;
                                final Integer co2;
                                {
                                    switch (o1.type()) {
                                    case 1:
                                        co1 = dpl.getKey(((GData1) o1).firstRef);
                                        break;
                                    case 2:
                                        co1 = dpl.getKey(((GData2) o1).parent.firstRef);
                                        break;
                                    case 3:
                                        co1 = dpl.getKey(((GData3) o1).parent.firstRef);
                                        break;
                                    case 4:
                                        co1 = dpl.getKey(((GData4) o1).parent.firstRef);
                                        break;
                                    case 5:
                                        co1 = dpl.getKey(((GData5) o1).parent.firstRef);
                                        break;
                                    default:
                                        GData t = o2.getBefore();
                                        while (t != null && t.getBefore() != null) {
                                            t = t.getBefore();
                                        }
                                        co1 = dpl.getKey(((GDataInit) t).getParent().firstRef);
                                    }
                                }
                                {
                                    switch (o2.type()) {
                                    case 1:
                                        co2 = dpl.getKey(((GData1) o2).firstRef);
                                        break;
                                    case 2:
                                        co2 = dpl.getKey(((GData2) o2).parent.firstRef);
                                        break;
                                    case 3:
                                        co2 = dpl.getKey(((GData3) o2).parent.firstRef);
                                        break;
                                    case 4:
                                        co2 = dpl.getKey(((GData4) o2).parent.firstRef);
                                        break;
                                    case 5:
                                        co2 = dpl.getKey(((GData5) o2).parent.firstRef);
                                        break;
                                    default:
                                        GData t = o2.getBefore();
                                        while (t != null && t.getBefore() != null) {
                                            t = t.getBefore();
                                        }
                                        co2 = dpl.getKey(((GDataInit) t).getParent().firstRef);
                                    }
                                }
                                int comparism = co1.compareTo(co2);
                                if (comparism == 0) {
                                    if (o1.ID > o2.ID) { // The id can "never" be equal!
                                        return 1;
                                    } else {
                                        return -1;
                                    }
                                } else {
                                    return comparism;
                                }
                            }
                        }
                    }
                });
            }

            for (Vertex v : singleVertices) {
                CLIPBOARD.add(new GData0("0 !LPE VERTEX " + bigDecimalToString(v.X) + " " + bigDecimalToString(v.Y) + " " + bigDecimalToString(v.Z))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$)
            }

            // 5. Create text data entry in the OS clipboard
            final StringBuilder cbString = new StringBuilder();
            for (GData data : CLIPBOARD) {
                if (CLIPBOARD_InvNext.contains(data)) {
                    cbString.append("0 BFC INVERTNEXT"); //$NON-NLS-1$
                    cbString.append(StringHelper.getLineDelimiter());
                    cbString.append(data.toString());
                    cbString.append(StringHelper.getLineDelimiter());
                } else {
                    cbString.append(data.toString());
                    cbString.append(StringHelper.getLineDelimiter());
                }
            }

            final String cbs = cbString.toString();
            if (!cbs.isEmpty()) {
                Display display = Display.getCurrent();
                Clipboard clipboard = new Clipboard(display);
                clipboard.setContents(new Object[] { cbs }, new Transfer[] { TextTransfer.getInstance() });
                clipboard.dispose();
            }

            // 6. Restore selection

            // Reduce the amount of superflous selected data
            selectedVertices.clear();
            selectedVertices.addAll(effSelectedVertices2);

            selectedLines.clear();
            selectedLines.addAll(effSelectedLines2);

            selectedTriangles.clear();
            selectedTriangles.addAll(effSelectedTriangles2);

            selectedQuads.clear();
            selectedQuads.addAll(effSelectedQuads2);

            selectedCondlines.clear();
            selectedCondlines.addAll(effSelectedCondlines2);

            selectedData.addAll(selectedLines);
            selectedData.addAll(selectedTriangles);
            selectedData.addAll(selectedQuads);
            selectedData.addAll(selectedCondlines);
            selectedData.addAll(selectedSubfiles);

            selectedVertices.retainAll(vertexLinkedToPositionInFile.keySet());
        }

    }

    public void paste() {
        if (linkedDatFile.isReadOnly())
            return;
        if (!CLIPBOARD.isEmpty()) {
            clearSelection();
            final HashBiMap<Integer, GData> dpl = linkedDatFile.getDrawPerLine_NOCLONE();

            int linecount = dpl.size();

            GData before = linkedDatFile.getDrawChainTail();
            GData tailData = null;
            for (GData g : CLIPBOARD) {
                Set<String> alreadyParsed = new HashSet<String>();
                alreadyParsed.add(linkedDatFile.getShortName());
                if (CLIPBOARD_InvNext.contains(g)) {
                    GDataBFC invNext = new GDataBFC(BFC.INVERTNEXT);
                    before.setNext(invNext);
                    before = invNext;
                    linecount++;
                    dpl.put(linecount, invNext);
                }
                ArrayList<ParsingResult> result = DatParser.parseLine(g.toString(), -1, 0, 0.5f, 0.5f, 0.5f, 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile, false, alreadyParsed, false);
                GData pasted = result.get(0).getGraphicalData();
                if (pasted == null)
                    pasted = new GData0(g.toString());
                linecount++;
                dpl.put(linecount, pasted);
                selectedData.add(pasted);
                switch (pasted.type()) {
                case 0:
                    selectedData.remove(pasted);
                    Vertex vertex = ((GData0) pasted).getVertex();
                    if (vertex != null) {
                        selectedVertices.add(vertex);
                    }
                    break;
                case 1:
                    selectedSubfiles.add((GData1) pasted);
                    Set<VertexInfo> vis = lineLinkedToVertices.get(pasted);
                    for (VertexInfo vertexInfo : vis) {
                        selectedVertices.add(vertexInfo.getVertex());
                        GData gs = vertexInfo.getLinkedData();
                        selectedData.add(gs);
                        switch (gs.type()) {
                        case 0:
                            selectedData.remove(gs);
                            Vertex vertex2 = ((GData0) gs).getVertex();
                            if (vertex2 != null) {
                                selectedVertices.add(vertex2);
                            }
                            break;
                        case 2:
                            selectedLines.add((GData2) gs);
                            break;
                        case 3:
                            selectedTriangles.add((GData3) gs);
                            break;
                        case 4:
                            selectedQuads.add((GData4) gs);
                            break;
                        case 5:
                            selectedCondlines.add((GData5) gs);
                            break;
                        default:
                            break;
                        }
                    }
                    break;
                case 2:
                    selectedLines.add((GData2) pasted);
                    break;
                case 3:
                    selectedTriangles.add((GData3) pasted);
                    break;
                case 4:
                    selectedQuads.add((GData4) pasted);
                    break;
                case 5:
                    selectedCondlines.add((GData5) pasted);
                    break;
                default:
                    break;
                }
                before.setNext(pasted);
                before = pasted;
                tailData = pasted;
            }
            linkedDatFile.setDrawChainTail(tailData);
            setModified(true, true);
            updateUnsavedStatus();
        }
    }

    public void addParsedLine(String lineToParse) {
        if (linkedDatFile.isReadOnly())
            return;
        clearSelection();
        final HashBiMap<Integer, GData> dpl = linkedDatFile.getDrawPerLine_NOCLONE();
        int linecount = dpl.size();
        GData before = linkedDatFile.getDrawChainTail();
        if (linecount == 1 && before.toString().trim().equals("")) { //$NON-NLS-1$
            GData tmp = before.getBefore();
            before.derefer();
            before = tmp;
            linecount--;
        }
        Set<String> alreadyParsed = new HashSet<String>();
        alreadyParsed.add(linkedDatFile.getShortName());
        ArrayList<ParsingResult> result = DatParser.parseLine(lineToParse, -1, 0, 0.5f, 0.5f, 0.5f, 1.0f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile, false, alreadyParsed, false);
        GData pasted = result.get(0).getGraphicalData();
        if (pasted == null)
            pasted = new GData0(lineToParse);
        linecount++;
        dpl.put(linecount, pasted);
        before.setNext(pasted);
        linkedDatFile.setDrawChainTail(pasted);
        setModified(true, true);
        updateUnsavedStatus();
        validateState();
    }

    public void addLine(Vertex v1, Vertex v2) {
        if (v1 == null || v2 == null) return;
        linkedDatFile.setObjVertex1(null);
        linkedDatFile.setObjVertex2(null);
        Set<VertexManifestation> refs1 = vertexLinkedToPositionInFile.get(v1);
        Set<VertexManifestation> refs2 = vertexLinkedToPositionInFile.get(v2);
        if (refs1 != null && refs2 != null) {
            Set<GData> grefs1 = new HashSet<GData>();
            Set<GData> grefs2 = new HashSet<GData>();
            for (VertexManifestation vm : refs1) {
                grefs1.add(vm.getGdata());
            }
            for (VertexManifestation vm : refs2) {
                grefs2.add(vm.getGdata());
            }
            grefs1.retainAll(grefs2);
            for (GData gData : grefs1) {
                if (gData.type() == 2)
                    return;
            }
        }
        setModified_NoSync();
        if (!Project.getUnsavedFiles().contains(linkedDatFile)) {
            Project.addUnsavedFile(linkedDatFile);
            Editor3DWindow.getWindow().updateTree_unsavedEntries();
        }
        GColour col = Editor3DWindow.getWindow().getLastUsedColour();
        if (col.getColourNumber() == 16 || col.getColourNumber() == 24) {
            // Will never return a 'null' colour!
            col = DatParser.validateColour(24, 0f, 0f, .0f, 0f);
        }
        linkedDatFile.addToTail(new GData2(col.getColourNumber(), col.getR(), col.getG(), col.getB(), col.getA(), v1, v2, View.DUMMY_REFERENCE, linkedDatFile));
        linkedDatFile.setObjVertex1(v1);
        linkedDatFile.setObjVertex2(v2);
    }

    public void addTriangle(Vertex v1, Vertex v2, Vertex v3, Composite3D c3d) {
        if (v1.equals(v2) || v1.equals(v3) || v2.equals(v3))
            return;
        if (v1 == null || v2 == null || v3 == null) return;
        Set<VertexManifestation> refs1 = vertexLinkedToPositionInFile.get(v1);
        Set<VertexManifestation> refs2 = vertexLinkedToPositionInFile.get(v2);
        Set<VertexManifestation> refs3 = vertexLinkedToPositionInFile.get(v3);
        Set<GData> grefs1 = new HashSet<GData>();
        Set<GData> grefs2 = new HashSet<GData>();
        Set<GData> grefs3 = new HashSet<GData>();
        if (refs1 == null)
            refs1 = new HashSet<VertexManifestation>();
        if (refs2 == null)
            refs2 = new HashSet<VertexManifestation>();
        if (refs3 == null)
            refs3 = new HashSet<VertexManifestation>();
        for (VertexManifestation vm : refs1) {
            grefs1.add(vm.getGdata());
        }
        for (VertexManifestation vm : refs2) {
            grefs2.add(vm.getGdata());
        }
        for (VertexManifestation vm : refs3) {
            grefs3.add(vm.getGdata());
        }
        grefs1.retainAll(grefs2);
        grefs1.retainAll(grefs3);
        for (GData gData : grefs1) {
            if (gData.type() == 3)
                return;
        }

        // Resolving Collinearity
        Vector3f v13f = new Vector3f(v1.x, v1.y, v1.z);
        Vector3f v23f = new Vector3f(v2.x, v2.y, v2.z);
        Vector3f v33f = new Vector3f(v3.x, v3.y, v3.z);
        Vector3f.sub(v13f, v33f, v13f);
        Vector3f.sub(v23f, v33f, v23f);
        double angle = Vector3f.angle(v13f, v23f);
        if (angle < Threshold.collinear_angle_minimum) {
            linkedDatFile.setObjVertex3(null);
            return;
        }

        Vector4f n = new Vector4f();
        n.setW(1f);
        n.setX((v3.y - v1.y) * (v2.z - v1.z) - (v3.z - v1.z) * (v2.y - v1.y));
        n.setY((v3.z - v1.z) * (v2.x - v1.x) - (v3.x - v1.x) * (v2.z - v1.z));
        n.setZ((v3.x - v1.x) * (v2.y - v1.y) - (v3.y - v1.y) * (v2.x - v1.x));
        Matrix4f vport = c3d.getViewport();
        Matrix4f.transform(vport, n, n);
        Vector4f.sub(n, new Vector4f(vport.m03, vport.m13, vport.m23, 0f), n);
        if (n.z > 0f ^ Editor3DWindow.getWindow().hasBfcToggle()) {
            Vertex t = v1;
            v1 = v2;
            v2 = t;
        }
        setModified_NoSync();
        if (!Project.getUnsavedFiles().contains(linkedDatFile)) {
            Project.addUnsavedFile(linkedDatFile);
            Editor3DWindow.getWindow().updateTree_unsavedEntries();
        }
        GColour col = Editor3DWindow.getWindow().getLastUsedColour();
        linkedDatFile.addToTail(new GData3(col.getColourNumber(), col.getR(), col.getG(), col.getB(), col.getA(), v1, v2, v3, View.DUMMY_REFERENCE, linkedDatFile));
        setModified(true, true);
    }

    public void addQuad(Vertex v1, Vertex v2, Vertex v3, Vertex v4, Composite3D c3d) {
        {
            Set<Vertex> dupl = new TreeSet<Vertex>();
            dupl.add(v1);
            dupl.add(v2);
            dupl.add(v3);
            dupl.add(v4);
            if (dupl.size() != 4)
                return;
        }
        if (v1 == null || v2 == null || v3 == null || v4 == null) return;
        Set<VertexManifestation> refs1 = vertexLinkedToPositionInFile.get(v1);
        Set<VertexManifestation> refs2 = vertexLinkedToPositionInFile.get(v2);
        Set<VertexManifestation> refs3 = vertexLinkedToPositionInFile.get(v3);
        Set<VertexManifestation> refs4 = vertexLinkedToPositionInFile.get(v4);
        Set<GData> grefs1 = new HashSet<GData>();
        Set<GData> grefs2 = new HashSet<GData>();
        Set<GData> grefs3 = new HashSet<GData>();
        Set<GData> grefs4 = new HashSet<GData>();
        if (refs1 == null)
            refs1 = new HashSet<VertexManifestation>();
        if (refs2 == null)
            refs2 = new HashSet<VertexManifestation>();
        if (refs3 == null)
            refs3 = new HashSet<VertexManifestation>();
        if (refs4 == null)
            refs4 = new HashSet<VertexManifestation>();
        for (VertexManifestation vm : refs1) {
            grefs1.add(vm.getGdata());
        }
        for (VertexManifestation vm : refs2) {
            grefs2.add(vm.getGdata());
        }
        for (VertexManifestation vm : refs3) {
            grefs3.add(vm.getGdata());
        }
        for (VertexManifestation vm : refs4) {
            grefs4.add(vm.getGdata());
        }
        grefs1.retainAll(grefs2);
        grefs1.retainAll(grefs3);
        grefs1.retainAll(grefs4);
        for (GData gData : grefs1) {
            if (gData.type() == 4)
                return;
        }

        // Resolving Hourglass, Coplanarity and Collinearity
        Vector3f v13f = new Vector3f(v1.x, v1.y, v1.z);
        Vector3f v23f = new Vector3f(v2.x, v2.y, v2.z);
        Vector3f v33f = new Vector3f(v3.x, v3.y, v3.z);
        Vector3f v43f = new Vector3f(v4.x, v4.y, v4.z);
        int cnc = 0;
        Vector3f[] lineVectors = new Vector3f[4];
        float[] normalDirections = new float[4];
        Vector3f[] normals = new Vector3f[4];
        boolean triedHGfix = false;

        lineVectors[0] = Vector3f.sub(v23f, v13f, null);
        lineVectors[1] = Vector3f.sub(v33f, v23f, null);
        lineVectors[2] = Vector3f.sub(v43f, v33f, null);
        lineVectors[3] = Vector3f.sub(v13f, v43f, null);
        normals[0] = Vector3f.cross(lineVectors[0], lineVectors[1], null);
        normals[1] = Vector3f.cross(lineVectors[1], lineVectors[2], null);
        normals[2] = Vector3f.cross(lineVectors[2], lineVectors[3], null);
        normals[3] = Vector3f.cross(lineVectors[3], lineVectors[0], null);

        Vector3f normal = new Vector3f();

        for (int i = 0; i < 4; i++) {
            normalDirections[i] = MathHelper.directionOfVectors(normals[0], normals[i]);
            if (normalDirections[i] < 0)
                cnc++;
            normal = Vector3f.add(normals[i], normal, null);
        }

        if (cnc == 2) {
            // Hourglass
            triedHGfix = true;
            Vertex t;
            t = v1;
            v1 = v2;
            v2 = t;
            v13f = new Vector3f(v1.x, v1.y, v1.z);
            v23f = new Vector3f(v2.x, v2.y, v2.z);
        } else if (cnc == 1 || cnc == 3) {
            // Concave
            // Backface Culling
            Vector4f n = new Vector4f();
            n.setW(1f);
            n.setX((v3.y - v1.y) * (v2.z - v1.z) - (v3.z - v1.z) * (v2.y - v1.y));
            n.setY((v3.z - v1.z) * (v2.x - v1.x) - (v3.x - v1.x) * (v2.z - v1.z));
            n.setZ((v3.x - v1.x) * (v2.y - v1.y) - (v3.y - v1.y) * (v2.x - v1.x));
            Matrix4f vport = c3d.getViewport();
            Matrix4f.transform(vport, n, n);
            Vector4f.sub(n, new Vector4f(vport.m03, vport.m13, vport.m23, 0f), n);
            if (n.z > 0f ^ Editor3DWindow.getWindow().hasBfcToggle()) {
                Vertex t = v1;
                v1 = v3;
                v3 = t;
            }
            setModified_NoSync();
            if (!Project.getUnsavedFiles().contains(linkedDatFile)) {
                Project.addUnsavedFile(linkedDatFile);
                Editor3DWindow.getWindow().updateTree_unsavedEntries();
            }
            GColour col = Editor3DWindow.getWindow().getLastUsedColour();
            linkedDatFile.addToTail(new GData3(col.getColourNumber(), col.getR(), col.getG(), col.getB(), col.getA(), v2, v3, v4, View.DUMMY_REFERENCE, linkedDatFile));
            linkedDatFile.addToTail(new GData3(col.getColourNumber(), col.getR(), col.getG(), col.getB(), col.getA(), v4, v1, v2, View.DUMMY_REFERENCE, linkedDatFile));
            setModified(true, true);
            return;
        }

        if (triedHGfix) {
            normal = new Vector3f();
            cnc = 0;
            lineVectors[0] = Vector3f.sub(v23f, v13f, null);
            lineVectors[1] = Vector3f.sub(v33f, v23f, null);
            lineVectors[2] = Vector3f.sub(v43f, v33f, null);
            lineVectors[3] = Vector3f.sub(v13f, v43f, null);
            normals[0] = Vector3f.cross(lineVectors[0], lineVectors[1], null);
            normals[1] = Vector3f.cross(lineVectors[1], lineVectors[2], null);
            normals[2] = Vector3f.cross(lineVectors[2], lineVectors[3], null);
            normals[3] = Vector3f.cross(lineVectors[3], lineVectors[0], null);

            for (int i = 0; i < 4; i++) {
                normalDirections[i] = MathHelper.directionOfVectors(normals[0], normals[i]);
                if (normalDirections[i] < 0)
                    cnc++;
            }

            if (cnc == 2) {
                // Hourglass II (retry)
                Vertex t;
                t = v1;
                v1 = v2;
                v2 = t;
                t = v2;
                v2 = v3;
                v3 = t;
                v13f = new Vector3f(v1.x, v1.y, v1.z);
                v23f = new Vector3f(v2.x, v2.y, v2.z);
                v33f = new Vector3f(v3.x, v3.y, v3.z);
            } else if (cnc == 1 || cnc == 3) {
                // Concave
                linkedDatFile.setObjVertex4(null);
                return;
            }

            lineVectors[0] = Vector3f.sub(v23f, v13f, null);
            lineVectors[1] = Vector3f.sub(v33f, v23f, null);
            lineVectors[2] = Vector3f.sub(v43f, v33f, null);
            lineVectors[3] = Vector3f.sub(v13f, v43f, null);
            normals[0] = Vector3f.cross(lineVectors[0], lineVectors[1], null);
            normals[1] = Vector3f.cross(lineVectors[1], lineVectors[2], null);
            normals[2] = Vector3f.cross(lineVectors[2], lineVectors[3], null);
            normals[3] = Vector3f.cross(lineVectors[3], lineVectors[0], null);

            for (int i = 0; i < 4; i++) {
                normalDirections[i] = MathHelper.directionOfVectors(normals[0], normals[i]);
                if (normalDirections[i] < 0)
                    cnc++;
                normal = Vector3f.add(normals[i], normal, null);
            }
        }

        double angle2 = Vector3f.angle(normals[0], normals[2]);
        double angle;

        boolean parseError = false;
        Vector3f.sub(v13f, v43f, v13f);
        Vector3f.sub(v23f, v43f, v23f);
        Vector3f.sub(v33f, v43f, v33f);
        angle = Vector3f.angle(v13f, v23f);
        parseError = angle < Threshold.collinear_angle_minimum;
        angle = Vector3f.angle(v23f, v33f);
        parseError = parseError || angle < Threshold.collinear_angle_minimum;
        angle = Vector3f.angle(v33f, v13f);
        parseError = parseError || angle < Threshold.collinear_angle_minimum;

        // Collinearity
        if (parseError) {
            linkedDatFile.setObjVertex3(null);
            linkedDatFile.setObjVertex4(null);
            return;
        }

        // Coplanarity
        if (angle2 > Threshold.coplanarity_angle_error) {
            linkedDatFile.setObjVertex4(null);
            return;
        }

        // Backface Culling
        Vector4f n = new Vector4f();
        n.setW(1f);
        n.setX((v3.y - v1.y) * (v2.z - v1.z) - (v3.z - v1.z) * (v2.y - v1.y));
        n.setY((v3.z - v1.z) * (v2.x - v1.x) - (v3.x - v1.x) * (v2.z - v1.z));
        n.setZ((v3.x - v1.x) * (v2.y - v1.y) - (v3.y - v1.y) * (v2.x - v1.x));
        Matrix4f vport = c3d.getViewport();
        Matrix4f.transform(vport, n, n);
        Vector4f.sub(n, new Vector4f(vport.m03, vport.m13, vport.m23, 0f), n);
        if (n.z > 0f ^ Editor3DWindow.getWindow().hasBfcToggle()) {
            Vertex t = v1;
            v1 = v3;
            v3 = t;
        }

        setModified_NoSync();
        if (!Project.getUnsavedFiles().contains(linkedDatFile)) {
            Project.addUnsavedFile(linkedDatFile);
            Editor3DWindow.getWindow().updateTree_unsavedEntries();
        }
        GColour col = Editor3DWindow.getWindow().getLastUsedColour();
        linkedDatFile.addToTail(new GData4(col.getColourNumber(), col.getR(), col.getG(), col.getB(), col.getA(), v1, v2, v3, v4, View.DUMMY_REFERENCE, linkedDatFile));
        setModified(true, true);
    }

    public void addCondline(Vertex v1, Vertex v2, Vertex v3, Vertex v4) {
        {
            Set<Vertex> dupl = new TreeSet<Vertex>();
            dupl.add(v1);
            dupl.add(v2);
            dupl.add(v3);
            dupl.add(v4);
            if (dupl.size() != 4)
                return;
        }
        if (v1 == null || v2 == null || v3 == null || v4 == null) return;
        Set<VertexManifestation> refs1 = vertexLinkedToPositionInFile.get(v1);
        Set<VertexManifestation> refs2 = vertexLinkedToPositionInFile.get(v2);
        Set<GData> grefs1 = new HashSet<GData>();
        Set<GData> grefs2 = new HashSet<GData>();
        if (refs1 == null)
            refs1 = new HashSet<VertexManifestation>();
        if (refs2 == null)
            refs2 = new HashSet<VertexManifestation>();
        for (VertexManifestation vm : refs1) {
            grefs1.add(vm.getGdata());
        }
        for (VertexManifestation vm : refs2) {
            grefs2.add(vm.getGdata());
        }
        grefs1.retainAll(grefs2);
        for (GData gData : grefs1) {
            if (gData.type() == 5) {
                GData5 cline = (GData5) gData;
                Vertex vc1 = new Vertex(cline.x1, cline.y1, cline.z1);
                Vertex vc2 = new Vertex(cline.x2, cline.y2, cline.z2);
                if (vc1.equals(v1) && vc2.equals(v2) || vc2.equals(v1) && vc1.equals(v2)) {
                    return;
                }
            }
        }
        setModified_NoSync();
        if (!Project.getUnsavedFiles().contains(linkedDatFile)) {
            Project.addUnsavedFile(linkedDatFile);
            Editor3DWindow.getWindow().updateTree_unsavedEntries();
        }
        GColour col = Editor3DWindow.getWindow().getLastUsedColour();
        if (col.getColourNumber() == 16 || col.getColourNumber() == 24) {
            // Will never return a 'null' colour!
            col = DatParser.validateColour(24, 0f, 0f, 0f, 0f);
        }
        linkedDatFile.addToTail(new GData5(col.getColourNumber(), col.getR(), col.getG(), col.getB(), col.getA(), v1, v2, v3, v4, View.DUMMY_REFERENCE, linkedDatFile));
    }

    public void addBackgroundPicture(String text, Vertex offset, BigDecimal angleA, BigDecimal angleB, BigDecimal angleC, Vertex scale, String texturePath) {
        setModified_NoSync();
        if (!Project.getUnsavedFiles().contains(linkedDatFile)) {
            Project.addUnsavedFile(linkedDatFile);
            Editor3DWindow.getWindow().updateTree_unsavedEntries();
        }
        GDataPNG pic = new GDataPNG(new GDataPNG(text, offset, angleA, angleB, angleC, scale, texturePath).getString(offset, angleA, angleB, angleC, scale, texturePath), offset, angleA, angleB, angleC, scale, texturePath);
        linkedDatFile.addToTail(pic);
        setSelectedBgPicture(pic);
        setModified_NoSync();
    }

    public void zoomToFit(Composite3D c3d) {
        final PerspectiveCalculator pc = c3d.getPerspectiveCalculator();
        float max_x = 0f;
        float max_y = 0f;
        for (Vertex v : getVertices()) {
            float ax = Math.abs(v.x);
            float ay = Math.abs(v.y);
            if (ax > max_x) max_x = ax;
            if (ay > max_y) max_y = ay;
        }
        Matrix4f id = new Matrix4f();
        Matrix4f.setIdentity(id);
        c3d.getTranslation().load(id);
        Rectangle b = c3d.getBounds();
        if (max_x > max_y) {
            c3d.setZoom(b.width / (max_x * 4f * View.PIXEL_PER_LDU));
        } else {
            c3d.setZoom(b.height / (max_y * 4f * View.PIXEL_PER_LDU));
        }
        pc.setZoom_exponent((float) (Math.log10(c3d.getZoom()) + 3f) * 10f);

        if (Float.isInfinite(c3d.getZoom()) || Float.isInfinite(pc.getZoom_exponent()) || Float.isNaN(c3d.getZoom()) || Float.isNaN(pc.getZoom_exponent())) {
            pc.setZoom_exponent(-20f);
            c3d.setZoom((float) Math.pow(10.0d, -20f / 10 - 3));
        }

        c3d.setViewportPixelPerLDU(c3d.getZoom() * View.PIXEL_PER_LDU);
        GuiManager.updateStatus(c3d);
        ((ScalableComposite) c3d.getParent()).redrawScales();
        pc.initializeViewportPerspective();
    }

    public synchronized void roundSelection(int coordsDecimalPlaces, int matrixDecimalPlaces, boolean moveAdjacentData, boolean syncWithTextEditors) {

        if (linkedDatFile.isReadOnly())
            return;

        final Set<Vertex> singleVertices = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());

        final HashSet<GData0> effSelectedVertices = new HashSet<GData0>();
        final HashSet<GData2> effSelectedLines = new HashSet<GData2>();
        final HashSet<GData3> effSelectedTriangles = new HashSet<GData3>();
        final HashSet<GData4> effSelectedQuads = new HashSet<GData4>();
        final HashSet<GData5> effSelectedCondlines = new HashSet<GData5>();

        selectedData.clear();

        // 0. Deselect selected subfile data (for whole selected subfiles)
        for (GData1 subf : selectedSubfiles) {
            Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
            for (VertexInfo vertexInfo : vis) {
                if (!moveAdjacentData)
                    selectedVertices.remove(vertexInfo.getVertex());
                GData g = vertexInfo.getLinkedData();
                switch (g.type()) {
                case 2:
                    selectedLines.remove(g);
                    break;
                case 3:
                    selectedTriangles.remove(g);
                    break;
                case 4:
                    selectedQuads.remove(g);
                    break;
                case 5:
                    selectedCondlines.remove(g);
                    break;
                default:
                    break;
                }
            }
        }

        // 1. Vertex Based Selection
        {
            final Set<Vertex> objectVertices = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());
            {
                HashMap<GData, Integer> occurMap = new HashMap<GData, Integer>();
                for (Vertex vertex : selectedVertices) {
                    Set<VertexManifestation> occurences = vertexLinkedToPositionInFile.get(vertex);
                    if (occurences == null)
                        continue;
                    boolean isPureSubfileVertex = true;
                    for (VertexManifestation vm : occurences) {
                        GData g = vm.getGdata();
                        int val = 1;
                        if (occurMap.containsKey(g)) {
                            val = occurMap.get(g);
                            val++;
                        }
                        occurMap.put(g, val);
                        switch (g.type()) {
                        case 0:
                            GData0 meta = (GData0) g;
                            boolean idCheck = !lineLinkedToVertices.containsKey(meta);
                            isPureSubfileVertex = isPureSubfileVertex && idCheck;
                            if (val == 1) {
                                if (!idCheck) {
                                    effSelectedVertices.add(meta);
                                }
                            }
                            break;
                        case 2:
                            GData2 line = (GData2) g;
                            idCheck = !line.parent.equals(View.DUMMY_REFERENCE);
                            isPureSubfileVertex = isPureSubfileVertex && idCheck;
                            if (val == 2) {
                                if (!idCheck) {
                                    selectedLines.add(line);
                                }
                            }
                            break;
                        case 3:
                            GData3 triangle = (GData3) g;
                            idCheck = !triangle.parent.equals(View.DUMMY_REFERENCE);
                            isPureSubfileVertex = isPureSubfileVertex && idCheck;
                            if (val == 3) {
                                if (!idCheck) {
                                    selectedTriangles.add(triangle);
                                }
                            }
                            break;
                        case 4:
                            GData4 quad = (GData4) g;
                            idCheck = !quad.parent.equals(View.DUMMY_REFERENCE);
                            isPureSubfileVertex = isPureSubfileVertex && idCheck;
                            if (val == 4) {
                                if (!idCheck) {
                                    selectedQuads.add(quad);
                                }
                            }
                            break;
                        case 5:
                            GData5 condline = (GData5) g;
                            idCheck = !condline.parent.equals(View.DUMMY_REFERENCE);
                            isPureSubfileVertex = isPureSubfileVertex && idCheck;
                            if (val == 4) {
                                if (!idCheck) {
                                    selectedCondlines.add(condline);
                                }
                            }
                            break;
                        }
                    }
                    if (isPureSubfileVertex)
                        objectVertices.add(vertex);
                }
            }

            // 2. Object Based Selection

            for (GData2 line : selectedLines) {
                if (line.parent.equals(View.DUMMY_REFERENCE))
                    effSelectedLines.add(line);
                Vertex[] verts = lines.get(line);
                if (verts == null)
                    continue;
                for (Vertex vertex : verts) {
                    objectVertices.add(vertex);
                }
            }
            for (GData3 triangle : selectedTriangles) {
                if (triangle.parent.equals(View.DUMMY_REFERENCE))
                    effSelectedTriangles.add(triangle);
                Vertex[] verts = triangles.get(triangle);
                if (verts == null)
                    continue;
                for (Vertex vertex : verts) {
                    objectVertices.add(vertex);
                }
            }
            for (GData4 quad : selectedQuads) {
                if (quad.parent.equals(View.DUMMY_REFERENCE))
                    effSelectedQuads.add(quad);
                Vertex[] verts = quads.get(quad);
                if (verts == null)
                    continue;
                for (Vertex vertex : verts) {
                    objectVertices.add(vertex);
                }
            }
            for (GData5 condline : selectedCondlines) {
                if (condline.parent.equals(View.DUMMY_REFERENCE))
                    effSelectedCondlines.add(condline);
                Vertex[] verts = condlines.get(condline);
                if (verts == null)
                    continue;
                for (Vertex vertex : verts) {
                    objectVertices.add(vertex);
                }
            }

            Set<GData0> vs = new HashSet<GData0>(effSelectedVertices);
            for (GData0 effvert : vs) {
                Vertex v = effvert.getVertex();
                if (v != null && objectVertices.contains(v)) {
                    singleVertices.add(v);
                }
            }

            singleVertices.addAll(objectVertices);
            singleVertices.addAll(selectedVertices);

            // 3. Rounding of the selected data (no whole subfiles!!)
            // + selectedData update!
            if (!singleVertices.isEmpty()) {
                setModified_NoSync();
            }
            for (Vertex vOld : singleVertices) {
                Vertex vNew = new Vertex(vOld.X.setScale(coordsDecimalPlaces, RoundingMode.HALF_UP), vOld.Y.setScale(coordsDecimalPlaces, RoundingMode.HALF_UP), vOld.Z.setScale(coordsDecimalPlaces,
                        RoundingMode.HALF_UP));
                changeVertexDirectFast(vOld, vNew, moveAdjacentData);
            }

            // 4. Subfile Based Rounding & Selection
            if (!selectedSubfiles.isEmpty()) {
                HashBiMap<Integer, GData> drawPerLine = linkedDatFile.getDrawPerLine_NOCLONE();
                HashSet<GData1> newSubfiles = new HashSet<GData1>();
                for (GData1 subf : selectedSubfiles) {
                    String roundedString = subf.getRoundedString(coordsDecimalPlaces, matrixDecimalPlaces);
                    GData roundedSubfile;
                    if (16 == subf.colourNumber) {
                        roundedSubfile = DatParser
                                .parseLine(roundedString, drawPerLine.getKey(subf).intValue(), 0, 0.5f, 0.5f, 0.5f, 1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile, false,
                                        new HashSet<String>(), false).get(0).getGraphicalData();
                    } else {
                        roundedSubfile = DatParser
                                .parseLine(roundedString, drawPerLine.getKey(subf).intValue(), 0, subf.r, subf.g, subf.b, subf.a, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile,
                                        false, new HashSet<String>(), false).get(0).getGraphicalData();
                    }
                    if (subf.equals(linkedDatFile.getDrawChainTail()))
                        linkedDatFile.setDrawChainTail(roundedSubfile);
                    GData oldNext = subf.getNext();
                    GData oldBefore = subf.getBefore();
                    oldBefore.setNext(roundedSubfile);
                    roundedSubfile.setNext(oldNext);
                    Integer oldNumber = drawPerLine.getKey(subf);
                    if (oldNumber != null)
                        drawPerLine.put(oldNumber, roundedSubfile);
                    remove(subf);
                    newSubfiles.add((GData1) roundedSubfile);
                }
                selectedSubfiles.clear();
                selectedSubfiles.addAll(newSubfiles);

                for (GData1 subf : selectedSubfiles) {
                    Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
                    for (VertexInfo vertexInfo : vis) {
                        selectedVertices.add(vertexInfo.getVertex());
                        GData g = vertexInfo.getLinkedData();
                        switch (g.type()) {
                        case 2:
                            selectedLines.add((GData2) g);
                            break;
                        case 3:
                            selectedTriangles.add((GData3) g);
                            break;
                        case 4:
                            selectedQuads.add((GData4) g);
                            break;
                        case 5:
                            selectedCondlines.add((GData5) g);
                            break;
                        default:
                            break;
                        }
                    }
                }
                setModified_NoSync();
            }

            if (isModified()) {
                for(Iterator<GData2> it = selectedLines.iterator();it.hasNext();){
                    if (!exist(it.next())) it.remove();
                }
                for(Iterator<GData3> it = selectedTriangles.iterator();it.hasNext();){
                    if (!exist(it.next())) it.remove();
                }
                for(Iterator<GData4> it = selectedQuads.iterator();it.hasNext();){
                    if (!exist(it.next())) it.remove();
                }
                for(Iterator<GData5> it = selectedCondlines.iterator();it.hasNext();){
                    if (!exist(it.next())) it.remove();
                }
                selectedData.addAll(selectedLines);
                selectedData.addAll(selectedTriangles);
                selectedData.addAll(selectedQuads);
                selectedData.addAll(selectedCondlines);
                selectedData.addAll(selectedSubfiles);

                if (syncWithTextEditors) syncWithTextEditors(true);
                updateUnsavedStatus();
            }

            selectedVertices.retainAll(vertexLinkedToPositionInFile.keySet());
        }
    }

    private boolean exist(GData g) {
        return lines.containsKey(g) || triangles.containsKey(g) || quads.containsKey(g) || condlines.containsKey(g) || lineLinkedToVertices.containsKey(g);
    }

    public synchronized void selectAll(SelectorSettings ss, boolean includeHidden) {
        clearSelection();
        if (ss == null) ss = new SelectorSettings();
        if (includeHidden) {

            if (ss.isVertices()) selectedVertices.addAll(vertexLinkedToPositionInFile.keySet());

            if (ss.isLines()) selectedLines.addAll(lines.keySet());
            if (ss.isTriangles()) selectedTriangles.addAll(triangles.keySet());
            if (ss.isQuads()) selectedQuads.addAll(quads.keySet());
            if (ss.isCondlines()) selectedCondlines.addAll(condlines.keySet());

            if (ss.isVertices() && ss.isLines() && ss.isTriangles() && ss.isQuads() && ss.isCondlines()) {
                selectedSubfiles.addAll(vertexCountInSubfile.keySet());
            }

        } else {

            if (ss.isVertices()) {
                for (Vertex v : vertexLinkedToPositionInFile.keySet()) {
                    if (!hiddenVertices.contains(v)) selectedVertices.add(v);
                }
            }
            if (ss.isVertices() && ss.isLines() && ss.isTriangles() && ss.isQuads() && ss.isCondlines()) {
                for (GData1 g : vertexCountInSubfile.keySet()) {
                    if (!hiddenData.contains(g)) selectedSubfiles.add(g);
                }
            }
            if (ss.isLines()) {
                for (GData2 g : lines.keySet()) {
                    if (!hiddenData.contains(g)) selectedLines.add(g);
                }
            }
            if (ss.isTriangles()) {
                for (GData3 g : triangles.keySet()) {
                    if (!hiddenData.contains(g)) selectedTriangles.add(g);
                }
            }
            if (ss.isQuads()) {
                for (GData4 g : quads.keySet()) {
                    if (!hiddenData.contains(g)) selectedQuads.add(g);
                }
            }
            if (ss.isCondlines()) {
                for (GData5 g : condlines.keySet()) {
                    if (!hiddenData.contains(g)) selectedCondlines.add(g);
                }
            }
        }

        selectedData.addAll(selectedLines);
        selectedData.addAll(selectedTriangles);
        selectedData.addAll(selectedQuads);
        selectedData.addAll(selectedCondlines);
        selectedData.addAll(selectedSubfiles);

    }


    public void addEdges(Edger2Settings es) {

        if (linkedDatFile.isReadOnly()) return;

        initBFCmap();

        final BigDecimal ed = es.getEqualDistance();
        TreeMap<Vertex, Vertex> snap = new TreeMap<Vertex, Vertex>();
        TreeMap<Vertex, TreeSet<Vertex>> snapToOriginal = new TreeMap<Vertex, TreeSet<Vertex>>();

        HashMap<AccurateEdge, Integer> edges = new HashMap<AccurateEdge, Integer>();
        HashSet<AccurateEdge> presentEdges = new HashSet<AccurateEdge>();

        switch (es.getScope()) {
        case 0: // All Data
        {
            Set<Vertex> allVerts = vertexLinkedToPositionInFile.keySet();
            for (Vertex vertex : allVerts) {
                if (!snap.containsKey(vertex)) snap.put(vertex, new Vertex(
                        vertex.X.subtract(vertex.X.remainder(ed, Threshold.mc)).setScale(ed.scale(), RoundingMode.HALF_UP),
                        vertex.Y.subtract(vertex.Y.remainder(ed, Threshold.mc)).setScale(ed.scale(), RoundingMode.HALF_UP),
                        vertex.Z.subtract(vertex.Z.remainder(ed, Threshold.mc)).setScale(ed.scale(), RoundingMode.HALF_UP)
                        ));
                if (snapToOriginal.containsKey(snap.get(vertex))) {
                    snapToOriginal.get(snap.get(vertex)).add(vertex);
                } else {
                    TreeSet<Vertex> h = new TreeSet<Vertex>();
                    h.add(vertex);
                    snapToOriginal.put(snap.get(vertex), h);
                }
            }
            Set<GData2> lins = lines.keySet();
            for (GData2 g2 : lins) {
                Vertex[] verts = lines.get(g2);
                AccurateEdge e1 = new AccurateEdge(snap.get(verts[0]), snap.get(verts[1]));
                presentEdges.add(e1);
            }
            Set<GData5> clins = condlines.keySet();
            for (GData5 g5 : clins) {
                Vertex[] verts = condlines.get(g5);
                AccurateEdge e1 = new AccurateEdge(snap.get(verts[0]), snap.get(verts[1]));
                presentEdges.add(e1);
            }

            Set<GData3> tris = triangles.keySet();
            for (GData3 g3 : tris) {
                Vertex[] verts = triangles.get(g3);
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[0]), snap.get(verts[1]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[1]), snap.get(verts[2]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[2]), snap.get(verts[0]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
            }
            Set<GData4> qs = quads.keySet();
            for (GData4 g4 : qs) {
                addLineQuadEdger2(g4, presentEdges, es, snap);
                Vertex[] verts = quads.get(g4);
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[0]), snap.get(verts[1]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[1]), snap.get(verts[2]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[2]), snap.get(verts[3]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[3]), snap.get(verts[0]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
            }

            GColour tmpCol = Editor3DWindow.getWindow().getLastUsedColour();
            Editor3DWindow.getWindow().setLastUsedColour(View.getLDConfigColour(16));
            if (es.getUnmatchedMode() < 2) {
                Set<AccurateEdge> ee = edges.keySet();
                for (AccurateEdge e : ee) {
                    if (edges.get(e) > 1) {
                        addLineEdger2(snapToOriginal.get(e.v1),  snapToOriginal.get(e.v2), es);
                    }
                }
            }

            Editor3DWindow.getWindow().setLastUsedColour(View.getLDConfigColour(4));
            if (es.getUnmatchedMode() != 1) {
                Set<AccurateEdge> ee = edges.keySet();
                for (AccurateEdge e : ee) {
                    if (edges.get(e) == 1) {
                        addEdgeEdger2(snapToOriginal.get(e.v1),  snapToOriginal.get(e.v2));
                    }
                }
            }
            Editor3DWindow.getWindow().setLastUsedColour(tmpCol);

        }
        break;
        case 1: // No Subfile Facets
        {
            Set<Vertex> allVerts = vertexLinkedToPositionInFile.keySet();
            for (Vertex vertex : allVerts) {
                if (!snap.containsKey(vertex)) snap.put(vertex, new Vertex(
                        vertex.X.subtract(vertex.X.remainder(ed, Threshold.mc)).setScale(ed.scale(), RoundingMode.HALF_UP),
                        vertex.Y.subtract(vertex.Y.remainder(ed, Threshold.mc)).setScale(ed.scale(), RoundingMode.HALF_UP),
                        vertex.Z.subtract(vertex.Z.remainder(ed, Threshold.mc)).setScale(ed.scale(), RoundingMode.HALF_UP)
                        ));
                if (snapToOriginal.containsKey(snap.get(vertex))) {
                    snapToOriginal.get(snap.get(vertex)).add(vertex);
                } else {
                    TreeSet<Vertex> h = new TreeSet<Vertex>();
                    h.add(vertex);
                    snapToOriginal.put(snap.get(vertex), h);
                }
            }
            Set<GData2> lins = lines.keySet();
            for (GData2 g2 : lins) {
                Vertex[] verts = lines.get(g2);
                AccurateEdge e1 = new AccurateEdge(snap.get(verts[0]), snap.get(verts[1]));
                presentEdges.add(e1);
            }
            Set<GData5> clins = condlines.keySet();
            for (GData5 g5 : clins) {
                Vertex[] verts = condlines.get(g5);
                AccurateEdge e1 = new AccurateEdge(snap.get(verts[0]), snap.get(verts[1]));
                presentEdges.add(e1);
            }

            Set<GData3> tris = triangles.keySet();
            for (GData3 g3 : tris) {
                if (!lineLinkedToVertices.containsKey(g3)) continue;
                Vertex[] verts = triangles.get(g3);
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[0]), snap.get(verts[1]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[1]), snap.get(verts[2]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[2]), snap.get(verts[0]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
            }
            Set<GData4> qs = quads.keySet();
            for (GData4 g4 : qs) {
                if (!lineLinkedToVertices.containsKey(g4)) continue;
                addLineQuadEdger2(g4, presentEdges, es, snap);
                Vertex[] verts = quads.get(g4);
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[0]), snap.get(verts[1]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[1]), snap.get(verts[2]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[2]), snap.get(verts[3]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[3]), snap.get(verts[0]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
            }

            GColour tmpCol = Editor3DWindow.getWindow().getLastUsedColour();
            Editor3DWindow.getWindow().setLastUsedColour(View.getLDConfigColour(16));
            if (es.getUnmatchedMode() < 2) {
                Set<AccurateEdge> ee = edges.keySet();
                for (AccurateEdge e : ee) {
                    if (edges.get(e) > 1) {
                        addLineEdger2(snapToOriginal.get(e.v1),  snapToOriginal.get(e.v2), es);
                    }
                }
            }

            Editor3DWindow.getWindow().setLastUsedColour(View.getLDConfigColour(4));
            if (es.getUnmatchedMode() != 1) {
                Set<AccurateEdge> ee = edges.keySet();
                for (AccurateEdge e : ee) {
                    if (edges.get(e) == 1) {
                        addEdgeEdger2(snapToOriginal.get(e.v1),  snapToOriginal.get(e.v2));
                    }
                }
            }
            Editor3DWindow.getWindow().setLastUsedColour(tmpCol);

        }
        break;
        case 2: // Selected Data Only
        {
            Set<Vertex> allVerts = vertexLinkedToPositionInFile.keySet();
            for (Vertex vertex : allVerts) {
                if (!snap.containsKey(vertex)) snap.put(vertex, new Vertex(
                        vertex.X.subtract(vertex.X.remainder(ed, Threshold.mc)).setScale(ed.scale(), RoundingMode.HALF_UP),
                        vertex.Y.subtract(vertex.Y.remainder(ed, Threshold.mc)).setScale(ed.scale(), RoundingMode.HALF_UP),
                        vertex.Z.subtract(vertex.Z.remainder(ed, Threshold.mc)).setScale(ed.scale(), RoundingMode.HALF_UP)
                        ));
                if (snapToOriginal.containsKey(snap.get(vertex))) {
                    snapToOriginal.get(snap.get(vertex)).add(vertex);
                } else {
                    TreeSet<Vertex> h = new TreeSet<Vertex>();
                    h.add(vertex);
                    snapToOriginal.put(snap.get(vertex), h);
                }
            }
            Set<GData2> lins = lines.keySet();
            for (GData2 g2 : lins) {
                Vertex[] verts = lines.get(g2);
                AccurateEdge e1 = new AccurateEdge(snap.get(verts[0]), snap.get(verts[1]));
                presentEdges.add(e1);
            }
            Set<GData5> clins = condlines.keySet();
            for (GData5 g5 : clins) {
                Vertex[] verts = condlines.get(g5);
                AccurateEdge e1 = new AccurateEdge(snap.get(verts[0]), snap.get(verts[1]));
                presentEdges.add(e1);
            }

            Set<GData3> tris = triangles.keySet();
            for (GData3 g3 : tris) {
                if (!lineLinkedToVertices.containsKey(g3)) continue;
                Vertex[] verts = triangles.get(g3);
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[0]), snap.get(verts[1]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[1]), snap.get(verts[2]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[2]), snap.get(verts[0]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
            }
            Set<GData4> qs = quads.keySet();
            for (GData4 g4 : qs) {
                if (!lineLinkedToVertices.containsKey(g4)) continue;
                Vertex[] verts = quads.get(g4);
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[0]), snap.get(verts[1]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[1]), snap.get(verts[2]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[2]), snap.get(verts[3]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[3]), snap.get(verts[0]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
            }


            {
                HashSet<AccurateEdge> selectedEdges = new HashSet<AccurateEdge>();

                for (GData3 g3 : selectedTriangles) {
                    Vertex[] verts = triangles.get(g3);
                    {
                        AccurateEdge e = new AccurateEdge(snap.get(verts[0]), snap.get(verts[1]));
                        if (!presentEdges.contains(e)) {
                            selectedEdges.add(e);
                        }
                    }
                    {
                        AccurateEdge e = new AccurateEdge(snap.get(verts[1]), snap.get(verts[2]));
                        if (!presentEdges.contains(e)) {
                            selectedEdges.add(e);
                        }
                    }
                    {
                        AccurateEdge e = new AccurateEdge(snap.get(verts[2]), snap.get(verts[0]));
                        if (!presentEdges.contains(e)) {
                            selectedEdges.add(e);
                        }
                    }
                }
                for (GData4 g4 : selectedQuads) {
                    addLineQuadEdger2(g4, presentEdges, es, snap);
                    Vertex[] verts = quads.get(g4);
                    {
                        AccurateEdge e = new AccurateEdge(snap.get(verts[0]), snap.get(verts[1]));
                        if (!presentEdges.contains(e)) {
                            selectedEdges.add(e);
                        }
                    }
                    {
                        AccurateEdge e = new AccurateEdge(snap.get(verts[1]), snap.get(verts[2]));
                        if (!presentEdges.contains(e)) {
                            selectedEdges.add(e);
                        }
                    }
                    {
                        AccurateEdge e = new AccurateEdge(snap.get(verts[2]), snap.get(verts[3]));
                        if (!presentEdges.contains(e)) {
                            selectedEdges.add(e);
                        }
                    }
                    {
                        AccurateEdge e = new AccurateEdge(snap.get(verts[3]), snap.get(verts[0]));
                        if (!presentEdges.contains(e)) {
                            selectedEdges.add(e);
                        }
                    }
                }

                Set<AccurateEdge> keySet = edges.keySet();
                for(Iterator<AccurateEdge> it = keySet.iterator(); it.hasNext();) {
                    if (!selectedEdges.contains(it.next())) {
                        it.remove();
                    }
                }
            }

            GColour tmpCol = Editor3DWindow.getWindow().getLastUsedColour();
            Editor3DWindow.getWindow().setLastUsedColour(View.getLDConfigColour(16));
            if (es.getUnmatchedMode() < 2) {
                Set<AccurateEdge> ee = edges.keySet();
                for (AccurateEdge e : ee) {
                    if (edges.get(e) > 1) {
                        addLineEdger2(snapToOriginal.get(e.v1),  snapToOriginal.get(e.v2), es);
                    }
                }
            }

            Editor3DWindow.getWindow().setLastUsedColour(View.getLDConfigColour(4));
            if (es.getUnmatchedMode() != 1) {
                Set<AccurateEdge> ee = edges.keySet();
                for (AccurateEdge e : ee) {
                    if (edges.get(e) == 1) {
                        addEdgeEdger2(snapToOriginal.get(e.v1),  snapToOriginal.get(e.v2));
                    }
                }
            }
            Editor3DWindow.getWindow().setLastUsedColour(tmpCol);
        }
        break;
        default:
            break;
        }

        disposeBFCmap();

    }

    private void addLineQuadEdger2(GData4 g4, HashSet<AccurateEdge> presentEdges, Edger2Settings es, TreeMap<Vertex, Vertex> snap) {

        Vertex[] verts = quads.get(g4);

        Vertex v1 = verts[0];
        Vertex v2 = verts[2];

        if (presentEdges.contains(new AccurateEdge(snap.get(v1), snap.get(v2)))) return;

        Vector3d n1;
        Vector3d n2;

        Vertex v3 = verts[1];
        Vertex v4 = verts[3];
        n1 = Vector3d.getNormal(new Vector3d(verts[2]), new Vector3d(verts[0]), new Vector3d(verts[1])); // T1 1-2-3
        n2 = Vector3d.getNormal(new Vector3d(verts[0]), new Vector3d(verts[1]), new Vector3d(verts[3])); // T2 3-4-1
        double angle;
        if (es.isExtendedRange()) {
            if (getBFCorientation(g4) == BFC.CCW) {
                n1.negate();
                n2.negate();
            }
            angle = Vector3d.angle(n1, n2);
        } else {
            angle = Vector3d.angle(n1, n2);
            if(angle > 90.0) angle = 180.0 - angle;
        }
        if (angle <= es.getAf().doubleValue()) {
            // No Line
        } else if (angle > es.getAf().doubleValue() && angle <= es.getAc().doubleValue()) {
            // Condline
            Editor3DWindow.getWindow().setLastUsedColour(View.getLDConfigColour(16));
            addCondline(v1, v2, v3, v4);
        } else if (angle > es.getAc().doubleValue() && angle <= es.getAe().doubleValue()) {
            // Condline + Edge Line
            Editor3DWindow.getWindow().setLastUsedColour(View.getLDConfigColour(16));
            addCondline(v1, v2, v3, v4);
            Editor3DWindow.getWindow().setLastUsedColour(View.getLDConfigColour(2));
            addLine(v1, v2);
        } else {
            // Edge Line
            Editor3DWindow.getWindow().setLastUsedColour(View.getLDConfigColour(16));
            addLine(v1, v2);
        }
    }

    private void addEdgeEdger2(TreeSet<Vertex> h1, TreeSet<Vertex> h2) {
        for (Vertex v1 : h1) {
            for (Vertex v2 : h2) {
                // if v1 is connected with v2 draw a line from v1 to v2
                if (isNeighbour(v1, v2)) {
                    addLine(v1, v2);
                }
            }
        }
    }

    private void addLineEdger2(TreeSet<Vertex> h1, TreeSet<Vertex> h2, Edger2Settings es) {

        Vertex[] rv1 = new Vertex[1];
        Vertex[] rv2 = new Vertex[1];
        ArrayList<GData> faces = linkedCommonFaces(h1, h2, rv1, rv2);
        if (faces.size() == 2) {
            Vertex v1 = rv1[0];
            Vertex v2 = rv2[0];

            GData g1 = faces.get(0);
            GData g2 = faces.get(1);

            Vertex v3 = null;
            Vertex v4 = null;

            Vector3d n1;
            Vector3d n2;
            if (g1.type() == 3) {
                GData3 g3 = (GData3) g1;
                Vertex[] vt = triangles.get(g3);
                TreeSet<Vertex> tvs = new TreeSet<Vertex>();
                tvs.add(vt[0]);
                tvs.add(vt[1]);
                tvs.add(vt[2]);
                tvs.remove(v1);
                tvs.remove(v2);
                v3 = tvs.iterator().next();
                n1 = Vector3d.getNormal(new Vector3d(vt[2]), new Vector3d(vt[0]), new Vector3d(vt[1]));
            } else {
                GData4 g4 = (GData4) g1;
                Vertex[] vq = quads.get(g4);
                if (vq[0].equals(v1) && vq[1].equals(v2)) {
                    n1 = Vector3d.getNormal(new Vector3d(vq[2]), new Vector3d(vq[0]), new Vector3d(vq[1])); // T1 1-2-3
                    v3 = vq[2];
                } else if (vq[1].equals(v1) && vq[2].equals(v2)) {
                    n1 = Vector3d.getNormal(new Vector3d(vq[2]), new Vector3d(vq[0]), new Vector3d(vq[1])); // T1 1-2-3
                    v3 = vq[0];
                } else if (vq[2].equals(v1) && vq[3].equals(v2)) {
                    n1 = Vector3d.getNormal(new Vector3d(vq[0]), new Vector3d(vq[1]), new Vector3d(vq[3])); // 22 3-4-1
                    v3 = vq[0];
                } else if (vq[3].equals(v1) && vq[0].equals(v2)) {
                    n1 = Vector3d.getNormal(new Vector3d(vq[0]), new Vector3d(vq[1]), new Vector3d(vq[3])); // 22 3-4-1
                    v3 = vq[2];
                } else if (vq[0].equals(v2) && vq[1].equals(v1)) {
                    n1 = Vector3d.getNormal(new Vector3d(vq[2]), new Vector3d(vq[0]), new Vector3d(vq[1])); // T1 1-2-3
                    v3 = vq[2];
                } else if (vq[1].equals(v2) && vq[2].equals(v1)) {
                    n1 = Vector3d.getNormal(new Vector3d(vq[2]), new Vector3d(vq[0]), new Vector3d(vq[1])); // T1 1-2-3
                    v3 = vq[0];
                } else if (vq[2].equals(v2) && vq[3].equals(v1)) {
                    n1 = Vector3d.getNormal(new Vector3d(vq[0]), new Vector3d(vq[1]), new Vector3d(vq[3])); // T2 3-4-1
                    v3 = vq[0];
                } else {
                    n1 = Vector3d.getNormal(new Vector3d(vq[0]), new Vector3d(vq[1]), new Vector3d(vq[3])); // T2 3-4-1
                    v3 = vq[2];
                }
            }
            if (g2.type() == 3) {
                GData3 g3 = (GData3) g2;
                Vertex[] vt = triangles.get(g3);
                TreeSet<Vertex> tvs = new TreeSet<Vertex>();
                tvs.add(vt[0]);
                tvs.add(vt[1]);
                tvs.add(vt[2]);
                tvs.remove(v1);
                tvs.remove(v2);
                v4 = tvs.iterator().next();
                n2 = Vector3d.getNormal(new Vector3d(vt[2]), new Vector3d(vt[0]), new Vector3d(vt[1]));
            } else {
                GData4 g4 = (GData4) g2;
                Vertex[] vq = quads.get(g4);
                if (vq[0].equals(v1) && vq[1].equals(v2)) {
                    n2 = Vector3d.getNormal(new Vector3d(vq[2]), new Vector3d(vq[0]), new Vector3d(vq[1])); // T1 1-2-3
                    v4 = vq[2];
                } else if (vq[1].equals(v1) && vq[2].equals(v2)) {
                    n2 = Vector3d.getNormal(new Vector3d(vq[2]), new Vector3d(vq[0]), new Vector3d(vq[1])); // T1 1-2-3
                    v4 = vq[0];
                } else if (vq[2].equals(v1) && vq[3].equals(v2)) {
                    n2 = Vector3d.getNormal(new Vector3d(vq[0]), new Vector3d(vq[1]), new Vector3d(vq[3])); // 22 3-4-1
                    v4 = vq[0];
                } else if (vq[3].equals(v1) && vq[0].equals(v2)) {
                    n2 = Vector3d.getNormal(new Vector3d(vq[0]), new Vector3d(vq[1]), new Vector3d(vq[3])); // 22 3-4-1
                    v4 = vq[2];
                } else if (vq[0].equals(v2) && vq[1].equals(v1)) {
                    n2 = Vector3d.getNormal(new Vector3d(vq[2]), new Vector3d(vq[0]), new Vector3d(vq[1])); // T1 1-2-3
                    v4 = vq[2];
                } else if (vq[1].equals(v2) && vq[2].equals(v1)) {
                    n2 = Vector3d.getNormal(new Vector3d(vq[2]), new Vector3d(vq[0]), new Vector3d(vq[1])); // T1 1-2-3
                    v4 = vq[0];
                } else if (vq[2].equals(v2) && vq[3].equals(v1)) {
                    n2 = Vector3d.getNormal(new Vector3d(vq[0]), new Vector3d(vq[1]), new Vector3d(vq[3])); // T2 3-4-1
                    v4 = vq[0];
                } else {
                    n2 = Vector3d.getNormal(new Vector3d(vq[0]), new Vector3d(vq[1]), new Vector3d(vq[3])); // T2 3-4-1
                    v4 = vq[2];
                }
            }

            double angle;
            if (es.isExtendedRange()) {
                if (getBFCorientation(g1) == BFC.CCW) {
                    n1.negate();
                }
                if (getBFCorientation(g2) == BFC.CCW) {
                    n2.negate();
                }
                angle = Vector3d.angle(n1, n2);
            } else {
                angle = Vector3d.angle(n1, n2);
                if(angle > 90.0) angle = 180.0 - angle;
            }

            if (angle <= es.getAf().doubleValue()) {
                // No Line
            } else if (angle > es.getAf().doubleValue() && angle <= es.getAc().doubleValue()) {
                // Condline
                Editor3DWindow.getWindow().setLastUsedColour(View.getLDConfigColour(16));
                addCondline(v1, v2, v3, v4);
            } else if (angle > es.getAc().doubleValue() && angle <= es.getAe().doubleValue()) {
                // Condline + Edge Line
                Editor3DWindow.getWindow().setLastUsedColour(View.getLDConfigColour(16));
                addCondline(v1, v2, v3, v4);
                Editor3DWindow.getWindow().setLastUsedColour(View.getLDConfigColour(2));
                addLine(v1, v2);
            } else {
                // Edge Line
                Editor3DWindow.getWindow().setLastUsedColour(View.getLDConfigColour(16));
                addLine(v1, v2);
            }

        } else {
            Editor3DWindow.getWindow().setLastUsedColour(View.getLDConfigColour(16));
            addLine(h1.iterator().next(), h2.iterator().next());
        }
    }

    private ArrayList<GData> linkedCommonFaces(TreeSet<Vertex> h1, TreeSet<Vertex> h2, Vertex[] rv1, Vertex[] rv2) {
        ArrayList<GData> result = new ArrayList<GData>();
        Set<VertexManifestation> m1 = new HashSet<VertexManifestation>();
        Set<VertexManifestation> m2 = new HashSet<VertexManifestation>();
        for (Vertex v1 : h1) {
            m1.addAll(vertexLinkedToPositionInFile.get(v1));
        }
        for (Vertex v2 : h2) {
            m2.addAll(vertexLinkedToPositionInFile.get(v2));
        }
        for (VertexManifestation a : m1) {
            for (VertexManifestation b : m2) {
                GData bg = b.getGdata();
                if (a.getGdata().equals(bg) && (bg.type() == 3 || bg.type() == 4)) {
                    result.add(bg);
                }
            }
        }
        rv1[0] = h1.iterator().next();
        rv2[0] = h2.iterator().next();
        return result;
    }

    public ArrayList<GData> linkedCommonFaces(Vertex v1, Vertex v2) {
        ArrayList<GData> result = new ArrayList<GData>();
        Set<VertexManifestation> m1 = vertexLinkedToPositionInFile.get(v1);
        Set<VertexManifestation> m2 = vertexLinkedToPositionInFile.get(v2);
        for (VertexManifestation a : m1) {
            for (VertexManifestation b : m2) {
                GData bg = b.getGdata();
                if (a.getGdata().equals(bg) && (bg.type() == 3 || bg.type() == 4)) {
                    result.add(bg);
                }
            }
        }
        return result;
    }

    public boolean isNeighbour(Vertex v1, Vertex v2) {
        Set<VertexManifestation> m1 = vertexLinkedToPositionInFile.get(v1);
        Set<VertexManifestation> m2 = vertexLinkedToPositionInFile.get(v2);
        for (VertexManifestation a : m1) {
            for (VertexManifestation b : m2) {
                if (a.getGdata().equals(b.getGdata())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Tests, if two surfaces share a common edge
     * @param g1 a surface
     * @param g2 another surface
     * @return {@code true} if they do / {@code false} otherwise
     */
    public boolean hasSameEdge(GData g1, GData g2) {

        int t1 = g1.type();
        int t2 = g2.type();

        TreeSet<Vertex> v1 = new TreeSet<Vertex>();
        TreeSet<Vertex> v2 = new TreeSet<Vertex>();

        switch (t1) {
        case 3:
            Vertex[] va = triangles.get(g1);
            v1.add(va[0]);
            v1.add(va[1]);
            v1.add(va[2]);
            break;
        case 4:
            Vertex[] va2 = quads.get(g1);
            v1.add(va2[0]);
            v1.add(va2[1]);
            v1.add(va2[2]);
            v1.add(va2[3]);
            break;
        default:
            return false;
        }

        switch (t2) {
        case 3:
            Vertex[] va = triangles.get(g2);
            v2.add(va[0]);
            v2.add(va[1]);
            v2.add(va[2]);
            break;
        case 4:
            Vertex[] va2 = quads.get(g2);
            v2.add(va2[0]);
            v2.add(va2[1]);
            v2.add(va2[2]);
            v2.add(va2[3]);
            break;
        default:
            return false;
        }

        int co = v1.size();
        v1.removeAll(v2);
        int cn = v1.size();

        return 2 == co - cn;
    }

    private boolean isConnected2(GData g1, GData g2) {

        int t1 = g1.type();
        int t2 = g2.type();

        TreeSet<Vertex> v1 = new TreeSet<Vertex>();
        TreeSet<Vertex> v2 = new TreeSet<Vertex>();

        GData1 p1 = null;
        GData1 p2 = null;

        switch (t1) {
        case 3:
            Vertex[] va = triangles.get(g1);
            v1.add(va[0]);
            v1.add(va[1]);
            v1.add(va[2]);
            p1 = ((GData3) g1).parent;
            break;
        case 4:
            Vertex[] va2 = quads.get(g1);
            v1.add(va2[0]);
            v1.add(va2[1]);
            v1.add(va2[2]);
            v1.add(va2[3]);
            p1 = ((GData4) g1).parent;
            break;
        default:
            return false;
        }

        switch (t2) {
        case 3:
            Vertex[] va = triangles.get(g2);
            v2.add(va[0]);
            v2.add(va[1]);
            v2.add(va[2]);
            p2 = ((GData3) g2).parent;
            break;
        case 4:
            Vertex[] va2 = quads.get(g2);
            v2.add(va2[0]);
            v2.add(va2[1]);
            v2.add(va2[2]);
            v2.add(va2[3]);
            p2 = ((GData4) g2).parent;
            break;
        default:
            return false;
        }

        if (!p1.equals(View.DUMMY_REFERENCE) && p1.equals(p2)) return true;

        int co = v1.size();
        v1.removeAll(v2);
        int cn = v1.size();

        return 2 == co - cn;
    }

    /**
     * Tests, if two surfaces share a common edge, or if a surface has an edge
     * @param g1 a surface
     * @param g2 another surface
     * @param adjaencyByPrecision a map which contains informations about near vertices
     * @return {@code true} if they do / {@code false} otherwise
     */
    private boolean hasSameEdge(GData g1, GData g2, TreeMap<Vertex, TreeSet<Vertex>> adjaencyByPrecision) {

        int t1 = g1.type();
        int t2 = g2.type();

        TreeSet<Vertex> v1 = new TreeSet<Vertex>();
        TreeSet<Vertex> v2 = new TreeSet<Vertex>();


        switch (t1) {
        case 2:
            Vertex[] va0 = lines.get(g1);
            v1.add(va0[0]);
            v1.add(va0[1]);
            break;
        case 3:
            Vertex[] va = triangles.get(g1);
            v1.add(va[0]);
            v1.add(va[1]);
            v1.add(va[2]);
            break;
        case 4:
            Vertex[] va2 = quads.get(g1);
            v1.add(va2[0]);
            v1.add(va2[1]);
            v1.add(va2[2]);
            v1.add(va2[3]);
            break;
        default:
            return false;
        }

        switch (t2) {
        case 2:
            Vertex[] va0 = lines.get(g2);
            v2.add(va0[0]);
            v2.add(va0[1]);
            break;
        case 3:
            Vertex[] va = triangles.get(g2);
            v2.add(va[0]);
            v2.add(va[1]);
            v2.add(va[2]);
            break;
        case 4:
            Vertex[] va2 = quads.get(g2);
            v2.add(va2[0]);
            v2.add(va2[1]);
            v2.add(va2[2]);
            v2.add(va2[3]);
            break;
        default:
            return false;
        }

        // Create the sets

        ArrayList<TreeSet<Vertex>> setList1 = new ArrayList<TreeSet<Vertex>>();
        ArrayList<TreeSet<Vertex>> setList2 = new ArrayList<TreeSet<Vertex>>();

        for (Vertex v : v1) {
            TreeSet<Vertex> newSet = new TreeSet<Vertex>();
            newSet.addAll(adjaencyByPrecision.get(v));
            setList1.add(newSet);
        }

        for (Vertex v : v2) {
            TreeSet<Vertex> newSet = new TreeSet<Vertex>();
            newSet.addAll(adjaencyByPrecision.get(v));
            setList2.add(newSet);
        }

        // Now we have to detect a least 2 set intersections

        int intersections = 0;
        for (TreeSet<Vertex> s1 : setList1) {
            for (TreeSet<Vertex> s2 : setList2) {
                TreeSet<Vertex> newSet = new TreeSet<Vertex>();
                newSet.addAll(s1);
                int co = newSet.size();
                newSet.removeAll(s2);
                int cn = newSet.size();
                if (co != cn) {
                    intersections++;
                    if (intersections == 2) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Tests, if an edge and a surface share a common edge, or if the edge is equal to another edge
     * @param e1 an edge
     * @param g2 a surface
     * @param adjaencyByPrecision a map which contains informations about near vertices
     * @return {@code true} if they do / {@code false} otherwise
     */
    private boolean hasSameEdge(AccurateEdge e1, GData g2, TreeMap<Vertex, TreeSet<Vertex>> adjaencyByPrecision) {

        int t2 = g2.type();

        TreeSet<Vertex> v1 = new TreeSet<Vertex>();
        TreeSet<Vertex> v2 = new TreeSet<Vertex>();

        v1.add(e1.v1);
        v1.add(e1.v2);

        switch (t2) {
        case 2:
            Vertex[] va0 = lines.get(g2);
            v2.add(va0[0]);
            v2.add(va0[1]);
            break;
        case 3:
            Vertex[] va = triangles.get(g2);
            v2.add(va[0]);
            v2.add(va[1]);
            v2.add(va[2]);
            break;
        case 4:
            Vertex[] va2 = quads.get(g2);
            v2.add(va2[0]);
            v2.add(va2[1]);
            v2.add(va2[2]);
            v2.add(va2[3]);
            break;
        default:
            return false;
        }

        // Create the sets

        ArrayList<TreeSet<Vertex>> setList1 = new ArrayList<TreeSet<Vertex>>();
        ArrayList<TreeSet<Vertex>> setList2 = new ArrayList<TreeSet<Vertex>>();

        for (Vertex v : v1) {
            TreeSet<Vertex> newSet = new TreeSet<Vertex>();
            newSet.addAll(adjaencyByPrecision.get(v));
            setList1.add(newSet);
        }

        for (Vertex v : v2) {
            TreeSet<Vertex> newSet = new TreeSet<Vertex>();
            newSet.addAll(adjaencyByPrecision.get(v));
            setList2.add(newSet);
        }

        // Now we have to detect a least 2 set intersections

        int intersections = 0;
        for (TreeSet<Vertex> s1 : setList1) {
            for (TreeSet<Vertex> s2 : setList2) {
                TreeSet<Vertex> newSet = new TreeSet<Vertex>();
                newSet.addAll(s1);
                int co = newSet.size();
                newSet.removeAll(s2);
                int cn = newSet.size();
                if (co != cn) {
                    intersections++;
                    if (intersections == 2) {
                        return true;
                    }
                }
            }
        }

        return false;
    }


    public GData2 hasEdge(Vertex v1, Vertex v2) {
        Set<VertexManifestation> m1 = vertexLinkedToPositionInFile.get(v1);
        Set<VertexManifestation> m2 = vertexLinkedToPositionInFile.get(v2);
        for (VertexManifestation a : m1) {
            for (VertexManifestation b : m2) {
                if (a.getGdata().equals(b.getGdata()) && b.getGdata().type() == 2) {
                    if (!lineLinkedToVertices.containsKey(b.getGdata())) return null;
                    return (GData2) b.getGdata();
                }
            }
        }
        return null;
    }

    public GData5 hasCondline(Vertex v1, Vertex v2) {
        Set<VertexManifestation> m1 = vertexLinkedToPositionInFile.get(v1);
        Set<VertexManifestation> m2 = vertexLinkedToPositionInFile.get(v2);
        for (VertexManifestation a : m1) {
            if (a.getPosition() > 1) continue;
            for (VertexManifestation b : m2) {
                if (b.getPosition() > 1) continue;
                if (a.getGdata().equals(b.getGdata()) && b.getGdata().type() == 5) {
                    if (!lineLinkedToVertices.containsKey(b.getGdata())) return null;
                    return (GData5) b.getGdata();
                }
            }
        }
        return null;
    }

    public boolean isNotInSubfileAndLinetype2to5(GData g) {
        if (!exist(g) || !lineLinkedToVertices.containsKey(g)) {
            return false;
        }
        switch (g.type()) {
        case 2:
        case 3:
        case 4:
        case 5:
            return true;
        default:
            return false;
        }
    }

    public BigDecimal[] getPreciseCoordinates(GData g) {
        switch (g.type()) {
        case 2:
            GData2 g2 = (GData2) g;
            return new BigDecimal[]{g2.X1, g2.Y1, g2.Z1, g2.X2, g2.Y2, g2.Z2};
        case 3:
            GData3 g3 = (GData3) g;
            return new BigDecimal[]{g3.X1, g3.Y1, g3.Z1, g3.X2, g3.Y2, g3.Z2, g3.X3, g3.Y3, g3.Z3};
        case 4:
            GData4 g4 = (GData4) g;
            return new BigDecimal[]{g4.X1, g4.Y1, g4.Z1, g4.X2, g4.Y2, g4.Z2, g4.X3, g4.Y3, g4.Z3, g4.X4, g4.Y4, g4.Z4};
        case 5:
            GData5 g5 = (GData5) g;
            return new BigDecimal[]{g5.X1, g5.Y1, g5.Z1, g5.X2, g5.Y2, g5.Z2, g5.X3, g5.Y3, g5.Z3, g5.X4, g5.Y4, g5.Z4};
        default:
            return null;
        }
    }


    public void initBFCmap() {
        linkedDatFile.getBFCorientationMap(bfcMap);
    }

    public void disposeBFCmap() {
        bfcMap.clear();
    }

    /**
     * Investigates the BFC orientation of type 2-5 lines
     * @param g the data to analyse
     * @return {@code BFC.CW|BFC.CCW|BFC.NOCERTIFY|BFC.NOCLIP}
     */
    public byte getBFCorientation(GData g) {
        if (bfcMap.containsKey(g)) {
            return bfcMap.get(g);
        }
        return BFC.NOCERTIFY;
    }

    public GColour getRandomSelectedColour(GColour lastUsedColour) {
        if (!selectedSubfiles.isEmpty()) {
            GData1 g1 = selectedSubfiles.iterator().next();
            lastUsedColour = new GColour(g1.colourNumber, g1.r, g1.g, g1.b, g1.a);
            if (Math.random() > .5) return lastUsedColour;
        }
        if (!selectedLines.isEmpty()) {
            GData2 g2 = selectedLines.iterator().next();
            lastUsedColour = new GColour(g2.colourNumber, g2.r, g2.g, g2.b, g2.a);
            if (Math.random() > .5) return lastUsedColour;
        }
        if (!selectedTriangles.isEmpty()) {
            GData3 g3 = selectedTriangles.iterator().next();
            lastUsedColour = new GColour(g3.colourNumber, g3.r, g3.g, g3.b, g3.a);
            if (Math.random() > .5) return lastUsedColour;
        }
        if (!selectedQuads.isEmpty()) {
            GData4 g4 = selectedQuads.iterator().next();
            lastUsedColour = new GColour(g4.colourNumber, g4.r, g4.g, g4.b, g4.a);
            if (Math.random() > .5) return lastUsedColour;
        }
        if (!selectedCondlines.isEmpty()) {
            GData5 g5 = selectedCondlines.iterator().next();
            lastUsedColour = new GColour(g5.colourNumber, g5.r, g5.g, g5.b, g5.a);
            if (Math.random() > .5) return lastUsedColour;
        }
        return lastUsedColour;
    }

    public GDataPNG getSelectedBgPicture() {
        return selectedBgPicture;
    }

    public void setSelectedBgPicture(GDataPNG pic) {
        selectedBgPicture = pic;
    }

    public int getSelectedBgPictureIndex() {
        return selectedBgPictureIndex;
    }

    public void setSelectedBgPictureIndex(int selectedBgPictureIndex) {
        this.selectedBgPictureIndex = selectedBgPictureIndex;
    }

    public void setVertexAndNormal(float x, float y, float z, boolean negate, GData gd, int useCubeMapCache) {
        boolean useCache = useCubeMapCache > 0;
        // TODO Needs better caching since the connectivity information of TEXMAP data is unknown and the orientation of the normals can vary.
        Vector4f v;
        switch (gd.type()) {
        case 3:
            v = new Vector4f(x, y, z, 1f);
            Matrix4f.transform(((GData3) gd).parent.productMatrix, v, v);
            break;
        case 4:
            v = new Vector4f(x, y, z, 1f);
            Matrix4f.transform(((GData4) gd).parent.productMatrix, v, v);
            break;
        default:
            throw new AssertionError();
        }
        if (useCache) {
            float[] n;
            if ((n = vertexLinkedToNormalCACHE.get(new Vertex(v.x, v.y, v.z, false))) != null) {
                GL11.glNormal3f(-n[0], -n[1], -n[2]);
            } else {
                n = dataLinkedToNormalCACHE.get(gd);
                if (n != null) {
                    if (negate) {
                        GL11.glNormal3f(-n[0], -n[1], -n[2]);
                    } else {
                        GL11.glNormal3f(n[0], n[1], n[2]);
                    }
                }
            }
        } else {
            float[] n = dataLinkedToNormalCACHE.get(gd);
            if (n != null) {
                if (negate) {
                    GL11.glNormal3f(-n[0], -n[1], -n[2]);
                } else {
                    GL11.glNormal3f(n[0], n[1], n[2]);
                }
            }
        }
        GL11.glVertex3f(v.x, v.y, v.z);
    }

    public void setVertex(float x, float y, float z, GData gd, boolean useCache) {
        // TODO Needs better caching since the connectivity information of TEXMAP data is unknown and the orientation of the normals can vary.
        if (useCache) {
            Vector4f v;
            switch (gd.type()) {
            case 2:
                v = new Vector4f(x, y, z, 1f);
                Matrix4f.transform(((GData2) gd).parent.productMatrix, v, v);
                break;
            case 3:
                v = new Vector4f(x, y, z, 1f);
                Matrix4f.transform(((GData3) gd).parent.productMatrix, v, v);
                break;
            case 4:
                v = new Vector4f(x, y, z, 1f);
                Matrix4f.transform(((GData4) gd).parent.productMatrix, v, v);
                break;
            case 5:
                v = new Vector4f(x, y, z, 1f);
                Matrix4f.transform(((GData5) gd).parent.productMatrix, v, v);
                break;
            default:
                throw new AssertionError();
            }
            GL11.glVertex3f(v.x, v.y, v.z);
        } else {
            GL11.glVertex3f(x, y, z);
        }
    }

    public void fillVertexNormalCache(GData data2draw) {
        while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
            data2draw.getVertexNormalMap(vertexLinkedToNormalCACHE, dataLinkedToNormalCACHE, this);
        }

    }

    public void clearVertexNormalCache() {
        vertexLinkedToNormalCACHE.clear();
        dataLinkedToNormalCACHE.clear();
    }

    public ArrayList<ParsingResult> checkForFlatScaling(GData1 ref) {
        ArrayList<ParsingResult> result = new ArrayList<ParsingResult>();

        Matrix4f tMatrix = (Matrix4f) ref.accurateLocalMatrix.getMatrix4f().invert();

        boolean plainOnX = true;
        boolean plainOnY = true;
        boolean plainOnZ = true;

        Set<VertexInfo> verts = lineLinkedToVertices.get(ref);
        if (verts == null) return result;
        for (VertexInfo vi : verts) {
            Vector4f vert = vi.vertex.toVector4fm();
            Vector4f vert2 = Matrix4f.transform(tMatrix, vert, null);

            if (plainOnX && Math.abs(vert2.x) > 0.001f) {
                plainOnX = false;
            }
            if (plainOnY && Math.abs(vert2.y) > 0.001f) {
                plainOnY = false;
            }
            if (plainOnZ && Math.abs(vert2.z) > 0.001f) {
                plainOnZ = false;
            }
            if (!plainOnX && !plainOnY && !plainOnZ) {
                return result;
            }
        }

        Matrix TMatrix2 = ref.accurateLocalMatrix;
        final BigDecimal lengthX =  MathHelper.sqrt(TMatrix2.M00.multiply(TMatrix2.M00).add(TMatrix2.M01.multiply(TMatrix2.M01)).add(TMatrix2.M02.multiply(TMatrix2.M02))).subtract(BigDecimal.ONE).abs();
        final BigDecimal lengthY =  MathHelper.sqrt(TMatrix2.M10.multiply(TMatrix2.M10).add(TMatrix2.M11.multiply(TMatrix2.M11)).add(TMatrix2.M12.multiply(TMatrix2.M12))).subtract(BigDecimal.ONE).abs();
        final BigDecimal lengthZ =  MathHelper.sqrt(TMatrix2.M20.multiply(TMatrix2.M20).add(TMatrix2.M21.multiply(TMatrix2.M21)).add(TMatrix2.M22.multiply(TMatrix2.M22))).subtract(BigDecimal.ONE).abs();
        final BigDecimal epsilon = new BigDecimal("0.000001"); //$NON-NLS-1$

        if (plainOnX && epsilon.compareTo(lengthX) < 0) {
            result.add(new ParsingResult("The flat subfile is scaled in X direction.", "[W02] Warning", ResultType.WARN)); //$NON-NLS-1$ //$NON-NLS-2$ // I18N Needs translation!
        }
        if (plainOnY && epsilon.compareTo(lengthY) < 0) {
            result.add(new ParsingResult("The flat subfile is scaled in Y direction.", "[W03] Warning", ResultType.WARN)); //$NON-NLS-1$ //$NON-NLS-2$ // I18N Needs translation!
        }
        if (plainOnZ && epsilon.compareTo(lengthZ) < 0) {
            result.add(new ParsingResult("The flat subfile is scaled in Z direction.", "[W04] Warning", ResultType.WARN)); //$NON-NLS-1$ //$NON-NLS-2$ // I18N Needs translation!
        }


        return result;
    }

    public int getSelectedItemIndex() {
        return selectedItemIndex;
    }

    public void setSelectedItemIndex(int selectedItemIndex) {
        this.selectedItemIndex = selectedItemIndex;
    }

    public GData getSelectedLine() {
        return selectedLine;
    }

    public void setSelectedLine(GData selectedLine) {
        this.selectedLine = selectedLine;
    }

    public GData updateSelectedLine(
            BigDecimal x1, BigDecimal y1, BigDecimal z1,
            BigDecimal x2, BigDecimal y2, BigDecimal z2,
            BigDecimal x3, BigDecimal y3, BigDecimal z3,
            BigDecimal x4, BigDecimal y4, BigDecimal z4, boolean moveAdjacentData) {

        if (selectedItemIndex != -1 && exist(selectedLine)) {

            if (moveAdjacentData) {

                final Vertex v1 = new Vertex(x1, y1, z1);
                final Vertex v2 = new Vertex(x2, y2, z2);
                final Vertex v3 = new Vertex(x3, y3, z3);
                final Vertex v4 = new Vertex(x4, y4, z4);

                Vertex oldVertex = null;
                Vertex newVertex = null;

                TreeSet<Vertex> ov = new TreeSet<Vertex>();
                TreeSet<Vertex> nv = new TreeSet<Vertex>();

                switch (selectedLine.type()) {
                case 5:
                case 4:
                    nv.add(v4);
                case 3:
                    nv.add(v3);
                case 2:
                    nv.add(v1);
                    nv.add(v2);
                    break;
                default:
                    return null;
                }

                Set<VertexInfo> vi = lineLinkedToVertices.get(selectedLine);
                if (vi != null) {
                    for (VertexInfo v : vi) {
                        ov.add(v.vertex);
                    }
                } else {
                    return null;
                }

                TreeSet<Vertex> nv2 = new TreeSet<Vertex>(nv);
                nv2.removeAll(ov);
                ov.removeAll(nv);

                if (nv2.size() != 1 || ov.size() != 1) {
                    return selectedLine;
                }

                oldVertex = ov.iterator().next();
                newVertex = nv2.iterator().next();

                GData nl = changeVertexDirectFast(oldVertex, newVertex, moveAdjacentData, selectedLine);

                if (nl != null) {
                    selectedLine = nl;
                }

                selectedData.clear();
                selectedData.addAll(selectedLines);
                selectedData.addAll(selectedTriangles);
                selectedData.addAll(selectedQuads);
                selectedData.addAll(selectedCondlines);
                selectedData.addAll(selectedSubfiles);
            } else {
                final GData before = selectedLine.getBefore();
                final GData next = selectedLine.getNext();
                final HashBiMap<Integer, GData> drawPerLine = linkedDatFile.getDrawPerLine_NOCLONE();
                GData newData = null;
                switch (selectedLine.type()) {
                case 2:
                    GData2 g2 = (GData2) selectedLine;
                    selectedLines.remove(g2);
                    selectedData.remove(g2);
                    newData = new GData2(g2.colourNumber, g2.r, g2.g, g2.b, g2.a, new Vertex(x1, y1, z1), new Vertex(x2, y2, z2), g2.parent, linkedDatFile);
                    selectedLines.add((GData2) newData);
                    selectedData.add(newData);
                    break;
                case 3:
                    GData3 g3 = (GData3) selectedLine;
                    selectedTriangles.remove(g3);
                    selectedData.remove(g3);
                    newData = new GData3(g3.colourNumber, g3.r, g3.g, g3.b, g3.a, new Vertex(x1, y1, z1), new Vertex(x2, y2, z2), new Vertex(x3, y3, z3), g3.parent, linkedDatFile);
                    selectedTriangles.add((GData3) newData);
                    selectedData.add(newData);
                    break;
                case 4:
                    GData4 g4 = (GData4) selectedLine;
                    selectedQuads.remove(g4);
                    selectedData.remove(g4);
                    newData = new GData4(g4.colourNumber, g4.r, g4.g, g4.b, g4.a, new Vertex(x1, y1, z1), new Vertex(x2, y2, z2), new Vertex(x3, y3, z3), new Vertex(x4, y4, z4), g4.parent, linkedDatFile);
                    selectedQuads.add((GData4) newData);
                    selectedData.add(newData);
                    break;
                case 5:
                    GData5 g5 = (GData5) selectedLine;
                    selectedCondlines.remove(g5);
                    selectedData.remove(g5);
                    newData = new GData5(g5.colourNumber, g5.r, g5.g, g5.b, g5.a, new Vertex(x1, y1, z1), new Vertex(x2, y2, z2), new Vertex(x3, y3, z3), new Vertex(x4, y4, z4), g5.parent, linkedDatFile);
                    selectedCondlines.add((GData5) newData);
                    selectedData.add(newData);
                    break;
                default:
                    return null;
                }
                before.setNext(newData);
                newData.setNext(next);
                drawPerLine.put(drawPerLine.getKey(selectedLine), newData);
                if (remove(selectedLine)) {
                    linkedDatFile.setDrawChainTail(newData);
                }
                selectedLine = newData;
            }
            setModified(true, true);
            updateUnsavedStatus();
            return selectedLine;
        }
        return null;

    }

    public double[] calculateAreas(Set<GData> finalTriangleSet) {
        double[] ca = new double[2];
        for (GData g : finalTriangleSet) {
            GData3 t = (GData3) g;

            Vector3d vertexA = new Vector3d();
            Vector3d vertexB = new Vector3d();
            Vector3d vertexC = new Vector3d();


            Vector3d vertexA2 = new Vector3d();
            Vector3d vertexB2 = new Vector3d();

            vertexA.setX(t.X1);
            vertexA.setY(t.Y1);
            vertexA.setZ(t.Z1);

            vertexB.setX(t.X2);
            vertexB.setY(t.Y2);
            vertexB.setZ(t.Z2);

            vertexC.setX(t.X3);
            vertexC.setY(t.Y3);
            vertexC.setZ(t.Z3);

            Vector3d.sub(vertexA, vertexC, vertexA2);
            Vector3d.sub(vertexB, vertexC, vertexB2);
            if (Vector3d.angle(vertexA2, vertexB2) < Threshold.collinear_angle_minimum) {
                ca[0] = -1;
                ca[1] = -1;
                return ca;
            }

            double a = Math.sqrt(Math.pow(t.x1 - t.x2, 2) + Math.pow(t.y1 - t.y2, 2));
            double b = Math.sqrt(Math.pow(t.x3 - t.x2, 2) + Math.pow(t.y3 - t.y2, 2));
            double c = Math.sqrt(Math.pow(t.x1 - t.x3, 2) + Math.pow(t.y1 - t.y3, 2));

            double s = (a + b + c) / 2.0;

            double f = Math.sqrt(s * (s - a) * (s - b) * (s - c));

            if (t.r == 0.1f) {
                ca[0] = ca[0] + f;
            } else {
                ca[1] = ca[1] + f;
            }

        }
        return ca;
    }

    public void selectTriangles(Set<GData> finalTriangleSet) {
        selectedData.addAll(finalTriangleSet);
        for (GData gData : finalTriangleSet) {
            selectedTriangles.add((GData3) gData);
        }
    }

    public void restoreTriangles(Set<GData> finalTriangleSet) {
        finalTriangleSet.clear();
        for (GData3 gData : selectedTriangles) {
            finalTriangleSet.add(gData);
        }
    }

    public void rectify(final RectifierSettings rs, boolean syncWithTextEditor) {

        if (linkedDatFile.isReadOnly()) return;

        final double targetAngle = rs.getMaximumAngle().doubleValue();
        final boolean colourise = rs.isColourise();

        final BigDecimal TWO = new BigDecimal(2);

        final Set<GData3> trisToIgnore = new HashSet<GData3>();

        final Set<GData2> linesToDelete = new HashSet<GData2>();
        final Set<GData4> quadsToDelete = new HashSet<GData4>();
        final Set<GData5> clinesToDelete = new HashSet<GData5>();

        final Set<GData> surfsToParse = new HashSet<GData>();

        if (rs.getScope() == 0) {
            surfsToParse.addAll(triangles.keySet());
            surfsToParse.addAll(quads.keySet());
        } else {
            surfsToParse.addAll(selectedTriangles);
            surfsToParse.addAll(selectedQuads);
        }

        clearSelection();

        for (Iterator<GData> ig = surfsToParse.iterator(); ig.hasNext();) {
            GData g = ig.next();
            if (!lineLinkedToVertices.containsKey(g)) {
                ig.remove();
            }
        }

        final boolean noAdjacentCondlines = rs.isNoBorderedQuadToRectConversation();
        final boolean replaceQuads = !rs.isNoQuadConversation();

        try
        {
            new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
            {
                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                {
                    try
                    {
                        monitor.beginTask("Rectify...", IProgressMonitor.UNKNOWN); //$NON-NLS-1$ I18N
                        for (GData g : surfsToParse) {
                            /* Check if the monitor has been canceled */
                            if (monitor.isCanceled()) break;
                            if (trisToIgnore.contains(g)) continue;
                            GData quad = null;
                            if (g.type() == 3 && replaceQuads) {

                                GData3 tri = (GData3) g;
                                Vertex[] v = triangles.get(tri);

                                @SuppressWarnings("unchecked")
                                ArrayList<GData>[] cf = new ArrayList[3];

                                cf[0] = linkedCommonFaces(v[0], v[1]);
                                cf[1] = linkedCommonFaces(v[0], v[2]);
                                cf[2] = linkedCommonFaces(v[1], v[2]);

                                int bestIndex = -1;
                                BigDecimal bestRatio = null;
                                for(int i = 0; i < 3; i++) {
                                    if (cf[i].size() == 2 && cf[i].get(0).type() == 3 && cf[i].get(1).type() == 3) {
                                        GData3 tri1 = (GData3) cf[i].get(0);
                                        GData3 tri2 = (GData3) cf[i].get(1);
                                        if (tri1.parent.equals(View.DUMMY_REFERENCE) && tri2.parent.equals(View.DUMMY_REFERENCE) && tri1.colourNumber == tri2.colourNumber && (tri1.colourNumber != -1 || tri1.r == tri2.r && tri1.g == tri2.g && tri1.b == tri2.b && tri1.a == tri2.a)) {

                                            TreeSet<Vertex> tri1V = new TreeSet<Vertex>();
                                            TreeSet<Vertex> tri2V = new TreeSet<Vertex>();
                                            TreeSet<Vertex> triC = new TreeSet<Vertex>();

                                            Vertex[] v1 = triangles.get(tri1);
                                            Vertex[] v2 = triangles.get(tri2);

                                            for (Vertex ve : v1) {
                                                tri1V.add(ve);
                                            }
                                            for (Vertex ve : v2) {
                                                tri2V.add(ve);
                                            }
                                            triC.addAll(tri1V);
                                            triC.retainAll(tri2V);
                                            tri2V.removeAll(tri1V);

                                            tri1V.removeAll(triC);
                                            tri1V.removeAll(tri2V);

                                            if (tri2V.iterator().hasNext() && tri1V.iterator().hasNext() && triC.size() == 2) {

                                                Vector3d n1 = new Vector3d(
                                                        tri1.Y3.subtract(tri1.Y1).multiply(tri1.Z2.subtract(tri1.Z1)).subtract(tri1.Z3.subtract(tri1.Z1).multiply(tri1.Y2.subtract(tri1.Y1))),
                                                        tri1.Z3.subtract(tri1.Z1).multiply(tri1.X2.subtract(tri1.X1)).subtract(tri1.X3.subtract(tri1.X1).multiply(tri1.Z2.subtract(tri1.Z1))),
                                                        tri1.X3.subtract(tri1.X1).multiply(tri1.Y2.subtract(tri1.Y1)).subtract(tri1.Y3.subtract(tri1.Y1).multiply(tri1.X2.subtract(tri1.X1)))
                                                        );
                                                Vector3d n2 = new Vector3d(
                                                        tri2.Y3.subtract(tri2.Y1).multiply(tri2.Z2.subtract(tri2.Z1)).subtract(tri2.Z3.subtract(tri2.Z1).multiply(tri2.Y2.subtract(tri2.Y1))),
                                                        tri2.Z3.subtract(tri2.Z1).multiply(tri2.X2.subtract(tri2.X1)).subtract(tri2.X3.subtract(tri2.X1).multiply(tri2.Z2.subtract(tri2.Z1))),
                                                        tri2.X3.subtract(tri2.X1).multiply(tri2.Y2.subtract(tri2.Y1)).subtract(tri2.Y3.subtract(tri2.Y1).multiply(tri2.X2.subtract(tri2.X1)))
                                                        );

                                                double angle = Vector3d.angle(n1, n2);
                                                angle = Math.min(angle, 180.0 - angle);
                                                if (angle <= targetAngle) {

                                                    Vertex first = tri1V.iterator().next();
                                                    Vertex third = tri2V.iterator().next();

                                                    Vertex second = null;
                                                    {
                                                        boolean firstFound = false;
                                                        for (Vertex ve : v1) {
                                                            if (firstFound) {
                                                                if (triC.contains(ve)) {
                                                                    second = ve;
                                                                    break;
                                                                }
                                                            } else if (ve.equals(first)) {
                                                                firstFound = true;
                                                            }
                                                        }
                                                        if (second == null) {
                                                            second = v1[0];
                                                        }
                                                    }

                                                    Vertex fourth = null;
                                                    for (Vertex ve : v1) {
                                                        if (triC.contains(ve) && !ve.equals(second)) {
                                                            fourth = ve;
                                                        }
                                                    }

                                                    Vector3d[] normals = new Vector3d[4];
                                                    float[] normalDirections = new float[4];
                                                    Vector3d[] lineVectors = new Vector3d[4];
                                                    int cnc = 0;
                                                    Vector3d vertexA = new Vector3d(first);
                                                    Vector3d vertexB = new Vector3d(second);
                                                    Vector3d vertexC = new Vector3d(third);
                                                    Vector3d vertexD = new Vector3d(fourth);
                                                    lineVectors[0] = Vector3d.sub(vertexB, vertexA);
                                                    lineVectors[1] = Vector3d.sub(vertexC, vertexB);
                                                    lineVectors[2] = Vector3d.sub(vertexD, vertexC);
                                                    lineVectors[3] = Vector3d.sub(vertexA, vertexD);
                                                    normals[0] = Vector3d.cross(lineVectors[0], lineVectors[1]);
                                                    normals[1] = Vector3d.cross(lineVectors[1], lineVectors[2]);
                                                    normals[2] = Vector3d.cross(lineVectors[2], lineVectors[3]);
                                                    normals[3] = Vector3d.cross(lineVectors[3], lineVectors[0]);
                                                    for (int k = 0; k < 4; k++) {
                                                        normalDirections[k] = MathHelper.directionOfVectors(normals[0], normals[k]);
                                                        if (normalDirections[k] < 0) {
                                                            cnc++;
                                                        }
                                                    }

                                                    if (cnc == 1 || cnc == 3 || !linkedDatFile.getDrawPerLine_NOCLONE().containsValue(tri2)) {
                                                        // Concave
                                                        continue;
                                                    }

                                                    Vector3d.sub(vertexA, vertexD, vertexA);
                                                    Vector3d.sub(vertexB, vertexD, vertexB);
                                                    Vector3d.sub(vertexC, vertexD, vertexC);

                                                    boolean parseError = false;
                                                    angle = Vector3d.angle(vertexA, vertexB); // AB
                                                    parseError = angle < Threshold.collinear_angle_minimum;
                                                    angle = Vector3d.angle(vertexB, vertexC); // BC
                                                    parseError = parseError || angle < Threshold.collinear_angle_minimum;
                                                    angle = 180.0 - Vector3d.angle(vertexA, vertexC); // 180 - AC
                                                    parseError = parseError || angle < Threshold.collinear_angle_minimum;
                                                    angle = 180.0 - Vector3d.angle(Vector3d.sub(vertexC, vertexB), Vector3d.sub(vertexA, vertexB)); // 180 - (C-B)(A-B)
                                                    parseError = parseError || angle < Threshold.collinear_angle_minimum;
                                                    if (parseError) {
                                                        continue;
                                                    }

                                                    angle = Vector3d.angle(normals[0], normals[2]);
                                                    if (angle > targetAngle) continue;

                                                    BigDecimal m1 = Vector3d.distSquare(new Vector3d(first), new Vector3d(third)).add(BigDecimal.ONE);
                                                    BigDecimal m2 = Vector3d.distSquare(new Vector3d(second), new Vector3d(fourth)).add(BigDecimal.ONE);
                                                    BigDecimal ratio = m1.compareTo(m2) > 0 ? m1.divide(m2, Threshold.mc) : m2.divide(m1, Threshold.mc);
                                                    if (bestIndex == -1) {
                                                        bestRatio = ratio;
                                                        bestIndex = i;
                                                    } else if (ratio.compareTo(bestRatio) < 0) {
                                                        bestRatio = ratio;
                                                        bestIndex = i;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (bestIndex != -1) {
                                    GData3 tri1 = (GData3) cf[bestIndex].get(0);
                                    GData3 tri2 = (GData3) cf[bestIndex].get(1);

                                    TreeSet<Vertex> tri1V = new TreeSet<Vertex>();
                                    TreeSet<Vertex> tri2V = new TreeSet<Vertex>();
                                    TreeSet<Vertex> triC = new TreeSet<Vertex>();

                                    Vertex[] v1 = triangles.get(tri1);
                                    Vertex[] v2 = triangles.get(tri2);

                                    for (Vertex ve : v1) {
                                        tri1V.add(ve);
                                    }
                                    for (Vertex ve : v2) {
                                        tri2V.add(ve);
                                    }
                                    triC.addAll(tri1V);
                                    triC.retainAll(tri2V);
                                    tri2V.removeAll(tri1V);

                                    tri1V.removeAll(triC);
                                    tri1V.removeAll(tri2V);

                                    if (tri2V.iterator().hasNext() && tri1V.iterator().hasNext() && triC.size() == 2) {

                                        Vertex first = tri1V.iterator().next();
                                        Vertex third = tri2V.iterator().next();

                                        Vertex second = null;
                                        {
                                            boolean firstFound = false;
                                            for (Vertex ve : v1) {
                                                if (firstFound) {
                                                    if (triC.contains(ve)) {
                                                        second = ve;
                                                        break;
                                                    }
                                                } else if (ve.equals(first)) {
                                                    firstFound = true;
                                                }
                                            }
                                            if (second == null) {
                                                second = v1[0];
                                            }
                                        }

                                        Vertex fourth = null;
                                        for (Vertex ve : v1) {
                                            if (triC.contains(ve) && !ve.equals(second)) {
                                                fourth = ve;
                                            }
                                        }

                                        Set<GData> lines1 = new HashSet<GData>();
                                        Set<GData> lines2 = new HashSet<GData>();
                                        Set<GData> lines3 = new HashSet<GData>();
                                        Set<GData> lines4 = new HashSet<GData>();


                                        {
                                            Set<VertexManifestation> s1 = vertexLinkedToPositionInFile.get(first);
                                            for (VertexManifestation m : s1) {
                                                GData gd = m.getGdata();
                                                int t = gd.type();
                                                if (lineLinkedToVertices.containsKey(gd) && m.getPosition() < 2 && (t == 2 || t == 5)) lines1.add(gd);
                                            }
                                        }
                                        {
                                            Set<VertexManifestation> s2 = vertexLinkedToPositionInFile.get(second);
                                            for (VertexManifestation m : s2) {
                                                GData gd = m.getGdata();
                                                int t = gd.type();
                                                if (lineLinkedToVertices.containsKey(gd) && m.getPosition() < 2 && (t == 2 || t == 5)) lines2.add(gd);
                                            }
                                        }
                                        {
                                            Set<VertexManifestation> s3 = vertexLinkedToPositionInFile.get(third);
                                            for (VertexManifestation m : s3) {
                                                GData gd = m.getGdata();
                                                int t = gd.type();
                                                if (lineLinkedToVertices.containsKey(gd) && m.getPosition() < 2 && (t == 2 || t == 5)) lines3.add(gd);
                                            }
                                        }
                                        {
                                            Set<VertexManifestation> s4 = vertexLinkedToPositionInFile.get(fourth);
                                            for (VertexManifestation m : s4) {
                                                GData gd = m.getGdata();
                                                int t = gd.type();
                                                if (lineLinkedToVertices.containsKey(gd) && m.getPosition() < 2 &&  (t == 2 || t == 5)) lines4.add(gd);
                                            }
                                        }

                                        if (colourise) {
                                            GColour yellow = View.hasLDConfigColour(14) ? View.getLDConfigColour(14) : new GColour(-1, 1f, 1f, 0f, 1f);

                                            quad = new GData4(yellow.getColourNumber(), yellow.getR(), yellow.getG(), yellow.getB(), yellow.getA(), first, second, third, fourth, View.DUMMY_REFERENCE, linkedDatFile);

                                            linkedDatFile.insertAfter(tri2, quad);
                                            setModified_NoSync();
                                        } else {

                                            quad = new GData4(tri1.colourNumber, tri1.r, tri1.g, tri1.b, tri1.a, first, second, third, fourth, View.DUMMY_REFERENCE, linkedDatFile);

                                            linkedDatFile.insertAfter(tri2, quad);
                                            setModified_NoSync();
                                        }



                                        lines1.retainAll(lines3);
                                        lines2.retainAll(lines4);
                                        lines1.addAll(lines2);
                                        for (GData gData : lines1) {
                                            int t = gData.type();
                                            switch (t) {
                                            // case 2:
                                            //  linesToDelete.add((GData2) gData);
                                            //  break;
                                            case 5:
                                                clinesToDelete.add((GData5) gData);
                                                break;
                                            default:
                                                break;
                                            }
                                        }

                                        trisToIgnore.add(tri1);
                                        trisToIgnore.add(tri2);
                                    }
                                }
                            }
                            if (quad != null || g.type() == 4) {
                                if (rs.isNoBorderedQuadToRectConversation()) continue;
                                if (quad == null) {
                                    quad = g;
                                }
                                GData4 qa = (GData4) quad;

                                BigDecimal d1X = qa.X1.add(qa.X3).divide(TWO);
                                BigDecimal d2X = qa.X2.add(qa.X4).divide(TWO);
                                if (d1X.compareTo(d2X) == 0) {
                                    BigDecimal d1Y = qa.Y1.add(qa.Y3).divide(TWO);
                                    BigDecimal d2Y = qa.Y2.add(qa.Y4).divide(TWO);
                                    if (d1Y.compareTo(d2Y) == 0) {
                                        BigDecimal d1Z = qa.Z1.add(qa.Z3).divide(TWO);
                                        BigDecimal d2Z = qa.Z2.add(qa.Z4).divide(TWO);
                                        if (d1Z.compareTo(d2Z) == 0) {

                                            // Its a rhombus!

                                            Vertex[] vq = quads.get(qa);
                                            GData2 e1 = hasEdge(vq[0], vq[1]);
                                            GData2 e2 = hasEdge(vq[1], vq[2]);
                                            GData2 e3 = hasEdge(vq[2], vq[3]);
                                            GData2 e4 = hasEdge(vq[3], vq[0]);

                                            if (noAdjacentCondlines && hasCondline(vq[0], vq[1]) != null || hasCondline(vq[1], vq[2])  != null || hasCondline(vq[2], vq[3]) != null || hasCondline(vq[3], vq[0]) != null) {
                                                continue;
                                            }

                                            int edgeflags =  (e1 != null ? 1 : 0) + (e2 != null ? 2 : 0) + (e3 != null ? 4 : 0) + (e4 != null ? 8 : 0);

                                            Matrix accurateLocalMatrix = null;
                                            String shortName = null;

                                            switch(edgeflags)
                                            {
                                            case 0:
                                                continue;
                                            case 1:
                                                vq = ROTQUAD(vq);
                                                vq = ROTQUAD(vq);
                                                vq = ROTQUAD(vq);
                                                shortName = "rect1.dat"; //$NON-NLS-1$
                                                break;
                                            case 2:
                                                shortName = "rect1.dat"; //$NON-NLS-1$
                                                break;
                                            case 4:
                                                vq = ROTQUAD(vq);
                                                shortName = "rect1.dat"; //$NON-NLS-1$
                                                break;
                                            case 8:
                                                vq = ROTQUAD(vq);
                                                vq = ROTQUAD(vq);
                                                shortName = "rect1.dat"; //$NON-NLS-1$
                                                break;
                                            case 3:
                                                vq = ROTQUAD(vq);
                                                vq = ROTQUAD(vq);
                                                vq = ROTQUAD(vq);
                                                shortName = "rect2a.dat"; //$NON-NLS-1$
                                                break;
                                            case 6:
                                                shortName = "rect2a.dat"; //$NON-NLS-1$
                                                break;
                                            case 12:
                                                vq = ROTQUAD(vq);
                                                shortName = "rect2a.dat"; //$NON-NLS-1$
                                                break;
                                            case 9:
                                                vq = ROTQUAD(vq);
                                                vq = ROTQUAD(vq);
                                                shortName = "rect2a.dat"; //$NON-NLS-1$
                                                break;
                                            case 5:
                                                shortName = "rect2p.dat"; //$NON-NLS-1$
                                                break;
                                            case 10:
                                                vq = ROTQUAD(vq);
                                                shortName = "rect2p.dat"; //$NON-NLS-1$
                                                break;
                                            case 7:
                                                vq = ROTQUAD(vq);
                                                vq = ROTQUAD(vq);
                                                vq = ROTQUAD(vq);
                                                shortName = "rect3.dat"; //$NON-NLS-1$
                                                break;
                                            case 14:
                                                shortName = "rect3.dat"; //$NON-NLS-1$
                                                break;
                                            case 13:
                                                vq = ROTQUAD(vq);
                                                shortName = "rect3.dat"; //$NON-NLS-1$
                                                break;
                                            case 11:
                                                vq = ROTQUAD(vq);
                                                vq = ROTQUAD(vq);
                                                shortName = "rect3.dat"; //$NON-NLS-1$
                                                break;
                                            case 15:
                                                shortName = "rect.dat"; //$NON-NLS-1$
                                                break;
                                            }

                                            Vector3d vertexA = new Vector3d(vq[0]);
                                            Vector3d vertexB = new Vector3d(vq[1]);
                                            Vector3d vertexD = new Vector3d(vq[3]);

                                            // Quad local basis
                                            Vector3d temp1 = Vector3d.sub(vertexB, vertexA);
                                            Vector3d temp2 = Vector3d.sub(vertexD, vertexA);
                                            Vector3d temp3 = Vector3d.cross(temp2, temp1);

                                            accurateLocalMatrix = new Matrix(
                                                    temp1.X.divide(TWO), temp1.Y.divide(TWO), temp1.Z.divide(TWO), BigDecimal.ZERO,
                                                    temp3.X.divide(TWO), temp3.Y.divide(TWO), temp3.Z.divide(TWO), BigDecimal.ZERO,
                                                    temp2.X.divide(TWO), temp2.Y.divide(TWO), temp2.Z.divide(TWO), BigDecimal.ZERO,
                                                    d1X, d1Y, d1Z, BigDecimal.ONE);

                                            StringBuilder lineBuilder = new StringBuilder();
                                            lineBuilder.append(1);
                                            lineBuilder.append(" "); //$NON-NLS-1$
                                            int colourNumber = qa.colourNumber;
                                            if (rs.isColourise()) colourNumber = 1;
                                            if (colourNumber == -1) {
                                                lineBuilder.append("0x2"); //$NON-NLS-1$
                                                lineBuilder.append(MathHelper.toHex((int) (255f * qa.r)).toUpperCase());
                                                lineBuilder.append(MathHelper.toHex((int) (255f * qa.g)).toUpperCase());
                                                lineBuilder.append(MathHelper.toHex((int) (255f * qa.b)).toUpperCase());
                                            } else {
                                                lineBuilder.append(colourNumber);
                                            }
                                            lineBuilder.append(" "); //$NON-NLS-1$

                                            lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M30));
                                            lineBuilder.append(" "); //$NON-NLS-1$
                                            lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M31));
                                            lineBuilder.append(" "); //$NON-NLS-1$
                                            lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M32));
                                            lineBuilder.append(" "); //$NON-NLS-1$
                                            lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M00));
                                            lineBuilder.append(" "); //$NON-NLS-1$
                                            lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M10));
                                            lineBuilder.append(" "); //$NON-NLS-1$
                                            lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M20));
                                            lineBuilder.append(" "); //$NON-NLS-1$
                                            lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M01));
                                            lineBuilder.append(" "); //$NON-NLS-1$
                                            lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M11));
                                            lineBuilder.append(" "); //$NON-NLS-1$
                                            lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M21));
                                            lineBuilder.append(" "); //$NON-NLS-1$
                                            lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M02));
                                            lineBuilder.append(" "); //$NON-NLS-1$
                                            lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M12));
                                            lineBuilder.append(" "); //$NON-NLS-1$
                                            lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M22));
                                            lineBuilder.append(" "); //$NON-NLS-1$
                                            lineBuilder.append(shortName);

                                            Set<String> alreadyParsed = new HashSet<String>();
                                            alreadyParsed.add(linkedDatFile.getShortName());
                                            ArrayList<ParsingResult> result = DatParser.parseLine(lineBuilder.toString(), -1, 0, 0.5f, 0.5f, 0.5f, 1.0f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile, false, alreadyParsed, false);
                                            GData rect = result.get(0).getGraphicalData();
                                            if (rect == null)
                                                rect = new GData0(lineBuilder.toString());
                                            linkedDatFile.insertAfter(qa, rect);

                                            quadsToDelete.add(qa);
                                            if (e1 != null) linesToDelete.add(e1);
                                            if (e2 != null) linesToDelete.add(e2);
                                            if (e3 != null) linesToDelete.add(e3);
                                            if (e4 != null) linesToDelete.add(e4);
                                        }
                                    }
                                }
                            }
                        }

                    }
                    finally
                    {
                        monitor.done();
                    }
                }
            });
        }
        catch (InvocationTargetException consumed) {
        } catch (InterruptedException consumed) {
        }


        selectedLines.addAll(linesToDelete);
        selectedTriangles.addAll(trisToIgnore);
        selectedQuads.addAll(quadsToDelete);
        selectedCondlines.addAll(clinesToDelete);
        selectedData.addAll(selectedLines);
        selectedData.addAll(selectedTriangles);
        selectedData.addAll(quadsToDelete);
        selectedData.addAll(selectedCondlines);

        delete(false, syncWithTextEditor);

        validateState();
    }

    private Vertex[] ROTQUAD(Vertex[] vq) {
        Vertex[] result = new Vertex[4];
        result[0] = vq[1];
        result[1] = vq[2];
        result[2] = vq[3];
        result[3] = vq[0];
        return result;
    }

    public void isecalc(IsecalcSettings is) {

        if (linkedDatFile.isReadOnly()) return;

        final ArrayList<GData2> newLines = new ArrayList<GData2>();

        final Set<GData2> linesToDelete = new HashSet<GData2>();
        final Set<GData5> clinesToDelete = new HashSet<GData5>();

        final ArrayList<GData> surfsToParse;

        if (is.getScope() == 0) {
            surfsToParse = new ArrayList<GData>(triangles.size() + quads.size());
            surfsToParse.addAll(triangles.keySet());
            surfsToParse.addAll(quads.keySet());
        } else {
            surfsToParse = new ArrayList<GData>(selectedTriangles.size() + selectedQuads.size());
            surfsToParse.addAll(selectedTriangles);
            surfsToParse.addAll(selectedQuads);
        }

        clearSelection();

        final int surfsSize = surfsToParse.size();

        try
        {
            new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
            {
                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                {
                    try
                    {
                        monitor.beginTask("Searching for intersections...", IProgressMonitor.UNKNOWN); //$NON-NLS-1$ I18N
                        for(int i = 0; i < surfsSize; i++) {
                            /* Check if the monitor has been canceled */
                            if (monitor.isCanceled()) break;
                            NLogger.debug(getClass(), "Checked " + (i + 1) + " of " + surfsSize + " surfaces."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            for(int j = i + 1; j < surfsSize; j++) {
                                GData s1 = surfsToParse.get(i);
                                GData s2 = surfsToParse.get(j);
                                if (isConnected2(s1, s2)) continue;
                                newLines.addAll(intersectionLines(clinesToDelete, linesToDelete, s1, s2));
                            }
                        }
                    }
                    finally
                    {
                        monitor.done();
                    }
                }
            });
        }
        catch (InvocationTargetException consumed) {
        } catch (InterruptedException consumed) {
        }

        if (!newLines.isEmpty()) {

            // Remove zero length lines
            BigDecimal EPSILON = new BigDecimal(".0001"); //$NON-NLS-1$
            for (Iterator<GData2> li = newLines.iterator(); li.hasNext();) {
                GData2 l = li.next();
                BigDecimal dx = l.X1.subtract(l.X2);
                BigDecimal dy = l.Y1.subtract(l.Y2);
                BigDecimal dz = l.Z1.subtract(l.Z2);
                BigDecimal len = dx.multiply(dx).add(dy.multiply(dy)).add(dz.multiply(dz));
                if (len.compareTo(EPSILON) <= 0) {
                    remove(l);
                    li.remove();
                }
            }

            final int lineCount = newLines.size();
            final BigDecimal SMALL = new BigDecimal("0.001"); //$NON-NLS-1$
            final BigDecimal SMALLANGLE = new BigDecimal("0.00001"); //$NON-NLS-1$
            final Vector3d zero = new Vector3d();

            // Merge lines with same directions
            int[] colin = new int[lineCount];
            int distline = 1;
            int flag = 0;
            for (int i=0; i < lineCount; i++)
            {
                if(colin[i] == 0)
                {
                    for (int j= i + 1; j < lineCount; j++)
                    {
                        flag=0;
                        Vector3d p11 = new Vector3d(newLines.get(i).X1, newLines.get(i).Y1, newLines.get(i).Z1);
                        Vector3d p12 = new Vector3d(newLines.get(i).X2, newLines.get(i).Y2, newLines.get(i).Z2);
                        Vector3d p21 = new Vector3d(newLines.get(j).X1, newLines.get(j).Y1, newLines.get(j).Z1);
                        Vector3d p22 = new Vector3d(newLines.get(j).X2, newLines.get(j).Y2, newLines.get(j).Z2);
                        Vector3d line1 = Vector3d.sub(p12, p11);
                        Vector3d line2 = Vector3d.sub(p22, p21);
                        Vector3d temp = Vector3d.cross(line1, line2);
                        BigDecimal angle = Vector3d.manhattan(temp, zero).divide(Vector3d.manhattan(p12, p11), Threshold.mc).divide(Vector3d.manhattan(p22, p21), Threshold.mc);
                        if (angle.compareTo(SMALLANGLE) < 0)
                        {
                            colin[i] = distline;
                            colin[j] = distline;
                            flag=1;
                        }
                    }
                    if((flag = 1) == 1) distline++;
                }
            }
            // printf("%d distinct direction(s)\n", distline-1);

            for (int i=0; i<lineCount-1; i++)
            {
                if(colin[i] > 0)
                {
                    flag=1;
                    while (flag==1)
                    {
                        flag=0;
                        for (int j=i+1; j<lineCount; j++)
                        {
                            if(colin[i]==colin[j])
                            {
                                Vector3d p11 = new Vector3d(newLines.get(i).X1, newLines.get(i).Y1, newLines.get(i).Z1);
                                Vector3d p12 = new Vector3d(newLines.get(i).X2, newLines.get(i).Y2, newLines.get(i).Z2);
                                Vector3d p21 = new Vector3d(newLines.get(j).X1, newLines.get(j).Y1, newLines.get(j).Z1);
                                Vector3d p22 = new Vector3d(newLines.get(j).X2, newLines.get(j).Y2, newLines.get(j).Z2);
                                if(Vector3d.manhattan(p11, p21).compareTo(SMALL) < 0 ||
                                        Vector3d.manhattan(p11, p22).compareTo(SMALL) < 0 ||
                                        Vector3d.manhattan(p12, p22).compareTo(SMALL) < 0 ||
                                        Vector3d.manhattan(p12, p21).compareTo(SMALL) < 0)
                                {
                                    int a=1,b=0;
                                    BigDecimal max, cur;
                                    max = Vector3d.manhattan(p11, p21);
                                    if ((cur = Vector3d.manhattan(p11, p22)).compareTo(max) > 0)
                                    {
                                        a=1; b=1;
                                        max = cur;
                                    }
                                    if ((cur = Vector3d.manhattan(p12, p21)).compareTo(max) > 0)
                                    {
                                        a=0; b=0;
                                        max = cur;
                                    }
                                    if ((cur = Vector3d.manhattan(p12, p22)).compareTo(max) > 0)
                                    {
                                        a=0; b=1;
                                    }
                                    GData2 l1 = newLines.get(i);
                                    GData2 l2 = newLines.get(j);
                                    GColour c = new GColour(24, View.line_Colour_r[0], View.line_Colour_g[0], View.line_Colour_b[0], 1f);
                                    GData2 nl;
                                    // SET(OutLine[i][a], OutLine[j][b]);
                                    if (a == 1) {
                                        if (b == 1) {
                                            nl = new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), l1.X1, l1.Y1, l1.Z1, l2.X2, l2.Y2, l2.Z2, View.DUMMY_REFERENCE, linkedDatFile);
                                        } else {
                                            nl = new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), l1.X1, l1.Y1, l1.Z1, l2.X1, l2.Y1, l2.Z1, View.DUMMY_REFERENCE, linkedDatFile);
                                        }
                                    } else {
                                        if (b == 1) {
                                            nl = new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), l2.X2, l2.Y2, l2.Z2, l1.X2, l1.Y2, l1.Z2, View.DUMMY_REFERENCE, linkedDatFile);
                                        } else {
                                            nl = new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), l2.X1, l2.Y1, l2.Z1, l1.X2, l1.Y2, l1.Z2, View.DUMMY_REFERENCE, linkedDatFile);
                                        }
                                    }
                                    remove(l1);
                                    newLines.remove(i);
                                    newLines.add(i, nl);
                                    colin[j]=-1;
                                    flag = 1;
                                }
                            }
                        }
                    }
                }
            }

            // Remove invalid collinear lines
            {
                int counter = 0;
                for (Iterator<GData2> li = newLines.iterator(); li.hasNext();) {
                    GData2 l = li.next();
                    if (colin[counter] < 0) {
                        remove(l);
                        li.remove();
                    }
                    counter++;
                }
            }

            // Append the lines
            for (GData2 line : newLines) {
                linkedDatFile.addToTail(line);
            }

            // Round to 6 decimal places

            selectedLines.addAll(newLines);
            selectedData.addAll(selectedLines);

            roundSelection(6, 10, true, false);

            clearSelection();
            setModified_NoSync();
        }

        selectedLines.addAll(linesToDelete);
        selectedCondlines.addAll(clinesToDelete);
        selectedData.addAll(selectedLines);
        selectedData.addAll(selectedCondlines);
        delete(false, false);

        if (isModified()) {
            syncWithTextEditors(true);
        }

        validateState();
    }

    /**
     * FOR ISECALC/INTERSECTOR ONLY
     * @param p
     * @param q
     * @param a
     * @param b
     * @param c
     * @param r
     * @return
     */
    private boolean intersectLineTriangle(Vertex p, Vertex q, Vertex a, Vertex b, Vertex c, Vector3d r) {
        final BigDecimal TOLERANCE = new BigDecimal("0.00001"); //$NON-NLS-1$ .00001
        final BigDecimal ZEROT = new BigDecimal("-0.00001"); //$NON-NLS-1$
        final BigDecimal ONET = new BigDecimal("1.00001"); //$NON-NLS-1$
        BigDecimal diskr = BigDecimal.ZERO;
        BigDecimal inv_diskr = BigDecimal.ZERO;
        Vector3d vert0 = new Vector3d(a);
        Vector3d vert1 = new Vector3d(b);
        Vector3d vert2 = new Vector3d(c);
        Vector3d corner1 = Vector3d.sub(vert1, vert0);
        Vector3d corner2 = Vector3d.sub(vert2, vert0);
        Vector3d orig = new Vector3d(p);
        Vector3d dir = Vector3d.sub(new Vector3d(q), orig);
        BigDecimal len = dir.normalise(dir);
        Vector3d pvec = Vector3d.cross(dir, corner2);
        diskr = Vector3d.dotP(corner1, pvec);
        if (diskr.abs().compareTo(TOLERANCE) < 0)
            return false;
        inv_diskr = BigDecimal.ONE.divide(diskr, Threshold.mc);
        Vector3d tvec = Vector3d.sub(orig, vert0);
        BigDecimal u = Vector3d.dotP(tvec, pvec).multiply(inv_diskr);
        if (u.compareTo(ZEROT) < 0 || u.compareTo(ONET) > 0)
            return false;
        Vector3d qvec = Vector3d.cross(tvec, corner1);
        BigDecimal v = Vector3d.dotP(dir, qvec).multiply(inv_diskr);
        if (v.compareTo(ZEROT) < 0 || u.add(v).compareTo(ONET) > 0)
            return false;
        BigDecimal t = Vector3d.dotP(corner2, qvec).multiply(inv_diskr);
        if (t.compareTo(ZEROT) < 0 || t.compareTo(len.add(TOLERANCE)) > 0)
            return false;
        r.setX(orig.X.add(dir.X.multiply(t)));
        r.setY(orig.Y.add(dir.Y.multiply(t)));
        r.setZ(orig.Z.add(dir.Z.multiply(t)));
        return true;
    }

    private final BigDecimal TOLERANCE = BigDecimal.ZERO; // new BigDecimal("0.00001"); //.00001
    private final BigDecimal ZEROT = BigDecimal.ZERO; //  = new BigDecimal("-0.00001");
    private final BigDecimal ONET = BigDecimal.ONE; //  = new BigDecimal("1.00001");
    private boolean intersectLineTriangleSuperFast(Vector3dd q, Vector3dd q2, Vector3d d, Vector3dd p2, Vector3d c, Vector3d dir, BigDecimal len) {
        BigDecimal diskr = BigDecimal.ZERO;
        BigDecimal inv_diskr = BigDecimal.ZERO;
        Vector3d vert0 = d;
        Vector3d vert1 = p2;
        Vector3d vert2 = c;
        Vector3d corner1 = Vector3d.sub(vert1, vert0);
        Vector3d corner2 = Vector3d.sub(vert2, vert0);
        Vector3d orig = q;
        Vector3d pvec = Vector3d.cross(dir, corner2);
        diskr = Vector3d.dotP(corner1, pvec);
        if (diskr.abs().compareTo(TOLERANCE) <= 0)
            return false;
        inv_diskr = BigDecimal.ONE.divide(diskr, Threshold.mc);
        Vector3d tvec = Vector3d.sub(orig, vert0);
        BigDecimal u = Vector3d.dotP(tvec, pvec).multiply(inv_diskr);
        if (u.compareTo(ZEROT) < 0 || u.compareTo(ONET) > 0)
            return false;
        Vector3d qvec = Vector3d.cross(tvec, corner1);
        BigDecimal v = Vector3d.dotP(dir, qvec).multiply(inv_diskr);
        if (v.compareTo(ZEROT) < 0 || u.add(v).compareTo(ONET) > 0)
            return false;
        BigDecimal t = Vector3d.dotP(corner2, qvec).multiply(inv_diskr);
        if (t.compareTo(ZEROT) < 0 || t.compareTo(len.add(TOLERANCE)) > 0)
            return false;
        return true;
    }

    private HashSet<GData2> intersectionLines(final Set<GData5> clinesToDelete, final Set<GData2> linesToDelete, GData g1, GData g2) {

        GColour c = new GColour(24, View.line_Colour_r[0], View.line_Colour_g[0], View.line_Colour_b[0], 1f);

        HashSet<GData2> result = new HashSet<GData2>();
        HashSet<Vector3d> points = new HashSet<Vector3d>();

        int t1 = g1.type();
        int t2 = g2.type();

        if (t1 == 3 && t2 == 3) {
            Vertex[] v1 = triangles.get(g1);
            Vertex[] v2 = triangles.get(g2);
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[0], v1[1], v2[0], v2[1], v2[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[1], v1[2], v2[0], v2[1], v2[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[2], v1[0], v2[0], v2[1], v2[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[0], v2[1], v1[0], v1[1], v1[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[1], v2[2], v1[0], v1[1], v1[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[2], v2[0], v1[0], v1[1], v1[2], p)) {
                    points.add(p);
                }
            }
        } else if (t1 == 4 && t2 == 4) {
            Vertex[] v1 = quads.get(g1);
            Vertex[] v2 = quads.get(g2);
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[0], v1[1], v2[0], v2[1], v2[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[1], v1[2], v2[0], v2[1], v2[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[2], v1[3], v2[0], v2[1], v2[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[3], v1[0], v2[0], v2[1], v2[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[0], v1[1], v2[2], v2[3], v2[0], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[1], v1[2], v2[2], v2[3], v2[0], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[2], v1[3], v2[2], v2[3], v2[0], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[3], v1[0], v2[2], v2[3], v2[0], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[0], v2[1], v1[0], v1[1], v1[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[1], v2[2], v1[0], v1[1], v1[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[2], v2[3], v1[0], v1[1], v1[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[3], v2[0], v1[0], v1[1], v1[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[0], v2[1], v1[2], v1[3], v1[0], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[1], v2[2], v1[2], v1[3], v1[0], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[2], v2[3], v1[2], v1[3], v1[0], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[3], v2[0], v1[2], v1[3], v1[0], p)) {
                    points.add(p);
                }
            }
        }

        if (t1 == 4 && t2 == 3) {
            GData g3 = g1;
            g1 = g2;
            g2 = g3;
            t1 = 3;
            t2 = 4;
        }

        if (t1 == 3 && t2 == 4) {
            Vertex[] v1 = triangles.get(g1);
            Vertex[] v2 = quads.get(g2);
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[0], v1[1], v2[0], v2[1], v2[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[1], v1[2], v2[0], v2[1], v2[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[2], v1[0], v2[0], v2[1], v2[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[0], v1[1], v2[2], v2[3], v2[0], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[1], v1[2], v2[2], v2[3], v2[0], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[2], v1[0], v2[2], v2[3], v2[0], p)) {
                    points.add(p);
                }
            }
        }

        final BigDecimal EPSILON = new BigDecimal(".0001"); //$NON-NLS-1$
        for(Iterator<Vector3d> i = points.iterator(); i.hasNext(); ) {
            Vector3d p1 = i.next();
            for (Vector3d p2 : points) {
                if (!p1.equals(p2)) {
                    Vector3d p3 = Vector3d.sub(p1, p2);
                    BigDecimal md = p3.X.multiply(p3.X).add(p3.Y.multiply(p3.Y)).add(p3.Z.multiply(p3.Z));
                    if (md.compareTo(EPSILON) <= 0) {
                        i.remove();
                        break;
                    }
                }
            }
        }
        if (points.size() == 4) {
            ArrayList<Vector3d> points2 = new ArrayList<Vector3d>();
            points2.addAll(points);
            result.add(new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), points2.get(0).X, points2.get(0).Y, points2.get(0).Z, points2.get(1).X, points2.get(1).Y, points2.get(1).Z, View.DUMMY_REFERENCE, linkedDatFile));
            result.add(new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), points2.get(1).X, points2.get(1).Y, points2.get(1).Z, points2.get(2).X, points2.get(2).Y, points2.get(2).Z, View.DUMMY_REFERENCE, linkedDatFile));
            result.add(new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), points2.get(2).X, points2.get(2).Y, points2.get(2).Z, points2.get(3).X, points2.get(3).Y, points2.get(3).Z, View.DUMMY_REFERENCE, linkedDatFile));
            result.add(new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), points2.get(0).X, points2.get(0).Y, points2.get(0).Z, points2.get(3).X, points2.get(3).Y, points2.get(3).Z, View.DUMMY_REFERENCE, linkedDatFile));
        } else if (points.size() == 3) {
            ArrayList<Vector3d> points2 = new ArrayList<Vector3d>();
            points2.addAll(points);
            result.add(new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), points2.get(0).X, points2.get(0).Y, points2.get(0).Z, points2.get(1).X, points2.get(1).Y, points2.get(1).Z, View.DUMMY_REFERENCE, linkedDatFile));
            result.add(new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), points2.get(2).X, points2.get(2).Y, points2.get(2).Z, points2.get(1).X, points2.get(1).Y, points2.get(1).Z, View.DUMMY_REFERENCE, linkedDatFile));
            result.add(new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), points2.get(0).X, points2.get(0).Y, points2.get(0).Z, points2.get(2).X, points2.get(2).Y, points2.get(2).Z, View.DUMMY_REFERENCE, linkedDatFile));
        } else if (points.size() == 2) {
            ArrayList<Vector3d> points2 = new ArrayList<Vector3d>();
            points2.addAll(points);
            result.add(new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), points2.get(0).X, points2.get(0).Y, points2.get(0).Z, points2.get(1).X, points2.get(1).Y, points2.get(1).Z, View.DUMMY_REFERENCE, linkedDatFile));
        }

        return result;
    }


    public void backupSelection() {
        backupSelectedCondlines.addAll(selectedCondlines);
        backupSelectedData.addAll(selectedData);
        backupSelectedLines.addAll(selectedLines);
        backupSelectedQuads.addAll(selectedQuads);
        backupSelectedSubfiles.addAll(selectedSubfiles);
        backupSelectedTriangles.addAll(selectedTriangles);
        backupSelectedVertices.addAll(selectedVertices);
    }

    private void backupSelectionClear() {
        backupSelectedCondlines.clear();
        backupSelectedData.clear();
        backupSelectedLines.clear();
        backupSelectedQuads.clear();
        backupSelectedSubfiles.clear();
        backupSelectedTriangles.clear();
        backupSelectedVertices.clear();
    }

    public void restoreSelection() {
        clearSelection3();
        selectedCondlines.addAll(backupSelectedCondlines);
        selectedData.addAll(backupSelectedData);
        selectedLines.addAll(backupSelectedLines);
        selectedQuads.addAll(backupSelectedQuads);
        selectedSubfiles.addAll(backupSelectedSubfiles);
        selectedTriangles.addAll(backupSelectedTriangles);
        selectedVertices.addAll(backupSelectedVertices);
        backupSelectionClear();
    }

    public void slicerpro(final SlicerProSettings ss) {
        if (linkedDatFile.isReadOnly()) return;
        Composite3D c3d =  linkedDatFile.getLastSelectedComposite();
        NLogger.debug(getClass(), "SlicerPro2 - (C) Nils Schmidt 2015"); //$NON-NLS-1$
        NLogger.debug(getClass(), "======================"); //$NON-NLS-1$
        if (c3d != null) {

            final int[] isCancelled = new int[]{0};

            final Set<GData2> debugLines = new HashSet<GData2>();

            final Set<GData3> trisToDelete = new HashSet<GData3>();
            final Set<GData4> quadsToDelete = new HashSet<GData4>();

            Vector4f dir4f = new Vector4f(c3d.getGenerator()[2]);
            final Vector3r dir = new Vector3r(dir4f);
            final Vector3r dirN = new Vector3r(dir);
            dirN.negate();

            // NLogger.debug(getClass(), "Ray Direction:" + dir.toString()); //$NON-NLS-1$
            // NLogger.debug(getClass(), "Neg. Ray Direction:" + dirN.toString()); //$NON-NLS-1$

            final Matrix4f vport = c3d.getViewport();
            final RationalMatrix m = new RationalMatrix(vport);
            final RationalMatrix minv = m.invert();
            final PerspectiveCalculator pc = c3d.getPerspectiveCalculator();

            // NLogger.debug(getClass(), "Viewport Matrix Inv.:\n" + minv.toString()); //$NON-NLS-1$

            NLogger.debug(getClass(), "Get target surfaces to parse."); //$NON-NLS-1$

            final HashSet<GData> targetSurfs = new HashSet<GData>();
            {
                Set<GData3> tris = triangles.keySet();
                for (GData3 tri : tris) {
                    if (lineLinkedToVertices.containsKey(tri) && !hiddenData.contains(tri)) {
                        targetSurfs.add(tri);
                    }
                }
            }
            {
                Set<GData4> qs = quads.keySet();
                for (GData4 quad : qs) {
                    if (lineLinkedToVertices.containsKey(quad) && !hiddenData.contains(quad)) {
                        targetSurfs.add(quad);
                    }
                }
            }

            NLogger.debug(getClass(), "Cleanup the selection."); //$NON-NLS-1$

            for(Iterator<GData3> ti = selectedTriangles.iterator(); ti.hasNext();) {
                GData3 tri = ti.next();
                if (!lineLinkedToVertices.containsKey(tri)) {
                    ti.remove();
                }
            }
            for(Iterator<GData4> qi = selectedQuads.iterator(); qi.hasNext();) {
                GData4 quad = qi.next();
                if (!lineLinkedToVertices.containsKey(quad)) {
                    qi.remove();
                }
            }

            targetSurfs.removeAll(selectedTriangles);
            targetSurfs.removeAll(selectedQuads);

            final ArrayList<GData> originSurfs = new ArrayList<GData>();
            originSurfs.addAll(selectedTriangles);
            originSurfs.addAll(selectedQuads);

            clearSelection();

            final ArrayList<ArrayList<IntersectionInfo>> intersections = new ArrayList<ArrayList<IntersectionInfo>>();
            final Set<GData3> newTriangles = Collections.newSetFromMap(new ThreadsafeHashMap<GData3, Boolean>());

            try
            {
                new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
                {
                    @Override
                    public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                    {
                        try
                        {
                            monitor.beginTask("Running SlicerPro2 (this may take some time)", IProgressMonitor.UNKNOWN); //$NON-NLS-1$ I18N

                            {

                                final Set<ArrayList<IntersectionInfo>> intersectionSet = Collections.newSetFromMap(new ThreadsafeHashMap<ArrayList<IntersectionInfo>, Boolean>());

                                final int iterations = originSurfs.size();
                                final int chunks = View.NUM_CORES;
                                final Thread[] threads = new Thread[chunks];

                                final String surfCount = "/" + iterations;//$NON-NLS-1$
                                final AtomicInteger counter2 = new AtomicInteger(0);

                                int lastend = 0;
                                for (int j = 0; j < chunks; ++j) {
                                    final int[] start = new int[] { lastend };
                                    lastend = Math.round(iterations / chunks * (j + 1));
                                    final int[] end = new int[] { lastend };
                                    if (j == chunks - 1) {
                                        end[0] = iterations;
                                    }
                                    threads[j] = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            for (int k = start[0]; k < end[0]; k++) {
                                                monitor.subTask(counter2.toString() + surfCount);
                                                GData o = originSurfs.get(k);
                                                /* Check if the monitor has been canceled */
                                                if (monitor.isCanceled()) {
                                                    isCancelled[0] = 1;
                                                    return;
                                                }
                                                counter2.incrementAndGet();
                                                for (GData t : targetSurfs) {
                                                    // Customise IDs for debugging
                                                    //                    if (o.ID == 9736 && t.ID == 9763) {
                                                    //                        NLogger.debug(getClass(), "Comparing target pair..."); //$NON-NLS-1$
                                                    //                    }
                                                    ArrayList<IntersectionInfo> ii = getIntersectionInfo(o, t, dir, dirN, m, minv, pc, ss);
                                                    if (ii != null) {
                                                        intersectionSet.add(ii);
                                                        switch (t.type()) {
                                                        case 3:
                                                            trisToDelete.add((GData3) t);
                                                            break;
                                                        case 4:
                                                            quadsToDelete.add((GData4) t);
                                                            break;
                                                        default:
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    });
                                    threads[j].start();
                                }
                                boolean isRunning = true;
                                while (isRunning) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                    }
                                    isRunning = false;
                                    for (Thread thread : threads) {
                                        if (thread.isAlive())
                                            isRunning = true;
                                    }
                                }
                                intersections.addAll(intersectionSet);
                            }

                            if (isCancelled[0] > 0) return;

                            NLogger.debug(getClass(), "Create new faces."); //$NON-NLS-1$

                            {
                                final int iterations = intersections.size();
                                final int chunks = View.NUM_CORES;
                                final Thread[] threads = new Thread[chunks];


                                final String maxIterations = "/" + iterations;//$NON-NLS-1$
                                final AtomicInteger counter2 = new AtomicInteger(0);

                                int lastend = 0;
                                for (int j = 0; j < chunks; ++j) {
                                    final int[] start = new int[] { lastend };
                                    lastend = Math.round(iterations / chunks * (j + 1));
                                    final int[] end = new int[] { lastend };
                                    if (j == chunks - 1) {
                                        end[0] = iterations;
                                    }
                                    threads[j] = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            for (int k = start[0]; k < end[0]; k++) {
                                                monitor.subTask(counter2.toString() + maxIterations);
                                                ArrayList<IntersectionInfo> ii = intersections.get(k);
                                                if (monitor.isCanceled()) {
                                                    isCancelled[0] = 2;
                                                    return;
                                                }
                                                counter2.incrementAndGet();
                                                for (IntersectionInfo info : ii) {
                                                    final int pointsToTriangulate = info.getAllVertices().size();
                                                    final ArrayList<Vector3d> av = info.getAllVertices();

                                                    final float R, G, B, A;
                                                    final int CN;
                                                    GData origin2 = info.getOrigin();
                                                    if (origin2.type() == 3) {
                                                        GData3 origin = (GData3) origin2;
                                                        CN = origin.colourNumber;
                                                        R = origin.r;
                                                        G = origin.g;
                                                        B = origin.b;
                                                        A = origin.a;
                                                    } else {
                                                        GData4 origin = (GData4) origin2;
                                                        CN = origin.colourNumber;
                                                        R = origin.r;
                                                        G = origin.g;
                                                        B = origin.b;
                                                        A = origin.a;
                                                    }

                                                    switch (pointsToTriangulate) {
                                                    case 3:
                                                        newTriangles.add(new GData3(CN, R, G, B, A,
                                                                av.get(0).X, av.get(0).Y, av.get(0).Z,
                                                                av.get(1).X, av.get(1).Y, av.get(1).Z,
                                                                av.get(2).X, av.get(2).Y, av.get(2).Z,
                                                                View.DUMMY_REFERENCE, linkedDatFile));
                                                        break;
                                                    case 4:
                                                    case 5:
                                                    case 6:
                                                    case 7:
                                                        newTriangles.addAll(MathHelper.triangulateNPoints(CN, R, G, B, A, pointsToTriangulate, av, View.DUMMY_REFERENCE, linkedDatFile));
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    });
                                    threads[j].start();
                                }
                                boolean isRunning = true;
                                while (isRunning) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                    }
                                    isRunning = false;
                                    for (Thread thread : threads) {
                                        if (thread.isAlive())
                                            isRunning = true;
                                    }
                                }
                            }

                        }
                        finally
                        {
                            monitor.done();
                        }
                    }
                });
            }
            catch (InvocationTargetException consumed) {
            } catch (InterruptedException consumed) {
            }


            NLogger.debug(getClass(), "Check for identical vertices, invalid winding and collinearity."); //$NON-NLS-1$
            final Set<GData3> trisToDelete2 = new HashSet<GData3>();
            {
                final Set<GData3> newTriangles2 = new HashSet<GData3>();
                for (GData3 g3 : newTriangles) {
                    Vertex[] verts = triangles.get(g3);
                    Set<Vertex> verts2 = new TreeSet<Vertex>();
                    for (Vertex vert : verts) {
                        verts2.add(vert);
                    }
                    if (verts2.size() < 3 || g3.isCollinear()) {
                        trisToDelete2.add(g3);
                    } else {
                        GData3 tri = checkNormal(g3, vport);
                        if (tri != null) {
                            trisToDelete2.add(g3);
                            newTriangles2.add(tri);
                        }
                    }
                }
                newTriangles.addAll(newTriangles2);
            }


            if (isCancelled[0] == 0) {
                NLogger.debug(getClass(), "Delete old target faces."); //$NON-NLS-1$

                selectedTriangles.addAll(trisToDelete);
                selectedQuads.addAll(quadsToDelete);
                selectedData.addAll(selectedTriangles);
                selectedData.addAll(selectedQuads);
                delete(false, false);
            } else {
                clearSelection();
            }

            // Append the triangles
            for (GData3 tri : newTriangles) {
                linkedDatFile.addToTail(tri);
            }

            for (GData2 lin : debugLines) {
                linkedDatFile.addToTail(lin);
            }

            NLogger.debug(getClass(), "Delete new, but invalid faces."); //$NON-NLS-1$

            newTriangles.removeAll(trisToDelete2);

            selectedTriangles.addAll(trisToDelete2);
            selectedData.addAll(selectedTriangles);
            delete(false, false);

            // Round to 6 decimal places

            selectedTriangles.addAll(newTriangles);
            selectedData.addAll(newTriangles);

            NLogger.debug(getClass(), "Round."); //$NON-NLS-1$
            roundSelection(6, 10, true, false);

            NLogger.debug(getClass(), "Rectify."); //$NON-NLS-1$
            RectifierSettings rs = new RectifierSettings();
            rs.setScope(1);
            rectify(rs, false);

            clearSelection();
            setModified(true, true);

            NLogger.debug(getClass(), "Done."); //$NON-NLS-1$

            validateState();

        } else {
            NLogger.debug(getClass(), "No 3D view selected. Cancel process."); //$NON-NLS-1$
        }
    }


    private ArrayList<IntersectionInfo> getIntersectionInfo(GData origin, GData target, Vector3r dir, Vector3r dirN, RationalMatrix m, RationalMatrix minv, PerspectiveCalculator pc, SlicerProSettings ss) {
        final int ot = origin.type();
        final int tt = target.type();

        final ArrayList<IntersectionInfo> result = new ArrayList<IntersectionInfo>();

        if (ot == 3 && tt == 3) {

            Vertex[] ov = triangles.get(origin);
            Vertex[] tv = triangles.get(target);

            Vector3r[] ov1 = new Vector3r[]{new Vector3r(ov[0]), new Vector3r(ov[1]), new Vector3r(ov[2])};
            Vector3r[] tv1 = new Vector3r[]{new Vector3r(tv[0]), new Vector3r(tv[1]), new Vector3r(tv[2])};

            IntersectionInfo tti = getTriangleTriangleIntersection(ov1, tv1, origin, target, dir, dirN, m, minv, pc, ss);
            if (tti == null) {
                return null;
            }
            result.add(tti);
            return result;

        } else if (ot == 4 && tt == 4) {

            Vertex[] ov = quads.get(origin);
            Vertex[] tv = quads.get(target);

            Vector3r[] ov1 = new Vector3r[]{new Vector3r(ov[0]), new Vector3r(ov[1]), new Vector3r(ov[2])};
            Vector3r[] ov2 = new Vector3r[]{new Vector3r(ov[2]), new Vector3r(ov[3]), new Vector3r(ov[0])};
            Vector3r[] tv1 = new Vector3r[]{new Vector3r(tv[0]), new Vector3r(tv[1]), new Vector3r(tv[2])};
            Vector3r[] tv2 = new Vector3r[]{new Vector3r(tv[2]), new Vector3r(tv[3]), new Vector3r(tv[0])};

            IntersectionInfo tti1 = getTriangleTriangleIntersection(ov1, tv1, origin, target, dir, dirN, m, minv, pc, ss);
            IntersectionInfo tti2 = getTriangleTriangleIntersection(ov1, tv2, origin, target, dir, dirN, m, minv, pc, ss);
            IntersectionInfo tti3 = getTriangleTriangleIntersection(ov2, tv1, origin, target, dir, dirN, m, minv, pc, ss);
            IntersectionInfo tti4 = getTriangleTriangleIntersection(ov2, tv2, origin, target, dir, dirN, m, minv, pc, ss);

            if (tti1 != null) {
                result.add(tti1);
            }
            if (tti2 != null) {
                result.add(tti2);
            }
            if (tti3 != null) {
                result.add(tti3);
            }
            if (tti4 != null) {
                result.add(tti4);
            }
            if (result.isEmpty()) return null;
            return result;
        } else if (ot == 4 && tt == 3) {
            Vertex[] ov = quads.get(origin);
            Vertex[] tv = triangles.get(target);

            Vector3r[] tv1 = new Vector3r[]{new Vector3r(tv[0]), new Vector3r(tv[1]), new Vector3r(tv[2])};
            Vector3r[] ov1 = new Vector3r[]{new Vector3r(ov[0]), new Vector3r(ov[1]), new Vector3r(ov[2])};
            Vector3r[] ov2 = new Vector3r[]{new Vector3r(ov[2]), new Vector3r(ov[3]), new Vector3r(ov[0])};

            IntersectionInfo tti1 = getTriangleTriangleIntersection(ov1, tv1, origin, target, dir, dirN, m, minv, pc, ss);
            IntersectionInfo tti2 = getTriangleTriangleIntersection(ov2, tv1, origin, target, dir, dirN, m, minv, pc, ss);

            if (tti1 != null) {
                result.add(tti1);
            }
            if (tti2 != null) {
                result.add(tti2);
            }
            if (result.isEmpty()) return null;
            return result;
        } else if (ot == 3 && tt == 4) {

            Vertex[] ov = triangles.get(origin);
            Vertex[] tv = quads.get(target);

            Vector3r[] ov1 = new Vector3r[]{new Vector3r(ov[0]), new Vector3r(ov[1]), new Vector3r(ov[2])};
            Vector3r[] tv1 = new Vector3r[]{new Vector3r(tv[0]), new Vector3r(tv[1]), new Vector3r(tv[2])};
            Vector3r[] tv2 = new Vector3r[]{new Vector3r(tv[2]), new Vector3r(tv[3]), new Vector3r(tv[0])};

            IntersectionInfo tti1 = getTriangleTriangleIntersection(ov1, tv1, origin, target, dir, dirN, m, minv, pc, ss);
            IntersectionInfo tti2 = getTriangleTriangleIntersection(ov1, tv2, origin, target, dir, dirN, m, minv, pc, ss);

            if (tti1 != null) {
                result.add(tti1);
            }
            if (tti2 != null) {
                result.add(tti2);
            }
            if (result.isEmpty()) return null;
            return result;

        }
        return null;
    }

    private boolean intersectRayTriangle(Vector3r origin2d, Vertex dir, Vector3r target2d, Vector3r target2d2, Vector3r target2d3, Vector3r ip) {
        Rational diskr = Rational.ZERO;
        Rational inv_diskr = Rational.ZERO;
        Vector3r vert0 = new Vector3r(target2d);
        Vector3r vert1 = new Vector3r(target2d2);
        Vector3r vert2 = new Vector3r(target2d3);
        vert0.setZ(Rational.ONE);
        vert1.setZ(Rational.ONE);
        vert2.setZ(Rational.ONE);
        Vector3r corner1 = Vector3r.sub(vert1, vert0);
        Vector3r corner2 = Vector3r.sub(vert2, vert0);
        Vector3r dir2 = new Vector3r(dir);
        Vector3r orig = new Vector3r(origin2d);
        orig.setZ(new Rational(-1000));
        Vector3r pvec = Vector3r.cross(dir2, corner2);
        diskr = Vector3r.dot(corner1, pvec);
        if (diskr.abs().compareTo(Rational.ZERO) == 0)
            return false;
        inv_diskr = Rational.ONE.divide(diskr);
        Vector3r tvec = Vector3r.sub(orig, vert0);
        Rational u = Vector3r.dot(tvec, pvec).multiply(inv_diskr);
        if (u.compareTo(Rational.ZERO) < 0 || u.compareTo(Rational.ONE) > 0)
            return false;
        Vector3r qvec = Vector3r.cross(tvec, corner1);
        Rational v = Vector3r.dot(dir2, qvec).multiply(inv_diskr);
        if (v.compareTo(Rational.ZERO) < 0 || u.add(v).compareTo(Rational.ONE) > 0)
            return false;
        Rational t = Vector3r.dot(corner2, qvec).multiply(inv_diskr);
        //        if (t.compareTo(Rational.ZERO) < 0)
        //            return false;
        ip.setX(orig.X.add(dir2.X.multiply(t)));
        ip.setY(orig.Y.add(dir2.Y.multiply(t)));
        ip.setZ(orig.Z.add(dir2.Z.multiply(t)));
        return true;
    }

    private void projectRayOnTrianglePlane(Vector3r vector3r, Vector3r dirN, Vector3r tv, Vector3r tv2, Vector3r tv3, Vector3r r) {
        Rational diskr = Rational.ZERO;
        Rational inv_diskr = Rational.ZERO;
        Vector3r vert0 = new Vector3r(tv);
        Vector3r vert1 = new Vector3r(tv2);
        Vector3r vert2 = new Vector3r(tv3);
        Vector3r corner1 = Vector3r.sub(vert1, vert0);
        Vector3r corner2 = Vector3r.sub(vert2, vert0);
        Vector3r orig2 = new Vector3r(vector3r);
        Vector3r dir2 = new Vector3r(dirN);
        Vector3r pvec = Vector3r.cross(dir2, corner2);
        diskr = Vector3r.dot(corner1, pvec);
        if (diskr.abs().compareTo(Rational.ZERO) == 0)
            return;
        inv_diskr = Rational.ONE.divide(diskr);
        Vector3r tvec = Vector3r.sub(orig2, vert0);
        Vector3r qvec = Vector3r.cross(tvec, corner1);
        Rational t = Vector3r.dot(corner2, qvec).multiply(inv_diskr);
        r.setX(orig2.X.add(dir2.X.multiply(t)));
        r.setY(orig2.Y.add(dir2.Y.multiply(t)));
        r.setZ(orig2.Z.add(dir2.Z.multiply(t)));
    }

    private int intersectLineLineSegment2DExact(Vector3r p, Vector3r p2, Vector3r q, Vector3r q2, Vector3r ip, Vector3r ip2) {

        Vector3r u = Vector3r.sub(p2, p);
        Vector3r v = Vector3r.sub(q2, q);
        Vector3r w = Vector3r.sub(p, q);
        Rational D = u.X.multiply(v.Y).subtract(u.Y.multiply(v.X));

        if (D.isZero()) {

            if (!u.X.multiply(w.Y).subtract(u.Y.multiply(w.X)).isZero() || !v.X.multiply(w.Y).subtract(v.Y.multiply(w.X)).isZero())  {
                return 0;
            }

            Rational du = u.X.multiply(u.X).add(u.Y.multiply(u.Y));
            Rational dv = v.X.multiply(v.X).add(v.Y.multiply(v.Y));

            if (du.isZero() || dv.isZero()) { // ||
                //                if (!p.equals(q))
                //                    return 0;
                //                ip.setX(p.X);
                //                ip.setY(p.Y);
                //                ip.setZ(p.Z);
                return 0;
            }
            //            if (du.isZero()) {
            //                if  (inSegment(p, q, q2))
            //                    return 0;
            //                ip.setX(p.X);
            //                ip.setY(p.Y);
            //                ip.setZ(p.Z);
            //                return 0;
            //            }
            //            if (dv.isZero()) {
            //                if  (inSegment(q, p, p2))
            //                    return 0;
            //                ip.setX(q.X);
            //                ip.setY(q.Y);
            //                ip.setZ(q.Z);
            //                return 0;
            //            }
            Rational t0, t1;
            Vector3r w2 = Vector3r.sub(p2, q);
            if (!v.X.isZero()) {
                t0 = w.X.divide(v.X);
                t1 = w2.X.divide(v.X);
            }
            else {
                t0 = w.Y.divide(v.Y);
                t1 = w2.Y.divide(v.Y);
            }
            if (t0.compareTo(t1) > 0) {
                Rational t=t0; t0=t1; t1=t;
            }
            if (t0.compareTo(Rational.ONE) > 0 || t1.compareTo(Rational.ZERO) < 0) {
                return 0;
            }
            t0 = t0.compareTo(Rational.ZERO) < 0 ? Rational.ZERO : t0;
            t1 = t1.compareTo(Rational.ONE) > 0 ? Rational.ONE : t1;
            if (t0.compareTo(t1) == 0) {
                //                ip.setX(q.X.add(t0.multiply(v.X)));
                //                ip.setY(q.Y.add(t0.multiply(v.Y)));
                //                ip.setZ(q.Z.add(t0.multiply(v.Z)));
                return 0;
            }

            ip.setX(q.X.add(t0.multiply(v.X)));
            ip.setY(q.Y.add(t0.multiply(v.Y)));
            ip.setZ(q.Z.add(t0.multiply(v.Z)));
            ip2.setX(q.X.add(t1.multiply(v.X)));
            ip2.setY(q.Y.add(t1.multiply(v.Y)));
            ip2.setZ(q.Z.add(t1.multiply(v.Z)));
            return 2;
        }

        Rational sI = v.X.multiply(w.Y).subtract(v.Y.multiply(w.X)).divide(D);
        if (sI.compareTo(Rational.ZERO) < 0 || sI.compareTo(Rational.ONE) > 0)
            return 0;

        Rational tI = u.X.multiply(w.Y).subtract(u.Y.multiply(w.X)).divide(D);
        if (tI.compareTo(Rational.ZERO) < 0 || tI.compareTo(Rational.ONE) > 0)
            return 0;

        ip.setX(p.X.add(sI.multiply(u.X)));
        ip.setY(p.Y.add(sI.multiply(u.Y)));
        ip.setZ(p.Z.add(sI.multiply(u.Z)));
        return 1;
    }

    private boolean inSegment(Vector3r p, Vector3r s1, Vector3r s2) {
        if (s1.X.compareTo(s1.X) != 0) {
            if (s1.X.compareTo(p.X) <= 0 && p.X.compareTo(s2.X) <= 0)
                return true;
            if (s1.X.compareTo(p.X) >= 0  && p.X.compareTo(s2.X) >= 0)
                return true;
        } else {
            if (s1.Y.compareTo(p.Y) <= 0 && p.Y.compareTo(s2.Y) <= 0)
                return true;
            if (s1.Y.compareTo(p.Y) >= 0 && p.Y.compareTo(s2.Y) >= 0)
                return true;
        }
        return false;
    }

    private boolean ills(Vector3r p, Vector3r p2, Vector3r q, Vector3r q2) {

        Vector3r u = Vector3r.sub(p2, p);
        Vector3r v = Vector3r.sub(q2, q);
        Vector3r w = Vector3r.sub(p, q);
        Rational D = u.X.multiply(v.Y).subtract(u.Y.multiply(v.X));

        if (D.isZero()) {

            if (!u.X.multiply(w.Y).subtract(u.Y.multiply(w.X)).isZero() || !v.X.multiply(w.Y).subtract(v.Y.multiply(w.X)).isZero())  {
                return false;
            }

            Rational du = u.X.multiply(u.X).add(u.Y.multiply(u.Y));
            Rational dv = v.X.multiply(v.X).add(v.Y.multiply(v.Y));

            if (du.isZero() && dv.isZero()) {
                return p.equals(q);
            }
            if (du.isZero()) {
                return !inSegment(p, q, q2);
            }
            if (dv.isZero()) {
                return !inSegment(q, p, p2);
            }
            Rational t0, t1;
            Vector3r w2 = Vector3r.sub(p2, q);
            if (!v.X.isZero()) {
                t0 = w.X.divide(v.X);
                t1 = w2.X.divide(v.X);
            }
            else {
                t0 = w.Y.divide(v.Y);
                t1 = w2.Y.divide(v.Y);
            }
            if (t0.compareTo(t1) > 0) {
                Rational t=t0; t0=t1; t1=t;
            }
            if (t0.compareTo(Rational.ONE) > 0 || t1.compareTo(Rational.ZERO) < 0) {
                return false;
            }
            return true;
        }

        Rational sI = v.X.multiply(w.Y).subtract(v.Y.multiply(w.X)).divide(D);
        if (sI.compareTo(Rational.ZERO) < 0 || sI.compareTo(Rational.ONE) > 0)
            return false;

        Rational tI = u.X.multiply(w.Y).subtract(u.Y.multiply(w.X)).divide(D);
        if (tI.compareTo(Rational.ZERO) < 0 || tI.compareTo(Rational.ONE) > 0)
            return false;

        return true;
    }

    private boolean intersectLineLineSegmentUnidirectional(Vector3dd p, Vector3dd p2, Vector3dd q, Vector3dd q2) {


        Vector3d sp = Vector3d.sub(p2, p);
        Vector3d sq = Vector3d.sub(q2, q);
        Vector3d c = Vector3d.add(Vector3d.cross(sp, sq), p);
        Vector3d d = Vector3d.sub(p, Vector3d.cross(sp, sq));

        return intersectLineTriangle(new Vertex(q), new Vertex(q2), new Vertex(d), new Vertex(p2), new Vertex(c), c);

    }

    private boolean intersectLineLineSegmentUnidirectionalFast(Vector3dd p, Vector3dd p2, Vector3d sp, Vector3d dir, BigDecimal len, Vector3dd q, Vector3dd q2) {

        Vector3d sq = Vector3d.sub(q2, q);
        //        Vector3d c = Vector3d.add(Vector3d.cross(sp, sq), p, null);
        //        Vector3d d = Vector3d.sub(p, Vector3d.cross(sp, sq));
        //
        //        return intersectLineTriangleSuperFast(q, q2, d, p2, c);

        Vector3d cross = Vector3d.cross(sq, sp);
        Vector3d c = Vector3d.add(cross, q);
        Vector3d d = Vector3d.sub(q, cross);

        return intersectLineTriangleSuperFast(p, q2, d, q2, c, dir, len);

    }

    private Vector3d intersectLineLineSegmentUnidirectional2(Vector3dd p, Vector3dd p2, Vector3dd q, Vector3dd q2) {


        Vector3d sp = Vector3d.sub(p2, p);
        Vector3d sq = Vector3d.sub(q2, q);
        Vector3d c = Vector3d.add(Vector3d.cross(sp, sq), p);
        Vector3d d = Vector3d.sub(p, Vector3d.cross(sp, sq));

        return intersectLineTriangle(new Vertex(q), new Vertex(q2), new Vertex(d), new Vertex(p2), new Vertex(c), c) ? c : null;

    }


    private IntersectionInfo getTriangleTriangleIntersection(Vector3r[] ov, Vector3r[] tv, GData origin, GData target, Vector3r dir, Vector3r dirN, RationalMatrix m, RationalMatrix minv, PerspectiveCalculator pc, SlicerProSettings ss) {

        ArrayList<Vector3r> insideTarget = new ArrayList<Vector3r>();
        ArrayList<Vector3r> insideOrigin = new ArrayList<Vector3r>();

        Vector3r[] target2D = new Vector3r[3];
        for(int i = 0; i < 3; i++) {
            target2D[i] = pc.getScreenCoordinatesFrom3D(new Vector3r(tv[i]), m);
        }
        Vector3r[] origin2D = new Vector3r[3];
        for(int i = 0; i < 3; i++) {
            origin2D[i] = pc.getScreenCoordinatesFrom3D(new Vector3r(ov[i]), m);
        }

        final Vertex dir0 = new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE);

        // How many points are inside the target?

        for(int i = 0; i < 3; i++) {
            Vector3r ip = new Vector3r();
            if (intersectRayTriangle(origin2D[i], dir0, target2D[0], target2D[1], target2D[2], ip)) {
                insideTarget.add(ip);
            }
        }

        // How many points are inside the origin?

        for(int i = 0; i < 3; i++) {
            Vector3r ip = new Vector3r();
            if (intersectRayTriangle(target2D[i], dir0, origin2D[0], origin2D[1], origin2D[2], ip)) {
                insideOrigin.add(ip);
            }
        }

        // Return if all points are inside the target
        if (insideTarget.size() == 3) {
            insideOrigin.clear();
            // Project points on the target plane
            ArrayList<Vector3r> iT = new ArrayList<Vector3r>();
            {
                ArrayList<Vector3r> insideTarget2 = new ArrayList<Vector3r>();
                insideTarget2.addAll(insideTarget);
                insideTarget.clear();
                for (Vector3r v : insideTarget2) {
                    Vector3r pv = new Vector3r();
                    v = pc.get3DCoordinatesFromScreen(v, minv);
                    projectRayOnTrianglePlane(new Vector3r(v.X, v.Y, v.Z) , dirN, tv[0], tv[1], tv[2], pv);
                    iT.add(pv);
                }
            }
            return new IntersectionInfo(target, origin, iT);
        }

        // Return if all points are inside the origin
        if (insideOrigin.size() == 3) {
            insideTarget.clear();
            // Project points on the target plane
            ArrayList<Vector3r> iT = new ArrayList<Vector3r>();
            {
                ArrayList<Vector3r> insideOrigin2 = new ArrayList<Vector3r>();
                insideOrigin2.addAll(insideOrigin);
                insideOrigin.clear();
                for (Vector3r v : insideOrigin2) {
                    Vector3r pv = new Vector3r();
                    v = pc.get3DCoordinatesFromScreen(v, minv);
                    projectRayOnTrianglePlane(new Vector3r(v.X, v.Y, v.Z) , dirN, tv[0], tv[1], tv[2], pv);
                    iT.add(pv);
                }
            }
            return new IntersectionInfo(target, origin, iT);
        }

        ArrayList<Vector3r> sideIntersections = new ArrayList<Vector3r>();

        // Calculate line intersections
        {
            Vector3r li = new Vector3r();
            Vector3r li2 = new Vector3r();
            switch (intersectLineLineSegment2DExact(target2D[0], target2D[1], origin2D[0], origin2D[1], li, li2)) {
            case 2:
                sideIntersections.add(li2);
            case 1:
                sideIntersections.add(li);
            default:
            }
        }
        {
            Vector3r li = new Vector3r();
            Vector3r li2 = new Vector3r();
            switch  (intersectLineLineSegment2DExact(target2D[0], target2D[1], origin2D[1], origin2D[2], li, li2)) {
            case 2:
                sideIntersections.add(li2);
            case 1:
                sideIntersections.add(li);
            default:
            }
        }
        {
            Vector3r li = new Vector3r();
            Vector3r li2 = new Vector3r();
            switch  (intersectLineLineSegment2DExact(target2D[0], target2D[1], origin2D[2], origin2D[0], li, li2)) {
            case 2:
                sideIntersections.add(li2);
            case 1:
                sideIntersections.add(li);
            default:
            }
        }
        {
            Vector3r li = new Vector3r();
            Vector3r li2 = new Vector3r();
            switch  (intersectLineLineSegment2DExact(target2D[1], target2D[2], origin2D[0], origin2D[1], li, li2)) {
            case 2:
                sideIntersections.add(li2);
            case 1:
                sideIntersections.add(li);
            default:
            }
        }
        {
            Vector3r li = new Vector3r();
            Vector3r li2 = new Vector3r();
            switch (intersectLineLineSegment2DExact(target2D[1], target2D[2], origin2D[1], origin2D[2], li, li2)) {
            case 2:
                sideIntersections.add(li2);
            case 1:
                sideIntersections.add(li);
            default:
            }
        }
        {
            Vector3r li = new Vector3r();
            Vector3r li2 = new Vector3r();
            switch  (intersectLineLineSegment2DExact(target2D[1], target2D[2], origin2D[2], origin2D[0], li, li2)) {
            case 2:
                sideIntersections.add(li2);
            case 1:
                sideIntersections.add(li);
            default:
            }
        }
        {
            Vector3r li = new Vector3r();
            Vector3r li2 = new Vector3r();
            switch  (intersectLineLineSegment2DExact(target2D[2], target2D[0], origin2D[0], origin2D[1], li, li2)) {
            case 2:
                sideIntersections.add(li2);
            case 1:
                sideIntersections.add(li);
            default:
            }
        }
        {
            Vector3r li = new Vector3r();
            Vector3r li2 = new Vector3r();
            switch  (intersectLineLineSegment2DExact(target2D[2], target2D[0], origin2D[1], origin2D[2], li, li2)) {
            case 2:
                sideIntersections.add(li2);
            case 1:
                sideIntersections.add(li);
            }
        }
        {
            Vector3r li = new Vector3r();
            Vector3r li2 = new Vector3r();
            switch (intersectLineLineSegment2DExact(target2D[2], target2D[0], origin2D[2], origin2D[0], li, li2)) {
            case 2:
                sideIntersections.add(li2);
            case 1:
                sideIntersections.add(li);
            default:
            }
        }

        // Return null if NO intersection is found
        if (sideIntersections.isEmpty()) {
            return null;
        } else {

            // Check for identical vertices and remove them
            {

                ArrayList<Vector3r> allVertices = new ArrayList<Vector3r>();

                for (Iterator<Vector3r> it = sideIntersections.iterator(); it.hasNext();) {
                    Vector3r v = it.next();
                    boolean notRemoved = true;
                    for (Vector3r v2 : allVertices) {
                        if (v.equals2d(v2)) {
                            it.remove();
                            notRemoved = false;
                            break;
                        }
                    }
                    if (notRemoved) {
                        allVertices.add(v);
                    }
                }
                for (Iterator<Vector3r> it = insideOrigin.iterator(); it.hasNext();) {
                    Vector3r v = it.next();
                    boolean notRemoved = true;
                    for (Vector3r v2 : allVertices) {
                        if (v.equals2d(v2)) {
                            it.remove();
                            notRemoved = false;
                            break;
                        }
                    }
                    if (notRemoved) {
                        allVertices.add(v);
                    }
                }
                for (Iterator<Vector3r> it = insideTarget.iterator(); it.hasNext();) {
                    Vector3r v = it.next();
                    boolean notRemoved = true;
                    for (Vector3r v2 : allVertices) {
                        if (v.equals2d(v2)) {
                            it.remove();
                            notRemoved = false;
                            break;
                        }
                    }
                    if (notRemoved) {
                        allVertices.add(v);
                    }
                }

                // Assuming that there are intersections, because all trivial cases were checked before
                // MARK Validate state

                final int size = allVertices.size();
                switch (size) {
                case 0: // Fall through
                case 1:
                case 2:
                    return null;
                case 3: // Triangle
                    break;
                case 4: // Four corners or more (up to 7)
                case 5:
                case 6:
                case 7:
                {
                    ArrayList<Vector3r> rv = new ArrayList<Vector3r>();
                    rv.addAll(convexHull(allVertices));
                    allVertices.clear();
                    allVertices.addAll(rv);
                }
                break;
                default:
                    break;
                }

                // Project points on the target plane
                {
                    ArrayList<Vector3r> allVertices2 = new ArrayList<Vector3r>();
                    allVertices2.addAll(allVertices);
                    allVertices.clear();
                    for (Vector3r v : allVertices2) {
                        Vector3r v3 = new Vector3r();
                        v = pc.get3DCoordinatesFromScreen(v, minv);
                        projectRayOnTrianglePlane(new Vector3r(v.X, v.Y, v.Z) , dirN, tv[0], tv[1], tv[2], v3);
                        allVertices.add(v3);
                    }
                    // Return the intersection info
                    return new IntersectionInfo(target, origin, allVertices);
                }
            }
        }
    }

    private GData3 checkNormal(GData3 g3, Matrix4f vport) {
        Vertex[] v = triangles.get(g3);

        Vector4f n = new Vector4f();
        n.setW(1f);
        n.setX((v[2].y - v[0].y) * (v[1].z - v[0].z) - (v[2].z - v[0].z) * (v[1].y - v[0].y));
        n.setY((v[2].z - v[0].z) * (v[1].x - v[0].x) - (v[2].x - v[0].x) * (v[1].z - v[0].z));
        n.setZ((v[2].x - v[0].x) * (v[1].y - v[0].y) - (v[2].y - v[0].y) * (v[1].x - v[0].x));
        Matrix4f.transform(vport, n, n);
        Vector4f.sub(n, new Vector4f(vport.m03, vport.m13, vport.m23, 0f), n);
        if (n.z > 0f ^ Editor3DWindow.getWindow().hasBfcToggle()) {
            return new GData3(g3.colourNumber, g3.r, g3.g, g3.b, g3.a, v[0], v[2], v[1], View.DUMMY_REFERENCE, linkedDatFile);
        } else {
            return null;
        }

    }

    private ArrayList<Vector3r> convexHull(ArrayList<Vector3r> allVertices) {

        ArrayList<ArrayList<Vector3r>> perms = permute(allVertices);

        for (ArrayList<Vector3r> permutation : perms) {

            ArrayList<Vector3r[]> lines = new ArrayList<Vector3r[]>();

            int sm1 = permutation.size() - 1;
            for (int i = 0; i < sm1; i++) {
                Vector3r v1 = permutation.get(i);
                Vector3r v2 = permutation.get(i + 1);
                lines.add(new Vector3r[]{v1, v2});
            }

            lines.add(new Vector3r[]{ permutation.get(sm1), permutation.get(0)});

            boolean skipIt = false;
            for (int j = 2; j < sm1; j++) {
                Vector3r[] l1 = lines.get(0);
                Vector3r[] l2 = lines.get(j);
                if (ills(l1[0], l1[1], l2[0], l2[1])) {
                    skipIt = true;
                    break;
                }
                if (skipIt) break;
            }
            sm1++;
            for (int i = 1; i < sm1; i++) {
                if (skipIt) break;
                for (int j = i + 2; j < sm1; j++) {
                    Vector3r[] l1 = lines.get(i);
                    Vector3r[] l2 = lines.get(j);
                    if (ills(l1[0], l1[1], l2[0], l2[1])) {
                        skipIt = true;
                        break;
                    }
                }
            }
            if (!skipIt) {
                return permutation;
            }
        }

        return allVertices;
    }

    private <T> ArrayList<ArrayList<T>> permute(List<T> num) {
        ArrayList<ArrayList<T>> result = new ArrayList<ArrayList<T>>();

        for (int i = 0; i < num.size(); i++) {
            ArrayList<T> first = new ArrayList<T>();
            T item = num.get(i);
            first.add(item);
            permuteHelper(i, result, first, item, num);
        }

        return result;
    }

    private <T> void permuteHelper(int removeAt, ArrayList<ArrayList<T>> result, ArrayList<T> first, T item, List<T> num) {
        ArrayList<T> nextRemaining = new ArrayList<T>();
        nextRemaining.addAll(num);
        nextRemaining.remove(removeAt);
        if (nextRemaining.isEmpty()) {
            result.add(first);
        } else {
            for (int i = 0; i < nextRemaining.size(); i++) {
                T item2 = nextRemaining.get(i);
                ArrayList<T> nextHead = new ArrayList<T>();
                nextHead.addAll(first);
                nextHead.add(item2);
                permuteHelper(i, result, nextHead, item2, nextRemaining);
            }
        }
    }

    public void intersector(final IntersectorSettings ins, boolean syncWithTextEditor) {
        Composite3D c3d =  linkedDatFile.getLastSelectedComposite();
        NLogger.debug(getClass(), "Intersector - (C) Nils Schmidt 2015"); //$NON-NLS-1$
        NLogger.debug(getClass(), "======================"); //$NON-NLS-1$
        if (c3d != null) {

            final int[] isCancelled = new int[]{0};


            final Set<GData3> trisToHide = new HashSet<GData3>();
            final Set<GData4> quadsToHide = new HashSet<GData4>();

            final Set<GData2> linesToDelete = new HashSet<GData2>();
            final Set<GData3> trisToDelete = new HashSet<GData3>();
            final Set<GData4> quadsToDelete = new HashSet<GData4>();
            final Set<GData5> condlinesToDelete = new HashSet<GData5>();

            NLogger.debug(getClass(), "Get target surfaces to parse."); //$NON-NLS-1$

            final HashSet<GData> targetSurfs = new HashSet<GData>();
            {
                Set<GData3> tris = triangles.keySet();
                for (GData3 tri : tris) {
                    if (!hiddenData.contains(tri)) {
                        targetSurfs.add(tri);
                    }
                }
            }
            {
                Set<GData4> qs = quads.keySet();
                for (GData4 quad : qs) {
                    if (!hiddenData.contains(quad)) {
                        targetSurfs.add(quad);
                    }
                }
            }

            NLogger.debug(getClass(), "Cleanup the selection."); //$NON-NLS-1$

            for(Iterator<GData3> ti = selectedTriangles.iterator(); ti.hasNext();) {
                GData3 tri = ti.next();
                if (!lineLinkedToVertices.containsKey(tri)) {
                    ti.remove();
                }
            }
            for(Iterator<GData4> qi = selectedQuads.iterator(); qi.hasNext();) {
                GData4 quad = qi.next();
                if (!lineLinkedToVertices.containsKey(quad)) {
                    qi.remove();
                }
            }
            for(Iterator<GData2> li = selectedLines.iterator(); li.hasNext();) {
                GData2 line = li.next();
                if (!lineLinkedToVertices.containsKey(line)) {
                    li.remove();
                }
            }
            for(Iterator<GData5> ci = selectedCondlines.iterator(); ci.hasNext();) {
                GData5 condline = ci.next();
                if (!lineLinkedToVertices.containsKey(condline)) {
                    ci.remove();
                }
            }

            final ArrayList<GData> originObjects = new ArrayList<GData>();
            originObjects.addAll(selectedTriangles);
            originObjects.addAll(selectedQuads);
            originObjects.addAll(selectedLines);
            originObjects.addAll(selectedCondlines);

            // Remove adjacent non-selected surfaces from targetSurfs!
            {
                TreeSet<Vertex> verts = new TreeSet<Vertex>();
                for (GData g3 : selectedTriangles) {
                    Vertex[] verts2 = triangles.get(g3);
                    for (Vertex vertex : verts2) {
                        verts.add(vertex);
                    }
                }
                for (GData g4 : selectedQuads) {
                    Vertex[] verts2 = quads.get(g4);
                    for (Vertex vertex : verts2) {
                        verts.add(vertex);
                    }
                }
                for (Vertex vertex : verts) {
                    Collection<GData> surfs = getLinkedSurfaces(vertex);
                    for (GData g : surfs) {
                        switch (g.type()) {
                        case 3:
                            trisToHide.add((GData3) g);
                            break;
                        case 4:
                            quadsToHide.add((GData4) g);
                            break;
                        default:
                            break;
                        }
                    }
                    targetSurfs.removeAll(surfs);
                }
            }

            clearSelection();

            final ArrayList<IntersectionInfoWithColour> intersections = new ArrayList<IntersectionInfoWithColour>();
            final Set<GData2> newLines =  Collections.newSetFromMap(new ThreadsafeHashMap<GData2, Boolean>());
            final Set<GData3> newTriangles = Collections.newSetFromMap(new ThreadsafeHashMap<GData3, Boolean>());
            final Set<GData5> newCondlines =  Collections.newSetFromMap(new ThreadsafeHashMap<GData5, Boolean>());
            try
            {
                new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
                {
                    @Override
                    public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                    {
                        try
                        {
                            monitor.beginTask("Running Intersector (this may take some time)", IProgressMonitor.UNKNOWN); //$NON-NLS-1$ I18N

                            {

                                final Set<IntersectionInfoWithColour> intersectionSet = Collections.newSetFromMap(new ThreadsafeHashMap<IntersectionInfoWithColour, Boolean>());

                                final int iterations = originObjects.size();
                                final int chunks = View.NUM_CORES;
                                final Thread[] threads = new Thread[chunks];

                                final String surfCount = "/" + iterations;//$NON-NLS-1$
                                final AtomicInteger counter2 = new AtomicInteger(0);

                                int lastend = 0;
                                for (int j = 0; j < chunks; ++j) {
                                    final int[] start = new int[] { lastend };
                                    lastend = Math.round(iterations / chunks * (j + 1));
                                    final int[] end = new int[] { lastend };
                                    if (j == chunks - 1) {
                                        end[0] = iterations;
                                    }
                                    threads[j] = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            for (int k = start[0]; k < end[0]; k++) {
                                                monitor.subTask(counter2.toString() + surfCount);
                                                GData o = originObjects.get(k);
                                                /* Check if the monitor has been canceled */
                                                if (monitor.isCanceled()) {
                                                    isCancelled[0] = 1;
                                                    return;
                                                }
                                                counter2.incrementAndGet();
                                                IntersectionInfoWithColour ii = getIntersectionInfo(o, targetSurfs, ins);
                                                if (ii != null) {
                                                    intersectionSet.add(ii);
                                                    switch (o.type()) {
                                                    case 2:
                                                        linesToDelete.add((GData2) o);
                                                        break;
                                                    case 3:
                                                        trisToDelete.add((GData3) o);
                                                        break;
                                                    case 4:
                                                        quadsToDelete.add((GData4) o);
                                                        break;
                                                    case 5:
                                                        condlinesToDelete.add((GData5) o);
                                                        break;
                                                    default:
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    });
                                    threads[j].start();
                                }
                                boolean isRunning = true;
                                while (isRunning) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                    }
                                    isRunning = false;
                                    for (Thread thread : threads) {
                                        if (thread.isAlive())
                                            isRunning = true;
                                    }
                                }
                                intersections.addAll(intersectionSet);
                            }

                            if (isCancelled[0] > 0) return;

                            for (GData t : targetSurfs) {
                                switch (t.type()) {
                                case 3:
                                    trisToHide.add((GData3) t);
                                    break;
                                case 4:
                                    quadsToHide.add((GData4) t);
                                    break;
                                default:
                                    break;
                                }
                            }

                            NLogger.debug(getClass(), "Create new faces."); //$NON-NLS-1$

                            {
                                final int iterations = intersections.size();
                                final int chunks = View.NUM_CORES;
                                final Thread[] threads = new Thread[chunks];


                                final String maxIterations = "/" + iterations;//$NON-NLS-1$
                                final AtomicInteger counter2 = new AtomicInteger(0);

                                int lastend = 0;
                                for (int j = 0; j < chunks; ++j) {
                                    final int[] start = new int[] { lastend };
                                    lastend = Math.round(iterations / chunks * (j + 1));
                                    final int[] end = new int[] { lastend };
                                    if (j == chunks - 1) {
                                        end[0] = iterations;
                                    }
                                    threads[j] = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            for (int k = start[0]; k < end[0]; k++) {
                                                monitor.subTask(counter2.toString() + maxIterations);
                                                IntersectionInfoWithColour info = intersections.get(k);
                                                if (monitor.isCanceled()) {
                                                    isCancelled[0] = 2;
                                                    return;
                                                }
                                                counter2.incrementAndGet();

                                                final ArrayList<Vector3dd> av = info.getAllVertices();
                                                final ArrayList<GColour> cols = info.getColours();
                                                final ArrayList<Integer> ts = info.getIsLine();

                                                newTriangles.addAll(MathHelper.triangulatePointGroups(cols, av, ts, View.DUMMY_REFERENCE, linkedDatFile));
                                                newLines.addAll(MathHelper.triangulatePointGroups2(cols, av, ts, View.DUMMY_REFERENCE, linkedDatFile));
                                                newCondlines.addAll(MathHelper.triangulatePointGroups5(cols, av, ts, View.DUMMY_REFERENCE, linkedDatFile));
                                            }
                                        }
                                    });
                                    threads[j].start();
                                }
                                boolean isRunning = true;
                                while (isRunning) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                    }
                                    isRunning = false;
                                    for (Thread thread : threads) {
                                        if (thread.isAlive())
                                            isRunning = true;
                                    }
                                }
                            }

                        }
                        finally
                        {
                            monitor.done();
                        }
                    }
                });
            }
            catch (InvocationTargetException consumed) {
            } catch (InterruptedException consumed) {
            }


            NLogger.debug(getClass(), "Check for identical vertices and collinearity."); //$NON-NLS-1$
            final Set<GData3> trisToDelete2 = new HashSet<GData3>();
            {
                for (GData3 g3 : newTriangles) {
                    Vertex[] verts = triangles.get(g3);
                    Set<Vertex> verts2 = new TreeSet<Vertex>();
                    for (Vertex vert : verts) {
                        verts2.add(vert);
                    }
                    if (verts2.size() < 3 || g3.isCollinear()) {
                        trisToDelete2.add(g3);
                    }
                }
            }


            if (isCancelled[0] == 0) {
                NLogger.debug(getClass(), "Hide intersecting faces."); //$NON-NLS-1$

                selectedTriangles.addAll(trisToHide);
                selectedQuads.addAll(quadsToHide);
                selectedData.addAll(selectedTriangles);
                selectedData.addAll(selectedQuads);
                hideSelection();

                clearSelection();

                NLogger.debug(getClass(), "Delete old selected objects."); //$NON-NLS-1$

                selectedLines.addAll(linesToDelete);
                selectedTriangles.addAll(trisToDelete);
                selectedQuads.addAll(quadsToDelete);
                selectedCondlines.addAll(condlinesToDelete);
                selectedData.addAll(selectedLines);
                selectedData.addAll(selectedTriangles);
                selectedData.addAll(selectedQuads);
                selectedData.addAll(selectedCondlines);
                delete(false, false);
            } else {
                clearSelection();
            }

            // Append the new data
            for (GData3 tri : newTriangles) {
                linkedDatFile.addToTail(tri);
            }

            for (GData2 lin : newLines) {
                linkedDatFile.addToTail(lin);
            }

            for (GData5 clin : newCondlines) {
                linkedDatFile.addToTail(clin);
            }

            NLogger.debug(getClass(), "Delete new, but invalid objects."); //$NON-NLS-1$

            newTriangles.removeAll(trisToDelete2);
            selectedTriangles.addAll(trisToDelete2);
            selectedData.addAll(selectedTriangles);
            delete(false, false);

            // Round to 6 decimal places

            selectedLines.addAll(newLines);
            selectedTriangles.addAll(newTriangles);
            selectedCondlines.addAll(newCondlines);
            selectedData.addAll(selectedLines);
            selectedData.addAll(selectedTriangles);
            selectedData.addAll(selectedCondlines);

            NLogger.debug(getClass(), "Round."); //$NON-NLS-1$
            roundSelection(6, 10, true, false);

            clearSelection();
            if (syncWithTextEditor) {
                setModified(true, true);
            } else {
                setModified_NoSync();
            }

            NLogger.debug(getClass(), "Done."); //$NON-NLS-1$

            validateState();

        } else {
            NLogger.debug(getClass(), "No 3D view selected. Cancel process."); //$NON-NLS-1$
        }
    }

    private IntersectionInfoWithColour getIntersectionInfo(GData origin, HashSet<GData> targetSurfs, IntersectorSettings ins) {

        final BigDecimal MIN_DIST = new BigDecimal(".0001"); //$NON-NLS-1$

        final int ot = origin.type();

        final ArrayList<ArrayList<Vector3dd>> fixedIntersectionLines = new ArrayList<ArrayList<Vector3dd>>();
        final ArrayList<ArrayList<Vector3dd>> allLines = new ArrayList<ArrayList<Vector3dd>>();
        final ArrayList<Vector3dd> fixedVertices = new ArrayList<Vector3dd>();

        final HashMap<GData, ArrayList<Vector3dd>> intersections = new HashMap<GData, ArrayList<Vector3dd>>();

        Vertex[] ov = null;
        switch (ot) {
        case 2:
            ov = lines.get(origin);
            break;
        case 3:
            ov = triangles.get(origin);
            break;
        case 4:
            ov = quads.get(origin);
            break;
        case 5:
            ov = condlines.get(origin);
            break;
        default:
            return null;
        }

        if (ot == 2 || ot == 5) {

            if (getLineFaceIntersection(fixedVertices, targetSurfs, ov)) {

                final ArrayList<Vector3dd> resultVertices = new ArrayList<Vector3dd>();
                final ArrayList<GColour> resultColours = new ArrayList<GColour>();
                final ArrayList<Integer> resultIsLine = new ArrayList<Integer>();

                Vector3dd start = fixedVertices.get(0);

                Vector3d normal = new Vector3d(new BigDecimal(1.34), new BigDecimal(-.77), new BigDecimal(2));
                normal.normalise(normal);
                for (int i = 1; i < fixedVertices.size(); i++) {
                    Vector3dd end = fixedVertices.get(i);


                    if (ins.isColourise()) {

                        // Calculate pseudo mid-point
                        Vector3dd mid = new Vector3dd();
                        mid.setX(start.X.multiply(MathHelper.R1).add(end.X.multiply(MathHelper.R2.add(MathHelper.R3))));
                        mid.setY(start.Y.multiply(MathHelper.R1).add(end.Y.multiply(MathHelper.R2.add(MathHelper.R3))));
                        mid.setZ(start.Z.multiply(MathHelper.R1).add(end.Z.multiply(MathHelper.R2.add(MathHelper.R3))));

                        int intersectionCount = 0;


                        for (GData3 g3 : triangles.keySet()) {
                            Vertex[] v = triangles.get(g3);
                            if (intersectRayTriangle(mid, normal, new Vector3dd(v[0]), new Vector3dd(v[1]), new Vector3dd(v[2]))) {
                                intersectionCount += 1;
                            }
                        }
                        for (GData4 g4 : quads.keySet()) {
                            Vertex[] v = quads.get(g4);
                            if (
                                    intersectRayTriangle(mid, normal, new Vector3dd(v[0]), new Vector3dd(v[1]), new Vector3dd(v[2])) ||
                                    intersectRayTriangle(mid, normal, new Vector3dd(v[2]), new Vector3dd(v[3]), new Vector3dd(v[0]))) {
                                intersectionCount += 1;
                            }
                        }
                        resultVertices.add(start);
                        resultVertices.add(end);
                        if (ot == 2) {
                            resultIsLine.add(1);
                        } else {
                            resultVertices.add(new Vector3dd(ov[2]));
                            resultVertices.add(new Vector3dd(ov[3]));
                            resultIsLine.add(2);
                        }
                        if (intersectionCount == 1) {
                            resultColours.add(View.getLDConfigColour(0));
                        } else if (intersectionCount % 2 == 0) {
                            resultColours.add(View.getLDConfigColour(28));
                        } else {
                            resultColours.add(View.getLDConfigColour(1));
                        }

                    } else {
                        final float R, G, B, A;
                        final int CN;
                        resultVertices.add(start);
                        resultVertices.add(end);
                        if (ot == 2) {
                            GData2 origin2 = (GData2) origin;
                            CN = origin2.colourNumber;
                            R = origin2.r;
                            G = origin2.g;
                            B = origin2.b;
                            A = origin2.a;
                            resultIsLine.add(1);
                        } else {
                            GData5 origin2 = (GData5) origin;
                            CN = origin2.colourNumber;
                            R = origin2.r;
                            G = origin2.g;
                            B = origin2.b;
                            A = origin2.a;
                            resultVertices.add(new Vector3dd(ov[2]));
                            resultVertices.add(new Vector3dd(ov[3]));
                            resultIsLine.add(2);
                        }
                        resultColours.add(new GColour(CN, R, G, B, A));
                    }
                    start = end;
                }


                return new IntersectionInfoWithColour(resultColours, resultVertices, resultIsLine);
            } else {
                return null;
            }

        } else {
            for (GData targetSurf : targetSurfs) {
                final int tt = targetSurf.type();

                if (ot == 3 && tt == 3) {

                    Vertex[] tv = triangles.get(targetSurf);

                    getTriangleTriangleIntersection(intersections, targetSurf, ov, tv, ins, false, false);

                } else if (ot == 4 && tt == 4) {

                    Vertex[] tv = quads.get(targetSurf);

                    Vertex[] ov1 = new Vertex[]{ov[0], ov[1], ov[2]};
                    Vertex[] ov2 = new Vertex[]{ov[2], ov[3], ov[0]};
                    Vertex[] tv1 = new Vertex[]{tv[0], tv[1], tv[2]};
                    Vertex[] tv2 = new Vertex[]{tv[2], tv[3], tv[0]};

                    getTriangleTriangleIntersection(intersections, targetSurf, ov1, tv1, ins, true, true);
                    getTriangleTriangleIntersection(intersections, targetSurf, ov1, tv2, ins, true, true);
                    getTriangleTriangleIntersection(intersections, targetSurf, ov2, tv1, ins, true, true);
                    getTriangleTriangleIntersection(intersections, targetSurf, ov2, tv2, ins, true, true);

                } else if (ot == 4 && tt == 3) {
                    Vertex[] tv = triangles.get(targetSurf);

                    Vertex[] tv1 = new Vertex[]{tv[0], tv[1], tv[2]};
                    Vertex[] ov1 = new Vertex[]{ov[0], ov[1], ov[2]};
                    Vertex[] ov2 = new Vertex[]{ov[2], ov[3], ov[0]};

                    getTriangleTriangleIntersection(intersections, targetSurf, ov1, tv1, ins, true, false);
                    getTriangleTriangleIntersection(intersections, targetSurf, ov2, tv1, ins, true, false);

                } else if (ot == 3 && tt == 4) {

                    Vertex[] tv = quads.get(targetSurf);

                    Vertex[] ov1 = new Vertex[]{ov[0], ov[1], ov[2]};
                    Vertex[] tv1 = new Vertex[]{tv[0], tv[1], tv[2]};
                    Vertex[] tv2 = new Vertex[]{tv[2], tv[3], tv[0]};

                    getTriangleTriangleIntersection(intersections, targetSurf, ov1, tv1, ins, false, true);
                    getTriangleTriangleIntersection(intersections, targetSurf, ov1, tv2, ins, false, true);

                }
            }

            for (GData key : intersections.keySet()) {
                ArrayList<Vector3dd> line = intersections.get(key);
                if (line.size() > 1) {
                    fixedIntersectionLines.add(line);
                }
            }

            // Check intersections within the fixed intersection lines
            {
                ArrayList<ArrayList<Vector3dd>> linesToRemove = new ArrayList<ArrayList<Vector3dd>>();
                ArrayList<ArrayList<Vector3dd>> newLines = new ArrayList<ArrayList<Vector3dd>>();
                for (Iterator<ArrayList<Vector3dd>> iterator = fixedIntersectionLines.iterator(); iterator.hasNext();) {
                    ArrayList<Vector3dd> line = iterator.next();
                    ArrayList<Vector3d> intersect = new ArrayList<Vector3d>();
                    for (ArrayList<Vector3dd> line2 : fixedIntersectionLines) {
                        if (line2 != line) {
                            TreeSet<Vector3dd> allVertices = new TreeSet<Vector3dd>();
                            for(int l = 0; l < 2; l++) {
                                allVertices.add(line.get(l));
                                allVertices.add(line2.get(l));
                            }
                            if (allVertices.size() == 4) {
                                Vector3d ip = intersectLineLineSegmentUnidirectional2(line.get(0), line.get(1), line2.get(0), line2.get(1));
                                if (ip != null) {
                                    intersect.add(ip);
                                }
                            }
                        }
                    }
                    if (!intersect.isEmpty()) {
                        TreeMap<BigDecimal, Vector3d> linePoints = new TreeMap<BigDecimal, Vector3d>();
                        Vector3d start = line.get(0);
                        Vector3d end = line.get(1);
                        for (Vector3d v : intersect) {
                            BigDecimal dist = Vector3d.manhattan(v, start);
                            linePoints.put(dist, v);
                        }
                        BigDecimal dist = Vector3d.manhattan(end, start);
                        linePoints.put(dist, end);

                        for (BigDecimal d : linePoints.keySet()) {
                            end = linePoints.get(d);
                            ArrayList<Vector3dd> newLine = new ArrayList<Vector3dd>();
                            newLine.add(new Vector3dd(start));
                            newLine.add(new Vector3dd(end));
                            newLines.add(newLine);
                            start = end;
                        }
                        linesToRemove.add(line);
                    }
                }
                fixedIntersectionLines.removeAll(linesToRemove);
                fixedIntersectionLines.addAll(newLines);
            }

            final ArrayList<Vector3dd> resultVertices = new ArrayList<Vector3dd>();
            final ArrayList<GColour> resultColours = new ArrayList<GColour>();
            final ArrayList<Integer> resultIsLine = new ArrayList<Integer>();

            Vector3d originalNormal = null;

            switch (ot) {
            case 3:
            {
                fixedVertices.add(new Vector3dd(ov[0]).round());
                fixedVertices.add(new Vector3dd(ov[1]).round());
                fixedVertices.add(new Vector3dd(ov[2]).round());
                GData3 g3 = (GData3) origin;
                originalNormal = new Vector3d(new Vertex(g3.xn, g3.yn, g3.zn));
            }
            break;
            case 4:
            {
                fixedVertices.add(new Vector3dd(ov[0]).round());
                fixedVertices.add(new Vector3dd(ov[1]).round());
                fixedVertices.add(new Vector3dd(ov[2]).round());
                fixedVertices.add(new Vector3dd(ov[3]).round());
                GData4 g4 = (GData4) origin;
                originalNormal = new Vector3d(new Vertex(g4.xn, g4.yn, g4.zn));
            }
            break;
            default:
                return null;
            }

            {
                final TreeSet<Vector3dd> allVertices = new TreeSet<Vector3dd>();
                for (ArrayList<Vector3dd> l : fixedIntersectionLines) {
                    allVertices.add(l.get(0).round());
                    allVertices.add(l.get(1).round());
                    //                    resultVertices.add(l.get(0).round());
                    //                    resultVertices.add(l.get(1).round());
                    //                    resultColours.add(new GColour(-1, 0f, 1f, 0f, 1f));
                    //                    resultIsLine.add(1);
                }
                allVertices.removeAll(fixedVertices);
                fixedVertices.addAll(allVertices);
            }

            allLines.addAll(fixedIntersectionLines);
            if (!allLines.isEmpty()) {
                final int vc = fixedVertices.size();
                for (int i = 0; i < vc; i++) {
                    for (int j = 0; j < vc; j++) {
                        if (i == j) continue;
                        boolean intersect = false;
                        Vector3dd v1 = fixedVertices.get(i);
                        Vector3dd v2 = fixedVertices.get(j);
                        int lc = allLines.size();
                        for (int k = 0; k < lc; k++) {
                            ArrayList<Vector3dd> l = allLines.get(k);
                            Vector3dd v3 = l.get(0);
                            Vector3dd v4 = l.get(1);
                            if (!v1.equals(v3) && !v1.equals(v4) && !v2.equals(v3) && !v2.equals(v4) && intersectLineLineSegmentUnidirectional(v1, v2, v3, v4)) {
                                intersect = true;
                                break;
                            }
                            if (Vector3dd.manhattan(v1, v3).compareTo(MIN_DIST) < 0 && Vector3dd.manhattan(v2, v4).compareTo(MIN_DIST) < 0 ||
                                    Vector3dd.manhattan(v2, v3).compareTo(MIN_DIST) < 0 && Vector3dd.manhattan(v1, v4).compareTo(MIN_DIST) < 0) {
                                intersect = true;
                                break;
                            }
                        }
                        if (intersect) {
                            continue;
                        } else {
                            BigDecimal dist = Vector3dd.manhattan(v1, v2);
                            if (dist.compareTo(MIN_DIST) > 0) {
                                ArrayList<Vector3dd> nl = new ArrayList<Vector3dd>();
                                nl.add(v1);
                                nl.add(v2);
                                allLines.add(nl);

                                //     // MARK DEBUG ONLY!
                                //     resultVertices.add(v1);
                                //     resultVertices.add(v2);
                                //     resultColours.add(new GColour(-1, 1f, 0f, 1f, 1f));
                                //     resultIsLine.add(1);
                            }
                        }
                    }
                }

                int lc = allLines.size();
                {
                    int removed = 0;
                    for (int i = 0; i + removed < lc; i++) {
                        for (int j = i + 1; j + removed < lc; j++) {
                            TreeSet<Vector3dd> allVertices = new TreeSet<Vector3dd>();
                            for(int l = 0; l < 2; l++) {
                                allVertices.add(allLines.get(i).get(l));
                                allVertices.add(allLines.get(j).get(l));
                            }
                            if (allVertices.size() == 2) {
                                removed += 1;
                                allLines.remove(j);
                            }
                        }
                    }

                    lc = allLines.size();

                    removed = 0;
                    for (int i = 0; i + removed < lc; i++) {
                        TreeSet<Vector3dd> allVertices = new TreeSet<Vector3dd>();
                        allVertices.add(allLines.get(i).get(0));
                        allVertices.add(allLines.get(i).get(1));
                        if (allVertices.size() == 1) {
                            removed += 1;
                            allLines.remove(i);
                        }
                    }
                }

                lc = allLines.size();

                for (int i = 0; i < lc; i++) {
                    for (int j = i + 1; j < lc; j++) {
                        for (int k = j + 1; k < lc; k++) {
                            TreeSet<Vector3dd> allVertices = new TreeSet<Vector3dd>();
                            for(int l = 0; l < 2; l++) {
                                allVertices.add(allLines.get(i).get(l).round());
                                allVertices.add(allLines.get(j).get(l).round());
                                allVertices.add(allLines.get(k).get(l).round());
                            }
                            if (allVertices.size() == 3) {
                                Vector3dd[] triVerts = new Vector3dd[3];
                                int l = 0;
                                for (Vector3dd v : allVertices) {
                                    triVerts[l] = v;
                                    l++;
                                }
                                boolean isInsideTriangle = false;
                                Vector3d normal = Vector3d.cross(Vector3d.sub(triVerts[2], triVerts[0]), Vector3d.sub(triVerts[1], triVerts[0]));
                                normal.normalise(normal);
                                for (Vector3dd fixed : fixedVertices) {
                                    if (fixed.equals(triVerts[0])) continue;
                                    if (fixed.equals(triVerts[1])) continue;
                                    if (fixed.equals(triVerts[2])) continue;
                                    if (intersectRayTriangle(fixed, normal, triVerts[0], triVerts[1], triVerts[2])) {
                                        isInsideTriangle = true;
                                        break;
                                    }
                                }
                                if (isInsideTriangle) continue;

                                // Check collinearity
                                {
                                    Vector3d vertexA = new Vector3d(triVerts[0]);
                                    Vector3d vertexB = new Vector3d(triVerts[1]);
                                    Vector3d vertexC = new Vector3d(triVerts[2]);
                                    Vector3d vertexA2 = new Vector3d();
                                    Vector3d vertexB2 = new Vector3d();
                                    Vector3d.sub(vertexA, vertexC, vertexA2);
                                    Vector3d.sub(vertexB, vertexC, vertexB2);
                                    if  (Vector3d.angle(vertexA2, vertexB2) < Threshold.collinear_angle_minimum) continue;
                                }

                                if (MathHelper.directionOfVectors(normal, originalNormal) == 1) {
                                    resultVertices.add(triVerts[0]);
                                    resultVertices.add(triVerts[1]);
                                    resultVertices.add(triVerts[2]);
                                } else {
                                    resultVertices.add(triVerts[0]);
                                    resultVertices.add(triVerts[2]);
                                    resultVertices.add(triVerts[1]);
                                }


                                if (ins.isColourise()) {

                                    // Calculate pseudo mid-point
                                    Vector3dd mid = new Vector3dd();

                                    mid.setX(triVerts[0].X.multiply(MathHelper.R1).add(triVerts[1].X.multiply(MathHelper.R2)).add(triVerts[2].X.multiply(MathHelper.R3)));
                                    mid.setY(triVerts[0].Y.multiply(MathHelper.R1).add(triVerts[1].Y.multiply(MathHelper.R2)).add(triVerts[2].Y.multiply(MathHelper.R3)));
                                    mid.setZ(triVerts[0].Z.multiply(MathHelper.R1).add(triVerts[1].Z.multiply(MathHelper.R2)).add(triVerts[2].Z.multiply(MathHelper.R3)));

                                    int intersectionCount = 0;

                                    for (GData3 g3 : triangles.keySet()) {
                                        Vertex[] v = triangles.get(g3);
                                        if (intersectRayTriangle(mid, normal, new Vector3dd(v[0]), new Vector3dd(v[1]), new Vector3dd(v[2]))) {
                                            intersectionCount += 1;
                                        }
                                    }
                                    for (GData4 g4 : quads.keySet()) {
                                        Vertex[] v = quads.get(g4);
                                        if (
                                                intersectRayTriangle(mid, normal, new Vector3dd(v[0]), new Vector3dd(v[1]), new Vector3dd(v[2])) ||
                                                intersectRayTriangle(mid, normal, new Vector3dd(v[2]), new Vector3dd(v[3]), new Vector3dd(v[0]))) {
                                            intersectionCount += 1;
                                        }
                                    }

                                    if (intersectionCount == 1) {
                                        resultColours.add(View.getLDConfigColour(7));
                                    } else if (intersectionCount % 2 == 0) {
                                        resultColours.add(View.getLDConfigColour(14));
                                    } else {
                                        resultColours.add(View.getLDConfigColour(11));
                                    }

                                } else {
                                    final float R, G, B, A;
                                    final int CN;
                                    if (ot == 3) {
                                        GData3 origin2 = (GData3) origin;
                                        CN = origin2.colourNumber;
                                        R = origin2.r;
                                        G = origin2.g;
                                        B = origin2.b;
                                        A = origin2.a;
                                    } else {
                                        GData4 origin2 = (GData4) origin;
                                        CN = origin2.colourNumber;
                                        R = origin2.r;
                                        G = origin2.g;
                                        B = origin2.b;
                                        A = origin2.a;
                                    }
                                    resultColours.add(new GColour(CN, R, G, B, A));
                                }
                                resultIsLine.add(0);
                            }
                        }

                    }
                }

                if (resultVertices.isEmpty()) return null;
                return new IntersectionInfoWithColour(resultColours, resultVertices, resultIsLine);
            } else {
                return null;
            }
        }

    }

    private final BigDecimal TOLERANCER = new BigDecimal("0.00001"); //$NON-NLS-1$ .00001
    private final BigDecimal ZEROTR = new BigDecimal("-0.00001"); //$NON-NLS-1$
    private final BigDecimal ONETR = new BigDecimal("1.00001"); //$NON-NLS-1$
    private boolean intersectRayTriangle(Vector3dd orig, Vector3d dir, Vector3dd vert0, Vector3dd vert1, Vector3dd vert2) {
        BigDecimal diskr = BigDecimal.ZERO;
        BigDecimal inv_diskr = BigDecimal.ZERO;
        Vector3d corner1 = Vector3d.sub(vert1, vert0);
        Vector3d corner2 = Vector3d.sub(vert2, vert0);
        Vector3d pvec = Vector3d.cross(dir, corner2);
        diskr = Vector3d.dotP(corner1, pvec);
        if (diskr.abs().compareTo(TOLERANCER) < 0)
            return false;
        inv_diskr = BigDecimal.ONE.divide(diskr, Threshold.mc);
        Vector3d tvec = Vector3d.sub(orig, vert0);
        BigDecimal u = Vector3d.dotP(tvec, pvec).multiply(inv_diskr);
        if (u.compareTo(ZEROTR) < 0 || u.compareTo(ONETR) > 0)
            return false;
        Vector3d qvec = Vector3d.cross(tvec, corner1);
        BigDecimal v = Vector3d.dotP(dir, qvec).multiply(inv_diskr);
        if (v.compareTo(ZEROTR) < 0 || u.add(v).compareTo(ONETR) > 0)
            return false;
        return true;
    }

    private boolean getLineFaceIntersection(ArrayList<Vector3dd> fixedVertices, HashSet<GData> targetSurfs, Vertex[] ov) {

        TreeMap<BigDecimal, Vector3d> linePoints = new TreeMap<BigDecimal, Vector3d>();
        Vector3d start = new Vector3d(ov[0]);
        Vector3d end = new Vector3d(ov[1]);

        for (GData g : targetSurfs) {
            Vector3d intersection = new Vector3d();
            switch (g.type()) {
            case 3:
            {
                Vertex[] verts = triangles.get(g);
                if (intersectLineTriangle(ov[0], ov[1], verts[0], verts[1], verts[2], intersection)) {
                    fixedVertices.add(new Vector3dd(intersection));
                    BigDecimal dist = Vector3d.manhattan(intersection, start);
                    linePoints.put(dist, intersection);
                }
            }
            break;
            case 4:
            {
                Vertex[] verts = quads.get(g);
                if (
                        intersectLineTriangle(ov[0], ov[1], verts[0], verts[1], verts[2], intersection) ||
                        intersectLineTriangle(ov[0], ov[1], verts[2], verts[3], verts[0], intersection)) {
                    fixedVertices.add(new Vector3dd(intersection));
                    BigDecimal dist = Vector3d.manhattan(intersection, start);
                    linePoints.put(dist, intersection);
                }
            }
            break;
            default:
                break;
            }
        }

        if (fixedVertices.isEmpty()) {
            return false;
        } else {
            fixedVertices.clear();
            BigDecimal dist = Vector3d.manhattan(end, start);
            linePoints.put(BigDecimal.ZERO, start);
            linePoints.put(dist, end);
            for (BigDecimal d : linePoints.keySet()) {
                fixedVertices.add(new Vector3dd(linePoints.get(d)));
            }
            return true;
        }

    }

    private void getTriangleTriangleIntersection(HashMap<GData, ArrayList<Vector3dd>> intersections, GData target, Vertex[] ov, Vertex[] tv, IntersectorSettings ins, boolean originIsQuad, boolean targetIsQuad) {
        ArrayList<Vector3dd> result2 = null;
        if (intersections.containsKey(target)) {
            result2 = intersections.get(target);
        } else {
            result2 = new ArrayList<Vector3dd>();
            intersections.put(target, result2);
        }

        final TreeSet<Vector3dd> result = new TreeSet<Vector3dd>();

        {
            Vector3dd r = new Vector3dd();
            if (intersectLineTriangle(tv[0], tv[1], ov[0], ov[1], ov[2], r)) {
                result.add(r.round());
            }
        }
        {
            Vector3dd r = new Vector3dd();
            if (intersectLineTriangle(tv[1], tv[2], ov[0], ov[1], ov[2], r)) {
                result.add(r.round());
            }
        }
        if (!targetIsQuad) {
            Vector3dd r = new Vector3dd();
            if (intersectLineTriangle(tv[2], tv[0], ov[0], ov[1], ov[2], r)) {
                result.add(r.round());
            }
        }
        {
            Vector3dd r = new Vector3dd();
            if (intersectLineTriangle(ov[0], ov[1], tv[0], tv[1], tv[2], r)) {
                result.add(r.round());
            }
        }
        {
            Vector3dd r = new Vector3dd();
            if (intersectLineTriangle(ov[1], ov[2], tv[0], tv[1], tv[2], r)) {
                result.add(r.round());
            }
        }
        if (!originIsQuad) {
            Vector3dd r = new Vector3dd();
            if (intersectLineTriangle(ov[2], ov[0], tv[0], tv[1], tv[2], r)) {
                result.add(r.round());
            }
        }
        result.removeAll(result2);
        result2.addAll(result);
    }

    public void splitQuads(boolean isModified) {

        final Set<GData4> quadsToDelete = new HashSet<GData4>();
        final Set<GData3> newTriangles = new HashSet<GData3>();

        final Set<GData4> quadsToParse = new HashSet<GData4>();

        quadsToParse.addAll(selectedQuads);

        clearSelection();

        for (Iterator<GData4> ig = quadsToParse.iterator(); ig.hasNext();) {
            GData4 g = ig.next();
            if (!lineLinkedToVertices.containsKey(g) || g.isCollinear()) {
                ig.remove();
            }
        }
        if (quadsToParse.isEmpty()) {
            return;
        } else {
            setModified_NoSync();
        }
        quadsToDelete.addAll(quadsToParse);

        for (GData4 g4 : quadsToParse) {
            Vertex[] v = quads.get(g4);

            switch (g4.getHourglassConfiguration()) {
            case 0:
            {
                GData3 tri1 = new GData3(g4.colourNumber, g4.r, g4.g, g4.b, g4.a, v[0], v[1], v[2], View.DUMMY_REFERENCE, linkedDatFile);
                GData3 tri2 = new GData3(g4.colourNumber, g4.r, g4.g, g4.b, g4.a, v[2], v[3], v[0], View.DUMMY_REFERENCE, linkedDatFile);
                newTriangles.add(tri1);
                newTriangles.add(tri2);
                linkedDatFile.insertAfter(g4, tri2);
                linkedDatFile.insertAfter(g4, tri1);
            }
            break;
            case 1:
            {
                GData3 tri1 = new GData3(g4.colourNumber, g4.r, g4.g, g4.b, g4.a, v[1], v[0], v[2], View.DUMMY_REFERENCE, linkedDatFile);
                GData3 tri2 = new GData3(g4.colourNumber, g4.r, g4.g, g4.b, g4.a, v[2], v[3], v[1], View.DUMMY_REFERENCE, linkedDatFile);
                newTriangles.add(tri1);
                newTriangles.add(tri2);
                linkedDatFile.insertAfter(g4, tri2);
                linkedDatFile.insertAfter(g4, tri1);
            }
            break;
            case 2:
            {
                GData3 tri1 = new GData3(g4.colourNumber, g4.r, g4.g, g4.b, g4.a, v[0], v[1], v[3], View.DUMMY_REFERENCE, linkedDatFile);
                GData3 tri2 = new GData3(g4.colourNumber, g4.r, g4.g, g4.b, g4.a, v[3], v[2], v[0], View.DUMMY_REFERENCE, linkedDatFile);
                newTriangles.add(tri1);
                newTriangles.add(tri2);
                linkedDatFile.insertAfter(g4, tri2);
                linkedDatFile.insertAfter(g4, tri1);
            }
            }

        }

        selectedQuads.addAll(quadsToDelete);
        selectedData.addAll(quadsToDelete);
        delete(false, false);

        selectedTriangles.addAll(newTriangles);
        selectedData.addAll(newTriangles);

        if (isModified && isModified()) {
            syncWithTextEditors(true);
        }
        validateState();

    }

    public void condlineToLine(boolean isModified) {

        final Set<GData5> condlinesToDelete = new HashSet<GData5>();
        final Set<GData2> newLines = new HashSet<GData2>();

        final Set<GData5> condlinesToParse = new HashSet<GData5>();

        condlinesToParse.addAll(selectedCondlines);

        clearSelection();

        for (Iterator<GData5> ig = condlinesToParse.iterator(); ig.hasNext();) {
            GData5 g = ig.next();
            if (!lineLinkedToVertices.containsKey(g)) {
                ig.remove();
            }
        }
        if (condlinesToParse.isEmpty()) {
            return;
        } else {
            setModified_NoSync();
        }
        condlinesToDelete.addAll(condlinesToParse);

        for (GData5 g5 : condlinesToParse) {
            Vertex[] v = condlines.get(g5);
            GData2 tri1 = new GData2(g5.colourNumber, g5.r, g5.g, g5.b, g5.a, v[0], v[1], View.DUMMY_REFERENCE, linkedDatFile);
            newLines.add(tri1);
            linkedDatFile.insertAfter(g5, tri1);
        }

        selectedCondlines.addAll(condlinesToDelete);
        selectedData.addAll(condlinesToDelete);
        delete(false, false);

        selectedLines.addAll(newLines);
        selectedData.addAll(newLines);

        if (isModified && isModified()) {
            syncWithTextEditors(true);
        }
        validateState();

    }

    public void lines2pattern() {

        if (linkedDatFile.isReadOnly()) return;

        final BigDecimal MIN_DIST = new BigDecimal(".0001"); //$NON-NLS-1$

        final Set<GData2> originalSelectionLines = new HashSet<GData2>();
        final Set<GData3> originalSelectionTriangles = new HashSet<GData3>();
        final Set<GData4> originalSelectionQuads = new HashSet<GData4>();
        final Set<GData3> newTriangles = new HashSet<GData3>();

        final ArrayList<ArrayList<Vector3dd>> linesToParse = new ArrayList<ArrayList<Vector3dd>>();
        final ArrayList<ArrayList<Vector3dd>> colourLines = new ArrayList<ArrayList<Vector3dd>>();
        final ArrayList<ArrayList<Vector3dh>> linesToParseHashed = new ArrayList<ArrayList<Vector3dh>>();

        final HashMap<ArrayList<Vector3dd>, GColour> colours = new HashMap<ArrayList<Vector3dd>, GColour>();

        final int chunks = View.NUM_CORES;

        originalSelectionLines.addAll(selectedLines);
        originalSelectionTriangles.addAll(selectedTriangles);
        originalSelectionQuads.addAll(selectedQuads);

        final Vector3d originalNormal = new Vector3d(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE);

        // Verify
        try
        {
            new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
            {
                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                {
                    try
                    {
                        monitor.beginTask("Running Lines2Pattern (this may take some time)", IProgressMonitor.UNKNOWN); //$NON-NLS-1$ I18N

                        for (GData3 g3 : selectedTriangles) {
                            TreeSet<Vertex> vs = new TreeSet<Vertex>();
                            Vertex[] verts = triangles.get(g3);
                            for (Vertex v : verts) {
                                vs.add(v);
                            }
                            if (vs.size() != 3) return;
                            {
                                ArrayList<Vector3dd> l = new ArrayList<Vector3dd>();
                                l.add(new Vector3dd(verts[0]));
                                l.add(new Vector3dd(verts[1]));
                                linesToParse.add(l);
                            }
                            {
                                ArrayList<Vector3dd> l = new ArrayList<Vector3dd>();
                                l.add(new Vector3dd(verts[1]));
                                l.add(new Vector3dd(verts[2]));
                                linesToParse.add(l);
                            }
                            {
                                ArrayList<Vector3dd> l = new ArrayList<Vector3dd>();
                                l.add(new Vector3dd(verts[2]));
                                l.add(new Vector3dd(verts[0]));
                                linesToParse.add(l);
                            }
                        }

                        for (GData4 g4 : selectedQuads) {
                            TreeSet<Vertex> vs = new TreeSet<Vertex>();
                            Vertex[] verts = quads.get(g4);
                            for (Vertex v : verts) {
                                vs.add(v);
                            }
                            if (vs.size() != 4) return;
                            {
                                ArrayList<Vector3dd> l = new ArrayList<Vector3dd>();
                                l.add(new Vector3dd(verts[0]));
                                l.add(new Vector3dd(verts[1]));
                                linesToParse.add(l);
                            }
                            {
                                ArrayList<Vector3dd> l = new ArrayList<Vector3dd>();
                                l.add(new Vector3dd(verts[1]));
                                l.add(new Vector3dd(verts[2]));
                                linesToParse.add(l);
                            }
                            {
                                ArrayList<Vector3dd> l = new ArrayList<Vector3dd>();
                                l.add(new Vector3dd(verts[2]));
                                l.add(new Vector3dd(verts[3]));
                                linesToParse.add(l);
                            }
                            {
                                ArrayList<Vector3dd> l = new ArrayList<Vector3dd>();
                                l.add(new Vector3dd(verts[3]));
                                l.add(new Vector3dd(verts[0]));
                                linesToParse.add(l);
                            }
                            {
                                ArrayList<Vector3dd> l = new ArrayList<Vector3dd>();
                                l.add(new Vector3dd(verts[1]));
                                l.add(new Vector3dd(verts[3]));
                                linesToParse.add(l);
                            }
                        }

                        TreeSet<Vertex> m1 = new TreeSet<Vertex>();
                        TreeSet<Vertex> m2 = new TreeSet<Vertex>();
                        for (GData2 g2 : selectedLines) {
                            Vertex[] verts = lines.get(g2);
                            for (Vertex v : verts) {
                                if (g2.colourNumber == 24) {
                                    if (m1.contains(v)) {
                                        m2.add(v);
                                    } else {
                                        m1.add(v);
                                    }
                                }
                            }
                            ArrayList<Vector3dd> l = new ArrayList<Vector3dd>();
                            l.add(new Vector3dd(verts[0]));
                            l.add(new Vector3dd(verts[1]));
                            if (g2.colourNumber == 24) {
                                linesToParse.add(l);
                            } else {
                                colourLines.add(l);
                                colours.put(l, new GColour(g2.colourNumber, g2.r, g2.g, g2.b, g2.a));
                            }
                        }
                        if (m1.size() != m2.size()) return;

                        BigDecimal seed = new BigDecimal("1.23456789"); //$NON-NLS-1$
                        BigDecimal seed2 = new BigDecimal("-1.832647382"); //$NON-NLS-1$
                        BigDecimal seed3 = new BigDecimal("1.427637292"); //$NON-NLS-1$
                        Vertex s = new Vertex(seed, seed2, seed3);
                        Vertex p1 = null;
                        Vertex p2 = null;
                        Vertex p3 = null;
                        for (Vertex vertex : m2) {
                            p1 = vertex;
                            break;
                        }
                        if (p1 == null) return;
                        for (Vertex vertex : m2) {
                            if (!vertex.equals(p1)) {
                                p2 = vertex;
                                break;
                            }
                        }
                        if (p2 == null) return;
                        for (Vertex vertex : m2) {
                            if (!vertex.equals(p1) && !vertex.equals(p2)) {
                                p3 = vertex;
                                break;
                            }
                        }
                        if (p3 == null) return;
                        Vector3d a = new Vector3d(p1.X.add(s.X), p1.Y.add(s.Y),p1.Z.add(s.Z));
                        Vector3d b = new Vector3d(p2.X.add(s.X), p2.Y.add(s.Y),p2.Z.add(s.Z));
                        Vector3d c = new Vector3d(p3.X.add(s.X), p3.Y.add(s.Y),p3.Z.add(s.Z));

                        Vector3d pOrigin = new Vector3d(p1);
                        Vector3d n = Vector3d.cross(Vector3d.sub(a, c), Vector3d.sub(b, c));
                        n.normalise(n);
                        originalNormal.setX(n.X);
                        originalNormal.setY(n.Y);
                        originalNormal.setZ(n.Z);
                        BigDecimal EPSILON = new BigDecimal("0.001"); //$NON-NLS-1$
                        for (Vertex vertex : m2) {
                            Vector3d vp = new Vector3d(vertex);
                            if (Vector3d.dotP(Vector3d.sub(pOrigin, vp), n).abs().compareTo(EPSILON) > 0) return;
                        }

                        if (monitor.isCanceled()) {
                            originalSelectionLines.clear();
                        }
                    }
                    finally
                    {
                        monitor.done();
                    }
                }
            });
        }
        catch (InvocationTargetException consumed) {
        } catch (InterruptedException consumed) {
        }

        if (originalSelectionLines.isEmpty()) return;
        clearSelection();

        // Calculate intersecting lines, if needed.
        {
            ArrayList<ArrayList<Vector3dd>> linesToRemove = new ArrayList<ArrayList<Vector3dd>>();
            ArrayList<ArrayList<Vector3dd>> newLines = new ArrayList<ArrayList<Vector3dd>>();
            for (Iterator<ArrayList<Vector3dd>> iterator = linesToParse.iterator(); iterator.hasNext();) {
                ArrayList<Vector3dd> line = iterator.next();
                ArrayList<Vector3d> intersect = new ArrayList<Vector3d>();
                for (ArrayList<Vector3dd> line2 : linesToParse) {
                    if (line2 != line) {
                        TreeSet<Vector3dd> allVertices = new TreeSet<Vector3dd>();
                        for(int l = 0; l < 2; l++) {
                            allVertices.add(line.get(l));
                            allVertices.add(line2.get(l));
                        }
                        if (allVertices.size() == 4) {
                            Vector3d ip = intersectLineLineSegmentUnidirectional2(line.get(0), line.get(1), line2.get(0), line2.get(1));
                            if (ip != null) {
                                intersect.add(ip);
                            }
                        }
                    }
                }
                if (!intersect.isEmpty()) {
                    TreeMap<BigDecimal, Vector3d> linePoints = new TreeMap<BigDecimal, Vector3d>();
                    Vector3d start = line.get(0);
                    Vector3d end = line.get(1);
                    for (Vector3d v : intersect) {
                        BigDecimal dist = Vector3d.manhattan(v, start);
                        linePoints.put(dist, v);
                    }
                    BigDecimal dist = Vector3d.manhattan(end, start);
                    linePoints.put(dist, end);

                    for (BigDecimal d : linePoints.keySet()) {
                        end = linePoints.get(d);
                        ArrayList<Vector3dd> newLine = new ArrayList<Vector3dd>();
                        newLine.add(new Vector3dd(start));
                        newLine.add(new Vector3dd(end));
                        newLines.add(newLine);
                        start = end;
                    }
                    linesToRemove.add(line);
                }
            }
            linesToParse.removeAll(linesToRemove);
            linesToParse.addAll(newLines);
        }

        final ArrayList<Vector3dd> resultVertices = new ArrayList<Vector3dd>();
        final ArrayList<GColour> resultColours = new ArrayList<GColour>();
        final ArrayList<Integer> resultIsLine = new ArrayList<Integer>();

        final Set<ArrayList<Vector3dd>> colourLines2 = Collections.newSetFromMap(new ThreadsafeHashMap<ArrayList<Vector3dd>, Boolean>());
        final ThreadsafeHashMap<ArrayList<Vector3dd> , GColour> colours2 = new ThreadsafeHashMap<ArrayList<Vector3dd>, GColour>();
        final Thread[] colourThreads = new Thread[chunks];

        // Spread coloured lines
        {

            try
            {
                new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
                {
                    @Override
                    public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                    {
                        try
                        {

                            if (!colourLines.isEmpty()) {

                                final ArrayList<Vector3dd> fixedVertices = new ArrayList<Vector3dd>();
                                final ArrayList<Vector3dd> colourVertices = new ArrayList<Vector3dd>();
                                final TreeMap<Vector3dd, GColour> vertexColour = new TreeMap<Vector3dd, GColour>();
                                {
                                    final TreeSet<Vector3dd> allVertices = new TreeSet<Vector3dd>();
                                    for (ArrayList<Vector3dd> l : linesToParse) {
                                        allVertices.add(l.get(0).round());
                                        allVertices.add(l.get(1).round());
                                    }
                                    for (ArrayList<Vector3dd> l : colourLines) {
                                        Vector3dd vc1 = l.get(0).round();
                                        Vector3dd vc2 = l.get(1).round();
                                        if (!vertexColour.containsKey(vc1)) {
                                            vertexColour.put(vc1, colours.get(l));
                                        } else {
                                            GColour gc = vertexColour.get(vc1);
                                            GColour gc2 = colours.get(l);
                                            if (gc.getColourNumber() != gc2.getColourNumber()) vertexColour.remove(vc1);
                                        }
                                        if (!vertexColour.containsKey(vc2)) {
                                            vertexColour.put(vc2, colours.get(l));
                                        } else {
                                            GColour gc = vertexColour.get(vc2);
                                            GColour gc2 = colours.get(l);
                                            if (gc.getColourNumber() != gc2.getColourNumber()) vertexColour.remove(vc2);
                                        }
                                        colourVertices.add(vc1);
                                        colourVertices.add(vc2);
                                    }
                                    fixedVertices.addAll(allVertices);
                                }

                                final ArrayList<ArrayList<Vector3dd>> fixedLinesToParse = new ArrayList<ArrayList<Vector3dd>>();
                                fixedLinesToParse.addAll(linesToParse);

                                final int vc = colourVertices.size();
                                final int vc2 = fixedVertices.size();

                                final AtomicInteger counter2 = new AtomicInteger(0);

                                for (int j = 0; j < chunks; ++j) {
                                    final int[] start = new int[] { j };
                                    colourThreads[j] = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            int counter = start[0];
                                            for (int i = 0; i < vc; i++) {
                                                if (counter == 0) {
                                                    counter = chunks;
                                                    counter2.incrementAndGet();
                                                    if (monitor.isCanceled()) {
                                                        return;
                                                    }
                                                    Vector3dd v1 = colourVertices.get(i);
                                                    for (int j = 0; j < vc2; j++) {
                                                        boolean intersect = false;
                                                        Vector3dd v2 = fixedVertices.get(j);
                                                        Vector3d sp = Vector3dd.sub(v2, v1);
                                                        Vector3d dir = new Vector3d();
                                                        BigDecimal len = sp.normalise(dir);
                                                        int lc = fixedLinesToParse.size();
                                                        for (int k = 0; k < lc; k++) {
                                                            ArrayList<Vector3dd> l = fixedLinesToParse.get(k);
                                                            Vector3dd v3 = l.get(0);
                                                            Vector3dd v4 = l.get(1);
                                                            if (!v1.equals(v3) && !v1.equals(v4) && !v2.equals(v3) && !v2.equals(v4) && intersectLineLineSegmentUnidirectionalFast(v1, v2, sp, dir, len, v3, v4)) {
                                                                intersect = true;
                                                                break;
                                                            }
                                                        }
                                                        if (intersect) {
                                                            continue;
                                                        } else {
                                                            BigDecimal dist = Vector3dd.manhattan(v1, v2);
                                                            if (dist.compareTo(MIN_DIST) > 0) {
                                                                if (vertexColour.containsKey(v1) && vertexColour.get(v1) != null) {
                                                                    ArrayList<Vector3dd> nl = new ArrayList<Vector3dd>();
                                                                    nl.add(v1);
                                                                    nl.add(v2);
                                                                    colours2.put(nl, vertexColour.get(v1));
                                                                    colourLines2.add(nl);
                                                                } else if (vertexColour.containsKey(v2) && vertexColour.get(v2) != null) {
                                                                    ArrayList<Vector3dd> nl = new ArrayList<Vector3dd>();
                                                                    nl.add(v1);
                                                                    nl.add(v2);
                                                                    colours2.put(nl, vertexColour.get(v2));
                                                                    colourLines2.add(nl);
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                counter -= 1;
                                            }
                                        }
                                    });
                                    colourThreads[j].start();
                                }
                            }
                        } finally {
                            monitor.done();
                        }
                    }
                });
            }
            catch (InvocationTargetException consumed) {
            } catch (InterruptedException consumed) {
            }
        }

        final ArrayList<Vector3dd> fixedVertices = new ArrayList<Vector3dd>();
        final ArrayList<Vector3dh> fixedVertices2 = new ArrayList<Vector3dh>();

        {
            final TreeSet<Vector3dd> allVertices = new TreeSet<Vector3dd>();
            for (ArrayList<Vector3dd> l : linesToParse) {
                allVertices.add(l.get(0).round());
                allVertices.add(l.get(1).round());
            }
            fixedVertices.addAll(allVertices);
        }

        if (!linesToParse.isEmpty()) {

            final ThreadsafeHashMap<Vector3dh, HashSet<Vector3dh>> neighbours = new ThreadsafeHashMap<Vector3dh, HashSet<Vector3dh>>();
            try
            {
                new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
                {
                    @Override
                    public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                    {
                        try
                        {
                            monitor.beginTask("Running Lines2Pattern (this may take some time)", IProgressMonitor.UNKNOWN); //$NON-NLS-1$ I18N

                            final Thread[] threads = new Thread[1];

                            {
                                TreeMap<Vector3dd, Vector3dh> hashedRelation = new TreeMap<Vector3dd, Vector3dh>();
                                for (Vector3dd v : fixedVertices) {
                                    Vector3dh vh;
                                    if (hashedRelation.containsKey(v)) {
                                        vh = hashedRelation.get(v);
                                    } else {
                                        vh = new Vector3dh(v);
                                        hashedRelation.put(v, vh);
                                    }

                                    fixedVertices2.add(vh);
                                }
                                for (ArrayList<Vector3dd> l : linesToParse) {

                                    Vector3dd v1nh = l.get(0).round();
                                    Vector3dd v2nh = l.get(1).round();

                                    Vector3dh v1;
                                    Vector3dh v2;

                                    if (hashedRelation.containsKey(v1nh)) {
                                        v1 = hashedRelation.get(v1nh);
                                    } else {
                                        v1 = new Vector3dh(v1nh);
                                        hashedRelation.put(v1nh, v1);
                                    }

                                    if (hashedRelation.containsKey(v2nh)) {
                                        v2 = hashedRelation.get(v2nh);
                                    } else {
                                        v2 = new Vector3dh(v2nh);
                                        hashedRelation.put(v2nh, v2);
                                    }

                                    ArrayList<Vector3dh> newline = new ArrayList<Vector3dh>();
                                    newline.add(v1);
                                    newline.add(v2);
                                    linesToParseHashed.add(newline);

                                }
                            }

                            final int vc = fixedVertices2.size();
                            final String vertCount = "/" + vc + ")"; //$NON-NLS-1$ //$NON-NLS-2$

                            threads[0] = new Thread(new Runnable() {
                                @Override
                                public void run() {

                                    for (int i = 0; i < vc; i++) {

                                        monitor.subTask("Detecting new edges (" + i + vertCount); //$NON-NLS-1$ I18N

                                        if (monitor.isCanceled()) {
                                            break;
                                        }

                                        Vector3dh v1 = fixedVertices2.get(i);
                                        for (int j = i + 1; j < vc; j++) {
                                            boolean intersect = false;
                                            Vector3dh v2 = fixedVertices2.get(j);

                                            Vector3d sp = Vector3dd.sub(v2, v1);
                                            Vector3d dir = new Vector3d();
                                            BigDecimal len = sp.normalise(dir);
                                            Iterator<ArrayList<Vector3dh>> li = linesToParseHashed.iterator();
                                            while (li.hasNext()) {
                                                ArrayList<Vector3dh> l = li.next();
                                                Vector3dh v3 = l.get(0);
                                                Vector3dh v4 = l.get(1);
                                                if (!v1.equals(v3) && !v1.equals(v4) && !v2.equals(v3) && !v2.equals(v4)) {
                                                    if (intersectLineLineSegmentUnidirectionalFast(v1, v2, sp, dir, len,  v3, v4)) {
                                                        intersect = true;
                                                        break;
                                                    }
                                                    // if (Vector3dd.manhattan(v1, v3).compareTo(MIN_DIST) < 0 && Vector3dd.manhattan(v2, v4).compareTo(MIN_DIST) < 0 ||
                                                    //  Vector3dd.manhattan(v2, v3).compareTo(MIN_DIST) < 0 && Vector3dd.manhattan(v1, v4).compareTo(MIN_DIST) < 0) {
                                                    //  intersect = true;
                                                    //  break;
                                                    // }
                                                }
                                            }
                                            if (!intersect) {
                                                BigDecimal dist = Vector3dd.manhattan(v1, v2);
                                                if (dist.compareTo(MIN_DIST) > 0) {
                                                    ArrayList<Vector3dh> nl = new ArrayList<Vector3dh>();
                                                    nl.add(v1);
                                                    nl.add(v2);
                                                    linesToParseHashed.add(nl);
                                                }
                                            }
                                        }
                                    }
                                }
                            });
                            threads[0].start();
                            boolean isRunning = true;
                            while (isRunning) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                }
                                isRunning = false;
                                if (threads[0].isAlive())
                                    isRunning = true;
                            }
                            if (!colourLines.isEmpty()) {
                                isRunning = true;
                                while (isRunning) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                    }
                                    isRunning = false;
                                    for (Thread thread : colourThreads) {
                                        if (thread.isAlive())
                                            isRunning = true;
                                    }
                                }
                            }
                            if (monitor.isCanceled()) {
                                selectedLines.addAll(originalSelectionLines);
                                selectedTriangles.addAll(originalSelectionTriangles);
                                selectedQuads.addAll(originalSelectionQuads);
                                selectedData.addAll(originalSelectionTriangles);
                                selectedData.addAll(originalSelectionQuads);
                                selectedData.addAll(originalSelectionLines);
                                originalSelectionLines.clear();
                                return;
                            } else {
                                colourLines.addAll(colourLines2);
                                colours.putAll(colours2);
                                linesToParse.clear();
                                fixedVertices2.clear();
                                for (ArrayList<Vector3dh> l : linesToParseHashed) {
                                    ArrayList<Vector3dd> nl = new ArrayList<Vector3dd>();
                                    nl.add(new Vector3dd(l.get(0)));
                                    nl.add(new Vector3dd(l.get(1)));
                                    linesToParse.add(nl);
                                }
                                linesToParseHashed.clear();
                            }
                        } finally {
                            monitor.done();
                        }
                    }
                });
            }
            catch (InvocationTargetException consumed) {
            } catch (InterruptedException consumed) {
            }

            if (originalSelectionLines.isEmpty()) return;

            int lc = linesToParse.size();
            {
                int removed = 0;
                for (int i = 0; i + removed < lc; i++) {
                    for (int j = i + 1; j + removed < lc; j++) {
                        TreeSet<Vector3dd> allVertices = new TreeSet<Vector3dd>();
                        for(int l = 0; l < 2; l++) {
                            allVertices.add(linesToParse.get(i).get(l));
                            allVertices.add(linesToParse.get(j).get(l));
                        }
                        if (allVertices.size() == 2) {
                            removed += 1;
                            linesToParse.remove(j);
                        }
                    }
                }

                lc = linesToParse.size();

                removed = 0;
                for (int i = 0; i + removed < lc; i++) {
                    TreeSet<Vector3dd> allVertices = new TreeSet<Vector3dd>();
                    allVertices.add(linesToParse.get(i).get(0));
                    allVertices.add(linesToParse.get(i).get(1));
                    if (allVertices.size() == 1) {
                        removed += 1;
                        linesToParse.remove(i);
                    }
                }

                lc = linesToParse.size();

                HashSet<Vector3dh> m1 = new HashSet<Vector3dh>();
                HashSet<Vector3dh> m2 = new HashSet<Vector3dh>();
                HashSet<Vector3dh> m3 = new HashSet<Vector3dh>();
                TreeMap<Vector3dd, Vector3dh> hashedRelation = new TreeMap<Vector3dd, Vector3dh>();
                for (int i = 0; i < lc; i++) {
                    Vector3dd v1nh = linesToParse.get(i).get(0).round();
                    Vector3dd v2nh = linesToParse.get(i).get(1).round();

                    Vector3dh v1;
                    Vector3dh v2;

                    if (hashedRelation.containsKey(v1nh)) {
                        v1 = hashedRelation.get(v1nh);
                    } else {
                        v1 = new Vector3dh(v1nh);
                        hashedRelation.put(v1nh, v1);
                    }

                    if (hashedRelation.containsKey(v2nh)) {
                        v2 = hashedRelation.get(v2nh);
                    } else {
                        v2 = new Vector3dh(v2nh);
                        hashedRelation.put(v2nh, v2);
                    }

                    ArrayList<Vector3dh> newline = new ArrayList<Vector3dh>();
                    newline.add(v1);
                    newline.add(v2);
                    linesToParseHashed.add(newline);

                    if (neighbours.containsKey(v1)) {
                        neighbours.get(v1).add(v2);
                    } else {
                        neighbours.put(v1, new HashSet<Vector3dh>());
                        neighbours.get(v1).add(v2);
                    }
                    if (neighbours.containsKey(v2)) {
                        neighbours.get(v2).add(v1);
                    } else {
                        neighbours.put(v2, new HashSet<Vector3dh>());
                        neighbours.get(v2).add(v1);
                    }
                    if (m1.contains(v1)) {
                        if (m2.contains(v1)) {
                            if (!m3.contains(v1)) {
                                m3.add(v1);
                            }
                        } else {
                            m2.add(v1);
                        }
                    } else {
                        m1.add(v1);
                    }
                    if (m1.contains(v2)) {
                        if (m2.contains(v2)) {
                            if (!m3.contains(v2)) {
                                m3.add(v2);
                            }
                        } else {
                            m2.add(v2);
                        }
                    } else {
                        m1.add(v2);
                    }
                }
                for (Vector3dd v : fixedVertices) {
                    fixedVertices2.add(hashedRelation.get(v));
                }
                m2.removeAll(m3);
                fixedVertices2.removeAll(m2);
            }

            try
            {
                new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
                {
                    @Override
                    public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                    {
                        try
                        {
                            monitor.beginTask("Running Lines2Pattern (this may take some time)", IProgressMonitor.UNKNOWN); //$NON-NLS-1$ I18N

                            final int lc = linesToParseHashed.size();

                            final Thread[] threads = new Thread[chunks];

                            final String vertCount = "/" + lc + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                            final AtomicInteger counter2 = new AtomicInteger(0);

                            final Lock rlock = new ReentrantLock(true);

                            for (int t = 0; t < chunks; ++t) {
                                final int[] start = new int[] { t };
                                threads[t] = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        int counter = start[0];
                                        HashSet<Vector3dh> allVertices = new HashSet<Vector3dh>();
                                        Vector3d normal = null;
                                        for (int i = 0; i < lc; i++) {
                                            if (counter == 0) {
                                                counter = chunks;
                                                monitor.subTask("Triangulate (" + counter2.toString() + vertCount); //$NON-NLS-1$ I18N
                                                counter2.incrementAndGet();
                                                if (monitor.isCanceled()) {
                                                    return;
                                                }
                                                for (int j = i + 1; j < lc; j++) {
                                                    for (int k = j + 1; k < lc; k++) {
                                                        for(int l = 0; l < 2; l++) {
                                                            allVertices.add(linesToParseHashed.get(i).get(l));
                                                            allVertices.add(linesToParseHashed.get(j).get(l));
                                                            allVertices.add(linesToParseHashed.get(k).get(l));
                                                        }
                                                        if (allVertices.size() == 3) {
                                                            Vector3dh[] triVerts = new Vector3dh[3];
                                                            int l = 0;
                                                            for (Vector3dh v : allVertices) {
                                                                triVerts[l] = v;
                                                                l++;
                                                            }
                                                            allVertices.clear();
                                                            boolean isInsideTriangle = false;
                                                            if (normal == null) {
                                                                normal = Vector3d.cross(Vector3d.sub(triVerts[2], triVerts[0]), Vector3d.sub(triVerts[1], triVerts[0]));
                                                                normal.normalise(normal);
                                                            }
                                                            for (Vector3dh fixed : fixedVertices2) {
                                                                if (fixed.equals(triVerts[0])) continue;
                                                                if (fixed.equals(triVerts[1])) continue;
                                                                if (fixed.equals(triVerts[2])) continue;
                                                                Set<Vector3dh> n1 = neighbours.get(triVerts[0]);
                                                                Set<Vector3dh> n2 = neighbours.get(triVerts[1]);
                                                                Set<Vector3dh> n3 = neighbours.get(triVerts[2]);
                                                                int nc = 0;
                                                                if (n1.contains(fixed)) nc += 1;
                                                                if (n2.contains(fixed)) nc += 1;
                                                                if (n3.contains(fixed)) nc += 1;
                                                                if (nc > 1) {
                                                                    if (intersectRayTriangle(fixed, normal, triVerts[0], triVerts[1], triVerts[2])) {
                                                                        isInsideTriangle = true;
                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                            if (isInsideTriangle) continue;

                                                            // Check collinearity
                                                            {
                                                                Vector3d vertexA = new Vector3d(triVerts[0]);
                                                                Vector3d vertexB = new Vector3d(triVerts[1]);
                                                                Vector3d vertexC = new Vector3d(triVerts[2]);
                                                                Vector3d vertexA2 = new Vector3d();
                                                                Vector3d vertexB2 = new Vector3d();
                                                                Vector3d.sub(vertexA, vertexC, vertexA2);
                                                                Vector3d.sub(vertexB, vertexC, vertexB2);
                                                                if  (Vector3d.angle(vertexA2, vertexB2) < Threshold.collinear_angle_minimum) continue;
                                                            }

                                                            {
                                                                HashSet<ArrayList<Vector3dd>> threeLines = new HashSet<ArrayList<Vector3dd>>();
                                                                threeLines.add(linesToParse.get(i));
                                                                threeLines.add(linesToParse.get(j));
                                                                threeLines.add(linesToParse.get(k));
                                                                ArrayList<Vector3dd> intersected = null;
                                                                for (Iterator<ArrayList<Vector3dd>> iterator = threeLines.iterator(); iterator.hasNext();) {
                                                                    ArrayList<Vector3dd> line = iterator.next();
                                                                    Vector3dd v1 = line.get(0);
                                                                    Vector3dd v2 = line.get(1);
                                                                    Vector3d sp = Vector3dd.sub(v2, v1);
                                                                    Vector3d dir = new Vector3d();
                                                                    BigDecimal len = sp.normalise(dir);
                                                                    for (ArrayList<Vector3dd> line2 : colourLines) {
                                                                        if (line2 != line) {
                                                                            TreeSet<Vector3dd> allVertices1 = new TreeSet<Vector3dd>();
                                                                            for(int l1 = 0; l1 < 2; l1++) {
                                                                                allVertices1.add(line.get(l1));
                                                                                allVertices1.add(line2.get(l1));
                                                                            }
                                                                            if (allVertices1.size() == 4) {
                                                                                if (intersectLineLineSegmentUnidirectionalFast(v1, v2, sp, dir, len, line2.get(0), line2.get(1))) {
                                                                                    intersected = line2;
                                                                                    break;
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                    if (intersected != null) {
                                                                        break;
                                                                    }
                                                                }
                                                                rlock.lock();
                                                                if (MathHelper.directionOfVectors(Vector3d.cross(Vector3d.sub(triVerts[2], triVerts[0]), Vector3d.sub(triVerts[1], triVerts[0])), originalNormal) == 1) {
                                                                    resultVertices.add(triVerts[0]);
                                                                    resultVertices.add(triVerts[1]);
                                                                    resultVertices.add(triVerts[2]);
                                                                } else {
                                                                    resultVertices.add(triVerts[0]);
                                                                    resultVertices.add(triVerts[2]);
                                                                    resultVertices.add(triVerts[1]);
                                                                }
                                                                if (intersected != null) {
                                                                    resultColours.add(colours.get(intersected) != null ? colours.get(intersected) : View.getLDConfigColour(16));
                                                                    resultIsLine.add(0);
                                                                } else {
                                                                    resultColours.add(View.getLDConfigColour(16));
                                                                    resultIsLine.add(0);
                                                                }
                                                                rlock.unlock();
                                                            }
                                                        } else {
                                                            allVertices.clear();
                                                        }
                                                    }
                                                }
                                            }
                                            counter -= 1;
                                        }
                                    }
                                });
                                threads[t].start();
                            }
                            boolean isRunning = true;
                            while (isRunning) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                }
                                isRunning = false;
                                for (Thread thread : threads) {
                                    if (thread.isAlive())
                                        isRunning = true;
                                }
                            }
                            if (monitor.isCanceled()) {
                                selectedLines.addAll(originalSelectionLines);
                                selectedTriangles.addAll(originalSelectionTriangles);
                                selectedQuads.addAll(originalSelectionQuads);
                                selectedData.addAll(originalSelectionTriangles);
                                selectedData.addAll(originalSelectionQuads);
                                selectedData.addAll(originalSelectionLines);
                                originalSelectionLines.clear();
                                return;
                            }
                        } finally {
                            monitor.done();
                        }
                    }
                });
            }
            catch (InvocationTargetException consumed) {
            } catch (InterruptedException consumed) {
            }

            if (originalSelectionLines.isEmpty()) return;

            newTriangles.addAll(MathHelper.triangulatePointGroups(resultColours, resultVertices, resultIsLine, View.DUMMY_REFERENCE, linkedDatFile));

            NLogger.debug(getClass(), "Check for identical vertices and collinearity."); //$NON-NLS-1$
            final Set<GData3> trisToDelete2 = new HashSet<GData3>();
            {
                for (GData3 g3 : newTriangles) {
                    Vertex[] verts = triangles.get(g3);
                    Set<Vertex> verts2 = new TreeSet<Vertex>();
                    for (Vertex vert : verts) {
                        verts2.add(vert);
                    }
                    if (verts2.size() < 3 || g3.isCollinear()) {
                        trisToDelete2.add(g3);
                    }
                }
            }

            // Append the new data
            for (GData3 tri : newTriangles) {
                linkedDatFile.addToTail(tri);
            }

            NLogger.debug(getClass(), "Delete new, but invalid objects."); //$NON-NLS-1$

            newTriangles.removeAll(trisToDelete2);
            selectedTriangles.addAll(trisToDelete2);
            selectedData.addAll(selectedTriangles);
            delete(false, false);

            // Round to 6 decimal places

            selectedTriangles.addAll(newTriangles);
            selectedData.addAll(selectedTriangles);

            NLogger.debug(getClass(), "Round."); //$NON-NLS-1$
            roundSelection(6, 10, true, false);

            setModified(true, true);

            NLogger.debug(getClass(), "Done."); //$NON-NLS-1$

            validateState();
        }
    }

    public void pathTruder(final PathTruderSettings ps) {
        if (linkedDatFile.isReadOnly()) return;

        final Set<GData2> originalSelection = new HashSet<GData2>();
        final Set<GData2> newLines = new HashSet<GData2>();
        final Set<GData3> newTriangles = new HashSet<GData3>();
        final Set<GData4> newQuads = new HashSet<GData4>();

        final ArrayList<GData2> shape1 = new ArrayList<GData2>();
        final ArrayList<GData2> shape2 = new ArrayList<GData2>();

        final ArrayList<GData2> path1 = new ArrayList<GData2>();
        final ArrayList<GData2> path2 = new ArrayList<GData2>();

        final ArrayList<GData2> path1endSegments = new ArrayList<GData2>();
        final ArrayList<GData2> path2endSegments = new ArrayList<GData2>();

        final ArrayList<GData2> lineIndicators = new ArrayList<GData2>();

        originalSelection.addAll(selectedLines);

        // Validate and evaluate selection
        {
            final GData2 shape1Normal;
            final GData2 shape2Normal;
            GData2 shape1Normal2 = null;
            GData2 shape2Normal2 = null;
            GData data2draw = linkedDatFile.getDrawChainStart();
            while ((data2draw = data2draw.getNext()) != null) {
                if (originalSelection.contains(data2draw)) {
                    GData2 line = (GData2) data2draw;
                    switch (line.colourNumber) {
                    case 1:
                        path1.add(line);
                        break;
                    case 2:
                        path2.add(line);
                        break;
                    case 5:
                        shape1.add(line);
                        break;
                    case 7:
                        lineIndicators.add(line);
                        break;
                    case 13:
                        shape2.add(line);
                        break;
                    case 4:
                        if (shape1Normal2 == null) {
                            shape1Normal2 = line;
                        } else {
                            return;
                        }
                        break;
                    case 12:
                        if (shape2Normal2 == null) {
                            shape2Normal2 = line;
                        } else {
                            return;
                        }
                        break;
                    default:
                        break;
                    }
                }
            }
            if (shape1Normal2 == null || shape1.isEmpty() || path1.isEmpty() || path2.isEmpty() || shape2Normal2 != null && shape2.isEmpty()) {
                return;
            }
            if (path1.size() != path2.size() || shape2Normal2 != null && shape1.size() != shape2.size()) {
                return;
            }
            // Copy shape 1 to shape 2
            if (shape2Normal2 == null) {
                shape2.clear();
                shape2Normal2 = shape1Normal2;
                shape2.addAll(shape1);
            }
            shape1Normal = shape1Normal2;
            shape2Normal = shape2Normal2;

            // Insert zero length lines as line indicators
            {
                Set<Vertex> liVerts = new TreeSet<Vertex>();
                for (GData2 ind : lineIndicators) {
                    Vertex[] verts = lines.get(ind);
                    liVerts.add(verts[0]);
                    liVerts.add(verts[1]);
                }

                Set<Integer> indices = new HashSet<Integer>();

                // Shape 1
                {
                    int ss = shape1.size();
                    final ArrayList<GData2> shapeTmp = new ArrayList<GData2>(ss);
                    int ssm = ss - 1;
                    for (int i = 0; i < ss; i++) {
                        Vertex[] verts = lines.get(shape1.get(i));
                        if (i == 0) {
                            if (liVerts.contains(verts[0])) {
                                shapeTmp.add(new GData2(verts[0], verts[0], View.DUMMY_REFERENCE, new GColour()));
                                indices.add(i);
                            }
                            shapeTmp.add(shape1.get(i));
                        } else if (i == ssm) {
                            shapeTmp.add(shape1.get(i));
                            if (liVerts.contains(verts[1])) {
                                shapeTmp.add(new GData2(verts[1], verts[1], View.DUMMY_REFERENCE, new GColour()));
                                indices.add(i);
                            }
                        } else {
                            Vertex[] verts2 = lines.get(shape1.get(i - 1));
                            if (verts2[1].equals(verts[0]) && liVerts.contains(verts[0])) {
                                shapeTmp.add(new GData2(verts[0], verts[0], View.DUMMY_REFERENCE, new GColour()));
                                indices.add(i);
                            }
                            shapeTmp.add(shape1.get(i));
                        }
                    }
                    shape1.clear();
                    shape1.addAll(shapeTmp);
                }

                // Shape 2
                {
                    int ss = shape2.size();
                    final ArrayList<GData2> shapeTmp = new ArrayList<GData2>(ss);
                    int ssm = ss - 1;
                    for (int i = 0; i < ss; i++) {
                        Vertex[] verts = lines.get(shape2.get(i));
                        if (i == 0) {
                            if (indices.contains(i)) {
                                shapeTmp.add(new GData2(verts[0], verts[0], View.DUMMY_REFERENCE, new GColour()));
                            }
                            shapeTmp.add(shape2.get(i));
                        } else if (i == ssm) {
                            shapeTmp.add(shape2.get(i));
                            if (indices.contains(i)) {
                                shapeTmp.add(new GData2(verts[1], verts[1], View.DUMMY_REFERENCE, new GColour()));
                            }
                        } else {
                            if (indices.contains(i)) {
                                shapeTmp.add(new GData2(verts[0], verts[0], View.DUMMY_REFERENCE, new GColour()));
                            }
                            shapeTmp.add(shape2.get(i));
                        }
                    }
                    shape2.clear();
                    shape2.addAll(shapeTmp);
                }
            }

            shape1.add(0, shape1Normal);
            shape2.add(0, shape2Normal);
        }

        // Clear selection
        clearSelection();

        try {
            new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress() {
                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    try {
                        monitor.beginTask("Running PathTruder...", IProgressMonitor.UNKNOWN); //$NON-NLS-1$ I18N

                        final Thread[] threads = new Thread[1];
                        threads[0] = new Thread(new Runnable() {
                            @Override
                            public void run() {

                                if (monitor.isCanceled()) {
                                    return;
                                }

                                final GColour lineColour = DatParser.validateColour(24, .5f, .5f, .5f, 1f).clone();
                                final GColour bodyColour = DatParser.validateColour(16, .5f, .5f, .5f, 1f).clone();

                                double VERTMERGE = 0.001;
                                double PI = 3.14159265358979323846;

                                int MAX_LINE = 1000;
                                double SMALL = 0.1;
                                double SMALLANGLE = .95;

                                double[][][] Path1 = new double[5 * MAX_LINE][2][3];
                                double[][][] Path2 = new double[5 * MAX_LINE][2][3];
                                double[][][] Path1a = new double[MAX_LINE][2][3];
                                double[][][] Path2a = new double[MAX_LINE][2][3];
                                /** [lineIndex][pointIndex][coordinateIndex] */
                                double[][][] Shape1 = new double[MAX_LINE][2][3];
                                /** [lineIndex][pointIndex][coordinateIndex] */
                                double[][][] Shape2 = new double[MAX_LINE][2][3];
                                double[][][] CurShape = new double[MAX_LINE][2][3];
                                double[][][] NxtShape = new double[MAX_LINE][2][3];
                                double[] Shape1Vect = new double[3], Shape2Vect = new double[3];

                                double[] temp1 = new double[3], temp2 = new double[3], temp3 = new double[3];
                                double[] XVect = new double[3], YVect = new double[3], ZVect = new double[3];

                                double Angle, ca, sa;
                                double ratio;
                                double[][][] SortBuf = new double[MAX_LINE][2][3];
                                int[][][] next = new int[MAX_LINE][2][2];

                                int Path1Len = 0;
                                int Path2Len = 0;
                                int Shape1Len = 0;
                                int Shape2Len = 0;

                                boolean circular = false;
                                double maxlength = ps.getMaxPathSegmentLength().doubleValue();
                                double dmax, d = 0.0;
                                double len;
                                int InLineIdx;
                                int NumPath;
                                boolean invert = ps.isInverted();

                                int transitions = ps.getTransitionCount();
                                double slope = ps.getTransitionCurveControl().doubleValue();
                                double position = ps.getTransitionCurveCenter().doubleValue();
                                double crease = ps.getPathAngleForLine().doubleValue();
                                boolean compensate = ps.isCompensation();
                                boolean endings = path1endSegments.size() == 2 && path2endSegments.size() == 2;
                                double rotation = ps.getRotation().doubleValue();

                                {
                                    // printf("Read path file 1\n"); //$NON-NLS-1$
                                    if (endings) {
                                        path1.add(0, path1endSegments.get(0));
                                        path1.add(path1endSegments.get(1));
                                    }
                                    for (GData2 p : path1) {
                                        SortBuf[Path1Len][0][0] = p.X1.doubleValue();
                                        SortBuf[Path1Len][0][1] = p.Y1.doubleValue();
                                        SortBuf[Path1Len][0][2] = p.Z1.doubleValue();
                                        SortBuf[Path1Len][1][0] = p.X2.doubleValue();
                                        SortBuf[Path1Len][1][1] = p.Y2.doubleValue();
                                        SortBuf[Path1Len][1][2] = p.Z2.doubleValue();
                                        next[Path1Len][0][0] = next[Path1Len][1][0] = -1;
                                        Path1Len++;
                                    }
                                    // printf("Sort path file 1\n"); //$NON-NLS-1$
                                    circular = true;
                                    for (int i = 0; i < Path1Len; i++) {
                                        for (int j = 0; j < 2; j++) {
                                            if (next[i][j][0] != -1)
                                                break;
                                            dmax = 10000000;
                                            for (int k = 0; k < Path1Len; k++) {
                                                if (k != i) {
                                                    for (int l = 0; l < 2; l++) {
                                                        d = MANHATTAN(SortBuf[i][j], SortBuf[k][l]);
                                                        if (d < dmax) {
                                                            dmax = d;
                                                            next[i][j][0] = k;
                                                            next[i][j][1] = l;
                                                        }
                                                        if (d == 0)
                                                            break;
                                                    }
                                                    if (d == 0)
                                                        break;
                                                }
                                            }
                                            if (dmax > SMALL) {
                                                next[i][j][0] = -1;
                                                circular = false;
                                            }
                                        }
                                    }
                                    if (circular) {
                                        next[next[0][0][0]][next[0][0][1]][0] = -1;
                                        next[0][0][0] = -1;
                                    }
                                    InLineIdx = 0;
                                    NumPath = 0;
                                    for (int i = 0; i < Path1Len; i++) {
                                        for (int j = 0; j < 2; j++) {
                                            int a, b, c, d2;
                                            if (next[i][j][0] == -1) {
                                                NumPath++;
                                                a = i;
                                                b = j;
                                                do {
                                                    SET(Path1a[InLineIdx][0], SortBuf[a][b]);
                                                    SET(Path1a[InLineIdx][1], SortBuf[a][1 - b]);
                                                    InLineIdx++;

                                                    d2 = next[a][1 - b][1];
                                                    c = next[a][1 - b][0];
                                                    next[a][1 - b][0] = -2;
                                                    next[a][b][0] = -2;
                                                    b = d2;
                                                    a = c;
                                                } while (a != -1);
                                            }
                                        }
                                    }

                                    Path1Len = InLineIdx;

                                    if (NumPath > 1) {
                                        //   printf("%d distinct paths found in Path file 1. Unexpected results may happen!\n" + NumPath); //$NON-NLS-1$
                                    }
                                }
                                {
                                    // printf("Read path file 2\n"); //$NON-NLS-1$
                                    if (endings) {
                                        path2.add(0, path2endSegments.get(0));
                                        path2.add(path2endSegments.get(1));
                                    }
                                    for (GData2 p : path2) {
                                        SortBuf[Path2Len][0][0] = p.X1.doubleValue();
                                        SortBuf[Path2Len][0][1] = p.Y1.doubleValue();
                                        SortBuf[Path2Len][0][2] = p.Z1.doubleValue();
                                        SortBuf[Path2Len][1][0] = p.X2.doubleValue();
                                        SortBuf[Path2Len][1][1] = p.Y2.doubleValue();
                                        SortBuf[Path2Len][1][2] = p.Z2.doubleValue();
                                        next[Path2Len][0][0] = next[Path2Len][1][0] = -1;
                                        Path2Len++;
                                    }
                                    // printf("Sort path file 2\n"); //$NON-NLS-1$
                                    circular = true;
                                    for (int i = 0; i < Path2Len; i++) {
                                        for (int j = 0; j < 2; j++) {
                                            if (next[i][j][0] != -1)
                                                break;
                                            dmax = 10000000;
                                            for (int k = 0; k < Path2Len; k++) {
                                                if (k != i) {
                                                    for (int l = 0; l < 2; l++) {
                                                        d = MANHATTAN(SortBuf[i][j], SortBuf[k][l]);
                                                        if (d < dmax) {
                                                            dmax = d;
                                                            next[i][j][0] = k;
                                                            next[i][j][1] = l;
                                                        }
                                                        if (d == 0)
                                                            break;
                                                    }
                                                    if (d == 0)
                                                        break;
                                                }
                                            }
                                            if (dmax > SMALL) {
                                                next[i][j][0] = -1;
                                                circular = false;
                                            }
                                        }
                                    }
                                    if (circular) {
                                        next[next[0][0][0]][next[0][0][1]][0] = -1;
                                        next[0][0][0] = -1;
                                    }
                                    InLineIdx = 0;
                                    NumPath = 0;
                                    for (int i = 0; i < Path2Len; i++) {
                                        for (int j = 0; j < 2; j++) {
                                            int a, b, c, d2;
                                            if (next[i][j][0] == -1) {
                                                NumPath++;
                                                a = i;
                                                b = j;
                                                do {
                                                    SET(Path2a[InLineIdx][0], SortBuf[a][b]);
                                                    SET(Path2a[InLineIdx][1], SortBuf[a][1 - b]);
                                                    InLineIdx++;

                                                    d2 = next[a][1 - b][1];
                                                    c = next[a][1 - b][0];
                                                    next[a][1 - b][0] = -2;
                                                    next[a][b][0] = -2;
                                                    b = d2;
                                                    a = c;
                                                } while (a != -1);
                                            }
                                        }
                                    }

                                    Path2Len = InLineIdx;

                                    // if (NumPath > 1)
                                    //    printf("%d distinct paths found in Path file 2. Unexpected results may happen!\n" + NumPath); //$NON-NLS-1$
                                }
                                // printf("Read shape file 1\n"); //$NON-NLS-1$
                                for (GData2 p : shape1) {
                                    Shape1[Shape1Len][0][0] = p.X1.doubleValue();
                                    Shape1[Shape1Len][0][1] = p.Y1.doubleValue();
                                    Shape1[Shape1Len][0][2] = p.Z1.doubleValue();
                                    Shape1[Shape1Len][1][0] = p.X2.doubleValue();
                                    Shape1[Shape1Len][1][1] = p.Y2.doubleValue();
                                    Shape1[Shape1Len][1][2] = p.Z2.doubleValue();
                                    Shape1Len++;
                                }
                                // printf("Read shape file 2\n"); //$NON-NLS-1$
                                for (GData2 p : shape2) {
                                    Shape2[Shape2Len][0][0] = p.X1.doubleValue();
                                    Shape2[Shape2Len][0][1] = p.Y1.doubleValue();
                                    Shape2[Shape2Len][0][2] = p.Z1.doubleValue();
                                    Shape2[Shape2Len][1][0] = p.X2.doubleValue();
                                    Shape2[Shape2Len][1][1] = p.Y2.doubleValue();
                                    Shape2[Shape2Len][1][2] = p.Z2.doubleValue();
                                    Shape2Len++;
                                }

                                if (Path1Len != Path2Len) {
                                    // printf("The two path files do not have the same number of elements!\n"); //$NON-NLS-1$
                                    return;
                                }

                                if (endings && Path1Len < 3 && !circular) {
                                    // printf("Path files must have at least 3 elements to use -e option!\n"); //$NON-NLS-1$
                                    return;
                                }

                                if (Shape1Len != Shape2Len) {
                                    // printf("The two shape files do not have the same number of elements!\n"); //$NON-NLS-1$
                                    // printf("Press <Enter> to quit"); //$NON-NLS-1$
                                    return;
                                }

                                // Split long lines
                                InLineIdx = 0;
                                for (int i = 0; i < Path1Len; i++) {
                                    double[] p1 = new double[3], p2 = new double[3], q1 = new double[3], q2 = new double[3], delta1 = new double[3], delta2 = new double[3], temp = new double[3];
                                    int nsplit1, nsplit2;

                                    SET(p1, Path1a[i][0]);
                                    SET(p2, Path1a[i][1]);

                                    SET(q1, Path2a[i][0]);
                                    SET(q2, Path2a[i][1]);

                                    nsplit1 = (int) (DIST(p1, p2) / maxlength) + 1;
                                    nsplit2 = (int) (DIST(q1, q2) / maxlength) + 1;

                                    // don't split endings segments
                                    if (endings) {
                                        if (i == 0 || i == Path1Len - 1)
                                            nsplit1 = nsplit2 = 1;
                                    }

                                    nsplit1 = nsplit1 > nsplit2 ? nsplit1 : nsplit2;

                                    SUB(delta1, p2, p1);
                                    MULT(delta1, delta1, 1.0 / nsplit1);
                                    SUB(delta2, q2, q1);
                                    MULT(delta2, delta2, 1.0 / nsplit1);
                                    for (int k = 0; k < nsplit1; k++) {
                                        MULT(temp, delta1, k);
                                        ADD(Path1[InLineIdx][0], p1, temp);
                                        ADD(Path1[InLineIdx][1], Path1[InLineIdx][0], delta1);
                                        MULT(temp, delta2, k);
                                        ADD(Path2[InLineIdx][0], q1, temp);
                                        ADD(Path2[InLineIdx][1], Path2[InLineIdx][0], delta2);

                                        InLineIdx++;
                                    }
                                }

                                Path1Len = Path2Len = InLineIdx;
                                SET(Path1[Path1Len][0], Path1[Path1Len - 1][1]);
                                SET(Path1[Path1Len][1], Path1[Path1Len - 1][0]);

                                SET(Path2[Path2Len][0], Path2[Path2Len - 1][1]);
                                SET(Path2[Path2Len][1], Path2[Path2Len - 1][0]);

                                len = DIST(Shape1[0][0], Shape1[0][1]);

                                for (int i = 1; i < Shape1Len; i++) {
                                    SUB(Shape1[i][0], Shape1[i][0], Shape1[0][0]);
                                    MULT(Shape1[i][0], Shape1[i][0], 1 / len);
                                    SUB(Shape1[i][1], Shape1[i][1], Shape1[0][0]);
                                    MULT(Shape1[i][1], Shape1[i][1], 1 / len);
                                }
                                SUB(Shape1Vect, Shape1[0][1], Shape1[0][0]);

                                Angle = Math.atan2(-Shape1Vect[0], -Shape1Vect[1]);

                                sa = Math.sin(Angle);
                                ca = Math.cos(Angle);

                                for (int i = 1; i < Shape1Len; i++) {
                                    Shape1[i - 1][0][0] = Shape1[i][0][0] * ca - Shape1[i][0][1] * sa;
                                    Shape1[i - 1][0][1] = Shape1[i][0][0] * sa + Shape1[i][0][1] * ca;
                                    Shape1[i - 1][1][0] = Shape1[i][1][0] * ca - Shape1[i][1][1] * sa;
                                    Shape1[i - 1][1][1] = Shape1[i][1][0] * sa + Shape1[i][1][1] * ca;
                                    Shape1[i - 1][0][2] = Shape1[i][0][2];
                                    Shape1[i - 1][1][2] = Shape1[i][1][2];
                                    if (invert) {
                                        Shape1[i - 1][0][0] = -Shape1[i - 1][0][0];
                                        Shape1[i - 1][1][0] = -Shape1[i - 1][1][0];
                                    }
                                }
                                Shape1Len--;

                                // Normalize shape 2

                                len = DIST(Shape2[0][0], Shape2[0][1]);

                                for (int i = 1; i < Shape2Len; i++) {
                                    SUB(Shape2[i][0], Shape2[i][0], Shape2[0][0]);
                                    MULT(Shape2[i][0], Shape2[i][0], 1 / len);
                                    SUB(Shape2[i][1], Shape2[i][1], Shape2[0][0]);
                                    MULT(Shape2[i][1], Shape2[i][1], 1 / len);
                                }
                                SUB(Shape2Vect, Shape2[0][1], Shape2[0][0]);

                                Angle = Math.atan2(-Shape2Vect[0], -Shape2Vect[1]);

                                sa = Math.sin(Angle);
                                ca = Math.cos(Angle);

                                for (int i = 1; i < Shape2Len; i++) {
                                    Shape2[i - 1][0][0] = Shape2[i][0][0] * ca - Shape2[i][0][1] * sa;
                                    Shape2[i - 1][0][1] = Shape2[i][0][0] * sa + Shape2[i][0][1] * ca;
                                    Shape2[i - 1][1][0] = Shape2[i][1][0] * ca - Shape2[i][1][1] * sa;
                                    Shape2[i - 1][1][1] = Shape2[i][1][0] * sa + Shape2[i][1][1] * ca;
                                    Shape2[i - 1][0][2] = Shape2[i][0][2];
                                    Shape2[i - 1][1][2] = Shape2[i][1][2];
                                    if (invert) {
                                        Shape2[i - 1][0][0] = -Shape2[i - 1][0][0];
                                        Shape2[i - 1][1][0] = -Shape2[i - 1][1][0];
                                    }

                                }
                                Shape2Len--;

                                // Extrusion
                                // Initialize current shape
                                if (circular)
                                    endings = false;

                                if (endings) {
                                    double Angle2 = PathLocalBasis(0, 1, XVect, YVect, ZVect, Path1, Path2);
                                    Angle = PathLocalBasis(0, 0, XVect, YVect, ZVect, Path1, Path2);
                                    if (Angle2 > 90) {
                                        MULT(XVect, XVect, -1);
                                        MULT(ZVect, ZVect, -1);
                                    }
                                } else {
                                    Angle = PathLocalBasis(circular ? Path1Len - 1 : 0, 0, XVect, YVect, ZVect, Path1, Path2);
                                }
                                // compensate sharp angles
                                if (compensate) {
                                    MULT(XVect, XVect, 1 / Math.cos(Angle * PI / 360));
                                }

                                // Calculate next transformed shape
                                for (int j = 0; j < Shape1Len; j++) {
                                    for (int k = 0; k < 2; k++) {
                                        MULT(NxtShape[j][k], XVect, Shape1[j][k][0]);
                                        MULT(temp1, YVect, Shape1[j][k][1]);
                                        ADD(NxtShape[j][k], NxtShape[j][k], temp1);
                                        MULT(temp1, ZVect, Shape1[j][k][2]);
                                        ADD(NxtShape[j][k], NxtShape[j][k], temp1);
                                        if (endings) {
                                            ADD(NxtShape[j][k], NxtShape[j][k], Path1[1][0]);
                                        } else {
                                            ADD(NxtShape[j][k], NxtShape[j][k], Path1[0][0]);
                                        }
                                    }

                                }
                                if (Angle > crease) {
                                    // sharp angle. Create line at junction
                                    for (int i = 0; i < Shape1Len; i++) {
                                        Vertex v1 = new Vertex(new BigDecimal(NxtShape[i][0][0]), new BigDecimal(NxtShape[i][0][1]), new BigDecimal(NxtShape[i][0][2]));
                                        Vertex v2 = new Vertex(new BigDecimal(NxtShape[i][1][0]), new BigDecimal(NxtShape[i][1][1]), new BigDecimal(NxtShape[i][1][2]));
                                        newLines.add(new GData2(lineColour.getColourNumber(), lineColour.getR(), lineColour.getG(), lineColour.getB(), lineColour.getA(), v1, v2, View.DUMMY_REFERENCE, linkedDatFile));
                                    }
                                }

                                int start, end;
                                start = 0;
                                end = Path1Len;
                                if (endings) {
                                    start++;
                                    end--;
                                }
                                for (int i = start; i < end; i++) {

                                    // Transfer old next shape to current.
                                    for (int j = 0; j < Shape1Len; j++) {
                                        SET(CurShape[j][0], NxtShape[j][0]);
                                        SET(CurShape[j][1], NxtShape[j][1]);
                                    }

                                    if (i == end - 1) {
                                        if (circular) {
                                            Angle = PathLocalBasis(i, 0, XVect, YVect, ZVect, Path1, Path2);
                                        } else {
                                            if (endings) {
                                                double Angle2 = PathLocalBasis(i, i + 1, XVect, YVect, ZVect, Path1, Path2);
                                                Angle = PathLocalBasis(i + 2, i + 2, XVect, YVect, ZVect, Path1, Path2);
                                                if (Angle2 < 90) {
                                                    // in that case the local
                                                    // base is mirrorred...
                                                    SUB(XVect, nullv, XVect);
                                                    SUB(ZVect, nullv, ZVect);
                                                }
                                            } else {
                                                Angle = PathLocalBasis(i + 1, i + 1, XVect, YVect, ZVect, Path1, Path2);
                                                // in that case the local base
                                                // is mirrorred...
                                                SUB(XVect, nullv, XVect);
                                                SUB(ZVect, nullv, ZVect);
                                            }
                                        }
                                    } else {
                                        Angle = PathLocalBasis(i, i + 1, XVect, YVect, ZVect, Path1, Path2);
                                    }

                                    // compensate sharp angles
                                    if (compensate) {
                                        MULT(XVect, XVect, 1 / Math.cos(Angle * PI / 360));
                                    }

                                    {
                                        double x;
                                        double j = (i + 1.0 - start) * transitions % (2 * (end - start));
                                        x = 1.0 * j / (end - start);
                                        if (x > 1.0)
                                            x = 2.0 - x;
                                        ratio = sigmoid(x, slope, position);
                                    }

                                    double rotangle = rotation * PI / 180.0 * ((i + 1.0) / Path1Len);

                                    sa = Math.sin(rotangle);
                                    ca = Math.cos(rotangle);

                                    for (int j = 0; j < Shape1Len; j++) {
                                        for (int k = 0; k < 2; k++) {
                                            temp1[0] = Shape1[j][k][0] * ca - Shape1[j][k][1] * sa;
                                            temp1[1] = Shape1[j][k][0] * sa + Shape1[j][k][1] * ca;
                                            temp2[0] = Shape2[j][k][0] * ca - Shape2[j][k][1] * sa;
                                            temp2[1] = Shape2[j][k][0] * sa + Shape2[j][k][1] * ca;

                                            MULT(NxtShape[j][k], XVect, temp1[0] * (1.0 - ratio) + temp2[0] * ratio);
                                            MULT(temp3, YVect, temp1[1] * (1.0 - ratio) + temp2[1] * ratio);
                                            ADD(NxtShape[j][k], NxtShape[j][k], temp3);
                                            MULT(temp3, ZVect, Shape1[j][k][2] * (1.0 - ratio) + Shape2[j][k][2] * ratio);
                                            ADD(NxtShape[j][k], NxtShape[j][k], temp3);
                                            ADD(NxtShape[j][k], NxtShape[j][k], Path1[i + 1][0]);
                                        }
                                    }
                                    if (Angle > crease) {
                                        // sharp angle. Create line at junction
                                        for (int j = 0; j < Shape1Len; j++) {
                                            Vertex v1 = new Vertex(new BigDecimal(NxtShape[j][0][0]), new BigDecimal(NxtShape[j][0][1]), new BigDecimal(NxtShape[j][0][2]));
                                            Vertex v2 = new Vertex(new BigDecimal(NxtShape[j][1][0]), new BigDecimal(NxtShape[j][1][1]), new BigDecimal(NxtShape[j][1][2]));
                                            newLines.add(new GData2(lineColour.getColourNumber(), lineColour.getR(), lineColour.getG(), lineColour.getB(), lineColour.getA(), v1, v2, View.DUMMY_REFERENCE, linkedDatFile));
                                        }
                                    }
                                    // Generate tri/quad sheet
                                    for (int j = 0; j < Shape1Len; j++) {
                                        if (!lineIndicators.isEmpty()) {
                                            if (DIST(Shape1[j][0], Shape1[j][1]) < EPSILON && DIST(Shape2[j][0], Shape2[j][1]) < EPSILON) {
                                                // Null lenth segment in shape file
                                                // -> generate line at that place
                                                Vertex v1 = new Vertex(new BigDecimal(CurShape[j][0][0]), new BigDecimal(CurShape[j][0][1]), new BigDecimal(CurShape[j][0][2]));
                                                Vertex v2 = new Vertex(new BigDecimal(NxtShape[j][0][0]), new BigDecimal(NxtShape[j][0][1]), new BigDecimal(NxtShape[j][0][2]));
                                                newLines.add(new GData2(lineColour.getColourNumber(), lineColour.getR(), lineColour.getG(), lineColour.getB(), lineColour.getA(), v1, v2, View.DUMMY_REFERENCE, linkedDatFile));
                                            }
                                        }
                                        if (DIST(CurShape[j][0], CurShape[j][1]) < VERTMERGE) {
                                            if (DIST(NxtShape[j][0], NxtShape[j][1]) < VERTMERGE || DIST(CurShape[j][0], NxtShape[j][0]) < VERTMERGE || DIST(NxtShape[j][1], CurShape[j][1]) < VERTMERGE) {
                                                // Degenerated. Nothing to
                                                // output
                                                continue;
                                            } else {
                                                Vertex v1 = new Vertex(new BigDecimal(CurShape[j][0][0]), new BigDecimal(CurShape[j][0][1]), new BigDecimal(CurShape[j][0][2]));
                                                Vertex v2 = new Vertex(new BigDecimal(NxtShape[j][1][0]), new BigDecimal(NxtShape[j][1][1]), new BigDecimal(NxtShape[j][1][2]));
                                                Vertex v3 = new Vertex(new BigDecimal(NxtShape[j][0][0]), new BigDecimal(NxtShape[j][0][1]), new BigDecimal(NxtShape[j][0][2]));
                                                newTriangles.add(new GData3(bodyColour.getColourNumber(), bodyColour.getR(), bodyColour.getG(), bodyColour.getB(), bodyColour.getA(), v1, v2, v3, View.DUMMY_REFERENCE, linkedDatFile));
                                                continue;
                                            }
                                        }
                                        if (DIST(NxtShape[j][0], NxtShape[j][1]) < VERTMERGE) {
                                            if (DIST(CurShape[j][0], NxtShape[j][0]) < VERTMERGE || DIST(NxtShape[j][1], CurShape[j][1]) < VERTMERGE) {
                                                // Degenerated. Nothing to
                                                // output
                                                continue;
                                            } else {
                                                Vertex v1 = new Vertex(new BigDecimal(CurShape[j][0][0]), new BigDecimal(CurShape[j][0][1]), new BigDecimal(CurShape[j][0][2]));
                                                Vertex v2 = new Vertex(new BigDecimal(CurShape[j][1][0]), new BigDecimal(CurShape[j][1][1]), new BigDecimal(CurShape[j][1][2]));
                                                Vertex v3 = new Vertex(new BigDecimal(NxtShape[j][0][0]), new BigDecimal(NxtShape[j][0][1]), new BigDecimal(NxtShape[j][0][2]));
                                                newTriangles.add(new GData3(bodyColour.getColourNumber(), bodyColour.getR(), bodyColour.getG(), bodyColour.getB(), bodyColour.getA(), v1, v2, v3, View.DUMMY_REFERENCE, linkedDatFile));
                                                continue;
                                            }
                                        }
                                        if (DIST(CurShape[j][0], NxtShape[j][0]) < VERTMERGE) {
                                            if (DIST(NxtShape[j][1], CurShape[j][1]) < VERTMERGE) {
                                                // Degenerated. Nothing to
                                                // output
                                                continue;
                                            } else {
                                                Vertex v1 = new Vertex(new BigDecimal(CurShape[j][0][0]), new BigDecimal(CurShape[j][0][1]), new BigDecimal(CurShape[j][0][2]));
                                                Vertex v2 = new Vertex(new BigDecimal(CurShape[j][1][0]), new BigDecimal(CurShape[j][1][1]), new BigDecimal(CurShape[j][1][2]));
                                                Vertex v3 = new Vertex(new BigDecimal(NxtShape[j][1][0]), new BigDecimal(NxtShape[j][1][1]), new BigDecimal(NxtShape[j][1][2]));
                                                newTriangles.add(new GData3(bodyColour.getColourNumber(), bodyColour.getR(), bodyColour.getG(), bodyColour.getB(), bodyColour.getA(), v1, v2, v3, View.DUMMY_REFERENCE, linkedDatFile));
                                                continue;
                                            }
                                        }
                                        if (DIST(NxtShape[j][1], CurShape[j][1]) < VERTMERGE) {
                                            Vertex v1 = new Vertex(new BigDecimal(CurShape[j][0][0]), new BigDecimal(CurShape[j][0][1]), new BigDecimal(CurShape[j][0][2]));
                                            Vertex v2 = new Vertex(new BigDecimal(CurShape[j][1][0]), new BigDecimal(CurShape[j][1][1]), new BigDecimal(CurShape[j][1][2]));
                                            Vertex v3 = new Vertex(new BigDecimal(NxtShape[j][0][0]), new BigDecimal(NxtShape[j][0][1]), new BigDecimal(NxtShape[j][0][2]));
                                            newTriangles.add(new GData3(bodyColour.getColourNumber(), bodyColour.getR(), bodyColour.getG(), bodyColour.getB(), bodyColour.getA(), v1, v2, v3, View.DUMMY_REFERENCE, linkedDatFile));
                                            continue;
                                        }
                                        if (Tri_Angle(CurShape[j][0], NxtShape[j][0], NxtShape[j][1], CurShape[j][1]) < SMALLANGLE) {
                                            Vertex v1 = new Vertex(new BigDecimal(CurShape[j][0][0]), new BigDecimal(CurShape[j][0][1]), new BigDecimal(CurShape[j][0][2]));
                                            Vertex v2 = new Vertex(new BigDecimal(CurShape[j][1][0]), new BigDecimal(CurShape[j][1][1]), new BigDecimal(CurShape[j][1][2]));
                                            Vertex v3 = new Vertex(new BigDecimal(NxtShape[j][1][0]), new BigDecimal(NxtShape[j][1][1]), new BigDecimal(NxtShape[j][1][2]));
                                            Vertex v4 = new Vertex(new BigDecimal(NxtShape[j][0][0]), new BigDecimal(NxtShape[j][0][1]), new BigDecimal(NxtShape[j][0][2]));
                                            newQuads.add(new GData4(bodyColour.getColourNumber(), bodyColour.getR(), bodyColour.getG(), bodyColour.getB(), bodyColour.getA(), v1, v2, v3, v4, View.DUMMY_REFERENCE, linkedDatFile));
                                        } else {
                                            {
                                                Vertex v1 = new Vertex(new BigDecimal(CurShape[j][0][0]), new BigDecimal(CurShape[j][0][1]), new BigDecimal(CurShape[j][0][2]));
                                                Vertex v2 = new Vertex(new BigDecimal(NxtShape[j][1][0]), new BigDecimal(NxtShape[j][1][1]), new BigDecimal(NxtShape[j][1][2]));
                                                Vertex v3 = new Vertex(new BigDecimal(NxtShape[j][0][0]), new BigDecimal(NxtShape[j][0][1]), new BigDecimal(NxtShape[j][0][2]));
                                                newTriangles.add(new GData3(bodyColour.getColourNumber(), bodyColour.getR(), bodyColour.getG(), bodyColour.getB(), bodyColour.getA(), v1, v2, v3, View.DUMMY_REFERENCE, linkedDatFile));
                                            }
                                            {
                                                Vertex v1 = new Vertex(new BigDecimal(CurShape[j][0][0]), new BigDecimal(CurShape[j][0][1]), new BigDecimal(CurShape[j][0][2]));
                                                Vertex v2 = new Vertex(new BigDecimal(CurShape[j][1][0]), new BigDecimal(CurShape[j][1][1]), new BigDecimal(CurShape[j][1][2]));
                                                Vertex v3 = new Vertex(new BigDecimal(NxtShape[j][1][0]), new BigDecimal(NxtShape[j][1][1]), new BigDecimal(NxtShape[j][1][2]));
                                                newTriangles.add(new GData3(bodyColour.getColourNumber(), bodyColour.getR(), bodyColour.getG(), bodyColour.getB(), bodyColour.getA(), v1, v2, v3, View.DUMMY_REFERENCE, linkedDatFile));
                                            }
                                        }
                                    }
                                }
                            }
                        });
                        threads[0].start();
                        boolean isRunning = true;
                        while (isRunning) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                            }
                            isRunning = false;
                            if (threads[0].isAlive())
                                isRunning = true;
                        }
                        if (monitor.isCanceled()) {
                            selectedLines.addAll(originalSelection);
                            selectedData.addAll(originalSelection);
                            originalSelection.clear();
                            return;
                        }
                    } finally {
                        monitor.done();
                    }
                }
            });
        } catch (InvocationTargetException consumed) {
        } catch (InterruptedException consumed) {
        }

        if (originalSelection.isEmpty()) {
            return;
        }

        NLogger.debug(getClass(), "Check for identical vertices and collinearity."); //$NON-NLS-1$
        final Set<GData2> linesToDelete2 = new HashSet<GData2>();
        final Set<GData3> trisToDelete2 = new HashSet<GData3>();
        final Set<GData4> quadsToDelete2 = new HashSet<GData4>();
        {
            for (GData2 g2 : newLines) {
                Vertex[] verts = lines.get(g2);
                Set<Vertex> verts2 = new TreeSet<Vertex>();
                for (Vertex vert : verts) {
                    verts2.add(vert);
                }
                if (verts2.size() < 2) {
                    linesToDelete2.add(g2);
                }
            }
            for (GData3 g3 : newTriangles) {
                Vertex[] verts = triangles.get(g3);
                Set<Vertex> verts2 = new TreeSet<Vertex>();
                for (Vertex vert : verts) {
                    verts2.add(vert);
                }
                if (verts2.size() < 3 || g3.isCollinear()) {
                    trisToDelete2.add(g3);
                }
            }
            for (GData4 g4 : newQuads) {
                Vertex[] verts = quads.get(g4);
                Set<Vertex> verts2 = new TreeSet<Vertex>();
                for (Vertex vert : verts) {
                    verts2.add(vert);
                }
                if (verts2.size() < 4 || g4.isCollinear()) {
                    quadsToDelete2.add(g4);
                }
            }
        }

        // Append the new data
        for (GData2 line : newLines) {
            linkedDatFile.addToTail(line);
        }
        for (GData3 tri : newTriangles) {
            linkedDatFile.addToTail(tri);
        }
        for (GData4 quad : newQuads) {
            linkedDatFile.addToTail(quad);
        }

        NLogger.debug(getClass(), "Delete new, but invalid objects."); //$NON-NLS-1$

        newLines.removeAll(linesToDelete2);
        newTriangles.removeAll(trisToDelete2);
        newQuads.removeAll(quadsToDelete2);
        selectedLines.addAll(linesToDelete2);
        selectedTriangles.addAll(trisToDelete2);
        selectedQuads.addAll(quadsToDelete2);
        selectedData.addAll(selectedLines);
        selectedData.addAll(selectedTriangles);
        selectedData.addAll(selectedQuads);
        delete(false, false);

        // Round to 6 decimal places

        selectedLines.addAll(newLines);
        selectedTriangles.addAll(newTriangles);
        selectedQuads.addAll(newQuads);
        selectedData.addAll(selectedLines);
        selectedData.addAll(selectedTriangles);
        selectedData.addAll(selectedQuads);

        NLogger.debug(getClass(), "Round."); //$NON-NLS-1$
        roundSelection(6, 10, true, false);

        setModified(true, true);

        NLogger.debug(getClass(), "Done."); //$NON-NLS-1$

        validateState();

    }

    // Calculate scaled sigmoid function between 0 and 1.
    // 1/(1+exp(-b*(x-m))) Scaled so that sigmoid(0)=0, sigmoid(1)=1
    // b is growth rate, m is max growth rate point
    // if b=1. returns a true x (linear relationship)
    private double sigmoid(double x,double b,double m)
    {
        double s0, s1, y;
        if(b == 1.0) return x;
        s0 = 1.0 / (1.0 + Math.exp(b * m));
        s1 = 1.0 / (1.0 + Math.exp(-b * (1.0 - m)));
        y = 1.0 / (1.0 + Math.exp(-b * (x - m)));
        y = (y - s0) / (s1 - s0);
        return y;
    }

    private final double EPSILON = 0.000001;
    /* Null vector */
    private double[] nullv = new double[]{0.0,0.0,0.0};

    private void CROSS(double[] dest, double[] left, double[] right) {
        dest[0]=left[1]*right[2]-left[2]*right[1];
        dest[1]=left[2]*right[0]-left[0]*right[2];
        dest[2]=left[0]*right[1]-left[1]*right[0];
    }

    private double DOT(double[] v1, double[] v2) {
        return v1[0]*v2[0]+v1[1]*v2[1]+v1[2]*v2[2];
    }

    private void SUB(double[] dest, double[] left, double[] right) {
        dest[0]=left[0]-right[0]; dest[1]=left[1]-right[1]; dest[2]=left[2]-right[2];
    }

    private void ADD(double[] dest, double[] left, double[] right) {
        dest[0]=left[0]+right[0]; dest[1]=left[1]+right[1]; dest[2]=left[2]+right[2];
    }

    private void MULT(double[] dest, double[] v, double factor) {
        dest[0]=factor*v[0]; dest[1]=factor*v[1]; dest[2]=factor*v[2];
    }

    private void SET(double[] dest, double[] src) {
        dest[0]=src[0]; dest[1]=src[1]; dest[2]=src[2];
    }

    private double MANHATTAN(double[] v1, double[] v2) {
        return Math.abs(v1[0]-v2[0]) + Math.abs(v1[1]-v2[1]) + Math.abs(v1[2]-v2[2]);
    }

    private double DIST(double[] v1, double[] v2) {
        return Math.sqrt((v1[0]-v2[0])*(v1[0]-v2[0]) + (v1[1]-v2[1])*(v1[1]-v2[1]) + (v1[2]-v2[2])*(v1[2]-v2[2]));
    }

    // Calculate local basis, based on the direction of the i-th vector between both paths,
    // and the average of the planes defined by the paths before and after this vector
    // Returns angle between these planes.
    private double PathLocalBasis (int n, int i,  double[] xv,double[] yv,double[] zv, double[][][] path1, double[][][] path2)
    {
        double a, scale;
        double[] temp1 = new double[3], temp2 = new double[3], temp3 = new double[3], temp4 = new double[3];

        // Calculate local coordinate basis
        scale = DIST(path2[i][0], path1[i][0]);

        if(scale < EPSILON)
        {
            // size is 0... any non-degenerated base will do!
            SET (yv, nullv);
            yv[0]=1;
        }
        else
        {
            SUB(yv, path1[i][0], path2[i][0]);
        }

        // Average Path Normal
        SUB(temp1, path1[i][1], path1[i][0]);
        SUB(temp2, path2[i][1], path1[i][0]);
        CROSS(xv, temp2, temp1);
        a=DIST(xv, nullv);
        if (a > EPSILON) {
            MULT(xv, xv, 1.0/a);
        } else {
            SET(xv, nullv);
        }
        SUB(temp1, path2[i][1], path2[i][0]);
        SUB(temp2, path1[i][0], path2[i][0]);
        CROSS(temp3, temp1, temp2);
        a=DIST(temp3, nullv);
        if (a > EPSILON) {
            MULT(temp3, temp3, 1.0/a);
        } else {
            SET(temp3, nullv);
        }
        ADD(xv, xv, temp3);
        a=DIST(xv, nullv);
        if (a > EPSILON) {
            MULT(xv, xv, 1/a);
        } else {
            SET(xv, nullv);
        }

        SUB(temp1, path1[n][1], path1[n][0]);
        SUB(temp2, path2[n][1], path1[n][0]);
        CROSS(temp4, temp2, temp1);
        a=DIST(temp4, nullv);
        if(a > EPSILON) {
            MULT(temp4, temp4, 1.0/a);
        } else {
            SET(temp4, nullv);
        }
        SUB(temp1, path2[n][1], path2[n][0]);
        SUB(temp2, path1[n][0], path2[n][0]);
        CROSS(temp3, temp1, temp2);
        a=DIST(temp3, nullv);
        if(a > EPSILON) {
            MULT(temp3, temp3, 1.0/a);
        } else {
            SET(temp3, nullv);
        }
        ADD(temp4, temp4, temp3);
        a=DIST(temp4, nullv);
        if(a > EPSILON) {
            MULT(temp4, temp4, 1.0/a);
        } else {
            SET(temp4, nullv);
        }

        // Average previous and current path normals
        ADD(xv, xv, temp4);
        a=DIST(xv, nullv);
        if(a > EPSILON) {
            MULT(xv, xv, 1/a);
        } else {
            SET(xv, nullv);
        }

        // calculate angle
        a = 360.0 / Math.PI * Math.acos(DOT(xv,temp4));

        CROSS(zv,xv,yv);
        MULT(xv, xv, scale);
        if(scale < EPSILON)
        {
            SET(yv, nullv);
            SET(zv, nullv);
        }
        return a;
    }

    // Tri_Angle computes the angle (in degrees) between the planes of a quad.
    // They are assumed to be non-degenerated
    private double Tri_Angle(double[] U0,double[] U1,double[] U2, double[] U3)
    {
        double[] Unorm = new double[3], Vnorm = new double[3];
        double[] Temp = new double[3];
        double[] U10 = new double[3], U20 = new double[3];
        double[] V10 = new double[3], V20 = new double[3];
        double len;
        SUB(U10, U1, U0);
        SUB(U20, U2, U0);
        SUB(V10, U2, U0);
        SUB(V20, U3, U0);
        CROSS(Temp, U10, U20);
        len = DIST(Temp, nullv);
        MULT(Unorm, Temp, 1/len);
        CROSS(Temp, V10, V20);
        len = DIST(Temp, nullv);
        MULT(Vnorm, Temp, 1/len);
        CROSS(Temp, Unorm, Vnorm);
        double dist;
        dist = DIST(Temp, nullv);
        if(dist > 0.9999999999) return 90;
        return 180 / Math.PI * Math.asin(dist);
    }

    public void symSplitter(SymSplitterSettings sims) {

        if (linkedDatFile.isReadOnly()) return;

        setModified_NoSync();
        final String originalContent = linkedDatFile.getText();

        clearSelection();

        final BigDecimal o = sims.getOffset();
        final BigDecimal p = sims.getPrecision();
        final boolean needMerge = BigDecimal.ZERO.compareTo(p) != 0;
        final int sp = sims.getSplitPlane();
        boolean wasModified = false;

        if (sims.isCutAcross()) {
            // First, do the cutting with intersector :)

            // We have to create a really big quad at the cutting plane
            final GData4 splitPlane;
            final BigDecimal a = new BigDecimal(100000000);
            final BigDecimal an = a.negate();
            switch (sp) {
            case SymSplitterSettings.Z_PLUS:
            case SymSplitterSettings.Z_MINUS:
                splitPlane = new GData4(16, .5f, .5f, .5f, 1f, a, a, o, a, an, o, an, an, o, an, a, o, new Vector3d(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE), View.DUMMY_REFERENCE, linkedDatFile);
                break;
            case SymSplitterSettings.Y_PLUS:
            case SymSplitterSettings.Y_MINUS:
                splitPlane = new GData4(16, .5f, .5f, .5f, 1f, a, o, a, a, o, an, an, o, an, an, o, a, new Vector3d(BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO), View.DUMMY_REFERENCE, linkedDatFile);
                break;
            case SymSplitterSettings.X_PLUS:
            case SymSplitterSettings.X_MINUS:
                splitPlane = new GData4(16, .5f, .5f, .5f, 1f, o, a, a, o, a, an, o, an, an, o, an, a, new Vector3d(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO), View.DUMMY_REFERENCE, linkedDatFile);
                break;
            default:
                return;
            }
            linkedDatFile.addToTail(splitPlane);

            // Now we have to select the data which is cut by the split plane

            selectAll(null, true);

            {
                HashSet<GData> dataToCut = new HashSet<GData>();
                for (GData g : selectedData) {
                    if (!lineLinkedToVertices.containsKey(g)) continue;
                    final Vertex[] verts;
                    switch (g.type()) {
                    case 2:
                        verts = lines.get(g);
                        break;
                    case 3:
                        verts = triangles.get(g);
                        break;
                    case 4:
                        verts = quads.get(g);
                        break;
                    case 5:
                        Vertex[] v2 = condlines.get(g);
                        verts = new Vertex[]{v2[0], v2[1]};
                        break;
                    default:
                        continue;
                    }


                    final int targetValue = verts.length;
                    int currentValue = 0;
                    int neg = 0;
                    int pos = 0;
                    for (Vertex v : verts) {
                        switch (sp) {
                        case SymSplitterSettings.Z_PLUS:
                            if (v.Z.compareTo(o) > 0) {
                                pos++;
                            } else {
                                neg++;
                            }
                            break;
                        case SymSplitterSettings.Z_MINUS:
                            if (v.Z.compareTo(o) > 0) {
                                neg++;
                            } else {
                                pos++;
                            }
                            break;
                        case SymSplitterSettings.Y_PLUS:
                            if (v.Y.compareTo(o) > 0) {
                                pos++;
                            } else {
                                neg++;
                            }
                            break;
                        case SymSplitterSettings.Y_MINUS:
                            if (v.Y.compareTo(o) > 0) {
                                neg++;
                            } else {
                                pos++;
                            }
                            break;
                        case SymSplitterSettings.X_PLUS:
                            if (v.X.compareTo(o) > 0) {
                                pos++;
                            } else {
                                neg++;
                            }
                            break;
                        case SymSplitterSettings.X_MINUS:
                            if (v.X.compareTo(o) > 0) {
                                neg++;
                            } else {
                                pos++;
                            }
                            break;
                        }
                    }
                    currentValue = Math.max(neg, pos);
                    if (targetValue != currentValue) {
                        dataToCut.add(g);
                    }
                }

                clearSelection();

                for (GData g : dataToCut) {
                    switch (g.type()) {
                    case 2:
                        selectedLines.add((GData2) g);
                        break;
                    case 3:
                        selectedTriangles.add((GData3) g);
                        break;
                    case 4:
                        selectedQuads.add((GData4) g);
                        break;
                    case 5:
                        selectedCondlines.add((GData5) g);
                        break;
                    default:
                        continue;
                    }
                    selectedData.add(g);
                }
            }

            // Remove the quad from the selection
            selectedData.remove(splitPlane);
            selectedQuads.remove(splitPlane);

            List<OpenGLRenderer> renderers = Editor3DWindow.getRenders();
            for (OpenGLRenderer renderer : renderers) {
                if (renderer.getC3D().getLockableDatFileReference().equals(linkedDatFile)) {
                    linkedDatFile.setLastSelectedComposite(renderer.getC3D());
                }
            }

            intersector(new IntersectorSettings(), false);

            showAll();
            clearSelection();

            // Remove the split plane
            selectedData.add(splitPlane);
            selectedQuads.add(splitPlane);
            delete(false, false);
        }


        // Get header, since it is the same on all three sets (behind, between, before)
        final StringBuilder headerSb = new StringBuilder();
        final StringBuilder beforeSb = new StringBuilder();
        final StringBuilder betweenSb = new StringBuilder();
        final StringBuilder behindSb = new StringBuilder();
        final GData lastHeaderLine;
        {
            GData g = linkedDatFile.getDrawChainStart();
            while ((g = g.getNext()) != null) {
                headerSb.append(g.toString());
                headerSb.append(StringHelper.getLineDelimiter());
                if (g.getNext() == null || g.getNext().type() != 0 && !(g.getNext().type() == 6 && (
                        ((GDataBFC) g.getNext()).type == BFC.CCW_CLIP ||
                        ((GDataBFC) g.getNext()).type == BFC.CW_CLIP ||
                        ((GDataBFC) g.getNext()).type == BFC.NOCERTIFY
                        ))) {
                    break;
                }
            }
            if (g == null) {
                return;
            } else {
                lastHeaderLine = g;
            }
        }

        // Merge vertices to the plane
        if (needMerge) {
            selectAll(null, true);
            TreeSet<Vertex> allVertices = new TreeSet<Vertex>();
            allVertices.addAll(selectedVertices);
            clearSelection();
            for (Vertex v : allVertices) {
                switch (sp) {
                case SymSplitterSettings.Z_PLUS:
                case SymSplitterSettings.Z_MINUS:
                    if (p.compareTo(v.Z.subtract(o).abs()) > 0) {
                        wasModified = changeVertexDirectFast(v, new Vertex(v.X, v.Y, o), true) || wasModified;
                    }
                    break;
                case SymSplitterSettings.Y_PLUS:
                case SymSplitterSettings.Y_MINUS:
                    if (p.compareTo(v.Y.subtract(o).abs()) > 0) {
                        wasModified = changeVertexDirectFast(v, new Vertex(v.X, o, v.Z), true) || wasModified;
                    }
                    break;
                case SymSplitterSettings.X_PLUS:
                case SymSplitterSettings.X_MINUS:
                    if (p.compareTo(v.X.subtract(o).abs()) > 0) {
                        wasModified = changeVertexDirectFast(v, new Vertex(o, v.Y, v.Z), true) || wasModified;
                    }
                    break;
                }
            }
        }

        // Separate the data according the plane and detect invalid data (identical vertices)

        selectAll(null, true);

        {

            HashSet<GData> subfilesWithInvertnext = new HashSet<GData>();

            HashSet<GData> before = new HashSet<GData>();
            HashSet<GData> between = new HashSet<GData>();
            HashSet<GData> behind = new HashSet<GData>();

            for (GData g : selectedData) {
                if (!lineLinkedToVertices.containsKey(g)) continue;
                boolean forceMiddle = false;
                final Vertex[] verts;
                switch (g.type()) {
                case 1:
                    GData1 g1 = (GData1) g;
                    {
                        boolean hasInvertnext = false;
                        GData invertNextData = g1.getBefore();
                        while (invertNextData != null && invertNextData.type() != 1 && (invertNextData.type() != 6 || ((GDataBFC) invertNextData).type != BFC.INVERTNEXT)) {
                            invertNextData = invertNextData.getBefore();
                        }
                        if (invertNextData != null && invertNextData.type() == 6) {
                            hasInvertnext = true;
                        }
                        if (hasInvertnext) {
                            subfilesWithInvertnext.add(g1);
                        }
                    }
                    String shortName = g1.shortName.trim();
                    if (shortName.contains("stug") || shortName.contains("stud.dat") || shortName.contains("stud2.dat")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        forceMiddle = true;
                    }
                    verts = new Vertex[]{new Vertex(g1.accurateProductMatrix.M30, g1.accurateProductMatrix.M31, g1.accurateProductMatrix.M32)};
                    break;
                case 2:
                    verts = lines.get(g);
                    {
                        TreeSet<Vertex> vv = new TreeSet<Vertex>();
                        for (Vertex v : verts) {
                            vv.add(v);
                        }
                        if (vv.size() != 2) continue;
                    }
                    break;
                case 3:
                    verts = triangles.get(g);
                    {
                        TreeSet<Vertex> vv = new TreeSet<Vertex>();
                        for (Vertex v : verts) {
                            vv.add(v);
                        }
                        if (vv.size() != 3) continue;
                    }
                    break;
                case 4:
                    verts = quads.get(g);
                    {
                        TreeSet<Vertex> vv = new TreeSet<Vertex>();
                        for (Vertex v : verts) {
                            vv.add(v);
                        }
                        if (vv.size() != 4) continue;
                    }
                    break;
                case 5:
                    Vertex[] v2 = condlines.get(g);
                    verts = new Vertex[]{v2[0], v2[1]};
                    {
                        TreeSet<Vertex> vv = new TreeSet<Vertex>();
                        for (Vertex v : verts) {
                            vv.add(v);
                        }
                        if (vv.size() != 2) continue;
                    }
                    break;
                default:
                    continue;
                }

                final int targetValue = verts.length;
                int currentValue = 0;
                int neg = 0;
                int pos = 0;
                for (Vertex v : verts) {
                    switch (sp) {
                    case SymSplitterSettings.Z_PLUS:
                        switch (v.Z.compareTo(o)) {
                        case -1:
                            neg++;
                            break;
                        case 0:
                            neg++;
                            pos++;
                            break;
                        case 1:
                            pos++;
                            break;
                        }
                        break;
                    case SymSplitterSettings.Z_MINUS:
                        switch (v.Z.compareTo(o)) {
                        case -1:
                            pos++;
                            break;
                        case 0:
                            neg++;
                            pos++;
                            break;
                        case 1:
                            neg++;
                            break;
                        }
                        break;
                    case SymSplitterSettings.Y_PLUS:
                        switch (v.Y.compareTo(o)) {
                        case -1:
                            neg++;
                            break;
                        case 0:
                            neg++;
                            pos++;
                            break;
                        case 1:
                            pos++;
                            break;
                        }
                        break;
                    case SymSplitterSettings.Y_MINUS:
                        switch (v.Y.compareTo(o)) {
                        case -1:
                            pos++;
                            break;
                        case 0:
                            neg++;
                            pos++;
                            break;
                        case 1:
                            neg++;
                            break;
                        }
                        break;
                    case SymSplitterSettings.X_PLUS:
                        switch (v.X.compareTo(o)) {
                        case -1:
                            neg++;
                            break;
                        case 0:
                            neg++;
                            pos++;
                            break;
                        case 1:
                            pos++;
                            break;
                        }
                        break;
                    case SymSplitterSettings.X_MINUS:
                        switch (v.X.compareTo(o)) {
                        case -1:
                            pos++;
                            break;
                        case 0:
                            neg++;
                            pos++;
                            break;
                        case 1:
                            neg++;
                            break;
                        }
                        break;
                    }
                }
                currentValue = Math.max(neg, pos);
                if (forceMiddle || targetValue == currentValue && neg == pos) {
                    between.add(g);
                } else if (targetValue != currentValue) {
                    between.add(g);
                } else if (pos == targetValue) {
                    before.add(g);
                } else if (neg == targetValue) {
                    behind.add(g);
                } else {
                    between.add(g);
                }
            }

            // Colourise only before and between

            final GData tail = linkedDatFile.getDrawChainTail();
            GColour blue = View.hasLDConfigColour(1) ? View.getLDConfigColour(1) : new GColour(-1, 0f, 0f, 1f, 1f);
            GColour yellow = View.hasLDConfigColour(14) ? View.getLDConfigColour(14) : new GColour(-1, 1f, 1f, 0f, 1f);
            GColour red = View.hasLDConfigColour(4) ? View.getLDConfigColour(4) : new GColour(-1, 1f, 0f, 0f, 1f);
            GColour lightBlue = View.hasLDConfigColour(9) ? View.getLDConfigColour(9) : new GColour(-1, 1f, .5f, .5f, 1f);
            GColour lightGreen = View.hasLDConfigColour(10) ? View.getLDConfigColour(10) : new GColour(-1, .5f, 1f, .5f, 1f);
            GColour violet = View.hasLDConfigColour(5) ? View.getLDConfigColour(5) : new GColour(-1, 1f, 0f, 1f, 1f);
            {
                GData g = lastHeaderLine;
                while ((g = g.getNext()) != null) {
                    if (g.type() < 1 || g.type() > 5) {
                        if (g.type() == 6 && ((GDataBFC) g).type == BFC.INVERTNEXT) continue;
                        beforeSb.append(g.toString());
                        if (!g.equals(tail)) beforeSb.append(StringHelper.getLineDelimiter());
                    } else {
                        if (before.contains(g)) {
                            if (sims.isColourise() && lineLinkedToVertices.containsKey(g)) {
                                switch (g.type()) {
                                case 3:
                                    beforeSb.append(((GData3) g).colourReplace(lightBlue.toString()));
                                    break;
                                case 4:
                                    beforeSb.append(((GData4) g).colourReplace(lightBlue.toString()));
                                    break;
                                case 1:
                                    if (subfilesWithInvertnext.contains(g)) {
                                        beforeSb.append(new GDataBFC(BFC.INVERTNEXT).toString());
                                    }
                                    beforeSb.append(((GData1) g).colourReplace(blue.toString()));
                                    break;
                                case 2:
                                    beforeSb.append(((GData2) g).colourReplace(blue.toString()));
                                    break;
                                case 5:
                                    beforeSb.append(((GData5) g).colourReplace(blue.toString()));
                                    break;
                                default:
                                    break;
                                }
                            } else {
                                if (subfilesWithInvertnext.contains(g)) {
                                    beforeSb.append(new GDataBFC(BFC.INVERTNEXT).toString());
                                }
                                beforeSb.append(g.toString());
                            }
                            if (!g.equals(tail)) beforeSb.append(StringHelper.getLineDelimiter());
                        }
                    }
                }
            }
            {
                GData g = lastHeaderLine;
                while ((g = g.getNext()) != null) {
                    if (g.type() < 1 || g.type() > 5) {
                        if (g.type() == 6 && ((GDataBFC) g).type == BFC.INVERTNEXT) continue;
                        betweenSb.append(g.toString());
                        if (!g.equals(tail)) betweenSb.append(StringHelper.getLineDelimiter());
                    } else {
                        if (between.contains(g)) {

                            if (sims.isValidate() && lineLinkedToVertices.containsKey(g)) {

                                boolean isSymmetrical = true;

                                if (g.type() != 1) {
                                    // Check symmetry

                                    final Vertex[] verts;
                                    switch (g.type()) {
                                    case 2:
                                        verts = lines.get(g);
                                        break;
                                    case 3:
                                        verts = triangles.get(g);
                                        break;
                                    case 4:
                                        verts = quads.get(g);
                                        break;
                                    case 5:
                                        Vertex[] v2 = condlines.get(g);
                                        verts = new Vertex[]{v2[0], v2[1]};
                                        break;
                                    default:
                                        continue;
                                    }

                                    switch (sp) {
                                    case SymSplitterSettings.Z_PLUS:
                                    case SymSplitterSettings.Z_MINUS:
                                        for (int i = 0; i < verts.length - 1; i++) {
                                            int j = (i + 1) % verts.length;
                                            BigDecimal di = verts[i].Z.subtract(o);
                                            BigDecimal dj = verts[j].Z.subtract(o);
                                            if (di.signum() != dj.signum()) {
                                                if (di.abs().subtract(dj.abs()).abs().compareTo(p) > 0) {
                                                    isSymmetrical = false;
                                                    break;
                                                }
                                            }
                                        }
                                        break;
                                    case SymSplitterSettings.Y_PLUS:
                                    case SymSplitterSettings.Y_MINUS:
                                        for (int i = 0; i < verts.length - 1; i++) {
                                            int j = (i + 1) % verts.length;
                                            BigDecimal di = verts[i].Y.subtract(o);
                                            BigDecimal dj = verts[j].Y.subtract(o);
                                            if (di.signum() != dj.signum()) {
                                                if (di.abs().subtract(dj.abs()).abs().compareTo(p) > 0) {
                                                    isSymmetrical = false;
                                                    break;
                                                }
                                            }
                                        }
                                        break;
                                    case SymSplitterSettings.X_PLUS:
                                    case SymSplitterSettings.X_MINUS:
                                        for (int i = 0; i < verts.length - 1; i++) {
                                            int j = (i + 1) % verts.length;
                                            BigDecimal di = verts[i].X.subtract(o);
                                            BigDecimal dj = verts[j].X.subtract(o);
                                            if (di.signum() != dj.signum()) {
                                                if (di.abs().subtract(dj.abs()).abs().compareTo(p) > 0) {
                                                    isSymmetrical = false;
                                                    break;
                                                }
                                            }
                                        }
                                        break;
                                    }
                                }

                                switch (g.type()) {
                                case 3:
                                    if (isSymmetrical) {
                                        betweenSb.append(((GData3) g).colourReplace(yellow.toString()));
                                    } else {
                                        betweenSb.append(((GData3) g).colourReplace(violet.toString()));
                                    }
                                    break;
                                case 4:
                                    if (isSymmetrical) {
                                        betweenSb.append(((GData4) g).colourReplace(yellow.toString()));
                                    } else {
                                        betweenSb.append(((GData4) g).colourReplace(violet.toString()));
                                    }
                                    break;
                                case 1:
                                    if (subfilesWithInvertnext.contains(g)) {
                                        betweenSb.append(new GDataBFC(BFC.INVERTNEXT).toString());
                                    }
                                    betweenSb.append(((GData1) g).colourReplace(yellow.toString()));
                                    break;
                                case 2:
                                    if (isSymmetrical) {
                                        betweenSb.append(((GData2) g).colourReplace(red.toString()));
                                    } else {
                                        betweenSb.append(((GData2) g).colourReplace(lightGreen.toString()));
                                    }

                                    break;
                                case 5:
                                    if (isSymmetrical) {
                                        betweenSb.append(((GData5) g).colourReplace(red.toString()));
                                    } else {
                                        betweenSb.append(((GData5) g).colourReplace(lightGreen.toString()));
                                    }
                                    break;
                                default:
                                    break;
                                }

                            } else if (sims.isColourise() && lineLinkedToVertices.containsKey(g)) {
                                switch (g.type()) {
                                case 3:
                                    betweenSb.append(((GData3) g).colourReplace(yellow.toString()));
                                    break;
                                case 4:
                                    betweenSb.append(((GData4) g).colourReplace(yellow.toString()));
                                    break;
                                case 1:
                                    if (subfilesWithInvertnext.contains(g)) {
                                        betweenSb.append(new GDataBFC(BFC.INVERTNEXT).toString());
                                    }
                                    betweenSb.append(((GData1) g).colourReplace(yellow.toString()));
                                    break;
                                case 2:
                                    betweenSb.append(((GData2) g).colourReplace(red.toString()));
                                    break;
                                case 5:
                                    betweenSb.append(((GData5) g).colourReplace(red.toString()));
                                    break;
                                default:
                                    break;
                                }
                            } else {
                                if (subfilesWithInvertnext.contains(g)) {
                                    betweenSb.append(new GDataBFC(BFC.INVERTNEXT).toString());
                                }
                                betweenSb.append(g.toString());
                            }
                            if (!g.equals(tail)) betweenSb.append(StringHelper.getLineDelimiter());
                        }
                    }
                }
            }
            {
                GData g = lastHeaderLine;
                while ((g = g.getNext()) != null) {
                    if (g.type() < 1 || g.type() > 5) {
                        if (g.type() == 6 && ((GDataBFC) g).type == BFC.INVERTNEXT) continue;
                        behindSb.append(g.toString());
                        if (!g.equals(tail)) behindSb.append(StringHelper.getLineDelimiter());
                    } else {
                        if (behind.contains(g)) {
                            if (subfilesWithInvertnext.contains(g)) {
                                behindSb.append(new GDataBFC(BFC.INVERTNEXT).toString());
                            }
                            behindSb.append(g.toString());
                            if (!g.equals(tail)) behindSb.append(StringHelper.getLineDelimiter());
                        }
                    }
                }
            }

            String headerS = headerSb.toString();
            String beforeS = beforeSb.toString();
            String betweenS = betweenSb.toString();
            String behindS = behindSb.toString();

            if (!headerS.endsWith(StringHelper.getLineDelimiter())) {
                headerS = headerS + StringHelper.getLineDelimiter();
            }
            headerS = headerS + "0 !LPE TODO SymSplitter: Section in front of the plane." + StringHelper.getLineDelimiter(); //$NON-NLS-1$ I18N
            if (!beforeS.endsWith(StringHelper.getLineDelimiter())) {
                beforeS = beforeS + StringHelper.getLineDelimiter();
            }
            beforeS = beforeS + "0 !LPE TODO SymSplitter: Section between the plane." + StringHelper.getLineDelimiter(); //$NON-NLS-1$ I18N
            if (!betweenS.endsWith(StringHelper.getLineDelimiter())) {
                betweenS = betweenS + StringHelper.getLineDelimiter();
            }
            betweenS = betweenS + "0 !LPE TODO SymSplitter: Section behind the plane." + StringHelper.getLineDelimiter(); //$NON-NLS-1$ I18N

            String symSplitterOutput = headerS + beforeS + betweenS + behindS;

            if (wasModified || !symSplitterOutput.equals(originalContent)) {
                if (!Project.getUnsavedFiles().contains(linkedDatFile)) {
                    Project.addUnsavedFile(linkedDatFile);
                    Editor3DWindow.getWindow().updateTree_unsavedEntries();
                }

                GDataCSG.resetCSG();
                GDataCSG.forceRecompile();
                setModified_NoSync();
                linkedDatFile.setText(symSplitterOutput);
                linkedDatFile.parseForData(true);

                setModified_NoSync();

                // Separate the data according the plane and hide or show it

                if (sims.getHideLevel() > 0) {

                    selectAll(null, true);

                    before.clear();
                    between.clear();
                    behind.clear();

                    for (GData g : selectedData) {
                        if (!lineLinkedToVertices.containsKey(g)) continue;
                        boolean forceMiddle = false;
                        final Vertex[] verts;
                        switch (g.type()) {
                        case 1:
                            GData1 g1 = (GData1) g;
                            String shortName = g1.shortName.trim();
                            if (shortName.contains("stug") || shortName.contains("stud.dat") || shortName.contains("stud2.dat")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                forceMiddle = true;
                            }
                            verts = new Vertex[]{new Vertex(g1.accurateProductMatrix.M30, g1.accurateProductMatrix.M31, g1.accurateProductMatrix.M32)};
                            break;
                        case 2:
                            verts = lines.get(g);
                            {
                                TreeSet<Vertex> vv = new TreeSet<Vertex>();
                                for (Vertex v : verts) {
                                    vv.add(v);
                                }
                                if (vv.size() != 2) continue;
                            }
                            break;
                        case 3:
                            verts = triangles.get(g);
                            {
                                TreeSet<Vertex> vv = new TreeSet<Vertex>();
                                for (Vertex v : verts) {
                                    vv.add(v);
                                }
                                if (vv.size() != 3) continue;
                            }
                            break;
                        case 4:
                            verts = quads.get(g);
                            {
                                TreeSet<Vertex> vv = new TreeSet<Vertex>();
                                for (Vertex v : verts) {
                                    vv.add(v);
                                }
                                if (vv.size() != 4) continue;
                            }
                            break;
                        case 5:
                            Vertex[] v2 = condlines.get(g);
                            verts = new Vertex[]{v2[0], v2[1]};
                            {
                                TreeSet<Vertex> vv = new TreeSet<Vertex>();
                                for (Vertex v : verts) {
                                    vv.add(v);
                                }
                                if (vv.size() != 2) continue;
                            }
                            break;
                        default:
                            continue;
                        }

                        final int targetValue = verts.length;
                        int currentValue = 0;
                        int neg = 0;
                        int pos = 0;
                        for (Vertex v : verts) {
                            switch (sp) {
                            case SymSplitterSettings.Z_PLUS:
                                switch (v.Z.compareTo(o)) {
                                case -1:
                                    neg++;
                                    break;
                                case 0:
                                    neg++;
                                    pos++;
                                    break;
                                case 1:
                                    pos++;
                                    break;
                                }
                                break;
                            case SymSplitterSettings.Z_MINUS:
                                switch (v.Z.compareTo(o)) {
                                case -1:
                                    pos++;
                                    break;
                                case 0:
                                    neg++;
                                    pos++;
                                    break;
                                case 1:
                                    neg++;
                                    break;
                                }
                                break;
                            case SymSplitterSettings.Y_PLUS:
                                switch (v.Y.compareTo(o)) {
                                case -1:
                                    neg++;
                                    break;
                                case 0:
                                    neg++;
                                    pos++;
                                    break;
                                case 1:
                                    pos++;
                                    break;
                                }
                                break;
                            case SymSplitterSettings.Y_MINUS:
                                switch (v.Y.compareTo(o)) {
                                case -1:
                                    pos++;
                                    break;
                                case 0:
                                    neg++;
                                    pos++;
                                    break;
                                case 1:
                                    neg++;
                                    break;
                                }
                                break;
                            case SymSplitterSettings.X_PLUS:
                                switch (v.X.compareTo(o)) {
                                case -1:
                                    neg++;
                                    break;
                                case 0:
                                    neg++;
                                    pos++;
                                    break;
                                case 1:
                                    pos++;
                                    break;
                                }
                                break;
                            case SymSplitterSettings.X_MINUS:
                                switch (v.X.compareTo(o)) {
                                case -1:
                                    pos++;
                                    break;
                                case 0:
                                    neg++;
                                    pos++;
                                    break;
                                case 1:
                                    neg++;
                                    break;
                                }
                                break;
                            }
                        }
                        currentValue = Math.max(neg, pos);
                        if (forceMiddle || targetValue == currentValue && neg == pos) {
                            between.add(g);
                        } else if (targetValue != currentValue) {
                            between.add(g);
                        } else if (pos == targetValue) {
                            before.add(g);
                        } else if (neg == targetValue) {
                            behind.add(g);
                        } else {
                            between.add(g);
                        }
                    }

                    hiddenVertices.addAll(selectedVertices);
                    clearSelection();

                    switch (sims.getHideLevel()) {
                    case 1: // between
                        for (GData g : before) {
                            hide(g);
                        }
                        for (GData g : behind) {
                            hide(g);
                        }
                        break;
                    case 2: // before
                        for (GData g : between) {
                            hide(g);
                        }
                        for (GData g : behind) {
                            hide(g);
                        }
                        break;
                    case 3: // behind
                        for (GData g : before) {
                            hide(g);
                        }
                        for (GData g : between) {
                            hide(g);
                        }
                        break;
                    default:
                        break;
                    }
                }

                if (isModified()) {
                    syncWithTextEditors(true);
                }

            } else {
                setModified(false, true);
            }

        }
        validateState();
    }

    public void unificator(final UnificatorSettings us) {

        if (linkedDatFile.isReadOnly()) return;

        final BigDecimal vt = us.getVertexThreshold().multiply(us.getVertexThreshold());
        final BigDecimal st = us.getSubvertexThreshold().multiply(us.getSubvertexThreshold());

        if (us.getScope() == 0) {
            selectAll(null, true);
        } else {
            for (GData gd : selectedData) {
                Vertex[] verts = null;
                switch (gd.type()) {
                case 2:
                    verts = lines.get(gd);
                    break;
                case 3:
                    verts = triangles.get(gd);
                    break;
                case 4:
                    verts = quads.get(gd);
                    break;
                case 5:
                    verts = condlines.get(gd);
                    break;
                default:
                    continue;
                }
                for (Vertex v : verts) {
                    selectedVertices.add(v);
                }
            }
        }

        final TreeSet<Vertex> selectedVerts = new TreeSet<Vertex>();
        selectedVerts.addAll(selectedVertices);

        clearSelection();

        try
        {
            new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, false, new IRunnableWithProgress()
            {
                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                {
                    monitor.beginTask("Running Unificator (this may take some time)", IProgressMonitor.UNKNOWN); //$NON-NLS-1$ I18N

                    monitor.subTask("Sorting out vertices..."); //$NON-NLS-1$ I18N

                    TreeSet<Vertex> subfileVertices = new TreeSet<Vertex>();
                    TreeSet<Vertex> fileVertices = new TreeSet<Vertex>();

                    for (Vertex v : selectedVerts) {

                        boolean isFileVertex = false;

                        if (vertexLinkedToSubfile.containsKey(v)) {
                            // Do not add points for condlines in subparts.
                            Set<VertexManifestation> mani = vertexLinkedToPositionInFile.get(v);
                            int controlPointCondlineInSubfile = 0;
                            for (VertexManifestation vm : mani) {
                                GData gd = vm.getGdata();
                                if (lineLinkedToVertices.containsKey(gd)) {
                                    // Better performance, since we can detect file vertices here!
                                    fileVertices.add(v);
                                    isFileVertex = true;
                                    break;
                                } else if (gd.type() == 5 && vm.getPosition() > 1) {
                                    controlPointCondlineInSubfile++;
                                }
                            }
                            if (controlPointCondlineInSubfile == mani.size()) {
                                continue;
                            }
                            subfileVertices.add(v);
                        }

                        if (!isFileVertex) {
                            Set<VertexManifestation> mani = vertexLinkedToPositionInFile.get(v);
                            for (VertexManifestation vm : mani) {
                                GData gd = vm.getGdata();
                                if (lineLinkedToVertices.containsKey(gd)) {
                                    fileVertices.add(v);
                                    break;
                                }
                            }
                        }
                    }


                    if (us.getSnapOn() == 0 || us.getSnapOn() == 2) {
                        monitor.subTask("Unify vertices..."); //$NON-NLS-1$ I18N
                        int i = 0;
                        int j = 0;

                        TreeMap<Vertex, Vertex> mergeTargets = new TreeMap<Vertex, Vertex>();
                        {
                            TreeMap<Vertex, TreeSet<Vertex>> unifyGroups = new TreeMap<Vertex, TreeSet<Vertex>>();
                            TreeSet<Vertex> inGroup = new TreeSet<Vertex>();

                            for (Vertex v1 : fileVertices) {
                                TreeSet<Vertex> group = new TreeSet<Vertex>();
                                group.add(v1);
                                for (Vertex v2 : fileVertices) {
                                    if (j > i && !inGroup.contains(v2)) {
                                        Vector3d v3d1 = new Vector3d(v1);
                                        Vector3d v3d2 = new Vector3d(v2);
                                        if (Vector3d.distSquare(v3d1, v3d2).compareTo(vt) < 0) {
                                            group.add(v2);
                                            inGroup.add(v2);
                                        }
                                    }
                                    j++;
                                }
                                unifyGroups.put(v1, group);
                                i++;
                            }

                            fileVertices.clear();

                            Set<Vertex> keySet = unifyGroups.keySet();
                            for (Vertex key : keySet) {
                                TreeSet<Vertex> group = unifyGroups.get(key);
                                if (group.size() > 1) {
                                    BigDecimal X = BigDecimal.ZERO;
                                    BigDecimal Y = BigDecimal.ZERO;
                                    BigDecimal Z = BigDecimal.ZERO;
                                    BigDecimal gc = new BigDecimal(group.size());
                                    for (Vertex gv : group) {
                                        X = X.add(gv.X);
                                        Y = Y.add(gv.Y);
                                        Z = Z.add(gv.Z);
                                    }
                                    X = X.divide(gc, Threshold.mc);
                                    Y = Y.divide(gc, Threshold.mc);
                                    Z = Z.divide(gc, Threshold.mc);
                                    Vertex newVertex = new Vertex(X, Y, Z);
                                    fileVertices.add(newVertex);
                                    for (Vertex gv : group) {
                                        mergeTargets.put(gv, newVertex);
                                    }
                                } else {
                                    fileVertices.add(key);
                                }
                            }
                        }

                        Set<Vertex> keySet = mergeTargets.keySet();
                        for (Vertex key : keySet) {
                            Vertex target = mergeTargets.get(key);
                            changeVertexDirectFast(key, target, true);
                            selectedVertices.add(target);
                        }
                    }

                    if (us.getSnapOn() == 1 || us.getSnapOn() == 2) {
                        monitor.subTask("Snap vertices to subfiles..."); //$NON-NLS-1$ I18N

                        int i = 0;
                        int j = 0;

                        TreeMap<Vertex, Vertex> mergeTargets = new TreeMap<Vertex, Vertex>();
                        {
                            TreeMap<Vertex, TreeSet<Vertex>> unifyGroups = new TreeMap<Vertex, TreeSet<Vertex>>();
                            TreeSet<Vertex> inGroup = new TreeSet<Vertex>();

                            for (Vertex v1 : subfileVertices) {
                                TreeSet<Vertex> group = new TreeSet<Vertex>();
                                for (Vertex v2 : fileVertices) {
                                    if (j > i && !inGroup.contains(v2)) {
                                        Vector3d v3d1 = new Vector3d(v1);
                                        Vector3d v3d2 = new Vector3d(v2);
                                        if (Vector3d.distSquare(v3d1, v3d2).compareTo(st) < 0) {
                                            group.add(v2);
                                            inGroup.add(v2);
                                        }
                                    }
                                    j++;
                                }
                                unifyGroups.put(v1, group);
                                i++;
                            }

                            fileVertices.clear();

                            Set<Vertex> keySet = unifyGroups.keySet();
                            for (Vertex key : keySet) {
                                TreeSet<Vertex> group = unifyGroups.get(key);
                                if (group.size() > 0) {
                                    for (Vertex gv : group) {
                                        mergeTargets.put(gv, key);
                                    }
                                }
                            }
                        }

                        Set<Vertex> keySet = mergeTargets.keySet();
                        for (Vertex key : keySet) {
                            Vertex target = mergeTargets.get(key);
                            changeVertexDirectFast(key, target, true);
                            selectedVertices.add(target);
                        }
                    }
                }
            });
        } catch (InvocationTargetException consumed) {
        } catch (InterruptedException consumed) {
        }

        // Round selection to 6 decimal places
        selectedVerts.clear();
        selectedVerts.addAll(selectedVertices);

        clearSelection();

        NLogger.debug(getClass(), "Check for identical vertices and collinearity."); //$NON-NLS-1$

        final Set<GData2> linesToDelete2 = new HashSet<GData2>();
        final Set<GData3> trisToDelete2 = new HashSet<GData3>();
        final Set<GData4> quadsToDelete2 = new HashSet<GData4>();
        final Set<GData5> clinesToDelete2 = new HashSet<GData5>();
        {
            for (GData2 g2 : lines.keySet()) {
                if (!lineLinkedToVertices.containsKey(g2)) continue;
                Vertex[] verts = lines.get(g2);
                Set<Vertex> verts2 = new TreeSet<Vertex>();
                for (Vertex vert : verts) {
                    verts2.add(vert);
                }
                if (verts2.size() < 2) {
                    linesToDelete2.add(g2);
                }
            }
            for (GData3 g3 : triangles.keySet()) {
                if (!lineLinkedToVertices.containsKey(g3)) continue;
                Vertex[] verts = triangles.get(g3);
                Set<Vertex> verts2 = new TreeSet<Vertex>();
                for (Vertex vert : verts) {
                    verts2.add(vert);
                }
                if (verts2.size() < 3 || g3.isCollinear()) {
                    trisToDelete2.add(g3);
                }
            }
            for (GData4 g4 : quads.keySet()) {
                if (!lineLinkedToVertices.containsKey(g4)) continue;
                Vertex[] verts = quads.get(g4);
                Set<Vertex> verts2 = new TreeSet<Vertex>();
                for (Vertex vert : verts) {
                    verts2.add(vert);
                }
                if (verts2.size() < 4 || g4.isCollinear()) {
                    quadsToDelete2.add(g4);
                }
            }
            for (GData5 g5 : condlines.keySet()) {
                if (!lineLinkedToVertices.containsKey(g5)) continue;
                Vertex[] verts = condlines.get(g5);
                Set<Vertex> verts2 = new TreeSet<Vertex>();
                for (Vertex vert : verts) {
                    verts2.add(vert);
                }
                if (verts2.size() < 4) {
                    clinesToDelete2.add(g5);
                }
            }
        }

        selectedLines.addAll(linesToDelete2);
        selectedTriangles.addAll(trisToDelete2);
        selectedQuads.addAll(quadsToDelete2);
        selectedCondlines.addAll(clinesToDelete2);
        selectedData.addAll(selectedLines);
        selectedData.addAll(selectedTriangles);
        selectedData.addAll(selectedQuads);
        selectedData.addAll(selectedCondlines);
        delete(false, false);

        // Round selection to 6 decimal places
        selectedVertices.addAll(selectedVerts);

        NLogger.debug(getClass(), "Round."); //$NON-NLS-1$
        roundSelection(6, 10, true, true);

        validateState();
    }

    public void flipSelection() {

        if (linkedDatFile.isReadOnly()) return;

        final Set<GData2> newLines = new HashSet<GData2>();
        final Set<GData3> newTriangles = new HashSet<GData3>();
        final Set<GData4> newQuads = new HashSet<GData4>();
        final Set<GData5> newCondlines = new HashSet<GData5>();

        final Set<GData2> effSelectedLines = new HashSet<GData2>();
        final Set<GData3> effSelectedTriangles = new HashSet<GData3>();
        final Set<GData4> effSelectedQuads = new HashSet<GData4>();
        final Set<GData5> effSelectedCondlines = new HashSet<GData5>();

        final Set<GData2> linesToDelete2 = new HashSet<GData2>();
        final Set<GData3> trisToDelete2 = new HashSet<GData3>();
        final Set<GData4> quadsToDelete2 = new HashSet<GData4>();
        final Set<GData5> clinesToDelete2 = new HashSet<GData5>();

        {
            for (GData2 g2 : selectedLines) {
                if (!lineLinkedToVertices.containsKey(g2)) continue;
                effSelectedLines.add(g2);
            }
            for (GData3 g3 : selectedTriangles) {
                if (!lineLinkedToVertices.containsKey(g3)) continue;
                effSelectedTriangles.add(g3);
            }
            for (GData4 g4 : selectedQuads) {
                if (!lineLinkedToVertices.containsKey(g4)) continue;
                effSelectedQuads.add(g4);
            }
            for (GData5 g5 : selectedCondlines) {
                if (!lineLinkedToVertices.containsKey(g5)) continue;
                effSelectedCondlines.add(g5);
            }
        }

        clearSelection();

        // Special case for coloured triangle pairs (Flipper behaviour)
        {
            HashSet<GData3> surfsToIgnore = new HashSet<GData3>();
            HashMap<GData3, GData3> trianglePair = new HashMap<GData3, GData3>();
            int i = 0;
            int j = 0;
            for (GData3 s1 : effSelectedTriangles) {
                for (GData3 s2 : effSelectedTriangles) {
                    if (j > i && !surfsToIgnore.contains(s2)) {
                        if (s1.colourNumber != 16 && s1.colourNumber == s2.colourNumber && s1.r == s2.r && s1.g == s2.g && s1.b == s2.b && hasSameEdge(s1, s2)) {
                            surfsToIgnore.add(s1);
                            surfsToIgnore.add(s2);
                            trianglePair.put(s1, s2);
                        }
                    }
                    j++;
                }
                i++;
            }
            effSelectedTriangles.removeAll(surfsToIgnore);
            for (GData3 s1 : trianglePair.keySet()) {
                GData3 s2 = trianglePair.get(s1);
                {
                    TreeSet<Vertex> v1 = new TreeSet<Vertex>();
                    TreeSet<Vertex> v2 = new TreeSet<Vertex>();
                    for (Vertex v : triangles.get(s1)) {
                        v1.add(v);
                    }
                    for (Vertex v : triangles.get(s2)) {
                        v2.add(v);
                    }
                    {
                        TreeSet<Vertex> sum = new TreeSet<Vertex>();
                        sum.addAll(v1);
                        sum.addAll(v2);
                        if (sum.size() != 4) continue;
                    }
                    v1.retainAll(v2);
                    if (v1.size() == 2) {

                        Vertex first = null;
                        Vertex second = null;
                        Vertex third = null;
                        Vertex fourth = null;
                        int i1 = 0;
                        for (Vertex v : triangles.get(s1)) {
                            if (!v1.contains(v)) {
                                first = v;
                                break;
                            }
                            i1++;
                        }
                        if (first == null) continue;
                        int i2 = 0;
                        for (Vertex v : triangles.get(s1)) {
                            if ((i2 > i1 || i1 == 2 && i2 == 0) && v1.contains(v)) {
                                second = v;
                                break;
                            }
                            i2++;
                        }
                        if (second == null) continue;
                        for (Vertex v : triangles.get(s2)) {
                            if (!v1.contains(v)) {
                                third = v;
                                break;
                            }
                        }
                        if (third == null) continue;
                        for (Vertex v : triangles.get(s2)) {
                            if (v1.contains(v) && !v.equals(second)) {
                                fourth = v;
                                break;
                            }
                        }
                        if (fourth == null) continue;

                        GData3 n1 = new GData3(s1.colourNumber, s1.r, s1.g, s1.b, s1.a, first, second, third, s1.parent, linkedDatFile);
                        GData3 n2 = new GData3(s1.colourNumber, s1.r, s1.g, s1.b, s1.a, third, fourth, first, s1.parent, linkedDatFile);

                        linkedDatFile.insertAfter(s1, n1);
                        linkedDatFile.insertAfter(s2, n2);

                        newTriangles.add(n1);
                        newTriangles.add(n2);

                        trisToDelete2.add(s1);
                        trisToDelete2.add(s2);

                        Iterator<Vertex> vit = v1.iterator();
                        Vertex cv1 = vit.next();
                        Vertex cv2 = vit.next();
                        GData5 cline = hasCondline(cv1, cv2);
                        if (cline != null && lineLinkedToVertices.containsKey(cline)) {
                            linkedDatFile.insertAfter(cline, new GData5(cline.colourNumber, cline.r, cline.g, cline.b, cline.a, first, third, second, fourth, cline.parent, linkedDatFile));
                            clinesToDelete2.add(cline);
                        }
                    }
                }

            }
        }

        for (GData2 g : effSelectedLines) {
            GData2 n = new GData2(g.colourNumber, g.r, g.g, g.b, g.a, g.X2, g.Y2, g.Z2, g.X1, g.Y1, g.Z1, g.parent, linkedDatFile);
            newLines.add(n);
            linkedDatFile.insertAfter(g, n);
            linesToDelete2.add(g);
        }

        for (GData3 g : effSelectedTriangles) {
            GData3 n = new GData3(g.colourNumber, g.r, g.g, g.b, g.a, new Vertex(g.X3, g.Y3, g.Z3), new Vertex(g.X1, g.Y1, g.Z1), new Vertex(g.X2, g.Y2, g.Z2), g.parent, linkedDatFile);
            newTriangles.add(n);
            linkedDatFile.insertAfter(g, n);
            trisToDelete2.add(g);
        }

        for (GData4 g : effSelectedQuads) {
            GData4 n = new GData4(g.colourNumber, g.r, g.g, g.b, g.a, new Vertex(g.X4, g.Y4, g.Z4), new Vertex(g.X1, g.Y1, g.Z1), new Vertex(g.X2, g.Y2, g.Z2), new Vertex(g.X3, g.Y3, g.Z3), g.parent, linkedDatFile);
            newQuads.add(n);
            linkedDatFile.insertAfter(g, n);
            quadsToDelete2.add(g);
        }

        for (GData5 g : effSelectedCondlines) {
            GData5 n = new GData5(g.colourNumber, g.r, g.g, g.b, g.a, new Vertex(g.X2, g.Y2, g.Z2), new Vertex(g.X1, g.Y1, g.Z1), new Vertex(g.X3, g.Y3, g.Z3), new Vertex(g.X4, g.Y4, g.Z4), g.parent, linkedDatFile);
            newCondlines.add(n);
            linkedDatFile.insertAfter(g, n);
            clinesToDelete2.add(g);
        }

        if (newLines.size() + newTriangles.size() + newQuads.size() + newCondlines.size() > 0) {
            setModified_NoSync();
        }

        selectedLines.addAll(linesToDelete2);
        selectedTriangles.addAll(trisToDelete2);
        selectedQuads.addAll(quadsToDelete2);
        selectedCondlines.addAll(clinesToDelete2);
        selectedData.addAll(selectedLines);
        selectedData.addAll(selectedTriangles);
        selectedData.addAll(selectedQuads);
        selectedData.addAll(selectedCondlines);
        delete(false, false);

        clearSelection();

        selectedLines.addAll(newLines);
        selectedTriangles.addAll(newTriangles);
        selectedQuads.addAll(newQuads);
        selectedCondlines.addAll(newCondlines);
        selectedData.addAll(selectedLines);
        selectedData.addAll(selectedTriangles);
        selectedData.addAll(selectedQuads);
        selectedData.addAll(selectedCondlines);

        if (isModified()) {
            syncWithTextEditors(true);
        }
        validateState();
    }

    public void selectAllWithSameColours(SelectorSettings ss, boolean includeHidden) {

        final Set<GColour> allColours = new HashSet<GColour>();

        final Set<GData1> effSelectedSubfiles = new HashSet<GData1>();
        final Set<GData2> effSelectedLines = new HashSet<GData2>();
        final Set<GData3> effSelectedTriangles = new HashSet<GData3>();
        final Set<GData4> effSelectedQuads = new HashSet<GData4>();
        final Set<GData5> effSelectedCondlines = new HashSet<GData5>();

        for (GData1 g : selectedSubfiles) {
            allColours.add(new GColour(g.colourNumber, g.r, g.g, g.b, g.a));
        }
        for (GData2 g : selectedLines) {
            allColours.add(new GColour(g.colourNumber, g.r, g.g, g.b, g.a));
        }
        for (GData3 g : selectedTriangles) {
            allColours.add(new GColour(g.colourNumber, g.r, g.g, g.b, g.a));
        }
        for (GData4 g : selectedQuads) {
            allColours.add(new GColour(g.colourNumber, g.r, g.g, g.b, g.a));
        }
        for (GData5 g : selectedCondlines) {
            allColours.add(new GColour(g.colourNumber, g.r, g.g, g.b, g.a));
        }

        clearSelection();

        if (ss.isVertices() && ss.isLines() && ss.isTriangles() && ss.isQuads() && ss.isCondlines()) {
            for (GData1 g : vertexCountInSubfile.keySet()) {
                if (!includeHidden && hiddenData.contains(g)) continue;
                if (allColours.contains(new GColour(g.colourNumber, g.r, g.g, g.b, g.a))) {
                    effSelectedSubfiles.add(g);
                }
            }

            for (GData1 subf : effSelectedSubfiles) {
                selectedSubfiles.add(subf);
                Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
                for (VertexInfo vertexInfo : vis) {
                    selectedVertices.add(vertexInfo.getVertex());
                    GData g = vertexInfo.getLinkedData();
                    if (!includeHidden && hiddenData.contains(g)) continue;
                    switch (g.type()) {
                    case 2:
                        effSelectedLines.add((GData2) g);
                        break;
                    case 3:
                        effSelectedTriangles.add((GData3) g);
                        break;
                    case 4:
                        effSelectedQuads.add((GData4) g);
                        break;
                    case 5:
                        effSelectedCondlines.add((GData5) g);
                        break;
                    default:
                        break;
                    }
                }
            }
        }

        if (ss.isLines()) {
            for (GData2 g : lines.keySet()) {
                if (!includeHidden && hiddenData.contains(g)) continue;
                if (allColours.contains(new GColour(g.colourNumber, g.r, g.g, g.b, g.a))) {
                    effSelectedLines.add(g);
                }
            }
        }
        if (ss.isTriangles()) {
            for (GData3 g : triangles.keySet()) {
                if (!includeHidden && hiddenData.contains(g)) continue;
                if (allColours.contains(new GColour(g.colourNumber, g.r, g.g, g.b, g.a))) {
                    effSelectedTriangles.add(g);
                }
            }
        }
        if (ss.isQuads()) {
            for (GData4 g : quads.keySet()) {
                if (!includeHidden && hiddenData.contains(g)) continue;
                if (allColours.contains(new GColour(g.colourNumber, g.r, g.g, g.b, g.a))) {
                    effSelectedQuads.add(g);
                }
            }
        }
        if (ss.isCondlines()) {
            for (GData5 g : condlines.keySet()) {
                if (!includeHidden && hiddenData.contains(g)) continue;
                if (allColours.contains(new GColour(g.colourNumber, g.r, g.g, g.b, g.a))) {
                    effSelectedCondlines.add(g);
                }
            }
        }

        selectedLines.addAll(effSelectedLines);
        selectedTriangles.addAll(effSelectedTriangles);
        selectedQuads.addAll(effSelectedQuads);
        selectedCondlines.addAll(effSelectedCondlines);
        selectedSubfiles.addAll(effSelectedSubfiles);

        selectedData.addAll(selectedLines);
        selectedData.addAll(selectedTriangles);
        selectedData.addAll(selectedQuads);
        selectedData.addAll(selectedCondlines);
        selectedData.addAll(selectedSubfiles);
    }

    public void selector(final SelectorSettings ss) {

        linkedDatFile.setDrawSelection(false);
        try
        {
            new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, false, new IRunnableWithProgress()
            {
                @Override
                public void run(final IProgressMonitor m) throws InvocationTargetException, InterruptedException
                {
                    m.beginTask("Selecting...", IProgressMonitor.UNKNOWN); //$NON-NLS-1$ I18N

                    final Set<GColour> allColours = new HashSet<GColour>();
                    final Set<Vector3d> allNormals = new HashSet<Vector3d>();
                    final TreeMap<Vertex, TreeSet<Vertex>> adjaencyByPrecision = new TreeMap<Vertex, TreeSet<Vertex>>();

                    // Get near vertices

                    if (ss.isDistance() && ss.getEqualDistance().compareTo(BigDecimal.ZERO) != 0) {
                        final BigDecimal ds = ss.getEqualDistance().multiply(ss.getEqualDistance());
                        int i = 0;
                        int j = 0;
                        for (Vertex v1 : vertexLinkedToPositionInFile.keySet()) {
                            TreeSet<Vertex> newSet = new TreeSet<Vertex>();
                            newSet.add(v1);
                            for (Vertex v2 : vertexLinkedToPositionInFile.keySet()) {
                                if (j > i) {
                                    Vector3d v3d1 = new Vector3d(v1);
                                    Vector3d v3d2 = new Vector3d(v2);
                                    if (Vector3d.distSquare(v3d1, v3d2).compareTo(ds) < 0) {
                                        newSet.add(v2);
                                    }
                                }
                                j++;
                            }
                            adjaencyByPrecision.put(v1, newSet);
                            i++;
                        }
                    } else {
                        for (Vertex v1 : vertexLinkedToPositionInFile.keySet()) {
                            TreeSet<Vertex> newSet = new TreeSet<Vertex>();
                            newSet.add(v1);
                            adjaencyByPrecision.put(v1, newSet);
                        }
                    }

                    // Get selected colour set

                    if (ss.isColour()) {
                        for (GData1 g : selectedSubfiles) {
                            allColours.add(new GColour(g.colourNumber, g.r, g.g, g.b, g.a));
                        }
                        for (GData2 g : selectedLines) {
                            allColours.add(new GColour(g.colourNumber, g.r, g.g, g.b, g.a));
                        }
                        for (GData3 g : selectedTriangles) {
                            allColours.add(new GColour(g.colourNumber, g.r, g.g, g.b, g.a));
                        }
                        for (GData4 g : selectedQuads) {
                            allColours.add(new GColour(g.colourNumber, g.r, g.g, g.b, g.a));
                        }
                        for (GData5 g : selectedCondlines) {
                            allColours.add(new GColour(g.colourNumber, g.r, g.g, g.b, g.a));
                        }
                    }

                    // Prepare normals for orientation check

                    if (ss.isOrientation()) {

                        fillVertexNormalCache(linkedDatFile.getDrawChainStart());

                        if (ss.getScope() == SelectorSettings.EVERYTHING) {
                            for (GData3 g : selectedTriangles) {
                                if (ss.isNoSubfiles() && !lineLinkedToVertices.containsKey(g)) continue;
                                if (dataLinkedToNormalCACHE.containsKey(g)) {
                                    float[] n = dataLinkedToNormalCACHE.get(g);
                                    allNormals.add(new Vector3d(new BigDecimal(n[0]), new BigDecimal(n[1]), new BigDecimal(n[2])));
                                } else {
                                    Vector4f n = new Vector4f(g.xn, g.yn, g.zn, 1f);
                                    Matrix4f.transform(g.parent.productMatrix, n, n);
                                    allNormals.add(new Vector3d(new BigDecimal(g.xn), new BigDecimal(g.yn), new BigDecimal(g.zn)));
                                }
                            }
                            for (GData4 g : selectedQuads) {
                                if (ss.isNoSubfiles() && !lineLinkedToVertices.containsKey(g)) continue;
                                if (dataLinkedToNormalCACHE.containsKey(g)) {
                                    float[] n = dataLinkedToNormalCACHE.get(g);
                                    allNormals.add(new Vector3d(new BigDecimal(n[0]), new BigDecimal(n[1]), new BigDecimal(n[2])));
                                } else {
                                    Vector4f n = new Vector4f(g.xn, g.yn, g.zn, 1f);
                                    Matrix4f.transform(g.parent.productMatrix, n, n);
                                    allNormals.add(new Vector3d(new BigDecimal(g.xn), new BigDecimal(g.yn), new BigDecimal(g.zn)));
                                }
                            }
                        } else {

                        }
                    }

                    switch (ss.getScope()) {
                    case SelectorSettings.EVERYTHING:
                    {
                        double angle = Math.min(Math.abs(ss.getAngle().doubleValue()), 180.0 - Math.abs(ss.getAngle().doubleValue()));

                        clearSelection2();

                        if (!ss.isOrientation()) {
                            for (GData1 g : vertexCountInSubfile.keySet()) {
                                if (canSelect(null, g, ss, allNormals, allColours, angle)) {
                                    selectedSubfiles.add(g);
                                    selectedData.add(g);
                                }
                            }
                            for (GData2 g : lines.keySet()) {
                                if (canSelect(null, g, ss, allNormals, allColours, angle)) {
                                    selectedLines.add(g);
                                    selectedData.add(g);
                                }
                            }
                            for (GData5 g : condlines.keySet()) {
                                if (canSelect(null, g, ss, allNormals, allColours, angle)) {
                                    selectedCondlines.add(g);
                                    selectedData.add(g);
                                }
                            }
                        }
                        for (GData3 g : triangles.keySet()) {
                            if (canSelect(null, g, ss, allNormals, allColours, angle)) {
                                selectedTriangles.add(g);
                                selectedData.add(g);
                            }
                        }
                        for (GData4 g : quads.keySet()) {
                            if (canSelect(null, g, ss, allNormals, allColours, angle)) {
                                selectedQuads.add(g);
                                selectedData.add(g);
                            }
                        }
                        break;
                    }
                    case SelectorSettings.TOUCHING:
                    case SelectorSettings.CONNECTED:

                        selectedVertices.clear();

                        double angle2 = ss.getAngle().doubleValue();

                        final Set<GData2> lastSelectedLines = new HashSet<GData2>();
                        final Set<GData3> lastSelectedTriangles = new HashSet<GData3>();
                        final Set<GData4> lastSelectedQuads = new HashSet<GData4>();
                        final Set<GData5> lastSelectedCondlines = new HashSet<GData5>();

                        final Set<GData2> newSelectedLines = new HashSet<GData2>();
                        final Set<GData3> newSelectedTriangles = new HashSet<GData3>();
                        final Set<GData4> newSelectedQuads = new HashSet<GData4>();
                        final Set<GData5> newSelectedCondlines = new HashSet<GData5>();

                        final Set<Vertex> addedSelectedVertices = new TreeSet<Vertex>();
                        final Set<GData2> addedSelectedLines = new HashSet<GData2>();
                        final Set<GData3> addedSelectedTriangles = new HashSet<GData3>();
                        final Set<GData4> addedSelectedQuads = new HashSet<GData4>();
                        final Set<GData5> addedSelectedCondlines = new HashSet<GData5>();

                        if (ss.isEdgeStop()) {
                            selectedData.clear();
                            selectedLines.clear();
                            selectedCondlines.clear();
                            selectedSubfiles.clear();

                            // Iterative Selection Spread I (Stops at edges)

                            // Init (Step 0)
                            // Filter invalid selection start (e.g. subfile content), since the selection wont be cleared
                            for (GData3 g : selectedTriangles) {
                                if (!canSelect(null, g, ss, allNormals, allColours, angle2)) {
                                    selectedTriangles.remove(g);
                                    selectedData.remove(g);
                                }
                            }
                            for (GData4 g : selectedQuads) {
                                if (!canSelect(null, g, ss, allNormals, allColours, angle2)) {
                                    selectedQuads.remove(g);
                                    selectedData.remove(g);
                                }
                            }

                            int c2 = selectedTriangles.size();
                            int c3 = selectedQuads.size();

                            lastSelectedTriangles.addAll(selectedTriangles);
                            lastSelectedQuads.addAll(selectedQuads);

                            addedSelectedTriangles.addAll(selectedTriangles);
                            addedSelectedQuads.addAll(selectedQuads);
                            addedSelectedVertices.addAll(selectedVertices);

                            // Interation, Step (1...n), "Select Touching" is only one step
                            do {
                                c2 = addedSelectedTriangles.size();
                                c3 = addedSelectedQuads.size();
                                newSelectedTriangles.clear();
                                newSelectedQuads.clear();

                                HashSet<AccurateEdge> touchingEdges = new HashSet<AccurateEdge>();
                                {
                                    Vertex[] verts;
                                    for (GData3 g : lastSelectedTriangles) {
                                        verts = triangles.get(g);
                                        for (Vertex v : verts) {
                                            TreeSet<Vertex> verts2 = adjaencyByPrecision.get(v);
                                            for (Vertex v2 : verts2) {
                                                addedSelectedVertices.add(v2);
                                            }
                                        }
                                        touchingEdges.add(new AccurateEdge(verts[0], verts[1]));
                                        touchingEdges.add(new AccurateEdge(verts[1], verts[2]));
                                        touchingEdges.add(new AccurateEdge(verts[2], verts[0]));
                                    }
                                    for (GData4 g : lastSelectedQuads) {
                                        verts = quads.get(g);
                                        for (Vertex v : verts) {
                                            TreeSet<Vertex> verts2 = adjaencyByPrecision.get(v);
                                            for (Vertex v2 : verts2) {
                                                addedSelectedVertices.add(v2);
                                            }
                                        }
                                        touchingEdges.add(new AccurateEdge(verts[0], verts[1]));
                                        touchingEdges.add(new AccurateEdge(verts[1], verts[2]));
                                        touchingEdges.add(new AccurateEdge(verts[2], verts[3]));
                                        touchingEdges.add(new AccurateEdge(verts[3], verts[0]));
                                    }
                                }
                                for (AccurateEdge edge : touchingEdges) {
                                    boolean skipEdge = false;
                                    for (GData2 line : lines.keySet()) {
                                        if (hasSameEdge(edge, line, adjaencyByPrecision)) {
                                            skipEdge = true;
                                            break;
                                        }
                                    }
                                    if (!skipEdge) {
                                        for (GData3 g : triangles.keySet()) {
                                            if (!addedSelectedTriangles.contains(g) && canSelect(null, g, ss, allNormals, allColours, angle2) && hasSameEdge(edge, g, adjaencyByPrecision)) {
                                                if (ss.isOrientation()) {
                                                    int faceCount = 0;
                                                    int angleCount = 0;
                                                    boolean notAdjacent = true;
                                                    for (GData3 g2 : addedSelectedTriangles) {
                                                        if (hasSameEdge(g, g2, adjaencyByPrecision)) {
                                                            faceCount++;
                                                            if (canSelect(g2, g, ss, allNormals, allColours, angle2)) {
                                                                angleCount++;
                                                                notAdjacent = false;
                                                            }
                                                        }
                                                    }
                                                    for (GData4 g2 : addedSelectedQuads) {
                                                        if (hasSameEdge(g, g2, adjaencyByPrecision)) {
                                                            faceCount++;
                                                            if (canSelect(g2, g, ss, allNormals, allColours, angle2)) {
                                                                angleCount++;
                                                                notAdjacent = false;
                                                            }
                                                        }
                                                    }
                                                    if (notAdjacent || faceCount != angleCount) break;
                                                }
                                                addedSelectedTriangles.add(g);
                                                newSelectedTriangles.add(g);
                                                Vertex[] verts = triangles.get(g);
                                                for (Vertex v : verts) {
                                                    TreeSet<Vertex> verts2 = adjaencyByPrecision.get(v);
                                                    for (Vertex v2 : verts2) {
                                                        addedSelectedVertices.add(v2);
                                                    }
                                                }
                                            }
                                        }
                                        for (GData4 g : quads.keySet()) {
                                            if (!addedSelectedQuads.contains(g) && canSelect(null, g, ss, allNormals, allColours, angle2) && hasSameEdge(edge, g, adjaencyByPrecision)) {
                                                if (ss.isOrientation()) {
                                                    int faceCount = 0;
                                                    int angleCount = 0;
                                                    boolean notAdjacent = true;
                                                    for (GData3 g2 : addedSelectedTriangles) {
                                                        if (hasSameEdge(g, g2, adjaencyByPrecision)) {
                                                            faceCount++;
                                                            if (canSelect(g2, g, ss, allNormals, allColours, angle2)) {
                                                                angleCount++;
                                                                notAdjacent = false;
                                                            }
                                                        }
                                                    }
                                                    for (GData4 g2 : addedSelectedQuads) {
                                                        if (hasSameEdge(g, g2, adjaencyByPrecision)) {
                                                            faceCount++;
                                                            if (canSelect(g2, g, ss, allNormals, allColours, angle2)) {
                                                                angleCount++;
                                                                notAdjacent = false;
                                                            }
                                                        }
                                                    }
                                                    if (notAdjacent || faceCount != angleCount) break;
                                                }
                                                addedSelectedQuads.add(g);
                                                newSelectedQuads.add(g);
                                                Vertex[] verts = quads.get(g);
                                                for (Vertex v : verts) {
                                                    TreeSet<Vertex> verts2 = adjaencyByPrecision.get(v);
                                                    for (Vertex v2 : verts2) {
                                                        addedSelectedVertices.add(v2);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                lastSelectedTriangles.clear();
                                lastSelectedQuads.clear();
                                lastSelectedTriangles.addAll(newSelectedTriangles);
                                lastSelectedQuads.addAll(newSelectedQuads);

                                if (ss.getScope() == SelectorSettings.TOUCHING) break;
                            } while (
                                    c2 != addedSelectedTriangles.size() ||
                                    c3 != addedSelectedQuads.size());

                            if (ss.isVertices()) {
                                selectedVertices.addAll(addedSelectedVertices);
                            }
                            if (ss.isTriangles()) {
                                selectedTriangles.addAll(addedSelectedTriangles);
                                selectedData.addAll(selectedTriangles);
                            }
                            if (ss.isQuads()) {
                                selectedQuads.addAll(addedSelectedQuads);
                                selectedData.addAll(selectedQuads);
                            }

                            // Now add lines and condlines
                            if (ss.isLines()) {
                                for (GData2 g : lines.keySet()) {
                                    if (canSelect(null, g, ss, allNormals, allColours, angle2)) {
                                        Vertex[] verts = lines.get(g);
                                        if (addedSelectedVertices.contains(verts[0]) && addedSelectedVertices.contains(verts[1])) {
                                            selectedLines.add(g);
                                        }
                                    }
                                }
                                selectedData.addAll(selectedLines);
                            }
                            if (ss.isCondlines()) {
                                for (GData5 g : condlines.keySet()) {
                                    if (canSelect(null, g, ss, allNormals, allColours, angle2)) {
                                        Vertex[] verts = condlines.get(g);
                                        if (addedSelectedVertices.contains(verts[0]) && addedSelectedVertices.contains(verts[1])) {
                                            selectedCondlines.add(g);
                                        }
                                    }
                                }
                                selectedData.addAll(selectedCondlines);
                            }

                        } else {

                            // Iterative Selection Spread II

                            // Init (Step 0)
                            // Filter invalid selection start (e.g. subfile content), since the selection wont be cleared
                            for (GData1 g : selectedSubfiles) {
                                if (!canSelect(null, g, ss, allNormals, allColours, angle2)) {
                                    selectedSubfiles.remove(g);
                                    selectedData.remove(g);
                                }
                            }
                            for (GData2 g : selectedLines) {
                                if (!canSelect(null, g, ss, allNormals, allColours, angle2)) {
                                    selectedLines.remove(g);
                                    selectedData.remove(g);
                                }
                            }
                            for (GData3 g : selectedTriangles) {
                                if (!canSelect(null, g, ss, allNormals, allColours, angle2)) {
                                    selectedTriangles.remove(g);
                                    selectedData.remove(g);
                                }
                            }
                            for (GData4 g : selectedQuads) {
                                if (!canSelect(null, g, ss, allNormals, allColours, angle2)) {
                                    selectedQuads.remove(g);
                                    selectedData.remove(g);
                                }
                            }
                            for (GData5 g : selectedCondlines) {
                                if (!canSelect(null, g, ss, allNormals, allColours, angle2)) {
                                    selectedCondlines.remove(g);
                                    selectedData.remove(g);
                                }
                            }

                            int c1 = selectedLines.size();
                            int c2 = selectedTriangles.size();
                            int c3 = selectedQuads.size();
                            int c4 = selectedCondlines.size();

                            lastSelectedLines.addAll(selectedLines);
                            lastSelectedTriangles.addAll(selectedTriangles);
                            lastSelectedQuads.addAll(selectedQuads);
                            lastSelectedCondlines.addAll(selectedCondlines);

                            if (!ss.isVertices()) selectedVertices.clear();

                            addedSelectedLines.addAll(selectedLines);
                            addedSelectedTriangles.addAll(selectedTriangles);
                            addedSelectedQuads.addAll(selectedQuads);
                            addedSelectedCondlines.addAll(selectedCondlines);
                            addedSelectedVertices.addAll(selectedVertices);

                            // Interation, Step (1...n), "Select Touching" is only one step
                            do {
                                c1 = addedSelectedLines.size();
                                c2 = addedSelectedTriangles.size();
                                c3 = addedSelectedQuads.size();
                                c4 = addedSelectedCondlines.size();
                                newSelectedLines.clear();
                                newSelectedTriangles.clear();
                                newSelectedQuads.clear();
                                newSelectedCondlines.clear();

                                TreeSet<Vertex> touchingVertices = new TreeSet<Vertex>();
                                {
                                    Vertex[] verts;
                                    for (GData2 g : lastSelectedLines) {
                                        verts = lines.get(g);
                                        for (Vertex v : verts) {
                                            TreeSet<Vertex> verts2 = adjaencyByPrecision.get(v);
                                            for (Vertex v2 : verts2) {
                                                touchingVertices.add(v2);
                                                addedSelectedVertices.add(v2);
                                            }
                                        }
                                    }
                                    for (GData3 g : lastSelectedTriangles) {
                                        verts = triangles.get(g);
                                        for (Vertex v : verts) {
                                            TreeSet<Vertex> verts2 = adjaencyByPrecision.get(v);
                                            for (Vertex v2 : verts2) {
                                                touchingVertices.add(v2);
                                                addedSelectedVertices.add(v2);
                                            }
                                        }
                                    }
                                    for (GData4 g : lastSelectedQuads) {
                                        verts = quads.get(g);
                                        for (Vertex v : verts) {
                                            TreeSet<Vertex> verts2 = adjaencyByPrecision.get(v);
                                            for (Vertex v2 : verts2) {
                                                touchingVertices.add(v2);
                                                addedSelectedVertices.add(v2);
                                            }
                                        }
                                    }
                                    for (GData5 g : lastSelectedCondlines) {
                                        verts = condlines.get(g);
                                        int c = 0;
                                        for (Vertex v : verts) {
                                            if (c > 1) break;
                                            TreeSet<Vertex> verts2 = adjaencyByPrecision.get(v);
                                            for (Vertex v2 : verts2) {
                                                touchingVertices.add(v2);
                                                addedSelectedVertices.add(v2);
                                            }
                                            c++;
                                        }
                                    }
                                }
                                for (Vertex v : touchingVertices) {
                                    Set<VertexManifestation> manis = vertexLinkedToPositionInFile.get(v);
                                    for (VertexManifestation mani : manis) {
                                        GData g = mani.getGdata();
                                        switch (g.type()) {
                                        case 2:
                                            if (!addedSelectedLines.contains(g) &&  canSelect(null, g, ss, allNormals, allColours, angle2)) {
                                                addedSelectedLines.add((GData2) g);
                                                newSelectedLines.add((GData2) g);
                                                Vertex[] verts = lines.get(g);
                                                for (Vertex ov : verts) {
                                                    TreeSet<Vertex> verts2 = adjaencyByPrecision.get(ov);
                                                    for (Vertex v2 : verts2) {
                                                        addedSelectedVertices.add(v2);
                                                    }
                                                }
                                            }
                                            break;
                                        case 3:

                                            if (!addedSelectedTriangles.contains(g) && canSelect(null, g, ss, allNormals, allColours, angle2)) {

                                                if (ss.isOrientation()) {
                                                    // We have to find a selected face, which shares one edge
                                                    int faceCount = 0;
                                                    int angleCount = 0;
                                                    boolean notAdjacent = true;
                                                    for (GData3 g2 : addedSelectedTriangles) {
                                                        if (hasSameEdge(g, g2, adjaencyByPrecision)) {
                                                            faceCount++;
                                                            if (canSelect(g2, g, ss, allNormals, allColours, angle2)) {
                                                                angleCount++;
                                                                notAdjacent = false;
                                                            }
                                                        }
                                                    }
                                                    for (GData4 g2 : addedSelectedQuads) {
                                                        if (hasSameEdge(g, g2, adjaencyByPrecision)) {
                                                            faceCount++;
                                                            if (canSelect(g2, g, ss, allNormals, allColours, angle2)) {
                                                                angleCount++;
                                                                notAdjacent = false;
                                                            }
                                                        }
                                                    }
                                                    if (notAdjacent || faceCount != angleCount) break;
                                                }

                                                addedSelectedTriangles.add((GData3) g);
                                                newSelectedTriangles.add((GData3) g);
                                                Vertex[] verts = triangles.get(g);
                                                for (Vertex ov : verts) {
                                                    TreeSet<Vertex> verts2 = adjaencyByPrecision.get(ov);
                                                    for (Vertex v2 : verts2) {
                                                        addedSelectedVertices.add(v2);
                                                    }
                                                }
                                            }
                                            break;
                                        case 4:
                                            if (!addedSelectedQuads.contains(g) && canSelect(null, g, ss, allNormals, allColours, angle2)) {

                                                if (ss.isOrientation()) {
                                                    // We have to find a selected face, which shares one edge
                                                    int faceCount = 0;
                                                    int angleCount = 0;
                                                    boolean notAdjacent = true;
                                                    for (GData3 g2 : addedSelectedTriangles) {
                                                        if (hasSameEdge(g, g2, adjaencyByPrecision)) {
                                                            faceCount++;
                                                            if (canSelect(g2, g, ss, allNormals, allColours, angle2)) {
                                                                angleCount++;
                                                            }
                                                        }
                                                    }
                                                    for (GData4 g2 : addedSelectedQuads) {
                                                        if (hasSameEdge(g, g2, adjaencyByPrecision)) {
                                                            faceCount++;
                                                            if (canSelect(g2, g, ss, allNormals, allColours, angle2)) {
                                                                angleCount++;
                                                            }
                                                        }
                                                    }
                                                    if (notAdjacent || faceCount != angleCount) break;
                                                }


                                                addedSelectedQuads.add((GData4) g);
                                                newSelectedQuads.add((GData4) g);
                                                Vertex[] verts = quads.get(g);
                                                for (Vertex ov : verts) {
                                                    TreeSet<Vertex> verts2 = adjaencyByPrecision.get(ov);
                                                    for (Vertex v2 : verts2) {
                                                        addedSelectedVertices.add(v2);
                                                    }
                                                }
                                            }
                                            break;
                                        case 5:
                                            if (!addedSelectedCondlines.contains(g) && canSelect(null, g, ss, allNormals, allColours, angle2)) {
                                                addedSelectedCondlines.add((GData5) g);
                                                newSelectedCondlines.add((GData5) g);
                                                Vertex[] verts = condlines.get(g);
                                                int c = 0;
                                                for (Vertex ov : verts) {
                                                    if (c > 1) break;
                                                    TreeSet<Vertex> verts2 = adjaencyByPrecision.get(ov);
                                                    for (Vertex v2 : verts2) {
                                                        addedSelectedVertices.add(v2);
                                                    }
                                                    c++;
                                                }
                                            }
                                            break;
                                        default:
                                            break;
                                        }

                                    }
                                }

                                lastSelectedLines.clear();
                                lastSelectedTriangles.clear();
                                lastSelectedQuads.clear();
                                lastSelectedCondlines.clear();
                                lastSelectedLines.addAll(newSelectedLines);
                                lastSelectedTriangles.addAll(newSelectedTriangles);
                                lastSelectedQuads.addAll(newSelectedQuads);
                                lastSelectedCondlines.addAll(newSelectedCondlines);

                                if (ss.getScope() == SelectorSettings.TOUCHING) break;
                            } while (
                                    c1 != addedSelectedLines.size() ||
                                    c2 != addedSelectedTriangles.size() ||
                                    c3 != addedSelectedQuads.size() ||
                                    c4 != addedSelectedCondlines.size());

                            if (ss.isVertices()) {
                                selectedVertices.addAll(addedSelectedVertices);
                            }

                            if (ss.isLines()) {
                                selectedLines.addAll(addedSelectedLines);
                                selectedData.addAll(selectedLines);
                            }

                            if (ss.isTriangles()) {
                                selectedTriangles.addAll(addedSelectedTriangles);
                                selectedData.addAll(selectedTriangles);
                            }

                            if (ss.isQuads()) {
                                selectedQuads.addAll(addedSelectedQuads);
                                selectedData.addAll(selectedQuads);
                            }

                            if (ss.isCondlines()) {
                                selectedCondlines.addAll(addedSelectedCondlines);
                                selectedData.addAll(selectedCondlines);
                            }

                        }

                        break;
                    default:
                        break;
                    }

                    clearVertexNormalCache();

                    // Extend selection to whole subfiles
                    if (ss.isWholeSubfiles() && !ss.isNoSubfiles()) {
                        selectSubfiles(null, null, false);
                    }
                }
            });
        }catch (InvocationTargetException consumed) {
        } catch (InterruptedException consumed) {
        }
        linkedDatFile.setDrawSelection(true);
    }

    private boolean canSelect(GData adjacentTo, GData what, SelectorSettings ss, Set<Vector3d> allNormals, Set<GColour> allColours, double angle) {

        if (ss.isColour()) {
            GColour myColour;
            switch (what.type()) {
            case 1:
                GData1 g1 = (GData1) what;
                myColour = new GColour(g1.colourNumber, g1.r, g1.g, g1.b, g1.a);
                break;
            case 2:
                GData2 g2 = (GData2) what;
                myColour = new GColour(g2.colourNumber, g2.r, g2.g, g2.b, g2.a);
                if (!ss.isLines()) return false;
                break;
            case 3:
                GData3 g3 = (GData3) what;
                myColour = new GColour(g3.colourNumber, g3.r, g3.g, g3.b, g3.a);
                if (!ss.isTriangles()) return false;
                break;
            case 4:
                GData4 g4 = (GData4) what;
                myColour = new GColour(g4.colourNumber, g4.r, g4.g, g4.b, g4.a);
                if (!ss.isQuads()) return false;
                break;
            case 5:
                GData5 g5 = (GData5) what;
                myColour = new GColour(g5.colourNumber, g5.r, g5.g, g5.b, g5.a);
                if (!ss.isCondlines()) return false;
                break;
            default:
                return false;
            }
            // Check Colour
            if (!allColours.contains(myColour)) {
                return false;
            }
        }

        if (ss.isOrientation()) {
            if (adjacentTo == null) {
                // SelectorSettings.EVERYTHING

                // Check normal orientation
                switch (what.type()) {
                case 3:
                {
                    if (!allNormals.isEmpty()) {
                        Vector3d n1;
                        if (dataLinkedToNormalCACHE.containsKey(what)) {
                            float[] n = dataLinkedToNormalCACHE.get(what);
                            n1 = new Vector3d(new BigDecimal(n[0]), new BigDecimal(n[1]), new BigDecimal(n[2]));
                        } else {
                            GData3 g = (GData3) what;
                            n1 = new Vector3d(new BigDecimal(g.xn), new BigDecimal(g.yn), new BigDecimal(g.zn));
                        }
                        int falseCounter = 0;
                        for (Vector3d n2 : allNormals) {
                            double angle2 = Vector3d.angle(n1, n2);
                            if (angle2 > 90.0) angle2 = 180.0 - angle2;
                            if (angle2 > angle) {
                                falseCounter++;
                            }
                        }
                        if (falseCounter == allNormals.size()) {
                            return false;
                        }
                    }
                }
                break;
                case 4:
                {
                    if (!allNormals.isEmpty()) {
                        Vector3d n1;
                        if (dataLinkedToNormalCACHE.containsKey(what)) {
                            float[] n = dataLinkedToNormalCACHE.get(what);
                            n1 = new Vector3d(new BigDecimal(n[0]), new BigDecimal(n[1]), new BigDecimal(n[2]));
                        } else {
                            GData4 g = (GData4) what;
                            n1 = new Vector3d(new BigDecimal(g.xn), new BigDecimal(g.yn), new BigDecimal(g.zn));
                        }
                        int falseCounter = 0;
                        for (Vector3d n2 : allNormals) {
                            double angle2 = Vector3d.angle(n1, n2);
                            if (angle2 > 90.0) angle2 = 180.0 - angle2;
                            if (angle2 > angle) {
                                falseCounter++;
                            }
                        }
                        if (falseCounter == allNormals.size()) {
                            return false;
                        }
                    }
                }
                break;
                default:
                    // Check subfile content
                    return !(ss.isNoSubfiles() && (!lineLinkedToVertices.containsKey(what) || what.type() == 1) || !(ss.isHidden() ^ !hiddenData.contains(what)));
                }
            } else {

                // Check normal orientation
                boolean noBFC = angle < 0.0;
                switch (what.type()) {
                case 3:
                {
                    Vector3d n1;
                    if (dataLinkedToNormalCACHE.containsKey(what)) {
                        float[] n = dataLinkedToNormalCACHE.get(what);
                        n1 = new Vector3d(new BigDecimal(n[0]), new BigDecimal(n[1]), new BigDecimal(n[2]));
                    } else {
                        GData3 g = (GData3) what;
                        n1 = new Vector3d(new BigDecimal(g.xn), new BigDecimal(g.yn), new BigDecimal(g.zn));
                        noBFC = true;
                    }
                    switch (adjacentTo.type()) {
                    case 3:
                    {
                        Vector3d n2;
                        if (dataLinkedToNormalCACHE.containsKey(adjacentTo)) {
                            float[] n = dataLinkedToNormalCACHE.get(adjacentTo);
                            n2 = new Vector3d(new BigDecimal(n[0]), new BigDecimal(n[1]), new BigDecimal(n[2]));
                        } else {
                            GData3 g = (GData3) adjacentTo;
                            n2 = new Vector3d(new BigDecimal(g.xn), new BigDecimal(g.yn), new BigDecimal(g.zn));
                            noBFC = true;
                        }
                        double angle2 = Vector3d.angle(n1, n2);
                        if (noBFC && angle2 > 90.0) angle2 = 180.0 - angle2;
                        if (angle2 > Math.abs(angle)) {
                            return false;
                        }
                    }
                    break;
                    case 4:
                    {
                        Vector3d n2;
                        if (dataLinkedToNormalCACHE.containsKey(adjacentTo)) {
                            float[] n = dataLinkedToNormalCACHE.get(adjacentTo);
                            n2 = new Vector3d(new BigDecimal(n[0]), new BigDecimal(n[1]), new BigDecimal(n[2]));
                        } else {
                            GData4 g = (GData4) adjacentTo;
                            n2 = new Vector3d(new BigDecimal(g.xn), new BigDecimal(g.yn), new BigDecimal(g.zn));
                            noBFC = true;
                        }
                        double angle2 = Vector3d.angle(n1, n2);
                        if (noBFC && angle2 > 90.0) angle2 = 180.0 - angle2;
                        if (angle2 > Math.abs(angle)) {
                            return false;
                        }
                    }
                    break;
                    default:
                        // Check subfile content
                        return !(ss.isNoSubfiles() && (!lineLinkedToVertices.containsKey(what) || what.type() == 1) || !(ss.isHidden() ^ !hiddenData.contains(what)));
                    }
                }
                break;
                case 4:
                {
                    Vector3d n1;
                    if (dataLinkedToNormalCACHE.containsKey(what)) {
                        float[] n = dataLinkedToNormalCACHE.get(what);
                        n1 = new Vector3d(new BigDecimal(n[0]), new BigDecimal(n[1]), new BigDecimal(n[2]));
                    } else {
                        GData4 g = (GData4) what;
                        n1 = new Vector3d(new BigDecimal(g.xn), new BigDecimal(g.yn), new BigDecimal(g.zn));
                        noBFC = true;
                    }

                    switch (adjacentTo.type()) {
                    case 3:
                    {
                        Vector3d n2;
                        if (dataLinkedToNormalCACHE.containsKey(adjacentTo)) {
                            float[] n = dataLinkedToNormalCACHE.get(adjacentTo);
                            n2 = new Vector3d(new BigDecimal(n[0]), new BigDecimal(n[1]), new BigDecimal(n[2]));
                        } else {
                            GData3 g = (GData3) adjacentTo;
                            n2 = new Vector3d(new BigDecimal(g.xn), new BigDecimal(g.yn), new BigDecimal(g.zn));
                            noBFC = true;
                        }
                        double angle2 = Vector3d.angle(n1, n2);
                        if (noBFC && angle2 > 90.0) angle2 = 180.0 - angle2;
                        if (angle2 > angle) {
                            return false;
                        }
                    }
                    break;
                    case 4:
                    {
                        Vector3d n2;
                        if (dataLinkedToNormalCACHE.containsKey(adjacentTo)) {
                            float[] n = dataLinkedToNormalCACHE.get(adjacentTo);
                            n2 = new Vector3d(new BigDecimal(n[0]), new BigDecimal(n[1]), new BigDecimal(n[2]));
                        } else {
                            GData4 g = (GData4) adjacentTo;
                            n2 = new Vector3d(new BigDecimal(g.xn), new BigDecimal(g.yn), new BigDecimal(g.zn));
                            noBFC = true;
                        }
                        double angle2 = Vector3d.angle(n1, n2);
                        if (noBFC && angle2 > 90.0) angle2 = 180.0 - angle2;
                        if (angle2 > angle) {
                            return false;
                        }
                    }
                    break;
                    default:
                        // Check subfile content
                        return !(ss.isNoSubfiles() && (!lineLinkedToVertices.containsKey(what) || what.type() == 1) || !(ss.isHidden() ^ !hiddenData.contains(what)));
                    }
                }
                break;
                default:
                    // Check subfile content
                    return !(ss.isNoSubfiles() && (!lineLinkedToVertices.containsKey(what) || what.type() == 1) || !(ss.isHidden() ^ !hiddenData.contains(what)));
                }
            }
        }
        // Check subfile content
        return !(ss.isNoSubfiles() && (!lineLinkedToVertices.containsKey(what) || what.type() == 1) || !(ss.isHidden() ^ !hiddenData.contains(what)));
    }

    public void selectIsolatedVertices() {
        clearSelection();
        for (Vertex v : vertexLinkedToPositionInFile.keySet()) {
            int vd = 0;
            for (VertexManifestation vm : vertexLinkedToPositionInFile.get(v)) {
                if (vm.getGdata().type() == 0) {
                    vd++;
                } else {
                    break;
                }
            }
            if (vd == vertexLinkedToPositionInFile.get(v).size()) {
                selectedVertices.add(v);
            }
        }
    }

    public void selectInverse(SelectorSettings sels) {

        final Set<Vertex> lastSelectedVertices = new TreeSet<Vertex>();
        final Set<GData1> lastSelectedSubfiles = new HashSet<GData1>();
        final Set<GData2> lastSelectedLines = new HashSet<GData2>();
        final Set<GData3> lastSelectedTriangles = new HashSet<GData3>();
        final Set<GData4> lastSelectedQuads = new HashSet<GData4>();
        final Set<GData5> lastSelectedCondlines = new HashSet<GData5>();

        lastSelectedVertices.addAll(selectedVertices);
        lastSelectedSubfiles.addAll(selectedSubfiles);
        lastSelectedLines.addAll(selectedLines);
        lastSelectedTriangles.addAll(selectedTriangles);
        lastSelectedQuads.addAll(selectedQuads);
        lastSelectedCondlines.addAll(selectedCondlines);

        clearSelection();

        if (sels.isVertices()) {
            for (Vertex v : vertexLinkedToPositionInFile.keySet()) {
                if (!hiddenVertices.contains(v) && !lastSelectedVertices.contains(v)) selectedVertices.add(v);
            }
        }
        if (sels.isVertices() && sels.isLines() && sels.isTriangles() && sels.isQuads() && sels.isCondlines()) {
            for (GData1 g : vertexCountInSubfile.keySet()) {
                if (!hiddenData.contains(g) && !lastSelectedSubfiles.contains(g)) selectedSubfiles.add(g);
            }
        }
        if (sels.isLines()) {
            for (GData2 g : lines.keySet()) {
                if (!hiddenData.contains(g) && !lastSelectedLines.contains(g)) selectedLines.add(g);
            }
        }
        if (sels.isTriangles()) {
            for (GData3 g : triangles.keySet()) {
                if (!hiddenData.contains(g) && !lastSelectedTriangles.contains(g)) selectedTriangles.add(g);
            }
        }
        if (sels.isQuads()) {
            for (GData4 g : quads.keySet()) {
                if (!hiddenData.contains(g) && !lastSelectedQuads.contains(g)) selectedQuads.add(g);
            }
        }
        if (sels.isCondlines()) {
            for (GData5 g : condlines.keySet()) {
                if (!hiddenData.contains(g) && !lastSelectedCondlines.contains(g)) selectedCondlines.add(g);
            }
        }

        selectedData.addAll(selectedLines);
        selectedData.addAll(selectedTriangles);
        selectedData.addAll(selectedQuads);
        selectedData.addAll(selectedCondlines);
        selectedData.addAll(selectedSubfiles);

    }

    public void subdivideCatmullClark() {

        if (linkedDatFile.isReadOnly()) return;

        // Backup selected surfaces
        HashSet<GData> surfsToParse = new HashSet<GData>();

        final Set<GData2> linesToDelete2 = new HashSet<GData2>();
        final Set<GData3> trisToDelete2 = new HashSet<GData3>();
        final Set<GData4> quadsToDelete2 = new HashSet<GData4>();

        final Set<GData2> newLines = new HashSet<GData2>();
        final Set<GData4> newQuads = new HashSet<GData4>();

        {
            for (GData3 g3 : selectedTriangles) {
                if (!lineLinkedToVertices.containsKey(g3)) continue;
                surfsToParse.add(g3);
                trisToDelete2.add(g3);
            }
            for (GData4 g4 : selectedQuads) {
                if (!lineLinkedToVertices.containsKey(g4)) continue;
                surfsToParse.add(g4);
                quadsToDelete2.add(g4);
            }
        }
        if (surfsToParse.isEmpty()) return;

        // Delete all condlines, since the are a PITA for subdivision

        clearSelection();
        selectedCondlines.addAll(condlines.keySet());
        selectedData.addAll(selectedCondlines);
        delete(false, false);

        TreeMap<Vertex, Vertex> newPoints = new TreeMap<Vertex, Vertex>();

        // Calculate new points
        for (Vertex v : vertexLinkedToPositionInFile.keySet()) {
            Set<VertexManifestation> manis = vertexLinkedToPositionInFile.get(v);
            HashSet<Vector3d> midEdge = new HashSet<Vector3d>();
            boolean keepIt = false;
            for (VertexManifestation m : manis) {
                GData gd = m.getGdata();
                switch (gd.type()) {
                case 0:
                    continue;
                case 2:
                    keepIt = true;
                    break;
                case 3:
                {
                    int p = m.getPosition();
                    Vertex[] verts = triangles.get(gd);
                    Vector3d vt = new Vector3d(v);
                    midEdge.add(Vector3d.add(vt, new Vector3d(verts[(p + 1) % 3])));
                    midEdge.add(Vector3d.add(vt, new Vector3d(verts[(p + 2) % 3])));
                }
                break;
                case 4:
                {
                    int p = m.getPosition();
                    Vertex[] verts = quads.get(gd);
                    Vector3d vt = new Vector3d(v);
                    midEdge.add(Vector3d.add(vt, new Vector3d(verts[(p + 1) % 4])));
                    midEdge.add(Vector3d.add(vt, new Vector3d(verts[(p + 3) % 4])));
                }
                break;
                default:
                    continue;
                }
                if (keepIt) break;
            }
            if (keepIt) {
                newPoints.put(v, v);
            } else {
                BigDecimal c = new BigDecimal(midEdge.size() * 2);
                Vector3d np = new Vector3d();
                for (Vector3d vd : midEdge) {
                    np = Vector3d.add(np, vd);
                }
                np.setX(np.X.divide(c, Threshold.mc));
                np.setY(np.Y.divide(c, Threshold.mc));
                np.setZ(np.Z.divide(c, Threshold.mc));
                newPoints.put(v, new Vertex(np));
            }
        }

        for (GData gd : surfsToParse) {
            Vertex[] originalVerts;
            int colourNumber;
            float r;
            float g;
            float b;
            float a;
            switch (gd.type()) {
            case 3:
                originalVerts = triangles.get(gd);
                GData3 g3 = (GData3) gd;
                colourNumber = g3.colourNumber; r = g3.r; g = g3.g; b = g3.b; a = g3.a;
                break;
            case 4:
                originalVerts = quads.get(gd);
                GData4 g4 = (GData4) gd;
                colourNumber = g4.colourNumber; r = g4.r; g = g4.g; b = g4.b; a = g4.a;
                break;
            default:
                continue;
            }

            final int c = originalVerts.length;

            BigDecimal c2 = new BigDecimal(c);
            Vector3d center = new Vector3d();
            for (Vertex vd : originalVerts) {
                center = Vector3d.add(center, new Vector3d(vd));
            }
            center.setX(center.X.divide(c2, Threshold.mc));
            center.setY(center.Y.divide(c2, Threshold.mc));
            center.setZ(center.Z.divide(c2, Threshold.mc));

            Vertex vc = new Vertex(center);

            Vertex[] ve = new Vertex[c];
            for(int i = 0; i < c; i++) {
                ve[i] = new Vertex(Vector3d.add(new Vector3d(originalVerts[i]), new Vector3d(originalVerts[(i + 1) % c])).scaledByHalf());
                GData2 g2 = null;
                if ((g2 = hasEdge(originalVerts[i], originalVerts[(i + 1) % c])) != null) {
                    if (!vertexLinkedToPositionInFile.containsKey(ve[i]) || hasEdge(originalVerts[i], ve[i]) == null) newLines.add(new GData2(24, View.line_Colour_r[0], View.line_Colour_g[0], View.line_Colour_b[0], 1f, originalVerts[i], ve[i], View.DUMMY_REFERENCE, linkedDatFile));
                    if (!vertexLinkedToPositionInFile.containsKey(ve[i]) || hasEdge(originalVerts[(i + 1) % c], ve[i]) == null) newLines.add(new GData2(24, View.line_Colour_r[0], View.line_Colour_g[0], View.line_Colour_b[0], 1f, originalVerts[(i + 1) % c], ve[i], View.DUMMY_REFERENCE, linkedDatFile));
                    linesToDelete2.add(g2);
                }
            }

            // Build quads
            for(int i = 0; i < c; i++) {
                newQuads.add(new GData4(colourNumber, r, g, b, a, newPoints.get(originalVerts[i]), ve[i], vc, ve[(c + i - 1) % c], View.DUMMY_REFERENCE, linkedDatFile));
            }

        }

        for (GData g : newLines) {
            linkedDatFile.addToTail(g);
        }
        for (GData g : newQuads) {
            linkedDatFile.addToTail(g);
        }

        selectedLines.addAll(linesToDelete2);
        selectedTriangles.addAll(trisToDelete2);
        selectedQuads.addAll(quadsToDelete2);
        selectedData.addAll(linesToDelete2);
        selectedData.addAll(trisToDelete2);
        selectedData.addAll(quadsToDelete2);
        delete(false, false);

        selectedLines.addAll(newLines);
        selectedData.addAll(newLines);
        selectedQuads.addAll(newQuads);
        selectedData.addAll(newQuads);
        roundSelection(6, 10, true, false);
        setModified(true, true);
        validateState();

    }

    public void subdivideLoop() {

        if (linkedDatFile.isReadOnly()) return;

        // Backup selected surfaces
        HashSet<GData3> surfsToParse = new HashSet<GData3>();

        final Set<GData2> linesToDelete2 = new HashSet<GData2>();
        final Set<GData3> trisToDelete2 = new HashSet<GData3>();

        final Set<GData2> newLines = new HashSet<GData2>();
        final Set<GData3> newTris = new HashSet<GData3>();

        for (GData3 g3 : selectedTriangles) {
            if (!lineLinkedToVertices.containsKey(g3)) continue;
            surfsToParse.add(g3);
            trisToDelete2.add(g3);
        }

        splitQuads(false);

        for (GData3 g3 : selectedTriangles) {
            if (!lineLinkedToVertices.containsKey(g3)) continue;
            surfsToParse.add(g3);
            trisToDelete2.add(g3);
        }

        if (surfsToParse.isEmpty()) return;

        // Delete all condlines, since the are a PITA for subdivision

        clearSelection();
        selectedCondlines.addAll(condlines.keySet());
        selectedData.addAll(selectedCondlines);
        delete(false, false);

        TreeMap<Vertex, Vertex> newPoints = new TreeMap<Vertex, Vertex>();

        for (GData3 g3 : surfsToParse) {
            Vertex[] originalVerts;
            int colourNumber;
            float r;
            float g;
            float b;
            float a;
            originalVerts = triangles.get(g3);
            colourNumber = g3.colourNumber; r = g3.r; g = g3.g; b = g3.b; a = g3.a;

            final int c = originalVerts.length;

            Vertex[] ve = new Vertex[c];
            for(int i = 0; i < c; i++) {
                ve[i] = new Vertex(Vector3d.add(new Vector3d(originalVerts[i]), new Vector3d(originalVerts[(i + 1) % c])).scaledByHalf());
                GData2 g2 = null;
                if ((g2 = hasEdge(originalVerts[i], originalVerts[(i + 1) % c])) != null) {
                    if (!vertexLinkedToPositionInFile.containsKey(ve[i]) || hasEdge(originalVerts[i], ve[i]) == null) newLines.add(new GData2(24, View.line_Colour_r[0], View.line_Colour_g[0], View.line_Colour_b[0], 1f, originalVerts[i], ve[i], View.DUMMY_REFERENCE, linkedDatFile));
                    if (!vertexLinkedToPositionInFile.containsKey(ve[i]) || hasEdge(originalVerts[(i + 1) % c], ve[i]) == null) newLines.add(new GData2(24, View.line_Colour_r[0], View.line_Colour_g[0], View.line_Colour_b[0], 1f, originalVerts[(i + 1) % c], ve[i], View.DUMMY_REFERENCE, linkedDatFile));
                    linesToDelete2.add(g2);
                }
            }

            // Build triangles
            newTris.add(new GData3(colourNumber, r, g, b, a, ve[0], ve[1], ve[2], View.DUMMY_REFERENCE, linkedDatFile));
            newTris.add(new GData3(colourNumber, r, g, b, a, originalVerts[0], ve[0], ve[2], View.DUMMY_REFERENCE, linkedDatFile));
            newTris.add(new GData3(colourNumber, r, g, b, a, originalVerts[1], ve[1], ve[0], View.DUMMY_REFERENCE, linkedDatFile));
            newTris.add(new GData3(colourNumber, r, g, b, a, originalVerts[2], ve[2], ve[1], View.DUMMY_REFERENCE, linkedDatFile));
        }

        for (GData g : newLines) {
            linkedDatFile.addToTail(g);
        }
        for (GData g : newTris) {
            linkedDatFile.addToTail(g);
        }

        selectedLines.addAll(linesToDelete2);
        selectedTriangles.addAll(trisToDelete2);
        selectedData.addAll(linesToDelete2);
        selectedData.addAll(trisToDelete2);
        delete(false, false);

        selectedLines.addAll(newLines);
        selectedData.addAll(newLines);
        selectedTriangles.addAll(newTris);
        selectedData.addAll(newTris);
        roundSelection(6, 10, true, false);

        TreeSet<Vertex> verticesToMove = new TreeSet<Vertex>();

        for (GData3 tri : selectedTriangles) {
            Vertex[] verts = triangles.get(tri);
            for (Vertex v : verts) {
                verticesToMove.add(v);
            }
        }

        int newContentSize = newLines.size() + newTris.size();

        clearSelection();

        // Calculate new points, based on Loop's Algorithm
        for (Vertex v : vertexLinkedToPositionInFile.keySet()) {
            Set<VertexManifestation> manis = vertexLinkedToPositionInFile.get(v);
            HashSet<Vector3d> midEdge = new HashSet<Vector3d>();
            boolean keepIt = false;
            for (VertexManifestation m : manis) {
                GData gd = m.getGdata();
                switch (gd.type()) {
                case 0:
                    continue;
                case 2:
                    keepIt = true;
                    break;
                case 3:
                {
                    int p = m.getPosition();
                    Vertex[] verts = triangles.get(gd);
                    midEdge.add(new Vector3d(verts[(p + 1) % 3]));
                    midEdge.add(new Vector3d(verts[(p + 2) % 3]));
                }
                break;
                case 4:
                {
                    int p = m.getPosition();
                    Vertex[] verts = quads.get(gd);
                    midEdge.add(new Vector3d(verts[(p + 1) % 4]));
                    midEdge.add(new Vector3d(verts[(p + 3) % 4]));
                }
                break;
                default:
                    continue;
                }
                if (keepIt) break;
            }
            if (keepIt) {
                newPoints.put(v, v);
            } else {
                double n = midEdge.size() + 1;
                double t = 3.0 / 8.0 + 1.0 / 4.0 * Math.cos(Math.PI * 2.0 / n);
                double alphaN = 3.0 / 8.0 + t * t;
                BigDecimal oneMinusAlphaDivN = new BigDecimal((1.0 - alphaN) / n);
                BigDecimal alphaN2 = new BigDecimal(alphaN);
                Vector3d np = new Vector3d();
                for (Vector3d vd : midEdge) {
                    np = Vector3d.add(np, vd);
                }
                np.setX(v.X.multiply(alphaN2).add(np.X.multiply(oneMinusAlphaDivN, Threshold.mc)));
                np.setY(v.Y.multiply(alphaN2).add(np.Y.multiply(oneMinusAlphaDivN, Threshold.mc)));
                np.setZ(v.Z.multiply(alphaN2).add(np.Z.multiply(oneMinusAlphaDivN, Threshold.mc)));
                newPoints.put(v, new Vertex(np));
            }
        }

        for (Vertex v : verticesToMove) {
            changeVertexDirectFast(v, newPoints.get(v), true);
        }

        clearSelection();

        GData gd = linkedDatFile.getDrawChainTail();
        while (newContentSize > 0) {
            switch (gd.type()) {
            case 2:
                selectedLines.add((GData2) gd);
                selectedData.add(gd);
                break;
            case 3:
                selectedTriangles.add((GData3) gd);
                selectedData.add(gd);
                break;
            default:
                break;
            }
            gd = gd.getBefore();
            newContentSize--;
        }
        roundSelection(6, 10, true, false);

        setModified(true, true);
        validateState();
    }

    public void split(int fractions) {

        if (linkedDatFile.isReadOnly()) return;

        final Set<GData2> newLines = new HashSet<GData2>();
        final Set<GData3> newTriangles = new HashSet<GData3>();
        final Set<GData5> newCondlines = new HashSet<GData5>();

        final Set<GData2> linesToDelete2 = new HashSet<GData2>();
        final Set<GData3> trisToDelete2 = new HashSet<GData3>();
        final Set<GData4> quadsToDelete2 = new HashSet<GData4>();
        final Set<GData5> clinesToDelete2 = new HashSet<GData5>();

        final Set<AccurateEdge> edgesToSplit = new HashSet<AccurateEdge>();

        {
            int i = 0;
            int j = 0;

            for (Vertex v1 : selectedVertices) {
                for (Vertex v2 : selectedVertices) {
                    if (j > i) {
                        if (isNeighbour(v1, v2)) {
                            edgesToSplit.add(new AccurateEdge(v1, v2));
                        }
                    }
                    j++;
                }
                i++;
            }
        }

        {
            for (GData2 g2 : selectedLines) {
                if (!lineLinkedToVertices.containsKey(g2)) continue;
                Vertex[] verts = lines.get(g2);
                edgesToSplit.add(new AccurateEdge(verts[0], verts[1]));
            }
            for (GData3 g3 : selectedTriangles) {
                if (!lineLinkedToVertices.containsKey(g3)) continue;
                Vertex[] verts = triangles.get(g3);
                edgesToSplit.add(new AccurateEdge(verts[0], verts[1]));
                edgesToSplit.add(new AccurateEdge(verts[1], verts[2]));
                edgesToSplit.add(new AccurateEdge(verts[2], verts[0]));
            }
            for (GData4 g4 : selectedQuads) {
                if (!lineLinkedToVertices.containsKey(g4)) continue;
                Vertex[] verts = quads.get(g4);
                edgesToSplit.add(new AccurateEdge(verts[0], verts[1]));
                edgesToSplit.add(new AccurateEdge(verts[1], verts[2]));
                edgesToSplit.add(new AccurateEdge(verts[2], verts[3]));
                edgesToSplit.add(new AccurateEdge(verts[3], verts[0]));
            }
            for (GData5 g5 : selectedCondlines) {
                if (!lineLinkedToVertices.containsKey(g5)) continue;
                Vertex[] verts = condlines.get(g5);
                edgesToSplit.add(new AccurateEdge(verts[0], verts[1]));
            }
        }

        clearSelection();

        for (GData2 g : new HashSet<GData2>(lines.keySet())) {
            if (!lineLinkedToVertices.containsKey(g)) continue;
            List<GData2> result = split(g, fractions, edgesToSplit);
            if (result.isEmpty()) continue;
            newLines.addAll(result);
            for (GData n : result) {
                linkedDatFile.insertAfter(g, n);
            }
            linesToDelete2.add(g);
        }

        for (GData3 g : new HashSet<GData3>(triangles.keySet())) {
            if (!lineLinkedToVertices.containsKey(g)) continue;
            List<GData3> result = split(g, fractions, edgesToSplit);
            if (result.isEmpty()) continue;
            newTriangles.addAll(result);
            for (GData n : result) {
                linkedDatFile.insertAfter(g, n);
            }
            trisToDelete2.add(g);
        }

        for (GData4 g : new HashSet<GData4>(quads.keySet())) {
            if (!lineLinkedToVertices.containsKey(g)) continue;
            List<GData3> result = split(g, fractions, edgesToSplit);
            if (result.isEmpty()) continue;
            newTriangles.addAll(result);
            for (GData n : result) {
                linkedDatFile.insertAfter(g, n);
            }
            quadsToDelete2.add(g);
        }

        for (GData5 g : new HashSet<GData5>(condlines.keySet())) {
            List<GData5> result = split(g, fractions, edgesToSplit);
            if (result.isEmpty()) continue;
            newCondlines.addAll(result);
            for (GData n : result) {
                linkedDatFile.insertAfter(g, n);
            }
            clinesToDelete2.add(g);
        }

        if (newLines.size() + newTriangles.size() + newCondlines.size() > 0) {
            setModified_NoSync();
        }

        selectedLines.addAll(linesToDelete2);
        selectedTriangles.addAll(trisToDelete2);
        selectedQuads.addAll(quadsToDelete2);
        selectedCondlines.addAll(clinesToDelete2);
        selectedData.addAll(selectedLines);
        selectedData.addAll(selectedTriangles);
        selectedData.addAll(selectedQuads);
        selectedData.addAll(selectedCondlines);
        delete(false, false);

        selectedTriangles.addAll(newTriangles);
        selectedData.addAll(selectedTriangles);
        rectify(new RectifierSettings(), false);

        clearSelection();
        if (isModified()) {
            syncWithTextEditors(true);
        }
        validateState();
    }

    private List<GData5> split(GData5 g, int fractions, Set<AccurateEdge> edgesToSplit) {

        ArrayList<GData5> result = new ArrayList<GData5>(fractions);

        // Detect how many edges are affected
        Vertex[] verts = condlines.get(g);
        int ec = edgesToSplit.contains(new AccurateEdge(verts[0], verts[1])) ? 1 :0;

        switch (ec) {
        case 0:
            return result;
        case 1:

            Vector3d A = new Vector3d(lines.get(g)[0]);
            Vector3d B = new Vector3d(lines.get(g)[1]);

            BigDecimal step = BigDecimal.ONE.divide(new BigDecimal(fractions), Threshold.mc);
            BigDecimal cur = BigDecimal.ZERO;
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusCur = BigDecimal.ONE.subtract(cur);
                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

                result.add(new GData5(g.colourNumber, g.r, g.g, g.b, g.a,

                        A.X.multiply(oneMinusCur).add(B.X.multiply(cur)),
                        A.Y.multiply(oneMinusCur).add(B.Y.multiply(cur)),
                        A.Z.multiply(oneMinusCur).add(B.Z.multiply(cur)),

                        A.X.multiply(oneMinusNext).add(B.X.multiply(next)),
                        A.Y.multiply(oneMinusNext).add(B.Y.multiply(next)),
                        A.Z.multiply(oneMinusNext).add(B.Z.multiply(next)),

                        g.X3, g.Y3, g.Z3,
                        g.X4, g.Y4, g.Z4,

                        View.DUMMY_REFERENCE, linkedDatFile));
                cur = next;
            }
        }
        return result;
    }

    private List<GData3> split(GData4 g, int fractions, Set<AccurateEdge> edgesToSplit) {

        // Detect how many edges are affected
        Vertex[] verts = quads.get(g);
        int ec = edgesToSplit.contains(new AccurateEdge(verts[0], verts[1])) ? 1 :0;
        ec += edgesToSplit.contains(new AccurateEdge(verts[1], verts[2])) ? 1 :0;
        ec += edgesToSplit.contains(new AccurateEdge(verts[2], verts[3])) ? 1 :0;
        ec += edgesToSplit.contains(new AccurateEdge(verts[3], verts[0])) ? 1 :0;

        switch (ec) {
        case 0:
            return new ArrayList<GData3>();
        case 1:
            if (edgesToSplit.contains(new AccurateEdge(verts[0], verts[1]))) {
                return splitQuad1(verts[0], verts[1], verts[2], verts[3], fractions, g);
            } else if (edgesToSplit.contains(new AccurateEdge(verts[1], verts[2]))) {
                return splitQuad1(verts[1], verts[2], verts[3], verts[0], fractions, g);
            } else if (edgesToSplit.contains(new AccurateEdge(verts[2], verts[3]))) {
                return splitQuad1(verts[2], verts[3], verts[0], verts[1], fractions, g);
            } else {
                return splitQuad1(verts[3], verts[0], verts[1], verts[2], fractions, g);
            }
        case 2:

            if (edgesToSplit.contains(new AccurateEdge(verts[0], verts[1]))) {

                if (edgesToSplit.contains(new AccurateEdge(verts[1], verts[2]))) {

                    return splitQuad21(verts[0], verts[1], verts[2], verts[3], fractions, g);

                } else if (edgesToSplit.contains(new AccurateEdge(verts[2], verts[3]))) {

                    return splitQuad22(verts[0], verts[1], verts[2], verts[3], fractions, g);

                } else if (edgesToSplit.contains(new AccurateEdge(verts[3], verts[0]))) {

                    return splitQuad21(verts[3], verts[0], verts[1], verts[2], fractions, g);

                }
            } else if (edgesToSplit.contains(new AccurateEdge(verts[1], verts[2]))) {

                if (edgesToSplit.contains(new AccurateEdge(verts[2], verts[3]))) {

                    return splitQuad21(verts[1], verts[2], verts[3], verts[0], fractions, g);

                } else if (edgesToSplit.contains(new AccurateEdge(verts[3], verts[0]))) {

                    return splitQuad22(verts[1], verts[2], verts[3], verts[0], fractions, g);

                }
            } else if (edgesToSplit.contains(new AccurateEdge(verts[2], verts[3]))) {

                return splitQuad21(verts[2], verts[3], verts[0], verts[1], fractions, g);

            }
        case 3:
            if (!edgesToSplit.contains(new AccurateEdge(verts[0], verts[1]))) {
                return splitQuad3(verts[0], verts[1], verts[2], verts[3], fractions, g);
            } else if (!edgesToSplit.contains(new AccurateEdge(verts[1], verts[2]))) {
                return splitQuad3(verts[1], verts[2], verts[3], verts[0], fractions, g);
            } else if (!edgesToSplit.contains(new AccurateEdge(verts[2], verts[3]))) {
                return splitQuad3(verts[2], verts[3], verts[0], verts[1], fractions, g);
            } else {
                return splitQuad3(verts[3], verts[0], verts[1], verts[2], fractions, g);
            }
        case 4:
            return splitQuad4(verts[0], verts[1], verts[2], verts[3], fractions, g);
        default:
            break;
        }

        return new ArrayList<GData3>();
    }

    private List<GData3> splitQuad1(Vertex v1, Vertex v2, Vertex v3, Vertex v4, int fractions, GData4 g) {
        ArrayList<GData3> result = new ArrayList<GData3>(fractions);

        Vector3d A = new Vector3d(v1);
        Vector3d B = new Vector3d(v2);


        int fracA = fractions / 2;

        BigDecimal step = BigDecimal.ONE.divide(new BigDecimal(fractions), Threshold.mc);
        BigDecimal cur = BigDecimal.ZERO;
        BigDecimal next = BigDecimal.ZERO;

        BigDecimal middle = BigDecimal.ZERO;

        for (int i = 0; i < fracA; i++) {
            next = next.add(step);

            BigDecimal oneMinusCur = BigDecimal.ONE.subtract(cur);
            BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

            result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                    A.X.multiply(oneMinusCur).add(B.X.multiply(cur)),
                    A.Y.multiply(oneMinusCur).add(B.Y.multiply(cur)),
                    A.Z.multiply(oneMinusCur).add(B.Z.multiply(cur)),

                    A.X.multiply(oneMinusNext).add(B.X.multiply(next)),
                    A.Y.multiply(oneMinusNext).add(B.Y.multiply(next)),
                    A.Z.multiply(oneMinusNext).add(B.Z.multiply(next)),

                    v4.X,
                    v4.Y,
                    v4.Z,

                    View.DUMMY_REFERENCE, linkedDatFile));
            cur = next;
            middle = next;
        }


        for (int i = fracA; i < fractions; i++) {
            if (i == fractions - 1) {
                next = BigDecimal.ONE;
            } else {
                next = next.add(step);
            }

            BigDecimal oneMinusCur = BigDecimal.ONE.subtract(cur);
            BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

            result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                    A.X.multiply(oneMinusCur).add(B.X.multiply(cur)),
                    A.Y.multiply(oneMinusCur).add(B.Y.multiply(cur)),
                    A.Z.multiply(oneMinusCur).add(B.Z.multiply(cur)),

                    A.X.multiply(oneMinusNext).add(B.X.multiply(next)),
                    A.Y.multiply(oneMinusNext).add(B.Y.multiply(next)),
                    A.Z.multiply(oneMinusNext).add(B.Z.multiply(next)),

                    v3.X,
                    v3.Y,
                    v3.Z,

                    View.DUMMY_REFERENCE, linkedDatFile));
            cur = next;
        }

        {

            cur = middle;

            BigDecimal oneMinusCur = BigDecimal.ONE.subtract(cur);

            result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                    A.X.multiply(oneMinusCur).add(B.X.multiply(cur)),
                    A.Y.multiply(oneMinusCur).add(B.Y.multiply(cur)),
                    A.Z.multiply(oneMinusCur).add(B.Z.multiply(cur)),

                    v3.X,
                    v3.Y,
                    v3.Z,

                    v4.X,
                    v4.Y,
                    v4.Z,

                    View.DUMMY_REFERENCE, linkedDatFile));
        }

        return result;
    }

    private List<GData3> splitQuad21(Vertex v1, Vertex v2, Vertex v3, Vertex v4, int fractions, GData4 g) {

        ArrayList<GData3> result = new ArrayList<GData3>(fractions * 8);

        // Split between v1-v2 & v2-v3
        Vector3d A = new Vector3d(v1);
        Vector3d B = new Vector3d(v2);
        Vector3d C = new Vector3d(v3);
        Vector3d D = new Vector3d(v4);

        BigDecimal step = BigDecimal.ONE.divide(new BigDecimal(fractions), Threshold.mc);

        ArrayList<Vector3d> newPoints = new ArrayList<Vector3d>(fractions * 4);

        {
            BigDecimal cur = BigDecimal.ZERO;
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusCur = BigDecimal.ONE.subtract(cur);
                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

                newPoints.add(new Vector3d(
                        A.X.multiply(oneMinusCur).add(B.X.multiply(cur)),
                        A.Y.multiply(oneMinusCur).add(B.Y.multiply(cur)),
                        A.Z.multiply(oneMinusCur).add(B.Z.multiply(cur))
                        ));
                newPoints.add(new Vector3d(
                        A.X.multiply(oneMinusNext).add(B.X.multiply(next)),
                        A.Y.multiply(oneMinusNext).add(B.Y.multiply(next)),
                        A.Z.multiply(oneMinusNext).add(B.Z.multiply(next))
                        ));

                newPoints.add(D);

                cur = next;
            }
        }
        {
            BigDecimal cur = BigDecimal.ZERO;
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusCur = BigDecimal.ONE.subtract(cur);
                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

                newPoints.add(new Vector3d(
                        B.X.multiply(oneMinusCur).add(C.X.multiply(cur)),
                        B.Y.multiply(oneMinusCur).add(C.Y.multiply(cur)),
                        B.Z.multiply(oneMinusCur).add(C.Z.multiply(cur))
                        ));
                newPoints.add(new Vector3d(
                        B.X.multiply(oneMinusNext).add(C.X.multiply(next)),
                        B.Y.multiply(oneMinusNext).add(C.Y.multiply(next)),
                        B.Z.multiply(oneMinusNext).add(C.Z.multiply(next))
                        ));

                newPoints.add(D);

                cur = next;
            }
        }

        final int pz = newPoints.size();
        for (int i = 0; i < pz; i += 3) {
            Vector3d p1 = newPoints.get(i);
            Vector3d p2 = newPoints.get(i + 1);
            Vector3d p3 = newPoints.get(i + 2);

            result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                    p3.X,
                    p3.Y,
                    p3.Z,

                    p1.X,
                    p1.Y,
                    p1.Z,

                    p2.X,
                    p2.Y,
                    p2.Z,

                    View.DUMMY_REFERENCE, linkedDatFile));
        }

        return result;
    }

    private List<GData3> splitQuad22(Vertex v1, Vertex v2, Vertex v3, Vertex v4, int fractions, GData4 g) {

        ArrayList<GData3> result = new ArrayList<GData3>(fractions * 8);

        // Split between v1-v2 & v3-v4
        Vector3d A = new Vector3d(v1);
        Vector3d B = new Vector3d(v2);
        Vector3d C = new Vector3d(v3);
        Vector3d D = new Vector3d(v4);

        BigDecimal step = BigDecimal.ONE.divide(new BigDecimal(fractions), Threshold.mc);

        ArrayList<Vector3d> newPoints = new ArrayList<Vector3d>(fractions * 4);

        {
            BigDecimal cur = BigDecimal.ZERO;
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusCur = BigDecimal.ONE.subtract(cur);
                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

                newPoints.add(new Vector3d(
                        A.X.multiply(oneMinusCur).add(B.X.multiply(cur)),
                        A.Y.multiply(oneMinusCur).add(B.Y.multiply(cur)),
                        A.Z.multiply(oneMinusCur).add(B.Z.multiply(cur))
                        ));
                newPoints.add(new Vector3d(
                        D.X.multiply(oneMinusCur).add(C.X.multiply(cur)),
                        D.Y.multiply(oneMinusCur).add(C.Y.multiply(cur)),
                        D.Z.multiply(oneMinusCur).add(C.Z.multiply(cur))
                        ));
                newPoints.add(new Vector3d(
                        A.X.multiply(oneMinusNext).add(B.X.multiply(next)),
                        A.Y.multiply(oneMinusNext).add(B.Y.multiply(next)),
                        A.Z.multiply(oneMinusNext).add(B.Z.multiply(next))
                        ));
                newPoints.add(new Vector3d(
                        D.X.multiply(oneMinusNext).add(C.X.multiply(next)),
                        D.Y.multiply(oneMinusNext).add(C.Y.multiply(next)),
                        D.Z.multiply(oneMinusNext).add(C.Z.multiply(next))
                        ));

                cur = next;
            }
        }

        final int pz = newPoints.size();
        for (int i = 0; i < pz; i += 4) {
            Vector3d p1 = newPoints.get(i);
            Vector3d p2 = newPoints.get(i + 1);
            Vector3d p3 = newPoints.get(i + 2);
            Vector3d p4 = newPoints.get(i + 3);

            result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                    p2.X,
                    p2.Y,
                    p2.Z,

                    p1.X,
                    p1.Y,
                    p1.Z,

                    p3.X,
                    p3.Y,
                    p3.Z,

                    View.DUMMY_REFERENCE, linkedDatFile));

            result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                    p3.X,
                    p3.Y,
                    p3.Z,

                    p4.X,
                    p4.Y,
                    p4.Z,

                    p2.X,
                    p2.Y,
                    p2.Z,

                    View.DUMMY_REFERENCE, linkedDatFile));

        }

        return result;
    }

    private List<GData3> splitQuad3(Vertex v1, Vertex v2, Vertex v3, Vertex v4, int fractions, GData4 g) {
        ArrayList<GData3> result = new ArrayList<GData3>(fractions * 8);

        Vector3d A = new Vector3d(v1);
        Vector3d B = new Vector3d(v2);
        Vector3d C = new Vector3d(v3);
        Vector3d D = new Vector3d(v4);

        Vector3d vc = Vector3d.add(Vector3d.add(Vector3d.add(A, B), C), D);
        vc.setX(vc.X.divide(new BigDecimal(4), Threshold.mc));
        vc.setY(vc.Y.divide(new BigDecimal(4), Threshold.mc));
        vc.setZ(vc.Z.divide(new BigDecimal(4), Threshold.mc));

        BigDecimal step = BigDecimal.ONE.divide(new BigDecimal(fractions), Threshold.mc);

        ArrayList<Vector3d> newPoints = new ArrayList<Vector3d>(fractions * 3);

        {
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {

                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);


                newPoints.add(new Vector3d(
                        B.X.multiply(oneMinusNext).add(C.X.multiply(next)),
                        B.Y.multiply(oneMinusNext).add(C.Y.multiply(next)),
                        B.Z.multiply(oneMinusNext).add(C.Z.multiply(next))
                        ));

                next = next.add(step);
            }
        }
        {
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {

                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

                newPoints.add(new Vector3d(
                        C.X.multiply(oneMinusNext).add(D.X.multiply(next)),
                        C.Y.multiply(oneMinusNext).add(D.Y.multiply(next)),
                        C.Z.multiply(oneMinusNext).add(D.Z.multiply(next))
                        ));

                next = next.add(step);
            }
        }
        {
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions + 1; i++) {

                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

                newPoints.add(new Vector3d(
                        D.X.multiply(oneMinusNext).add(A.X.multiply(next)),
                        D.Y.multiply(oneMinusNext).add(A.Y.multiply(next)),
                        D.Z.multiply(oneMinusNext).add(A.Z.multiply(next))
                        ));

                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }
            }
        }

        fractions = fractions * 3;
        for (int i = 0; i < fractions + 1; i++) {

            result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                    vc.X,
                    vc.Y,
                    vc.Z,

                    newPoints.get(i).X,
                    newPoints.get(i).Y,
                    newPoints.get(i).Z,

                    newPoints.get((i + 1) % (fractions + 1)).X,
                    newPoints.get((i + 1) % (fractions + 1)).Y,
                    newPoints.get((i + 1) % (fractions + 1)).Z,

                    View.DUMMY_REFERENCE, linkedDatFile));


        }

        return result;
    }

    private List<GData3> splitQuad4(Vertex v1, Vertex v2, Vertex v3, Vertex v4, int fractions, GData4 g) {

        ArrayList<GData3> result = new ArrayList<GData3>(fractions * 8);

        Vector3d A = new Vector3d(v1);
        Vector3d B = new Vector3d(v2);
        Vector3d C = new Vector3d(v3);
        Vector3d D = new Vector3d(v4);

        Vector3d vc = Vector3d.add(Vector3d.add(Vector3d.add(A, B), C), D);
        vc.setX(vc.X.divide(new BigDecimal(4), Threshold.mc));
        vc.setY(vc.Y.divide(new BigDecimal(4), Threshold.mc));
        vc.setZ(vc.Z.divide(new BigDecimal(4), Threshold.mc));

        BigDecimal step = BigDecimal.ONE.divide(new BigDecimal(fractions), Threshold.mc);

        ArrayList<Vector3d> newPoints = new ArrayList<Vector3d>(fractions * 4);

        {
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);


                newPoints.add(new Vector3d(
                        A.X.multiply(oneMinusNext).add(B.X.multiply(next)),
                        A.Y.multiply(oneMinusNext).add(B.Y.multiply(next)),
                        A.Z.multiply(oneMinusNext).add(B.Z.multiply(next))
                        ));
            }
        }
        {
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);


                newPoints.add(new Vector3d(
                        B.X.multiply(oneMinusNext).add(C.X.multiply(next)),
                        B.Y.multiply(oneMinusNext).add(C.Y.multiply(next)),
                        B.Z.multiply(oneMinusNext).add(C.Z.multiply(next))
                        ));
            }
        }
        {
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

                newPoints.add(new Vector3d(
                        C.X.multiply(oneMinusNext).add(D.X.multiply(next)),
                        C.Y.multiply(oneMinusNext).add(D.Y.multiply(next)),
                        C.Z.multiply(oneMinusNext).add(D.Z.multiply(next))
                        ));
            }
        }
        {
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

                newPoints.add(new Vector3d(
                        D.X.multiply(oneMinusNext).add(A.X.multiply(next)),
                        D.Y.multiply(oneMinusNext).add(A.Y.multiply(next)),
                        D.Z.multiply(oneMinusNext).add(A.Z.multiply(next))
                        ));
            }
        }

        fractions = fractions * 4;
        for (int i = 0; i < fractions; i++) {

            result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                    vc.X,
                    vc.Y,
                    vc.Z,

                    newPoints.get(i).X,
                    newPoints.get(i).Y,
                    newPoints.get(i).Z,

                    newPoints.get((i + 1) % fractions).X,
                    newPoints.get((i + 1) % fractions).Y,
                    newPoints.get((i + 1) % fractions).Z,

                    View.DUMMY_REFERENCE, linkedDatFile));


        }

        return result;
    }

    private List<GData3> split(GData3 g, int fractions, Set<AccurateEdge> edgesToSplit) {

        // Detect how many edges are affected
        Vertex[] verts = triangles.get(g);
        int ec = edgesToSplit.contains(new AccurateEdge(verts[0], verts[1])) ? 1 :0;
        ec += edgesToSplit.contains(new AccurateEdge(verts[1], verts[2])) ? 1 :0;
        ec += edgesToSplit.contains(new AccurateEdge(verts[2], verts[0])) ? 1 :0;

        switch (ec) {
        case 0:
            return new ArrayList<GData3>();
        case 1:
            if (edgesToSplit.contains(new AccurateEdge(verts[0], verts[1]))) {
                return splitTri1(verts[0], verts[1], verts[2], fractions, g);
            } else if (edgesToSplit.contains(new AccurateEdge(verts[1], verts[2]))) {
                return splitTri1(verts[1], verts[2], verts[0], fractions, g);
            } else {
                return splitTri1(verts[2], verts[0], verts[1], fractions, g);
            }
        case 2:
            if (edgesToSplit.contains(new AccurateEdge(verts[0], verts[1]))) {
                if (edgesToSplit.contains(new AccurateEdge(verts[1], verts[2]))) {
                    return splitTri2(verts[1], verts[2], verts[0], fractions, g);
                } else {
                    return splitTri2(verts[0], verts[1], verts[2], fractions, g);
                }
            } else if (edgesToSplit.contains(new AccurateEdge(verts[1], verts[2]))) {
                if (edgesToSplit.contains(new AccurateEdge(verts[0], verts[1]))) {
                    return splitTri2(verts[1], verts[2], verts[0], fractions, g);
                } else {
                    return splitTri2(verts[2], verts[0], verts[1], fractions, g);
                }
            } else if (edgesToSplit.contains(new AccurateEdge(verts[2], verts[0]))) {
                if (edgesToSplit.contains(new AccurateEdge(verts[0], verts[1]))) {
                    return splitTri2(verts[0], verts[1], verts[2], fractions, g);
                } else {
                    return splitTri2(verts[2], verts[0], verts[1], fractions, g);
                }
            }
        case 3:
            return splitTri3(verts[0], verts[1], verts[2], fractions, g);
        default:
            break;
        }

        return new ArrayList<GData3>();
    }

    private List<GData3> splitTri1(Vertex v1, Vertex v2, Vertex v3, int fractions, GData3 g) {
        ArrayList<GData3> result = new ArrayList<GData3>(fractions);

        Vector3d A = new Vector3d(v1);
        Vector3d B = new Vector3d(v2);

        BigDecimal step = BigDecimal.ONE.divide(new BigDecimal(fractions), Threshold.mc);
        BigDecimal cur = BigDecimal.ZERO;
        BigDecimal next = BigDecimal.ZERO;
        for (int i = 0; i < fractions; i++) {
            if (i == fractions - 1) {
                next = BigDecimal.ONE;
            } else {
                next = next.add(step);
            }

            BigDecimal oneMinusCur = BigDecimal.ONE.subtract(cur);
            BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

            result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                    A.X.multiply(oneMinusCur).add(B.X.multiply(cur)),
                    A.Y.multiply(oneMinusCur).add(B.Y.multiply(cur)),
                    A.Z.multiply(oneMinusCur).add(B.Z.multiply(cur)),

                    A.X.multiply(oneMinusNext).add(B.X.multiply(next)),
                    A.Y.multiply(oneMinusNext).add(B.Y.multiply(next)),
                    A.Z.multiply(oneMinusNext).add(B.Z.multiply(next)),

                    v3.X,
                    v3.Y,
                    v3.Z,

                    View.DUMMY_REFERENCE, linkedDatFile));
            cur = next;
        }

        return result;
    }

    private List<GData3> splitTri2(Vertex v1, Vertex v2, Vertex v3, int fractions, GData3 g) {

        ArrayList<GData3> result = new ArrayList<GData3>(fractions);

        Vector3d A = new Vector3d(v1);
        Vector3d B = new Vector3d(v2);
        Vector3d C = new Vector3d(v3);

        BigDecimal step = BigDecimal.ONE.divide(new BigDecimal(fractions), Threshold.mc);
        BigDecimal cur = BigDecimal.ZERO;
        BigDecimal next = BigDecimal.ZERO;
        for (int i = 0; i < fractions; i++) {
            if (i == fractions - 1) {
                next = BigDecimal.ONE;
            } else {
                next = next.add(step);
            }

            BigDecimal oneMinusCur = BigDecimal.ONE.subtract(cur);
            BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

            if (i == 0) {
                result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                        v1.X,
                        v1.Y,
                        v1.Z,

                        A.X.multiply(oneMinusNext).add(B.X.multiply(next)),
                        A.Y.multiply(oneMinusNext).add(B.Y.multiply(next)),
                        A.Z.multiply(oneMinusNext).add(B.Z.multiply(next)),

                        A.X.multiply(oneMinusNext).add(C.X.multiply(next)),
                        A.Y.multiply(oneMinusNext).add(C.Y.multiply(next)),
                        A.Z.multiply(oneMinusNext).add(C.Z.multiply(next)),

                        View.DUMMY_REFERENCE, linkedDatFile));
            } else {
                result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                        A.X.multiply(oneMinusNext).add(C.X.multiply(next)),
                        A.Y.multiply(oneMinusNext).add(C.Y.multiply(next)),
                        A.Z.multiply(oneMinusNext).add(C.Z.multiply(next)),

                        A.X.multiply(oneMinusCur).add(C.X.multiply(cur)),
                        A.Y.multiply(oneMinusCur).add(C.Y.multiply(cur)),
                        A.Z.multiply(oneMinusCur).add(C.Z.multiply(cur)),

                        A.X.multiply(oneMinusCur).add(B.X.multiply(cur)),
                        A.Y.multiply(oneMinusCur).add(B.Y.multiply(cur)),
                        A.Z.multiply(oneMinusCur).add(B.Z.multiply(cur)),

                        View.DUMMY_REFERENCE, linkedDatFile));
                result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                        A.X.multiply(oneMinusCur).add(B.X.multiply(cur)),
                        A.Y.multiply(oneMinusCur).add(B.Y.multiply(cur)),
                        A.Z.multiply(oneMinusCur).add(B.Z.multiply(cur)),

                        A.X.multiply(oneMinusNext).add(B.X.multiply(next)),
                        A.Y.multiply(oneMinusNext).add(B.Y.multiply(next)),
                        A.Z.multiply(oneMinusNext).add(B.Z.multiply(next)),

                        A.X.multiply(oneMinusNext).add(C.X.multiply(next)),
                        A.Y.multiply(oneMinusNext).add(C.Y.multiply(next)),
                        A.Z.multiply(oneMinusNext).add(C.Z.multiply(next)),

                        View.DUMMY_REFERENCE, linkedDatFile));
            }

            cur = next;
        }

        return result;
    }

    private List<GData3> splitTri3(Vertex v1, Vertex v2, Vertex v3, int fractions, GData3 g) {

        ArrayList<GData3> result = new ArrayList<GData3>(fractions * 3);

        Vector3d A = new Vector3d(v1);
        Vector3d B = new Vector3d(v2);
        Vector3d C = new Vector3d(v3);

        Vector3d vc = Vector3d.add(Vector3d.add(A, B), C);
        vc.setX(vc.X.divide(new BigDecimal(3), Threshold.mc));
        vc.setY(vc.Y.divide(new BigDecimal(3), Threshold.mc));
        vc.setZ(vc.Z.divide(new BigDecimal(3), Threshold.mc));

        BigDecimal step = BigDecimal.ONE.divide(new BigDecimal(fractions), Threshold.mc);

        ArrayList<Vector3d> newPoints = new ArrayList<Vector3d>(fractions * 3);

        {
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);


                newPoints.add(new Vector3d(
                        A.X.multiply(oneMinusNext).add(B.X.multiply(next)),
                        A.Y.multiply(oneMinusNext).add(B.Y.multiply(next)),
                        A.Z.multiply(oneMinusNext).add(B.Z.multiply(next))
                        ));
            }
        }
        {
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);


                newPoints.add(new Vector3d(
                        B.X.multiply(oneMinusNext).add(C.X.multiply(next)),
                        B.Y.multiply(oneMinusNext).add(C.Y.multiply(next)),
                        B.Z.multiply(oneMinusNext).add(C.Z.multiply(next))
                        ));
            }
        }
        {
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

                newPoints.add(new Vector3d(
                        C.X.multiply(oneMinusNext).add(A.X.multiply(next)),
                        C.Y.multiply(oneMinusNext).add(A.Y.multiply(next)),
                        C.Z.multiply(oneMinusNext).add(A.Z.multiply(next))
                        ));
            }
        }

        fractions = fractions * 3;
        for (int i = 0; i < fractions; i++) {

            result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                    vc.X,
                    vc.Y,
                    vc.Z,

                    newPoints.get(i).X,
                    newPoints.get(i).Y,
                    newPoints.get(i).Z,

                    newPoints.get((i + 1) % fractions).X,
                    newPoints.get((i + 1) % fractions).Y,
                    newPoints.get((i + 1) % fractions).Z,

                    View.DUMMY_REFERENCE, linkedDatFile));


        }

        return result;
    }

    private List<GData2> split(GData2 g, int fractions, Set<AccurateEdge> edgesToSplit) {

        ArrayList<GData2> result = new ArrayList<GData2>(fractions);

        // Detect how many edges are affected
        Vertex[] verts = lines.get(g);
        int ec = edgesToSplit.contains(new AccurateEdge(verts[0], verts[1])) ? 1 :0;

        switch (ec) {
        case 0:
            return result;
        case 1:

            Vector3d A = new Vector3d(lines.get(g)[0]);
            Vector3d B = new Vector3d(lines.get(g)[1]);

            BigDecimal step = BigDecimal.ONE.divide(new BigDecimal(fractions), Threshold.mc);
            BigDecimal cur = BigDecimal.ZERO;
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusCur = BigDecimal.ONE.subtract(cur);
                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

                // Cx = Ax * (1-t) + Bx * t
                // Cy = Ay * (1-t) + By * t

                result.add(new GData2(g.colourNumber, g.r, g.g, g.b, g.a,

                        A.X.multiply(oneMinusCur).add(B.X.multiply(cur)),
                        A.Y.multiply(oneMinusCur).add(B.Y.multiply(cur)),
                        A.Z.multiply(oneMinusCur).add(B.Z.multiply(cur)),

                        A.X.multiply(oneMinusNext).add(B.X.multiply(next)),
                        A.Y.multiply(oneMinusNext).add(B.Y.multiply(next)),
                        A.Z.multiply(oneMinusNext).add(B.Z.multiply(next)),

                        View.DUMMY_REFERENCE, linkedDatFile));
                cur = next;
            }
        }
        return result;
    }

    private final AtomicBoolean resetTimer = new AtomicBoolean(false);
    private final AtomicInteger tid = new AtomicInteger(0);
    private final AtomicInteger openThreads = new AtomicInteger(0);
    private final Lock lock = new ReentrantLock();
    public void syncWithTextEditors(boolean addHistory) {

        if (addHistory) linkedDatFile.addHistory();

        try {
            lock.lock();

            if (isSkipSyncWithTextEditor() || !isSyncWithTextEditor())  {
                // lock.unlock() call on finally!
                return;
            }
            if (openThreads.get() > 10) {
                resetTimer.set(true);
                // lock.unlock() call on finally!
                return;
            }
            final AtomicInteger tid2 = new AtomicInteger(tid.incrementAndGet());
            final Thread syncThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    openThreads.incrementAndGet();
                    do {
                        resetTimer.set(false);
                        try {
                            Thread.sleep(450);
                        } catch (InterruptedException e) {
                        }
                        if (tid2.get() != tid.get()) break;
                        try {
                            Thread.sleep(450);
                        } catch (InterruptedException e) {
                        }
                        if (tid2.get() != tid.get()) break;
                        try {
                            Thread.sleep(450);
                        } catch (InterruptedException e) {
                        }
                        if (tid2.get() != tid.get()) break;
                        try {
                            Thread.sleep(450);
                        } catch (InterruptedException e) {
                        }
                        if (tid2.get() != tid.get()) break;
                    } while (resetTimer.get());
                    openThreads.decrementAndGet();
                    if (tid2.get() != tid.get() || isSkipSyncWithTextEditor() || !isSyncWithTextEditor()) return;
                    boolean notFound = true;
                    Lock lock2 = null;
                    try {
                        lock2 = linkedDatFile.getHistory().getLock();
                        lock.lock();
                        // "lock2" will be locked, if undo/redo tries to restore the state.
                        // Any attempt to broke the data structure with an old synchronisation state will be
                        // prevented with this lock.
                        if (lock2.tryLock()) {
                            try {
                                // A lot of stuff can throw an exception here, since the thread waits two seconds and
                                // the state of the program may not allow a synchronisation anymore
                                for (EditorTextWindow w : Project.getOpenTextWindows()) {
                                    for (final CTabItem t : w.getTabFolder().getItems()) {
                                        final DatFile txtDat = ((CompositeTab) t).getState().getFileNameObj();
                                        if (txtDat != null && txtDat.equals(linkedDatFile)) {
                                            notFound = false;
                                            final String txt;
                                            if (isModified()) {
                                                txt = txtDat.getText();
                                            } else {
                                                txt = null;
                                            }
                                            Display.getDefault().asyncExec(new Runnable() {
                                                @Override
                                                public void run() {

                                                    int ti = ((CompositeTab) t).getTextComposite().getTopIndex();

                                                    Point r = ((CompositeTab) t).getTextComposite().getSelectionRange();
                                                    ((CompositeTab) t).getState().setSync(true);
                                                    if (isModified() && txt != null) {
                                                        ((CompositeTab) t).getTextComposite().setText(txt);
                                                    }
                                                    ((CompositeTab) t).getTextComposite().setTopIndex(ti);
                                                    try {
                                                        ((CompositeTab) t).getTextComposite().setSelectionRange(r.x, r.y);
                                                    } catch (IllegalArgumentException consumed) {}
                                                    ((CompositeTab) t).getTextComposite().redraw();
                                                    ((CompositeTab) t).getControl().redraw();
                                                    ((CompositeTab) t).getState().setSync(false);
                                                    setUpdated(true);
                                                }
                                            });
                                        }
                                    }
                                }
                            } catch (Exception consumed) {
                                setUpdated(true);
                            } finally {
                                if (notFound) setUpdated(true);
                            }
                            if (WorkbenchManager.getUserSettingState().getSyncWithLpeInline().get()) {
                                while (!isUpdated() && Editor3DWindow.getAlive().get()) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                    }
                                }
                                Display.getDefault().asyncExec(new Runnable() {
                                    @Override
                                    public void run() {
                                        SubfileCompiler.compile(linkedDatFile, true, true);
                                    }
                                });
                            }
                        } else {
                            NLogger.debug(getClass(), "Synchronisation was skipped due to undo/redo."); //$NON-NLS-1$
                        }
                    } finally {
                        if (lock2 != null) lock2.unlock();
                        lock.unlock();
                    }
                }
            });
            syncThread.start();
        } finally {
            lock.unlock();
        }
    }

    public boolean isSyncWithLpeInline() {
        return WorkbenchManager.getUserSettingState().getSyncWithLpeInline().get();
    }

    public boolean isSyncWithTextEditor() {
        return WorkbenchManager.getUserSettingState().getSyncWithTextEditor().get();
    }

    public void setSyncWithTextEditor(boolean syncWithTextEditor) {
        WorkbenchManager.getUserSettingState().getSyncWithTextEditor().set(syncWithTextEditor);
    }

    public boolean isSkipSyncWithTextEditor() {
        return skipSyncWithTextEditor.get();
    }

    public void setSkipSyncWithTextEditor(boolean syncWithTextEditor) {
        this.skipSyncWithTextEditor.set(syncWithTextEditor);
    }

    public AtomicBoolean getResetTimer() {
        return resetTimer;
    }

    public void merge(MergeTo mode, boolean syncWithTextEditor) {

        if (linkedDatFile.isReadOnly()) return;

        Vector3d newVertex = new Vector3d();
        Set<Vertex> originVerts = new TreeSet<Vertex>();

        if (mode != MergeTo.LAST_SELECTED) {
            originVerts.addAll(selectedVertices);
            for (GData2 g : selectedLines) {
                for (Vertex v : lines.get(g)) {
                    originVerts.add(v);
                }
            }
            for (GData3 g : selectedTriangles) {
                for (Vertex v : triangles.get(g)) {
                    originVerts.add(v);
                }
            }
            for (GData4 g : selectedQuads) {
                for (Vertex v : quads.get(g)) {
                    originVerts.add(v);
                }
            }
            for (GData5 g : selectedCondlines) {
                for (Vertex v : condlines.get(g)) {
                    originVerts.add(v);
                }
            }
        }

        switch (mode) {
        case AVERAGE:
            if (originVerts.size() == 0) return;
            for (Vertex v : originVerts) {
                newVertex = Vector3d.add(newVertex, new Vector3d(v));
            }
            final BigDecimal size = new BigDecimal(originVerts.size());
            newVertex.setX(newVertex.X.divide(size, Threshold.mc));
            newVertex.setY(newVertex.Y.divide(size, Threshold.mc));
            newVertex.setZ(newVertex.Z.divide(size, Threshold.mc));
            break;
        case LAST_SELECTED:
            if (lastSelectedVertex == null || !vertexLinkedToPositionInFile.containsKey(lastSelectedVertex)) return;
            newVertex = new Vector3d(lastSelectedVertex);
            lastSelectedVertex = null;
            break;
        case NEAREST_EDGE:
        case NEAREST_FACE:
            if (originVerts.size() == 0) return;
            {
                // This is a little bit more complex.
                // First, I had to extend the selection to adjacent data,
                // so the nearest edge will not be adjacent to the (selected) origin vertex

                for (Vertex v : originVerts) {
                    for (VertexManifestation mani : vertexLinkedToPositionInFile.get(v)) {
                        GData gd = mani.getGdata();
                        switch (gd.type()) {
                        case 2:
                            selectedLines.add((GData2) gd);
                            break;
                        case 3:
                            selectedTriangles.add((GData3) gd);
                            break;
                        case 4:
                            selectedQuads.add((GData4) gd);
                            break;
                        case 5:
                            selectedCondlines.add((GData5) gd);
                            break;
                        default:
                            continue;
                        }
                        selectedData.add(gd);
                    }

                    // Then invert the selection, so that getMinimalDistanceVertexToLines() will snap on the target

                    selectInverse(new SelectorSettings());

                    // And using changeVertexDirectFast() to do the merge
                    boolean modified = false;
                    if (mode == MergeTo.NEAREST_EDGE) {
                        for (Vertex vertex : originVerts) {
                            final Vertex[] target = getMinimalDistanceVerticesToLines(vertex);
                            modified = changeVertexDirectFast(vertex, target[2], true) || modified;
                            // And split at target position!
                            modified = split(target[0], target[1], target[2]) || modified;
                        }
                    } else {
                        for (Vertex vertex : originVerts) {
                            final Vertex target = getMinimalDistanceVertexToSurfaces(vertex);
                            modified = changeVertexDirectFast(vertex, target, true) || modified;
                        }
                    }
                    clearSelection();

                    if (modified) {
                        IdenticalVertexRemover.removeIdenticalVertices(this, false);
                        clearSelection();
                        setModified_NoSync();
                    }
                }
            }
            if (syncWithTextEditor) {
                syncWithTextEditors(true);
            }
            return;
        case NEAREST_VERTEX:
            if (originVerts.size() == 0) return;
            {
                float minDist = Float.MAX_VALUE;
                Set<Vertex> allVerticesMinusSelection = new TreeSet<Vertex>();
                allVerticesMinusSelection.addAll(getVertices());
                allVerticesMinusSelection.removeAll(originVerts);
                clearSelection();
                for (Vertex vertex2 : originVerts) {
                    selectedVertices.clear();
                    selectedVertices.add(vertex2);
                    Vertex minVertex = new Vertex(0f, 0f, 0f);
                    Vector4f next = vertex2.toVector4fm();
                    for (Vertex vertex : allVerticesMinusSelection) {
                        Vector4f sub = Vector4f.sub(next, vertex.toVector4fm(), null);
                        float d2 = sub.lengthSquared();
                        if (d2 < minDist) {
                            minVertex = vertex;
                            minDist = d2;
                        }
                    }
                    newVertex = new Vector3d(minVertex);
                    Merger.mergeTo(new Vertex(newVertex), this, false);
                }
                clearSelection();
                setModified_NoSync();
            }
            if (syncWithTextEditor) {
                syncWithTextEditors(true);
            }
            return;
        default:
            return;
        }
        Merger.mergeTo(new Vertex(newVertex), this, syncWithTextEditor);
        clearSelection();
        validateState();
    }

    public boolean split(Vertex start, Vertex end, Vertex target) {

        if (linkedDatFile.isReadOnly()) return false;

        final Set<GData2> newLines = new HashSet<GData2>();
        final Set<GData3> newTriangles = new HashSet<GData3>();
        final Set<GData5> newCondlines = new HashSet<GData5>();

        final Set<GData2> effSelectedLines = new HashSet<GData2>();
        final Set<GData3> effSelectedTriangles = new HashSet<GData3>();
        final Set<GData4> effSelectedQuads = new HashSet<GData4>();
        final Set<GData5> effSelectedCondlines = new HashSet<GData5>();


        final Set<GData2> linesToDelete2 = new HashSet<GData2>();
        final Set<GData3> trisToDelete2 = new HashSet<GData3>();
        final Set<GData4> quadsToDelete2 = new HashSet<GData4>();
        final Set<GData5> clinesToDelete2 = new HashSet<GData5>();

        Set<VertexManifestation> manis1 = vertexLinkedToPositionInFile.get(start);
        Set<VertexManifestation> manis2 = vertexLinkedToPositionInFile.get(end);

        if (manis1 == null || manis2 == null || manis1.isEmpty() || manis2.isEmpty()) return false;

        Set<GData> setA = new HashSet<GData>();
        Set<GData> setB = new HashSet<GData>();

        for (VertexManifestation m : manis1) {
            setA.add(m.getGdata());
        }
        for (VertexManifestation m : manis1) {
            setB.add(m.getGdata());
        }

        setA.retainAll(setB);

        for (GData g : setA) {
            if (!lineLinkedToVertices.containsKey(g)) continue;
            switch (g.type()) {
            case 2:
                effSelectedLines.add((GData2) g);
                break;
            case 3:
                effSelectedTriangles.add((GData3) g);
                break;
            case 4:
                effSelectedQuads.add((GData4) g);
                break;
            case 5:
                effSelectedCondlines.add((GData5) g);
                break;
            default:
                continue;
            }
        }

        for (GData2 g : effSelectedLines) {
            List<GData2> result = split(g, start, end, target);
            newLines.addAll(result);
            for (GData n : result) {
                linkedDatFile.insertAfter(g, n);
            }
            linesToDelete2.add(g);
        }

        for (GData3 g : effSelectedTriangles) {
            List<GData3> result = split(g, start, end, target);
            if (result.isEmpty()) continue;
            newTriangles.addAll(result);
            for (GData n : result) {
                linkedDatFile.insertAfter(g, n);
            }
            trisToDelete2.add(g);
        }

        for (GData4 g : effSelectedQuads) {
            List<GData3> result = split(g, start, end, target);
            if (result.isEmpty()) continue;
            newTriangles.addAll(result);
            for (GData n : result) {
                linkedDatFile.insertAfter(g, n);
            }
            quadsToDelete2.add(g);
        }

        for (GData5 g : effSelectedCondlines) {
            List<GData5> result = split(g, start, end, target);
            newCondlines.addAll(result);
            for (GData n : result) {
                linkedDatFile.insertAfter(g, n);
            }
            clinesToDelete2.add(g);
        }

        if (newLines.size() + newTriangles.size() + newCondlines.size() > 0) {
            setModified_NoSync();
        }

        backupSelection();
        clearSelection();
        selectedLines.addAll(linesToDelete2);
        selectedTriangles.addAll(trisToDelete2);
        selectedQuads.addAll(quadsToDelete2);
        selectedCondlines.addAll(clinesToDelete2);
        selectedData.addAll(selectedLines);
        selectedData.addAll(selectedTriangles);
        selectedData.addAll(selectedQuads);
        selectedData.addAll(selectedCondlines);
        delete(false, false);

        selectedTriangles.addAll(newTriangles);
        selectedData.addAll(selectedTriangles);
        rectify(new RectifierSettings(), false);

        clearSelection();
        restoreSelection();

        validateState();

        return isModified();
    }

    private List<GData5> split(GData5 g, Vertex start, Vertex end, Vertex target) {
        ArrayList<GData5> result = new ArrayList<GData5>();

        if (!start.equals(end)) {
            Vertex[] verts = condlines.get(g);
            if ((verts[0].equals(start) || verts[0].equals(end)) && (verts[1].equals(start) || verts[1].equals(end))) {
                result.add(new GData5(g.colourNumber, g.r, g.g, g.b, g.a,

                        start.X,
                        start.Y,
                        start.Z,

                        target.X,
                        target.Y,
                        target.Z,

                        g.X3,
                        g.Y3,
                        g.Z3,

                        g.X4,
                        g.Y4,
                        g.Z4,

                        View.DUMMY_REFERENCE, linkedDatFile));


                result.add(new GData5(g.colourNumber, g.r, g.g, g.b, g.a,

                        target.X,
                        target.Y,
                        target.Z,

                        end.X,
                        end.Y,
                        end.Z,

                        g.X3,
                        g.Y3,
                        g.Z3,

                        g.X4,
                        g.Y4,
                        g.Z4,

                        View.DUMMY_REFERENCE, linkedDatFile));
            }
        }
        return result;
    }

    private List<GData3> split(GData4 g, Vertex start, Vertex end, Vertex target) {
        ArrayList<GData3> result = new ArrayList<GData3>();
        if (!start.equals(end)) {
            Vertex[] verts = quads.get(g);
            if ((verts[0].equals(start) || verts[0].equals(end)) && (verts[1].equals(start) || verts[1].equals(end))) {
                return splitQuad(verts[0], verts[1], verts[2], verts[3], target, g);
            } else if ((verts[1].equals(start) || verts[1].equals(end)) && (verts[2].equals(start) || verts[2].equals(end))) {
                return splitQuad(verts[1], verts[2], verts[3], verts[0], target, g);
            } else if ((verts[2].equals(start) || verts[2].equals(end)) && (verts[3].equals(start) || verts[3].equals(end))) {
                return splitQuad(verts[2], verts[3], verts[0], verts[1], target, g);
            } else if ((verts[3].equals(start) || verts[3].equals(end)) && (verts[0].equals(start) || verts[0].equals(end))) {
                return splitQuad(verts[3], verts[0], verts[1], verts[2], target, g);
            }
        }
        return result;
    }

    private List<GData3> splitQuad(Vertex v1, Vertex v2, Vertex v3, Vertex v4, Vertex target, GData4 g) {
        ArrayList<GData3> result = new ArrayList<GData3>();
        result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                v4.X,
                v4.Y,
                v4.Z,

                v1.X,
                v1.Y,
                v1.Z,

                target.X,
                target.Y,
                target.Z,

                View.DUMMY_REFERENCE, linkedDatFile));

        result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                target.X,
                target.Y,
                target.Z,

                v2.X,
                v2.Y,
                v2.Z,

                v4.X,
                v4.Y,
                v4.Z,

                View.DUMMY_REFERENCE, linkedDatFile));

        result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,
                v2.X,
                v2.Y,
                v2.Z,

                v3.X,
                v3.Y,
                v3.Z,

                v4.X,
                v4.Y,
                v4.Z,

                View.DUMMY_REFERENCE, linkedDatFile));
        return result;
    }

    private List<GData3> split(GData3 g, Vertex start, Vertex end, Vertex target) {
        ArrayList<GData3> result = new ArrayList<GData3>();
        if (!start.equals(end)) {
            Vertex[] verts = triangles.get(g);
            if ((verts[0].equals(start) || verts[0].equals(end)) && (verts[1].equals(start) || verts[1].equals(end))) {
                return splitTri(verts[0], verts[1], verts[2], target, g);
            } else if ((verts[1].equals(start) || verts[1].equals(end)) && (verts[2].equals(start) || verts[2].equals(end))) {
                return splitTri(verts[1], verts[2], verts[0], target, g);
            } else if ((verts[2].equals(start) || verts[2].equals(end)) && (verts[0].equals(start) || verts[0].equals(end))) {
                return splitTri(verts[2], verts[0], verts[1], target, g);
            }
        }
        return result;
    }

    private List<GData3> splitTri(Vertex v1, Vertex v2, Vertex v3, Vertex target, GData3 g) {
        ArrayList<GData3> result = new ArrayList<GData3>();
        result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                v3.X,
                v3.Y,
                v3.Z,

                v1.X,
                v1.Y,
                v1.Z,

                target.X,
                target.Y,
                target.Z,

                View.DUMMY_REFERENCE, linkedDatFile));

        result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                target.X,
                target.Y,
                target.Z,

                v2.X,
                v2.Y,
                v2.Z,

                v3.X,
                v3.Y,
                v3.Z,

                View.DUMMY_REFERENCE, linkedDatFile));
        return result;
    }

    private List<GData2> split(GData2 g, Vertex start, Vertex end, Vertex target) {
        ArrayList<GData2> result = new ArrayList<GData2>();

        if (!start.equals(end)) {
            Vertex[] verts = lines.get(g);
            if ((verts[0].equals(start) || verts[0].equals(end)) && (verts[1].equals(start) || verts[1].equals(end))) {
                result.add(new GData2(g.colourNumber, g.r, g.g, g.b, g.a,

                        start.X,
                        start.Y,
                        start.Z,

                        target.X,
                        target.Y,
                        target.Z,

                        View.DUMMY_REFERENCE, linkedDatFile));


                result.add(new GData2(g.colourNumber, g.r, g.g, g.b, g.a,

                        target.X,
                        target.Y,
                        target.Z,

                        end.X,
                        end.Y,
                        end.Z,

                        View.DUMMY_REFERENCE, linkedDatFile));
            }
        }
        return result;
    }

    public GData1 reloadSubfile(GData1 g) {
        HashBiMap<Integer, GData> drawPerLine = linkedDatFile.getDrawPerLine_NOCLONE();
        HeaderState.state().setState(HeaderState._99_DONE);
        // Clear the cache..
        GData.parsedLines.clear();
        GData.CACHE_parsedFilesSource.clear();
        GData1 reloadedSubfile = (GData1) DatParser
                .parseLine(g.toString(), drawPerLine.getKey(g).intValue(), 0, 0.5f, 0.5f, 0.5f, 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile, false,
                        new HashSet<String>(), false).get(0).getGraphicalData();
        // Clear the cache..
        GData.parsedLines.clear();
        GData.CACHE_parsedFilesSource.clear();
        GData oldNext = g.getNext();
        GData oldBefore = g.getBefore();
        oldBefore.setNext(reloadedSubfile);
        reloadedSubfile.setNext(oldNext);
        Integer oldNumber = drawPerLine.getKey(g);
        if (oldNumber != null)
            drawPerLine.put(oldNumber, reloadedSubfile);
        remove(g);
        return reloadedSubfile;
    }


    public void transformSubfile(GData1 g, Matrix M, boolean clearSelection, boolean syncWithTextEditor) {
        HashBiMap<Integer, GData> drawPerLine = linkedDatFile.getDrawPerLine_NOCLONE();
        HeaderState.state().setState(HeaderState._99_DONE);
        StringBuilder colourBuilder = new StringBuilder();
        if (g.colourNumber == -1) {
            colourBuilder.append("0x2"); //$NON-NLS-1$
            colourBuilder.append(MathHelper.toHex((int) (255f * g.r)).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * g.g)).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * g.b)).toUpperCase());
        } else {
            colourBuilder.append(g.colourNumber);
        }
        // Clear the cache..
        GData.parsedLines.clear();
        GData.CACHE_parsedFilesSource.clear();
        GData1 reloadedSubfile = (GData1) DatParser
                .parseLine("1 " + colourBuilder.toString() + M.toLDrawString() + g.shortName , 0, 0, 0.5f, 0.5f, 0.5f, 1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile, false, //$NON-NLS-1$
                        new HashSet<String>(), false).get(0).getGraphicalData();
        // Clear the cache..
        GData.parsedLines.clear();
        GData.CACHE_parsedFilesSource.clear();
        GData oldNext = g.getNext();
        GData oldBefore = g.getBefore();
        oldBefore.setNext(reloadedSubfile);
        reloadedSubfile.setNext(oldNext);
        Integer oldNumber = drawPerLine.getKey(g);
        if (oldNumber != null)
            drawPerLine.put(oldNumber, reloadedSubfile);
        remove(g);
        if (clearSelection) {
            clearSelection();
        } else {
            selectedData.remove(g);
            selectedSubfiles.remove(g);
        }
        selectedData.add(reloadedSubfile);
        selectedSubfiles.add(reloadedSubfile);
        selectSubfiles(null, null, false);
        if (syncWithTextEditor) {
            setModified(true, true);
        } else {
            setModified_NoSync();
        }
    }

    public void setXyzOrTranslateOrTransform(Vertex target, Vertex pivot, TransformationMode tm, boolean x, boolean y, boolean z, boolean moveAdjacentData, boolean syncWithTextEditors) {
        if (linkedDatFile.isReadOnly())
            return;

        Matrix transformation = null;
        Vertex offset = null;
        if (tm == TransformationMode.TRANSLATE) offset = new Vertex(target.X, target.Y, target.Z);

        if (pivot == null) pivot = new Vertex(0f, 0f, 0f);

        switch (tm) {
        case ROTATE:
            RotationSnap flag;
            if (x) {
                try {
                    target.X.intValueExact();
                    switch (Math.abs(target.X.intValue())) {
                    case 90:
                        flag = RotationSnap.DEG90;
                        break;
                    case 180:
                        flag = RotationSnap.DEG180;
                        break;
                    case 270:
                        flag = RotationSnap.DEG270;
                        break;
                    case 360:
                        flag = RotationSnap.DEG360;
                        break;
                    default:
                        flag = RotationSnap.COMPLEX;
                        break;
                    }
                } catch (ArithmeticException ae) {
                    flag = RotationSnap.COMPLEX;
                }
                transformation = View.ACCURATE_ID.rotate(target.X.divide(new BigDecimal(180), Threshold.mc).multiply(new BigDecimal(Math.PI)), flag, new BigDecimal[] { BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO });
            } else if (y) {
                try {
                    target.Y.intValueExact();
                    switch (Math.abs(target.Y.intValue())) {
                    case 90:
                        flag = RotationSnap.DEG90;
                        break;
                    case 180:
                        flag = RotationSnap.DEG180;
                        break;
                    case 270:
                        flag = RotationSnap.DEG270;
                        break;
                    case 360:
                        flag = RotationSnap.DEG360;
                        break;
                    default:
                        flag = RotationSnap.COMPLEX;
                        break;
                    }
                } catch (ArithmeticException ae) {
                    flag = RotationSnap.COMPLEX;
                }
                transformation = View.ACCURATE_ID.rotate(target.Y.divide(new BigDecimal(180), Threshold.mc).multiply(new BigDecimal(Math.PI)), flag, new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO });
            } else {
                try {
                    target.Z.intValueExact();
                    switch (Math.abs(target.Z.intValue())) {
                    case 90:
                        flag = RotationSnap.DEG90;
                        break;
                    case 180:
                        flag = RotationSnap.DEG180;
                        break;
                    case 270:
                        flag = RotationSnap.DEG270;
                        break;
                    case 360:
                        flag = RotationSnap.DEG360;
                        break;
                    default:
                        flag = RotationSnap.COMPLEX;
                        break;
                    }
                } catch (ArithmeticException ae) {
                    flag = RotationSnap.COMPLEX;
                }
                transformation = View.ACCURATE_ID.rotate(target.Z.divide(new BigDecimal(180), Threshold.mc).multiply(new BigDecimal(Math.PI)), flag, new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE });
            }
            break;
        case SCALE:
            BigDecimal sx = target.X;
            BigDecimal sy = target.Y;
            BigDecimal sz = target.Z;
            if (sx.compareTo(BigDecimal.ZERO) == 0) sx = new BigDecimal("0.000000001"); //$NON-NLS-1$
            if (sy.compareTo(BigDecimal.ZERO) == 0) sy = new BigDecimal("0.000000001"); //$NON-NLS-1$
            if (sz.compareTo(BigDecimal.ZERO) == 0) sz = new BigDecimal("0.000000001"); //$NON-NLS-1$
            transformation = new Matrix(
                    x ? sx : BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                            BigDecimal.ZERO, y ? sy : BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
                                    BigDecimal.ZERO, BigDecimal.ZERO, z ? sz : BigDecimal.ONE, BigDecimal.ZERO,
                                            BigDecimal.ZERO,  BigDecimal.ZERO,  BigDecimal.ZERO, BigDecimal.ONE);
            break;
        case SET:
            break;
        case TRANSLATE:
            transformation = new Matrix(
                    BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO,
                    offset.X, offset.Y, offset.Z, BigDecimal.ONE);
            break;
        default:
            break;
        }

        final Set<Vertex> singleVertices = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());

        final HashSet<GData0> effSelectedVertices = new HashSet<GData0>();
        final HashSet<GData2> effSelectedLines = new HashSet<GData2>();
        final HashSet<GData3> effSelectedTriangles = new HashSet<GData3>();
        final HashSet<GData4> effSelectedQuads = new HashSet<GData4>();
        final HashSet<GData5> effSelectedCondlines = new HashSet<GData5>();

        selectedData.clear();

        // 0. Deselect selected subfile data (for whole selected subfiles)
        for (GData1 subf : selectedSubfiles) {
            Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
            for (VertexInfo vertexInfo : vis) {
                GData g = vertexInfo.getLinkedData();
                switch (g.type()) {
                case 2:
                    selectedLines.remove(g);
                    break;
                case 3:
                    selectedTriangles.remove(g);
                    break;
                case 4:
                    selectedQuads.remove(g);
                    break;
                case 5:
                    selectedCondlines.remove(g);
                    break;
                default:
                    break;
                }
            }
        }

        // 1. Vertex Based Selection
        {
            final Set<Vertex> objectVertices = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());
            {
                HashMap<GData, Integer> occurMap = new HashMap<GData, Integer>();
                for (Vertex vertex : selectedVertices) {
                    Set<VertexManifestation> occurences = vertexLinkedToPositionInFile.get(vertex);
                    if (occurences == null)
                        continue;
                    boolean isPureSubfileVertex = true;
                    for (VertexManifestation vm : occurences) {
                        GData g = vm.getGdata();
                        int val = 1;
                        if (occurMap.containsKey(g)) {
                            val = occurMap.get(g);
                            val++;
                        }
                        occurMap.put(g, val);
                        switch (g.type()) {
                        case 0:
                            GData0 meta = (GData0) g;
                            boolean idCheck = !lineLinkedToVertices.containsKey(meta);
                            isPureSubfileVertex = isPureSubfileVertex && idCheck;
                            if (val == 1 && !idCheck) {
                                effSelectedVertices.add(meta);
                            }
                            break;
                        case 2:
                            GData2 line = (GData2) g;
                            isPureSubfileVertex = isPureSubfileVertex && !line.parent.equals(View.DUMMY_REFERENCE);
                            break;
                        case 3:
                            GData3 triangle = (GData3) g;
                            isPureSubfileVertex = isPureSubfileVertex && !triangle.parent.equals(View.DUMMY_REFERENCE);
                            break;
                        case 4:
                            GData4 quad = (GData4) g;
                            isPureSubfileVertex = isPureSubfileVertex && !quad.parent.equals(View.DUMMY_REFERENCE);
                            break;
                        case 5:
                            GData5 condline = (GData5) g;
                            isPureSubfileVertex = isPureSubfileVertex && !condline.parent.equals(View.DUMMY_REFERENCE);
                            break;
                        }
                    }
                    if (isPureSubfileVertex)
                        objectVertices.add(vertex);
                }
            }

            // 2. Object Based Selection

            for (GData2 line : selectedLines) {
                if (line.parent.equals(View.DUMMY_REFERENCE))
                    effSelectedLines.add(line);
                Vertex[] verts = lines.get(line);
                if (verts == null)
                    continue;
                for (Vertex vertex : verts) {
                    objectVertices.add(vertex);
                }
            }
            for (GData3 triangle : selectedTriangles) {
                if (triangle.parent.equals(View.DUMMY_REFERENCE))
                    effSelectedTriangles.add(triangle);
                Vertex[] verts = triangles.get(triangle);
                if (verts == null)
                    continue;
                for (Vertex vertex : verts) {
                    objectVertices.add(vertex);
                }
            }
            for (GData4 quad : selectedQuads) {
                if (quad.parent.equals(View.DUMMY_REFERENCE))
                    effSelectedQuads.add(quad);
                Vertex[] verts = quads.get(quad);
                if (verts == null)
                    continue;
                for (Vertex vertex : verts) {
                    objectVertices.add(vertex);
                }
            }
            for (GData5 condline : selectedCondlines) {
                if (condline.parent.equals(View.DUMMY_REFERENCE))
                    effSelectedCondlines.add(condline);
                Vertex[] verts = condlines.get(condline);
                if (verts == null)
                    continue;
                for (Vertex vertex : verts) {
                    objectVertices.add(vertex);
                }
            }

            Set<GData0> vs = new HashSet<GData0>(effSelectedVertices);
            for (GData0 effvert : vs) {
                Vertex v = effvert.getVertex();
                if (v != null && objectVertices.contains(v)) {
                    singleVertices.add(v);
                }
            }

            singleVertices.addAll(objectVertices);
            singleVertices.addAll(selectedVertices);

            // 3. Set XYZ of the selected data
            if (!singleVertices.isEmpty()) {
                setModified_NoSync();
            }
            final Matrix forward = Matrix.mul(View.ACCURATE_ID.translate(new BigDecimal[] { pivot.X.negate(), pivot.Y.negate(), pivot.Z.negate() }), View.ACCURATE_ID);
            final Matrix backward = Matrix.mul(View.ACCURATE_ID.translate(new BigDecimal[] { pivot.X, pivot.Y, pivot.Z }), View.ACCURATE_ID);

            if (tm == TransformationMode.ROTATE || tm == TransformationMode.SCALE) {
                selectedVertices.addAll(singleVertices);
                transform(new HashSet<GData>(), selectedVertices, forward, true, true);
                transform(new HashSet<GData>(), selectedVertices, transformation, true, true);
                transform(new HashSet<GData>(), selectedVertices, backward, true, true);
            }
            selectedData.clear();

            for (Vertex vOld : singleVertices) {
                switch (tm) {
                case ROTATE:
                case SCALE:
                    continue;
                case SET:
                    break;
                case TRANSLATE:
                    target = new Vertex(vOld.X.add(offset.X), vOld.Y.add(offset.Y), vOld.Z.add(offset.Z));
                    break;
                }
                Vertex vNew;
                if (x) {
                    if (y) {
                        if (z) {
                            vNew = new Vertex(target.X, target.Y, target.Z);
                        } else {
                            vNew = new Vertex(target.X, target.Y, vOld.Z);
                        }
                    } else {
                        if (z) {
                            vNew = new Vertex(target.X, vOld.Y, target.Z);
                        } else {
                            vNew = new Vertex(target.X, vOld.Y, vOld.Z);
                        }
                    }
                } else {
                    if (y) {
                        if (z) {
                            vNew = new Vertex(vOld.X, target.Y, target.Z);
                        } else {
                            vNew = new Vertex(vOld.X, target.Y, vOld.Z);
                        }
                    } else {
                        if (z) {
                            vNew = new Vertex(vOld.X, vOld.Y, target.Z);
                        } else {
                            vNew = new Vertex(vOld.X, vOld.Y, vOld.Z);
                        }
                    }
                }
                changeVertexDirectFast(vOld, vNew, moveAdjacentData);
                selectedVertices.add(vNew);
            }

            if ((tm == TransformationMode.TRANSLATE || tm == TransformationMode.SCALE || tm == TransformationMode.ROTATE) && !selectedSubfiles.isEmpty()) {
                setModified_NoSync();
                for (GData1 s : new HashSet<GData1>(selectedSubfiles)) {
                    Matrix M = Matrix.mul(forward, s.accurateLocalMatrix);
                    M = Matrix.mul(transformation, M);
                    M = Matrix.mul(backward, M);
                    transformSubfile(s, M, false, false);
                }
            } else {
                selectedSubfiles.clear();
            }

            if (isModified()) {
                for(Iterator<GData2> it = selectedLines.iterator();it.hasNext();){
                    if (!exist(it.next())) it.remove();
                }
                for(Iterator<GData3> it = selectedTriangles.iterator();it.hasNext();){
                    if (!exist(it.next())) it.remove();
                }
                for(Iterator<GData4> it = selectedQuads.iterator();it.hasNext();){
                    if (!exist(it.next())) it.remove();
                }
                for(Iterator<GData5> it = selectedCondlines.iterator();it.hasNext();){
                    if (!exist(it.next())) it.remove();
                }
                selectedData.addAll(selectedLines);
                selectedData.addAll(selectedTriangles);
                selectedData.addAll(selectedQuads);
                selectedData.addAll(selectedCondlines);
                selectedData.addAll(selectedSubfiles);

                IdenticalVertexRemover.removeIdenticalVertices(this, false);

                if (syncWithTextEditors) syncWithTextEditors(true);
                updateUnsavedStatus();
            }
            selectedVertices.retainAll(vertexLinkedToPositionInFile.keySet());
        }
    }

    public static List<GData> getClipboard() {
        return CLIPBOARD;
    }

    public ArrayList<MemorySnapshot> getSnapshots() {
        return snapshots;
    }

    public void addSnapshot() {
        if (NLogger.DEBUG) {
            MemorySnapshot snapshot = new MemorySnapshot(linkedDatFile);
            getSnapshots().add(snapshot);
            NLogger.debug(getClass(), "CREATED SNAPSHOT ON " + snapshot.toString()); //$NON-NLS-1$
        }
    }

    public void loadSnapshot(MemorySnapshot s) {
        if (NLogger.DEBUG) {
            clear();
            GData0 emptyLine = new GData0(""); //$NON-NLS-1$
            linkedDatFile.getDrawPerLine_NOCLONE().clear();
            linkedDatFile.getDrawChainStart().setNext(emptyLine);
            linkedDatFile.getDrawPerLine_NOCLONE().put(1, emptyLine);
            // FIXME Needs implementation!
            setModified(true, false);
            StringBuilder sb = new StringBuilder();
            for (String line : s.getBackup()) {
                sb.append("\n"); //$NON-NLS-1$
                sb.append(line);
            }
            sb.deleteCharAt(0);
            linkedDatFile.setText(sb.toString());
            SubfileCompiler.compile(linkedDatFile, false, true);
        }
    }

    public void cleanupSelection() {

        selectedData.clear();

        for (Iterator<Vertex> vi = selectedVertices.iterator(); vi.hasNext();) {
            if (!vertexLinkedToPositionInFile.containsKey(vi.next())) {
                vi.remove();
            }
        }

        for (Iterator<GData1> g1i = selectedSubfiles.iterator(); g1i.hasNext();) {
            GData1 g1 = g1i.next();
            if (vertexCountInSubfile.keySet().contains(g1)) {
                selectedData.add(g1);
            } else {
                g1i.remove();
                selectedData.remove(g1);
            }
        }

        for (Iterator<GData2> g2i = selectedLines.iterator(); g2i.hasNext();) {
            GData2 g2 = g2i.next();
            if (lines.keySet().contains(g2)) {
                selectedData.add(g2);
            } else {
                g2i.remove();
                selectedData.remove(g2);
            }
        }

        for (Iterator<GData3> g3i = selectedTriangles.iterator(); g3i.hasNext();) {
            GData3 g3 = g3i.next();
            if (triangles.keySet().contains(g3)) {
                selectedData.add(g3);
            } else {
                g3i.remove();
                selectedData.remove(g3);
            }
        }

        for (Iterator<GData4> g4i = selectedQuads.iterator(); g4i.hasNext();) {
            GData4 g4 = g4i.next();
            if (quads.keySet().contains(g4)) {
                selectedData.add(g4);
            } else {
                g4i.remove();
                selectedData.remove(g4);
            }
        }

        for (Iterator<GData5> g5i = selectedCondlines.iterator(); g5i.hasNext();) {
            GData5 g5 = g5i.next();
            if (condlines.keySet().contains(g5)) {
                selectedData.add(g5);
            } else {
                g5i.remove();
                selectedData.remove(g5);
            }
        }
    }
}
