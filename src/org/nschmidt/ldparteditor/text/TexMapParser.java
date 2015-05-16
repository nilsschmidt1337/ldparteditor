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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.data.BFC;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.GData2;
import org.nschmidt.ldparteditor.data.GData3;
import org.nschmidt.ldparteditor.data.GData4;
import org.nschmidt.ldparteditor.data.GData5;
import org.nschmidt.ldparteditor.data.GDataBFC;
import org.nschmidt.ldparteditor.data.GDataCSG;
import org.nschmidt.ldparteditor.data.GDataTEX;
import org.nschmidt.ldparteditor.data.GTexture;
import org.nschmidt.ldparteditor.data.TexMeta;
import org.nschmidt.ldparteditor.data.TexType;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * @author nils
 *
 */
public enum TexMapParser {
    INSTANCE;

    private static final Pattern WHITESPACE = Pattern.compile("\\s+"); //$NON-NLS-1$

    public static GDataTEX parseGeometry(String line, int depth, float r, float g, float b, float a, GData1 gData1, Matrix4f pMatrix, Set<String> alreadyParsed) {
        String tline = line.replaceAll("0\\s+\\Q!:\\E\\s+", ""); //$NON-NLS-1$ //$NON-NLS-2$
        GData data = parseLine(tline, depth, r, g, b, a, gData1, pMatrix, alreadyParsed);
        return new GDataTEX(data, line, TexMeta.GEOMETRY, null);
    }

    public static GDataTEX parseTEXMAP(String[] data_segments, String line, GData1 parent) {
        int segs = data_segments.length;
        if (segs == 3) {
            if (data_segments[2].equals("END")) { //$NON-NLS-1$
                return new GDataTEX(null, line, TexMeta.END, null);
            } else if (data_segments[2].equals("FALLBACK")) { //$NON-NLS-1$
                return new GDataTEX(null, line, TexMeta.FALLBACK, null);
            }
        } else if (segs > 3) {
            Vector4f v1 = new Vector4f();
            Vector4f v2 = new Vector4f();
            Vector4f v3 = new Vector4f();
            float a = 0f;
            float b = 0f;
            TexMeta meta = data_segments[2].equals("START") ? TexMeta.START : data_segments[2].equals("NEXT") ? TexMeta.NEXT : null; //$NON-NLS-1$ //$NON-NLS-2$
            if (meta != null && data_segments[3].equals("PLANAR") || data_segments[3].equals("CYLINDRICAL") || data_segments[3].equals("SPHERICAL")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                if (segs > 13) {
                    String texture = ""; //$NON-NLS-1$
                    String glossmap = null;
                    TexType type;
                    try {
                        v1.setX(Float.parseFloat(data_segments[4]) * 1000f);
                        v1.setY(Float.parseFloat(data_segments[5]) * 1000f);
                        v1.setZ(Float.parseFloat(data_segments[6]) * 1000f);
                        v1.setW(1f);
                        v2.setX(Float.parseFloat(data_segments[7]) * 1000f);
                        v2.setY(Float.parseFloat(data_segments[8]) * 1000f);
                        v2.setZ(Float.parseFloat(data_segments[9]) * 1000f);
                        v2.setW(1f);
                        v3.setX(Float.parseFloat(data_segments[10]) * 1000f);
                        v3.setY(Float.parseFloat(data_segments[11]) * 1000f);
                        v3.setZ(Float.parseFloat(data_segments[12]) * 1000f);
                        v3.setW(1f);
                    } catch (Exception e) {
                        return null;
                    }

                    Matrix4f.transform(parent.getProductMatrix(), v1, v1);
                    Matrix4f.transform(parent.getProductMatrix(), v2, v2);
                    Matrix4f.transform(parent.getProductMatrix(), v3, v3);

                    StringBuilder tex = new StringBuilder();
                    StringBuilder gloss = new StringBuilder();
                    final int len = line.length();
                    final int target;
                    boolean whitespace1 = false;
                    boolean whitespace2 = false;
                    if (data_segments[3].equals("PLANAR")) { //$NON-NLS-1$
                        type = TexType.PLANAR;
                        target = 14;
                    } else if (data_segments[3].equals("CYLINDRICAL")) { //$NON-NLS-1$
                        if (segs > 14) {
                            try {
                                a = (float) (Float.parseFloat(data_segments[13]) / 360f * Math.PI);
                                if (a <= 0f || a > Math.PI * 2f)
                                    return null;
                            } catch (Exception e) {
                                return null;
                            }
                        } else {
                            return null;
                        }
                        type = TexType.CYLINDRICAL;
                        target = 15;
                    } else {
                        if (segs > 15) {
                            try {
                                a = (float) (Float.parseFloat(data_segments[13]) / 360f * Math.PI);
                                if (a <= 0f || a > Math.PI * 2f)
                                    return null;
                                b = (float) (Float.parseFloat(data_segments[14]) / 360f * Math.PI);
                                if (b <= 0f || b > Math.PI * 2f)
                                    return null;
                            } catch (Exception e) {
                                return null;
                            }
                        } else {
                            return null;
                        }
                        type = TexType.SPHERICAL;
                        target = 16;
                    }

                    int mode = 0;
                    int counter = 0;
                    for (int i = 0; i < len; i++) {
                        String sub = line.substring(i, i + 1);
                        switch (mode) {
                        case 0: // Parse initial whitespace
                            if (sub.matches("\\S")) { //$NON-NLS-1$
                                mode = 1;
                            }
                        case 1: // Parse non-whitespace (first letter)
                            if (sub.matches("\\S")) { //$NON-NLS-1$
                                counter++;
                                if (counter == target) {
                                    if (sub.equals("\"")) { //$NON-NLS-1$
                                        mode = 4;
                                        sub = ""; //$NON-NLS-1$
                                    } else {
                                        mode = 3;
                                    }
                                } else {
                                    mode = 2;
                                }
                            }
                            break;
                        case 2: // Parse non-whitespace (rest)
                            if (sub.matches("\\s")) { //$NON-NLS-1$
                                mode = 1;
                            }
                            break;
                        }
                        switch (mode) {
                        case 3: // Parse texture till whitespace
                            if (sub.matches("\\S")) { //$NON-NLS-1$
                                tex.append(sub);
                            } else {
                                whitespace1 = true;
                                mode = 5;
                            }
                            break;
                        case 4: // Parse texture till "
                            if (sub.equals("\"")) { //$NON-NLS-1$
                                mode = 5;
                            } else {
                                tex.append(sub);
                            }
                            break;
                        case 5: // Additional whitespace
                            if (sub.matches("\\S")) { //$NON-NLS-1$
                                if (whitespace1)
                                    mode = 6;
                            } else {
                                whitespace1 = true;
                                break;
                            }
                        case 6: // GLOSSMAP
                            if (sub.equals("G"))mode = 7; //$NON-NLS-1$
                            break;
                        case 7: // GLOSSMAP
                            if (sub.equals("L"))mode = 8; //$NON-NLS-1$
                            break;
                        case 8: // GLOSSMAP
                            if (sub.equals("O"))mode = 9; //$NON-NLS-1$
                            break;
                        case 9: // GLOSSMAP
                            if (sub.equals("S"))mode = 10; //$NON-NLS-1$
                            break;
                        case 10: // GLOSSMAP
                            if (sub.equals("S"))mode = 11; //$NON-NLS-1$
                            break;
                        case 11: // GLOSSMAP
                            if (sub.equals("M"))mode = 12; //$NON-NLS-1$
                            break;
                        case 12: // GLOSSMAP
                            if (sub.equals("A"))mode = 13; //$NON-NLS-1$
                            break;
                        case 13: // GLOSSMAP
                            if (sub.equals("P"))mode = 14; //$NON-NLS-1$
                            break;
                        case 14: // Additional whitespace between GLOSSMAP and
                            // glossmap-path
                            if (sub.matches("\\S")) { //$NON-NLS-1$
                                if (whitespace2)
                                    mode = 15;
                            } else {
                                whitespace2 = true;
                            }
                            break;
                        }
                        switch (mode) {
                        case 15:
                            if (sub.matches("\\S")) { //$NON-NLS-1$
                                if (sub.equals("\"")) { //$NON-NLS-1$
                                    mode = 17;
                                    sub = ""; //$NON-NLS-1$
                                } else {
                                    mode = 16;
                                }
                            }
                            break;
                        }
                        switch (mode) {
                        case 16: // Parse texture till whitespace
                            if (sub.matches("\\S")) { //$NON-NLS-1$
                                gloss.append(sub);
                            } else {
                                mode = 18;
                            }
                            break;
                        case 17: // Parse texture till "
                            if (sub.equals("\"")) { //$NON-NLS-1$
                                mode = 18;
                            } else {
                                gloss.append(sub);
                            }
                            break;
                        }
                    }

                    texture = tex.toString();
                    glossmap = gloss.toString();

                    NLogger.debug(TexMapParser.class, texture);
                    NLogger.debug(TexMapParser.class, glossmap);

                    if (glossmap.isEmpty())
                        glossmap = null;

                    return new GDataTEX(null, line, meta, new GTexture(type, texture, glossmap, 1, new Vector3f(v1.x, v1.y, v1.z), new Vector3f(v2.x, v2.y, v2.z), new Vector3f(v3.x, v3.y, v3.z), a, b));
                }
            }
        }
        return null;
    }

    public static GData parseLine(String line, int depth, float r, float g, float b, float a, GData1 gData1, Matrix4f pMatrix, Set<String> alreadyParsed) {
        final String[] data_segments = WHITESPACE.split(line.trim());
        return parseLine(data_segments, line, depth, r, g, b, a, gData1, pMatrix, alreadyParsed);
    }

    // What follows now is a very minimalistic DAT file parser (<500LOC)

    private static GColour cValue = new GColour();
    private static final Vector3f start = new Vector3f();
    private static final Vector3f end = new Vector3f();
    private static final Vector3f vertexA = new Vector3f();
    private static final Vector3f vertexB = new Vector3f();
    private static final Vector3f vertexC = new Vector3f();
    private static final Vector3f vertexD = new Vector3f();
    private static final Vector3f controlI = new Vector3f();
    private static final Vector3f controlII = new Vector3f();

    public static GData parseLine(String[] data_segments, String line, int depth, float r, float g, float b, float a, GData1 parent, Matrix4f productMatrix, Set<String> alreadyParsed) {
        // Get the linetype
        int linetype = 0;
        char c;
        if (!(data_segments.length > 0 && data_segments[0].length() == 1 && Character.isDigit(c = data_segments[0].charAt(0)))) {
            return null;
        }
        linetype = Character.getNumericValue(c);
        // Parse the line according to its type
        switch (linetype) {
        case 0:
            return parse_Comment(line, data_segments, depth, r, g, b, a, parent, productMatrix, alreadyParsed);
        case 1:
            return parse_Reference(data_segments, depth, r, g, b, a, parent, productMatrix, alreadyParsed);
        case 2:
            return parse_Line(data_segments, r, g, b, a, parent);
        case 3:
            return parse_Triangle(data_segments, r, g, b, a, parent);
        case 4:
            return parse_Quad(data_segments, r, g, b, a, parent);
        case 5:
            return parse_Condline(data_segments, r, g, b, a, parent);
        }
        return null;
    }

    private static GColour validateColour(String arg, float r, float g, float b, float a) {
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
            } else {
                return null;
            }
        }
        return cValue;
    }

    private static GData parse_Comment(String line, String[] data_segments, int depth, float r, float g, float b, float a, GData1 parent, Matrix4f productMatrix, Set<String> alreadyParsed) {
        line = WHITESPACE.matcher(line).replaceAll(" ").trim(); //$NON-NLS-1$
        if (line.startsWith("0 !: ")) { //$NON-NLS-1$
            GData newLPEmetaTag = TexMapParser.parseGeometry(line, depth, r, g, b, a, parent, productMatrix, alreadyParsed);
            return newLPEmetaTag;
        } else if (line.startsWith("0 !TEXMAP ")) { //$NON-NLS-1$
            GData newLPEmetaTag = TexMapParser.parseTEXMAP(data_segments, line, parent);
            return newLPEmetaTag;
        } else if (line.startsWith("0 BFC ")) { //$NON-NLS-1$
            if (line.startsWith("INVERTNEXT", 6)) { //$NON-NLS-1$
                return new GDataBFC(BFC.INVERTNEXT);
            } else if (line.startsWith("CERTIFY CCW", 6)) { //$NON-NLS-1$
                return new GDataBFC(BFC.CCW_CLIP);
            } else if (line.startsWith("CERTIFY CW", 6)) { //$NON-NLS-1$
                return new GDataBFC(BFC.CW_CLIP);
            } else if (line.startsWith("CERTIFY", 6)) { //$NON-NLS-1$
                return new GDataBFC(BFC.CCW_CLIP);
            } else if (line.startsWith("NOCERTIFY", 6)) { //$NON-NLS-1$
                return new GDataBFC(BFC.NOCERTIFY);
            } else if (line.startsWith("CCW", 6)) { //$NON-NLS-1$
                return new GDataBFC(BFC.CCW);
            } else if (line.startsWith("CW", 6)) { //$NON-NLS-1$
                return new GDataBFC(BFC.CW);
            } else if (line.startsWith("NOCLIP", 6)) { //$NON-NLS-1$
                return new GDataBFC(BFC.NOCLIP);
            } else if (line.startsWith("CLIP CCW", 6)) { //$NON-NLS-1$
                return new GDataBFC(BFC.CCW_CLIP);
            } else if (line.startsWith("CLIP CW", 6)) { //$NON-NLS-1$
                return new GDataBFC(BFC.CW_CLIP);
            } else if (line.startsWith("CCW CLIP", 6)) { //$NON-NLS-1$
                return new GDataBFC(BFC.CCW_CLIP);
            } else if (line.startsWith("CW CLIP", 6)) { //$NON-NLS-1$
                return new GDataBFC(BFC.CW_CLIP);
            } else if (line.startsWith("CLIP", 6)) { //$NON-NLS-1$
                return new GDataBFC(BFC.CLIP);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private static GData parse_Reference(String[] data_segments, int depth, float r, float g, float b, float a, GData1 parent, Matrix4f productMatrix, Set<String> alreadyParsed) {
        if (data_segments.length < 15) {
            return null;
        } else {
            GColour colour = validateColour(data_segments[1], r, g, b, a);
            if (colour == null)
                return null;
            Matrix4f tMatrix = new Matrix4f();
            float det = 0;
            try {
                tMatrix.m30 = Float.parseFloat(data_segments[2]) * 1000f;
                tMatrix.m31 = Float.parseFloat(data_segments[3]) * 1000f;
                tMatrix.m32 = Float.parseFloat(data_segments[4]) * 1000f;
                tMatrix.m00 = Float.parseFloat(data_segments[5]);
                tMatrix.m10 = Float.parseFloat(data_segments[6]);
                tMatrix.m20 = Float.parseFloat(data_segments[7]);
                tMatrix.m01 = Float.parseFloat(data_segments[8]);
                tMatrix.m11 = Float.parseFloat(data_segments[9]);
                tMatrix.m21 = Float.parseFloat(data_segments[10]);
                tMatrix.m02 = Float.parseFloat(data_segments[11]);
                tMatrix.m12 = Float.parseFloat(data_segments[12]);
                tMatrix.m22 = Float.parseFloat(data_segments[13]);
            } catch (NumberFormatException nfe) {
                return null;
            }
            tMatrix.m33 = 1f;
            det = tMatrix.determinant();
            if (Math.abs(det) < Threshold.singularity_determinant)
                return null;
            // [WARNING] Check file existance
            boolean fileExists = true;
            StringBuilder sb = new StringBuilder();
            for (int s = 14; s < data_segments.length - 1; s++) {
                sb.append(data_segments[s]);
                sb.append(" "); //$NON-NLS-1$
            }
            sb.append(data_segments[data_segments.length - 1]);
            String shortFilename = sb.toString();
            shortFilename = shortFilename.toLowerCase(Locale.ENGLISH);
            try {
                shortFilename = shortFilename.replaceAll("s\\\\", "S" + File.separator).replaceAll("\\\\", File.separator); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            } catch (Exception e) {
                // Workaround for windows OS / JVM BUG
                shortFilename = shortFilename.replace("s\\\\", "S" + File.separator).replace("\\\\", File.separator); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            if (alreadyParsed.contains(shortFilename)) {
                if (!View.DUMMY_REFERENCE.equals(parent))
                    parent.firstRef.setRecursive(true);
                return null;
            } else {
                alreadyParsed.add(shortFilename);
            }
            String shortFilename2 = shortFilename.startsWith("S" + File.separator) ? "s" + shortFilename.substring(1) : shortFilename; //$NON-NLS-1$ //$NON-NLS-2$
            File fileToOpen = null;
            String[] prefix = new String[]{WorkbenchManager.getUserSettingState().getLdrawFolderPath(), WorkbenchManager.getUserSettingState().getUnofficialFolderPath(), Project.getProjectPath()};
            String[] middle = new String[]{File.separator + "PARTS", File.separator + "parts", File.separator + "P", File.separator + "p"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            String[] suffix = new String[]{File.separator + shortFilename, File.separator + shortFilename2};
            for (int a1 = 0; a1 < prefix.length; a1++) {
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
                Matrix4f destMatrix = new Matrix4f();
                Matrix4f.mul(productMatrix, tMatrix, destMatrix);
                GDataCSG.forceRecompile();
                final GData1 result = new GData1(colour.getColourNumber(), colour.getR(), colour.getG(), colour.getB(), colour.getA(), tMatrix, lines, absoluteFilename, sb.toString(), depth, det < 0,
                        destMatrix, parent.firstRef, alreadyParsed, parent);
                if (result != null && result.firstRef.isRecursive()) {
                    return null;
                }
                alreadyParsed.remove(shortFilename);
                return result;
            } else if (!fileExists) {
                return null;
            } else {
                absoluteFilename = fileToOpen.getAbsolutePath();
                UTF8BufferedReader reader;
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
                    reader.close();
                } catch (FileNotFoundException e1) {
                    return null;
                } catch (LDParsingException e1) {
                    return null;
                } catch (UnsupportedEncodingException e1) {
                    return null;
                }
                Matrix4f destMatrix = new Matrix4f();
                Matrix4f.mul(productMatrix, tMatrix, destMatrix);
                GDataCSG.forceRecompile();
                final GData1 result = new GData1(colour.getColourNumber(), colour.getR(), colour.getG(), colour.getB(), colour.getA(), tMatrix, lines, absoluteFilename, sb.toString(), depth, det < 0,
                        destMatrix, parent.firstRef, alreadyParsed, parent);
                alreadyParsed.remove(shortFilename);
                if (result != null && result.firstRef.isRecursive()) {
                    return null;
                }
                return result;
            }
        }
    }

    private static GData parse_Line(String[] data_segments, float r, float g, float b, float a, GData1 parent) {
        if (data_segments.length != 8) {
            return null;
        } else {
            GColour colour = validateColour(data_segments[1], r, g, b, a);
            if (colour == null)
                return null;
            try {
                start.setX(Float.parseFloat(data_segments[2]));
                start.setY(Float.parseFloat(data_segments[3]));
                start.setZ(Float.parseFloat(data_segments[4]));
                end.setX(Float.parseFloat(data_segments[5]));
                end.setY(Float.parseFloat(data_segments[6]));
                end.setZ(Float.parseFloat(data_segments[7]));
            } catch (NumberFormatException nfe) {
                return null;
            }
            if (Vector3f.sub(start, end, null).length() < Threshold.identical_vertex_distance.floatValue()) {
                return null;
            }
            return new GData2(new Vertex(start.x * 1000f, start.y * 1000f, start.z * 1000f, false), new Vertex(end.x * 1000f, end.y * 1000f, end.z * 1000f, false), colour, parent);
        }
    }

    private static GData parse_Triangle(String[] data_segments, float r, float g, float b, float a, GData1 parent) {
        if (data_segments.length != 11) {
            return null;
        } else {
            GColour colour = validateColour(data_segments[1], r, g, b, a);
            if (colour == null)
                return null;
            try {
                vertexA.setX(Float.parseFloat(data_segments[2]));
                vertexA.setY(Float.parseFloat(data_segments[3]));
                vertexA.setZ(Float.parseFloat(data_segments[4]));
                vertexB.setX(Float.parseFloat(data_segments[5]));
                vertexB.setY(Float.parseFloat(data_segments[6]));
                vertexB.setZ(Float.parseFloat(data_segments[7]));
                vertexC.setX(Float.parseFloat(data_segments[8]));
                vertexC.setY(Float.parseFloat(data_segments[9]));
            } catch (NumberFormatException nfe) {
                return null;
            }
            try {
                vertexC.setZ(Float.parseFloat(data_segments[10]));
            } catch (NumberFormatException nfe) {
                return null;
            }
            return new GData3(new Vertex(vertexA.x * 1000f, vertexA.y * 1000f, vertexA.z * 1000f, false), new Vertex(vertexB.x * 1000f, vertexB.y * 1000f, vertexB.z * 1000f, false), new Vertex(vertexC.x * 1000f,
                    vertexC.y * 1000f, vertexC.z * 1000f, false), parent, colour);
        }
    }

    private static GData parse_Quad(String[] data_segments, float r, float g, float b, float a, GData1 parent) {
        if (data_segments.length != 14) {
            return null;
        } else {
            GColour colour = validateColour(data_segments[1], r, g, b, a);
            if (colour == null)
                return null;
            try {
                vertexA.setX(Float.parseFloat(data_segments[2]));
                vertexA.setY(Float.parseFloat(data_segments[3]));
                vertexA.setZ(Float.parseFloat(data_segments[4]));
                vertexB.setX(Float.parseFloat(data_segments[5]));
                vertexB.setY(Float.parseFloat(data_segments[6]));
                vertexB.setZ(Float.parseFloat(data_segments[7]));
                vertexC.setX(Float.parseFloat(data_segments[8]));
                vertexC.setY(Float.parseFloat(data_segments[9]));
                vertexC.setZ(Float.parseFloat(data_segments[10]));
                vertexD.setX(Float.parseFloat(data_segments[11]));
                vertexD.setY(Float.parseFloat(data_segments[12]));
                vertexD.setZ(Float.parseFloat(data_segments[13]));
            } catch (NumberFormatException nfe) {
                return null;
            }
            return new GData4(new Vertex(vertexA.x * 1000f, vertexA.y * 1000f, vertexA.z * 1000f, false), new Vertex(vertexB.x * 1000f, vertexB.y * 1000f, vertexB.z * 1000f, false), new Vertex(vertexC.x * 1000f,
                    vertexC.y * 1000f, vertexC.z * 1000f, false), new Vertex(vertexD.x * 1000f, vertexD.y * 1000f, vertexD.z * 1000f, false), parent, colour);
        }
    }

    private static GData parse_Condline(String[] data_segments, float r, float g, float b, float a, GData1 parent) {
        if (data_segments.length != 14) {
            return null;
        } else {
            GColour colour = validateColour(data_segments[1], r, g, b, a);
            if (colour == null)
                return null;
            try {
                start.setX(Float.parseFloat(data_segments[2]));
                start.setY(Float.parseFloat(data_segments[3]));
                start.setZ(Float.parseFloat(data_segments[4]));
                end.setX(Float.parseFloat(data_segments[5]));
                end.setY(Float.parseFloat(data_segments[6]));
                end.setZ(Float.parseFloat(data_segments[7]));
                controlI.setX(Float.parseFloat(data_segments[8]));
                controlI.setY(Float.parseFloat(data_segments[9]));
                controlI.setZ(Float.parseFloat(data_segments[10]));
                controlII.setX(Float.parseFloat(data_segments[11]));
                controlII.setY(Float.parseFloat(data_segments[12]));
                controlII.setZ(Float.parseFloat(data_segments[13]));
            } catch (NumberFormatException nfe) {
                return null;
            }
            final float epsilon = Threshold.identical_vertex_distance.floatValue();
            if (Vector3f.sub(start, end, null).length() < epsilon || Vector3f.sub(controlI, controlII, null).length() < epsilon) {
                return null;
            }
            return new GData5(new Vertex(start.x * 1000f, start.y * 1000f, start.z * 1000f, false), new Vertex(end.x * 1000f, end.y * 1000f, end.z * 1000f, false), new Vertex(controlI.x * 1000f,
                    controlI.y * 1000f, controlI.z * 1000f, false), new Vertex(controlII.x * 1000f, controlII.y * 1000f, controlII.z * 1000f, false), colour, parent);
        }
    }
}
