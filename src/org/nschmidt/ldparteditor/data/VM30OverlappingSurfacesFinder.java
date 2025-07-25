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
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.nschmidt.ldparteditor.enumtype.MyLanguage;
import org.nschmidt.ldparteditor.helper.LDPartEditorException;
import org.nschmidt.ldparteditor.helper.composite3d.OverlapSettings;
import org.nschmidt.ldparteditor.helper.math.rtree.RTree;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;

class VM30OverlappingSurfacesFinder extends VM29LineSurfaceIntersector {

    protected VM30OverlappingSurfacesFinder(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public void findOverlaps(OverlapSettings os) {
        // Step 1: Create the RTree and add all (selected) surfaces to it
        final RTree tree = new RTree();

        final Set<GData3> trianglesForTree;
        final Set<GData4> quadsForTree;

        final Set<GData3> trianglesToCheck;
        final Set<GData4> quadsToCheck;

        final Set<GData3> trianglesToFilter = new HashSet<>();
        final Set<GData4> quadsToFilter = new HashSet<>();

        final boolean filterOnSelection = os.getScope() == 1;

        if (filterOnSelection) {
            // Check against surfaces from the whole file and filter on the selection!
            trianglesToFilter.addAll(selectedTriangles);
            quadsToFilter.addAll(selectedQuads);

            // Add surfaces from the whole file
            trianglesForTree = triangles.keySet();
            quadsForTree = quads.keySet();
            trianglesToCheck = triangles.keySet();
            quadsToCheck = quads.keySet();
        } else if (os.getScope() == 2) {
            // Only add selected surfaces
            trianglesForTree = new HashSet<>();
            quadsForTree = new HashSet<>();
            trianglesForTree.addAll(selectedTriangles);
            quadsForTree.addAll(selectedQuads);
            trianglesToCheck = trianglesForTree;
            quadsToCheck = quadsForTree;
        } else {
            // Add surfaces from the whole file
            trianglesForTree = triangles.keySet();
            quadsForTree = quads.keySet();
            trianglesToCheck = triangles.keySet();
            quadsToCheck = quads.keySet();
        }

        for(GData3 triangle : trianglesForTree) {
            tree.add(triangle, triangles, quads);
        }

        for(GData4 quad : quadsForTree) {
            tree.add(quad, triangles, quads);
        }

        // Step 2: Clear the selection
        clearSelection2();

        // Step 3: Find overlaps
        try
        {
            new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
            {
                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                {
                    try
                    {
                        monitor.beginTask(I18n.E3D_OVERLAPPING_SURFACES_FINDER, trianglesToCheck.size() + quadsToCheck.size());

                        trianglesToCheck.parallelStream().forEach(triangle -> {
                            synchronized (monitor) {
                                if (monitor.isCanceled()) return;
                            }

                            processOverlaps(tree.searchForIntersections(triangle, triangles, quads), triangle, filterOnSelection, trianglesToFilter, quadsToFilter);

                            synchronized (monitor) {
                                monitor.worked(1);
                            }
                        });

                        quadsToCheck.parallelStream().forEach(quad -> {
                            synchronized (monitor) {
                                if (monitor.isCanceled()) return;
                            }

                            processOverlaps(tree.searchForIntersections(quad, triangles, quads), quad, filterOnSelection, trianglesToFilter, quadsToFilter);

                            synchronized (monitor) {
                                monitor.worked(1);
                            }
                        });
                    } catch (Exception ex) {
                        NLogger.error(getClass(), ex);
                    } finally {
                        monitor.done();
                    }
                }
            });
        } catch (InvocationTargetException ite) {
            NLogger.error(VM30OverlappingSurfacesFinder.class, ite);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new LDPartEditorException(ie);
        }

        MessageBox messageBox = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_INFORMATION | SWT.OK);
        messageBox.setText(I18n.DIALOG_INFO);
        Object[] messageArguments = {selectedData.size()};
        MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
        formatter.setLocale(MyLanguage.getLocale());
        formatter.applyPattern(I18n.OVERLAP_VERBOSE_MSG);
        messageBox.setMessage(formatter.format(messageArguments));
        messageBox.open();
    }

    private void processOverlaps(Set<GData> overlaps, GData geometry, boolean filterOnSelection, Set<GData3> trianglesToFilter, Set<GData4> quadsToFilter) {
        if (!overlaps.isEmpty()) {

            boolean overlapWithoutAdjacency = false;
            for (GData overlap : overlaps) {
                if (!hasSameEdge(overlap, geometry)) {
                    if (!filterOnSelection || (
                            trianglesToFilter.contains(overlap) || trianglesToFilter.contains(geometry)
                         || quadsToFilter.contains(overlap) || quadsToFilter.contains(geometry))) {
                        addToSelection(overlap);
                        overlapWithoutAdjacency = true;
                    }
                }
            }

            if (overlapWithoutAdjacency) {
                addToSelection(geometry);
            }
        }
    }

    private synchronized void addToSelection(GData geometry) {
        selectedData.add(geometry);
        if (geometry instanceof GData3 triangle) {
            selectedTriangles.add(triangle);
        } else if (geometry instanceof GData4 quad) {
            selectedQuads.add(quad);
        }
    }
}
