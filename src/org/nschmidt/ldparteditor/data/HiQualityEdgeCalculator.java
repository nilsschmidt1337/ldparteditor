package org.nschmidt.ldparteditor.data;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.lwjgl.opengl.GL11;

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

        List<Float> data = new LinkedList<>();

        for (GDataAndWinding d : dataInOrder) {
            List<Float> target = data;
            final GData gd = d.data;
            if (hiddenSet.contains(gd)) continue;
            final float[][] lGeom;
            final float r;
            final float g;
            final float b;
            if (!hideLines && gd instanceof GData2 gd2) {
                lGeom = gd2.lGeom;
                r = gd2.r;
                g = gd2.g;
                b = gd2.b;
            } else if (!hideCondlines && gd instanceof GData5 gd5) {
                continue;
            } else {
                continue;
            }

            addQuad(target,
                    lGeom[1][0], lGeom[1][1], lGeom[1][2],
                    lGeom[0][0], lGeom[0][1], lGeom[0][2],
                    lGeom[3][0], lGeom[3][1], lGeom[3][2],
                    lGeom[2][0], lGeom[2][1], lGeom[2][2],
                    0f,0f,0f,
                    0f,0f,0f,
                    0f,0f,0f,
                    0f,0f,0f,
                    r,g,b);
        }

        // FIXME: JUST FOR DEBUGGING (FRONT VIEW)
        // Position
        data.add(10f);
        data.add(0f);
        data.add(0f);
        // Point A
        data.add(0f);
        data.add(0f);
        data.add(0f);
        // Point B
        data.add(0f);
        data.add(0f);
        data.add(0f);
        // Point C
        data.add(0f);
        data.add(0f);
        data.add(0f);
        // Point D
        data.add(0f);
        data.add(0f);
        data.add(0f);
        // Color
        data.add(1f);
        data.add(0f);
        data.add(0f);

        // Position
        data.add(10f);
        data.add(10f);
        data.add(0f);
        // Point A
        data.add(0f);
        data.add(0f);
        data.add(0f);
        // Point B
        data.add(0f);
        data.add(0f);
        data.add(0f);
        // Point C
        data.add(0f);
        data.add(0f);
        data.add(0f);
        // Point D
        data.add(0f);
        data.add(0f);
        data.add(0f);
        // Color
        data.add(1f);
        data.add(0f);
        data.add(0f);

        // Position
        data.add(0f);
        data.add(10f);
        data.add(0f);
        // Point A
        data.add(0f);
        data.add(0f);
        data.add(0f);
        // Point B
        data.add(0f);
        data.add(0f);
        data.add(0f);
        // Point C
        data.add(0f);
        data.add(0f);
        data.add(0f);
        // Point D
        data.add(0f);
        data.add(0f);
        data.add(0f);
        // Color
        data.add(0f);
        data.add(1f);
        data.add(0f);

        // Position
        data.add(0f);
        data.add(0f);
        data.add(0f);
        // Point A
        data.add(0f);
        data.add(0f);
        data.add(0f);
        // Point B
        data.add(0f);
        data.add(0f);
        data.add(0f);
        // Point C
        data.add(0f);
        data.add(0f);
        data.add(0f);
        // Point D
        data.add(0f);
        data.add(0f);
        data.add(0f);
        // Color
        data.add(0f);
        data.add(1f);
        data.add(0f);

        result[0] = splitInChunks(data);


        return result;
    }

    private static void addQuad(List<Float> data,
            float v1x, float v1y, float v1z,
            float v2x, float v2y, float v2z,
            float v3x, float v3y, float v3z,
            float v4x, float v4y, float v4z,
            float ax, float ay, float az,
            float bx, float by, float bz,
            float cx, float cy, float cz,
            float dx, float dy, float dz,
            float r, float g, float b) {
        // Position
        data.add(v1x);
        data.add(v1y);
        data.add(v1z);
        // Point A
        data.add(ax);
        data.add(ay);
        data.add(az);
        // Point B
        data.add(bx);
        data.add(by);
        data.add(bz);
        // Point C
        data.add(cx);
        data.add(cy);
        data.add(cz);
        // Point D
        data.add(dx);
        data.add(dy);
        data.add(dz);
        // Color
        data.add(r);
        data.add(g);
        data.add(b);

        // Position
        data.add(v2x);
        data.add(v2y);
        data.add(v2z);
        // Point A
        data.add(ax);
        data.add(ay);
        data.add(az);
        // Point B
        data.add(bx);
        data.add(by);
        data.add(bz);
        // Point C
        data.add(cx);
        data.add(cy);
        data.add(cz);
        // Point D
        data.add(dx);
        data.add(dy);
        data.add(dz);
        // Color
        data.add(r);
        data.add(g);
        data.add(b);

        // Position
        data.add(v3x);
        data.add(v3y);
        data.add(v3z);
        // Point A
        data.add(ax);
        data.add(ay);
        data.add(az);
        // Point B
        data.add(bx);
        data.add(by);
        data.add(bz);
        // Point C
        data.add(cx);
        data.add(cy);
        data.add(cz);
        // Point D
        data.add(dx);
        data.add(dy);
        data.add(dz);
        // Color
        data.add(r);
        data.add(g);
        data.add(b);

        // Position
        data.add(v4x);
        data.add(v4y);
        data.add(v4z);
        // Point A
        data.add(ax);
        data.add(ay);
        data.add(az);
        // Point B
        data.add(bx);
        data.add(by);
        data.add(bz);
        // Point C
        data.add(cx);
        data.add(cy);
        data.add(cz);
        // Point D
        data.add(dx);
        data.add(dy);
        data.add(dz);
        // Color
        data.add(r);
        data.add(g);
        data.add(b);
    }

    private static float[][] splitInChunks(List<Float> data) {
        final int size = data.size();
        // TODO Do an actual split [multiple of 72 (18 * 4)]
        float[][] result = new float[size > 0 ? 1 : 0][size];
        int i = 0;
        for (Float f : data) {
            result[0][i] = f;
            i++;
        }

        return result;
    }
}
