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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.nschmidt.ldparteditor.enumtype.Threshold;
import org.nschmidt.ldparteditor.helper.LDPartEditorException;
import org.nschmidt.ldparteditor.helper.composite3d.UnificatorSettings;
import org.nschmidt.ldparteditor.helper.math.Vector3d;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;

class VM17Unificator extends VM16Subdivide {

    private static final int VERTICES = 0;
    private static final int SUBPART_VERTICES = 1;
    private static final int VERTICES_AND_SUBPART_VERTICES = 2;

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
                selectedVertices.addAll(Arrays.asList(verts));
            }
        }

        final SortedSet<Vertex> selectedVerts = new TreeSet<>();
        selectedVerts.addAll(selectedVertices);

        clearSelection();

        try
        {
            new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, false, new IRunnableWithProgress()
            {
                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                {
                    monitor.beginTask(I18n.VM_UNIFICATOR, IProgressMonitor.UNKNOWN);

                    monitor.subTask(I18n.VM_SORT_OUT);

                    SortedSet<Vertex> subfileVertices = new TreeSet<>();
                    SortedSet<Vertex> fileVertices = new TreeSet<>();

                    for (Vertex v : selectedVerts) {

                        boolean isFileVertex = false;

                        if (vertexLinkedToSubfile.containsKey(v)) {
                            // Do not add points for condlines in subparts.
                            Set<VertexManifestation> mani = vertexLinkedToPositionInFile.get(v);
                            int controlPointCondlineInSubfile = 0;
                            for (VertexManifestation vm : mani) {
                                GData gd = vm.gdata();
                                if (lineLinkedToVertices.containsKey(gd)) {
                                    // Better performance, since we can detect file vertices here!
                                    fileVertices.add(v);
                                    isFileVertex = true;
                                    break;
                                } else if (gd.type() == 5 && vm.position() > 1) {
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
                                GData gd = vm.gdata();
                                if (lineLinkedToVertices.containsKey(gd)) {
                                    fileVertices.add(v);
                                    break;
                                }
                            }
                        }
                    }


                    if (us.getSnapOn() == VERTICES || us.getSnapOn() == VERTICES_AND_SUBPART_VERTICES) {
                        monitor.subTask(I18n.VM_UNIFY);
                        int i = 0;
                        int j = 0;

                        SortedMap<Vertex, Vertex> mergeTargets = new TreeMap<>();
                        {
                            SortedMap<Vertex, SortedSet<Vertex>> unifyGroups = new TreeMap<>();
                            SortedSet<Vertex> inGroup = new TreeSet<>();

                            for (Vertex v1 : fileVertices) {
                                if (us.getSnapOn() == VERTICES_AND_SUBPART_VERTICES && subfileVertices.contains(v1)) {
                                    i++;
                                    continue;
                                }
                                SortedSet<Vertex> group = new TreeSet<>();
                                group.add(v1);
                                j = 0;
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
                                SortedSet<Vertex> group = unifyGroups.get(key);
                                if (group.size() > 1) {
                                    BigDecimal x = BigDecimal.ZERO;
                                    BigDecimal y = BigDecimal.ZERO;
                                    BigDecimal z = BigDecimal.ZERO;
                                    BigDecimal gc = new BigDecimal(group.size());
                                    for (Vertex gv : group) {
                                        x = x.add(gv.xp);
                                        y = y.add(gv.yp);
                                        z = z.add(gv.zp);
                                    }
                                    x = x.divide(gc, Threshold.MC);
                                    y = y.divide(gc, Threshold.MC);
                                    z = z.divide(gc, Threshold.MC);
                                    Vertex newVertex = new Vertex(x, y, z);
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
                            if (us.getSnapOn() > 0 && subfileVertices.contains(target) && subfileVertices.contains(key)) {
                                continue;
                            }
                            changeVertexDirectFast(key, target, true);
                            selectedVertices.add(target);
                        }
                    }

                    if (us.getSnapOn() == SUBPART_VERTICES || us.getSnapOn() == VERTICES_AND_SUBPART_VERTICES) {
                        monitor.subTask(I18n.VM_SNAP);

                        SortedMap<Vertex, Vertex> mergeTargets = new TreeMap<>();
                        {
                            SortedMap<Vertex, SortedSet<Vertex>> unifyGroups = new TreeMap<>();
                            SortedSet<Vertex> inGroup = new TreeSet<>();

                            for (Vertex v1 : subfileVertices) {
                                SortedSet<Vertex> group = new TreeSet<>();
                                for (Vertex v2 : fileVertices) {
                                    if (!inGroup.contains(v2)) {
                                        Vector3d v3d1 = new Vector3d(v1);
                                        Vector3d v3d2 = new Vector3d(v2);
                                        if (Vector3d.distSquare(v3d1, v3d2).compareTo(st) < 0) {
                                            group.add(v2);
                                            inGroup.add(v2);
                                        }
                                    }
                                }
                                unifyGroups.put(v1, group);
                            }

                            fileVertices.clear();

                            Set<Vertex> keySet = unifyGroups.keySet();
                            for (Vertex key : keySet) {
                                SortedSet<Vertex> group = unifyGroups.get(key);
                                if (!group.isEmpty()) {
                                    for (Vertex gv : group) {
                                        mergeTargets.put(gv, key);
                                    }
                                }
                            }
                        }

                        Set<Vertex> keySet = mergeTargets.keySet();
                        for (Vertex key : keySet) {
                            Vertex target = mergeTargets.get(key);
                            if (subfileVertices.contains(target) && subfileVertices.contains(key)) {
                                continue;
                            }
                            changeVertexDirectFast(key, target, true);
                            selectedVertices.add(target);
                        }
                    }
                }
            });
        } catch (InvocationTargetException ite) {
            NLogger.error(VM17Unificator.class, ite);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new LDPartEditorException(ie);
        }

        // Round selection to 6 decimal places
        selectedVerts.clear();
        selectedVerts.addAll(selectedVertices);

        clearSelection();

        NLogger.debug(getClass(), "Check for identical vertices and collinearity."); //$NON-NLS-1$

        final Set<GData2> linesToDelete2 = new HashSet<>();
        final Set<GData3> trisToDelete2 = new HashSet<>();
        final Set<GData4> quadsToDelete2 = new HashSet<>();
        final Set<GData5> clinesToDelete2 = new HashSet<>();
        {
            for (Entry<GData2, Vertex[]> entry : lines.entrySet()) {
                GData2 g2 = entry.getKey();
                if (!lineLinkedToVertices.containsKey(g2)) continue;
                Vertex[] verts = entry.getValue();
                SortedSet<Vertex> verts2 = new TreeSet<>();
                verts2.addAll(Arrays.asList(verts));
                if (verts2.size() < 2) {
                    linesToDelete2.add(g2);
                }
            }
            for (Entry<GData3, Vertex[]> entry : triangles.entrySet()) {
                GData3 g3 = entry.getKey();
                if (!lineLinkedToVertices.containsKey(g3)) continue;
                Vertex[] verts = entry.getValue();
                SortedSet<Vertex> verts2 = new TreeSet<>();
                verts2.addAll(Arrays.asList(verts));
                if (verts2.size() < 3 || g3.isCollinear()) {
                    trisToDelete2.add(g3);
                }
            }
            for (Entry<GData4, Vertex[]> entry : quads.entrySet()) {
                GData4 g4 = entry.getKey();
                if (!lineLinkedToVertices.containsKey(g4)) continue;
                Vertex[] verts = entry.getValue();
                SortedSet<Vertex> verts2 = new TreeSet<>();
                verts2.addAll(Arrays.asList(verts));
                if (verts2.size() < 3 || g4.isCollinear()) {
                    quadsToDelete2.add(g4);
                }
            }
            for (Entry<GData5, Vertex[]> entry : condlines.entrySet()) {
                GData5 g5 = entry.getKey();
                if (!lineLinkedToVertices.containsKey(g5)) continue;
                Vertex[] verts = entry.getValue();
                SortedSet<Vertex> verts2 = new TreeSet<>();
                verts2.addAll(Arrays.asList(verts));
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
        roundSelection(6, 10, true, true, true, true, true);

        validateState();
    }
}
