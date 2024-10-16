/*
 * Copyright 2017 Gary W. Lucas., modified by Nils Schmidt (removed not required methods)
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
package org.tinfour.constrained.delaunay;

/**
 * Defines constants for use in QuadEdge related operations.
 */
public final class QuadEdgeConstants {

    private QuadEdgeConstants() {
        // a private constructor to deter applications from
        // constructing instances of this class.
    }

    /**
     * The maximum value of a constraint index based on the three bytes allocated
     * for its storage. This would be a value of 16777215, or 2^24-1 but we reserve
     * the 3 values at the top for special use. In practice this value is larger
     * than the available memory on many contemporary computers would allow.
     */
    public static final int CONSTRAINT_INDEX_MAX = (1 << 24) - 4; // 16777215

    /**
     * A mask that can be anded with the QuadEdgePartner's index field to extract
     * the constraint index, equivalent to the 24 low-order bits.
     */
    public static final int CONSTRAINT_INDEX_MASK = 0x00ffffff;

    /**
     * A bit indicating that an edge is constrained. This bit just happens to be the
     * sign bit, a feature that is exploited by the isConstrained() method.
     */
    public static final int CONSTRAINT_EDGE_FLAG = 1 << 31;

    /**
     * A bit indicating that the edge is the border of a constrained region
     */
    public static final int CONSTRAINT_REGION_BORDER_FLAG = 1 << 30;

    /**
     * A bit indicating that an edge is in the interior of a constrained region.
     */
    public static final int CONSTRAINT_REGION_INTERIOR_FLAG = 1 << 29;

    /**
     * A bit indicating that an edge is part of a non-region constraint line. Edges
     * are allowed to be both an interior and a line, so a separate flag bit is
     * required for both cases.
     */
    public static final int CONSTRAINT_LINE_MEMBER_FLAG = 1 << 28;
}
