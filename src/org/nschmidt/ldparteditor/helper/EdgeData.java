package org.nschmidt.ldparteditor.helper;

import java.util.Arrays;

public record EdgeData(float[][] vertices, int[][] indices) {

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.deepHashCode(indices);
        result = prime * result + Arrays.deepHashCode(vertices);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof EdgeData))
            return false;
        EdgeData other = (EdgeData) obj;
        return Arrays.deepEquals(indices, other.indices) && Arrays.deepEquals(vertices, other.vertices);
    }

    @Override
    public String toString() {
        return "EdgeData [vertices=" + Arrays.toString(vertices) + ", indices=" + Arrays.toString(indices) + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
