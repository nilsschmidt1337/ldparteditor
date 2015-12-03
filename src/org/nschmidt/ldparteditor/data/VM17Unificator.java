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
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.helpers.composite3d.UnificatorSettings;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;

class VM17Unificator extends VM16Subdivide {

    protected VM17Unificator(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public void unificator(final UnificatorSettings us) {

        if (linkedDatFile.isReadOnly()) return;

        final BigDecimal vt = us.getVertexThreshold().multiply(us.getVertexThreshold());
        final BigDecimal st = us.getSubvertexThreshold().multiply(us.getSubvertexThreshold());

        if (us.getScope() == 0) {
            selectAll(null, true);
        } else {
            for (GData gd : selectedData) {
                Vertex[] verts = null;
                switch (gd.type()) {
                case 2:
                    verts = lines.get(gd);
                    break;
                case 3:
                    verts = triangles.get(gd);
                    break;
                case 4:
                    verts = quads.get(gd);
                    break;
                case 5:
                    verts = condlines.get(gd);
                    break;
                default:
                    continue;
                }
                for (Vertex v : verts) {
                    selectedVertices.add(v);
                }
            }
        }

        final TreeSet<Vertex> selectedVerts = new TreeSet<Vertex>();
        selectedVerts.addAll(selectedVertices);

        clearSelection();

        try
        {
            new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, false, new IRunnableWithProgress()
            {
                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                {
                    monitor.beginTask(I18n.VM_Unificator, IProgressMonitor.UNKNOWN);

                    monitor.subTask(I18n.VM_SortOut);

                    TreeSet<Vertex> subfileVertices = new TreeSet<Vertex>();
                    TreeSet<Vertex> fileVertices = new TreeSet<Vertex>();

                    for (Vertex v : selectedVerts) {

                        boolean isFileVertex = false;

                        if (vertexLinkedToSubfile.containsKey(v)) {
                            // Do not add points for condlines in subparts.
                            Set<VertexManifestation> mani = vertexLinkedToPositionInFile.get(v);
                            int controlPointCondlineInSubfile = 0;
                            for (VertexManifestation vm : mani) {
                                GData gd = vm.getGdata();
                                if (lineLinkedToVertices.containsKey(gd)) {
                                    // Better performance, since we can detect file vertices here!
                                    fileVertices.add(v);
                                    isFileVertex = true;
                                    break;
                                } else if (gd.type() == 5 && vm.getPosition() > 1) {
                                    controlPointCondlineInSubfile++;
                                }
                            }
                            if (controlPointCondlineInSubfile == mani.size()) {
                                continue;
                            }
                            subfileVertices.add(v);
                        }

                        if (!isFileVertex) {
                            Set<VertexManifestation> mani = vertexLinkedToPositionInFile.get(v);
                            for (VertexManifestation vm : mani) {
                                GData gd = vm.getGdata();
                                if (lineLinkedToVertices.containsKey(gd)) {
                                    fileVertices.add(v);
                                    break;
                                }
                            }
                        }
                    }


                    if (us.getSnapOn() == 0 || us.getSnapOn() == 2) {
                        monitor.subTask(I18n.VM_Unify);
                        int i = 0;
                        int j = 0;

                        TreeMap<Vertex, Vertex> mergeTargets = new TreeMap<Vertex, Vertex>();
                        {
                            TreeMap<Vertex, TreeSet<Vertex>> unifyGroups = new TreeMap<Vertex, TreeSet<Vertex>>();
                            TreeSet<Vertex> inGroup = new TreeSet<Vertex>();

                            for (Vertex v1 : fileVertices) {
                                TreeSet<Vertex> group = new TreeSet<Vertex>();
                                group.add(v1);
                                for (Vertex v2 : fileVertices) {
                                    if (j > i && !inGroup.contains(v2)) {
                                        Vector3d v3d1 = new Vector3d(v1);
                                        Vector3d v3d2 = new Vector3d(v2);
                                        if (Vector3d.distSquare(v3d1, v3d2).compareTo(vt) < 0) {
                                            group.add(v2);
                                            inGroup.add(v2);
                                        }
                                    }
                                    j++;
                                }
                                unifyGroups.put(v1, group);
                                i++;
                            }

                            fileVertices.clear();

                            Set<Vertex> keySet = unifyGroups.keySet();
                            for (Vertex key : keySet) {
                                TreeSet<Vertex> group = unifyGroups.get(key);
                                if (group.size() > 1) {
                                    BigDecimal X = BigDecimal.ZERO;
                                    BigDecimal Y = BigDecimal.ZERO;
                                    BigDecimal Z = BigDecimal.ZERO;
                                    BigDecimal gc = new BigDecimal(group.size());
                                    for (Vertex gv : group) {
                                        X = X.add(gv.X);
                                        Y = Y.add(gv.Y);
                                        Z = Z.add(gv.Z);
                                    }
                                    X = X.divide(gc, Threshold.mc);
                                    Y = Y.divide(gc, Threshold.mc);
                                    Z = Z.divide(gc, Threshold.mc);
                                    Vertex newVertex = new Vertex(X, Y, Z);
                                    fileVertices.add(newVertex);
                                    for (Vertex gv : group) {
                                        mergeTargets.put(gv, newVertex);
                                    }
                                } else {
                                    fileVertices.add(key);
                                }
                            }
                        }

                        Set<Vertex> keySet = mergeTargets.keySet();
                        for (Vertex key : keySet) {
                            Vertex target = mergeTargets.get(key);
                            changeVertexDirectFast(key, target, true);
                            selectedVertices.add(target);
                        }
                    }

                    if (us.getSnapOn() == 1 || us.getSnapOn() == 2) {
                        monitor.subTask(I18n.VM_Snap);

                        int i = 0;
                        int j = 0;

                        TreeMap<Vertex, Vertex> mergeTargets = new TreeMap<Vertex, Vertex>();
                        {
                            TreeMap<Vertex, TreeSet<Vertex>> unifyGroups = new TreeMap<Vertex, TreeSet<Vertex>>();
                            TreeSet<Vertex> inGroup = new TreeSet<Vertex>();

                            for (Vertex v1 : subfileVertices) {
                                TreeSet<Vertex> group = new TreeSet<Vertex>();
                                for (Vertex v2 : fileVertices) {
                                    if (j > i && !inGroup.contains(v2)) {
                                        Vector3d v3d1 = new Vector3d(v1);
                                        Vector3d v3d2 = new Vector3d(v2);
                                        if (Vector3d.distSquare(v3d1, v3d2).compareTo(st) < 0) {
                                            group.add(v2);
                                            inGroup.add(v2);
                                        }
                                    }
                                    j++;
                                }
                                unifyGroups.put(v1, group);
                                i++;
                            }

                            fileVertices.clear();

                            Set<Vertex> keySet = unifyGroups.keySet();
                            for (Vertex key : keySet) {
                                TreeSet<Vertex> group = unifyGroups.get(key);
                                if (group.size() > 0) {
                                    for (Vertex gv : group) {
                                        mergeTargets.put(gv, key);
                                    }
                                }
                            }
                        }

                        Set<Vertex> keySet = mergeTargets.keySet();
                        for (Vertex key : keySet) {
                            Vertex target = mergeTargets.get(key);
                            changeVertexDirectFast(key, target, true);
                            selectedVertices.add(target);
                        }
                    }
                }
            });
        } catch (InvocationTargetException consumed) {
        } catch (InterruptedException consumed) {
        }

        // Round selection to 6 decimal places
        selectedVerts.clear();
        selectedVerts.addAll(selectedVertices);

        clearSelection();

        NLogger.debug(getClass(), "Check for identical vertices and collinearity."); //$NON-NLS-1$

        final Set<GData2> linesToDelete2 = new HashSet<GData2>();
        final Set<GData3> trisToDelete2 = new HashSet<GData3>();
        final Set<GData4> quadsToDelete2 = new HashSet<GData4>();
        final Set<GData5> clinesToDelete2 = new HashSet<GData5>();
        {
            for (GData2 g2 : lines.keySet()) {
                if (!lineLinkedToVertices.containsKey(g2)) continue;
                Vertex[] verts = lines.get(g2);
                Set<Vertex> verts2 = new TreeSet<Vertex>();
                for (Vertex vert : verts) {
                    verts2.add(vert);
                }
                if (verts2.size() < 2) {
                    linesToDelete2.add(g2);
                }
            }
            for (GData3 g3 : triangles.keySet()) {
                if (!lineLinkedToVertices.containsKey(g3)) continue;
                Vertex[] verts = triangles.get(g3);
                Set<Vertex> verts2 = new TreeSet<Vertex>();
                for (Vertex vert : verts) {
                    verts2.add(vert);
                }
                if (verts2.size() < 3 || g3.isCollinear()) {
                    trisToDelete2.add(g3);
                }
            }
            for (GData4 g4 : quads.keySet()) {
                if (!lineLinkedToVertices.containsKey(g4)) continue;
                Vertex[] verts = quads.get(g4);
                Set<Vertex> verts2 = new TreeSet<Vertex>();
                for (Vertex vert : verts) {
                    verts2.add(vert);
                }
                if (verts2.size() < 4 || g4.isCollinear()) {
                    quadsToDelete2.add(g4);
                }
            }
            for (GData5 g5 : condlines.keySet()) {
                if (!lineLinkedToVertices.containsKey(g5)) continue;
                Vertex[] verts = condlines.get(g5);
                Set<Vertex> verts2 = new TreeSet<Vertex>();
                for (Vertex vert : verts) {
                    verts2.add(vert);
                }
                if (verts2.size() < 4) {
                    clinesToDelete2.add(g5);
                }
            }
        }

        selectedLines.addAll(linesToDelete2);
        selectedTriangles.addAll(trisToDelete2);
        selectedQuads.addAll(quadsToDelete2);
        selectedCondlines.addAll(clinesToDelete2);
        selectedData.addAll(selectedLines);
        selectedData.addAll(selectedTriangles);
        selectedData.addAll(selectedQuads);
        selectedData.addAll(selectedCondlines);
        delete(false, false);

        // Round selection to 6 decimal places
        selectedVertices.addAll(selectedVerts);

        NLogger.debug(getClass(), "Round."); //$NON-NLS-1$
        roundSelection(6, 10, true, true);

        validateState();
    }
}
