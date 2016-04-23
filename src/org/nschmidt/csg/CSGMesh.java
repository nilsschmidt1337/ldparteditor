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

public class CSGMesh extends CSGPrimitive implements Primitive {

    public final int ID = id_counter.getAndIncrement();

    private final GDataCSG start;
    private final ArrayList<GData> cachedData;

    public CSGMesh(GDataCSG gDataCSG, ArrayList<GData> cachedData2) {
        start = gDataCSG;
        cachedData = cachedData2;
    }

    @Override
    public List<Polygon> toPolygons(GColour colour) {
        List<Polygon> polygons = new ArrayList<Polygon>();

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
                        new Vertex(new Vector3d(g3.x1, g3.y1, g3.z1), new Vector3d(g3.xn, g3.yn, g3.zn)),
                        new Vertex(new Vector3d(g3.x2, g3.y2, g3.z2), new Vector3d(g3.xn, g3.yn, g3.zn)),
                        new Vertex(new Vector3d(g3.x3, g3.y3, g3.z3), new Vector3d(g3.xn, g3.yn, g3.zn))
                        );
                p1.getShared().set("colour", new GColourIndex(colour2, ID)); //$NON-NLS-1$
                polygons.add(p1);
            } else {
                GData4 g4 = (GData4) g;
                GColour colour2 = colour;
                if (g4.colourNumber != 16) {
                    colour2 = new GColour(g4.colourNumber, g4.r, g4.g, g4.b, g4.a);
                }
                Polygon p1 = new Polygon(
                        new Vertex(new Vector3d(g4.x1, g4.y1, g4.z1), new Vector3d(g4.xn, g4.yn, g4.zn)),
                        new Vertex(new Vector3d(g4.x2, g4.y2, g4.z2), new Vector3d(g4.xn, g4.yn, g4.zn)),
                        new Vertex(new Vector3d(g4.x3, g4.y3, g4.z3), new Vector3d(g4.xn, g4.yn, g4.zn))
                        );
                Polygon p2 = new Polygon(
                        new Vertex(new Vector3d(g4.x3, g4.y3, g4.z3), new Vector3d(g4.xn, g4.yn, g4.zn)),
                        new Vertex(new Vector3d(g4.x4, g4.y4, g4.z4), new Vector3d(g4.xn, g4.yn, g4.zn)),
                        new Vertex(new Vector3d(g4.x1, g4.y1, g4.z1), new Vector3d(g4.xn, g4.yn, g4.zn))
                        );
                p1.getShared().set("colour", new GColourIndex(colour2, ID)); //$NON-NLS-1$
                p2.getShared().set("colour", new GColourIndex(colour2, ID)); //$NON-NLS-1$
                polygons.add(p1);
                polygons.add(p2);
            }
        }

        return polygons;
    }

    public static void fillCache(ArrayList<GData> cachedData, GData start) {
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

    public static boolean needCacheRefresh(ArrayList<GData> cachedData2, GData start, DatFile df) {
        ArrayList<GData> cachedData = new ArrayList<GData>();
        GData next = start;
        while ((next = next.getNext()) != null && next.type() == 8) {

        }
        if (next == null) {
            return false;
        }
        next = next.getBefore();
        while ((next = next.getNext()) != null) {
            final int type = next.type();
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
    public CSG toCSG(GColour colour) {
        return CSG.fromPolygons(toPolygons(colour));
    }
}
