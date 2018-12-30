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
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.csg.CSG;
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
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * Provides a static parser for LDraw files and lines
 *
 * @author nils
 *
 */
public enum DatParser {
    INSTANCE;

    private static boolean upatePngImages = false;

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

    public static ArrayList<ParsingResult> parseLine(String line, int lineNumber, int depth, float r, float g, float b, float a, GData1 parent, Matrix4f productMatrix, Matrix accurateProductMatrix,
            DatFile datFile, boolean errorCheckOnly, Set<String> alreadyParsed, boolean checkForFlatScaling) {
        ArrayList<ParsingResult> result = new ArrayList<ParsingResult>();
        // Get the linetype
        int linetype = 0;
        final String[] data_segments = WHITESPACE.split(line.trim());

        char c;
        if (data_segments.length < 1 || data_segments[0].length() >  1 || !Character.isDigit(c = data_segments[0].charAt(0))) {
            result.add(new ParsingResult(I18n.DATPARSER_InvalidType, "[E0D] " + I18n.DATPARSER_SyntaxError, ResultType.ERROR)); //$NON-NLS-1$
            return new ArrayList<ParsingResult>(result);
        }
        linetype = Character.getNumericValue(c);
        // Parse the line according to its type
        switch (linetype) {
        case 0:
            result.addAll(parse_Comment(line, lineNumber, data_segments, depth, r, g, b, a, parent, productMatrix, datFile, errorCheckOnly, alreadyParsed));
            break;
        case 1:
            result.addAll(parse_Reference(data_segments, depth, r, g, b, a, parent, productMatrix, accurateProductMatrix, datFile, errorCheckOnly, alreadyParsed, checkForFlatScaling, lineNumber));
            break;
        case 2:
            result.addAll(parse_Line(data_segments, r, g, b, a, parent, datFile, errorCheckOnly));
            break;
        case 3:
            result.addAll(parse_Triangle(data_segments, r, g, b, a, parent, datFile, errorCheckOnly, depth));
            break;
        case 4:
            result.addAll(parse_Quad(data_segments, r, g, b, a, parent, datFile, errorCheckOnly, depth));
            break;
        case 5:
            result.addAll(parse_Condline(data_segments, r, g, b, a, parent, datFile, errorCheckOnly, depth));
            break;
        default:
            // Mark unknown linetypes as error
            Object[] messageArguments = {linetype};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.LOCALE);
            formatter.applyPattern(I18n.DATPARSER_UnknownLineType);
            result.add(new ParsingResult(formatter.format(messageArguments), "[E99] " + I18n.DATPARSER_SyntaxError, ResultType.ERROR)); //$NON-NLS-1$
        }
        return result;
    }

    /**
     * Validates the colour argument and highlights possible errors
     *
     * Please note that the returned value will be always the same instance due
     * to performance reasons! <br>
     * Use {@code clone()} to obtain a new instance!
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
                cValue.set(24, View.line_Colour_r[0], View.line_Colour_g[0], View.line_Colour_b[0], 1f);
                break;
            default:
                if (View.hasLDConfigColour(colourValue)) {
                    GColour colour = View.getLDConfigColour(colourValue);
                    cValue.set(colour);
                } else {
                    int A = colourValue - 256 >> 4;
                    int B = colourValue - 256 & 0x0F;
                    if (View.hasLDConfigColour(A) && View.hasLDConfigColour(B)) {
                        GColour colourA = View.getLDConfigColour(A);
                        GColour colourB = View.getLDConfigColour(B);
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
     * Use {@code clone()} to obtain a new instance!
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
            cValue.set(24, View.line_Colour_r[0], View.line_Colour_g[0], View.line_Colour_b[0], 1f);
            break;
        default:
            if (View.hasLDConfigColour(arg)) {
                GColour colour = View.getLDConfigColour(arg);
                cValue.set(colour);
            } else {
                int A = arg - 256 >> 4;
                int B = arg - 256 & 0x0F;
                if (View.hasLDConfigColour(A) && View.hasLDConfigColour(B)) {
                    GColour colourA = View.getLDConfigColour(A);
                    GColour colourB = View.getLDConfigColour(B);
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
     * @param line
     *            the line to check
     * @param data_segments
     * @param parent
     * @param productMatrix
     * @param datFile
     * @param errorCheckOnly
     * @param det
     * @param invertNext
     * @param isCCW
     * @return an empty list if there was no error
     */
    private static ArrayList<ParsingResult> parse_Comment(String line, int lineNumber, String[] data_segments, int depth, float r, float g, float b, float a, GData1 parent, Matrix4f productMatrix,
            DatFile datFile, boolean errorCheckOnly, Set<String> alreadyParsed) {

        ArrayList<ParsingResult> result = new ArrayList<ParsingResult>();
        line = WHITESPACE.matcher(line).replaceAll(" ").trim(); //$NON-NLS-1$

        if (line.startsWith(I18n.DATFILE_InlinePrefix)) {
            result.add(new ParsingResult(new GData0(line, parent)));
            result.add(new ParsingResult(I18n.DATPARSER_InliningRelict, "[W01] " + I18n.DATPARSER_Warning, ResultType.WARN)); //$NON-NLS-1$
        } else if (line.startsWith("0 !: ")) { //$NON-NLS-1$
            GData newLPEmetaTag = TexMapParser.parseGeometry(line, depth, r, g, b, a, parent, productMatrix, alreadyParsed, datFile);
            if (newLPEmetaTag == null) {
                newLPEmetaTag = new GData0(line, parent);
                result.add(new ParsingResult(newLPEmetaTag));
                result.add(new ParsingResult(I18n.DATPARSER_InvalidTEXMAP, "[E99] " + I18n.DATPARSER_SyntaxError, ResultType.ERROR)); //$NON-NLS-1$
            } else {
                result.add(new ParsingResult(newLPEmetaTag));
            }
        } else if (line.startsWith("0 !TEXMAP ")) { //$NON-NLS-1$
            GData newLPEmetaTag = TexMapParser.parseTEXMAP(data_segments, line, parent, datFile);
            if (newLPEmetaTag == null) {
                newLPEmetaTag = new GData0(line, parent);
                result.add(new ParsingResult(newLPEmetaTag));
                result.add(new ParsingResult(I18n.DATPARSER_InvalidTEXMAP, "[E99] " + I18n.DATPARSER_SyntaxError, ResultType.ERROR)); //$NON-NLS-1$
            } else {
                result.add(new ParsingResult(newLPEmetaTag));
            }
        } else if (line.startsWith("0 !LPE")) { //$NON-NLS-1$
            GData0 newLPEmetaTag = new GData0(line, parent);
            result.add(new ParsingResult(newLPEmetaTag));
            result.add(new ParsingResult(I18n.DATPARSER_UnofficialMetaCommand, "[W0D] " + I18n.DATPARSER_Warning, ResultType.WARN)); //$NON-NLS-1$
            if (line.startsWith("TODO ", 7)) { //$NON-NLS-1$
                result.add(new ParsingResult(line.substring(12), "[WFF] " + I18n.DATPARSER_TODO, ResultType.WARN)); //$NON-NLS-1$
            } else if (line.startsWith("VERTEX ", 7)) { //$NON-NLS-1$
                Object[] messageArguments = {line.substring(14)};
                MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                formatter.setLocale(MyLanguage.LOCALE);
                formatter.applyPattern(I18n.DATPARSER_VertexAt);
                result.add(new ParsingResult(formatter.format(messageArguments) , "[WFE] " + I18n.DATPARSER_VertexDeclaration, ResultType.WARN)); //$NON-NLS-1$
                boolean numberError = false;
                if (data_segments.length == 6) {
                    try {
                        start.setX(new BigDecimal(data_segments[3], Threshold.mc));
                        start.setY(new BigDecimal(data_segments[4], Threshold.mc));
                        start.setZ(new BigDecimal(data_segments[5], Threshold.mc));
                    } catch (NumberFormatException nfe) {
                        numberError = true;
                    }
                } else {
                    numberError = true;
                }
                if (numberError) {
                    result.add(new ParsingResult(I18n.DATPARSER_InvalidNumberFormat, "[E99] " + I18n.DATPARSER_SyntaxError, ResultType.ERROR)); //$NON-NLS-1$
                } else if (!errorCheckOnly) {
                    if (depth == 0) {
                        datFile.getVertexManager().addVertex(new Vertex(start.X, start.Y, start.Z), newLPEmetaTag);
                    } else {
                        Vector4f vert = new Vector4f(start.getXf() * 1000f, start.getYf() * 1000f, start.getZf() * 1000f, 1f);
                        Matrix4f.transform(productMatrix, vert, vert);
                        datFile.getVertexManager().addSubfileVertex(new Vertex(vert), newLPEmetaTag, parent);
                    }
                }
            } else if (line.startsWith("DISTANCE ", 7)) { //$NON-NLS-1$
                boolean numberError = false;
                final GColour colour;
                if (data_segments.length == 10) {
                    colour = validateColour(data_segments[3], r, g, b, a);
                    if (colour == null) {
                        result.add(new ParsingResult(I18n.DATPARSER_InvalidColour, "[E99] " + I18n.DATPARSER_SyntaxError, ResultType.ERROR)); //$NON-NLS-1$
                        return result;
                    }
                    try {
                        start.setX(new BigDecimal(data_segments[4], Threshold.mc));
                        start.setY(new BigDecimal(data_segments[5], Threshold.mc));
                        start.setZ(new BigDecimal(data_segments[6], Threshold.mc));
                        end.setX(new BigDecimal(data_segments[7], Threshold.mc));
                        end.setY(new BigDecimal(data_segments[8], Threshold.mc));
                        end.setZ(new BigDecimal(data_segments[9], Threshold.mc));
                    } catch (NumberFormatException nfe) {
                        numberError = true;
                    }
                } else {
                    numberError = true;
                    colour = null;
                }
                if (numberError) {
                    result.add(new ParsingResult(I18n.DATPARSER_InvalidNumberFormat, "[E99] " + I18n.DATPARSER_SyntaxError, ResultType.ERROR)); //$NON-NLS-1$
                } else if (!errorCheckOnly) {
                    if (depth == 0) {
                        result.remove(0);
                        result.add(0, new ParsingResult(new GData2(colour.getColourNumber(), colour.getR(), colour.getG(), colour.getB(), colour.getA(), start.X, start.Y, start.Z, end.X, end.Y, end.Z, parent, datFile, false)));
                    }
                }
            } else if (line.startsWith("PROTRACTOR ", 7)) { //$NON-NLS-1$) {
                boolean numberError = false;
                final GColour colour;
                if (data_segments.length == 13) {
                    colour = validateColour(data_segments[3], r, g, b, a);
                    if (colour == null) {
                        result.add(new ParsingResult(I18n.DATPARSER_InvalidColour, "[E99] " + I18n.DATPARSER_SyntaxError, ResultType.ERROR)); //$NON-NLS-1$
                        return result;
                    }
                    try {
                        vertexA.setX(new BigDecimal(data_segments[4], Threshold.mc));
                        vertexA.setY(new BigDecimal(data_segments[5], Threshold.mc));
                        vertexA.setZ(new BigDecimal(data_segments[6], Threshold.mc));
                        vertexB.setX(new BigDecimal(data_segments[7], Threshold.mc));
                        vertexB.setY(new BigDecimal(data_segments[8], Threshold.mc));
                        vertexB.setZ(new BigDecimal(data_segments[9], Threshold.mc));
                        vertexC.setX(new BigDecimal(data_segments[10], Threshold.mc));
                        vertexC.setY(new BigDecimal(data_segments[11], Threshold.mc));
                        vertexC.setZ(new BigDecimal(data_segments[12], Threshold.mc));
                    } catch (NumberFormatException nfe) {
                        numberError = true;
                    }
                } else {
                    numberError = true;
                    colour = null;
                }
                if (numberError) {
                    result.add(new ParsingResult(I18n.DATPARSER_InvalidNumberFormat, "[E99] " + I18n.DATPARSER_SyntaxError, ResultType.ERROR)); //$NON-NLS-1$
                } else if (!errorCheckOnly) {
                    if (depth == 0) {
                        result.remove(0);
                        result.add(0, new ParsingResult(new GData3(colour.getColourNumber(), colour.getR(), colour.getG(), colour.getB(), colour.getA(), vertexA.X, vertexA.Y, vertexA.Z, vertexB.X, vertexB.Y, vertexB.Z, vertexC.X, vertexC.Y, vertexC.Z, parent, datFile, false)));
                    }
                }
            } else if (line.startsWith("CSG_", 7)) { //$NON-NLS-1$
                if (line.startsWith("UNION", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSG.UNION, line, parent)));
                } else if (line.startsWith("DIFFERENCE", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSG.DIFFERENCE, line, parent)));
                } else if (line.startsWith("INTERSECTION", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSG.INTERSECTION, line, parent)));
                } else if (line.startsWith("TRANSFORM", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSG.TRANSFORM, line, parent)));
                } else if (line.startsWith("CUBOID", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSG.CUBOID, line, parent)));
                } else if (line.startsWith("ELLIPSOID", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSG.ELLIPSOID, line, parent)));
                } else if (line.startsWith("QUAD", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSG.QUAD, line, parent)));
                } else if (line.startsWith("CYLINDER", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSG.CYLINDER, line, parent)));
                } else if (line.startsWith("MESH", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSG.MESH, line, parent)));
                } else if (line.startsWith("CONE", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSG.CONE, line, parent)));
                } else if (line.startsWith("CIRCLE", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSG.CIRCLE, line, parent)));
                } else if (line.startsWith("COMPILE", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSG.COMPILE, line, parent)));
                } else if (line.startsWith("QUALITY", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSG.QUALITY, line, parent)));
                } else if (line.startsWith("EPSILON", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSG.EPSILON, line, parent)));
                } else if (line.startsWith("TJUNCTION_EPSILON", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSG.TJUNCTION, line, parent)));
                } else if (line.startsWith("EDGE_COLLAPSE_EPSILON", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSG.COLLAPSE, line, parent)));
                } else if (line.startsWith("DONT_OPTIMIZE", 11) || line.startsWith("DONT_OPTIMISE", 11) ) { //$NON-NLS-1$ //$NON-NLS-2$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSG.DONTOPTIMIZE, line, parent)));
                } else if (line.startsWith("EXTRUDE", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSG.EXTRUDE, line, parent)));
                } else if (line.startsWith("EXT_CFG", 11)) { //$NON-NLS-1$
                    result.remove(0);
                    result.add(0, new ParsingResult(new GDataCSG(datFile, CSG.EXTRUDE_CFG, line, parent)));
                    GDataCSG.forceRecompile(datFile);
                }
            } else if (line.startsWith("PNG", 7) && depth == 0 && data_segments.length >= 12) { //$NON-NLS-1$
                try {
                    Vertex offset = new Vertex(new BigDecimal(data_segments[3]), new BigDecimal(data_segments[4]), new BigDecimal(data_segments[5]));
                    BigDecimal a1 = new BigDecimal(data_segments[6]);
                    BigDecimal a2 = new BigDecimal(data_segments[7]);
                    BigDecimal a3 = new BigDecimal(data_segments[8]);
                    Vertex scale = new Vertex(new BigDecimal(data_segments[9]), new BigDecimal(data_segments[10]), BigDecimal.ONE);
                    StringBuilder sb = new StringBuilder();
                    for (int s = 11; s < data_segments.length - 1; s++) {
                        sb.append(data_segments[s]);
                        sb.append(" "); //$NON-NLS-1$
                    }
                    sb.append(data_segments[data_segments.length - 1]);
                    result.remove(0);
                    final GDataPNG gpng = new GDataPNG(line, offset, a1, a2, a3, scale, sb.toString(), parent);
                    if (!errorCheckOnly) datFile.getVertexManager().setSelectedBgPicture(gpng);
                    result.add(0, new ParsingResult(gpng));
                    if (!errorCheckOnly) upatePngImages = true;
                } catch (Exception ex) {}
            }
        } else if (line.startsWith("0 BFC ")) { //$NON-NLS-1$
            if (line.startsWith("INVERTNEXT", 6)) { //$NON-NLS-1$
                result.add(new ParsingResult(new GDataBFC(BFC.INVERTNEXT, parent)));
            } else if (line.startsWith("CERTIFY", 6)) { //$NON-NLS-1$
                if (line.startsWith("CLIP CCW", 14) || line.startsWith("CCW CLIP", 14)) { //$NON-NLS-1$ //$NON-NLS-2$
                    result.add(new ParsingResult(I18n.DATPARSER_MLCAD_ClipCCW, "[W0C] " + I18n.DATPARSER_Warning, ResultType.WARN)); //$NON-NLS-1$
                    result.add(new ParsingResult(new GDataBFC(BFC.CCW_CLIP, parent)));
                } else if (line.startsWith("CLIP CW", 14) || line.startsWith("CW CLIP", 14)) { //$NON-NLS-1$ //$NON-NLS-2$
                    result.add(new ParsingResult(I18n.DATPARSER_MLCAD_ClipCW, "[W0C] " + I18n.DATPARSER_Warning, ResultType.WARN)); //$NON-NLS-1$
                    result.add(new ParsingResult(new GDataBFC(BFC.CW_CLIP, parent)));
                } else if (line.startsWith("CCW", 14)) { //$NON-NLS-1$
                    result.add(new ParsingResult(new GDataBFC(BFC.CCW_CLIP, parent)));
                } else if (line.startsWith("CW", 14)) { //$NON-NLS-1$
                    result.add(new ParsingResult(new GDataBFC(BFC.CW_CLIP, parent)));
                } else if (line.startsWith("INVERTNEXT", 14)) { //$NON-NLS-1$
                    result.add(new ParsingResult(I18n.DATPARSER_MLCAD_InvertNext, "[W0B] " + I18n.DATPARSER_Warning, ResultType.WARN)); //$NON-NLS-1$
                    result.add(new ParsingResult(new GDataBFC(BFC.INVERTNEXT, parent)));
                } else if (line.startsWith("CLIP", 14)) { //$NON-NLS-1$
                    result.add(new ParsingResult(I18n.DATPARSER_MLCAD_Clip, "[W0B] " + I18n.DATPARSER_Warning, ResultType.WARN)); //$NON-NLS-1$
                    result.add(new ParsingResult(new GDataBFC(BFC.CLIP, parent)));
                } else if (line.startsWith("NOCLIP", 14)) { //$NON-NLS-1$
                    result.add(new ParsingResult(I18n.DATPARSER_MLCAD_NoClip, "[W0B] " + I18n.DATPARSER_Warning, ResultType.WARN)); //$NON-NLS-1$
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
     * @param data_segments
     * @param r
     * @param g
     * @param b
     * @param parent
     * @param accurateProductMatrix
     * @param datFile
     * @param errorCheckOnly
     * @param alreadyParsed
     * @param line
     *            the line to check
     * @param det2
     * @param invertNext
     * @param isCCW
     * @return an empty list if there was no error
     */
    private static ArrayList<ParsingResult> parse_Reference(String[] data_segments, int depth, float r, float g, float b, float a, GData1 parent, Matrix4f productMatrix, Matrix accurateProductMatrix,
            DatFile datFile, boolean errorCheckOnly, Set<String> alreadyParsed, boolean checkForFlatScaling, int lineNumber) {
        ArrayList<ParsingResult> result = new ArrayList<ParsingResult>();
        boolean parseError = false;
        boolean hasDitheredColour = false;
        // [ERROR] Check less argument count
        if (data_segments.length < 15) {
            Object[] messageArguments = {data_segments.length, 15};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.LOCALE);
            formatter.applyPattern(I18n.DATPARSER_WrongArgumentCount);
            result.add(new ParsingResult(formatter.format(messageArguments), "[E99] " + I18n.DATPARSER_SyntaxError, ResultType.ERROR)); //$NON-NLS-1$
        } else {
            // [ERROR] Check colour
            GColour colour = validateColour(data_segments[1], r, g, b, a);
            if (colour == null) {
                result.add(new ParsingResult(I18n.DATPARSER_InvalidColour, "[E99] " + I18n.DATPARSER_SyntaxError, ResultType.ERROR)); //$NON-NLS-1$
                return result;
            }
            hasDitheredColour = colour.getType() != null && GCType.DITHERED == colour.getType().type();
            // [ERROR] Check singularity
            Matrix4f tMatrix = new Matrix4f();
            BigDecimal M00;
            BigDecimal M01;
            BigDecimal M02;
            final BigDecimal M03 = BigDecimal.ZERO;
            BigDecimal M10;
            BigDecimal M11;
            BigDecimal M12;
            final BigDecimal M13 = BigDecimal.ZERO;
            BigDecimal M20;
            BigDecimal M21;
            BigDecimal M22;
            final BigDecimal M23 = BigDecimal.ZERO;
            BigDecimal M30;
            BigDecimal M31;
            BigDecimal M32;
            final BigDecimal M33 = BigDecimal.ONE;
            float det = 0;
            while (true) {
                try {
                    // Offset
                    M30 = new BigDecimal(data_segments[2]);
                    tMatrix.m30 = M30.floatValue() * 1000f;
                    M31 = new BigDecimal(data_segments[3]);
                    tMatrix.m31 = M31.floatValue() * 1000f;
                    M32 = new BigDecimal(data_segments[4]);
                    tMatrix.m32 = M32.floatValue() * 1000f;
                    // First row
                    M00 = new BigDecimal(data_segments[5]);
                    tMatrix.m00 = M00.floatValue();
                    M10 = new BigDecimal(data_segments[6]);
                    tMatrix.m10 = M10.floatValue();
                    M20 = new BigDecimal(data_segments[7]);
                    tMatrix.m20 = M20.floatValue();
                    // Second row
                    M01 = new BigDecimal(data_segments[8]);
                    tMatrix.m01 = M01.floatValue();
                    M11 = new BigDecimal(data_segments[9]);
                    tMatrix.m11 = M11.floatValue();
                    M21 = new BigDecimal(data_segments[10]);
                    tMatrix.m21 = M21.floatValue();
                    // Third row
                    M02 = new BigDecimal(data_segments[11]);
                    tMatrix.m02 = M02.floatValue();
                    M12 = new BigDecimal(data_segments[12]);
                    tMatrix.m12 = M12.floatValue();
                    M22 = new BigDecimal(data_segments[13]);
                    tMatrix.m22 = M22.floatValue();
                } catch (NumberFormatException nfe) {
                    M00 = null; M01 = null; M02 = null; M10 = null;
                    M11 = null; M12 = null; M20 = null; M21 = null;
                    M22 = null; M30 = null; M31 = null; M32 = null;
                    result.add(new ParsingResult(I18n.DATPARSER_InvalidNumberFormat, "[E99] " + I18n.DATPARSER_SyntaxError, ResultType.ERROR)); //$NON-NLS-1$
                    break;
                }
                tMatrix.m33 = 1f;
                det = tMatrix.determinant();
                parseError = Math.abs(det) < Threshold.singularity_determinant;
                break;
            }
            // [WARNING] Check file existance
            boolean fileExists = true;
            StringBuilder sb = new StringBuilder();
            for (int s = 14; s < data_segments.length - 1; s++) {
                sb.append(data_segments[s]);
                sb.append(" "); //$NON-NLS-1$
            }
            sb.append(data_segments[data_segments.length - 1]);
            String shortFilename = sb.toString();
            boolean isLowercase = shortFilename.equals(shortFilename.toLowerCase(Locale.ENGLISH));
            shortFilename = shortFilename.toLowerCase(Locale.ENGLISH);
            try {
                shortFilename = shortFilename.replaceAll("s\\\\", "S" + File.separator).replaceAll("\\\\", File.separator); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            } catch (Exception e) {
                // Workaround for windows OS / JVM BUG
                shortFilename = shortFilename.replace("s\\", "S" + File.separator).replace("\\", File.separator); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            if (alreadyParsed.contains(shortFilename)) {
                result.add(new ParsingResult(I18n.DATPARSER_Recursive, "[E01] " + I18n.DATPARSER_LogicError, ResultType.ERROR)); //$NON-NLS-1$
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

            ArrayList<String> lines = null;
            if (parseError) {
                result.add(new ParsingResult(I18n.DATPARSER_SingularMatrix, "[E02] " + I18n.DATPARSER_LogicError, ResultType.ERROR)); //$NON-NLS-1$
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
                                lines = new ArrayList<String>(4096);
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

                if (result.size() < 1) {
                    if (!errorCheckOnly) {

                        Matrix TMatrix = new Matrix(M00, M01, M02, M03, M10, M11, M12, M13, M20, M21, M22, M23, M30, M31, M32, M33);
                        Matrix DESTMatrix = Matrix.mul(accurateProductMatrix, TMatrix);

                        Matrix4f destMatrix = new Matrix4f();
                        Matrix4f.mul(productMatrix, tMatrix, destMatrix);

                        result.add(new ParsingResult(new GData1(colour.getColourNumber(), colour.getR(), colour.getG(), colour.getB(), colour.getA(), tMatrix, TMatrix, lines, absoluteFilename, sb
                                .toString(), depth, det < 0, destMatrix, DESTMatrix, datFile, parent.firstRef, readOnly, errorCheckOnly, alreadyParsed, parent)));
                        GDataCSG.forceRecompile(datFile);
                    }

                    // Avoid scaling of flat files
                    if (depth == 0) {
                        GData g1 = null;
                        if (result.size() == 1) {
                            g1 = result.get(0).getGraphicalData();
                        }
                        if (g1 == null) {
                            g1 = datFile.getDrawPerLine_NOCLONE().getValue(lineNumber);
                        }
                        if (g1 != null && g1.type() == 1) {
                            result.addAll(datFile.getVertexManager().checkForFlatScaling((GData1) g1));
                        }

                    }

                }

            } else if (!fileExists) {
                result.add(new ParsingResult(I18n.DATPARSER_FileNotFound, "[E01] " + I18n.DATPARSER_DataError, ResultType.ERROR)); //$NON-NLS-1$
            } else {
                absoluteFilename = fileToOpen.getAbsolutePath();
                UTF8BufferedReader reader = null;
                String line = null;
                lines = new ArrayList<String>(4096);
                try {
                    reader = new UTF8BufferedReader(absoluteFilename);
                    while (true) {
                        line = reader.readLine();
                        if (line == null) {
                            break;
                        }
                        lines.add(line);
                    }
                } catch (FileNotFoundException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (LDParsingException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (UnsupportedEncodingException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } finally {
                    try {
                        if (reader != null)
                            reader.close();
                    } catch (LDParsingException e1) {
                    }
                }

                if (result.size() < 1) {
                    if (!errorCheckOnly) {

                        Matrix TMatrix = new Matrix(M00, M01, M02, M03, M10, M11, M12, M13, M20, M21, M22, M23, M30, M31, M32, M33);
                        Matrix DESTMatrix = Matrix.mul(accurateProductMatrix, TMatrix);

                        Matrix4f destMatrix = new Matrix4f();
                        Matrix4f.mul(productMatrix, tMatrix, destMatrix);

                        result.add(new ParsingResult(new GData1(colour.getColourNumber(), colour.getR(), colour.getG(), colour.getB(), colour.getA(), tMatrix, TMatrix, lines, absoluteFilename, sb
                                .toString(), depth, det < 0, destMatrix, DESTMatrix, datFile, parent.firstRef, readOnly, errorCheckOnly, alreadyParsed, parent)));
                    }

                    // Avoid scaling of flat files
                    if (depth == 0) {
                        GData g1 = null;
                        if (result.size() == 1) {
                            g1 = result.get(0).getGraphicalData();
                        }
                        if (g1 == null) {
                            g1 = datFile.getDrawPerLine_NOCLONE().getValue(lineNumber);
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
                        result.add(new ParsingResult(I18n.DATPARSER_Recursive, "[E01] " + I18n.DATPARSER_LogicError, ResultType.ERROR)); //$NON-NLS-1$
                    }
                    if (g1.firstRef.isMovedTo()) {
                        result.add(new ParsingResult(I18n.DATPARSER_MovedTo, "[E2A] " + I18n.DATPARSER_LogicError, ResultType.ERROR)); //$NON-NLS-1$
                    }
                }
            }
            alreadyParsed.remove(shortFilename);
            // [WARNING] Check spaces in dat file name
            if (data_segments.length > 15) {
                result.add(new ParsingResult(I18n.DATPARSER_FilenameWhitespace, "[W01] " + I18n.DATPARSER_Warning, ResultType.WARN)); //$NON-NLS-1$
            }
            // [WARNING] Dithered colour
            if (hasDitheredColour) {
                result.add(new ParsingResult(I18n.DATPARSER_DitheredColour, "[WDC] " + I18n.DATPARSER_Warning, ResultType.WARN)); //$NON-NLS-1$
            }
            // [WARNING] Upper- & Mixed-Case file name
            if (!isLowercase) {
                result.add(new ParsingResult(I18n.DATPARSER_InvalidCase, "[WCC] " + I18n.DATPARSER_Warning, ResultType.WARN)); //$NON-NLS-1$
            }
        }
        return result;
    }

    /**
     * Checks "line lines" and highlights syntax errors
     *
     * @param line
     *            the line to check
     * @param data_segments
     * @param b
     * @param g
     * @param r
     * @param datFile
     * @param parent
     * @param errorCheckOnly
     * @return an empty list if there was no error
     */
    private static ArrayList<ParsingResult> parse_Line(String[] data_segments, float r, float g, float b, float a, GData1 parent, DatFile datFile, boolean errorCheckOnly) {
        ArrayList<ParsingResult> result = new ArrayList<ParsingResult>();
        boolean parseError = false;
        // [ERROR] Check argument count
        if (data_segments.length != 8) {
            Object[] messageArguments = {data_segments.length, 8};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.LOCALE);
            formatter.applyPattern(I18n.DATPARSER_WrongArgumentCount);
            result.add(new ParsingResult(formatter.format(messageArguments), "[E99] " + I18n.DATPARSER_SyntaxError, ResultType.ERROR)); //$NON-NLS-1$
        } else {
            // [ERROR] Check colour
            GColour colour = validateColour(data_segments[1], r, g, b, a);
            if (colour == null) {
                result.add(new ParsingResult(I18n.DATPARSER_InvalidColour, "[E99] " + I18n.DATPARSER_SyntaxError, ResultType.ERROR)); //$NON-NLS-1$
                return result;
            }
            // [ERROR] Check identical vertices
            while (true) {
                try {
                    // Start vertex
                    start.setX(new BigDecimal(data_segments[2], Threshold.mc));
                    start.setY(new BigDecimal(data_segments[3], Threshold.mc));
                    start.setZ(new BigDecimal(data_segments[4], Threshold.mc));
                    // End vertex
                    end.setX(new BigDecimal(data_segments[5], Threshold.mc));
                    end.setY(new BigDecimal(data_segments[6], Threshold.mc));
                    end.setZ(new BigDecimal(data_segments[7], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    result.add(new ParsingResult(I18n.DATPARSER_InvalidNumberFormat, "[E99] " + I18n.DATPARSER_SyntaxError, ResultType.ERROR)); //$NON-NLS-1$
                    break;
                }
                parseError = Vector3d.sub(start, end).length().compareTo(Threshold.identical_vertex_distance) < 0;
                if (parseError) {
                    result.add(new ParsingResult(I18n.DATPARSER_IdenticalVertices, "[E0D] " + I18n.DATPARSER_DataError, ResultType.ERROR)); //$NON-NLS-1$
                }
                if (result.size() < 1 && !errorCheckOnly) {
                    GData2 data = new GData2(colour.getColourNumber(), colour.getR(), colour.getG(), colour.getB(), colour.getA(), start.X, start.Y, start.Z, end.X, end.Y, end.Z, parent, datFile, true);
                    result.add(new ParsingResult(data));
                }
                // [WARNING] Dithered colour
                if (colour.getType() != null && GCType.DITHERED == colour.getType().type()) {
                    result.add(new ParsingResult(I18n.DATPARSER_DitheredColour, "[WDC] " + I18n.DATPARSER_Warning, ResultType.WARN)); //$NON-NLS-1$
                }
                break;
            }
        }
        return result;
    }

    /**
     * Checks triangle lines and highlights syntax errors
     *
     * @param line
     *            the line to check
     * @param data_segments
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
    private static ArrayList<ParsingResult> parse_Triangle(String[] data_segments, float r, float g, float b, float a, GData1 parent, DatFile datFile, boolean errorCheckOnly, int depth) {
        ArrayList<ParsingResult> result = new ArrayList<ParsingResult>();
        // [ERROR] Check argument count
        if (data_segments.length != 11) {
            Object[] messageArguments = {data_segments.length, 11};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.LOCALE);
            formatter.applyPattern(I18n.DATPARSER_WrongArgumentCount);
            result.add(new ParsingResult(formatter.format(messageArguments), "[E99] " + I18n.DATPARSER_SyntaxError, ResultType.ERROR)); //$NON-NLS-1$
        } else {
            // [ERROR] Check colour
            GColour colour = validateColour(data_segments[1], r, g, b, a);
            if (colour == null) {
                result.add(new ParsingResult(I18n.DATPARSER_InvalidColour, "[E99] " + I18n.DATPARSER_SyntaxError, ResultType.ERROR)); //$NON-NLS-1$
                return result;
            }
            // [ERROR] Check identical vertices
            while (true) {
                try {
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
                } catch (NumberFormatException nfe) {
                    result.add(new ParsingResult(I18n.DATPARSER_InvalidNumberFormat, "[E99] " + I18n.DATPARSER_SyntaxError, ResultType.ERROR)); //$NON-NLS-1$
                    break;
                }
                if (!errorCheckOnly) { // result.size() < 1 &&
                    GData3 data = new GData3(colour.getColourNumber(), colour.getR(), colour.getG(), colour.getB(), colour.getA(), vertexA.X, vertexA.Y, vertexA.Z, vertexB.X, vertexB.Y, vertexB.Z,
                            vertexC.X, vertexC.Y, vertexC.Z, parent, datFile, true);
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
                    parseError = angle < Threshold.collinear_angle_minimum || angle > Threshold.collinear_angle_maximum;

                    if (!parseError) {
                        vertexA2.negate();
                        angle = Vector3d.angle(vertexA2, vertexC2);
                        sumAngle = sumAngle + angle;
                        parseError = angle < Threshold.collinear_angle_minimum || angle > Threshold.collinear_angle_maximum;
                    }

                    if (!parseError) {
                        angle = 180.0 - sumAngle;
                        parseError = angle < Threshold.collinear_angle_minimum || angle > Threshold.collinear_angle_maximum;
                    }

                    if (parseError) {
                        result.add(new ParsingResult(I18n.DATPARSER_CollinearVertices, "[E01] " + I18n.DATPARSER_LogicError, ResultType.ERROR)); //$NON-NLS-1$
                    }

                    parseError = vertexA2.length().compareTo(Threshold.identical_vertex_distance) < 0;
                    parseError = parseError || vertexB2.length().compareTo(Threshold.identical_vertex_distance) < 0;
                    parseError = parseError || vertexC2.length().compareTo(Threshold.identical_vertex_distance) < 0;
                    if (parseError) {
                        result.add(new ParsingResult(I18n.DATPARSER_IdenticalVertices, "[E0D] " + I18n.DATPARSER_DataError, ResultType.ERROR)); //$NON-NLS-1$
                    }
                    // [WARNING] Dithered colour
                    if (colour.getType() != null && GCType.DITHERED == colour.getType().type()) {
                        result.add(new ParsingResult(I18n.DATPARSER_DitheredColour, "[WDC] " + I18n.DATPARSER_Warning, ResultType.WARN)); //$NON-NLS-1$
                    }
                }
                break;
            }
        }
        return result;
    }

    /**
     * Checks quad lines and highlights syntax errors
     *
     * @param line
     *            the line to check
     * @param data_segments
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
    private static ArrayList<ParsingResult> parse_Quad(String[] data_segments, float r, float g, float b, float a, GData1 parent, DatFile datFile, boolean errorCheckOnly, int depth) {
        ArrayList<ParsingResult> result = new ArrayList<ParsingResult>();
        // [ERROR] Check argument count
        if (data_segments.length != 14) {
            Object[] messageArguments = {data_segments.length, 14};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.LOCALE);
            formatter.applyPattern(I18n.DATPARSER_WrongArgumentCount);
            result.add(new ParsingResult(formatter.format(messageArguments), "[E99] " + I18n.DATPARSER_SyntaxError, ResultType.ERROR)); //$NON-NLS-1$
        } else {
            // [ERROR] Check colour
            GColour colour = validateColour(data_segments[1], r, g, b, a);
            if (colour == null) {
                result.add(new ParsingResult(I18n.DATPARSER_InvalidColour, "[E99] " + I18n.DATPARSER_SyntaxError, ResultType.ERROR)); //$NON-NLS-1$
                return result;
            }
            // [ERROR] Check hourglass, concave form, coplanarity & identical
            // vertices
            while (true) {
                try {
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
                } catch (NumberFormatException nfe) {
                    result.add(new ParsingResult(I18n.DATPARSER_InvalidNumberFormat, "[E99] " + I18n.DATPARSER_SyntaxError, ResultType.ERROR)); //$NON-NLS-1$
                    break;
                }

                final boolean depthLower1 = depth < 1;

                vertexA2.X = vertexA.X;
                vertexA2.Y = vertexA.Y;
                vertexA2.Z = vertexA.Z;

                vertexB2.X = vertexB.X;
                vertexB2.Y = vertexB.Y;
                vertexB2.Z = vertexB.Z;

                vertexC2.X = vertexC.X;
                vertexC2.Y = vertexC.Y;
                vertexC2.Z = vertexC.Z;

                vertexD2.X = vertexD.X;
                vertexD2.Y = vertexD.Y;
                vertexD2.Z = vertexD.Z;

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
                    GData4 data = new GData4(colour.getColourNumber(), colour.getR(), colour.getG(), colour.getB(), colour.getA(), vertexA2.X, vertexA2.Y, vertexA2.Z, vertexB2.X, vertexB2.Y,
                            vertexB2.Z, vertexC2.X, vertexC2.Y, vertexC2.Z, vertexD2.X, vertexD2.Y, vertexD2.Z, normal, parent, datFile);
                    result.add(new ParsingResult(data));
                }
                if (depthLower1) {
                    boolean parseError = false;
                    boolean parseWarning = false;
                    parseError = cnc > 0 && cnc < 4;
                    if (cnc == 2) {
                        // Hourglass
                        switch (fcc) {
                        case 1:
                            result.add(new ParsingResult(I18n.DATPARSER_HourglassQuadrilateral, "[E41] " + I18n.DATPARSER_DataError, ResultType.ERROR)); //$NON-NLS-1$
                            break;
                        default: // 2
                            result.add(new ParsingResult(I18n.DATPARSER_HourglassQuadrilateral, "[E42] " + I18n.DATPARSER_DataError, ResultType.ERROR)); //$NON-NLS-1$
                            break;
                        }
                    } else if (cnc == 1 || cnc == 3) {
                        // Concave
                        result.add(new ParsingResult(I18n.DATPARSER_ConcaveQuadrilateral, "[E04] " + I18n.DATPARSER_DataError, ResultType.ERROR)); //$NON-NLS-1$
                    }

                    double angle;

                    // Coplanarity
                    if (!parseError) {

                        angle = Math.max(Vector3d.angle(normals[0], normals[2]), Vector3d.angle(normals[1], normals[3]));

                        parseWarning = angle > Threshold.coplanarity_angle_warning;
                        if (angle > Threshold.coplanarity_angle_error) {
                            result.add(new ParsingResult(I18n.DATPARSER_Coplanarity, "[E24] " + I18n.DATPARSER_DataError, ResultType.ERROR)); //$NON-NLS-1$
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
                        parseError = angle < Threshold.collinear_angle_minimum || angle > Threshold.collinear_angle_maximum;

                        if (!parseError) {
                            angle = Vector3d.angle(vertexB2, vertexC2);
                            sumAngle = sumAngle + angle;
                            parseError = angle < Threshold.collinear_angle_minimum || angle > Threshold.collinear_angle_maximum;
                        }

                        if (!parseError) {
                            vertexA2.negate();
                            vertexB2.negate();
                            angle = Vector3d.angle(vertexA2, vertexB2);
                            sumAngle = sumAngle + angle;
                            parseError = angle < Threshold.collinear_angle_minimum || angle > Threshold.collinear_angle_maximum;

                        }

                        if (!parseError) {
                            angle = 360.0 - sumAngle;
                            parseError = angle < Threshold.collinear_angle_minimum || angle > Threshold.collinear_angle_maximum;
                        }

                        if (parseError) {
                            result.add(new ParsingResult(I18n.DATPARSER_CollinearVertices, "[E34] " + I18n.DATPARSER_DataError, ResultType.ERROR)); //$NON-NLS-1$
                        }
                    }

                    parseError = vertexA2.length().compareTo(Threshold.identical_vertex_distance) < 0;
                    parseError = parseError || vertexB2.length().compareTo(Threshold.identical_vertex_distance) < 0;
                    parseError = parseError || vertexC2.length().compareTo(Threshold.identical_vertex_distance) < 0;
                    parseError = parseError || vertexD2.length().compareTo(Threshold.identical_vertex_distance) < 0;
                    if (parseError) {
                        if (!errorCheckOnly) {
                            ParsingResult p1 = result.get(0);
                            result.clear();
                            result.add(p1);
                        } else {
                            result.clear();
                        }
                        result.add(new ParsingResult(I18n.DATPARSER_IdenticalVertices, "[E44] " + I18n.DATPARSER_DataError, ResultType.ERROR)); //$NON-NLS-1$

                    } else if (parseWarning) {
                        result.add(new ParsingResult(I18n.DATPARSER_NearCoplanarity, "[W24] " + I18n.DATPARSER_Warning, ResultType.WARN)); //$NON-NLS-1$
                    }
                    // [WARNING] Dithered colour
                    if (colour.getType() != null && GCType.DITHERED == colour.getType().type()) {
                        result.add(new ParsingResult(I18n.DATPARSER_DitheredColour, "[WDC] " + I18n.DATPARSER_Warning, ResultType.WARN)); //$NON-NLS-1$
                    }
                }
                break;
            }
        }
        return result;
    }

    /**
     * Checks conditional lines and highlights syntax errors
     *
     * @param line
     *            the line to check
     * @param data_segments
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
    private static ArrayList<ParsingResult> parse_Condline(String[] data_segments, float r, float g, float b, float a, GData1 parent, DatFile datFile, boolean errorCheckOnly, int depth) {
        ArrayList<ParsingResult> result = new ArrayList<ParsingResult>();
        // [ERROR] Check argument count
        if (data_segments.length != 14) {
            Object[] messageArguments = {data_segments.length, 14};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.LOCALE);
            formatter.applyPattern(I18n.DATPARSER_WrongArgumentCount);
            result.add(new ParsingResult(formatter.format(messageArguments), "[E99] " + I18n.DATPARSER_SyntaxError, ResultType.ERROR)); //$NON-NLS-1$
        } else {
            // [ERROR] Check colour
            GColour colour = validateColour(data_segments[1], r, g, b, a);
            if (colour == null) {
                result.add(new ParsingResult(I18n.DATPARSER_InvalidColour, "[E99] " + I18n.DATPARSER_SyntaxError, ResultType.ERROR)); //$NON-NLS-1$
                return result;
            }
            // [ERROR] Check identical vertices
            while (true) {
                try {
                    // start vertex
                    start.setX(new BigDecimal(data_segments[2], Threshold.mc));
                    start.setY(new BigDecimal(data_segments[3], Threshold.mc));
                    start.setZ(new BigDecimal(data_segments[4], Threshold.mc));
                    // end vertex
                    end.setX(new BigDecimal(data_segments[5], Threshold.mc));
                    end.setY(new BigDecimal(data_segments[6], Threshold.mc));
                    end.setZ(new BigDecimal(data_segments[7], Threshold.mc));
                    // control vertex I
                    controlI.setX(new BigDecimal(data_segments[8], Threshold.mc));
                    controlI.setY(new BigDecimal(data_segments[9], Threshold.mc));
                    controlI.setZ(new BigDecimal(data_segments[10], Threshold.mc));
                    // control vertex II
                    controlII.setX(new BigDecimal(data_segments[11], Threshold.mc));
                    controlII.setY(new BigDecimal(data_segments[12], Threshold.mc));
                    controlII.setZ(new BigDecimal(data_segments[13], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    result.add(new ParsingResult(I18n.DATPARSER_InvalidNumberFormat, "[E99] " + I18n.DATPARSER_SyntaxError, ResultType.ERROR)); //$NON-NLS-1$
                    break;
                }
                if (Vector3d.sub(start, end).length().compareTo(Threshold.identical_vertex_distance) < 0) {
                    result.add(new ParsingResult(I18n.DATPARSER_IdenticalVertices, "[E0D] " + I18n.DATPARSER_DataError, ResultType.ERROR)); //$NON-NLS-1$
                } else if (depth < 1 && Vector3d.sub(controlI, controlII).length().compareTo(Threshold.identical_vertex_distance) < 0) {
                    result.add(new ParsingResult(I18n.DATPARSER_IdenticalControlPoints, "[E05] " + I18n.DATPARSER_DataError, ResultType.ERROR)); //$NON-NLS-1$
                }
                if (result.size() < 1 && !errorCheckOnly) {
                    GData5 data = new GData5(colour.getColourNumber(), colour.getR(), colour.getG(), colour.getB(), colour.getA(), start.X, start.Y, start.Z, end.X, end.Y, end.Z, controlI.X,
                            controlI.Y, controlI.Z, controlII.X, controlII.Y, controlII.Z, parent, datFile);
                    result.add(new ParsingResult(data));
                }
                if (depth < 1 && colour.getType() != null && GCType.DITHERED == colour.getType().type()) {
                    result.add(new ParsingResult(I18n.DATPARSER_DitheredColour, "[WDC] " + I18n.DATPARSER_Warning, ResultType.WARN)); //$NON-NLS-1$
                }
                break;
            }
        }
        return result;
    }

    public static boolean isUpatePngImages() {
        return upatePngImages;
    }

    public static void setUpatePngImages(boolean upatePngImages) {
        DatParser.upatePngImages = upatePngImages;
    }

}
