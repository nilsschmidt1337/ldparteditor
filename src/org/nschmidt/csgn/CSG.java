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
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

import org.lwjgl.util.vector.Matrix4f;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.GData3;
import org.nschmidt.ldparteditor.enums.View;

public class CSG {

    TreeMap<GData3, Integer> result = new TreeMap<GData3, Integer>();

    private List<Triangle> triangles = new ArrayList<>();

    private CSG() {
    }

    public static final byte UNION = 0;
    public static final byte DIFFERENCE = 1;
    public static final byte INTERSECTION = 2;
    public static final byte CUBOID = 3;
    public static final byte ELLIPSOID = 4;
    public static final byte QUAD = 5;
    public static final byte CYLINDER = 6;
    public static final byte CIRCLE = 7;
    public static final byte COMPILE = 8;
    public static final byte QUALITY = 9;
    public static final byte EPSILON = 10;
    public static final byte CONE = 11;
    public static final byte TRANSFORM = 12;
    public static final byte MESH = 13;
    public static final byte EXTRUDE = 14;
    public static final byte EXTRUDE_CFG = 15;


    /**
     * Returns a transformed copy of this CSG.
     *
     * @param transform
     *            the transform to apply
     *
     * @return a transformed copy of this CSG
     */
    public CSG transformed(Matrix4f transform) {
        return transformed(new Transform().apply(transform));
    }

    /**
     * Returns a transformed copy of this CSG.
     *
     * @param transform
     *            the transform to apply
     *
     * @return a transformed copy of this CSG
     */
    public CSG transformed(Transform transform) {
        List<Triangle> newtriangles = new ArrayList<>();
        for (Triangle t : triangles) {
            newtriangles.add(t.transformed(transform));
        }
        CSG result = CSG.fromTriangles(newtriangles);
        return result;
    }

    /**
     * Returns a transformed copy of this CSG.
     *
     * @param transform
     *            the transform to apply
     * @param ID
     *
     * @return a transformed copy of this CSG
     */
    public CSG transformed(Transform transform, GColour c, int ID) {
        List<Triangle> newtriangles = new ArrayList<>();
        for (Triangle t : triangles) {
            newtriangles.add(t.transformed(transform, c, ID));
        }
        CSG result = CSG.fromTriangles(newtriangles);
        return result;
    }

    public TreeMap<GData3, Integer> getResult() {
        return result;
    }

    public List<Triangle> getTriangles() {
        return triangles;
    }

    public void draw(Composite3D c3d) {
        // TODO Auto-generated method stub

    }

    public void draw_textured(Composite3D c3d) {
        // TODO Auto-generated method stub

    }

    public CSG difference(CSG csg) {
        // TODO Auto-generated method stub
        return null;
    }

    public CSG intersect(CSG csg) {
        // TODO Auto-generated method stub
        return null;
    }

    public CSG transformed(Matrix4f matrix, GColour colour, int iD) {
        // TODO Auto-generated method stub
        return null;
    }

    public CSG union(CSG csg) {
        // TODO Auto-generated method stub
        List<Triangle> newtriangles = new ArrayList<>();
        newtriangles.addAll(triangles);
        newtriangles.addAll(csg.triangles);
        return CSG.fromTriangles(newtriangles);
    }

    /**
     * Returns this csg as list of LDraw triangles
     *
     * @return this csg as list of LDraw triangles
     */
    public TreeMap<GData3, Integer> toLDrawTriangles(GData1 parent) {
        TreeMap<GData3, Integer> result = new TreeMap<GData3, Integer>();
        for (Triangle p : this.triangles) {
            result.putAll(p.toLDrawTriangle(parent));
        }
        return result;
    }

    public GData1 compile() {
        // TODO Auto-generated method stub
        Matrix4f id = new Matrix4f();
        Matrix4f.setIdentity(id);
        GColour col = View.getLDConfigColour(16);
        GData1 g1 = new GData1(-1, col.getR(), col.getG(), col.getB(), 1f, id, View.ACCURATE_ID, new ArrayList<String>(), null, null, 1, false, id, View.ACCURATE_ID, null, View.DUMMY_REFERENCE, true, false,
                new HashSet<String>(), View.DUMMY_REFERENCE);
        this.result = toLDrawTriangles(g1);
        return g1;
    }

    public void compile_without_t_junctions(DatFile df) {
        // TODO Auto-generated method stub
        compile();
    }

    public static CSG fromTriangles(List<Triangle> triangles) {
        CSG csg = new CSG();
        csg.triangles = triangles;
        return csg;
    }
}
