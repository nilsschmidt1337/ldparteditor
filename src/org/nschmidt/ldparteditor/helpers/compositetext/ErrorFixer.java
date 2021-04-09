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
package org.nschmidt.ldparteditor.helpers.compositetext;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.HashSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.Matrix;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.text.DatParser;

/**
 * @author nils
 *
 */
final class ErrorFixer {

    public static String fix(int lineNumber, String sort, String line, String text, DatFile datFile, Shell tWinShell) {
        int s = Integer.parseInt(sort, 16);
        final GColour col16 = View.getLDConfigColour(16);
        switch (s) {
        case 1: // Duplicated line
            text = QuickFixer.setLine(lineNumber + 1, "<rm>", text); //$NON-NLS-1$
            break;
        case 10: // INVERTNEXT (Flat subfile)
        case 11:
        case 12:
        {
            text = QuickFixer.setLine(lineNumber + 1, "<rm>", text); //$NON-NLS-1$
            GData1 subfileToFlip = null;
            boolean validState = false;
            GData g = datFile.getDrawPerLine_NOCLONE().getValue(lineNumber + 1).getNext();
            while (g != null && g.type() < 2) {
                lineNumber++;
                if (g.type() == 1) {
                    validState = true;
                    break;
                } else if (!g.toString().trim().isEmpty()) {
                    break;
                }
                g = g.getNext();
            }
            if (validState) {
                subfileToFlip = (GData1) g;
                Matrix m = null;
                final Matrix t = subfileToFlip.getAccurateProductMatrix();
                GData1 untransformedSubfile;
                StringBuilder colourBuilder = new StringBuilder();
                if (subfileToFlip.colourNumber == -1) {
                    colourBuilder.append("0x2"); //$NON-NLS-1$
                    colourBuilder.append(MathHelper.toHex((int) (255f * subfileToFlip.r)).toUpperCase());
                    colourBuilder.append(MathHelper.toHex((int) (255f * subfileToFlip.g)).toUpperCase());
                    colourBuilder.append(MathHelper.toHex((int) (255f * subfileToFlip.b)).toUpperCase());
                } else {
                    colourBuilder.append(subfileToFlip.colourNumber);
                }
                untransformedSubfile = (GData1) DatParser
                        .parseLine("1 " + colourBuilder.toString() + " 0 0 0 1 0 0 0 1 0 0 0 1 " + subfileToFlip.getShortName() , 0, 0, col16.getR(), col16.getG(), col16.getB(), 1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, datFile, false, //$NON-NLS-1$ //$NON-NLS-2$
                                new HashSet<>()).get(0).getGraphicalData();
                if (untransformedSubfile == null) break;

                switch (s) {
                case 10: // INVERTNEXT on X (Flat subfile)
                    m = new Matrix(
                            t.m00.negate(), t.m01.negate(), t.m02.negate(), t.m03,
                            t.m10, t.m11, t.m12, t.m13,
                            t.m20, t.m21, t.m22, t.m23,
                            t.m30, t.m31, t.m32, t.m33);
                    break;
                case 11: // INVERTNEXT on Y (Flat subfile)
                    m = new Matrix(
                            t.m00, t.m01, t.m02, t.m03,
                            t.m10.negate(), t.m11.negate(), t.m12.negate(), t.m13,
                            t.m20, t.m21, t.m22, t.m23,
                            t.m30, t.m31, t.m32, t.m33);
                    break;
                case 12: // INVERTNEXT on Z (Flat subfile)
                    m = new Matrix(
                            t.m00, t.m01, t.m02, t.m03,
                            t.m10, t.m11, t.m12, t.m13,
                            t.m20.negate(), t.m21.negate(), t.m22.negate(), t.m23,
                            t.m30, t.m31, t.m32, t.m33);
                    break;
                default:
                    break;
                }

                GData1 newSubfile = (GData1) DatParser
                        .parseLine(untransformedSubfile.getTransformedString(m, null, datFile, false) , datFile.getDrawPerLine_NOCLONE().getKey(subfileToFlip).intValue(), 0, col16.getR(), col16.getG(), col16.getB(), 1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, datFile, false,
                                new HashSet<>()).get(0).getGraphicalData();
                if (newSubfile == null) break;
                text = QuickFixer.setLine(lineNumber + 1, newSubfile.toString(), text);
                datFile.getVertexManager().remove(untransformedSubfile);
                datFile.getVertexManager().remove(newSubfile);
            }
        }
        break;
        case 2: // Singular Matrix
        {
            String[] dataSegments = line.trim().split(" "); //$NON-NLS-1$

            boolean m00 = false;
            boolean m01 = false;
            boolean m02 = false;
            boolean m10 = false;
            boolean m11 = false;
            boolean m12 = false;
            boolean m20 = false;
            boolean m21 = false;
            boolean m22 = false;

            {
                int i = 0;
                for (String seg : dataSegments) {
                    if (!seg.trim().equals("")) {  //$NON-NLS-1$
                        i++;
                        switch (i) {
                        case 6:
                            m00 = new BigDecimal(seg).equals(BigDecimal.ZERO);
                            break;
                        case 7:
                            m01 = new BigDecimal(seg).equals(BigDecimal.ZERO);
                            break;
                        case 8:
                            m02 = new BigDecimal(seg).equals(BigDecimal.ZERO);
                            break;
                        case 9:
                            m10 = new BigDecimal(seg).equals(BigDecimal.ZERO);
                            break;
                        case 10:
                            m11 = new BigDecimal(seg).equals(BigDecimal.ZERO);
                            break;
                        case 11:
                            m12 = new BigDecimal(seg).equals(BigDecimal.ZERO);
                            break;
                        case 12:
                            m20 = new BigDecimal(seg).equals(BigDecimal.ZERO);
                            break;
                        case 13:
                            m21 = new BigDecimal(seg).equals(BigDecimal.ZERO);
                            break;
                        case 14:
                            m22 = new BigDecimal(seg).equals(BigDecimal.ZERO);
                            break;
                        default:
                            break;
                        }
                    }
                }
            }
            {
                StringBuilder sb = new StringBuilder();
                int i = 0;
                for (String seg : dataSegments) {
                    if (!seg.trim().equals("")) {  //$NON-NLS-1$
                        i++;
                        switch (i) {
                        case 6:
                            if (m00 && (m01 && m02 || m10 && m20)) {
                                sb.append("1"); //$NON-NLS-1$
                            } else {
                                sb.append(seg);
                            }
                            break;
                        case 10:
                            if (m11 && (m10 && m12 || m01 && m21)) {
                                sb.append("1"); //$NON-NLS-1$
                            } else {
                                sb.append(seg);
                            }
                            break;
                        case 14:
                            if (m22 && (m20 && m21 || m02 && m12)) {
                                sb.append("1"); //$NON-NLS-1$
                            } else {
                                sb.append(seg);
                            }
                            break;
                        default:
                            sb.append(seg);
                            break;
                        }
                    }
                    sb.append(" "); //$NON-NLS-1$
                }
                text = QuickFixer.setLine(lineNumber + 1, sb.toString().trim(), text);
            }
        }
        break;
        case 4: // Concave quad
        {

            String[] dataSegments = line.trim().split("\\s+"); //$NON-NLS-1$
            text = QuickFixer.setLine(lineNumber + 1,
                    "3 " + dataSegments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                            dataSegments[2] + " " + dataSegments[3] + " " + dataSegments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            dataSegments[5] + " " + dataSegments[6] + " " + dataSegments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            dataSegments[8] + " " + dataSegments[9] + " " + dataSegments[10] +  //$NON-NLS-1$ //$NON-NLS-2$
                            "<br>3 " + dataSegments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                            dataSegments[8] + " " + dataSegments[9] + " " + dataSegments[10] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            dataSegments[11] + " " + dataSegments[12] + " " + dataSegments[13] + " " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            dataSegments[2] + " " + dataSegments[3] + " " + dataSegments[4] + //$NON-NLS-1$ //$NON-NLS-2$
                            "<br>5 24 " +  //$NON-NLS-1$
                            dataSegments[8] + " " + dataSegments[9] + " " + dataSegments[10] + " " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            dataSegments[2] + " " + dataSegments[3] + " " + dataSegments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            dataSegments[5] + " " + dataSegments[6] + " " + dataSegments[7] + " " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            dataSegments[11] + " " + dataSegments[12] + " " + dataSegments[13], text);  //$NON-NLS-1$ //$NON-NLS-2$
        }
        break;
        case 65: // Hourglass quad (first variant)
        {
            String[] dataSegments = line.trim().split("\\s+"); //$NON-NLS-1$
            text = QuickFixer.setLine(lineNumber + 1, dataSegments[0] + " " + dataSegments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                    dataSegments[5] + " " + dataSegments[6] + " " + dataSegments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    dataSegments[2] + " " + dataSegments[3] + " " + dataSegments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    dataSegments[8] + " " + dataSegments[9] + " " + dataSegments[10] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    dataSegments[11] + " " + dataSegments[12] + " " + dataSegments[13], text); //$NON-NLS-1$ //$NON-NLS-2$
        }
        break;
        case 66: // Hourglass quad (second variant)
        {
            String[] dataSegments = line.trim().split("\\s+"); //$NON-NLS-1$
            text = QuickFixer.setLine(lineNumber + 1, dataSegments[0] + " " + dataSegments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                    dataSegments[2] + " " + dataSegments[3] + " " + dataSegments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    dataSegments[8] + " " + dataSegments[9] + " " + dataSegments[10] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    dataSegments[5] + " " + dataSegments[6] + " " + dataSegments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    dataSegments[11] + " " + dataSegments[12] + " " + dataSegments[13], text); //$NON-NLS-1$ //$NON-NLS-2$
        }
        break;
        case 13: // Line identical vertices (line, triangle)
            text = QuickFixer.setLine(lineNumber + 1, "<rm>", text); //$NON-NLS-1$
            break;
        case 68: // Identical vertices (quad)
        {
            String[] dataSegments = line.trim().split("\\s+"); //$NON-NLS-1$

            final Vector3d vertexA = new Vector3d();
            final Vector3d vertexB = new Vector3d();
            final Vector3d vertexC = new Vector3d();
            final Vector3d vertexD = new Vector3d();

            // 1st vertex
            vertexA.setX(new BigDecimal(dataSegments[2], Threshold.MC));
            vertexA.setY(new BigDecimal(dataSegments[3], Threshold.MC));
            vertexA.setZ(new BigDecimal(dataSegments[4], Threshold.MC));
            // 2nd vertex
            vertexB.setX(new BigDecimal(dataSegments[5], Threshold.MC));
            vertexB.setY(new BigDecimal(dataSegments[6], Threshold.MC));
            vertexB.setZ(new BigDecimal(dataSegments[7], Threshold.MC));
            // 3rd vertex
            vertexC.setX(new BigDecimal(dataSegments[8], Threshold.MC));
            vertexC.setY(new BigDecimal(dataSegments[9], Threshold.MC));
            vertexC.setZ(new BigDecimal(dataSegments[10], Threshold.MC));
            // 4th vertex
            vertexD.setX(new BigDecimal(dataSegments[11], Threshold.MC));
            vertexD.setY(new BigDecimal(dataSegments[12], Threshold.MC));
            vertexD.setZ(new BigDecimal(dataSegments[13], Threshold.MC));


            Vector3d.sub(vertexA, vertexD, vertexA);
            Vector3d.sub(vertexB, vertexD, vertexB);
            Vector3d.sub(vertexC, vertexD, vertexC);

            boolean sAD = vertexA.length().compareTo(Threshold.IDENTICAL_VERTEX_DISTANCE) < 0; // AD
            boolean sBD = vertexB.length().compareTo(Threshold.IDENTICAL_VERTEX_DISTANCE) < 0; // BD
            boolean sCD = vertexC.length().compareTo(Threshold.IDENTICAL_VERTEX_DISTANCE) < 0; // CD
            boolean sAB = Vector3d.sub(vertexA, vertexB).length().compareTo(Threshold.IDENTICAL_VERTEX_DISTANCE) < 0; // AB
            boolean sBC = Vector3d.sub(vertexB, vertexC).length().compareTo(Threshold.IDENTICAL_VERTEX_DISTANCE) < 0; // BC
            boolean sAC = Vector3d.sub(vertexA, vertexC).length().compareTo(Threshold.IDENTICAL_VERTEX_DISTANCE) < 0; // AC

            if (sAD && !(sBD || sCD || sAB || sBC || sAC)) {
                text = QuickFixer.setLine(lineNumber + 1, "3 " + dataSegments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                        dataSegments[5] + " " + dataSegments[6] + " " + dataSegments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        dataSegments[8] + " " + dataSegments[9] + " " + dataSegments[10] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        dataSegments[11] + " " + dataSegments[12] + " " + dataSegments[13], text); //$NON-NLS-1$ //$NON-NLS-2$
            } else if (sCD && !(sBD || sAD || sAB || sBC || sAC)) {
                text = QuickFixer.setLine(lineNumber + 1, "3 " + dataSegments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                        dataSegments[2] + " " + dataSegments[3] + " " + dataSegments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        dataSegments[5] + " " + dataSegments[6] + " " + dataSegments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        dataSegments[11] + " " + dataSegments[12] + " " + dataSegments[13], text); //$NON-NLS-1$ //$NON-NLS-2$
            } else if (sBC && !(sBD || sCD || sAB || sAD || sAC)) {
                text = QuickFixer.setLine(lineNumber + 1, "3 " + dataSegments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                        dataSegments[2] + " " + dataSegments[3] + " " + dataSegments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        dataSegments[8] + " " + dataSegments[9] + " " + dataSegments[10] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        dataSegments[11] + " " + dataSegments[12] + " " + dataSegments[13], text); //$NON-NLS-1$ //$NON-NLS-2$
            } else if (sAB && !(sBD || sCD || sAD || sBC || sAC)) {
                text = QuickFixer.setLine(lineNumber + 1, "3 " + dataSegments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                        dataSegments[5] + " " + dataSegments[6] + " " + dataSegments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        dataSegments[8] + " " + dataSegments[9] + " " + dataSegments[10] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        dataSegments[11] + " " + dataSegments[12] + " " + dataSegments[13], text); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                text = QuickFixer.setLine(lineNumber + 1, "<rm>", text); //$NON-NLS-1$
            }
        }
        break;
        case 52: // Collinear quad
        {
            String[] dataSegments = line.trim().split("\\s+"); //$NON-NLS-1$

            final Vector3d vertexA = new Vector3d();
            final Vector3d vertexB = new Vector3d();
            final Vector3d vertexC = new Vector3d();
            final Vector3d vertexD = new Vector3d();

            // 1st vertex
            vertexA.setX(new BigDecimal(dataSegments[2], Threshold.MC));
            vertexA.setY(new BigDecimal(dataSegments[3], Threshold.MC));
            vertexA.setZ(new BigDecimal(dataSegments[4], Threshold.MC));
            // 2nd vertex
            vertexB.setX(new BigDecimal(dataSegments[5], Threshold.MC));
            vertexB.setY(new BigDecimal(dataSegments[6], Threshold.MC));
            vertexB.setZ(new BigDecimal(dataSegments[7], Threshold.MC));
            // 3rd vertex
            vertexC.setX(new BigDecimal(dataSegments[8], Threshold.MC));
            vertexC.setY(new BigDecimal(dataSegments[9], Threshold.MC));
            vertexC.setZ(new BigDecimal(dataSegments[10], Threshold.MC));
            // 4th vertex
            vertexD.setX(new BigDecimal(dataSegments[11], Threshold.MC));
            vertexD.setY(new BigDecimal(dataSegments[12], Threshold.MC));
            vertexD.setZ(new BigDecimal(dataSegments[13], Threshold.MC));

            Vertex vA = new Vertex(vertexA.x, vertexA.y, vertexA.z);
            Vertex vB = new Vertex(vertexB.x, vertexB.y, vertexB.z);
            Vertex vC = new Vertex(vertexC.x, vertexC.y, vertexC.z);
            Vertex vD = new Vertex(vertexD.x, vertexD.y, vertexD.z);

            Vector3d.sub(vertexB, vertexA, vertexA);
            Vector3d.sub(vertexB, vertexC, vertexB);
            Vector3d.sub(vertexD, vertexC, vertexC);
            Vector3d.sub(vertexD, new Vector3d(vA), vertexD);


            boolean pointA;
            boolean pointB;
            boolean pointC;
            boolean pointD;
            double angle = Vector3d.angle(vertexA, vertexD);
            double sumAngle = angle;
            pointA = angle < Threshold.COLLINEAR_ANGLE_MINIMUM || angle > Threshold.COLLINEAR_ANGLE_MAXIMUM;

            angle = Vector3d.angle(vertexB, vertexC);
            sumAngle = sumAngle + angle;
            pointC = angle < Threshold.COLLINEAR_ANGLE_MINIMUM || angle > Threshold.COLLINEAR_ANGLE_MAXIMUM;

            vertexA.negate();
            vertexB.negate();
            angle = Vector3d.angle(vertexA, vertexB);
            sumAngle = sumAngle + angle;
            pointB = angle < Threshold.COLLINEAR_ANGLE_MINIMUM || angle > Threshold.COLLINEAR_ANGLE_MAXIMUM;

            angle = 360.0 - sumAngle;
            pointD = angle < Threshold.COLLINEAR_ANGLE_MINIMUM || angle > Threshold.COLLINEAR_ANGLE_MAXIMUM;

            VertexManager vm = datFile.getVertexManager();

            if (pointA && (pointC || pointB || pointD) || pointC && (pointB || pointD) || pointB && pointD) {
                text = QuickFixer.setLine(lineNumber + 1, "<rm>", text); //$NON-NLS-1$
            } else if (pointA) {
                NLogger.debug(ErrorFixer.class, "Point A"); //$NON-NLS-1$
                if (vm.linkedCommonFaces(vD, vA).size() == 1 && vm.linkedCommonFaces(vA, vB).size() == 1) {
                    text = QuickFixer.setLine(lineNumber + 1, "3 " + dataSegments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                            dataSegments[5] + " " + dataSegments[6] + " " + dataSegments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            dataSegments[8] + " " + dataSegments[9] + " " + dataSegments[10] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            dataSegments[11] + " " + dataSegments[12] + " " + dataSegments[13], text); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    text = QuickFixer.setLine(lineNumber + 1,
                            "3 " + dataSegments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                                    dataSegments[2] + " " + dataSegments[3] + " " + dataSegments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    dataSegments[5] + " " + dataSegments[6] + " " + dataSegments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    dataSegments[8] + " " + dataSegments[9] + " " + dataSegments[10] +  //$NON-NLS-1$ //$NON-NLS-2$
                                    "<br>3 " + dataSegments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                                    dataSegments[8] + " " + dataSegments[9] + " " + dataSegments[10] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    dataSegments[11] + " " + dataSegments[12] + " " + dataSegments[13] + " " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    dataSegments[2] + " " + dataSegments[3] + " " + dataSegments[4], text);  //$NON-NLS-1$ //$NON-NLS-2$

                }
            } else if (pointB) {
                NLogger.debug(ErrorFixer.class, "Point B"); //$NON-NLS-1$
                if (vm.linkedCommonFaces(vA, vB).size() == 1 && vm.linkedCommonFaces(vB, vC).size() == 1) {
                    text = QuickFixer.setLine(lineNumber + 1, "3 " + dataSegments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                            dataSegments[2] + " " + dataSegments[3] + " " + dataSegments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            dataSegments[8] + " " + dataSegments[9] + " " + dataSegments[10] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            dataSegments[11] + " " + dataSegments[12] + " " + dataSegments[13], text); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    text = QuickFixer.setLine(lineNumber + 1,
                            "3 " + dataSegments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                                    dataSegments[5] + " " + dataSegments[6] + " " + dataSegments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    dataSegments[8] + " " + dataSegments[9] + " " + dataSegments[10] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    dataSegments[11] + " " + dataSegments[12] + " " + dataSegments[13] + //$NON-NLS-1$ //$NON-NLS-2$
                                    "<br>3 " + dataSegments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                                    dataSegments[11] + " " + dataSegments[12] + " " + dataSegments[13] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    dataSegments[2] + " " + dataSegments[3] + " " + dataSegments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    dataSegments[5] + " " + dataSegments[6] + " " + dataSegments[7], text);  //$NON-NLS-1$ //$NON-NLS-2$
                }
            } else if (pointC) {
                NLogger.debug(ErrorFixer.class, "Point C"); //$NON-NLS-1$
                if (vm.linkedCommonFaces(vB, vC).size() == 1 && vm.linkedCommonFaces(vC, vD).size() == 1) {
                    text = QuickFixer.setLine(lineNumber + 1, "3 " + dataSegments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                            dataSegments[2] + " " + dataSegments[3] + " " + dataSegments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            dataSegments[5] + " " + dataSegments[6] + " " + dataSegments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            dataSegments[11] + " " + dataSegments[12] + " " + dataSegments[13], text); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    text = QuickFixer.setLine(lineNumber + 1,
                            "3 " + dataSegments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                                    dataSegments[2] + " " + dataSegments[3] + " " + dataSegments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    dataSegments[5] + " " + dataSegments[6] + " " + dataSegments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    dataSegments[8] + " " + dataSegments[9] + " " + dataSegments[10] +  //$NON-NLS-1$ //$NON-NLS-2$
                                    "<br>3 " + dataSegments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                                    dataSegments[8] + " " + dataSegments[9] + " " + dataSegments[10] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    dataSegments[11] + " " + dataSegments[12] + " " + dataSegments[13] + " " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    dataSegments[2] + " " + dataSegments[3] + " " + dataSegments[4], text);  //$NON-NLS-1$ //$NON-NLS-2$

                }
            } else if (pointD) {
                NLogger.debug(ErrorFixer.class, "Point D"); //$NON-NLS-1$
                if (vm.linkedCommonFaces(vC, vD).size() == 1 && vm.linkedCommonFaces(vD, vA).size() == 1) {
                    text = QuickFixer.setLine(lineNumber + 1, "3 " + dataSegments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                            dataSegments[2] + " " + dataSegments[3] + " " + dataSegments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            dataSegments[5] + " " + dataSegments[6] + " " + dataSegments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            dataSegments[8] + " " + dataSegments[9] + " " + dataSegments[10], text); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    text = QuickFixer.setLine(lineNumber + 1,
                            "3 " + dataSegments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                                    dataSegments[5] + " " + dataSegments[6] + " " + dataSegments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    dataSegments[8] + " " + dataSegments[9] + " " + dataSegments[10] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    dataSegments[11] + " " + dataSegments[12] + " " + dataSegments[13] + //$NON-NLS-1$ //$NON-NLS-2$
                                    "<br>3 " + dataSegments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                                    dataSegments[11] + " " + dataSegments[12] + " " + dataSegments[13] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    dataSegments[2] + " " + dataSegments[3] + " " + dataSegments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    dataSegments[5] + " " + dataSegments[6] + " " + dataSegments[7], text);  //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }
        break;
        case 36: // Coplanar Quad
        {
            String[] dataSegments = line.trim().split("\\s+"); //$NON-NLS-1$

            final Vector3d vertexA = new Vector3d();
            final Vector3d vertexB = new Vector3d();
            final Vector3d vertexC = new Vector3d();
            final Vector3d vertexD = new Vector3d();

            // 1st vertex
            vertexA.setX(new BigDecimal(dataSegments[2], Threshold.MC));
            vertexA.setY(new BigDecimal(dataSegments[3], Threshold.MC));
            vertexA.setZ(new BigDecimal(dataSegments[4], Threshold.MC));
            // 2nd vertex
            vertexB.setX(new BigDecimal(dataSegments[5], Threshold.MC));
            vertexB.setY(new BigDecimal(dataSegments[6], Threshold.MC));
            vertexB.setZ(new BigDecimal(dataSegments[7], Threshold.MC));
            // 3rd vertex
            vertexC.setX(new BigDecimal(dataSegments[8], Threshold.MC));
            vertexC.setY(new BigDecimal(dataSegments[9], Threshold.MC));
            vertexC.setZ(new BigDecimal(dataSegments[10], Threshold.MC));
            // 4th vertex
            vertexD.setX(new BigDecimal(dataSegments[11], Threshold.MC));
            vertexD.setY(new BigDecimal(dataSegments[12], Threshold.MC));
            vertexD.setZ(new BigDecimal(dataSegments[13], Threshold.MC));

            Vector3d[] normals = new Vector3d[4];
            Vector3d[] lineVectors = new Vector3d[4];
            lineVectors[0] = Vector3d.sub(vertexB, vertexA);
            lineVectors[1] = Vector3d.sub(vertexC, vertexB);
            lineVectors[2] = Vector3d.sub(vertexD, vertexC);
            lineVectors[3] = Vector3d.sub(vertexA, vertexD);
            normals[0] = Vector3d.cross(lineVectors[0], lineVectors[1]);
            normals[1] = Vector3d.cross(lineVectors[1], lineVectors[2]);
            normals[2] = Vector3d.cross(lineVectors[2], lineVectors[3]);
            normals[3] = Vector3d.cross(lineVectors[3], lineVectors[0]);

            double angle = Vector3d.angle(normals[0], normals[2]);
            angle = Math.min(angle, Math.abs(180.0 - angle));
            if (angle > Threshold.coplanarityAngleError) {
                text = QuickFixer.setLine(lineNumber + 1,
                        "3 " + dataSegments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                                dataSegments[2] + " " + dataSegments[3] + " " + dataSegments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                dataSegments[5] + " " + dataSegments[6] + " " + dataSegments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                dataSegments[8] + " " + dataSegments[9] + " " + dataSegments[10] +  //$NON-NLS-1$ //$NON-NLS-2$
                                "<br>3 " + dataSegments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                                dataSegments[8] + " " + dataSegments[9] + " " + dataSegments[10] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                dataSegments[11] + " " + dataSegments[12] + " " + dataSegments[13] + " " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                dataSegments[2] + " " + dataSegments[3] + " " + dataSegments[4] + //$NON-NLS-1$ //$NON-NLS-2$
                                "<br>5 24 " +  //$NON-NLS-1$
                                dataSegments[8] + " " + dataSegments[9] + " " + dataSegments[10] + " " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                dataSegments[2] + " " + dataSegments[3] + " " + dataSegments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                dataSegments[5] + " " + dataSegments[6] + " " + dataSegments[7] + " " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                dataSegments[11] + " " + dataSegments[12] + " " + dataSegments[13], text);  //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                text = QuickFixer.setLine(lineNumber + 1,
                        "3 " + dataSegments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                                dataSegments[5] + " " + dataSegments[6] + " " + dataSegments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                dataSegments[8] + " " + dataSegments[9] + " " + dataSegments[10] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                dataSegments[11] + " " + dataSegments[12] + " " + dataSegments[13] + //$NON-NLS-1$ //$NON-NLS-2$
                                "<br>3 " + dataSegments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                                dataSegments[11] + " " + dataSegments[12] + " " + dataSegments[13] + " " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                dataSegments[2] + " " + dataSegments[3] + " " + dataSegments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                dataSegments[5] + " " + dataSegments[6] + " " + dataSegments[7] + //$NON-NLS-1$ //$NON-NLS-2$
                                "<br>5 24 " +  //$NON-NLS-1$
                                dataSegments[11] + " " + dataSegments[12] + " " + dataSegments[13] + " " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                dataSegments[5] + " " + dataSegments[6] + " " + dataSegments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                dataSegments[2] + " " + dataSegments[3] + " " + dataSegments[4] + " " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                dataSegments[8] + " " + dataSegments[9] + " " + dataSegments[10], text);  //$NON-NLS-1$ //$NON-NLS-2$
            }

        }
        break;
        case 42: // '~Moved to' Reference
        {
            GData1 g1 =  (GData1) datFile.getDrawPerLine_NOCLONE().getValue(lineNumber + 1);
            String newReference = g1.getSolvedMoveTo();
            if (newReference == null) {
                MessageBox messageBox = new MessageBox(tWinShell, SWT.ICON_INFORMATION);
                messageBox.setText(I18n.ERRORFIXER_MOVED_TO);

                Object[] messageArguments = {lineNumber + 1, g1.toString()};
                MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                formatter.setLocale(MyLanguage.locale);
                formatter.applyPattern(I18n.ERRORFIXER_MOVED_TO_HINT);

                messageBox.setMessage(formatter.format(messageArguments));
                messageBox.open();
            } else {
                text = QuickFixer.setLine(lineNumber + 1, newReference, text);
            }
        }
        break;
        default:
            break;
        }
        return text;
    }

}
