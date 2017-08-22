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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GColourIndex;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.GData3;
import org.nschmidt.ldparteditor.enums.View;

public class Triangle {

    /**
     * Triangle vertices
     */
    public List<Vector3d> vertices = new ArrayList<>();
    /**
     * The linked DatFile
     */
    final DatFile df;

    private GColourIndex colour = new GColourIndex(new GColour(-1, 0f, 0f, 0f, 1f), 0);

    /**
     * Plane defined by this triangle.
     */
    public final Plane plane;

    public Triangle(DatFile df, Vector3d v1, Vector3d v2, Vector3d v3, GColourIndex colour) {
        this.df = df;
        this.vertices.add(v1);
        this.vertices.add(v2);
        this.vertices.add(v3);
        this.colour = colour;
        this.plane = Plane.createFromPoints(v1, v2, v3);
    }

    @Override
    public Triangle clone() {
        List<Vector3d> newVertices = new ArrayList<Vector3d>(vertices.size());
        for (Vector3d vertex : vertices) {
            newVertices.add(vertex.clone());
        }
        return new Triangle(df, newVertices.get(0), newVertices.get(1), newVertices.get(2), new GColourIndex(colour.getColour(), colour.getIndex()));
    }

    public Triangle transform(Transform transform) {

        for (Vector3d v : vertices) {
            transform.transform(v);
        }

        if (transform.isMirror()) {
            // the transformation includes mirroring. flip polygon
            flip();
        }
        return this;
    }

    public Triangle transformed(Transform transform) {
        return clone().transform(transform);
    }

    public Triangle transformed(Transform transform, GColour c, int ID) {
        Triangle result = clone().transform(transform);
        GColourIndex colour = null;
        if ((colour = this.getColour()) != null) {
            GColour c2;
            if ((c2 = colour.getColour()) != null) {
                if (c2.getColourNumber() == 16) {
                    result.setColour(new GColourIndex(c.clone(), ID));
                } else {
                    result.setColour(new GColourIndex(c2.clone(), ID));
                }
            }
        }
        return result;
    }

    public Triangle flip() {

        Collections.reverse(vertices);
        plane.flip();

        return this;
    }

    public HashMap<GData3, Integer> toLDrawTriangle(GData1 parent) {
        HashMap<GData3, Integer> result = new HashMap<GData3, Integer>();
        if (this.vertices.size() >= 3) {
            int dID = CSGPrimitive.id_counter.getAndIncrement();
            final GColour c16 = View.getLDConfigColour(16);
            for (int i = 0; i < this.vertices.size() - 2; i++) {
                org.nschmidt.ldparteditor.data.Vertex v1 = new org.nschmidt.ldparteditor.data.Vertex((float) this.vertices.get(0).x, (float) this.vertices.get(0).y,
                        (float) this.vertices.get(0).z);
                org.nschmidt.ldparteditor.data.Vertex v2 = new org.nschmidt.ldparteditor.data.Vertex((float) this.vertices.get(i + 1).x, (float) this.vertices.get(i + 1).y,
                        (float) this.vertices.get(i + 1).z);
                org.nschmidt.ldparteditor.data.Vertex v3 = new org.nschmidt.ldparteditor.data.Vertex((float) this.vertices.get(i + 2).x, (float) this.vertices.get(i + 2).y,
                        (float) this.vertices.get(i + 2).z);
                GColourIndex colour = null;
                if ((colour = this.colour) == null) {
                    result.put(new GData3(v1, v2, v3, parent, c16, true), dID);
                } else {
                    result.put(new GData3(v1, v2, v3, parent, colour.getColour(), true), colour.getIndex());
                }
            }
        }
        return result;
    }

    public GColourIndex getColour() {
        return colour;
    }

    public void setColour(GColourIndex colour) {
        this.colour = colour;
    }
}
