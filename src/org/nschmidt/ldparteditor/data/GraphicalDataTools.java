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
public enum GraphicalDataTools {
    INSTANCE;

    public static BigDecimal[] getPreciseCoordinates(GData g) {
        switch (g.type()) {
        case 2:
            GData2 g2 = (GData2) g;
            return new BigDecimal[]{g2.X1, g2.Y1, g2.Z1, g2.X2, g2.Y2, g2.Z2};
        case 3:
            GData3 g3 = (GData3) g;
            return new BigDecimal[]{g3.X1, g3.Y1, g3.Z1, g3.X2, g3.Y2, g3.Z2, g3.X3, g3.Y3, g3.Z3};
        case 4:
            GData4 g4 = (GData4) g;
            return new BigDecimal[]{g4.X1, g4.Y1, g4.Z1, g4.X2, g4.Y2, g4.Z2, g4.X3, g4.Y3, g4.Z3, g4.X4, g4.Y4, g4.Z4};
        case 5:
            GData5 g5 = (GData5) g;
            return new BigDecimal[]{g5.X1, g5.Y1, g5.Z1, g5.X2, g5.Y2, g5.Z2, g5.X3, g5.Y3, g5.Z3, g5.X4, g5.Y4, g5.Z4};
        default:
            return null;
        }
    }
}
