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

import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GData3;
import org.nschmidt.ldparteditor.data.Vertex;

public enum CSGOptimizerUnificator {
    INSTANCE;

    public static volatile double epsilon = 0.1;

    static SortedMap<GData3, IdAndPlane> optimize(SortedMap<GData3, IdAndPlane> csgResult) {
        if (Double.compare(0.0, Math.abs(epsilon)) == 0) {
            return csgResult;
        }

        final SortedMap<GData3, IdAndPlane> result = new TreeMap<>();

        final SortedMap<VectorCSGd, VectorCSGd> points = new TreeMap<>((v1, v2) -> v1.compareTo(v2, epsilon));

        for (Entry<GData3, IdAndPlane> entry : csgResult.entrySet()) {
            final GData3 key = entry.getKey();
            final IdAndPlane value = entry.getValue();

            VectorCSGd a = points.computeIfAbsent(new VectorCSGd(key.x1, key.y1, key.z1), v -> v);
            VectorCSGd b = points.computeIfAbsent(new VectorCSGd(key.x2, key.y2, key.z2), v -> v);
            VectorCSGd c = points.computeIfAbsent(new VectorCSGd(key.x3, key.y3, key.z3), v -> v);

            if (checkIdenticalVertices(a, b, c)) {
                continue;
            }

            Vertex v1 = new Vertex((float) a.x, (float) a.y, (float) a.z);
            Vertex v2 = new Vertex((float) b.x, (float) b.y, (float) b.z);
            Vertex v3 = new Vertex((float) c.x, (float) c.y, (float) c.z);
            result.put(new GData3(v1, v2, v3, key.parent, new GColour(key.colourNumber, key.r, key.g, key.b, key.a), true), value);
        }

        return result;
    }

    private static boolean checkIdenticalVertices(VectorCSGd a, VectorCSGd b, VectorCSGd c) {
        return a.compareTo(b, epsilon) == 0 || a.compareTo(c, epsilon) == 0 || b.compareTo(c, epsilon) == 0;
    }
}
