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
import java.util.List;
import java.util.Queue;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.nschmidt.ldparteditor.helpers.math.HashBiMap;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.text.StringHelper;

// FIXME Needs implementation!!
public class HistoryManager {

    private DatFile df;

    private boolean hasNoThread = true;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private final AtomicInteger action = new AtomicInteger(0);

    private Queue<Object[]> workQueue = new ConcurrentLinkedQueue<Object[]>();

    public HistoryManager(DatFile df) {
        this.df = df;
    }

    public void pushHistory(String text, int selectionStart, int selectionEnd, GData[] data, boolean[] selectedData, Vertex[] selectedVertices) {
        if (hasNoThread) {
            hasNoThread = false;
            new Thread(new Runnable() {
                @Override
                public void run() {

                    int pointer = 0;
                    int pointerMax = 0;

                    final ArrayList<Integer> historySelectionStart = new ArrayList<Integer>();
                    final ArrayList<Integer> historySelectionEnd = new ArrayList<Integer>();
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
                                    pointerMax = pointerMax - delta;
                                    if (pointer > 100) {
                                        pointer = pointer - delta;
                                    }
                                }

                                historySelectionStart.add((Integer) newEntry[1]);
                                historySelectionEnd.add((Integer) newEntry[2]);
                                historySelectedData.add((boolean[]) newEntry[4]);
                                historySelectedVertices.add((Vertex[]) newEntry[5]);
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
                                                    } else {
                                                        // Remove the previous entry, because it only contains a new text selection
                                                        historySelectionStart.remove(pointer - 1);
                                                        historySelectionEnd.remove(pointer - 1);
                                                        historySelectedData.remove(pointer - 1);
                                                        historySelectedVertices.remove(pointer - 1);
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
                            } else {
                                final int action2 = action.get();
                                if (action2 > 0) {
                                    boolean doRestore = false;
                                    switch (action2) {
                                    case 1:
                                        // Undo
                                        if (pointer > 0) {
                                            NLogger.debug(getClass(), "Requested undo. " + (pointer - 1)); //$NON-NLS-1$
                                            pointer--;
                                            doRestore = true;
                                        }
                                        break;
                                    case 2:
                                        // Redo
                                        if (pointer < pointerMax - 1) {
                                            NLogger.debug(getClass(), "Requested redo. " + (pointer + 1)); //$NON-NLS-1$
                                            pointer++;
                                            doRestore = true;
                                        }
                                        break;
                                    default:
                                        break;
                                    }
                                    if (doRestore) {
                                        final int pointer2 = pointer;
                                        final boolean openTextEditor = historySelectionStart.get(pointer2) != -1;

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
                                        final String decompressed = StringHelper.decompress(text);

                                        Display.getDefault().asyncExec(new Runnable() {
                                            @Override
                                            public void run() {
                                                GDataCSG.resetCSG();
                                                GDataCSG.forceRecompile();
                                                Project.getUnsavedFiles().add(df);
                                                df.setText(decompressed);
                                                df.parseForData(false);
                                                if (openTextEditor) {

                                                }
                                                final VertexManager vm = df.getVertexManager();

                                                vm.clearSelection2();
                                                for (Vertex vertex : historySelectedVertices.get(pointer2)) {
                                                    vm.getSelectedVertices().add(vertex);
                                                }
                                                boolean[] selection = historySelectedData.get(pointer2);
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
                                                vm.updateUnsavedStatus();

                                                if (openTextEditor) {

                                                }
                                                df.getVertexManager().setModified(true, false);
                                            }
                                        });
                                    }
                                    action.set(0);
                                }
                            }
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                        } catch (Exception e) {
                            NLogger.debug(getClass(), e);
                        }
                    }
                    // TODO Cleanup the data here
                }
            }).start();
        }

        while (!workQueue.offer(new Object[]{text, selectionStart, selectionEnd, data, selectedData, selectedVertices})) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}
        }

    }

    public void deleteHistory() {
        isRunning.set(false);
    }

    public void undo() {
        BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
            @Override
            public void run() {
                action.set(1);
                while (action.get() > 0) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {}
                }
            }
        });
    }

    public void redo() {
        BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
            @Override
            public void run() {
                action.set(2);
                while (action.get() > 0) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {}
                }
            }
        });
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
}
