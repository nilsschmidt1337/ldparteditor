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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.nschmidt.ldparteditor.dialog.smooth.SmoothDialog;
import org.nschmidt.ldparteditor.enumtype.Threshold;

class VM25Smooth extends VM24MeshReducer {

    protected VM25Smooth(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public void smooth(boolean x, boolean y, boolean z, BigDecimal factor, int iterations) {

        SmoothDialog.setX(x);
        SmoothDialog.setY(y);
        SmoothDialog.setZ(z);

        SmoothDialog.setFactor(factor);
        SmoothDialog.setIterations(iterations);

        Object[] obj = getSmoothedVertices(selectedVertices);
        clearSelection();

        @SuppressWarnings("unchecked")
        List<Vertex> newVerts = (List<Vertex>) obj[0];

        @SuppressWarnings("unchecked")
        List<Vertex> oldVerts = (List<Vertex>) obj[3];

        int size = newVerts.size();
        for (int i = 0; i < size; i++) {
            Vertex v1 = oldVerts.get(i);
            Vertex v2 = newVerts.get(i);

            if (!v1.equals(v2)) {
                changeVertexDirectFast(v1, v2, true);
                selectedVertices.add(v2);
            }
        }

        if (!selectedVertices.isEmpty()) {
            setModifiedNoSync();
            linkedDatFile.getVertexManager().restoreHideShowState();
            syncWithTextEditors(true);
            updateUnsavedStatus();
        }
    }


    private Vertex[] getNeighbourVertices(Vertex old) {

        SortedSet<Vertex> tverts1 = new TreeSet<>();
        SortedSet<Vertex> tverts2 = new TreeSet<>();
        Set<GData> surfs = getLinkedSurfaces(old);

        for (GData g : surfs) {
            Vertex[] v;
            if (g.type() == 3) {
                v = triangles.get(g);
            } else if (g.type() == 4) {
                v = quads.get(g);
            } else {
                continue;
            }
            for (Vertex v2 : v) {
                if (tverts1.contains(v2)) {
                    tverts2.add(v2);
                }
                tverts1.add(v2);
            }
        }
        tverts2.remove(old);

        if (tverts2.size() != surfs.size()) {
            return new Vertex[0];
        }

        Vertex[] result = new Vertex[tverts2.size()];
        Iterator<Vertex> it = tverts2.iterator();
        for (int i = 0; i < result.length; i++) {
            result[i] = it.next();
        }
        return result;
    }

    Object[] getSmoothedVertices(Set<Vertex> verts) {

        final boolean isX = SmoothDialog.isX();
        final boolean isY = SmoothDialog.isY();
        final boolean isZ = SmoothDialog.isZ();

        List<Vertex> vertsToProcess = new ArrayList<>();
        List<Vertex> originalVerts = new ArrayList<>();
        List<Vertex> newPos = new ArrayList<>();

        SortedSet<Vertex> origVerts = new TreeSet<>();
        origVerts.addAll(verts);

        {
            SortedSet<Vertex> allVerts = new TreeSet<>();
            for (Vertex vertex : verts) {
                allVerts.add(vertex);
                allVerts.addAll(Arrays.asList(getNeighbourVertices(vertex)));
            }
            vertsToProcess.addAll(allVerts);
            originalVerts.addAll(vertsToProcess);
        }


        SortedMap<Integer, List<Integer>> adjacency = new TreeMap<>();
        SortedMap<Vertex, Integer> indmap = new TreeMap<>();

        int i = 0;
        for (Vertex vertex : vertsToProcess) {
            indmap.put(vertex, i);
            i++;
        }

        for (Vertex vertex : origVerts) {
            Integer key = indmap.get(vertex);
            List<Integer> ad;
            if (adjacency.containsKey(key)) {
                ad = adjacency.get(key);
            } else {
                ad = new ArrayList<>();
                adjacency.put(key, ad);
            }
            for (Vertex vertex2 : getNeighbourVertices(vertex)) {
                ad.add(indmap.get(vertex2));
            }
        }

        final BigDecimal factor = SmoothDialog.getFactor();
        final BigDecimal oneMinusFactor = BigDecimal.ONE.subtract(factor);
        final int iterations = SmoothDialog.getIterations();
        final int size = vertsToProcess.size();
        for (int j = 0; j < iterations; j++) {
            i = 0;
            newPos.clear();
            for (Vertex vertex : vertsToProcess) {
                if (origVerts.contains(vertex)) {

                    List<Integer> il = adjacency.get(indmap.get(vertex));

                    if (!il.isEmpty()) {
                        final BigDecimal ad = new BigDecimal(il.size());

                        BigDecimal vx = BigDecimal.ZERO;
                        BigDecimal vy = BigDecimal.ZERO;
                        BigDecimal vz = BigDecimal.ZERO;

                        for (Integer k : il) {
                            if (isX) vx = vx.add(vertsToProcess.get(k).xp);
                            if (isY) vy = vy.add(vertsToProcess.get(k).yp);
                            if (isZ) vz = vz.add(vertsToProcess.get(k).zp);
                        }

                        if (isX) {
                            vx = vx.divide(ad, Threshold.MC).multiply(factor).add(vertex.xp.multiply(oneMinusFactor));
                        } else {
                            vx = vertex.xp;
                        }
                        if (isY) {
                            vy = vy.divide(ad, Threshold.MC).multiply(factor).add(vertex.yp.multiply(oneMinusFactor));
                        } else {
                            vy = vertex.yp;
                        }
                        if (isZ) {
                            vz = vz.divide(ad, Threshold.MC).multiply(factor).add(vertex.zp.multiply(oneMinusFactor));
                        } else {
                            vz = vertex.zp;
                        }

                        newPos.add(new Vertex(vx, vy, vz));

                    } else {
                        newPos.add(vertex);
                    }

                } else {
                    newPos.add(null);
                }
                i++;
            }
            origVerts.clear();
            indmap.clear();
            i = 0;

            while (i < size) {
                Vertex nv = newPos.get(i);
                if (nv != null) {
                    origVerts.add(nv);
                    vertsToProcess.set(i, nv);
                    indmap.put(nv, i);
                } else {
                    indmap.put(vertsToProcess.get(i), i);
                }
                i++;
            }

        }

        return new Object[]{vertsToProcess, indmap, adjacency, originalVerts};
    }
}
