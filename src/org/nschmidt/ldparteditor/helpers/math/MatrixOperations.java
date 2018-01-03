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
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.data.Matrix;
import org.nschmidt.ldparteditor.enums.View;

public enum MatrixOperations {
    INSTANCE;

    private static double EPSILON = 0.01;

    /**
     * Returns matrix without rotation and translation components.
     * Useful for moving a subfile to the manipulator position
     * @param M
     * @return
     */
    public static Matrix removeRotationAndTranslation(final Matrix M) {

        // TODO Needs BigDecimal accuracy?

        // Quick test with FP arithmetic if this removal is necessary
        {
            Vector3f x = new Vector3f(M.M00.floatValue(), M.M01.floatValue(), M.M02.floatValue());
            x.normalise();
            Vector3f y = new Vector3f(M.M10.floatValue(), M.M11.floatValue(), M.M12.floatValue());
            y.normalise();
            Vector3f z = new Vector3f(M.M20.floatValue(), M.M21.floatValue(), M.M22.floatValue());
            z.normalise();

            final double dxy = Math.abs(Vector3f.dot(x, y));
            final double dxz = Math.abs(Vector3f.dot(x, z));
            final double dyz = Math.abs(Vector3f.dot(y, z));

            if (dxy < EPSILON && dxz < EPSILON && dyz < EPSILON) {
                return View.ACCURATE_ID;
            }
        }

        final Matrix4f m = new Matrix4f();
        m.setIdentity();
        Vector3f x = new Vector3f(M.M00.floatValue(), M.M01.floatValue(), M.M02.floatValue());
        Vector3f y = new Vector3f(M.M10.floatValue(), M.M11.floatValue(), M.M12.floatValue());
        Vector3f z = new Vector3f(M.M20.floatValue(), M.M21.floatValue(), M.M22.floatValue());
        final float xl = x.length();
        final float yl = y.length();
        final float zl = z.length();
        final double xy_angle = Vector3d.angleRad(new Vector3d(M.M00, M.M01, M.M02), new Vector3d(M.M10, M.M11, M.M12));
        final double xz_angle = Vector3d.angleRad(new Vector3d(M.M00, M.M01, M.M02), new Vector3d(M.M20, M.M21, M.M22));
        final double yz_angle = Vector3d.angleRad(new Vector3d(M.M10, M.M11, M.M12), new Vector3d(M.M20, M.M21, M.M22));

        m.m10 = (float) (Math.cos(xy_angle) * yl);
        m.m11 = (float) (Math.sin(xy_angle) * yl);

        Vector3f pz = new Vector3f();
        Vector3f.cross(x, y, pz);
        pz.normalise();
        z.normalise();

        final float z_dir = Vector3f.dot(z, pz);
        final float z_coeff = z_dir > 0 ? 1f : -1f;

        m.m20 = (float) (Math.cos(xz_angle) * zl);
        m.m21 = (float) (Math.cos(yz_angle) * zl);
        m.m22 = (float) (Math.sin(xz_angle) * Math.sin(yz_angle) * zl * z_coeff);

        m.m00 = xl;

        return new Matrix(m);
    }

    public static void moveManipulatorToSubfileOrCSGMatrix(Composite3D c3d, Matrix M, Matrix4f m) {
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
            Vector4f ny = MathHelper.getNearestPointToLine(0, 0, 0, x.x, x.y, x.z, y.x, y.y, y.z);
            Vector4f.sub(new Vector4f(y.x, y.y, y.z, 1f), ny, ny);
            ny.normalise(ny);
            y.set(ny.x, ny.y, ny.z);
        }

        if (dxz > EPSILON) {
            Vector4f nz1 = MathHelper.getNearestPointToLine(0, 0, 0, x.x, x.y, x.z, z.x, z.y, z.z);
            Vector4f.sub(new Vector4f(z.x, z.y, z.z, 1f), nz1, nz1);
            nz1.normalise(nz1);
            z.set(nz1.x, nz1.y, nz1.z);
        }

        if (dyz > EPSILON) {
            Vector4f nz2 = MathHelper.getNearestPointToLine(0, 0, 0, y.x, y.y, y.z, z.x, z.y, z.z);
            Vector4f.sub(new Vector4f(z.x, z.y, z.z, 1f), nz2, nz2);
            nz2.normalise(nz2);
            z.set(nz2.x, nz2.y, nz2.z);
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
