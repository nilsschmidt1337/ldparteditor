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
import org.nschmidt.ldparteditor.data.tools.IdenticalVertexRemover;
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.helpers.composite3d.SelectorSettings;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeTreeMap;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;

class VM24MeshReducer extends VM23FlatSubfileTester {

    protected VM24MeshReducer(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public void meshReduce(int count) {

        // FIXME Needs better performance. I have to implement time measurements first.

        final int[] reduceCount = new int[1];
        final boolean[] newIteration = new boolean[1];
        final int faceCount = triangles.size() + quads.size();

        linkedDatFile.setDrawSelection(false);

        {
            final Set<Vertex> verticesToProcess = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());

            verticesToProcess.addAll(vertexLinkedToPositionInFile.keySet());

            clearSelection();
            selectAll(new SelectorSettings(), true);
            splitQuads(false);
            clearSelection();


            final VertexManager vm = (VertexManager) this;
            final DatFile df = linkedDatFile;
            newIteration[0] = true;
            try
            {
                new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
                {
                    @Override
                    public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                    {
                        try
                        {
                            monitor.beginTask(I18n.E3D_MeshReduce, verticesToProcess.size());

                            final AtomicBoolean a = new AtomicBoolean();

                            for (final Vertex v : verticesToProcess) {
                                if (monitor.isCanceled()) break;
                                if (!vertexLinkedToPositionInFile.containsKey(v)) continue;
                                Display.getDefault().asyncExec(new Runnable() {
                                    @Override
                                    public void run() {

                                        while (true) {

                                            // 1. Ermittle alle angrenzenden Flächen
                                            final HashSet<GData> surfs = getLinkedSurfaces(v);

                                            // 2. Ermittle alle angrenzenden Punkte
                                            final TreeSet<Vertex> verts = new TreeSet<Vertex>();

                                            {
                                                int delta = 1;

                                                for (final GData gData : surfs) {
                                                    if (gData.type() == 3) {
                                                        for (Vertex tv : triangles.get(gData)) {
                                                            verts.add(tv);
                                                        }
                                                    } else {
                                                        for (Vertex tv : quads.get(gData)) {
                                                            verts.add(tv);
                                                        }
                                                        delta += 1;
                                                    }
                                                }

                                                // 3. Ist das Polygon geschlossen? Wenn nein, breche ab.
                                                if (verts.size() - delta != surfs.size()) {
                                                    break;
                                                }
                                            }

                                            // 4. Entferne den Ursprungspunkt aus der Menge
                                            verts.remove(v);

                                            // 7. Prüfe die Kandidaten
                                            for (final Vertex t : verts) {
                                                final HashSet<GData> tsurfs = getLinkedSurfaces(t);
                                                final int oldcount = tsurfs.size();
                                                tsurfs.removeAll(surfs);

                                                // 7.1 t muss zwei Flächen mit v teilen
                                                if (oldcount - tsurfs.size() != 2) {
                                                    continue;
                                                }

                                                // 7.2 t darf nur zwei angrenzende Punkte mit v teilen
                                                {
                                                    final TreeSet<Vertex> verts2 = new TreeSet<Vertex>();
                                                    for (final GData gData : tsurfs) {
                                                        if (gData.type() == 3) {
                                                            for (Vertex tv : triangles.get(gData)) {
                                                                verts2.add(tv);
                                                            }
                                                        } else {
                                                            for (Vertex tv : quads.get(gData)) {
                                                                verts2.add(tv);
                                                            }
                                                        }
                                                    }
                                                    verts2.remove(t);
                                                    int oldcount2 = verts2.size();
                                                    verts2.removeAll(verts);
                                                    if (oldcount2 - verts2.size() != 2) {
                                                        continue;
                                                    }
                                                }

                                                // 7.3 die Normalen dürfen nicht kippen!
                                                {
                                                    boolean cont = false;
                                                    final int surfcount = surfs.size();
                                                    Vertex[][] surfsv = new Vertex[surfcount][4];
                                                    Vector3d[] oldNormals = new Vector3d[surfcount];
                                                    Vector3d[] newNormals = new Vector3d[surfcount];
                                                    int s = 0;
                                                    for (final GData gData : surfs) {
                                                        int i = 0;
                                                        if (gData.type() == 3) {
                                                            for (Vertex tv : triangles.get(gData)) {
                                                                surfsv[s][i] = tv;
                                                                i++;
                                                            }
                                                        } else {
                                                            for (Vertex tv : quads.get(gData)) {
                                                                surfsv[s][i] = tv;
                                                                i++;
                                                            }
                                                            if (surfsv[s][1].equals(v)) {
                                                                surfsv[s][0] = surfsv[s][1];
                                                                surfsv[s][1] = surfsv[s][2];
                                                                surfsv[s][2] = surfsv[s][3];
                                                            } else if (surfsv[s][2].equals(v)) {
                                                                Vertex tmp = surfsv[s][0];
                                                                surfsv[s][0] = surfsv[s][2];
                                                                surfsv[s][1] = surfsv[s][3];
                                                                surfsv[s][2] = tmp;
                                                            } else if (surfsv[s][3].equals(v)) {
                                                                Vertex tmp = surfsv[s][0];
                                                                Vertex tmp2 = surfsv[s][1];
                                                                surfsv[s][0] = surfsv[s][3];
                                                                surfsv[s][1] = tmp;
                                                                surfsv[s][2] = tmp2;
                                                            }
                                                            surfsv[s][3] = null;
                                                        }
                                                        oldNormals[s] = Vector3d.getNormal(new Vector3d(surfsv[s][0]), new Vector3d(surfsv[s][1]), new Vector3d(surfsv[s][2]));
                                                        s++;
                                                    }
                                                    HashSet<Integer> ignoreSet = new HashSet<Integer>();
                                                    for (s = 0; s < surfcount; s++) {
                                                        for (int i = 0; i < 3; i++) {
                                                            if (surfsv[s][i].equals(t)) {
                                                                ignoreSet.add(s);
                                                            }
                                                            if (surfsv[s][i].equals(v)) {
                                                                surfsv[s][i] = t;
                                                            }
                                                        }
                                                        if (!ignoreSet.contains(s)) {
                                                            newNormals[s] = Vector3d.getNormal(new Vector3d(surfsv[s][0]), new Vector3d(surfsv[s][1]), new Vector3d(surfsv[s][2]));
                                                            double angle = Vector3d.angle(oldNormals[s], newNormals[s]);
                                                            if (angle > 3.0) {
                                                                cont = true;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    if (cont) {
                                                        continue;
                                                    }
                                                }
                                                // Als letzten Schritt => Kante zusammenfallen lassen

                                                // merge(MergeTo.LAST_SELECTED, false);
                                                changeVertexDirectFast(v, t, true);
                                                // IdenticalVertexRemover.removeIdenticalVertices(vm, df, false, true);

                                                // addLine(v, t);
                                                reduceCount[0]++;

                                                break;
                                            }

                                            break;
                                        }
                                        monitor.worked(1);
                                        a.set(true);
                                    }});
                                while (!a.get()) {
                                    Thread.sleep(5);
                                }
                                a.set(false);
                            }

                            IdenticalVertexRemover.removeIdenticalVertices(vm, df, false, true);

                        } catch (Exception ex) {
                            NLogger.error(getClass(), ex);
                        } finally {
                            if (monitor.isCanceled()) {
                                newIteration[0] = false;
                            }
                            monitor.done();
                        }
                    }
                });
            }
            catch (InvocationTargetException consumed) {
            } catch (InterruptedException consumed) {
            }

            clearSelection2();

        }

        if (reduceCount[0] > 0 && newIteration[0] && faceCount != (triangles.size() + quads.size())) {
            meshReduce(reduceCount[0] + count);
            return;
        }

        MessageBox messageBox = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_INFORMATION | SWT.OK);
        messageBox.setText(I18n.DIALOG_Info);
        Object[] messageArguments = {count};
        MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
        formatter.setLocale(MyLanguage.LOCALE);
        formatter.applyPattern(I18n.E3D_ReduceCount);
        messageBox.setMessage(formatter.format(messageArguments));
        messageBox.open();

        syncWithTextEditors(true);
        setModified(true, true);

        linkedDatFile.setDrawSelection(true);

    }
}
