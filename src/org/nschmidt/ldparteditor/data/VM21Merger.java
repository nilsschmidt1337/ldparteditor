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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.data.tools.IdenticalVertexRemover;
import org.nschmidt.ldparteditor.data.tools.Merger;
import org.nschmidt.ldparteditor.dialogs.direction.DirectionDialog;
import org.nschmidt.ldparteditor.enums.MergeTo;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.Manipulator;
import org.nschmidt.ldparteditor.helpers.composite3d.SelectorSettings;
import org.nschmidt.ldparteditor.helpers.math.Rational;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.helpers.math.Vector3r;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;

public class VM21Merger extends VM20Manipulator {

    protected VM21Merger(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public void merge(MergeTo mode, boolean syncWithTextEditor, boolean directional) {

        if (linkedDatFile.isReadOnly()) return;

        final Vector3r dir;

        // Get a direction if necessary...
        if (directional) {
            if (linkedDatFile.getLastSelectedComposite() == null) {
                return;
            }
            Manipulator manipulator = linkedDatFile.getLastSelectedComposite().getManipulator();
            if (new DirectionDialog(Editor3DWindow.getWindow().getShell(), manipulator).open() != IDialogConstants.OK_ID) {
                return;
            }
            if (!DirectionDialog.calculateDirection(manipulator)) {
                return;
            }
            dir = new Vector3r(new Vertex(DirectionDialog.getDirection()));
        } else {
            dir = null;
        }

        Vector3d newVertex = new Vector3d();
        SortedSet<Vertex> originVerts = new TreeSet<>();

        if (mode != MergeTo.LAST_SELECTED) {
            originVerts.addAll(selectedVertices);
            for (GData2 g : selectedLines) {
                originVerts.addAll(Arrays.asList(lines.get(g)));
            }
            for (GData3 g : selectedTriangles) {
                originVerts.addAll(Arrays.asList(triangles.get(g)));
            }
            for (GData4 g : selectedQuads) {
                originVerts.addAll(Arrays.asList(quads.get(g)));
            }
            for (GData5 g : selectedCondlines) {
                originVerts.addAll(Arrays.asList(condlines.get(g)));
            }
        }

        switch (mode) {
        case AVERAGE:
            if (originVerts.isEmpty()) return;
            for (Vertex v : originVerts) {
                newVertex = Vector3d.add(newVertex, new Vector3d(v));
            }
            final BigDecimal size = new BigDecimal(originVerts.size());
            newVertex.setX(newVertex.x.divide(size, Threshold.MC));
            newVertex.setY(newVertex.y.divide(size, Threshold.MC));
            newVertex.setZ(newVertex.z.divide(size, Threshold.MC));
            break;
        case LAST_SELECTED:
            if (lastSelectedVertex == null || !vertexLinkedToPositionInFile.containsKey(lastSelectedVertex)) return;
            newVertex = new Vector3d(lastSelectedVertex);
            lastSelectedVertex = null;
            break;
        case NEAREST_EDGE:
        case NEAREST_EDGE_SPLIT:
        case NEAREST_FACE:
            if (originVerts.isEmpty()) return;
            {
                // This is a little bit more complex.
                // First, I had to extend the selection to adjacent data,
                // so the nearest edge will not be adjacent to the (selected) origin vertex

                final Set<VertexManifestation> emptySet = new HashSet<>();
                for (Vertex v : originVerts) {
                    for (VertexManifestation mani : vertexLinkedToPositionInFile.getOrDefault(v, emptySet)) {
                        GData gd = mani.getGdata();
                        switch (gd.type()) {
                        case 2:
                            selectedLines.add((GData2) gd);
                            break;
                        case 3:
                            selectedTriangles.add((GData3) gd);
                            break;
                        case 4:
                            selectedQuads.add((GData4) gd);
                            break;
                        case 5:
                            selectedCondlines.add((GData5) gd);
                            break;
                        default:
                            continue;
                        }
                        selectedData.add(gd);
                    }
                }

                // Then invert the selection, so that getMinimalDistanceVertexToLines() will snap on the target

                if (!directional) {
                    selectInverse(new SelectorSettings());
                }

                // And using changeVertexDirectFast() to do the merge
                boolean modified = false;
                if (mode == MergeTo.NEAREST_EDGE) {
                    for (Vertex vertex : originVerts) {
                        final Object[] target = getMinimalDistanceVerticesToLines(vertex, false);
                        modified = changeVertexDirectFast(vertex, (Vertex) target[2], true) || modified;
                    }
                } else if (mode == MergeTo.NEAREST_EDGE_SPLIT) {
                    for (Vertex vertex : originVerts) {
                        final Object[] target = getMinimalDistanceVerticesToLines(vertex, false);
                        modified = changeVertexDirectFast(vertex, (Vertex) target[2], true) || modified;
                        // And split at target position!
                        modified = split((Vertex) target[0], (Vertex) target[1], (Vertex) target[2]) || modified;
                    }
                } else if (directional) {
                    final Set<GData> allSurfs = new HashSet<>();
                    allSurfs.addAll(triangles.keySet());
                    allSurfs.addAll(quads.keySet());
                    for (Vertex vertex : originVerts) {
                        Set<GData> linkedSurfs = getLinkedSurfaces(vertex);
                        Vector3r orig = new Vector3r(vertex);
                        Vector3r r = new Vector3r();
                        Vector3r p1 = new Vector3r();
                        Vector3r p2 = new Vector3r();
                        Vector3r p3 = new Vector3r();
                        Vector3r p4 = new Vector3r();
                        Vertex[] verts;
                        Rational[] distance = new Rational[1];
                        for (GData surf : allSurfs) {
                            if ((!triangles.containsKey(surf) && !quads.containsKey(surf)) || linkedSurfs.contains(surf) || hiddenData.contains(surf) || selectedData.contains(surf)) {
                                continue;
                            }
                            switch (surf.type()) {
                            case 3:
                                GData3 gd3 = (GData3) surf;
                                verts = triangles.get(gd3);
                                if (verts == null) {
                                    continue;
                                }
                                p1.set(verts[0]);
                                p2.set(verts[1]);
                                p3.set(verts[2]);
                                projectRayOnTriangleWithDistance(orig, dir, p1, p2, p3, r, distance);
                                continue;
                            case 4:
                                GData4 gd4 = (GData4) surf;
                                verts = quads.get(gd4);
                                if (verts == null) {
                                    continue;
                                }
                                p1.set(verts[0]);
                                p2.set(verts[1]);
                                p3.set(verts[2]);
                                p4.set(verts[3]);
                                projectRayOnTriangleWithDistance(orig, dir, p1, p2, p3, r, distance);
                                projectRayOnTriangleWithDistance(orig, dir, p3, p4, p1, r, distance);
                                continue;
                            default:
                                continue;
                            }
                        }

                        if (distance[0] != null && distance[0].compareTo(Rational.ZERO) > 0) {
                            final Vertex target = new Vertex(r);
                            modified = changeVertexDirectFast(vertex, target, true) || modified;
                        }
                    }
                } else {
                    GData3 dummyTriangle = new GData3(
                            new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                            new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                            new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                            View.DUMMY_REFERENCE, new GColour(-1, 1f, 1f, 1f, 1f), false);
                    GData4 dummyQuad = new GData4(
                            new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                            new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                            new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                            new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                            View.DUMMY_REFERENCE, new GColour(-1, 1f, 1f, 1f, 1f));
                    selectedTriangles.add(dummyTriangle);
                    selectedQuads.add(dummyQuad);
                    for (Vertex vertex : originVerts) {
                        final Vertex target = getMinimalDistanceVertexToSurfaces(vertex);
                        modified = changeVertexDirectFast(vertex, target, true) || modified;
                    }
                    selectedTriangles.remove(dummyTriangle);
                    selectedQuads.remove(dummyQuad);
                }
                clearSelection();

                if (modified) {
                    IdenticalVertexRemover.removeIdenticalVertices(this, linkedDatFile, false, true);
                    clearSelection();
                    setModifiedNoSync();
                }
            }

            if (syncWithTextEditor) {
                syncWithTextEditors(true);
            }
            return;
        case NEAREST_VERTEX:
            if (originVerts.isEmpty()) return;
            {
                float minDist = Float.MAX_VALUE;
                SortedSet<Vertex> allVerticesMinusSelection = new TreeSet<>();
                allVerticesMinusSelection.addAll(getVertices());
                allVerticesMinusSelection.removeAll(originVerts);
                clearSelection();
                for (Vertex vertex2 : originVerts) {
                    selectedVertices.clear();
                    selectedVertices.add(vertex2);
                    Vertex minVertex = new Vertex(0f, 0f, 0f);
                    Vector4f next = vertex2.toVector4fm();
                    for (Vertex vertex : allVerticesMinusSelection) {
                        Vector4f sub = Vector4f.sub(next, vertex.toVector4fm(), null);
                        float d2 = sub.lengthSquared();
                        if (d2 < minDist) {
                            minVertex = vertex;
                            minDist = d2;
                        }
                    }
                    newVertex = new Vector3d(minVertex);
                    Merger.mergeTo(new Vertex(newVertex), this, linkedDatFile, false);
                }
                clearSelection();
                setModifiedNoSync();
            }
            if (syncWithTextEditor) {
                syncWithTextEditors(true);
            }
            return;
        default:
            return;
        }
        Merger.mergeTo(new Vertex(newVertex), this, linkedDatFile, syncWithTextEditor);
        clearSelection();
        validateState();
    }

    private void projectRayOnTriangleWithDistance(
            Vector3r vector3r, Vector3r dirN,
            Vector3r tv, Vector3r tv2, Vector3r tv3,
            Vector3r r, Rational[] distance) {
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
        Rational u = Vector3r.dot(tvec, pvec).multiply(invDiskr);
        if (u.compareTo(Rational.ZERO) < 0 || u.compareTo(Rational.ONE) > 0)
            return;
        Vector3r qvec = Vector3r.cross(tvec, corner1);
        Rational v = Vector3r.dot(dir2, qvec).multiply(invDiskr);
        if (v.compareTo(Rational.ZERO) < 0 || u.add(v).compareTo(Rational.ONE) > 0)
            return;
        Rational t = Vector3r.dot(corner2, qvec).multiply(invDiskr);
        if (t.compareTo(Rational.ZERO) < 0)
            return;
        if (distance[0] == null || t.compareTo(distance[0]) < 0) {
            r.setX(orig2.x.add(dir2.x.multiply(t)));
            r.setY(orig2.y.add(dir2.y.multiply(t)));
            r.setZ(orig2.z.add(dir2.z.multiply(t)));
            distance[0] = t;
        }
    }
}
