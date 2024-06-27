package org.nschmidt.ldparteditor;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Ignore;
import org.junit.Test;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.data.GDataPNG;
import org.nschmidt.ldparteditor.data.Vertex;

@SuppressWarnings("java:S5960")
public class CalibrateImageTest {

    private static final Matrix4f ID = Matrix4f.setIdentity(new Matrix4f());

    private Vector4f[] calibrate(final Matrix4f imageMatrix, final Vector4f imageOffset, final Vector4f imageScale, final Vector4f pivot, float scale) {
        final Vector4f newImageOffset = new Vector4f((imageOffset.x - pivot.x) * scale + pivot.x, (imageOffset.y - pivot.y) * scale + pivot.y, (imageOffset.z - pivot.z) * scale + pivot.z, 0f);
        final Vector4f newImageScale = new Vector4f(imageScale.x * scale, imageScale.y * scale, 0f, 0f);
        return new Vector4f[] {newImageOffset, newImageScale};
    }

    @Test
    public void testImageScaleByTwoWithZeroOrigin() {
         Vector4f[] result = calibrate(ID, new Vector4f(), new Vector4f(1f, 1f, 0f, 1f), new Vector4f(), 2f);
         final Vector4f offset = result[0];
         final Vector4f scale = result[1];
         assertEquals(0f, offset.x, 0f);
         assertEquals(0f, offset.y, 0f);
         assertEquals(0f, offset.z, 0f);
         assertEquals(2f, scale.x, 0f);
         assertEquals(2f, scale.y, 0f);
    }

    @Test
    public void testImageScaleByTwoWithShiftedOrigin() {
         Vector4f[] result = calibrate(ID, new Vector4f(2f, 4f, 8f, 1f), new Vector4f(1f, 1f, 0f, 1f), new Vector4f(), 2f);
         final Vector4f offset = result[0];
         final Vector4f scale = result[1];
         assertEquals(4f, offset.x, 0f);
         assertEquals(8f, offset.y, 0f);
         assertEquals(16f, offset.z, 0f);
         assertEquals(2f, scale.x, 0f);
         assertEquals(2f, scale.y, 0f);
    }

    @Test
    public void testImageScaleByTwoWithShiftedPivot() {
         Vector4f[] result = calibrate(ID, new Vector4f(0f, 0f, 0f, 1f), new Vector4f(1f, 1f, 0f, 1f), new Vector4f(1f, 1f, 0f, 1f), 2f);
         final Vector4f offset = result[0];
         final Vector4f scale = result[1];
         assertEquals(-1f, offset.x, 0f);
         assertEquals(-1f, offset.y, 0f);
         assertEquals(0f, offset.z, 0f);
         assertEquals(2f, scale.x, 0f);
         assertEquals(2f, scale.y, 0f);
    }

    @Test
    public void testImageScaleByTwoWithShiftedPivotAndUndoAgain() {
        Vector4f[] firstResult = calibrate(ID, new Vector4f(0f, 0f, 0f, 1f), new Vector4f(1f, 1f, 0f, 1f), new Vector4f(1f, 1f, 0f, 1f), 2f);
        final Vector4f firstOffset = firstResult[0];
        final Vector4f firstScale = firstResult[1];
        Vector4f[] finalResult = calibrate(ID, new Vector4f(firstOffset.x, firstOffset.y, firstOffset.z, 1f), new Vector4f(firstScale.x, firstScale.y, 0f, 1f), new Vector4f(1f, 1f, 0f, 1f), 0.5f);
        final Vector4f offset = finalResult[0];
        final Vector4f scale = finalResult[1];
        assertEquals(0f, offset.x, 0f);
        assertEquals(0f, offset.y, 0f);
        assertEquals(0f, offset.z, 0f);
        assertEquals(1f, scale.x, 0f);
        assertEquals(1f, scale.y, 0f);
    }

    @Test
    @Ignore
    // TODO Remove ignore asap
    public void testImageScaleComplexExample() {
        final GDataPNG png = new GDataPNG("0 !LPE PNG 5 1 0 90 90 0 10 10 test.png", //$NON-NLS-1$
            new Vertex(BigDecimal.valueOf(5), BigDecimal.ONE, BigDecimal.ZERO),
            BigDecimal.valueOf(90), BigDecimal.valueOf(90), BigDecimal.valueOf(0),
            new Vertex(BigDecimal.valueOf(10), BigDecimal.valueOf(10), BigDecimal.ZERO), "test.png", null); //$NON-NLS-1$

        final Matrix4f maxtrix = png.getMatrix();
        Vector4f[] result = calibrate(maxtrix, new Vector4f(5f, 1f, 0f, 1f), new Vector4f(10f, 10f, 0f, 1f), new Vector4f(0f, 5f, 1f, 1f), 2f);
        final Vector4f offset = result[0];
        final Vector4f scale = result[1];
        assertEquals(5f, offset.x, 0f);
        assertEquals(1f, offset.y, 0f);
        assertEquals(0f, offset.z, 0f);
        assertEquals(20f, scale.x, 0f);
        assertEquals(20f, scale.y, 0f);
    }
}
