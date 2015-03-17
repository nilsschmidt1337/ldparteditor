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
package org.nschmidt.ldparteditor.enums;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lwjgl.util.vector.Matrix4f;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.DatType;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.Matrix;
import org.nschmidt.ldparteditor.data.PGData3;
import org.nschmidt.ldparteditor.data.colour.GCChrome;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.text.TextTriangulator;
import org.nschmidt.ldparteditor.text.UTF8BufferedReader;

/**
 * This class provides constants for the 3D view
 *
 * @author nils
 *
 */
public enum View {
    INSTANCE;

    /** Resolution of the viewport at 100% zoom */
    public final static float PIXEL_PER_LDU = 1000.0f;
    /** i18n reference string for the current unit */
    public static String unit = "LDU"; //$NON-NLS-1$
    /** The current unit factor */
    public static BigDecimal unit_factor = BigDecimal.ONE;
    /** The standard decimal format for floating point numbers (0 digits) */
    public static final java.text.DecimalFormat NUMBER_FORMAT0F = new java.text.DecimalFormat("###,##0;-###,##0"); //$NON-NLS-1$
    /** The standard decimal format for floating point numbers (2 digits) */
    public static final java.text.DecimalFormat NUMBER_FORMAT2F = new java.text.DecimalFormat(" ###,##0.00;-###,##0.00"); //$NON-NLS-1$
    /** The standard decimal format for floating point numbers (4 digits) */
    public static final java.text.DecimalFormat NUMBER_FORMAT4F = new java.text.DecimalFormat("###,##0.0000;-###,##0.0000"); //$NON-NLS-1$

    public static final float[] BFC_front_Colour_r = new float[] { 0f };
    public static final float[] BFC_front_Colour_g = new float[] { .9f };
    public static final float[] BFC_front_Colour_b = new float[] { 0f };

    public static final float[] BFC_back__Colour_r = new float[] { .9f };
    public static final float[] BFC_back__Colour_g = new float[] { 0f };
    public static final float[] BFC_back__Colour_b = new float[] { 0f };

    public static final float[] BFC_uncertified__Colour_r = new float[] { 0f };
    public static final float[] BFC_uncertified__Colour_g = new float[] { 0f };
    public static final float[] BFC_uncertified__Colour_b = new float[] { 1f };

    public static final float[] vertex_Colour_r = new float[] { 0.75f };
    public static final float[] vertex_Colour_g = new float[] { 0.75f };
    public static final float[] vertex_Colour_b = new float[] { 0.05f };

    public static final float[] vertex_selected_Colour_r = new float[] { 0.75f };
    public static final float[] vertex_selected_Colour_g = new float[] { 0.05f };
    public static final float[] vertex_selected_Colour_b = new float[] { 0.05f };

    public static final float[] condline_selected_Colour_r = new float[] { 0.75f };
    public static final float[] condline_selected_Colour_g = new float[] { 0.35f };
    public static final float[] condline_selected_Colour_b = new float[] { 0.05f };

    public static final float[] line_Colour_r = new float[] { 0f };
    public static final float[] line_Colour_g = new float[] { 0f };
    public static final float[] line_Colour_b = new float[] { 0f };

    public static final float[] manipulator_selected_Colour_r = new float[] { 0.75f };
    public static final float[] manipulator_selected_Colour_g = new float[] { 0.75f };
    public static final float[] manipulator_selected_Colour_b = new float[] { 0f };

    public static final float[] lineWidth1000 = new float[] { 100f };
    public static final float[] lineWidth = new float[] { .100f };
    public static final float[] lineWidthGL = new float[] { 1.5f };


    public final static Set<PGData3> FRONT = TextTriangulator.triangulateGLText(Font.MONOSPACE, I18n.PERSPECTIVE_FRONT, 0.07, 0.3, .012f, 16.9);
    public final static Set<PGData3> BACK = TextTriangulator.triangulateGLText(Font.MONOSPACE, I18n.PERSPECTIVE_BACK, 0.07, 0.3, .012f, 16.9);
    public final static Set<PGData3> BOTTOM = TextTriangulator.triangulateGLText(Font.MONOSPACE, I18n.PERSPECTIVE_BOTTOM, 0.07, 0.3, .012f, 16.9);
    public final static Set<PGData3> LEFT = TextTriangulator.triangulateGLText(Font.MONOSPACE, I18n.PERSPECTIVE_LEFT, 0.07, 0.3, .012f, 16.9);
    public final static Set<PGData3> RIGHT = TextTriangulator.triangulateGLText(Font.MONOSPACE, I18n.PERSPECTIVE_RIGHT, 0.07, 0.3, .012f, 16.9);
    public final static Set<PGData3> TOP = TextTriangulator.triangulateGLText(Font.MONOSPACE, I18n.PERSPECTIVE_TOP, 0.07, 0.3, .012f, 16.9);

    private static final GColour BLACK = new GColour(-1, 0f, 0f, 0f, 1f);

    public static final int NUM_CORES = Runtime.getRuntime().availableProcessors();

    public static final Matrix4f ID = Matrix4f.setIdentity(new Matrix4f());
    public static final GData1 DUMMY_REFERENCE = new GData1();
    public static final DatFile DUMMY_DATFILE = new DatFile(File.separator + File.separator + "(no file selected)", "DUMMY FILE", true, DatType.PART); //$NON-NLS-1$ //$NON-NLS-2$ I18N

    public static final Matrix ACCURATE_ID = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE);

    private static final HashMap<Integer, GColour> colourFromIndex = new HashMap<Integer, GColour>();
    private static final HashMap<Integer, GColour> edgeColourFromIndex = new HashMap<Integer, GColour>();
    private static final HashMap<Integer, String> colourNameFromIndex = new HashMap<Integer, String>();

    private static final HashMap<IndexedEntry, Integer> indexFromColour = new HashMap<IndexedEntry, Integer>();

    public static final Locale LOCALE = Locale.getDefault();

    public static final GColour getLDConfigColour(int index) {
        GColour result =  colourFromIndex.get(index);
        if (result == null) result = new GColour(index, 0f, 0f, 0f, 1f);
        return result;
    }

    public static final boolean hasLDConfigColour(int index) {
        GColour result = colourFromIndex.get(index);
        return result != null;
    }

    public static final GColour getLDConfigEdgeColour(int index, Composite3D c3d) {
        if (c3d.isBlackEdges()) return BLACK;
        GColour result = edgeColourFromIndex.get(index);
        if (result == null) result = new GColour(index, 0f, 0f, 0f, 1f);
        return result;
    }

    public static String getLDConfigColourName(Integer index) {
        String result = colourNameFromIndex.get(index);
        if (result == null) result = "<???>"; //$NON-NLS-1$
        return colourNameFromIndex.get(index);
    }

    public static final void setLDConfigColour(int index, GColour col) {
        colourFromIndex.put(index, col);
    }

    public static final HashMap<Integer, GColour> getColourMap() {
        return colourFromIndex;
    }

    public static final HashMap<Integer, String> getNameMap() {
        return colourNameFromIndex;
    }

    /**
     *
     * @param r
     * @param g
     * @param b
     * @return {@code -1} if the index was not found
     */
    public static final int getLDConfigIndex(float r, float g, float b) {
        IndexedEntry e = new IndexedEntry(r,g,b);
        if (indexFromColour.containsKey(e)) {
            return indexFromColour.get(e);
        }
        return -1;
    }

    private static class IndexedEntry {

        final float r;
        final float g;
        final float b;

        public IndexedEntry(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Float.floatToIntBits(b);
            result = prime * result + Float.floatToIntBits(g);
            result = prime * result + Float.floatToIntBits(r);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            IndexedEntry other = (IndexedEntry) obj;
            if (Float.floatToIntBits(b) != Float.floatToIntBits(other.b))
                return false;
            if (Float.floatToIntBits(g) != Float.floatToIntBits(other.g))
                return false;
            if (Float.floatToIntBits(r) != Float.floatToIntBits(other.r))
                return false;
            return true;
        }

    }

    public static final boolean loadLDConfig(String location) {
        if (new File(location).exists()) {
            indexFromColour.clear();
            edgeColourFromIndex.clear();
            colourFromIndex.clear();
            colourNameFromIndex.clear();
            Pattern p = Pattern.compile("ALPHA\\s+\\d+"); //$NON-NLS-1$
            try {
                indexFromColour.put(new IndexedEntry(View.line_Colour_r[0], View.line_Colour_g[0], View.line_Colour_b[0]), 24);
                UTF8BufferedReader reader = new UTF8BufferedReader(location);
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    String[] data_segments = line.trim().split("\\s+"); //$NON-NLS-1$
                    if (data_segments.length > 6) {
                        if ("!COLOUR".equals(data_segments[1])) { //$NON-NLS-1$
                            int index = Integer.parseInt(data_segments[4]);
                            float R = Integer.parseInt(data_segments[6].substring(1, 3), 16) / 255f + .000001f * index;
                            float G = Integer.parseInt(data_segments[6].substring(3, 5), 16) / 255f + .000001f * index;
                            float B = Integer.parseInt(data_segments[6].substring(5, 7), 16) / 255f + .000001f * index;

                            float R2 = Integer.parseInt(data_segments[8].substring(1, 3), 16) / 255f;
                            float G2 = Integer.parseInt(data_segments[8].substring(3, 5), 16) / 255f;
                            float B2 = Integer.parseInt(data_segments[8].substring(5, 7), 16) / 255f;

                            Matcher m = p.matcher(line);

                            if (m.find()) {
                                String alphaStr = m.group().replaceAll("ALPHA", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
                                float alpha = Float.parseFloat(alphaStr) / 255f;
                                GColour colour = new GColour(index, R, G, B, alpha);
                                colourFromIndex.put(index, colour);
                            } else if (line.contains(" CHROME")) { //$NON-NLS-1$
                                GColour colour = new GColour(index, R, G, B, 1f, new GCChrome());
                                colourFromIndex.put(index, colour);
                            } else {
                                GColour colour = new GColour(index, R, G, B, 1f);
                                colourFromIndex.put(index, colour);
                            }
                            indexFromColour.put(new IndexedEntry(R, G, B), index);
                            edgeColourFromIndex.put(index, new GColour(index, R2, G2, B2, 1f));
                            colourNameFromIndex.put(index, data_segments[2].replaceAll("_", " ")); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    }
                }
                reader.close();
                return true;
            } catch (Exception e) {
            }
        }
        return false;
    }

}
