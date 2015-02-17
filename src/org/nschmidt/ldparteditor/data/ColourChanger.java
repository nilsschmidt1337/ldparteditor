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

import org.eclipse.swt.custom.StyledText;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.text.StringHelper;

/**
 * Sorts selected lines by a given criteria
 *
 * @author nils
 *
 */
public enum ColourChanger {
    INSTANCE;

    public static void changeColour(StyledText st, int fromLine, int toLine, DatFile fileNameObj, int colourNumber, float r, float g, float b) {

        // Backup selection range
        final int x = st.getSelectionRange().x;
        final int y = st.getSelectionRange().y;

        // Replace colours

        StringBuilder colourBuilder = new StringBuilder();
        if (colourNumber == -1) {
            colourBuilder.append("0x2"); //$NON-NLS-1$
            colourBuilder.append(MathHelper.toHex((int) (255f * r)).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * g)).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * b)).toUpperCase());
        } else {
            colourBuilder.append(colourNumber);
        }
        String col = colourBuilder.toString();
        StringBuilder newDatText = new StringBuilder();
        final String ld = StringHelper.getLineDelimiter();
        GData data2draw = fileNameObj.getDrawChainStart();
        int lineCount = 0;
        while ((data2draw = data2draw.getNext()) != null) {
            lineCount += 1;
            if (lineCount >= fromLine && lineCount <= toLine) {
                switch (data2draw.type()) {
                case 1:
                {
                    GData1 gd = (GData1) data2draw;
                    newDatText.append(gd.colourReplace(col));
                }
                break;
                case 2:
                {
                    GData2 gd = (GData2) data2draw;
                    newDatText.append(gd.colourReplace(col));
                }
                break;
                case 3:
                {
                    GData3 gd = (GData3) data2draw;
                    newDatText.append(gd.colourReplace(col));
                }
                break;
                case 4:
                {
                    GData4 gd = (GData4) data2draw;
                    newDatText.append(gd.colourReplace(col));
                }
                break;
                case 5:
                {
                    GData5 gd = (GData5) data2draw;
                    newDatText.append(gd.colourReplace(col));
                }
                break;
                default:
                    newDatText.append(data2draw.toString());
                    break;
                }
            } else {
                newDatText.append(data2draw.toString());
            }

            if (data2draw.getNext() != null) newDatText.append(ld);
        }

        st.setText(newDatText.toString());


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
