/* --------------------------------------------------------------------
 * Copyright (C) 2018  Gary W. Lucas., modified by Nils Schmidt (removed not required methods)
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
 * ---------------------------------------------------------------------
 */
package org.tinfour.constrained.delaunay;

/**
 * Provides methods and elements for a simple representation of a triangle based
 * on QuadEdge edges.
 */
public class SimpleTriangle {

    private final QuadEdge edgeA;
    private final QuadEdge edgeB;
    private final QuadEdge edgeC;
    private final int index;

    /**
     * Construct a simple triangle from the specified edges. For efficiency
     * purposes, this constructor is very lean and does not perform sanity checking
     * on the inputs. In particular, it is essential that the specified edge be a
     * member of the specified TIN and that the TIN must not be modified while the
     * SimpleTriangle instance is in use.
     *
     * @param tin a reference to the TIN that was used to create this triangle.
     * @param a   a valid edge
     * @param b   a valid edge
     * @param c   a valid edge
     */
    public SimpleTriangle(IncrementalTin tin, QuadEdge a, QuadEdge b, QuadEdge c) {
        this.edgeA = a;
        this.edgeB = b;
        this.edgeC = c;
        index = computeIndex();
    }

    /**
     * Construct a simple triangle from the specified edges. For efficiency
     * purposes, this constructor is very lean and does not perform sanity checking
     * on the inputs. In particular, it is essential that the specified edge be a
     * member of the specified TIN and that the TIN must not be modified while the
     * SimpleTriangle instance is in use.
     *
     *
     * @param tin a reference to the TIN that was used to create this triangle.
     * @param a   a valid edge which must be a member of the specified TIN.
     */
    public SimpleTriangle(IncrementalTin tin, QuadEdge a) {
        this.edgeA = a;
        this.edgeB = a.getForward();
        this.edgeC = a.getReverse();
        index = computeIndex();
    }

    private int computeIndex() {
        int aIndex = edgeA.getIndex();
        int bIndex = edgeB.getIndex();
        int cIndex = edgeC.getIndex();
        if (aIndex <= bIndex) {
            return aIndex < cIndex ? aIndex : cIndex;
        } else {
            return bIndex < cIndex ? bIndex : cIndex;
        }
    }

    /**
     * Get edge a from the triangle
     *
     * @return a valid edge
     */
    public QuadEdge getEdgeA() {
        return edgeA;
    }

    /**
     * Get edge b from the triangle
     *
     * @return a valid edge
     */
    public QuadEdge getEdgeB() {
        return edgeB;
    }

    /**
     * Get edge c from the triangle
     *
     * @return a valid edge
     */
    public QuadEdge getEdgeC() {
        return edgeC;
    }

    /**
     * Gets vertex A of the triangle. The method names used in this class follow the
     * conventions of trigonometry. Vertices are labeled so that vertex A is
     * opposite edge a, vertex B is opposite edge b, etc. This approach is slightly
     * different than that used in other parts of the Tinfour API.
     *
     * @return a valid vertex
     */
    public Pnt getVertexA() {
        return edgeC.getA();
    }

    /**
     * Gets vertex B of the triangle. The method names used in this class follow the
     * conventions of trigonometry. Vertices are labeled so that vertex A is
     * opposite edge a, vertex B is opposite edge b, etc. This approach is slightly
     * different than that used in other parts of the Tinfour API.
     *
     * @return a valid vertex
     */
    public Pnt getVertexB() {
        return edgeA.getA();
    }

    /**
     * Gets vertex A of the triangle. The method names used in this class follow the
     * conventions of trigonometry. Vertices are labeled so that vertex A is
     * opposite edge a, vertex B is opposite edge b, etc. This approach is slightly
     * different than that used in other parts of the Tinfour API.
     *
     * @return a valid vertex
     */
    public Pnt getVertexC() {
        return edgeB.getA();
    }

    /**
     * Gets a unique index value associated with the triangle.
     * <p>
     * The index value for the triangle is taken from the lowest-value index of the
     * three edges that comprise the triangle. It will be stable provided that the
     * underlying Triangulated Irregular Network (TIN) is not modified.
     * 
     * @return an arbitrary integer value.
     */
    public int getIndex() {
        return index;
    }
}
