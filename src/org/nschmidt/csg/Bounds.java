/**
 * Bounds.java
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

/**
 * Bounding box for CSGs.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class Bounds {

    private final Vector3d min;
    private final Vector3d max;

    /**
     * Constructor.
     *
     * @param min
     *            min x,y,z values
     * @param max
     *            max x,y,z values
     */
    public Bounds(Vector3d min, Vector3d max) {
        this.min = min.clone();
        this.max = max.clone();
    }

    public Bounds() {
        this.min = new Vector3d(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        this.max = new Vector3d(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
    }

    /**
     * Indicates whether the specified bounding box intersects with this
     * bounding box (check includes box boundary).
     *
     * @param b
     *            box to check
     * @return {@code true} if the bounding box intersects this bounding box;
     *         {@code false} otherwise
     */
    public boolean intersects(Bounds b) {

        if (b.min.x > max.x || b.max.x < min.x) {
            return false;
        }
        if (b.min.y > max.y || b.max.y < min.y) {
            return false;
        }
        if (b.min.z > max.z || b.max.z < min.z) {
            return false;
        }

        return true;

    }

    /**
     * @return the min x,y,z values
     */
    public Vector3d getMin() {
        return min;
    }

    /**
     * @return the max x,y,z values
     */
    public Vector3d getMax() {
        return max;
    }

    public void union(Bounds other) {
        Vector3d o_max = other.max;
        Vector3d o_min = other.min;
        if (o_min.x > max.x) max.x = o_min.x;
        if (o_min.y > max.y) max.y = o_min.y;
        if (o_min.z > max.z) max.z = o_min.z;
        if (o_min.x < min.x) min.x = o_min.x;
        if (o_min.y < min.y) min.y = o_min.y;
        if (o_min.z < min.z) min.z = o_min.z;
        if (o_max.x > max.x) max.x = o_max.x;
        if (o_max.y > max.y) max.y = o_max.y;
        if (o_max.z > max.z) max.z = o_max.z;
        if (o_max.x < min.x) min.x = o_max.x;
        if (o_max.y < min.y) min.y = o_max.y;
        if (o_max.z < min.z) min.z = o_max.z;
    }

    @Override
    public String toString() {
        return min.toString() + "->" + max.toString(); //$NON-NLS-1$
    }
}
