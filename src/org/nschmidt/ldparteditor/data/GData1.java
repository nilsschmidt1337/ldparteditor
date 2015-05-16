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
import java.math.RoundingMode;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.swt.graphics.Rectangle;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.csg.CSG;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.composite3d.PerspectiveCalculator;
import org.nschmidt.ldparteditor.helpers.composite3d.ViewIdleManager;
import org.nschmidt.ldparteditor.helpers.compositetext.Inliner;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.text.DatParser;
import org.nschmidt.ldparteditor.text.TexMapParser;

/**
 * @author nils
 *
 */
public final class GData1 extends GData {

    private static Set<String> filesWithLogo1 = new HashSet<String>();
    private static Set<String> filesWithLogo2 = new HashSet<String>();

    static {
        filesWithLogo1.add("STUD.DAT"); //$NON-NLS-1$
        filesWithLogo1.add("STUD.dat"); //$NON-NLS-1$
        filesWithLogo1.add("stud.dat"); //$NON-NLS-1$
        filesWithLogo2.add("STUD2.DAT"); //$NON-NLS-1$
        filesWithLogo2.add("STUD2.dat"); //$NON-NLS-1$
        filesWithLogo2.add("stud2.dat"); //$NON-NLS-1$
    }

    public final int colourNumber;

    public final float r;
    public final float g;
    public final float b;
    public final float a;

    final FloatBuffer matrix;
    final Matrix4f productMatrix;
    final Matrix4f localMatrix;
    final Matrix accurateProductMatrix;
    final Matrix accurateLocalMatrix;

    final String name;
    final String shortName;

    final boolean readOnly;

    final Vector4f boundingBoxMin = new Vector4f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, 1f);
    final Vector4f boundingBoxMax = new Vector4f(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE, 1f);

    final boolean negativeDeterminant;

    boolean recursive = false;
    boolean movedTo = false;

    final GData myGData = new GDataInit(this);

    public final GData1 firstRef;
    public final GData1 parent;

    final int depth;

    public GData1() {
        this.colourNumber = 0;
        this.r = 0;
        this.g = 0;
        this.b = 0;
        this.a = 0;
        this.firstRef = this;
        this.parent = View.DUMMY_REFERENCE;
        this.depth = 0;
        this.matrix = null;
        this.productMatrix = View.ID;
        this.localMatrix = View.ID;
        this.accurateProductMatrix = View.ACCURATE_ID;
        this.accurateLocalMatrix = View.ACCURATE_ID;
        this.negativeDeterminant = false;
        this.name = null;
        this.shortName = null;
        this.readOnly = false;
    }

    public GData1(int colourNumber, float r, float g, float b, float a, Matrix4f tMatrix, Matrix TMatrix, ArrayList<String> lines, String name, String shortName, int depth, boolean det,
            Matrix4f pMatrix, Matrix PMatrix, DatFile datFile, GData1 firstRef, boolean readOnly, boolean errorCheckOnly, Set<String> alreadyParsed, GData1 parent) {
        this.parent = parent;
        depth++;
        if (depth < 16) {
            if (depth == 1) {
                this.firstRef = this;
                this.readOnly = readOnly;
            } else {
                this.firstRef = firstRef.firstRef;
                this.readOnly = firstRef.readOnly;
            }

            this.depth = depth;

            negativeDeterminant = det;
            this.colourNumber = colourNumber;
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
            this.name = name;
            this.shortName = shortName;
            this.productMatrix = new Matrix4f(pMatrix);
            this.localMatrix = new Matrix4f(tMatrix);
            this.accurateProductMatrix = PMatrix;
            this.accurateLocalMatrix = TMatrix;
            matrix = BufferUtils.createFloatBuffer(16);
            tMatrix.store(matrix);

            matrix.position(0);

            if (!GData.CACHE_parsedFilesSource.containsKey(name)) {
                GData.CACHE_parsedFilesSource.put(name, lines);
            }

            StringBuilder keyBuilder = new StringBuilder();
            keyBuilder.append(name);
            keyBuilder.append(r);
            keyBuilder.append(g);
            keyBuilder.append(b);
            keyBuilder.append(det);
            String key = keyBuilder.toString();

            GData anchorData = myGData;

            for (String line : lines) {
                if (isNotBlank(line)) {

                    StringBuilder keyBuilder2 = new StringBuilder();
                    keyBuilder2.append(key);
                    keyBuilder2.append(line);
                    keyBuilder2.append(colourNumber);
                    String key3 = keyBuilder2.toString();

                    if (GData.parsedLines.containsKey(key3)) {
                        GData gdata = GData.parsedLines.get(key3);
                        final GData res_gdata;
                        switch (gdata.type()) {
                        case 0:
                            res_gdata = new GData0(line);
                            break;
                        case 1:
                            GData1 gd1 = (GData1) gdata;
                            alreadyParsed.add(gd1.shortName);
                            GData1 newGdata1 = new GData1(gd1.colourNumber, gd1.r, gd1.g, gd1.b, gd1.a, new Matrix4f(gd1.localMatrix), gd1.accurateLocalMatrix,
                                    GData.CACHE_parsedFilesSource.get(gd1.name), gd1.name, gd1.shortName, depth, gd1.negativeDeterminant, Matrix4f.mul(this.productMatrix, gd1.localMatrix, null),
                                    Matrix.mul(this.accurateProductMatrix, gd1.accurateLocalMatrix), datFile, this.firstRef, false, errorCheckOnly, alreadyParsed, this);
                            alreadyParsed.remove(gd1.shortName);
                            this.boundingBoxMin.x = Math.min(this.boundingBoxMin.x, newGdata1.boundingBoxMin.x);
                            this.boundingBoxMin.y = Math.min(this.boundingBoxMin.y, newGdata1.boundingBoxMin.y);
                            this.boundingBoxMin.z = Math.min(this.boundingBoxMin.z, newGdata1.boundingBoxMin.z);
                            this.boundingBoxMin.x = Math.min(this.boundingBoxMin.x, newGdata1.boundingBoxMax.x);
                            this.boundingBoxMin.y = Math.min(this.boundingBoxMin.y, newGdata1.boundingBoxMax.y);
                            this.boundingBoxMin.z = Math.min(this.boundingBoxMin.z, newGdata1.boundingBoxMax.z);
                            this.boundingBoxMax.x = Math.max(this.boundingBoxMax.x, newGdata1.boundingBoxMin.x);
                            this.boundingBoxMax.y = Math.max(this.boundingBoxMax.y, newGdata1.boundingBoxMin.y);
                            this.boundingBoxMax.z = Math.max(this.boundingBoxMax.z, newGdata1.boundingBoxMin.z);
                            this.boundingBoxMax.x = Math.max(this.boundingBoxMax.x, newGdata1.boundingBoxMax.x);
                            this.boundingBoxMax.y = Math.max(this.boundingBoxMax.y, newGdata1.boundingBoxMax.y);
                            this.boundingBoxMax.z = Math.max(this.boundingBoxMax.z, newGdata1.boundingBoxMax.z);
                            res_gdata = newGdata1;
                            break;
                        case 2:
                            GData2 gd2 = (GData2) gdata;
                            GData2 newGdata2 = new GData2(this, gd2.colourNumber, gd2.r, gd2.g, gd2.b, gd2.a, gd2.X1, gd2.Y1, gd2.Z1, gd2.X2, gd2.Y2, gd2.Z2, gd2.x1, gd2.y1, gd2.z1, gd2.x2, gd2.y2,
                                    gd2.z2, datFile);
                            res_gdata = newGdata2;
                            break;
                        case 3:
                            GData3 gd3 = (GData3) gdata;
                            GData3 newGdata3 = new GData3(gd3.colourNumber, gd3.r, gd3.g, gd3.b, gd3.a, gd3.X1, gd3.Y1, gd3.Z1, gd3.X2, gd3.Y2, gd3.Z2, gd3.X3, gd3.Y3, gd3.Z3, gd3.x1, gd3.y1, gd3.z1,
                                    gd3.x2, gd3.y2, gd3.z2, gd3.x3, gd3.y3, gd3.z3, gd3.xn, gd3.yn, gd3.zn, this, datFile);
                            res_gdata = newGdata3;
                            break;
                        case 4:
                            GData4 gd4 = (GData4) gdata;
                            GData4 newGdata4 = new GData4(gd4.colourNumber, gd4.r, gd4.g, gd4.b, gd4.a, gd4.X1, gd4.Y1, gd4.Z1, gd4.X2, gd4.Y2, gd4.Z2, gd4.X3, gd4.Y3, gd4.Z3, gd4.X4, gd4.Y4, gd4.Z4,
                                    gd4.x1, gd4.y1, gd4.z1, gd4.x2, gd4.y2, gd4.z2, gd4.x3, gd4.y3, gd4.z3, gd4.x4, gd4.y4, gd4.z4, gd4.xn, gd4.yn, gd4.zn, this, datFile);
                            res_gdata = newGdata4;
                            break;
                        case 5:
                            GData5 gd5 = (GData5) gdata;
                            GData5 newGdata5 = new GData5(true, gd5.colourNumber, gd5.r, gd5.g, gd5.b, gd5.a, gd5.X1, gd5.Y1, gd5.Z1, gd5.X2, gd5.Y2, gd5.Z2, gd5.X3, gd5.Y3, gd5.Z3, gd5.X4, gd5.Y4,
                                    gd5.Z4, gd5.x1, gd5.y1, gd5.z1, gd5.x2, gd5.y2, gd5.z2, gd5.x3, gd5.y3, gd5.z3, gd5.x4, gd5.y4, gd5.z4, this, datFile);
                            res_gdata = newGdata5;
                            break;
                        case 6:
                            GDataBFC gd6 = (GDataBFC) gdata;
                            res_gdata = new GDataBFC(gd6.type);
                            break;
                        case 8:
                            GDataCSG gd8 = (GDataCSG) gdata;
                            res_gdata = new GDataCSG(gd8.type, gd8.text, this);
                            break;
                        case 9:
                            GDataTEX gd9 = (GDataTEX) gdata;
                            res_gdata = new GDataTEX(gd9.linkedData, gd9.text, gd9.meta, gd9.linkedTexture);
                            break;
                        default:
                            NLogger.debug(getClass(), "CACHE ERROR"); //$NON-NLS-1$
                            return;
                        }

                        anchorData.setNext(res_gdata);
                        anchorData = res_gdata;

                    } else {
                        GData gdata = DatParser.parseLine(line, 0, depth, r, g, b, a, this, pMatrix, accurateProductMatrix, datFile, errorCheckOnly, alreadyParsed, false).get(0).getGraphicalData();
                        if (gdata != null) {
                            GData.parsedLines.put(key3, gdata);
                            if (gdata.type() == 1) {
                                GData1 newGdata1 = (GData1) gdata;
                                this.boundingBoxMin.x = Math.min(this.boundingBoxMin.x, newGdata1.boundingBoxMin.x);
                                this.boundingBoxMin.y = Math.min(this.boundingBoxMin.y, newGdata1.boundingBoxMin.y);
                                this.boundingBoxMin.z = Math.min(this.boundingBoxMin.z, newGdata1.boundingBoxMin.z);
                                this.boundingBoxMin.x = Math.min(this.boundingBoxMin.x, newGdata1.boundingBoxMax.x);
                                this.boundingBoxMin.y = Math.min(this.boundingBoxMin.y, newGdata1.boundingBoxMax.y);
                                this.boundingBoxMin.z = Math.min(this.boundingBoxMin.z, newGdata1.boundingBoxMax.z);
                                this.boundingBoxMax.x = Math.max(this.boundingBoxMax.x, newGdata1.boundingBoxMin.x);
                                this.boundingBoxMax.y = Math.max(this.boundingBoxMax.y, newGdata1.boundingBoxMin.y);
                                this.boundingBoxMax.z = Math.max(this.boundingBoxMax.z, newGdata1.boundingBoxMin.z);
                                this.boundingBoxMax.x = Math.max(this.boundingBoxMax.x, newGdata1.boundingBoxMax.x);
                                this.boundingBoxMax.y = Math.max(this.boundingBoxMax.y, newGdata1.boundingBoxMax.y);
                                this.boundingBoxMax.z = Math.max(this.boundingBoxMax.z, newGdata1.boundingBoxMax.z);
                            }
                        } else {
                            gdata = new GData0(line);
                        }
                        anchorData.setNext(gdata);
                        anchorData = gdata;
                    }
                } else {
                    GData0 gdata = new GData0(line);
                    anchorData.setNext(gdata);
                    anchorData = gdata;
                }
            }

            GData description = myGData.getNext();
            if (description != null && description.type() == 0) {
                if (((GData0) description).text.trim().startsWith("0 ~Moved to")) { //$NON-NLS-1$
                    this.firstRef.setMovedTo(true);
                }
            }
        } else {
            this.firstRef = firstRef;
            this.readOnly = firstRef.readOnly;
            this.depth = depth;
            this.colourNumber = 0;
            this.r = 0;
            this.g = 0;
            this.b = 0;
            this.a = 0;
            this.name = null;
            this.shortName = null;
            this.matrix = null;
            this.productMatrix = null;
            this.localMatrix = null;
            this.accurateProductMatrix = null;
            this.accurateLocalMatrix = null;
            this.negativeDeterminant = false;
        }
    }

    /**
     * SLOWER, FOR TEXMAP ONLY, uses no cache, uses no bounding box!
     *
     * @param colourNumber
     * @param r
     * @param g
     * @param b
     * @param a
     * @param tMatrix
     * @param lines
     * @param name
     * @param shortName
     * @param depth
     * @param det
     * @param pMatrix
     * @param firstRef
     */
    public GData1(int colourNumber, float r, float g, float b, float a, Matrix4f tMatrix, ArrayList<String> lines, String name, String shortName, int depth, boolean det, Matrix4f pMatrix,
            GData1 firstRef, Set<String> alreadyParsed, GData1 parent) {

        this.accurateLocalMatrix = null;
        this.accurateProductMatrix = null;
        this.parent = parent;
        depth++;
        if (depth < 16) {
            if (depth == 1) {
                this.firstRef = this;

            } else {
                this.firstRef = firstRef.firstRef;
            }
            this.readOnly = false;
            this.depth = depth;
            this.boundingBoxMin.x = -10000000f;
            this.boundingBoxMin.y = -10000000f;
            this.boundingBoxMin.z = -1000000f;
            this.boundingBoxMax.x = 10000000f;
            this.boundingBoxMax.y = 10000000f;
            this.boundingBoxMax.z = 10000000f;
            negativeDeterminant = det;
            this.colourNumber = colourNumber;
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
            this.name = name;
            this.shortName = shortName;
            this.productMatrix = new Matrix4f(pMatrix);
            this.localMatrix = new Matrix4f(tMatrix);
            matrix = BufferUtils.createFloatBuffer(16);
            tMatrix.store(matrix);

            matrix.position(0);

            GData anchorData = myGData;

            for (String line : lines) {
                if (isNotBlank(line)) {
                    GData gdata = TexMapParser.parseLine(line, depth, r, g, b, a, this, pMatrix, alreadyParsed);
                    if (gdata == null) {
                        gdata = new GData0(line);
                    }
                    anchorData.setNext(gdata);
                    anchorData = gdata;
                } else {
                    GData0 gdata = new GData0(line);
                    anchorData.setNext(gdata);
                    anchorData = gdata;
                }
            }
        } else {
            this.firstRef = firstRef;
            this.readOnly = firstRef.readOnly;
            this.depth = depth;
            this.colourNumber = 0;
            this.r = 0;
            this.g = 0;
            this.b = 0;
            this.a = 0;
            this.name = null;
            this.shortName = null;
            this.matrix = null;
            this.productMatrix = null;
            this.localMatrix = null;
            this.negativeDeterminant = false;
        }
    }

    public Matrix4f getProductMatrix() {
        return productMatrix;
    }

    public Matrix getAccurateProductMatrix() {
        return accurateProductMatrix;
    }

    private boolean isNotBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return false;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(str.charAt(i)) == false) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void draw(Composite3D c3d) {
        if (!visible)
            return;
        if (matrix != null) {

            final Rectangle bounds = c3d.getClientArea();
            final PerspectiveCalculator PC = c3d.getPerspectiveCalculator();

            Vector4f bbmin = new Vector4f();
            Vector4f bbmax = new Vector4f();

            Vector4f c1 = new Vector4f(boundingBoxMin);
            Vector4f c2 = new Vector4f(boundingBoxMin);
            Vector4f c3 = new Vector4f(boundingBoxMin);
            Vector4f c4 = new Vector4f(boundingBoxMin);
            Vector4f c5 = new Vector4f(boundingBoxMax);
            Vector4f c6 = new Vector4f(boundingBoxMax);
            Vector4f c7 = new Vector4f(boundingBoxMax);
            Vector4f c8 = new Vector4f(boundingBoxMax);

            c2.x = boundingBoxMax.x;
            c3.y = boundingBoxMax.y;
            c4.z = boundingBoxMax.z;

            c6.x = boundingBoxMin.x;
            c7.y = boundingBoxMin.y;
            c8.z = boundingBoxMin.z;

            c1.set(PC.getScreenCoordinatesFrom3D(c1.x, c1.y, c1.z));
            c2.set(PC.getScreenCoordinatesFrom3D(c2.x, c2.y, c2.z));
            c3.set(PC.getScreenCoordinatesFrom3D(c3.x, c3.y, c3.z));
            c4.set(PC.getScreenCoordinatesFrom3D(c4.x, c4.y, c4.z));
            c5.set(PC.getScreenCoordinatesFrom3D(c5.x, c5.y, c5.z));
            c6.set(PC.getScreenCoordinatesFrom3D(c6.x, c6.y, c6.z));
            c7.set(PC.getScreenCoordinatesFrom3D(c7.x, c7.y, c7.z));
            c8.set(PC.getScreenCoordinatesFrom3D(c8.x, c8.y, c8.z));

            bbmin.x = Math.min(c1.x, Math.min(c2.x, Math.min(c3.x, Math.min(c4.x, Math.min(c5.x, Math.min(c6.x, Math.min(c7.x, c8.x)))))));
            bbmax.x = Math.max(c1.x, Math.max(c2.x, Math.max(c3.x, Math.max(c4.x, Math.max(c5.x, Math.max(c6.x, Math.max(c7.x, c8.x)))))));

            bbmin.y = Math.min(c1.y, Math.min(c2.y, Math.min(c3.y, Math.min(c4.y, Math.min(c5.y, Math.min(c6.y, Math.min(c7.y, c8.y)))))));
            bbmax.y = Math.max(c1.y, Math.max(c2.y, Math.max(c3.y, Math.max(c4.y, Math.max(c5.y, Math.max(c6.y, Math.max(c7.y, c8.y)))))));

            Rectangle boundingBox = new Rectangle((int) bbmin.x, (int) bbmin.y, (int) (bbmax.x - bbmin.x), (int) (bbmax.y - bbmin.y));

            boolean tempNegativeDeterminant = GData.globalNegativeDeterminant;
            GData.globalNegativeDeterminant = GData.globalNegativeDeterminant ^ negativeDeterminant;

            if (boundingBox.intersects(bounds) || boundingBox.contains(0, 0) || boundingBox.contains(bounds.width, bounds.height) || boundingBox.contains(bounds.width, 0)
                    || boundingBox.contains(0, bounds.height) || bounds.contains(boundingBox.x, boundingBox.y) || bounds.contains(boundingBox.x, boundingBox.y + boundingBox.height)
                    || bounds.contains(boundingBox.x + boundingBox.width, boundingBox.y) || bounds.contains(boundingBox.x + boundingBox.width, boundingBox.y + boundingBox.height)) {

                GL11.glPushMatrix();
                GL11.glMultMatrix(matrix);

                if (c3d.isShowingLogo()) {
                    if (filesWithLogo1.contains(shortName))
                        drawStudLogo1();
                    else if (filesWithLogo2.contains(shortName))
                        drawStudLogo2();
                }

                GData data2draw = myGData;
                if (GData.accumClip > 0) {
                    GData.accumClip++;
                    while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get())
                        data2draw.draw(c3d);
                    GData.accumClip--;
                } else {
                    while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get())
                        data2draw.draw(c3d);
                    if (GData.accumClip > 0)
                        GData.accumClip = 0;
                }

                GL11.glPopMatrix();

            }

            GData.globalNegativeDeterminant = tempNegativeDeterminant;

        }
    }

    @Override
    public void drawRandomColours(Composite3D c3d) {
        if (!visible)
            return;
        if (matrix != null) {

            final Rectangle bounds = c3d.getClientArea();
            final PerspectiveCalculator PC = c3d.getPerspectiveCalculator();

            Vector4f bbmin = new Vector4f();
            Vector4f bbmax = new Vector4f();

            Vector4f c1 = new Vector4f(boundingBoxMin);
            Vector4f c2 = new Vector4f(boundingBoxMin);
            Vector4f c3 = new Vector4f(boundingBoxMin);
            Vector4f c4 = new Vector4f(boundingBoxMin);
            Vector4f c5 = new Vector4f(boundingBoxMax);
            Vector4f c6 = new Vector4f(boundingBoxMax);
            Vector4f c7 = new Vector4f(boundingBoxMax);
            Vector4f c8 = new Vector4f(boundingBoxMax);

            c2.x = boundingBoxMax.x;
            c3.y = boundingBoxMax.y;
            c4.z = boundingBoxMax.z;

            c6.x = boundingBoxMin.x;
            c7.y = boundingBoxMin.y;
            c8.z = boundingBoxMin.z;

            c1.set(PC.getScreenCoordinatesFrom3D(c1.x, c1.y, c1.z));
            c2.set(PC.getScreenCoordinatesFrom3D(c2.x, c2.y, c2.z));
            c3.set(PC.getScreenCoordinatesFrom3D(c3.x, c3.y, c3.z));
            c4.set(PC.getScreenCoordinatesFrom3D(c4.x, c4.y, c4.z));
            c5.set(PC.getScreenCoordinatesFrom3D(c5.x, c5.y, c5.z));
            c6.set(PC.getScreenCoordinatesFrom3D(c6.x, c6.y, c6.z));
            c7.set(PC.getScreenCoordinatesFrom3D(c7.x, c7.y, c7.z));
            c8.set(PC.getScreenCoordinatesFrom3D(c8.x, c8.y, c8.z));

            bbmin.x = Math.min(c1.x, Math.min(c2.x, Math.min(c3.x, Math.min(c4.x, Math.min(c5.x, Math.min(c6.x, Math.min(c7.x, c8.x)))))));
            bbmax.x = Math.max(c1.x, Math.max(c2.x, Math.max(c3.x, Math.max(c4.x, Math.max(c5.x, Math.max(c6.x, Math.max(c7.x, c8.x)))))));

            bbmin.y = Math.min(c1.y, Math.min(c2.y, Math.min(c3.y, Math.min(c4.y, Math.min(c5.y, Math.min(c6.y, Math.min(c7.y, c8.y)))))));
            bbmax.y = Math.max(c1.y, Math.max(c2.y, Math.max(c3.y, Math.max(c4.y, Math.max(c5.y, Math.max(c6.y, Math.max(c7.y, c8.y)))))));

            Rectangle boundingBox = new Rectangle((int) bbmin.x, (int) bbmin.y, (int) (bbmax.x - bbmin.x), (int) (bbmax.y - bbmin.y));

            boolean tempNegativeDeterminant = GData.globalNegativeDeterminant;
            GData.globalNegativeDeterminant = GData.globalNegativeDeterminant ^ negativeDeterminant;

            if (boundingBox.intersects(bounds) || boundingBox.contains(0, 0) || boundingBox.contains(bounds.width, bounds.height) || boundingBox.contains(bounds.width, 0)
                    || boundingBox.contains(0, bounds.height) || bounds.contains(boundingBox.x, boundingBox.y) || bounds.contains(boundingBox.x, boundingBox.y + boundingBox.height)
                    || bounds.contains(boundingBox.x + boundingBox.width, boundingBox.y) || bounds.contains(boundingBox.x + boundingBox.width, boundingBox.y + boundingBox.height)) {

                GL11.glPushMatrix();
                GL11.glMultMatrix(matrix);

                if (c3d.isShowingLogo()) {
                    if (filesWithLogo1.contains(shortName))
                        drawStudLogo1();
                    else if (filesWithLogo2.contains(shortName))
                        drawStudLogo2();
                }

                GData data2draw = myGData;
                if (GData.accumClip > 0) {
                    GData.accumClip++;
                    while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get())
                        data2draw.drawRandomColours(c3d);
                    GData.accumClip--;
                } else {
                    while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get())
                        data2draw.drawRandomColours(c3d);
                    if (GData.accumClip > 0)
                        GData.accumClip = 0;
                }

                GL11.glPopMatrix();

            }

            GData.globalNegativeDeterminant = tempNegativeDeterminant;

        }
    }

    @Override
    public void drawBFC(Composite3D c3d) {
        if (!visible)
            return;
        if (matrix != null) {

            final Rectangle bounds = c3d.getClientArea();
            final PerspectiveCalculator PC = c3d.getPerspectiveCalculator();

            Vector4f bbmin = new Vector4f();
            Vector4f bbmax = new Vector4f();

            Vector4f c1 = new Vector4f(boundingBoxMin);
            Vector4f c2 = new Vector4f(boundingBoxMin);
            Vector4f c3 = new Vector4f(boundingBoxMin);
            Vector4f c4 = new Vector4f(boundingBoxMin);
            Vector4f c5 = new Vector4f(boundingBoxMax);
            Vector4f c6 = new Vector4f(boundingBoxMax);
            Vector4f c7 = new Vector4f(boundingBoxMax);
            Vector4f c8 = new Vector4f(boundingBoxMax);

            c2.x = boundingBoxMax.x;
            c3.y = boundingBoxMax.y;
            c4.z = boundingBoxMax.z;

            c6.x = boundingBoxMin.x;
            c7.y = boundingBoxMin.y;
            c8.z = boundingBoxMin.z;

            c1.set(PC.getScreenCoordinatesFrom3D(c1.x, c1.y, c1.z));
            c2.set(PC.getScreenCoordinatesFrom3D(c2.x, c2.y, c2.z));
            c3.set(PC.getScreenCoordinatesFrom3D(c3.x, c3.y, c3.z));
            c4.set(PC.getScreenCoordinatesFrom3D(c4.x, c4.y, c4.z));
            c5.set(PC.getScreenCoordinatesFrom3D(c5.x, c5.y, c5.z));
            c6.set(PC.getScreenCoordinatesFrom3D(c6.x, c6.y, c6.z));
            c7.set(PC.getScreenCoordinatesFrom3D(c7.x, c7.y, c7.z));
            c8.set(PC.getScreenCoordinatesFrom3D(c8.x, c8.y, c8.z));

            bbmin.x = Math.min(c1.x, Math.min(c2.x, Math.min(c3.x, Math.min(c4.x, Math.min(c5.x, Math.min(c6.x, Math.min(c7.x, c8.x)))))));
            bbmax.x = Math.max(c1.x, Math.max(c2.x, Math.max(c3.x, Math.max(c4.x, Math.max(c5.x, Math.max(c6.x, Math.max(c7.x, c8.x)))))));

            bbmin.y = Math.min(c1.y, Math.min(c2.y, Math.min(c3.y, Math.min(c4.y, Math.min(c5.y, Math.min(c6.y, Math.min(c7.y, c8.y)))))));
            bbmax.y = Math.max(c1.y, Math.max(c2.y, Math.max(c3.y, Math.max(c4.y, Math.max(c5.y, Math.max(c6.y, Math.max(c7.y, c8.y)))))));

            Rectangle boundingBox = new Rectangle((int) bbmin.x, (int) bbmin.y, (int) (bbmax.x - bbmin.x), (int) (bbmax.y - bbmin.y));

            byte tempWinding = GData.localWinding;
            boolean tempInvertNext = GData.globalInvertNext;
            boolean tempInvertNextFound = GData.globalInvertNextFound;
            boolean tempNegativeDeterminant = GData.globalNegativeDeterminant;

            GData.globalInvertNextFound = false;
            GData.localWinding = BFC.NOCERTIFY;
            GData.globalNegativeDeterminant = GData.globalNegativeDeterminant ^ negativeDeterminant;

            if (boundingBox.intersects(bounds) || boundingBox.contains(0, 0) || boundingBox.contains(bounds.width, bounds.height) || boundingBox.contains(bounds.width, 0)
                    || boundingBox.contains(0, bounds.height) || bounds.contains(boundingBox.x, boundingBox.y) || bounds.contains(boundingBox.x, boundingBox.y + boundingBox.height)
                    || bounds.contains(boundingBox.x + boundingBox.width, boundingBox.y) || bounds.contains(boundingBox.x + boundingBox.width, boundingBox.y + boundingBox.height)) {

                /*
                 * // Bounding Boxes: GL11.glPushMatrix();
                 * GL11.glLoadIdentity();
                 * GL11.glMultMatrix(c3d.getRenderer().getViewport());
                 *
                 * GL11.glLineWidth(3f); GL11.glBegin(GL11.GL_LINES);
                 * GL11.glColor3f(1f, 1f, 0f);
                 *
                 * GL11.glVertex3f(boundingBoxMinReal.x, boundingBoxMinReal.y,
                 * boundingBoxMinReal.z); GL11.glVertex3f(boundingBoxMinReal.x,
                 * boundingBoxMinReal.y, boundingBoxMaxReal.z);
                 *
                 * GL11.glVertex3f(boundingBoxMinReal.x, boundingBoxMinReal.y,
                 * boundingBoxMinReal.z); GL11.glVertex3f(boundingBoxMinReal.x,
                 * boundingBoxMaxReal.y, boundingBoxMinReal.z);
                 *
                 * GL11.glVertex3f(boundingBoxMinReal.x, boundingBoxMinReal.y,
                 * boundingBoxMinReal.z); GL11.glVertex3f(boundingBoxMaxReal.x,
                 * boundingBoxMinReal.y, boundingBoxMinReal.z);
                 *
                 * GL11.glVertex3f(boundingBoxMinReal.x, boundingBoxMaxReal.y,
                 * boundingBoxMaxReal.z); GL11.glVertex3f(boundingBoxMaxReal.x,
                 * boundingBoxMaxReal.y, boundingBoxMaxReal.z);
                 *
                 * GL11.glVertex3f(boundingBoxMaxReal.x, boundingBoxMinReal.y,
                 * boundingBoxMaxReal.z); GL11.glVertex3f(boundingBoxMaxReal.x,
                 * boundingBoxMaxReal.y, boundingBoxMaxReal.z);
                 *
                 * GL11.glVertex3f(boundingBoxMaxReal.x, boundingBoxMaxReal.y,
                 * boundingBoxMinReal.z); GL11.glVertex3f(boundingBoxMaxReal.x,
                 * boundingBoxMaxReal.y, boundingBoxMaxReal.z);
                 *
                 * GL11.glEnd();
                 *
                 * GL11.glPopMatrix();
                 */

                GL11.glPushMatrix();
                GL11.glMultMatrix(matrix);

                if (c3d.isShowingLogo()) {
                    if (filesWithLogo1.contains(shortName))
                        drawStudLogo1();
                    else if (filesWithLogo2.contains(shortName))
                        drawStudLogo2();
                }

                GData data2draw = myGData;

                if (GData.accumClip > 0) {
                    GData.accumClip++;
                    while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get())
                        data2draw.draw(c3d);
                    GData.accumClip--;
                } else {
                    while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                        switch (tempWinding) {
                        case BFC.NOCERTIFY:
                            data2draw.drawBFCuncertified(c3d);
                            break;
                        default:
                            data2draw.drawBFC(c3d);
                            break;
                        }
                    }
                    if (GData.accumClip > 0)
                        GData.accumClip = 0;
                }

                GL11.glPopMatrix();

            }

            GData.localWinding = tempWinding;
            if (tempInvertNextFound)
                GData.globalInvertNext = !tempInvertNext;

            GData.globalNegativeDeterminant = tempNegativeDeterminant;
        }
    }

    @Override
    public void drawBFCuncertified(Composite3D c3d) {
        if (!visible)
            return;
        if (matrix != null) {

            final Rectangle bounds = c3d.getClientArea();
            final PerspectiveCalculator PC = c3d.getPerspectiveCalculator();

            Vector4f bbmin = new Vector4f();
            Vector4f bbmax = new Vector4f();

            Vector4f c1 = new Vector4f(boundingBoxMin);
            Vector4f c2 = new Vector4f(boundingBoxMin);
            Vector4f c3 = new Vector4f(boundingBoxMin);
            Vector4f c4 = new Vector4f(boundingBoxMin);
            Vector4f c5 = new Vector4f(boundingBoxMax);
            Vector4f c6 = new Vector4f(boundingBoxMax);
            Vector4f c7 = new Vector4f(boundingBoxMax);
            Vector4f c8 = new Vector4f(boundingBoxMax);

            c2.x = boundingBoxMax.x;
            c3.y = boundingBoxMax.y;
            c4.z = boundingBoxMax.z;

            c6.x = boundingBoxMin.x;
            c7.y = boundingBoxMin.y;
            c8.z = boundingBoxMin.z;

            c1.set(PC.getScreenCoordinatesFrom3D(c1.x, c1.y, c1.z));
            c2.set(PC.getScreenCoordinatesFrom3D(c2.x, c2.y, c2.z));
            c3.set(PC.getScreenCoordinatesFrom3D(c3.x, c3.y, c3.z));
            c4.set(PC.getScreenCoordinatesFrom3D(c4.x, c4.y, c4.z));
            c5.set(PC.getScreenCoordinatesFrom3D(c5.x, c5.y, c5.z));
            c6.set(PC.getScreenCoordinatesFrom3D(c6.x, c6.y, c6.z));
            c7.set(PC.getScreenCoordinatesFrom3D(c7.x, c7.y, c7.z));
            c8.set(PC.getScreenCoordinatesFrom3D(c8.x, c8.y, c8.z));

            bbmin.x = Math.min(c1.x, Math.min(c2.x, Math.min(c3.x, Math.min(c4.x, Math.min(c5.x, Math.min(c6.x, Math.min(c7.x, c8.x)))))));
            bbmax.x = Math.max(c1.x, Math.max(c2.x, Math.max(c3.x, Math.max(c4.x, Math.max(c5.x, Math.max(c6.x, Math.max(c7.x, c8.x)))))));

            bbmin.y = Math.min(c1.y, Math.min(c2.y, Math.min(c3.y, Math.min(c4.y, Math.min(c5.y, Math.min(c6.y, Math.min(c7.y, c8.y)))))));
            bbmax.y = Math.max(c1.y, Math.max(c2.y, Math.max(c3.y, Math.max(c4.y, Math.max(c5.y, Math.max(c6.y, Math.max(c7.y, c8.y)))))));

            Rectangle boundingBox = new Rectangle((int) bbmin.x, (int) bbmin.y, (int) (bbmax.x - bbmin.x), (int) (bbmax.y - bbmin.y));


            byte tempWinding = GData.localWinding;
            boolean tempInvertNext = GData.globalInvertNext;
            boolean tempInvertNextFound = GData.globalInvertNextFound;
            boolean tempNegativeDeterminant = GData.globalNegativeDeterminant;
            GData.globalNegativeDeterminant = GData.globalNegativeDeterminant ^ negativeDeterminant;

            if (boundingBox.intersects(bounds) || boundingBox.contains(0, 0) || boundingBox.contains(bounds.width, bounds.height) || boundingBox.contains(bounds.width, 0)
                    || boundingBox.contains(0, bounds.height) || bounds.contains(boundingBox.x, boundingBox.y) || bounds.contains(boundingBox.x, boundingBox.y + boundingBox.height)
                    || bounds.contains(boundingBox.x + boundingBox.width, boundingBox.y) || bounds.contains(boundingBox.x + boundingBox.width, boundingBox.y + boundingBox.height)) {

                GL11.glPushMatrix();
                GL11.glMultMatrix(matrix);

                if (c3d.isShowingLogo()) {
                    if (filesWithLogo1.contains(shortName))
                        drawStudLogo1();
                    else if (filesWithLogo2.contains(shortName))
                        drawStudLogo2();
                }

                GData data2draw = myGData;
                if (GData.accumClip > 0) {
                    GData.accumClip++;
                    while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get())
                        data2draw.drawBFCuncertified(c3d);
                    GData.accumClip--;
                } else {
                    while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get())
                        data2draw.drawBFCuncertified(c3d);
                    if (GData.accumClip > 0)
                        GData.accumClip = 0;
                }

                GL11.glPopMatrix();

            }

            GData.localWinding = tempWinding;
            if (tempInvertNextFound)
                GData.globalInvertNext = !tempInvertNext;

            GData.globalNegativeDeterminant = tempNegativeDeterminant;

        }
    }

    @Override
    public void drawBFC_backOnly(Composite3D c3d) {
        if (!visible)
            return;
        if (matrix != null) {

            final Rectangle bounds = c3d.getClientArea();
            final PerspectiveCalculator PC = c3d.getPerspectiveCalculator();

            Vector4f bbmin = new Vector4f();
            Vector4f bbmax = new Vector4f();

            Vector4f c1 = new Vector4f(boundingBoxMin);
            Vector4f c2 = new Vector4f(boundingBoxMin);
            Vector4f c3 = new Vector4f(boundingBoxMin);
            Vector4f c4 = new Vector4f(boundingBoxMin);
            Vector4f c5 = new Vector4f(boundingBoxMax);
            Vector4f c6 = new Vector4f(boundingBoxMax);
            Vector4f c7 = new Vector4f(boundingBoxMax);
            Vector4f c8 = new Vector4f(boundingBoxMax);

            c2.x = boundingBoxMax.x;
            c3.y = boundingBoxMax.y;
            c4.z = boundingBoxMax.z;

            c6.x = boundingBoxMin.x;
            c7.y = boundingBoxMin.y;
            c8.z = boundingBoxMin.z;

            c1.set(PC.getScreenCoordinatesFrom3D(c1.x, c1.y, c1.z));
            c2.set(PC.getScreenCoordinatesFrom3D(c2.x, c2.y, c2.z));
            c3.set(PC.getScreenCoordinatesFrom3D(c3.x, c3.y, c3.z));
            c4.set(PC.getScreenCoordinatesFrom3D(c4.x, c4.y, c4.z));
            c5.set(PC.getScreenCoordinatesFrom3D(c5.x, c5.y, c5.z));
            c6.set(PC.getScreenCoordinatesFrom3D(c6.x, c6.y, c6.z));
            c7.set(PC.getScreenCoordinatesFrom3D(c7.x, c7.y, c7.z));
            c8.set(PC.getScreenCoordinatesFrom3D(c8.x, c8.y, c8.z));

            bbmin.x = Math.min(c1.x, Math.min(c2.x, Math.min(c3.x, Math.min(c4.x, Math.min(c5.x, Math.min(c6.x, Math.min(c7.x, c8.x)))))));
            bbmax.x = Math.max(c1.x, Math.max(c2.x, Math.max(c3.x, Math.max(c4.x, Math.max(c5.x, Math.max(c6.x, Math.max(c7.x, c8.x)))))));

            bbmin.y = Math.min(c1.y, Math.min(c2.y, Math.min(c3.y, Math.min(c4.y, Math.min(c5.y, Math.min(c6.y, Math.min(c7.y, c8.y)))))));
            bbmax.y = Math.max(c1.y, Math.max(c2.y, Math.max(c3.y, Math.max(c4.y, Math.max(c5.y, Math.max(c6.y, Math.max(c7.y, c8.y)))))));

            Rectangle boundingBox = new Rectangle((int) bbmin.x, (int) bbmin.y, (int) (bbmax.x - bbmin.x), (int) (bbmax.y - bbmin.y));

            byte tempWinding = GData.localWinding;
            boolean tempInvertNext = GData.globalInvertNext;
            boolean tempInvertNextFound = GData.globalInvertNextFound;
            boolean tempNegativeDeterminant = GData.globalNegativeDeterminant;

            GData.globalInvertNextFound = false;
            GData.localWinding = BFC.NOCERTIFY;
            GData.globalNegativeDeterminant = GData.globalNegativeDeterminant ^ negativeDeterminant;

            if (boundingBox.intersects(bounds) || boundingBox.contains(0, 0) || boundingBox.contains(bounds.width, bounds.height) || boundingBox.contains(bounds.width, 0)
                    || boundingBox.contains(0, bounds.height) || bounds.contains(boundingBox.x, boundingBox.y) || bounds.contains(boundingBox.x, boundingBox.y + boundingBox.height)
                    || bounds.contains(boundingBox.x + boundingBox.width, boundingBox.y) || bounds.contains(boundingBox.x + boundingBox.width, boundingBox.y + boundingBox.height)) {

                GL11.glPushMatrix();
                GL11.glMultMatrix(matrix);

                if (c3d.isShowingLogo()) {
                    if (filesWithLogo1.contains(shortName))
                        drawStudLogo1();
                    else if (filesWithLogo2.contains(shortName))
                        drawStudLogo2();
                }

                GData data2draw = myGData;

                if (GData.accumClip > 0) {
                    GData.accumClip++;
                    while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get())
                        data2draw.draw(c3d);
                    GData.accumClip--;
                } else {
                    while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                        switch (tempWinding) {
                        case BFC.NOCERTIFY:
                            data2draw.drawBFCuncertified(c3d);
                            break;
                        default:
                            data2draw.drawBFC_backOnly(c3d);
                            break;
                        }
                    }
                    if (GData.accumClip > 0)
                        GData.accumClip = 0;
                }

                GL11.glPopMatrix();

            }

            GData.localWinding = tempWinding;
            if (tempInvertNextFound)
                GData.globalInvertNext = !tempInvertNext;

            GData.globalNegativeDeterminant = tempNegativeDeterminant;
        }
    }

    @Override
    public void drawBFC_Colour(Composite3D c3d) {
        if (!visible)
            return;
        if (matrix != null) {

            final Rectangle bounds = c3d.getClientArea();
            final PerspectiveCalculator PC = c3d.getPerspectiveCalculator();

            Vector4f bbmin = new Vector4f();
            Vector4f bbmax = new Vector4f();

            Vector4f c1 = new Vector4f(boundingBoxMin);
            Vector4f c2 = new Vector4f(boundingBoxMin);
            Vector4f c3 = new Vector4f(boundingBoxMin);
            Vector4f c4 = new Vector4f(boundingBoxMin);
            Vector4f c5 = new Vector4f(boundingBoxMax);
            Vector4f c6 = new Vector4f(boundingBoxMax);
            Vector4f c7 = new Vector4f(boundingBoxMax);
            Vector4f c8 = new Vector4f(boundingBoxMax);

            c2.x = boundingBoxMax.x;
            c3.y = boundingBoxMax.y;
            c4.z = boundingBoxMax.z;

            c6.x = boundingBoxMin.x;
            c7.y = boundingBoxMin.y;
            c8.z = boundingBoxMin.z;

            c1.set(PC.getScreenCoordinatesFrom3D(c1.x, c1.y, c1.z));
            c2.set(PC.getScreenCoordinatesFrom3D(c2.x, c2.y, c2.z));
            c3.set(PC.getScreenCoordinatesFrom3D(c3.x, c3.y, c3.z));
            c4.set(PC.getScreenCoordinatesFrom3D(c4.x, c4.y, c4.z));
            c5.set(PC.getScreenCoordinatesFrom3D(c5.x, c5.y, c5.z));
            c6.set(PC.getScreenCoordinatesFrom3D(c6.x, c6.y, c6.z));
            c7.set(PC.getScreenCoordinatesFrom3D(c7.x, c7.y, c7.z));
            c8.set(PC.getScreenCoordinatesFrom3D(c8.x, c8.y, c8.z));

            bbmin.x = Math.min(c1.x, Math.min(c2.x, Math.min(c3.x, Math.min(c4.x, Math.min(c5.x, Math.min(c6.x, Math.min(c7.x, c8.x)))))));
            bbmax.x = Math.max(c1.x, Math.max(c2.x, Math.max(c3.x, Math.max(c4.x, Math.max(c5.x, Math.max(c6.x, Math.max(c7.x, c8.x)))))));

            bbmin.y = Math.min(c1.y, Math.min(c2.y, Math.min(c3.y, Math.min(c4.y, Math.min(c5.y, Math.min(c6.y, Math.min(c7.y, c8.y)))))));
            bbmax.y = Math.max(c1.y, Math.max(c2.y, Math.max(c3.y, Math.max(c4.y, Math.max(c5.y, Math.max(c6.y, Math.max(c7.y, c8.y)))))));

            Rectangle boundingBox = new Rectangle((int) bbmin.x, (int) bbmin.y, (int) (bbmax.x - bbmin.x), (int) (bbmax.y - bbmin.y));

            byte tempWinding = GData.localWinding;
            boolean tempInvertNext = GData.globalInvertNext;
            boolean tempInvertNextFound = GData.globalInvertNextFound;
            boolean tempNegativeDeterminant = GData.globalNegativeDeterminant;

            GData.globalInvertNextFound = false;
            GData.localWinding = BFC.NOCERTIFY;
            GData.globalNegativeDeterminant = GData.globalNegativeDeterminant ^ negativeDeterminant;

            if (boundingBox.intersects(bounds) || boundingBox.contains(0, 0) || boundingBox.contains(bounds.width, bounds.height) || boundingBox.contains(bounds.width, 0)
                    || boundingBox.contains(0, bounds.height) || bounds.contains(boundingBox.x, boundingBox.y) || bounds.contains(boundingBox.x, boundingBox.y + boundingBox.height)
                    || bounds.contains(boundingBox.x + boundingBox.width, boundingBox.y) || bounds.contains(boundingBox.x + boundingBox.width, boundingBox.y + boundingBox.height)) {

                GL11.glPushMatrix();
                GL11.glMultMatrix(matrix);

                if (c3d.isShowingLogo()) {
                    if (filesWithLogo1.contains(shortName))
                        drawStudLogo1();
                    else if (filesWithLogo2.contains(shortName))
                        drawStudLogo2();
                }

                GData data2draw = myGData;

                if (GData.accumClip > 0) {
                    GData.accumClip++;
                    while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get())
                        data2draw.draw(c3d);
                    GData.accumClip--;
                } else {
                    while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                        switch (tempWinding) {
                        case BFC.NOCERTIFY:
                            data2draw.drawBFCuncertified(c3d);
                            break;
                        default:
                            data2draw.drawBFC_Colour(c3d);
                            break;
                        }
                    }
                    if (GData.accumClip > 0)
                        GData.accumClip = 0;
                }

                GL11.glPopMatrix();

            }

            GData.localWinding = tempWinding;
            if (tempInvertNextFound)
                GData.globalInvertNext = !tempInvertNext;

            GData.globalNegativeDeterminant = tempNegativeDeterminant;
        }
    }

    @Override
    public void drawBFC_Textured(Composite3D c3d) {
        boolean tNext = GData.globalFoundTEXMAPNEXT;
        GData.globalFoundTEXMAPNEXT = false;
        if (!visible || !GData.globalDrawObjects)
            return;
        if (matrix != null) {

            final Rectangle bounds = c3d.getClientArea();
            final PerspectiveCalculator PC = c3d.getPerspectiveCalculator();

            Vector4f bbmin = new Vector4f();
            Vector4f bbmax = new Vector4f();

            Vector4f c1 = new Vector4f(boundingBoxMin);
            Vector4f c2 = new Vector4f(boundingBoxMin);
            Vector4f c3 = new Vector4f(boundingBoxMin);
            Vector4f c4 = new Vector4f(boundingBoxMin);
            Vector4f c5 = new Vector4f(boundingBoxMax);
            Vector4f c6 = new Vector4f(boundingBoxMax);
            Vector4f c7 = new Vector4f(boundingBoxMax);
            Vector4f c8 = new Vector4f(boundingBoxMax);

            c2.x = boundingBoxMax.x;
            c3.y = boundingBoxMax.y;
            c4.z = boundingBoxMax.z;

            c6.x = boundingBoxMin.x;
            c7.y = boundingBoxMin.y;
            c8.z = boundingBoxMin.z;

            c1.set(PC.getScreenCoordinatesFrom3D(c1.x, c1.y, c1.z));
            c2.set(PC.getScreenCoordinatesFrom3D(c2.x, c2.y, c2.z));
            c3.set(PC.getScreenCoordinatesFrom3D(c3.x, c3.y, c3.z));
            c4.set(PC.getScreenCoordinatesFrom3D(c4.x, c4.y, c4.z));
            c5.set(PC.getScreenCoordinatesFrom3D(c5.x, c5.y, c5.z));
            c6.set(PC.getScreenCoordinatesFrom3D(c6.x, c6.y, c6.z));
            c7.set(PC.getScreenCoordinatesFrom3D(c7.x, c7.y, c7.z));
            c8.set(PC.getScreenCoordinatesFrom3D(c8.x, c8.y, c8.z));

            bbmin.x = Math.min(c1.x, Math.min(c2.x, Math.min(c3.x, Math.min(c4.x, Math.min(c5.x, Math.min(c6.x, Math.min(c7.x, c8.x)))))));
            bbmax.x = Math.max(c1.x, Math.max(c2.x, Math.max(c3.x, Math.max(c4.x, Math.max(c5.x, Math.max(c6.x, Math.max(c7.x, c8.x)))))));

            bbmin.y = Math.min(c1.y, Math.min(c2.y, Math.min(c3.y, Math.min(c4.y, Math.min(c5.y, Math.min(c6.y, Math.min(c7.y, c8.y)))))));
            bbmax.y = Math.max(c1.y, Math.max(c2.y, Math.max(c3.y, Math.max(c4.y, Math.max(c5.y, Math.max(c6.y, Math.max(c7.y, c8.y)))))));

            Rectangle boundingBox = new Rectangle((int) bbmin.x, (int) bbmin.y, (int) (bbmax.x - bbmin.x), (int) (bbmax.y - bbmin.y));

            byte tempWinding = GData.localWinding;
            boolean tempInvertNext = GData.globalInvertNext;
            boolean tempInvertNextFound = GData.globalInvertNextFound;
            boolean tempNegativeDeterminant = GData.globalNegativeDeterminant;

            GData.globalInvertNextFound = false;
            GData.localWinding = BFC.NOCERTIFY;
            GData.globalNegativeDeterminant = GData.globalNegativeDeterminant ^ negativeDeterminant;

            if (boundingBox.intersects(bounds) || boundingBox.contains(0, 0) || boundingBox.contains(bounds.width, bounds.height) || boundingBox.contains(bounds.width, 0)
                    || boundingBox.contains(0, bounds.height) || bounds.contains(boundingBox.x, boundingBox.y) || bounds.contains(boundingBox.x, boundingBox.y + boundingBox.height)
                    || bounds.contains(boundingBox.x + boundingBox.width, boundingBox.y) || bounds.contains(boundingBox.x + boundingBox.width, boundingBox.y + boundingBox.height)) {

                GData.globalFoundTEXMAPStack.push(false);

                GData data2draw = myGData;

                if (GData.accumClip > 0) {
                    GData.accumClip++;
                    while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get())
                        data2draw.drawBFC_Textured(c3d);
                    GData.accumClip--;
                } else {
                    while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                        data2draw.drawBFC_Textured(c3d);
                    }
                    if (GData.accumClip > 0)
                        GData.accumClip = 0;
                }

                if (GData.globalFoundTEXMAPStack.peek()) {
                    GData.globalFoundTEXMAPStack.pop();
                    GData.globalTextureStack.pop();
                    GData.globalFoundTEXMAPStack.push(false);
                    GData.globalDrawObjects = true;
                }
                GData.globalFoundTEXMAPStack.pop();

            }

            GData.localWinding = tempWinding;
            if (tempInvertNextFound)
                GData.globalInvertNext = !tempInvertNext;

            GData.globalNegativeDeterminant = tempNegativeDeterminant;
        }
        if (tNext) {
            GData.globalFoundTEXMAPStack.pop();
            GData.globalTextureStack.pop();
            GData.globalFoundTEXMAPStack.push(false);
            GData.globalFoundTEXMAPNEXT = false;
        }
    }

    @Override
    public void drawWhileAddCondlines(Composite3D c3d) {
        if (!visible)
            return;
        if (matrix != null) {

            final Rectangle bounds = c3d.getClientArea();
            final PerspectiveCalculator PC = c3d.getPerspectiveCalculator();

            Vector4f bbmin = new Vector4f();
            Vector4f bbmax = new Vector4f();

            Vector4f c1 = new Vector4f(boundingBoxMin);
            Vector4f c2 = new Vector4f(boundingBoxMin);
            Vector4f c3 = new Vector4f(boundingBoxMin);
            Vector4f c4 = new Vector4f(boundingBoxMin);
            Vector4f c5 = new Vector4f(boundingBoxMax);
            Vector4f c6 = new Vector4f(boundingBoxMax);
            Vector4f c7 = new Vector4f(boundingBoxMax);
            Vector4f c8 = new Vector4f(boundingBoxMax);

            c2.x = boundingBoxMax.x;
            c3.y = boundingBoxMax.y;
            c4.z = boundingBoxMax.z;

            c6.x = boundingBoxMin.x;
            c7.y = boundingBoxMin.y;
            c8.z = boundingBoxMin.z;

            c1.set(PC.getScreenCoordinatesFrom3D(c1.x, c1.y, c1.z));
            c2.set(PC.getScreenCoordinatesFrom3D(c2.x, c2.y, c2.z));
            c3.set(PC.getScreenCoordinatesFrom3D(c3.x, c3.y, c3.z));
            c4.set(PC.getScreenCoordinatesFrom3D(c4.x, c4.y, c4.z));
            c5.set(PC.getScreenCoordinatesFrom3D(c5.x, c5.y, c5.z));
            c6.set(PC.getScreenCoordinatesFrom3D(c6.x, c6.y, c6.z));
            c7.set(PC.getScreenCoordinatesFrom3D(c7.x, c7.y, c7.z));
            c8.set(PC.getScreenCoordinatesFrom3D(c8.x, c8.y, c8.z));

            bbmin.x = Math.min(c1.x, Math.min(c2.x, Math.min(c3.x, Math.min(c4.x, Math.min(c5.x, Math.min(c6.x, Math.min(c7.x, c8.x)))))));
            bbmax.x = Math.max(c1.x, Math.max(c2.x, Math.max(c3.x, Math.max(c4.x, Math.max(c5.x, Math.max(c6.x, Math.max(c7.x, c8.x)))))));

            bbmin.y = Math.min(c1.y, Math.min(c2.y, Math.min(c3.y, Math.min(c4.y, Math.min(c5.y, Math.min(c6.y, Math.min(c7.y, c8.y)))))));
            bbmax.y = Math.max(c1.y, Math.max(c2.y, Math.max(c3.y, Math.max(c4.y, Math.max(c5.y, Math.max(c6.y, Math.max(c7.y, c8.y)))))));

            Rectangle boundingBox = new Rectangle((int) bbmin.x, (int) bbmin.y, (int) (bbmax.x - bbmin.x), (int) (bbmax.y - bbmin.y));

            byte tempWinding = GData.localWinding;
            boolean tempInvertNext = GData.globalInvertNext;
            boolean tempInvertNextFound = GData.globalInvertNextFound;
            boolean tempNegativeDeterminant = GData.globalNegativeDeterminant;

            GData.globalInvertNextFound = false;
            GData.localWinding = BFC.NOCERTIFY;
            GData.globalNegativeDeterminant = GData.globalNegativeDeterminant ^ negativeDeterminant;

            if (boundingBox.intersects(bounds) || boundingBox.contains(0, 0) || boundingBox.contains(bounds.width, bounds.height) || boundingBox.contains(bounds.width, 0)
                    || boundingBox.contains(0, bounds.height) || bounds.contains(boundingBox.x, boundingBox.y) || bounds.contains(boundingBox.x, boundingBox.y + boundingBox.height)
                    || bounds.contains(boundingBox.x + boundingBox.width, boundingBox.y) || bounds.contains(boundingBox.x + boundingBox.width, boundingBox.y + boundingBox.height)) {

                GL11.glPushMatrix();
                GL11.glMultMatrix(matrix);

                if (c3d.isShowingLogo()) {
                    if (filesWithLogo1.contains(shortName))
                        drawStudLogo1();
                    else if (filesWithLogo2.contains(shortName))
                        drawStudLogo2();
                }

                GData data2draw = myGData;

                if (GData.accumClip > 0) {
                    GData.accumClip++;
                    while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                        switch (data2draw.type()) {
                        case 1:
                        case 5:
                            data2draw.drawWhileAddCondlines(c3d);
                            break;
                        default:
                            data2draw.draw(c3d);
                        }
                    }
                    GData.accumClip--;
                } else {
                    while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                        switch (tempWinding) {
                        case BFC.NOCERTIFY:
                            switch (data2draw.type()) {
                            case 1:
                            case 5:
                                data2draw.drawWhileAddCondlines(c3d);
                                break;
                            default:
                                data2draw.draw(c3d);
                            }
                            break;
                        default:
                            data2draw.drawWhileAddCondlines(c3d);
                            break;
                        }
                    }
                    if (GData.accumClip > 0)
                        GData.accumClip = 0;
                }

                GL11.glPopMatrix();

            }

            GData.localWinding = tempWinding;
            if (tempInvertNextFound)
                GData.globalInvertNext = !tempInvertNext;

            GData.globalNegativeDeterminant = tempNegativeDeterminant;
        }
    }

    public static void updateBoundingBox(int count, Vector4f v1, Vector4f v2, Vector4f v3, Vector4f v4, GData1 instance) {
        switch (count) {
        case 4:
            instance.boundingBoxMin.x = Math.min(instance.boundingBoxMin.x, v4.x);
            instance.boundingBoxMin.y = Math.min(instance.boundingBoxMin.y, v4.y);
            instance.boundingBoxMin.z = Math.min(instance.boundingBoxMin.z, v4.z);
            instance.boundingBoxMax.x = Math.max(instance.boundingBoxMax.x, v4.x);
            instance.boundingBoxMax.y = Math.max(instance.boundingBoxMax.y, v4.y);
            instance.boundingBoxMax.z = Math.max(instance.boundingBoxMax.z, v4.z);
        case 3:
            instance.boundingBoxMin.x = Math.min(instance.boundingBoxMin.x, v3.x);
            instance.boundingBoxMin.y = Math.min(instance.boundingBoxMin.y, v3.y);
            instance.boundingBoxMin.z = Math.min(instance.boundingBoxMin.z, v3.z);
            instance.boundingBoxMax.x = Math.max(instance.boundingBoxMax.x, v3.x);
            instance.boundingBoxMax.y = Math.max(instance.boundingBoxMax.y, v3.y);
            instance.boundingBoxMax.z = Math.max(instance.boundingBoxMax.z, v3.z);
        case 2:
            instance.boundingBoxMin.x = Math.min(instance.boundingBoxMin.x, v2.x);
            instance.boundingBoxMin.y = Math.min(instance.boundingBoxMin.y, v2.y);
            instance.boundingBoxMin.z = Math.min(instance.boundingBoxMin.z, v2.z);
            instance.boundingBoxMax.x = Math.max(instance.boundingBoxMax.x, v2.x);
            instance.boundingBoxMax.y = Math.max(instance.boundingBoxMax.y, v2.y);
            instance.boundingBoxMax.z = Math.max(instance.boundingBoxMax.z, v2.z);
            instance.boundingBoxMin.x = Math.min(instance.boundingBoxMin.x, v1.x);
            instance.boundingBoxMin.y = Math.min(instance.boundingBoxMin.y, v1.y);
            instance.boundingBoxMin.z = Math.min(instance.boundingBoxMin.z, v1.z);
            instance.boundingBoxMax.x = Math.max(instance.boundingBoxMax.x, v1.x);
            instance.boundingBoxMax.y = Math.max(instance.boundingBoxMax.y, v1.y);
            instance.boundingBoxMax.z = Math.max(instance.boundingBoxMax.z, v1.z);
            break;
        default:
            break;
        }
    }

    @Override
    public int type() {
        return 1;
    }

    @Override
    String getNiceString() {
        if (text != null)
            return text;
        StringBuilder lineBuilder = new StringBuilder();
        lineBuilder.append(1);
        lineBuilder.append(" "); //$NON-NLS-1$
        if (colourNumber == -1) {
            lineBuilder.append("0x2"); //$NON-NLS-1$
            lineBuilder.append(MathHelper.toHex((int) (255f * r)).toUpperCase());
            lineBuilder.append(MathHelper.toHex((int) (255f * g)).toUpperCase());
            lineBuilder.append(MathHelper.toHex((int) (255f * b)).toUpperCase());
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
        text = lineBuilder.toString();
        return text;
    }

    public String getTransformedString(Matrix transformation, DatFile df, boolean avoidFlatScaling) {
        Matrix accurateLocalMatrix = new Matrix(this.accurateLocalMatrix);
        BigDecimal tx = this.accurateLocalMatrix.M30.add(BigDecimal.ZERO);
        BigDecimal ty = this.accurateLocalMatrix.M31.add(BigDecimal.ZERO);
        BigDecimal tz = this.accurateLocalMatrix.M32.add(BigDecimal.ZERO);
        accurateLocalMatrix = accurateLocalMatrix.translate(new BigDecimal[] { tx.negate(), ty.negate(), tz.negate() });
        accurateLocalMatrix = Matrix.mul(transformation, accurateLocalMatrix);
        accurateLocalMatrix = accurateLocalMatrix.translate(new BigDecimal[] { tx, ty, tz });

        // Avoid scaling of flat files
        GData1 untransformedSubfile;
        StringBuilder colourBuilder = new StringBuilder();
        if (this.colourNumber == -1) {
            colourBuilder.append("0x2"); //$NON-NLS-1$
            colourBuilder.append(MathHelper.toHex((int) (255f * this.r)).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * this.g)).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * this.b)).toUpperCase());
        } else {
            colourBuilder.append(this.colourNumber);
        }
        // Clear the cache..
        GData.parsedLines.clear();
        GData.CACHE_parsedFilesSource.clear();
        untransformedSubfile = (GData1) DatParser
                .parseLine("1 " + colourBuilder.toString() + " 0 0 0 1 0 0 0 1 0 0 0 1 " + this.shortName , 0, 0, 0.5f, 0.5f, 0.5f, 1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, df, false, //$NON-NLS-1$ //$NON-NLS-2$
                        new HashSet<String>(), false).get(0).getGraphicalData();
        // Clear the cache..
        GData.parsedLines.clear();
        GData.CACHE_parsedFilesSource.clear();
        boolean plainOnX = untransformedSubfile.boundingBoxMin.x - untransformedSubfile.boundingBoxMax.x == 0f;
        boolean plainOnY = untransformedSubfile.boundingBoxMin.y - untransformedSubfile.boundingBoxMax.y == 0f;
        boolean plainOnZ = untransformedSubfile.boundingBoxMin.z - untransformedSubfile.boundingBoxMax.z == 0f;

        if (avoidFlatScaling && (plainOnX || plainOnY || plainOnZ)) {

            final BigDecimal EPSILON = new BigDecimal("1.00000001"); //$NON-NLS-1$
            // Check if it's a rotation matrix
            BigDecimal discrX = transformation.M00.multiply(transformation.M00).add(transformation.M01.multiply(transformation.M01)).add(transformation.M02.multiply(transformation.M02));
            BigDecimal discrY = transformation.M10.multiply(transformation.M10).add(transformation.M11.multiply(transformation.M11)).add(transformation.M12.multiply(transformation.M12));
            BigDecimal discrZ = transformation.M20.multiply(transformation.M20).add(transformation.M21.multiply(transformation.M21)).add(transformation.M22.multiply(transformation.M22));
            if (discrX.compareTo(EPSILON) > 0 && discrY.compareTo(EPSILON) > 0 && discrZ.compareTo(EPSILON) > 0) {
                if (plainOnX && avoidFlatScaling) {
                    accurateLocalMatrix = accurateLocalMatrix.set(BigDecimal.ONE , 0, 0);
                    accurateLocalMatrix = accurateLocalMatrix.set(BigDecimal.ZERO, 0, 1);
                    accurateLocalMatrix = accurateLocalMatrix.set(BigDecimal.ZERO, 0, 2);
                }
                if (plainOnY && avoidFlatScaling) {
                    accurateLocalMatrix = accurateLocalMatrix.set(BigDecimal.ONE , 1, 1);
                    accurateLocalMatrix = accurateLocalMatrix.set(BigDecimal.ZERO, 1, 0);
                    accurateLocalMatrix = accurateLocalMatrix.set(BigDecimal.ZERO, 1, 2);
                }
                if (plainOnZ && avoidFlatScaling) {
                    accurateLocalMatrix = accurateLocalMatrix.set(BigDecimal.ONE , 2, 2);
                    accurateLocalMatrix = accurateLocalMatrix.set(BigDecimal.ZERO, 2, 0);
                    accurateLocalMatrix = accurateLocalMatrix.set(BigDecimal.ZERO, 2, 1);
                }
            }
        }

        df.getVertexManager().remove(untransformedSubfile);
        StringBuilder lineBuilder = new StringBuilder();
        lineBuilder.append(1);
        lineBuilder.append(" "); //$NON-NLS-1$
        if (colourNumber == -1) {
            lineBuilder.append("0x2"); //$NON-NLS-1$
            lineBuilder.append(MathHelper.toHex((int) (255f * r)).toUpperCase());
            lineBuilder.append(MathHelper.toHex((int) (255f * g)).toUpperCase());
            lineBuilder.append(MathHelper.toHex((int) (255f * b)).toUpperCase());
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

        {
            String[] data_segments = lineBuilder.toString().trim().split(" "); //$NON-NLS-1$

            boolean M00 = false;
            boolean M01 = false;
            boolean M02 = false;
            boolean M10 = false;
            boolean M11 = false;
            boolean M12 = false;
            boolean M20 = false;
            boolean M21 = false;
            boolean M22 = false;
            BigDecimal EPSILON = new BigDecimal(".00001"); //$NON-NLS-1$
            {
                int i = 0;
                for (String seg : data_segments) {
                    if (!seg.trim().equals("")) {  //$NON-NLS-1$
                        i++;
                        switch (i) {
                        case 6:
                            M00 = new BigDecimal(seg).abs().compareTo(EPSILON) < 0;
                            break;
                        case 7:
                            M01 = new BigDecimal(seg).abs().compareTo(EPSILON) < 0;
                            break;
                        case 8:
                            M02 = new BigDecimal(seg).abs().compareTo(EPSILON) < 0;
                            break;
                        case 9:
                            M10 = new BigDecimal(seg).abs().compareTo(EPSILON) < 0;
                            break;
                        case 10:
                            M11 = new BigDecimal(seg).abs().compareTo(EPSILON) < 0;
                            break;
                        case 11:
                            M12 = new BigDecimal(seg).abs().compareTo(EPSILON) < 0;
                            break;
                        case 12:
                            M20 = new BigDecimal(seg).abs().compareTo(EPSILON) < 0;
                            break;
                        case 13:
                            M21 = new BigDecimal(seg).abs().compareTo(EPSILON) < 0;
                            break;
                        case 14:
                            M22 = new BigDecimal(seg).abs().compareTo(EPSILON) < 0;
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
                return sb.toString();
            }
        }
    }

    public String getColouredString(String colour) {
        StringBuilder lineBuilder = new StringBuilder();
        lineBuilder.append(1);
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(colour);
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
        return lineBuilder.toString();
    }

    public String getRoundedString(int coordsDecimalPlaces, int matrixDecimalPlaces) {
        StringBuilder lineBuilder = new StringBuilder();
        lineBuilder.append(1);
        lineBuilder.append(" "); //$NON-NLS-1$
        if (colourNumber == -1) {
            lineBuilder.append("0x2"); //$NON-NLS-1$
            lineBuilder.append(MathHelper.toHex((int) (255f * r)).toUpperCase());
            lineBuilder.append(MathHelper.toHex((int) (255f * g)).toUpperCase());
            lineBuilder.append(MathHelper.toHex((int) (255f * b)).toUpperCase());
        } else {
            lineBuilder.append(colourNumber);
        }
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M30.setScale(coordsDecimalPlaces, RoundingMode.HALF_UP)));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M31.setScale(coordsDecimalPlaces, RoundingMode.HALF_UP)));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M32.setScale(coordsDecimalPlaces, RoundingMode.HALF_UP)));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M00.setScale(matrixDecimalPlaces, RoundingMode.HALF_UP)));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M10.setScale(matrixDecimalPlaces, RoundingMode.HALF_UP)));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M20.setScale(matrixDecimalPlaces, RoundingMode.HALF_UP)));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M01.setScale(matrixDecimalPlaces, RoundingMode.HALF_UP)));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M11.setScale(matrixDecimalPlaces, RoundingMode.HALF_UP)));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M21.setScale(matrixDecimalPlaces, RoundingMode.HALF_UP)));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M02.setScale(matrixDecimalPlaces, RoundingMode.HALF_UP)));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M12.setScale(matrixDecimalPlaces, RoundingMode.HALF_UP)));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(accurateLocalMatrix.M22.setScale(matrixDecimalPlaces, RoundingMode.HALF_UP)));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(shortName);
        return lineBuilder.toString();
    }

    @Override
    public String inlinedString(byte bfc, GColour colour) {
        boolean flipSurfaces = false;
        StringBuilder sb = new StringBuilder();
        float r = this.r;
        float g = this.g;
        float b = this.b;
        int colourNumber = this.colourNumber;
        if (Inliner.recursively) {
            sb = new StringBuilder();
            if (this.colourNumber == 16) {
                r = colour.getR();
                g = colour.getG();
                b = colour.getB();
                colourNumber = colour.getColourNumber();
            }
        } else {
            sb = new StringBuilder();
        }

        if (Inliner.withSubfileReference) {
            sb.append("0 !LPE INLINE " + getNiceString() + "<br>"); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            if (!(Inliner.recursively && !this.equals(this.firstRef)) && !Inliner.noComment) sb.append("0 // Inlined: " + getNiceString() + "<br>"); //$NON-NLS-1$ //$NON-NLS-2$ I18N Needs translation!
            if (negativeDeterminant) {
                if (bfc == BFC.CCW_CLIP) bfc = BFC.CW_CLIP;
                else if (bfc == BFC.CW_CLIP) bfc = BFC.CCW_CLIP;
            }
        }

        ArrayList<GData> dataToInline = new ArrayList<GData>();
        GData data2draw = myGData;
        while ((data2draw = data2draw.getNext()) != null) {
            dataToInline.add(data2draw);
        }

        byte lastBFC = BFC.NOCERTIFY;

        boolean foundInvertNext = false;
        for (GData gs : dataToInline) {
            switch (gs.type()) {
            case 0: // Comment
                String line = ((GData0) gs).text;
                line = line.replaceAll("\\s+", " ").trim(); //$NON-NLS-1$ //$NON-NLS-2$

                // Single vertex declaration
                if (line.startsWith("0 !LPE") && line.startsWith("VERTEX ", 7)) { //$NON-NLS-1$ //$NON-NLS-2$
                    String[] data_segments = line.trim().split("\\s+"); //$NON-NLS-1$
                    Vector3d start = new Vector3d();
                    boolean numberError = false;
                    if (data_segments.length == 6) {
                        try {
                            start.setX(new BigDecimal(data_segments[3], Threshold.mc));
                        } catch (NumberFormatException nfe) {
                            numberError = true;
                        }
                        try {
                            start.setY(new BigDecimal(data_segments[4], Threshold.mc));
                        } catch (NumberFormatException nfe) {
                            numberError = true;
                        }
                        try {
                            start.setZ(new BigDecimal(data_segments[5], Threshold.mc));
                        } catch (NumberFormatException nfe) {
                            numberError = true;
                        }
                    } else {
                        numberError = true;
                    }
                    if (!numberError) {
                        Vector4f vert = new Vector4f(start.getXf() * 1000f, start.getYf() * 1000f, start.getZf() * 1000f, 1f);
                        Matrix4f.transform(productMatrix, vert, vert);
                        vert.scale(.001f);
                        sb.append("0 !LPE VERTEX " + vert.x + " " + vert.y + " " + vert.z + "<br>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    } else {
                        sb.append(((GData0) gs).text + "<br>"); //$NON-NLS-1$
                    }
                } else if (!Inliner.recursively) {
                    sb.append(((GData0) gs).text + "<br>"); //$NON-NLS-1$
                }
                break;
            case 6: // BFC
                GDataBFC g6 = (GDataBFC) gs;
                if (Inliner.withSubfileReference) {
                    sb.append(g6.toString() + "<br>"); //$NON-NLS-1$
                } else {
                    if (g6.type == BFC.INVERTNEXT) {
                        if (lastBFC == BFC.NOCLIP) {
                            // Ignore invertnext on NOCLIP
                            if (!Inliner.recursively) sb.append("0 // " + g6.toString() + "<br>"); //$NON-NLS-1$ //$NON-NLS-2$
                        } else {
                            if (Inliner.recursively) {
                                if (bfc == BFC.CCW_CLIP) {
                                    bfc = BFC.CW_CLIP;
                                } else if (bfc == BFC.CW_CLIP) {
                                    bfc = BFC.CCW_CLIP;
                                }
                                foundInvertNext = true;
                            } else {
                                sb.append(g6.toString() + "<br>"); //$NON-NLS-1$
                            }
                        }
                    } else {
                        lastBFC = g6.type;
                        if (lastBFC == BFC.NOCERTIFY) {
                            // Ignore bfc statements on NOCERTIFY
                            if (!Inliner.recursively) sb.append("0 // " + g6.toString() + "<br>"); //$NON-NLS-1$ //$NON-NLS-2$
                        } else if (lastBFC == BFC.CCW_CLIP && bfc == BFC.CW_CLIP) {
                            flipSurfaces = true;
                            if (!Inliner.recursively) sb.append("0 // " + g6.toString() + "<br>"); //$NON-NLS-1$ //$NON-NLS-2$
                        } else if (lastBFC == BFC.CW_CLIP && bfc == BFC.CCW_CLIP) {
                            flipSurfaces = true;
                            if (!Inliner.recursively) sb.append("0 // " + g6.toString() + "<br>"); //$NON-NLS-1$ //$NON-NLS-2$
                        } else if (lastBFC != BFC.CCW_CLIP && g6.type != BFC.CW_CLIP) {
                            if (!Inliner.recursively) sb.append(g6.toString() + "<br>"); //$NON-NLS-1$
                        } else if (lastBFC == BFC.NOCLIP) {
                            sb.append(g6.toString() + "<br>"); //$NON-NLS-1$
                        } else {
                            if (!Inliner.recursively) sb.append("0 // " + g6.toString() + "<br>"); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    }
                }
                break;
            case 1: // Subfile
                if (Inliner.recursively) {
                    GData1 g1 = (GData1) gs;
                    GColour col = null;
                    if (g1.colourNumber == 16) {
                        col = new GColour(colourNumber, r, g, b, a);
                    } else {
                        col = new GColour(g1.colourNumber, g1.r, g1.g, g1.b, g1.a);
                    }
                    sb.append(g1.inlinedString(bfc, col));
                    if (foundInvertNext) {
                        if (bfc == BFC.CCW_CLIP) {
                            bfc = BFC.CW_CLIP;
                        } else if (bfc == BFC.CW_CLIP) {
                            bfc = BFC.CCW_CLIP;
                        }
                        foundInvertNext = false;
                    }
                } else {
                    GData1 g1 = (GData1) gs;
                    Matrix newMatrix = g1.accurateProductMatrix;
                    StringBuilder lineBuilder1 = new StringBuilder();
                    lineBuilder1.append(1);
                    lineBuilder1.append(" "); //$NON-NLS-1$
                    if (g1.colourNumber == -1) {
                        lineBuilder1.append("0x2"); //$NON-NLS-1$
                        lineBuilder1.append(MathHelper.toHex((int) (255f * g1.r)).toUpperCase());
                        lineBuilder1.append(MathHelper.toHex((int) (255f * g1.g)).toUpperCase());
                        lineBuilder1.append(MathHelper.toHex((int) (255f * g1.b)).toUpperCase());
                    } else if (g1.colourNumber == 16) {
                        if (colourNumber == -1) {
                            lineBuilder1.append("0x2"); //$NON-NLS-1$
                            lineBuilder1.append(MathHelper.toHex((int) (255f * r)).toUpperCase());
                            lineBuilder1.append(MathHelper.toHex((int) (255f * g)).toUpperCase());
                            lineBuilder1.append(MathHelper.toHex((int) (255f * b)).toUpperCase());
                        } else {
                            lineBuilder1.append(colourNumber);
                        }
                    } else {
                        lineBuilder1.append(g1.colourNumber);
                    }
                    lineBuilder1.append(" "); //$NON-NLS-1$
                    lineBuilder1.append(bigDecimalToString(newMatrix.M30));
                    lineBuilder1.append(" "); //$NON-NLS-1$
                    lineBuilder1.append(bigDecimalToString(newMatrix.M31));
                    lineBuilder1.append(" "); //$NON-NLS-1$
                    lineBuilder1.append(bigDecimalToString(newMatrix.M32));
                    lineBuilder1.append(" "); //$NON-NLS-1$
                    lineBuilder1.append(bigDecimalToString(newMatrix.M00));
                    lineBuilder1.append(" "); //$NON-NLS-1$
                    lineBuilder1.append(bigDecimalToString(newMatrix.M10));
                    lineBuilder1.append(" "); //$NON-NLS-1$
                    lineBuilder1.append(bigDecimalToString(newMatrix.M20));
                    lineBuilder1.append(" "); //$NON-NLS-1$
                    lineBuilder1.append(bigDecimalToString(newMatrix.M01));
                    lineBuilder1.append(" "); //$NON-NLS-1$
                    lineBuilder1.append(bigDecimalToString(newMatrix.M11));
                    lineBuilder1.append(" "); //$NON-NLS-1$
                    lineBuilder1.append(bigDecimalToString(newMatrix.M21));
                    lineBuilder1.append(" "); //$NON-NLS-1$
                    lineBuilder1.append(bigDecimalToString(newMatrix.M02));
                    lineBuilder1.append(" "); //$NON-NLS-1$
                    lineBuilder1.append(bigDecimalToString(newMatrix.M12));
                    lineBuilder1.append(" "); //$NON-NLS-1$
                    lineBuilder1.append(bigDecimalToString(newMatrix.M22));
                    lineBuilder1.append(" "); //$NON-NLS-1$
                    lineBuilder1.append(g1.shortName);
                    sb.append(lineBuilder1.toString() + "<br>"); //$NON-NLS-1$
                }
                break;
            case 2: // Line
                GData2 g2 = (GData2) gs;
                StringBuilder lineBuilder2 = new StringBuilder();
                lineBuilder2.append(2);
                lineBuilder2.append(" "); //$NON-NLS-1$
                if (g2.colourNumber == -1) {
                    lineBuilder2.append("0x2"); //$NON-NLS-1$
                    lineBuilder2.append(MathHelper.toHex((int) (255f * g2.r)).toUpperCase());
                    lineBuilder2.append(MathHelper.toHex((int) (255f * g2.g)).toUpperCase());
                    lineBuilder2.append(MathHelper.toHex((int) (255f * g2.b)).toUpperCase());
                } else if (g2.colourNumber == 16) {
                    if (colourNumber == -1) {
                        lineBuilder2.append("0x2"); //$NON-NLS-1$
                        lineBuilder2.append(MathHelper.toHex((int) (255f * r)).toUpperCase());
                        lineBuilder2.append(MathHelper.toHex((int) (255f * g)).toUpperCase());
                        lineBuilder2.append(MathHelper.toHex((int) (255f * b)).toUpperCase());
                    } else {
                        lineBuilder2.append(colourNumber);
                    }
                } else {
                    lineBuilder2.append(g2.colourNumber);
                }
                BigDecimal[] g2_v1 = accurateProductMatrix.transform(g2.X1, g2.Y1, g2.Z1);
                BigDecimal[] g2_v2 = accurateProductMatrix.transform(g2.X2, g2.Y2, g2.Z2);
                lineBuilder2.append(" "); //$NON-NLS-1$
                lineBuilder2.append(bigDecimalToString(g2_v1[0]));
                lineBuilder2.append(" "); //$NON-NLS-1$
                lineBuilder2.append(bigDecimalToString(g2_v1[1]));
                lineBuilder2.append(" "); //$NON-NLS-1$
                lineBuilder2.append(bigDecimalToString(g2_v1[2]));
                lineBuilder2.append(" "); //$NON-NLS-1$
                lineBuilder2.append(bigDecimalToString(g2_v2[0]));
                lineBuilder2.append(" "); //$NON-NLS-1$
                lineBuilder2.append(bigDecimalToString(g2_v2[1]));
                lineBuilder2.append(" "); //$NON-NLS-1$
                lineBuilder2.append(bigDecimalToString(g2_v2[2]));
                sb.append(lineBuilder2.toString() + "<br>"); //$NON-NLS-1$
                break;
            case 3: // Triangle
                GData3 g3 = (GData3) gs;
                StringBuilder lineBuilder3 = new StringBuilder();
                lineBuilder3.append(3);
                lineBuilder3.append(" "); //$NON-NLS-1$
                if (g3.colourNumber == -1) {
                    lineBuilder3.append("0x2"); //$NON-NLS-1$
                    lineBuilder3.append(MathHelper.toHex((int) (255f * g3.r)).toUpperCase());
                    lineBuilder3.append(MathHelper.toHex((int) (255f * g3.g)).toUpperCase());
                    lineBuilder3.append(MathHelper.toHex((int) (255f * g3.b)).toUpperCase());
                } else if (g3.colourNumber == 16) {
                    if (colourNumber == -1) {
                        lineBuilder3.append("0x2"); //$NON-NLS-1$
                        lineBuilder3.append(MathHelper.toHex((int) (255f * r)).toUpperCase());
                        lineBuilder3.append(MathHelper.toHex((int) (255f * g)).toUpperCase());
                        lineBuilder3.append(MathHelper.toHex((int) (255f * b)).toUpperCase());
                    } else {
                        lineBuilder3.append(colourNumber);
                    }
                } else {
                    lineBuilder3.append(g3.colourNumber);
                }
                BigDecimal[] g3_v1 = accurateProductMatrix.transform(g3.X1, g3.Y1, g3.Z1);
                BigDecimal[] g3_v2 = accurateProductMatrix.transform(g3.X2, g3.Y2, g3.Z2);
                BigDecimal[] g3_v3 = accurateProductMatrix.transform(g3.X3, g3.Y3, g3.Z3);
                if (flipSurfaces) {
                    BigDecimal[] temp = g3_v1;
                    g3_v1 = g3_v2;
                    g3_v2 = temp;
                }

                lineBuilder3.append(" "); //$NON-NLS-1$
                lineBuilder3.append(bigDecimalToString(g3_v1[0]));
                lineBuilder3.append(" "); //$NON-NLS-1$
                lineBuilder3.append(bigDecimalToString(g3_v1[1]));
                lineBuilder3.append(" "); //$NON-NLS-1$
                lineBuilder3.append(bigDecimalToString(g3_v1[2]));
                lineBuilder3.append(" "); //$NON-NLS-1$
                lineBuilder3.append(bigDecimalToString(g3_v2[0]));
                lineBuilder3.append(" "); //$NON-NLS-1$
                lineBuilder3.append(bigDecimalToString(g3_v2[1]));
                lineBuilder3.append(" "); //$NON-NLS-1$
                lineBuilder3.append(bigDecimalToString(g3_v2[2]));
                lineBuilder3.append(" "); //$NON-NLS-1$
                lineBuilder3.append(bigDecimalToString(g3_v3[0]));
                lineBuilder3.append(" "); //$NON-NLS-1$
                lineBuilder3.append(bigDecimalToString(g3_v3[1]));
                lineBuilder3.append(" "); //$NON-NLS-1$
                lineBuilder3.append(bigDecimalToString(g3_v3[2]));
                sb.append(lineBuilder3.toString() + "<br>"); //$NON-NLS-1$
                break;
            case 4: // Quad
                GData4 g4 = (GData4) gs;
                StringBuilder lineBuilder4 = new StringBuilder();
                lineBuilder4.append(4);
                lineBuilder4.append(" "); //$NON-NLS-1$
                if (g4.colourNumber == -1) {
                    lineBuilder4.append("0x2"); //$NON-NLS-1$
                    lineBuilder4.append(MathHelper.toHex((int) (255f * g4.r)).toUpperCase());
                    lineBuilder4.append(MathHelper.toHex((int) (255f * g4.g)).toUpperCase());
                    lineBuilder4.append(MathHelper.toHex((int) (255f * g4.b)).toUpperCase());
                } else if (g4.colourNumber == 16) {
                    if (colourNumber == -1) {
                        lineBuilder4.append("0x2"); //$NON-NLS-1$
                        lineBuilder4.append(MathHelper.toHex((int) (255f * r)).toUpperCase());
                        lineBuilder4.append(MathHelper.toHex((int) (255f * g)).toUpperCase());
                        lineBuilder4.append(MathHelper.toHex((int) (255f * b)).toUpperCase());
                    } else {
                        lineBuilder4.append(colourNumber);
                    }
                } else {
                    lineBuilder4.append(g4.colourNumber);
                }
                BigDecimal[] g4_v1 = accurateProductMatrix.transform(g4.X1, g4.Y1, g4.Z1);
                BigDecimal[] g4_v2 = accurateProductMatrix.transform(g4.X2, g4.Y2, g4.Z2);
                BigDecimal[] g4_v3 = accurateProductMatrix.transform(g4.X3, g4.Y3, g4.Z3);
                BigDecimal[] g4_v4 = accurateProductMatrix.transform(g4.X4, g4.Y4, g4.Z4);
                if (flipSurfaces) {
                    BigDecimal[] temp = g4_v2;
                    g4_v2 = g4_v4;
                    g4_v4 = temp;
                }
                lineBuilder4.append(" "); //$NON-NLS-1$
                lineBuilder4.append(bigDecimalToString(g4_v1[0]));
                lineBuilder4.append(" "); //$NON-NLS-1$
                lineBuilder4.append(bigDecimalToString(g4_v1[1]));
                lineBuilder4.append(" "); //$NON-NLS-1$
                lineBuilder4.append(bigDecimalToString(g4_v1[2]));
                lineBuilder4.append(" "); //$NON-NLS-1$
                lineBuilder4.append(bigDecimalToString(g4_v2[0]));
                lineBuilder4.append(" "); //$NON-NLS-1$
                lineBuilder4.append(bigDecimalToString(g4_v2[1]));
                lineBuilder4.append(" "); //$NON-NLS-1$
                lineBuilder4.append(bigDecimalToString(g4_v2[2]));
                lineBuilder4.append(" "); //$NON-NLS-1$
                lineBuilder4.append(bigDecimalToString(g4_v3[0]));
                lineBuilder4.append(" "); //$NON-NLS-1$
                lineBuilder4.append(bigDecimalToString(g4_v3[1]));
                lineBuilder4.append(" "); //$NON-NLS-1$
                lineBuilder4.append(bigDecimalToString(g4_v3[2]));
                lineBuilder4.append(" "); //$NON-NLS-1$
                lineBuilder4.append(bigDecimalToString(g4_v4[0]));
                lineBuilder4.append(" "); //$NON-NLS-1$
                lineBuilder4.append(bigDecimalToString(g4_v4[1]));
                lineBuilder4.append(" "); //$NON-NLS-1$
                lineBuilder4.append(bigDecimalToString(g4_v4[2]));
                sb.append(lineBuilder4.toString() + "<br>"); //$NON-NLS-1$
                break;
            case 5: // Condline
                GData5 g5 = (GData5) gs;
                StringBuilder lineBuilder5 = new StringBuilder();
                lineBuilder5.append(5);
                lineBuilder5.append(" "); //$NON-NLS-1$
                if (g5.colourNumber == -1) {
                    lineBuilder5.append("0x2"); //$NON-NLS-1$
                    lineBuilder5.append(MathHelper.toHex((int) (255f * g5.r)).toUpperCase());
                    lineBuilder5.append(MathHelper.toHex((int) (255f * g5.g)).toUpperCase());
                    lineBuilder5.append(MathHelper.toHex((int) (255f * g5.b)).toUpperCase());
                } else if (g5.colourNumber == 16) {
                    if (colourNumber == -1) {
                        lineBuilder5.append("0x2"); //$NON-NLS-1$
                        lineBuilder5.append(MathHelper.toHex((int) (255f * r)).toUpperCase());
                        lineBuilder5.append(MathHelper.toHex((int) (255f * g)).toUpperCase());
                        lineBuilder5.append(MathHelper.toHex((int) (255f * b)).toUpperCase());
                    } else {
                        lineBuilder5.append(colourNumber);
                    }
                } else {
                    lineBuilder5.append(g5.colourNumber);
                }
                BigDecimal[] g5_v1 = accurateProductMatrix.transform(g5.X1, g5.Y1, g5.Z1);
                BigDecimal[] g5_v2 = accurateProductMatrix.transform(g5.X2, g5.Y2, g5.Z2);
                BigDecimal[] g5_v3 = accurateProductMatrix.transform(g5.X3, g5.Y3, g5.Z3);
                BigDecimal[] g5_v4 = accurateProductMatrix.transform(g5.X4, g5.Y4, g5.Z4);
                lineBuilder5.append(" "); //$NON-NLS-1$
                lineBuilder5.append(bigDecimalToString(g5_v1[0]));
                lineBuilder5.append(" "); //$NON-NLS-1$
                lineBuilder5.append(bigDecimalToString(g5_v1[1]));
                lineBuilder5.append(" "); //$NON-NLS-1$
                lineBuilder5.append(bigDecimalToString(g5_v1[2]));
                lineBuilder5.append(" "); //$NON-NLS-1$
                lineBuilder5.append(bigDecimalToString(g5_v2[0]));
                lineBuilder5.append(" "); //$NON-NLS-1$
                lineBuilder5.append(bigDecimalToString(g5_v2[1]));
                lineBuilder5.append(" "); //$NON-NLS-1$
                lineBuilder5.append(bigDecimalToString(g5_v2[2]));
                lineBuilder5.append(" "); //$NON-NLS-1$
                lineBuilder5.append(bigDecimalToString(g5_v3[0]));
                lineBuilder5.append(" "); //$NON-NLS-1$
                lineBuilder5.append(bigDecimalToString(g5_v3[1]));
                lineBuilder5.append(" "); //$NON-NLS-1$
                lineBuilder5.append(bigDecimalToString(g5_v3[2]));
                lineBuilder5.append(" "); //$NON-NLS-1$
                lineBuilder5.append(bigDecimalToString(g5_v4[0]));
                lineBuilder5.append(" "); //$NON-NLS-1$
                lineBuilder5.append(bigDecimalToString(g5_v4[1]));
                lineBuilder5.append(" "); //$NON-NLS-1$
                lineBuilder5.append(bigDecimalToString(g5_v4[2]));
                sb.append(lineBuilder5.toString() + "<br>"); //$NON-NLS-1$
                break;
            case 8: // CSG Statement
                GDataCSG g8 = (GDataCSG) gs;
                byte csgType = g8.getCSGtype();
                StringBuilder lineBuilder8 = new StringBuilder();

                String line2 = ((GDataCSG) gs).text;
                line2 = line2.replaceAll("\\s+", " ").trim(); //$NON-NLS-1$ //$NON-NLS-2$
                String[] data_segments2 = line2.trim().split("\\s+"); //$NON-NLS-1$

                switch (csgType) {

                case CSG.QUAD:
                    lineBuilder8.append("0 !LPE CSG_QUAD "); //$NON-NLS-1$
                    break;
                case CSG.CUBOID:
                    lineBuilder8.append("0 !LPE CSG_CUBOID "); //$NON-NLS-1$
                    break;
                case CSG.ELLIPSOID:
                    lineBuilder8.append("0 !LPE CSG_ELLIPSOID "); //$NON-NLS-1$
                    break;
                case CSG.CIRCLE:
                    lineBuilder8.append("0 !LPE CSG_CIRCLE "); //$NON-NLS-1$
                    break;
                case CSG.CYLINDER:
                    lineBuilder8.append("0 !LPE CSG_CYLINDER "); //$NON-NLS-1$
                    break;

                case CSG.INTERSECTION:
                    lineBuilder8.append("0 !LPE CSG_INTERSECTION "); //$NON-NLS-1$
                    break;
                case CSG.DIFFERENCE:
                    lineBuilder8.append("0 !LPE CSG_DIFFERENCE "); //$NON-NLS-1$
                    break;
                case CSG.UNION:
                    lineBuilder8.append("0 !LPE CSG_UNION "); //$NON-NLS-1$
                    break;
                default:
                    break;
                }
                switch (csgType) {
                case CSG.INTERSECTION:
                case CSG.DIFFERENCE:
                case CSG.UNION:
                    lineBuilder8.append(data_segments2[3] + this.ID + " " + data_segments2[4] + this.ID + " " + data_segments2[5] + this.ID); //$NON-NLS-1$ //$NON-NLS-2$
                    break;
                case CSG.QUAD:
                case CSG.CUBOID:
                case CSG.ELLIPSOID:
                case CSG.CIRCLE:
                case CSG.CYLINDER:
                    lineBuilder8.append(data_segments2[3] + this.ID + " " + data_segments2[4] + " " + //$NON-NLS-1$ //$NON-NLS-2$
                            MathHelper.csgMatrixMult(g8.matrix, productMatrix));
                    break;
                case CSG.QUALITY:
                case CSG.EPSILON:
                    lineBuilder8.append(g8.getNiceString());
                    break;
                case CSG.COMPILE:
                    lineBuilder8.append("0 !LPE CSG_COMPILE " + data_segments2[3] + this.ID); //$NON-NLS-1$
                    break;
                default:
                    break;
                }
                sb.append(lineBuilder8.toString() + "<br>"); //$NON-NLS-1$
                break;
            case 9: // TEXMAP Statement
                GDataTEX g9 = (GDataTEX) gs;
                sb.append(g9.transformAndColourReplace(null, accurateProductMatrix) + "<br>"); //$NON-NLS-1$
                break;
            default:
                break;
            }
        }
        if (Inliner.withSubfileReference)
            sb.append("0 !LPE INLINE_END<br>"); //$NON-NLS-1$
        if (lastBFC == BFC.NOCLIP) {
            sb.append(new GDataBFC(BFC.CLIP).toString() + "<br>"); //$NON-NLS-1$
        }
        return sb.toString();
    }

    @Override
    public String transformAndColourReplace(String colour, Matrix matrix) {
        Matrix localMatrix;
        if (accurateLocalMatrix == null) {
            localMatrix = Matrix.mul(matrix, new Matrix(this.localMatrix));
        } else {
            localMatrix = Matrix.mul(matrix, accurateLocalMatrix);
        }
        StringBuilder lineBuilder = new StringBuilder();
        lineBuilder.append(1);
        lineBuilder.append(" "); //$NON-NLS-1$
        StringBuilder colourBuilder = new StringBuilder();
        if (colourNumber == -1) {
            colourBuilder.append("0x2"); //$NON-NLS-1$
            colourBuilder.append(MathHelper.toHex((int) (255f * r)).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * g)).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * b)).toUpperCase());
        } else {
            colourBuilder.append(colourNumber);
        }
        String col = colourBuilder.toString();
        if (col.equals(colour))
            col = "16"; //$NON-NLS-1$
        lineBuilder.append(col);
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(localMatrix.M30));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(localMatrix.M31));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(localMatrix.M32));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(localMatrix.M00));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(localMatrix.M10));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(localMatrix.M20));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(localMatrix.M01));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(localMatrix.M11));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(localMatrix.M21));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(localMatrix.M02));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(localMatrix.M12));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(bigDecimalToString(localMatrix.M22));
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(shortName);
        return lineBuilder.toString();
    }

    private static void drawStudLogo1() {
        final float Y = -4.04f;
        GL11.glLineWidth(2f);
        GL11.glColor4f(0f, 0f, 0f, 1f);
        // Letter L
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3f(2.5f * 1000f, Y * 1000f, -3.3f * 1000f);
        GL11.glVertex3f(2.48f * 1000f, Y * 1000f, -3.2f * 1000f);
        GL11.glVertex3f(2.43f * 1000f, Y * 1000f, -3.12f * 1000f);
        GL11.glVertex3f(2.35f * 1000f, Y * 1000f, -3.07f * 1000f);
        GL11.glVertex3f(2.25f * 1000f, Y * 1000f, -3.05f * 1000f);
        GL11.glVertex3f(2.15f * 1000f, Y * 1000f, -3.07f * 1000f);
        GL11.glVertex3f(2.07f * 1000f, Y * 1000f, -3.12f * 1000f);
        GL11.glVertex3f(2.02f * 1000f, Y * 1000f, -3.2f * 1000f);
        GL11.glVertex3f(2f * 1000f, Y * 1000f, -3.3f * 1000f);
        GL11.glVertex3f(2f * 1000f, Y * 1000f, -4.08f * 1000f);
        GL11.glVertex3f(-1.95f * 1000f, Y * 1000f, -3.05f * 1000f);
        GL11.glVertex3f(-2.05f * 1000f, Y * 1000f, -3.07f * 1000f);
        GL11.glVertex3f(-2.13f * 1000f, Y * 1000f, -3.12f * 1000f);
        GL11.glVertex3f(-2.18f * 1000f, Y * 1000f, -3.2f * 1000f);
        GL11.glVertex3f(-2.2f * 1000f, Y * 1000f, -3.3f * 1000f);
        GL11.glVertex3f(-2.18f * 1000f, Y * 1000f, -3.4f * 1000f);
        GL11.glVertex3f(-2.13f * 1000f, Y * 1000f, -3.48f * 1000f);
        GL11.glVertex3f(-2.05f * 1000f, Y * 1000f, -3.53f * 1000f);
        GL11.glVertex3f(-1.95f * 1000f, Y * 1000f, -3.55f * 1000f);
        GL11.glVertex3f(2.25f * 1000f, Y * 1000f, -4.65f * 1000f);
        GL11.glVertex3f(2.35f * 1000f, Y * 1000f, -4.63f * 1000f);
        GL11.glVertex3f(2.43f * 1000f, Y * 1000f, -4.58f * 1000f);
        GL11.glVertex3f(2.48f * 1000f, Y * 1000f, -4.5f * 1000f);
        GL11.glVertex3f(2.5f * 1000f, Y * 1000f, -4.4f * 1000f);
        GL11.glVertex3f(2.5f * 1000f, Y * 1000f, -3.3f * 1000f);
        GL11.glEnd();
        // Letter E
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3f(-0.15f * 1000f, Y * 1000f, -1.42f * 1000f);
        GL11.glVertex3f(-1.7f * 1000f, Y * 1000f, -1.02f * 1000f);
        GL11.glVertex3f(-1.7f * 1000f, Y * 1000f, -0.1f * 1000f);
        GL11.glVertex3f(-1.72f * 1000f, Y * 1000f, 0f * 1000f);
        GL11.glVertex3f(-1.77f * 1000f, Y * 1000f, 0.08f * 1000f);
        GL11.glVertex3f(-1.85f * 1000f, Y * 1000f, 0.13f * 1000f);
        GL11.glVertex3f(-1.95f * 1000f, Y * 1000f, 0.15f * 1000f);
        GL11.glVertex3f(-2.05f * 1000f, Y * 1000f, 0.13f * 1000f);
        GL11.glVertex3f(-2.13f * 1000f, Y * 1000f, 0.08f * 1000f);
        GL11.glVertex3f(-2.18f * 1000f, Y * 1000f, 0f * 1000f);
        GL11.glVertex3f(-2.2f * 1000f, Y * 1000f, -0.1f * 1000f);
        GL11.glVertex3f(-2.2f * 1000f, Y * 1000f, -1.2f * 1000f);
        GL11.glVertex3f(-2.18f * 1000f, Y * 1000f, -1.3f * 1000f);
        GL11.glVertex3f(-2.13f * 1000f, Y * 1000f, -1.38f * 1000f);
        GL11.glVertex3f(-2.05f * 1000f, Y * 1000f, -1.43f * 1000f);
        GL11.glVertex3f(-1.95f * 1000f, Y * 1000f, -1.45f * 1000f);
        GL11.glVertex3f(2.25f * 1000f, Y * 1000f, -2.55f * 1000f);
        GL11.glVertex3f(2.35f * 1000f, Y * 1000f, -2.53f * 1000f);
        GL11.glVertex3f(2.43f * 1000f, Y * 1000f, -2.48f * 1000f);
        GL11.glVertex3f(2.48f * 1000f, Y * 1000f, -2.4f * 1000f);
        GL11.glVertex3f(2.5f * 1000f, Y * 1000f, -2.3f * 1000f);
        GL11.glVertex3f(2.5f * 1000f, Y * 1000f, -1.2f * 1000f);
        GL11.glVertex3f(2.48f * 1000f, Y * 1000f, -1.1f * 1000f);
        GL11.glVertex3f(2.43f * 1000f, Y * 1000f, -1.02f * 1000f);
        GL11.glVertex3f(2.35f * 1000f, Y * 1000f, -0.97f * 1000f);
        GL11.glVertex3f(2.25f * 1000f, Y * 1000f, -0.95f * 1000f);
        GL11.glVertex3f(2.15f * 1000f, Y * 1000f, -0.97f * 1000f);
        GL11.glVertex3f(2.07f * 1000f, Y * 1000f, -1.02f * 1000f);
        GL11.glVertex3f(2.02f * 1000f, Y * 1000f, -1.1f * 1000f);
        GL11.glVertex3f(2f * 1000f, Y * 1000f, -1.2f * 1000f);
        GL11.glVertex3f(2f * 1000f, Y * 1000f, -1.98f * 1000f);
        GL11.glVertex3f(0.35f * 1000f, Y * 1000f, -1.55f * 1000f);
        GL11.glVertex3f(0.35f * 1000f, Y * 1000f, -1f * 1000f);
        GL11.glVertex3f(0.33f * 1000f, Y * 1000f, -0.9f * 1000f);
        GL11.glVertex3f(0.28f * 1000f, Y * 1000f, -0.82f * 1000f);
        GL11.glVertex3f(0.2f * 1000f, Y * 1000f, -0.77f * 1000f);
        GL11.glVertex3f(0.1f * 1000f, Y * 1000f, -0.75f * 1000f);
        GL11.glVertex3f(0f * 1000f, Y * 1000f, -0.77f * 1000f);
        GL11.glVertex3f(-0.08f * 1000f, Y * 1000f, -0.82f * 1000f);
        GL11.glVertex3f(-0.13f * 1000f, Y * 1000f, -0.9f * 1000f);
        GL11.glVertex3f(-0.15f * 1000f, Y * 1000f, -1f * 1000f);
        GL11.glVertex3f(-0.15f * 1000f, Y * 1000f, -1.42f * 1000f);
        GL11.glEnd();
        // Letter G
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3f(-1.32f * 1000f, Y * 1000f, 0.74f * 1000f);
        GL11.glVertex3f(-1.5f * 1000f, Y * 1000f, 0.83f * 1000f);
        GL11.glVertex3f(-1.63f * 1000f, Y * 1000f, 0.97f * 1000f);
        GL11.glVertex3f(-1.69f * 1000f, Y * 1000f, 1.16f * 1000f);
        GL11.glVertex3f(-1.68f * 1000f, Y * 1000f, 1.35f * 1000f);
        GL11.glVertex3f(-1.59f * 1000f, Y * 1000f, 1.53f * 1000f);
        GL11.glVertex3f(-1.44f * 1000f, Y * 1000f, 1.65f * 1000f);
        GL11.glVertex3f(-1.26f * 1000f, Y * 1000f, 1.72f * 1000f);
        GL11.glVertex3f(-1.06f * 1000f, Y * 1000f, 1.71f * 1000f);
        GL11.glVertex3f(-1.07f * 1000f, Y * 1000f, 1.71f * 1000f);
        GL11.glVertex3f(-0.97f * 1000f, Y * 1000f, 1.7f * 1000f);
        GL11.glVertex3f(-0.88f * 1000f, Y * 1000f, 1.73f * 1000f);
        GL11.glVertex3f(-0.8f * 1000f, Y * 1000f, 1.79f * 1000f);
        GL11.glVertex3f(-0.76f * 1000f, Y * 1000f, 1.88f * 1000f);
        GL11.glVertex3f(-0.75f * 1000f, Y * 1000f, 1.98f * 1000f);
        GL11.glVertex3f(-0.78f * 1000f, Y * 1000f, 2.07f * 1000f);
        GL11.glVertex3f(-0.85f * 1000f, Y * 1000f, 2.14f * 1000f);
        GL11.glVertex3f(-0.94f * 1000f, Y * 1000f, 2.19f * 1000f);
        GL11.glVertex3f(-1.32f * 1000f, Y * 1000f, 2.21f * 1000f);
        GL11.glVertex3f(-1.69f * 1000f, Y * 1000f, 2.09f * 1000f);
        GL11.glVertex3f(-1.99f * 1000f, Y * 1000f, 1.83f * 1000f);
        GL11.glVertex3f(-2.16f * 1000f, Y * 1000f, 1.48f * 1000f);
        GL11.glVertex3f(-2.19f * 1000f, Y * 1000f, 1.09f * 1000f);
        GL11.glVertex3f(-2.06f * 1000f, Y * 1000f, 0.72f * 1000f);
        GL11.glVertex3f(-1.8f * 1000f, Y * 1000f, 0.43f * 1000f);
        GL11.glVertex3f(-1.45f * 1000f, Y * 1000f, 0.26f * 1000f);
        GL11.glVertex3f(1.24f * 1000f, Y * 1000f, -0.47f * 1000f);
        GL11.glVertex3f(1.63f * 1000f, Y * 1000f, -0.49f * 1000f);
        GL11.glVertex3f(2f * 1000f, Y * 1000f, -0.37f * 1000f);
        GL11.glVertex3f(2.29f * 1000f, Y * 1000f, -0.11f * 1000f);
        GL11.glVertex3f(2.47f * 1000f, Y * 1000f, 0.24f * 1000f);
        GL11.glVertex3f(2.49f * 1000f, Y * 1000f, 0.63f * 1000f);
        GL11.glVertex3f(2.37f * 1000f, Y * 1000f, 1f * 1000f);
        GL11.glVertex3f(2.11f * 1000f, Y * 1000f, 1.29f * 1000f);
        GL11.glVertex3f(1.76f * 1000f, Y * 1000f, 1.47f * 1000f);
        GL11.glVertex3f(0.1f * 1000f, Y * 1000f, 1.91f * 1000f);
        GL11.glVertex3f(0f * 1000f, Y * 1000f, 1.89f * 1000f);
        GL11.glVertex3f(-0.08f * 1000f, Y * 1000f, 1.83f * 1000f);
        GL11.glVertex3f(-0.13f * 1000f, Y * 1000f, 1.75f * 1000f);
        GL11.glVertex3f(-0.15f * 1000f, Y * 1000f, 1.65f * 1000f);
        GL11.glVertex3f(-0.15f * 1000f, Y * 1000f, 0.85f * 1000f);
        GL11.glVertex3f(-0.13f * 1000f, Y * 1000f, 0.75f * 1000f);
        GL11.glVertex3f(-0.08f * 1000f, Y * 1000f, 0.67f * 1000f);
        GL11.glVertex3f(0f * 1000f, Y * 1000f, 0.62f * 1000f);
        GL11.glVertex3f(0.1f * 1000f, Y * 1000f, 0.6f * 1000f);
        GL11.glVertex3f(0.2f * 1000f, Y * 1000f, 0.62f * 1000f);
        GL11.glVertex3f(0.28f * 1000f, Y * 1000f, 0.67f * 1000f);
        GL11.glVertex3f(0.33f * 1000f, Y * 1000f, 0.75f * 1000f);
        GL11.glVertex3f(0.35f * 1000f, Y * 1000f, 0.85f * 1000f);
        GL11.glVertex3f(0.35f * 1000f, Y * 1000f, 1.33f * 1000f);
        GL11.glVertex3f(1.63f * 1000f, Y * 1000f, 0.98f * 1000f);
        GL11.glVertex3f(1.8f * 1000f, Y * 1000f, 0.9f * 1000f);
        GL11.glVertex3f(1.93f * 1000f, Y * 1000f, 0.75f * 1000f);
        GL11.glVertex3f(2f * 1000f, Y * 1000f, 0.57f * 1000f);
        GL11.glVertex3f(1.98f * 1000f, Y * 1000f, 0.37f * 1000f);
        GL11.glVertex3f(1.9f * 1000f, Y * 1000f, 0.2f * 1000f);
        GL11.glVertex3f(1.75f * 1000f, Y * 1000f, 0.07f * 1000f);
        GL11.glVertex3f(1.57f * 1000f, Y * 1000f, 0f * 1000f);
        GL11.glVertex3f(1.37f * 1000f, Y * 1000f, 0.02f * 1000f);
        GL11.glVertex3f(-1.32f * 1000f, Y * 1000f, 0.74f * 1000f);
        GL11.glEnd();
        // Letter O
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3f(-1.06f * 1000f, Y * 1000f, 4.11f * 1000f);
        GL11.glVertex3f(-1.26f * 1000f, Y * 1000f, 4.12f * 1000f);
        GL11.glVertex3f(-1.44f * 1000f, Y * 1000f, 4.05f * 1000f);
        GL11.glVertex3f(-1.59f * 1000f, Y * 1000f, 3.93f * 1000f);
        GL11.glVertex3f(-1.68f * 1000f, Y * 1000f, 3.75f * 1000f);
        GL11.glVertex3f(-1.69f * 1000f, Y * 1000f, 3.56f * 1000f);
        GL11.glVertex3f(-1.63f * 1000f, Y * 1000f, 3.37f * 1000f);
        GL11.glVertex3f(-1.5f * 1000f, Y * 1000f, 3.23f * 1000f);
        GL11.glVertex3f(-1.32f * 1000f, Y * 1000f, 3.14f * 1000f);
        GL11.glVertex3f(1.37f * 1000f, Y * 1000f, 2.42f * 1000f);
        GL11.glVertex3f(1.57f * 1000f, Y * 1000f, 2.4f * 1000f);
        GL11.glVertex3f(1.75f * 1000f, Y * 1000f, 2.47f * 1000f);
        GL11.glVertex3f(1.9f * 1000f, Y * 1000f, 2.6f * 1000f);
        GL11.glVertex3f(1.98f * 1000f, Y * 1000f, 2.77f * 1000f);
        GL11.glVertex3f(2f * 1000f, Y * 1000f, 2.97f * 1000f);
        GL11.glVertex3f(1.93f * 1000f, Y * 1000f, 3.15f * 1000f);
        GL11.glVertex3f(1.8f * 1000f, Y * 1000f, 3.3f * 1000f);
        GL11.glVertex3f(1.63f * 1000f, Y * 1000f, 3.38f * 1000f);
        GL11.glVertex3f(-1.07f * 1000f, Y * 1000f, 4.1f * 1000f);
        GL11.glVertex3f(-1.06f * 1000f, Y * 1000f, 4.11f * 1000f);
        GL11.glEnd();
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3f(-1.45f * 1000f, Y * 1000f, 2.66f * 1000f);
        GL11.glVertex3f(-1.8f * 1000f, Y * 1000f, 2.83f * 1000f);
        GL11.glVertex3f(-2.06f * 1000f, Y * 1000f, 3.12f * 1000f);
        GL11.glVertex3f(-2.19f * 1000f, Y * 1000f, 3.49f * 1000f);
        GL11.glVertex3f(-2.16f * 1000f, Y * 1000f, 3.88f * 1000f);
        GL11.glVertex3f(-1.99f * 1000f, Y * 1000f, 4.23f * 1000f);
        GL11.glVertex3f(-1.69f * 1000f, Y * 1000f, 4.49f * 1000f);
        GL11.glVertex3f(-1.32f * 1000f, Y * 1000f, 4.61f * 1000f);
        GL11.glVertex3f(-0.94f * 1000f, Y * 1000f, 4.59f * 1000f);
        GL11.glVertex3f(1.76f * 1000f, Y * 1000f, 3.87f * 1000f);
        GL11.glVertex3f(2.11f * 1000f, Y * 1000f, 3.69f * 1000f);
        GL11.glVertex3f(2.37f * 1000f, Y * 1000f, 3.4f * 1000f);
        GL11.glVertex3f(2.49f * 1000f, Y * 1000f, 3.03f * 1000f);
        GL11.glVertex3f(2.47f * 1000f, Y * 1000f, 2.64f * 1000f);
        GL11.glVertex3f(2.29f * 1000f, Y * 1000f, 2.29f * 1000f);
        GL11.glVertex3f(2f * 1000f, Y * 1000f, 2.03f * 1000f);
        GL11.glVertex3f(1.63f * 1000f, Y * 1000f, 1.91f * 1000f);
        GL11.glVertex3f(1.24f * 1000f, Y * 1000f, 1.93f * 1000f);
        GL11.glVertex3f(-1.45f * 1000f, Y * 1000f, 2.66f * 1000f);
        GL11.glEnd();
    }

    private static void drawStudLogo2() {
        final float Y = -0.04f;
        GL11.glLineWidth(2f);
        GL11.glColor4f(0f, 0f, 0f, 1f);
        // Letter L
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3f(2.5f * 620f, Y * 620f, -3.3f * 620f);
        GL11.glVertex3f(2.48f * 620f, Y * 620f, -3.2f * 620f);
        GL11.glVertex3f(2.43f * 620f, Y * 620f, -3.12f * 620f);
        GL11.glVertex3f(2.35f * 620f, Y * 620f, -3.07f * 620f);
        GL11.glVertex3f(2.25f * 620f, Y * 620f, -3.05f * 620f);
        GL11.glVertex3f(2.15f * 620f, Y * 620f, -3.07f * 620f);
        GL11.glVertex3f(2.07f * 620f, Y * 620f, -3.12f * 620f);
        GL11.glVertex3f(2.02f * 620f, Y * 620f, -3.2f * 620f);
        GL11.glVertex3f(2f * 620f, Y * 620f, -3.3f * 620f);
        GL11.glVertex3f(2f * 620f, Y * 620f, -4.08f * 620f);
        GL11.glVertex3f(-1.95f * 620f, Y * 620f, -3.05f * 620f);
        GL11.glVertex3f(-2.05f * 620f, Y * 620f, -3.07f * 620f);
        GL11.glVertex3f(-2.13f * 620f, Y * 620f, -3.12f * 620f);
        GL11.glVertex3f(-2.18f * 620f, Y * 620f, -3.2f * 620f);
        GL11.glVertex3f(-2.2f * 620f, Y * 620f, -3.3f * 620f);
        GL11.glVertex3f(-2.18f * 620f, Y * 620f, -3.4f * 620f);
        GL11.glVertex3f(-2.13f * 620f, Y * 620f, -3.48f * 620f);
        GL11.glVertex3f(-2.05f * 620f, Y * 620f, -3.53f * 620f);
        GL11.glVertex3f(-1.95f * 620f, Y * 620f, -3.55f * 620f);
        GL11.glVertex3f(2.25f * 620f, Y * 620f, -4.65f * 620f);
        GL11.glVertex3f(2.35f * 620f, Y * 620f, -4.63f * 620f);
        GL11.glVertex3f(2.43f * 620f, Y * 620f, -4.58f * 620f);
        GL11.glVertex3f(2.48f * 620f, Y * 620f, -4.5f * 620f);
        GL11.glVertex3f(2.5f * 620f, Y * 620f, -4.4f * 620f);
        GL11.glVertex3f(2.5f * 620f, Y * 620f, -3.3f * 620f);
        GL11.glEnd();
        // Letter E
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3f(-0.15f * 620f, Y * 620f, -1.42f * 620f);
        GL11.glVertex3f(-1.7f * 620f, Y * 620f, -1.02f * 620f);
        GL11.glVertex3f(-1.7f * 620f, Y * 620f, -0.1f * 620f);
        GL11.glVertex3f(-1.72f * 620f, Y * 620f, 0f * 620f);
        GL11.glVertex3f(-1.77f * 620f, Y * 620f, 0.08f * 620f);
        GL11.glVertex3f(-1.85f * 620f, Y * 620f, 0.13f * 620f);
        GL11.glVertex3f(-1.95f * 620f, Y * 620f, 0.15f * 620f);
        GL11.glVertex3f(-2.05f * 620f, Y * 620f, 0.13f * 620f);
        GL11.glVertex3f(-2.13f * 620f, Y * 620f, 0.08f * 620f);
        GL11.glVertex3f(-2.18f * 620f, Y * 620f, 0f * 620f);
        GL11.glVertex3f(-2.2f * 620f, Y * 620f, -0.1f * 620f);
        GL11.glVertex3f(-2.2f * 620f, Y * 620f, -1.2f * 620f);
        GL11.glVertex3f(-2.18f * 620f, Y * 620f, -1.3f * 620f);
        GL11.glVertex3f(-2.13f * 620f, Y * 620f, -1.38f * 620f);
        GL11.glVertex3f(-2.05f * 620f, Y * 620f, -1.43f * 620f);
        GL11.glVertex3f(-1.95f * 620f, Y * 620f, -1.45f * 620f);
        GL11.glVertex3f(2.25f * 620f, Y * 620f, -2.55f * 620f);
        GL11.glVertex3f(2.35f * 620f, Y * 620f, -2.53f * 620f);
        GL11.glVertex3f(2.43f * 620f, Y * 620f, -2.48f * 620f);
        GL11.glVertex3f(2.48f * 620f, Y * 620f, -2.4f * 620f);
        GL11.glVertex3f(2.5f * 620f, Y * 620f, -2.3f * 620f);
        GL11.glVertex3f(2.5f * 620f, Y * 620f, -1.2f * 620f);
        GL11.glVertex3f(2.48f * 620f, Y * 620f, -1.1f * 620f);
        GL11.glVertex3f(2.43f * 620f, Y * 620f, -1.02f * 620f);
        GL11.glVertex3f(2.35f * 620f, Y * 620f, -0.97f * 620f);
        GL11.glVertex3f(2.25f * 620f, Y * 620f, -0.95f * 620f);
        GL11.glVertex3f(2.15f * 620f, Y * 620f, -0.97f * 620f);
        GL11.glVertex3f(2.07f * 620f, Y * 620f, -1.02f * 620f);
        GL11.glVertex3f(2.02f * 620f, Y * 620f, -1.1f * 620f);
        GL11.glVertex3f(2f * 620f, Y * 620f, -1.2f * 620f);
        GL11.glVertex3f(2f * 620f, Y * 620f, -1.98f * 620f);
        GL11.glVertex3f(0.35f * 620f, Y * 620f, -1.55f * 620f);
        GL11.glVertex3f(0.35f * 620f, Y * 620f, -1f * 620f);
        GL11.glVertex3f(0.33f * 620f, Y * 620f, -0.9f * 620f);
        GL11.glVertex3f(0.28f * 620f, Y * 620f, -0.82f * 620f);
        GL11.glVertex3f(0.2f * 620f, Y * 620f, -0.77f * 620f);
        GL11.glVertex3f(0.1f * 620f, Y * 620f, -0.75f * 620f);
        GL11.glVertex3f(0f * 620f, Y * 620f, -0.77f * 620f);
        GL11.glVertex3f(-0.08f * 620f, Y * 620f, -0.82f * 620f);
        GL11.glVertex3f(-0.13f * 620f, Y * 620f, -0.9f * 620f);
        GL11.glVertex3f(-0.15f * 620f, Y * 620f, -1f * 620f);
        GL11.glVertex3f(-0.15f * 620f, Y * 620f, -1.42f * 620f);
        GL11.glEnd();
        // Letter G
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3f(-1.32f * 620f, Y * 620f, 0.74f * 620f);
        GL11.glVertex3f(-1.5f * 620f, Y * 620f, 0.83f * 620f);
        GL11.glVertex3f(-1.63f * 620f, Y * 620f, 0.97f * 620f);
        GL11.glVertex3f(-1.69f * 620f, Y * 620f, 1.16f * 620f);
        GL11.glVertex3f(-1.68f * 620f, Y * 620f, 1.35f * 620f);
        GL11.glVertex3f(-1.59f * 620f, Y * 620f, 1.53f * 620f);
        GL11.glVertex3f(-1.44f * 620f, Y * 620f, 1.65f * 620f);
        GL11.glVertex3f(-1.26f * 620f, Y * 620f, 1.72f * 620f);
        GL11.glVertex3f(-1.06f * 620f, Y * 620f, 1.71f * 620f);
        GL11.glVertex3f(-1.07f * 620f, Y * 620f, 1.71f * 620f);
        GL11.glVertex3f(-0.97f * 620f, Y * 620f, 1.7f * 620f);
        GL11.glVertex3f(-0.88f * 620f, Y * 620f, 1.73f * 620f);
        GL11.glVertex3f(-0.8f * 620f, Y * 620f, 1.79f * 620f);
        GL11.glVertex3f(-0.76f * 620f, Y * 620f, 1.88f * 620f);
        GL11.glVertex3f(-0.75f * 620f, Y * 620f, 1.98f * 620f);
        GL11.glVertex3f(-0.78f * 620f, Y * 620f, 2.07f * 620f);
        GL11.glVertex3f(-0.85f * 620f, Y * 620f, 2.14f * 620f);
        GL11.glVertex3f(-0.94f * 620f, Y * 620f, 2.19f * 620f);
        GL11.glVertex3f(-1.32f * 620f, Y * 620f, 2.21f * 620f);
        GL11.glVertex3f(-1.69f * 620f, Y * 620f, 2.09f * 620f);
        GL11.glVertex3f(-1.99f * 620f, Y * 620f, 1.83f * 620f);
        GL11.glVertex3f(-2.16f * 620f, Y * 620f, 1.48f * 620f);
        GL11.glVertex3f(-2.19f * 620f, Y * 620f, 1.09f * 620f);
        GL11.glVertex3f(-2.06f * 620f, Y * 620f, 0.72f * 620f);
        GL11.glVertex3f(-1.8f * 620f, Y * 620f, 0.43f * 620f);
        GL11.glVertex3f(-1.45f * 620f, Y * 620f, 0.26f * 620f);
        GL11.glVertex3f(1.24f * 620f, Y * 620f, -0.47f * 620f);
        GL11.glVertex3f(1.63f * 620f, Y * 620f, -0.49f * 620f);
        GL11.glVertex3f(2f * 620f, Y * 620f, -0.37f * 620f);
        GL11.glVertex3f(2.29f * 620f, Y * 620f, -0.11f * 620f);
        GL11.glVertex3f(2.47f * 620f, Y * 620f, 0.24f * 620f);
        GL11.glVertex3f(2.49f * 620f, Y * 620f, 0.63f * 620f);
        GL11.glVertex3f(2.37f * 620f, Y * 620f, 1f * 620f);
        GL11.glVertex3f(2.11f * 620f, Y * 620f, 1.29f * 620f);
        GL11.glVertex3f(1.76f * 620f, Y * 620f, 1.47f * 620f);
        GL11.glVertex3f(0.1f * 620f, Y * 620f, 1.91f * 620f);
        GL11.glVertex3f(0f * 620f, Y * 620f, 1.89f * 620f);
        GL11.glVertex3f(-0.08f * 620f, Y * 620f, 1.83f * 620f);
        GL11.glVertex3f(-0.13f * 620f, Y * 620f, 1.75f * 620f);
        GL11.glVertex3f(-0.15f * 620f, Y * 620f, 1.65f * 620f);
        GL11.glVertex3f(-0.15f * 620f, Y * 620f, 0.85f * 620f);
        GL11.glVertex3f(-0.13f * 620f, Y * 620f, 0.75f * 620f);
        GL11.glVertex3f(-0.08f * 620f, Y * 620f, 0.67f * 620f);
        GL11.glVertex3f(0f * 620f, Y * 620f, 0.62f * 620f);
        GL11.glVertex3f(0.1f * 620f, Y * 620f, 0.6f * 620f);
        GL11.glVertex3f(0.2f * 620f, Y * 620f, 0.62f * 620f);
        GL11.glVertex3f(0.28f * 620f, Y * 620f, 0.67f * 620f);
        GL11.glVertex3f(0.33f * 620f, Y * 620f, 0.75f * 620f);
        GL11.glVertex3f(0.35f * 620f, Y * 620f, 0.85f * 620f);
        GL11.glVertex3f(0.35f * 620f, Y * 620f, 1.33f * 620f);
        GL11.glVertex3f(1.63f * 620f, Y * 620f, 0.98f * 620f);
        GL11.glVertex3f(1.8f * 620f, Y * 620f, 0.9f * 620f);
        GL11.glVertex3f(1.93f * 620f, Y * 620f, 0.75f * 620f);
        GL11.glVertex3f(2f * 620f, Y * 620f, 0.57f * 620f);
        GL11.glVertex3f(1.98f * 620f, Y * 620f, 0.37f * 620f);
        GL11.glVertex3f(1.9f * 620f, Y * 620f, 0.2f * 620f);
        GL11.glVertex3f(1.75f * 620f, Y * 620f, 0.07f * 620f);
        GL11.glVertex3f(1.57f * 620f, Y * 620f, 0f * 620f);
        GL11.glVertex3f(1.37f * 620f, Y * 620f, 0.02f * 620f);
        GL11.glVertex3f(-1.32f * 620f, Y * 620f, 0.74f * 620f);
        GL11.glEnd();
        // Letter O
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3f(-1.06f * 620f, Y * 620f, 4.11f * 620f);
        GL11.glVertex3f(-1.26f * 620f, Y * 620f, 4.12f * 620f);
        GL11.glVertex3f(-1.44f * 620f, Y * 620f, 4.05f * 620f);
        GL11.glVertex3f(-1.59f * 620f, Y * 620f, 3.93f * 620f);
        GL11.glVertex3f(-1.68f * 620f, Y * 620f, 3.75f * 620f);
        GL11.glVertex3f(-1.69f * 620f, Y * 620f, 3.56f * 620f);
        GL11.glVertex3f(-1.63f * 620f, Y * 620f, 3.37f * 620f);
        GL11.glVertex3f(-1.5f * 620f, Y * 620f, 3.23f * 620f);
        GL11.glVertex3f(-1.32f * 620f, Y * 620f, 3.14f * 620f);
        GL11.glVertex3f(1.37f * 620f, Y * 620f, 2.42f * 620f);
        GL11.glVertex3f(1.57f * 620f, Y * 620f, 2.4f * 620f);
        GL11.glVertex3f(1.75f * 620f, Y * 620f, 2.47f * 620f);
        GL11.glVertex3f(1.9f * 620f, Y * 620f, 2.6f * 620f);
        GL11.glVertex3f(1.98f * 620f, Y * 620f, 2.77f * 620f);
        GL11.glVertex3f(2f * 620f, Y * 620f, 2.97f * 620f);
        GL11.glVertex3f(1.93f * 620f, Y * 620f, 3.15f * 620f);
        GL11.glVertex3f(1.8f * 620f, Y * 620f, 3.3f * 620f);
        GL11.glVertex3f(1.63f * 620f, Y * 620f, 3.38f * 620f);
        GL11.glVertex3f(-1.07f * 620f, Y * 620f, 4.1f * 620f);
        GL11.glVertex3f(-1.06f * 620f, Y * 620f, 4.11f * 620f);
        GL11.glEnd();
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3f(-1.45f * 620f, Y * 620f, 2.66f * 620f);
        GL11.glVertex3f(-1.8f * 620f, Y * 620f, 2.83f * 620f);
        GL11.glVertex3f(-2.06f * 620f, Y * 620f, 3.12f * 620f);
        GL11.glVertex3f(-2.19f * 620f, Y * 620f, 3.49f * 620f);
        GL11.glVertex3f(-2.16f * 620f, Y * 620f, 3.88f * 620f);
        GL11.glVertex3f(-1.99f * 620f, Y * 620f, 4.23f * 620f);
        GL11.glVertex3f(-1.69f * 620f, Y * 620f, 4.49f * 620f);
        GL11.glVertex3f(-1.32f * 620f, Y * 620f, 4.61f * 620f);
        GL11.glVertex3f(-0.94f * 620f, Y * 620f, 4.59f * 620f);
        GL11.glVertex3f(1.76f * 620f, Y * 620f, 3.87f * 620f);
        GL11.glVertex3f(2.11f * 620f, Y * 620f, 3.69f * 620f);
        GL11.glVertex3f(2.37f * 620f, Y * 620f, 3.4f * 620f);
        GL11.glVertex3f(2.49f * 620f, Y * 620f, 3.03f * 620f);
        GL11.glVertex3f(2.47f * 620f, Y * 620f, 2.64f * 620f);
        GL11.glVertex3f(2.29f * 620f, Y * 620f, 2.29f * 620f);
        GL11.glVertex3f(2f * 620f, Y * 620f, 2.03f * 620f);
        GL11.glVertex3f(1.63f * 620f, Y * 620f, 1.91f * 620f);
        GL11.glVertex3f(1.24f * 620f, Y * 620f, 1.93f * 620f);
        GL11.glVertex3f(-1.45f * 620f, Y * 620f, 2.66f * 620f);
        GL11.glEnd();
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public boolean isMovedTo() {
        return movedTo;
    }

    public void setMovedTo(boolean movedTo) {
        this.movedTo = movedTo;
    }

    @Override
    public void getBFCorientationMap(HashMap<GData, Byte> map) {
        if (matrix != null) {
            byte tempWinding = GData.localWinding;
            boolean tempInvertNext = GData.globalInvertNext;
            boolean tempInvertNextFound = GData.globalInvertNextFound;
            boolean tempNegativeDeterminant = GData.globalNegativeDeterminant;
            GData.globalInvertNextFound = false;
            GData.localWinding = BFC.NOCERTIFY;
            GData.globalNegativeDeterminant = GData.globalNegativeDeterminant ^ negativeDeterminant;
            if (true) {
                GData data2draw = myGData;
                if (GData.accumClip > 0) {
                    GData.accumClip++;
                    while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get())
                        data2draw.getBFCorientationMapNOCLIP(map);
                    GData.accumClip--;
                } else {
                    while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                        switch (GData.localWinding) {
                        case BFC.NOCERTIFY:
                            data2draw.getBFCorientationMapNOCERTIFY(map);
                            break;
                        default:
                            data2draw.getBFCorientationMap(map);
                            break;
                        }
                    }
                    if (GData.accumClip > 0)
                        GData.accumClip = 0;
                }
            }
            GData.localWinding = tempWinding;
            if (tempInvertNextFound)
                GData.globalInvertNext = !tempInvertNext;
            GData.globalNegativeDeterminant = tempNegativeDeterminant;
        }
    }

    @Override
    public void getBFCorientationMapNOCERTIFY(HashMap<GData, Byte> map) {
        boolean tempNegativeDeterminant = GData.globalNegativeDeterminant;
        GData.globalNegativeDeterminant = GData.globalNegativeDeterminant ^ negativeDeterminant;
        GData data2draw = myGData;
        if (GData.accumClip > 0) {
            GData.accumClip++;
            while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get())
                data2draw.getBFCorientationMapNOCERTIFY(map);
            GData.accumClip--;
        } else {
            while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get())
                data2draw.getBFCorientationMapNOCERTIFY(map);
            if (GData.accumClip > 0)
                GData.accumClip = 0;
        }
        GData.globalNegativeDeterminant = tempNegativeDeterminant;
    }

    @Override
    public void getBFCorientationMapNOCLIP(HashMap<GData, Byte> map) {
        boolean tempNegativeDeterminant = GData.globalNegativeDeterminant;
        GData.globalNegativeDeterminant = GData.globalNegativeDeterminant ^ negativeDeterminant;
        GData data2draw = myGData;
        if (GData.accumClip > 0) {
            GData.accumClip++;
            while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get())
                data2draw.getBFCorientationMapNOCLIP(map);
            GData.accumClip--;
        } else {
            while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get())
                data2draw.getBFCorientationMapNOCLIP(map);
            if (GData.accumClip > 0)
                GData.accumClip = 0;
        }
        GData.globalNegativeDeterminant = tempNegativeDeterminant;
    }

    @Override
    public void getVertexNormalMap(TreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, HashMap<GData, float[]> dataLinkedToNormalCACHE, VertexManager vm) {
        if (matrix != null) {
            boolean tNext = GData.globalFoundTEXMAPNEXT;
            GData.globalFoundTEXMAPNEXT = false;

            byte tempWinding = GData.localWinding;
            boolean tempInvertNext = GData.globalInvertNext;
            boolean tempInvertNextFound = GData.globalInvertNextFound;
            boolean tempNegativeDeterminant = GData.globalNegativeDeterminant;
            GData.globalInvertNextFound = false;
            GData.localWinding = BFC.NOCERTIFY;
            GData.globalNegativeDeterminant = GData.globalNegativeDeterminant ^ negativeDeterminant;
            if (true) {

                GData.globalFoundTEXMAPStack.push(false);

                GData data2draw = myGData;
                if (GData.accumClip > 0) {
                    GData.accumClip++;
                    while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get())
                        data2draw.getVertexNormalMapNOCLIP(vertexLinkedToNormalCACHE, dataLinkedToNormalCACHE, vm);
                    GData.accumClip--;
                } else {
                    while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                        switch (GData.localWinding) {
                        case BFC.NOCERTIFY:
                            data2draw.getVertexNormalMapNOCERTIFY(vertexLinkedToNormalCACHE, dataLinkedToNormalCACHE, vm);
                            break;
                        default:
                            data2draw.getVertexNormalMap(vertexLinkedToNormalCACHE, dataLinkedToNormalCACHE, vm);
                            break;
                        }
                    }
                    if (GData.accumClip > 0)
                        GData.accumClip = 0;
                }

                if (GData.globalFoundTEXMAPStack.peek()) {
                    GData.globalFoundTEXMAPStack.pop();
                    GData.globalTextureStack.pop();
                    GData.globalFoundTEXMAPStack.push(false);
                    GData.globalDrawObjects = true;
                }
                GData.globalFoundTEXMAPStack.pop();
            }
            GData.localWinding = tempWinding;
            if (tempInvertNextFound)
                GData.globalInvertNext = !tempInvertNext;
            GData.globalNegativeDeterminant = tempNegativeDeterminant;
            if (tNext) {
                GData.globalFoundTEXMAPStack.pop();
                GData.globalTextureStack.pop();
                GData.globalFoundTEXMAPStack.push(false);
                GData.globalFoundTEXMAPNEXT = false;
            }
        }
    }

    @Override
    public void getVertexNormalMapNOCERTIFY(TreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, HashMap<GData, float[]> dataLinkedToNormalCACHE, VertexManager vm) {
        boolean tempNegativeDeterminant = GData.globalNegativeDeterminant;
        GData.globalNegativeDeterminant = GData.globalNegativeDeterminant ^ negativeDeterminant;
        GData data2draw = myGData;
        if (GData.accumClip > 0) {
            GData.accumClip++;
            while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get())
                data2draw.getVertexNormalMapNOCERTIFY(vertexLinkedToNormalCACHE, dataLinkedToNormalCACHE, vm);
            GData.accumClip--;
        } else {
            while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get())
                data2draw.getVertexNormalMapNOCERTIFY(vertexLinkedToNormalCACHE, dataLinkedToNormalCACHE, vm);
            if (GData.accumClip > 0)
                GData.accumClip = 0;
        }
        GData.globalNegativeDeterminant = tempNegativeDeterminant;
    }

    @Override
    public void getVertexNormalMapNOCLIP(TreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, HashMap<GData, float[]> dataLinkedToNormalCACHE, VertexManager vm) {
        boolean tempNegativeDeterminant = GData.globalNegativeDeterminant;
        GData.globalNegativeDeterminant = GData.globalNegativeDeterminant ^ negativeDeterminant;
        GData data2draw = myGData;
        if (GData.accumClip > 0) {
            GData.accumClip++;
            while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get())
                data2draw.getVertexNormalMapNOCLIP(vertexLinkedToNormalCACHE, dataLinkedToNormalCACHE, vm);
            GData.accumClip--;
        } else {
            while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get())
                data2draw.getVertexNormalMapNOCLIP(vertexLinkedToNormalCACHE, dataLinkedToNormalCACHE, vm);
            if (GData.accumClip > 0)
                GData.accumClip = 0;
        }
        GData.globalNegativeDeterminant = tempNegativeDeterminant;
    }

    private String getSolvedMoveTo(GColour col, int depth) {
        GData description = myGData.getNext();
        if (description.type() == 0) {
            if (((GData0) description).text.trim().startsWith("0 ~Moved to")) { //$NON-NLS-1$
                GData data2draw = myGData;
                GData1 nextReference = null;
                while ((data2draw = data2draw.getNext()) != null) {
                    if (data2draw.type() == 1) {
                        nextReference = (GData1) data2draw;
                        break;
                    }
                }
                if (nextReference != null) {
                    if (nextReference.colourNumber != 16) {
                        col = new GColour(nextReference.colourNumber, nextReference.r, nextReference.g, nextReference.b, nextReference.a);
                    }
                    return nextReference.getSolvedMoveTo(col, ++depth);
                }
            } else {
                if (depth == 0) return null;
                {
                    Matrix newMatrix = this.accurateProductMatrix;
                    StringBuilder lineBuilder1 = new StringBuilder();
                    lineBuilder1.append(1);
                    lineBuilder1.append(" "); //$NON-NLS-1$
                    if (this.colourNumber == -1) {
                        lineBuilder1.append("0x2"); //$NON-NLS-1$
                        lineBuilder1.append(MathHelper.toHex((int) (255f * this.r)).toUpperCase());
                        lineBuilder1.append(MathHelper.toHex((int) (255f * this.g)).toUpperCase());
                        lineBuilder1.append(MathHelper.toHex((int) (255f * this.b)).toUpperCase());
                    } else if (this.colourNumber == 16) {
                        if (col.getColourNumber() == -1) {
                            lineBuilder1.append("0x2"); //$NON-NLS-1$
                            lineBuilder1.append(MathHelper.toHex((int) (255f * col.getR())).toUpperCase());
                            lineBuilder1.append(MathHelper.toHex((int) (255f * col.getG())).toUpperCase());
                            lineBuilder1.append(MathHelper.toHex((int) (255f * col.getB())).toUpperCase());
                        } else {
                            lineBuilder1.append(col.getColourNumber());
                        }
                    } else {
                        lineBuilder1.append(this.colourNumber);
                    }
                    lineBuilder1.append(" "); //$NON-NLS-1$
                    lineBuilder1.append(bigDecimalToString(newMatrix.M30));
                    lineBuilder1.append(" "); //$NON-NLS-1$
                    lineBuilder1.append(bigDecimalToString(newMatrix.M31));
                    lineBuilder1.append(" "); //$NON-NLS-1$
                    lineBuilder1.append(bigDecimalToString(newMatrix.M32));
                    lineBuilder1.append(" "); //$NON-NLS-1$
                    lineBuilder1.append(bigDecimalToString(newMatrix.M00));
                    lineBuilder1.append(" "); //$NON-NLS-1$
                    lineBuilder1.append(bigDecimalToString(newMatrix.M10));
                    lineBuilder1.append(" "); //$NON-NLS-1$
                    lineBuilder1.append(bigDecimalToString(newMatrix.M20));
                    lineBuilder1.append(" "); //$NON-NLS-1$
                    lineBuilder1.append(bigDecimalToString(newMatrix.M01));
                    lineBuilder1.append(" "); //$NON-NLS-1$
                    lineBuilder1.append(bigDecimalToString(newMatrix.M11));
                    lineBuilder1.append(" "); //$NON-NLS-1$
                    lineBuilder1.append(bigDecimalToString(newMatrix.M21));
                    lineBuilder1.append(" "); //$NON-NLS-1$
                    lineBuilder1.append(bigDecimalToString(newMatrix.M02));
                    lineBuilder1.append(" "); //$NON-NLS-1$
                    lineBuilder1.append(bigDecimalToString(newMatrix.M12));
                    lineBuilder1.append(" "); //$NON-NLS-1$
                    lineBuilder1.append(bigDecimalToString(newMatrix.M22));
                    lineBuilder1.append(" "); //$NON-NLS-1$
                    lineBuilder1.append(this.shortName);
                    return lineBuilder1.toString();
                }
            }
        }
        return null;
    }

    public String getSolvedMoveTo() {
        return getSolvedMoveTo(new GColour(colourNumber, r, g, b, a), 0);
    }

    public String colourReplace(String col) {
        StringBuilder lineBuilder = new StringBuilder();
        lineBuilder.append(1);
        lineBuilder.append(" "); //$NON-NLS-1$
        lineBuilder.append(col);
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
        return lineBuilder.toString();
    }

}
