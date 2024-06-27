package org.nschmidt.ldparteditor;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Ignore;
import org.junit.Test;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.data.GDataPNG;
import org.nschmidt.ldparteditor.data.Vertex;

@SuppressWarnings("java:S5960")
public class CalibrateImageTest {

    private static final Matrix4f ID = Matrix4f.setIdentity(new Matrix4f());

    private Vector4f[] calibrate(Matrix4f imageMatrix, final Vector4f imageOffset,
            float angleA, float angleB, float angleC,
            final Vector4f imageScale, final Vector4f pivot, float scale) {

        Matrix4f imageBasis = Matrix4f.setIdentity(new Matrix4f());
        Matrix4f.rotate((float) (angleC / 180.0 * Math.PI), new Vector3f(0f, 0f, 1f), imageBasis, imageBasis);
        Matrix4f.rotate((float) (angleB / 180.0 * Math.PI), new Vector3f(1f, 0f, 0f), imageBasis, imageBasis);
        Matrix4f.rotate((float) (angleA / 180.0 * Math.PI), new Vector3f(0f, 1f, 0f), imageBasis, imageBasis);

        imageMatrix = imageMatrix.scale(new Vector3f(1E-3f, 1E-3f, 1E-3f));
        imageMatrix.m30 = 0f;
        imageMatrix.m31 = 0f;
        imageMatrix.m32 = 0f;
        imageBasis = repairSingularMatrix(imageBasis);
        imageBasis = Matrix4f.invert(imageBasis, null);

        imageMatrix = imageMatrix.scale(new Vector3f(scale, scale, scale));
        final Vector4f pivotOnImage = pivot;// Matrix4f.transform(imageMatrix, pivot, null);
        final Vector4f newImageOffset = new Vector4f((imageOffset.x - pivotOnImage.x) * scale + pivotOnImage.x, (imageOffset.y - pivotOnImage.y) * scale + pivotOnImage.y, (imageOffset.z - pivotOnImage.z) * scale + pivotOnImage.z, 0f);
        final Vector4f newImageScale = new Vector4f(imageScale.x * scale, imageScale.y * scale, 0f, 0f);
        return new Vector4f[] {newImageOffset, newImageScale};
    }

    private Matrix4f repairSingularMatrix(Matrix4f imageMatrix) {
        boolean m00 = Float.compare(imageMatrix.m00, 0f) == 0;
        boolean m01 = Float.compare(imageMatrix.m01, 0f) == 0;
        boolean m02 = Float.compare(imageMatrix.m02, 0f) == 0;
        boolean m10 = Float.compare(imageMatrix.m10, 0f) == 0;
        boolean m11 = Float.compare(imageMatrix.m11, 0f) == 0;
        boolean m12 = Float.compare(imageMatrix.m12, 0f) == 0;
        boolean m20 = Float.compare(imageMatrix.m20, 0f) == 0;
        boolean m21 = Float.compare(imageMatrix.m21, 0f) == 0;
        boolean m22 = Float.compare(imageMatrix.m22, 0f) == 0;

        if (m00 && (m01 && m02 || m10 && m20)) {
            imageMatrix.m00 = 1f;
        }
        if (m11 && (m10 && m12 || m01 && m21)) {
            imageMatrix.m11 = 1f;
        }
        if (m22 && (m20 && m21 || m02 && m12)) {
            imageMatrix.m22 = 1f;
        }

        return imageMatrix;
    }

    @Test
    public void testImageScaleByTwoWithZeroOrigin() {
         Vector4f[] result = calibrate(ID, new Vector4f(), 0f, 0f, 0f, new Vector4f(1f, 1f, 0f, 1f), new Vector4f(), 2f);
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
         Vector4f[] result = calibrate(ID, new Vector4f(2f, 4f, 8f, 1f), 0f, 0f, 0f, new Vector4f(1f, 1f, 0f, 1f), new Vector4f(), 2f);
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
         Vector4f[] result = calibrate(ID, new Vector4f(0f, 0f, 0f, 1f), 0f, 0f, 0f, new Vector4f(1f, 1f, 0f, 1f), new Vector4f(1f, 1f, 0f, 1f), 2f);
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
        Vector4f[] firstResult = calibrate(ID, new Vector4f(0f, 0f, 0f, 1f), 0f, 0f, 0f, new Vector4f(1f, 1f, 0f, 1f), new Vector4f(1f, 1f, 0f, 1f), 2f);
        final Vector4f firstOffset = firstResult[0];
        final Vector4f firstScale = firstResult[1];
        Vector4f[] finalResult = calibrate(ID, new Vector4f(firstOffset.x, firstOffset.y, firstOffset.z, 1f), 0f, 0f, 0f, new Vector4f(firstScale.x, firstScale.y, 0f, 1f), new Vector4f(1f, 1f, 0f, 1f), 0.5f);
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
        Vector4f[] result = calibrate(maxtrix, new Vector4f(5f, 1f, 0f, 1f), 90f, 90f, 0f, new Vector4f(10f, 10f, 0f, 1f), new Vector4f(0f, 5f, 1f, 1f), 2f);
        final Vector4f offset = result[0];
        final Vector4f scale = result[1];
        assertEquals(5f, offset.x, 0f);
        assertEquals(1f, offset.y, 0f);
        assertEquals(0f, offset.z, 0f);
        assertEquals(20f, scale.x, 0f);
        assertEquals(20f, scale.y, 0f);
    }
}
