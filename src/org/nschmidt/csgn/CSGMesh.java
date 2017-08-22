package org.nschmidt.csgn;

import java.util.ArrayList;
import java.util.List;

import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GDataCSGN;

public class CSGMesh extends CSGPrimitive implements Primitive {

    public Integer ID;

    public CSGMesh(GDataCSGN gDataCSGN, ArrayList<GData> cachedData, List<Triangle> triangleCache) {
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Triangle> toTriangles(DatFile df, GColour colour) {
        // TODO Auto-generated method stub
        return null;
    }

}
