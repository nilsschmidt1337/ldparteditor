package org.nschmidt.csg;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.nschmidt.ldparteditor.helpers.math.MathHelper;

enum CSGOptimizerFlipTriangle {
    INSTANCE;

    public static boolean optimize(Random rnd, Map<Plane, List<GData3>> trianglesPerPlane, Map<GData3, IdAndPlane> optimization, Map<GData3, Map<GData3, Boolean>> flipCache) {
        boolean result = false;

        for (List<GData3> triangles : trianglesPerPlane.values()) {

            final Map<VectorCSGd, Map<VectorCSGd, List<GData3>>> edgeMap = new TreeMap<>();
            final Map<GData3, VectorCSGd[]> trimap = new HashMap<>();

            // Get edge info
            for (GData3 tri : triangles) {
                final VectorCSGd[] triverts = new VectorCSGd[]{
                        new VectorCSGd(tri.x1, tri.y1, tri.z1),
                        new VectorCSGd(tri.x2, tri.y2, tri.z2),
                        new VectorCSGd(tri.x3, tri.y3, tri.z3)};

                trimap.put(tri, triverts);

                for (int i = 0; i < 3; i++) {
                    VectorCSGd a = triverts[i];
                    for (int j = i + 1; j < 3; j++) {
                        VectorCSGd b = triverts[j];

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

            GData3 ta = null;
            GData3 tb = null;
            VectorCSGd na = null;
            VectorCSGd nb = null;
            VectorCSGd nc = null;
            VectorCSGd o = null;

            boolean endSearch = false;

            for (GData3 tri : triangles) {
                final VectorCSGd[] triverts = trimap.get(tri);
                for (int i = 0; i < 3; i++) {
                    VectorCSGd a = triverts[i];
                    VectorCSGd b = triverts[(i + 1) % 3];
                    List<GData3> commonTris = edgeMap.get(a).get(b);
                    if (commonTris.size() == 2) {
                        GData3 other = commonTris.get(0) == tri ? commonTris.get(1) : commonTris.get(0);

                        if (flipCache.containsKey(tri) && flipCache.get(tri).containsKey(other)) {
                            continue;
                        }

                        VectorCSGd c = triverts[(i + 2) % 3];
                        VectorCSGd co;
                        {
                            Set<VectorCSGd> ov = new TreeSet<>();
                            ov.addAll(Arrays.asList(trimap.get(other)));
                            ov.remove(a);
                            ov.remove(b);
                            if (ov.size() == 0) {
                                optimization.remove(other);
                                flipCache.remove(other);
                                continue;
                            }
                            co = ov.iterator().next();
                        }

                        if (MathHelper.canBeProjectedToLineSegmentCSG(a, b, c)
                                && MathHelper.canBeProjectedToLineSegmentCSG(a, b, co)
                                && MathHelper.hasNarrowAngleDistribution(a, b, c, a, co, b, c, co, a, co, b, c)) {
                            ta = tri;
                            tb = other;
                            na = c;
                            nc = co;
                            nb = a;
                            o = b;
                            endSearch = rnd.nextBoolean();
                            if (endSearch) break;
                        } else {
                            if (!flipCache.containsKey(tri)) flipCache.put(tri, new HashMap<>());
                            if (!flipCache.containsKey(other)) flipCache.put(other, new HashMap<>());
                            {
                                Map<GData3, Boolean> map = flipCache.get(tri);
                                if (!map.containsKey(other)) map.put(other, null);
                            }
                            {
                                Map<GData3, Boolean> map = flipCache.get(other);
                                if (!map.containsKey(tri)) map.put(tri, null);
                            }
                        }
                    }
                }
                if (endSearch) break;
            }

            if (na != null) {
                final IdAndPlane oldIdA = optimization.get(ta);
                final IdAndPlane oldIdB = optimization.get(tb);
                optimization.remove(ta);
                optimization.remove(tb);
                flipCache.remove(ta);
                flipCache.remove(tb);
                optimization.put(createTriangle(ta, nc, na, nb, 4), oldIdA);
                optimization.put(createTriangle(tb, nc, o, na, 14), oldIdB);
                result = true;
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
