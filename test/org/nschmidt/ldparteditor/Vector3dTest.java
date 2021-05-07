package org.nschmidt.ldparteditor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.Test;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;

@SuppressWarnings("java:S5960")
public class Vector3dTest {

    private static final float FLOAT_DELTA_NULL = 0f;
    private static final float FLOAT_DELTA = 1E-6f;

    @Test
    public void testNullVectorConstrucor() {
        final Vector3d nullVector = new Vector3d();

        assertIsNullVector(nullVector);
    }

    @Test
    public void testNullVectorAddition() {
        final Vector3d nullVector = new Vector3d();
        final Vector3d sum = Vector3d.add(nullVector, nullVector);

        assertIsNullVector(sum);
    }

    private void assertIsNullVector(Vector3d nullVector) {
        assertTrue(BigDecimal.ZERO.compareTo(nullVector.x) == 0);
        assertTrue(BigDecimal.ZERO.compareTo(nullVector.y) == 0);
        assertTrue(BigDecimal.ZERO.compareTo(nullVector.z) == 0);
        assertEquals(0f, nullVector.getXf(), FLOAT_DELTA_NULL);
        assertEquals(0f, nullVector.getYf(), FLOAT_DELTA_NULL);
        assertEquals(0f, nullVector.getZf(), FLOAT_DELTA_NULL);
        assertEquals(0f, nullVector.length().floatValue(), FLOAT_DELTA);
    }
}
