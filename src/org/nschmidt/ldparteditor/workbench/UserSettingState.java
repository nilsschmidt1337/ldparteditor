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
import org.nschmidt.ldparteditor.enums.Task;
import org.nschmidt.ldparteditor.enums.TextTask;
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

    /** Your transformation matrix precision */
    private int transMatrixPrecision = 5;
    /** Your coordinates precision */
    private int coordsPrecision = 3;

    /** {@code true} if the user wants to delete this settings */
    private boolean resetOnStart = false;
    /** LDConfig.ldr */
    private String ldConfigPath = null;
    /** {@code true} if the user wants active synchronisation with the text editor */
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
        return syncWithTextEditor;
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
}
