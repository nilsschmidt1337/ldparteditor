package org.nschmidt.ldparteditor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.GData3;
import org.nschmidt.ldparteditor.data.GData4;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.helper.math.Vector3d;
import org.nschmidt.ldparteditor.helper.math.rtree.BoundingBox;
import org.nschmidt.ldparteditor.helper.math.rtree.RTree;

@SuppressWarnings("java:S5960")
public class RTreeTest {

    private static final String TEST = "test"; //$NON-NLS-1$

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
        final float area = resultAB.areaHalf();

        assertEquals(resultAB, resultBA);
        assertEquals(3f, area, 0f);
    }

    @Test
    public void testBoundingBoxArea() {
        final BoundingBox cut = new BoundingBox(
                0, 0, 0, 1, 2, 3);

        final float area = cut.areaHalf();

        assertEquals(11f, area, 0f);
    }

    @Test
    public void testBoundingBoxCopy() {
        final BoundingBox cut = new BoundingBox(
                -3, -2, -1, 1, 2, 3);
        final BoundingBox clone = cut.copy();

        assertEquals(cut, clone);
    }

    @Test
    public void testRTreeWith3Triangles() {
        final DatFile df = new DatFile(TEST);
        final BigDecimal one = BigDecimal.ONE;
        final BigDecimal zero = BigDecimal.ZERO;
        final GData1 parent = new GData1(0, 0, 0, 0, 0,
            View.ID, View.ACCURATE_ID, List.of(), TEST, TEST, 0, false,
            View.ID, View.ACCURATE_ID, df, null, false, false, Set.of(), null);

        final GData3 tri1 = new GData3(0, 0, 0, 0, 0.5f, zero, zero, zero, one, zero, zero, one, one, zero, parent, df, true);
        final GData3 tri2 = new GData3(0, 0, 0, 0, 0.5f, zero, zero, zero, one, zero, zero, one, zero, one, parent, df, true);
        final GData3 tri3 = new GData3(0, 0, 0, 0, 0.5f, zero, zero, zero, one, one, zero, one, zero, one, parent, df, true);

        final RTree cut = new RTree();

        cut.add(tri1);
        cut.add(tri2);
        cut.add(tri3);

        assertEquals(3, cut.size());
    }

    @Test
    public void testRTreeWith4Triangles() {
        final DatFile df = new DatFile(TEST);
        final BigDecimal one = BigDecimal.ONE;
        final BigDecimal zero = BigDecimal.ZERO;
        final GData1 parent = new GData1(0, 0, 0, 0, 0,
            View.ID, View.ACCURATE_ID, List.of(), TEST, TEST, 0, false,
            View.ID, View.ACCURATE_ID, df, null, false, false, Set.of(), null);

        final GData3 tri1 = new GData3(0, 0, 0, 0, 0.5f, zero, zero, zero, one, zero, zero, one, one, zero, parent, df, true);
        final GData3 tri2 = new GData3(0, 0, 0, 0, 0.5f, zero, zero, zero, one, zero, zero, one, zero, one, parent, df, true);
        final GData3 tri3 = new GData3(0, 0, 0, 0, 0.5f, zero, zero, zero, one, one, zero, one, zero, one, parent, df, true);
        final GData3 tri4 = new GData3(0, 0, 0, 0, 0.5f, zero, zero, zero, one, one, zero, one, one, one, parent, df, true);

        final RTree cut = new RTree();

        cut.add(tri1);
        cut.add(tri2);
        cut.add(tri3);
        cut.add(tri4);

        assertEquals(4, cut.size());
    }

    @Test
    public void testRTreeWith4TrianglesAndOneQuadAndDoRayCasting() {
        final DatFile df = new DatFile(TEST);
        final BigDecimal one = BigDecimal.ONE;
        final BigDecimal zero = BigDecimal.ZERO;
        final GData1 parent = new GData1(0, 0, 0, 0, 0,
            View.ID, View.ACCURATE_ID, List.of(), TEST, TEST, 0, false,
            View.ID, View.ACCURATE_ID, df, null, false, false, Set.of(), null);

        final GData3 tri1 = new GData3(0, 0, 0, 0, 0.5f, zero, zero, zero, one, zero, zero, one, one, zero, parent, df, true);
        final GData3 tri2 = new GData3(0, 0, 0, 0, 0.5f, zero, zero, zero, one, zero, zero, one, zero, one, parent, df, true);
        final GData3 tri3 = new GData3(0, 0, 0, 0, 0.5f, zero, zero, zero, one, one, zero, one, zero, one, parent, df, true);
        final GData3 tri4 = new GData3(0, 0, 0, 0, 0.5f, zero, zero, zero, one, one, zero, one, one, one, parent, df, true);
        final GData4 quad = new GData4(0, 0, 0, 0, 0.5f, zero, zero, zero, one, zero, zero, one, one, zero, zero, one, zero, new Vector3d(), parent, df);

        final RTree cut = new RTree();

        cut.add(tri1);
        cut.add(tri2);
        cut.add(tri3);
        cut.add(tri4);
        cut.add(quad);

        // That is a line segment from [.5|.5|.5] to [.5|.5|-1.5]
        final float[] start = new float[] {.5f, .5f, .5f};
        final float[] end = new float[] {.5f, .5f, -1.5f};

        List<GData> result = cut.searchGeometryDataOnSegment(
                start[0], start[1], start[2],
                end[0], end[1], end[2]);

        assertEquals(3, result.size());
        assertEquals(5, cut.size());
    }
}
