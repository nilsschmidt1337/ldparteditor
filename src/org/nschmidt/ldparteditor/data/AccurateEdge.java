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
public class AccurateEdge {

    public final Vertex v1;
    public final Vertex v2;

    public AccurateEdge(Vertex v1, Vertex v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    @Override
    public int hashCode() {
        return 1337;
    }

    @Override
    public boolean equals(Object obj) {
        AccurateEdge other = (AccurateEdge) obj;
        return
                this.v1.X.compareTo(other.v1.X) == 0 &&
                this.v1.Y.compareTo(other.v1.Y) == 0 &&
                this.v1.Z.compareTo(other.v1.Z) == 0 &&
                this.v2.X.compareTo(other.v2.X) == 0 &&
                this.v2.Y.compareTo(other.v2.Y) == 0 &&
                this.v2.Z.compareTo(other.v2.Z) == 0 ||

                this.v1.X.compareTo(other.v2.X) == 0 &&
                this.v1.Y.compareTo(other.v2.Y) == 0 &&
                this.v1.Z.compareTo(other.v2.Z) == 0 &&
                this.v2.X.compareTo(other.v1.X) == 0 &&
                this.v2.Y.compareTo(other.v1.Y) == 0 &&
                this.v2.Z.compareTo(other.v1.Z) == 0;
    }

    @Override
    public String toString() {
        return v1.toString() + " to " + v2.toString(); //$NON-NLS-1$
    }

}
