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
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.widget.TreeItem;

/**
 * Copies all detected problems to the clipboard
 */
public enum Issue2ClipboardCopier {
    INSTANCE;

    /**
     * Copies selected syntax and logic errors to the clipboard
     *
     * @param cText
     *            the selected CompositeText
     * @param issues
     *            the selected Issues
     */
    public static void copyTextIssues(StyledText cText, Set<TreeItem> issues) {

        if (issues.isEmpty())
            return;

        List<Integer> lineNumbers = new ArrayList<>();
        Map<Integer, Set<TreeItem>> issuesInLine = calculateIssuesInLine(cText, issues, lineNumbers);

        if (lineNumbers.isEmpty()) return;

        Collections.sort(lineNumbers);

        final StringBuilder sbWarningsErrors = new StringBuilder();
        final StringBuilder sbHints = new StringBuilder();
        for (Integer l : lineNumbers) {
            for (TreeItem issue : issuesInLine.get(l)) {
                if (issue != null && issue.getData() != null) {
                    if (!"---".equals(issue.getText(1))) { //$NON-NLS-1$
                        int firstBracket = issue.getText(1).indexOf('[');
                        if (firstBracket == -1) firstBracket = issue.getText(1).length();
                        String lineNumber = issue.getText(1).substring(0, firstBracket);
                        sbWarningsErrors.append(lineNumber);
                        for (int i = 0; i < (10 - lineNumber.length()); i++) {
                            sbWarningsErrors.append(' ');
                        }

                        sbWarningsErrors.append(" : "); //$NON-NLS-1$

                        int firstLineBreak = issue.getText(0).indexOf('\n');
                        if (firstLineBreak == -1) firstLineBreak = issue.getText(0).length();
                        sbWarningsErrors.append(issue.getText(0).substring(0, firstLineBreak));
                        sbWarningsErrors.append("\r\n"); //$NON-NLS-1$
                    } else {
                        int firstLineBreak = issue.getText(0).indexOf('\n');
                        if (firstLineBreak == -1) firstLineBreak = issue.getText(0).length();
                        sbHints.append(" - "); //$NON-NLS-1$
                        sbHints.append(issue.getText(0).substring(0, firstLineBreak));
                        sbHints.append("\r\n"); //$NON-NLS-1$
                    }
                }
            }
        }

        sbWarningsErrors.insert(0, sbHints.toString());

        NLogger.debug(Issue2ClipboardCopier.class, "Copy text {0}", sbWarningsErrors.toString()); //$NON-NLS-1$

        Clipboard clipboard2 = new Clipboard(Display.getCurrent());
        clipboard2.setContents(new Object[] { sbWarningsErrors.toString() }, new Transfer[] { TextTransfer.getInstance() });
        clipboard2.dispose();
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
}
