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

import java.util.List;

import org.junit.Test;
import org.nschmidt.ldparteditor.helper.math.EightSphereGenerator;

@SuppressWarnings("java:S5960")
public class EightSphereGeneratorTest {

    @Test
    public void testEmptyEightSphere() {
        final List<String> result = EightSphereGenerator.addEightSphere(0, false);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testLowResEightSphereWithCondlines() {
        final List<String> result = EightSphereGenerator.addEightSphere(8, true);
        assertEquals(13, result.size());
    }

    @Test
    public void testNormalEightSphereWithCondlines() {
        final List<String> result = EightSphereGenerator.addEightSphere(16, true);
        assertEquals(46, result.size());
    }

    @Test
    public void testLowResEightSphereWithoutCondlines() {
        final List<String> result = EightSphereGenerator.addEightSphere(8, false);
        assertEquals(4, result.size());
    }

    @Test
    public void testNormalEightSphereWithoutCondlines() {
        final List<String> result = EightSphereGenerator.addEightSphere(16, false);
        assertEquals(16, result.size());
    }
}
