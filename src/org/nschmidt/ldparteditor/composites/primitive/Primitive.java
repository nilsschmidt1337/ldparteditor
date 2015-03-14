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
package org.nschmidt.ldparteditor.composites.primitive;

import java.util.ArrayList;

import org.lwjgl.util.vector.Matrix4f;
import org.nschmidt.ldparteditor.data.GData;

public class Primitive {


    private ArrayList<GData> graphicalData = new ArrayList<GData>();
    private ArrayList<Primitive> primitives = new ArrayList<Primitive>();
    private ArrayList<Primitive> primitivesExtended = new ArrayList<Primitive>();
    private boolean extended = false;
    private boolean category = false;

    public Primitive() {
        primitives.add(this);
        primitivesExtended.add(this);
    }

    public Primitive(boolean category) {
        primitives.add(this);
        primitivesExtended.add(this);
        primitivesExtended.add(new Primitive());
        setCategory(true);
    }

    public void toggle() {
        if (isCategory()) {
            this.extended = !this.extended;
        }
    }

    public ArrayList<Primitive> getPrimitives() {
        if (isExtended()) {
            return primitivesExtended;
        } else {
            return primitives;
        }
    }

    public void draw(float x, float y, Matrix4f rotation) {

    }

    public void setPrimitives(ArrayList<Primitive> primitives) {
        this.primitives = primitives;
    }

    public boolean isExtended() {
        return extended;
    }

    public void setExtended(boolean extended) {
        this.extended = extended;
    }

    public boolean isCategory() {
        return category;
    }

    public void setCategory(boolean category) {
        this.category = category;
    }

    public ArrayList<GData> getGraphicalData() {
        return graphicalData;
    }

    public void setGraphicalData(ArrayList<GData> graphicalData) {
        this.graphicalData = graphicalData;
    }
}
