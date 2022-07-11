/**
 * Transform.java
 *
 * Copyright 2014-2014 Michael Hoffer <info@michaelhoffer.de>. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of Michael Hoffer
 * <info@michaelhoffer.de>.
 */
package org.nschmidt.csg;

import org.lwjgl.util.vector.Matrix4f;

/**
 * Transform. Transformations (translation, rotation, scale) can be applied to
 * geometrical objects like {@link CSG}, {@link Polygon}, {@link Vertex2} and
 * {@link VectorCSGd}.
 *
 * This transform class uses the builder pattern to define combined
 * transformations.<br>
 * <br>
 *
 * <b>Example:</b>
 *
 * <blockquote>
 *
 * <pre>
 * // t applies rotation and translation
 * Transform t = Transform.unity().rotX(45).translate(2, 1, 0);
 * </pre>
 *
 * </blockquote>
 *
 * <b>TODO:</b> use quaternions for rotations.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class Transform {

    /**
     * Internal 4x4 matrix.
     */
    private final Matrix4f m;

    /**
     * Constructor.
     *
     * Creates a unit transform.
     */
    public Transform() {
        m = new Matrix4f();
        m.m00 = 1;
        m.m11 = 1;
        m.m22 = 1;
        m.m33 = 1;
    }

    /**
     * Returns a new unity transform.
     *
     * @return unity transform
     */
    static Transform unity() {
        return new Transform();
    }

    /**
     * Applies this transform to the specified vector.
     *
     * @param vec
     *            vector to transform
     *
     * @return the specified vector
     */
    VectorCSGd transform(VectorCSGd vec) {
        double x;
        double y;
        x = m.m00 * vec.x + m.m01 * vec.y + m.m02 * vec.z + m.m30;
        y = m.m10 * vec.x + m.m11 * vec.y + m.m12 * vec.z + m.m31;
        vec.z = m.m20 * vec.x + m.m21 * vec.y + m.m22 * vec.z + m.m32;
        vec.x = x;
        vec.y = y;

        return vec;
    }

    /**
     * Indicates whether this transform performs a mirror operation.
     *
     * @return <code>true</code> if this transform performs a mirror operation;
     *         <code>false</code> otherwise
     */
    public boolean isMirror() {
        return m.determinant() < 0;
    }

    @Override
    public String toString() {
        return m.toString();
    }

    Transform apply(Matrix4f matrix) {
        Matrix4f.mul(m, matrix, m);
        return this;
    }
}
