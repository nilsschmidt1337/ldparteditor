package org.nschmidt.ldparteditor;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.data.GDataPNG;
import org.nschmidt.ldparteditor.data.Vertex;

@SuppressWarnings("java:S5960")
public class CalibrateImageTest {

    private static Matrix4f id() {
        return Matrix4f.setIdentity(new Matrix4f());
    }

    private Vector4f[] calibrate(Matrix4f imageMatrix, final Vector4f imageOffset,
            float angleA, float angleB, float angleC,
            final Vector4f imageScale, final Vector4f pivot, float scale) {

        Matrix4f imageBasis = Matrix4f.setIdentity(new Matrix4f());
        Matrix4f.rotate((float) (angleC / 180.0 * Math.PI), new Vector3f(0f, 0f, 1f), imageBasis, imageBasis);
        Matrix4f.rotate((float) (angleB / 180.0 * Math.PI), new Vector3f(1f, 0f, 0f), imageBasis, imageBasis);
        Matrix4f.rotate((float) (angleA / 180.0 * Math.PI), new Vector3f(0f, 1f, 0f), imageBasis, imageBasis);

        imageBasis = Matrix4f.invert(imageBasis, null);

        final Vector4f realImageOffset = (Vector4f) new Vector4f(imageMatrix.m30, imageMatrix.m31, imageMatrix.m32, 1000f).scale(.001f);
        final Vector4f pivotToImageCenter = Vector4f.sub(pivot, realImageOffset, null);
        final Vector4f pivotToImageCenterScaled = (Vector4f) pivotToImageCenter.scale(scale).scale(.5f);
        final Vector4f offsetDelta = (Vector4f) Matrix4f.transform(imageBasis, pivotToImageCenterScaled, null).negate();

        final Vector4f newImageOffset = Vector4f.add(imageOffset, offsetDelta, null);
        final Vector4f newImageScale = new Vector4f(imageScale.x * scale, imageScale.y * scale, 0f, 0f);
        return new Vector4f[] {newImageOffset, newImageScale};
    }

    @Test
    public void testImageScaleByTwoWithZeroOrigin() {
         Vector4f[] result = calibrate(id(), new Vector4f(), 0f, 0f, 0f, new Vector4f(1f, 1f, 0f, 1f), new Vector4f(), 2f);
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
         Vector4f[] result = calibrate(id().translate(new Vector3f(2000f, 4000f, 8000f)), new Vector4f(2f, 4f, 8f, 1f), 0f, 0f, 0f, new Vector4f(1f, 1f, 0f, 1f), new Vector4f(), 2f);
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
         Vector4f[] result = calibrate(id(), new Vector4f(0f, 0f, 0f, 1f), 0f, 0f, 0f, new Vector4f(1f, 1f, 0f, 1f), new Vector4f(1f, 1f, 0f, 1f), 2f);
         final Vector4f offset = result[0];
         final Vector4f scale = result[1];
         assertEquals(-1f, offset.x, 0f);
         assertEquals(-1f, offset.y, 0f);
         assertEquals(0f, offset.z, 0f);
         assertEquals(2f, scale.x, 0f);
         assertEquals(2f, scale.y, 0f);
    }

    @Test
    public void testImageScaleComplexExample() {
        final GDataPNG png = new GDataPNG("0 !LPE PNG 5 1 0 90 90 0 10 10 test.png", //$NON-NLS-1$
            new Vertex(BigDecimal.valueOf(5), BigDecimal.ONE, BigDecimal.ZERO),
            BigDecimal.valueOf(90), BigDecimal.valueOf(90), BigDecimal.valueOf(0),
            new Vertex(BigDecimal.valueOf(10), BigDecimal.valueOf(10), BigDecimal.ZERO), "test.png", null); //$NON-NLS-1$

        final Matrix4f maxtrix = png.getMatrix();
        Vector4f[] result = calibrate(maxtrix, new Vector4f(5f, 1f, 0f, 1f), 90f, 90f, 0f, new Vector4f(10f, 10f, 0f, 1f), new Vector4f(0f, 5f, 1f, 1f), 2f);
        final Vector4f offset = result[0];
        final Vector4f scale = result[1];
        assertEquals(5f, offset.x, 0.001f);
        assertEquals(1f, offset.y, 0.001f);
        assertEquals(0f, offset.z, 0.001f);
        assertEquals(20f, scale.x, 0.001f);
        assertEquals(20f, scale.y, 0.001f);
    }
}
