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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.csg.CSG;
import org.nschmidt.csg.CSGCircle;
import org.nschmidt.csg.CSGCone;
import org.nschmidt.csg.CSGCube;
import org.nschmidt.csg.CSGCylinder;
import org.nschmidt.csg.CSGQuad;
import org.nschmidt.csg.CSGSphere;
import org.nschmidt.csg.Plane;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.helpers.math.Vector3dd;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.text.DatParser;

/**
 * @author nils
 *
 */
public final class GDataCSG extends GData {

    final byte type;

    private final static HashMap<String, CSG> linkedCSG = new HashMap<String, CSG>();
    private static boolean deleteAndRecompile = true;

    private final static HashSet<GDataCSG> registeredData = new HashSet<GDataCSG>();
    private final static HashSet<GDataCSG> parsedData = new HashSet<GDataCSG>();

    private static int quality = 16;
    private int global_quality = 16;
    private double global_epsilon = 1e-6;

    private final String ref1;
    private final String ref2;
    private final String ref3;

    private CSG compiledCSG = null;
    private final GColour colour;
    final Matrix4f matrix;

    public static void forceRecompile() {
        registeredData.add(null);
        Plane.EPSILON = 1e-3;
    }

    public static void fullReset() {
        quality = 16;
        registeredData.clear();
        linkedCSG.clear();
        parsedData.clear();
        Plane.EPSILON = 1e-3;
    }

    public static void resetCSG() {
        quality = 16;
        HashSet<GDataCSG> ref = new HashSet<GDataCSG>(registeredData);
        ref.removeAll(parsedData);
        deleteAndRecompile = !ref.isEmpty();
        if (deleteAndRecompile) {
            registeredData.clear();
            registeredData.add(null);
            linkedCSG.clear();
        }
        parsedData.clear();
    }

    public byte getCSGtype() {
        return type;
    }

    // CASE 1 0 !LPE [CSG TAG] [ID] [COLOUR] [MATRIX] 17
    // CASE 2 0 !LPE [CSG TAG] [ID] [ID2] [ID3] 6
    // CASE 3 0 !LPE [CSG TAG] [ID] 4
    public GDataCSG(byte type, String csgLine, GData1 parent) {
        registeredData.add(this);
        String[] data_segments = csgLine.trim().split("\\s+"); //$NON-NLS-1$
        this.type = type;
        this.text = csgLine;
        switch (type) {
        case CSG.COMPILE:
            if (data_segments.length == 4) {
                ref1 = data_segments[3] + "#>" + parent.shortName; //$NON-NLS-1$
            } else {
                ref1 = null;
            }
            ref2 = null;
            ref3 = null;
            colour = null;
            matrix = null;
            break;
        case CSG.QUAD:
        case CSG.CIRCLE:
        case CSG.ELLIPSOID:
        case CSG.CUBOID:
        case CSG.CYLINDER:
        case CSG.CONE:
            if (data_segments.length == 17) {
                ref1 = data_segments[3] + "#>" + parent.shortName; //$NON-NLS-1$
                GColour c = DatParser.validateColour(data_segments[4], .5f, .5f, .5f, 1f);
                if (c != null) {
                    colour = c.clone();
                } else {
                    colour = null;
                }
                matrix = MathHelper.matrixFromStrings(data_segments[5], data_segments[6], data_segments[7], data_segments[8], data_segments[11], data_segments[14], data_segments[9],
                        data_segments[12], data_segments[15], data_segments[10], data_segments[13], data_segments[16]);
            } else {
                colour = null;
                matrix = null;
                ref1 = null;
            }
            ref2 = null;
            ref3 = null;
            break;
        case CSG.DIFFERENCE:
        case CSG.INTERSECTION:
        case CSG.UNION:
            if (data_segments.length == 6) {
                ref1 = data_segments[3] + "#>" + parent.shortName; //$NON-NLS-1$
                ref2 = data_segments[4] + "#>" + parent.shortName; //$NON-NLS-1$
                ref3 = data_segments[5] + "#>" + parent.shortName; //$NON-NLS-1$
            } else {
                ref1 = null;
                ref2 = null;
                ref3 = null;
            }
            colour = null;
            matrix = null;
            break;
        case CSG.QUALITY:
            if (data_segments.length == 4) {
                try {
                    int q = Integer.parseInt(data_segments[3]);
                    if (q > 0 && q < 49) {
                        global_quality = q;
                    } else {
                        throw new Exception();
                    }
                } catch (Exception e) {
                }
                ref1 = data_segments[3] + "#>" + parent.shortName; //$NON-NLS-1$
            } else {
                ref1 = null;
            }
            ref2 = null;
            ref3 = null;
            colour = null;
            matrix = null;
            break;
        case CSG.EPSILON:
            if (data_segments.length == 4) {
                try {
                    double q = Double.parseDouble(data_segments[3]);
                    if (q > 0d) {
                        global_epsilon = q;
                    } else {
                        throw new Exception();
                    }
                } catch (Exception e) {
                }
                ref1 = data_segments[3] + "#>" + parent.shortName; //$NON-NLS-1$
            } else {
                ref1 = null;
            }
            ref2 = null;
            ref3 = null;
            colour = null;
            matrix = null;
            break;
        default:
            ref1 = null;
            ref2 = null;
            ref3 = null;
            colour = null;
            matrix = null;
            break;
        }
    }

    private void drawAndParse(Composite3D c3d) {
        parsedData.add(this);
        if (deleteAndRecompile) {
            registeredData.remove(null);
            try {
                compiledCSG = null;
                registeredData.add(this);
                if (ref1 != null) {
                    switch (type) {
                    case CSG.QUAD:
                    case CSG.CIRCLE:
                    case CSG.ELLIPSOID:
                    case CSG.CUBOID:
                    case CSG.CYLINDER:
                    case CSG.CONE:
                        if (matrix != null) {
                            switch (type) {
                            case CSG.QUAD:
                                CSGQuad quad = new CSGQuad();
                                CSG csgQuad = quad.toCSG(colour).transformed(matrix);
                                linkedCSG.put(ref1, csgQuad);
                                break;
                            case CSG.CIRCLE:
                                CSGCircle circle = new CSGCircle(quality);
                                CSG csgCircle = circle.toCSG(colour).transformed(matrix);
                                linkedCSG.put(ref1, csgCircle);
                                break;
                            case CSG.ELLIPSOID:
                                CSGSphere sphere = new CSGSphere(quality, quality / 2);
                                CSG csgSphere = sphere.toCSG(colour).transformed(matrix);
                                linkedCSG.put(ref1, csgSphere);
                                break;
                            case CSG.CUBOID:
                                CSGCube cube = new CSGCube();
                                CSG csgCube = cube.toCSG(colour).transformed(matrix);
                                linkedCSG.put(ref1, csgCube);
                                break;
                            case CSG.CYLINDER:
                                CSGCylinder cylinder = new CSGCylinder(quality);
                                CSG csgCylinder = cylinder.toCSG(colour).transformed(matrix);
                                linkedCSG.put(ref1, csgCylinder);
                                break;
                            case CSG.CONE:
                                CSGCone cone = new CSGCone(quality);
                                CSG csgCone = cone.toCSG(colour).transformed(matrix);
                                linkedCSG.put(ref1, csgCone);
                                break;
                            default:
                                break;
                            }
                        }
                        break;
                    case CSG.COMPILE:
                        if (linkedCSG.containsKey(ref1)) {
                            compiledCSG = linkedCSG.get(ref1);
                            compiledCSG.compile();
                        } else {
                            compiledCSG = null;
                        }
                        break;
                    case CSG.DIFFERENCE:
                        if (linkedCSG.containsKey(ref1) && linkedCSG.containsKey(ref2)) {
                            linkedCSG.put(ref3, linkedCSG.get(ref1).difference(linkedCSG.get(ref2)));
                        }
                        break;
                    case CSG.INTERSECTION:
                        if (linkedCSG.containsKey(ref1) && linkedCSG.containsKey(ref2)) {
                            linkedCSG.put(ref3, linkedCSG.get(ref1).intersect(linkedCSG.get(ref2)));
                        }
                        break;
                    case CSG.UNION:
                        if (linkedCSG.containsKey(ref1) && linkedCSG.containsKey(ref2)) {
                            linkedCSG.put(ref3, linkedCSG.get(ref1).union(linkedCSG.get(ref2)));
                        }
                        break;
                    case CSG.QUALITY:
                        quality = global_quality;
                        break;
                    case CSG.EPSILON:
                        Plane.EPSILON = global_epsilon;
                        break;
                    default:
                        break;
                    }
                }
            } catch (StackOverflowError e) {
                registeredData.clear();
                linkedCSG.clear();
                parsedData.clear();
                deleteAndRecompile = false;
                registeredData.add(null);
                Plane.EPSILON = Plane.EPSILON * 10d;
            } catch (Exception e) {

            }
        }
        if (compiledCSG != null) {
            if (c3d.getRenderMode() != 5) {
                compiledCSG.draw(c3d);
            } else {
                compiledCSG.draw_textured(c3d);
            }
        }
    }

    @Override
    public void draw(Composite3D c3d) {
        drawAndParse(c3d);
    }

    @Override
    public void drawRandomColours(Composite3D c3d) {
        drawAndParse(c3d);
    }

    @Override
    public void drawBFC(Composite3D c3d) {
        drawAndParse(c3d);
    }

    @Override
    public void drawBFCuncertified(Composite3D c3d) {
        drawAndParse(c3d);
    }

    @Override
    public void drawBFC_backOnly(Composite3D c3d) {
        drawAndParse(c3d);
    }

    @Override
    public void drawBFC_Colour(Composite3D c3d) {
        drawAndParse(c3d);
    }

    @Override
    public void drawBFC_Textured(Composite3D c3d) {
        drawAndParse(c3d);
    }

    @Override
    public void drawWhileAddCondlines(Composite3D c3d) {
        drawAndParse(c3d);
    }

    @Override
    public int type() {
        return 8;
    }

    @Override
    String getNiceString() {
        return text;
    }

    @Override
    public String inlinedString(byte bfc, GColour colour) {
        switch (type) {
        case CSG.COMPILE:
            if (compiledCSG != null) {
                final BigDecimal MIN_DIST = new BigDecimal(".0001"); //$NON-NLS-1$
                StringBuilder sb = new StringBuilder();

                Object[] messageArguments = {getNiceString()};
                MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                formatter.setLocale(MyLanguage.LOCALE);
                formatter.applyPattern(I18n.DATFILE_Inlined);

                sb.append(formatter.format(messageArguments) + "<br>"); //$NON-NLS-1$

                ArrayList<GData3> result = compiledCSG.getResult();

                // FIXME Needs T-Junction Elimination!


                // Create data array

                int vertexIDcounter = 0;
                int triangleIDcounter = 0;
                int triangleCount = result.size();
                int vertexCount = triangleCount * 3;
                int mergeCount = 0;

                float[][] vertices = new float[vertexCount][3];

                int[][] vertexMerges = new int[vertexCount][2];

                float[][] triangles = new float[triangleCount][8];

                for (GData3 g3 : result) {

                    triangles[triangleIDcounter][3] = g3.colourNumber;
                    triangles[triangleIDcounter][4] = g3.r;
                    triangles[triangleIDcounter][5] = g3.g;
                    triangles[triangleIDcounter][6] = g3.b;
                    triangles[triangleIDcounter][7] = g3.a;

                    triangles[triangleIDcounter][0] = vertexIDcounter;
                    vertices[vertexIDcounter][0] = g3.x1;
                    vertices[vertexIDcounter][1] = g3.y1;
                    vertices[vertexIDcounter][2] = g3.z1;
                    vertexIDcounter++;
                    triangles[triangleIDcounter][1] = vertexIDcounter;
                    vertices[vertexIDcounter][0] = g3.x2;
                    vertices[vertexIDcounter][1] = g3.y2;
                    vertices[vertexIDcounter][2] = g3.z2;
                    vertexIDcounter++;
                    triangles[triangleIDcounter][2] = vertexIDcounter;
                    vertices[vertexIDcounter][0] = g3.x3;
                    vertices[vertexIDcounter][1] = g3.y3;
                    vertices[vertexIDcounter][2] = g3.z3;
                    vertexIDcounter++;
                    triangleIDcounter++;
                }

                // Remove near vertices

                for (int i = 0; i < vertexCount; i++) {
                    for (int j = i + 1; j < vertexCount; j++) {
                        if (0.000001f >
                        Math.pow(vertices[i][0] - vertices[j][0], 2) +
                        Math.pow(vertices[i][1] - vertices[j][1], 2) +
                        Math.pow(vertices[i][2] - vertices[j][2], 2)) {
                            vertexCount--;
                            vertices[j][0] = vertices[vertexCount][0];
                            vertices[j][1] = vertices[vertexCount][1];
                            vertices[j][2] = vertices[vertexCount][2];
                            vertexMerges[mergeCount][0] = i;
                            vertexMerges[mergeCount][1] = j;
                            mergeCount++;
                            j--;
                        }
                    }
                }

                // Apply merges to triangles
                for (int i = 0; i < triangleCount; i++) {
                    for (int j = 0; j < mergeCount; j++) {
                        for (int k = 0; k < 3; k++) {
                            if (triangles[i][k] == vertexMerges[j][1]) {
                                triangles[i][k] = vertexMerges[j][0];
                            }
                        }
                    }
                }


                for (GData3 g3 : result) {
                    StringBuilder lineBuilder3 = new StringBuilder();
                    lineBuilder3.append(3);
                    lineBuilder3.append(" "); //$NON-NLS-1$
                    if (g3.colourNumber == -1) {
                        lineBuilder3.append("0x2"); //$NON-NLS-1$
                        lineBuilder3.append(MathHelper.toHex((int) (255f * g3.r)).toUpperCase());
                        lineBuilder3.append(MathHelper.toHex((int) (255f * g3.g)).toUpperCase());
                        lineBuilder3.append(MathHelper.toHex((int) (255f * g3.b)).toUpperCase());
                    } else {
                        lineBuilder3.append(g3.colourNumber);
                    }
                    Vector4f g3_v1 = new Vector4f(g3.x1, g3.y1, g3.z1, 1f);
                    Vector4f g3_v2 = new Vector4f(g3.x2, g3.y2, g3.z2, 1f);
                    Vector4f g3_v3 = new Vector4f(g3.x3, g3.y3, g3.z3, 1f);
                    lineBuilder3.append(" "); //$NON-NLS-1$
                    lineBuilder3.append(floatToString(g3_v1.x / 1000f));
                    lineBuilder3.append(" "); //$NON-NLS-1$
                    lineBuilder3.append(floatToString(g3_v1.y / 1000f));
                    lineBuilder3.append(" "); //$NON-NLS-1$
                    lineBuilder3.append(floatToString(g3_v1.z / 1000f));
                    lineBuilder3.append(" "); //$NON-NLS-1$
                    lineBuilder3.append(floatToString(g3_v2.x / 1000f));
                    lineBuilder3.append(" "); //$NON-NLS-1$
                    lineBuilder3.append(floatToString(g3_v2.y / 1000f));
                    lineBuilder3.append(" "); //$NON-NLS-1$
                    lineBuilder3.append(floatToString(g3_v2.z / 1000f));
                    lineBuilder3.append(" "); //$NON-NLS-1$
                    lineBuilder3.append(floatToString(g3_v3.x / 1000f));
                    lineBuilder3.append(" "); //$NON-NLS-1$
                    lineBuilder3.append(floatToString(g3_v3.y / 1000f));
                    lineBuilder3.append(" "); //$NON-NLS-1$
                    lineBuilder3.append(floatToString(g3_v3.z / 1000f));
                    sb.append(lineBuilder3.toString() + "<br>"); //$NON-NLS-1$
                }
                return sb.toString();
            } else {
                return getNiceString();
            }
        default:
            return getNiceString();
        }
    }

    private String floatToString(float flt) {
        String result;
        if (flt == (int) flt) {
            result = String.format("%d", (int) flt); //$NON-NLS-1$
        } else {
            result = String.format("%s", flt); //$NON-NLS-1$
            if (result.equals("0.0"))result = "0"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (result.startsWith("-0.")) return "-" + result.substring(2); //$NON-NLS-1$ //$NON-NLS-2$
        if (result.startsWith("0.")) return result.substring(1); //$NON-NLS-1$
        return result;
    }

    @Override
    public String transformAndColourReplace(String colour2, Matrix matrix) {
        boolean notChoosen = true;
        String t = null;
        switch (type) {
        case CSG.QUAD:
            if (notChoosen) {
                t = " CSG_QUAD "; //$NON-NLS-1$
                notChoosen = false;
            }
        case CSG.CIRCLE:
            if (notChoosen) {
                t = " CSG_CIRCLE "; //$NON-NLS-1$
                notChoosen = false;
            }
        case CSG.ELLIPSOID:
            if (notChoosen) {
                t = " CSG_ELLIPSOID "; //$NON-NLS-1$
                notChoosen = false;
            }
        case CSG.CUBOID:
            if (notChoosen) {
                t = " CSG_CUBOID "; //$NON-NLS-1$
                notChoosen = false;
            }
        case CSG.CYLINDER:
            if (notChoosen) {
                t = " CSG_CYLINDER "; //$NON-NLS-1$
                notChoosen = false;
            }
        case CSG.CONE:
            if (notChoosen) {
                t = " CSG_CONE "; //$NON-NLS-1$
                notChoosen = false;
            }
            StringBuilder colourBuilder = new StringBuilder();
            if (colour == null) {
                colourBuilder.append(16);
            } else if (colour.getColourNumber() == -1) {
                colourBuilder.append("0x2"); //$NON-NLS-1$
                colourBuilder.append(MathHelper.toHex((int) (255f * colour.getR())).toUpperCase());
                colourBuilder.append(MathHelper.toHex((int) (255f * colour.getG())).toUpperCase());
                colourBuilder.append(MathHelper.toHex((int) (255f * colour.getB())).toUpperCase());
            } else {
                colourBuilder.append(colour.getColourNumber());
            }
            Matrix4f newMatrix = new Matrix4f(this.matrix);
            Matrix4f newMatrix2 = new Matrix4f(matrix.getMatrix4f());
            Matrix4f.transpose(newMatrix, newMatrix);
            newMatrix.m30 = newMatrix.m03;
            newMatrix.m31 = newMatrix.m13;
            newMatrix.m32 = newMatrix.m23;
            newMatrix.m03 = 0f;
            newMatrix.m13 = 0f;
            newMatrix.m23 = 0f;
            Matrix4f.mul(newMatrix2, newMatrix, newMatrix);
            String col = colourBuilder.toString();
            if (col.equals(colour2))
                col = "16"; //$NON-NLS-1$
            String tag = ref1.substring(0, ref1.lastIndexOf("#>")); //$NON-NLS-1$
            return "0 !LPE" + t + tag + " " + col + " " + MathHelper.matrixToString(newMatrix); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        default:
            return text;
        }
    }

    @Override
    public void getBFCorientationMap(HashMap<GData, Byte> map) {}
    @Override
    public void getBFCorientationMapNOCERTIFY(HashMap<GData, Byte> map) {}
    @Override
    public void getBFCorientationMapNOCLIP(HashMap<GData, Byte> map) {}
    @Override
    public void getVertexNormalMap(TreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, HashMap<GData, float[]> dataLinkedToNormalCACHE, VertexManager vm) {}
    @Override
    public void getVertexNormalMapNOCERTIFY(TreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, HashMap<GData, float[]> dataLinkedToNormalCACHE, VertexManager vm) {}
    @Override
    public void getVertexNormalMapNOCLIP(TreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, HashMap<GData, float[]> dataLinkedToNormalCACHE, VertexManager vm) {}

    private final BigDecimal TOLERANCE = BigDecimal.ZERO; // new BigDecimal("0.00001"); //.00001
    private final BigDecimal ZEROT = BigDecimal.ZERO; //  = new BigDecimal("-0.00001");
    private final BigDecimal ONET = BigDecimal.ONE; //  = new BigDecimal("1.00001");
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

    private boolean intersectLineLineSegmentUnidirectionalFast(Vector3dd p, Vector3dd p2, Vector3d sp, Vector3d dir, BigDecimal len, Vector3dd q, Vector3dd q2) {
        Vector3d sq = Vector3d.sub(q2, q);
        Vector3d cross = Vector3d.cross(sq, sp);
        Vector3d c = Vector3d.add(cross, q);
        Vector3d d = Vector3d.sub(q, cross);
        return intersectLineTriangleSuperFast(p, q2, d, q2, c, dir, len);
    }
}
