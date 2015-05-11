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
import org.nschmidt.ldparteditor.composites.compositetab.CompositeTab;
import org.nschmidt.ldparteditor.helpers.math.HashBiMap;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeHashMap;
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
                                // Dont store more than hundred undo/redo entries
                                if (pointerMax > 100) {
                                    int delta = pointerMax - 100;
                                    removeFromListLessIndex(historySelectionStart, delta);
                                    removeFromListLessIndex(historySelectionEnd, delta);
                                    removeFromListLessIndex(historySelectedData, delta);
                                    removeFromListLessIndex(historySelectedVertices, delta);
                                    removeFromListLessIndex(historyText, delta);
                                    removeFromListLessIndex(historyTopIndex, delta);
                                    pointerMax = pointerMax - delta;
                                    if (pointer > 100) {
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
                                                        if (historyText.get(pointer - 1) == null) {
                                                            historyText.remove(pointer - 1);
                                                            historyText.remove(pointer);
                                                            historyText.add(null);
                                                        } else {
                                                            historyText.remove(pointer - 1);
                                                        }
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
                                if (action2 > 0) {
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
                                    default:
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
                                        final int pointer2 = pointer;
                                        int[] text = null;
                                        int k = 0;
                                        while ((text = historyText.get(pointer2 - k)) == null) {
                                            k++;
                                            if (pointer2 == k) break;
                                        }
                                        if (text == null) {
                                            action.set(0);
                                            return;
                                        }
                                        sq.offer(10);
                                        final String decompressed = StringHelper.decompress(text);
                                        Display.getDefault().syncExec(new Runnable() {
                                            @Override
                                            public void run() {
                                                GDataCSG.resetCSG();
                                                GDataCSG.forceRecompile();
                                                Project.getUnsavedFiles().add(df);
                                                df.setText(decompressed);
                                                sq.offer(20);
                                                m[0].getShell().redraw();
                                                m[0].getShell().update();
                                                m[0].getShell().getDisplay().readAndDispatch();
                                                df.parseForData(false);
                                                sq.offer(60);
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
                                                } catch (Exception consumed) {
                                                    NLogger.debug(getClass(), consumed);
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

                                                vm.setModified_NoSync();
                                                vm.setUpdated(true);
                                                vm.setSkipSyncWithTextEditor(false);
                                                //                                                vm.setSkipSyncWithTextEditor(false);
                                                //                                                if (hasTextEditor) {
                                                //                                                    vm.setModified_NoSync();
                                                //                                                    vm.syncWithTextEditors(false);
                                                //                                                } else {
                                                //                                                    vm.setModified(true, false);
                                                //                                                }
                                                //                                                if (openTextEditor || hasTextEditor) {
                                                //                                                    vm.setUpdated(true);
                                                //                                                }
                                                Editor3DWindow.getWindow().updateTree_unsavedEntries();
                                                sq.offer(10);
                                            }
                                        });
                                    }
                                    action.set(0);
                                    sq.offer(0);
                                } else {
                                    if (workQueue.isEmpty()) Thread.sleep(100);
                                }
                            }
                        } catch (InterruptedException e) {
                        } catch (Exception e) {
                            NLogger.debug(getClass(), e);
                        }
                    }
                    // TODO Cleanup the data here
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

    public void undo() {
        if (lock.tryLock()) {
            try {
                action(1);
            } finally {
                lock.unlock();
            }
        } else {
            NLogger.debug(getClass(), "Undo was skipped due to synchronisation."); //$NON-NLS-1$
        }
    }

    public void redo() {
        if (lock.tryLock()) {
            try {
                action(2);
            } finally {
                lock.unlock();
            }
        } else {
            NLogger.debug(getClass(), "Redo was skipped due to synchronisation."); //$NON-NLS-1$
        }
    }

    private void action(final int mode) {
        if (df.isReadOnly() || !df.getVertexManager().isUpdated() && WorkbenchManager.getUserSettingState().getSyncWithTextEditor().get()) return;
        if (action.get() == 0) {
            BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
                @Override
                public void run() {
                    action.set(mode);
                    Display.getCurrent().readAndDispatch();
                }
            });
        }
        BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
            @Override
            public void run() {
                try
                {
                    final ProgressMonitorDialog mon = new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell());
                    mon.run(true, false, new IRunnableWithProgress()
                    {
                        @Override
                        public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                        {
                            m[0] = mon;
                            monitor.beginTask("Loading Data...", 100); //$NON-NLS-1$ I18N
                            while (action.get() > 0) {
                                Integer inc = sq.poll(1000, TimeUnit.MILLISECONDS);
                                if (inc != null) {
                                    monitor.worked(inc);
                                }
                            }
                        }
                    });
                } catch (Exception ex) {
                    NLogger.debug(getClass(), ex);
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
