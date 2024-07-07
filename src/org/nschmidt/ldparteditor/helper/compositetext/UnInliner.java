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

import org.eclipse.swt.custom.StyledText;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.Matrix;
import org.nschmidt.ldparteditor.helper.math.HashBiMap;
import org.nschmidt.ldparteditor.text.DatParser;

/**
 * Un-Inlines selected linked lines
 */
public enum UnInliner {
    INSTANCE;

    private static final String INLINE = "INLINE"; //$NON-NLS-1$

    /**
     * Un-Inlines selected linked lines (clears the selection)
     *
     * @param cText
     *            the selected CompositeText
     * @param lineStart
     *            start line number to un-inline
     * @param lineEnd
     *            end line number to un-inline
     * @param datFile
     */
    public static void unInline(StyledText cText, int lineStart, int lineEnd, DatFile datFile) {

        if (datFile.isReadOnly())
            return;

        // Clear the selection
        datFile.getVertexManager().clearSelection();

        // Compile all linked subfile data
        SubfileCompiler.compile(datFile, false, false);

        HashBiMap<Integer, GData> dpl = datFile.getDrawPerLineNoClone();

        String text = cText.getText();
        String text2 = text;
        int c = cText.getCaretOffset();

        // Set the identifiers for each line
        text2 = "<L1>" + text2; //$NON-NLS-1$
        if (text2.contains("\r\n")) { //$NON-NLS-1$ Windows line termination
            text2 = text2.replace("\r\n", "#!%"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (text2.contains("\n")) { //$NON-NLS-1$ Linux/Mac line termination
            text2 = text2.replace("\n", "#!%"); //$NON-NLS-1$ //$NON-NLS-2$
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

        int foundLpeInline = 0;

        if (lineStart == lineEnd) {
            lineEnd += 1;
        }

        for (int line = lineStart; line < lineEnd; line++) {
            final GData data = dpl.getValue(line);
            if (data == null) break;
            if (data.type() == 0) {
                final String lineString = data.toString();
                if (isLpeInline(lineString)) {
                    text2 = QuickFixer.setLine(line, removeLpeInline(lineString), text2);
                    foundLpeInline += 1;
                } else if (foundLpeInline > 0 && isLpeInlineEnd(lineString)) {
                    text2 = QuickFixer.setLine(line, "<rm>", text2); //$NON-NLS-1$
                    foundLpeInline -= 1;
                } else if (foundLpeInline > 0) {
                    text2 = QuickFixer.setLine(line, "<rm>", text2); //$NON-NLS-1$
                }
            } else if (foundLpeInline > 0) {
                text2 = QuickFixer.setLine(line, "<rm>", text2); //$NON-NLS-1$
            }

            // Expand the selection if the last LPE INLINE_END is not part of the selection
            if (foundLpeInline > 0 && lineEnd <= line + 1) {
                lineEnd += 1;
            }
        }

        datFile.getVertexManager().setModified(false, true);
        cText.setSelection(0);
        cText.setText(QuickFixer.restoreLineTermination(text2));
        int tl = cText.getText().length();
        try {
            cText.setSelection(Math.min(c, tl));
        } catch (Exception e) {
            cText.setSelection(0);
        }
    }

    private static boolean isLpeInline(String line) {
        String[] dataSegments = line.trim().split("\\s+"); //$NON-NLS-1$
        // Check for 0 !LPE INLINE 1 Colour m1 m2 m3 m4 m5 m6 m7
        // m8 m9 m10 m11 m12 path
        if (dataSegments.length >= 18 && dataSegments[2].equals(INLINE) &&
                dataSegments[3].equals("1") && //$NON-NLS-1$
                dataSegments[1].equals("!LPE") && dataSegments[0].equals("0")) { //$NON-NLS-1$ //$NON-NLS-2$
            GColour c = DatParser.validateColour(dataSegments[4], 0f, 0f, 0f, 1f);
            if (c != null) {
                Matrix theMatrix = DatParser.matrixFromStringsPrecise(dataSegments[5], dataSegments[6], dataSegments[7], dataSegments[8], dataSegments[9], dataSegments[10],
                        dataSegments[11], dataSegments[12], dataSegments[13], dataSegments[14], dataSegments[15], dataSegments[16]);
                return theMatrix != null;
            }
        }

        return false;
    }

    private static String removeLpeInline(String line) {
        final int inlineIndex = line.indexOf(INLINE);
        if (inlineIndex != -1) {
            return line.substring(inlineIndex + INLINE.length()).trim();
        }

        return line;
    }

    private static boolean isLpeInlineEnd(String line) {
        String[] dataSegments = line.trim().split("\\s+"); //$NON-NLS-1$
        return dataSegments.length == 3 && dataSegments[2].equals("INLINE_END") && //$NON-NLS-1$
        dataSegments[1].equals("!LPE") && dataSegments[0].equals("0"); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
