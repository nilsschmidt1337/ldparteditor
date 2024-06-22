package org.nschmidt.ldparteditor;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.nschmidt.ldparteditor.helper.math.MathHelper;

@SuppressWarnings("java:S5960")
public class MathHelperTest {

    @Test
    public void testRoundingUp99() {
        String result = MathHelper.roundNumericString("0.9999"); //$NON-NLS-1$
        assertEquals("1", result); //$NON-NLS-1$
    }

    @Test
    public void testRoundingUp98() {
        String result = MathHelper.roundNumericString("0.9998"); //$NON-NLS-1$
        assertEquals("1", result); //$NON-NLS-1$
    }

    @Test
    public void testRoundingUp97() {
        String result = MathHelper.roundNumericString("0.9997"); //$NON-NLS-1$
        assertEquals("1", result); //$NON-NLS-1$
    }
    @Test
    public void testRoundingUp01() {
        String result = MathHelper.roundNumericString("0.0001"); //$NON-NLS-1$
        assertEquals("0", result); //$NON-NLS-1$
    }

    @Test
    public void testRoundingUp02() {
        String result = MathHelper.roundNumericString("0.0002"); //$NON-NLS-1$
        assertEquals("0", result); //$NON-NLS-1$
    }

    @Test
    public void testRoundingUp03() {
        String result = MathHelper.roundNumericString("0.0003"); //$NON-NLS-1$
        assertEquals("0", result); //$NON-NLS-1$
    }

    @Test
    public void testRoundingUpScientific() {
        String result = MathHelper.roundNumericString("1263276279.999E-1"); //$NON-NLS-1$
        assertEquals("126327628", result); //$NON-NLS-1$
    }

    @Test
    public void testRoundingUpFloat() {
        String result = MathHelper.roundNumericString("364748439.7769999"); //$NON-NLS-1$
        assertEquals("364748439.777", result); //$NON-NLS-1$
    }

    @Test
    public void testRoundingUpInteger() {
        String result = MathHelper.roundNumericString("123"); //$NON-NLS-1$
        assertEquals("123", result); //$NON-NLS-1$
    }

    @Test
    public void testRoundingUpBigger() {
        String result = MathHelper.roundNumericString("199.9999"); //$NON-NLS-1$
        assertEquals("200", result); //$NON-NLS-1$
    }

    @Test
    public void testRoundingString() {
        String result = MathHelper.roundNumericString("fooBar"); //$NON-NLS-1$
        assertEquals("fooBar", result); //$NON-NLS-1$
    }

    @Test
    public void testRoundingZero() {
        String result = MathHelper.roundNumericString("0"); //$NON-NLS-1$
        assertEquals("0", result); //$NON-NLS-1$
    }

    @Test
    public void testRoundingNano() {
        String result = MathHelper.roundNumericString("0.000000001"); //$NON-NLS-1$
        assertEquals("0", result); //$NON-NLS-1$
    }

    @Test
    public void testRoundingNegative() {
        String result = MathHelper.roundNumericString("-11.250002"); //$NON-NLS-1$
        assertEquals("-11.25", result); //$NON-NLS-1$
    }

    @Test
    public void testRoundingNullString() {
        String result = MathHelper.roundNumericString("null"); //$NON-NLS-1$
        assertEquals("null", result); //$NON-NLS-1$
    }

    @Test(expected = NullPointerException.class)
    public void testRoundingNull() {
        String nullStr = null;
        MathHelper.roundNumericString(nullStr);
    }

    @Test
    public void testRemovingFourZeros() {
        String result = MathHelper.roundNumericString("0.123000077236237"); //$NON-NLS-1$
        assertEquals(".123", result); //$NON-NLS-1$
    }

    @Test
    public void testRemovingFourNines() {
        String result = MathHelper.roundNumericString("0.464348999942479"); //$NON-NLS-1$
        assertEquals(".464349", result); //$NON-NLS-1$
    }

    @Test
    public void testRemovingFourNinesAndFourZeros() {
        String result = MathHelper.roundNumericString("0.865999900001"); //$NON-NLS-1$
        assertEquals(".866", result); //$NON-NLS-1$
    }

    @Test
    public void testRemovingFourZerosAndFourNines() {
        String result = MathHelper.roundNumericString("0.12000099994711"); //$NON-NLS-1$
        assertEquals(".12", result); //$NON-NLS-1$
    }
}
