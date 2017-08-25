package org.nschmidt.csgn;

import java.util.ArrayList;
import java.util.List;

import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GColourIndex;

public class CSGCube extends CSGPrimitive implements Primitive {

    public final int ID = id_counter.getAndIncrement();

    @Override
    public List<Triangle> toTriangles(DatFile df, GColour colour) {

        int[][][] a = {
                // position // normal
                { { 0, 4, 6, 2 }, { -1, 0, 0 } }, { { 1, 3, 7, 5 }, { +1, 0, 0 } }, { { 0, 1, 5, 4 }, { 0, -1, 0 } }, { { 2, 6, 7, 3 }, { 0, +1, 0 } }, { { 0, 2, 3, 1 }, { 0, 0, -1 } },
                { { 4, 5, 7, 6 }, { 0, 0, +1 } } };
        List<Triangle> triangles = new ArrayList<Triangle>();
        for (int[][] info : a) {
            List<Vector3d> vertices = new ArrayList<Vector3d>();
            for (int i : info[0]) {
                Vector3d pos = new Vector3d(2000d * (Math.min(1, i & 1) - 0.5), 2000d * (Math.min(1, i & 2) - 0.5), 2000d
                        * (Math.min(1, i & 4) - 0.5));
                vertices.add(pos);
            }
            triangles.add(new Triangle(df, vertices.get(0), vertices.get(1), vertices.get(2), new GColourIndex(colour, ID)));
            triangles.add(new Triangle(df, vertices.get(2), vertices.get(3), vertices.get(0), new GColourIndex(colour, ID)));
        }

        return triangles;
    }

    @Override
    public CSG toCSG(DatFile df, GColour colour) {
        return CSG.fromTriangles(toTriangles(df, colour));
    }
}
