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
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

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
    public static BigDecimal unit_factor = BigDecimal.ONE;
    /** threshold for "solid" edges (default: 5e-6f) */
    public static float edge_threshold = 5e-6f;
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

    public final static GColour RANDOM_COLOUR = new GColour(-1, 1f, 1f, 1f, 0f);

    public static final float[] Color16_override_r = new float[] { 0f };
    public static final float[] Color16_override_g = new float[] { 0f };
    public static final float[] Color16_override_b = new float[] { 0f };

    public static final float[] BFC_front_Colour_r = new float[] { 0f };
    public static final float[] BFC_front_Colour_g = new float[] { .9f };
    public static final float[] BFC_front_Colour_b = new float[] { 0f };

    public static final float[] BFC_back__Colour_r = new float[] { .9f };
    public static final float[] BFC_back__Colour_g = new float[] { 0f };
    public static final float[] BFC_back__Colour_b = new float[] { 0f };

    public static final float[] BFC_uncertified_Colour_r = new float[] { 0f };
    public static final float[] BFC_uncertified_Colour_g = new float[] { 0f };
    public static final float[] BFC_uncertified_Colour_b = new float[] { 1f };

    public static final float[] vertex_Colour_r = new float[] { 0.118f };
    public static final float[] vertex_Colour_g = new float[] { 0.565f };
    public static final float[] vertex_Colour_b = new float[] { 1f };

    public static final float[] vertex_selected_Colour_r = new float[] { 0.75f };
    public static final float[] vertex_selected_Colour_g = new float[] { 0.05f };
    public static final float[] vertex_selected_Colour_b = new float[] { 0.05f };

    public static final float[] condline_selected_Colour_r = new float[] { 0.75f };
    public static final float[] condline_selected_Colour_g = new float[] { 0.35f };
    public static final float[] condline_selected_Colour_b = new float[] { 0.05f };

    public static final float[] line_Colour_r = new float[] { 0f };
    public static final float[] line_Colour_g = new float[] { 0f };
    public static final float[] line_Colour_b = new float[] { 0f };

    public static final float[] meshline_Colour_r = new float[] { 0f };
    public static final float[] meshline_Colour_g = new float[] { 0f };
    public static final float[] meshline_Colour_b = new float[] { 0f };

    public static final float[] condline_Colour_r = new float[] { 0f };
    public static final float[] condline_Colour_g = new float[] { 0f };
    public static final float[] condline_Colour_b = new float[] { 0f };

    public static final float[] condline_hidden_Colour_r = new float[] { 1f };
    public static final float[] condline_hidden_Colour_g = new float[] { .44f };
    public static final float[] condline_hidden_Colour_b = new float[] { .1f };

    public static final float[] condline_shown_Colour_r = new float[] { .553f };
    public static final float[] condline_shown_Colour_g = new float[] { .22f };
    public static final float[] condline_shown_Colour_b = new float[] { 1f };

    public static final float[] cursor1_Colour_r = new float[] { 1f };
    public static final float[] cursor1_Colour_g = new float[] { 0f };
    public static final float[] cursor1_Colour_b = new float[] { 0f };

    public static final float[] cursor2_Colour_r = new float[] { 0f };
    public static final float[] cursor2_Colour_g = new float[] { 0f };
    public static final float[] cursor2_Colour_b = new float[] { 1f };

    public static final float[] background_Colour_r = new float[] { 1f };
    public static final float[] background_Colour_g = new float[] { 1f };
    public static final float[] background_Colour_b = new float[] { 1f };

    public static final float[] light1_Colour_r = new float[] { 0.85f };
    public static final float[] light1_Colour_g = new float[] { 0.85f };
    public static final float[] light1_Colour_b = new float[] { 0.85f };

    public static final float[] light1_specular_Colour_r = new float[] { 0.5f };
    public static final float[] light1_specular_Colour_g = new float[] { 0.5f };
    public static final float[] light1_specular_Colour_b = new float[] { 0.5f };

    public static final float[] light2_Colour_r = new float[] { 0.27f };
    public static final float[] light2_Colour_g = new float[] { 0.27f };
    public static final float[] light2_Colour_b = new float[] { 0.27f };

    public static final float[] light2_specular_Colour_r = new float[] { 0f };
    public static final float[] light2_specular_Colour_g = new float[] { 0f };
    public static final float[] light2_specular_Colour_b = new float[] { 0f };

    public static final float[] light3_Colour_r = new float[] { 0.27f };
    public static final float[] light3_Colour_g = new float[] { 0.27f };
    public static final float[] light3_Colour_b = new float[] { 0.27f };

    public static final float[] light3_specular_Colour_r = new float[] { 0f };
    public static final float[] light3_specular_Colour_g = new float[] { 0f };
    public static final float[] light3_specular_Colour_b = new float[] { 0f };

    public static final float[] light4_Colour_r = new float[] { 0.27f };
    public static final float[] light4_Colour_g = new float[] { 0.27f };
    public static final float[] light4_Colour_b = new float[] { 0.27f };

    public static final float[] light4_specular_Colour_r = new float[] { 0f };
    public static final float[] light4_specular_Colour_g = new float[] { 0f };
    public static final float[] light4_specular_Colour_b = new float[] { 0f };

    public static final float[] manipulator_selected_Colour_r = new float[] { 0.75f };
    public static final float[] manipulator_selected_Colour_g = new float[] { 0.75f };
    public static final float[] manipulator_selected_Colour_b = new float[] { 0f };

    public static final float[] manipulator_innerCircle_Colour_r = new float[] { .3f };
    public static final float[] manipulator_innerCircle_Colour_g = new float[] { .3f };
    public static final float[] manipulator_innerCircle_Colour_b = new float[] { .3f };

    public static final float[] manipulator_outerCircle_Colour_r = new float[] { .85f };
    public static final float[] manipulator_outerCircle_Colour_g = new float[] { .85f };
    public static final float[] manipulator_outerCircle_Colour_b = new float[] { .85f };

    public static final float[] manipulator_x_axis_Colour_r = new float[] { .5f };
    public static final float[] manipulator_x_axis_Colour_g = new float[] { 0f };
    public static final float[] manipulator_x_axis_Colour_b = new float[] { 0f };

    public static final float[] manipulator_y_axis_Colour_r = new float[] { 0f };
    public static final float[] manipulator_y_axis_Colour_g = new float[] { .5f };
    public static final float[] manipulator_y_axis_Colour_b = new float[] { 0f };

    public static final float[] manipulator_z_axis_Colour_r = new float[] { 0f };
    public static final float[] manipulator_z_axis_Colour_g = new float[] { 0f };
    public static final float[] manipulator_z_axis_Colour_b = new float[] { .5f };

    public static final float[] lineWidth1000 = new float[] { 100f };
    public static final float[] lineWidth = new float[] { .100f };
    public static final float[] lineWidthGL = new float[] { 1.5f };

    public static final float[] add_Object_Colour_r = new float[] { 1f };
    public static final float[] add_Object_Colour_g = new float[] { 0.6f };
    public static final float[] add_Object_Colour_b = new float[] { 0f };

    public static final float[] origin_Colour_r = new float[] { 0f };
    public static final float[] origin_Colour_g = new float[] { 0f };
    public static final float[] origin_Colour_b = new float[] { 0f };

    public static final float[] grid10_Colour_r = new float[] { .5f };
    public static final float[] grid10_Colour_g = new float[] { .5f };
    public static final float[] grid10_Colour_b = new float[] { .5f };

    public static final float[] grid_Colour_r = new float[] { 0.15f };
    public static final float[] grid_Colour_g = new float[] { 0.15f };
    public static final float[] grid_Colour_b = new float[] { 0.15f };

    public static final float[] rubberBand_Colour_r = new float[] { 1f };
    public static final float[] rubberBand_Colour_g = new float[] { 0f };
    public static final float[] rubberBand_Colour_b = new float[] { 0f };

    public static final float[] text_Colour_r = new float[] { 0f };
    public static final float[] text_Colour_g = new float[] { 0f };
    public static final float[] text_Colour_b = new float[] { 0f };

    public static final float[] x_axis_Colour_r = new float[] { 1f };
    public static final float[] x_axis_Colour_g = new float[] { 0f };
    public static final float[] x_axis_Colour_b = new float[] { 0f };

    public static final float[] y_axis_Colour_r = new float[] { 0f };
    public static final float[] y_axis_Colour_g = new float[] { 1f };
    public static final float[] y_axis_Colour_b = new float[] { 0f };

    public static final float[] z_axis_Colour_r = new float[] { 0f };
    public static final float[] z_axis_Colour_g = new float[] { 0f };
    public static final float[] z_axis_Colour_b = new float[] { 1f };

    public static final float[] primitive_background_Colour_r = new float[] { 1f };
    public static final float[] primitive_background_Colour_g = new float[] { 1f };
    public static final float[] primitive_background_Colour_b = new float[] { 1f };

    public static final float[] primitive_light1_Colour_r = new float[] { 0.8f };
    public static final float[] primitive_light1_Colour_g = new float[] { 0.8f };
    public static final float[] primitive_light1_Colour_b = new float[] { 0.8f };

    public static final float[] primitive_light1_specular_Colour_r = new float[] { 0.5f };
    public static final float[] primitive_light1_specular_Colour_g = new float[] { 0.5f };
    public static final float[] primitive_light1_specular_Colour_b = new float[] { 0.5f };

    public static final float[] primitive_light2_Colour_r = new float[] { 0.25f };
    public static final float[] primitive_light2_Colour_g = new float[] { 0.25f };
    public static final float[] primitive_light2_Colour_b = new float[] { 0.25f };

    public static final float[] primitive_light2_specular_Colour_r = new float[] { 0f };
    public static final float[] primitive_light2_specular_Colour_g = new float[] { 0f };
    public static final float[] primitive_light2_specular_Colour_b = new float[] { 0f };

    public static final float[] primitive_light3_Colour_r = new float[] { 0.25f };
    public static final float[] primitive_light3_Colour_g = new float[] { 0.25f };
    public static final float[] primitive_light3_Colour_b = new float[] { 0.25f };

    public static final float[] primitive_light3_specular_Colour_r = new float[] { 0f };
    public static final float[] primitive_light3_specular_Colour_g = new float[] { 0f };
    public static final float[] primitive_light3_specular_Colour_b = new float[] { 0f };

    public static final float[] primitive_light4_Colour_r = new float[] { 0.25f };
    public static final float[] primitive_light4_Colour_g = new float[] { 0.25f };
    public static final float[] primitive_light4_Colour_b = new float[] { 0.25f };

    public static final float[] primitive_light4_specular_Colour_r = new float[] { 0f };
    public static final float[] primitive_light4_specular_Colour_g = new float[] { 0f };
    public static final float[] primitive_light4_specular_Colour_b = new float[] { 0f };

    public static final float[] primitive_signFG_Colour_r = new float[] { .2f };
    public static final float[] primitive_signFG_Colour_g = new float[] { .2f };
    public static final float[] primitive_signFG_Colour_b = new float[] { 1f };

    public static final float[] primitive_signBG_Colour_r = new float[] { 1f };
    public static final float[] primitive_signBG_Colour_g = new float[] { 1f };
    public static final float[] primitive_signBG_Colour_b = new float[] { 1f };

    public static final float[] primitive_plusNminus_Colour_r = new float[] { 1f };
    public static final float[] primitive_plusNminus_Colour_g = new float[] { 1f };
    public static final float[] primitive_plusNminus_Colour_b = new float[] { 1f };

    public static final float[] primitive_selectedCell_Colour_r = new float[] { 1f };
    public static final float[] primitive_selectedCell_Colour_g = new float[] { .3f };
    public static final float[] primitive_selectedCell_Colour_b = new float[] { .3f };

    public static final float[] primitive_focusedCell_Colour_r = new float[] { .6f };
    public static final float[] primitive_focusedCell_Colour_g = new float[] { .6f };
    public static final float[] primitive_focusedCell_Colour_b = new float[] { 1f };

    public static final float[] primitive_normalCell_Colour_r = new float[] { .3f };
    public static final float[] primitive_normalCell_Colour_g = new float[] { .3f };
    public static final float[] primitive_normalCell_Colour_b = new float[] { .3f };

    public static final float[] primitive_cell_1_Colour_r = new float[] { .7f };
    public static final float[] primitive_cell_1_Colour_g = new float[] { .7f };
    public static final float[] primitive_cell_1_Colour_b = new float[] { .7f };

    public static final float[] primitive_cell_2_Colour_r = new float[] { 1f };
    public static final float[] primitive_cell_2_Colour_g = new float[] { 1f };
    public static final float[] primitive_cell_2_Colour_b = new float[] { 1f };

    public static final float[] primitive_categoryCell_1_Colour_r = new float[] { .6f };
    public static final float[] primitive_categoryCell_1_Colour_g = new float[] { .4f };
    public static final float[] primitive_categoryCell_1_Colour_b = new float[] { .3f };

    public static final float[] primitive_categoryCell_2_Colour_r = new float[] { .7f };
    public static final float[] primitive_categoryCell_2_Colour_g = new float[] { .5f };
    public static final float[] primitive_categoryCell_2_Colour_b = new float[] { .4f };

    public static final float[] primitive_edge_Colour_r = new float[] { 0f };
    public static final float[] primitive_edge_Colour_g = new float[] { 0f };
    public static final float[] primitive_edge_Colour_b = new float[] { 0f };

    public static final float[] primitive_condline_Colour_r = new float[] { 0f };
    public static final float[] primitive_condline_Colour_g = new float[] { 0f };
    public static final float[] primitive_condline_Colour_b = new float[] { 1f };

    public final static Set<PGData3> FRONT = TextTriangulator.triangulateGLText(Font.MONOSPACE, I18n.PERSPECTIVE_FRONT, 0.07, 0.3, .012f * (1f + View.getIconsize()), 16.9);
    public final static Set<PGData3> BACK = TextTriangulator.triangulateGLText(Font.MONOSPACE, I18n.PERSPECTIVE_BACK, 0.07, 0.3, .012f * (1f + View.getIconsize()), 16.9);
    public final static Set<PGData3> BOTTOM = TextTriangulator.triangulateGLText(Font.MONOSPACE, I18n.PERSPECTIVE_BOTTOM, 0.07, 0.3, .012f * (1f + View.getIconsize()), 16.9);
    public final static Set<PGData3> LEFT = TextTriangulator.triangulateGLText(Font.MONOSPACE, I18n.PERSPECTIVE_LEFT, 0.07, 0.3, .012f * (1f + View.getIconsize()), 16.9);
    public final static Set<PGData3> RIGHT = TextTriangulator.triangulateGLText(Font.MONOSPACE, I18n.PERSPECTIVE_RIGHT, 0.07, 0.3, .012f * (1f + View.getIconsize()), 16.9);
    public final static Set<PGData3> TOP = TextTriangulator.triangulateGLText(Font.MONOSPACE, I18n.PERSPECTIVE_TOP, 0.07, 0.3, .012f * (1f + View.getIconsize()), 16.9);

    public final static Set<PGData3> S = TextTriangulator.triangulateGLText(Font.MONOSPACE, "*", 0.07, 0.3, 16.9); //$NON-NLS-1$
    public final static Set<PGData3> D0 = TextTriangulator.triangulateGLText(Font.MONOSPACE, "0", 0.07, 0.3, 16.9); //$NON-NLS-1$
    public final static Set<PGData3> D1 = TextTriangulator.triangulateGLText(Font.MONOSPACE, "1", 0.07, 0.3, 16.9); //$NON-NLS-1$
    public final static Set<PGData3> D2 = TextTriangulator.triangulateGLText(Font.MONOSPACE, "2", 0.07, 0.3, 16.9); //$NON-NLS-1$
    public final static Set<PGData3> D3 = TextTriangulator.triangulateGLText(Font.MONOSPACE, "3", 0.07, 0.3, 16.9); //$NON-NLS-1$
    public final static Set<PGData3> D4 = TextTriangulator.triangulateGLText(Font.MONOSPACE, "4", 0.07, 0.3, 16.9); //$NON-NLS-1$
    public final static Set<PGData3> D5 = TextTriangulator.triangulateGLText(Font.MONOSPACE, "5", 0.07, 0.3, 16.9); //$NON-NLS-1$
    public final static Set<PGData3> D6 = TextTriangulator.triangulateGLText(Font.MONOSPACE, "6", 0.07, 0.3, 16.9); //$NON-NLS-1$
    public final static Set<PGData3> D7 = TextTriangulator.triangulateGLText(Font.MONOSPACE, "7", 0.07, 0.3, 16.9); //$NON-NLS-1$
    public final static Set<PGData3> D8 = TextTriangulator.triangulateGLText(Font.MONOSPACE, "8", 0.07, 0.3, 16.9); //$NON-NLS-1$
    public final static Set<PGData3> D9 = TextTriangulator.triangulateGLText(Font.MONOSPACE, "9", 0.07, 0.3, 16.9); //$NON-NLS-1$
    public final static Set<PGData3> Dd = TextTriangulator.triangulateGLText(Font.MONOSPACE, ".", 0.07, 0.3, 16.9); //$NON-NLS-1$
    public final static Set<PGData3> Dc = TextTriangulator.triangulateGLText(Font.MONOSPACE, ",", 0.07, 0.3, 16.9); //$NON-NLS-1$
    public final static Set<PGData3> Dg = TextTriangulator.triangulateGLText(Font.MONOSPACE, "°", 0.07, 0.3, 16.9); //$NON-NLS-1$
    public final static Set<PGData3> DX = TextTriangulator.triangulateGLText(Font.MONOSPACE, "dX =", 0.07, 0.3, 16.9); //$NON-NLS-1$
    public final static Set<PGData3> DY = TextTriangulator.triangulateGLText(Font.MONOSPACE, "dY =", 0.07, 0.3, 16.9); //$NON-NLS-1$
    public final static Set<PGData3> DZ = TextTriangulator.triangulateGLText(Font.MONOSPACE, "dZ =", 0.07, 0.3, 16.9); //$NON-NLS-1$
    public final static Set<PGData3> DA = TextTriangulator.triangulateGLText(Font.MONOSPACE, "D  =", 0.07, 0.3, 16.9); //$NON-NLS-1$
    public final static Set<PGData3> DM = TextTriangulator.triangulateGLText(Font.MONOSPACE, "-", 0.07, 0.3, 16.9); //$NON-NLS-1$

    private static final GColour BLACK = new GColour(-1, 0f, 0f, 0f, 1f);
    private static IndexedEntry col16_indexedEntry = new IndexedEntry(.5f + .000016f, .5f + .000016f, .5f + .000016f);
    private static GColour original_col16 = new GColour(-1, 0f, 0f, 0f, 1f);

    public static final int NUM_CORES = Runtime.getRuntime().availableProcessors();

    private static final int iconSize = WorkbenchManager.getUserSettingState().getIconSize();

    public static final Matrix4f ID = Matrix4f.setIdentity(new Matrix4f());
    public static final GData1 DUMMY_REFERENCE = new GData1();
    public static final DatFile DUMMY_DATFILE = new DatFile(File.separator + File.separator + I18n.E3D_NoFileSelected, "DUMMY FILE", true, DatType.PART); //$NON-NLS-1$

    public static final Matrix ACCURATE_ID = new Matrix(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE);

    private static final HashMap<Integer, GColour> colourFromIndex = new HashMap<Integer, GColour>();
    private static final HashMap<Integer, GColour> edgeColourFromIndex = new HashMap<Integer, GColour>();
    private static final HashMap<Integer, String> colourNameFromIndex = new HashMap<Integer, String>();

    private static final HashMap<IndexedEntry, Integer> indexFromColour = new HashMap<IndexedEntry, Integer>();

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

    public static int getIconsize() {
        return iconSize;
    }

    public static int getImageSizeFromIconSize() {
        final int imgSize;
        switch (View.getIconsize()) {
        case -1:
            imgSize = 12;
            break;
        case 0:
            imgSize = 16;
            break;
        case 1:
            imgSize = 20;
            break;
        case 2:
            imgSize = 24;
            break;
        case 3:
            imgSize = 28;
            break;
        case 4:
            imgSize = 32;
            break;
        case 5:
            imgSize = 32;
            break;
        default:
            imgSize = 16;
            break;
        }
        return imgSize;
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
                indexFromColour.put(new IndexedEntry(View.line_Colour_r[0], View.line_Colour_g[0], View.line_Colour_b[0]), 24);
                reader = new UTF8BufferedReader(location);
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

                            Matcher m = pAlpha.matcher(line);

                            if (m.find()) {
                                String alphaStr = m.group().replaceAll("ALPHA", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
                                float alpha = Float.parseFloat(alphaStr) / 255f;
                                GColour colour = new GColour(index, R, G, B, alpha);
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
                                            colour = new GColour(index, R, G, B, Math.min(alpha, .99f), new GCGlitter(vR, vG, vB, fraction, minSize, maxSize));
                                        } else if (line.contains(" SPECKLE")) { //$NON-NLS-1$
                                            Matcher m6 = pSpeckle.matcher(line);
                                            m6.find();
                                            String valStr = m6.group().replaceAll("SPECKLE\\s+VALUE\\s+", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
                                            float vR = Integer.parseInt(valStr.substring(1, 3), 16) / 255f;
                                            float vG = Integer.parseInt(valStr.substring(3, 5), 16) / 255f;
                                            float vB = Integer.parseInt(valStr.substring(5, 7), 16) / 255f;
                                            colour = new GColour(index, R, G, B, Math.min(alpha, .99f), new GCSpeckle(vR, vG, vB, fraction, minSize, maxSize));
                                        }
                                    } catch (Exception e) {
                                        NLogger.error(View.class, "Line: " + line); //$NON-NLS-1$
                                        NLogger.error(View.class, e);
                                    }
                                }
                                colourFromIndex.put(index, colour);
                            } else if (line.contains(" CHROME")) { //$NON-NLS-1$
                                GColour colour = new GColour(index, R, G, B, 1f, new GCChrome());
                                colourFromIndex.put(index, colour);
                            } else if (line.contains(" RUBBER")) { //$NON-NLS-1$
                                GColour colour = new GColour(index, R, G, B, 1f, new GCRubber());
                                colourFromIndex.put(index, colour);
                            } else if (line.contains(" MATTE_METALLIC")) { //$NON-NLS-1$
                                GColour colour = new GColour(index, R, G, B, 1f, new GCMatteMetal());
                                colourFromIndex.put(index, colour);
                            } else if (line.contains(" METAL")) { //$NON-NLS-1$
                                GColour colour = new GColour(index, R, G, B, 1f, new GCMetal());
                                colourFromIndex.put(index, colour);
                            } else if (line.contains(" PEARLESCENT")) { //$NON-NLS-1$
                                GColour colour = new GColour(index, R, G, B, .99f, new GCPearl());
                                colourFromIndex.put(index, colour);
                            } else {
                                GColour colour = new GColour(index, R, G, B, 1f);
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
                                            colour = new GColour(index, R, G, B, 99f, new GCGlitter(vR, vG, vB, fraction, minSize, maxSize));
                                        } else if (line.contains(" SPECKLE")) { //$NON-NLS-1$
                                            Matcher m6 = pSpeckle.matcher(line);
                                            m6.find();
                                            String valStr = m6.group().replaceAll("SPECKLE\\s+VALUE\\s+", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
                                            float vR = Integer.parseInt(valStr.substring(1, 3), 16) / 255f;
                                            float vG = Integer.parseInt(valStr.substring(3, 5), 16) / 255f;
                                            float vB = Integer.parseInt(valStr.substring(5, 7), 16) / 255f;
                                            colour = new GColour(index, R, G, B, .99f, new GCSpeckle(vR, vG, vB, fraction, minSize, maxSize));
                                        }
                                    } catch (Exception e) {
                                        NLogger.error(View.class, "Line: " + line); //$NON-NLS-1$
                                        NLogger.error(View.class, e);
                                    }
                                }
                                colourFromIndex.put(index, colour);
                            }
                            IndexedEntry entry = new IndexedEntry(R, G, B);
                            if (index == 16) {
                                col16_indexedEntry = entry;
                                original_col16 = colourFromIndex.get(16).clone();
                            }
                            indexFromColour.put(entry, index);
                            edgeColourFromIndex.put(index, new GColour(index, R2, G2, B2, 1f));
                            colourNameFromIndex.put(index, data_segments[2].replaceAll("_", " ")); //$NON-NLS-1$ //$NON-NLS-2$
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
        if (Color16_override_r != null && Color16_override_g != null && Color16_override_b != null &&
                Color16_override_r[0] > 0f && Color16_override_g[0] > 0f && Color16_override_b[0] > 0f) {
            r = Color16_override_r[0];
            g = Color16_override_g[0];
            b = Color16_override_b[0];
        } else {
            r = original_col16.getR();
            g = original_col16.getG();
            b = original_col16.getB();
        }
        col16.setR(r);
        col16.setG(g);
        col16.setB(b);
        indexFromColour.remove(col16_indexedEntry);
        col16_indexedEntry = new IndexedEntry(r + .000016f, g + .000016f, b + .000016f);
        indexFromColour.put(col16_indexedEntry, 16);
    }

}
