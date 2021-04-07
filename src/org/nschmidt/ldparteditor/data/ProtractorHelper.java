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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.TreeSet;

import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;

public enum ProtractorHelper {
    INSTANCE;

    public static BigDecimal[] changeLength(BigDecimal newLength, GData3 tri) {
        BigDecimal factor = newLength.divide(tri.getProtractorLength(), Threshold.MC);

        Vector3d center = new Vector3d(tri.X1, tri.Y1, tri.Z1);
        Vector3d aToC = Vector3d.sub(new Vector3d(tri.X3, tri.Y3, tri.Z3), center);

        BigDecimal[] result = new BigDecimal[3];
        result[0] = tri.X1.add(aToC.X.multiply(factor, Threshold.MC));
        result[1] = tri.Y1.add(aToC.Y.multiply(factor, Threshold.MC));
        result[2] = tri.Z1.add(aToC.Z.multiply(factor, Threshold.MC));
        return result;
    }

    public static BigDecimal[] changeLength(BigDecimal newLength, GData2 line) {
        BigDecimal factor = newLength.divide(line.getLength(), Threshold.MC);

        Vector3d center = new Vector3d(line.X1, line.Y1, line.Z1);
        Vector3d aToB = Vector3d.sub(new Vector3d(line.X2, line.Y2, line.Z2), center);

        BigDecimal[] result = new BigDecimal[3];
        result[0] = line.X1.add(aToB.X.multiply(factor, Threshold.MC));
        result[1] = line.Y1.add(aToB.Y.multiply(factor, Threshold.MC));
        result[2] = line.Z1.add(aToB.Z.multiply(factor, Threshold.MC));
        return result;
    }

    public static BigDecimal[] changeAngle(double angle, GData3 tri) {
        return changeAngle(angle, new Vector3d(tri.X1, tri.Y1, tri.Z1), new Vector3d(tri.X2, tri.Y2, tri.Z2), new Vector3d(tri.X3, tri.Y3, tri.Z3), 6, 10);
    }

    public static BigDecimal[] changeAngle(double angle, Vector3d a, Vector3d b, Vector3d c, int angleAccuracy, int lenghtAccuracy) {

        BigDecimal[] result = new BigDecimal[3];
        result[0] = c.X;
        result[1] = c.Y;
        result[2] = c.Z;

        Vector3d oldPos = new Vector3d(c.X, c.Y, c.Z);
        Vector3d center = new Vector3d(a.X, a.Y, a.Z);

        Vector3d aToB = Vector3d.sub(new Vector3d(b.X, b.Y, b.Z), center);
        Vector3d aToC = Vector3d.sub(new Vector3d(c.X, c.Y, c.Z), center);
        BigDecimal targetDistSq = Vector3d.distSquare(new Vector3d(c.X, c.Y, c.Z), center);
        if (angle == 0.0 || angle == 180.0 || BigDecimal.ZERO.compareTo(aToB.length()) == 0 || BigDecimal.ZERO.compareTo(aToC.length()) == 0) {
            return result;
        }
        Vector3d u = new Vector3d();
        aToB.normalise(u);
        Vector3d v = new Vector3d();
        aToC.normalise(v);

        double targetAngle = angle;

        TreeSet<Vertex> itearatedPositions =  new TreeSet<>();
        int iterations = 0;

        final BigDecimal tenth = new BigDecimal("0.1"); //$NON-NLS-1$

        Vertex pMin = new Vertex(oldPos);
        ArrayList<Object[]> res = new ArrayList<>();

        int innerIterations = 0;

        while (iterations < angleAccuracy) {
            itearatedPositions.clear();
            innerIterations = 0;
            while (!itearatedPositions.contains(pMin) && innerIterations < 1000) {
                itearatedPositions.add(pMin);
                innerIterations++;
                Vector3d min = new Vector3d(pMin);
                res.add(eval1(targetAngle, aToB, center, min));
                res.add(eval1(targetAngle, aToB, center, Vector3d.add(min, u)));
                res.add(eval1(targetAngle, aToB, center, Vector3d.add(min, v)));
                res.add(eval1(targetAngle, aToB, center, Vector3d.add(Vector3d.add(min, u), v)));
                res.add(eval1(targetAngle, aToB, center, Vector3d.add(Vector3d.sub(min, u), v)));
                res.add(eval1(targetAngle, aToB, center, Vector3d.sub(min, u)));
                res.add(eval1(targetAngle, aToB, center, Vector3d.sub(min, v)));
                res.add(eval1(targetAngle, aToB, center, Vector3d.sub(Vector3d.add(min, u), v)));
                res.add(eval1(targetAngle, aToB, center, Vector3d.sub(Vector3d.sub(min, u), v)));

                int minI = -1;
                double minCorr = 1E100;
                for (int i = 0; i < res.size(); i++) {
                    Object[] r = res.get(i);
                    double corr = (double) r[0];
                    if (corr < minCorr) {
                        minCorr = corr;
                        minI = i;
                    }
                }

                if (minI == -1) {
                    break;
                }

                pMin = new Vertex((Vector3d) res.get(minI)[1]);
                res.clear();
            }
            u = u.scale(tenth);
            v = v.scale(tenth);
            iterations++;
        }

        u = Vector3d.sub(new Vector3d(pMin), center);
        u.normalise(u);

        iterations = 0;
        while (iterations < lenghtAccuracy) {
            itearatedPositions.clear();
            innerIterations = 0;
            while (!itearatedPositions.contains(pMin) && innerIterations < 1000) {
                itearatedPositions.add(pMin);
                innerIterations++;
                Vector3d min = new Vector3d(pMin);
                res.add(eval2(targetDistSq, center, min));
                res.add(eval2(targetDistSq, center, Vector3d.add(min, u)));
                res.add(eval2(targetDistSq, center, Vector3d.sub(min, u)));

                int minI = -1;
                double minCorr = 1E100;
                for (int i = 0; i < res.size(); i++) {
                    Object[] r = res.get(i);
                    double corr = (double) r[0];
                    if (corr < minCorr) {
                        minCorr = corr;
                        minI = i;
                    }
                }

                if (minI == -1) {
                    break;
                }

                pMin = new Vertex((Vector3d) res.get(minI)[1]);
                res.clear();
            }
            u = u.scale(tenth);
            iterations++;
        }

        result[0] = pMin.X;
        result[1] = pMin.Y;
        result[2] = pMin.Z;
        return result;
    }

    private static Object[] eval1(double targetAngle, Vector3d aToB, Vector3d center, Vector3d newPos) {
        Object[] result = new Object[2];
        double f1 = Vector3d.angle(aToB, Vector3d.sub(newPos, center)) / targetAngle;
        result[0] = Math.abs(f1 - 1);
        result[1] = newPos;
        return result;
    }


    private static Object[] eval2(BigDecimal targetDistSq, Vector3d center, Vector3d newPos) {
        Object[] result = new Object[2];
        double f2 = Vector3d.distSquare(center, newPos).divide(targetDistSq, Threshold.MC).doubleValue();
        result[0] = Math.abs(f2 - 1);
        result[1] = newPos;
        return result;
    }
}
