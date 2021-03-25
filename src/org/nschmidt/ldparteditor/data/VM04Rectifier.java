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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.lwjgl.util.vector.Matrix4f;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.LDPartEditorException;
import org.nschmidt.ldparteditor.helpers.composite3d.RectifierSettings;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
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

        final BigDecimal TWO = new BigDecimal(2);

        final Set<GData3> trisToIgnore = new HashSet<>();

        final Set<GData2> linesToDelete = new HashSet<>();
        final Set<GData4> quadsToDelete = new HashSet<>();
        final Set<GData5> clinesToDelete = new HashSet<>();

        final Set<GData> surfsToParse = new HashSet<>();

        if (rs.getScope() == 0) {
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
        final GColour col16 = View.getLDConfigColour(16);

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
                            monitor.beginTask(I18n.VM_Rectify, IProgressMonitor.UNKNOWN);
                            for (GData g : surfsToParse) {
                                /* Check if the monitor has been canceled */
                                if (monitor.isCanceled()) break;
                                if (trisToIgnore.contains(g)) continue;
                                GData quad = null;
                                if (g.type() == 3 && replaceQuads) {

                                    GData3 tri = (GData3) g;
                                    Vertex[] v = triangles.get(tri);

                                    @SuppressWarnings("unchecked")
                                    ArrayList<GData>[] cf = new ArrayList[3];

                                    cf[0] = linkedCommonFaces(v[0], v[1]);
                                    cf[1] = linkedCommonFaces(v[0], v[2]);
                                    cf[2] = linkedCommonFaces(v[1], v[2]);

                                    int bestIndex = -1;
                                    BigDecimal bestRatio = null;
                                    for(int i = 0; i < 3; i++) {
                                        if (cf[i].size() == 2 && cf[i].get(0).type() == 3 && cf[i].get(1).type() == 3) {
                                            GData3 tri1 = (GData3) cf[i].get(0);
                                            GData3 tri2 = (GData3) cf[i].get(1);
                                            if (tri1.parent.equals(View.DUMMY_REFERENCE) && tri2.parent.equals(View.DUMMY_REFERENCE) && tri1.colourNumber == tri2.colourNumber && (tri1.colourNumber != -1 || tri1.r == tri2.r && tri1.g == tri2.g && tri1.b == tri2.b && tri1.a == tri2.a)) {

                                                TreeSet<Vertex> tri1V = new TreeSet<>();
                                                TreeSet<Vertex> tri2V = new TreeSet<>();
                                                TreeSet<Vertex> triC = new TreeSet<>();

                                                Vertex[] v1 = triangles.get(tri1);
                                                Vertex[] v2 = triangles.get(tri2);

                                                for (Vertex ve : v1) {
                                                    tri1V.add(ve);
                                                }
                                                for (Vertex ve : v2) {
                                                    tri2V.add(ve);
                                                }
                                                triC.addAll(tri1V);
                                                triC.retainAll(tri2V);
                                                tri2V.removeAll(tri1V);

                                                tri1V.removeAll(triC);
                                                tri1V.removeAll(tri2V);

                                                if (tri2V.iterator().hasNext() && tri1V.iterator().hasNext() && triC.size() == 2) {

                                                    Vector3d n1 = new Vector3d(
                                                            tri1.Y3.subtract(tri1.Y1).multiply(tri1.Z2.subtract(tri1.Z1)).subtract(tri1.Z3.subtract(tri1.Z1).multiply(tri1.Y2.subtract(tri1.Y1))),
                                                            tri1.Z3.subtract(tri1.Z1).multiply(tri1.X2.subtract(tri1.X1)).subtract(tri1.X3.subtract(tri1.X1).multiply(tri1.Z2.subtract(tri1.Z1))),
                                                            tri1.X3.subtract(tri1.X1).multiply(tri1.Y2.subtract(tri1.Y1)).subtract(tri1.Y3.subtract(tri1.Y1).multiply(tri1.X2.subtract(tri1.X1)))
                                                            );
                                                    Vector3d n2 = new Vector3d(
                                                            tri2.Y3.subtract(tri2.Y1).multiply(tri2.Z2.subtract(tri2.Z1)).subtract(tri2.Z3.subtract(tri2.Z1).multiply(tri2.Y2.subtract(tri2.Y1))),
                                                            tri2.Z3.subtract(tri2.Z1).multiply(tri2.X2.subtract(tri2.X1)).subtract(tri2.X3.subtract(tri2.X1).multiply(tri2.Z2.subtract(tri2.Z1))),
                                                            tri2.X3.subtract(tri2.X1).multiply(tri2.Y2.subtract(tri2.Y1)).subtract(tri2.Y3.subtract(tri2.Y1).multiply(tri2.X2.subtract(tri2.X1)))
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

                                                        if (cnc == 1 || cnc == 3 || !linkedDatFile.getDrawPerLine_NOCLONE().containsValue(tri2)) {
                                                            // Concave
                                                            continue;
                                                        }

                                                        angle = Vector3d.angle(normals[0], normals[2]);
                                                        if (angle > targetAngle) continue;

                                                        Vector3d A = Vector3d.sub(vertexB, vertexA);
                                                        Vector3d B = Vector3d.sub(vertexB, vertexC);
                                                        Vector3d C = Vector3d.sub(vertexD, vertexC);
                                                        Vector3d D = Vector3d.sub(vertexD, vertexA);

                                                        angle = Vector3d.angle(A, D);
                                                        double sumAngle = angle;
                                                        if (angle < Threshold.collinear_angle_minimum || angle > Threshold.collinear_angle_maximum) {
                                                            continue;
                                                        }

                                                        angle = Vector3d.angle(B, C);
                                                        sumAngle = sumAngle + angle;
                                                        if (angle < Threshold.collinear_angle_minimum || angle > Threshold.collinear_angle_maximum) {
                                                            continue;
                                                        }

                                                        A.negate();
                                                        B.negate();
                                                        angle = Vector3d.angle(A, B);
                                                        sumAngle = sumAngle + angle;
                                                        if (angle < Threshold.collinear_angle_minimum || angle > Threshold.collinear_angle_maximum) {
                                                            continue;
                                                        }

                                                        angle = 360.0 - sumAngle;
                                                        if (angle < Threshold.collinear_angle_minimum || angle > Threshold.collinear_angle_maximum) {
                                                            continue;
                                                        }

                                                        BigDecimal m1 = Vector3d.distSquare(new Vector3d(first), new Vector3d(third)).add(BigDecimal.ONE);
                                                        BigDecimal m2 = Vector3d.distSquare(new Vector3d(second), new Vector3d(fourth)).add(BigDecimal.ONE);
                                                        BigDecimal ratio = m1.compareTo(m2) > 0 ? m1.divide(m2, Threshold.mc) : m2.divide(m1, Threshold.mc);
                                                        if (bestIndex == -1) {
                                                            bestRatio = ratio;
                                                            bestIndex = i;
                                                        } else if (ratio.compareTo(bestRatio) < 0) {
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

                                        TreeSet<Vertex> tri1V = new TreeSet<>();
                                        TreeSet<Vertex> tri2V = new TreeSet<>();
                                        TreeSet<Vertex> triC = new TreeSet<>();

                                        Vertex[] v1 = triangles.get(tri1);
                                        Vertex[] v2 = triangles.get(tri2);

                                        for (Vertex ve : v1) {
                                            tri1V.add(ve);
                                        }
                                        for (Vertex ve : v2) {
                                            tri2V.add(ve);
                                        }
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
                                                    GData gd = m.getGdata();
                                                    int t = gd.type();
                                                    if (lineLinkedToVertices.containsKey(gd) && m.getPosition() < 2 && (t == 2 || t == 5)) lines1.add(gd);
                                                }
                                            }
                                            {
                                                Set<VertexManifestation> s2 = vertexLinkedToPositionInFile.get(second);
                                                for (VertexManifestation m : s2) {
                                                    GData gd = m.getGdata();
                                                    int t = gd.type();
                                                    if (lineLinkedToVertices.containsKey(gd) && m.getPosition() < 2 && (t == 2 || t == 5)) lines2.add(gd);
                                                }
                                            }
                                            {
                                                Set<VertexManifestation> s3 = vertexLinkedToPositionInFile.get(third);
                                                for (VertexManifestation m : s3) {
                                                    GData gd = m.getGdata();
                                                    int t = gd.type();
                                                    if (lineLinkedToVertices.containsKey(gd) && m.getPosition() < 2 && (t == 2 || t == 5)) lines3.add(gd);
                                                }
                                            }
                                            {
                                                Set<VertexManifestation> s4 = vertexLinkedToPositionInFile.get(fourth);
                                                for (VertexManifestation m : s4) {
                                                    GData gd = m.getGdata();
                                                    int t = gd.type();
                                                    if (lineLinkedToVertices.containsKey(gd) && m.getPosition() < 2 &&  (t == 2 || t == 5)) lines4.add(gd);
                                                }
                                            }

                                            if (colourise) {
                                                GColour yellow = View.hasLDConfigColour(14) ? View.getLDConfigColour(14) : new GColour(-1, 1f, 1f, 0f, 1f);

                                                quad = new GData4(yellow.getColourNumber(), yellow.getR(), yellow.getG(), yellow.getB(), yellow.getA(), first, second, third, fourth, View.DUMMY_REFERENCE, linkedDatFile);

                                            } else {

                                                quad = new GData4(tri1.colourNumber, tri1.r, tri1.g, tri1.b, tri1.a, first, second, third, fourth, View.DUMMY_REFERENCE, linkedDatFile);

                                            }

                                            linkedDatFile.insertAfter(tri2, quad);
                                            setModified_NoSync();
                                            result2[0] += 2;

                                            lines1.retainAll(lines3);
                                            lines2.retainAll(lines4);
                                            lines1.addAll(lines2);
                                            for (GData gData : lines1) {
                                                int t = gData.type();
                                                switch (t) {
                                                case 5:
                                                    clinesToDelete.add((GData5) gData);
                                                    result2[1] += 1;
                                                    break;
                                                default:
                                                    break;
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

                                    BigDecimal d1X = qa.X1.add(qa.X3).divide(TWO);
                                    BigDecimal d2X = qa.X2.add(qa.X4).divide(TWO);
                                    if (d1X.compareTo(d2X) == 0) {
                                        BigDecimal d1Y = qa.Y1.add(qa.Y3).divide(TWO);
                                        BigDecimal d2Y = qa.Y2.add(qa.Y4).divide(TWO);
                                        if (d1Y.compareTo(d2Y) == 0) {
                                            BigDecimal d1Z = qa.Z1.add(qa.Z3).divide(TWO);
                                            BigDecimal d2Z = qa.Z2.add(qa.Z4).divide(TWO);
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
                                                    vq = ROTQUAD(vq);
                                                    vq = ROTQUAD(vq);
                                                    vq = ROTQUAD(vq);
                                                    shortName = "rect1.dat"; //$NON-NLS-1$
                                                    break;
                                                case 2:
                                                    shortName = "rect1.dat"; //$NON-NLS-1$
                                                    break;
                                                case 4:
                                                    vq = ROTQUAD(vq);
                                                    shortName = "rect1.dat"; //$NON-NLS-1$
                                                    break;
                                                case 8:
                                                    vq = ROTQUAD(vq);
                                                    vq = ROTQUAD(vq);
                                                    shortName = "rect1.dat"; //$NON-NLS-1$
                                                    break;
                                                case 3:
                                                    vq = ROTQUAD(vq);
                                                    vq = ROTQUAD(vq);
                                                    vq = ROTQUAD(vq);
                                                    shortName = "rect2a.dat"; //$NON-NLS-1$
                                                    break;
                                                case 6:
                                                    shortName = "rect2a.dat"; //$NON-NLS-1$
                                                    break;
                                                case 12:
                                                    vq = ROTQUAD(vq);
                                                    shortName = "rect2a.dat"; //$NON-NLS-1$
                                                    break;
                                                case 9:
                                                    vq = ROTQUAD(vq);
                                                    vq = ROTQUAD(vq);
                                                    shortName = "rect2a.dat"; //$NON-NLS-1$
                                                    break;
                                                case 5:
                                                    shortName = "rect2p.dat"; //$NON-NLS-1$
                                                    break;
                                                case 10:
                                                    vq = ROTQUAD(vq);
                                                    shortName = "rect2p.dat"; //$NON-NLS-1$
                                                    break;
                                                case 7:
                                                    vq = ROTQUAD(vq);
                                                    vq = ROTQUAD(vq);
                                                    vq = ROTQUAD(vq);
                                                    shortName = "rect3.dat"; //$NON-NLS-1$
                                                    break;
                                                case 14:
                                                    shortName = "rect3.dat"; //$NON-NLS-1$
                                                    break;
                                                case 13:
                                                    vq = ROTQUAD(vq);
                                                    shortName = "rect3.dat"; //$NON-NLS-1$
                                                    break;
                                                case 11:
                                                    vq = ROTQUAD(vq);
                                                    vq = ROTQUAD(vq);
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
                                                        temp1.X.divide(TWO), temp1.Y.divide(TWO), temp1.Z.divide(TWO), BigDecimal.ZERO,
                                                        temp3.X.divide(TWO), temp3.Y.divide(TWO), temp3.Z.divide(TWO), BigDecimal.ZERO,
                                                        temp2.X.divide(TWO), temp2.Y.divide(TWO), temp2.Z.divide(TWO), BigDecimal.ZERO,
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

                                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M30));
                                                lineBuilder.append(" "); //$NON-NLS-1$
                                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M31));
                                                lineBuilder.append(" "); //$NON-NLS-1$
                                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M32));
                                                lineBuilder.append(" "); //$NON-NLS-1$
                                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M00));
                                                lineBuilder.append(" "); //$NON-NLS-1$
                                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M10));
                                                lineBuilder.append(" "); //$NON-NLS-1$
                                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M20));
                                                lineBuilder.append(" "); //$NON-NLS-1$
                                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M01));
                                                lineBuilder.append(" "); //$NON-NLS-1$
                                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M11));
                                                lineBuilder.append(" "); //$NON-NLS-1$
                                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M21));
                                                lineBuilder.append(" "); //$NON-NLS-1$
                                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M02));
                                                lineBuilder.append(" "); //$NON-NLS-1$
                                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M12));
                                                lineBuilder.append(" "); //$NON-NLS-1$
                                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M22));
                                                lineBuilder.append(" "); //$NON-NLS-1$
                                                lineBuilder.append(shortName);

                                                Set<String> alreadyParsed = new HashSet<>();
                                                alreadyParsed.add(linkedDatFile.getShortName());
                                                ArrayList<ParsingResult> result = DatParser.parseLine(lineBuilder.toString(), -1, 0, col16.getR(), col16.getG(), col16.getB(), 1.0f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile, false, alreadyParsed);
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
            catch (InvocationTargetException consumed) {
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
                    ArrayList<GData>[] cf = new ArrayList[3];

                    cf[0] = linkedCommonFaces(v[0], v[1]);
                    cf[1] = linkedCommonFaces(v[0], v[2]);
                    cf[2] = linkedCommonFaces(v[1], v[2]);

                    int bestIndex = -1;
                    BigDecimal bestRatio = null;
                    for(int i = 0; i < 3; i++) {
                        if (cf[i].size() == 2 && cf[i].get(0).type() == 3 && cf[i].get(1).type() == 3) {
                            GData3 tri1 = (GData3) cf[i].get(0);
                            GData3 tri2 = (GData3) cf[i].get(1);
                            if (tri1.parent.equals(View.DUMMY_REFERENCE) && tri2.parent.equals(View.DUMMY_REFERENCE) && tri1.colourNumber == tri2.colourNumber && (tri1.colourNumber != -1 || tri1.r == tri2.r && tri1.g == tri2.g && tri1.b == tri2.b && tri1.a == tri2.a)) {

                                TreeSet<Vertex> tri1V = new TreeSet<>();
                                TreeSet<Vertex> tri2V = new TreeSet<>();
                                TreeSet<Vertex> triC = new TreeSet<>();

                                Vertex[] v1 = triangles.get(tri1);
                                Vertex[] v2 = triangles.get(tri2);

                                for (Vertex ve : v1) {
                                    tri1V.add(ve);
                                }
                                for (Vertex ve : v2) {
                                    tri2V.add(ve);
                                }
                                triC.addAll(tri1V);
                                triC.retainAll(tri2V);
                                tri2V.removeAll(tri1V);

                                tri1V.removeAll(triC);
                                tri1V.removeAll(tri2V);

                                if (tri2V.iterator().hasNext() && tri1V.iterator().hasNext() && triC.size() == 2) {

                                    Vector3d n1 = new Vector3d(
                                            tri1.Y3.subtract(tri1.Y1).multiply(tri1.Z2.subtract(tri1.Z1)).subtract(tri1.Z3.subtract(tri1.Z1).multiply(tri1.Y2.subtract(tri1.Y1))),
                                            tri1.Z3.subtract(tri1.Z1).multiply(tri1.X2.subtract(tri1.X1)).subtract(tri1.X3.subtract(tri1.X1).multiply(tri1.Z2.subtract(tri1.Z1))),
                                            tri1.X3.subtract(tri1.X1).multiply(tri1.Y2.subtract(tri1.Y1)).subtract(tri1.Y3.subtract(tri1.Y1).multiply(tri1.X2.subtract(tri1.X1)))
                                            );
                                    Vector3d n2 = new Vector3d(
                                            tri2.Y3.subtract(tri2.Y1).multiply(tri2.Z2.subtract(tri2.Z1)).subtract(tri2.Z3.subtract(tri2.Z1).multiply(tri2.Y2.subtract(tri2.Y1))),
                                            tri2.Z3.subtract(tri2.Z1).multiply(tri2.X2.subtract(tri2.X1)).subtract(tri2.X3.subtract(tri2.X1).multiply(tri2.Z2.subtract(tri2.Z1))),
                                            tri2.X3.subtract(tri2.X1).multiply(tri2.Y2.subtract(tri2.Y1)).subtract(tri2.Y3.subtract(tri2.Y1).multiply(tri2.X2.subtract(tri2.X1)))
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

                                        if (cnc == 1 || cnc == 3 || !linkedDatFile.getDrawPerLine_NOCLONE().containsValue(tri2)) {
                                            // Concave
                                            continue;
                                        }

                                        angle = Vector3d.angle(normals[0], normals[2]);
                                        if (angle > targetAngle) continue;

                                        Vector3d A = Vector3d.sub(vertexB, vertexA);
                                        Vector3d B = Vector3d.sub(vertexB, vertexC);
                                        Vector3d C = Vector3d.sub(vertexD, vertexC);
                                        Vector3d D = Vector3d.sub(vertexD, vertexA);

                                        angle = Vector3d.angle(A, D);
                                        double sumAngle = angle;
                                        if (angle < Threshold.collinear_angle_minimum || angle > Threshold.collinear_angle_maximum) {
                                            continue;
                                        }

                                        angle = Vector3d.angle(B, C);
                                        sumAngle = sumAngle + angle;
                                        if (angle < Threshold.collinear_angle_minimum || angle > Threshold.collinear_angle_maximum) {
                                            continue;
                                        }

                                        A.negate();
                                        B.negate();
                                        angle = Vector3d.angle(A, B);
                                        sumAngle = sumAngle + angle;
                                        if (angle < Threshold.collinear_angle_minimum || angle > Threshold.collinear_angle_maximum) {
                                            continue;
                                        }

                                        angle = 360.0 - sumAngle;
                                        if (angle < Threshold.collinear_angle_minimum || angle > Threshold.collinear_angle_maximum) {
                                            continue;
                                        }

                                        BigDecimal m1 = Vector3d.distSquare(new Vector3d(first), new Vector3d(third)).add(BigDecimal.ONE);
                                        BigDecimal m2 = Vector3d.distSquare(new Vector3d(second), new Vector3d(fourth)).add(BigDecimal.ONE);
                                        BigDecimal ratio = m1.compareTo(m2) > 0 ? m1.divide(m2, Threshold.mc) : m2.divide(m1, Threshold.mc);
                                        if (bestIndex == -1) {
                                            bestRatio = ratio;
                                            bestIndex = i;
                                        } else if (ratio.compareTo(bestRatio) < 0) {
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

                        TreeSet<Vertex> tri1V = new TreeSet<>();
                        TreeSet<Vertex> tri2V = new TreeSet<>();
                        TreeSet<Vertex> triC = new TreeSet<>();

                        Vertex[] v1 = triangles.get(tri1);
                        Vertex[] v2 = triangles.get(tri2);

                        for (Vertex ve : v1) {
                            tri1V.add(ve);
                        }
                        for (Vertex ve : v2) {
                            tri2V.add(ve);
                        }
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
                                    GData gd = m.getGdata();
                                    int t = gd.type();
                                    if (lineLinkedToVertices.containsKey(gd) && m.getPosition() < 2 && (t == 2 || t == 5)) lines1.add(gd);
                                }
                            }
                            {
                                Set<VertexManifestation> s2 = vertexLinkedToPositionInFile.get(second);
                                for (VertexManifestation m : s2) {
                                    GData gd = m.getGdata();
                                    int t = gd.type();
                                    if (lineLinkedToVertices.containsKey(gd) && m.getPosition() < 2 && (t == 2 || t == 5)) lines2.add(gd);
                                }
                            }
                            {
                                Set<VertexManifestation> s3 = vertexLinkedToPositionInFile.get(third);
                                for (VertexManifestation m : s3) {
                                    GData gd = m.getGdata();
                                    int t = gd.type();
                                    if (lineLinkedToVertices.containsKey(gd) && m.getPosition() < 2 && (t == 2 || t == 5)) lines3.add(gd);
                                }
                            }
                            {
                                Set<VertexManifestation> s4 = vertexLinkedToPositionInFile.get(fourth);
                                for (VertexManifestation m : s4) {
                                    GData gd = m.getGdata();
                                    int t = gd.type();
                                    if (lineLinkedToVertices.containsKey(gd) && m.getPosition() < 2 &&  (t == 2 || t == 5)) lines4.add(gd);
                                }
                            }

                            if (colourise) {
                                GColour yellow = View.hasLDConfigColour(14) ? View.getLDConfigColour(14) : new GColour(-1, 1f, 1f, 0f, 1f);

                                quad = new GData4(yellow.getColourNumber(), yellow.getR(), yellow.getG(), yellow.getB(), yellow.getA(), first, second, third, fourth, View.DUMMY_REFERENCE, linkedDatFile);

                                linkedDatFile.insertAfter(tri2, quad);
                                setModified_NoSync();
                            } else {

                                quad = new GData4(tri1.colourNumber, tri1.r, tri1.g, tri1.b, tri1.a, first, second, third, fourth, View.DUMMY_REFERENCE, linkedDatFile);

                                linkedDatFile.insertAfter(tri2, quad);
                                setModified_NoSync();
                            }



                            lines1.retainAll(lines3);
                            lines2.retainAll(lines4);
                            lines1.addAll(lines2);
                            for (GData gData : lines1) {
                                int t = gData.type();
                                switch (t) {
                                case 5:
                                    clinesToDelete.add((GData5) gData);
                                    break;
                                default:
                                    break;
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

                    BigDecimal d1X = qa.X1.add(qa.X3).divide(TWO);
                    BigDecimal d2X = qa.X2.add(qa.X4).divide(TWO);
                    if (d1X.compareTo(d2X) == 0) {
                        BigDecimal d1Y = qa.Y1.add(qa.Y3).divide(TWO);
                        BigDecimal d2Y = qa.Y2.add(qa.Y4).divide(TWO);
                        if (d1Y.compareTo(d2Y) == 0) {
                            BigDecimal d1Z = qa.Z1.add(qa.Z3).divide(TWO);
                            BigDecimal d2Z = qa.Z2.add(qa.Z4).divide(TWO);
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
                                    vq = ROTQUAD(vq);
                                    vq = ROTQUAD(vq);
                                    vq = ROTQUAD(vq);
                                    shortName = "rect1.dat"; //$NON-NLS-1$
                                    break;
                                case 2:
                                    shortName = "rect1.dat"; //$NON-NLS-1$
                                    break;
                                case 4:
                                    vq = ROTQUAD(vq);
                                    shortName = "rect1.dat"; //$NON-NLS-1$
                                    break;
                                case 8:
                                    vq = ROTQUAD(vq);
                                    vq = ROTQUAD(vq);
                                    shortName = "rect1.dat"; //$NON-NLS-1$
                                    break;
                                case 3:
                                    vq = ROTQUAD(vq);
                                    vq = ROTQUAD(vq);
                                    vq = ROTQUAD(vq);
                                    shortName = "rect2a.dat"; //$NON-NLS-1$
                                    break;
                                case 6:
                                    shortName = "rect2a.dat"; //$NON-NLS-1$
                                    break;
                                case 12:
                                    vq = ROTQUAD(vq);
                                    shortName = "rect2a.dat"; //$NON-NLS-1$
                                    break;
                                case 9:
                                    vq = ROTQUAD(vq);
                                    vq = ROTQUAD(vq);
                                    shortName = "rect2a.dat"; //$NON-NLS-1$
                                    break;
                                case 5:
                                    shortName = "rect2p.dat"; //$NON-NLS-1$
                                    break;
                                case 10:
                                    vq = ROTQUAD(vq);
                                    shortName = "rect2p.dat"; //$NON-NLS-1$
                                    break;
                                case 7:
                                    vq = ROTQUAD(vq);
                                    vq = ROTQUAD(vq);
                                    vq = ROTQUAD(vq);
                                    shortName = "rect3.dat"; //$NON-NLS-1$
                                    break;
                                case 14:
                                    shortName = "rect3.dat"; //$NON-NLS-1$
                                    break;
                                case 13:
                                    vq = ROTQUAD(vq);
                                    shortName = "rect3.dat"; //$NON-NLS-1$
                                    break;
                                case 11:
                                    vq = ROTQUAD(vq);
                                    vq = ROTQUAD(vq);
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
                                        temp1.X.divide(TWO), temp1.Y.divide(TWO), temp1.Z.divide(TWO), BigDecimal.ZERO,
                                        temp3.X.divide(TWO), temp3.Y.divide(TWO), temp3.Z.divide(TWO), BigDecimal.ZERO,
                                        temp2.X.divide(TWO), temp2.Y.divide(TWO), temp2.Z.divide(TWO), BigDecimal.ZERO,
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

                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M30));
                                lineBuilder.append(" "); //$NON-NLS-1$
                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M31));
                                lineBuilder.append(" "); //$NON-NLS-1$
                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M32));
                                lineBuilder.append(" "); //$NON-NLS-1$
                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M00));
                                lineBuilder.append(" "); //$NON-NLS-1$
                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M10));
                                lineBuilder.append(" "); //$NON-NLS-1$
                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M20));
                                lineBuilder.append(" "); //$NON-NLS-1$
                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M01));
                                lineBuilder.append(" "); //$NON-NLS-1$
                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M11));
                                lineBuilder.append(" "); //$NON-NLS-1$
                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M21));
                                lineBuilder.append(" "); //$NON-NLS-1$
                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M02));
                                lineBuilder.append(" "); //$NON-NLS-1$
                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M12));
                                lineBuilder.append(" "); //$NON-NLS-1$
                                lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M22));
                                lineBuilder.append(" "); //$NON-NLS-1$
                                lineBuilder.append(shortName);

                                Set<String> alreadyParsed = new HashSet<>();
                                alreadyParsed.add(linkedDatFile.getShortName());
                                ArrayList<ParsingResult> result = DatParser.parseLine(lineBuilder.toString(), -1, 0, col16.getR(), col16.getG(), col16.getB(), 1.0f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile, false, alreadyParsed);
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

    private Vertex[] ROTQUAD(Vertex[] vq) {
        Vertex[] result = new Vertex[4];
        result[0] = vq[1];
        result[1] = vq[2];
        result[2] = vq[3];
        result[3] = vq[0];
        return result;
    }

    private Matrix repair(Matrix m) {
        final BigDecimal lengthY =  MathHelper.sqrt(m.M10.multiply(m.M10).add(m.M11.multiply(m.M11)).add(m.M12.multiply(m.M12))).subtract(BigDecimal.ONE).abs();
        final BigDecimal epsilon = new BigDecimal("0.000001"); //$NON-NLS-1$
        if (epsilon.compareTo(lengthY) < 0) {
            String line = "1 16 " + m.toLDrawString() + "1.dat"; //$NON-NLS-1$ //$NON-NLS-2$
            String[] data_segments = line.trim().split("\\s+"); //$NON-NLS-1$
            StringBuilder sb = new StringBuilder();
            for (int k = 0; k < 3; k++) {
                sb.setLength(0);
                int i = 0;
                String sign;
                for (String seg : data_segments) {
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
            data_segments = line.trim().split("\\s+"); //$NON-NLS-1$
            // [ERROR] Check singularity
            try {
                // Offset
                BigDecimal M30 = new BigDecimal(data_segments[2]);
                BigDecimal M31 = new BigDecimal(data_segments[3]);
                BigDecimal M32 = new BigDecimal(data_segments[4]);
                // First row
                BigDecimal M00 = new BigDecimal(data_segments[5]);
                BigDecimal M10 = new BigDecimal(data_segments[6]);
                BigDecimal M20 = new BigDecimal(data_segments[7]);
                // Second row
                BigDecimal M01 = new BigDecimal(data_segments[8]);
                BigDecimal M11 = new BigDecimal(data_segments[9]);
                BigDecimal M21 = new BigDecimal(data_segments[10]);
                // Third row
                BigDecimal M02 = new BigDecimal(data_segments[11]);
                BigDecimal M12 = new BigDecimal(data_segments[12]);
                BigDecimal M22 = new BigDecimal(data_segments[13]);
                return new Matrix(M00, M01, M02, M02, M10, M11, M12, M12, M20, M21, M22, M22, M30, M31, M32, M32);
            } catch (NumberFormatException nfe) {
            }
        }
        return m;
    }

    private boolean hasGoodDeterminant(String line) {
        String[] data_segments = line.trim().split("\\s+"); //$NON-NLS-1$
        // [ERROR] Check singularity
        Matrix4f tMatrix = new Matrix4f();
        float det = 0;
        try {
            // Offset
            BigDecimal M30 = new BigDecimal(data_segments[2]);
            tMatrix.m30 = M30.floatValue() * 1000f;
            BigDecimal M31 = new BigDecimal(data_segments[3]);
            tMatrix.m31 = M31.floatValue() * 1000f;
            BigDecimal M32 = new BigDecimal(data_segments[4]);
            tMatrix.m32 = M32.floatValue() * 1000f;
            // First row
            BigDecimal M00 = new BigDecimal(data_segments[5]);
            tMatrix.m00 = M00.floatValue();
            BigDecimal M10 = new BigDecimal(data_segments[6]);
            tMatrix.m10 = M10.floatValue();
            BigDecimal M20 = new BigDecimal(data_segments[7]);
            tMatrix.m20 = M20.floatValue();
            // Second row
            BigDecimal M01 = new BigDecimal(data_segments[8]);
            tMatrix.m01 = M01.floatValue();
            BigDecimal M11 = new BigDecimal(data_segments[9]);
            tMatrix.m11 = M11.floatValue();
            BigDecimal M21 = new BigDecimal(data_segments[10]);
            tMatrix.m21 = M21.floatValue();
            // Third row
            BigDecimal M02 = new BigDecimal(data_segments[11]);
            tMatrix.m02 = M02.floatValue();
            BigDecimal M12 = new BigDecimal(data_segments[12]);
            tMatrix.m12 = M12.floatValue();
            BigDecimal M22 = new BigDecimal(data_segments[13]);
            tMatrix.m22 = M22.floatValue();
        } catch (NumberFormatException nfe) {
            // Can't happen
            return false;
        }
        tMatrix.m33 = 1f;
        det = tMatrix.determinant();
        return Math.abs(det) >= Threshold.singularity_determinant;
    }
}
