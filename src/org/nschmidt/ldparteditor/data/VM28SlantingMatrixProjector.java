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

import org.nschmidt.ldparteditor.logger.NLogger;

public class VM28SlantingMatrixProjector extends VM27YTruder {

    protected VM28SlantingMatrixProjector(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    // These variables are static because the same transformation matrix should be calculated for many DatFiles
    private static Vertex[] axisX = null;
    private static Vertex[] axisY = null;
    private static Vertex[] axisZ = null;

    public void storeAxisForSlantingMatrixProjector() {
        final int selectedLinesSize = selectedLines.size();
        if (selectedLinesSize > 1 && selectedLinesSize < 4) {
            if (selectedVertices.isEmpty() && selectedSubfiles.isEmpty() && selectedTriangles.isEmpty() && selectedQuads.isEmpty()) {
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
    }

    public void getSlantingMatrix() {
        // FIXME Needs impl.
    }

    public void projectWithSlantingMatrix() {
        // FIXME Needs impl.
    }
}
