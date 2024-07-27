package org.nschmidt.ldparteditor.data;

import java.util.List;
import java.util.Set;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.enumtype.Colour;
import org.nschmidt.ldparteditor.helper.EdgeData;

public enum HiQualityEdgeCalculator {
    INSTANCE;

    public static EdgeData[] hiQualityEdgeData(List<GDataAndWinding> dataInOrder, List<Float> dataLines, List<Integer> indicesLines, List<Float> dataTransparentLines, List<Integer> indicesTransparentLines, List<Float> dataCondlines, List<Integer> indicesCondlines, List<Float> dataTransparentCondlines, List<Integer> indicesTransparentCondlines, Set<GData> hiddenSet, boolean hideLines,
            boolean hideCondlines, boolean condlineMode) {
        EdgeData[] result = new EdgeData[4];
        // [0] hi-quality solid lines
        // [1] hi-quality transparent lines
        // [2] hi-quality solid condlines
        // [3] hi-quality transparent condlines
        dataLines.clear();
        indicesLines.clear();

        dataCondlines.clear();
        indicesCondlines.clear();

        dataTransparentLines.clear();
        indicesTransparentLines.clear();

        dataTransparentCondlines.clear();
        indicesTransparentCondlines.clear();

        int pointCountLines = 0;
        int pointCountCondlines = 0;
        int pointCountTransparentLines = 0;
        int pointCountTransparentCondlines = 0;

        for (GDataAndWinding d : dataInOrder) {
            int index = pointCountLines;
            List<Float> target = dataLines;
            List<Integer> tagetIndices = indicesLines;
            final GData gd = d.data;
            final boolean negDet = d.negativeDeterminant;
            if (hiddenSet.contains(gd)) continue;
            final float[][] lGeom;
            final Matrix4f matrix;
            final float r;
            final float g;
            final float b;
            if (!hideLines && gd instanceof GData2 gd2) {
                if (gd2.a < 1f) {
                    index = pointCountTransparentLines;
                    pointCountTransparentLines += 16;
                    target = dataTransparentLines;
                    tagetIndices = indicesTransparentLines;
                } else {
                    pointCountLines += 16;
                }

                lGeom = gd2.lGeom;
                matrix = gd2.parent.productMatrix;
                r = gd2.r;
                g = gd2.g;
                b = gd2.b;


                for (int i = 2; i < 18; i++) {
                    addPoint(target, matrix,
                            lGeom[i][0] * lGeom[20][0], lGeom[i][1] * lGeom[20][1], lGeom[i][2] * lGeom[20][2],
                            r,g,b);
                }
            } else if (!hideCondlines && gd instanceof GData5 gd5) {
                if (gd5.a < 1f) {
                    index = pointCountTransparentCondlines;
                    pointCountTransparentCondlines += 16;
                    target = dataTransparentCondlines;
                    tagetIndices = indicesTransparentCondlines;
                } else {
                    index = pointCountCondlines;
                    pointCountCondlines += 16;
                    target = dataCondlines;
                    tagetIndices = indicesCondlines;
                }

                lGeom = gd5.lGeom;
                matrix = gd5.parent.productMatrix;

                if (condlineMode) {
                    if (gd5.wasShown()) {
                        r = Colour.condlineShownColourR;
                        g = Colour.condlineShownColourG;
                        b = Colour.condlineShownColourB;
                    } else {
                        r = Colour.condlineHiddenColourR;
                        g = Colour.condlineHiddenColourG;
                        b = Colour.condlineHiddenColourB;
                    }
                } else {
                    r = gd5.r;
                    g = gd5.g;
                    b = gd5.b;
                }

                for (int i = 2; i < 18; i++) {
                    addPoint(target, matrix,
                            lGeom[i][0] * lGeom[20][0], lGeom[i][1] * lGeom[20][1], lGeom[i][2] * lGeom[20][2],
                            gd5.x1,gd5.y1,gd5.z1,
                            gd5.x2,gd5.y2,gd5.z2,
                            gd5.x3,gd5.y3,gd5.z3,
                            gd5.x4,gd5.y4,gd5.z4,
                            r,g,b);
                }
            } else {
                continue;
            }


            if (negDet) {
                int startIndex1 = index;
                int startIndex2 = index + 1;

                for (int i = 0; i < 7; i++) {
                    tagetIndices.add(index + 3);
                    tagetIndices.add(index + 1);
                    tagetIndices.add(index);
                    tagetIndices.add(index + 2);
                    index += 2;
                }

                tagetIndices.add(index);
                tagetIndices.add(startIndex1);
                tagetIndices.add(startIndex2);
                tagetIndices.add(index + 1);
            } else {
                int startIndex1 = index;
                int startIndex2 = index + 1;

                for (int i = 0; i < 7; i++) {
                    tagetIndices.add(index + 3);
                    tagetIndices.add(index + 2);
                    tagetIndices.add(index);
                    tagetIndices.add(index + 1);
                    index += 2;
                }

                tagetIndices.add(index);
                tagetIndices.add(index + 1);
                tagetIndices.add(startIndex2);
                tagetIndices.add(startIndex1);
            }
        }

        result[0] = copyData(dataLines, indicesLines);
        result[1] = copyData(dataTransparentLines, indicesTransparentLines);
        result[2] = copyData(dataCondlines, indicesCondlines);
        result[3] = copyData(dataTransparentCondlines, indicesTransparentCondlines);

        return result;
    }

    private static void addPoint(List<Float> data, Matrix4f matrix,
            float vx, float vy, float vz,
            float r, float g, float b) {
        final Vector4f v = Matrix4f.transform(matrix, new Vector4f(vx, vy, vz, 1f), null);
        // Position
        data.add(v.x);
        data.add(v.y);
        data.add(v.z);
        // Color
        data.add(r);
        data.add(g);
        data.add(b);
    }

    private static void addPoint(List<Float> data, Matrix4f matrix,
            float vx, float vy, float vz,
            float ax, float ay,float az,
            float bx, float by, float bz,
            float cx, float cy, float cz,
            float dx, float dy, float dz,
            float colR, float colG, float colB) {
        final Vector4f v = Matrix4f.transform(matrix, new Vector4f(vx, vy, vz, 1f), null);
        final Vector4f a = Matrix4f.transform(matrix, new Vector4f(ax, ay, az, 1f), null);
        final Vector4f b = Matrix4f.transform(matrix, new Vector4f(bx, by, bz, 1f), null);
        final Vector4f c = Matrix4f.transform(matrix, new Vector4f(cx, cy, cz, 1f), null);
        final Vector4f d = Matrix4f.transform(matrix, new Vector4f(dx, dy, dz, 1f), null);
        // Position
        data.add(v.x);
        data.add(v.y);
        data.add(v.z);
        // Point A
        data.add(a.x);
        data.add(a.y);
        data.add(a.z);
        // Point B
        data.add(b.x);
        data.add(b.y);
        data.add(b.z);
        // Point C
        data.add(c.x);
        data.add(c.y);
        data.add(c.z);
        // Point D
        data.add(d.x);
        data.add(d.y);
        data.add(d.z);
        // Color
        data.add(colR);
        data.add(colG);
        data.add(colB);
    }

    private static EdgeData copyData(List<Float> data, List<Integer> indices) {
        final int size = data.size();
        final int indexCount = indices.size();
        final float[] copiedData = new float[size];
        final int[] copiedIndices = new int[indexCount];
        int i = 0;
        for (Float f : data) {
            copiedData[i++] = f;
        }

        i = 0;
        for (Integer n : indices) {
            copiedIndices[i++] = n;
        }

        return new EdgeData(copiedData, copiedIndices);
    }
}
