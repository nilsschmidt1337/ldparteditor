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
package org.nschmidt.ldparteditor.data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.composite3d.Edger2Settings;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;

class VM06Edger2 extends VM05Distance {

    protected VM06Edger2(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    /**
     * Investigates the BFC orientation of type 2-5 lines
     * @param g the data to analyse
     * @return {@code BFC.CW|BFC.CCW|BFC.NOCERTIFY|BFC.NOCLIP}
     */
    private byte getBFCorientation(GData g) {
        if (bfcMap.containsKey(g)) {
            return bfcMap.get(g);
        }
        return BFC.NOCERTIFY;
    }

    private void addEdgeEdger2(TreeSet<Vertex> h1, TreeSet<Vertex> h2) {
        for (Vertex v1 : h1) {
            for (Vertex v2 : h2) {
                // if v1 is connected with v2 draw a line from v1 to v2
                if (isNeighbour(v1, v2)) {
                    addLine(v1, v2);
                }
            }
        }
    }

    private void addLineEdger2(TreeSet<Vertex> h1, TreeSet<Vertex> h2, Edger2Settings es) {

        Vertex[] rv1 = new Vertex[1];
        Vertex[] rv2 = new Vertex[1];
        ArrayList<GData> faces = linkedCommonFaces(h1, h2, rv1, rv2);
        if (faces.size() == 2) {
            Vertex v1 = rv1[0];
            Vertex v2 = rv2[0];

            GData g1 = faces.get(0);
            GData g2 = faces.get(1);

            Vertex v3 = null;
            Vertex v4 = null;

            Vector3d n1;
            Vector3d n2;
            if (g1.type() == 3) {
                GData3 g3 = (GData3) g1;
                Vertex[] vt = triangles.get(g3);
                TreeSet<Vertex> tvs = new TreeSet<Vertex>();
                tvs.add(vt[0]);
                tvs.add(vt[1]);
                tvs.add(vt[2]);
                tvs.remove(v1);
                tvs.remove(v2);
                v3 = tvs.iterator().next();
                n1 = Vector3d.getNormal(new Vector3d(vt[2]), new Vector3d(vt[0]), new Vector3d(vt[1]));
            } else {
                GData4 g4 = (GData4) g1;
                Vertex[] vq = quads.get(g4);
                if (vq[0].equals(v1) && vq[1].equals(v2)) {
                    n1 = Vector3d.getNormal(new Vector3d(vq[2]), new Vector3d(vq[0]), new Vector3d(vq[1])); // T1 1-2-3
                    v3 = vq[2];
                } else if (vq[1].equals(v1) && vq[2].equals(v2)) {
                    n1 = Vector3d.getNormal(new Vector3d(vq[2]), new Vector3d(vq[0]), new Vector3d(vq[1])); // T1 1-2-3
                    v3 = vq[0];
                } else if (vq[2].equals(v1) && vq[3].equals(v2)) {
                    n1 = Vector3d.getNormal(new Vector3d(vq[0]), new Vector3d(vq[1]), new Vector3d(vq[3])); // 22 3-4-1
                    v3 = vq[0];
                } else if (vq[3].equals(v1) && vq[0].equals(v2)) {
                    n1 = Vector3d.getNormal(new Vector3d(vq[0]), new Vector3d(vq[1]), new Vector3d(vq[3])); // 22 3-4-1
                    v3 = vq[2];
                } else if (vq[0].equals(v2) && vq[1].equals(v1)) {
                    n1 = Vector3d.getNormal(new Vector3d(vq[2]), new Vector3d(vq[0]), new Vector3d(vq[1])); // T1 1-2-3
                    v3 = vq[2];
                } else if (vq[1].equals(v2) && vq[2].equals(v1)) {
                    n1 = Vector3d.getNormal(new Vector3d(vq[2]), new Vector3d(vq[0]), new Vector3d(vq[1])); // T1 1-2-3
                    v3 = vq[0];
                } else if (vq[2].equals(v2) && vq[3].equals(v1)) {
                    n1 = Vector3d.getNormal(new Vector3d(vq[0]), new Vector3d(vq[1]), new Vector3d(vq[3])); // T2 3-4-1
                    v3 = vq[0];
                } else {
                    n1 = Vector3d.getNormal(new Vector3d(vq[0]), new Vector3d(vq[1]), new Vector3d(vq[3])); // T2 3-4-1
                    v3 = vq[2];
                }
            }
            if (g2.type() == 3) {
                GData3 g3 = (GData3) g2;
                Vertex[] vt = triangles.get(g3);
                TreeSet<Vertex> tvs = new TreeSet<Vertex>();
                tvs.add(vt[0]);
                tvs.add(vt[1]);
                tvs.add(vt[2]);
                tvs.remove(v1);
                tvs.remove(v2);
                v4 = tvs.iterator().next();
                n2 = Vector3d.getNormal(new Vector3d(vt[2]), new Vector3d(vt[0]), new Vector3d(vt[1]));
            } else {
                GData4 g4 = (GData4) g2;
                Vertex[] vq = quads.get(g4);
                if (vq[0].equals(v1) && vq[1].equals(v2)) {
                    n2 = Vector3d.getNormal(new Vector3d(vq[2]), new Vector3d(vq[0]), new Vector3d(vq[1])); // T1 1-2-3
                    v4 = vq[2];
                } else if (vq[1].equals(v1) && vq[2].equals(v2)) {
                    n2 = Vector3d.getNormal(new Vector3d(vq[2]), new Vector3d(vq[0]), new Vector3d(vq[1])); // T1 1-2-3
                    v4 = vq[0];
                } else if (vq[2].equals(v1) && vq[3].equals(v2)) {
                    n2 = Vector3d.getNormal(new Vector3d(vq[0]), new Vector3d(vq[1]), new Vector3d(vq[3])); // 22 3-4-1
                    v4 = vq[0];
                } else if (vq[3].equals(v1) && vq[0].equals(v2)) {
                    n2 = Vector3d.getNormal(new Vector3d(vq[0]), new Vector3d(vq[1]), new Vector3d(vq[3])); // 22 3-4-1
                    v4 = vq[2];
                } else if (vq[0].equals(v2) && vq[1].equals(v1)) {
                    n2 = Vector3d.getNormal(new Vector3d(vq[2]), new Vector3d(vq[0]), new Vector3d(vq[1])); // T1 1-2-3
                    v4 = vq[2];
                } else if (vq[1].equals(v2) && vq[2].equals(v1)) {
                    n2 = Vector3d.getNormal(new Vector3d(vq[2]), new Vector3d(vq[0]), new Vector3d(vq[1])); // T1 1-2-3
                    v4 = vq[0];
                } else if (vq[2].equals(v2) && vq[3].equals(v1)) {
                    n2 = Vector3d.getNormal(new Vector3d(vq[0]), new Vector3d(vq[1]), new Vector3d(vq[3])); // T2 3-4-1
                    v4 = vq[0];
                } else {
                    n2 = Vector3d.getNormal(new Vector3d(vq[0]), new Vector3d(vq[1]), new Vector3d(vq[3])); // T2 3-4-1
                    v4 = vq[2];
                }
            }

            double angle;
            if (es.isExtendedRange()) {
                if (getBFCorientation(g1) == BFC.CCW) {
                    n1.negate();
                }
                if (getBFCorientation(g2) == BFC.CCW) {
                    n2.negate();
                }
                angle = Vector3d.angle(n1, n2);
            } else {
                angle = Vector3d.angle(n1, n2);
                if(angle > 90.0) angle = 180.0 - angle;
            }

            if (angle <= es.getAf().doubleValue()) {
                // No Line
            } else if (angle > es.getAf().doubleValue() && angle <= es.getAc().doubleValue()) {
                // Condline
                Editor3DWindow.getWindow().setLastUsedColour(View.getLDConfigColour(16));
                addCondline(v1, v2, v3, v4);
            } else if (angle > es.getAc().doubleValue() && angle <= es.getAe().doubleValue()) {
                // Condline + Edge Line
                Editor3DWindow.getWindow().setLastUsedColour(View.getLDConfigColour(16));
                addCondline(v1, v2, v3, v4);
                Editor3DWindow.getWindow().setLastUsedColour(View.getLDConfigColour(2));
                addLine(v1, v2);
            } else {
                // Edge Line
                Editor3DWindow.getWindow().setLastUsedColour(View.getLDConfigColour(16));
                addLine(v1, v2);
            }

        } else {
            Editor3DWindow.getWindow().setLastUsedColour(View.getLDConfigColour(16));
            addLine(h1.iterator().next(), h2.iterator().next());
        }
    }

    private void addLineQuadEdger2(GData4 g4, HashSet<AccurateEdge> presentEdges, Edger2Settings es, TreeMap<Vertex, Vertex> snap) {

        Vertex[] verts = quads.get(g4);

        Vertex v1 = verts[0];
        Vertex v2 = verts[2];

        if (presentEdges.contains(new AccurateEdge(snap.get(v1), snap.get(v2)))) return;

        Vector3d n1;
        Vector3d n2;

        Vertex v3 = verts[1];
        Vertex v4 = verts[3];
        n1 = Vector3d.getNormal(new Vector3d(verts[2]), new Vector3d(verts[0]), new Vector3d(verts[1])); // T1 1-2-3
        n2 = Vector3d.getNormal(new Vector3d(verts[0]), new Vector3d(verts[1]), new Vector3d(verts[3])); // T2 3-4-1
        double angle;
        if (es.isExtendedRange()) {
            if (getBFCorientation(g4) == BFC.CCW) {
                n1.negate();
                n2.negate();
            }
            angle = Vector3d.angle(n1, n2);
        } else {
            angle = Vector3d.angle(n1, n2);
            if(angle > 90.0) angle = 180.0 - angle;
        }
        if (angle <= es.getAf().doubleValue()) {
            // No Line
        } else if (angle > es.getAf().doubleValue() && angle <= es.getAc().doubleValue()) {
            // Condline
            Editor3DWindow.getWindow().setLastUsedColour(View.getLDConfigColour(16));
            addCondline(v1, v2, v3, v4);
        } else if (angle > es.getAc().doubleValue() && angle <= es.getAe().doubleValue()) {
            // Condline + Edge Line
            Editor3DWindow.getWindow().setLastUsedColour(View.getLDConfigColour(16));
            addCondline(v1, v2, v3, v4);
            Editor3DWindow.getWindow().setLastUsedColour(View.getLDConfigColour(2));
            addLine(v1, v2);
        } else {
            // Edge Line
            Editor3DWindow.getWindow().setLastUsedColour(View.getLDConfigColour(16));
            addLine(v1, v2);
        }
    }

    public void addEdges(Edger2Settings es) {

        if (linkedDatFile.isReadOnly()) return;

        initBFCmap();

        final BigDecimal ed = es.getEqualDistance();
        TreeMap<Vertex, Vertex> snap = new TreeMap<Vertex, Vertex>();
        TreeMap<Vertex, TreeSet<Vertex>> snapToOriginal = new TreeMap<Vertex, TreeSet<Vertex>>();

        HashMap<AccurateEdge, Integer> edges = new HashMap<AccurateEdge, Integer>();
        HashSet<AccurateEdge> presentEdges = new HashSet<AccurateEdge>();

        switch (es.getScope()) {
        case 0: // All Data
        {
            Set<Vertex> allVerts = vertexLinkedToPositionInFile.keySet();
            for (Vertex vertex : allVerts) {
                if (!snap.containsKey(vertex)) snap.put(vertex, new Vertex(
                        vertex.X.subtract(vertex.X.remainder(ed, Threshold.mc)).setScale(ed.scale(), RoundingMode.HALF_UP),
                        vertex.Y.subtract(vertex.Y.remainder(ed, Threshold.mc)).setScale(ed.scale(), RoundingMode.HALF_UP),
                        vertex.Z.subtract(vertex.Z.remainder(ed, Threshold.mc)).setScale(ed.scale(), RoundingMode.HALF_UP)
                        ));
                if (snapToOriginal.containsKey(snap.get(vertex))) {
                    snapToOriginal.get(snap.get(vertex)).add(vertex);
                } else {
                    TreeSet<Vertex> h = new TreeSet<Vertex>();
                    h.add(vertex);
                    snapToOriginal.put(snap.get(vertex), h);
                }
            }
            Set<GData2> lins = lines.keySet();
            for (GData2 g2 : lins) {
                if (!g2.isLine) {
                    continue;
                }    
                Vertex[] verts = lines.get(g2);
                AccurateEdge e1 = new AccurateEdge(snap.get(verts[0]), snap.get(verts[1]));
                presentEdges.add(e1);            
            }
            Set<GData5> clins = condlines.keySet();
            for (GData5 g5 : clins) {
                Vertex[] verts = condlines.get(g5);
                AccurateEdge e1 = new AccurateEdge(snap.get(verts[0]), snap.get(verts[1]));
                presentEdges.add(e1);
            }

            Set<GData3> tris = triangles.keySet();
            for (GData3 g3 : tris) {
                if (!g3.isTriangle) {
                    continue;
                }
                Vertex[] verts = triangles.get(g3);
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[0]), snap.get(verts[1]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[1]), snap.get(verts[2]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[2]), snap.get(verts[0]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
            }
            Set<GData4> qs = quads.keySet();
            for (GData4 g4 : qs) {
                if (es.isCondlineOnQuads()) addLineQuadEdger2(g4, presentEdges, es, snap);
                Vertex[] verts = quads.get(g4);
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[0]), snap.get(verts[1]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[1]), snap.get(verts[2]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[2]), snap.get(verts[3]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[3]), snap.get(verts[0]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
            }

            GColour tmpCol = Editor3DWindow.getWindow().getLastUsedColour();
            Editor3DWindow.getWindow().setLastUsedColour(View.getLDConfigColour(16));
            if (es.getUnmatchedMode() < 2) {
                Set<AccurateEdge> ee = edges.keySet();
                for (AccurateEdge e : ee) {
                    if (edges.get(e) > 1) {
                        addLineEdger2(snapToOriginal.get(e.v1),  snapToOriginal.get(e.v2), es);
                    }
                }
            }

            Editor3DWindow.getWindow().setLastUsedColour(View.getLDConfigColour(4));
            if (es.getUnmatchedMode() != 1) {
                Set<AccurateEdge> ee = edges.keySet();
                for (AccurateEdge e : ee) {
                    if (edges.get(e) == 1) {
                        addEdgeEdger2(snapToOriginal.get(e.v1),  snapToOriginal.get(e.v2));
                    }
                }
            }
            Editor3DWindow.getWindow().setLastUsedColour(tmpCol);

        }
        break;
        case 1: // No Subfile Facets
        {
            Set<Vertex> allVerts = vertexLinkedToPositionInFile.keySet();
            for (Vertex vertex : allVerts) {
                if (!snap.containsKey(vertex)) snap.put(vertex, new Vertex(
                        vertex.X.subtract(vertex.X.remainder(ed, Threshold.mc)).setScale(ed.scale(), RoundingMode.HALF_UP),
                        vertex.Y.subtract(vertex.Y.remainder(ed, Threshold.mc)).setScale(ed.scale(), RoundingMode.HALF_UP),
                        vertex.Z.subtract(vertex.Z.remainder(ed, Threshold.mc)).setScale(ed.scale(), RoundingMode.HALF_UP)
                        ));
                if (snapToOriginal.containsKey(snap.get(vertex))) {
                    snapToOriginal.get(snap.get(vertex)).add(vertex);
                } else {
                    TreeSet<Vertex> h = new TreeSet<Vertex>();
                    h.add(vertex);
                    snapToOriginal.put(snap.get(vertex), h);
                }
            }
            Set<GData2> lins = lines.keySet();
            for (GData2 g2 : lins) {
                if (!g2.isLine) {
                    continue;
                }
                Vertex[] verts = lines.get(g2);
                AccurateEdge e1 = new AccurateEdge(snap.get(verts[0]), snap.get(verts[1]));
                presentEdges.add(e1);
            }
            Set<GData5> clins = condlines.keySet();
            for (GData5 g5 : clins) {
                Vertex[] verts = condlines.get(g5);
                AccurateEdge e1 = new AccurateEdge(snap.get(verts[0]), snap.get(verts[1]));
                presentEdges.add(e1);
            }

            Set<GData3> tris = triangles.keySet();
            for (GData3 g3 : tris) {
                if (!lineLinkedToVertices.containsKey(g3) || !g3.isTriangle) continue;
                Vertex[] verts = triangles.get(g3);
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[0]), snap.get(verts[1]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[1]), snap.get(verts[2]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[2]), snap.get(verts[0]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
            }
            Set<GData4> qs = quads.keySet();
            for (GData4 g4 : qs) {
                if (!lineLinkedToVertices.containsKey(g4)) continue;
                addLineQuadEdger2(g4, presentEdges, es, snap);
                Vertex[] verts = quads.get(g4);
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[0]), snap.get(verts[1]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[1]), snap.get(verts[2]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[2]), snap.get(verts[3]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[3]), snap.get(verts[0]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
            }

            GColour tmpCol = Editor3DWindow.getWindow().getLastUsedColour();
            Editor3DWindow.getWindow().setLastUsedColour(View.getLDConfigColour(16));
            if (es.getUnmatchedMode() < 2) {
                Set<AccurateEdge> ee = edges.keySet();
                for (AccurateEdge e : ee) {
                    if (edges.get(e) > 1) {
                        addLineEdger2(snapToOriginal.get(e.v1),  snapToOriginal.get(e.v2), es);
                    }
                }
            }

            Editor3DWindow.getWindow().setLastUsedColour(View.getLDConfigColour(4));
            if (es.getUnmatchedMode() != 1) {
                Set<AccurateEdge> ee = edges.keySet();
                for (AccurateEdge e : ee) {
                    if (edges.get(e) == 1) {
                        addEdgeEdger2(snapToOriginal.get(e.v1),  snapToOriginal.get(e.v2));
                    }
                }
            }
            Editor3DWindow.getWindow().setLastUsedColour(tmpCol);

        }
        break;
        case 2: // Selected Data Only
        {
            Set<Vertex> allVerts = vertexLinkedToPositionInFile.keySet();
            for (Vertex vertex : allVerts) {
                if (!snap.containsKey(vertex)) snap.put(vertex, new Vertex(
                        vertex.X.subtract(vertex.X.remainder(ed, Threshold.mc)).setScale(ed.scale(), RoundingMode.HALF_UP),
                        vertex.Y.subtract(vertex.Y.remainder(ed, Threshold.mc)).setScale(ed.scale(), RoundingMode.HALF_UP),
                        vertex.Z.subtract(vertex.Z.remainder(ed, Threshold.mc)).setScale(ed.scale(), RoundingMode.HALF_UP)
                        ));
                if (snapToOriginal.containsKey(snap.get(vertex))) {
                    snapToOriginal.get(snap.get(vertex)).add(vertex);
                } else {
                    TreeSet<Vertex> h = new TreeSet<Vertex>();
                    h.add(vertex);
                    snapToOriginal.put(snap.get(vertex), h);
                }
            }
            Set<GData2> lins = lines.keySet();
            for (GData2 g2 : lins) {
                if (!g2.isLine) {
                    continue;
                }
                Vertex[] verts = lines.get(g2);
                AccurateEdge e1 = new AccurateEdge(snap.get(verts[0]), snap.get(verts[1]));
                presentEdges.add(e1);
            }
            Set<GData5> clins = condlines.keySet();
            for (GData5 g5 : clins) {
                Vertex[] verts = condlines.get(g5);
                AccurateEdge e1 = new AccurateEdge(snap.get(verts[0]), snap.get(verts[1]));
                presentEdges.add(e1);
            }

            Set<GData3> tris = triangles.keySet();
            for (GData3 g3 : tris) {
                if (!lineLinkedToVertices.containsKey(g3) || !g3.isTriangle) continue;
                Vertex[] verts = triangles.get(g3);
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[0]), snap.get(verts[1]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[1]), snap.get(verts[2]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[2]), snap.get(verts[0]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
            }
            Set<GData4> qs = quads.keySet();
            for (GData4 g4 : qs) {
                if (!lineLinkedToVertices.containsKey(g4)) continue;
                Vertex[] verts = quads.get(g4);
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[0]), snap.get(verts[1]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[1]), snap.get(verts[2]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[2]), snap.get(verts[3]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
                {
                    AccurateEdge e = new AccurateEdge(snap.get(verts[3]), snap.get(verts[0]));
                    if (!presentEdges.contains(e)) {
                        if (edges.containsKey(e)) {
                            edges.put(e, edges.get(e) + 1);
                        } else {
                            edges.put(e, 1);
                        }
                    }
                }
            }


            {
                HashSet<AccurateEdge> selectedEdges = new HashSet<AccurateEdge>();

                for (GData3 g3 : selectedTriangles) {
                    if (!g3.isTriangle) continue;
                    Vertex[] verts = triangles.get(g3);
                    {
                        AccurateEdge e = new AccurateEdge(snap.get(verts[0]), snap.get(verts[1]));
                        if (!presentEdges.contains(e)) {
                            selectedEdges.add(e);
                        }
                    }
                    {
                        AccurateEdge e = new AccurateEdge(snap.get(verts[1]), snap.get(verts[2]));
                        if (!presentEdges.contains(e)) {
                            selectedEdges.add(e);
                        }
                    }
                    {
                        AccurateEdge e = new AccurateEdge(snap.get(verts[2]), snap.get(verts[0]));
                        if (!presentEdges.contains(e)) {
                            selectedEdges.add(e);
                        }
                    }
                }
                for (GData4 g4 : selectedQuads) {
                    addLineQuadEdger2(g4, presentEdges, es, snap);
                    Vertex[] verts = quads.get(g4);
                    {
                        AccurateEdge e = new AccurateEdge(snap.get(verts[0]), snap.get(verts[1]));
                        if (!presentEdges.contains(e)) {
                            selectedEdges.add(e);
                        }
                    }
                    {
                        AccurateEdge e = new AccurateEdge(snap.get(verts[1]), snap.get(verts[2]));
                        if (!presentEdges.contains(e)) {
                            selectedEdges.add(e);
                        }
                    }
                    {
                        AccurateEdge e = new AccurateEdge(snap.get(verts[2]), snap.get(verts[3]));
                        if (!presentEdges.contains(e)) {
                            selectedEdges.add(e);
                        }
                    }
                    {
                        AccurateEdge e = new AccurateEdge(snap.get(verts[3]), snap.get(verts[0]));
                        if (!presentEdges.contains(e)) {
                            selectedEdges.add(e);
                        }
                    }
                }

                Set<AccurateEdge> keySet = edges.keySet();
                for(Iterator<AccurateEdge> it = keySet.iterator(); it.hasNext();) {
                    if (!selectedEdges.contains(it.next())) {
                        it.remove();
                    }
                }
            }

            GColour tmpCol = Editor3DWindow.getWindow().getLastUsedColour();
            Editor3DWindow.getWindow().setLastUsedColour(View.getLDConfigColour(16));
            if (es.getUnmatchedMode() < 2) {
                Set<AccurateEdge> ee = edges.keySet();
                for (AccurateEdge e : ee) {
                    if (edges.get(e) > 1) {
                        addLineEdger2(snapToOriginal.get(e.v1),  snapToOriginal.get(e.v2), es);
                    }
                }
            }

            Editor3DWindow.getWindow().setLastUsedColour(View.getLDConfigColour(4));
            if (es.getUnmatchedMode() != 1) {
                Set<AccurateEdge> ee = edges.keySet();
                for (AccurateEdge e : ee) {
                    if (edges.get(e) == 1) {
                        addEdgeEdger2(snapToOriginal.get(e.v1),  snapToOriginal.get(e.v2));
                    }
                }
            }
            Editor3DWindow.getWindow().setLastUsedColour(tmpCol);
        }
        break;
        default:
            break;
        }

        disposeBFCmap();

        if (isModified()) {
            setModified(true, true);
        }
        validateState();

    }

    private void initBFCmap() {
        linkedDatFile.getBFCorientationMap(bfcMap);
    }

    private void disposeBFCmap() {
        bfcMap.clear();
    }
}
