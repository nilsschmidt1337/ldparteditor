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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.nschmidt.ldparteditor.enumtype.LDConfig;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.helper.LDPartEditorException;
import org.nschmidt.ldparteditor.helper.composite3d.PathTruderSettings;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.text.DatParser;

class VM07PathTruder extends VM06Edger2 {

    /* Null vector */
    private final double[] nullv = new double[]{0.0,0.0,0.0};
    private static final double EPSILON = 0.000001;

    protected VM07PathTruder(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    @SuppressWarnings("java:S2111")
    public void pathTruder(final PathTruderSettings ps, boolean syncWithEditor, Set<GData> sl) {
        if (linkedDatFile.isReadOnly() && syncWithEditor) return;

        final Set<GData2> originalSelection = new HashSet<>();
        final Set<GData2> newLines = new HashSet<>();
        final Set<GData3> newTriangles = new HashSet<>();
        final Set<GData4> newQuads = new HashSet<>();

        final List<GData2> shape1 = new ArrayList<>();
        final List<GData2> shape2 = new ArrayList<>();

        final List<GData2> path1 = new ArrayList<>();
        final List<GData2> path2 = new ArrayList<>();

        final List<GData2> path1endSegments = new ArrayList<>();
        final List<GData2> path2endSegments = new ArrayList<>();

        final List<GData2> lineIndicators = new ArrayList<>();

        if (syncWithEditor) {
            originalSelection.addAll(selectedLines);
        } else {
            for (GData gd : sl) {
                originalSelection.add((GData2) gd);
            }
        }

        // Validate and evaluate selection
        {
            final GData2 shape1Normal;
            final GData2 shape2Normal;
            GData2 shape1Normal2 = null;
            GData2 shape2Normal2 = null;
            GData data2draw = linkedDatFile.getDrawChainStart();
            int lineNumber = 0;
            while ((data2draw = data2draw.getNext()) != null) {
                lineNumber += 1;
                GData2 line = null;
                if (originalSelection.contains(data2draw) && data2draw.type() == 2) line = (GData2) data2draw;
                if (!originalSelection.contains(data2draw) && data2draw.type() == 9) {
                    for (GData2 g2 : originalSelection) {
                        if ((int) g2.a == lineNumber) {
                            line = g2;
                            break;
                        }
                    }
                }
                if (line != null) {
                    switch (line.colourNumber) {
                    case 1:
                        path1.add(line);
                        break;
                    case 2:
                        path2.add(line);
                        break;
                    case 5:
                        shape1.add(line);
                        break;
                    case 7:
                        lineIndicators.add(line);
                        break;
                    case 13:
                        shape2.add(line);
                        break;
                    case 4:
                        if (shape1Normal2 == null) {
                            shape1Normal2 = line;
                        } else {
                            return;
                        }
                        break;
                    case 12:
                        if (shape2Normal2 == null) {
                            shape2Normal2 = line;
                        } else {
                            return;
                        }
                        break;
                    default:
                        break;
                    }
                }
            }
            if (shape1Normal2 == null || shape1.isEmpty() || path1.isEmpty() || path2.isEmpty() || shape2Normal2 != null && shape2.isEmpty()) {
                return;
            }
            if (path1.size() != path2.size() || shape2Normal2 != null && shape1.size() != shape2.size()) {
                return;
            }
            // Copy shape 1 to shape 2
            if (shape2Normal2 == null) {
                shape2.clear();
                shape2Normal2 = shape1Normal2;
                shape2.addAll(shape1);
            }
            shape1Normal = shape1Normal2;
            shape2Normal = shape2Normal2;

            // Insert zero length lines as line indicators
            {
                SortedSet<Vertex> liVerts = new TreeSet<>();
                for (GData2 ind : lineIndicators) {
                    Vertex[] verts = lines.get(ind);
                    if (verts == null) {
                        verts = new Vertex[2];
                        verts[0] = new Vertex(ind.x1p, ind.y1p, ind.z1p);
                        verts[1] = new Vertex(ind.x2p, ind.y2p, ind.z2p);
                    }
                    liVerts.add(verts[0]);
                    liVerts.add(verts[1]);
                }

                Set<Integer> indices = new HashSet<>();

                // Shape 1
                {
                    int ss = shape1.size();
                    final List<GData2> shapeTmp = new ArrayList<>(ss);
                    int ssm = ss - 1;
                    for (int i = 0; i < ss; i++) {
                        Vertex[] verts = lines.get(shape1.get(i));
                        if (verts == null) {
                            GData2 ind = shape1.get(i);
                            verts = new Vertex[2];
                            verts[0] = new Vertex(ind.x1p, ind.y1p, ind.z1p);
                            verts[1] = new Vertex(ind.x2p, ind.y2p, ind.z2p);
                        }
                        if (i == 0) {
                            if (liVerts.contains(verts[0])) {
                                shapeTmp.add(new GData2(verts[0], verts[0], View.DUMMY_REFERENCE, new GColour(), true));
                                indices.add(i);
                            }
                            shapeTmp.add(shape1.get(i));
                        } else if (i == ssm) {
                            shapeTmp.add(shape1.get(i));
                            if (liVerts.contains(verts[1])) {
                                shapeTmp.add(new GData2(verts[1], verts[1], View.DUMMY_REFERENCE, new GColour(), true));
                                indices.add(i);
                            }
                        } else {
                            Vertex[] verts2 = lines.get(shape1.get(i - 1));
                            if (verts2 == null) {
                                GData2 ind = shape1.get(i - 1);
                                verts2 = new Vertex[2];
                                verts2[0] = new Vertex(ind.x1p, ind.y1p, ind.z1p);
                                verts2[1] = new Vertex(ind.x2p, ind.y2p, ind.z2p);
                            }
                            if (verts2[1].equals(verts[0]) && liVerts.contains(verts[0])) {
                                shapeTmp.add(new GData2(verts[0], verts[0], View.DUMMY_REFERENCE, new GColour(), true));
                                indices.add(i);
                            }
                            shapeTmp.add(shape1.get(i));
                        }
                    }
                    shape1.clear();
                    shape1.addAll(shapeTmp);
                }

                // Shape 2
                {
                    int ss = shape2.size();
                    final List<GData2> shapeTmp = new ArrayList<>(ss);
                    int ssm = ss - 1;
                    for (int i = 0; i < ss; i++) {
                        Vertex[] verts = lines.get(shape2.get(i));
                        if (verts == null) {
                            GData2 ind = shape2.get(i);
                            verts = new Vertex[2];
                            verts[0] = new Vertex(ind.x1p, ind.y1p, ind.z1p);
                            verts[1] = new Vertex(ind.x2p, ind.y2p, ind.z2p);
                        }
                        if (i == 0) {
                            if (indices.contains(i)) {
                                shapeTmp.add(new GData2(verts[0], verts[0], View.DUMMY_REFERENCE, new GColour(), true));
                            }
                            shapeTmp.add(shape2.get(i));
                        } else if (i == ssm) {
                            shapeTmp.add(shape2.get(i));
                            if (indices.contains(i)) {
                                shapeTmp.add(new GData2(verts[1], verts[1], View.DUMMY_REFERENCE, new GColour(), true));
                            }
                        } else {
                            if (indices.contains(i)) {
                                shapeTmp.add(new GData2(verts[0], verts[0], View.DUMMY_REFERENCE, new GColour(), true));
                            }
                            shapeTmp.add(shape2.get(i));
                        }
                    }
                    shape2.clear();
                    shape2.addAll(shapeTmp);
                }
            }

            shape1.add(0, shape1Normal);
            shape2.add(0, shape2Normal);
        }

        if (syncWithEditor) {

            // Clear selection
            clearSelection2();

            try {
                new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress() {
                    @Override
                    public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                        try {
                            monitor.beginTask(I18n.VM_PATH_TRUDER, IProgressMonitor.UNKNOWN);

                            final GColour col16 = LDConfig.getColour16();
                            final Thread[] threads = new Thread[1];
                            threads[0] = new Thread(() -> {

                                if (monitor.isCanceled()) {
                                    return;
                                }

                                final GColour lineColour = DatParser.validateColour(24, .5f, .5f, .5f, 1f).createClone();
                                final GColour bodyColour = DatParser.validateColour(16, col16.getR(), col16.getG(), col16.getB(), 1f).createClone();

                                double vertmerge = 0.001;
                                double pi = 3.14159265358979323846;

                                int maxLine = 1000;
                                double small = 0.1;
                                double smallAngle = .95;

                                double[][][] path1Arr = new double[5 * maxLine][2][3];
                                double[][][] path2Arr = new double[5 * maxLine][2][3];
                                double[][][] path1aArr = new double[maxLine][2][3];
                                double[][][] path2aArr = new double[maxLine][2][3];
                                /** [lineIndex][pointIndex][coordinateIndex] */
                                double[][][] shape1Arr = new double[maxLine][2][3];
                                /** [lineIndex][pointIndex][coordinateIndex] */
                                double[][][] shape2Arr = new double[maxLine][2][3];
                                double[][][] curShape = new double[maxLine][2][3];
                                double[][][] nxtShape = new double[maxLine][2][3];
                                double[] shape1Vect = new double[3];
                                double[] shape2Vect = new double[3];

                                double[] temp1 = new double[3];
                                double[] temp2 = new double[3];
                                double[] temp3 = new double[3];
                                double[] xVect = new double[3];
                                double[] yVect = new double[3];
                                double[] zVect = new double[3];

                                double angle;
                                double ca;
                                double sa;
                                double ratio;
                                double[][][] sortBuf = new double[maxLine][2][3];
                                int[][][] next = new int[maxLine][2][2];

                                int path1Len = 0;
                                int path2Len = 0;
                                int shape1Len = 0;
                                int shape2Len = 0;

                                boolean circular = false;
                                double maxlength = ps.getMaxPathSegmentLength().doubleValue();
                                double dmax;
                                double d = 0.0;
                                double len;
                                int inLineIdx;
                                int numPath;
                                boolean invert = ps.isInverted();

                                int transitions = ps.getTransitionCount();
                                double slope = ps.getTransitionCurveControl().doubleValue();
                                double position = ps.getTransitionCurveCenter().doubleValue();
                                double crease = ps.getPathAngleForLine().doubleValue();
                                boolean compensate = ps.isCompensation();
                                boolean endings = path1endSegments.size() == 2 && path2endSegments.size() == 2;
                                double rotation = ps.getRotation().doubleValue();

                                {
                                    // Read path file 1
                                    if (endings) {
                                        path1.add(0, path1endSegments.get(0));
                                        path1.add(path1endSegments.get(1));
                                    }
                                    for (GData2 p : path1) {
                                        sortBuf[path1Len][0][0] = p.x1p.doubleValue();
                                        sortBuf[path1Len][0][1] = p.y1p.doubleValue();
                                        sortBuf[path1Len][0][2] = p.z1p.doubleValue();
                                        sortBuf[path1Len][1][0] = p.x2p.doubleValue();
                                        sortBuf[path1Len][1][1] = p.y2p.doubleValue();
                                        sortBuf[path1Len][1][2] = p.z2p.doubleValue();
                                        next[path1Len][0][0] = next[path1Len][1][0] = -1;
                                        path1Len++;
                                    }
                                    // Sort path file 1
                                    circular = true;
                                    for (int i = 0; i < path1Len; i++) {
                                        for (int j = 0; j < 2; j++) {
                                            if (next[i][j][0] != -1)
                                                break;
                                            dmax = 10000000;
                                            for (int k = 0; k < path1Len; k++) {
                                                if (k != i) {
                                                    for (int l = 0; l < 2; l++) {
                                                        d = manhattan(sortBuf[i][j], sortBuf[k][l]);
                                                        if (d < dmax) {
                                                            dmax = d;
                                                            next[i][j][0] = k;
                                                            next[i][j][1] = l;
                                                        }
                                                        if (d == 0)
                                                            break;
                                                    }
                                                    if (d == 0)
                                                        break;
                                                }
                                            }
                                            if (dmax > small) {
                                                next[i][j][0] = -1;
                                                circular = false;
                                            }
                                        }
                                    }
                                    if (circular) {
                                        next[next[0][0][0]][next[0][0][1]][0] = -1;
                                        next[0][0][0] = -1;
                                    }
                                    inLineIdx = 0;
                                    numPath = 0;
                                    for (int i = 0; i < path1Len; i++) {
                                        for (int j = 0; j < 2; j++) {
                                            int a;
                                            int b;
                                            int c;
                                            int d2;
                                            if (next[i][j][0] == -1) {
                                                numPath++;
                                                a = i;
                                                b = j;
                                                do {
                                                    set(path1aArr[inLineIdx][0], sortBuf[a][b]);
                                                    set(path1aArr[inLineIdx][1], sortBuf[a][1 - b]);
                                                    inLineIdx++;

                                                    d2 = next[a][1 - b][1];
                                                    c = next[a][1 - b][0];
                                                    next[a][1 - b][0] = -2;
                                                    next[a][b][0] = -2;
                                                    b = d2;
                                                    a = c;
                                                } while (a != -1);
                                            }
                                        }
                                    }

                                    path1Len = inLineIdx;

                                    if (numPath > 1) {
                                        // NumPath distinct paths found in Path file 1. Unexpected results may happen!
                                    }
                                }
                                {
                                    // Read path file 2
                                    if (endings) {
                                        path2.add(0, path2endSegments.get(0));
                                        path2.add(path2endSegments.get(1));
                                    }
                                    for (GData2 p : path2) {
                                        sortBuf[path2Len][0][0] = p.x1p.doubleValue();
                                        sortBuf[path2Len][0][1] = p.y1p.doubleValue();
                                        sortBuf[path2Len][0][2] = p.z1p.doubleValue();
                                        sortBuf[path2Len][1][0] = p.x2p.doubleValue();
                                        sortBuf[path2Len][1][1] = p.y2p.doubleValue();
                                        sortBuf[path2Len][1][2] = p.z2p.doubleValue();
                                        next[path2Len][0][0] = next[path2Len][1][0] = -1;
                                        path2Len++;
                                    }
                                    // Sort path file 2
                                    circular = true;
                                    for (int i = 0; i < path2Len; i++) {
                                        for (int j = 0; j < 2; j++) {
                                            if (next[i][j][0] != -1)
                                                break;
                                            dmax = 10000000;
                                            for (int k = 0; k < path2Len; k++) {
                                                if (k != i) {
                                                    for (int l = 0; l < 2; l++) {
                                                        d = manhattan(sortBuf[i][j], sortBuf[k][l]);
                                                        if (d < dmax) {
                                                            dmax = d;
                                                            next[i][j][0] = k;
                                                            next[i][j][1] = l;
                                                        }
                                                        if (d == 0)
                                                            break;
                                                    }
                                                    if (d == 0)
                                                        break;
                                                }
                                            }
                                            if (dmax > small) {
                                                next[i][j][0] = -1;
                                                circular = false;
                                            }
                                        }
                                    }
                                    if (circular) {
                                        next[next[0][0][0]][next[0][0][1]][0] = -1;
                                        next[0][0][0] = -1;
                                    }
                                    inLineIdx = 0;
                                    numPath = 0;
                                    for (int i = 0; i < path2Len; i++) {
                                        for (int j = 0; j < 2; j++) {
                                            int a;
                                            int b;
                                            int c;
                                            int d2;
                                            if (next[i][j][0] == -1) {
                                                numPath++;
                                                a = i;
                                                b = j;
                                                do {
                                                    set(path2aArr[inLineIdx][0], sortBuf[a][b]);
                                                    set(path2aArr[inLineIdx][1], sortBuf[a][1 - b]);
                                                    inLineIdx++;

                                                    d2 = next[a][1 - b][1];
                                                    c = next[a][1 - b][0];
                                                    next[a][1 - b][0] = -2;
                                                    next[a][b][0] = -2;
                                                    b = d2;
                                                    a = c;
                                                } while (a != -1);
                                            }
                                        }
                                    }

                                    path2Len = inLineIdx;

                                }

                                for (GData2 p : shape1) {
                                    shape1Arr[shape1Len][0][0] = p.x1p.doubleValue();
                                    shape1Arr[shape1Len][0][1] = p.y1p.doubleValue();
                                    shape1Arr[shape1Len][0][2] = p.z1p.doubleValue();
                                    shape1Arr[shape1Len][1][0] = p.x2p.doubleValue();
                                    shape1Arr[shape1Len][1][1] = p.y2p.doubleValue();
                                    shape1Arr[shape1Len][1][2] = p.z2p.doubleValue();
                                    shape1Len++;
                                }

                                for (GData2 p : shape2) {
                                    shape2Arr[shape2Len][0][0] = p.x1p.doubleValue();
                                    shape2Arr[shape2Len][0][1] = p.y1p.doubleValue();
                                    shape2Arr[shape2Len][0][2] = p.z1p.doubleValue();
                                    shape2Arr[shape2Len][1][0] = p.x2p.doubleValue();
                                    shape2Arr[shape2Len][1][1] = p.y2p.doubleValue();
                                    shape2Arr[shape2Len][1][2] = p.z2p.doubleValue();
                                    shape2Len++;
                                }

                                if (path1Len != path2Len) {
                                    // The two path files do not have the same number of elements!
                                    return;
                                }

                                if (endings && path1Len < 3 && !circular) {
                                    // Path files must have at least 3 elements to use -e option!
                                    return;
                                }

                                if (shape1Len != shape2Len) {
                                    // The two shape files do not have the same number of elements!
                                    return;
                                }

                                // Split long lines
                                inLineIdx = 0;
                                for (int i = 0; i < path1Len; i++) {
                                    double[] p1 = new double[3];
                                    double[] p2 = new double[3];
                                    double[] q1 = new double[3];
                                    double[] q2 = new double[3];
                                    double[] delta1 = new double[3];
                                    double[] delta2 = new double[3];
                                    double[] temp = new double[3];
                                    int nsplit1;
                                    int nsplit2;

                                    set(p1, path1aArr[i][0]);
                                    set(p2, path1aArr[i][1]);

                                    set(q1, path2aArr[i][0]);
                                    set(q2, path2aArr[i][1]);

                                    nsplit1 = (int) (dist(p1, p2) / maxlength) + 1;
                                    nsplit2 = (int) (dist(q1, q2) / maxlength) + 1;

                                    // don't split endings segments
                                    if (endings && (i == 0 || i == path1Len - 1)) {
                                        nsplit1 = nsplit2 = 1;
                                    }

                                    nsplit1 = nsplit1 > nsplit2 ? nsplit1 : nsplit2;

                                    sub(delta1, p2, p1);
                                    mult(delta1, delta1, 1.0 / nsplit1);
                                    sub(delta2, q2, q1);
                                    mult(delta2, delta2, 1.0 / nsplit1);
                                    for (int k = 0; k < nsplit1; k++) {
                                        mult(temp, delta1, k);
                                        add(path1Arr[inLineIdx][0], p1, temp);
                                        add(path1Arr[inLineIdx][1], path1Arr[inLineIdx][0], delta1);
                                        mult(temp, delta2, k);
                                        add(path2Arr[inLineIdx][0], q1, temp);
                                        add(path2Arr[inLineIdx][1], path2Arr[inLineIdx][0], delta2);

                                        inLineIdx++;
                                    }
                                }

                                path1Len = path2Len = inLineIdx;
                                set(path1Arr[path1Len][0], path1Arr[path1Len - 1][1]);
                                set(path1Arr[path1Len][1], path1Arr[path1Len - 1][0]);

                                set(path2Arr[path2Len][0], path2Arr[path2Len - 1][1]);
                                set(path2Arr[path2Len][1], path2Arr[path2Len - 1][0]);

                                len = dist(shape1Arr[0][0], shape1Arr[0][1]);

                                for (int i = 1; i < shape1Len; i++) {
                                    sub(shape1Arr[i][0], shape1Arr[i][0], shape1Arr[0][0]);
                                    mult(shape1Arr[i][0], shape1Arr[i][0], 1 / len);
                                    sub(shape1Arr[i][1], shape1Arr[i][1], shape1Arr[0][0]);
                                    mult(shape1Arr[i][1], shape1Arr[i][1], 1 / len);
                                }
                                sub(shape1Vect, shape1Arr[0][1], shape1Arr[0][0]);

                                angle = Math.atan2(-shape1Vect[0], -shape1Vect[1]);

                                sa = Math.sin(angle);
                                ca = Math.cos(angle);

                                for (int i = 1; i < shape1Len; i++) {
                                    shape1Arr[i - 1][0][0] = shape1Arr[i][0][0] * ca - shape1Arr[i][0][1] * sa;
                                    shape1Arr[i - 1][0][1] = shape1Arr[i][0][0] * sa + shape1Arr[i][0][1] * ca;
                                    shape1Arr[i - 1][1][0] = shape1Arr[i][1][0] * ca - shape1Arr[i][1][1] * sa;
                                    shape1Arr[i - 1][1][1] = shape1Arr[i][1][0] * sa + shape1Arr[i][1][1] * ca;
                                    shape1Arr[i - 1][0][2] = shape1Arr[i][0][2];
                                    shape1Arr[i - 1][1][2] = shape1Arr[i][1][2];
                                    if (invert) {
                                        shape1Arr[i - 1][0][0] = -shape1Arr[i - 1][0][0];
                                        shape1Arr[i - 1][1][0] = -shape1Arr[i - 1][1][0];
                                    }
                                }
                                shape1Len--;

                                // Normalize shape 2

                                len = dist(shape2Arr[0][0], shape2Arr[0][1]);

                                for (int i = 1; i < shape2Len; i++) {
                                    sub(shape2Arr[i][0], shape2Arr[i][0], shape2Arr[0][0]);
                                    mult(shape2Arr[i][0], shape2Arr[i][0], 1 / len);
                                    sub(shape2Arr[i][1], shape2Arr[i][1], shape2Arr[0][0]);
                                    mult(shape2Arr[i][1], shape2Arr[i][1], 1 / len);
                                }
                                sub(shape2Vect, shape2Arr[0][1], shape2Arr[0][0]);

                                angle = Math.atan2(-shape2Vect[0], -shape2Vect[1]);

                                sa = Math.sin(angle);
                                ca = Math.cos(angle);

                                for (int i = 1; i < shape2Len; i++) {
                                    shape2Arr[i - 1][0][0] = shape2Arr[i][0][0] * ca - shape2Arr[i][0][1] * sa;
                                    shape2Arr[i - 1][0][1] = shape2Arr[i][0][0] * sa + shape2Arr[i][0][1] * ca;
                                    shape2Arr[i - 1][1][0] = shape2Arr[i][1][0] * ca - shape2Arr[i][1][1] * sa;
                                    shape2Arr[i - 1][1][1] = shape2Arr[i][1][0] * sa + shape2Arr[i][1][1] * ca;
                                    shape2Arr[i - 1][0][2] = shape2Arr[i][0][2];
                                    shape2Arr[i - 1][1][2] = shape2Arr[i][1][2];
                                    if (invert) {
                                        shape2Arr[i - 1][0][0] = -shape2Arr[i - 1][0][0];
                                        shape2Arr[i - 1][1][0] = -shape2Arr[i - 1][1][0];
                                    }

                                }

                                // Extrusion
                                // Initialize current shape
                                if (circular)
                                    endings = false;

                                if (endings) {
                                    double angle2 = pathLocalBasis(0, 1, xVect, yVect, zVect, path1Arr, path2Arr);
                                    angle = pathLocalBasis(0, 0, xVect, yVect, zVect, path1Arr, path2Arr);
                                    if (angle2 > 90) {
                                        mult(xVect, xVect, -1);
                                        mult(zVect, zVect, -1);
                                    }
                                } else {
                                    angle = pathLocalBasis(circular ? path1Len - 1 : 0, 0, xVect, yVect, zVect, path1Arr, path2Arr);
                                }
                                // compensate sharp angles
                                if (compensate) {
                                    mult(xVect, xVect, 1 / Math.cos(angle * pi / 360));
                                }

                                // Calculate next transformed shape
                                for (int j = 0; j < shape1Len; j++) {
                                    for (int k = 0; k < 2; k++) {
                                        mult(nxtShape[j][k], xVect, shape1Arr[j][k][0]);
                                        mult(temp1, yVect, shape1Arr[j][k][1]);
                                        add(nxtShape[j][k], nxtShape[j][k], temp1);
                                        mult(temp1, zVect, shape1Arr[j][k][2]);
                                        add(nxtShape[j][k], nxtShape[j][k], temp1);
                                        if (endings) {
                                            add(nxtShape[j][k], nxtShape[j][k], path1Arr[1][0]);
                                        } else {
                                            add(nxtShape[j][k], nxtShape[j][k], path1Arr[0][0]);
                                        }
                                    }

                                }
                                if (angle > crease) {
                                    // sharp angle. Create line at junction
                                    for (int i = 0; i < shape1Len; i++) {
                                        Vertex v1 = new Vertex(new BigDecimal(nxtShape[i][0][0]), new BigDecimal(nxtShape[i][0][1]), new BigDecimal(nxtShape[i][0][2]));
                                        Vertex v2 = new Vertex(new BigDecimal(nxtShape[i][1][0]), new BigDecimal(nxtShape[i][1][1]), new BigDecimal(nxtShape[i][1][2]));
                                        newLines.add(new GData2(lineColour.getColourNumber(), lineColour.getR(), lineColour.getG(), lineColour.getB(), lineColour.getA(), v1, v2, View.DUMMY_REFERENCE, linkedDatFile, true));
                                    }
                                }

                                int start;
                                int end;
                                start = 0;
                                end = path1Len;
                                if (endings) {
                                    start++;
                                    end--;
                                }
                                for (int i = start; i < end; i++) {

                                    // Transfer old next shape to current.
                                    for (int j = 0; j < shape1Len; j++) {
                                        set(curShape[j][0], nxtShape[j][0]);
                                        set(curShape[j][1], nxtShape[j][1]);
                                    }

                                    if (i == end - 1) {
                                        if (circular) {
                                            angle = pathLocalBasis(i, 0, xVect, yVect, zVect, path1Arr, path2Arr);
                                        } else {
                                            if (endings) {
                                                double angle2 = pathLocalBasis(i, i + 1, xVect, yVect, zVect, path1Arr, path2Arr);
                                                angle = pathLocalBasis(i + 2, i + 2, xVect, yVect, zVect, path1Arr, path2Arr);
                                                if (angle2 < 90) {
                                                    // in that case the local
                                                    // base is mirrorred...
                                                    sub(xVect, nullv, xVect);
                                                    sub(zVect, nullv, zVect);
                                                }
                                            } else {
                                                angle = pathLocalBasis(i + 1, i + 1, xVect, yVect, zVect, path1Arr, path2Arr);
                                                // in that case the local base
                                                // is mirrorred...
                                                sub(xVect, nullv, xVect);
                                                sub(zVect, nullv, zVect);
                                            }
                                        }
                                    } else {
                                        angle = pathLocalBasis(i, i + 1, xVect, yVect, zVect, path1Arr, path2Arr);
                                    }

                                    // compensate sharp angles
                                    if (compensate) {
                                        mult(xVect, xVect, 1 / Math.cos(angle * pi / 360));
                                    }

                                    {
                                        double x;
                                        double j = (i + 1.0 - start) * transitions % (2 * (end - start));
                                        x = 1.0 * j / (end - start);
                                        if (x > 1.0)
                                            x = 2.0 - x;
                                        ratio = sigmoid(x, slope, position);
                                    }

                                    double rotangle = rotation * pi / 180.0 * ((i + 1.0) / path1Len);

                                    sa = Math.sin(rotangle);
                                    ca = Math.cos(rotangle);

                                    for (int j = 0; j < shape1Len; j++) {
                                        for (int k = 0; k < 2; k++) {
                                            temp1[0] = shape1Arr[j][k][0] * ca - shape1Arr[j][k][1] * sa;
                                            temp1[1] = shape1Arr[j][k][0] * sa + shape1Arr[j][k][1] * ca;
                                            temp2[0] = shape2Arr[j][k][0] * ca - shape2Arr[j][k][1] * sa;
                                            temp2[1] = shape2Arr[j][k][0] * sa + shape2Arr[j][k][1] * ca;

                                            mult(nxtShape[j][k], xVect, temp1[0] * (1.0 - ratio) + temp2[0] * ratio);
                                            mult(temp3, yVect, temp1[1] * (1.0 - ratio) + temp2[1] * ratio);
                                            add(nxtShape[j][k], nxtShape[j][k], temp3);
                                            mult(temp3, zVect, shape1Arr[j][k][2] * (1.0 - ratio) + shape2Arr[j][k][2] * ratio);
                                            add(nxtShape[j][k], nxtShape[j][k], temp3);
                                            add(nxtShape[j][k], nxtShape[j][k], path1Arr[i + 1][0]);
                                        }
                                    }
                                    if (angle > crease) {
                                        // sharp angle. Create line at junction
                                        for (int j = 0; j < shape1Len; j++) {
                                            Vertex v1 = new Vertex(new BigDecimal(nxtShape[j][0][0]), new BigDecimal(nxtShape[j][0][1]), new BigDecimal(nxtShape[j][0][2]));
                                            Vertex v2 = new Vertex(new BigDecimal(nxtShape[j][1][0]), new BigDecimal(nxtShape[j][1][1]), new BigDecimal(nxtShape[j][1][2]));
                                            newLines.add(new GData2(lineColour.getColourNumber(), lineColour.getR(), lineColour.getG(), lineColour.getB(), lineColour.getA(), v1, v2, View.DUMMY_REFERENCE, linkedDatFile, true));
                                        }
                                    }
                                    // Generate tri/quad sheet
                                    for (int j = 0; j < shape1Len; j++) {
                                        if (!lineIndicators.isEmpty() && dist(shape1Arr[j][0], shape1Arr[j][1]) < EPSILON && dist(shape2Arr[j][0], shape2Arr[j][1]) < EPSILON) {
                                            // Null lenth segment in shape file
                                            // -> generate line at that place
                                            Vertex v1 = new Vertex(new BigDecimal(curShape[j][0][0]), new BigDecimal(curShape[j][0][1]), new BigDecimal(curShape[j][0][2]));
                                            Vertex v2 = new Vertex(new BigDecimal(nxtShape[j][0][0]), new BigDecimal(nxtShape[j][0][1]), new BigDecimal(nxtShape[j][0][2]));
                                            newLines.add(new GData2(lineColour.getColourNumber(), lineColour.getR(), lineColour.getG(), lineColour.getB(), lineColour.getA(), v1, v2, View.DUMMY_REFERENCE, linkedDatFile, true));
                                        }
                                        if (dist(curShape[j][0], curShape[j][1]) < vertmerge) {
                                            if (dist(nxtShape[j][0], nxtShape[j][1]) < vertmerge || dist(curShape[j][0], nxtShape[j][0]) < vertmerge || dist(nxtShape[j][1], curShape[j][1]) < vertmerge) {
                                                // Degenerated. Nothing to
                                                // output
                                                continue;
                                            } else {
                                                Vertex v1 = new Vertex(new BigDecimal(curShape[j][0][0]), new BigDecimal(curShape[j][0][1]), new BigDecimal(curShape[j][0][2]));
                                                Vertex v2 = new Vertex(new BigDecimal(nxtShape[j][1][0]), new BigDecimal(nxtShape[j][1][1]), new BigDecimal(nxtShape[j][1][2]));
                                                Vertex v3 = new Vertex(new BigDecimal(nxtShape[j][0][0]), new BigDecimal(nxtShape[j][0][1]), new BigDecimal(nxtShape[j][0][2]));
                                                newTriangles.add(new GData3(bodyColour.getColourNumber(), bodyColour.getR(), bodyColour.getG(), bodyColour.getB(), bodyColour.getA(), v1, v2, v3, View.DUMMY_REFERENCE, linkedDatFile, true));
                                                continue;
                                            }
                                        }
                                        if (dist(nxtShape[j][0], nxtShape[j][1]) < vertmerge) {
                                            if (dist(curShape[j][0], nxtShape[j][0]) < vertmerge || dist(nxtShape[j][1], curShape[j][1]) < vertmerge) {
                                                // Degenerated. Nothing to
                                                // output
                                                continue;
                                            } else {
                                                Vertex v1 = new Vertex(new BigDecimal(curShape[j][0][0]), new BigDecimal(curShape[j][0][1]), new BigDecimal(curShape[j][0][2]));
                                                Vertex v2 = new Vertex(new BigDecimal(curShape[j][1][0]), new BigDecimal(curShape[j][1][1]), new BigDecimal(curShape[j][1][2]));
                                                Vertex v3 = new Vertex(new BigDecimal(nxtShape[j][0][0]), new BigDecimal(nxtShape[j][0][1]), new BigDecimal(nxtShape[j][0][2]));
                                                newTriangles.add(new GData3(bodyColour.getColourNumber(), bodyColour.getR(), bodyColour.getG(), bodyColour.getB(), bodyColour.getA(), v1, v2, v3, View.DUMMY_REFERENCE, linkedDatFile, true));
                                                continue;
                                            }
                                        }
                                        if (dist(curShape[j][0], nxtShape[j][0]) < vertmerge) {
                                            if (dist(nxtShape[j][1], curShape[j][1]) < vertmerge) {
                                                // Degenerated. Nothing to
                                                // output
                                                continue;
                                            } else {
                                                Vertex v1 = new Vertex(new BigDecimal(curShape[j][0][0]), new BigDecimal(curShape[j][0][1]), new BigDecimal(curShape[j][0][2]));
                                                Vertex v2 = new Vertex(new BigDecimal(curShape[j][1][0]), new BigDecimal(curShape[j][1][1]), new BigDecimal(curShape[j][1][2]));
                                                Vertex v3 = new Vertex(new BigDecimal(nxtShape[j][1][0]), new BigDecimal(nxtShape[j][1][1]), new BigDecimal(nxtShape[j][1][2]));
                                                newTriangles.add(new GData3(bodyColour.getColourNumber(), bodyColour.getR(), bodyColour.getG(), bodyColour.getB(), bodyColour.getA(), v1, v2, v3, View.DUMMY_REFERENCE, linkedDatFile, true));
                                                continue;
                                            }
                                        }
                                        if (dist(nxtShape[j][1], curShape[j][1]) < vertmerge) {
                                            Vertex v1 = new Vertex(new BigDecimal(curShape[j][0][0]), new BigDecimal(curShape[j][0][1]), new BigDecimal(curShape[j][0][2]));
                                            Vertex v2 = new Vertex(new BigDecimal(curShape[j][1][0]), new BigDecimal(curShape[j][1][1]), new BigDecimal(curShape[j][1][2]));
                                            Vertex v3 = new Vertex(new BigDecimal(nxtShape[j][0][0]), new BigDecimal(nxtShape[j][0][1]), new BigDecimal(nxtShape[j][0][2]));
                                            newTriangles.add(new GData3(bodyColour.getColourNumber(), bodyColour.getR(), bodyColour.getG(), bodyColour.getB(), bodyColour.getA(), v1, v2, v3, View.DUMMY_REFERENCE, linkedDatFile, true));
                                            continue;
                                        }
                                        if (triAngle(curShape[j][0], nxtShape[j][0], nxtShape[j][1], curShape[j][1]) < smallAngle) {
                                            Vertex v1 = new Vertex(new BigDecimal(curShape[j][0][0]), new BigDecimal(curShape[j][0][1]), new BigDecimal(curShape[j][0][2]));
                                            Vertex v2 = new Vertex(new BigDecimal(curShape[j][1][0]), new BigDecimal(curShape[j][1][1]), new BigDecimal(curShape[j][1][2]));
                                            Vertex v3 = new Vertex(new BigDecimal(nxtShape[j][1][0]), new BigDecimal(nxtShape[j][1][1]), new BigDecimal(nxtShape[j][1][2]));
                                            Vertex v4 = new Vertex(new BigDecimal(nxtShape[j][0][0]), new BigDecimal(nxtShape[j][0][1]), new BigDecimal(nxtShape[j][0][2]));
                                            newQuads.add(new GData4(bodyColour.getColourNumber(), bodyColour.getR(), bodyColour.getG(), bodyColour.getB(), bodyColour.getA(), v1, v2, v3, v4, View.DUMMY_REFERENCE, linkedDatFile));
                                        } else {
                                            {
                                                Vertex v1 = new Vertex(new BigDecimal(curShape[j][0][0]), new BigDecimal(curShape[j][0][1]), new BigDecimal(curShape[j][0][2]));
                                                Vertex v2 = new Vertex(new BigDecimal(nxtShape[j][1][0]), new BigDecimal(nxtShape[j][1][1]), new BigDecimal(nxtShape[j][1][2]));
                                                Vertex v3 = new Vertex(new BigDecimal(nxtShape[j][0][0]), new BigDecimal(nxtShape[j][0][1]), new BigDecimal(nxtShape[j][0][2]));
                                                newTriangles.add(new GData3(bodyColour.getColourNumber(), bodyColour.getR(), bodyColour.getG(), bodyColour.getB(), bodyColour.getA(), v1, v2, v3, View.DUMMY_REFERENCE, linkedDatFile, true));
                                            }
                                            {
                                                Vertex v1 = new Vertex(new BigDecimal(curShape[j][0][0]), new BigDecimal(curShape[j][0][1]), new BigDecimal(curShape[j][0][2]));
                                                Vertex v2 = new Vertex(new BigDecimal(curShape[j][1][0]), new BigDecimal(curShape[j][1][1]), new BigDecimal(curShape[j][1][2]));
                                                Vertex v3 = new Vertex(new BigDecimal(nxtShape[j][1][0]), new BigDecimal(nxtShape[j][1][1]), new BigDecimal(nxtShape[j][1][2]));
                                                newTriangles.add(new GData3(bodyColour.getColourNumber(), bodyColour.getR(), bodyColour.getG(), bodyColour.getB(), bodyColour.getA(), v1, v2, v3, View.DUMMY_REFERENCE, linkedDatFile, true));
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
                            if (monitor.isCanceled()) {
                                selectedLines.addAll(originalSelection);
                                selectedData.addAll(originalSelection);
                                originalSelection.clear();
                            }
                        } finally {
                            monitor.done();
                        }
                    }
                });
            } catch (InvocationTargetException ite) {
                NLogger.error(VM07PathTruder.class, ite);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new LDPartEditorException(ie);
            }
        } else {
            final GColour col16 = LDConfig.getColour16();
            final Thread[] threads = new Thread[1];
            threads[0] = new Thread(() -> {

                final GColour bodyColour = DatParser.validateColour(16, col16.getR(), col16.getG(), col16.getB(), 1f).createClone();

                double vertmerge = 0.001;
                double pi = 3.14159265358979323846;

                int maxLine = 1000;
                double small = 0.1;
                double smallAngle = .95;

                double[][][] path1Arr = new double[5 * maxLine][2][3];
                double[][][] path2Arr = new double[5 * maxLine][2][3];
                double[][][] path1aArr = new double[maxLine][2][3];
                double[][][] path2aArr = new double[maxLine][2][3];
                /** [lineIndex][pointIndex][coordinateIndex] */
                double[][][] shape1Arr = new double[maxLine][2][3];
                /** [lineIndex][pointIndex][coordinateIndex] */
                double[][][] shape2Arr = new double[maxLine][2][3];
                double[][][] curShape = new double[maxLine][2][3];
                double[][][] nxtShape = new double[maxLine][2][3];
                double[] shape1Vect = new double[3];
                double[] shape2Vect = new double[3];

                double[] temp1 = new double[3];
                double[] temp2 = new double[3];
                double[] temp3 = new double[3];
                double[] xVect = new double[3];
                double[] yVect = new double[3];
                double[] zVect = new double[3];

                double angle;
                double ca;
                double sa;
                double ratio;
                double[][][] sortBuf = new double[maxLine][2][3];
                int[][][] next = new int[maxLine][2][2];

                int path1Len = 0;
                int path2Len = 0;
                int shape1Len = 0;
                int shape2Len = 0;

                boolean circular = false;
                double maxlength = ps.getMaxPathSegmentLength().doubleValue();
                double dmax;
                double d = 0.0;
                double len;
                int inLineIdx;
                int numPath;
                boolean invert = ps.isInverted();

                int transitions = ps.getTransitionCount();
                double slope = ps.getTransitionCurveControl().doubleValue();
                double position = ps.getTransitionCurveCenter().doubleValue();
                boolean compensate = ps.isCompensation();
                boolean endings = path1endSegments.size() == 2 && path2endSegments.size() == 2;
                double rotation = ps.getRotation().doubleValue();

                {
                    // Read path file 1
                    if (endings) {
                        path1.add(0, path1endSegments.get(0));
                        path1.add(path1endSegments.get(1));
                    }
                    for (GData2 p : path1) {
                        sortBuf[path1Len][0][0] = p.x1p.doubleValue();
                        sortBuf[path1Len][0][1] = p.y1p.doubleValue();
                        sortBuf[path1Len][0][2] = p.z1p.doubleValue();
                        sortBuf[path1Len][1][0] = p.x2p.doubleValue();
                        sortBuf[path1Len][1][1] = p.y2p.doubleValue();
                        sortBuf[path1Len][1][2] = p.z2p.doubleValue();
                        next[path1Len][0][0] = next[path1Len][1][0] = -1;
                        path1Len++;
                    }
                    // Sort path file 1
                    circular = true;
                    for (int i = 0; i < path1Len; i++) {
                        for (int j = 0; j < 2; j++) {
                            if (next[i][j][0] != -1)
                                break;
                            dmax = 10000000;
                            for (int k = 0; k < path1Len; k++) {
                                if (k != i) {
                                    for (int l = 0; l < 2; l++) {
                                        d = manhattan(sortBuf[i][j], sortBuf[k][l]);
                                        if (d < dmax) {
                                            dmax = d;
                                            next[i][j][0] = k;
                                            next[i][j][1] = l;
                                        }
                                        if (d == 0)
                                            break;
                                    }
                                    if (d == 0)
                                        break;
                                }
                            }
                            if (dmax > small) {
                                next[i][j][0] = -1;
                                circular = false;
                            }
                        }
                    }
                    if (circular) {
                        next[next[0][0][0]][next[0][0][1]][0] = -1;
                        next[0][0][0] = -1;
                    }
                    inLineIdx = 0;
                    numPath = 0;
                    for (int i = 0; i < path1Len; i++) {
                        for (int j = 0; j < 2; j++) {
                            int a;
                            int b;
                            int c;
                            int d2;
                            if (next[i][j][0] == -1) {
                                numPath++;
                                a = i;
                                b = j;
                                do {
                                    set(path1aArr[inLineIdx][0], sortBuf[a][b]);
                                    set(path1aArr[inLineIdx][1], sortBuf[a][1 - b]);
                                    inLineIdx++;

                                    d2 = next[a][1 - b][1];
                                    c = next[a][1 - b][0];
                                    next[a][1 - b][0] = -2;
                                    next[a][b][0] = -2;
                                    b = d2;
                                    a = c;
                                } while (a != -1);
                            }
                        }
                    }

                    path1Len = inLineIdx;

                    if (numPath > 1) {
                        // NumPath distinct paths found in Path file 1. Unexpected results may happen!
                    }
                }
                {
                    // Read path file 2
                    if (endings) {
                        path2.add(0, path2endSegments.get(0));
                        path2.add(path2endSegments.get(1));
                    }
                    for (GData2 p : path2) {
                        sortBuf[path2Len][0][0] = p.x1p.doubleValue();
                        sortBuf[path2Len][0][1] = p.y1p.doubleValue();
                        sortBuf[path2Len][0][2] = p.z1p.doubleValue();
                        sortBuf[path2Len][1][0] = p.x2p.doubleValue();
                        sortBuf[path2Len][1][1] = p.y2p.doubleValue();
                        sortBuf[path2Len][1][2] = p.z2p.doubleValue();
                        next[path2Len][0][0] = next[path2Len][1][0] = -1;
                        path2Len++;
                    }
                    // Sort path file 2
                    circular = true;
                    for (int i = 0; i < path2Len; i++) {
                        for (int j = 0; j < 2; j++) {
                            if (next[i][j][0] != -1)
                                break;
                            dmax = 10000000;
                            for (int k = 0; k < path2Len; k++) {
                                if (k != i) {
                                    for (int l = 0; l < 2; l++) {
                                        d = manhattan(sortBuf[i][j], sortBuf[k][l]);
                                        if (d < dmax) {
                                            dmax = d;
                                            next[i][j][0] = k;
                                            next[i][j][1] = l;
                                        }
                                        if (d == 0)
                                            break;
                                    }
                                    if (d == 0)
                                        break;
                                }
                            }
                            if (dmax > small) {
                                next[i][j][0] = -1;
                                circular = false;
                            }
                        }
                    }
                    if (circular) {
                        next[next[0][0][0]][next[0][0][1]][0] = -1;
                        next[0][0][0] = -1;
                    }
                    inLineIdx = 0;
                    numPath = 0;
                    for (int i = 0; i < path2Len; i++) {
                        for (int j = 0; j < 2; j++) {
                            int a;
                            int b;
                            int c;
                            int d2;
                            if (next[i][j][0] == -1) {
                                numPath++;
                                a = i;
                                b = j;
                                do {
                                    set(path2aArr[inLineIdx][0], sortBuf[a][b]);
                                    set(path2aArr[inLineIdx][1], sortBuf[a][1 - b]);
                                    inLineIdx++;

                                    d2 = next[a][1 - b][1];
                                    c = next[a][1 - b][0];
                                    next[a][1 - b][0] = -2;
                                    next[a][b][0] = -2;
                                    b = d2;
                                    a = c;
                                } while (a != -1);
                            }
                        }
                    }

                    path2Len = inLineIdx;

                }

                for (GData2 p : shape1) {
                    shape1Arr[shape1Len][0][0] = p.x1p.doubleValue();
                    shape1Arr[shape1Len][0][1] = p.y1p.doubleValue();
                    shape1Arr[shape1Len][0][2] = p.z1p.doubleValue();
                    shape1Arr[shape1Len][1][0] = p.x2p.doubleValue();
                    shape1Arr[shape1Len][1][1] = p.y2p.doubleValue();
                    shape1Arr[shape1Len][1][2] = p.z2p.doubleValue();
                    shape1Len++;
                }

                for (GData2 p : shape2) {
                    shape2Arr[shape2Len][0][0] = p.x1p.doubleValue();
                    shape2Arr[shape2Len][0][1] = p.y1p.doubleValue();
                    shape2Arr[shape2Len][0][2] = p.z1p.doubleValue();
                    shape2Arr[shape2Len][1][0] = p.x2p.doubleValue();
                    shape2Arr[shape2Len][1][1] = p.y2p.doubleValue();
                    shape2Arr[shape2Len][1][2] = p.z2p.doubleValue();
                    shape2Len++;
                }

                if (path1Len != path2Len) {
                    // The two path files do not have the same number of elements!
                    return;
                }

                if (endings && path1Len < 3 && !circular) {
                    // Path files must have at least 3 elements to use -e option!
                    return;
                }

                if (shape1Len != shape2Len) {
                    // The two shape files do not have the same number of elements!
                    return;
                }

                // Split long lines
                inLineIdx = 0;
                for (int i = 0; i < path1Len; i++) {
                    double[] p1 = new double[3];
                    double[] p2 = new double[3];
                    double[] q1 = new double[3];
                    double[] q2 = new double[3];
                    double[] delta1 = new double[3];
                    double[] delta2 = new double[3];
                    double[] temp = new double[3];
                    int nsplit1;
                    int nsplit2;

                    set(p1, path1aArr[i][0]);
                    set(p2, path1aArr[i][1]);

                    set(q1, path2aArr[i][0]);
                    set(q2, path2aArr[i][1]);

                    nsplit1 = (int) (dist(p1, p2) / maxlength) + 1;
                    nsplit2 = (int) (dist(q1, q2) / maxlength) + 1;

                    // don't split endings segments
                    if (endings && (i == 0 || i == path1Len - 1)) {
                        nsplit1 = nsplit2 = 1;
                    }

                    nsplit1 = nsplit1 > nsplit2 ? nsplit1 : nsplit2;

                    sub(delta1, p2, p1);
                    mult(delta1, delta1, 1.0 / nsplit1);
                    sub(delta2, q2, q1);
                    mult(delta2, delta2, 1.0 / nsplit1);
                    for (int k = 0; k < nsplit1; k++) {
                        mult(temp, delta1, k);
                        add(path1Arr[inLineIdx][0], p1, temp);
                        add(path1Arr[inLineIdx][1], path1Arr[inLineIdx][0], delta1);
                        mult(temp, delta2, k);
                        add(path2Arr[inLineIdx][0], q1, temp);
                        add(path2Arr[inLineIdx][1], path2Arr[inLineIdx][0], delta2);

                        inLineIdx++;
                    }
                }

                path1Len = path2Len = inLineIdx;
                set(path1Arr[path1Len][0], path1Arr[path1Len - 1][1]);
                set(path1Arr[path1Len][1], path1Arr[path1Len - 1][0]);

                set(path2Arr[path2Len][0], path2Arr[path2Len - 1][1]);
                set(path2Arr[path2Len][1], path2Arr[path2Len - 1][0]);

                len = dist(shape1Arr[0][0], shape1Arr[0][1]);

                for (int i = 1; i < shape1Len; i++) {
                    sub(shape1Arr[i][0], shape1Arr[i][0], shape1Arr[0][0]);
                    mult(shape1Arr[i][0], shape1Arr[i][0], 1 / len);
                    sub(shape1Arr[i][1], shape1Arr[i][1], shape1Arr[0][0]);
                    mult(shape1Arr[i][1], shape1Arr[i][1], 1 / len);
                }
                sub(shape1Vect, shape1Arr[0][1], shape1Arr[0][0]);

                angle = Math.atan2(-shape1Vect[0], -shape1Vect[1]);

                sa = Math.sin(angle);
                ca = Math.cos(angle);

                for (int i = 1; i < shape1Len; i++) {
                    shape1Arr[i - 1][0][0] = shape1Arr[i][0][0] * ca - shape1Arr[i][0][1] * sa;
                    shape1Arr[i - 1][0][1] = shape1Arr[i][0][0] * sa + shape1Arr[i][0][1] * ca;
                    shape1Arr[i - 1][1][0] = shape1Arr[i][1][0] * ca - shape1Arr[i][1][1] * sa;
                    shape1Arr[i - 1][1][1] = shape1Arr[i][1][0] * sa + shape1Arr[i][1][1] * ca;
                    shape1Arr[i - 1][0][2] = shape1Arr[i][0][2];
                    shape1Arr[i - 1][1][2] = shape1Arr[i][1][2];
                    if (invert) {
                        shape1Arr[i - 1][0][0] = -shape1Arr[i - 1][0][0];
                        shape1Arr[i - 1][1][0] = -shape1Arr[i - 1][1][0];
                    }
                }
                shape1Len--;

                // Normalize shape 2

                len = dist(shape2Arr[0][0], shape2Arr[0][1]);

                for (int i = 1; i < shape2Len; i++) {
                    sub(shape2Arr[i][0], shape2Arr[i][0], shape2Arr[0][0]);
                    mult(shape2Arr[i][0], shape2Arr[i][0], 1 / len);
                    sub(shape2Arr[i][1], shape2Arr[i][1], shape2Arr[0][0]);
                    mult(shape2Arr[i][1], shape2Arr[i][1], 1 / len);
                }
                sub(shape2Vect, shape2Arr[0][1], shape2Arr[0][0]);

                angle = Math.atan2(-shape2Vect[0], -shape2Vect[1]);

                sa = Math.sin(angle);
                ca = Math.cos(angle);

                for (int i = 1; i < shape2Len; i++) {
                    shape2Arr[i - 1][0][0] = shape2Arr[i][0][0] * ca - shape2Arr[i][0][1] * sa;
                    shape2Arr[i - 1][0][1] = shape2Arr[i][0][0] * sa + shape2Arr[i][0][1] * ca;
                    shape2Arr[i - 1][1][0] = shape2Arr[i][1][0] * ca - shape2Arr[i][1][1] * sa;
                    shape2Arr[i - 1][1][1] = shape2Arr[i][1][0] * sa + shape2Arr[i][1][1] * ca;
                    shape2Arr[i - 1][0][2] = shape2Arr[i][0][2];
                    shape2Arr[i - 1][1][2] = shape2Arr[i][1][2];
                    if (invert) {
                        shape2Arr[i - 1][0][0] = -shape2Arr[i - 1][0][0];
                        shape2Arr[i - 1][1][0] = -shape2Arr[i - 1][1][0];
                    }

                }

                // Extrusion
                // Initialize current shape
                if (circular)
                    endings = false;

                if (endings) {
                    double angle2 = pathLocalBasis(0, 1, xVect, yVect, zVect, path1Arr, path2Arr);
                    angle = pathLocalBasis(0, 0, xVect, yVect, zVect, path1Arr, path2Arr);
                    if (angle2 > 90) {
                        mult(xVect, xVect, -1);
                        mult(zVect, zVect, -1);
                    }
                } else {
                    angle = pathLocalBasis(circular ? path1Len - 1 : 0, 0, xVect, yVect, zVect, path1Arr, path2Arr);
                }
                // compensate sharp angles
                if (compensate) {
                    mult(xVect, xVect, 1 / Math.cos(angle * pi / 360));
                }

                // Calculate next transformed shape
                for (int j = 0; j < shape1Len; j++) {
                    for (int k = 0; k < 2; k++) {
                        mult(nxtShape[j][k], xVect, shape1Arr[j][k][0]);
                        mult(temp1, yVect, shape1Arr[j][k][1]);
                        add(nxtShape[j][k], nxtShape[j][k], temp1);
                        mult(temp1, zVect, shape1Arr[j][k][2]);
                        add(nxtShape[j][k], nxtShape[j][k], temp1);
                        if (endings) {
                            add(nxtShape[j][k], nxtShape[j][k], path1Arr[1][0]);
                        } else {
                            add(nxtShape[j][k], nxtShape[j][k], path1Arr[0][0]);
                        }
                    }

                }

                int start;
                int end;
                start = 0;
                end = path1Len;
                if (endings) {
                    start++;
                    end--;
                }
                for (int i = start; i < end; i++) {

                    // Transfer old next shape to current.
                    for (int j = 0; j < shape1Len; j++) {
                        set(curShape[j][0], nxtShape[j][0]);
                        set(curShape[j][1], nxtShape[j][1]);
                    }

                    if (i == end - 1) {
                        if (circular) {
                            angle = pathLocalBasis(i, 0, xVect, yVect, zVect, path1Arr, path2Arr);
                        } else {
                            if (endings) {
                                double angle2 = pathLocalBasis(i, i + 1, xVect, yVect, zVect, path1Arr, path2Arr);
                                angle = pathLocalBasis(i + 2, i + 2, xVect, yVect, zVect, path1Arr, path2Arr);
                                if (angle2 < 90) {
                                    // in that case the local
                                    // base is mirrorred...
                                    sub(xVect, nullv, xVect);
                                    sub(zVect, nullv, zVect);
                                }
                            } else {
                                angle = pathLocalBasis(i + 1, i + 1, xVect, yVect, zVect, path1Arr, path2Arr);
                                // in that case the local base
                                // is mirrorred...
                                sub(xVect, nullv, xVect);
                                sub(zVect, nullv, zVect);
                            }
                        }
                    } else {
                        angle = pathLocalBasis(i, i + 1, xVect, yVect, zVect, path1Arr, path2Arr);
                    }

                    // compensate sharp angles
                    if (compensate) {
                        mult(xVect, xVect, 1 / Math.cos(angle * pi / 360));
                    }

                    {
                        double x;
                        double j = (i + 1.0 - start) * transitions % (2 * (end - start));
                        x = 1.0 * j / (end - start);
                        if (x > 1.0)
                            x = 2.0 - x;
                        ratio = sigmoid(x, slope, position);
                    }

                    double rotangle = rotation * pi / 180.0 * ((i + 1.0) / path1Len);

                    sa = Math.sin(rotangle);
                    ca = Math.cos(rotangle);

                    for (int j = 0; j < shape1Len; j++) {
                        for (int k = 0; k < 2; k++) {
                            temp1[0] = shape1Arr[j][k][0] * ca - shape1Arr[j][k][1] * sa;
                            temp1[1] = shape1Arr[j][k][0] * sa + shape1Arr[j][k][1] * ca;
                            temp2[0] = shape2Arr[j][k][0] * ca - shape2Arr[j][k][1] * sa;
                            temp2[1] = shape2Arr[j][k][0] * sa + shape2Arr[j][k][1] * ca;

                            mult(nxtShape[j][k], xVect, temp1[0] * (1.0 - ratio) + temp2[0] * ratio);
                            mult(temp3, yVect, temp1[1] * (1.0 - ratio) + temp2[1] * ratio);
                            add(nxtShape[j][k], nxtShape[j][k], temp3);
                            mult(temp3, zVect, shape1Arr[j][k][2] * (1.0 - ratio) + shape2Arr[j][k][2] * ratio);
                            add(nxtShape[j][k], nxtShape[j][k], temp3);
                            add(nxtShape[j][k], nxtShape[j][k], path1Arr[i + 1][0]);
                        }
                    }
                    // Generate tri/quad sheet
                    for (int j = 0; j < shape1Len; j++) {
                        if (dist(curShape[j][0], curShape[j][1]) < vertmerge) {
                            if (dist(nxtShape[j][0], nxtShape[j][1]) < vertmerge || dist(curShape[j][0], nxtShape[j][0]) < vertmerge || dist(nxtShape[j][1], curShape[j][1]) < vertmerge) {
                                // Degenerated. Nothing to output
                                continue;
                            } else {
                                Vertex v1 = new Vertex(new BigDecimal(curShape[j][0][0]), new BigDecimal(curShape[j][0][1]), new BigDecimal(curShape[j][0][2]));
                                Vertex v2 = new Vertex(new BigDecimal(nxtShape[j][1][0]), new BigDecimal(nxtShape[j][1][1]), new BigDecimal(nxtShape[j][1][2]));
                                Vertex v3 = new Vertex(new BigDecimal(nxtShape[j][0][0]), new BigDecimal(nxtShape[j][0][1]), new BigDecimal(nxtShape[j][0][2]));
                                newTriangles.add(new GData3(v1, v2, v3, View.DUMMY_REFERENCE, bodyColour, true));
                                continue;
                            }
                        }
                        if (dist(nxtShape[j][0], nxtShape[j][1]) < vertmerge) {
                            if (dist(curShape[j][0], nxtShape[j][0]) < vertmerge || dist(nxtShape[j][1], curShape[j][1]) < vertmerge) {
                                // Degenerated. Nothing to output
                                continue;
                            } else {
                                Vertex v1 = new Vertex(new BigDecimal(curShape[j][0][0]), new BigDecimal(curShape[j][0][1]), new BigDecimal(curShape[j][0][2]));
                                Vertex v2 = new Vertex(new BigDecimal(curShape[j][1][0]), new BigDecimal(curShape[j][1][1]), new BigDecimal(curShape[j][1][2]));
                                Vertex v3 = new Vertex(new BigDecimal(nxtShape[j][0][0]), new BigDecimal(nxtShape[j][0][1]), new BigDecimal(nxtShape[j][0][2]));
                                newTriangles.add(new GData3(v1, v2, v3, View.DUMMY_REFERENCE, bodyColour, true));
                                continue;
                            }
                        }
                        if (dist(curShape[j][0], nxtShape[j][0]) < vertmerge) {
                            if (dist(nxtShape[j][1], curShape[j][1]) < vertmerge) {
                                // Degenerated. Nothing to output
                                continue;
                            } else {
                                Vertex v1 = new Vertex(new BigDecimal(curShape[j][0][0]), new BigDecimal(curShape[j][0][1]), new BigDecimal(curShape[j][0][2]));
                                Vertex v2 = new Vertex(new BigDecimal(curShape[j][1][0]), new BigDecimal(curShape[j][1][1]), new BigDecimal(curShape[j][1][2]));
                                Vertex v3 = new Vertex(new BigDecimal(nxtShape[j][1][0]), new BigDecimal(nxtShape[j][1][1]), new BigDecimal(nxtShape[j][1][2]));
                                newTriangles.add(new GData3(v1, v2, v3, View.DUMMY_REFERENCE, bodyColour, true));
                                continue;
                            }
                        }
                        if (dist(nxtShape[j][1], curShape[j][1]) < vertmerge) {
                            Vertex v1 = new Vertex(new BigDecimal(curShape[j][0][0]), new BigDecimal(curShape[j][0][1]), new BigDecimal(curShape[j][0][2]));
                            Vertex v2 = new Vertex(new BigDecimal(curShape[j][1][0]), new BigDecimal(curShape[j][1][1]), new BigDecimal(curShape[j][1][2]));
                            Vertex v3 = new Vertex(new BigDecimal(nxtShape[j][0][0]), new BigDecimal(nxtShape[j][0][1]), new BigDecimal(nxtShape[j][0][2]));
                            newTriangles.add(new GData3(v1, v2, v3, View.DUMMY_REFERENCE, bodyColour, true));
                            continue;
                        }
                        if (triAngle(curShape[j][0], nxtShape[j][0], nxtShape[j][1], curShape[j][1]) < smallAngle) {
                            Vertex v1 = new Vertex(new BigDecimal(curShape[j][0][0]), new BigDecimal(curShape[j][0][1]), new BigDecimal(curShape[j][0][2]));
                            Vertex v2 = new Vertex(new BigDecimal(curShape[j][1][0]), new BigDecimal(curShape[j][1][1]), new BigDecimal(curShape[j][1][2]));
                            Vertex v3 = new Vertex(new BigDecimal(nxtShape[j][1][0]), new BigDecimal(nxtShape[j][1][1]), new BigDecimal(nxtShape[j][1][2]));
                            Vertex v4 = new Vertex(new BigDecimal(nxtShape[j][0][0]), new BigDecimal(nxtShape[j][0][1]), new BigDecimal(nxtShape[j][0][2]));
                            newQuads.add(new GData4(v1, v2, v3, v4, View.DUMMY_REFERENCE, bodyColour));
                        } else {
                            {
                                Vertex v1 = new Vertex(new BigDecimal(curShape[j][0][0]), new BigDecimal(curShape[j][0][1]), new BigDecimal(curShape[j][0][2]));
                                Vertex v2 = new Vertex(new BigDecimal(nxtShape[j][1][0]), new BigDecimal(nxtShape[j][1][1]), new BigDecimal(nxtShape[j][1][2]));
                                Vertex v3 = new Vertex(new BigDecimal(nxtShape[j][0][0]), new BigDecimal(nxtShape[j][0][1]), new BigDecimal(nxtShape[j][0][2]));
                                newTriangles.add(new GData3(v1, v2, v3, View.DUMMY_REFERENCE, bodyColour, true));
                            }
                            {
                                Vertex v1 = new Vertex(new BigDecimal(curShape[j][0][0]), new BigDecimal(curShape[j][0][1]), new BigDecimal(curShape[j][0][2]));
                                Vertex v2 = new Vertex(new BigDecimal(curShape[j][1][0]), new BigDecimal(curShape[j][1][1]), new BigDecimal(curShape[j][1][2]));
                                Vertex v3 = new Vertex(new BigDecimal(nxtShape[j][1][0]), new BigDecimal(nxtShape[j][1][1]), new BigDecimal(nxtShape[j][1][2]));
                                newTriangles.add(new GData3(v1, v2, v3, View.DUMMY_REFERENCE, bodyColour, true));
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
        }

        if (!syncWithEditor) {
            sl.clear();
            sl.addAll(newTriangles);
            sl.addAll(newQuads);
            return;
        }

        if (originalSelection.isEmpty()) {
            return;
        }

        NLogger.debug(getClass(), "Check for identical vertices and collinearity."); //$NON-NLS-1$
        final Set<GData2> linesToDelete2 = new HashSet<>();
        final Set<GData3> trisToDelete2 = new HashSet<>();
        final Set<GData4> quadsToDelete2 = new HashSet<>();
        {
            for (GData2 g2 : newLines) {
                Vertex[] verts = lines.get(g2);
                SortedSet<Vertex> verts2 = new TreeSet<>();
                verts2.addAll(Arrays.asList(verts));
                if (verts2.size() < 2) {
                    linesToDelete2.add(g2);
                }
            }
            for (GData3 g3 : newTriangles) {
                Vertex[] verts = triangles.get(g3);
                SortedSet<Vertex> verts2 = new TreeSet<>();
                verts2.addAll(Arrays.asList(verts));
                if (verts2.size() < 3 || g3.isCollinear()) {
                    trisToDelete2.add(g3);
                }
            }
            for (GData4 g4 : newQuads) {
                Vertex[] verts = quads.get(g4);
                SortedSet<Vertex> verts2 = new TreeSet<>();
                verts2.addAll(Arrays.asList(verts));
                if (verts2.size() < 4 || g4.isCollinear()) {
                    quadsToDelete2.add(g4);
                }
            }
        }

        // Append the new data
        for (GData2 line : newLines) {
            linkedDatFile.addToTailOrInsertAfterCursor(line);
        }
        for (GData3 tri : newTriangles) {
            linkedDatFile.addToTailOrInsertAfterCursor(tri);
        }
        for (GData4 quad : newQuads) {
            linkedDatFile.addToTailOrInsertAfterCursor(quad);
        }

        NLogger.debug(getClass(), "Delete new, but invalid objects."); //$NON-NLS-1$

        newLines.removeAll(linesToDelete2);
        newTriangles.removeAll(trisToDelete2);
        newQuads.removeAll(quadsToDelete2);
        selectedLines.addAll(linesToDelete2);
        selectedTriangles.addAll(trisToDelete2);
        selectedQuads.addAll(quadsToDelete2);
        selectedData.addAll(selectedLines);
        selectedData.addAll(selectedTriangles);
        selectedData.addAll(selectedQuads);
        delete(false, false);

        // Round to 6 decimal places

        selectedLines.addAll(newLines);
        selectedTriangles.addAll(newTriangles);
        selectedQuads.addAll(newQuads);
        selectedData.addAll(selectedLines);
        selectedData.addAll(selectedTriangles);
        selectedData.addAll(selectedQuads);

        NLogger.debug(getClass(), "Round."); //$NON-NLS-1$
        roundSelection(6, 10, true, false, true, true, true);

        if (syncWithEditor) {
            setModified(true, true);
            validateState();
        }

        NLogger.debug(getClass(), "Done."); //$NON-NLS-1$
    }

    // Calculate scaled sigmoid function between 0 and 1.
    // 1/(1+exp(-b*(x-m))) Scaled so that sigmoid(0)=0, sigmoid(1)=1
    // b is growth rate, m is max growth rate point
    // if b=1. returns a true x (linear relationship)
    private double sigmoid(double x,double b,double m)
    {
        double s0;
        double s1;
        double y;
        if(b == 1.0) return x;
        s0 = 1.0 / (1.0 + Math.exp(b * m));
        s1 = 1.0 / (1.0 + Math.exp(-b * (1.0 - m)));
        y = 1.0 / (1.0 + Math.exp(-b * (x - m)));
        y = (y - s0) / (s1 - s0);
        return y;
    }

    private void cross(double[] dest, double[] left, double[] right) {
        dest[0]=left[1]*right[2]-left[2]*right[1];
        dest[1]=left[2]*right[0]-left[0]*right[2];
        dest[2]=left[0]*right[1]-left[1]*right[0];
    }

    private double dot(double[] v1, double[] v2) {
        return v1[0]*v2[0]+v1[1]*v2[1]+v1[2]*v2[2];
    }

    private void sub(double[] dest, double[] left, double[] right) {
        dest[0]=left[0]-right[0]; dest[1]=left[1]-right[1]; dest[2]=left[2]-right[2];
    }

    private void add(double[] dest, double[] left, double[] right) {
        dest[0]=left[0]+right[0]; dest[1]=left[1]+right[1]; dest[2]=left[2]+right[2];
    }

    private void mult(double[] dest, double[] v, double factor) {
        dest[0]=factor*v[0]; dest[1]=factor*v[1]; dest[2]=factor*v[2];
    }

    private void set(double[] dest, double[] src) {
        dest[0]=src[0]; dest[1]=src[1]; dest[2]=src[2];
    }

    private double manhattan(double[] v1, double[] v2) {
        return Math.abs(v1[0]-v2[0]) + Math.abs(v1[1]-v2[1]) + Math.abs(v1[2]-v2[2]);
    }

    private double dist(double[] v1, double[] v2) {
        return Math.sqrt((v1[0]-v2[0])*(v1[0]-v2[0]) + (v1[1]-v2[1])*(v1[1]-v2[1]) + (v1[2]-v2[2])*(v1[2]-v2[2]));
    }


    // Calculate local basis, based on the direction of the i-th vector between both paths,
    // and the average of the planes defined by the paths before and after this vector
    // Returns angle between these planes.
    private double pathLocalBasis(int n, int i,  double[] xv,double[] yv,double[] zv, double[][][] path1, double[][][] path2)
    {
        double a;
        double scale;
        double[] temp1 = new double[3];
        double[] temp2 = new double[3];
        double[] temp3 = new double[3];
        double[] temp4 = new double[3];

        // Calculate local coordinate basis
        scale = dist(path2[i][0], path1[i][0]);

        if(scale < EPSILON)
        {
            // size is 0... any non-degenerated base will do!
            set (yv, nullv);
            yv[0]=1;
        }
        else
        {
            sub(yv, path1[i][0], path2[i][0]);
        }

        // Average Path Normal
        sub(temp1, path1[i][1], path1[i][0]);
        sub(temp2, path2[i][1], path1[i][0]);
        cross(xv, temp2, temp1);
        a=dist(xv, nullv);
        if (a > EPSILON) {
            mult(xv, xv, 1.0/a);
        } else {
            set(xv, nullv);
        }
        sub(temp1, path2[i][1], path2[i][0]);
        sub(temp2, path1[i][0], path2[i][0]);
        cross(temp3, temp1, temp2);
        a=dist(temp3, nullv);
        if (a > EPSILON) {
            mult(temp3, temp3, 1.0/a);
        } else {
            set(temp3, nullv);
        }
        add(xv, xv, temp3);
        a=dist(xv, nullv);
        if (a > EPSILON) {
            mult(xv, xv, 1/a);
        } else {
            set(xv, nullv);
        }

        sub(temp1, path1[n][1], path1[n][0]);
        sub(temp2, path2[n][1], path1[n][0]);
        cross(temp4, temp2, temp1);
        a=dist(temp4, nullv);
        if(a > EPSILON) {
            mult(temp4, temp4, 1.0/a);
        } else {
            set(temp4, nullv);
        }
        sub(temp1, path2[n][1], path2[n][0]);
        sub(temp2, path1[n][0], path2[n][0]);
        cross(temp3, temp1, temp2);
        a=dist(temp3, nullv);
        if(a > EPSILON) {
            mult(temp3, temp3, 1.0/a);
        } else {
            set(temp3, nullv);
        }
        add(temp4, temp4, temp3);
        a=dist(temp4, nullv);
        if(a > EPSILON) {
            mult(temp4, temp4, 1.0/a);
        } else {
            set(temp4, nullv);
        }

        // Average previous and current path normals
        add(xv, xv, temp4);
        a=dist(xv, nullv);
        if(a > EPSILON) {
            mult(xv, xv, 1/a);
        } else {
            set(xv, nullv);
        }

        // calculate angle
        a = 360.0 / Math.PI * Math.acos(dot(xv,temp4));

        cross(zv,xv,yv);
        mult(xv, xv, scale);
        if(scale < EPSILON)
        {
            set(yv, nullv);
            set(zv, nullv);
        }
        return a;
    }

    // triAngle computes the angle (in degrees) between the planes of a quad.
    // They are assumed to be non-degenerated
    private double triAngle(double[] u0,double[] u1,double[] u2, double[] u3)
    {
        double[] unorm = new double[3];
        double[] vnorm = new double[3];
        double[] temp = new double[3];
        double[] u10 = new double[3];
        double[] u20 = new double[3];
        double[] v10 = new double[3];
        double[] v20 = new double[3];
        double len;
        sub(u10, u1, u0);
        sub(u20, u2, u0);
        sub(v10, u2, u0);
        sub(v20, u3, u0);
        cross(temp, u10, u20);
        len = dist(temp, nullv);
        mult(unorm, temp, 1/len);
        cross(temp, v10, v20);
        len = dist(temp, nullv);
        mult(vnorm, temp, 1/len);
        cross(temp, unorm, vnorm);
        double dist;
        dist = dist(temp, nullv);
        if(dist > 0.9999999999) return 90;
        return 180 / Math.PI * Math.asin(dist);
    }
}
