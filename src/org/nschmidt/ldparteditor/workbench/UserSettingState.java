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
package org.nschmidt.ldparteditor.workbench;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.wb.swt.SWTResourceManager;
import org.nschmidt.ldparteditor.composites.ToolItemState;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.enums.Colour;
import org.nschmidt.ldparteditor.enums.Task;
import org.nschmidt.ldparteditor.enums.TextTask;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.state.KeyStateManager;

/**
 * This class represents the permanent state of the application setting with
 * focus on user dependent information (e.g. user name, LDraw path, ...)
 *
 * @author nils
 *
 */
public class UserSettingState implements Serializable {
    // TODO New values, which were not included in the state before, have to be initialized! (@ WorkbenchManager.loadWorkbench())
    /** V1.00 */
    private static final long serialVersionUID = 1L;
    /** Where your part authoring folder is located. */
    private String authoringFolderPath = ""; //$NON-NLS-1$
    /** Where your LDraw folder is located. */
    private String ldrawFolderPath = ""; //$NON-NLS-1$
    /** Where your unofficial parts folder is located. */
    private String unofficialFolderPath = ""; //$NON-NLS-1$
    /** Your LDraw user name. */
    private String ldrawUserName = ""; //$NON-NLS-1$
    /** The license under you want to publish your work. */
    private String license = ""; //$NON-NLS-1$
    /** Your real name. */
    private String realUserName = ""; //$NON-NLS-1$
    /** {@code true} if the user wants to use relative paths */
    private boolean usingRelativePaths = false;
    /** Your colour palette. */
    private ArrayList<GColour> userPalette = new ArrayList<GColour>();

    /** Your coarse move snap value */
    private BigDecimal coarse_move_snap = new BigDecimal("1"); //$NON-NLS-1$
    /** Your coarse rotate snap value */
    private BigDecimal coarse_rotate_snap = new BigDecimal("90"); //$NON-NLS-1$
    /** Your coarse scale snap value */
    private BigDecimal coarse_scale_snap = new BigDecimal("2"); //$NON-NLS-1$
    /** Your medium move snap value */
    private BigDecimal medium_move_snap = new BigDecimal("0.01"); //$NON-NLS-1$
    /** Your medium rotate snap value */
    private BigDecimal medium_rotate_snap = new BigDecimal("11.25"); //$NON-NLS-1$
    /** Your medium scale snap value */
    private BigDecimal medium_scale_snap = new BigDecimal("1.1"); //$NON-NLS-1$
    /** Your fine move snap value */
    private BigDecimal fine_move_snap = new BigDecimal("0.0001"); //$NON-NLS-1$
    /** Your fine rotate snap value */
    private BigDecimal fine_rotate_snap = BigDecimal.ONE;
    /** Your fine scale snap value */
    private BigDecimal fine_scale_snap = new BigDecimal("1.001"); //$NON-NLS-1$

    /** Your "fuzziness factor", LDU distance below which vertices would be considered the same in 3D space. */
    private BigDecimal fuzziness3D = new BigDecimal("0.001"); //$NON-NLS-1$

    /** Your "fuzziness factor", "pixel" distance below which vertices would be considered the same in 2D projected space. */
    private int fuzziness2D = 7;

    /** Your transformation matrix precision */
    private int transMatrixPrecision = 5;
    /** Your coordinates precision */
    private int coordsPrecision = 3;

    /** {@code true} if the user wants to delete this settings */
    private boolean resetOnStart = false;
    /** LDConfig.ldr */
    private String ldConfigPath = null;
    /** {@code true} if the user wants active synchronisation with the text editor */
    @SuppressWarnings("unused")
    private AtomicBoolean syncWithTextEditor = new AtomicBoolean(true);
    /** {@code true} if the user wants active synchronisation with the text editor */
    private AtomicBoolean syncWithLpeInline = new AtomicBoolean(false);

    private int iconSize = 0;

    private float[] manipulatorSize = null;

    private ArrayList<String> recentItems = new ArrayList<String>();

    private Locale locale = Locale.US;

    /** {@code true} if the user has got the information that BFC certification is mandatory for the LDraw Standard Preview Mode  */
    private boolean bfcCertificationRequiredForLDrawMode = false;

    private String[] key3DStrings = null;
    private String[] key3DKeys = null;
    private Task[] key3DTasks = null;

    private String[] keyTextStrings = null;
    private String[] keyTextKeys = null;
    private TextTask[] keyTextTasks = null;

    private ArrayList<ToolItemState> toolItemConfig3D = new ArrayList<ToolItemState>();

    /** {@code true} if anti-aliasing is enabled for 3D windows */
    private boolean antiAliasing = false;

    /** {@code true} if the new OpenGL 3.3 engine is enabled for 3D windows */
    private boolean newEngine = false;
    /** {@code true} if the Vulkan engine is enabled for 3D windows */
    private boolean vulkanEngine = false;

    /** {@code true} if invalid shapes are allowed in the 3D editor */
    private boolean allowInvalidShapes = false;
    
    /** {@code true} if the user can translate the 3D view with the cursor */
    private boolean translateViewByCursor = false;

    private boolean disableMAD3D = false;
    private boolean disableMADtext = false;

    private float[] Color16_override_r = null;
    private float[] Color16_override_g = null;
    private float[] Color16_override_b = null;

    private float[] BFC_front_Colour_r = null;
    private float[] BFC_front_Colour_g = null;
    private float[] BFC_front_Colour_b = null;

    private float[] BFC_back__Colour_r = null;
    private float[] BFC_back__Colour_g = null;
    private float[] BFC_back__Colour_b = null;

    private float[] BFC_uncertified_Colour_r = null;
    private float[] BFC_uncertified_Colour_g = null;
    private float[] BFC_uncertified_Colour_b = null;

    private float[] vertex_Colour_r = null;
    private float[] vertex_Colour_g = null;
    private float[] vertex_Colour_b = null;

    private float[] vertex_selected_Colour_r = null;
    private float[] vertex_selected_Colour_g = null;
    private float[] vertex_selected_Colour_b = null;

    private float[] condline_selected_Colour_r = null;
    private float[] condline_selected_Colour_g = null;
    private float[] condline_selected_Colour_b = null;

    private float[] line_Colour_r = null;
    private float[] line_Colour_g = null;
    private float[] line_Colour_b = null;

    private float[] meshline_Colour_r = null;
    private float[] meshline_Colour_g = null;
    private float[] meshline_Colour_b = null;

    private float[] condline_Colour_r = null;
    private float[] condline_Colour_g = null;
    private float[] condline_Colour_b = null;

    private float[] condline_hidden_Colour_r = null;
    private float[] condline_hidden_Colour_g = null;
    private float[] condline_hidden_Colour_b = null;

    private float[] condline_shown_Colour_r = null;
    private float[] condline_shown_Colour_g = null;
    private float[] condline_shown_Colour_b = null;

    private float[] background_Colour_r = null;
    private float[] background_Colour_g = null;
    private float[] background_Colour_b = null;

    private float[] light1_Colour_r = null;
    private float[] light1_Colour_g = null;
    private float[] light1_Colour_b = null;

    private float[] light1_specular_Colour_r = null;
    private float[] light1_specular_Colour_g = null;
    private float[] light1_specular_Colour_b = null;

    private float[] light2_Colour_r = null;
    private float[] light2_Colour_g = null;
    private float[] light2_Colour_b = null;

    private float[] light2_specular_Colour_r = null;
    private float[] light2_specular_Colour_g = null;
    private float[] light2_specular_Colour_b = null;

    private float[] light3_Colour_r = null;
    private float[] light3_Colour_g = null;
    private float[] light3_Colour_b = null;

    private float[] light3_specular_Colour_r = null;
    private float[] light3_specular_Colour_g = null;
    private float[] light3_specular_Colour_b = null;

    private float[] light4_Colour_r = null;
    private float[] light4_Colour_g = null;
    private float[] light4_Colour_b = null;

    private float[] light4_specular_Colour_r = null;
    private float[] light4_specular_Colour_g = null;
    private float[] light4_specular_Colour_b = null;

    private float[] manipulator_selected_Colour_r = null;
    private float[] manipulator_selected_Colour_g = null;
    private float[] manipulator_selected_Colour_b = null;

    private float[] manipulator_innerCircle_Colour_r = null;
    private float[] manipulator_innerCircle_Colour_g = null;
    private float[] manipulator_innerCircle_Colour_b = null;

    private float[] manipulator_outerCircle_Colour_r = null;
    private float[] manipulator_outerCircle_Colour_g = null;
    private float[] manipulator_outerCircle_Colour_b = null;

    private float[] manipulator_x_axis_Colour_r = null;
    private float[] manipulator_x_axis_Colour_g = null;
    private float[] manipulator_x_axis_Colour_b = null;

    private float[] manipulator_y_axis_Colour_r = null;
    private float[] manipulator_y_axis_Colour_g = null;
    private float[] manipulator_y_axis_Colour_b = null;

    private float[] manipulator_z_axis_Colour_r = null;
    private float[] manipulator_z_axis_Colour_g = null;
    private float[] manipulator_z_axis_Colour_b = null;

    private float[] add_Object_Colour_r = null;
    private float[] add_Object_Colour_g = null;
    private float[] add_Object_Colour_b = null;

    private float[] origin_Colour_r = null;
    private float[] origin_Colour_g = null;
    private float[] origin_Colour_b = null;

    private float[] grid10_Colour_r = null;
    private float[] grid10_Colour_g = null;
    private float[] grid10_Colour_b = null;

    private float[] grid_Colour_r = null;
    private float[] grid_Colour_g = null;
    private float[] grid_Colour_b = null;

    private float[] rubberBand_Colour_r = null;
    private float[] rubberBand_Colour_g = null;
    private float[] rubberBand_Colour_b = null;

    private float[] text_Colour_r = null;
    private float[] text_Colour_g = null;
    private float[] text_Colour_b = null;

    private float[] x_axis_Colour_r = null;
    private float[] x_axis_Colour_g = null;
    private float[] x_axis_Colour_b = null;

    private float[] y_axis_Colour_r = null;
    private float[] y_axis_Colour_g = null;
    private float[] y_axis_Colour_b = null;

    private float[] z_axis_Colour_r = null;
    private float[] z_axis_Colour_g = null;
    private float[] z_axis_Colour_b = null;

    private float[] primitive_background_Colour_r = null;
    private float[] primitive_background_Colour_g = null;
    private float[] primitive_background_Colour_b = null;

    private float[] primitive_signFG_Colour_r = null;
    private float[] primitive_signFG_Colour_g = null;
    private float[] primitive_signFG_Colour_b = null;

    private float[] primitive_signBG_Colour_r = null;
    private float[] primitive_signBG_Colour_g = null;
    private float[] primitive_signBG_Colour_b = null;

    private float[] primitive_plusNminus_Colour_r = null;
    private float[] primitive_plusNminus_Colour_g = null;
    private float[] primitive_plusNminus_Colour_b = null;

    private float[] primitive_selectedCell_Colour_r = null;
    private float[] primitive_selectedCell_Colour_g = null;
    private float[] primitive_selectedCell_Colour_b = null;

    private float[] primitive_focusedCell_Colour_r = null;
    private float[] primitive_focusedCell_Colour_g = null;
    private float[] primitive_focusedCell_Colour_b = null;

    private float[] primitive_normalCell_Colour_r = null;
    private float[] primitive_normalCell_Colour_g = null;
    private float[] primitive_normalCell_Colour_b = null;

    private float[] primitive_cell_1_Colour_r = null;
    private float[] primitive_cell_1_Colour_g = null;
    private float[] primitive_cell_1_Colour_b = null;

    private float[] primitive_cell_2_Colour_r = null;
    private float[] primitive_cell_2_Colour_g = null;
    private float[] primitive_cell_2_Colour_b = null;

    private float[] primitive_categoryCell_1_Colour_r = null;
    private float[] primitive_categoryCell_1_Colour_g = null;
    private float[] primitive_categoryCell_1_Colour_b = null;

    private float[] primitive_categoryCell_2_Colour_r = null;
    private float[] primitive_categoryCell_2_Colour_g = null;
    private float[] primitive_categoryCell_2_Colour_b = null;

    private float[] primitive_edge_Colour_r = null;
    private float[] primitive_edge_Colour_g = null;
    private float[] primitive_edge_Colour_b = null;

    private float[] primitive_condline_Colour_r = null;
    private float[] primitive_condline_Colour_g = null;
    private float[] primitive_condline_Colour_b = null;

    private int[] line_box_font_r = null;
    private int[] line_box_font_g = null;
    private int[] line_box_font_b = null;

    private int[] line_colourAttr_font_r = null;
    private int[] line_colourAttr_font_g = null;
    private int[] line_colourAttr_font_b = null;

    private int[] line_comment_font_r = null;
    private int[] line_comment_font_g = null;
    private int[] line_comment_font_b = null;

    private int[] line_error_underline_r = null;
    private int[] line_error_underline_g = null;
    private int[] line_error_underline_b = null;

    private int[] line_highlight_background_r = null;
    private int[] line_highlight_background_g = null;
    private int[] line_highlight_background_b = null;

    private int[] line_highlight_selected_background_r = null;
    private int[] line_highlight_selected_background_g = null;
    private int[] line_highlight_selected_background_b = null;

    private int[] line_hint_underline_r = null;
    private int[] line_hint_underline_g = null;
    private int[] line_hint_underline_b = null;

    private int[] line_primary_font_r = null;
    private int[] line_primary_font_g = null;
    private int[] line_primary_font_b = null;

    private int[] line_quad_font_r = null;
    private int[] line_quad_font_g = null;
    private int[] line_quad_font_b = null;

    private int[] line_secondary_font_r = null;
    private int[] line_secondary_font_g = null;
    private int[] line_secondary_font_b = null;

    private int[] line_warning_underline_r = null;
    private int[] line_warning_underline_g = null;
    private int[] line_warning_underline_b = null;

    private int[] text_background_r = null;
    private int[] text_background_g = null;
    private int[] text_background_b = null;

    private int[] text_foreground_r = null;
    private int[] text_foreground_g = null;
    private int[] text_foreground_b = null;

    private int[] text_foreground_hidden_r = null;
    private int[] text_foreground_hidden_g = null;
    private int[] text_foreground_hidden_b = null;

    private float[] cursor1_Colour_r = null;
    private float[] cursor1_Colour_g = null;
    private float[] cursor1_Colour_b = null;

    private float[] cursor2_Colour_r = null;
    private float[] cursor2_Colour_g = null;
    private float[] cursor2_Colour_b = null;

    private boolean syncingTabs = false;

    private int textWinArr = 2;

    private boolean roundX = false;
    private boolean roundY = false;
    private boolean roundZ = false;

    private transient int openGLVersion = 20;

    private boolean movingAdjacentData = false;

    private double coplanarity_angle_warning = 1d;
    private double coplanarity_angle_error = 3d;

    public UserSettingState() {
        this.getUserPalette().add(new GColour(0, 0.02f, 0.075f, 0.114f, 1f));

        this.getUserPalette().add(new GColour(1, 0f, 0.333f, 0.749f, 1f));
        this.getUserPalette().add(new GColour(2, 0.145f, 0.478f, 0.243f, 1f));
        this.getUserPalette().add(new GColour(3, 0f, 0.514f, 0.561f, 1f));
        this.getUserPalette().add(new GColour(4, 0.788f, 0.102f, 0.035f, 1f));

        this.getUserPalette().add(new GColour(5, 0.784f, 0.439f, 0.627f, 1f));
        this.getUserPalette().add(new GColour(6, 0.345f, 0.224f, 0.153f, 1f));
        this.getUserPalette().add(new GColour(7, 0.608f, 0.631f, 0.616f, 1f));
        this.getUserPalette().add(new GColour(72, 0.335f, 0.342f, 0.323f, 1f));

        this.getUserPalette().add(new GColour(9, 0.706f, 0.824f, 0.89f, 1f));
        this.getUserPalette().add(new GColour(10, 0.294f, 0.624f, 0.29f, 1f));
        this.getUserPalette().add(new GColour(11, 0.333f, 0.647f, 0.686f, 1f));
        this.getUserPalette().add(new GColour(12, 0.949f, 0.439f, 0.369f, 1f));

        this.getUserPalette().add(new GColour(13, 0.988f, 0.592f, 0.675f, 1f));
        this.getUserPalette().add(new GColour(14, 0.949f, 0.804f, 0.216f, 1f));
        this.getUserPalette().add(new GColour(15, 1f, 1f, 1f, 1f));
        this.getUserPalette().add(new GColour(16, 0.498f, 0.498f, 0.498f, 1f));

        syncWithTextEditor = new AtomicBoolean(true);
    }

    /**
     * @return where your part authoring folder is located.
     */
    public String getAuthoringFolderPath() {
        return authoringFolderPath;
    }

    /**
     * @param path
     *            the path where your part authoring folder is located.
     */
    public void setAuthoringFolderPath(String path) {
        this.authoringFolderPath = path;
    }

    /**
     * @return where your LDraw folder is located.
     */
    public String getLdrawFolderPath() {
        return ldrawFolderPath;
    }

    /**
     * @param path
     *            the path where your LDraw folder is located.
     */
    public void setLdrawFolderPath(String path) {
        this.ldrawFolderPath = path;
    }

    /**
     * @return where your unofficial parts folder is located.
     */
    public String getUnofficialFolderPath() {
        return unofficialFolderPath;
    }

    /**
     * @param path
     *            the path where your unofficial parts folder is located.
     */
    public void setUnofficialFolderPath(String path) {
        this.unofficialFolderPath = path;
    }

    /**
     * @return your LDraw user name.
     */
    public String getLdrawUserName() {
        return ldrawUserName;
    }

    /**
     * @param name
     *            your LDraw user name to set.
     */
    public void setLdrawUserName(String name) {
        this.ldrawUserName = name;
    }

    /**
     * @return the license under you want to publish your work.
     */
    public String getLicense() {
        return license;
    }

    /**
     * @param license
     *            your new license to set.
     */
    public void setLicense(String license) {
        this.license = license;
    }

    /**
     * @return your real name.
     */
    public String getRealUserName() {
        return realUserName;
    }

    /**
     * @param name
     *            your real name to set.
     */
    public void setRealUserName(String name) {
        this.realUserName = name;
    }

    /**
     * @return {@code true} if the user wants to use relative paths, which share
     *         a common base path.
     */
    public boolean isUsingRelativePaths() {
        return usingRelativePaths;
    }

    /**
     * @param usingRelativePaths
     *            Set to {@code true} if the user wants to use relative paths,
     *            which share a common base path.
     */
    public void setUsingRelativePaths(boolean usingRelativePaths) {
        this.usingRelativePaths = usingRelativePaths;
    }

    /**
     * @return your colour palette (17 colours)
     */
    public ArrayList<GColour> getUserPalette() {
        return userPalette;
    }

    /**
     * @param userPalette
     *            sets the colour palette (17 colours)
     */
    public void setUserPalette(ArrayList<GColour> userPalette) {
        this.userPalette = userPalette;
    }

    public BigDecimal getCoarse_move_snap() {
        return coarse_move_snap;
    }

    public void setCoarse_move_snap(BigDecimal coarse_move_snap) {
        this.coarse_move_snap = coarse_move_snap;
    }

    public BigDecimal getCoarse_rotate_snap() {
        return coarse_rotate_snap;
    }

    public void setCoarse_rotate_snap(BigDecimal coarse_rotate_snap) {
        this.coarse_rotate_snap = coarse_rotate_snap;
    }

    public BigDecimal getCoarse_scale_snap() {
        return coarse_scale_snap;
    }

    public void setCoarse_scale_snap(BigDecimal coarse_scale_snap) {
        this.coarse_scale_snap = coarse_scale_snap;
    }

    public BigDecimal getMedium_move_snap() {
        return medium_move_snap;
    }

    public void setMedium_move_snap(BigDecimal medium_move_snap) {
        this.medium_move_snap = medium_move_snap;
    }

    public BigDecimal getMedium_rotate_snap() {
        return medium_rotate_snap;
    }

    public void setMedium_rotate_snap(BigDecimal medium_rotate_snap) {
        this.medium_rotate_snap = medium_rotate_snap;
    }

    public BigDecimal getMedium_scale_snap() {
        return medium_scale_snap;
    }

    public void setMedium_scale_snap(BigDecimal medium_scale_snap) {
        this.medium_scale_snap = medium_scale_snap;
    }

    public BigDecimal getFine_move_snap() {
        return fine_move_snap;
    }

    public void setFine_move_snap(BigDecimal fine_move_snap) {
        this.fine_move_snap = fine_move_snap;
    }

    public BigDecimal getFine_rotate_snap() {
        return fine_rotate_snap;
    }

    public void setFine_rotate_snap(BigDecimal fine_rotate_snap) {
        this.fine_rotate_snap = fine_rotate_snap;
    }

    public BigDecimal getFine_scale_snap() {
        return fine_scale_snap;
    }

    public void setFine_scale_snap(BigDecimal fine_scale_snap) {
        this.fine_scale_snap = fine_scale_snap;
    }

    public int getTransMatrixPrecision() {
        return transMatrixPrecision;
    }

    public void setTransMatrixPrecision(int transMatrixPrecision) {
        this.transMatrixPrecision = transMatrixPrecision;
    }

    public int getCoordsPrecision() {
        return coordsPrecision;
    }

    public void setCoordsPrecision(int coordsPrecision) {
        this.coordsPrecision = coordsPrecision;
    }

    public boolean isResetOnStart() {
        return resetOnStart;
    }

    public void setResetOnStart(boolean resetOnStart) {
        this.resetOnStart = resetOnStart;
    }

    public String getLdConfigPath() {
        return ldConfigPath;
    }

    public void setLdConfigPath(String ldConfigPath) {
        this.ldConfigPath = ldConfigPath;
    }

    public AtomicBoolean getSyncWithTextEditor() {
        // return syncWithTextEditor;
        return new AtomicBoolean(true);
    }

    public void setSyncWithTextEditor(AtomicBoolean syncWithTextEditor) {
        this.syncWithTextEditor = syncWithTextEditor;
    }

    public AtomicBoolean getSyncWithLpeInline() {
        return syncWithLpeInline;
    }

    public void setSyncWithLpeInline(AtomicBoolean syncWithLpeInline) {
        this.syncWithLpeInline = syncWithLpeInline;
    }

    public int getIconSize() {
        return iconSize;
    }

    public void setIconSize(int iconSize) {
        this.iconSize = iconSize;
    }

    public float[] getManipulatorSize() {
        return manipulatorSize;
    }

    public void setManipulatorSize(float[] manipulatorSize) {
        this.manipulatorSize = manipulatorSize;
    }

    public ArrayList<String> getRecentItems() {
        return recentItems;
    }

    public void setRecentItems(ArrayList<String> recentItems) {
        this.recentItems = recentItems;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public boolean isBfcCertificationRequiredForLDrawMode() {
        return bfcCertificationRequiredForLDrawMode;
    }

    public void setBfcCertificationRequiredForLDrawMode(boolean bfcCertificationRequiredForLDrawMode) {
        this.bfcCertificationRequiredForLDrawMode = bfcCertificationRequiredForLDrawMode;
    }

    public ArrayList<ToolItemState> getToolItemConfig3D() {
        return toolItemConfig3D;
    }

    public void setToolItemConfig3D(ArrayList<ToolItemState> toolItemConfig3D) {
        this.toolItemConfig3D = toolItemConfig3D;
    }

    public void loadShortkeys() {
        if (key3DStrings != null && key3DKeys != null && key3DTasks != null) {
            final int size = key3DStrings.length;
            for (int i = 0; i < size; i++) {
                final String oldKey = KeyStateManager.getMapKey(key3DTasks[i]);
                if (oldKey != null) {
                    KeyStateManager.changeKey(oldKey, key3DKeys[i], key3DStrings[i], key3DTasks[i]);
                }
            }
        }

        if (keyTextStrings != null && keyTextKeys != null && keyTextTasks != null) {
            final int size = keyTextStrings.length;
            for (int i = 0; i < size; i++) {
                final String oldKey = KeyStateManager.getMapKey(keyTextTasks[i]);
                if (oldKey != null) {
                    KeyStateManager.changeKey(oldKey, keyTextKeys[i], keyTextStrings[i], keyTextTasks[i]);
                }
            }
        }
    }

    public void saveShortkeys() {
        HashMap<String, Task> m1 = KeyStateManager.getTaskmap();
        HashMap<Task, String> m2 = KeyStateManager.getTaskKeymap();
        HashMap<String, TextTask> m3 = KeyStateManager.getTextTaskmap();
        HashMap<TextTask, String> m4 = KeyStateManager.getTextTaskKeymap();

        int size1 = m1.size();
        int size2 = m3.size();

        key3DStrings = new String[size1];
        key3DKeys = new String[size1];
        key3DTasks = new Task[size1];

        keyTextStrings = new String[size2];
        keyTextKeys = new String[size2];
        keyTextTasks = new TextTask[size2];

        {
            int i = 0;
            for (String k : m1.keySet()) {
                Task t = m1.get(k);
                String keyString = m2.get(t);
                key3DStrings[i] = keyString;
                key3DKeys[i] = k;
                key3DTasks[i] = t;
                i++;
            }
        }

        {
            int i = 0;
            for (String k : m3.keySet()) {
                TextTask t = m3.get(k);
                String keyString = m4.get(t);
                keyTextStrings[i] = keyString;
                keyTextKeys[i] = k;
                keyTextTasks[i] = t;
                i++;
            }
        }
    }

    public boolean isAntiAliasing() {
        return antiAliasing;
    }

    public void setAntiAliasing(boolean antiAliasing) {
        this.antiAliasing = antiAliasing;
    }

    public boolean isOpenGL33Engine() {
        return newEngine;
    }

    public void setOpenGL33Engine(boolean openGL33Engine) {
        this.newEngine = openGL33Engine;
    }

    public boolean isVulkanEngine() {
        return vulkanEngine;
    }

    public void setVulkanEngine(boolean vulkanEngine) {
        this.vulkanEngine = vulkanEngine;
    }

    public void saveColours() {

        Color16_override_r = View.Color16_override_r;
        Color16_override_g = View.Color16_override_g;
        Color16_override_b = View.Color16_override_b;

        BFC_front_Colour_r = View.BFC_front_Colour_r;
        BFC_front_Colour_g = View.BFC_front_Colour_g;
        BFC_front_Colour_b = View.BFC_front_Colour_b;

        BFC_back__Colour_r = View.BFC_back__Colour_r;
        BFC_back__Colour_g = View.BFC_back__Colour_g;
        BFC_back__Colour_b = View.BFC_back__Colour_b;

        BFC_uncertified_Colour_r = View.BFC_uncertified_Colour_r;
        BFC_uncertified_Colour_g = View.BFC_uncertified_Colour_g;
        BFC_uncertified_Colour_b = View.BFC_uncertified_Colour_b;

        vertex_Colour_r = View.vertex_Colour_r;
        vertex_Colour_g = View.vertex_Colour_g;
        vertex_Colour_b = View.vertex_Colour_b;

        vertex_selected_Colour_r = View.vertex_selected_Colour_r;
        vertex_selected_Colour_g = View.vertex_selected_Colour_g;
        vertex_selected_Colour_b = View.vertex_selected_Colour_b;

        condline_selected_Colour_r = View.condline_selected_Colour_r;
        condline_selected_Colour_g = View.condline_selected_Colour_g;
        condline_selected_Colour_b = View.condline_selected_Colour_b;

        line_Colour_r = View.line_Colour_r;
        line_Colour_g = View.line_Colour_g;
        line_Colour_b = View.line_Colour_b;

        meshline_Colour_r = View.meshline_Colour_r;
        meshline_Colour_g = View.meshline_Colour_g;
        meshline_Colour_b = View.meshline_Colour_b;

        condline_Colour_r = View.condline_Colour_r;
        condline_Colour_g = View.condline_Colour_g;
        condline_Colour_b = View.condline_Colour_b;

        condline_hidden_Colour_r = View.condline_hidden_Colour_r;
        condline_hidden_Colour_g = View.condline_hidden_Colour_g;
        condline_hidden_Colour_b = View.condline_hidden_Colour_b;

        condline_shown_Colour_r = View.condline_shown_Colour_r;
        condline_shown_Colour_g = View.condline_shown_Colour_g;
        condline_shown_Colour_b = View.condline_shown_Colour_b;

        background_Colour_r = View.background_Colour_r;
        background_Colour_g = View.background_Colour_g;
        background_Colour_b = View.background_Colour_b;

        light1_Colour_r = View.light1_Colour_r;
        light1_Colour_g = View.light1_Colour_g;
        light1_Colour_b = View.light1_Colour_b;

        light1_specular_Colour_r = View.light1_specular_Colour_r;
        light1_specular_Colour_g = View.light1_specular_Colour_g;
        light1_specular_Colour_b = View.light1_specular_Colour_b;

        light2_Colour_r = View.light2_Colour_r;
        light2_Colour_g = View.light2_Colour_g;
        light2_Colour_b = View.light2_Colour_b;

        light2_specular_Colour_r = View.light2_specular_Colour_r;
        light2_specular_Colour_g = View.light2_specular_Colour_g;
        light2_specular_Colour_b = View.light2_specular_Colour_b;

        light3_Colour_r = View.light3_Colour_r;
        light3_Colour_g = View.light3_Colour_g;
        light3_Colour_b = View.light3_Colour_b;

        light3_specular_Colour_r = View.light3_specular_Colour_r;
        light3_specular_Colour_g = View.light3_specular_Colour_g;
        light3_specular_Colour_b = View.light3_specular_Colour_b;

        light4_Colour_r = View.light4_Colour_r;
        light4_Colour_g = View.light4_Colour_g;
        light4_Colour_b = View.light4_Colour_b;

        light4_specular_Colour_r = View.light4_specular_Colour_r;
        light4_specular_Colour_g = View.light4_specular_Colour_g;
        light4_specular_Colour_b = View.light4_specular_Colour_b;

        manipulator_selected_Colour_r = View.manipulator_selected_Colour_r;
        manipulator_selected_Colour_g = View.manipulator_selected_Colour_g;
        manipulator_selected_Colour_b = View.manipulator_selected_Colour_b;

        manipulator_innerCircle_Colour_r = View.manipulator_innerCircle_Colour_r;
        manipulator_innerCircle_Colour_g = View.manipulator_innerCircle_Colour_g;
        manipulator_innerCircle_Colour_b = View.manipulator_innerCircle_Colour_b;

        manipulator_outerCircle_Colour_r = View.manipulator_outerCircle_Colour_r;
        manipulator_outerCircle_Colour_g = View.manipulator_outerCircle_Colour_g;
        manipulator_outerCircle_Colour_b = View.manipulator_outerCircle_Colour_b;

        manipulator_x_axis_Colour_r = View.manipulator_x_axis_Colour_r;
        manipulator_x_axis_Colour_g = View.manipulator_x_axis_Colour_g;
        manipulator_x_axis_Colour_b = View.manipulator_x_axis_Colour_b;

        manipulator_y_axis_Colour_r = View.manipulator_y_axis_Colour_r;
        manipulator_y_axis_Colour_g = View.manipulator_y_axis_Colour_g;
        manipulator_y_axis_Colour_b = View.manipulator_y_axis_Colour_b;

        manipulator_z_axis_Colour_r = View.manipulator_z_axis_Colour_r;
        manipulator_z_axis_Colour_g = View.manipulator_z_axis_Colour_g;
        manipulator_z_axis_Colour_b = View.manipulator_z_axis_Colour_b;

        add_Object_Colour_r = View.add_Object_Colour_r;
        add_Object_Colour_g = View.add_Object_Colour_g;
        add_Object_Colour_b = View.add_Object_Colour_b;

        origin_Colour_r = View.origin_Colour_r;
        origin_Colour_g = View.origin_Colour_g;
        origin_Colour_b = View.origin_Colour_b;

        grid10_Colour_r = View.grid10_Colour_r;
        grid10_Colour_g = View.grid10_Colour_g;
        grid10_Colour_b = View.grid10_Colour_b;

        grid_Colour_r = View.grid_Colour_r;
        grid_Colour_g = View.grid_Colour_b;
        grid_Colour_b = View.grid_Colour_b;

        rubberBand_Colour_r = View.rubberBand_Colour_r;
        rubberBand_Colour_g = View.rubberBand_Colour_g;
        rubberBand_Colour_b = View.rubberBand_Colour_b;

        text_Colour_r = View.text_Colour_r;
        text_Colour_g = View.text_Colour_g;
        text_Colour_b = View.text_Colour_b;

        x_axis_Colour_r = View.x_axis_Colour_r;
        x_axis_Colour_g = View.x_axis_Colour_g;
        x_axis_Colour_b = View.x_axis_Colour_b;

        y_axis_Colour_r = View.y_axis_Colour_r;
        y_axis_Colour_g = View.y_axis_Colour_g;
        y_axis_Colour_b = View.y_axis_Colour_b;

        z_axis_Colour_r = View.z_axis_Colour_r;
        z_axis_Colour_g = View.z_axis_Colour_g;
        z_axis_Colour_b = View.z_axis_Colour_b;

        primitive_background_Colour_r = View.primitive_background_Colour_r;
        primitive_background_Colour_g = View.primitive_background_Colour_g;
        primitive_background_Colour_b = View.primitive_background_Colour_b;

        primitive_signFG_Colour_r = View.primitive_signFG_Colour_r;
        primitive_signFG_Colour_g = View.primitive_signFG_Colour_g;
        primitive_signFG_Colour_b = View.primitive_signFG_Colour_b;

        primitive_signBG_Colour_r = View.primitive_signBG_Colour_r;
        primitive_signBG_Colour_g = View.primitive_signBG_Colour_g;
        primitive_signBG_Colour_b = View.primitive_signBG_Colour_b;

        primitive_plusNminus_Colour_r = View.primitive_plusNminus_Colour_r;
        primitive_plusNminus_Colour_g = View.primitive_plusNminus_Colour_g;
        primitive_plusNminus_Colour_b = View.primitive_plusNminus_Colour_b;

        primitive_selectedCell_Colour_r = View.primitive_selectedCell_Colour_r;
        primitive_selectedCell_Colour_g = View.primitive_selectedCell_Colour_g;
        primitive_selectedCell_Colour_b = View.primitive_selectedCell_Colour_b;

        primitive_focusedCell_Colour_r = View.primitive_focusedCell_Colour_r;
        primitive_focusedCell_Colour_g = View.primitive_focusedCell_Colour_g;
        primitive_focusedCell_Colour_b = View.primitive_focusedCell_Colour_b;

        primitive_normalCell_Colour_r = View.primitive_normalCell_Colour_r;
        primitive_normalCell_Colour_g = View.primitive_normalCell_Colour_g;
        primitive_normalCell_Colour_b = View.primitive_normalCell_Colour_b;

        primitive_cell_1_Colour_r = View.primitive_cell_1_Colour_r;
        primitive_cell_1_Colour_g = View.primitive_cell_1_Colour_g;
        primitive_cell_1_Colour_b = View.primitive_cell_1_Colour_b;

        primitive_cell_2_Colour_r = View.primitive_cell_2_Colour_r;
        primitive_cell_2_Colour_g = View.primitive_cell_2_Colour_g;
        primitive_cell_2_Colour_b = View.primitive_cell_2_Colour_b;

        primitive_categoryCell_1_Colour_r = View.primitive_categoryCell_1_Colour_r;
        primitive_categoryCell_1_Colour_g = View.primitive_categoryCell_1_Colour_g;
        primitive_categoryCell_1_Colour_b = View.primitive_categoryCell_1_Colour_b;

        primitive_categoryCell_2_Colour_r = View.primitive_categoryCell_2_Colour_r;
        primitive_categoryCell_2_Colour_g = View.primitive_categoryCell_2_Colour_g;
        primitive_categoryCell_2_Colour_b = View.primitive_categoryCell_2_Colour_b;

        primitive_edge_Colour_r = View.primitive_edge_Colour_r;
        primitive_edge_Colour_g = View.primitive_edge_Colour_g;
        primitive_edge_Colour_b = View.primitive_edge_Colour_b;

        primitive_condline_Colour_r = View.primitive_condline_Colour_r;
        primitive_condline_Colour_g = View.primitive_condline_Colour_g;
        primitive_condline_Colour_b = View.primitive_condline_Colour_b;

        line_box_font_r = new int[]{Colour.line_box_font[0].getRed()};
        line_box_font_g = new int[]{Colour.line_box_font[0].getGreen()};
        line_box_font_b = new int[]{Colour.line_box_font[0].getBlue()};

        line_colourAttr_font_r = new int[]{Colour.line_colourAttr_font[0].getRed()};
        line_colourAttr_font_g = new int[]{Colour.line_colourAttr_font[0].getGreen()};
        line_colourAttr_font_b = new int[]{Colour.line_colourAttr_font[0].getBlue()};

        line_comment_font_r = new int[]{Colour.line_comment_font[0].getRed()};
        line_comment_font_g = new int[]{Colour.line_comment_font[0].getGreen()};
        line_comment_font_b = new int[]{Colour.line_comment_font[0].getBlue()};

        line_error_underline_r = new int[]{Colour.line_error_underline[0].getRed()};
        line_error_underline_g = new int[]{Colour.line_error_underline[0].getGreen()};
        line_error_underline_b = new int[]{Colour.line_error_underline[0].getBlue()};

        line_highlight_background_r = new int[]{Colour.line_highlight_background[0].getRed()};
        line_highlight_background_g = new int[]{Colour.line_highlight_background[0].getGreen()};
        line_highlight_background_b = new int[]{Colour.line_highlight_background[0].getBlue()};

        line_highlight_selected_background_r = new int[]{Colour.line_highlight_selected_background[0].getRed()};
        line_highlight_selected_background_g = new int[]{Colour.line_highlight_selected_background[0].getGreen()};
        line_highlight_selected_background_b = new int[]{Colour.line_highlight_selected_background[0].getBlue()};

        line_hint_underline_r = new int[]{Colour.line_hint_underline[0].getRed()};
        line_hint_underline_g = new int[]{Colour.line_hint_underline[0].getGreen()};
        line_hint_underline_b = new int[]{Colour.line_hint_underline[0].getBlue()};

        line_primary_font_r = new int[]{Colour.line_primary_font[0].getRed()};
        line_primary_font_g = new int[]{Colour.line_primary_font[0].getGreen()};
        line_primary_font_b = new int[]{Colour.line_primary_font[0].getBlue()};

        line_quad_font_r = new int[]{Colour.line_quad_font[0].getRed()};
        line_quad_font_g = new int[]{Colour.line_quad_font[0].getGreen()};
        line_quad_font_b = new int[]{Colour.line_quad_font[0].getBlue()};

        line_secondary_font_r = new int[]{Colour.line_secondary_font[0].getRed()};
        line_secondary_font_g = new int[]{Colour.line_secondary_font[0].getGreen()};
        line_secondary_font_b = new int[]{Colour.line_secondary_font[0].getBlue()};

        line_warning_underline_r = new int[]{Colour.line_warning_underline[0].getRed()};
        line_warning_underline_g = new int[]{Colour.line_warning_underline[0].getGreen()};
        line_warning_underline_b = new int[]{Colour.line_warning_underline[0].getBlue()};

        text_background_r = new int[]{Colour.text_background[0].getRed()};
        text_background_g = new int[]{Colour.text_background[0].getGreen()};
        text_background_b = new int[]{Colour.text_background[0].getBlue()};

        text_foreground_r = new int[]{Colour.text_foreground[0].getRed()};
        text_foreground_g = new int[]{Colour.text_foreground[0].getGreen()};
        text_foreground_b = new int[]{Colour.text_foreground[0].getBlue()};

        text_foreground_hidden_r = new int[]{Colour.text_foreground_hidden[0].getRed()};
        text_foreground_hidden_g = new int[]{Colour.text_foreground_hidden[0].getGreen()};
        text_foreground_hidden_b = new int[]{Colour.text_foreground_hidden[0].getBlue()};

        cursor1_Colour_r = View.cursor1_Colour_r;
        cursor1_Colour_g = View.cursor1_Colour_g;
        cursor1_Colour_b = View.cursor1_Colour_b;

        cursor2_Colour_r = View.cursor2_Colour_r;
        cursor2_Colour_g = View.cursor2_Colour_g;
        cursor2_Colour_b = View.cursor2_Colour_b;
    }

    public void loadColours() {

        if (Color16_override_r != null) View.Color16_override_r[0] = Color16_override_r[0];
        if (Color16_override_g != null) View.Color16_override_g[0] = Color16_override_g[0];
        if (Color16_override_b != null) View.Color16_override_b[0] = Color16_override_b[0];

        if (BFC_front_Colour_r != null) View.BFC_front_Colour_r[0] = BFC_front_Colour_r[0];
        if (BFC_front_Colour_g != null) View.BFC_front_Colour_g[0] = BFC_front_Colour_g[0];
        if (BFC_front_Colour_b != null) View.BFC_front_Colour_b[0] = BFC_front_Colour_b[0];

        if (BFC_back__Colour_r != null) View.BFC_back__Colour_r[0] = BFC_back__Colour_r[0];
        if (BFC_back__Colour_g != null) View.BFC_back__Colour_g[0] = BFC_back__Colour_g[0];
        if (BFC_back__Colour_b != null) View.BFC_back__Colour_b[0] = BFC_back__Colour_b[0];

        if (BFC_uncertified_Colour_r != null) View.BFC_uncertified_Colour_r[0] = BFC_uncertified_Colour_r[0];
        if (BFC_uncertified_Colour_g != null) View.BFC_uncertified_Colour_g[0] = BFC_uncertified_Colour_g[0];
        if (BFC_uncertified_Colour_b != null) View.BFC_uncertified_Colour_b[0] = BFC_uncertified_Colour_b[0];

        if (vertex_Colour_r != null) View.vertex_Colour_r[0] = vertex_Colour_r[0];
        if (vertex_Colour_g != null) View.vertex_Colour_g[0] = vertex_Colour_g[0];
        if (vertex_Colour_b != null) View.vertex_Colour_b[0] = vertex_Colour_b[0];

        if (vertex_selected_Colour_r != null) View.vertex_selected_Colour_r[0] = vertex_selected_Colour_r[0];
        if (vertex_selected_Colour_g != null) View.vertex_selected_Colour_g[0] = vertex_selected_Colour_g[0];
        if (vertex_selected_Colour_b != null) View.vertex_selected_Colour_b[0] = vertex_selected_Colour_b[0];

        if (condline_selected_Colour_r != null) View.condline_selected_Colour_r[0] = condline_selected_Colour_r[0];
        if (condline_selected_Colour_g != null) View.condline_selected_Colour_g[0] = condline_selected_Colour_g[0];
        if (condline_selected_Colour_b != null) View.condline_selected_Colour_b[0] = condline_selected_Colour_b[0];

        if (line_Colour_r != null) View.line_Colour_r[0] = line_Colour_r[0];
        if (line_Colour_g != null) View.line_Colour_g[0] = line_Colour_g[0];
        if (line_Colour_b != null) View.line_Colour_b[0] = line_Colour_b[0];

        if (meshline_Colour_r != null) View.meshline_Colour_r[0] = meshline_Colour_r[0];
        if (meshline_Colour_g != null) View.meshline_Colour_g[0] = meshline_Colour_g[0];
        if (meshline_Colour_b != null) View.meshline_Colour_b[0] = meshline_Colour_b[0];

        if (condline_Colour_r != null) View.condline_Colour_r[0] = condline_Colour_r[0];
        if (condline_Colour_g != null) View.condline_Colour_g[0] = condline_Colour_g[0];
        if (condline_Colour_b != null) View.condline_Colour_b[0] = condline_Colour_b[0];

        if (condline_hidden_Colour_r != null) View.condline_hidden_Colour_r[0] = condline_hidden_Colour_r[0];
        if (condline_hidden_Colour_g != null) View.condline_hidden_Colour_g[0] = condline_hidden_Colour_g[0];
        if (condline_hidden_Colour_b != null) View.condline_hidden_Colour_b[0] = condline_hidden_Colour_b[0];

        if (condline_shown_Colour_r != null) View.condline_shown_Colour_r[0] = condline_shown_Colour_r[0];
        if (condline_shown_Colour_g != null) View.condline_shown_Colour_g[0] = condline_shown_Colour_g[0];
        if (condline_shown_Colour_b != null) View.condline_shown_Colour_b[0] = condline_shown_Colour_b[0];

        if (background_Colour_r != null) View.background_Colour_r[0] = background_Colour_r[0];
        if (background_Colour_g != null) View.background_Colour_g[0] = background_Colour_g[0];
        if (background_Colour_b != null) View.background_Colour_b[0] = background_Colour_b[0];

        if (light1_Colour_r != null) View.light1_Colour_r[0] = light1_Colour_r[0];
        if (light1_Colour_g != null) View.light1_Colour_g[0] = light1_Colour_g[0];
        if (light1_Colour_b != null) View.light1_Colour_b[0] = light1_Colour_b[0];

        if (light1_specular_Colour_r != null) View.light1_specular_Colour_r[0] = light1_specular_Colour_r[0];
        if (light1_specular_Colour_g != null) View.light1_specular_Colour_g[0] = light1_specular_Colour_g[0];
        if (light1_specular_Colour_b != null) View.light1_specular_Colour_b[0] = light1_specular_Colour_b[0];

        if (light2_Colour_r != null) View.light2_Colour_r[0] = light2_Colour_r[0];
        if (light2_Colour_g != null) View.light2_Colour_g[0] = light2_Colour_g[0];
        if (light2_Colour_b != null) View.light2_Colour_b[0] = light2_Colour_b[0];

        if (light2_specular_Colour_r != null) View.light2_specular_Colour_r[0] = light2_specular_Colour_r[0];
        if (light2_specular_Colour_g != null) View.light2_specular_Colour_g[0] = light2_specular_Colour_g[0];
        if (light2_specular_Colour_b != null) View.light2_specular_Colour_b[0] = light2_specular_Colour_b[0];

        if (light3_Colour_r != null) View.light3_Colour_r[0] = light3_Colour_r[0];
        if (light3_Colour_g != null) View.light3_Colour_g[0] = light3_Colour_g[0];
        if (light3_Colour_b != null) View.light3_Colour_b[0] = light3_Colour_b[0];

        if (light3_specular_Colour_r != null) View.light3_specular_Colour_r[0] = light3_specular_Colour_r[0];
        if (light3_specular_Colour_g != null) View.light3_specular_Colour_g[0] = light3_specular_Colour_g[0];
        if (light3_specular_Colour_b != null) View.light3_specular_Colour_b[0] = light3_specular_Colour_b[0];

        if (light4_Colour_r != null) View.light4_Colour_r[0] = light4_Colour_r[0];
        if (light4_Colour_g != null) View.light4_Colour_g[0] = light4_Colour_g[0];
        if (light4_Colour_b != null) View.light4_Colour_b[0] = light4_Colour_b[0];

        if (light4_specular_Colour_r != null) View.light4_specular_Colour_r[0] = light4_specular_Colour_r[0];
        if (light4_specular_Colour_g != null) View.light4_specular_Colour_g[0] = light4_specular_Colour_g[0];
        if (light4_specular_Colour_b != null) View.light4_specular_Colour_b[0] = light4_specular_Colour_b[0];

        if (manipulator_selected_Colour_r != null) View.manipulator_selected_Colour_r[0] = manipulator_selected_Colour_r[0];
        if (manipulator_selected_Colour_g != null) View.manipulator_selected_Colour_g[0] = manipulator_selected_Colour_g[0];
        if (manipulator_selected_Colour_b != null) View.manipulator_selected_Colour_b[0] = manipulator_selected_Colour_b[0];

        if (manipulator_innerCircle_Colour_r != null) View.manipulator_innerCircle_Colour_r[0] = manipulator_innerCircle_Colour_r[0];
        if (manipulator_innerCircle_Colour_g != null) View.manipulator_innerCircle_Colour_g[0] = manipulator_innerCircle_Colour_g[0];
        if (manipulator_innerCircle_Colour_b != null) View.manipulator_innerCircle_Colour_b[0] = manipulator_innerCircle_Colour_b[0];

        if (manipulator_outerCircle_Colour_r != null) View.manipulator_outerCircle_Colour_r[0] = manipulator_outerCircle_Colour_r[0];
        if (manipulator_outerCircle_Colour_g != null) View.manipulator_outerCircle_Colour_g[0] = manipulator_outerCircle_Colour_g[0];
        if (manipulator_outerCircle_Colour_b != null) View.manipulator_outerCircle_Colour_b[0] = manipulator_outerCircle_Colour_b[0];

        if (manipulator_x_axis_Colour_r != null) View.manipulator_x_axis_Colour_r[0] = manipulator_x_axis_Colour_r[0];
        if (manipulator_x_axis_Colour_g != null) View.manipulator_x_axis_Colour_g[0] = manipulator_x_axis_Colour_g[0];
        if (manipulator_x_axis_Colour_b != null) View.manipulator_x_axis_Colour_b[0] = manipulator_x_axis_Colour_b[0];

        if (manipulator_y_axis_Colour_r != null) View.manipulator_y_axis_Colour_r[0] = manipulator_y_axis_Colour_r[0];
        if (manipulator_y_axis_Colour_g != null) View.manipulator_y_axis_Colour_g[0] = manipulator_y_axis_Colour_g[0];
        if (manipulator_y_axis_Colour_b != null) View.manipulator_y_axis_Colour_b[0] = manipulator_y_axis_Colour_b[0];

        if (manipulator_z_axis_Colour_r != null) View.manipulator_z_axis_Colour_r[0] = manipulator_z_axis_Colour_r[0];
        if (manipulator_z_axis_Colour_g != null) View.manipulator_z_axis_Colour_g[0] = manipulator_z_axis_Colour_g[0];
        if (manipulator_z_axis_Colour_b != null) View.manipulator_z_axis_Colour_b[0] = manipulator_z_axis_Colour_b[0];

        if (add_Object_Colour_r != null) View.add_Object_Colour_r[0] = add_Object_Colour_r[0];
        if (add_Object_Colour_g != null) View.add_Object_Colour_g[0] = add_Object_Colour_g[0];
        if (add_Object_Colour_b != null) View.add_Object_Colour_b[0] = add_Object_Colour_b[0];

        if (origin_Colour_r != null) View.origin_Colour_r[0] = origin_Colour_r[0];
        if (origin_Colour_g != null) View.origin_Colour_g[0] = origin_Colour_g[0];
        if (origin_Colour_b != null) View.origin_Colour_b[0] = origin_Colour_b[0];

        if (grid10_Colour_r != null) View.grid10_Colour_r[0] = grid10_Colour_r[0];
        if (grid10_Colour_g != null) View.grid10_Colour_g[0] = grid10_Colour_g[0];
        if (grid10_Colour_b != null) View.grid10_Colour_b[0] = grid10_Colour_b[0];

        if (grid_Colour_r != null) View.grid_Colour_r[0] = grid_Colour_r[0];
        if (grid_Colour_g != null) View.grid_Colour_g[0] = grid_Colour_g[0];
        if (grid_Colour_b != null) View.grid_Colour_b[0] = grid_Colour_b[0];

        if (rubberBand_Colour_r != null) View.rubberBand_Colour_r[0] = rubberBand_Colour_r[0];
        if (rubberBand_Colour_g != null) View.rubberBand_Colour_g[0] = rubberBand_Colour_g[0];
        if (rubberBand_Colour_b != null) View.rubberBand_Colour_b[0] = rubberBand_Colour_b[0];

        if (text_Colour_r != null) View.text_Colour_r[0] = text_Colour_r[0];
        if (text_Colour_g != null) View.text_Colour_g[0] = text_Colour_g[0];
        if (text_Colour_b != null) View.text_Colour_b[0] = text_Colour_b[0];

        if (x_axis_Colour_r != null) View.x_axis_Colour_r[0] = x_axis_Colour_r[0];
        if (x_axis_Colour_g != null) View.x_axis_Colour_g[0] = x_axis_Colour_g[0];
        if (x_axis_Colour_b != null) View.x_axis_Colour_b[0] = x_axis_Colour_b[0];

        if (y_axis_Colour_r != null) View.y_axis_Colour_r[0] = y_axis_Colour_r[0];
        if (y_axis_Colour_g != null) View.y_axis_Colour_g[0] = y_axis_Colour_g[0];
        if (y_axis_Colour_b != null) View.y_axis_Colour_b[0] = y_axis_Colour_b[0];

        if (z_axis_Colour_r != null) View.z_axis_Colour_r[0] = z_axis_Colour_r[0];
        if (z_axis_Colour_g != null) View.z_axis_Colour_g[0] = z_axis_Colour_g[0];
        if (z_axis_Colour_b != null) View.z_axis_Colour_b[0] = z_axis_Colour_b[0];

        if (primitive_background_Colour_r != null) View.primitive_background_Colour_r[0] = primitive_background_Colour_r[0];
        if (primitive_background_Colour_g != null) View.primitive_background_Colour_g[0] = primitive_background_Colour_g[0];
        if (primitive_background_Colour_b != null) View.primitive_background_Colour_b[0] = primitive_background_Colour_b[0];

        if (primitive_signFG_Colour_r != null) View.primitive_signFG_Colour_r[0] = primitive_signFG_Colour_r[0];
        if (primitive_signFG_Colour_g != null) View.primitive_signFG_Colour_g[0] = primitive_signFG_Colour_g[0];
        if (primitive_signFG_Colour_b != null) View.primitive_signFG_Colour_b[0] = primitive_signFG_Colour_b[0];

        if (primitive_signBG_Colour_r != null) View.primitive_signBG_Colour_r[0] = primitive_signBG_Colour_r[0];
        if (primitive_signBG_Colour_g != null) View.primitive_signBG_Colour_g[0] = primitive_signBG_Colour_g[0];
        if (primitive_signBG_Colour_b != null) View.primitive_signBG_Colour_b[0] = primitive_signBG_Colour_b[0];

        if (primitive_plusNminus_Colour_r != null) View.primitive_plusNminus_Colour_r[0] = primitive_plusNminus_Colour_r[0];
        if (primitive_plusNminus_Colour_g != null) View.primitive_plusNminus_Colour_g[0] = primitive_plusNminus_Colour_g[0];
        if (primitive_plusNminus_Colour_b != null) View.primitive_plusNminus_Colour_b[0] = primitive_plusNminus_Colour_b[0];

        if (primitive_selectedCell_Colour_r != null) View.primitive_selectedCell_Colour_r[0] = primitive_selectedCell_Colour_r[0];
        if (primitive_selectedCell_Colour_g != null) View.primitive_selectedCell_Colour_g[0] = primitive_selectedCell_Colour_g[0];
        if (primitive_selectedCell_Colour_b != null) View.primitive_selectedCell_Colour_b[0] = primitive_selectedCell_Colour_b[0];

        if (primitive_focusedCell_Colour_r != null) View.primitive_focusedCell_Colour_r[0] = primitive_focusedCell_Colour_r[0];
        if (primitive_focusedCell_Colour_g != null) View.primitive_focusedCell_Colour_g[0] = primitive_focusedCell_Colour_g[0];
        if (primitive_focusedCell_Colour_b != null) View.primitive_focusedCell_Colour_b[0] = primitive_focusedCell_Colour_b[0];

        if (primitive_normalCell_Colour_r != null) View.primitive_normalCell_Colour_r[0] = primitive_normalCell_Colour_r[0];
        if (primitive_normalCell_Colour_g != null) View.primitive_normalCell_Colour_g[0] = primitive_normalCell_Colour_g[0];
        if (primitive_normalCell_Colour_b != null) View.primitive_normalCell_Colour_b[0] = primitive_normalCell_Colour_b[0];

        if (primitive_cell_1_Colour_r != null) View.primitive_cell_1_Colour_r[0] = primitive_cell_1_Colour_r[0];
        if (primitive_cell_1_Colour_g != null) View.primitive_cell_1_Colour_g[0] = primitive_cell_1_Colour_g[0];
        if (primitive_cell_1_Colour_b != null) View.primitive_cell_1_Colour_b[0] = primitive_cell_1_Colour_b[0];

        if (primitive_cell_2_Colour_r != null) View.primitive_cell_2_Colour_r[0] = primitive_cell_2_Colour_r[0];
        if (primitive_cell_2_Colour_g != null) View.primitive_cell_2_Colour_g[0] = primitive_cell_2_Colour_g[0];
        if (primitive_cell_2_Colour_b != null) View.primitive_cell_2_Colour_b[0] = primitive_cell_2_Colour_b[0];

        if (primitive_categoryCell_1_Colour_r != null) View.primitive_categoryCell_1_Colour_r[0] = primitive_categoryCell_1_Colour_r[0];
        if (primitive_categoryCell_1_Colour_g != null) View.primitive_categoryCell_1_Colour_g[0] = primitive_categoryCell_1_Colour_g[0];
        if (primitive_categoryCell_1_Colour_b != null) View.primitive_categoryCell_1_Colour_b[0] = primitive_categoryCell_1_Colour_b[0];

        if (primitive_categoryCell_2_Colour_r != null) View.primitive_categoryCell_2_Colour_r[0] = primitive_categoryCell_2_Colour_r[0];
        if (primitive_categoryCell_2_Colour_g != null) View.primitive_categoryCell_2_Colour_g[0] = primitive_categoryCell_2_Colour_g[0];
        if (primitive_categoryCell_2_Colour_b != null) View.primitive_categoryCell_2_Colour_b[0] = primitive_categoryCell_2_Colour_b[0];

        if (primitive_edge_Colour_r != null) View.primitive_edge_Colour_r[0] = primitive_edge_Colour_r[0];
        if (primitive_edge_Colour_g != null) View.primitive_edge_Colour_g[0] = primitive_edge_Colour_g[0];
        if (primitive_edge_Colour_b != null) View.primitive_edge_Colour_b[0] = primitive_edge_Colour_b[0];

        if (primitive_condline_Colour_r != null) View.primitive_condline_Colour_r[0] = primitive_condline_Colour_r[0];
        if (primitive_condline_Colour_g != null) View.primitive_condline_Colour_g[0] = primitive_condline_Colour_g[0];
        if (primitive_condline_Colour_b != null) View.primitive_condline_Colour_b[0] = primitive_condline_Colour_b[0];

        if (cursor1_Colour_r != null) View.cursor1_Colour_r[0] = cursor1_Colour_r[0];
        if (cursor1_Colour_g != null) View.cursor1_Colour_g[0] = cursor1_Colour_g[0];
        if (cursor1_Colour_b != null) View.cursor1_Colour_b[0] = cursor1_Colour_b[0];

        if (cursor2_Colour_r != null) View.cursor2_Colour_r[0] = cursor2_Colour_r[0];
        if (cursor2_Colour_g != null) View.cursor2_Colour_g[0] = cursor2_Colour_g[0];
        if (cursor2_Colour_b != null) View.cursor2_Colour_b[0] = cursor2_Colour_b[0];

        if (line_box_font_r != null) {
            Colour.line_box_font[0] = SWTResourceManager.getColor(line_box_font_r[0], line_box_font_g[0], line_box_font_b[0]);
        }

        if (line_colourAttr_font_r != null) {
            Colour.line_colourAttr_font[0] = SWTResourceManager.getColor(line_colourAttr_font_r[0], line_colourAttr_font_g[0], line_colourAttr_font_b[0]);
        }

        if (line_comment_font_r != null) {
            Colour.line_comment_font[0] = SWTResourceManager.getColor(line_comment_font_r[0], line_comment_font_g[0], line_comment_font_b[0]);
        }

        if (line_error_underline_r != null) {
            Colour.line_error_underline[0] = SWTResourceManager.getColor(line_error_underline_r[0], line_error_underline_g[0], line_error_underline_b[0]);
        }

        if (line_highlight_background_r != null) {
            Colour.line_highlight_background[0] = SWTResourceManager.getColor(line_highlight_background_r[0], line_highlight_background_g[0], line_highlight_background_b[0]);
        }

        if (line_highlight_selected_background_r != null) {
            Colour.line_highlight_selected_background[0] = SWTResourceManager.getColor(line_highlight_selected_background_r[0], line_highlight_selected_background_g[0], line_highlight_selected_background_b[0]);
        }

        if (line_hint_underline_r != null) {
            Colour.line_hint_underline[0] = SWTResourceManager.getColor(line_hint_underline_r[0], line_hint_underline_g[0], line_hint_underline_b[0]);
        }

        if (line_primary_font_r != null) {
            Colour.line_primary_font[0] = SWTResourceManager.getColor(line_primary_font_r[0], line_primary_font_g[0], line_primary_font_b[0]);
        }

        if (line_quad_font_r != null) {
            Colour.line_quad_font[0] = SWTResourceManager.getColor(line_quad_font_r[0], line_quad_font_g[0], line_quad_font_b[0]);
        }

        if (line_secondary_font_r != null) {
            Colour.line_secondary_font[0] = SWTResourceManager.getColor(line_secondary_font_r[0], line_secondary_font_g[0], line_secondary_font_b[0]);
        }

        if (line_warning_underline_r != null) {
            Colour.line_warning_underline[0] = SWTResourceManager.getColor(line_warning_underline_r[0], line_warning_underline_g[0], line_warning_underline_b[0]);
        }

        if (text_background_r != null) {
            Colour.text_background[0] = SWTResourceManager.getColor(text_background_r[0], text_background_g[0], text_background_b[0]);
        }

        if (text_foreground_r != null) {
            Colour.text_foreground[0] = SWTResourceManager.getColor(text_foreground_r[0], text_foreground_g[0], text_foreground_b[0]);
        }

        if (text_foreground_hidden_r != null) {
            Colour.text_foreground_hidden[0] = SWTResourceManager.getColor(text_foreground_hidden_r[0], text_foreground_hidden_g[0], text_foreground_hidden_b[0]);
        }
    }

    public boolean isAllowInvalidShapes() {
        return allowInvalidShapes;
    }

    public void setAllowInvalidShapes(boolean allowInvalidShapes) {
        this.allowInvalidShapes = allowInvalidShapes;
    }

    public boolean isSyncingTabs() {
        return syncingTabs;
    }

    public void setSyncingTabs(boolean syncingTabs) {
        this.syncingTabs = syncingTabs;
    }

    public int getTextWinArr() {
        return textWinArr;
    }

    public void setTextWinArr(int textWinArr) {
        this.textWinArr = textWinArr;
    }

    public boolean isDisableMAD3D() {
        return !disableMAD3D;
    }

    public void setDisableMAD3D(boolean disableMAD3D) {
        this.disableMAD3D = !disableMAD3D;
    }

    public boolean isDisableMADtext() {
        return !disableMADtext;
    }

    public void setDisableMADtext(boolean disableMADtext) {
        this.disableMADtext = !disableMADtext;
    }

    public boolean isRoundX() {
        return roundX;
    }

    public void setRoundX(boolean roundX) {
        this.roundX = roundX;
    }

    public boolean isRoundY() {
        return roundY;
    }

    public void setRoundY(boolean roundY) {
        this.roundY = roundY;
    }

    public boolean isRoundZ() {
        return roundZ;
    }

    public void setRoundZ(boolean roundZ) {
        this.roundZ = roundZ;
    }

    public int getOpenGLVersion() {
        return openGLVersion;
    }

    public String getOpenGLVersionString() {
        switch (openGLVersion) {
        case 20:
            return "OpenGL 2.0"; //$NON-NLS-1$
        case 33:
            return "OpenGL 3.3"; //$NON-NLS-1$
        case 100:
            return "Vulkan API 1.0"; //$NON-NLS-1$
        default:
            NLogger.error(getClass(), "getOpenGLVersionString(): No version string defined! OpenGL " + openGLVersion); //$NON-NLS-1$
            return openGLVersion + " [n.def.!]"; //$NON-NLS-1$
        }
    }

    public void setOpenGLVersion(int openGLVersion) {
        this.openGLVersion = openGLVersion;
    }

    public BigDecimal getFuzziness3D() {
        return fuzziness3D;
    }

    public void setFuzziness3D(BigDecimal fuzziness3d) {
        fuzziness3D = fuzziness3d;
    }

    public int getFuzziness2D() {
        return fuzziness2D;
    }

    public void setFuzziness2D(int fuzziness2d) {
        fuzziness2D = fuzziness2d;
    }

    public boolean isMovingAdjacentData() {
        return movingAdjacentData;
    }

    public void setMovingAdjacentData(boolean movingAdjacentData) {
        this.movingAdjacentData = movingAdjacentData;
    }
    
    public boolean isTranslatingViewByCursor() {
        return translateViewByCursor;
    }

    public void setTranslatingViewByCursor(boolean translateViewByCursor) {
        this.translateViewByCursor = translateViewByCursor;
    }

    public double getCoplanarity_angle_warning() {
        return coplanarity_angle_warning;
    }

    public void setCoplanarity_angle_warning(double coplanarity_angle_warning) {
        this.coplanarity_angle_warning = coplanarity_angle_warning;
    }

    public double getCoplanarity_angle_error() {
        return coplanarity_angle_error;
    }

    public void setCoplanarity_angle_error(double coplanarity_angle_error) {
        this.coplanarity_angle_error = coplanarity_angle_error;
    }
}
