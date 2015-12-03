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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.math.HashBiMap;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeTreeMap;
import org.nschmidt.ldparteditor.text.DatParser;
import org.nschmidt.ldparteditor.text.StringHelper;

class VM99Clipboard extends VM19ColourChanger {

    private static final List<GData> CLIPBOARD = new ArrayList<GData>();
    private static final Set<GData> CLIPBOARD_InvNext = Collections.newSetFromMap(new ThreadsafeHashMap<GData, Boolean>());

    protected VM99Clipboard(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public static List<GData> getClipboard() {
        return CLIPBOARD;
    }

    public void copy() {

        // Has to copy what IS selected, nothing more, nothing less.

        CLIPBOARD.clear();
        CLIPBOARD_InvNext.clear();

        final Set<Vertex> singleVertices = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());

        final HashSet<GData2> effSelectedLines = new HashSet<GData2>();
        final HashSet<GData3> effSelectedTriangles = new HashSet<GData3>();
        final HashSet<GData4> effSelectedQuads = new HashSet<GData4>();
        final HashSet<GData5> effSelectedCondlines = new HashSet<GData5>();

        final TreeSet<Vertex> effSelectedVertices2 = new TreeSet<Vertex>(selectedVertices);
        final HashSet<GData2> effSelectedLines2 = new HashSet<GData2>(selectedLines);
        final HashSet<GData3> effSelectedTriangles2 = new HashSet<GData3>(selectedTriangles);
        final HashSet<GData4> effSelectedQuads2 = new HashSet<GData4>(selectedQuads);
        final HashSet<GData5> effSelectedCondlines2 = new HashSet<GData5>(selectedCondlines);

        selectedData.clear();

        {
            final Set<Vertex> objectVertices = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());
            // 0. Deselect selected subfile data I (for whole selected subfiles)
            for (GData1 subf : selectedSubfiles) {
                Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
                for (VertexInfo vertexInfo : vis) {
                    selectedVertices.remove(vertexInfo.getVertex());
                    GData g = vertexInfo.getLinkedData();
                    switch (g.type()) {
                    case 2:
                        selectedLines.remove(g);
                        {
                            Vertex[] verts = lines.get(g);
                            if (verts == null)
                                continue;
                            for (Vertex vertex : verts) {
                                objectVertices.add(vertex);
                            }
                        }
                        break;
                    case 3:
                        selectedTriangles.remove(g);
                        {
                            Vertex[] verts = triangles.get(g);
                            if (verts == null)
                                continue;
                            for (Vertex vertex : verts) {
                                objectVertices.add(vertex);
                            }
                        }
                        break;
                    case 4:
                        selectedQuads.remove(g);
                        {
                            Vertex[] verts = quads.get(g);
                            if (verts == null)
                                continue;
                            for (Vertex vertex : verts) {
                                objectVertices.add(vertex);
                            }
                        }
                        break;
                    case 5:
                        selectedCondlines.remove(g);
                        {
                            Vertex[] verts = condlines.get(g);
                            if (verts == null)
                                continue;
                            for (Vertex vertex : verts) {
                                objectVertices.add(vertex);
                            }
                        }
                        break;
                    default:
                        break;
                    }
                }
            }
            // 1. Vertex Based Selection

            {
                HashMap<GData, Integer> occurMap = new HashMap<GData, Integer>();
                for (Vertex vertex : selectedVertices) {
                    Set<VertexManifestation> occurences = vertexLinkedToPositionInFile.get(vertex);
                    if (occurences == null)
                        continue;
                    for (VertexManifestation vm : occurences) {
                        GData g = vm.getGdata();
                        int val = 1;
                        int type = g.type();
                        if (occurMap.containsKey(g)) {
                            val = occurMap.get(g);
                            if (type != 5 || vm.getPosition() < 2) {
                                val++;
                                occurMap.put(g, val);
                            }
                        } else if (type != 5 || vm.getPosition() < 2) {
                            occurMap.put(g, val);
                        }
                        switch (type) {
                        case 2:
                            GData2 line = (GData2) g;
                            if (val == 2) {
                                selectedLines.add(line);
                            }
                            break;
                        case 3:
                            GData3 triangle = (GData3) g;
                            if (val == 3) {
                                selectedTriangles.add(triangle);
                            }
                            break;
                        case 4:
                            GData4 quad = (GData4) g;
                            if (val == 4) {
                                selectedQuads.add(quad);
                            }
                            break;
                        case 5:
                            GData5 condline = (GData5) g;
                            if (val == 2) {
                                selectedCondlines.add(condline);
                            }
                            break;
                        }
                    }
                }
            }

            // 2. Deselect selected subfile data II (for whole selected
            // subfiles, remove all from selection, which belongs to a
            // completely selected subfile)
            for (GData1 subf : selectedSubfiles) {
                Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
                for (VertexInfo vertexInfo : vis) {
                    GData g = vertexInfo.getLinkedData();
                    switch (g.type()) {
                    case 2:
                        selectedLines.remove(g);
                        {
                            Vertex[] verts = lines.get(g);
                            if (verts == null)
                                continue;
                            for (Vertex vertex : verts) {
                                objectVertices.add(vertex);
                            }
                        }
                        break;
                    case 3:
                        selectedTriangles.remove(g);
                        {
                            Vertex[] verts = triangles.get(g);
                            if (verts == null)
                                continue;
                            for (Vertex vertex : verts) {
                                objectVertices.add(vertex);
                            }
                        }
                        break;
                    case 4:
                        selectedQuads.remove(g);
                        {
                            Vertex[] verts = quads.get(g);
                            if (verts == null)
                                continue;
                            for (Vertex vertex : verts) {
                                objectVertices.add(vertex);
                            }
                        }
                        break;
                    case 5:
                        selectedCondlines.remove(g);
                        {
                            Vertex[] verts = condlines.get(g);
                            if (verts == null)
                                continue;
                            for (Vertex vertex : verts) {
                                objectVertices.add(vertex);
                            }
                        }
                        break;
                    default:
                        break;
                    }
                }
            }

            // 3. Object Based Selection

            for (GData2 line : selectedLines) {
                if (line.parent.equals(View.DUMMY_REFERENCE)) {
                    effSelectedLines.add(line);
                } else {
                    Vertex[] verts = lines.get(line);
                    if (verts == null)
                        continue;
                    effSelectedLines.add(new GData2(verts[0], verts[1], line.parent, new GColour(line.colourNumber, line.r, line.g, line.b, line.a)));
                }
                Vertex[] verts = lines.get(line);
                if (verts == null)
                    continue;
                for (Vertex vertex : verts) {
                    objectVertices.add(vertex);
                }
            }
            for (GData3 triangle : selectedTriangles) {
                if (triangle.parent.equals(View.DUMMY_REFERENCE)) {
                    effSelectedTriangles.add(triangle);
                } else {
                    Vertex[] verts = triangles.get(triangle);
                    if (verts == null)
                        continue;
                    effSelectedTriangles.add(new GData3(verts[0], verts[1], verts[2], triangle.parent, new GColour(triangle.colourNumber, triangle.r, triangle.g, triangle.b, triangle.a)));
                }
                Vertex[] verts = triangles.get(triangle);
                if (verts == null)
                    continue;
                for (Vertex vertex : verts) {
                    objectVertices.add(vertex);
                }
            }
            for (GData4 quad : selectedQuads) {
                if (quad.parent.equals(View.DUMMY_REFERENCE)) {
                    effSelectedQuads.add(quad);
                } else {
                    Vertex[] verts = quads.get(quad);
                    if (verts == null)
                        continue;
                    effSelectedQuads.add(new GData4(verts[0], verts[1], verts[2], verts[3], quad.parent, new GColour(quad.colourNumber, quad.r, quad.g, quad.b, quad.a)));
                }
                Vertex[] verts = quads.get(quad);
                if (verts == null)
                    continue;
                for (Vertex vertex : verts) {
                    objectVertices.add(vertex);
                }
            }
            for (GData5 condline : selectedCondlines) {
                if (condline.parent.equals(View.DUMMY_REFERENCE)) {
                    effSelectedCondlines.add(condline);
                } else {
                    Vertex[] verts = condlines.get(condline);
                    if (verts == null)
                        continue;
                    effSelectedCondlines.add(new GData5(verts[0], verts[1], verts[2], verts[3], condline.parent,
                            new GColour(condline.colourNumber, condline.r, condline.g, condline.b, condline.a)));
                }
                Vertex[] verts = condlines.get(condline);
                if (verts == null)
                    continue;
                for (Vertex vertex : verts) {
                    objectVertices.add(vertex);
                }
            }

            singleVertices.addAll(selectedVertices);
            singleVertices.removeAll(objectVertices);

            // 4. Copy of the selected data (no whole subfiles!!)

            CLIPBOARD.addAll(effSelectedLines);
            CLIPBOARD.addAll(effSelectedTriangles);
            CLIPBOARD.addAll(effSelectedQuads);
            CLIPBOARD.addAll(effSelectedCondlines);

            // 4. Subfile Based Copy (with INVERTNEXT)
            if (!selectedSubfiles.isEmpty()) {
                for (GData1 subf : selectedSubfiles) {
                    boolean hasInvertnext = false;
                    GData invertNextData = subf.getBefore();
                    while (invertNextData != null && invertNextData.type() != 1 && (invertNextData.type() != 6 || ((GDataBFC) invertNextData).type != BFC.INVERTNEXT)) {
                        invertNextData = invertNextData.getBefore();
                    }
                    if (invertNextData != null && invertNextData.type() == 6) {
                        hasInvertnext = true;
                    }
                    if (hasInvertnext) {
                        CLIPBOARD_InvNext.add(subf);
                    }
                }
                CLIPBOARD.addAll(selectedSubfiles);
            }


            // Sort the clipboard content by linenumber (or ID if the linenumber is the same)

            {
                final HashBiMap<Integer, GData> dpl = linkedDatFile.getDrawPerLine_NOCLONE();
                Collections.sort(CLIPBOARD, new Comparator<GData>(){
                    @Override
                    public int compare(GData o1, GData o2) {
                        if (dpl.containsValue(o1)) {
                            if (dpl.containsValue(o2)) {
                                return dpl.getKey(o1).compareTo(dpl.getKey(o2));
                            } else {
                                switch (o2.type()) {
                                case 1:
                                    return dpl.getKey(o1).compareTo(dpl.getKey(((GData1) o2).firstRef));
                                case 2:
                                    return dpl.getKey(o1).compareTo(dpl.getKey(((GData2) o2).parent.firstRef));
                                case 3:
                                    return dpl.getKey(o1).compareTo(dpl.getKey(((GData3) o2).parent.firstRef));
                                case 4:
                                    return dpl.getKey(o1).compareTo(dpl.getKey(((GData4) o2).parent.firstRef));
                                case 5:
                                    return dpl.getKey(o1).compareTo(dpl.getKey(((GData5) o2).parent.firstRef));
                                default:
                                    GData t = o2.getBefore();
                                    while (t != null && t.getBefore() != null) {
                                        t = t.getBefore();
                                    }
                                    return dpl.getKey(o1).compareTo(dpl.getKey(((GDataInit) t).getParent().firstRef));
                                }
                            }
                        } else {
                            if (dpl.containsValue(o2)) {
                                switch (o1.type()) {
                                case 1:
                                    return dpl.getKey(((GData1) o1).firstRef).compareTo(dpl.getKey(o2));
                                case 2:
                                    return dpl.getKey(((GData2) o1).parent.firstRef).compareTo(dpl.getKey(o2));
                                case 3:
                                    return dpl.getKey(((GData3) o1).parent.firstRef).compareTo(dpl.getKey(o2));
                                case 4:
                                    return dpl.getKey(((GData4) o1).parent.firstRef).compareTo(dpl.getKey(o2));
                                case 5:
                                    return dpl.getKey(((GData5) o1).parent.firstRef).compareTo(dpl.getKey(o2));
                                default:
                                    GData t = o2.getBefore();
                                    while (t != null && t.getBefore() != null) {
                                        t = t.getBefore();
                                    }
                                    return dpl.getKey(((GDataInit) t).getParent().firstRef).compareTo(dpl.getKey(o2));
                                }
                            } else {
                                final Integer co1;
                                final Integer co2;
                                {
                                    switch (o1.type()) {
                                    case 1:
                                        co1 = dpl.getKey(((GData1) o1).firstRef);
                                        break;
                                    case 2:
                                        co1 = dpl.getKey(((GData2) o1).parent.firstRef);
                                        break;
                                    case 3:
                                        co1 = dpl.getKey(((GData3) o1).parent.firstRef);
                                        break;
                                    case 4:
                                        co1 = dpl.getKey(((GData4) o1).parent.firstRef);
                                        break;
                                    case 5:
                                        co1 = dpl.getKey(((GData5) o1).parent.firstRef);
                                        break;
                                    default:
                                        GData t = o2.getBefore();
                                        while (t != null && t.getBefore() != null) {
                                            t = t.getBefore();
                                        }
                                        co1 = dpl.getKey(((GDataInit) t).getParent().firstRef);
                                    }
                                }
                                {
                                    switch (o2.type()) {
                                    case 1:
                                        co2 = dpl.getKey(((GData1) o2).firstRef);
                                        break;
                                    case 2:
                                        co2 = dpl.getKey(((GData2) o2).parent.firstRef);
                                        break;
                                    case 3:
                                        co2 = dpl.getKey(((GData3) o2).parent.firstRef);
                                        break;
                                    case 4:
                                        co2 = dpl.getKey(((GData4) o2).parent.firstRef);
                                        break;
                                    case 5:
                                        co2 = dpl.getKey(((GData5) o2).parent.firstRef);
                                        break;
                                    default:
                                        GData t = o2.getBefore();
                                        while (t != null && t.getBefore() != null) {
                                            t = t.getBefore();
                                        }
                                        co2 = dpl.getKey(((GDataInit) t).getParent().firstRef);
                                    }
                                }
                                int comparism = co1.compareTo(co2);
                                if (comparism == 0) {
                                    if (o1.ID > o2.ID) { // The id can "never" be equal!
                                        return 1;
                                    } else {
                                        return -1;
                                    }
                                } else {
                                    return comparism;
                                }
                            }
                        }
                    }
                });
            }

            for (Vertex v : singleVertices) {
                CLIPBOARD.add(new GData0("0 !LPE VERTEX " + bigDecimalToString(v.X) + " " + bigDecimalToString(v.Y) + " " + bigDecimalToString(v.Z))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$)
            }

            // 5. Create text data entry in the OS clipboard
            final StringBuilder cbString = new StringBuilder();
            for (GData data : CLIPBOARD) {
                if (CLIPBOARD_InvNext.contains(data)) {
                    cbString.append("0 BFC INVERTNEXT"); //$NON-NLS-1$
                    cbString.append(StringHelper.getLineDelimiter());
                    cbString.append(data.toString());
                    cbString.append(StringHelper.getLineDelimiter());
                } else {
                    cbString.append(data.toString());
                    cbString.append(StringHelper.getLineDelimiter());
                }
            }

            final String cbs = cbString.toString();
            if (!cbs.isEmpty()) {
                Display display = Display.getCurrent();
                Clipboard clipboard = new Clipboard(display);
                clipboard.setContents(new Object[] { cbs }, new Transfer[] { TextTransfer.getInstance() });
                clipboard.dispose();
            }

            // 6. Restore selection

            // Reduce the amount of superflous selected data
            selectedVertices.clear();
            selectedVertices.addAll(effSelectedVertices2);

            selectedLines.clear();
            selectedLines.addAll(effSelectedLines2);

            selectedTriangles.clear();
            selectedTriangles.addAll(effSelectedTriangles2);

            selectedQuads.clear();
            selectedQuads.addAll(effSelectedQuads2);

            selectedCondlines.clear();
            selectedCondlines.addAll(effSelectedCondlines2);

            selectedData.addAll(selectedLines);
            selectedData.addAll(selectedTriangles);
            selectedData.addAll(selectedQuads);
            selectedData.addAll(selectedCondlines);
            selectedData.addAll(selectedSubfiles);

            selectedVertices.retainAll(vertexLinkedToPositionInFile.keySet());
        }

    }

    public void paste() {
        if (linkedDatFile.isReadOnly())
            return;
        if (!CLIPBOARD.isEmpty()) {
            clearSelection();
            final HashBiMap<Integer, GData> dpl = linkedDatFile.getDrawPerLine_NOCLONE();

            int linecount = dpl.size();

            GData before = linkedDatFile.getDrawChainTail();
            GData tailData = null;
            for (GData g : CLIPBOARD) {
                Set<String> alreadyParsed = new HashSet<String>();
                alreadyParsed.add(linkedDatFile.getShortName());
                if (CLIPBOARD_InvNext.contains(g)) {
                    GDataBFC invNext = new GDataBFC(BFC.INVERTNEXT);
                    before.setNext(invNext);
                    before = invNext;
                    linecount++;
                    dpl.put(linecount, invNext);
                }
                ArrayList<ParsingResult> result = DatParser.parseLine(g.toString(), -1, 0, 0.5f, 0.5f, 0.5f, 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile, false, alreadyParsed, false);
                GData pasted = result.get(0).getGraphicalData();
                if (pasted == null)
                    pasted = new GData0(g.toString());
                linecount++;
                dpl.put(linecount, pasted);
                selectedData.add(pasted);
                switch (pasted.type()) {
                case 0:
                    selectedData.remove(pasted);
                    Vertex vertex = ((GData0) pasted).getVertex();
                    if (vertex != null) {
                        selectedVertices.add(vertex);
                    }
                    break;
                case 1:
                    selectedSubfiles.add((GData1) pasted);
                    Set<VertexInfo> vis = lineLinkedToVertices.get(pasted);
                    for (VertexInfo vertexInfo : vis) {
                        selectedVertices.add(vertexInfo.getVertex());
                        GData gs = vertexInfo.getLinkedData();
                        selectedData.add(gs);
                        switch (gs.type()) {
                        case 0:
                            selectedData.remove(gs);
                            Vertex vertex2 = ((GData0) gs).getVertex();
                            if (vertex2 != null) {
                                selectedVertices.add(vertex2);
                            }
                            break;
                        case 2:
                            selectedLines.add((GData2) gs);
                            break;
                        case 3:
                            selectedTriangles.add((GData3) gs);
                            break;
                        case 4:
                            selectedQuads.add((GData4) gs);
                            break;
                        case 5:
                            selectedCondlines.add((GData5) gs);
                            break;
                        default:
                            break;
                        }
                    }
                    break;
                case 2:
                    selectedLines.add((GData2) pasted);
                    break;
                case 3:
                    selectedTriangles.add((GData3) pasted);
                    break;
                case 4:
                    selectedQuads.add((GData4) pasted);
                    break;
                case 5:
                    selectedCondlines.add((GData5) pasted);
                    break;
                default:
                    break;
                }
                before.setNext(pasted);
                before = pasted;
                tailData = pasted;
            }
            linkedDatFile.setDrawChainTail(tailData);
            setModified(true, true);
            updateUnsavedStatus();
        }
    }
}
