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
package org.nschmidt.ldparteditor.helper.composite3d;

import java.util.Objects;

import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.helper.math.Vector3d;

class ScreenVertex implements Comparable<ScreenVertex> {

    private final Vertex vertex3d;
    private final Vector3d projection;

    ScreenVertex(Vertex vertex3d, Vector3d projection) {
        this.vertex3d = vertex3d;
        this.projection = projection;
    }

    public Vertex getVertex3D() {
        return vertex3d;
    }

    @Override
    public int compareTo(ScreenVertex o) {
        final int yComparison = this.projection.y.compareTo(o.projection.y);
        if (yComparison == 0) {
            // Compare x instead
            return this.projection.x.compareTo(o.projection.x);
        }

        return yComparison;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.projection.x, this.projection.y);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof ScreenVertex))
            return false;
        ScreenVertex other = (ScreenVertex) obj;
        return this.compareTo(other) == 0;
    }
}
