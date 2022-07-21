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
import java.util.Set;

import org.eclipse.swt.custom.StyledText;
import org.nschmidt.ldparteditor.logger.NLogger;

/**
 * Splits selected quads into triangles
 */
public enum QuadSplitter {
    INSTANCE;

    public static void splitQuadsIntoTriangles(StyledText st, int fromLine, int toLine, DatFile fileNameObj) {

        // Backup selection range
        final int x = st.getSelectionRange().x;
        final int y = st.getSelectionRange().y;

        VertexManager vm = fileNameObj.getVertexManager();
        vm.clearSelection();

        Set<GData4> selectedQuads = new HashSet<>();

        GData data2draw = fileNameObj.getDrawChainStart();
        int lineCount = 0;
        while ((data2draw = data2draw.getNext()) != null) {
            lineCount += 1;
            if (data2draw.type() == 4 && lineCount >= fromLine && lineCount <= toLine) {
                selectedQuads.add((GData4) data2draw);
            }
        }

        vm.getSelectedQuads().addAll(selectedQuads);
        vm.getSelectedData().addAll(selectedQuads);

        vm.splitQuads(false);

        if (vm.isModified()) {
            st.setText(fileNameObj.getText());
        }

        // Restore selection range

        try {
            st.setSelectionRange(x, y);
        } catch (IllegalArgumentException iae1) {
            try {
                st.setSelectionRange(x - 1, y);
            } catch (IllegalArgumentException iae2) {
                try {
                    st.setSelectionRange(x - 2, y);
                } catch (IllegalArgumentException consumed) {
                    NLogger.debug(QuadSplitter.class, consumed);
                }
            }
        }

        st.showSelection();
    }
}
