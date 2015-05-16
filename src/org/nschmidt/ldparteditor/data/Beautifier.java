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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.custom.StyledText;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.helpers.math.HashBiMap;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.text.StringHelper;

/**
 * @author nils
 *
 */
public enum Beautifier {
    INSTANCE;

    private static String lastCommentLine = null;
    private static int elc = 0;
    private static HashSet<String> lines = new HashSet<String>();

    /**
     * Beautify the file source (clears the selection)
     *
     * @param cText
     *            the selected CompositeText
     * @param datFile
     */
    public static void beautify(StyledText cText, DatFile datFile) {


        // TODO Needs implementation!

        if (datFile.isReadOnly())
            return;

        lastCommentLine = null;
        lastType = 0;

        datFile.getVertexManager().clearSelection();

        GDataCSG.resetCSG();
        GDataCSG.forceRecompile();

        TreeSet<Integer> lineNumbers = new TreeSet<Integer>(datFile.getDrawPerLine_NOCLONE().keySet());

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

        int headerLastLine = 1;
        {
            HashBiMap<Integer, GData> dpl = datFile.getDrawPerLine_NOCLONE();
            Set<Integer> keys = dpl.keySet();

            boolean headerEnd = false;
            for (Integer key : keys) {
                GData gd = dpl.getValue(key);
                switch (gd.type()) {
                case 0:
                case 6:
                    break;
                default:
                    headerLastLine = key;
                    GData g0 = gd;
                    while (g0 != null && g0.toString().trim().equals("")) { //$NON-NLS-1$
                        g0 = g0.getBefore();
                        headerLastLine -= 1;
                    }
                    headerLastLine += 1;
                    headerEnd = true;
                    break;
                }
                if (headerEnd) {
                    break;
                }
            }
        }

        for (Iterator<Integer> it = lineNumbers.descendingIterator(); it.hasNext();) {
            Integer l = it.next();
            if (l < headerLastLine) break;
            String line = getLine(l, text2);
            text2 = Beautifier.beautify(l, line, text2, datFile);
        }
        lines.clear();

        cText.setText(restoreLineTermination(text2));
        int tl = cText.getText().length();
        try {
            cText.setSelection(Math.min(c, tl));
        } catch (Exception e) {
            cText.setSelection(0);
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

    static String restoreLineTermination(String text) {
        text = text.replaceAll("<L[0-9]+><rm></L[0-9]+>", ""); //$NON-NLS-1$ //$NON-NLS-2$
        text = text.replaceAll("</L[0-9]+>", "<br>"); //$NON-NLS-1$ //$NON-NLS-2$
        text = text.replaceAll("<L[0-9]+>", ""); //$NON-NLS-1$ //$NON-NLS-2$
        final int tl = text.length();
        if (tl > 3) text = text.substring(0, tl - 4);
        return text.replaceAll("<br>", StringHelper.getLineDelimiter()); //$NON-NLS-1$
    }

    private static int lastType = 0;
    private static String beautify(Integer lineNumber, String line, String source, DatFile datFile) {

        GData gd = datFile.getDrawPerLine_NOCLONE().getValue(lineNumber);

        int type = gd.type();
        String trimmedLine = line.trim();
        String[] data_segments = trimmedLine.split("\\s+"); //$NON-NLS-1$
        StringBuilder normalized = new StringBuilder();
        for (String string : data_segments) {
            normalized.append(string);
            normalized.append(" "); //$NON-NLS-1$
        }
        String normalizedLine = normalized.toString().trim();

        if (type != 0) {
            lastType = type;
        }

        switch (type) {
        case 0:
            if (lastType != 0 && normalizedLine.equals("")) { //$NON-NLS-1$
                source = setLine(lineNumber, "<rm>", source); //$NON-NLS-1$
            } else {
                lastType = 0;
            }
            break;
        case 1:

            GData invertNextData = gd.getBefore();

            // Check if a INVERTNEXT is present
            while (invertNextData != null && invertNextData.type() != 1 && (invertNextData.type() != 6 || ((GDataBFC) invertNextData).type != BFC.INVERTNEXT)) {
                invertNextData = invertNextData.getBefore();
            }
            if (invertNextData != null && invertNextData.type() == 6) {
                normalizedLine = "IV" + normalizedLine; //$NON-NLS-1$
                if (lines.contains(normalizedLine)) {
                    lastCommentLine = "0 BFC INVERTNEXT"; //$NON-NLS-1$
                    source = setLine(lineNumber, "<rm>", source); //$NON-NLS-1$
                } else {
                    source = setLine(lineNumber, trimmedLine, source);
                    lastCommentLine = null;
                    lines.add(normalizedLine);
                    elc = 0;
                }
            } else {
                normalizedLine = "NIV" + normalizedLine; //$NON-NLS-1$
                if (lines.contains(normalizedLine)) {
                    source = setLine(lineNumber, "<rm>", source); //$NON-NLS-1$
                } else {
                    source = setLine(lineNumber, trimmedLine, source);
                    lastCommentLine = null;
                    lines.add(normalizedLine);
                    elc = 0;
                }
            }
            break;
        case 2:
        {
            if (lines.contains(normalizedLine)) {
                source = setLine(lineNumber, "<rm>", source); //$NON-NLS-1$
            } else {
                lines.add(normalizedLine);
                StringBuilder normalized2 = new StringBuilder();

                normalized2.append(data_segments[0]);
                normalized2.append(" "); //$NON-NLS-1$
                normalized2.append(data_segments[1]);
                normalized2.append(" "); //$NON-NLS-1$

                normalized2.append(data_segments[5]);
                normalized2.append(" "); //$NON-NLS-1$
                normalized2.append(data_segments[6]);
                normalized2.append(" "); //$NON-NLS-1$
                normalized2.append(data_segments[7]);
                normalized2.append(" "); //$NON-NLS-1$

                normalized2.append(data_segments[2]);
                normalized2.append(" "); //$NON-NLS-1$
                normalized2.append(data_segments[3]);
                normalized2.append(" "); //$NON-NLS-1$
                normalized2.append(data_segments[4]);

                String normalizedLine2 = normalized2.toString().trim();

                if (lines.contains(normalizedLine2)) {
                    source = setLine(lineNumber, "<rm>", source); //$NON-NLS-1$
                } else {
                    source = setLine(lineNumber, trimmedLine, source);
                    lines.add(normalizedLine2);
                    elc = 0;
                    lastCommentLine = null;
                }
            }
        }
        break;
        case 3:
        {
            if (lines.contains(normalizedLine)) {
                source = setLine(lineNumber, "<rm>", source); //$NON-NLS-1$
            } else {
                lines.add(normalizedLine);
                StringBuilder normalized2 = new StringBuilder();

                normalized2.append(data_segments[0]);
                normalized2.append(" "); //$NON-NLS-1$
                normalized2.append(data_segments[1]);
                normalized2.append(" "); //$NON-NLS-1$

                normalized2.append(data_segments[8]);
                normalized2.append(" "); //$NON-NLS-1$
                normalized2.append(data_segments[9]);
                normalized2.append(" "); //$NON-NLS-1$
                normalized2.append(data_segments[10]);
                normalized2.append(" "); //$NON-NLS-1$

                normalized2.append(data_segments[2]);
                normalized2.append(" "); //$NON-NLS-1$
                normalized2.append(data_segments[3]);
                normalized2.append(" "); //$NON-NLS-1$
                normalized2.append(data_segments[4]);
                normalized2.append(" "); //$NON-NLS-1$

                normalized2.append(data_segments[5]);
                normalized2.append(" "); //$NON-NLS-1$
                normalized2.append(data_segments[6]);
                normalized2.append(" "); //$NON-NLS-1$
                normalized2.append(data_segments[7]);

                String normalizedLine2 = normalized2.toString().trim();

                if (lines.contains(normalizedLine2)) {
                    source = setLine(lineNumber, "<rm>", source); //$NON-NLS-1$
                } else {
                    lines.add(normalizedLine2);
                    StringBuilder normalized3 = new StringBuilder();

                    normalized3.append(data_segments[0]);
                    normalized3.append(" "); //$NON-NLS-1$
                    normalized3.append(data_segments[1]);
                    normalized3.append(" "); //$NON-NLS-1$

                    normalized3.append(data_segments[5]);
                    normalized3.append(" "); //$NON-NLS-1$
                    normalized3.append(data_segments[6]);
                    normalized3.append(" "); //$NON-NLS-1$
                    normalized3.append(data_segments[7]);
                    normalized3.append(" "); //$NON-NLS-1$

                    normalized3.append(data_segments[8]);
                    normalized3.append(" "); //$NON-NLS-1$
                    normalized3.append(data_segments[9]);
                    normalized3.append(" "); //$NON-NLS-1$
                    normalized3.append(data_segments[10]);
                    normalized3.append(" "); //$NON-NLS-1$

                    normalized3.append(data_segments[2]);
                    normalized3.append(" "); //$NON-NLS-1$
                    normalized3.append(data_segments[3]);
                    normalized3.append(" "); //$NON-NLS-1$
                    normalized3.append(data_segments[4]);

                    String normalizedLine3 = normalized3.toString().trim();
                    if (lines.contains(normalizedLine3)) {
                        source = setLine(lineNumber, "<rm>", source); //$NON-NLS-1$
                    } else {
                        source = setLine(lineNumber, trimmedLine, source);
                        lines.add(normalizedLine3);
                        elc = 0;
                        lastCommentLine = null;
                    }
                }
            }
        }
        break;
        case 4:
        {
            if (lines.contains(normalizedLine)) {
                source = setLine(lineNumber, "<rm>", source); //$NON-NLS-1$
            } else {
                lines.add(normalizedLine);
                StringBuilder normalized2 = new StringBuilder();

                normalized2.append(data_segments[0]);
                normalized2.append(" "); //$NON-NLS-1$
                normalized2.append(data_segments[1]);
                normalized2.append(" "); //$NON-NLS-1$

                normalized2.append(data_segments[11]);
                normalized2.append(" "); //$NON-NLS-1$
                normalized2.append(data_segments[12]);
                normalized2.append(" "); //$NON-NLS-1$
                normalized2.append(data_segments[13]);
                normalized2.append(" "); //$NON-NLS-1$

                normalized2.append(data_segments[2]);
                normalized2.append(" "); //$NON-NLS-1$
                normalized2.append(data_segments[3]);
                normalized2.append(" "); //$NON-NLS-1$
                normalized2.append(data_segments[4]);
                normalized2.append(" "); //$NON-NLS-1$

                normalized2.append(data_segments[5]);
                normalized2.append(" "); //$NON-NLS-1$
                normalized2.append(data_segments[6]);
                normalized2.append(" "); //$NON-NLS-1$
                normalized2.append(data_segments[7]);
                normalized2.append(" "); //$NON-NLS-1$

                normalized2.append(data_segments[8]);
                normalized2.append(" "); //$NON-NLS-1$
                normalized2.append(data_segments[9]);
                normalized2.append(" "); //$NON-NLS-1$
                normalized2.append(data_segments[10]);

                String normalizedLine2 = normalized2.toString().trim();

                if (lines.contains(normalizedLine2)) {
                    source = setLine(lineNumber, "<rm>", source); //$NON-NLS-1$
                } else {
                    lines.add(normalizedLine2);
                    StringBuilder normalized3 = new StringBuilder();

                    normalized3.append(data_segments[0]);
                    normalized3.append(" "); //$NON-NLS-1$
                    normalized3.append(data_segments[1]);
                    normalized3.append(" "); //$NON-NLS-1$

                    normalized3.append(data_segments[8]);
                    normalized3.append(" "); //$NON-NLS-1$
                    normalized3.append(data_segments[9]);
                    normalized3.append(" "); //$NON-NLS-1$
                    normalized3.append(data_segments[10]);
                    normalized3.append(" "); //$NON-NLS-1$

                    normalized3.append(data_segments[11]);
                    normalized3.append(" "); //$NON-NLS-1$
                    normalized3.append(data_segments[12]);
                    normalized3.append(" "); //$NON-NLS-1$
                    normalized3.append(data_segments[13]);
                    normalized3.append(" "); //$NON-NLS-1$

                    normalized3.append(data_segments[2]);
                    normalized3.append(" "); //$NON-NLS-1$
                    normalized3.append(data_segments[3]);
                    normalized3.append(" "); //$NON-NLS-1$
                    normalized3.append(data_segments[4]);
                    normalized3.append(" "); //$NON-NLS-1$

                    normalized3.append(data_segments[5]);
                    normalized3.append(" "); //$NON-NLS-1$
                    normalized3.append(data_segments[6]);
                    normalized3.append(" "); //$NON-NLS-1$
                    normalized3.append(data_segments[7]);

                    String normalizedLine3 = normalized3.toString().trim();
                    if (lines.contains(normalizedLine3)) {
                        source = setLine(lineNumber, "<rm>", source); //$NON-NLS-1$
                    } else {
                        lines.add(normalizedLine3);
                        StringBuilder normalized4 = new StringBuilder();

                        normalized4.append(data_segments[0]);
                        normalized4.append(" "); //$NON-NLS-1$
                        normalized4.append(data_segments[1]);
                        normalized4.append(" "); //$NON-NLS-1$

                        normalized4.append(data_segments[5]);
                        normalized4.append(" "); //$NON-NLS-1$
                        normalized4.append(data_segments[6]);
                        normalized4.append(" "); //$NON-NLS-1$
                        normalized4.append(data_segments[7]);
                        normalized4.append(" "); //$NON-NLS-1$

                        normalized4.append(data_segments[8]);
                        normalized4.append(" "); //$NON-NLS-1$
                        normalized4.append(data_segments[9]);
                        normalized4.append(" "); //$NON-NLS-1$
                        normalized4.append(data_segments[10]);
                        normalized4.append(" "); //$NON-NLS-1$

                        normalized4.append(data_segments[11]);
                        normalized4.append(" "); //$NON-NLS-1$
                        normalized4.append(data_segments[12]);
                        normalized4.append(" "); //$NON-NLS-1$
                        normalized4.append(data_segments[13]);
                        normalized4.append(" "); //$NON-NLS-1$

                        normalized4.append(data_segments[2]);
                        normalized4.append(" "); //$NON-NLS-1$
                        normalized4.append(data_segments[3]);
                        normalized4.append(" "); //$NON-NLS-1$
                        normalized4.append(data_segments[4]);

                        String normalizedLine4 = normalized4.toString().trim();
                        if (lines.contains(normalizedLine4)) {
                            source = setLine(lineNumber, "<rm>", source); //$NON-NLS-1$
                        } else {
                            source = setLine(lineNumber, trimmedLine, source);
                            lines.add(normalizedLine4);
                            elc = 0;
                            lastCommentLine = null;
                        }
                    }
                }
            }
        }
        break;
        case 5:
        {
            GData5 gd5 = (GData5) gd;

            Vector4f p1 = MathHelper.getNearestPointToLine(gd5.x1, gd5.y1, gd5.z1, gd5.x2, gd5.y2, gd5.z2, gd5.x3, gd5.y3, gd5.z3);
            Vector4f p2 = MathHelper.getNearestPointToLine(gd5.x1, gd5.y1, gd5.z1, gd5.x2, gd5.y2, gd5.z2, gd5.x4, gd5.y4, gd5.z4);

            Vector3f pa1 = Vector3f.sub(new Vector3f(gd5.x3, gd5.y3, gd5.z3), new Vector3f(p1.x, p1.y, p1.z), null);
            Vector3f pa2 = Vector3f.sub(new Vector3f(gd5.x4, gd5.y4, gd5.z4), new Vector3f(p2.x, p2.y, p2.z), null);

            float a = (float) (Vector3f.angle(pa1, pa2) / Math.PI * 180.0);


            if (a > Threshold.condline_angle_maximum) {
                source = setLine(lineNumber, "<rm>", source); //$NON-NLS-1$
            } else {
                StringBuilder normalized3 = new StringBuilder();

                normalized3.append(data_segments[0]);
                normalized3.append(" "); //$NON-NLS-1$
                normalized3.append(data_segments[1]);
                normalized3.append(" "); //$NON-NLS-1$

                normalized3.append(data_segments[2]);
                normalized3.append(" "); //$NON-NLS-1$
                normalized3.append(data_segments[3]);
                normalized3.append(" "); //$NON-NLS-1$
                normalized3.append(data_segments[4]);
                normalized3.append(" "); //$NON-NLS-1$

                normalized3.append(data_segments[5]);
                normalized3.append(" "); //$NON-NLS-1$
                normalized3.append(data_segments[6]);
                normalized3.append(" "); //$NON-NLS-1$
                normalized3.append(data_segments[7]);
                normalized3.append(" "); //$NON-NLS-1$

                String normalizedLine3 = normalized3.toString().trim();

                if (lines.contains(normalizedLine3)) {
                    source = setLine(lineNumber, "<rm>", source); //$NON-NLS-1$
                } else {
                    lines.add(normalizedLine3);
                    StringBuilder normalized2 = new StringBuilder();

                    normalized2.append(data_segments[0]);
                    normalized2.append(" "); //$NON-NLS-1$
                    normalized2.append(data_segments[1]);
                    normalized2.append(" "); //$NON-NLS-1$

                    normalized2.append(data_segments[5]);
                    normalized2.append(" "); //$NON-NLS-1$
                    normalized2.append(data_segments[6]);
                    normalized2.append(" "); //$NON-NLS-1$
                    normalized2.append(data_segments[7]);
                    normalized2.append(" "); //$NON-NLS-1$

                    normalized2.append(data_segments[2]);
                    normalized2.append(" "); //$NON-NLS-1$
                    normalized2.append(data_segments[3]);
                    normalized2.append(" "); //$NON-NLS-1$
                    normalized2.append(data_segments[4]);
                    normalized2.append(" "); //$NON-NLS-1$

                    String normalizedLine2 = normalized2.toString().trim();

                    if (lines.contains(normalizedLine2)) {
                        source = setLine(lineNumber, "<rm>", source); //$NON-NLS-1$
                    } else {
                        source = setLine(lineNumber, trimmedLine, source);
                        lines.add(normalizedLine2);
                        elc = 0;
                        lastCommentLine = null;
                    }
                }
            }
        }
        break;
        default:
            if (trimmedLine.isEmpty()) {
                elc++;
                if (elc < 3) {
                    source = setLine(lineNumber, trimmedLine, source);
                } else {
                    source = setLine(lineNumber, "<rm>", source); //$NON-NLS-1$
                }
            } else {
                if (lastCommentLine != null && lastCommentLine.equals(normalizedLine)) {
                    source = setLine(lineNumber, "<rm>", source); //$NON-NLS-1$
                } else {
                    elc = 0;
                    lastCommentLine = normalizedLine;
                    source = setLine(lineNumber, trimmedLine, source);
                }
            }
            break;
        }




        return source;
    }

}
