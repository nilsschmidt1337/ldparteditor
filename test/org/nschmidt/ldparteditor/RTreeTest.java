package org.nschmidt.ldparteditor;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.nschmidt.ldparteditor.helper.math.rtree.BoundingBox;

public class RTreeTest {

    @Test
    public void testEmptyBoundingBoxContainsZeroVector() {
        final BoundingBox cut = new BoundingBox();
        assertTrue(cut.contains(0, 0, 0));
    }
}
