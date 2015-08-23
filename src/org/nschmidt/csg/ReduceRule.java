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

import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Locale;

/**
 * @author nils
 *
 */
public class ReduceRule implements Comparable<ReduceRule> {

    private static final java.text.DecimalFormat NUMBER_FORMAT0F = new java.text.DecimalFormat("###,##000;-###,##000", new DecimalFormatSymbols(Locale.getDefault())); //$NON-NLS-1$


    final int adjacency;
    final int[] lengths;
    final int[] angles;

    public ReduceRule(int adjacency, int[] lengths, int[] angles) {
        this.adjacency = adjacency;
        this.lengths = lengths;
        this.angles = angles;
    }

    @Override
    public int compareTo(ReduceRule o) {
        if (this.adjacency != o.adjacency) {
            return Integer.compare(this.adjacency, o.adjacency);
        }

        boolean tryReverse = false;

        for (int i=0; i< adjacency; i++) {
            int delta = Math.abs(angles[i] - o.angles[i]);
            if (delta > 1) { // 2
                tryReverse = true;
                break;
            }
        }

        if (!tryReverse) {
            for (int i=0; i< adjacency; i++) {
                int delta = Math.abs(lengths[i] - o.lengths[i]);
                if (delta > 1) { // 5
                    tryReverse = true;
                    break;
                }
            }
        }

        if (tryReverse) {

            for (int i=0; i < adjacency; i++) {
                int delta = angles[adjacency - i - 1] - o.angles[i];
                int delta2 = Math.abs(delta);
                if (delta2 > 1) { // 2
                    return Integer.compare(delta, 0);
                }
            }

            {
                int delta = lengths[0] - o.lengths[0];
                int delta2 = Math.abs(delta);
                if (delta2 > 1) { // 5
                    return Integer.compare(delta, 0);
                }
            }
            for (int i=1; i < adjacency; i++) {
                int delta = lengths[adjacency - i] - o.lengths[i];
                int delta2 = Math.abs(delta);
                if (delta2 > 1) { // 5
                    return Integer.compare(delta, 0);
                }
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
        if (adjacency != other.adjacency)
            return false;

        if (!Arrays.equals(angles, other.angles))
            return false;
        if (!Arrays.equals(lengths, other.lengths))
            return false;


        return true;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append(adjacency + "|"); //$NON-NLS-1$
        for (int i = 0; i < adjacency; i++) {
            sb.append(NUMBER_FORMAT0F.format(lengths[i]) + "|");//$NON-NLS-1$
        }
        for (int i = 0; i < adjacency; i++) {
            sb.append(NUMBER_FORMAT0F.format(angles[i]) + "|");//$NON-NLS-1$
        }

        return sb.toString();
    }

    public String toStringShort() {
        final StringBuilder sb = new StringBuilder();
        final int lastIndex = adjacency - 1;
        for (int i = 0; i < adjacency; i++) {
            sb.append(lengths[i]);
            sb.append("|");//$NON-NLS-1$

        }
        for (int i = 0; i < adjacency; i++) {
            sb.append(angles[i]);
            if (i < lastIndex) sb.append("|");//$NON-NLS-1$
        }
        return sb.toString();
    }

}
