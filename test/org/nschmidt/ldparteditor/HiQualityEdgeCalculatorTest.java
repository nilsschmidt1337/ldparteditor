package org.nschmidt.ldparteditor;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.nschmidt.ldparteditor.data.BFC;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.GData2;
import org.nschmidt.ldparteditor.data.GData5;
import org.nschmidt.ldparteditor.data.GDataAndWinding;
import org.nschmidt.ldparteditor.data.HiQualityEdgeCalculator;
import org.nschmidt.ldparteditor.enumtype.View;

@SuppressWarnings("java:S5960")
public class HiQualityEdgeCalculatorTest {

    private static final String TEST = "test"; //$NON-NLS-1$

    @Test
    public void testEmptyData() {
        float[][][] result = HiQualityEdgeCalculator.hiQualityEdgeData(List.of(), Set.of(), false, false);
        assertEquals(4, result.length);
        // FIXME: One for debugging. Should be zero.
        assertEquals(1, result[0].length);
        assertEquals(0, result[1].length);
        assertEquals(0, result[2].length);
        assertEquals(0, result[3].length);
    }

    @Test
    public void testOneEdge() {
        final DatFile df = new DatFile(TEST);
        final BigDecimal one = BigDecimal.ONE;
        final BigDecimal zero = BigDecimal.ZERO;
        final GData1 parent = new GData1(0, 0, 0, 0, 0,
            View.ID, View.ACCURATE_ID, List.of(), TEST, TEST, 0, false,
            View.ID, View.ACCURATE_ID, df, null, false, false, Set.of(), null);
        final GData2 edge = new GData2(0, 0, 0, 0, 0, zero, zero, zero, one, zero, zero, parent, df, true);
        final GDataAndWinding w = new GDataAndWinding(edge, BFC.CCW, false, false, 0);
        // TODO return index arrays, too: int[][][]
        float[][][] result = HiQualityEdgeCalculator.hiQualityEdgeData(List.of(w), Set.of(), false, false);
        assertEquals(4, result.length);
        assertEquals(1, result[0].length);
        // TODO Reduce this size with an index array to 288 (16 * 18)
        assertEquals(648, result[0][0].length);
        assertEquals(0, result[1].length);
        assertEquals(0, result[2].length);
        assertEquals(0, result[3].length);
    }

    @Test
    public void testOneHiddenEdge() {
        final DatFile df = new DatFile(TEST);
        final BigDecimal one = BigDecimal.ONE;
        final BigDecimal zero = BigDecimal.ZERO;
        final GData1 parent = new GData1(0, 0, 0, 0, 0,
            View.ID, View.ACCURATE_ID, List.of(), TEST, TEST, 0, false,
            View.ID, View.ACCURATE_ID, df, null, false, false, Set.of(), null);
        final GData2 edge = new GData2(0, 0, 0, 0, 0, zero, zero, zero, one, zero, zero, parent, df, true);
        final GDataAndWinding w = new GDataAndWinding(edge, BFC.CCW, false, false, 0);
        float[][][] result = HiQualityEdgeCalculator.hiQualityEdgeData(List.of(w), Set.of(edge), false, false);
        assertEquals(4, result.length);
        // FIXME: One for debugging. Should be zero.
        assertEquals(1, result[0].length);
        assertEquals(72, result[0][0].length);
        assertEquals(0, result[1].length);
        assertEquals(0, result[2].length);
        assertEquals(0, result[3].length);
    }

    @Test
    public void testHideLines() {
        final DatFile df = new DatFile(TEST);
        final BigDecimal one = BigDecimal.ONE;
        final BigDecimal zero = BigDecimal.ZERO;
        final GData1 parent = new GData1(0, 0, 0, 0, 0,
            View.ID, View.ACCURATE_ID, List.of(), TEST, TEST, 0, false,
            View.ID, View.ACCURATE_ID, df, null, false, false, Set.of(), null);
        final GData2 edge = new GData2(0, 0, 0, 0, 0, zero, zero, zero, one, zero, zero, parent, df, true);
        final GDataAndWinding w = new GDataAndWinding(edge, BFC.CCW, false, false, 0);
        float[][][] result = HiQualityEdgeCalculator.hiQualityEdgeData(List.of(w), Set.of(), true, false);
        assertEquals(4, result.length);
        // FIXME: One for debugging. Should be zero.
        assertEquals(1, result[0].length);
        assertEquals(72, result[0][0].length);
        assertEquals(0, result[1].length);
        assertEquals(0, result[2].length);
        assertEquals(0, result[3].length);
    }

    @Test
    public void testHideCondlines() {
        final DatFile df = new DatFile(TEST);
        final BigDecimal one = BigDecimal.ONE;
        final BigDecimal zero = BigDecimal.ZERO;
        final GData1 parent = new GData1(0, 0, 0, 0, 0,
            View.ID, View.ACCURATE_ID, List.of(), TEST, TEST, 0, false,
            View.ID, View.ACCURATE_ID, df, null, false, false, Set.of(), null);
        final GData5 condline = new GData5(0, 0, 0, 0, 0, zero, zero, zero, one, zero, zero,
            zero, one, zero, zero, one.negate(), one, parent, df);
        final GDataAndWinding w = new GDataAndWinding(condline, BFC.CCW, false, false, 0);
        float[][][] result = HiQualityEdgeCalculator.hiQualityEdgeData(List.of(w), Set.of(), false, true);
        assertEquals(4, result.length);
        // FIXME: One for debugging. Should be zero.
        assertEquals(1, result[0].length);
        assertEquals(72, result[0][0].length);
        assertEquals(0, result[1].length);
        assertEquals(0, result[2].length);
        assertEquals(0, result[3].length);
    }
}
