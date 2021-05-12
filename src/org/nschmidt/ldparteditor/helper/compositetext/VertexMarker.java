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

import java.math.BigDecimal;

import org.eclipse.swt.custom.StyledText;
import org.nschmidt.ldparteditor.composite.compositetab.CompositeTabState;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GData2;
import org.nschmidt.ldparteditor.data.GData3;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.text.StringHelper;

/**
 * Generates vertex mark footprints, which are highlighted within the editor
 *
 * @author nils
 *
 */
public enum VertexMarker {
    INSTANCE;

    public static void markTheVertex(CompositeTabState state, StyledText compositeText, DatFile datFile) {
        state.setReplacingVertex(false);
        int caretStart = compositeText.getSelection().x;
        int caretEnd = compositeText.getSelection().y;
        final int startLine = compositeText.getLineAtOffset(caretStart);
        final int endLine = compositeText.getLineAtOffset(caretEnd);
        BigDecimal x = null;
        BigDecimal y = null;
        BigDecimal z = null;
        if (startLine == endLine) {
            GData dataInLine = datFile.getDrawPerLine().getValue(startLine + 1);
            final int type = dataInLine.type();
            if (type != 1) {
                int off = compositeText.getOffsetAtLine(startLine);
                caretStart -= off;
                String line = compositeText.getLine(startLine);
                String[] dataSegments = line.trim().split("\\s+"); //$NON-NLS-1$
                boolean isDistanceOrProtractor = type == 2 && !((GData2) dataInLine).isLine
                        || type == 3 && !((GData3) dataInLine).isTriangle;
                if (isDistanceOrProtractor) {
                    for (int i = 1; i < dataSegments.length - 2; i++) {
                        dataSegments[i] = dataSegments[i + 2];
                    }
                }
                VertexManager vm = datFile.getVertexManager();
                Vertex vertexToReplace = null;
                try {
                    switch (type) {
                    case 0:
                        if (dataSegments.length == 6 && "0".equals(dataSegments[0]) && "!LPE".equals(dataSegments[1]) && "VERTEX".equals(dataSegments[2]) && caretStart > line.indexOf("VERTEX") + 6) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                            x = new BigDecimal(dataSegments[3]);
                            y = new BigDecimal(dataSegments[4]);
                            z = new BigDecimal(dataSegments[5]);
                            state.setReplacingVertex(true);
                            vertexToReplace = vm.getDeclaredVertices().get(dataInLine)[0];
                        }
                        break;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        int index2 = StringHelper.getIndexFromWhitespaces(line, caretStart) - (isDistanceOrProtractor ? 2 : 0);
                        if (index2 > 1) {
                            if (type > 3 && index2 > 10) {
                                x = new BigDecimal(dataSegments[11]);
                                y = new BigDecimal(dataSegments[12]);
                                z = new BigDecimal(dataSegments[13]);
                                if (type == 4)
                                    vertexToReplace = vm.getQuads().get(dataInLine)[3];
                                else if (type == 5)
                                    vertexToReplace = vm.getCondlines().get(dataInLine)[3];
                            } else if (type > 2 && index2 > 7) {
                                x = new BigDecimal(dataSegments[8]);
                                y = new BigDecimal(dataSegments[9]);
                                z = new BigDecimal(dataSegments[10]);
                                if (type == 3)
                                    vertexToReplace = vm.getTriangles().get(dataInLine)[2];
                                else if (type == 4)
                                    vertexToReplace = vm.getQuads().get(dataInLine)[2];
                                else if (type == 5)
                                    vertexToReplace = vm.getCondlines().get(dataInLine)[2];
                            } else if (index2 > 4) {
                                x = new BigDecimal(dataSegments[5]);
                                y = new BigDecimal(dataSegments[6]);
                                z = new BigDecimal(dataSegments[7]);
                                if (type == 2)
                                    vertexToReplace = vm.getLines().get(dataInLine)[1];
                                else if (type == 3)
                                    vertexToReplace = vm.getTriangles().get(dataInLine)[1];
                                else if (type == 4)
                                    vertexToReplace = vm.getQuads().get(dataInLine)[1];
                                else if (type == 5)
                                    vertexToReplace = vm.getCondlines().get(dataInLine)[1];
                            } else {
                                x = new BigDecimal(dataSegments[2]);
                                y = new BigDecimal(dataSegments[3]);
                                z = new BigDecimal(dataSegments[4]);
                                if (type == 2)
                                    vertexToReplace = vm.getLines().get(dataInLine)[0];
                                else if (type == 3)
                                    vertexToReplace = vm.getTriangles().get(dataInLine)[0];
                                else if (type == 4)
                                    vertexToReplace = vm.getQuads().get(dataInLine)[0];
                                else if (type == 5)
                                    vertexToReplace = vm.getCondlines().get(dataInLine)[0];
                            }
                            state.setReplacingVertex(true);
                        }
                    default:
                        break;
                    }
                    if (state.isReplacingVertex()) {
                        state.setToReplaceX(x);
                        state.setToReplaceY(y);
                        state.setToReplaceZ(z);
                        vm.setVertexToReplace(vertexToReplace);
                        vm.getSelectedVertices().clear();
                        vm.getSelectedVertices().add(vertexToReplace);
                    }
                } catch (NumberFormatException nfe) {

                }
                NLogger.debug(VertexMarker.class, "Mark vertex at {0}", compositeText.getLine(startLine)); //$NON-NLS-1$
            }
        }
        compositeText.redraw(0, 0, compositeText.getBounds().width, compositeText.getBounds().height, true);
    }

}
