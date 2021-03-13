/*
 * Copyright (c) 2005, 2007 by L. Paul Chew.
 *
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.nschmidt.delaunay;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * A 2D Delaunay Triangulation (DT) with incremental site insertion.
 *
 * This is not the fastest way to build a DT, but it's a reasonable way to build
 * a DT incrementally and it makes a nice interactive display. There are several
 * O(n log n) methods, but they require that the sites are all known initially.
 *
 * A Triangulation is a Set of Triangles. A Triangulation is unmodifiable as a
 * Set; the only way to change it is to add sites (via delaunayPlace).
 *
 * @author Paul Chew Modified, optimised and customised by Nils Schmidt
 *
 *         Created July 2005. Derived from an earlier, messier version.
 *
 *         Modified November 2007. Rewrote to use AbstractSet as parent class
 *         and to use the Graph class internally. Tried to make the DT algorithm
 *         clearer by explicitly creating a cavity. Added code needed to find a
 *         Voronoi cell.
 *
 */
public class Triangulation extends AbstractSet<Triangle> {

    private Triangle mostRecent = null; // Most recently "active" triangle
    private Graph<Triangle> triGraph; // Holds triangles for navigation

    /**
     * All sites must fall within the initial triangle.
     *
     * @param triangle
     *            the initial triangle
     */
    public Triangulation(Triangle triangle) {
        triGraph = new Graph<>();
        triGraph.add(triangle);
        mostRecent = triangle;
    }

    /* The following two methods are required by AbstractSet */

    @Override
    public Iterator<Triangle> iterator() {
        return triGraph.nodeSet().iterator();
    }

    @Override
    public int size() {
        return triGraph.nodeSet().size();
    }

    @Override
    public String toString() {
        return "Triangulation with " + size() + " triangles"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * True iff triangle is a member of this triangulation. This method isn't
     * required by AbstractSet, but it improves efficiency.
     *
     * @param triangle
     *            the object to check for membership
     */
    @Override
    public boolean contains(Object triangle) {
        return triGraph.nodeSet().contains(triangle);
    }

    /**
     * Report neighbor opposite the given vertex of triangle.
     *
     * @param site
     *            a vertex of triangle
     * @param triangle
     *            we want the neighbor of this triangle
     * @return the neighbor opposite site in triangle; null if none
     * @throws IllegalArgumentException
     *             if site is not in this triangle
     */
    public Triangle neighborOpposite(Pnt site, Triangle triangle) {
        if (!triangle.contains(site))
            throw new IllegalArgumentException("Bad vertex; not in triangle"); //$NON-NLS-1$
        for (Triangle neighbor : triGraph.neighbors(triangle)) {
            if (!neighbor.contains(site))
                return neighbor;
        }
        return null;
    }

    /**
     * Return the set of triangles adjacent to triangle.
     *
     * @param triangle
     *            the triangle to check
     * @return the neighbors of triangle
     */
    public Set<Triangle> neighbors(Triangle triangle) {
        return triGraph.neighbors(triangle);
    }

    /**
     * Report triangles surrounding site in order (cw or ccw).
     *
     * @param site
     *            we want the surrounding triangles for this site
     * @param triangle
     *            a "starting" triangle that has site as a vertex
     * @return all triangles surrounding site in order (cw or ccw)
     * @throws IllegalArgumentException
     *             if site is not in triangle
     */
    public List<Triangle> surroundingTriangles(Pnt site, Triangle triangle) {
        if (!triangle.contains(site))
            throw new IllegalArgumentException("Site not in triangle"); //$NON-NLS-1$
        List<Triangle> list = new ArrayList<>();
        Triangle start = triangle;
        Pnt guide = triangle.getVertexButNot(site); // Affects cw or ccw
        while (true) {
            list.add(triangle);
            Triangle previous = triangle;
            triangle = this.neighborOpposite(guide, triangle); // Next triangle
            guide = previous.getVertexButNot(site, guide); // Update guide
            if (triangle == start)
                break;
        }
        return list;
    }

    /**
     * Locate the triangle with point inside it or on its boundary.
     *
     * @param point
     *            the point to locate
     * @return the triangle that holds point; null if no such triangle
     */
    public Triangle locate(Pnt point) {
        Triangle triangle = mostRecent;
        if (!this.contains(triangle))
            triangle = null;

        // Try a directed walk (this works fine in 2D, but can fail in 3D)
        Set<Triangle> visited = new HashSet<>();
        while (triangle != null) {
            if (visited.contains(triangle)) { // This should never happen
                System.out.println("Warning: Caught in a locate loop"); //$NON-NLS-1$
                break;
            }
            visited.add(triangle);
            // Corner opposite point
            Pnt corner = point.isOutside(triangle.toArray(new Pnt[0]));
            if (corner == null)
                return triangle;
            triangle = this.neighborOpposite(corner, triangle);
        }
        // No luck; try brute force
        System.out.println("Warning: Checking all triangles for " + point); //$NON-NLS-1$
        for (Triangle tri : this) {
            if (point.isOutside(tri.toArray(new Pnt[0])) == null)
                return tri;
        }
        // No such triangle
        System.out.println("Warning: No triangle holds " + point); //$NON-NLS-1$
        return null;
    }

    /**
     * Place a new site into the DT. Nothing happens if the site matches an
     * existing DT vertex.
     *
     * @param site
     *            the new Pnt
     * @throws IllegalArgumentException
     *             if site does not lie in any triangle
     */
    public void delaunayPlace(Pnt site) {
        // Uses straightforward scheme rather than best asymptotic time

        // Locate containing triangle
        Triangle triangle = locate(site);
        // Give up if no containing triangle or if site is already in DT
        if (triangle == null)
            throw new IllegalArgumentException("No containing triangle"); //$NON-NLS-1$
        if (triangle.contains(site))
            return;

        // Determine the cavity and update the triangulation
        Set<Triangle> cavity = getCavity(site, triangle);
        Triangle nextMostRecent = update(site, cavity);
        if (nextMostRecent != null)
            mostRecent = nextMostRecent;
    }

    /**
     * Determine the cavity caused by site.
     *
     * @param site
     *            the site causing the cavity
     * @param triangle
     *            the triangle containing site
     * @return set of all triangles that have site in their circumcircle
     */
    private Set<Triangle> getCavity(Pnt site, Triangle triangle) {
        Set<Triangle> encroached = new HashSet<>();
        Queue<Triangle> toBeChecked = new LinkedList<>();
        Set<Triangle> marked = new HashSet<>();
        toBeChecked.add(triangle);
        marked.add(triangle);
        while (!toBeChecked.isEmpty()) {
            triangle = toBeChecked.remove();
            if (site.vsCircumcircle(triangle.toArray(new Pnt[0])) == 1)
                continue; // Site outside triangle => triangle not in cavity
            encroached.add(triangle);
            // Check the neighbors
            for (Triangle neighbor : triGraph.neighbors(triangle)) {
                if (marked.contains(neighbor))
                    continue;
                marked.add(neighbor);
                toBeChecked.add(neighbor);
            }
        }
        return encroached;
    }

    /**
     * Update the triangulation by removing the cavity triangles and then
     * filling the cavity with new triangles.
     *
     * @param site
     *            the site that created the cavity
     * @param cavity
     *            the triangles with site in their circumcircle
     * @return one of the new triangles
     */
    private Triangle update(Pnt site, Set<Triangle> cavity) {
        Set<Set<Pnt>> boundary = new HashSet<>();
        Set<Triangle> theTriangles = new HashSet<>();

        // Find boundary facets and adjacent triangles
        for (Triangle triangle : cavity) {
            theTriangles.addAll(neighbors(triangle));
            for (Pnt vertex : triangle) {
                Set<Pnt> facet = triangle.facetOpposite(vertex);
                if (boundary.contains(facet))
                    boundary.remove(facet);
                else
                    boundary.add(facet);
            }
        }
        theTriangles.removeAll(cavity); // Adj triangles only

        // Remove the cavity triangles from the triangulation
        for (Triangle triangle : cavity)
            triGraph.remove(triangle);

        // Build each new triangle and add it to the triangulation
        Set<Triangle> newTriangles = new HashSet<>();
        for (Set<Pnt> vertices : boundary) {
            vertices.add(site);
            Triangle tri = new Triangle(vertices);
            triGraph.add(tri);
            newTriangles.add(tri);
        }

        // Update the graph links for each new triangle
        theTriangles.addAll(newTriangles); // Adj triangle + new triangles
        for (Triangle triangle : newTriangles)
            for (Triangle other : theTriangles)
                if (triangle.isNeighbor(other))
                    triGraph.add(triangle, other);

        // Return one of the new triangles
        if (newTriangles.iterator().hasNext()) {
            return newTriangles.iterator().next();
        }
        return null;
    }

}