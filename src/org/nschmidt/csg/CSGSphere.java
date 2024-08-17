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
package org.nschmidt.csg;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GColourIndex;
import org.nschmidt.ldparteditor.helper.math.EightSphereGenerator;
import org.nschmidt.ldparteditor.logger.NLogger;

/**
 * A solid iso-sphere.
 *
 */
public class CSGSphere implements Primitive {

    public final int id = idCounter.getAndIncrement();

    private static final Pattern WHITESPACE = Pattern.compile("\\s+"); //$NON-NLS-1$

    private int divisions;

    /**
     * Constructor. Creates a sphere with radius 1 and center [0,0,0].
     */
    public CSGSphere() {
        init();
    }

    /**
     * Constructor. Creates a sphere with the specified divisions.
     *
     * @param divisions
     *            number of divisions
     */
    public CSGSphere(int divisions) {
        this.divisions = divisions;
    }

    private void init() {
        divisions = 16;
    }

    @Override
    public List<Polygon> toPolygons(DatFile df, GColour colour) {
        List<Polygon> polygons = new ArrayList<>();
        final List<String> lines = EightSphereGenerator.addEightSphere(divisions, false);
        for (String line : lines) {
            String[] segments = WHITESPACE.split(line);
            if (segments.length < 11) continue;

            try {
                final double v1x = Double.parseDouble(segments[2]);
                final double v1y = Double.parseDouble(segments[3]);
                final double v1z = Double.parseDouble(segments[4]);

                final double v2x = Double.parseDouble(segments[5]);
                final double v2y = Double.parseDouble(segments[6]);
                final double v2z = Double.parseDouble(segments[7]);

                final double v3x = Double.parseDouble(segments[8]);
                final double v3y = Double.parseDouble(segments[9]);
                final double v3z = Double.parseDouble(segments[10]);

                for (int xf = -1; xf < 2; xf += 2) {
                    for (int yf = -1; yf < 2; yf += 2) {
                        for (int zf = -1; zf < 2; zf += 2) {
                            final VectorCSGd a = new VectorCSGd(xf * v1x * 1000.0, yf * v1y * 1000.0, zf * v1z * 1000.0);
                            final VectorCSGd b = new VectorCSGd(xf * v2x * 1000.0, yf * v2y * 1000.0, zf * v2z * 1000.0);
                            final VectorCSGd c = new VectorCSGd(xf * v3x * 1000.0, yf * v3y * 1000.0, zf * v3z * 1000.0);

                            final List<VectorCSGd> vertices = new ArrayList<>(3);
                            vertices.add(a);
                            if (xf * yf * zf < 0) {
                                vertices.add(c);
                                vertices.add(b);
                            } else {
                                vertices.add(b);
                                vertices.add(c);
                            }

                            polygons.add(new Polygon(df, vertices, new GColourIndex(colour, id)));
                        }
                    }
                }
            } catch (NumberFormatException nfe) {
                NLogger.debug(CSGSphere.class, nfe);
            }
        }

        return polygons;
    }

    @Override
    public CSG toCSG(DatFile df, GColour colour) {
        return CSG.fromPolygons(toPolygons(df, colour));
    }
}
