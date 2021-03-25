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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Event;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.enums.ObjectMode;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.Cocoa;
import org.nschmidt.ldparteditor.helpers.LDPartEditorException;
import org.nschmidt.ldparteditor.helpers.composite3d.PerspectiveCalculator;
import org.nschmidt.ldparteditor.helpers.composite3d.SelectorSettings;
import org.nschmidt.ldparteditor.helpers.math.HashBiMap;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.helpers.math.PowerRay;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeTreeMap;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.text.DatParser;
import org.nschmidt.ldparteditor.text.StringHelper;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

class VM01SelectHelper extends VM01Select {

    protected VM01SelectHelper(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    /**
     * @return {@code true} if the selection did not use a rubber band
     */
    public synchronized boolean selectVertices(final Composite3D c3d, boolean addSomething, boolean forceRayTest) {
        final boolean noTrans = Editor3DWindow.getWindow().hasNoTransparentSelection();
        final boolean noCondlineVerts = !c3d.isShowingCondlineControlPoints();
        if (!(c3d.getKeys().isCtrlPressed() || (Cocoa.isCocoa && c3d.getKeys().isCmdPressed())) && !addSomething || addSomething) {
            clearSelection2();
        }
        final Vector4f selectionStart = new Vector4f(c3d.getSelectionStart());
        final Vector4f selectionWidth = new Vector4f(c3d.getSelectionWidth());
        final Vector4f selectionHeight = new Vector4f(c3d.getSelectionHeight());
        final Vector4f selectionDepth;

        final boolean needRayTest;
        {
            boolean needRayTest2 = false;
            if (Math.abs(selectionWidth.x) < 0.001f && Math.abs(selectionWidth.y) < 0.001f && Math.abs(selectionWidth.z) < 0.001f)
                needRayTest2 = true;
            if (Math.abs(selectionHeight.x) < 0.001f && Math.abs(selectionHeight.y) < 0.001f && Math.abs(selectionHeight.z) < 0.001f)
                needRayTest2 = true;
            needRayTest = needRayTest2 || forceRayTest;
        }

        if (needRayTest) {

            Vector4f zAxis4f = new Vector4f(0, 0, c3d.hasNegDeterminant() ^ c3d.isWarpedSelection() ? -1f : 1f, 1f);
            Matrix4f ovr_inverse2 = Matrix4f.invert(c3d.getRotation(), null);
            Matrix4f.transform(ovr_inverse2, zAxis4f, zAxis4f);
            selectionDepth = (Vector4f) new Vector4f(zAxis4f.x, zAxis4f.y, zAxis4f.z, 0f).normalise();
            selectionDepth.w = 1f;
            final float discr = 1f / c3d.getZoom();

            final long complexity = c3d.isShowingHiddenVertices() ? vertexLinkedToPositionInFile.size() : vertexLinkedToPositionInFile.size() * ((long) triangles.size() + (long) quads.size());
            if (complexity < View.NUM_CORES * 100L) {
                for (Vertex vertex : vertexLinkedToPositionInFile.keySet()) {
                    if (hiddenVertices.contains(vertex) || noCondlineVerts && isPureCondlineControlPoint(vertex))
                        continue;
                    MathHelper.crossProduct(selectionDepth, Vector4f.sub(vertex.toVector4f(), selectionStart, null), selectionWidth);
                    if (selectionWidth.x * selectionWidth.x + selectionWidth.y * selectionWidth.y + selectionWidth.z * selectionWidth.z < discr) {
                        selectVertices_helper(c3d, vertex, selectionDepth, powerRay, noTrans, needRayTest);
                    }
                }
            } else { // Multithreaded selection for many faces
                backupSelection();
                final int chunks = View.NUM_CORES;
                final Thread[] threads = new Thread[chunks];
                final AtomicBoolean dialogCanceled = new AtomicBoolean(false);
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
                                if (hiddenVertices.contains(vertex) || noCondlineVerts && isPureCondlineControlPoint(vertex))
                                    continue;
                                MathHelper.crossProduct(selectionDepth, Vector4f.sub(vertex.toVector4f(), selectionStart, null), result);
                                if (result.x * result.x + result.y * result.y + result.z * result.z < discr) {
                                    if (dialogCanceled.get()) return;
                                    selectVertices_helper(c3d, vertex, selectionDepth, powerRay, noTrans, needRayTest);
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
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new LDPartEditorException(ie);
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
                                try
                                {
                                    m.beginTask(I18n.VM_Selecting, IProgressMonitor.UNKNOWN);
                                    boolean isRunning = true;
                                    while (isRunning) {
                                        try {
                                            Thread.sleep(100);
                                        } catch (InterruptedException ie) {
                                            Thread.currentThread().interrupt();
                                            throw new LDPartEditorException(ie);
                                        }
                                        isRunning = false;
                                        for (Thread thread : threads) {
                                            if (thread.isAlive()) {
                                                isRunning = true;
                                            }
                                        }
                                        if (m.isCanceled()) {
                                            dialogCanceled.set(true);
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
                    } catch (InvocationTargetException consumed) {
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new LDPartEditorException(ie);
                    }
                    linkedDatFile.setDrawSelection(true);
                }
            }
        } else {
            selectionDepth = new Vector4f();
            MathHelper.crossProduct(selectionHeight, selectionWidth, selectionDepth);
            selectionDepth.w = 0f;
            selectionDepth.normalise();
            if (c3d.hasNegDeterminant() ^ c3d.isWarpedSelection()) {
                selectionDepth.negate();
            }
            selectionDepth.w = 1f;

            final long complexity = c3d.isShowingHiddenVertices() ? vertexLinkedToPositionInFile.size() : vertexLinkedToPositionInFile.size() * ((long) triangles.size() + (long) quads.size());
            if (complexity < View.NUM_CORES * 100L) {
                float[][] A = new float[3][3];
                float[] b = new float[3];
                for (Vertex vertex : vertexLinkedToPositionInFile.keySet()) {
                    if (hiddenVertices.contains(vertex) || noCondlineVerts && isPureCondlineControlPoint(vertex))
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
                        selectVertices_helper(c3d, vertex, selectionDepth, powerRay, noTrans, needRayTest);
                    }
                }
            } else {  // Multithreaded selection for many, many faces
                backupSelection();
                final int chunks = View.NUM_CORES;
                final Thread[] threads = new Thread[chunks];
                final AtomicBoolean dialogCanceled = new AtomicBoolean(false);
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
                                if (hiddenVertices.contains(vertex) || noCondlineVerts && isPureCondlineControlPoint(vertex))
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
                                    if (dialogCanceled.get()) return;
                                    selectVertices_helper(c3d, vertex, selectionDepth, powerRay, noTrans, needRayTest);
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
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new LDPartEditorException(ie);
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
                                try
                                {
                                    m.beginTask(I18n.VM_Selecting, IProgressMonitor.UNKNOWN);
                                    boolean isRunning = true;
                                    while (isRunning) {
                                        try {
                                            Thread.sleep(100);
                                        } catch (InterruptedException ie) {
                                            Thread.currentThread().interrupt();
                                            throw new LDPartEditorException(ie);
                                        }
                                        isRunning = false;
                                        for (Thread thread : threads) {
                                            if (thread.isAlive())
                                                isRunning = true;
                                        }
                                        if (m.isCanceled()) {
                                            dialogCanceled.set(true);
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
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new LDPartEditorException(ie);
                    }
                    linkedDatFile.setDrawSelection(true);
                }
            }
        }
        if (addSomething) {
            TreeSet<Vertex> nearVertices = new TreeSet<>();
            float zoom = c3d.getZoom() * 1000f;
            NLogger.debug(getClass(), zoom);
            final PerspectiveCalculator pc = c3d.getPerspectiveCalculator();
            ArrayList<Vector4f> vertsOnScreen = new ArrayList<>(selectedVertices.size());
            ArrayList<Vertex> verts = new ArrayList<>(selectedVertices.size());
            for (Vertex v : selectedVertices) {
                vertsOnScreen.add(pc.getScreenCoordinatesFrom3D(v.x, v.y, v.z));
                verts.add(v);
            }
            final float EPSILON_SQR = (float) Math.pow(WorkbenchManager.getUserSettingState().getFuzziness2D(), 2.0);
            NLogger.debug(getClass(), "2D EPSILON² around selection is {0}", EPSILON_SQR); //$NON-NLS-1$
            if (EPSILON_SQR > 1f) {
                final ArrayList<Vector4f> nearVertices2 = new ArrayList<>();
                final int size = selectedVertices.size();
                for (int i = 0; i < size; i++) {
                    final Vector4f sv = vertsOnScreen.get(i);
                    final float svx = sv.x;
                    final float svy = sv.y;
                    boolean isNear = false;
                    for (Vector4f sv2 : nearVertices2) {
                        final float dx = svx - sv2.x;
                        final float dy = svy - sv2.y;
                        float dist = dx * dx + dy * dy;
                        // NLogger.debug(getClass(), "DIST is {0}", dist); //$NON-NLS-1$
                        if (dist < EPSILON_SQR) {
                            isNear = true;
                            break;
                        }
                    }
                    nearVertices2.add(sv);
                    if (!isNear) {
                        nearVertices.add(verts.get(i));
                    }
                }
                selectedVertices.clear();
                selectedVertices.addAll(nearVertices);
            }

            if (BigDecimal.ZERO.compareTo(WorkbenchManager.getUserSettingState().getFuzziness3D()) < 0) {
                nearVertices.clear();
                final ArrayList<Vertex> nearVertices2 = new ArrayList<>();
                BigDecimal EPSILON;
                EPSILON = WorkbenchManager.getUserSettingState().getFuzziness3D();
                EPSILON = EPSILON.multiply(EPSILON, Threshold.mc);
                NLogger.debug(getClass(), "3D EPSILON² around selection is {0}", EPSILON); //$NON-NLS-1$
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
            }

        } else if (Editor3DWindow.getWindow().isMovingAdjacentData() && Editor3DWindow.getWindow().getWorkingType() == ObjectMode.VERTICES) {
            {
                HashMap<GData, Integer> occurMap = new HashMap<>();
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
                        default:
                            break;
                        }
                    }
                }
            }
        }
        return needRayTest;
    }

    /**
     * ONLY FOR SELECT SUBFILES
     * @param c3d
     */
    private synchronized void selectVertices2(final Composite3D c3d) {
        final boolean noTrans = Editor3DWindow.getWindow().hasNoTransparentSelection();
        final boolean noCondlineVerts = !c3d.isShowingCondlineControlPoints();

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

            Vector4f zAxis4f = new Vector4f(0, 0, c3d.hasNegDeterminant() ^ c3d.isWarpedSelection() ? -1f : 1f, 1f);
            Matrix4f ovr_inverse2 = Matrix4f.invert(c3d.getRotation(), null);
            Matrix4f.transform(ovr_inverse2, zAxis4f, zAxis4f);
            selectionDepth = (Vector4f) new Vector4f(zAxis4f.x, zAxis4f.y, zAxis4f.z, 0f).normalise();
            selectionDepth.w = 1f;
            final float discr = 1f / c3d.getZoom();

            final long complexity = c3d.isShowingHiddenVertices() ? vertexLinkedToPositionInFile.size() : vertexLinkedToPositionInFile.size() * ((long) triangles.size() + (long) quads.size());
            if (complexity < View.NUM_CORES * 100L) {
                for (Vertex vertex : vertexLinkedToPositionInFile.keySet()) {
                    if (hiddenVertices.contains(vertex) || noCondlineVerts && isPureCondlineControlPoint(vertex))
                        continue;
                    MathHelper.crossProduct(selectionDepth, Vector4f.sub(vertex.toVector4f(), selectionStart, null), selectionWidth);
                    if (selectionWidth.x * selectionWidth.x + selectionWidth.y * selectionWidth.y + selectionWidth.z * selectionWidth.z < discr) {
                        selectVertices2_helper(c3d, vertex, selectionDepth, powerRay, noTrans);
                    }
                }
            } else { // Multithreaded selection for many faces
                final int chunks = View.NUM_CORES;
                final Thread[] threads = new Thread[chunks];
                final AtomicBoolean dialogCanceled = new AtomicBoolean(false);
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
                                if (hiddenVertices.contains(vertex) || noCondlineVerts && isPureCondlineControlPoint(vertex))
                                    continue;
                                MathHelper.crossProduct(selectionDepth, Vector4f.sub(vertex.toVector4f(), selectionStart, null), result);
                                if (result.x * result.x + result.y * result.y + result.z * result.z < discr) {
                                    if (dialogCanceled.get()) return;
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
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new LDPartEditorException(ie);
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
                                try
                                {
                                    m.beginTask(I18n.VM_Selecting, IProgressMonitor.UNKNOWN);
                                    boolean isRunning = true;
                                    while (isRunning) {
                                        try {
                                            Thread.sleep(100);
                                        } catch (InterruptedException ie) {
                                            Thread.currentThread().interrupt();
                                            throw new LDPartEditorException(ie);
                                        }
                                        isRunning = false;
                                        for (Thread thread : threads) {
                                            if (thread.isAlive())
                                                isRunning = true;
                                        }
                                        if (m.isCanceled()) {
                                            dialogCanceled.set(true);
                                        }
                                    }
                                }
                                finally
                                {
                                    if (m.isCanceled()) {
                                        clearSelection2();
                                    }
                                    m.done();
                                }
                            }
                        });
                    } catch (InvocationTargetException consumed) {
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new LDPartEditorException(ie);
                    }
                    linkedDatFile.setDrawSelection(true);
                }
            }
        } else {
            selectionDepth = new Vector4f();
            MathHelper.crossProduct(selectionHeight, selectionWidth, selectionDepth);
            selectionDepth.w = 0f;
            selectionDepth.normalise();
            if (c3d.hasNegDeterminant() ^ c3d.isWarpedSelection()) {
                selectionDepth.negate();
            }
            selectionDepth.w = 1f;

            final long complexity = c3d.isShowingHiddenVertices() ? vertexLinkedToPositionInFile.size() : vertexLinkedToPositionInFile.size() * ((long) triangles.size() + (long) quads.size());
            if (complexity < View.NUM_CORES * 100L) {
                float[][] A = new float[3][3];
                float[] b = new float[3];
                for (Vertex vertex : vertexLinkedToPositionInFile.keySet()) {
                    if (hiddenVertices.contains(vertex) || noCondlineVerts && isPureCondlineControlPoint(vertex))
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
                final AtomicBoolean dialogCanceled = new AtomicBoolean(false);
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
                                if (hiddenVertices.contains(vertex) || noCondlineVerts && isPureCondlineControlPoint(vertex))
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
                                    if (dialogCanceled.get()) return;
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
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new LDPartEditorException(ie);
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
                                try
                                {
                                    m.beginTask(I18n.VM_Selecting, IProgressMonitor.UNKNOWN);
                                    boolean isRunning = true;
                                    while (isRunning) {
                                        try {
                                            Thread.sleep(100);
                                        } catch (InterruptedException ie) {
                                            Thread.currentThread().interrupt();
                                            throw new LDPartEditorException(ie);
                                        }
                                        isRunning = false;
                                        for (Thread thread : threads) {
                                            if (thread.isAlive())
                                                isRunning = true;
                                        }
                                        if (m.isCanceled()) {
                                            dialogCanceled.set(true);
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
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new LDPartEditorException(ie);
                    }
                    linkedDatFile.setDrawSelection(true);
                }
            }
        }
    }

    private void selectVertices_helper(final Composite3D c3d, final Vertex vertex, final Vector4f rayDirection, PowerRay powerRay, boolean noTrans, boolean needRayTest) {
        final Set<GData3> tris = triangles.keySet();
        final Set<GData4> qs = quads.keySet();
        if (c3d.isShowingHiddenVertices()) {
            if (selectedVertices.contains(vertex)) {
                if (needRayTest || c3d.getKeys().isAltPressed())  {
                    selectedVertices.remove(vertex);
                }
            } else {
                selectedVertices.add(vertex);
                if (Editor3DWindow.getWindow().getWorkingType() == ObjectMode.VERTICES) lastSelectedVertex = vertex;
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
                    if (needRayTest || c3d.getKeys().isAltPressed()) {
                        selectedVertices.remove(vertex);
                    }
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

    public synchronized void selectLines(Composite3D c3d, SelectorSettings sels) {
        final boolean noTrans = Editor3DWindow.getWindow().hasNoTransparentSelection();
        if (!(c3d.getKeys().isCtrlPressed() || (Cocoa.isCocoa && c3d.getKeys().isCmdPressed()))) {
            clearSelection2();
        }
        Set<Vertex> selectedVerticesTemp = Collections.newSetFromMap(new ThreadsafeTreeMap<>());
        selectedVerticesTemp.addAll(selectedVertices);
        selectedVertices.clear();
        Vector4f selectionWidth = new Vector4f(c3d.getSelectionWidth());
        Vector4f selectionHeight = new Vector4f(c3d.getSelectionHeight());
        selectVertices(c3d, false, false);
        boolean allVertsFromLine = false;
        boolean needRayTest = false;
        if (Math.abs(selectionWidth.x) < 0.001f && Math.abs(selectionWidth.y) < 0.001f && Math.abs(selectionWidth.z) < 0.001f)
            needRayTest = true;
        if (Math.abs(selectionHeight.x) < 0.001f && Math.abs(selectionHeight.y) < 0.001f && Math.abs(selectionHeight.z) < 0.001f)
            needRayTest = true;

        if (selectedVertices.size() < 2 || needRayTest) {
            if (selectedVertices.size() == 1) {
                Vertex selectedVertex = selectedVertices.iterator().next();
                if (sels.isLines() || !sels.isCondlines()) {
                     for (GData2 line : lines.keySet()) {
                        if (hiddenData.contains(line))
                            continue;
                        for (Vertex tvertex : lines.get(line)) {
                            if (selectedVertex.equals(tvertex)) {
                                if (selectedLines.contains(line)) {
                                    if (needRayTest || c3d.getKeys().isAltPressed()) selectedData.remove(line);
                                    if (needRayTest || c3d.getKeys().isAltPressed()) selectedLines.remove(line);
                                } else {
                                    selectedData.add(line);
                                    selectedLines.add(line);
                                }
                            }
                        }
                    }
                }
                if (sels.isCondlines() || !sels.isLines()) {
                    for (GData5 line : condlines.keySet()) {
                        if (hiddenData.contains(line))
                            continue;
                        for (Vertex tvertex : condlines.get(line)) {
                            if (selectedVertex.equals(tvertex)) {
                                if (selectedCondlines.contains(line)) {
                                    if (needRayTest || c3d.getKeys().isAltPressed()) selectedData.remove(line);
                                    if (needRayTest || c3d.getKeys().isAltPressed()) selectedCondlines.remove(line);
                                } else {
                                    selectedData.add(line);
                                    selectedCondlines.add(line);
                                }
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
                // x(u) = a + (g - a)u

                // Difference
                // x(t) - x(u) = (s - a) + dt + (a - g)u
                // x(t) - x(u) = e + dt + f u

                float[] a = new float[3];
                float[] g = new float[3];
                float[] e = new float[3];
                float[] f = new float[3];

                float[][] M = new float[2][2];
                float[] b = new float[] { 0f, 0f };
                if (sels.isLines() || !sels.isCondlines()) {
                    for (GData2 line : lines.keySet()) {
                        if (hiddenData.contains(line))
                            continue;
                        allVertsFromLine = false;
                        for (Vertex tvertex : lines.get(line)) {
                            if (allVertsFromLine) { // b
                                f[0] = a[0] - tvertex.x;
                                f[1] = a[1] - tvertex.y;
                                f[2] = a[2] - tvertex.z;
                                g[0] = tvertex.x;
                                g[1] = tvertex.y;
                                g[2] = tvertex.z;
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
                                float distanceSquared = (float) (Math.pow(e[0] + d[0] * solution[0] + f[0] * solution[1], 2) + Math.pow(e[1] + d[1] * solution[0] + f[1] * solution[1], 2) + Math.pow(e[2] + d[2] * solution[0] + f[2] * solution[1], 2));
                                if (distanceSquared < discr) {
                                    if (!isVertexVisible(c3d, new Vertex(MathHelper.getNearestPointToLineSegment(a[0], a[1], a[2], g[0], g[1], g[2], a[0] - f[0] * solution[1], a[1] - f[1] * solution[1], a[2] - f[2] * solution[1])), selectionDepth, noTrans))
                                        continue;
                                    if (selectedLines.contains(line)) {
                                        if (needRayTest || c3d.getKeys().isAltPressed()) selectedData.remove(line);
                                        if (needRayTest || c3d.getKeys().isAltPressed()) selectedLines.remove(line);
                                    } else {
                                        selectedData.add(line);
                                        selectedLines.add(line);
                                    }
                                }
                            }
                        } catch (RuntimeException re1) {
                        }
                    }
                }
                if (sels.isCondlines() || !sels.isLines()) {
                    for (GData5 line : condlines.keySet()) {
                        if (hiddenData.contains(line))
                            continue;
                        allVertsFromLine = false;
                        for (Vertex tvertex : condlines.get(line)) {
                            if (allVertsFromLine) { // b
                                f[0] = a[0] - tvertex.x;
                                f[1] = a[1] - tvertex.y;
                                f[2] = a[2] - tvertex.z;
                                g[0] = tvertex.x;
                                g[1] = tvertex.y;
                                g[2] = tvertex.z;
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
                                    if (!isVertexVisible(c3d, new Vertex(MathHelper.getNearestPointToLineSegment(a[0], a[1], a[2], g[0], g[1], g[2], a[0] - f[0] * solution[1], a[1] - f[1] * solution[1], a[2] - f[2] * solution[1])), selectionDepth, noTrans))
                                        continue;
                                    if (selectedCondlines.contains(line)) {
                                        if (needRayTest || c3d.getKeys().isAltPressed()) selectedData.remove(line);
                                        if (needRayTest || c3d.getKeys().isAltPressed()) selectedCondlines.remove(line);
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
            }
        } else {
            if (sels.isLines() || !sels.isCondlines()) {
                for (GData2 line : lines.keySet()) {
                    if (hiddenData.contains(line))
                        continue;
                    allVertsFromLine = false;
                    for (Vertex tvertex : lines.get(line)) {
                        if (!selectedVertices.contains(tvertex))
                            break;
                        if (allVertsFromLine) {
                            if (selectedLines.contains(line)) {
                                if (needRayTest || c3d.getKeys().isAltPressed()) selectedData.remove(line);
                                if (needRayTest || c3d.getKeys().isAltPressed()) selectedLines.remove(line);
                            } else {
                                selectedData.add(line);
                                selectedLines.add(line);
                            }
                        }
                        allVertsFromLine = true;
                    }
                }
            }
            if (sels.isCondlines() || !sels.isLines()) {
                for (GData5 line : condlines.keySet()) {
                    if (hiddenData.contains(line))
                        continue;
                    allVertsFromLine = false;
                    for (Vertex tvertex : condlines.get(line)) {
                        if (!selectedVertices.contains(tvertex))
                            break;
                        if (allVertsFromLine) {
                            if (selectedCondlines.contains(line)) {
                                if (needRayTest || c3d.getKeys().isAltPressed()) selectedData.remove(line);
                                if (needRayTest || c3d.getKeys().isAltPressed()) selectedCondlines.remove(line);
                            } else {
                                selectedData.add(line);
                                selectedCondlines.add(line);
                            }
                        }
                        allVertsFromLine = true;
                    }
                }
            }
        }
        selectedVertices.clear();
        selectedVertices.addAll(selectedVerticesTemp);
    }

    /**
     * ONLY FOR SELECT SUBFILES
     * @param c3d
     * @param selectionHeight
     * @param selectionWidth
     */
    private synchronized void selectLines2(Composite3D c3d, Vector4f selectionWidth, Vector4f selectionHeight) {
        final boolean noTrans = Editor3DWindow.getWindow().hasNoTransparentSelection();
        Set<Vertex> tmpVerts = Collections.newSetFromMap(new ThreadsafeTreeMap<>());
        tmpVerts.addAll(selectedVerticesForSubfile);
        selectedVerticesForSubfile.clear();
        selectVertices2(c3d);
        boolean allVertsFromLine = false;
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
                // x(u) = a + (g - a)u

                // Difference
                // x(t) - x(u) = (s - a) + dt + (a - g)u
                // x(t) - x(u) = e + dt + f u

                float[] a = new float[3];
                float[] g = new float[3];
                float[] e = new float[3];
                float[] f = new float[3];

                float[][] M = new float[2][2];
                float[] b = new float[] { 0f, 0f };
                for (GData2 line : lines.keySet()) {
                    if (hiddenData.contains(line))
                        continue;
                    allVertsFromLine = false;
                    for (Vertex tvertex : lines.get(line)) {
                        if (allVertsFromLine) { // b
                            f[0] = a[0] - tvertex.x;
                            f[1] = a[1] - tvertex.y;
                            f[2] = a[2] - tvertex.z;
                            g[0] = tvertex.x;
                            g[1] = tvertex.y;
                            g[2] = tvertex.z;
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
                                if (!isVertexVisible(c3d, new Vertex(MathHelper.getNearestPointToLineSegment(a[0], a[1], a[2], g[0], g[1], g[2], a[0] - f[0] * solution[1], a[1] - f[1] * solution[1], a[2] - f[2] * solution[1])), selectionDepth, noTrans))
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
                            g[0] = tvertex.x;
                            g[1] = tvertex.y;
                            g[2] = tvertex.z;
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
                                if (!isVertexVisible(c3d, new Vertex(MathHelper.getNearestPointToLineSegment(a[0], a[1], a[2], g[0], g[1], g[2], a[0] - f[0] * solution[1], a[1] - f[1] * solution[1], a[2] - f[2] * solution[1])), selectionDepth, noTrans))
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

    public synchronized void selectFaces(Composite3D c3d, Event event, SelectorSettings sels) {
        if (!(c3d.getKeys().isCtrlPressed() || (Cocoa.isCocoa && c3d.getKeys().isCmdPressed()))) {
            clearSelection2();
        }
        Set<Vertex> selectedVerticesTemp = Collections.newSetFromMap(new ThreadsafeTreeMap<>());
        selectedVerticesTemp.addAll(selectedVertices);
        selectedVertices.clear();
        boolean allVertsFromLine = false;
        Vector4f selectionWidth = new Vector4f(c3d.getSelectionWidth());
        Vector4f selectionHeight = new Vector4f(c3d.getSelectionHeight());
        selectVertices(c3d, false, false);
        boolean needRayTest = false;
        if (Math.abs(selectionWidth.x) < 0.001f && Math.abs(selectionWidth.y) < 0.001f && Math.abs(selectionWidth.z) < 0.001f)
            needRayTest = true;
        if (Math.abs(selectionHeight.x) < 0.001f && Math.abs(selectionHeight.y) < 0.001f && Math.abs(selectionHeight.z) < 0.001f)
            needRayTest = true;
        if (selectedVertices.size() < 2 || needRayTest) {
            if (selectedVertices.size() == 1) {
                Vertex selectedVertex = selectedVertices.iterator().next();
                if (sels.isTriangles() || !sels.isQuads()) {
                    for (GData3 line : triangles.keySet()) {
                        if (hiddenData.contains(line))
                            continue;
                        for (Vertex tvertex : triangles.get(line)) {
                            if (selectedVertex.equals(tvertex)) {
                                if (selectedTriangles.contains(line)) {
                                    if (needRayTest || c3d.getKeys().isAltPressed()) selectedData.remove(line);
                                    if (needRayTest || c3d.getKeys().isAltPressed()) selectedTriangles.remove(line);
                                } else {
                                    selectedData.add(line);
                                    selectedTriangles.add(line);
                                }
                            }
                        }
                    }
                }
                if (sels.isQuads() || !sels.isTriangles()) {
                    for (GData4 line : quads.keySet()) {
                        if (hiddenData.contains(line))
                            continue;
                        for (Vertex tvertex : quads.get(line)) {
                            if (selectedVertex.equals(tvertex)) {
                                if (selectedQuads.contains(line)) {
                                    if (needRayTest || c3d.getKeys().isAltPressed()) selectedData.remove(line);
                                    if (needRayTest || c3d.getKeys().isAltPressed()) selectedQuads.remove(line);
                                } else {
                                    selectedData.add(line);
                                    selectedQuads.add(line);
                                }
                            }
                        }
                    }
                }
            } else {
                GData selection = selectFaces_helper(c3d, event);
                if (selection != null) {
                    if (selection.type() == 4 && sels.isQuads()) {
                        GData4 gd4 = (GData4) selection;
                        if (selectedQuads.contains(gd4)) {
                            if (needRayTest || c3d.getKeys().isAltPressed()) selectedData.remove(gd4);
                            if (needRayTest || c3d.getKeys().isAltPressed()) selectedQuads.remove(gd4);
                        } else {
                            selectedData.add(gd4);
                            selectedQuads.add(gd4);
                        }
                    } else if (selection.type() == 3 && (sels.isTriangles() || !sels.isQuads())) {
                        GData3 gd3 = (GData3) selection;
                        if (selectedTriangles.contains(gd3)) {
                            if (needRayTest || c3d.getKeys().isAltPressed()) selectedData.remove(gd3);
                            if (needRayTest || c3d.getKeys().isAltPressed()) selectedTriangles.remove(gd3);
                        } else {
                            selectedData.add(gd3);
                            selectedTriangles.add(gd3);
                        }
                    }
                }
            }
        } else {
            if (sels.isTriangles() || !sels.isQuads()) {
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
                            if (needRayTest || c3d.getKeys().isAltPressed()) selectedData.remove(line);
                            if (needRayTest || c3d.getKeys().isAltPressed()) selectedTriangles.remove(line);
                        } else {
                            selectedData.add(line);
                            selectedTriangles.add(line);
                        }
                    }
                }
            }
            if (sels.isQuads() || !sels.isTriangles()) {
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
                            if (needRayTest || c3d.getKeys().isAltPressed()) selectedData.remove(line);
                            if (needRayTest || c3d.getKeys().isAltPressed()) selectedQuads.remove(line);
                        } else {
                            selectedData.add(line);
                            selectedQuads.add(line);
                        }
                    }
                }
            }
        }
        selectedVertices.clear();
        selectedVertices.addAll(selectedVerticesTemp);
    }

    /**
     * ONLY FOR SELECT SUBFILES
     * @param c3d
     * @param event
     * @param selectionHeight
     * @param selectionWidth
     */
    private synchronized void selectFaces2(Composite3D c3d, Event event, Vector4f selectionWidth, Vector4f selectionHeight) {
        Set<Vertex> selVert4sTemp = Collections.newSetFromMap(new ThreadsafeTreeMap<>());
        selVert4sTemp.addAll(selectedVerticesForSubfile);
        selectedVerticesForSubfile.clear();
        selectVertices2(c3d);
        boolean allVertsFromLine = false;
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
                    } else if (selection.type() == 3) {
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
                    if (triangle.isTriangle) minDist = dist[0];
                    if (triangle.isTriangle || result == null) result = triangle;
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

    synchronized void selectWholeSubfiles() {
        HashSet<GData1> subfilesToAdd = new HashSet<>();
        for (GData g : selectedData) {
            subfilesToAdd.add(g.parent.firstRef);
        }
        subfilesToAdd.remove(View.DUMMY_REFERENCE);
        for (GData1 g : subfilesToAdd) {
            removeSubfileFromSelection(g);
        }
        for (GData1 g : subfilesToAdd) {
            addSubfileToSelection(g);
        }
    }

    public void removeSubfileFromSelection(GData1 subf) {
        selectedData.remove(subf);
        selectedSubfiles.remove(subf);
        Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
        if (vis == null) return;
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

    public void addSubfileToSelection(GData1 subf) {
        selectedData.add(subf);
        selectedSubfiles.add(subf);
        Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
        if (vis == null) return;
        for (VertexInfo vertexInfo : vis) {
            selectedVertices.add(vertexInfo.getVertex());
            GData g = vertexInfo.getLinkedData();
            selectedData.add(g);
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

    public synchronized void selectSubfiles(Composite3D c3d, Event event) {

        selectedVerticesForSubfile.clear();
        selectedLinesForSubfile.clear();
        selectedTrianglesForSubfile.clear();
        selectedQuadsForSubfile.clear();
        selectedCondlinesForSubfile.clear();

        HashSet<GData1> backupSubfiles = new HashSet<>(selectedSubfiles);

        if (!(c3d.getKeys().isCtrlPressed() || (Cocoa.isCocoa && c3d.getKeys().isCmdPressed()))) {
            clearSelection2();
            backupSubfiles.clear();
        }

        backupSelection();
        clearSelection();
        {
            final Vector4f selectionWidth = new Vector4f(c3d.getSelectionWidth());
            final Vector4f selectionHeight = new Vector4f(c3d.getSelectionHeight());
            selectFaces2(c3d, event, selectionWidth, selectionHeight);
            selectLines2(c3d, selectionWidth, selectionHeight);
        }

        // Determine which subfiles were selected

        selectedData.addAll(selectedLinesForSubfile);
        selectedData.addAll(selectedTrianglesForSubfile);
        selectedData.addAll(selectedQuadsForSubfile);
        selectedData.addAll(selectedCondlinesForSubfile);

        selectedVerticesForSubfile.clear();
        selectedLinesForSubfile.clear();
        selectedTrianglesForSubfile.clear();
        selectedQuadsForSubfile.clear();
        selectedCondlinesForSubfile.clear();

        if (NLogger.DEBUG) {
            NLogger.debug(getClass(), "Selected data:"); //$NON-NLS-1$
            for (GData g : selectedData) {
                NLogger.debug(getClass(), g.toString());
            }
        }

        NLogger.debug(getClass(), "Subfiles in selection:"); //$NON-NLS-1$
        HashSet<GData1> subs = new HashSet<>();
        for (GData g : selectedData) {
            GData1 s = g.parent.firstRef;
            if (!View.DUMMY_REFERENCE.equals(s)) {
                subs.add(s);
            }
        }
        for (GData g : subs) {
            NLogger.debug(getClass(), g.toString());
        }

        selectedData.clear();

        NLogger.debug(getClass(), "Subfiles in selection, to add/remove:"); //$NON-NLS-1$
        HashSet<GData1> subsToAdd = new HashSet<>();
        HashSet<GData1> subsToRemove = new HashSet<>();
        if (c3d.getKeys().isCtrlPressed()) {
            for (GData1 subf : backupSubfiles) {
                if (subs.contains(subf)) {
                    subsToRemove.add(subf);
                } else {
                    subsToAdd.add(subf);
                }
            }
            for (GData1 subf : subs) {
                if (!subsToRemove.contains(subf)) {
                    subsToAdd.add(subf);
                }
            }
        } else {
            subsToAdd.addAll(subs);
        }

        restoreSelection();

        NLogger.debug(getClass(), "Subfiles in selection (REMOVE)"); //$NON-NLS-1$
        for (GData1 g : subsToRemove) {
            NLogger.debug(getClass(), g.toString());
            removeSubfileFromSelection(g);
        }

        NLogger.debug(getClass(), "Subfiles in selection (ADD)"); //$NON-NLS-1$
        for (GData1 g : subsToAdd) {
            NLogger.debug(getClass(), g.toString());
            addSubfileToSelection(g);
        }
    }

    public Vector4f getSelectionCenter() {

        final Set<Vertex> objectVertices = Collections.newSetFromMap(new ThreadsafeTreeMap<>());
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
                if (vis == null) continue;
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

    public int[] toggleTEXMAP() {
        return toggleHelper("0 !: "); //$NON-NLS-1$
    }

    public int[] toggleComment() {
        return toggleHelper("0 // "); //$NON-NLS-1$
    }

    private int[] toggleHelper(final String token) {
        int[] offsetCorrection = new int[]{0, 0};
        boolean firstToken = true;
        final String token2 = token.substring(0, 4);
        final GColour col16 = View.getLDConfigColour(16);
        HashBiMap<Integer, GData> dpl = linkedDatFile.getDrawPerLine_NOCLONE();
        for (GData g : selectedData) {
            final GData b = g.getBefore();
            final GData n = g.getNext();
            final String oldStr = g.toString();
            final String lineToParse;
            if (oldStr.startsWith(token)) {
                lineToParse = oldStr.substring(5);
                offsetCorrection[0] -= 5;
                if (firstToken) {
                    firstToken = false;
                    offsetCorrection[1] = -5;
                }
            } else if (oldStr.startsWith(token2)) {
                lineToParse = oldStr.substring(4);
                offsetCorrection[0] -= 4;
                if (firstToken) {
                    firstToken = false;
                    offsetCorrection[1] = -4;
                }
            } else {
                lineToParse = token + oldStr;
                offsetCorrection[0] += 5;
                if (firstToken) {
                    firstToken = false;
                    offsetCorrection[1] = 5;
                }
            }
            Integer line = dpl.getKey(g);
            if (remove(g)) {
                linkedDatFile.setDrawChainTail(b);
            }
            Set<String> alreadyParsed = new HashSet<>();
            alreadyParsed.add(linkedDatFile.getShortName());
            GData pasted;
            if (StringHelper.isNotBlank(lineToParse)) {
                ArrayList<ParsingResult> result = DatParser.parseLine(lineToParse, -1, 0, col16.getR(), col16.getG(), col16.getB(), 1.0f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile, false, alreadyParsed);
                pasted = result.get(0).getGraphicalData();
                if (pasted == null)
                    pasted = new GData0(lineToParse, View.DUMMY_REFERENCE);
            } else {
                pasted = new GData0(lineToParse, View.DUMMY_REFERENCE);
            }
            if (token2.equals(pasted.toString())) {
                pasted = new GData0(lineToParse, View.DUMMY_REFERENCE);
            }
            b.setNext(pasted);
            pasted.setNext(n);
            dpl.put(line, pasted);
            linkedDatFile.setDrawChainTail(dpl.getValue(dpl.size()));
        }
        return offsetCorrection;
    }
}
