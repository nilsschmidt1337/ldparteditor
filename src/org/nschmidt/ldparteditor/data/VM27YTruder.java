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
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.composite3d.YTruderSettings;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.text.DatParser;

class VM27YTruder extends VM26LineIntersector {

    private final double EPSILON = 0.000001;
    private final double SMALL = 0.01;
    private double[] nullv = new double[] { 0.0, 0.0, 0.0 };

    protected VM27YTruder(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

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

        final GColour col16 = View.getLDConfigColour(16);
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

        if (ys.getAxis() == 0) {
            x = 1;
            y = 0;
            z = 2;
        } else if (ys.getAxis() == 1) {
            x = 0;
            y = 1;
            z = 2;
        } else if (ys.getAxis() == 2) {
            x = 0;
            y = 2;
            z = 1;
        }

        int originalLineCount = 0;
        for (GData2 gData2 : originalSelection) {
            inLine[originalLineCount][0][x] = gData2.X1.doubleValue();
            inLine[originalLineCount][0][y] = gData2.Y1.doubleValue();
            inLine[originalLineCount][0][z] = gData2.Z1.doubleValue();
            inLine[originalLineCount][1][x] = gData2.X2.doubleValue();
            inLine[originalLineCount][1][y] = gData2.Y2.doubleValue();
            inLine[originalLineCount][1][z] = gData2.Z2.doubleValue();
            lineUsed[originalLineCount] = 0;
            originalLineCount++;
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
                                if (MANHATTAN(inLine[current][end], inLine[j][k]) < SMALL) {
                                    current = j;
                                    end = 1 - k;
                                    lineUsed[current] = 1;
                                    flag = true;
                                    break;
                                }
                                if (flag)
                                    break;
                            }
                        }
                        if (flag)
                            break;
                    }
                } while (flag);

                end = 1 - end;
                surfstart = numSurf;
                SET(surf[numSurf][0], inLine[current][1 - end]);
                SET(surf[numSurf][1], inLine[current][end]);
                SET(surf[numSurf][2], inLine[current][end]);
                SET(surf[numSurf][3], inLine[current][1 - end]);
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
                    d0 = DIST(p0, surf[numSurf][0]);
                    d1 = DIST(p1, surf[numSurf][1]);
                    if (d0 > EPSILON) {
                        surf[numSurf][3][0] = surf[numSurf][3][0] * (d0 + distance) / d0;
                        surf[numSurf][3][2] = surf[numSurf][3][2] * (d0 + distance) / d0;
                    }
                    if (d1 > EPSILON) {
                        surf[numSurf][2][0] = surf[numSurf][2][0] * (d1 + distance) / d1;
                        surf[numSurf][2][2] = surf[numSurf][2][2] * (d1 + distance) / d1;
                    }
                    double a;
                    a = Tri_Angle(surf[numSurf][0], surf[numSurf][1], surf[numSurf][2], surf[numSurf][0], surf[numSurf][2], surf[numSurf][3]);
                    if (a > 0.5) {
                        SET(condLine[numCond][0], surf[numSurf][0]);
                        SET(condLine[numCond][1], surf[numSurf][2]);
                        SET(condLine[numCond][2], surf[numSurf][1]);
                        SET(condLine[numCond][3], surf[numSurf][3]);
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
                                if (MANHATTAN(inLine[current][end], inLine[j][k]) < SMALL && lineUsed[j] < 2) {
                                    current = j;
                                    end = 1 - k;
                                    flag = true;
                                    SET(surf[numSurf][0], inLine[current][1 - end]);
                                    SET(surf[numSurf][1], inLine[current][end]);
                                    SET(surf[numSurf][2], inLine[current][end]);
                                    SET(surf[numSurf][3], inLine[current][1 - end]);
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
                                        d0 = DIST(p0, surf[numSurf][0]);
                                        d1 = DIST(p1, surf[numSurf][1]);
                                        if (d0 > EPSILON) {
                                            surf[numSurf][3][0] = surf[numSurf][3][0] * (d0 + distance) / d0;
                                            surf[numSurf][3][2] = surf[numSurf][3][2] * (d0 + distance) / d0;
                                        }
                                        if (d1 > EPSILON) {
                                            surf[numSurf][2][0] = surf[numSurf][2][0] * (d1 + distance) / d1;
                                            surf[numSurf][2][2] = surf[numSurf][2][2] * (d1 + distance) / d1;
                                        }

                                        SET(condLine[numCond][0], surf[numSurf][0]);
                                        SET(condLine[numCond][1], surf[numSurf][2]);
                                        SET(condLine[numCond][2], surf[numSurf][1]);
                                        SET(condLine[numCond][3], surf[numSurf][3]);
                                        condFlag[numCond] = 5;
                                        numCond++;

                                        break;
                                    default:
                                        break;
                                    }
                                    SET(condLine[numCond][0], surf[numSurf][0]);
                                    SET(condLine[numCond][1], surf[numSurf][3]);
                                    SET(condLine[numCond][2], surf[numSurf][1]);
                                    SET(condLine[numCond][3], surf[numSurf - 1][0]);
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
                if (MANHATTAN(surf[numSurf - 1][1], surf[surfstart][0]) < SMALL) {
                    SET(condLine[numCond][0], surf[numSurf - 1][1]);
                    SET(condLine[numCond][1], surf[numSurf - 1][2]);
                    SET(condLine[numCond][2], surf[numSurf - 1][0]);
                    SET(condLine[numCond][3], surf[surfstart][1]);
                    condFlag[numCond] = 5;
                    numCond++;
                } else {
                    SET(condLine[numCond][0], surf[numSurf - 1][1]);
                    SET(condLine[numCond][1], surf[numSurf - 1][2]);
                    condFlag[numCond] = 2;
                    numCond++;
                    SET(condLine[numCond][0], surf[surfstart][0]);
                    SET(condLine[numCond][1], surf[surfstart][3]);
                    condFlag[numCond] = 2;
                    numCond++;
                }
            }
        }

        for (int k = 0; k < numSurf; k++) {
            if (MANHATTAN(surf[k][0], surf[k][3]) < SMALL && MANHATTAN(surf[k][1], surf[k][2]) < SMALL)
                continue;
            if (MANHATTAN(surf[k][0], surf[k][3]) < SMALL) {
                Vertex v1 = new Vertex(new BigDecimal(surf[k][0][x]), new BigDecimal(surf[k][0][y]), new BigDecimal(surf[k][0][z]));
                Vertex v2 = new Vertex(new BigDecimal(surf[k][1][x]), new BigDecimal(surf[k][1][y]), new BigDecimal(surf[k][1][z]));
                Vertex v3 = new Vertex(new BigDecimal(surf[k][2][x]), new BigDecimal(surf[k][2][y]), new BigDecimal(surf[k][2][z]));
                newTriangles.add(new GData3(
                        bodyColour.getColourNumber(), bodyColour.getR(), bodyColour.getG(), bodyColour.getB(), bodyColour.getA(),
                        v1, v2, v3, View.DUMMY_REFERENCE, linkedDatFile, true));
            } else if (MANHATTAN(surf[k][1], surf[k][2]) < SMALL) {
                Vertex v1 = new Vertex(new BigDecimal(surf[k][0][x]), new BigDecimal(surf[k][0][y]), new BigDecimal(surf[k][0][z]));
                Vertex v2 = new Vertex(new BigDecimal(surf[k][1][x]), new BigDecimal(surf[k][1][y]), new BigDecimal(surf[k][1][z]));
                Vertex v3 = new Vertex(new BigDecimal(surf[k][3][x]), new BigDecimal(surf[k][3][y]), new BigDecimal(surf[k][3][z]));
                newTriangles.add(new GData3(
                        bodyColour.getColourNumber(), bodyColour.getR(), bodyColour.getG(), bodyColour.getB(), bodyColour.getA(),
                        v1, v2, v3, View.DUMMY_REFERENCE, linkedDatFile, true));
            } else if (mode == YTruderSettings.MODE_TRANSLATE_BY_DISTANCE
                    || mode == YTruderSettings.MODE_SYMMETRY_ACROSS_PLANE
                    || Tri_Angle(surf[k][0], surf[k][1], surf[k][2], surf[k][0], surf[k][2], surf[k][3]) <= 0.5) {
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
            if (MANHATTAN(condLine[k][0], condLine[k][1]) < SMALL)
                continue;
            if (condFlag[k] == 5) {
                double a;
                a = Tri_Angle(condLine[k][0], condLine[k][1], condLine[k][2], condLine[k][0], condLine[k][3], condLine[k][1]);
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
                Set<Vertex> verts2 = new TreeSet<>();
                for (Vertex vert : verts) {
                    verts2.add(vert);
                }
                if (verts2.size() < 2) {
                    linesToDelete2.add(g2);
                }
            }
            for (GData3 g3 : newTriangles) {
                Vertex[] verts = triangles.get(g3);
                Set<Vertex> verts2 = new TreeSet<>();
                for (Vertex vert : verts) {
                    verts2.add(vert);
                }
                if (verts2.size() < 3 || g3.isCollinear()) {
                    trisToDelete2.add(g3);
                }
            }
            for (GData4 g4 : newQuads) {
                Vertex[] verts = quads.get(g4);
                Set<Vertex> verts2 = new TreeSet<>();
                for (Vertex vert : verts) {
                    verts2.add(vert);
                }
                if (verts2.size() < 4 || g4.isCollinear()) {
                    quadsToDelete2.add(g4);
                }
            }
            for (GData5 g5 : newCondlines) {
                Vertex[] verts = condlines.get(g5);
                Set<Vertex> verts2 = new TreeSet<>();
                for (Vertex vert : verts) {
                    verts2.add(vert);
                }
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

    private void CROSS(double[] dest, double[] left, double[] right) {
        dest[0] = left[1] * right[2] - left[2] * right[1];
        dest[1] = left[2] * right[0] - left[0] * right[2];
        dest[2] = left[0] * right[1] - left[1] * right[0];
    }

    private double DOT(double[] v1, double[] v2) {
        return v1[0] * v2[0] + v1[1] * v2[1] + v1[2] * v2[2];
    }

    private void SUB(double[] dest, double[] left, double[] right) {
        dest[0] = left[0] - right[0];
        dest[1] = left[1] - right[1];
        dest[2] = left[2] - right[2];
    }

    private void MULT(double[] dest, double[] v, double factor) {
        dest[0] = factor * v[0];
        dest[1] = factor * v[1];
        dest[2] = factor * v[2];
    }

    private void SET(double[] dest, double[] src) {
        dest[0] = src[0];
        dest[1] = src[1];
        dest[2] = src[2];
    }

    private double MANHATTAN(double[] v1, double[] v2) {
        return Math.abs(v1[0] - v2[0]) + Math.abs(v1[1] - v2[1]) + Math.abs(v1[2] - v2[2]);
    }

    private double DIST(double[] v1, double[] v2) {
        return Math.sqrt((v1[0] - v2[0]) * (v1[0] - v2[0]) + (v1[1] - v2[1]) * (v1[1] - v2[1]) + (v1[2] - v2[2]) * (v1[2] - v2[2]));
    }

    // Tri_Angle computes the cosine of the angle between the planes of two
    // triangles.
    // They are assumed to be non-degenerated
    private double Tri_Angle(double[] u0, double[] u1, double[] u2, double[] v0, double[] v1, double[] v2) {
        double[] unorm = new double[3];
        double[] vnorm = new double[3];
        double[] temp = new double[3];
        double[] u10 = new double[3];
        double[] u20 = new double[3];
        double[] v10 = new double[3];
        double[] v20 = new double[3];
        double len;
        SUB(u10, u1, u0);
        SUB(u20, u2, u0);
        SUB(v10, v1, v0);
        SUB(v20, v2, v0);
        CROSS(temp, u10, u20);
        len = DIST(temp, nullv);
        MULT(unorm, temp, 1 / len);
        CROSS(temp, v10, v20);
        len = DIST(temp, nullv);
        MULT(vnorm, temp, 1 / len);
        return 180 / 3.14159 * Math.acos(DOT(unorm, vnorm));
    }
}
