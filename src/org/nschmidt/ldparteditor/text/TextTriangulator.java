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
package org.nschmidt.ldparteditor.text;



import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.PathIterator;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.nschmidt.delaunay.Pnt;
import org.nschmidt.delaunay.Triangle;
import org.nschmidt.delaunay.Triangulation;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GData0;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.GData3;
import org.nschmidt.ldparteditor.data.PGData3;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.helper.LDPartEditorException;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;

/**
 * @author nils
 *
 */
public enum TextTriangulator {
    INSTANCE;

    public static Set<GData> triangulateText(Font font, final float r, final float g, final float b, final String text, final double flatness, final double interpolateFlatness, final GData1 parent, final DatFile datFile, int fontHeight) {
        final GlyphVector vector = font.createGlyphVector(new FontRenderContext(null, false, false), text);

        final Set<GData> finalTriangleSet = Collections.synchronizedSet(new HashSet<>());

        if (vector.getNumGlyphs() == 0)
            return finalTriangleSet;

        double maxHeight = 0.0000001d;
        for (int i = 0; i < vector.getNumGlyphs(); i++) {
            double height = vector.getGlyphMetrics(0).getBounds2D().getHeight();
            if (height > maxHeight)
                maxHeight = height;
        }

        final double scale = fontHeight / maxHeight;
        final Thread[] threads = new Thread[vector.getNumGlyphs()];
        final AtomicInteger counter = new AtomicInteger(vector.getNumGlyphs());

        try
        {
            new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, true, new IRunnableWithProgress()
            {
                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                {
                    try
                    {
                        monitor.beginTask(I18n.TXT2DAT_TRIANGULATE, IProgressMonitor.UNKNOWN);
                        for (int j = 0; j < vector.getNumGlyphs(); j++) {
                            final int[] i = new int[1];
                            i[0] = j;
                            threads[j] = new Thread(() -> {
                                Shape characterShape = vector.getGlyphOutline(i[0]);
                                NLogger.debug(TextTriangulator.class, "Triangulating {0}", text.charAt(i[0])); //$NON-NLS-1$
                                Set<GData> characterTriangleSet = triangulateShape(monitor, characterShape, flatness, interpolateFlatness, parent, datFile, scale, r, g, b);
                                if (characterTriangleSet.isEmpty()) {
                                    counter.decrementAndGet();
                                }

                                NLogger.debug(TextTriangulator.class, "Triangulating [Done] {0}", text.charAt(i[0])); //$NON-NLS-1$

                                synchronized (finalTriangleSet) {
                                    finalTriangleSet.addAll(characterTriangleSet);
                                }
                            });
                            threads[j].start();
                        }
                        boolean isRunning = true;
                        while (isRunning) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                throw new LDPartEditorException(ie);
                            }
                            isRunning = false;
                            for (Thread thread : threads) {
                                if (thread.isAlive())
                                    isRunning = true;
                            }
                        }
                    }
                    finally
                    {
                        monitor.done();
                    }
                }
            });
        }
        catch (InvocationTargetException consumed) {
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new LDPartEditorException(ie);
        }

        datFile.getVertexManager().clearSelection2();
        datFile.getVertexManager().selectTriangles(finalTriangleSet);
        datFile.getVertexManager().roundSelection(3, 5, true, false, true, true, true);
        datFile.getVertexManager().restoreTriangles(finalTriangleSet);
        datFile.getVertexManager().clearSelection2();

        return finalTriangleSet;
    }

    private static Set<GData> triangulateShape(IProgressMonitor monitor, Shape shape, double flatness, double interpolateFlatness, GData1 parent, DatFile datFile, double scale, float r, float g,
            float b) {
        PathIterator shapePathIterator = shape.getPathIterator(null, flatness);

        /*
         * Add all points of the shape to the triangulation
         */
        double x = 0;
        double y = 0;
        double px;
        double py;

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;

        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        List<Pnt> places = new ArrayList<>();

        while (!shapePathIterator.isDone() && !monitor.isCanceled()) {
            px = x;
            py = y;

            double[] args = new double[6];

            switch (shapePathIterator.currentSegment(args)) {
            case PathIterator.SEG_MOVETO:
                x = args[0];
                y = args[1];
                break;
            case PathIterator.SEG_LINETO:
                x = args[0];
                y = args[1];

                if (px == x && py == y)
                    break;

                Pnt p1 = new Pnt(px, py);
                Pnt p2 = new Pnt(x, y);

                places.add(p1);
                if (px > maxX)
                    maxX = px;
                if (py > maxY)
                    maxY = py;
                if (px < minX)
                    minX = px;
                if (py < minY)
                    minY = py;

                Pnt lengthVector = new Pnt(x - px, y - py);

                // sqrt( x^2 + y^2 )
                double length = Math.sqrt(lengthVector.coord(0) * lengthVector.coord(0) + lengthVector.coord(1) * lengthVector.coord(1));

                double num = length / interpolateFlatness;
                double nx = lengthVector.coord(0) / num;
                double ny = lengthVector.coord(1) / num;

                if (num > 1) {
                    double start = (num - Math.floor(num)) / 2.0;

                    double cx = p1.coord(0);
                    double cy = p1.coord(1);
                    double ll = length;
                    ll = ll - start;
                    while (ll > interpolateFlatness) {
                        places.add(new Pnt(cx, cy));
                        if (cx > maxX)
                            maxX = cx;
                        if (cy > maxY)
                            maxY = cy;
                        if (cx < minX)
                            minX = cx;
                        if (cy < minY)
                            minY = cy;

                        cx = cx + nx;
                        cy = cy + ny;

                        ll = ll - interpolateFlatness;
                    }
                }

                places.add(p2);
                if (x > maxX)
                    maxX = x;
                if (y > maxY)
                    maxY = y;
                if (x < minX)
                    minX = x;
                if (y < minY)
                    minY = y;
                break;
            case PathIterator.SEG_QUADTO:
                break;
            case PathIterator.SEG_CUBICTO:
                break;
            case PathIterator.SEG_CLOSE:
                break;
            default:
                break;
            }

            shapePathIterator.next();
        }

        /*
         * Build the triangle which encompasses the shape (-0.5,+1) - (+0.5,+1)
         * - (0,-1)
         */
        double trisize = 100000d;
        Triangle tri = new Triangle(new Pnt(-trisize / 2.0, trisize), new Pnt(+trisize / 2.0, trisize), new Pnt(0, -trisize));

        // now add all triangles which are in the shape to the set
        Set<GData> finalTriangleSet = new HashSet<>();

        if (!places.isEmpty()) {

            GData anchor = new GData0(null, View.DUMMY_REFERENCE);

            // initialize the triangulation with this triangle
            Triangulation triangulation = new Triangulation(tri);

            triangulation.delaunayPlace(new Pnt(minX - 0.0, minY - 0.0));
            triangulation.delaunayPlace(new Pnt(maxX + 0.0, minY - 0.0));
            triangulation.delaunayPlace(new Pnt(maxX + 0.0, maxY + 0.0));
            triangulation.delaunayPlace(new Pnt(minX - 0.0, maxY + 0.0));

            for (Pnt place : places) {
                triangulation.delaunayPlace(place);
                if (triangulation.size() > 4000 || monitor.isCanceled()) break;
            }

            for (Triangle triangle : triangulation) {

                if (monitor.isCanceled()) break;

                Pnt point1 = triangle.get(0);
                Pnt point2 = triangle.get(1);
                Pnt point3 = triangle.get(2);

                double midX = point1.coord(0);
                double midY = point1.coord(1);
                midX = midX + point2.coord(0);
                midY = midY + point2.coord(1);
                midX = midX + point3.coord(0);
                midY = midY + point3.coord(1);

                midX /= 3.0;
                midY /= 3.0;

                if (shape.contains(midX, midY)) {
                    double[] vec1 = new double[] { point3.coord(0) - point1.coord(0), point3.coord(1) - point1.coord(1) };
                    double[] vec2 = new double[] { point3.coord(0) - point2.coord(0), point3.coord(1) - point2.coord(1) };
                    double wind = vec1[0] * vec2[1] - vec1[1] * vec2[0];
                    if (wind < 0) {
                        GData3 gdt = new GData3(-1, r, g, b, 1f, (float) (point1.coord(0) * scale), (float) (point1.coord(1) * scale), 0f, (float) (point2.coord(0) * scale),
                                (float) (point2.coord(1) * scale), 0f, (float) (point3.coord(0) * scale), (float) (point3.coord(1) * scale), 0f, parent, datFile, true);
                        anchor.setNext(gdt);
                        anchor = gdt;
                        finalTriangleSet.add(gdt);
                    } else {
                        GData3 gdt = new GData3(-1, r, g, b, 1f, (float) (point1.coord(0) * scale), (float) (point1.coord(1) * scale), 0f, (float) (point3.coord(0) * scale),
                                (float) (point3.coord(1) * scale), 0f, (float) (point2.coord(0) * scale), (float) (point2.coord(1) * scale), 0f, parent, datFile, true);
                        anchor.setNext(gdt);
                        anchor = gdt;
                        finalTriangleSet.add(gdt);
                    }
                } else if (!(point1.coord(0) == tri.get(0).coord(0) && point1.coord(1) == tri.get(0).coord(1)

                        ||

                        point2.coord(0) == tri.get(0).coord(0) && point2.coord(1) == tri.get(0).coord(1)

                        ||

                        point3.coord(0) == tri.get(0).coord(0) && point3.coord(1) == tri.get(0).coord(1)

                        ||

                        point1.coord(0) == tri.get(1).coord(0) && point1.coord(1) == tri.get(1).coord(1)

                        ||

                        point2.coord(0) == tri.get(1).coord(0) && point2.coord(1) == tri.get(1).coord(1)

                        ||

                        point3.coord(0) == tri.get(1).coord(0) && point3.coord(1) == tri.get(1).coord(1)

                        ||

                        point1.coord(0) == tri.get(2).coord(0) && point1.coord(1) == tri.get(2).coord(1)

                        ||

                        point2.coord(0) == tri.get(2).coord(0) && point2.coord(1) == tri.get(2).coord(1)

                        ||

                        point3.coord(0) == tri.get(2).coord(0) && point3.coord(1) == tri.get(2).coord(1)

                        )) {
                    double[] vec1 = new double[] { point3.coord(0) - point1.coord(0), point3.coord(1) - point1.coord(1) };
                    double[] vec2 = new double[] { point3.coord(0) - point2.coord(0), point3.coord(1) - point2.coord(1) };
                    double wind = vec1[0] * vec2[1] - vec1[1] * vec2[0];
                    if (wind < 0) {
                        GData3 gdt = new GData3(-1, 0.95f, 0.95f, 0.90f, 1f, (float) (point1.coord(0) * scale), (float) (point1.coord(1) * scale), 0f, (float) (point2.coord(0) * scale),
                                (float) (point2.coord(1) * scale), 0f, (float) (point3.coord(0) * scale), (float) (point3.coord(1) * scale), 0f, parent, datFile, true);
                        anchor.setNext(gdt);
                        anchor = gdt;
                        finalTriangleSet.add(gdt);
                    } else {
                        GData3 gdt = new GData3(-1, 0.95f, 0.95f, 0.90f, 1f, (float) (point1.coord(0) * scale), (float) (point1.coord(1) * scale), 0f, (float) (point3.coord(0) * scale),
                                (float) (point3.coord(1) * scale), 0f, (float) (point2.coord(0) * scale), (float) (point2.coord(1) * scale), 0f, parent, datFile, true);
                        anchor.setNext(gdt);
                        anchor = gdt;
                        finalTriangleSet.add(gdt);
                    }

                }

            }
        }

        return finalTriangleSet;
    }

    public static Set<PGData3> triangulateGLText(org.eclipse.swt.graphics.Font font, final String text, final double flatness, final double interpolateFlatness, float fontHeight) {
        String[] ff = font.getFontData()[0].getName().split(Pattern.quote("-")); //$NON-NLS-1$
        String fontName;
        if (ff.length > 1) {
            fontName = ff[1];
        } else {
            fontName = ff[0];
        }
        Font myFont = new Font(fontName, Font.BOLD | Font.HANGING_BASELINE, 8);
        final GlyphVector vector = myFont.createGlyphVector(new FontRenderContext(null, false, false), text);

        final Set<PGData3> finalTriangleSet = new HashSet<>();

        if (vector.getNumGlyphs() == 0)
            return finalTriangleSet;

        double maxHeight = 0d;
        for (int i = 0; i < vector.getNumGlyphs(); i++) {
            double height = vector.getGlyphMetrics(0).getBounds2D().getHeight();
            if (height > maxHeight)
                maxHeight = height;
        }

        final double scale = fontHeight / maxHeight;
        for (int j = 0; j < vector.getNumGlyphs(); j++) {
            Shape characterShape = vector.getGlyphOutline(j);
            Set<PGData3> characterTriangleSet = triangulateGLShape(characterShape, flatness, interpolateFlatness, scale);
            finalTriangleSet.addAll(characterTriangleSet);
        }
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        final Set<PGData3> finalTriangleSet2 = new HashSet<>();
        for (PGData3 tri : finalTriangleSet) {
            minX = Math.min(minX, tri.x1);
            minX = Math.min(minX, tri.x2);
            minX = Math.min(minX, tri.x3);
            minY = Math.min(minY, tri.y1);
            minY = Math.min(minY, tri.y2);
            minY = Math.min(minY, tri.y3);
        }
        for (PGData3 tri : finalTriangleSet) {
            finalTriangleSet2.add(new PGData3(
                    tri.x1 + minX - minY, tri.y1 + minY, 0f,
                    tri.x2 + minX - minY, tri.y2 + minY, 0f,
                    tri.x3 + minX - minY, tri.y3 + minY, 0f));
        }
        return finalTriangleSet2;
    }

    public static Set<PGData3> triangulateGLText(org.eclipse.swt.graphics.Font font, final String text, final double flatness, final double interpolateFlatness) {
        String[] ff = font.getFontData()[0].getName().split(Pattern.quote("-")); //$NON-NLS-1$
        String fontName;
        if (ff.length > 1) {
            fontName = ff[1];
        } else {
            fontName = ff[0];
        }
        Font myFont = new Font(fontName, Font.BOLD | Font.HANGING_BASELINE, 8);
        final GlyphVector vector = myFont.createGlyphVector(new FontRenderContext(null, false, false), text);

        final Set<PGData3> finalTriangleSet = new HashSet<>();

        if (vector.getNumGlyphs() == 0)
            return finalTriangleSet;

        for (int j = 0; j < vector.getNumGlyphs(); j++) {
            Shape characterShape = vector.getGlyphOutline(j);
            Set<PGData3> characterTriangleSet = triangulateGLShape(characterShape, flatness, interpolateFlatness, .002f);
            finalTriangleSet.addAll(characterTriangleSet);
        }
        return finalTriangleSet;
    }

    private static Set<PGData3> triangulateGLShape(Shape shape, double flatness, double interpolateFlatness, double scale) {
        PathIterator shapePathIterator = shape.getPathIterator(null, flatness);
        /*
         * Add all points of the shape to the triangulation
         */
        double x = 0;
        double y = 0;
        double px;
        double py;

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;

        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        List<Pnt> places = new ArrayList<>();

        while (!shapePathIterator.isDone()) {
            px = x;
            py = y;

            double[] args = new double[6];

            switch (shapePathIterator.currentSegment(args)) {
            case PathIterator.SEG_MOVETO:
                x = args[0];
                y = args[1];
                break;
            case PathIterator.SEG_LINETO:
                x = args[0];
                y = args[1];

                if (px == x && py == y)
                    break;

                Pnt p1 = new Pnt(px, py);
                Pnt p2 = new Pnt(x, y);

                places.add(p1);
                if (px > maxX)
                    maxX = px;
                if (py > maxY)
                    maxY = py;
                if (px < minX)
                    minX = px;
                if (py < minY)
                    minY = py;

                Pnt lengthVector = new Pnt(x - px, y - py);

                // sqrt( x^2 + y^2 )
                double length = Math.sqrt(lengthVector.coord(0) * lengthVector.coord(0) + lengthVector.coord(1) * lengthVector.coord(1));

                double num = length / interpolateFlatness;
                double nx = lengthVector.coord(0) / num;
                double ny = lengthVector.coord(1) / num;

                if (num > 1) {
                    double start = (num - Math.floor(num)) / 2.0;

                    double cx = p1.coord(0);
                    double cy = p1.coord(1);
                    double ll = length;
                    ll = ll - start;
                    while (ll > interpolateFlatness) {
                        places.add(new Pnt(cx, cy));
                        if (cx > maxX)
                            maxX = cx;
                        if (cy > maxY)
                            maxY = cy;
                        if (cx < minX)
                            minX = cx;
                        if (cy < minY)
                            minY = cy;

                        cx = cx + nx;
                        cy = cy + ny;

                        ll = ll - interpolateFlatness;
                    }
                }
                places.add(p2);
                if (x > maxX)
                    maxX = x;
                if (y > maxY)
                    maxY = y;
                if (x < minX)
                    minX = x;
                if (y < minY)
                    minY = y;
                break;
            case PathIterator.SEG_QUADTO:
                break;
            case PathIterator.SEG_CUBICTO:
                break;
            case PathIterator.SEG_CLOSE:
                break;
            default:
                break;
            }

            shapePathIterator.next();
        }

        /*
         * Build the triangle which encompasses the shape (-0.5,+1) - (+0.5,+1)
         * - (0,-1)
         */
        double trisize = 100000d;
        Triangle tri = new Triangle(new Pnt(-trisize / 2.0, trisize), new Pnt(+trisize / 2.0, trisize), new Pnt(0, -trisize));

        // now add all triangles which are in the shape to the set
        Set<PGData3> finalTriangleSet = new HashSet<>();

        if (!places.isEmpty()) {

            // initialize the triangulation with this triangle
            Triangulation triangulation = new Triangulation(tri);

            triangulation.delaunayPlace(new Pnt(minX - 0.0, minY - 0.0));
            triangulation.delaunayPlace(new Pnt(maxX + 0.0, minY - 0.0));
            triangulation.delaunayPlace(new Pnt(maxX + 0.0, maxY + 0.0));
            triangulation.delaunayPlace(new Pnt(minX - 0.0, maxY + 0.0));

            for (Pnt place : places) {
                triangulation.delaunayPlace(place);
                if (triangulation.size() > 4000) break;
            }

            for (Triangle triangle : triangulation) {
                Pnt point1 = triangle.get(0);
                Pnt point2 = triangle.get(1);
                Pnt point3 = triangle.get(2);
                double midX = point1.coord(0);
                double midY = point1.coord(1);
                midX = midX + point2.coord(0);
                midY = midY + point2.coord(1);
                midX = midX + point3.coord(0);
                midY = midY + point3.coord(1);
                midX /= 3.0;
                midY /= 3.0;
                if (shape.contains(midX, midY)) {
                    double[] vec1 = new double[] { point3.coord(0) - point1.coord(0), point3.coord(1) - point1.coord(1) };
                    double[] vec2 = new double[] { point3.coord(0) - point2.coord(0), point3.coord(1) - point2.coord(1) };
                    double wind = vec1[0] * vec2[1] - vec1[1] * vec2[0];
                    if (wind < 0) {
                        PGData3 gdt = new PGData3((float) (point1.coord(0) * scale), (float) (point1.coord(1) * scale), 0f, (float) (point2.coord(0) * scale),
                                (float) (point2.coord(1) * scale), 0f, (float) (point3.coord(0) * scale), (float) (point3.coord(1) * scale), 0f);
                        finalTriangleSet.add(gdt);
                    } else {
                        PGData3 gdt = new PGData3((float) (point1.coord(0) * scale), (float) (point1.coord(1) * scale), 0f, (float) (point3.coord(0) * scale),
                                (float) (point3.coord(1) * scale), 0f, (float) (point2.coord(0) * scale), (float) (point2.coord(1) * scale), 0f);
                        finalTriangleSet.add(gdt);
                    }
                }
            }
        }
        return finalTriangleSet;
    }
}