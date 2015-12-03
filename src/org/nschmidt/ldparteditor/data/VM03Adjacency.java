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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.nschmidt.ldparteditor.enums.View;

class VM03Adjacency extends VM02Add {

    public VM03Adjacency(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    protected boolean isConnected2(GData g1, GData g2) {

        int t1 = g1.type();
        int t2 = g2.type();

        TreeSet<Vertex> v1 = new TreeSet<Vertex>();
        TreeSet<Vertex> v2 = new TreeSet<Vertex>();

        GData1 p1 = null;
        GData1 p2 = null;

        switch (t1) {
        case 3:
            Vertex[] va = triangles.get(g1);
            v1.add(va[0]);
            v1.add(va[1]);
            v1.add(va[2]);
            p1 = ((GData3) g1).parent;
            break;
        case 4:
            Vertex[] va2 = quads.get(g1);
            v1.add(va2[0]);
            v1.add(va2[1]);
            v1.add(va2[2]);
            v1.add(va2[3]);
            p1 = ((GData4) g1).parent;
            break;
        default:
            return false;
        }

        switch (t2) {
        case 3:
            Vertex[] va = triangles.get(g2);
            v2.add(va[0]);
            v2.add(va[1]);
            v2.add(va[2]);
            p2 = ((GData3) g2).parent;
            break;
        case 4:
            Vertex[] va2 = quads.get(g2);
            v2.add(va2[0]);
            v2.add(va2[1]);
            v2.add(va2[2]);
            v2.add(va2[3]);
            p2 = ((GData4) g2).parent;
            break;
        default:
            return false;
        }

        if (!p1.equals(View.DUMMY_REFERENCE) && p1.equals(p2)) return true;

        int co = v1.size();
        v1.removeAll(v2);
        int cn = v1.size();

        return 2 == co - cn;
    }

    public HashSet<GData> getLinkedSurfaces(Vertex vertex) {
        HashSet<GData> rval = new HashSet<GData>();
        Set<VertexManifestation> vm = vertexLinkedToPositionInFile.get(vertex);
        if (vm != null) {
            for (VertexManifestation m : vm) {
                int type = m.getGdata().type();
                if (type > 2 || type < 5)
                    rval.add(m.getGdata());
            }
        }
        return rval;
    }

    public GData2 hasEdge(Vertex v1, Vertex v2) {
        Set<VertexManifestation> m1 = vertexLinkedToPositionInFile.get(v1);
        Set<VertexManifestation> m2 = vertexLinkedToPositionInFile.get(v2);
        for (VertexManifestation a : m1) {
            for (VertexManifestation b : m2) {
                if (a.getGdata().equals(b.getGdata()) && b.getGdata().type() == 2) {
                    if (!lineLinkedToVertices.containsKey(b.getGdata())) return null;
                    return (GData2) b.getGdata();
                }
            }
        }
        return null;
    }

    public GData5 hasCondline(Vertex v1, Vertex v2) {
        Set<VertexManifestation> m1 = vertexLinkedToPositionInFile.get(v1);
        Set<VertexManifestation> m2 = vertexLinkedToPositionInFile.get(v2);
        for (VertexManifestation a : m1) {
            if (a.getPosition() > 1) continue;
            for (VertexManifestation b : m2) {
                if (b.getPosition() > 1) continue;
                if (a.getGdata().equals(b.getGdata()) && b.getGdata().type() == 5) {
                    if (!lineLinkedToVertices.containsKey(b.getGdata())) return null;
                    return (GData5) b.getGdata();
                }
            }
        }
        return null;
    }

    protected ArrayList<GData> linkedCommonFaces(TreeSet<Vertex> h1, TreeSet<Vertex> h2, Vertex[] rv1, Vertex[] rv2) {
        ArrayList<GData> result = new ArrayList<GData>();
        Set<VertexManifestation> m1 = new HashSet<VertexManifestation>();
        Set<VertexManifestation> m2 = new HashSet<VertexManifestation>();
        for (Vertex v1 : h1) {
            m1.addAll(vertexLinkedToPositionInFile.get(v1));
        }
        for (Vertex v2 : h2) {
            m2.addAll(vertexLinkedToPositionInFile.get(v2));
        }
        for (VertexManifestation a : m1) {
            for (VertexManifestation b : m2) {
                GData bg = b.getGdata();
                if (a.getGdata().equals(bg) && (bg.type() == 3 || bg.type() == 4)) {
                    result.add(bg);
                }
            }
        }
        rv1[0] = h1.iterator().next();
        rv2[0] = h2.iterator().next();
        return result;
    }

    public ArrayList<GData> linkedCommonFaces(Vertex v1, Vertex v2) {
        ArrayList<GData> result = new ArrayList<GData>();
        Set<VertexManifestation> m1 = vertexLinkedToPositionInFile.get(v1);
        Set<VertexManifestation> m2 = vertexLinkedToPositionInFile.get(v2);
        for (VertexManifestation a : m1) {
            for (VertexManifestation b : m2) {
                GData bg = b.getGdata();
                if (a.getGdata().equals(bg) && (bg.type() == 3 || bg.type() == 4)) {
                    result.add(bg);
                }
            }
        }
        return result;
    }

    public boolean isNeighbour(Vertex v1, Vertex v2) {
        Set<VertexManifestation> m1 = vertexLinkedToPositionInFile.get(v1);
        Set<VertexManifestation> m2 = vertexLinkedToPositionInFile.get(v2);
        for (VertexManifestation a : m1) {
            for (VertexManifestation b : m2) {
                if (a.getGdata().equals(b.getGdata())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Tests, if two surfaces share a common edge
     * @param g1 a surface
     * @param g2 another surface
     * @return {@code true} if they do / {@code false} otherwise
     */
    public boolean hasSameEdge(GData g1, GData g2) {

        int t1 = g1.type();
        int t2 = g2.type();

        TreeSet<Vertex> v1 = new TreeSet<Vertex>();
        TreeSet<Vertex> v2 = new TreeSet<Vertex>();

        switch (t1) {
        case 3:
            Vertex[] va = triangles.get(g1);
            v1.add(va[0]);
            v1.add(va[1]);
            v1.add(va[2]);
            break;
        case 4:
            Vertex[] va2 = quads.get(g1);
            v1.add(va2[0]);
            v1.add(va2[1]);
            v1.add(va2[2]);
            v1.add(va2[3]);
            break;
        default:
            return false;
        }

        switch (t2) {
        case 3:
            Vertex[] va = triangles.get(g2);
            v2.add(va[0]);
            v2.add(va[1]);
            v2.add(va[2]);
            break;
        case 4:
            Vertex[] va2 = quads.get(g2);
            v2.add(va2[0]);
            v2.add(va2[1]);
            v2.add(va2[2]);
            v2.add(va2[3]);
            break;
        default:
            return false;
        }

        int co = v1.size();
        v1.removeAll(v2);
        int cn = v1.size();

        return 2 == co - cn;
    }

    /**
     * Tests, if two surfaces share a common edge, or if a surface has an edge
     * @param g1 a surface
     * @param g2 another surface
     * @param adjaencyByPrecision a map which contains informations about near vertices
     * @return {@code true} if they do / {@code false} otherwise
     */
    protected boolean hasSameEdge(GData g1, GData g2, TreeMap<Vertex, TreeSet<Vertex>> adjaencyByPrecision) {

        int t1 = g1.type();
        int t2 = g2.type();

        TreeSet<Vertex> v1 = new TreeSet<Vertex>();
        TreeSet<Vertex> v2 = new TreeSet<Vertex>();


        switch (t1) {
        case 2:
            Vertex[] va0 = lines.get(g1);
            v1.add(va0[0]);
            v1.add(va0[1]);
            break;
        case 3:
            Vertex[] va = triangles.get(g1);
            v1.add(va[0]);
            v1.add(va[1]);
            v1.add(va[2]);
            break;
        case 4:
            Vertex[] va2 = quads.get(g1);
            v1.add(va2[0]);
            v1.add(va2[1]);
            v1.add(va2[2]);
            v1.add(va2[3]);
            break;
        default:
            return false;
        }

        switch (t2) {
        case 2:
            Vertex[] va0 = lines.get(g2);
            v2.add(va0[0]);
            v2.add(va0[1]);
            break;
        case 3:
            Vertex[] va = triangles.get(g2);
            v2.add(va[0]);
            v2.add(va[1]);
            v2.add(va[2]);
            break;
        case 4:
            Vertex[] va2 = quads.get(g2);
            v2.add(va2[0]);
            v2.add(va2[1]);
            v2.add(va2[2]);
            v2.add(va2[3]);
            break;
        default:
            return false;
        }

        // Create the sets

        ArrayList<TreeSet<Vertex>> setList1 = new ArrayList<TreeSet<Vertex>>();
        ArrayList<TreeSet<Vertex>> setList2 = new ArrayList<TreeSet<Vertex>>();

        for (Vertex v : v1) {
            TreeSet<Vertex> newSet = new TreeSet<Vertex>();
            newSet.addAll(adjaencyByPrecision.get(v));
            setList1.add(newSet);
        }

        for (Vertex v : v2) {
            TreeSet<Vertex> newSet = new TreeSet<Vertex>();
            newSet.addAll(adjaencyByPrecision.get(v));
            setList2.add(newSet);
        }

        // Now we have to detect a least 2 set intersections

        int intersections = 0;
        for (TreeSet<Vertex> s1 : setList1) {
            for (TreeSet<Vertex> s2 : setList2) {
                TreeSet<Vertex> newSet = new TreeSet<Vertex>();
                newSet.addAll(s1);
                int co = newSet.size();
                newSet.removeAll(s2);
                int cn = newSet.size();
                if (co != cn) {
                    intersections++;
                    if (intersections == 2) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Tests, if an edge and a surface share a common edge, or if the edge is equal to another edge
     * @param e1 an edge
     * @param g2 a surface
     * @param adjaencyByPrecision a map which contains informations about near vertices
     * @return {@code true} if they do / {@code false} otherwise
     */
    protected boolean hasSameEdge(AccurateEdge e1, GData g2, TreeMap<Vertex, TreeSet<Vertex>> adjaencyByPrecision) {

        int t2 = g2.type();

        TreeSet<Vertex> v1 = new TreeSet<Vertex>();
        TreeSet<Vertex> v2 = new TreeSet<Vertex>();

        v1.add(e1.v1);
        v1.add(e1.v2);

        switch (t2) {
        case 2:
            Vertex[] va0 = lines.get(g2);
            v2.add(va0[0]);
            v2.add(va0[1]);
            break;
        case 3:
            Vertex[] va = triangles.get(g2);
            v2.add(va[0]);
            v2.add(va[1]);
            v2.add(va[2]);
            break;
        case 4:
            Vertex[] va2 = quads.get(g2);
            v2.add(va2[0]);
            v2.add(va2[1]);
            v2.add(va2[2]);
            v2.add(va2[3]);
            break;
        default:
            return false;
        }

        // Create the sets

        ArrayList<TreeSet<Vertex>> setList1 = new ArrayList<TreeSet<Vertex>>();
        ArrayList<TreeSet<Vertex>> setList2 = new ArrayList<TreeSet<Vertex>>();

        for (Vertex v : v1) {
            TreeSet<Vertex> newSet = new TreeSet<Vertex>();
            newSet.addAll(adjaencyByPrecision.get(v));
            setList1.add(newSet);
        }

        for (Vertex v : v2) {
            TreeSet<Vertex> newSet = new TreeSet<Vertex>();
            newSet.addAll(adjaencyByPrecision.get(v));
            setList2.add(newSet);
        }

        // Now we have to detect a least 2 set intersections

        int intersections = 0;
        for (TreeSet<Vertex> s1 : setList1) {
            for (TreeSet<Vertex> s2 : setList2) {
                TreeSet<Vertex> newSet = new TreeSet<Vertex>();
                newSet.addAll(s1);
                int co = newSet.size();
                newSet.removeAll(s2);
                int cn = newSet.size();
                if (co != cn) {
                    intersections++;
                    if (intersections == 2) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public HashSet<GData3> getLinkedTriangles(Vertex vertex) {
        HashSet<GData3> rval = new HashSet<GData3>();
        Set<VertexManifestation> vm = vertexLinkedToPositionInFile.get(vertex);
        if (vm != null) {
            for (VertexManifestation m : vm) {
                if (m.getGdata().type() == 3)
                    rval.add((GData3) m.getGdata());
            }
        }
        return rval;
    }
}
