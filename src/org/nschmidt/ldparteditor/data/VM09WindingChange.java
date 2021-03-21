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
import org.nschmidt.ldparteditor.helpers.math.HashBiMap;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.text.DatParser;

class VM09WindingChange extends VM08SlicerPro {

    protected VM09WindingChange(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public synchronized void windingChangeSelection(boolean syncWithEditors) {
        if (linkedDatFile.isReadOnly())
            return;

        final HashSet<GData2> effSelectedLines = new HashSet<>();
        final HashSet<GData3> effSelectedTriangles = new HashSet<>();
        final HashSet<GData4> effSelectedQuads = new HashSet<>();
        final HashSet<GData5> effSelectedCondlines = new HashSet<>();

        final HashSet<GData2> subSelectedLines = new HashSet<>();
        final HashSet<GData3> subSelectedTriangles = new HashSet<>();
        final HashSet<GData4> subSelectedQuads = new HashSet<>();
        final HashSet<GData5> subSelectedCondlines = new HashSet<>();

        final GColour col16 = View.getLDConfigColour(16);

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

        // 3. Winding change of the selected data (no whole subfiles!!)
        // + selectedData update!

        ArrayList<GData> modData = new ArrayList<>();

        for (GData3 gd : effSelectedTriangles)
            modData.add(changeWinding(gd));
        for (GData4 gd : effSelectedQuads)
            modData.add(changeWinding(gd));

        modData.addAll(effSelectedLines);
        modData.addAll(effSelectedCondlines);

        selectedLines.clear();
        selectedTriangles.clear();
        selectedQuads.clear();
        selectedCondlines.clear();

        for (GData gData : modData) {
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
            }
        }

        if (!selectedTriangles.isEmpty())
            setModified_NoSync();
        if (!selectedQuads.isEmpty())
            setModified_NoSync();

        // 4. Subfile Based Change & Selection
        if (!selectedSubfiles.isEmpty()) {


            final HashBiMap<Integer, GData> dpl = linkedDatFile.getDrawPerLine_NOCLONE();
            HashSet<GData1> newSubfiles = new HashSet<>();
            for (GData1 subf : selectedSubfiles) {
                GData1 untransformedSubfile;
                StringBuilder colourBuilder = new StringBuilder();
                if (subf.colourNumber == -1) {
                    colourBuilder.append("0x2"); //$NON-NLS-1$
                    colourBuilder.append(MathHelper.toHex((int) (255f * subf.r)).toUpperCase());
                    colourBuilder.append(MathHelper.toHex((int) (255f * subf.g)).toUpperCase());
                    colourBuilder.append(MathHelper.toHex((int) (255f * subf.b)).toUpperCase());
                } else {
                    colourBuilder.append(subf.colourNumber);
                }
                untransformedSubfile = (GData1) DatParser
                        .parseLine("1 " + colourBuilder.toString() + " 0 0 0 1 0 0 0 1 0 0 0 1 " + subf.shortName , 0, 0, col16.getR(), col16.getG(), col16.getB(), 1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile, false, //$NON-NLS-1$ //$NON-NLS-2$
                                new HashSet<>()).get(0).getGraphicalData();
                if (untransformedSubfile == null) {
                    continue;
                }

                boolean plainOnX = untransformedSubfile.boundingBoxMin.x - untransformedSubfile.boundingBoxMax.x == 0f;
                boolean plainOnY = untransformedSubfile.boundingBoxMin.y - untransformedSubfile.boundingBoxMax.y == 0f;
                boolean plainOnZ = untransformedSubfile.boundingBoxMin.z - untransformedSubfile.boundingBoxMax.z == 0f;


                boolean hasInvertnext = false;
                GData invertNextData = subf.getBefore();

                // Check if a INVERTNEXT is present
                while (invertNextData != null && invertNextData.type() != 1 && (invertNextData.type() != 6 || ((GDataBFC) invertNextData).type != BFC.INVERTNEXT)) {
                    invertNextData = invertNextData.getBefore();
                }
                if (invertNextData != null && invertNextData.type() == 6) {
                    hasInvertnext = true;
                }

                boolean needsInvertNext = false;

                GData1 newSubfile = subf;

                if (plainOnX ^ plainOnY ^ plainOnZ) {
                    Matrix m;
                    final Matrix t = subf.accurateLocalMatrix;
                    if (plainOnX) {
                        m = new Matrix(
                                t.M00.negate(), t.M01.negate(), t.M02.negate(), t.M03,
                                t.M10, t.M11, t.M12, t.M13,
                                t.M20, t.M21, t.M22, t.M23,
                                t.M30, t.M31, t.M32, t.M33);
                    } else if (plainOnY) {
                        m = new Matrix(
                                t.M00, t.M01, t.M02, t.M03,
                                t.M10.negate(), t.M11.negate(), t.M12.negate(), t.M13,
                                t.M20, t.M21, t.M22, t.M23,
                                t.M30, t.M31, t.M32, t.M33);
                    } else {
                        m = new Matrix(
                                t.M00, t.M01, t.M02, t.M03,
                                t.M10, t.M11, t.M12, t.M13,
                                t.M20.negate(), t.M21.negate(), t.M22.negate(), t.M23,
                                t.M30, t.M31, t.M32, t.M33);
                    }
                    newSubfile = (GData1) DatParser
                            .parseLine(untransformedSubfile.getTransformedString(m, null, linkedDatFile, false) , dpl.getKey(subf).intValue(), 0, col16.getR(), col16.getG(), col16.getB(), 1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile, false,
                                    new HashSet<>()).get(0).getGraphicalData();

                    if (newSubfile == null) {
                        continue;
                    }
                } else {
                    needsInvertNext = true;
                }
                remove(untransformedSubfile);

                if (hasInvertnext) {
                    // Remove Invert Next
                    GDataBFC gbfc = (GDataBFC) invertNextData;
                    dpl.removeByValue(gbfc);
                    gbfc.getBefore().setNext(gbfc.getNext());
                    remove(gbfc);
                } else if (needsInvertNext && !hasInvertnext) {
                    // Add Invert Next
                    GData before = subf.getBefore();
                    int lineToInsert = dpl.getKey(before);
                    TreeSet<Integer> ts = new TreeSet<>();
                    ts.addAll(dpl.keySet());
                    for (Iterator<Integer> dsi = ts.descendingIterator(); dsi.hasNext();) {
                        Integer k = dsi.next();
                        if (k > lineToInsert) {
                            GData gdata = dpl.getValue(k);
                            dpl.removeByKey(k);
                            dpl.put(k + 1, gdata);
                        }
                    }
                    GDataBFC newInvNext = new GDataBFC(BFC.INVERTNEXT, View.DUMMY_REFERENCE);
                    dpl.put(lineToInsert + 1, newInvNext);
                    before.setNext(newInvNext);
                    newInvNext.setNext(subf);
                }

                if (hasInvertnext || needsInvertNext && !hasInvertnext) {

                    // Update Draw per line

                    TreeSet<Integer> ts = new TreeSet<>();
                    ts.addAll(dpl.keySet());

                    int counter = 1;
                    GData tail = null;
                    for (Integer k : ts) {
                        GData gdata = dpl.getValue(k);
                        dpl.removeByKey(k);
                        dpl.put(counter, gdata);
                        counter++;
                        tail = gdata;
                    }
                    if (tail != null) {
                        linkedDatFile.setDrawChainTail(tail);
                    } else {
                        GData0 blankLine = new GData0("", View.DUMMY_REFERENCE); //$NON-NLS-1$
                        linkedDatFile.getDrawChainStart().setNext(blankLine);
                        dpl.put(1, blankLine);
                        linkedDatFile.setDrawChainTail(blankLine);
                    }
                }

                if (!subf.equals(newSubfile)) {
                    if (subf.equals(linkedDatFile.getDrawChainTail()))
                        linkedDatFile.setDrawChainTail(newSubfile);
                    GData oldNext = subf.getNext();
                    GData oldBefore = subf.getBefore();
                    oldBefore.setNext(newSubfile);
                    newSubfile.setNext(oldNext);
                    Integer oldNumber = dpl.getKey(subf);
                    if (oldNumber != null)
                        dpl.put(oldNumber, newSubfile);
                    remove(subf);
                }
                newSubfiles.add(newSubfile);

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
            setModified_NoSync();
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
            linkedDatFile.getVertexManager().restoreHideShowState();
            if (syncWithEditors) syncWithTextEditors(true);
            updateUnsavedStatus();
        }

    }

    private GData changeWinding(GData gData) {
        GData result = null;
        if (gData.type() < 4) {
            GData3 gd3 = (GData3) gData;
            GData3 newGdata3 = new GData3(gd3.colourNumber, gd3.r, gd3.g, gd3.b, gd3.a, gd3.X2, gd3.Y2, gd3.Z2, gd3.X1, gd3.Y1, gd3.Z1, gd3.X3, gd3.Y3, gd3.Z3, gd3.x2, gd3.y2, gd3.z2, gd3.x1, gd3.y1, gd3.z1, gd3.x3,
                    gd3.y3, gd3.z3, -gd3.xn, -gd3.yn, -gd3.zn, gd3.parent, linkedDatFile, gd3.isTriangle);
            result = newGdata3;
        } else {
            GData4 gd4 = (GData4) gData;
            GData4 newGdata4 = new GData4(gd4.colourNumber, gd4.r, gd4.g, gd4.b, gd4.a, gd4.X3, gd4.Y3, gd4.Z3, gd4.X2, gd4.Y2, gd4.Z2, gd4.X1, gd4.Y1, gd4.Z1, gd4.X4, gd4.Y4, gd4.Z4, gd4.x3, gd4.y3, gd4.z3, gd4.x2,
                    gd4.y2, gd4.z2, gd4.x1, gd4.y1, gd4.z1, gd4.x4, gd4.y4, gd4.z4, -gd4.xn, -gd4.yn, -gd4.zn, gd4.parent, linkedDatFile);
            result = newGdata4;
        }
        linker(gData, result);
        return result;
    }
}
