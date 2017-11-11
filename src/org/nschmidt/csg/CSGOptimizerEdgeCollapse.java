package org.nschmidt.csg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
import org.nschmidt.ldparteditor.helpers.math.Vector3d;

enum CSGOptimizerEdgeCollapse {
    INSTANCE;

    // TODO This epsilon should be accessible by the user!
    public static volatile double epsilon = 0.9999;

    public static boolean optimize(Random rnd, Map<Plane, List<GData3>> trianglesPerPlane, TreeMap<GData3, IdAndPlane> optimization) {
        boolean result = false;

        for (List<GData3> triangles : trianglesPerPlane.values()) {

            final Set<VectorCSGd> verticesToProcess = new TreeSet<>();
            final Map<VectorCSGd, List<GData3>> linkedSurfaceMap = new TreeMap<>();
            final Map<GData3, VectorCSGd[]> trimap = new HashMap<>();

            for (GData3 tri : triangles) {
                final VectorCSGd[] triverts = new VectorCSGd[]{
                        new VectorCSGd(tri.x1, tri.y1, tri.z1),
                        new VectorCSGd(tri.x2, tri.y2, tri.z2),
                        new VectorCSGd(tri.x3, tri.y3, tri.z3)};

                verticesToProcess.add(triverts[0]);
                verticesToProcess.add(triverts[1]);
                verticesToProcess.add(triverts[2]);

                if (!linkedSurfaceMap.containsKey(triverts[0])) linkedSurfaceMap.put(triverts[0], new ArrayList<>());
                if (!linkedSurfaceMap.containsKey(triverts[1])) linkedSurfaceMap.put(triverts[1], new ArrayList<>());
                if (!linkedSurfaceMap.containsKey(triverts[2])) linkedSurfaceMap.put(triverts[2], new ArrayList<>());

                linkedSurfaceMap.get(triverts[0]).add(tri);
                linkedSurfaceMap.get(triverts[1]).add(tri);
                linkedSurfaceMap.get(triverts[2]).add(tri);

                trimap.put(tri, triverts);
            }

            boolean foundOptimization = false;

            for (final VectorCSGd v : verticesToProcess) {
                if (foundOptimization) break;

                // 1. Ermittle alle angrenzenden Flächen
                List<GData3> surfs = linkedSurfaceMap.get(v);

                // 1.1 Ist es ein eindeutiger Eckpunkt?
                if (surfs.size() == 1) {
                    continue;
                }

                // 2. Ermittle alle angrenzenden Punkte
                final TreeSet<VectorCSGd> verts = new TreeSet<VectorCSGd>();
                for (final GData3 g : surfs) {
                    verts.addAll(Arrays.asList(trimap.get(g)));
                }

                // 3.2 Ist das Polygon geschlossen?
                final boolean isPolygonLoop = (verts.size() - 1) == surfs.size();
                int commonPoints = 0;
                int commonSurfaces = 0;

                // 4. Entferne den Ursprungspunkt aus der Menge
                verts.remove(v);

                // 5. Prüfe die Kandidaten
                for (final VectorCSGd t : verts) {
                    if (foundOptimization) break;

                    final List<GData3> tsurfs = new ArrayList<>(linkedSurfaceMap.get(t));
                    final int oldcount = tsurfs.size();
                    tsurfs.removeAll(surfs);

                    // 5.1 t muss zwei Flächen mit v teilen
                    commonSurfaces = oldcount - tsurfs.size();
                    if (commonSurfaces > 2 || commonSurfaces == 0) {
                        continue;
                    }

                    // 5.2 t darf nur maximal zwei angrenzende Punkte mit v teilen
                    {
                        final TreeSet<VectorCSGd> verts2 = new TreeSet<>();
                        for (final GData3 gData : tsurfs) {
                            verts2.addAll(Arrays.asList(trimap.get(gData)));
                        }
                        verts2.remove(t);
                        int oldcount2 = verts2.size();
                        verts2.removeAll(verts);
                        commonPoints = oldcount2 - verts2.size();
                        if (commonPoints > 2 || commonPoints == 0) {
                            continue;
                        }
                    }

                    if (isPolygonLoop && (commonPoints < 2 || commonSurfaces < 2)) {
                        continue;
                    } else if (!isPolygonLoop && (commonPoints != 1 || verts.size() != 3)) {
                        continue;
                    }

                    // 5.3 die Normalen dürfen nicht kippen!
                    {
                        boolean cont = false;
                        final int surfcount = surfs.size();
                        VectorCSGd[][] surfsv = new VectorCSGd[surfcount][4];
                        Vector3d[] oldNormals = new Vector3d[surfcount];
                        Vector3d[] newNormals = new Vector3d[surfcount];
                        int s = 0;
                        for (final GData3 gData : surfs) {
                            int i = 0;
                            for (VectorCSGd tv : trimap.get(gData)) {
                                surfsv[s][i] = tv;
                                i++;
                            }
                            oldNormals[s] = Vector3d.getNormal(new Vector3d(surfsv[s][0]), new Vector3d(surfsv[s][1]), new Vector3d(surfsv[s][2]));
                            s++;
                        }
                        HashSet<Integer> ignoreSet = new HashSet<Integer>();
                        for (s = 0; s < surfcount; s++) {
                            for (int i = 0; i < 3; i++) {
                                if (surfsv[s][i].equals(t)) {
                                    ignoreSet.add(s);
                                }
                                if (surfsv[s][i].equals(v)) {
                                    surfsv[s][i] = t;
                                }
                            }
                            if (!ignoreSet.contains(s)) {
                                newNormals[s] = Vector3d.getNormal(new Vector3d(surfsv[s][0]), new Vector3d(surfsv[s][1]), new Vector3d(surfsv[s][2]));
                                double angle = Vector3d.angle(oldNormals[s], newNormals[s]);
                                if (angle > 3.0) {
                                    cont = true;
                                    break;
                                }
                            }
                        }
                        if (cont) {
                            continue;
                        }
                    }
                    // Als letzten Schritt => Kante zusammenfallen lassen

                    // v -> t
                    if (isPolygonLoop) {
                        doOptimize(v, t, optimization, linkedSurfaceMap, trimap);
                        foundOptimization = true;
                        result = true;
                        break;
                    } else {
                        if (isBoundaryPoint(t, linkedSurfaceMap, trimap)) {
                            VectorCSGd ref = t.minus(v);
                            double m = ref.magnitude();
                            if (m > 0.0) {
                                ref = ref.dividedBy(m);
                                for (VectorCSGd r : verts) {
                                    if (r != t) {
                                        VectorCSGd ref2 = v.minus(r);
                                        double m2 = ref2.magnitude();
                                        if (m2 > 0.0) {
                                            ref2 = ref2.dividedBy(m2);
                                            double diskr = ref.dot(ref2);
                                            if (diskr > epsilon) {
                                                doOptimize(v, t, optimization, linkedSurfaceMap, trimap);
                                                foundOptimization = true;
                                                result = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    private static void doOptimize(VectorCSGd v, VectorCSGd t,
            TreeMap<GData3, IdAndPlane> optimization,
            Map<VectorCSGd, List<GData3>> linkedSurfaceMap,
            Map<GData3, VectorCSGd[]> trimap) {

        final List<GData3> affectedSurfaces = linkedSurfaceMap.get(v);
        for (GData3 g : affectedSurfaces) {

            Set<VectorCSGd> verts = new TreeSet<>(Arrays.asList(trimap.get(g)));

            if (!verts.contains(t)) {
                List<VectorCSGd> nv = new ArrayList<>(Arrays.asList(trimap.get(g)));
                int i = -1;
                if (nv.get(0).compareTo(v) == 0) i = 0;
                if (nv.get(1).compareTo(v) == 0) i = 1;
                if (nv.get(2).compareTo(v) == 0) i = 2;
                if (i > -1) {
                    nv.set(i, t);
                    optimization.put(createTriangle(g, nv.get(0), nv.get(1), nv.get(2), 449), optimization.get(g)); // Purple
                }
            }

            optimization.remove(g);
        }

    }

    private static boolean isBoundaryPoint(VectorCSGd v, Map<VectorCSGd, List<GData3>> linkedSurfaceMap, Map<GData3, VectorCSGd[]> trimap) {
        final List<GData3> surfs = linkedSurfaceMap.get(v);

        // Ist es ein eindeutiger Eckpunkt?
        if (surfs.size() == 1) {
            return true;
        }

        // Ermittle alle angrenzenden Punkte
        final TreeSet<VectorCSGd> verts = new TreeSet<VectorCSGd>();
        for (final GData3 g : surfs) {
            verts.addAll(Arrays.asList(trimap.get(g)));
        }

        // Ist das Polygon nicht geschlossen?
        return (verts.size() - 1) != surfs.size();
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
