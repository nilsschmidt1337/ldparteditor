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
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.composite3d.PathTruderSettings;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.text.DatParser;

class VM07PathTruder extends VM06Edger2 {

    /* Null vector */
    private final double[] nullv = new double[]{0.0,0.0,0.0};
    private final double EPSILON = 0.000001;

    protected VM07PathTruder(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public void pathTruder(final PathTruderSettings ps) {
        if (linkedDatFile.isReadOnly()) return;

        final Set<GData2> originalSelection = new HashSet<GData2>();
        final Set<GData2> newLines = new HashSet<GData2>();
        final Set<GData3> newTriangles = new HashSet<GData3>();
        final Set<GData4> newQuads = new HashSet<GData4>();

        final ArrayList<GData2> shape1 = new ArrayList<GData2>();
        final ArrayList<GData2> shape2 = new ArrayList<GData2>();

        final ArrayList<GData2> path1 = new ArrayList<GData2>();
        final ArrayList<GData2> path2 = new ArrayList<GData2>();

        final ArrayList<GData2> path1endSegments = new ArrayList<GData2>();
        final ArrayList<GData2> path2endSegments = new ArrayList<GData2>();

        final ArrayList<GData2> lineIndicators = new ArrayList<GData2>();

        originalSelection.addAll(selectedLines);

        // Validate and evaluate selection
        {
            final GData2 shape1Normal;
            final GData2 shape2Normal;
            GData2 shape1Normal2 = null;
            GData2 shape2Normal2 = null;
            GData data2draw = linkedDatFile.getDrawChainStart();
            while ((data2draw = data2draw.getNext()) != null) {
                if (originalSelection.contains(data2draw)) {
                    GData2 line = (GData2) data2draw;
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
                Set<Vertex> liVerts = new TreeSet<Vertex>();
                for (GData2 ind : lineIndicators) {
                    Vertex[] verts = lines.get(ind);
                    liVerts.add(verts[0]);
                    liVerts.add(verts[1]);
                }

                Set<Integer> indices = new HashSet<Integer>();

                // Shape 1
                {
                    int ss = shape1.size();
                    final ArrayList<GData2> shapeTmp = new ArrayList<GData2>(ss);
                    int ssm = ss - 1;
                    for (int i = 0; i < ss; i++) {
                        Vertex[] verts = lines.get(shape1.get(i));
                        if (i == 0) {
                            if (liVerts.contains(verts[0])) {
                                shapeTmp.add(new GData2(verts[0], verts[0], View.DUMMY_REFERENCE, new GColour()));
                                indices.add(i);
                            }
                            shapeTmp.add(shape1.get(i));
                        } else if (i == ssm) {
                            shapeTmp.add(shape1.get(i));
                            if (liVerts.contains(verts[1])) {
                                shapeTmp.add(new GData2(verts[1], verts[1], View.DUMMY_REFERENCE, new GColour()));
                                indices.add(i);
                            }
                        } else {
                            Vertex[] verts2 = lines.get(shape1.get(i - 1));
                            if (verts2[1].equals(verts[0]) && liVerts.contains(verts[0])) {
                                shapeTmp.add(new GData2(verts[0], verts[0], View.DUMMY_REFERENCE, new GColour()));
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
                    final ArrayList<GData2> shapeTmp = new ArrayList<GData2>(ss);
                    int ssm = ss - 1;
                    for (int i = 0; i < ss; i++) {
                        Vertex[] verts = lines.get(shape2.get(i));
                        if (i == 0) {
                            if (indices.contains(i)) {
                                shapeTmp.add(new GData2(verts[0], verts[0], View.DUMMY_REFERENCE, new GColour()));
                            }
                            shapeTmp.add(shape2.get(i));
                        } else if (i == ssm) {
                            shapeTmp.add(shape2.get(i));
                            if (indices.contains(i)) {
                                shapeTmp.add(new GData2(verts[1], verts[1], View.DUMMY_REFERENCE, new GColour()));
                            }
                        } else {
                            if (indices.contains(i)) {
                                shapeTmp.add(new GData2(verts[0], verts[0], View.DUMMY_REFERENCE, new GColour()));
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

        // Clear selection
        clearSelection();

        try {
            new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress() {
                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    try {
                        monitor.beginTask(I18n.VM_PathTruder, IProgressMonitor.UNKNOWN);

                        final Thread[] threads = new Thread[1];
                        threads[0] = new Thread(new Runnable() {
                            @Override
                            public void run() {

                                if (monitor.isCanceled()) {
                                    return;
                                }

                                final GColour lineColour = DatParser.validateColour(24, .5f, .5f, .5f, 1f).clone();
                                final GColour bodyColour = DatParser.validateColour(16, .5f, .5f, .5f, 1f).clone();

                                double VERTMERGE = 0.001;
                                double PI = 3.14159265358979323846;

                                int MAX_LINE = 1000;
                                double SMALL = 0.1;
                                double SMALLANGLE = .95;

                                double[][][] Path1 = new double[5 * MAX_LINE][2][3];
                                double[][][] Path2 = new double[5 * MAX_LINE][2][3];
                                double[][][] Path1a = new double[MAX_LINE][2][3];
                                double[][][] Path2a = new double[MAX_LINE][2][3];
                                /** [lineIndex][pointIndex][coordinateIndex] */
                                double[][][] Shape1 = new double[MAX_LINE][2][3];
                                /** [lineIndex][pointIndex][coordinateIndex] */
                                double[][][] Shape2 = new double[MAX_LINE][2][3];
                                double[][][] CurShape = new double[MAX_LINE][2][3];
                                double[][][] NxtShape = new double[MAX_LINE][2][3];
                                double[] Shape1Vect = new double[3], Shape2Vect = new double[3];

                                double[] temp1 = new double[3], temp2 = new double[3], temp3 = new double[3];
                                double[] XVect = new double[3], YVect = new double[3], ZVect = new double[3];

                                double Angle, ca, sa;
                                double ratio;
                                double[][][] SortBuf = new double[MAX_LINE][2][3];
                                int[][][] next = new int[MAX_LINE][2][2];

                                int Path1Len = 0;
                                int Path2Len = 0;
                                int Shape1Len = 0;
                                int Shape2Len = 0;

                                boolean circular = false;
                                double maxlength = ps.getMaxPathSegmentLength().doubleValue();
                                double dmax, d = 0.0;
                                double len;
                                int InLineIdx;
                                int NumPath;
                                boolean invert = ps.isInverted();

                                int transitions = ps.getTransitionCount();
                                double slope = ps.getTransitionCurveControl().doubleValue();
                                double position = ps.getTransitionCurveCenter().doubleValue();
                                double crease = ps.getPathAngleForLine().doubleValue();
                                boolean compensate = ps.isCompensation();
                                boolean endings = path1endSegments.size() == 2 && path2endSegments.size() == 2;
                                double rotation = ps.getRotation().doubleValue();

                                {
                                    // printf("Read path file 1\n"); //$NON-NLS-1$
                                    if (endings) {
                                        path1.add(0, path1endSegments.get(0));
                                        path1.add(path1endSegments.get(1));
                                    }
                                    for (GData2 p : path1) {
                                        SortBuf[Path1Len][0][0] = p.X1.doubleValue();
                                        SortBuf[Path1Len][0][1] = p.Y1.doubleValue();
                                        SortBuf[Path1Len][0][2] = p.Z1.doubleValue();
                                        SortBuf[Path1Len][1][0] = p.X2.doubleValue();
                                        SortBuf[Path1Len][1][1] = p.Y2.doubleValue();
                                        SortBuf[Path1Len][1][2] = p.Z2.doubleValue();
                                        next[Path1Len][0][0] = next[Path1Len][1][0] = -1;
                                        Path1Len++;
                                    }
                                    // printf("Sort path file 1\n"); //$NON-NLS-1$
                                    circular = true;
                                    for (int i = 0; i < Path1Len; i++) {
                                        for (int j = 0; j < 2; j++) {
                                            if (next[i][j][0] != -1)
                                                break;
                                            dmax = 10000000;
                                            for (int k = 0; k < Path1Len; k++) {
                                                if (k != i) {
                                                    for (int l = 0; l < 2; l++) {
                                                        d = MANHATTAN(SortBuf[i][j], SortBuf[k][l]);
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
                                            if (dmax > SMALL) {
                                                next[i][j][0] = -1;
                                                circular = false;
                                            }
                                        }
                                    }
                                    if (circular) {
                                        next[next[0][0][0]][next[0][0][1]][0] = -1;
                                        next[0][0][0] = -1;
                                    }
                                    InLineIdx = 0;
                                    NumPath = 0;
                                    for (int i = 0; i < Path1Len; i++) {
                                        for (int j = 0; j < 2; j++) {
                                            int a, b, c, d2;
                                            if (next[i][j][0] == -1) {
                                                NumPath++;
                                                a = i;
                                                b = j;
                                                do {
                                                    SET(Path1a[InLineIdx][0], SortBuf[a][b]);
                                                    SET(Path1a[InLineIdx][1], SortBuf[a][1 - b]);
                                                    InLineIdx++;

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

                                    Path1Len = InLineIdx;

                                    if (NumPath > 1) {
                                        //   printf("%d distinct paths found in Path file 1. Unexpected results may happen!\n" + NumPath); //$NON-NLS-1$
                                    }
                                }
                                {
                                    // printf("Read path file 2\n"); //$NON-NLS-1$
                                    if (endings) {
                                        path2.add(0, path2endSegments.get(0));
                                        path2.add(path2endSegments.get(1));
                                    }
                                    for (GData2 p : path2) {
                                        SortBuf[Path2Len][0][0] = p.X1.doubleValue();
                                        SortBuf[Path2Len][0][1] = p.Y1.doubleValue();
                                        SortBuf[Path2Len][0][2] = p.Z1.doubleValue();
                                        SortBuf[Path2Len][1][0] = p.X2.doubleValue();
                                        SortBuf[Path2Len][1][1] = p.Y2.doubleValue();
                                        SortBuf[Path2Len][1][2] = p.Z2.doubleValue();
                                        next[Path2Len][0][0] = next[Path2Len][1][0] = -1;
                                        Path2Len++;
                                    }
                                    // printf("Sort path file 2\n"); //$NON-NLS-1$
                                    circular = true;
                                    for (int i = 0; i < Path2Len; i++) {
                                        for (int j = 0; j < 2; j++) {
                                            if (next[i][j][0] != -1)
                                                break;
                                            dmax = 10000000;
                                            for (int k = 0; k < Path2Len; k++) {
                                                if (k != i) {
                                                    for (int l = 0; l < 2; l++) {
                                                        d = MANHATTAN(SortBuf[i][j], SortBuf[k][l]);
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
                                            if (dmax > SMALL) {
                                                next[i][j][0] = -1;
                                                circular = false;
                                            }
                                        }
                                    }
                                    if (circular) {
                                        next[next[0][0][0]][next[0][0][1]][0] = -1;
                                        next[0][0][0] = -1;
                                    }
                                    InLineIdx = 0;
                                    NumPath = 0;
                                    for (int i = 0; i < Path2Len; i++) {
                                        for (int j = 0; j < 2; j++) {
                                            int a, b, c, d2;
                                            if (next[i][j][0] == -1) {
                                                NumPath++;
                                                a = i;
                                                b = j;
                                                do {
                                                    SET(Path2a[InLineIdx][0], SortBuf[a][b]);
                                                    SET(Path2a[InLineIdx][1], SortBuf[a][1 - b]);
                                                    InLineIdx++;

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

                                    Path2Len = InLineIdx;

                                    // if (NumPath > 1)
                                    //    printf("%d distinct paths found in Path file 2. Unexpected results may happen!\n" + NumPath); //$NON-NLS-1$
                                }
                                // printf("Read shape file 1\n"); //$NON-NLS-1$
                                for (GData2 p : shape1) {
                                    Shape1[Shape1Len][0][0] = p.X1.doubleValue();
                                    Shape1[Shape1Len][0][1] = p.Y1.doubleValue();
                                    Shape1[Shape1Len][0][2] = p.Z1.doubleValue();
                                    Shape1[Shape1Len][1][0] = p.X2.doubleValue();
                                    Shape1[Shape1Len][1][1] = p.Y2.doubleValue();
                                    Shape1[Shape1Len][1][2] = p.Z2.doubleValue();
                                    Shape1Len++;
                                }
                                // printf("Read shape file 2\n"); //$NON-NLS-1$
                                for (GData2 p : shape2) {
                                    Shape2[Shape2Len][0][0] = p.X1.doubleValue();
                                    Shape2[Shape2Len][0][1] = p.Y1.doubleValue();
                                    Shape2[Shape2Len][0][2] = p.Z1.doubleValue();
                                    Shape2[Shape2Len][1][0] = p.X2.doubleValue();
                                    Shape2[Shape2Len][1][1] = p.Y2.doubleValue();
                                    Shape2[Shape2Len][1][2] = p.Z2.doubleValue();
                                    Shape2Len++;
                                }

                                if (Path1Len != Path2Len) {
                                    // printf("The two path files do not have the same number of elements!\n"); //$NON-NLS-1$
                                    return;
                                }

                                if (endings && Path1Len < 3 && !circular) {
                                    // printf("Path files must have at least 3 elements to use -e option!\n"); //$NON-NLS-1$
                                    return;
                                }

                                if (Shape1Len != Shape2Len) {
                                    // printf("The two shape files do not have the same number of elements!\n"); //$NON-NLS-1$
                                    // printf("Press <Enter> to quit"); //$NON-NLS-1$
                                    return;
                                }

                                // Split long lines
                                InLineIdx = 0;
                                for (int i = 0; i < Path1Len; i++) {
                                    double[] p1 = new double[3], p2 = new double[3], q1 = new double[3], q2 = new double[3], delta1 = new double[3], delta2 = new double[3], temp = new double[3];
                                    int nsplit1, nsplit2;

                                    SET(p1, Path1a[i][0]);
                                    SET(p2, Path1a[i][1]);

                                    SET(q1, Path2a[i][0]);
                                    SET(q2, Path2a[i][1]);

                                    nsplit1 = (int) (DIST(p1, p2) / maxlength) + 1;
                                    nsplit2 = (int) (DIST(q1, q2) / maxlength) + 1;

                                    // don't split endings segments
                                    if (endings) {
                                        if (i == 0 || i == Path1Len - 1)
                                            nsplit1 = nsplit2 = 1;
                                    }

                                    nsplit1 = nsplit1 > nsplit2 ? nsplit1 : nsplit2;

                                    SUB(delta1, p2, p1);
                                    MULT(delta1, delta1, 1.0 / nsplit1);
                                    SUB(delta2, q2, q1);
                                    MULT(delta2, delta2, 1.0 / nsplit1);
                                    for (int k = 0; k < nsplit1; k++) {
                                        MULT(temp, delta1, k);
                                        ADD(Path1[InLineIdx][0], p1, temp);
                                        ADD(Path1[InLineIdx][1], Path1[InLineIdx][0], delta1);
                                        MULT(temp, delta2, k);
                                        ADD(Path2[InLineIdx][0], q1, temp);
                                        ADD(Path2[InLineIdx][1], Path2[InLineIdx][0], delta2);

                                        InLineIdx++;
                                    }
                                }

                                Path1Len = Path2Len = InLineIdx;
                                SET(Path1[Path1Len][0], Path1[Path1Len - 1][1]);
                                SET(Path1[Path1Len][1], Path1[Path1Len - 1][0]);

                                SET(Path2[Path2Len][0], Path2[Path2Len - 1][1]);
                                SET(Path2[Path2Len][1], Path2[Path2Len - 1][0]);

                                len = DIST(Shape1[0][0], Shape1[0][1]);

                                for (int i = 1; i < Shape1Len; i++) {
                                    SUB(Shape1[i][0], Shape1[i][0], Shape1[0][0]);
                                    MULT(Shape1[i][0], Shape1[i][0], 1 / len);
                                    SUB(Shape1[i][1], Shape1[i][1], Shape1[0][0]);
                                    MULT(Shape1[i][1], Shape1[i][1], 1 / len);
                                }
                                SUB(Shape1Vect, Shape1[0][1], Shape1[0][0]);

                                Angle = Math.atan2(-Shape1Vect[0], -Shape1Vect[1]);

                                sa = Math.sin(Angle);
                                ca = Math.cos(Angle);

                                for (int i = 1; i < Shape1Len; i++) {
                                    Shape1[i - 1][0][0] = Shape1[i][0][0] * ca - Shape1[i][0][1] * sa;
                                    Shape1[i - 1][0][1] = Shape1[i][0][0] * sa + Shape1[i][0][1] * ca;
                                    Shape1[i - 1][1][0] = Shape1[i][1][0] * ca - Shape1[i][1][1] * sa;
                                    Shape1[i - 1][1][1] = Shape1[i][1][0] * sa + Shape1[i][1][1] * ca;
                                    Shape1[i - 1][0][2] = Shape1[i][0][2];
                                    Shape1[i - 1][1][2] = Shape1[i][1][2];
                                    if (invert) {
                                        Shape1[i - 1][0][0] = -Shape1[i - 1][0][0];
                                        Shape1[i - 1][1][0] = -Shape1[i - 1][1][0];
                                    }
                                }
                                Shape1Len--;

                                // Normalize shape 2

                                len = DIST(Shape2[0][0], Shape2[0][1]);

                                for (int i = 1; i < Shape2Len; i++) {
                                    SUB(Shape2[i][0], Shape2[i][0], Shape2[0][0]);
                                    MULT(Shape2[i][0], Shape2[i][0], 1 / len);
                                    SUB(Shape2[i][1], Shape2[i][1], Shape2[0][0]);
                                    MULT(Shape2[i][1], Shape2[i][1], 1 / len);
                                }
                                SUB(Shape2Vect, Shape2[0][1], Shape2[0][0]);

                                Angle = Math.atan2(-Shape2Vect[0], -Shape2Vect[1]);

                                sa = Math.sin(Angle);
                                ca = Math.cos(Angle);

                                for (int i = 1; i < Shape2Len; i++) {
                                    Shape2[i - 1][0][0] = Shape2[i][0][0] * ca - Shape2[i][0][1] * sa;
                                    Shape2[i - 1][0][1] = Shape2[i][0][0] * sa + Shape2[i][0][1] * ca;
                                    Shape2[i - 1][1][0] = Shape2[i][1][0] * ca - Shape2[i][1][1] * sa;
                                    Shape2[i - 1][1][1] = Shape2[i][1][0] * sa + Shape2[i][1][1] * ca;
                                    Shape2[i - 1][0][2] = Shape2[i][0][2];
                                    Shape2[i - 1][1][2] = Shape2[i][1][2];
                                    if (invert) {
                                        Shape2[i - 1][0][0] = -Shape2[i - 1][0][0];
                                        Shape2[i - 1][1][0] = -Shape2[i - 1][1][0];
                                    }

                                }
                                Shape2Len--;

                                // Extrusion
                                // Initialize current shape
                                if (circular)
                                    endings = false;

                                if (endings) {
                                    double Angle2 = PathLocalBasis(0, 1, XVect, YVect, ZVect, Path1, Path2);
                                    Angle = PathLocalBasis(0, 0, XVect, YVect, ZVect, Path1, Path2);
                                    if (Angle2 > 90) {
                                        MULT(XVect, XVect, -1);
                                        MULT(ZVect, ZVect, -1);
                                    }
                                } else {
                                    Angle = PathLocalBasis(circular ? Path1Len - 1 : 0, 0, XVect, YVect, ZVect, Path1, Path2);
                                }
                                // compensate sharp angles
                                if (compensate) {
                                    MULT(XVect, XVect, 1 / Math.cos(Angle * PI / 360));
                                }

                                // Calculate next transformed shape
                                for (int j = 0; j < Shape1Len; j++) {
                                    for (int k = 0; k < 2; k++) {
                                        MULT(NxtShape[j][k], XVect, Shape1[j][k][0]);
                                        MULT(temp1, YVect, Shape1[j][k][1]);
                                        ADD(NxtShape[j][k], NxtShape[j][k], temp1);
                                        MULT(temp1, ZVect, Shape1[j][k][2]);
                                        ADD(NxtShape[j][k], NxtShape[j][k], temp1);
                                        if (endings) {
                                            ADD(NxtShape[j][k], NxtShape[j][k], Path1[1][0]);
                                        } else {
                                            ADD(NxtShape[j][k], NxtShape[j][k], Path1[0][0]);
                                        }
                                    }

                                }
                                if (Angle > crease) {
                                    // sharp angle. Create line at junction
                                    for (int i = 0; i < Shape1Len; i++) {
                                        Vertex v1 = new Vertex(new BigDecimal(NxtShape[i][0][0]), new BigDecimal(NxtShape[i][0][1]), new BigDecimal(NxtShape[i][0][2]));
                                        Vertex v2 = new Vertex(new BigDecimal(NxtShape[i][1][0]), new BigDecimal(NxtShape[i][1][1]), new BigDecimal(NxtShape[i][1][2]));
                                        newLines.add(new GData2(lineColour.getColourNumber(), lineColour.getR(), lineColour.getG(), lineColour.getB(), lineColour.getA(), v1, v2, View.DUMMY_REFERENCE, linkedDatFile));
                                    }
                                }

                                int start, end;
                                start = 0;
                                end = Path1Len;
                                if (endings) {
                                    start++;
                                    end--;
                                }
                                for (int i = start; i < end; i++) {

                                    // Transfer old next shape to current.
                                    for (int j = 0; j < Shape1Len; j++) {
                                        SET(CurShape[j][0], NxtShape[j][0]);
                                        SET(CurShape[j][1], NxtShape[j][1]);
                                    }

                                    if (i == end - 1) {
                                        if (circular) {
                                            Angle = PathLocalBasis(i, 0, XVect, YVect, ZVect, Path1, Path2);
                                        } else {
                                            if (endings) {
                                                double Angle2 = PathLocalBasis(i, i + 1, XVect, YVect, ZVect, Path1, Path2);
                                                Angle = PathLocalBasis(i + 2, i + 2, XVect, YVect, ZVect, Path1, Path2);
                                                if (Angle2 < 90) {
                                                    // in that case the local
                                                    // base is mirrorred...
                                                    SUB(XVect, nullv, XVect);
                                                    SUB(ZVect, nullv, ZVect);
                                                }
                                            } else {
                                                Angle = PathLocalBasis(i + 1, i + 1, XVect, YVect, ZVect, Path1, Path2);
                                                // in that case the local base
                                                // is mirrorred...
                                                SUB(XVect, nullv, XVect);
                                                SUB(ZVect, nullv, ZVect);
                                            }
                                        }
                                    } else {
                                        Angle = PathLocalBasis(i, i + 1, XVect, YVect, ZVect, Path1, Path2);
                                    }

                                    // compensate sharp angles
                                    if (compensate) {
                                        MULT(XVect, XVect, 1 / Math.cos(Angle * PI / 360));
                                    }

                                    {
                                        double x;
                                        double j = (i + 1.0 - start) * transitions % (2 * (end - start));
                                        x = 1.0 * j / (end - start);
                                        if (x > 1.0)
                                            x = 2.0 - x;
                                        ratio = sigmoid(x, slope, position);
                                    }

                                    double rotangle = rotation * PI / 180.0 * ((i + 1.0) / Path1Len);

                                    sa = Math.sin(rotangle);
                                    ca = Math.cos(rotangle);

                                    for (int j = 0; j < Shape1Len; j++) {
                                        for (int k = 0; k < 2; k++) {
                                            temp1[0] = Shape1[j][k][0] * ca - Shape1[j][k][1] * sa;
                                            temp1[1] = Shape1[j][k][0] * sa + Shape1[j][k][1] * ca;
                                            temp2[0] = Shape2[j][k][0] * ca - Shape2[j][k][1] * sa;
                                            temp2[1] = Shape2[j][k][0] * sa + Shape2[j][k][1] * ca;

                                            MULT(NxtShape[j][k], XVect, temp1[0] * (1.0 - ratio) + temp2[0] * ratio);
                                            MULT(temp3, YVect, temp1[1] * (1.0 - ratio) + temp2[1] * ratio);
                                            ADD(NxtShape[j][k], NxtShape[j][k], temp3);
                                            MULT(temp3, ZVect, Shape1[j][k][2] * (1.0 - ratio) + Shape2[j][k][2] * ratio);
                                            ADD(NxtShape[j][k], NxtShape[j][k], temp3);
                                            ADD(NxtShape[j][k], NxtShape[j][k], Path1[i + 1][0]);
                                        }
                                    }
                                    if (Angle > crease) {
                                        // sharp angle. Create line at junction
                                        for (int j = 0; j < Shape1Len; j++) {
                                            Vertex v1 = new Vertex(new BigDecimal(NxtShape[j][0][0]), new BigDecimal(NxtShape[j][0][1]), new BigDecimal(NxtShape[j][0][2]));
                                            Vertex v2 = new Vertex(new BigDecimal(NxtShape[j][1][0]), new BigDecimal(NxtShape[j][1][1]), new BigDecimal(NxtShape[j][1][2]));
                                            newLines.add(new GData2(lineColour.getColourNumber(), lineColour.getR(), lineColour.getG(), lineColour.getB(), lineColour.getA(), v1, v2, View.DUMMY_REFERENCE, linkedDatFile));
                                        }
                                    }
                                    // Generate tri/quad sheet
                                    for (int j = 0; j < Shape1Len; j++) {
                                        if (!lineIndicators.isEmpty()) {
                                            if (DIST(Shape1[j][0], Shape1[j][1]) < EPSILON && DIST(Shape2[j][0], Shape2[j][1]) < EPSILON) {
                                                // Null lenth segment in shape file
                                                // -> generate line at that place
                                                Vertex v1 = new Vertex(new BigDecimal(CurShape[j][0][0]), new BigDecimal(CurShape[j][0][1]), new BigDecimal(CurShape[j][0][2]));
                                                Vertex v2 = new Vertex(new BigDecimal(NxtShape[j][0][0]), new BigDecimal(NxtShape[j][0][1]), new BigDecimal(NxtShape[j][0][2]));
                                                newLines.add(new GData2(lineColour.getColourNumber(), lineColour.getR(), lineColour.getG(), lineColour.getB(), lineColour.getA(), v1, v2, View.DUMMY_REFERENCE, linkedDatFile));
                                            }
                                        }
                                        if (DIST(CurShape[j][0], CurShape[j][1]) < VERTMERGE) {
                                            if (DIST(NxtShape[j][0], NxtShape[j][1]) < VERTMERGE || DIST(CurShape[j][0], NxtShape[j][0]) < VERTMERGE || DIST(NxtShape[j][1], CurShape[j][1]) < VERTMERGE) {
                                                // Degenerated. Nothing to
                                                // output
                                                continue;
                                            } else {
                                                Vertex v1 = new Vertex(new BigDecimal(CurShape[j][0][0]), new BigDecimal(CurShape[j][0][1]), new BigDecimal(CurShape[j][0][2]));
                                                Vertex v2 = new Vertex(new BigDecimal(NxtShape[j][1][0]), new BigDecimal(NxtShape[j][1][1]), new BigDecimal(NxtShape[j][1][2]));
                                                Vertex v3 = new Vertex(new BigDecimal(NxtShape[j][0][0]), new BigDecimal(NxtShape[j][0][1]), new BigDecimal(NxtShape[j][0][2]));
                                                newTriangles.add(new GData3(bodyColour.getColourNumber(), bodyColour.getR(), bodyColour.getG(), bodyColour.getB(), bodyColour.getA(), v1, v2, v3, View.DUMMY_REFERENCE, linkedDatFile));
                                                continue;
                                            }
                                        }
                                        if (DIST(NxtShape[j][0], NxtShape[j][1]) < VERTMERGE) {
                                            if (DIST(CurShape[j][0], NxtShape[j][0]) < VERTMERGE || DIST(NxtShape[j][1], CurShape[j][1]) < VERTMERGE) {
                                                // Degenerated. Nothing to
                                                // output
                                                continue;
                                            } else {
                                                Vertex v1 = new Vertex(new BigDecimal(CurShape[j][0][0]), new BigDecimal(CurShape[j][0][1]), new BigDecimal(CurShape[j][0][2]));
                                                Vertex v2 = new Vertex(new BigDecimal(CurShape[j][1][0]), new BigDecimal(CurShape[j][1][1]), new BigDecimal(CurShape[j][1][2]));
                                                Vertex v3 = new Vertex(new BigDecimal(NxtShape[j][0][0]), new BigDecimal(NxtShape[j][0][1]), new BigDecimal(NxtShape[j][0][2]));
                                                newTriangles.add(new GData3(bodyColour.getColourNumber(), bodyColour.getR(), bodyColour.getG(), bodyColour.getB(), bodyColour.getA(), v1, v2, v3, View.DUMMY_REFERENCE, linkedDatFile));
                                                continue;
                                            }
                                        }
                                        if (DIST(CurShape[j][0], NxtShape[j][0]) < VERTMERGE) {
                                            if (DIST(NxtShape[j][1], CurShape[j][1]) < VERTMERGE) {
                                                // Degenerated. Nothing to
                                                // output
                                                continue;
                                            } else {
                                                Vertex v1 = new Vertex(new BigDecimal(CurShape[j][0][0]), new BigDecimal(CurShape[j][0][1]), new BigDecimal(CurShape[j][0][2]));
                                                Vertex v2 = new Vertex(new BigDecimal(CurShape[j][1][0]), new BigDecimal(CurShape[j][1][1]), new BigDecimal(CurShape[j][1][2]));
                                                Vertex v3 = new Vertex(new BigDecimal(NxtShape[j][1][0]), new BigDecimal(NxtShape[j][1][1]), new BigDecimal(NxtShape[j][1][2]));
                                                newTriangles.add(new GData3(bodyColour.getColourNumber(), bodyColour.getR(), bodyColour.getG(), bodyColour.getB(), bodyColour.getA(), v1, v2, v3, View.DUMMY_REFERENCE, linkedDatFile));
                                                continue;
                                            }
                                        }
                                        if (DIST(NxtShape[j][1], CurShape[j][1]) < VERTMERGE) {
                                            Vertex v1 = new Vertex(new BigDecimal(CurShape[j][0][0]), new BigDecimal(CurShape[j][0][1]), new BigDecimal(CurShape[j][0][2]));
                                            Vertex v2 = new Vertex(new BigDecimal(CurShape[j][1][0]), new BigDecimal(CurShape[j][1][1]), new BigDecimal(CurShape[j][1][2]));
                                            Vertex v3 = new Vertex(new BigDecimal(NxtShape[j][0][0]), new BigDecimal(NxtShape[j][0][1]), new BigDecimal(NxtShape[j][0][2]));
                                            newTriangles.add(new GData3(bodyColour.getColourNumber(), bodyColour.getR(), bodyColour.getG(), bodyColour.getB(), bodyColour.getA(), v1, v2, v3, View.DUMMY_REFERENCE, linkedDatFile));
                                            continue;
                                        }
                                        if (Tri_Angle(CurShape[j][0], NxtShape[j][0], NxtShape[j][1], CurShape[j][1]) < SMALLANGLE) {
                                            Vertex v1 = new Vertex(new BigDecimal(CurShape[j][0][0]), new BigDecimal(CurShape[j][0][1]), new BigDecimal(CurShape[j][0][2]));
                                            Vertex v2 = new Vertex(new BigDecimal(CurShape[j][1][0]), new BigDecimal(CurShape[j][1][1]), new BigDecimal(CurShape[j][1][2]));
                                            Vertex v3 = new Vertex(new BigDecimal(NxtShape[j][1][0]), new BigDecimal(NxtShape[j][1][1]), new BigDecimal(NxtShape[j][1][2]));
                                            Vertex v4 = new Vertex(new BigDecimal(NxtShape[j][0][0]), new BigDecimal(NxtShape[j][0][1]), new BigDecimal(NxtShape[j][0][2]));
                                            newQuads.add(new GData4(bodyColour.getColourNumber(), bodyColour.getR(), bodyColour.getG(), bodyColour.getB(), bodyColour.getA(), v1, v2, v3, v4, View.DUMMY_REFERENCE, linkedDatFile));
                                        } else {
                                            {
                                                Vertex v1 = new Vertex(new BigDecimal(CurShape[j][0][0]), new BigDecimal(CurShape[j][0][1]), new BigDecimal(CurShape[j][0][2]));
                                                Vertex v2 = new Vertex(new BigDecimal(NxtShape[j][1][0]), new BigDecimal(NxtShape[j][1][1]), new BigDecimal(NxtShape[j][1][2]));
                                                Vertex v3 = new Vertex(new BigDecimal(NxtShape[j][0][0]), new BigDecimal(NxtShape[j][0][1]), new BigDecimal(NxtShape[j][0][2]));
                                                newTriangles.add(new GData3(bodyColour.getColourNumber(), bodyColour.getR(), bodyColour.getG(), bodyColour.getB(), bodyColour.getA(), v1, v2, v3, View.DUMMY_REFERENCE, linkedDatFile));
                                            }
                                            {
                                                Vertex v1 = new Vertex(new BigDecimal(CurShape[j][0][0]), new BigDecimal(CurShape[j][0][1]), new BigDecimal(CurShape[j][0][2]));
                                                Vertex v2 = new Vertex(new BigDecimal(CurShape[j][1][0]), new BigDecimal(CurShape[j][1][1]), new BigDecimal(CurShape[j][1][2]));
                                                Vertex v3 = new Vertex(new BigDecimal(NxtShape[j][1][0]), new BigDecimal(NxtShape[j][1][1]), new BigDecimal(NxtShape[j][1][2]));
                                                newTriangles.add(new GData3(bodyColour.getColourNumber(), bodyColour.getR(), bodyColour.getG(), bodyColour.getB(), bodyColour.getA(), v1, v2, v3, View.DUMMY_REFERENCE, linkedDatFile));
                                            }
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
                            } catch (InterruptedException e) {
                            }
                            isRunning = false;
                            if (threads[0].isAlive())
                                isRunning = true;
                        }
                        if (monitor.isCanceled()) {
                            selectedLines.addAll(originalSelection);
                            selectedData.addAll(originalSelection);
                            originalSelection.clear();
                            return;
                        }
                    } finally {
                        monitor.done();
                    }
                }
            });
        } catch (InvocationTargetException consumed) {
        } catch (InterruptedException consumed) {
        }

        if (originalSelection.isEmpty()) {
            return;
        }

        NLogger.debug(getClass(), "Check for identical vertices and collinearity."); //$NON-NLS-1$
        final Set<GData2> linesToDelete2 = new HashSet<GData2>();
        final Set<GData3> trisToDelete2 = new HashSet<GData3>();
        final Set<GData4> quadsToDelete2 = new HashSet<GData4>();
        {
            for (GData2 g2 : newLines) {
                Vertex[] verts = lines.get(g2);
                Set<Vertex> verts2 = new TreeSet<Vertex>();
                for (Vertex vert : verts) {
                    verts2.add(vert);
                }
                if (verts2.size() < 2) {
                    linesToDelete2.add(g2);
                }
            }
            for (GData3 g3 : newTriangles) {
                Vertex[] verts = triangles.get(g3);
                Set<Vertex> verts2 = new TreeSet<Vertex>();
                for (Vertex vert : verts) {
                    verts2.add(vert);
                }
                if (verts2.size() < 3 || g3.isCollinear()) {
                    trisToDelete2.add(g3);
                }
            }
            for (GData4 g4 : newQuads) {
                Vertex[] verts = quads.get(g4);
                Set<Vertex> verts2 = new TreeSet<Vertex>();
                for (Vertex vert : verts) {
                    verts2.add(vert);
                }
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
        roundSelection(6, 10, true, false);

        setModified(true, true);

        NLogger.debug(getClass(), "Done."); //$NON-NLS-1$

        validateState();

    }

    // Calculate scaled sigmoid function between 0 and 1.
    // 1/(1+exp(-b*(x-m))) Scaled so that sigmoid(0)=0, sigmoid(1)=1
    // b is growth rate, m is max growth rate point
    // if b=1. returns a true x (linear relationship)
    private double sigmoid(double x,double b,double m)
    {
        double s0, s1, y;
        if(b == 1.0) return x;
        s0 = 1.0 / (1.0 + Math.exp(b * m));
        s1 = 1.0 / (1.0 + Math.exp(-b * (1.0 - m)));
        y = 1.0 / (1.0 + Math.exp(-b * (x - m)));
        y = (y - s0) / (s1 - s0);
        return y;
    }

    private void CROSS(double[] dest, double[] left, double[] right) {
        dest[0]=left[1]*right[2]-left[2]*right[1];
        dest[1]=left[2]*right[0]-left[0]*right[2];
        dest[2]=left[0]*right[1]-left[1]*right[0];
    }

    private double DOT(double[] v1, double[] v2) {
        return v1[0]*v2[0]+v1[1]*v2[1]+v1[2]*v2[2];
    }

    private void SUB(double[] dest, double[] left, double[] right) {
        dest[0]=left[0]-right[0]; dest[1]=left[1]-right[1]; dest[2]=left[2]-right[2];
    }

    private void ADD(double[] dest, double[] left, double[] right) {
        dest[0]=left[0]+right[0]; dest[1]=left[1]+right[1]; dest[2]=left[2]+right[2];
    }

    private void MULT(double[] dest, double[] v, double factor) {
        dest[0]=factor*v[0]; dest[1]=factor*v[1]; dest[2]=factor*v[2];
    }

    private void SET(double[] dest, double[] src) {
        dest[0]=src[0]; dest[1]=src[1]; dest[2]=src[2];
    }

    private double MANHATTAN(double[] v1, double[] v2) {
        return Math.abs(v1[0]-v2[0]) + Math.abs(v1[1]-v2[1]) + Math.abs(v1[2]-v2[2]);
    }

    private double DIST(double[] v1, double[] v2) {
        return Math.sqrt((v1[0]-v2[0])*(v1[0]-v2[0]) + (v1[1]-v2[1])*(v1[1]-v2[1]) + (v1[2]-v2[2])*(v1[2]-v2[2]));
    }


    // Calculate local basis, based on the direction of the i-th vector between both paths,
    // and the average of the planes defined by the paths before and after this vector
    // Returns angle between these planes.
    private double PathLocalBasis (int n, int i,  double[] xv,double[] yv,double[] zv, double[][][] path1, double[][][] path2)
    {
        double a, scale;
        double[] temp1 = new double[3], temp2 = new double[3], temp3 = new double[3], temp4 = new double[3];

        // Calculate local coordinate basis
        scale = DIST(path2[i][0], path1[i][0]);

        if(scale < EPSILON)
        {
            // size is 0... any non-degenerated base will do!
            SET (yv, nullv);
            yv[0]=1;
        }
        else
        {
            SUB(yv, path1[i][0], path2[i][0]);
        }

        // Average Path Normal
        SUB(temp1, path1[i][1], path1[i][0]);
        SUB(temp2, path2[i][1], path1[i][0]);
        CROSS(xv, temp2, temp1);
        a=DIST(xv, nullv);
        if (a > EPSILON) {
            MULT(xv, xv, 1.0/a);
        } else {
            SET(xv, nullv);
        }
        SUB(temp1, path2[i][1], path2[i][0]);
        SUB(temp2, path1[i][0], path2[i][0]);
        CROSS(temp3, temp1, temp2);
        a=DIST(temp3, nullv);
        if (a > EPSILON) {
            MULT(temp3, temp3, 1.0/a);
        } else {
            SET(temp3, nullv);
        }
        ADD(xv, xv, temp3);
        a=DIST(xv, nullv);
        if (a > EPSILON) {
            MULT(xv, xv, 1/a);
        } else {
            SET(xv, nullv);
        }

        SUB(temp1, path1[n][1], path1[n][0]);
        SUB(temp2, path2[n][1], path1[n][0]);
        CROSS(temp4, temp2, temp1);
        a=DIST(temp4, nullv);
        if(a > EPSILON) {
            MULT(temp4, temp4, 1.0/a);
        } else {
            SET(temp4, nullv);
        }
        SUB(temp1, path2[n][1], path2[n][0]);
        SUB(temp2, path1[n][0], path2[n][0]);
        CROSS(temp3, temp1, temp2);
        a=DIST(temp3, nullv);
        if(a > EPSILON) {
            MULT(temp3, temp3, 1.0/a);
        } else {
            SET(temp3, nullv);
        }
        ADD(temp4, temp4, temp3);
        a=DIST(temp4, nullv);
        if(a > EPSILON) {
            MULT(temp4, temp4, 1.0/a);
        } else {
            SET(temp4, nullv);
        }

        // Average previous and current path normals
        ADD(xv, xv, temp4);
        a=DIST(xv, nullv);
        if(a > EPSILON) {
            MULT(xv, xv, 1/a);
        } else {
            SET(xv, nullv);
        }

        // calculate angle
        a = 360.0 / Math.PI * Math.acos(DOT(xv,temp4));

        CROSS(zv,xv,yv);
        MULT(xv, xv, scale);
        if(scale < EPSILON)
        {
            SET(yv, nullv);
            SET(zv, nullv);
        }
        return a;
    }

    // Tri_Angle computes the angle (in degrees) between the planes of a quad.
    // They are assumed to be non-degenerated
    private double Tri_Angle(double[] U0,double[] U1,double[] U2, double[] U3)
    {
        double[] Unorm = new double[3], Vnorm = new double[3];
        double[] Temp = new double[3];
        double[] U10 = new double[3], U20 = new double[3];
        double[] V10 = new double[3], V20 = new double[3];
        double len;
        SUB(U10, U1, U0);
        SUB(U20, U2, U0);
        SUB(V10, U2, U0);
        SUB(V20, U3, U0);
        CROSS(Temp, U10, U20);
        len = DIST(Temp, nullv);
        MULT(Unorm, Temp, 1/len);
        CROSS(Temp, V10, V20);
        len = DIST(Temp, nullv);
        MULT(Vnorm, Temp, 1/len);
        CROSS(Temp, Unorm, Vnorm);
        double dist;
        dist = DIST(Temp, nullv);
        if(dist > 0.9999999999) return 90;
        return 180 / Math.PI * Math.asin(dist);
    }

}
