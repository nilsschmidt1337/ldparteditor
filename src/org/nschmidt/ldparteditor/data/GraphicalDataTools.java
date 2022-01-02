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

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

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
            return new BigDecimal[]{g2.x1p, g2.y1p, g2.z1p, g2.x2p, g2.y2p, g2.z2p};
        case 3:
            GData3 g3 = (GData3) g;
            return new BigDecimal[]{g3.x1p, g3.y1p, g3.z1p, g3.x2p, g3.y2p, g3.z2p, g3.x3p, g3.y3p, g3.z3p};
        case 4:
            GData4 g4 = (GData4) g;
            return new BigDecimal[]{g4.x1p, g4.y1p, g4.z1p, g4.x2p, g4.y2p, g4.z2p, g4.x3p, g4.y3p, g4.z3p, g4.x4p, g4.y4p, g4.z4p};
        case 5:
            GData5 g5 = (GData5) g;
            return new BigDecimal[]{g5.x1p, g5.y1p, g5.z1p, g5.x2p, g5.y2p, g5.z2p, g5.x3p, g5.y3p, g5.z3p, g5.x4p, g5.y4p, g5.z4p};
        default:
            return new BigDecimal[0];
        }
    }

    static void setVertex(float x, float y, float z, GData gd, boolean useCache) {
        // TODO Needs better caching since the connectivity information of TEXMAP data is unknown and the orientation of the normals can vary.
        if (useCache) {
            Vector4f v;
            switch (gd.type()) {
            case 2:
                v = new Vector4f(x, y, z, 1f);
                Matrix4f.transform(((GData2) gd).parent.productMatrix, v, v);
                break;
            case 3:
                v = new Vector4f(x, y, z, 1f);
                Matrix4f.transform(((GData3) gd).parent.productMatrix, v, v);
                break;
            case 4:
                v = new Vector4f(x, y, z, 1f);
                Matrix4f.transform(((GData4) gd).parent.productMatrix, v, v);
                break;
            case 5:
                v = new Vector4f(x, y, z, 1f);
                Matrix4f.transform(((GData5) gd).parent.productMatrix, v, v);
                break;
            default:
                throw new AssertionError();
            }
            GL11.glVertex3f(v.x, v.y, v.z);
        } else {
            GL11.glVertex3f(x, y, z);
        }
    }
}
