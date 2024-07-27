package org.nschmidt.ldparteditor.data;

import java.util.ArrayList;
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

        List<Float> data = new ArrayList<>(1_000_000);
        List<Integer> indices = new ArrayList<>(1_000_000);

        int pointCount = 0;

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

            for (int i = 2; i < 18; i++) {
                addPoint(target, matrix,
                        lGeom[i][0] * lGeom[20][0], lGeom[i][1] * lGeom[20][1], lGeom[i][2] * lGeom[20][2],
                        0f,0f,0f,
                        0f,0f,0f,
                        0f,0f,0f,
                        0f,0f,0f,
                        r,g,b);
            }

            int index = pointCount;
            pointCount += 16;
            if (negDet) {
                int startIndex1 = index;
                int startIndex2 = index + 1;

                for (int i = 0; i < 7; i++) {
                    indices.add(index + 3);
                    indices.add(index + 1);
                    indices.add(index);
                    indices.add(index + 2);
                    index += 2;
                }

                indices.add(index);
                indices.add(startIndex1);
                indices.add(startIndex2);
                indices.add(index + 1);
            } else {
                int startIndex1 = index;
                int startIndex2 = index + 1;

                for (int i = 0; i < 7; i++) {
                    indices.add(index + 3);
                    indices.add(index + 2);
                    indices.add(index);
                    indices.add(index + 1);
                    index += 2;
                }

                indices.add(index);
                indices.add(index + 1);
                indices.add(startIndex2);
                indices.add(startIndex1);
            }
        }

        result[0] = splitInChunks(data, indices);
        result[1] = new EdgeData(new float[0][0], new int[0][0]);
        result[2] = new EdgeData(new float[0][0], new int[0][0]);
        result[3] = new EdgeData(new float[0][0], new int[0][0]);

        return result;
    }

    private static void addPoint(List<Float> data, Matrix4f matrix,
            float v1x, float v1y, float v1z,
            float ax, float ay,float az,
            float bx, float by, float bz,
            float cx, float cy, float cz,
            float dx, float dy, float dz,
            float r, float g, float b) {
        final Vector4f v = Matrix4f.transform(matrix, new Vector4f(v1x, v1y, v1z, 1f), null);
        /*final Vector4f a = Matrix4f.transform(matrix, new Vector4f(v1x, v1y, v1z, 1f), null);
        final Vector4f b = Matrix4f.transform(matrix, new Vector4f(v1x, v1y, v1z, 1f), null);
        final Vector4f c = Matrix4f.transform(matrix, new Vector4f(v1x, v1y, v1z, 1f), null);
        final Vector4f d = Matrix4f.transform(matrix, new Vector4f(v1x, v1y, v1z, 1f), null);*/
        // TODO Inline later?
        addPoint(data, v.x, v.y, v.z, ax, ay, az, bx, by, bz, cx, cy, cz, dx, dy, dz, r, g, b);
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

    private static EdgeData splitInChunks(List<Float> data, List<Integer> indices) {
        final int size = data.size();
        final int indexCount = indices.size();
        // TODO Do an actual split [multiple of 72 (18 * 4)]
        final float[][] chunkedData = new float[size > 0 ? 1 : 0][size];
        final int[][] chunkedIndices = new int[indexCount > 0 ? 1 : 0][indexCount];
        int i = 0;
        for (Float f : data) {
            chunkedData[0][i] = f;
            i++;
        }

        i = 0;
        for (Integer n : indices) {
            chunkedIndices[0][i] = n;
            i++;
        }

        return new EdgeData(chunkedData, chunkedIndices);
    }
}
