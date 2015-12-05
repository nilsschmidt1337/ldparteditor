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
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.nschmidt.ldparteditor.enums.MergeTo;
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.helpers.composite3d.SelectorSettings;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeTreeMap;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;

class VM22TJunctionFixer extends VM21Merger {

    protected VM22TJunctionFixer(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public void fixTjunctions(final boolean calculateDistance) {

        linkedDatFile.setDrawSelection(false);

        final Set<Vertex> verticesToProcess = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());
        final Set<Vertex> verticesToSelect = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());

        if (selectedVertices.isEmpty()) {
            verticesToProcess.addAll(vertexLinkedToPositionInFile.keySet());
        } else {
            verticesToProcess.addAll(selectedVertices);
        }

        clearSelection();

        final int[] TjunctionCount = new int[1];
        try
        {
            new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
            {
                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                {
                    try
                    {
                        monitor.beginTask(I18n.E3D_Tjunction, verticesToProcess.size());

                        final AtomicBoolean a = new AtomicBoolean();

                        for (final Vertex v : verticesToProcess) {
                            if (monitor.isCanceled()) break;
                            if (!vertexLinkedToPositionInFile.containsKey(v)) continue;
                            Display.getDefault().asyncExec(new Runnable() {
                                @Override
                                public void run() {
                                    clearSelection2();
                                    if (isTjunctionCandidate(v, calculateDistance)) {
                                        clearSelection2();
                                        selectedVertices.add(v);
                                        verticesToSelect.add(v);
                                        merge(MergeTo.NEAREST_EDGE, false);
                                        TjunctionCount[0]++;
                                    }
                                    monitor.worked(1);
                                    a.set(true);
                                }});
                            while (!a.get()) {
                                Thread.sleep(10);
                            }
                            a.set(false);
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
        } catch (InterruptedException consumed) {
        }

        clearSelection2();

        MessageBox messageBox = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_INFORMATION | SWT.OK);
        messageBox.setText(I18n.DIALOG_Info);
        Object[] messageArguments = {TjunctionCount[0]};
        MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
        formatter.setLocale(MyLanguage.LOCALE);
        formatter.applyPattern(I18n.E3D_TjunctionCount);
        messageBox.setMessage(formatter.format(messageArguments));
        messageBox.open();

        syncWithTextEditors(true);
        setModified(true, true);

        selectedVertices.addAll(verticesToSelect);
        linkedDatFile.setDrawSelection(true);

    }

    private boolean isTjunctionCandidate(Vertex v, final boolean calculateDistance) {

        HashSet<GData> surfs = getLinkedSurfaces(v);

        int surfCount = surfs.size();
        if (surfCount == 0) {
            return false;
        }

        TreeSet<Vertex> verts = new TreeSet<Vertex>();
        TreeSet<Vertex> verts2 = new TreeSet<Vertex>();

        for (GData g : surfs) {
            switch (g.type()) {
            case 3:
                GData3 g3 = (GData3) g;
                Vertex v1 = new Vertex(g3.X1, g3.Y1, g3.Z1);
                Vertex v2 = new Vertex(g3.X2, g3.Y2, g3.Z2);
                Vertex v3 = new Vertex(g3.X3, g3.Y3, g3.Z3);
                if (verts2.contains(v1)) verts.add(v1);
                if (verts2.contains(v2)) verts.add(v2);
                if (verts2.contains(v3)) verts.add(v3);
                verts2.add(v1);
                verts2.add(v2);
                verts2.add(v3);
                break;
            case 4:
                GData4 g4 = (GData4) g;
                Vertex v4 = new Vertex(g4.X1, g4.Y1, g4.Z1);
                Vertex v5 = new Vertex(g4.X2, g4.Y2, g4.Z2);
                Vertex v6 = new Vertex(g4.X3, g4.Y3, g4.Z3);
                Vertex v7 = new Vertex(g4.X4, g4.Y4, g4.Z4);
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

            double result = (double) getMinimalDistanceVerticesToLines(v, false)[3];
            System.out.println(result);
            return result < 1.0;
        } else {
            return false;
        }
    }
}
