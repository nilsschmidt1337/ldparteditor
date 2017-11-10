package org.nschmidt.csg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.nschmidt.ldparteditor.data.GData3;

enum CSGOptimizerFlipTriangle {
    INSTANCE;

    public static boolean optimize(Random rnd, Map<Plane, List<GData3>> trianglesPerPlane, TreeMap<GData3, IdAndPlane> optimization) {
        boolean result = false;

        // FIXME Do iterative optimization here!

        for (List<GData3> triangles : trianglesPerPlane.values()) {

            final Map<VectorCSGd, Map<VectorCSGd, List<GData3>>> edgeMap = new TreeMap<>();

            for (GData3 tri : triangles) {
                final VectorCSGd[] triverts = new VectorCSGd[]{new VectorCSGd(tri.x1, tri.y1, tri.z1), new VectorCSGd(tri.x2, tri.y2, tri.z2), new VectorCSGd(tri.x3, tri.y3, tri.z3)};

                for (VectorCSGd a : triverts) {
                    for (VectorCSGd b : triverts) {
                        if (a == b) continue;
                        if (!edgeMap.containsKey(a)) edgeMap.put(a, new TreeMap<>());
                        if (!edgeMap.containsKey(b)) edgeMap.put(b, new TreeMap<>());
                        {
                            Map<VectorCSGd, List<GData3>> map = edgeMap.get(a);
                            if (!map.containsKey(b)) map.put(b, new ArrayList<>());
                            map.get(b).add(tri);
                        }
                        {
                            Map<VectorCSGd, List<GData3>> map = edgeMap.get(b);
                            if (!map.containsKey(a)) map.put(a, new ArrayList<>());
                            map.get(a).add(tri);
                        }
                    }
                }

            }

        }

        return result;
    }
}
