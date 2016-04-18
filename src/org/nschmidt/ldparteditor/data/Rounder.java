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

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.custom.StyledText;
import org.nschmidt.ldparteditor.composites.compositetab.CompositeTabState;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.math.HashBiMap;
import org.nschmidt.ldparteditor.text.DatParser;
import org.nschmidt.ldparteditor.text.StringHelper;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * Rounds selected lines
 *
 * @author nils
 *
 */
public enum Rounder {
    INSTANCE;

    /**
     * Rounds selected lines (clears the selection)
     *
     * @param cText
     *            the selected CompositeText
     * @param lineStart
     *            start line number to round
     * @param lineEnd
     *            end line number to round
     * @param datFile
     */
    public static void round(CompositeTabState st, StyledText cText, int lineStart, int lineEnd, DatFile datFile) {

        if (datFile.isReadOnly())
            return;

        datFile.getVertexManager().clearSelection();

        GDataCSG.resetCSG(false);
        GDataCSG.forceRecompile();

        // Check here if single vertex replacing (ALT+SHIFT+R) is active
        // If so, round only this vertex!

        if (st.isReplacingVertex() && datFile.getVertexManager().getVertices().contains(datFile.getVertexManager().getVertexToReplace())) {
            Vertex vOld = new Vertex(st.getToReplaceX(), st.getToReplaceY(), st.getToReplaceZ());
            int coordsDecimalPlaces = WorkbenchManager.getUserSettingState().getCoordsPrecision();
            Vertex vNew = new Vertex(vOld.X.setScale(coordsDecimalPlaces, RoundingMode.HALF_UP), vOld.Y.setScale(coordsDecimalPlaces, RoundingMode.HALF_UP), vOld.Z.setScale(coordsDecimalPlaces,
                    RoundingMode.HALF_UP));
            datFile.getVertexManager().changeVertexDirectFast(vOld, vNew, true);
            st.setToReplaceX(vNew.X);
            st.setToReplaceY(vNew.Y);
            st.setToReplaceZ(vNew.Z);
            datFile.getVertexManager().setVertexToReplace(vNew);
            datFile.getVertexManager().setModified_NoSync();

            ArrayList<Integer> lineNumbers = new ArrayList<Integer>();

            lineNumbers.addAll(datFile.getDrawPerLine_NOCLONE().keySet());

            boolean lastLineIsEmpty = datFile.getDrawChainTail().type() == 0 && ((GData0) datFile.getDrawChainTail()).text.isEmpty();

            Collections.sort(lineNumbers);
            Collections.reverse(lineNumbers);

            String text = cText.getText();
            String text2 = text;
            int c = cText.getCaretOffset();

            // Set the identifiers for each line
            text2 = "<L1>" + text2; //$NON-NLS-1$
            if (text2.contains("\r\n")) { //$NON-NLS-1$ Windows line termination
                text2 = text2.replaceAll("\\r\\n", "#!%"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (text2.contains("\n")) { //$NON-NLS-1$ Linux/Mac line termination
                text2 = text2.replaceAll("\\n", "#!%"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (!text2.endsWith("#!%")) { //$NON-NLS-1$
                text2 = text2 + "#!%"; //$NON-NLS-1$
            }
            {
                int state = 0;
                int l = 1;
                StringBuilder sb = new StringBuilder();
                for (char ch : text2.toCharArray()) {
                    if (state == 0 && ch == '#') {
                        state++;
                    } else if (state == 1 && ch == '!') {
                        state++;
                    } else if (state == 2 && ch == '%') {
                        state = 0;
                        sb.append("</L" + l + "><L" + (l + 1) + ">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        l++;
                    } else {
                        sb.append(ch);
                    }
                }
                text2 = sb.toString();
            }
            for (Integer l : lineNumbers) {
                String line = getLine(l, text2);
                text2 = Rounder.round2(l, line, text2, datFile);
            }
            cText.setText(restoreLineTermination(text2, lastLineIsEmpty));
            int tl = cText.getText().length();
            try {
                cText.setSelection(Math.min(c, tl));
            } catch (Exception e) {
                cText.setSelection(0);
            }

        } else {

            ArrayList<Integer> lineNumbers = new ArrayList<Integer>();
            lineEnd += 1;
            for (int line = lineStart; line < lineEnd; line++) {
                lineNumbers.add(line);
            }

            boolean lastLineIsEmpty = datFile.getDrawChainTail().type() == 0 && ((GData0) datFile.getDrawChainTail()).text.isEmpty();

            Collections.sort(lineNumbers);
            Collections.reverse(lineNumbers);

            String text = cText.getText();
            String text2 = text;
            int c = cText.getCaretOffset();

            // Set the identifiers for each line
            text2 = "<L1>" + text2; //$NON-NLS-1$
            if (text2.contains("\r\n")) { //$NON-NLS-1$ Windows line termination
                text2 = text2.replaceAll("\\r\\n", "#!%"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (text2.contains("\n")) { //$NON-NLS-1$ Linux/Mac line termination
                text2 = text2.replaceAll("\\n", "#!%"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (!text2.endsWith("#!%")) { //$NON-NLS-1$
                text2 = text2 + "#!%"; //$NON-NLS-1$
            }
            {
                int state = 0;
                int l = 1;
                StringBuilder sb = new StringBuilder();
                for (char ch : text2.toCharArray()) {
                    if (state == 0 && ch == '#') {
                        state++;
                    } else if (state == 1 && ch == '!') {
                        state++;
                    } else if (state == 2 && ch == '%') {
                        state = 0;
                        sb.append("</L" + l + "><L" + (l + 1) + ">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        l++;
                    } else {
                        sb.append(ch);
                    }
                }
                text2 = sb.toString();
            }
            int coordsDecimalPlaces = WorkbenchManager.getUserSettingState().getCoordsPrecision();
            int matrixDecimalPlaces = WorkbenchManager.getUserSettingState().getTransMatrixPrecision();
            for (Integer l : lineNumbers) {
                String line = getLine(l, text2);
                text2 = Rounder.round(l, line, text2, datFile, coordsDecimalPlaces, matrixDecimalPlaces);
            }
            datFile.getVertexManager().setModified_NoSync();

            cText.setText(restoreLineTermination(text2, lastLineIsEmpty));
            int tl = cText.getText().length();
            try {
                cText.setSelection(Math.min(c, tl));
            } catch (Exception e) {
                cText.setSelection(0);
            }
        }
    }

    static String insertBeforeLine(int line, String textToInsert, String text) {
        return text.replaceFirst("<L" + line + ">", textToInsert + "<L" + line + ">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    static String setLine(int line, String textToSet, String text) {
        return text.replaceFirst("<L" + line + ">.*</L" + line + ">", "<L" + line + ">" + textToSet.replace("\\", "\\\\") + "</L" + line + ">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
    }

    static String getLine(int line, String text) {
        Pattern pattern = Pattern.compile("<L" + line + ">.*</L" + line + ">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            Pattern pattern2 = Pattern.compile(">.*<"); //$NON-NLS-1$
            Matcher matcher2 = pattern2.matcher(matcher.group(0));
            if (matcher2.find()) {
                String g = matcher2.group();
                return g.substring(1, g.length() - 1);
            }
        }
        return ""; //$NON-NLS-1$
    }

    static String insertAfterLine(int line, String textToInsert, String text) {
        return text.replaceFirst("</L" + line + ">", "</L" + line + ">" + textToInsert); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    static String restoreLineTermination(String text, boolean lastLineIsEmpty) {
        text = text.replaceAll("<L[0-9]+><rm></L[0-9]+>", ""); //$NON-NLS-1$ //$NON-NLS-2$
        text = text.replaceAll("</L[0-9]+>", "<br>"); //$NON-NLS-1$ //$NON-NLS-2$
        text = text.replaceAll("<L[0-9]+>", ""); //$NON-NLS-1$ //$NON-NLS-2$
        final int tl = text.length();
        if (tl > 3) text = text.substring(0, tl - 4);
        return text.replaceAll("<br>", StringHelper.getLineDelimiter()) + (lastLineIsEmpty ? StringHelper.getLineDelimiter() : ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private static String round(Integer lineNumber, String line, String source, DatFile datFile, int coordsDecimalPlaces, int matrixDecimalPlaces) {

        HashBiMap<Integer, GData> drawPerLine = datFile.getDrawPerLine_NOCLONE();
        GData gd = drawPerLine.getValue(lineNumber);

        if (gd.type() == 1) {
            GData1 subf = (GData1) gd;
            String roundedString = subf.getRoundedString(coordsDecimalPlaces, matrixDecimalPlaces);
            GData roundedSubfile;
            if (16 == subf.colourNumber) {
                roundedSubfile = DatParser
                        .parseLine(roundedString, drawPerLine.getKey(subf).intValue(), 0, 0.5f, 0.5f, 0.5f, 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, datFile, false, new HashSet<String>(), false)
                        .get(0).getGraphicalData();
            } else {
                roundedSubfile = DatParser
                        .parseLine(roundedString, drawPerLine.getKey(subf).intValue(), 0, subf.r, subf.g, subf.b, subf.a, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, datFile, false,
                                new HashSet<String>(), false).get(0).getGraphicalData();
            }
            if (subf.equals(datFile.getDrawChainTail()))
                datFile.setDrawChainTail(roundedSubfile);
            GData oldNext = subf.getNext();
            GData oldBefore = subf.getBefore();
            oldBefore.setNext(roundedSubfile);
            roundedSubfile.setNext(oldNext);
            Integer oldNumber = drawPerLine.getKey(subf);
            if (oldNumber != null)
                drawPerLine.put(oldNumber, roundedSubfile);
            datFile.getVertexManager().remove(subf);
            source = setLine(lineNumber, roundedString, source);
        } else {
            Set<VertexInfo> singleVertices = datFile.getVertexManager().getLineLinkedToVertices().get(gd);
            if (singleVertices != null) {
                for (VertexInfo vOld2 : singleVertices) {
                    Vertex vOld = vOld2.getVertex();
                    Vertex vNew = new Vertex(vOld.X.setScale(coordsDecimalPlaces, RoundingMode.HALF_UP), vOld.Y.setScale(coordsDecimalPlaces, RoundingMode.HALF_UP), vOld.Z.setScale(
                            coordsDecimalPlaces, RoundingMode.HALF_UP));
                    datFile.getVertexManager().changeVertexDirectFast(vOld, vNew, true);
                }
                source = setLine(lineNumber, datFile.getDrawPerLine_NOCLONE().getValue(lineNumber).toString(), source);
            } else {
                source = setLine(lineNumber, line, source);
            }
        }

        return source;
    }

    private static String round2(Integer lineNumber, String line, String source, DatFile datFile) {

        GData gd = datFile.getDrawPerLine_NOCLONE().getValue(lineNumber);

        source = setLine(lineNumber, gd.toString(), source);

        return source;
    }

}
