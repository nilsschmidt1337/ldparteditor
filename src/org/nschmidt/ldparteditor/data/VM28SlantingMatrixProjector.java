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
import java.util.Set;

import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.text.DatParser;

public class VM28SlantingMatrixProjector extends VM27YTruder {

    protected VM28SlantingMatrixProjector(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    // These variables are static because the same transformation matrix should be calculated for many DatFiles
    private static Vertex[] axisX = null;
    private static Vertex[] axisY = null;
    private static Vertex[] axisZ = null;

    private static Matrix transformation = View.ACCURATE_ID;

    public void storeAxisForSlantingMatrixProjector() {
        final int selectedLinesSize = selectedLines.size();
        if (selectedLinesSize > 1 && selectedLinesSize < 4) {
            boolean hasXaxis = false;
            boolean hasYaxis = false;
            boolean hasZaxis = false;
            Vertex[] tmpAxisX = null;
            Vertex[] tmpAxisY = null;
            Vertex[] tmpAxisZ = null;

            for (GData2 line : selectedLines) {
                if (line.colourNumber == 4) {
                    tmpAxisX = lines.get(line);
                    hasXaxis = true;
                } else if (line.colourNumber == 2) {
                    tmpAxisY = lines.get(line);
                    hasYaxis = true;
                } else if (line.colourNumber == 1) {
                    tmpAxisZ = lines.get(line);
                    hasZaxis = true;
                }
            }

            if (hasXaxis && hasYaxis) {
                axisX = tmpAxisX;
                axisY = tmpAxisY;
                if (hasZaxis) {
                    axisZ = tmpAxisZ;
                    NLogger.debug(getClass(), "Stored selected X,Y,Z axis"); //$NON-NLS-1$
                } else {
                    NLogger.debug(getClass(), "Stored selected X,Y axis"); //$NON-NLS-1$
                }
            }
        }
    }

    public Matrix getSlantingMatrix() {
        final Matrix result = View.ACCURATE_ID;
        if (axisX == null || axisY == null) {
            return View.ACCURATE_ID;
        }
        // FIXME Needs impl.
        transformation = result;
        return result;
    }

    public void projectWithSlantingMatrix(boolean moveSubfilesToOrigin) {
        if (axisX == null || axisY == null) {
            return;
        }
        if (moveSubfilesToOrigin) {
            Set<GData1> newSubfileSelection = new HashSet<GData1>();
            for (GData1 s : selectedSubfiles) {
                StringBuilder lineBuilder = new StringBuilder();
                lineBuilder.append(1);
                lineBuilder.append(" "); //$NON-NLS-1$
                if (s.colourNumber == -1) {
                    lineBuilder.append("0x2"); //$NON-NLS-1$
                    lineBuilder.append(MathHelper.toHex((int) (255f * s.r)).toUpperCase());
                    lineBuilder.append(MathHelper.toHex((int) (255f * s.g)).toUpperCase());
                    lineBuilder.append(MathHelper.toHex((int) (255f * s.b)).toUpperCase());
                } else {
                    lineBuilder.append(s.colourNumber);
                }
                lineBuilder.append(" 0 0 0 1 0 0 0 1 0 0 0 1 "); //$NON-NLS-1$
                final GColour col16 = View.getLDConfigColour(16);
                Set<String> alreadyParsed = new HashSet<String>();
                alreadyParsed.add(linkedDatFile.getShortName());
                ArrayList<ParsingResult> subfileLine = DatParser
                        .parseLine(lineBuilder.toString(), -1, 0, col16.getR(), col16.getG(), col16.getB(), 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile, false, alreadyParsed, false);
                GData1 subfile = (GData1) subfileLine.get(0).getGraphicalData();
                if (subfile != null) {
                    linker(s, subfile);
                }
            }
            selectedSubfiles.clear();
            selectedSubfiles.addAll(newSubfileSelection);
            reSelectSubFiles();
        }
        transformSelection(transformation, null, Editor3DWindow.getWindow().isMovingAdjacentData());
    }
}
