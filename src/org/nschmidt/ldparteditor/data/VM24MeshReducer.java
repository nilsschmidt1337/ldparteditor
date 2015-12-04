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
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeTreeMap;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;

class VM24MeshReducer extends VM23FlatSubfileTester {

    protected VM24MeshReducer(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public void meshReduce() {

        final BigDecimal TOLERANCER = new BigDecimal("0.00001"); //$NON-NLS-1$ .00001

        linkedDatFile.setDrawSelection(false);

        final Set<Vertex> verticesToProcess = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());
        final Set<Vertex> verticesToSelect = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());

        if (selectedVertices.isEmpty()) {
            verticesToProcess.addAll(vertexLinkedToPositionInFile.keySet());
        } else {
            verticesToProcess.addAll(selectedVertices);
        }

        clearSelection();

        final int[] reduceCount = new int[1];
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

                                        if (!onAPlane(verts)) {
                                            break;
                                        }

                                        // 4. Entferne den Ursprungspunkt aus der Menge
                                        verts.remove(v);

                                        // 5. Finde zwei Kandidaten
                                        final Vertex[] targets = new Vertex[2];
                                        boolean foundCandidates = false;

                                        for (final Vertex v1 : verts) {
                                            if (foundCandidates) {
                                                break;
                                            }
                                            for (final Vertex v2 : verts) {
                                                if (foundCandidates) {
                                                    break;
                                                }
                                                if (v1 != v2) {
                                                    final Vector3d vd0 = new Vector3d(v);
                                                    final Vector3d vd1 = new Vector3d(v1);
                                                    final Vector3d vd2 = new Vector3d(v2);
                                                    Vector3d.sub(vd1, vd0, vd1);
                                                    Vector3d.sub(vd2, vd0, vd2);
                                                    vd1.normalise(vd1);
                                                    vd2.normalise(vd2);
                                                    final BigDecimal dot = Vector3d.dotP(vd1, vd2).abs();
                                                    if (BigDecimal.ONE.subtract(dot).compareTo(TOLERANCER) < 0) {
                                                        targets[0] = v1;
                                                        targets[1] = v1;
                                                        foundCandidates = true;
                                                        break;
                                                    }
                                                }
                                            }
                                        }

                                        // 6. Vertex eignet sich nicht?
                                        if (!foundCandidates) {
                                            break;
                                        }

                                        // 7. Prüfe die Kandidaten
                                        for (final Vertex t : targets) {
                                            final HashSet<GData> tsurfs = getLinkedSurfaces(t);
                                            final int oldcount = tsurfs.size();
                                            tsurfs.removeAll(surfs);

                                            // 7.1 t muss zwei Flächen mit v teilen
                                            if (oldcount - tsurfs.size() != 2) {
                                                continue;
                                            }

                                            // 7.2

                                            // Als letzten Schritt => Kante zusammenfallen lassen

                                            clearSelection2();

                                            selectedVertices.add(v);
                                            selectedVertices.add(t);

                                            lastSelectedVertex = t;

                                            merge(MergeTo.LAST_SELECTED, false);

                                            addLine(v, t);
                                            reduceCount[0]++;

                                            break;
                                        }

                                        break;
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
        Object[] messageArguments = {reduceCount[0]};
        MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
        formatter.setLocale(MyLanguage.LOCALE);
        formatter.applyPattern(I18n.E3D_ReduceCount);
        messageBox.setMessage(formatter.format(messageArguments));
        messageBox.open();

        syncWithTextEditors(true);
        setModified(true, true);

        selectedVertices.addAll(verticesToSelect);
        linkedDatFile.setDrawSelection(true);

    }

    private boolean onAPlane(TreeSet<Vertex> m2) {
        BigDecimal seed = new BigDecimal("1.23456789"); //$NON-NLS-1$
        BigDecimal seed2 = new BigDecimal("-1.832647382"); //$NON-NLS-1$
        BigDecimal seed3 = new BigDecimal("1.427637292"); //$NON-NLS-1$
        Vertex s = new Vertex(seed, seed2, seed3);
        Vertex p1 = null;
        Vertex p2 = null;
        Vertex p3 = null;
        for (Vertex vertex : m2) {
            p1 = vertex;
            break;
        }
        if (p1 == null) return false;
        for (Vertex vertex : m2) {
            if (!vertex.equals(p1)) {
                p2 = vertex;
                break;
            }
        }
        if (p2 == null) return false;
        for (Vertex vertex : m2) {
            if (!vertex.equals(p1) && !vertex.equals(p2)) {
                p3 = vertex;
                break;
            }
        }
        if (p3 == null) return false;
        Vector3d a = new Vector3d(p1.X.add(s.X), p1.Y.add(s.Y),p1.Z.add(s.Z));
        Vector3d b = new Vector3d(p2.X.add(s.X), p2.Y.add(s.Y),p2.Z.add(s.Z));
        Vector3d c = new Vector3d(p3.X.add(s.X), p3.Y.add(s.Y),p3.Z.add(s.Z));

        Vector3d pOrigin = new Vector3d(p1);
        Vector3d n = Vector3d.cross(Vector3d.sub(a, c), Vector3d.sub(b, c));
        n.normalise(n);
        BigDecimal EPSILON = new BigDecimal("0.01"); //$NON-NLS-1$
        for (Vertex vertex : m2) {
            Vector3d vp = new Vector3d(vertex);
            final BigDecimal d = Vector3d.dotP(Vector3d.sub(pOrigin, vp), n).abs();
            if (d.compareTo(EPSILON) > 0) {
                NLogger.debug(getClass(), d.toString());
                return false;
            }
        }

        return true;
    }
}
