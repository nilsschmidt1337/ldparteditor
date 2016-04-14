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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.nschmidt.ldparteditor.enums.View;

class VM18LineConverter extends VM17Unificator {

    protected VM18LineConverter(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public final void condlineToLine() {

        final Set<GData5> condlinesToDelete = new HashSet<GData5>();
        final Set<GData2> newLines = new HashSet<GData2>();

        final Set<GData5> condlinesToParse = new HashSet<GData5>();

        condlinesToParse.addAll(selectedCondlines);

        clearSelection();

        for (Iterator<GData5> ig = condlinesToParse.iterator(); ig.hasNext();) {
            GData5 g = ig.next();
            if (!lineLinkedToVertices.containsKey(g)) {
                ig.remove();
            }
        }
        if (condlinesToParse.isEmpty()) {
            return;
        } else {
            setModified_NoSync();
        }
        condlinesToDelete.addAll(condlinesToParse);

        for (GData5 g5 : condlinesToParse) {
            Vertex[] v = condlines.get(g5);
            GData2 line = new GData2(g5.colourNumber, g5.r, g5.g, g5.b, g5.a, v[0], v[1], View.DUMMY_REFERENCE, linkedDatFile, true);
            newLines.add(line);
            linkedDatFile.insertAfter(g5, line);
        }

        selectedCondlines.addAll(condlinesToDelete);
        selectedData.addAll(condlinesToDelete);
        delete(false, false);

        selectedLines.addAll(newLines);
        selectedData.addAll(newLines);

        if (isModified()) {
            setModified(true, true);
        }
        validateState();

    }

    public final void lineToCondline() {

        final Set<GData2> linesToDelete = new HashSet<GData2>();
        final Set<GData5> newCondlines = new HashSet<GData5>();

        final Set<GData2> linesToParse = new HashSet<GData2>();

        linesToParse.addAll(selectedLines);

        clearSelection();

        for (Iterator<GData2> ig = linesToParse.iterator(); ig.hasNext();) {
            GData2 g = ig.next();
            if (!lineLinkedToVertices.containsKey(g)) {
                ig.remove();
            }
        }
        if (linesToParse.isEmpty()) {
            return;
        } else {
            setModified_NoSync();
        }
        linesToDelete.addAll(linesToParse);

        for (GData2 g2 : linesToParse) {
            Vertex[] v = lines.get(g2);

            ArrayList<GData> faces = linkedCommonFaces(v[0], v[1]);

            if (faces.size() == 2) {

                TreeSet<Vertex> fv1 = new TreeSet<Vertex>();
                TreeSet<Vertex> fv2 = new TreeSet<Vertex>();

                switch (faces.get(0).type()) {
                case 3:
                {
                    GData3 g3 = (GData3) faces.get(0);
                    Vertex[] v3 = triangles.get(g3);
                    for (Vertex tv : v3) {
                        fv1.add(tv);
                    }
                }
                break;
                case 4:
                {
                    GData4 g4 = (GData4) faces.get(0);
                    Vertex[] v4 = quads.get(g4);
                    for (Vertex tv : v4) {
                        fv1.add(tv);
                    }
                }
                break;
                }
                switch (faces.get(1).type()) {
                case 3:
                {
                    GData3 g3 = (GData3) faces.get(1);
                    Vertex[] v3 = triangles.get(g3);
                    for (Vertex tv : v3) {
                        fv2.add(tv);
                    }
                }
                break;
                case 4:
                {
                    GData4 g4 = (GData4) faces.get(1);
                    Vertex[] v4 = quads.get(g4);
                    for (Vertex tv : v4) {
                        fv2.add(tv);
                    }
                }
                break;
                }

                fv1.remove(v[0]);
                fv1.remove(v[1]);

                fv2.remove(v[0]);
                fv2.remove(v[1]);

                if (fv1.isEmpty() || fv2.isEmpty()) {
                    linesToDelete.remove(g2);
                } else {
                    GData5 cLine = new GData5(g2.colourNumber, g2.r, g2.g, g2.b, g2.a, v[0], v[1], fv1.iterator().next(), fv2.iterator().next(), View.DUMMY_REFERENCE, linkedDatFile);
                    newCondlines.add(cLine);
                    linkedDatFile.insertAfter(g2, cLine);
                }
            } else {
                linesToDelete.remove(g2);
            }
        }

        selectedLines.addAll(linesToDelete);
        selectedData.addAll(linesToDelete);
        delete(false, false);

        selectedCondlines.addAll(newCondlines);
        selectedData.addAll(newCondlines);

        if (isModified()) {
            setModified(true, true);
        }
        validateState();
    }
}
