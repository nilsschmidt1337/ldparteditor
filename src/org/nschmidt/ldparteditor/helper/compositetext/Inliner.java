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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.custom.StyledText;
import org.nschmidt.ldparteditor.data.BFC;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GDataBFC;
import org.nschmidt.ldparteditor.enumtype.LDConfig;
import org.nschmidt.ldparteditor.helper.math.HashBiMap;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.text.StringHelper;

/**
 * Inlines selected lines
 *
 * @author nils
 *
 */
public enum Inliner {
    INSTANCE;

    private static BFC bfcStatusTarget = BFC.NOCERTIFY;
    private static boolean hasINVERTNEXT = false;

    public static boolean withSubfileReference = false;
    public static boolean recursively = false;
    public static boolean noComment = false;
    public static DatFile datfile;

    /**
     * Inlines selected lines (clears the selection)
     *
     * @param cText
     *            the selected CompositeText
     * @param lineStart
     *            start line number to inline
     * @param lineEnd
     *            end line number to inline
     * @param datFile
     */
    public static void inline(StyledText cText, int lineStart, int lineEnd, DatFile datFile) {

        if (datFile.isReadOnly())
            return;

        datFile.getVertexManager().clearSelection();

        bfcStatusTarget = BFC.NOCERTIFY;
        hasINVERTNEXT = false;

        HashBiMap<Integer, GData> dpl = datFile.getDrawPerLineNoClone();

        Set<Integer> keys = dpl.keySet();

        for (Integer key : keys) {
            GData gd = dpl.getValue(key);
            switch (gd.type()) {
            case 6:
                GDataBFC gbfc = (GDataBFC) gd;
                BFC t = gbfc.getType();
                if (t == BFC.CCW_CLIP || t == BFC.CCW) {
                    bfcStatusTarget = BFC.CCW_CLIP;
                } else if (t == BFC.CW_CLIP || t == BFC.CW) {
                    bfcStatusTarget = BFC.CW_CLIP;
                } else if (t == BFC.NOCLIP) {
                    bfcStatusTarget = BFC.NOCLIP;
                } else if (t == BFC.INVERTNEXT) {
                    hasINVERTNEXT = true;
                    if (bfcStatusTarget == BFC.CCW_CLIP) {
                        bfcStatusTarget = BFC.CW_CLIP;
                    } else if (bfcStatusTarget == BFC.CW_CLIP) {
                        bfcStatusTarget = BFC.CCW_CLIP;
                    }
                }
                break;
            case 1:
                break;
            default:
                if (hasINVERTNEXT && !(gd.type() == 0 && gd.toString().trim().isEmpty())) {
                    hasINVERTNEXT = false;
                    if (bfcStatusTarget == BFC.CCW_CLIP || bfcStatusTarget == BFC.CCW) {
                        bfcStatusTarget = BFC.CW_CLIP;
                    } else if (bfcStatusTarget == BFC.CW_CLIP || bfcStatusTarget == BFC.CW) {
                        bfcStatusTarget = BFC.CCW_CLIP;
                    }
                }
                break;
            }
            if (key == lineStart) {
                if (key > 1) {
                    GData gd2 = dpl.getValue(key - 1);
                    if (gd2.type() == 6) {
                        GDataBFC gbfc2 = (GDataBFC) gd2;
                        BFC t2 = gbfc2.getType();
                        if (t2 == BFC.INVERTNEXT) {
                            hasINVERTNEXT = true;
                            if (bfcStatusTarget == BFC.CCW_CLIP) {
                                bfcStatusTarget = BFC.CW_CLIP;
                            } else if (bfcStatusTarget == BFC.CW_CLIP) {
                                bfcStatusTarget = BFC.CCW_CLIP;
                            }
                        }
                    }
                }
                break;
            }
        }

        List<Integer> lineNumbers = new ArrayList<>();
        lineEnd += 1;
        for (int line = lineStart; line < lineEnd; line++) {
            lineNumbers.add(line);
        }

        Collections.sort(lineNumbers);
        Collections.reverse(lineNumbers);

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
        Inliner.datfile = datFile;
        for (Integer l : lineNumbers) {
            String line = getLine(l, text2);
            NLogger.debug(Inliner.class, "Inlining: {0}", line); //$NON-NLS-1$
            text2 = Inliner.inline(l, line, text2, datFile, false);
        }
        Inliner.datfile = null;
        cText.setText(restoreLineTermination(text2));
        int tl = cText.getText().length();
        try {
            cText.setSelection(Math.min(c, tl));
        } catch (Exception e) {
            cText.setSelection(0);
        }

    }

    /**
     * Inlines selected lines (clears the selection)
     *
     * @param cText
     *            the selected CompositeText
     * @param lineStart
     *            start line number to inline
     * @param lineEnd
     *            end line number to inline
     * @param datFile
     */
    public static void inline(StyledText cText, List<Integer> lineNumbers, DatFile datFile) {

        if (datFile.isReadOnly())
            return;

        datFile.getVertexManager().clearSelection();

        bfcStatusTarget = BFC.NOCERTIFY;
        hasINVERTNEXT = false;

        HashBiMap<Integer, GData> dpl = datFile.getDrawPerLineNoClone();

        Map<Integer, BFC> bfcStatusToLine = new HashMap<>();

        Set<Integer> keys = dpl.keySet();

        for (Integer key : keys) {
            GData gd = dpl.getValue(key);
            switch (gd.type()) {
            case 6:
                GDataBFC gbfc = (GDataBFC) gd;
                BFC t = gbfc.getType();
                if (t == BFC.CCW_CLIP || t == BFC.CCW) {
                    bfcStatusTarget = BFC.CCW_CLIP;
                } else if (t == BFC.CW_CLIP || t == BFC.CW) {
                    bfcStatusTarget = BFC.CW_CLIP;
                } else if (t == BFC.NOCLIP) {
                    bfcStatusTarget = BFC.NOCLIP;
                } else if (t == BFC.INVERTNEXT) {
                    hasINVERTNEXT = true;
                    if (bfcStatusTarget == BFC.CCW_CLIP) {
                        bfcStatusTarget = BFC.CW_CLIP;
                    } else if (bfcStatusTarget == BFC.CW_CLIP) {
                        bfcStatusTarget = BFC.CCW_CLIP;
                    }
                }
                break;
            case 1:
                break;
            default:
                if (hasINVERTNEXT && !(gd.type() == 0 && gd.toString().trim().isEmpty())) {
                    hasINVERTNEXT = false;
                    if (bfcStatusTarget == BFC.CCW_CLIP || bfcStatusTarget == BFC.CCW) {
                        bfcStatusTarget = BFC.CW_CLIP;
                    } else if (bfcStatusTarget == BFC.CW_CLIP || bfcStatusTarget == BFC.CW) {
                        bfcStatusTarget = BFC.CCW_CLIP;
                    }
                }
                break;
            }
            bfcStatusToLine.put(key, bfcStatusTarget);
        }

        Collections.sort(lineNumbers);
        Collections.reverse(lineNumbers);

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
        for (Integer l : lineNumbers) {
            bfcStatusTarget = bfcStatusToLine.get(l);
            String line = getLine(l, text2);
            NLogger.debug(Inliner.class, "Inlining: {0}", line); //$NON-NLS-1$
            text2 = Inliner.inline(l, line, text2, datFile, true);
        }
        cText.setText(restoreLineTermination(text2));
        int tl = cText.getText().length();
        try {
            cText.setSelection(Math.min(c, tl));
        } catch (Exception e) {
            cText.setSelection(0);
        }

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

    static String restoreLineTermination(String text) {
        text = text.replaceAll("<L[0-9]+><rm></L[0-9]+>", ""); //$NON-NLS-1$ //$NON-NLS-2$
        text = text.replaceAll("</L[0-9]+>", "<br>"); //$NON-NLS-1$ //$NON-NLS-2$
        text = text.replaceAll("<L[0-9]+>", ""); //$NON-NLS-1$ //$NON-NLS-2$
        final int tl = text.length();
        if (tl > 3) text = text.substring(0, tl - 4);
        return text.replace("<br>", StringHelper.getLineDelimiter()); //$NON-NLS-1$
    }

    private static String inline(Integer lineNumber, String line, String source, DatFile datFile, boolean removeTrailingBreak) {

        GData gd = datFile.getDrawPerLineNoClone().getValue(lineNumber);
        switch (gd.type()) {
        case 6:
            GDataBFC gbfc = (GDataBFC) gd;
            BFC t = gbfc.getType();
            if (t == BFC.CCW_CLIP || t == BFC.CCW) {
                bfcStatusTarget = BFC.CCW_CLIP;
            } else if (t == BFC.CW_CLIP || t == BFC.CW) {
                bfcStatusTarget = BFC.CW_CLIP;
            } else if (t == BFC.NOCLIP) {
                bfcStatusTarget = BFC.NOCLIP;
            } else if (t == BFC.INVERTNEXT) {
                hasINVERTNEXT = true;
                if (bfcStatusTarget == BFC.CCW_CLIP || bfcStatusTarget == BFC.CCW) {
                    bfcStatusTarget = BFC.CW_CLIP;
                } else if (bfcStatusTarget == BFC.CW_CLIP || bfcStatusTarget == BFC.CW) {
                    bfcStatusTarget = BFC.CCW_CLIP;
                }
            }
            break;
        case 1:
            break;
        default:
            if (hasINVERTNEXT && !line.trim().isEmpty()) {
                hasINVERTNEXT = false;
                if (bfcStatusTarget == BFC.CCW_CLIP || bfcStatusTarget == BFC.CCW) {
                    bfcStatusTarget = BFC.CW_CLIP;
                } else if (bfcStatusTarget == BFC.CW_CLIP || bfcStatusTarget == BFC.CW) {
                    bfcStatusTarget = BFC.CCW_CLIP;
                }
            }
            break;
        }

        if (removeTrailingBreak) {
            String inlinedString = gd.inlinedString(bfcStatusTarget, LDConfig.getColour16());
            int end = inlinedString.length();
            if (inlinedString.endsWith("<br>")) { //$NON-NLS-1$
                end -= 4;
            }
            inlinedString = inlinedString.substring(0, end);
            source = setLine(lineNumber, inlinedString, source);
        } else {
            source = setLine(lineNumber, gd.inlinedString(bfcStatusTarget, LDConfig.getColour16()), source);
        }

        if (hasINVERTNEXT && gd.type() == 1) {
            hasINVERTNEXT = false;
            if (bfcStatusTarget == BFC.CCW_CLIP || bfcStatusTarget == BFC.CCW) {
                bfcStatusTarget = BFC.CW_CLIP;
            } else if (bfcStatusTarget == BFC.CW_CLIP || bfcStatusTarget == BFC.CW) {
                bfcStatusTarget = BFC.CCW_CLIP;
            }
        }

        return source;
    }

}
