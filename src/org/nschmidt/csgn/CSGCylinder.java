package org.nschmidt.csgn;

import java.util.ArrayList;
import java.util.List;

import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GColourIndex;

public class CSGCylinder extends CSGPrimitive implements Primitive {

    public final int ID = id_counter.getAndIncrement();

    private Vector3d start;
    private Vector3d end;
    private double radius;
    private int numSlices;

    /**
     * Constructor. Creates a new cylinder with center {@code [0,0,0]} and
     * ranging from {@code [0,-0.5,0]} to {@code [0,0.5,0]}, i.e.
     * {@code size = 1}.
     */
    public CSGCylinder() {
        this.start = new Vector3d(0, 0d, 0);
        this.end = new Vector3d(0, 1000d, 0);
        this.radius = 1000d;
        this.numSlices = 16;
    }

    /**
     * Constructor. Creates a cylinder . The resolution of the tessellation can
     * be controlled with {@code numSlices}.
     *
     * @param numSlices
     *            number of slices (used for tessellation)
     */
    public CSGCylinder(int numSlices) {
        this.start = new Vector3d(0, 0d, 0);
        this.end = new Vector3d(0, 1000d, 0);
        this.radius = 1000d;
        this.numSlices = numSlices;
    }

    /**
     * Constructor. Creates a cylinder ranging from {@code [0,0,0]} to
     * {@code [0,0,height]} with the specified {@code radius} and {@code height}
     * . The resolution of the tessellation can be controlled with
     * {@code numSlices}.
     *
     * @param radius
     *            cylinder radius
     * @param height
     *            cylinder height
     * @param numSlices
     *            number of slices (used for tessellation)
     */
    public CSGCylinder(double radius, double height, int numSlices) {
        this.start = Vector3d.ZERO;
        this.end = Vector3d.Z_ONE.times(height);
        this.radius = radius;
        this.numSlices = numSlices;
    }

    @Override
    public List<Triangle> toTriangles(DatFile df, GColour colour) {
        final Vector3d s = getStart();
        Vector3d e = getEnd();
        final Vector3d ray = e.minus(s);
        final Vector3d axisZ = ray.unit();
        boolean isY = Math.abs(axisZ.y) > 0.5;
        final Vector3d axisX = new Vector3d(isY ? 1 : 0, !isY ? 1 : 0, 0).cross(axisZ).unit();
        final Vector3d axisY = axisX.cross(axisZ).unit();
        Vector3d startV = s.clone();
        Vector3d endV = e.clone();
        List<Triangle> polygons = new ArrayList<>();

        for (int i = 0; i < numSlices; i++) {
            double t0 = i / (double) numSlices, t1 = (i + 1) / (double) numSlices;
            {
                Vector3d v1 = startV;
                Vector3d v2 = cylPoint(axisX, axisY, axisZ, ray, s, radius, 0, t0, -1);
                Vector3d v3 = cylPoint(axisX, axisY, axisZ, ray, s, radius, 0, t1, -1);
                polygons.add(new Triangle(df, v1, v2, v3, new GColourIndex(colour, ID)));
            }
            {
                Vector3d v1 = cylPoint(axisX, axisY, axisZ, ray, s, radius, 0, t1, 0);
                Vector3d v2 = cylPoint(axisX, axisY, axisZ, ray, s, radius, 0, t0, 0);
                Vector3d v3 = cylPoint(axisX, axisY, axisZ, ray, s, radius, 1, t0, 0);
                Vector3d v4 = cylPoint(axisX, axisY, axisZ, ray, s, radius, 1, t1, 0);
                polygons.add(new Triangle(df, v1, v2, v3, new GColourIndex(colour, ID)));
                polygons.add(new Triangle(df, v3, v4, v1, new GColourIndex(colour, ID)));
            }
            {
                Vector3d v1 = endV;
                Vector3d v2 = cylPoint(axisX, axisY, axisZ, ray, s, radius, 1, t1, 1);
                Vector3d v3 =  cylPoint(axisX, axisY, axisZ, ray, s, radius, 1, t0, 1);
                polygons.add(new Triangle(df, v1, v2, v3, new GColourIndex(colour, ID)));
            }
        }

        return polygons;
    }

    private Vector3d cylPoint(Vector3d axisX, Vector3d axisY, Vector3d axisZ, Vector3d ray, Vector3d s, double r, double stack, double slice, double normalBlend) {
        double angle = slice * Math.PI * 2;
        Vector3d out = axisX.times(Math.cos(angle)).plus(axisY.times(Math.sin(angle)));
        Vector3d pos = s.plus(ray.times(stack)).plus(out.times(r));
        return pos;
    }

    /**
     * @return the start
     */
    public Vector3d getStart() {
        return start;
    }

    /**
     * @param start
     *            the start to set
     */
    public void setStart(Vector3d start) {
        this.start = start;
    }

    /**
     * @return the end
     */
    public Vector3d getEnd() {
        return end;
    }

    /**
     * @param end
     *            the end to set
     */
    public void setEnd(Vector3d end) {
        this.end = end;
    }

    /**
     * @return the radius
     */
    public double getRadius() {
        return radius;
    }

    /**
     * @param radius
     *            the radius to set
     */
    public void setRadius(double radius) {
        this.radius = radius;
    }

    /**
     * @return the number of slices
     */
    public int getNumSlices() {
        return numSlices;
    }

    /**
     * @param numSlices
     *            the number of slices to set
     */
    public void setNumSlices(int numSlices) {
        this.numSlices = numSlices;
    }

    @Override
    public CSG toCSG(DatFile df, GColour colour) {
        return CSG.fromTriangles(toTriangles(df, colour));
    }
}
