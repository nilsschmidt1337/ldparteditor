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
package org.nschmidt.ldparteditor.helper.compositetext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.lwjgl.util.vector.Matrix4f;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.enumtype.DatKeyword;
import org.nschmidt.ldparteditor.enumtype.LDConfig;
import org.nschmidt.ldparteditor.enumtype.Threshold;
import org.nschmidt.ldparteditor.helper.math.HashBiMap;
import org.nschmidt.ldparteditor.helper.math.MathHelper;
import org.nschmidt.ldparteditor.helper.math.Vector3d;
import org.nschmidt.ldparteditor.logger.NLogger;

/**
 * @author nils
 *
 */
enum WarningFixer {
    INSTANCE;

    public static String fix(int lineNumber, String sort, String line, String text, String description, DatFile datFile) {
        int s = Integer.parseInt(sort, 16);
        switch (s) {
        case 204: // Upper- & Mixed-Case File Name
        {
            String[] dataSegments = line.trim().split("\\s+"); //$NON-NLS-1$
            if (dataSegments.length > 11) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 11; i++) {
                    sb.append(" "); //$NON-NLS-1$
                    sb.append(dataSegments[i]);
                }
                for (int i = 11; i < dataSegments.length; i++) {
                    sb.append(" "); //$NON-NLS-1$
                    sb.append(dataSegments[i].toLowerCase(Locale.ENGLISH));
                }
                text = QuickFixer.setLine(lineNumber + 1, sb.toString().trim(), text);
            }
        }
        break;
        case 220: // Dithered Colour
        {
            String[] dataSegments = line.trim().split("\\s+"); //$NON-NLS-1$
            StringBuilder colourBuilder = new StringBuilder();
            try {

                if (dataSegments.length > 3) {
                    int colourValue = Integer.parseInt(dataSegments[1]);
                    int indexA = colourValue - 256 >> 4;
                    int indexB = colourValue - 256 & 0x0F;
                    if (LDConfig.hasColour(indexA) && LDConfig.hasColour(indexB)) {
                        GColour colourA = LDConfig.getColour(indexA);
                        GColour colourB = LDConfig.getColour(indexB);
                        colourBuilder.append("0x2"); //$NON-NLS-1$
                        colourBuilder.append(MathHelper.toHex((int) (255f * ((colourA.getR() + colourB.getR()) / 2f))).toUpperCase());
                        colourBuilder.append(MathHelper.toHex((int) (255f * ((colourA.getG() + colourB.getG()) / 2f))).toUpperCase());
                        colourBuilder.append(MathHelper.toHex((int) (255f * ((colourA.getB() + colourB.getB()) / 2f))).toUpperCase());
                    } else {
                        return text;
                    }

                    StringBuilder sb = new StringBuilder();
                    sb.append(dataSegments[0]);
                    sb.append(" ");  //$NON-NLS-1$
                    sb.append(colourBuilder.toString());
                    for (int i = 2; i < dataSegments.length - 1; i++) {
                        sb.append(" "); //$NON-NLS-1$
                        sb.append(dataSegments[i]);
                    }
                    sb.append(" "); //$NON-NLS-1$
                    sb.append(dataSegments[dataSegments.length - 1]);
                    text = QuickFixer.setLine(lineNumber + 1, sb.toString().trim(), text);
                }
            } catch (NumberFormatException nfe) {
                return text;
            }
        }
        break;
        case 11: // 0 BFC CERTIFY INVERTNEXT
        {
            String[] dataSegments = line.trim().split("\\s+"); //$NON-NLS-1$
            text = QuickFixer.setLine(lineNumber + 1,
                    "0 " + dataSegments[1] + " " +  dataSegments[3], text);  //$NON-NLS-1$ //$NON-NLS-2$
        }
        break;
        case 12: // 0 BFC CERTIFY CLIP CCW
        {
            String[] dataSegments = line.trim().split("\\s+"); //$NON-NLS-1$
            text = QuickFixer.setLine(lineNumber + 1,
                    "0 " + dataSegments[1] + " " +  dataSegments[3] + " " +  dataSegments[4], text);  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        break;
        case 36: // Coplanar Quad
        {
            String[] dataSegments = line.trim().split("\\s+"); //$NON-NLS-1$

            final Vector3d vertexA = new Vector3d();
            final Vector3d vertexB = new Vector3d();
            final Vector3d vertexC = new Vector3d();
            final Vector3d vertexD = new Vector3d();

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
            if (angle > Threshold.coplanarityAngleError) {
                text = QuickFixer.setLine(lineNumber + 1,
                        "3 " + dataSegments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                                dataSegments[2] + " " + dataSegments[3] + " " + dataSegments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                dataSegments[5] + " " + dataSegments[6] + " " + dataSegments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                dataSegments[8] + " " + dataSegments[9] + " " + dataSegments[10] +  //$NON-NLS-1$ //$NON-NLS-2$
                                "<br>3 " + dataSegments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                                dataSegments[8] + " " + dataSegments[9] + " " + dataSegments[10] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                dataSegments[11] + " " + dataSegments[12] + " " + dataSegments[13] + " " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                dataSegments[2] + " " + dataSegments[3] + " " + dataSegments[4] + //$NON-NLS-1$ //$NON-NLS-2$
                                "<br>5 24 " +  //$NON-NLS-1$
                                dataSegments[8] + " " + dataSegments[9] + " " + dataSegments[10] + " " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                dataSegments[2] + " " + dataSegments[3] + " " + dataSegments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                dataSegments[5] + " " + dataSegments[6] + " " + dataSegments[7] + " " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                dataSegments[11] + " " + dataSegments[12] + " " + dataSegments[13], text);  //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                text = QuickFixer.setLine(lineNumber + 1,
                        "3 " + dataSegments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                                dataSegments[5] + " " + dataSegments[6] + " " + dataSegments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                dataSegments[8] + " " + dataSegments[9] + " " + dataSegments[10] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                dataSegments[11] + " " + dataSegments[12] + " " + dataSegments[13] + //$NON-NLS-1$ //$NON-NLS-2$
                                "<br>3 " + dataSegments[1] + " " +  //$NON-NLS-1$ //$NON-NLS-2$
                                dataSegments[11] + " " + dataSegments[12] + " " + dataSegments[13] + " " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                dataSegments[2] + " " + dataSegments[3] + " " + dataSegments[4] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                dataSegments[5] + " " + dataSegments[6] + " " + dataSegments[7] + //$NON-NLS-1$ //$NON-NLS-2$
                                "<br>5 24 " +  //$NON-NLS-1$
                                dataSegments[11] + " " + dataSegments[12] + " " + dataSegments[13] + " " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                dataSegments[5] + " " + dataSegments[6] + " " + dataSegments[7] + " " +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                dataSegments[2] + " " + dataSegments[3] + " " + dataSegments[4] + " " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                dataSegments[8] + " " + dataSegments[9] + " " + dataSegments[10], text);  //$NON-NLS-1$ //$NON-NLS-2$
            }

        }
        break;
        case 2: // Flat subfile scaled on X
        {
            String[] dataSegments = line.trim().split("\\s+"); //$NON-NLS-1$
            StringBuilder sb = new StringBuilder();
            for (int k = 0; k < 3; k++) {
                sb.setLength(0);
                int i = 0;
                String sign;
                for (String seg : dataSegments) {
                    if (!seg.trim().equals("")) {  //$NON-NLS-1$
                        i++;
                        switch (i) {
                        case 6:
                            sign = seg.startsWith("-") ? "-" : ""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            sb.append(k == 0 ? sign + "1" : "0"); //$NON-NLS-1$ //$NON-NLS-2$
                            break;
                        case 9:
                            sign = seg.startsWith("-") ? "-" : ""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            sb.append(k == 2 ? sign + "1" : "0"); //$NON-NLS-1$ //$NON-NLS-2$
                            break;
                        case 12:
                            sign = seg.startsWith("-") ? "-" : ""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            sb.append(k == 1 ? sign + "1" : "0"); //$NON-NLS-1$ //$NON-NLS-2$
                            break;
                        default:
                            sb.append(seg);
                            break;
                        }
                    }
                    sb.append(" "); //$NON-NLS-1$
                }
                if (hasGoodDeterminant(sb.toString())) break;
            }
            text = QuickFixer.setLine(lineNumber + 1, sb.toString().trim(), text);
        }
        break;
        case 3: // Flat subfile scaled on Y
        {
            String[] dataSegments = line.trim().split("\\s+"); //$NON-NLS-1$
            StringBuilder sb = new StringBuilder();
            for (int k = 0; k < 3; k++) {
                sb.setLength(0);
                int i = 0;
                String sign;
                for (String seg : dataSegments) {
                    if (!seg.trim().equals("")) {  //$NON-NLS-1$
                        i++;
                        switch (i) {
                        case 7:
                            sign = seg.startsWith("-") ? "-" : ""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            sb.append(k == 1 ? sign + "1" : "0"); //$NON-NLS-1$ //$NON-NLS-2$
                            break;
                        case 10:
                            sign = seg.startsWith("-") ? "-" : ""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            sb.append(k == 0 ? sign + "1" : "0"); //$NON-NLS-1$ //$NON-NLS-2$
                            break;
                        case 13:
                            sign = seg.startsWith("-") ? "-" : ""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            sb.append(k == 2 ? sign + "1" : "0"); //$NON-NLS-1$ //$NON-NLS-2$
                            break;
                        default:
                            sb.append(seg);
                            break;
                        }
                    }
                    sb.append(" "); //$NON-NLS-1$
                }
                if (hasGoodDeterminant(sb.toString())) break;
            }
            text = QuickFixer.setLine(lineNumber + 1, sb.toString().trim(), text);
        }
        break;
        case 4: // Flat subfile scaled on Z
        {
            String[] dataSegments = line.trim().split("\\s+"); //$NON-NLS-1$
            StringBuilder sb = new StringBuilder();

            for (int k = 0; k < 3; k++) {
                sb.setLength(0);
                int i = 0;
                String sign;
                for (String seg : dataSegments) {
                    if (!seg.trim().equals("")) {  //$NON-NLS-1$
                        i++;
                        switch (i) {
                        case 8:
                            sign = seg.startsWith("-") ? "-" : ""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            sb.append(k == 2 ? sign + "1" : "0"); //$NON-NLS-1$ //$NON-NLS-2$
                            break;
                        case 11:
                            sign = seg.startsWith("-") ? "-" : ""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            sb.append(k == 1 ? sign + "1" : "0"); //$NON-NLS-1$ //$NON-NLS-2$
                            break;
                        case 14:
                            sign = seg.startsWith("-") ? "-" : ""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            sb.append(k == 0 ? sign + "1" : "0"); //$NON-NLS-1$ //$NON-NLS-2$
                            break;
                        default:
                            sb.append(seg);
                            break;
                        }
                    }
                    sb.append(" "); //$NON-NLS-1$
                }
                if (hasGoodDeterminant(sb.toString())) break;
            }
            text = QuickFixer.setLine(lineNumber + 1, sb.toString().trim(), text);
        }
        break;
        case 1 /* Inlining relict */, 13 /* Unofficial Meta Command */, 254 /* !LPE VERTEX */:
            text = QuickFixer.setLine(lineNumber + 1, "<rm>", text); //$NON-NLS-1$
            break;
        case 192: /* FIXME !LPE CONST needs to be inlined */
            final List<String> pair = getPair(description);
            final String variable = pair.get(0);
            final String variableNeg = "-" +pair.get(0); //$NON-NLS-1$
            final String value = pair.get(1);
            NLogger.debug(WarningFixer.class, "Variable:" + variable); //$NON-NLS-1$
            NLogger.debug(WarningFixer.class, "Value   :" + value); //$NON-NLS-1$
            if (DatKeyword.getKeywords().contains(variable)) break;
            text = inlineConstant(lineNumber, text, datFile, variable, variableNeg, value);
            break;
        default:
            break;
        }
        return text;
    }


    private static String inlineConstant(int lineNumber, String text, DatFile datFile, final String variable,
            final String variableNeg, final String value) {
        String line;
        // Delete the declaration
        text = QuickFixer.setLine(lineNumber + 1, "<rm>", text); //$NON-NLS-1$
        lineNumber++;
        final HashBiMap<Integer, GData> dpl = datFile.getDrawPerLineNoClone();
        final Pattern whitespace = Pattern.compile("\\s+"); //$NON-NLS-1$
        final String valueInBrackets = "(" + value + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        final StringBuilder sb = new StringBuilder();
        boolean doReplace = false;
        boolean isCSGbody = false;
        boolean isCSGtransform = false;
        while (dpl.containsKey(lineNumber + 1)) {
            line = QuickFixer.getLine(lineNumber, text);
            lineNumber++;
            final String[] dataSegments = whitespace.split(line);
            if (dataSegments.length > 1 && "0".equals(dataSegments[0]) ) { //$NON-NLS-1$
                if ("//".equals(dataSegments[1]) || "!:".equals(dataSegments[1]) || "!TEXMAP".equals(dataSegments[1])) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    // Skip comment and texmap lines
                    continue;
                }
                
                if (dataSegments.length > 2 && "!LPE".equals(dataSegments[1]) && dataSegments[2].startsWith("CSG_")) { //$NON-NLS-1$ //$NON-NLS-2$
                    if ("CSG_UNION".equals(dataSegments[2]) || "CSG_DIFFERENCE".equals(dataSegments[2]) || "CSG_INTERSECTION".equals(dataSegments[2]) || "CSG_COMPILE".equals(dataSegments[2])) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                        // Skip CSG actions
                        continue;
                    }
                    
                    isCSGbody = "CSG_EXTRUDE".equals(dataSegments[2]) || "CSG_QUAD".equals(dataSegments[2]) || "CSG_MESH".equals(dataSegments[2]) || "CSG_CUBOID".equals(dataSegments[2]) || "CSG_ELLIPSOID".equals(dataSegments[2]) || "CSG_CYLINDER".equals(dataSegments[2]) || "CSG_CONE".equals(dataSegments[2]) || "CSG_CIRCLE".equals(dataSegments[2]); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
                    
                    isCSGtransform = "CSG_TRANSFORM".equals(dataSegments[2]) ; //$NON-NLS-1$
                }
            }
            
            if (dataSegments.length == 6 && "0".equals(dataSegments[0]) && "!LPE".equals(dataSegments[1]) && "CONST".equals(dataSegments[2]) && "=".equals(dataSegments[4])) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                if (variable.equals(dataSegments[3])) {
                    NLogger.debug(WarningFixer.class, "Same variable defined in line " + lineNumber); //$NON-NLS-1$
                    break;
                }
                
                String expr = dataSegments[5];
                if (expr.contains(variable)) {
                    int index = 0;
                    int attempts = 0;
                    while (index != -1 && attempts < 100) {
                        attempts += 1;
                        index = expr.indexOf(variable, index);
                        if (index != -1) {
                            index += variable.length();
                            if (index == expr.length()) {
                                // variable is at the end of the expression
                                expr  = expr.substring(0, index - variable.length()) +  valueInBrackets;
                            } else {
                                // Variable is not at the end, next character must be non-alphabetic and non-numeric
                                char nextCharacter = expr.charAt(index);
                                if (!Character.isAlphabetic(nextCharacter) && !Character.isDigit(nextCharacter)) {
                                    expr  = expr.substring(0, index - variable.length()) + valueInBrackets + expr.substring(index);
                                    index = 0;
                                }
                            }
                        }
                    }
                    
                    text = QuickFixer.setLine(lineNumber, "0 !LPE CONST " + dataSegments[3] + " = " + expr, text); //$NON-NLS-1$ //$NON-NLS-2$
                    continue;
                }
            }

            int segCount = 0;
            sb.setLength(0);
            for (String seg : dataSegments) {
                segCount++;
                if (isCSGbody && segCount == 4 || isCSGtransform && (segCount == 4 || segCount == 5)) {
                    // Don't replace CSG variables
                    sb.append(seg);
                } else if (seg.equals(variable)) {
                    sb.append(value);
                    doReplace = true;
                } else if (seg.equals(variableNeg)) {
                    if (value.startsWith("-")) { //$NON-NLS-1$
                        sb.append(value.substring(1));
                    } else {
                        sb.append('-');
                        sb.append(value);
                    }
                    
                    doReplace = true;
                } else {
                    sb.append(seg);
                }
                sb.append(' ');
            }
            
            if (doReplace) {
                sb.setLength(sb.length() - 1);
                text = QuickFixer.setLine(lineNumber, sb.toString(), text);
                doReplace = false;
            }
        }
        return text;
    }
    
    
    private static List<String> getPair(String description) {
        final  List<String> result = new ArrayList<>();
        final int lenght = description.length();
        final StringBuilder sb = new StringBuilder();
        boolean collectToken = false;
        for (int i = 0; i < lenght; i++) {
            final char c = description.charAt(i);
            
            
            if (collectToken) {
                if (c == ']') {
                    result.add(sb.toString());
                    collectToken = false;
                    sb.setLength(0);
                } else {
                    sb.append(c);
                }
            }
            
            if (c == '[') {
                collectToken = true;
            }
        }        
        return result;
    }

    private static boolean hasGoodDeterminant(String line) {
        String[] dataSegments = line.trim().split("\\s+"); //$NON-NLS-1$
        // [ERROR] Check singularity
        Matrix4f tMatrix = new Matrix4f();
        float det = 0;
        try {
            // Offset
            BigDecimal m30 = new BigDecimal(dataSegments[2]);
            tMatrix.m30 = m30.floatValue() * 1000f;
            BigDecimal m31 = new BigDecimal(dataSegments[3]);
            tMatrix.m31 = m31.floatValue() * 1000f;
            BigDecimal m32 = new BigDecimal(dataSegments[4]);
            tMatrix.m32 = m32.floatValue() * 1000f;
            // First row
            BigDecimal m00 = new BigDecimal(dataSegments[5]);
            tMatrix.m00 = m00.floatValue();
            BigDecimal m10 = new BigDecimal(dataSegments[6]);
            tMatrix.m10 = m10.floatValue();
            BigDecimal m20 = new BigDecimal(dataSegments[7]);
            tMatrix.m20 = m20.floatValue();
            // Second row
            BigDecimal m01 = new BigDecimal(dataSegments[8]);
            tMatrix.m01 = m01.floatValue();
            BigDecimal m11 = new BigDecimal(dataSegments[9]);
            tMatrix.m11 = m11.floatValue();
            BigDecimal m21 = new BigDecimal(dataSegments[10]);
            tMatrix.m21 = m21.floatValue();
            // Third row
            BigDecimal m02 = new BigDecimal(dataSegments[11]);
            tMatrix.m02 = m02.floatValue();
            BigDecimal m12 = new BigDecimal(dataSegments[12]);
            tMatrix.m12 = m12.floatValue();
            BigDecimal m22 = new BigDecimal(dataSegments[13]);
            tMatrix.m22 = m22.floatValue();
        } catch (NumberFormatException nfe) {
            // Can't happen
            return false;
        }
        tMatrix.m33 = 1f;
        det = tMatrix.determinant();
        return Math.abs(det) >= Threshold.SINGULARITY_DETERMINANT;
    }
}
