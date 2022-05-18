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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.nschmidt.ldparteditor.enumtype.Threshold;
import org.nschmidt.ldparteditor.helper.LDPartEditorException;
import org.nschmidt.ldparteditor.helper.math.Vector3d;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;

class VM29LineSurfaceIntersector extends VM28SlantingMatrixProjector {

    protected VM29LineSurfaceIntersector(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public void intersectionVerticesBetweenLinesAndSurfaces3D(boolean showPopup) {

        final Set<GData2> linesToIntersect = new HashSet<>();
        final Set<GData> surfacesToIntersect = new HashSet<>();

        if (selectedLines.isEmpty()) {
            if (showPopup) {
                MessageBox messageBox = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
                messageBox.setText(I18n.E3D_LINE_SURFACE_INTERSECTION);
                messageBox.setMessage(I18n.E3D_INTERSECTION_POINTS_QUESTION);
                int result = messageBox.open();
                if (result != SWT.YES) {
                    return;
                }
            }
            linesToIntersect.addAll(lines.keySet());
        } else {
            linesToIntersect.addAll(selectedLines);
        }

        if (selectedTriangles.isEmpty() && selectedQuads.isEmpty()) {
            surfacesToIntersect.addAll(triangles.keySet());
            surfacesToIntersect.addAll(quads.keySet());
        } else {
            surfacesToIntersect.addAll(selectedTriangles);
            surfacesToIntersect.addAll(selectedQuads);
        }

        clearSelection();

        final List<List<Vertex>> linesToParse = new ArrayList<>();
        final List<List<Vertex>> surfacesToParse = new ArrayList<>();
        SortedSet<Vertex> m1 = new TreeSet<>();
        SortedSet<Vertex> m2 = new TreeSet<>();

        for (GData2 g2 : linesToIntersect) {
            Vertex[] verts = lines.get(g2);
            for (Vertex v : verts) {
                if (m1.contains(v)) {
                    m2.add(v);
                } else {
                    m1.add(v);
                }
            }
            List<Vertex> l = new ArrayList<>();
            l.add(verts[0]);
            l.add(verts[1]);
            linesToParse.add(l);
        }

        for (GData surf : surfacesToIntersect) {
            if (surf instanceof GData3 gd3) addSurf(gd3, surfacesToParse);
            if (surf instanceof GData4 gd4) addSurf(gd4, surfacesToParse);
        }

        SortedSet<Vertex> intersectionPoints = new TreeSet<>();

        // Calculate intersection points


        if (showPopup) {
            try
            {
                new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
                {
                    @Override
                    public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                    {
                        try
                        {
                            monitor.beginTask(I18n.VM_SEARCH_INTERSECTION, linesToParse.size());
                            for (List<Vertex> line : linesToParse) {
                                if (monitor.isCanceled()) break;
                                for (List<Vertex> surf : surfacesToParse) {
                                    if (monitor.isCanceled()) break;
                                    SortedSet<Vertex> allVertices = new TreeSet<>();
                                    for(int l = 0; l < 2; l++) {
                                        allVertices.add(line.get(l));
                                    }
                                    for(int l = 0; l < 3; l++) {
                                        allVertices.add(surf.get(l));
                                    }
                                    if (allVertices.size() == 5) {
                                        Vector3d ip = new Vector3d();
                                        if (intersectLineTriangle(
                                                line.get(0), line.get(1),
                                                surf.get(0), surf.get(1), surf.get(2), ip)) {
                                            intersectionPoints.add(new Vertex(ip));
                                        }
                                    }
                                }
                                monitor.worked(1);
                            }
                        } catch (Exception ex) {
                            NLogger.error(getClass(), ex);
                        } finally {
                            monitor.done();
                        }
                    }
                });
            }
            catch (InvocationTargetException ite) {
                NLogger.error(VM29LineSurfaceIntersector.class, ite);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new LDPartEditorException(ie);
            }
        } else {
            for (List<Vertex> line : linesToParse) {
                for (List<Vertex> surf : surfacesToParse) {
                    SortedSet<Vertex> allVertices = new TreeSet<>();
                    for(int l = 0; l < 2; l++) {
                        allVertices.add(line.get(l));
                    }
                    for(int l = 0; l < 3; l++) {
                        allVertices.add(surf.get(l));
                    }
                    if (allVertices.size() == 5) {
                        Vector3d ip = new Vector3d();
                        if (intersectLineTriangle(
                                line.get(0), line.get(1),
                                surf.get(0), surf.get(1), surf.get(2), ip)) {
                            intersectionPoints.add(new Vertex(ip));
                        }
                    }
                }
            }
        }

        for (Vertex intersection : intersectionPoints) {
            linkedDatFile.addToTailOrInsertAfterCursor(addVertex(intersection));
        }

        if (!intersectionPoints.isEmpty()) {
            setModifiedNoSync();
            syncWithTextEditors(true);
            updateUnsavedStatus();
        }
    }

    private void addSurf(GData3 gd3, List<List<Vertex>> surfacesToParse) {
        Vertex[] verts = triangles.get(gd3);
        List<Vertex> tri = new ArrayList<>();
        tri.add(verts[0]);
        tri.add(verts[1]);
        tri.add(verts[2]);
        surfacesToParse.add(tri);
    }

    private void addSurf(GData4 gd4, List<List<Vertex>> surfacesToParse) {
        Vertex[] verts = quads.get(gd4);
        List<Vertex> tri1 = new ArrayList<>();
        tri1.add(verts[0]);
        tri1.add(verts[1]);
        tri1.add(verts[2]);
        surfacesToParse.add(tri1);

        List<Vertex> tri2 = new ArrayList<>();
        tri2.add(verts[2]);
        tri2.add(verts[3]);
        tri2.add(verts[0]);
        surfacesToParse.add(tri2);
    }

    private boolean intersectLineTriangle(Vertex p, Vertex q, Vertex a, Vertex b, Vertex c, Vector3d r) {
        final BigDecimal tolerance = new BigDecimal("0.00001"); //$NON-NLS-1$ .00001
        final BigDecimal zerot = new BigDecimal("-0.00001"); //$NON-NLS-1$
        final BigDecimal onet = new BigDecimal("1.00001"); //$NON-NLS-1$
        BigDecimal diskr;
        BigDecimal invDiskr;
        Vector3d vert0 = new Vector3d(a);
        Vector3d vert1 = new Vector3d(b);
        Vector3d vert2 = new Vector3d(c);
        Vector3d corner1 = Vector3d.sub(vert1, vert0);
        Vector3d corner2 = Vector3d.sub(vert2, vert0);
        Vector3d orig = new Vector3d(p);
        Vector3d dir = Vector3d.sub(new Vector3d(q), orig);
        BigDecimal len = dir.normalise(dir);
        Vector3d pvec = Vector3d.cross(dir, corner2);
        diskr = Vector3d.dotP(corner1, pvec);
        if (diskr.abs().compareTo(tolerance) < 0)
            return false;
        invDiskr = BigDecimal.ONE.divide(diskr, Threshold.MC);
        Vector3d tvec = Vector3d.sub(orig, vert0);
        BigDecimal u = Vector3d.dotP(tvec, pvec).multiply(invDiskr);
        if (u.compareTo(zerot) < 0 || u.compareTo(onet) > 0)
            return false;
        Vector3d qvec = Vector3d.cross(tvec, corner1);
        BigDecimal v = Vector3d.dotP(dir, qvec).multiply(invDiskr);
        if (v.compareTo(zerot) < 0 || u.add(v).compareTo(onet) > 0)
            return false;
        BigDecimal t = Vector3d.dotP(corner2, qvec).multiply(invDiskr);
        if (t.compareTo(zerot) < 0 || t.compareTo(len.add(tolerance)) > 0)
            return false;
        r.setX(orig.x.add(dir.x.multiply(t)));
        r.setY(orig.y.add(dir.y.multiply(t)));
        r.setZ(orig.z.add(dir.z.multiply(t)));
        return true;
    }
}
