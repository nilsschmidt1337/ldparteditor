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
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.nschmidt.ldparteditor.enumtype.MergeTo;
import org.nschmidt.ldparteditor.enumtype.MyLanguage;
import org.nschmidt.ldparteditor.helper.LDPartEditorException;
import org.nschmidt.ldparteditor.helper.composite3d.SelectorSettings;
import org.nschmidt.ldparteditor.helper.composite3d.TJunctionSettings;
import org.nschmidt.ldparteditor.helper.math.ThreadsafeSortedMap;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;

class VM22TJunctionFixer extends VM21Merger {

    protected VM22TJunctionFixer(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public void fixTjunctions(TJunctionSettings tjs) {

        final int mode = tjs.getMode();
        final boolean calculateDistance = (mode < 2);
        final boolean doMerge = (mode != 0);

        linkedDatFile.setDrawSelection(false);

        final Set<Vertex> verticesToProcess = Collections.newSetFromMap(new ThreadsafeSortedMap<>());
        final Set<Vertex> verticesToSelect = Collections.newSetFromMap(new ThreadsafeSortedMap<>());

        if (selectedVertices.isEmpty()) {
            verticesToProcess.addAll(vertexLinkedToPositionInFile.keySet());
        } else {
            verticesToProcess.addAll(selectedVertices);
        }

        clearSelection();

        final int[] tJunctionCount = new int[1];
        try
        {
            new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
            {
                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                {
                    try
                    {
                        monitor.beginTask(I18n.E3D_T_JUNCTION, verticesToProcess.size());

                        final AtomicBoolean a = new AtomicBoolean();

                        for (final Vertex v : verticesToProcess) {
                            if (monitor.isCanceled()) break;
                            if (vertexLinkedToPositionInFile.containsKey(v)) {
                                Display.getDefault().asyncExec(() -> {
                                    clearSelection2();
                                    if (isTjunctionCandidate(v, calculateDistance)) {
                                        clearSelection2();
                                        selectedVertices.add(v);
                                        verticesToSelect.add(v);
                                        if (doMerge) merge(MergeTo.NEAREST_EDGE_SPLIT, false, false);
                                        tJunctionCount[0]++;
                                    }
                                    monitor.worked(1);
                                    a.set(true);
                                });
                                while (!a.get()) {
                                    Thread.sleep(10);
                                }
                                a.set(false);
                            }
                        }
                    } catch (Exception ex) {
                        NLogger.error(getClass(), ex);
                    } finally {
                        monitor.done();
                    }
                }
            });
        }
        catch (InvocationTargetException consumed) {
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new LDPartEditorException(ie);
        }

        clearSelection2();

        MessageBox messageBox = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_INFORMATION | SWT.OK);
        messageBox.setText(I18n.DIALOG_INFO);
        Object[] messageArguments = {tJunctionCount[0]};
        MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
        formatter.setLocale(MyLanguage.getLocale());
        formatter.applyPattern(I18n.E3D_T_JUNCTION_COUNT);
        messageBox.setMessage(formatter.format(messageArguments));
        messageBox.open();

        syncWithTextEditors(true);
        setModified(true, true);

        selectedVertices.addAll(verticesToSelect);
        linkedDatFile.setDrawSelection(true);

    }

    private boolean isTjunctionCandidate(Vertex v, final boolean calculateDistance) {

        Set<GData> surfs = getLinkedSurfaces(v);

        int surfCount = surfs.size();
        if (surfCount == 0) {
            return false;
        }

        SortedSet<Vertex> verts = new TreeSet<>();
        SortedSet<Vertex> verts2 = new TreeSet<>();

        for (GData g : surfs) {
            switch (g.type()) {
            case 3:
                GData3 g3 = (GData3) g;
                Vertex v1 = new Vertex(g3.x1p, g3.y1p, g3.z1p);
                Vertex v2 = new Vertex(g3.x2p, g3.y2p, g3.z2p);
                Vertex v3 = new Vertex(g3.x3p, g3.y3p, g3.z3p);
                if (verts2.contains(v1)) verts.add(v1);
                if (verts2.contains(v2)) verts.add(v2);
                if (verts2.contains(v3)) verts.add(v3);
                verts2.add(v1);
                verts2.add(v2);
                verts2.add(v3);
                break;
            case 4:
                GData4 g4 = (GData4) g;
                Vertex v4 = new Vertex(g4.x1p, g4.y1p, g4.z1p);
                Vertex v5 = new Vertex(g4.x2p, g4.y2p, g4.z2p);
                Vertex v6 = new Vertex(g4.x3p, g4.y3p, g4.z3p);
                Vertex v7 = new Vertex(g4.x4p, g4.y4p, g4.z4p);
                if (verts2.contains(v4)) verts.add(v4);
                if (verts2.contains(v5)) verts.add(v5);
                if (verts2.contains(v6)) verts.add(v6);
                if (verts2.contains(v7)) verts.add(v7);
                verts2.add(v4);
                verts2.add(v5);
                verts2.add(v6);
                verts2.add(v7);
                break;
            default:
            }
        }

        int vertCount = verts.size();

        if (surfCount + 1 != vertCount) {

            if (!calculateDistance) {
                return true;
            }

            for (VertexManifestation mani : vertexLinkedToPositionInFile.get(v)) {
                GData gd = mani.gdata();
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

            double result = (double) getMinimalDistanceVerticesToLines(v, false)[3];
            NLogger.debug(getClass(), result);
            return result < 1.0;
        } else {
            return false;
        }
    }
}
