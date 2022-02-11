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

    private final VectorCSGd min;
    private final VectorCSGd max;

    /**
     * Constructor.
     *
     * @param min
     *            min x,y,z values
     * @param max
     *            max x,y,z values
     */
    public Bounds(VectorCSGd min, VectorCSGd max) {
        this.min = min.createClone();
        this.max = max.createClone();
    }

    public Bounds() {
        this.min = new VectorCSGd(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        this.max = new VectorCSGd(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
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

        return !(b.min.z > max.z || b.max.z < min.z);
    }

    /**
     * @return the min x,y,z values
     */
    public VectorCSGd getMin() {
        return min;
    }

    /**
     * @return the max x,y,z values
     */
    public VectorCSGd getMax() {
        return max;
    }

    public void union(Bounds other) {
        VectorCSGd oMax = other.max;
        VectorCSGd oMin = other.min;
        if (oMin.x > max.x) max.x = oMin.x;
        if (oMin.y > max.y) max.y = oMin.y;
        if (oMin.z > max.z) max.z = oMin.z;
        if (oMin.x < min.x) min.x = oMin.x;
        if (oMin.y < min.y) min.y = oMin.y;
        if (oMin.z < min.z) min.z = oMin.z;
        if (oMax.x > max.x) max.x = oMax.x;
        if (oMax.y > max.y) max.y = oMax.y;
        if (oMax.z > max.z) max.z = oMax.z;
        if (oMax.x < min.x) min.x = oMax.x;
        if (oMax.y < min.y) min.y = oMax.y;
        if (oMax.z < min.z) min.z = oMax.z;
    }

    @Override
    public String toString() {
        return min.toString() + "->" + max.toString(); //$NON-NLS-1$
    }
}
