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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;

/**
 * @author nils
 *
 */
final class ErrorFixer {

    public static String fix(int lineNumber, String sort, String line, String text, DatFile datFile, Shell tWinShell) {
        int s = Integer.parseInt(sort, 16);
        switch (s) {
        case 1: // Duplicated line
            text = QuickFixer.setLine(lineNumber + 1, "<rm>", text); //$NON-NLS-1$
            break;
        case 2: // Singular Matrix
        {
            String[] data_segments = line.trim().split(" "); //$NON-NLS-1$

            boolean M00 = false;
            boolean M01 = false;
            boolean M02 = false;
            boolean M10 = false;
            boolean M11 = false;
            boolean M12 = false;
            boolean M20 = false;
            boolean M21 = false;
            boolean M22 = false;

            {
                int i = 0;
                for (String seg : data_segments) {
                    if (!seg.trim().equals("")) {  //$NON-NLS-1$
                        i++;
                        switch (i) {
                        case 6:
                            M00 = new BigDecimal(seg).equals(BigDecimal.ZERO);
                            break;
                        case 7:
                            M01 = new BigDecimal(seg).equals(BigDecimal.ZERO);
                            break;
                        case 8:
                            M02 = new BigDecimal(seg).equals(BigDecimal.ZERO);
                            break;
                        case 9:
                            M10 = new BigDecimal(seg).equals(BigDecimal.ZERO);
                            break;
                        case 10:
                            M11 = new BigDecimal(seg).equals(BigDecimal.ZERO);
                            break;
                        case 11:
                            M12 = new BigDecimal(seg).equals(BigDecimal.ZERO);
                            break;
                        case 12:
                            M20 = new BigDecimal(seg).equals(BigDecimal.ZERO);
                            break;
                        case 13:
                            M21 = new BigDecimal(seg).equals(BigDecimal.ZERO);
                            break;
                        case 14:
                            M22 = new BigDecimal(seg).equals(BigDecimal.ZERO);
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
                for (String seg : data_segments) {
                    if (!seg.trim().equals("")) {  //$NON-NLS-1$
                        i++;
                        switch (i) {
                        case 6:
                            if (M00 && (M01 && M02 || M10 && M20)) {
                                sb.append("1"); //$NON-NLS-1$
                            } else {
                                sb.append(seg);
                            }
                            break;
                        case 10:
                            if (M11 && (M10 && M12 || M01 && M21)) {
                                sb.append("1"); //$NON-NLS-1$
                            } else {
                                sb.append(seg);
                            }
                            break;
                        case 14:
                            if (M22 && (M20 && M21 || M02 && M12)) {
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

            String[] data_segments = line.trim().split("\\s+"); //$NON-NLS-1$
            text = QuickFixer.setLine(lineNumber + 1,
                    "3 " + data_segments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                            data_segments[2] + " " + data_segments[3] + " " + data_segments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            data_segments[5] + " " + data_segments[6] + " " + data_segments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            data_segments[8] + " " + data_segments[9] + " " + data_segments[10] +  //$NON-NLS-1$ //$NON-NLS-2$
                            "<br>3 " + data_segments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                            data_segments[8] + " " + data_segments[9] + " " + data_segments[10] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            data_segments[11] + " " + data_segments[12] + " " + data_segments[13] + " " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            data_segments[2] + " " + data_segments[3] + " " + data_segments[4] + //$NON-NLS-1$ //$NON-NLS-2$
                            "<br>5 24 " +  //$NON-NLS-1$
                            data_segments[8] + " " + data_segments[9] + " " + data_segments[10] + " " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            data_segments[2] + " " + data_segments[3] + " " + data_segments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            data_segments[5] + " " + data_segments[6] + " " + data_segments[7] + " " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            data_segments[11] + " " + data_segments[12] + " " + data_segments[13], text);  //$NON-NLS-1$ //$NON-NLS-2$
        }
        break;
        case 65: // Hourglass quad (first variant)
        {
            String[] data_segments = line.trim().split("\\s+"); //$NON-NLS-1$
            text = QuickFixer.setLine(lineNumber + 1, data_segments[0] + " " + data_segments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                    data_segments[5] + " " + data_segments[6] + " " + data_segments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    data_segments[2] + " " + data_segments[3] + " " + data_segments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    data_segments[8] + " " + data_segments[9] + " " + data_segments[10] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    data_segments[11] + " " + data_segments[12] + " " + data_segments[13], text); //$NON-NLS-1$ //$NON-NLS-2$
        }
        break;
        case 66: // Hourglass quad (second variant)
        {
            String[] data_segments = line.trim().split("\\s+"); //$NON-NLS-1$
            text = QuickFixer.setLine(lineNumber + 1, data_segments[0] + " " + data_segments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                    data_segments[2] + " " + data_segments[3] + " " + data_segments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    data_segments[8] + " " + data_segments[9] + " " + data_segments[10] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    data_segments[5] + " " + data_segments[6] + " " + data_segments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    data_segments[11] + " " + data_segments[12] + " " + data_segments[13], text); //$NON-NLS-1$ //$NON-NLS-2$
        }
        break;
        case 13: // Line identical vertices (line, triangle)
            text = QuickFixer.setLine(lineNumber + 1, "<rm>", text); //$NON-NLS-1$
            break;
        case 68: // Identical vertices (quad)
        {
            String[] data_segments = line.trim().split("\\s+"); //$NON-NLS-1$

            final Vector3d vertexA = new Vector3d();
            final Vector3d vertexB = new Vector3d();
            final Vector3d vertexC = new Vector3d();
            final Vector3d vertexD = new Vector3d();

            // 1st vertex
            vertexA.setX(new BigDecimal(data_segments[2], Threshold.mc));
            vertexA.setY(new BigDecimal(data_segments[3], Threshold.mc));
            vertexA.setZ(new BigDecimal(data_segments[4], Threshold.mc));
            // 2nd vertex
            vertexB.setX(new BigDecimal(data_segments[5], Threshold.mc));
            vertexB.setY(new BigDecimal(data_segments[6], Threshold.mc));
            vertexB.setZ(new BigDecimal(data_segments[7], Threshold.mc));
            // 3rd vertex
            vertexC.setX(new BigDecimal(data_segments[8], Threshold.mc));
            vertexC.setY(new BigDecimal(data_segments[9], Threshold.mc));
            vertexC.setZ(new BigDecimal(data_segments[10], Threshold.mc));
            // 4th vertex
            vertexD.setX(new BigDecimal(data_segments[11], Threshold.mc));
            vertexD.setY(new BigDecimal(data_segments[12], Threshold.mc));
            vertexD.setZ(new BigDecimal(data_segments[13], Threshold.mc));


            Vector3d.sub(vertexA, vertexD, vertexA);
            Vector3d.sub(vertexB, vertexD, vertexB);
            Vector3d.sub(vertexC, vertexD, vertexC);

            boolean AD = vertexA.length().compareTo(Threshold.identical_vertex_distance) < 0; // AD
            boolean BD = vertexB.length().compareTo(Threshold.identical_vertex_distance) < 0; // BD
            boolean CD = vertexC.length().compareTo(Threshold.identical_vertex_distance) < 0; // CD
            boolean AB = Vector3d.sub(vertexA, vertexB).length().compareTo(Threshold.identical_vertex_distance) < 0; // AB
            boolean BC = Vector3d.sub(vertexB, vertexC).length().compareTo(Threshold.identical_vertex_distance) < 0; // BC
            boolean AC = Vector3d.sub(vertexA, vertexC).length().compareTo(Threshold.identical_vertex_distance) < 0; // AC

            if (AD && !(BD || CD || AB || BC || AC)) {
                text = QuickFixer.setLine(lineNumber + 1, "3 " + data_segments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                        data_segments[5] + " " + data_segments[6] + " " + data_segments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        data_segments[8] + " " + data_segments[9] + " " + data_segments[10] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        data_segments[11] + " " + data_segments[12] + " " + data_segments[13], text); //$NON-NLS-1$ //$NON-NLS-2$
            } else if (CD && !(BD || AD || AB || BC || AC)) {
                text = QuickFixer.setLine(lineNumber + 1, "3 " + data_segments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                        data_segments[2] + " " + data_segments[3] + " " + data_segments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        data_segments[5] + " " + data_segments[6] + " " + data_segments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        data_segments[11] + " " + data_segments[12] + " " + data_segments[13], text); //$NON-NLS-1$ //$NON-NLS-2$
            } else if (BC && !(BD || CD || AB || AD || AC)) {
                text = QuickFixer.setLine(lineNumber + 1, "3 " + data_segments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                        data_segments[2] + " " + data_segments[3] + " " + data_segments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        data_segments[8] + " " + data_segments[9] + " " + data_segments[10] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        data_segments[11] + " " + data_segments[12] + " " + data_segments[13], text); //$NON-NLS-1$ //$NON-NLS-2$
            } else if (AB && !(BD || CD || AD || BC || AC)) {
                text = QuickFixer.setLine(lineNumber + 1, "3 " + data_segments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                        data_segments[5] + " " + data_segments[6] + " " + data_segments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        data_segments[8] + " " + data_segments[9] + " " + data_segments[10] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        data_segments[11] + " " + data_segments[12] + " " + data_segments[13], text); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                text = QuickFixer.setLine(lineNumber + 1, "<rm>", text); //$NON-NLS-1$
            }
        }
        break;
        case 52: // Collinear quad
        {
            String[] data_segments = line.trim().split("\\s+"); //$NON-NLS-1$

            final Vector3d vertexA = new Vector3d();
            final Vector3d vertexB = new Vector3d();
            final Vector3d vertexC = new Vector3d();
            final Vector3d vertexD = new Vector3d();

            // 1st vertex
            vertexA.setX(new BigDecimal(data_segments[2], Threshold.mc));
            vertexA.setY(new BigDecimal(data_segments[3], Threshold.mc));
            vertexA.setZ(new BigDecimal(data_segments[4], Threshold.mc));
            // 2nd vertex
            vertexB.setX(new BigDecimal(data_segments[5], Threshold.mc));
            vertexB.setY(new BigDecimal(data_segments[6], Threshold.mc));
            vertexB.setZ(new BigDecimal(data_segments[7], Threshold.mc));
            // 3rd vertex
            vertexC.setX(new BigDecimal(data_segments[8], Threshold.mc));
            vertexC.setY(new BigDecimal(data_segments[9], Threshold.mc));
            vertexC.setZ(new BigDecimal(data_segments[10], Threshold.mc));
            // 4th vertex
            vertexD.setX(new BigDecimal(data_segments[11], Threshold.mc));
            vertexD.setY(new BigDecimal(data_segments[12], Threshold.mc));
            vertexD.setZ(new BigDecimal(data_segments[13], Threshold.mc));

            Vertex A = new Vertex(vertexA.X, vertexA.Y, vertexA.Z);
            Vertex B = new Vertex(vertexB.X, vertexB.Y, vertexB.Z);
            Vertex C = new Vertex(vertexC.X, vertexC.Y, vertexC.Z);
            Vertex D = new Vertex(vertexD.X, vertexD.Y, vertexD.Z);

            Vector3d.sub(vertexA, vertexD, vertexA);
            Vector3d.sub(vertexB, vertexD, vertexB);
            Vector3d.sub(vertexC, vertexD, vertexC);

            double angle = Vector3d.angle(vertexA, vertexB);
            boolean AB = angle < Threshold.collinear_angle_minimum;
            angle = Vector3d.angle(vertexB, vertexC);
            boolean BC = angle < Threshold.collinear_angle_minimum;
            boolean ACD = 180.0 - Vector3d.angle(vertexC, vertexA) < Threshold.collinear_angle_minimum;
            angle = 180.0 - Vector3d.angle(Vector3d.sub(vertexC, vertexB), Vector3d.sub(vertexA, vertexB)); // 180 - (C-B)(A-B)
            boolean ACB = angle < Threshold.collinear_angle_minimum;

            VertexManager vm = datFile.getVertexManager();

            if (AB && (BC || ACD || ACB) || BC && (ACD || ACB) || ACD && ACB) {
                text = QuickFixer.setLine(lineNumber + 1, "<rm>", text); //$NON-NLS-1$
            } else if (AB) {
                NLogger.debug(ErrorFixer.class, "AB"); //$NON-NLS-1$
                if (vm.linkedCommonFaces(A, B).size() == 1 && vm.linkedCommonFaces(D, A).size() == 1) {
                    text = QuickFixer.setLine(lineNumber + 1, "3 " + data_segments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                            data_segments[5] + " " + data_segments[6] + " " + data_segments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            data_segments[8] + " " + data_segments[9] + " " + data_segments[10] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            data_segments[11] + " " + data_segments[12] + " " + data_segments[13], text); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    text = QuickFixer.setLine(lineNumber + 1,
                            "3 " + data_segments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                                    data_segments[2] + " " + data_segments[3] + " " + data_segments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    data_segments[5] + " " + data_segments[6] + " " + data_segments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    data_segments[8] + " " + data_segments[9] + " " + data_segments[10] +  //$NON-NLS-1$ //$NON-NLS-2$
                                    "<br>3 " + data_segments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                                    data_segments[8] + " " + data_segments[9] + " " + data_segments[10] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    data_segments[11] + " " + data_segments[12] + " " + data_segments[13] + " " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    data_segments[2] + " " + data_segments[3] + " " + data_segments[4], text);  //$NON-NLS-1$ //$NON-NLS-2$

                }
            } else if (BC) {
                NLogger.debug(ErrorFixer.class, "BC"); //$NON-NLS-1$
                if (vm.linkedCommonFaces(B, C).size() == 1 && vm.linkedCommonFaces(C, D).size() == 1) {
                    text = QuickFixer.setLine(lineNumber + 1, "3 " + data_segments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                            data_segments[2] + " " + data_segments[3] + " " + data_segments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            data_segments[5] + " " + data_segments[6] + " " + data_segments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            data_segments[11] + " " + data_segments[12] + " " + data_segments[13], text); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    text = QuickFixer.setLine(lineNumber + 1,
                            "3 " + data_segments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                                    data_segments[2] + " " + data_segments[3] + " " + data_segments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    data_segments[5] + " " + data_segments[6] + " " + data_segments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    data_segments[8] + " " + data_segments[9] + " " + data_segments[10] +  //$NON-NLS-1$ //$NON-NLS-2$
                                    "<br>3 " + data_segments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                                    data_segments[8] + " " + data_segments[9] + " " + data_segments[10] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    data_segments[11] + " " + data_segments[12] + " " + data_segments[13] + " " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    data_segments[2] + " " + data_segments[3] + " " + data_segments[4], text);  //$NON-NLS-1$ //$NON-NLS-2$

                }
            } else if (ACD) {
                NLogger.debug(ErrorFixer.class, "ACD"); //$NON-NLS-1$
                if (vm.linkedCommonFaces(C, D).size() == 1 && vm.linkedCommonFaces(D, A).size() == 1) {
                    text = QuickFixer.setLine(lineNumber + 1, "3 " + data_segments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                            data_segments[2] + " " + data_segments[3] + " " + data_segments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            data_segments[5] + " " + data_segments[6] + " " + data_segments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            data_segments[8] + " " + data_segments[9] + " " + data_segments[10], text); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    text = QuickFixer.setLine(lineNumber + 1,
                            "3 " + data_segments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                                    data_segments[5] + " " + data_segments[6] + " " + data_segments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    data_segments[8] + " " + data_segments[9] + " " + data_segments[10] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    data_segments[11] + " " + data_segments[12] + " " + data_segments[13] + //$NON-NLS-1$ //$NON-NLS-2$
                                    "<br>3 " + data_segments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                                    data_segments[11] + " " + data_segments[12] + " " + data_segments[13] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    data_segments[2] + " " + data_segments[3] + " " + data_segments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    data_segments[5] + " " + data_segments[6] + " " + data_segments[7], text);  //$NON-NLS-1$ //$NON-NLS-2$
                }
            } else if (ACB) {
                NLogger.debug(ErrorFixer.class, "ACB"); //$NON-NLS-1$
                if (vm.linkedCommonFaces(A, B).size() == 1 && vm.linkedCommonFaces(B, C).size() == 1) {
                    text = QuickFixer.setLine(lineNumber + 1, "3 " + data_segments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                            data_segments[2] + " " + data_segments[3] + " " + data_segments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            data_segments[8] + " " + data_segments[9] + " " + data_segments[10] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            data_segments[11] + " " + data_segments[12] + " " + data_segments[13], text); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    text = QuickFixer.setLine(lineNumber + 1,
                            "3 " + data_segments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                                    data_segments[5] + " " + data_segments[6] + " " + data_segments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    data_segments[8] + " " + data_segments[9] + " " + data_segments[10] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    data_segments[11] + " " + data_segments[12] + " " + data_segments[13] + //$NON-NLS-1$ //$NON-NLS-2$
                                    "<br>3 " + data_segments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                                    data_segments[11] + " " + data_segments[12] + " " + data_segments[13] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    data_segments[2] + " " + data_segments[3] + " " + data_segments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    data_segments[5] + " " + data_segments[6] + " " + data_segments[7], text);  //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }
        break;
        case 36: // Coplanar Quad
        {
            String[] data_segments = line.trim().split("\\s+"); //$NON-NLS-1$

            final Vector3d vertexA = new Vector3d();
            final Vector3d vertexB = new Vector3d();
            final Vector3d vertexC = new Vector3d();
            final Vector3d vertexD = new Vector3d();

            // 1st vertex
            vertexA.setX(new BigDecimal(data_segments[2], Threshold.mc));
            vertexA.setY(new BigDecimal(data_segments[3], Threshold.mc));
            vertexA.setZ(new BigDecimal(data_segments[4], Threshold.mc));
            // 2nd vertex
            vertexB.setX(new BigDecimal(data_segments[5], Threshold.mc));
            vertexB.setY(new BigDecimal(data_segments[6], Threshold.mc));
            vertexB.setZ(new BigDecimal(data_segments[7], Threshold.mc));
            // 3rd vertex
            vertexC.setX(new BigDecimal(data_segments[8], Threshold.mc));
            vertexC.setY(new BigDecimal(data_segments[9], Threshold.mc));
            vertexC.setZ(new BigDecimal(data_segments[10], Threshold.mc));
            // 4th vertex
            vertexD.setX(new BigDecimal(data_segments[11], Threshold.mc));
            vertexD.setY(new BigDecimal(data_segments[12], Threshold.mc));
            vertexD.setZ(new BigDecimal(data_segments[13], Threshold.mc));

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
            if (angle > Threshold.coplanarity_angle_error) {
                text = QuickFixer.setLine(lineNumber + 1,
                        "3 " + data_segments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                                data_segments[2] + " " + data_segments[3] + " " + data_segments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                data_segments[5] + " " + data_segments[6] + " " + data_segments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                data_segments[8] + " " + data_segments[9] + " " + data_segments[10] +  //$NON-NLS-1$ //$NON-NLS-2$
                                "<br>3 " + data_segments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                                data_segments[8] + " " + data_segments[9] + " " + data_segments[10] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                data_segments[11] + " " + data_segments[12] + " " + data_segments[13] + " " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                data_segments[2] + " " + data_segments[3] + " " + data_segments[4] + //$NON-NLS-1$ //$NON-NLS-2$
                                "<br>5 24 " +  //$NON-NLS-1$
                                data_segments[8] + " " + data_segments[9] + " " + data_segments[10] + " " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                data_segments[2] + " " + data_segments[3] + " " + data_segments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                data_segments[5] + " " + data_segments[6] + " " + data_segments[7] + " " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                data_segments[11] + " " + data_segments[12] + " " + data_segments[13], text);  //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                text = QuickFixer.setLine(lineNumber + 1,
                        "3 " + data_segments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                                data_segments[5] + " " + data_segments[6] + " " + data_segments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                data_segments[8] + " " + data_segments[9] + " " + data_segments[10] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                data_segments[11] + " " + data_segments[12] + " " + data_segments[13] + //$NON-NLS-1$ //$NON-NLS-2$
                                "<br>3 " + data_segments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                                data_segments[11] + " " + data_segments[12] + " " + data_segments[13] + " " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                data_segments[2] + " " + data_segments[3] + " " + data_segments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                data_segments[5] + " " + data_segments[6] + " " + data_segments[7] + //$NON-NLS-1$ //$NON-NLS-2$
                                "<br>5 24 " +  //$NON-NLS-1$
                                data_segments[11] + " " + data_segments[12] + " " + data_segments[13] + " " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                data_segments[5] + " " + data_segments[6] + " " + data_segments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                data_segments[2] + " " + data_segments[3] + " " + data_segments[4] + " " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                data_segments[8] + " " + data_segments[9] + " " + data_segments[10], text);  //$NON-NLS-1$ //$NON-NLS-2$
            }

        }
        break;
        case 42: // '~Moved to' Reference
        {
            GData1 g1 =  (GData1) datFile.getDrawPerLine_NOCLONE().getValue(lineNumber + 1);
            String newReference = g1.getSolvedMoveTo();
            if (newReference == null) {
                MessageBox messageBox = new MessageBox(tWinShell, SWT.ICON_INFORMATION);
                messageBox.setText(I18n.ERRORFIXER_MovedTo);

                Object[] messageArguments = {lineNumber + 1, g1.toString()};
                MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                formatter.setLocale(MyLanguage.LOCALE);
                formatter.applyPattern(I18n.ERRORFIXER_MovedToHint);

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
