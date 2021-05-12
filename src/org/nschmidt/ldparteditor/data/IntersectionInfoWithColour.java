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
import java.util.List;

import org.nschmidt.ldparteditor.helper.math.Vector3dd;

/**
 * @author nils
 *
 */
class IntersectionInfoWithColour {

    private List<Vector3dd> allVertices = new ArrayList<>();
    private List<GColour> colours = new ArrayList<>();
    private List<Integer> isLine = new ArrayList<>();

    IntersectionInfoWithColour(List<GColour> colours, List<Vector3dd> allVertices, List<Integer> resultIsLine) {
        this.isLine.addAll(resultIsLine);
        this.allVertices.addAll(allVertices);
        this.colours.addAll(colours);
    }

    public List<Vector3dd> getAllVertices() {
        return allVertices;
    }

    public void setAllVertices(List<Vector3dd> allVertices) {
        this.allVertices = allVertices;
    }

    public List<GColour> getColours() {
        return colours;
    }

    public void setColours(List<GColour> colours) {
        this.colours = colours;
    }

    public List<Integer> getIsLine() {
        return isLine;
    }

    public void setIsLine(List<Integer> isLine) {
        this.isLine = isLine;
    }
}
