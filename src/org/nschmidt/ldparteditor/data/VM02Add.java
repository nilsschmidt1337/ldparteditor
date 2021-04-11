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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.text.DatParser;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * @author nils
 *
 */
class VM02Add extends VM01SelectHelper {

    protected VM02Add(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public synchronized GData0 addVertex(Vertex vertex, GData0 vertexTag) {
        if (vertex == null || vertexTag == null) {
            vertexTag = new GData0("0 !LPE VERTEX 0 0 0", View.DUMMY_REFERENCE); //$NON-NLS-1$
            vertex = new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        getManifestationLock().lock();
        if (!vertexLinkedToPositionInFile.containsKey(vertex))
            vertexLinkedToPositionInFile.put(vertex, Collections.newSetFromMap(new ThreadsafeHashMap<>()));
        vertexLinkedToPositionInFile.get(vertex).add(new VertexManifestation(0, vertexTag));
        getManifestationLock().unlock();
        lineLinkedToVertices.put(vertexTag, Collections.newSetFromMap(new ThreadsafeHashMap<>()));
        lineLinkedToVertices.get(vertexTag).add(new VertexInfo(vertex, 0, vertexTag));
        declaredVertices.put(vertexTag, new Vertex[] { vertex });
        return vertexTag;
    }

    public synchronized GData0 addSubfileVertex(Vertex vertex, GData0 vertexTag, GData1 subfile) {
        if (!vertexCountInSubfile.containsKey(subfile)) {
            vertexCountInSubfile.put(subfile, 0);
        }
        int vertexCount = vertexCountInSubfile.get(subfile);
        vertexCount--;
        getManifestationLock().lock();
        if (!vertexLinkedToPositionInFile.containsKey(vertex))
            vertexLinkedToPositionInFile.put(vertex, Collections.newSetFromMap(new ThreadsafeHashMap<>()));
        vertexLinkedToPositionInFile.get(vertex).add(new VertexManifestation(0, vertexTag));
        getManifestationLock().unlock();
        if (!lineLinkedToVertices.containsKey(subfile))
            lineLinkedToVertices.put(subfile, Collections.newSetFromMap(new ThreadsafeHashMap<>()));
        lineLinkedToVertices.get(subfile).add(new VertexInfo(vertex, vertexCount, vertexTag));
        declaredVertices.put(vertexTag, new Vertex[] { vertex });
        vertexCountInSubfile.put(subfile, vertexCount);
        if (!vertexLinkedToSubfile.containsKey(vertex)) {
            vertexLinkedToSubfile.put(vertex, Collections.newSetFromMap(new ThreadsafeHashMap<>()));
        }
        vertexLinkedToSubfile.get(vertex).add(subfile);
        return vertexTag;
    }

    public synchronized void add(GData gdata) {

        final GData originalData = gdata;
        final boolean subVertex;
        final int max;
        switch (gdata.type()) {
        case 2: // Line
            GData2 gd2 = (GData2) gdata;
            GData1 parent2 = gd2.parent;
            subVertex = parent2.depth > 0;
            Vector4f[] lv = new Vector4f[2];
            lv[0] = new Vector4f(gd2.x1, gd2.y1, gd2.z1, 1f);
            lv[1] = new Vector4f(gd2.x2, gd2.y2, gd2.z2, 1f);
            if (subVertex) {
                gdata = parent2.firstRef;
                Matrix4f matrix = parent2.productMatrix;
                Matrix4f.transform(matrix, lv[0], lv[0]);
                Matrix4f.transform(matrix, lv[1], lv[1]);
                GData1.updateBoundingBox(2, lv[0], lv[1], null, null, parent2);
            }
            vdArray[0] = new VertexManifestation(0, gd2);
            vdArray[1] = new VertexManifestation(1, gd2);
            if (gd2.x1p == null) {
                vArray[0] = new Vertex(lv[0]);
                vArray[1] = new Vertex(lv[1]);
            } else {
                if (subVertex) {
                    Matrix matrix = parent2.accurateProductMatrix;
                    BigDecimal[] p1 = matrix.transform(gd2.x1p, gd2.y1p, gd2.z1p);
                    BigDecimal[] p2 = matrix.transform(gd2.x2p, gd2.y2p, gd2.z2p);
                    vArray[0] = new Vertex(p1[0], p1[1], p1[2], lv[0]);
                    vArray[1] = new Vertex(p2[0], p2[1], p2[2], lv[1]);
                } else {
                    vArray[0] = new Vertex(gd2.x1p, gd2.y1p, gd2.z1p, lv[0]);
                    vArray[1] = new Vertex(gd2.x2p, gd2.y2p, gd2.z2p, lv[1]);
                }
            }
            lines.put(gd2, Arrays.copyOf(vArray, 2));
            max = 2;
            break;
        case 3: // Triangle
            GData3 gd3 = (GData3) gdata;
            GData1 parent3 = gd3.parent;
            subVertex = parent3.depth > 0;
            Vector4f[] tv = new Vector4f[3];
            tv[0] = new Vector4f(gd3.x1, gd3.y1, gd3.z1, 1f);
            tv[1] = new Vector4f(gd3.x2, gd3.y2, gd3.z2, 1f);
            tv[2] = new Vector4f(gd3.x3, gd3.y3, gd3.z3, 1f);
            if (subVertex) {
                gdata = parent3.firstRef;
                Matrix4f matrix = parent3.productMatrix;
                Matrix4f.transform(matrix, tv[0], tv[0]);
                Matrix4f.transform(matrix, tv[1], tv[1]);
                Matrix4f.transform(matrix, tv[2], tv[2]);
                GData1.updateBoundingBox(3, tv[0], tv[1], tv[2], null, parent3);
            }
            vdArray[0] = new VertexManifestation(0, gd3);
            vdArray[1] = new VertexManifestation(1, gd3);
            vdArray[2] = new VertexManifestation(2, gd3);
            if (gd3.x1p == null) {
                vArray[0] = new Vertex(tv[0]);
                vArray[1] = new Vertex(tv[1]);
                vArray[2] = new Vertex(tv[2]);
            } else {
                if (subVertex) {
                    Matrix matrix = parent3.accurateProductMatrix;
                    BigDecimal[] p1 = matrix.transform(gd3.x1p, gd3.y1p, gd3.z1p);
                    BigDecimal[] p2 = matrix.transform(gd3.x2p, gd3.y2p, gd3.z2p);
                    BigDecimal[] p3 = matrix.transform(gd3.x3p, gd3.y3p, gd3.z3p);
                    vArray[0] = new Vertex(p1[0], p1[1], p1[2], tv[0]);
                    vArray[1] = new Vertex(p2[0], p2[1], p2[2], tv[1]);
                    vArray[2] = new Vertex(p3[0], p3[1], p3[2], tv[2]);
                } else {
                    vArray[0] = new Vertex(gd3.x1p, gd3.y1p, gd3.z1p, tv[0]);
                    vArray[1] = new Vertex(gd3.x2p, gd3.y2p, gd3.z2p, tv[1]);
                    vArray[2] = new Vertex(gd3.x3p, gd3.y3p, gd3.z3p, tv[2]);
                }
            }
            triangles.put(gd3, Arrays.copyOf(vArray, 3));
            max = 3;
            break;
        case 4: // Quad
            GData4 gd4 = (GData4) gdata;
            GData1 parent4 = gd4.parent;
            subVertex = parent4.depth > 0;
            Vector4f[] qv = new Vector4f[4];
            qv[0] = new Vector4f(gd4.x1, gd4.y1, gd4.z1, 1f);
            qv[1] = new Vector4f(gd4.x2, gd4.y2, gd4.z2, 1f);
            qv[2] = new Vector4f(gd4.x3, gd4.y3, gd4.z3, 1f);
            qv[3] = new Vector4f(gd4.x4, gd4.y4, gd4.z4, 1f);
            if (subVertex) {
                gdata = parent4.firstRef;
                Matrix4f matrix4 = parent4.productMatrix;
                Matrix4f.transform(matrix4, qv[0], qv[0]);
                Matrix4f.transform(matrix4, qv[1], qv[1]);
                Matrix4f.transform(matrix4, qv[2], qv[2]);
                Matrix4f.transform(matrix4, qv[3], qv[3]);
                GData1.updateBoundingBox(4, qv[0], qv[1], qv[2], qv[3], parent4);
            }
            vdArray[0] = new VertexManifestation(0, gd4);
            vdArray[1] = new VertexManifestation(1, gd4);
            vdArray[2] = new VertexManifestation(2, gd4);
            vdArray[3] = new VertexManifestation(3, gd4);
            if (gd4.x1p == null) {
                vArray[0] = new Vertex(qv[0]);
                vArray[1] = new Vertex(qv[1]);
                vArray[2] = new Vertex(qv[2]);
                vArray[3] = new Vertex(qv[3]);
            } else {
                if (subVertex) {
                    Matrix matrix = parent4.accurateProductMatrix;
                    BigDecimal[] p1 = matrix.transform(gd4.x1p, gd4.y1p, gd4.z1p);
                    BigDecimal[] p2 = matrix.transform(gd4.x2p, gd4.y2p, gd4.z2p);
                    BigDecimal[] p3 = matrix.transform(gd4.x3p, gd4.y3p, gd4.z3p);
                    BigDecimal[] p4 = matrix.transform(gd4.x4p, gd4.y4p, gd4.z4p);
                    vArray[0] = new Vertex(p1[0], p1[1], p1[2], qv[0]);
                    vArray[1] = new Vertex(p2[0], p2[1], p2[2], qv[1]);
                    vArray[2] = new Vertex(p3[0], p3[1], p3[2], qv[2]);
                    vArray[3] = new Vertex(p4[0], p4[1], p4[2], qv[3]);
                } else {
                    vArray[0] = new Vertex(gd4.x1p, gd4.y1p, gd4.z1p, qv[0]);
                    vArray[1] = new Vertex(gd4.x2p, gd4.y2p, gd4.z2p, qv[1]);
                    vArray[2] = new Vertex(gd4.x3p, gd4.y3p, gd4.z3p, qv[2]);
                    vArray[3] = new Vertex(gd4.x4p, gd4.y4p, gd4.z4p, qv[3]);
                }
            }
            quads.put(gd4, Arrays.copyOf(vArray, 4));
            max = 4;
            break;
        case 5: // Optional Line
            GData5 gd5 = (GData5) gdata;
            GData1 parent5 = gd5.parent;
            subVertex = parent5.depth > 0;
            Vector4f[] ov = new Vector4f[4];
            ov[0] = new Vector4f(gd5.x1, gd5.y1, gd5.z1, 1f);
            ov[1] = new Vector4f(gd5.x2, gd5.y2, gd5.z2, 1f);
            ov[2] = new Vector4f(gd5.x3, gd5.y3, gd5.z3, 1f);
            ov[3] = new Vector4f(gd5.x4, gd5.y4, gd5.z4, 1f);
            if (subVertex) {
                Matrix4f matrix5 = parent5.productMatrix;
                Matrix4f.transform(matrix5, ov[0], ov[0]);
                Matrix4f.transform(matrix5, ov[1], ov[1]);
                Matrix4f.transform(matrix5, ov[2], ov[2]);
                Matrix4f.transform(matrix5, ov[3], ov[3]);
                gdata = parent5.firstRef;
                GData1.updateBoundingBox(2, ov[0], ov[1], null, null, parent5);
            }
            vdArray[0] = new VertexManifestation(0, gd5);
            vdArray[1] = new VertexManifestation(1, gd5);
            vdArray[2] = new VertexManifestation(2, gd5);
            vdArray[3] = new VertexManifestation(3, gd5);
            if (gd5.x1p == null) {
                vArray[0] = new Vertex(ov[0]);
                vArray[1] = new Vertex(ov[1]);
                vArray[2] = new Vertex(ov[2]);
                vArray[3] = new Vertex(ov[3]);
            } else {
                if (subVertex) {
                    Matrix matrix = parent5.accurateProductMatrix;
                    BigDecimal[] p1 = matrix.transform(gd5.x1p, gd5.y1p, gd5.z1p);
                    BigDecimal[] p2 = matrix.transform(gd5.x2p, gd5.y2p, gd5.z2p);
                    BigDecimal[] p3 = matrix.transform(gd5.x3p, gd5.y3p, gd5.z3p);
                    BigDecimal[] p4 = matrix.transform(gd5.x4p, gd5.y4p, gd5.z4p);
                    vArray[0] = new Vertex(p1[0], p1[1], p1[2], ov[0]);
                    vArray[1] = new Vertex(p2[0], p2[1], p2[2], ov[1]);
                    vArray[2] = new Vertex(p3[0], p3[1], p3[2], ov[2]);
                    vArray[3] = new Vertex(p4[0], p4[1], p4[2], ov[3]);
                } else {
                    vArray[0] = new Vertex(gd5.x1p, gd5.y1p, gd5.z1p, ov[0]);
                    vArray[1] = new Vertex(gd5.x2p, gd5.y2p, gd5.z2p, ov[1]);
                    vArray[2] = new Vertex(gd5.x3p, gd5.y3p, gd5.z3p, ov[2]);
                    vArray[3] = new Vertex(gd5.x4p, gd5.y4p, gd5.z4p, ov[3]);
                }
            }
            condlines.put(gd5, Arrays.copyOf(vArray, 4));
            max = 4;
            break;
        default:
            max = 0;
            subVertex = false;
            break;
        }

        getManifestationLock().lock();
        if (subVertex) {
            if (!vertexCountInSubfile.containsKey(gdata)) {
                vertexCountInSubfile.put((GData1) gdata, 0);
            }
            int vertexCount = vertexCountInSubfile.get(gdata);
            for (int i = 0; i < max; i++) {
                vertexCount--;

                if (!vertexLinkedToPositionInFile.containsKey(vArray[i])) {
                    vertexLinkedToPositionInFile.put(vArray[i], Collections.newSetFromMap(new ThreadsafeHashMap<>()));
                }

                vertexLinkedToPositionInFile.get(vArray[i]).add(vdArray[i]);

                if (!vertexLinkedToSubfile.containsKey(vArray[i])) {
                    vertexLinkedToSubfile.put(vArray[i], Collections.newSetFromMap(new ThreadsafeHashMap<>()));
                }

                vertexLinkedToSubfile.get(vArray[i]).add((GData1) gdata);

                if (!lineLinkedToVertices.containsKey(gdata)) {
                    lineLinkedToVertices.put(gdata, new HashSet<>());
                }

                lineLinkedToVertices.get(gdata).add(new VertexInfo(vArray[i], vertexCount, originalData));
            }
            vertexCountInSubfile.put((GData1) gdata, vertexCount);
        } else {
            for (int i = 0; i < max; i++) {
                if (!vertexLinkedToPositionInFile.containsKey(vArray[i])) {
                    vertexLinkedToPositionInFile.put(vArray[i], Collections.newSetFromMap(new ThreadsafeHashMap<>()));
                }

                vertexLinkedToPositionInFile.get(vArray[i]).add(vdArray[i]);

                if (!lineLinkedToVertices.containsKey(gdata)) {
                    lineLinkedToVertices.put(gdata, Collections.newSetFromMap(new ThreadsafeHashMap<>()));
                }

                lineLinkedToVertices.get(gdata).add(new VertexInfo(vArray[i], vdArray[i].getPosition(), gdata));
            }
        }
        getManifestationLock().unlock();
    }

    public void addBackgroundPicture(String text, Vertex offset, BigDecimal angleA, BigDecimal angleB, BigDecimal angleC, Vertex scale, String texturePath) {
        setModifiedNoSync();
        if (!Project.getUnsavedFiles().contains(linkedDatFile)) {
            Project.addUnsavedFile(linkedDatFile);
            Editor3DWindow.getWindow().updateTreeUnsavedEntries();
        }
        GDataPNG pic = new GDataPNG(new GDataPNG(text, offset, angleA, angleB, angleC, scale, texturePath, View.DUMMY_REFERENCE).getString(offset, angleA, angleB, angleC, scale, texturePath), offset, angleA, angleB, angleC, scale, texturePath, View.DUMMY_REFERENCE);
        linkedDatFile.addToTailOrInsertAfterCursor(pic);
        setSelectedBgPicture(pic);
        setModifiedNoSync();
    }

    public void addLine(Vertex v1, Vertex v2) {
        addLine(v1, v2, true);
    }

    public void addDistance(Vertex v1, Vertex v2) {
        addLine(v1, v2, false);
    }

    private void addLine(Vertex v1, Vertex v2, boolean isRealLine) {
        if (v1 == null || v2 == null) return;
        linkedDatFile.setObjVertex1(null);
        linkedDatFile.setObjVertex2(null);
        Set<VertexManifestation> refs1 = vertexLinkedToPositionInFile.get(v1);
        Set<VertexManifestation> refs2 = vertexLinkedToPositionInFile.get(v2);
        if (refs1 != null && refs2 != null) {
            Set<GData> grefs1 = new HashSet<>();
            Set<GData> grefs2 = new HashSet<>();
            for (VertexManifestation vm : refs1) {
                grefs1.add(vm.getGdata());
            }
            for (VertexManifestation vm : refs2) {
                grefs2.add(vm.getGdata());
            }
            grefs1.retainAll(grefs2);
            for (GData gData : grefs1) {
                if (gData.type() == 2 && isRealLine)
                    return;
            }
        }
        setModifiedNoSync();
        if (!Project.getUnsavedFiles().contains(linkedDatFile)) {
            Project.addUnsavedFile(linkedDatFile);
            Editor3DWindow.getWindow().updateTreeUnsavedEntries();
        }
        GColour col = Editor3DWindow.getWindow().getLastUsedColour();
        if (col.getColourNumber() == 16 || col.getColourNumber() == 24) {
            // Will never return a 'null' colour!
            col = DatParser.validateColour(24, 0f, 0f, .0f, 0f);
        }
        linkedDatFile.addToTailOrInsertAfterCursor(new GData2(col.getColourNumber(), col.getR(), col.getG(), col.getB(), col.getA(), v1, v2, View.DUMMY_REFERENCE, linkedDatFile, isRealLine));
        linkedDatFile.setObjVertex1(v1);
        linkedDatFile.setObjVertex2(v2);
    }

    public void addTriangle(Vertex v1, Vertex v2, Vertex v3, Composite3D c3d) {
        addTriangle(v1, v2, v3, c3d, true);
    }

    public void addProtractor(Vertex v1, Vertex v2, Vertex v3, Composite3D c3d) {
        addTriangle(v1, v2, v3, c3d, false);
    }

    private void addTriangle(Vertex v1, Vertex v2, Vertex v3, Composite3D c3d, boolean isTriangle) {
        if (v1 == null || v2 == null || v3 == null) return;
        final boolean allowInvalidShapes = WorkbenchManager.getUserSettingState().isAllowInvalidShapes();
        if (v1.equals(v2) || v1.equals(v3) || v2.equals(v3))
            return;
        Set<VertexManifestation> refs1 = vertexLinkedToPositionInFile.get(v1);
        Set<VertexManifestation> refs2 = vertexLinkedToPositionInFile.get(v2);
        Set<VertexManifestation> refs3 = vertexLinkedToPositionInFile.get(v3);
        Set<GData> grefs1 = new HashSet<>();
        Set<GData> grefs2 = new HashSet<>();
        Set<GData> grefs3 = new HashSet<>();
        if (refs1 == null)
            refs1 = new HashSet<>();
        if (refs2 == null)
            refs2 = new HashSet<>();
        if (refs3 == null)
            refs3 = new HashSet<>();
        for (VertexManifestation vm : refs1) {
            grefs1.add(vm.getGdata());
        }
        for (VertexManifestation vm : refs2) {
            grefs2.add(vm.getGdata());
        }
        for (VertexManifestation vm : refs3) {
            grefs3.add(vm.getGdata());
        }
        grefs1.retainAll(grefs2);
        grefs1.retainAll(grefs3);
        for (GData gData : grefs1) {
            if (gData.type() == 3 && isTriangle)
                return;
        }

        // Resolving Collinearity
        double angle;
        Vector3f a = new Vector3f();
        Vector3f b = new Vector3f();
        Vector3f c = new Vector3f();

        {
            Vector3f v13f = new Vector3f(v1.x, v1.y, v1.z);
            Vector3f v23f = new Vector3f(v2.x, v2.y, v2.z);
            Vector3f v33f = new Vector3f(v3.x, v3.y, v3.z);
            Vector3f.sub(v23f, v13f, a);
            Vector3f.sub(v33f, v23f, b);
            Vector3f.sub(v33f, v13f, c);
        }

        angle = Vector3f.angle(a, c) * 180d / Math.PI;
        double sumAngle = angle;
        if ((angle < Threshold.COLLINEAR_ANGLE_MINIMUM || angle > Threshold.COLLINEAR_ANGLE_MAXIMUM) && !allowInvalidShapes) {
            linkedDatFile.setObjVertex3(null);
            return;
        }

        a.negate();
        angle = Vector3f.angle(a, b) * 180d / Math.PI;
        sumAngle = sumAngle + angle;
        if ((angle < Threshold.COLLINEAR_ANGLE_MINIMUM || angle > Threshold.COLLINEAR_ANGLE_MAXIMUM) && !allowInvalidShapes) {
            linkedDatFile.setObjVertex3(null);
            return;
        }

        angle = 180.0 - sumAngle;
        if ((angle < Threshold.COLLINEAR_ANGLE_MINIMUM || angle > Threshold.COLLINEAR_ANGLE_MAXIMUM) && !allowInvalidShapes) {
            linkedDatFile.setObjVertex3(null);
            return;
        }

        Vector4f n = new Vector4f();
        n.setW(1f);
        n.setX((v3.y - v1.y) * (v2.z - v1.z) - (v3.z - v1.z) * (v2.y - v1.y));
        n.setY((v3.z - v1.z) * (v2.x - v1.x) - (v3.x - v1.x) * (v2.z - v1.z));
        n.setZ((v3.x - v1.x) * (v2.y - v1.y) - (v3.y - v1.y) * (v2.x - v1.x));
        Matrix4f vport = c3d.getViewport();
        Matrix4f.transform(vport, n, n);
        Vector4f.sub(n, new Vector4f(vport.m03, vport.m13, vport.m23, 0f), n);
        if (isTriangle && n.z > 0f ^ Editor3DWindow.getWindow().hasBfcToggle()) {
            Vertex t = v1;
            v1 = v2;
            v2 = t;
        }
        setModifiedNoSync();
        if (!Project.getUnsavedFiles().contains(linkedDatFile)) {
            Project.addUnsavedFile(linkedDatFile);
            Editor3DWindow.getWindow().updateTreeUnsavedEntries();
        }
        GColour col = Editor3DWindow.getWindow().getLastUsedColour();
        linkedDatFile.addToTailOrInsertAfterCursor(new GData3(col.getColourNumber(), col.getR(), col.getG(), col.getB(), col.getA(), v1, v2, v3, View.DUMMY_REFERENCE, linkedDatFile, isTriangle));
        setModified(true, true);
    }

    public void addQuad(Vertex v1, Vertex v2, Vertex v3, Vertex v4, Composite3D c3d) {
        if (v1 == null || v2 == null || v3 == null || v4 == null) return;
        final boolean allowInvalidShapes = WorkbenchManager.getUserSettingState().isAllowInvalidShapes();
        {
            SortedSet<Vertex> dupl = new TreeSet<>();
            dupl.add(v1);
            dupl.add(v2);
            dupl.add(v3);
            dupl.add(v4);
            if (dupl.size() != 4)
                return;
        }
        Set<VertexManifestation> refs1 = vertexLinkedToPositionInFile.get(v1);
        Set<VertexManifestation> refs2 = vertexLinkedToPositionInFile.get(v2);
        Set<VertexManifestation> refs3 = vertexLinkedToPositionInFile.get(v3);
        Set<VertexManifestation> refs4 = vertexLinkedToPositionInFile.get(v4);
        Set<GData> grefs1 = new HashSet<>();
        Set<GData> grefs2 = new HashSet<>();
        Set<GData> grefs3 = new HashSet<>();
        Set<GData> grefs4 = new HashSet<>();
        if (refs1 == null)
            refs1 = new HashSet<>();
        if (refs2 == null)
            refs2 = new HashSet<>();
        if (refs3 == null)
            refs3 = new HashSet<>();
        if (refs4 == null)
            refs4 = new HashSet<>();
        for (VertexManifestation vm : refs1) {
            grefs1.add(vm.getGdata());
        }
        for (VertexManifestation vm : refs2) {
            grefs2.add(vm.getGdata());
        }
        for (VertexManifestation vm : refs3) {
            grefs3.add(vm.getGdata());
        }
        for (VertexManifestation vm : refs4) {
            grefs4.add(vm.getGdata());
        }
        grefs1.retainAll(grefs2);
        grefs1.retainAll(grefs3);
        grefs1.retainAll(grefs4);
        for (GData gData : grefs1) {
            if (gData.type() == 4)
                return;
        }

        // Resolving Hourglass, Coplanarity and Collinearity
        Vector3f v13f = new Vector3f(v1.x, v1.y, v1.z);
        Vector3f v23f = new Vector3f(v2.x, v2.y, v2.z);
        Vector3f v33f = new Vector3f(v3.x, v3.y, v3.z);
        Vector3f v43f = new Vector3f(v4.x, v4.y, v4.z);
        int cnc = 0;
        Vector3f[] lineVectors = new Vector3f[4];
        float[] normalDirections = new float[4];
        Vector3f[] normals = new Vector3f[4];
        boolean triedHGfix = false;

        lineVectors[0] = Vector3f.sub(v23f, v13f, null);
        lineVectors[1] = Vector3f.sub(v33f, v23f, null);
        lineVectors[2] = Vector3f.sub(v43f, v33f, null);
        lineVectors[3] = Vector3f.sub(v13f, v43f, null);
        normals[0] = Vector3f.cross(lineVectors[0], lineVectors[1], null);
        normals[1] = Vector3f.cross(lineVectors[1], lineVectors[2], null);
        normals[2] = Vector3f.cross(lineVectors[2], lineVectors[3], null);
        normals[3] = Vector3f.cross(lineVectors[3], lineVectors[0], null);

        Vector3f normal = new Vector3f();

        for (int i = 0; i < 4; i++) {
            normalDirections[i] = MathHelper.directionOfVectors(normals[0], normals[i]);
            if (normalDirections[i] < 0)
                cnc++;
            normal = Vector3f.add(normals[i], normal, null);
        }

        if (cnc == 2) {
            // Hourglass
            triedHGfix = true;
            Vertex t;
            t = v1;
            v1 = v2;
            v2 = t;
            v13f = new Vector3f(v1.x, v1.y, v1.z);
            v23f = new Vector3f(v2.x, v2.y, v2.z);
        } else if (cnc == 1 || cnc == 3) {
            // Concave
            // Backface Culling
            Vector4f n = new Vector4f();
            n.setW(1f);
            n.setX((v3.y - v1.y) * (v2.z - v1.z) - (v3.z - v1.z) * (v2.y - v1.y));
            n.setY((v3.z - v1.z) * (v2.x - v1.x) - (v3.x - v1.x) * (v2.z - v1.z));
            n.setZ((v3.x - v1.x) * (v2.y - v1.y) - (v3.y - v1.y) * (v2.x - v1.x));
            Matrix4f vport = c3d.getViewport();
            Matrix4f.transform(vport, n, n);
            Vector4f.sub(n, new Vector4f(vport.m03, vport.m13, vport.m23, 0f), n);
            if (n.z > 0f ^ Editor3DWindow.getWindow().hasBfcToggle()) {
                Vertex t = v1;
                v1 = v3;
                v3 = t;
            }
            setModifiedNoSync();
            if (!Project.getUnsavedFiles().contains(linkedDatFile)) {
                Project.addUnsavedFile(linkedDatFile);
                Editor3DWindow.getWindow().updateTreeUnsavedEntries();
            }
            GColour col = Editor3DWindow.getWindow().getLastUsedColour();
            linkedDatFile.addToTailOrInsertAfterCursor(new GData3(col.getColourNumber(), col.getR(), col.getG(), col.getB(), col.getA(), v2, v3, v4, View.DUMMY_REFERENCE, linkedDatFile, true));
            linkedDatFile.addToTailOrInsertAfterCursor(new GData3(col.getColourNumber(), col.getR(), col.getG(), col.getB(), col.getA(), v4, v1, v2, View.DUMMY_REFERENCE, linkedDatFile, true));
            setModified(true, true);
            return;
        }

        if (triedHGfix) {
            normal = new Vector3f();
            cnc = 0;
            lineVectors[0] = Vector3f.sub(v23f, v13f, null);
            lineVectors[1] = Vector3f.sub(v33f, v23f, null);
            lineVectors[2] = Vector3f.sub(v43f, v33f, null);
            lineVectors[3] = Vector3f.sub(v13f, v43f, null);
            normals[0] = Vector3f.cross(lineVectors[0], lineVectors[1], null);
            normals[1] = Vector3f.cross(lineVectors[1], lineVectors[2], null);
            normals[2] = Vector3f.cross(lineVectors[2], lineVectors[3], null);
            normals[3] = Vector3f.cross(lineVectors[3], lineVectors[0], null);

            for (int i = 0; i < 4; i++) {
                normalDirections[i] = MathHelper.directionOfVectors(normals[0], normals[i]);
                if (normalDirections[i] < 0)
                    cnc++;
            }

            if (cnc == 2) {
                // Hourglass II (retry)
                Vertex t;
                t = v1;
                v1 = v2;
                v2 = t;
                t = v2;
                v2 = v3;
                v3 = t;
                v13f = new Vector3f(v1.x, v1.y, v1.z);
                v23f = new Vector3f(v2.x, v2.y, v2.z);
                v33f = new Vector3f(v3.x, v3.y, v3.z);
            } else if ((cnc == 1 || cnc == 3) && !allowInvalidShapes) {
                // Concave
                linkedDatFile.setObjVertex4(null);
                return;
            }

            lineVectors[0] = Vector3f.sub(v23f, v13f, null);
            lineVectors[1] = Vector3f.sub(v33f, v23f, null);
            lineVectors[2] = Vector3f.sub(v43f, v33f, null);
            lineVectors[3] = Vector3f.sub(v13f, v43f, null);
            normals[0] = Vector3f.cross(lineVectors[0], lineVectors[1], null);
            normals[1] = Vector3f.cross(lineVectors[1], lineVectors[2], null);
            normals[2] = Vector3f.cross(lineVectors[2], lineVectors[3], null);
            normals[3] = Vector3f.cross(lineVectors[3], lineVectors[0], null);

            for (int i = 0; i < 4; i++) {
                normalDirections[i] = MathHelper.directionOfVectors(normals[0], normals[i]);
                if (normalDirections[i] < 0)
                    cnc++;
                normal = Vector3f.add(normals[i], normal, null);
            }
        }

        double angle2 = Vector3f.angle(normals[0], normals[2]) * 180d / Math.PI;
        double angle;

        // Collinearity
        Vector3f a = new Vector3f();
        Vector3f b = new Vector3f();
        Vector3f c = new Vector3f();
        Vector3f d = new Vector3f();

        Vector3f.sub(v23f, v13f, a);
        Vector3f.sub(v23f, v33f, b);
        Vector3f.sub(v43f, v33f, c);
        Vector3f.sub(v43f, v13f, d);

        angle = Vector3f.angle(a, d) * 180d / Math.PI;
        double sumAngle = angle;
        if ((angle < Threshold.COLLINEAR_ANGLE_MINIMUM || angle > Threshold.COLLINEAR_ANGLE_MAXIMUM) && !allowInvalidShapes) {
            linkedDatFile.setObjVertex3(null);
            linkedDatFile.setObjVertex4(null);
            return;
        }

        angle = Vector3f.angle(b, c) * 180d / Math.PI;
        sumAngle = sumAngle + angle;
        if ((angle < Threshold.COLLINEAR_ANGLE_MINIMUM || angle > Threshold.COLLINEAR_ANGLE_MAXIMUM) && !allowInvalidShapes) {
            linkedDatFile.setObjVertex3(null);
            linkedDatFile.setObjVertex4(null);
            return;
        }

        a.negate();
        b.negate();
        angle = Vector3f.angle(a, b) * 180d / Math.PI;
        sumAngle = sumAngle + angle;
        if ((angle < Threshold.COLLINEAR_ANGLE_MINIMUM || angle > Threshold.COLLINEAR_ANGLE_MAXIMUM) && !allowInvalidShapes) {
            linkedDatFile.setObjVertex3(null);
            linkedDatFile.setObjVertex4(null);
            return;
        }

        angle = 360.0 - sumAngle;
        if ((angle < Threshold.COLLINEAR_ANGLE_MINIMUM || angle > Threshold.COLLINEAR_ANGLE_MAXIMUM) && !allowInvalidShapes) {
            linkedDatFile.setObjVertex3(null);
            linkedDatFile.setObjVertex4(null);
            return;
        }

        // Coplanarity
        if (angle2 > Threshold.coplanarityAngleError && !allowInvalidShapes) {
            linkedDatFile.setObjVertex4(null);
            return;
        }

        // Backface Culling
        Vector4f n = new Vector4f();
        n.setW(1f);
        n.setX((v3.y - v1.y) * (v2.z - v1.z) - (v3.z - v1.z) * (v2.y - v1.y));
        n.setY((v3.z - v1.z) * (v2.x - v1.x) - (v3.x - v1.x) * (v2.z - v1.z));
        n.setZ((v3.x - v1.x) * (v2.y - v1.y) - (v3.y - v1.y) * (v2.x - v1.x));
        Matrix4f vport = c3d.getViewport();
        Matrix4f.transform(vport, n, n);
        Vector4f.sub(n, new Vector4f(vport.m03, vport.m13, vport.m23, 0f), n);
        if (n.z > 0f ^ Editor3DWindow.getWindow().hasBfcToggle()) {
            Vertex t = v1;
            v1 = v3;
            v3 = t;
        }

        setModifiedNoSync();
        if (!Project.getUnsavedFiles().contains(linkedDatFile)) {
            Project.addUnsavedFile(linkedDatFile);
            Editor3DWindow.getWindow().updateTreeUnsavedEntries();
        }
        GColour col = Editor3DWindow.getWindow().getLastUsedColour();
        linkedDatFile.addToTailOrInsertAfterCursor(new GData4(col.getColourNumber(), col.getR(), col.getG(), col.getB(), col.getA(), v1, v2, v3, v4, View.DUMMY_REFERENCE, linkedDatFile));
        setModified(true, true);
    }

    public void addCondline(Vertex v1, Vertex v2, Vertex v3, Vertex v4) {
        if (v1 == null || v2 == null || v3 == null || v4 == null) return;
        {
            SortedSet<Vertex> dupl = new TreeSet<>();
            dupl.add(v1);
            dupl.add(v2);
            dupl.add(v3);
            dupl.add(v4);
            if (dupl.size() != 4)
                return;
        }
        Set<VertexManifestation> refs1 = vertexLinkedToPositionInFile.get(v1);
        Set<VertexManifestation> refs2 = vertexLinkedToPositionInFile.get(v2);
        Set<GData> grefs1 = new HashSet<>();
        Set<GData> grefs2 = new HashSet<>();
        if (refs1 == null)
            refs1 = new HashSet<>();
        if (refs2 == null)
            refs2 = new HashSet<>();
        for (VertexManifestation vm : refs1) {
            grefs1.add(vm.getGdata());
        }
        for (VertexManifestation vm : refs2) {
            grefs2.add(vm.getGdata());
        }
        grefs1.retainAll(grefs2);
        for (GData gData : grefs1) {
            if (gData.type() == 5) {
                GData5 cline = (GData5) gData;
                Vertex vc1 = new Vertex(cline.x1, cline.y1, cline.z1);
                Vertex vc2 = new Vertex(cline.x2, cline.y2, cline.z2);
                if (vc1.equals(v1) && vc2.equals(v2) || vc2.equals(v1) && vc1.equals(v2)) {
                    return;
                }
            }
        }
        setModifiedNoSync();
        if (!Project.getUnsavedFiles().contains(linkedDatFile)) {
            Project.addUnsavedFile(linkedDatFile);
            Editor3DWindow.getWindow().updateTreeUnsavedEntries();
        }
        GColour col = Editor3DWindow.getWindow().getLastUsedColour();
        if (col.getColourNumber() == 16 || col.getColourNumber() == 24) {
            // Will never return a 'null' colour!
            col = DatParser.validateColour(24, 0f, 0f, 0f, 0f);
        }

        linkedDatFile.addToTailOrInsertAfterCursor(new GData5(col.getColourNumber(), col.getR(), col.getG(), col.getB(), col.getA(), v1, v2, v3, v4, View.DUMMY_REFERENCE, linkedDatFile));
    }

    public void addParsedLine(String lineToParse) {
        if (linkedDatFile.isReadOnly())
            return;
        clearSelection();

        final GColour col16 = View.getLDConfigColour(16);
        Set<String> alreadyParsed = new HashSet<>();
        alreadyParsed.add(linkedDatFile.getShortName());
        List<ParsingResult> result = DatParser.parseLine(lineToParse, -1, 0, col16.getR(), col16.getG(), col16.getB(), 1.0f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile, false, alreadyParsed);
        GData pasted = result.get(0).getGraphicalData();
        if (pasted == null) {
            pasted = new GData0(lineToParse, View.DUMMY_REFERENCE);
        }

        setModifiedNoSync();
        if (!Project.getUnsavedFiles().contains(linkedDatFile)) {
            Project.addUnsavedFile(linkedDatFile);
            Editor3DWindow.getWindow().updateTreeUnsavedEntries();
        }
        linkedDatFile.addToTailOrInsertAfterCursor(pasted);
        setModified(true, true);
    }
}
