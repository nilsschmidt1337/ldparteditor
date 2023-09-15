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
import org.nschmidt.ldparteditor.logger.NLogger;

/**
 * Selects selected lines in the "3D view"
 */
public enum Text2SelectionConverter {
    INSTANCE;

    /**
     * Selects selected lines in the "3D view"
     *
     * @param st
     *            the selected StyledText, can be null.
     * @param lineStart
     *            start line number to annotate
     * @param lineEnd
     *            end line number to annotate
     * @param datFile
     */
    public static void convert(StyledText st, int lineStart, int lineEnd, DatFile datFile) {
        datFile.getVertexManager().clearSelection();
        
        if (st != null) {
            final boolean oneLine = lineStart == lineEnd;
            NLogger.debug(Text2SelectionConverter.class, "One line selected: " + oneLine); //$NON-NLS-1$
            final String selectionText = st.getSelectionText();
            
            // detect if the last line is just selected at the beginning if multiple lines are selected
            if (!oneLine && (selectionText.endsWith("\n") || selectionText.endsWith("\r"))) { //$NON-NLS-1$ //$NON-NLS-2$
                NLogger.debug(Text2SelectionConverter.class, "Removed last line from selection."); //$NON-NLS-1$
                lineEnd--;
            }
        }
        
        lineEnd++;
        for (int line = lineStart; line < lineEnd; line++) {
            datFile.getVertexManager().addTextLineToSelection(line);
        }
    }
}
