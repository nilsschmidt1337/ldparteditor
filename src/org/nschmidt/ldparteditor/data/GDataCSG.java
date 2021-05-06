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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
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
import org.nschmidt.csg.CSGOptimizerEdgeCollapse;
import org.nschmidt.csg.CSGOptimizerTJunction;
import org.nschmidt.csg.CSGQuad;
import org.nschmidt.csg.CSGSphere;
import org.nschmidt.csg.IdAndPlane;
import org.nschmidt.csg.Plane;
import org.nschmidt.csg.Polygon;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.enums.Colour;
import org.nschmidt.ldparteditor.enums.LDConfig;
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.Cocoa;
import org.nschmidt.ldparteditor.helpers.LDPartEditorException;
import org.nschmidt.ldparteditor.helpers.composite3d.PathTruderSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.PerspectiveCalculator;
import org.nschmidt.ldparteditor.helpers.compositetext.Inliner;
import org.nschmidt.ldparteditor.helpers.math.HashBiMap;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.helpers.math.PowerRay;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeSortedMap;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.text.DatParser;

/**
 * @author nils
 *
 */
public final class GDataCSG extends GData {

    final byte type;

    static volatile Lock staticLock = new ReentrantLock();

    private static final ThreadsafeHashMap<DatFile, Map<String, CSG>> linkedCSG = new ThreadsafeHashMap<>();
    private static final ThreadsafeHashMap<DatFile, HashBiMap<Integer, GDataCSG>> idToGDataCSG = new ThreadsafeHashMap<>();
    private static final ThreadsafeHashMap<DatFile, Set<GData3>> selectedTrianglesMap = new ThreadsafeHashMap<>();
    private static final ThreadsafeHashMap<DatFile, Set<GDataCSG>> selectedBodyMap = new ThreadsafeHashMap<>();

    private static final ThreadsafeHashMap<DatFile, Set<GData3>> backupSelectedTrianglesMap = new ThreadsafeHashMap<>();
    private static final ThreadsafeHashMap<DatFile, Set<GDataCSG>> backupSelectedBodyMap = new ThreadsafeHashMap<>();

    private static volatile boolean deleteAndRecompile = true;

    private static final ThreadsafeHashMap<DatFile, Set<GDataCSG>> registeredData = new ThreadsafeHashMap<>();
    private static final ThreadsafeHashMap<DatFile, Set<GDataCSG>> parsedData = new ThreadsafeHashMap<>();
    private static final ThreadsafeHashMap<DatFile, PathTruderSettings> globalExtruderConfig = new ThreadsafeHashMap<>();
    private static final ThreadsafeHashMap<DatFile, Boolean> clearPolygonCache = new ThreadsafeHashMap<>();
    private static final ThreadsafeHashMap<DatFile, Boolean> fullClearPolygonCache = new ThreadsafeHashMap<>();

    private final List<GData> cachedData = new ArrayList<>();
    private final List<Polygon> polygonCache = new ArrayList<>();
    private PathTruderSettings extruderConfig = new PathTruderSettings();

    private static volatile int quality = 16;
    private int globalQuality = 16;
    private double globalEpsilon = 1e-6;

    private final String ref1;
    private final String ref2;
    private final String ref3;

    private volatile CSG compiledCSG = null;
    private CSG dataCSG = null;

    private final GColour colour;
    final Matrix4f matrix;

    public static synchronized void forceRecompile(DatFile df) {
        registeredData.putIfAbsent(df, new HashSet<>()).add(null);
        clearPolygonCache.putIfAbsent(df, true);
        Plane.epsilon = 1e-3;
    }

    static synchronized void fullReset(DatFile df) {
        quality = 16;
        registeredData.putIfAbsent(df, new HashSet<>()).clear();
        linkedCSG.putIfAbsent(df, new HashMap<>()).clear();
        parsedData.putIfAbsent(df, new HashSet<>()).clear();
        idToGDataCSG.putIfAbsent(df, new HashBiMap<>()).clear();
        selectedTrianglesMap.putIfAbsent(df, new HashSet<>()).clear();
        selectedBodyMap.putIfAbsent(df, new HashSet<>()).clear();
        backupSelectionClear(df);
        Plane.epsilon = 1e-3;
    }

    public GColour getColour() {
        return colour;
    }

    public static synchronized void resetCSG(DatFile df, boolean useLowQuality) {
        df.setOptimizingCSG(true);
        if (useLowQuality) {
            quality = 12;
        } else {
            quality = 16;
        }
        Set<GDataCSG> ref = new HashSet<>(registeredData.putIfAbsent(df, new HashSet<>()));
        ref.removeAll(parsedData.putIfAbsent(df, new HashSet<>()));
        clearPolygonCache.putIfAbsent(df, true);
        fullClearPolygonCache.putIfAbsent(df, true);
        deleteAndRecompile = !ref.isEmpty();
        if (deleteAndRecompile) {
            globalExtruderConfig.put(df, new PathTruderSettings());
            registeredData.get(df).clear();
            registeredData.get(df).add(null);
            linkedCSG.putIfAbsent(df, new HashMap<>()).clear();
            idToGDataCSG.putIfAbsent(df, new HashBiMap<>()).clear();
        }
        parsedData.get(df).clear();
    }

    public byte getCSGtype() {
        return type;
    }

    GDataCSG(DatFile df, final int colourNumber, float r, float g, float b, float a, GDataCSG c) {
        this(df, c.type, c.colourReplace(new GColour(colourNumber, r, g, b, a).toString()), c.parent);
    }

    GDataCSG(DatFile df, Matrix4f m, GDataCSG c) {
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
        registeredData.putIfAbsent(df, new HashSet<>()).add(this);
        String[] dataSegments = csgLine.trim().split("\\s+"); //$NON-NLS-1$
        final GColour col16 = LDConfig.getColour16();
        this.type = type;
        this.text = csgLine;
        switch (type) {
        case CSG.COMPILE:
            if (dataSegments.length == 4) {
                ref1 = dataSegments[3] + "#>" + parent.shortName; //$NON-NLS-1$
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
            if (dataSegments.length == 17) {
                ref1 = dataSegments[3] + "#>" + parent.shortName; //$NON-NLS-1$
                GColour c = DatParser.validateColour(dataSegments[4], col16.getR(), col16.getG(), col16.getB(), 1f);
                if (c != null) {
                    colour = c.createClone();
                } else {
                    colour = LDConfig.getColour16();
                }
                matrix = MathHelper.matrixFromStrings(dataSegments[5], dataSegments[6], dataSegments[7], dataSegments[8], dataSegments[11], dataSegments[14], dataSegments[9],
                        dataSegments[12], dataSegments[15], dataSegments[10], dataSegments[13], dataSegments[16]);
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
            if (dataSegments.length == 6) {
                ref1 = dataSegments[3] + "#>" + parent.shortName; //$NON-NLS-1$
                ref2 = dataSegments[4] + "#>" + parent.shortName; //$NON-NLS-1$
                ref3 = dataSegments[5] + "#>" + parent.shortName; //$NON-NLS-1$
            } else {
                ref1 = null;
                ref2 = null;
                ref3 = null;
            }
            colour = null;
            matrix = null;
            break;
        case CSG.TRANSFORM:
            if (dataSegments.length == 18) {
                ref1 = dataSegments[3] + "#>" + parent.shortName; //$NON-NLS-1$
                ref2 = dataSegments[4] + "#>" + parent.shortName; //$NON-NLS-1$
                GColour c = DatParser.validateColour(dataSegments[5], col16.getR(), col16.getG(), col16.getB(), 1f);
                if (c != null) {
                    colour = c.createClone();
                } else {
                    colour = LDConfig.getColour16();
                }
                matrix = MathHelper.matrixFromStrings(dataSegments[6], dataSegments[7], dataSegments[8], dataSegments[9], dataSegments[10], dataSegments[11], dataSegments[12],
                        dataSegments[13], dataSegments[14], dataSegments[15], dataSegments[16], dataSegments[17]);
            } else {
                colour = null;
                ref1 = null;
                ref2 = null;
                matrix = null;
            }
            ref3 = null;
            break;
        case CSG.QUALITY:
            if (dataSegments.length == 4) {
                try {
                    int q = Integer.parseInt(dataSegments[3]);
                    if (q > 0 && q < 49) {
                        globalQuality = q;
                    }
                } catch (NumberFormatException e) {
                }
                ref1 = dataSegments[3] + "#>" + parent.shortName; //$NON-NLS-1$
            } else {
                ref1 = null;
            }
            ref2 = null;
            ref3 = null;
            colour = null;
            matrix = null;
            break;
        case CSG.EPSILON:
            if (dataSegments.length == 4) {
                try {
                    double q = Double.parseDouble(dataSegments[3]);
                    if (q > 0d) {
                        globalEpsilon = q;
                    }
                } catch (NumberFormatException e) {
                }
                ref1 = dataSegments[3] + "#>" + parent.shortName; //$NON-NLS-1$
            } else {
                ref1 = null;
            }
            ref2 = null;
            ref3 = null;
            colour = null;
            matrix = null;
            break;
        case CSG.TJUNCTION:
            if (dataSegments.length == 4) {
                try {
                    double q = Double.parseDouble(dataSegments[3]);
                    if (q > 0d) {
                        CSGOptimizerTJunction.epsilon = q;
                    }
                } catch (NumberFormatException e) {
                }
                ref1 = dataSegments[3] + "#>" + parent.shortName; //$NON-NLS-1$
            } else {
                ref1 = null;
            }
            ref2 = null;
            ref3 = null;
            colour = null;
            matrix = null;
            break;
        case CSG.COLLAPSE:
            if (dataSegments.length == 4) {
                try {
                    double q = Double.parseDouble(dataSegments[3]);
                    if (q > 0d && q <= 1d) {
                        CSGOptimizerEdgeCollapse.epsilon = q;
                    }
                } catch (NumberFormatException e) {
                }
                ref1 = dataSegments[3] + "#>" + parent.shortName; //$NON-NLS-1$
            } else {
                ref1 = null;
            }
            ref2 = null;
            ref3 = null;
            colour = null;
            matrix = null;
            break;
        case CSG.EXTRUDE_CFG:
            if (dataSegments.length == 4 && "DEFAULT".equals(dataSegments[3])) { //$NON-NLS-1$
                extruderConfig = new PathTruderSettings();
                ref1 = dataSegments[3] + "#>" + parent.shortName; //$NON-NLS-1$
            } else if (dataSegments.length == 17) {
                try {
                    extruderConfig.setMaxPathSegmentLength(new BigDecimal(dataSegments[4]));
                    extruderConfig.setTransitionCount(Integer.parseInt(dataSegments[6]));
                    extruderConfig.setTransitionCurveControl(new BigDecimal(dataSegments[8]));
                    extruderConfig.setTransitionCurveCenter(new BigDecimal(dataSegments[10]));
                    extruderConfig.setRotation(new BigDecimal(dataSegments[12]));
                    extruderConfig.setCompensation(Boolean.parseBoolean(dataSegments[14]));
                    extruderConfig.setInverted(Boolean.parseBoolean(dataSegments[16]));
                } catch (Exception ex) {
                    extruderConfig = new PathTruderSettings();
                }
                ref1 = dataSegments[3] + "#>" + parent.shortName; //$NON-NLS-1$
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

    void drawAndParse(Composite3D c3d, DatFile df, boolean doDraw) {

        if (type == CSG.DONTOPTIMIZE) {
            df.setOptimizingCSG(false);
        }

        final boolean clearCaches = clearPolygonCache.putIfAbsent(df, true)
                || type == CSG.MESH && CSGMesh.needCacheRefresh(cachedData, this, df)
                || type == CSG.EXTRUDE && CSGExtrude.needCacheRefresh(cachedData, this, df);

        if (clearCaches) {
            clearPolygonCache.put(df, true);
        }
        final Set<GDataCSG> tmpParsedData = GDataCSG.parsedData.putIfAbsent(df, new HashSet<>());
        tmpParsedData.add(this);
        final boolean modified = c3d != null && c3d.getManipulator().isModified();
        if (deleteAndRecompile || modified || clearCaches) {
            final HashBiMap<Integer, GDataCSG> tmpIdToGDataCSG = GDataCSG.idToGDataCSG.putIfAbsent(df, new HashBiMap<>());
            final Map<String, CSG> tmpLinkedCSG = GDataCSG.linkedCSG.putIfAbsent(df, new HashMap<>());
            final Set<GDataCSG> tmpRegisteredData = GDataCSG.registeredData.putIfAbsent(df, new HashSet<>());
            final Matrix4f m;
            if (modified) {
                m = c3d.getManipulator().getTempTransformationCSG4f();
            } else {
                m = View.ID;
            }
            tmpRegisteredData.remove(null);
            try {
                compiledCSG = null;
                tmpRegisteredData.add(this);
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
                                tmpIdToGDataCSG.put(quad.id, this);
                                CSG csgQuad = quad.toCSG(df, colour);
                                if (modified && isSelected(df)) {
                                    csgQuad = transformWithManipulator(csgQuad, m, matrix);
                                } else {
                                    csgQuad = csgQuad.transformed(matrix);
                                }
                                dataCSG = csgQuad;
                                tmpLinkedCSG.put(ref1, csgQuad);
                                break;
                            case CSG.CIRCLE:
                                CSGCircle circle = new CSGCircle(quality);
                                tmpIdToGDataCSG.put(circle.id, this);
                                CSG csgCircle = circle.toCSG(df, colour);
                                if (modified && isSelected(df)) {
                                    csgCircle = transformWithManipulator(csgCircle, m, matrix);
                                } else {
                                    csgCircle = csgCircle.transformed(matrix);
                                }
                                dataCSG = csgCircle;
                                tmpLinkedCSG.put(ref1, csgCircle);
                                break;
                            case CSG.ELLIPSOID:
                                CSGSphere sphere = new CSGSphere(quality, quality / 2);
                                tmpIdToGDataCSG.put(sphere.id, this);
                                CSG csgSphere = sphere.toCSG(df, colour);
                                if (modified && isSelected(df)) {
                                    csgSphere = transformWithManipulator(csgSphere, m, matrix);
                                } else {
                                    csgSphere = csgSphere.transformed(matrix);
                                }
                                dataCSG = csgSphere;
                                tmpLinkedCSG.put(ref1, csgSphere);
                                break;
                            case CSG.CUBOID:
                                CSGCube cube = new CSGCube();
                                tmpIdToGDataCSG.put(cube.id, this);
                                CSG csgCube = cube.toCSG(df, colour);
                                if (modified && isSelected(df)) {
                                    csgCube = transformWithManipulator(csgCube, m, matrix);
                                } else {
                                    csgCube = csgCube.transformed(matrix);
                                }
                                dataCSG = csgCube;
                                tmpLinkedCSG.put(ref1, csgCube);
                                break;
                            case CSG.CYLINDER:
                                CSGCylinder cylinder = new CSGCylinder(quality);
                                tmpIdToGDataCSG.put(cylinder.id, this);
                                CSG csgCylinder = cylinder.toCSG(df, colour);
                                if (modified && isSelected(df)) {
                                    csgCylinder = transformWithManipulator(csgCylinder, m, matrix);
                                } else {
                                    csgCylinder = csgCylinder.transformed(matrix);
                                }
                                dataCSG = csgCylinder;
                                tmpLinkedCSG.put(ref1, csgCylinder);
                                break;
                            case CSG.CONE:
                                CSGCone cone = new CSGCone(quality);
                                tmpIdToGDataCSG.put(cone.id, this);
                                CSG csgCone = cone.toCSG(df, colour);
                                if (modified && isSelected(df)) {
                                    csgCone = transformWithManipulator(csgCone, m, matrix);
                                } else {
                                    csgCone = csgCone.transformed(matrix);
                                }
                                dataCSG = csgCone;
                                tmpLinkedCSG.put(ref1, csgCone);
                                break;
                            case CSG.MESH:
                                if (clearCaches) {
                                    polygonCache.clear();
                                }
                                CSGMesh mesh = new CSGMesh(this, cachedData, polygonCache);
                                CSGMesh.fillCache(cachedData, this);
                                CSG csgMesh = mesh.toCSG(df, colour);
                                tmpIdToGDataCSG.put(mesh.id, this);
                                if (modified && isSelected(df)) {
                                    csgMesh = transformWithManipulator(csgMesh, m, matrix);
                                } else {
                                    csgMesh = csgMesh.transformed(matrix);
                                }
                                dataCSG = csgMesh;
                                tmpLinkedCSG.put(ref1, csgMesh);
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
                                tmpIdToGDataCSG.put(extruder.id, this);
                                if (modified && isSelected(df)) {
                                    csgExtruder = transformWithManipulator(csgExtruder, m, matrix);
                                } else {
                                    csgExtruder = csgExtruder.transformed(matrix);
                                }
                                dataCSG = csgExtruder;
                                tmpLinkedCSG.put(ref1, csgExtruder);
                                break;
                            default:
                                break;
                            }
                        }
                        break;
                    case CSG.COMPILE:
                        if (tmpLinkedCSG.containsKey(ref1)) {
                            compiledCSG = tmpLinkedCSG.get(ref1);
                            compiledCSG.compile();
                        } else {
                            compiledCSG = null;
                        }
                        break;
                    case CSG.DIFFERENCE:
                        if (tmpLinkedCSG.containsKey(ref1) && tmpLinkedCSG.containsKey(ref2)) {
                            tmpLinkedCSG.put(ref3, tmpLinkedCSG.get(ref1).difference(tmpLinkedCSG.get(ref2)));
                        }
                        break;
                    case CSG.INTERSECTION:
                        if (tmpLinkedCSG.containsKey(ref1) && tmpLinkedCSG.containsKey(ref2)) {
                            tmpLinkedCSG.put(ref3, tmpLinkedCSG.get(ref1).intersect(tmpLinkedCSG.get(ref2)));
                        }
                        break;
                    case CSG.UNION:
                        if (tmpLinkedCSG.containsKey(ref1) && tmpLinkedCSG.containsKey(ref2)) {
                            tmpLinkedCSG.put(ref3, tmpLinkedCSG.get(ref1).union(tmpLinkedCSG.get(ref2)));
                        }
                        break;
                    case CSG.TRANSFORM:
                        if (tmpLinkedCSG.containsKey(ref1) && matrix != null) {
                            tmpIdToGDataCSG.put(id, this);
                            if (modified && isSelected(df)) {
                                dataCSG = tmpLinkedCSG.get(ref1).transformed(matrix).transformed(m, colour, id);
                            } else {
                                dataCSG = tmpLinkedCSG.get(ref1).transformed(matrix, colour, id);
                            }
                            tmpLinkedCSG.put(ref2, dataCSG);
                        }
                        break;
                    case CSG.QUALITY:
                        quality = c3d != null && c3d.getManipulator().isModified() ? Math.min(globalQuality, 12) : globalQuality;
                        break;
                    case CSG.EPSILON:
                        Plane.epsilon = globalEpsilon;
                        break;
                    case CSG.EXTRUDE_CFG:
                        globalExtruderConfig.put(df, extruderConfig);
                        break;
                    default:
                        break;
                    }
                }
            } catch (Exception e) {
                // StackOverflowError is not possible anymore...
                NLogger.error(getClass(), e);
            }
        }
        if (compiledCSG != null && c3d != null && doDraw) {
            if (c3d.getRenderMode() != 5) {
                compiledCSG.draw(c3d, df);
            } else {
                compiledCSG.drawTextured(c3d, df);
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
    public synchronized void drawGL20RandomColours(Composite3D c3d) {
        drawAndParse(c3d);
    }

    @Override
    public synchronized void drawGL20BFC(Composite3D c3d) {
        drawAndParse(c3d);
    }

    @Override
    public synchronized void drawGL20BFCuncertified(Composite3D c3d) {
        drawAndParse(c3d);
    }

    @Override
    public synchronized void drawGL20BFCbackOnly(Composite3D c3d) {
        drawAndParse(c3d);
    }

    @Override
    public synchronized void drawGL20BFCcolour(Composite3D c3d) {
        drawAndParse(c3d);
    }

    @Override
    public synchronized void drawGL20BFCtextured(Composite3D c3d) {
        drawAndParse(c3d);
    }

    @Override
    public synchronized void drawGL20WhileAddCondlines(Composite3D c3d) {
        drawAndParse(c3d);
    }

    @Override
    public void drawGL20CoplanarityHeatmap(Composite3D c3d) {
        drawAndParse(c3d);
    }

    @Override
    public void drawGL20Wireframe(Composite3D c3d) {
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

    boolean wasNotCompiled() {
        return (CSG.COMPILE == type && compiledCSG == null);
    }

    @Override
    public synchronized String inlinedString(final BFC bfc, final GColour colour) {
        staticLock.lock();
        try {
            if (type == CSG.COMPILE) {

                if (compiledCSG == null) {
                    // Try to do a rebuild
                    final List<GDataCSG> csgData = new ArrayList<>();
                    final DatFile df = Inliner.datfile;
                    GDataCSG.resetCSG(df, true);
                    GData g = df.getDrawChainStart();
                    while ((g = g.next) != null) {
                        if (g instanceof GDataCSG) {
                            csgData.add((GDataCSG) g);
                        }
                    }
                    for (GDataCSG csg : csgData) {
                        csg.drawAndParse(null, df, false);
                    }
                }

                if (compiledCSG != null) {

                    final StringBuilder sb = new StringBuilder();

                    try {
                        new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(false, false, new IRunnableWithProgress() {

                            @Override
                            public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                                Editor3DWindow.getWindow().getShell().getDisplay().readAndDispatch();
                                Object[] messageArguments = {getNiceString()};
                                MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                                formatter.setLocale(MyLanguage.getLocale());
                                formatter.applyPattern(I18n.DATFILE_INLINED);

                                sb.append(formatter.format(messageArguments) + "<br>"); //$NON-NLS-1$

                                SortedMap<GData3, IdAndPlane> result = compiledCSG.getResult(null);

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
                                    Vector4f g3V1 = new Vector4f(g3.x1, g3.y1, g3.z1, 1f);
                                    Vector4f g3V2 = new Vector4f(g3.x2, g3.y2, g3.z2, 1f);
                                    Vector4f g3V3 = new Vector4f(g3.x3, g3.y3, g3.z3, 1f);
                                    lineBuilder3.append(" "); //$NON-NLS-1$
                                    lineBuilder3.append(floatToString(g3V1.x / 1000f));
                                    lineBuilder3.append(" "); //$NON-NLS-1$
                                    lineBuilder3.append(floatToString(g3V1.y / 1000f));
                                    lineBuilder3.append(" "); //$NON-NLS-1$
                                    lineBuilder3.append(floatToString(g3V1.z / 1000f));
                                    lineBuilder3.append(" "); //$NON-NLS-1$
                                    lineBuilder3.append(floatToString(g3V2.x / 1000f));
                                    lineBuilder3.append(" "); //$NON-NLS-1$
                                    lineBuilder3.append(floatToString(g3V2.y / 1000f));
                                    lineBuilder3.append(" "); //$NON-NLS-1$
                                    lineBuilder3.append(floatToString(g3V2.z / 1000f));
                                    lineBuilder3.append(" "); //$NON-NLS-1$
                                    lineBuilder3.append(floatToString(g3V3.x / 1000f));
                                    lineBuilder3.append(" "); //$NON-NLS-1$
                                    lineBuilder3.append(floatToString(g3V3.y / 1000f));
                                    lineBuilder3.append(" "); //$NON-NLS-1$
                                    lineBuilder3.append(floatToString(g3V3.z / 1000f));
                                    sb.append(lineBuilder3.toString() + "<br>"); //$NON-NLS-1$
                                }
                            }
                        });
                    } catch (InvocationTargetException consumed) {
                        consumed.printStackTrace();
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new LDPartEditorException(ie);
                    }
                    return sb.toString();
                } else {
                    return getNiceString();
                }
            } else {
                return getNiceString();
            }
        } finally {
            staticLock.unlock();
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
            t = " CSG_QUAD "; //$NON-NLS-1$
            notChoosen = false;
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

    private synchronized String transform(Matrix4f m) {
        boolean notChoosen = true;
        String t = null;
        switch (type) {
        case CSG.QUAD:
            t = " CSG_QUAD "; //$NON-NLS-1$
            notChoosen = false;
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
            BigDecimal tx = accurateLocalMatrix.m30.add(BigDecimal.ZERO);
            BigDecimal ty = accurateLocalMatrix.m31.add(BigDecimal.ZERO);
            BigDecimal tz = accurateLocalMatrix.m32.add(BigDecimal.ZERO);
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

    private synchronized String colourReplace(String colour2) {
        boolean notChoosen = true;
        String t = null;
        switch (type) {
        case CSG.QUAD:
            t = " CSG_QUAD "; //$NON-NLS-1$
            notChoosen = false;
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
    public void getBFCorientationMap(Map<GData,BFC> map) {
        // Implementation is not required.
    }

    @Override
    public void getBFCorientationMapNOCERTIFY(Map<GData, BFC> map) {
        // Implementation is not required.
    }

    @Override
    public void getBFCorientationMapNOCLIP(Map<GData, BFC> map) {
        // Implementation is not required.
    }

    @Override
    public void getVertexNormalMap(GDataState state, ThreadsafeSortedMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {
        // Implementation is not required.
    }

    @Override
    public void getVertexNormalMapNOCERTIFY(GDataState state, ThreadsafeSortedMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {
        // Implementation is not required.
    }

    @Override
    public void getVertexNormalMapNOCLIP(GDataState state, ThreadsafeSortedMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {
        // Implementation is not required.
    }


    static synchronized boolean hasSelectionCSG(DatFile df) {
        return !selectedBodyMap.putIfAbsent(df, new HashSet<>()).isEmpty();
    }

    private synchronized boolean isSelected(DatFile df) {
        return selectedBodyMap.putIfAbsent(df, new HashSet<>()).contains(this);
    }

    static synchronized void drawSelectionCSG(Composite3D c3d) {
        final Set<GData3> selectedTriangles = selectedTrianglesMap.putIfAbsent(c3d.getLockableDatFileReference(), new HashSet<>());
        if (!selectedTriangles.isEmpty()) {
            GL11.glColor3f(Colour.vertexSelectedColourR, Colour.vertexSelectedColourG, Colour.vertexSelectedColourB);
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
        staticLock.lock();
        try {
            final DatFile df = c3d.getLockableDatFileReference();
            final Set<GData3> selectedTriangles = selectedTrianglesMap.putIfAbsent(df, new HashSet<>());
            if (!(c3d.getKeys().isCtrlPressed() || (Cocoa.IS_COCOA && c3d.getKeys().isCmdPressed()))) {
                selectedTriangles.clear();
            }
            final Integer selectedBodyID = selectCSGhelper(c3d, event);
            if (selectedBodyID != null) {
                for (Entry<String, CSG> csg_pair : linkedCSG.putIfAbsent(df, new HashMap<>()).entrySet()) {
                    if (csg_pair.getKey() != null && csg_pair.getKey().endsWith("#>null")) { //$NON-NLS-1$
                        CSG csg = csg_pair.getValue();
                        if (csg != null) {
                            for(Entry<GData3, IdAndPlane> pair : csg.getResult(df).entrySet()) {
                                if (selectedBodyID.equals(pair.getValue().id)) {
                                    selectedTriangles.add(pair.getKey());
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            staticLock.unlock();
        }
    }

    private static Integer selectCSGhelper(Composite3D c3d, Event event) {
        final PowerRay powerRay = new PowerRay();
        final DatFile df = c3d.getLockableDatFileReference();
        final HashBiMap<Integer, GData> dpl = df.getDrawPerLineNoClone();
        registeredData.putIfAbsent(df, new HashSet<>());

        PerspectiveCalculator perspective = c3d.getPerspectiveCalculator();
        Matrix4f viewportRotation = c3d.getRotation();
        Vector4f zAxis4f = new Vector4f(0, 0, -1f, 1f);
        Matrix4f ovrInverse2 = Matrix4f.invert(viewportRotation, null);
        Matrix4f.transform(ovrInverse2, zAxis4f, zAxis4f);
        Vector4f rayDirection = (Vector4f) new Vector4f(zAxis4f.x, zAxis4f.y, zAxis4f.z, 0f).normalise();
        rayDirection.w = 1f;

        Vertex[] triQuadVerts = new Vertex[3];

        Vector4f orig = perspective.get3DCoordinatesFromScreen(event.x, event.y);
        Vector4f point = new Vector4f(orig);

        double minDist = Double.MAX_VALUE;
        final double[] dist = new double[1];
        Integer result = null;
        GDataCSG resultObj = null;
        for (CSG csg : linkedCSG.putIfAbsent(df, new HashMap<>()).values()) {
            for(Entry<GData3, IdAndPlane> pair : csg.getResult(df).entrySet()) {
                final GData3 triangle = pair.getKey();

                triQuadVerts[0] = new Vertex(triangle.x1, triangle.y1, triangle.z1);
                triQuadVerts[1] = new Vertex(triangle.x2, triangle.y2, triangle.z2);
                triQuadVerts[2] = new Vertex(triangle.x3, triangle.y3, triangle.z3);

                if (powerRay.triangleIntersect(orig, rayDirection, triQuadVerts[0], triQuadVerts[1], triQuadVerts[2], point, dist) && dist[0] < minDist) {
                    Integer result2 = pair.getValue().id;
                    if (result2 != null) {
                        for (GDataCSG c : registeredData.get(df)) {
                            if (dpl.containsValue(c) && idToGDataCSG.putIfAbsent(df, new HashBiMap<>()).containsKey(result2) && (c.type == CSG.TRANSFORM && c.ref1 != null && c.ref2 != null || c.ref1 != null && c.ref2 == null && c.ref3 == null && c.type != CSG.COMPILE)) {
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
        selectedBodyMap.putIfAbsent(df, new HashSet<>());
        if (!(c3d.getKeys().isCtrlPressed() || (Cocoa.IS_COCOA && c3d.getKeys().isCmdPressed()))) {
            selectedBodyMap.get(df).clear();
        }
        selectedBodyMap.get(df).add(resultObj);
        return result;
    }

    public static synchronized Set<GDataCSG> getSelection(DatFile df) {
        Set<GDataCSG> result = selectedBodyMap.putIfAbsent(df, new HashSet<>());
        for (Iterator<GDataCSG> it = result.iterator(); it.hasNext();) {
            if (it.next() == null) it.remove();
        }
        return result;
    }

    static synchronized void selectAll(DatFile df) {
        clearSelection(df);
        Set<GDataCSG> newSelection = new HashSet<>(registeredData.putIfAbsent(df, new HashSet<>()));
        for (Iterator<GDataCSG> it = newSelection.iterator(); it.hasNext();) {
            final GDataCSG g = it.next();
            if (g != null && g.canSelect()) {
                continue;
            }
            it.remove();
        }
        selectedBodyMap.get(df).addAll(newSelection);
    }

    synchronized boolean canSelect() {
        return ref1 != null && ref2 == null && ref3 == null && type != CSG.COMPILE && ref1.endsWith("#>null") && type != CSG.QUALITY && type != CSG.EPSILON && type != CSG.TJUNCTION && type != CSG.COLLAPSE && type != CSG.DONTOPTIMIZE; //$NON-NLS-1$
    }

    static synchronized Set<GColour> getSelectedColours(DatFile df) {
        final Set<GColour> colours = new HashSet<>();
        final Set<GDataCSG> selection = getSelection(df);
        for (GDataCSG g : selection) {
            colours.add(g.colour);
        }
        return colours;
    }

    static synchronized void selectAllWithSameColours(DatFile df, Set<GColour> allColours) {
        Set<GDataCSG> newSelection = new HashSet<>(registeredData.putIfAbsent(df, new HashSet<>()));
        for (Iterator<GDataCSG> it = newSelection.iterator(); it.hasNext();) {
            final GDataCSG g = it.next();
            if (g != null && g.canSelect() && allColours.contains(g.colour)) {
                continue;
            }
            it.remove();
        }
        selectedBodyMap.get(df).addAll(newSelection);
    }

    public static synchronized void clearSelection(DatFile df) {
        selectedBodyMap.putIfAbsent(df, new HashSet<>()).clear();
    }

    static void rebuildSelection(DatFile df) {
        final Composite3D c3d = df.getLastSelectedComposite();
        if (c3d == null || df.getLastSelectedComposite().isDisposed()) return;
        final Set<GData3> selectedTriangles = selectedTrianglesMap.putIfAbsent(df, new HashSet<>());
        final Set<GDataCSG> selectedBodies = selectedBodyMap.get(df);
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
                            GData1 g1 = new GData1(-1, .5f, .5f, .5f, 1f, id, View.ACCURATE_ID, new ArrayList<>(), null, null, 1, false, id, View.ACCURATE_ID, null, View.DUMMY_REFERENCE, true, false,
                                    new HashSet<>(), View.DUMMY_REFERENCE);
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

    synchronized String getRoundedString(int coordsDecimalPlaces, int matrixDecimalPlaces, final boolean onX,  final boolean onY,  final boolean onZ) {
        boolean notChoosen = true;
        String t = null;
        switch (type) {
        case CSG.QUAD:
            t = " CSG_QUAD "; //$NON-NLS-1$
            notChoosen = false;
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
        default:
            break;
        }
        return null;
    }

    static synchronized void backupSelection(DatFile linkedDatFile) {
        while (true) {
            try {
                backupSelectedBodyMap.put(linkedDatFile, new HashSet<>(selectedBodyMap.putIfAbsent(linkedDatFile, new HashSet<>())));
                backupSelectedTrianglesMap.put(linkedDatFile, new HashSet<>(selectedTrianglesMap.putIfAbsent(linkedDatFile, new HashSet<>())));
                break;
            } catch (ConcurrentModificationException cme) {}
        }
    }

    static synchronized void backupSelectionClear(DatFile linkedDatFile) {
        while (true) {
            try {
                backupSelectedBodyMap.put(linkedDatFile, new HashSet<>());
                backupSelectedTrianglesMap.put(linkedDatFile, new HashSet<>());
                break;
            } catch (ConcurrentModificationException cme) {}
        }
    }

    static synchronized void restoreSelection(DatFile linkedDatFile) {
        while (true) {
            try {
                selectedBodyMap.put(linkedDatFile, backupSelectedBodyMap.putIfAbsent(linkedDatFile, new HashSet<>()));
                selectedTrianglesMap.put(linkedDatFile, backupSelectedTrianglesMap.putIfAbsent(linkedDatFile, new HashSet<>()));
                break;
            } catch (ConcurrentModificationException cme) {}
        }
    }

    static Collection<CSG> getCSGs(final DatFile df) {
        return linkedCSG.putIfAbsent(df, new HashMap<>()).values();
    }

    static void finishCacheCleanup(DatFile df) {
        if (Boolean.TRUE.equals(clearPolygonCache.getOrDefault(df, false))) {
            if (Boolean.FALSE.equals(fullClearPolygonCache.getOrDefault(df, false))) {
                fullClearPolygonCache.put(df, true);
                clearPolygonCache.put(df, true);
            } else {
                clearPolygonCache.put(df, false);
            }
        }
    }

    public int[] getDataSize() {
        if (datasize != null) {
            return datasize;
        }
        return new int[]{0, 0, 0};
    }

    public Set<GData3> getSurfaces() {
        if (surfaces == null) {
            return new HashSet<>();
        } else {
            return surfaces;
        }
    }

    static Set<GData3> getSelectionData(DatFile df) {
        return selectedTrianglesMap.putIfAbsent(df, new HashSet<>());
    }

    private Set<GData3> surfaces = null;
    private int[] datasize = null;
    void cacheResult(DatFile df) {
        final int[] result = new int[]{0, 0, 0};
        if (compiledCSG != null) {
            surfaces = new HashSet<>(compiledCSG.getResult(df).keySet());
            for (GData3 tri : surfaces) {
                if (tri.a < 1f) {
                    result[2] += 6;
                } else {
                    result[1] += 6;
                }
            }
            result[0] = 60 * surfaces.size();
        } else {
            surfaces = new HashSet<>();
        }
        datasize = result;
    }
}
