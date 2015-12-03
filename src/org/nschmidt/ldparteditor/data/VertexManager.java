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

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.nio.FloatBuffer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MessageBox;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.composites.ScalableComposite;
import org.nschmidt.ldparteditor.data.tools.IdenticalVertexRemover;
import org.nschmidt.ldparteditor.data.tools.Merger;
import org.nschmidt.ldparteditor.enums.MergeTo;
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.enums.RotationSnap;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.enums.TransformationMode;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.enums.WorkingMode;
import org.nschmidt.ldparteditor.helpers.Manipulator;
import org.nschmidt.ldparteditor.helpers.composite3d.GuiManager;
import org.nschmidt.ldparteditor.helpers.composite3d.PerspectiveCalculator;
import org.nschmidt.ldparteditor.helpers.composite3d.SelectorSettings;
import org.nschmidt.ldparteditor.helpers.math.HashBiMap;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeTreeMap;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.text.DatParser;
import org.nschmidt.ldparteditor.text.HeaderState;

/**
 * All actions are THREAD safe!! 1. Displays all vertices <br>
 * 2. Provides functions to <br>
 * 2.1 Manipulate.. <br>
 * 2.2 Create.. <br>
 * 2.3 Delete vertices
 *
 * @author nils
 *
 */
public class VertexManager extends VM99Clipboard {

    public VertexManager(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public synchronized void draw(Composite3D c3d) {
        if (!linkedDatFile.isDrawSelection()) return;
        Matrix4f vm = c3d.getViewport();
        Matrix4f ivm = c3d.getViewport_Inverse();

        Manipulator manipulator = c3d.getManipulator();
        FloatBuffer matrix = manipulator.getTempTransformation();
        final boolean modifiedManipulator = manipulator.isModified();

        if (c3d.isShowingVertices()) {
            Vector4f tr = new Vector4f(vm.m30, vm.m31, vm.m32 + 330f * c3d.getZoom(), 1f);
            Matrix4f.transform(ivm, tr, tr);
            GL11.glDisable(GL11.GL_LIGHTING);
            if (c3d.isShowingHiddenVertices()) {
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glColor3f(View.vertex_Colour_r[0] * .5f, View.vertex_Colour_g[0] * .5f, View.vertex_Colour_b[0] * .5f);
                GL11.glBegin(GL11.GL_POINTS);
                for (Vertex vertex : vertexLinkedToPositionInFile.keySet()) {
                    if (hiddenVertices.contains(vertex))
                        continue;
                    GL11.glVertex3f(vertex.x, vertex.y, vertex.z);
                }
                GL11.glEnd();
                GL11.glEnable(GL11.GL_DEPTH_TEST);
            }
            GL11.glColor3f(View.vertex_Colour_r[0], View.vertex_Colour_g[0], View.vertex_Colour_b[0]);
            GL11.glBegin(GL11.GL_POINTS);
            for (Vertex vertex : vertexLinkedToPositionInFile.keySet()) {
                if (hiddenVertices.contains(vertex))
                    continue;
                GL11.glVertex3f(vertex.x + tr.x, vertex.y + tr.y, vertex.z + tr.z);
            }
            GL11.glEnd();
        }
        if (!modifiedManipulator)
            GL11.glDisable(GL11.GL_DEPTH_TEST);

        GL11.glPushMatrix();
        // Matrix4f.transform(ivm, tr, tr);
        GL11.glMultMatrix(matrix);

        if (modifiedManipulator) {
            Set<GData> alreadyMoved = new HashSet<GData>();

            Set<Vertex> allVertices = new TreeSet<Vertex>(selectedVertices);
            GL11.glLineWidth(2f);
            if (c3d.isShowingVertices()) {
                GL11.glBegin(GL11.GL_POINTS);
                for (Vertex vertex : selectedVertices) {
                    GL11.glColor3f(View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0]);
                    GL11.glVertex3f(vertex.x, vertex.y, vertex.z);
                    GL11.glColor3f(View.vertex_Colour_r[0], View.vertex_Colour_g[0], View.vertex_Colour_b[0]);
                }
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
                    GData gd = m.getGdata();
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
                    } else if (lineLinkedToVertices.containsKey(gd) && Editor3DWindow.getWindow().isMovingAdjacentData()) {
                        if (gd.visible) {
                            dataToHide.add(gd);
                            gd.hide();
                        }
                        switch (gd.type()) {
                        case 2:
                            Vertex[] lineverts2 = lines.get(gd);
                            Vertex[] lineverts = lineverts2.clone();
                            for (int i = 0; i < 2; i++) {
                                Vertex v2 = lineverts[i];
                                if (!v2.equals(vertex)) {
                                    Vector4f res = manipulator.getUntransformed(v2.x, v2.y, v2.z);
                                    lineverts[i] = new Vertex(res.x, res.y, res.z, true);
                                }
                            }
                            GL11.glBegin(GL11.GL_LINES);
                            GL11.glColor3f(View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0]);
                            GL11.glVertex3f(lineverts[0].x, lineverts[0].y, lineverts[0].z);
                            GL11.glVertex3f(lineverts[1].x, lineverts[1].y, lineverts[1].z);
                            GL11.glColor3f(View.vertex_Colour_r[0], View.vertex_Colour_g[0], View.vertex_Colour_b[0]);
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
                                    triverts[i] = new Vertex(res.x, res.y, res.z, true);
                                }
                            }
                            nx = (triverts[2].y - triverts[0].y) * (triverts[1].z - triverts[0].z) - (triverts[2].z - triverts[0].z) * (triverts[1].y - triverts[0].y);
                            ny = (triverts[2].z - triverts[0].z) * (triverts[1].x - triverts[0].x) - (triverts[2].x - triverts[0].x) * (triverts[1].z - triverts[0].z);
                            nz = (triverts[2].x - triverts[0].x) * (triverts[1].y - triverts[0].y) - (triverts[2].y - triverts[0].y) * (triverts[1].x - triverts[0].x);
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
                            GL11.glColor3f(View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0]);
                            GL11.glVertex3f(triverts[0].x, triverts[0].y, triverts[0].z);
                            GL11.glVertex3f(triverts[1].x, triverts[1].y, triverts[1].z);

                            GL11.glVertex3f(triverts[1].x, triverts[1].y, triverts[1].z);
                            GL11.glVertex3f(triverts[2].x, triverts[2].y, triverts[2].z);

                            GL11.glVertex3f(triverts[2].x, triverts[2].y, triverts[2].z);
                            GL11.glVertex3f(triverts[0].x, triverts[0].y, triverts[0].z);
                            GL11.glColor3f(View.vertex_Colour_r[0], View.vertex_Colour_g[0], View.vertex_Colour_b[0]);
                            GL11.glEnd();
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
                            GL11.glColor3f(View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0]);
                            GL11.glVertex3f(quadverts[0].x, quadverts[0].y, quadverts[0].z);
                            GL11.glVertex3f(quadverts[1].x, quadverts[1].y, quadverts[1].z);

                            GL11.glVertex3f(quadverts[1].x, quadverts[1].y, quadverts[1].z);
                            GL11.glVertex3f(quadverts[2].x, quadverts[2].y , quadverts[2].z);

                            GL11.glVertex3f(quadverts[2].x, quadverts[2].y, quadverts[2].z);
                            GL11.glVertex3f(quadverts[3].x, quadverts[3].y, quadverts[3].z);

                            GL11.glVertex3f(quadverts[3].x, quadverts[3].y, quadverts[3].z);
                            GL11.glVertex3f(quadverts[0].x, quadverts[0].y, quadverts[0].z);
                            GL11.glColor3f(View.vertex_Colour_r[0], View.vertex_Colour_g[0], View.vertex_Colour_b[0]);
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
                            GL11.glColor3f(View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0]);
                            GL11.glVertex3f(condverts[0].x, condverts[0].y, condverts[0].z);
                            GL11.glVertex3f(condverts[1].x, condverts[1].y, condverts[1].z);
                            GL11.glColor3f(View.vertex_Colour_r[0], View.vertex_Colour_g[0], View.vertex_Colour_b[0]);
                            GL11.glEnd();
                            break;
                        default:
                            break;
                        }
                    }
                }
            }
        } else if (c3d.isShowingVertices()) {
            GL11.glBegin(GL11.GL_POINTS);
            for (Vertex vertex : selectedVertices) {
                if (hiddenVertices.contains(vertex))
                    continue;
                GL11.glColor3f(View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0]);
                GL11.glVertex3f(vertex.x, vertex.y, vertex.z);
                GL11.glColor3f(View.vertex_Colour_r[0], View.vertex_Colour_g[0], View.vertex_Colour_b[0]);
            }
            GL11.glEnd();
        }

        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        if (c3d.isLightOn())
            GL11.glEnable(GL11.GL_LIGHTING);

        final boolean drawWireframe = c3d.getRenderMode() == -1;
        if (c3d.isMeshLines() || drawWireframe) {
            Vector4f tr2 = new Vector4f(vm.m30, vm.m31, vm.m32 + 300f * c3d.getZoom(), 1f);
            Matrix4f.transform(ivm, tr2, tr2);

            GL11.glLineWidth(2f);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glColor3f(View.line_Colour_r[0], View.line_Colour_g[0], View.line_Colour_b[0]);

            if (c3d.isSubMeshLines() || drawWireframe) {
                Vertex[] quadverts = new Vertex[4];
                for (GData3 gdata : triangles.keySet()) {
                    if (!gdata.visible) // if (hiddenData.contains(gdata))
                        continue;
                    GL11.glBegin(GL11.GL_LINE_LOOP);
                    for (Vertex vertex : triangles.get(gdata)) {
                        GL11.glVertex3f(vertex.x + tr2.x, vertex.y + tr2.y, vertex.z + tr2.z);
                    }
                    GL11.glEnd();
                }
                for (GData4 gdata : quads.keySet()) {
                    if (!gdata.visible)
                        continue;
                    quadverts = quads.get(gdata);
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
                for (GData gdata : lineLinkedToVertices.keySet()) {
                    if (!gdata.visible)
                        continue;
                    switch (gdata.type()) {
                    case 3:
                        GL11.glBegin(GL11.GL_LINE_LOOP);
                        for (VertexInfo info : lineLinkedToVertices.get(gdata)) {
                            Vertex triVertex = info.vertex;
                            GL11.glVertex3f(triVertex.x + tr2.x, triVertex.y + tr2.y, triVertex.z + tr2.z);
                        }
                        GL11.glEnd();
                        break;
                    case 4:
                        for (VertexInfo info : lineLinkedToVertices.get(gdata)) {
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


        if (!selectedData.isEmpty()) {

            GL11.glPushMatrix();

            Vector4f tr = new Vector4f(vm.m30, vm.m31, vm.m32 + 330f * c3d.getZoom(), 1f);
            Matrix4f.transform(ivm, tr, tr);

            GL11.glTranslatef(tr.x, tr.y, tr.z);
            GL11.glMultMatrix(matrix);

            if (!modifiedManipulator)
                GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glColor3f(View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0]);
            Vertex[] dataVerts = new Vertex[4];
            int i = 0;
            if (!selectedLines.isEmpty()) {
                GL11.glLineWidth(3f);
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
            if (!selectedCondlines.isEmpty()) {
                GL11.glLineWidth(3f);
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
                GL11.glLineWidth(4f);
                GL11.glBegin(GL11.GL_LINES);
                for (GData5 line : selectedCondlines) {
                    i = 0;
                    if (condlines.get(line) != null) {
                        for (Vertex tvertex : condlines.get(line)) {
                            dataVerts[i] = tvertex;
                            i++;
                        }
                        GL11.glColor3f(View.condline_selected_Colour_r[0], View.condline_selected_Colour_g[0], View.condline_selected_Colour_b[0]);
                        GL11.glVertex3f(dataVerts[0].x, dataVerts[0].y, dataVerts[0].z);
                        GL11.glVertex3f(dataVerts[2].x, dataVerts[2].y, dataVerts[2].z);
                        GL11.glColor3f(View.condline_selected_Colour_r[0] / 2f, View.condline_selected_Colour_g[0] / 2f, View.condline_selected_Colour_b[0] / 2f);
                        GL11.glVertex3f(dataVerts[0].x, dataVerts[0].y, dataVerts[0].z);
                        GL11.glVertex3f(dataVerts[3].x, dataVerts[3].y, dataVerts[3].z);
                    }
                }
                GL11.glEnd();
                GL11.glColor3f(View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0]);
            }
            if (c3d.isFillingSelectedFaces()) {
                if (!selectedTriangles.isEmpty()) {
                    GL11.glBegin(GL11.GL_TRIANGLES);
                    for (GData3 tri : selectedTriangles) {
                        i = 0;
                        if (triangles.get(tri) != null) {
                            for (Vertex tvertex : triangles.get(tri)) {
                                dataVerts[i] = tvertex;
                                i++;
                            }
                            GL11.glVertex3f(dataVerts[0].x, dataVerts[0].y, dataVerts[0].z);
                            GL11.glVertex3f(dataVerts[1].x, dataVerts[1].y, dataVerts[1].z);
                            GL11.glVertex3f(dataVerts[2].x, dataVerts[2].y, dataVerts[2].z);
                            GL11.glVertex3f(dataVerts[0].x, dataVerts[0].y, dataVerts[0].z);
                            GL11.glVertex3f(dataVerts[2].x, dataVerts[2].y, dataVerts[2].z);
                            GL11.glVertex3f(dataVerts[1].x, dataVerts[1].y, dataVerts[1].z);
                        }
                    }
                    GL11.glEnd();
                }
                if (!selectedQuads.isEmpty()) {
                    GL11.glBegin(GL11.GL_QUADS);
                    for (GData4 quad : selectedQuads) {
                        i = 0;
                        if (quads.get(quad) != null) {
                            for (Vertex tvertex : quads.get(quad)) {
                                dataVerts[i] = tvertex;
                                i++;
                            }
                            GL11.glVertex3f(dataVerts[0].x, dataVerts[0].y, dataVerts[0].z);
                            GL11.glVertex3f(dataVerts[1].x, dataVerts[1].y, dataVerts[1].z);
                            GL11.glVertex3f(dataVerts[2].x, dataVerts[2].y, dataVerts[2].z);
                            GL11.glVertex3f(dataVerts[3].x, dataVerts[3].y, dataVerts[3].z);
                            GL11.glVertex3f(dataVerts[2].x, dataVerts[2].y, dataVerts[2].z);
                            GL11.glVertex3f(dataVerts[1].x, dataVerts[1].y, dataVerts[1].z);
                            GL11.glVertex3f(dataVerts[0].x, dataVerts[0].y, dataVerts[0].z);
                            GL11.glVertex3f(dataVerts[3].x, dataVerts[3].y, dataVerts[3].z);
                        }
                    }
                    GL11.glEnd();
                }
            }
            if (!selectedTriangles.isEmpty()) {
                GL11.glLineWidth(2f);
                if (c3d.isFillingSelectedFaces())
                    GL11.glColor3f(View.line_Colour_r[0], View.line_Colour_g[0], View.line_Colour_b[0]);
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
                GL11.glColor3f(View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0]);
            }
            if (!selectedQuads.isEmpty()) {
                GL11.glLineWidth(2f);
                if (c3d.isFillingSelectedFaces())
                    GL11.glColor3f(View.line_Colour_r[0], View.line_Colour_g[0], View.line_Colour_b[0]);
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
                GL11.glColor3f(View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0]);
            }
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            if (c3d.isLightOn())
                GL11.glEnable(GL11.GL_LIGHTING);

            GL11.glPopMatrix();
        }
    }

    public synchronized GData changeColour(int index, float r, float g, float b, float a, GData dataToModify) {
        HashSet<GData> newSet = new HashSet<GData>();
        newSet.add(dataToModify);
        changeColour(index, r, g, b, a, newSet);
        if (newSet.iterator().hasNext()) {
            return newSet.iterator().next();
        } else {
            return null;
        }
    }

    public synchronized void changeColour(int index, float r, float g, float b, float a, Set<GData> dataToModify) {
        HashSet<GData> newData = new HashSet<GData>();
        GColour colour = View.getLDConfigColour(index);
        final float cr = colour.getR();
        final float cg = colour.getG();
        final float cb = colour.getB();
        final float ca = colour.getA();
        for (GData gData : dataToModify) {
            if (index > -1) {
                switch (gData.type()) {
                case 2:
                case 5:
                    if (index == 24) {
                        r = View.line_Colour_r[0];
                        g = View.line_Colour_g[0];
                        b = View.line_Colour_b[0];
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
            }
            GData newGData = null;
            switch (gData.type()) {
            case 2:
                GData2 gd2 = (GData2) gData;
                GData2 newGdata2 = new GData2(gd2.parent, index, r, g, b, a, gd2.X1, gd2.Y1, gd2.Z1, gd2.X2, gd2.Y2, gd2.Z2, gd2.x1, gd2.y1, gd2.z1, gd2.x2, gd2.y2, gd2.z2, linkedDatFile);
                newData.add(newGdata2);
                newGData = newGdata2;
                break;
            case 3:
                GData3 gd3 = (GData3) gData;
                GData3 newGdata3 = new GData3(index, r, g, b, a, gd3.X1, gd3.Y1, gd3.Z1, gd3.X2, gd3.Y2, gd3.Z2, gd3.X3, gd3.Y3, gd3.Z3, gd3.x1, gd3.y1, gd3.z1, gd3.x2, gd3.y2, gd3.z2, gd3.x3,
                        gd3.y3, gd3.z3, gd3.xn, gd3.yn, gd3.zn, gd3.parent, linkedDatFile);
                newData.add(newGdata3);
                newGData = newGdata3;
                break;
            case 4:
                GData4 gd4 = (GData4) gData;
                GData4 newGdata4 = new GData4(index, r, g, b, a, gd4.X1, gd4.Y1, gd4.Z1, gd4.X2, gd4.Y2, gd4.Z2, gd4.X3, gd4.Y3, gd4.Z3, gd4.X4, gd4.Y4, gd4.Z4, gd4.x1, gd4.y1, gd4.z1, gd4.x2,
                        gd4.y2, gd4.z2, gd4.x3, gd4.y3, gd4.z3, gd4.x4, gd4.y4, gd4.z4, gd4.xn, gd4.yn, gd4.zn, gd4.parent, linkedDatFile);
                newData.add(newGdata4);
                newGData = newGdata4;
                break;
            case 5:
                GData5 gd5 = (GData5) gData;
                GData5 newGdata5 = new GData5(true, index, r, g, b, a, gd5.X1, gd5.Y1, gd5.Z1, gd5.X2, gd5.Y2, gd5.Z2, gd5.X3, gd5.Y3, gd5.Z3, gd5.X4, gd5.Y4, gd5.Z4, gd5.x1, gd5.y1, gd5.z1, gd5.x2,
                        gd5.y2, gd5.z2, gd5.x3, gd5.y3, gd5.z3, gd5.x4, gd5.y4, gd5.z4, gd5.parent, linkedDatFile);
                newData.add(newGdata5);
                newGData = newGdata5;
                break;
            }
            linker(gData, newGData);
        }
        dataToModify.clear();
        dataToModify.addAll(newData);
    }

    public synchronized void adjustRotationCenter(Composite3D c3d, Event event) {
        Point cSize = c3d.getSize();
        PerspectiveCalculator perspective = c3d.getPerspectiveCalculator();
        Matrix4f viewport_translation = c3d.getTranslation();
        Matrix4f viewport_rotation = c3d.getRotation();
        float viewport_pixel_per_ldu = c3d.getViewportPixelPerLDU();

        float dx = 0;
        float dy = 0;

        Vector4f zAxis4f = new Vector4f(0, 0, -1f, 1f);
        Matrix4f ovr_inverse2 = Matrix4f.invert(viewport_rotation, null);
        Matrix4f.transform(ovr_inverse2, zAxis4f, zAxis4f);
        Vector4f rayDirection = (Vector4f) new Vector4f(zAxis4f.x, zAxis4f.y, zAxis4f.z, 0f).normalise();
        rayDirection.w = 1f;

        Vertex[] triQuadVerts = new Vertex[4];
        int i = 0;
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
        for (GData3 triangle : triangles.keySet()) {
            if (hiddenData.contains(triangle))
                continue;
            i = 0;
            for (Vertex tvertex : triangles.get(triangle)) {
                triQuadVerts[i] = tvertex;
                i++;
            }
            if (powerRay.TRIANGLE_INTERSECT(orig, rayDirection, triQuadVerts[0], triQuadVerts[1], triQuadVerts[2], point, dist)) {
                if (dist[0] < minDist) {
                    minDist = dist[0];
                    minPoint.set(point);
                    objectSelected = true;
                    selectedObject = triangle;
                }
            }
        }
        for (GData4 quad : quads.keySet()) {
            if (hiddenData.contains(quad))
                continue;
            i = 0;
            for (Vertex tvertex : quads.get(quad)) {
                triQuadVerts[i] = tvertex;
                i++;
            }
            if (powerRay.TRIANGLE_INTERSECT(orig, rayDirection, triQuadVerts[0], triQuadVerts[1], triQuadVerts[2], point, dist)
                    || powerRay.TRIANGLE_INTERSECT(orig, rayDirection, triQuadVerts[2], triQuadVerts[3], triQuadVerts[0], point, dist)) {
                if (dist[0] < minDist) {
                    minDist = dist[0];
                    minPoint.set(point);
                    objectSelected = true;
                    selectedObject = quad;
                }
            }
        }

        if (objectSelected) {
            if (Editor3DWindow.getWindow().getWorkingAction() != WorkingMode.SELECT) {
                c3d.getManipulator().getPosition().set(minPoint.x, minPoint.y, minPoint.z, 1f);
                if (selectedObject.type() == 3) {
                    GData3 tri = (GData3) selectedObject;
                    Vertex[] verts = triangles.get(tri);
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
            Vector4f zAxis4f_translation = new Vector4f(-minPoint.x, -minPoint.y, -minPoint.z, 1.0f);
            Vector3f zAxis3 = new Vector3f(zAxis4f_translation.x, zAxis4f_translation.y, zAxis4f_translation.z);
            viewport_translation.load(Matrix4f.translate(zAxis3, View.ID, null));
        } else {
            if (event == null)
                return;
            dx = (event.x - cSize.x / 2) / viewport_pixel_per_ldu;
            dy = (cSize.y / 2 - event.y) / viewport_pixel_per_ldu;

            Vector4f xAxis4f_translation = new Vector4f(dx, 0, 0, 1.0f);
            Vector4f yAxis4f_translation = new Vector4f(0, dy, 0, 1.0f);

            Matrix4f.transform(ovr_inverse2, xAxis4f_translation, xAxis4f_translation);
            Matrix4f.transform(ovr_inverse2, yAxis4f_translation, yAxis4f_translation);
            Vector3f xAxis3 = new Vector3f(xAxis4f_translation.x, xAxis4f_translation.y, xAxis4f_translation.z);
            Vector3f yAxis3 = new Vector3f(yAxis4f_translation.x, yAxis4f_translation.y, yAxis4f_translation.z);

            Matrix4f.translate(xAxis3, viewport_translation, viewport_translation);
            Matrix4f.translate(yAxis3, viewport_translation, viewport_translation);
        }

        c3d.getTranslation().load(viewport_translation);

        perspective.calculateOriginData();

        c3d.getManipulator().setAccurateXaxis(new BigDecimal(c3d.getManipulator().getXaxis().x), new BigDecimal(c3d.getManipulator().getXaxis().y), new BigDecimal(c3d.getManipulator().getXaxis().z));
        c3d.getManipulator().setAccurateYaxis(new BigDecimal(c3d.getManipulator().getYaxis().x), new BigDecimal(c3d.getManipulator().getYaxis().y), new BigDecimal(c3d.getManipulator().getYaxis().z));
        c3d.getManipulator().setAccurateZaxis(new BigDecimal(c3d.getManipulator().getZaxis().x), new BigDecimal(c3d.getManipulator().getZaxis().y), new BigDecimal(c3d.getManipulator().getZaxis().z));
        c3d.getManipulator().setAccuratePosition(new BigDecimal(c3d.getManipulator().getPosition().x / 1000f), new BigDecimal(c3d.getManipulator().getPosition().y / 1000f),
                new BigDecimal(c3d.getManipulator().getPosition().z / 1000f));

        if (event != null) {
            c3d.getCursor3D().set(perspective.get3DCoordinatesFromScreen(event.x, event.y));
            c3d.getScreenXY().set(event.x, event.y);
            GuiManager.updateStatus(c3d);
            ((ScalableComposite) c3d.getParent()).redrawScales(event.x, event.y);
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
    public synchronized void transformSelection(Matrix transformation, boolean moveAdjacentData) {
        if (linkedDatFile.isReadOnly())
            return;

        final Set<Vertex> singleVertices = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());

        final HashSet<GData0> effSelectedVertices = new HashSet<GData0>();
        final HashSet<GData2> effSelectedLines = new HashSet<GData2>();
        final HashSet<GData3> effSelectedTriangles = new HashSet<GData3>();
        final HashSet<GData4> effSelectedQuads = new HashSet<GData4>();
        final HashSet<GData5> effSelectedCondlines = new HashSet<GData5>();

        selectedData.clear();

        // 0. Deselect selected subfile data (for whole selected subfiles)
        for (GData1 subf : selectedSubfiles) {
            Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
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
            transform(allData, allVertices, transformation, true, moveAdjacentData);

            // 4. Subfile Based Transformation & Selection
            if (!selectedSubfiles.isEmpty()) {
                HashBiMap<Integer, GData> drawPerLine = linkedDatFile.getDrawPerLine_NOCLONE();
                HashSet<GData1> newSubfiles = new HashSet<GData1>();
                for (GData1 subf : selectedSubfiles) {
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

                syncWithTextEditors(true);
                updateUnsavedStatus();

            }
            newSelectedData.clear();
            selectedVertices.retainAll(vertexLinkedToPositionInFile.keySet());
        }
    }

    /**
     * Transforms the selection (vertices or data)
     * @param allData
     * @param allVertices
     * @param transformation
     * @param moveAdjacentData
     */
    private void transform(Set<GData> allData, Set<Vertex> allVertices, Matrix transformation, boolean updateSelection, boolean moveAdjacentData) {
        HashSet<GData> allData2 = new HashSet<GData>(allData);
        TreeSet<Vertex> verticesToTransform = new TreeSet<Vertex>();
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
        for (Vertex v : allVertices) {
            BigDecimal[] temp = transformation.transform(v.X, v.Y, v.Z);
            oldToNewVertex.put(v, new Vertex(temp[0], temp[1], temp[2]));
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
                    Vertex v1 = oldToNewVertex.get(verts[0]);
                    if (v1 == null) v1 = verts[0];
                    newData = addVertex(v1);
                    if (updateSelection) {
                        selectedVertices.remove(verts[0]);
                        selectedVertices.add(v1);
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
                    newData = new GData2(g2.colourNumber, g2.r, g2.g, g2.b, g2.a, v1, v2, g2.parent, linkedDatFile);
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
                    newData = new GData3(g3.colourNumber, g3.r, g3.g, g3.b, g3.a, v1, v2, v3, g3.parent, linkedDatFile);
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

    public synchronized void colourChangeSelection(int index, float r, float g, float b, float a) {
        if (linkedDatFile.isReadOnly())
            return;

        final HashSet<GData2> effSelectedLines = new HashSet<GData2>();
        final HashSet<GData3> effSelectedTriangles = new HashSet<GData3>();
        final HashSet<GData4> effSelectedQuads = new HashSet<GData4>();
        final HashSet<GData5> effSelectedCondlines = new HashSet<GData5>();

        final HashSet<GData2> subSelectedLines = new HashSet<GData2>();
        final HashSet<GData3> subSelectedTriangles = new HashSet<GData3>();
        final HashSet<GData4> subSelectedQuads = new HashSet<GData4>();
        final HashSet<GData5> subSelectedCondlines = new HashSet<GData5>();

        selectedData.clear();
        selectedVertices.clear();

        // 0. Deselect selected subfile data (for whole selected subfiles)
        for (GData1 subf : selectedSubfiles) {
            Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
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

        ArrayList<GData> modData = new ArrayList<GData>();
        for (GData2 gd : effSelectedLines)
            modData.add(changeColour(index, r, g, b, a, gd));
        for (GData3 gd : effSelectedTriangles)
            modData.add(changeColour(index, r, g, b, a, gd));
        for (GData4 gd : effSelectedQuads)
            modData.add(changeColour(index, r, g, b, a, gd));
        for (GData5 gd : effSelectedCondlines)
            modData.add(changeColour(index, r, g, b, a, gd));

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

        if (!selectedLines.isEmpty())
            setModified_NoSync();
        if (!selectedTriangles.isEmpty())
            setModified_NoSync();
        if (!selectedQuads.isEmpty())
            setModified_NoSync();
        if (!selectedCondlines.isEmpty())
            setModified_NoSync();

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

            HashBiMap<Integer, GData> drawPerLine = linkedDatFile.getDrawPerLine_NOCLONE();
            HashSet<GData1> newSubfiles = new HashSet<GData1>();
            for (GData1 subf : selectedSubfiles) {
                String colouredString = subf.getColouredString(col);
                GData oldNext = subf.getNext();
                GData oldBefore = subf.getBefore();
                remove(subf);
                GData colouredSubfile;
                if ("16".equals(col)) { //$NON-NLS-1$
                    colouredSubfile = DatParser
                            .parseLine(colouredString, drawPerLine.getKey(subf).intValue(), 0, 0.5f, 0.5f, 0.5f, 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile, false,
                                    new HashSet<String>(), false).get(0).getGraphicalData();
                } else {
                    colouredSubfile = DatParser
                            .parseLine(colouredString, drawPerLine.getKey(subf).intValue(), 0, subf.r, subf.g, subf.b, subf.a, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile, false,
                                    new HashSet<String>(), false).get(0).getGraphicalData();
                }
                if (subf.equals(linkedDatFile.getDrawChainTail()))
                    linkedDatFile.setDrawChainTail(colouredSubfile);

                oldBefore.setNext(colouredSubfile);
                colouredSubfile.setNext(oldNext);
                Integer oldNumber = drawPerLine.getKey(subf);
                if (oldNumber != null)
                    drawPerLine.put(oldNumber, colouredSubfile);
                newSubfiles.add((GData1) colouredSubfile);
            }
            selectedSubfiles.clear();
            selectedSubfiles.addAll(newSubfiles);

            for (GData1 subf : selectedSubfiles) {
                Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
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

            syncWithTextEditors(true);
            updateUnsavedStatus();
        }
    }

    public Vector4f getSelectionCenter() {

        final Set<Vertex> objectVertices = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());
        objectVertices.addAll(selectedVertices);

        // 1. Object Based Selection

        for (GData2 line : selectedLines) {
            Vertex[] verts = lines.get(line);
            if (verts == null)
                continue;
            for (Vertex vertex : verts) {
                objectVertices.add(vertex);
            }
        }
        for (GData3 triangle : selectedTriangles) {
            Vertex[] verts = triangles.get(triangle);
            if (verts == null)
                continue;
            for (Vertex vertex : verts) {
                objectVertices.add(vertex);
            }
        }
        for (GData4 quad : selectedQuads) {
            Vertex[] verts = quads.get(quad);
            if (verts == null)
                continue;
            for (Vertex vertex : verts) {
                objectVertices.add(vertex);
            }
        }
        for (GData5 condline : selectedCondlines) {
            Vertex[] verts = condlines.get(condline);
            if (verts == null)
                continue;
            for (Vertex vertex : verts) {
                objectVertices.add(vertex);
            }
        }

        // 2. Subfile Based Selection
        if (!selectedSubfiles.isEmpty()) {

            for (GData1 subf : selectedSubfiles) {
                Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
                for (VertexInfo vertexInfo : vis) {
                    objectVertices.add(vertexInfo.getVertex());
                }
            }
        }
        if (!objectVertices.isEmpty()) {
            float x = 0f;
            float y = 0f;
            float z = 0f;
            for (Vertex vertex : objectVertices) {
                x = x + vertex.x;
                y = y + vertex.y;
                z = z + vertex.z;
            }
            float count = objectVertices.size();
            return new Vector4f(x / count, y / count, z / count, 1f);
        } else {
            return new Vector4f(0f, 0f, 0f, 1f);
        }
    }

    public Vector4f getVertexNormal(Vertex min) {
        Vector4f result = new Vector4f(0f, 0f, 0f, 0f);
        Set<VertexManifestation> linked = vertexLinkedToPositionInFile.get(min);

        for (VertexManifestation m : linked) {
            GData g = m.getGdata();
            Vector3f n;
            switch (g.type()) {
            case 3:
                GData3 g3 = (GData3) g;
                n = new Vector3f(g3.xn, g3.yn, g3.zn);
                if (n.lengthSquared() != 0) {
                    n.normalise();
                    result.set(n.x + result.x, n.y + result.y, n.z + result.z);
                }
                break;
            case 4:
                GData4 g4 = (GData4) g;
                n = new Vector3f(g4.xn, g4.yn, g4.zn);
                if (n.lengthSquared() != 0) {
                    n.normalise();
                    result.set(n.x + result.x, n.y + result.y, n.z + result.z);
                }
                break;
            }
        }
        if (result.lengthSquared() == 0)
            return new Vector4f(0f, 0f, 1f, 1f);
        result.normalise();
        result.setW(1f);
        return result;
    }

    public void setVertexAndNormal(float x, float y, float z, boolean negate, GData gd, int useCubeMapCache) {
        boolean useCache = useCubeMapCache > 0;
        // TODO Needs better caching since the connectivity information of TEXMAP data is unknown and the orientation of the normals can vary.
        Vector4f v;
        switch (gd.type()) {
        case 3:
            v = new Vector4f(x, y, z, 1f);
            Matrix4f.transform(((GData3) gd).parent.productMatrix, v, v);
            break;
        case 4:
            v = new Vector4f(x, y, z, 1f);
            Matrix4f.transform(((GData4) gd).parent.productMatrix, v, v);
            break;
        default:
            throw new AssertionError();
        }
        if (useCache) {
            float[] n;
            if ((n = vertexLinkedToNormalCACHE.get(new Vertex(v.x, v.y, v.z, false))) != null) {
                GL11.glNormal3f(-n[0], -n[1], -n[2]);
            } else {
                n = dataLinkedToNormalCACHE.get(gd);
                if (n != null) {
                    if (negate) {
                        GL11.glNormal3f(-n[0], -n[1], -n[2]);
                    } else {
                        GL11.glNormal3f(n[0], n[1], n[2]);
                    }
                }
            }
        } else {
            float[] n = dataLinkedToNormalCACHE.get(gd);
            if (n != null) {
                if (negate) {
                    GL11.glNormal3f(-n[0], -n[1], -n[2]);
                } else {
                    GL11.glNormal3f(n[0], n[1], n[2]);
                }
            }
        }
        GL11.glVertex3f(v.x, v.y, v.z);
    }

    public void setVertex(float x, float y, float z, GData gd, boolean useCache) {
        // TODO Needs better caching since the connectivity information of TEXMAP data is unknown and the orientation of the normals can vary.
        if (useCache) {
            Vector4f v;
            switch (gd.type()) {
            case 2:
                v = new Vector4f(x, y, z, 1f);
                Matrix4f.transform(((GData2) gd).parent.productMatrix, v, v);
                break;
            case 3:
                v = new Vector4f(x, y, z, 1f);
                Matrix4f.transform(((GData3) gd).parent.productMatrix, v, v);
                break;
            case 4:
                v = new Vector4f(x, y, z, 1f);
                Matrix4f.transform(((GData4) gd).parent.productMatrix, v, v);
                break;
            case 5:
                v = new Vector4f(x, y, z, 1f);
                Matrix4f.transform(((GData5) gd).parent.productMatrix, v, v);
                break;
            default:
                throw new AssertionError();
            }
            GL11.glVertex3f(v.x, v.y, v.z);
        } else {
            GL11.glVertex3f(x, y, z);
        }
    }

    public ArrayList<ParsingResult> checkForFlatScaling(GData1 ref) {
        ArrayList<ParsingResult> result = new ArrayList<ParsingResult>();

        Matrix4f tMatrix = (Matrix4f) ref.accurateLocalMatrix.getMatrix4f().invert();

        boolean plainOnX = true;
        boolean plainOnY = true;
        boolean plainOnZ = true;

        Set<VertexInfo> verts = lineLinkedToVertices.get(ref);
        if (verts == null) return result;
        for (VertexInfo vi : verts) {
            Vector4f vert = vi.vertex.toVector4f();
            vert.setX(vert.x / 1000f);
            vert.setY(vert.y / 1000f);
            vert.setZ(vert.z / 1000f);
            Vector4f vert2 = Matrix4f.transform(tMatrix, vert, null);

            if (plainOnX && Math.abs(vert2.x) > 0.001f) {
                plainOnX = false;
            }
            if (plainOnY && Math.abs(vert2.y) > 0.001f) {
                plainOnY = false;
            }
            if (plainOnZ && Math.abs(vert2.z) > 0.001f) {
                plainOnZ = false;
            }
            if (!plainOnX && !plainOnY && !plainOnZ) {
                return result;
            }
        }

        Matrix TMatrix2 = ref.accurateLocalMatrix;
        final BigDecimal lengthX =  plainOnX ? MathHelper.sqrt(TMatrix2.M00.multiply(TMatrix2.M00).add(TMatrix2.M01.multiply(TMatrix2.M01)).add(TMatrix2.M02.multiply(TMatrix2.M02))).subtract(BigDecimal.ONE).abs() : null;
        final BigDecimal lengthY =  plainOnY ? MathHelper.sqrt(TMatrix2.M10.multiply(TMatrix2.M10).add(TMatrix2.M11.multiply(TMatrix2.M11)).add(TMatrix2.M12.multiply(TMatrix2.M12))).subtract(BigDecimal.ONE).abs() : null;
        final BigDecimal lengthZ =  plainOnZ ? MathHelper.sqrt(TMatrix2.M20.multiply(TMatrix2.M20).add(TMatrix2.M21.multiply(TMatrix2.M21)).add(TMatrix2.M22.multiply(TMatrix2.M22))).subtract(BigDecimal.ONE).abs() : null;
        // Epsilon is 0.000001 / DATHeader default value is 0.0005
        final BigDecimal epsilon = new BigDecimal("0.000001"); //$NON-NLS-1$
        if (plainOnX && epsilon.compareTo(lengthX) < 0) {
            result.add(new ParsingResult(I18n.VM_FlatScaledX, "[W02] " + I18n.DATPARSER_Warning, ResultType.WARN)); //$NON-NLS-1$
        }
        if (plainOnY && epsilon.compareTo(lengthY) < 0) {
            result.add(new ParsingResult(I18n.VM_FlatScaledY, "[W03] " + I18n.DATPARSER_Warning, ResultType.WARN)); //$NON-NLS-1$
        }
        if (plainOnZ && epsilon.compareTo(lengthZ) < 0) {
            result.add(new ParsingResult(I18n.VM_FlatScaledZ, "[W04] " + I18n.DATPARSER_Warning, ResultType.WARN)); //$NON-NLS-1$
        }

        return result;
    }

    public void condlineToLine() {

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
            GData2 line = new GData2(g5.colourNumber, g5.r, g5.g, g5.b, g5.a, v[0], v[1], View.DUMMY_REFERENCE, linkedDatFile);
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

    public void lineToCondline() {

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

    public void merge(MergeTo mode, boolean syncWithTextEditor) {

        if (linkedDatFile.isReadOnly()) return;

        Vector3d newVertex = new Vector3d();
        Set<Vertex> originVerts = new TreeSet<Vertex>();

        if (mode != MergeTo.LAST_SELECTED) {
            originVerts.addAll(selectedVertices);
            for (GData2 g : selectedLines) {
                for (Vertex v : lines.get(g)) {
                    originVerts.add(v);
                }
            }
            for (GData3 g : selectedTriangles) {
                for (Vertex v : triangles.get(g)) {
                    originVerts.add(v);
                }
            }
            for (GData4 g : selectedQuads) {
                for (Vertex v : quads.get(g)) {
                    originVerts.add(v);
                }
            }
            for (GData5 g : selectedCondlines) {
                for (Vertex v : condlines.get(g)) {
                    originVerts.add(v);
                }
            }
        }

        switch (mode) {
        case AVERAGE:
            if (originVerts.size() == 0) return;
            for (Vertex v : originVerts) {
                newVertex = Vector3d.add(newVertex, new Vector3d(v));
            }
            final BigDecimal size = new BigDecimal(originVerts.size());
            newVertex.setX(newVertex.X.divide(size, Threshold.mc));
            newVertex.setY(newVertex.Y.divide(size, Threshold.mc));
            newVertex.setZ(newVertex.Z.divide(size, Threshold.mc));
            break;
        case LAST_SELECTED:
            if (lastSelectedVertex == null || !vertexLinkedToPositionInFile.containsKey(lastSelectedVertex)) return;
            newVertex = new Vector3d(lastSelectedVertex);
            lastSelectedVertex = null;
            break;
        case NEAREST_EDGE:
        case NEAREST_FACE:
            if (originVerts.size() == 0) return;
            {
                // This is a little bit more complex.
                // First, I had to extend the selection to adjacent data,
                // so the nearest edge will not be adjacent to the (selected) origin vertex

                for (Vertex v : originVerts) {
                    for (VertexManifestation mani : vertexLinkedToPositionInFile.get(v)) {
                        GData gd = mani.getGdata();
                        switch (gd.type()) {
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
                            continue;
                        }
                        selectedData.add(gd);
                    }

                    // Then invert the selection, so that getMinimalDistanceVertexToLines() will snap on the target

                    selectInverse(new SelectorSettings());

                    // And using changeVertexDirectFast() to do the merge
                    boolean modified = false;
                    if (mode == MergeTo.NEAREST_EDGE) {
                        for (Vertex vertex : originVerts) {
                            final Object[] target = getMinimalDistanceVerticesToLines(vertex, false);
                            modified = changeVertexDirectFast(vertex, (Vertex) target[2], true) || modified;
                            // And split at target position!
                            modified = split((Vertex) target[0], (Vertex) target[1], (Vertex) target[2]) || modified;
                        }
                    } else {
                        for (Vertex vertex : originVerts) {
                            final Vertex target = getMinimalDistanceVertexToSurfaces(vertex);
                            modified = changeVertexDirectFast(vertex, target, true) || modified;
                        }
                    }
                    clearSelection();

                    if (modified) {
                        IdenticalVertexRemover.removeIdenticalVertices(this, linkedDatFile, false, true);
                        clearSelection();
                        setModified_NoSync();
                    }
                }
            }
            if (syncWithTextEditor) {
                syncWithTextEditors(true);
            }
            return;
        case NEAREST_VERTEX:
            if (originVerts.size() == 0) return;
            {
                float minDist = Float.MAX_VALUE;
                Set<Vertex> allVerticesMinusSelection = new TreeSet<Vertex>();
                allVerticesMinusSelection.addAll(getVertices());
                allVerticesMinusSelection.removeAll(originVerts);
                clearSelection();
                for (Vertex vertex2 : originVerts) {
                    selectedVertices.clear();
                    selectedVertices.add(vertex2);
                    Vertex minVertex = new Vertex(0f, 0f, 0f);
                    Vector4f next = vertex2.toVector4fm();
                    for (Vertex vertex : allVerticesMinusSelection) {
                        Vector4f sub = Vector4f.sub(next, vertex.toVector4fm(), null);
                        float d2 = sub.lengthSquared();
                        if (d2 < minDist) {
                            minVertex = vertex;
                            minDist = d2;
                        }
                    }
                    newVertex = new Vector3d(minVertex);
                    Merger.mergeTo(new Vertex(newVertex), this, linkedDatFile, false);
                }
                clearSelection();
                setModified_NoSync();
            }
            if (syncWithTextEditor) {
                syncWithTextEditors(true);
            }
            return;
        default:
            return;
        }
        Merger.mergeTo(new Vertex(newVertex), this, linkedDatFile, syncWithTextEditor);
        clearSelection();
        validateState();
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
        selectSubfiles(null, null, false);
        if (syncWithTextEditor) {
            setModified(true, true);
        } else {
            setModified_NoSync();
        }
    }

    public void setXyzOrTranslateOrTransform(Vertex target, Vertex pivot, TransformationMode tm, boolean x, boolean y, boolean z, boolean moveAdjacentData, boolean syncWithTextEditors) {
        if (linkedDatFile.isReadOnly())
            return;

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
            if (sx.compareTo(BigDecimal.ZERO) == 0) sx = new BigDecimal("0.000000001"); //$NON-NLS-1$
            if (sy.compareTo(BigDecimal.ZERO) == 0) sy = new BigDecimal("0.000000001"); //$NON-NLS-1$
            if (sz.compareTo(BigDecimal.ZERO) == 0) sz = new BigDecimal("0.000000001"); //$NON-NLS-1$
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

        final Set<Vertex> singleVertices = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());

        final HashSet<GData0> effSelectedVertices = new HashSet<GData0>();
        final HashSet<GData2> effSelectedLines = new HashSet<GData2>();
        final HashSet<GData3> effSelectedTriangles = new HashSet<GData3>();
        final HashSet<GData4> effSelectedQuads = new HashSet<GData4>();
        final HashSet<GData5> effSelectedCondlines = new HashSet<GData5>();

        selectedData.clear();

        // 0. Deselect selected subfile data (for whole selected subfiles)
        for (GData1 subf : selectedSubfiles) {
            Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
            for (VertexInfo vertexInfo : vis) {
                GData g = vertexInfo.getLinkedData();
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
            {
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
                            if (val == 1 && !idCheck) {
                                effSelectedVertices.add(meta);
                            }
                            break;
                        case 2:
                            GData2 line = (GData2) g;
                            isPureSubfileVertex = isPureSubfileVertex && !line.parent.equals(View.DUMMY_REFERENCE);
                            break;
                        case 3:
                            GData3 triangle = (GData3) g;
                            isPureSubfileVertex = isPureSubfileVertex && !triangle.parent.equals(View.DUMMY_REFERENCE);
                            break;
                        case 4:
                            GData4 quad = (GData4) g;
                            isPureSubfileVertex = isPureSubfileVertex && !quad.parent.equals(View.DUMMY_REFERENCE);
                            break;
                        case 5:
                            GData5 condline = (GData5) g;
                            isPureSubfileVertex = isPureSubfileVertex && !condline.parent.equals(View.DUMMY_REFERENCE);
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
                    singleVertices.add(v);
                }
            }

            singleVertices.addAll(objectVertices);
            singleVertices.addAll(selectedVertices);

            // 3. Set XYZ of the selected data
            if (!singleVertices.isEmpty()) {
                setModified_NoSync();
            }
            final Matrix forward = Matrix.mul(View.ACCURATE_ID.translate(new BigDecimal[] { pivot.X.negate(), pivot.Y.negate(), pivot.Z.negate() }), View.ACCURATE_ID);
            final Matrix backward = Matrix.mul(View.ACCURATE_ID.translate(new BigDecimal[] { pivot.X, pivot.Y, pivot.Z }), View.ACCURATE_ID);

            if (tm == TransformationMode.ROTATE || tm == TransformationMode.SCALE) {
                selectedVertices.addAll(singleVertices);
                transform(new HashSet<GData>(), selectedVertices, forward, true, true);
                transform(new HashSet<GData>(), selectedVertices, transformation, true, true);
                transform(new HashSet<GData>(), selectedVertices, backward, true, true);
            }
            selectedData.clear();

            for (Vertex vOld : singleVertices) {
                switch (tm) {
                case ROTATE:
                case SCALE:
                    continue;
                case SET:
                    break;
                case TRANSLATE:
                    target = new Vertex(vOld.X.add(offset.X), vOld.Y.add(offset.Y), vOld.Z.add(offset.Z));
                    break;
                }
                Vertex vNew;
                if (x) {
                    if (y) {
                        if (z) {
                            vNew = new Vertex(target.X, target.Y, target.Z);
                        } else {
                            vNew = new Vertex(target.X, target.Y, vOld.Z);
                        }
                    } else {
                        if (z) {
                            vNew = new Vertex(target.X, vOld.Y, target.Z);
                        } else {
                            vNew = new Vertex(target.X, vOld.Y, vOld.Z);
                        }
                    }
                } else {
                    if (y) {
                        if (z) {
                            vNew = new Vertex(vOld.X, target.Y, target.Z);
                        } else {
                            vNew = new Vertex(vOld.X, target.Y, vOld.Z);
                        }
                    } else {
                        if (z) {
                            vNew = new Vertex(vOld.X, vOld.Y, target.Z);
                        } else {
                            vNew = new Vertex(vOld.X, vOld.Y, vOld.Z);
                        }
                    }
                }
                changeVertexDirectFast(vOld, vNew, moveAdjacentData);
                selectedVertices.add(vNew);
            }

            if ((tm == TransformationMode.TRANSLATE || tm == TransformationMode.SCALE || tm == TransformationMode.ROTATE) && !selectedSubfiles.isEmpty()) {
                setModified_NoSync();
                for (GData1 s : new HashSet<GData1>(selectedSubfiles)) {
                    Matrix M = Matrix.mul(forward, s.accurateLocalMatrix);
                    M = Matrix.mul(transformation, M);
                    M = Matrix.mul(backward, M);
                    transformSubfile(s, M, false, false);
                }
            } else {
                selectedSubfiles.clear();
            }

            if (isModified()) {
                for(Iterator<GData2> it = selectedLines.iterator();it.hasNext();){
                    if (!exist(it.next())) it.remove();
                }
                for(Iterator<GData3> it = selectedTriangles.iterator();it.hasNext();){
                    if (!exist(it.next())) it.remove();
                }
                for(Iterator<GData4> it = selectedQuads.iterator();it.hasNext();){
                    if (!exist(it.next())) it.remove();
                }
                for(Iterator<GData5> it = selectedCondlines.iterator();it.hasNext();){
                    if (!exist(it.next())) it.remove();
                }
                selectedData.addAll(selectedLines);
                selectedData.addAll(selectedTriangles);
                selectedData.addAll(selectedQuads);
                selectedData.addAll(selectedCondlines);
                selectedData.addAll(selectedSubfiles);

                IdenticalVertexRemover.removeIdenticalVertices(this, linkedDatFile, false, true);

                if (syncWithTextEditors) syncWithTextEditors(true);
                updateUnsavedStatus();
            }
            selectedVertices.retainAll(vertexLinkedToPositionInFile.keySet());
        }
    }

    public void fixTjunctions() {

        linkedDatFile.setDrawSelection(false);

        final Set<Vertex> verticesToProcess = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());
        final Set<Vertex> verticesToSelect = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());

        if (selectedVertices.isEmpty()) {
            verticesToProcess.addAll(vertexLinkedToPositionInFile.keySet());
        } else {
            verticesToProcess.addAll(selectedVertices);
        }

        clearSelection();

        final int[] TjunctionCount = new int[1];
        try
        {
            new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
            {
                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                {
                    try
                    {
                        monitor.beginTask(I18n.E3D_Tjunction, verticesToProcess.size());

                        final AtomicBoolean a = new AtomicBoolean();

                        for (final Vertex v : verticesToProcess) {
                            if (monitor.isCanceled()) break;
                            if (!vertexLinkedToPositionInFile.containsKey(v)) continue;
                            Display.getDefault().asyncExec(new Runnable() {
                                @Override
                                public void run() {
                                    clearSelection2();
                                    if (isTjunctionCandidate(v)) {
                                        clearSelection2();
                                        selectedVertices.add(v);
                                        verticesToSelect.add(v);
                                        merge(MergeTo.NEAREST_EDGE, false);
                                        TjunctionCount[0]++;
                                    }
                                    monitor.worked(1);
                                    a.set(true);
                                }});
                            while (!a.get()) {
                                Thread.sleep(10);
                            }
                            a.set(false);
                        }
                    } catch (Exception ex) {
                        NLogger.error(getClass(), ex);
                    } finally {
                        monitor.done();
                    }
                }
            });
        }
        catch (InvocationTargetException consumed) {
        } catch (InterruptedException consumed) {
        }

        clearSelection2();

        MessageBox messageBox = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_INFORMATION | SWT.OK);
        messageBox.setText(I18n.DIALOG_Info);
        Object[] messageArguments = {TjunctionCount[0]};
        MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
        formatter.setLocale(MyLanguage.LOCALE);
        formatter.applyPattern(I18n.E3D_TjunctionCount);
        messageBox.setMessage(formatter.format(messageArguments));
        messageBox.open();

        syncWithTextEditors(true);
        setModified(true, true);

        selectedVertices.addAll(verticesToSelect);
        linkedDatFile.setDrawSelection(true);

    }

    private boolean isTjunctionCandidate(Vertex v) {

        HashSet<GData> surfs = getLinkedSurfaces(v);

        int surfCount = surfs.size();
        if (surfCount == 0) {
            return false;
        }

        TreeSet<Vertex> verts = new TreeSet<Vertex>();
        TreeSet<Vertex> verts2 = new TreeSet<Vertex>();

        for (GData g : surfs) {
            switch (g.type()) {
            case 3:
                GData3 g3 = (GData3) g;
                Vertex v1 = new Vertex(g3.X1, g3.Y1, g3.Z1);
                Vertex v2 = new Vertex(g3.X2, g3.Y2, g3.Z2);
                Vertex v3 = new Vertex(g3.X3, g3.Y3, g3.Z3);
                if (verts2.contains(v1)) verts.add(v1);
                if (verts2.contains(v2)) verts.add(v2);
                if (verts2.contains(v3)) verts.add(v3);
                verts2.add(v1);
                verts2.add(v2);
                verts2.add(v3);
                break;
            case 4:
                GData4 g4 = (GData4) g;
                Vertex v4 = new Vertex(g4.X1, g4.Y1, g4.Z1);
                Vertex v5 = new Vertex(g4.X2, g4.Y2, g4.Z2);
                Vertex v6 = new Vertex(g4.X3, g4.Y3, g4.Z3);
                Vertex v7 = new Vertex(g4.X4, g4.Y4, g4.Z4);
                if (verts2.contains(v4)) verts.add(v4);
                if (verts2.contains(v5)) verts.add(v5);
                if (verts2.contains(v6)) verts.add(v6);
                if (verts2.contains(v7)) verts.add(v7);
                verts2.add(v4);
                verts2.add(v5);
                verts2.add(v6);
                verts2.add(v7);
                break;
            default:
            }
        }

        int vertCount = verts.size();

        if (surfCount + 1 != vertCount) {
            for (VertexManifestation mani : vertexLinkedToPositionInFile.get(v)) {
                GData gd = mani.getGdata();
                switch (gd.type()) {
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
                    continue;
                }
                selectedData.add(gd);
            }

            // Then invert the selection, so that getMinimalDistanceVertexToLines() will snap on the target

            selectInverse(new SelectorSettings());

            double result = (double) getMinimalDistanceVerticesToLines(v, false)[3];
            System.out.println(result);
            return result < 1.0;
        } else {
            return false;
        }
    }
}
