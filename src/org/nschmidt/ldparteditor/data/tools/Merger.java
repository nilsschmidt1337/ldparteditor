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

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.nschmidt.ldparteditor.data.GData2;
import org.nschmidt.ldparteditor.data.GData3;
import org.nschmidt.ldparteditor.data.GData4;
import org.nschmidt.ldparteditor.data.GData5;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.data.VertexManager;

/**
 * Merges selected vertices to a target position
 *
 * @author nils
 *
 */
public enum Merger {
    INSTANCE;

    public static void mergeTo(Vertex newVertex, VertexManager vm, boolean syncWithTextEditor) {

        Set<GData2> lines = vm.getSelectedLines();
        Set<GData3> tris = vm.getSelectedTriangles();
        Set<GData4> quads = vm.getSelectedQuads();
        Set<GData5> clines = vm.getSelectedCondlines();

        Map<GData2, Vertex[]> l = vm.getLines();
        Map<GData3, Vertex[]> t = vm.getTriangles();
        Map<GData4, Vertex[]> q = vm.getQuads();
        Map<GData5, Vertex[]> c = vm.getCondlines();

        Set<Vertex> originVerts = new TreeSet<Vertex>();
        originVerts.addAll(vm.getSelectedVertices());

        for (GData2 g : lines) {
            for (Vertex v : l.get(g)) {
                originVerts.add(v);
            }
        }
        for (GData3 g : tris) {
            for (Vertex v : t.get(g)) {
                originVerts.add(v);
            }
        }
        for (GData4 g : quads) {
            for (Vertex v : q.get(g)) {
                originVerts.add(v);
            }
        }
        for (GData5 g : clines) {
            for (Vertex v : c.get(g)) {
                originVerts.add(v);
            }
        }

        vm.clearSelection();

        if (!originVerts.isEmpty()) {
            boolean modified = false;
            for (Vertex oldVertex : originVerts) {
                modified = vm.changeVertexDirectFast(oldVertex, newVertex, true) || modified;
            }
            if (modified) {

                IdenticalVertexRemover.removeIdenticalVertices(vm, false);

                if (syncWithTextEditor) {
                    vm.setModified(true, true);
                } else {
                    vm.setModified_NoSync();
                }
            }
        }
    }
}
