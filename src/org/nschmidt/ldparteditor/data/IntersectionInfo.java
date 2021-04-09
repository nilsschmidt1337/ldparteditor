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

import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.helpers.math.Vector3r;

/**
 * @author nils
 *
 */
class IntersectionInfo {

    private GData target;
    private GData origin;

    private ArrayList<Vector3d> allVertices = new ArrayList<>();

    IntersectionInfo(GData target2, GData origin2, ArrayList<Vector3r> allVertices) {
        for (Vector3r v : allVertices) {
            this.allVertices.add(new Vector3d(v.x.BigDecimalValue(), v.y.BigDecimalValue(), v.z.BigDecimalValue()));
        }
        this.target = target2;
        this.origin = origin2;
    }

    public GData getTarget() {
        return target;
    }


    public void setTarget(GData target) {
        this.target = target;
    }


    public GData getOrigin() {
        return origin;
    }


    public void setOrigin(GData origin) {
        this.origin = origin;
    }


    public ArrayList<Vector3d> getAllVertices() {
        return allVertices;
    }


    public void setAllVertices(ArrayList<Vector3d> allVertices) {
        this.allVertices = allVertices;
    }
}
