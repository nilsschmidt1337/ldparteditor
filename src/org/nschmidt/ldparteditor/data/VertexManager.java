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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
import org.nschmidt.ldparteditor.helpers.composite3d.IntersectorSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.IsecalcSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.PerspectiveCalculator;
import org.nschmidt.ldparteditor.helpers.composite3d.RectifierSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.SelectorSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.SymSplitterSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.UnificatorSettings;
import org.nschmidt.ldparteditor.helpers.math.HashBiMap;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeTreeMap;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.helpers.math.Vector3dd;
import org.nschmidt.ldparteditor.helpers.math.Vector3dh;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.text.DatParser;
import org.nschmidt.ldparteditor.text.HeaderState;
import org.nschmidt.ldparteditor.text.StringHelper;

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

    public synchronized void showHidden() {
        for (GData gd : dataToHide) {
            gd.show();
        }
        dataToHide.clear();
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

    public void hideSelection() {
        for (GData1 data : selectedSubfiles) {
            hide(data);
        }
        for (GData2 data : selectedLines) {
            hide(data);
        }
        for (GData3 data : selectedTriangles) {
            hide(data);
        }
        for (GData4 data : selectedQuads) {
            hide(data);
        }
        for (GData5 data : selectedCondlines) {
            hide(data);
        }
        for (Vertex vert : selectedVertices) {
            Set<VertexManifestation> m = vertexLinkedToPositionInFile.get(vert);
            boolean isHidden = true;
            for (VertexManifestation vm : m) {
                if (vm.getGdata().type() != 0 && vm.getGdata().visible) {
                    isHidden = false;
                    break;
                }
            }
            if (isHidden)
                hiddenVertices.add(vert);
        }
        clearSelection();
    }

    private void hide(GData gdata) {
        gdata.hide();
        hiddenData.add(gdata);
    }

    public void showAll() {
        for (GData ghost : hiddenData) {
            ghost.show();
        }
        hiddenVertices.clear();
        hiddenData.clear();
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

    public void isecalc(IsecalcSettings is) {

        if (linkedDatFile.isReadOnly()) return;

        final ArrayList<GData2> newLines = new ArrayList<GData2>();

        final Set<GData2> linesToDelete = new HashSet<GData2>();
        final Set<GData5> clinesToDelete = new HashSet<GData5>();

        final ArrayList<GData> surfsToParse;

        if (is.getScope() == 0) {
            surfsToParse = new ArrayList<GData>(triangles.size() + quads.size());
            surfsToParse.addAll(triangles.keySet());
            surfsToParse.addAll(quads.keySet());
        } else {
            surfsToParse = new ArrayList<GData>(selectedTriangles.size() + selectedQuads.size());
            surfsToParse.addAll(selectedTriangles);
            surfsToParse.addAll(selectedQuads);
        }

        clearSelection();

        final int surfsSize = surfsToParse.size();

        try
        {
            new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
            {
                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                {
                    try
                    {
                        monitor.beginTask(I18n.VM_SearchIntersection, IProgressMonitor.UNKNOWN);
                        for(int i = 0; i < surfsSize; i++) {
                            /* Check if the monitor has been canceled */
                            if (monitor.isCanceled()) break;
                            NLogger.debug(getClass(), "Checked {0}  of {1} surfaces.", i + 1, surfsSize); //$NON-NLS-1$
                            for(int j = i + 1; j < surfsSize; j++) {
                                GData s1 = surfsToParse.get(i);
                                GData s2 = surfsToParse.get(j);
                                if (isConnected2(s1, s2)) continue;
                                newLines.addAll(intersectionLines(clinesToDelete, linesToDelete, s1, s2));
                            }
                        }
                    }
                    finally
                    {
                        monitor.done();
                    }
                }
            });
        }
        catch (InvocationTargetException consumed) {
        } catch (InterruptedException consumed) {
        }

        if (!newLines.isEmpty()) {

            // Remove zero length lines
            BigDecimal EPSILON = new BigDecimal(".0001"); //$NON-NLS-1$
            for (Iterator<GData2> li = newLines.iterator(); li.hasNext();) {
                GData2 l = li.next();
                BigDecimal dx = l.X1.subtract(l.X2);
                BigDecimal dy = l.Y1.subtract(l.Y2);
                BigDecimal dz = l.Z1.subtract(l.Z2);
                BigDecimal len = dx.multiply(dx).add(dy.multiply(dy)).add(dz.multiply(dz));
                if (len.compareTo(EPSILON) <= 0) {
                    remove(l);
                    li.remove();
                }
            }

            final int lineCount = newLines.size();
            final BigDecimal SMALL = new BigDecimal("0.001"); //$NON-NLS-1$
            final BigDecimal SMALLANGLE = new BigDecimal("0.00001"); //$NON-NLS-1$
            final Vector3d zero = new Vector3d();

            // Merge lines with same directions
            int[] colin = new int[lineCount];
            int distline = 1;
            int flag = 0;
            for (int i=0; i < lineCount; i++)
            {
                if(colin[i] == 0)
                {
                    for (int j= i + 1; j < lineCount; j++)
                    {
                        flag=0;
                        Vector3d p11 = new Vector3d(newLines.get(i).X1, newLines.get(i).Y1, newLines.get(i).Z1);
                        Vector3d p12 = new Vector3d(newLines.get(i).X2, newLines.get(i).Y2, newLines.get(i).Z2);
                        Vector3d p21 = new Vector3d(newLines.get(j).X1, newLines.get(j).Y1, newLines.get(j).Z1);
                        Vector3d p22 = new Vector3d(newLines.get(j).X2, newLines.get(j).Y2, newLines.get(j).Z2);
                        Vector3d line1 = Vector3d.sub(p12, p11);
                        Vector3d line2 = Vector3d.sub(p22, p21);
                        Vector3d temp = Vector3d.cross(line1, line2);
                        BigDecimal angle = Vector3d.manhattan(temp, zero).divide(Vector3d.manhattan(p12, p11), Threshold.mc).divide(Vector3d.manhattan(p22, p21), Threshold.mc);
                        if (angle.compareTo(SMALLANGLE) < 0)
                        {
                            colin[i] = distline;
                            colin[j] = distline;
                            flag=1;
                        }
                    }
                    if((flag = 1) == 1) distline++;
                }
            }
            // printf("%d distinct direction(s)\n", distline-1);

            for (int i=0; i<lineCount-1; i++)
            {
                if(colin[i] > 0)
                {
                    flag=1;
                    while (flag==1)
                    {
                        flag=0;
                        for (int j=i+1; j<lineCount; j++)
                        {
                            if(colin[i]==colin[j])
                            {
                                Vector3d p11 = new Vector3d(newLines.get(i).X1, newLines.get(i).Y1, newLines.get(i).Z1);
                                Vector3d p12 = new Vector3d(newLines.get(i).X2, newLines.get(i).Y2, newLines.get(i).Z2);
                                Vector3d p21 = new Vector3d(newLines.get(j).X1, newLines.get(j).Y1, newLines.get(j).Z1);
                                Vector3d p22 = new Vector3d(newLines.get(j).X2, newLines.get(j).Y2, newLines.get(j).Z2);
                                if(Vector3d.manhattan(p11, p21).compareTo(SMALL) < 0 ||
                                        Vector3d.manhattan(p11, p22).compareTo(SMALL) < 0 ||
                                        Vector3d.manhattan(p12, p22).compareTo(SMALL) < 0 ||
                                        Vector3d.manhattan(p12, p21).compareTo(SMALL) < 0)
                                {
                                    int a=1,b=0;
                                    BigDecimal max, cur;
                                    max = Vector3d.manhattan(p11, p21);
                                    if ((cur = Vector3d.manhattan(p11, p22)).compareTo(max) > 0)
                                    {
                                        a=1; b=1;
                                        max = cur;
                                    }
                                    if ((cur = Vector3d.manhattan(p12, p21)).compareTo(max) > 0)
                                    {
                                        a=0; b=0;
                                        max = cur;
                                    }
                                    if ((cur = Vector3d.manhattan(p12, p22)).compareTo(max) > 0)
                                    {
                                        a=0; b=1;
                                    }
                                    GData2 l1 = newLines.get(i);
                                    GData2 l2 = newLines.get(j);
                                    GColour c = new GColour(24, View.line_Colour_r[0], View.line_Colour_g[0], View.line_Colour_b[0], 1f);
                                    GData2 nl;
                                    // SET(OutLine[i][a], OutLine[j][b]);
                                    if (a == 1) {
                                        if (b == 1) {
                                            nl = new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), l1.X1, l1.Y1, l1.Z1, l2.X2, l2.Y2, l2.Z2, View.DUMMY_REFERENCE, linkedDatFile);
                                        } else {
                                            nl = new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), l1.X1, l1.Y1, l1.Z1, l2.X1, l2.Y1, l2.Z1, View.DUMMY_REFERENCE, linkedDatFile);
                                        }
                                    } else {
                                        if (b == 1) {
                                            nl = new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), l2.X2, l2.Y2, l2.Z2, l1.X2, l1.Y2, l1.Z2, View.DUMMY_REFERENCE, linkedDatFile);
                                        } else {
                                            nl = new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), l2.X1, l2.Y1, l2.Z1, l1.X2, l1.Y2, l1.Z2, View.DUMMY_REFERENCE, linkedDatFile);
                                        }
                                    }
                                    remove(l1);
                                    newLines.remove(i);
                                    newLines.add(i, nl);
                                    colin[j]=-1;
                                    flag = 1;
                                }
                            }
                        }
                    }
                }
            }

            // Remove invalid collinear lines
            {
                int counter = 0;
                for (Iterator<GData2> li = newLines.iterator(); li.hasNext();) {
                    GData2 l = li.next();
                    if (colin[counter] < 0) {
                        remove(l);
                        li.remove();
                    }
                    counter++;
                }
            }

            // Append the lines
            for (GData2 line : newLines) {
                linkedDatFile.addToTail(line);
            }

            // Round to 6 decimal places

            selectedLines.addAll(newLines);
            selectedData.addAll(selectedLines);

            roundSelection(6, 10, true, false);

            clearSelection();
            setModified_NoSync();
        }

        selectedLines.addAll(linesToDelete);
        selectedCondlines.addAll(clinesToDelete);
        selectedData.addAll(selectedLines);
        selectedData.addAll(selectedCondlines);
        delete(false, false);

        if (isModified()) {
            setModified(true, true);
        }

        validateState();
    }

    /**
     * FOR ISECALC/INTERSECTOR ONLY
     * @param p
     * @param q
     * @param a
     * @param b
     * @param c
     * @param r
     * @return
     */
    private boolean intersectLineTriangle(Vertex p, Vertex q, Vertex a, Vertex b, Vertex c, Vector3d r) {
        final BigDecimal TOLERANCE = new BigDecimal("0.00001"); //$NON-NLS-1$ .00001
        final BigDecimal ZEROT = new BigDecimal("-0.00001"); //$NON-NLS-1$
        final BigDecimal ONET = new BigDecimal("1.00001"); //$NON-NLS-1$
        BigDecimal diskr = BigDecimal.ZERO;
        BigDecimal inv_diskr = BigDecimal.ZERO;
        Vector3d vert0 = new Vector3d(a);
        Vector3d vert1 = new Vector3d(b);
        Vector3d vert2 = new Vector3d(c);
        Vector3d corner1 = Vector3d.sub(vert1, vert0);
        Vector3d corner2 = Vector3d.sub(vert2, vert0);
        Vector3d orig = new Vector3d(p);
        Vector3d dir = Vector3d.sub(new Vector3d(q), orig);
        BigDecimal len = dir.normalise(dir);
        Vector3d pvec = Vector3d.cross(dir, corner2);
        diskr = Vector3d.dotP(corner1, pvec);
        if (diskr.abs().compareTo(TOLERANCE) < 0)
            return false;
        inv_diskr = BigDecimal.ONE.divide(diskr, Threshold.mc);
        Vector3d tvec = Vector3d.sub(orig, vert0);
        BigDecimal u = Vector3d.dotP(tvec, pvec).multiply(inv_diskr);
        if (u.compareTo(ZEROT) < 0 || u.compareTo(ONET) > 0)
            return false;
        Vector3d qvec = Vector3d.cross(tvec, corner1);
        BigDecimal v = Vector3d.dotP(dir, qvec).multiply(inv_diskr);
        if (v.compareTo(ZEROT) < 0 || u.add(v).compareTo(ONET) > 0)
            return false;
        BigDecimal t = Vector3d.dotP(corner2, qvec).multiply(inv_diskr);
        if (t.compareTo(ZEROT) < 0 || t.compareTo(len.add(TOLERANCE)) > 0)
            return false;
        r.setX(orig.X.add(dir.X.multiply(t)));
        r.setY(orig.Y.add(dir.Y.multiply(t)));
        r.setZ(orig.Z.add(dir.Z.multiply(t)));
        return true;
    }

    private boolean intersectLineTriangleSuperFast(Vector3dd q, Vector3dd q2, Vector3d d, Vector3dd p2, Vector3d c, Vector3d dir, BigDecimal len) {
        BigDecimal diskr = BigDecimal.ZERO;
        BigDecimal inv_diskr = BigDecimal.ZERO;
        Vector3d vert0 = d;
        Vector3d vert1 = p2;
        Vector3d vert2 = c;
        Vector3d corner1 = Vector3d.sub(vert1, vert0);
        Vector3d corner2 = Vector3d.sub(vert2, vert0);
        Vector3d orig = q;
        Vector3d pvec = Vector3d.cross(dir, corner2);
        diskr = Vector3d.dotP(corner1, pvec);
        if (diskr.abs().compareTo(TOLERANCE) <= 0)
            return false;
        inv_diskr = BigDecimal.ONE.divide(diskr, Threshold.mc);
        Vector3d tvec = Vector3d.sub(orig, vert0);
        BigDecimal u = Vector3d.dotP(tvec, pvec).multiply(inv_diskr);
        if (u.compareTo(ZEROT) < 0 || u.compareTo(ONET) > 0)
            return false;
        Vector3d qvec = Vector3d.cross(tvec, corner1);
        BigDecimal v = Vector3d.dotP(dir, qvec).multiply(inv_diskr);
        if (v.compareTo(ZEROT) < 0 || u.add(v).compareTo(ONET) > 0)
            return false;
        BigDecimal t = Vector3d.dotP(corner2, qvec).multiply(inv_diskr);
        if (t.compareTo(ZEROT) < 0 || t.compareTo(len.add(TOLERANCE)) > 0)
            return false;
        return true;
    }

    private HashSet<GData2> intersectionLines(final Set<GData5> clinesToDelete, final Set<GData2> linesToDelete, GData g1, GData g2) {

        GColour c = new GColour(24, View.line_Colour_r[0], View.line_Colour_g[0], View.line_Colour_b[0], 1f);

        HashSet<GData2> result = new HashSet<GData2>();
        HashSet<Vector3d> points = new HashSet<Vector3d>();

        int t1 = g1.type();
        int t2 = g2.type();

        if (t1 == 3 && t2 == 3) {
            Vertex[] v1 = triangles.get(g1);
            Vertex[] v2 = triangles.get(g2);
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[0], v1[1], v2[0], v2[1], v2[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[1], v1[2], v2[0], v2[1], v2[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[2], v1[0], v2[0], v2[1], v2[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[0], v2[1], v1[0], v1[1], v1[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[1], v2[2], v1[0], v1[1], v1[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[2], v2[0], v1[0], v1[1], v1[2], p)) {
                    points.add(p);
                }
            }
        } else if (t1 == 4 && t2 == 4) {
            Vertex[] v1 = quads.get(g1);
            Vertex[] v2 = quads.get(g2);
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[0], v1[1], v2[0], v2[1], v2[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[1], v1[2], v2[0], v2[1], v2[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[2], v1[3], v2[0], v2[1], v2[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[3], v1[0], v2[0], v2[1], v2[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[0], v1[1], v2[2], v2[3], v2[0], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[1], v1[2], v2[2], v2[3], v2[0], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[2], v1[3], v2[2], v2[3], v2[0], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[3], v1[0], v2[2], v2[3], v2[0], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[0], v2[1], v1[0], v1[1], v1[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[1], v2[2], v1[0], v1[1], v1[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[2], v2[3], v1[0], v1[1], v1[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[3], v2[0], v1[0], v1[1], v1[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[0], v2[1], v1[2], v1[3], v1[0], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[1], v2[2], v1[2], v1[3], v1[0], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[2], v2[3], v1[2], v1[3], v1[0], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[3], v2[0], v1[2], v1[3], v1[0], p)) {
                    points.add(p);
                }
            }
        }

        if (t1 == 4 && t2 == 3) {
            GData g3 = g1;
            g1 = g2;
            g2 = g3;
            t1 = 3;
            t2 = 4;
        }

        if (t1 == 3 && t2 == 4) {
            Vertex[] v1 = triangles.get(g1);
            Vertex[] v2 = quads.get(g2);
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[0], v1[1], v2[0], v2[1], v2[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[1], v1[2], v2[0], v2[1], v2[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[2], v1[0], v2[0], v2[1], v2[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[0], v1[1], v2[2], v2[3], v2[0], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[1], v1[2], v2[2], v2[3], v2[0], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v1[2], v1[0], v2[2], v2[3], v2[0], p)) {
                    points.add(p);
                }
            }

            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[0], v2[1], v1[0], v1[1], v1[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[1], v2[2], v1[0], v1[1], v1[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[2], v2[3], v1[0], v1[1], v1[2], p)) {
                    points.add(p);
                }
            }
            {
                Vector3d p = new Vector3d();
                if (intersectLineTriangle(v2[3], v2[0], v1[0], v1[1], v1[2], p)) {
                    points.add(p);
                }
            }
        }

        final BigDecimal EPSILON = new BigDecimal(".0001"); //$NON-NLS-1$
        for(Iterator<Vector3d> i = points.iterator(); i.hasNext(); ) {
            Vector3d p1 = i.next();
            for (Vector3d p2 : points) {
                if (!p1.equals(p2)) {
                    Vector3d p3 = Vector3d.sub(p1, p2);
                    BigDecimal md = p3.X.multiply(p3.X).add(p3.Y.multiply(p3.Y)).add(p3.Z.multiply(p3.Z));
                    if (md.compareTo(EPSILON) <= 0) {
                        i.remove();
                        break;
                    }
                }
            }
        }
        if (points.size() == 4) {
            ArrayList<Vector3d> points2 = new ArrayList<Vector3d>();
            points2.addAll(points);
            result.add(new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), points2.get(0).X, points2.get(0).Y, points2.get(0).Z, points2.get(1).X, points2.get(1).Y, points2.get(1).Z, View.DUMMY_REFERENCE, linkedDatFile));
            result.add(new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), points2.get(1).X, points2.get(1).Y, points2.get(1).Z, points2.get(2).X, points2.get(2).Y, points2.get(2).Z, View.DUMMY_REFERENCE, linkedDatFile));
            result.add(new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), points2.get(2).X, points2.get(2).Y, points2.get(2).Z, points2.get(3).X, points2.get(3).Y, points2.get(3).Z, View.DUMMY_REFERENCE, linkedDatFile));
            result.add(new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), points2.get(0).X, points2.get(0).Y, points2.get(0).Z, points2.get(3).X, points2.get(3).Y, points2.get(3).Z, View.DUMMY_REFERENCE, linkedDatFile));
        } else if (points.size() == 3) {
            ArrayList<Vector3d> points2 = new ArrayList<Vector3d>();
            points2.addAll(points);
            result.add(new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), points2.get(0).X, points2.get(0).Y, points2.get(0).Z, points2.get(1).X, points2.get(1).Y, points2.get(1).Z, View.DUMMY_REFERENCE, linkedDatFile));
            result.add(new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), points2.get(2).X, points2.get(2).Y, points2.get(2).Z, points2.get(1).X, points2.get(1).Y, points2.get(1).Z, View.DUMMY_REFERENCE, linkedDatFile));
            result.add(new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), points2.get(0).X, points2.get(0).Y, points2.get(0).Z, points2.get(2).X, points2.get(2).Y, points2.get(2).Z, View.DUMMY_REFERENCE, linkedDatFile));
        } else if (points.size() == 2) {
            ArrayList<Vector3d> points2 = new ArrayList<Vector3d>();
            points2.addAll(points);
            result.add(new GData2(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), points2.get(0).X, points2.get(0).Y, points2.get(0).Z, points2.get(1).X, points2.get(1).Y, points2.get(1).Z, View.DUMMY_REFERENCE, linkedDatFile));
        }

        return result;
    }

    private boolean intersectLineLineSegmentUnidirectional(Vector3dd p, Vector3dd p2, Vector3dd q, Vector3dd q2) {


        Vector3d sp = Vector3d.sub(p2, p);
        Vector3d sq = Vector3d.sub(q2, q);
        Vector3d c = Vector3d.add(Vector3d.cross(sp, sq), p);
        Vector3d d = Vector3d.sub(p, Vector3d.cross(sp, sq));

        return intersectLineTriangle(new Vertex(q), new Vertex(q2), new Vertex(d), new Vertex(p2), new Vertex(c), c);

    }

    private boolean intersectLineLineSegmentUnidirectionalFast(Vector3dd p, Vector3dd p2, Vector3d sp, Vector3d dir, BigDecimal len, Vector3dd q, Vector3dd q2) {

        Vector3d sq = Vector3d.sub(q2, q);
        //        Vector3d c = Vector3d.add(Vector3d.cross(sp, sq), p, null);
        //        Vector3d d = Vector3d.sub(p, Vector3d.cross(sp, sq));
        //
        //        return intersectLineTriangleSuperFast(q, q2, d, p2, c);

        Vector3d cross = Vector3d.cross(sq, sp);
        Vector3d c = Vector3d.add(cross, q);
        Vector3d d = Vector3d.sub(q, cross);

        return intersectLineTriangleSuperFast(p, q2, d, q2, c, dir, len);

    }

    private Vector3d intersectLineLineSegmentUnidirectional2(Vector3dd p, Vector3dd p2, Vector3dd q, Vector3dd q2) {


        Vector3d sp = Vector3d.sub(p2, p);
        Vector3d sq = Vector3d.sub(q2, q);
        Vector3d c = Vector3d.add(Vector3d.cross(sp, sq), p);
        Vector3d d = Vector3d.sub(p, Vector3d.cross(sp, sq));

        return intersectLineTriangle(new Vertex(q), new Vertex(q2), new Vertex(d), new Vertex(p2), new Vertex(c), c) ? c : null;

    }

    public void intersector(final IntersectorSettings ins, boolean syncWithTextEditor) {
        Composite3D c3d =  linkedDatFile.getLastSelectedComposite();
        NLogger.debug(getClass(), "Intersector - (C) Nils Schmidt 2015"); //$NON-NLS-1$
        NLogger.debug(getClass(), "======================"); //$NON-NLS-1$
        if (c3d != null) {

            final int[] isCancelled = new int[]{0};


            final Set<GData3> trisToHide = new HashSet<GData3>();
            final Set<GData4> quadsToHide = new HashSet<GData4>();

            final Set<GData2> linesToDelete = new HashSet<GData2>();
            final Set<GData3> trisToDelete = new HashSet<GData3>();
            final Set<GData4> quadsToDelete = new HashSet<GData4>();
            final Set<GData5> condlinesToDelete = new HashSet<GData5>();

            NLogger.debug(getClass(), "Get target surfaces to parse."); //$NON-NLS-1$

            final HashSet<GData> targetSurfs = new HashSet<GData>();
            {
                Set<GData3> tris = triangles.keySet();
                for (GData3 tri : tris) {
                    if (!hiddenData.contains(tri)) {
                        targetSurfs.add(tri);
                    }
                }
            }
            {
                Set<GData4> qs = quads.keySet();
                for (GData4 quad : qs) {
                    if (!hiddenData.contains(quad)) {
                        targetSurfs.add(quad);
                    }
                }
            }

            NLogger.debug(getClass(), "Cleanup the selection."); //$NON-NLS-1$

            for(Iterator<GData3> ti = selectedTriangles.iterator(); ti.hasNext();) {
                GData3 tri = ti.next();
                if (!lineLinkedToVertices.containsKey(tri)) {
                    ti.remove();
                }
            }
            for(Iterator<GData4> qi = selectedQuads.iterator(); qi.hasNext();) {
                GData4 quad = qi.next();
                if (!lineLinkedToVertices.containsKey(quad)) {
                    qi.remove();
                }
            }
            for(Iterator<GData2> li = selectedLines.iterator(); li.hasNext();) {
                GData2 line = li.next();
                if (!lineLinkedToVertices.containsKey(line)) {
                    li.remove();
                }
            }
            for(Iterator<GData5> ci = selectedCondlines.iterator(); ci.hasNext();) {
                GData5 condline = ci.next();
                if (!lineLinkedToVertices.containsKey(condline)) {
                    ci.remove();
                }
            }

            final ArrayList<GData> originObjects = new ArrayList<GData>();
            originObjects.addAll(selectedTriangles);
            originObjects.addAll(selectedQuads);
            originObjects.addAll(selectedLines);
            originObjects.addAll(selectedCondlines);

            // Remove adjacent non-selected surfaces from targetSurfs!
            {
                TreeSet<Vertex> verts = new TreeSet<Vertex>();
                for (GData g3 : selectedTriangles) {
                    Vertex[] verts2 = triangles.get(g3);
                    for (Vertex vertex : verts2) {
                        verts.add(vertex);
                    }
                }
                for (GData g4 : selectedQuads) {
                    Vertex[] verts2 = quads.get(g4);
                    for (Vertex vertex : verts2) {
                        verts.add(vertex);
                    }
                }
                for (Vertex vertex : verts) {
                    Collection<GData> surfs = getLinkedSurfaces(vertex);
                    for (GData g : surfs) {
                        switch (g.type()) {
                        case 3:
                            trisToHide.add((GData3) g);
                            break;
                        case 4:
                            quadsToHide.add((GData4) g);
                            break;
                        default:
                            break;
                        }
                    }
                    targetSurfs.removeAll(surfs);
                }
            }

            clearSelection();

            final ArrayList<IntersectionInfoWithColour> intersections = new ArrayList<IntersectionInfoWithColour>();
            final Set<GData2> newLines =  Collections.newSetFromMap(new ThreadsafeHashMap<GData2, Boolean>());
            final Set<GData3> newTriangles = Collections.newSetFromMap(new ThreadsafeHashMap<GData3, Boolean>());
            final Set<GData5> newCondlines =  Collections.newSetFromMap(new ThreadsafeHashMap<GData5, Boolean>());
            try
            {
                new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
                {
                    @Override
                    public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                    {
                        try
                        {
                            monitor.beginTask(I18n.VM_Intersector, IProgressMonitor.UNKNOWN);

                            {

                                final Set<IntersectionInfoWithColour> intersectionSet = Collections.newSetFromMap(new ThreadsafeHashMap<IntersectionInfoWithColour, Boolean>());

                                final int iterations = originObjects.size();
                                final int chunks = View.NUM_CORES;
                                final Thread[] threads = new Thread[chunks];

                                final String surfCount = "/" + iterations;//$NON-NLS-1$
                                final AtomicInteger counter2 = new AtomicInteger(0);

                                int lastend = 0;
                                for (int j = 0; j < chunks; ++j) {
                                    final int[] start = new int[] { lastend };
                                    lastend = Math.round(iterations / chunks * (j + 1));
                                    final int[] end = new int[] { lastend };
                                    if (j == chunks - 1) {
                                        end[0] = iterations;
                                    }
                                    threads[j] = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            for (int k = start[0]; k < end[0]; k++) {
                                                monitor.subTask(counter2.toString() + surfCount);
                                                GData o = originObjects.get(k);
                                                /* Check if the monitor has been canceled */
                                                if (monitor.isCanceled()) {
                                                    isCancelled[0] = 1;
                                                    return;
                                                }
                                                counter2.incrementAndGet();
                                                IntersectionInfoWithColour ii = getIntersectionInfo(o, targetSurfs, ins);
                                                if (ii != null) {
                                                    intersectionSet.add(ii);
                                                    switch (o.type()) {
                                                    case 2:
                                                        linesToDelete.add((GData2) o);
                                                        break;
                                                    case 3:
                                                        trisToDelete.add((GData3) o);
                                                        break;
                                                    case 4:
                                                        quadsToDelete.add((GData4) o);
                                                        break;
                                                    case 5:
                                                        condlinesToDelete.add((GData5) o);
                                                        break;
                                                    default:
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    });
                                    threads[j].start();
                                }
                                boolean isRunning = true;
                                while (isRunning) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                    }
                                    isRunning = false;
                                    for (Thread thread : threads) {
                                        if (thread.isAlive())
                                            isRunning = true;
                                    }
                                }
                                intersections.addAll(intersectionSet);
                            }

                            if (isCancelled[0] > 0) return;

                            for (GData t : targetSurfs) {
                                switch (t.type()) {
                                case 3:
                                    trisToHide.add((GData3) t);
                                    break;
                                case 4:
                                    quadsToHide.add((GData4) t);
                                    break;
                                default:
                                    break;
                                }
                            }

                            NLogger.debug(getClass(), "Create new faces."); //$NON-NLS-1$

                            {
                                final int iterations = intersections.size();
                                final int chunks = View.NUM_CORES;
                                final Thread[] threads = new Thread[chunks];


                                final String maxIterations = "/" + iterations;//$NON-NLS-1$
                                final AtomicInteger counter2 = new AtomicInteger(0);

                                int lastend = 0;
                                for (int j = 0; j < chunks; ++j) {
                                    final int[] start = new int[] { lastend };
                                    lastend = Math.round(iterations / chunks * (j + 1));
                                    final int[] end = new int[] { lastend };
                                    if (j == chunks - 1) {
                                        end[0] = iterations;
                                    }
                                    threads[j] = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            for (int k = start[0]; k < end[0]; k++) {
                                                monitor.subTask(counter2.toString() + maxIterations);
                                                IntersectionInfoWithColour info = intersections.get(k);
                                                if (monitor.isCanceled()) {
                                                    isCancelled[0] = 2;
                                                    return;
                                                }
                                                counter2.incrementAndGet();

                                                final ArrayList<Vector3dd> av = info.getAllVertices();
                                                final ArrayList<GColour> cols = info.getColours();
                                                final ArrayList<Integer> ts = info.getIsLine();

                                                newTriangles.addAll(MathHelper.triangulatePointGroups(cols, av, ts, View.DUMMY_REFERENCE, linkedDatFile));
                                                newLines.addAll(MathHelper.triangulatePointGroups2(cols, av, ts, View.DUMMY_REFERENCE, linkedDatFile));
                                                newCondlines.addAll(MathHelper.triangulatePointGroups5(cols, av, ts, View.DUMMY_REFERENCE, linkedDatFile));
                                            }
                                        }
                                    });
                                    threads[j].start();
                                }
                                boolean isRunning = true;
                                while (isRunning) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                    }
                                    isRunning = false;
                                    for (Thread thread : threads) {
                                        if (thread.isAlive())
                                            isRunning = true;
                                    }
                                }
                            }

                        }
                        finally
                        {
                            monitor.done();
                        }
                    }
                });
            }
            catch (InvocationTargetException consumed) {
            } catch (InterruptedException consumed) {
            }


            NLogger.debug(getClass(), "Check for identical vertices and collinearity."); //$NON-NLS-1$
            final Set<GData3> trisToDelete2 = new HashSet<GData3>();
            {
                for (GData3 g3 : newTriangles) {
                    Vertex[] verts = triangles.get(g3);
                    Set<Vertex> verts2 = new TreeSet<Vertex>();
                    for (Vertex vert : verts) {
                        verts2.add(vert);
                    }
                    if (verts2.size() < 3 || g3.isCollinear()) {
                        trisToDelete2.add(g3);
                    }
                }
            }


            if (isCancelled[0] == 0) {
                NLogger.debug(getClass(), "Hide intersecting faces."); //$NON-NLS-1$

                selectedTriangles.addAll(trisToHide);
                selectedQuads.addAll(quadsToHide);
                selectedData.addAll(selectedTriangles);
                selectedData.addAll(selectedQuads);
                hideSelection();

                clearSelection();

                NLogger.debug(getClass(), "Delete old selected objects."); //$NON-NLS-1$

                selectedLines.addAll(linesToDelete);
                selectedTriangles.addAll(trisToDelete);
                selectedQuads.addAll(quadsToDelete);
                selectedCondlines.addAll(condlinesToDelete);
                selectedData.addAll(selectedLines);
                selectedData.addAll(selectedTriangles);
                selectedData.addAll(selectedQuads);
                selectedData.addAll(selectedCondlines);
                delete(false, false);
            } else {
                clearSelection();
            }

            // Append the new data
            for (GData3 tri : newTriangles) {
                linkedDatFile.addToTail(tri);
            }

            for (GData2 lin : newLines) {
                linkedDatFile.addToTail(lin);
            }

            for (GData5 clin : newCondlines) {
                linkedDatFile.addToTail(clin);
            }

            NLogger.debug(getClass(), "Delete new, but invalid objects."); //$NON-NLS-1$

            newTriangles.removeAll(trisToDelete2);
            selectedTriangles.addAll(trisToDelete2);
            selectedData.addAll(selectedTriangles);
            delete(false, false);

            // Round to 6 decimal places

            selectedLines.addAll(newLines);
            selectedTriangles.addAll(newTriangles);
            selectedCondlines.addAll(newCondlines);
            selectedData.addAll(selectedLines);
            selectedData.addAll(selectedTriangles);
            selectedData.addAll(selectedCondlines);

            NLogger.debug(getClass(), "Round."); //$NON-NLS-1$
            roundSelection(6, 10, true, false);

            clearSelection();
            if (syncWithTextEditor) {
                setModified(true, true);
            } else {
                setModified_NoSync();
            }

            NLogger.debug(getClass(), "Done."); //$NON-NLS-1$

            validateState();

        } else {
            NLogger.debug(getClass(), "No 3D view selected. Cancel process."); //$NON-NLS-1$
        }
    }

    private IntersectionInfoWithColour getIntersectionInfo(GData origin, HashSet<GData> targetSurfs, IntersectorSettings ins) {

        final BigDecimal MIN_DIST = new BigDecimal(".0001"); //$NON-NLS-1$

        final int ot = origin.type();

        final ArrayList<ArrayList<Vector3dd>> fixedIntersectionLines = new ArrayList<ArrayList<Vector3dd>>();
        final ArrayList<ArrayList<Vector3dd>> allLines = new ArrayList<ArrayList<Vector3dd>>();
        final ArrayList<Vector3dd> fixedVertices = new ArrayList<Vector3dd>();

        final HashMap<GData, ArrayList<Vector3dd>> intersections = new HashMap<GData, ArrayList<Vector3dd>>();

        Vertex[] ov = null;
        switch (ot) {
        case 2:
            ov = lines.get(origin);
            break;
        case 3:
            ov = triangles.get(origin);
            break;
        case 4:
            ov = quads.get(origin);
            break;
        case 5:
            ov = condlines.get(origin);
            break;
        default:
            return null;
        }

        if (ot == 2 || ot == 5) {

            if (getLineFaceIntersection(fixedVertices, targetSurfs, ov)) {

                final ArrayList<Vector3dd> resultVertices = new ArrayList<Vector3dd>();
                final ArrayList<GColour> resultColours = new ArrayList<GColour>();
                final ArrayList<Integer> resultIsLine = new ArrayList<Integer>();

                Vector3dd start = fixedVertices.get(0);

                Vector3d normal = new Vector3d(new BigDecimal(1.34), new BigDecimal(-.77), new BigDecimal(2));
                normal.normalise(normal);
                for (int i = 1; i < fixedVertices.size(); i++) {
                    Vector3dd end = fixedVertices.get(i);


                    if (ins.isColourise()) {

                        // Calculate pseudo mid-point
                        Vector3dd mid = new Vector3dd();
                        mid.setX(start.X.multiply(MathHelper.R1).add(end.X.multiply(MathHelper.R2.add(MathHelper.R3))));
                        mid.setY(start.Y.multiply(MathHelper.R1).add(end.Y.multiply(MathHelper.R2.add(MathHelper.R3))));
                        mid.setZ(start.Z.multiply(MathHelper.R1).add(end.Z.multiply(MathHelper.R2.add(MathHelper.R3))));

                        int intersectionCount = 0;


                        for (GData3 g3 : triangles.keySet()) {
                            Vertex[] v = triangles.get(g3);
                            if (intersectRayTriangle(mid, normal, new Vector3dd(v[0]), new Vector3dd(v[1]), new Vector3dd(v[2]))) {
                                intersectionCount += 1;
                            }
                        }
                        for (GData4 g4 : quads.keySet()) {
                            Vertex[] v = quads.get(g4);
                            if (
                                    intersectRayTriangle(mid, normal, new Vector3dd(v[0]), new Vector3dd(v[1]), new Vector3dd(v[2])) ||
                                    intersectRayTriangle(mid, normal, new Vector3dd(v[2]), new Vector3dd(v[3]), new Vector3dd(v[0]))) {
                                intersectionCount += 1;
                            }
                        }
                        resultVertices.add(start);
                        resultVertices.add(end);
                        if (ot == 2) {
                            resultIsLine.add(1);
                        } else {
                            resultVertices.add(new Vector3dd(ov[2]));
                            resultVertices.add(new Vector3dd(ov[3]));
                            resultIsLine.add(2);
                        }
                        if (intersectionCount == 1) {
                            resultColours.add(View.getLDConfigColour(0));
                        } else if (intersectionCount % 2 == 0) {
                            resultColours.add(View.getLDConfigColour(28));
                        } else {
                            resultColours.add(View.getLDConfigColour(1));
                        }

                    } else {
                        final float R, G, B, A;
                        final int CN;
                        resultVertices.add(start);
                        resultVertices.add(end);
                        if (ot == 2) {
                            GData2 origin2 = (GData2) origin;
                            CN = origin2.colourNumber;
                            R = origin2.r;
                            G = origin2.g;
                            B = origin2.b;
                            A = origin2.a;
                            resultIsLine.add(1);
                        } else {
                            GData5 origin2 = (GData5) origin;
                            CN = origin2.colourNumber;
                            R = origin2.r;
                            G = origin2.g;
                            B = origin2.b;
                            A = origin2.a;
                            resultVertices.add(new Vector3dd(ov[2]));
                            resultVertices.add(new Vector3dd(ov[3]));
                            resultIsLine.add(2);
                        }
                        resultColours.add(new GColour(CN, R, G, B, A));
                    }
                    start = end;
                }


                return new IntersectionInfoWithColour(resultColours, resultVertices, resultIsLine);
            } else {
                return null;
            }

        } else {
            for (GData targetSurf : targetSurfs) {
                final int tt = targetSurf.type();

                if (ot == 3 && tt == 3) {

                    Vertex[] tv = triangles.get(targetSurf);

                    getTriangleTriangleIntersection(intersections, targetSurf, ov, tv, ins, false, false);

                } else if (ot == 4 && tt == 4) {

                    Vertex[] tv = quads.get(targetSurf);

                    Vertex[] ov1 = new Vertex[]{ov[0], ov[1], ov[2]};
                    Vertex[] ov2 = new Vertex[]{ov[2], ov[3], ov[0]};
                    Vertex[] tv1 = new Vertex[]{tv[0], tv[1], tv[2]};
                    Vertex[] tv2 = new Vertex[]{tv[2], tv[3], tv[0]};

                    getTriangleTriangleIntersection(intersections, targetSurf, ov1, tv1, ins, true, true);
                    getTriangleTriangleIntersection(intersections, targetSurf, ov1, tv2, ins, true, true);
                    getTriangleTriangleIntersection(intersections, targetSurf, ov2, tv1, ins, true, true);
                    getTriangleTriangleIntersection(intersections, targetSurf, ov2, tv2, ins, true, true);

                } else if (ot == 4 && tt == 3) {
                    Vertex[] tv = triangles.get(targetSurf);

                    Vertex[] tv1 = new Vertex[]{tv[0], tv[1], tv[2]};
                    Vertex[] ov1 = new Vertex[]{ov[0], ov[1], ov[2]};
                    Vertex[] ov2 = new Vertex[]{ov[2], ov[3], ov[0]};

                    getTriangleTriangleIntersection(intersections, targetSurf, ov1, tv1, ins, true, false);
                    getTriangleTriangleIntersection(intersections, targetSurf, ov2, tv1, ins, true, false);

                } else if (ot == 3 && tt == 4) {

                    Vertex[] tv = quads.get(targetSurf);

                    Vertex[] ov1 = new Vertex[]{ov[0], ov[1], ov[2]};
                    Vertex[] tv1 = new Vertex[]{tv[0], tv[1], tv[2]};
                    Vertex[] tv2 = new Vertex[]{tv[2], tv[3], tv[0]};

                    getTriangleTriangleIntersection(intersections, targetSurf, ov1, tv1, ins, false, true);
                    getTriangleTriangleIntersection(intersections, targetSurf, ov1, tv2, ins, false, true);

                }
            }

            for (GData key : intersections.keySet()) {
                ArrayList<Vector3dd> line = intersections.get(key);
                if (line.size() > 1) {
                    fixedIntersectionLines.add(line);
                }
            }

            // Check intersections within the fixed intersection lines
            {
                ArrayList<ArrayList<Vector3dd>> linesToRemove = new ArrayList<ArrayList<Vector3dd>>();
                ArrayList<ArrayList<Vector3dd>> newLines = new ArrayList<ArrayList<Vector3dd>>();
                for (Iterator<ArrayList<Vector3dd>> iterator = fixedIntersectionLines.iterator(); iterator.hasNext();) {
                    ArrayList<Vector3dd> line = iterator.next();
                    ArrayList<Vector3d> intersect = new ArrayList<Vector3d>();
                    for (ArrayList<Vector3dd> line2 : fixedIntersectionLines) {
                        if (line2 != line) {
                            TreeSet<Vector3dd> allVertices = new TreeSet<Vector3dd>();
                            for(int l = 0; l < 2; l++) {
                                allVertices.add(line.get(l));
                                allVertices.add(line2.get(l));
                            }
                            if (allVertices.size() == 4) {
                                Vector3d ip = intersectLineLineSegmentUnidirectional2(line.get(0), line.get(1), line2.get(0), line2.get(1));
                                if (ip != null) {
                                    intersect.add(ip);
                                }
                            }
                        }
                    }
                    if (!intersect.isEmpty()) {
                        TreeMap<BigDecimal, Vector3d> linePoints = new TreeMap<BigDecimal, Vector3d>();
                        Vector3d start = line.get(0);
                        Vector3d end = line.get(1);
                        for (Vector3d v : intersect) {
                            BigDecimal dist = Vector3d.manhattan(v, start);
                            linePoints.put(dist, v);
                        }
                        BigDecimal dist = Vector3d.manhattan(end, start);
                        linePoints.put(dist, end);

                        for (BigDecimal d : linePoints.keySet()) {
                            end = linePoints.get(d);
                            ArrayList<Vector3dd> newLine = new ArrayList<Vector3dd>();
                            newLine.add(new Vector3dd(start));
                            newLine.add(new Vector3dd(end));
                            newLines.add(newLine);
                            start = end;
                        }
                        linesToRemove.add(line);
                    }
                }
                fixedIntersectionLines.removeAll(linesToRemove);
                fixedIntersectionLines.addAll(newLines);
            }

            final ArrayList<Vector3dd> resultVertices = new ArrayList<Vector3dd>();
            final ArrayList<GColour> resultColours = new ArrayList<GColour>();
            final ArrayList<Integer> resultIsLine = new ArrayList<Integer>();

            Vector3d originalNormal = null;

            switch (ot) {
            case 3:
            {
                fixedVertices.add(new Vector3dd(ov[0]).round());
                fixedVertices.add(new Vector3dd(ov[1]).round());
                fixedVertices.add(new Vector3dd(ov[2]).round());
                GData3 g3 = (GData3) origin;
                originalNormal = new Vector3d(new Vertex(g3.xn, g3.yn, g3.zn));
            }
            break;
            case 4:
            {
                fixedVertices.add(new Vector3dd(ov[0]).round());
                fixedVertices.add(new Vector3dd(ov[1]).round());
                fixedVertices.add(new Vector3dd(ov[2]).round());
                fixedVertices.add(new Vector3dd(ov[3]).round());
                GData4 g4 = (GData4) origin;
                originalNormal = new Vector3d(new Vertex(g4.xn, g4.yn, g4.zn));
            }
            break;
            default:
                return null;
            }

            {
                final TreeSet<Vector3dd> allVertices = new TreeSet<Vector3dd>();
                for (ArrayList<Vector3dd> l : fixedIntersectionLines) {
                    allVertices.add(l.get(0).round());
                    allVertices.add(l.get(1).round());
                    //                    resultVertices.add(l.get(0).round());
                    //                    resultVertices.add(l.get(1).round());
                    //                    resultColours.add(new GColour(-1, 0f, 1f, 0f, 1f));
                    //                    resultIsLine.add(1);
                }
                allVertices.removeAll(fixedVertices);
                fixedVertices.addAll(allVertices);
            }

            allLines.addAll(fixedIntersectionLines);
            if (!allLines.isEmpty()) {
                final int vc = fixedVertices.size();
                for (int i = 0; i < vc; i++) {
                    for (int j = 0; j < vc; j++) {
                        if (i == j) continue;
                        boolean intersect = false;
                        Vector3dd v1 = fixedVertices.get(i);
                        Vector3dd v2 = fixedVertices.get(j);
                        int lc = allLines.size();
                        for (int k = 0; k < lc; k++) {
                            ArrayList<Vector3dd> l = allLines.get(k);
                            Vector3dd v3 = l.get(0);
                            Vector3dd v4 = l.get(1);
                            if (!v1.equals(v3) && !v1.equals(v4) && !v2.equals(v3) && !v2.equals(v4) && intersectLineLineSegmentUnidirectional(v1, v2, v3, v4)) {
                                intersect = true;
                                break;
                            }
                            if (Vector3dd.manhattan(v1, v3).compareTo(MIN_DIST) < 0 && Vector3dd.manhattan(v2, v4).compareTo(MIN_DIST) < 0 ||
                                    Vector3dd.manhattan(v2, v3).compareTo(MIN_DIST) < 0 && Vector3dd.manhattan(v1, v4).compareTo(MIN_DIST) < 0) {
                                intersect = true;
                                break;
                            }
                        }
                        if (intersect) {
                            continue;
                        } else {
                            BigDecimal dist = Vector3dd.manhattan(v1, v2);
                            if (dist.compareTo(MIN_DIST) > 0) {
                                ArrayList<Vector3dd> nl = new ArrayList<Vector3dd>();
                                nl.add(v1);
                                nl.add(v2);
                                allLines.add(nl);
                            }
                        }
                    }
                }

                int lc = allLines.size();
                {
                    int removed = 0;
                    for (int i = 0; i + removed < lc; i++) {
                        for (int j = i + 1; j + removed < lc; j++) {
                            TreeSet<Vector3dd> allVertices = new TreeSet<Vector3dd>();
                            for(int l = 0; l < 2; l++) {
                                allVertices.add(allLines.get(i).get(l));
                                allVertices.add(allLines.get(j).get(l));
                            }
                            if (allVertices.size() == 2) {
                                removed += 1;
                                allLines.remove(j);
                            }
                        }
                    }

                    lc = allLines.size();

                    removed = 0;
                    for (int i = 0; i + removed < lc; i++) {
                        TreeSet<Vector3dd> allVertices = new TreeSet<Vector3dd>();
                        allVertices.add(allLines.get(i).get(0));
                        allVertices.add(allLines.get(i).get(1));
                        if (allVertices.size() == 1) {
                            removed += 1;
                            allLines.remove(i);
                        }
                    }
                }

                lc = allLines.size();

                for (int i = 0; i < lc; i++) {
                    for (int j = i + 1; j < lc; j++) {
                        for (int k = j + 1; k < lc; k++) {
                            TreeSet<Vector3dd> allVertices = new TreeSet<Vector3dd>();
                            for(int l = 0; l < 2; l++) {
                                allVertices.add(allLines.get(i).get(l).round());
                                allVertices.add(allLines.get(j).get(l).round());
                                allVertices.add(allLines.get(k).get(l).round());
                            }
                            if (allVertices.size() == 3) {
                                Vector3dd[] triVerts = new Vector3dd[3];
                                int l = 0;
                                for (Vector3dd v : allVertices) {
                                    triVerts[l] = v;
                                    l++;
                                }
                                boolean isInsideTriangle = false;
                                Vector3d normal = Vector3d.cross(Vector3d.sub(triVerts[2], triVerts[0]), Vector3d.sub(triVerts[1], triVerts[0]));
                                normal.normalise(normal);
                                for (Vector3dd fixed : fixedVertices) {
                                    if (fixed.equals(triVerts[0])) continue;
                                    if (fixed.equals(triVerts[1])) continue;
                                    if (fixed.equals(triVerts[2])) continue;
                                    if (intersectRayTriangle(fixed, normal, triVerts[0], triVerts[1], triVerts[2])) {
                                        isInsideTriangle = true;
                                        break;
                                    }
                                }
                                if (isInsideTriangle) continue;

                                // Check collinearity
                                {
                                    Vector3d vertexA = new Vector3d(triVerts[0]);
                                    Vector3d vertexB = new Vector3d(triVerts[1]);
                                    Vector3d vertexC = new Vector3d(triVerts[2]);
                                    Vector3d vertexA2 = new Vector3d();
                                    Vector3d vertexB2 = new Vector3d();
                                    Vector3d.sub(vertexA, vertexC, vertexA2);
                                    Vector3d.sub(vertexB, vertexC, vertexB2);
                                    if  (Vector3d.angle(vertexA2, vertexB2) < Threshold.collinear_angle_minimum) continue;
                                }

                                if (MathHelper.directionOfVectors(normal, originalNormal) == 1) {
                                    resultVertices.add(triVerts[0]);
                                    resultVertices.add(triVerts[1]);
                                    resultVertices.add(triVerts[2]);
                                } else {
                                    resultVertices.add(triVerts[0]);
                                    resultVertices.add(triVerts[2]);
                                    resultVertices.add(triVerts[1]);
                                }


                                if (ins.isColourise()) {

                                    // Calculate pseudo mid-point
                                    Vector3dd mid = new Vector3dd();

                                    mid.setX(triVerts[0].X.multiply(MathHelper.R1).add(triVerts[1].X.multiply(MathHelper.R2)).add(triVerts[2].X.multiply(MathHelper.R3)));
                                    mid.setY(triVerts[0].Y.multiply(MathHelper.R1).add(triVerts[1].Y.multiply(MathHelper.R2)).add(triVerts[2].Y.multiply(MathHelper.R3)));
                                    mid.setZ(triVerts[0].Z.multiply(MathHelper.R1).add(triVerts[1].Z.multiply(MathHelper.R2)).add(triVerts[2].Z.multiply(MathHelper.R3)));

                                    int intersectionCount = 0;

                                    for (GData3 g3 : triangles.keySet()) {
                                        Vertex[] v = triangles.get(g3);
                                        if (intersectRayTriangle(mid, normal, new Vector3dd(v[0]), new Vector3dd(v[1]), new Vector3dd(v[2]))) {
                                            intersectionCount += 1;
                                        }
                                    }
                                    for (GData4 g4 : quads.keySet()) {
                                        Vertex[] v = quads.get(g4);
                                        if (
                                                intersectRayTriangle(mid, normal, new Vector3dd(v[0]), new Vector3dd(v[1]), new Vector3dd(v[2])) ||
                                                intersectRayTriangle(mid, normal, new Vector3dd(v[2]), new Vector3dd(v[3]), new Vector3dd(v[0]))) {
                                            intersectionCount += 1;
                                        }
                                    }

                                    if (intersectionCount == 1) {
                                        resultColours.add(View.getLDConfigColour(7));
                                    } else if (intersectionCount % 2 == 0) {
                                        resultColours.add(View.getLDConfigColour(14));
                                    } else {
                                        resultColours.add(View.getLDConfigColour(11));
                                    }

                                } else {
                                    final float R, G, B, A;
                                    final int CN;
                                    if (ot == 3) {
                                        GData3 origin2 = (GData3) origin;
                                        CN = origin2.colourNumber;
                                        R = origin2.r;
                                        G = origin2.g;
                                        B = origin2.b;
                                        A = origin2.a;
                                    } else {
                                        GData4 origin2 = (GData4) origin;
                                        CN = origin2.colourNumber;
                                        R = origin2.r;
                                        G = origin2.g;
                                        B = origin2.b;
                                        A = origin2.a;
                                    }
                                    resultColours.add(new GColour(CN, R, G, B, A));
                                }
                                resultIsLine.add(0);
                            }
                        }

                    }
                }

                if (resultVertices.isEmpty()) return null;
                return new IntersectionInfoWithColour(resultColours, resultVertices, resultIsLine);
            } else {
                return null;
            }
        }

    }

    private boolean intersectRayTriangle(Vector3dd orig, Vector3d dir, Vector3dd vert0, Vector3dd vert1, Vector3dd vert2) {
        BigDecimal diskr = BigDecimal.ZERO;
        BigDecimal inv_diskr = BigDecimal.ZERO;
        Vector3d corner1 = Vector3d.sub(vert1, vert0);
        Vector3d corner2 = Vector3d.sub(vert2, vert0);
        Vector3d pvec = Vector3d.cross(dir, corner2);
        diskr = Vector3d.dotP(corner1, pvec);
        if (diskr.abs().compareTo(TOLERANCER) < 0)
            return false;
        inv_diskr = BigDecimal.ONE.divide(diskr, Threshold.mc);
        Vector3d tvec = Vector3d.sub(orig, vert0);
        BigDecimal u = Vector3d.dotP(tvec, pvec).multiply(inv_diskr);
        if (u.compareTo(ZEROTR) < 0 || u.compareTo(ONETR) > 0)
            return false;
        Vector3d qvec = Vector3d.cross(tvec, corner1);
        BigDecimal v = Vector3d.dotP(dir, qvec).multiply(inv_diskr);
        if (v.compareTo(ZEROTR) < 0 || u.add(v).compareTo(ONETR) > 0)
            return false;
        return true;
    }

    private boolean getLineFaceIntersection(ArrayList<Vector3dd> fixedVertices, HashSet<GData> targetSurfs, Vertex[] ov) {

        TreeMap<BigDecimal, Vector3d> linePoints = new TreeMap<BigDecimal, Vector3d>();
        Vector3d start = new Vector3d(ov[0]);
        Vector3d end = new Vector3d(ov[1]);

        for (GData g : targetSurfs) {
            Vector3d intersection = new Vector3d();
            switch (g.type()) {
            case 3:
            {
                Vertex[] verts = triangles.get(g);
                if (intersectLineTriangle(ov[0], ov[1], verts[0], verts[1], verts[2], intersection)) {
                    fixedVertices.add(new Vector3dd(intersection));
                    BigDecimal dist = Vector3d.manhattan(intersection, start);
                    linePoints.put(dist, intersection);
                }
            }
            break;
            case 4:
            {
                Vertex[] verts = quads.get(g);
                if (
                        intersectLineTriangle(ov[0], ov[1], verts[0], verts[1], verts[2], intersection) ||
                        intersectLineTriangle(ov[0], ov[1], verts[2], verts[3], verts[0], intersection)) {
                    fixedVertices.add(new Vector3dd(intersection));
                    BigDecimal dist = Vector3d.manhattan(intersection, start);
                    linePoints.put(dist, intersection);
                }
            }
            break;
            default:
                break;
            }
        }

        if (fixedVertices.isEmpty()) {
            return false;
        } else {
            fixedVertices.clear();
            BigDecimal dist = Vector3d.manhattan(end, start);
            linePoints.put(BigDecimal.ZERO, start);
            linePoints.put(dist, end);
            for (BigDecimal d : linePoints.keySet()) {
                fixedVertices.add(new Vector3dd(linePoints.get(d)));
            }
            return true;
        }

    }

    private void getTriangleTriangleIntersection(HashMap<GData, ArrayList<Vector3dd>> intersections, GData target, Vertex[] ov, Vertex[] tv, IntersectorSettings ins, boolean originIsQuad, boolean targetIsQuad) {
        ArrayList<Vector3dd> result2 = null;
        if (intersections.containsKey(target)) {
            result2 = intersections.get(target);
        } else {
            result2 = new ArrayList<Vector3dd>();
            intersections.put(target, result2);
        }

        final TreeSet<Vector3dd> result = new TreeSet<Vector3dd>();

        {
            Vector3dd r = new Vector3dd();
            if (intersectLineTriangle(tv[0], tv[1], ov[0], ov[1], ov[2], r)) {
                result.add(r.round());
            }
        }
        {
            Vector3dd r = new Vector3dd();
            if (intersectLineTriangle(tv[1], tv[2], ov[0], ov[1], ov[2], r)) {
                result.add(r.round());
            }
        }
        if (!targetIsQuad) {
            Vector3dd r = new Vector3dd();
            if (intersectLineTriangle(tv[2], tv[0], ov[0], ov[1], ov[2], r)) {
                result.add(r.round());
            }
        }
        {
            Vector3dd r = new Vector3dd();
            if (intersectLineTriangle(ov[0], ov[1], tv[0], tv[1], tv[2], r)) {
                result.add(r.round());
            }
        }
        {
            Vector3dd r = new Vector3dd();
            if (intersectLineTriangle(ov[1], ov[2], tv[0], tv[1], tv[2], r)) {
                result.add(r.round());
            }
        }
        if (!originIsQuad) {
            Vector3dd r = new Vector3dd();
            if (intersectLineTriangle(ov[2], ov[0], tv[0], tv[1], tv[2], r)) {
                result.add(r.round());
            }
        }
        result.removeAll(result2);
        result2.addAll(result);
    }

    public void splitQuads(boolean isModified) {

        final Set<GData4> quadsToDelete = new HashSet<GData4>();
        final Set<GData3> newTriangles = new HashSet<GData3>();

        final Set<GData4> quadsToParse = new HashSet<GData4>();

        quadsToParse.addAll(selectedQuads);

        clearSelection();

        for (Iterator<GData4> ig = quadsToParse.iterator(); ig.hasNext();) {
            GData4 g = ig.next();
            if (!lineLinkedToVertices.containsKey(g) || g.isCollinear()) {
                ig.remove();
            }
        }
        if (quadsToParse.isEmpty()) {
            return;
        } else {
            setModified_NoSync();
        }
        quadsToDelete.addAll(quadsToParse);

        for (GData4 g4 : quadsToParse) {
            Vertex[] v = quads.get(g4);

            switch (g4.getHourglassConfiguration()) {
            case 0:
            {
                GData3 tri1 = new GData3(g4.colourNumber, g4.r, g4.g, g4.b, g4.a, v[0], v[1], v[2], View.DUMMY_REFERENCE, linkedDatFile);
                GData3 tri2 = new GData3(g4.colourNumber, g4.r, g4.g, g4.b, g4.a, v[2], v[3], v[0], View.DUMMY_REFERENCE, linkedDatFile);
                newTriangles.add(tri1);
                newTriangles.add(tri2);
                linkedDatFile.insertAfter(g4, tri2);
                linkedDatFile.insertAfter(g4, tri1);
            }
            break;
            case 1:
            {
                GData3 tri1 = new GData3(g4.colourNumber, g4.r, g4.g, g4.b, g4.a, v[1], v[0], v[2], View.DUMMY_REFERENCE, linkedDatFile);
                GData3 tri2 = new GData3(g4.colourNumber, g4.r, g4.g, g4.b, g4.a, v[2], v[3], v[1], View.DUMMY_REFERENCE, linkedDatFile);
                newTriangles.add(tri1);
                newTriangles.add(tri2);
                linkedDatFile.insertAfter(g4, tri2);
                linkedDatFile.insertAfter(g4, tri1);
            }
            break;
            case 2:
            {
                GData3 tri1 = new GData3(g4.colourNumber, g4.r, g4.g, g4.b, g4.a, v[0], v[1], v[3], View.DUMMY_REFERENCE, linkedDatFile);
                GData3 tri2 = new GData3(g4.colourNumber, g4.r, g4.g, g4.b, g4.a, v[3], v[2], v[0], View.DUMMY_REFERENCE, linkedDatFile);
                newTriangles.add(tri1);
                newTriangles.add(tri2);
                linkedDatFile.insertAfter(g4, tri2);
                linkedDatFile.insertAfter(g4, tri1);
            }
            }

        }

        selectedQuads.addAll(quadsToDelete);
        selectedData.addAll(quadsToDelete);
        delete(false, false);

        selectedTriangles.addAll(newTriangles);
        selectedData.addAll(newTriangles);

        if (isModified && isModified()) {
            setModified(true, true);
        }
        validateState();

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

    public void lines2pattern() {

        if (linkedDatFile.isReadOnly()) return;

        final BigDecimal MIN_DIST = new BigDecimal(".0001"); //$NON-NLS-1$

        final Set<GData2> originalSelectionLines = new HashSet<GData2>();
        final Set<GData3> originalSelectionTriangles = new HashSet<GData3>();
        final Set<GData4> originalSelectionQuads = new HashSet<GData4>();
        final Set<GData3> newTriangles = new HashSet<GData3>();

        final ArrayList<ArrayList<Vector3dd>> linesToParse = new ArrayList<ArrayList<Vector3dd>>();
        final ArrayList<ArrayList<Vector3dd>> colourLines = new ArrayList<ArrayList<Vector3dd>>();
        final ArrayList<ArrayList<Vector3dh>> linesToParseHashed = new ArrayList<ArrayList<Vector3dh>>();

        final HashMap<ArrayList<Vector3dd>, GColour> colours = new HashMap<ArrayList<Vector3dd>, GColour>();

        final int chunks = View.NUM_CORES;

        originalSelectionLines.addAll(selectedLines);
        originalSelectionTriangles.addAll(selectedTriangles);
        originalSelectionQuads.addAll(selectedQuads);

        final Vector3d originalNormal = new Vector3d(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE);

        // Verify
        try
        {
            new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
            {
                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                {
                    try
                    {
                        monitor.beginTask(I18n.VM_Lines2Pattern, IProgressMonitor.UNKNOWN);

                        for (GData3 g3 : selectedTriangles) {
                            TreeSet<Vertex> vs = new TreeSet<Vertex>();
                            Vertex[] verts = triangles.get(g3);
                            for (Vertex v : verts) {
                                vs.add(v);
                            }
                            if (vs.size() != 3) return;
                            {
                                ArrayList<Vector3dd> l = new ArrayList<Vector3dd>();
                                l.add(new Vector3dd(verts[0]));
                                l.add(new Vector3dd(verts[1]));
                                linesToParse.add(l);
                            }
                            {
                                ArrayList<Vector3dd> l = new ArrayList<Vector3dd>();
                                l.add(new Vector3dd(verts[1]));
                                l.add(new Vector3dd(verts[2]));
                                linesToParse.add(l);
                            }
                            {
                                ArrayList<Vector3dd> l = new ArrayList<Vector3dd>();
                                l.add(new Vector3dd(verts[2]));
                                l.add(new Vector3dd(verts[0]));
                                linesToParse.add(l);
                            }
                        }

                        for (GData4 g4 : selectedQuads) {
                            TreeSet<Vertex> vs = new TreeSet<Vertex>();
                            Vertex[] verts = quads.get(g4);
                            for (Vertex v : verts) {
                                vs.add(v);
                            }
                            if (vs.size() != 4) return;
                            {
                                ArrayList<Vector3dd> l = new ArrayList<Vector3dd>();
                                l.add(new Vector3dd(verts[0]));
                                l.add(new Vector3dd(verts[1]));
                                linesToParse.add(l);
                            }
                            {
                                ArrayList<Vector3dd> l = new ArrayList<Vector3dd>();
                                l.add(new Vector3dd(verts[1]));
                                l.add(new Vector3dd(verts[2]));
                                linesToParse.add(l);
                            }
                            {
                                ArrayList<Vector3dd> l = new ArrayList<Vector3dd>();
                                l.add(new Vector3dd(verts[2]));
                                l.add(new Vector3dd(verts[3]));
                                linesToParse.add(l);
                            }
                            {
                                ArrayList<Vector3dd> l = new ArrayList<Vector3dd>();
                                l.add(new Vector3dd(verts[3]));
                                l.add(new Vector3dd(verts[0]));
                                linesToParse.add(l);
                            }
                            {
                                ArrayList<Vector3dd> l = new ArrayList<Vector3dd>();
                                l.add(new Vector3dd(verts[1]));
                                l.add(new Vector3dd(verts[3]));
                                linesToParse.add(l);
                            }
                        }

                        TreeSet<Vertex> m1 = new TreeSet<Vertex>();
                        TreeSet<Vertex> m2 = new TreeSet<Vertex>();
                        for (GData2 g2 : selectedLines) {
                            Vertex[] verts = lines.get(g2);
                            for (Vertex v : verts) {
                                if (g2.colourNumber == 24) {
                                    if (m1.contains(v)) {
                                        m2.add(v);
                                    } else {
                                        m1.add(v);
                                    }
                                }
                            }
                            ArrayList<Vector3dd> l = new ArrayList<Vector3dd>();
                            l.add(new Vector3dd(verts[0]));
                            l.add(new Vector3dd(verts[1]));
                            if (g2.colourNumber == 24) {
                                linesToParse.add(l);
                            } else {
                                colourLines.add(l);
                                colours.put(l, new GColour(g2.colourNumber, g2.r, g2.g, g2.b, g2.a));
                            }
                        }
                        if (m1.size() != m2.size()) return;

                        BigDecimal seed = new BigDecimal("1.23456789"); //$NON-NLS-1$
                        BigDecimal seed2 = new BigDecimal("-1.832647382"); //$NON-NLS-1$
                        BigDecimal seed3 = new BigDecimal("1.427637292"); //$NON-NLS-1$
                        Vertex s = new Vertex(seed, seed2, seed3);
                        Vertex p1 = null;
                        Vertex p2 = null;
                        Vertex p3 = null;
                        for (Vertex vertex : m2) {
                            p1 = vertex;
                            break;
                        }
                        if (p1 == null) return;
                        for (Vertex vertex : m2) {
                            if (!vertex.equals(p1)) {
                                p2 = vertex;
                                break;
                            }
                        }
                        if (p2 == null) return;
                        for (Vertex vertex : m2) {
                            if (!vertex.equals(p1) && !vertex.equals(p2)) {
                                p3 = vertex;
                                break;
                            }
                        }
                        if (p3 == null) return;
                        Vector3d a = new Vector3d(p1.X.add(s.X), p1.Y.add(s.Y),p1.Z.add(s.Z));
                        Vector3d b = new Vector3d(p2.X.add(s.X), p2.Y.add(s.Y),p2.Z.add(s.Z));
                        Vector3d c = new Vector3d(p3.X.add(s.X), p3.Y.add(s.Y),p3.Z.add(s.Z));

                        Vector3d pOrigin = new Vector3d(p1);
                        Vector3d n = Vector3d.cross(Vector3d.sub(a, c), Vector3d.sub(b, c));
                        n.normalise(n);
                        originalNormal.setX(n.X);
                        originalNormal.setY(n.Y);
                        originalNormal.setZ(n.Z);
                        BigDecimal EPSILON = new BigDecimal("0.001"); //$NON-NLS-1$
                        for (Vertex vertex : m2) {
                            Vector3d vp = new Vector3d(vertex);
                            if (Vector3d.dotP(Vector3d.sub(pOrigin, vp), n).abs().compareTo(EPSILON) > 0) return;
                        }

                        if (monitor.isCanceled()) {
                            originalSelectionLines.clear();
                        }
                    }
                    finally
                    {
                        monitor.done();
                    }
                }
            });
        }
        catch (InvocationTargetException consumed) {
        } catch (InterruptedException consumed) {
        }

        if (originalSelectionLines.isEmpty()) return;
        clearSelection();

        // Calculate intersecting lines, if needed.
        {
            ArrayList<ArrayList<Vector3dd>> linesToRemove = new ArrayList<ArrayList<Vector3dd>>();
            ArrayList<ArrayList<Vector3dd>> newLines = new ArrayList<ArrayList<Vector3dd>>();
            for (Iterator<ArrayList<Vector3dd>> iterator = linesToParse.iterator(); iterator.hasNext();) {
                ArrayList<Vector3dd> line = iterator.next();
                ArrayList<Vector3d> intersect = new ArrayList<Vector3d>();
                for (ArrayList<Vector3dd> line2 : linesToParse) {
                    if (line2 != line) {
                        TreeSet<Vector3dd> allVertices = new TreeSet<Vector3dd>();
                        for(int l = 0; l < 2; l++) {
                            allVertices.add(line.get(l));
                            allVertices.add(line2.get(l));
                        }
                        if (allVertices.size() == 4) {
                            Vector3d ip = intersectLineLineSegmentUnidirectional2(line.get(0), line.get(1), line2.get(0), line2.get(1));
                            if (ip != null) {
                                intersect.add(ip);
                            }
                        }
                    }
                }
                if (!intersect.isEmpty()) {
                    TreeMap<BigDecimal, Vector3d> linePoints = new TreeMap<BigDecimal, Vector3d>();
                    Vector3d start = line.get(0);
                    Vector3d end = line.get(1);
                    for (Vector3d v : intersect) {
                        BigDecimal dist = Vector3d.manhattan(v, start);
                        linePoints.put(dist, v);
                    }
                    BigDecimal dist = Vector3d.manhattan(end, start);
                    linePoints.put(dist, end);

                    for (BigDecimal d : linePoints.keySet()) {
                        end = linePoints.get(d);
                        ArrayList<Vector3dd> newLine = new ArrayList<Vector3dd>();
                        newLine.add(new Vector3dd(start));
                        newLine.add(new Vector3dd(end));
                        newLines.add(newLine);
                        start = end;
                    }
                    linesToRemove.add(line);
                }
            }
            linesToParse.removeAll(linesToRemove);
            linesToParse.addAll(newLines);
        }

        final ArrayList<Vector3dd> resultVertices = new ArrayList<Vector3dd>();
        final ArrayList<GColour> resultColours = new ArrayList<GColour>();
        final ArrayList<Integer> resultIsLine = new ArrayList<Integer>();

        final Set<ArrayList<Vector3dd>> colourLines2 = Collections.newSetFromMap(new ThreadsafeHashMap<ArrayList<Vector3dd>, Boolean>());
        final ThreadsafeHashMap<ArrayList<Vector3dd> , GColour> colours2 = new ThreadsafeHashMap<ArrayList<Vector3dd>, GColour>();
        final Thread[] colourThreads = new Thread[chunks];

        // Spread coloured lines
        {

            try
            {
                new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
                {
                    @Override
                    public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                    {
                        try
                        {

                            if (!colourLines.isEmpty()) {

                                final ArrayList<Vector3dd> fixedVertices = new ArrayList<Vector3dd>();
                                final ArrayList<Vector3dd> colourVertices = new ArrayList<Vector3dd>();
                                final TreeMap<Vector3dd, GColour> vertexColour = new TreeMap<Vector3dd, GColour>();
                                {
                                    final TreeSet<Vector3dd> allVertices = new TreeSet<Vector3dd>();
                                    for (ArrayList<Vector3dd> l : linesToParse) {
                                        allVertices.add(l.get(0).round());
                                        allVertices.add(l.get(1).round());
                                    }
                                    for (ArrayList<Vector3dd> l : colourLines) {
                                        Vector3dd vc1 = l.get(0).round();
                                        Vector3dd vc2 = l.get(1).round();
                                        if (!vertexColour.containsKey(vc1)) {
                                            vertexColour.put(vc1, colours.get(l));
                                        } else {
                                            GColour gc = vertexColour.get(vc1);
                                            GColour gc2 = colours.get(l);
                                            if (gc.getColourNumber() != gc2.getColourNumber()) vertexColour.remove(vc1);
                                        }
                                        if (!vertexColour.containsKey(vc2)) {
                                            vertexColour.put(vc2, colours.get(l));
                                        } else {
                                            GColour gc = vertexColour.get(vc2);
                                            GColour gc2 = colours.get(l);
                                            if (gc.getColourNumber() != gc2.getColourNumber()) vertexColour.remove(vc2);
                                        }
                                        colourVertices.add(vc1);
                                        colourVertices.add(vc2);
                                    }
                                    fixedVertices.addAll(allVertices);
                                }

                                final ArrayList<ArrayList<Vector3dd>> fixedLinesToParse = new ArrayList<ArrayList<Vector3dd>>();
                                fixedLinesToParse.addAll(linesToParse);

                                final int vc = colourVertices.size();
                                final int vc2 = fixedVertices.size();

                                final AtomicInteger counter2 = new AtomicInteger(0);

                                for (int j = 0; j < chunks; ++j) {
                                    final int[] start = new int[] { j };
                                    colourThreads[j] = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            int counter = start[0];
                                            for (int i = 0; i < vc; i++) {
                                                if (counter == 0) {
                                                    counter = chunks;
                                                    counter2.incrementAndGet();
                                                    if (monitor.isCanceled()) {
                                                        return;
                                                    }
                                                    Vector3dd v1 = colourVertices.get(i);
                                                    for (int j = 0; j < vc2; j++) {
                                                        boolean intersect = false;
                                                        Vector3dd v2 = fixedVertices.get(j);
                                                        Vector3d sp = Vector3dd.sub(v2, v1);
                                                        Vector3d dir = new Vector3d();
                                                        BigDecimal len = sp.normalise(dir);
                                                        int lc = fixedLinesToParse.size();
                                                        for (int k = 0; k < lc; k++) {
                                                            ArrayList<Vector3dd> l = fixedLinesToParse.get(k);
                                                            Vector3dd v3 = l.get(0);
                                                            Vector3dd v4 = l.get(1);
                                                            if (!v1.equals(v3) && !v1.equals(v4) && !v2.equals(v3) && !v2.equals(v4) && intersectLineLineSegmentUnidirectionalFast(v1, v2, sp, dir, len, v3, v4)) {
                                                                intersect = true;
                                                                break;
                                                            }
                                                        }
                                                        if (intersect) {
                                                            continue;
                                                        } else {
                                                            BigDecimal dist = Vector3dd.manhattan(v1, v2);
                                                            if (dist.compareTo(MIN_DIST) > 0) {
                                                                if (vertexColour.containsKey(v1) && vertexColour.get(v1) != null) {
                                                                    ArrayList<Vector3dd> nl = new ArrayList<Vector3dd>();
                                                                    nl.add(v1);
                                                                    nl.add(v2);
                                                                    colours2.put(nl, vertexColour.get(v1));
                                                                    colourLines2.add(nl);
                                                                } else if (vertexColour.containsKey(v2) && vertexColour.get(v2) != null) {
                                                                    ArrayList<Vector3dd> nl = new ArrayList<Vector3dd>();
                                                                    nl.add(v1);
                                                                    nl.add(v2);
                                                                    colours2.put(nl, vertexColour.get(v2));
                                                                    colourLines2.add(nl);
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                counter -= 1;
                                            }
                                        }
                                    });
                                    colourThreads[j].start();
                                }
                            }
                        } finally {
                            monitor.done();
                        }
                    }
                });
            }
            catch (InvocationTargetException consumed) {
            } catch (InterruptedException consumed) {
            }
        }

        final ArrayList<Vector3dd> fixedVertices = new ArrayList<Vector3dd>();
        final ArrayList<Vector3dh> fixedVertices2 = new ArrayList<Vector3dh>();

        {
            final TreeSet<Vector3dd> allVertices = new TreeSet<Vector3dd>();
            for (ArrayList<Vector3dd> l : linesToParse) {
                allVertices.add(l.get(0).round());
                allVertices.add(l.get(1).round());
            }
            fixedVertices.addAll(allVertices);
        }

        if (!linesToParse.isEmpty()) {

            final ThreadsafeHashMap<Vector3dh, HashSet<Vector3dh>> neighbours = new ThreadsafeHashMap<Vector3dh, HashSet<Vector3dh>>();
            try
            {
                new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
                {
                    @Override
                    public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                    {
                        try
                        {
                            monitor.beginTask(I18n.VM_Lines2Pattern, IProgressMonitor.UNKNOWN);

                            final Thread[] threads = new Thread[1];

                            {
                                TreeMap<Vector3dd, Vector3dh> hashedRelation = new TreeMap<Vector3dd, Vector3dh>();
                                for (Vector3dd v : fixedVertices) {
                                    Vector3dh vh;
                                    if (hashedRelation.containsKey(v)) {
                                        vh = hashedRelation.get(v);
                                    } else {
                                        vh = new Vector3dh(v);
                                        hashedRelation.put(v, vh);
                                    }

                                    fixedVertices2.add(vh);
                                }
                                for (ArrayList<Vector3dd> l : linesToParse) {

                                    Vector3dd v1nh = l.get(0).round();
                                    Vector3dd v2nh = l.get(1).round();

                                    Vector3dh v1;
                                    Vector3dh v2;

                                    if (hashedRelation.containsKey(v1nh)) {
                                        v1 = hashedRelation.get(v1nh);
                                    } else {
                                        v1 = new Vector3dh(v1nh);
                                        hashedRelation.put(v1nh, v1);
                                    }

                                    if (hashedRelation.containsKey(v2nh)) {
                                        v2 = hashedRelation.get(v2nh);
                                    } else {
                                        v2 = new Vector3dh(v2nh);
                                        hashedRelation.put(v2nh, v2);
                                    }

                                    ArrayList<Vector3dh> newline = new ArrayList<Vector3dh>();
                                    newline.add(v1);
                                    newline.add(v2);
                                    linesToParseHashed.add(newline);

                                }
                            }

                            final int vc = fixedVertices2.size();
                            final String vertCount = "/" + vc + ")"; //$NON-NLS-1$ //$NON-NLS-2$

                            threads[0] = new Thread(new Runnable() {
                                @Override
                                public void run() {

                                    for (int i = 0; i < vc; i++) {

                                        Object[] messageArguments = {i, vertCount};
                                        MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                                        formatter.setLocale(MyLanguage.LOCALE);
                                        formatter.applyPattern(I18n.VM_DetectNewEdges);

                                        monitor.subTask(formatter.format(messageArguments));

                                        if (monitor.isCanceled()) {
                                            break;
                                        }

                                        Vector3dh v1 = fixedVertices2.get(i);
                                        for (int j = i + 1; j < vc; j++) {
                                            boolean intersect = false;
                                            Vector3dh v2 = fixedVertices2.get(j);

                                            Vector3d sp = Vector3dd.sub(v2, v1);
                                            Vector3d dir = new Vector3d();
                                            BigDecimal len = sp.normalise(dir);
                                            Iterator<ArrayList<Vector3dh>> li = linesToParseHashed.iterator();
                                            while (li.hasNext()) {
                                                ArrayList<Vector3dh> l = li.next();
                                                Vector3dh v3 = l.get(0);
                                                Vector3dh v4 = l.get(1);
                                                if (!v1.equals(v3) && !v1.equals(v4) && !v2.equals(v3) && !v2.equals(v4)) {
                                                    if (intersectLineLineSegmentUnidirectionalFast(v1, v2, sp, dir, len,  v3, v4)) {
                                                        intersect = true;
                                                        break;
                                                    }
                                                }
                                            }
                                            if (!intersect) {
                                                BigDecimal dist = Vector3dd.manhattan(v1, v2);
                                                if (dist.compareTo(MIN_DIST) > 0) {
                                                    ArrayList<Vector3dh> nl = new ArrayList<Vector3dh>();
                                                    nl.add(v1);
                                                    nl.add(v2);
                                                    linesToParseHashed.add(nl);
                                                }
                                            }
                                        }
                                    }
                                }
                            });
                            threads[0].start();
                            boolean isRunning = true;
                            while (isRunning) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                }
                                isRunning = false;
                                if (threads[0].isAlive())
                                    isRunning = true;
                            }
                            if (!colourLines.isEmpty()) {
                                isRunning = true;
                                while (isRunning) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                    }
                                    isRunning = false;
                                    for (Thread thread : colourThreads) {
                                        if (thread.isAlive())
                                            isRunning = true;
                                    }
                                }
                            }
                            if (monitor.isCanceled()) {
                                selectedLines.addAll(originalSelectionLines);
                                selectedTriangles.addAll(originalSelectionTriangles);
                                selectedQuads.addAll(originalSelectionQuads);
                                selectedData.addAll(originalSelectionTriangles);
                                selectedData.addAll(originalSelectionQuads);
                                selectedData.addAll(originalSelectionLines);
                                originalSelectionLines.clear();
                                return;
                            } else {
                                colourLines.addAll(colourLines2);
                                colours.putAll(colours2);
                                linesToParse.clear();
                                fixedVertices2.clear();
                                for (ArrayList<Vector3dh> l : linesToParseHashed) {
                                    ArrayList<Vector3dd> nl = new ArrayList<Vector3dd>();
                                    nl.add(new Vector3dd(l.get(0)));
                                    nl.add(new Vector3dd(l.get(1)));
                                    linesToParse.add(nl);
                                }
                                linesToParseHashed.clear();
                            }
                        } finally {
                            monitor.done();
                        }
                    }
                });
            }
            catch (InvocationTargetException consumed) {
            } catch (InterruptedException consumed) {
            }

            if (originalSelectionLines.isEmpty()) return;

            int lc = linesToParse.size();
            {
                int removed = 0;
                for (int i = 0; i + removed < lc; i++) {
                    for (int j = i + 1; j + removed < lc; j++) {
                        TreeSet<Vector3dd> allVertices = new TreeSet<Vector3dd>();
                        for(int l = 0; l < 2; l++) {
                            allVertices.add(linesToParse.get(i).get(l));
                            allVertices.add(linesToParse.get(j).get(l));
                        }
                        if (allVertices.size() == 2) {
                            removed += 1;
                            linesToParse.remove(j);
                        }
                    }
                }

                lc = linesToParse.size();

                removed = 0;
                for (int i = 0; i + removed < lc; i++) {
                    TreeSet<Vector3dd> allVertices = new TreeSet<Vector3dd>();
                    allVertices.add(linesToParse.get(i).get(0));
                    allVertices.add(linesToParse.get(i).get(1));
                    if (allVertices.size() == 1) {
                        removed += 1;
                        linesToParse.remove(i);
                    }
                }

                lc = linesToParse.size();

                HashSet<Vector3dh> m1 = new HashSet<Vector3dh>();
                HashSet<Vector3dh> m2 = new HashSet<Vector3dh>();
                HashSet<Vector3dh> m3 = new HashSet<Vector3dh>();
                TreeMap<Vector3dd, Vector3dh> hashedRelation = new TreeMap<Vector3dd, Vector3dh>();
                for (int i = 0; i < lc; i++) {
                    Vector3dd v1nh = linesToParse.get(i).get(0).round();
                    Vector3dd v2nh = linesToParse.get(i).get(1).round();

                    Vector3dh v1;
                    Vector3dh v2;

                    if (hashedRelation.containsKey(v1nh)) {
                        v1 = hashedRelation.get(v1nh);
                    } else {
                        v1 = new Vector3dh(v1nh);
                        hashedRelation.put(v1nh, v1);
                    }

                    if (hashedRelation.containsKey(v2nh)) {
                        v2 = hashedRelation.get(v2nh);
                    } else {
                        v2 = new Vector3dh(v2nh);
                        hashedRelation.put(v2nh, v2);
                    }

                    ArrayList<Vector3dh> newline = new ArrayList<Vector3dh>();
                    newline.add(v1);
                    newline.add(v2);
                    linesToParseHashed.add(newline);

                    if (neighbours.containsKey(v1)) {
                        neighbours.get(v1).add(v2);
                    } else {
                        neighbours.put(v1, new HashSet<Vector3dh>());
                        neighbours.get(v1).add(v2);
                    }
                    if (neighbours.containsKey(v2)) {
                        neighbours.get(v2).add(v1);
                    } else {
                        neighbours.put(v2, new HashSet<Vector3dh>());
                        neighbours.get(v2).add(v1);
                    }
                    if (m1.contains(v1)) {
                        if (m2.contains(v1)) {
                            if (!m3.contains(v1)) {
                                m3.add(v1);
                            }
                        } else {
                            m2.add(v1);
                        }
                    } else {
                        m1.add(v1);
                    }
                    if (m1.contains(v2)) {
                        if (m2.contains(v2)) {
                            if (!m3.contains(v2)) {
                                m3.add(v2);
                            }
                        } else {
                            m2.add(v2);
                        }
                    } else {
                        m1.add(v2);
                    }
                }
                for (Vector3dd v : fixedVertices) {
                    fixedVertices2.add(hashedRelation.get(v));
                }
                m2.removeAll(m3);
                fixedVertices2.removeAll(m2);
            }

            try
            {
                new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
                {
                    @Override
                    public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                    {
                        try
                        {
                            monitor.beginTask(I18n.VM_Lines2Pattern, IProgressMonitor.UNKNOWN);

                            final int lc = linesToParseHashed.size();

                            final Thread[] threads = new Thread[chunks];

                            final String vertCount = "/" + lc + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                            final AtomicInteger counter2 = new AtomicInteger(0);

                            final Lock rlock = new ReentrantLock(true);

                            for (int t = 0; t < chunks; ++t) {
                                final int[] start = new int[] { t };
                                threads[t] = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        int counter = start[0];
                                        HashSet<Vector3dh> allVertices = new HashSet<Vector3dh>();
                                        Vector3d normal = null;
                                        for (int i = 0; i < lc; i++) {
                                            if (counter == 0) {
                                                counter = chunks;

                                                Object[] messageArguments = {counter2.toString(), vertCount};
                                                MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                                                formatter.setLocale(MyLanguage.LOCALE);
                                                formatter.applyPattern(I18n.VM_Triangulate);

                                                monitor.subTask(formatter.format(messageArguments));
                                                counter2.incrementAndGet();
                                                if (monitor.isCanceled()) {
                                                    return;
                                                }
                                                for (int j = i + 1; j < lc; j++) {
                                                    for (int k = j + 1; k < lc; k++) {
                                                        for(int l = 0; l < 2; l++) {
                                                            allVertices.add(linesToParseHashed.get(i).get(l));
                                                            allVertices.add(linesToParseHashed.get(j).get(l));
                                                            allVertices.add(linesToParseHashed.get(k).get(l));
                                                        }
                                                        if (allVertices.size() == 3) {
                                                            Vector3dh[] triVerts = new Vector3dh[3];
                                                            int l = 0;
                                                            for (Vector3dh v : allVertices) {
                                                                triVerts[l] = v;
                                                                l++;
                                                            }
                                                            allVertices.clear();
                                                            boolean isInsideTriangle = false;
                                                            if (normal == null) {
                                                                normal = Vector3d.cross(Vector3d.sub(triVerts[2], triVerts[0]), Vector3d.sub(triVerts[1], triVerts[0]));
                                                                normal.normalise(normal);
                                                            }
                                                            for (Vector3dh fixed : fixedVertices2) {
                                                                if (fixed.equals(triVerts[0])) continue;
                                                                if (fixed.equals(triVerts[1])) continue;
                                                                if (fixed.equals(triVerts[2])) continue;
                                                                Set<Vector3dh> n1 = neighbours.get(triVerts[0]);
                                                                Set<Vector3dh> n2 = neighbours.get(triVerts[1]);
                                                                Set<Vector3dh> n3 = neighbours.get(triVerts[2]);
                                                                int nc = 0;
                                                                if (n1.contains(fixed)) nc += 1;
                                                                if (n2.contains(fixed)) nc += 1;
                                                                if (n3.contains(fixed)) nc += 1;
                                                                if (nc > 1) {
                                                                    if (intersectRayTriangle(fixed, normal, triVerts[0], triVerts[1], triVerts[2])) {
                                                                        isInsideTriangle = true;
                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                            if (isInsideTriangle) continue;

                                                            // Check collinearity
                                                            {
                                                                Vector3d vertexA = new Vector3d(triVerts[0]);
                                                                Vector3d vertexB = new Vector3d(triVerts[1]);
                                                                Vector3d vertexC = new Vector3d(triVerts[2]);
                                                                Vector3d vertexA2 = new Vector3d();
                                                                Vector3d vertexB2 = new Vector3d();
                                                                Vector3d.sub(vertexA, vertexC, vertexA2);
                                                                Vector3d.sub(vertexB, vertexC, vertexB2);
                                                                if  (Vector3d.angle(vertexA2, vertexB2) < Threshold.collinear_angle_minimum) continue;
                                                            }

                                                            {
                                                                HashSet<ArrayList<Vector3dd>> threeLines = new HashSet<ArrayList<Vector3dd>>();
                                                                threeLines.add(linesToParse.get(i));
                                                                threeLines.add(linesToParse.get(j));
                                                                threeLines.add(linesToParse.get(k));
                                                                ArrayList<Vector3dd> intersected = null;
                                                                for (Iterator<ArrayList<Vector3dd>> iterator = threeLines.iterator(); iterator.hasNext();) {
                                                                    ArrayList<Vector3dd> line = iterator.next();
                                                                    Vector3dd v1 = line.get(0);
                                                                    Vector3dd v2 = line.get(1);
                                                                    Vector3d sp = Vector3dd.sub(v2, v1);
                                                                    Vector3d dir = new Vector3d();
                                                                    BigDecimal len = sp.normalise(dir);
                                                                    for (ArrayList<Vector3dd> line2 : colourLines) {
                                                                        if (line2 != line) {
                                                                            TreeSet<Vector3dd> allVertices1 = new TreeSet<Vector3dd>();
                                                                            for(int l1 = 0; l1 < 2; l1++) {
                                                                                allVertices1.add(line.get(l1));
                                                                                allVertices1.add(line2.get(l1));
                                                                            }
                                                                            if (allVertices1.size() == 4) {
                                                                                if (intersectLineLineSegmentUnidirectionalFast(v1, v2, sp, dir, len, line2.get(0), line2.get(1))) {
                                                                                    intersected = line2;
                                                                                    break;
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                    if (intersected != null) {
                                                                        break;
                                                                    }
                                                                }
                                                                rlock.lock();
                                                                if (MathHelper.directionOfVectors(Vector3d.cross(Vector3d.sub(triVerts[2], triVerts[0]), Vector3d.sub(triVerts[1], triVerts[0])), originalNormal) == 1) {
                                                                    resultVertices.add(triVerts[0]);
                                                                    resultVertices.add(triVerts[1]);
                                                                    resultVertices.add(triVerts[2]);
                                                                } else {
                                                                    resultVertices.add(triVerts[0]);
                                                                    resultVertices.add(triVerts[2]);
                                                                    resultVertices.add(triVerts[1]);
                                                                }
                                                                if (intersected != null) {
                                                                    resultColours.add(colours.get(intersected) != null ? colours.get(intersected) : View.getLDConfigColour(16));
                                                                    resultIsLine.add(0);
                                                                } else {
                                                                    resultColours.add(View.getLDConfigColour(16));
                                                                    resultIsLine.add(0);
                                                                }
                                                                rlock.unlock();
                                                            }
                                                        } else {
                                                            allVertices.clear();
                                                        }
                                                    }
                                                }
                                            }
                                            counter -= 1;
                                        }
                                    }
                                });
                                threads[t].start();
                            }
                            boolean isRunning = true;
                            while (isRunning) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                }
                                isRunning = false;
                                for (Thread thread : threads) {
                                    if (thread.isAlive())
                                        isRunning = true;
                                }
                            }
                            if (monitor.isCanceled()) {
                                selectedLines.addAll(originalSelectionLines);
                                selectedTriangles.addAll(originalSelectionTriangles);
                                selectedQuads.addAll(originalSelectionQuads);
                                selectedData.addAll(originalSelectionTriangles);
                                selectedData.addAll(originalSelectionQuads);
                                selectedData.addAll(originalSelectionLines);
                                originalSelectionLines.clear();
                                return;
                            }
                        } finally {
                            monitor.done();
                        }
                    }
                });
            }
            catch (InvocationTargetException consumed) {
            } catch (InterruptedException consumed) {
            }

            if (originalSelectionLines.isEmpty()) return;

            newTriangles.addAll(MathHelper.triangulatePointGroups(resultColours, resultVertices, resultIsLine, View.DUMMY_REFERENCE, linkedDatFile));

            NLogger.debug(getClass(), "Check for identical vertices and collinearity."); //$NON-NLS-1$
            final Set<GData3> trisToDelete2 = new HashSet<GData3>();
            {
                for (GData3 g3 : newTriangles) {
                    Vertex[] verts = triangles.get(g3);
                    Set<Vertex> verts2 = new TreeSet<Vertex>();
                    for (Vertex vert : verts) {
                        verts2.add(vert);
                    }
                    if (verts2.size() < 3 || g3.isCollinear()) {
                        trisToDelete2.add(g3);
                    }
                }
            }

            // Append the new data
            for (GData3 tri : newTriangles) {
                linkedDatFile.addToTail(tri);
            }

            NLogger.debug(getClass(), "Delete new, but invalid objects."); //$NON-NLS-1$

            newTriangles.removeAll(trisToDelete2);
            selectedTriangles.addAll(trisToDelete2);
            selectedData.addAll(selectedTriangles);
            delete(false, false);

            // Round to 6 decimal places

            selectedTriangles.addAll(newTriangles);
            selectedData.addAll(selectedTriangles);

            NLogger.debug(getClass(), "Round."); //$NON-NLS-1$
            roundSelection(6, 10, true, false);

            setModified(true, true);

            NLogger.debug(getClass(), "Done."); //$NON-NLS-1$

            validateState();
        }
    }

    public void symSplitter(SymSplitterSettings sims) {

        if (linkedDatFile.isReadOnly()) return;

        setModified_NoSync();
        final String originalContent = linkedDatFile.getText();

        clearSelection();

        final BigDecimal o = sims.getOffset();
        final BigDecimal p = sims.getPrecision();
        final boolean needMerge = BigDecimal.ZERO.compareTo(p) != 0;
        final int sp = sims.getSplitPlane();
        boolean wasModified = false;

        if (sims.isCutAcross()) {
            // First, do the cutting with intersector :)

            // We have to create a really big quad at the cutting plane
            final GData4 splitPlane;
            final BigDecimal a = new BigDecimal(100000000);
            final BigDecimal an = a.negate();
            switch (sp) {
            case SymSplitterSettings.Z_PLUS:
            case SymSplitterSettings.Z_MINUS:
                splitPlane = new GData4(16, .5f, .5f, .5f, 1f, a, a, o, a, an, o, an, an, o, an, a, o, new Vector3d(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE), View.DUMMY_REFERENCE, linkedDatFile);
                break;
            case SymSplitterSettings.Y_PLUS:
            case SymSplitterSettings.Y_MINUS:
                splitPlane = new GData4(16, .5f, .5f, .5f, 1f, a, o, a, a, o, an, an, o, an, an, o, a, new Vector3d(BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO), View.DUMMY_REFERENCE, linkedDatFile);
                break;
            case SymSplitterSettings.X_PLUS:
            case SymSplitterSettings.X_MINUS:
                splitPlane = new GData4(16, .5f, .5f, .5f, 1f, o, a, a, o, a, an, o, an, an, o, an, a, new Vector3d(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO), View.DUMMY_REFERENCE, linkedDatFile);
                break;
            default:
                return;
            }
            linkedDatFile.addToTail(splitPlane);

            // Now we have to select the data which is cut by the split plane

            selectAll(null, true);

            {
                HashSet<GData> dataToCut = new HashSet<GData>();
                for (GData g : selectedData) {
                    if (!lineLinkedToVertices.containsKey(g)) continue;
                    final Vertex[] verts;
                    switch (g.type()) {
                    case 2:
                        verts = lines.get(g);
                        break;
                    case 3:
                        verts = triangles.get(g);
                        break;
                    case 4:
                        verts = quads.get(g);
                        break;
                    case 5:
                        Vertex[] v2 = condlines.get(g);
                        verts = new Vertex[]{v2[0], v2[1]};
                        break;
                    default:
                        continue;
                    }


                    final int targetValue = verts.length;
                    int currentValue = 0;
                    int neg = 0;
                    int pos = 0;
                    for (Vertex v : verts) {
                        switch (sp) {
                        case SymSplitterSettings.Z_PLUS:
                            if (v.Z.compareTo(o) > 0) {
                                pos++;
                            } else {
                                neg++;
                            }
                            break;
                        case SymSplitterSettings.Z_MINUS:
                            if (v.Z.compareTo(o) > 0) {
                                neg++;
                            } else {
                                pos++;
                            }
                            break;
                        case SymSplitterSettings.Y_PLUS:
                            if (v.Y.compareTo(o) > 0) {
                                pos++;
                            } else {
                                neg++;
                            }
                            break;
                        case SymSplitterSettings.Y_MINUS:
                            if (v.Y.compareTo(o) > 0) {
                                neg++;
                            } else {
                                pos++;
                            }
                            break;
                        case SymSplitterSettings.X_PLUS:
                            if (v.X.compareTo(o) > 0) {
                                pos++;
                            } else {
                                neg++;
                            }
                            break;
                        case SymSplitterSettings.X_MINUS:
                            if (v.X.compareTo(o) > 0) {
                                neg++;
                            } else {
                                pos++;
                            }
                            break;
                        }
                    }
                    currentValue = Math.max(neg, pos);
                    if (targetValue != currentValue) {
                        dataToCut.add(g);
                    }
                }

                clearSelection();

                for (GData g : dataToCut) {
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
                        continue;
                    }
                    selectedData.add(g);
                }
            }

            // Remove the quad from the selection
            selectedData.remove(splitPlane);
            selectedQuads.remove(splitPlane);

            List<OpenGLRenderer> renderers = Editor3DWindow.getRenders();
            for (OpenGLRenderer renderer : renderers) {
                if (renderer.getC3D().getLockableDatFileReference().equals(linkedDatFile)) {
                    linkedDatFile.setLastSelectedComposite(renderer.getC3D());
                }
            }

            intersector(new IntersectorSettings(), false);

            showAll();
            clearSelection();

            // Remove the split plane
            selectedData.add(splitPlane);
            selectedQuads.add(splitPlane);
            delete(false, false);
        }


        // Get header, since it is the same on all three sets (behind, between, before)
        final StringBuilder headerSb = new StringBuilder();
        final StringBuilder beforeSb = new StringBuilder();
        final StringBuilder betweenSb = new StringBuilder();
        final StringBuilder behindSb = new StringBuilder();
        final GData lastHeaderLine;
        {
            GData g = linkedDatFile.getDrawChainStart();
            while ((g = g.getNext()) != null) {
                headerSb.append(g.toString());
                headerSb.append(StringHelper.getLineDelimiter());
                if (g.getNext() == null || g.getNext().type() != 0 && !(g.getNext().type() == 6 && (
                        ((GDataBFC) g.getNext()).type == BFC.CCW_CLIP ||
                        ((GDataBFC) g.getNext()).type == BFC.CW_CLIP ||
                        ((GDataBFC) g.getNext()).type == BFC.NOCERTIFY
                        ))) {
                    break;
                }
            }
            if (g == null) {
                return;
            } else {
                lastHeaderLine = g;
            }
        }

        // Merge vertices to the plane
        if (needMerge) {
            selectAll(null, true);
            TreeSet<Vertex> allVertices = new TreeSet<Vertex>();
            allVertices.addAll(selectedVertices);
            clearSelection();
            for (Vertex v : allVertices) {
                switch (sp) {
                case SymSplitterSettings.Z_PLUS:
                case SymSplitterSettings.Z_MINUS:
                    if (p.compareTo(v.Z.subtract(o).abs()) > 0) {
                        wasModified = changeVertexDirectFast(v, new Vertex(v.X, v.Y, o), true) || wasModified;
                    }
                    break;
                case SymSplitterSettings.Y_PLUS:
                case SymSplitterSettings.Y_MINUS:
                    if (p.compareTo(v.Y.subtract(o).abs()) > 0) {
                        wasModified = changeVertexDirectFast(v, new Vertex(v.X, o, v.Z), true) || wasModified;
                    }
                    break;
                case SymSplitterSettings.X_PLUS:
                case SymSplitterSettings.X_MINUS:
                    if (p.compareTo(v.X.subtract(o).abs()) > 0) {
                        wasModified = changeVertexDirectFast(v, new Vertex(o, v.Y, v.Z), true) || wasModified;
                    }
                    break;
                }
            }
        }

        // Separate the data according the plane and detect invalid data (identical vertices)

        selectAll(null, true);

        {

            HashSet<GData> subfilesWithInvertnext = new HashSet<GData>();

            HashSet<GData> before = new HashSet<GData>();
            HashSet<GData> between = new HashSet<GData>();
            HashSet<GData> behind = new HashSet<GData>();

            for (GData g : selectedData) {
                if (!lineLinkedToVertices.containsKey(g)) continue;
                boolean forceMiddle = false;
                final Vertex[] verts;
                switch (g.type()) {
                case 1:
                    GData1 g1 = (GData1) g;
                    {
                        boolean hasInvertnext = false;
                        GData invertNextData = g1.getBefore();
                        while (invertNextData != null && invertNextData.type() != 1 && (invertNextData.type() != 6 || ((GDataBFC) invertNextData).type != BFC.INVERTNEXT)) {
                            invertNextData = invertNextData.getBefore();
                        }
                        if (invertNextData != null && invertNextData.type() == 6) {
                            hasInvertnext = true;
                        }
                        if (hasInvertnext) {
                            subfilesWithInvertnext.add(g1);
                        }
                    }
                    String shortName = g1.shortName.trim();
                    if (shortName.contains("stug") || shortName.contains("stud.dat") || shortName.contains("stud2.dat")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        forceMiddle = true;
                    }
                    verts = new Vertex[]{new Vertex(g1.accurateProductMatrix.M30, g1.accurateProductMatrix.M31, g1.accurateProductMatrix.M32)};
                    break;
                case 2:
                    verts = lines.get(g);
                    {
                        TreeSet<Vertex> vv = new TreeSet<Vertex>();
                        for (Vertex v : verts) {
                            vv.add(v);
                        }
                        if (vv.size() != 2) continue;
                    }
                    break;
                case 3:
                    verts = triangles.get(g);
                    {
                        TreeSet<Vertex> vv = new TreeSet<Vertex>();
                        for (Vertex v : verts) {
                            vv.add(v);
                        }
                        if (vv.size() != 3) continue;
                    }
                    break;
                case 4:
                    verts = quads.get(g);
                    {
                        TreeSet<Vertex> vv = new TreeSet<Vertex>();
                        for (Vertex v : verts) {
                            vv.add(v);
                        }
                        if (vv.size() != 4) continue;
                    }
                    break;
                case 5:
                    Vertex[] v2 = condlines.get(g);
                    verts = new Vertex[]{v2[0], v2[1]};
                    {
                        TreeSet<Vertex> vv = new TreeSet<Vertex>();
                        for (Vertex v : verts) {
                            vv.add(v);
                        }
                        if (vv.size() != 2) continue;
                    }
                    break;
                default:
                    continue;
                }

                final int targetValue = verts.length;
                int currentValue = 0;
                int neg = 0;
                int pos = 0;
                for (Vertex v : verts) {
                    switch (sp) {
                    case SymSplitterSettings.Z_PLUS:
                        switch (v.Z.compareTo(o)) {
                        case -1:
                            neg++;
                            break;
                        case 0:
                            neg++;
                            pos++;
                            break;
                        case 1:
                            pos++;
                            break;
                        }
                        break;
                    case SymSplitterSettings.Z_MINUS:
                        switch (v.Z.compareTo(o)) {
                        case -1:
                            pos++;
                            break;
                        case 0:
                            neg++;
                            pos++;
                            break;
                        case 1:
                            neg++;
                            break;
                        }
                        break;
                    case SymSplitterSettings.Y_PLUS:
                        switch (v.Y.compareTo(o)) {
                        case -1:
                            neg++;
                            break;
                        case 0:
                            neg++;
                            pos++;
                            break;
                        case 1:
                            pos++;
                            break;
                        }
                        break;
                    case SymSplitterSettings.Y_MINUS:
                        switch (v.Y.compareTo(o)) {
                        case -1:
                            pos++;
                            break;
                        case 0:
                            neg++;
                            pos++;
                            break;
                        case 1:
                            neg++;
                            break;
                        }
                        break;
                    case SymSplitterSettings.X_PLUS:
                        switch (v.X.compareTo(o)) {
                        case -1:
                            neg++;
                            break;
                        case 0:
                            neg++;
                            pos++;
                            break;
                        case 1:
                            pos++;
                            break;
                        }
                        break;
                    case SymSplitterSettings.X_MINUS:
                        switch (v.X.compareTo(o)) {
                        case -1:
                            pos++;
                            break;
                        case 0:
                            neg++;
                            pos++;
                            break;
                        case 1:
                            neg++;
                            break;
                        }
                        break;
                    }
                }
                currentValue = Math.max(neg, pos);
                if (forceMiddle || targetValue == currentValue && neg == pos) {
                    between.add(g);
                } else if (targetValue != currentValue) {
                    between.add(g);
                } else if (pos == targetValue) {
                    before.add(g);
                } else if (neg == targetValue) {
                    behind.add(g);
                } else {
                    between.add(g);
                }
            }

            // Colourise only before and between

            final GData tail = linkedDatFile.getDrawChainTail();
            GColour blue = View.hasLDConfigColour(1) ? View.getLDConfigColour(1) : new GColour(-1, 0f, 0f, 1f, 1f);
            GColour yellow = View.hasLDConfigColour(14) ? View.getLDConfigColour(14) : new GColour(-1, 1f, 1f, 0f, 1f);
            GColour red = View.hasLDConfigColour(4) ? View.getLDConfigColour(4) : new GColour(-1, 1f, 0f, 0f, 1f);
            GColour lightBlue = View.hasLDConfigColour(9) ? View.getLDConfigColour(9) : new GColour(-1, 1f, .5f, .5f, 1f);
            GColour lightGreen = View.hasLDConfigColour(10) ? View.getLDConfigColour(10) : new GColour(-1, .5f, 1f, .5f, 1f);
            GColour violet = View.hasLDConfigColour(5) ? View.getLDConfigColour(5) : new GColour(-1, 1f, 0f, 1f, 1f);
            {
                GData g = lastHeaderLine;
                while ((g = g.getNext()) != null) {
                    if (g.type() < 1 || g.type() > 5) {
                        if (g.type() == 6 && ((GDataBFC) g).type == BFC.INVERTNEXT) continue;
                        beforeSb.append(g.toString());
                        if (!g.equals(tail)) beforeSb.append(StringHelper.getLineDelimiter());
                    } else {
                        if (before.contains(g)) {
                            if (sims.isColourise() && lineLinkedToVertices.containsKey(g)) {
                                switch (g.type()) {
                                case 3:
                                    beforeSb.append(((GData3) g).colourReplace(lightBlue.toString()));
                                    break;
                                case 4:
                                    beforeSb.append(((GData4) g).colourReplace(lightBlue.toString()));
                                    break;
                                case 1:
                                    if (subfilesWithInvertnext.contains(g)) {
                                        beforeSb.append(new GDataBFC(BFC.INVERTNEXT).toString());
                                    }
                                    beforeSb.append(((GData1) g).colourReplace(blue.toString()));
                                    break;
                                case 2:
                                    beforeSb.append(((GData2) g).colourReplace(blue.toString()));
                                    break;
                                case 5:
                                    beforeSb.append(((GData5) g).colourReplace(blue.toString()));
                                    break;
                                default:
                                    break;
                                }
                            } else {
                                if (subfilesWithInvertnext.contains(g)) {
                                    beforeSb.append(new GDataBFC(BFC.INVERTNEXT).toString());
                                }
                                beforeSb.append(g.toString());
                            }
                            if (!g.equals(tail)) beforeSb.append(StringHelper.getLineDelimiter());
                        }
                    }
                }
            }
            {
                GData g = lastHeaderLine;
                while ((g = g.getNext()) != null) {
                    if (g.type() < 1 || g.type() > 5) {
                        if (g.type() == 6 && ((GDataBFC) g).type == BFC.INVERTNEXT) continue;
                        betweenSb.append(g.toString());
                        if (!g.equals(tail)) betweenSb.append(StringHelper.getLineDelimiter());
                    } else {
                        if (between.contains(g)) {

                            if (sims.isValidate() && lineLinkedToVertices.containsKey(g)) {

                                boolean isSymmetrical = true;

                                if (g.type() != 1) {
                                    // Check symmetry

                                    final Vertex[] verts;
                                    switch (g.type()) {
                                    case 2:
                                        verts = lines.get(g);
                                        break;
                                    case 3:
                                        verts = triangles.get(g);
                                        break;
                                    case 4:
                                        verts = quads.get(g);
                                        break;
                                    case 5:
                                        Vertex[] v2 = condlines.get(g);
                                        verts = new Vertex[]{v2[0], v2[1]};
                                        break;
                                    default:
                                        continue;
                                    }

                                    switch (sp) {
                                    case SymSplitterSettings.Z_PLUS:
                                    case SymSplitterSettings.Z_MINUS:
                                        for (int i = 0; i < verts.length - 1; i++) {
                                            int j = (i + 1) % verts.length;
                                            BigDecimal di = verts[i].Z.subtract(o);
                                            BigDecimal dj = verts[j].Z.subtract(o);
                                            if (di.signum() != dj.signum()) {
                                                if (di.abs().subtract(dj.abs()).abs().compareTo(p) > 0) {
                                                    isSymmetrical = false;
                                                    break;
                                                }
                                            }
                                        }
                                        break;
                                    case SymSplitterSettings.Y_PLUS:
                                    case SymSplitterSettings.Y_MINUS:
                                        for (int i = 0; i < verts.length - 1; i++) {
                                            int j = (i + 1) % verts.length;
                                            BigDecimal di = verts[i].Y.subtract(o);
                                            BigDecimal dj = verts[j].Y.subtract(o);
                                            if (di.signum() != dj.signum()) {
                                                if (di.abs().subtract(dj.abs()).abs().compareTo(p) > 0) {
                                                    isSymmetrical = false;
                                                    break;
                                                }
                                            }
                                        }
                                        break;
                                    case SymSplitterSettings.X_PLUS:
                                    case SymSplitterSettings.X_MINUS:
                                        for (int i = 0; i < verts.length - 1; i++) {
                                            int j = (i + 1) % verts.length;
                                            BigDecimal di = verts[i].X.subtract(o);
                                            BigDecimal dj = verts[j].X.subtract(o);
                                            if (di.signum() != dj.signum()) {
                                                if (di.abs().subtract(dj.abs()).abs().compareTo(p) > 0) {
                                                    isSymmetrical = false;
                                                    break;
                                                }
                                            }
                                        }
                                        break;
                                    }
                                }

                                switch (g.type()) {
                                case 3:
                                    if (isSymmetrical) {
                                        betweenSb.append(((GData3) g).colourReplace(yellow.toString()));
                                    } else {
                                        betweenSb.append(((GData3) g).colourReplace(violet.toString()));
                                    }
                                    break;
                                case 4:
                                    if (isSymmetrical) {
                                        betweenSb.append(((GData4) g).colourReplace(yellow.toString()));
                                    } else {
                                        betweenSb.append(((GData4) g).colourReplace(violet.toString()));
                                    }
                                    break;
                                case 1:
                                    if (subfilesWithInvertnext.contains(g)) {
                                        betweenSb.append(new GDataBFC(BFC.INVERTNEXT).toString());
                                    }
                                    betweenSb.append(((GData1) g).colourReplace(yellow.toString()));
                                    break;
                                case 2:
                                    if (isSymmetrical) {
                                        betweenSb.append(((GData2) g).colourReplace(red.toString()));
                                    } else {
                                        betweenSb.append(((GData2) g).colourReplace(lightGreen.toString()));
                                    }

                                    break;
                                case 5:
                                    if (isSymmetrical) {
                                        betweenSb.append(((GData5) g).colourReplace(red.toString()));
                                    } else {
                                        betweenSb.append(((GData5) g).colourReplace(lightGreen.toString()));
                                    }
                                    break;
                                default:
                                    break;
                                }

                            } else if (sims.isColourise() && lineLinkedToVertices.containsKey(g)) {
                                switch (g.type()) {
                                case 3:
                                    betweenSb.append(((GData3) g).colourReplace(yellow.toString()));
                                    break;
                                case 4:
                                    betweenSb.append(((GData4) g).colourReplace(yellow.toString()));
                                    break;
                                case 1:
                                    if (subfilesWithInvertnext.contains(g)) {
                                        betweenSb.append(new GDataBFC(BFC.INVERTNEXT).toString());
                                    }
                                    betweenSb.append(((GData1) g).colourReplace(yellow.toString()));
                                    break;
                                case 2:
                                    betweenSb.append(((GData2) g).colourReplace(red.toString()));
                                    break;
                                case 5:
                                    betweenSb.append(((GData5) g).colourReplace(red.toString()));
                                    break;
                                default:
                                    break;
                                }
                            } else {
                                if (subfilesWithInvertnext.contains(g)) {
                                    betweenSb.append(new GDataBFC(BFC.INVERTNEXT).toString());
                                }
                                betweenSb.append(g.toString());
                            }
                            if (!g.equals(tail)) betweenSb.append(StringHelper.getLineDelimiter());
                        }
                    }
                }
            }
            {
                GData g = lastHeaderLine;
                while ((g = g.getNext()) != null) {
                    if (g.type() < 1 || g.type() > 5) {
                        if (g.type() == 6 && ((GDataBFC) g).type == BFC.INVERTNEXT) continue;
                        behindSb.append(g.toString());
                        if (!g.equals(tail)) behindSb.append(StringHelper.getLineDelimiter());
                    } else {
                        if (behind.contains(g)) {
                            if (subfilesWithInvertnext.contains(g)) {
                                behindSb.append(new GDataBFC(BFC.INVERTNEXT).toString());
                            }
                            behindSb.append(g.toString());
                            if (!g.equals(tail)) behindSb.append(StringHelper.getLineDelimiter());
                        }
                    }
                }
            }

            String headerS = headerSb.toString();
            String beforeS = beforeSb.toString();
            String betweenS = betweenSb.toString();
            String behindS = behindSb.toString();

            if (!headerS.endsWith(StringHelper.getLineDelimiter())) {
                headerS = headerS + StringHelper.getLineDelimiter();
            }
            headerS = headerS + I18n.VM_SymsplitterFront + StringHelper.getLineDelimiter();
            if (!beforeS.endsWith(StringHelper.getLineDelimiter())) {
                beforeS = beforeS + StringHelper.getLineDelimiter();
            }
            beforeS = beforeS + I18n.VM_SymsplitterBetween + StringHelper.getLineDelimiter();
            if (!betweenS.endsWith(StringHelper.getLineDelimiter())) {
                betweenS = betweenS + StringHelper.getLineDelimiter();
            }
            betweenS = betweenS + I18n.VM_SymsplitterBehind + StringHelper.getLineDelimiter();

            String symSplitterOutput = headerS + beforeS + betweenS + behindS;

            if (wasModified || !symSplitterOutput.equals(originalContent)) {
                if (!Project.getUnsavedFiles().contains(linkedDatFile)) {
                    Project.addUnsavedFile(linkedDatFile);
                    Editor3DWindow.getWindow().updateTree_unsavedEntries();
                }

                GDataCSG.resetCSG();
                GDataCSG.forceRecompile();
                setModified_NoSync();
                linkedDatFile.setText(symSplitterOutput);
                linkedDatFile.parseForData(true);

                setModified_NoSync();

                // Separate the data according the plane and hide or show it

                if (sims.getHideLevel() > 0) {

                    selectAll(null, true);

                    before.clear();
                    between.clear();
                    behind.clear();

                    for (GData g : selectedData) {
                        if (!lineLinkedToVertices.containsKey(g)) continue;
                        boolean forceMiddle = false;
                        final Vertex[] verts;
                        switch (g.type()) {
                        case 1:
                            GData1 g1 = (GData1) g;
                            String shortName = g1.shortName.trim();
                            if (shortName.contains("stug") || shortName.contains("stud.dat") || shortName.contains("stud2.dat")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                forceMiddle = true;
                            }
                            verts = new Vertex[]{new Vertex(g1.accurateProductMatrix.M30, g1.accurateProductMatrix.M31, g1.accurateProductMatrix.M32)};
                            break;
                        case 2:
                            verts = lines.get(g);
                            {
                                TreeSet<Vertex> vv = new TreeSet<Vertex>();
                                for (Vertex v : verts) {
                                    vv.add(v);
                                }
                                if (vv.size() != 2) continue;
                            }
                            break;
                        case 3:
                            verts = triangles.get(g);
                            {
                                TreeSet<Vertex> vv = new TreeSet<Vertex>();
                                for (Vertex v : verts) {
                                    vv.add(v);
                                }
                                if (vv.size() != 3) continue;
                            }
                            break;
                        case 4:
                            verts = quads.get(g);
                            {
                                TreeSet<Vertex> vv = new TreeSet<Vertex>();
                                for (Vertex v : verts) {
                                    vv.add(v);
                                }
                                if (vv.size() != 4) continue;
                            }
                            break;
                        case 5:
                            Vertex[] v2 = condlines.get(g);
                            verts = new Vertex[]{v2[0], v2[1]};
                            {
                                TreeSet<Vertex> vv = new TreeSet<Vertex>();
                                for (Vertex v : verts) {
                                    vv.add(v);
                                }
                                if (vv.size() != 2) continue;
                            }
                            break;
                        default:
                            continue;
                        }

                        final int targetValue = verts.length;
                        int currentValue = 0;
                        int neg = 0;
                        int pos = 0;
                        for (Vertex v : verts) {
                            switch (sp) {
                            case SymSplitterSettings.Z_PLUS:
                                switch (v.Z.compareTo(o)) {
                                case -1:
                                    neg++;
                                    break;
                                case 0:
                                    neg++;
                                    pos++;
                                    break;
                                case 1:
                                    pos++;
                                    break;
                                }
                                break;
                            case SymSplitterSettings.Z_MINUS:
                                switch (v.Z.compareTo(o)) {
                                case -1:
                                    pos++;
                                    break;
                                case 0:
                                    neg++;
                                    pos++;
                                    break;
                                case 1:
                                    neg++;
                                    break;
                                }
                                break;
                            case SymSplitterSettings.Y_PLUS:
                                switch (v.Y.compareTo(o)) {
                                case -1:
                                    neg++;
                                    break;
                                case 0:
                                    neg++;
                                    pos++;
                                    break;
                                case 1:
                                    pos++;
                                    break;
                                }
                                break;
                            case SymSplitterSettings.Y_MINUS:
                                switch (v.Y.compareTo(o)) {
                                case -1:
                                    pos++;
                                    break;
                                case 0:
                                    neg++;
                                    pos++;
                                    break;
                                case 1:
                                    neg++;
                                    break;
                                }
                                break;
                            case SymSplitterSettings.X_PLUS:
                                switch (v.X.compareTo(o)) {
                                case -1:
                                    neg++;
                                    break;
                                case 0:
                                    neg++;
                                    pos++;
                                    break;
                                case 1:
                                    pos++;
                                    break;
                                }
                                break;
                            case SymSplitterSettings.X_MINUS:
                                switch (v.X.compareTo(o)) {
                                case -1:
                                    pos++;
                                    break;
                                case 0:
                                    neg++;
                                    pos++;
                                    break;
                                case 1:
                                    neg++;
                                    break;
                                }
                                break;
                            }
                        }
                        currentValue = Math.max(neg, pos);
                        if (forceMiddle || targetValue == currentValue && neg == pos) {
                            between.add(g);
                        } else if (targetValue != currentValue) {
                            between.add(g);
                        } else if (pos == targetValue) {
                            before.add(g);
                        } else if (neg == targetValue) {
                            behind.add(g);
                        } else {
                            between.add(g);
                        }
                    }

                    hiddenVertices.addAll(selectedVertices);
                    clearSelection();

                    switch (sims.getHideLevel()) {
                    case 1: // between
                        for (GData g : before) {
                            hide(g);
                        }
                        for (GData g : behind) {
                            hide(g);
                        }
                        break;
                    case 2: // before
                        for (GData g : between) {
                            hide(g);
                        }
                        for (GData g : behind) {
                            hide(g);
                        }
                        break;
                    case 3: // behind
                        for (GData g : before) {
                            hide(g);
                        }
                        for (GData g : between) {
                            hide(g);
                        }
                        break;
                    default:
                        break;
                    }
                }

                if (isModified()) {
                    setModified(true, true);
                }

            } else {
                setModified(false, true);
            }

        }
        validateState();
    }

    public void unificator(final UnificatorSettings us) {

        if (linkedDatFile.isReadOnly()) return;

        final BigDecimal vt = us.getVertexThreshold().multiply(us.getVertexThreshold());
        final BigDecimal st = us.getSubvertexThreshold().multiply(us.getSubvertexThreshold());

        if (us.getScope() == 0) {
            selectAll(null, true);
        } else {
            for (GData gd : selectedData) {
                Vertex[] verts = null;
                switch (gd.type()) {
                case 2:
                    verts = lines.get(gd);
                    break;
                case 3:
                    verts = triangles.get(gd);
                    break;
                case 4:
                    verts = quads.get(gd);
                    break;
                case 5:
                    verts = condlines.get(gd);
                    break;
                default:
                    continue;
                }
                for (Vertex v : verts) {
                    selectedVertices.add(v);
                }
            }
        }

        final TreeSet<Vertex> selectedVerts = new TreeSet<Vertex>();
        selectedVerts.addAll(selectedVertices);

        clearSelection();

        try
        {
            new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, false, new IRunnableWithProgress()
            {
                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                {
                    monitor.beginTask(I18n.VM_Unificator, IProgressMonitor.UNKNOWN);

                    monitor.subTask(I18n.VM_SortOut);

                    TreeSet<Vertex> subfileVertices = new TreeSet<Vertex>();
                    TreeSet<Vertex> fileVertices = new TreeSet<Vertex>();

                    for (Vertex v : selectedVerts) {

                        boolean isFileVertex = false;

                        if (vertexLinkedToSubfile.containsKey(v)) {
                            // Do not add points for condlines in subparts.
                            Set<VertexManifestation> mani = vertexLinkedToPositionInFile.get(v);
                            int controlPointCondlineInSubfile = 0;
                            for (VertexManifestation vm : mani) {
                                GData gd = vm.getGdata();
                                if (lineLinkedToVertices.containsKey(gd)) {
                                    // Better performance, since we can detect file vertices here!
                                    fileVertices.add(v);
                                    isFileVertex = true;
                                    break;
                                } else if (gd.type() == 5 && vm.getPosition() > 1) {
                                    controlPointCondlineInSubfile++;
                                }
                            }
                            if (controlPointCondlineInSubfile == mani.size()) {
                                continue;
                            }
                            subfileVertices.add(v);
                        }

                        if (!isFileVertex) {
                            Set<VertexManifestation> mani = vertexLinkedToPositionInFile.get(v);
                            for (VertexManifestation vm : mani) {
                                GData gd = vm.getGdata();
                                if (lineLinkedToVertices.containsKey(gd)) {
                                    fileVertices.add(v);
                                    break;
                                }
                            }
                        }
                    }


                    if (us.getSnapOn() == 0 || us.getSnapOn() == 2) {
                        monitor.subTask(I18n.VM_Unify);
                        int i = 0;
                        int j = 0;

                        TreeMap<Vertex, Vertex> mergeTargets = new TreeMap<Vertex, Vertex>();
                        {
                            TreeMap<Vertex, TreeSet<Vertex>> unifyGroups = new TreeMap<Vertex, TreeSet<Vertex>>();
                            TreeSet<Vertex> inGroup = new TreeSet<Vertex>();

                            for (Vertex v1 : fileVertices) {
                                TreeSet<Vertex> group = new TreeSet<Vertex>();
                                group.add(v1);
                                for (Vertex v2 : fileVertices) {
                                    if (j > i && !inGroup.contains(v2)) {
                                        Vector3d v3d1 = new Vector3d(v1);
                                        Vector3d v3d2 = new Vector3d(v2);
                                        if (Vector3d.distSquare(v3d1, v3d2).compareTo(vt) < 0) {
                                            group.add(v2);
                                            inGroup.add(v2);
                                        }
                                    }
                                    j++;
                                }
                                unifyGroups.put(v1, group);
                                i++;
                            }

                            fileVertices.clear();

                            Set<Vertex> keySet = unifyGroups.keySet();
                            for (Vertex key : keySet) {
                                TreeSet<Vertex> group = unifyGroups.get(key);
                                if (group.size() > 1) {
                                    BigDecimal X = BigDecimal.ZERO;
                                    BigDecimal Y = BigDecimal.ZERO;
                                    BigDecimal Z = BigDecimal.ZERO;
                                    BigDecimal gc = new BigDecimal(group.size());
                                    for (Vertex gv : group) {
                                        X = X.add(gv.X);
                                        Y = Y.add(gv.Y);
                                        Z = Z.add(gv.Z);
                                    }
                                    X = X.divide(gc, Threshold.mc);
                                    Y = Y.divide(gc, Threshold.mc);
                                    Z = Z.divide(gc, Threshold.mc);
                                    Vertex newVertex = new Vertex(X, Y, Z);
                                    fileVertices.add(newVertex);
                                    for (Vertex gv : group) {
                                        mergeTargets.put(gv, newVertex);
                                    }
                                } else {
                                    fileVertices.add(key);
                                }
                            }
                        }

                        Set<Vertex> keySet = mergeTargets.keySet();
                        for (Vertex key : keySet) {
                            Vertex target = mergeTargets.get(key);
                            changeVertexDirectFast(key, target, true);
                            selectedVertices.add(target);
                        }
                    }

                    if (us.getSnapOn() == 1 || us.getSnapOn() == 2) {
                        monitor.subTask(I18n.VM_Snap);

                        int i = 0;
                        int j = 0;

                        TreeMap<Vertex, Vertex> mergeTargets = new TreeMap<Vertex, Vertex>();
                        {
                            TreeMap<Vertex, TreeSet<Vertex>> unifyGroups = new TreeMap<Vertex, TreeSet<Vertex>>();
                            TreeSet<Vertex> inGroup = new TreeSet<Vertex>();

                            for (Vertex v1 : subfileVertices) {
                                TreeSet<Vertex> group = new TreeSet<Vertex>();
                                for (Vertex v2 : fileVertices) {
                                    if (j > i && !inGroup.contains(v2)) {
                                        Vector3d v3d1 = new Vector3d(v1);
                                        Vector3d v3d2 = new Vector3d(v2);
                                        if (Vector3d.distSquare(v3d1, v3d2).compareTo(st) < 0) {
                                            group.add(v2);
                                            inGroup.add(v2);
                                        }
                                    }
                                    j++;
                                }
                                unifyGroups.put(v1, group);
                                i++;
                            }

                            fileVertices.clear();

                            Set<Vertex> keySet = unifyGroups.keySet();
                            for (Vertex key : keySet) {
                                TreeSet<Vertex> group = unifyGroups.get(key);
                                if (group.size() > 0) {
                                    for (Vertex gv : group) {
                                        mergeTargets.put(gv, key);
                                    }
                                }
                            }
                        }

                        Set<Vertex> keySet = mergeTargets.keySet();
                        for (Vertex key : keySet) {
                            Vertex target = mergeTargets.get(key);
                            changeVertexDirectFast(key, target, true);
                            selectedVertices.add(target);
                        }
                    }
                }
            });
        } catch (InvocationTargetException consumed) {
        } catch (InterruptedException consumed) {
        }

        // Round selection to 6 decimal places
        selectedVerts.clear();
        selectedVerts.addAll(selectedVertices);

        clearSelection();

        NLogger.debug(getClass(), "Check for identical vertices and collinearity."); //$NON-NLS-1$

        final Set<GData2> linesToDelete2 = new HashSet<GData2>();
        final Set<GData3> trisToDelete2 = new HashSet<GData3>();
        final Set<GData4> quadsToDelete2 = new HashSet<GData4>();
        final Set<GData5> clinesToDelete2 = new HashSet<GData5>();
        {
            for (GData2 g2 : lines.keySet()) {
                if (!lineLinkedToVertices.containsKey(g2)) continue;
                Vertex[] verts = lines.get(g2);
                Set<Vertex> verts2 = new TreeSet<Vertex>();
                for (Vertex vert : verts) {
                    verts2.add(vert);
                }
                if (verts2.size() < 2) {
                    linesToDelete2.add(g2);
                }
            }
            for (GData3 g3 : triangles.keySet()) {
                if (!lineLinkedToVertices.containsKey(g3)) continue;
                Vertex[] verts = triangles.get(g3);
                Set<Vertex> verts2 = new TreeSet<Vertex>();
                for (Vertex vert : verts) {
                    verts2.add(vert);
                }
                if (verts2.size() < 3 || g3.isCollinear()) {
                    trisToDelete2.add(g3);
                }
            }
            for (GData4 g4 : quads.keySet()) {
                if (!lineLinkedToVertices.containsKey(g4)) continue;
                Vertex[] verts = quads.get(g4);
                Set<Vertex> verts2 = new TreeSet<Vertex>();
                for (Vertex vert : verts) {
                    verts2.add(vert);
                }
                if (verts2.size() < 4 || g4.isCollinear()) {
                    quadsToDelete2.add(g4);
                }
            }
            for (GData5 g5 : condlines.keySet()) {
                if (!lineLinkedToVertices.containsKey(g5)) continue;
                Vertex[] verts = condlines.get(g5);
                Set<Vertex> verts2 = new TreeSet<Vertex>();
                for (Vertex vert : verts) {
                    verts2.add(vert);
                }
                if (verts2.size() < 4) {
                    clinesToDelete2.add(g5);
                }
            }
        }

        selectedLines.addAll(linesToDelete2);
        selectedTriangles.addAll(trisToDelete2);
        selectedQuads.addAll(quadsToDelete2);
        selectedCondlines.addAll(clinesToDelete2);
        selectedData.addAll(selectedLines);
        selectedData.addAll(selectedTriangles);
        selectedData.addAll(selectedQuads);
        selectedData.addAll(selectedCondlines);
        delete(false, false);

        // Round selection to 6 decimal places
        selectedVertices.addAll(selectedVerts);

        NLogger.debug(getClass(), "Round."); //$NON-NLS-1$
        roundSelection(6, 10, true, true);

        validateState();
    }

    public void flipSelection() {

        if (linkedDatFile.isReadOnly()) return;

        final Set<GData2> newLines = new HashSet<GData2>();
        final Set<GData3> newTriangles = new HashSet<GData3>();
        final Set<GData4> newQuads = new HashSet<GData4>();
        final Set<GData5> newCondlines = new HashSet<GData5>();

        final Set<GData2> effSelectedLines = new HashSet<GData2>();
        final Set<GData3> effSelectedTriangles = new HashSet<GData3>();
        final Set<GData4> effSelectedQuads = new HashSet<GData4>();
        final Set<GData5> effSelectedCondlines = new HashSet<GData5>();

        final Set<GData2> linesToDelete2 = new HashSet<GData2>();
        final Set<GData3> trisToDelete2 = new HashSet<GData3>();
        final Set<GData4> quadsToDelete2 = new HashSet<GData4>();
        final Set<GData5> clinesToDelete2 = new HashSet<GData5>();

        {
            for (GData2 g2 : selectedLines) {
                if (!lineLinkedToVertices.containsKey(g2)) continue;
                effSelectedLines.add(g2);
            }
            for (GData3 g3 : selectedTriangles) {
                if (!lineLinkedToVertices.containsKey(g3)) continue;
                effSelectedTriangles.add(g3);
            }
            for (GData4 g4 : selectedQuads) {
                if (!lineLinkedToVertices.containsKey(g4)) continue;
                effSelectedQuads.add(g4);
            }
            for (GData5 g5 : selectedCondlines) {
                if (!lineLinkedToVertices.containsKey(g5)) continue;
                effSelectedCondlines.add(g5);
            }
        }

        clearSelection();

        // Special case for coloured triangle pairs (Flipper behaviour)
        {
            HashSet<GData3> surfsToIgnore = new HashSet<GData3>();
            HashMap<GData3, GData3> trianglePair = new HashMap<GData3, GData3>();
            int i = 0;
            int j = 0;
            for (GData3 s1 : effSelectedTriangles) {
                for (GData3 s2 : effSelectedTriangles) {
                    if (j > i && !surfsToIgnore.contains(s2)) {
                        if (s1.colourNumber != 16 && s1.colourNumber == s2.colourNumber && s1.r == s2.r && s1.g == s2.g && s1.b == s2.b && hasSameEdge(s1, s2)) {
                            surfsToIgnore.add(s1);
                            surfsToIgnore.add(s2);
                            trianglePair.put(s1, s2);
                        }
                    }
                    j++;
                }
                i++;
            }
            effSelectedTriangles.removeAll(surfsToIgnore);
            for (GData3 s1 : trianglePair.keySet()) {
                GData3 s2 = trianglePair.get(s1);
                {
                    TreeSet<Vertex> v1 = new TreeSet<Vertex>();
                    TreeSet<Vertex> v2 = new TreeSet<Vertex>();
                    for (Vertex v : triangles.get(s1)) {
                        v1.add(v);
                    }
                    for (Vertex v : triangles.get(s2)) {
                        v2.add(v);
                    }
                    {
                        TreeSet<Vertex> sum = new TreeSet<Vertex>();
                        sum.addAll(v1);
                        sum.addAll(v2);
                        if (sum.size() != 4) continue;
                    }
                    v1.retainAll(v2);
                    if (v1.size() == 2) {

                        Vertex first = null;
                        Vertex second = null;
                        Vertex third = null;
                        Vertex fourth = null;
                        int i1 = 0;
                        for (Vertex v : triangles.get(s1)) {
                            if (!v1.contains(v)) {
                                first = v;
                                break;
                            }
                            i1++;
                        }
                        if (first == null) continue;
                        int i2 = 0;
                        for (Vertex v : triangles.get(s1)) {
                            if ((i2 > i1 || i1 == 2 && i2 == 0) && v1.contains(v)) {
                                second = v;
                                break;
                            }
                            i2++;
                        }
                        if (second == null) continue;
                        for (Vertex v : triangles.get(s2)) {
                            if (!v1.contains(v)) {
                                third = v;
                                break;
                            }
                        }
                        if (third == null) continue;
                        for (Vertex v : triangles.get(s2)) {
                            if (v1.contains(v) && !v.equals(second)) {
                                fourth = v;
                                break;
                            }
                        }
                        if (fourth == null) continue;

                        GData3 n1 = new GData3(s1.colourNumber, s1.r, s1.g, s1.b, s1.a, first, second, third, s1.parent, linkedDatFile);
                        GData3 n2 = new GData3(s1.colourNumber, s1.r, s1.g, s1.b, s1.a, third, fourth, first, s1.parent, linkedDatFile);

                        linkedDatFile.insertAfter(s1, n1);
                        linkedDatFile.insertAfter(s2, n2);

                        newTriangles.add(n1);
                        newTriangles.add(n2);

                        trisToDelete2.add(s1);
                        trisToDelete2.add(s2);

                        Iterator<Vertex> vit = v1.iterator();
                        Vertex cv1 = vit.next();
                        Vertex cv2 = vit.next();
                        GData5 cline = hasCondline(cv1, cv2);
                        if (cline != null && lineLinkedToVertices.containsKey(cline)) {
                            linkedDatFile.insertAfter(cline, new GData5(cline.colourNumber, cline.r, cline.g, cline.b, cline.a, first, third, second, fourth, cline.parent, linkedDatFile));
                            clinesToDelete2.add(cline);
                        }
                    }
                }

            }
        }

        for (GData2 g : effSelectedLines) {
            GData2 n = new GData2(g.colourNumber, g.r, g.g, g.b, g.a, g.X2, g.Y2, g.Z2, g.X1, g.Y1, g.Z1, g.parent, linkedDatFile);
            newLines.add(n);
            linkedDatFile.insertAfter(g, n);
            linesToDelete2.add(g);
        }

        for (GData3 g : effSelectedTriangles) {
            GData3 n = new GData3(g.colourNumber, g.r, g.g, g.b, g.a, new Vertex(g.X3, g.Y3, g.Z3), new Vertex(g.X1, g.Y1, g.Z1), new Vertex(g.X2, g.Y2, g.Z2), g.parent, linkedDatFile);
            newTriangles.add(n);
            linkedDatFile.insertAfter(g, n);
            trisToDelete2.add(g);
        }

        for (GData4 g : effSelectedQuads) {
            GData4 n = new GData4(g.colourNumber, g.r, g.g, g.b, g.a, new Vertex(g.X4, g.Y4, g.Z4), new Vertex(g.X1, g.Y1, g.Z1), new Vertex(g.X2, g.Y2, g.Z2), new Vertex(g.X3, g.Y3, g.Z3), g.parent, linkedDatFile);
            newQuads.add(n);
            linkedDatFile.insertAfter(g, n);
            quadsToDelete2.add(g);
        }

        for (GData5 g : effSelectedCondlines) {
            GData5 n = new GData5(g.colourNumber, g.r, g.g, g.b, g.a, new Vertex(g.X2, g.Y2, g.Z2), new Vertex(g.X1, g.Y1, g.Z1), new Vertex(g.X3, g.Y3, g.Z3), new Vertex(g.X4, g.Y4, g.Z4), g.parent, linkedDatFile);
            newCondlines.add(n);
            linkedDatFile.insertAfter(g, n);
            clinesToDelete2.add(g);
        }

        if (newLines.size() + newTriangles.size() + newQuads.size() + newCondlines.size() > 0) {
            setModified_NoSync();
        }

        selectedLines.addAll(linesToDelete2);
        selectedTriangles.addAll(trisToDelete2);
        selectedQuads.addAll(quadsToDelete2);
        selectedCondlines.addAll(clinesToDelete2);
        selectedData.addAll(selectedLines);
        selectedData.addAll(selectedTriangles);
        selectedData.addAll(selectedQuads);
        selectedData.addAll(selectedCondlines);
        delete(false, false);

        clearSelection();

        selectedLines.addAll(newLines);
        selectedTriangles.addAll(newTriangles);
        selectedQuads.addAll(newQuads);
        selectedCondlines.addAll(newCondlines);
        selectedData.addAll(selectedLines);
        selectedData.addAll(selectedTriangles);
        selectedData.addAll(selectedQuads);
        selectedData.addAll(selectedCondlines);

        if (isModified()) {
            syncWithTextEditors(true);
        }
        validateState();
    }

    public void subdivideCatmullClark() {

        if (linkedDatFile.isReadOnly()) return;

        // Backup selected surfaces
        HashSet<GData> surfsToParse = new HashSet<GData>();

        final Set<GData2> linesToDelete2 = new HashSet<GData2>();
        final Set<GData3> trisToDelete2 = new HashSet<GData3>();
        final Set<GData4> quadsToDelete2 = new HashSet<GData4>();

        final Set<GData2> newLines = new HashSet<GData2>();
        final Set<GData4> newQuads = new HashSet<GData4>();

        {
            for (GData3 g3 : selectedTriangles) {
                if (!lineLinkedToVertices.containsKey(g3)) continue;
                surfsToParse.add(g3);
                trisToDelete2.add(g3);
            }
            for (GData4 g4 : selectedQuads) {
                if (!lineLinkedToVertices.containsKey(g4)) continue;
                surfsToParse.add(g4);
                quadsToDelete2.add(g4);
            }
        }
        if (surfsToParse.isEmpty()) return;

        // Delete all condlines, since the are a PITA for subdivision

        clearSelection();
        selectedCondlines.addAll(condlines.keySet());
        selectedData.addAll(selectedCondlines);
        delete(false, false);

        TreeMap<Vertex, Vertex> newPoints = new TreeMap<Vertex, Vertex>();

        // Calculate new points
        for (Vertex v : vertexLinkedToPositionInFile.keySet()) {
            Set<VertexManifestation> manis = vertexLinkedToPositionInFile.get(v);
            HashSet<Vector3d> midEdge = new HashSet<Vector3d>();
            boolean keepIt = false;
            for (VertexManifestation m : manis) {
                GData gd = m.getGdata();
                switch (gd.type()) {
                case 0:
                    continue;
                case 2:
                    keepIt = true;
                    break;
                case 3:
                {
                    int p = m.getPosition();
                    Vertex[] verts = triangles.get(gd);
                    Vector3d vt = new Vector3d(v);
                    midEdge.add(Vector3d.add(vt, new Vector3d(verts[(p + 1) % 3])));
                    midEdge.add(Vector3d.add(vt, new Vector3d(verts[(p + 2) % 3])));
                }
                break;
                case 4:
                {
                    int p = m.getPosition();
                    Vertex[] verts = quads.get(gd);
                    Vector3d vt = new Vector3d(v);
                    midEdge.add(Vector3d.add(vt, new Vector3d(verts[(p + 1) % 4])));
                    midEdge.add(Vector3d.add(vt, new Vector3d(verts[(p + 3) % 4])));
                }
                break;
                default:
                    continue;
                }
                if (keepIt) break;
            }
            if (keepIt) {
                newPoints.put(v, v);
            } else {
                BigDecimal c = new BigDecimal(midEdge.size() * 2);
                Vector3d np = new Vector3d();
                for (Vector3d vd : midEdge) {
                    np = Vector3d.add(np, vd);
                }
                np.setX(np.X.divide(c, Threshold.mc));
                np.setY(np.Y.divide(c, Threshold.mc));
                np.setZ(np.Z.divide(c, Threshold.mc));
                newPoints.put(v, new Vertex(np));
            }
        }

        for (GData gd : surfsToParse) {
            Vertex[] originalVerts;
            int colourNumber;
            float r;
            float g;
            float b;
            float a;
            switch (gd.type()) {
            case 3:
                originalVerts = triangles.get(gd);
                GData3 g3 = (GData3) gd;
                colourNumber = g3.colourNumber; r = g3.r; g = g3.g; b = g3.b; a = g3.a;
                break;
            case 4:
                originalVerts = quads.get(gd);
                GData4 g4 = (GData4) gd;
                colourNumber = g4.colourNumber; r = g4.r; g = g4.g; b = g4.b; a = g4.a;
                break;
            default:
                continue;
            }

            final int c = originalVerts.length;

            BigDecimal c2 = new BigDecimal(c);
            Vector3d center = new Vector3d();
            for (Vertex vd : originalVerts) {
                center = Vector3d.add(center, new Vector3d(vd));
            }
            center.setX(center.X.divide(c2, Threshold.mc));
            center.setY(center.Y.divide(c2, Threshold.mc));
            center.setZ(center.Z.divide(c2, Threshold.mc));

            Vertex vc = new Vertex(center);

            Vertex[] ve = new Vertex[c];
            for(int i = 0; i < c; i++) {
                ve[i] = new Vertex(Vector3d.add(new Vector3d(originalVerts[i]), new Vector3d(originalVerts[(i + 1) % c])).scaledByHalf());
                GData2 g2 = null;
                if ((g2 = hasEdge(originalVerts[i], originalVerts[(i + 1) % c])) != null) {
                    if (!vertexLinkedToPositionInFile.containsKey(ve[i]) || hasEdge(originalVerts[i], ve[i]) == null) newLines.add(new GData2(24, View.line_Colour_r[0], View.line_Colour_g[0], View.line_Colour_b[0], 1f, originalVerts[i], ve[i], View.DUMMY_REFERENCE, linkedDatFile));
                    if (!vertexLinkedToPositionInFile.containsKey(ve[i]) || hasEdge(originalVerts[(i + 1) % c], ve[i]) == null) newLines.add(new GData2(24, View.line_Colour_r[0], View.line_Colour_g[0], View.line_Colour_b[0], 1f, originalVerts[(i + 1) % c], ve[i], View.DUMMY_REFERENCE, linkedDatFile));
                    linesToDelete2.add(g2);
                }
            }

            // Build quads
            for(int i = 0; i < c; i++) {
                newQuads.add(new GData4(colourNumber, r, g, b, a, newPoints.get(originalVerts[i]), ve[i], vc, ve[(c + i - 1) % c], View.DUMMY_REFERENCE, linkedDatFile));
            }

        }

        for (GData g : newLines) {
            linkedDatFile.addToTail(g);
        }
        for (GData g : newQuads) {
            linkedDatFile.addToTail(g);
        }

        selectedLines.addAll(linesToDelete2);
        selectedTriangles.addAll(trisToDelete2);
        selectedQuads.addAll(quadsToDelete2);
        selectedData.addAll(linesToDelete2);
        selectedData.addAll(trisToDelete2);
        selectedData.addAll(quadsToDelete2);
        delete(false, false);

        selectedLines.addAll(newLines);
        selectedData.addAll(newLines);
        selectedQuads.addAll(newQuads);
        selectedData.addAll(newQuads);
        roundSelection(6, 10, true, false);
        setModified(true, true);
        validateState();

    }

    public void subdivideLoop() {

        if (linkedDatFile.isReadOnly()) return;

        // Backup selected surfaces
        HashSet<GData3> surfsToParse = new HashSet<GData3>();

        final Set<GData2> linesToDelete2 = new HashSet<GData2>();
        final Set<GData3> trisToDelete2 = new HashSet<GData3>();

        final Set<GData2> newLines = new HashSet<GData2>();
        final Set<GData3> newTris = new HashSet<GData3>();

        for (GData3 g3 : selectedTriangles) {
            if (!lineLinkedToVertices.containsKey(g3)) continue;
            surfsToParse.add(g3);
            trisToDelete2.add(g3);
        }

        splitQuads(false);

        for (GData3 g3 : selectedTriangles) {
            if (!lineLinkedToVertices.containsKey(g3)) continue;
            surfsToParse.add(g3);
            trisToDelete2.add(g3);
        }

        if (surfsToParse.isEmpty()) return;

        // Delete all condlines, since the are a PITA for subdivision

        clearSelection();
        selectedCondlines.addAll(condlines.keySet());
        selectedData.addAll(selectedCondlines);
        delete(false, false);

        TreeMap<Vertex, Vertex> newPoints = new TreeMap<Vertex, Vertex>();

        for (GData3 g3 : surfsToParse) {
            Vertex[] originalVerts;
            int colourNumber;
            float r;
            float g;
            float b;
            float a;
            originalVerts = triangles.get(g3);
            colourNumber = g3.colourNumber; r = g3.r; g = g3.g; b = g3.b; a = g3.a;

            final int c = originalVerts.length;

            Vertex[] ve = new Vertex[c];
            for(int i = 0; i < c; i++) {
                ve[i] = new Vertex(Vector3d.add(new Vector3d(originalVerts[i]), new Vector3d(originalVerts[(i + 1) % c])).scaledByHalf());
                GData2 g2 = null;
                if ((g2 = hasEdge(originalVerts[i], originalVerts[(i + 1) % c])) != null) {
                    if (!vertexLinkedToPositionInFile.containsKey(ve[i]) || hasEdge(originalVerts[i], ve[i]) == null) newLines.add(new GData2(24, View.line_Colour_r[0], View.line_Colour_g[0], View.line_Colour_b[0], 1f, originalVerts[i], ve[i], View.DUMMY_REFERENCE, linkedDatFile));
                    if (!vertexLinkedToPositionInFile.containsKey(ve[i]) || hasEdge(originalVerts[(i + 1) % c], ve[i]) == null) newLines.add(new GData2(24, View.line_Colour_r[0], View.line_Colour_g[0], View.line_Colour_b[0], 1f, originalVerts[(i + 1) % c], ve[i], View.DUMMY_REFERENCE, linkedDatFile));
                    linesToDelete2.add(g2);
                }
            }

            // Build triangles
            newTris.add(new GData3(colourNumber, r, g, b, a, ve[0], ve[1], ve[2], View.DUMMY_REFERENCE, linkedDatFile));
            newTris.add(new GData3(colourNumber, r, g, b, a, originalVerts[0], ve[0], ve[2], View.DUMMY_REFERENCE, linkedDatFile));
            newTris.add(new GData3(colourNumber, r, g, b, a, originalVerts[1], ve[1], ve[0], View.DUMMY_REFERENCE, linkedDatFile));
            newTris.add(new GData3(colourNumber, r, g, b, a, originalVerts[2], ve[2], ve[1], View.DUMMY_REFERENCE, linkedDatFile));
        }

        for (GData g : newLines) {
            linkedDatFile.addToTail(g);
        }
        for (GData g : newTris) {
            linkedDatFile.addToTail(g);
        }

        selectedLines.addAll(linesToDelete2);
        selectedTriangles.addAll(trisToDelete2);
        selectedData.addAll(linesToDelete2);
        selectedData.addAll(trisToDelete2);
        delete(false, false);

        selectedLines.addAll(newLines);
        selectedData.addAll(newLines);
        selectedTriangles.addAll(newTris);
        selectedData.addAll(newTris);
        roundSelection(6, 10, true, false);

        TreeSet<Vertex> verticesToMove = new TreeSet<Vertex>();

        for (GData3 tri : selectedTriangles) {
            Vertex[] verts = triangles.get(tri);
            for (Vertex v : verts) {
                verticesToMove.add(v);
            }
        }

        int newContentSize = newLines.size() + newTris.size();

        clearSelection();

        // Calculate new points, based on Loop's Algorithm
        for (Vertex v : vertexLinkedToPositionInFile.keySet()) {
            Set<VertexManifestation> manis = vertexLinkedToPositionInFile.get(v);
            HashSet<Vector3d> midEdge = new HashSet<Vector3d>();
            boolean keepIt = false;
            for (VertexManifestation m : manis) {
                GData gd = m.getGdata();
                switch (gd.type()) {
                case 0:
                    continue;
                case 2:
                    keepIt = true;
                    break;
                case 3:
                {
                    int p = m.getPosition();
                    Vertex[] verts = triangles.get(gd);
                    midEdge.add(new Vector3d(verts[(p + 1) % 3]));
                    midEdge.add(new Vector3d(verts[(p + 2) % 3]));
                }
                break;
                case 4:
                {
                    int p = m.getPosition();
                    Vertex[] verts = quads.get(gd);
                    midEdge.add(new Vector3d(verts[(p + 1) % 4]));
                    midEdge.add(new Vector3d(verts[(p + 3) % 4]));
                }
                break;
                default:
                    continue;
                }
                if (keepIt) break;
            }
            if (keepIt) {
                newPoints.put(v, v);
            } else {
                double n = midEdge.size() + 1;
                double t = 3.0 / 8.0 + 1.0 / 4.0 * Math.cos(Math.PI * 2.0 / n);
                double alphaN = 3.0 / 8.0 + t * t;
                BigDecimal oneMinusAlphaDivN = new BigDecimal((1.0 - alphaN) / n);
                BigDecimal alphaN2 = new BigDecimal(alphaN);
                Vector3d np = new Vector3d();
                for (Vector3d vd : midEdge) {
                    np = Vector3d.add(np, vd);
                }
                np.setX(v.X.multiply(alphaN2).add(np.X.multiply(oneMinusAlphaDivN, Threshold.mc)));
                np.setY(v.Y.multiply(alphaN2).add(np.Y.multiply(oneMinusAlphaDivN, Threshold.mc)));
                np.setZ(v.Z.multiply(alphaN2).add(np.Z.multiply(oneMinusAlphaDivN, Threshold.mc)));
                newPoints.put(v, new Vertex(np));
            }
        }

        for (Vertex v : verticesToMove) {
            changeVertexDirectFast(v, newPoints.get(v), true);
        }

        clearSelection();

        GData gd = linkedDatFile.getDrawChainTail();
        while (newContentSize > 0) {
            switch (gd.type()) {
            case 2:
                selectedLines.add((GData2) gd);
                selectedData.add(gd);
                break;
            case 3:
                selectedTriangles.add((GData3) gd);
                selectedData.add(gd);
                break;
            default:
                break;
            }
            gd = gd.getBefore();
            newContentSize--;
        }
        roundSelection(6, 10, true, false);

        setModified(true, true);
        validateState();
    }

    public void split(int fractions) {

        if (linkedDatFile.isReadOnly()) return;

        final Set<GData2> newLines = new HashSet<GData2>();
        final Set<GData3> newTriangles = new HashSet<GData3>();
        final Set<GData5> newCondlines = new HashSet<GData5>();

        final Set<GData2> linesToDelete2 = new HashSet<GData2>();
        final Set<GData3> trisToDelete2 = new HashSet<GData3>();
        final Set<GData4> quadsToDelete2 = new HashSet<GData4>();
        final Set<GData5> clinesToDelete2 = new HashSet<GData5>();

        final Set<AccurateEdge> edgesToSplit = new HashSet<AccurateEdge>();

        {
            int i = 0;
            int j = 0;

            for (Vertex v1 : selectedVertices) {
                for (Vertex v2 : selectedVertices) {
                    if (j > i) {
                        if (isNeighbour(v1, v2)) {
                            edgesToSplit.add(new AccurateEdge(v1, v2));
                        }
                    }
                    j++;
                }
                i++;
            }
        }

        {
            for (GData2 g2 : selectedLines) {
                if (!lineLinkedToVertices.containsKey(g2)) continue;
                Vertex[] verts = lines.get(g2);
                edgesToSplit.add(new AccurateEdge(verts[0], verts[1]));
            }
            for (GData3 g3 : selectedTriangles) {
                if (!lineLinkedToVertices.containsKey(g3)) continue;
                Vertex[] verts = triangles.get(g3);
                edgesToSplit.add(new AccurateEdge(verts[0], verts[1]));
                edgesToSplit.add(new AccurateEdge(verts[1], verts[2]));
                edgesToSplit.add(new AccurateEdge(verts[2], verts[0]));
            }
            for (GData4 g4 : selectedQuads) {
                if (!lineLinkedToVertices.containsKey(g4)) continue;
                Vertex[] verts = quads.get(g4);
                edgesToSplit.add(new AccurateEdge(verts[0], verts[1]));
                edgesToSplit.add(new AccurateEdge(verts[1], verts[2]));
                edgesToSplit.add(new AccurateEdge(verts[2], verts[3]));
                edgesToSplit.add(new AccurateEdge(verts[3], verts[0]));
            }
            for (GData5 g5 : selectedCondlines) {
                if (!lineLinkedToVertices.containsKey(g5)) continue;
                Vertex[] verts = condlines.get(g5);
                edgesToSplit.add(new AccurateEdge(verts[0], verts[1]));
            }
        }

        clearSelection();

        for (GData2 g : new HashSet<GData2>(lines.keySet())) {
            if (!lineLinkedToVertices.containsKey(g)) continue;
            List<GData2> result = split(g, fractions, edgesToSplit);
            if (result.isEmpty()) continue;
            newLines.addAll(result);
            for (GData n : result) {
                linkedDatFile.insertAfter(g, n);
            }
            linesToDelete2.add(g);
        }

        for (GData3 g : new HashSet<GData3>(triangles.keySet())) {
            if (!lineLinkedToVertices.containsKey(g)) continue;
            List<GData3> result = split(g, fractions, edgesToSplit);
            if (result.isEmpty()) continue;
            newTriangles.addAll(result);
            for (GData n : result) {
                linkedDatFile.insertAfter(g, n);
            }
            trisToDelete2.add(g);
        }

        for (GData4 g : new HashSet<GData4>(quads.keySet())) {
            if (!lineLinkedToVertices.containsKey(g)) continue;
            List<GData3> result = split(g, fractions, edgesToSplit);
            if (result.isEmpty()) continue;
            newTriangles.addAll(result);
            for (GData n : result) {
                linkedDatFile.insertAfter(g, n);
            }
            quadsToDelete2.add(g);
        }

        for (GData5 g : new HashSet<GData5>(condlines.keySet())) {
            List<GData5> result = split(g, fractions, edgesToSplit);
            if (result.isEmpty()) continue;
            newCondlines.addAll(result);
            for (GData n : result) {
                linkedDatFile.insertAfter(g, n);
            }
            clinesToDelete2.add(g);
        }

        if (newLines.size() + newTriangles.size() + newCondlines.size() > 0) {
            setModified_NoSync();
        }

        selectedLines.addAll(linesToDelete2);
        selectedTriangles.addAll(trisToDelete2);
        selectedQuads.addAll(quadsToDelete2);
        selectedCondlines.addAll(clinesToDelete2);
        selectedData.addAll(selectedLines);
        selectedData.addAll(selectedTriangles);
        selectedData.addAll(selectedQuads);
        selectedData.addAll(selectedCondlines);
        delete(false, false);

        selectedTriangles.addAll(newTriangles);
        selectedData.addAll(selectedTriangles);
        rectify(new RectifierSettings(), false, false);

        clearSelection();
        if (isModified()) {
            setModified(true, true);
        }
        validateState();
    }

    private List<GData5> split(GData5 g, int fractions, Set<AccurateEdge> edgesToSplit) {

        ArrayList<GData5> result = new ArrayList<GData5>(fractions);

        // Detect how many edges are affected
        Vertex[] verts = condlines.get(g);
        int ec = edgesToSplit.contains(new AccurateEdge(verts[0], verts[1])) ? 1 :0;

        switch (ec) {
        case 0:
            return result;
        case 1:

            Vector3d A = new Vector3d(lines.get(g)[0]);
            Vector3d B = new Vector3d(lines.get(g)[1]);

            BigDecimal step = BigDecimal.ONE.divide(new BigDecimal(fractions), Threshold.mc);
            BigDecimal cur = BigDecimal.ZERO;
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusCur = BigDecimal.ONE.subtract(cur);
                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

                result.add(new GData5(g.colourNumber, g.r, g.g, g.b, g.a,

                        A.X.multiply(oneMinusCur).add(B.X.multiply(cur)),
                        A.Y.multiply(oneMinusCur).add(B.Y.multiply(cur)),
                        A.Z.multiply(oneMinusCur).add(B.Z.multiply(cur)),

                        A.X.multiply(oneMinusNext).add(B.X.multiply(next)),
                        A.Y.multiply(oneMinusNext).add(B.Y.multiply(next)),
                        A.Z.multiply(oneMinusNext).add(B.Z.multiply(next)),

                        g.X3, g.Y3, g.Z3,
                        g.X4, g.Y4, g.Z4,

                        View.DUMMY_REFERENCE, linkedDatFile));
                cur = next;
            }
        }
        return result;
    }

    private List<GData3> split(GData4 g, int fractions, Set<AccurateEdge> edgesToSplit) {

        // Detect how many edges are affected
        Vertex[] verts = quads.get(g);
        int ec = edgesToSplit.contains(new AccurateEdge(verts[0], verts[1])) ? 1 :0;
        ec += edgesToSplit.contains(new AccurateEdge(verts[1], verts[2])) ? 1 :0;
        ec += edgesToSplit.contains(new AccurateEdge(verts[2], verts[3])) ? 1 :0;
        ec += edgesToSplit.contains(new AccurateEdge(verts[3], verts[0])) ? 1 :0;

        switch (ec) {
        case 0:
            return new ArrayList<GData3>();
        case 1:
            if (edgesToSplit.contains(new AccurateEdge(verts[0], verts[1]))) {
                return splitQuad1(verts[0], verts[1], verts[2], verts[3], fractions, g);
            } else if (edgesToSplit.contains(new AccurateEdge(verts[1], verts[2]))) {
                return splitQuad1(verts[1], verts[2], verts[3], verts[0], fractions, g);
            } else if (edgesToSplit.contains(new AccurateEdge(verts[2], verts[3]))) {
                return splitQuad1(verts[2], verts[3], verts[0], verts[1], fractions, g);
            } else {
                return splitQuad1(verts[3], verts[0], verts[1], verts[2], fractions, g);
            }
        case 2:

            if (edgesToSplit.contains(new AccurateEdge(verts[0], verts[1]))) {

                if (edgesToSplit.contains(new AccurateEdge(verts[1], verts[2]))) {

                    return splitQuad21(verts[0], verts[1], verts[2], verts[3], fractions, g);

                } else if (edgesToSplit.contains(new AccurateEdge(verts[2], verts[3]))) {

                    return splitQuad22(verts[0], verts[1], verts[2], verts[3], fractions, g);

                } else if (edgesToSplit.contains(new AccurateEdge(verts[3], verts[0]))) {

                    return splitQuad21(verts[3], verts[0], verts[1], verts[2], fractions, g);

                }
            } else if (edgesToSplit.contains(new AccurateEdge(verts[1], verts[2]))) {

                if (edgesToSplit.contains(new AccurateEdge(verts[2], verts[3]))) {

                    return splitQuad21(verts[1], verts[2], verts[3], verts[0], fractions, g);

                } else if (edgesToSplit.contains(new AccurateEdge(verts[3], verts[0]))) {

                    return splitQuad22(verts[1], verts[2], verts[3], verts[0], fractions, g);

                }
            } else if (edgesToSplit.contains(new AccurateEdge(verts[2], verts[3]))) {

                return splitQuad21(verts[2], verts[3], verts[0], verts[1], fractions, g);

            }
        case 3:
            if (!edgesToSplit.contains(new AccurateEdge(verts[0], verts[1]))) {
                return splitQuad3(verts[0], verts[1], verts[2], verts[3], fractions, g);
            } else if (!edgesToSplit.contains(new AccurateEdge(verts[1], verts[2]))) {
                return splitQuad3(verts[1], verts[2], verts[3], verts[0], fractions, g);
            } else if (!edgesToSplit.contains(new AccurateEdge(verts[2], verts[3]))) {
                return splitQuad3(verts[2], verts[3], verts[0], verts[1], fractions, g);
            } else {
                return splitQuad3(verts[3], verts[0], verts[1], verts[2], fractions, g);
            }
        case 4:
            return splitQuad4(verts[0], verts[1], verts[2], verts[3], fractions, g);
        default:
            break;
        }

        return new ArrayList<GData3>();
    }

    private List<GData3> splitQuad1(Vertex v1, Vertex v2, Vertex v3, Vertex v4, int fractions, GData4 g) {
        ArrayList<GData3> result = new ArrayList<GData3>(fractions);

        Vector3d A = new Vector3d(v1);
        Vector3d B = new Vector3d(v2);


        int fracA = fractions / 2;

        BigDecimal step = BigDecimal.ONE.divide(new BigDecimal(fractions), Threshold.mc);
        BigDecimal cur = BigDecimal.ZERO;
        BigDecimal next = BigDecimal.ZERO;

        BigDecimal middle = BigDecimal.ZERO;

        for (int i = 0; i < fracA; i++) {
            next = next.add(step);

            BigDecimal oneMinusCur = BigDecimal.ONE.subtract(cur);
            BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

            result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                    A.X.multiply(oneMinusCur).add(B.X.multiply(cur)),
                    A.Y.multiply(oneMinusCur).add(B.Y.multiply(cur)),
                    A.Z.multiply(oneMinusCur).add(B.Z.multiply(cur)),

                    A.X.multiply(oneMinusNext).add(B.X.multiply(next)),
                    A.Y.multiply(oneMinusNext).add(B.Y.multiply(next)),
                    A.Z.multiply(oneMinusNext).add(B.Z.multiply(next)),

                    v4.X,
                    v4.Y,
                    v4.Z,

                    View.DUMMY_REFERENCE, linkedDatFile));
            cur = next;
            middle = next;
        }


        for (int i = fracA; i < fractions; i++) {
            if (i == fractions - 1) {
                next = BigDecimal.ONE;
            } else {
                next = next.add(step);
            }

            BigDecimal oneMinusCur = BigDecimal.ONE.subtract(cur);
            BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

            result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                    A.X.multiply(oneMinusCur).add(B.X.multiply(cur)),
                    A.Y.multiply(oneMinusCur).add(B.Y.multiply(cur)),
                    A.Z.multiply(oneMinusCur).add(B.Z.multiply(cur)),

                    A.X.multiply(oneMinusNext).add(B.X.multiply(next)),
                    A.Y.multiply(oneMinusNext).add(B.Y.multiply(next)),
                    A.Z.multiply(oneMinusNext).add(B.Z.multiply(next)),

                    v3.X,
                    v3.Y,
                    v3.Z,

                    View.DUMMY_REFERENCE, linkedDatFile));
            cur = next;
        }

        {

            cur = middle;

            BigDecimal oneMinusCur = BigDecimal.ONE.subtract(cur);

            result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                    A.X.multiply(oneMinusCur).add(B.X.multiply(cur)),
                    A.Y.multiply(oneMinusCur).add(B.Y.multiply(cur)),
                    A.Z.multiply(oneMinusCur).add(B.Z.multiply(cur)),

                    v3.X,
                    v3.Y,
                    v3.Z,

                    v4.X,
                    v4.Y,
                    v4.Z,

                    View.DUMMY_REFERENCE, linkedDatFile));
        }

        return result;
    }

    private List<GData3> splitQuad21(Vertex v1, Vertex v2, Vertex v3, Vertex v4, int fractions, GData4 g) {

        ArrayList<GData3> result = new ArrayList<GData3>(fractions * 8);

        // Split between v1-v2 & v2-v3
        Vector3d A = new Vector3d(v1);
        Vector3d B = new Vector3d(v2);
        Vector3d C = new Vector3d(v3);
        Vector3d D = new Vector3d(v4);

        BigDecimal step = BigDecimal.ONE.divide(new BigDecimal(fractions), Threshold.mc);

        ArrayList<Vector3d> newPoints = new ArrayList<Vector3d>(fractions * 4);

        {
            BigDecimal cur = BigDecimal.ZERO;
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusCur = BigDecimal.ONE.subtract(cur);
                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

                newPoints.add(new Vector3d(
                        A.X.multiply(oneMinusCur).add(B.X.multiply(cur)),
                        A.Y.multiply(oneMinusCur).add(B.Y.multiply(cur)),
                        A.Z.multiply(oneMinusCur).add(B.Z.multiply(cur))
                        ));
                newPoints.add(new Vector3d(
                        A.X.multiply(oneMinusNext).add(B.X.multiply(next)),
                        A.Y.multiply(oneMinusNext).add(B.Y.multiply(next)),
                        A.Z.multiply(oneMinusNext).add(B.Z.multiply(next))
                        ));

                newPoints.add(D);

                cur = next;
            }
        }
        {
            BigDecimal cur = BigDecimal.ZERO;
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusCur = BigDecimal.ONE.subtract(cur);
                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

                newPoints.add(new Vector3d(
                        B.X.multiply(oneMinusCur).add(C.X.multiply(cur)),
                        B.Y.multiply(oneMinusCur).add(C.Y.multiply(cur)),
                        B.Z.multiply(oneMinusCur).add(C.Z.multiply(cur))
                        ));
                newPoints.add(new Vector3d(
                        B.X.multiply(oneMinusNext).add(C.X.multiply(next)),
                        B.Y.multiply(oneMinusNext).add(C.Y.multiply(next)),
                        B.Z.multiply(oneMinusNext).add(C.Z.multiply(next))
                        ));

                newPoints.add(D);

                cur = next;
            }
        }

        final int pz = newPoints.size();
        for (int i = 0; i < pz; i += 3) {
            Vector3d p1 = newPoints.get(i);
            Vector3d p2 = newPoints.get(i + 1);
            Vector3d p3 = newPoints.get(i + 2);

            result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                    p3.X,
                    p3.Y,
                    p3.Z,

                    p1.X,
                    p1.Y,
                    p1.Z,

                    p2.X,
                    p2.Y,
                    p2.Z,

                    View.DUMMY_REFERENCE, linkedDatFile));
        }

        return result;
    }

    private List<GData3> splitQuad22(Vertex v1, Vertex v2, Vertex v3, Vertex v4, int fractions, GData4 g) {

        ArrayList<GData3> result = new ArrayList<GData3>(fractions * 8);

        // Split between v1-v2 & v3-v4
        Vector3d A = new Vector3d(v1);
        Vector3d B = new Vector3d(v2);
        Vector3d C = new Vector3d(v3);
        Vector3d D = new Vector3d(v4);

        BigDecimal step = BigDecimal.ONE.divide(new BigDecimal(fractions), Threshold.mc);

        ArrayList<Vector3d> newPoints = new ArrayList<Vector3d>(fractions * 4);

        {
            BigDecimal cur = BigDecimal.ZERO;
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusCur = BigDecimal.ONE.subtract(cur);
                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

                newPoints.add(new Vector3d(
                        A.X.multiply(oneMinusCur).add(B.X.multiply(cur)),
                        A.Y.multiply(oneMinusCur).add(B.Y.multiply(cur)),
                        A.Z.multiply(oneMinusCur).add(B.Z.multiply(cur))
                        ));
                newPoints.add(new Vector3d(
                        D.X.multiply(oneMinusCur).add(C.X.multiply(cur)),
                        D.Y.multiply(oneMinusCur).add(C.Y.multiply(cur)),
                        D.Z.multiply(oneMinusCur).add(C.Z.multiply(cur))
                        ));
                newPoints.add(new Vector3d(
                        A.X.multiply(oneMinusNext).add(B.X.multiply(next)),
                        A.Y.multiply(oneMinusNext).add(B.Y.multiply(next)),
                        A.Z.multiply(oneMinusNext).add(B.Z.multiply(next))
                        ));
                newPoints.add(new Vector3d(
                        D.X.multiply(oneMinusNext).add(C.X.multiply(next)),
                        D.Y.multiply(oneMinusNext).add(C.Y.multiply(next)),
                        D.Z.multiply(oneMinusNext).add(C.Z.multiply(next))
                        ));

                cur = next;
            }
        }

        final int pz = newPoints.size();
        for (int i = 0; i < pz; i += 4) {
            Vector3d p1 = newPoints.get(i);
            Vector3d p2 = newPoints.get(i + 1);
            Vector3d p3 = newPoints.get(i + 2);
            Vector3d p4 = newPoints.get(i + 3);

            result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                    p2.X,
                    p2.Y,
                    p2.Z,

                    p1.X,
                    p1.Y,
                    p1.Z,

                    p3.X,
                    p3.Y,
                    p3.Z,

                    View.DUMMY_REFERENCE, linkedDatFile));

            result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                    p3.X,
                    p3.Y,
                    p3.Z,

                    p4.X,
                    p4.Y,
                    p4.Z,

                    p2.X,
                    p2.Y,
                    p2.Z,

                    View.DUMMY_REFERENCE, linkedDatFile));

        }

        return result;
    }

    private List<GData3> splitQuad3(Vertex v1, Vertex v2, Vertex v3, Vertex v4, int fractions, GData4 g) {
        ArrayList<GData3> result = new ArrayList<GData3>(fractions * 8);

        Vector3d A = new Vector3d(v1);
        Vector3d B = new Vector3d(v2);
        Vector3d C = new Vector3d(v3);
        Vector3d D = new Vector3d(v4);

        Vector3d vc = Vector3d.add(Vector3d.add(Vector3d.add(A, B), C), D);
        vc.setX(vc.X.divide(new BigDecimal(4), Threshold.mc));
        vc.setY(vc.Y.divide(new BigDecimal(4), Threshold.mc));
        vc.setZ(vc.Z.divide(new BigDecimal(4), Threshold.mc));

        BigDecimal step = BigDecimal.ONE.divide(new BigDecimal(fractions), Threshold.mc);

        ArrayList<Vector3d> newPoints = new ArrayList<Vector3d>(fractions * 3);

        {
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {

                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);


                newPoints.add(new Vector3d(
                        B.X.multiply(oneMinusNext).add(C.X.multiply(next)),
                        B.Y.multiply(oneMinusNext).add(C.Y.multiply(next)),
                        B.Z.multiply(oneMinusNext).add(C.Z.multiply(next))
                        ));

                next = next.add(step);
            }
        }
        {
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {

                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

                newPoints.add(new Vector3d(
                        C.X.multiply(oneMinusNext).add(D.X.multiply(next)),
                        C.Y.multiply(oneMinusNext).add(D.Y.multiply(next)),
                        C.Z.multiply(oneMinusNext).add(D.Z.multiply(next))
                        ));

                next = next.add(step);
            }
        }
        {
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions + 1; i++) {

                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

                newPoints.add(new Vector3d(
                        D.X.multiply(oneMinusNext).add(A.X.multiply(next)),
                        D.Y.multiply(oneMinusNext).add(A.Y.multiply(next)),
                        D.Z.multiply(oneMinusNext).add(A.Z.multiply(next))
                        ));

                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }
            }
        }

        fractions = fractions * 3;
        for (int i = 0; i < fractions + 1; i++) {

            result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                    vc.X,
                    vc.Y,
                    vc.Z,

                    newPoints.get(i).X,
                    newPoints.get(i).Y,
                    newPoints.get(i).Z,

                    newPoints.get((i + 1) % (fractions + 1)).X,
                    newPoints.get((i + 1) % (fractions + 1)).Y,
                    newPoints.get((i + 1) % (fractions + 1)).Z,

                    View.DUMMY_REFERENCE, linkedDatFile));


        }

        return result;
    }

    private List<GData3> splitQuad4(Vertex v1, Vertex v2, Vertex v3, Vertex v4, int fractions, GData4 g) {

        ArrayList<GData3> result = new ArrayList<GData3>(fractions * 8);

        Vector3d A = new Vector3d(v1);
        Vector3d B = new Vector3d(v2);
        Vector3d C = new Vector3d(v3);
        Vector3d D = new Vector3d(v4);

        Vector3d vc = Vector3d.add(Vector3d.add(Vector3d.add(A, B), C), D);
        vc.setX(vc.X.divide(new BigDecimal(4), Threshold.mc));
        vc.setY(vc.Y.divide(new BigDecimal(4), Threshold.mc));
        vc.setZ(vc.Z.divide(new BigDecimal(4), Threshold.mc));

        BigDecimal step = BigDecimal.ONE.divide(new BigDecimal(fractions), Threshold.mc);

        ArrayList<Vector3d> newPoints = new ArrayList<Vector3d>(fractions * 4);

        {
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);


                newPoints.add(new Vector3d(
                        A.X.multiply(oneMinusNext).add(B.X.multiply(next)),
                        A.Y.multiply(oneMinusNext).add(B.Y.multiply(next)),
                        A.Z.multiply(oneMinusNext).add(B.Z.multiply(next))
                        ));
            }
        }
        {
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);


                newPoints.add(new Vector3d(
                        B.X.multiply(oneMinusNext).add(C.X.multiply(next)),
                        B.Y.multiply(oneMinusNext).add(C.Y.multiply(next)),
                        B.Z.multiply(oneMinusNext).add(C.Z.multiply(next))
                        ));
            }
        }
        {
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

                newPoints.add(new Vector3d(
                        C.X.multiply(oneMinusNext).add(D.X.multiply(next)),
                        C.Y.multiply(oneMinusNext).add(D.Y.multiply(next)),
                        C.Z.multiply(oneMinusNext).add(D.Z.multiply(next))
                        ));
            }
        }
        {
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

                newPoints.add(new Vector3d(
                        D.X.multiply(oneMinusNext).add(A.X.multiply(next)),
                        D.Y.multiply(oneMinusNext).add(A.Y.multiply(next)),
                        D.Z.multiply(oneMinusNext).add(A.Z.multiply(next))
                        ));
            }
        }

        fractions = fractions * 4;
        for (int i = 0; i < fractions; i++) {

            result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                    vc.X,
                    vc.Y,
                    vc.Z,

                    newPoints.get(i).X,
                    newPoints.get(i).Y,
                    newPoints.get(i).Z,

                    newPoints.get((i + 1) % fractions).X,
                    newPoints.get((i + 1) % fractions).Y,
                    newPoints.get((i + 1) % fractions).Z,

                    View.DUMMY_REFERENCE, linkedDatFile));


        }

        return result;
    }

    private List<GData3> split(GData3 g, int fractions, Set<AccurateEdge> edgesToSplit) {

        // Detect how many edges are affected
        Vertex[] verts = triangles.get(g);
        int ec = edgesToSplit.contains(new AccurateEdge(verts[0], verts[1])) ? 1 :0;
        ec += edgesToSplit.contains(new AccurateEdge(verts[1], verts[2])) ? 1 :0;
        ec += edgesToSplit.contains(new AccurateEdge(verts[2], verts[0])) ? 1 :0;

        switch (ec) {
        case 0:
            return new ArrayList<GData3>();
        case 1:
            if (edgesToSplit.contains(new AccurateEdge(verts[0], verts[1]))) {
                return splitTri1(verts[0], verts[1], verts[2], fractions, g);
            } else if (edgesToSplit.contains(new AccurateEdge(verts[1], verts[2]))) {
                return splitTri1(verts[1], verts[2], verts[0], fractions, g);
            } else {
                return splitTri1(verts[2], verts[0], verts[1], fractions, g);
            }
        case 2:
            if (edgesToSplit.contains(new AccurateEdge(verts[0], verts[1]))) {
                if (edgesToSplit.contains(new AccurateEdge(verts[1], verts[2]))) {
                    return splitTri2(verts[1], verts[2], verts[0], fractions, g);
                } else {
                    return splitTri2(verts[0], verts[1], verts[2], fractions, g);
                }
            } else if (edgesToSplit.contains(new AccurateEdge(verts[1], verts[2]))) {
                if (edgesToSplit.contains(new AccurateEdge(verts[0], verts[1]))) {
                    return splitTri2(verts[1], verts[2], verts[0], fractions, g);
                } else {
                    return splitTri2(verts[2], verts[0], verts[1], fractions, g);
                }
            } else if (edgesToSplit.contains(new AccurateEdge(verts[2], verts[0]))) {
                if (edgesToSplit.contains(new AccurateEdge(verts[0], verts[1]))) {
                    return splitTri2(verts[0], verts[1], verts[2], fractions, g);
                } else {
                    return splitTri2(verts[2], verts[0], verts[1], fractions, g);
                }
            }
        case 3:
            return splitTri3(verts[0], verts[1], verts[2], fractions, g);
        default:
            break;
        }

        return new ArrayList<GData3>();
    }

    private List<GData3> splitTri1(Vertex v1, Vertex v2, Vertex v3, int fractions, GData3 g) {
        ArrayList<GData3> result = new ArrayList<GData3>(fractions);

        Vector3d A = new Vector3d(v1);
        Vector3d B = new Vector3d(v2);

        BigDecimal step = BigDecimal.ONE.divide(new BigDecimal(fractions), Threshold.mc);
        BigDecimal cur = BigDecimal.ZERO;
        BigDecimal next = BigDecimal.ZERO;
        for (int i = 0; i < fractions; i++) {
            if (i == fractions - 1) {
                next = BigDecimal.ONE;
            } else {
                next = next.add(step);
            }

            BigDecimal oneMinusCur = BigDecimal.ONE.subtract(cur);
            BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

            result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                    A.X.multiply(oneMinusCur).add(B.X.multiply(cur)),
                    A.Y.multiply(oneMinusCur).add(B.Y.multiply(cur)),
                    A.Z.multiply(oneMinusCur).add(B.Z.multiply(cur)),

                    A.X.multiply(oneMinusNext).add(B.X.multiply(next)),
                    A.Y.multiply(oneMinusNext).add(B.Y.multiply(next)),
                    A.Z.multiply(oneMinusNext).add(B.Z.multiply(next)),

                    v3.X,
                    v3.Y,
                    v3.Z,

                    View.DUMMY_REFERENCE, linkedDatFile));
            cur = next;
        }

        return result;
    }

    private List<GData3> splitTri2(Vertex v1, Vertex v2, Vertex v3, int fractions, GData3 g) {

        ArrayList<GData3> result = new ArrayList<GData3>(fractions);

        Vector3d A = new Vector3d(v1);
        Vector3d B = new Vector3d(v2);
        Vector3d C = new Vector3d(v3);

        BigDecimal step = BigDecimal.ONE.divide(new BigDecimal(fractions), Threshold.mc);
        BigDecimal cur = BigDecimal.ZERO;
        BigDecimal next = BigDecimal.ZERO;
        for (int i = 0; i < fractions; i++) {
            if (i == fractions - 1) {
                next = BigDecimal.ONE;
            } else {
                next = next.add(step);
            }

            BigDecimal oneMinusCur = BigDecimal.ONE.subtract(cur);
            BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

            if (i == 0) {
                result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                        v1.X,
                        v1.Y,
                        v1.Z,

                        A.X.multiply(oneMinusNext).add(B.X.multiply(next)),
                        A.Y.multiply(oneMinusNext).add(B.Y.multiply(next)),
                        A.Z.multiply(oneMinusNext).add(B.Z.multiply(next)),

                        A.X.multiply(oneMinusNext).add(C.X.multiply(next)),
                        A.Y.multiply(oneMinusNext).add(C.Y.multiply(next)),
                        A.Z.multiply(oneMinusNext).add(C.Z.multiply(next)),

                        View.DUMMY_REFERENCE, linkedDatFile));
            } else {
                result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                        A.X.multiply(oneMinusNext).add(C.X.multiply(next)),
                        A.Y.multiply(oneMinusNext).add(C.Y.multiply(next)),
                        A.Z.multiply(oneMinusNext).add(C.Z.multiply(next)),

                        A.X.multiply(oneMinusCur).add(C.X.multiply(cur)),
                        A.Y.multiply(oneMinusCur).add(C.Y.multiply(cur)),
                        A.Z.multiply(oneMinusCur).add(C.Z.multiply(cur)),

                        A.X.multiply(oneMinusCur).add(B.X.multiply(cur)),
                        A.Y.multiply(oneMinusCur).add(B.Y.multiply(cur)),
                        A.Z.multiply(oneMinusCur).add(B.Z.multiply(cur)),

                        View.DUMMY_REFERENCE, linkedDatFile));
                result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                        A.X.multiply(oneMinusCur).add(B.X.multiply(cur)),
                        A.Y.multiply(oneMinusCur).add(B.Y.multiply(cur)),
                        A.Z.multiply(oneMinusCur).add(B.Z.multiply(cur)),

                        A.X.multiply(oneMinusNext).add(B.X.multiply(next)),
                        A.Y.multiply(oneMinusNext).add(B.Y.multiply(next)),
                        A.Z.multiply(oneMinusNext).add(B.Z.multiply(next)),

                        A.X.multiply(oneMinusNext).add(C.X.multiply(next)),
                        A.Y.multiply(oneMinusNext).add(C.Y.multiply(next)),
                        A.Z.multiply(oneMinusNext).add(C.Z.multiply(next)),

                        View.DUMMY_REFERENCE, linkedDatFile));
            }

            cur = next;
        }

        return result;
    }

    private List<GData3> splitTri3(Vertex v1, Vertex v2, Vertex v3, int fractions, GData3 g) {

        ArrayList<GData3> result = new ArrayList<GData3>(fractions * 3);

        Vector3d A = new Vector3d(v1);
        Vector3d B = new Vector3d(v2);
        Vector3d C = new Vector3d(v3);

        Vector3d vc = Vector3d.add(Vector3d.add(A, B), C);
        vc.setX(vc.X.divide(new BigDecimal(3), Threshold.mc));
        vc.setY(vc.Y.divide(new BigDecimal(3), Threshold.mc));
        vc.setZ(vc.Z.divide(new BigDecimal(3), Threshold.mc));

        BigDecimal step = BigDecimal.ONE.divide(new BigDecimal(fractions), Threshold.mc);

        ArrayList<Vector3d> newPoints = new ArrayList<Vector3d>(fractions * 3);

        {
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);


                newPoints.add(new Vector3d(
                        A.X.multiply(oneMinusNext).add(B.X.multiply(next)),
                        A.Y.multiply(oneMinusNext).add(B.Y.multiply(next)),
                        A.Z.multiply(oneMinusNext).add(B.Z.multiply(next))
                        ));
            }
        }
        {
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);


                newPoints.add(new Vector3d(
                        B.X.multiply(oneMinusNext).add(C.X.multiply(next)),
                        B.Y.multiply(oneMinusNext).add(C.Y.multiply(next)),
                        B.Z.multiply(oneMinusNext).add(C.Z.multiply(next))
                        ));
            }
        }
        {
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

                newPoints.add(new Vector3d(
                        C.X.multiply(oneMinusNext).add(A.X.multiply(next)),
                        C.Y.multiply(oneMinusNext).add(A.Y.multiply(next)),
                        C.Z.multiply(oneMinusNext).add(A.Z.multiply(next))
                        ));
            }
        }

        fractions = fractions * 3;
        for (int i = 0; i < fractions; i++) {

            result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a,

                    vc.X,
                    vc.Y,
                    vc.Z,

                    newPoints.get(i).X,
                    newPoints.get(i).Y,
                    newPoints.get(i).Z,

                    newPoints.get((i + 1) % fractions).X,
                    newPoints.get((i + 1) % fractions).Y,
                    newPoints.get((i + 1) % fractions).Z,

                    View.DUMMY_REFERENCE, linkedDatFile));


        }

        return result;
    }

    private List<GData2> split(GData2 g, int fractions, Set<AccurateEdge> edgesToSplit) {

        ArrayList<GData2> result = new ArrayList<GData2>(fractions);

        // Detect how many edges are affected
        Vertex[] verts = lines.get(g);
        int ec = edgesToSplit.contains(new AccurateEdge(verts[0], verts[1])) ? 1 :0;

        switch (ec) {
        case 0:
            return result;
        case 1:

            Vector3d A = new Vector3d(lines.get(g)[0]);
            Vector3d B = new Vector3d(lines.get(g)[1]);

            BigDecimal step = BigDecimal.ONE.divide(new BigDecimal(fractions), Threshold.mc);
            BigDecimal cur = BigDecimal.ZERO;
            BigDecimal next = BigDecimal.ZERO;
            for (int i = 0; i < fractions; i++) {
                if (i == fractions - 1) {
                    next = BigDecimal.ONE;
                } else {
                    next = next.add(step);
                }

                BigDecimal oneMinusCur = BigDecimal.ONE.subtract(cur);
                BigDecimal oneMinusNext = BigDecimal.ONE.subtract(next);

                // Cx = Ax * (1-t) + Bx * t
                // Cy = Ay * (1-t) + By * t

                result.add(new GData2(g.colourNumber, g.r, g.g, g.b, g.a,

                        A.X.multiply(oneMinusCur).add(B.X.multiply(cur)),
                        A.Y.multiply(oneMinusCur).add(B.Y.multiply(cur)),
                        A.Z.multiply(oneMinusCur).add(B.Z.multiply(cur)),

                        A.X.multiply(oneMinusNext).add(B.X.multiply(next)),
                        A.Y.multiply(oneMinusNext).add(B.Y.multiply(next)),
                        A.Z.multiply(oneMinusNext).add(B.Z.multiply(next)),

                        View.DUMMY_REFERENCE, linkedDatFile));
                cur = next;
            }
        }
        return result;
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

    public boolean split(Vertex start, Vertex end, Vertex target) {

        if (linkedDatFile.isReadOnly()) return false;

        final Set<GData2> newLines = new HashSet<GData2>();
        final Set<GData3> newTriangles = new HashSet<GData3>();
        final Set<GData5> newCondlines = new HashSet<GData5>();

        final Set<GData2> effSelectedLines = new HashSet<GData2>();
        final Set<GData3> effSelectedTriangles = new HashSet<GData3>();
        final Set<GData4> effSelectedQuads = new HashSet<GData4>();
        final Set<GData5> effSelectedCondlines = new HashSet<GData5>();


        final Set<GData2> linesToDelete2 = new HashSet<GData2>();
        final Set<GData3> trisToDelete2 = new HashSet<GData3>();
        final Set<GData4> quadsToDelete2 = new HashSet<GData4>();
        final Set<GData5> clinesToDelete2 = new HashSet<GData5>();

        Set<VertexManifestation> manis1 = vertexLinkedToPositionInFile.get(start);
        Set<VertexManifestation> manis2 = vertexLinkedToPositionInFile.get(end);

        if (manis1 == null || manis2 == null || manis1.isEmpty() || manis2.isEmpty()) return false;

        Set<GData> setA = new HashSet<GData>();
        Set<GData> setB = new HashSet<GData>();

        for (VertexManifestation m : manis1) {
            setA.add(m.getGdata());
        }
        for (VertexManifestation m : manis1) {
            setB.add(m.getGdata());
        }

        setA.retainAll(setB);

        for (GData g : setA) {
            if (!lineLinkedToVertices.containsKey(g)) continue;
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
                continue;
            }
        }

        for (GData2 g : effSelectedLines) {
            List<GData2> result = split(g, start, end, target);
            newLines.addAll(result);
            for (GData n : result) {
                linkedDatFile.insertAfter(g, n);
            }
            linesToDelete2.add(g);
        }

        for (GData3 g : effSelectedTriangles) {
            List<GData3> result = split(g, start, end, target);
            if (result.isEmpty()) continue;
            newTriangles.addAll(result);
            for (GData n : result) {
                linkedDatFile.insertAfter(g, n);
            }
            trisToDelete2.add(g);
        }

        for (GData4 g : effSelectedQuads) {
            List<GData3> result = split(g, start, end, target);
            if (result.isEmpty()) continue;
            newTriangles.addAll(result);
            for (GData n : result) {
                linkedDatFile.insertAfter(g, n);
            }
            quadsToDelete2.add(g);
        }

        for (GData5 g : effSelectedCondlines) {
            List<GData5> result = split(g, start, end, target);
            newCondlines.addAll(result);
            for (GData n : result) {
                linkedDatFile.insertAfter(g, n);
            }
            clinesToDelete2.add(g);
        }

        if (newLines.size() + newTriangles.size() + newCondlines.size() > 0) {
            setModified_NoSync();
        }

        backupSelection();
        clearSelection();
        selectedLines.addAll(linesToDelete2);
        selectedTriangles.addAll(trisToDelete2);
        selectedQuads.addAll(quadsToDelete2);
        selectedCondlines.addAll(clinesToDelete2);
        selectedData.addAll(selectedLines);
        selectedData.addAll(selectedTriangles);
        selectedData.addAll(selectedQuads);
        selectedData.addAll(selectedCondlines);
        delete(false, false);

        selectedTriangles.addAll(newTriangles);
        selectedData.addAll(selectedTriangles);
        rectify(new RectifierSettings(), false, false);

        clearSelection();
        restoreSelection();

        validateState();

        return isModified();
    }

    private List<GData5> split(GData5 g, Vertex start, Vertex end, Vertex target) {
        ArrayList<GData5> result = new ArrayList<GData5>();

        if (!start.equals(end)) {
            Vertex[] verts = condlines.get(g);
            if ((verts[0].equals(start) || verts[0].equals(end)) && (verts[1].equals(start) || verts[1].equals(end))) {
                result.add(new GData5(g.colourNumber, g.r, g.g, g.b, g.a,

                        start.X,
                        start.Y,
                        start.Z,

                        target.X,
                        target.Y,
                        target.Z,

                        g.X3,
                        g.Y3,
                        g.Z3,

                        g.X4,
                        g.Y4,
                        g.Z4,

                        View.DUMMY_REFERENCE, linkedDatFile));


                result.add(new GData5(g.colourNumber, g.r, g.g, g.b, g.a,

                        target.X,
                        target.Y,
                        target.Z,

                        end.X,
                        end.Y,
                        end.Z,

                        g.X3,
                        g.Y3,
                        g.Z3,

                        g.X4,
                        g.Y4,
                        g.Z4,

                        View.DUMMY_REFERENCE, linkedDatFile));
            }
        }
        return result;
    }

    private List<GData3> split(GData4 g, Vertex start, Vertex end, Vertex target) {
        ArrayList<GData3> result = new ArrayList<GData3>();
        if (!start.equals(end)) {
            Vertex[] verts = quads.get(g);
            if ((verts[0].equals(start) || verts[0].equals(end)) && (verts[1].equals(start) || verts[1].equals(end))) {
                return splitQuad(verts[0], verts[1], verts[2], verts[3], target, g);
            } else if ((verts[1].equals(start) || verts[1].equals(end)) && (verts[2].equals(start) || verts[2].equals(end))) {
                return splitQuad(verts[1], verts[2], verts[3], verts[0], target, g);
            } else if ((verts[2].equals(start) || verts[2].equals(end)) && (verts[3].equals(start) || verts[3].equals(end))) {
                return splitQuad(verts[2], verts[3], verts[0], verts[1], target, g);
            } else if ((verts[3].equals(start) || verts[3].equals(end)) && (verts[0].equals(start) || verts[0].equals(end))) {
                return splitQuad(verts[3], verts[0], verts[1], verts[2], target, g);
            }
        }
        return result;
    }

    private List<GData3> splitQuad(Vertex v1, Vertex v2, Vertex v3, Vertex v4, Vertex target, GData4 g) {
        ArrayList<GData3> result = new ArrayList<GData3>();
        result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a, v4.X, v4.Y, v4.Z, v1.X, v1.Y, v1.Z, target.X, target.Y, target.Z, View.DUMMY_REFERENCE, linkedDatFile));
        result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a, target.X, target.Y, target.Z, v2.X, v2.Y, v2.Z, v4.X, v4.Y, v4.Z, View.DUMMY_REFERENCE, linkedDatFile));
        result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a, v2.X, v2.Y, v2.Z, v3.X, v3.Y, v3.Z, v4.X, v4.Y, v4.Z, View.DUMMY_REFERENCE, linkedDatFile));
        return result;
    }

    private List<GData3> split(GData3 g, Vertex start, Vertex end, Vertex target) {
        ArrayList<GData3> result = new ArrayList<GData3>();
        if (!start.equals(end)) {
            Vertex[] verts = triangles.get(g);
            if ((verts[0].equals(start) || verts[0].equals(end)) && (verts[1].equals(start) || verts[1].equals(end))) {
                return splitTri(verts[0], verts[1], verts[2], target, g);
            } else if ((verts[1].equals(start) || verts[1].equals(end)) && (verts[2].equals(start) || verts[2].equals(end))) {
                return splitTri(verts[1], verts[2], verts[0], target, g);
            } else if ((verts[2].equals(start) || verts[2].equals(end)) && (verts[0].equals(start) || verts[0].equals(end))) {
                return splitTri(verts[2], verts[0], verts[1], target, g);
            }
        }
        return result;
    }

    private List<GData3> splitTri(Vertex v1, Vertex v2, Vertex v3, Vertex target, GData3 g) {
        ArrayList<GData3> result = new ArrayList<GData3>();
        result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a, v3.X, v3.Y, v3.Z, v1.X, v1.Y, v1.Z, target.X, target.Y, target.Z, View.DUMMY_REFERENCE, linkedDatFile));
        result.add(new GData3(g.colourNumber, g.r, g.g, g.b, g.a, target.X, target.Y, target.Z, v2.X, v2.Y, v2.Z, v3.X, v3.Y, v3.Z, View.DUMMY_REFERENCE, linkedDatFile));
        return result;
    }

    private List<GData2> split(GData2 g, Vertex start, Vertex end, Vertex target) {
        ArrayList<GData2> result = new ArrayList<GData2>();

        if (!start.equals(end)) {
            Vertex[] verts = lines.get(g);
            if ((verts[0].equals(start) || verts[0].equals(end)) && (verts[1].equals(start) || verts[1].equals(end))) {
                result.add(new GData2(g.colourNumber, g.r, g.g, g.b, g.a, start.X, start.Y, start.Z, target.X, target.Y, target.Z, View.DUMMY_REFERENCE, linkedDatFile));
                result.add(new GData2(g.colourNumber, g.r, g.g, g.b, g.a, target.X, target.Y, target.Z, end.X, end.Y, end.Z, View.DUMMY_REFERENCE, linkedDatFile));
            }
        }
        return result;
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
