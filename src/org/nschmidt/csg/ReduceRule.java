/* MIT - License

Copyright (c) 2015 - this year, Nils Schmidt

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package org.nschmidt.csg;

import java.util.Arrays;

/**
 * @author nils
 *
 */
public class ReduceRule implements Comparable<ReduceRule> {

    final int planes;
    final int adjacency;
    final int[] lengths;
    final int[] angles;

    public ReduceRule(int planes, int adjacency, int[] lengths, int[] angles) {
        this.planes = planes;
        this.adjacency = adjacency;
        this.lengths = lengths;
        this.angles = angles;
    }

    @Override
    public int compareTo(ReduceRule o) {
        // TODO Auto-generated method stub
        if (this.planes != o.planes) {
            return Integer.compare(this.planes, o.planes);
        }
        if (this.adjacency != o.adjacency) {
            return Integer.compare(this.adjacency, o.adjacency);
        }

        for (int i=0; i< adjacency; i++) {
            int delta = Math.abs(angles[i] - o.angles[i]);
            int delta2 = Math.abs(delta);
            if (delta2 > 2) {
                return Integer.compare(delta, 0);
            }
        }

        for (int i=0; i< adjacency; i++) {
            int delta = Math.abs(lengths[i] - o.lengths[i]);
            int delta2 = Math.abs(delta);
            if (delta2 > 5) {
                return Integer.compare(delta, 0);
            }
        }

        return 0;
    }

    @Override
    public int hashCode() {
        return 1337;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ReduceRule other = (ReduceRule) obj;
        if (adjacency != other.adjacency || planes != other.planes)
            return false;

        if (!Arrays.equals(angles, other.angles))
            return false;
        if (!Arrays.equals(lengths, other.lengths))
            return false;


        return true;
    }

}
