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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.enums.ManipulatorScope;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.composite3d.SelectorSettings;
import org.nschmidt.ldparteditor.helpers.math.HashBiMap;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeTreeMap;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.text.DatParser;
import org.nschmidt.ldparteditor.text.StringHelper;

class VM99Clipboard extends VM28SlantingMatrixProjector {

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
                if (vis == null) continue;
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
                if (vis == null) continue;
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
                    effSelectedLines.add(new GData2(verts[0], verts[1], line.parent, new GColour(line.colourNumber, line.r, line.g, line.b, line.a), line.isLine));
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
                    effSelectedTriangles.add(new GData3(verts[0], verts[1], verts[2], triangle.parent, new GColour(triangle.colourNumber, triangle.r, triangle.g, triangle.b, triangle.a), true));
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
            for (Iterator<GData> ci = CLIPBOARD.iterator(); ci.hasNext();) {
                if (ci.next() == null) ci.remove();
            }
            // Sort the clipboard content by linenumber (or ID if the linenumber is the same)
            {
                final HashBiMap<Integer, GData> dpl = linkedDatFile.getDrawPerLine_NOCLONE();
                Collections.sort(CLIPBOARD, new Comparator<GData>(){
                    @Override
                    public int compare(GData o1, GData o2) {
                        try {
                            if (dpl.containsValue(o1)) {
                                if (dpl.containsValue(o2)) {
                                    return dpl.getKey(o1).compareTo(dpl.getKey(o2));
                                } else {
                                    if (o2.type() == 1) {
                                        return dpl.getKey(o1).compareTo(dpl.getKey(((GData1) o2).firstRef));
                                    } else {
                                        return dpl.getKey(o1).compareTo(dpl.getKey(o2.parent.firstRef));
                                    }
                                }
                            } else {
                                if (dpl.containsValue(o2)) {
                                    if (o1.type() == 1) {
                                        return dpl.getKey(((GData1) o1).firstRef).compareTo(dpl.getKey(o2));
                                    } else {
                                        return dpl.getKey(o1.parent.firstRef).compareTo(dpl.getKey(o2));
                                    }
                                } else {
                                    final Integer co1;
                                    final Integer co2;
                                    if (o1.type() == 1) {
                                        co1 = dpl.getKey(((GData1) o1).firstRef);
                                    } else {
                                        co1 = dpl.getKey(o1.parent.firstRef);
                                    }
                                    if (o2.type() == 1) {
                                        co2 = dpl.getKey(((GData1) o2).firstRef);
                                    } else {
                                        co2 = dpl.getKey(o2.parent.firstRef);
                                    }
                                    if (co1 == null && co2 == null) {
                                        return 0;
                                    } else if (co1 == null) {
                                        return -1;
                                    } else if (co2 == null) {
                                        return 1;
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
                        } catch (NullPointerException npe) {
                            NLogger.error(getClass(), "NullPointerException within CLIPBOARD!"); //$NON-NLS-1$
                            NLogger.error(getClass(), "Object 1:" + o1.toString()); //$NON-NLS-1$
                            NLogger.error(getClass(), "Object 2:" + o2.toString()); //$NON-NLS-1$
                            if (o1.ID > o2.ID) { // The id can "never" be equal!
                                return 1;
                            } else {
                                return -1;
                            }
                        }
                    }
                });
            }

            for (Vertex v : singleVertices) {
                CLIPBOARD.add(new GData0("0 !LPE VERTEX " + bigDecimalToString(v.X) + " " + bigDecimalToString(v.Y) + " " + bigDecimalToString(v.Z), View.DUMMY_REFERENCE)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$)
            }

            // 5. Create text data entry in the OS clipboard
            final StringBuilder cbString = new StringBuilder();
            final Matrix rotM;
            {
                final Composite3D c3d = linkedDatFile.getLastSelectedComposite();
                if (c3d != null && !c3d.isDisposed()) {
                    rotM = c3d.getManipulator().getAccurateRotation();
                } else {
                    rotM = null;
                }
            }
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
                if (data.type() == 2 && !((GData2) data).isLine) {
                    GData2 gd2 = (GData2) data;
                    BigDecimal dx = gd2.X1.subtract(gd2.X2);
                    BigDecimal dy = gd2.Y1.subtract(gd2.Y2);
                    BigDecimal dz = gd2.Z1.subtract(gd2.Z2);
                    if (Editor3DWindow.getWindow().getTransformationMode() == ManipulatorScope.LOCAL && rotM != null) {
                        Vector3d tr = rotM.transform(new Vector3d(dx, dy, dz));
                        dx = tr.X;
                        dy = tr.Y;
                        dz = tr.Z;
                    }
                    cbString.append("0 //~  DIST: " + bigDecimalToString(gd2.getLength()) + " DX: " + bigDecimalToString(dx) + " DY: " + bigDecimalToString(dy) + " DZ: " + bigDecimalToString(dz)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    cbString.append(StringHelper.getLineDelimiter());
                } else if (data.type() == 3 && !((GData3) data).isTriangle) {
                    GData3 gd3 = (GData3) data;
                    cbString.append("0 //~  ANGLE: " + gd3.getProtractorAngle() + " LENGTH: " + bigDecimalToString(gd3.getProtractorLength()));  //$NON-NLS-1$//$NON-NLS-2$
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

    public void paste(SelectorSettings sels) {
        if (linkedDatFile.isReadOnly())
            return;
        final boolean insertVertices = sels == null || sels.isVertices();
        final boolean insertLines = sels == null || sels.isLines();
        final boolean insertTriangles = sels == null || sels.isTriangles();
        final boolean insertQuads = sels == null || sels.isQuads();
        final boolean insertCondlines = sels == null || sels.isCondlines();
        final GColour col16 = View.getLDConfigColour(16);
        if (!CLIPBOARD.isEmpty()) {
            clearSelection();
            if (Editor3DWindow.getWindow().isInsertingAtCursorPosition()) {
                for (GData g : CLIPBOARD) {
                    if (g.type() == 0 && (!insertVertices || g.text.startsWith("0 //~"))) { //$NON-NLS-1$
                        continue;
                    }
                    if (!insertLines && g.type() == 2) {
                        continue;
                    }
                    if (!insertTriangles && g.type() == 3) {
                        continue;
                    }
                    if (!insertQuads && g.type() == 4) {
                        continue;
                    }
                    if (!insertCondlines && g.type() == 5) {
                        continue;
                    }
                    Set<String> alreadyParsed = new HashSet<String>();
                    alreadyParsed.add(linkedDatFile.getShortName());
                    if (CLIPBOARD_InvNext.contains(g)) {
                        GDataBFC invNext = new GDataBFC(BFC.INVERTNEXT, View.DUMMY_REFERENCE);
                        linkedDatFile.insertAfterCursor(invNext);
                    }
                    ArrayList<ParsingResult> result = DatParser.parseLine(g.toString(), -1, 0, col16.getR(), col16.getG(), col16.getB(), 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile, false, alreadyParsed, false);
                    GData pasted = result.get(0).getGraphicalData();
                    if (pasted == null)
                        pasted = new GData0(g.toString(), View.DUMMY_REFERENCE);
                    linkedDatFile.insertAfterCursor(pasted);
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
                        if (vis == null) continue;
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
                }
            } else {
                final HashBiMap<Integer, GData> dpl = linkedDatFile.getDrawPerLine_NOCLONE();
                int linecount = dpl.size();
                GData before = linkedDatFile.getDrawChainTail();
                GData tailData = null;
                for (GData g : CLIPBOARD) {
                    if (!insertVertices && g.type() == 0 && g.text.contains("VERTEX")) { //$NON-NLS-1$
                        continue;
                    }
                    if (!insertLines && g.type() == 2) {
                        continue;
                    }
                    if (!insertTriangles && g.type() == 3) {
                        continue;
                    }
                    if (!insertQuads && g.type() == 4) {
                        continue;
                    }
                    if (!insertCondlines && g.type() == 5) {
                        continue;
                    }
                    Set<String> alreadyParsed = new HashSet<String>();
                    alreadyParsed.add(linkedDatFile.getShortName());
                    if (CLIPBOARD_InvNext.contains(g)) {
                        GDataBFC invNext = new GDataBFC(BFC.INVERTNEXT, View.DUMMY_REFERENCE);
                        before.setNext(invNext);
                        before = invNext;
                        linecount++;
                        dpl.put(linecount, invNext);
                    }
                    ArrayList<ParsingResult> result = DatParser.parseLine(g.toString(), -1, 0, col16.getR(), col16.getG(), col16.getB(), 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile, false, alreadyParsed, false);
                    GData pasted = result.get(0).getGraphicalData();
                    if (pasted == null)
                        pasted = new GData0(g.toString(), View.DUMMY_REFERENCE);
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
                        if (vis != null) {
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
            }
            setModified(true, true);
            updateUnsavedStatus();
        }
    }


    public void pasteToJoin(GData g2) {
        if (linkedDatFile.isReadOnly())
            return;
        final GColour col16 = View.getLDConfigColour(16);
        if (!CLIPBOARD.isEmpty()) {
            clearSelection();
            final HashBiMap<Integer, GData> dpl = linkedDatFile.getDrawPerLine_NOCLONE();
            int linecount = dpl.size();
            final GData oldNext = g2.next;
            GData before = g2;
            for (GData g : CLIPBOARD) {
                Set<String> alreadyParsed = new HashSet<String>();
                alreadyParsed.add(linkedDatFile.getShortName());
                if (CLIPBOARD_InvNext.contains(g)) {
                    GDataBFC invNext = new GDataBFC(BFC.INVERTNEXT, View.DUMMY_REFERENCE);
                    before.setNext(invNext);
                    before = invNext;
                    linecount++;
                }
                ArrayList<ParsingResult> result = DatParser.parseLine(g.toString(), -1, 0, col16.getR(), col16.getG(), col16.getB(), 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile, false, alreadyParsed, false);
                GData pasted = result.get(0).getGraphicalData();
                if (pasted == null)
                    pasted = new GData0(g.toString(), View.DUMMY_REFERENCE);
                linecount++;
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
                    if (vis != null) {
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
            }
            before.setNext(oldNext);

            dpl.clear();
            g2 = linkedDatFile.getDrawChainStart();
            for(int line = 1; line <= linecount; line++) {
                g2 = g2.next;
                dpl.put(line, g2);
            }

            linkedDatFile.setDrawChainTail(dpl.getValue(linecount));
        }
        setModified(true, true);
        updateUnsavedStatus();
    }

    public void extendClipboardContent(boolean cutExtension) {

        TreeMap<Integer, ArrayList<GData>> dataToInsert = new TreeMap<Integer, ArrayList<GData>>();
        HashSet<GData> cset = new HashSet<GData>(CLIPBOARD);

        if (CLIPBOARD.size() > 0) {
            final Pattern WHITESPACE = Pattern.compile("\\s+"); //$NON-NLS-1$
            GData g = CLIPBOARD.get(0);
            ArrayList<GData> l = new ArrayList<>();
            while ((g = g.before) != null && g.type() == 0 && (g.toString().trim().isEmpty() || WHITESPACE.matcher(g.toString()).replaceAll(" ").trim().startsWith("0 //"))) { //$NON-NLS-1$ //$NON-NLS-2$
                l.add(g);
            }
            if (!l.isEmpty()) {
                dataToInsert.put(0, l);
            }
        }

        int lineNumber = -1;
        for (Iterator<GData> itc = CLIPBOARD.iterator(); itc.hasNext();) {
            GData g = itc.next();
            ArrayList<GData> l = new ArrayList<>();
            while ((g = g.next) != null && (g.type() < 1 || g.type() > 5) && !CLIPBOARD_InvNext.contains(g) && !cset.contains(g)) {
                l.add(g);
            }
            if (!l.isEmpty()) {
                dataToInsert.put(lineNumber, l);
            }
            lineNumber--;
        }

        for (int key : dataToInsert.keySet()) {
            CLIPBOARD.addAll(-key, dataToInsert.get(key));
        }
        {
            java.util.ListIterator<GData> li = CLIPBOARD.listIterator(CLIPBOARD.size());
            while(li.hasPrevious()) {
                GData g = li.previous();
                if (g.toString().trim().isEmpty() || g.type() == 0) {
                    li.remove();
                } else {
                    break;
                }
            }
        }
        {
            java.util.ListIterator<GData> li = CLIPBOARD.listIterator(0);
            while(li.hasNext()) {
                GData g = li.next();
                if (g.toString().trim().isEmpty()) {
                    li.remove();
                } else {
                    break;
                }
            }
        }
        if (cutExtension) {
            final HashBiMap<Integer, GData> dpl = linkedDatFile.getDrawPerLine_NOCLONE();
            java.util.ListIterator<GData> li = CLIPBOARD.listIterator(CLIPBOARD.size());
            while(li.hasPrevious()) {
                GData g = li.previous();
                if (!cset.contains(g)) {
                    dpl.removeByValue(g);
                    g.getBefore().setNext(g.getNext());
                    remove(g);
                }
            }
        }

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
        int len = cbString.length();
        if (len > StringHelper.getLineDelimiter().length()) cbString.delete(len - StringHelper.getLineDelimiter().length(), len);

        final String cbs = cbString.toString();
        if (!cbs.isEmpty()) {
            Display display = Display.getCurrent();
            Clipboard clipboard = new Clipboard(display);
            clipboard.setContents(new Object[] { cbs }, new Transfer[] { TextTransfer.getInstance() });
            clipboard.dispose();
        }
    }

    public String getClipboardText() {
        final String result;
        Display display = Display.getCurrent();
        Clipboard clipboard = new Clipboard(display);
        result = (String) clipboard.getContents(TextTransfer.getInstance());
        clipboard.dispose();
        return result == null ? "" : result; //$NON-NLS-1$
    }

    public static void copySingleVertexIntoClipboard(final Vertex vertex) {
        CLIPBOARD.clear();
        CLIPBOARD_InvNext.clear();
        final StringBuilder sb = new StringBuilder();
        sb.append("0 !LPE VERTEX "); //$NON-NLS-1$
        sb.append(MathHelper.bigDecimalToString(vertex.X));
        sb.append(" "); //$NON-NLS-1$
        sb.append(MathHelper.bigDecimalToString(vertex.Y));
        sb.append(" "); //$NON-NLS-1$
        sb.append(MathHelper.bigDecimalToString(vertex.Z));
        CLIPBOARD.add(new GData0(sb.toString(), View.DUMMY_REFERENCE));
    }

    public static Vertex getSingleVertexFromClipboard() {
        Vertex result = null;
        if (VertexManager.getClipboard().size() == 1) {
            GData vertex = VertexManager.getClipboard().get(0);
            if (vertex.type() == 0) {
                String line = vertex.toString();
                line = line.replaceAll("\\s+", " ").trim(); //$NON-NLS-1$ //$NON-NLS-2$
                String[] data_segments = line.split("\\s+"); //$NON-NLS-1$
                if (line.startsWith("0 !LPE VERTEX ")) { //$NON-NLS-1$
                    Vector3d start = new Vector3d();
                    boolean numberError = false;
                    if (data_segments.length == 6) {
                        try {
                            start.setX(new BigDecimal(data_segments[3], Threshold.mc));
                        } catch (NumberFormatException nfe) {
                            numberError = true;
                        }

                        try {
                            start.setY(new BigDecimal(data_segments[4], Threshold.mc));
                        } catch (NumberFormatException nfe) {
                            numberError = true;
                        }

                        try {
                            start.setZ(new BigDecimal(data_segments[5], Threshold.mc));
                        } catch (NumberFormatException nfe) {
                            numberError = true;
                        }
                    } else {
                        numberError = true;
                    }

                    if (!numberError) {
                        result = new Vertex(start);
                    }
                }
            }
        }

        return result;
    }
}
