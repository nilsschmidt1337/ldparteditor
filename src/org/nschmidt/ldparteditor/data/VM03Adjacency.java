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

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.nschmidt.csg.CSG;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.math.HashBiMap;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeSortedMap;
import org.nschmidt.ldparteditor.text.DatParser;

class VM03Adjacency extends VM02Add {

    protected VM03Adjacency(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    protected boolean isConnected2(GData g1, GData g2) {

        int t1 = g1.type();
        int t2 = g2.type();

        SortedSet<Vertex> v1 = new TreeSet<>();
        SortedSet<Vertex> v2 = new TreeSet<>();

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

    public Set<GData> getLinkedSurfaces(Vertex vertex) {
        Set<GData> rval = new HashSet<>();
        Set<VertexManifestation> vm = vertexLinkedToPositionInFile.get(vertex);
        if (vm != null) {
            getManifestationLock().lock();
            for (VertexManifestation m : vm) {
                int type = m.getGdata().type();
                if (type < 5 && type > 2)
                    rval.add(m.getGdata());
            }
            getManifestationLock().unlock();
        }
        return rval;
    }

    public Set<GData> getLinkedSurfacesOfSameColour(Vertex vertex) {
        Set<GData> rval = new HashSet<>();
        Set<VertexManifestation> vm = vertexLinkedToPositionInFile.get(vertex);
        if (vm != null) {
            GColour colour = null;
            getManifestationLock().lock();
            for (VertexManifestation m : vm) {
                int type = m.getGdata().type();
                GColour col = null;
                if (type == 3) {
                    GData3 gd = (GData3) m.getGdata();
                    col = new GColour(gd.colourNumber, gd.r, gd.g, gd.b, gd.a);
                }
                if (type == 4) {
                    GData4 gd = (GData4) m.getGdata();
                    col = new GColour(gd.colourNumber, gd.r, gd.g, gd.b, gd.a);
                }
                if (col != null) {
                    if (colour == null) colour = col;
                    if (colour.equals(col)) {
                        rval.add(m.getGdata());
                    }
                }
            }
            getManifestationLock().unlock();
        }
        return rval;
    }

    public GData2 hasEdge(Vertex v1, Vertex v2) {
        Set<VertexManifestation> m1 = vertexLinkedToPositionInFile.get(v1);
        Set<VertexManifestation> m2 = vertexLinkedToPositionInFile.get(v2);
        if (m1 == null || m2 == null) {
            return null;
        }
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
        if (m1 == null || m2 == null) {
            return null;
        }
        getManifestationLock().lock();
        for (VertexManifestation a : m1) {
            if (a.getPosition() > 1) continue;
            for (VertexManifestation b : m2) {
                if (b.getPosition() > 1) continue;
                if (a.getGdata().equals(b.getGdata()) && b.getGdata().type() == 5) {
                    if (!lineLinkedToVertices.containsKey(b.getGdata())) {
                        getManifestationLock().unlock();
                        return null;
                    }
                    getManifestationLock().unlock();
                    return (GData5) b.getGdata();
                }
            }
        }
        getManifestationLock().unlock();
        return null;
    }

    public boolean hasCondlineAndNoEdge(Vertex v1, Vertex v2) {
        boolean hasCondline = false;
        Set<VertexManifestation> m1 = vertexLinkedToPositionInFile.get(v1);
        Set<VertexManifestation> m2 = vertexLinkedToPositionInFile.get(v2);
        if (m1 == null || m2 == null) {
            return false;
        }
        getManifestationLock().lock();
        for (VertexManifestation a : m1) {
            if (a.getPosition() > 1) continue;
            for (VertexManifestation b : m2) {
                if (b.getPosition() > 1) continue;
                if (a.getGdata().equals(b.getGdata())) {
                    final int type = b.getGdata().type();
                    if (type == 5) {
                        hasCondline = true;
                    } else if (type == 2) {
                        getManifestationLock().unlock();
                        return false;
                    }
                }
            }
        }
        getManifestationLock().unlock();
        return hasCondline;
    }

    protected List<GData> linkedCommonFaces(SortedSet<Vertex> h1, SortedSet<Vertex> h2) {
        List<GData> result = new ArrayList<>();
        Set<VertexManifestation> m1 = new HashSet<>();
        Set<VertexManifestation> m2 = new HashSet<>();
        if (h1 == null || h2 == null) {
            return result;
        }
        for (Vertex v1 : h1) {
            m1.addAll(vertexLinkedToPositionInFile.get(v1));
        }
        for (Vertex v2 : h2) {
            m2.addAll(vertexLinkedToPositionInFile.get(v2));
        }
        for (VertexManifestation a : m1) {
            for (VertexManifestation b : m2) {
                GData bg = b.getGdata();
                if (a.getGdata().equals(bg)) {
                    switch (bg.type()) {
                    case 3:
                        if (((GData3) bg).isTriangle) {
                            result.add(bg);
                        }
                        continue;
                    case 4:
                        result.add(bg);
                        continue;
                    default:
                        continue;
                    }
                }
            }
        }
        return result;
    }

    public List<GData> linkedCommonFaces(Vertex v1, Vertex v2) {
        List<GData> result = new ArrayList<>();
        Set<VertexManifestation> m1 = vertexLinkedToPositionInFile.get(v1);
        Set<VertexManifestation> m2 = vertexLinkedToPositionInFile.get(v2);
        if (m1 == null || m2 == null) {
            return result;
        }
        getManifestationLock().lock();
        for (VertexManifestation a : m1) {
            for (VertexManifestation b : m2) {
                GData bg = b.getGdata();
                if (a.getGdata().equals(bg)) {
                    switch (bg.type()) {
                    case 3:
                        if (((GData3) bg).isTriangle) {
                            result.add(bg);
                        }
                        continue;
                    case 4:
                        result.add(bg);
                        continue;
                    default:
                        continue;
                    }
                }
            }
        }
        getManifestationLock().unlock();
        return result;
    }

    public boolean isNeighbour(Vertex v1, Vertex v2) {
        Set<VertexManifestation> m1 = vertexLinkedToPositionInFile.get(v1);
        Set<VertexManifestation> m2 = vertexLinkedToPositionInFile.get(v2);
        if (m1 == null || m2 == null) {
            return false;
        }
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

        SortedSet<Vertex> v1 = new TreeSet<>();
        SortedSet<Vertex> v2 = new TreeSet<>();

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
    protected boolean hasSameEdge(GData g1, GData g2, SortedMap<Vertex, SortedSet<Vertex>> adjaencyByPrecision) {

        int t1 = g1.type();
        int t2 = g2.type();

        SortedSet<Vertex> v1 = new TreeSet<>();
        SortedSet<Vertex> v2 = new TreeSet<>();


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

        List<SortedSet<Vertex>> setList1 = new ArrayList<>();
        List<SortedSet<Vertex>> setList2 = new ArrayList<>();

        for (Vertex v : v1) {
            SortedSet<Vertex> newSet = new TreeSet<>();
            newSet.addAll(adjaencyByPrecision.get(v));
            setList1.add(newSet);
        }

        for (Vertex v : v2) {
            SortedSet<Vertex> newSet = new TreeSet<>();
            newSet.addAll(adjaencyByPrecision.get(v));
            setList2.add(newSet);
        }

        // Now we have to detect a least 2 set intersections

        int intersections = 0;
        for (SortedSet<Vertex> s1 : setList1) {
            for (SortedSet<Vertex> s2 : setList2) {
                SortedSet<Vertex> newSet = new TreeSet<>();
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

    public boolean isEdgeAdjacentToSelectedData(GData g1, Set<? extends GData> data, SortedMap<Vertex, SortedSet<Vertex>> adjaencyByPrecision) {
        if (data == null) {
            data = selectedData;
        }
        for (GData g2 : data) {
            if (hasSameEdge2(g1, g2, adjaencyByPrecision)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Tests, if two surfaces share a common edge, or if a surface has an edge or a condline
     * @param g1 a surface
     * @param g2 another surface
     * @param adjaencyByPrecision a map which contains informations about near vertices
     * @return {@code true} if they do / {@code false} otherwise
     */
    private boolean hasSameEdge2(GData g1, GData g2, SortedMap<Vertex, SortedSet<Vertex>> adjaencyByPrecision) {

        int t1 = g1.type();
        int t2 = g2.type();

        SortedSet<Vertex> v1 = new TreeSet<>();
        SortedSet<Vertex> v2 = new TreeSet<>();

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
        case 5:
            Vertex[] va3 = condlines.get(g1);
            v1.add(va3[0]);
            v1.add(va3[1]);
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
        case 5:
            Vertex[] va3 = condlines.get(g2);
            v2.add(va3[0]);
            v2.add(va3[1]);
            break;
        default:
            return false;
        }

        // Create the sets

        List<SortedSet<Vertex>> setList1 = new ArrayList<>();
        List<SortedSet<Vertex>> setList2 = new ArrayList<>();

        for (Vertex v : v1) {
            SortedSet<Vertex> newSet = new TreeSet<>();
            newSet.addAll(adjaencyByPrecision.get(v));
            setList1.add(newSet);
        }

        for (Vertex v : v2) {
            SortedSet<Vertex> newSet = new TreeSet<>();
            newSet.addAll(adjaencyByPrecision.get(v));
            setList2.add(newSet);
        }

        // Now we have to detect a least 2 set intersections

        int intersections = 0;
        for (SortedSet<Vertex> s1 : setList1) {
            for (SortedSet<Vertex> s2 : setList2) {
                SortedSet<Vertex> newSet = new TreeSet<>();
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
    protected boolean hasSameEdge(AccurateEdge e1, GData g2, SortedMap<Vertex, SortedSet<Vertex>> adjaencyByPrecision) {

        int t2 = g2.type();

        SortedSet<Vertex> v1 = new TreeSet<>();
        SortedSet<Vertex> v2 = new TreeSet<>();

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

        List<SortedSet<Vertex>> setList1 = new ArrayList<>();
        List<SortedSet<Vertex>> setList2 = new ArrayList<>();

        for (Vertex v : v1) {
            SortedSet<Vertex> newSet = new TreeSet<>();
            newSet.addAll(adjaencyByPrecision.get(v));
            setList1.add(newSet);
        }

        for (Vertex v : v2) {
            SortedSet<Vertex> newSet = new TreeSet<>();
            newSet.addAll(adjaencyByPrecision.get(v));
            setList2.add(newSet);
        }

        // Now we have to detect a least 2 set intersections

        int intersections = 0;
        for (SortedSet<Vertex> s1 : setList1) {
            for (SortedSet<Vertex> s2 : setList2) {
                SortedSet<Vertex> newSet = new TreeSet<>();
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

    public synchronized void roundSelection(int coordsDecimalPlaces, int matrixDecimalPlaces, boolean moveAdjacentData, boolean syncWithTextEditors
            , final boolean onX,  final boolean onY,  final boolean onZ) {

        if (linkedDatFile.isReadOnly())
            return;

        final Set<Vertex> singleVertices = Collections.newSetFromMap(new ThreadsafeSortedMap<>());

        final Set<GData0> effSelectedVertices = new HashSet<>();
        final Set<GData2> effSelectedLines = new HashSet<>();
        final Set<GData3> effSelectedTriangles = new HashSet<>();
        final Set<GData4> effSelectedQuads = new HashSet<>();
        final Set<GData5> effSelectedCondlines = new HashSet<>();

        final GColour col16 = View.getLDConfigColour(16);

        selectedData.clear();

        // 0. Deselect selected subfile data (for whole selected subfiles)
        for (GData1 subf : selectedSubfiles) {
            Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
            if (vis == null) continue;
            for (VertexInfo vertexInfo : vis) {
                if (!moveAdjacentData)
                    selectedVertices.remove(vertexInfo.getVertex());
                GData g = vertexInfo.getLinkedData();
                switch (g.type()) {
                case 2:
                    selectedLines.remove(g);
                    break;
                case 3:
                    selectedTriangles.remove(g);
                    break;
                case 4:
                    selectedQuads.remove(g);
                    break;
                case 5:
                    selectedCondlines.remove(g);
                    break;
                default:
                    break;
                }
            }
        }

        // 1. Vertex Based Selection
        {
            final Set<Vertex> objectVertices = Collections.newSetFromMap(new ThreadsafeSortedMap<>());
            {
                Map<GData, Integer> occurMap = new HashMap<>();
                for (Vertex vertex : selectedVertices) {
                    Set<VertexManifestation> occurences = vertexLinkedToPositionInFile.get(vertex);
                    if (occurences == null)
                        continue;
                    boolean isPureSubfileVertex = true;
                    for (VertexManifestation vm : occurences) {
                        GData g = vm.getGdata();
                        int val = 1;
                        if (occurMap.containsKey(g)) {
                            val = occurMap.get(g);
                            val++;
                        }
                        occurMap.put(g, val);
                        switch (g.type()) {
                        case 0:
                            GData0 meta = (GData0) g;
                            boolean idCheck = !lineLinkedToVertices.containsKey(meta);
                            isPureSubfileVertex = isPureSubfileVertex && idCheck;
                            if (val == 1) {
                                if (!idCheck) {
                                    effSelectedVertices.add(meta);
                                }
                            }
                            break;
                        case 2:
                            GData2 line = (GData2) g;
                            idCheck = !line.parent.equals(View.DUMMY_REFERENCE);
                            isPureSubfileVertex = isPureSubfileVertex && idCheck;
                            if (val == 2) {
                                if (!idCheck) {
                                    selectedLines.add(line);
                                }
                            }
                            break;
                        case 3:
                            GData3 triangle = (GData3) g;
                            idCheck = !triangle.parent.equals(View.DUMMY_REFERENCE);
                            isPureSubfileVertex = isPureSubfileVertex && idCheck;
                            if (val == 3) {
                                if (!idCheck) {
                                    selectedTriangles.add(triangle);
                                }
                            }
                            break;
                        case 4:
                            GData4 quad = (GData4) g;
                            idCheck = !quad.parent.equals(View.DUMMY_REFERENCE);
                            isPureSubfileVertex = isPureSubfileVertex && idCheck;
                            if (val == 4) {
                                if (!idCheck) {
                                    selectedQuads.add(quad);
                                }
                            }
                            break;
                        case 5:
                            GData5 condline = (GData5) g;
                            idCheck = !condline.parent.equals(View.DUMMY_REFERENCE);
                            isPureSubfileVertex = isPureSubfileVertex && idCheck;
                            if (val == 4) {
                                if (!idCheck) {
                                    selectedCondlines.add(condline);
                                }
                            }
                            break;
                        default:
                            break;
                        }
                    }
                    if (isPureSubfileVertex)
                        objectVertices.add(vertex);
                }
            }

            // 2. Object Based Selection

            for (GData2 line : selectedLines) {
                if (line.parent.equals(View.DUMMY_REFERENCE))
                    effSelectedLines.add(line);
                Vertex[] verts = lines.get(line);
                if (verts == null)
                    continue;
                objectVertices.addAll(Arrays.asList(verts));
            }
            for (GData3 triangle : selectedTriangles) {
                if (triangle.parent.equals(View.DUMMY_REFERENCE))
                    effSelectedTriangles.add(triangle);
                Vertex[] verts = triangles.get(triangle);
                if (verts == null)
                    continue;
                objectVertices.addAll(Arrays.asList(verts));
            }
            for (GData4 quad : selectedQuads) {
                if (quad.parent.equals(View.DUMMY_REFERENCE))
                    effSelectedQuads.add(quad);
                Vertex[] verts = quads.get(quad);
                if (verts == null)
                    continue;
                objectVertices.addAll(Arrays.asList(verts));
            }
            for (GData5 condline : selectedCondlines) {
                if (condline.parent.equals(View.DUMMY_REFERENCE))
                    effSelectedCondlines.add(condline);
                Vertex[] verts = condlines.get(condline);
                if (verts == null)
                    continue;
                objectVertices.addAll(Arrays.asList(verts));
            }

            Set<GData0> vs = new HashSet<>(effSelectedVertices);
            for (GData0 effvert : vs) {
                Vertex v = effvert.getVertex();
                if (v != null && objectVertices.contains(v)) {
                    singleVertices.add(v);
                }
            }

            singleVertices.addAll(objectVertices);
            singleVertices.addAll(selectedVertices);

            // 3. Rounding of the selected data (no whole subfiles!!)
            // + selectedData update!
            if (!singleVertices.isEmpty()) {
                setModifiedNoSync();
            }
            for (Vertex vOld : singleVertices) {
                Vertex vNew = new Vertex(onX ? vOld.xp.setScale(coordsDecimalPlaces, RoundingMode.HALF_UP) : vOld.xp, onY ? vOld.yp.setScale(coordsDecimalPlaces, RoundingMode.HALF_UP) : vOld.yp, onZ ? vOld.zp.setScale(coordsDecimalPlaces,
                        RoundingMode.HALF_UP) : vOld.zp);
                changeVertexDirectFast(vOld, vNew, moveAdjacentData);
            }

            // 4. Subfile Based Rounding & Selection
            if (!selectedSubfiles.isEmpty()) {
                HashBiMap<Integer, GData> drawPerLine = linkedDatFile.getDrawPerLineNoClone();
                Set<GData1> newSubfiles = new HashSet<>();
                for (GData1 subf : selectedSubfiles) {
                    String roundedString = subf.getRoundedString(coordsDecimalPlaces, matrixDecimalPlaces, onX, onY, onZ);
                    GData roundedSubfile;
                    if (16 == subf.colourNumber) {
                        roundedSubfile = DatParser
                                .parseLine(roundedString, drawPerLine.getKey(subf).intValue(), 0, col16.getR(), col16.getG(), col16.getB(), 1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile, false,
                                        new HashSet<>()).get(0).getGraphicalData();
                    } else {
                        roundedSubfile = DatParser
                                .parseLine(roundedString, drawPerLine.getKey(subf).intValue(), 0, subf.r, subf.g, subf.b, subf.a, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile,
                                        false, new HashSet<>()).get(0).getGraphicalData();
                    }
                    if (roundedSubfile != null) {
                        if (subf.equals(linkedDatFile.getDrawChainTail()))
                            linkedDatFile.setDrawChainTail(roundedSubfile);
                        GData oldNext = subf.getNext();
                        GData oldBefore = subf.getBefore();
                        oldBefore.setNext(roundedSubfile);
                        roundedSubfile.setNext(oldNext);
                        Integer oldNumber = drawPerLine.getKey(subf);
                        if (oldNumber != null)
                            drawPerLine.put(oldNumber, roundedSubfile);
                        remove(subf);
                        newSubfiles.add((GData1) roundedSubfile);
                    }
                }
                selectedSubfiles.clear();
                selectedSubfiles.addAll(newSubfiles);

                for (GData1 subf : selectedSubfiles) {
                    Set<VertexInfo> vis = lineLinkedToVertices.get(subf);
                    if (vis == null) continue;
                    for (VertexInfo vertexInfo : vis) {
                        selectedVertices.add(vertexInfo.getVertex());
                        GData g = vertexInfo.getLinkedData();
                        switch (g.type()) {
                        case 2:
                            selectedLines.add((GData2) g);
                            break;
                        case 3:
                            selectedTriangles.add((GData3) g);
                            break;
                        case 4:
                            selectedQuads.add((GData4) g);
                            break;
                        case 5:
                            selectedCondlines.add((GData5) g);
                            break;
                        default:
                            break;
                        }
                    }
                }
                setModifiedNoSync();
            }

            if (GDataCSG.hasSelectionCSG(linkedDatFile)) {
                Set<GDataCSG> newCSGSelection = new HashSet<>();
                HashBiMap<Integer, GData> drawPerLine = linkedDatFile.getDrawPerLineNoClone();
                for (GDataCSG csg : GDataCSG.getSelection(linkedDatFile)) {
                    if (csg.type == CSG.COMPILE || csg.type == CSG.QUALITY || csg.type == CSG.UNION || csg.type == CSG.DIFFERENCE || csg.type == CSG.INTERSECTION  || csg.type == CSG.EPSILON || csg.type == CSG.TJUNCTION || csg.type == CSG.COLLAPSE || csg.type == CSG.DONTOPTIMIZE || csg.type == CSG.EXTRUDE_CFG) {
                        continue;
                    }
                    GColour col = csg.getColour();
                    String roundedString = csg.getRoundedString(coordsDecimalPlaces, matrixDecimalPlaces, onX, onY, onZ);
                    GData roundedCSG;
                    if (16 == col.getColourNumber()) {
                        roundedCSG = DatParser
                                .parseLine(roundedString, drawPerLine.getKey(csg).intValue(), 0, col16.getR(), col16.getG(), col16.getB(), 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile, false, new HashSet<>())
                                .get(0).getGraphicalData();
                    } else {
                        roundedCSG = DatParser
                                .parseLine(roundedString, drawPerLine.getKey(csg).intValue(), 0, col.getR(), col.getG(), col.getB(), col.getA(), View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, linkedDatFile, false,
                                        new HashSet<>()).get(0).getGraphicalData();
                    }
                    if (roundedCSG != null) {
                        if (csg.equals(linkedDatFile.getDrawChainTail()))
                            linkedDatFile.setDrawChainTail(roundedCSG);
                        GData oldNext = csg.getNext();
                        GData oldBefore = csg.getBefore();
                        oldBefore.setNext(roundedCSG);
                        roundedCSG.setNext(oldNext);
                        Integer oldNumber = drawPerLine.getKey(csg);
                        if (oldNumber != null)
                            drawPerLine.put(oldNumber, roundedCSG);
                        remove(csg);
                        newCSGSelection.add((GDataCSG) roundedCSG);
                    }
                }
                GDataCSG.getSelection(linkedDatFile).clear();
                GDataCSG.getSelection(linkedDatFile).addAll(newCSGSelection);
                setModifiedNoSync();
            }

            if (isModified()) {
                for(Iterator<GData2> it = selectedLines.iterator();it.hasNext();){
                    if (!exist(it.next())) it.remove();
                }
                for(Iterator<GData3> it = selectedTriangles.iterator();it.hasNext();){
                    if (!exist(it.next())) it.remove();
                }
                for(Iterator<GData4> it = selectedQuads.iterator();it.hasNext();){
                    if (!exist(it.next())) it.remove();
                }
                for(Iterator<GData5> it = selectedCondlines.iterator();it.hasNext();){
                    if (!exist(it.next())) it.remove();
                }
                selectedData.addAll(selectedLines);
                selectedData.addAll(selectedTriangles);
                selectedData.addAll(selectedQuads);
                selectedData.addAll(selectedCondlines);
                selectedData.addAll(selectedSubfiles);

                if (syncWithTextEditors){
                    linkedDatFile.getVertexManager().restoreHideShowState();
                    syncWithTextEditors(true);
                }
                updateUnsavedStatus();
            }

            selectedVertices.retainAll(vertexLinkedToPositionInFile.keySet());
        }
    }

    public synchronized Set<GData0> getLinkedVertexMetaCommands(Vertex v) {
        Set<GData0> result = new HashSet<>();
        Set<VertexManifestation> manis = vertexLinkedToPositionInFile.get(v);
        if (manis != null) {
            for (VertexManifestation mani : manis) {
                GData gd = mani.getGdata();
                if (gd != null && gd.type() == 0) {
                    result.add((GData0) gd);
                }
            }
        }
        return result;
    }
}
