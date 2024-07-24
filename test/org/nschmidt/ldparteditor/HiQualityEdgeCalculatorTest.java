package org.nschmidt.ldparteditor;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.nschmidt.ldparteditor.data.HiQualityEdgeCalculator;

@SuppressWarnings("java:S5960")
public class HiQualityEdgeCalculatorTest {

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
}
