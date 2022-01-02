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

/**
 * @author nils
 *
 */
class AccurateEdge {

    final Vertex v1;
    final Vertex v2;

    AccurateEdge(Vertex v1, Vertex v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    @Override
    public int hashCode() {
        return 1337;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AccurateEdge)) {
            return false;
        }

        AccurateEdge other = (AccurateEdge) obj;
        return
                this.v1.xp.compareTo(other.v1.xp) == 0 &&
                this.v1.yp.compareTo(other.v1.yp) == 0 &&
                this.v1.zp.compareTo(other.v1.zp) == 0 &&
                this.v2.xp.compareTo(other.v2.xp) == 0 &&
                this.v2.yp.compareTo(other.v2.yp) == 0 &&
                this.v2.zp.compareTo(other.v2.zp) == 0 ||

                this.v1.xp.compareTo(other.v2.xp) == 0 &&
                this.v1.yp.compareTo(other.v2.yp) == 0 &&
                this.v1.zp.compareTo(other.v2.zp) == 0 &&
                this.v2.xp.compareTo(other.v1.xp) == 0 &&
                this.v2.yp.compareTo(other.v1.yp) == 0 &&
                this.v2.zp.compareTo(other.v1.zp) == 0;
    }

    @Override
    public String toString() {
        return v1.toString() + " to " + v2.toString(); //$NON-NLS-1$
    }
}
