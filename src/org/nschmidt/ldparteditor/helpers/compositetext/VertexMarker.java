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

import java.math.BigDecimal;

import org.eclipse.swt.custom.StyledText;
import org.nschmidt.ldparteditor.composites.compositetab.CompositeTabState;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GData;
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
        int caret_start = compositeText.getSelection().x;
        int caret_end = compositeText.getSelection().y;
        final int startLine = compositeText.getLineAtOffset(caret_start);
        final int endLine = compositeText.getLineAtOffset(caret_end);
        BigDecimal x = null;
        BigDecimal y = null;
        BigDecimal z = null;
        if (startLine == endLine) {
            GData dataInLine = datFile.getDrawPerLine().getValue(startLine + 1);
            final int type = dataInLine.type();
            if (type != 1) {
                int off = compositeText.getOffsetAtLine(startLine);
                caret_start -= off;
                caret_end -= off;
                String line = compositeText.getLine(startLine);
                String[] data_segments = line.trim().split("\\s+"); //$NON-NLS-1$
                VertexManager vm = datFile.getVertexManager();
                Vertex vertexToReplace = null;
                try {
                    switch (type) {
                    case 0:
                        if (data_segments.length == 6 && "0".equals(data_segments[0]) && "!LPE".equals(data_segments[1]) && "VERTEX".equals(data_segments[2])) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            if (caret_start > line.indexOf("VERTEX") + 6) { //$NON-NLS-1$
                                x = new BigDecimal(data_segments[3]);
                                y = new BigDecimal(data_segments[4]);
                                z = new BigDecimal(data_segments[5]);
                                state.setReplacingVertex(true);
                                vertexToReplace = vm.getDeclaredVertices().get(dataInLine)[0];
                            }
                        }
                        break;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        int index2 = StringHelper.getIndexFromWhitespaces(line, caret_start);
                        if (index2 > 1) {
                            if (type > 3 && index2 > 10) {
                                x = new BigDecimal(data_segments[11]);
                                y = new BigDecimal(data_segments[12]);
                                z = new BigDecimal(data_segments[13]);
                                if (type == 4)
                                    vertexToReplace = vm.getQuads().get(dataInLine)[3];
                                else if (type == 5)
                                    vertexToReplace = vm.getCondlines().get(dataInLine)[3];
                            } else if (type > 2 && index2 > 7) {
                                x = new BigDecimal(data_segments[8]);
                                y = new BigDecimal(data_segments[9]);
                                z = new BigDecimal(data_segments[10]);
                                if (type == 3)
                                    vertexToReplace = vm.getTriangles().get(dataInLine)[2];
                                else if (type == 4)
                                    vertexToReplace = vm.getQuads().get(dataInLine)[2];
                                else if (type == 5)
                                    vertexToReplace = vm.getCondlines().get(dataInLine)[2];
                            } else if (index2 > 4) {
                                x = new BigDecimal(data_segments[5]);
                                y = new BigDecimal(data_segments[6]);
                                z = new BigDecimal(data_segments[7]);
                                if (type == 2)
                                    vertexToReplace = vm.getLines().get(dataInLine)[1];
                                else if (type == 3)
                                    vertexToReplace = vm.getTriangles().get(dataInLine)[1];
                                else if (type == 4)
                                    vertexToReplace = vm.getQuads().get(dataInLine)[1];
                                else if (type == 5)
                                    vertexToReplace = vm.getCondlines().get(dataInLine)[1];
                            } else {
                                x = new BigDecimal(data_segments[2]);
                                y = new BigDecimal(data_segments[3]);
                                z = new BigDecimal(data_segments[4]);
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
