package org.nschmidt.csg;

import java.util.List;

public class NodePolygon {
    private final Node node;
    private final List<Polygon> polygons;

    public NodePolygon(Node node, List<Polygon> polygons) {
        this.node = node;
        this.polygons = polygons;
    }

    public Node getNode() {
        return node;
    }

    public List<Polygon> getPolygons() {
        return polygons;
    }
}
