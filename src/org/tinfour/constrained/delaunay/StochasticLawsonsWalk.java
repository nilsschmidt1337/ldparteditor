/*
 * Copyright 2013 Gary W. Lucas., modified by Nils Schmidt (removed not required methods)
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
 * Notes: One of the key features of the XORSHIFT psuedo-random function
 * is that every bit in the value passes conventional tests for
 * randomness.
 */
package org.tinfour.constrained.delaunay;

/**
 * Methods and definitions to perform a stochastic Lawson's walk. The walk uses
 * randomization to prevent getting trapped in infinite loops in the case of
 * imperfections in Delaunay triangulations.
 * <p>
 * A good review of walk algorithms for point location is provided by
 * <cite>Soukal, R.; Ma&#769;lkova&#769;, Kolingerova&#769; (2012) "Walking
 * algorithms for point location in TIN models", Computational Geoscience
 * 16:853-869</cite>.
 * <p>
 * The randomization in this class uses a custom implementation of the XORShift
 * Random Number Generator which was discovered by George Marsaglia in 2003.
 * This method is faster than the java.util.Random class. While it is slower
 * than Java's ThreadLocalRandom, that Java class has the disadvantage that
 * there is no way to set the seed value for the random sequence. For debugging
 * and development purposes, the Tinfour team requires that the behavior of the
 * code be reproduced every time it is run. The sequence produced by
 * ThreadLocalRandom is always different. The exact form used here is taken from
 * http://www.javamex.com/tutorials/random_numbers/xorshift.shtml#.WEC2g7IrJQL
 * The technique used here were studied extensively by Sebastiano Vigna see
 * <cite>An experimental exploration of Marsaglia's xorshift generators,
 * scrambled</cite> at http://vigna.di.unimi.it/ftp/papers/xorshift.pdf
 */
class StochasticLawsonsWalk {

    /**
     * The positive threshold used to determine if a higher-precision calculation is
     * required for performing calculations related to the half-plane calculation.
     * When a computed value is sufficiently close to zero, there is a concern that
     * numerical issues involved in the half-plane calculations might result in
     * incorrect determinations. This value helps define "sufficiently close".
     */
    private final double halfPlaneThreshold;
    /**
     * The negative of the halfPlaneThreshold.
     */
    private final double halfPlaneThresholdNeg;

    /**
     * A set of geometric utilities used for various computations.
     */
    private final GeometricOperations geoOp;

    /**
     * A randomization seed used to select which side of a triangle is tested first
     * for potential transfer during a walk. Seeded with 1 during a reset operation.
     */
    private long seed = 1L;

    /**
     * Constructs an instance using specified thresholds.
     *
     * @param thresholds a valid thresholds object
     */
    StochasticLawsonsWalk(final Thresholds thresholds) {
        geoOp = new GeometricOperations(thresholds);
        halfPlaneThreshold = thresholds.getHalfPlaneThreshold();
        halfPlaneThresholdNeg = -thresholds.getHalfPlaneThreshold();
    }

    /**
     * Search the mesh beginning at the specified edge position to find the triangle
     * that contains the specified coordinates.
     * <p>
     * The algorithm requires the initial edge to be an interior-side edge of the
     * TIN. If necessary, the initial edge is replaced with its dual in order to
     * ensure that this condition is met before beginning the walk.
     *
     * @param startingEdge the edge giving both the starting point of the search and
     *                     storing the ultimate destination.
     * @param x            the x coordinate of interest
     * @param y            the y coordinate of interest
     * @return an edge (not necessarily the closest one) of a triangle that contains
     *         the specified coordinates, or the nearest exterior-side edge if the
     *         point lies outside the convex hull of the TIN.
     */
    QuadEdge findAnEdgeFromEnclosingTriangle(final QuadEdge startingEdge, final double x, final double y) {
        Pnt v0;
        Pnt v1;
        Pnt v2;
        double vX0;
        double vY0;
        double vX1;
        double vY1;
        double vX2;
        double vY2;
        double pX0;
        double pY0;
        double pX1;
        double pY1;
        double pX2;
        double pY2; // the perpendicular vector to a side
        double h0;
        double h1;
        double h2;
        QuadEdge edge = startingEdge;

        if (edge.getForward().getB() == null) {
            // it's an exterior-side edge, use its dual.
            edge = edge.getDual();
        }

        v0 = edge.getA();
        v1 = edge.getB();

        vX0 = x - v0.x;
        vY0 = y - v0.y;
        pX0 = v0.y - v1.y; // the perpendicular
        pY0 = v1.x - v0.x;

        h0 = vX0 * pX0 + vY0 * pY0;

        if (h0 < this.halfPlaneThresholdNeg) {
            // transfer to opposite triangle. The opposite triangle will
            // never be null, though it can be a ghost
            edge = edge.getDual();
            v0 = edge.getA();
        } else if (h0 < this.halfPlaneThreshold) {
            // coordinate is close to the ray on which segment t.a, t.getB() lies
            h0 = geoOp.halfPlane(v0.x, v0.y, v1.x, v1.y, x, y);
            if (h0 < 0) {
                edge = edge.getDual();
                v0 = edge.getA();
            }
        }

        while (true) {
            // if the search reaches a ghost, the target coordinates
            // are exterior to the TIN. transition to the perimeter-edge search.
            // Vertex 2 is the vertex opposite the current edge, treating
            // the current edge as an interior-oriented edge of a triangle.
            // It is important to avoid any of the testing below because
            // vertex 2 of a ghost is null and cannot be accessed.
            v1 = edge.getB();
            v2 = edge.getForward().getB();
            if (v2 == null) {
                // edge is in exterior of the TIN
                return findAssociatedPerimeterEdge(edge, x, y);
            }

            // having tested that the vertex is on the included half-plane
            // defined by the triangle's initial segment, test the other two.
            // Lawson showed that when the TIN is not an optimum
            // Delauny Triangulation the walk could fall into an infinite loop.
            // The random operation prevents that (thus the "stochastic" in the name)
            // One of the key features of the XORSHIFT psuedo-random function
            // is that every bit in the value passes conventional tests for
            // randomness. Thus the code below determines the branch based on
            // the low-order bit value.
            long edgeSelectionForNextTest = randomNext();
            if ((edgeSelectionForNextTest & 1) == 0) {
                vX1 = x - v1.x;
                vY1 = y - v1.y;
                pX1 = v1.y - v2.y; // the perpendicular, use -y for x
                pY1 = v2.x - v1.x;
                h1 = vX1 * pX1 + vY1 * pY1;
                if (h1 < halfPlaneThresholdNeg) {
                    edge = edge.getForward().getDual();
                    v0 = edge.getA(); // should also be v1
                    continue;
                } else if (h1 < halfPlaneThreshold) {
                    h1 = geoOp.halfPlane(v1.x, v1.y, v2.x, v2.y, x, y);
                    if (h1 < 0) {
                        edge = edge.getForward().getDual();
                        v0 = edge.getA();
                        continue;
                    }
                }

                vX2 = x - v2.x;
                vY2 = y - v2.y;
                pX2 = v2.y - v0.y; // the perpendicular, use -y for x
                pY2 = v0.x - v2.x;
                h2 = vX2 * pX2 + vY2 * pY2;
                if (h2 < halfPlaneThresholdNeg) {
                    edge = edge.getReverse().getDual();
                    v0 = edge.getA();
                    continue;
                } else if (h2 < halfPlaneThreshold) {
                    h2 = geoOp.halfPlane(v2.x, v2.y, v0.x, v0.y, x, y);
                    if (h2 < 0) {
                        edge = edge.getReverse().getDual();
                        v0 = edge.getA();
                        continue;
                    }
                }
            } else {
                vX2 = x - v2.x;
                vY2 = y - v2.y;
                pX2 = v2.y - v0.y; // the perpendicular, use -y for x
                pY2 = v0.x - v2.x;
                h2 = vX2 * pX2 + vY2 * pY2;
                if (h2 < halfPlaneThresholdNeg) {
                    edge = edge.getReverse().getDual();
                    v0 = edge.getA();
                    continue;
                } else if (h2 < halfPlaneThreshold) {
                    h2 = geoOp.halfPlane(v2.x, v2.y, v0.x, v0.y, x, y);
                    if (h2 < 0) {
                        edge = edge.getReverse().getDual();
                        v0 = edge.getA();
                        continue;
                    }
                }

                vX1 = x - v1.x;
                vY1 = y - v1.y;
                pX1 = v1.y - v2.y; // the perpendicular
                pY1 = v2.x - v1.x;
                h1 = vX1 * pX1 + vY1 * pY1;
                if (h1 < halfPlaneThresholdNeg) {
                    edge = edge.getForward().getDual();
                    v0 = edge.getA();
                    continue;
                } else if (h1 < halfPlaneThreshold) {
                    h1 = geoOp.halfPlane(v1.x, v1.y, v2.x, v2.y, x, y);
                    if (h1 < 0) {
                        edge = edge.getForward().getDual();
                        v0 = edge.getA();
                        continue;
                    }
                }
            }

            // there was no transfer, the vertex is in the triangle
            // defined by the current edge
            return edge;

        }

    }

    /**
     * Used to find the associated perimeter edge of the TIN when the search
     * determines that the search coordinates lie to the exterior of the mesh.
     *
     * @param startingEdge the starting edge for the perimeter search
     * @param x            the x coordinate of interest
     * @param y            the y coordinate of interest
     * @return the exterior-side edge that subtends the search coordinates
     */
    private QuadEdge findAssociatedPerimeterEdge(final QuadEdge startingEdge, final double x, final double y) {

        QuadEdge edge = startingEdge;
        // This method is called when the first phase of the walk algorithm
        // traversed into a ghost triangle and it is known that the
        // point given by coordinates (x,y) is outside the existing convex hull
        // of the TIN. However, the search may not have reached the most
        // optimal position, especially if it is being used to construct a TIN.
        // The walk needs to move left or right along the edge of the
        // hull to find the edge segment that most closely subtends the vertex.
        // Ideally, if one were to drop a perpendicular from the target
        // onto the line on which the segment lies, the resulting point will
        // be between the endpoints of the segment.
        // During the loops below, the calculation computes the projection
        // of coordinate point in the direction of the edge to determine
        // if the point lies behind or in front of the initial vertex in
        // the edge. It then moves forward or backward depending on the
        // result. As part of the search, the algorithm computes the
        // h parameter below indicates the side on which the point lies with
        // relation to the half plane defined by the triangle edge. If h<0,
        // convex hull (perimeter) has turned away from the target and the
        // walk can be terminated.
        // A special cases exists when h=0. The quantity h/2 happens to also
        // represent the area of a triangle that would be constructed from the
        // point and the edge (taken in order). So if h is zero, the result would
        // be a zero-sized triangle. To avoid that, the perimeter search
        // algorithm will not move to an adjacent edge if the result involves
        // an h=0 case.
        //
        // IMPORTANT: This method must only be called when the point is
        // to the exterior of the Convex Hull. If it is lying on the perimeter,
        // there are conditions under which it will fail. It assumes that the
        // findAnEdgeFromEnclosingTriangle method will have ensured that if the
        // point lies on the perimeter, the it will return the interior edge on
        // which it lies and that this method is never called.

        QuadEdge nEdge;
        Pnt v0 = edge.getA();
        Pnt v1 = edge.getB();
        double vX0 = x - v0.x;
        double vY0 = y - v0.y;
        double tX = v1.x - v0.x;
        double tY = v1.y - v0.y;
        double tC = tX * vX0 + tY * vY0;
        double pX;
        double pY;
        double h;

        if (halfPlaneThresholdNeg < tC && tC < halfPlaneThreshold) {
            tC = geoOp.direction(v0.x, v0.y, v1.x, v1.y, x, y);
        }
        if (tC < 0) {
            // the vertex is retrograde to the starting ghost
            // transfer backward. Technically, this is in the
            // same direction as the perimeter. But since
            // we are navigating the exterior region, it is
            // effectively a "retrograde" movement.
            while (true) {
                nEdge = edge.getReverse().getReverseFromDual();

                v0 = nEdge.getA();
                v1 = nEdge.getB();
                vX0 = x - v0.x;
                vY0 = y - v0.y;
                tX = v1.x - v0.x;
                tY = v1.y - v0.y;
                pX = -tY;
                pY = tX;
                h = pX * vX0 + pY * vY0;
                // test for h < 0
                if (h < halfPlaneThresholdNeg) {
                    break;
                } else if (h < halfPlaneThreshold) {
                    h = geoOp.halfPlane(v0.x, v0.y, v1.x, v1.y, x, y);
                    if (h <= 0) {
                        break;
                    }
                }

                edge = nEdge;

                tC = tX * vX0 + tY * vY0;
                if (tC > halfPlaneThreshold) {
                    break;
                } else if (tC > halfPlaneThresholdNeg) {
                    tC = geoOp.direction(v0.x, v0.y, v1.x, v1.y, x, y);
                    if (tC >= 0) {
                        break;
                    }
                }
            }
        } else {
            // tC should be greater than zero
            // the vertex is positioned in a positive direction
            // relative to the exterior-side edge.
            while (true) {
                nEdge = edge.getForward().getForwardFromDual();
                v0 = nEdge.getA();
                v1 = nEdge.getB();
                vX0 = x - v0.x;
                vY0 = y - v0.y;
                tX = v1.x - v0.x;
                tY = v1.y - v0.y;
                pX = -tY;
                pY = tX;
                h = pX * vX0 + pY * vY0;
                // test for h<0
                if (h < halfPlaneThresholdNeg) {
                    break;
                } else if (h < halfPlaneThreshold) {
                    h = geoOp.halfPlane(v0.x, v0.y, v1.x, v1.y, x, y);
                    if (h <= 0) {
                        break;
                    }
                }
                tC = tX * vX0 + tY * vY0;
                if (tC < halfPlaneThresholdNeg) {
                    break;
                } else if (tC < halfPlaneThreshold) {
                    tC = geoOp.direction(v0.x, v0.y, v1.x, v1.y, x, y);
                    if (tC <= 0) {
                        break;
                    }
                }
                edge = nEdge;
            }
        }

        return edge;
    }

    private long randomNext() {
        seed ^= seed << 21;
        seed ^= seed >>> 35;
        seed ^= seed << 4;
        return seed;
    }
}
