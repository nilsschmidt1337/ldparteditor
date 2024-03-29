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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.nschmidt.ldparteditor.enumtype.View;

class VM18LineConverter extends VM17Unificator {

    protected VM18LineConverter(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public final void condlineToLine() {
        if (linkedDatFile.isReadOnly()) {
            return;
        }
        
        addSnapshot();
        
        final Set<GData5> condlinesToDelete = new HashSet<>();
        final Set<GData2> newLines = new HashSet<>();

        final Set<GData5> condlinesToParse = new HashSet<>();

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
            setModifiedNoSync();
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
        if (linkedDatFile.isReadOnly()) {
            return;
        }
        
        addSnapshot();

        final Set<GData2> linesToDelete = new HashSet<>();
        final Set<GData5> newCondlines = new HashSet<>();

        final Set<GData2> linesToParse = new HashSet<>();

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
            setModifiedNoSync();
        }
        linesToDelete.addAll(linesToParse);

        for (GData2 g2 : linesToParse) {
            Vertex[] v = lines.get(g2);

            List<GData> faces = linkedCommonFaces(v[0], v[1]);

            if (faces.size() == 2) {

                SortedSet<Vertex> fv1 = new TreeSet<>();
                SortedSet<Vertex> fv2 = new TreeSet<>();

                switch (faces.get(0).type()) {
                case 3:
                {
                    GData3 g3 = (GData3) faces.get(0);
                    Vertex[] v3 = triangles.get(g3);
                    fv1.addAll(Arrays.asList(v3));
                }
                break;
                case 4:
                {
                    GData4 g4 = (GData4) faces.get(0);
                    Vertex[] v4 = quads.get(g4);
                    fv1.addAll(Arrays.asList(v4));
                }
                break;
                default:
                break;
                }
                switch (faces.get(1).type()) {
                case 3:
                {
                    GData3 g3 = (GData3) faces.get(1);
                    Vertex[] v3 = triangles.get(g3);
                    fv2.addAll(Arrays.asList(v3));
                }
                break;
                case 4:
                {
                    GData4 g4 = (GData4) faces.get(1);
                    Vertex[] v4 = quads.get(g4);
                    fv2.addAll(Arrays.asList(v4));
                }
                break;
                default:
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
