package org.nschmidt.ldparteditor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.GData3;
import org.nschmidt.ldparteditor.data.GData4;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.helper.math.Vector3d;
import org.nschmidt.ldparteditor.helper.math.rtree.BoundingBox;
import org.nschmidt.ldparteditor.helper.math.rtree.RNode;
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
    public void testParallelSegmentIntersection() {
        boolean result = RNode.hasSegmentIntersection(new Vector4f(0f, 0f, 0f, 0f), new float[] {1f, 0f, 0f}, 0f, 1f, 0f, 1f, 1f, 0f);
        assertFalse(result);
    }

    @Test
    public void testZeroSizedSegmentIntersection() {
        boolean result = RNode.hasSegmentIntersection(new Vector4f(0f, 0f, 0f, 0f), new float[] {1f, 0f, 0f}, 0f, 0f, 0f, 0f, 0f, 0f);
        assertFalse(result);
    }

    @Test
    public void testZeroSizedRaySegmentIntersection() {
        boolean result = RNode.hasSegmentIntersection(new Vector4f(0f, 0f, 0f, 0f), new float[] {0f, 0f, 0f}, 0f, 0f, 0f, 0f, 0f, 0f);
        assertFalse(result);
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

        cut.add(tri1, df.getVertexManager().getTriangles(), df.getVertexManager().getQuads());
        cut.add(tri2, df.getVertexManager().getTriangles(), df.getVertexManager().getQuads());
        cut.add(tri3, df.getVertexManager().getTriangles(), df.getVertexManager().getQuads());

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

        cut.add(tri1, df.getVertexManager().getTriangles(), df.getVertexManager().getQuads());
        cut.add(tri2, df.getVertexManager().getTriangles(), df.getVertexManager().getQuads());
        cut.add(tri3, df.getVertexManager().getTriangles(), df.getVertexManager().getQuads());
        cut.add(tri4, df.getVertexManager().getTriangles(), df.getVertexManager().getQuads());

        assertEquals(4, cut.size());
    }

    @Test
    public void testRTreeWith4TrianglesAndOneQuadAndDoIntersectionTest() {
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

        cut.add(tri1, df.getVertexManager().getTriangles(), df.getVertexManager().getQuads());
        cut.add(tri2, df.getVertexManager().getTriangles(), df.getVertexManager().getQuads());
        cut.add(tri3, df.getVertexManager().getTriangles(), df.getVertexManager().getQuads());
        cut.add(tri4, df.getVertexManager().getTriangles(), df.getVertexManager().getQuads());
        cut.add(quad, df.getVertexManager().getTriangles(), df.getVertexManager().getQuads());

        // That is a line segment from [.5|.5|.5] to [.5|.5|-1.5]
        final float[] start = new float[] {.5f, .5f, .5f};
        final float[] end = new float[] {.5f, .5f, -1.5f};

        final BoundingBox bb = new BoundingBox();
        bb.insert(tri4, df.getVertexManager().getTriangles(), df.getVertexManager().getQuads());

        List<GData> result = cut.searchGeometryDataOnSegment(
                start[0], start[1], start[2],
                end[0], end[1], end[2],
                bb, df.getVertexManager().getTriangles(), df.getVertexManager().getQuads());

        assertEquals(4, result.size());
        assertEquals(5, cut.size());
    }

    @Test
    public void testRTreeWithTwoOverlappingTrianglesAndDoIntersectionTest() {
        final DatFile df = new DatFile(TEST);
        final BigDecimal two = BigDecimal.valueOf(2L);
        final BigDecimal one = BigDecimal.ONE;
        final BigDecimal zero = BigDecimal.ZERO;
        final GData1 parent = new GData1(0, 0, 0, 0, 0,
            View.ID, View.ACCURATE_ID, List.of(), TEST, TEST, 0, false,
            View.ID, View.ACCURATE_ID, df, null, false, false, Set.of(), null);

        final GData3 tri1 = new GData3(0, 0, 0, 0, 0.5f, zero, zero, zero, two, zero, zero, zero, two, zero, parent, df, true);
        final GData3 tri2 = new GData3(0, 0, 0, 0, 0.5f, one, one, one, one, one, zero, one, zero, one, parent, df, true);

        final RTree cut = new RTree();

        cut.add(tri1, df.getVertexManager().getTriangles(), df.getVertexManager().getQuads());
        cut.add(tri2, df.getVertexManager().getTriangles(), df.getVertexManager().getQuads());

        Set<GData> result1 = cut.searchForIntersections(tri1, df.getVertexManager().getTriangles(), df.getVertexManager().getQuads());
        Set<GData> result2 = cut.searchForIntersections(tri2, df.getVertexManager().getTriangles(), df.getVertexManager().getQuads());

        assertTrue(result1.contains(tri2));
        assertEquals(1, result1.size());

        assertTrue(result2.contains(tri1));
        assertEquals(1, result2.size());

        assertEquals(2, cut.size());
    }

    @Test
    public void testRTreeWithTwoDisjunctTrianglesAndDoIntersectionTest() {
        final DatFile df = new DatFile(TEST);
        final BigDecimal one = BigDecimal.ONE;
        final BigDecimal zero = BigDecimal.ZERO;
        final GData1 parent = new GData1(0, 0, 0, 0, 0,
            View.ID, View.ACCURATE_ID, List.of(), TEST, TEST, 0, false,
            View.ID, View.ACCURATE_ID, df, null, false, false, Set.of(), null);

        final GData3 tri1 = new GData3(0, 0, 0, 0, 0.5f, zero, zero, zero, one, zero, zero, zero, one, zero, parent, df, true);
        final GData3 tri2 = new GData3(0, 0, 0, 0, 0.5f, zero, zero, one, one, zero, one, zero, one, one, parent, df, true);

        final RTree cut = new RTree();

        cut.add(tri1, df.getVertexManager().getTriangles(), df.getVertexManager().getQuads());
        cut.add(tri2, df.getVertexManager().getTriangles(), df.getVertexManager().getQuads());

        Set<GData> result = cut.searchForIntersections(tri1, df.getVertexManager().getTriangles(), df.getVertexManager().getQuads());

        assertTrue(result.isEmpty());
    }

    @Test
    public void testRTreeWithTwoOverlappingTrianglesOnSamePlaneAndDoIntersectionTest() {
        final DatFile df = new DatFile(TEST);
        final GData1 parent = new GData1(0, 0, 0, 0, 0,
            View.ID, View.ACCURATE_ID, List.of(), TEST, TEST, 0, false,
            View.ID, View.ACCURATE_ID, df, null, false, false, Set.of(), null);

        // Triangles:
        // 3 16 12.44 -25.75 0 .39 -20.2 0 16.72 -5.46 0
        // 3 16 10.53 -21.31 0 6.1 -18.62 0 13.39 -11.17 0
        final GData3 outerTriangle = new GData3(0, 0, 0, 0, 0.5f, d(12.44), d(-25.75), d(0), d(.39), d(-20.2), d(0), d(16.72), d(-5.46), d(0), parent, df, true);
        final GData3 innerTriangle = new GData3(0, 0, 0, 0, 0.5f, d(10.53), d(-21.31), d(0), d(6.1), d(-18.62), d(0), d(13.39), d(-11.17), d(0), parent, df, true);

        final RTree cut = new RTree();

        cut.add(outerTriangle, df.getVertexManager().getTriangles(), df.getVertexManager().getQuads());
        cut.add(innerTriangle, df.getVertexManager().getTriangles(), df.getVertexManager().getQuads());

        Set<GData> result = cut.searchForIntersections(innerTriangle, df.getVertexManager().getTriangles(), df.getVertexManager().getQuads());

        assertTrue(result.contains(outerTriangle));
        assertEquals(1, result.size());
        assertEquals(2, cut.size());
    }

    @Test
    public void testRTreeWithTwoPartiallyOverlappingTrianglesOnSamePlaneAndDoIntersectionTest() {
        final DatFile df = new DatFile(TEST);
        final GData1 parent = new GData1(0, 0, 0, 0, 0,
            View.ID, View.ACCURATE_ID, List.of(), TEST, TEST, 0, false,
            View.ID, View.ACCURATE_ID, df, null, false, false, Set.of(), null);

        // Triangles:
        // 3 16 7.32 -29.42 0 2.31 -26.06 0 8.22 -20.5 0
        // 3 16 4.81 -29.52 0 3.16 -28.17 0 9.67 -26.26 0
        final GData3 tri1 = new GData3(0, 0, 0, 0, 0.5f, d(7.32), d(-29.42), d(0), d(2.31), d(-26.06), d(0), d(8.22), d(-20.5), d(0), parent, df, true);
        final GData3 tri2 = new GData3(0, 0, 0, 0, 0.5f, d(4.81), d(-29.52), d(0), d(3.16), d(-28.17), d(0), d(9.67), d(-26.26), d(0), parent, df, true);

        final RTree cut = new RTree();

        cut.add(tri1, df.getVertexManager().getTriangles(), df.getVertexManager().getQuads());
        cut.add(tri2, df.getVertexManager().getTriangles(), df.getVertexManager().getQuads());

        Set<GData> result1 = cut.searchForIntersections(tri1, df.getVertexManager().getTriangles(), df.getVertexManager().getQuads());
        Set<GData> result2 = cut.searchForIntersections(tri2, df.getVertexManager().getTriangles(), df.getVertexManager().getQuads());

        assertTrue(result1.contains(tri2));
        assertEquals(1, result1.size());

        assertTrue(result2.contains(tri1));
        assertEquals(1, result2.size());

        assertEquals(2, cut.size());
    }

    @Test
    public void testRTreeWithTwoTrianglesWithCommonPointTest() {
        final DatFile df = new DatFile(TEST);
        final GData1 parent = new GData1(0, 0, 0, 0, 0,
            View.ID, View.ACCURATE_ID, List.of(), TEST, TEST, 0, false,
            View.ID, View.ACCURATE_ID, df, null, false, false, Set.of(), null);

        // Triangles:
        // 3 16 .03 -17.51 0 -4.48 -17.67 0 2.42 -11.47 0
        // 3 16 2.62 -17.84 0 -1.49 -19.4 0 .03 -17.51 0
        final GData3 tri1 = new GData3(0, 0, 0, 0, 0.5f, d(.03), d(-17.51), d(0), d(-4.48), d(-17.67), d(0), d(2.42), d(-11.47), d(0), parent, df, true);
        final GData3 tri2 = new GData3(15, 1f, 1f, 1f, 0.5f, d(2.62), d(-17.84), d(0), d(-1.49), d(-19.4), d(0), d(.03), d(-17.51), d(0), parent, df, true);

        final RTree cut = new RTree();

        cut.add(tri1, df.getVertexManager().getTriangles(), df.getVertexManager().getQuads());
        cut.add(tri2, df.getVertexManager().getTriangles(), df.getVertexManager().getQuads());

        Set<GData> result1 = cut.searchForIntersections(tri1, df.getVertexManager().getTriangles(), df.getVertexManager().getQuads());
        Set<GData> result2 = cut.searchForIntersections(tri2, df.getVertexManager().getTriangles(), df.getVertexManager().getQuads());

        assertTrue(result1.isEmpty());
        assertTrue(result2.isEmpty());

        assertEquals(2, cut.size());
    }

    private BigDecimal d(double value) {
        return BigDecimal.valueOf(value);
    }
}
