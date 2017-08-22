package org.nschmidt.csgn;

import java.util.List;

import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;

public class CSGSphere extends CSGPrimitive implements Primitive {

    public Integer ID;

    public CSGSphere(int quality, int i) {
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
