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
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.nschmidt.ldparteditor.enumtype.Colour;
import org.nschmidt.ldparteditor.enumtype.LDConfig;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.helper.math.HashBiMap;
import org.nschmidt.ldparteditor.helper.math.MathHelper;
import org.nschmidt.ldparteditor.text.DatParser;

class VM19ColourChanger extends VM18LineConverter {

    private final Random rnd = new Random((GData.getLastID() + 1) * 19364792647L);

    protected VM19ColourChanger(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public final synchronized void colourChangeSelection(int index, float r, float g, float b, float a, boolean syncWithEditors) {
        if (linkedDatFile.isReadOnly())
            return;

        backupHideShowState();

        final Set<GData2> effSelectedLines = new HashSet<>();
        final Set<GData3> effSelectedTriangles = new HashSet<>();
        final Set<GData4> effSelectedQuads = new HashSet<>();
        final Set<GData5> effSelectedCondlines = new HashSet<>();

        final Set<GData2> subSelectedLines = new HashSet<>();
        final Set<GData3> subSelectedTriangles = new HashSet<>();
        final Set<GData4> subSelectedQuads = new HashSet<>();
        final Set<GData5> subSelectedCondlines = new HashSet<>();

        final GColour col16 = LDConfig.getColour16();

        selectedData.clear();
        selectedVertices.clear();

        // 0. Deselect selected subfile data (for whole selected subfiles)
        for (GData1 subf : selectedSubfiles) {
            Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
            if (vis == null) continue;
            for (VertexInfo vertexInfo : vis) {
                GData gt = vertexInfo.getLinkedData();
                switch (gt.type()) {
                case 2:
                    selectedLines.remove(gt);
                    break;
                case 3:
                    selectedTriangles.remove(gt);
                    break;
                case 4:
                    selectedQuads.remove(gt);
                    break;
                case 5:
                    selectedCondlines.remove(gt);
                    break;
                default:
                    break;
                }
            }
        }

        // 2. Object Based Selection

        for (GData2 line : selectedLines) {
            if (line.parent.equals(View.DUMMY_REFERENCE))
                effSelectedLines.add(line);
            else
                subSelectedLines.add(line);
        }
        for (GData3 triangle : selectedTriangles) {
            if (triangle.parent.equals(View.DUMMY_REFERENCE))
                effSelectedTriangles.add(triangle);
            else
                subSelectedTriangles.add(triangle);
        }
        for (GData4 quad : selectedQuads) {
            if (quad.parent.equals(View.DUMMY_REFERENCE))
                effSelectedQuads.add(quad);
            else
                subSelectedQuads.add(quad);
        }
        for (GData5 condline : selectedCondlines) {
            if (condline.parent.equals(View.DUMMY_REFERENCE))
                effSelectedCondlines.add(condline);
            else
                subSelectedCondlines.add(condline);
        }

        // 3. Transformation of the selected data (no whole subfiles!!)
        // + selectedData update!

        List<GData> modData = new ArrayList<>();
        for (GData2 gd : effSelectedLines)
            modData.add(changeColour(index, r, g, b, a, gd));
        for (GData3 gd : effSelectedTriangles)
            modData.add(changeColour(index, r, g, b, a, gd));
        for (GData4 gd : effSelectedQuads)
            modData.add(changeColour(index, r, g, b, a, gd));
        for (GData5 gd : effSelectedCondlines)
            modData.add(changeColour(index, r, g, b, a, gd));

        if (GDataCSG.hasSelectionCSG(linkedDatFile)) {
            List<GData> newSelection = new ArrayList<>();
            for (GDataCSG gd : GDataCSG.getSelection(linkedDatFile))
                newSelection.add(changeColour(index, r, g, b, a, gd));
            modData.addAll(newSelection);
            GDataCSG.getSelection(linkedDatFile).clear();
            for (GData gd : newSelection) {
                GDataCSG.getSelection(linkedDatFile).add((GDataCSG) gd);
            }
            setModifiedNoSync();
        }

        selectedLines.clear();
        selectedTriangles.clear();
        selectedQuads.clear();
        selectedCondlines.clear();

        for (GData gData : modData) {
            if (gData == null) continue;
            switch (gData.type()) {
            case 2:
                selectedLines.add((GData2) gData);
                break;
            case 3:
                selectedTriangles.add((GData3) gData);
                break;
            case 4:
                selectedQuads.add((GData4) gData);
                break;
            case 5:
                selectedCondlines.add((GData5) gData);
                break;
            default:
                break;
            }
        }

        if (!selectedLines.isEmpty())
            setModifiedNoSync();
        if (!selectedTriangles.isEmpty())
            setModifiedNoSync();
        if (!selectedQuads.isEmpty())
            setModifiedNoSync();
        if (!selectedCondlines.isEmpty())
            setModifiedNoSync();

        // 4. Subfile Based Change & Selection
        if (!selectedSubfiles.isEmpty()) {

            StringBuilder colourBuilder = new StringBuilder();
            if (index == -1) {
                colourBuilder.append("0x2"); //$NON-NLS-1$
                colourBuilder.append(MathHelper.toHex((int) (255f * r)).toUpperCase());
                colourBuilder.append(MathHelper.toHex((int) (255f * g)).toUpperCase());
                colourBuilder.append(MathHelper.toHex((int) (255f * b)).toUpperCase());
            } else {
                colourBuilder.append(index);
            }
            String col = colourBuilder.toString();
            final boolean isRandomColour = a == 0f;
            HashBiMap<Integer, GData> drawPerLine = linkedDatFile.getDrawPerLineNoClone();
            Set<GData1> newSubfiles = new HashSet<>();
            for (GData1 subf : selectedSubfiles) {
                if (!drawPerLine.containsValue(subf)) {
                    continue;
                }
                if (isRandomColour) {
                    colourBuilder.setLength(0);
                    colourBuilder.append("0x2"); //$NON-NLS-1$
                    colourBuilder.append(MathHelper.toHex(rnd.nextInt(255)).toUpperCase());
                    colourBuilder.append(MathHelper.toHex(rnd.nextInt(255)).toUpperCase());
                    colourBuilder.append(MathHelper.toHex(rnd.nextInt(255)).toUpperCase());
                    col = colourBuilder.toString();
                }
                String colouredString = subf.getColouredString(col);
                GData oldNext = subf.getNext();
                GData oldBefore = subf.getBefore();
                remove(subf);
                GData colouredSubfile;
                if ("16".equals(col)) { //$NON-NLS-1$
                    colouredSubfile = DatParser
                            .parseLine(colouredString, drawPerLine.getKey(subf).intValue(), 0, col16.getR(), col16.getG(), col16.getB(), 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile, false,
                                    new HashSet<>()).get(0).getGraphicalData();
                } else {
                    colouredSubfile = DatParser
                            .parseLine(colouredString, drawPerLine.getKey(subf).intValue(), 0, subf.r, subf.g, subf.b, subf.a, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile, false,
                                    new HashSet<>()).get(0).getGraphicalData();
                }
                if (colouredSubfile != null) {
                    if (subf.equals(linkedDatFile.getDrawChainTail()))
                        linkedDatFile.setDrawChainTail(colouredSubfile);

                    oldBefore.setNext(colouredSubfile);
                    colouredSubfile.setNext(oldNext);
                    Integer oldNumber = drawPerLine.getKey(subf);
                    if (oldNumber != null)
                        drawPerLine.put(oldNumber, colouredSubfile);
                    newSubfiles.add((GData1) colouredSubfile);
                }
            }
            selectedSubfiles.clear();
            selectedSubfiles.addAll(newSubfiles);

            for (GData1 subf : selectedSubfiles) {
                Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
                if (vis == null) continue;
                for (VertexInfo vertexInfo : vis) {
                    selectedVertices.add(vertexInfo.getVertex());
                    GData gt = vertexInfo.getLinkedData();
                    switch (gt.type()) {
                    case 2:
                        selectedLines.add((GData2) gt);
                        break;
                    case 3:
                        selectedTriangles.add((GData3) gt);
                        break;
                    case 4:
                        selectedQuads.add((GData4) gt);
                        break;
                    case 5:
                        selectedCondlines.add((GData5) gt);
                        break;
                    default:
                        break;
                    }
                }
            }
            setModifiedNoSync();
        }

        if (isModified()) {
            selectedLines.addAll(subSelectedLines);
            selectedTriangles.addAll(subSelectedTriangles);
            selectedQuads.addAll(subSelectedQuads);
            selectedCondlines.addAll(subSelectedCondlines);
            selectedData.addAll(selectedLines);
            selectedData.addAll(selectedTriangles);
            selectedData.addAll(selectedQuads);
            selectedData.addAll(selectedCondlines);
            selectedData.addAll(selectedSubfiles);
            restoreHideShowState();
            if (syncWithEditors) syncWithTextEditors(true);
            updateUnsavedStatus();
        } else {
            restoreHideShowState();
        }
    }

    private final synchronized GData changeColour(int index, float r, float g, float b, float a, GData dataToModify) {
        Set<GData> newSet = new HashSet<>();
        newSet.add(dataToModify);
        changeColour(index, r, g, b, a, newSet);
        if (newSet.iterator().hasNext()) {
            return newSet.iterator().next();
        } else {
            return null;
        }
    }

    private final synchronized void changeColour(int index, float r, float g, float b, float a, Set<GData> dataToModify) {
        Set<GData> newData = new HashSet<>();
        GColour colour = LDConfig.getColour(index);
        final float cr = colour.getR();
        final float cg = colour.getG();
        final float cb = colour.getB();
        final float ca = colour.getA();
        final boolean isRandomColour = a == 0f;
        if (isRandomColour) {
            index = -1;
            a = 1f;
        }
        for (GData gData : dataToModify) {
            if (gData == null || gData.type() != 8 && !lineLinkedToVertices.containsKey(gData)) {
                continue;
            }
            if (index > -1) {
                switch (gData.type()) {
                case 2, 5:
                    if (index == 24) {
                        r = Colour.lineColourR;
                        g = Colour.lineColourG;
                        b = Colour.lineColourB;
                        a = 1f;
                    } else {
                        r = cr;
                        g = cg;
                        b = cb;
                        a = ca;
                    }
                    break;
                default:
                    r = cr;
                    g = cg;
                    b = cb;
                    a = ca;
                }
            } else if (isRandomColour) {
                r = rnd.nextFloat();
                g = rnd.nextFloat();
                b = rnd.nextFloat();
            }
            GData newGData = null;
            switch (gData.type()) {
            case 2:
                GData2 gd2 = (GData2) gData;
                GData2 newGdata2 = new GData2(gd2.parent, index, r, g, b, a, gd2.x1p, gd2.y1p, gd2.z1p, gd2.x2p, gd2.y2p, gd2.z2p, gd2.x1, gd2.y1, gd2.z1, gd2.x2, gd2.y2, gd2.z2, linkedDatFile, gd2.isLine);
                newData.add(newGdata2);
                newGData = newGdata2;
                break;
            case 3:
                GData3 gd3 = (GData3) gData;
                GData3 newGdata3 = new GData3(index, r, g, b, a, gd3.x1p, gd3.y1p, gd3.z1p, gd3.x2p, gd3.y2p, gd3.z2p, gd3.x3p, gd3.y3p, gd3.z3p, gd3.x1, gd3.y1, gd3.z1, gd3.x2, gd3.y2, gd3.z2, gd3.x3,
                        gd3.y3, gd3.z3, gd3.xn, gd3.yn, gd3.zn, gd3.parent, linkedDatFile, gd3.isTriangle);
                newData.add(newGdata3);
                newGData = newGdata3;
                break;
            case 4:
                GData4 gd4 = (GData4) gData;
                GData4 newGdata4 = new GData4(index, r, g, b, a, gd4.x1p, gd4.y1p, gd4.z1p, gd4.x2p, gd4.y2p, gd4.z2p, gd4.x3p, gd4.y3p, gd4.z3p, gd4.x4p, gd4.y4p, gd4.z4p, gd4.x1, gd4.y1, gd4.z1, gd4.x2,
                        gd4.y2, gd4.z2, gd4.x3, gd4.y3, gd4.z3, gd4.x4, gd4.y4, gd4.z4, gd4.xn, gd4.yn, gd4.zn, gd4.parent, linkedDatFile);
                newData.add(newGdata4);
                newGData = newGdata4;
                break;
            case 5:
                GData5 gd5 = (GData5) gData;
                GData5 newGdata5 = new GData5(index, r, g, b, a, gd5.x1p, gd5.y1p, gd5.z1p, gd5.x2p, gd5.y2p, gd5.z2p, gd5.x3p, gd5.y3p, gd5.z3p, gd5.x4p, gd5.y4p, gd5.z4p, gd5.x1, gd5.y1, gd5.z1, gd5.x2, gd5.y2,
                        gd5.z2, gd5.x3, gd5.y3, gd5.z3, gd5.x4, gd5.y4, gd5.z4, gd5.parent, linkedDatFile);
                newData.add(newGdata5);
                newGData = newGdata5;
                break;
            case 8:
                GDataCSG gdC = (GDataCSG) gData;
                GDataCSG newGdataC = new GDataCSG(linkedDatFile, index, r, g, b, a, gdC);
                newData.add(newGdataC);
                newGData = newGdataC;
                break;
            default:
                continue;
            }
            linker(gData, newGData);
        }
        dataToModify.clear();
        dataToModify.addAll(newData);
    }
}
