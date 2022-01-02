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
import java.util.List;

import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GColourIndex;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GData3;
import org.nschmidt.ldparteditor.data.GData4;
import org.nschmidt.ldparteditor.data.GDataCSG;
import org.nschmidt.ldparteditor.data.GDataTEX;

public class CSGMesh implements Primitive {

    public final int id = idCounter.getAndIncrement();

    private final GDataCSG start;
    private final List<GData> cachedData;
    private final List<Polygon> polygonCache;

    public CSGMesh(GDataCSG gDataCSG, List<GData> cachedData2, List<Polygon> polygonCache2) {
        start = gDataCSG;
        cachedData = cachedData2;
        polygonCache = polygonCache2;
    }

    @Override
    public List<Polygon> toPolygons(DatFile df, GColour colour) {
        List<Polygon> polygons = new ArrayList<>();

        if (!needCacheRefresh(cachedData, start, df) && !polygonCache.isEmpty()) {
            for (Polygon p : polygonCache) {
                GColourIndex i = p.getColour();
                p.setColour(new GColourIndex(i.colour(), id));
            }
            return polygonCache;
        }

        cachedData.clear();
        fillCache(cachedData, start);

        // FIXME Needs winding check!
        for (GData g : cachedData) {
            if (g.type() == 9) {
                g = ((GDataTEX) g).getLinkedData();
            }
            if (g.type() == 3) {
                GData3 g3 = (GData3) g;
                GColour colour2 = colour;
                if (g3.colourNumber != 16) {
                    colour2 = new GColour(g3.colourNumber, g3.r, g3.g, g3.b, g3.a);
                }

                Polygon p1 = new Polygon(
                        df,
                        new VectorCSGd(g3.x1, g3.y1, g3.z1),
                        new VectorCSGd(g3.x2, g3.y2, g3.z2), new VectorCSGd(g3.x3, g3.y3, g3.z3)
                        );
                p1.setColour(new GColourIndex(colour2, id));
                polygons.add(p1);
            } else {
                GData4 g4 = (GData4) g;
                GColour colour2 = colour;
                if (g4.colourNumber != 16) {
                    colour2 = new GColour(g4.colourNumber, g4.r, g4.g, g4.b, g4.a);
                }
                Polygon p1 = new Polygon(
                        df,
                        new VectorCSGd(g4.x1, g4.y1, g4.z1),
                        new VectorCSGd(g4.x2, g4.y2, g4.z2), new VectorCSGd(g4.x3, g4.y3, g4.z3)
                        );
                Polygon p2 = new Polygon(
                        df,
                        new VectorCSGd(g4.x3, g4.y3, g4.z3),
                        new VectorCSGd(g4.x4, g4.y4, g4.z4), new VectorCSGd(g4.x1, g4.y1, g4.z1)
                        );
                p1.setColour(new GColourIndex(colour2, id));
                p2.setColour(new GColourIndex(colour2, id));
                polygons.add(p1);
                polygons.add(p2);
            }
        }
        polygonCache.clear();
        polygonCache.addAll(polygons);
        return polygons;
    }

    public static void fillCache(List<GData> cachedData, GData start) {
        if (cachedData.isEmpty()) {
            GData next = start;
            while ((next = next.getNext()) != null && next.type() == 8) {

            }
            if (next == null) {
                return;
            }
            next = next.getBefore();
            while ((next = next.getNext()) != null) {
                final int type = next.type();
                if (type == 0) continue;
                if (type > 2 && type < 5) {
                    cachedData.add(next);
                } else if (type == 9) {
                    final GData tex = ((GDataTEX) next).getLinkedData();
                    if (tex != null) {
                        final int textype = tex.type();
                        if (textype > 2 && textype < 5) {
                            cachedData.add(next);
                        }
                    }
                } else {
                    break;
                }
            }
        }
    }

    public static boolean needCacheRefresh(List<GData> cachedData2, GData start, DatFile df) {
        List<GData> cachedData = new ArrayList<>();
        GData next = start;
        while ((next = next.getNext()) != null && next.type() == 8) {

        }
        if (next == null) {
            return false;
        }
        next = next.getBefore();
        while ((next = next.getNext()) != null) {
            final int type = next.type();
            if (type == 0) continue;
            if (type > 2 && type < 5) {
                cachedData.add(next);
            } else if (type == 9) {
                final GData tex = ((GDataTEX) next).getLinkedData();
                if (tex != null) {
                    final int textype = tex.type();
                    if (textype > 2 && textype < 5) {
                        cachedData.add(next);
                    }
                }
            } else {
                break;
            }
        }

        final int size = cachedData.size();
        if (size != cachedData2.size()) {
            GDataCSG.resetCSG(df, false);
            GDataCSG.forceRecompile(df);
            return true;
        }

        for (int i = 0; i < size; i++) {
            if (cachedData.get(i) != cachedData2.get(i)) {
                GDataCSG.resetCSG(df, false);
                GDataCSG.forceRecompile(df);
                return true;
            }
        }

        return false;
    }

    @Override
    public CSG toCSG(DatFile df, GColour colour) {
        return CSG.fromPolygons(toPolygons(df, colour));
    }
}
