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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.LDPartEditorException;
import org.nschmidt.ldparteditor.helpers.composite3d.PerspectiveCalculator;
import org.nschmidt.ldparteditor.helpers.composite3d.RectifierSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.SlicerProSettings;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.helpers.math.Rational;
import org.nschmidt.ldparteditor.helpers.math.RationalMatrix;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.helpers.math.Vector3r;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;

class VM08SlicerPro extends VM07PathTruder {

    protected VM08SlicerPro(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public void slicerpro(final SlicerProSettings ss) {
        if (linkedDatFile.isReadOnly()) return;
        Composite3D c3d =  linkedDatFile.getLastSelectedComposite();
        NLogger.debug(getClass(), "SlicerPro2 - (C) Nils Schmidt 2015"); //$NON-NLS-1$
        NLogger.debug(getClass(), "======================"); //$NON-NLS-1$
        if (c3d != null) {

            final int[] isCancelled = new int[]{0};

            final Set<GData2> debugLines = new HashSet<>();

            final Set<GData3> trisToDelete = new HashSet<>();
            final Set<GData4> quadsToDelete = new HashSet<>();

            Vector4f dir4f = new Vector4f(c3d.getGenerator()[2]);
            final Vector3r dir = new Vector3r(dir4f);
            final Vector3r dirN = new Vector3r(dir);
            dirN.negate();

            // NLogger.debug(getClass(), "Ray Direction:" + dir.toString()); //$NON-NLS-1$
            // NLogger.debug(getClass(), "Neg. Ray Direction:" + dirN.toString()); //$NON-NLS-1$

            final Matrix4f vport = c3d.getViewport();
            final RationalMatrix m = new RationalMatrix(vport);
            final RationalMatrix minv = m.invert();
            final PerspectiveCalculator pc = c3d.getPerspectiveCalculator();

            // NLogger.debug(getClass(), "Viewport Matrix Inv.:\n" + minv.toString()); //$NON-NLS-1$

            NLogger.debug(getClass(), "Get target surfaces to parse."); //$NON-NLS-1$

            final Set<GData> targetSurfs = new HashSet<>();
            {
                Set<GData3> tris = triangles.keySet();
                for (GData3 tri : tris) {
                    if (lineLinkedToVertices.containsKey(tri) && !hiddenData.contains(tri)) {
                        targetSurfs.add(tri);
                    }
                }
            }
            {
                Set<GData4> qs = quads.keySet();
                for (GData4 quad : qs) {
                    if (lineLinkedToVertices.containsKey(quad) && !hiddenData.contains(quad)) {
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

            targetSurfs.removeAll(selectedTriangles);
            targetSurfs.removeAll(selectedQuads);

            final List<GData> originSurfs = new ArrayList<>();
            originSurfs.addAll(selectedTriangles);
            originSurfs.addAll(selectedQuads);

            clearSelection();

            final List<List<IntersectionInfo>> intersections = new ArrayList<>();
            final Set<GData3> newTriangles = Collections.newSetFromMap(new ThreadsafeHashMap<>());

            try
            {
                new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
                {
                    @Override
                    public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                    {
                        try
                        {
                            monitor.beginTask(I18n.VM_SLICERPRO, IProgressMonitor.UNKNOWN);

                            {

                                final Set<List<IntersectionInfo>> intersectionSet = Collections.newSetFromMap(new ThreadsafeHashMap<>());

                                final int iterations = originSurfs.size();
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
                                            GData o = originSurfs.get(k);
                                            /* Check if the monitor has been canceled */
                                            if (monitor.isCanceled()) {
                                                isCancelled[0] = 1;
                                                return;
                                            }
                                            counter2.incrementAndGet();
                                            for (GData t : targetSurfs) {
                                                List<IntersectionInfo> ii = getIntersectionInfo(o, t, dir, dirN, m, minv, pc, ss);
                                                if (!ii.isEmpty()) {
                                                    intersectionSet.add(ii);
                                                    switch (t.type()) {
                                                    case 3:
                                                        trisToDelete.add((GData3) t);
                                                        break;
                                                    case 4:
                                                        quadsToDelete.add((GData4) t);
                                                        break;
                                                    default:
                                                        break;
                                                    }
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

                            NLogger.debug(getClass(), "Create new faces."); //$NON-NLS-1$

                            {
                                final int iterations = intersections.size();
                                final int chunks = View.NUM_CORES;
                                final Thread[] threads = new Thread[chunks];


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
                                            List<IntersectionInfo> ii = intersections.get(k);
                                            if (monitor.isCanceled()) {
                                                isCancelled[0] = 2;
                                                return;
                                            }
                                            counter2.incrementAndGet();
                                            for (IntersectionInfo info : ii) {
                                                final int pointsToTriangulate = info.getAllVertices().size();
                                                final List<Vector3d> av = info.getAllVertices();

                                                final float R;
                                                final float G;
                                                final float B;
                                                final float A;
                                                final int CN;
                                                GData origin2 = info.getOrigin();
                                                if (origin2.type() == 3) {
                                                    GData3 origin = (GData3) origin2;
                                                    CN = origin.colourNumber;
                                                    R = origin.r;
                                                    G = origin.g;
                                                    B = origin.b;
                                                    A = origin.a;
                                                } else {
                                                    GData4 origin = (GData4) origin2;
                                                    CN = origin.colourNumber;
                                                    R = origin.r;
                                                    G = origin.g;
                                                    B = origin.b;
                                                    A = origin.a;
                                                }

                                                switch (pointsToTriangulate) {
                                                case 3:
                                                    newTriangles.add(new GData3(CN, R, G, B, A,
                                                            av.get(0).x, av.get(0).y, av.get(0).z,
                                                            av.get(1).x, av.get(1).y, av.get(1).z,
                                                            av.get(2).x, av.get(2).y, av.get(2).z,
                                                            View.DUMMY_REFERENCE, linkedDatFile, true));
                                                    break;
                                                case 4:
                                                case 5:
                                                case 6:
                                                case 7:
                                                    newTriangles.addAll(MathHelper.triangulateNPoints(CN, R, G, B, A, pointsToTriangulate, av, View.DUMMY_REFERENCE, linkedDatFile));
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
                            }

                        }
                        finally
                        {
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


            NLogger.debug(getClass(), "Check for identical vertices, invalid winding and collinearity."); //$NON-NLS-1$
            final Set<GData3> trisToDelete2 = new HashSet<>();
            {
                final Set<GData3> newTriangles2 = new HashSet<>();
                for (GData3 g3 : newTriangles) {
                    Vertex[] verts = triangles.get(g3);
                    SortedSet<Vertex> verts2 = new TreeSet<>();
                    verts2.addAll(Arrays.asList(verts));
                    if (verts2.size() < 3 || g3.isCollinear()) {
                        trisToDelete2.add(g3);
                    } else {
                        GData3 tri = checkNormal(g3, vport);
                        if (tri != null) {
                            trisToDelete2.add(g3);
                            newTriangles2.add(tri);
                        }
                    }
                }
                newTriangles.addAll(newTriangles2);
            }


            if (isCancelled[0] == 0) {
                NLogger.debug(getClass(), "Delete old target faces."); //$NON-NLS-1$

                selectedTriangles.addAll(trisToDelete);
                selectedQuads.addAll(quadsToDelete);
                selectedData.addAll(selectedTriangles);
                selectedData.addAll(selectedQuads);
                delete(false, false);
            } else {
                clearSelection();
            }

            // Append the triangles
            for (GData3 tri : newTriangles) {
                linkedDatFile.addToTailOrInsertAfterCursor(tri);
            }

            for (GData2 lin : debugLines) {
                linkedDatFile.addToTailOrInsertAfterCursor(lin);
            }

            NLogger.debug(getClass(), "Delete new, but invalid faces."); //$NON-NLS-1$

            newTriangles.removeAll(trisToDelete2);

            selectedTriangles.addAll(trisToDelete2);
            selectedData.addAll(selectedTriangles);
            delete(false, false);

            // Round to 6 decimal places

            selectedTriangles.addAll(newTriangles);
            selectedData.addAll(newTriangles);

            NLogger.debug(getClass(), "Round."); //$NON-NLS-1$
            roundSelection(6, 10, true, false, true, true, true);

            NLogger.debug(getClass(), "Rectify."); //$NON-NLS-1$
            RectifierSettings rs = new RectifierSettings();
            rs.setScope(1);
            rs.setNoBorderedQuadToRectConversation(true);
            rectify(rs, false, false);

            clearSelection();
            setModified(true, true);

            NLogger.debug(getClass(), "Done."); //$NON-NLS-1$

            validateState();

        } else {
            NLogger.debug(getClass(), "No 3D view selected. Cancel process."); //$NON-NLS-1$
        }
    }

    private boolean intersectRayTriangle(Vector3r origin2d, Vertex dir, Vector3r target2d, Vector3r target2d2, Vector3r target2d3, Vector3r ip) {
        Rational diskr;
        Rational invDiskr;
        Vector3r vert0 = new Vector3r(target2d);
        Vector3r vert1 = new Vector3r(target2d2);
        Vector3r vert2 = new Vector3r(target2d3);
        vert0.setZ(Rational.ONE);
        vert1.setZ(Rational.ONE);
        vert2.setZ(Rational.ONE);
        Vector3r corner1 = Vector3r.sub(vert1, vert0);
        Vector3r corner2 = Vector3r.sub(vert2, vert0);
        Vector3r dir2 = new Vector3r(dir);
        Vector3r orig = new Vector3r(origin2d);
        orig.setZ(new Rational(-1000));
        Vector3r pvec = Vector3r.cross(dir2, corner2);
        diskr = Vector3r.dot(corner1, pvec);
        if (diskr.abs().compareTo(Rational.ZERO) == 0)
            return false;
        invDiskr = Rational.ONE.divide(diskr);
        Vector3r tvec = Vector3r.sub(orig, vert0);
        Rational u = Vector3r.dot(tvec, pvec).multiply(invDiskr);
        if (u.compareTo(Rational.ZERO) < 0 || u.compareTo(Rational.ONE) > 0)
            return false;
        Vector3r qvec = Vector3r.cross(tvec, corner1);
        Rational v = Vector3r.dot(dir2, qvec).multiply(invDiskr);
        if (v.compareTo(Rational.ZERO) < 0 || u.add(v).compareTo(Rational.ONE) > 0)
            return false;
        Rational t = Vector3r.dot(corner2, qvec).multiply(invDiskr);
        ip.setX(orig.x.add(dir2.x.multiply(t)));
        ip.setY(orig.y.add(dir2.y.multiply(t)));
        ip.setZ(orig.z.add(dir2.z.multiply(t)));
        return true;
    }

    private IntersectionInfo getTriangleTriangleIntersection(Vector3r[] ov, Vector3r[] tv, GData origin, GData target, Vector3r dir, Vector3r dirN, RationalMatrix m, RationalMatrix minv, PerspectiveCalculator pc, SlicerProSettings ss) {

        List<Vector3r> insideTarget = new ArrayList<>();
        List<Vector3r> insideOrigin = new ArrayList<>();

        Vector3r[] target2D = new Vector3r[3];
        for(int i = 0; i < 3; i++) {
            target2D[i] = pc.getScreenCoordinatesFrom3D(new Vector3r(tv[i]), m);
        }
        Vector3r[] origin2D = new Vector3r[3];
        for(int i = 0; i < 3; i++) {
            origin2D[i] = pc.getScreenCoordinatesFrom3D(new Vector3r(ov[i]), m);
        }

        final Vertex dir0 = new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE);

        // How many points are inside the target?

        for(int i = 0; i < 3; i++) {
            Vector3r ip = new Vector3r();
            if (intersectRayTriangle(origin2D[i], dir0, target2D[0], target2D[1], target2D[2], ip)) {
                insideTarget.add(ip);
            }
        }

        // How many points are inside the origin?

        for(int i = 0; i < 3; i++) {
            Vector3r ip = new Vector3r();
            if (intersectRayTriangle(target2D[i], dir0, origin2D[0], origin2D[1], origin2D[2], ip)) {
                insideOrigin.add(ip);
            }
        }

        // Return if all points are inside the target
        if (insideTarget.size() == 3) {
            insideOrigin.clear();
            // Project points on the target plane
            List<Vector3r> iT = new ArrayList<>();
            {
                List<Vector3r> insideTarget2 = new ArrayList<>();
                insideTarget2.addAll(insideTarget);
                insideTarget.clear();
                for (Vector3r v : insideTarget2) {
                    Vector3r pv = new Vector3r();
                    v = pc.get3DCoordinatesFromScreen(v, minv);
                    projectRayOnTrianglePlane(new Vector3r(v.x, v.y, v.z) , dirN, tv[0], tv[1], tv[2], pv);
                    iT.add(pv);
                }
            }
            return new IntersectionInfo(target, origin, iT);
        }

        // Return if all points are inside the origin
        if (insideOrigin.size() == 3) {
            insideTarget.clear();
            // Project points on the target plane
            List<Vector3r> iT = new ArrayList<>();
            {
                List<Vector3r> insideOrigin2 = new ArrayList<>();
                insideOrigin2.addAll(insideOrigin);
                insideOrigin.clear();
                for (Vector3r v : insideOrigin2) {
                    Vector3r pv = new Vector3r();
                    v = pc.get3DCoordinatesFromScreen(v, minv);
                    projectRayOnTrianglePlane(new Vector3r(v.x, v.y, v.z) , dirN, tv[0], tv[1], tv[2], pv);
                    iT.add(pv);
                }
            }
            return new IntersectionInfo(target, origin, iT);
        }

        List<Vector3r> sideIntersections = new ArrayList<>();

        // Calculate line intersections
        {
            Vector3r li = new Vector3r();
            Vector3r li2 = new Vector3r();
            switch (intersectLineLineSegment2DExact(target2D[0], target2D[1], origin2D[0], origin2D[1], li, li2)) {
            case 2:
                sideIntersections.add(li2);
            case 1:
                sideIntersections.add(li);
            default:
                break;
            }
        }
        {
            Vector3r li = new Vector3r();
            Vector3r li2 = new Vector3r();
            switch  (intersectLineLineSegment2DExact(target2D[0], target2D[1], origin2D[1], origin2D[2], li, li2)) {
            case 2:
                sideIntersections.add(li2);
            case 1:
                sideIntersections.add(li);
            default:
            }
        }
        {
            Vector3r li = new Vector3r();
            Vector3r li2 = new Vector3r();
            switch  (intersectLineLineSegment2DExact(target2D[0], target2D[1], origin2D[2], origin2D[0], li, li2)) {
            case 2:
                sideIntersections.add(li2);
            case 1:
                sideIntersections.add(li);
            default:
            }
        }
        {
            Vector3r li = new Vector3r();
            Vector3r li2 = new Vector3r();
            switch  (intersectLineLineSegment2DExact(target2D[1], target2D[2], origin2D[0], origin2D[1], li, li2)) {
            case 2:
                sideIntersections.add(li2);
            case 1:
                sideIntersections.add(li);
            default:
            }
        }
        {
            Vector3r li = new Vector3r();
            Vector3r li2 = new Vector3r();
            switch (intersectLineLineSegment2DExact(target2D[1], target2D[2], origin2D[1], origin2D[2], li, li2)) {
            case 2:
                sideIntersections.add(li2);
            case 1:
                sideIntersections.add(li);
            default:
            }
        }
        {
            Vector3r li = new Vector3r();
            Vector3r li2 = new Vector3r();
            switch  (intersectLineLineSegment2DExact(target2D[1], target2D[2], origin2D[2], origin2D[0], li, li2)) {
            case 2:
                sideIntersections.add(li2);
            case 1:
                sideIntersections.add(li);
            default:
            }
        }
        {
            Vector3r li = new Vector3r();
            Vector3r li2 = new Vector3r();
            switch  (intersectLineLineSegment2DExact(target2D[2], target2D[0], origin2D[0], origin2D[1], li, li2)) {
            case 2:
                sideIntersections.add(li2);
            case 1:
                sideIntersections.add(li);
            default:
            }
        }
        {
            Vector3r li = new Vector3r();
            Vector3r li2 = new Vector3r();
            switch  (intersectLineLineSegment2DExact(target2D[2], target2D[0], origin2D[1], origin2D[2], li, li2)) {
            case 2:
                sideIntersections.add(li2);
            case 1:
                sideIntersections.add(li);
            default:
                break;
            }
        }
        {
            Vector3r li = new Vector3r();
            Vector3r li2 = new Vector3r();
            switch (intersectLineLineSegment2DExact(target2D[2], target2D[0], origin2D[2], origin2D[0], li, li2)) {
            case 2:
                sideIntersections.add(li2);
            case 1:
                sideIntersections.add(li);
            default:
            }
        }

        // Return null if NO intersection is found
        if (sideIntersections.isEmpty()) {
            return null;
        } else {

            // Check for identical vertices and remove them
            {

                List<Vector3r> allVertices = new ArrayList<>();

                for (Iterator<Vector3r> it = sideIntersections.iterator(); it.hasNext();) {
                    Vector3r v = it.next();
                    boolean notRemoved = true;
                    for (Vector3r v2 : allVertices) {
                        if (v.equals2d(v2)) {
                            it.remove();
                            notRemoved = false;
                            break;
                        }
                    }
                    if (notRemoved) {
                        allVertices.add(v);
                    }
                }
                for (Iterator<Vector3r> it = insideOrigin.iterator(); it.hasNext();) {
                    Vector3r v = it.next();
                    boolean notRemoved = true;
                    for (Vector3r v2 : allVertices) {
                        if (v.equals2d(v2)) {
                            it.remove();
                            notRemoved = false;
                            break;
                        }
                    }
                    if (notRemoved) {
                        allVertices.add(v);
                    }
                }
                for (Iterator<Vector3r> it = insideTarget.iterator(); it.hasNext();) {
                    Vector3r v = it.next();
                    boolean notRemoved = true;
                    for (Vector3r v2 : allVertices) {
                        if (v.equals2d(v2)) {
                            it.remove();
                            notRemoved = false;
                            break;
                        }
                    }
                    if (notRemoved) {
                        allVertices.add(v);
                    }
                }

                // Assuming that there are intersections, because all trivial cases were checked before
                // MARK Validate state

                final int size = allVertices.size();
                switch (size) {
                case 0: // Fall through
                case 1:
                case 2:
                    return null;
                case 3: // Triangle
                    break;
                case 4: // Four corners or more (up to 7)
                case 5:
                case 6:
                case 7:
                {
                    List<Vector3r> rv = new ArrayList<>();
                    rv.addAll(convexHull(allVertices));
                    allVertices.clear();
                    allVertices.addAll(rv);
                }
                break;
                default:
                    break;
                }

                // Project points on the target plane
                {
                    List<Vector3r> allVertices2 = new ArrayList<>();
                    allVertices2.addAll(allVertices);
                    allVertices.clear();
                    for (Vector3r v : allVertices2) {
                        Vector3r v3 = new Vector3r();
                        v = pc.get3DCoordinatesFromScreen(v, minv);
                        projectRayOnTrianglePlane(new Vector3r(v.x, v.y, v.z) , dirN, tv[0], tv[1], tv[2], v3);
                        allVertices.add(v3);
                    }
                    // Return the intersection info
                    return new IntersectionInfo(target, origin, allVertices);
                }
            }
        }
    }

    private List<IntersectionInfo> getIntersectionInfo(GData origin, GData target, Vector3r dir, Vector3r dirN, RationalMatrix m, RationalMatrix minv, PerspectiveCalculator pc, SlicerProSettings ss) {
        final int ot = origin.type();
        final int tt = target.type();

        final List<IntersectionInfo> result = new ArrayList<>();

        if (ot == 3 && tt == 3) {

            Vertex[] ov = triangles.get(origin);
            Vertex[] tv = triangles.get(target);

            Vector3r[] ov1 = new Vector3r[]{new Vector3r(ov[0]), new Vector3r(ov[1]), new Vector3r(ov[2])};
            Vector3r[] tv1 = new Vector3r[]{new Vector3r(tv[0]), new Vector3r(tv[1]), new Vector3r(tv[2])};

            IntersectionInfo tti = getTriangleTriangleIntersection(ov1, tv1, origin, target, dir, dirN, m, minv, pc, ss);
            if (tti != null) {
                result.add(tti);
            }
        } else if (ot == 4 && tt == 4) {

            Vertex[] ov = quads.get(origin);
            Vertex[] tv = quads.get(target);

            Vector3r[] ov1 = new Vector3r[]{new Vector3r(ov[0]), new Vector3r(ov[1]), new Vector3r(ov[2])};
            Vector3r[] ov2 = new Vector3r[]{new Vector3r(ov[2]), new Vector3r(ov[3]), new Vector3r(ov[0])};
            Vector3r[] tv1 = new Vector3r[]{new Vector3r(tv[0]), new Vector3r(tv[1]), new Vector3r(tv[2])};
            Vector3r[] tv2 = new Vector3r[]{new Vector3r(tv[2]), new Vector3r(tv[3]), new Vector3r(tv[0])};

            IntersectionInfo tti1 = getTriangleTriangleIntersection(ov1, tv1, origin, target, dir, dirN, m, minv, pc, ss);
            IntersectionInfo tti2 = getTriangleTriangleIntersection(ov1, tv2, origin, target, dir, dirN, m, minv, pc, ss);
            IntersectionInfo tti3 = getTriangleTriangleIntersection(ov2, tv1, origin, target, dir, dirN, m, minv, pc, ss);
            IntersectionInfo tti4 = getTriangleTriangleIntersection(ov2, tv2, origin, target, dir, dirN, m, minv, pc, ss);

            if (tti1 != null) {
                result.add(tti1);
            }
            if (tti2 != null) {
                result.add(tti2);
            }
            if (tti3 != null) {
                result.add(tti3);
            }
            if (tti4 != null) {
                result.add(tti4);
            }
        } else if (ot == 4 && tt == 3) {
            Vertex[] ov = quads.get(origin);
            Vertex[] tv = triangles.get(target);

            Vector3r[] tv1 = new Vector3r[]{new Vector3r(tv[0]), new Vector3r(tv[1]), new Vector3r(tv[2])};
            Vector3r[] ov1 = new Vector3r[]{new Vector3r(ov[0]), new Vector3r(ov[1]), new Vector3r(ov[2])};
            Vector3r[] ov2 = new Vector3r[]{new Vector3r(ov[2]), new Vector3r(ov[3]), new Vector3r(ov[0])};

            IntersectionInfo tti1 = getTriangleTriangleIntersection(ov1, tv1, origin, target, dir, dirN, m, minv, pc, ss);
            IntersectionInfo tti2 = getTriangleTriangleIntersection(ov2, tv1, origin, target, dir, dirN, m, minv, pc, ss);

            if (tti1 != null) {
                result.add(tti1);
            }
            if (tti2 != null) {
                result.add(tti2);
            }
        } else if (ot == 3 && tt == 4) {

            Vertex[] ov = triangles.get(origin);
            Vertex[] tv = quads.get(target);

            Vector3r[] ov1 = new Vector3r[]{new Vector3r(ov[0]), new Vector3r(ov[1]), new Vector3r(ov[2])};
            Vector3r[] tv1 = new Vector3r[]{new Vector3r(tv[0]), new Vector3r(tv[1]), new Vector3r(tv[2])};
            Vector3r[] tv2 = new Vector3r[]{new Vector3r(tv[2]), new Vector3r(tv[3]), new Vector3r(tv[0])};

            IntersectionInfo tti1 = getTriangleTriangleIntersection(ov1, tv1, origin, target, dir, dirN, m, minv, pc, ss);
            IntersectionInfo tti2 = getTriangleTriangleIntersection(ov1, tv2, origin, target, dir, dirN, m, minv, pc, ss);

            if (tti1 != null) {
                result.add(tti1);
            }
            if (tti2 != null) {
                result.add(tti2);
            }
        }

        return result;
    }

    private void projectRayOnTrianglePlane(Vector3r vector3r, Vector3r dirN, Vector3r tv, Vector3r tv2, Vector3r tv3, Vector3r r) {
        Rational diskr;
        Vector3r vert0 = new Vector3r(tv);
        Vector3r vert1 = new Vector3r(tv2);
        Vector3r vert2 = new Vector3r(tv3);
        Vector3r corner1 = Vector3r.sub(vert1, vert0);
        Vector3r corner2 = Vector3r.sub(vert2, vert0);
        Vector3r orig2 = new Vector3r(vector3r);
        Vector3r dir2 = new Vector3r(dirN);
        Vector3r pvec = Vector3r.cross(dir2, corner2);
        diskr = Vector3r.dot(corner1, pvec);
        if (diskr.abs().compareTo(Rational.ZERO) == 0)
            return;
        Rational invDiskr = Rational.ONE.divide(diskr);
        Vector3r tvec = Vector3r.sub(orig2, vert0);
        Vector3r qvec = Vector3r.cross(tvec, corner1);
        Rational t = Vector3r.dot(corner2, qvec).multiply(invDiskr);
        r.setX(orig2.x.add(dir2.x.multiply(t)));
        r.setY(orig2.y.add(dir2.y.multiply(t)));
        r.setZ(orig2.z.add(dir2.z.multiply(t)));
    }

    private List<Vector3r> convexHull(List<Vector3r> allVertices) {

        List<List<Vector3r>> perms = permute(allVertices);

        for (List<Vector3r> permutation : perms) {

            List<Vector3r[]> lines = new ArrayList<>();

            int sm1 = permutation.size() - 1;
            for (int i = 0; i < sm1; i++) {
                Vector3r v1 = permutation.get(i);
                Vector3r v2 = permutation.get(i + 1);
                lines.add(new Vector3r[]{v1, v2});
            }

            lines.add(new Vector3r[]{ permutation.get(sm1), permutation.get(0)});

            boolean skipIt = false;
            for (int j = 2; j < sm1; j++) {
                Vector3r[] l1 = lines.get(0);
                Vector3r[] l2 = lines.get(j);
                if (ills(l1[0], l1[1], l2[0], l2[1])) {
                    skipIt = true;
                    break;
                }
                if (skipIt) break;
            }
            sm1++;
            for (int i = 1; i < sm1; i++) {
                if (skipIt) break;
                for (int j = i + 2; j < sm1; j++) {
                    Vector3r[] l1 = lines.get(i);
                    Vector3r[] l2 = lines.get(j);
                    if (ills(l1[0], l1[1], l2[0], l2[1])) {
                        skipIt = true;
                        break;
                    }
                }
            }
            if (!skipIt) {
                return permutation;
            }
        }

        return allVertices;
    }

    private <T> List<List<T>> permute(List<T> num) {
        List<List<T>> result = new ArrayList<>();

        for (int i = 0; i < num.size(); i++) {
            List<T> first = new ArrayList<>();
            T item = num.get(i);
            first.add(item);
            permuteHelper(i, result, first, num);
        }

        return result;
    }

    private <T> void permuteHelper(int removeAt, List<List<T>> result, List<T> first, List<T> num) {
        List<T> nextRemaining = new ArrayList<>();
        nextRemaining.addAll(num);
        nextRemaining.remove(removeAt);
        if (nextRemaining.isEmpty()) {
            result.add(first);
        } else {
            for (int i = 0; i < nextRemaining.size(); i++) {
                T item = nextRemaining.get(i);
                List<T> nextHead = new ArrayList<>();
                nextHead.addAll(first);
                nextHead.add(item);
                permuteHelper(i, result, nextHead, nextRemaining);
            }
        }
    }

    private GData3 checkNormal(GData3 g3, Matrix4f vport) {
        Vertex[] v = triangles.get(g3);

        Vector4f n = new Vector4f();
        n.setW(1f);
        n.setX((v[2].y - v[0].y) * (v[1].z - v[0].z) - (v[2].z - v[0].z) * (v[1].y - v[0].y));
        n.setY((v[2].z - v[0].z) * (v[1].x - v[0].x) - (v[2].x - v[0].x) * (v[1].z - v[0].z));
        n.setZ((v[2].x - v[0].x) * (v[1].y - v[0].y) - (v[2].y - v[0].y) * (v[1].x - v[0].x));
        Matrix4f.transform(vport, n, n);
        Vector4f.sub(n, new Vector4f(vport.m03, vport.m13, vport.m23, 0f), n);
        if (n.z > 0f ^ Editor3DWindow.getWindow().hasBfcToggle()) {
            return new GData3(g3.colourNumber, g3.r, g3.g, g3.b, g3.a, v[0], v[2], v[1], View.DUMMY_REFERENCE, linkedDatFile, g3.isTriangle);
        } else {
            return null;
        }

    }

    private int intersectLineLineSegment2DExact(Vector3r p, Vector3r p2, Vector3r q, Vector3r q2, Vector3r ip, Vector3r ip2) {

        Vector3r u = Vector3r.sub(p2, p);
        Vector3r v = Vector3r.sub(q2, q);
        Vector3r w = Vector3r.sub(p, q);
        Rational d = u.x.multiply(v.y).subtract(u.y.multiply(v.x));

        if (d.isZero()) {

            if (!u.x.multiply(w.y).subtract(u.y.multiply(w.x)).isZero() || !v.x.multiply(w.y).subtract(v.y.multiply(w.x)).isZero())  {
                return 0;
            }

            Rational du = u.x.multiply(u.x).add(u.y.multiply(u.y));
            Rational dv = v.x.multiply(v.x).add(v.y.multiply(v.y));

            if (du.isZero() || dv.isZero()) {
                return 0;
            }

            Rational t0;
            Rational t1;
            Vector3r w2 = Vector3r.sub(p2, q);
            if (!v.x.isZero()) {
                t0 = w.x.divide(v.x);
                t1 = w2.x.divide(v.x);
            }
            else {
                t0 = w.y.divide(v.y);
                t1 = w2.y.divide(v.y);
            }
            if (t0.compareTo(t1) > 0) {
                Rational t=t0; t0=t1; t1=t;
            }
            if (t0.compareTo(Rational.ONE) > 0 || t1.compareTo(Rational.ZERO) < 0) {
                return 0;
            }
            t0 = t0.compareTo(Rational.ZERO) < 0 ? Rational.ZERO : t0;
            t1 = t1.compareTo(Rational.ONE) > 0 ? Rational.ONE : t1;
            if (t0.compareTo(t1) == 0) {
                return 0;
            }

            ip.setX(q.x.add(t0.multiply(v.x)));
            ip.setY(q.y.add(t0.multiply(v.y)));
            ip.setZ(q.z.add(t0.multiply(v.z)));
            ip2.setX(q.x.add(t1.multiply(v.x)));
            ip2.setY(q.y.add(t1.multiply(v.y)));
            ip2.setZ(q.z.add(t1.multiply(v.z)));
            return 2;
        }

        Rational sI = v.x.multiply(w.y).subtract(v.y.multiply(w.x)).divide(d);
        if (sI.compareTo(Rational.ZERO) < 0 || sI.compareTo(Rational.ONE) > 0)
            return 0;

        Rational tI = u.x.multiply(w.y).subtract(u.y.multiply(w.x)).divide(d);
        if (tI.compareTo(Rational.ZERO) < 0 || tI.compareTo(Rational.ONE) > 0)
            return 0;

        ip.setX(p.x.add(sI.multiply(u.x)));
        ip.setY(p.y.add(sI.multiply(u.y)));
        ip.setZ(p.z.add(sI.multiply(u.z)));
        return 1;
    }

    private boolean ills(Vector3r p, Vector3r p2, Vector3r q, Vector3r q2) {

        Vector3r u = Vector3r.sub(p2, p);
        Vector3r v = Vector3r.sub(q2, q);
        Vector3r w = Vector3r.sub(p, q);
        Rational d = u.x.multiply(v.y).subtract(u.y.multiply(v.x));

        if (d.isZero()) {

            if (!u.x.multiply(w.y).subtract(u.y.multiply(w.x)).isZero() || !v.x.multiply(w.y).subtract(v.y.multiply(w.x)).isZero())  {
                return false;
            }

            Rational du = u.x.multiply(u.x).add(u.y.multiply(u.y));
            Rational dv = v.x.multiply(v.x).add(v.y.multiply(v.y));

            if (du.isZero() && dv.isZero()) {
                return p.equals(q);
            }
            if (du.isZero()) {
                return !inSegment(p, q, q2);
            }
            if (dv.isZero()) {
                return !inSegment(q, p, p2);
            }
            Rational t0;
            Rational t1;
            Vector3r w2 = Vector3r.sub(p2, q);
            if (!v.x.isZero()) {
                t0 = w.x.divide(v.x);
                t1 = w2.x.divide(v.x);
            }
            else {
                t0 = w.y.divide(v.y);
                t1 = w2.y.divide(v.y);
            }
            if (t0.compareTo(t1) > 0) {
                Rational t=t0; t0=t1; t1=t;
            }
            if (t0.compareTo(Rational.ONE) > 0 || t1.compareTo(Rational.ZERO) < 0) {
                return false;
            }
            return true;
        }

        Rational sI = v.x.multiply(w.y).subtract(v.y.multiply(w.x)).divide(d);
        if (sI.compareTo(Rational.ZERO) < 0 || sI.compareTo(Rational.ONE) > 0)
            return false;

        Rational tI = u.x.multiply(w.y).subtract(u.y.multiply(w.x)).divide(d);
        if (tI.compareTo(Rational.ZERO) < 0 || tI.compareTo(Rational.ONE) > 0)
            return false;

        return true;
    }

    private boolean inSegment(Vector3r p, Vector3r s1, Vector3r s2) {
        if (s1.x.compareTo(s1.x) != 0) {
            if (s1.x.compareTo(p.x) <= 0 && p.x.compareTo(s2.x) <= 0)
                return true;
            if (s1.x.compareTo(p.x) >= 0  && p.x.compareTo(s2.x) >= 0)
                return true;
        } else {
            if (s1.y.compareTo(p.y) <= 0 && p.y.compareTo(s2.y) <= 0)
                return true;
            if (s1.y.compareTo(p.y) >= 0 && p.y.compareTo(s2.y) >= 0)
                return true;
        }
        return false;
    }
}
