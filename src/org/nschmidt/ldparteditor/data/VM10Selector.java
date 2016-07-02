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
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.helpers.composite3d.SelectorSettings;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;

class VM10Selector extends VM09WindingChange {

    protected VM10Selector(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public final void selector(final SelectorSettings ss) {

        linkedDatFile.setDrawSelection(false);
        try
        {
            new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, false, new IRunnableWithProgress()
            {
                @Override
                public void run(final IProgressMonitor m) throws InvocationTargetException, InterruptedException
                {
                    m.beginTask(I18n.VM_Selecting, IProgressMonitor.UNKNOWN);

                    final Set<GColour> allColours = new HashSet<GColour>();
                    final Set<Vector3d> allNormals = new HashSet<Vector3d>();
                    final TreeMap<Vertex, TreeSet<Vertex>> adjaencyByPrecision = new TreeMap<Vertex, TreeSet<Vertex>>();

                    // Get near vertices

                    if (ss.isDistance() && ss.getEqualDistance().compareTo(BigDecimal.ZERO) != 0) {
                        final BigDecimal ds = ss.getEqualDistance().multiply(ss.getEqualDistance());
                        int i = 0;
                        int j = 0;
                        for (Vertex v1 : vertexLinkedToPositionInFile.keySet()) {
                            TreeSet<Vertex> newSet = new TreeSet<Vertex>();
                            newSet.add(v1);
                            for (Vertex v2 : vertexLinkedToPositionInFile.keySet()) {
                                if (j > i) {
                                    Vector3d v3d1 = new Vector3d(v1);
                                    Vector3d v3d2 = new Vector3d(v2);
                                    if (Vector3d.distSquare(v3d1, v3d2).compareTo(ds) < 0) {
                                        newSet.add(v2);
                                    }
                                }
                                j++;
                            }
                            adjaencyByPrecision.put(v1, newSet);
                            i++;
                        }
                    } else {
                        for (Vertex v1 : vertexLinkedToPositionInFile.keySet()) {
                            TreeSet<Vertex> newSet = new TreeSet<Vertex>();
                            newSet.add(v1);
                            adjaencyByPrecision.put(v1, newSet);
                        }
                    }

                    // Get selected colour set

                    if (ss.isColour()) {
                        for (GData1 g : selectedSubfiles) {
                            allColours.add(new GColour(g.colourNumber, g.r, g.g, g.b, g.a));
                        }
                        for (GData2 g : selectedLines) {
                            allColours.add(new GColour(g.colourNumber, g.r, g.g, g.b, g.a));
                        }
                        for (GData3 g : selectedTriangles) {
                            allColours.add(new GColour(g.colourNumber, g.r, g.g, g.b, g.a));
                        }
                        for (GData4 g : selectedQuads) {
                            allColours.add(new GColour(g.colourNumber, g.r, g.g, g.b, g.a));
                        }
                        for (GData5 g : selectedCondlines) {
                            allColours.add(new GColour(g.colourNumber, g.r, g.g, g.b, g.a));
                        }
                    }

                    // Prepare normals for orientation check

                    if (ss.isOrientation()) {

                        fillVertexNormalCache(linkedDatFile.getDrawChainStart());

                        if (ss.getScope() == SelectorSettings.EVERYTHING) {
                            for (GData3 g : selectedTriangles) {
                                if (ss.isNoSubfiles() && !lineLinkedToVertices.containsKey(g)) continue;
                                if (dataLinkedToNormalCACHE.containsKey(g)) {
                                    float[] n = dataLinkedToNormalCACHE.get(g);
                                    allNormals.add(new Vector3d(new BigDecimal(n[0]), new BigDecimal(n[1]), new BigDecimal(n[2])));
                                } else {
                                    Vector4f n = new Vector4f(g.xn, g.yn, g.zn, 1f);
                                    Matrix4f.transform(g.parent.productMatrix, n, n);
                                    allNormals.add(new Vector3d(new BigDecimal(g.xn), new BigDecimal(g.yn), new BigDecimal(g.zn)));
                                }
                            }
                            for (GData4 g : selectedQuads) {
                                if (ss.isNoSubfiles() && !lineLinkedToVertices.containsKey(g)) continue;
                                if (dataLinkedToNormalCACHE.containsKey(g)) {
                                    float[] n = dataLinkedToNormalCACHE.get(g);
                                    allNormals.add(new Vector3d(new BigDecimal(n[0]), new BigDecimal(n[1]), new BigDecimal(n[2])));
                                } else {
                                    Vector4f n = new Vector4f(g.xn, g.yn, g.zn, 1f);
                                    Matrix4f.transform(g.parent.productMatrix, n, n);
                                    allNormals.add(new Vector3d(new BigDecimal(g.xn), new BigDecimal(g.yn), new BigDecimal(g.zn)));
                                }
                            }
                        } else {

                        }
                    }

                    switch (ss.getScope()) {
                    case SelectorSettings.EVERYTHING:
                    {
                        double angle = Math.min(Math.abs(ss.getAngle().doubleValue()), 180.0 - Math.abs(ss.getAngle().doubleValue()));

                        clearSelection2();

                        if (!ss.isOrientation()) {
                            for (GData1 g : vertexCountInSubfile.keySet()) {
                                if (canSelect(null, g, ss, allNormals, allColours, angle)) {
                                    selectedSubfiles.add(g);
                                    selectedData.add(g);
                                }
                            }
                            for (GData2 g : lines.keySet()) {
                                if (canSelect(null, g, ss, allNormals, allColours, angle)) {
                                    selectedLines.add(g);
                                    selectedData.add(g);
                                }
                            }
                            for (GData5 g : condlines.keySet()) {
                                if (canSelect(null, g, ss, allNormals, allColours, angle)) {
                                    selectedCondlines.add(g);
                                    selectedData.add(g);
                                }
                            }
                        }
                        for (GData3 g : triangles.keySet()) {
                            if (canSelect(null, g, ss, allNormals, allColours, angle)) {
                                selectedTriangles.add(g);
                                selectedData.add(g);
                            }
                        }
                        for (GData4 g : quads.keySet()) {
                            if (canSelect(null, g, ss, allNormals, allColours, angle)) {
                                selectedQuads.add(g);
                                selectedData.add(g);
                            }
                        }
                        break;
                    }
                    case SelectorSettings.TOUCHING:
                    case SelectorSettings.CONNECTED:

                        selectedVertices.clear();

                        double angle2 = ss.getAngle().doubleValue();

                        final Set<GData2> lastSelectedLines = new HashSet<GData2>();
                        final Set<GData3> lastSelectedTriangles = new HashSet<GData3>();
                        final Set<GData4> lastSelectedQuads = new HashSet<GData4>();
                        final Set<GData5> lastSelectedCondlines = new HashSet<GData5>();

                        final Set<GData2> newSelectedLines = new HashSet<GData2>();
                        final Set<GData3> newSelectedTriangles = new HashSet<GData3>();
                        final Set<GData4> newSelectedQuads = new HashSet<GData4>();
                        final Set<GData5> newSelectedCondlines = new HashSet<GData5>();

                        final Set<Vertex> addedSelectedVertices = new TreeSet<Vertex>();
                        final Set<GData2> addedSelectedLines = new HashSet<GData2>();
                        final Set<GData3> addedSelectedTriangles = new HashSet<GData3>();
                        final Set<GData4> addedSelectedQuads = new HashSet<GData4>();
                        final Set<GData5> addedSelectedCondlines = new HashSet<GData5>();

                        if (ss.isEdgeStop()) {
                            selectedData.clear();
                            selectedLines.clear();
                            selectedCondlines.clear();
                            selectedSubfiles.clear();

                            // Iterative Selection Spread I (Stops at edges)

                            // Init (Step 0)
                            // Filter invalid selection start (e.g. subfile content), since the selection wont be cleared
                            for (GData3 g : selectedTriangles) {
                                if (!canSelect(null, g, ss, allNormals, allColours, angle2)) {
                                    selectedTriangles.remove(g);
                                    selectedData.remove(g);
                                }
                            }
                            for (GData4 g : selectedQuads) {
                                if (!canSelect(null, g, ss, allNormals, allColours, angle2)) {
                                    selectedQuads.remove(g);
                                    selectedData.remove(g);
                                }
                            }

                            int c2 = selectedTriangles.size();
                            int c3 = selectedQuads.size();

                            lastSelectedTriangles.addAll(selectedTriangles);
                            lastSelectedQuads.addAll(selectedQuads);

                            addedSelectedTriangles.addAll(selectedTriangles);
                            addedSelectedQuads.addAll(selectedQuads);
                            addedSelectedVertices.addAll(selectedVertices);

                            // Interation, Step (1...n), "Select Touching" is only one step
                            do {
                                c2 = addedSelectedTriangles.size();
                                c3 = addedSelectedQuads.size();
                                newSelectedTriangles.clear();
                                newSelectedQuads.clear();

                                HashSet<AccurateEdge> touchingEdges = new HashSet<AccurateEdge>();
                                {
                                    Vertex[] verts;
                                    for (GData3 g : lastSelectedTriangles) {
                                        verts = triangles.get(g);
                                        for (Vertex v : verts) {
                                            TreeSet<Vertex> verts2 = adjaencyByPrecision.get(v);
                                            for (Vertex v2 : verts2) {
                                                addedSelectedVertices.add(v2);
                                            }
                                        }
                                        touchingEdges.add(new AccurateEdge(verts[0], verts[1]));
                                        touchingEdges.add(new AccurateEdge(verts[1], verts[2]));
                                        touchingEdges.add(new AccurateEdge(verts[2], verts[0]));
                                    }
                                    for (GData4 g : lastSelectedQuads) {
                                        verts = quads.get(g);
                                        for (Vertex v : verts) {
                                            TreeSet<Vertex> verts2 = adjaencyByPrecision.get(v);
                                            for (Vertex v2 : verts2) {
                                                addedSelectedVertices.add(v2);
                                            }
                                        }
                                        touchingEdges.add(new AccurateEdge(verts[0], verts[1]));
                                        touchingEdges.add(new AccurateEdge(verts[1], verts[2]));
                                        touchingEdges.add(new AccurateEdge(verts[2], verts[3]));
                                        touchingEdges.add(new AccurateEdge(verts[3], verts[0]));
                                    }
                                }
                                for (AccurateEdge edge : touchingEdges) {
                                    boolean skipEdge = false;
                                    for (GData2 line : lines.keySet()) {
                                        if (hasSameEdge(edge, line, adjaencyByPrecision)) {
                                            skipEdge = true;
                                            break;
                                        }
                                    }
                                    if (!skipEdge) {
                                        for (GData3 g : triangles.keySet()) {
                                            if (!addedSelectedTriangles.contains(g) && canSelect(null, g, ss, allNormals, allColours, angle2) && hasSameEdge(edge, g, adjaencyByPrecision) && isEdgeAdjacent(ss, g, addedSelectedLines, addedSelectedTriangles, addedSelectedQuads, addedSelectedCondlines, newSelectedLines, newSelectedTriangles, newSelectedQuads, newSelectedCondlines, adjaencyByPrecision)) {
                                                if (ss.isOrientation()) {
                                                    int faceCount = 0;
                                                    int angleCount = 0;
                                                    boolean notAdjacent = true;
                                                    for (GData3 g2 : addedSelectedTriangles) {
                                                        if (hasSameEdge(g, g2, adjaencyByPrecision)) {
                                                            faceCount++;
                                                            if (canSelect(g2, g, ss, allNormals, allColours, angle2)) {
                                                                angleCount++;
                                                                notAdjacent = false;
                                                            }
                                                        }
                                                    }
                                                    for (GData4 g2 : addedSelectedQuads) {
                                                        if (hasSameEdge(g, g2, adjaencyByPrecision)) {
                                                            faceCount++;
                                                            if (canSelect(g2, g, ss, allNormals, allColours, angle2)) {
                                                                angleCount++;
                                                                notAdjacent = false;
                                                            }
                                                        }
                                                    }
                                                    if (notAdjacent || faceCount != angleCount) break;
                                                }
                                                addedSelectedTriangles.add(g);
                                                newSelectedTriangles.add(g);
                                                Vertex[] verts = triangles.get(g);
                                                for (Vertex v : verts) {
                                                    TreeSet<Vertex> verts2 = adjaencyByPrecision.get(v);
                                                    for (Vertex v2 : verts2) {
                                                        addedSelectedVertices.add(v2);
                                                    }
                                                }
                                            }
                                        }
                                        for (GData4 g : quads.keySet()) {
                                            if (!addedSelectedQuads.contains(g) && canSelect(null, g, ss, allNormals, allColours, angle2) && hasSameEdge(edge, g, adjaencyByPrecision) && isEdgeAdjacent(ss, g, addedSelectedLines, addedSelectedTriangles, addedSelectedQuads, addedSelectedCondlines, newSelectedLines, newSelectedTriangles, newSelectedQuads, newSelectedCondlines, adjaencyByPrecision)) {
                                                if (ss.isOrientation()) {
                                                    int faceCount = 0;
                                                    int angleCount = 0;
                                                    boolean notAdjacent = true;
                                                    for (GData3 g2 : addedSelectedTriangles) {
                                                        if (hasSameEdge(g, g2, adjaencyByPrecision)) {
                                                            faceCount++;
                                                            if (canSelect(g2, g, ss, allNormals, allColours, angle2)) {
                                                                angleCount++;
                                                                notAdjacent = false;
                                                            }
                                                        }
                                                    }
                                                    for (GData4 g2 : addedSelectedQuads) {
                                                        if (hasSameEdge(g, g2, adjaencyByPrecision)) {
                                                            faceCount++;
                                                            if (canSelect(g2, g, ss, allNormals, allColours, angle2)) {
                                                                angleCount++;
                                                                notAdjacent = false;
                                                            }
                                                        }
                                                    }
                                                    if (notAdjacent || faceCount != angleCount) break;
                                                }
                                                addedSelectedQuads.add(g);
                                                newSelectedQuads.add(g);
                                                Vertex[] verts = quads.get(g);
                                                for (Vertex v : verts) {
                                                    TreeSet<Vertex> verts2 = adjaencyByPrecision.get(v);
                                                    for (Vertex v2 : verts2) {
                                                        addedSelectedVertices.add(v2);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                lastSelectedTriangles.clear();
                                lastSelectedQuads.clear();
                                lastSelectedTriangles.addAll(newSelectedTriangles);
                                lastSelectedQuads.addAll(newSelectedQuads);

                                if (ss.getScope() == SelectorSettings.TOUCHING) break;
                            } while (
                                    c2 != addedSelectedTriangles.size() ||
                                    c3 != addedSelectedQuads.size());

                            if (ss.isVertices()) {
                                selectedVertices.addAll(addedSelectedVertices);
                            }
                            if (ss.isTriangles()) {
                                selectedTriangles.addAll(addedSelectedTriangles);
                                selectedData.addAll(selectedTriangles);
                            }
                            if (ss.isQuads()) {
                                selectedQuads.addAll(addedSelectedQuads);
                                selectedData.addAll(selectedQuads);
                            }

                            // Now add lines and condlines
                            if (ss.isLines()) {
                                for (GData2 g : lines.keySet()) {
                                    if (canSelect(null, g, ss, allNormals, allColours, angle2) && isEdgeAdjacent(ss, g, adjaencyByPrecision)) {
                                        Vertex[] verts = lines.get(g);
                                        if (addedSelectedVertices.contains(verts[0]) && addedSelectedVertices.contains(verts[1])) {
                                            selectedLines.add(g);
                                        }
                                    }
                                }
                                selectedData.addAll(selectedLines);
                            }
                            if (ss.isCondlines()) {
                                for (GData5 g : condlines.keySet()) {
                                    if (canSelect(null, g, ss, allNormals, allColours, angle2) && isEdgeAdjacent(ss, g, adjaencyByPrecision)) {
                                        Vertex[] verts = condlines.get(g);
                                        if (addedSelectedVertices.contains(verts[0]) && addedSelectedVertices.contains(verts[1])) {
                                            selectedCondlines.add(g);
                                        }
                                    }
                                }
                                selectedData.addAll(selectedCondlines);
                            }

                        } else {

                            // Iterative Selection Spread II

                            // Init (Step 0)
                            // Filter invalid selection start (e.g. subfile content), since the selection wont be cleared
                            for (GData1 g : selectedSubfiles) {
                                if (!canSelect(null, g, ss, allNormals, allColours, angle2)) {
                                    selectedSubfiles.remove(g);
                                    selectedData.remove(g);
                                }
                            }
                            for (GData2 g : selectedLines) {
                                if (!canSelect(null, g, ss, allNormals, allColours, angle2)) {
                                    selectedLines.remove(g);
                                    selectedData.remove(g);
                                }
                            }
                            for (GData3 g : selectedTriangles) {
                                if (!canSelect(null, g, ss, allNormals, allColours, angle2)) {
                                    selectedTriangles.remove(g);
                                    selectedData.remove(g);
                                }
                            }
                            for (GData4 g : selectedQuads) {
                                if (!canSelect(null, g, ss, allNormals, allColours, angle2)) {
                                    selectedQuads.remove(g);
                                    selectedData.remove(g);
                                }
                            }
                            for (GData5 g : selectedCondlines) {
                                if (!canSelect(null, g, ss, allNormals, allColours, angle2)) {
                                    selectedCondlines.remove(g);
                                    selectedData.remove(g);
                                }
                            }

                            int c1 = selectedLines.size();
                            int c2 = selectedTriangles.size();
                            int c3 = selectedQuads.size();
                            int c4 = selectedCondlines.size();

                            lastSelectedLines.addAll(selectedLines);
                            lastSelectedTriangles.addAll(selectedTriangles);
                            lastSelectedQuads.addAll(selectedQuads);
                            lastSelectedCondlines.addAll(selectedCondlines);

                            if (!ss.isVertices()) selectedVertices.clear();

                            addedSelectedLines.addAll(selectedLines);
                            addedSelectedTriangles.addAll(selectedTriangles);
                            addedSelectedQuads.addAll(selectedQuads);
                            addedSelectedCondlines.addAll(selectedCondlines);
                            addedSelectedVertices.addAll(selectedVertices);

                            // Interation, Step (1...n), "Select Touching" is only one step
                            do {
                                c1 = addedSelectedLines.size();
                                c2 = addedSelectedTriangles.size();
                                c3 = addedSelectedQuads.size();
                                c4 = addedSelectedCondlines.size();
                                newSelectedLines.clear();
                                newSelectedTriangles.clear();
                                newSelectedQuads.clear();
                                newSelectedCondlines.clear();

                                TreeSet<Vertex> touchingVertices = new TreeSet<Vertex>();
                                {
                                    Vertex[] verts;
                                    for (GData2 g : lastSelectedLines) {
                                        verts = lines.get(g);
                                        for (Vertex v : verts) {
                                            TreeSet<Vertex> verts2 = adjaencyByPrecision.get(v);
                                            for (Vertex v2 : verts2) {
                                                touchingVertices.add(v2);
                                                addedSelectedVertices.add(v2);
                                            }
                                        }
                                    }
                                    for (GData3 g : lastSelectedTriangles) {
                                        verts = triangles.get(g);
                                        for (Vertex v : verts) {
                                            TreeSet<Vertex> verts2 = adjaencyByPrecision.get(v);
                                            for (Vertex v2 : verts2) {
                                                touchingVertices.add(v2);
                                                addedSelectedVertices.add(v2);
                                            }
                                        }
                                    }
                                    for (GData4 g : lastSelectedQuads) {
                                        verts = quads.get(g);
                                        for (Vertex v : verts) {
                                            TreeSet<Vertex> verts2 = adjaencyByPrecision.get(v);
                                            for (Vertex v2 : verts2) {
                                                touchingVertices.add(v2);
                                                addedSelectedVertices.add(v2);
                                            }
                                        }
                                    }
                                    for (GData5 g : lastSelectedCondlines) {
                                        verts = condlines.get(g);
                                        int c = 0;
                                        for (Vertex v : verts) {
                                            if (c > 1) break;
                                            TreeSet<Vertex> verts2 = adjaencyByPrecision.get(v);
                                            for (Vertex v2 : verts2) {
                                                touchingVertices.add(v2);
                                                addedSelectedVertices.add(v2);
                                            }
                                            c++;
                                        }
                                    }
                                }
                                for (Vertex v : touchingVertices) {
                                    Set<VertexManifestation> manis = vertexLinkedToPositionInFile.get(v);
                                    for (VertexManifestation mani : manis) {
                                        GData g = mani.getGdata();
                                        switch (g.type()) {
                                        case 2:
                                            if (!addedSelectedLines.contains(g) && canSelect(null, g, ss, allNormals, allColours, angle2) && isEdgeAdjacent(ss, g, addedSelectedLines, addedSelectedTriangles, addedSelectedQuads, addedSelectedCondlines, newSelectedLines, newSelectedTriangles, newSelectedQuads, newSelectedCondlines, adjaencyByPrecision)) {
                                                addedSelectedLines.add((GData2) g);
                                                newSelectedLines.add((GData2) g);
                                                Vertex[] verts = lines.get(g);
                                                for (Vertex ov : verts) {
                                                    TreeSet<Vertex> verts2 = adjaencyByPrecision.get(ov);
                                                    for (Vertex v2 : verts2) {
                                                        addedSelectedVertices.add(v2);
                                                    }
                                                }
                                            }
                                            break;
                                        case 3:

                                            if (!addedSelectedTriangles.contains(g) && canSelect(null, g, ss, allNormals, allColours, angle2) && isEdgeAdjacent(ss, g, addedSelectedLines, addedSelectedTriangles, addedSelectedQuads, addedSelectedCondlines, newSelectedLines, newSelectedTriangles, newSelectedQuads, newSelectedCondlines, adjaencyByPrecision)) {

                                                if (ss.isOrientation()) {
                                                    // We have to find a selected face, which shares one edge
                                                    int faceCount = 0;
                                                    int angleCount = 0;
                                                    boolean notAdjacent = true;
                                                    for (GData3 g2 : addedSelectedTriangles) {
                                                        if (hasSameEdge(g, g2, adjaencyByPrecision)) {
                                                            faceCount++;
                                                            if (canSelect(g2, g, ss, allNormals, allColours, angle2)) {
                                                                angleCount++;
                                                                notAdjacent = false;
                                                            }
                                                        }
                                                    }
                                                    for (GData4 g2 : addedSelectedQuads) {
                                                        if (hasSameEdge(g, g2, adjaencyByPrecision)) {
                                                            faceCount++;
                                                            if (canSelect(g2, g, ss, allNormals, allColours, angle2)) {
                                                                angleCount++;
                                                                notAdjacent = false;
                                                            }
                                                        }
                                                    }
                                                    if (notAdjacent || faceCount != angleCount) break;
                                                }

                                                addedSelectedTriangles.add((GData3) g);
                                                newSelectedTriangles.add((GData3) g);
                                                Vertex[] verts = triangles.get(g);
                                                for (Vertex ov : verts) {
                                                    TreeSet<Vertex> verts2 = adjaencyByPrecision.get(ov);
                                                    for (Vertex v2 : verts2) {
                                                        addedSelectedVertices.add(v2);
                                                    }
                                                }
                                            }
                                            break;
                                        case 4:
                                            if (!addedSelectedQuads.contains(g) && canSelect(null, g, ss, allNormals, allColours, angle2) && isEdgeAdjacent(ss, g, addedSelectedLines, addedSelectedTriangles, addedSelectedQuads, addedSelectedCondlines, newSelectedLines, newSelectedTriangles, newSelectedQuads, newSelectedCondlines, adjaencyByPrecision)) {

                                                if (ss.isOrientation()) {
                                                    // We have to find a selected face, which shares one edge
                                                    int faceCount = 0;
                                                    int angleCount = 0;
                                                    boolean notAdjacent = true;
                                                    for (GData3 g2 : addedSelectedTriangles) {
                                                        if (hasSameEdge(g, g2, adjaencyByPrecision)) {
                                                            faceCount++;
                                                            if (canSelect(g2, g, ss, allNormals, allColours, angle2)) {
                                                                angleCount++;
                                                            }
                                                        }
                                                    }
                                                    for (GData4 g2 : addedSelectedQuads) {
                                                        if (hasSameEdge(g, g2, adjaencyByPrecision)) {
                                                            faceCount++;
                                                            if (canSelect(g2, g, ss, allNormals, allColours, angle2)) {
                                                                angleCount++;
                                                            }
                                                        }
                                                    }
                                                    if (notAdjacent || faceCount != angleCount) break;
                                                }

                                                addedSelectedQuads.add((GData4) g);
                                                newSelectedQuads.add((GData4) g);
                                                Vertex[] verts = quads.get(g);
                                                for (Vertex ov : verts) {
                                                    TreeSet<Vertex> verts2 = adjaencyByPrecision.get(ov);
                                                    for (Vertex v2 : verts2) {
                                                        addedSelectedVertices.add(v2);
                                                    }
                                                }
                                            }
                                            break;
                                        case 5:
                                            if (!addedSelectedCondlines.contains(g) && canSelect(null, g, ss, allNormals, allColours, angle2) && isEdgeAdjacent(ss, g, addedSelectedLines, addedSelectedTriangles, addedSelectedQuads, addedSelectedCondlines, newSelectedLines, newSelectedTriangles, newSelectedQuads, newSelectedCondlines, adjaencyByPrecision)) {
                                                addedSelectedCondlines.add((GData5) g);
                                                newSelectedCondlines.add((GData5) g);
                                                Vertex[] verts = condlines.get(g);
                                                int c = 0;
                                                for (Vertex ov : verts) {
                                                    if (c > 1) break;
                                                    TreeSet<Vertex> verts2 = adjaencyByPrecision.get(ov);
                                                    for (Vertex v2 : verts2) {
                                                        addedSelectedVertices.add(v2);
                                                    }
                                                    c++;
                                                }
                                            }
                                            break;
                                        default:
                                            break;
                                        }
                                    }
                                }

                                lastSelectedLines.clear();
                                lastSelectedTriangles.clear();
                                lastSelectedQuads.clear();
                                lastSelectedCondlines.clear();
                                lastSelectedLines.addAll(newSelectedLines);
                                lastSelectedTriangles.addAll(newSelectedTriangles);
                                lastSelectedQuads.addAll(newSelectedQuads);
                                lastSelectedCondlines.addAll(newSelectedCondlines);

                                if (ss.getScope() == SelectorSettings.TOUCHING) break;
                            } while (
                                    c1 != addedSelectedLines.size() ||
                                    c2 != addedSelectedTriangles.size() ||
                                    c3 != addedSelectedQuads.size() ||
                                    c4 != addedSelectedCondlines.size());

                            if (ss.isVertices()) {
                                selectedVertices.addAll(addedSelectedVertices);
                            }

                            if (ss.isLines()) {
                                selectedLines.addAll(addedSelectedLines);
                                selectedData.addAll(selectedLines);
                            }

                            if (ss.isTriangles()) {
                                selectedTriangles.addAll(addedSelectedTriangles);
                                selectedData.addAll(selectedTriangles);
                            }

                            if (ss.isQuads()) {
                                selectedQuads.addAll(addedSelectedQuads);
                                selectedData.addAll(selectedQuads);
                            }

                            if (ss.isCondlines()) {
                                selectedCondlines.addAll(addedSelectedCondlines);
                                selectedData.addAll(selectedCondlines);
                            }
                        }

                        break;
                    default:
                        break;
                    }

                    clearVertexNormalCache();
                    
                    // Deselect excluded data types (vertices, lines, etc.)
                    
                    if (!ss.isVertices()) {
                        selectedVertices.clear();
                    }

                    if (!ss.isLines()) {
                        selectedData.removeAll(selectedLines);
                        selectedLines.clear();                    
                    }

                    if (!ss.isTriangles()) {
                        selectedData.removeAll(selectedTriangles);
                        selectedTriangles.clear();
                    }

                    if (!ss.isQuads()) {
                        selectedData.removeAll(selectedQuads);
                        selectedQuads.clear();                      
                    }

                    if (!ss.isCondlines()) {
                        selectedData.removeAll(selectedCondlines);
                        selectedCondlines.clear();                      
                    }

                    // Extend selection to whole subfiles
                    if (ss.isWholeSubfiles() && !ss.isNoSubfiles()) {
                        selectWholeSubfiles();
                    }
                }
            });
        }catch (InvocationTargetException consumed) {
        } catch (InterruptedException consumed) {
        }
        linkedDatFile.setDrawSelection(true);
    }


    private boolean isEdgeAdjacent(SelectorSettings ss, GData g1, TreeMap<Vertex, TreeSet<Vertex>> adjaencyByPrecision) {
        if (ss.isEdgeAdjacency()) {
            return isEdgeAdjacentToSelectedData(g1, null, adjaencyByPrecision);
        }
        return true;
    }

    private boolean isEdgeAdjacent(SelectorSettings ss, GData g1,
            Set<? extends GData> data2,
            Set<? extends GData> data3,
            Set<? extends GData> data4,
            Set<? extends GData> data5,
            Set<? extends GData> data2r,
            Set<? extends GData> data3r,
            Set<? extends GData> data4r,
            Set<? extends GData> data5r,
            TreeMap<Vertex, TreeSet<Vertex>> adjaencyByPrecision) {
        if (ss.isEdgeAdjacency()) {
            HashSet<GData> selectedData = new HashSet<GData>();
            selectedData.addAll(data2);
            selectedData.addAll(data3);
            selectedData.addAll(data4);
            selectedData.addAll(data5);
            selectedData.removeAll(data2r);
            selectedData.removeAll(data3r);
            selectedData.removeAll(data4r);
            selectedData.removeAll(data5r);
            return isEdgeAdjacentToSelectedData(g1, selectedData, adjaencyByPrecision);
        }
        return true;
    }

    private boolean canSelect(GData adjacentTo, GData what, SelectorSettings ss, Set<Vector3d> allNormals, Set<GColour> allColours, double angle) {

        if (ss.isColour()) {
            GColour myColour;
            switch (what.type()) {
            case 1:
                GData1 g1 = (GData1) what;
                myColour = new GColour(g1.colourNumber, g1.r, g1.g, g1.b, g1.a);
                break;
            case 2:
                GData2 g2 = (GData2) what;
                myColour = new GColour(g2.colourNumber, g2.r, g2.g, g2.b, g2.a);
                if (!ss.isLines()) return false;
                break;
            case 3:
                GData3 g3 = (GData3) what;
                myColour = new GColour(g3.colourNumber, g3.r, g3.g, g3.b, g3.a);
                if (!ss.isTriangles()) return false;
                break;
            case 4:
                GData4 g4 = (GData4) what;
                myColour = new GColour(g4.colourNumber, g4.r, g4.g, g4.b, g4.a);
                if (!ss.isQuads()) return false;
                break;
            case 5:
                GData5 g5 = (GData5) what;
                myColour = new GColour(g5.colourNumber, g5.r, g5.g, g5.b, g5.a);
                if (!ss.isCondlines()) return false;
                break;
            default:
                return false;
            }
            // Check Colour
            if (!allColours.contains(myColour)) {
                return false;
            }
        }

        if (ss.isOrientation()) {
            if (adjacentTo == null) {
                // SelectorSettings.EVERYTHING

                // Check normal orientation
                switch (what.type()) {
                case 3:
                {
                    if (!allNormals.isEmpty()) {
                        Vector3d n1;
                        if (dataLinkedToNormalCACHE.containsKey(what)) {
                            float[] n = dataLinkedToNormalCACHE.get(what);
                            n1 = new Vector3d(new BigDecimal(n[0]), new BigDecimal(n[1]), new BigDecimal(n[2]));
                        } else {
                            GData3 g = (GData3) what;
                            n1 = new Vector3d(new BigDecimal(g.xn), new BigDecimal(g.yn), new BigDecimal(g.zn));
                        }
                        int falseCounter = 0;
                        for (Vector3d n2 : allNormals) {
                            double angle2 = Vector3d.angle(n1, n2);
                            if (angle2 > 90.0) angle2 = 180.0 - angle2;
                            if (angle2 > angle) {
                                falseCounter++;
                            }
                        }
                        if (falseCounter == allNormals.size()) {
                            return false;
                        }
                    }
                }
                break;
                case 4:
                {
                    if (!allNormals.isEmpty()) {
                        Vector3d n1;
                        if (dataLinkedToNormalCACHE.containsKey(what)) {
                            float[] n = dataLinkedToNormalCACHE.get(what);
                            n1 = new Vector3d(new BigDecimal(n[0]), new BigDecimal(n[1]), new BigDecimal(n[2]));
                        } else {
                            GData4 g = (GData4) what;
                            n1 = new Vector3d(new BigDecimal(g.xn), new BigDecimal(g.yn), new BigDecimal(g.zn));
                        }
                        int falseCounter = 0;
                        for (Vector3d n2 : allNormals) {
                            double angle2 = Vector3d.angle(n1, n2);
                            if (angle2 > 90.0) angle2 = 180.0 - angle2;
                            if (angle2 > angle) {
                                falseCounter++;
                            }
                        }
                        if (falseCounter == allNormals.size()) {
                            return false;
                        }
                    }
                }
                break;
                default:
                    // Check subfile content
                    return !(ss.isNoSubfiles() && (!lineLinkedToVertices.containsKey(what) || what.type() == 1) || !(ss.isHidden() ^ !hiddenData.contains(what)));
                }
            } else {

                // Check normal orientation
                boolean noBFC = angle < 0.0;
                switch (what.type()) {
                case 3:
                {
                    Vector3d n1;
                    if (dataLinkedToNormalCACHE.containsKey(what)) {
                        float[] n = dataLinkedToNormalCACHE.get(what);
                        n1 = new Vector3d(new BigDecimal(n[0]), new BigDecimal(n[1]), new BigDecimal(n[2]));
                    } else {
                        GData3 g = (GData3) what;
                        n1 = new Vector3d(new BigDecimal(g.xn), new BigDecimal(g.yn), new BigDecimal(g.zn));
                        noBFC = true;
                    }
                    switch (adjacentTo.type()) {
                    case 3:
                    {
                        Vector3d n2;
                        if (dataLinkedToNormalCACHE.containsKey(adjacentTo)) {
                            float[] n = dataLinkedToNormalCACHE.get(adjacentTo);
                            n2 = new Vector3d(new BigDecimal(n[0]), new BigDecimal(n[1]), new BigDecimal(n[2]));
                        } else {
                            GData3 g = (GData3) adjacentTo;
                            n2 = new Vector3d(new BigDecimal(g.xn), new BigDecimal(g.yn), new BigDecimal(g.zn));
                            noBFC = true;
                        }
                        double angle2 = Vector3d.angle(n1, n2);
                        if (noBFC && angle2 > 90.0) angle2 = 180.0 - angle2;
                        if (angle2 > Math.abs(angle)) {
                            return false;
                        }
                    }
                    break;
                    case 4:
                    {
                        Vector3d n2;
                        if (dataLinkedToNormalCACHE.containsKey(adjacentTo)) {
                            float[] n = dataLinkedToNormalCACHE.get(adjacentTo);
                            n2 = new Vector3d(new BigDecimal(n[0]), new BigDecimal(n[1]), new BigDecimal(n[2]));
                        } else {
                            GData4 g = (GData4) adjacentTo;
                            n2 = new Vector3d(new BigDecimal(g.xn), new BigDecimal(g.yn), new BigDecimal(g.zn));
                            noBFC = true;
                        }
                        double angle2 = Vector3d.angle(n1, n2);
                        if (noBFC && angle2 > 90.0) angle2 = 180.0 - angle2;
                        if (angle2 > Math.abs(angle)) {
                            return false;
                        }
                    }
                    break;
                    default:
                        // Check subfile content
                        return !(ss.isNoSubfiles() && (!lineLinkedToVertices.containsKey(what) || what.type() == 1) || !(ss.isHidden() ^ !hiddenData.contains(what)));
                    }
                }
                break;
                case 4:
                {
                    Vector3d n1;
                    if (dataLinkedToNormalCACHE.containsKey(what)) {
                        float[] n = dataLinkedToNormalCACHE.get(what);
                        n1 = new Vector3d(new BigDecimal(n[0]), new BigDecimal(n[1]), new BigDecimal(n[2]));
                    } else {
                        GData4 g = (GData4) what;
                        n1 = new Vector3d(new BigDecimal(g.xn), new BigDecimal(g.yn), new BigDecimal(g.zn));
                        noBFC = true;
                    }

                    switch (adjacentTo.type()) {
                    case 3:
                    {
                        Vector3d n2;
                        if (dataLinkedToNormalCACHE.containsKey(adjacentTo)) {
                            float[] n = dataLinkedToNormalCACHE.get(adjacentTo);
                            n2 = new Vector3d(new BigDecimal(n[0]), new BigDecimal(n[1]), new BigDecimal(n[2]));
                        } else {
                            GData3 g = (GData3) adjacentTo;
                            n2 = new Vector3d(new BigDecimal(g.xn), new BigDecimal(g.yn), new BigDecimal(g.zn));
                            noBFC = true;
                        }
                        double angle2 = Vector3d.angle(n1, n2);
                        if (noBFC && angle2 > 90.0) angle2 = 180.0 - angle2;
                        if (angle2 > angle) {
                            return false;
                        }
                    }
                    break;
                    case 4:
                    {
                        Vector3d n2;
                        if (dataLinkedToNormalCACHE.containsKey(adjacentTo)) {
                            float[] n = dataLinkedToNormalCACHE.get(adjacentTo);
                            n2 = new Vector3d(new BigDecimal(n[0]), new BigDecimal(n[1]), new BigDecimal(n[2]));
                        } else {
                            GData4 g = (GData4) adjacentTo;
                            n2 = new Vector3d(new BigDecimal(g.xn), new BigDecimal(g.yn), new BigDecimal(g.zn));
                            noBFC = true;
                        }
                        double angle2 = Vector3d.angle(n1, n2);
                        if (noBFC && angle2 > 90.0) angle2 = 180.0 - angle2;
                        if (angle2 > angle) {
                            return false;
                        }
                    }
                    break;
                    default:
                        // Check subfile content
                        return !(ss.isNoSubfiles() && (!lineLinkedToVertices.containsKey(what) || what.type() == 1) || !(ss.isHidden() ^ !hiddenData.contains(what)));
                    }
                }
                break;
                default:
                    // Check subfile content
                    return !(ss.isNoSubfiles() && (!lineLinkedToVertices.containsKey(what) || what.type() == 1) || !(ss.isHidden() ^ !hiddenData.contains(what)));
                }
            }
        }
        // Check subfile content
        return !(ss.isNoSubfiles() && (!lineLinkedToVertices.containsKey(what) || what.type() == 1) || !(ss.isHidden() ^ !hiddenData.contains(what)));
    }
}
