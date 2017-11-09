package org.nschmidt.csg;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.nschmidt.ldparteditor.data.GData3;

enum CSGOptimizerEdgeCollapse {
    INSTANCE;

    public static boolean optimize(Random rnd, Map<Plane, List<GData3>> trianglesPerPlane, TreeMap<GData3, IdAndPlane> optimization) {

        // FIXME Do iterative optimization here!

        boolean result = false;
        return result;
    }
}
