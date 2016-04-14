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

import java.math.BigDecimal;

/**
 * @author nils
 *
 */
public final class GDataDist extends GData2 {

    public GDataDist(GColour c, GData1 parent, Vertex v1, Vertex v2) {
        super(c, parent, v1, v2);
    }

    public GDataDist(GData1 parent, int colourNumber, float r, float g, float b, float a, BigDecimal x1, BigDecimal y1, BigDecimal z1, BigDecimal x2, BigDecimal y2, BigDecimal z2, float x12,
            float y12, float z12, float x22, float y22, float z22, DatFile datFile) {
        super(parent, colourNumber, r, g, b, a, x1, y1, z1, x2, y2, z2, x12, y12, z12, x22, y22, z22, datFile);
    }

    public GDataDist(int colourNumber, float r, float g, float b, float a, BigDecimal x1, BigDecimal y1, BigDecimal z1, BigDecimal x2, BigDecimal y2, BigDecimal z2, GData1 parent, DatFile datFile) {
        super(colourNumber, r, g, b, a, x1, y1, z1, x2, y2, z2, parent, datFile);
    }

    public GDataDist(int colourNumber, float r, float g, float b, float a, Vertex v1, Vertex v2, GData1 parent, DatFile datFile) {
        super(colourNumber, r, g, b, a, v1, v2, parent, datFile);
    }

    public GDataDist(Vertex v1, Vertex v2, GColour c, GData1 parent) {
        super(v1, v2, c, parent);
    }

    public GDataDist(Vertex v1, Vertex v2, GData1 parent, GColour c) {
        super(v1, v2, parent, c);
    }

}
