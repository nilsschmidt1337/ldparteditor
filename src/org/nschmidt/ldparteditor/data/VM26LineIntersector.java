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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.nschmidt.ldparteditor.helper.LDPartEditorException;
import org.nschmidt.ldparteditor.helper.math.Vector3d;
import org.nschmidt.ldparteditor.helper.math.Vector3dd;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;

class VM26LineIntersector extends VM25Smooth {

    protected VM26LineIntersector(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public void intersectionVerticesBetweenLines3D(boolean showPopup) {

        final Set<GData2> linesToIntersect = new HashSet<>();

        if (selectedLines.isEmpty()) {
            if (showPopup) {
                MessageBox messageBox = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
                messageBox.setText(I18n.E3D_LINE_INTERSECTION);
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

        clearSelection();

        final List<List<Vector3dd>> linesToParse = new ArrayList<>();
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
            List<Vector3dd> l = new ArrayList<>();
            l.add(new Vector3dd(verts[0]));
            l.add(new Vector3dd(verts[1]));
            linesToParse.add(l);
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
                            int i = 0;
                            int j = 0;
                            for (Iterator<List<Vector3dd>> iterator = linesToParse.iterator(); iterator.hasNext();) {
                                if (monitor.isCanceled()) break;
                                List<Vector3dd> line = iterator.next();
                                i++;
                                j = 0;
                                for (List<Vector3dd> line2 : linesToParse) {
                                    j++;
                                    if (j > i) {
                                        if (monitor.isCanceled()) break;
                                        SortedSet<Vector3dd> allVertices = new TreeSet<>();
                                        for(int l = 0; l < 2; l++) {
                                            allVertices.add(line.get(l));
                                            allVertices.add(line2.get(l));
                                        }
                                        if (allVertices.size() == 4) {
                                            Vector3d ip = intersectLineLineSegmentUnidirectional2(line.get(0), line.get(1), line2.get(0), line2.get(1));
                                            if (ip != null) {
                                                intersectionPoints.add(new Vertex(ip));
                                            }
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
                NLogger.error(VM26LineIntersector.class, ite);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new LDPartEditorException(ie);
            }
        } else {
            int i = 0;
            int j = 0;
            for (Iterator<List<Vector3dd>> iterator = linesToParse.iterator(); iterator.hasNext();) {
                List<Vector3dd> line = iterator.next();
                i++;
                j = 0;
                for (List<Vector3dd> line2 : linesToParse) {
                    j++;
                    if (j > i) {
                        SortedSet<Vector3dd> allVertices = new TreeSet<>();
                        for(int l = 0; l < 2; l++) {
                            allVertices.add(line.get(l));
                            allVertices.add(line2.get(l));
                        }
                        if (allVertices.size() == 4) {
                            Vector3d ip = intersectLineLineSegmentUnidirectional2(line.get(0), line.get(1), line2.get(0), line2.get(1));
                            if (ip != null) {
                                intersectionPoints.add(new Vertex(ip));
                            }
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
}
