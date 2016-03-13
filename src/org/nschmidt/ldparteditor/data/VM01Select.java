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
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.lwjgl.util.vector.Matrix4f;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.composite3d.SelectorSettings;
import org.nschmidt.ldparteditor.helpers.math.HashBiMap;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.text.DatParser;

class VM01Select extends VM00Snapshot {

    protected VM01Select(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public GDataPNG getSelectedBgPicture() {
        return selectedBgPicture;
    }

    public void setSelectedBgPicture(GDataPNG pic) {
        selectedBgPicture = pic;
    }

    public int getSelectedBgPictureIndex() {
        return selectedBgPictureIndex;
    }

    public void setSelectedBgPictureIndex(int selectedBgPictureIndex) {
        this.selectedBgPictureIndex = selectedBgPictureIndex;
    }

    public synchronized void clearSelection() {
        final Editor3DWindow win = Editor3DWindow.getWindow();
        selectedItemIndex = -1;
        win.disableSelectionTab();
        selectedData.clear();
        selectedVertices.clear();
        selectedSubfiles.clear();
        selectedLines.clear();
        selectedTriangles.clear();
        selectedQuads.clear();
        selectedCondlines.clear();
        lastSelectedVertex = null;
    }

    public synchronized void clearSelection2() {
        selectedItemIndex = -1;
        selectedData.clear();
        selectedVertices.clear();
        selectedSubfiles.clear();
        selectedLines.clear();
        selectedTriangles.clear();
        selectedQuads.clear();
        selectedCondlines.clear();
        lastSelectedVertex = null;
    }

    protected void clearSelection3() {
        selectedItemIndex = -1;
        selectedData.clear();
        selectedVertices.clear();
        selectedSubfiles.clear();
        selectedLines.clear();
        selectedTriangles.clear();
        selectedQuads.clear();
        selectedCondlines.clear();
        lastSelectedVertex = null;
    }

    public synchronized void selectAll(SelectorSettings ss, boolean includeHidden) {
        clearSelection();
        if (ss == null) ss = new SelectorSettings();
        if (includeHidden) {

            if (ss.isVertices()) selectedVertices.addAll(vertexLinkedToPositionInFile.keySet());

            if (ss.isLines()) selectedLines.addAll(lines.keySet());
            if (ss.isTriangles()) selectedTriangles.addAll(triangles.keySet());
            if (ss.isQuads()) selectedQuads.addAll(quads.keySet());
            if (ss.isCondlines()) selectedCondlines.addAll(condlines.keySet());

            if (ss.isVertices() && ss.isLines() && ss.isTriangles() && ss.isQuads() && ss.isCondlines()) {
                selectedSubfiles.addAll(vertexCountInSubfile.keySet());
            }

        } else {

            if (ss.isVertices()) {
                for (Vertex v : vertexLinkedToPositionInFile.keySet()) {
                    if (!hiddenVertices.contains(v)) selectedVertices.add(v);
                }
            }
            if (ss.isVertices() && ss.isLines() && ss.isTriangles() && ss.isQuads() && ss.isCondlines()) {
                for (GData1 g : vertexCountInSubfile.keySet()) {
                    if (!hiddenData.contains(g)) selectedSubfiles.add(g);
                }
            }
            if (ss.isLines()) {
                for (GData2 g : lines.keySet()) {
                    if (!hiddenData.contains(g)) selectedLines.add(g);
                }
            }
            if (ss.isTriangles()) {
                for (GData3 g : triangles.keySet()) {
                    if (!hiddenData.contains(g)) selectedTriangles.add(g);
                }
            }
            if (ss.isQuads()) {
                for (GData4 g : quads.keySet()) {
                    if (!hiddenData.contains(g)) selectedQuads.add(g);
                }
            }
            if (ss.isCondlines()) {
                for (GData5 g : condlines.keySet()) {
                    if (!hiddenData.contains(g)) selectedCondlines.add(g);
                }
            }
        }

        selectedData.addAll(selectedLines);
        selectedData.addAll(selectedTriangles);
        selectedData.addAll(selectedQuads);
        selectedData.addAll(selectedCondlines);
        selectedData.addAll(selectedSubfiles);
    }

    public void selectAllWithSameColours(SelectorSettings ss, boolean includeHidden) {

        final Set<GColour> allColours = new HashSet<GColour>();

        final Set<GData1> effSelectedSubfiles = new HashSet<GData1>();
        final Set<GData2> effSelectedLines = new HashSet<GData2>();
        final Set<GData3> effSelectedTriangles = new HashSet<GData3>();
        final Set<GData4> effSelectedQuads = new HashSet<GData4>();
        final Set<GData5> effSelectedCondlines = new HashSet<GData5>();

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

        clearSelection();

        if (ss.isVertices() && ss.isLines() && ss.isTriangles() && ss.isQuads() && ss.isCondlines()) {
            for (GData1 g : vertexCountInSubfile.keySet()) {
                if (!includeHidden && hiddenData.contains(g)) continue;
                if (allColours.contains(new GColour(g.colourNumber, g.r, g.g, g.b, g.a))) {
                    effSelectedSubfiles.add(g);
                }
            }

            for (GData1 subf : effSelectedSubfiles) {
                selectedSubfiles.add(subf);
                Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
                for (VertexInfo vertexInfo : vis) {
                    selectedVertices.add(vertexInfo.getVertex());
                    GData g = vertexInfo.getLinkedData();
                    if (!includeHidden && hiddenData.contains(g)) continue;
                    switch (g.type()) {
                    case 2:
                        effSelectedLines.add((GData2) g);
                        break;
                    case 3:
                        effSelectedTriangles.add((GData3) g);
                        break;
                    case 4:
                        effSelectedQuads.add((GData4) g);
                        break;
                    case 5:
                        effSelectedCondlines.add((GData5) g);
                        break;
                    default:
                        break;
                    }
                }
            }
        }

        if (ss.isLines()) {
            for (GData2 g : lines.keySet()) {
                if (!includeHidden && hiddenData.contains(g)) continue;
                if (allColours.contains(new GColour(g.colourNumber, g.r, g.g, g.b, g.a))) {
                    effSelectedLines.add(g);
                }
            }
        }
        if (ss.isTriangles()) {
            for (GData3 g : triangles.keySet()) {
                if (!includeHidden && hiddenData.contains(g)) continue;
                if (allColours.contains(new GColour(g.colourNumber, g.r, g.g, g.b, g.a))) {
                    effSelectedTriangles.add(g);
                }
            }
        }
        if (ss.isQuads()) {
            for (GData4 g : quads.keySet()) {
                if (!includeHidden && hiddenData.contains(g)) continue;
                if (allColours.contains(new GColour(g.colourNumber, g.r, g.g, g.b, g.a))) {
                    effSelectedQuads.add(g);
                }
            }
        }
        if (ss.isCondlines()) {
            for (GData5 g : condlines.keySet()) {
                if (!includeHidden && hiddenData.contains(g)) continue;
                if (allColours.contains(new GColour(g.colourNumber, g.r, g.g, g.b, g.a))) {
                    effSelectedCondlines.add(g);
                }
            }
        }

        selectedLines.addAll(effSelectedLines);
        selectedTriangles.addAll(effSelectedTriangles);
        selectedQuads.addAll(effSelectedQuads);
        selectedCondlines.addAll(effSelectedCondlines);
        selectedSubfiles.addAll(effSelectedSubfiles);

        selectedData.addAll(selectedLines);
        selectedData.addAll(selectedTriangles);
        selectedData.addAll(selectedQuads);
        selectedData.addAll(selectedCondlines);
        selectedData.addAll(selectedSubfiles);
    }

    public void backupSelection() {
        backupSelectedCondlines.addAll(selectedCondlines);
        backupSelectedData.addAll(selectedData);
        backupSelectedLines.addAll(selectedLines);
        backupSelectedQuads.addAll(selectedQuads);
        backupSelectedSubfiles.addAll(selectedSubfiles);
        backupSelectedTriangles.addAll(selectedTriangles);
        backupSelectedVertices.addAll(selectedVertices);
    }

    protected void backupSelectionClear() {
        backupSelectedCondlines.clear();
        backupSelectedData.clear();
        backupSelectedLines.clear();
        backupSelectedQuads.clear();
        backupSelectedSubfiles.clear();
        backupSelectedTriangles.clear();
        backupSelectedVertices.clear();
    }

    public void restoreSelection() {
        clearSelection3();
        selectedCondlines.addAll(backupSelectedCondlines);
        selectedData.addAll(backupSelectedData);
        selectedLines.addAll(backupSelectedLines);
        selectedQuads.addAll(backupSelectedQuads);
        selectedSubfiles.addAll(backupSelectedSubfiles);
        selectedTriangles.addAll(backupSelectedTriangles);
        selectedVertices.addAll(backupSelectedVertices);
        backupSelectionClear();
    }

    public int getSelectedItemIndex() {
        return selectedItemIndex;
    }

    public void setSelectedItemIndex(int selectedItemIndex) {
        this.selectedItemIndex = selectedItemIndex;
    }

    public GData getSelectedLine() {
        return selectedLine;
    }

    public void setSelectedLine(GData selectedLine) {
        this.selectedLine = selectedLine;
    }

    public GData updateSelectedLine(
            BigDecimal x1, BigDecimal y1, BigDecimal z1,
            BigDecimal x2, BigDecimal y2, BigDecimal z2,
            BigDecimal x3, BigDecimal y3, BigDecimal z3,
            BigDecimal x4, BigDecimal y4, BigDecimal z4, boolean moveAdjacentData) {

        if (selectedItemIndex != -1 && exist(selectedLine)) {


            if (selectedLine.type() == 1) {
                // [ERROR] Check singularity
                Matrix4f tMatrix = new Matrix4f();
                // Offset
                tMatrix.m30 = x1.floatValue() * 1000f;
                tMatrix.m31 = y1.floatValue() * 1000f;
                tMatrix.m32 = z1.floatValue() * 1000f;
                // First row
                tMatrix.m00 = x2.floatValue();
                tMatrix.m10 = y2.floatValue();
                tMatrix.m20 = z2.floatValue();
                // Second row
                tMatrix.m01 = x3.floatValue();
                tMatrix.m11 = y3.floatValue();
                tMatrix.m21 = z3.floatValue();
                // Third row
                tMatrix.m02 = x4.floatValue();
                tMatrix.m12 = y4.floatValue();
                tMatrix.m22 = z4.floatValue();
                tMatrix.m33 = 1f;
                float det = tMatrix.determinant();
                if (Math.abs(det) < Threshold.singularity_determinant) {
                    return selectedLine;
                }
                Matrix TMatrix = new Matrix(x2, x3, x4, BigDecimal.ZERO, y2, y3, y4, BigDecimal.ZERO, z2, z3, z4, BigDecimal.ZERO, x1, y1, z1, BigDecimal.ONE);

                final GData before = selectedLine.getBefore();
                final GData next = selectedLine.getNext();
                final HashBiMap<Integer, GData> drawPerLine = linkedDatFile.getDrawPerLine_NOCLONE();
                GData newData = null;
                GData1 g1 = (GData1) selectedLine;
                selectedSubfiles.remove(g1);
                selectedData.remove(g1);

                StringBuilder transformationBuilder = new StringBuilder();
                transformationBuilder.append("1 "); //$NON-NLS-1$
                if (g1.colourNumber == -1) {
                    transformationBuilder.append("0x2"); //$NON-NLS-1$
                    transformationBuilder.append(MathHelper.toHex((int) (255f * g1.r)).toUpperCase());
                    transformationBuilder.append(MathHelper.toHex((int) (255f * g1.g)).toUpperCase());
                    transformationBuilder.append(MathHelper.toHex((int) (255f * g1.b)).toUpperCase());
                } else {
                    transformationBuilder.append(g1.colourNumber);
                }
                transformationBuilder.append(TMatrix.toLDrawString());
                transformationBuilder.append(g1.shortName);

                String transformedString = transformationBuilder.toString();

                if (16 == g1.colourNumber) {
                    newData = DatParser
                            .parseLine(transformedString, drawPerLine.getKey(g1).intValue(), 0, 0.5f, 0.5f, 0.5f, 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile, false,
                                    new HashSet<String>(), false).get(0).getGraphicalData();
                } else {
                    newData = DatParser
                            .parseLine(transformedString, drawPerLine.getKey(g1).intValue(), 0, g1.r, g1.g, g1.b, g1.a, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile, false,
                                    new HashSet<String>(), false).get(0).getGraphicalData();
                }
                if (g1.equals(linkedDatFile.getDrawChainTail()))
                    linkedDatFile.setDrawChainTail(newData);

                before.setNext(newData);
                newData.setNext(next);
                drawPerLine.put(drawPerLine.getKey(selectedLine), newData);
                if (remove(selectedLine)) {
                    linkedDatFile.setDrawChainTail(newData);
                }
                selectSubfile(newData);
                selectedLine = newData;
                setModified(true, true);
                updateUnsavedStatus();
                return selectedLine;
            }


            if (moveAdjacentData) {

                final Vertex v1 = new Vertex(x1, y1, z1);
                final Vertex v2 = new Vertex(x2, y2, z2);
                final Vertex v3 = new Vertex(x3, y3, z3);
                final Vertex v4 = new Vertex(x4, y4, z4);

                Vertex oldVertex = null;
                Vertex newVertex = null;

                TreeSet<Vertex> ov = new TreeSet<Vertex>();
                TreeSet<Vertex> nv = new TreeSet<Vertex>();

                switch (selectedLine.type()) {
                case 5:
                case 4:
                    nv.add(v4);
                case 3:
                    nv.add(v3);
                case 2:
                    nv.add(v1);
                    nv.add(v2);
                    break;
                default:
                    return null;
                }

                Set<VertexInfo> vi = lineLinkedToVertices.get(selectedLine);
                if (vi != null) {
                    for (VertexInfo v : vi) {
                        ov.add(v.vertex);
                    }
                } else {
                    return null;
                }

                TreeSet<Vertex> nv2 = new TreeSet<Vertex>(nv);
                nv2.removeAll(ov);
                ov.removeAll(nv);

                if (nv2.size() != 1 || ov.size() != 1) {
                    return selectedLine;
                }

                oldVertex = ov.iterator().next();
                newVertex = nv2.iterator().next();

                GData nl = changeVertexDirectFast(oldVertex, newVertex, moveAdjacentData, selectedLine);

                if (nl != null) {
                    selectedLine = nl;
                }

                selectedData.clear();
                selectedData.addAll(selectedLines);
                selectedData.addAll(selectedTriangles);
                selectedData.addAll(selectedQuads);
                selectedData.addAll(selectedCondlines);
                selectedData.addAll(selectedSubfiles);
            } else {
                final GData before = selectedLine.getBefore();
                final GData next = selectedLine.getNext();
                final HashBiMap<Integer, GData> drawPerLine = linkedDatFile.getDrawPerLine_NOCLONE();
                GData newData = null;
                switch (selectedLine.type()) {
                case 2:
                    GData2 g2 = (GData2) selectedLine;
                    selectedLines.remove(g2);
                    selectedData.remove(g2);
                    newData = new GData2(g2.colourNumber, g2.r, g2.g, g2.b, g2.a, new Vertex(x1, y1, z1), new Vertex(x2, y2, z2), g2.parent, linkedDatFile);
                    selectedLines.add((GData2) newData);
                    selectedData.add(newData);
                    break;
                case 3:
                    GData3 g3 = (GData3) selectedLine;
                    selectedTriangles.remove(g3);
                    selectedData.remove(g3);
                    newData = new GData3(g3.colourNumber, g3.r, g3.g, g3.b, g3.a, new Vertex(x1, y1, z1), new Vertex(x2, y2, z2), new Vertex(x3, y3, z3), g3.parent, linkedDatFile);
                    selectedTriangles.add((GData3) newData);
                    selectedData.add(newData);
                    break;
                case 4:
                    GData4 g4 = (GData4) selectedLine;
                    selectedQuads.remove(g4);
                    selectedData.remove(g4);
                    newData = new GData4(g4.colourNumber, g4.r, g4.g, g4.b, g4.a, new Vertex(x1, y1, z1), new Vertex(x2, y2, z2), new Vertex(x3, y3, z3), new Vertex(x4, y4, z4), g4.parent, linkedDatFile);
                    selectedQuads.add((GData4) newData);
                    selectedData.add(newData);
                    break;
                case 5:
                    GData5 g5 = (GData5) selectedLine;
                    selectedCondlines.remove(g5);
                    selectedData.remove(g5);
                    newData = new GData5(g5.colourNumber, g5.r, g5.g, g5.b, g5.a, new Vertex(x1, y1, z1), new Vertex(x2, y2, z2), new Vertex(x3, y3, z3), new Vertex(x4, y4, z4), g5.parent, linkedDatFile);
                    selectedCondlines.add((GData5) newData);
                    selectedData.add(newData);
                    break;
                default:
                    return null;
                }
                before.setNext(newData);
                newData.setNext(next);
                drawPerLine.put(drawPerLine.getKey(selectedLine), newData);
                if (remove(selectedLine)) {
                    linkedDatFile.setDrawChainTail(newData);
                }
                selectedLine = newData;
            }
            setModified(true, true);
            updateUnsavedStatus();
            return selectedLine;
        }
        return null;

    }

    protected void selectSubfile(GData data) {
        if (data.type() == 1) {
            GData1 g1 = (GData1) data;
            if (View.DUMMY_REFERENCE.equals(g1.parent)) {
                selectedSubfiles.add(g1);
                selectedData.add(g1);
                Set<VertexInfo> vis = lineLinkedToVertices.get(g1);
                for (VertexInfo vertexInfo : vis) {
                    GData g = vertexInfo.getLinkedData();
                    switch (g.type()) {
                    case 2:
                        selectedData.add(g);
                        selectedLines.add((GData2) g);
                        break;
                    case 3:
                        selectedData.add(g);
                        selectedTriangles.add((GData3) g);
                        break;
                    case 4:
                        selectedData.add(g);
                        selectedQuads.add((GData4) g);
                        break;
                    case 5:
                        selectedData.add(g);
                        selectedCondlines.add((GData5) g);
                        break;
                    default:
                        break;
                    }
                }
            }
        }
    }

    public void selectInverse(SelectorSettings sels) {

        final Set<Vertex> lastSelectedVertices = new TreeSet<Vertex>();
        final Set<GData1> lastSelectedSubfiles = new HashSet<GData1>();
        final Set<GData2> lastSelectedLines = new HashSet<GData2>();
        final Set<GData3> lastSelectedTriangles = new HashSet<GData3>();
        final Set<GData4> lastSelectedQuads = new HashSet<GData4>();
        final Set<GData5> lastSelectedCondlines = new HashSet<GData5>();

        lastSelectedVertices.addAll(selectedVertices);
        lastSelectedSubfiles.addAll(selectedSubfiles);
        lastSelectedLines.addAll(selectedLines);
        lastSelectedTriangles.addAll(selectedTriangles);
        lastSelectedQuads.addAll(selectedQuads);
        lastSelectedCondlines.addAll(selectedCondlines);

        clearSelection();

        if (sels.isVertices()) {
            for (Vertex v : vertexLinkedToPositionInFile.keySet()) {
                if (!hiddenVertices.contains(v) && !lastSelectedVertices.contains(v)) selectedVertices.add(v);
            }
        }
        if (sels.isVertices() && sels.isLines() && sels.isTriangles() && sels.isQuads() && sels.isCondlines()) {
            for (GData1 g : vertexCountInSubfile.keySet()) {
                if (!hiddenData.contains(g) && !lastSelectedSubfiles.contains(g)) selectedSubfiles.add(g);
            }
        }
        if (sels.isLines()) {
            for (GData2 g : lines.keySet()) {
                if (!hiddenData.contains(g) && !lastSelectedLines.contains(g)) selectedLines.add(g);
            }
        }
        if (sels.isTriangles()) {
            for (GData3 g : triangles.keySet()) {
                if (!hiddenData.contains(g) && !lastSelectedTriangles.contains(g)) selectedTriangles.add(g);
            }
        }
        if (sels.isQuads()) {
            for (GData4 g : quads.keySet()) {
                if (!hiddenData.contains(g) && !lastSelectedQuads.contains(g)) selectedQuads.add(g);
            }
        }
        if (sels.isCondlines()) {
            for (GData5 g : condlines.keySet()) {
                if (!hiddenData.contains(g) && !lastSelectedCondlines.contains(g)) selectedCondlines.add(g);
            }
        }

        selectedData.addAll(selectedLines);
        selectedData.addAll(selectedTriangles);
        selectedData.addAll(selectedQuads);
        selectedData.addAll(selectedCondlines);
        selectedData.addAll(selectedSubfiles);

    }

    public void selectIsolatedVertices() {
        clearSelection();
        for (Vertex v : vertexLinkedToPositionInFile.keySet()) {
            int vd = 0;
            for (VertexManifestation vm : vertexLinkedToPositionInFile.get(v)) {
                if (vm.getGdata().type() == 0) {
                    vd++;
                } else {
                    break;
                }
            }
            if (vd == vertexLinkedToPositionInFile.get(v).size()) {
                selectedVertices.add(v);
            }
        }
    }

    public synchronized Set<Vertex> getSelectedVertices() {
        return selectedVertices;
    }

    public synchronized Set<Vertex> getHiddenVertices() {
        return hiddenVertices;
    }

    public synchronized Set<GData> getSelectedData() {
        return selectedData;
    }

    public synchronized Set<GData1> getSelectedSubfiles() {
        return selectedSubfiles;
    }

    public synchronized Set<GData2> getSelectedLines() {
        return selectedLines;
    }

    public synchronized Set<GData3> getSelectedTriangles() {
        return selectedTriangles;
    }

    public synchronized Set<GData4> getSelectedQuads() {
        return selectedQuads;
    }

    public synchronized Set<GData5> getSelectedCondlines() {
        return selectedCondlines;
    }

    public synchronized void addTextLineToSelection(int line) {
        final GData gd;
        if ((gd = linkedDatFile.getDrawPerLine_NOCLONE().getValue(line)) != null) {
            if (gd.type() > 0 && gd.type() < 6) {
                selectedData.add(gd);
            }
            switch (gd.type()) {
            case 1:
                selectSubfile(gd);
                break;
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
                break;
            }
        }
    }

    public void reSelectSubFiles() {
        // Restore selected subfiles
        for (GData1 subf : selectedSubfiles) {
            Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
            for (VertexInfo vertexInfo : vis) {
                selectedVertices.add(vertexInfo.getVertex());
                GData g = vertexInfo.getLinkedData();
                switch (g.type()) {
                case 2:
                    selectedLines.add((GData2) g);
                    break;
                case 3:
                    selectedTriangles.add((GData3) g);
                    break;
                case 4:
                    selectedQuads.add((GData4) g);
                    break;
                case 5:
                    selectedCondlines.add((GData5) g);
                    break;
                default:
                    break;
                }
            }
        }
    }

    public GColour getRandomSelectedColour(GColour lastUsedColour) {
        if (!selectedSubfiles.isEmpty()) {
            GData1 g1 = selectedSubfiles.iterator().next();
            lastUsedColour = new GColour(g1.colourNumber, g1.r, g1.g, g1.b, g1.a);
            if (Math.random() > .5) return lastUsedColour;
        }
        if (!selectedLines.isEmpty()) {
            GData2 g2 = selectedLines.iterator().next();
            lastUsedColour = new GColour(g2.colourNumber, g2.r, g2.g, g2.b, g2.a);
            if (Math.random() > .5) return lastUsedColour;
        }
        if (!selectedTriangles.isEmpty()) {
            GData3 g3 = selectedTriangles.iterator().next();
            lastUsedColour = new GColour(g3.colourNumber, g3.r, g3.g, g3.b, g3.a);
            if (Math.random() > .5) return lastUsedColour;
        }
        if (!selectedQuads.isEmpty()) {
            GData4 g4 = selectedQuads.iterator().next();
            lastUsedColour = new GColour(g4.colourNumber, g4.r, g4.g, g4.b, g4.a);
            if (Math.random() > .5) return lastUsedColour;
        }
        if (!selectedCondlines.isEmpty()) {
            GData5 g5 = selectedCondlines.iterator().next();
            lastUsedColour = new GColour(g5.colourNumber, g5.r, g5.g, g5.b, g5.a);
            if (Math.random() > .5) return lastUsedColour;
        }
        return lastUsedColour;
    }

    public void selectTriangles(Set<GData> finalTriangleSet) {
        selectedData.addAll(finalTriangleSet);
        for (GData gData : finalTriangleSet) {
            selectedTriangles.add((GData3) gData);
        }
    }

    public void restoreTriangles(Set<GData> finalTriangleSet) {
        finalTriangleSet.clear();
        for (GData3 gData : selectedTriangles) {
            finalTriangleSet.add(gData);
        }
    }

    public boolean isNotInSubfileAndLinetype1to5(GData g) {
        if (!exist(g) || !lineLinkedToVertices.containsKey(g)) {
            return false;
        }
        switch (g.type()) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
            return true;
        default:
            return false;
        }
    }
}
