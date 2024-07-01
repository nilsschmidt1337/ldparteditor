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
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.Test;
import org.nschmidt.ldparteditor.helper.math.Vector3d;

@SuppressWarnings("java:S5960")
public class Vector3dTest {

    private static final float FLOAT_DELTA_NULL = 0f;
    private static final float FLOAT_DELTA = 1E-6f;

    @Test
    public void testNullVectorConstrucor() {
        final Vector3d nullVector = new Vector3d();

        assertIsNullVector(nullVector);
    }

    @Test
    public void testNullVectorAddition() {
        final Vector3d nullVector = new Vector3d();
        final Vector3d sum = Vector3d.add(nullVector, nullVector);

        assertIsNullVector(sum);
    }

    private void assertIsNullVector(Vector3d nullVector) {
        assertTrue(BigDecimal.ZERO.compareTo(nullVector.x) == 0);
        assertTrue(BigDecimal.ZERO.compareTo(nullVector.y) == 0);
        assertTrue(BigDecimal.ZERO.compareTo(nullVector.z) == 0);
        assertEquals(0f, nullVector.getXf(), FLOAT_DELTA_NULL);
        assertEquals(0f, nullVector.getYf(), FLOAT_DELTA_NULL);
        assertEquals(0f, nullVector.getZf(), FLOAT_DELTA_NULL);
        assertEquals(0f, nullVector.length().floatValue(), FLOAT_DELTA);
    }
}
