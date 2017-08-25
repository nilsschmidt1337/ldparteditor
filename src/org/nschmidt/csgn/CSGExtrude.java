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
package org.nschmidt.csgn;

import java.util.ArrayList;
import java.util.List;

import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GDataCSGN;
import org.nschmidt.ldparteditor.helpers.composite3d.PathTruderSettings;

public class CSGExtrude extends CSGPrimitive implements Primitive {

    public Integer ID;

    public CSGExtrude(GDataCSGN gDataCSGN, ArrayList<GData> cachedData, PathTruderSettings extruderConfig, List<Triangle> triangleCache) {
        // TODO Auto-generated constructor stub
    }

    public static boolean needCacheRefresh(ArrayList<GData> cachedData, GDataCSGN gDataCSGN, DatFile df) {
        // TODO Auto-generated method stub
        return false;
    }

    public static void fillCache(ArrayList<GData> cachedData, GDataCSGN gDataCSGN) {
        // TODO Auto-generated method stub
    }

    @Override
    public CSG toCSG(DatFile df, GColour colour) {
        return null;
    }

    @Override
    public List<Triangle> toTriangles(DatFile df, GColour colour) {
        // TODO Auto-generated method stub
        return null;
    }
}
