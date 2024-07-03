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

import org.junit.Test;
import org.nschmidt.ldparteditor.helper.math.MathHelper;

@SuppressWarnings("java:S5960")
public class MathHelperTest {

    @Test
    public void testRoundingUp99() {
        String result = MathHelper.roundDecimalString("0.9999"); //$NON-NLS-1$
        assertEquals("1", result); //$NON-NLS-1$
    }

    @Test
    public void testRoundingUp98() {
        String result = MathHelper.roundDecimalString("0.9998"); //$NON-NLS-1$
        assertEquals("1", result); //$NON-NLS-1$
    }

    @Test
    public void testRoundingUp97() {
        String result = MathHelper.roundDecimalString("0.9997"); //$NON-NLS-1$
        assertEquals("1", result); //$NON-NLS-1$
    }
    @Test
    public void testRoundingUp01() {
        String result = MathHelper.roundDecimalString("0.0001"); //$NON-NLS-1$
        assertEquals("0", result); //$NON-NLS-1$
    }

    @Test
    public void testRoundingUp02() {
        String result = MathHelper.roundDecimalString("0.0002"); //$NON-NLS-1$
        assertEquals("0", result); //$NON-NLS-1$
    }

    @Test
    public void testRoundingUp03() {
        String result = MathHelper.roundDecimalString("0.0003"); //$NON-NLS-1$
        assertEquals("0", result); //$NON-NLS-1$
    }

    @Test
    public void testRoundingUpScientific() {
        String result = MathHelper.roundDecimalString("1263276279.999E-1"); //$NON-NLS-1$
        assertEquals("126327628", result); //$NON-NLS-1$
    }

    @Test
    public void testRoundingUpFloat() {
        String result = MathHelper.roundDecimalString("364748439.7769999"); //$NON-NLS-1$
        assertEquals("364748439.777", result); //$NON-NLS-1$
    }

    @Test
    public void testRoundingUpInteger() {
        String result = MathHelper.roundDecimalString("123"); //$NON-NLS-1$
        assertEquals("123", result); //$NON-NLS-1$
    }

    @Test
    public void testRoundingUpBigger() {
        String result = MathHelper.roundDecimalString("199.9999"); //$NON-NLS-1$
        assertEquals("200", result); //$NON-NLS-1$
    }

    @Test
    public void testRoundingString() {
        String result = MathHelper.roundDecimalString("fooBar"); //$NON-NLS-1$
        assertEquals("fooBar", result); //$NON-NLS-1$
    }

    @Test
    public void testRoundingZero() {
        String result = MathHelper.roundDecimalString("0"); //$NON-NLS-1$
        assertEquals("0", result); //$NON-NLS-1$
    }

    @Test
    public void testRoundingNano() {
        String result = MathHelper.roundDecimalString("0.000000001"); //$NON-NLS-1$
        assertEquals("0", result); //$NON-NLS-1$
    }

    @Test
    public void testRoundingNegative() {
        String result = MathHelper.roundDecimalString("-11.250002"); //$NON-NLS-1$
        assertEquals("-11.25", result); //$NON-NLS-1$
    }

    @Test
    public void testRoundingNullString() {
        String result = MathHelper.roundDecimalString("null"); //$NON-NLS-1$
        assertEquals("null", result); //$NON-NLS-1$
    }

    @Test(expected = NullPointerException.class)
    public void testRoundingNull() {
        String nullStr = null;
        MathHelper.roundDecimalString(nullStr);
    }

    @Test
    public void testRemovingFourZeros() {
        String result = MathHelper.roundDecimalString("0.123000077236237"); //$NON-NLS-1$
        assertEquals(".123", result); //$NON-NLS-1$
    }

    @Test
    public void testRemovingFourNines() {
        String result = MathHelper.roundDecimalString("0.464348999942479"); //$NON-NLS-1$
        assertEquals(".464349", result); //$NON-NLS-1$
    }

    @Test
    public void testRemovingFourNinesAndFourZeros() {
        String result = MathHelper.roundDecimalString("0.865999900001"); //$NON-NLS-1$
        assertEquals(".866", result); //$NON-NLS-1$
    }

    @Test
    public void testRemovingFourZerosAndFourNines() {
        String result = MathHelper.roundDecimalString("0.12000099994711"); //$NON-NLS-1$
        assertEquals(".12", result); //$NON-NLS-1$
    }

    @Test
    public void testNoRoundingUp99WithPreciseSnap() {
        MathHelper.setPreciseSnap(true);
        String result = MathHelper.roundDecimalString("0.9999"); //$NON-NLS-1$
        MathHelper.setPreciseSnap(false);
        assertEquals(".9999", result); //$NON-NLS-1$
    }

    @Test
    public void testRoundingUp99WithPreciseSnap() {
        MathHelper.setPreciseSnap(true);
        String result = MathHelper.roundDecimalStringAlways("0.9999"); //$NON-NLS-1$
        MathHelper.setPreciseSnap(false);
        assertEquals("1", result); //$NON-NLS-1$
    }
}
