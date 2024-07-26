package org.nschmidt.ldparteditor.data;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.helper.EdgeData;

public enum HiQualityEdgeCalculator {
    INSTANCE;

    public static EdgeData[] hiQualityEdgeData(List<GDataAndWinding> dataInOrder, Set<GData> hiddenSet, boolean hideLines,
            boolean hideCondlines) {
        EdgeData[] result = new EdgeData[4];
        // [0][*chunk*] hi-quality solid lines
        // [1][*chunk*] hi-quality transparent lines
        // [2][*chunk*] hi-quality solid condlines
        // [3][*chunk*] hi-quality transparent condlines

        // TODO Auto-generated method stub

        List<Float> data = new LinkedList<>();

        for (GDataAndWinding d : dataInOrder) {
            List<Float> target = data;
            final GData gd = d.data;
            final boolean negDet = d.negativeDeterminant;
            if (hiddenSet.contains(gd)) continue;
            final float[][] lGeom;
            final Matrix4f matrix;
            final float r;
            final float g;
            final float b;
            if (!hideLines && gd instanceof GData2 gd2) {
                lGeom = gd2.lGeom;
                matrix = gd2.parent.productMatrix;
                r = gd2.r;
                g = gd2.g;
                b = gd2.b;
            } else if (!hideCondlines && gd instanceof GData5 gd5) {
                continue;
            } else {
                continue;
            }

            addQuad(target,
                    matrix, negDet,
                    lGeom[2][0] * lGeom[20][0], lGeom[2][1] * lGeom[20][1], lGeom[2][2] * lGeom[20][2],
                    lGeom[4][0] * lGeom[20][0], lGeom[4][1] * lGeom[20][1], lGeom[4][2] * lGeom[20][2],
                    lGeom[5][0] * lGeom[20][0], lGeom[5][1] * lGeom[20][1], lGeom[5][2] * lGeom[20][2],
                    lGeom[3][0] * lGeom[20][0], lGeom[3][1] * lGeom[20][1], lGeom[3][2] * lGeom[20][2],
                    0f,0f,0f,
                    0f,0f,0f,
                    0f,0f,0f,
                    0f,0f,0f,
                    r,g,b);

            addQuad(target,
                    matrix, negDet,
                    lGeom[4][0] * lGeom[20][0], lGeom[4][1] * lGeom[20][1], lGeom[4][2] * lGeom[20][2],
                    lGeom[6][0] * lGeom[20][0], lGeom[6][1] * lGeom[20][1], lGeom[6][2] * lGeom[20][2],
                    lGeom[7][0] * lGeom[20][0], lGeom[7][1] * lGeom[20][1], lGeom[7][2] * lGeom[20][2],
                    lGeom[5][0] * lGeom[20][0], lGeom[5][1] * lGeom[20][1], lGeom[5][2] * lGeom[20][2],
                    0f,0f,0f,
                    0f,0f,0f,
                    0f,0f,0f,
                    0f,0f,0f,
                    r,g,b);

            addQuad(target,
                    matrix, negDet,
                    lGeom[6][0] * lGeom[20][0], lGeom[6][1] * lGeom[20][1], lGeom[6][2] * lGeom[20][2],
                    lGeom[8][0] * lGeom[20][0], lGeom[8][1] * lGeom[20][1], lGeom[8][2] * lGeom[20][2],
                    lGeom[9][0] * lGeom[20][0], lGeom[9][1] * lGeom[20][1], lGeom[9][2] * lGeom[20][2],
                    lGeom[7][0] * lGeom[20][0], lGeom[7][1] * lGeom[20][1], lGeom[7][2] * lGeom[20][2],
                    0f,0f,0f,
                    0f,0f,0f,
                    0f,0f,0f,
                    0f,0f,0f,
                    r,g,b);

            addQuad(target,
                    matrix, negDet,
                    lGeom[8][0] * lGeom[20][0], lGeom[8][1] * lGeom[20][1], lGeom[8][2] * lGeom[20][2],
                    lGeom[10][0] * lGeom[20][0], lGeom[10][1] * lGeom[20][1], lGeom[10][2] * lGeom[20][2],
                    lGeom[11][0] * lGeom[20][0], lGeom[11][1] * lGeom[20][1], lGeom[11][2] * lGeom[20][2],
                    lGeom[9][0] * lGeom[20][0], lGeom[9][1] * lGeom[20][1], lGeom[9][2] * lGeom[20][2],
                    0f,0f,0f,
                    0f,0f,0f,
                    0f,0f,0f,
                    0f,0f,0f,
                    r,g,b);

            addQuad(target,
                    matrix, negDet,
                    lGeom[10][0] * lGeom[20][0], lGeom[10][1] * lGeom[20][1], lGeom[10][2] * lGeom[20][2],
                    lGeom[12][0] * lGeom[20][0], lGeom[12][1] * lGeom[20][1], lGeom[12][2] * lGeom[20][2],
                    lGeom[13][0] * lGeom[20][0], lGeom[13][1] * lGeom[20][1], lGeom[13][2] * lGeom[20][2],
                    lGeom[11][0] * lGeom[20][0], lGeom[11][1] * lGeom[20][1], lGeom[11][2] * lGeom[20][2],
                    0f,0f,0f,
                    0f,0f,0f,
                    0f,0f,0f,
                    0f,0f,0f,
                    r,g,b);

            addQuad(target,
                    matrix, negDet,
                    lGeom[12][0] * lGeom[20][0], lGeom[12][1] * lGeom[20][1], lGeom[12][2] * lGeom[20][2],
                    lGeom[14][0] * lGeom[20][0], lGeom[14][1] * lGeom[20][1], lGeom[14][2] * lGeom[20][2],
                    lGeom[15][0] * lGeom[20][0], lGeom[15][1] * lGeom[20][1], lGeom[15][2] * lGeom[20][2],
                    lGeom[13][0] * lGeom[20][0], lGeom[13][1] * lGeom[20][1], lGeom[13][2] * lGeom[20][2],
                    0f,0f,0f,
                    0f,0f,0f,
                    0f,0f,0f,
                    0f,0f,0f,
                    r,g,b);

            addQuad(target,
                    matrix, negDet,
                    lGeom[14][0] * lGeom[20][0], lGeom[14][1] * lGeom[20][1], lGeom[14][2] * lGeom[20][2],
                    lGeom[16][0] * lGeom[20][0], lGeom[16][1] * lGeom[20][1], lGeom[16][2] * lGeom[20][2],
                    lGeom[17][0] * lGeom[20][0], lGeom[17][1] * lGeom[20][1], lGeom[17][2] * lGeom[20][2],
                    lGeom[15][0] * lGeom[20][0], lGeom[15][1] * lGeom[20][1], lGeom[15][2] * lGeom[20][2],
                    0f,0f,0f,
                    0f,0f,0f,
                    0f,0f,0f,
                    0f,0f,0f,
                    r,g,b);

            addQuad(target,
                    matrix, negDet,
                    lGeom[16][0] * lGeom[20][0], lGeom[16][1] * lGeom[20][1], lGeom[16][2] * lGeom[20][2],
                    lGeom[2][0] * lGeom[20][0], lGeom[2][1] * lGeom[20][1], lGeom[2][2] * lGeom[20][2],
                    lGeom[3][0] * lGeom[20][0], lGeom[3][1] * lGeom[20][1], lGeom[3][2] * lGeom[20][2],
                    lGeom[17][0] * lGeom[20][0], lGeom[17][1] * lGeom[20][1], lGeom[17][2] * lGeom[20][2],
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
        result[1] = new EdgeData(new float[0][0], new int[0][0]);
        result[2] = new EdgeData(new float[0][0], new int[0][0]);
        result[3] = new EdgeData(new float[0][0], new int[0][0]);

        return result;
    }

    private static void addQuad(List<Float> data,
            Matrix4f matrix, boolean negDet,
            float v1x, float v1y,
            float v1z, float v2x, float v2y,
            float v2z, float v3x, float v3y,
            float v3z, float v4x, float v4y,
            float v4z, float ax, float ay,
            float az, float bx, float by,
            float bz, float cx, float cy,
            float cz, float dx, float dy,
            float dz, float r, float g, float b) {
        final Vector4f v1;
        final Vector4f v2 = Matrix4f.transform(matrix, new Vector4f(v2x, v2y, v2z, 1f), null);
        final Vector4f v3;
        final Vector4f v4 = Matrix4f.transform(matrix, new Vector4f(v4x, v4y, v4z, 1f), null);
        if (negDet) {
            v1 = Matrix4f.transform(matrix, new Vector4f(v1x, v1y, v1z, 1f), null);
            v3 = Matrix4f.transform(matrix, new Vector4f(v3x, v3y, v3z, 1f), null);
        } else {
            v1 = Matrix4f.transform(matrix, new Vector4f(v3x, v3y, v3z, 1f), null);
            v3 = Matrix4f.transform(matrix, new Vector4f(v1x, v1y, v1z, 1f), null);
        }
        addPoint(data, v1.x, v1.y, v1.z, ax, ay, az, bx, by, bz, cx, cy, cz, dx, dy, dz, r, g, b);
        addPoint(data, v2.x, v2.y, v2.z, ax, ay, az, bx, by, bz, cx, cy, cz, dx, dy, dz, r, g, b);
        addPoint(data, v3.x, v3.y, v3.z, ax, ay, az, bx, by, bz, cx, cy, cz, dx, dy, dz, r, g, b);
        addPoint(data, v4.x, v4.y, v4.z, ax, ay, az, bx, by, bz, cx, cy, cz, dx, dy, dz, r, g, b);
    }

    private static void addPoint(List<Float> data, float x, float y, float z, float ax, float ay, float az,
            float bx, float by, float bz, float cx, float cy, float cz, float dx, float dy, float dz, float r, float g,
            float b) {
        // Position
        data.add(x);
        data.add(y);
        data.add(z);
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

    private static EdgeData splitInChunks(List<Float> data) {
        final int size = data.size();
        // TODO Do an actual split [multiple of 72 (18 * 4)]
        float[][] result = new float[size > 0 ? 1 : 0][size];
        int i = 0;
        for (Float f : data) {
            result[0][i] = f;
            i++;
        }

        return new EdgeData(result, null);
    }
}
