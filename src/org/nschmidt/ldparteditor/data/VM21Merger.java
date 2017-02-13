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
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.data.tools.IdenticalVertexRemover;
import org.nschmidt.ldparteditor.data.tools.Merger;
import org.nschmidt.ldparteditor.dialogs.direction.DirectionDialog;
import org.nschmidt.ldparteditor.enums.MergeTo;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.helpers.Manipulator;
import org.nschmidt.ldparteditor.helpers.composite3d.SelectorSettings;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;

public class VM21Merger extends VM20Manipulator {

    protected VM21Merger(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public void merge(MergeTo mode, boolean syncWithTextEditor, boolean directional) {

        if (linkedDatFile.isReadOnly()) return;



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
        }

        Vector3d newVertex = new Vector3d();
        Set<Vertex> originVerts = new TreeSet<Vertex>();

        if (mode != MergeTo.LAST_SELECTED) {
            originVerts.addAll(selectedVertices);
            for (GData2 g : selectedLines) {
                for (Vertex v : lines.get(g)) {
                    originVerts.add(v);
                }
            }
            for (GData3 g : selectedTriangles) {
                for (Vertex v : triangles.get(g)) {
                    originVerts.add(v);
                }
            }
            for (GData4 g : selectedQuads) {
                for (Vertex v : quads.get(g)) {
                    originVerts.add(v);
                }
            }
            for (GData5 g : selectedCondlines) {
                for (Vertex v : condlines.get(g)) {
                    originVerts.add(v);
                }
            }
        }

        switch (mode) {
        case AVERAGE:
            if (originVerts.size() == 0) return;
            for (Vertex v : originVerts) {
                newVertex = Vector3d.add(newVertex, new Vector3d(v));
            }
            final BigDecimal size = new BigDecimal(originVerts.size());
            newVertex.setX(newVertex.X.divide(size, Threshold.mc));
            newVertex.setY(newVertex.Y.divide(size, Threshold.mc));
            newVertex.setZ(newVertex.Z.divide(size, Threshold.mc));
            break;
        case LAST_SELECTED:
            if (lastSelectedVertex == null || !vertexLinkedToPositionInFile.containsKey(lastSelectedVertex)) return;
            newVertex = new Vector3d(lastSelectedVertex);
            lastSelectedVertex = null;
            break;
        case NEAREST_EDGE:
        case NEAREST_EDGE_SPLIT:
        case NEAREST_FACE:
            if (originVerts.size() == 0) return;
            {
                // This is a little bit more complex.
                // First, I had to extend the selection to adjacent data,
                // so the nearest edge will not be adjacent to the (selected) origin vertex

                for (Vertex v : originVerts) {
                    for (VertexManifestation mani : vertexLinkedToPositionInFile.get(v)) {
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

                    // Then invert the selection, so that getMinimalDistanceVertexToLines() will snap on the target

                    selectInverse(new SelectorSettings());

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
                    } else {
                        if (directional) {
                            // FIXME NEEDS IMPLEMENTATION!!!
                            for (Vertex vertex : originVerts) {

                            }
                        } else {
                            for (Vertex vertex : originVerts) {
                                final Vertex target = getMinimalDistanceVertexToSurfaces(vertex);
                                modified = changeVertexDirectFast(vertex, target, true) || modified;
                            }
                        }
                    }
                    clearSelection();

                    if (modified) {
                        IdenticalVertexRemover.removeIdenticalVertices(this, linkedDatFile, false, true);
                        clearSelection();
                        setModified_NoSync();
                    }
                }
            }
            if (syncWithTextEditor) {
                syncWithTextEditors(true);
            }
            return;
        case NEAREST_VERTEX:
            if (originVerts.size() == 0) return;
            {
                float minDist = Float.MAX_VALUE;
                Set<Vertex> allVerticesMinusSelection = new TreeSet<Vertex>();
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
                setModified_NoSync();
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

}
