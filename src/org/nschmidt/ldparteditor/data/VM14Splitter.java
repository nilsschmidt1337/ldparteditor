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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.composite3d.RectifierSettings;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;

class VM14Splitter extends VM13SymSplitter {

    protected VM14Splitter(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public void split(int fractions) {

        if (linkedDatFile.isReadOnly()) return;

        final Set<GData2> newLines = new HashSet<>();
        final Set<GData3> newTriangles = new HashSet<>();
        final Set<GData5> newCondlines = new HashSet<>();

        final Set<GData2> linesToDelete2 = new HashSet<>();
        final Set<GData3> trisToDelete2 = new HashSet<>();
        final Set<GData4> quadsToDelete2 = new HashSet<>();
        final Set<GData5> clinesToDelete2 = new HashSet<>();

        final Set<AccurateEdge> edgesToSplit = new HashSet<>();

        {
            int i = 0;
            int j = 0;

            for (Vertex v1 : selectedVertices) {
                for (Vertex v2 : selectedVertices) {
                    if (j > i) {
                        if (isNeighbour(v1, v2)) {
                            edgesToSplit.add(new AccurateEdge(v1, v2));
                        }
                    }
                    j++;
                }
                i++;
            }
        }

        {
            for (GData2 g2 : selectedLines) {
                if (!lineLinkedToVertices.containsKey(g2)) continue;
                Vertex[] verts = lines.get(g2);
                edgesToSplit.add(new AccurateEdge(verts[0], verts[1]));
            }
            for (GData3 g3 : selectedTriangles) {
                if (!lineLinkedToVertices.containsKey(g3)) continue;
                Vertex[] verts = triangles.get(g3);
                edgesToSplit.add(new AccurateEdge(verts[0], verts[1]));
                edgesToSplit.add(new AccurateEdge(verts[1], verts[2]));
                edgesToSplit.add(new AccurateEdge(verts[2], verts[0]));
            }
            for (GData4 g4 : selectedQuads) {
                if (!lineLinkedToVertices.containsKey(g4)) continue;
                Vertex[] verts = quads.get(g4);
                edgesToSplit.add(new AccurateEdge(verts[0], verts[1]));
                edgesToSplit.add(new AccurateEdge(verts[1], verts[2]));
                edgesToSplit.add(new AccurateEdge(verts[2], verts[3]));
                edgesToSplit.add(new AccurateEdge(verts[3], verts[0]));
            }
            for (GData5 g5 : selectedCondlines) {
                if (!lineLinkedToVertices.containsKey(g5)) continue;
                Vertex[] verts = condlines.get(g5);
                edgesToSplit.add(new AccurateEdge(verts[0], verts[1]));
            }
        }

        clearSelection();

        for (GData2 g : new HashSet<>(lines.keySet())) {
            if (!lineLinkedToVertices.containsKey(g)) continue;
            List<GData2> result = split(g, fractions, edgesToSplit);
            if (result.isEmpty()) continue;
            newLines.addAll(result);
            for (GData n : result) {
                linkedDatFile.insertAfter(g, n);
            }
            linesToDelete2.add(g);
        }

        for (GData3 g : new HashSet<>(triangles.keySet())) {
            if (!lineLinkedToVertices.containsKey(g)) continue;
            List<GData3> result = split(g, fractions, edgesToSplit);
            if (result.isEmpty()) continue;
            newTriangles.addAll(result);
            for (GData n : result) {
                linkedDatFile.insertAfter(g, n);
            }
            trisToDelete2.add(g);
        }

        for (GData4 g : new HashSet<>(quads.keySet())) {
            if (!lineLinkedToVertices.containsKey(g)) continue;
            List<GData3> result = split(g, fractions, edgesToSplit);
            if (result.isEmpty()) continue;
            newTriangles.addAll(result);
            for (GData n : result) {
                linkedDatFile.insertAfter(g, n);
            }
            quadsToDelete2.add(g);
        }

        for (GData5 g : new HashSet<>(condlines.keySet())) {
            List<GData5> result = split(g, fractions, edgesToSplit);
            if (result.isEmpty()) continue;
            newCondlines.addAll(result);
            for (GData n : result) {
                linkedDatFile.insertAfter(g, n);
            }
            clinesToDelete2.add(g);
        }

        if (newLines.size() + newTriangles.size() + newCondlines.size() > 0) {
            setModified_NoSync();
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

        selectedTriangles.addAll(newTriangles);
        selectedData.addAll(selectedTriangles);
        RectifierSettings rs = new RectifierSettings();
        rs.setScope(1);
        rs.setNoBorderedQuadToRectConversation(true);
        rectify(rs, false, false);

        clearSelection();
        if (isModified()) {
            setModified(true, true);
        }
        validateState();
    }

    private List<GData5> split(GData5 g, int fractions, Set<AccurateEdge> edgesToSplit) {

        ArrayList<GData5> result = new ArrayList<>(fractions);

        // Detect how many edges are affected
        Vertex[] verts = condlines.get(g);
        int ec = edgesToSplit.contains(new AccurateEdge(verts[0], verts[1])) ? 1 :0;

        switch (ec) {
        case 0:
            return result;
        case 1:

            Vector3d A = new Vector3d(condlines.get(g)[0]);
            Vector3d B = new Vector3d(condlines.get(g)[1]);

            BigDecimal step = BigDecimal.ONE.divide(new BigDecimal(fractions), Threshold.mc);
            BigDecimal cur = BigDecimal.ZERO;
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusCur = BigDecimal.ONE.subtract(cur);
                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

                result.add(new GData5(g.colourNumber, g.r, g.g, g.b, g.a,

                        A.X.multiply(oneMinusCur).add(B.X.multiply(cur)),
                        A.Y.multiply(oneMinusCur).add(B.Y.multiply(cur)),
                        A.Z.multiply(oneMinusCur).add(B.Z.multiply(cur)),

                        A.X.multiply(oneMinusNext).add(B.X.multiply(next)),
                        A.Y.multiply(oneMinusNext).add(B.Y.multiply(next)),
                        A.Z.multiply(oneMinusNext).add(B.Z.multiply(next)),

                        g.X3, g.Y3, g.Z3,
                        g.X4, g.Y4, g.Z4,

                        View.DUMMY_REFERENCE, linkedDatFile));
                cur = next;
            }
            break;
        default:
            break;
        }
        return result;
    }

    private List<GData3> split(GData4 g, int fractions, Set<AccurateEdge> edgesToSplit) {

        // Detect how many edges are affected
        Vertex[] verts = quads.get(g);
        int ec = edgesToSplit.contains(new AccurateEdge(verts[0], verts[1])) ? 1 :0;
        ec += edgesToSplit.contains(new AccurateEdge(verts[1], verts[2])) ? 1 :0;
        ec += edgesToSplit.contains(new AccurateEdge(verts[2], verts[3])) ? 1 :0;
        ec += edgesToSplit.contains(new AccurateEdge(verts[3], verts[0])) ? 1 :0;

        switch (ec) {
        case 0:
            return new ArrayList<>();
        case 1:
            if (edgesToSplit.contains(new AccurateEdge(verts[0], verts[1]))) {
                return splitQuad1(verts[0], verts[1], verts[2], verts[3], fractions, g);
            } else if (edgesToSplit.contains(new AccurateEdge(verts[1], verts[2]))) {
                return splitQuad1(verts[1], verts[2], verts[3], verts[0], fractions, g);
            } else if (edgesToSplit.contains(new AccurateEdge(verts[2], verts[3]))) {
                return splitQuad1(verts[2], verts[3], verts[0], verts[1], fractions, g);
            } else {
                return splitQuad1(verts[3], verts[0], verts[1], verts[2], fractions, g);
            }
        case 2:

            if (edgesToSplit.contains(new AccurateEdge(verts[0], verts[1]))) {

                if (edgesToSplit.contains(new AccurateEdge(verts[1], verts[2]))) {

                    return splitQuad21(verts[0], verts[1], verts[2], verts[3], fractions, g);

                } else if (edgesToSplit.contains(new AccurateEdge(verts[2], verts[3]))) {

                    return splitQuad22(verts[0], verts[1], verts[2], verts[3], fractions, g);

                } else if (edgesToSplit.contains(new AccurateEdge(verts[3], verts[0]))) {

                    return splitQuad21(verts[3], verts[0], verts[1], verts[2], fractions, g);

                }
            } else if (edgesToSplit.contains(new AccurateEdge(verts[1], verts[2]))) {

                if (edgesToSplit.contains(new AccurateEdge(verts[2], verts[3]))) {

                    return splitQuad21(verts[1], verts[2], verts[3], verts[0], fractions, g);

                } else if (edgesToSplit.contains(new AccurateEdge(verts[3], verts[0]))) {

                    return splitQuad22(verts[1], verts[2], verts[3], verts[0], fractions, g);

                }
            } else if (edgesToSplit.contains(new AccurateEdge(verts[2], verts[3]))) {

                return splitQuad21(verts[2], verts[3], verts[0], verts[1], fractions, g);

            }
        case 3:
            if (!edgesToSplit.contains(new AccurateEdge(verts[0], verts[1]))) {
                return splitQuad3(verts[0], verts[1], verts[2], verts[3], fractions, g);
            } else if (!edgesToSplit.contains(new AccurateEdge(verts[1], verts[2]))) {
                return splitQuad3(verts[1], verts[2], verts[3], verts[0], fractions, g);
            } else if (!edgesToSplit.contains(new AccurateEdge(verts[2], verts[3]))) {
                return splitQuad3(verts[2], verts[3], verts[0], verts[1], fractions, g);
            } else {
                return splitQuad3(verts[3], verts[0], verts[1], verts[2], fractions, g);
            }
        case 4:
            return splitQuad4(verts[0], verts[1], verts[2], verts[3], fractions, g);
        default:
            break;
        }

        return new ArrayList<>();
    }

    private List<GData3> splitQuad1(Vertex v1, Vertex v2, Vertex v3, Vertex v4, int fractions, GData4 g) {
        ArrayList<GData3> result = new ArrayList<>(fractions);

        Vector3d A = new Vector3d(v1);
        Vector3d B = new Vector3d(v2);


        int fracA = fractions / 2;

        BigDecimal step = BigDecimal.ONE.divide(new BigDecimal(fractions), Threshold.mc);
        BigDecimal cur = BigDecimal.ZERO;
        BigDecimal next = BigDecimal.ZERO;

        BigDecimal middle = BigDecimal.ZERO;

        for (int i = 0; i < fracA; i++) {
            next = next.add(step);

            BigDecimal oneMinusCur = BigDecimal.ONE.subtract(cur);
            BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

            result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                    A.X.multiply(oneMinusCur).add(B.X.multiply(cur)),
                    A.Y.multiply(oneMinusCur).add(B.Y.multiply(cur)),
                    A.Z.multiply(oneMinusCur).add(B.Z.multiply(cur)),

                    A.X.multiply(oneMinusNext).add(B.X.multiply(next)),
                    A.Y.multiply(oneMinusNext).add(B.Y.multiply(next)),
                    A.Z.multiply(oneMinusNext).add(B.Z.multiply(next)),

                    v4.X,
                    v4.Y,
                    v4.Z,

                    View.DUMMY_REFERENCE, linkedDatFile, true));
            cur = next;
            middle = next;
        }


        for (int i = fracA; i < fractions; i++) {
            if (i == fractions - 1) {
                next = BigDecimal.ONE;
            } else {
                next = next.add(step);
            }

            BigDecimal oneMinusCur = BigDecimal.ONE.subtract(cur);
            BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

            result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                    A.X.multiply(oneMinusCur).add(B.X.multiply(cur)),
                    A.Y.multiply(oneMinusCur).add(B.Y.multiply(cur)),
                    A.Z.multiply(oneMinusCur).add(B.Z.multiply(cur)),

                    A.X.multiply(oneMinusNext).add(B.X.multiply(next)),
                    A.Y.multiply(oneMinusNext).add(B.Y.multiply(next)),
                    A.Z.multiply(oneMinusNext).add(B.Z.multiply(next)),

                    v3.X,
                    v3.Y,
                    v3.Z,

                    View.DUMMY_REFERENCE, linkedDatFile, true));
            cur = next;
        }

        {

            cur = middle;

            BigDecimal oneMinusCur = BigDecimal.ONE.subtract(cur);

            result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                    A.X.multiply(oneMinusCur).add(B.X.multiply(cur)),
                    A.Y.multiply(oneMinusCur).add(B.Y.multiply(cur)),
                    A.Z.multiply(oneMinusCur).add(B.Z.multiply(cur)),

                    v3.X,
                    v3.Y,
                    v3.Z,

                    v4.X,
                    v4.Y,
                    v4.Z,

                    View.DUMMY_REFERENCE, linkedDatFile, true));
        }

        return result;
    }

    private List<GData3> splitQuad21(Vertex v1, Vertex v2, Vertex v3, Vertex v4, int fractions, GData4 g) {

        ArrayList<GData3> result = new ArrayList<>(fractions * 8);

        // Split between v1-v2 & v2-v3
        Vector3d A = new Vector3d(v1);
        Vector3d B = new Vector3d(v2);
        Vector3d C = new Vector3d(v3);
        Vector3d D = new Vector3d(v4);

        BigDecimal step = BigDecimal.ONE.divide(new BigDecimal(fractions), Threshold.mc);

        ArrayList<Vector3d> newPoints = new ArrayList<>(fractions * 4);

        {
            BigDecimal cur = BigDecimal.ZERO;
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusCur = BigDecimal.ONE.subtract(cur);
                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

                newPoints.add(new Vector3d(
                        A.X.multiply(oneMinusCur).add(B.X.multiply(cur)),
                        A.Y.multiply(oneMinusCur).add(B.Y.multiply(cur)),
                        A.Z.multiply(oneMinusCur).add(B.Z.multiply(cur))
                        ));
                newPoints.add(new Vector3d(
                        A.X.multiply(oneMinusNext).add(B.X.multiply(next)),
                        A.Y.multiply(oneMinusNext).add(B.Y.multiply(next)),
                        A.Z.multiply(oneMinusNext).add(B.Z.multiply(next))
                        ));

                newPoints.add(D);

                cur = next;
            }
        }
        {
            BigDecimal cur = BigDecimal.ZERO;
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusCur = BigDecimal.ONE.subtract(cur);
                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

                newPoints.add(new Vector3d(
                        B.X.multiply(oneMinusCur).add(C.X.multiply(cur)),
                        B.Y.multiply(oneMinusCur).add(C.Y.multiply(cur)),
                        B.Z.multiply(oneMinusCur).add(C.Z.multiply(cur))
                        ));
                newPoints.add(new Vector3d(
                        B.X.multiply(oneMinusNext).add(C.X.multiply(next)),
                        B.Y.multiply(oneMinusNext).add(C.Y.multiply(next)),
                        B.Z.multiply(oneMinusNext).add(C.Z.multiply(next))
                        ));

                newPoints.add(D);

                cur = next;
            }
        }

        final int pz = newPoints.size();
        for (int i = 0; i < pz; i += 3) {
            Vector3d p1 = newPoints.get(i);
            Vector3d p2 = newPoints.get(i + 1);
            Vector3d p3 = newPoints.get(i + 2);

            result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                    p3.X,
                    p3.Y,
                    p3.Z,

                    p1.X,
                    p1.Y,
                    p1.Z,

                    p2.X,
                    p2.Y,
                    p2.Z,

                    View.DUMMY_REFERENCE, linkedDatFile, true));
        }

        return result;
    }

    private List<GData3> splitQuad22(Vertex v1, Vertex v2, Vertex v3, Vertex v4, int fractions, GData4 g) {

        ArrayList<GData3> result = new ArrayList<>(fractions * 8);

        // Split between v1-v2 & v3-v4
        Vector3d A = new Vector3d(v1);
        Vector3d B = new Vector3d(v2);
        Vector3d C = new Vector3d(v3);
        Vector3d D = new Vector3d(v4);

        BigDecimal step = BigDecimal.ONE.divide(new BigDecimal(fractions), Threshold.mc);

        ArrayList<Vector3d> newPoints = new ArrayList<>(fractions * 4);

        {
            BigDecimal cur = BigDecimal.ZERO;
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusCur = BigDecimal.ONE.subtract(cur);
                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

                newPoints.add(new Vector3d(
                        A.X.multiply(oneMinusCur).add(B.X.multiply(cur)),
                        A.Y.multiply(oneMinusCur).add(B.Y.multiply(cur)),
                        A.Z.multiply(oneMinusCur).add(B.Z.multiply(cur))
                        ));
                newPoints.add(new Vector3d(
                        D.X.multiply(oneMinusCur).add(C.X.multiply(cur)),
                        D.Y.multiply(oneMinusCur).add(C.Y.multiply(cur)),
                        D.Z.multiply(oneMinusCur).add(C.Z.multiply(cur))
                        ));
                newPoints.add(new Vector3d(
                        A.X.multiply(oneMinusNext).add(B.X.multiply(next)),
                        A.Y.multiply(oneMinusNext).add(B.Y.multiply(next)),
                        A.Z.multiply(oneMinusNext).add(B.Z.multiply(next))
                        ));
                newPoints.add(new Vector3d(
                        D.X.multiply(oneMinusNext).add(C.X.multiply(next)),
                        D.Y.multiply(oneMinusNext).add(C.Y.multiply(next)),
                        D.Z.multiply(oneMinusNext).add(C.Z.multiply(next))
                        ));

                cur = next;
            }
        }

        final int pz = newPoints.size();
        for (int i = 0; i < pz; i += 4) {
            Vector3d p1 = newPoints.get(i);
            Vector3d p2 = newPoints.get(i + 1);
            Vector3d p3 = newPoints.get(i + 2);
            Vector3d p4 = newPoints.get(i + 3);

            result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                    p2.X,
                    p2.Y,
                    p2.Z,

                    p1.X,
                    p1.Y,
                    p1.Z,

                    p3.X,
                    p3.Y,
                    p3.Z,

                    View.DUMMY_REFERENCE, linkedDatFile, true));

            result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                    p3.X,
                    p3.Y,
                    p3.Z,

                    p4.X,
                    p4.Y,
                    p4.Z,

                    p2.X,
                    p2.Y,
                    p2.Z,

                    View.DUMMY_REFERENCE, linkedDatFile, true));

        }

        return result;
    }

    private List<GData3> splitQuad3(Vertex v1, Vertex v2, Vertex v3, Vertex v4, int fractions, GData4 g) {
        ArrayList<GData3> result = new ArrayList<>(fractions * 8);

        Vector3d A = new Vector3d(v1);
        Vector3d B = new Vector3d(v2);
        Vector3d C = new Vector3d(v3);
        Vector3d D = new Vector3d(v4);

        Vector3d vc = Vector3d.add(Vector3d.add(Vector3d.add(A, B), C), D);
        vc.setX(vc.X.divide(new BigDecimal(4), Threshold.mc));
        vc.setY(vc.Y.divide(new BigDecimal(4), Threshold.mc));
        vc.setZ(vc.Z.divide(new BigDecimal(4), Threshold.mc));

        BigDecimal step = BigDecimal.ONE.divide(new BigDecimal(fractions), Threshold.mc);

        ArrayList<Vector3d> newPoints = new ArrayList<>(fractions * 3);

        {
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {

                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);


                newPoints.add(new Vector3d(
                        B.X.multiply(oneMinusNext).add(C.X.multiply(next)),
                        B.Y.multiply(oneMinusNext).add(C.Y.multiply(next)),
                        B.Z.multiply(oneMinusNext).add(C.Z.multiply(next))
                        ));

                next = next.add(step);
            }
        }
        {
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {

                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

                newPoints.add(new Vector3d(
                        C.X.multiply(oneMinusNext).add(D.X.multiply(next)),
                        C.Y.multiply(oneMinusNext).add(D.Y.multiply(next)),
                        C.Z.multiply(oneMinusNext).add(D.Z.multiply(next))
                        ));

                next = next.add(step);
            }
        }
        {
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions + 1; i++) {

                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

                newPoints.add(new Vector3d(
                        D.X.multiply(oneMinusNext).add(A.X.multiply(next)),
                        D.Y.multiply(oneMinusNext).add(A.Y.multiply(next)),
                        D.Z.multiply(oneMinusNext).add(A.Z.multiply(next))
                        ));

                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }
            }
        }

        fractions = fractions * 3;
        for (int i = 0; i < fractions + 1; i++) {

            result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                    vc.X,
                    vc.Y,
                    vc.Z,

                    newPoints.get(i).X,
                    newPoints.get(i).Y,
                    newPoints.get(i).Z,

                    newPoints.get((i + 1) % (fractions + 1)).X,
                    newPoints.get((i + 1) % (fractions + 1)).Y,
                    newPoints.get((i + 1) % (fractions + 1)).Z,

                    View.DUMMY_REFERENCE, linkedDatFile, true));


        }

        return result;
    }

    private List<GData3> splitQuad4(Vertex v1, Vertex v2, Vertex v3, Vertex v4, int fractions, GData4 g) {

        ArrayList<GData3> result = new ArrayList<>(fractions * 8);

        Vector3d A = new Vector3d(v1);
        Vector3d B = new Vector3d(v2);
        Vector3d C = new Vector3d(v3);
        Vector3d D = new Vector3d(v4);

        Vector3d vc = Vector3d.add(Vector3d.add(Vector3d.add(A, B), C), D);
        vc.setX(vc.X.divide(new BigDecimal(4), Threshold.mc));
        vc.setY(vc.Y.divide(new BigDecimal(4), Threshold.mc));
        vc.setZ(vc.Z.divide(new BigDecimal(4), Threshold.mc));

        BigDecimal step = BigDecimal.ONE.divide(new BigDecimal(fractions), Threshold.mc);

        ArrayList<Vector3d> newPoints = new ArrayList<>(fractions * 4);

        {
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);


                newPoints.add(new Vector3d(
                        A.X.multiply(oneMinusNext).add(B.X.multiply(next)),
                        A.Y.multiply(oneMinusNext).add(B.Y.multiply(next)),
                        A.Z.multiply(oneMinusNext).add(B.Z.multiply(next))
                        ));
            }
        }
        {
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);


                newPoints.add(new Vector3d(
                        B.X.multiply(oneMinusNext).add(C.X.multiply(next)),
                        B.Y.multiply(oneMinusNext).add(C.Y.multiply(next)),
                        B.Z.multiply(oneMinusNext).add(C.Z.multiply(next))
                        ));
            }
        }
        {
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

                newPoints.add(new Vector3d(
                        C.X.multiply(oneMinusNext).add(D.X.multiply(next)),
                        C.Y.multiply(oneMinusNext).add(D.Y.multiply(next)),
                        C.Z.multiply(oneMinusNext).add(D.Z.multiply(next))
                        ));
            }
        }
        {
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

                newPoints.add(new Vector3d(
                        D.X.multiply(oneMinusNext).add(A.X.multiply(next)),
                        D.Y.multiply(oneMinusNext).add(A.Y.multiply(next)),
                        D.Z.multiply(oneMinusNext).add(A.Z.multiply(next))
                        ));
            }
        }

        fractions = fractions * 4;
        for (int i = 0; i < fractions; i++) {

            result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                    vc.X,
                    vc.Y,
                    vc.Z,

                    newPoints.get(i).X,
                    newPoints.get(i).Y,
                    newPoints.get(i).Z,

                    newPoints.get((i + 1) % fractions).X,
                    newPoints.get((i + 1) % fractions).Y,
                    newPoints.get((i + 1) % fractions).Z,

                    View.DUMMY_REFERENCE, linkedDatFile, true));


        }

        return result;
    }

    private List<GData3> split(GData3 g, int fractions, Set<AccurateEdge> edgesToSplit) {

        // Detect how many edges are affected
        Vertex[] verts = triangles.get(g);
        int ec = edgesToSplit.contains(new AccurateEdge(verts[0], verts[1])) ? 1 :0;
        ec += edgesToSplit.contains(new AccurateEdge(verts[1], verts[2])) ? 1 :0;
        ec += edgesToSplit.contains(new AccurateEdge(verts[2], verts[0])) ? 1 :0;

        switch (ec) {
        case 0:
            return new ArrayList<>();
        case 1:
            if (edgesToSplit.contains(new AccurateEdge(verts[0], verts[1]))) {
                return splitTri1(verts[0], verts[1], verts[2], fractions, g);
            } else if (edgesToSplit.contains(new AccurateEdge(verts[1], verts[2]))) {
                return splitTri1(verts[1], verts[2], verts[0], fractions, g);
            } else {
                return splitTri1(verts[2], verts[0], verts[1], fractions, g);
            }
        case 2:
            if (edgesToSplit.contains(new AccurateEdge(verts[0], verts[1]))) {
                if (edgesToSplit.contains(new AccurateEdge(verts[1], verts[2]))) {
                    return splitTri2(verts[1], verts[2], verts[0], fractions, g);
                } else {
                    return splitTri2(verts[0], verts[1], verts[2], fractions, g);
                }
            } else if (edgesToSplit.contains(new AccurateEdge(verts[1], verts[2]))) {
                if (edgesToSplit.contains(new AccurateEdge(verts[0], verts[1]))) {
                    return splitTri2(verts[1], verts[2], verts[0], fractions, g);
                } else {
                    return splitTri2(verts[2], verts[0], verts[1], fractions, g);
                }
            } else if (edgesToSplit.contains(new AccurateEdge(verts[2], verts[0]))) {
                if (edgesToSplit.contains(new AccurateEdge(verts[0], verts[1]))) {
                    return splitTri2(verts[0], verts[1], verts[2], fractions, g);
                } else {
                    return splitTri2(verts[2], verts[0], verts[1], fractions, g);
                }
            }
        case 3:
            return splitTri3(verts[0], verts[1], verts[2], fractions, g);
        default:
            break;
        }

        return new ArrayList<>();
    }

    private List<GData3> splitTri1(Vertex v1, Vertex v2, Vertex v3, int fractions, GData3 g) {
        ArrayList<GData3> result = new ArrayList<>(fractions);

        Vector3d A = new Vector3d(v1);
        Vector3d B = new Vector3d(v2);

        BigDecimal step = BigDecimal.ONE.divide(new BigDecimal(fractions), Threshold.mc);
        BigDecimal cur = BigDecimal.ZERO;
        BigDecimal next = BigDecimal.ZERO;
        for (int i = 0; i < fractions; i++) {
            if (i == fractions - 1) {
                next = BigDecimal.ONE;
            } else {
                next = next.add(step);
            }

            BigDecimal oneMinusCur = BigDecimal.ONE.subtract(cur);
            BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

            result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                    A.X.multiply(oneMinusCur).add(B.X.multiply(cur)),
                    A.Y.multiply(oneMinusCur).add(B.Y.multiply(cur)),
                    A.Z.multiply(oneMinusCur).add(B.Z.multiply(cur)),

                    A.X.multiply(oneMinusNext).add(B.X.multiply(next)),
                    A.Y.multiply(oneMinusNext).add(B.Y.multiply(next)),
                    A.Z.multiply(oneMinusNext).add(B.Z.multiply(next)),

                    v3.X,
                    v3.Y,
                    v3.Z,

                    View.DUMMY_REFERENCE, linkedDatFile, true));
            cur = next;
        }

        return result;
    }

    private List<GData3> splitTri2(Vertex v1, Vertex v2, Vertex v3, int fractions, GData3 g) {

        ArrayList<GData3> result = new ArrayList<>(fractions);

        Vector3d A = new Vector3d(v1);
        Vector3d B = new Vector3d(v2);
        Vector3d C = new Vector3d(v3);

        BigDecimal step = BigDecimal.ONE.divide(new BigDecimal(fractions), Threshold.mc);
        BigDecimal cur = BigDecimal.ZERO;
        BigDecimal next = BigDecimal.ZERO;
        for (int i = 0; i < fractions; i++) {
            if (i == fractions - 1) {
                next = BigDecimal.ONE;
            } else {
                next = next.add(step);
            }

            BigDecimal oneMinusCur = BigDecimal.ONE.subtract(cur);
            BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

            if (i == 0) {
                result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                        v1.X,
                        v1.Y,
                        v1.Z,

                        A.X.multiply(oneMinusNext).add(B.X.multiply(next)),
                        A.Y.multiply(oneMinusNext).add(B.Y.multiply(next)),
                        A.Z.multiply(oneMinusNext).add(B.Z.multiply(next)),

                        A.X.multiply(oneMinusNext).add(C.X.multiply(next)),
                        A.Y.multiply(oneMinusNext).add(C.Y.multiply(next)),
                        A.Z.multiply(oneMinusNext).add(C.Z.multiply(next)),

                        View.DUMMY_REFERENCE, linkedDatFile, true));
            } else {
                result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                        A.X.multiply(oneMinusNext).add(C.X.multiply(next)),
                        A.Y.multiply(oneMinusNext).add(C.Y.multiply(next)),
                        A.Z.multiply(oneMinusNext).add(C.Z.multiply(next)),

                        A.X.multiply(oneMinusCur).add(C.X.multiply(cur)),
                        A.Y.multiply(oneMinusCur).add(C.Y.multiply(cur)),
                        A.Z.multiply(oneMinusCur).add(C.Z.multiply(cur)),

                        A.X.multiply(oneMinusCur).add(B.X.multiply(cur)),
                        A.Y.multiply(oneMinusCur).add(B.Y.multiply(cur)),
                        A.Z.multiply(oneMinusCur).add(B.Z.multiply(cur)),

                        View.DUMMY_REFERENCE, linkedDatFile, true));
                result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                        A.X.multiply(oneMinusCur).add(B.X.multiply(cur)),
                        A.Y.multiply(oneMinusCur).add(B.Y.multiply(cur)),
                        A.Z.multiply(oneMinusCur).add(B.Z.multiply(cur)),

                        A.X.multiply(oneMinusNext).add(B.X.multiply(next)),
                        A.Y.multiply(oneMinusNext).add(B.Y.multiply(next)),
                        A.Z.multiply(oneMinusNext).add(B.Z.multiply(next)),

                        A.X.multiply(oneMinusNext).add(C.X.multiply(next)),
                        A.Y.multiply(oneMinusNext).add(C.Y.multiply(next)),
                        A.Z.multiply(oneMinusNext).add(C.Z.multiply(next)),

                        View.DUMMY_REFERENCE, linkedDatFile, true));
            }

            cur = next;
        }

        return result;
    }

    private List<GData3> splitTri3(Vertex v1, Vertex v2, Vertex v3, int fractions, GData3 g) {

        ArrayList<GData3> result = new ArrayList<>(fractions * 3);

        Vector3d A = new Vector3d(v1);
        Vector3d B = new Vector3d(v2);
        Vector3d C = new Vector3d(v3);

        Vector3d vc = Vector3d.add(Vector3d.add(A, B), C);
        vc.setX(vc.X.divide(new BigDecimal(3), Threshold.mc));
        vc.setY(vc.Y.divide(new BigDecimal(3), Threshold.mc));
        vc.setZ(vc.Z.divide(new BigDecimal(3), Threshold.mc));

        BigDecimal step = BigDecimal.ONE.divide(new BigDecimal(fractions), Threshold.mc);

        ArrayList<Vector3d> newPoints = new ArrayList<>(fractions * 3);

        {
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);


                newPoints.add(new Vector3d(
                        A.X.multiply(oneMinusNext).add(B.X.multiply(next)),
                        A.Y.multiply(oneMinusNext).add(B.Y.multiply(next)),
                        A.Z.multiply(oneMinusNext).add(B.Z.multiply(next))
                        ));
            }
        }
        {
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);


                newPoints.add(new Vector3d(
                        B.X.multiply(oneMinusNext).add(C.X.multiply(next)),
                        B.Y.multiply(oneMinusNext).add(C.Y.multiply(next)),
                        B.Z.multiply(oneMinusNext).add(C.Z.multiply(next))
                        ));
            }
        }
        {
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

                newPoints.add(new Vector3d(
                        C.X.multiply(oneMinusNext).add(A.X.multiply(next)),
                        C.Y.multiply(oneMinusNext).add(A.Y.multiply(next)),
                        C.Z.multiply(oneMinusNext).add(A.Z.multiply(next))
                        ));
            }
        }

        fractions = fractions * 3;
        for (int i = 0; i < fractions; i++) {

            result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                    vc.X,
                    vc.Y,
                    vc.Z,

                    newPoints.get(i).X,
                    newPoints.get(i).Y,
                    newPoints.get(i).Z,

                    newPoints.get((i + 1) % fractions).X,
                    newPoints.get((i + 1) % fractions).Y,
                    newPoints.get((i + 1) % fractions).Z,

                    View.DUMMY_REFERENCE, linkedDatFile, true));


        }

        return result;
    }

    private List<GData2> split(GData2 g, int fractions, Set<AccurateEdge> edgesToSplit) {

        ArrayList<GData2> result = new ArrayList<>(fractions);

        // Detect how many edges are affected
        Vertex[] verts = lines.get(g);
        int ec = edgesToSplit.contains(new AccurateEdge(verts[0], verts[1])) ? 1 :0;

        switch (ec) {
        case 0:
            return result;
        case 1:

            Vector3d A = new Vector3d(lines.get(g)[0]);
            Vector3d B = new Vector3d(lines.get(g)[1]);

            BigDecimal step = BigDecimal.ONE.divide(new BigDecimal(fractions), Threshold.mc);
            BigDecimal cur = BigDecimal.ZERO;
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusCur = BigDecimal.ONE.subtract(cur);
                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

                // Cx = Ax * (1-t) + Bx * t
                // Cy = Ay * (1-t) + By * t

                result.add(new GData2(g.colourNumber, g.r, g.g, g.b, g.a,

                        A.X.multiply(oneMinusCur).add(B.X.multiply(cur)),
                        A.Y.multiply(oneMinusCur).add(B.Y.multiply(cur)),
                        A.Z.multiply(oneMinusCur).add(B.Z.multiply(cur)),

                        A.X.multiply(oneMinusNext).add(B.X.multiply(next)),
                        A.Y.multiply(oneMinusNext).add(B.Y.multiply(next)),
                        A.Z.multiply(oneMinusNext).add(B.Z.multiply(next)),

                        View.DUMMY_REFERENCE, linkedDatFile, g.isLine));
                cur = next;
            }
            break;
        default:
            break;
        }
        return result;
    }

    private List<GData3> split(GData3 g, Vertex start, Vertex end, Vertex target) {
        ArrayList<GData3> result = new ArrayList<>();
        if (!start.equals(end)) {
            Vertex[] verts = triangles.get(g);
            if ((verts[0].equals(start) || verts[0].equals(end)) && (verts[1].equals(start) || verts[1].equals(end))) {
                return splitTri(verts[0], verts[1], verts[2], target, g);
            } else if ((verts[1].equals(start) || verts[1].equals(end)) && (verts[2].equals(start) || verts[2].equals(end))) {
                return splitTri(verts[1], verts[2], verts[0], target, g);
            } else if ((verts[2].equals(start) || verts[2].equals(end)) && (verts[0].equals(start) || verts[0].equals(end))) {
                return splitTri(verts[2], verts[0], verts[1], target, g);
            }
        }
        return result;
    }

    private List<GData3> splitTri(Vertex v1, Vertex v2, Vertex v3, Vertex target, GData3 g) {
        ArrayList<GData3> result = new ArrayList<>();
        result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a, v3.X, v3.Y, v3.Z, v1.X, v1.Y, v1.Z, target.X, target.Y, target.Z, View.DUMMY_REFERENCE, linkedDatFile, true));
        result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a, target.X, target.Y, target.Z, v2.X, v2.Y, v2.Z, v3.X, v3.Y, v3.Z, View.DUMMY_REFERENCE, linkedDatFile, true));
        return result;
    }

    private List<GData2> split(GData2 g, Vertex start, Vertex end, Vertex target) {
        ArrayList<GData2> result = new ArrayList<>();
        if (!start.equals(end)) {
            Vertex[] verts = lines.get(g);
            if ((verts[0].equals(start) || verts[0].equals(end)) && (verts[1].equals(start) || verts[1].equals(end))) {
                result.add(new GData2(g.colourNumber, g.r, g.g, g.b, g.a, start.X, start.Y, start.Z, target.X, target.Y, target.Z, View.DUMMY_REFERENCE, linkedDatFile, true));
                result.add(new GData2(g.colourNumber, g.r, g.g, g.b, g.a, target.X, target.Y, target.Z, end.X, end.Y, end.Z, View.DUMMY_REFERENCE, linkedDatFile, true));
            }
        }
        return result;
    }

    public boolean split(Vertex start, Vertex end, Vertex target) {

        if (linkedDatFile.isReadOnly()) return false;

        final Set<GData2> newLines = new HashSet<>();
        final Set<GData3> newTriangles = new HashSet<>();
        final Set<GData5> newCondlines = new HashSet<>();

        final Set<GData2> effSelectedLines = new HashSet<>();
        final Set<GData3> effSelectedTriangles = new HashSet<>();
        final Set<GData4> effSelectedQuads = new HashSet<>();
        final Set<GData5> effSelectedCondlines = new HashSet<>();


        final Set<GData2> linesToDelete2 = new HashSet<>();
        final Set<GData3> trisToDelete2 = new HashSet<>();
        final Set<GData4> quadsToDelete2 = new HashSet<>();
        final Set<GData5> clinesToDelete2 = new HashSet<>();

        Set<VertexManifestation> manis1 = vertexLinkedToPositionInFile.get(start);
        Set<VertexManifestation> manis2 = vertexLinkedToPositionInFile.get(end);

        if (manis1 == null || manis2 == null || manis1.isEmpty() || manis2.isEmpty()) return false;

        Set<GData> setA = new HashSet<>();
        Set<GData> setB = new HashSet<>();

        for (VertexManifestation m : manis1) {
            setA.add(m.getGdata());
        }
        for (VertexManifestation m : manis1) {
            setB.add(m.getGdata());
        }

        setA.retainAll(setB);

        for (GData g : setA) {
            if (!lineLinkedToVertices.containsKey(g)) continue;
            switch (g.type()) {
            case 2:
                effSelectedLines.add((GData2) g);
                break;
            case 3:
                effSelectedTriangles.add((GData3) g);
                break;
            case 4:
                effSelectedQuads.add((GData4) g);
                break;
            case 5:
                effSelectedCondlines.add((GData5) g);
                break;
            default:
                continue;
            }
        }

        for (GData2 g : effSelectedLines) {
            List<GData2> result = split(g, start, end, target);
            if (result.isEmpty()) continue;
            newLines.addAll(result);
            for (GData n : result) {
                linkedDatFile.insertAfter(g, n);
            }
            linesToDelete2.add(g);
        }

        for (GData3 g : effSelectedTriangles) {
            List<GData3> result = split(g, start, end, target);
            if (result.isEmpty()) continue;
            newTriangles.addAll(result);
            for (GData n : result) {
                linkedDatFile.insertAfter(g, n);
            }
            trisToDelete2.add(g);
        }

        for (GData4 g : effSelectedQuads) {
            List<GData3> result = split(g, start, end, target);
            if (result.isEmpty()) continue;
            newTriangles.addAll(result);
            for (GData n : result) {
                linkedDatFile.insertAfter(g, n);
            }
            quadsToDelete2.add(g);
        }

        for (GData5 g : effSelectedCondlines) {
            List<GData5> result = split(g, start, end, target);
            if (result.isEmpty()) continue;
            newCondlines.addAll(result);
            for (GData n : result) {
                linkedDatFile.insertAfter(g, n);
            }
            clinesToDelete2.add(g);
        }

        if (newLines.size() + newTriangles.size() + newCondlines.size() > 0) {
            setModified_NoSync();
        }

        backupSelection();
        clearSelection();
        selectedLines.addAll(linesToDelete2);
        selectedTriangles.addAll(trisToDelete2);
        selectedQuads.addAll(quadsToDelete2);
        selectedCondlines.addAll(clinesToDelete2);
        selectedData.addAll(selectedLines);
        selectedData.addAll(selectedTriangles);
        selectedData.addAll(selectedQuads);
        selectedData.addAll(selectedCondlines);
        delete(false, false);

        selectedTriangles.addAll(newTriangles);
        selectedData.addAll(selectedTriangles);

        RectifierSettings rs = new RectifierSettings();
        rs.setScope(1);
        rs.setNoBorderedQuadToRectConversation(true);
        rectify(rs, false, false);

        clearSelection();
        restoreSelection();

        validateState();

        return isModified();
    }

    private List<GData3> split(GData4 g, Vertex start, Vertex end, Vertex target) {
        ArrayList<GData3> result = new ArrayList<>();
        if (!start.equals(end)) {
            Vertex[] verts = quads.get(g);
            if ((verts[0].equals(start) || verts[0].equals(end)) && (verts[1].equals(start) || verts[1].equals(end))) {
                return splitQuad(verts[0], verts[1], verts[2], verts[3], target, g);
            } else if ((verts[1].equals(start) || verts[1].equals(end)) && (verts[2].equals(start) || verts[2].equals(end))) {
                return splitQuad(verts[1], verts[2], verts[3], verts[0], target, g);
            } else if ((verts[2].equals(start) || verts[2].equals(end)) && (verts[3].equals(start) || verts[3].equals(end))) {
                return splitQuad(verts[2], verts[3], verts[0], verts[1], target, g);
            } else if ((verts[3].equals(start) || verts[3].equals(end)) && (verts[0].equals(start) || verts[0].equals(end))) {
                return splitQuad(verts[3], verts[0], verts[1], verts[2], target, g);
            }
        }
        return result;
    }

    private List<GData3> splitQuad(Vertex v1, Vertex v2, Vertex v3, Vertex v4, Vertex target, GData4 g) {
        ArrayList<GData3> result = new ArrayList<>();
        result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a, v4.X, v4.Y, v4.Z, v1.X, v1.Y, v1.Z, target.X, target.Y, target.Z, View.DUMMY_REFERENCE, linkedDatFile, true));
        result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a, target.X, target.Y, target.Z, v2.X, v2.Y, v2.Z, v4.X, v4.Y, v4.Z, View.DUMMY_REFERENCE, linkedDatFile, true));
        result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a, v2.X, v2.Y, v2.Z, v3.X, v3.Y, v3.Z, v4.X, v4.Y, v4.Z, View.DUMMY_REFERENCE, linkedDatFile, true));
        return result;
    }

    private List<GData5> split(GData5 g, Vertex start, Vertex end, Vertex target) {
        ArrayList<GData5> result = new ArrayList<>();

        if (!start.equals(end)) {
            Vertex[] verts = condlines.get(g);
            if ((verts[0].equals(start) || verts[0].equals(end)) && (verts[1].equals(start) || verts[1].equals(end))) {
                result.add(new GData5(g.colourNumber, g.r, g.g, g.b, g.a, start.X, start.Y, start.Z, target.X, target.Y, target.Z, g.X3, g.Y3, g.Z3, g.X4, g.Y4, g.Z4, View.DUMMY_REFERENCE, linkedDatFile));
                result.add(new GData5(g.colourNumber, g.r, g.g, g.b, g.a, target.X, target.Y, target.Z, end.X, end.Y, end.Z, g.X3, g.Y3, g.Z3, g.X4, g.Y4, g.Z4, View.DUMMY_REFERENCE, linkedDatFile));
            }
        }
        return result;
    }

    public void splitQuads(boolean isModified) {

        final Set<GData4> quadsToDelete = new HashSet<>();
        final Set<GData3> newTriangles = new HashSet<>();

        final Set<GData4> quadsToParse = new HashSet<>();

        quadsToParse.addAll(selectedQuads);

        clearSelection();

        for (Iterator<GData4> ig = quadsToParse.iterator(); ig.hasNext();) {
            GData4 g = ig.next();
            if (!lineLinkedToVertices.containsKey(g) || g.isCollinear()) {
                ig.remove();
            }
        }
        if (quadsToParse.isEmpty()) {
            return;
        } else {
            setModified_NoSync();
        }
        quadsToDelete.addAll(quadsToParse);

        for (GData4 g4 : quadsToParse) {
            Vertex[] v = quads.get(g4);

            switch (g4.getHourglassConfiguration()) {
            case 0:
            {
                GData3 tri1 = new GData3(g4.colourNumber, g4.r, g4.g, g4.b, g4.a, v[0], v[1], v[2], View.DUMMY_REFERENCE, linkedDatFile, true);
                GData3 tri2 = new GData3(g4.colourNumber, g4.r, g4.g, g4.b, g4.a, v[2], v[3], v[0], View.DUMMY_REFERENCE, linkedDatFile, true);
                newTriangles.add(tri1);
                newTriangles.add(tri2);
                linkedDatFile.insertAfter(g4, tri2);
                linkedDatFile.insertAfter(g4, tri1);
            }
            break;
            case 1:
            {
                GData3 tri1 = new GData3(g4.colourNumber, g4.r, g4.g, g4.b, g4.a, v[1], v[0], v[2], View.DUMMY_REFERENCE, linkedDatFile, true);
                GData3 tri2 = new GData3(g4.colourNumber, g4.r, g4.g, g4.b, g4.a, v[2], v[3], v[1], View.DUMMY_REFERENCE, linkedDatFile, true);
                newTriangles.add(tri1);
                newTriangles.add(tri2);
                linkedDatFile.insertAfter(g4, tri2);
                linkedDatFile.insertAfter(g4, tri1);
            }
            break;
            case 2:
            {
                GData3 tri1 = new GData3(g4.colourNumber, g4.r, g4.g, g4.b, g4.a, v[0], v[1], v[3], View.DUMMY_REFERENCE, linkedDatFile, true);
                GData3 tri2 = new GData3(g4.colourNumber, g4.r, g4.g, g4.b, g4.a, v[3], v[2], v[0], View.DUMMY_REFERENCE, linkedDatFile, true);
                newTriangles.add(tri1);
                newTriangles.add(tri2);
                linkedDatFile.insertAfter(g4, tri2);
                linkedDatFile.insertAfter(g4, tri1);
            }
            break;
            default:
                break;
            }

        }

        selectedQuads.addAll(quadsToDelete);
        selectedData.addAll(quadsToDelete);
        delete(false, false);

        selectedTriangles.addAll(newTriangles);
        selectedData.addAll(newTriangles);

        if (isModified && isModified()) {
            setModified(true, true);
        }
        validateState();

    }
}
