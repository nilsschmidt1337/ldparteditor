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
package org.nschmidt.ldparteditor.text;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.csg.CSGType;
import org.nschmidt.ldparteditor.data.BFC;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GData0;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.GData2;
import org.nschmidt.ldparteditor.data.GData3;
import org.nschmidt.ldparteditor.data.GData4;
import org.nschmidt.ldparteditor.data.GData5;
import org.nschmidt.ldparteditor.data.GDataBFC;
import org.nschmidt.ldparteditor.data.GDataCSG;
import org.nschmidt.ldparteditor.data.GDataPNG;
import org.nschmidt.ldparteditor.data.Matrix;
import org.nschmidt.ldparteditor.data.ParsingResult;
import org.nschmidt.ldparteditor.data.ResultType;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.data.colour.GCDithered;
import org.nschmidt.ldparteditor.data.colour.GCType;
import org.nschmidt.ldparteditor.enumtype.Colour;
import org.nschmidt.ldparteditor.enumtype.LDConfig;
import org.nschmidt.ldparteditor.enumtype.MyLanguage;
import org.nschmidt.ldparteditor.enumtype.Threshold;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.helper.math.MathHelper;
import org.nschmidt.ldparteditor.helper.math.Vector3d;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * Provides a static parser for LDraw files and lines
 *
 * @author nils
 *
 */
public enum DatParser {
    INSTANCE;

    private static boolean updatePngImages = false;

    private static GColour cValue = new GColour();
    private static final Pattern WHITESPACE = Pattern.compile("\\s+"); //$NON-NLS-1$

    private static final Vector3d start = new Vector3d();
    private static final Vector3d end = new Vector3d();
    private static final Vector3d vertexA = new Vector3d();
    private static final Vector3d vertexB = new Vector3d();
    private static final Vector3d vertexC = new Vector3d();
    private static final Vector3d vertexD = new Vector3d();
    private static final Vector3d controlI = new Vector3d();
    private static final Vector3d controlII = new Vector3d();

    private static final Vector3d vertexA2 = new Vector3d();
    private static final Vector3d vertexB2 = new Vector3d();
    private static final Vector3d vertexC2 = new Vector3d();
    private static final Vector3d vertexD2 = new Vector3d();

    public static List<ParsingResult> parseLine(String line, int lineNumber, int depth, float r, float g, float b, float a, GData1 parent, Matrix4f productMatrix, Matrix accurateProductMatrix,
            DatFile datFile, boolean errorCheckOnly, Set<String> alreadyParsed) {
        List<ParsingResult> result = new ArrayList<>();
        // Get the linetype
        int linetype = 0;
        final String[] dataSegments = WHITESPACE.split(line.trim());

        char c;
        if (dataSegments.length < 1 || dataSegments[0].length() >  1 || !Character.isDigit(c = dataSegments[0].charAt(0))) {
            result.add(new ParsingResult(I18n.DATPARSER_INVALID_TYPE, "[E0D] " + I18n.DATPARSER_SYNTAX_ERROR, ResultType.ERROR)); //$NON-NLS-1$
            return new ArrayList<>(result);
        }
        linetype = Character.getNumericValue(c);
        // Parse the line according to its type
        switch (linetype) {
        case 0:
            result.addAll(parseComment(line, dataSegments, depth, r, g, b, a, parent, productMatrix, datFile, errorCheckOnly, alreadyParsed));
            break;
        case 1:
            result.addAll(parseReference(dataSegments, depth, r, g, b, a, parent, productMatrix, accurateProductMatrix, datFile, errorCheckOnly, alreadyParsed, lineNumber));
            break;
        case 2:
            result.addAll(parseLine(dataSegments, r, g, b, a, parent, datFile, errorCheckOnly));
            break;
        case 3:
            result.addAll(parseTriangle(dataSegments, r, g, b, a, parent, datFile, errorCheckOnly, depth));
            break;
        case 4:
            result.addAll(parseQuad(dataSegments, r, g, b, a, parent, datFile, errorCheckOnly, depth));
            break;
        case 5:
            result.addAll(parseCondline(dataSegments, r, g, b, a, parent, datFile, errorCheckOnly, depth));
            break;
        default:
            // Mark unknown linetypes as error
            Object[] messageArguments = {linetype};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.getLocale());
            formatter.applyPattern(I18n.DATPARSER_UNKNOWN_LINE_TYPE);
            result.add(new ParsingResult(formatter.format(messageArguments), "[E99] " + I18n.DATPARSER_SYNTAX_ERROR, ResultType.ERROR)); //$NON-NLS-1$
        }
        return result;
    }

    /**
     * Validates the colour argument and highlights possible errors
     *
     * Please note that the returned value will be always the same instance due
     * to performance reasons! <br>
     * Use {@code createClone()} to obtain a new instance!
     *
     * @param arg
     *            the argument data
     * @return {@code null} if the colour is invalid
     */
    public static GColour validateColour(String arg, float r, float g, float b, float a) {
        int colourValue;
        try {
            colourValue = Integer.parseInt(arg);
            switch (colourValue) {
            case 16:
                cValue.set(16, r, g, b, a);
                break;
            case 24:
                cValue.set(24, Colour.lineColourR, Colour.lineColourG, Colour.lineColourB, 1f);
                break;
            default:
                if (LDConfig.hasColour(colourValue)) {
                    GColour colour = LDConfig.getColour(colourValue);
                    cValue.set(colour);
                } else {
                    int colourIndexA = colourValue - 256 >> 4;
                    int colourIndexB = colourValue - 256 & 0x0F;
                    if (LDConfig.hasColour(colourIndexA) && LDConfig.hasColour(colourIndexB)) {
                        GColour colourA = LDConfig.getColour(colourIndexA);
                        GColour colourB = LDConfig.getColour(colourIndexB);
                        GColour ditheredColour = new GColour(
                                colourValue,
                                (colourA.getR() + colourB.getR()) / 2f,
                                (colourA.getG() + colourB.getG()) / 2f,
                                (colourA.getB() + colourB.getB()) / 2f,
                                1f, new GCDithered());
                        cValue.set(ditheredColour);
                        break;
                    }
                    return null;
                }
                break;
            }
        } catch (NumberFormatException nfe) {
            if (arg.length() == 9 && arg.substring(0, 3).equals("0x2")) { //$NON-NLS-1$
                cValue.setA(1f);
                try {
                    cValue.setR(Integer.parseInt(arg.substring(3, 5), 16) / 255f);
                    cValue.setG(Integer.parseInt(arg.substring(5, 7), 16) / 255f);
                    cValue.setB(Integer.parseInt(arg.substring(7, 9), 16) / 255f);
                } catch (NumberFormatException nfe2) {
                    return null;
                }
                cValue.setColourNumber(-1);
                cValue.setType(null);
            } else {
                return null;
            }
        }
        return cValue;
    }

    /**
     * Validates the colour argument and highlights possible errors
     *
     * Please note that the returned value will be always the same instance due
     * to performance reasons! <br>
     * Use {@code createClone()} to obtain a new instance!
     *
     * @param arg
     *            the argument data
     * @return {@code null} if the colour is invalid
     */
    public static GColour validateColour(int arg, float r, float g, float b, float a) {
        switch (arg) {
        case 16:
            cValue.set(16, r, g, b, a);
            break;
        case 24:
            cValue.set(24, Colour.lineColourR, Colour.lineColourG, Colour.lineColourB, 1f);
            break;
        default:
            if (LDConfig.hasColour(arg)) {
                GColour colour = LDConfig.getColour(arg);
                cValue.set(colour);
            } else {
                int colourIndexA = arg - 256 >> 4;
                int colourIndexB = arg - 256 & 0x0F;
                if (LDConfig.hasColour(colourIndexA) && LDConfig.hasColour(colourIndexB)) {
                    GColour colourA = LDConfig.getColour(colourIndexA);
                    GColour colourB = LDConfig.getColour(colourIndexB);
                    GColour ditheredColour = new GColour(
                            arg,
                            (colourA.getR() + colourB.getR()) / 2f,
                            (colourA.getG() + colourB.getG()) / 2f,
                            (colourA.getB() + colourB.getB()) / 2f,
                            1f, new GCDithered());
                    cValue.set(ditheredColour);
                    break;
                }
                return null;
            }
            break;
        }
        return cValue;
    }

    /**
     * Checks comment lines
     *
     * @return an empty list if there was no error
     */
    private static List<ParsingResult> parseComment(String line, String[] dataSegments, int depth, float r, float g, float b, float a, GData1 parent, Matrix4f productMatrix, DatFile datFile,
            boolean errorCheckOnly, Set<String> alreadyParsed) {

        List<ParsingResult> result = new ArrayList<>();
        line = WHITESPACE.matcher(line).replaceAll(" ").trim(); //$NON-NLS-1$

        if (line.startsWith(I18n.DATFILE_INLINE_PREFIX)) {
            result.add(new ParsingResult(new GData0(line, parent)));
            result.add(new ParsingResult(I18n.DATPARSER_INLINING_RELICT, "[W01] " + I18n.DATPARSER_WARNING, ResultType.WARN)); //$NON-NLS-1$
        } else if (line.startsWith("0 !: ")) { //$NON-NLS-1$
            GData newLPEmetaTag = TexMapParser.parseGeometry(line, depth, r, g, b, a, parent, productMatrix, alreadyParsed, datFile);
            if (newLPEmetaTag == null) {
                newLPEmetaTag = new GData0(line, parent);
                result.add(new ParsingResult(newLPEmetaTag));
                result.add(new ParsingResult(I18n.DATPARSER_INVALID_TEXMAP, "[E99] " + I18n.DATPARSER_SYNTAX_ERROR, ResultType.ERROR)); //$NON-NLS-1$
            } else {
                result.add(new ParsingResult(newLPEmetaTag));
            }
        } else if (line.startsWith("0 !TEXMAP ")) { //$NON-NLS-1$
            GData newLPEmetaTag = TexMapParser.parseTEXMAP(dataSegments, line, parent);
            if (newLPEmetaTag == null) {
                newLPEmetaTag = new GData0(line, parent);
                result.add(new ParsingResult(newLPEmetaTag));
                result.add(new ParsingResult(I18n.DATPARSER_INVALID_TEXMAP, "[E99] " + I18n.DATPARSER_SYNTAX_ERROR, ResultType.ERROR)); //$NON-NLS-1$
            } else {
                result.add(new ParsingResult(newLPEmetaTag));
            }
        } else if (line.startsWith("0 !LPE")) { //$NON-NLS-1$
            GData0 newLPEmetaTag = new GData0(line, parent);
            result.add(new ParsingResult(newLPEmetaTag));
            result.add(new ParsingResult(I18n.DATPARSER_UNOFFICIAL_META_COMMAND, "[W0D] " + I18n.DATPARSER_WARNING, ResultType.WARN)); //$NON-NLS-1$
            if (line.startsWith("TODO ", 7)) { //$NON-NLS-1$
                result.add(new ParsingResult(line.substring(12), "[WFF] " + I18n.DATPARSER_TODO, ResultType.WARN)); //$NON-NLS-1$
            } else if (line.startsWith("VERTEX ", 7)) { //$NON-NLS-1$
                Object[] messageArguments = {line.substring(14)};
                MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                formatter.setLocale(MyLanguage.getLocale());
                formatter.applyPattern(I18n.DATPARSER_VERTEX_AT);
                result.add(new ParsingResult(formatter.format(messageArguments) , "[WFE] " + I18n.DATPARSER_VERTEX_DECLARATION, ResultType.WARN)); //$NON-NLS-1$
                boolean numberError = false;
                if (dataSegments.length == 6) {
                    try {
                        start.setX(new BigDecimal(dataSegments[3], Threshold.MC));
                        start.setY(new BigDecimal(dataSegments[4], Threshold.MC));
                        start.setZ(new BigDecimal(dataSegments[5], Threshold.MC));
                    } catch (NumberFormatException nfe) {
                        numberError = true;
                    }
                } else {
                    numberError = true;
                }
                if (numberError) {
                    result.add(new ParsingResult(I18n.DATPARSER_INVALID_NUMBER_FORMAT, "[E99] " + I18n.DATPARSER_SYNTAX_ERROR, ResultType.ERROR)); //$NON-NLS-1$
                } else if (!errorCheckOnly) {
                    if (depth == 0) {
                        datFile.getVertexManager().addVertex(new Vertex(start.x, start.y, start.z), newLPEmetaTag);
                    } else {
                        Vector4f vert = new Vector4f(start.getXf() * 1000f, start.getYf() * 1000f, start.getZf() * 1000f, 1f);
                        Matrix4f.transform(productMatrix, vert, vert);
                        datFile.getVertexManager().addSubfileVertex(new Vertex(vert), newLPEmetaTag, parent);
                    }
                }
            } else if (line.startsWith("DISTANCE ", 7)) { //$NON-NLS-1$
                boolean numberError = false;
                final GColour colour;
                if (dataSegments.length == 10) {
                    colour = validateColour(dataSegments[3], r, g, b, a);
                    if (colour == null) {
                        result.add(new ParsingResult(I18n.DATPARSER_INVALID_COLOUR, "[E99] " + I18n.DATPARSER_SYNTAX_ERROR, ResultType.ERROR)); //$NON-NLS-1$
                        return result;
                    }
                    try {
                        start.setX(new BigDecimal(dataSegments[4], Threshold.MC));
                        start.setY(new BigDecimal(dataSegments[5], Threshold.MC));
                        start.setZ(new BigDecimal(dataSegments[6], Threshold.MC));
                        end.setX(new BigDecimal(dataSegments[7], Threshold.MC));
                        end.setY(new BigDecimal(dataSegments[8], Threshold.MC));
                        end.setZ(new BigDecimal(dataSegments[9], Threshold.MC));
                    } catch (NumberFormatException nfe) {
                        numberError = true;
                    }
                } else {
                    numberError = true;
                    colour = null;
                }
                if (numberError) {
                    result.add(new ParsingResult(I18n.DATPARSER_INVALID_NUMBER_FORMAT, "[E99] " + I18n.DATPARSER_SYNTAX_ERROR, ResultType.ERROR)); //$NON-NLS-1$
                } else if (!errorCheckOnly && depth == 0) {
                    result.remove(0);
                    result.add(0, new ParsingResult(new GData2(colour.getColourNumber(), colour.getR(), colour.getG(), colour.getB(), colour.getA(), start.x, start.y, start.z, end.x, end.y, end.z, parent, datFile, false)));
                }
            } else if (line.startsWith("PROTRACTOR ", 7)) { //$NON-NLS-1$
                boolean numberError = false;
                final GColour colour;
                if (dataSegments.length == 13) {
                    colour = validateColour(dataSegments[3], r, g, b, a);
                    if (colour == null) {
                        result.add(new ParsingResult(I18n.DATPARSER_INVALID_COLOUR, "[E99] " + I18n.DATPARSER_SYNTAX_ERROR, ResultType.ERROR)); //$NON-NLS-1$
                        return result;
                    }
                    try {
                        vertexA.setX(new BigDecimal(dataSegments[4], Threshold.MC));
                        vertexA.setY(new BigDecimal(dataSegments[5], Threshold.MC));
                        vertexA.setZ(new BigDecimal(dataSegments[6], Threshold.MC));
                        vertexB.setX(new BigDecimal(dataSegments[7], Threshold.MC));
                        vertexB.setY(new BigDecimal(dataSegments[8], Threshold.MC));
                        vertexB.setZ(new BigDecimal(dataSegments[9], Threshold.MC));
                        vertexC.setX(new BigDecimal(dataSegments[10], Threshold.MC));
                        vertexC.setY(new BigDecimal(dataSegments[11], Threshold.MC));
                        vertexC.setZ(new BigDecimal(dataSegments[12], Threshold.MC));
                    } catch (NumberFormatException nfe) {
                        numberError = true;
                    }
                } else {
                    numberError = true;
                    colour = null;
                }
                if (numberError) {
                    result.add(new ParsingResult(I18n.DATPARSER_INVALID_NUMBER_FORMAT, "[E99] " + I18n.DATPARSER_SYNTAX_ERROR, ResultType.ERROR)); //$NON-NLS-1$
                } else if (!errorCheckOnly && depth == 0) {
                    result.remove(0);
                    result.add(0, new ParsingResult(new GData3(colour.getColourNumber(), colour.getR(), colour.getG(), colour.getB(), colour.getA(), vertexA.x, vertexA.y, vertexA.z, vertexB.x, vertexB.y, vertexB.z, vertexC.x, vertexC.y, vertexC.z, parent, datFile, false)));
                }
            } else if (line.startsWith("CSG_", 7)) { //$NON-NLS-1$
                if (line.startsWith("UNION", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSGType.UNION, line, parent)));
                } else if (line.startsWith("DIFFERENCE", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSGType.DIFFERENCE, line, parent)));
                } else if (line.startsWith("INTERSECTION", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSGType.INTERSECTION, line, parent)));
                } else if (line.startsWith("TRANSFORM", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSGType.TRANSFORM, line, parent)));
                } else if (line.startsWith("CUBOID", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSGType.CUBOID, line, parent)));
                } else if (line.startsWith("ELLIPSOID", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSGType.ELLIPSOID, line, parent)));
                } else if (line.startsWith("QUAD", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSGType.QUAD, line, parent)));
                } else if (line.startsWith("CYLINDER", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSGType.CYLINDER, line, parent)));
                } else if (line.startsWith("MESH", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSGType.MESH, line, parent)));
                } else if (line.startsWith("CONE", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSGType.CONE, line, parent)));
                } else if (line.startsWith("CIRCLE", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSGType.CIRCLE, line, parent)));
                } else if (line.startsWith("COMPILE", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSGType.COMPILE, line, parent)));
                } else if (line.startsWith("QUALITY", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSGType.QUALITY, line, parent)));
                } else if (line.startsWith("EPSILON", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSGType.EPSILON, line, parent)));
                } else if (line.startsWith("TJUNCTION_EPSILON", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSGType.TJUNCTION, line, parent)));
                } else if (line.startsWith("EDGE_COLLAPSE_EPSILON", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSGType.COLLAPSE, line, parent)));
                } else if (line.startsWith("DONT_OPTIMIZE", 11) || line.startsWith("DONT_OPTIMISE", 11) ) { //$NON-NLS-1$ //$NON-NLS-2$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSGType.DONTOPTIMIZE, line, parent)));
                } else if (line.startsWith("EXTRUDE", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSGType.EXTRUDE, line, parent)));
                } else if (line.startsWith("EXT_CFG", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSGType.EXTRUDE_CFG, line, parent)));
                    GDataCSG.forceRecompile(datFile);
                }
            } else if (line.startsWith("PNG", 7) && depth == 0 && dataSegments.length >= 12) { //$NON-NLS-1$
                try {
                    Vertex offset = new Vertex(new BigDecimal(dataSegments[3]), new BigDecimal(dataSegments[4]), new BigDecimal(dataSegments[5]));
                    BigDecimal a1 = new BigDecimal(dataSegments[6]);
                    BigDecimal a2 = new BigDecimal(dataSegments[7]);
                    BigDecimal a3 = new BigDecimal(dataSegments[8]);
                    Vertex scale = new Vertex(new BigDecimal(dataSegments[9]), new BigDecimal(dataSegments[10]), BigDecimal.ONE);
                    StringBuilder sb = new StringBuilder();
                    for (int s = 11; s < dataSegments.length - 1; s++) {
                        sb.append(dataSegments[s]);
                        sb.append(" "); //$NON-NLS-1$
                    }
                    sb.append(dataSegments[dataSegments.length - 1]);
                    result.remove(0);
                    final GDataPNG gpng = new GDataPNG(line, offset, a1, a2, a3, scale, sb.toString(), parent);
                    if (!errorCheckOnly) datFile.getVertexManager().setSelectedBgPicture(gpng);
                    result.add(0, new ParsingResult(gpng));
                    if (!errorCheckOnly) updatePngImages = true;
                } catch (Exception consumed) {
                    NLogger.debug(DatParser.class, consumed);
                }
            }
        } else if (line.startsWith("0 BFC ")) { //$NON-NLS-1$
            if (line.startsWith("INVERTNEXT", 6)) { //$NON-NLS-1$
                result.add(new ParsingResult(new GDataBFC(BFC.INVERTNEXT, parent)));
            } else if (line.startsWith("CERTIFY", 6)) { //$NON-NLS-1$
                if (line.startsWith("CLIP CCW", 14) || line.startsWith("CCW CLIP", 14)) { //$NON-NLS-1$ //$NON-NLS-2$
                    result.add(new ParsingResult(I18n.DATPARSER_MLCAD_CLIP_CCW, "[W0C] " + I18n.DATPARSER_WARNING, ResultType.WARN)); //$NON-NLS-1$
                    result.add(new ParsingResult(new GDataBFC(BFC.CCW_CLIP, parent)));
                } else if (line.startsWith("CLIP CW", 14) || line.startsWith("CW CLIP", 14)) { //$NON-NLS-1$ //$NON-NLS-2$
                    result.add(new ParsingResult(I18n.DATPARSER_MLCAD_CLIP_CW, "[W0C] " + I18n.DATPARSER_WARNING, ResultType.WARN)); //$NON-NLS-1$
                    result.add(new ParsingResult(new GDataBFC(BFC.CW_CLIP, parent)));
                } else if (line.startsWith("CCW", 14)) { //$NON-NLS-1$
                    result.add(new ParsingResult(new GDataBFC(BFC.CCW_CLIP, parent)));
                } else if (line.startsWith("CW", 14)) { //$NON-NLS-1$
                    result.add(new ParsingResult(new GDataBFC(BFC.CW_CLIP, parent)));
                } else if (line.startsWith("INVERTNEXT", 14)) { //$NON-NLS-1$
                    result.add(new ParsingResult(I18n.DATPARSER_MLCAD_INVERT_NEXT, "[W0B] " + I18n.DATPARSER_WARNING, ResultType.WARN)); //$NON-NLS-1$
                    result.add(new ParsingResult(new GDataBFC(BFC.INVERTNEXT, parent)));
                } else if (line.startsWith("CLIP", 14)) { //$NON-NLS-1$
                    result.add(new ParsingResult(I18n.DATPARSER_MLCAD_CLIP, "[W0B] " + I18n.DATPARSER_WARNING, ResultType.WARN)); //$NON-NLS-1$
                    result.add(new ParsingResult(new GDataBFC(BFC.CLIP, parent)));
                } else if (line.startsWith("NOCLIP", 14)) { //$NON-NLS-1$
                    result.add(new ParsingResult(I18n.DATPARSER_MLCAD_NO_CLIP, "[W0B] " + I18n.DATPARSER_WARNING, ResultType.WARN)); //$NON-NLS-1$
                    result.add(new ParsingResult(new GDataBFC(BFC.NOCLIP, parent)));
                } else {
                    result.add(new ParsingResult(new GData0(line, parent)));
                }
            } else if (line.startsWith("NOCERTIFY", 6)) { //$NON-NLS-1$
                result.add(new ParsingResult(new GDataBFC(BFC.NOCERTIFY, parent)));
            } else if (line.startsWith("CCW", 6)) { //$NON-NLS-1$
                result.add(new ParsingResult(new GDataBFC(BFC.CCW, parent)));
            } else if (line.startsWith("CW", 6)) { //$NON-NLS-1$
                result.add(new ParsingResult(new GDataBFC(BFC.CW, parent)));
            } else if (line.startsWith("NOCLIP", 6)) { //$NON-NLS-1$
                result.add(new ParsingResult(new GDataBFC(BFC.NOCLIP, parent)));
            } else if (line.startsWith("CLIP CCW", 6)) { //$NON-NLS-1$
                result.add(new ParsingResult(new GDataBFC(BFC.CCW_CLIP, parent)));
            } else if (line.startsWith("CLIP CW", 6)) { //$NON-NLS-1$
                result.add(new ParsingResult(new GDataBFC(BFC.CW_CLIP, parent)));
            } else if (line.startsWith("CCW CLIP", 6)) { //$NON-NLS-1$
                result.add(new ParsingResult(new GDataBFC(BFC.CCW_CLIP, parent)));
            } else if (line.startsWith("CW CLIP", 6)) { //$NON-NLS-1$
                result.add(new ParsingResult(new GDataBFC(BFC.CW_CLIP, parent)));
            } else if (line.startsWith("CLIP", 6)) { //$NON-NLS-1$
                result.add(new ParsingResult(new GDataBFC(BFC.CLIP, parent)));
            } else {
                result.add(new ParsingResult(new GData0(line, parent)));
            }
        } else {
            if (line.equals("0 STEP")) { //$NON-NLS-1$
                result.add(new ParsingResult(new GData0(line, true, parent)));
            } else {
                result.add(new ParsingResult(new GData0(line, parent)));
            }

        }
        return result;
    }

    /**
     * Checks reference lines and highlights syntax errors
     *
     * @return an empty list if there was no error
     */
    private static List<ParsingResult> parseReference(String[] dataSegments, int depth, float r, float g, float b, float a, GData1 parent, Matrix4f productMatrix, Matrix accurateProductMatrix,
            DatFile datFile, boolean errorCheckOnly, Set<String> alreadyParsed, int lineNumber) {
        List<ParsingResult> result = new ArrayList<>();
        boolean parseError = false;
        boolean hasDitheredColour = false;
        // [ERROR] Check less argument count
        if (dataSegments.length < 15) {
            Object[] messageArguments = {dataSegments.length, 15};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.getLocale());
            formatter.applyPattern(I18n.DATPARSER_WRONG_ARGUMENT_COUNT);
            result.add(new ParsingResult(formatter.format(messageArguments), "[E99] " + I18n.DATPARSER_SYNTAX_ERROR, ResultType.ERROR)); //$NON-NLS-1$
        } else {
            // [ERROR] Check colour
            GColour colour = validateColour(dataSegments[1], r, g, b, a);
            if (colour == null) {
                result.add(new ParsingResult(I18n.DATPARSER_INVALID_COLOUR, "[E99] " + I18n.DATPARSER_SYNTAX_ERROR, ResultType.ERROR)); //$NON-NLS-1$
                return result;
            }
            hasDitheredColour = colour.getType() != null && GCType.DITHERED == colour.getType().type();
            // [ERROR] Check singularity
            Matrix4f tMatrix = new Matrix4f();
            BigDecimal m00;
            BigDecimal m01;
            BigDecimal m02;
            final BigDecimal m03 = BigDecimal.ZERO;
            BigDecimal m10;
            BigDecimal m11;
            BigDecimal m12;
            final BigDecimal m13 = BigDecimal.ZERO;
            BigDecimal m20;
            BigDecimal m21;
            BigDecimal m22;
            final BigDecimal m23 = BigDecimal.ZERO;
            BigDecimal m30;
            BigDecimal m31;
            BigDecimal m32;
            final BigDecimal m33 = BigDecimal.ONE;
            float det = 0;
            try {
                // Offset
                m30 = new BigDecimal(dataSegments[2]);
                tMatrix.m30 = m30.floatValue() * 1000f;
                m31 = new BigDecimal(dataSegments[3]);
                tMatrix.m31 = m31.floatValue() * 1000f;
                m32 = new BigDecimal(dataSegments[4]);
                tMatrix.m32 = m32.floatValue() * 1000f;
                // First row
                m00 = new BigDecimal(dataSegments[5]);
                tMatrix.m00 = m00.floatValue();
                m10 = new BigDecimal(dataSegments[6]);
                tMatrix.m10 = m10.floatValue();
                m20 = new BigDecimal(dataSegments[7]);
                tMatrix.m20 = m20.floatValue();
                // Second row
                m01 = new BigDecimal(dataSegments[8]);
                tMatrix.m01 = m01.floatValue();
                m11 = new BigDecimal(dataSegments[9]);
                tMatrix.m11 = m11.floatValue();
                m21 = new BigDecimal(dataSegments[10]);
                tMatrix.m21 = m21.floatValue();
                // Third row
                m02 = new BigDecimal(dataSegments[11]);
                tMatrix.m02 = m02.floatValue();
                m12 = new BigDecimal(dataSegments[12]);
                tMatrix.m12 = m12.floatValue();
                m22 = new BigDecimal(dataSegments[13]);
                tMatrix.m22 = m22.floatValue();
                tMatrix.m33 = 1f;

                det = tMatrix.determinant();
                parseError = Math.abs(det) < Threshold.SINGULARITY_DETERMINANT;
            } catch (NumberFormatException nfe) {
                m00 = null; m01 = null; m02 = null; m10 = null;
                m11 = null; m12 = null; m20 = null; m21 = null;
                m22 = null; m30 = null; m31 = null; m32 = null;
                result.add(new ParsingResult(I18n.DATPARSER_INVALID_NUMBER_FORMAT, "[E99] " + I18n.DATPARSER_SYNTAX_ERROR, ResultType.ERROR)); //$NON-NLS-1$
            }

            // [WARNING] Check file existance
            boolean fileExists = true;
            StringBuilder sb = new StringBuilder();
            for (int s = 14; s < dataSegments.length - 1; s++) {
                sb.append(dataSegments[s]);
                sb.append(" "); //$NON-NLS-1$
            }
            sb.append(dataSegments[dataSegments.length - 1]);
            String shortFilename = sb.toString();
            boolean isLowercase = shortFilename.equals(shortFilename.toLowerCase(Locale.ENGLISH));
            shortFilename = shortFilename.toLowerCase(Locale.ENGLISH);
            shortFilename = shortFilename.replace("s\\", "S" + File.separator).replace("\\", File.separator); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if (alreadyParsed.contains(shortFilename)) {
                result.add(new ParsingResult(I18n.DATPARSER_RECURSIVE, "[E01] " + I18n.DATPARSER_LOGIC_ERROR, ResultType.ERROR)); //$NON-NLS-1$
                if (!View.DUMMY_REFERENCE.equals(parent))
                    parent.firstRef.setRecursive(true);
            } else {
                alreadyParsed.add(shortFilename);
            }
            String shortFilename2 = shortFilename.startsWith("S" + File.separator) ? "s" + shortFilename.substring(1) : shortFilename; //$NON-NLS-1$ //$NON-NLS-2$
            String shortFilename3 = shortFilename.startsWith("S" + File.separator) ? shortFilename.substring(2) : shortFilename; //$NON-NLS-1$
            File fileToOpen = null;
            boolean readOnly = false;

            String[] prefix ;
            int readyOnlyAt = 2;
            if (datFile != null && !datFile.isProjectFile() && !View.DUMMY_DATFILE.equals(datFile)) {
                File dff = new File(datFile.getOldName()).getParentFile();
                if (dff != null && dff.exists() && dff.isDirectory()) {
                    prefix = new String[]{dff.getAbsolutePath(), Project.getProjectPath(), WorkbenchManager.getUserSettingState().getUnofficialFolderPath(), WorkbenchManager.getUserSettingState().getLdrawFolderPath()};
                    readyOnlyAt = 3;
                } else {
                    prefix = new String[]{Project.getProjectPath(), WorkbenchManager.getUserSettingState().getUnofficialFolderPath(), WorkbenchManager.getUserSettingState().getLdrawFolderPath()};
                }
            } else {
                prefix = new String[]{Project.getProjectPath(), WorkbenchManager.getUserSettingState().getUnofficialFolderPath(), WorkbenchManager.getUserSettingState().getLdrawFolderPath()};
            }

            String[] middle = new String[]{"", File.separator + "PARTS", File.separator + "parts", File.separator + "P", File.separator + "p"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            String[] suffix = new String[]{File.separator + shortFilename, File.separator + shortFilename2, File.separator + shortFilename3};
            for (int a1 = 0; a1 < prefix.length; a1++) {
                readOnly = a1 == readyOnlyAt;
                String s1 = prefix[a1];
                for (int a2 = 0; a2 < middle.length; a2++) {
                    String s2 = middle[a2];
                    for (int a3 = 0; a3 < suffix.length; a3++) {
                        String s3 = suffix[a3];
                        File f = new File(s1 + s2 + s3);
                        fileExists = f.exists() && f.isFile();
                        if (fileExists) {
                            fileToOpen = f;
                            break;
                        }
                    }
                    if (fileExists) break;
                }
                if (fileExists) break;
            }

            List<String> lines = null;
            if (parseError) {
                result.add(new ParsingResult(I18n.DATPARSER_SINGULAR_MATRIX, "[E02] " + I18n.DATPARSER_LOGIC_ERROR, ResultType.ERROR)); //$NON-NLS-1$
            }

            String absoluteFilename = null;

            // MARK Virtual file check for project files...
            boolean isVirtual = false;
            for (DatFile df : Project.getUnsavedFiles()) {
                String fn = df.getNewName();
                for (int a1 = 0; a1 < prefix.length; a1++) {
                    String s1 = prefix[a1];
                    for (int a2 = 0; a2 < middle.length; a2++) {
                        String s2 = middle[a2];
                        for (int a3 = 0; a3 < suffix.length; a3++) {
                            String s3 = suffix[a3];
                            if (fn.equals(s1 + s2 + s3)) {
                                lines = new ArrayList<>(4096);
                                lines.addAll(Arrays.asList(df.getText().split(StringHelper.getLineDelimiter())));
                                absoluteFilename = fn;
                                isVirtual = true;
                                readOnly = false;
                                break;
                            }
                        }
                        if (isVirtual) break;
                    }
                    if (isVirtual) break;
                }
                if (isVirtual) break;
            }

            if (isVirtual) {

                if (result.isEmpty()) {
                    if (!errorCheckOnly) {

                        Matrix tMatrixP = new Matrix(m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33);
                        Matrix destMatrixP = Matrix.mul(accurateProductMatrix, tMatrixP);

                        Matrix4f destMatrix = new Matrix4f();
                        Matrix4f.mul(productMatrix, tMatrix, destMatrix);

                        result.add(new ParsingResult(new GData1(colour.getColourNumber(), colour.getR(), colour.getG(), colour.getB(), colour.getA(), tMatrix, tMatrixP, lines, absoluteFilename, sb
                                .toString(), depth, det < 0, destMatrix, destMatrixP, datFile, parent.firstRef, readOnly, errorCheckOnly, alreadyParsed, parent)));
                        GDataCSG.forceRecompile(datFile);
                    }

                    // Avoid scaling of flat files
                    if (depth == 0) {
                        GData g1 = null;
                        if (result.size() == 1) {
                            g1 = result.get(0).getGraphicalData();
                        }
                        if (g1 == null) {
                            g1 = datFile.getDrawPerLineNoClone().getValue(lineNumber);
                        }
                        if (g1 != null && g1.type() == 1) {
                            result.addAll(datFile.getVertexManager().checkForFlatScaling((GData1) g1));
                        }
                    }
                }
            } else if (!fileExists) {
                result.add(new ParsingResult(I18n.DATPARSER_FILE_NOT_FOUND, "[E01] " + I18n.DATPARSER_DATA_ERROR, ResultType.ERROR)); //$NON-NLS-1$
            } else {
                absoluteFilename = fileToOpen.getAbsolutePath();
                String line = null;
                lines = new ArrayList<>(4096);
                try (UTF8BufferedReader reader = new UTF8BufferedReader(absoluteFilename)) {
                    while (true) {
                        line = reader.readLine();
                        if (line == null) {
                            break;
                        }
                        lines.add(line);
                    }
                } catch (FileNotFoundException | LDParsingException ex) {
                    NLogger.debug(DatParser.class, ex);
                }

                if (result.isEmpty()) {
                    if (!errorCheckOnly) {

                        Matrix tMatrixP = new Matrix(m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33);
                        Matrix destMatrixP = Matrix.mul(accurateProductMatrix, tMatrixP);

                        Matrix4f destMatrix = new Matrix4f();
                        Matrix4f.mul(productMatrix, tMatrix, destMatrix);

                        result.add(new ParsingResult(new GData1(colour.getColourNumber(), colour.getR(), colour.getG(), colour.getB(), colour.getA(), tMatrix, tMatrixP, lines, absoluteFilename, sb
                                .toString(), depth, det < 0, destMatrix, destMatrixP, datFile, parent.firstRef, readOnly, errorCheckOnly, alreadyParsed, parent)));
                    }

                    // Avoid scaling of flat files
                    if (depth == 0) {
                        GData g1 = null;
                        if (result.size() == 1) {
                            g1 = result.get(0).getGraphicalData();
                        }
                        if (g1 == null) {
                            g1 = datFile.getDrawPerLineNoClone().getValue(lineNumber);
                        }
                        if (g1 != null && g1.type() == 1) {
                            result.addAll(datFile.getVertexManager().checkForFlatScaling((GData1) g1));
                        }
                    }
                }
            }
            if (parent.equals(View.DUMMY_REFERENCE) && result.size() == 1) {
                GData1 g1 = (GData1) result.get(0).getGraphicalData();
                if (g1 != null) {
                    if (g1.firstRef.isRecursive()) {
                        result.add(new ParsingResult(I18n.DATPARSER_RECURSIVE, "[E01] " + I18n.DATPARSER_LOGIC_ERROR, ResultType.ERROR)); //$NON-NLS-1$
                    }
                    if (g1.firstRef.isMovedTo()) {
                        result.add(new ParsingResult(I18n.DATPARSER_MOVED_TO, "[E2A] " + I18n.DATPARSER_LOGIC_ERROR, ResultType.ERROR)); //$NON-NLS-1$
                    }
                }
            }
            alreadyParsed.remove(shortFilename);
            // [WARNING] Check spaces in dat file name
            if (dataSegments.length > 15) {
                result.add(new ParsingResult(I18n.DATPARSER_FILENAME_WHITESPACE, "[W01] " + I18n.DATPARSER_WARNING, ResultType.WARN)); //$NON-NLS-1$
            }
            // [WARNING] Dithered colour
            if (hasDitheredColour) {
                result.add(new ParsingResult(I18n.DATPARSER_DITHERED_COLOUR, "[WDC] " + I18n.DATPARSER_WARNING, ResultType.WARN)); //$NON-NLS-1$
            }
            // [WARNING] Upper- & Mixed-Case file name
            if (!isLowercase) {
                result.add(new ParsingResult(I18n.DATPARSER_INVALID_CASE, "[WCC] " + I18n.DATPARSER_WARNING, ResultType.WARN)); //$NON-NLS-1$
            }
        }
        return result;
    }

    /**
     * Checks "line lines" and highlights syntax errors
     *
     * @param line
     *            the line to check
     * @param dataSegments
     * @param b
     * @param g
     * @param r
     * @param datFile
     * @param parent
     * @param errorCheckOnly
     * @return an empty list if there was no error
     */
    private static List<ParsingResult> parseLine(String[] dataSegments, float r, float g, float b, float a, GData1 parent, DatFile datFile, boolean errorCheckOnly) {
        List<ParsingResult> result = new ArrayList<>();
        boolean parseError = false;
        // [ERROR] Check argument count
        if (dataSegments.length != 8) {
            Object[] messageArguments = {dataSegments.length, 8};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.getLocale());
            formatter.applyPattern(I18n.DATPARSER_WRONG_ARGUMENT_COUNT);
            result.add(new ParsingResult(formatter.format(messageArguments), "[E99] " + I18n.DATPARSER_SYNTAX_ERROR, ResultType.ERROR)); //$NON-NLS-1$
        } else {
            // [ERROR] Check colour
            GColour colour = validateColour(dataSegments[1], r, g, b, a);
            if (colour == null) {
                result.add(new ParsingResult(I18n.DATPARSER_INVALID_COLOUR, "[E99] " + I18n.DATPARSER_SYNTAX_ERROR, ResultType.ERROR)); //$NON-NLS-1$
                return result;
            }
            // [ERROR] Check identical vertices
            try {
                // Start vertex
                start.setX(new BigDecimal(dataSegments[2], Threshold.MC));
                start.setY(new BigDecimal(dataSegments[3], Threshold.MC));
                start.setZ(new BigDecimal(dataSegments[4], Threshold.MC));
                // End vertex
                end.setX(new BigDecimal(dataSegments[5], Threshold.MC));
                end.setY(new BigDecimal(dataSegments[6], Threshold.MC));
                end.setZ(new BigDecimal(dataSegments[7], Threshold.MC));

                parseError = Vector3d.sub(start, end).length().compareTo(Threshold.IDENTICAL_VERTEX_DISTANCE) < 0;
                if (parseError) {
                    result.add(new ParsingResult(I18n.DATPARSER_IDENTICAL_VERTICES, "[E0D] " + I18n.DATPARSER_DATA_ERROR, ResultType.ERROR)); //$NON-NLS-1$
                }
                if (result.isEmpty() && !errorCheckOnly) {
                    GData2 data = new GData2(colour.getColourNumber(), colour.getR(), colour.getG(), colour.getB(), colour.getA(), start.x, start.y, start.z, end.x, end.y, end.z, parent, datFile, true);
                    result.add(new ParsingResult(data));
                }
                // [WARNING] Dithered colour
                if (colour.getType() != null && GCType.DITHERED == colour.getType().type()) {
                    result.add(new ParsingResult(I18n.DATPARSER_DITHERED_COLOUR, "[WDC] " + I18n.DATPARSER_WARNING, ResultType.WARN)); //$NON-NLS-1$
                }
            } catch (NumberFormatException nfe) {
                result.add(new ParsingResult(I18n.DATPARSER_INVALID_NUMBER_FORMAT, "[E99] " + I18n.DATPARSER_SYNTAX_ERROR, ResultType.ERROR)); //$NON-NLS-1$
            }
        }
        return result;
    }

    /**
     * Checks triangle lines and highlights syntax errors
     *
     * @param line
     *            the line to check
     * @param dataSegments
     * @param b
     * @param g
     * @param r
     * @param productMatrix
     * @param datFile
     * @param errorCheckOnly
     * @param det
     * @param invertNext
     * @param isCCW
     * @return an empty list if there was no error
     */
    private static List<ParsingResult> parseTriangle(String[] dataSegments, float r, float g, float b, float a, GData1 parent, DatFile datFile, boolean errorCheckOnly, int depth) {
        List<ParsingResult> result = new ArrayList<>();
        // [ERROR] Check argument count
        if (dataSegments.length != 11) {
            Object[] messageArguments = {dataSegments.length, 11};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.getLocale());
            formatter.applyPattern(I18n.DATPARSER_WRONG_ARGUMENT_COUNT);
            result.add(new ParsingResult(formatter.format(messageArguments), "[E99] " + I18n.DATPARSER_SYNTAX_ERROR, ResultType.ERROR)); //$NON-NLS-1$
        } else {
            // [ERROR] Check colour
            GColour colour = validateColour(dataSegments[1], r, g, b, a);
            if (colour == null) {
                result.add(new ParsingResult(I18n.DATPARSER_INVALID_COLOUR, "[E99] " + I18n.DATPARSER_SYNTAX_ERROR, ResultType.ERROR)); //$NON-NLS-1$
                return result;
            }
            // [ERROR] Check identical vertices
            try {
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

                if (!errorCheckOnly) { // result.size() < 1 &&
                    GData3 data = new GData3(colour.getColourNumber(), colour.getR(), colour.getG(), colour.getB(), colour.getA(), vertexA.x, vertexA.y, vertexA.z, vertexB.x, vertexB.y, vertexB.z,
                            vertexC.x, vertexC.y, vertexC.z, parent, datFile, true);
                    result.add(new ParsingResult(data));
                }
                if (depth < 1) {
                    boolean parseError = false;
                    double angle;

                    Vector3d.sub(vertexA, vertexC, vertexA2);
                    Vector3d.sub(vertexB, vertexC, vertexB2);
                    Vector3d.sub(vertexB, vertexA, vertexC2);

                    angle = Vector3d.angle(vertexA2, vertexB2);
                    double sumAngle = angle;
                    parseError = angle < Threshold.COLLINEAR_ANGLE_MINIMUM || angle > Threshold.COLLINEAR_ANGLE_MAXIMUM;

                    if (!parseError) {
                        vertexA2.negate();
                        angle = Vector3d.angle(vertexA2, vertexC2);
                        sumAngle = sumAngle + angle;
                        parseError = angle < Threshold.COLLINEAR_ANGLE_MINIMUM || angle > Threshold.COLLINEAR_ANGLE_MAXIMUM;
                    }

                    if (!parseError) {
                        angle = 180.0 - sumAngle;
                        parseError = angle < Threshold.COLLINEAR_ANGLE_MINIMUM || angle > Threshold.COLLINEAR_ANGLE_MAXIMUM;
                    }

                    if (parseError) {
                        result.add(new ParsingResult(I18n.DATPARSER_COLLINEAR_VERTICES, "[E01] " + I18n.DATPARSER_LOGIC_ERROR, ResultType.ERROR)); //$NON-NLS-1$
                    }

                    parseError = vertexA2.length().compareTo(Threshold.IDENTICAL_VERTEX_DISTANCE) < 0;
                    parseError = parseError || vertexB2.length().compareTo(Threshold.IDENTICAL_VERTEX_DISTANCE) < 0;
                    parseError = parseError || vertexC2.length().compareTo(Threshold.IDENTICAL_VERTEX_DISTANCE) < 0;
                    if (parseError) {
                        result.add(new ParsingResult(I18n.DATPARSER_IDENTICAL_VERTICES, "[E0D] " + I18n.DATPARSER_DATA_ERROR, ResultType.ERROR)); //$NON-NLS-1$
                    }
                    // [WARNING] Dithered colour
                    if (colour.getType() != null && GCType.DITHERED == colour.getType().type()) {
                        result.add(new ParsingResult(I18n.DATPARSER_DITHERED_COLOUR, "[WDC] " + I18n.DATPARSER_WARNING, ResultType.WARN)); //$NON-NLS-1$
                    }
                }
            } catch (NumberFormatException nfe) {
                result.add(new ParsingResult(I18n.DATPARSER_INVALID_NUMBER_FORMAT, "[E99] " + I18n.DATPARSER_SYNTAX_ERROR, ResultType.ERROR)); //$NON-NLS-1$
            }
        }
        return result;
    }

    /**
     * Checks quad lines and highlights syntax errors
     *
     * @param line
     *            the line to check
     * @param dataSegments
     * @param b
     * @param g
     * @param r
     * @param productMatrix
     * @param datFile
     * @param errorCheckOnly
     * @param det
     * @param invertNext
     * @param isCCW
     * @return an empty list if there was no error
     */
    private static List<ParsingResult> parseQuad(String[] dataSegments, float r, float g, float b, float a, GData1 parent, DatFile datFile, boolean errorCheckOnly, int depth) {
        List<ParsingResult> result = new ArrayList<>();
        // [ERROR] Check argument count
        if (dataSegments.length != 14) {
            Object[] messageArguments = {dataSegments.length, 14};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.getLocale());
            formatter.applyPattern(I18n.DATPARSER_WRONG_ARGUMENT_COUNT);
            result.add(new ParsingResult(formatter.format(messageArguments), "[E99] " + I18n.DATPARSER_SYNTAX_ERROR, ResultType.ERROR)); //$NON-NLS-1$
        } else {
            // [ERROR] Check colour
            GColour colour = validateColour(dataSegments[1], r, g, b, a);
            if (colour == null) {
                result.add(new ParsingResult(I18n.DATPARSER_INVALID_COLOUR, "[E99] " + I18n.DATPARSER_SYNTAX_ERROR, ResultType.ERROR)); //$NON-NLS-1$
                return result;
            }
            // [ERROR] Check hourglass, concave form, coplanarity & identical
            // vertices
            try {
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


                final boolean depthLower1 = depth < 1;

                vertexA2.x = vertexA.x;
                vertexA2.y = vertexA.y;
                vertexA2.z = vertexA.z;

                vertexB2.x = vertexB.x;
                vertexB2.y = vertexB.y;
                vertexB2.z = vertexB.z;

                vertexC2.x = vertexC.x;
                vertexC2.y = vertexC.y;
                vertexC2.z = vertexC.z;

                vertexD2.x = vertexD.x;
                vertexD2.y = vertexD.y;
                vertexD2.z = vertexD.z;

                Vector3d[] normals = new Vector3d[4];
                float[] normalDirections = new float[4];
                Vector3d[] lineVectors = new Vector3d[4];
                int cnc = 0;
                int fcc = 0;
                lineVectors[0] = Vector3d.sub(vertexB, vertexA);
                lineVectors[1] = Vector3d.sub(vertexC, vertexB);
                lineVectors[2] = Vector3d.sub(vertexD, vertexC);
                lineVectors[3] = Vector3d.sub(vertexA, vertexD);
                normals[0] = Vector3d.cross(lineVectors[0], lineVectors[1]);
                normals[1] = Vector3d.cross(lineVectors[1], lineVectors[2]);
                normals[2] = Vector3d.cross(lineVectors[2], lineVectors[3]);
                normals[3] = Vector3d.cross(lineVectors[3], lineVectors[0]);

                Vector3d normal = new Vector3d();

                for (int i = 0; i < 4; i++) {
                    normalDirections[i] = MathHelper.directionOfVectors(normals[0], normals[i]);
                    if (depthLower1 && normalDirections[i] < 0) {
                        if (cnc < 1) fcc = i;
                        cnc++;
                    }
                    normal = Vector3d.add(normals[i], normal);
                }

                if (!errorCheckOnly) { // result.size() < 1 &&
                    GData4 data = new GData4(colour.getColourNumber(), colour.getR(), colour.getG(), colour.getB(), colour.getA(), vertexA2.x, vertexA2.y, vertexA2.z, vertexB2.x, vertexB2.y,
                            vertexB2.z, vertexC2.x, vertexC2.y, vertexC2.z, vertexD2.x, vertexD2.y, vertexD2.z, normal, parent, datFile);
                    result.add(new ParsingResult(data));
                }
                if (depthLower1) {
                    boolean parseError = false;
                    boolean parseWarning = false;
                    parseError = cnc > 0 && cnc < 4;
                    if (cnc == 2) {
                        // Hourglass
                        if (fcc == 1) {
                            result.add(new ParsingResult(I18n.DATPARSER_HOURGLASS_QUADRILATERAL, "[E41] " + I18n.DATPARSER_DATA_ERROR, ResultType.ERROR)); //$NON-NLS-1$
                        } else {
                            // fcc is 2
                            result.add(new ParsingResult(I18n.DATPARSER_HOURGLASS_QUADRILATERAL, "[E42] " + I18n.DATPARSER_DATA_ERROR, ResultType.ERROR)); //$NON-NLS-1$
                        }
                    } else if (cnc == 1 || cnc == 3) {
                        // Concave
                        result.add(new ParsingResult(I18n.DATPARSER_CONCAVE_QUADRILATERAL, "[E04] " + I18n.DATPARSER_DATA_ERROR, ResultType.ERROR)); //$NON-NLS-1$
                    }

                    double angle;

                    // Coplanarity
                    if (!parseError) {

                        angle = Math.max(Vector3d.angle(normals[0], normals[2]), Vector3d.angle(normals[1], normals[3]));

                        parseWarning = angle > Threshold.coplanarityAngleWarning;
                        if (angle > Threshold.coplanarityAngleError) {
                            result.add(new ParsingResult(I18n.DATPARSER_COPLANARITY, "[E24] " + I18n.DATPARSER_DATA_ERROR, ResultType.ERROR)); //$NON-NLS-1$
                            parseError = true;
                            parseWarning = false;
                        }
                    }

                    Vector3d.sub(vertexB, vertexA, vertexA2);
                    Vector3d.sub(vertexB, vertexC, vertexB2);
                    Vector3d.sub(vertexD, vertexC, vertexC2);
                    Vector3d.sub(vertexD, vertexA, vertexD2);

                    if (!parseError) {

                        angle = Vector3d.angle(vertexA2, vertexD2);
                        double sumAngle = angle;
                        parseError = angle < Threshold.COLLINEAR_ANGLE_MINIMUM || angle > Threshold.COLLINEAR_ANGLE_MAXIMUM;

                        if (!parseError) {
                            angle = Vector3d.angle(vertexB2, vertexC2);
                            sumAngle = sumAngle + angle;
                            parseError = angle < Threshold.COLLINEAR_ANGLE_MINIMUM || angle > Threshold.COLLINEAR_ANGLE_MAXIMUM;
                        }

                        if (!parseError) {
                            vertexA2.negate();
                            vertexB2.negate();
                            angle = Vector3d.angle(vertexA2, vertexB2);
                            sumAngle = sumAngle + angle;
                            parseError = angle < Threshold.COLLINEAR_ANGLE_MINIMUM || angle > Threshold.COLLINEAR_ANGLE_MAXIMUM;

                        }

                        if (!parseError) {
                            angle = 360.0 - sumAngle;
                            parseError = angle < Threshold.COLLINEAR_ANGLE_MINIMUM || angle > Threshold.COLLINEAR_ANGLE_MAXIMUM;
                        }

                        if (parseError) {
                            result.add(new ParsingResult(I18n.DATPARSER_COLLINEAR_VERTICES, "[E34] " + I18n.DATPARSER_DATA_ERROR, ResultType.ERROR)); //$NON-NLS-1$
                        }
                    }

                    parseError = vertexA2.length().compareTo(Threshold.IDENTICAL_VERTEX_DISTANCE) < 0;
                    parseError = parseError || vertexB2.length().compareTo(Threshold.IDENTICAL_VERTEX_DISTANCE) < 0;
                    parseError = parseError || vertexC2.length().compareTo(Threshold.IDENTICAL_VERTEX_DISTANCE) < 0;
                    parseError = parseError || vertexD2.length().compareTo(Threshold.IDENTICAL_VERTEX_DISTANCE) < 0;
                    if (parseError) {
                        if (!errorCheckOnly) {
                            ParsingResult p1 = result.get(0);
                            result.clear();
                            result.add(p1);
                        } else {
                            result.clear();
                        }
                        result.add(new ParsingResult(I18n.DATPARSER_IDENTICAL_VERTICES, "[E44] " + I18n.DATPARSER_DATA_ERROR, ResultType.ERROR)); //$NON-NLS-1$

                    } else if (parseWarning) {
                        result.add(new ParsingResult(I18n.DATPARSER_NEAR_COPLANARITY, "[W24] " + I18n.DATPARSER_WARNING, ResultType.WARN)); //$NON-NLS-1$
                    }
                    // [WARNING] Dithered colour
                    if (colour.getType() != null && GCType.DITHERED == colour.getType().type()) {
                        result.add(new ParsingResult(I18n.DATPARSER_DITHERED_COLOUR, "[WDC] " + I18n.DATPARSER_WARNING, ResultType.WARN)); //$NON-NLS-1$
                    }
                }
            } catch (NumberFormatException nfe) {
                result.add(new ParsingResult(I18n.DATPARSER_INVALID_NUMBER_FORMAT, "[E99] " + I18n.DATPARSER_SYNTAX_ERROR, ResultType.ERROR)); //$NON-NLS-1$
            }
        }

        return result;
    }

    /**
     * Checks conditional lines and highlights syntax errors
     *
     * @param line
     *            the line to check
     * @param dataSegments
     * @param b
     * @param g
     * @param r
     * @param datFile
     * @param errorCheckOnly
     * @param p
     * @param det
     * @param invertNext
     * @param isCCW
     * @return an empty list if there was no error
     */
    private static List<ParsingResult> parseCondline(String[] dataSegments, float r, float g, float b, float a, GData1 parent, DatFile datFile, boolean errorCheckOnly, int depth) {
        List<ParsingResult> result = new ArrayList<>();
        // [ERROR] Check argument count
        if (dataSegments.length != 14) {
            Object[] messageArguments = {dataSegments.length, 14};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.getLocale());
            formatter.applyPattern(I18n.DATPARSER_WRONG_ARGUMENT_COUNT);
            result.add(new ParsingResult(formatter.format(messageArguments), "[E99] " + I18n.DATPARSER_SYNTAX_ERROR, ResultType.ERROR)); //$NON-NLS-1$
        } else {
            // [ERROR] Check colour
            GColour colour = validateColour(dataSegments[1], r, g, b, a);
            if (colour == null) {
                result.add(new ParsingResult(I18n.DATPARSER_INVALID_COLOUR, "[E99] " + I18n.DATPARSER_SYNTAX_ERROR, ResultType.ERROR)); //$NON-NLS-1$
                return result;
            }
            // [ERROR] Check identical vertices
            try {
                // start vertex
                start.setX(new BigDecimal(dataSegments[2], Threshold.MC));
                start.setY(new BigDecimal(dataSegments[3], Threshold.MC));
                start.setZ(new BigDecimal(dataSegments[4], Threshold.MC));
                // end vertex
                end.setX(new BigDecimal(dataSegments[5], Threshold.MC));
                end.setY(new BigDecimal(dataSegments[6], Threshold.MC));
                end.setZ(new BigDecimal(dataSegments[7], Threshold.MC));
                // control vertex I
                controlI.setX(new BigDecimal(dataSegments[8], Threshold.MC));
                controlI.setY(new BigDecimal(dataSegments[9], Threshold.MC));
                controlI.setZ(new BigDecimal(dataSegments[10], Threshold.MC));
                // control vertex II
                controlII.setX(new BigDecimal(dataSegments[11], Threshold.MC));
                controlII.setY(new BigDecimal(dataSegments[12], Threshold.MC));
                controlII.setZ(new BigDecimal(dataSegments[13], Threshold.MC));

                if (Vector3d.sub(start, end).length().compareTo(Threshold.IDENTICAL_VERTEX_DISTANCE) < 0) {
                    result.add(new ParsingResult(I18n.DATPARSER_IDENTICAL_VERTICES, "[E0D] " + I18n.DATPARSER_DATA_ERROR, ResultType.ERROR)); //$NON-NLS-1$
                } else if (depth < 1 && Vector3d.sub(controlI, controlII).length().compareTo(Threshold.IDENTICAL_VERTEX_DISTANCE) < 0) {
                    result.add(new ParsingResult(I18n.DATPARSER_IDENTICAL_CONTROL_POINTS, "[E05] " + I18n.DATPARSER_DATA_ERROR, ResultType.ERROR)); //$NON-NLS-1$
                }
                if (result.isEmpty() && !errorCheckOnly) {
                    GData5 data = new GData5(colour.getColourNumber(), colour.getR(), colour.getG(), colour.getB(), colour.getA(), start.x, start.y, start.z, end.x, end.y, end.z, controlI.x,
                            controlI.y, controlI.z, controlII.x, controlII.y, controlII.z, parent, datFile);
                    result.add(new ParsingResult(data));
                }
                if (depth < 1 && colour.getType() != null && GCType.DITHERED == colour.getType().type()) {
                    result.add(new ParsingResult(I18n.DATPARSER_DITHERED_COLOUR, "[WDC] " + I18n.DATPARSER_WARNING, ResultType.WARN)); //$NON-NLS-1$
                }
            } catch (NumberFormatException nfe) {
                result.add(new ParsingResult(I18n.DATPARSER_INVALID_NUMBER_FORMAT, "[E99] " + I18n.DATPARSER_SYNTAX_ERROR, ResultType.ERROR)); //$NON-NLS-1$
            }
        }

        return result;
    }

    public static void triggerPngImageUpdate() {
        if (updatePngImages) {
            updatePngImages = false;
            Editor3DWindow.getWindow().updateBgPictureTab();
        }
    }
}
