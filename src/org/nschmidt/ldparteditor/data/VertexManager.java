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
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.csg.CSG;
import org.nschmidt.ldparteditor.composite.Composite3D;
import org.nschmidt.ldparteditor.composite.ScalableComposite;
import org.nschmidt.ldparteditor.enumtype.Colour;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.enumtype.WorkingMode;
import org.nschmidt.ldparteditor.helper.Manipulator;
import org.nschmidt.ldparteditor.helper.composite3d.GuiStatusManager;
import org.nschmidt.ldparteditor.helper.composite3d.PerspectiveCalculator;
import org.nschmidt.ldparteditor.helper.math.ThreadsafeSortedMap;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.AddToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.MiscToggleToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.TransformationModeToolItem;

/**
 * All actions are THREAD safe!! 1. Displays all vertices <br>
 * 2. Provides functions to <br>
 * 2.1 Manipulate.. <br>
 * 2.2 Create.. <br>
 * 2.3 Delete vertices
 */
public final class VertexManager extends VM99Clipboard {

    private volatile AtomicBoolean calculateCondlineControlPoints = new AtomicBoolean(true);

    VertexManager(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    final synchronized void drawGL20(final Composite3D c3d) {
        if (!linkedDatFile.isDrawSelection()) return;

        Matrix4f vm = c3d.getViewport();
        Matrix4f ivm = c3d.getViewportInverse();
        Set<Vertex> tmpHiddenVertices = c3d.getTmpHiddenVertices();

        Manipulator manipulator = c3d.getManipulator();
        FloatBuffer matrix = manipulator.getTempTransformation();
        FloatBuffer matrixInv = manipulator.getTempTransformationInv();
        final boolean modifiedManipulator = manipulator.isModified();

        if (calculateCondlineControlPoints.compareAndSet(true, false)) {
            CompletableFuture.runAsync( () -> {
                final Set<Vertex> tmpHiddenVertices2 = Collections.newSetFromMap(new ThreadsafeSortedMap<>());
                tmpHiddenVertices2.addAll(hiddenVertices);
                if (c3d.isShowingCondlineControlPoints() || c3d.getRenderMode() == 6 || AddToolItem.isAddingCondlines()) {
                    if (!tmpHiddenVertices.isEmpty()) {
                        boolean pureControlPoint;
                        for (Map.Entry<Vertex, Set<VertexManifestation>> entry : vertexLinkedToPositionInFile.threadSafeEntrySet()) {
                            final Vertex vertex = entry.getKey();
                            Set<VertexManifestation> manis = entry.getValue();
                            if (manis != null) {
                                pureControlPoint = true;
                                for (VertexManifestation m : manis) {
                                    if (m.position() < 2 || m.gdata().type() != 5) {
                                        pureControlPoint = false;
                                        break;
                                    }
                                }
                                if (pureControlPoint) {
                                    tmpHiddenVertices2.remove(vertex);
                                }
                            }
                        }
                    }
                } else {
                    boolean pureControlPoint;
                    for (Map.Entry<Vertex, Set<VertexManifestation>> entry : vertexLinkedToPositionInFile.threadSafeEntrySet()) {
                        final Vertex vertex = entry.getKey();
                        Set<VertexManifestation> manis = entry.getValue();
                        if (manis != null) {
                            pureControlPoint = true;
                            for (VertexManifestation m : manis) {
                                if (m.position() < 2  || m.gdata().type() != 5) {
                                    pureControlPoint = false;
                                    break;
                                }
                            }
                            if (pureControlPoint) {
                                tmpHiddenVertices2.add(vertex);
                            }
                        }
                    }
                }
                for (Iterator<Vertex> iterator = tmpHiddenVertices.iterator(); iterator
                        .hasNext();) {
                    Vertex vertex = iterator.next();
                    if (!tmpHiddenVertices2.contains(vertex)) {
                        iterator.remove();
                    }
                }
                tmpHiddenVertices.addAll(tmpHiddenVertices2);
                calculateCondlineControlPoints.set(true);
            });
        }


        if (c3d.isShowingVertices()) {

            Vector4f tr = new Vector4f(vm.m30, vm.m31, vm.m32 + 330f * c3d.getZoom(), 1f);
            Matrix4f.transform(ivm, tr, tr);
            GL11.glDisable(GL11.GL_LIGHTING);
            if (c3d.isShowingHiddenVertices()) {
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glColor3f(Colour.vertexColourR * .5f, Colour.vertexColourG * .5f, Colour.vertexColourB * .5f);
                GL11.glBegin(GL11.GL_POINTS);
                for (Vertex vertex : vertexLinkedToPositionInFile.keySet()) {
                    if (tmpHiddenVertices.contains(vertex))
                        continue;
                    GL11.glVertex3f(vertex.x, vertex.y, vertex.z);
                }
                GL11.glEnd();
                GL11.glEnable(GL11.GL_DEPTH_TEST);
            }
            GL11.glColor3f(Colour.vertexColourR, Colour.vertexColourG, Colour.vertexColourB);
            GL11.glBegin(GL11.GL_POINTS);
            for (Vertex vertex : vertexLinkedToPositionInFile.keySet()) {
                if (tmpHiddenVertices.contains(vertex))
                    continue;
                GL11.glVertex3f(vertex.x + tr.x, vertex.y + tr.y, vertex.z + tr.z);
            }
            GL11.glEnd();
        }
        if (!modifiedManipulator)
            GL11.glDisable(GL11.GL_DEPTH_TEST);

        GL11.glPushMatrix();
        GL11.glMultMatrixf(matrix);

        if (modifiedManipulator) {
            Set<GData> alreadyMoved = new HashSet<>();

            SortedSet<Vertex> allVertices = new TreeSet<>(selectedVertices);
            GraphicalDataTools.setLineWidth(2f);
            if (c3d.isShowingVertices()) {
                GL11.glBegin(GL11.GL_POINTS);
                GL11.glColor3f(Colour.vertexSelectedTmpColourR, Colour.vertexSelectedTmpColourG, Colour.vertexSelectedTmpColourB);
                for (Vertex vertex : selectedVertices) {
                    GL11.glVertex3f(vertex.x, vertex.y, vertex.z);
                }
                GL11.glColor3f(Colour.vertexColourR, Colour.vertexColourG, Colour.vertexColourB);
                GL11.glEnd();
            }

            if (c3d.isLightOn())
                GL11.glEnable(GL11.GL_LIGHTING);

            Vertex[] verts;
            float nx;
            float ny;
            float nz;
            for (GData2 g2 : selectedLines) {
                if (g2.visible) {
                    dataToHide.add(g2);
                    g2.hide();
                }
                if ((verts = lines.get(g2)) != null) {
                    allVertices.add(verts[0]);
                    allVertices.add(verts[1]);
                }
            }
            for (GData3 g3 : selectedTriangles) {
                if (g3.visible) {
                    dataToHide.add(g3);
                    g3.hide();
                }
                if ((verts = triangles.get(g3)) != null) {
                    allVertices.add(verts[0]);
                    allVertices.add(verts[1]);
                    allVertices.add(verts[2]);
                }
            }
            for (GData4 g4 : selectedQuads) {
                if (g4.visible) {
                    dataToHide.add(g4);
                    g4.hide();
                }
                if ((verts = quads.get(g4)) != null) {
                    allVertices.add(verts[0]);
                    allVertices.add(verts[1]);
                    allVertices.add(verts[2]);
                    allVertices.add(verts[3]);
                }
            }
            for (GData5 g5 : selectedCondlines) {
                if (g5.visible) {
                    dataToHide.add(g5);
                    g5.hide();
                }
                if ((verts = condlines.get(g5)) != null) {
                    allVertices.add(verts[0]);
                    allVertices.add(verts[1]);
                }
            }
            for (Vertex vertex : allVertices) {
                Set<VertexManifestation> vms = vertexLinkedToPositionInFile.get(vertex);
                if (vms == null) continue;
                for (VertexManifestation m : vms) {
                    GData gd = m.gdata();
                    if (alreadyMoved.contains(gd)) continue;
                    alreadyMoved.add(gd);
                    if (selectedData.contains(gd)) {
                        if (gd.visible) {
                            dataToHide.add(gd);
                            gd.hide();
                        }
                        switch (gd.type()) {
                        case 3:
                            GData3 gd3 = (GData3) gd;
                            Vertex[] triverts = triangles.get(gd3);
                            nx = (triverts[2].y - triverts[0].y) * (triverts[1].z - triverts[0].z) - (triverts[2].z - triverts[0].z) * (triverts[1].y - triverts[0].y);
                            ny = (triverts[2].z - triverts[0].z) * (triverts[1].x - triverts[0].x) - (triverts[2].x - triverts[0].x) * (triverts[1].z - triverts[0].z);
                            nz = (triverts[2].x - triverts[0].x) * (triverts[1].y - triverts[0].y) - (triverts[2].y - triverts[0].y) * (triverts[1].x - triverts[0].x);
                            if (!gd3.isTriangle) {
                                Vertex[] triverts2 = triverts.clone();
                                for (int i = 0; i < 3; i++) {
                                    Vertex v3 = triverts[i];
                                    if (!allVertices.contains(v3)) {
                                        Vector4f res = manipulator.getUntransformed(v3.x, v3.y, v3.z);
                                        triverts[i] = new Vertex(res.x, res.y, res.z);
                                    }
                                }
                                GL11.glMultMatrixf(matrixInv);
                                new GData3(triverts2[0], triverts2[1], triverts2[2], null, new GColour(16, gd3.r, gd3.g, gd3.b, 0f), false).drawProtractorGL20(true, c3d, triverts[0].xp, triverts[0].yp, triverts[0].zp, triverts[1].xp, triverts[1].yp, triverts[1].zp, triverts[2].xp, triverts[2].yp, triverts[2].zp);
                                GL11.glMultMatrixf(matrix);
                                GL11.glBegin(GL11.GL_LINES);
                                GL11.glColor3f(Colour.vertexSelectedTmpColourR, Colour.vertexSelectedTmpColourG, Colour.vertexSelectedTmpColourB);
                                GL11.glVertex3f(triverts[0].x, triverts[0].y, triverts[0].z);
                                GL11.glVertex3f(triverts[1].x, triverts[1].y, triverts[1].z);
                                GL11.glVertex3f(triverts[2].x, triverts[2].y, triverts[2].z);
                                GL11.glVertex3f(triverts[1].x, triverts[1].y, triverts[1].z);
                                GL11.glColor3f(Colour.vertexColourR, Colour.vertexColourG, Colour.vertexColourB);
                                GL11.glEnd();
                            } else {
                                GL11.glBegin(GL11.GL_TRIANGLES);
                                GL11.glColor3f(gd3.r, gd3.g, gd3.b);
                                GL11.glNormal3f(nx, ny, nz);
                                GL11.glVertex3f(triverts[0].x, triverts[0].y, triverts[0].z);
                                GL11.glVertex3f(triverts[1].x, triverts[1].y, triverts[1].z);
                                GL11.glVertex3f(triverts[2].x, triverts[2].y, triverts[2].z);
                                GL11.glNormal3f(-nx, -ny, -nz);
                                GL11.glVertex3f(triverts[0].x, triverts[0].y, triverts[0].z);
                                GL11.glVertex3f(triverts[2].x, triverts[2].y, triverts[2].z);
                                GL11.glVertex3f(triverts[1].x, triverts[1].y, triverts[1].z);
                                GL11.glEnd();
                            }
                            break;
                        case 4:
                            GData4 gd4 = (GData4) gd;
                            Vertex[] quadverts = quads.get(gd4);
                            final Vector3f[] normals = new Vector3f[] { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
                            {
                                final Vector3f[] lineVectors = new Vector3f[] { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
                                Vector3f.sub(new Vector3f(quadverts[1].x, quadverts[1].y, quadverts[1].z), new Vector3f(quadverts[0].x, quadverts[0].y, quadverts[0].z), lineVectors[0]);
                                Vector3f.sub(new Vector3f(quadverts[2].x, quadverts[2].y, quadverts[2].z), new Vector3f(quadverts[1].x, quadverts[1].y, quadverts[1].z), lineVectors[1]);
                                Vector3f.sub(new Vector3f(quadverts[3].x, quadverts[3].y, quadverts[3].z), new Vector3f(quadverts[2].x, quadverts[2].y, quadverts[2].z), lineVectors[2]);
                                Vector3f.sub(new Vector3f(quadverts[0].x, quadverts[0].y, quadverts[0].z), new Vector3f(quadverts[3].x, quadverts[3].y, quadverts[3].z), lineVectors[3]);
                                Vector3f.cross(lineVectors[0], lineVectors[1], normals[0]);
                                Vector3f.cross(lineVectors[1], lineVectors[2], normals[1]);
                                Vector3f.cross(lineVectors[2], lineVectors[3], normals[2]);
                                Vector3f.cross(lineVectors[3], lineVectors[0], normals[3]);
                            }
                            Vector3f normal = new Vector3f();
                            for (int i = 0; i < 4; i++) {
                                Vector3f.add(normals[i], normal, normal);
                            }
                            nx = -normal.x;
                            ny = -normal.y;
                            nz = -normal.z;
                            GL11.glBegin(GL11.GL_QUADS);
                            GL11.glColor3f(gd4.r, gd4.g, gd4.b);
                            GL11.glNormal3f(nx, ny, nz);
                            GL11.glVertex3f(quadverts[0].x, quadverts[0].y, quadverts[0].z);
                            GL11.glVertex3f(quadverts[1].x, quadverts[1].y, quadverts[1].z);
                            GL11.glVertex3f(quadverts[2].x, quadverts[2].y, quadverts[2].z);
                            GL11.glVertex3f(quadverts[3].x, quadverts[3].y, quadverts[3].z);
                            GL11.glNormal3f(-nx, -ny, -nz);
                            GL11.glVertex3f(quadverts[0].x, quadverts[0].y, quadverts[0].z);
                            GL11.glVertex3f(quadverts[3].x, quadverts[3].y, quadverts[3].z);
                            GL11.glVertex3f(quadverts[2].x, quadverts[2].y, quadverts[2].z);
                            GL11.glVertex3f(quadverts[1].x, quadverts[1].y, quadverts[1].z);
                            GL11.glEnd();
                            break;
                        default:
                            break;
                        }
                    } else if (lineLinkedToVertices.containsKey(gd) && MiscToggleToolItem.isMovingAdjacentData()) {
                        if (gd.visible) {
                            dataToHide.add(gd);
                            gd.hide();
                        }
                        switch (gd.type()) {
                        case 2:
                            final GData2 gd2 = (GData2) gd;
                            Vertex[] lineverts2 = lines.get(gd);
                            Vertex[] lineverts = lineverts2.clone();
                            for (int i = 0; i < 2; i++) {
                                Vertex v2 = lineverts[i];
                                if (!v2.equals(vertex)) {
                                    Vector4f res = manipulator.getUntransformed(v2.x, v2.y, v2.z);
                                    if (gd2.isLine) {
                                        lineverts[i] = new Vertex(res.x, res.y, res.z, true);
                                    } else {
                                        lineverts[i] = new Vertex(res.x, res.y, res.z);
                                    }
                                }
                            }
                            if (!gd2.isLine) {
                                GL11.glMultMatrixf(matrixInv);
                                new GData2(lineverts2[0], lineverts2[1], null, new GColour(16, Colour.vertexSelectedTmpColourR, Colour.vertexSelectedTmpColourG, Colour.vertexSelectedTmpColourB, 0f), false).drawDistanceGL20(c3d, lineverts[0].xp, lineverts[0].yp, lineverts[0].zp, lineverts[1].xp, lineverts[1].yp, lineverts[1].zp);
                                GL11.glBegin(GL11.GL_LINES);
                                GL11.glColor3f(Colour.vertexSelectedTmpColourR, Colour.vertexSelectedTmpColourG, Colour.vertexSelectedTmpColourB);
                                GL11.glVertex3f(lineverts2[0].x, lineverts2[0].y, lineverts2[0].z);
                                GL11.glVertex3f(lineverts2[1].x, lineverts2[1].y, lineverts2[1].z);
                                GL11.glColor3f(Colour.vertexColourR, Colour.vertexColourG, Colour.vertexColourB);
                                GL11.glEnd();
                                GL11.glMultMatrixf(matrix);
                            }
                            GL11.glBegin(GL11.GL_LINES);
                            GL11.glColor3f(Colour.vertexSelectedTmpColourR, Colour.vertexSelectedTmpColourG, Colour.vertexSelectedTmpColourB);
                            GL11.glVertex3f(lineverts[0].x, lineverts[0].y, lineverts[0].z);
                            GL11.glVertex3f(lineverts[1].x, lineverts[1].y, lineverts[1].z);
                            GL11.glColor3f(Colour.vertexColourR, Colour.vertexColourG, Colour.vertexColourB);
                            GL11.glEnd();
                            break;
                        case 3:
                            GData3 gd3 = (GData3) gd;
                            Vertex[] triverts2 = triangles.get(gd3);
                            Vertex[] triverts = triverts2.clone();
                            for (int i = 0; i < 3; i++) {
                                Vertex v3 = triverts[i];
                                if (!allVertices.contains(v3)) {
                                    Vector4f res = manipulator.getUntransformed(v3.x, v3.y, v3.z);
                                    if (gd3.isTriangle) {
                                        triverts[i] = new Vertex(res.x, res.y, res.z, true);
                                    } else {
                                        triverts[i] = new Vertex(res.x, res.y, res.z);
                                    }
                                }
                            }
                            nx = (triverts[2].y - triverts[0].y) * (triverts[1].z - triverts[0].z) - (triverts[2].z - triverts[0].z) * (triverts[1].y - triverts[0].y);
                            ny = (triverts[2].z - triverts[0].z) * (triverts[1].x - triverts[0].x) - (triverts[2].x - triverts[0].x) * (triverts[1].z - triverts[0].z);
                            nz = (triverts[2].x - triverts[0].x) * (triverts[1].y - triverts[0].y) - (triverts[2].y - triverts[0].y) * (triverts[1].x - triverts[0].x);
                            if (!gd3.isTriangle) {
                                GL11.glMultMatrixf(matrixInv);
                                new GData3(triverts2[0], triverts2[1], triverts2[2], null, new GColour(16, gd3.r, gd3.g, gd3.b, 0f), false).drawProtractorGL20(true, c3d, triverts[0].xp, triverts[0].yp, triverts[0].zp, triverts[1].xp, triverts[1].yp, triverts[1].zp, triverts[2].xp, triverts[2].yp, triverts[2].zp);
                                GL11.glMultMatrixf(matrix);
                                GL11.glBegin(GL11.GL_LINES);
                                GL11.glColor3f(Colour.vertexSelectedTmpColourR, Colour.vertexSelectedTmpColourG, Colour.vertexSelectedTmpColourB);
                                GL11.glVertex3f(triverts[0].x, triverts[0].y, triverts[0].z);
                                GL11.glVertex3f(triverts[1].x, triverts[1].y, triverts[1].z);
                                GL11.glVertex3f(triverts[2].x, triverts[2].y, triverts[2].z);
                                GL11.glVertex3f(triverts[0].x, triverts[0].y, triverts[0].z);
                                GL11.glColor3f(Colour.vertexColourR, Colour.vertexColourG, Colour.vertexColourB);
                                GL11.glEnd();
                            } else {
                                GL11.glBegin(GL11.GL_TRIANGLES);
                                GL11.glColor3f(gd3.r, gd3.g, gd3.b);
                                GL11.glNormal3f(nx, ny, nz);
                                GL11.glVertex3f(triverts[0].x, triverts[0].y, triverts[0].z);
                                GL11.glVertex3f(triverts[1].x, triverts[1].y, triverts[1].z);
                                GL11.glVertex3f(triverts[2].x, triverts[2].y, triverts[2].z);
                                GL11.glNormal3f(-nx, -ny, -nz);
                                GL11.glVertex3f(triverts[0].x, triverts[0].y, triverts[0].z);
                                GL11.glVertex3f(triverts[2].x, triverts[2].y, triverts[2].z);
                                GL11.glVertex3f(triverts[1].x, triverts[1].y, triverts[1].z);
                                GL11.glEnd();
                                GL11.glBegin(GL11.GL_LINES);
                                GL11.glColor3f(Colour.vertexSelectedTmpColourR, Colour.vertexSelectedTmpColourG, Colour.vertexSelectedTmpColourB);
                                GL11.glVertex3f(triverts[0].x, triverts[0].y, triverts[0].z);
                                GL11.glVertex3f(triverts[1].x, triverts[1].y, triverts[1].z);

                                GL11.glVertex3f(triverts[1].x, triverts[1].y, triverts[1].z);
                                GL11.glVertex3f(triverts[2].x, triverts[2].y, triverts[2].z);

                                GL11.glVertex3f(triverts[2].x, triverts[2].y, triverts[2].z);
                                GL11.glVertex3f(triverts[0].x, triverts[0].y, triverts[0].z);
                                GL11.glColor3f(Colour.vertexColourR, Colour.vertexColourG, Colour.vertexColourB);
                                GL11.glEnd();
                            }
                            break;
                        case 4:
                            GData4 gd4 = (GData4) gd;
                            Vertex[] quadverts2 = quads.get(gd4);
                            Vertex[] quadverts = quadverts2.clone();
                            for (int i = 0; i < 4; i++) {
                                Vertex v4 = quadverts[i];
                                if (!allVertices.contains(v4)) {
                                    Vector4f res = manipulator.getUntransformed(v4.x, v4.y, v4.z);
                                    quadverts[i] = new Vertex(res.x, res.y, res.z, true);
                                }
                            }
                            final Vector3f[] normals = new Vector3f[] { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
                            {
                                final Vector3f[] lineVectors = new Vector3f[] { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
                                Vector3f.sub(new Vector3f(quadverts[1].x, quadverts[1].y, quadverts[1].z), new Vector3f(quadverts[0].x, quadverts[0].y, quadverts[0].z), lineVectors[0]);
                                Vector3f.sub(new Vector3f(quadverts[2].x, quadverts[2].y, quadverts[2].z), new Vector3f(quadverts[1].x, quadverts[1].y, quadverts[1].z), lineVectors[1]);
                                Vector3f.sub(new Vector3f(quadverts[3].x, quadverts[3].y, quadverts[3].z), new Vector3f(quadverts[2].x, quadverts[2].y, quadverts[2].z), lineVectors[2]);
                                Vector3f.sub(new Vector3f(quadverts[0].x, quadverts[0].y, quadverts[0].z), new Vector3f(quadverts[3].x, quadverts[3].y, quadverts[3].z), lineVectors[3]);
                                Vector3f.cross(lineVectors[0], lineVectors[1], normals[0]);
                                Vector3f.cross(lineVectors[1], lineVectors[2], normals[1]);
                                Vector3f.cross(lineVectors[2], lineVectors[3], normals[2]);
                                Vector3f.cross(lineVectors[3], lineVectors[0], normals[3]);
                            }
                            Vector3f normal = new Vector3f();
                            for (int i = 0; i < 4; i++) {
                                Vector3f.add(normals[i], normal, normal);
                            }
                            nx = -normal.x;
                            ny = -normal.y;
                            nz = -normal.z;
                            GL11.glBegin(GL11.GL_QUADS);
                            GL11.glColor3f(gd4.r, gd4.g, gd4.b);
                            GL11.glNormal3f(nx, ny, nz);
                            GL11.glVertex3f(quadverts[0].x, quadverts[0].y, quadverts[0].z);
                            GL11.glVertex3f(quadverts[1].x, quadverts[1].y, quadverts[1].z);
                            GL11.glVertex3f(quadverts[2].x, quadverts[2].y, quadverts[2].z);
                            GL11.glVertex3f(quadverts[3].x, quadverts[3].y, quadverts[3].z);
                            GL11.glNormal3f(-nx, -ny, -nz);
                            GL11.glVertex3f(quadverts[0].x, quadverts[0].y, quadverts[0].z);
                            GL11.glVertex3f(quadverts[3].x, quadverts[3].y, quadverts[3].z);
                            GL11.glVertex3f(quadverts[2].x, quadverts[2].y, quadverts[2].z);
                            GL11.glVertex3f(quadverts[1].x, quadverts[1].y, quadverts[1].z);
                            GL11.glEnd();
                            GL11.glBegin(GL11.GL_LINES);
                            GL11.glColor3f(Colour.vertexSelectedTmpColourR, Colour.vertexSelectedTmpColourG, Colour.vertexSelectedTmpColourB);
                            GL11.glVertex3f(quadverts[0].x, quadverts[0].y, quadverts[0].z);
                            GL11.glVertex3f(quadverts[1].x, quadverts[1].y, quadverts[1].z);

                            GL11.glVertex3f(quadverts[1].x, quadverts[1].y, quadverts[1].z);
                            GL11.glVertex3f(quadverts[2].x, quadverts[2].y , quadverts[2].z);

                            GL11.glVertex3f(quadverts[2].x, quadverts[2].y, quadverts[2].z);
                            GL11.glVertex3f(quadverts[3].x, quadverts[3].y, quadverts[3].z);

                            GL11.glVertex3f(quadverts[3].x, quadverts[3].y, quadverts[3].z);
                            GL11.glVertex3f(quadverts[0].x, quadverts[0].y, quadverts[0].z);
                            GL11.glColor3f(Colour.vertexColourR, Colour.vertexColourG, Colour.vertexColourB);
                            GL11.glEnd();
                            break;
                        case 5:
                            Vertex[] condverts2 = condlines.get(gd);
                            Vertex[] condverts = condverts2.clone();
                            for (int i = 0; i < 2; i++) {
                                Vertex v5 = condverts[i];
                                if (!v5.equals(vertex)) {
                                    Vector4f res = manipulator.getUntransformed(v5.x, v5.y, v5.z);
                                    condverts[i] = new Vertex(res.x, res.y, res.z, true);
                                }
                            }
                            GL11.glBegin(GL11.GL_LINES);
                            GL11.glColor3f(Colour.vertexSelectedTmpColourR, Colour.vertexSelectedTmpColourG, Colour.vertexSelectedTmpColourB);
                            GL11.glVertex3f(condverts[0].x, condverts[0].y, condverts[0].z);
                            GL11.glVertex3f(condverts[1].x, condverts[1].y, condverts[1].z);
                            GL11.glColor3f(Colour.vertexColourR, Colour.vertexColourG, Colour.vertexColourB);
                            GL11.glEnd();
                            break;
                        default:
                            break;
                        }
                    }
                }
            }
        } else if (c3d.isShowingVertices() && !OpenGLRenderer.getSmoothing().get()) {
            GL11.glBegin(GL11.GL_POINTS);
            GL11.glColor3f(Colour.vertexSelectedTmpColourR, Colour.vertexSelectedTmpColourG, Colour.vertexSelectedTmpColourB);
            for (Vertex vertex : selectedVertices) {
                if (tmpHiddenVertices.contains(vertex))
                    continue;
                GL11.glVertex3f(vertex.x, vertex.y, vertex.z);
            }
            GL11.glColor3f(Colour.vertexColourR, Colour.vertexColourG, Colour.vertexColourB);
            GL11.glEnd();
        }

        GL11.glPopMatrix();

        if (OpenGLRenderer.getSmoothing().get()) {

            GL11.glDisable(GL11.GL_LIGHTING);

            GL11.glBegin(GL11.GL_POINTS);
            GL11.glColor3f(Colour.vertexSelectedTmpColourR, Colour.vertexSelectedTmpColourG, Colour.vertexSelectedTmpColourB);
            Object[] obj = getSmoothedVertices(selectedVertices);
            @SuppressWarnings("unchecked")
            List<Vertex> verts = (List<Vertex>) obj[0];
            @SuppressWarnings("unchecked")
            SortedMap<Vertex, Integer> indmap = (SortedMap<Vertex, Integer>) obj[1];
            @SuppressWarnings("unchecked")
            SortedMap<Integer, List<Integer>> adjacency = (SortedMap<Integer, List<Integer>>) obj[2];
            for (Vertex vertex : verts) {
                GL11.glVertex3f(vertex.x, vertex.y, vertex.z);
            }
            GL11.glColor3f(Colour.vertexColourR, Colour.vertexColourG, Colour.vertexColourB);
            GL11.glEnd();

            GraphicalDataTools.setLineWidth(2f);
            GL11.glBegin(GL11.GL_LINES);
            GL11.glColor3f(Colour.vertexSelectedTmpColourR, Colour.vertexSelectedTmpColourG, Colour.vertexSelectedTmpColourB);

            for (Vertex vertex : verts) {
                if (adjacency.containsKey(indmap.get(vertex))) {
                    for (Integer i : adjacency.get(indmap.get(vertex))) {
                        GL11.glVertex3f(vertex.x, vertex.y, vertex.z);
                        Vertex vertex2 = verts.get(i);
                        GL11.glVertex3f(vertex2.x, vertex2.y, vertex2.z);
                    }
                }
            }
            GL11.glColor3f(Colour.vertexColourR, Colour.vertexColourG, Colour.vertexColourB);
            GL11.glEnd();
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        if (c3d.isLightOn())
            GL11.glEnable(GL11.GL_LIGHTING);

        final boolean drawWireframe = c3d.getRenderMode() == -1;
        if (c3d.isMeshLines() || drawWireframe) {
            Vector4f tr2 = new Vector4f(vm.m30, vm.m31, vm.m32 + 300f * c3d.getZoom(), 1f);
            Matrix4f.transform(ivm, tr2, tr2);

            GraphicalDataTools.setLineWidth(2f);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glColor3f(Colour.meshlineColourR, Colour.meshlineColourG, Colour.meshlineColourB);

            if (c3d.isSubMeshLines() || drawWireframe) {
                for (Entry<GData3, Vertex[]> entry : triangles.entrySet()) {
                    GData3 gdata = entry.getKey();
                    if (!(gdata.visible || gdata.isTriangle))
                        continue;
                    GL11.glBegin(GL11.GL_LINE_LOOP);
                    for (Vertex vertex : entry.getValue()) {
                        GL11.glVertex3f(vertex.x + tr2.x, vertex.y + tr2.y, vertex.z + tr2.z);
                    }
                    GL11.glEnd();
                }
                for (Entry<GData4, Vertex[]> entry : quads.entrySet()) {
                    GData4 gdata = entry.getKey();
                    if (!gdata.visible)
                        continue;
                    final Vertex[] quadverts = entry.getValue();
                    GL11.glBegin(GL11.GL_LINES);
                    GL11.glVertex3f(quadverts[0].x + tr2.x, quadverts[0].y + tr2.y, quadverts[0].z + tr2.z);
                    GL11.glVertex3f(quadverts[1].x + tr2.x, quadverts[1].y + tr2.y, quadverts[1].z + tr2.z);

                    GL11.glVertex3f(quadverts[1].x + tr2.x, quadverts[1].y + tr2.y, quadverts[1].z + tr2.z);
                    GL11.glVertex3f(quadverts[2].x + tr2.x, quadverts[2].y + tr2.y, quadverts[2].z + tr2.z);

                    GL11.glVertex3f(quadverts[2].x + tr2.x, quadverts[2].y + tr2.y, quadverts[2].z + tr2.z);
                    GL11.glVertex3f(quadverts[3].x + tr2.x, quadverts[3].y + tr2.y, quadverts[3].z + tr2.z);

                    GL11.glVertex3f(quadverts[3].x + tr2.x, quadverts[3].y + tr2.y, quadverts[3].z + tr2.z);
                    GL11.glVertex3f(quadverts[0].x + tr2.x, quadverts[0].y + tr2.y, quadverts[0].z + tr2.z);
                    GL11.glEnd();
                }
            } else {
                Vertex[] quadverts = new Vertex[4];
                for (Entry<GData, Set<VertexInfo>> entry : lineLinkedToVertices.entrySet()) {
                    GData gdata = entry.getKey();
                    if (!gdata.visible)
                        continue;
                    switch (gdata.type()) {
                    case 3:
                        if (!((GData3) gdata).isTriangle)
                            continue;
                        GL11.glBegin(GL11.GL_LINE_LOOP);
                        for (VertexInfo info : entry.getValue()) {
                            Vertex triVertex = info.vertex;
                            GL11.glVertex3f(triVertex.x + tr2.x, triVertex.y + tr2.y, triVertex.z + tr2.z);
                        }
                        GL11.glEnd();
                        break;
                    case 4:
                        for (VertexInfo info : entry.getValue()) {
                            quadverts[info.position] = info.vertex;
                        }
                        GL11.glBegin(GL11.GL_LINES);
                        GL11.glVertex3f(quadverts[0].x + tr2.x, quadverts[0].y + tr2.y, quadverts[0].z + tr2.z);
                        GL11.glVertex3f(quadverts[1].x + tr2.x, quadverts[1].y + tr2.y, quadverts[1].z + tr2.z);

                        GL11.glVertex3f(quadverts[1].x + tr2.x, quadverts[1].y + tr2.y, quadverts[1].z + tr2.z);
                        GL11.glVertex3f(quadverts[2].x + tr2.x, quadverts[2].y + tr2.y, quadverts[2].z + tr2.z);

                        GL11.glVertex3f(quadverts[2].x + tr2.x, quadverts[2].y + tr2.y, quadverts[2].z + tr2.z);
                        GL11.glVertex3f(quadverts[3].x + tr2.x, quadverts[3].y + tr2.y, quadverts[3].z + tr2.z);

                        GL11.glVertex3f(quadverts[3].x + tr2.x, quadverts[3].y + tr2.y, quadverts[3].z + tr2.z);
                        GL11.glVertex3f(quadverts[0].x + tr2.x, quadverts[0].y + tr2.y, quadverts[0].z + tr2.z);
                        GL11.glEnd();
                        break;
                    default:
                        break;
                    }
                }
            }

            if (c3d.isLightOn())
                GL11.glEnable(GL11.GL_LIGHTING);
        }

        if (!selectedData.isEmpty() || GDataCSG.hasSelectionCSG(linkedDatFile)) {

            GL11.glPushMatrix();

            Vector4f tr = new Vector4f(vm.m30, vm.m31, vm.m32 + 330f * c3d.getZoom(), 1f);
            Matrix4f.transform(ivm, tr, tr);

            GL11.glTranslatef(tr.x, tr.y, tr.z);
            GL11.glMultMatrixf(matrix);

            if (!modifiedManipulator)
                GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glColor3f(Colour.vertexSelectedTmpColourR, Colour.vertexSelectedTmpColourG, Colour.vertexSelectedTmpColourB);
            Vertex[] dataVerts = new Vertex[4];
            int i = 0;
            if (!selectedLines.isEmpty()) {
                GraphicalDataTools.setLineWidth(3f);
                boolean hasDistanceLine = false;
                for (GData2 line : selectedLines) {
                    if (!line.isLine) {
                        hasDistanceLine = true;
                        break;
                    }
                }
                if (hasDistanceLine) {
                    for (GData2 line : selectedLines) {
                        i = 0;
                        if (lines.get(line) != null) {
                            for (Vertex tvertex : lines.get(line)) {
                                dataVerts[i] = tvertex;
                                i++;
                            }
                            if (!line.isLine) {
                                Vertex[] lineverts2 = lines.get(line);
                                Vertex[] lineverts = lineverts2.clone();
                                for (int i1 = 0; i1 < 2; i1++) {
                                    Vertex v2 = lineverts[i1];
                                    Vector4f res = manipulator.getUntransformed(v2.x, v2.y, v2.z);
                                    lineverts[i1] = new Vertex(res.x, res.y, res.z);
                                }
                                GL11.glMultMatrixf(matrixInv);
                                new GData2(dataVerts[0], dataVerts[1], null, new GColour(16, Colour.vertexSelectedTmpColourR, Colour.vertexSelectedTmpColourG, Colour.vertexSelectedTmpColourB, 0f), false).drawDistanceGL20(c3d, lineverts[0].xp, lineverts[0].yp, lineverts[0].zp, lineverts[1].xp, lineverts[1].yp, lineverts[1].zp);
                                GL11.glColor3f(Colour.vertexSelectedTmpColourR, Colour.vertexSelectedTmpColourG, Colour.vertexSelectedTmpColourB);
                                GL11.glMultMatrixf(matrix);
                            }
                            GL11.glBegin(GL11.GL_LINES);
                            GL11.glVertex3f(dataVerts[0].x, dataVerts[0].y, dataVerts[0].z);
                            GL11.glVertex3f(dataVerts[1].x, dataVerts[1].y, dataVerts[1].z);
                            GL11.glEnd();
                        }
                    }
                } else {
                    GL11.glBegin(GL11.GL_LINES);
                    for (GData2 line : selectedLines) {
                        i = 0;
                        if (lines.get(line) != null) {
                            for (Vertex tvertex : lines.get(line)) {
                                dataVerts[i] = tvertex;
                                i++;
                            }
                            GL11.glVertex3f(dataVerts[0].x, dataVerts[0].y, dataVerts[0].z);
                            GL11.glVertex3f(dataVerts[1].x, dataVerts[1].y, dataVerts[1].z);
                        }
                    }
                    GL11.glEnd();
                }
            }
            if (!selectedCondlines.isEmpty()) {
                GraphicalDataTools.setLineWidth(3f);
                GL11.glBegin(GL11.GL_LINES);
                for (GData5 line : selectedCondlines) {
                    i = 0;
                    if (condlines.get(line) != null) {
                        for (Vertex tvertex : condlines.get(line)) {
                            dataVerts[i] = tvertex;
                            i++;
                        }
                        GL11.glVertex3f(dataVerts[0].x, dataVerts[0].y, dataVerts[0].z);
                        GL11.glVertex3f(dataVerts[1].x, dataVerts[1].y, dataVerts[1].z);
                    }
                }
                GL11.glEnd();
                GraphicalDataTools.setLineWidth(4f);
                GL11.glBegin(GL11.GL_LINES);
                for (GData5 line : selectedCondlines) {
                    i = 0;
                    if (condlines.get(line) != null) {
                        for (Vertex tvertex : condlines.get(line)) {
                            dataVerts[i] = tvertex;
                            i++;
                        }
                        GL11.glColor3f(Colour.condlineSelectedColourR, Colour.condlineSelectedColourG, Colour.condlineSelectedColourB);
                        GL11.glVertex3f(dataVerts[0].x, dataVerts[0].y, dataVerts[0].z);
                        GL11.glVertex3f(dataVerts[2].x, dataVerts[2].y, dataVerts[2].z);
                        GL11.glColor3f(Colour.condlineSelectedColourR / 2f, Colour.condlineSelectedColourG / 2f, Colour.condlineSelectedColourB / 2f);
                        GL11.glVertex3f(dataVerts[0].x, dataVerts[0].y, dataVerts[0].z);
                        GL11.glVertex3f(dataVerts[3].x, dataVerts[3].y, dataVerts[3].z);
                    }
                }
                GL11.glEnd();
                GL11.glColor3f(Colour.vertexSelectedTmpColourR, Colour.vertexSelectedTmpColourG, Colour.vertexSelectedTmpColourB);
            }
            if (!selectedTriangles.isEmpty()) {
                GraphicalDataTools.setLineWidth(2f);
                boolean hasProtractor = false;
                for (GData3 tri : selectedTriangles) {
                    if (!tri.isTriangle) {
                        hasProtractor = true;
                        break;
                    }
                }
                if (hasProtractor) {

                    for (GData3 tri : selectedTriangles) {
                        i = 0;
                        if (triangles.get(tri) != null) {
                            for (Vertex tvertex : triangles.get(tri)) {
                                dataVerts[i] = tvertex;
                                i++;
                            }
                            if (!tri.isTriangle) {
                                Vertex[] lineverts2 = triangles.get(tri);
                                Vertex[] lineverts = lineverts2.clone();
                                for (int i1 = 0; i1 < 3; i1++) {
                                    Vertex v2 = lineverts[i1];
                                    Vector4f res = manipulator.getUntransformed(v2.x, v2.y, v2.z);
                                    lineverts[i1] = new Vertex(res.x, res.y, res.z);
                                }
                                GL11.glMultMatrixf(matrixInv);
                                new GData3(dataVerts[0], dataVerts[1], dataVerts[2], null, new GColour(16, tri.r, tri.g, tri.b, 0f), false).drawProtractorGL20(true, c3d, lineverts[0].xp, lineverts[0].yp, lineverts[0].zp, lineverts[1].xp, lineverts[1].yp, lineverts[1].zp, lineverts[2].xp, lineverts[2].yp, lineverts[2].zp);
                                GL11.glMultMatrixf(matrix);
                                GL11.glBegin(GL11.GL_LINES);
                                GL11.glColor3f(Colour.vertexSelectedTmpColourR, Colour.vertexSelectedTmpColourG, Colour.vertexSelectedTmpColourB);
                                GL11.glVertex3f(dataVerts[0].x, dataVerts[0].y, dataVerts[0].z);
                                GL11.glVertex3f(dataVerts[1].x, dataVerts[1].y, dataVerts[1].z);
                                GL11.glVertex3f(dataVerts[2].x, dataVerts[2].y, dataVerts[2].z);
                                GL11.glVertex3f(dataVerts[1].x, dataVerts[1].y, dataVerts[1].z);
                                GL11.glEnd();
                            } else {
                                GL11.glBegin(GL11.GL_LINES);
                                GL11.glVertex3f(dataVerts[0].x, dataVerts[0].y, dataVerts[0].z);
                                GL11.glVertex3f(dataVerts[1].x, dataVerts[1].y, dataVerts[1].z);
                                GL11.glVertex3f(dataVerts[1].x, dataVerts[1].y, dataVerts[1].z);
                                GL11.glVertex3f(dataVerts[2].x, dataVerts[2].y, dataVerts[2].z);
                                GL11.glVertex3f(dataVerts[2].x, dataVerts[2].y, dataVerts[2].z);
                                GL11.glVertex3f(dataVerts[0].x, dataVerts[0].y, dataVerts[0].z);
                                GL11.glEnd();
                            }
                        }
                    }
                } else {
                    GL11.glBegin(GL11.GL_LINES);
                    for (GData3 tri : selectedTriangles) {
                        i = 0;
                        if (triangles.get(tri) != null) {
                            for (Vertex tvertex : triangles.get(tri)) {
                                dataVerts[i] = tvertex;
                                i++;
                            }
                            GL11.glVertex3f(dataVerts[0].x, dataVerts[0].y, dataVerts[0].z);
                            GL11.glVertex3f(dataVerts[1].x, dataVerts[1].y, dataVerts[1].z);
                            GL11.glVertex3f(dataVerts[1].x, dataVerts[1].y, dataVerts[1].z);
                            GL11.glVertex3f(dataVerts[2].x, dataVerts[2].y, dataVerts[2].z);
                            GL11.glVertex3f(dataVerts[2].x, dataVerts[2].y, dataVerts[2].z);
                            GL11.glVertex3f(dataVerts[0].x, dataVerts[0].y, dataVerts[0].z);
                        }
                    }
                    GL11.glEnd();
                    GL11.glColor3f(Colour.vertexSelectedTmpColourR, Colour.vertexSelectedTmpColourG, Colour.vertexSelectedTmpColourB);
                }
            }
            if (!selectedQuads.isEmpty()) {
                GraphicalDataTools.setLineWidth(2f);
                GL11.glBegin(GL11.GL_LINES);
                for (GData4 quad : selectedQuads) {
                    i = 0;
                    if (quads.get(quad) != null) {
                        for (Vertex tvertex : quads.get(quad)) {
                            dataVerts[i] = tvertex;
                            i++;
                        }
                        GL11.glVertex3f(dataVerts[0].x, dataVerts[0].y, dataVerts[0].z);
                        GL11.glVertex3f(dataVerts[1].x, dataVerts[1].y, dataVerts[1].z);
                        GL11.glVertex3f(dataVerts[1].x, dataVerts[1].y, dataVerts[1].z);
                        GL11.glVertex3f(dataVerts[2].x, dataVerts[2].y, dataVerts[2].z);
                        GL11.glVertex3f(dataVerts[2].x, dataVerts[2].y, dataVerts[2].z);
                        GL11.glVertex3f(dataVerts[3].x, dataVerts[3].y, dataVerts[3].z);
                        GL11.glVertex3f(dataVerts[3].x, dataVerts[3].y, dataVerts[3].z);
                        GL11.glVertex3f(dataVerts[0].x, dataVerts[0].y, dataVerts[0].z);
                    }
                }
                GL11.glEnd();
                GL11.glColor3f(Colour.vertexSelectedTmpColourR, Colour.vertexSelectedTmpColourG, Colour.vertexSelectedTmpColourB);
            }

            GL11.glEnable(GL11.GL_DEPTH_TEST);
            if (c3d.isLightOn())
                GL11.glEnable(GL11.GL_LIGHTING);

            GL11.glPopMatrix();

            if (GDataCSG.hasSelectionCSG(linkedDatFile)) {
                GraphicalDataTools.setLineWidth(2f);
                if (!modifiedManipulator)
                    GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glDisable(GL11.GL_LIGHTING);
                GDataCSG.rebuildSelection(linkedDatFile);
                GDataCSG.drawSelectionCSG(c3d);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                if (c3d.isLightOn())
                    GL11.glEnable(GL11.GL_LIGHTING);
            }
        }
    }

    @SuppressWarnings("java:S2111")
    public synchronized void adjustRotationCenter(Composite3D c3d, Event event) {
        Point cSize = c3d.getSize();
        PerspectiveCalculator perspective = c3d.getPerspectiveCalculator();
        Matrix4f viewportTranslation = c3d.getTranslation();
        Matrix4f viewportRotation = c3d.getRotation();
        float viewportPixelPerLDU = c3d.getViewportPixelPerLDU();

        float dx = 0;
        float dy = 0;

        Vector4f zAxis4f = new Vector4f(0, 0, -1f, 1f);
        Matrix4f ovrInverse2 = Matrix4f.invert(viewportRotation, null);
        Matrix4f.transform(ovrInverse2, zAxis4f, zAxis4f);
        Vector4f rayDirection = (Vector4f) new Vector4f(zAxis4f.x, zAxis4f.y, zAxis4f.z, 0f).normalise();
        rayDirection.w = 1f;

        Vertex[] triQuadVerts;
        boolean objectSelected = false;
        GData selectedObject = null;
        Vector4f orig;
        if (event == null) {
            orig = c3d.getManipulator().getPosition();
        } else {
            orig = perspective.get3DCoordinatesFromScreen(event.x, event.y);
        }
        Vector4f point = new Vector4f(orig);

        Vector4f minPoint = new Vector4f();
        double minDist = Double.MAX_VALUE;
        final double[] dist = new double[1];
        for (Entry<GData3, Vertex[]> entry : triangles.entrySet()) {
            GData3 triangle = entry.getKey();
            if (hiddenData.contains(triangle))
                continue;
            triQuadVerts = entry.getValue();
            if (powerRay.triangleIntersect(orig, rayDirection, triQuadVerts[0], triQuadVerts[1], triQuadVerts[2], point, dist) && dist[0] < minDist) {
                minDist = dist[0];
                minPoint.set(point);
                objectSelected = true;
                selectedObject = triangle;
            }
        }

        for (CSG csg : GDataCSG.getCSGs(linkedDatFile)) {
            for(GData3 triangle : csg.getResult(linkedDatFile).keySet()) {
                if (powerRay.triangleIntersect(orig, rayDirection,
                        new Vertex(triangle.x1, triangle.y1, triangle.z1),
                        new Vertex(triangle.x2, triangle.y2, triangle.z2),
                        new Vertex(triangle.x3, triangle.y3, triangle.z3), point, dist) && dist[0] < minDist) {
                    minDist = dist[0];
                    minPoint.set(point);
                    objectSelected = true;
                    selectedObject = triangle;
                }
            }
        }

        for (Entry<GData4, Vertex[]> entry : quads.entrySet()) {
            GData4 quad = entry.getKey();
            if (hiddenData.contains(quad))
                continue;
            triQuadVerts = entry.getValue();
            if ((powerRay.triangleIntersect(orig, rayDirection, triQuadVerts[0], triQuadVerts[1], triQuadVerts[2], point, dist)
                    || powerRay.triangleIntersect(orig, rayDirection, triQuadVerts[2], triQuadVerts[3], triQuadVerts[0], point, dist))
                    && dist[0] < minDist) {
                minDist = dist[0];
                minPoint.set(point);
                objectSelected = true;
                selectedObject = quad;
            }
        }

        if (objectSelected) {
            if (TransformationModeToolItem.getWorkingAction() != WorkingMode.SELECT) {
                c3d.getManipulator().getPosition().set(minPoint.x, minPoint.y, minPoint.z, 1f);
                if (selectedObject.type() == 3) {
                    GData3 tri = (GData3) selectedObject;
                    Vertex[] verts = triangles.get(tri);
                    if (verts == null) {
                        verts = new Vertex[3];
                        verts[0] = new Vertex(tri.x1, tri.y1, tri.z1);
                        verts[1] = new Vertex(tri.x2, tri.y2, tri.z2);
                        verts[2] = new Vertex(tri.x3, tri.y3, tri.z3);
                    }
                    float xn = (verts[2].y - verts[0].y) * (verts[1].z - verts[0].z) - (verts[2].z - verts[0].z) * (verts[1].y - verts[0].y);
                    float yn = (verts[2].z - verts[0].z) * (verts[1].x - verts[0].x) - (verts[2].x - verts[0].x) * (verts[1].z - verts[0].z);
                    float zn = (verts[2].x - verts[0].x) * (verts[1].y - verts[0].y) - (verts[2].y - verts[0].y) * (verts[1].x - verts[0].x);
                    Vector3f n = new Vector3f(-xn, -yn, -zn);
                    n.normalise();
                    c3d.getManipulator().getZaxis().set(n.x, n.y, n.z, 1f);

                    Vector4f v0 = verts[0].toVector4fm();
                    Vector4f v1 = verts[1].toVector4fm();
                    Vector4f v2 = verts[2].toVector4fm();

                    Vector4f d1 = Vector4f.sub(v0, v1, null);
                    Vector4f d2 = Vector4f.sub(v0, v2, null);
                    Vector4f d3 = Vector4f.sub(v1, v2, null);

                    float dist1 = d1.lengthSquared();
                    float dist2 = d2.lengthSquared();
                    float dist3 = d3.lengthSquared();

                    float minD = Math.min(dist1, Math.min(dist2, dist3));
                    if (minD == dist1) {
                        d1.normalise();
                        c3d.getManipulator().getXaxis().set(d1.x, d1.y, d1.z, 1f);
                    } else if (minD == dist2) {
                        d2.normalise();
                        c3d.getManipulator().getXaxis().set(d2.x, d2.y, d2.z, 1f);
                    } else {
                        d3.normalise();
                        c3d.getManipulator().getXaxis().set(d3.x, d3.y, d3.z, 1f);
                    }
                    Vector4f zaxis = c3d.getManipulator().getZaxis();
                    Vector4f xaxis = c3d.getManipulator().getXaxis();
                    Vector3f cross = Vector3f.cross(new Vector3f(xaxis.x, xaxis.y, xaxis.z), new Vector3f(zaxis.x, zaxis.y, zaxis.z), null);
                    c3d.getManipulator().getYaxis().set(cross.x, cross.y, cross.z, 1f);

                } else if (selectedObject.type() == 4) {
                    GData4 quad = (GData4) selectedObject;

                    Vertex[] verts = quads.get(quad);
                    Vector4f v0 = verts[0].toVector4fm();
                    Vector4f v1 = verts[1].toVector4fm();
                    Vector4f v2 = verts[2].toVector4fm();
                    Vector4f v3 = verts[3].toVector4fm();

                    Vector3f[] normals = new Vector3f[4];
                    Vector4f[] lineVectors = new Vector4f[4];
                    lineVectors[0] = Vector4f.sub(v1, v0, null);
                    lineVectors[1] = Vector4f.sub(v2, v1, null);
                    lineVectors[2] = Vector4f.sub(v3, v2, null);
                    lineVectors[3] = Vector4f.sub(v0, v3, null);
                    normals[0] = Vector3f.cross(new Vector3f(lineVectors[0].x, lineVectors[0].y, lineVectors[0].z), new Vector3f(lineVectors[1].x, lineVectors[1].y, lineVectors[1].z), null);
                    normals[1] = Vector3f.cross(new Vector3f(lineVectors[1].x, lineVectors[1].y, lineVectors[1].z), new Vector3f(lineVectors[2].x, lineVectors[2].y, lineVectors[2].z), null);
                    normals[2] = Vector3f.cross(new Vector3f(lineVectors[2].x, lineVectors[2].y, lineVectors[2].z), new Vector3f(lineVectors[3].x, lineVectors[3].y, lineVectors[3].z), null);
                    normals[3] = Vector3f.cross(new Vector3f(lineVectors[3].x, lineVectors[3].y, lineVectors[3].z), new Vector3f(lineVectors[0].x, lineVectors[0].y, lineVectors[0].z), null);

                    Vector3f n = new Vector3f();
                    for (int k = 0; k < 4; k++) {
                        n = Vector3f.add(normals[k], n, null);
                    }
                    n.normalise();

                    c3d.getManipulator().getZaxis().set(n.x, n.y, n.z, 1f);

                    Vector4f d1 = Vector4f.sub(v0, v1, null);
                    Vector4f d2 = Vector4f.sub(v0, v2, null);
                    Vector4f d3 = Vector4f.sub(v1, v2, null);

                    float dist1 = d1.lengthSquared();
                    float dist2 = d2.lengthSquared();
                    float dist3 = d3.lengthSquared();

                    float minD = Math.min(dist1, Math.min(dist2, dist3));
                    if (minD == dist1) {
                        d1.normalise();
                        c3d.getManipulator().getXaxis().set(d1.x, d1.y, d1.z, 1f);
                    } else if (minD == dist2) {
                        d2.normalise();
                        c3d.getManipulator().getXaxis().set(d2.x, d2.y, d2.z, 1f);
                    } else {
                        d3.normalise();
                        c3d.getManipulator().getXaxis().set(d3.x, d3.y, d3.z, 1f);
                    }
                    Vector4f zaxis = c3d.getManipulator().getZaxis();
                    Vector4f xaxis = c3d.getManipulator().getXaxis();
                    Vector3f cross = Vector3f.cross(new Vector3f(xaxis.x, xaxis.y, xaxis.z), new Vector3f(zaxis.x, zaxis.y, zaxis.z), null);
                    c3d.getManipulator().getYaxis().set(cross.x, cross.y, cross.z, 1f);
                }
            }
            Vector4f zAxis4fTranslation = new Vector4f(-minPoint.x, -minPoint.y, -minPoint.z, 1.0f);
            Vector3f zAxis3 = new Vector3f(zAxis4fTranslation.x, zAxis4fTranslation.y, zAxis4fTranslation.z);
            viewportTranslation.load(Matrix4f.translate(zAxis3, View.ID, null));
        } else {
            if (event == null)
                return;
            dx = (event.x - cSize.x / 2f) / viewportPixelPerLDU;
            dy = (cSize.y / 2f - event.y) / viewportPixelPerLDU;

            Vector4f xAxis4fTranslation = new Vector4f(dx, 0, 0, 1.0f);
            Vector4f yAxis4fTranslation = new Vector4f(0, dy, 0, 1.0f);

            Matrix4f.transform(ovrInverse2, xAxis4fTranslation, xAxis4fTranslation);
            Matrix4f.transform(ovrInverse2, yAxis4fTranslation, yAxis4fTranslation);
            Vector3f xAxis3 = new Vector3f(xAxis4fTranslation.x, xAxis4fTranslation.y, xAxis4fTranslation.z);
            Vector3f yAxis3 = new Vector3f(yAxis4fTranslation.x, yAxis4fTranslation.y, yAxis4fTranslation.z);

            Matrix4f.translate(xAxis3, viewportTranslation, viewportTranslation);
            Matrix4f.translate(yAxis3, viewportTranslation, viewportTranslation);
        }

        c3d.getTranslation().load(viewportTranslation);

        perspective.calculateOriginData();

        c3d.getManipulator().setAccurateXaxis(new BigDecimal(c3d.getManipulator().getXaxis().x), new BigDecimal(c3d.getManipulator().getXaxis().y), new BigDecimal(c3d.getManipulator().getXaxis().z));
        c3d.getManipulator().setAccurateYaxis(new BigDecimal(c3d.getManipulator().getYaxis().x), new BigDecimal(c3d.getManipulator().getYaxis().y), new BigDecimal(c3d.getManipulator().getYaxis().z));
        c3d.getManipulator().setAccurateZaxis(new BigDecimal(c3d.getManipulator().getZaxis().x), new BigDecimal(c3d.getManipulator().getZaxis().y), new BigDecimal(c3d.getManipulator().getZaxis().z));
        c3d.getManipulator().setAccuratePosition(new BigDecimal(c3d.getManipulator().getPosition().x / 1000f), new BigDecimal(c3d.getManipulator().getPosition().y / 1000f),
                new BigDecimal(c3d.getManipulator().getPosition().z / 1000f));

        if (event != null) {
            c3d.getCursor3D().set(perspective.get3DCoordinatesFromScreen(event.x, event.y));
            c3d.getScreenXY().set(event.x, event.y);
            GuiStatusManager.updateStatus(c3d);
            ((ScalableComposite) c3d.getParent()).redrawScales(event.x, event.y);
        }
    }

    public static void copySingleVertexIntoClipboard(final Vertex vertex) {
        copySingleVertexIntoClipboardContent(vertex);
    }

    public static Vertex getSingleVertexFromClipboard() {
        return getSingleVertexFromClipboardContent();
    }

    public static List<GData> getClipboard() {
        return getClipboardContent();
    }
}
