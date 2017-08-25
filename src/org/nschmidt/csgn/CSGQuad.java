package org.nschmidt.csgn;

import java.util.ArrayList;
import java.util.List;

import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GColourIndex;

public class CSGQuad extends CSGPrimitive implements Primitive {

    public final int ID = id_counter.getAndIncrement();

    @Override
    public List<Triangle> toTriangles(DatFile df, GColour colour) {

        List<Triangle> triangles = new ArrayList<>();
        // position // normal
        int[][] info = { { 2, 6, 7, 3 }, { 0, 1, 0 } } ;

        List<Vector3d> vertices = new ArrayList<Vector3d>();
        for (int i : info[0]) {
            vertices.add(new Vector3d(
                    2000d * (1 * Math.min(1, i & 1) - 0.5),
                    (Math.min(1, i & 2) - 0.5),
                    2000d * (Math.min(1, i & 4) - 0.5)));
        }
        triangles.add(new Triangle(df, vertices.get(0), vertices.get(1), vertices.get(2), new GColourIndex(colour, ID)));
        triangles.add(new Triangle(df, vertices.get(2), vertices.get(3), vertices.get(0), new GColourIndex(colour, ID)));

        return triangles;
    }

    @Override
    public CSG toCSG(DatFile df, GColour colour) {
        return CSG.fromTriangles(toTriangles(df, colour));
    }
}
