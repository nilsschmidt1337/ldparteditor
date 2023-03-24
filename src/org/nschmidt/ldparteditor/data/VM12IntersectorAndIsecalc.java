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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.nschmidt.ldparteditor.composite.Composite3D;
import org.nschmidt.ldparteditor.data.tool.FlipTriangleOptimizer;
import org.nschmidt.ldparteditor.enumtype.Colour;
import org.nschmidt.ldparteditor.enumtype.LDConfig;
import org.nschmidt.ldparteditor.enumtype.MyLanguage;
import org.nschmidt.ldparteditor.enumtype.Threshold;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.helper.LDPartEditorException;
import org.nschmidt.ldparteditor.helper.composite3d.IntersectorSettings;
import org.nschmidt.ldparteditor.helper.composite3d.IsecalcSettings;
import org.nschmidt.ldparteditor.helper.composite3d.SelectorSettings;
import org.nschmidt.ldparteditor.helper.math.MathHelper;
import org.nschmidt.ldparteditor.helper.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.helper.math.Vector3d;
import org.nschmidt.ldparteditor.helper.math.Vector3dd;
import org.nschmidt.ldparteditor.helper.math.Vector3dh;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;

class VM12IntersectorAndIsecalc extends VM11HideShow {

    private static final BigDecimal TOLERANCE = BigDecimal.ZERO;
    private static final BigDecimal ZEROT = BigDecimal.ZERO;
    private static final BigDecimal ONET = BigDecimal.ONE;

    private static final BigDecimal TOLERANCER = new BigDecimal("0.00001"); //$NON-NLS-1$
    private static final BigDecimal ZEROTR = new BigDecimal("-0.00001"); //$NON-NLS-1$
    private static final BigDecimal ONETR = new BigDecimal("1.00001"); //$NON-NLS-1$

    protected VM12IntersectorAndIsecalc(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public int isecalc(IsecalcSettings is) {

        if (linkedDatFile.isReadOnly()) return 0;

        final List<GData2> newLines = new ArrayList<>();
        final List<GData> surfsToParse;

        if (is.getScope() == 0) {
            surfsToParse = new ArrayList<>(triangles.size() + quads.size());
            surfsToParse.addAll(triangles.keySet());
            surfsToParse.addAll(quads.keySet());
        } else {
            surfsToParse = new ArrayList<>(selectedTriangles.size() + selectedQuads.size());
            surfsToParse.addAll(selectedTriangles);
            surfsToParse.addAll(selectedQuads);
        }

        clearSelection();

        final int surfsSize = surfsToParse.size();

        try
        {
            new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
            {
                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                {
                    try
                    {
                        monitor.beginTask(I18n.VM_SEARCH_INTERSECTION, IProgressMonitor.UNKNOWN);
                        for(int i = 0; i < surfsSize; i++) {
                            /* Check if the monitor has been canceled */
                            if (monitor.isCanceled()) break;
                            NLogger.debug(getClass(), "Checked {0}  of {1} surfaces.", i + 1, surfsSize); //$NON-NLS-1$
                            for(int j = i + 1; j < surfsSize; j++) {
                                GData s1 = surfsToParse.get(i);
                                GData s2 = surfsToParse.get(j);
                                if (isConnected2(s1, s2)) continue;
                                newLines.addAll(intersectionLines(s1, s2));
                            }
                        }
                    }
                    finally
                    {
                        monitor.done();
                    }
                }
            });
        }
        catch (InvocationTargetException ite) {
            NLogger.error(VM12IntersectorAndIsecalc.class, ite);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new LDPartEditorException(ie);
        }

        if (!newLines.isEmpty()) {

            // Remove zero length lines
            BigDecimal epsilon = new BigDecimal(".0001"); //$NON-NLS-1$
            for (Iterator<GData2> li = newLines.iterator(); li.hasNext();) {
                GData2 l = li.next();
                BigDecimal dx = l.x1p.subtract(l.x2p);
                BigDecimal dy = l.y1p.subtract(l.y2p);
                BigDecimal dz = l.z1p.subtract(l.z2p);
                BigDecimal len = dx.multiply(dx).add(dy.multiply(dy)).add(dz.multiply(dz));
                if (len.compareTo(epsilon) <= 0) {
                    remove(l);
                    li.remove();
                }
            }

            final int lineCount = newLines.size();
            final BigDecimal small = new BigDecimal("0.001"); //$NON-NLS-1$
            final BigDecimal smallangle = new BigDecimal("0.00001"); //$NON-NLS-1$
            final Vector3d zero = new Vector3d();

            // Merge lines with same directions
            int[] colin = new int[lineCount];
            int distline = 1;
            int flag = 0;
            for (int i=0; i < lineCount; i++)
            {
                if(colin[i] == 0)
                {
                    for (int j= i + 1; j < lineCount; j++)
                    {
                        Vector3d p11 = new Vector3d(newLines.get(i).x1p, newLines.get(i).y1p, newLines.get(i).z1p);
                        Vector3d p12 = new Vector3d(newLines.get(i).x2p, newLines.get(i).y2p, newLines.get(i).z2p);
                        Vector3d p21 = new Vector3d(newLines.get(j).x1p, newLines.get(j).y1p, newLines.get(j).z1p);
                        Vector3d p22 = new Vector3d(newLines.get(j).x2p, newLines.get(j).y2p, newLines.get(j).z2p);
                        Vector3d line1 = Vector3d.sub(p12, p11);
                        Vector3d line2 = Vector3d.sub(p22, p21);
                        Vector3d temp = Vector3d.cross(line1, line2);
                        BigDecimal angle = Vector3d.manhattan(temp, zero).divide(Vector3d.manhattan(p12, p11), Threshold.MC).divide(Vector3d.manhattan(p22, p21), Threshold.MC);
                        if (angle.compareTo(smallangle) < 0)
                        {
                            colin[i] = distline;
                            colin[j] = distline;
                        }
                    }

                    distline++;
                }
            }

            for (int i=0; i<lineCount-1; i++)
            {
                if(colin[i] > 0)
                {
                    flag=1;
                    while (flag==1)
                    {
                        flag=0;
                        for (int j=i+1; j<lineCount; j++)
                        {
                            if(colin[i]==colin[j])
                            {
                                Vector3d p11 = new Vector3d(newLines.get(i).x1p, newLines.get(i).y1p, newLines.get(i).z1p);
                                Vector3d p12 = new Vector3d(newLines.get(i).x2p, newLines.get(i).y2p, newLines.get(i).z2p);
                                Vector3d p21 = new Vector3d(newLines.get(j).x1p, newLines.get(j).y1p, newLines.get(j).z1p);
                                Vector3d p22 = new Vector3d(newLines.get(j).x2p, newLines.get(j).y2p, newLines.get(j).z2p);
                                if(Vector3d.manhattan(p11, p21).compareTo(small) < 0 ||
                                        Vector3d.manhattan(p11, p22).compareTo(small) < 0 ||
                                        Vector3d.manhattan(p12, p22).compareTo(small) < 0 ||
                                        Vector3d.manhattan(p12, p21).compareTo(small) < 0)
                                {
                                    int a = 1;
                                    int b = 0;
                                    BigDecimal max;
                                    BigDecimal cur;
                                    max = Vector3d.manhattan(p11, p21);
                                    if ((cur = Vector3d.manhattan(p11, p22)).compareTo(max) > 0)
                                    {
                                        a=1; b=1;
                                        max = cur;
                                    }
                                    if ((cur = Vector3d.manhattan(p12, p21)).compareTo(max) > 0)
                                    {
                                        a=0; b=0;
                                        max = cur;
                                    }
                                    if (Vector3d.manhattan(p12, p22).compareTo(max) > 0)
                                    {
                                        a=0; b=1;
                                    }
                                    GData2 l1 = newLines.get(i);
                                    GData2 l2 = newLines.get(j);
                                    GColour c = new GColour(24, Colour.lineColourR, Colour.lineColourG, Colour.lineColourB, 1f);
                                    GData2 nl;
                                    if (a == 1) {
                                        if (b == 1) {
                                            nl = new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), l1.x1p, l1.y1p, l1.z1p, l2.x2p, l2.y2p, l2.z2p, View.DUMMY_REFERENCE, linkedDatFile, true);
                                        } else {
                                            nl = new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), l1.x1p, l1.y1p, l1.z1p, l2.x1p, l2.y1p, l2.z1p, View.DUMMY_REFERENCE, linkedDatFile, true);
                                        }
                                    } else {
                                        if (b == 1) {
                                            nl = new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), l2.x2p, l2.y2p, l2.z2p, l1.x2p, l1.y2p, l1.z2p, View.DUMMY_REFERENCE, linkedDatFile, true);
                                        } else {
                                            nl = new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), l2.x1p, l2.y1p, l2.z1p, l1.x2p, l1.y2p, l1.z2p, View.DUMMY_REFERENCE, linkedDatFile, true);
                                        }
                                    }
                                    remove(l1);
                                    newLines.remove(i);
                                    newLines.add(i, nl);
                                    colin[j]=-1;
                                    flag = 1;
                                }
                            }
                        }
                    }
                }
            }

            // Remove invalid collinear lines
            {
                int counter = 0;
                for (Iterator<GData2> li = newLines.iterator(); li.hasNext();) {
                    GData2 l = li.next();
                    if (colin[counter] < 0) {
                        remove(l);
                        li.remove();
                    }
                    counter++;
                }
            }

            // Append the lines
            for (GData2 line : newLines) {
                linkedDatFile.addToTailOrInsertAfterCursor(line);
            }

            // Round to 6 decimal places

            selectedLines.addAll(newLines);
            selectedData.addAll(selectedLines);

            roundSelection(6, 10, true, false, true, true, true);

            clearSelection();
            setModifiedNoSync();
        }

        selectedData.addAll(selectedLines);
        selectedData.addAll(selectedCondlines);
        delete(false, false);

        if (isModified()) {
            setModified(true, true);
        }

        validateState();
        return newLines.size();
    }

    public int[] intersector(final IntersectorSettings ins, boolean syncWithTextEditor) {
        Composite3D c3d =  linkedDatFile.getLastSelectedComposite();
        NLogger.debug(getClass(), "Intersector - (C) Nils Schmidt 2015"); //$NON-NLS-1$
        NLogger.debug(getClass(), "======================"); //$NON-NLS-1$
        if (c3d != null) {

            final int[] isCancelled = new int[]{0};


            final Set<GData3> trisToHide = new HashSet<>();
            final Set<GData4> quadsToHide = new HashSet<>();

            final Set<GData2> linesToDelete = new HashSet<>();
            final Set<GData3> trisToDelete = new HashSet<>();
            final Set<GData4> quadsToDelete = new HashSet<>();
            final Set<GData5> condlinesToDelete = new HashSet<>();

            NLogger.debug(getClass(), "Get target surfaces to parse."); //$NON-NLS-1$

            final Set<GData> targetSurfs = new HashSet<>();
            {
                Set<GData3> tris = triangles.keySet();
                for (GData3 tri : tris) {
                    if (!hiddenData.contains(tri)) {
                        targetSurfs.add(tri);
                    }
                }
            }
            {
                Set<GData4> qs = quads.keySet();
                for (GData4 quad : qs) {
                    if (!hiddenData.contains(quad)) {
                        targetSurfs.add(quad);
                    }
                }
            }

            NLogger.debug(getClass(), "Cleanup the selection."); //$NON-NLS-1$

            for(Iterator<GData3> ti = selectedTriangles.iterator(); ti.hasNext();) {
                GData3 tri = ti.next();
                if (!lineLinkedToVertices.containsKey(tri)) {
                    ti.remove();
                }
            }
            for(Iterator<GData4> qi = selectedQuads.iterator(); qi.hasNext();) {
                GData4 quad = qi.next();
                if (!lineLinkedToVertices.containsKey(quad)) {
                    qi.remove();
                }
            }
            for(Iterator<GData2> li = selectedLines.iterator(); li.hasNext();) {
                GData2 line = li.next();
                if (!lineLinkedToVertices.containsKey(line)) {
                    li.remove();
                }
            }
            for(Iterator<GData5> ci = selectedCondlines.iterator(); ci.hasNext();) {
                GData5 condline = ci.next();
                if (!lineLinkedToVertices.containsKey(condline)) {
                    ci.remove();
                }
            }

            final List<GData> originObjects = new ArrayList<>();
            originObjects.addAll(selectedTriangles);
            originObjects.addAll(selectedQuads);
            originObjects.addAll(selectedLines);
            originObjects.addAll(selectedCondlines);

            // Remove adjacent non-selected surfaces from targetSurfs!
            {
                SortedSet<Vertex> verts = new TreeSet<>();
                for (GData g3 : selectedTriangles) {
                    Vertex[] verts2 = triangles.get(g3);
                    verts.addAll(Arrays.asList(verts2));
                }
                for (GData g4 : selectedQuads) {
                    Vertex[] verts2 = quads.get(g4);
                    verts.addAll(Arrays.asList(verts2));
                }
                for (Vertex vertex : verts) {
                    Collection<GData> surfs = getLinkedSurfaces(vertex);
                    for (GData g : surfs) {
                        switch (g.type()) {
                        case 3:
                            trisToHide.add((GData3) g);
                            break;
                        case 4:
                            quadsToHide.add((GData4) g);
                            break;
                        default:
                            break;
                        }
                    }
                    targetSurfs.removeAll(surfs);
                }
            }

            clearSelection();

            final List<IntersectionInfoWithColour> intersections = new ArrayList<>();
            final Set<GData2> newLines =  Collections.newSetFromMap(new ThreadsafeHashMap<>());
            final Set<GData3> newTriangles = Collections.newSetFromMap(new ThreadsafeHashMap<>());
            final Set<GData5> newCondlines =  Collections.newSetFromMap(new ThreadsafeHashMap<>());
            try
            {
                new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
                {
                    @Override
                    public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                    {
                        try
                        {
                            monitor.beginTask(I18n.VM_INTERSECTOR, IProgressMonitor.UNKNOWN);

                            {

                                final Set<IntersectionInfoWithColour> intersectionSet = Collections.newSetFromMap(new ThreadsafeHashMap<>());

                                final int iterations = originObjects.size();
                                final int chunks = View.NUM_CORES;
                                final Thread[] threads = new Thread[chunks];

                                final String surfCount = "/" + iterations;//$NON-NLS-1$
                                final AtomicInteger counter2 = new AtomicInteger(0);

                                int lastend = 0;
                                for (int j = 0; j < chunks; ++j) {
                                    final int[] start = new int[] { lastend };
                                    lastend = iterations / chunks * (j + 1);
                                    final int[] end = new int[] { lastend };
                                    if (j == chunks - 1) {
                                        end[0] = iterations;
                                    }
                                    threads[j] = new Thread(() -> {
                                        for (int k = start[0]; k < end[0]; k++) {
                                            monitor.subTask(counter2.toString() + surfCount);
                                            GData o = originObjects.get(k);
                                            /* Check if the monitor has been canceled */
                                            if (monitor.isCanceled()) {
                                                isCancelled[0] = 1;
                                                return;
                                            }
                                            counter2.incrementAndGet();
                                            IntersectionInfoWithColour ii = getIntersectionInfo(o, targetSurfs, ins);
                                            if (ii != null) {
                                                intersectionSet.add(ii);
                                                switch (o.type()) {
                                                case 2:
                                                    linesToDelete.add((GData2) o);
                                                    break;
                                                case 3:
                                                    trisToDelete.add((GData3) o);
                                                    break;
                                                case 4:
                                                    quadsToDelete.add((GData4) o);
                                                    break;
                                                case 5:
                                                    condlinesToDelete.add((GData5) o);
                                                    break;
                                                default:
                                                    break;
                                                }
                                            }
                                        }
                                    });
                                    threads[j].start();
                                }
                                boolean isRunning = true;
                                while (isRunning) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException ie) {
                                        Thread.currentThread().interrupt();
                                        throw new LDPartEditorException(ie);
                                    }
                                    isRunning = false;
                                    for (Thread thread : threads) {
                                        if (thread.isAlive())
                                            isRunning = true;
                                    }
                                }
                                intersections.addAll(intersectionSet);
                            }

                            if (isCancelled[0] > 0) return;

                            for (GData t : targetSurfs) {
                                switch (t.type()) {
                                case 3:
                                    trisToHide.add((GData3) t);
                                    break;
                                case 4:
                                    quadsToHide.add((GData4) t);
                                    break;
                                default:
                                    break;
                                }
                            }

                            NLogger.debug(getClass(), "Create new faces."); //$NON-NLS-1$

                            {
                                final int iterations = intersections.size();
                                final int chunks = View.NUM_CORES;
                                final Thread[] threads = new Thread[chunks];
                                
                                if (NLogger.debugging) {
                                    for (IntersectionInfoWithColour intersection : intersections) {
                                        NLogger.debug(getClass(), "Intersection:\n" + intersection.toString()); //$NON-NLS-1$
                                    }
                                }


                                final String maxIterations = "/" + iterations;//$NON-NLS-1$
                                final AtomicInteger counter2 = new AtomicInteger(0);

                                int lastend = 0;
                                for (int j = 0; j < chunks; ++j) {
                                    final int[] start = new int[] { lastend };
                                    lastend = iterations / chunks * (j + 1);
                                    final int[] end = new int[] { lastend };
                                    if (j == chunks - 1) {
                                        end[0] = iterations;
                                    }
                                    threads[j] = new Thread(() -> {
                                        for (int k = start[0]; k < end[0]; k++) {
                                            monitor.subTask(counter2.toString() + maxIterations);
                                            IntersectionInfoWithColour info = intersections.get(k);
                                            if (monitor.isCanceled()) {
                                                isCancelled[0] = 2;
                                                return;
                                            }
                                            counter2.incrementAndGet();

                                            final List<Vector3dd> av = info.getAllVertices();
                                            final List<GColour> cols = info.getColours();
                                            final List<Integer> ts = info.getIsLine();

                                            newTriangles.addAll(MathHelper.triangulatePointGroups(cols, av, ts, View.DUMMY_REFERENCE, linkedDatFile));
                                            newLines.addAll(MathHelper.triangulatePointGroups2(cols, av, ts, View.DUMMY_REFERENCE, linkedDatFile));
                                            newCondlines.addAll(MathHelper.triangulatePointGroups5(cols, av, ts, View.DUMMY_REFERENCE, linkedDatFile));
                                        }
                                    });
                                    threads[j].start();
                                }
                                boolean isRunning = true;
                                while (isRunning) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException ie) {
                                        Thread.currentThread().interrupt();
                                        throw new LDPartEditorException(ie);
                                    }
                                    isRunning = false;
                                    for (Thread thread : threads) {
                                        if (thread.isAlive())
                                            isRunning = true;
                                    }
                                }
                            }

                        }
                        finally
                        {
                            monitor.done();
                        }
                    }
                });
            }
            catch (InvocationTargetException ite) {
                NLogger.error(VM12IntersectorAndIsecalc.class, ite);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new LDPartEditorException(ie);
            }


            NLogger.debug(getClass(), "Check for identical vertices and collinearity."); //$NON-NLS-1$
            final Set<GData3> trisToDelete2 = new HashSet<>();
            {
                for (GData3 g3 : newTriangles) {
                    Vertex[] verts = triangles.get(g3);
                    SortedSet<Vertex> verts2 = new TreeSet<>();
                    verts2.addAll(Arrays.asList(verts));
                    if (verts2.size() < 3 || g3.isCollinear()) {
                        trisToDelete2.add(g3);
                    }
                }
            }


            if (isCancelled[0] == 0) {
                NLogger.debug(getClass(), "Hide intersecting faces."); //$NON-NLS-1$

                if (ins.isHidingOther()) {
                    selectedTriangles.addAll(trisToHide);
                    selectedQuads.addAll(quadsToHide);
                    selectedData.addAll(selectedTriangles);
                    selectedData.addAll(selectedQuads);
                    selectedSubfiles.clear();
                    selectedSubfiles.addAll(vertexCountInSubfile.keySet());
                    selectedData.addAll(selectedSubfiles);
                    hideSelection();
                }

                clearSelection();

                NLogger.debug(getClass(), "Delete old selected objects."); //$NON-NLS-1$

                selectedLines.addAll(linesToDelete);
                selectedTriangles.addAll(trisToDelete);
                selectedQuads.addAll(quadsToDelete);
                selectedCondlines.addAll(condlinesToDelete);
                selectedData.addAll(selectedLines);
                selectedData.addAll(selectedTriangles);
                selectedData.addAll(selectedQuads);
                selectedData.addAll(selectedCondlines);
                delete(false, false);
            } else {
                clearSelection();
            }

            // Append the new data
            for (GData3 tri : newTriangles) {
                linkedDatFile.addToTailOrInsertAfterCursor(tri);
            }

            for (GData2 lin : newLines) {
                linkedDatFile.addToTailOrInsertAfterCursor(lin);
            }

            for (GData5 clin : newCondlines) {
                linkedDatFile.addToTailOrInsertAfterCursor(clin);
            }

            NLogger.debug(getClass(), "Delete new, but invalid objects."); //$NON-NLS-1$

            newTriangles.removeAll(trisToDelete2);
            selectedTriangles.addAll(trisToDelete2);
            selectedData.addAll(selectedTriangles);
            delete(false, false);

            // Round to 6 decimal places

            selectedLines.addAll(newLines);
            selectedTriangles.addAll(newTriangles);
            selectedCondlines.addAll(newCondlines);
            selectedData.addAll(selectedLines);
            selectedData.addAll(selectedTriangles);
            selectedData.addAll(selectedCondlines);

            NLogger.debug(getClass(), "Round."); //$NON-NLS-1$
            roundSelection(6, 10, true, false, true, true, true);

            clearSelection();
            if (syncWithTextEditor) {
                setModified(true, true);
            } else {
                setModifiedNoSync();
            }

            NLogger.debug(getClass(), "Done."); //$NON-NLS-1$

            validateState();
            return new int[] {newLines.size(), newTriangles.size(), newCondlines.size(), linesToDelete.size(), trisToDelete.size(), quadsToDelete.size(), condlinesToDelete.size()};
        } else {
            NLogger.debug(getClass(), "No 3D view selected. Cancel process."); //$NON-NLS-1$
        }
        
        return new int[] {0, 0, 0, 0, 0, 0, 0};
    }

    private Set<GData2> intersectionLines(GData g1, GData g2) {

        GColour c = new GColour(24, Colour.lineColourR, Colour.lineColourG, Colour.lineColourB, 1f);

        Set<GData2> result = new HashSet<>();
        Set<Vector3d> points = new HashSet<>();

        int t1 = g1.type();
        int t2 = g2.type();

        if (t1 == 3 && t2 == 3) {
            Vertex[] v1 = triangles.get(g1);
            Vertex[] v2 = triangles.get(g2);
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[0], v1[1], v2[0], v2[1], v2[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[1], v1[2], v2[0], v2[1], v2[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[2], v1[0], v2[0], v2[1], v2[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[0], v2[1], v1[0], v1[1], v1[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[1], v2[2], v1[0], v1[1], v1[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[2], v2[0], v1[0], v1[1], v1[2], p)) {
                    points.add(p);
                }
            }
        } else if (t1 == 4 && t2 == 4) {
            Vertex[] v1 = quads.get(g1);
            Vertex[] v2 = quads.get(g2);
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[0], v1[1], v2[0], v2[1], v2[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[1], v1[2], v2[0], v2[1], v2[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[2], v1[3], v2[0], v2[1], v2[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[3], v1[0], v2[0], v2[1], v2[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[0], v1[1], v2[2], v2[3], v2[0], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[1], v1[2], v2[2], v2[3], v2[0], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[2], v1[3], v2[2], v2[3], v2[0], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[3], v1[0], v2[2], v2[3], v2[0], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[0], v2[1], v1[0], v1[1], v1[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[1], v2[2], v1[0], v1[1], v1[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[2], v2[3], v1[0], v1[1], v1[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[3], v2[0], v1[0], v1[1], v1[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[0], v2[1], v1[2], v1[3], v1[0], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[1], v2[2], v1[2], v1[3], v1[0], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[2], v2[3], v1[2], v1[3], v1[0], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[3], v2[0], v1[2], v1[3], v1[0], p)) {
                    points.add(p);
                }
            }
        }

        if (t1 == 4 && t2 == 3) {
            GData g3 = g1;
            g1 = g2;
            g2 = g3;
            t1 = 3;
            t2 = 4;
        }

        if (t1 == 3 && t2 == 4) {
            Vertex[] v1 = triangles.get(g1);
            Vertex[] v2 = quads.get(g2);
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[0], v1[1], v2[0], v2[1], v2[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[1], v1[2], v2[0], v2[1], v2[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[2], v1[0], v2[0], v2[1], v2[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[0], v1[1], v2[2], v2[3], v2[0], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[1], v1[2], v2[2], v2[3], v2[0], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[2], v1[0], v2[2], v2[3], v2[0], p)) {
                    points.add(p);
                }
            }

            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[0], v2[1], v1[0], v1[1], v1[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[1], v2[2], v1[0], v1[1], v1[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[2], v2[3], v1[0], v1[1], v1[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[3], v2[0], v1[0], v1[1], v1[2], p)) {
                    points.add(p);
                }
            }
        }

        final BigDecimal epsilon = new BigDecimal(".0001"); //$NON-NLS-1$
        for(Iterator<Vector3d> i = points.iterator(); i.hasNext(); ) {
            Vector3d p1 = i.next();
            for (Vector3d p2 : points) {
                if (!p1.equals(p2)) {
                    Vector3d p3 = Vector3d.sub(p1, p2);
                    BigDecimal md = p3.x.multiply(p3.x).add(p3.y.multiply(p3.y)).add(p3.z.multiply(p3.z));
                    if (md.compareTo(epsilon) <= 0) {
                        i.remove();
                        break;
                    }
                }
            }
        }
        if (points.size() == 4) {
            List<Vector3d> points2 = new ArrayList<>();
            points2.addAll(points);
            result.add(new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), points2.get(0).x, points2.get(0).y, points2.get(0).z, points2.get(1).x, points2.get(1).y, points2.get(1).z, View.DUMMY_REFERENCE, linkedDatFile, true));
            result.add(new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), points2.get(1).x, points2.get(1).y, points2.get(1).z, points2.get(2).x, points2.get(2).y, points2.get(2).z, View.DUMMY_REFERENCE, linkedDatFile, true));
            result.add(new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), points2.get(2).x, points2.get(2).y, points2.get(2).z, points2.get(3).x, points2.get(3).y, points2.get(3).z, View.DUMMY_REFERENCE, linkedDatFile, true));
            result.add(new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), points2.get(0).x, points2.get(0).y, points2.get(0).z, points2.get(3).x, points2.get(3).y, points2.get(3).z, View.DUMMY_REFERENCE, linkedDatFile, true));
        } else if (points.size() == 3) {
            List<Vector3d> points2 = new ArrayList<>();
            points2.addAll(points);
            result.add(new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), points2.get(0).x, points2.get(0).y, points2.get(0).z, points2.get(1).x, points2.get(1).y, points2.get(1).z, View.DUMMY_REFERENCE, linkedDatFile, true));
            result.add(new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), points2.get(2).x, points2.get(2).y, points2.get(2).z, points2.get(1).x, points2.get(1).y, points2.get(1).z, View.DUMMY_REFERENCE, linkedDatFile, true));
            result.add(new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), points2.get(0).x, points2.get(0).y, points2.get(0).z, points2.get(2).x, points2.get(2).y, points2.get(2).z, View.DUMMY_REFERENCE, linkedDatFile, true));
        } else if (points.size() == 2) {
            List<Vector3d> points2 = new ArrayList<>();
            points2.addAll(points);
            result.add(new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), points2.get(0).x, points2.get(0).y, points2.get(0).z, points2.get(1).x, points2.get(1).y, points2.get(1).z, View.DUMMY_REFERENCE, linkedDatFile, true));
        }

        return result;
    }

    @SuppressWarnings("java:S2111")
    private IntersectionInfoWithColour getIntersectionInfo(GData origin, Set<GData> targetSurfs, IntersectorSettings ins) {
        NLogger.debug(getClass(), "Calculate intersection info for {0}.", origin.toString()); //$NON-NLS-1$
        
        final BigDecimal minDist = new BigDecimal(".0001"); //$NON-NLS-1$

        final int ot = origin.type();

        final List<List<Vector3dd>> fixedIntersectionLines = new ArrayList<>();
        final List<List<Vector3dd>> allLines = new ArrayList<>();
        final List<Vector3dd> fixedVertices = new ArrayList<>();

        final Map<GData, List<Vector3dd>> intersections = new HashMap<>();

        Vertex[] ov = null;
        switch (ot) {
        case 2:
            ov = lines.get(origin);
            break;
        case 3:
            ov = triangles.get(origin);
            break;
        case 4:
            ov = quads.get(origin);
            break;
        case 5:
            ov = condlines.get(origin);
            break;
        default:
            return null;
        }

        if (ot == 2 || ot == 5) {

            if (getLineFaceIntersection(fixedVertices, targetSurfs, ov)) {

                final List<Vector3dd> resultVertices = new ArrayList<>();
                final List<GColour> resultColours = new ArrayList<>();
                final List<Integer> resultIsLine = new ArrayList<>();

                Vector3dd start = fixedVertices.get(0);

                Vector3d normal = new Vector3d(new BigDecimal(1.34), new BigDecimal(-.77), new BigDecimal(2));
                normal.normalise(normal);
                for (int i = 1; i < fixedVertices.size(); i++) {
                    Vector3dd end = fixedVertices.get(i);


                    if (ins.isColourise()) {

                        // Calculate pseudo mid-point
                        Vector3dd mid = new Vector3dd();
                        mid.setX(start.x.multiply(MathHelper.R1).add(end.x.multiply(MathHelper.R2.add(MathHelper.R3))));
                        mid.setY(start.y.multiply(MathHelper.R1).add(end.y.multiply(MathHelper.R2.add(MathHelper.R3))));
                        mid.setZ(start.z.multiply(MathHelper.R1).add(end.z.multiply(MathHelper.R2.add(MathHelper.R3))));

                        int intersectionCount = 0;


                        for (Vertex[] v : triangles.values()) {
                            if (intersectRayTriangle(mid, normal, new Vector3dd(v[0]), new Vector3dd(v[1]), new Vector3dd(v[2]))) {
                                intersectionCount += 1;
                            }
                        }
                        for (Vertex[] v : quads.values()) {
                            if (
                                    intersectRayTriangle(mid, normal, new Vector3dd(v[0]), new Vector3dd(v[1]), new Vector3dd(v[2])) ||
                                    intersectRayTriangle(mid, normal, new Vector3dd(v[2]), new Vector3dd(v[3]), new Vector3dd(v[0]))) {
                                intersectionCount += 1;
                            }
                        }
                        resultVertices.add(start);
                        resultVertices.add(end);
                        if (ot == 2) {
                            resultIsLine.add(1);
                        } else {
                            resultVertices.add(new Vector3dd(ov[2]));
                            resultVertices.add(new Vector3dd(ov[3]));
                            resultIsLine.add(2);
                        }
                        if (intersectionCount == 1) {
                            resultColours.add(LDConfig.getColour(0));
                        } else if (intersectionCount % 2 == 0) {
                            resultColours.add(LDConfig.getColour(28));
                        } else {
                            resultColours.add(LDConfig.getColour(1));
                        }

                    } else {
                        final float R;
                        final float G;
                        final float B;
                        final float A;
                        final int CN;
                        resultVertices.add(start);
                        resultVertices.add(end);
                        if (ot == 2) {
                            GData2 origin2 = (GData2) origin;
                            CN = origin2.colourNumber;
                            R = origin2.r;
                            G = origin2.g;
                            B = origin2.b;
                            A = origin2.a;
                            resultIsLine.add(1);
                        } else {
                            GData5 origin2 = (GData5) origin;
                            CN = origin2.colourNumber;
                            R = origin2.r;
                            G = origin2.g;
                            B = origin2.b;
                            A = origin2.a;
                            resultVertices.add(new Vector3dd(ov[2]));
                            resultVertices.add(new Vector3dd(ov[3]));
                            resultIsLine.add(2);
                        }
                        resultColours.add(new GColour(CN, R, G, B, A));
                    }
                    start = end;
                }


                NLogger.debug(getClass(), "Result A"); //$NON-NLS-1$
                return new IntersectionInfoWithColour(resultColours, resultVertices, resultIsLine);
            } else {
                NLogger.debug(getClass(), "Result B"); //$NON-NLS-1$
                return null;
            }

        } else {
            for (GData targetSurf : targetSurfs) {
                final int tt = targetSurf.type();

                if (ot == 3 && tt == 3) {

                    Vertex[] tv = triangles.get(targetSurf);

                    getTriangleTriangleIntersection(intersections, targetSurf, ov, tv, false, false);

                } else if (ot == 4 && tt == 4) {

                    Vertex[] tv = quads.get(targetSurf);

                    Vertex[] ov1 = new Vertex[]{ov[0], ov[1], ov[2]};
                    Vertex[] ov2 = new Vertex[]{ov[2], ov[3], ov[0]};
                    Vertex[] tv1 = new Vertex[]{tv[0], tv[1], tv[2]};
                    Vertex[] tv2 = new Vertex[]{tv[2], tv[3], tv[0]};

                    getTriangleTriangleIntersection(intersections, targetSurf, ov1, tv1, true, true);
                    getTriangleTriangleIntersection(intersections, targetSurf, ov1, tv2, true, true);
                    getTriangleTriangleIntersection(intersections, targetSurf, ov2, tv1, true, true);
                    getTriangleTriangleIntersection(intersections, targetSurf, ov2, tv2, true, true);

                } else if (ot == 4 && tt == 3) {
                    Vertex[] tv = triangles.get(targetSurf);

                    Vertex[] tv1 = new Vertex[]{tv[0], tv[1], tv[2]};
                    Vertex[] ov1 = new Vertex[]{ov[0], ov[1], ov[2]};
                    Vertex[] ov2 = new Vertex[]{ov[2], ov[3], ov[0]};

                    getTriangleTriangleIntersection(intersections, targetSurf, ov1, tv1, true, false);
                    getTriangleTriangleIntersection(intersections, targetSurf, ov2, tv1, true, false);

                } else if (ot == 3 && tt == 4) {

                    Vertex[] tv = quads.get(targetSurf);

                    Vertex[] ov1 = new Vertex[]{ov[0], ov[1], ov[2]};
                    Vertex[] tv1 = new Vertex[]{tv[0], tv[1], tv[2]};
                    Vertex[] tv2 = new Vertex[]{tv[2], tv[3], tv[0]};

                    getTriangleTriangleIntersection(intersections, targetSurf, ov1, tv1, false, true);
                    getTriangleTriangleIntersection(intersections, targetSurf, ov1, tv2, false, true);

                }
            }

            for (List<Vector3dd> line : intersections.values()) {
                if (line.size() > 1) {
                    fixedIntersectionLines.add(line);
                }
            }

            // Check intersections within the fixed intersection lines
            {
                List<List<Vector3dd>> linesToRemove = new ArrayList<>();
                List<List<Vector3dd>> newLines = new ArrayList<>();
                for (Iterator<List<Vector3dd>> iterator = fixedIntersectionLines.iterator(); iterator.hasNext();) {
                    List<Vector3dd> line = iterator.next();
                    List<Vector3d> intersect = new ArrayList<>();
                    for (List<Vector3dd> line2 : fixedIntersectionLines) {
                        if (line2 != line) {
                            SortedSet<Vector3dd> allVertices = new TreeSet<>();
                            for(int l = 0; l < 2; l++) {
                                allVertices.add(line.get(l));
                                allVertices.add(line2.get(l));
                            }
                            if (allVertices.size() == 4) {
                                Vector3d ip = intersectLineLineSegmentUnidirectional2(line.get(0), line.get(1), line2.get(0), line2.get(1));
                                if (ip != null) {
                                    intersect.add(ip);
                                }
                            }
                        }
                    }
                    if (!intersect.isEmpty()) {
                        SortedMap<BigDecimal, Vector3d> linePoints = new TreeMap<>();
                        Vector3d start = line.get(0);
                        Vector3d end = line.get(1);
                        for (Vector3d v : intersect) {
                            BigDecimal dist = Vector3d.manhattan(v, start);
                            linePoints.put(dist, v);
                        }
                        BigDecimal dist = Vector3d.manhattan(end, start);
                        linePoints.put(dist, end);

                        for (Vector3d point : linePoints.values()) {
                            end = point;
                            List<Vector3dd> newLine = new ArrayList<>();
                            newLine.add(new Vector3dd(start));
                            newLine.add(new Vector3dd(end));
                            newLines.add(newLine);
                            start = end;
                        }
                        linesToRemove.add(line);
                    }
                }
                fixedIntersectionLines.removeAll(linesToRemove);
                fixedIntersectionLines.addAll(newLines);
            }

            final List<Vector3dd> resultVertices = new ArrayList<>();
            final List<GColour> resultColours = new ArrayList<>();
            final List<Integer> resultIsLine = new ArrayList<>();

            Vector3d originalNormal = null;

            switch (ot) {
            case 3:
            {
                fixedVertices.add(new Vector3dd(ov[0]).round());
                fixedVertices.add(new Vector3dd(ov[1]).round());
                fixedVertices.add(new Vector3dd(ov[2]).round());
                GData3 g3 = (GData3) origin;
                originalNormal = new Vector3d(new Vertex(g3.xn, g3.yn, g3.zn));
            }
            break;
            case 4:
            {
                fixedVertices.add(new Vector3dd(ov[0]).round());
                fixedVertices.add(new Vector3dd(ov[1]).round());
                fixedVertices.add(new Vector3dd(ov[2]).round());
                fixedVertices.add(new Vector3dd(ov[3]).round());
                GData4 g4 = (GData4) origin;
                originalNormal = new Vector3d(new Vertex(g4.xn, g4.yn, g4.zn));
            }
            break;
            default:
                NLogger.debug(getClass(), "Result C"); //$NON-NLS-1$
                return null;
            }

            {
                final SortedSet<Vector3dd> allVertices = new TreeSet<>();
                for (List<Vector3dd> l : fixedIntersectionLines) {
                    allVertices.add(l.get(0).round());
                    allVertices.add(l.get(1).round());
                }
                allVertices.removeAll(fixedVertices);
                fixedVertices.addAll(allVertices);
            }

            allLines.addAll(fixedIntersectionLines);
            if (!allLines.isEmpty()) {
                final int vc = fixedVertices.size();
                for (int i = 0; i < vc; i++) {
                    for (int j = 0; j < vc; j++) {
                        if (i == j) continue;
                        boolean intersect = false;
                        Vector3dd v1 = fixedVertices.get(i);
                        Vector3dd v2 = fixedVertices.get(j);
                        int lc = allLines.size();
                        for (int k = 0; k < lc; k++) {
                            List<Vector3dd> l = allLines.get(k);
                            Vector3dd v3 = l.get(0);
                            Vector3dd v4 = l.get(1);
                            if (!v1.equals(v3) && !v1.equals(v4) && !v2.equals(v3) && !v2.equals(v4) && intersectLineLineSegmentUnidirectional(v1, v2, v3, v4)) {
                                intersect = true;
                                break;
                            }
                            if (Vector3d.manhattan(v1, v3).compareTo(minDist) < 0 && Vector3d.manhattan(v2, v4).compareTo(minDist) < 0 ||
                                    Vector3d.manhattan(v2, v3).compareTo(minDist) < 0 && Vector3d.manhattan(v1, v4).compareTo(minDist) < 0) {
                                intersect = true;
                                break;
                            }
                        }
                        if (!intersect) {
                            BigDecimal dist = Vector3d.manhattan(v1, v2);
                            if (dist.compareTo(minDist) > 0) {
                                List<Vector3dd> nl = new ArrayList<>();
                                nl.add(v1);
                                nl.add(v2);
                                allLines.add(nl);
                            }
                        }
                    }
                }

                int lc = allLines.size();
                {
                    int removed = 0;
                    for (int i = 0; i + removed < lc; i++) {
                        for (int j = i + 1; j + removed < lc; j++) {
                            SortedSet<Vector3dd> allVertices = new TreeSet<>();
                            for(int l = 0; l < 2; l++) {
                                allVertices.add(allLines.get(i).get(l));
                                allVertices.add(allLines.get(j).get(l));
                            }
                            if (allVertices.size() == 2) {
                                removed += 1;
                                allLines.remove(j);
                            }
                        }
                    }

                    lc = allLines.size();

                    removed = 0;
                    for (int i = 0; i + removed < lc; i++) {
                        SortedSet<Vector3dd> allVertices = new TreeSet<>();
                        allVertices.add(allLines.get(i).get(0));
                        allVertices.add(allLines.get(i).get(1));
                        if (allVertices.size() == 1) {
                            removed += 1;
                            allLines.remove(i);
                        }
                    }
                }

                lc = allLines.size();

                for (int i = 0; i < lc; i++) {
                    for (int j = i + 1; j < lc; j++) {
                        for (int k = j + 1; k < lc; k++) {
                            SortedSet<Vector3dd> allVertices = new TreeSet<>();
                            for(int l = 0; l < 2; l++) {
                                allVertices.add(allLines.get(i).get(l).round());
                                allVertices.add(allLines.get(j).get(l).round());
                                allVertices.add(allLines.get(k).get(l).round());
                            }
                            if (allVertices.size() == 3) {
                                Vector3dd[] triVerts = new Vector3dd[3];
                                int l = 0;
                                for (Vector3dd v : allVertices) {
                                    triVerts[l] = v;
                                    l++;
                                }
                                boolean isInsideTriangle = false;
                                Vector3d normal = Vector3d.cross(Vector3d.sub(triVerts[2], triVerts[0]), Vector3d.sub(triVerts[1], triVerts[0]));
                                normal.normalise(normal);
                                for (Vector3dd fixed : fixedVertices) {
                                    if (fixed.equals(triVerts[0])) continue;
                                    if (fixed.equals(triVerts[1])) continue;
                                    if (fixed.equals(triVerts[2])) continue;
                                    if (intersectRayTriangle(fixed, normal, triVerts[0], triVerts[1], triVerts[2])) {
                                        isInsideTriangle = true;
                                        break;
                                    }
                                }
                                if (isInsideTriangle) continue;

                                // Check collinearity
                                {
                                    double angle;
                                    Vector3d vertexA = new Vector3d(triVerts[0]);
                                    Vector3d vertexB = new Vector3d(triVerts[1]);
                                    Vector3d vertexC = new Vector3d(triVerts[2]);
                                    Vector3d a = new Vector3d();
                                    Vector3d b = new Vector3d();
                                    Vector3d c = new Vector3d();
                                    Vector3d.sub(vertexB, vertexA, a);
                                    Vector3d.sub(vertexC, vertexB, b);
                                    Vector3d.sub(vertexC, vertexA, c);

                                    angle = Vector3d.angle(a, c);
                                    double sumAngle = angle;
                                    if (angle < Threshold.COLLINEAR_ANGLE_MINIMUM || angle > Threshold.COLLINEAR_ANGLE_MAXIMUM) {
                                        continue;
                                    }

                                    a.negate();
                                    angle = Vector3d.angle(a, b);
                                    sumAngle = sumAngle + angle;
                                    if (angle < Threshold.COLLINEAR_ANGLE_MINIMUM || angle > Threshold.COLLINEAR_ANGLE_MAXIMUM) {
                                        continue;
                                    }

                                    angle = 180.0 - sumAngle;
                                    if (angle < Threshold.COLLINEAR_ANGLE_MINIMUM || angle > Threshold.COLLINEAR_ANGLE_MAXIMUM) {
                                        continue;
                                    }
                                }

                                if (MathHelper.directionOfVectors(normal, originalNormal) == 1) {
                                    resultVertices.add(triVerts[0]);
                                    resultVertices.add(triVerts[1]);
                                    resultVertices.add(triVerts[2]);
                                } else {
                                    resultVertices.add(triVerts[0]);
                                    resultVertices.add(triVerts[2]);
                                    resultVertices.add(triVerts[1]);
                                }

                                if (ins.isColourise()) {

                                    // Calculate pseudo mid-point
                                    Vector3dd mid = new Vector3dd();

                                    mid.setX(triVerts[0].x.multiply(MathHelper.R1).add(triVerts[1].x.multiply(MathHelper.R2)).add(triVerts[2].x.multiply(MathHelper.R3)));
                                    mid.setY(triVerts[0].y.multiply(MathHelper.R1).add(triVerts[1].y.multiply(MathHelper.R2)).add(triVerts[2].y.multiply(MathHelper.R3)));
                                    mid.setZ(triVerts[0].z.multiply(MathHelper.R1).add(triVerts[1].z.multiply(MathHelper.R2)).add(triVerts[2].z.multiply(MathHelper.R3)));

                                    int intersectionCount = 0;

                                    for (Vertex[] v : triangles.values()) {
                                        if (intersectRayTriangle(mid, normal, new Vector3dd(v[0]), new Vector3dd(v[1]), new Vector3dd(v[2]))) {
                                            intersectionCount += 1;
                                        }
                                    }
                                    for (Vertex[] v : quads.values()) {
                                        if (
                                                intersectRayTriangle(mid, normal, new Vector3dd(v[0]), new Vector3dd(v[1]), new Vector3dd(v[2])) ||
                                                intersectRayTriangle(mid, normal, new Vector3dd(v[2]), new Vector3dd(v[3]), new Vector3dd(v[0]))) {
                                            intersectionCount += 1;
                                        }
                                    }

                                    if (intersectionCount == 1) {
                                        resultColours.add(LDConfig.getColour(7));
                                    } else if (intersectionCount % 2 == 0) {
                                        resultColours.add(LDConfig.getColour(14));
                                    } else {
                                        resultColours.add(LDConfig.getColour(11));
                                    }

                                } else {
                                    final float R;
                                    final float G;
                                    final float B;
                                    final float A;
                                    final int CN;
                                    if (ot == 3) {
                                        GData3 origin2 = (GData3) origin;
                                        CN = origin2.colourNumber;
                                        R = origin2.r;
                                        G = origin2.g;
                                        B = origin2.b;
                                        A = origin2.a;
                                    } else {
                                        GData4 origin2 = (GData4) origin;
                                        CN = origin2.colourNumber;
                                        R = origin2.r;
                                        G = origin2.g;
                                        B = origin2.b;
                                        A = origin2.a;
                                    }
                                    resultColours.add(new GColour(CN, R, G, B, A));
                                }
                                resultIsLine.add(0);
                            }
                        }

                    }
                }

                if (resultVertices.isEmpty()) {
                    NLogger.debug(getClass(), "Result D"); //$NON-NLS-1$
                    return null;
                }
                
                NLogger.debug(getClass(), "Result E"); //$NON-NLS-1$
                return new IntersectionInfoWithColour(resultColours, resultVertices, resultIsLine);
            } else {
                NLogger.debug(getClass(), "Result F"); //$NON-NLS-1$
                return null;
            }
        }
    }

    private boolean getLineFaceIntersection(List<Vector3dd> fixedVertices, Set<GData> targetSurfs, Vertex[] ov) {

        SortedMap<BigDecimal, Vector3d> linePoints = new TreeMap<>();
        Vector3d start = new Vector3d(ov[0]);
        Vector3d end = new Vector3d(ov[1]);

        for (GData g : targetSurfs) {
            Vector3d intersection = new Vector3d();
            switch (g.type()) {
            case 3:
            {
                Vertex[] verts = triangles.get(g);
                if (intersectLineTriangle(ov[0], ov[1], verts[0], verts[1], verts[2], intersection)) {
                    fixedVertices.add(new Vector3dd(intersection));
                    BigDecimal dist = Vector3d.manhattan(intersection, start);
                    linePoints.put(dist, intersection);
                }
            }
            break;
            case 4:
            {
                Vertex[] verts = quads.get(g);
                if (
                        intersectLineTriangle(ov[0], ov[1], verts[0], verts[1], verts[2], intersection) ||
                        intersectLineTriangle(ov[0], ov[1], verts[2], verts[3], verts[0], intersection)) {
                    fixedVertices.add(new Vector3dd(intersection));
                    BigDecimal dist = Vector3d.manhattan(intersection, start);
                    linePoints.put(dist, intersection);
                }
            }
            break;
            default:
                break;
            }
        }

        if (fixedVertices.isEmpty()) {
            return false;
        } else {
            fixedVertices.clear();
            BigDecimal dist = Vector3d.manhattan(end, start);
            linePoints.put(BigDecimal.ZERO, start);
            linePoints.put(dist, end);
            for (Vector3d v : linePoints.values()) {
                fixedVertices.add(new Vector3dd(v));
            }
            return true;
        }

    }

    private void getTriangleTriangleIntersection(Map<GData, List<Vector3dd>> intersections, GData target, Vertex[] ov, Vertex[] tv, boolean originIsQuad, boolean targetIsQuad) {
        List<Vector3dd> result2 = null;
        if (intersections.containsKey(target)) {
            result2 = intersections.get(target);
        } else {
            result2 = new ArrayList<>();
            intersections.put(target, result2);
        }

        final SortedSet<Vector3dd> result = new TreeSet<>();

        {
            Vector3dd r = new Vector3dd();
            if (intersectLineTriangle(tv[0], tv[1], ov[0], ov[1], ov[2], r)) {
                result.add(r.round());
            }
        }
        {
            Vector3dd r = new Vector3dd();
            if (intersectLineTriangle(tv[1], tv[2], ov[0], ov[1], ov[2], r)) {
                result.add(r.round());
            }
        }
        if (!targetIsQuad) {
            Vector3dd r = new Vector3dd();
            if (intersectLineTriangle(tv[2], tv[0], ov[0], ov[1], ov[2], r)) {
                result.add(r.round());
            }
        }
        {
            Vector3dd r = new Vector3dd();
            if (intersectLineTriangle(ov[0], ov[1], tv[0], tv[1], tv[2], r)) {
                result.add(r.round());
            }
        }
        {
            Vector3dd r = new Vector3dd();
            if (intersectLineTriangle(ov[1], ov[2], tv[0], tv[1], tv[2], r)) {
                result.add(r.round());
            }
        }
        if (!originIsQuad) {
            Vector3dd r = new Vector3dd();
            if (intersectLineTriangle(ov[2], ov[0], tv[0], tv[1], tv[2], r)) {
                result.add(r.round());
            }
        }
        result.removeAll(result2);
        result2.addAll(result);
    }

    private boolean intersectLineLineSegmentUnidirectional(Vector3dd p, Vector3dd p2, Vector3dd q, Vector3dd q2) {


        Vector3d sp = Vector3d.sub(p2, p);
        Vector3d sq = Vector3d.sub(q2, q);
        Vector3d c = Vector3d.add(Vector3d.cross(sp, sq), p);
        Vector3d d = Vector3d.sub(p, Vector3d.cross(sp, sq));

        return intersectLineTriangle(new Vertex(q), new Vertex(q2), new Vertex(d), new Vertex(p2), new Vertex(c), c);

    }

    /**
     * FOR ISECALC/INTERSECTOR ONLY
     * @param p
     * @param q
     * @param a
     * @param b
     * @param c
     * @param r
     * @return
     */
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

    protected Vector3d intersectLineLineSegmentUnidirectional2(Vector3dd p, Vector3dd p2, Vector3dd q, Vector3dd q2) {


        Vector3d sp = Vector3d.sub(p2, p);
        Vector3d sq = Vector3d.sub(q2, q);
        Vector3d c = Vector3d.add(Vector3d.cross(sp, sq), p);
        Vector3d d = Vector3d.sub(p, Vector3d.cross(sp, sq));

        return intersectLineTriangle(new Vertex(q), new Vertex(q2), new Vertex(d), new Vertex(p2), new Vertex(c), c) ? c : null;

    }

    public void lines2pattern() {

        if (linkedDatFile.isReadOnly()) return;

        final BigDecimal minDist = new BigDecimal(".0001"); //$NON-NLS-1$

        final Set<GData2> originalSelectionLines = new HashSet<>();
        final Set<GData3> originalSelectionTriangles = new HashSet<>();
        final Set<GData4> originalSelectionQuads = new HashSet<>();
        final Set<GData3> newTriangles = new HashSet<>();
        final Set<GData3> colouredTriangles = new HashSet<>();

        final List<List<Vector3dd>> linesToParse = new ArrayList<>();
        final List<List<Vector3dd>> colourLines = new ArrayList<>();
        final List<List<Vector3dh>> linesToParseHashed = new ArrayList<>();

        final Map<List<Vector3dd>, GColour> colours = new HashMap<>();

        final int chunks = View.NUM_CORES;

        originalSelectionLines.addAll(selectedLines);
        originalSelectionTriangles.addAll(selectedTriangles);
        originalSelectionQuads.addAll(selectedQuads);

        final Vector3d originalNormal = new Vector3d(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE);

        // Verify
        try
        {
            new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
            {
                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                {
                    try
                    {
                        monitor.beginTask(I18n.VM_LINES_2_PATTERN, IProgressMonitor.UNKNOWN);

                        for (GData3 g3 : selectedTriangles) {
                            SortedSet<Vertex> vs = new TreeSet<>();
                            Vertex[] verts = triangles.get(g3);
                            vs.addAll(Arrays.asList(verts));
                            if (vs.size() != 3) return;
                            {
                                List<Vector3dd> l = new ArrayList<>();
                                l.add(new Vector3dd(verts[0]));
                                l.add(new Vector3dd(verts[1]));
                                linesToParse.add(l);
                            }
                            {
                                List<Vector3dd> l = new ArrayList<>();
                                l.add(new Vector3dd(verts[1]));
                                l.add(new Vector3dd(verts[2]));
                                linesToParse.add(l);
                            }
                            {
                                List<Vector3dd> l = new ArrayList<>();
                                l.add(new Vector3dd(verts[2]));
                                l.add(new Vector3dd(verts[0]));
                                linesToParse.add(l);
                            }
                        }

                        for (GData4 g4 : selectedQuads) {
                            SortedSet<Vertex> vs = new TreeSet<>();
                            Vertex[] verts = quads.get(g4);
                            vs.addAll(Arrays.asList(verts));
                            if (vs.size() != 4) return;
                            {
                                List<Vector3dd> l = new ArrayList<>();
                                l.add(new Vector3dd(verts[0]));
                                l.add(new Vector3dd(verts[1]));
                                linesToParse.add(l);
                            }
                            {
                                List<Vector3dd> l = new ArrayList<>();
                                l.add(new Vector3dd(verts[1]));
                                l.add(new Vector3dd(verts[2]));
                                linesToParse.add(l);
                            }
                            {
                                List<Vector3dd> l = new ArrayList<>();
                                l.add(new Vector3dd(verts[2]));
                                l.add(new Vector3dd(verts[3]));
                                linesToParse.add(l);
                            }
                            {
                                List<Vector3dd> l = new ArrayList<>();
                                l.add(new Vector3dd(verts[3]));
                                l.add(new Vector3dd(verts[0]));
                                linesToParse.add(l);
                            }
                            {
                                List<Vector3dd> l = new ArrayList<>();
                                l.add(new Vector3dd(verts[1]));
                                l.add(new Vector3dd(verts[3]));
                                linesToParse.add(l);
                            }
                        }

                        SortedSet<Vertex> m1 = new TreeSet<>();
                        SortedSet<Vertex> m2 = new TreeSet<>();
                        for (GData2 g2 : selectedLines) {
                            Vertex[] verts = lines.get(g2);
                            for (Vertex v : verts) {
                                if (g2.colourNumber == 24) {
                                    if (m1.contains(v)) {
                                        m2.add(v);
                                    } else {
                                        m1.add(v);
                                    }
                                }
                            }
                            List<Vector3dd> l = new ArrayList<>();
                            l.add(new Vector3dd(verts[0]));
                            l.add(new Vector3dd(verts[1]));
                            if (g2.colourNumber == 24) {
                                linesToParse.add(l);
                            } else {
                                colourLines.add(l);
                                colours.put(l, new GColour(g2.colourNumber, g2.r, g2.g, g2.b, g2.a));
                            }
                        }
                        if (m1.size() != m2.size()) return;

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
                        if (p1 == null) return;
                        for (Vertex vertex : m2) {
                            if (!vertex.equals(p1)) {
                                p2 = vertex;
                                break;
                            }
                        }
                        if (p2 == null) return;
                        for (Vertex vertex : m2) {
                            if (!vertex.equals(p1) && !vertex.equals(p2)) {
                                p3 = vertex;
                                break;
                            }
                        }
                        if (p3 == null) return;
                        Vector3d a = new Vector3d(p1.xp.add(s.xp), p1.yp.add(s.yp),p1.zp.add(s.zp));
                        Vector3d b = new Vector3d(p2.xp.add(s.xp), p2.yp.add(s.yp),p2.zp.add(s.zp));
                        Vector3d c = new Vector3d(p3.xp.add(s.xp), p3.yp.add(s.yp),p3.zp.add(s.zp));

                        Vector3d pOrigin = new Vector3d(p1);
                        Vector3d n = Vector3d.cross(Vector3d.sub(a, c), Vector3d.sub(b, c));
                        n.normalise(n);
                        originalNormal.setX(n.x);
                        originalNormal.setY(n.y);
                        originalNormal.setZ(n.z);
                        BigDecimal epsilon = new BigDecimal("0.001"); //$NON-NLS-1$
                        for (Vertex vertex : m2) {
                            Vector3d vp = new Vector3d(vertex);
                            if (Vector3d.dotP(Vector3d.sub(pOrigin, vp), n).abs().compareTo(epsilon) > 0) return;
                        }

                        if (monitor.isCanceled()) {
                            originalSelectionLines.clear();
                        }
                    }
                    finally
                    {
                        monitor.done();
                    }
                }
            });
        }
        catch (InvocationTargetException ite) {
            NLogger.error(VM12IntersectorAndIsecalc.class, ite);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new LDPartEditorException(ie);
        }

        if (originalSelectionLines.isEmpty()) return;
        clearSelection();

        // Calculate intersecting lines, if needed.
        {
            List<List<Vector3dd>> linesToRemove = new ArrayList<>();
            List<List<Vector3dd>> newLines = new ArrayList<>();
            for (Iterator<List<Vector3dd>> iterator = linesToParse.iterator(); iterator.hasNext();) {
                List<Vector3dd> line = iterator.next();
                List<Vector3d> intersect = new ArrayList<>();
                for (List<Vector3dd> line2 : linesToParse) {
                    if (line2 != line) {
                        SortedSet<Vector3dd> allVertices = new TreeSet<>();
                        for(int l = 0; l < 2; l++) {
                            allVertices.add(line.get(l));
                            allVertices.add(line2.get(l));
                        }
                        if (allVertices.size() == 4) {
                            Vector3d ip = intersectLineLineSegmentUnidirectional2(line.get(0), line.get(1), line2.get(0), line2.get(1));
                            if (ip != null) {
                                intersect.add(ip);
                            }
                        }
                    }
                }
                if (!intersect.isEmpty()) {
                    SortedMap<BigDecimal, Vector3d> linePoints = new TreeMap<>();
                    Vector3d start = line.get(0);
                    Vector3d end = line.get(1);
                    for (Vector3d v : intersect) {
                        BigDecimal dist = Vector3d.manhattan(v, start);
                        linePoints.put(dist, v);
                    }
                    BigDecimal dist = Vector3d.manhattan(end, start);
                    linePoints.put(dist, end);

                    for (Vector3d point : linePoints.values()) {
                        end = point;
                        List<Vector3dd> newLine = new ArrayList<>();
                        newLine.add(new Vector3dd(start));
                        newLine.add(new Vector3dd(end));
                        newLines.add(newLine);
                        start = end;
                    }
                    linesToRemove.add(line);
                }
            }
            linesToParse.removeAll(linesToRemove);
            linesToParse.addAll(newLines);
        }

        final List<Vector3dd> resultVertices = new ArrayList<>();
        final List<GColour> resultColours = new ArrayList<>();
        final List<Integer> resultIsLine = new ArrayList<>();

        final Set<List<Vector3dd>> colourLines2 = Collections.newSetFromMap(new ThreadsafeHashMap<>());
        final ThreadsafeHashMap<List<Vector3dd> , GColour> colours2 = new ThreadsafeHashMap<>();
        final Thread[] colourThreads = new Thread[chunks];

        // Spread coloured lines
        {

            try
            {
                new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
                {
                    @Override
                    public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                    {
                        try
                        {

                            if (!colourLines.isEmpty()) {

                                final List<Vector3dd> fixedVertices = new ArrayList<>();
                                final List<Vector3dd> colourVertices = new ArrayList<>();
                                final SortedMap<Vector3dd, GColour> vertexColour = new TreeMap<>();
                                {
                                    final SortedSet<Vector3dd> allVertices = new TreeSet<>();
                                    for (List<Vector3dd> l : linesToParse) {
                                        allVertices.add(l.get(0).round());
                                        allVertices.add(l.get(1).round());
                                    }
                                    for (List<Vector3dd> l : colourLines) {
                                        Vector3dd vc1 = l.get(0).round();
                                        Vector3dd vc2 = l.get(1).round();
                                        if (!vertexColour.containsKey(vc1)) {
                                            vertexColour.put(vc1, colours.get(l));
                                        } else {
                                            GColour gc = vertexColour.get(vc1);
                                            GColour gc2 = colours.get(l);
                                            if (gc.getColourNumber() != gc2.getColourNumber()) vertexColour.remove(vc1);
                                        }
                                        if (!vertexColour.containsKey(vc2)) {
                                            vertexColour.put(vc2, colours.get(l));
                                        } else {
                                            GColour gc = vertexColour.get(vc2);
                                            GColour gc2 = colours.get(l);
                                            if (gc.getColourNumber() != gc2.getColourNumber()) vertexColour.remove(vc2);
                                        }
                                        colourVertices.add(vc1);
                                        colourVertices.add(vc2);
                                    }
                                    fixedVertices.addAll(allVertices);
                                }

                                final List<List<Vector3dd>> fixedLinesToParse = new ArrayList<>();
                                fixedLinesToParse.addAll(linesToParse);

                                final int vc = colourVertices.size();
                                final int vc2 = fixedVertices.size();

                                final AtomicInteger counter2 = new AtomicInteger(0);

                                for (int j = 0; j < chunks; ++j) {
                                    final int[] start = new int[] { j };
                                    colourThreads[j] = new Thread(() -> {
                                        int counter = start[0];
                                        for (int i = 0; i < vc; i++) {
                                            if (counter == 0) {
                                                counter = chunks;
                                                counter2.incrementAndGet();
                                                if (monitor.isCanceled()) {
                                                    return;
                                                }
                                                Vector3dd v1 = colourVertices.get(i);
                                                for (int vi = 0; vi < vc2; vi++) {
                                                    boolean intersect = false;
                                                    Vector3dd v2 = fixedVertices.get(vi);
                                                    Vector3d sp = Vector3d.sub(v2, v1);
                                                    Vector3d dir = new Vector3d();
                                                    BigDecimal len = sp.normalise(dir);
                                                    int lc = fixedLinesToParse.size();
                                                    for (int k = 0; k < lc; k++) {
                                                        List<Vector3dd> l = fixedLinesToParse.get(k);
                                                        Vector3dd v3 = l.get(0);
                                                        Vector3dd v4 = l.get(1);
                                                        if (!v1.equals(v3) && !v1.equals(v4) && !v2.equals(v3) && !v2.equals(v4) && intersectLineLineSegmentUnidirectionalFast(v1, sp, dir, len, v3, v4)) {
                                                            intersect = true;
                                                            break;
                                                        }
                                                    }
                                                    if (intersect) {
                                                        continue;
                                                    } else {
                                                        BigDecimal dist = Vector3d.manhattan(v1, v2);
                                                        if (dist.compareTo(minDist) > 0) {
                                                            if (vertexColour.containsKey(v1) && vertexColour.get(v1) != null) {
                                                                List<Vector3dd> nl = new ArrayList<>();
                                                                nl.add(v1);
                                                                nl.add(v2);
                                                                colours2.put(nl, vertexColour.get(v1));
                                                                colourLines2.add(nl);
                                                            } else if (vertexColour.containsKey(v2) && vertexColour.get(v2) != null) {
                                                                List<Vector3dd> nl = new ArrayList<>();
                                                                nl.add(v1);
                                                                nl.add(v2);
                                                                colours2.put(nl, vertexColour.get(v2));
                                                                colourLines2.add(nl);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            counter -= 1;
                                        }
                                    });
                                    colourThreads[j].start();
                                }
                            }
                        } finally {
                            monitor.done();
                        }
                    }
                });
            }
            catch (InvocationTargetException ite) {
                NLogger.error(VM12IntersectorAndIsecalc.class, ite);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new LDPartEditorException(ie);
            }
        }

        final List<Vector3dd> fixedVertices = new ArrayList<>();
        final List<Vector3dh> fixedVertices2 = new ArrayList<>();

        {
            final SortedSet<Vector3dd> allVertices = new TreeSet<>();
            for (List<Vector3dd> l : linesToParse) {
                allVertices.add(l.get(0).round());
                allVertices.add(l.get(1).round());
            }
            fixedVertices.addAll(allVertices);
        }

        if (!linesToParse.isEmpty()) {

            final ThreadsafeHashMap<Vector3dh, HashSet<Vector3dh>> neighbours = new ThreadsafeHashMap<>();
            try
            {
                new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
                {
                    @Override
                    public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                    {
                        try
                        {
                            monitor.beginTask(I18n.VM_LINES_2_PATTERN, IProgressMonitor.UNKNOWN);

                            final Thread[] threads = new Thread[1];

                            {
                                SortedMap<Vector3dd, Vector3dh> hashedRelation = new TreeMap<>();
                                for (Vector3dd v : fixedVertices) {
                                    Vector3dh vh;
                                    if (hashedRelation.containsKey(v)) {
                                        vh = hashedRelation.get(v);
                                    } else {
                                        vh = new Vector3dh(v);
                                        hashedRelation.put(v, vh);
                                    }

                                    fixedVertices2.add(vh);
                                }
                                for (List<Vector3dd> l : linesToParse) {

                                    Vector3dd v1nh = l.get(0).round();
                                    Vector3dd v2nh = l.get(1).round();

                                    Vector3dh v1;
                                    Vector3dh v2;

                                    if (hashedRelation.containsKey(v1nh)) {
                                        v1 = hashedRelation.get(v1nh);
                                    } else {
                                        v1 = new Vector3dh(v1nh);
                                        hashedRelation.put(v1nh, v1);
                                    }

                                    if (hashedRelation.containsKey(v2nh)) {
                                        v2 = hashedRelation.get(v2nh);
                                    } else {
                                        v2 = new Vector3dh(v2nh);
                                        hashedRelation.put(v2nh, v2);
                                    }

                                    List<Vector3dh> newline = new ArrayList<>();
                                    newline.add(v1);
                                    newline.add(v2);
                                    linesToParseHashed.add(newline);

                                }
                            }

                            final int vc = fixedVertices2.size();
                            final String vertCount = "/" + vc + ")"; //$NON-NLS-1$ //$NON-NLS-2$

                            threads[0] = new Thread(() -> {

                                for (int i = 0; i < vc; i++) {

                                    Object[] messageArguments = {i, vertCount};
                                    MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                                    formatter.setLocale(MyLanguage.getLocale());
                                    formatter.applyPattern(I18n.VM_DETECT_NEW_EDGES);

                                    monitor.subTask(formatter.format(messageArguments));

                                    if (monitor.isCanceled()) {
                                        break;
                                    }

                                    Vector3dh v1 = fixedVertices2.get(i);
                                    for (int j = i + 1; j < vc; j++) {
                                        boolean intersect = false;
                                        Vector3dh v2 = fixedVertices2.get(j);

                                        Vector3d sp = Vector3d.sub(v2, v1);
                                        Vector3d dir = new Vector3d();
                                        BigDecimal len = sp.normalise(dir);
                                        Iterator<List<Vector3dh>> li = linesToParseHashed.iterator();
                                        while (li.hasNext()) {
                                            List<Vector3dh> l = li.next();
                                            Vector3dh v3 = l.get(0);
                                            Vector3dh v4 = l.get(1);
                                            if (!v1.equals(v3) && !v1.equals(v4) && !v2.equals(v3) && !v2.equals(v4)
                                                    && intersectLineLineSegmentUnidirectionalFast(v1, sp, dir, len, v3,  v4)) {
                                                intersect = true;
                                                break;
                                            }
                                        }
                                        if (!intersect) {
                                            BigDecimal dist = Vector3d.manhattan(v1, v2);
                                            if (dist.compareTo(minDist) > 0) {
                                                List<Vector3dh> nl = new ArrayList<>();
                                                nl.add(v1);
                                                nl.add(v2);
                                                linesToParseHashed.add(nl);
                                            }
                                        }
                                    }
                                }
                            });
                            threads[0].start();
                            boolean isRunning = true;
                            while (isRunning) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException ie) {
                                    Thread.currentThread().interrupt();
                                    throw new LDPartEditorException(ie);
                                }
                                isRunning = false;
                                if (threads[0].isAlive())
                                    isRunning = true;
                            }
                            if (!colourLines.isEmpty()) {
                                isRunning = true;
                                while (isRunning) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException ie) {
                                        Thread.currentThread().interrupt();
                                        throw new LDPartEditorException(ie);
                                    }
                                    isRunning = false;
                                    for (Thread thread : colourThreads) {
                                        if (thread.isAlive())
                                            isRunning = true;
                                    }
                                }
                            }
                            if (monitor.isCanceled()) {
                                selectedLines.addAll(originalSelectionLines);
                                selectedTriangles.addAll(originalSelectionTriangles);
                                selectedQuads.addAll(originalSelectionQuads);
                                selectedData.addAll(originalSelectionTriangles);
                                selectedData.addAll(originalSelectionQuads);
                                selectedData.addAll(originalSelectionLines);
                                originalSelectionLines.clear();
                            } else {
                                colourLines.addAll(colourLines2);
                                colours.putAll(colours2);
                                linesToParse.clear();
                                fixedVertices2.clear();
                                for (List<Vector3dh> l : linesToParseHashed) {
                                    List<Vector3dd> nl = new ArrayList<>();
                                    nl.add(new Vector3dd(l.get(0)));
                                    nl.add(new Vector3dd(l.get(1)));
                                    linesToParse.add(nl);
                                }
                                linesToParseHashed.clear();
                            }
                        } finally {
                            monitor.done();
                        }
                    }
                });
            }
            catch (InvocationTargetException ite) {
                NLogger.error(VM12IntersectorAndIsecalc.class, ite);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new LDPartEditorException(ie);
            }

            if (originalSelectionLines.isEmpty()) return;

            int lc = linesToParse.size();
            {
                int removed = 0;
                for (int i = 0; i + removed < lc; i++) {
                    for (int j = i + 1; j + removed < lc; j++) {
                        SortedSet<Vector3dd> allVertices = new TreeSet<>();
                        for(int l = 0; l < 2; l++) {
                            allVertices.add(linesToParse.get(i).get(l));
                            allVertices.add(linesToParse.get(j).get(l));
                        }
                        if (allVertices.size() == 2) {
                            removed += 1;
                            linesToParse.remove(j);
                        }
                    }
                }

                lc = linesToParse.size();

                removed = 0;
                for (int i = 0; i + removed < lc; i++) {
                    SortedSet<Vector3dd> allVertices = new TreeSet<>();
                    allVertices.add(linesToParse.get(i).get(0));
                    allVertices.add(linesToParse.get(i).get(1));
                    if (allVertices.size() == 1) {
                        removed += 1;
                        linesToParse.remove(i);
                    }
                }

                lc = linesToParse.size();

                Set<Vector3dh> m1 = new HashSet<>();
                Set<Vector3dh> m2 = new HashSet<>();
                Set<Vector3dh> m3 = new HashSet<>();
                SortedMap<Vector3dd, Vector3dh> hashedRelation = new TreeMap<>();
                for (int i = 0; i < lc; i++) {
                    Vector3dd v1nh = linesToParse.get(i).get(0).round();
                    Vector3dd v2nh = linesToParse.get(i).get(1).round();

                    Vector3dh v1;
                    Vector3dh v2;

                    if (hashedRelation.containsKey(v1nh)) {
                        v1 = hashedRelation.get(v1nh);
                    } else {
                        v1 = new Vector3dh(v1nh);
                        hashedRelation.put(v1nh, v1);
                    }

                    if (hashedRelation.containsKey(v2nh)) {
                        v2 = hashedRelation.get(v2nh);
                    } else {
                        v2 = new Vector3dh(v2nh);
                        hashedRelation.put(v2nh, v2);
                    }

                    List<Vector3dh> newline = new ArrayList<>();
                    newline.add(v1);
                    newline.add(v2);
                    linesToParseHashed.add(newline);

                    if (neighbours.containsKey(v1)) {
                        neighbours.get(v1).add(v2);
                    } else {
                        neighbours.put(v1, new HashSet<>());
                        neighbours.get(v1).add(v2);
                    }
                    if (neighbours.containsKey(v2)) {
                        neighbours.get(v2).add(v1);
                    } else {
                        neighbours.put(v2, new HashSet<>());
                        neighbours.get(v2).add(v1);
                    }
                    if (m1.contains(v1)) {
                        if (m2.contains(v1)) {
                            if (!m3.contains(v1)) {
                                m3.add(v1);
                            }
                        } else {
                            m2.add(v1);
                        }
                    } else {
                        m1.add(v1);
                    }
                    if (m1.contains(v2)) {
                        if (m2.contains(v2)) {
                            if (!m3.contains(v2)) {
                                m3.add(v2);
                            }
                        } else {
                            m2.add(v2);
                        }
                    } else {
                        m1.add(v2);
                    }
                }
                for (Vector3dd v : fixedVertices) {
                    fixedVertices2.add(hashedRelation.get(v));
                }
                m2.removeAll(m3);
                fixedVertices2.removeAll(m2);
            }

            try
            {
                new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
                {
                    @Override
                    public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                    {
                        try
                        {
                            monitor.beginTask(I18n.VM_LINES_2_PATTERN, IProgressMonitor.UNKNOWN);

                            final int lc = linesToParseHashed.size();

                            final Thread[] threads = new Thread[chunks];

                            final String vertCount = "/" + lc + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                            final AtomicInteger counter2 = new AtomicInteger(0);

                            final Lock rlock = new ReentrantLock(true);

                            for (int t = 0; t < chunks; ++t) {
                                final int[] start = new int[] { t };
                                threads[t] = new Thread(() -> {
                                    int counter = start[0];
                                    Set<Vector3dh> allVertices = new HashSet<>();
                                    Vector3d normal = null;
                                    for (int i = 0; i < lc; i++) {
                                        if (counter == 0) {
                                            counter = chunks;

                                            Object[] messageArguments = {counter2.toString(), vertCount};
                                            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                                            formatter.setLocale(MyLanguage.getLocale());
                                            formatter.applyPattern(I18n.VM_TRIANGULATE);

                                            monitor.subTask(formatter.format(messageArguments));
                                            counter2.incrementAndGet();
                                            if (monitor.isCanceled()) {
                                                return;
                                            }
                                            for (int j = i + 1; j < lc; j++) {
                                                for (int k = j + 1; k < lc; k++) {
                                                    for(int l = 0; l < 2; l++) {
                                                        allVertices.add(linesToParseHashed.get(i).get(l));
                                                        allVertices.add(linesToParseHashed.get(j).get(l));
                                                        allVertices.add(linesToParseHashed.get(k).get(l));
                                                    }
                                                    if (allVertices.size() == 3) {
                                                        Vector3dh[] triVerts = new Vector3dh[3];
                                                        int l = 0;
                                                        for (Vector3dh v : allVertices) {
                                                            triVerts[l] = v;
                                                            l++;
                                                        }
                                                        allVertices.clear();
                                                        boolean isInsideTriangle = false;
                                                        if (normal == null) {
                                                            normal = Vector3d.cross(Vector3d.sub(triVerts[2], triVerts[0]), Vector3d.sub(triVerts[1], triVerts[0]));
                                                            normal.normalise(normal);
                                                        }
                                                        for (Vector3dh fixed : fixedVertices2) {
                                                            if (fixed.equals(triVerts[0])) continue;
                                                            if (fixed.equals(triVerts[1])) continue;
                                                            if (fixed.equals(triVerts[2])) continue;
                                                            Set<Vector3dh> n1 = neighbours.get(triVerts[0]);
                                                            Set<Vector3dh> n2 = neighbours.get(triVerts[1]);
                                                            Set<Vector3dh> n3 = neighbours.get(triVerts[2]);
                                                            int nc = 0;
                                                            if (n1.contains(fixed)) nc += 1;
                                                            if (n2.contains(fixed)) nc += 1;
                                                            if (n3.contains(fixed)) nc += 1;
                                                            if (nc > 1 && intersectRayTriangle(fixed, normal, triVerts[0], triVerts[1], triVerts[2])) {
                                                                isInsideTriangle = true;
                                                                break;
                                                            }
                                                        }
                                                        if (isInsideTriangle) continue;

                                                        // Check collinearity
                                                        {
                                                            double angle;
                                                            Vector3d vertexA = new Vector3d(triVerts[0]);
                                                            Vector3d vertexB = new Vector3d(triVerts[1]);
                                                            Vector3d vertexC = new Vector3d(triVerts[2]);
                                                            Vector3d a = new Vector3d();
                                                            Vector3d b = new Vector3d();
                                                            Vector3d c = new Vector3d();
                                                            Vector3d.sub(vertexB, vertexA, a);
                                                            Vector3d.sub(vertexC, vertexB, b);
                                                            Vector3d.sub(vertexC, vertexA, c);

                                                            angle = Vector3d.angle(a, c);
                                                            double sumAngle = angle;
                                                            if (angle < Threshold.COLLINEAR_ANGLE_MINIMUM || angle > Threshold.COLLINEAR_ANGLE_MAXIMUM) {
                                                                continue;
                                                            }

                                                            a.negate();
                                                            angle = Vector3d.angle(a, b);
                                                            sumAngle = sumAngle + angle;
                                                            if (angle < Threshold.COLLINEAR_ANGLE_MINIMUM || angle > Threshold.COLLINEAR_ANGLE_MAXIMUM) {
                                                                continue;
                                                            }

                                                            angle = 180.0 - sumAngle;
                                                            if (angle < Threshold.COLLINEAR_ANGLE_MINIMUM || angle > Threshold.COLLINEAR_ANGLE_MAXIMUM) {
                                                                continue;
                                                            }
                                                        }

                                                        {
                                                            Set<List<Vector3dd>> threeLines = new HashSet<>();
                                                            threeLines.add(linesToParse.get(i));
                                                            threeLines.add(linesToParse.get(j));
                                                            threeLines.add(linesToParse.get(k));
                                                            List<Vector3dd> intersected = null;
                                                            for (Iterator<List<Vector3dd>> iterator = threeLines.iterator(); iterator.hasNext();) {
                                                                List<Vector3dd> line = iterator.next();
                                                                Vector3dd v1 = line.get(0);
                                                                Vector3dd v2 = line.get(1);
                                                                Vector3d sp = Vector3d.sub(v2, v1);
                                                                Vector3d dir = new Vector3d();
                                                                BigDecimal len = sp.normalise(dir);
                                                                for (List<Vector3dd> line2 : colourLines) {
                                                                    if (line2 != line) {
                                                                        SortedSet<Vector3dd> allVertices1 = new TreeSet<>();
                                                                        for(int l1 = 0; l1 < 2; l1++) {
                                                                            allVertices1.add(line.get(l1));
                                                                            allVertices1.add(line2.get(l1));
                                                                        }
                                                                        if (allVertices1.size() == 4 && intersectLineLineSegmentUnidirectionalFast(v1, sp, dir, len, line2.get(0), line2.get(1))) {
                                                                            intersected = line2;
                                                                            break;
                                                                        }
                                                                    }
                                                                }
                                                                if (intersected != null) {
                                                                    break;
                                                                }
                                                            }

                                                            rlock.lock();
                                                            if (MathHelper.directionOfVectors(Vector3d.cross(Vector3d.sub(triVerts[2], triVerts[0]), Vector3d.sub(triVerts[1], triVerts[0])), originalNormal) == 1) {
                                                                resultVertices.add(triVerts[0]);
                                                                resultVertices.add(triVerts[1]);
                                                                resultVertices.add(triVerts[2]);
                                                            } else {
                                                                resultVertices.add(triVerts[0]);
                                                                resultVertices.add(triVerts[2]);
                                                                resultVertices.add(triVerts[1]);
                                                            }

                                                            if (intersected != null) {
                                                                resultColours.add(colours.get(intersected) != null ? colours.get(intersected) : LDConfig.getColour16());
                                                            } else {
                                                                resultColours.add(LDConfig.getColour16());
                                                            }
                                                            resultIsLine.add(0);
                                                            rlock.unlock();
                                                        }
                                                    } else {
                                                        allVertices.clear();
                                                    }
                                                }
                                            }
                                        }
                                        counter -= 1;
                                    }
                                });
                                threads[t].start();
                            }
                            boolean isRunning = true;
                            while (isRunning) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException ie) {
                                    Thread.currentThread().interrupt();
                                    throw new LDPartEditorException(ie);
                                }
                                isRunning = false;
                                for (Thread thread : threads) {
                                    if (thread.isAlive())
                                        isRunning = true;
                                }
                            }
                            if (monitor.isCanceled()) {
                                selectedLines.addAll(originalSelectionLines);
                                selectedTriangles.addAll(originalSelectionTriangles);
                                selectedQuads.addAll(originalSelectionQuads);
                                selectedData.addAll(originalSelectionTriangles);
                                selectedData.addAll(originalSelectionQuads);
                                selectedData.addAll(originalSelectionLines);
                                originalSelectionLines.clear();
                            }
                        } finally {
                            monitor.done();
                        }
                    }
                });
            }
            catch (InvocationTargetException ite) {
                NLogger.error(VM12IntersectorAndIsecalc.class, ite);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new LDPartEditorException(ie);
            }

            if (originalSelectionLines.isEmpty()) return;

            newTriangles.addAll(MathHelper.triangulatePointGroups(resultColours, resultVertices, resultIsLine, View.DUMMY_REFERENCE, linkedDatFile));

            NLogger.debug(getClass(), "Check for identical vertices and collinearity."); //$NON-NLS-1$
            final Set<GData3> trisToDelete2 = new HashSet<>();
            {
                for (GData3 g3 : newTriangles) {
                    Vertex[] verts = triangles.get(g3);
                    SortedSet<Vertex> verts2 = new TreeSet<>();
                    verts2.addAll(Arrays.asList(verts));
                    if (verts2.size() < 3 || g3.isCollinear()) {
                        trisToDelete2.add(g3);
                    }
                }
            }

            // Append the new data
            for (GData3 tri : newTriangles) {
                linkedDatFile.addToTailOrInsertAfterCursor(tri);
            }

            NLogger.debug(getClass(), "Delete new, but invalid objects."); //$NON-NLS-1$

            newTriangles.removeAll(trisToDelete2);
            selectedTriangles.addAll(trisToDelete2);
            selectedData.addAll(selectedTriangles);
            delete(false, false);

            // Round to 6 decimal places

            selectedTriangles.addAll(newTriangles);
            selectedData.addAll(selectedTriangles);

            NLogger.debug(getClass(), "Round."); //$NON-NLS-1$
            roundSelection(6, 10, true, false, true, true, true);

            // Fill surfaces
            NLogger.debug(getClass(), "Colour fill."); //$NON-NLS-1$

            newTriangles.clear();
            newTriangles.addAll(selectedTriangles);
            clearSelection();

            final VertexManager vm = linkedDatFile.getVertexManager();
            final SelectorSettings ss = new SelectorSettings();
            ss.setScope(SelectorSettings.CONNECTED);
            ss.setEdgeStop(true);
            ss.setCondlines(false);
            ss.setLines(false);
            ss.setVertices(false);

            colouredTriangles.addAll(newTriangles);

            for (Iterator<GData3> it = newTriangles.iterator(); it.hasNext();) {
                final GData3 tri = it.next();

                // Skip uncoloured or subfile triangles
                if (tri.colourNumber == 16 || !lineLinkedToVertices.containsKey(tri)) {
                    continue;
                }

                clearSelection();
                selectedTriangles.add(tri);
                selectorSilent(ss);

                // Remove the old selected triangles from the set of new triangles
                if (newTriangles.removeAll(selectedTriangles)) {
                    // Reset the iterator
                    it = newTriangles.iterator();
                }

                // Don't want to colour already coloured triangles
                selectedTriangles.removeIf(g -> g.colourNumber != 16);

                // Change the colour
                vm.colourChangeSelection(tri.colourNumber, tri.r, tri.g, tri.b, tri.a, false);
                // Add the new coloured triangles to the final selection
                colouredTriangles.addAll(selectedTriangles);
            }

            // Cleanup coloured triangle selection, remove subfile/deleted content
            colouredTriangles.removeIf(g -> !lineLinkedToVertices.containsKey(g));

            // Restore selection
            clearSelection();
            selectedTriangles.addAll(colouredTriangles);
            selectedData.addAll(selectedTriangles);

            // Prepare triangle set for the FlipTriangleOptimizer
            final List<GData3> unbindTriangles = new ArrayList<>();
            for (final GData3 tri : colouredTriangles) {
                final Vertex v1 = new Vertex(tri.x1p, tri.y1p, tri.z1p);
                final Vertex v2 = new Vertex(tri.x2p, tri.y2p, tri.z2p);
                final Vertex v3 = new Vertex(tri.x3p, tri.y3p, tri.z3p);
                unbindTriangles.add(new GData3(v1, v2, v3, tri.parent, new GColour(tri.colourNumber, tri.r, tri.g, tri.b, tri.a), true));
            }

            delete(false, false);

            final List<GData3> optimization = new ArrayList<>();
            final Random rnd = new Random(12345L);
            final Map<GData3, Map<GData3, Boolean>> flipCache = new HashMap<>();
            optimization.addAll(unbindTriangles);

            try
            {
                new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
                {
                    @Override
                    public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                    {
                        try
                        {
                            monitor.beginTask(I18n.VM_LINES_2_PATTERN, IProgressMonitor.UNKNOWN);

                            while (FlipTriangleOptimizer.optimize(rnd, unbindTriangles, optimization, flipCache) && !monitor.isCanceled()) {
                                unbindTriangles.clear();
                                unbindTriangles.addAll(optimization);
                            }
                        } finally {
                            monitor.done();
                        }
                    }
                });
            }
            catch (InvocationTargetException ite) {
                NLogger.error(VM12IntersectorAndIsecalc.class, ite);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new LDPartEditorException(ie);
            }

            // Restore selection
            clearSelection();
            colouredTriangles.clear();
            for (final GData3 tri : optimization) {
                final Vertex v1 = new Vertex(tri.x1p, tri.y1p, tri.z1p);
                final Vertex v2 = new Vertex(tri.x2p, tri.y2p, tri.z2p);
                final Vertex v3 = new Vertex(tri.x3p, tri.y3p, tri.z3p);
                final GData3 newTri = new GData3(tri.colourNumber, tri.r, tri.g, tri.b, tri.a, v1, v2, v3, tri.parent, linkedDatFile, true);
                colouredTriangles.add(newTri);
                linkedDatFile.addToTailOrInsertAfterCursor(newTri);
            }

            selectedTriangles.addAll(colouredTriangles);
            selectedData.addAll(selectedTriangles);

            setModified(true, true);

            NLogger.debug(getClass(), "Done."); //$NON-NLS-1$

            validateState();
        }
    }

    private boolean intersectLineLineSegmentUnidirectionalFast(Vector3dd p, Vector3d sp, Vector3d dir, BigDecimal len, Vector3dd q, Vector3dd q2) {

        Vector3d sq = Vector3d.sub(q2, q);

        Vector3d cross = Vector3d.cross(sq, sp);
        Vector3d c = Vector3d.add(cross, q);
        Vector3d d = Vector3d.sub(q, cross);

        return intersectLineTriangleSuperFast(p, d, q2, c, dir, len);

    }

    private boolean intersectRayTriangle(Vector3dd orig, Vector3d dir, Vector3dd vert0, Vector3dd vert1, Vector3dd vert2) {
        BigDecimal diskr;
        BigDecimal invDiskr;
        Vector3d corner1 = Vector3d.sub(vert1, vert0);
        Vector3d corner2 = Vector3d.sub(vert2, vert0);
        Vector3d pvec = Vector3d.cross(dir, corner2);
        diskr = Vector3d.dotP(corner1, pvec);
        if (diskr.abs().compareTo(TOLERANCER) < 0)
            return false;
        invDiskr = BigDecimal.ONE.divide(diskr, Threshold.MC);
        Vector3d tvec = Vector3d.sub(orig, vert0);
        BigDecimal u = Vector3d.dotP(tvec, pvec).multiply(invDiskr);
        if (u.compareTo(ZEROTR) < 0 || u.compareTo(ONETR) > 0)
            return false;
        Vector3d qvec = Vector3d.cross(tvec, corner1);
        BigDecimal v = Vector3d.dotP(dir, qvec).multiply(invDiskr);
        return !(v.compareTo(ZEROTR) < 0 || u.add(v).compareTo(ONETR) > 0);
    }

    private boolean intersectLineTriangleSuperFast(Vector3dd q, Vector3d d, Vector3dd p2, Vector3d c, Vector3d dir, BigDecimal len) {
        BigDecimal diskr;
        BigDecimal invDiskr;
        Vector3d vert0 = d;
        Vector3d vert1 = p2;
        Vector3d vert2 = c;
        Vector3d corner1 = Vector3d.sub(vert1, vert0);
        Vector3d corner2 = Vector3d.sub(vert2, vert0);
        Vector3d orig = q;
        Vector3d pvec = Vector3d.cross(dir, corner2);
        diskr = Vector3d.dotP(corner1, pvec);
        if (diskr.abs().compareTo(TOLERANCE) <= 0)
            return false;
        invDiskr = BigDecimal.ONE.divide(diskr, Threshold.MC);
        Vector3d tvec = Vector3d.sub(orig, vert0);
        BigDecimal u = Vector3d.dotP(tvec, pvec).multiply(invDiskr);
        if (u.compareTo(ZEROT) < 0 || u.compareTo(ONET) > 0)
            return false;
        Vector3d qvec = Vector3d.cross(tvec, corner1);
        BigDecimal v = Vector3d.dotP(dir, qvec).multiply(invDiskr);
        if (v.compareTo(ZEROT) < 0 || u.add(v).compareTo(ONET) > 0)
            return false;
        BigDecimal t = Vector3d.dotP(corner2, qvec).multiply(invDiskr);
        return !(t.compareTo(ZEROT) < 0 || t.compareTo(len.add(TOLERANCE)) > 0);
    }
}
