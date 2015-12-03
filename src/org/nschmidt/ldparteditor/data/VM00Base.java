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
import java.util.Collections;
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

import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.nschmidt.ldparteditor.composites.compositetab.CompositeTab;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.composite3d.ViewIdleManager;
import org.nschmidt.ldparteditor.helpers.compositetext.SubfileCompiler;
import org.nschmidt.ldparteditor.helpers.math.HashBiMap;
import org.nschmidt.ldparteditor.helpers.math.PowerRay;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeTreeMap;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.shells.editortext.EditorTextWindow;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * @author nils
 *
 */
class VM00Base {

    protected final ArrayList<MemorySnapshot> snapshots = new ArrayList<MemorySnapshot>();

    // 1 Vertex kann an mehreren Stellen (GData2-5 + position) manifestiert sein
    /**
     * Subfile-Inhalte sind hierbei enthalten. Die Manifestierung gegen
     * {@code lineLinkedToVertices} checken, wenn ausgeschlossen werden soll,
     * dass es sich um Subfile Daten handelt
     */
    protected final ThreadsafeTreeMap<Vertex, Set<VertexManifestation>> vertexLinkedToPositionInFile = new ThreadsafeTreeMap<Vertex, Set<VertexManifestation>>();

    // 1 Vertex kann keinem oder mehreren Subfiles angehören
    protected final ThreadsafeTreeMap<Vertex, Set<GData1>> vertexLinkedToSubfile = new ThreadsafeTreeMap<Vertex, Set<GData1>>();

    // Auf Dateiebene: 1 Vertex kann an mehreren Stellen (GData1-5 + position)
    // manifestiert sein, ist er auch im Subfile, so gibt VertexInfo dies an
    /** Subfile-Inhalte sind hier nicht als Key refenziert!! */
    protected final ThreadsafeHashMap<GData, Set<VertexInfo>> lineLinkedToVertices = new ThreadsafeHashMap<GData, Set<VertexInfo>>();

    public final ThreadsafeHashMap<GData, Set<VertexInfo>> getLineLinkedToVertices() {
        return lineLinkedToVertices;
    }

    protected final TreeMap<Vertex, float[]> vertexLinkedToNormalCACHE = new TreeMap<Vertex, float[]>();
    protected final HashMap<GData, float[]> dataLinkedToNormalCACHE = new HashMap<GData, float[]>();

    protected final ThreadsafeHashMap<GData1, Integer> vertexCountInSubfile = new ThreadsafeHashMap<GData1, Integer>();

    protected final ThreadsafeHashMap<GData0, Vertex[]> declaredVertices = new ThreadsafeHashMap<GData0, Vertex[]>();
    protected final ThreadsafeHashMap<GData2, Vertex[]> lines = new ThreadsafeHashMap<GData2, Vertex[]>();
    protected final ThreadsafeHashMap<GData3, Vertex[]> triangles = new ThreadsafeHashMap<GData3, Vertex[]>();
    protected final ThreadsafeHashMap<GData4, Vertex[]> quads = new ThreadsafeHashMap<GData4, Vertex[]>();
    protected final ThreadsafeHashMap<GData5, Vertex[]> condlines = new ThreadsafeHashMap<GData5, Vertex[]>();

    protected final Vertex[] vArray = new Vertex[4];
    protected final VertexManifestation[] vdArray = new VertexManifestation[4];

    protected final Set<Vertex> selectedVertices = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());

    protected final Set<GData> selectedData = Collections.newSetFromMap(new ThreadsafeHashMap<GData, Boolean>());
    protected final Set<GData1> selectedSubfiles = Collections.newSetFromMap(new ThreadsafeHashMap<GData1, Boolean>());
    protected final Set<GData2> selectedLines = Collections.newSetFromMap(new ThreadsafeHashMap<GData2, Boolean>());
    protected final Set<GData3> selectedTriangles = Collections.newSetFromMap(new ThreadsafeHashMap<GData3, Boolean>());
    protected final Set<GData4> selectedQuads = Collections.newSetFromMap(new ThreadsafeHashMap<GData4, Boolean>());
    protected final Set<GData5> selectedCondlines = Collections.newSetFromMap(new ThreadsafeHashMap<GData5, Boolean>());

    protected final Set<Vertex> backupSelectedVertices = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());

    protected final Set<GData> backupSelectedData = Collections.newSetFromMap(new ThreadsafeHashMap<GData, Boolean>());
    protected final Set<GData1> backupSelectedSubfiles = Collections.newSetFromMap(new ThreadsafeHashMap<GData1, Boolean>());
    protected final Set<GData2> backupSelectedLines = Collections.newSetFromMap(new ThreadsafeHashMap<GData2, Boolean>());
    protected final Set<GData3> backupSelectedTriangles = Collections.newSetFromMap(new ThreadsafeHashMap<GData3, Boolean>());
    protected final Set<GData4> backupSelectedQuads = Collections.newSetFromMap(new ThreadsafeHashMap<GData4, Boolean>());
    protected final Set<GData5> backupSelectedCondlines = Collections.newSetFromMap(new ThreadsafeHashMap<GData5, Boolean>());

    protected final Set<GData> newSelectedData = Collections.newSetFromMap(new ThreadsafeHashMap<GData, Boolean>());

    protected GDataPNG selectedBgPicture = null;
    protected int selectedBgPictureIndex = -1;

    protected final Set<Vertex> selectedVerticesForSubfile = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());
    protected final Set<GData2> selectedLinesForSubfile = Collections.newSetFromMap(new ThreadsafeHashMap<GData2, Boolean>());
    protected final Set<GData3> selectedTrianglesForSubfile = Collections.newSetFromMap(new ThreadsafeHashMap<GData3, Boolean>());
    protected final Set<GData4> selectedQuadsForSubfile = Collections.newSetFromMap(new ThreadsafeHashMap<GData4, Boolean>());
    protected final Set<GData5> selectedCondlinesForSubfile = Collections.newSetFromMap(new ThreadsafeHashMap<GData5, Boolean>());

    protected static final List<GData> CLIPBOARD = new ArrayList<GData>();
    protected static final Set<GData> CLIPBOARD_InvNext = Collections.newSetFromMap(new ThreadsafeHashMap<GData, Boolean>());

    protected final Set<GData> dataToHide = Collections.newSetFromMap(new ThreadsafeHashMap<GData, Boolean>());

    protected final PowerRay powerRay = new PowerRay();

    protected final DatFile linkedDatFile;

    protected Vertex vertexToReplace = null;

    protected boolean modified = false;
    protected boolean updated = true;

    protected AtomicBoolean skipSyncWithTextEditor = new AtomicBoolean(false);

    protected int selectedItemIndex = -1;
    protected GData selectedLine = null;

    protected Vertex lastSelectedVertex = null;

    protected TreeSet<Vertex> hiddenVertices = new TreeSet<Vertex>();
    protected HashSet<GData> hiddenData = new HashSet<GData>();

    protected final HashMap<GData, Byte> bfcMap = new HashMap<GData, Byte>();

    protected final AtomicBoolean resetTimer = new AtomicBoolean(false);
    protected final AtomicInteger tid = new AtomicInteger(0);
    protected final AtomicInteger openThreads = new AtomicInteger(0);
    protected final Lock lock = new ReentrantLock();

    protected final BigDecimal TOLERANCE = BigDecimal.ZERO; // new BigDecimal("0.00001"); //.00001
    protected final BigDecimal ZEROT = BigDecimal.ZERO; //  = new BigDecimal("-0.00001");
    protected final BigDecimal ONET = BigDecimal.ONE; //  = new BigDecimal("1.00001");

    protected final BigDecimal TOLERANCER = new BigDecimal("0.00001"); //$NON-NLS-1$ .00001
    protected final BigDecimal ZEROTR = new BigDecimal("-0.00001"); //$NON-NLS-1$
    protected final BigDecimal ONETR = new BigDecimal("1.00001"); //$NON-NLS-1$

    protected VM00Base(DatFile linkedDatFile) {
        this.linkedDatFile = linkedDatFile;
    }

    public final synchronized void setUpdated(boolean updated) {
        this.updated = updated;
        if (updated) {
            ViewIdleManager.renderLDrawStandard[0].set(true);
        }
    }

    public final synchronized void setModified_NoSync() {
        this.modified = true;
        setUpdated(false);
    }

    public final boolean isModified() {
        return modified;
    }

    public final synchronized void setModified(boolean modified, boolean addHistory) {
        if (modified) {
            setUpdated(false);
            syncWithTextEditors(addHistory);
        }
        this.modified = modified;
    }

    public final boolean isUpdated() {
        return updated;
    }

    protected final String bigDecimalToString(BigDecimal bd) {
        String result;
        if (bd.compareTo(BigDecimal.ZERO) == 0)
            return "0"; //$NON-NLS-1$
        BigDecimal bd2 = bd.stripTrailingZeros();
        result = bd2.toPlainString();
        if (result.startsWith("-0."))return "-" + result.substring(2); //$NON-NLS-1$ //$NON-NLS-2$
        if (result.startsWith("0."))return result.substring(1); //$NON-NLS-1$
        return result;
    }

    public final void syncWithTextEditors(boolean addHistory) {

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
                        for(int i = 0; i < 4; i++) {
                            try {
                                Thread.sleep(450);
                            } catch (InterruptedException e) {
                            }
                            if (tid2.get() != tid.get()) break;
                        }
                    } while (resetTimer.get());
                    openThreads.decrementAndGet();
                    if (tid2.get() != tid.get() || isSkipSyncWithTextEditor() || !isSyncWithTextEditor()) return;
                    boolean notFound = true;
                    boolean tryToUnlockLock2 = false;
                    Lock lock2 = null;
                    try {
                        lock2 = linkedDatFile.getHistory().getLock();
                        lock.lock();
                        // "lock2" will be locked, if undo/redo tries to restore the state.
                        // Any attempt to broke the data structure with an old synchronisation state will be
                        // prevented with this lock.
                        if (lock2.tryLock()) {
                            tryToUnlockLock2 = true;
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

                                // We want to know what can go wrong here
                                // because it SHOULD be avoided!!
                                NLogger.error(getClass(), "Synchronisation with the text editor failed."); //$NON-NLS-1$
                                NLogger.error(getClass(), consumed);

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
                        if (lock2 != null && tryToUnlockLock2) lock2.unlock();
                        lock.unlock();
                    }
                }
            });
            syncThread.start();
        } finally {
            lock.unlock();
        }
    }

    public final boolean isSyncWithLpeInline() {
        return WorkbenchManager.getUserSettingState().getSyncWithLpeInline().get();
    }

    public final boolean isSyncWithTextEditor() {
        return WorkbenchManager.getUserSettingState().getSyncWithTextEditor().get();
    }

    public final void setSyncWithTextEditor(boolean syncWithTextEditor) {
        WorkbenchManager.getUserSettingState().getSyncWithTextEditor().set(syncWithTextEditor);
    }

    public final boolean isSkipSyncWithTextEditor() {
        return skipSyncWithTextEditor.get();
    }

    public final void setSkipSyncWithTextEditor(boolean syncWithTextEditor) {
        this.skipSyncWithTextEditor.set(syncWithTextEditor);
    }

    public final void updateUnsavedStatus() {
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

    /**
     * Validates the current data structure against dead references and other
     * inconsistencies. All calls to this method will be "suppressed" in the release version.
     * Except the correction of 'trivial' selection inconsistancies
     */
    public final synchronized void validateState() {
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

    public final void cleanupSelection() {

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
            }
        }

        for (Iterator<GData2> g2i = selectedLines.iterator(); g2i.hasNext();) {
            GData2 g2 = g2i.next();
            if (lines.keySet().contains(g2)) {
                selectedData.add(g2);
            } else {
                g2i.remove();
            }
        }

        for (Iterator<GData3> g3i = selectedTriangles.iterator(); g3i.hasNext();) {
            GData3 g3 = g3i.next();
            if (triangles.keySet().contains(g3)) {
                selectedData.add(g3);
            } else {
                g3i.remove();
            }
        }

        for (Iterator<GData4> g4i = selectedQuads.iterator(); g4i.hasNext();) {
            GData4 g4 = g4i.next();
            if (quads.keySet().contains(g4)) {
                selectedData.add(g4);
            } else {
                g4i.remove();
            }
        }

        for (Iterator<GData5> g5i = selectedCondlines.iterator(); g5i.hasNext();) {
            GData5 g5 = g5i.next();
            if (condlines.keySet().contains(g5)) {
                selectedData.add(g5);
            } else {
                g5i.remove();
            }
        }
    }

    protected final boolean exist(GData g) {
        return lines.containsKey(g) || triangles.containsKey(g) || quads.containsKey(g) || condlines.containsKey(g) || lineLinkedToVertices.containsKey(g);
    }

    /**
     *
     * @param gdata
     * @return {@code true} if the tail was removed
     */
    public final synchronized boolean remove(final GData gdata) {
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
     * FOR TEXT EDITOR ONLY (Performace should be improved (has currently O(n)
     * runtime n=number of code lines)
     *
     * @param oldVertex
     * @param newVertex
     * @param modifyVertexMetaCommands
     * @return
     */
    public final synchronized boolean changeVertexDirect(Vertex oldVertex, Vertex newVertex, boolean modifyVertexMetaCommands) {// ,
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

    public final synchronized boolean changeVertexDirectFast(Vertex oldVertex, Vertex newVertex, boolean moveAdjacentData) {

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


    public final synchronized GData changeVertexDirectFast(Vertex oldVertex, Vertex newVertex, boolean moveAdjacentData, GData og) {

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

    public final synchronized GData0 addVertex(Vertex vertex) {
        if (vertex == null) {
            vertex = new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        if (!vertexLinkedToPositionInFile.containsKey(vertex))
            vertexLinkedToPositionInFile.put(vertex, Collections.newSetFromMap(new ThreadsafeHashMap<VertexManifestation, Boolean>()));
        GData0 vertexTag = new GData0("0 !LPE VERTEX " + bigDecimalToString(vertex.X) + " " + bigDecimalToString(vertex.Y) + " " + bigDecimalToString(vertex.Z)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$)
        vertexLinkedToPositionInFile.get(vertex).add(new VertexManifestation(0, vertexTag));
        lineLinkedToVertices.put(vertexTag, Collections.newSetFromMap(new ThreadsafeHashMap<VertexInfo, Boolean>()));
        lineLinkedToVertices.get(vertexTag).add(new VertexInfo(vertex, 0, vertexTag));
        declaredVertices.put(vertexTag, new Vertex[] { vertex });
        return vertexTag;
    }

    public final void clearVertexNormalCache() {
        vertexLinkedToNormalCACHE.clear();
        dataLinkedToNormalCACHE.clear();
    }

    public final void fillVertexNormalCache(GData data2draw) {
        while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
            data2draw.getVertexNormalMap(vertexLinkedToNormalCACHE, dataLinkedToNormalCACHE, this);
        }
    }

    public final void delete(boolean moveAdjacentData, boolean setModified) {
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

    protected final void linker(GData oldData, GData newData) {
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

    public synchronized HashMap<GData0, Vertex[]> getDeclaredVertices() {
        return new HashMap<GData0, Vertex[]>(declaredVertices);
    }

    public final synchronized HashMap<GData2, Vertex[]> getLines() {
        return new HashMap<GData2, Vertex[]>(lines);
    }

    public final synchronized HashMap<GData3, Vertex[]> getTriangles() {
        return new HashMap<GData3, Vertex[]>(triangles);
    }

    public final synchronized ThreadsafeHashMap<GData3, Vertex[]> getTriangles_NOCLONE() {
        return triangles;
    }

    public final synchronized ThreadsafeHashMap<GData4, Vertex[]> getQuads_NOCLONE() {
        return quads;
    }

    public final synchronized HashMap<GData4, Vertex[]> getQuads() {
        return new HashMap<GData4, Vertex[]>(quads);
    }

    public final synchronized HashMap<GData5, Vertex[]> getCondlines() {
        return new HashMap<GData5, Vertex[]>(condlines);
    }

    public final Vertex getVertexToReplace() {
        return vertexToReplace;
    }

    public final void setVertexToReplace(Vertex vertexToReplace) {
        this.vertexToReplace = vertexToReplace;
    }

    public final AtomicBoolean getResetTimer() {
        return resetTimer;
    }

    public final Set<Vertex> getVertices() {
        return vertexLinkedToPositionInFile.keySet();
    }

    public final synchronized void clear() {
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
}
