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
 *  The memory in this container is organized into pages, each page
 * holding a fixed number of Edges.   Some of the Edges are
 * committed to the TIN, others are in an "available state".  The pages
 * include links so that the container can maintain a single-direction linked
 * list of pages which have at least one QuadEdge in the "available" state.
 *
 * By design, the class guarantees that ALWAYS at least one page in an available
 * state.  This guarantee allows us to shave one conditional operation each
 * time a QuadEdge is inserted:
 *
 *    With guarantee:
 *       1) Add a QuadEdge to the page.
 *       2) Check to see if the page is full, if so add a new page
 *
 *    Without guarantee
 *       1) Check to see if there's an available page, if not add one
 *       2) Add QuadEdge to page
 *       3) Check to see if the page is full, if so add a new page
 *
 * The design of the class is based on the idea that Edges are added
 * and removed at random, but the number of Edges grows as the data
 * is processed. If this growth assumption is unfounded, then this class
 * would tend to end up with a lot of partially-populated pages
 *
 * The QuadEdge index
 *  The idea here is that the index element of a QuadEdge allows
 * the class to compute what page it belongs to.  So when a QuadEdge is
 * freed, it can modify the appropriate page.  However, there is a complication
 * in that we want the base reference for an edge and its dual to have
 * unique indices (for consistency with the SemiVirtualEdge classes).  So
 * the index for a distinct edge is multiplied by 2.  Thus, when trying to
 * relate an edge to a page, the page is identified by dividing the index
 * by 2.
 */
package org.tinfour.constrained.delaunay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Provides an object-pool implementation that the manages the allocation,
 * deletion, and reuse of Edges.
 * <p>
 * This class is written using a very old-school approach as a way of minimizing
 * the frequency with which objects are garbage collected. Edges are extensively
 * allocated and freed as the TIN is built. Were they simply constructed and put
 * out-of-scope, the resulting garbage collection could degrade performance.
 * <p>
 * Note that this class is <strong>not thread safe</strong>.
 * <p>
 * For performance reasons, many of the methods in this class make the
 * assumption that any edges passed into the method are under the management of
 * the current instance. If this assumption is violated, serious errors could
 * occur. For example, if an application uses one edge pool to allocate an edge
 * and then passes it to the deallocEdge method another edge pool instance, both
 * instances could become seriously corrupted.
 */
class EdgePool {

    /**
     * The number of edges in an edge-pool page.
     */
    private static final int EDGE_POOL_PAGE_SIZE = 1024;

    /**
     * The number of Edges stored in a page
     */
    private final int pageSize;

    /**
     * The number of edge indices for a page, a value equal to pageSize*2;
     */
    private final int pageSize2;

    private Page[] pages;
    /**
     * The next page that includes available Edges. This reference is never null.
     * There is always at least one page with at least one free QuadEdge in it.
     */
    private Page nextAvailablePage;
    private int nAllocated;

    /**
     * The constraint maps provide a way of tying a constraint object reference to
     * the edges that are associated with it. Separate maps are maintained for the
     * borders of region constraints (borders) and linear constraints. This indirect
     * method is used to economize on memory use by edges. Although it would be
     * possible to add constraint references to the edge structure, doing so would
     * increase the edge memory use by an unacceptably large degree.
     */
    private HashMap<Integer, PolygonConstraint> borderConstraintMap = new HashMap<>();
    private HashMap<Integer, PolygonConstraint> linearConstraintMap = new HashMap<>();

    /**
     * Construct a QuadEdge manager allocating a small number of initial edges.
     *
     */
    public EdgePool() {
        this.pageSize = EDGE_POOL_PAGE_SIZE;
        this.pageSize2 = EDGE_POOL_PAGE_SIZE * 2;
        pages = new Page[1];
        pages[0] = new Page(0);
        nextAvailablePage = pages[0];
        nextAvailablePage.initializeEdges();
    }

    private void allocatePage() {
        int oldLength = pages.length;
        Page[] newPages = new Page[oldLength + 1];
        System.arraycopy(pages, 0, newPages, 0, pages.length);
        newPages[oldLength] = new Page(oldLength);
        newPages[oldLength].initializeEdges();
        pages = newPages;
        nextAvailablePage = pages[oldLength];
        for (int i = 0; i < pages.length - 1; i++) {
            pages[i].nextPage = pages[i + 1];
        }
    }

    QuadEdge allocateEdge(Pnt a, Pnt b) {
        Page page = nextAvailablePage;
        QuadEdge e = page.allocateEdge();
        if (page.isFullyAllocated()) {
            nextAvailablePage = page.nextPage;
            if (nextAvailablePage == null) {
                allocatePage();
            }
        }
        nAllocated++;

        e.setVertices(a, b);
        return e;
    }

    /**
     * Deallocates the QuadEdge returning it to the QuadEdge pool.
     *
     * @param e a valid QuadEdge
     */
    public void deallocateEdge(QuadEdge e) {
        // Note: Although there is a sanity check method that can
        // be used to verify that the input edge belongs to this
        // edge pool, it is not used here for performance purposes.
        int iPage = e.getIndex() / pageSize2;
        Page page = pages[iPage];
        if (page.isFullyAllocated()) {
            // since it will no longer be fully allocated,
            // add it to the linked list
            page.nextPage = nextAvailablePage;
            nextAvailablePage = page;
        }
        page.deallocateEdge(e);
        nAllocated--;
    }

    

    /**
     * Get first valid, non-ghost QuadEdge in collection
     *
     * @return for a non-empty collection, a valid QuadEdge; otherwise a null
     */
    public QuadEdge getStartingEdge() {
        for (Page p : pages) {
            if (p.nAllocated > 0) {
                for (int i = 0; i < p.nAllocated; i++) {
                    if (p.edges[i].getB() != null && p.edges[i].getA() != null) {
                        return p.edges[i];
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get a list of the Edges currently stored in the collection
     *
     * @return a valid, potentially empty list of edges
     */
    public List<QuadEdge> getEdges() {
        ArrayList<QuadEdge> eList = new ArrayList<>(nAllocated);
        for (Page p : pages) {
            for (int j = 0; j < p.nAllocated; j++) {
                eList.add(p.edges[j]);
            }
        }
        return eList;
    }

    /**
     * Constructs an iterator that will optionally skip ghost edges.
     * 
     * @return a valid instance of an iterator
     */
    Iterator<QuadEdge> getIterator() {
        return new Iterator<QuadEdge>() {
            QuadEdge currentEdge;
            int nextPage;
            int nextEdge;
            boolean hasNext = findNextEdge(0, -1);

            private boolean findNextEdge(int iPage, int iEdge) {
                nextPage = iPage;
                nextEdge = iEdge;
                while (nextPage < pages.length) {
                    nextEdge++;
                    if (nextEdge < pages[nextPage].nAllocated) {
                        QuadEdge e = pages[nextPage].edges[nextEdge];
                        if (e.getA() == null || e.getB() == null) {
                            continue;
                        }
                        
                        return true;
                    } else {
                        nextEdge = -1;
                        nextPage++;
                    }
                }
                return false;
            }

            @Override
            public boolean hasNext() {
                return hasNext;
            }

            /**
             * Overrides the default remove operation with an implementation that throws an
             * UnsupportedOperationException. Tinfour requires a specific set of
             * relationships between edges, and removing an edge from an iterator would
             * damage the overall structure and result in faulty behavior. Therefore,
             * Tinfour iterators do not support remove operations.
             */
            @Override
            public void remove() {
                throw new UnsupportedOperationException("Remove operation not supported by this iterator"); //$NON-NLS-1$
            }

            @Override
            public QuadEdge next() {
                currentEdge = null;
                if (hasNext) {
                    currentEdge = pages[nextPage].edges[nextEdge];
                    hasNext = findNextEdge(nextPage, nextEdge);
                } else {
                    throw new NoSuchElementException();
                }
                return currentEdge;
            }
        };
    }

    /**
     * Gets the maximum value of an edge index that is currently allocated within
     * the edge pool.
     *
     * @return a positive number or zero if the pool is currently unallocated.
     */
    public int getMaximumAllocationIndex() {
        for (int iPage = pages.length - 1; iPage >= 0; iPage--) {
            Page p = pages[iPage];
            if (p.nAllocated > 0) {
                return p.pageID * pageSize2 + p.nAllocated * 2;
            }
        }
        return 0;
    }

    /**
     * Split the edge e into two by inserting a new vertex m into the edge. The
     * insertion point does not necessarily have to lie on the segment. This method
     * splits the segment into two segments so that edge e(a,b) becomes edges p(a,m)
     * and and e(m,b), with forward and reverse links for both segments being
     * adjusted accordingly. The new segment p(a,m) is returned and the input
     * segment e is adjusted with new vertices (m,b).
     * <p>
     * The split edge method preserves constraint flags and other attributes
     * associated with the edge.
     * 
     * @param e the input segment
     * @param m the insertion vertex
     * @return a valid instance of a QuadEdge or QuadEdgePartner (depending on the
     *         class of the input)
     */
    QuadEdge splitEdge(QuadEdge e, Pnt m) {
        QuadEdge b = e.getBaseReference();
        QuadEdge d = e.getDual();

        QuadEdge eR = e.getReverse();
        QuadEdge dF = d.getForward();

        Pnt a = e.getA();

        e.setA(m);
        QuadEdge p = this.allocateEdge(a, m);
        QuadEdge q = p.getDual();

        p.setForward(e);
        p.setReverse(eR);
        q.setForward(dF);
        q.setReverse(d);

        // copy the constraint flags, if any
        p.dual.index = b.dual.index;

        // p is on the same side of the original edge e and
        // q is on the same side as the dual edge d.
        if (e.isConstrainedRegionBorder()) {
            PolygonConstraint c = borderConstraintMap.get(e.getIndex());
            if (c != null) {
                this.addBorderConstraintToMap(p, c);
            }
            c = borderConstraintMap.get(d.getIndex());
            if (c != null) {
                addBorderConstraintToMap(q, c);
            }
        } else if (e.isConstraintLineMember()) {
            PolygonConstraint c = linearConstraintMap.get(e.getIndex());
            if (c != null) {
                addLinearConstraintToMap(p, c);
            }
        }

        return p;

    }

    /**
     * Adds the specified constraint to the border constraint map, thus recording
     * which region constraint lies to the left side of the edge (e.g. which region
     * is bordered by the specified edge).
     * 
     * @param edge       a valid edge instance
     * @param constraint a valid constraint instance
     */
    void addBorderConstraintToMap(QuadEdge edge, PolygonConstraint constraint) {
        borderConstraintMap.put(edge.getIndex(), constraint);
    }

    /**
     * Adds the specified constraint to the linear constraint map, thus recording
     * which constraint lies to the left side of the edge.
     * 
     * @param edge       a valid edge instance
     * @param constraint a valid constraint instance
     */
    private void addLinearConstraintToMap(QuadEdge edge, PolygonConstraint constraint) {
        int index = edge.getIndex();
        linearConstraintMap.put(index, constraint);
        linearConstraintMap.put(index ^ 1, constraint);
    }

    

    private class Page {
        int pageID;
        int pageOffset;
        int nAllocated;
        QuadEdge[] edges;
        Page nextPage;

        Page(int pageID) {
            this.pageID = pageID;
            pageOffset = pageID * pageSize2;
            edges = new QuadEdge[pageSize];
        }

        /**
         * Sets up the array of free Edges. This method is almost always called when a
         * new page is created. The only time it is not is in the compact() operation
         * where Edges will be shifted around.
         */
        void initializeEdges() {
            for (int i = 0; i < pageSize; i++) {
                edges[i] = new QuadEdge(pageOffset + i * 2);
            }
        }

        QuadEdge allocateEdge() {
            QuadEdge e = edges[nAllocated];
            e.setIndex(pageID * pageSize2 + nAllocated * 2);
            nAllocated++;
            return e;
        }

        /**
         * Free the QuadEdge for reuse, setting any external references to null, but not
         * damaging any arrays or management structures.
         * <p>
         * Note that it is important that deallocation set the QuadEdge back to its
         * initialization states. To conserve processing the allocation routine assumes
         * that any unused QuadEdge in the collection is already in its initialized
         * state and so doesn't do any extra work.
         *
         * @param e a valid QuadEdge
         */
        void deallocateEdge(QuadEdge be) {
            // reset to initialization state as necessary.
            // in this following block, we clear all flags that matter.
            // We also set any references to null to prevent
            // object retention and expedite garbage collection.
            // Note that the variable arrayIndex is NOT the edge index,
            // but rather the array index for the edge within the array of edge pairs
            // stored by this class.

            QuadEdge e = be.getBaseReference();
            int arrayIndex = (e.getIndex() - pageOffset) / 2;
            e.clear();

            // The array of Edges must be kept
            // so that all allocated Edges are together at the beginning
            // of the array and all the free Edges are together at
            // the end of the array. If the removal
            // left a "hole" in the section of the array dedicated to allocated
            // Edges, shift Edges around, reassigning the managementID
            // of the QuadEdge that was shifted into the hole.
            nAllocated--;
            // nAllocated is now the index of the last allocated QuadEdge
            // in the array. We can modify the allocationID of that
            // QuadEdge and its position in the array because the
            // EdgeManager class is the only one that manipulates these
            // values.

            if (arrayIndex < nAllocated) {
                QuadEdge swap = edges[nAllocated];
                edges[arrayIndex] = swap;
                int oldIndex = swap.getIndex();
                int newIndex = pageOffset + arrayIndex * 2;
                swap.setIndex(newIndex);
                edges[nAllocated] = e;

                // the swap operation will change the index of the line. And, because
                // the index is used as a key into the constraint maps, we need to
                // adjust the entries. The fact that this action is necessarily
                // highlights one of the disadvantages of the design choice of
                // swapping edges. It was chosen in an effort to save memory
                // (constrast it with the semi-virtual implementation which
                // maintains a free list). But it did have side-effects. The
                // semi-virtual implementation may have the better approach.
                if (swap.isConstraintLineMember() && linearConstraintMap.containsKey(oldIndex)) {
                    PolygonConstraint c = linearConstraintMap.get(oldIndex);
                    linearConstraintMap.remove(oldIndex);
                    linearConstraintMap.remove(oldIndex ^ 1);
                    linearConstraintMap.put(newIndex, c);
                    linearConstraintMap.put(newIndex ^ 1, c);
                }
                if (swap.isConstrainedRegionBorder()) {
                    if (borderConstraintMap.containsKey(oldIndex)) {
                        PolygonConstraint c = borderConstraintMap.get(oldIndex);
                        borderConstraintMap.remove(oldIndex);
                        borderConstraintMap.put(newIndex, c);
                    }
                    oldIndex ^= 1; // set index to dual
                    newIndex ^= 1;
                    if (borderConstraintMap.containsKey(oldIndex)) {
                        PolygonConstraint c = borderConstraintMap.get(oldIndex);
                        borderConstraintMap.remove(oldIndex);
                        borderConstraintMap.put(newIndex, c);
                    }
                }

                e.setIndex(pageOffset + nAllocated * 2); // pro forma, for safety
            }
        }

        boolean isFullyAllocated() {
            return nAllocated == edges.length;
        }
    }

    public Iterator<QuadEdge> iterator() {
        return getIterator(true);
    }

    /**
     * Constructs an iterator that will optionally skip ghost edges.
     * 
     * @param includeGhostEdges indicates that ghost edges are to be included in the
     *                          iterator production.
     * @return a valid instance of an iterator
     */
    public Iterator<QuadEdge> getIterator(final boolean includeGhostEdges) {
        return new Iterator<QuadEdge>() {
            QuadEdge currentEdge;
            int nextPage;
            int nextEdge;
            boolean skipGhosts = !includeGhostEdges;
            boolean hasNext = findNextEdge(0, -1);

            private boolean findNextEdge(int iPage, int iEdge) {
                nextPage = iPage;
                nextEdge = iEdge;
                while (nextPage < pages.length) {
                    nextEdge++;
                    if (nextEdge < pages[nextPage].nAllocated) {
                        if (skipGhosts) {
                            QuadEdge e = pages[nextPage].edges[nextEdge];
                            if (e.getA() == null || e.getB() == null) {
                                continue;
                            }
                        }
                        return true;
                    } else {
                        nextEdge = -1;
                        nextPage++;
                    }
                }
                return false;
            }

            @Override
            public boolean hasNext() {
                return hasNext;
            }

            /**
             * Overrides the default remove operation with an implementation that throws an
             * UnsupportedOperationException. Tinfour requires a specific set of
             * relationships between edges, and removing an edge from an iterator would
             * damage the overall structure and result in faulty behavior. Therefore,
             * Tinfour iterators do not support remove operations.
             */
            @Override
            public void remove() {
                throw new UnsupportedOperationException("Remove operation not supported by this iterator"); //$NON-NLS-1$
            }

            @Override
            public QuadEdge next() {
                currentEdge = null;
                if (hasNext) {
                    currentEdge = pages[nextPage].edges[nextEdge];
                    hasNext = findNextEdge(nextPage, nextEdge);
                } else {
                    throw new NoSuchElementException();
                }
                return currentEdge;
            }
        };
    }
}
