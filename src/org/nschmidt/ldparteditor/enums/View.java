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
import org.nschmidt.ldparteditor.data.colour.GCGlitter;
import org.nschmidt.ldparteditor.data.colour.GCMatteMetal;
import org.nschmidt.ldparteditor.data.colour.GCMetal;
import org.nschmidt.ldparteditor.data.colour.GCPearl;
import org.nschmidt.ldparteditor.data.colour.GCRubber;
import org.nschmidt.ldparteditor.data.colour.GCSpeckle;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.text.LDParsingException;
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
    public static final float PIXEL_PER_LDU = 1000.0f;
    /** i18n reference string for the current unit */
    public static String unit = "LDU"; //$NON-NLS-1$
    /** The current unit factor */
    public static BigDecimal unitFactor = BigDecimal.ONE;
    /** threshold for "solid" edges (default: 5e-6f) */
    public static float edgeThreshold = 5e-6f;
    /** The standard decimal format for floating point numbers (0 digits) */
    public static final String NUMBER_FORMAT0F = "###,##0;-###,##0"; //$NON-NLS-1$
    /** The standard decimal format for floating point numbers (1 digit) */
    public static final String NUMBER_FORMAT1F = "###,##0.0;-###,##0.0"; //$NON-NLS-1$
    /** The standard decimal format for floating point numbers (2 digits) */
    public static final String NUMBER_FORMAT2F = " ###,##0.00;-###,##0.00"; //$NON-NLS-1$
    /** The standard decimal format for floating point numbers (4 digits) */
    public static final String NUMBER_FORMAT4F = "###,##0.0000;-###,##0.0000"; //$NON-NLS-1$
    /** The standard decimal format for floating point numbers (8 digits) */
    public static final String NUMBER_FORMAT8F = "###,##0.00000000;-###,##0.00000000"; //$NON-NLS-1$
    /** The decimal format for floating point numbers with four leading zeros (4 digits) */
    public static final String NUMBER_FORMATL4F = " ###,##0000.0000;-###,##0000.0000"; //$NON-NLS-1$

    public static final GColour RANDOM_COLOUR = new GColour(-1, 1f, 1f, 1f, 0f);

    public static final float[] COLOUR16_OVERRIDE_R = new float[] { 0f };
    public static final float[] COLOUR16_OVERRIDE_G = new float[] { 0f };
    public static final float[] COLOUR16_OVERRIDE_B = new float[] { 0f };

    public static final float[] BFC_FRONT_COLOUR_R = new float[] { 0f };
    public static final float[] BFC_FRONT_COLOUR_G = new float[] { .9f };
    public static final float[] BFC_FRONT_COLOUR_B = new float[] { 0f };

    public static final float[] BFC_BACK__COLOUR_R = new float[] { .9f };
    public static final float[] BFC_BACK__COLOUR_G = new float[] { 0f };
    public static final float[] BFC_BACK__COLOUR_B = new float[] { 0f };

    public static final float[] BFC_UNCERTIFIED_COLOUR_R = new float[] { 0f };
    public static final float[] BFC_UNCERTIFIED_COLOUR_G = new float[] { 0f };
    public static final float[] BFC_UNCERTIFIED_COLOUR_B = new float[] { 1f };

    public static final float[] VERTEX_COLOUR_R = new float[] { 0.118f };
    public static final float[] VERTEX_COLOUR_G = new float[] { 0.565f };
    public static final float[] VERTEX_COLOUR_B = new float[] { 1f };

    public static final float[] VERTEX_SELECTED_COLOUR_R = new float[] { 0.75f };
    public static final float[] VERTEX_SELECTED_COLOUR_G = new float[] { 0.05f };
    public static final float[] VERTEX_SELECTED_COLOUR_B = new float[] { 0.05f };

    public static final float[] CONDLINE_SELECTED_COLOUR_R = new float[] { 0.75f };
    public static final float[] CONDLINE_SELECTED_COLOUR_G = new float[] { 0.35f };
    public static final float[] CONDLINE_SELECTED_COLOUR_B = new float[] { 0.05f };

    public static final float[] LINE_COLOUR_R = new float[] { 0f };
    public static final float[] LINE_COLOUR_G = new float[] { 0f };
    public static final float[] LINE_COLOUR_B = new float[] { 0f };

    public static final float[] MESHLINE_COLOUR_R = new float[] { 0f };
    public static final float[] MESHLINE_COLOUR_G = new float[] { 0f };
    public static final float[] MESHLINE_COLOUR_B = new float[] { 0f };

    public static final float[] CONDLINE_COLOUR_R = new float[] { 0f };
    public static final float[] CONDLINE_COLOUR_G = new float[] { 0f };
    public static final float[] CONDLINE_COLOUR_B = new float[] { 0f };

    public static final float[] CONDLINE_HIDDEN_COLOUR_R = new float[] { 1f };
    public static final float[] CONDLINE_HIDDEN_COLOUR_G = new float[] { .44f };
    public static final float[] CONDLINE_HIDDEN_COLOUR_B = new float[] { .1f };

    public static final float[] CONDLINE_SHOWN_COLOUR_R = new float[] { .553f };
    public static final float[] CONDLINE_SHOWN_COLOUR_G = new float[] { .22f };
    public static final float[] CONDLINE_SHOWN_COLOUR_B = new float[] { 1f };

    public static final float[] CURSOR1_COLOUR_R = new float[] { 1f };
    public static final float[] CURSOR1_COLOUR_G = new float[] { 0f };
    public static final float[] CURSOR1_COLOUR_B = new float[] { 0f };

    public static final float[] CURSOR2_COLOUR_R = new float[] { 0f };
    public static final float[] CURSOR2_COLOUR_G = new float[] { 0f };
    public static final float[] CURSOR2_COLOUR_B = new float[] { 1f };

    public static final float[] BACKGROUND_COLOUR_R = new float[] { 1f };
    public static final float[] BACKGROUND_COLOUR_G = new float[] { 1f };
    public static final float[] BACKGROUND_COLOUR_B = new float[] { 1f };

    public static final float[] LIGHT1_COLOUR_R = new float[] { 0.85f };
    public static final float[] LIGHT1_COLOUR_G = new float[] { 0.85f };
    public static final float[] LIGHT1_COLOUR_B = new float[] { 0.85f };

    public static final float[] LIGHT1_SPECULAR_COLOUR_R = new float[] { 0.5f };
    public static final float[] LIGHT1_SPECULAR_COLOUR_G = new float[] { 0.5f };
    public static final float[] LIGHT1_SPECULAR_COLOUR_B = new float[] { 0.5f };

    public static final float[] LIGHT2_COLOUR_R = new float[] { 0.27f };
    public static final float[] LIGHT2_COLOUR_G = new float[] { 0.27f };
    public static final float[] LIGHT2_COLOUR_B = new float[] { 0.27f };

    public static final float[] LIGHT2_SPECULAR_COLOUR_R = new float[] { 0f };
    public static final float[] LIGHT2_SPECULAR_COLOUR_G = new float[] { 0f };
    public static final float[] LIGHT2_SPECULAR_COLOUR_B = new float[] { 0f };

    public static final float[] LIGHT3_COLOUR_R = new float[] { 0.27f };
    public static final float[] LIGHT3_COLOUR_G = new float[] { 0.27f };
    public static final float[] LIGHT3_COLOUR_B = new float[] { 0.27f };

    public static final float[] LIGHT3_SPECULAR_COLOUR_R = new float[] { 0f };
    public static final float[] LIGHT3_SPECULAR_COLOUR_G = new float[] { 0f };
    public static final float[] LIGHT3_SPECULAR_COLOUR_B = new float[] { 0f };

    public static final float[] LIGHT4_COLOUR_R = new float[] { 0.27f };
    public static final float[] LIGHT4_COLOUR_G = new float[] { 0.27f };
    public static final float[] LIGHT4_COLOUR_B = new float[] { 0.27f };

    public static final float[] LIGHT4_SPECULAR_COLOUR_R = new float[] { 0f };
    public static final float[] LIGHT4_SPECULAR_COLOUR_G = new float[] { 0f };
    public static final float[] LIGHT4_SPECULAR_COLOUR_B = new float[] { 0f };

    public static final float[] MANIPULATOR_SELECTED_COLOUR_R = new float[] { 0.75f };
    public static final float[] MANIPULATOR_SELECTED_COLOUR_G = new float[] { 0.75f };
    public static final float[] MANIPULATOR_SELECTED_COLOUR_B = new float[] { 0f };

    public static final float[] MANIPULATOR_INNERCIRCLE_COLOUR_R = new float[] { .3f };
    public static final float[] MANIPULATOR_INNERCIRCLE_COLOUR_G = new float[] { .3f };
    public static final float[] MANIPULATOR_INNERCIRCLE_COLOUR_B = new float[] { .3f };

    public static final float[] MANIPULATOR_OUTERCIRCLE_COLOUR_R = new float[] { .85f };
    public static final float[] MANIPULATOR_OUTERCIRCLE_COLOUR_G = new float[] { .85f };
    public static final float[] MANIPULATOR_OUTERCIRCLE_COLOUR_B = new float[] { .85f };

    public static final float[] MANIPULATOR_X_AXIS_COLOUR_R = new float[] { .5f };
    public static final float[] MANIPULATOR_X_AXIS_COLOUR_G = new float[] { 0f };
    public static final float[] MANIPULATOR_X_AXIS_COLOUR_B = new float[] { 0f };

    public static final float[] MANIPULATOR_Y_AXIS_COLOUR_R = new float[] { 0f };
    public static final float[] MANIPULATOR_Y_AXIS_COLOUR_G = new float[] { .5f };
    public static final float[] MANIPULATOR_Y_AXIS_COLOUR_B = new float[] { 0f };

    public static final float[] MANIPULATOR_Z_AXIS_COLOUR_R = new float[] { 0f };
    public static final float[] MANIPULATOR_Z_AXIS_COLOUR_G = new float[] { 0f };
    public static final float[] MANIPULATOR_Z_AXIS_COLOUR_B = new float[] { .5f };

    public static final float[] LINE_WIDTH_1000 = new float[] { 100f };
    public static final float[] LINE_WIDTH = new float[] { .100f };
    public static final float[] LINE_WIDTH_GL = new float[] { 1.5f };

    public static final float[] ADD_OBJECT_COLOUR_R = new float[] { 1f };
    public static final float[] ADD_OBJECT_COLOUR_G = new float[] { 0.6f };
    public static final float[] ADD_OBJECT_COLOUR_B = new float[] { 0f };

    public static final float[] ORIGIN_COLOUR_R = new float[] { 0f };
    public static final float[] ORIGIN_COLOUR_G = new float[] { 0f };
    public static final float[] ORIGIN_COLOUR_B = new float[] { 0f };

    public static final float[] GRID10_COLOUR_R = new float[] { .5f };
    public static final float[] GRID10_COLOUR_G = new float[] { .5f };
    public static final float[] GRID10_COLOUR_B = new float[] { .5f };

    public static final float[] GRID_COLOUR_R = new float[] { 0.15f };
    public static final float[] GRID_COLOUR_G = new float[] { 0.15f };
    public static final float[] GRID_COLOUR_B = new float[] { 0.15f };

    public static final float[] RUBBER_BAND_COLOUR_R = new float[] { 1f };
    public static final float[] RUBBER_BAND_COLOUR_G = new float[] { 0f };
    public static final float[] RUBBER_BAND_COLOUR_B = new float[] { 0f };

    public static final float[] TEXT_COLOUR_R = new float[] { 0f };
    public static final float[] TEXT_COLOUR_G = new float[] { 0f };
    public static final float[] TEXT_COLOUR_B = new float[] { 0f };

    public static final float[] X_AXIS_COLOUR_R = new float[] { 1f };
    public static final float[] X_AXIS_COLOUR_G = new float[] { 0f };
    public static final float[] X_AXIS_COLOUR_B = new float[] { 0f };

    public static final float[] Y_AXIS_COLOUR_R = new float[] { 0f };
    public static final float[] Y_AXIS_COLOUR_G = new float[] { 1f };
    public static final float[] Y_AXIS_COLOUR_B = new float[] { 0f };

    public static final float[] Z_AXIS_COLOUR_R = new float[] { 0f };
    public static final float[] Z_AXIS_COLOUR_G = new float[] { 0f };
    public static final float[] Z_AXIS_COLOUR_B = new float[] { 1f };

    public static final float[] PRIMITIVE_BACKGROUND_COLOUR_R = new float[] { 1f };
    public static final float[] PRIMITIVE_BACKGROUND_COLOUR_G = new float[] { 1f };
    public static final float[] PRIMITIVE_BACKGROUND_COLOUR_B = new float[] { 1f };

    public static final float[] PRIMITIVE_SIGN_FG_COLOUR_R = new float[] { .2f };
    public static final float[] PRIMITIVE_SIGN_FG_COLOUR_G = new float[] { .2f };
    public static final float[] PRIMITIVE_SIGN_FG_COLOUR_B = new float[] { 1f };

    public static final float[] PRIMITIVE_SIGN_BG_COLOUR_R = new float[] { 1f };
    public static final float[] PRIMITIVE_SIGN_BG_COLOUR_G = new float[] { 1f };
    public static final float[] PRIMITIVE_SIGN_BG_COLOUR_B = new float[] { 1f };

    public static final float[] PRIMITIVE_PLUS_N_MINUS_COLOUR_R = new float[] { 1f };
    public static final float[] PRIMITIVE_PLUS_N_MINUS_COLOUR_G = new float[] { 1f };
    public static final float[] PRIMITIVE_PLUS_N_MINUS_COLOUR_B = new float[] { 1f };

    public static final float[] PRIMITIVE_SELECTED_CELL_COLOUR_R = new float[] { 1f };
    public static final float[] PRIMITIVE_SELECTED_CELL_COLOUR_G = new float[] { .3f };
    public static final float[] PRIMITIVE_SELECTED_CELL_COLOUR_B = new float[] { .3f };

    public static final float[] PRIMITIVE_FOCUSED_CELL_COLOUR_R = new float[] { .6f };
    public static final float[] PRIMITIVE_FOCUSED_CELL_COLOUR_G = new float[] { .6f };
    public static final float[] PRIMITIVE_FOCUSED_CELL_COLOUR_B = new float[] { 1f };

    public static final float[] PRIMITIVE_NORMAL_CELL_COLOUR_R = new float[] { .3f };
    public static final float[] PRIMITIVE_NORMAL_CELL_COLOUR_G = new float[] { .3f };
    public static final float[] PRIMITIVE_NORMAL_CELL_COLOUR_B = new float[] { .3f };

    public static final float[] PRIMITIVE_CELL_1_COLOUR_R = new float[] { .7f };
    public static final float[] PRIMITIVE_CELL_1_COLOUR_G = new float[] { .7f };
    public static final float[] PRIMITIVE_CELL_1_COLOUR_B = new float[] { .7f };

    public static final float[] PRIMITIVE_CELL_2_COLOUR_R = new float[] { 1f };
    public static final float[] PRIMITIVE_CELL_2_COLOUR_G = new float[] { 1f };
    public static final float[] PRIMITIVE_CELL_2_COLOUR_B = new float[] { 1f };

    public static final float[] PRIMITIVE_CATEGORYCELL_1_COLOUR_R = new float[] { .6f };
    public static final float[] PRIMITIVE_CATEGORYCELL_1_COLOUR_G = new float[] { .4f };
    public static final float[] PRIMITIVE_CATEGORYCELL_1_COLOUR_B = new float[] { .3f };

    public static final float[] PRIMITIVE_CATEGORYCELL_2_COLOUR_R = new float[] { .7f };
    public static final float[] PRIMITIVE_CATEGORYCELL_2_COLOUR_G = new float[] { .5f };
    public static final float[] PRIMITIVE_CATEGORYCELL_2_COLOUR_B = new float[] { .4f };

    public static final float[] PRIMITIVE_EDGE_COLOUR_R = new float[] { 0f };
    public static final float[] PRIMITIVE_EDGE_COLOUR_G = new float[] { 0f };
    public static final float[] PRIMITIVE_EDGE_COLOUR_B = new float[] { 0f };

    public static final float[] PRIMITIVE_CONDLINE_COLOUR_R = new float[] { 0f };
    public static final float[] PRIMITIVE_CONDLINE_COLOUR_G = new float[] { 0f };
    public static final float[] PRIMITIVE_CONDLINE_COLOUR_B = new float[] { 1f };

    public static final Set<PGData3> FRONT = TextTriangulator.triangulateGLText(Font.MONOSPACE, I18n.PERSPECTIVE_FRONT, 0.07, 0.3, .012f * (1f + IconSize.getIconsize() / 4f));
    public static final Set<PGData3> BACK = TextTriangulator.triangulateGLText(Font.MONOSPACE, I18n.PERSPECTIVE_BACK, 0.07, 0.3, .012f * (1f + IconSize.getIconsize() / 4f));
    public static final Set<PGData3> BOTTOM = TextTriangulator.triangulateGLText(Font.MONOSPACE, I18n.PERSPECTIVE_BOTTOM, 0.07, 0.3, .012f * (1f + IconSize.getIconsize() / 4f));
    public static final Set<PGData3> LEFT = TextTriangulator.triangulateGLText(Font.MONOSPACE, I18n.PERSPECTIVE_LEFT, 0.07, 0.3, .012f * (1f + IconSize.getIconsize() / 4f));
    public static final Set<PGData3> RIGHT = TextTriangulator.triangulateGLText(Font.MONOSPACE, I18n.PERSPECTIVE_RIGHT, 0.07, 0.3, .012f * (1f + IconSize.getIconsize() / 4f));
    public static final Set<PGData3> TOP = TextTriangulator.triangulateGLText(Font.MONOSPACE, I18n.PERSPECTIVE_TOP, 0.07, 0.3, .012f * (1f + IconSize.getIconsize() / 4f));

    public static final Set<PGData3> S = TextTriangulator.triangulateGLText(Font.MONOSPACE, "*", 0.07, 0.3); //$NON-NLS-1$
    public static final Set<PGData3> D0 = TextTriangulator.triangulateGLText(Font.MONOSPACE, "0", 0.07, 0.3); //$NON-NLS-1$
    public static final Set<PGData3> D1 = TextTriangulator.triangulateGLText(Font.MONOSPACE, "1", 0.07, 0.3); //$NON-NLS-1$
    public static final Set<PGData3> D2 = TextTriangulator.triangulateGLText(Font.MONOSPACE, "2", 0.07, 0.3); //$NON-NLS-1$
    public static final Set<PGData3> D3 = TextTriangulator.triangulateGLText(Font.MONOSPACE, "3", 0.07, 0.3); //$NON-NLS-1$
    public static final Set<PGData3> D4 = TextTriangulator.triangulateGLText(Font.MONOSPACE, "4", 0.07, 0.3); //$NON-NLS-1$
    public static final Set<PGData3> D5 = TextTriangulator.triangulateGLText(Font.MONOSPACE, "5", 0.07, 0.3); //$NON-NLS-1$
    public static final Set<PGData3> D6 = TextTriangulator.triangulateGLText(Font.MONOSPACE, "6", 0.07, 0.3); //$NON-NLS-1$
    public static final Set<PGData3> D7 = TextTriangulator.triangulateGLText(Font.MONOSPACE, "7", 0.07, 0.3); //$NON-NLS-1$
    public static final Set<PGData3> D8 = TextTriangulator.triangulateGLText(Font.MONOSPACE, "8", 0.07, 0.3); //$NON-NLS-1$
    public static final Set<PGData3> D9 = TextTriangulator.triangulateGLText(Font.MONOSPACE, "9", 0.07, 0.3); //$NON-NLS-1$
    public static final Set<PGData3> DDot = TextTriangulator.triangulateGLText(Font.MONOSPACE, ".", 0.07, 0.3); //$NON-NLS-1$
    public static final Set<PGData3> DComma = TextTriangulator.triangulateGLText(Font.MONOSPACE, ",", 0.07, 0.3); //$NON-NLS-1$
    public static final Set<PGData3> DDegree = TextTriangulator.triangulateGLText(Font.MONOSPACE, "°", 0.07, 0.3); //$NON-NLS-1$
    public static final Set<PGData3> DX = TextTriangulator.triangulateGLText(Font.MONOSPACE, "dX =", 0.07, 0.3); //$NON-NLS-1$
    public static final Set<PGData3> DY = TextTriangulator.triangulateGLText(Font.MONOSPACE, "dY =", 0.07, 0.3); //$NON-NLS-1$
    public static final Set<PGData3> DZ = TextTriangulator.triangulateGLText(Font.MONOSPACE, "dZ =", 0.07, 0.3); //$NON-NLS-1$
    public static final Set<PGData3> DA = TextTriangulator.triangulateGLText(Font.MONOSPACE, "D  =", 0.07, 0.3); //$NON-NLS-1$
    public static final Set<PGData3> DM = TextTriangulator.triangulateGLText(Font.MONOSPACE, "-", 0.07, 0.3); //$NON-NLS-1$

    private static final GColour BLACK = new GColour(-1, 0f, 0f, 0f, 1f);
    private static IndexedEntry col16IndexedEntry = new IndexedEntry(.5f + .000016f, .5f + .000016f, .5f + .000016f);
    private static GColour originalColour16 = new GColour(-1, 0f, 0f, 0f, 1f);

    public static final int NUM_CORES = Runtime.getRuntime().availableProcessors();

    public static final Matrix4f ID = Matrix4f.setIdentity(new Matrix4f());
    public static final GData1 DUMMY_REFERENCE = new GData1();
    public static final DatFile DUMMY_DATFILE = new DatFile(File.separator + File.separator + I18n.E3D_NO_FILE_SELECTED, "DUMMY FILE", true, DatType.PART); //$NON-NLS-1$

    public static final Matrix ACCURATE_ID = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE);

    private static final HashMap<Integer, GColour> colourFromIndex = new HashMap<>();
    private static final HashMap<Integer, GColour> edgeColourFromIndex = new HashMap<>();
    private static final HashMap<Integer, String> colourNameFromIndex = new HashMap<>();

    private static final HashMap<IndexedEntry, Integer> indexFromColour = new HashMap<>();

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
        return colourNameFromIndex.getOrDefault(index, "<???>"); //$NON-NLS-1$
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
            Pattern pAlpha = Pattern.compile("ALPHA\\s+\\d+"); //$NON-NLS-1$
            Pattern pFraction = Pattern.compile("FRACTION\\s+\\d+.?\\d*"); //$NON-NLS-1$
            Pattern pSize = Pattern.compile("SIZE\\s+\\d+.?\\d*"); //$NON-NLS-1$
            Pattern pMinSize = Pattern.compile("MINSIZE\\s+\\d+.?\\d*"); //$NON-NLS-1$
            Pattern pMaxSize = Pattern.compile("MAXSIZE\\s+\\d+.?\\d*"); //$NON-NLS-1$
            Pattern pSpeckle = Pattern.compile("SPECKLE\\s+VALUE\\s+#[A-F0-9]{6}"); //$NON-NLS-1$
            Pattern pGlitter = Pattern.compile("GLITTER\\s+VALUE\\s+#[A-F0-9]{6}"); //$NON-NLS-1$
            UTF8BufferedReader reader = null;
            try {
                indexFromColour.put(new IndexedEntry(View.LINE_COLOUR_R[0], View.LINE_COLOUR_G[0], View.LINE_COLOUR_B[0]), 24);
                reader = new UTF8BufferedReader(location);
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    String[] dataSegments = line.trim().split("\\s+"); //$NON-NLS-1$
                    if (dataSegments.length > 6) {
                        if ("!COLOUR".equals(dataSegments[1])) { //$NON-NLS-1$
                            int index = Integer.parseInt(dataSegments[4]);

                            float magicIndexNumber = index;
                            while (magicIndexNumber > 512f) {
                                magicIndexNumber = magicIndexNumber / 13.37f;
                            }

                            float r = Integer.parseInt(dataSegments[6].substring(1, 3), 16) / 255f + .000001f * magicIndexNumber;
                            float g = Integer.parseInt(dataSegments[6].substring(3, 5), 16) / 255f + .000001f * magicIndexNumber;
                            float b = Integer.parseInt(dataSegments[6].substring(5, 7), 16) / 255f + .000001f * magicIndexNumber;

                            float r2 = Integer.parseInt(dataSegments[8].substring(1, 3), 16) / 255f;
                            float g2 = Integer.parseInt(dataSegments[8].substring(3, 5), 16) / 255f;
                            float b2 = Integer.parseInt(dataSegments[8].substring(5, 7), 16) / 255f;

                            Matcher m = pAlpha.matcher(line);

                            if (m.find()) {
                                String alphaStr = m.group().replaceAll("ALPHA", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
                                float alpha = Float.parseFloat(alphaStr) / 255f;
                                GColour colour = new GColour(index, r, g, b, alpha);
                                if (line.contains(" MATERIAL")) { //$NON-NLS-1$
                                    try {

                                        Matcher m2 = pFraction.matcher(line);
                                        Matcher m3 = pSize.matcher(line);
                                        Matcher m4 = pMinSize.matcher(line);
                                        Matcher m5 = pMaxSize.matcher(line);

                                        m2.find();

                                        float fraction = Float.parseFloat(m2.group().replaceAll("FRACTION\\s+", "").trim()); //$NON-NLS-1$ //$NON-NLS-2$
                                        float minSize = 0f;
                                        float maxSize = 0f;
                                        if (!m4.find()) {
                                            m3.find();
                                            minSize = Float.parseFloat(m3.group().replaceAll("SIZE\\s+", "").trim()); //$NON-NLS-1$ //$NON-NLS-2$
                                            maxSize = minSize;
                                        } else {
                                            m5.find();
                                            minSize = Float.parseFloat(m4.group().replaceAll("MINSIZE\\s+", "").trim()); //$NON-NLS-1$ //$NON-NLS-2$
                                            maxSize = Float.parseFloat(m5.group().replaceAll("MAXSIZE\\s+", "").trim()); //$NON-NLS-1$ //$NON-NLS-2$
                                        }

                                        if (line.contains(" GLITTER")) { //$NON-NLS-1$
                                            Matcher m6 = pGlitter.matcher(line);
                                            m6.find();
                                            String valStr = m6.group().replaceAll("GLITTER\\s+VALUE\\s+", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
                                            float vR = Integer.parseInt(valStr.substring(1, 3), 16) / 255f;
                                            float vG = Integer.parseInt(valStr.substring(3, 5), 16) / 255f;
                                            float vB = Integer.parseInt(valStr.substring(5, 7), 16) / 255f;
                                            colour = new GColour(index, r, g, b, Math.min(alpha, .99f), new GCGlitter(vR, vG, vB, fraction, minSize, maxSize));
                                        } else if (line.contains(" SPECKLE")) { //$NON-NLS-1$
                                            Matcher m6 = pSpeckle.matcher(line);
                                            m6.find();
                                            String valStr = m6.group().replaceAll("SPECKLE\\s+VALUE\\s+", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
                                            float vR = Integer.parseInt(valStr.substring(1, 3), 16) / 255f;
                                            float vG = Integer.parseInt(valStr.substring(3, 5), 16) / 255f;
                                            float vB = Integer.parseInt(valStr.substring(5, 7), 16) / 255f;
                                            colour = new GColour(index, r, g, b, Math.min(alpha, .99f), new GCSpeckle(vR, vG, vB, fraction, minSize, maxSize));
                                        }
                                    } catch (Exception e) {
                                        NLogger.error(View.class, "Line: " + line); //$NON-NLS-1$
                                        NLogger.error(View.class, e);
                                    }
                                }
                                colourFromIndex.put(index, colour);
                            } else if (line.contains(" CHROME")) { //$NON-NLS-1$
                                GColour colour = new GColour(index, r, g, b, 1f, new GCChrome());
                                colourFromIndex.put(index, colour);
                            } else if (line.contains(" RUBBER")) { //$NON-NLS-1$
                                GColour colour = new GColour(index, r, g, b, 1f, new GCRubber());
                                colourFromIndex.put(index, colour);
                            } else if (line.contains(" MATTE_METALLIC")) { //$NON-NLS-1$
                                GColour colour = new GColour(index, r, g, b, 1f, new GCMatteMetal());
                                colourFromIndex.put(index, colour);
                            } else if (line.contains(" METAL")) { //$NON-NLS-1$
                                GColour colour = new GColour(index, r, g, b, 1f, new GCMetal());
                                colourFromIndex.put(index, colour);
                            } else if (line.contains(" PEARLESCENT")) { //$NON-NLS-1$
                                GColour colour = new GColour(index, r, g, b, .99f, new GCPearl());
                                colourFromIndex.put(index, colour);
                            } else {
                                GColour colour = new GColour(index, r, g, b, 1f);
                                if (line.contains(" MATERIAL")) { //$NON-NLS-1$
                                    try {

                                        Matcher m2 = pFraction.matcher(line);
                                        Matcher m3 = pSize.matcher(line);
                                        Matcher m4 = pMinSize.matcher(line);
                                        Matcher m5 = pMaxSize.matcher(line);

                                        m2.find();

                                        float fraction = Float.parseFloat(m2.group().replaceAll("FRACTION\\s+", "").trim()); //$NON-NLS-1$ //$NON-NLS-2$
                                        float minSize = 0f;
                                        float maxSize = 0f;
                                        if (!m4.find()) {
                                            m3.find();
                                            minSize = Float.parseFloat(m3.group().replaceAll("SIZE\\s+", "").trim()); //$NON-NLS-1$ //$NON-NLS-2$
                                            maxSize = minSize;
                                        } else {
                                            m5.find();
                                            minSize = Float.parseFloat(m4.group().replaceAll("MINSIZE\\s+", "").trim()); //$NON-NLS-1$ //$NON-NLS-2$
                                            maxSize = Float.parseFloat(m5.group().replaceAll("MAXSIZE\\s+", "").trim()); //$NON-NLS-1$ //$NON-NLS-2$
                                        }

                                        if (line.contains(" GLITTER")) { //$NON-NLS-1$
                                            Matcher m6 = pGlitter.matcher(line);
                                            m6.find();
                                            String valStr = m6.group().replaceAll("GLITTER\\s+VALUE\\s+", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
                                            float vR = Integer.parseInt(valStr.substring(1, 3), 16) / 255f;
                                            float vG = Integer.parseInt(valStr.substring(3, 5), 16) / 255f;
                                            float vB = Integer.parseInt(valStr.substring(5, 7), 16) / 255f;
                                            colour = new GColour(index, r, g, b, 99f, new GCGlitter(vR, vG, vB, fraction, minSize, maxSize));
                                        } else if (line.contains(" SPECKLE")) { //$NON-NLS-1$
                                            Matcher m6 = pSpeckle.matcher(line);
                                            m6.find();
                                            String valStr = m6.group().replaceAll("SPECKLE\\s+VALUE\\s+", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
                                            float vR = Integer.parseInt(valStr.substring(1, 3), 16) / 255f;
                                            float vG = Integer.parseInt(valStr.substring(3, 5), 16) / 255f;
                                            float vB = Integer.parseInt(valStr.substring(5, 7), 16) / 255f;
                                            colour = new GColour(index, r, g, b, .99f, new GCSpeckle(vR, vG, vB, fraction, minSize, maxSize));
                                        }
                                    } catch (Exception e) {
                                        NLogger.error(View.class, "Line: " + line); //$NON-NLS-1$
                                        NLogger.error(View.class, e);
                                    }
                                }
                                colourFromIndex.put(index, colour);
                            }
                            IndexedEntry entry = new IndexedEntry(r, g, b);
                            if (index == 16) {
                                col16IndexedEntry = entry;
                                originalColour16 = colourFromIndex.get(16).createClone();
                            }
                            indexFromColour.put(entry, index);
                            edgeColourFromIndex.put(index, new GColour(index, r2, g2, b2, 1f));
                            colourNameFromIndex.put(index, dataSegments[2].replaceAll("_", " ")); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    }
                }
                return true;
            } catch (Exception e) {
            } finally {
                try {
                    if (reader != null)
                        reader.close();
                } catch (LDParsingException e1) {
                }
            }
        }
        return false;
    }

    public static void overrideColour16() {
        float r;
        float g;
        float b;
        GColour col16 = getLDConfigColour(16);
        if (COLOUR16_OVERRIDE_R[0] > 0f && COLOUR16_OVERRIDE_G[0] > 0f && COLOUR16_OVERRIDE_B[0] > 0f) {
            r = COLOUR16_OVERRIDE_R[0];
            g = COLOUR16_OVERRIDE_G[0];
            b = COLOUR16_OVERRIDE_B[0];
        } else {
            r = originalColour16.getR();
            g = originalColour16.getG();
            b = originalColour16.getB();
        }
        col16.setR(r);
        col16.setG(g);
        col16.setB(b);
        indexFromColour.remove(col16IndexedEntry);
        col16IndexedEntry = new IndexedEntry(r + .000016f, g + .000016f, b + .000016f);
        indexFromColour.put(col16IndexedEntry, 16);
    }

}
