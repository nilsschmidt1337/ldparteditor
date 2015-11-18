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
package org.nschmidt.ldparteditor.data.tools;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GData2;
import org.nschmidt.ldparteditor.data.GData3;
import org.nschmidt.ldparteditor.data.GData4;
import org.nschmidt.ldparteditor.data.GData5;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.data.VertexInfo;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeHashMap;

/**
 * Removes objects with identical vertices
 *
 * @author nils
 *
 */
public enum IdenticalVertexRemover {
    INSTANCE;

    public static void removeIdenticalVertices(VertexManager vm, DatFile df, boolean syncWithTextEditor, boolean convertQuadsToTriangles) {

        vm.backupSelection();
        vm.clearSelection2();

        ThreadsafeHashMap<GData, Set<VertexInfo>> lineLinkedToVertices = vm.getLineLinkedToVertices();

        Map<GData2, Vertex[]> l = vm.getLines();
        Map<GData3, Vertex[]> t = vm.getTriangles();
        Map<GData4, Vertex[]> q = vm.getQuads();
        Map<GData5, Vertex[]> c = vm.getCondlines();

        final Set<GData2> linesToDelete2 = new HashSet<GData2>();
        final Set<GData3> trisToDelete2 = new HashSet<GData3>();
        final Set<GData4> quadsToDelete2 = new HashSet<GData4>();
        final Set<GData5> clinesToDelete2 = new HashSet<GData5>();
        {
            for (GData2 g2 : l.keySet()) {
                if (!lineLinkedToVertices.containsKey(g2)) continue;
                Vertex[] verts = l.get(g2);
                Set<Vertex> verts2 = new TreeSet<Vertex>();
                for (Vertex vert : verts) {
                    verts2.add(vert);
                }
                if (verts2.size() < 2) {
                    linesToDelete2.add(g2);
                }
            }
            for (GData3 g3 : t.keySet()) {
                if (!lineLinkedToVertices.containsKey(g3)) continue;
                Vertex[] verts = t.get(g3);
                Set<Vertex> verts2 = new TreeSet<Vertex>();
                for (Vertex vert : verts) {
                    verts2.add(vert);
                }
                if (verts2.size() < 3 || g3.isCollinear()) {
                    trisToDelete2.add(g3);
                }
            }
            for (GData4 g4 : q.keySet()) {
                if (!lineLinkedToVertices.containsKey(g4)) continue;
                Vertex[] verts = q.get(g4);
                Set<Vertex> verts2 = new TreeSet<Vertex>();
                for (Vertex vert : verts) {
                    verts2.add(vert);
                }
                if (convertQuadsToTriangles && verts2.size() == 3) {

                    // Quad to triangle conversion!

                    Vertex v1 = null;
                    Vertex v2 = null;
                    Vertex v3 = null;
                    for (Vertex v : verts) {
                        if (verts2.contains(v)) {
                            v1 = v;
                            verts2.remove(v);
                            break;
                        }
                    }
                    for (Vertex v : verts) {
                        if (verts2.contains(v)) {
                            v2 = v;
                            verts2.remove(v);
                            break;
                        }
                    }
                    for (Vertex v : verts) {
                        if (verts2.contains(v)) {
                            v3 = v;
                            verts2.remove(v);
                            break;
                        }
                    }

                    df.insertAfter(g4, new GData3(g4.colourNumber, g4.r, g4.g, g4.b, g4.a, v1, v2, v3, g4.parent, df));
                    quadsToDelete2.add(g4);
                } else if (verts2.size() < 4 || g4.isCollinear()) {
                    quadsToDelete2.add(g4);
                }
            }
            for (GData5 g5 : c.keySet()) {
                if (!lineLinkedToVertices.containsKey(g5)) continue;
                Vertex[] verts = c.get(g5);
                Set<Vertex> verts2 = new TreeSet<Vertex>();
                for (Vertex vert : verts) {
                    verts2.add(vert);
                }
                if (verts2.size() < 4) {
                    clinesToDelete2.add(g5);
                }
            }
        }

        vm.getSelectedLines().addAll(linesToDelete2);
        vm.getSelectedTriangles().addAll(trisToDelete2);
        vm.getSelectedQuads().addAll(quadsToDelete2);
        vm.getSelectedCondlines().addAll(clinesToDelete2);
        vm.getSelectedData().addAll(vm.getSelectedLines());
        vm.getSelectedData().addAll(vm.getSelectedTriangles());
        vm.getSelectedData().addAll(vm.getSelectedQuads());
        vm.getSelectedData().addAll(vm.getSelectedCondlines());

        vm.delete(false, false);
        vm.restoreSelection();

        if (syncWithTextEditor) {
            vm.setModified(true, true);
        } else {
            vm.setModified_NoSync();
        }
    }
}
