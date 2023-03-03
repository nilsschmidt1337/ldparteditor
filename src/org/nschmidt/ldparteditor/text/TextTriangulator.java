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
import org.tinfour.constrained.delaunay.IncrementalTin;
import org.tinfour.constrained.delaunay.Pnt;
import org.tinfour.constrained.delaunay.PolygonConstraint;
import org.tinfour.constrained.delaunay.SimpleTriangle;

public enum TextTriangulator {
    INSTANCE;

    public static Set<GData> triangulateText(Font font, final float r, final float g, final float b, final String text, final double flatness, final GData1 parent, final DatFile datFile, int fontHeight, int mode) {
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
                        if (mode == 2) {
                            NLogger.debug(TextTriangulator.class, "Triangulating {0}", text); //$NON-NLS-1$
                            List<Shape> shapes = new ArrayList<>();
                            for (int j = 0; j < vector.getNumGlyphs(); j++) {
                                shapes.add(vector.getGlyphOutline(j));
                            }
                            Set<GData> characterTriangleSet = triangulateShape(monitor, shapes, parent, datFile, flatness, scale, r, g, b, mode);
                            NLogger.debug(TextTriangulator.class, "Triangulating [Done] {0}", text); //$NON-NLS-1$
                            
                            synchronized (finalTriangleSet) {
                                finalTriangleSet.addAll(characterTriangleSet);
                            }
                        } else {
                            for (int j = 0; j < vector.getNumGlyphs(); j++) {
                                final int[] i = new int[1];
                                i[0] = j;
                                threads[j] = new Thread(() -> {
                                    Shape characterShape = vector.getGlyphOutline(i[0]);
                                    NLogger.debug(TextTriangulator.class, "Triangulating {0}", text.charAt(i[0])); //$NON-NLS-1$
                                    List<Shape> shapes = new ArrayList<>();
                                    shapes.add(characterShape);
                                    Set<GData> characterTriangleSet = triangulateShape(monitor, shapes, parent, datFile, flatness, scale, r, g, b, mode);
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
                    } catch (Exception ex) {
                        NLogger.error(TextTriangulator.class, ex);
                        throw ex;
                    }
                    finally
                    {
                        monitor.done();
                    }
                }
            });
        }
        catch (InvocationTargetException ite) {
            NLogger.error(TextTriangulator.class, ite);
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

    private static Set<GData> triangulateShape(IProgressMonitor monitor, List<Shape> shapes, GData1 parent, DatFile datFile, double flatness, double scale, float r, float g,
            float b, int mode) {
        
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        
        // now add all triangles which are in the shape to the set
        Set<GData> finalTriangleSet = new HashSet<>();
        
        /*
         * Add all polygons of the shape to the triangulation
         */
        final IncrementalTin tin = new IncrementalTin(); 
        final List<PolygonConstraint> outlines = new ArrayList<>();

        // Loop on characters and generate constraints.
        for (Shape shape : shapes) {
            PathIterator path = shape.getPathIterator(null, flatness);
            double[] d = new double[6];
            double px;
            double py;
            List<Pnt> vList = new ArrayList<>();
            while (!path.isDone()) {
                int flag = path.currentSegment(d);
                switch (flag) {
                case PathIterator.SEG_MOVETO:
                    vList.clear();
                    vList.add(new Pnt(d[0], d[1]));
                    px = d[0];
                    py = d[1];
                    if (px > maxX)
                      maxX = px;
                    if (py > maxY)
                      maxY = py;
                    if (px < minX)
                      minX = px;
                    if (py < minY)
                      minY = py;
                    break;
                case PathIterator.SEG_LINETO:
                    vList.add(new Pnt(d[0], d[1]));
                    px = d[0];
                    py = d[1];
                    if (px > maxX)
                      maxX = px;
                    if (py > maxY)
                      maxY = py;
                    if (px < minX)
                      minX = px;
                    if (py < minY)
                      minY = py;
                    break;
                case PathIterator.SEG_CLOSE:
                    PolygonConstraint poly = new PolygonConstraint();
                    int n = vList.size();
                    for (int i = n - 1; i >= 0; i--) {
                        Pnt v = vList.get(i);
                        poly.add(v);
                    }
                    poly.complete();
                    if (Math.abs(poly.getArea()) < 0.00001d) break;
                    outlines.add(poly);
                    break;
                default:
                    break;
                }
                
                path.next();
            }
        }
        
        if (!outlines.isEmpty()) {
            PolygonConstraint poly = new PolygonConstraint();
            poly.add(new Pnt(minX - (maxX - minX) * 0.01, minY - (maxY - minY) * 0.01));
            poly.add(new Pnt(minX - (maxX - minX) * 0.01, maxY + (maxY - minY) * 0.01));
            poly.add(new Pnt(maxX + (maxX - minX) * 0.01, maxY + (maxY - minY) * 0.01));
            poly.add(new Pnt(maxX + (maxX - minX) * 0.01, minY - (maxY - minY) * 0.01));
            outlines.add(poly);
        }
        
        tin.addConstraints(outlines, true);

        if (tin.isBootstrapped()) {
              GData anchor = new GData0(null, View.DUMMY_REFERENCE);
              int newTriangleCount = 0;
              int limit = 4000 * shapes.size();
              for (SimpleTriangle triangle : tin.triangles()) {
                  newTriangleCount += 1;
                  if (newTriangleCount > limit || monitor.isCanceled()) break;

                  Pnt point1 = triangle.getVertexA();
                  Pnt point2 = triangle.getVertexB();
                  Pnt point3 = triangle.getVertexC();

                  double midX = point1.x;
                  double midY = point1.y;
                  midX = midX + point2.x;
                  midY = midY + point2.y;
                  midX = midX + point3.x;
                  midY = midY + point3.y;

                  midX /= 3.0;
                  midY /= 3.0;

                  if (shapesContains(shapes, midX, midY)) {
                      double[] vec1 = new double[] { point3.x - point1.x, point3.y - point1.y };
                      double[] vec2 = new double[] { point3.x - point2.x, point3.y - point2.y };
                      double wind = vec1[0] * vec2[1] - vec1[1] * vec2[0];
                      if (wind < 0) {
                          GData3 gdt = new GData3(-1, r, g, b, 1f, (float) (point1.x * scale), (float) (point1.y * scale), 0f, (float) (point2.x * scale),
                                  (float) (point2.y * scale), 0f, (float) (point3.x * scale), (float) (point3.y * scale), 0f, parent, datFile, true);
                          anchor.setNext(gdt);
                          anchor = gdt;
                          finalTriangleSet.add(gdt);
                      } else {
                          GData3 gdt = new GData3(-1, r, g, b, 1f, (float) (point1.x * scale), (float) (point1.y * scale), 0f, (float) (point3.x * scale),
                                  (float) (point3.y * scale), 0f, (float) (point2.x * scale), (float) (point2.y * scale), 0f, parent, datFile, true);
                          anchor.setNext(gdt);
                          anchor = gdt;
                          finalTriangleSet.add(gdt);
                      }
                  } else if (mode != 1) {
                      double[] vec1 = new double[] { point3.x - point1.x, point3.y - point1.y };
                      double[] vec2 = new double[] { point3.x - point2.x, point3.y - point2.y };
                      double wind = vec1[0] * vec2[1] - vec1[1] * vec2[0];
                      if (wind < 0) {
                          GData3 gdt = new GData3(-1, 0.95f, 0.95f, 0.90f, 1f, (float) (point1.x * scale), (float) (point1.y * scale), 0f, (float) (point2.x * scale),
                                  (float) (point2.y * scale), 0f, (float) (point3.x * scale), (float) (point3.y * scale), 0f, parent, datFile, true);
                          anchor.setNext(gdt);
                          anchor = gdt;
                          finalTriangleSet.add(gdt);
                      } else {
                          GData3 gdt = new GData3(-1, 0.95f, 0.95f, 0.90f, 1f, (float) (point1.x * scale), (float) (point1.y * scale), 0f, (float) (point3.x * scale),
                                  (float) (point3.y * scale), 0f, (float) (point2.x * scale), (float) (point2.y * scale), 0f, parent, datFile, true);
                          anchor.setNext(gdt);
                          anchor = gdt;
                          finalTriangleSet.add(gdt);
                      }
                  }

              }
        }

        if (monitor.isCanceled()) finalTriangleSet.clear();
        return finalTriangleSet;
    }

    static boolean shapesContains(List<Shape> shapes, double x, double y) {
        for (Shape shape : shapes) {
            if (shape.contains(x, y)) return true;
        }
        
        return false;
    }

    public static Set<PGData3> triangulateGLText(org.eclipse.swt.graphics.Font font, final String text, final double flatness, float fontHeight) {
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
            Set<PGData3> characterTriangleSet = triangulateGLShape(characterShape, flatness, scale);
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

    public static Set<PGData3> triangulateGLText(org.eclipse.swt.graphics.Font font, final String text, final double flatness) {
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
            Set<PGData3> characterTriangleSet = triangulateGLShape(characterShape, flatness, .002f);
            finalTriangleSet.addAll(characterTriangleSet);
        }
        return finalTriangleSet;
    }

    private static Set<PGData3> triangulateGLShape(Shape shape, double flatness, double scale) {
        /*
         * Add all polygons of the shape to the triangulation
         */
        final IncrementalTin tin = new IncrementalTin(); 
        final List<PolygonConstraint> outlines = new ArrayList<>();

        // Loop on characters and generate constraints.
        PathIterator path = shape.getPathIterator(null, flatness);
        double[] d = new double[6];
        List<Pnt> vList = new ArrayList<>();
        while (!path.isDone()) {
            int flag = path.currentSegment(d);
            switch (flag) {
            case PathIterator.SEG_MOVETO:
                vList.clear();
                vList.add(new Pnt(d[0], d[1]));
                break;
            case PathIterator.SEG_LINETO:
                vList.add(new Pnt(d[0], d[1]));
                break;
            case PathIterator.SEG_CLOSE:
                PolygonConstraint poly = new PolygonConstraint();
                int n = vList.size();
                for (int i = n - 1; i >= 0; i--) {
                    Pnt v = vList.get(i);
                    poly.add(v);
                }
                poly.complete();
                if (Math.abs(poly.getArea()) < 0.00001d) break;
                outlines.add(poly);
                break;
            default:
                break;
            }
            
            path.next();
        }
        
        tin.addConstraints(outlines, true);

        // now add all triangles which are in the shape to the set
        Set<PGData3> finalTriangleSet = new HashSet<>();

        if (tin.isBootstrapped()) {
            for (SimpleTriangle triangle : tin.triangles()) {
                Pnt point1 = triangle.getVertexA();
                Pnt point2 = triangle.getVertexB();
                Pnt point3 = triangle.getVertexC();
                double midX = point1.x;
                double midY = point1.y;
                midX = midX + point2.x;
                midY = midY + point2.y;
                midX = midX + point3.x;
                midY = midY + point3.y;
                midX /= 3.0;
                midY /= 3.0;
                if (shape.contains(midX, midY)) {
                    double[] vec1 = new double[] { point3.x - point1.x, point3.y - point1.y };
                    double[] vec2 = new double[] { point3.x - point2.x, point3.y - point2.y };
                    double wind = vec1[0] * vec2[1] - vec1[1] * vec2[0];
                    if (wind < 0) {
                        PGData3 gdt = new PGData3((float) (point1.x * scale), (float) (point1.y * scale), 0f, (float) (point2.x * scale),
                                (float) (point2.y * scale), 0f, (float) (point3.x * scale), (float) (point3.y * scale), 0f);
                        finalTriangleSet.add(gdt);
                    } else {
                        PGData3 gdt = new PGData3((float) (point1.x * scale), (float) (point1.y * scale), 0f, (float) (point3.x * scale),
                                (float) (point3.y * scale), 0f, (float) (point2.x * scale), (float) (point2.y * scale), 0f);
                        finalTriangleSet.add(gdt);
                    }
                }
            }
        }
        return finalTriangleSet;
    }
}