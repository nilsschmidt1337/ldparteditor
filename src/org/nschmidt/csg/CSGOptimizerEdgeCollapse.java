/* MIT - License

Copyright (c) 2012 - this year, Nils Schmidt

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package org.nschmidt.csg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.GData3;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;

public enum CSGOptimizerEdgeCollapse {
    INSTANCE;

    public static volatile double epsilon = 0.9999;

    static boolean optimize(Map<Plane, List<GData3>> trianglesPerPlane, Map<GData3, IdAndPlane> optimization) {
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
                final TreeSet<VectorCSGd> verts = new TreeSet<>();
                for (final GData3 g : surfs) {
                    verts.addAll(Arrays.asList(trimap.get(g)));
                }

                // 3.2 Ist das Polygon geschlossen?
                final int delta = ((verts.size() - 1) - surfs.size());
                final boolean polygonLoop = delta == 0;

                // 4. Entferne den Ursprungspunkt aus der Menge
                verts.remove(v);

                // 5. Prüfe die Kandidaten
                for (final VectorCSGd t : verts) {
                    if (foundOptimization) break;

                    final List<GData3> tsurfs = new ArrayList<>(linkedSurfaceMap.get(t));
                    final int oldcount = tsurfs.size();
                    tsurfs.removeAll(surfs);

                    // 5.1 t muss zwei Flächen mit v teilen

                    int vexp = 2;

                    final  int ds = oldcount - tsurfs.size();
                    if (ds == 1) {
                        vexp = 1;
                    } else if (ds != 2 || !polygonLoop) {
                        continue;
                    }

                    // 5.2 t darf nur zwei angrenzende Punkte mit v teilen
                    {
                        final TreeSet<VectorCSGd> verts2 = new TreeSet<>();
                        for (final GData3 gData : new ArrayList<>(linkedSurfaceMap.get(t))) {
                            verts2.addAll(Arrays.asList(trimap.get(gData)));
                        }
                        verts2.remove(t);
                        verts2.retainAll(verts);
                        if (verts2.size() != vexp) {
                            continue;
                        }
                    }

                    if (!polygonLoop) {
                        boolean noInterpolation = true;
                        VectorCSGd ref = t.minus(v);
                        double m;
                        if (delta == 1 && (m = ref.magnitude())  > 0.0) {
                            ref = ref.dividedBy(m);
                            for (VectorCSGd r : verts) {
                                if (r != t) {
                                    VectorCSGd ref2 = v.minus(r);
                                    double m2 = ref2.magnitude();
                                    if (m2 > 0.0) {
                                        ref2 = ref2.dividedBy(m2);
                                        double diskr = ref.dot(ref2);
                                        if (diskr > epsilon) {
                                            {
                                                final TreeSet<VectorCSGd> verts2 = new TreeSet<>();
                                                for (final GData3 gData : new ArrayList<>(linkedSurfaceMap.get(r))) {
                                                    verts2.addAll(Arrays.asList(trimap.get(gData)));
                                                }
                                                verts2.remove(r);
                                                verts2.retainAll(verts);
                                                if (verts2.size() != 1) {
                                                    continue;
                                                }
                                            }
                                            noInterpolation = false;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        if (noInterpolation) {
                            continue;
                        }
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
                        HashSet<Integer> ignoreSet = new HashSet<>();
                        for (s = 0; s < surfcount; s++) {
                            for (int i = 0; i < 3; i++) {
                                if (surfsv[s][i].compareTo(t) == 0) {
                                    ignoreSet.add(s);
                                }
                                if (surfsv[s][i].compareTo(v) == 0) {
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
                    doOptimize(v, t, optimization, linkedSurfaceMap, trimap);
                    foundOptimization = true;
                    result = true;
                    break;
                }
            }
        }

        return result;
    }

    private static void doOptimize(VectorCSGd v, VectorCSGd t,
            Map<GData3, IdAndPlane> optimization,
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
                    optimization.put(createTriangle(g, nv.get(0), nv.get(1), nv.get(2)), optimization.get(g)); // Purple
                }
            }

            optimization.remove(g);
        }

    }

    private static GData3 createTriangle(GData3 idol, VectorCSGd a, VectorCSGd b, VectorCSGd c) {
        Vertex v1 = new Vertex((float) a.x, (float) a.y, (float) a.z);
        Vertex v2 = new Vertex((float) b.x, (float) b.y, (float) b.z);
        Vertex v3 = new Vertex((float) c.x, (float) c.y, (float) c.z);
        GData1 parent = idol.parent;
        GColour colour = new GColour(idol.colourNumber, idol.r, idol.g, idol.b, idol.a);
        return new GData3(v1, v2, v3, parent, colour, true);
    }
}
