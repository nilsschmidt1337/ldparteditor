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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.lwjgl.util.vector.Matrix4f;
import org.nschmidt.ldparteditor.data.tools.IdenticalVertexRemover;
import org.nschmidt.ldparteditor.enums.RotationSnap;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.enums.TransformationMode;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.math.HashBiMap;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeTreeMap;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.text.DatParser;
import org.nschmidt.ldparteditor.text.HeaderState;

public class VM20Manipulator extends VM19ColourChanger {

    protected VM20Manipulator(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    /**
     * Transforms the selection (vertices or data)
     * @param allData
     * @param allVertices
     * @param transformation
     * @param newVertex
     * @param moveAdjacentData
     */
    private void transform(Set<GData> allData, Set<Vertex> allVertices, Matrix transformation, Vector3d newVertex, boolean updateSelection, boolean moveAdjacentData) {
        HashSet<GData> allData2 = new HashSet<GData>(allData);
        TreeSet<Vertex> verticesToTransform = new TreeSet<Vertex>();
        TreeSet<Vertex> transformedLPEvertices = new TreeSet<Vertex>();
        verticesToTransform.addAll(allVertices);
        for (GData gd : allData) {
            Set<VertexInfo> vis = lineLinkedToVertices.get(gd);
            if (vis != null) {
                for (VertexInfo vi : vis) {
                    allVertices.add(vi.vertex);
                }
            }
        }
        TreeMap<Vertex, Vertex> oldToNewVertex = new TreeMap<Vertex, Vertex>();
        // Calculate the new vertex position
        if (newVertex == null) {
            for (Vertex v : allVertices) {
                BigDecimal[] temp = transformation.transform(v.X, v.Y, v.Z);
                oldToNewVertex.put(v, new Vertex(temp[0], temp[1], temp[2]));
            }
        } else {
            for (Vertex v : allVertices) {
                oldToNewVertex.put(v, new Vertex(
                        newVertex.X == null ? v.X : newVertex.X,
                                newVertex.Y == null ? v.Y : newVertex.Y,
                                        newVertex.Z == null ? v.Z : newVertex.Z
                        ));
            }
        }
        // Evaluate the adjacency
        HashMap<GData, Integer> verticesCountPerGData = new HashMap<GData, Integer>();
        for (Vertex v : allVertices) {
            Set<VertexManifestation> manis = vertexLinkedToPositionInFile.get(v);
            if (manis == null) continue;
            for (VertexManifestation m : manis) {
                GData gd = m.getGdata();
                if (lineLinkedToVertices.containsKey(gd)) {
                    final int type = gd.type();
                    switch (type) {
                    case 0:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        break;
                    default:
                        continue;
                    }
                    if (verticesCountPerGData.containsKey(gd)) {
                        verticesCountPerGData.put(gd, verticesCountPerGData.get(gd) + 1);
                    } else {
                        verticesCountPerGData.put(gd, 1);
                    }
                    allData2.add(gd);
                }
            }
        }
        // Transform the data
        if (updateSelection) {
            selectedData.clear();
            selectedLines.clear();
            selectedTriangles.clear();
            selectedQuads.clear();
            selectedCondlines.clear();
        }
        HashSet<GData> allNewData = new HashSet<GData>();
        for (GData gd : allData2) {
            GData newData = null;
            final int type = gd.type();
            switch (type) {
            case 0:
            {
                Vertex[] verts = declaredVertices.get(gd);
                if (verts != null) {
                    if (transformedLPEvertices.contains(verts[0])) {
                        continue;
                    } else {
                        if (!moveAdjacentData) transformedLPEvertices.add(verts[0]);
                        Vertex v1 = oldToNewVertex.get(verts[0]);
                        if (v1 == null) v1 = verts[0];
                        newData = addVertex(v1);
                        if (updateSelection) {
                            selectedVertices.remove(verts[0]);
                            selectedVertices.add(v1);
                        }
                    }
                }
            }
            break;
            case 2:
            {
                int avc = 0;
                Vertex[] verts = lines.get(gd);
                if (verts != null) {
                    Vertex v1 = oldToNewVertex.get(verts[0]);
                    Vertex v2 = oldToNewVertex.get(verts[1]);
                    if (v1 == null) v1 = verts[0]; else avc++;
                    if (v2 == null) v2 = verts[1]; else avc++;
                    if (!moveAdjacentData && (avc != 2 || !allData.contains(gd))) continue;
                    if (updateSelection) {
                        if (selectedVertices.contains(verts[0])) {
                            selectedVertices.remove(verts[0]);
                            selectedVertices.add(v1);
                        }
                        if (selectedVertices.contains(verts[1])) {
                            selectedVertices.remove(verts[1]);
                            selectedVertices.add(v2);
                        }
                    }
                    GData2 g2 = (GData2) gd;
                    newData = new GData2(g2.colourNumber, g2.r, g2.g, g2.b, g2.a, v1, v2, g2.parent, linkedDatFile, g2.isLine);
                }
            }
            break;
            case 3:
            {
                int avc = 0;
                Vertex[] verts = triangles.get(gd);
                if (verts != null) {
                    Vertex v1 = oldToNewVertex.get(verts[0]);
                    Vertex v2 = oldToNewVertex.get(verts[1]);
                    Vertex v3 = oldToNewVertex.get(verts[2]);
                    if (v1 == null) v1 = verts[0]; else avc++;
                    if (v2 == null) v2 = verts[1]; else avc++;
                    if (v3 == null) v3 = verts[2]; else avc++;
                    if (!moveAdjacentData && (avc != 3 || !allData.contains(gd))) continue;
                    if (updateSelection) {
                        if (selectedVertices.contains(verts[0])) {
                            selectedVertices.remove(verts[0]);
                            selectedVertices.add(v1);
                        }
                        if (selectedVertices.contains(verts[1])) {
                            selectedVertices.remove(verts[1]);
                            selectedVertices.add(v2);
                        }
                        if (selectedVertices.contains(verts[2])) {
                            selectedVertices.remove(verts[2]);
                            selectedVertices.add(v3);
                        }
                    }
                    GData3 g3 = (GData3) gd;
                    newData = new GData3(g3.colourNumber, g3.r, g3.g, g3.b, g3.a, v1, v2, v3, g3.parent, linkedDatFile, g3.isTriangle);
                }
            }
            break;
            case 4:
            {
                int avc = 0;
                Vertex[] verts = quads.get(gd);
                if (verts != null) {
                    Vertex v1 = oldToNewVertex.get(verts[0]);
                    Vertex v2 = oldToNewVertex.get(verts[1]);
                    Vertex v3 = oldToNewVertex.get(verts[2]);
                    Vertex v4 = oldToNewVertex.get(verts[3]);
                    if (v1 == null) v1 = verts[0]; else avc++;
                    if (v2 == null) v2 = verts[1]; else avc++;
                    if (v3 == null) v3 = verts[2]; else avc++;
                    if (v4 == null) v4 = verts[3]; else avc++;
                    if (!moveAdjacentData && (avc != 4 || !allData.contains(gd))) continue;
                    if (updateSelection) {
                        if (selectedVertices.contains(verts[0])) {
                            selectedVertices.remove(verts[0]);
                            selectedVertices.add(v1);
                        }
                        if (selectedVertices.contains(verts[1])) {
                            selectedVertices.remove(verts[1]);
                            selectedVertices.add(v2);
                        }
                        if (selectedVertices.contains(verts[2])) {
                            selectedVertices.remove(verts[2]);
                            selectedVertices.add(v3);
                        }
                        if (selectedVertices.contains(verts[3])) {
                            selectedVertices.remove(verts[3]);
                            selectedVertices.add(v4);
                        }
                    }
                    GData4 g4 = (GData4) gd;
                    newData = new GData4(g4.colourNumber, g4.r, g4.g, g4.b, g4.a, v1, v2, v3, v4, g4.parent, linkedDatFile);
                }
            }
            break;
            case 5:
            {
                int avc = 0;
                Vertex[] verts = condlines.get(gd);
                if (verts != null) {
                    Vertex v1 = oldToNewVertex.get(verts[0]);
                    Vertex v2 = oldToNewVertex.get(verts[1]);
                    Vertex v3 = oldToNewVertex.get(verts[2]);
                    Vertex v4 = oldToNewVertex.get(verts[3]);
                    if (v1 == null) v1 = verts[0]; else avc++;
                    if (v2 == null) v2 = verts[1]; else avc++;
                    if (v3 == null) v3 = verts[2]; else avc++;
                    if (v4 == null) v4 = verts[3]; else avc++;
                    if (!moveAdjacentData && (avc != 4 || !allData.contains(gd))) continue;
                    if (updateSelection) {
                        if (selectedVertices.contains(verts[0])) {
                            selectedVertices.remove(verts[0]);
                            selectedVertices.add(v1);
                        }
                        if (selectedVertices.contains(verts[1])) {
                            selectedVertices.remove(verts[1]);
                            selectedVertices.add(v2);
                        }
                        if (selectedVertices.contains(verts[2])) {
                            selectedVertices.remove(verts[2]);
                            selectedVertices.add(v3);
                        }
                        if (selectedVertices.contains(verts[3])) {
                            selectedVertices.remove(verts[3]);
                            selectedVertices.add(v4);
                        }
                    }
                    GData5 g5 = (GData5) gd;
                    newData = new GData5(g5.colourNumber, g5.r, g5.g, g5.b, g5.a, v1, v2, v3, v4, g5.parent, linkedDatFile);
                }
            }
            break;
            default:
                continue;
            }
            if (newData != null) {
                linker(gd, newData);
                allNewData.add(newData);
                setModified_NoSync();
                if (updateSelection) {
                    switch (newData.type()) {
                    case 2:
                        if (verticesCountPerGData.get(gd) != 2) continue;
                        selectedLines.add((GData2) newData);
                        break;
                    case 3:
                        if (verticesCountPerGData.get(gd) != 3) continue;
                        selectedTriangles.add((GData3) newData);
                        break;
                    case 4:
                        if (verticesCountPerGData.get(gd) != 4) continue;
                        selectedQuads.add((GData4) newData);
                        break;
                    case 5:
                        if (verticesCountPerGData.get(gd) != 4) continue;
                        selectedCondlines.add((GData5) newData);
                        break;
                    default:
                        continue;
                    }
                    selectedData.add(newData);
                }
            }
        }
        if (updateSelection && moveAdjacentData) {
            for (Vertex v : oldToNewVertex.keySet()) {
                Vertex nv = oldToNewVertex.get(v);
                if (nv != null) {
                    if (vertexLinkedToPositionInFile.containsKey(nv)) {
                        selectedVertices.add(nv);
                    }
                }
            }
        }
    }

    /**
     *
     * @param transformation
     *            the transformation matrix
     * @param moveAdjacentData
     *            {@code true} if all data should be transformed which is
     *            adjacent to the current selection
     */
    public final synchronized void transformSelection(Matrix transformation, Vector3d newVertex, boolean moveAdjacentData) {
        if (linkedDatFile.isReadOnly())
            return;

        final Set<Vertex> singleVertices = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());

        final HashSet<GData0> effSelectedVertices = new HashSet<GData0>();
        final HashSet<GData2> effSelectedLines = new HashSet<GData2>();
        final HashSet<GData3> effSelectedTriangles = new HashSet<GData3>();
        final HashSet<GData4> effSelectedQuads = new HashSet<GData4>();
        final HashSet<GData5> effSelectedCondlines = new HashSet<GData5>();

        selectedData.clear();

        //-1. Update CSG Tree

        if (GDataCSG.hasSelectionCSG(linkedDatFile)) {
            Matrix4f lowAccTransformation = transformation.getMatrix4f();
            Matrix4f.transpose(lowAccTransformation, lowAccTransformation);
            ArrayList<GData> newSelection = new ArrayList<>();
            for (GDataCSG gd : GDataCSG.getSelection(linkedDatFile))
                newSelection.add(transformCSG(lowAccTransformation, gd));
            GDataCSG.getSelection(linkedDatFile).clear();
            for (GData gd : newSelection) {
                GDataCSG.getSelection(linkedDatFile).add((GDataCSG) gd);
            }
            setModified_NoSync();
        }

        // 0. Deselect selected subfile data (for whole selected subfiles)
        for (GData1 subf : selectedSubfiles) {
            Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
            if (vis == null) continue;
            for (VertexInfo vertexInfo : vis) {
                if (!moveAdjacentData)
                    selectedVertices.remove(vertexInfo.getVertex());
                GData g = vertexInfo.getLinkedData();
                if (lineLinkedToVertices.containsKey(g)) continue;
                switch (g.type()) {
                case 2:
                    selectedLines.remove(g);
                    break;
                case 3:
                    selectedTriangles.remove(g);
                    break;
                case 4:
                    selectedQuads.remove(g);
                    break;
                case 5:
                    selectedCondlines.remove(g);
                    break;
                default:
                    break;
                }
            }
        }

        // 1. Vertex Based Selection
        {
            final Set<Vertex> objectVertices = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());
            if (moveAdjacentData) {
                HashMap<GData, Integer> occurMap = new HashMap<GData, Integer>();
                for (Vertex vertex : selectedVertices) {
                    Set<VertexManifestation> occurences = vertexLinkedToPositionInFile.get(vertex);
                    if (occurences == null)
                        continue;
                    boolean isPureSubfileVertex = true;
                    for (VertexManifestation vm : occurences) {
                        GData g = vm.getGdata();
                        int val = 1;
                        if (occurMap.containsKey(g)) {
                            val = occurMap.get(g);
                            val++;
                        }
                        occurMap.put(g, val);
                        switch (g.type()) {
                        case 0:
                            GData0 meta = (GData0) g;
                            boolean idCheck = !lineLinkedToVertices.containsKey(meta);
                            isPureSubfileVertex = isPureSubfileVertex && idCheck;
                            if (val == 1) {
                                if (!idCheck) {
                                    effSelectedVertices.add(meta);
                                    newSelectedData.add(meta);
                                }
                            }
                            break;
                        case 2:
                            GData2 line = (GData2) g;
                            idCheck = !line.parent.equals(View.DUMMY_REFERENCE);
                            isPureSubfileVertex = isPureSubfileVertex && idCheck;
                            if (val == 2) {
                                if (!idCheck) {
                                    effSelectedLines.add(line);
                                    newSelectedData.add(line);
                                }
                            }
                            break;
                        case 3:
                            GData3 triangle = (GData3) g;
                            idCheck = !triangle.parent.equals(View.DUMMY_REFERENCE);
                            isPureSubfileVertex = isPureSubfileVertex && idCheck;
                            if (val == 3) {
                                if (!idCheck) {
                                    effSelectedTriangles.add(triangle);
                                    newSelectedData.add(triangle);
                                }
                            }
                            break;
                        case 4:
                            GData4 quad = (GData4) g;
                            idCheck = !quad.parent.equals(View.DUMMY_REFERENCE);
                            isPureSubfileVertex = isPureSubfileVertex && idCheck;
                            if (val == 4) {
                                if (!idCheck) {
                                    effSelectedQuads.add(quad);
                                    newSelectedData.add(quad);
                                }
                            }
                            break;
                        case 5:
                            GData5 condline = (GData5) g;
                            idCheck = !condline.parent.equals(View.DUMMY_REFERENCE);
                            isPureSubfileVertex = isPureSubfileVertex && idCheck;
                            if (val == 4) {
                                if (!idCheck) {
                                    effSelectedCondlines.add(condline);
                                    newSelectedData.add(condline);
                                }
                            }
                            break;
                        }
                    }
                    if (isPureSubfileVertex)
                        objectVertices.add(vertex);
                }
            }

            // 2. Object Based Selection

            for (GData2 line : selectedLines) {
                if (line.parent.equals(View.DUMMY_REFERENCE))
                    effSelectedLines.add(line);
                Vertex[] verts = lines.get(line);
                if (verts == null)
                    continue;
                for (Vertex vertex : verts) {
                    objectVertices.add(vertex);
                }
            }
            for (GData3 triangle : selectedTriangles) {
                if (triangle.parent.equals(View.DUMMY_REFERENCE))
                    effSelectedTriangles.add(triangle);
                Vertex[] verts = triangles.get(triangle);
                if (verts == null)
                    continue;
                for (Vertex vertex : verts) {
                    objectVertices.add(vertex);
                }
            }
            for (GData4 quad : selectedQuads) {
                if (quad.parent.equals(View.DUMMY_REFERENCE))
                    effSelectedQuads.add(quad);
                Vertex[] verts = quads.get(quad);
                if (verts == null)
                    continue;
                for (Vertex vertex : verts) {
                    objectVertices.add(vertex);
                }
            }
            for (GData5 condline : selectedCondlines) {
                if (condline.parent.equals(View.DUMMY_REFERENCE))
                    effSelectedCondlines.add(condline);
                Vertex[] verts = condlines.get(condline);
                if (verts == null)
                    continue;
                for (Vertex vertex : verts) {
                    objectVertices.add(vertex);
                }
            }

            Set<GData0> vs = new HashSet<GData0>(effSelectedVertices);
            for (GData0 effvert : vs) {
                Vertex v = effvert.getVertex();
                if (v != null && objectVertices.contains(v)) {
                    effSelectedVertices.remove(effvert);
                }
            }

            singleVertices.addAll(selectedVertices);
            singleVertices.removeAll(objectVertices);

            // Reduce the amount of superflous selected data
            selectedVertices.clear();
            selectedVertices.addAll(singleVertices);

            selectedLines.clear();
            selectedLines.addAll(effSelectedLines);

            selectedTriangles.clear();
            selectedTriangles.addAll(effSelectedTriangles);

            selectedQuads.clear();
            selectedQuads.addAll(effSelectedQuads);

            selectedCondlines.clear();
            selectedCondlines.addAll(effSelectedCondlines);

            // 3. Transformation of the selected data (no whole subfiles!!)
            // + selectedData update!


            HashSet<GData> allData = new HashSet<GData>();
            allData.addAll(selectedLines);
            allData.addAll(selectedTriangles);
            allData.addAll(selectedQuads);
            allData.addAll(selectedCondlines);
            HashSet<Vertex> allVertices = new HashSet<Vertex>();
            allVertices.addAll(selectedVertices);
            transform(allData, allVertices, transformation, newVertex, true, moveAdjacentData);

            // 4. Subfile Based Transformation & Selection
            if (!selectedSubfiles.isEmpty()) {
                HashBiMap<Integer, GData> drawPerLine = linkedDatFile.getDrawPerLine_NOCLONE();
                HashSet<GData1> newSubfiles = new HashSet<GData1>();
                for (GData1 subf : selectedSubfiles) {
                    if (!drawPerLine.containsKey(subf)) {
                        continue;
                    }
                    String transformedString = subf.getTransformedString(transformation, linkedDatFile, true);
                    GData transformedSubfile = DatParser
                            .parseLine(transformedString, drawPerLine.getKey(subf).intValue(), 0, subf.r, subf.g, subf.b, subf.a, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile,
                                    false, new HashSet<String>(), false).get(0).getGraphicalData();
                    if (subf.equals(linkedDatFile.getDrawChainTail()))
                        linkedDatFile.setDrawChainTail(transformedSubfile);
                    GData oldNext = subf.getNext();
                    GData oldBefore = subf.getBefore();
                    oldBefore.setNext(transformedSubfile);
                    transformedSubfile.setNext(oldNext);
                    Integer oldNumber = drawPerLine.getKey(subf);
                    if (oldNumber != null)
                        drawPerLine.put(oldNumber, transformedSubfile);
                    remove(subf);
                    newSubfiles.add((GData1) transformedSubfile);
                }
                selectedSubfiles.clear();
                selectedSubfiles.addAll(newSubfiles);

                for (GData1 subf : selectedSubfiles) {
                    Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
                    if (vis == null) continue;
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
                setModified_NoSync();
            }

            if (isModified()) {
                selectedData.addAll(selectedLines);
                selectedData.addAll(selectedTriangles);
                selectedData.addAll(selectedQuads);
                selectedData.addAll(selectedCondlines);
                selectedData.addAll(selectedSubfiles);
                restoreHideShowState();
                syncWithTextEditors(true);
                updateUnsavedStatus();

            }
            newSelectedData.clear();
            selectedVertices.retainAll(vertexLinkedToPositionInFile.keySet());
        }
    }

    private GDataCSG transformCSG(Matrix4f lowAccTransformation, GDataCSG gData) {
        GDataCSG gdC = gData;
        GDataCSG newGData = new GDataCSG(linkedDatFile, lowAccTransformation, gdC);
        linker(gData, newGData);
        return newGData;
    }

    public final void setXyzOrTranslateOrTransform(Vertex target, Vertex pivot, TransformationMode tm, boolean x, boolean y, boolean z, boolean moveAdjacentData, boolean syncWithTextEditors) {
        if (linkedDatFile.isReadOnly())
            return;

        backupHideShowState();

        boolean swapWinding = false;
        Matrix transformation = null;
        Vertex offset = null;
        if (tm == TransformationMode.TRANSLATE) offset = new Vertex(target.X, target.Y, target.Z);

        if (pivot == null) pivot = new Vertex(0f, 0f, 0f);

        switch (tm) {
        case ROTATE:
            RotationSnap flag;
            if (x) {
                try {
                    target.X.intValueExact();
                    switch (Math.abs(target.X.intValue())) {
                    case 90:
                        flag = RotationSnap.DEG90;
                        break;
                    case 180:
                        flag = RotationSnap.DEG180;
                        break;
                    case 270:
                        flag = RotationSnap.DEG270;
                        break;
                    case 360:
                        flag = RotationSnap.DEG360;
                        break;
                    default:
                        flag = RotationSnap.COMPLEX;
                        break;
                    }
                } catch (ArithmeticException ae) {
                    flag = RotationSnap.COMPLEX;
                }
                transformation = View.ACCURATE_ID.rotate(target.X.divide(new BigDecimal(180), Threshold.mc).multiply(new BigDecimal(Math.PI)), flag, new BigDecimal[] { BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO });
            } else if (y) {
                try {
                    target.Y.intValueExact();
                    switch (Math.abs(target.Y.intValue())) {
                    case 90:
                        flag = RotationSnap.DEG90;
                        break;
                    case 180:
                        flag = RotationSnap.DEG180;
                        break;
                    case 270:
                        flag = RotationSnap.DEG270;
                        break;
                    case 360:
                        flag = RotationSnap.DEG360;
                        break;
                    default:
                        flag = RotationSnap.COMPLEX;
                        break;
                    }
                } catch (ArithmeticException ae) {
                    flag = RotationSnap.COMPLEX;
                }
                transformation = View.ACCURATE_ID.rotate(target.Y.divide(new BigDecimal(180), Threshold.mc).multiply(new BigDecimal(Math.PI)), flag, new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO });
            } else {
                try {
                    target.Z.intValueExact();
                    switch (Math.abs(target.Z.intValue())) {
                    case 90:
                        flag = RotationSnap.DEG90;
                        break;
                    case 180:
                        flag = RotationSnap.DEG180;
                        break;
                    case 270:
                        flag = RotationSnap.DEG270;
                        break;
                    case 360:
                        flag = RotationSnap.DEG360;
                        break;
                    default:
                        flag = RotationSnap.COMPLEX;
                        break;
                    }
                } catch (ArithmeticException ae) {
                    flag = RotationSnap.COMPLEX;
                }
                transformation = View.ACCURATE_ID.rotate(target.Z.divide(new BigDecimal(180), Threshold.mc).multiply(new BigDecimal(Math.PI)), flag, new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE });
            }
            break;
        case SCALE:
            BigDecimal sx = target.X;
            BigDecimal sy = target.Y;
            BigDecimal sz = target.Z;

            int count = 0;
            int cmp1 = 0;
            int cmp2 = 0;
            int cmp3 = 0;
            if ((cmp1 = sx.compareTo(BigDecimal.ZERO)) == 0) sx = new BigDecimal("0.000000001"); //$NON-NLS-1$
            if ((cmp2 = sy.compareTo(BigDecimal.ZERO)) == 0) sy = new BigDecimal("0.000000001"); //$NON-NLS-1$
            if ((cmp3 = sz.compareTo(BigDecimal.ZERO)) == 0) sz = new BigDecimal("0.000000001"); //$NON-NLS-1$
            if (cmp1 < 0) count++;
            if (cmp2 < 0) count++;
            if (cmp3 < 0) count++;
            swapWinding = count == 1 || count == 3;

            transformation = new Matrix(
                    x ? sx : BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                            BigDecimal.ZERO, y ? sy : BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
                                    BigDecimal.ZERO, BigDecimal.ZERO, z ? sz : BigDecimal.ONE, BigDecimal.ZERO,
                                            BigDecimal.ZERO,  BigDecimal.ZERO,  BigDecimal.ZERO, BigDecimal.ONE);
            break;
        case SET:
            break;
        case TRANSLATE:
            transformation = new Matrix(
                    BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO,
                    offset.X, offset.Y, offset.Z, BigDecimal.ONE);
            break;
        default:
            break;
        }

        if (tm == TransformationMode.SET) {

            transformSelection(View.ACCURATE_ID, new Vector3d(x ? target.X : null,  y ? target.Y : null, z ? target.Z : null), moveAdjacentData);

        } else {
            final Matrix forward = Matrix.mul(View.ACCURATE_ID.translate(new BigDecimal[] { pivot.X.negate(), pivot.Y.negate(), pivot.Z.negate() }), View.ACCURATE_ID);
            final Matrix backward = Matrix.mul(View.ACCURATE_ID.translate(new BigDecimal[] { pivot.X, pivot.Y, pivot.Z }), View.ACCURATE_ID);

            transformation = Matrix.mul(backward, transformation);
            transformation = Matrix.mul(transformation, forward);

            transformSelection(transformation, null, moveAdjacentData);

            if (swapWinding) {
                backupHideShowState();
                windingChangeSelection(syncWithTextEditors);
            }

        }

        IdenticalVertexRemover.removeIdenticalVertices(this, linkedDatFile, false, true);

        if (syncWithTextEditors) syncWithTextEditors(true);
        updateUnsavedStatus();
        selectedVertices.retainAll(vertexLinkedToPositionInFile.keySet());

    }

    public void transformSubfile(GData1 g, Matrix M, boolean clearSelection, boolean syncWithTextEditor) {
        HashBiMap<Integer, GData> drawPerLine = linkedDatFile.getDrawPerLine_NOCLONE();
        HeaderState.state().setState(HeaderState._99_DONE);
        StringBuilder colourBuilder = new StringBuilder();
        if (g.colourNumber == -1) {
            colourBuilder.append("0x2"); //$NON-NLS-1$
            colourBuilder.append(MathHelper.toHex((int) (255f * g.r)).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * g.g)).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * g.b)).toUpperCase());
        } else {
            colourBuilder.append(g.colourNumber);
        }
        // Clear the cache..
        GData.parsedLines.clear();
        GData.CACHE_parsedFilesSource.clear();
        GData1 reloadedSubfile = (GData1) DatParser
                .parseLine("1 " + colourBuilder.toString() + M.toLDrawString() + g.shortName , 0, 0, 0.5f, 0.5f, 0.5f, 1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile, false, //$NON-NLS-1$
                        new HashSet<String>(), false).get(0).getGraphicalData();
        // Clear the cache..
        GData.parsedLines.clear();
        GData.CACHE_parsedFilesSource.clear();
        GData oldNext = g.getNext();
        GData oldBefore = g.getBefore();
        oldBefore.setNext(reloadedSubfile);
        reloadedSubfile.setNext(oldNext);
        Integer oldNumber = drawPerLine.getKey(g);
        if (oldNumber != null)
            drawPerLine.put(oldNumber, reloadedSubfile);
        remove(g);
        if (clearSelection) {
            clearSelection();
        } else {
            selectedData.remove(g);
            selectedSubfiles.remove(g);
        }
        selectedData.add(reloadedSubfile);
        selectedSubfiles.add(reloadedSubfile);
        selectWholeSubfiles();
        if (syncWithTextEditor) {
            restoreHideShowState();
            setModified(true, true);
        } else {
            setModified_NoSync();
        }
    }
}
