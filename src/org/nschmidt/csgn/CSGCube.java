package org.nschmidt.csgn;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GColourIndex;

public class CSGCube extends CSGPrimitive implements Primitive {

    public final int ID = id_counter.getAndIncrement();

    /**
     * Center of this cube.
     */
    private Vector3d center;
    /**
     * Cube dimensions.
     */
    private Vector3d dimensions;

    private boolean centered = true;

    /**
     * Constructor. Creates a new cube with center {@code [0,0,0]} and
     * dimensions {@code [1,1,1]}.
     */
    public CSGCube() {
        center = new Vector3d(0d, 0d, 0d);
        dimensions = new Vector3d(2000d, 2000d, 2000d);
    }

    /**
     * Constructor. Creates a new cube with center {@code [0,0,0]} and
     * dimensions {@code [size,size,size]}.
     *
     * @param size
     *            size
     */
    public CSGCube(double size) {
        center = new Vector3d(0, 0, 0);
        dimensions = new Vector3d(size, size, size);
    }

    /**
     * Constructor. Creates a new cuboid with the specified center and
     * dimensions.
     *
     * @param center
     *            center of the cuboid
     * @param dimensions
     *            cube dimensions
     */
    public CSGCube(Vector3d center, Vector3d dimensions) {
        this.center = center;
        this.dimensions = dimensions;
    }

    /**
     * Constructor. Creates a new cuboid with center {@code [0,0,0]} and with
     * the specified dimensions.
     *
     * @param w
     *            width
     * @param h
     *            height
     * @param d
     *            depth
     */
    public CSGCube(double w, double h, double d) {
        this(Vector3d.ZERO, new Vector3d(w, h, d));
    }

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
                Vector3d pos = new Vector3d(center.x + dimensions.x * (1 * Math.min(1, i & 1) - 0.5), center.y + dimensions.y * (1 * Math.min(1, i & 2) - 0.5), center.z + dimensions.z
                        * (1 * Math.min(1, i & 4) - 0.5));
                vertices.add(pos);
            }
            triangles.add(new Triangle(df, vertices.get(0), vertices.get(1), vertices.get(2), new GColourIndex(colour, ID)));
            triangles.add(new Triangle(df, vertices.get(2), vertices.get(3), vertices.get(0), new GColourIndex(colour, ID)));
        }

        if (!centered) {

            Transform centerTransform = Transform.unity().apply(
                    Matrix4f.setIdentity(new Matrix4f()).translate(new Vector3f((float) dimensions.x / 2f, (float) dimensions.y / 2f, (float) dimensions.z / 2f)));

            for (Triangle p : triangles) {
                p.transform(centerTransform);
            }
        }

        return triangles;
    }

    /**
     * @return the center
     */
    public Vector3d getCenter() {
        return center;
    }

    /**
     * @param center
     *            the center to set
     */
    public void setCenter(Vector3d center) {
        this.center = center;
    }

    /**
     * @return the dimensions
     */
    public Vector3d getDimensions() {
        return dimensions;
    }

    /**
     * @param dimensions
     *            the dimensions to set
     */
    public void setDimensions(Vector3d dimensions) {
        this.dimensions = dimensions;
    }

    public CSGCube noCenter() {
        centered = false;
        return this;
    }

    @Override
    public CSG toCSG(DatFile df, GColour colour) {
        return CSG.fromTriangles(toTriangles(df, colour));
    }
}
