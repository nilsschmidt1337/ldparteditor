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
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.composites.compositetab.CompositeTab;
import org.nschmidt.ldparteditor.helpers.math.HashBiMap;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.shells.editortext.EditorTextWindow;
import org.nschmidt.ldparteditor.text.StringHelper;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

public class HistoryManager {

    private DatFile df;

    private boolean hasNoThread = true;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private final AtomicInteger action = new AtomicInteger(0);
    private final ProgressMonitorDialog[] m = new ProgressMonitorDialog[1];
    private final SynchronousQueue<Integer> sq = new SynchronousQueue<Integer>();
    private final Lock lock = new ReentrantLock();

    private Queue<Object[]> workQueue = new ConcurrentLinkedQueue<Object[]>();

    public HistoryManager(DatFile df) {
        this.df = df;
    }

    public void pushHistory(String text, int selectionStart, int selectionEnd, GData[] data, boolean[] selectedData, Vertex[] selectedVertices, int topIndex) {
        if (df.isReadOnly()) return;
        if (hasNoThread) {
            hasNoThread = false;
            new Thread(new Runnable() {
                @Override
                public void run() {

                    final int MAX_ITEM_COUNT = 3; // default is 100

                    boolean restoreWasScuccessful = false;

                    int pointer = 0;
                    int pointerMax = 0;

                    final ArrayList<Integer> historySelectionStart = new ArrayList<Integer>();
                    final ArrayList<Integer> historySelectionEnd = new ArrayList<Integer>();
                    final ArrayList<Integer> historyTopIndex = new ArrayList<Integer>();
                    final ArrayList<int[]> historyText = new ArrayList<int[]>();
                    final ArrayList<boolean[]> historySelectedData = new ArrayList<boolean[]>();
                    final ArrayList<Vertex[]> historySelectedVertices = new ArrayList<Vertex[]>();

                    while (isRunning.get() && Editor3DWindow.getAlive().get()) {
                        try {
                            Object[] newEntry = workQueue.poll();
                            if (newEntry != null) {
                                final int[] result;
                                String text = (String) newEntry[0];
                                GData[] data = (GData[]) newEntry[3];
                                if (text != null) {
                                    result = StringHelper.compress(text);
                                } else if (data != null) {
                                    StringBuilder sb = new StringBuilder();
                                    int size = data.length - 1;
                                    if (size > 0) {
                                        final String ld = StringHelper.getLineDelimiter();
                                        for (int i = 0; i < size; i++) {
                                            sb.append(data[i].toString());
                                            sb.append(ld);
                                        }
                                        sb.append(data[size].toString());
                                    }
                                    result = StringHelper.compress(sb.toString());
                                } else {
                                    // throw new AssertionError("There must be data to backup!"); //$NON-NLS-1$
                                    continue;
                                }

                                NLogger.debug(getClass(), "Pointer   : " + pointer); //$NON-NLS-1$
                                NLogger.debug(getClass(), "PointerMax: " + pointerMax); //$NON-NLS-1$

                                if (pointer != pointerMax) {
                                    // Delete old entries
                                    removeFromListAboveOrEqualIndex(historySelectionStart, pointer);
                                    removeFromListAboveOrEqualIndex(historySelectionEnd, pointer);
                                    removeFromListAboveOrEqualIndex(historySelectedData, pointer);
                                    removeFromListAboveOrEqualIndex(historySelectedVertices, pointer);
                                    removeFromListAboveOrEqualIndex(historyText, pointer);
                                    removeFromListAboveOrEqualIndex(historyTopIndex, pointer);
                                    pointerMax = pointer;
                                }
                                // Dont store more than MAX_ITEM_COUNT undo/redo entries
                                if (pointerMax > MAX_ITEM_COUNT) {
                                    int delta = pointerMax - MAX_ITEM_COUNT;
                                    removeFromListLessIndex(historySelectionStart, delta);
                                    removeFromListLessIndex(historySelectionEnd, delta);
                                    removeFromListLessIndex(historySelectedData, delta);
                                    removeFromListLessIndex(historySelectedVertices, delta);
                                    removeFromListLessIndex(historyText, delta);
                                    removeFromListLessIndex(historyTopIndex, delta);
                                    pointerMax = pointerMax - delta;
                                    if (pointer > MAX_ITEM_COUNT) {
                                        pointer = pointer - delta;
                                    }
                                }

                                historySelectionStart.add((Integer) newEntry[1]);
                                historySelectionEnd.add((Integer) newEntry[2]);
                                historySelectedData.add((boolean[]) newEntry[4]);
                                historySelectedVertices.add((Vertex[]) newEntry[5]);
                                historyTopIndex.add((Integer) newEntry[6]);
                                historyText.add(result);

                                // 1. Cleanup duplicated text entries
                                if (pointer > 0) {
                                    int pStart = historySelectionStart.get(pointer - 1);
                                    int[] previous;
                                    int k = 1;
                                    while ((previous = historyText.get(pointer - k)) == null) {
                                        if (k == pointer) break;
                                        k++;
                                    }
                                    if (previous != null) {
                                        if (previous.length == result.length) {
                                            boolean match = true;
                                            for (int i = 0; i < previous.length; i++) {
                                                int v1 = previous[i];
                                                int v2 = result[i];
                                                if (v1 != v2) {
                                                    match = false;
                                                    break;
                                                }
                                            }

                                            if (match && !Editor3DWindow.getWindow().isAddingSomething()) {
                                                if (pStart != -1) {
                                                    if ((Integer) newEntry[2] == 0) {
                                                        // Skip saving this entry since only the cursor was moved
                                                        removeFromListAboveOrEqualIndex(historySelectionStart, pointer);
                                                        removeFromListAboveOrEqualIndex(historySelectionEnd, pointer);
                                                        removeFromListAboveOrEqualIndex(historySelectedData, pointer);
                                                        removeFromListAboveOrEqualIndex(historySelectedVertices, pointer);
                                                        removeFromListAboveOrEqualIndex(historyText, pointer);
                                                        removeFromListAboveOrEqualIndex(historyTopIndex, pointer);
                                                    } else {
                                                        // Remove the previous entry, because it only contains a new text selection
                                                        historySelectionStart.remove(pointer - 1);
                                                        historySelectionEnd.remove(pointer - 1);
                                                        historySelectedData.remove(pointer - 1);
                                                        historySelectedVertices.remove(pointer - 1);
                                                        historyTopIndex.remove(pointer - 1);
                                                        historyText.remove(pointer - 1);
                                                    }
                                                    pointerMax--;
                                                    pointer--;
                                                }
                                            }
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
                                    restoreWasScuccessful = false;

                                    boolean doRestore = false;
                                    switch (action2) {
                                    case 1:
                                        // Undo
                                        if (pointer > 0) {
                                            if (pointerMax == pointer && pointer > 1) pointer--;
                                            NLogger.debug(getClass(), "Requested undo. " + (pointer - 1)); //$NON-NLS-1$
                                            pointer--;
                                            delta = -1;
                                            doRestore = true;
                                        }
                                        break;
                                    case 2:
                                        // Redo
                                        if (pointer < pointerMax - 1) {
                                            NLogger.debug(getClass(), "Requested redo. " + (pointer + 1) + ' ' + pointerMax); //$NON-NLS-1$
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
                                        while (!hasTextEditor && historySelectionStart.get(pointer) != -1 && pointer > 0 && pointer < pointerMax - 1) {
                                            pointer += delta;
                                        }
                                        int[] text = historyText.get(pointer);
                                        final int pointer2 = pointer;
                                        if (text == null) {
                                            // FIXME This case should not happen! Class needs better documentation!
                                            action.set(0);
                                            return;
                                        }
                                        NLogger.debug(getClass(), "Waiting for monitor..."); //$NON-NLS-1$
                                        sq.put(10);
                                        if (m[0] == null || m[0].getShell() == null) {
                                            NLogger.error(getClass(), "Monitor creation failed!"); //$NON-NLS-1$
                                            action.set(0);
                                            hasNoThread = true;
                                            return;
                                        }
                                        NLogger.debug(getClass(), "Accepted monitor."); //$NON-NLS-1$
                                        final String decompressed = StringHelper.decompress(text);
                                        Display.getDefault().syncExec(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    sq.put(20);
                                                } catch (InterruptedException e) {
                                                }
                                                GDataCSG.resetCSG();
                                                GDataCSG.forceRecompile();
                                                Project.getUnsavedFiles().add(df);
                                                df.setText(decompressed);
                                                m[0].getShell().redraw();
                                                m[0].getShell().update();
                                                m[0].getShell().getDisplay().readAndDispatch();
                                                df.parseForData(false);
                                                try {
                                                    sq.put(60);
                                                } catch (InterruptedException e) {
                                                }
                                                m[0].getShell().redraw();
                                                m[0].getShell().update();
                                                m[0].getShell().getDisplay().readAndDispatch();
                                                boolean hasTextEditor = false;
                                                try {
                                                    for (EditorTextWindow w : Project.getOpenTextWindows()) {
                                                        for (final CTabItem t : w.getTabFolder().getItems()) {
                                                            final DatFile txtDat = ((CompositeTab) t).getState().getFileNameObj();
                                                            if (txtDat != null && txtDat.equals(df)) {
                                                                int ti = ((CompositeTab) t).getTextComposite().getTopIndex();
                                                                Point r = ((CompositeTab) t).getTextComposite().getSelectionRange();
                                                                if (openTextEditor) {
                                                                    r.x = historySelectionStart.get(pointer2);
                                                                    r.y = historySelectionEnd.get(pointer2);
                                                                    ti = historyTopIndex.get(pointer2);
                                                                }
                                                                ((CompositeTab) t).getState().setSync(true);
                                                                ((CompositeTab) t).getTextComposite().setText(decompressed);
                                                                ((CompositeTab) t).getTextComposite().setTopIndex(ti);
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

                                                    switch (action2) {
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
                                                final Vertex[] verts = historySelectedVertices.get(pointer2);
                                                if (verts != null) {
                                                    for (Vertex vertex : verts) {
                                                        vm.getSelectedVertices().add(vertex);
                                                    }
                                                }
                                                boolean[] selection = historySelectedData.get(pointer2);
                                                if (selection != null) {
                                                    int i = 0;
                                                    final HashBiMap<Integer, GData> map = df.getDrawPerLine_NOCLONE();
                                                    TreeSet<Integer> ts = new TreeSet<Integer>(map.keySet());
                                                    for (Integer key : ts) {
                                                        if (selection[i]) {
                                                            GData gd = map.getValue(key);
                                                            vm.getSelectedData().add(gd);
                                                            switch (gd.type()) {
                                                            case 1:
                                                                vm.getSelectedSubfiles().add((GData1) gd);
                                                                break;
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
                                                        i++;
                                                    }
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

                                                // Never ever call "vm.setModified_NoSync();" here. NEVER delete the following lines.
                                                vm.setModified(false, false);
                                                vm.setUpdated(true);
                                                vm.setSkipSyncWithTextEditor(false);

                                                Editor3DWindow.getWindow().updateTree_unsavedEntries();
                                                try {
                                                    sq.put(10);
                                                } catch (InterruptedException e) {
                                                }
                                            }
                                        });
                                    }
                                    action.set(0);
                                    restoreWasScuccessful = true;
                                } else {
                                    restoreWasScuccessful = true;
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

                        if (!restoreWasScuccessful) {
                            action.set(0);
                        }
                        restoreWasScuccessful = false;
                    }
                }
            }).start();
        }

        while (!workQueue.offer(new Object[]{text, selectionStart, selectionEnd, data, selectedData, selectedVertices, topIndex})) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}
        }

    }

    public void deleteHistory() {
        isRunning.set(false);
    }

    public void undo(final Shell sh) {
        if (lock.tryLock()) {
            try {
                action(1, sh);
            } finally {
                lock.unlock();
            }
        } else {
            NLogger.debug(getClass(), "Undo was skipped due to synchronisation."); //$NON-NLS-1$
        }
    }

    public void redo(final Shell sh) {
        if (lock.tryLock()) {
            try {
                action(2, sh);
            } finally {
                lock.unlock();
            }
        } else {
            NLogger.debug(getClass(), "Redo was skipped due to synchronisation."); //$NON-NLS-1$
        }
    }

    private void action(final int mode, final Shell sh) {
        if (df.isReadOnly() || !df.getVertexManager().isUpdated() && WorkbenchManager.getUserSettingState().getSyncWithTextEditor().get()) return;
        BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
            @Override
            public void run() {
                try
                {
                    final ProgressMonitorDialog mon = new ProgressMonitorDialog(sh == null ? Editor3DWindow.getWindow().getShell() : sh);
                    mon.run(true, false, new IRunnableWithProgress()
                    {
                        @Override
                        public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                        {
                            try
                            {
                                if (action.get() == 0) {
                                    action.set(mode);
                                }
                                m[0] = mon;
                                NLogger.debug(getClass(), "Provided Monitor..."); //$NON-NLS-1$
                                monitor.beginTask(I18n.E3D_LoadingData, 100);
                                while (action.get() > 0) {
                                    Integer inc = sq.poll(1000, TimeUnit.MILLISECONDS);
                                    if (inc != null) {
                                        monitor.worked(inc);
                                        NLogger.debug(getClass(), "Polled progress info. (" + inc + ")"); //$NON-NLS-1$ //$NON-NLS-2$
                                    } else {
                                        NLogger.debug(getClass(), "Progress info has timed out."); //$NON-NLS-1$
                                    }
                                }
                            } catch (Exception ex) {

                                // We want to know what can go wrong here
                                // because it SHOULD be avoided!!

                                switch (mode) {
                                case 1:
                                    NLogger.error(getClass(), "Undo failed within the ProgressMonitor.run() call."); //$NON-NLS-1$
                                    break;
                                case 2:
                                    NLogger.error(getClass(), "Redo failed within the ProgressMonitor.run() call."); //$NON-NLS-1$
                                    break;
                                default:
                                    // Can't happen
                                    break;
                                }

                                NLogger.error(getClass(), ex);
                            }
                        }
                    });
                } catch (Exception undoRedoException) {

                    // We want to know what can go wrong here
                    // because it SHOULD be avoided!!

                    switch (mode) {
                    case 1:
                        NLogger.error(getClass(), "Undo failed while the ProgressMonitor was shown."); //$NON-NLS-1$
                        break;
                    case 2:
                        NLogger.error(getClass(), "Redo failed while the ProgressMonitor was shown."); //$NON-NLS-1$
                        break;
                    default:
                        // Can't happen
                        break;
                    }

                    NLogger.error(getClass(), undoRedoException);
                }
            }
        });
        df.getVertexManager().setSelectedBgPicture(null);
        df.getVertexManager().setSelectedBgPictureIndex(0);
        Editor3DWindow.getWindow().updateBgPictureTab();
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
