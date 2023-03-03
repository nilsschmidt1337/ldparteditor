/*
 * Copyright 2016 Gary W. Lucas., modified by Nils Schmidt (removed not required methods)
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

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An implementation of the IConstraint interface intended to store constraints
 * comprised of a polygon. The polygon is allowed to be non-convex, but the
 * segments comprising the polygon must be not intersect except at segment
 * endpoints (e.g. the polygon must be a simple, non intersecting closed loop).
 * All segments in the chain must be non-zero-length.
 * <p>
 * For polygons defining an area, the interior of the area is defined as being
 * bounded by a counter-clockwise polygon. Thus a clockwise polygon would define
 * a "hole" in the area. It is worth noting that this convention is just the
 * opposite of that taken by ESRI's Shapefile format, though it is consistent
 * with conventions used in general computational geometry.
 * <p>
 * <strong>Organizing the list of vertices that defines the polygon</strong>
 * Some implementations of polygon geometries include an extra "closure" vertex
 * so that the last vertex in the list of vertices that defines the polygon is
 * also the first. Although that approach has some advantages, this class does
 * not use it. Each vertex in the polygon geometry is assumed to be unique.
 * Thus, if the polygon represents a triangle, the getVertices and Vertex
 * iterator methods will return exactly three vertices.
 */
public class PolygonConstraint implements Iterable<Pnt> {

    private double squareArea;
    private List<Pnt> list;
    private final Rectangle2D bounds = new Rectangle2D.Double();
    private double x = Double.NaN;
    private double y = Double.NaN;
    private Object applicationData;
    private int constraintIndex;
    private QuadEdge constraintLinkingEdge;
    private IncrementalTin maintainingTin;
    private boolean isComplete;
    private double length;

    /**
     * Standard constructor
     */
    public PolygonConstraint() {
        list = new ArrayList<>();
    }

    public List<Pnt> getVertices() {
        return list;
    }

    public final void complete() {
        if (isComplete) {
            return;
        }

        isComplete = true;

        if (list.size() > 1) {
            // The calling application may have included a "closure" vertex
            // adding the same vertex to both the start and end of the polygon.
            // That approach is not in keeping with the requirement that all
            // vertices in the list be unique. The following logic provides
            // a bit of forgiveness to the applicaiton by removing the extra vertex.
            Pnt a = list.get(0);
            Pnt b = list.get(list.size() - 1);
            if (a.getX() == b.getX() && a.getY() == b.getY()) {
                list.remove(list.size() - 1);
            } else {
                // since no closure was supplied, we need to complete the
                // length calculation to include the last segment.
                length += list.get(0).getDistance(list.get(list.size() - 1));
            }
        }

        if (list.size() < 3) {
            return;
        }

        double xCenter = 0;
        double yCenter = 0;
        for (Pnt v : list) {
            xCenter += v.getX();
            yCenter += v.getY();
        }
        xCenter /= list.size();
        yCenter /= list.size();

        KahanSummation lenSum = new KahanSummation();
        KahanSummation areaSum = new KahanSummation();

        squareArea = 0;
        length = 0;
        Pnt a = list.get(list.size() - 1);
        for (Pnt b : list) {
            lenSum.add(a.getDistance(b));
            double aX = a.getX() - xCenter;
            double aY = a.getY() - yCenter;
            double bX = b.getX() - xCenter;
            double bY = b.getY() - yCenter;
            areaSum.add(aX * bY - aY * bX);
            a = b;
        }
        length = lenSum.getSum();
        squareArea = areaSum.getSum() / 2.0;
    }

    /**
     * Get the computed square area for the constraint polygon. The area is not
     * available until the complete() method is called. It is assumed that the area
     * of a polygon with a counterclockwise orientation is positive and that the
     * area of a polygon with a clockwise orientation is negative.
     *
     * @return if available, a non-zero (potentially negative) square area for the
     *         constraint; otherwise, a zero
     */
    public double getArea() {
        return squareArea;
    }

    public double getNominalPointSpacing() {
        if (list.size() < 2) {
            return Double.NaN;
        }
        if (isComplete) {
            return length / list.size();
        }
        return length / (list.size() - 1);
    }

    PolygonConstraint getConstraintWithNewGeometry(List<Pnt> geometry) {
        PolygonConstraint c = new PolygonConstraint();
        c.applicationData = applicationData;
        c.constraintIndex = constraintIndex;
        c.maintainingTin = maintainingTin;
        c.constraintLinkingEdge = constraintLinkingEdge;
        for (Pnt v : geometry) {
            c.add(v);
            v.setConstraintMember(true);
        }
        c.complete();
        return c;
    }

    public boolean isValid() {
        if (list.size() < 3) {
            return false;
        } else if (list.size() == 3) {
            Pnt v0 = list.get(0);
            Pnt v1 = list.get(2);
            if (v0.getX() == v1.getX() && v0.getY() == v1.getY()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        String appStr = ""; //$NON-NLS-1$
        if (applicationData == null) {
            return "PolygonConstraint, area=" + getArea(); //$NON-NLS-1$
        } else {
            appStr = applicationData.toString();

            return "PolygonConstraint, area=" + getArea() + ", appData=" + appStr; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public final void add(Pnt v) {
        isComplete = false;
    
        double vx = v.getX();
        double vy = v.getY();
        if (list.isEmpty()) {
            bounds.setRect(vx, vy, 0, 0);
        } else if (vx == x && vy == y) {
            return; // quietly ignore duplicate points
        } else {
            length += v.getDistance(x, y);
            bounds.add(vx, vy);
        }
    
        x = vx;
        y = vy;
        v.setConstraintMember(true);
        list.add(v);
    }

    public Rectangle2D getBounds() {
        return bounds;
    
    }

    public void setApplicationData(Object applicationData) {
        this.applicationData = applicationData;
    }

    public Object getApplicationData() {
        return applicationData;
    }

    void setConstraintIndex(IncrementalTin tin, int index) {
        constraintIndex = index;
        maintainingTin = tin;
    }

    public int getConstraintIndex() {
        return constraintIndex;
    }

    public double getLength() {
        return length;
    }

    public Iterator<Pnt> iterator() {
        return list.iterator();
    }

    public QuadEdge getConstraintLinkingEdge() {
        return constraintLinkingEdge;
    }

    public void setConstraintLinkingEdge(QuadEdge edge) {
        constraintLinkingEdge = edge;
    }

    public IncrementalTin getManagingTin() {
        return maintainingTin;
    }
}
