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
class VertexManifestation {

    private final int position;
    private final GData gdata;

    VertexManifestation(int position, GData gdata) {
        this.position = position;
        this.gdata = gdata;
    }

    /**
     * @return the position from the vertex in the LDraw line (0-3)
     */
    public int getPosition() {
        return position;
    }

    /**
     * @return the linked LDraw line
     */
    public GData getGdata() {
        return gdata;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (gdata == null ? 0 : gdata.hashCode());
        result = prime * result + position;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VertexManifestation other = (VertexManifestation) obj;
        if (gdata == null) {
            if (other.gdata != null)
                return false;
        } else if (!gdata.equals(other.gdata))
            return false;
        if (position != other.position)
            return false;
        return true;
    }

}
