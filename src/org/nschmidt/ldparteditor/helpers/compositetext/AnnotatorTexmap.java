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
package org.nschmidt.ldparteditor.helpers.compositetext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.custom.StyledText;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GDataCSG;
import org.nschmidt.ldparteditor.text.StringHelper;

/**
 * Annotates selected lines
 *
 * @author nils
 *
 */
public enum AnnotatorTexmap {
    INSTANCE;

    private static int offset;

    /**
     * Annotates selected lines with the TEXMAP meta command (clears the selection)
     *
     * @param cText
     *            the selected CompositeText
     * @param lineStart
     *            start line number to annotate
     * @param lineEnd
     *            end line number to annotate
     * @param datFile
     */
    public static void annotate(StyledText cText, int lineStart, int lineEnd, DatFile datFile) {

        if (datFile.isReadOnly())
            return;

        offset = 0;

        datFile.getVertexManager().clearSelection();

        GDataCSG.resetCSG();
        GDataCSG.forceRecompile();

        ArrayList<Integer> lineNumbers = new ArrayList<Integer>();
        lineEnd += 1;
        for (int line = lineStart; line < lineEnd; line++) {
            lineNumbers.add(line);
        }

        Collections.sort(lineNumbers);
        Collections.reverse(lineNumbers);

        String text = cText.getText();
        String text2 = text;
        int c = cText.getSelectionRange().x + cText.getSelectionRange().y;

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
            text2 = AnnotatorTexmap.annotate(l, line, text2, datFile);
        }
        cText.setText(restoreLineTermination(text2));
        int tl = cText.getText().length();
        try {
            cText.setSelection(Math.min(c + offset, tl));
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

    private static String annotate(Integer lineNumber, String line, String source, DatFile datFile) {

        String[] data_segments = line.trim().split("\\s+"); //$NON-NLS-1$

        if (data_segments.length > 1) {
            if (data_segments[0].equals("0") && data_segments[1].equals("!:")) { //$NON-NLS-1$ //$NON-NLS-2$
                int state = 0;
                StringBuilder sb = new StringBuilder();
                char[] ca = line.toCharArray();
                for (char ch : ca) {
                    if (state < 4) {
                        if (state == 0 && ch == ' ') {
                        } else if (state == 0 && ch == '0') {
                            state++;
                        } else if (state == 0) {
                            state = 4;
                        } else if (state == 1 && ch == ' ') {
                        } else if (state == 1 && ch == '!') {
                            state++;
                        } else if (state == 1) {
                            state = 4;
                        } else if (state == 2 && ch == ':') {
                            state++;
                        } else if (state == 3) {
                            state = 4;
                        }
                        offset--;
                    } else {
                        sb.append(ch);
                    }
                }
                line = sb.toString();
            } else {
                line = "0 !: " + line; //$NON-NLS-1$
                offset += 5;
            }
        } else {
            line = "0 !: " + line; //$NON-NLS-1$
            offset += 5;
        }

        source = setLine(lineNumber, line, source);

        return source;
    }

}
