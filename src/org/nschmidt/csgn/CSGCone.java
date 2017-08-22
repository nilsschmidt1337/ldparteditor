package org.nschmidt.csgn;

import java.util.List;

import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;

public class CSGCone extends CSGPrimitive implements Primitive {

    public Integer ID;

    public CSGCone(int quality) {
        // TODO Auto-generated constructor stub
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
