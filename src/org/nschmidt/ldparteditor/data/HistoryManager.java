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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Point;
import org.nschmidt.ldparteditor.composites.compositetab.CompositeTab;
import org.nschmidt.ldparteditor.helpers.composite3d.GuiStatusManager;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.shells.editortext.EditorTextWindow;
import org.nschmidt.ldparteditor.text.StringHelper;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

class HistoryManager {

    private static final Pattern pattern = Pattern.compile("\r?\n|\r"); //$NON-NLS-1$

    private DatFile df;

    private boolean hasNoThread = true;
    private volatile AtomicBoolean isRunning = new AtomicBoolean(true);
    private volatile AtomicInteger action = new AtomicInteger(0);
    private final Lock lock = new ReentrantLock();
    private volatile int mode = 0;

    private volatile Queue<Object[]> workQueue = new ConcurrentLinkedQueue<>();
    private volatile Queue<Object[]> answerQueue = new ConcurrentLinkedQueue<>();

    HistoryManager(DatFile df) {
        this.df = df;
    }

    void pushHistory(String text, int selectionStart, int selectionEnd, GData[] data, HashMap<String, ArrayList<Boolean>> selectedData, HashMap<String, ArrayList<Boolean>> hiddenData, Vertex[] selectedVertices, Vertex[] hiddenVertices, int topIndex) {
        if (df.isReadOnly()) return;
        if (hasNoThread) {
            hasNoThread = false;
            new Thread(new Runnable() {
                @SuppressWarnings("unchecked")
                @Override
                public void run() {

                    final int MAX_ITEM_COUNT = 100; // default is 100

                    int pointer = 0;
                    int pointerMax = 0;

                    final ArrayList<Integer> historySelectionStart = new ArrayList<>();
                    final ArrayList<Integer> historySelectionEnd = new ArrayList<>();
                    final ArrayList<Integer> historyTopIndex = new ArrayList<>();
                    final ArrayList<String> historyFullText = new ArrayList<>();
                    final ArrayList<String[]> historyText = new ArrayList<>();
                    final ArrayList<HashMap<String, ArrayList<Boolean>>> historySelectedData = new ArrayList<>();
                    final ArrayList<HashMap<String, ArrayList<Boolean>>> historyHiddenData = new ArrayList<>();
                    final ArrayList<Vertex[]> historySelectedVertices = new ArrayList<>();
                    final ArrayList<Vertex[]> historyHiddenVertices = new ArrayList<>();

                    while (isRunning.get() && Editor3DWindow.getAlive().get()) {
                        try {
                            Object[] newEntry = workQueue.poll();
                            if (newEntry != null) {
                                final String[] result;
                                final String resultFullText;
                                String text = (String) newEntry[0];
                                GData[] data = (GData[]) newEntry[3];
                                if (text != null && !text.isEmpty()) {
                                    if (text.charAt(text.length() - 1) == '\r') {
                                        text = text.substring(0, text.length() - 1);
                                    }
                                    if (text.length() > 0 && text.charAt(text.length() - 1) == '\n') {
                                        text = text.substring(0, text.length() - 1);
                                    }
                                    if (text.length() > 0 && text.charAt(text.length() - 1) == '\r') {
                                        text = text.substring(0, text.length() - 1);
                                    }
                                    if (text.length() > 0 && text.charAt(text.length() - 1) == '\n') {
                                        text = text.substring(0, text.length() - 1);
                                    }
                                    String[] result2 = pattern.split(text);
                                    if (result2.length == 0) {
                                        result = new String[]{""}; //$NON-NLS-1$
                                    } else {
                                        result = result2;
                                    }
                                    resultFullText = text;
                                } else if (data != null) {
                                    final int size = data.length;
                                    if (size > 0) {
                                        final String ld = StringHelper.getLineDelimiter();
                                        final StringBuilder sb = new StringBuilder();
                                        result = new String[size];
                                        for (int i = 0; i < size; i++) {
                                            if (i > 0) {
                                                sb.append(ld);
                                            }
                                            result[i] = data[i].toString();
                                            sb.append(result[i]);
                                        }
                                        resultFullText = sb.toString();
                                    } else {
                                        result = new String[]{""}; //$NON-NLS-1$
                                        resultFullText = result[0];
                                    }
                                } else {
                                    // throw new AssertionError("There must be data to backup!"); //$NON-NLS-1$
                                    continue;
                                }

                                NLogger.debug(getClass(), "Pointer   : {0}", pointer); //$NON-NLS-1$
                                NLogger.debug(getClass(), "PointerMax: {0}", pointerMax); //$NON-NLS-1$
                                NLogger.debug(getClass(), "Item Count: {0}", historyText.size()); //$NON-NLS-1$

                                if (pointer != pointerMax) {
                                    // Delete old entries
                                    removeFromListAboveOrEqualIndex(historySelectionStart, pointer + 1);
                                    removeFromListAboveOrEqualIndex(historySelectionEnd, pointer + 1);
                                    removeFromListAboveOrEqualIndex(historySelectedData, pointer + 1);
                                    removeFromListAboveOrEqualIndex(historyHiddenData, pointer + 1);
                                    removeFromListAboveOrEqualIndex(historySelectedVertices, pointer + 1);
                                    removeFromListAboveOrEqualIndex(historyHiddenVertices, pointer + 1);
                                    removeFromListAboveOrEqualIndex(historyText, pointer + 1);
                                    removeFromListAboveOrEqualIndex(historyFullText, pointer + 1);
                                    removeFromListAboveOrEqualIndex(historyTopIndex, pointer + 1);
                                    pointerMax = pointer + 1;
                                }
                                // Dont store more than MAX_ITEM_COUNT undo/redo entries
                                {
                                    final int item_count = historyText.size();
                                    if (item_count > MAX_ITEM_COUNT) {
                                        int delta = item_count - MAX_ITEM_COUNT;
                                        removeFromListLessIndex(historySelectionStart, delta + 1);
                                        removeFromListLessIndex(historySelectionEnd, delta + 1);
                                        removeFromListLessIndex(historySelectedData, delta + 1);
                                        removeFromListLessIndex(historyHiddenData, delta + 1);
                                        removeFromListLessIndex(historySelectedVertices, delta + 1);
                                        removeFromListLessIndex(historyHiddenVertices, delta + 1);
                                        removeFromListLessIndex(historyText, delta + 1);
                                        removeFromListLessIndex(historyFullText, delta + 1);
                                        removeFromListLessIndex(historyTopIndex, delta + 1);
                                        pointerMax = pointerMax - delta;
                                        if (pointer > MAX_ITEM_COUNT) {
                                            pointer = pointer - delta;
                                        }
                                    }
                                }

                                historySelectionStart.add((Integer) newEntry[1]);
                                historySelectionEnd.add((Integer) newEntry[2]);
                                historySelectedData.add((HashMap<String, ArrayList<Boolean>>) newEntry[4]);
                                historySelectedVertices.add((Vertex[]) newEntry[5]);
                                historyTopIndex.add((Integer) newEntry[6]);
                                historyHiddenData.add((HashMap<String, ArrayList<Boolean>>) newEntry[7]);
                                historyHiddenVertices.add((Vertex[]) newEntry[8]);
                                historyText.add(result);
                                historyFullText.add(resultFullText);

                                // 1. Cleanup duplicated text entries

                                if (pointer > 0) {
                                    int pStart = historySelectionStart.get(pointer - 1);
                                    String[] previous = historyText.get(pointer - 1);
                                    if (Arrays.equals(previous, result) && !Editor3DWindow.getWindow().isAddingSomething()) {
                                        if (pStart != -1) {
                                            if ((Integer) newEntry[2] == 0) {
                                                // Skip saving this entry since only the cursor was moved
                                                removeFromListAboveOrEqualIndex(historySelectionStart, pointer);
                                                removeFromListAboveOrEqualIndex(historySelectionEnd, pointer);
                                                removeFromListAboveOrEqualIndex(historySelectedData, pointer);
                                                removeFromListAboveOrEqualIndex(historyHiddenData, pointer);
                                                removeFromListAboveOrEqualIndex(historySelectedVertices, pointer);
                                                removeFromListAboveOrEqualIndex(historyHiddenVertices, pointer);
                                                removeFromListAboveOrEqualIndex(historyText, pointer);
                                                removeFromListAboveOrEqualIndex(historyFullText, pointer);
                                                removeFromListAboveOrEqualIndex(historyTopIndex, pointer);
                                            } else {
                                                // Remove the previous entry, because it only contains a new text selection
                                                historySelectionStart.remove(pointer - 1);
                                                historySelectionEnd.remove(pointer - 1);
                                                historySelectedData.remove(pointer - 1);
                                                historyHiddenData.remove(pointer - 1);
                                                historySelectedVertices.remove(pointer - 1);
                                                historyHiddenVertices.remove(pointer - 1);
                                                historyText.remove(pointer - 1);
                                                historyFullText.remove(pointer - 1);
                                                historyTopIndex.remove(pointer - 1);
                                            }
                                            pointerMax--;
                                            pointer--;
                                        }
                                    }
                                }

                                // FIXME 2. There is still more cleanup work to do

                                pointerMax++;
                                pointer++;
                                NLogger.debug(getClass(), "Added undo/redo data"); //$NON-NLS-1$
                                if (workQueue.isEmpty()) Thread.sleep(100);
                            } else {
                                final int action2 = action.get();
                                int delta = 0;
                                if (action2 > 0 && action2 < 3) {
                                    boolean doRestore = false;
                                    switch (action2) {
                                    case 1:
                                        // Undo
                                        if (pointer > 0) {
                                            if (pointerMax == pointer && pointer > 1) pointer--;
                                            NLogger.debug(getClass(), "Requested undo."); //$NON-NLS-1$
                                            pointer--;
                                            delta = -1;
                                            doRestore = true;
                                        }
                                        break;
                                    case 2:
                                        // Redo
                                        if (pointer < pointerMax - 1 && pointer + 1 < historySelectionStart.size()) {
                                            NLogger.debug(getClass(), "Requested redo."); //$NON-NLS-1$
                                            pointer++;
                                            delta = 1;
                                            doRestore = true;
                                        }
                                        break;
                                    }
                                    if (doRestore) {
                                        df.getVertexManager().setSkipSyncWithTextEditor(true);

                                        final boolean openTextEditor = historySelectionStart.get(pointer) != -1;
                                        boolean hasTextEditor = false;
                                        for (EditorTextWindow w : Project.getOpenTextWindows()) {
                                            for (final CTabItem t : w.getTabFolder().getItems()) {
                                                final DatFile txtDat = ((CompositeTab) t).getState().getFileNameObj();
                                                if (txtDat != null && txtDat.equals(df)) {
                                                    hasTextEditor = true;
                                                    break;
                                                }
                                            }
                                            if (hasTextEditor) break;
                                        }
                                        while (!hasTextEditor && pointer + delta > -1 && pointer + delta < historySelectionStart.size() && historySelectionStart.get(pointer) != -1 && pointer > 0 && pointer < pointerMax - 1) {
                                            pointer += delta;
                                        }
                                        final int start = historySelectionStart.get(pointer);
                                        final int end = historySelectionEnd.get(pointer);
                                        final int topIndex = historyTopIndex.get(pointer);
                                        final String fullText = historyFullText.get(pointer);
                                        final String[] lines = historyText.get(pointer);
                                        HashMap<String, ArrayList<Boolean>> selection = historySelectedData.get(pointer);
                                        HashMap<String, ArrayList<Boolean>> hiddenSelection = historyHiddenData.get(pointer);
                                        final Vertex[] verts = historySelectedVertices.get(pointer);
                                        final Vertex[] verts2 = historyHiddenVertices.get(pointer);
                                        while (!answerQueue.offer(new Object[]{
                                                openTextEditor,
                                                start,
                                                end,
                                                topIndex,
                                                fullText,
                                                lines,
                                                selection,
                                                hiddenSelection,
                                                verts,
                                                verts2,
                                                false
                                        })) {
                                            try {
                                                Thread.sleep(100);
                                            } catch (InterruptedException e) {}
                                        }
                                    } else {
                                        while (!answerQueue.offer(new Object[]{
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                true
                                        })) {
                                            try {
                                                Thread.sleep(100);
                                            } catch (InterruptedException e) {}
                                        }
                                    }
                                    action.set(0);
                                } else {
                                    if (workQueue.isEmpty()) Thread.sleep(100);
                                }
                            }
                        } catch (InterruptedException e) {
                            // We want to know what can go wrong here
                            // because it SHOULD be avoided!!

                            NLogger.error(getClass(), "The HistoryManager cycle was interruped [InterruptedException]! :("); //$NON-NLS-1$
                            NLogger.error(getClass(), e);
                        } catch (Exception e) {
                            NLogger.error(getClass(), "The HistoryManager cycle was throwing an exception :("); //$NON-NLS-1$
                            NLogger.error(getClass(), e);
                        }

                    }
                    action.set(0);
                }
            }).start();
        }

        while (!workQueue.offer(new Object[]{text, selectionStart, selectionEnd, data, selectedData, selectedVertices, topIndex, hiddenData, hiddenVertices})) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}
        }

    }

    void deleteHistory() {
        isRunning.set(false);
    }

    void undo(boolean focusTextEditor) {
        if (lock.tryLock()) {
            try {
                action(1, focusTextEditor);
            } finally {
                lock.unlock();
            }
        } else {
            NLogger.debug(getClass(), "Undo was skipped due to synchronisation."); //$NON-NLS-1$
        }
    }

    void redo(boolean focusTextEditor) {
        if (lock.tryLock()) {
            try {
                action(2, focusTextEditor);
            } finally {
                lock.unlock();
            }
        } else {
            NLogger.debug(getClass(), "Redo was skipped due to synchronisation."); //$NON-NLS-1$
        }
    }

    @SuppressWarnings("unchecked")
    private void action(final int action_mode, final boolean focusTextEditor) {
        if (action.get() != 0 || df.isReadOnly() || !df.getVertexManager().isUpdated() && WorkbenchManager.getUserSettingState().getSyncWithTextEditor().get()) return;

        mode = action_mode;

        action.set(mode);
        if (!isRunning.get()) {
            hasNoThread = true;
            isRunning.set(true);
            pushHistory(null, -1, -1, null, null, null, null, null, -1);
            NLogger.debug(getClass(), "Forked history thread..."); //$NON-NLS-1$
        }

        boolean openTextEditor = false;
        int start = -1;
        int end = -1;
        int topIndex = -1;
        String fullText = null;
        String[] lines = null;
        HashMap<String, ArrayList<Boolean>> selection = null;
        HashMap<String, ArrayList<Boolean>> hiddenSelection = null;
        Vertex[] verts = null;
        Vertex[] verts2 = null;
        while (true) {
            Object[] newEntry = answerQueue.poll();
            if (newEntry != null) {
                if ((boolean) newEntry[10]) {
                    action.set(0);
                    return;
                }
                openTextEditor = (boolean) newEntry[0];
                start = (int) newEntry[1];
                end = (int) newEntry[2];
                topIndex = (int) newEntry[3];
                fullText = (String) newEntry[4];
                lines = (String[]) newEntry[5];
                selection = (HashMap<String, ArrayList<Boolean>>) newEntry[6];
                hiddenSelection = (HashMap<String, ArrayList<Boolean>>) newEntry[7];
                verts = (Vertex[]) newEntry[8];
                verts2 = (Vertex[]) newEntry[9];
                break;
            }
            if (answerQueue.isEmpty()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
        }

        df.parseForChanges(lines);
        GDataCSG.resetCSG(df, false);
        GDataCSG.forceRecompile(df);
        Project.getUnsavedFiles().add(df);
        df.setText(fullText);
        boolean hasTextEditor = false;
        try {
            for (EditorTextWindow w : Project.getOpenTextWindows()) {
                for (final CTabItem t : w.getTabFolder().getItems()) {
                    final DatFile txtDat = ((CompositeTab) t).getState().getFileNameObj();
                    if (txtDat != null && txtDat.equals(df)) {
                        int ti = ((CompositeTab) t).getTextComposite().getTopIndex();
                        Point r = ((CompositeTab) t).getTextComposite().getSelectionRange();
                        if (openTextEditor) {
                            r.x = start;
                            r.y = end;
                            ti = topIndex;
                        }
                        ((CompositeTab) t).getState().setSync(true);
                        ((CompositeTab) t).getTextComposite().setText(fullText);
                        ((CompositeTab) t).getTextComposite().setTopIndex(ti);
                        if (focusTextEditor) {
                            ((CompositeTab) t).getTextComposite().forceFocus();
                        }
                        try {
                            ((CompositeTab) t).getTextComposite().setSelectionRange(r.x, r.y);
                        } catch (IllegalArgumentException consumed) {}
                        ((CompositeTab) t).getTextComposite().redraw();
                        ((CompositeTab) t).getControl().redraw();
                        ((CompositeTab) t).getTextComposite().update();
                        ((CompositeTab) t).getControl().update();
                        ((CompositeTab) t).getState().setSync(false);
                        hasTextEditor = true;
                        break;
                    }
                }
                if (hasTextEditor) break;
            }
        } catch (Exception undoRedoException) {

            // We want to know what can go wrong here
            // because it SHOULD be avoided!!

            switch (action_mode) {
            case 1:
                NLogger.error(getClass(), "Undo failed."); //$NON-NLS-1$
                break;
            case 2:
                NLogger.error(getClass(), "Redo failed."); //$NON-NLS-1$
                break;
            default:
                // Can't happen
                break;
            }

            NLogger.error(getClass(), undoRedoException);
        }

        final VertexManager vm = df.getVertexManager();

        vm.clearSelection2();

        if (verts != null) {
            for (Vertex vertex : verts) {
                vm.getSelectedVertices().add(vertex);
            }
        }

        if (verts2 != null) {
            vm.getHiddenVertices().clear();
            for (Vertex vertex : verts2) {
                vm.getHiddenVertices().add(vertex);
            }
        }

        if (hiddenSelection != null) {
            if (hiddenSelection.isEmpty() && !vm.hiddenData.isEmpty()) {
                vm.showAll();
            } else {
                vm.hiddenData.clear();
                vm.restoreHideShowState(hiddenSelection);
            }
        }
        if (selection != null) {
            vm.restoreSelectedDataState(selection);
            ThreadsafeHashMap<GData, Set<VertexInfo>> llv = vm.getLineLinkedToVertices();
            for (GData1 g1 : vm.getSelectedSubfiles()) {
                final Set<VertexInfo> vis = llv.get(g1);
                if (vis != null) {
                    for (VertexInfo vi : vis) {
                        vm.getSelectedVertices().add(vi.vertex);
                        GData gd = vi.getLinkedData();
                        vm.getSelectedData().add(gd);
                        switch (gd.type()) {
                        case 2:
                            vm.getSelectedLines().add((GData2) gd);
                            break;
                        case 3:
                            vm.getSelectedTriangles().add((GData3) gd);
                            break;
                        case 4:
                            vm.getSelectedQuads().add((GData4) gd);
                            break;
                        case 5:
                            vm.getSelectedCondlines().add((GData5) gd);
                            break;
                        default:
                            break;
                        }
                    }
                }
            }
        }
        vm.updateUnsavedStatus();

        // Redraw the content of the StyledText (to see the selection)
        try {
            for (EditorTextWindow w : Project.getOpenTextWindows()) {
                for (final CTabItem t : w.getTabFolder().getItems()) {
                    if (df.equals(((CompositeTab) t).getState().getFileNameObj())) {
                        ((CompositeTab) t).getTextComposite().redraw();
                        hasTextEditor = true;
                        break;
                    }
                }
                if (hasTextEditor) break;
            }
        } catch (Exception undoRedoException) {

            // We want to know what can go wrong here
            // because it SHOULD be avoided!!

            switch (action_mode) {
            case 1:
                NLogger.error(getClass(), "Undo StyledText redraw failed."); //$NON-NLS-1$
                break;
            case 2:
                NLogger.error(getClass(), "Redo StyledText redraw failed."); //$NON-NLS-1$
                break;
            default:
                // Can't happen
                break;
            }

            NLogger.error(getClass(), undoRedoException);
        }

        // Never ever call "vm.setModified_NoSync();" here. NEVER delete the following lines.
        vm.setModified(false, false);
        vm.setUpdated(true);
        vm.setSkipSyncWithTextEditor(false);

        Editor3DWindow.getWindow().updateTree_unsavedEntries();
        action.set(0);
        df.getVertexManager().setSelectedBgPicture(null);
        df.getVertexManager().setSelectedBgPictureIndex(0);
        Editor3DWindow.getWindow().updateBgPictureTab();
        GuiStatusManager.updateStatus(df);
        NLogger.debug(getClass(), "done."); //$NON-NLS-1$
    }

    private void removeFromListAboveOrEqualIndex(List<?> l, int i) {
        i--;
        for (int j = l.size() - 1; j > i; j--) {
            l.remove(j);
        }
    }

    private void removeFromListLessIndex(List<?> l, int i) {
        i--;
        for (int j = i - 1; j > -1; j--) {
            l.remove(j);
        }
    }

    public void setDatFile(DatFile df) {
        this.df = df;
    }

    public Lock getLock() {
        return lock;
    }
}
