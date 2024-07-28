package org.nschmidt.ldparteditor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
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
        EdgeData[] result = HiQualityEdgeCalculator.hiQualityEdgeData(List.of(),
                fList(), iList(), fList(), iList(), fList(), iList(), fList(), iList(),
                Set.of(), false, false, false, false);
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
        EdgeData[] result = HiQualityEdgeCalculator.hiQualityEdgeData(List.of(w),
                fList(), iList(), fList(), iList(), fList(), iList(), fList(), iList(),
                Set.of(), false, false, false, false);
        assertEquals(4, result.length);
        assertEquals(96, result[0].vertices().length);
        assertEquals(0, result[1].vertices().length);
        assertEquals(0, result[2].vertices().length);
        assertEquals(0, result[3].vertices().length);
        assertEquals(32, result[0].indices().length);
        assertEquals(0, result[1].indices().length);
        assertEquals(0, result[2].indices().length);
        assertEquals(0, result[3].indices().length);
    }

    @Test
    public void testOneTransparentEdge() {
        final DatFile df = new DatFile(TEST);
        final BigDecimal one = BigDecimal.ONE;
        final BigDecimal zero = BigDecimal.ZERO;
        final GData1 parent = new GData1(0, 0, 0, 0, 0,
            View.ID, View.ACCURATE_ID, List.of(), TEST, TEST, 0, false,
            View.ID, View.ACCURATE_ID, df, null, false, false, Set.of(), null);
        final GData2 edge = new GData2(0, 0, 0, 0, 0.5f, zero, zero, zero, one, zero, zero, parent, df, true);
        final GDataAndWinding w = new GDataAndWinding(edge, BFC.CCW, false, false, 0);
        EdgeData[] result = HiQualityEdgeCalculator.hiQualityEdgeData(List.of(w),
                fList(), iList(), fList(), iList(), fList(), iList(), fList(), iList(),
                Set.of(), false, false, false, false);
        assertEquals(4, result.length);
        assertEquals(0, result[0].vertices().length);
        assertEquals(96, result[1].vertices().length);
        assertEquals(0, result[2].vertices().length);
        assertEquals(0, result[3].vertices().length);
        assertEquals(0, result[0].indices().length);
        assertEquals(32, result[1].indices().length);
        assertEquals(0, result[2].indices().length);
        assertEquals(0, result[3].indices().length);
    }

    @Test
    public void testOneRandomColourEdge() {
        final DatFile df = new DatFile(TEST);
        final BigDecimal one = BigDecimal.ONE;
        final BigDecimal zero = BigDecimal.ZERO;
        final GData1 parent = new GData1(0, 0, 0, 0, 0,
            View.ID, View.ACCURATE_ID, List.of(), TEST, TEST, 0, false,
            View.ID, View.ACCURATE_ID, df, null, false, false, Set.of(), null);
        final GData2 edge = new GData2(0, -1f, -1f, -1f, 1f, zero, zero, zero, one, zero, zero, parent, df, true);
        final GDataAndWinding w = new GDataAndWinding(edge, BFC.CCW, false, false, 0);
        EdgeData[] result = HiQualityEdgeCalculator.hiQualityEdgeData(List.of(w),
                fList(), iList(), fList(), iList(), fList(), iList(), fList(), iList(),
                Set.of(), false, false, false, true);
        assertEquals(4, result.length);
        assertEquals(96, result[0].vertices().length);
        assertNotEquals(-1f, result[0].vertices()[3], 0.001f);
        assertNotEquals(-1f, result[0].vertices()[4], 0.001f);
        assertNotEquals(-1f, result[0].vertices()[5], 0.001f);
        assertEquals(0, result[1].vertices().length);
        assertEquals(0, result[2].vertices().length);
        assertEquals(0, result[3].vertices().length);
        assertEquals(32, result[0].indices().length);
        assertEquals(0, result[1].indices().length);
        assertEquals(0, result[2].indices().length);
        assertEquals(0, result[3].indices().length);
    }

    @Test
    public void testOneCondline() {
        final DatFile df = new DatFile(TEST);
        final BigDecimal one = BigDecimal.ONE;
        final BigDecimal zero = BigDecimal.ZERO;
        final GData1 parent = new GData1(0, 0, 0, 0, 0,
            View.ID, View.ACCURATE_ID, List.of(), TEST, TEST, 0, false,
            View.ID, View.ACCURATE_ID, df, null, false, false, Set.of(), null);
        final GData5 condline = new GData5(0, 0, 0, 0, 1f, zero, zero, zero, one, zero, zero, zero, zero, zero, zero, zero, zero, parent, df);
        final GDataAndWinding w = new GDataAndWinding(condline, BFC.CCW, false, false, 0);
        EdgeData[] result = HiQualityEdgeCalculator.hiQualityEdgeData(List.of(w),
                fList(), iList(), fList(), iList(), fList(), iList(), fList(), iList(),
                Set.of(), false, false, false, false);
        assertEquals(4, result.length);
        assertEquals(0, result[0].vertices().length);
        assertEquals(0, result[1].vertices().length);
        assertEquals(288, result[2].vertices().length);
        assertEquals(0, result[3].vertices().length);
        assertEquals(0, result[0].indices().length);
        assertEquals(0, result[1].indices().length);
        assertEquals(32, result[2].indices().length);
        assertEquals(0, result[3].indices().length);
    }

    @Test
    public void testOneCondlineInCondlineMode() {
        final DatFile df = new DatFile(TEST);
        final BigDecimal one = BigDecimal.ONE;
        final BigDecimal zero = BigDecimal.ZERO;
        final GData1 parent = new GData1(0, 0, 0, 0, 0,
            View.ID, View.ACCURATE_ID, List.of(), TEST, TEST, 0, false,
            View.ID, View.ACCURATE_ID, df, null, false, false, Set.of(), null);
        final GData5 condline = new GData5(0, 0, 0, 0, 1f, zero, zero, zero, one, zero, zero, zero, zero, zero, zero, zero, zero, parent, df);
        final GDataAndWinding w = new GDataAndWinding(condline, BFC.CCW, false, false, 0);
        EdgeData[] result = HiQualityEdgeCalculator.hiQualityEdgeData(List.of(w),
                fList(), iList(), fList(), iList(), fList(), iList(), fList(), iList(),
                Set.of(), false, false, true, false);
        assertEquals(4, result.length);
        assertEquals(96, result[0].vertices().length);
        assertEquals(0, result[1].vertices().length);
        assertEquals(0, result[2].vertices().length);
        assertEquals(0, result[3].vertices().length);
        assertEquals(32, result[0].indices().length);
        assertEquals(0, result[1].indices().length);
        assertEquals(0, result[2].indices().length);
        assertEquals(0, result[3].indices().length);
    }

    @Test
    public void testOneTransparentCondline() {
        final DatFile df = new DatFile(TEST);
        final BigDecimal one = BigDecimal.ONE;
        final BigDecimal zero = BigDecimal.ZERO;
        final GData1 parent = new GData1(0, 0, 0, 0, 0,
            View.ID, View.ACCURATE_ID, List.of(), TEST, TEST, 0, false,
            View.ID, View.ACCURATE_ID, df, null, false, false, Set.of(), null);
        final GData5 condline = new GData5(0, 0, 0, 0, 0.5f, zero, zero, zero, one, zero, zero, zero, zero, zero, zero, zero, zero, parent, df);
        final GDataAndWinding w = new GDataAndWinding(condline, BFC.CCW, false, false, 0);
        EdgeData[] result = HiQualityEdgeCalculator.hiQualityEdgeData(List.of(w),
                fList(), iList(), fList(), iList(), fList(), iList(), fList(), iList(),
                Set.of(), false, false, false, false);
        assertEquals(4, result.length);
        assertEquals(0, result[0].vertices().length);
        assertEquals(0, result[1].vertices().length);
        assertEquals(0, result[2].vertices().length);
        assertEquals(288, result[3].vertices().length);
        assertEquals(0, result[0].indices().length);
        assertEquals(0, result[1].indices().length);
        assertEquals(0, result[2].indices().length);
        assertEquals(32, result[3].indices().length);
    }

    @Test
    public void testOneRandomColourCondline() {
        final DatFile df = new DatFile(TEST);
        final BigDecimal one = BigDecimal.ONE;
        final BigDecimal zero = BigDecimal.ZERO;
        final GData1 parent = new GData1(0, 0, 0, 0, 0,
            View.ID, View.ACCURATE_ID, List.of(), TEST, TEST, 0, false,
            View.ID, View.ACCURATE_ID, df, null, false, false, Set.of(), null);
        final GData5 condline = new GData5(0, -1f, -1f, -1f, 1f, zero, zero, zero, one, zero, zero, zero, zero, zero, zero, zero, zero, parent, df);
        final GDataAndWinding w = new GDataAndWinding(condline, BFC.CCW, false, false, 0);
        EdgeData[] result = HiQualityEdgeCalculator.hiQualityEdgeData(List.of(w),
                fList(), iList(), fList(), iList(), fList(), iList(), fList(), iList(),
                Set.of(), false, false, false, true);
        assertEquals(4, result.length);
        assertEquals(0, result[0].vertices().length);
        assertEquals(0, result[1].vertices().length);
        assertEquals(288, result[2].vertices().length);
        assertNotEquals(-1f, result[2].vertices()[15], 0.001f);
        assertNotEquals(-1f, result[2].vertices()[16], 0.001f);
        assertNotEquals(-1f, result[2].vertices()[17], 0.001f);
        assertEquals(0, result[3].vertices().length);
        assertEquals(0, result[0].indices().length);
        assertEquals(0, result[1].indices().length);
        assertEquals(32, result[2].indices().length);
        assertEquals(0, result[3].indices().length);
    }

    private List<Integer> iList() {
        return new ArrayList<>();
    }

    private List<Float> fList() {
        return new ArrayList<>();
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
        EdgeData[] result = HiQualityEdgeCalculator.hiQualityEdgeData(List.of(w),
                fList(), iList(), fList(), iList(), fList(), iList(), fList(), iList(),
                Set.of(edge), false, false, false, false);
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
        EdgeData[] result = HiQualityEdgeCalculator.hiQualityEdgeData(List.of(w),
                fList(), iList(), fList(), iList(), fList(), iList(), fList(), iList(),
                Set.of(), true, false, false, false);
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
        EdgeData[] result = HiQualityEdgeCalculator.hiQualityEdgeData(List.of(w),
                fList(), iList(), fList(), iList(), fList(), iList(), fList(), iList(),
                Set.of(), false, true, false, false);
        assertEmptyResult(result);
    }
}
