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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.nschmidt.ldparteditor.helpers.composite3d.GuiStatusManager;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;

class VM15Flipper extends VM14Splitter {

    protected VM15Flipper(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public void flipSelection() {

        if (linkedDatFile.isReadOnly()) return;

        final boolean adjacentData = Editor3DWindow.getWindow().isMovingAdjacentData();
        Editor3DWindow.getWindow().setMovingAdjacentData(false);
        GuiStatusManager.updateStatus();

        final Set<GData2> newLines = new HashSet<>();
        final Set<GData3> newTriangles = new HashSet<>();
        final Set<GData4> newQuads = new HashSet<>();
        final Set<GData5> newCondlines = new HashSet<>();

        final Set<GData2> effSelectedLines = new HashSet<>();
        final Set<GData3> effSelectedTriangles = new HashSet<>();
        final Set<GData4> effSelectedQuads = new HashSet<>();
        final Set<GData5> effSelectedCondlines = new HashSet<>();

        final Set<GData2> linesToDelete2 = new HashSet<>();
        final Set<GData3> trisToDelete2 = new HashSet<>();
        final Set<GData4> quadsToDelete2 = new HashSet<>();
        final Set<GData5> clinesToDelete2 = new HashSet<>();

        {
            for (GData2 g2 : selectedLines) {
                if (!lineLinkedToVertices.containsKey(g2)) continue;
                effSelectedLines.add(g2);
            }
            for (GData3 g3 : selectedTriangles) {
                if (!lineLinkedToVertices.containsKey(g3)) continue;
                effSelectedTriangles.add(g3);
            }
            for (GData4 g4 : selectedQuads) {
                if (!lineLinkedToVertices.containsKey(g4)) continue;
                effSelectedQuads.add(g4);
            }
            for (GData5 g5 : selectedCondlines) {
                if (!lineLinkedToVertices.containsKey(g5)) continue;
                effSelectedCondlines.add(g5);
            }
        }

        clearSelection();

        // Special case for coloured triangle pairs (Flipper behaviour)
        {
            HashSet<GData3> surfsToIgnore = new HashSet<>();
            HashMap<GData3, GData3> trianglePair = new HashMap<>();
            int i = 0;
            int j = 0;
            // Special case for only two triangles
            if (effSelectedTriangles.size() == 2) {
                for (GData3 s1 : effSelectedTriangles) {
                    for (GData3 s2 : effSelectedTriangles) {
                        if (j > i && !surfsToIgnore.contains(s2)) {
                            if (hasSameEdge(s1, s2)) {
                                surfsToIgnore.add(s1);
                                surfsToIgnore.add(s2);
                                trianglePair.put(s1, s2);
                            }
                        }
                        j++;
                    }
                    i++;
                }
            } else {
                for (GData3 s1 : effSelectedTriangles) {
                    for (GData3 s2 : effSelectedTriangles) {
                        if (j > i && !surfsToIgnore.contains(s2)) {
                            if (s1.colourNumber != 16 && s1.colourNumber == s2.colourNumber && s1.r == s2.r && s1.g == s2.g && s1.b == s2.b && hasSameEdge(s1, s2)) {
                                surfsToIgnore.add(s1);
                                surfsToIgnore.add(s2);
                                trianglePair.put(s1, s2);
                            }
                        }
                        j++;
                    }
                    i++;
                }
            }

            effSelectedTriangles.removeAll(surfsToIgnore);
            for (GData3 s1 : trianglePair.keySet()) {
                GData3 s2 = trianglePair.get(s1);
                {
                    TreeSet<Vertex> v1 = new TreeSet<>();
                    TreeSet<Vertex> v2 = new TreeSet<>();
                    for (Vertex v : triangles.get(s1)) {
                        v1.add(v);
                    }
                    for (Vertex v : triangles.get(s2)) {
                        v2.add(v);
                    }
                    {
                        TreeSet<Vertex> sum = new TreeSet<>();
                        sum.addAll(v1);
                        sum.addAll(v2);
                        if (sum.size() != 4) continue;
                    }
                    v1.retainAll(v2);
                    if (v1.size() == 2) {

                        Vertex first = null;
                        Vertex second = null;
                        Vertex third = null;
                        Vertex fourth = null;
                        int i1 = 0;
                        for (Vertex v : triangles.get(s1)) {
                            if (!v1.contains(v)) {
                                first = v;
                                break;
                            }
                            i1++;
                        }
                        if (first == null) continue;
                        int i2 = 0;
                        for (Vertex v : triangles.get(s1)) {
                            if ((i2 > i1 || i1 == 2 && i2 == 0) && v1.contains(v)) {
                                second = v;
                                break;
                            }
                            i2++;
                        }
                        if (second == null) continue;
                        for (Vertex v : triangles.get(s2)) {
                            if (!v1.contains(v)) {
                                third = v;
                                break;
                            }
                        }
                        if (third == null) continue;
                        for (Vertex v : triangles.get(s2)) {
                            if (v1.contains(v) && !v.equals(second)) {
                                fourth = v;
                                break;
                            }
                        }
                        if (fourth == null) continue;

                        GData3 n1 = new GData3(s1.colourNumber, s1.r, s1.g, s1.b, s1.a, first, second, third, s1.parent, linkedDatFile, s1.isTriangle);
                        GData3 n2 = new GData3(s1.colourNumber, s1.r, s1.g, s1.b, s1.a, third, fourth, first, s1.parent, linkedDatFile, s1.isTriangle);

                        linkedDatFile.insertAfter(s1, n1);
                        linkedDatFile.insertAfter(s2, n2);

                        newTriangles.add(n1);
                        newTriangles.add(n2);

                        trisToDelete2.add(s1);
                        trisToDelete2.add(s2);

                        Iterator<Vertex> vit = v1.iterator();
                        Vertex cv1 = vit.next();
                        Vertex cv2 = vit.next();
                        GData5 cline = hasCondline(cv1, cv2);
                        if (cline != null && lineLinkedToVertices.containsKey(cline)) {
                            linkedDatFile.insertAfter(cline, new GData5(cline.colourNumber, cline.r, cline.g, cline.b, cline.a, first, third, second, fourth, cline.parent, linkedDatFile));
                            clinesToDelete2.add(cline);
                        }

                        // Adjacent Condline Check 1
                        checkAdjacentCondline(clinesToDelete2, first, second, third, fourth);
                        checkAdjacentCondline(clinesToDelete2, first, second, fourth, third);
                        // Adjacent Condline Check 2
                        checkAdjacentCondline(clinesToDelete2, second, third, first, fourth);
                        checkAdjacentCondline(clinesToDelete2, second, third, fourth, first);
                        // Adjacent Condline Check 3
                        checkAdjacentCondline(clinesToDelete2, third, fourth, first, second);
                        checkAdjacentCondline(clinesToDelete2, third, fourth, second, first);
                        // Adjacent Condline Check 4
                        checkAdjacentCondline(clinesToDelete2, fourth, first, third, second);
                        checkAdjacentCondline(clinesToDelete2, fourth, first, second, third);
                    }
                }

            }
        }

        for (GData2 g : effSelectedLines) {
            GData2 n = new GData2(g.colourNumber, g.r, g.g, g.b, g.a, g.x2p, g.y2p, g.z2p, g.x1p, g.y1p, g.z1p, g.parent, linkedDatFile, g.isLine);
            newLines.add(n);
            linkedDatFile.insertAfter(g, n);
            linesToDelete2.add(g);
        }

        for (GData3 g : effSelectedTriangles) {
            GData3 n = new GData3(g.colourNumber, g.r, g.g, g.b, g.a, new Vertex(g.x3p, g.y3p, g.z3p), new Vertex(g.x1p, g.y1p, g.z1p), new Vertex(g.x2p, g.y2p, g.z2p), g.parent, linkedDatFile, g.isTriangle);
            newTriangles.add(n);
            linkedDatFile.insertAfter(g, n);
            trisToDelete2.add(g);
        }

        for (GData4 g : effSelectedQuads) {
            GData4 n = new GData4(g.colourNumber, g.r, g.g, g.b, g.a, new Vertex(g.x4p, g.y4p, g.z4p), new Vertex(g.x1p, g.y1p, g.z1p), new Vertex(g.x2p, g.y2p, g.z2p), new Vertex(g.x3p, g.y3p, g.z3p), g.parent, linkedDatFile);
            newQuads.add(n);
            linkedDatFile.insertAfter(g, n);
            quadsToDelete2.add(g);
        }

        for (GData5 g : effSelectedCondlines) {
            if (clinesToDelete2.contains(g)) {
                continue;
            }
            GData5 n = new GData5(g.colourNumber, g.r, g.g, g.b, g.a, new Vertex(g.x2p, g.y2p, g.z2p), new Vertex(g.x1p, g.y1p, g.z1p), new Vertex(g.x3p, g.y3p, g.z3p), new Vertex(g.x4p, g.y4p, g.z4p), g.parent, linkedDatFile);
            newCondlines.add(n);
            linkedDatFile.insertAfter(g, n);
            clinesToDelete2.add(g);
        }

        if (newLines.size() + newTriangles.size() + newQuads.size() + newCondlines.size() > 0) {
            setModifiedNoSync();
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

        clearSelection();

        selectedLines.addAll(newLines);
        selectedTriangles.addAll(newTriangles);
        selectedQuads.addAll(newQuads);
        selectedCondlines.addAll(newCondlines);
        selectedData.addAll(selectedLines);
        selectedData.addAll(selectedTriangles);
        selectedData.addAll(selectedQuads);
        selectedData.addAll(selectedCondlines);

        Editor3DWindow.getWindow().setMovingAdjacentData(adjacentData);

        if (isModified()) {
            syncWithTextEditors(true);
        }
        validateState();
    }

    private void checkAdjacentCondline(final Set<GData5> clinesToDelete,
            Vertex start, Vertex end, Vertex controlPoint, Vertex mergeTarget) {
        final GData5 cline= hasCondline(start, end);
        if (cline != null && lineLinkedToVertices.containsKey(cline)) {
            Vertex[] verts = condlines.get(cline);
            if (verts != null) {
                Vertex backupVert = null;
                if (verts[2].equals(controlPoint)) {
                    backupVert = verts[3];
                } else if (verts[3].equals(controlPoint)) {
                    backupVert = verts[2];
                }
                if (backupVert != null) {
                    linkedDatFile.insertAfter(cline, new GData5(cline.colourNumber, cline.r, cline.g, cline.b, cline.a, verts[0], verts[1], backupVert, mergeTarget, cline.parent, linkedDatFile));
                    clinesToDelete.add(cline);
                }
            }
        }
    }
}
