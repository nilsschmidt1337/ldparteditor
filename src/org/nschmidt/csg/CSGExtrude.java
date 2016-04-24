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
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GDataCSG;
import org.nschmidt.ldparteditor.data.GDataTEX;
import org.nschmidt.ldparteditor.helpers.composite3d.PathTruderSettings;

public class CSGExtrude extends CSGPrimitive implements Primitive {

    // FIXME Needs implementation for issue #272
    public final int ID = id_counter.getAndIncrement();

    private final GDataCSG start;
    private final ArrayList<GData> cachedData;
    private final PathTruderSettings pts;

    public CSGExtrude(GDataCSG gDataCSG, ArrayList<GData> cachedData2, PathTruderSettings pts2) {
        start = gDataCSG;
        cachedData = cachedData2;
        pts = pts2;
    }

    @Override
    public List<Polygon> toPolygons(GColour colour) {
        List<Polygon> polygons = new ArrayList<Polygon>();

        cachedData.clear();
        fillCache(cachedData, start);

        for (GData g : cachedData) {
            if (g.type() == 9) {
                g = ((GDataTEX) g).getLinkedData();
            }

            // FIXME Needs implementation for issue #272
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
            if (type == 2) {
                cachedData.add(next);
            } else if (type == 9) {
                final GData tex = ((GDataTEX) next).getLinkedData();
                if (tex != null) {
                    final int textype = tex.type();
                    if (textype == 2) {
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
