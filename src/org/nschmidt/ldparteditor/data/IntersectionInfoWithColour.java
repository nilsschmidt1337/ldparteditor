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
package org.nschmidt.ldparteditor.data;

import java.util.ArrayList;

import org.nschmidt.ldparteditor.helpers.math.Vector3dd;

/**
 * @author nils
 *
 */
public class IntersectionInfoWithColour {

    private ArrayList<Vector3dd> allVertices = new ArrayList<>();
    private ArrayList<GColour> colours = new ArrayList<>();
    private ArrayList<Integer> isLine = new ArrayList<>();

    public IntersectionInfoWithColour(ArrayList<GColour> colours, ArrayList<Vector3dd> allVertices, ArrayList<Integer> resultIsLine) {
        this.isLine.addAll(resultIsLine);
        this.allVertices.addAll(allVertices);
        this.colours.addAll(colours);
    }

    public ArrayList<Vector3dd> getAllVertices() {
        return allVertices;
    }

    public void setAllVertices(ArrayList<Vector3dd> allVertices) {
        this.allVertices = allVertices;
    }

    public ArrayList<GColour> getColours() {
        return colours;
    }

    public void setColours(ArrayList<GColour> colours) {
        this.colours = colours;
    }

    public ArrayList<Integer> getIsLine() {
        return isLine;
    }

    public void setIsLine(ArrayList<Integer> isLine) {
        this.isLine = isLine;
    }
}
