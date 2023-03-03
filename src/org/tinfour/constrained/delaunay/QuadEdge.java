/*
 * Copyright 2015 Gary W. Lucas., modified by Nils Schmidt (removed not required methods)
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
* The layout of this class is intended to accomplish the following:
*  a) conserve memory in applications with a very large number of edges
*  b) support the use of an edge pool by providing an application ID field.
*
* The memory layout is based on the following ideas
*  a) In most JVM's,  the size in memory of an object must be a multiple
*     of eight.  So an object containing one byte would still have to
*     be eight bytes large in memory.
*  b) In addition to the memory required by the object itself, all objects
*     require a certain amount of memory to control the allocation of
*     objects on the heap. This value is out-of-the-control of any
*     code and is JVM-implementation-dependent.  Eight bytes appear to be
*     the minimum.
*  c) integers are 4 bytes. doubles 8. floats 4.
*  d) although it is implementation dependent, many Java JVM's can represent
*     object references in 4 bytes provided the memory size of the JVM is
*     less than 32 gigabytes.  Although the normal range of a 4 byte memory
*     address would be 4 gigabytes, some of the major JVM's (Hotspot for
*     Windows) take advantage of the fact that objects are aligned in memory
*     on 8-byte boundaries to "compress"  references by dividing them by 8
*     when storing them in an object and multiplying them by eight when
*     accessing them.
*       so assume references are 4 bytes.
*  e) all objects include a reference to their own class definition.
*
*  So this class contains the following fundamental member elements
*  all of which are 4 bytes in JVMs where references are compressed:
*
*      reference to the class        (4)
*      reference to the dual         (4)
*      reference to the vertex       (4)
*      reference to the forward edge (4)
*      reference to the reverse edge (4)
*         subtotal                    20
*
*  Since the size of the class must be a multiple of 8, that leaves 4
*  bytes which will be allocated in memory no matter what.  So we
*  put that to use by defining an integer application data element
*
*       integer index                (4)
*         subtotal                    24
*
*  Flags and book-keeping elements used by Java for memory management
*  In the Windows/Hotspot JVM where I've inspected this using the Unsafe API,
*  I observed 8 bytes of memory use.
*
*      flags                        (8)
*         total                      32
*
*
* The index element of this class is used to assign a unique integer value
* to each edge created in the TIN-building process or other applications.
* In the case of the EdgePool, it is used to manage allocation of edges.
* If instances are not managed by an EdgePool, the index value is free
* for use in other interpretations.
*
* CONSTRAINTS
*   In QuadEdgePartner, the index value is used as a way of indicating
* whether the edge is a constrained edge according to the definition
* of a Constrained Delaunay Triangulation.   To conserve memory and
* keep the size of the class small, the low order two bytes are
* allocated to indicating the "constraint index". The use of a constraint
* index is also intended to support operations in which the constraint index
* of an edge can be traced back to the constraint that defined it.
* So, typically, the constraint index is an index back to the list of
* constraints that was added to the incremental-TIN implementation.
* If the lower two bytes of the QuadEdgePartner's index element are clear,
* so that (index&0xffff) == 0, the edge is considered not constrained.  When
* the constraint-index is set using the setConstraint() method, the code adds
* one to the value stored. The getConstraintIndex() method masks out the
* index field and subtracts a value of 1 from the result before returning it.
* The consequence of this design choice is that the maximum value that can
* be stored in the low-order two bytes of the index element is (2^16-1)-1,
* or 65534...
*
* Special considerations for setForward() and setReverse()
*   Even though this class does implement the IQuadEdge interface, the
* setForward() and setReverse() methods do not accept IQuadEdge as an
* interface.  In an earlier version, there was an experiment that used
* IQuadEdge for the forward and reverse member elements, however the overhead
* due to Java type casting resulted in a 20 percent degradation in performance.
*/
package org.tinfour.constrained.delaunay;

import java.util.Objects;

/**
 * A representation of an edge with forward and reverse links on one side and
 * counterpart links attached to its dual (other side).
 * <p>
 * This concept is based on the structure popularized by <cite>Guibas, L. and
 * Stolfi, J. (1985) "Primitives for the manipulation of subdivisions and the
 * computation of Voronoi diagrams" ACM Transactions on Graphics, 4(2), 1985, p.
 * 75-123.</cite>
 */
public class QuadEdge {

    /**
     * An arbitrary index value. For IncrementalTin, the index is used to manage the
     * edge pool.
     */
    int index;

    /**
     * The dual of this edge (always valid, never null.
     */
    QuadEdge dual;
    /**
     * The initial vertex of this edge, the second vertex of the dual.
     */
    Pnt v;
    /**
     * The forward link of this edge.
     */
    QuadEdge f;
    /**
     * The reverse link of this edge.
     */
    QuadEdge r;

    /**
     * Construct the edge setting its dual with the specified reference.
     *
     * @param partner a valid element.
     */
    QuadEdge(final QuadEdge partner) {
        dual = partner;
    }

    /**
     * Construct the edge and its dual assigning the pair the specified index.
     *
     * @param index an arbitrary integer value.
     */
    QuadEdge(final int index) {
        dual = new QuadEdgePartner(this);
        this.index = index;
    }

    /**
     * Sets the vertices for this edge (and its dual).
     *
     * @param a the initial vertex, must be a valid reference.
     * @param b the second vertex, may be a valid reference or a null for a ghost
     *          edge.
     */
    public void setVertices(final Pnt a, final Pnt b) {
        this.v = a;
        this.dual.v = b;
    }

    /**
     * Gets the initial vertex for this edge.
     *
     * @return a valid reference.
     */
    public final Pnt getA() {
        return v;
    }

    /**
     * Sets the initial vertex for this edge.
     *
     * @param a a valid reference.
     */
    public final void setA(final Pnt a) {
        this.v = a;
    }

    /**
     * Gets the second vertex for this edge.
     *
     * @return a valid reference or a null for a ghost edge.
     */
    public final Pnt getB() {
        return dual.v;
    }

    /**
     * Gets the forward reference of the edge.
     *
     * @return a valid reference.
     */
    public final QuadEdge getForward() {
        return f;
    }

    /**
     * Gets the reverse reference of the edge.
     *
     * @return a valid reference.
     */
    public final QuadEdge getReverse() {
        return r;
    }

    /**
     * Gets the forward reference of the dual.
     *
     * @return a valid reference
     */
    public final QuadEdge getForwardFromDual() {
        return dual.f;
    }

    /**
     * Gets the reverse link of the dual.
     *
     * @return a valid reference
     */
    public final QuadEdge getReverseFromDual() {
        return dual.r;
    }

    /**
     * Gets the dual of the reverse link.
     *
     * @return a valid reference
     */
    public final QuadEdge getDualFromReverse() {
        return r.dual;
    }

    /**
     * Sets the forward reference for this edge.
     *
     * @param e a valid reference
     */
    public final void setForward(final QuadEdge e) {
        this.f = e;
        e.r = this;
    }

    /**
     * Sets the reverse reference for this edge.
     *
     * @param e a valid reference
     */
    public final void setReverse(final QuadEdge e) {
        this.r = e;
        e.f = this;
    }

    /**
     * Gets the dual edge to this instance.
     *
     * @return a valid edge.
     */
    public final QuadEdge getDual() {
        return dual;
    }

    /**
     * Gets the index value for this edge.
     *
     * @return an integer value
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets the index value for this edge. Because this index value is used by
     * edge-pool implementations and for other data management activities, the scope
     * of this method is limited to protected. The actual definition of this element
     * is left to the application that uses it.
     *
     * @param index an integer value
     */
    protected void setIndex(final int index) {
        this.index = index;
    }

    /**
     * Gets the reference to the side-zero edge of the pair.
     *
     * @return a link to the side-zero edge of the pair.
     */
    public QuadEdge getBaseReference() {
        return this;
    }

    public void setConstraintIndex(int constraintIndex) {
        dual.setConstraintIndex(constraintIndex);
    }

    /**
     * Gets the index of the constrain associated with
     *
     * @return true if the edge is constrained; otherwise, false.
     */
    public boolean isConstrained() {
        return dual.isConstrained();
    }

    public void setConstrained(int constraintIndex) {
        dual.setConstrained(constraintIndex);
    }

    /**
     * Sets all vertices and link references to null (the link to a dual is not
     * affected).
     */
    public void clear() {
        // note that the index of the partner is set to -1,
        // but the index of the base, which is used for management purposes
        // is left alone.
        this.v = null;
        this.f = null;
        this.r = null;
        dual.v = null;
        dual.f = null;
        dual.r = null;
        dual.index = 0;
    }

    /**
     * An implementation of the equals method which check for a matching reference.
     *
     * @param o a valid reference or a null
     * @return true if the specified reference matches this.
     */
    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.index);
    }

    public boolean isConstrainedRegionInterior() {
        return dual.isConstrainedRegionInterior();
    }

    public boolean isConstrainedRegionBorder() {
        return dual.isConstrainedRegionBorder();
    }

    public boolean isConstraintLineMember() {
        return dual.isConstraintLineMember();
    }

    public void setConstrainedRegionBorderFlag() {
        dual.setConstrainedRegionBorderFlag();
    }

    public void setConstrainedRegionInteriorFlag() {
        dual.setConstrainedRegionInteriorFlag();
    }
}
