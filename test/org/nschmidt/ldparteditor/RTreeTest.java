package org.nschmidt.ldparteditor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.nschmidt.ldparteditor.helper.math.rtree.BoundingBox;

public class RTreeTest {

    @Test
    public void testEmptyBoundingBoxContainsZeroVector() {
        final BoundingBox cut = new BoundingBox();
        assertTrue(cut.contains(0, 0, 0));
    }

    @Test
    public void testBoundingBoxIntersection() {
        final BoundingBox cutA = new BoundingBox(
                0, 0, 0, 1, 2, 3);
        final BoundingBox cutB = new BoundingBox(
                -1, -1, -1, 1, 1, 1);

        final BoundingBox resultAB = cutA.intersection(cutB);
        final BoundingBox resultBA = cutB.intersection(cutA);
        final float area = resultAB.area();

        assertEquals(resultAB, resultBA);
        assertEquals(6f, area, 0f);
    }

    @Test
    public void testBoundingBoxArea() {
        final BoundingBox cut = new BoundingBox(
                0, 0, 0, 1, 2, 3);

        final float area = cut.area();

        assertEquals(22f, area, 0f);
    }
}
