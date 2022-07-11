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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.custom.StyledText;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.text.HeaderState;
import org.nschmidt.ldparteditor.text.StringHelper;
import org.nschmidt.ldparteditor.widget.TreeItem;

/**
 * Fixes all detected problems
 *
 * @author nils
 *
 */
public enum QuickFixer {
    INSTANCE;

    /**
     * Fixes selected syntax and logic errors
     *
     * @param cText
     *            the selected CompositeText
     * @param issues
     *            the selected Issues
     * @param datFile
     */
    public static void fixTextIssues(StyledText cText, Set<TreeItem> issues, DatFile datFile) {

        if (datFile.isReadOnly() || issues.isEmpty())
            return;

        List<Integer> lineNumbers = new ArrayList<>();
        Map<Integer, Set<TreeItem>> issuesInLine = calculateIssuesInLine(cText, issues, lineNumbers);

        if (lineNumbers.isEmpty()) return;

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

        HeaderState h = datFile.getDatHeader().getState().createClone();

        for (Integer l : lineNumbers) {

            for (TreeItem t : issuesInLine.get(l)) {
                NLogger.debug(QuickFixer.class, "Fixing: {0}{1}]", t.getText(2).substring(0, 2), Integer.parseInt(t.getText(2).substring(2, 4), 16)); //$NON-NLS-1$

                String type = t.getText(2).substring(1, 2);
                String sort = t.getText(2).substring(2, 4);

                String line = getLine(l, text2);

                if ("H".equals(type)) { //$NON-NLS-1$
                    text2 = HintFixer.fix(l, sort, text2, datFile, h);
                } else if ("W".equals(type)) { //$NON-NLS-1$
                    text2 = WarningFixer.fix(l, sort, line, text2, t.getText(0), datFile);
                } else if ("E".equals(type)) { //$NON-NLS-1$
                    text2 = ErrorFixer.fix(l, sort, line, text2, datFile, cText.getShell());
                }

                NLogger.debug(QuickFixer.class, line);

            }
        }
        datFile.getVertexManager().setModified(false, true);
        cText.setText(restoreLineTermination(text2));
        int tl = cText.getText().length();
        try {
            cText.setSelection(Math.min(c, tl));
        } catch (Exception e) {
            cText.setSelection(0);
        }

    }

    private static Map<Integer, Set<TreeItem>> calculateIssuesInLine(StyledText cText, Set<TreeItem> issues, List<Integer> lineNumbers) {
        final Map<Integer, Set<TreeItem>> issuesInLine = new HashMap<>();
        final Set<Integer> numbers = new HashSet<>();
        
        for (TreeItem t : issues) {
            if (t == null || t.getText(0).isEmpty())
                continue;
            int offset = (Integer) t.getData();
            Integer i = offset > -1 ? cText.getLineAtOffset(offset) : offset * -1;
            if (!numbers.contains(i)) {
                numbers.add(i);
                lineNumbers.add(i);
                issuesInLine.put(i, new HashSet<>());
            }
            issuesInLine.get(i).add(t);
        }
        
        return issuesInLine;
    }

    static String insertBeforeLine(int line, String textToInsert, String text) {
        return text.replaceFirst("<L" + line + ">", textToInsert + "<L" + line + ">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    static String setLine(int line, String textToSet, String text) {
        return text.replaceFirst("<L" + line + ">.*</L" + line + ">", "<L" + line + ">" + Matcher.quoteReplacement(textToSet) + "</L" + line + ">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
    }

    static String getLine(int line, String text) {
        line++;
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
        if (line == 0) {
            return insertBeforeLine(1, textToInsert, text);
        }
        return text.replaceFirst("</L" + line + ">", "</L" + line + ">" + textToInsert); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    static String restoreLineTermination(String text) {
        text = text.replaceAll("<L[0-9]+><rm></L[0-9]+>", ""); //$NON-NLS-1$ //$NON-NLS-2$
        text = text.replaceAll("</L[0-9]+>", "<br>"); //$NON-NLS-1$ //$NON-NLS-2$
        text = text.replaceAll("<L[0-9]+>", ""); //$NON-NLS-1$ //$NON-NLS-2$
        final int tl = text.length();
        if (tl > 3) text = text.substring(0, tl - 4);
        return text.replace("<br>", StringHelper.getLineDelimiter()); //$NON-NLS-1$
    }
}
