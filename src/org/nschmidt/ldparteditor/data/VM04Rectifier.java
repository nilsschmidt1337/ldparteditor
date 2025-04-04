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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.lwjgl.util.vector.Matrix4f;
import org.nschmidt.ldparteditor.enumtype.Axis;
import org.nschmidt.ldparteditor.enumtype.LDConfig;
import org.nschmidt.ldparteditor.enumtype.Threshold;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.helper.LDPartEditorException;
import org.nschmidt.ldparteditor.helper.composite3d.RectifierSettings;
import org.nschmidt.ldparteditor.helper.math.MathHelper;
import org.nschmidt.ldparteditor.helper.math.Vector3d;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.text.DatParser;

class VM04Rectifier extends VM03Adjacency {

    protected VM04Rectifier(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public int[] rectify(final RectifierSettings rs, boolean syncWithTextEditor, final boolean showProgressMonitor) {

        final int[] result2 = new int[]{0, 0, 0, 0};

        if (linkedDatFile.isReadOnly()) return result2;

        final double targetAngle = rs.getMaximumAngle().doubleValue();
        final boolean colourise = rs.isColourise();
        final int scope = rs.getScope();

        final BigDecimal two = new BigDecimal(2);

        final Set<GData3> trisToIgnore = new HashSet<>();

        final Set<GData2> linesToDelete = new HashSet<>();
        final Set<GData4> quadsToDelete = new HashSet<>();
        final Set<GData5> clinesToDelete = new HashSet<>();

        final Set<GData> surfsToParse = new HashSet<>();

        if (scope == 0) {
            surfsToParse.addAll(triangles.keySet());
            surfsToParse.addAll(quads.keySet());
        } else {
            surfsToParse.addAll(selectedTriangles);
            surfsToParse.addAll(selectedQuads);
        }

        clearSelection();

        for (Iterator<GData> ig = surfsToParse.iterator(); ig.hasNext();) {
            GData g = ig.next();
            if (!lineLinkedToVertices.containsKey(g)) {
                ig.remove();
            }
        }

        final boolean noAdjacentCondlines = rs.isNoRectConversationOnAdjacentCondlines();
        final boolean replaceQuads = !rs.isNoQuadConversation();
        final GColour col16 = LDConfig.getColour16();

        if (showProgressMonitor) {
            try
            {
                new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
                {
                    @Override
                    public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                    {
                        try
                        {
                            monitor.beginTask(I18n.VM_RECTIFY, IProgressMonitor.UNKNOWN);
                            for (GData g : surfsToParse) {
                                /* Check if the monitor has been canceled */
                                if (monitor.isCanceled()) break;
                                if (trisToIgnore.contains(g)) continue;
                                GData quad = null;
                                if (g.type() == 3 && replaceQuads) {

                                    GData3 tri = (GData3) g;
                                    Vertex[] v = triangles.get(tri);

                                    @SuppressWarnings("unchecked")
                                    List<GData>[] cf = new ArrayList[3];

                                    cf[0] = linkedCommonFaces(v[0], v[1]);
                                    cf[1] = linkedCommonFaces(v[0], v[2]);
                                    cf[2] = linkedCommonFaces(v[1], v[2]);

                                    int bestIndex = -1;
                                    BigDecimal bestRatio = null;
                                    BigDecimal bestRatioSelection = null;
                                    for(int i = 0; i < 3; i++) {
                                        if (cf[i].size() == 2 && cf[i].get(0).type() == 3 && cf[i].get(1).type() == 3) {
                                            GData3 tri1 = (GData3) cf[i].get(0);
                                            GData3 tri2 = (GData3) cf[i].get(1);
                                            if (tri1.parent.equals(View.DUMMY_REFERENCE) && tri2.parent.equals(View.DUMMY_REFERENCE) && tri1.colourNumber == tri2.colourNumber && (tri1.colourNumber != -1 || tri1.r == tri2.r && tri1.g == tri2.g && tri1.b == tri2.b && tri1.a == tri2.a)) {

                                                SortedSet<Vertex> tri1V = new TreeSet<>();
                                                SortedSet<Vertex> tri2V = new TreeSet<>();
                                                SortedSet<Vertex> triC = new TreeSet<>();

                                                Vertex[] v1 = triangles.get(tri1);
                                                Vertex[] v2 = triangles.get(tri2);

                                                tri1V.addAll(Arrays.asList(v1));
                                                tri2V.addAll(Arrays.asList(v2));
                                                triC.addAll(tri1V);
                                                triC.retainAll(tri2V);
                                                tri2V.removeAll(tri1V);

                                                tri1V.removeAll(triC);
                                                tri1V.removeAll(tri2V);

                                                if (tri2V.iterator().hasNext() && tri1V.iterator().hasNext() && triC.size() == 2) {

                                                    Vector3d n1 = new Vector3d(
                                                            tri1.y3p.subtract(tri1.y1p).multiply(tri1.z2p.subtract(tri1.z1p)).subtract(tri1.z3p.subtract(tri1.z1p).multiply(tri1.y2p.subtract(tri1.y1p))),
                                                            tri1.z3p.subtract(tri1.z1p).multiply(tri1.x2p.subtract(tri1.x1p)).subtract(tri1.x3p.subtract(tri1.x1p).multiply(tri1.z2p.subtract(tri1.z1p))),
                                                            tri1.x3p.subtract(tri1.x1p).multiply(tri1.y2p.subtract(tri1.y1p)).subtract(tri1.y3p.subtract(tri1.y1p).multiply(tri1.x2p.subtract(tri1.x1p)))
                                                            );
                                                    Vector3d n2 = new Vector3d(
                                                            tri2.y3p.subtract(tri2.y1p).multiply(tri2.z2p.subtract(tri2.z1p)).subtract(tri2.z3p.subtract(tri2.z1p).multiply(tri2.y2p.subtract(tri2.y1p))),
                                                            tri2.z3p.subtract(tri2.z1p).multiply(tri2.x2p.subtract(tri2.x1p)).subtract(tri2.x3p.subtract(tri2.x1p).multiply(tri2.z2p.subtract(tri2.z1p))),
                                                            tri2.x3p.subtract(tri2.x1p).multiply(tri2.y2p.subtract(tri2.y1p)).subtract(tri2.y3p.subtract(tri2.y1p).multiply(tri2.x2p.subtract(tri2.x1p)))
                                                            );

                                                    double angle = Vector3d.angle(n1, n2);
                                                    angle = Math.min(angle, 180.0 - angle);
                                                    if (angle <= targetAngle) {

                                                        Vertex first = tri1V.iterator().next();
                                                        Vertex third = tri2V.iterator().next();

                                                        Vertex second = null;
                                                        {
                                                            boolean firstFound = false;
                                                            for (Vertex ve : v1) {
                                                                if (firstFound) {
                                                                    if (triC.contains(ve)) {
                                                                        second = ve;
                                                                        break;
                                                                    }
                                                                } else if (ve.equals(first)) {
                                                                    firstFound = true;
                                                                }
                                                            }
                                                            if (second == null) {
                                                                second = v1[0];
                                                            }
                                                        }

                                                        Vertex fourth = null;
                                                        for (Vertex ve : v1) {
                                                            if (triC.contains(ve) && !ve.equals(second)) {
                                                                fourth = ve;
                                                            }
                                                        }

                                                        Vector3d[] normals = new Vector3d[4];
                                                        float[] normalDirections = new float[4];
                                                        Vector3d[] lineVectors = new Vector3d[4];
                                                        int cnc = 0;
                                                        Vector3d vertexA = new Vector3d(first);
                                                        Vector3d vertexB = new Vector3d(second);
                                                        Vector3d vertexC = new Vector3d(third);
                                                        Vector3d vertexD = new Vector3d(fourth);
                                                        lineVectors[0] = Vector3d.sub(vertexB, vertexA);
                                                        lineVectors[1] = Vector3d.sub(vertexC, vertexB);
                                                        lineVectors[2] = Vector3d.sub(vertexD, vertexC);
                                                        lineVectors[3] = Vector3d.sub(vertexA, vertexD);
                                                        normals[0] = Vector3d.cross(lineVectors[0], lineVectors[1]);
                                                        normals[1] = Vector3d.cross(lineVectors[1], lineVectors[2]);
                                                        normals[2] = Vector3d.cross(lineVectors[2], lineVectors[3]);
                                                        normals[3] = Vector3d.cross(lineVectors[3], lineVectors[0]);
                                                        for (int k = 0; k < 4; k++) {
                                                            normalDirections[k] = MathHelper.directionOfVectors(normals[0], normals[k]);
                                                            if (normalDirections[k] < 0) {
                                                                cnc++;
                                                            }
                                                        }

                                                        if (cnc == 1 || cnc == 3 || !linkedDatFile.getDrawPerLineNoClone().containsValue(tri2)) {
                                                            // Concave
                                                            continue;
                                                        }

                                                        angle = Vector3d.angle(normals[0], normals[2]);
                                                        if (angle > targetAngle) continue;

                                                        Vector3d a = Vector3d.sub(vertexB, vertexA);
                                                        Vector3d b = Vector3d.sub(vertexB, vertexC);
                                                        Vector3d c = Vector3d.sub(vertexD, vertexC);
                                                        Vector3d d = Vector3d.sub(vertexD, vertexA);

                                                        angle = Vector3d.angle(a, d);
                                                        double sumAngle = angle;
                                                        if (angle < Threshold.COLLINEAR_ANGLE_MINIMUM || angle > Threshold.COLLINEAR_ANGLE_MAXIMUM) {
                                                            continue;
                                                        }

                                                        angle = Vector3d.angle(b, c);
                                                        sumAngle = sumAngle + angle;
                                                        if (angle < Threshold.COLLINEAR_ANGLE_MINIMUM || angle > Threshold.COLLINEAR_ANGLE_MAXIMUM) {
                                                            continue;
                                                        }

                                                        a.negate();
                                                        b.negate();
                                                        angle = Vector3d.angle(a, b);
                                                        sumAngle = sumAngle + angle;
                                                        if (angle < Threshold.COLLINEAR_ANGLE_MINIMUM || angle > Threshold.COLLINEAR_ANGLE_MAXIMUM) {
                                                            continue;
                                                        }

                                                        angle = 360.0 - sumAngle;
                                                        if (angle < Threshold.COLLINEAR_ANGLE_MINIMUM || angle > Threshold.COLLINEAR_ANGLE_MAXIMUM) {
                                                            continue;
                                                        }

                                                        BigDecimal m1 = Vector3d.distSquare(new Vector3d(first), new Vector3d(third)).add(BigDecimal.ONE);
                                                        BigDecimal m2 = Vector3d.distSquare(new Vector3d(second), new Vector3d(fourth)).add(BigDecimal.ONE);
                                                        BigDecimal ratio = m1.compareTo(m2) > 0 ? m1.divide(m2, Threshold.MC) : m2.divide(m1, Threshold.MC);
                                                        // When both surfaces were selected, then prefer the selection over possible better unselected alternatives.
                                                        if (scope == 1 && (bestRatioSelection == null || ratio.compareTo(bestRatioSelection) < 0) && surfsToParse.contains(tri1) && surfsToParse.contains(tri2)) {
                                                            bestRatioSelection = ratio;
                                                            bestIndex = i;
                                                        } else if (bestRatioSelection == null && (bestIndex == -1 || ratio.compareTo(bestRatio) < 0)) {
                                                            bestRatio = ratio;
                                                            bestIndex = i;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (bestIndex != -1) {
                                        GData3 tri1 = (GData3) cf[bestIndex].get(0);
                                        GData3 tri2 = (GData3) cf[bestIndex].get(1);

                                        SortedSet<Vertex> tri1V = new TreeSet<>();
                                        SortedSet<Vertex> tri2V = new TreeSet<>();
                                        SortedSet<Vertex> triC = new TreeSet<>();

                                        Vertex[] v1 = triangles.get(tri1);
                                        Vertex[] v2 = triangles.get(tri2);

                                        tri1V.addAll(Arrays.asList(v1));
                                        tri2V.addAll(Arrays.asList(v2));
                                        triC.addAll(tri1V);
                                        triC.retainAll(tri2V);
                                        tri2V.removeAll(tri1V);

                                        tri1V.removeAll(triC);
                                        tri1V.removeAll(tri2V);

                                        if (tri2V.iterator().hasNext() && tri1V.iterator().hasNext() && triC.size() == 2) {

                                            Vertex first = tri1V.iterator().next();
                                            Vertex third = tri2V.iterator().next();

                                            Vertex second = null;
                                            {
                                                boolean firstFound = false;
                                                for (Vertex ve : v1) {
                                                    if (firstFound) {
                                                        if (triC.contains(ve)) {
                                                            second = ve;
                                                            break;
                                                        }
                                                    } else if (ve.equals(first)) {
                                                        firstFound = true;
                                                    }
                                                }
                                                if (second == null) {
                                                    second = v1[0];
                                                }
                                            }

                                            Vertex fourth = null;
                                            for (Vertex ve : v1) {
                                                if (triC.contains(ve) && !ve.equals(second)) {
                                                    fourth = ve;
                                                }
                                            }

                                            Set<GData> lines1 = new HashSet<>();
                                            Set<GData> lines2 = new HashSet<>();
                                            Set<GData> lines3 = new HashSet<>();
                                            Set<GData> lines4 = new HashSet<>();


                                            {
                                                Set<VertexManifestation> s1 = vertexLinkedToPositionInFile.get(first);
                                                for (VertexManifestation m : s1) {
                                                    GData gd = m.gdata();
                                                    int t = gd.type();
                                                    if (lineLinkedToVertices.containsKey(gd) && m.position() < 2 && (t == 2 || t == 5)) lines1.add(gd);
                                                }
                                            }
                                            {
                                                Set<VertexManifestation> s2 = vertexLinkedToPositionInFile.get(second);
                                                for (VertexManifestation m : s2) {
                                                    GData gd = m.gdata();
                                                    int t = gd.type();
                                                    if (lineLinkedToVertices.containsKey(gd) && m.position() < 2 && (t == 2 || t == 5)) lines2.add(gd);
                                                }
                                            }
                                            {
                                                Set<VertexManifestation> s3 = vertexLinkedToPositionInFile.get(third);
                                                for (VertexManifestation m : s3) {
                                                    GData gd = m.gdata();
                                                    int t = gd.type();
                                                    if (lineLinkedToVertices.containsKey(gd) && m.position() < 2 && (t == 2 || t == 5)) lines3.add(gd);
                                                }
                                            }
                                            {
                                                Set<VertexManifestation> s4 = vertexLinkedToPositionInFile.get(fourth);
                                                for (VertexManifestation m : s4) {
                                                    GData gd = m.gdata();
                                                    int t = gd.type();
                                                    if (lineLinkedToVertices.containsKey(gd) && m.position() < 2 &&  (t == 2 || t == 5)) lines4.add(gd);
                                                }
                                            }

                                            if (colourise) {
                                                GColour yellow = LDConfig.hasColour(14) ? LDConfig.getColour(14) : new GColour(-1, 1f, 1f, 0f, 1f);

                                                quad = new GData4(yellow.getColourNumber(), yellow.getR(), yellow.getG(), yellow.getB(), yellow.getA(), first, second, third, fourth, View.DUMMY_REFERENCE, linkedDatFile);

                                            } else {

                                                quad = new GData4(tri1.colourNumber, tri1.r, tri1.g, tri1.b, tri1.a, first, second, third, fourth, View.DUMMY_REFERENCE, linkedDatFile);

                                            }

                                            linkedDatFile.insertAfter(tri2, quad);
                                            setModifiedNoSync();
                                            result2[0] += 2;

                                            lines1.retainAll(lines3);
                                            lines2.retainAll(lines4);
                                            lines1.addAll(lines2);
                                            for (GData gData : lines1) {
                                                if (gData.type() == 5) {
                                                    clinesToDelete.add((GData5) gData);
                                                    result2[1] += 1;
                                                }
                                            }

                                            trisToIgnore.add(tri1);
                                            trisToIgnore.add(tri2);
                                        }
                                    }
                                }
                                if (quad != null || g.type() == 4) {
                                    if (rs.isNoBorderedQuadToRectConversation()) continue;
                                    if (quad == null) {
                                        quad = g;
                                    }
                                    GData4 qa = (GData4) quad;

                                    BigDecimal d1X = qa.x1p.add(qa.x3p).divide(two);
                                    BigDecimal d2X = qa.x2p.add(qa.x4p).divide(two);
                                    if (d1X.compareTo(d2X) == 0) {
                                        BigDecimal d1Y = qa.y1p.add(qa.y3p).divide(two);
                                        BigDecimal d2Y = qa.y2p.add(qa.y4p).divide(two);
                                        if (d1Y.compareTo(d2Y) == 0) {
                                            BigDecimal d1Z = qa.z1p.add(qa.z3p).divide(two);
                                            BigDecimal d2Z = qa.z2p.add(qa.z4p).divide(two);
                                            if (d1Z.compareTo(d2Z) == 0) {

                                                // Its a rhombus!

                                                Vertex[] vq = quads.get(qa);
                                                GData2 e1 = hasEdge(vq[0], vq[1]);
                                                GData2 e2 = hasEdge(vq[1], vq[2]);
                                                GData2 e3 = hasEdge(vq[2], vq[3]);
                                                GData2 e4 = hasEdge(vq[3], vq[0]);

                                                if (noAdjacentCondlines && hasCondline(vq[0], vq[1]) != null || hasCondline(vq[1], vq[2])  != null || hasCondline(vq[2], vq[3]) != null || hasCondline(vq[3], vq[0]) != null) {
                                                    continue;
                                                }

                                                if (linesToDelete.contains(e1)) {
                                                    e1 = null;
                                                }
                                                if (linesToDelete.contains(e2)) {
                                                    e2 = null;
                                                }
                                                if (linesToDelete.contains(e3)) {
                                                    e3 = null;
                                                }
                                                if (linesToDelete.contains(e4)) {
                                                    e4 = null;
                                                }

                                                int edgeflags =  (e1 != null ? 1 : 0) + (e2 != null ? 2 : 0) + (e3 != null ? 4 : 0) + (e4 != null ? 8 : 0);

                                                Matrix accurateLocalMatrix = null;
                                                String shortName = null;

                                                switch(edgeflags)
                                                {
                                                case 0:
                                                    continue;
                                                case 1:
                                                    vq = rotquad(vq);
                                                    vq = rotquad(vq);
                                                    vq = rotquad(vq);
                                                    shortName = "rect1.dat"; //$NON-NLS-1$
                                                    break;
                                                case 2:
                                                    shortName = "rect1.dat"; //$NON-NLS-1$
                                                    break;
                                                case 4:
                                                    vq = rotquad(vq);
                                                    shortName = "rect1.dat"; //$NON-NLS-1$
                                                    break;
                                                case 8:
                                                    vq = rotquad(vq);
                                                    vq = rotquad(vq);
                                                    shortName = "rect1.dat"; //$NON-NLS-1$
                                                    break;
                                                case 3:
                                                    vq = rotquad(vq);
                                                    vq = rotquad(vq);
                                                    vq = rotquad(vq);
                                                    shortName = "rect2a.dat"; //$NON-NLS-1$
                                                    break;
                                                case 6:
                                                    shortName = "rect2a.dat"; //$NON-NLS-1$
                                                    break;
                                                case 12:
                                                    vq = rotquad(vq);
                                                    shortName = "rect2a.dat"; //$NON-NLS-1$
                                                    break;
                                                case 9:
                                                    vq = rotquad(vq);
                                                    vq = rotquad(vq);
                                                    shortName = "rect2a.dat"; //$NON-NLS-1$
                                                    break;
                                                case 5:
                                                    shortName = "rect2p.dat"; //$NON-NLS-1$
                                                    break;
                                                case 10:
                                                    vq = rotquad(vq);
                                                    shortName = "rect2p.dat"; //$NON-NLS-1$
                                                    break;
                                                case 7:
                                                    vq = rotquad(vq);
                                                    vq = rotquad(vq);
                                                    vq = rotquad(vq);
                                                    shortName = "rect3.dat"; //$NON-NLS-1$
                                                    break;
                                                case 14:
                                                    shortName = "rect3.dat"; //$NON-NLS-1$
                                                    break;
                                                case 13:
                                                    vq = rotquad(vq);
                                                    shortName = "rect3.dat"; //$NON-NLS-1$
                                                    break;
                                                case 11:
                                                    vq = rotquad(vq);
                                                    vq = rotquad(vq);
                                                    shortName = "rect3.dat"; //$NON-NLS-1$
                                                    break;
                                                case 15:
                                                    shortName = "rect.dat"; //$NON-NLS-1$
                                                    break;
                                                default:
                                                    break;
                                                }

                                                Vector3d vertexA = new Vector3d(vq[0]);
                                                Vector3d vertexB = new Vector3d(vq[1]);
                                                Vector3d vertexD = new Vector3d(vq[3]);

                                                // Quad local basis
                                                Vector3d temp1 = Vector3d.sub(vertexB, vertexA);
                                                Vector3d temp2 = Vector3d.sub(vertexD, vertexA);
                                                Vector3d temp3 = Vector3d.cross(temp2, temp1);

                                                accurateLocalMatrix = new Matrix(
                                                        temp1.x.divide(two), temp1.y.divide(two), temp1.z.divide(two), BigDecimal.ZERO,
                                                        temp3.x.divide(two), temp3.y.divide(two), temp3.z.divide(two), BigDecimal.ZERO,
                                                        temp2.x.divide(two), temp2.y.divide(two), temp2.z.divide(two), BigDecimal.ZERO,
                                                        d1X, d1Y, d1Z, BigDecimal.ONE);

                                                accurateLocalMatrix = repair(accurateLocalMatrix);

                                                StringBuilder lineBuilder = new StringBuilder();
                                                lineBuilder.append(1);
                                                lineBuilder.append(" "); //$NON-NLS-1$
                                                int colourNumber = qa.colourNumber;
                                                if (rs.isColourise()) colourNumber = 1;
                                                if (colourNumber == -1) {
                                                    lineBuilder.append("0x2"); //$NON-NLS-1$
                                                    lineBuilder.append(MathHelper.toHex((int) (255f * qa.r)).toUpperCase());
                                                    lineBuilder.append(MathHelper.toHex((int) (255f * qa.g)).toUpperCase());
                                                    lineBuilder.append(MathHelper.toHex((int) (255f * qa.b)).toUpperCase());
                                                } else {
                                                    lineBuilder.append(colourNumber);
                                                }
                                                lineBuilder.append(" "); //$NON-NLS-1$

                                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.m30));
                                                lineBuilder.append(" "); //$NON-NLS-1$
                                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.m31));
                                                lineBuilder.append(" "); //$NON-NLS-1$
                                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.m32));

                                                final int lastDecimalPointInPosition = lineBuilder.lastIndexOf("."); //$NON-NLS-1$

                                                final Axis x = determineAxis(accurateLocalMatrix.m00, accurateLocalMatrix.m10, accurateLocalMatrix.m20);
                                                final Axis y = determineAxis(accurateLocalMatrix.m01, accurateLocalMatrix.m11, accurateLocalMatrix.m21);
                                                final Axis z = determineAxis(accurateLocalMatrix.m02, accurateLocalMatrix.m12, accurateLocalMatrix.m22);

                                                final boolean hasDifferentOrthogonalAxes =
                                                        x != Axis.NONE && y != Axis.NONE && z != Axis.NONE &&
                                                        x != y && y != z && x != z;

                                                lineBuilder.append(" "); //$NON-NLS-1$
                                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.m00));
                                                lineBuilder.append(" "); //$NON-NLS-1$
                                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.m10));
                                                lineBuilder.append(" "); //$NON-NLS-1$
                                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.m20));
                                                lineBuilder.append(" "); //$NON-NLS-1$
                                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.m01));
                                                lineBuilder.append(" "); //$NON-NLS-1$
                                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.m11));
                                                lineBuilder.append(" "); //$NON-NLS-1$
                                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.m21));
                                                lineBuilder.append(" "); //$NON-NLS-1$
                                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.m02));
                                                lineBuilder.append(" "); //$NON-NLS-1$
                                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.m12));
                                                lineBuilder.append(" "); //$NON-NLS-1$
                                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.m22));

                                                if (rs.isNoDecimalsInRectPrims() && !hasDifferentOrthogonalAxes && lineBuilder.lastIndexOf(".") > lastDecimalPointInPosition) continue; //$NON-NLS-1$

                                                lineBuilder.append(" "); //$NON-NLS-1$
                                                lineBuilder.append(shortName);

                                                Set<String> alreadyParsed = new HashSet<>();
                                                alreadyParsed.add(linkedDatFile.getShortName());
                                                List<ParsingResult> result = DatParser.parseLine(lineBuilder.toString(), -1, 0, col16.getR(), col16.getG(), col16.getB(), 1.0f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile, false, alreadyParsed);
                                                GData rect = result.get(0).getGraphicalData();
                                                if (rect == null)
                                                    rect = new GData0(lineBuilder.toString(), View.DUMMY_REFERENCE);
                                                linkedDatFile.insertAfter(qa, rect);

                                                quadsToDelete.add(qa);
                                                result2[2] += 1;
                                                if (e1 != null) {linesToDelete.add(e1); result2[3] += 1;}
                                                if (e2 != null) {linesToDelete.add(e2); result2[3] += 1;}
                                                if (e3 != null) {linesToDelete.add(e3); result2[3] += 1;}
                                                if (e4 != null) {linesToDelete.add(e4); result2[3] += 1;}
                                            }
                                        }
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
            catch (InvocationTargetException ite) {
                NLogger.error(VM04Rectifier.class, ite);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new LDPartEditorException(ie);
            }
        } else {
            for (GData g : surfsToParse) {
                if (trisToIgnore.contains(g)) continue;
                GData quad = null;
                if (g.type() == 3 && replaceQuads) {

                    GData3 tri = (GData3) g;
                    Vertex[] v = triangles.get(tri);

                    @SuppressWarnings("unchecked")
                    List<GData>[] cf = new ArrayList[3];

                    cf[0] = linkedCommonFaces(v[0], v[1]);
                    cf[1] = linkedCommonFaces(v[0], v[2]);
                    cf[2] = linkedCommonFaces(v[1], v[2]);

                    int bestIndex = -1;
                    BigDecimal bestRatio = null;
                    BigDecimal bestRatioSelection = null;
                    for(int i = 0; i < 3; i++) {
                        if (cf[i].size() == 2 && cf[i].get(0).type() == 3 && cf[i].get(1).type() == 3) {
                            GData3 tri1 = (GData3) cf[i].get(0);
                            GData3 tri2 = (GData3) cf[i].get(1);
                            if (tri1.parent.equals(View.DUMMY_REFERENCE) && tri2.parent.equals(View.DUMMY_REFERENCE) && tri1.colourNumber == tri2.colourNumber && (tri1.colourNumber != -1 || tri1.r == tri2.r && tri1.g == tri2.g && tri1.b == tri2.b && tri1.a == tri2.a)) {

                                SortedSet<Vertex> tri1V = new TreeSet<>();
                                SortedSet<Vertex> tri2V = new TreeSet<>();
                                SortedSet<Vertex> triC = new TreeSet<>();

                                Vertex[] v1 = triangles.get(tri1);
                                Vertex[] v2 = triangles.get(tri2);

                                tri1V.addAll(Arrays.asList(v1));
                                tri2V.addAll(Arrays.asList(v2));
                                triC.addAll(tri1V);
                                triC.retainAll(tri2V);
                                tri2V.removeAll(tri1V);

                                tri1V.removeAll(triC);
                                tri1V.removeAll(tri2V);

                                if (tri2V.iterator().hasNext() && tri1V.iterator().hasNext() && triC.size() == 2) {

                                    Vector3d n1 = new Vector3d(
                                            tri1.y3p.subtract(tri1.y1p).multiply(tri1.z2p.subtract(tri1.z1p)).subtract(tri1.z3p.subtract(tri1.z1p).multiply(tri1.y2p.subtract(tri1.y1p))),
                                            tri1.z3p.subtract(tri1.z1p).multiply(tri1.x2p.subtract(tri1.x1p)).subtract(tri1.x3p.subtract(tri1.x1p).multiply(tri1.z2p.subtract(tri1.z1p))),
                                            tri1.x3p.subtract(tri1.x1p).multiply(tri1.y2p.subtract(tri1.y1p)).subtract(tri1.y3p.subtract(tri1.y1p).multiply(tri1.x2p.subtract(tri1.x1p)))
                                            );
                                    Vector3d n2 = new Vector3d(
                                            tri2.y3p.subtract(tri2.y1p).multiply(tri2.z2p.subtract(tri2.z1p)).subtract(tri2.z3p.subtract(tri2.z1p).multiply(tri2.y2p.subtract(tri2.y1p))),
                                            tri2.z3p.subtract(tri2.z1p).multiply(tri2.x2p.subtract(tri2.x1p)).subtract(tri2.x3p.subtract(tri2.x1p).multiply(tri2.z2p.subtract(tri2.z1p))),
                                            tri2.x3p.subtract(tri2.x1p).multiply(tri2.y2p.subtract(tri2.y1p)).subtract(tri2.y3p.subtract(tri2.y1p).multiply(tri2.x2p.subtract(tri2.x1p)))
                                            );

                                    double angle = Vector3d.angle(n1, n2);
                                    angle = Math.min(angle, 180.0 - angle);
                                    if (angle <= targetAngle) {

                                        Vertex first = tri1V.iterator().next();
                                        Vertex third = tri2V.iterator().next();

                                        Vertex second = null;
                                        {
                                            boolean firstFound = false;
                                            for (Vertex ve : v1) {
                                                if (firstFound) {
                                                    if (triC.contains(ve)) {
                                                        second = ve;
                                                        break;
                                                    }
                                                } else if (ve.equals(first)) {
                                                    firstFound = true;
                                                }
                                            }
                                            if (second == null) {
                                                second = v1[0];
                                            }
                                        }

                                        Vertex fourth = null;
                                        for (Vertex ve : v1) {
                                            if (triC.contains(ve) && !ve.equals(second)) {
                                                fourth = ve;
                                            }
                                        }

                                        Vector3d[] normals = new Vector3d[4];
                                        float[] normalDirections = new float[4];
                                        Vector3d[] lineVectors = new Vector3d[4];
                                        int cnc = 0;
                                        Vector3d vertexA = new Vector3d(first);
                                        Vector3d vertexB = new Vector3d(second);
                                        Vector3d vertexC = new Vector3d(third);
                                        Vector3d vertexD = new Vector3d(fourth);
                                        lineVectors[0] = Vector3d.sub(vertexB, vertexA);
                                        lineVectors[1] = Vector3d.sub(vertexC, vertexB);
                                        lineVectors[2] = Vector3d.sub(vertexD, vertexC);
                                        lineVectors[3] = Vector3d.sub(vertexA, vertexD);
                                        normals[0] = Vector3d.cross(lineVectors[0], lineVectors[1]);
                                        normals[1] = Vector3d.cross(lineVectors[1], lineVectors[2]);
                                        normals[2] = Vector3d.cross(lineVectors[2], lineVectors[3]);
                                        normals[3] = Vector3d.cross(lineVectors[3], lineVectors[0]);
                                        for (int k = 0; k < 4; k++) {
                                            normalDirections[k] = MathHelper.directionOfVectors(normals[0], normals[k]);
                                            if (normalDirections[k] < 0) {
                                                cnc++;
                                            }
                                        }

                                        if (cnc == 1 || cnc == 3 || !linkedDatFile.getDrawPerLineNoClone().containsValue(tri2)) {
                                            // Concave
                                            continue;
                                        }

                                        angle = Vector3d.angle(normals[0], normals[2]);
                                        if (angle > targetAngle) continue;

                                        Vector3d a = Vector3d.sub(vertexB, vertexA);
                                        Vector3d b = Vector3d.sub(vertexB, vertexC);
                                        Vector3d c = Vector3d.sub(vertexD, vertexC);
                                        Vector3d d = Vector3d.sub(vertexD, vertexA);

                                        angle = Vector3d.angle(a, d);
                                        double sumAngle = angle;
                                        if (angle < Threshold.COLLINEAR_ANGLE_MINIMUM || angle > Threshold.COLLINEAR_ANGLE_MAXIMUM) {
                                            continue;
                                        }

                                        angle = Vector3d.angle(b, c);
                                        sumAngle = sumAngle + angle;
                                        if (angle < Threshold.COLLINEAR_ANGLE_MINIMUM || angle > Threshold.COLLINEAR_ANGLE_MAXIMUM) {
                                            continue;
                                        }

                                        a.negate();
                                        b.negate();
                                        angle = Vector3d.angle(a, b);
                                        sumAngle = sumAngle + angle;
                                        if (angle < Threshold.COLLINEAR_ANGLE_MINIMUM || angle > Threshold.COLLINEAR_ANGLE_MAXIMUM) {
                                            continue;
                                        }

                                        angle = 360.0 - sumAngle;
                                        if (angle < Threshold.COLLINEAR_ANGLE_MINIMUM || angle > Threshold.COLLINEAR_ANGLE_MAXIMUM) {
                                            continue;
                                        }

                                        BigDecimal m1 = Vector3d.distSquare(new Vector3d(first), new Vector3d(third)).add(BigDecimal.ONE);
                                        BigDecimal m2 = Vector3d.distSquare(new Vector3d(second), new Vector3d(fourth)).add(BigDecimal.ONE);
                                        BigDecimal ratio = m1.compareTo(m2) > 0 ? m1.divide(m2, Threshold.MC) : m2.divide(m1, Threshold.MC);
                                        // When both surfaces were selected, then prefer the selection over possible better unselected alternatives.
                                        if (scope == 1 && (bestRatioSelection == null || ratio.compareTo(bestRatioSelection) < 0) && surfsToParse.contains(tri1) && surfsToParse.contains(tri2)) {
                                            bestRatioSelection = ratio;
                                            bestIndex = i;
                                        } else if (bestRatioSelection == null && (bestIndex == -1 || ratio.compareTo(bestRatio) < 0)) {
                                            bestRatio = ratio;
                                            bestIndex = i;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (bestIndex != -1) {
                        GData3 tri1 = (GData3) cf[bestIndex].get(0);
                        GData3 tri2 = (GData3) cf[bestIndex].get(1);

                        SortedSet<Vertex> tri1V = new TreeSet<>();
                        SortedSet<Vertex> tri2V = new TreeSet<>();
                        SortedSet<Vertex> triC = new TreeSet<>();

                        Vertex[] v1 = triangles.get(tri1);
                        Vertex[] v2 = triangles.get(tri2);

                        tri1V.addAll(Arrays.asList(v1));
                        tri2V.addAll(Arrays.asList(v2));
                        triC.addAll(tri1V);
                        triC.retainAll(tri2V);
                        tri2V.removeAll(tri1V);

                        tri1V.removeAll(triC);
                        tri1V.removeAll(tri2V);

                        if (tri2V.iterator().hasNext() && tri1V.iterator().hasNext() && triC.size() == 2) {

                            Vertex first = tri1V.iterator().next();
                            Vertex third = tri2V.iterator().next();

                            Vertex second = null;
                            {
                                boolean firstFound = false;
                                for (Vertex ve : v1) {
                                    if (firstFound) {
                                        if (triC.contains(ve)) {
                                            second = ve;
                                            break;
                                        }
                                    } else if (ve.equals(first)) {
                                        firstFound = true;
                                    }
                                }
                                if (second == null) {
                                    second = v1[0];
                                }
                            }

                            Vertex fourth = null;
                            for (Vertex ve : v1) {
                                if (triC.contains(ve) && !ve.equals(second)) {
                                    fourth = ve;
                                }
                            }

                            Set<GData> lines1 = new HashSet<>();
                            Set<GData> lines2 = new HashSet<>();
                            Set<GData> lines3 = new HashSet<>();
                            Set<GData> lines4 = new HashSet<>();


                            {
                                Set<VertexManifestation> s1 = vertexLinkedToPositionInFile.get(first);
                                for (VertexManifestation m : s1) {
                                    GData gd = m.gdata();
                                    int t = gd.type();
                                    if (lineLinkedToVertices.containsKey(gd) && m.position() < 2 && (t == 2 || t == 5)) lines1.add(gd);
                                }
                            }
                            {
                                Set<VertexManifestation> s2 = vertexLinkedToPositionInFile.get(second);
                                for (VertexManifestation m : s2) {
                                    GData gd = m.gdata();
                                    int t = gd.type();
                                    if (lineLinkedToVertices.containsKey(gd) && m.position() < 2 && (t == 2 || t == 5)) lines2.add(gd);
                                }
                            }
                            {
                                Set<VertexManifestation> s3 = vertexLinkedToPositionInFile.get(third);
                                for (VertexManifestation m : s3) {
                                    GData gd = m.gdata();
                                    int t = gd.type();
                                    if (lineLinkedToVertices.containsKey(gd) && m.position() < 2 && (t == 2 || t == 5)) lines3.add(gd);
                                }
                            }
                            {
                                Set<VertexManifestation> s4 = vertexLinkedToPositionInFile.get(fourth);
                                for (VertexManifestation m : s4) {
                                    GData gd = m.gdata();
                                    int t = gd.type();
                                    if (lineLinkedToVertices.containsKey(gd) && m.position() < 2 &&  (t == 2 || t == 5)) lines4.add(gd);
                                }
                            }

                            if (colourise) {
                                GColour yellow = LDConfig.hasColour(14) ? LDConfig.getColour(14) : new GColour(-1, 1f, 1f, 0f, 1f);

                                quad = new GData4(yellow.getColourNumber(), yellow.getR(), yellow.getG(), yellow.getB(), yellow.getA(), first, second, third, fourth, View.DUMMY_REFERENCE, linkedDatFile);

                                linkedDatFile.insertAfter(tri2, quad);
                                setModifiedNoSync();
                            } else {

                                quad = new GData4(tri1.colourNumber, tri1.r, tri1.g, tri1.b, tri1.a, first, second, third, fourth, View.DUMMY_REFERENCE, linkedDatFile);

                                linkedDatFile.insertAfter(tri2, quad);
                                setModifiedNoSync();
                            }

                            result2[0] += 2;

                            lines1.retainAll(lines3);
                            lines2.retainAll(lines4);
                            lines1.addAll(lines2);
                            for (GData gData : lines1) {
                                if (gData.type() == 5) {
                                    clinesToDelete.add((GData5) gData);
                                }
                            }

                            trisToIgnore.add(tri1);
                            trisToIgnore.add(tri2);
                        }
                    }
                }
                if (quad != null || g.type() == 4) {
                    if (rs.isNoBorderedQuadToRectConversation()) continue;
                    if (quad == null) {
                        quad = g;
                    }
                    GData4 qa = (GData4) quad;

                    BigDecimal d1X = qa.x1p.add(qa.x3p).divide(two);
                    BigDecimal d2X = qa.x2p.add(qa.x4p).divide(two);
                    if (d1X.compareTo(d2X) == 0) {
                        BigDecimal d1Y = qa.y1p.add(qa.y3p).divide(two);
                        BigDecimal d2Y = qa.y2p.add(qa.y4p).divide(two);
                        if (d1Y.compareTo(d2Y) == 0) {
                            BigDecimal d1Z = qa.z1p.add(qa.z3p).divide(two);
                            BigDecimal d2Z = qa.z2p.add(qa.z4p).divide(two);
                            if (d1Z.compareTo(d2Z) == 0) {

                                // Its a rhombus!

                                Vertex[] vq = quads.get(qa);
                                GData2 e1 = hasEdge(vq[0], vq[1]);
                                GData2 e2 = hasEdge(vq[1], vq[2]);
                                GData2 e3 = hasEdge(vq[2], vq[3]);
                                GData2 e4 = hasEdge(vq[3], vq[0]);

                                if (noAdjacentCondlines && hasCondline(vq[0], vq[1]) != null || hasCondline(vq[1], vq[2])  != null || hasCondline(vq[2], vq[3]) != null || hasCondline(vq[3], vq[0]) != null) {
                                    continue;
                                }

                                if (linesToDelete.contains(e1)) {
                                    e1 = null;
                                }
                                if (linesToDelete.contains(e2)) {
                                    e2 = null;
                                }
                                if (linesToDelete.contains(e3)) {
                                    e3 = null;
                                }
                                if (linesToDelete.contains(e4)) {
                                    e4 = null;
                                }

                                int edgeflags =  (e1 != null ? 1 : 0) + (e2 != null ? 2 : 0) + (e3 != null ? 4 : 0) + (e4 != null ? 8 : 0);

                                Matrix accurateLocalMatrix = null;
                                String shortName = null;

                                switch(edgeflags)
                                {
                                case 0:
                                    continue;
                                case 1:
                                    vq = rotquad(vq);
                                    vq = rotquad(vq);
                                    vq = rotquad(vq);
                                    shortName = "rect1.dat"; //$NON-NLS-1$
                                    break;
                                case 2:
                                    shortName = "rect1.dat"; //$NON-NLS-1$
                                    break;
                                case 4:
                                    vq = rotquad(vq);
                                    shortName = "rect1.dat"; //$NON-NLS-1$
                                    break;
                                case 8:
                                    vq = rotquad(vq);
                                    vq = rotquad(vq);
                                    shortName = "rect1.dat"; //$NON-NLS-1$
                                    break;
                                case 3:
                                    vq = rotquad(vq);
                                    vq = rotquad(vq);
                                    vq = rotquad(vq);
                                    shortName = "rect2a.dat"; //$NON-NLS-1$
                                    break;
                                case 6:
                                    shortName = "rect2a.dat"; //$NON-NLS-1$
                                    break;
                                case 12:
                                    vq = rotquad(vq);
                                    shortName = "rect2a.dat"; //$NON-NLS-1$
                                    break;
                                case 9:
                                    vq = rotquad(vq);
                                    vq = rotquad(vq);
                                    shortName = "rect2a.dat"; //$NON-NLS-1$
                                    break;
                                case 5:
                                    shortName = "rect2p.dat"; //$NON-NLS-1$
                                    break;
                                case 10:
                                    vq = rotquad(vq);
                                    shortName = "rect2p.dat"; //$NON-NLS-1$
                                    break;
                                case 7:
                                    vq = rotquad(vq);
                                    vq = rotquad(vq);
                                    vq = rotquad(vq);
                                    shortName = "rect3.dat"; //$NON-NLS-1$
                                    break;
                                case 14:
                                    shortName = "rect3.dat"; //$NON-NLS-1$
                                    break;
                                case 13:
                                    vq = rotquad(vq);
                                    shortName = "rect3.dat"; //$NON-NLS-1$
                                    break;
                                case 11:
                                    vq = rotquad(vq);
                                    vq = rotquad(vq);
                                    shortName = "rect3.dat"; //$NON-NLS-1$
                                    break;
                                case 15:
                                    shortName = "rect.dat"; //$NON-NLS-1$
                                    break;
                                default:
                                    break;
                                }

                                Vector3d vertexA = new Vector3d(vq[0]);
                                Vector3d vertexB = new Vector3d(vq[1]);
                                Vector3d vertexD = new Vector3d(vq[3]);

                                // Quad local basis
                                Vector3d temp1 = Vector3d.sub(vertexB, vertexA);
                                Vector3d temp2 = Vector3d.sub(vertexD, vertexA);
                                Vector3d temp3 = Vector3d.cross(temp2, temp1);

                                accurateLocalMatrix = new Matrix(
                                        temp1.x.divide(two), temp1.y.divide(two), temp1.z.divide(two), BigDecimal.ZERO,
                                        temp3.x.divide(two), temp3.y.divide(two), temp3.z.divide(two), BigDecimal.ZERO,
                                        temp2.x.divide(two), temp2.y.divide(two), temp2.z.divide(two), BigDecimal.ZERO,
                                        d1X, d1Y, d1Z, BigDecimal.ONE);

                                accurateLocalMatrix = repair(accurateLocalMatrix);

                                StringBuilder lineBuilder = new StringBuilder();
                                lineBuilder.append(1);
                                lineBuilder.append(" "); //$NON-NLS-1$
                                int colourNumber = qa.colourNumber;
                                if (rs.isColourise()) colourNumber = 1;
                                if (colourNumber == -1) {
                                    lineBuilder.append("0x2"); //$NON-NLS-1$
                                    lineBuilder.append(MathHelper.toHex((int) (255f * qa.r)).toUpperCase());
                                    lineBuilder.append(MathHelper.toHex((int) (255f * qa.g)).toUpperCase());
                                    lineBuilder.append(MathHelper.toHex((int) (255f * qa.b)).toUpperCase());
                                } else {
                                    lineBuilder.append(colourNumber);
                                }
                                lineBuilder.append(" "); //$NON-NLS-1$

                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.m30));
                                lineBuilder.append(" "); //$NON-NLS-1$
                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.m31));
                                lineBuilder.append(" "); //$NON-NLS-1$
                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.m32));
                                lineBuilder.append(" "); //$NON-NLS-1$

                                final int lastDecimalPointInPosition = lineBuilder.lastIndexOf("."); //$NON-NLS-1$

                                final Axis x = determineAxis(accurateLocalMatrix.m00, accurateLocalMatrix.m10, accurateLocalMatrix.m20);
                                final Axis y = determineAxis(accurateLocalMatrix.m01, accurateLocalMatrix.m11, accurateLocalMatrix.m21);
                                final Axis z = determineAxis(accurateLocalMatrix.m02, accurateLocalMatrix.m12, accurateLocalMatrix.m22);

                                final boolean hasDifferentOrthogonalAxes =
                                        x != Axis.NONE && y != Axis.NONE && z != Axis.NONE &&
                                        x != y && y != z && x != z;

                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.m00));
                                lineBuilder.append(" "); //$NON-NLS-1$
                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.m10));
                                lineBuilder.append(" "); //$NON-NLS-1$
                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.m20));
                                lineBuilder.append(" "); //$NON-NLS-1$
                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.m01));
                                lineBuilder.append(" "); //$NON-NLS-1$
                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.m11));
                                lineBuilder.append(" "); //$NON-NLS-1$
                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.m21));
                                lineBuilder.append(" "); //$NON-NLS-1$
                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.m02));
                                lineBuilder.append(" "); //$NON-NLS-1$
                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.m12));
                                lineBuilder.append(" "); //$NON-NLS-1$
                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.m22));

                                if (rs.isNoDecimalsInRectPrims() && !hasDifferentOrthogonalAxes && lineBuilder.lastIndexOf(".") > lastDecimalPointInPosition) continue; //$NON-NLS-1$

                                lineBuilder.append(" "); //$NON-NLS-1$
                                lineBuilder.append(shortName);

                                Set<String> alreadyParsed = new HashSet<>();
                                alreadyParsed.add(linkedDatFile.getShortName());
                                List<ParsingResult> result = DatParser.parseLine(lineBuilder.toString(), -1, 0, col16.getR(), col16.getG(), col16.getB(), 1.0f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile, false, alreadyParsed);
                                GData rect = result.get(0).getGraphicalData();
                                if (rect == null)
                                    rect = new GData0(lineBuilder.toString(), View.DUMMY_REFERENCE);
                                linkedDatFile.insertAfter(qa, rect);

                                quadsToDelete.add(qa);
                                if (e1 != null) linesToDelete.add(e1);
                                if (e2 != null) linesToDelete.add(e2);
                                if (e3 != null) linesToDelete.add(e3);
                                if (e4 != null) linesToDelete.add(e4);
                            }
                        }
                    }
                }
            }
        }

        selectedLines.addAll(linesToDelete);
        selectedTriangles.addAll(trisToIgnore);
        selectedQuads.addAll(quadsToDelete);
        selectedCondlines.addAll(clinesToDelete);
        selectedData.addAll(selectedLines);
        selectedData.addAll(selectedTriangles);
        selectedData.addAll(quadsToDelete);
        selectedData.addAll(selectedCondlines);

        delete(false, syncWithTextEditor);

        validateState();

        return result2;
    }

    private Vertex[] rotquad(Vertex[] vq) {
        Vertex[] result = new Vertex[4];
        result[0] = vq[1];
        result[1] = vq[2];
        result[2] = vq[3];
        result[3] = vq[0];
        return result;
    }

    private Matrix repair(Matrix m) {
        final BigDecimal lengthY =  MathHelper.sqrt(m.m10.multiply(m.m10).add(m.m11.multiply(m.m11)).add(m.m12.multiply(m.m12))).subtract(BigDecimal.ONE).abs();
        final BigDecimal epsilon = new BigDecimal("0.000001"); //$NON-NLS-1$
        if (epsilon.compareTo(lengthY) < 0) {
            String line = "1 16 " + m.toLDrawString() + "1.dat"; //$NON-NLS-1$ //$NON-NLS-2$
            String[] dataSegments = line.trim().split("\\s+"); //$NON-NLS-1$
            StringBuilder sb = new StringBuilder();
            for (int k = 0; k < 3; k++) {
                sb.setLength(0);
                int i = 0;
                String sign;
                for (String seg : dataSegments) {
                    if (!seg.trim().equals("")) {  //$NON-NLS-1$
                        i++;
                        switch (i) {
                        case 7:
                            sign = seg.startsWith("-") ? "-" : ""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            sb.append(k == 1 ? sign + "1" : "0"); //$NON-NLS-1$ //$NON-NLS-2$
                            break;
                        case 10:
                            sign = seg.startsWith("-") ? "-" : ""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            sb.append(k == 0 ? sign + "1" : "0"); //$NON-NLS-1$ //$NON-NLS-2$
                            break;
                        case 13:
                            sign = seg.startsWith("-") ? "-" : ""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            sb.append(k == 2 ? sign + "1" : "0"); //$NON-NLS-1$ //$NON-NLS-2$
                            break;
                        default:
                            sb.append(seg);
                            break;
                        }
                    }
                    sb.append(" "); //$NON-NLS-1$
                }
                if (hasGoodDeterminant(sb.toString())) break;
            }
            line = sb.toString();
            dataSegments = line.trim().split("\\s+"); //$NON-NLS-1$
            // [ERROR] Check singularity
            try {
                // Offset
                BigDecimal m30 = new BigDecimal(dataSegments[2]);
                BigDecimal m31 = new BigDecimal(dataSegments[3]);
                BigDecimal m32 = new BigDecimal(dataSegments[4]);
                // First row
                BigDecimal m00 = new BigDecimal(dataSegments[5]);
                BigDecimal m10 = new BigDecimal(dataSegments[6]);
                BigDecimal m20 = new BigDecimal(dataSegments[7]);
                // Second row
                BigDecimal m01 = new BigDecimal(dataSegments[8]);
                BigDecimal m11 = new BigDecimal(dataSegments[9]);
                BigDecimal m21 = new BigDecimal(dataSegments[10]);
                // Third row
                BigDecimal m02 = new BigDecimal(dataSegments[11]);
                BigDecimal m12 = new BigDecimal(dataSegments[12]);
                BigDecimal m22 = new BigDecimal(dataSegments[13]);
                return new Matrix(m00, m01, m02, m02, m10, m11, m12, m12, m20, m21, m22, m22, m30, m31, m32, m32);
            } catch (NumberFormatException nfe) {
                NLogger.debug(VM04Rectifier.class, nfe);
            }
        }
        return m;
    }

    private boolean hasGoodDeterminant(String line) {
        String[] dataSegments = line.trim().split("\\s+"); //$NON-NLS-1$
        // [ERROR] Check singularity
        Matrix4f tMatrix = new Matrix4f();
        float det = 0;
        try {
            // Offset
            BigDecimal m30 = new BigDecimal(dataSegments[2]);
            tMatrix.m30 = m30.floatValue() * 1000f;
            BigDecimal m31 = new BigDecimal(dataSegments[3]);
            tMatrix.m31 = m31.floatValue() * 1000f;
            BigDecimal m32 = new BigDecimal(dataSegments[4]);
            tMatrix.m32 = m32.floatValue() * 1000f;
            // First row
            BigDecimal m00 = new BigDecimal(dataSegments[5]);
            tMatrix.m00 = m00.floatValue();
            BigDecimal m10 = new BigDecimal(dataSegments[6]);
            tMatrix.m10 = m10.floatValue();
            BigDecimal m20 = new BigDecimal(dataSegments[7]);
            tMatrix.m20 = m20.floatValue();
            // Second row
            BigDecimal m01 = new BigDecimal(dataSegments[8]);
            tMatrix.m01 = m01.floatValue();
            BigDecimal m11 = new BigDecimal(dataSegments[9]);
            tMatrix.m11 = m11.floatValue();
            BigDecimal m21 = new BigDecimal(dataSegments[10]);
            tMatrix.m21 = m21.floatValue();
            // Third row
            BigDecimal m02 = new BigDecimal(dataSegments[11]);
            tMatrix.m02 = m02.floatValue();
            BigDecimal m12 = new BigDecimal(dataSegments[12]);
            tMatrix.m12 = m12.floatValue();
            BigDecimal m22 = new BigDecimal(dataSegments[13]);
            tMatrix.m22 = m22.floatValue();
        } catch (NumberFormatException nfe) {
            // Can't happen
            return false;
        }
        tMatrix.m33 = 1f;
        det = tMatrix.determinant();
        return Math.abs(det) >= Threshold.SINGULARITY_DETERMINANT;
    }

    private Axis determineAxis(BigDecimal x, BigDecimal y, BigDecimal z) {
        if (BigDecimal.ZERO.compareTo(y) == 0 && BigDecimal.ZERO.compareTo(z) == 0) return Axis.X;
        if (BigDecimal.ZERO.compareTo(x) == 0 && BigDecimal.ZERO.compareTo(z) == 0) return Axis.Y;
        if (BigDecimal.ZERO.compareTo(y) == 0 && BigDecimal.ZERO.compareTo(x) == 0) return Axis.Z;
        return Axis.NONE;
    }
}
