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
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
import org.nschmidt.csg.CSGExtrude;
import org.nschmidt.csg.CSGMesh;
import org.nschmidt.csg.CSGQuad;
import org.nschmidt.csg.CSGSphere;
import org.nschmidt.csg.Plane;
import org.nschmidt.csg.Polygon;
import org.nschmidt.csg.VectorCSGd;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.Cocoa;
import org.nschmidt.ldparteditor.helpers.composite3d.PathTruderSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.PerspectiveCalculator;
import org.nschmidt.ldparteditor.helpers.math.HashBiMap;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.helpers.math.PowerRay;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeTreeMap;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.text.DatParser;

/**
 * @author nils
 *
 */
public final class GDataCSG extends GData {

    final byte type;

    static volatile Lock static_lock = new ReentrantLock();

    private final static ThreadsafeHashMap<DatFile, HashMap<String, CSG>> linkedCSG = new ThreadsafeHashMap<DatFile, HashMap<String, CSG>>();
    private final static ThreadsafeHashMap<DatFile, HashBiMap<Integer, GDataCSG>> idToGDataCSG = new ThreadsafeHashMap<DatFile, HashBiMap<Integer, GDataCSG>>();
    private final static ThreadsafeHashMap<DatFile, HashSet<GData3>> selectedTrianglesMap = new ThreadsafeHashMap<DatFile, HashSet<GData3>>();
    private final static ThreadsafeHashMap<DatFile, HashSet<GDataCSG>> selectedBodyMap = new ThreadsafeHashMap<DatFile, HashSet<GDataCSG>>();

    private final static ThreadsafeHashMap<DatFile, HashSet<GData3>> backupSelectedTrianglesMap = new ThreadsafeHashMap<DatFile, HashSet<GData3>>();
    private final static ThreadsafeHashMap<DatFile, HashSet<GDataCSG>> backupSelectedBodyMap = new ThreadsafeHashMap<DatFile, HashSet<GDataCSG>>();

    private static boolean deleteAndRecompile = true;

    private final static ThreadsafeHashMap<DatFile, HashSet<GDataCSG>> registeredData = new ThreadsafeHashMap<DatFile, HashSet<GDataCSG>>();
    private final static ThreadsafeHashMap<DatFile, HashSet<GDataCSG>> parsedData = new ThreadsafeHashMap<DatFile, HashSet<GDataCSG>>();
    private final static ThreadsafeHashMap<DatFile, PathTruderSettings> globalExtruderConfig = new ThreadsafeHashMap<DatFile, PathTruderSettings>();
    private final static ThreadsafeHashMap<DatFile, Boolean> clearPolygonCache = new ThreadsafeHashMap<DatFile, Boolean>();
    private final static ThreadsafeHashMap<DatFile, Boolean> fullClearPolygonCache = new ThreadsafeHashMap<DatFile, Boolean>();
    private final static ThreadsafeHashMap<DatFile, ArrayList<VectorCSGd[]>> allNewPolygonVertices = new ThreadsafeHashMap<DatFile, ArrayList<VectorCSGd[]>>();
    private final static ThreadsafeHashMap<DatFile, Boolean> compileAndInline = new ThreadsafeHashMap<DatFile, Boolean>();

    private final ArrayList<GData> cachedData = new ArrayList<GData>();
    private final List<Polygon> polygonCache = new ArrayList<Polygon>();
    private PathTruderSettings extruderConfig = new PathTruderSettings();

    private static int quality = 16;
    private int global_quality = 16;
    private double global_epsilon = 1e-6;

    private final String ref1;
    private final String ref2;
    private final String ref3;

    private CSG compiledCSG = null;
    private CSG dataCSG = null;

    private final DatFile myDat;
    private final GColour colour;
    final Matrix4f matrix;

    public synchronized static void forceRecompile(DatFile df) {
        registeredData.putIfAbsent(df, new HashSet<GDataCSG>()).add(null);
        allNewPolygonVertices.putIfAbsent(df, new ArrayList<VectorCSGd[]>()).clear();
        clearPolygonCache.putIfAbsent(df, true);
        Plane.EPSILON = 1e-3;
    }

    public synchronized static void fullReset(DatFile df) {
        quality = 16;
        registeredData.putIfAbsent(df, new HashSet<GDataCSG>()).clear();
        linkedCSG.putIfAbsent(df, new HashMap<String, CSG>()).clear();
        parsedData.putIfAbsent(df, new HashSet<GDataCSG>()).clear();
        idToGDataCSG.putIfAbsent(df, new HashBiMap<Integer, GDataCSG>()).clear();
        selectedTrianglesMap.putIfAbsent(df, new HashSet<GData3>()).clear();
        selectedBodyMap.putIfAbsent(df, new HashSet<GDataCSG>()).clear();
        allNewPolygonVertices.putIfAbsent(df, new ArrayList<VectorCSGd[]>()).clear();
        backupSelectionClear(df);
        Plane.EPSILON = 1e-3;
    }

    public GColour getColour() {
        return colour;
    }

    public synchronized static void resetCSG(DatFile df, boolean useLowQuality) {
        if (useLowQuality) {
            if (allNewPolygonVertices.containsKey(df)) {
                allNewPolygonVertices.get(df).clear();
            }
            quality = 12;
        } else {
            quality = 16;
        }
        HashSet<GDataCSG> ref = new HashSet<GDataCSG>(registeredData.putIfAbsent(df, new HashSet<GDataCSG>()));
        ref.removeAll(parsedData.putIfAbsent(df, new HashSet<GDataCSG>()));
        clearPolygonCache.putIfAbsent(df, true);
        fullClearPolygonCache.putIfAbsent(df, true);
        compileAndInline.putIfAbsent(df, false);
        deleteAndRecompile = !ref.isEmpty();
        if (deleteAndRecompile) {
            globalExtruderConfig.put(df, new PathTruderSettings());
            registeredData.get(df).clear();
            registeredData.get(df).add(null);
            linkedCSG.putIfAbsent(df, new HashMap<String, CSG>()).clear();
            idToGDataCSG.putIfAbsent(df, new HashBiMap<Integer, GDataCSG>()).clear();
        }
        parsedData.get(df).clear();
    }

    public byte getCSGtype() {
        return type;
    }

    public GDataCSG(DatFile df, final int colourNumber, float r, float g, float b, float a, GDataCSG c) {
        this(df, c.type, c.colourReplace(new GColour(colourNumber, r, g, b, a).toString()), c.parent);
    }

    public GDataCSG(DatFile df, Matrix4f m, GDataCSG c) {
        this(df, c.type, c.transform(m), c.parent);
    }

    // CASE 0 0 !LPE [CSG TAG] [ID] [ID2] [COLOUR] [MATRIX] 17
    // CASE 1 0 !LPE [CSG TAG] [ID] [COLOUR] [MATRIX] 17
    // CASE 2 0 !LPE [CSG TAG] [ID] [ID2] [ID3] 6
    // CASE 3 0 !LPE [CSG TAG] [ID] 4
    public GDataCSG(DatFile df, byte type, String csgLine, GData1 parent) {
        super(parent);
        clearPolygonCache.put(df, true);
        fullClearPolygonCache.put(df, false);
        myDat = df;
        registeredData.putIfAbsent(df, new HashSet<GDataCSG>()).add(this);
        String[] data_segments = csgLine.trim().split("\\s+"); //$NON-NLS-1$
        final GColour col16 = View.getLDConfigColour(16);
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
        case CSG.MESH:
        case CSG.EXTRUDE:
        case CSG.CONE:
            if (data_segments.length == 17) {
                ref1 = data_segments[3] + "#>" + parent.shortName; //$NON-NLS-1$
                GColour c = DatParser.validateColour(data_segments[4], col16.getR(), col16.getG(), col16.getB(), 1f);
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
        case CSG.TRANSFORM:
            if (data_segments.length == 18) {
                ref1 = data_segments[3] + "#>" + parent.shortName; //$NON-NLS-1$
                ref2 = data_segments[4] + "#>" + parent.shortName; //$NON-NLS-1$
                GColour c = DatParser.validateColour(data_segments[5], col16.getR(), col16.getG(), col16.getB(), 1f);
                if (c != null) {
                    colour = c.clone();
                } else {
                    colour = View.getLDConfigColour(16);
                }
                matrix = MathHelper.matrixFromStrings(data_segments[6], data_segments[7], data_segments[8], data_segments[9], data_segments[10], data_segments[11], data_segments[12],
                        data_segments[13], data_segments[14], data_segments[15], data_segments[16], data_segments[17]);
            } else {
                colour = null;
                ref1 = null;
                ref2 = null;
                matrix = null;
            }
            ref3 = null;
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
        case CSG.EXTRUDE_CFG:
            if (data_segments.length == 4 && "DEFAULT".equals(data_segments[3])) { //$NON-NLS-1$
                extruderConfig = new PathTruderSettings();
                ref1 = data_segments[3] + "#>" + parent.shortName; //$NON-NLS-1$
            } else if (data_segments.length == 17) {
                try {
                    extruderConfig.setMaxPathSegmentLength(new BigDecimal(data_segments[4]));
                    extruderConfig.setTransitionCount(Integer.parseInt(data_segments[6]));
                    extruderConfig.setTransitionCurveControl(new BigDecimal(data_segments[8]));
                    extruderConfig.setTransitionCurveCenter(new BigDecimal(data_segments[10]));
                    extruderConfig.setRotation(new BigDecimal(data_segments[12]));
                    extruderConfig.setCompensation(Boolean.parseBoolean(data_segments[14]));
                    extruderConfig.setInverted(Boolean.parseBoolean(data_segments[16]));
                } catch (Exception ex) {
                    extruderConfig = new PathTruderSettings();
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
        final DatFile df = c3d.getLockableDatFileReference();
        drawAndParse(c3d, df, true);
    }

    public void drawAndParse(Composite3D c3d, DatFile df, boolean doDraw) {

        final boolean clearCaches = clearPolygonCache.putIfAbsent(df, true) || type == CSG.MESH && CSGMesh.needCacheRefresh(cachedData, this, df) || type == CSG.EXTRUDE && CSGExtrude.needCacheRefresh(cachedData, this, df);
        if (clearCaches) {
            clearPolygonCache.put(df, true);
        }
        final HashSet<GDataCSG> parsedData = GDataCSG.parsedData.putIfAbsent(df, new HashSet<GDataCSG>());
        parsedData.add(this);
        final boolean modified = c3d != null && c3d.getManipulator().isModified();
        if (deleteAndRecompile || modified || clearCaches) {
            final HashBiMap<Integer, GDataCSG> idToGDataCSG = GDataCSG.idToGDataCSG.putIfAbsent(df, new HashBiMap<Integer, GDataCSG>());
            final HashMap<String, CSG> linkedCSG = GDataCSG.linkedCSG.putIfAbsent(df, new HashMap<String, CSG>());
            final HashSet<GDataCSG> registeredData = GDataCSG.registeredData.putIfAbsent(df, new HashSet<GDataCSG>());
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
                    case CSG.MESH:
                    case CSG.EXTRUDE:
                        if (matrix != null) {
                            switch (type) {
                            case CSG.QUAD:
                                CSGQuad quad = new CSGQuad();
                                idToGDataCSG.put(quad.ID, this);
                                CSG csgQuad = quad.toCSG(df, colour);
                                if (modified && isSelected(df)) {
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
                                CSG csgCircle = circle.toCSG(df, colour);
                                if (modified && isSelected(df)) {
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
                                CSG csgSphere = sphere.toCSG(df, colour);
                                if (modified && isSelected(df)) {
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
                                CSG csgCube = cube.toCSG(df, colour);
                                if (modified && isSelected(df)) {
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
                                CSG csgCylinder = cylinder.toCSG(df, colour);
                                if (modified && isSelected(df)) {
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
                                CSG csgCone = cone.toCSG(df, colour);
                                if (modified && isSelected(df)) {
                                    csgCone = transformWithManipulator(csgCone, m, matrix);
                                } else {
                                    csgCone = csgCone.transformed(matrix);
                                }
                                dataCSG = csgCone;
                                linkedCSG.put(ref1, csgCone);
                                break;
                            case CSG.MESH:
                                if (clearCaches) {
                                    polygonCache.clear();
                                }
                                CSGMesh mesh = new CSGMesh(this, cachedData, polygonCache);
                                CSGMesh.fillCache(cachedData, this);
                                CSG csgMesh = mesh.toCSG(df, colour);
                                idToGDataCSG.put(mesh.ID, this);
                                if (modified && isSelected(df)) {
                                    csgMesh = transformWithManipulator(csgMesh, m, matrix);
                                } else {
                                    csgMesh = csgMesh.transformed(matrix);
                                }
                                dataCSG = csgMesh;
                                linkedCSG.put(ref1, csgMesh);
                                break;
                            case CSG.EXTRUDE:
                                if (clearCaches) {
                                    polygonCache.clear();
                                }
                                PathTruderSettings gconf = globalExtruderConfig.putIfAbsent(df, extruderConfig);
                                if (gconf != extruderConfig) {
                                    extruderConfig = gconf;
                                    polygonCache.clear();
                                }
                                CSGExtrude extruder = new CSGExtrude(this, cachedData, extruderConfig, polygonCache);
                                CSGExtrude.fillCache(cachedData, this);
                                CSG csgExtruder = extruder.toCSG(df, colour);
                                idToGDataCSG.put(extruder.ID, this);
                                if (modified && isSelected(df)) {
                                    csgExtruder = transformWithManipulator(csgExtruder, m, matrix);
                                } else {
                                    csgExtruder = csgExtruder.transformed(matrix);
                                }
                                dataCSG = csgExtruder;
                                linkedCSG.put(ref1, csgExtruder);
                                break;
                            default:
                                break;
                            }
                        }
                        break;
                    case CSG.COMPILE:
                        if (linkedCSG.containsKey(ref1)) {
                            compiledCSG = linkedCSG.get(ref1);
                            if (GDataCSG.isInlining(df)) {
                                // FIXME compiledCSG.compile_without_t_junctions(df);
                                compiledCSG.compile();
                            } else {
                                compiledCSG.compile();
                            }
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
                    case CSG.TRANSFORM:
                        if (linkedCSG.containsKey(ref1) && matrix != null) {
                            idToGDataCSG.put(ID, this);
                            if (modified && isSelected(df)) {
                                dataCSG = linkedCSG.get(ref1).transformed(matrix).transformed(m, colour, ID);
                            } else {
                                dataCSG = linkedCSG.get(ref1).transformed(matrix, colour, ID);
                            }
                            linkedCSG.put(ref2, dataCSG);
                        }
                        break;
                    case CSG.QUALITY:
                        quality = c3d != null && c3d.getManipulator().isModified() ? 12 : global_quality;
                        break;
                    case CSG.EPSILON:
                        Plane.EPSILON = global_epsilon;
                        break;
                    case CSG.EXTRUDE_CFG:
                        globalExtruderConfig.put(df, extruderConfig);
                        break;
                    default:
                        break;
                    }
                }
            /* Is not possible anymore...
            } catch (StackOverflowError e) {
            */
            } catch (Exception e) {
                NLogger.error(getClass(), e);
            }
        }
        if (compiledCSG != null && c3d != null && doDraw) {
            if (c3d.getRenderMode() != 5) {
                compiledCSG.draw(c3d);
            } else {
                compiledCSG.draw_textured(c3d);
            }
        }
    }

    private CSG transformWithManipulator(CSG csg, Matrix4f transformation4f, Matrix4f myMatrix) {
        return csg.transformed(myMatrix).transformed(transformation4f);
    }

    @Override
    public synchronized void drawGL20(Composite3D c3d) {
        drawAndParse(c3d);
    }

    @Override
    public synchronized void drawGL20_RandomColours(Composite3D c3d) {
        drawAndParse(c3d);
    }

    @Override
    public synchronized void drawGL20_BFC(Composite3D c3d) {
        drawAndParse(c3d);
    }

    @Override
    public synchronized void drawGL20_BFCuncertified(Composite3D c3d) {
        drawAndParse(c3d);
    }

    @Override
    public synchronized void drawGL20_BFC_backOnly(Composite3D c3d) {
        drawAndParse(c3d);
    }

    @Override
    public synchronized void drawGL20_BFC_Colour(Composite3D c3d) {
        drawAndParse(c3d);
    }

    @Override
    public synchronized void drawGL20_BFC_Textured(Composite3D c3d) {
        drawAndParse(c3d);
    }

    @Override
    public synchronized void drawGL20_WhileAddCondlines(Composite3D c3d) {
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
    public synchronized String inlinedString(final byte bfc, final GColour colour) {
        try {
            static_lock.lock();
            switch (type) {
            case CSG.COMPILE:
                if (compiledCSG != null) {
                    compileAndInline.put(myDat, true);
                    resetCSG(myDat, false);
                    GDataCSG.forceRecompile(myDat);
                    GData g = myDat.getDrawChainStart();
                    deleteAndRecompile = true;
                    while ((g = g.getNext()) != null) {
                        if (g.type() == 8) {
                            GDataCSG gcsg = (GDataCSG) g;
                            gcsg.drawAndParse(null, myDat, false);
                        }
                    }
                    if (!deleteAndRecompile) {
                        return getNiceString() + "<br>0 // INLINE FAILED! :("; //$NON-NLS-1$
                    }

                    compileAndInline.put(Project.getFileToEdit(), false);
                    allNewPolygonVertices.get(myDat).clear();

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

                                TreeMap<GData3, Integer> result = compiledCSG.getResult();

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
        } finally {
            static_lock.unlock();
        }
    }

    private String floatToString(float flt) {
        String result;
        if (flt == (int) flt) {
            result = String.format("%d", (int) flt); //$NON-NLS-1$
        } else {
            result = String.format("%s", flt); //$NON-NLS-1$
        }
        if (result.equals("0.0"))result = "0"; //$NON-NLS-1$ //$NON-NLS-2$
        if (result.startsWith("-0.")) return "-" + result.substring(2); //$NON-NLS-1$ //$NON-NLS-2$
        if (result.startsWith("0.")) return result.substring(1); //$NON-NLS-1$
        return result;
    }

    @Override
    public synchronized String transformAndColourReplace(String colour2, Matrix matrix) {
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
        case CSG.MESH:
            if (notChoosen) {
                t = " CSG_MESH "; //$NON-NLS-1$
                notChoosen = false;
            }
        case CSG.EXTRUDE:
            if (notChoosen) {
                t = " CSG_EXTRUDE "; //$NON-NLS-1$
                notChoosen = false;
            }
        case CSG.TRANSFORM:
            if (notChoosen) {
                t = " CSG_TRANSFORM "; //$NON-NLS-1$
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

    public synchronized String transform(Matrix4f m) {
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
        case CSG.MESH:
            if (notChoosen) {
                t = " CSG_MESH "; //$NON-NLS-1$
                notChoosen = false;
            }
        case CSG.EXTRUDE:
            if (notChoosen) {
                t = " CSG_EXTRUDE "; //$NON-NLS-1$
                notChoosen = false;
            }
        case CSG.TRANSFORM:
            if (notChoosen) {
                t = " CSG_TRANSFORM "; //$NON-NLS-1$
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
            if (type == CSG.TRANSFORM) {
                tag = tag + " " + ref2.substring(0, ref2.lastIndexOf("#>")); //$NON-NLS-1$ //$NON-NLS-2$
                accurateLocalMatrix = accurateLocalMatrix.transpose();
                accurateLocalMatrix = accurateLocalMatrix.transposeXYZ();
            }
            return "0 !LPE" + t + tag + " " + colourBuilder.toString() + accurateLocalMatrix.toLDrawString(); //$NON-NLS-1$ //$NON-NLS-2$
        default:
            return text;
        }
    }

    public synchronized String colourReplace(String colour2) {
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
        case CSG.MESH:
            if (notChoosen) {
                t = " CSG_MESH "; //$NON-NLS-1$
                notChoosen = false;
            }
        case CSG.EXTRUDE:
            if (notChoosen) {
                t = " CSG_EXTRUDE "; //$NON-NLS-1$
                notChoosen = false;
            }
        case CSG.TRANSFORM:
            if (notChoosen) {
                t = " CSG_TRANSFORM "; //$NON-NLS-1$
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
            if (type == CSG.TRANSFORM) {
                tag = tag + " " + ref2.substring(0, ref2.lastIndexOf("#>")); //$NON-NLS-1$ //$NON-NLS-2$
            }
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
    public void getVertexNormalMap(GDataState state, ThreadsafeTreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {}
    @Override
    public void getVertexNormalMapNOCERTIFY(GDataState state, ThreadsafeTreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {}
    @Override
    public void getVertexNormalMapNOCLIP(GDataState state, ThreadsafeTreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {}

    public synchronized static boolean hasSelectionCSG(DatFile df) {
        return !selectedBodyMap.putIfAbsent(df, new HashSet<GDataCSG>()).isEmpty();
    }

    public synchronized boolean isSelected(DatFile df) {
        return selectedBodyMap.putIfAbsent(df, new HashSet<GDataCSG>()).contains(this);
    }

    public synchronized static void drawSelectionCSG(Composite3D c3d, final boolean modifiedManipulator) {
        final HashSet<GData3> selectedTriangles = selectedTrianglesMap.putIfAbsent(c3d.getLockableDatFileReference(), new HashSet<GData3>());
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
        try {
            static_lock.lock();
            final DatFile df = c3d.getLockableDatFileReference();
            final HashSet<GData3> selectedTriangles = selectedTrianglesMap.putIfAbsent(df, new HashSet<GData3>());
            if (!(c3d.getKeys().isCtrlPressed() || (Cocoa.isCocoa && c3d.getKeys().isCmdPressed()))) {
                selectedTriangles.clear();
            }
            final Integer selectedBodyID = selectCSG_helper(c3d, event);
            if (selectedBodyID != null) {
                for (Entry<String, CSG> csg_pair : linkedCSG.putIfAbsent(df, new HashMap<String, CSG>()).entrySet()) {
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
        } finally {
            static_lock.unlock();
        }
    }

    private static Integer selectCSG_helper(Composite3D c3d, Event event) {
        final PowerRay powerRay = new PowerRay();
        final DatFile df = c3d.getLockableDatFileReference();
        final HashBiMap<Integer, GData> dpl = df.getDrawPerLine_NOCLONE();
        registeredData.putIfAbsent(df, new HashSet<GDataCSG>());

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
        for (CSG csg : linkedCSG.putIfAbsent(df, new HashMap<String, CSG>()).values()) {
            for(Entry<GData3, Integer> pair : csg.getResult().entrySet()) {
                final GData3 triangle = pair.getKey();

                triQuadVerts[0] = new Vertex(triangle.x1, triangle.y1, triangle.z1);
                triQuadVerts[1] = new Vertex(triangle.x2, triangle.y2, triangle.z2);
                triQuadVerts[2] = new Vertex(triangle.x3, triangle.y3, triangle.z3);

                if (powerRay.TRIANGLE_INTERSECT(orig, rayDirection, triQuadVerts[0], triQuadVerts[1], triQuadVerts[2], point, dist)) {
                    if (dist[0] < minDist) {
                        Integer result2 = pair.getValue();
                        if (result2 != null) {
                            for (GDataCSG c : registeredData.get(df)) {
                                if (dpl.containsValue(c) && idToGDataCSG.putIfAbsent(df, new HashBiMap<Integer, GDataCSG>()).containsKey(result2)) {
                                    if (c.type == CSG.TRANSFORM && c.ref1 != null && c.ref2 != null || c.ref1 != null && c.ref2 == null && c.ref3 == null && c.type != CSG.COMPILE) {
                                        resultObj = idToGDataCSG.get(df).getValue(result2);
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
        selectedBodyMap.putIfAbsent(df, new HashSet<GDataCSG>());
        if (!(c3d.getKeys().isCtrlPressed() || (Cocoa.isCocoa && c3d.getKeys().isCmdPressed()))) {
            selectedBodyMap.get(df).clear();
        }
        selectedBodyMap.get(df).add(resultObj);
        return result;
    }

    public synchronized static HashSet<GDataCSG> getSelection(DatFile df) {
        HashSet<GDataCSG> result = selectedBodyMap.putIfAbsent(df, new HashSet<GDataCSG>());
        for (Iterator<GDataCSG> it = result.iterator(); it.hasNext();) {
            if (it.next() == null) it.remove();
        }
        return result;
    }

    public synchronized static void selectAll(DatFile df) {
        clearSelection(df);
        HashSet<GDataCSG> newSelection = new HashSet<GDataCSG>(registeredData.putIfAbsent(df, new HashSet<GDataCSG>()));
        for (Iterator<GDataCSG> it = newSelection.iterator(); it.hasNext();) {
            final GDataCSG g = it.next();
            if (g != null && g.canSelect()) {
                continue;
            }
            it.remove();
        }
        selectedBodyMap.get(df).addAll(newSelection);
    }

    public synchronized boolean canSelect() {
        if (ref1 != null && ref2 == null && ref3 == null && type != CSG.COMPILE) {
            if (ref1.endsWith("#>null") && type != CSG.QUALITY && type != CSG.EPSILON) { //$NON-NLS-1$
                return true;
            }
        }
        return false;
    }

    public synchronized static HashSet<GColour> getSelectedColours(DatFile df) {
        final HashSet<GColour> colours = new HashSet<GColour>();
        final HashSet<GDataCSG> selection = getSelection(df);
        for (GDataCSG g : selection) {
            colours.add(g.colour);
        }
        return colours;
    }

    public synchronized static void selectAllWithSameColours(DatFile df, Set<GColour> allColours) {
        HashSet<GDataCSG> newSelection = new HashSet<GDataCSG>(registeredData.putIfAbsent(df, new HashSet<GDataCSG>()));
        for (Iterator<GDataCSG> it = newSelection.iterator(); it.hasNext();) {
            final GDataCSG g = it.next();
            if (g != null && g.canSelect() && allColours.contains(g.colour)) {
                continue;
            }
            it.remove();
        }
        selectedBodyMap.get(df).addAll(newSelection);
    }

    public synchronized static void clearSelection(DatFile df) {
        selectedBodyMap.putIfAbsent(df, new HashSet<GDataCSG>()).clear();
    }

    public static void rebuildSelection(DatFile df) {
        final Composite3D c3d = df.getLastSelectedComposite();
        if (c3d == null || df.getLastSelectedComposite().isDisposed()) return;
        final HashSet<GData3> selectedTriangles = selectedTrianglesMap.putIfAbsent(df, new HashSet<GData3>());
        final HashSet<GDataCSG> selectedBodies = selectedBodyMap.get(df);
        selectedTriangles.clear();
        if (selectedBodies != null) {
            try {
                for (GDataCSG c : selectedBodies) {
                    if (c == null) {
                        selectedTriangles.clear();
                        selectedBodies.clear();
                        return;
                    }
                    if (c.dataCSG == null) {
                        selectedTriangles.clear();
                    } else {
                        for (Polygon p : c.dataCSG.getPolygons()) {
                            Matrix4f id = new Matrix4f();
                            Matrix4f.setIdentity(id);
                            GData1 g1 = new GData1(-1, .5f, .5f, .5f, 1f, id, View.ACCURATE_ID, new ArrayList<String>(), null, null, 1, false, id, View.ACCURATE_ID, null, View.DUMMY_REFERENCE, true, false,
                                    new HashSet<String>(), View.DUMMY_REFERENCE);
                            selectedTriangles.addAll(p.toLDrawTriangles(g1).keySet());
                        }
                    }
                }
                if (selectedTriangles.isEmpty()) {
                    selectedBodies.clear();
                }
            } catch (ConcurrentModificationException consumed) {

            }
        }
    }

    public synchronized Matrix4f getLDrawMatrix() {
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

    public synchronized String getRoundedString(int coordsDecimalPlaces, int matrixDecimalPlaces, final boolean onX,  final boolean onY,  final boolean onZ) {
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
        case CSG.MESH:
            if (notChoosen) {
                t = " CSG_MESH "; //$NON-NLS-1$
                notChoosen = false;
            }
        case CSG.EXTRUDE:
            if (notChoosen) {
                t = " CSG_EXTRUDE "; //$NON-NLS-1$
                notChoosen = false;
            }
        case CSG.TRANSFORM:
            if (notChoosen) {
                t = " CSG_TRANSFORM "; //$NON-NLS-1$
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
            Matrix4f.transpose(newMatrix, newMatrix);
            newMatrix.m30 = newMatrix.m03;
            newMatrix.m31 = newMatrix.m13;
            newMatrix.m32 = newMatrix.m23;
            newMatrix.m03 = 0f;
            newMatrix.m13 = 0f;
            newMatrix.m23 = 0f;
            String tag = ref1.substring(0, ref1.lastIndexOf("#>")); //$NON-NLS-1$
            if (type == CSG.TRANSFORM) {
                tag = tag + " " + ref2.substring(0, ref2.lastIndexOf("#>")); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return "0 !LPE" + t + tag + " " + colourBuilder.toString() + " " + MathHelper.matrixToString(newMatrix, coordsDecimalPlaces, matrixDecimalPlaces, onX, onY, onZ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        return null;
    }

    public static synchronized void backupSelection(DatFile linkedDatFile) {
        while (true) {
            try {
                backupSelectedBodyMap.put(linkedDatFile, new HashSet<GDataCSG>(selectedBodyMap.putIfAbsent(linkedDatFile, new HashSet<GDataCSG>())));
                backupSelectedTrianglesMap.put(linkedDatFile, new HashSet<GData3>(selectedTrianglesMap.putIfAbsent(linkedDatFile, new HashSet<GData3>())));
                break;
            } catch (ConcurrentModificationException cme) {}
        }
    }

    public static synchronized void backupSelectionClear(DatFile linkedDatFile) {
        while (true) {
            try {
                backupSelectedBodyMap.put(linkedDatFile, new HashSet<GDataCSG>());
                backupSelectedTrianglesMap.put(linkedDatFile, new HashSet<GData3>());
                break;
            } catch (ConcurrentModificationException cme) {}
        }
    }

    public static synchronized void restoreSelection(DatFile linkedDatFile) {
        while (true) {
            try {
                selectedBodyMap.put(linkedDatFile, backupSelectedBodyMap.putIfAbsent(linkedDatFile, new HashSet<GDataCSG>()));
                selectedTrianglesMap.put(linkedDatFile, backupSelectedTrianglesMap.putIfAbsent(linkedDatFile, new HashSet<GData3>()));
                break;
            } catch (ConcurrentModificationException cme) {}
        }
    }

    public static Collection<CSG> getCSGs(final DatFile df) {
        return linkedCSG.putIfAbsent(df, new HashMap<String, CSG>()).values();
    }

    public static void finishCacheCleanup(DatFile df) {
        if (clearPolygonCache.get(df) == true) {
            if (fullClearPolygonCache.get(df) != true) {
                fullClearPolygonCache.put(df, true);
                clearPolygonCache.put(df, true);
                if (allNewPolygonVertices.containsKey(df)) {
                    allNewPolygonVertices.get(df).clear();
                }
            } else {
                clearPolygonCache.put(df, false);
            }
        }
    }

    public static List<VectorCSGd[]> getNewPolyVertices(DatFile df) {
        ArrayList<VectorCSGd[]> result = new ArrayList<VectorCSGd[]>();
        if (compileAndInline.get(df)) {
            return allNewPolygonVertices.putIfAbsent(df, result);
        } else {
            return result;
        }
    }

    public static boolean isInlining(DatFile df) {
        return compileAndInline.get(df);
    }

    public int[] getDataSize() {
        final int[] result = new int[]{0, 0, 0};
        if (compiledCSG != null) {
            TreeMap<GData3, Integer> resultData = compiledCSG.getResult();
            for (GData3 tri : resultData.keySet()) {
                if (tri.a < 1f) {
                    result[2] += 6;
                } else {
                    result[1] += 6;
                }
            }
            result[0] = 60 * resultData.size();
        }
        return result;
    }

    public Set<GData3> getSurfaces() {
        if (compiledCSG == null) {
            return new HashSet<GData3>();
        } else {
            return compiledCSG.getResult().keySet();
        }
    }

    public static HashSet<GData3> getSelectionData(DatFile df) {
        return selectedTrianglesMap.putIfAbsent(df, new HashSet<GData3>());
    }
}
