package org.nschmidt.ldparteditor;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

@SuppressWarnings("java:S5960")
public class CalibrateImageTest {

    private static final Matrix4f ID = Matrix4f.setIdentity(new Matrix4f());

    private Vector4f[] calibrate(final Matrix4f imageMatrix, final Vector4f imageOffset, final Vector4f imageScale, final Vector4f pivot, float scale) {
        final Vector4f newImageOffset = imageOffset;
        final Vector4f newImageScale = new Vector4f(imageScale.x * scale, imageScale.y * scale, 0f, 0f);
        return new Vector4f[] {newImageOffset, newImageScale};
    }

    @Test
    public void testImageScaleByTwoWithZeroOrigin() {
         Vector4f[] result = calibrate(ID, new Vector4f(), new Vector4f(1f, 1f, 0f, 0f), new Vector4f(), 2f);
         final Vector4f offset = result[0];
         final Vector4f scale = result[1];
         assertEquals(0f, offset.x, 0f);
         assertEquals(0f, offset.y, 0f);
         assertEquals(0f, offset.z, 0f);
         assertEquals(2f, scale.x, 0f);
         assertEquals(2f, scale.y, 0f);
    }
}
