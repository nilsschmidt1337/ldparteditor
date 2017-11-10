package org.nschmidt.csg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.GData3;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.enums.View;

enum CSGOptimizerEdgeCollapse {
    INSTANCE;

    public static boolean optimize(Random rnd, Map<Plane, List<GData3>> trianglesPerPlane, TreeMap<GData3, IdAndPlane> optimization) {
        boolean result = false;

        // FIXME Do iterative optimization here!

        for (List<GData3> triangles : trianglesPerPlane.values()) {

            final Set<VectorCSGd> vertices = new TreeSet<>();
            final Map<VectorCSGd, List<GData3>> linkedSurfaceMap = new TreeMap<>();
            final Map<GData3, VectorCSGd[]> trimap = new HashMap<>();

            for (GData3 tri : triangles) {
                final VectorCSGd[] triverts = new VectorCSGd[]{new VectorCSGd(tri.x1, tri.y1, tri.z1), new VectorCSGd(tri.x2, tri.y2, tri.z2), new VectorCSGd(tri.x3, tri.y3, tri.z3)};
                vertices.add(triverts[0]);
                vertices.add(triverts[1]);
                vertices.add(triverts[2]);
                if (!linkedSurfaceMap.containsKey(triverts[0])) linkedSurfaceMap.put(triverts[0], new ArrayList<>());
                if (!linkedSurfaceMap.containsKey(triverts[1])) linkedSurfaceMap.put(triverts[1], new ArrayList<>());
                if (!linkedSurfaceMap.containsKey(triverts[2])) linkedSurfaceMap.put(triverts[2], new ArrayList<>());

                linkedSurfaceMap.get(triverts[0]).add(tri);
                linkedSurfaceMap.get(triverts[1]).add(tri);
                linkedSurfaceMap.get(triverts[2]).add(tri);

                trimap.put(tri, triverts);
            }

        }

        return result;
    }

    private static GData3 createTriangle(GData3 idol, VectorCSGd a, VectorCSGd b, VectorCSGd c, int col) {
        Vertex v1 = new Vertex((float) a.x, (float) a.y, (float) a.z);
        Vertex v2 = new Vertex((float) b.x, (float) b.y, (float) b.z);
        Vertex v3 = new Vertex((float) c.x, (float) c.y, (float) c.z);
        GData1 parent = idol.parent;
        GColour colour = View.getLDConfigColour(col); // new GColour(idol.colourNumber, idol.r, idol.g, idol.b, idol.a);
        return new GData3(v1, v2, v3, parent, colour, true);
    }
}
