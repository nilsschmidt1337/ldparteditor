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

import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.swt.custom.StyledText;
import org.nschmidt.ldparteditor.helpers.compositetext.Inliner;

/**
 * Splits selected quads into triangles
 *
 * @author nils
 *
 */
public enum Unrectifier {
    INSTANCE;

    public static void splitAllIntoTriangles(StyledText st, DatFile fileNameObj, boolean splitQuads) {

        // Backup selection range
        final int x = st.getSelectionRange().x;
        final int y = st.getSelectionRange().y;

        ArrayList<Integer> lineNumbers = new ArrayList<>();

        HashSet<String> rectPrimitives = new HashSet<>();
        rectPrimitives.add("rect.dat"); //$NON-NLS-1$
        rectPrimitives.add("rect1.dat"); //$NON-NLS-1$
        rectPrimitives.add("rect2a.dat"); //$NON-NLS-1$
        rectPrimitives.add("rect2p.dat"); //$NON-NLS-1$
        rectPrimitives.add("rect3.dat"); //$NON-NLS-1$

        int lineCount = 0;
        GData data2draw = fileNameObj.getDrawChainStart();
        while ((data2draw = data2draw.getNext()) != null) {
            lineCount += 1;
            switch (data2draw.type()) {
            case 1:
                GData1 g1 = (GData1) data2draw;
                if (rectPrimitives.contains(g1.shortName.trim().toLowerCase())) {
                    lineNumbers.add(lineCount);
                }
                break;
            default:
            }
        }

        Inliner.withSubfileReference = false;
        Inliner.recursively = true;
        Inliner.noComment = true;

        Inliner.inline(st, lineNumbers, fileNameObj);

        VertexManager vm = fileNameObj.getVertexManager();
        vm.clearSelection();

        if (splitQuads) {
            HashSet<GData4> selectedQuads = new HashSet<>();

            data2draw = fileNameObj.getDrawChainStart();
            while ((data2draw = data2draw.getNext()) != null) {
                switch (data2draw.type()) {
                case 4:
                    selectedQuads.add((GData4) data2draw);
                    break;
                default:
                }
            }

            vm.getSelectedQuads().addAll(selectedQuads);
            vm.getSelectedData().addAll(selectedQuads);

            vm.splitQuads(false);
        }

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
                } catch (IllegalArgumentException consumed) {}
            }
        }

        st.showSelection();
    }
}
