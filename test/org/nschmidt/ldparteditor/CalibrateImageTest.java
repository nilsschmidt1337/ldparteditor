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
package org.nschmidt.ldparteditor;

import static org.junit.Assert.assertEquals;
import static org.nschmidt.ldparteditor.dialog.calibrate.CalibrateDialog.calibrateOffset;

import java.math.BigDecimal;

import org.junit.Test;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.data.GDataPNG;
import org.nschmidt.ldparteditor.data.Vertex;

@SuppressWarnings("java:S5960")
public class CalibrateImageTest {

    private static Matrix4f id() {
        return Matrix4f.setIdentity(new Matrix4f());
    }

    @Test
    public void testImageOffsetScaleByTwoWithZeroOrigin() {
         final Vector4f offset = calibrateOffset(id(), new Vector4f(), 0f, 0f, 0f, new Vector4f(), 2f);
         assertEquals(0f, offset.x, 0f);
         assertEquals(0f, offset.y, 0f);
         assertEquals(0f, offset.z, 0f);
    }

    @Test
    public void testImageOffsetScaleByTwoWithShiftedOrigin() {
         final Vector4f offset = calibrateOffset(id().translate(new Vector3f(2000f, 4000f, 8000f)), new Vector4f(2f, 4f, 8f, 1f), 0f, 0f, 0f, new Vector4f(), 2f);
         assertEquals(4f, offset.x, 0f);
         assertEquals(8f, offset.y, 0f);
         assertEquals(16f, offset.z, 0f);
    }

    @Test
    public void testImageOffsetScaleByTwoWithShiftedPivot() {
         final Vector4f offset = calibrateOffset(id(), new Vector4f(0f, 0f, 0f, 1f), 0f, 0f, 0f, new Vector4f(1f, 1f, 0f, 1f), 2f);
         assertEquals(-1f, offset.x, 0f);
         assertEquals(-1f, offset.y, 0f);
         assertEquals(0f, offset.z, 0f);
    }

    @Test
    public void testImageOffsetComplexExample() {
        final GDataPNG png = new GDataPNG("0 !LPE PNG 5 1 0 90 90 0 10 10 test.png", //$NON-NLS-1$
            new Vertex(BigDecimal.valueOf(5), BigDecimal.ONE, BigDecimal.ZERO),
            BigDecimal.valueOf(90), BigDecimal.valueOf(90), BigDecimal.valueOf(0),
            new Vertex(BigDecimal.valueOf(10), BigDecimal.valueOf(10), BigDecimal.ZERO), "test.png", null); //$NON-NLS-1$

        final Matrix4f maxtrix = png.getMatrix();
        final Vector4f offset = calibrateOffset(maxtrix, new Vector4f(5f, 1f, 0f, 1f), 90f, 90f, 0f, new Vector4f(0f, 5f, 1f, 1f), 2f);
        assertEquals(5f, offset.x, 0.001f);
        assertEquals(1f, offset.y, 0.001f);
        assertEquals(0f, offset.z, 0.001f);
    }
}
