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
import org.nschmidt.ldparteditor.dialog.primgen2.PrimGen2Dialog;
import org.nschmidt.ldparteditor.text.PrimitiveReplacer;

@SuppressWarnings("java:S5960")
public class PrimGen2Test {

    private static final String NAME = "Nils Schmidt"; //$NON-NLS-1$
    private static final String LDRAW_USER = "BlackBrick89"; //$NON-NLS-1$

    @Test(expected = ArithmeticException.class)
    public void testZeroSizedCirlce() {
        PrimGen2Dialog.buildPrimitiveSource(PrimGen2Dialog.CIRCLE, 0, 0, 0, 0, 0, 0, 0, false, 0, NAME, LDRAW_USER);
    }

    @Test
    public void testOneSixteenthCirlce() {
        final String result = PrimGen2Dialog.buildPrimitiveSource(PrimGen2Dialog.CIRCLE, 16, 1, 0, 0, 0, 0, 0, false, 0, NAME, LDRAW_USER);
        assertEquals("""
                0 Circle 0.0625
                0 Name: 1-16edge.dat
                0 Author: Nils Schmidt [BlackBrick89]
                0 !LDRAW_ORG Unofficial_Primitive
                0 !LICENSE Licensed under CC BY 4.0 : see CAreadme.txt

                0 BFC CERTIFY CW

                2 24 1 0 0 0.9239 0 0.3827
                0 // Build by LDPartEditor (PrimGen 2.X)""", result); //$NON-NLS-1$
    }

    @Test
    public void testPrimitiveReplacementOfUnknownFile() {
        final List<String> result = PrimitiveReplacer.substitutePrimitives("unknown.dat", List.of(), 56); //$NON-NLS-1$
        assertTrue(result.isEmpty());
    }

    @Test
    public void testEmptyEightSphere() {
        final List<String> result = PrimGen2Dialog.addEightSphere(0, false);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testLowResEightSphereWithCondlines() {
        final List<String> result = PrimGen2Dialog.addEightSphere(8, true);
        assertEquals(13, result.size());
    }

    @Test
    public void testNormalEightSphereWithCondlines() {
        final List<String> result = PrimGen2Dialog.addEightSphere(16, true);
        assertEquals(46, result.size());
    }

    @Test
    public void testLowResEightSphereWithoutCondlines() {
        final List<String> result = PrimGen2Dialog.addEightSphere(8, false);
        assertEquals(4, result.size());
    }

    @Test
    public void testNormalEightSphereWithoutCondlines() {
        final List<String> result = PrimGen2Dialog.addEightSphere(16, false);
        assertEquals(16, result.size());
    }
}
