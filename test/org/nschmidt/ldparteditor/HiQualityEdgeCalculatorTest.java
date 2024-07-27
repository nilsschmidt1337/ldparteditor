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
import org.nschmidt.ldparteditor.helper.EdgeData;

@SuppressWarnings("java:S5960")
public class HiQualityEdgeCalculatorTest {

    private static final String TEST = "test"; //$NON-NLS-1$

    @Test
    public void testEmptyData() {
        EdgeData[] result = HiQualityEdgeCalculator.hiQualityEdgeData(List.of(), Set.of(), false, false);
        assertEmptyResult(result);
    }

    private void assertEmptyResult(EdgeData[] result) {
        assertEquals(4, result.length);
        assertEquals(0, result[0].vertices().length);
        assertEquals(0, result[1].vertices().length);
        assertEquals(0, result[2].vertices().length);
        assertEquals(0, result[3].vertices().length);
        assertEquals(0, result[0].indices().length);
        assertEquals(0, result[1].indices().length);
        assertEquals(0, result[2].indices().length);
        assertEquals(0, result[3].indices().length);
    }

    @Test
    public void testOneEdge() {
        final DatFile df = new DatFile(TEST);
        final BigDecimal one = BigDecimal.ONE;
        final BigDecimal zero = BigDecimal.ZERO;
        final GData1 parent = new GData1(0, 0, 0, 0, 0,
            View.ID, View.ACCURATE_ID, List.of(), TEST, TEST, 0, false,
            View.ID, View.ACCURATE_ID, df, null, false, false, Set.of(), null);
        final GData2 edge = new GData2(0, 0, 0, 0, 1f, zero, zero, zero, one, zero, zero, parent, df, true);
        final GDataAndWinding w = new GDataAndWinding(edge, BFC.CCW, false, false, 0);
        EdgeData[] result = HiQualityEdgeCalculator.hiQualityEdgeData(List.of(w), Set.of(), false, false);
        assertEquals(4, result.length);
        assertEquals(1, result[0].vertices().length);
        assertEquals(288, result[0].vertices()[0].length);
        assertEquals(0, result[1].vertices().length);
        assertEquals(0, result[2].vertices().length);
        assertEquals(0, result[3].vertices().length);
        assertEquals(32, result[0].indices()[0].length);
        assertEquals(0, result[1].indices().length);
        assertEquals(0, result[2].indices().length);
        assertEquals(0, result[3].indices().length);
    }

    @Test
    public void testOneHiddenEdge() {
        final DatFile df = new DatFile(TEST);
        final BigDecimal one = BigDecimal.ONE;
        final BigDecimal zero = BigDecimal.ZERO;
        final GData1 parent = new GData1(0, 0, 0, 0, 0,
            View.ID, View.ACCURATE_ID, List.of(), TEST, TEST, 0, false,
            View.ID, View.ACCURATE_ID, df, null, false, false, Set.of(), null);
        final GData2 edge = new GData2(0, 0, 0, 0, 1f, zero, zero, zero, one, zero, zero, parent, df, true);
        final GDataAndWinding w = new GDataAndWinding(edge, BFC.CCW, false, false, 0);
        EdgeData[] result = HiQualityEdgeCalculator.hiQualityEdgeData(List.of(w), Set.of(edge), false, false);
        assertEquals(4, result.length);
        assertEmptyResult(result);
    }

    @Test
    public void testHideLines() {
        final DatFile df = new DatFile(TEST);
        final BigDecimal one = BigDecimal.ONE;
        final BigDecimal zero = BigDecimal.ZERO;
        final GData1 parent = new GData1(0, 0, 0, 0, 0,
            View.ID, View.ACCURATE_ID, List.of(), TEST, TEST, 0, false,
            View.ID, View.ACCURATE_ID, df, null, false, false, Set.of(), null);
        final GData2 edge = new GData2(0, 0, 0, 0, 1f, zero, zero, zero, one, zero, zero, parent, df, true);
        final GDataAndWinding w = new GDataAndWinding(edge, BFC.CCW, false, false, 0);
        EdgeData[] result = HiQualityEdgeCalculator.hiQualityEdgeData(List.of(w), Set.of(), true, false);
        assertEmptyResult(result);
    }

    @Test
    public void testHideCondlines() {
        final DatFile df = new DatFile(TEST);
        final BigDecimal one = BigDecimal.ONE;
        final BigDecimal zero = BigDecimal.ZERO;
        final GData1 parent = new GData1(0, 0, 0, 0, 0,
            View.ID, View.ACCURATE_ID, List.of(), TEST, TEST, 0, false,
            View.ID, View.ACCURATE_ID, df, null, false, false, Set.of(), null);
        final GData5 condline = new GData5(0, 0, 0, 0, 1f, zero, zero, zero, one, zero, zero,
            zero, one, zero, zero, one.negate(), one, parent, df);
        final GDataAndWinding w = new GDataAndWinding(condline, BFC.CCW, false, false, 0);
        EdgeData[] result = HiQualityEdgeCalculator.hiQualityEdgeData(List.of(w), Set.of(), false, true);
        assertEmptyResult(result);
    }
}
