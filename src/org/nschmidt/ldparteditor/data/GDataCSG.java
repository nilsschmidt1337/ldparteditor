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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Event;
import org.lwjgl.opengl.GL11;
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
import org.nschmidt.csg.Polygon;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.composite3d.PerspectiveCalculator;
import org.nschmidt.ldparteditor.helpers.math.HashBiMap;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.helpers.math.PowerRay;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.text.DatParser;

/**
 * @author nils
 *
 */
public final class GDataCSG extends GData {

    final byte type;

    private final static HashMap<String, CSG> linkedCSG = new HashMap<String, CSG>();
    private final static HashBiMap<Integer, GDataCSG> idToGDataCSG = new HashBiMap<Integer, GDataCSG>();
    private final static HashMap<DatFile, HashSet<GData3>> selectedTrianglesMap = new HashMap<DatFile, HashSet<GData3>>();
    private final static HashMap<DatFile, HashSet<GDataCSG>> selectedBodyMap = new HashMap<DatFile, HashSet<GDataCSG>>();
    private static boolean deleteAndRecompile = true;

    private final static HashSet<GDataCSG> registeredData = new HashSet<GDataCSG>();
    private final static HashSet<GDataCSG> parsedData = new HashSet<GDataCSG>();

    private static int quality = 16;
    private int global_quality = 16;
    private double global_epsilon = 1e-6;

    private final String ref1;
    private final String ref2;
    private final String ref3;

    final GData1 parent;

    private CSG compiledCSG = null;
    private CSG dataCSG = null;

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
        idToGDataCSG.clear();
        Plane.EPSILON = 1e-3;
    }

    public static void resetCSG(boolean useLowQuality) {
        quality = useLowQuality ? 12 : 16;
        HashSet<GDataCSG> ref = new HashSet<GDataCSG>(registeredData);
        ref.removeAll(parsedData);
        deleteAndRecompile = !ref.isEmpty();
        if (deleteAndRecompile) {
            registeredData.clear();
            registeredData.add(null);
            linkedCSG.clear();
            idToGDataCSG.clear();
        }
        parsedData.clear();
    }

    public byte getCSGtype() {
        return type;
    }

    public GDataCSG(final int colourNumber, float r, float g, float b, float a, GDataCSG c) {
        this(c.type, c.colourReplace(new GColour(colourNumber, r, g, b, a).toString()), c.parent);
    }

    public GDataCSG(Matrix4f m, GDataCSG c) {
        this(c.type, c.transform(m), c.parent);
    }

    // CASE 1 0 !LPE [CSG TAG] [ID] [COLOUR] [MATRIX] 17
    // CASE 2 0 !LPE [CSG TAG] [ID] [ID2] [ID3] 6
    // CASE 3 0 !LPE [CSG TAG] [ID] 4
    public GDataCSG(byte type, String csgLine, GData1 parent) {
        this.parent = parent;
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
                    colour = View.getLDConfigColour(16);
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
                    }
                } catch (NumberFormatException e) {
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
                    }
                } catch (NumberFormatException e) {
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
        final boolean modified = c3d.getManipulator().isModified();
        if (deleteAndRecompile || modified) {
            final Matrix4f m;
            if (modified) {
                m = c3d.getManipulator().getTempTransformationCSG4f();
            } else {
                m = View.ID;
            }
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
                                idToGDataCSG.put(quad.ID, this);
                                CSG csgQuad = quad.toCSG(colour);
                                if (modified && isSelected(c3d)) {
                                    csgQuad = transformWithManipulator(csgQuad, m, matrix);
                                } else {
                                    csgQuad = csgQuad.transformed(matrix);
                                }
                                dataCSG = csgQuad;
                                linkedCSG.put(ref1, csgQuad);
                                break;
                            case CSG.CIRCLE:
                                CSGCircle circle = new CSGCircle(quality);
                                idToGDataCSG.put(circle.ID, this);
                                CSG csgCircle = circle.toCSG(colour);
                                if (modified && isSelected(c3d)) {
                                    csgCircle = transformWithManipulator(csgCircle, m, matrix);
                                } else {
                                    csgCircle = csgCircle.transformed(matrix);
                                }
                                dataCSG = csgCircle;
                                linkedCSG.put(ref1, csgCircle);
                                break;
                            case CSG.ELLIPSOID:
                                CSGSphere sphere = new CSGSphere(quality, quality / 2);
                                idToGDataCSG.put(sphere.ID, this);
                                CSG csgSphere = sphere.toCSG(colour);
                                if (modified && isSelected(c3d)) {
                                    csgSphere = transformWithManipulator(csgSphere, m, matrix);
                                } else {
                                    csgSphere = csgSphere.transformed(matrix);
                                }
                                dataCSG = csgSphere;
                                linkedCSG.put(ref1, csgSphere);
                                break;
                            case CSG.CUBOID:
                                CSGCube cube = new CSGCube();
                                idToGDataCSG.put(cube.ID, this);
                                CSG csgCube = cube.toCSG(colour);
                                if (modified && isSelected(c3d)) {
                                    csgCube = transformWithManipulator(csgCube, m, matrix);
                                } else {
                                    csgCube = csgCube.transformed(matrix);
                                }
                                dataCSG = csgCube;
                                linkedCSG.put(ref1, csgCube);
                                break;
                            case CSG.CYLINDER:
                                CSGCylinder cylinder = new CSGCylinder(quality);
                                idToGDataCSG.put(cylinder.ID, this);
                                CSG csgCylinder = cylinder.toCSG(colour);
                                if (modified && isSelected(c3d)) {
                                    csgCylinder = transformWithManipulator(csgCylinder, m, matrix);
                                } else {
                                    csgCylinder = csgCylinder.transformed(matrix);
                                }
                                dataCSG = csgCylinder;
                                linkedCSG.put(ref1, csgCylinder);
                                break;
                            case CSG.CONE:
                                CSGCone cone = new CSGCone(quality);
                                idToGDataCSG.put(cone.ID, this);
                                CSG csgCone = cone.toCSG(colour);
                                if (modified && isSelected(c3d)) {
                                    csgCone = transformWithManipulator(csgCone, m, matrix);
                                } else {
                                    csgCone = csgCone.transformed(matrix);
                                }
                                dataCSG = csgCone;
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
                        quality = c3d.getManipulator().isModified() ? 12 : global_quality;
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

    private CSG transformWithManipulator(CSG csg, Matrix4f transformation4f, Matrix4f myMatrix) {
        // FIXME Needs implementation for issue #161
        // done!?
        return csg.transformed(myMatrix).transformed(transformation4f);
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
    public String inlinedString(final byte bfc, final GColour colour) {
        switch (type) {
        case CSG.COMPILE:
            if (compiledCSG != null) {

                final StringBuilder sb = new StringBuilder();

                try {
                    new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(false, false, new IRunnableWithProgress() {

                        @Override
                        public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                            Editor3DWindow.getWindow().getShell().getDisplay().readAndDispatch();
                            Object[] messageArguments = {getNiceString()};
                            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                            formatter.setLocale(MyLanguage.LOCALE);
                            formatter.applyPattern(I18n.DATFILE_Inlined);

                            sb.append(formatter.format(messageArguments) + "<br>"); //$NON-NLS-1$

                            HashMap<GData3, Integer> result = compiledCSG.getResult();

                            for (GData3 g3 : result.keySet()) {
                                StringBuilder lineBuilder3 = new StringBuilder();
                                lineBuilder3.append("3 "); //$NON-NLS-1$
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
                        }
                    });
                } catch (InvocationTargetException consumed) {
                    consumed.printStackTrace();
                } catch (InterruptedException consumed) {
                    consumed.printStackTrace();
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

    public String transform(Matrix4f m) {
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

            // FIXME Needs implementation for issue #161
            // done!

            Matrix4f oldMatrix = new Matrix4f(matrix);
            oldMatrix.m30 = oldMatrix.m30 / 1000f;
            oldMatrix.m31 = oldMatrix.m31 / 1000f;
            oldMatrix.m32 = oldMatrix.m32 / 1000f;
            Matrix4f.transpose(oldMatrix, oldMatrix);
            oldMatrix.m30 = oldMatrix.m03;
            oldMatrix.m31 = oldMatrix.m13;
            oldMatrix.m32 = oldMatrix.m23;
            oldMatrix.m03 = 0f;
            oldMatrix.m13 = 0f;
            oldMatrix.m23 = 0f;
            Matrix accurateLocalMatrix = new Matrix(oldMatrix);
            Matrix transformation = new Matrix(m);
            transformation = transformation.transpose();
            BigDecimal tx = accurateLocalMatrix.M30.add(BigDecimal.ZERO);
            BigDecimal ty = accurateLocalMatrix.M31.add(BigDecimal.ZERO);
            BigDecimal tz = accurateLocalMatrix.M32.add(BigDecimal.ZERO);
            accurateLocalMatrix = accurateLocalMatrix.translate(new BigDecimal[] { tx.negate(), ty.negate(), tz.negate() });
            accurateLocalMatrix = Matrix.mul(transformation, accurateLocalMatrix);
            accurateLocalMatrix = accurateLocalMatrix.translate(new BigDecimal[] { tx, ty, tz });

            String tag = ref1.substring(0, ref1.lastIndexOf("#>")); //$NON-NLS-1$
            return "0 !LPE" + t + tag + " " + colourBuilder.toString() + " " + accurateLocalMatrix.toLDrawString(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        default:
            return text;
        }
    }

    public String colourReplace(String colour2) {
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
            Matrix4f newMatrix = new Matrix4f(this.matrix);
            Matrix4f.transpose(newMatrix, newMatrix);
            newMatrix.m30 = newMatrix.m03;
            newMatrix.m31 = newMatrix.m13;
            newMatrix.m32 = newMatrix.m23;
            newMatrix.m03 = 0f;
            newMatrix.m13 = 0f;
            newMatrix.m23 = 0f;
            String col = colour2;
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
    public void getVertexNormalMap(TreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, HashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {}
    @Override
    public void getVertexNormalMapNOCERTIFY(TreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, HashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {}
    @Override
    public void getVertexNormalMapNOCLIP(TreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, HashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {}

    public static boolean hasSelectionCSG(DatFile df) {
        final HashSet<GDataCSG> selectedBodies = selectedBodyMap.get(df);
        if (selectedBodies == null) {
            selectedBodyMap.put(df, new HashSet<GDataCSG>());
            return false;
        }
        return !selectedBodies.isEmpty();
    }

    public boolean isSelected(Composite3D c3d) {
        final HashSet<GDataCSG> map = selectedBodyMap.get(c3d.getLockableDatFileReference());
        if (map == null) {
            return false;
        }
        return map.contains(this);
    }

    public static void drawSelectionCSG(Composite3D c3d, final boolean modifiedManipulator) {
        final HashSet<GData3> selectedTriangles = selectedTrianglesMap.get(c3d.getLockableDatFileReference());
        if (selectedTriangles == null) {
            selectedTrianglesMap.put(c3d.getLockableDatFileReference(), new HashSet<GData3>());
            return;
        }
        if (!selectedTriangles.isEmpty()) {
            GL11.glColor3f(View.vertex_selected_Colour_r[0], View.vertex_selected_Colour_g[0], View.vertex_selected_Colour_b[0]);
            GL11.glBegin(GL11.GL_LINES);
            for (GData3 tri : selectedTriangles) {
                GL11.glVertex3f(tri.x1, tri.y1, tri.z1);
                GL11.glVertex3f(tri.x2, tri.y2, tri.z2);
                GL11.glVertex3f(tri.x2, tri.y2, tri.z2);
                GL11.glVertex3f(tri.x3, tri.y3, tri.z3);
                GL11.glVertex3f(tri.x3, tri.y3, tri.z3);
                GL11.glVertex3f(tri.x1, tri.y1, tri.z1);
            }
            GL11.glEnd();
        }
    }

    public static void selectCSG(Composite3D c3d, Event event) {
        final HashSet<GData3> selectedTriangles = selectedTrianglesMap.get(c3d.getLockableDatFileReference());
        if (selectedTriangles == null) {
            selectedTrianglesMap.put(c3d.getLockableDatFileReference(), new HashSet<GData3>());
            return;
        }
        if (!c3d.getKeys().isCtrlPressed()) {
            selectedTriangles.clear();
        }
        final Integer selectedBodyID = selectCSG_helper(c3d, event);
        if (selectedBodyID != null) {
            for (Entry<String, CSG> csg_pair : linkedCSG.entrySet()) {
                if (csg_pair.getKey() != null && csg_pair.getKey().endsWith("#>null")) { //$NON-NLS-1$
                    CSG csg = csg_pair.getValue();
                    if (csg != null) {
                        for(Entry<GData3, Integer> pair : csg.getResult().entrySet()) {
                            if (selectedBodyID.equals(pair.getValue())) {
                                selectedTriangles.add(pair.getKey());
                            }
                        }
                    }
                }
            }
        }
    }

    private static Integer selectCSG_helper(Composite3D c3d, Event event) {
        final PowerRay powerRay = new PowerRay();
        final HashBiMap<Integer, GData> dpl = c3d.getLockableDatFileReference().getDrawPerLine_NOCLONE();

        PerspectiveCalculator perspective = c3d.getPerspectiveCalculator();
        Matrix4f viewport_rotation = c3d.getRotation();
        Vector4f zAxis4f = new Vector4f(0, 0, -1f, 1f);
        Matrix4f ovr_inverse2 = Matrix4f.invert(viewport_rotation, null);
        Matrix4f.transform(ovr_inverse2, zAxis4f, zAxis4f);
        Vector4f rayDirection = (Vector4f) new Vector4f(zAxis4f.x, zAxis4f.y, zAxis4f.z, 0f).normalise();
        rayDirection.w = 1f;

        Vertex[] triQuadVerts = new Vertex[3];

        Vector4f orig = perspective.get3DCoordinatesFromScreen(event.x, event.y);
        Vector4f point = new Vector4f(orig);

        double minDist = Double.MAX_VALUE;
        final double[] dist = new double[1];
        Integer result = null;
        GDataCSG resultObj = null;
        for (CSG csg : linkedCSG.values()) {
            for(Entry<GData3, Integer> pair : csg.getResult().entrySet()) {
                final GData3 triangle = pair.getKey();

                triQuadVerts[0] = new Vertex(triangle.x1, triangle.y1, triangle.z1);
                triQuadVerts[1] = new Vertex(triangle.x2, triangle.y2, triangle.z2);
                triQuadVerts[2] = new Vertex(triangle.x3, triangle.y3, triangle.z3);

                if (powerRay.TRIANGLE_INTERSECT(orig, rayDirection, triQuadVerts[0], triQuadVerts[1], triQuadVerts[2], point, dist)) {
                    if (dist[0] < minDist) {
                        Integer result2 = pair.getValue();
                        if (result2 != null) {
                            for (GDataCSG c : registeredData) {
                                if (dpl.containsValue(c) && idToGDataCSG.containsKey(result2)) {
                                    if (c.ref1 != null && c.ref2 == null && c.ref3 == null && c.type != CSG.COMPILE) {
                                        resultObj = idToGDataCSG.getValue(result2);
                                        if (resultObj != null && resultObj.ref1 != null && resultObj.ref1.endsWith("#>null")) { //$NON-NLS-1$
                                            minDist = dist[0];
                                            result = result2;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        selectedBodyMap.putIfAbsent(c3d.getLockableDatFileReference(), new HashSet<GDataCSG>());
        if (!c3d.getKeys().isCtrlPressed()) {
            selectedBodyMap.get(c3d.getLockableDatFileReference()).clear();
        }
        selectedBodyMap.get(c3d.getLockableDatFileReference()).add(resultObj);
        return result;
    }

    public static HashSet<GDataCSG> getSelection(DatFile df) {
        selectedBodyMap.putIfAbsent(df, new HashSet<GDataCSG>());
        return selectedBodyMap.get(df);
    }

    public static void selectAll(DatFile df) {
        clearSelection(df);
        HashSet<GDataCSG> newSelection = new HashSet<GDataCSG>(registeredData);
        for (Iterator<GDataCSG> it = newSelection.iterator(); it.hasNext();) {
            final GDataCSG g = it.next();
            if (g.ref1 != null && g.ref2 == null && g.ref3 == null && g.type != CSG.COMPILE) {
                if (g.ref1.endsWith("#>null") && g.type != CSG.QUALITY) { //$NON-NLS-1$
                    continue;
                }
            }
            it.remove();
        }
        selectedBodyMap.get(df).addAll(newSelection);
    }

    public static void clearSelection(DatFile df) {
        selectedBodyMap.putIfAbsent(df, new HashSet<GDataCSG>());
        selectedBodyMap.get(df).clear();
    }

    public static void rebuildSelection(DatFile df) {
        final Composite3D c3d = df.getLastSelectedComposite();
        if (c3d == null || df.getLastSelectedComposite().isDisposed()) return;
        final HashSet<GData3> selectedTriangles = selectedTrianglesMap.get(df);
        final HashSet<GDataCSG> selectedBodies = selectedBodyMap.get(df);
        if (selectedTriangles == null) {
            selectedTrianglesMap.put(df, new HashSet<GData3>());
            return;
        }
        selectedTriangles.clear();
        for (GDataCSG c : selectedBodies) {
            if (c == null) {
                selectedTriangles.clear();
                selectedBodies.clear();
                return;
            }
            if (c.dataCSG == null) {
                selectedTriangles.clear();
            }
            for (Polygon p : c.dataCSG.getPolygons()) {
                Matrix4f id = new Matrix4f();
                Matrix4f.setIdentity(id);
                GData1 g1 = new GData1(-1, .5f, .5f, .5f, 1f, id, View.ACCURATE_ID, new ArrayList<String>(), null, null, 1, false, id, View.ACCURATE_ID, null, View.DUMMY_REFERENCE, true, false,
                        new HashSet<String>(), View.DUMMY_REFERENCE);
                selectedTriangles.addAll(p.toLDrawTriangles(g1).keySet());
            }
        }
        if (selectedTriangles.isEmpty()) {
            selectedBodies.clear();
        }
    }

    public Matrix4f getLDrawMatrix() {
        Matrix4f oldMatrix = new Matrix4f(matrix);
        Matrix4f.transpose(oldMatrix, oldMatrix);
        oldMatrix.m30 = oldMatrix.m03;
        oldMatrix.m31 = oldMatrix.m13;
        oldMatrix.m32 = oldMatrix.m23;
        oldMatrix.m03 = 0f;
        oldMatrix.m13 = 0f;
        oldMatrix.m23 = 0f;
        return oldMatrix;
    }
}
