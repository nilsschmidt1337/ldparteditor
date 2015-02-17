import java.math.BigDecimal;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.data.Matrix;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.main.LDPartEditor;


// Precise Matrix test

        Matrix m1 = new Matrix(
                BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO,
                BigDecimal.TEN.negate(), BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ONE);
        NLogger.debug(LDPartEditor.class, "m1:\n" + m1.toString()); //$NON-NLS-1$
        Matrix m2 = new Matrix(
                BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.TEN, BigDecimal.TEN,
                BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE);
        NLogger.debug(LDPartEditor.class, "m2:\n" + m2.toString()); //$NON-NLS-1$
        Matrix m3 = Matrix.mul(m1, m2);

        Matrix4f m4 = new Matrix4f();
        m4.m00 = 1f; m4.m01 = 0f; m4.m02 = 0f; m4.m03 = 0f;
        m4.m10 = 0f; m4.m11 = 1f; m4.m12 = 1f; m4.m13 = 0f;
        m4.m20 = -10f; m4.m21 = 10f; m4.m22 = 10f; m4.m23 = 0f;
        m4.m30 = 0f; m4.m31 = 10f; m4.m32 = 0f; m4.m33 = 1f;
        NLogger.debug(LDPartEditor.class, "m4:\n" + m4.toString()); //$NON-NLS-1$

        Matrix4f m5 = new Matrix4f();
        m5.m00 = 1f; m5.m01 = 0f; m5.m02 = 10f; m5.m03 = 10f;
        m5.m10 = 0f; m5.m11 = 1f; m5.m12 = 0f; m5.m13 = 0f;
        m5.m20 = 0f; m5.m21 = 0f; m5.m22 = 1f; m5.m23 = 0f;
        m5.m30 = 0f; m5.m31 = 0f; m5.m32 = 0f; m5.m33 = 1f;
        NLogger.debug(LDPartEditor.class, "m5:\n" + m5.toString()); //$NON-NLS-1$


        Matrix4f m6 = new Matrix4f();

        Matrix4f.mul(m4, m5, m6);


        NLogger.debug(LDPartEditor.class, "Is:\n" + m3.toString()); //$NON-NLS-1$
        NLogger.debug(LDPartEditor.class, "Should be:\n" + m6.toString()); //$NON-NLS-1$


        BigDecimal x = new BigDecimal(1);
        BigDecimal y = new BigDecimal(2);
        BigDecimal z = new BigDecimal(3);

        BigDecimal[] t = m3.transform(x, y, z);

        Vector4f v1 = new Vector4f(t[0].floatValue(), t[1].floatValue(), t[2].floatValue(), t[3].floatValue());

        Vector4f v2 = new Vector4f(1f, 2f, 3f, 1f);
        Matrix4f.transform(m6, v2, v2);

        NLogger.debug(LDPartEditor.class, "Is:\n" + v1.toString()); //$NON-NLS-1$
        NLogger.debug(LDPartEditor.class, "Should be:\n" + v2.toString()); //$NON-NLS-1$


        Matrix4f m4 = new Matrix4f();
        m4.m00 = rnd(); m4.m01 = rnd(); m4.m02 = rnd(); m4.m03 = rnd();
        m4.m10 = rnd(); m4.m11 = rnd(); m4.m12 = rnd(); m4.m13 = rnd();
        m4.m20 = rnd(); m4.m21 = rnd(); m4.m22 = rnd(); m4.m23 = rnd();
        m4.m30 = rnd(); m4.m31 = rnd(); m4.m32 = rnd(); m4.m33 = rnd();


        Matrix M = new Matrix(
                new BigDecimal(m4.m00), new BigDecimal(m4.m01), new BigDecimal(m4.m02), new BigDecimal(m4.m03),
                new BigDecimal(m4.m10), new BigDecimal(m4.m11), new BigDecimal(m4.m12), new BigDecimal(m4.m13),
                new BigDecimal(m4.m20), new BigDecimal(m4.m21), new BigDecimal(m4.m22), new BigDecimal(m4.m23),
                new BigDecimal(m4.m30), new BigDecimal(m4.m31), new BigDecimal(m4.m32), new BigDecimal(m4.m33)
                );


        Matrix4f.invert(m4, m4);
        NLogger.debug(LDPartEditor.class, "m4:\n" + m4.toString()); //$NON-NLS-1$


        NLogger.debug(LDPartEditor.class, "\n" + M.inverse().toString()); //$NON-NLS-1$


        private static float rnd() {
            return (float) Math.max(Math.random(), 0.001);
        }
