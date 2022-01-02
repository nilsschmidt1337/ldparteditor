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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.composite.compositetab.CompositeTab;
import org.nschmidt.ldparteditor.composite.compositetab.CompositeTabState;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.helper.LDPartEditorException;
import org.nschmidt.ldparteditor.helper.composite3d.ViewIdleManager;
import org.nschmidt.ldparteditor.helper.compositetext.SubfileCompiler;
import org.nschmidt.ldparteditor.helper.math.HashBiMap;
import org.nschmidt.ldparteditor.helper.math.PowerRay;
import org.nschmidt.ldparteditor.helper.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.helper.math.ThreadsafeSortedMap;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.shell.editortext.EditorTextWindow;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * @author nils
 *
 */
class VM00Base {

    protected final List<MemorySnapshot> snapshots = new ArrayList<>();

    // 1 Vertex kann an mehreren Stellen (GData2-5 + position) manifestiert sein
    /**
     * Subfile-Inhalte sind hierbei enthalten. Die Manifestierung gegen
     * {@code lineLinkedToVertices} checken, wenn ausgeschlossen werden soll,
     * dass es sich um Subfile Daten handelt
     */
    protected final ThreadsafeSortedMap<Vertex, Set<VertexManifestation>> vertexLinkedToPositionInFile = new ThreadsafeSortedMap<>();

    // 1 Vertex kann keinem oder mehreren Subfiles angehören
    protected final ThreadsafeSortedMap<Vertex, Set<GData1>> vertexLinkedToSubfile = new ThreadsafeSortedMap<>();

    // Auf Dateiebene: 1 Vertex kann an mehreren Stellen (GData1-5 + position)
    // manifestiert sein, ist er auch im Subfile, so gibt VertexInfo dies an
    /** Subfile-Inhalte sind hier nicht als Key refenziert!! */
    protected final ThreadsafeHashMap<GData, Set<VertexInfo>> lineLinkedToVertices = new ThreadsafeHashMap<>();

    public final ThreadsafeHashMap<GData, Set<VertexInfo>> getLineLinkedToVertices() {
        return lineLinkedToVertices;
    }

    private final ThreadsafeSortedMap<Vertex, float[]> vertexLinkedToNormalCACHE = new ThreadsafeSortedMap<>();
    protected final ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE = new ThreadsafeHashMap<>();

    protected final ThreadsafeHashMap<GData1, Integer> vertexCountInSubfile = new ThreadsafeHashMap<>();

    protected final ThreadsafeHashMap<GData0, Vertex[]> declaredVertices = new ThreadsafeHashMap<>();
    protected final ThreadsafeHashMap<GData2, Vertex[]> lines = new ThreadsafeHashMap<>();
    protected final ThreadsafeHashMap<GData3, Vertex[]> triangles = new ThreadsafeHashMap<>();
    protected final ThreadsafeHashMap<GData4, Vertex[]> quads = new ThreadsafeHashMap<>();
    protected final ThreadsafeHashMap<GData5, Vertex[]> condlines = new ThreadsafeHashMap<>();

    protected final Vertex[] vArray = new Vertex[4];
    protected final VertexManifestation[] vdArray = new VertexManifestation[4];

    protected final Set<Vertex> selectedVertices = Collections.newSetFromMap(new ThreadsafeSortedMap<>());

    protected final Set<GData> selectedData = Collections.newSetFromMap(new ThreadsafeHashMap<>());
    protected final Set<GData1> selectedSubfiles = Collections.newSetFromMap(new ThreadsafeHashMap<>());
    protected final Set<GData2> selectedLines = Collections.newSetFromMap(new ThreadsafeHashMap<>());
    protected final Set<GData3> selectedTriangles = Collections.newSetFromMap(new ThreadsafeHashMap<>());
    protected final Set<GData4> selectedQuads = Collections.newSetFromMap(new ThreadsafeHashMap<>());
    protected final Set<GData5> selectedCondlines = Collections.newSetFromMap(new ThreadsafeHashMap<>());

    protected final Set<Vertex> backupSelectedVertices = Collections.newSetFromMap(new ThreadsafeSortedMap<>());

    protected final Set<GData> backupSelectedData = Collections.newSetFromMap(new ThreadsafeHashMap<>());
    protected final Set<GData1> backupSelectedSubfiles = Collections.newSetFromMap(new ThreadsafeHashMap<>());
    protected final Set<GData2> backupSelectedLines = Collections.newSetFromMap(new ThreadsafeHashMap<>());
    protected final Set<GData3> backupSelectedTriangles = Collections.newSetFromMap(new ThreadsafeHashMap<>());
    protected final Set<GData4> backupSelectedQuads = Collections.newSetFromMap(new ThreadsafeHashMap<>());
    protected final Set<GData5> backupSelectedCondlines = Collections.newSetFromMap(new ThreadsafeHashMap<>());

    protected final Set<GData> newSelectedData = Collections.newSetFromMap(new ThreadsafeHashMap<>());

    protected GDataPNG selectedBgPicture = null;
    protected int selectedBgPictureIndex = -1;

    protected final Set<Vertex> selectedVerticesForSubfile = Collections.newSetFromMap(new ThreadsafeSortedMap<>());
    protected final Set<GData2> selectedLinesForSubfile = Collections.newSetFromMap(new ThreadsafeHashMap<>());
    protected final Set<GData3> selectedTrianglesForSubfile = Collections.newSetFromMap(new ThreadsafeHashMap<>());
    protected final Set<GData4> selectedQuadsForSubfile = Collections.newSetFromMap(new ThreadsafeHashMap<>());
    protected final Set<GData5> selectedCondlinesForSubfile = Collections.newSetFromMap(new ThreadsafeHashMap<>());

    protected final Set<GData> dataToHide = Collections.newSetFromMap(new ThreadsafeHashMap<>());

    protected final PowerRay powerRay = new PowerRay();

    protected final DatFile linkedDatFile;

    private Vertex vertexToReplace = null;

    private boolean modified = false;
    private boolean updated = true;

    private final AtomicBoolean skipSyncWithTextEditor = new AtomicBoolean(false);

    protected int selectedItemIndex = -1;
    protected GData selectedLine = null;

    protected Vertex lastSelectedVertex = null;

    protected final Set<Vertex> hiddenVertices = Collections.newSetFromMap(new ThreadsafeSortedMap<>());
    protected final Set<GData> hiddenData = Collections.newSetFromMap(new ThreadsafeHashMap<>());

    protected final Map<GData, BFC> bfcMap = new HashMap<>();

    private volatile AtomicBoolean resetTimer = new AtomicBoolean(false);
    private volatile AtomicBoolean skipTimer = new AtomicBoolean(false);
    private volatile AtomicInteger tid = new AtomicInteger(0);
    private volatile AtomicInteger openThreads = new AtomicInteger(0);
    private volatile Lock lock = new ReentrantLock();
    private volatile Lock manifestationLock = new ReentrantLock();

    protected VM00Base(DatFile linkedDatFile) {
        this.linkedDatFile = linkedDatFile;
    }

    public final synchronized void setUpdated(boolean updated) {
        this.updated = updated;
        if (updated) {
            ViewIdleManager.renderLDrawStandard[0].set(true);
        }
    }

    public final synchronized void setModifiedNoSync() {
        this.modified = true;
        setUpdated(false);
    }

    public final synchronized boolean isModified() {
        return modified;
    }

    public final synchronized void setModified(boolean modified, boolean addHistory) {
        if (modified) {
            setUpdated(false);
            syncWithTextEditors(addHistory);
        }
        this.modified = modified;
    }

    public final synchronized boolean isUpdated() {
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

    public boolean isPureCondlineControlPoint(Vertex v) {
        boolean pureControlPoint = false;
        Set<VertexManifestation> manis = vertexLinkedToPositionInFile.get(v);
        if (manis != null) {
            pureControlPoint = true;
            for (VertexManifestation m : manis) {
                if (m.position() < 2 || m.gdata().type() != 5) {
                    pureControlPoint = false;
                    break;
                }
            }
        }
        return pureControlPoint;
    }

    public final void syncWithTextEditors(boolean addHistory) {

        if (addHistory) linkedDatFile.addHistory();

        lock.lock();
        try {

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
            final Thread syncThread = new Thread(() -> {
                openThreads.incrementAndGet();
                do {
                    if (skipTimer.get()) {
                        break;
                    }
                    resetTimer.set(false);
                    for(int i = 0; i < 4; i++) {
                        try {
                            Thread.sleep(450);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new LDPartEditorException(ie);
                        }
                        if (tid2.get() != tid.get()) break;
                    }
                } while (resetTimer.get());
                skipTimer.set(false);
                openThreads.decrementAndGet();
                if (tid2.get() != tid.get() || isSkipSyncWithTextEditor() || !isSyncWithTextEditor()) return;
                boolean notFound = true;
                boolean tryToUnlockLock2 = false;
                final Lock lock2 = linkedDatFile.getHistory().getLock();
                try {
                    // "lock2" will be locked, if undo/redo tries to restore the state.
                    // Any attempt to broke the data structure with an old synchronisation state will be
                    // prevented with this lock.
                    tryToUnlockLock2 = lock2.tryLock();
                    if (tryToUnlockLock2) {
                        try {
                            // A lot of stuff can throw an exception here, since the thread waits two seconds and
                            // the state of the program may not allow a synchronisation anymore
                            for (EditorTextWindow w : Project.getOpenTextWindows()) {
                                for (final CTabItem t : w.getTabFolder().getItems()) {
                                    // FIXME Implement a solid solution against this NullPointer errors here...
                                    final CompositeTab ctab = ((CompositeTab) t);
                                    if (ctab == null) continue;
                                    final CompositeTabState state = ctab.getState();
                                    if (state == null) continue;
                                    final DatFile txtDat = state.getFileNameObj();
                                    if (txtDat != null && txtDat.equals(linkedDatFile)) {
                                        notFound = false;
                                        final String txt;
                                        if (isModified()) {
                                            txt = txtDat.getText();
                                        } else {
                                            txt = null;
                                        }
                                        Display.getDefault().asyncExec(() -> {
                                            try {
                                                int ti = ctab.getTextComposite().getTopIndex();
                                                Point r = ctab.getTextComposite().getSelectionRange();
                                                ctab.getState().setSync(true);
                                                if (isModified() && txt != null) {
                                                    ctab.getTextComposite().setText(txt);
                                                }
                                                ctab.getTextComposite().setTopIndex(ti);
                                                try {
                                                    ctab.getTextComposite().setSelectionRange(r.x, r.y);
                                                } catch (IllegalArgumentException iae) {
                                                    // It is not critical, just print it during debug mode.
                                                    NLogger.debug(VM00Base.class, iae);
                                                }
                                                ctab.getTextComposite().redraw();
                                                ctab.getControl().redraw();
                                                ctab.getState().setSync(false);
                                            } catch (SWTException ex) {
                                                // The text editor widget could be disposed
                                                NLogger.error(getClass(), ex);
                                            } finally {
                                                setUpdated(true);
                                            }
                                        });
                                    }
                                }
                            }
                        } catch (Exception criticalException) {

                            // We want to know what can go wrong here
                            // because it SHOULD be avoided!!
                            NLogger.error(getClass(), "Synchronisation with the text editor failed."); //$NON-NLS-1$
                            NLogger.error(getClass(), criticalException);

                            setUpdated(true);
                        } finally {
                            if (notFound) setUpdated(true);
                        }
                        if (WorkbenchManager.getUserSettingState().getSyncWithLpeInline().get()) {
                            while (!isUpdated() && Editor3DWindow.getAlive().get()) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException ie) {
                                    Thread.currentThread().interrupt();
                                    throw new LDPartEditorException(ie);
                                }
                            }
                            Display.getDefault().asyncExec(() ->
                                SubfileCompiler.compile(linkedDatFile, true, true)
                            );
                        }
                    } else {
                        NLogger.debug(getClass(), "Synchronisation was skipped due to undo/redo."); //$NON-NLS-1$
                    }
                } finally {
                    try {
                        if (tryToUnlockLock2) lock2.unlock();
                    } catch (Exception e) {
                        NLogger.error(getClass(), e);
                    }
                }
            });
            syncThread.start();
        } finally {
            try {
                lock.unlock();
            } catch (Exception e) {
                NLogger.error(getClass(), e);
            }
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
                Editor3DWindow.getWindow().updateTreeUnsavedEntries();
            }
        } else if (!Project.getUnsavedFiles().contains(linkedDatFile)) {
            Project.addUnsavedFile(linkedDatFile);
            Editor3DWindow.getWindow().updateTreeUnsavedEntries();
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
        cleanupHiddenData();
        // Do not validate more stuff on release, since it costs a lot performance.
        if (!NLogger.debugging) return;

        Set<Vertex> vertices = vertexLinkedToPositionInFile.keySet();
        SortedSet<Vertex> verticesInUse = new TreeSet<>();

        for (Vertex[] verts : declaredVertices.values()) {
            verticesInUse.addAll(Arrays.asList(verts));
        }
        for (Vertex[] verts : lines.values()) {
            verticesInUse.addAll(Arrays.asList(verts));
        }
        for (Vertex[] verts : triangles.values()) {
            verticesInUse.addAll(Arrays.asList(verts));
        }
        for (Vertex[] verts : quads.values()) {
            verticesInUse.addAll(Arrays.asList(verts));
        }
        for (Vertex[] verts : condlines.values()) {
            verticesInUse.addAll(Arrays.asList(verts));
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
        SortedSet<Integer> lineNumbers = new TreeSet<>(lineMap.keySet());
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

        if (vertexCount != vertexUseCount) {
            throw new AssertionError("The number of vertices displayed is not equal to the number of stored vertices."); //$NON-NLS-1$
        }
    }

    private final void cleanupHiddenData() {
        if (!hiddenData.isEmpty()) {
            Map<String, List<GData>> dict = null;
            Set<GData1> s1 = vertexCountInSubfile.keySet();
            Set<GData2> s2 = lines.keySet();
            Set<GData3> s3 = triangles.keySet();
            Set<GData4> s4 = quads.keySet();
            Set<GData5> s5 = condlines.keySet();
            Set<GData> tmpDataToHide = new HashSet<>();
            for (Iterator<GData> hi = hiddenData.iterator(); hi.hasNext();) {
                final GData oldData = hi.next();
                if (
                        s1.contains(oldData)
                        || s2.contains(oldData)
                        || s3.contains(oldData)
                        || s4.contains(oldData)
                        || s5.contains(oldData)) {
                    oldData.visible = false;
                    continue;
                }
                if (dict == null) {
                    dict = buildObjectDictionary();
                }
                List<GData> g3 = dict.get(oldData.toString());
                if (g3 != null) {
                    for (GData g : g3) {
                        if (isSharingSameSubfile(g, oldData)) {
                            tmpDataToHide.add(g);
                            g.visible = false;
                        }
                    }
                    hi.remove();
                }
            }
            hiddenData.addAll(tmpDataToHide);
        }
    }

    private Map<String, List<GData>> buildObjectDictionary() {
        Map<String, List<GData>> dict = new HashMap<>();
        for (GData1 g1 : vertexCountInSubfile.keySet()) {
            final String key = g1.getNiceString();
            dict.putIfAbsent(key, new ArrayList<>());
            dict.get(key).add(g1);
        }
        for (GData2 g2 : lines.keySet()) {
            final String key = g2.getNiceString();
            dict.putIfAbsent(key, new ArrayList<>());
            dict.get(key).add(g2);
        }
        for (GData3 g3 : triangles.keySet()) {
            final String key = g3.getNiceString();
            dict.putIfAbsent(key, new ArrayList<>());
            dict.get(key).add(g3);
        }
        for (GData4 g4 : quads.keySet()) {
            final String key = g4.getNiceString();
            dict.putIfAbsent(key, new ArrayList<>());
            dict.get(key).add(g4);
        }
        for (GData5 g5 : condlines.keySet()) {
            final String key = g5.getNiceString();
            dict.putIfAbsent(key, new ArrayList<>());
            dict.get(key).add(g5);
        }
        return dict;
    }

    private boolean isSharingSameSubfile(GData g1, GData g2) {
        GData1 s1 = g1.parent;
        GData1 s2 = g2.parent;
        if (s1 == View.DUMMY_REFERENCE && s2 == View.DUMMY_REFERENCE) {
            return true;
        }
        if (s1 == View.DUMMY_REFERENCE || s2 == View.DUMMY_REFERENCE) {
            return false;
        }
        return s1.getNiceString().equals(s2.getNiceString());
    }

    private final void cleanupSelection() {

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
        getManifestationLock().lock();
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
        getManifestationLock().unlock();
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
    public final synchronized boolean changeVertexDirect(Vertex oldVertex, Vertex newVertex, boolean modifyVertexMetaCommands) {
        HashBiMap<Integer, GData> drawPerLine = linkedDatFile.getDrawPerLineNoClone();
        SortedSet<Integer> keys = new TreeSet<>(drawPerLine.keySet());
        Set<GData> dataToRemove = new HashSet<>();
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

                updateTail = remove(gData) || updateTail;

                if (v0[0].equals(oldVertex))
                    v0[0] = newVertex;

                GData0 newGdata0 = addVertex(newVertex);

                drawPerLine.put(oldNumber, newGdata0);
                break;
            case 2:

                GData2 gd2 = (GData2) gData;
                Vertex[] v2 = lines.get(gd2);

                updateTail = remove(gData) || updateTail;

                if (v2[0].equals(oldVertex))
                    v2[0] = newVertex;
                if (v2[1].equals(oldVertex))
                    v2[1] = newVertex;

                GData2 newGdata2 = new GData2(gd2.colourNumber, gd2.r, gd2.g, gd2.b, gd2.a, v2[0], v2[1], View.DUMMY_REFERENCE, linkedDatFile, gd2.isLine);

                drawPerLine.put(oldNumber, newGdata2);
                break;
            case 3:

                GData3 gd3 = (GData3) gData;
                Vertex[] v3 = triangles.get(gd3);

                updateTail = remove(gData) || updateTail;

                if (v3[0].equals(oldVertex))
                    v3[0] = newVertex;
                if (v3[1].equals(oldVertex))
                    v3[1] = newVertex;
                if (v3[2].equals(oldVertex))
                    v3[2] = newVertex;

                GData3 newGdata3 = new GData3(gd3.colourNumber, gd3.r, gd3.g, gd3.b, gd3.a, v3[0], v3[1], v3[2], View.DUMMY_REFERENCE, linkedDatFile, gd3.isTriangle);

                drawPerLine.put(oldNumber, newGdata3);
                break;
            case 4:

                GData4 gd4 = (GData4) gData;
                Vertex[] v4 = quads.get(gd4);

                updateTail = remove(gData) || updateTail;

                if (v4[0].equals(oldVertex))
                    v4[0] = newVertex;
                if (v4[1].equals(oldVertex))
                    v4[1] = newVertex;
                if (v4[2].equals(oldVertex))
                    v4[2] = newVertex;
                if (v4[3].equals(oldVertex))
                    v4[3] = newVertex;

                GData4 newGdata4 = new GData4(gd4.colourNumber, gd4.r, gd4.g, gd4.b, gd4.a, v4[0], v4[1], v4[2], v4[3], View.DUMMY_REFERENCE, linkedDatFile);

                drawPerLine.put(oldNumber, newGdata4);
                break;
            case 5:

                GData5 gd5 = (GData5) gData;
                Vertex[] v5 = condlines.get(gd5);

                updateTail = remove(gData) || updateTail;

                if (v5[0].equals(oldVertex))
                    v5[0] = newVertex;
                if (v5[1].equals(oldVertex))
                    v5[1] = newVertex;
                if (v5[2].equals(oldVertex))
                    v5[2] = newVertex;
                if (v5[3].equals(oldVertex))
                    v5[3] = newVertex;

                GData5 newGdata5 = new GData5(gd5.colourNumber, gd5.r, gd5.g, gd5.b, gd5.a, v5[0], v5[1], v5[2], v5[3], View.DUMMY_REFERENCE, linkedDatFile);

                drawPerLine.put(oldNumber, newGdata5);
                break;
            default:
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
        Set<VertexManifestation> manis = new HashSet<>(manis2);

        HashBiMap<Integer, GData> drawPerLine = linkedDatFile.getDrawPerLineNoClone();

        for (VertexManifestation mani : manis) {
            GData oldData = mani.gdata();
            if (!lineLinkedToVertices.containsKey(oldData))
                continue;
            GData newData = null;
            switch (oldData.type()) {
            case 0:
                GData0 oldVm = (GData0) oldData;
                GData0 newVm = null;
                Vertex[] va = declaredVertices.get(oldVm);
                if (va == null || !moveAdjacentData && !selectedVertices.contains(va[0]))
                    continue;
                if (va[0].equals(oldVertex))
                    va[0] = newVertex;
                newVm = addVertex(va[0]);
                newData = newVm;
                break;
            case 2:
                GData2 oldLin = (GData2) oldData;
                if (!moveAdjacentData && !selectedLines.contains(oldLin))
                    continue;
                GData2 newLin = null;
                switch (mani.position()) {
                case 0:
                    newLin = new GData2(oldLin.colourNumber, oldLin.r, oldLin.g, oldLin.b, oldLin.a, newVertex.xp, newVertex.yp, newVertex.zp, oldLin.x2p, oldLin.y2p, oldLin.z2p, oldLin.parent,
                            linkedDatFile, oldLin.isLine);
                    break;
                case 1:
                    newLin = new GData2(oldLin.colourNumber, oldLin.r, oldLin.g, oldLin.b, oldLin.a, oldLin.x1p, oldLin.y1p, oldLin.z1p, newVertex.xp, newVertex.yp, newVertex.zp, oldLin.parent,
                            linkedDatFile, oldLin.isLine);
                    break;
                default:
                    NLogger.error(VM00Base.class, "Unsupported vertex position index on instance: " + oldData + " type: " + oldData.type() + " index: " + mani.position()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    continue;
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
                switch (mani.position()) {
                case 0:
                    newTri = new GData3(oldTri.colourNumber, oldTri.r, oldTri.g, oldTri.b, oldTri.a, newVertex, new Vertex(oldTri.x2p, oldTri.y2p, oldTri.z2p),
                            new Vertex(oldTri.x3p, oldTri.y3p, oldTri.z3p), oldTri.parent, linkedDatFile, oldTri.isTriangle);
                    break;
                case 1:
                    newTri = new GData3(oldTri.colourNumber, oldTri.r, oldTri.g, oldTri.b, oldTri.a, new Vertex(oldTri.x1p, oldTri.y1p, oldTri.z1p), newVertex,
                            new Vertex(oldTri.x3p, oldTri.y3p, oldTri.z3p), oldTri.parent, linkedDatFile, oldTri.isTriangle);
                    break;
                case 2:
                    newTri = new GData3(oldTri.colourNumber, oldTri.r, oldTri.g, oldTri.b, oldTri.a, new Vertex(oldTri.x1p, oldTri.y1p, oldTri.z1p), new Vertex(oldTri.x2p, oldTri.y2p, oldTri.z2p),
                            newVertex, oldTri.parent, linkedDatFile, oldTri.isTriangle);
                    break;
                default:
                    NLogger.error(VM00Base.class, "Unsupported vertex position index on instance: " + oldData + " type: " + oldData.type() + " index: " + mani.position()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    continue;
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
                switch (mani.position()) {
                case 0:
                    newQuad = new GData4(oldQuad.colourNumber, oldQuad.r, oldQuad.g, oldQuad.b, oldQuad.a, newVertex, new Vertex(oldQuad.x2p, oldQuad.y2p, oldQuad.z2p), new Vertex(oldQuad.x3p,
                            oldQuad.y3p, oldQuad.z3p), new Vertex(oldQuad.x4p, oldQuad.y4p, oldQuad.z4p), oldQuad.parent, linkedDatFile);
                    break;
                case 1:
                    newQuad = new GData4(oldQuad.colourNumber, oldQuad.r, oldQuad.g, oldQuad.b, oldQuad.a, new Vertex(oldQuad.x1p, oldQuad.y1p, oldQuad.z1p), newVertex, new Vertex(oldQuad.x3p,
                            oldQuad.y3p, oldQuad.z3p), new Vertex(oldQuad.x4p, oldQuad.y4p, oldQuad.z4p), oldQuad.parent, linkedDatFile);
                    break;
                case 2:
                    newQuad = new GData4(oldQuad.colourNumber, oldQuad.r, oldQuad.g, oldQuad.b, oldQuad.a, new Vertex(oldQuad.x1p, oldQuad.y1p, oldQuad.z1p), new Vertex(oldQuad.x2p, oldQuad.y2p,
                            oldQuad.z2p), newVertex, new Vertex(oldQuad.x4p, oldQuad.y4p, oldQuad.z4p), oldQuad.parent, linkedDatFile);
                    break;
                case 3:
                    newQuad = new GData4(oldQuad.colourNumber, oldQuad.r, oldQuad.g, oldQuad.b, oldQuad.a, new Vertex(oldQuad.x1p, oldQuad.y1p, oldQuad.z1p), new Vertex(oldQuad.x2p, oldQuad.y2p,
                            oldQuad.z2p), new Vertex(oldQuad.x3p, oldQuad.y3p, oldQuad.z3p), newVertex, oldQuad.parent, linkedDatFile);
                    break;
                default:
                    NLogger.error(VM00Base.class, "Unsupported vertex position index on instance: " + oldData + " type: " + oldData.type() + " index: " + mani.position()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    continue;
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
                switch (mani.position()) {
                case 0:
                    newCLin = new GData5(oldCLin.colourNumber, oldCLin.r, oldCLin.g, oldCLin.b, oldCLin.a, newVertex, new Vertex(oldCLin.x2p, oldCLin.y2p, oldCLin.z2p), new Vertex(oldCLin.x3p,
                            oldCLin.y3p, oldCLin.z3p), new Vertex(oldCLin.x4p, oldCLin.y4p, oldCLin.z4p), oldCLin.parent, linkedDatFile);
                    break;
                case 1:
                    newCLin = new GData5(oldCLin.colourNumber, oldCLin.r, oldCLin.g, oldCLin.b, oldCLin.a, new Vertex(oldCLin.x1p, oldCLin.y1p, oldCLin.z1p), newVertex, new Vertex(oldCLin.x3p,
                            oldCLin.y3p, oldCLin.z3p), new Vertex(oldCLin.x4p, oldCLin.y4p, oldCLin.z4p), oldCLin.parent, linkedDatFile);
                    break;
                case 2:
                    newCLin = new GData5(oldCLin.colourNumber, oldCLin.r, oldCLin.g, oldCLin.b, oldCLin.a, new Vertex(oldCLin.x1p, oldCLin.y1p, oldCLin.z1p), new Vertex(oldCLin.x2p, oldCLin.y2p,
                            oldCLin.z2p), newVertex, new Vertex(oldCLin.x4p, oldCLin.y4p, oldCLin.z4p), oldCLin.parent, linkedDatFile);
                    break;
                case 3:
                    newCLin = new GData5(oldCLin.colourNumber, oldCLin.r, oldCLin.g, oldCLin.b, oldCLin.a, new Vertex(oldCLin.x1p, oldCLin.y1p, oldCLin.z1p), new Vertex(oldCLin.x2p, oldCLin.y2p,
                            oldCLin.z2p), new Vertex(oldCLin.x3p, oldCLin.y3p, oldCLin.z3p), newVertex, oldCLin.parent, linkedDatFile);
                    break;
                default:
                    NLogger.error(VM00Base.class, "Unsupported vertex position index on instance: " + oldData + " type: " + oldData.type() + " index: " + mani.position()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    continue;
                }
                newData = newCLin;
                if (selectedCondlines.contains(oldCLin))
                    selectedCondlines.add(newCLin);
                break;
            default:
                NLogger.error(VM00Base.class, "Unsupported vertex change on instance: " + oldData + " type: " + oldData.type()); //$NON-NLS-1$ //$NON-NLS-2$
                continue;
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
        Set<VertexManifestation> manis = new HashSet<>(manis2);

        HashBiMap<Integer, GData> drawPerLine = linkedDatFile.getDrawPerLineNoClone();

        for (VertexManifestation mani : manis) {
            GData oldData = mani.gdata();
            if (!lineLinkedToVertices.containsKey(oldData))
                continue;
            GData newData = null;
            switch (oldData.type()) {
            case 0:
                GData0 oldVm = (GData0) oldData;
                GData0 newVm = null;
                Vertex[] va = declaredVertices.get(oldVm);
                if (va == null || !moveAdjacentData && !selectedVertices.contains(va[0]))
                    continue;
                if (va[0].equals(oldVertex))
                    va[0] = newVertex;
                newVm = addVertex(va[0]);
                newData = newVm;
                break;
            case 2:
                GData2 oldLin = (GData2) oldData;
                if (!moveAdjacentData && !selectedLines.contains(oldLin))
                    continue;
                GData2 newLin = null;
                switch (mani.position()) {
                case 0:
                    newLin = new GData2(oldLin.colourNumber, oldLin.r, oldLin.g, oldLin.b, oldLin.a, newVertex.xp, newVertex.yp, newVertex.zp, oldLin.x2p, oldLin.y2p, oldLin.z2p, oldLin.parent,
                            linkedDatFile, oldLin.isLine);
                    break;
                case 1:
                    newLin = new GData2(oldLin.colourNumber, oldLin.r, oldLin.g, oldLin.b, oldLin.a, oldLin.x1p, oldLin.y1p, oldLin.z1p, newVertex.xp, newVertex.yp, newVertex.zp, oldLin.parent,
                            linkedDatFile, oldLin.isLine);
                    break;
                default:
                    NLogger.error(VM00Base.class, "Unsupported vertex position index on instance: " + oldData + " type: " + oldData.type() + " index: " + mani.position()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    continue;
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
                switch (mani.position()) {
                case 0:
                    newTri = new GData3(oldTri.colourNumber, oldTri.r, oldTri.g, oldTri.b, oldTri.a, newVertex, new Vertex(oldTri.x2p, oldTri.y2p, oldTri.z2p),
                            new Vertex(oldTri.x3p, oldTri.y3p, oldTri.z3p), oldTri.parent, linkedDatFile, oldTri.isTriangle);
                    break;
                case 1:
                    newTri = new GData3(oldTri.colourNumber, oldTri.r, oldTri.g, oldTri.b, oldTri.a, new Vertex(oldTri.x1p, oldTri.y1p, oldTri.z1p), newVertex,
                            new Vertex(oldTri.x3p, oldTri.y3p, oldTri.z3p), oldTri.parent, linkedDatFile, oldTri.isTriangle);
                    break;
                case 2:
                    newTri = new GData3(oldTri.colourNumber, oldTri.r, oldTri.g, oldTri.b, oldTri.a, new Vertex(oldTri.x1p, oldTri.y1p, oldTri.z1p), new Vertex(oldTri.x2p, oldTri.y2p, oldTri.z2p),
                            newVertex, oldTri.parent, linkedDatFile, oldTri.isTriangle);
                    break;
                default:
                    NLogger.error(VM00Base.class, "Unsupported vertex position index on instance: " + oldData + " type: " + oldData.type() + " index: " + mani.position()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    continue;
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
                switch (mani.position()) {
                case 0:
                    newQuad = new GData4(oldQuad.colourNumber, oldQuad.r, oldQuad.g, oldQuad.b, oldQuad.a, newVertex, new Vertex(oldQuad.x2p, oldQuad.y2p, oldQuad.z2p), new Vertex(oldQuad.x3p,
                            oldQuad.y3p, oldQuad.z3p), new Vertex(oldQuad.x4p, oldQuad.y4p, oldQuad.z4p), oldQuad.parent, linkedDatFile);
                    break;
                case 1:
                    newQuad = new GData4(oldQuad.colourNumber, oldQuad.r, oldQuad.g, oldQuad.b, oldQuad.a, new Vertex(oldQuad.x1p, oldQuad.y1p, oldQuad.z1p), newVertex, new Vertex(oldQuad.x3p,
                            oldQuad.y3p, oldQuad.z3p), new Vertex(oldQuad.x4p, oldQuad.y4p, oldQuad.z4p), oldQuad.parent, linkedDatFile);
                    break;
                case 2:
                    newQuad = new GData4(oldQuad.colourNumber, oldQuad.r, oldQuad.g, oldQuad.b, oldQuad.a, new Vertex(oldQuad.x1p, oldQuad.y1p, oldQuad.z1p), new Vertex(oldQuad.x2p, oldQuad.y2p,
                            oldQuad.z2p), newVertex, new Vertex(oldQuad.x4p, oldQuad.y4p, oldQuad.z4p), oldQuad.parent, linkedDatFile);
                    break;
                case 3:
                    newQuad = new GData4(oldQuad.colourNumber, oldQuad.r, oldQuad.g, oldQuad.b, oldQuad.a, new Vertex(oldQuad.x1p, oldQuad.y1p, oldQuad.z1p), new Vertex(oldQuad.x2p, oldQuad.y2p,
                            oldQuad.z2p), new Vertex(oldQuad.x3p, oldQuad.y3p, oldQuad.z3p), newVertex, oldQuad.parent, linkedDatFile);
                    break;
                default:
                    NLogger.error(VM00Base.class, "Unsupported vertex position index on instance: " + oldData + " type: " + oldData.type() + " index: " + mani.position()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    continue;
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
                switch (mani.position()) {
                case 0:
                    newCLin = new GData5(oldCLin.colourNumber, oldCLin.r, oldCLin.g, oldCLin.b, oldCLin.a, newVertex, new Vertex(oldCLin.x2p, oldCLin.y2p, oldCLin.z2p), new Vertex(oldCLin.x3p,
                            oldCLin.y3p, oldCLin.z3p), new Vertex(oldCLin.x4p, oldCLin.y4p, oldCLin.z4p), oldCLin.parent, linkedDatFile);
                    break;
                case 1:
                    newCLin = new GData5(oldCLin.colourNumber, oldCLin.r, oldCLin.g, oldCLin.b, oldCLin.a, new Vertex(oldCLin.x1p, oldCLin.y1p, oldCLin.z1p), newVertex, new Vertex(oldCLin.x3p,
                            oldCLin.y3p, oldCLin.z3p), new Vertex(oldCLin.x4p, oldCLin.y4p, oldCLin.z4p), oldCLin.parent, linkedDatFile);
                    break;
                case 2:
                    newCLin = new GData5(oldCLin.colourNumber, oldCLin.r, oldCLin.g, oldCLin.b, oldCLin.a, new Vertex(oldCLin.x1p, oldCLin.y1p, oldCLin.z1p), new Vertex(oldCLin.x2p, oldCLin.y2p,
                            oldCLin.z2p), newVertex, new Vertex(oldCLin.x4p, oldCLin.y4p, oldCLin.z4p), oldCLin.parent, linkedDatFile);
                    break;
                case 3:
                    newCLin = new GData5(oldCLin.colourNumber, oldCLin.r, oldCLin.g, oldCLin.b, oldCLin.a, new Vertex(oldCLin.x1p, oldCLin.y1p, oldCLin.z1p), new Vertex(oldCLin.x2p, oldCLin.y2p,
                            oldCLin.z2p), new Vertex(oldCLin.x3p, oldCLin.y3p, oldCLin.z3p), newVertex, oldCLin.parent, linkedDatFile);
                    break;
                default:
                    NLogger.error(VM00Base.class, "Unsupported vertex position index on instance: " + oldData + " type: " + oldData.type() + " index: " + mani.position()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    continue;
                }
                newData = newCLin;
                if (selectedCondlines.contains(oldCLin))
                    selectedCondlines.add(newCLin);
                break;
            default:
                NLogger.error(VM00Base.class, "Unsupported vertex change on instance: " + oldData + " type: " + oldData.type()); //$NON-NLS-1$ //$NON-NLS-2$
                continue;
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
        final GData0 vertexTag = new GData0("0 !LPE VERTEX " + bigDecimalToString(vertex.xp) + " " + bigDecimalToString(vertex.yp) + " " + bigDecimalToString(vertex.zp), View.DUMMY_REFERENCE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$)
        getManifestationLock().lock();
        Set<VertexManifestation> manifestations = vertexLinkedToPositionInFile.computeIfAbsent(vertex, v -> Collections.newSetFromMap(new ThreadsafeHashMap<>()));
        manifestations.add(new VertexManifestation(0, vertexTag));
        getManifestationLock().unlock();
        lineLinkedToVertices.put(vertexTag, Collections.newSetFromMap(new ThreadsafeHashMap<>()));
        lineLinkedToVertices.get(vertexTag).add(new VertexInfo(vertex, 0, vertexTag));
        declaredVertices.put(vertexTag, new Vertex[] { vertex });
        return vertexTag;
    }

    public final void clearVertexNormalCache() {
        vertexLinkedToNormalCACHE.clear();
        dataLinkedToNormalCACHE.clear();
    }

    public final void fillVertexNormalCache(GData data2draw) {
        GDataState state = new GDataState();
        data2draw.getVertexNormalMap(state, vertexLinkedToNormalCACHE, dataLinkedToNormalCACHE, this);
        while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
            data2draw.getVertexNormalMap(state, vertexLinkedToNormalCACHE, dataLinkedToNormalCACHE, this);
        }
    }

    public Vector4f getVertexNormal(Vertex min) {
        Vector4f result = new Vector4f(0f, 0f, 0f, 0f);
        Set<VertexManifestation> linked = vertexLinkedToPositionInFile.get(min);
        for (VertexManifestation m : linked) {
            GData g = m.gdata();
            Vector3f n = null;
            switch (g.type()) {
            case 3:
                GData3 g3 = (GData3) g;
                n = new Vector3f(g3.xn, g3.yn, g3.zn);
                break;
            case 4:
                GData4 g4 = (GData4) g;
                n = new Vector3f(g4.xn, g4.yn, g4.zn);
                break;
            default:
                break;
            }
            if (n != null && n.lengthSquared() != 0) {
                n.normalise();
                result.set(n.x + result.x, n.y + result.y, n.z + result.z);
            }
        }
        if (result.lengthSquared() == 0)
            return new Vector4f(0f, 0f, 1f, 1f);
        result.normalise();
        result.setW(1f);
        return result;
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

    public final void delete(boolean moveAdjacentData, boolean setModified) {
        if (linkedDatFile.isReadOnly())
            return;

        if (selectedBgPicture != null && linkedDatFile.getDrawPerLineNoClone().containsValue(selectedBgPicture)) {
            GData before = selectedBgPicture.getBefore();
            GData next = selectedBgPicture.getNext();
            linkedDatFile.getDrawPerLineNoClone().removeByValue(selectedBgPicture);
            before.setNext(next);
            remove(selectedBgPicture);
            selectedBgPicture = null;
            setModifiedNoSync();
        }

        final Set<Vertex> singleVertices = Collections.newSetFromMap(new ThreadsafeSortedMap<>());

        final Set<GData0> effSelectedVertices = new HashSet<>();
        final Set<GData2> effSelectedLines = new HashSet<>();
        final Set<GData3> effSelectedTriangles = new HashSet<>();
        final Set<GData4> effSelectedQuads = new HashSet<>();
        final Set<GData5> effSelectedCondlines = new HashSet<>();

        selectedData.clear();

        // 0. Deselect selected subfile data (for whole selected subfiles)
        for (GData1 subf : selectedSubfiles) {
            Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
            if (vis == null) continue;
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
            final Set<Vertex> objectVertices = Collections.newSetFromMap(new ThreadsafeSortedMap<>());
            {
                Map<GData, Integer> occurMap = new HashMap<>();
                for (Vertex vertex : selectedVertices) {
                    Set<VertexManifestation> occurences = vertexLinkedToPositionInFile.get(vertex);
                    if (occurences == null)
                        continue;
                    boolean isPureSubfileVertex = true;
                    for (VertexManifestation vm : occurences) {
                        GData g = vm.gdata();
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
                            idCheck = !line.parent.equals(View.DUMMY_REFERENCE);
                            isPureSubfileVertex = isPureSubfileVertex && idCheck;
                            if ((moveAdjacentData || val == 2) && !idCheck) {
                                selectedLines.add(line);
                            }
                            break;
                        case 3:
                            GData3 triangle = (GData3) g;
                            idCheck = !triangle.parent.equals(View.DUMMY_REFERENCE);
                            isPureSubfileVertex = isPureSubfileVertex && idCheck;
                            if ((moveAdjacentData || val == 3) && !idCheck) {
                                selectedTriangles.add(triangle);
                            }
                            break;
                        case 4:
                            GData4 quad = (GData4) g;
                            idCheck = !quad.parent.equals(View.DUMMY_REFERENCE);
                            isPureSubfileVertex = isPureSubfileVertex && idCheck;
                            if ((moveAdjacentData || val == 4) && !idCheck) {
                                selectedQuads.add(quad);
                            }
                            break;
                        case 5:
                            GData5 condline = (GData5) g;
                            idCheck = !condline.parent.equals(View.DUMMY_REFERENCE);
                            isPureSubfileVertex = isPureSubfileVertex && idCheck;
                            if ((moveAdjacentData || val == 4) && !idCheck) {
                                selectedCondlines.add(condline);
                            }
                            break;
                        default:
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
                objectVertices.addAll(Arrays.asList(verts));
            }
            for (GData3 triangle : selectedTriangles) {
                if (triangle.parent.equals(View.DUMMY_REFERENCE))
                    effSelectedTriangles.add(triangle);
                Vertex[] verts = triangles.get(triangle);
                if (verts == null)
                    continue;
                objectVertices.addAll(Arrays.asList(verts));
            }
            for (GData4 quad : selectedQuads) {
                if (quad.parent.equals(View.DUMMY_REFERENCE))
                    effSelectedQuads.add(quad);
                Vertex[] verts = quads.get(quad);
                if (verts == null)
                    continue;
                objectVertices.addAll(Arrays.asList(verts));
            }
            for (GData5 condline : selectedCondlines) {
                if (condline.parent.equals(View.DUMMY_REFERENCE))
                    effSelectedCondlines.add(condline);
                Vertex[] verts = condlines.get(condline);
                if (verts == null)
                    continue;
                objectVertices.addAll(Arrays.asList(verts));
            }

            if (moveAdjacentData) {
                singleVertices.addAll(selectedVertices);
                singleVertices.removeAll(objectVertices);
            }

            // 3. Deletion of the selected data (no whole subfiles!!)

            if (!effSelectedVertices.isEmpty())
                setModifiedNoSync();
            if (!effSelectedLines.isEmpty())
                setModifiedNoSync();
            if (!effSelectedTriangles.isEmpty())
                setModifiedNoSync();
            if (!effSelectedQuads.isEmpty())
                setModifiedNoSync();
            if (!effSelectedCondlines.isEmpty())
                setModifiedNoSync();
            final HashBiMap<Integer, GData> dpl = linkedDatFile.getDrawPerLineNoClone();
            for (GData0 gd : effSelectedVertices) {
                dpl.removeByValue(gd);
                gd.getBefore().setNext(gd.getNext());
                remove(gd);
            }
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
                    Set<VertexManifestation> occurences = new HashSet<>(tmp);
                    for (VertexManifestation vm : occurences) {
                        GData g = vm.gdata();
                        if (lineLinkedToVertices.containsKey(g)) {
                            dpl.removeByValue(g);
                            g.getBefore().setNext(g.getNext());
                            remove(g);
                            setModifiedNoSync();
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
                    if (gd.getBefore() != null) gd.getBefore().setNext(gd.getNext());
                    remove(gd);
                }
                selectedSubfiles.clear();
                setModifiedNoSync();
            }

            if (isModified()) {

                // Update Draw per line

                SortedSet<Integer> ts = new TreeSet<>();
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
                    GData0 blankLine = new GData0("", View.DUMMY_REFERENCE); //$NON-NLS-1$
                    linkedDatFile.getDrawChainStart().setNext(blankLine);
                    dpl.put(1, blankLine);
                    linkedDatFile.setDrawChainTail(blankLine);
                }

                if (setModified) syncWithTextEditors(true);
                updateUnsavedStatus();
            }
        }
    }

    /**
     * Replaces GData objects with a new one.
     * @param oldData
     * @param newData
     */
    protected final void linker(GData oldData, GData newData) {
        HashBiMap<Integer, GData> drawPerLine = linkedDatFile.getDrawPerLineNoClone();
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

    public synchronized Map<GData0, Vertex[]> getDeclaredVertices() {
        return new HashMap<>(declaredVertices);
    }

    public final synchronized Map<GData2, Vertex[]> getLines() {
        return new HashMap<>(lines);
    }

    public final synchronized Map<GData3, Vertex[]> getTriangles() {
        return new HashMap<>(triangles);
    }

    public final synchronized ThreadsafeHashMap<GData3, Vertex[]> getTrianglesNoClone() {
        return triangles;
    }

    public final synchronized ThreadsafeHashMap<GData4, Vertex[]> getQuadsNoClone() {
        return quads;
    }

    public final synchronized Map<GData4, Vertex[]> getQuads() {
        return new HashMap<>(quads);
    }

    public final synchronized Map<GData5, Vertex[]> getCondlines() {
        return new HashMap<>(condlines);
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
        getManifestationLock().lock();
        vertexLinkedToPositionInFile.clear();
        getManifestationLock().unlock();
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

    public void skipSyncTimer() {
        skipTimer.set(true);
    }

    public Lock getManifestationLock() {
        return manifestationLock;
    }
}
