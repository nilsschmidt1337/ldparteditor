/* MIT - License

Copyright (c) 2012 - this year, Nils Schmidt

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package org.nschmidt.ldparteditor.helpers.math;

import java.math.BigDecimal;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.data.Matrix;
import org.nschmidt.ldparteditor.data.ProtractorHelper;

public enum MatrixOperations {
    INSTANCE;

    private static double EPSILON = 0.01;

    /**
     * Returns an approximted rotation matrix from an input matrix.
     * Useful for moving the manipulator to a subfile
     * @param m
     * @return
     */
    public static Matrix extractRotation(final Matrix m) {
        // TODO Needs implementation!
        return m;
    }

    /**
     * Returns matrix without rotation and translation components.
     * Useful for moving a subfile to the maniulator position
     * @param m
     * @return
     */
    public static Matrix removeRotationAndTranslation(final Matrix m) {
        // TODO Needs implementation!
        return m;
    }

    public static void moveManipulatorToSubfileMatrix(Composite3D c3d, Matrix M, Matrix4f m) {
        c3d.getManipulator().getPosition().set(m.m30, m.m31, m.m32, 1f);
        c3d.getManipulator().setAccuratePosition(M.M30, M.M31, M.M32);
        Vector3f x = new Vector3f(m.m00, m.m01, m.m02);
        x.normalise();
        Vector3f y = new Vector3f(m.m10, m.m11, m.m12);
        y.normalise();
        Vector3f z = new Vector3f(m.m20, m.m21, m.m22);
        z.normalise();

        final double dxy = Math.abs(Vector3f.dot(x, y));
        final double dxz = Math.abs(Vector3f.dot(x, z));
        final double dyz = Math.abs(Vector3f.dot(y, z));

        if (dxy > EPSILON) {
            BigDecimal[] ny = ProtractorHelper.changeAngle(90.0, new Vector3d(), new Vector3d(new BigDecimal(x.x), new BigDecimal(x.y), new BigDecimal(x.z)), new Vector3d(new BigDecimal(y.x), new BigDecimal(y.y), new BigDecimal(y.z)), 7, 10);
            y.set(ny[0].floatValue(), ny[1].floatValue(), ny[2].floatValue());
        }

        if (dxz > EPSILON) {
            BigDecimal[] nz1 = ProtractorHelper.changeAngle(90.0, new Vector3d(), new Vector3d(new BigDecimal(x.x), new BigDecimal(x.y), new BigDecimal(x.z)), new Vector3d(new BigDecimal(z.x), new BigDecimal(z.y), new BigDecimal(z.z)), 7, 10);
            z.set(nz1[0].floatValue(), nz1[1].floatValue(), nz1[2].floatValue());
        }

        if (dyz > EPSILON) {
            BigDecimal[] nz2 = ProtractorHelper.changeAngle(90.0, new Vector3d(), new Vector3d(new BigDecimal(y.x), new BigDecimal(y.y), new BigDecimal(y.z)), new Vector3d(new BigDecimal(z.x), new BigDecimal(z.y), new BigDecimal(z.z)), 7, 10);
            z.set(nz2[0].floatValue(), nz2[1].floatValue(), nz2[2].floatValue());
        }

        c3d.getManipulator().getXaxis().set(x.x, x.y, x.z, 1f);
        c3d.getManipulator().getYaxis().set(y.x, y.y, y.z, 1f);
        c3d.getManipulator().getZaxis().set(z.x, z.y, z.z, 1f);
        c3d.getManipulator().setAccurateXaxis(new BigDecimal(c3d.getManipulator().getXaxis().x), new BigDecimal(c3d.getManipulator().getXaxis().y),
                new BigDecimal(c3d.getManipulator().getXaxis().z));
        c3d.getManipulator().setAccurateYaxis(new BigDecimal(c3d.getManipulator().getYaxis().x), new BigDecimal(c3d.getManipulator().getYaxis().y),
                new BigDecimal(c3d.getManipulator().getYaxis().z));
        c3d.getManipulator().setAccurateZaxis(new BigDecimal(c3d.getManipulator().getZaxis().x), new BigDecimal(c3d.getManipulator().getZaxis().y),
                new BigDecimal(c3d.getManipulator().getZaxis().z));
    }

    public static void moveManipulatorToCSGMatrix(Composite3D c3d, Matrix M, Matrix4f m) {
        c3d.getManipulator().getPosition().set(m.m30, m.m31, m.m32, 1f);
        c3d.getManipulator().setAccuratePosition(M.M30, M.M31, M.M32);
        Vector3f x = new Vector3f(m.m00, m.m01, m.m02);
        x.normalise();
        Vector3f y = new Vector3f(m.m10, m.m11, m.m12);
        y.normalise();
        Vector3f z = new Vector3f(m.m20, m.m21, m.m22);
        z.normalise();

        final double dxy = Math.abs(Vector3f.dot(x, y));
        final double dxz = Math.abs(Vector3f.dot(x, z));
        final double dyz = Math.abs(Vector3f.dot(y, z));

        if (dxy > EPSILON) {
            BigDecimal[] ny = ProtractorHelper.changeAngle(90.0, new Vector3d(), new Vector3d(new BigDecimal(x.x), new BigDecimal(x.y), new BigDecimal(x.z)), new Vector3d(new BigDecimal(y.x), new BigDecimal(y.y), new BigDecimal(y.z)), 7, 10);
            y.set(ny[0].floatValue(), ny[1].floatValue(), ny[2].floatValue());
        }

        if (dxz > EPSILON) {
            BigDecimal[] nz1 = ProtractorHelper.changeAngle(90.0, new Vector3d(), new Vector3d(new BigDecimal(x.x), new BigDecimal(x.y), new BigDecimal(x.z)), new Vector3d(new BigDecimal(z.x), new BigDecimal(z.y), new BigDecimal(z.z)), 7, 10);
            z.set(nz1[0].floatValue(), nz1[1].floatValue(), nz1[2].floatValue());
        }

        if (dyz > EPSILON) {
            BigDecimal[] nz2 = ProtractorHelper.changeAngle(90.0, new Vector3d(), new Vector3d(new BigDecimal(y.x), new BigDecimal(y.y), new BigDecimal(y.z)), new Vector3d(new BigDecimal(z.x), new BigDecimal(z.y), new BigDecimal(z.z)), 7, 10);
            z.set(nz2[0].floatValue(), nz2[1].floatValue(), nz2[2].floatValue());
        }

        c3d.getManipulator().getXaxis().set(x.x, x.y, x.z, 1f);
        c3d.getManipulator().getYaxis().set(y.x, y.y, y.z, 1f);
        c3d.getManipulator().getZaxis().set(z.x, z.y, z.z, 1f);
        c3d.getManipulator().setAccurateXaxis(new BigDecimal(c3d.getManipulator().getXaxis().x), new BigDecimal(c3d.getManipulator().getXaxis().y),
                new BigDecimal(c3d.getManipulator().getXaxis().z));
        c3d.getManipulator().setAccurateYaxis(new BigDecimal(c3d.getManipulator().getYaxis().x), new BigDecimal(c3d.getManipulator().getYaxis().y),
                new BigDecimal(c3d.getManipulator().getYaxis().z));
        c3d.getManipulator().setAccurateZaxis(new BigDecimal(c3d.getManipulator().getZaxis().x), new BigDecimal(c3d.getManipulator().getZaxis().y),
                new BigDecimal(c3d.getManipulator().getZaxis().z));
    }
}
