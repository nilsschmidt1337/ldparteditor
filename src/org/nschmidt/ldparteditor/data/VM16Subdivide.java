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
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.nschmidt.ldparteditor.enumtype.Colour;
import org.nschmidt.ldparteditor.enumtype.Threshold;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.helper.math.Vector3d;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;

class VM16Subdivide extends VM15Flipper {

    protected VM16Subdivide(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public void subdivideCatmullClark(boolean showDialog) {


        if (linkedDatFile.isReadOnly()) return;

        if (showDialog) {
            if (selectedQuads.isEmpty() && selectedTriangles.isEmpty()) {
                final MessageBox messageBox = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_INFORMATION | SWT.OK);
                messageBox.setText(I18n.DIALOG_INFO);
                messageBox.setMessage(I18n.E3D_SUBDIVIDE_NO_SELECTION);
                messageBox.open();
                return;
            }

            final MessageBox messageBox = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
            messageBox.setText(I18n.DIALOG_WARNING);
            messageBox.setMessage(I18n.E3D_SUBDIVIDE_WARNING_CATMULL_CLARK);
            if (messageBox.open() != SWT.YES) {
                return;
            }
        }

        // Backup selected surfaces
        Set<GData> surfsToParse = new HashSet<>();

        final Set<GData2> linesToDelete2 = new HashSet<>();
        final Set<GData3> trisToDelete2 = new HashSet<>();
        final Set<GData4> quadsToDelete2 = new HashSet<>();

        final Set<GData2> newLines = new HashSet<>();
        final Set<GData4> newQuads = new HashSet<>();

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

        SortedMap<Vertex, Vertex> newPoints = new TreeMap<>();

        // Calculate new points
        for (Entry<Vertex, Set<VertexManifestation>> entry : vertexLinkedToPositionInFile.entrySet()) {
            Vertex v = entry.getKey();
            Set<VertexManifestation> manis = entry.getValue();
            Set<Vector3d> midEdge = new HashSet<>();
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
                BigDecimal c = new BigDecimal(Math.max(midEdge.size() * 2, 1));
                Vector3d np = new Vector3d();
                for (Vector3d vd : midEdge) {
                    np = Vector3d.add(np, vd);
                }
                np.setX(np.x.divide(c, Threshold.MC));
                np.setY(np.y.divide(c, Threshold.MC));
                np.setZ(np.z.divide(c, Threshold.MC));
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

            BigDecimal c2 = new BigDecimal(Math.max(c, 1));
            Vector3d center = new Vector3d();
            for (Vertex vd : originalVerts) {
                center = Vector3d.add(center, new Vector3d(vd));
            }
            center.setX(center.x.divide(c2, Threshold.MC));
            center.setY(center.y.divide(c2, Threshold.MC));
            center.setZ(center.z.divide(c2, Threshold.MC));

            Vertex vc = new Vertex(center);

            Vertex[] ve = new Vertex[c];
            for(int i = 0; i < c; i++) {
                ve[i] = new Vertex(Vector3d.add(new Vector3d(originalVerts[i]), new Vector3d(originalVerts[(i + 1) % c])).scaledByHalf());
                GData2 g2 = null;
                if ((g2 = hasEdge(originalVerts[i], originalVerts[(i + 1) % c])) != null) {
                    if (!vertexLinkedToPositionInFile.containsKey(ve[i]) || hasEdge(originalVerts[i], ve[i]) == null) newLines.add(new GData2(24, Colour.lineColourR, Colour.lineColourG, Colour.lineColourB, 1f, originalVerts[i], ve[i], View.DUMMY_REFERENCE, linkedDatFile, true));
                    if (!vertexLinkedToPositionInFile.containsKey(ve[i]) || hasEdge(originalVerts[(i + 1) % c], ve[i]) == null) newLines.add(new GData2(24, Colour.lineColourR, Colour.lineColourG, Colour.lineColourB, 1f, originalVerts[(i + 1) % c], ve[i], View.DUMMY_REFERENCE, linkedDatFile, true));
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
        roundSelection(6, 10, true, false, true, true, true);
        setModified(true, true);
        validateState();

    }

    public void subdivideLoop(boolean showDialog) {

        if (linkedDatFile.isReadOnly()) return;

        if (showDialog) {
            if (selectedQuads.isEmpty() && selectedTriangles.isEmpty()) {
                final MessageBox messageBox = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_INFORMATION | SWT.OK);
                messageBox.setText(I18n.DIALOG_INFO);
                messageBox.setMessage(I18n.E3D_SUBDIVIDE_NO_SELECTION);
                messageBox.open();
                return;
            }

            final MessageBox messageBox = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
            messageBox.setText(I18n.DIALOG_WARNING);
            messageBox.setMessage(I18n.E3D_SUBDIVIDE_WARNING_LOOP);
            if (messageBox.open() != SWT.YES) {
                return;
            }
        }


        // Backup selected surfaces
        Set<GData3> surfsToParse = new HashSet<>();

        final Set<GData2> linesToDelete2 = new HashSet<>();
        final Set<GData3> trisToDelete2 = new HashSet<>();

        final Set<GData2> newLines = new HashSet<>();
        final Set<GData3> newTris = new HashSet<>();

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

        SortedMap<Vertex, Vertex> newPoints = new TreeMap<>();

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
                    if (!vertexLinkedToPositionInFile.containsKey(ve[i]) || hasEdge(originalVerts[i], ve[i]) == null) newLines.add(new GData2(24, Colour.lineColourR, Colour.lineColourG, Colour.lineColourB, 1f, originalVerts[i], ve[i], View.DUMMY_REFERENCE, linkedDatFile, true));
                    if (!vertexLinkedToPositionInFile.containsKey(ve[i]) || hasEdge(originalVerts[(i + 1) % c], ve[i]) == null) newLines.add(new GData2(24, Colour.lineColourR, Colour.lineColourG, Colour.lineColourB, 1f, originalVerts[(i + 1) % c], ve[i], View.DUMMY_REFERENCE, linkedDatFile, true));
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
        roundSelection(6, 10, true, false, true, true, true);

        SortedSet<Vertex> verticesToMove = new TreeSet<>();

        for (GData3 tri : selectedTriangles) {
            Vertex[] verts = triangles.get(tri);
            verticesToMove.addAll(Arrays.asList(verts));
        }

        int newContentSize = newLines.size() + newTris.size();

        clearSelection();

        // Calculate new points, based on Loop's Algorithm
        for (Entry<Vertex, Set<VertexManifestation>> entry : vertexLinkedToPositionInFile.entrySet()) {
            Vertex v = entry.getKey();
            Set<VertexManifestation> manis = entry.getValue();
            Set<Vector3d> midEdge = new HashSet<>();
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
                double n = midEdge.size() + 1d;
                double t = 3.0 / 8.0 + 1.0 / 4.0 * Math.cos(Math.PI * 2.0 / n);
                double alphaN = 3.0 / 8.0 + t * t;
                BigDecimal oneMinusAlphaDivN = new BigDecimal((1.0 - alphaN) / n);
                BigDecimal alphaN2 = new BigDecimal(alphaN);
                Vector3d np = new Vector3d();
                for (Vector3d vd : midEdge) {
                    np = Vector3d.add(np, vd);
                }
                np.setX(v.xp.multiply(alphaN2).add(np.x.multiply(oneMinusAlphaDivN, Threshold.MC)));
                np.setY(v.yp.multiply(alphaN2).add(np.y.multiply(oneMinusAlphaDivN, Threshold.MC)));
                np.setZ(v.zp.multiply(alphaN2).add(np.z.multiply(oneMinusAlphaDivN, Threshold.MC)));
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
        roundSelection(6, 10, true, false, true, true, true);

        setModified(true, true);
        validateState();
    }
}
