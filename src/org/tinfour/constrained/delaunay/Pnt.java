/*
 * Copyright 2014 Gary W. Lucas., modified by Nils Schmidt (removed not required methods)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
* Notes:
*
* The selection of elements in this class was chosen to reduce the amount
* of memory used by the class.  In general, I caution anyone modifying this
* class to be mindful of the effect of new elements on overall size.
* The data type for the x and y coordinates is double while the coordinate
* for z is float. This choice reflects the pedigree of this code and the
* fact that it was originally intended for Geographic Information System (GIS)
* implementations.  In GIS systems, it is common to represent the horizontal
* coordinates of a point in meters.  Since the circumference of the Earth
* is about 40 million meters, it is common to see cases where two
* points might have coordinates that are only be one meter apart,
* but due to the global reference system used to represent them they have
* values close to 10's of millions.  In such a case, the 4-byte float format
* does not provide enough precision to represent the points, so the
* eight-byte Java double must be used.  However, the range of z coordinates
* in GIS systems tends to be much smaller, permitting a the use of Java
* floats as a way of conserving memory space.
*
* Recall that the size of a object instance must be a multiple of 8.
* On a 32 bit JVM and many 64 bit JVM's, the design of this class
* results  in the following layout:
*    Java overhead:                      8 bytes  (JVM dependent)
*    Class reference (used by Java)      4 bytes
*    int index                           4 bytes
*    double x                            8 bytes
*    double y                            8 bytes
*    float  z                            4 bytes
*    byte   status                       1 byte
*    byte   auxiliaryIndex               1 byte
*    padding (reserved by Java)          2 bytes (not committed at this time)
*    --------------------------        ---------
*    Total                              40 bytes
*
*  Because of byte alignment, the one-byte status element does not
*  actually change the overall size of the Vertex objects.
*  And, in fact, there is room to add one or more data elements totaling
*  to 3 bytes or less without increasing the memory use for
*  instances of this class. This use can be accomplished by applications
*  that use Tinfour by creating derived classes from this class.
*  However, testing shows that, to make this approach work, some implementations
*  of Java require that the elements to exploit the padding must be declared
*  in the base class.  So the following elements are declared with
*  protected scope and left for the use of derived classed as required
*  by applications that use Tinfour:
*     reserved0
*     reserved1
*     reserved2
*/
package org.tinfour.constrained.delaunay;

/**
 * Represents a point in a connected network on a planar surface.
 */
public class Pnt {

    /**
     * A bit flag indicating that the vertex is synthetic and was created through
     * some form of mesh processing rather than being supplied as a data sample.
     */
    static final int BIT_SYNTHETIC = 0x01;

    /**
     * A bit flag indicating that the vertex is a member of a constraint edge.
     */
    static final int BIT_CONSTRAINT = 0x02;

    /**
     * The Cartesian coordinate of the vertex (immutable).
     */
    public final double x;
    /**
     * The Cartesian coordinate of the vertex (immutable).
     */
    public final double y;

    /**
     * The bit-mapped status flags for the vertex. The assignment of meaning to the
     * bits for this field are defined by static members of this class.
     */
    protected byte status;

    /**
     * Construct a vertex with the specified coordinates and ID value. If the z
     * value is NaN then the vertex will be treated as a "null data value".
     *
     * @param x     the coordinate on the surface on which the vertex is defined
     * @param y     the coordinate on the surface on which the vertex is defined
     */
    public Pnt(final double x, final double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the square of the distance from the vertex to an arbitrary point.
     *
     * @param x coordinate of arbitrary point
     * @param y coordinate of arbitrary point
     * @return a distance in units squared
     */
    double getDistanceSq(final double x, final double y) {
        double dx = this.x - x;
        double dy = this.y - y;
        return dx * dx + dy * dy;
    }

    /**
     * Gets the distance from the vertex to an arbitrary point.
     *
     * @param x coordinate of arbitrary point
     * @param y coordinate of arbitrary point
     * @return the distance in the applicable coordinate system
     */
    double getDistance(final double x, final double y) {
        double dx = this.x - x;
        double dy = this.y - y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Get the distance to the vertex.
     *
     * @param v a valid vertex
     * @return the distance to the vertex
     */
    double getDistance(final Pnt v) {
        double dx = x - v.x;
        double dy = y - v.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Get the x coordinate associated with the vertex. The x coordinate is
     * immutable and established when the vertex is constructed. it is populated
     * whether the vertex contains a null data value (Z value or I value).
     *
     * @return a valid floating point value.
     */
    public double getX() {
        return x;
    }

    /**
     * Get the y coordinate associated with the vertex. The y coordinate is
     * inmmutable and established when the vertex is constructed. it is populated
     * whether the vertex contains a null data value (Z value or I value).
     *
     * @return a valid floating point value.
     */
    public double getY() {
        return y;
    }

    /**
     * Indicates whether a vertex is synthetic (was created through a Tinfour
     * procedure rather than supplied by an application).
     *
     * @return true if vertex is synthetic; otherwise, false
     */
    public boolean isSynthetic() {
        return (status & BIT_SYNTHETIC) != 0;
    }

    /**
     * Sets or clears the is-synthetic status of a vertex.
     *
     * @param synthetic true if vertex is synthetic; otherwise, false
     */
    public void setSynthetic(boolean synthetic) {
        if (synthetic) {
            status |= BIT_SYNTHETIC;
        } else {
            status &= ~BIT_SYNTHETIC;
        }
    }

    /**
     * Sets or clears the is-constraint-member status of a vertex.
     *
     * @param constraintMember true if vertex is a part of a constraint definition
     *                         or lies on the border of an area constraint;
     *                         otherwise, false
     */
    public void setConstraintMember(boolean constraintMember) {
        if (constraintMember) {
            status |= BIT_CONSTRAINT;
        } else {
            status &= ~BIT_CONSTRAINT;
        }
    }

    /**
     * Sets the status value of the vertex. This method is intended to provide an
     * efficient way of setting multiple status flags at once.
     *
     * @param status a valid status value. Because the status is defined as a single
     *               byte, higher-order bytes will be ignored.
     */
    public void setStatus(int status) {
        this.status = (byte) status;
    }

    /**
     * Indicates whether a vertex is part of a constraint definition or lies on the
     * border of an area constraint.
     *
     * @return true if vertex is a constraint member; otherwise, false
     */
    public boolean isConstraintMember() {
        return (status & BIT_CONSTRAINT) != 0;
    }
}
