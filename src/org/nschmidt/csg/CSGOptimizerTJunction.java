package org.nschmidt.csg;

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

enum CSGOptimizerTJunction {
    INSTANCE;

    private static double epsilon;

    public static boolean optimize(Random rnd, Map<Plane, List<GData3>> trianglesPerPlane, TreeMap<GData3, IdAndPlane> optimization) {
        boolean result = false;

        for (List<GData3> triangles : trianglesPerPlane.values()) {

            final Set<VectorCSGd> vertices = new TreeSet<>();
            final Map<GData3, VectorCSGd[]> trimap = new HashMap<>();

            for (GData3 tri : triangles) {
                final VectorCSGd[] triverts = new VectorCSGd[]{new VectorCSGd(tri.x1, tri.y1, tri.z1), new VectorCSGd(tri.x2, tri.y2, tri.z2), new VectorCSGd(tri.x3, tri.y3, tri.z3)};
                vertices.add(triverts[0]);
                vertices.add(triverts[1]);
                vertices.add(triverts[2]);
                trimap.put(tri, triverts);
            }

            // Limit epsilon to max 0.01 LDU
            epsilon = 5.5;

            for (GData3 tri : triangles) {
                final VectorCSGd[] triverts = trimap.get(tri);
                for (VectorCSGd v : vertices) {
                    if (triverts[0].compareTo(v) == 0) continue;
                    if (triverts[1].compareTo(v) == 0) continue;
                    if (triverts[2].compareTo(v) == 0) continue;

                    {
                        double d =  MathHelper.getNearestPointDistanceToLineSegmentCSG(triverts[0], triverts[1], v);
                        if (d < epsilon) {
                            final IdAndPlane oldId = optimization.get(tri);
                            optimization.remove(tri);
                            optimization.put(createTriangle(tri, triverts[0], v, triverts[2]), oldId);
                            optimization.put(createTriangle(tri, v, triverts[1], triverts[2]), oldId);
                            result = true;
                            break;
                        }
                    }

                    {
                        double d = MathHelper.getNearestPointDistanceToLineSegmentCSG(triverts[1], triverts[2], v);
                        if (d < epsilon) {
                            final IdAndPlane oldId = optimization.get(tri);
                            optimization.remove(tri);
                            optimization.put(createTriangle(tri, triverts[1], v, triverts[0]), oldId);
                            optimization.put(createTriangle(tri, v, triverts[2], triverts[0]), oldId);
                            result = true;
                            break;
                        }
                    }

                    {
                        double d = MathHelper.getNearestPointDistanceToLineSegmentCSG(triverts[2], triverts[0], v);
                        if (d < epsilon) {
                            final IdAndPlane oldId = optimization.get(tri);
                            optimization.remove(tri);
                            optimization.put(createTriangle(tri, triverts[1], v, triverts[0]), oldId);
                            optimization.put(createTriangle(tri, v, triverts[1], triverts[2]), oldId);
                            result = true;
                            break;
                        }
                    }
                }
                if (result) break;
            }
        }
        return result;
    }

    private static GData3 createTriangle(GData3 idol, VectorCSGd a, VectorCSGd b, VectorCSGd c) {
        Vertex v1 = new Vertex((float) a.x, (float) a.y, (float) a.z);
        Vertex v2 = new Vertex((float) b.x, (float) b.y, (float) b.z);
        Vertex v3 = new Vertex((float) c.x, (float) c.y, (float) c.z);
        GData1 parent = idol.parent;
        GColour colour = View.getLDConfigColour(42); // new GColour(idol.colourNumber, idol.r, idol.g, idol.b, idol.a);
        return new GData3(v1, v2, v3, parent, colour, true);
    }
}
