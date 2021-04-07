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
    // Do not rename fields. It will break backwards compatibility! New values, which were not included in the state before, have to be initialized! (@ WorkbenchManager.loadWorkbench())

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
    private ArrayList<GColour> userPalette = new ArrayList<>();

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

    private ArrayList<String> recentItems = new ArrayList<>();

    private Locale locale = Locale.US;

    /** {@code true} if the user has got the information that BFC certification is mandatory for the LDraw Standard Preview Mode  */
    private boolean bfcCertificationRequiredForLDrawMode = false;

    private String[] key3DStrings = null;
    private String[] key3DKeys = null;
    private Task[] key3DTasks = null;

    private String[] keyTextStrings = null;
    private String[] keyTextKeys = null;
    private TextTask[] keyTextTasks = null;

    private ArrayList<ToolItemState> toolItemConfig3D = new ArrayList<>();

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
    private double viewportScaleFactor = 1d;

    private int mouseButtonLayout = 0;

    private boolean invertingWheelZoomDirection = false;

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

    public void setCoarse_move_snap(BigDecimal coarseMoveSnap) {
        this.coarse_move_snap = coarseMoveSnap;
    }

    public BigDecimal getCoarse_rotate_snap() {
        return coarse_rotate_snap;
    }

    public void setCoarse_rotate_snap(BigDecimal coarseRotateSnap) {
        this.coarse_rotate_snap = coarseRotateSnap;
    }

    public BigDecimal getCoarse_scale_snap() {
        return coarse_scale_snap;
    }

    public void setCoarse_scale_snap(BigDecimal coarseScaleSnap) {
        this.coarse_scale_snap = coarseScaleSnap;
    }

    public BigDecimal getMedium_move_snap() {
        return medium_move_snap;
    }

    public void setMedium_move_snap(BigDecimal mediumMoveSnap) {
        this.medium_move_snap = mediumMoveSnap;
    }

    public BigDecimal getMedium_rotate_snap() {
        return medium_rotate_snap;
    }

    public void setMedium_rotate_snap(BigDecimal mediumRotateSnap) {
        this.medium_rotate_snap = mediumRotateSnap;
    }

    public BigDecimal getMedium_scale_snap() {
        return medium_scale_snap;
    }

    public void setMedium_scale_snap(BigDecimal mediumScaleSnap) {
        this.medium_scale_snap = mediumScaleSnap;
    }

    public BigDecimal getFine_move_snap() {
        return fine_move_snap;
    }

    public void setFine_move_snap(BigDecimal fineMoveSnap) {
        this.fine_move_snap = fineMoveSnap;
    }

    public BigDecimal getFine_rotate_snap() {
        return fine_rotate_snap;
    }

    public void setFine_rotate_snap(BigDecimal fineRotateSnap) {
        this.fine_rotate_snap = fineRotateSnap;
    }

    public BigDecimal getFine_scale_snap() {
        return fine_scale_snap;
    }

    public void setFine_scale_snap(BigDecimal fineScaleSnap) {
        this.fine_scale_snap = fineScaleSnap;
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

    void loadShortkeys() {
        if (key3DStrings != null && key3DKeys != null && key3DTasks != null) {
            final int size = key3DStrings.length;
            for (int i = 0; i < size; i++) {
                KeyStateManager.changeKey(key3DKeys[i], key3DStrings[i], key3DTasks[i]);
            }
        }

        if (keyTextStrings != null && keyTextKeys != null && keyTextTasks != null) {
            final int size = keyTextStrings.length;
            for (int i = 0; i < size; i++) {
                KeyStateManager.changeKey(keyTextKeys[i], keyTextStrings[i], keyTextTasks[i]);
            }
        }
    }

    void saveShortkeys() {
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

    void saveColours() {

        Color16_override_r = View.COLOUR16_OVERRIDE_R;
        Color16_override_g = View.COLOUR16_OVERRIDE_G;
        Color16_override_b = View.COLOUR16_OVERRIDE_B;

        BFC_front_Colour_r = View.BFC_FRONT_COLOUR_R;
        BFC_front_Colour_g = View.BFC_FRONT_COLOUR_G;
        BFC_front_Colour_b = View.BFC_FRONT_COLOUR_B;

        BFC_back__Colour_r = View.BFC_BACK__COLOUR_R;
        BFC_back__Colour_g = View.BFC_BACK__COLOUR_G;
        BFC_back__Colour_b = View.BFC_BACK__COLOUR_B;

        BFC_uncertified_Colour_r = View.BFC_UNCERTIFIED_COLOUR_R;
        BFC_uncertified_Colour_g = View.BFC_UNCERTIFIED_COLOUR_G;
        BFC_uncertified_Colour_b = View.BFC_UNCERTIFIED_COLOUR_B;

        vertex_Colour_r = View.VERTEX_COLOUR_R;
        vertex_Colour_g = View.VERTEX_COLOUR_G;
        vertex_Colour_b = View.VERTEX_COLOUR_B;

        vertex_selected_Colour_r = View.VERTEX_SELECTED_COLOUR_R;
        vertex_selected_Colour_g = View.VERTEX_SELECTED_COLOUR_G;
        vertex_selected_Colour_b = View.VERTEX_SELECTED_COLOUR_B;

        condline_selected_Colour_r = View.CONDLINE_SELECTED_COLOUR_R;
        condline_selected_Colour_g = View.CONDLINE_SELECTED_COLOUR_G;
        condline_selected_Colour_b = View.CONDLINE_SELECTED_COLOUR_B;

        line_Colour_r = View.LINE_COLOUR_R;
        line_Colour_g = View.LINE_COLOUR_G;
        line_Colour_b = View.LINE_COLOUR_B;

        meshline_Colour_r = View.MESHLINE_COLOUR_R;
        meshline_Colour_g = View.MESHLINE_COLOUR_G;
        meshline_Colour_b = View.MESHLINE_COLOUR_B;

        condline_Colour_r = View.CONDLINE_COLOUR_R;
        condline_Colour_g = View.CONDLINE_COLOUR_G;
        condline_Colour_b = View.CONDLINE_COLOUR_B;

        condline_hidden_Colour_r = View.CONDLINE_HIDDEN_COLOUR_R;
        condline_hidden_Colour_g = View.CONDLINE_HIDDEN_COLOUR_G;
        condline_hidden_Colour_b = View.CONDLINE_HIDDEN_COLOUR_B;

        condline_shown_Colour_r = View.CONDLINE_SHOWN_COLOUR_R;
        condline_shown_Colour_g = View.CONDLINE_SHOWN_COLOUR_G;
        condline_shown_Colour_b = View.CONDLINE_SHOWN_COLOUR_B;

        background_Colour_r = View.BACKGROUND_COLOUR_R;
        background_Colour_g = View.BACKGROUND_COLOUR_G;
        background_Colour_b = View.BACKGROUND_COLOUR_B;

        light1_Colour_r = View.LIGHT1_COLOUR_R;
        light1_Colour_g = View.LIGHT1_COLOUR_G;
        light1_Colour_b = View.LIGHT1_COLOUR_B;

        light1_specular_Colour_r = View.LIGHT1_SPECULAR_COLOUR_R;
        light1_specular_Colour_g = View.LIGHT1_SPECULAR_COLOUR_G;
        light1_specular_Colour_b = View.LIGHT1_SPECULAR_COLOUR_B;

        light2_Colour_r = View.LIGHT2_COLOUR_R;
        light2_Colour_g = View.LIGHT2_COLOUR_G;
        light2_Colour_b = View.LIGHT2_COLOUR_B;

        light2_specular_Colour_r = View.LIGHT2_SPECULAR_COLOUR_R;
        light2_specular_Colour_g = View.LIGHT2_SPECULAR_COLOUR_G;
        light2_specular_Colour_b = View.LIGHT2_SPECULAR_COLOUR_B;

        light3_Colour_r = View.LIGHT3_COLOUR_R;
        light3_Colour_g = View.LIGHT3_COLOUR_G;
        light3_Colour_b = View.LIGHT3_COLOUR_B;

        light3_specular_Colour_r = View.LIGHT3_SPECULAR_COLOUR_R;
        light3_specular_Colour_g = View.LIGHT3_SPECULAR_COLOUR_G;
        light3_specular_Colour_b = View.LIGHT3_SPECULAR_COLOUR_B;

        light4_Colour_r = View.LIGHT4_COLOUR_R;
        light4_Colour_g = View.LIGHT4_COLOUR_G;
        light4_Colour_b = View.LIGHT4_COLOUR_B;

        light4_specular_Colour_r = View.LIGHT4_SPECULAR_COLOUR_R;
        light4_specular_Colour_g = View.LIGHT4_SPECULAR_COLOUR_G;
        light4_specular_Colour_b = View.LIGHT4_SPECULAR_COLOUR_B;

        manipulator_selected_Colour_r = View.MANIPULATOR_SELECTED_COLOUR_R;
        manipulator_selected_Colour_g = View.MANIPULATOR_SELECTED_COLOUR_G;
        manipulator_selected_Colour_b = View.MANIPULATOR_SELECTED_COLOUR_B;

        manipulator_innerCircle_Colour_r = View.MANIPULATOR_INNERCIRCLE_COLOUR_R;
        manipulator_innerCircle_Colour_g = View.MANIPULATOR_INNERCIRCLE_COLOUR_G;
        manipulator_innerCircle_Colour_b = View.MANIPULATOR_INNERCIRCLE_COLOUR_B;

        manipulator_outerCircle_Colour_r = View.MANIPULATOR_OUTERCIRCLE_COLOUR_R;
        manipulator_outerCircle_Colour_g = View.MANIPULATOR_OUTERCIRCLE_COLOUR_G;
        manipulator_outerCircle_Colour_b = View.MANIPULATOR_OUTERCIRCLE_COLOUR_B;

        manipulator_x_axis_Colour_r = View.MANIPULATOR_X_AXIS_COLOUR_R;
        manipulator_x_axis_Colour_g = View.MANIPULATOR_X_AXIS_COLOUR_G;
        manipulator_x_axis_Colour_b = View.MANIPULATOR_X_AXIS_COLOUR_B;

        manipulator_y_axis_Colour_r = View.MANIPULATOR_Y_AXIS_COLOUR_R;
        manipulator_y_axis_Colour_g = View.MANIPULATOR_Y_AXIS_COLOUR_G;
        manipulator_y_axis_Colour_b = View.MANIPULATOR_Y_AXIS_COLOUR_B;

        manipulator_z_axis_Colour_r = View.MANIPULATOR_Z_AXIS_COLOUR_R;
        manipulator_z_axis_Colour_g = View.MANIPULATOR_Z_AXIS_COLOUR_G;
        manipulator_z_axis_Colour_b = View.MANIPULATOR_Z_AXIS_COLOUR_B;

        add_Object_Colour_r = View.ADD_OBJECT_COLOUR_R;
        add_Object_Colour_g = View.ADD_OBJECT_COLOUR_G;
        add_Object_Colour_b = View.ADD_OBJECT_COLOUR_B;

        origin_Colour_r = View.ORIGIN_COLOUR_R;
        origin_Colour_g = View.ORIGIN_COLOUR_G;
        origin_Colour_b = View.ORIGIN_COLOUR_B;

        grid10_Colour_r = View.GRID10_COLOUR_R;
        grid10_Colour_g = View.GRID10_COLOUR_G;
        grid10_Colour_b = View.GRID10_COLOUR_B;

        grid_Colour_r = View.GRID_COLOUR_R;
        grid_Colour_g = View.GRID_COLOUR_B;
        grid_Colour_b = View.GRID_COLOUR_B;

        rubberBand_Colour_r = View.RUBBER_BAND_COLOUR_R;
        rubberBand_Colour_g = View.RUBBER_BAND_COLOUR_G;
        rubberBand_Colour_b = View.RUBBER_BAND_COLOUR_B;

        text_Colour_r = View.TEXT_COLOUR_R;
        text_Colour_g = View.TEXT_COLOUR_G;
        text_Colour_b = View.TEXT_COLOUR_B;

        x_axis_Colour_r = View.X_AXIS_COLOUR_R;
        x_axis_Colour_g = View.X_AXIS_COLOUR_G;
        x_axis_Colour_b = View.X_AXIS_COLOUR_B;

        y_axis_Colour_r = View.Y_AXIS_COLOUR_R;
        y_axis_Colour_g = View.Y_AXIS_COLOUR_G;
        y_axis_Colour_b = View.Y_AXIS_COLOUR_B;

        z_axis_Colour_r = View.Z_AXIS_COLOUR_R;
        z_axis_Colour_g = View.Z_AXIS_COLOUR_G;
        z_axis_Colour_b = View.Z_AXIS_COLOUR_B;

        primitive_background_Colour_r = View.PRIMITIVE_BACKGROUND_COLOUR_R;
        primitive_background_Colour_g = View.PRIMITIVE_BACKGROUND_COLOUR_G;
        primitive_background_Colour_b = View.PRIMITIVE_BACKGROUND_COLOUR_B;

        primitive_signFG_Colour_r = View.PRIMITIVE_SIGN_FG_COLOUR_R;
        primitive_signFG_Colour_g = View.PRIMITIVE_SIGN_FG_COLOUR_G;
        primitive_signFG_Colour_b = View.PRIMITIVE_SIGN_FG_COLOUR_B;

        primitive_signBG_Colour_r = View.PRIMITIVE_SIGN_BG_COLOUR_R;
        primitive_signBG_Colour_g = View.PRIMITIVE_SIGN_BG_COLOUR_G;
        primitive_signBG_Colour_b = View.PRIMITIVE_SIGN_BG_COLOUR_B;

        primitive_plusNminus_Colour_r = View.PRIMITIVE_PLUS_N_MINUS_COLOUR_R;
        primitive_plusNminus_Colour_g = View.PRIMITIVE_PLUS_N_MINUS_COLOUR_G;
        primitive_plusNminus_Colour_b = View.PRIMITIVE_PLUS_N_MINUS_COLOUR_B;

        primitive_selectedCell_Colour_r = View.PRIMITIVE_SELECTED_CELL_COLOUR_R;
        primitive_selectedCell_Colour_g = View.PRIMITIVE_SELECTED_CELL_COLOUR_G;
        primitive_selectedCell_Colour_b = View.PRIMITIVE_SELECTED_CELL_COLOUR_B;

        primitive_focusedCell_Colour_r = View.PRIMITIVE_FOCUSED_CELL_COLOUR_R;
        primitive_focusedCell_Colour_g = View.PRIMITIVE_FOCUSED_CELL_COLOUR_G;
        primitive_focusedCell_Colour_b = View.PRIMITIVE_FOCUSED_CELL_COLOUR_B;

        primitive_normalCell_Colour_r = View.PRIMITIVE_NORMAL_CELL_COLOUR_R;
        primitive_normalCell_Colour_g = View.PRIMITIVE_NORMAL_CELL_COLOUR_G;
        primitive_normalCell_Colour_b = View.PRIMITIVE_NORMAL_CELL_COLOUR_B;

        primitive_cell_1_Colour_r = View.PRIMITIVE_CELL_1_COLOUR_R;
        primitive_cell_1_Colour_g = View.PRIMITIVE_CELL_1_COLOUR_G;
        primitive_cell_1_Colour_b = View.PRIMITIVE_CELL_1_COLOUR_B;

        primitive_cell_2_Colour_r = View.PRIMITIVE_CELL_2_COLOUR_R;
        primitive_cell_2_Colour_g = View.PRIMITIVE_CELL_2_COLOUR_G;
        primitive_cell_2_Colour_b = View.PRIMITIVE_CELL_2_COLOUR_B;

        primitive_categoryCell_1_Colour_r = View.PRIMITIVE_CATEGORYCELL_1_COLOUR_R;
        primitive_categoryCell_1_Colour_g = View.PRIMITIVE_CATEGORYCELL_1_COLOUR_G;
        primitive_categoryCell_1_Colour_b = View.PRIMITIVE_CATEGORYCELL_1_COLOUR_B;

        primitive_categoryCell_2_Colour_r = View.PRIMITIVE_CATEGORYCELL_2_COLOUR_R;
        primitive_categoryCell_2_Colour_g = View.PRIMITIVE_CATEGORYCELL_2_COLOUR_G;
        primitive_categoryCell_2_Colour_b = View.PRIMITIVE_CATEGORYCELL_2_COLOUR_B;

        primitive_edge_Colour_r = View.PRIMITIVE_EDGE_COLOUR_R;
        primitive_edge_Colour_g = View.PRIMITIVE_EDGE_COLOUR_G;
        primitive_edge_Colour_b = View.PRIMITIVE_EDGE_COLOUR_B;

        primitive_condline_Colour_r = View.PRIMITIVE_CONDLINE_COLOUR_R;
        primitive_condline_Colour_g = View.PRIMITIVE_CONDLINE_COLOUR_G;
        primitive_condline_Colour_b = View.PRIMITIVE_CONDLINE_COLOUR_B;

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

        cursor1_Colour_r = View.CURSOR1_COLOUR_R;
        cursor1_Colour_g = View.CURSOR1_COLOUR_G;
        cursor1_Colour_b = View.CURSOR1_COLOUR_B;

        cursor2_Colour_r = View.CURSOR2_COLOUR_R;
        cursor2_Colour_g = View.CURSOR2_COLOUR_G;
        cursor2_Colour_b = View.CURSOR2_COLOUR_B;
    }

    public void loadColours() {

        if (Color16_override_r != null) View.COLOUR16_OVERRIDE_R[0] = Color16_override_r[0];
        if (Color16_override_g != null) View.COLOUR16_OVERRIDE_G[0] = Color16_override_g[0];
        if (Color16_override_b != null) View.COLOUR16_OVERRIDE_B[0] = Color16_override_b[0];

        if (BFC_front_Colour_r != null) View.BFC_FRONT_COLOUR_R[0] = BFC_front_Colour_r[0];
        if (BFC_front_Colour_g != null) View.BFC_FRONT_COLOUR_G[0] = BFC_front_Colour_g[0];
        if (BFC_front_Colour_b != null) View.BFC_FRONT_COLOUR_B[0] = BFC_front_Colour_b[0];

        if (BFC_back__Colour_r != null) View.BFC_BACK__COLOUR_R[0] = BFC_back__Colour_r[0];
        if (BFC_back__Colour_g != null) View.BFC_BACK__COLOUR_G[0] = BFC_back__Colour_g[0];
        if (BFC_back__Colour_b != null) View.BFC_BACK__COLOUR_B[0] = BFC_back__Colour_b[0];

        if (BFC_uncertified_Colour_r != null) View.BFC_UNCERTIFIED_COLOUR_R[0] = BFC_uncertified_Colour_r[0];
        if (BFC_uncertified_Colour_g != null) View.BFC_UNCERTIFIED_COLOUR_G[0] = BFC_uncertified_Colour_g[0];
        if (BFC_uncertified_Colour_b != null) View.BFC_UNCERTIFIED_COLOUR_B[0] = BFC_uncertified_Colour_b[0];

        if (vertex_Colour_r != null) View.VERTEX_COLOUR_R[0] = vertex_Colour_r[0];
        if (vertex_Colour_g != null) View.VERTEX_COLOUR_G[0] = vertex_Colour_g[0];
        if (vertex_Colour_b != null) View.VERTEX_COLOUR_B[0] = vertex_Colour_b[0];

        if (vertex_selected_Colour_r != null) View.VERTEX_SELECTED_COLOUR_R[0] = vertex_selected_Colour_r[0];
        if (vertex_selected_Colour_g != null) View.VERTEX_SELECTED_COLOUR_G[0] = vertex_selected_Colour_g[0];
        if (vertex_selected_Colour_b != null) View.VERTEX_SELECTED_COLOUR_B[0] = vertex_selected_Colour_b[0];

        if (condline_selected_Colour_r != null) View.CONDLINE_SELECTED_COLOUR_R[0] = condline_selected_Colour_r[0];
        if (condline_selected_Colour_g != null) View.CONDLINE_SELECTED_COLOUR_G[0] = condline_selected_Colour_g[0];
        if (condline_selected_Colour_b != null) View.CONDLINE_SELECTED_COLOUR_B[0] = condline_selected_Colour_b[0];

        if (line_Colour_r != null) View.LINE_COLOUR_R[0] = line_Colour_r[0];
        if (line_Colour_g != null) View.LINE_COLOUR_G[0] = line_Colour_g[0];
        if (line_Colour_b != null) View.LINE_COLOUR_B[0] = line_Colour_b[0];

        if (meshline_Colour_r != null) View.MESHLINE_COLOUR_R[0] = meshline_Colour_r[0];
        if (meshline_Colour_g != null) View.MESHLINE_COLOUR_G[0] = meshline_Colour_g[0];
        if (meshline_Colour_b != null) View.MESHLINE_COLOUR_B[0] = meshline_Colour_b[0];

        if (condline_Colour_r != null) View.CONDLINE_COLOUR_R[0] = condline_Colour_r[0];
        if (condline_Colour_g != null) View.CONDLINE_COLOUR_G[0] = condline_Colour_g[0];
        if (condline_Colour_b != null) View.CONDLINE_COLOUR_B[0] = condline_Colour_b[0];

        if (condline_hidden_Colour_r != null) View.CONDLINE_HIDDEN_COLOUR_R[0] = condline_hidden_Colour_r[0];
        if (condline_hidden_Colour_g != null) View.CONDLINE_HIDDEN_COLOUR_G[0] = condline_hidden_Colour_g[0];
        if (condline_hidden_Colour_b != null) View.CONDLINE_HIDDEN_COLOUR_B[0] = condline_hidden_Colour_b[0];

        if (condline_shown_Colour_r != null) View.CONDLINE_SHOWN_COLOUR_R[0] = condline_shown_Colour_r[0];
        if (condline_shown_Colour_g != null) View.CONDLINE_SHOWN_COLOUR_G[0] = condline_shown_Colour_g[0];
        if (condline_shown_Colour_b != null) View.CONDLINE_SHOWN_COLOUR_B[0] = condline_shown_Colour_b[0];

        if (background_Colour_r != null) View.BACKGROUND_COLOUR_R[0] = background_Colour_r[0];
        if (background_Colour_g != null) View.BACKGROUND_COLOUR_G[0] = background_Colour_g[0];
        if (background_Colour_b != null) View.BACKGROUND_COLOUR_B[0] = background_Colour_b[0];

        if (light1_Colour_r != null) View.LIGHT1_COLOUR_R[0] = light1_Colour_r[0];
        if (light1_Colour_g != null) View.LIGHT1_COLOUR_G[0] = light1_Colour_g[0];
        if (light1_Colour_b != null) View.LIGHT1_COLOUR_B[0] = light1_Colour_b[0];

        if (light1_specular_Colour_r != null) View.LIGHT1_SPECULAR_COLOUR_R[0] = light1_specular_Colour_r[0];
        if (light1_specular_Colour_g != null) View.LIGHT1_SPECULAR_COLOUR_G[0] = light1_specular_Colour_g[0];
        if (light1_specular_Colour_b != null) View.LIGHT1_SPECULAR_COLOUR_B[0] = light1_specular_Colour_b[0];

        if (light2_Colour_r != null) View.LIGHT2_COLOUR_R[0] = light2_Colour_r[0];
        if (light2_Colour_g != null) View.LIGHT2_COLOUR_G[0] = light2_Colour_g[0];
        if (light2_Colour_b != null) View.LIGHT2_COLOUR_B[0] = light2_Colour_b[0];

        if (light2_specular_Colour_r != null) View.LIGHT2_SPECULAR_COLOUR_R[0] = light2_specular_Colour_r[0];
        if (light2_specular_Colour_g != null) View.LIGHT2_SPECULAR_COLOUR_G[0] = light2_specular_Colour_g[0];
        if (light2_specular_Colour_b != null) View.LIGHT2_SPECULAR_COLOUR_B[0] = light2_specular_Colour_b[0];

        if (light3_Colour_r != null) View.LIGHT3_COLOUR_R[0] = light3_Colour_r[0];
        if (light3_Colour_g != null) View.LIGHT3_COLOUR_G[0] = light3_Colour_g[0];
        if (light3_Colour_b != null) View.LIGHT3_COLOUR_B[0] = light3_Colour_b[0];

        if (light3_specular_Colour_r != null) View.LIGHT3_SPECULAR_COLOUR_R[0] = light3_specular_Colour_r[0];
        if (light3_specular_Colour_g != null) View.LIGHT3_SPECULAR_COLOUR_G[0] = light3_specular_Colour_g[0];
        if (light3_specular_Colour_b != null) View.LIGHT3_SPECULAR_COLOUR_B[0] = light3_specular_Colour_b[0];

        if (light4_Colour_r != null) View.LIGHT4_COLOUR_R[0] = light4_Colour_r[0];
        if (light4_Colour_g != null) View.LIGHT4_COLOUR_G[0] = light4_Colour_g[0];
        if (light4_Colour_b != null) View.LIGHT4_COLOUR_B[0] = light4_Colour_b[0];

        if (light4_specular_Colour_r != null) View.LIGHT4_SPECULAR_COLOUR_R[0] = light4_specular_Colour_r[0];
        if (light4_specular_Colour_g != null) View.LIGHT4_SPECULAR_COLOUR_G[0] = light4_specular_Colour_g[0];
        if (light4_specular_Colour_b != null) View.LIGHT4_SPECULAR_COLOUR_B[0] = light4_specular_Colour_b[0];

        if (manipulator_selected_Colour_r != null) View.MANIPULATOR_SELECTED_COLOUR_R[0] = manipulator_selected_Colour_r[0];
        if (manipulator_selected_Colour_g != null) View.MANIPULATOR_SELECTED_COLOUR_G[0] = manipulator_selected_Colour_g[0];
        if (manipulator_selected_Colour_b != null) View.MANIPULATOR_SELECTED_COLOUR_B[0] = manipulator_selected_Colour_b[0];

        if (manipulator_innerCircle_Colour_r != null) View.MANIPULATOR_INNERCIRCLE_COLOUR_R[0] = manipulator_innerCircle_Colour_r[0];
        if (manipulator_innerCircle_Colour_g != null) View.MANIPULATOR_INNERCIRCLE_COLOUR_G[0] = manipulator_innerCircle_Colour_g[0];
        if (manipulator_innerCircle_Colour_b != null) View.MANIPULATOR_INNERCIRCLE_COLOUR_B[0] = manipulator_innerCircle_Colour_b[0];

        if (manipulator_outerCircle_Colour_r != null) View.MANIPULATOR_OUTERCIRCLE_COLOUR_R[0] = manipulator_outerCircle_Colour_r[0];
        if (manipulator_outerCircle_Colour_g != null) View.MANIPULATOR_OUTERCIRCLE_COLOUR_G[0] = manipulator_outerCircle_Colour_g[0];
        if (manipulator_outerCircle_Colour_b != null) View.MANIPULATOR_OUTERCIRCLE_COLOUR_B[0] = manipulator_outerCircle_Colour_b[0];

        if (manipulator_x_axis_Colour_r != null) View.MANIPULATOR_X_AXIS_COLOUR_R[0] = manipulator_x_axis_Colour_r[0];
        if (manipulator_x_axis_Colour_g != null) View.MANIPULATOR_X_AXIS_COLOUR_G[0] = manipulator_x_axis_Colour_g[0];
        if (manipulator_x_axis_Colour_b != null) View.MANIPULATOR_X_AXIS_COLOUR_B[0] = manipulator_x_axis_Colour_b[0];

        if (manipulator_y_axis_Colour_r != null) View.MANIPULATOR_Y_AXIS_COLOUR_R[0] = manipulator_y_axis_Colour_r[0];
        if (manipulator_y_axis_Colour_g != null) View.MANIPULATOR_Y_AXIS_COLOUR_G[0] = manipulator_y_axis_Colour_g[0];
        if (manipulator_y_axis_Colour_b != null) View.MANIPULATOR_Y_AXIS_COLOUR_B[0] = manipulator_y_axis_Colour_b[0];

        if (manipulator_z_axis_Colour_r != null) View.MANIPULATOR_Z_AXIS_COLOUR_R[0] = manipulator_z_axis_Colour_r[0];
        if (manipulator_z_axis_Colour_g != null) View.MANIPULATOR_Z_AXIS_COLOUR_G[0] = manipulator_z_axis_Colour_g[0];
        if (manipulator_z_axis_Colour_b != null) View.MANIPULATOR_Z_AXIS_COLOUR_B[0] = manipulator_z_axis_Colour_b[0];

        if (add_Object_Colour_r != null) View.ADD_OBJECT_COLOUR_R[0] = add_Object_Colour_r[0];
        if (add_Object_Colour_g != null) View.ADD_OBJECT_COLOUR_G[0] = add_Object_Colour_g[0];
        if (add_Object_Colour_b != null) View.ADD_OBJECT_COLOUR_B[0] = add_Object_Colour_b[0];

        if (origin_Colour_r != null) View.ORIGIN_COLOUR_R[0] = origin_Colour_r[0];
        if (origin_Colour_g != null) View.ORIGIN_COLOUR_G[0] = origin_Colour_g[0];
        if (origin_Colour_b != null) View.ORIGIN_COLOUR_B[0] = origin_Colour_b[0];

        if (grid10_Colour_r != null) View.GRID10_COLOUR_R[0] = grid10_Colour_r[0];
        if (grid10_Colour_g != null) View.GRID10_COLOUR_G[0] = grid10_Colour_g[0];
        if (grid10_Colour_b != null) View.GRID10_COLOUR_B[0] = grid10_Colour_b[0];

        if (grid_Colour_r != null) View.GRID_COLOUR_R[0] = grid_Colour_r[0];
        if (grid_Colour_g != null) View.GRID_COLOUR_G[0] = grid_Colour_g[0];
        if (grid_Colour_b != null) View.GRID_COLOUR_B[0] = grid_Colour_b[0];

        if (rubberBand_Colour_r != null) View.RUBBER_BAND_COLOUR_R[0] = rubberBand_Colour_r[0];
        if (rubberBand_Colour_g != null) View.RUBBER_BAND_COLOUR_G[0] = rubberBand_Colour_g[0];
        if (rubberBand_Colour_b != null) View.RUBBER_BAND_COLOUR_B[0] = rubberBand_Colour_b[0];

        if (text_Colour_r != null) View.TEXT_COLOUR_R[0] = text_Colour_r[0];
        if (text_Colour_g != null) View.TEXT_COLOUR_G[0] = text_Colour_g[0];
        if (text_Colour_b != null) View.TEXT_COLOUR_B[0] = text_Colour_b[0];

        if (x_axis_Colour_r != null) View.X_AXIS_COLOUR_R[0] = x_axis_Colour_r[0];
        if (x_axis_Colour_g != null) View.X_AXIS_COLOUR_G[0] = x_axis_Colour_g[0];
        if (x_axis_Colour_b != null) View.X_AXIS_COLOUR_B[0] = x_axis_Colour_b[0];

        if (y_axis_Colour_r != null) View.Y_AXIS_COLOUR_R[0] = y_axis_Colour_r[0];
        if (y_axis_Colour_g != null) View.Y_AXIS_COLOUR_G[0] = y_axis_Colour_g[0];
        if (y_axis_Colour_b != null) View.Y_AXIS_COLOUR_B[0] = y_axis_Colour_b[0];

        if (z_axis_Colour_r != null) View.Z_AXIS_COLOUR_R[0] = z_axis_Colour_r[0];
        if (z_axis_Colour_g != null) View.Z_AXIS_COLOUR_G[0] = z_axis_Colour_g[0];
        if (z_axis_Colour_b != null) View.Z_AXIS_COLOUR_B[0] = z_axis_Colour_b[0];

        if (primitive_background_Colour_r != null) View.PRIMITIVE_BACKGROUND_COLOUR_R[0] = primitive_background_Colour_r[0];
        if (primitive_background_Colour_g != null) View.PRIMITIVE_BACKGROUND_COLOUR_G[0] = primitive_background_Colour_g[0];
        if (primitive_background_Colour_b != null) View.PRIMITIVE_BACKGROUND_COLOUR_B[0] = primitive_background_Colour_b[0];

        if (primitive_signFG_Colour_r != null) View.PRIMITIVE_SIGN_FG_COLOUR_R[0] = primitive_signFG_Colour_r[0];
        if (primitive_signFG_Colour_g != null) View.PRIMITIVE_SIGN_FG_COLOUR_G[0] = primitive_signFG_Colour_g[0];
        if (primitive_signFG_Colour_b != null) View.PRIMITIVE_SIGN_FG_COLOUR_B[0] = primitive_signFG_Colour_b[0];

        if (primitive_signBG_Colour_r != null) View.PRIMITIVE_SIGN_BG_COLOUR_R[0] = primitive_signBG_Colour_r[0];
        if (primitive_signBG_Colour_g != null) View.PRIMITIVE_SIGN_BG_COLOUR_G[0] = primitive_signBG_Colour_g[0];
        if (primitive_signBG_Colour_b != null) View.PRIMITIVE_SIGN_BG_COLOUR_B[0] = primitive_signBG_Colour_b[0];

        if (primitive_plusNminus_Colour_r != null) View.PRIMITIVE_PLUS_N_MINUS_COLOUR_R[0] = primitive_plusNminus_Colour_r[0];
        if (primitive_plusNminus_Colour_g != null) View.PRIMITIVE_PLUS_N_MINUS_COLOUR_G[0] = primitive_plusNminus_Colour_g[0];
        if (primitive_plusNminus_Colour_b != null) View.PRIMITIVE_PLUS_N_MINUS_COLOUR_B[0] = primitive_plusNminus_Colour_b[0];

        if (primitive_selectedCell_Colour_r != null) View.PRIMITIVE_SELECTED_CELL_COLOUR_R[0] = primitive_selectedCell_Colour_r[0];
        if (primitive_selectedCell_Colour_g != null) View.PRIMITIVE_SELECTED_CELL_COLOUR_G[0] = primitive_selectedCell_Colour_g[0];
        if (primitive_selectedCell_Colour_b != null) View.PRIMITIVE_SELECTED_CELL_COLOUR_B[0] = primitive_selectedCell_Colour_b[0];

        if (primitive_focusedCell_Colour_r != null) View.PRIMITIVE_FOCUSED_CELL_COLOUR_R[0] = primitive_focusedCell_Colour_r[0];
        if (primitive_focusedCell_Colour_g != null) View.PRIMITIVE_FOCUSED_CELL_COLOUR_G[0] = primitive_focusedCell_Colour_g[0];
        if (primitive_focusedCell_Colour_b != null) View.PRIMITIVE_FOCUSED_CELL_COLOUR_B[0] = primitive_focusedCell_Colour_b[0];

        if (primitive_normalCell_Colour_r != null) View.PRIMITIVE_NORMAL_CELL_COLOUR_R[0] = primitive_normalCell_Colour_r[0];
        if (primitive_normalCell_Colour_g != null) View.PRIMITIVE_NORMAL_CELL_COLOUR_G[0] = primitive_normalCell_Colour_g[0];
        if (primitive_normalCell_Colour_b != null) View.PRIMITIVE_NORMAL_CELL_COLOUR_B[0] = primitive_normalCell_Colour_b[0];

        if (primitive_cell_1_Colour_r != null) View.PRIMITIVE_CELL_1_COLOUR_R[0] = primitive_cell_1_Colour_r[0];
        if (primitive_cell_1_Colour_g != null) View.PRIMITIVE_CELL_1_COLOUR_G[0] = primitive_cell_1_Colour_g[0];
        if (primitive_cell_1_Colour_b != null) View.PRIMITIVE_CELL_1_COLOUR_B[0] = primitive_cell_1_Colour_b[0];

        if (primitive_cell_2_Colour_r != null) View.PRIMITIVE_CELL_2_COLOUR_R[0] = primitive_cell_2_Colour_r[0];
        if (primitive_cell_2_Colour_g != null) View.PRIMITIVE_CELL_2_COLOUR_G[0] = primitive_cell_2_Colour_g[0];
        if (primitive_cell_2_Colour_b != null) View.PRIMITIVE_CELL_2_COLOUR_B[0] = primitive_cell_2_Colour_b[0];

        if (primitive_categoryCell_1_Colour_r != null) View.PRIMITIVE_CATEGORYCELL_1_COLOUR_R[0] = primitive_categoryCell_1_Colour_r[0];
        if (primitive_categoryCell_1_Colour_g != null) View.PRIMITIVE_CATEGORYCELL_1_COLOUR_G[0] = primitive_categoryCell_1_Colour_g[0];
        if (primitive_categoryCell_1_Colour_b != null) View.PRIMITIVE_CATEGORYCELL_1_COLOUR_B[0] = primitive_categoryCell_1_Colour_b[0];

        if (primitive_categoryCell_2_Colour_r != null) View.PRIMITIVE_CATEGORYCELL_2_COLOUR_R[0] = primitive_categoryCell_2_Colour_r[0];
        if (primitive_categoryCell_2_Colour_g != null) View.PRIMITIVE_CATEGORYCELL_2_COLOUR_G[0] = primitive_categoryCell_2_Colour_g[0];
        if (primitive_categoryCell_2_Colour_b != null) View.PRIMITIVE_CATEGORYCELL_2_COLOUR_B[0] = primitive_categoryCell_2_Colour_b[0];

        if (primitive_edge_Colour_r != null) View.PRIMITIVE_EDGE_COLOUR_R[0] = primitive_edge_Colour_r[0];
        if (primitive_edge_Colour_g != null) View.PRIMITIVE_EDGE_COLOUR_G[0] = primitive_edge_Colour_g[0];
        if (primitive_edge_Colour_b != null) View.PRIMITIVE_EDGE_COLOUR_B[0] = primitive_edge_Colour_b[0];

        if (primitive_condline_Colour_r != null) View.PRIMITIVE_CONDLINE_COLOUR_R[0] = primitive_condline_Colour_r[0];
        if (primitive_condline_Colour_g != null) View.PRIMITIVE_CONDLINE_COLOUR_G[0] = primitive_condline_Colour_g[0];
        if (primitive_condline_Colour_b != null) View.PRIMITIVE_CONDLINE_COLOUR_B[0] = primitive_condline_Colour_b[0];

        if (cursor1_Colour_r != null) View.CURSOR1_COLOUR_R[0] = cursor1_Colour_r[0];
        if (cursor1_Colour_g != null) View.CURSOR1_COLOUR_G[0] = cursor1_Colour_g[0];
        if (cursor1_Colour_b != null) View.CURSOR1_COLOUR_B[0] = cursor1_Colour_b[0];

        if (cursor2_Colour_r != null) View.CURSOR2_COLOUR_R[0] = cursor2_Colour_r[0];
        if (cursor2_Colour_g != null) View.CURSOR2_COLOUR_G[0] = cursor2_Colour_g[0];
        if (cursor2_Colour_b != null) View.CURSOR2_COLOUR_B[0] = cursor2_Colour_b[0];

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

    public void setCoplanarity_angle_warning(double coplanarityAngleWarning) {
        this.coplanarity_angle_warning = coplanarityAngleWarning;
    }

    public double getCoplanarity_angle_error() {
        return coplanarity_angle_error;
    }

    public void setCoplanarity_angle_error(double coplanarityAngleError) {
        this.coplanarity_angle_error = coplanarityAngleError;
    }

    public double getViewportScaleFactor() {
        return viewportScaleFactor;
    }

    public void setViewportScaleFactor(double viewportScaleFactor) {
        this.viewportScaleFactor = viewportScaleFactor;
    }

    public int getMouseButtonLayout() {
        return mouseButtonLayout;
    }

    public void setMouseButtonLayout(int mouseButtonLayout) {
        this.mouseButtonLayout = mouseButtonLayout;
    }

    public boolean isInvertingWheelZoomDirection() {
        return invertingWheelZoomDirection;
    }

    public void setInvertingWheelZoomDirection(boolean invertingWheelZoomDirection) {
        this.invertingWheelZoomDirection = invertingWheelZoomDirection;
    }
}
