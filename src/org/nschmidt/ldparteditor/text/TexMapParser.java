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
import org.nschmidt.ldparteditor.data.colour.GCDithered;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * @author nils
 *
 */
public enum TexMapParser {
    INSTANCE;

    private static final Pattern WHITESPACE = Pattern.compile("\\s+"); //$NON-NLS-1$

    static GDataTEX parseGeometry(String line, int depth, float r, float g, float b, float a, GData1 parent, Matrix4f pMatrix, Set<String> alreadyParsed, DatFile datFile) {
        String tline = line.replaceAll("0\\s+\\Q!:\\E\\s+", ""); //$NON-NLS-1$ //$NON-NLS-2$
        GData data = parseLine(tline, depth, r, g, b, a, parent, pMatrix, alreadyParsed, datFile);
        return new GDataTEX(data, line, TexMeta.GEOMETRY, null, parent);
    }

    static GDataTEX parseTEXMAP(String[] dataSegments, String line, GData1 parent) {
        int segs = dataSegments.length;
        if (segs == 3) {
            if (dataSegments[2].equals("END")) { //$NON-NLS-1$
                return new GDataTEX(null, line, TexMeta.END, null, parent);
            } else if (dataSegments[2].equals("FALLBACK")) { //$NON-NLS-1$
                return new GDataTEX(null, line, TexMeta.FALLBACK, null, parent);
            }
        } else if (segs > 3) {
            Vector4f v1 = new Vector4f();
            Vector4f v2 = new Vector4f();
            Vector4f v3 = new Vector4f();
            float a = 0f;
            float b = 0f;
            TexMeta meta = dataSegments[2].equals("START") ? TexMeta.START : dataSegments[2].equals("NEXT") ? TexMeta.NEXT : null; //$NON-NLS-1$ //$NON-NLS-2$
            if (meta != null && dataSegments[3].equals("PLANAR") || dataSegments[3].equals("CYLINDRICAL") || dataSegments[3].equals("SPHERICAL")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                if (segs > 13) {
                    String texture = ""; //$NON-NLS-1$
                    String glossmap = null;
                    TexType type;
                    try {
                        v1.setX(Float.parseFloat(dataSegments[4]) * 1000f);
                        v1.setY(Float.parseFloat(dataSegments[5]) * 1000f);
                        v1.setZ(Float.parseFloat(dataSegments[6]) * 1000f);
                        v1.setW(1f);
                        v2.setX(Float.parseFloat(dataSegments[7]) * 1000f);
                        v2.setY(Float.parseFloat(dataSegments[8]) * 1000f);
                        v2.setZ(Float.parseFloat(dataSegments[9]) * 1000f);
                        v2.setW(1f);
                        v3.setX(Float.parseFloat(dataSegments[10]) * 1000f);
                        v3.setY(Float.parseFloat(dataSegments[11]) * 1000f);
                        v3.setZ(Float.parseFloat(dataSegments[12]) * 1000f);
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
                    if (dataSegments[3].equals("PLANAR")) { //$NON-NLS-1$
                        type = TexType.PLANAR;
                        target = 14;
                    } else if (dataSegments[3].equals("CYLINDRICAL")) { //$NON-NLS-1$
                        if (segs > 14) {
                            try {
                                a = (float) (Float.parseFloat(dataSegments[13]) / 360f * Math.PI);
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
                                a = (float) (Float.parseFloat(dataSegments[13]) / 360f * Math.PI);
                                if (a <= 0f || a > Math.PI * 2f)
                                    return null;
                                b = (float) (Float.parseFloat(dataSegments[14]) / 360f * Math.PI);
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
                        default:
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
                        default:
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
                        default:
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
                        default:
                            break;
                        }
                    }

                    texture = tex.toString();
                    glossmap = gloss.toString();

                    if (glossmap.isEmpty())
                        glossmap = null;

                    return new GDataTEX(null, line, meta, new GTexture(type, texture, glossmap, 1, new Vector3f(v1.x, v1.y, v1.z), new Vector3f(v2.x, v2.y, v2.z), new Vector3f(v3.x, v3.y, v3.z), a, b), parent);
                }
            }
        }
        return null;
    }

    public static GData parseLine(String line, int depth, float r, float g, float b, float a, GData1 parent, Matrix4f pMatrix, Set<String> alreadyParsed, DatFile df) {
        final String[] dataSegments = WHITESPACE.split(line.trim());
        return parseLine(dataSegments, line, depth, r, g, b, a, parent, pMatrix, alreadyParsed, df);
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

    public static GData parseLine(String[] dataSegments, String line, int depth, float r, float g, float b, float a, GData1 parent, Matrix4f productMatrix, Set<String> alreadyParsed, DatFile datFile) {
        // Get the linetype
        int linetype = 0;
        char c;
        if (!(dataSegments.length > 0 && dataSegments[0].length() == 1 && Character.isDigit(c = dataSegments[0].charAt(0)))) {
            return null;
        }
        linetype = Character.getNumericValue(c);
        // Parse the line according to its type
        switch (linetype) {
        case 0:
            return parseComment(line, dataSegments, depth, r, g, b, a, parent, productMatrix, alreadyParsed, datFile);
        case 1:
            return parseReference(dataSegments, depth, r, g, b, a, parent, productMatrix, alreadyParsed, datFile);
        case 2:
            return parseLine(dataSegments, r, g, b, a, parent);
        case 3:
            return parseTriangle(dataSegments, r, g, b, a, parent);
        case 4:
            return parseQuad(dataSegments, r, g, b, a, parent);
        case 5:
            return parseCondline(dataSegments, r, g, b, a, parent);
        default:
            break;
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
                cValue.set(24, View.LINE_COLOUR_R[0], View.LINE_COLOUR_G[0], View.LINE_COLOUR_B[0], 1f);
                break;
            default:
                if (View.hasLDConfigColour(colourValue)) {
                    GColour colour = View.getLDConfigColour(colourValue);
                    cValue.set(colour);
                } else {
                    int indexA = colourValue - 256 >> 4;
                    int indexB = colourValue - 256 & 0x0F;
                    if (View.hasLDConfigColour(indexA) && View.hasLDConfigColour(indexB)) {
                        GColour colourA = View.getLDConfigColour(indexA);
                        GColour colourB = View.getLDConfigColour(indexB);
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

    private static GData parseComment(String line, String[] dataSegments, int depth, float r, float g, float b, float a, GData1 parent, Matrix4f productMatrix, Set<String> alreadyParsed, DatFile datFile) {
        line = WHITESPACE.matcher(line).replaceAll(" ").trim(); //$NON-NLS-1$
        if (line.startsWith("0 !: ")) { //$NON-NLS-1$
            return TexMapParser.parseGeometry(line, depth, r, g, b, a, parent, productMatrix, alreadyParsed, datFile);
        } else if (line.startsWith("0 !TEXMAP ")) { //$NON-NLS-1$
            return TexMapParser.parseTEXMAP(dataSegments, line, parent);
        } else if (line.startsWith("0 BFC ")) { //$NON-NLS-1$
            if (line.startsWith("INVERTNEXT", 6)) { //$NON-NLS-1$
                return new GDataBFC(BFC.INVERTNEXT, parent);
            } else if (line.startsWith("CERTIFY CCW", 6)) { //$NON-NLS-1$
                return new GDataBFC(BFC.CCW_CLIP, parent);
            } else if (line.startsWith("CERTIFY CW", 6)) { //$NON-NLS-1$
                return new GDataBFC(BFC.CW_CLIP, parent);
            } else if (line.startsWith("CERTIFY", 6)) { //$NON-NLS-1$
                return new GDataBFC(BFC.CCW_CLIP, parent);
            } else if (line.startsWith("NOCERTIFY", 6)) { //$NON-NLS-1$
                return new GDataBFC(BFC.NOCERTIFY, parent);
            } else if (line.startsWith("CCW", 6)) { //$NON-NLS-1$
                return new GDataBFC(BFC.CCW, parent);
            } else if (line.startsWith("CW", 6)) { //$NON-NLS-1$
                return new GDataBFC(BFC.CW, parent);
            } else if (line.startsWith("NOCLIP", 6)) { //$NON-NLS-1$
                return new GDataBFC(BFC.NOCLIP, parent);
            } else if (line.startsWith("CLIP CCW", 6)) { //$NON-NLS-1$
                return new GDataBFC(BFC.CCW_CLIP, parent);
            } else if (line.startsWith("CLIP CW", 6)) { //$NON-NLS-1$
                return new GDataBFC(BFC.CW_CLIP, parent);
            } else if (line.startsWith("CCW CLIP", 6)) { //$NON-NLS-1$
                return new GDataBFC(BFC.CCW_CLIP, parent);
            } else if (line.startsWith("CW CLIP", 6)) { //$NON-NLS-1$
                return new GDataBFC(BFC.CW_CLIP, parent);
            } else if (line.startsWith("CLIP", 6)) { //$NON-NLS-1$
                return new GDataBFC(BFC.CLIP, parent);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private static GData parseReference(String[] dataSegments, int depth, float r, float g, float b, float a, GData1 parent, Matrix4f productMatrix, Set<String> alreadyParsed, DatFile datFile) {
        if (dataSegments.length < 15) {
            return null;
        } else {
            GColour colour = validateColour(dataSegments[1], r, g, b, a);
            if (colour == null)
                return null;
            Matrix4f tMatrix = new Matrix4f();
            float det = 0;
            try {
                tMatrix.m30 = Float.parseFloat(dataSegments[2]) * 1000f;
                tMatrix.m31 = Float.parseFloat(dataSegments[3]) * 1000f;
                tMatrix.m32 = Float.parseFloat(dataSegments[4]) * 1000f;
                tMatrix.m00 = Float.parseFloat(dataSegments[5]);
                tMatrix.m10 = Float.parseFloat(dataSegments[6]);
                tMatrix.m20 = Float.parseFloat(dataSegments[7]);
                tMatrix.m01 = Float.parseFloat(dataSegments[8]);
                tMatrix.m11 = Float.parseFloat(dataSegments[9]);
                tMatrix.m21 = Float.parseFloat(dataSegments[10]);
                tMatrix.m02 = Float.parseFloat(dataSegments[11]);
                tMatrix.m12 = Float.parseFloat(dataSegments[12]);
                tMatrix.m22 = Float.parseFloat(dataSegments[13]);
            } catch (NumberFormatException nfe) {
                return null;
            }
            tMatrix.m33 = 1f;
            det = tMatrix.determinant();
            if (Math.abs(det) < Threshold.SINGULARITY_DETERMINANT)
                return null;
            // [WARNING] Check file existance
            boolean fileExists = true;
            StringBuilder sb = new StringBuilder();
            for (int s = 14; s < dataSegments.length - 1; s++) {
                sb.append(dataSegments[s]);
                sb.append(" "); //$NON-NLS-1$
            }
            sb.append(dataSegments[dataSegments.length - 1]);
            String shortFilename = sb.toString();
            shortFilename = shortFilename.toLowerCase(Locale.ENGLISH);
            try {
                shortFilename = shortFilename.replaceAll("s\\\\", "S" + File.separator).replaceAll("\\\\", File.separator); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            } catch (Exception e) {
                // Workaround for windows OS / JVM BUG
                shortFilename = shortFilename.replace("s\\", "S" + File.separator).replace("\\", File.separator); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            if (alreadyParsed.contains(shortFilename)) {
                if (!View.DUMMY_REFERENCE.equals(parent))
                    parent.firstRef.setRecursive(true);
                return null;
            } else {
                alreadyParsed.add(shortFilename);
            }
            String shortFilename2 = shortFilename.startsWith("S" + File.separator) ? "s" + shortFilename.substring(1) : shortFilename; //$NON-NLS-1$ //$NON-NLS-2$
            String shortFilename3 = shortFilename.startsWith("S" + File.separator) ? shortFilename.substring(2) : shortFilename; //$NON-NLS-1$
            File fileToOpen = null;
            String[] prefix;
            if (datFile != null && !datFile.isProjectFile() && !View.DUMMY_DATFILE.equals(datFile)) {
                File dff = new File(datFile.getOldName()).getParentFile();
                if (dff != null && dff.exists() && dff.isDirectory()) {
                    prefix = new String[]{dff.getAbsolutePath(), Project.getProjectPath(), WorkbenchManager.getUserSettingState().getUnofficialFolderPath(), WorkbenchManager.getUserSettingState().getLdrawFolderPath()};
                } else {
                    prefix = new String[]{Project.getProjectPath(), WorkbenchManager.getUserSettingState().getUnofficialFolderPath(), WorkbenchManager.getUserSettingState().getLdrawFolderPath()};
                }
            } else {
                prefix = new String[]{Project.getProjectPath(), WorkbenchManager.getUserSettingState().getUnofficialFolderPath(), WorkbenchManager.getUserSettingState().getLdrawFolderPath()};
            }

            String[] middle = new String[]{"", File.separator + "PARTS", File.separator + "parts", File.separator + "P", File.separator + "p"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            String[] suffix = new String[]{File.separator + shortFilename, File.separator + shortFilename2, File.separator + shortFilename3};
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
                                lines = new ArrayList<>(4096);
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
                GDataCSG.forceRecompile(datFile);
                final GData1 result = new GData1(colour.getColourNumber(), colour.getR(), colour.getG(), colour.getB(), colour.getA(), tMatrix, lines, absoluteFilename, sb.toString(), depth, det < 0,
                        destMatrix, parent.firstRef, alreadyParsed, parent, datFile);
                if (result != null && result.firstRef.isRecursive()) {
                    return null;
                }
                alreadyParsed.remove(shortFilename);
                return result;
            } else if (!fileExists) {
                alreadyParsed.remove(shortFilename);
                return null;
            } else {
                absoluteFilename = fileToOpen.getAbsolutePath();
                UTF8BufferedReader reader = null;
                String line = null;
                lines = new ArrayList<>(4096);
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
                    alreadyParsed.remove(shortFilename);
                    return null;
                } catch (LDParsingException e1) {
                    alreadyParsed.remove(shortFilename);
                    return null;
                } catch (UnsupportedEncodingException e1) {
                    alreadyParsed.remove(shortFilename);
                    return null;
                } finally {
                    try {
                        if (reader != null)
                            reader.close();
                    } catch (LDParsingException e1) {
                    }
                }
                Matrix4f destMatrix = new Matrix4f();
                Matrix4f.mul(productMatrix, tMatrix, destMatrix);
                GDataCSG.forceRecompile(datFile);
                final GData1 result = new GData1(colour.getColourNumber(), colour.getR(), colour.getG(), colour.getB(), colour.getA(), tMatrix, lines, absoluteFilename, sb.toString(), depth, det < 0,
                        destMatrix, parent.firstRef, alreadyParsed, parent, datFile);
                alreadyParsed.remove(shortFilename);
                if (result != null && result.firstRef.isRecursive()) {
                    return null;
                }
                return result;
            }
        }
    }

    private static GData parseLine(String[] dataSegments, float r, float g, float b, float a, GData1 parent) {
        if (dataSegments.length != 8) {
            return null;
        } else {
            GColour colour = validateColour(dataSegments[1], r, g, b, a);
            if (colour == null)
                return null;
            try {
                start.setX(Float.parseFloat(dataSegments[2]));
                start.setY(Float.parseFloat(dataSegments[3]));
                start.setZ(Float.parseFloat(dataSegments[4]));
                end.setX(Float.parseFloat(dataSegments[5]));
                end.setY(Float.parseFloat(dataSegments[6]));
                end.setZ(Float.parseFloat(dataSegments[7]));
            } catch (NumberFormatException nfe) {
                return null;
            }
            if (Vector3f.sub(start, end, null).length() < Threshold.IDENTICAL_VERTEX_DISTANCE.floatValue()) {
                return null;
            }
            return new GData2(new Vertex(start.x * 1000f, start.y * 1000f, start.z * 1000f, false), new Vertex(end.x * 1000f, end.y * 1000f, end.z * 1000f, false), colour, parent);
        }
    }

    private static GData parseTriangle(String[] dataSegments, float r, float g, float b, float a, GData1 parent) {
        if (dataSegments.length != 11) {
            return null;
        } else {
            GColour colour = validateColour(dataSegments[1], r, g, b, a);
            if (colour == null)
                return null;
            try {
                vertexA.setX(Float.parseFloat(dataSegments[2]));
                vertexA.setY(Float.parseFloat(dataSegments[3]));
                vertexA.setZ(Float.parseFloat(dataSegments[4]));
                vertexB.setX(Float.parseFloat(dataSegments[5]));
                vertexB.setY(Float.parseFloat(dataSegments[6]));
                vertexB.setZ(Float.parseFloat(dataSegments[7]));
                vertexC.setX(Float.parseFloat(dataSegments[8]));
                vertexC.setY(Float.parseFloat(dataSegments[9]));
            } catch (NumberFormatException nfe) {
                return null;
            }
            try {
                vertexC.setZ(Float.parseFloat(dataSegments[10]));
            } catch (NumberFormatException nfe) {
                return null;
            }
            return new GData3(new Vertex(vertexA.x * 1000f, vertexA.y * 1000f, vertexA.z * 1000f, false), new Vertex(vertexB.x * 1000f, vertexB.y * 1000f, vertexB.z * 1000f, false), new Vertex(vertexC.x * 1000f,
                    vertexC.y * 1000f, vertexC.z * 1000f, false), parent, colour, true);
        }
    }

    private static GData parseQuad(String[] dataSegments, float r, float g, float b, float a, GData1 parent) {
        if (dataSegments.length != 14) {
            return null;
        } else {
            GColour colour = validateColour(dataSegments[1], r, g, b, a);
            if (colour == null)
                return null;
            try {
                vertexA.setX(Float.parseFloat(dataSegments[2]));
                vertexA.setY(Float.parseFloat(dataSegments[3]));
                vertexA.setZ(Float.parseFloat(dataSegments[4]));
                vertexB.setX(Float.parseFloat(dataSegments[5]));
                vertexB.setY(Float.parseFloat(dataSegments[6]));
                vertexB.setZ(Float.parseFloat(dataSegments[7]));
                vertexC.setX(Float.parseFloat(dataSegments[8]));
                vertexC.setY(Float.parseFloat(dataSegments[9]));
                vertexC.setZ(Float.parseFloat(dataSegments[10]));
                vertexD.setX(Float.parseFloat(dataSegments[11]));
                vertexD.setY(Float.parseFloat(dataSegments[12]));
                vertexD.setZ(Float.parseFloat(dataSegments[13]));
            } catch (NumberFormatException nfe) {
                return null;
            }
            return new GData4(new Vertex(vertexA.x * 1000f, vertexA.y * 1000f, vertexA.z * 1000f, false), new Vertex(vertexB.x * 1000f, vertexB.y * 1000f, vertexB.z * 1000f, false), new Vertex(vertexC.x * 1000f,
                    vertexC.y * 1000f, vertexC.z * 1000f, false), new Vertex(vertexD.x * 1000f, vertexD.y * 1000f, vertexD.z * 1000f, false), parent, colour);
        }
    }

    private static GData parseCondline(String[] dataSegments, float r, float g, float b, float a, GData1 parent) {
        if (dataSegments.length != 14) {
            return null;
        } else {
            GColour colour = validateColour(dataSegments[1], r, g, b, a);
            if (colour == null)
                return null;
            try {
                start.setX(Float.parseFloat(dataSegments[2]));
                start.setY(Float.parseFloat(dataSegments[3]));
                start.setZ(Float.parseFloat(dataSegments[4]));
                end.setX(Float.parseFloat(dataSegments[5]));
                end.setY(Float.parseFloat(dataSegments[6]));
                end.setZ(Float.parseFloat(dataSegments[7]));
                controlI.setX(Float.parseFloat(dataSegments[8]));
                controlI.setY(Float.parseFloat(dataSegments[9]));
                controlI.setZ(Float.parseFloat(dataSegments[10]));
                controlII.setX(Float.parseFloat(dataSegments[11]));
                controlII.setY(Float.parseFloat(dataSegments[12]));
                controlII.setZ(Float.parseFloat(dataSegments[13]));
            } catch (NumberFormatException nfe) {
                return null;
            }
            final float epsilon = Threshold.IDENTICAL_VERTEX_DISTANCE.floatValue();
            if (Vector3f.sub(start, end, null).length() < epsilon || Vector3f.sub(controlI, controlII, null).length() < epsilon) {
                return null;
            }
            return new GData5(new Vertex(start.x * 1000f, start.y * 1000f, start.z * 1000f, false), new Vertex(end.x * 1000f, end.y * 1000f, end.z * 1000f, false), new Vertex(controlI.x * 1000f,
                    controlI.y * 1000f, controlI.z * 1000f, false), new Vertex(controlII.x * 1000f, controlII.y * 1000f, controlII.z * 1000f, false), colour, parent);
        }
    }
}
