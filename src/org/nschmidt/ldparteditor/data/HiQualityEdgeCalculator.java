package org.nschmidt.ldparteditor.data;

import java.util.List;
import java.util.Set;

public enum HiQualityEdgeCalculator {
    INSTANCE;

    public static float[][][] hiQualityEdgeData(List<GDataAndWinding> dataInOrder, Set<GData> hiddenSet, boolean hideLines,
            boolean hideCondlines) {
        float[][][] result = new float[4][0][0];
        // [0][*chunk*] hi-quality solid lines
        // [1][*chunk*] hi-quality transparent lines
        // [2][*chunk*] hi-quality solid condlines
        // [3][*chunk*] hi-quality transparent condlines

        // TODO Auto-generated method stub

        return result;
    }
}
