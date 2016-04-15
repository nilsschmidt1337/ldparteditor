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
import java.util.TreeMap;
import java.util.TreeSet;

import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;

class VM16Subdivide extends VM15Flipper {

    protected VM16Subdivide(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public void subdivideCatmullClark() {

        if (linkedDatFile.isReadOnly()) return;

        // Backup selected surfaces
        HashSet<GData> surfsToParse = new HashSet<GData>();

        final Set<GData2> linesToDelete2 = new HashSet<GData2>();
        final Set<GData3> trisToDelete2 = new HashSet<GData3>();
        final Set<GData4> quadsToDelete2 = new HashSet<GData4>();

        final Set<GData2> newLines = new HashSet<GData2>();
        final Set<GData4> newQuads = new HashSet<GData4>();

        {
            for (GData3 g3 : selectedTriangles) {
                if (!lineLinkedToVertices.containsKey(g3)) continue;
                surfsToParse.add(g3);
                trisToDelete2.add(g3);
            }
            for (GData4 g4 : selectedQuads) {
                if (!lineLinkedToVertices.containsKey(g4)) continue;
                surfsToParse.add(g4);
                quadsToDelete2.add(g4);
            }
        }
        if (surfsToParse.isEmpty()) return;

        // Delete all condlines, since the are a PITA for subdivision

        clearSelection();
        selectedCondlines.addAll(condlines.keySet());
        selectedData.addAll(selectedCondlines);
        delete(false, false);

        TreeMap<Vertex, Vertex> newPoints = new TreeMap<Vertex, Vertex>();

        // Calculate new points
        for (Vertex v : vertexLinkedToPositionInFile.keySet()) {
            Set<VertexManifestation> manis = vertexLinkedToPositionInFile.get(v);
            HashSet<Vector3d> midEdge = new HashSet<Vector3d>();
            boolean keepIt = false;
            for (VertexManifestation m : manis) {
                GData gd = m.getGdata();
                switch (gd.type()) {
                case 0:
                    continue;
                case 2:
                    keepIt = true;
                    break;
                case 3:
                {
                    int p = m.getPosition();
                    Vertex[] verts = triangles.get(gd);
                    Vector3d vt = new Vector3d(v);
                    midEdge.add(Vector3d.add(vt, new Vector3d(verts[(p + 1) % 3])));
                    midEdge.add(Vector3d.add(vt, new Vector3d(verts[(p + 2) % 3])));
                }
                break;
                case 4:
                {
                    int p = m.getPosition();
                    Vertex[] verts = quads.get(gd);
                    Vector3d vt = new Vector3d(v);
                    midEdge.add(Vector3d.add(vt, new Vector3d(verts[(p + 1) % 4])));
                    midEdge.add(Vector3d.add(vt, new Vector3d(verts[(p + 3) % 4])));
                }
                break;
                default:
                    continue;
                }
                if (keepIt) break;
            }
            if (keepIt) {
                newPoints.put(v, v);
            } else {
                BigDecimal c = new BigDecimal(midEdge.size() * 2);
                Vector3d np = new Vector3d();
                for (Vector3d vd : midEdge) {
                    np = Vector3d.add(np, vd);
                }
                np.setX(np.X.divide(c, Threshold.mc));
                np.setY(np.Y.divide(c, Threshold.mc));
                np.setZ(np.Z.divide(c, Threshold.mc));
                newPoints.put(v, new Vertex(np));
            }
        }

        for (GData gd : surfsToParse) {
            Vertex[] originalVerts;
            int colourNumber;
            float r;
            float g;
            float b;
            float a;
            switch (gd.type()) {
            case 3:
                originalVerts = triangles.get(gd);
                GData3 g3 = (GData3) gd;
                colourNumber = g3.colourNumber; r = g3.r; g = g3.g; b = g3.b; a = g3.a;
                break;
            case 4:
                originalVerts = quads.get(gd);
                GData4 g4 = (GData4) gd;
                colourNumber = g4.colourNumber; r = g4.r; g = g4.g; b = g4.b; a = g4.a;
                break;
            default:
                continue;
            }

            final int c = originalVerts.length;

            BigDecimal c2 = new BigDecimal(c);
            Vector3d center = new Vector3d();
            for (Vertex vd : originalVerts) {
                center = Vector3d.add(center, new Vector3d(vd));
            }
            center.setX(center.X.divide(c2, Threshold.mc));
            center.setY(center.Y.divide(c2, Threshold.mc));
            center.setZ(center.Z.divide(c2, Threshold.mc));

            Vertex vc = new Vertex(center);

            Vertex[] ve = new Vertex[c];
            for(int i = 0; i < c; i++) {
                ve[i] = new Vertex(Vector3d.add(new Vector3d(originalVerts[i]), new Vector3d(originalVerts[(i + 1) % c])).scaledByHalf());
                GData2 g2 = null;
                if ((g2 = hasEdge(originalVerts[i], originalVerts[(i + 1) % c])) != null) {
                    if (!vertexLinkedToPositionInFile.containsKey(ve[i]) || hasEdge(originalVerts[i], ve[i]) == null) newLines.add(new GData2(24, View.line_Colour_r[0], View.line_Colour_g[0], View.line_Colour_b[0], 1f, originalVerts[i], ve[i], View.DUMMY_REFERENCE, linkedDatFile, true));
                    if (!vertexLinkedToPositionInFile.containsKey(ve[i]) || hasEdge(originalVerts[(i + 1) % c], ve[i]) == null) newLines.add(new GData2(24, View.line_Colour_r[0], View.line_Colour_g[0], View.line_Colour_b[0], 1f, originalVerts[(i + 1) % c], ve[i], View.DUMMY_REFERENCE, linkedDatFile, true));
                    linesToDelete2.add(g2);
                }
            }

            // Build quads
            for(int i = 0; i < c; i++) {
                newQuads.add(new GData4(colourNumber, r, g, b, a, newPoints.get(originalVerts[i]), ve[i], vc, ve[(c + i - 1) % c], View.DUMMY_REFERENCE, linkedDatFile));
            }

        }

        for (GData g : newLines) {
            linkedDatFile.addToTailOrInsertAfterCursor(g);
        }
        for (GData g : newQuads) {
            linkedDatFile.addToTailOrInsertAfterCursor(g);
        }

        selectedLines.addAll(linesToDelete2);
        selectedTriangles.addAll(trisToDelete2);
        selectedQuads.addAll(quadsToDelete2);
        selectedData.addAll(linesToDelete2);
        selectedData.addAll(trisToDelete2);
        selectedData.addAll(quadsToDelete2);
        delete(false, false);

        selectedLines.addAll(newLines);
        selectedData.addAll(newLines);
        selectedQuads.addAll(newQuads);
        selectedData.addAll(newQuads);
        roundSelection(6, 10, true, false);
        setModified(true, true);
        validateState();

    }

    public void subdivideLoop() {

        if (linkedDatFile.isReadOnly()) return;

        // Backup selected surfaces
        HashSet<GData3> surfsToParse = new HashSet<GData3>();

        final Set<GData2> linesToDelete2 = new HashSet<GData2>();
        final Set<GData3> trisToDelete2 = new HashSet<GData3>();

        final Set<GData2> newLines = new HashSet<GData2>();
        final Set<GData3> newTris = new HashSet<GData3>();

        for (GData3 g3 : selectedTriangles) {
            if (!lineLinkedToVertices.containsKey(g3)) continue;
            surfsToParse.add(g3);
            trisToDelete2.add(g3);
        }

        splitQuads(false);

        for (GData3 g3 : selectedTriangles) {
            if (!lineLinkedToVertices.containsKey(g3)) continue;
            surfsToParse.add(g3);
            trisToDelete2.add(g3);
        }

        if (surfsToParse.isEmpty()) return;

        // Delete all condlines, since the are a PITA for subdivision

        clearSelection();
        selectedCondlines.addAll(condlines.keySet());
        selectedData.addAll(selectedCondlines);
        delete(false, false);

        TreeMap<Vertex, Vertex> newPoints = new TreeMap<Vertex, Vertex>();

        for (GData3 g3 : surfsToParse) {
            Vertex[] originalVerts;
            int colourNumber;
            float r;
            float g;
            float b;
            float a;
            originalVerts = triangles.get(g3);
            colourNumber = g3.colourNumber; r = g3.r; g = g3.g; b = g3.b; a = g3.a;

            final int c = originalVerts.length;

            Vertex[] ve = new Vertex[c];
            for(int i = 0; i < c; i++) {
                ve[i] = new Vertex(Vector3d.add(new Vector3d(originalVerts[i]), new Vector3d(originalVerts[(i + 1) % c])).scaledByHalf());
                GData2 g2 = null;
                if ((g2 = hasEdge(originalVerts[i], originalVerts[(i + 1) % c])) != null) {
                    if (!vertexLinkedToPositionInFile.containsKey(ve[i]) || hasEdge(originalVerts[i], ve[i]) == null) newLines.add(new GData2(24, View.line_Colour_r[0], View.line_Colour_g[0], View.line_Colour_b[0], 1f, originalVerts[i], ve[i], View.DUMMY_REFERENCE, linkedDatFile, true));
                    if (!vertexLinkedToPositionInFile.containsKey(ve[i]) || hasEdge(originalVerts[(i + 1) % c], ve[i]) == null) newLines.add(new GData2(24, View.line_Colour_r[0], View.line_Colour_g[0], View.line_Colour_b[0], 1f, originalVerts[(i + 1) % c], ve[i], View.DUMMY_REFERENCE, linkedDatFile, true));
                    linesToDelete2.add(g2);
                }
            }

            // Build triangles
            newTris.add(new GData3(colourNumber, r, g, b, a, ve[0], ve[1], ve[2], View.DUMMY_REFERENCE, linkedDatFile, true));
            newTris.add(new GData3(colourNumber, r, g, b, a, originalVerts[0], ve[0], ve[2], View.DUMMY_REFERENCE, linkedDatFile, true));
            newTris.add(new GData3(colourNumber, r, g, b, a, originalVerts[1], ve[1], ve[0], View.DUMMY_REFERENCE, linkedDatFile, true));
            newTris.add(new GData3(colourNumber, r, g, b, a, originalVerts[2], ve[2], ve[1], View.DUMMY_REFERENCE, linkedDatFile, true));
        }

        for (GData g : newLines) {
            linkedDatFile.addToTailOrInsertAfterCursor(g);
        }
        for (GData g : newTris) {
            linkedDatFile.addToTailOrInsertAfterCursor(g);
        }

        selectedLines.addAll(linesToDelete2);
        selectedTriangles.addAll(trisToDelete2);
        selectedData.addAll(linesToDelete2);
        selectedData.addAll(trisToDelete2);
        delete(false, false);

        selectedLines.addAll(newLines);
        selectedData.addAll(newLines);
        selectedTriangles.addAll(newTris);
        selectedData.addAll(newTris);
        roundSelection(6, 10, true, false);

        TreeSet<Vertex> verticesToMove = new TreeSet<Vertex>();

        for (GData3 tri : selectedTriangles) {
            Vertex[] verts = triangles.get(tri);
            for (Vertex v : verts) {
                verticesToMove.add(v);
            }
        }

        int newContentSize = newLines.size() + newTris.size();

        clearSelection();

        // Calculate new points, based on Loop's Algorithm
        for (Vertex v : vertexLinkedToPositionInFile.keySet()) {
            Set<VertexManifestation> manis = vertexLinkedToPositionInFile.get(v);
            HashSet<Vector3d> midEdge = new HashSet<Vector3d>();
            boolean keepIt = false;
            for (VertexManifestation m : manis) {
                GData gd = m.getGdata();
                switch (gd.type()) {
                case 0:
                    continue;
                case 2:
                    keepIt = true;
                    break;
                case 3:
                {
                    int p = m.getPosition();
                    Vertex[] verts = triangles.get(gd);
                    midEdge.add(new Vector3d(verts[(p + 1) % 3]));
                    midEdge.add(new Vector3d(verts[(p + 2) % 3]));
                }
                break;
                case 4:
                {
                    int p = m.getPosition();
                    Vertex[] verts = quads.get(gd);
                    midEdge.add(new Vector3d(verts[(p + 1) % 4]));
                    midEdge.add(new Vector3d(verts[(p + 3) % 4]));
                }
                break;
                default:
                    continue;
                }
                if (keepIt) break;
            }
            if (keepIt) {
                newPoints.put(v, v);
            } else {
                double n = midEdge.size() + 1;
                double t = 3.0 / 8.0 + 1.0 / 4.0 * Math.cos(Math.PI * 2.0 / n);
                double alphaN = 3.0 / 8.0 + t * t;
                BigDecimal oneMinusAlphaDivN = new BigDecimal((1.0 - alphaN) / n);
                BigDecimal alphaN2 = new BigDecimal(alphaN);
                Vector3d np = new Vector3d();
                for (Vector3d vd : midEdge) {
                    np = Vector3d.add(np, vd);
                }
                np.setX(v.X.multiply(alphaN2).add(np.X.multiply(oneMinusAlphaDivN, Threshold.mc)));
                np.setY(v.Y.multiply(alphaN2).add(np.Y.multiply(oneMinusAlphaDivN, Threshold.mc)));
                np.setZ(v.Z.multiply(alphaN2).add(np.Z.multiply(oneMinusAlphaDivN, Threshold.mc)));
                newPoints.put(v, new Vertex(np));
            }
        }

        for (Vertex v : verticesToMove) {
            changeVertexDirectFast(v, newPoints.get(v), true);
        }

        clearSelection();

        GData gd = linkedDatFile.getDrawChainTail();
        while (newContentSize > 0) {
            switch (gd.type()) {
            case 2:
                selectedLines.add((GData2) gd);
                selectedData.add(gd);
                break;
            case 3:
                selectedTriangles.add((GData3) gd);
                selectedData.add(gd);
                break;
            default:
                break;
            }
            gd = gd.getBefore();
            newContentSize--;
        }
        roundSelection(6, 10, true, false);

        setModified(true, true);
        validateState();
    }
}
