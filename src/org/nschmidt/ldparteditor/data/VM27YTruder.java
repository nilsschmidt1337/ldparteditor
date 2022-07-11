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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.nschmidt.ldparteditor.enumtype.LDConfig;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.helper.composite3d.YTruderSettings;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.text.DatParser;

class VM27YTruder extends VM26LineIntersector {

    private static final double EPSILON = 0.000001;
    private static final double SMALL = 0.01;
    private double[] nullv = new double[] { 0.0, 0.0, 0.0 };
    private static final int X_AXIS = 0;
    private static final int Y_AXIS = 1;
    private static final int Z_AXIS = 2;

    protected VM27YTruder(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    @SuppressWarnings("java:S2111")
    public void yTruder(YTruderSettings ys) {
        if (linkedDatFile.isReadOnly())
            return;

        final double distance = ys.getDistance();
        int mode = ys.getMode();
        if (distance == 0 && (mode == YTruderSettings.MODE_TRANSLATE_BY_DISTANCE || mode == YTruderSettings.MODE_EXTRUDE_RADIALLY))
            return;

        final Set<GData2> originalSelection = new HashSet<>();
        originalSelection.addAll(selectedLines);
        if (originalSelection.isEmpty())
            return;

        final Set<GData2> newLines = new HashSet<>();
        final Set<GData3> newTriangles = new HashSet<>();
        final Set<GData4> newQuads = new HashSet<>();
        final Set<GData5> newCondlines = new HashSet<>();

        final GColour col16 = LDConfig.getColour16();
        final GColour lineColour = DatParser.validateColour(24, .5f, .5f, .5f, 1f).createClone();
        final GColour bodyColour = DatParser.validateColour(16, col16.getR(), col16.getG(), col16.getB(), 1f).createClone();

        final int maxLine = originalSelection.size() * 3;
        final int maxTri = originalSelection.size() * 3;
        double[][][] inLine = new double[maxLine][2][3];
        int[] lineUsed = new int[maxLine];
        double[][][] surf = new double[maxTri][4][3];
        double[][][] condLine = new double[maxTri][4][3];
        int[] condFlag = new int[maxTri];

        int numSurf;
        int numCond;

        int x = 0;
        int y = 1;
        int z = 2;

        double angleLineThr = ys.getCondlineAngleThreshold();

        int end;
        int current;
        int surfstart;

        boolean flag = false;

        if (ys.getAxis() == X_AXIS) {
            x = 1;
            y = 0;
            z = 2;
        } else if (ys.getAxis() == Y_AXIS) {
            x = 0;
            y = 1;
            z = 2;
        } else if (ys.getAxis() == Z_AXIS) {
            x = 0;
            y = 2;
            z = 1;
        }

        int originalLineCount = 0;
        for (GData2 gData2 : originalSelection) {
            Vertex[] verts = lines.get(gData2);
            if (verts != null) {
                inLine[originalLineCount][0][x] = verts[0].xp.doubleValue();
                inLine[originalLineCount][0][y] = verts[0].yp.doubleValue();
                inLine[originalLineCount][0][z] = verts[0].zp.doubleValue();
                inLine[originalLineCount][1][x] = verts[1].xp.doubleValue();
                inLine[originalLineCount][1][y] = verts[1].yp.doubleValue();
                inLine[originalLineCount][1][z] = verts[1].zp.doubleValue();
                lineUsed[originalLineCount] = 0;
                originalLineCount++;
            }
        }

        // Extruding...

        numSurf = 0;
        numCond = 0;
        condFlag[numCond] = 0;
        for (int i = 0; i < originalLineCount; i++) {
            double[] p0 = new double[3];
            double[] p1 = new double[3];
            double d0;
            double d1;
            if (lineUsed[i] == 0) {
                lineUsed[i] = 1;
                current = i;
                end = 0;
                do {
                    flag = false;
                    for (int j = 0; j < originalLineCount; j++) {
                        if (lineUsed[j] == 0) {
                            for (int k = 0; k < 2; k++) {
                                if (manhattan(inLine[current][end], inLine[j][k]) < SMALL) {
                                    current = j;
                                    end = 1 - k;
                                    lineUsed[current] = 1;
                                    flag = true;
                                    break;
                                }
                            }
                        }
                        if (flag)
                            break;
                    }
                } while (flag);

                end = 1 - end;
                surfstart = numSurf;
                set(surf[numSurf][0], inLine[current][1 - end]);
                set(surf[numSurf][1], inLine[current][end]);
                set(surf[numSurf][2], inLine[current][end]);
                set(surf[numSurf][3], inLine[current][1 - end]);
                switch (mode) {
                case YTruderSettings.MODE_TRANSLATE_BY_DISTANCE:
                    surf[numSurf][2][1] = surf[numSurf][2][1] + distance;
                    surf[numSurf][3][1] = surf[numSurf][3][1] + distance;
                    break;
                case YTruderSettings.MODE_SYMMETRY_ACROSS_PLANE:
                    surf[numSurf][2][1] = 2 * distance - surf[numSurf][2][1];
                    surf[numSurf][3][1] = 2 * distance - surf[numSurf][3][1];
                    break;
                case YTruderSettings.MODE_PROJECTION_ON_PLANE:
                    surf[numSurf][2][1] = distance;
                    surf[numSurf][3][1] = distance;
                    break;
                case YTruderSettings.MODE_EXTRUDE_RADIALLY:
                    p0[0] = 0;
                    p0[1] = surf[numSurf][0][1];
                    p0[2] = 0;
                    p1[0] = 0;
                    p1[1] = surf[numSurf][1][1];
                    p1[2] = 0;
                    d0 = dist(p0, surf[numSurf][0]);
                    d1 = dist(p1, surf[numSurf][1]);
                    if (d0 > EPSILON) {
                        surf[numSurf][3][0] = surf[numSurf][3][0] * (d0 + distance) / d0;
                        surf[numSurf][3][2] = surf[numSurf][3][2] * (d0 + distance) / d0;
                    }
                    if (d1 > EPSILON) {
                        surf[numSurf][2][0] = surf[numSurf][2][0] * (d1 + distance) / d1;
                        surf[numSurf][2][2] = surf[numSurf][2][2] * (d1 + distance) / d1;
                    }
                    double a;
                    a = triAngle(surf[numSurf][0], surf[numSurf][1], surf[numSurf][2], surf[numSurf][0], surf[numSurf][2], surf[numSurf][3]);
                    if (a > 0.5) {
                        set(condLine[numCond][0], surf[numSurf][0]);
                        set(condLine[numCond][1], surf[numSurf][2]);
                        set(condLine[numCond][2], surf[numSurf][1]);
                        set(condLine[numCond][3], surf[numSurf][3]);
                        condFlag[numCond] = 5;
                        numCond++;
                    }
                    break;
                default:
                    break;
                }
                numSurf++;
                lineUsed[current] = 2;

                do {
                    flag = false;
                    for (int j = 0; j < originalLineCount; j++) {
                        if (lineUsed[j] < 2) {
                            for (int k = 0; k < 2; k++) {
                                if (manhattan(inLine[current][end], inLine[j][k]) < SMALL && lineUsed[j] < 2) {
                                    current = j;
                                    end = 1 - k;
                                    flag = true;
                                    set(surf[numSurf][0], inLine[current][1 - end]);
                                    set(surf[numSurf][1], inLine[current][end]);
                                    set(surf[numSurf][2], inLine[current][end]);
                                    set(surf[numSurf][3], inLine[current][1 - end]);
                                    switch (mode) {
                                    case YTruderSettings.MODE_TRANSLATE_BY_DISTANCE:
                                        surf[numSurf][2][1] = surf[numSurf][2][1] + distance;
                                        surf[numSurf][3][1] = surf[numSurf][3][1] + distance;
                                        break;
                                    case YTruderSettings.MODE_SYMMETRY_ACROSS_PLANE:
                                        surf[numSurf][2][1] = 2 * distance - surf[numSurf][2][1];
                                        surf[numSurf][3][1] = 2 * distance - surf[numSurf][3][1];
                                        break;
                                    case YTruderSettings.MODE_PROJECTION_ON_PLANE:
                                        surf[numSurf][2][1] = distance;
                                        surf[numSurf][3][1] = distance;
                                        break;
                                    case YTruderSettings.MODE_EXTRUDE_RADIALLY:
                                        p0[0] = 0;
                                        p0[1] = surf[numSurf][0][1];
                                        p0[2] = 0;
                                        p1[0] = 0;
                                        p1[1] = surf[numSurf][1][1];
                                        p1[2] = 0;
                                        d0 = dist(p0, surf[numSurf][0]);
                                        d1 = dist(p1, surf[numSurf][1]);
                                        if (d0 > EPSILON) {
                                            surf[numSurf][3][0] = surf[numSurf][3][0] * (d0 + distance) / d0;
                                            surf[numSurf][3][2] = surf[numSurf][3][2] * (d0 + distance) / d0;
                                        }
                                        if (d1 > EPSILON) {
                                            surf[numSurf][2][0] = surf[numSurf][2][0] * (d1 + distance) / d1;
                                            surf[numSurf][2][2] = surf[numSurf][2][2] * (d1 + distance) / d1;
                                        }

                                        set(condLine[numCond][0], surf[numSurf][0]);
                                        set(condLine[numCond][1], surf[numSurf][2]);
                                        set(condLine[numCond][2], surf[numSurf][1]);
                                        set(condLine[numCond][3], surf[numSurf][3]);
                                        condFlag[numCond] = 5;
                                        numCond++;

                                        break;
                                    default:
                                        break;
                                    }
                                    set(condLine[numCond][0], surf[numSurf][0]);
                                    set(condLine[numCond][1], surf[numSurf][3]);
                                    set(condLine[numCond][2], surf[numSurf][1]);
                                    set(condLine[numCond][3], surf[numSurf - 1][0]);
                                    condFlag[numCond] = 5;
                                    numSurf++;
                                    numCond++;
                                    lineUsed[current] = 2;
                                }
                                if (flag)
                                    break;
                            }
                        }
                        if (flag)
                            break;
                    }
                } while (flag);
                if (manhattan(surf[numSurf - 1][1], surf[surfstart][0]) < SMALL) {
                    set(condLine[numCond][0], surf[numSurf - 1][1]);
                    set(condLine[numCond][1], surf[numSurf - 1][2]);
                    set(condLine[numCond][2], surf[numSurf - 1][0]);
                    set(condLine[numCond][3], surf[surfstart][1]);
                    condFlag[numCond] = 5;
                    numCond++;
                } else {
                    set(condLine[numCond][0], surf[numSurf - 1][1]);
                    set(condLine[numCond][1], surf[numSurf - 1][2]);
                    condFlag[numCond] = 2;
                    numCond++;
                    set(condLine[numCond][0], surf[surfstart][0]);
                    set(condLine[numCond][1], surf[surfstart][3]);
                    condFlag[numCond] = 2;
                    numCond++;
                }
            }
        }

        for (int k = 0; k < numSurf; k++) {
            if (manhattan(surf[k][0], surf[k][3]) < SMALL && manhattan(surf[k][1], surf[k][2]) < SMALL)
                continue;
            if (manhattan(surf[k][0], surf[k][3]) < SMALL) {
                Vertex v1 = new Vertex(new BigDecimal(surf[k][0][x]), new BigDecimal(surf[k][0][y]), new BigDecimal(surf[k][0][z]));
                Vertex v2 = new Vertex(new BigDecimal(surf[k][1][x]), new BigDecimal(surf[k][1][y]), new BigDecimal(surf[k][1][z]));
                Vertex v3 = new Vertex(new BigDecimal(surf[k][2][x]), new BigDecimal(surf[k][2][y]), new BigDecimal(surf[k][2][z]));
                newTriangles.add(new GData3(
                        bodyColour.getColourNumber(), bodyColour.getR(), bodyColour.getG(), bodyColour.getB(), bodyColour.getA(),
                        v1, v2, v3, View.DUMMY_REFERENCE, linkedDatFile, true));
            } else if (manhattan(surf[k][1], surf[k][2]) < SMALL) {
                Vertex v1 = new Vertex(new BigDecimal(surf[k][0][x]), new BigDecimal(surf[k][0][y]), new BigDecimal(surf[k][0][z]));
                Vertex v2 = new Vertex(new BigDecimal(surf[k][1][x]), new BigDecimal(surf[k][1][y]), new BigDecimal(surf[k][1][z]));
                Vertex v3 = new Vertex(new BigDecimal(surf[k][3][x]), new BigDecimal(surf[k][3][y]), new BigDecimal(surf[k][3][z]));
                newTriangles.add(new GData3(
                        bodyColour.getColourNumber(), bodyColour.getR(), bodyColour.getG(), bodyColour.getB(), bodyColour.getA(),
                        v1, v2, v3, View.DUMMY_REFERENCE, linkedDatFile, true));
            } else if (mode == YTruderSettings.MODE_TRANSLATE_BY_DISTANCE
                    || mode == YTruderSettings.MODE_SYMMETRY_ACROSS_PLANE
                    || triAngle(surf[k][0], surf[k][1], surf[k][2], surf[k][0], surf[k][2], surf[k][3]) <= 0.5) {
                Vertex v1 = new Vertex(new BigDecimal(surf[k][0][x]), new BigDecimal(surf[k][0][y]), new BigDecimal(surf[k][0][z]));
                Vertex v2 = new Vertex(new BigDecimal(surf[k][1][x]), new BigDecimal(surf[k][1][y]), new BigDecimal(surf[k][1][z]));
                Vertex v3 = new Vertex(new BigDecimal(surf[k][2][x]), new BigDecimal(surf[k][2][y]), new BigDecimal(surf[k][2][z]));
                Vertex v4 = new Vertex(new BigDecimal(surf[k][3][x]), new BigDecimal(surf[k][3][y]), new BigDecimal(surf[k][3][z]));
                newQuads.add(new GData4(
                        bodyColour.getColourNumber(), bodyColour.getR(), bodyColour.getG(), bodyColour.getB(), bodyColour.getA(),
                        v1, v2, v3, v4, View.DUMMY_REFERENCE, linkedDatFile));
            } else {
                {
                    Vertex v1 = new Vertex(new BigDecimal(surf[k][0][x]), new BigDecimal(surf[k][0][y]), new BigDecimal(surf[k][0][z]));
                    Vertex v2 = new Vertex(new BigDecimal(surf[k][1][x]), new BigDecimal(surf[k][1][y]), new BigDecimal(surf[k][1][z]));
                    Vertex v3 = new Vertex(new BigDecimal(surf[k][2][x]), new BigDecimal(surf[k][2][y]), new BigDecimal(surf[k][2][z]));
                    newTriangles.add(new GData3(
                            bodyColour.getColourNumber(), bodyColour.getR(), bodyColour.getG(), bodyColour.getB(), bodyColour.getA(),
                            v1, v2, v3, View.DUMMY_REFERENCE, linkedDatFile, true));
                }
                {
                    Vertex v1 = new Vertex(new BigDecimal(surf[k][0][x]), new BigDecimal(surf[k][0][y]), new BigDecimal(surf[k][0][z]));
                    Vertex v2 = new Vertex(new BigDecimal(surf[k][2][x]), new BigDecimal(surf[k][2][y]), new BigDecimal(surf[k][2][z]));
                    Vertex v3 = new Vertex(new BigDecimal(surf[k][3][x]), new BigDecimal(surf[k][3][y]), new BigDecimal(surf[k][3][z]));
                    newTriangles.add(new GData3(
                            bodyColour.getColourNumber(), bodyColour.getR(), bodyColour.getG(), bodyColour.getB(), bodyColour.getA(),
                            v1, v2, v3, View.DUMMY_REFERENCE, linkedDatFile, true));
                }
            }
        }

        for (int k = 0; k < numCond; k++) {
            if (manhattan(condLine[k][0], condLine[k][1]) < SMALL)
                continue;
            if (condFlag[k] == 5) {
                double a;
                a = triAngle(condLine[k][0], condLine[k][1], condLine[k][2], condLine[k][0], condLine[k][3], condLine[k][1]);
                if (a < angleLineThr) {
                    Vertex v1 = new Vertex(new BigDecimal(condLine[k][0][x]), new BigDecimal(condLine[k][0][y]), new BigDecimal(condLine[k][0][z]));
                    Vertex v2 = new Vertex(new BigDecimal(condLine[k][1][x]), new BigDecimal(condLine[k][1][y]), new BigDecimal(condLine[k][1][z]));
                    Vertex v3 = new Vertex(new BigDecimal(condLine[k][2][x]), new BigDecimal(condLine[k][2][y]), new BigDecimal(condLine[k][2][z]));
                    Vertex v4 = new Vertex(new BigDecimal(condLine[k][3][x]), new BigDecimal(condLine[k][3][y]), new BigDecimal(condLine[k][3][z]));
                    newCondlines.add(new GData5(
                            lineColour.getColourNumber(), lineColour.getR(), lineColour.getG(), lineColour.getB(), lineColour.getA(),
                            v1, v2, v3, v4, View.DUMMY_REFERENCE, linkedDatFile));
                } else {
                    Vertex v1 = new Vertex(new BigDecimal(condLine[k][0][x]), new BigDecimal(condLine[k][0][y]), new BigDecimal(condLine[k][0][z]));
                    Vertex v2 = new Vertex(new BigDecimal(condLine[k][1][x]), new BigDecimal(condLine[k][1][y]), new BigDecimal(condLine[k][1][z]));
                    newLines.add(new GData2(
                            lineColour.getColourNumber(), lineColour.getR(), lineColour.getG(), lineColour.getB(), lineColour.getA(),
                            v1, v2, View.DUMMY_REFERENCE, linkedDatFile, true));
                }
            }

            if (condFlag[k] == 2) {
                Vertex v1 = new Vertex(new BigDecimal(condLine[k][0][x]), new BigDecimal(condLine[k][0][y]), new BigDecimal(condLine[k][0][z]));
                Vertex v2 = new Vertex(new BigDecimal(condLine[k][1][x]), new BigDecimal(condLine[k][1][y]), new BigDecimal(condLine[k][1][z]));
                newLines.add(new GData2(
                        lineColour.getColourNumber(), lineColour.getR(), lineColour.getG(), lineColour.getB(), lineColour.getA(),
                        v1, v2, View.DUMMY_REFERENCE, linkedDatFile, true));
            }

        }

        NLogger.debug(getClass(), "Check for identical vertices and collinearity."); //$NON-NLS-1$
        final Set<GData2> linesToDelete2 = new HashSet<>();
        final Set<GData3> trisToDelete2 = new HashSet<>();
        final Set<GData4> quadsToDelete2 = new HashSet<>();
        final Set<GData5> condlinesToDelete2 = new HashSet<>();
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
            for (GData5 g5 : newCondlines) {
                Vertex[] verts = condlines.get(g5);
                SortedSet<Vertex> verts2 = new TreeSet<>();
                verts2.addAll(Arrays.asList(verts));
                if (verts2.size() < 4) {
                    condlinesToDelete2.add(g5);
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
        for (GData5 condline : newCondlines) {
            linkedDatFile.addToTailOrInsertAfterCursor(condline);
        }

        NLogger.debug(getClass(), "Delete new, but invalid objects."); //$NON-NLS-1$

        clearSelection2();
        newLines.removeAll(linesToDelete2);
        newTriangles.removeAll(trisToDelete2);
        newQuads.removeAll(quadsToDelete2);
        newCondlines.removeAll(condlinesToDelete2);
        selectedLines.addAll(linesToDelete2);
        selectedTriangles.addAll(trisToDelete2);
        selectedQuads.addAll(quadsToDelete2);
        selectedCondlines.addAll(condlinesToDelete2);
        selectedData.addAll(selectedLines);
        selectedData.addAll(selectedTriangles);
        selectedData.addAll(selectedQuads);
        selectedData.addAll(selectedCondlines);
        delete(false, false);

        // Round to 6 decimal places

        selectedLines.addAll(newLines);
        selectedTriangles.addAll(newTriangles);
        selectedQuads.addAll(newQuads);
        selectedCondlines.addAll(newCondlines);
        selectedData.addAll(selectedLines);
        selectedData.addAll(selectedTriangles);
        selectedData.addAll(selectedQuads);
        selectedData.addAll(selectedCondlines);

        NLogger.debug(getClass(), "Round."); //$NON-NLS-1$
        roundSelection(6, 10, true, false, true, true, true);

        setModified(true, true);
        validateState();

        NLogger.debug(getClass(), "Done."); //$NON-NLS-1$
    }

    private void cross(double[] dest, double[] left, double[] right) {
        dest[0] = left[1] * right[2] - left[2] * right[1];
        dest[1] = left[2] * right[0] - left[0] * right[2];
        dest[2] = left[0] * right[1] - left[1] * right[0];
    }

    private double dot(double[] v1, double[] v2) {
        return v1[0] * v2[0] + v1[1] * v2[1] + v1[2] * v2[2];
    }

    private void sub(double[] dest, double[] left, double[] right) {
        dest[0] = left[0] - right[0];
        dest[1] = left[1] - right[1];
        dest[2] = left[2] - right[2];
    }

    private void mult(double[] dest, double[] v, double factor) {
        dest[0] = factor * v[0];
        dest[1] = factor * v[1];
        dest[2] = factor * v[2];
    }

    private void set(double[] dest, double[] src) {
        dest[0] = src[0];
        dest[1] = src[1];
        dest[2] = src[2];
    }

    private double manhattan(double[] v1, double[] v2) {
        return Math.abs(v1[0] - v2[0]) + Math.abs(v1[1] - v2[1]) + Math.abs(v1[2] - v2[2]);
    }

    private double dist(double[] v1, double[] v2) {
        return Math.sqrt((v1[0] - v2[0]) * (v1[0] - v2[0]) + (v1[1] - v2[1]) * (v1[1] - v2[1]) + (v1[2] - v2[2]) * (v1[2] - v2[2]));
    }

    // Tri_Angle computes the cosine of the angle between the planes of two
    // triangles.
    // They are assumed to be non-degenerated
    private double triAngle(double[] u0, double[] u1, double[] u2, double[] v0, double[] v1, double[] v2) {
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
        sub(v10, v1, v0);
        sub(v20, v2, v0);
        cross(temp, u10, u20);
        len = dist(temp, nullv);
        mult(unorm, temp, 1 / len);
        cross(temp, v10, v20);
        len = dist(temp, nullv);
        mult(vnorm, temp, 1 / len);
        return 180 / 3.14159 * Math.acos(dot(unorm, vnorm));
    }
}
