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
    private BigDecimal coarseMoveSnap = new BigDecimal("1"); //$NON-NLS-1$
    /** Your coarse rotate snap value */
    private BigDecimal coarseRotateSnap = new BigDecimal("90"); //$NON-NLS-1$
    /** Your coarse scale snap value */
    private BigDecimal coarseScaleSnap = new BigDecimal("2"); //$NON-NLS-1$
    /** Your medium move snap value */
    private BigDecimal mediumMoveSnap = new BigDecimal("0.01"); //$NON-NLS-1$
    /** Your medium rotate snap value */
    private BigDecimal mediumRotateSnap = new BigDecimal("11.25"); //$NON-NLS-1$
    /** Your medium scale snap value */
    private BigDecimal mediumScaleSnap = new BigDecimal("1.1"); //$NON-NLS-1$
    /** Your fine move snap value */
    private BigDecimal fineMoveSnap = new BigDecimal("0.0001"); //$NON-NLS-1$
    /** Your fine rotate snap value */
    private BigDecimal fineRotateSnap = BigDecimal.ONE;
    /** Your fine scale snap value */
    private BigDecimal fineScaleSnap = new BigDecimal("1.001"); //$NON-NLS-1$

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

    private float[] color16OverrideR = null;
    private float[] color16OverrideG = null;
    private float[] color16OverrideB = null;

    private float[] bfcFrontColourR = null;
    private float[] bfcFrontColourG = null;
    private float[] bfcFrontColourB = null;

    private float[] bfcBackColourR = null;
    private float[] bfcBackColourG = null;
    private float[] bfcBackColourB = null;

    private float[] bfcUncertifiedColourR = null;
    private float[] bfcUncertifiedColourG = null;
    private float[] bfcUncertifiedColourB = null;

    private float[] vertexColourR = null;
    private float[] vertexColourG = null;
    private float[] vertexColourB = null;

    private float[] vertexSelectedColourR = null;
    private float[] vertexSelectedColourG = null;
    private float[] vertexSelectedColourB = null;

    private float[] condlineSelectedColourR = null;
    private float[] condlineSelectedColourG = null;
    private float[] condlineSelectedColourB = null;

    private float[] lineColourR = null;
    private float[] lineColourG = null;
    private float[] lineColourB = null;

    private float[] meshlineColourR = null;
    private float[] meshlineColourG = null;
    private float[] meshlineColourB = null;

    private float[] condlineColourR = null;
    private float[] condlineColourG = null;
    private float[] condlineColourB = null;

    private float[] condlineHiddenColourR = null;
    private float[] condlineHiddenColourG = null;
    private float[] condlineHiddenColourB = null;

    private float[] condlineShownColourR = null;
    private float[] condlineShownColourG = null;
    private float[] condlineShownColourB = null;

    private float[] backgroundColourR = null;
    private float[] backgroundColourG = null;
    private float[] backgroundColourB = null;

    private float[] light1ColourR = null;
    private float[] light1ColourG = null;
    private float[] light1ColourB = null;

    private float[] light1SpecularColourR = null;
    private float[] light1SpecularColourG = null;
    private float[] light1SpecularColourB = null;

    private float[] light2ColourR = null;
    private float[] light2ColourG = null;
    private float[] light2ColourB = null;

    private float[] light2SpecularColourR = null;
    private float[] light2SpecularColourG = null;
    private float[] light2SpecularColourB = null;

    private float[] light3ColourR = null;
    private float[] light3ColourG = null;
    private float[] light3ColourB = null;

    private float[] light3SpecularColourR = null;
    private float[] light3SpecularColourG = null;
    private float[] light3SpecularColourB = null;

    private float[] light4ColourR = null;
    private float[] light4ColourG = null;
    private float[] light4ColourB = null;

    private float[] light4SpecularColourR = null;
    private float[] light4SpecularColourG = null;
    private float[] light4SpecularColourB = null;

    private float[] manipulatorSelectedColourR = null;
    private float[] manipulatorSelectedColourG = null;
    private float[] manipulatorSelectedColourB = null;

    private float[] manipulatorInnerCircleColourR = null;
    private float[] manipulatorInnerCircleColourG = null;
    private float[] manipulatorInnerCircleColourB = null;

    private float[] manipulatorOuterCircleColourR = null;
    private float[] manipulatorOuterCircleColourG = null;
    private float[] manipulatorOuterCircleColourB = null;

    private float[] manipulatorXAxisColourR = null;
    private float[] manipulatorXAxisColourG = null;
    private float[] manipulatorXAxisColourB = null;

    private float[] manipulatorYAxisColourR = null;
    private float[] manipulatorYAxisColourG = null;
    private float[] manipulatorYAxisColourB = null;

    private float[] manipulatorZAxisColourR = null;
    private float[] manipulatorZAxisColourG = null;
    private float[] manipulatorZAxisColourB = null;

    private float[] addObjectColourR = null;
    private float[] addObjectColourG = null;
    private float[] addObjectColourB = null;

    private float[] originColourR = null;
    private float[] originColourG = null;
    private float[] originColourB = null;

    private float[] grid10ColourR = null;
    private float[] grid10ColourG = null;
    private float[] grid10ColourB = null;

    private float[] gridColourR = null;
    private float[] gridColourG = null;
    private float[] gridColourB = null;

    private float[] rubberBandColourR = null;
    private float[] rubberBandColourG = null;
    private float[] rubberBandColourB = null;

    private float[] textColourR = null;
    private float[] textColourG = null;
    private float[] textColourB = null;

    private float[] xAxisColourR = null;
    private float[] xAxisColourG = null;
    private float[] xAxisColourB = null;

    private float[] yAxisColourR = null;
    private float[] yAxisColourG = null;
    private float[] yAxisColourB = null;

    private float[] zAxisColourR = null;
    private float[] zAxisColourG = null;
    private float[] zAxisColourB = null;

    private float[] primitiveBackgroundColourR = null;
    private float[] primitiveBackgroundColourG = null;
    private float[] primitiveBackgroundColourB = null;

    private float[] primitiveSignFGColourR = null;
    private float[] primitiveSignFGColourG = null;
    private float[] primitiveSignFGColourB = null;

    private float[] primitiveSignBGColourR = null;
    private float[] primitiveSignBGColourG = null;
    private float[] primitiveSignBGColourB = null;

    private float[] primitivePlusAndMinusColourR = null;
    private float[] primitivePlusAndMinusColourG = null;
    private float[] primitivePlusAndMinusColourB = null;

    private float[] primitiveSelectedCellColourR = null;
    private float[] primitiveSelectedCellColourG = null;
    private float[] primitiveSelectedCellColourB = null;

    private float[] primitiveFocusedCellColourR = null;
    private float[] primitiveFocusedCellColourG = null;
    private float[] primitiveFocusedCellColourB = null;

    private float[] primitiveNormalCellColourR = null;
    private float[] primitiveNormalCellColourG = null;
    private float[] primitiveNormalCellColourB = null;

    private float[] primitiveCell1ColourR = null;
    private float[] primitiveCell1ColourG = null;
    private float[] primitiveCell1ColourB = null;

    private float[] primitiveCell2ColourR = null;
    private float[] primitiveCell2ColourG = null;
    private float[] primitiveCell2ColourB = null;

    private float[] primitiveCategoryCell1ColourR = null;
    private float[] primitiveCategoryCell1ColourG = null;
    private float[] primitiveCategoryCell1ColourB = null;

    private float[] primitiveCategoryCell2ColourR = null;
    private float[] primitiveCategoryCell2ColourG = null;
    private float[] primitiveCategoryCell2ColourB = null;

    private float[] primitiveEdgeColourR = null;
    private float[] primitiveEdgeColourG = null;
    private float[] primitiveEdgeColourB = null;

    private float[] primitiveCondlineColourR = null;
    private float[] primitiveCondlineColourG = null;
    private float[] primitiveCondlineColourB = null;

    private int[] lineBoxFontR = null;
    private int[] lineBoxFontG = null;
    private int[] lineBoxFontB = null;

    private int[] lineColourAttrFontR = null;
    private int[] lineColourAttrFontG = null;
    private int[] lineColourAttrFontB = null;

    private int[] lineCommentFontR = null;
    private int[] lineCommentFontG = null;
    private int[] lineCommentFontB = null;

    private int[] lineErrorUnderlineR = null;
    private int[] lineErrorUnderlineG = null;
    private int[] lineErrorUnderlineB = null;

    private int[] lineHighlightBackgroundR = null;
    private int[] lineHighlightBackgroundG = null;
    private int[] lineHighlightBackgroundB = null;

    private int[] lineHighlightSelectedBackgroundR = null;
    private int[] lineHighlightSelectedBackgroundG = null;
    private int[] lineHighlightSelectedBackgroundB = null;

    private int[] lineHintUnderlineR = null;
    private int[] lineHintUnderlineG = null;
    private int[] lineHintUnderlineB = null;

    private int[] linePrimaryFontR = null;
    private int[] linePrimaryFontG = null;
    private int[] linePrimaryFontB = null;

    private int[] lineQuadFontR = null;
    private int[] lineQuadFontG = null;
    private int[] lineQuadFontB = null;

    private int[] lineSecondaryFontR = null;
    private int[] lineSecondaryFontG = null;
    private int[] lineSecondaryFontB = null;

    private int[] lineWarningUnderlineR = null;
    private int[] lineWarningUnderlineG = null;
    private int[] lineWarningUnderlineB = null;

    private int[] textBackgroundR = null;
    private int[] textBackgroundG = null;
    private int[] textBackgroundB = null;

    private int[] textForegroundR = null;
    private int[] textForegroundG = null;
    private int[] textForegroundB = null;

    private int[] textForegroundHiddenR = null;
    private int[] textForegroundHiddenG = null;
    private int[] textForegroundHiddenB = null;

    private float[] cursor1ColourR = null;
    private float[] cursor1ColourG = null;
    private float[] cursor1ColourB = null;

    private float[] cursor2ColourR = null;
    private float[] cursor2ColourG = null;
    private float[] cursor2ColourB = null;

    private boolean syncingTabs = false;

    private int textWinArr = 2;

    private boolean roundX = false;
    private boolean roundY = false;
    private boolean roundZ = false;

    private transient int openGLVersion = 20;

    private boolean movingAdjacentData = false;

    private double coplanarityAngleWarning = 1d;
    private double coplanarityAngleError = 3d;
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

    public BigDecimal getCoarseMoveSnap() {
        return coarseMoveSnap;
    }

    public void setCoarseMoveSnap(BigDecimal coarseMoveSnap) {
        this.coarseMoveSnap = coarseMoveSnap;
    }

    public BigDecimal getCoarseRotateSnap() {
        return coarseRotateSnap;
    }

    public void setCoarseRotateSnap(BigDecimal coarseRotateSnap) {
        this.coarseRotateSnap = coarseRotateSnap;
    }

    public BigDecimal getCoarseScaleSnap() {
        return coarseScaleSnap;
    }

    public void setCoarseScaleSnap(BigDecimal coarseScaleSnap) {
        this.coarseScaleSnap = coarseScaleSnap;
    }

    public BigDecimal getMediumMoveSnap() {
        return mediumMoveSnap;
    }

    public void setMediumMoveSnap(BigDecimal mediumMoveSnap) {
        this.mediumMoveSnap = mediumMoveSnap;
    }

    public BigDecimal getMediumRotateSnap() {
        return mediumRotateSnap;
    }

    public void setMediumRotateSnap(BigDecimal mediumRotateSnap) {
        this.mediumRotateSnap = mediumRotateSnap;
    }

    public BigDecimal getMediumScaleSnap() {
        return mediumScaleSnap;
    }

    public void setMediumScaleSnap(BigDecimal mediumScaleSnap) {
        this.mediumScaleSnap = mediumScaleSnap;
    }

    public BigDecimal getFineMoveSnap() {
        return fineMoveSnap;
    }

    public void setFineMoveSnap(BigDecimal fineMoveSnap) {
        this.fineMoveSnap = fineMoveSnap;
    }

    public BigDecimal getFineRotateSnap() {
        return fineRotateSnap;
    }

    public void setFineRotateSnap(BigDecimal fineRotateSnap) {
        this.fineRotateSnap = fineRotateSnap;
    }

    public BigDecimal getFineScaleSnap() {
        return fineScaleSnap;
    }

    public void setFineScaleSnap(BigDecimal fineScaleSnap) {
        this.fineScaleSnap = fineScaleSnap;
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

        color16OverrideR = View.COLOUR16_OVERRIDE_R;
        color16OverrideG = View.COLOUR16_OVERRIDE_G;
        color16OverrideB = View.COLOUR16_OVERRIDE_B;

        bfcFrontColourR = View.BFC_FRONT_COLOUR_R;
        bfcFrontColourG = View.BFC_FRONT_COLOUR_G;
        bfcFrontColourB = View.BFC_FRONT_COLOUR_B;

        bfcBackColourR = View.BFC_BACK__COLOUR_R;
        bfcBackColourG = View.BFC_BACK__COLOUR_G;
        bfcBackColourB = View.BFC_BACK__COLOUR_B;

        bfcUncertifiedColourR = View.BFC_UNCERTIFIED_COLOUR_R;
        bfcUncertifiedColourG = View.BFC_UNCERTIFIED_COLOUR_G;
        bfcUncertifiedColourB = View.BFC_UNCERTIFIED_COLOUR_B;

        vertexColourR = View.VERTEX_COLOUR_R;
        vertexColourG = View.VERTEX_COLOUR_G;
        vertexColourB = View.VERTEX_COLOUR_B;

        vertexSelectedColourR = View.VERTEX_SELECTED_COLOUR_R;
        vertexSelectedColourG = View.VERTEX_SELECTED_COLOUR_G;
        vertexSelectedColourB = View.VERTEX_SELECTED_COLOUR_B;

        condlineSelectedColourR = View.CONDLINE_SELECTED_COLOUR_R;
        condlineSelectedColourG = View.CONDLINE_SELECTED_COLOUR_G;
        condlineSelectedColourB = View.CONDLINE_SELECTED_COLOUR_B;

        lineColourR = View.LINE_COLOUR_R;
        lineColourG = View.LINE_COLOUR_G;
        lineColourB = View.LINE_COLOUR_B;

        meshlineColourR = View.MESHLINE_COLOUR_R;
        meshlineColourG = View.MESHLINE_COLOUR_G;
        meshlineColourB = View.MESHLINE_COLOUR_B;

        condlineColourR = View.CONDLINE_COLOUR_R;
        condlineColourG = View.CONDLINE_COLOUR_G;
        condlineColourB = View.CONDLINE_COLOUR_B;

        condlineHiddenColourR = View.CONDLINE_HIDDEN_COLOUR_R;
        condlineHiddenColourG = View.CONDLINE_HIDDEN_COLOUR_G;
        condlineHiddenColourB = View.CONDLINE_HIDDEN_COLOUR_B;

        condlineShownColourR = View.CONDLINE_SHOWN_COLOUR_R;
        condlineShownColourG = View.CONDLINE_SHOWN_COLOUR_G;
        condlineShownColourB = View.CONDLINE_SHOWN_COLOUR_B;

        backgroundColourR = View.BACKGROUND_COLOUR_R;
        backgroundColourG = View.BACKGROUND_COLOUR_G;
        backgroundColourB = View.BACKGROUND_COLOUR_B;

        light1ColourR = View.LIGHT1_COLOUR_R;
        light1ColourG = View.LIGHT1_COLOUR_G;
        light1ColourB = View.LIGHT1_COLOUR_B;

        light1SpecularColourR = View.LIGHT1_SPECULAR_COLOUR_R;
        light1SpecularColourG = View.LIGHT1_SPECULAR_COLOUR_G;
        light1SpecularColourB = View.LIGHT1_SPECULAR_COLOUR_B;

        light2ColourR = View.LIGHT2_COLOUR_R;
        light2ColourG = View.LIGHT2_COLOUR_G;
        light2ColourB = View.LIGHT2_COLOUR_B;

        light2SpecularColourR = View.LIGHT2_SPECULAR_COLOUR_R;
        light2SpecularColourG = View.LIGHT2_SPECULAR_COLOUR_G;
        light2SpecularColourB = View.LIGHT2_SPECULAR_COLOUR_B;

        light3ColourR = View.LIGHT3_COLOUR_R;
        light3ColourG = View.LIGHT3_COLOUR_G;
        light3ColourB = View.LIGHT3_COLOUR_B;

        light3SpecularColourR = View.LIGHT3_SPECULAR_COLOUR_R;
        light3SpecularColourG = View.LIGHT3_SPECULAR_COLOUR_G;
        light3SpecularColourB = View.LIGHT3_SPECULAR_COLOUR_B;

        light4ColourR = View.LIGHT4_COLOUR_R;
        light4ColourG = View.LIGHT4_COLOUR_G;
        light4ColourB = View.LIGHT4_COLOUR_B;

        light4SpecularColourR = View.LIGHT4_SPECULAR_COLOUR_R;
        light4SpecularColourG = View.LIGHT4_SPECULAR_COLOUR_G;
        light4SpecularColourB = View.LIGHT4_SPECULAR_COLOUR_B;

        manipulatorSelectedColourR = View.MANIPULATOR_SELECTED_COLOUR_R;
        manipulatorSelectedColourG = View.MANIPULATOR_SELECTED_COLOUR_G;
        manipulatorSelectedColourB = View.MANIPULATOR_SELECTED_COLOUR_B;

        manipulatorInnerCircleColourR = View.MANIPULATOR_INNERCIRCLE_COLOUR_R;
        manipulatorInnerCircleColourG = View.MANIPULATOR_INNERCIRCLE_COLOUR_G;
        manipulatorInnerCircleColourB = View.MANIPULATOR_INNERCIRCLE_COLOUR_B;

        manipulatorOuterCircleColourR = View.MANIPULATOR_OUTERCIRCLE_COLOUR_R;
        manipulatorOuterCircleColourG = View.MANIPULATOR_OUTERCIRCLE_COLOUR_G;
        manipulatorOuterCircleColourB = View.MANIPULATOR_OUTERCIRCLE_COLOUR_B;

        manipulatorXAxisColourR = View.MANIPULATOR_X_AXIS_COLOUR_R;
        manipulatorXAxisColourG = View.MANIPULATOR_X_AXIS_COLOUR_G;
        manipulatorXAxisColourB = View.MANIPULATOR_X_AXIS_COLOUR_B;

        manipulatorYAxisColourR = View.MANIPULATOR_Y_AXIS_COLOUR_R;
        manipulatorYAxisColourG = View.MANIPULATOR_Y_AXIS_COLOUR_G;
        manipulatorYAxisColourB = View.MANIPULATOR_Y_AXIS_COLOUR_B;

        manipulatorZAxisColourR = View.MANIPULATOR_Z_AXIS_COLOUR_R;
        manipulatorZAxisColourG = View.MANIPULATOR_Z_AXIS_COLOUR_G;
        manipulatorZAxisColourB = View.MANIPULATOR_Z_AXIS_COLOUR_B;

        addObjectColourR = View.ADD_OBJECT_COLOUR_R;
        addObjectColourG = View.ADD_OBJECT_COLOUR_G;
        addObjectColourB = View.ADD_OBJECT_COLOUR_B;

        originColourR = View.ORIGIN_COLOUR_R;
        originColourG = View.ORIGIN_COLOUR_G;
        originColourB = View.ORIGIN_COLOUR_B;

        grid10ColourR = View.GRID10_COLOUR_R;
        grid10ColourG = View.GRID10_COLOUR_G;
        grid10ColourB = View.GRID10_COLOUR_B;

        gridColourR = View.GRID_COLOUR_R;
        gridColourG = View.GRID_COLOUR_B;
        gridColourB = View.GRID_COLOUR_B;

        rubberBandColourR = View.RUBBER_BAND_COLOUR_R;
        rubberBandColourG = View.RUBBER_BAND_COLOUR_G;
        rubberBandColourB = View.RUBBER_BAND_COLOUR_B;

        textColourR = View.TEXT_COLOUR_R;
        textColourG = View.TEXT_COLOUR_G;
        textColourB = View.TEXT_COLOUR_B;

        xAxisColourR = View.X_AXIS_COLOUR_R;
        xAxisColourG = View.X_AXIS_COLOUR_G;
        xAxisColourB = View.X_AXIS_COLOUR_B;

        yAxisColourR = View.Y_AXIS_COLOUR_R;
        yAxisColourG = View.Y_AXIS_COLOUR_G;
        yAxisColourB = View.Y_AXIS_COLOUR_B;

        zAxisColourR = View.Z_AXIS_COLOUR_R;
        zAxisColourG = View.Z_AXIS_COLOUR_G;
        zAxisColourB = View.Z_AXIS_COLOUR_B;

        primitiveBackgroundColourR = View.PRIMITIVE_BACKGROUND_COLOUR_R;
        primitiveBackgroundColourG = View.PRIMITIVE_BACKGROUND_COLOUR_G;
        primitiveBackgroundColourB = View.PRIMITIVE_BACKGROUND_COLOUR_B;

        primitiveSignFGColourR = View.PRIMITIVE_SIGN_FG_COLOUR_R;
        primitiveSignFGColourG = View.PRIMITIVE_SIGN_FG_COLOUR_G;
        primitiveSignFGColourB = View.PRIMITIVE_SIGN_FG_COLOUR_B;

        primitiveSignBGColourR = View.PRIMITIVE_SIGN_BG_COLOUR_R;
        primitiveSignBGColourG = View.PRIMITIVE_SIGN_BG_COLOUR_G;
        primitiveSignBGColourB = View.PRIMITIVE_SIGN_BG_COLOUR_B;

        primitivePlusAndMinusColourR = View.PRIMITIVE_PLUS_N_MINUS_COLOUR_R;
        primitivePlusAndMinusColourG = View.PRIMITIVE_PLUS_N_MINUS_COLOUR_G;
        primitivePlusAndMinusColourB = View.PRIMITIVE_PLUS_N_MINUS_COLOUR_B;

        primitiveSelectedCellColourR = View.PRIMITIVE_SELECTED_CELL_COLOUR_R;
        primitiveSelectedCellColourG = View.PRIMITIVE_SELECTED_CELL_COLOUR_G;
        primitiveSelectedCellColourB = View.PRIMITIVE_SELECTED_CELL_COLOUR_B;

        primitiveFocusedCellColourR = View.PRIMITIVE_FOCUSED_CELL_COLOUR_R;
        primitiveFocusedCellColourG = View.PRIMITIVE_FOCUSED_CELL_COLOUR_G;
        primitiveFocusedCellColourB = View.PRIMITIVE_FOCUSED_CELL_COLOUR_B;

        primitiveNormalCellColourR = View.PRIMITIVE_NORMAL_CELL_COLOUR_R;
        primitiveNormalCellColourG = View.PRIMITIVE_NORMAL_CELL_COLOUR_G;
        primitiveNormalCellColourB = View.PRIMITIVE_NORMAL_CELL_COLOUR_B;

        primitiveCell1ColourR = View.PRIMITIVE_CELL_1_COLOUR_R;
        primitiveCell1ColourG = View.PRIMITIVE_CELL_1_COLOUR_G;
        primitiveCell1ColourB = View.PRIMITIVE_CELL_1_COLOUR_B;

        primitiveCell2ColourR = View.PRIMITIVE_CELL_2_COLOUR_R;
        primitiveCell2ColourG = View.PRIMITIVE_CELL_2_COLOUR_G;
        primitiveCell2ColourB = View.PRIMITIVE_CELL_2_COLOUR_B;

        primitiveCategoryCell1ColourR = View.PRIMITIVE_CATEGORYCELL_1_COLOUR_R;
        primitiveCategoryCell1ColourG = View.PRIMITIVE_CATEGORYCELL_1_COLOUR_G;
        primitiveCategoryCell1ColourB = View.PRIMITIVE_CATEGORYCELL_1_COLOUR_B;

        primitiveCategoryCell2ColourR = View.PRIMITIVE_CATEGORYCELL_2_COLOUR_R;
        primitiveCategoryCell2ColourG = View.PRIMITIVE_CATEGORYCELL_2_COLOUR_G;
        primitiveCategoryCell2ColourB = View.PRIMITIVE_CATEGORYCELL_2_COLOUR_B;

        primitiveEdgeColourR = View.PRIMITIVE_EDGE_COLOUR_R;
        primitiveEdgeColourG = View.PRIMITIVE_EDGE_COLOUR_G;
        primitiveEdgeColourB = View.PRIMITIVE_EDGE_COLOUR_B;

        primitiveCondlineColourR = View.PRIMITIVE_CONDLINE_COLOUR_R;
        primitiveCondlineColourG = View.PRIMITIVE_CONDLINE_COLOUR_G;
        primitiveCondlineColourB = View.PRIMITIVE_CONDLINE_COLOUR_B;

        lineBoxFontR = new int[]{Colour.lineBoxFont[0].getRed()};
        lineBoxFontG = new int[]{Colour.lineBoxFont[0].getGreen()};
        lineBoxFontB = new int[]{Colour.lineBoxFont[0].getBlue()};

        lineColourAttrFontR = new int[]{Colour.lineColourAttrFont[0].getRed()};
        lineColourAttrFontG = new int[]{Colour.lineColourAttrFont[0].getGreen()};
        lineColourAttrFontB = new int[]{Colour.lineColourAttrFont[0].getBlue()};

        lineCommentFontR = new int[]{Colour.lineCommentFont[0].getRed()};
        lineCommentFontG = new int[]{Colour.lineCommentFont[0].getGreen()};
        lineCommentFontB = new int[]{Colour.lineCommentFont[0].getBlue()};

        lineErrorUnderlineR = new int[]{Colour.lineErrorUnderline[0].getRed()};
        lineErrorUnderlineG = new int[]{Colour.lineErrorUnderline[0].getGreen()};
        lineErrorUnderlineB = new int[]{Colour.lineErrorUnderline[0].getBlue()};

        lineHighlightBackgroundR = new int[]{Colour.lineHighlightBackground[0].getRed()};
        lineHighlightBackgroundG = new int[]{Colour.lineHighlightBackground[0].getGreen()};
        lineHighlightBackgroundB = new int[]{Colour.lineHighlightBackground[0].getBlue()};

        lineHighlightSelectedBackgroundR = new int[]{Colour.lineHighlightSelectedBackground[0].getRed()};
        lineHighlightSelectedBackgroundG = new int[]{Colour.lineHighlightSelectedBackground[0].getGreen()};
        lineHighlightSelectedBackgroundB = new int[]{Colour.lineHighlightSelectedBackground[0].getBlue()};

        lineHintUnderlineR = new int[]{Colour.lineHintUnderline[0].getRed()};
        lineHintUnderlineG = new int[]{Colour.lineHintUnderline[0].getGreen()};
        lineHintUnderlineB = new int[]{Colour.lineHintUnderline[0].getBlue()};

        linePrimaryFontR = new int[]{Colour.linePrimaryFont[0].getRed()};
        linePrimaryFontG = new int[]{Colour.linePrimaryFont[0].getGreen()};
        linePrimaryFontB = new int[]{Colour.linePrimaryFont[0].getBlue()};

        lineQuadFontR = new int[]{Colour.lineQuadFont[0].getRed()};
        lineQuadFontG = new int[]{Colour.lineQuadFont[0].getGreen()};
        lineQuadFontB = new int[]{Colour.lineQuadFont[0].getBlue()};

        lineSecondaryFontR = new int[]{Colour.lineSecondaryFont[0].getRed()};
        lineSecondaryFontG = new int[]{Colour.lineSecondaryFont[0].getGreen()};
        lineSecondaryFontB = new int[]{Colour.lineSecondaryFont[0].getBlue()};

        lineWarningUnderlineR = new int[]{Colour.lineWarningUnderline[0].getRed()};
        lineWarningUnderlineG = new int[]{Colour.lineWarningUnderline[0].getGreen()};
        lineWarningUnderlineB = new int[]{Colour.lineWarningUnderline[0].getBlue()};

        textBackgroundR = new int[]{Colour.textBackground[0].getRed()};
        textBackgroundG = new int[]{Colour.textBackground[0].getGreen()};
        textBackgroundB = new int[]{Colour.textBackground[0].getBlue()};

        textForegroundR = new int[]{Colour.textForeground[0].getRed()};
        textForegroundG = new int[]{Colour.textForeground[0].getGreen()};
        textForegroundB = new int[]{Colour.textForeground[0].getBlue()};

        textForegroundHiddenR = new int[]{Colour.textForegroundHidden[0].getRed()};
        textForegroundHiddenG = new int[]{Colour.textForegroundHidden[0].getGreen()};
        textForegroundHiddenB = new int[]{Colour.textForegroundHidden[0].getBlue()};

        cursor1ColourR = View.CURSOR1_COLOUR_R;
        cursor1ColourG = View.CURSOR1_COLOUR_G;
        cursor1ColourB = View.CURSOR1_COLOUR_B;

        cursor2ColourR = View.CURSOR2_COLOUR_R;
        cursor2ColourG = View.CURSOR2_COLOUR_G;
        cursor2ColourB = View.CURSOR2_COLOUR_B;
    }

    public void loadColours() {

        if (color16OverrideR != null) View.COLOUR16_OVERRIDE_R[0] = color16OverrideR[0];
        if (color16OverrideG != null) View.COLOUR16_OVERRIDE_G[0] = color16OverrideG[0];
        if (color16OverrideB != null) View.COLOUR16_OVERRIDE_B[0] = color16OverrideB[0];

        if (bfcFrontColourR != null) View.BFC_FRONT_COLOUR_R[0] = bfcFrontColourR[0];
        if (bfcFrontColourG != null) View.BFC_FRONT_COLOUR_G[0] = bfcFrontColourG[0];
        if (bfcFrontColourB != null) View.BFC_FRONT_COLOUR_B[0] = bfcFrontColourB[0];

        if (bfcBackColourR != null) View.BFC_BACK__COLOUR_R[0] = bfcBackColourR[0];
        if (bfcBackColourG != null) View.BFC_BACK__COLOUR_G[0] = bfcBackColourG[0];
        if (bfcBackColourB != null) View.BFC_BACK__COLOUR_B[0] = bfcBackColourB[0];

        if (bfcUncertifiedColourR != null) View.BFC_UNCERTIFIED_COLOUR_R[0] = bfcUncertifiedColourR[0];
        if (bfcUncertifiedColourG != null) View.BFC_UNCERTIFIED_COLOUR_G[0] = bfcUncertifiedColourG[0];
        if (bfcUncertifiedColourB != null) View.BFC_UNCERTIFIED_COLOUR_B[0] = bfcUncertifiedColourB[0];

        if (vertexColourR != null) View.VERTEX_COLOUR_R[0] = vertexColourR[0];
        if (vertexColourG != null) View.VERTEX_COLOUR_G[0] = vertexColourG[0];
        if (vertexColourB != null) View.VERTEX_COLOUR_B[0] = vertexColourB[0];

        if (vertexSelectedColourR != null) View.VERTEX_SELECTED_COLOUR_R[0] = vertexSelectedColourR[0];
        if (vertexSelectedColourG != null) View.VERTEX_SELECTED_COLOUR_G[0] = vertexSelectedColourG[0];
        if (vertexSelectedColourB != null) View.VERTEX_SELECTED_COLOUR_B[0] = vertexSelectedColourB[0];

        if (condlineSelectedColourR != null) View.CONDLINE_SELECTED_COLOUR_R[0] = condlineSelectedColourR[0];
        if (condlineSelectedColourG != null) View.CONDLINE_SELECTED_COLOUR_G[0] = condlineSelectedColourG[0];
        if (condlineSelectedColourB != null) View.CONDLINE_SELECTED_COLOUR_B[0] = condlineSelectedColourB[0];

        if (lineColourR != null) View.LINE_COLOUR_R[0] = lineColourR[0];
        if (lineColourG != null) View.LINE_COLOUR_G[0] = lineColourG[0];
        if (lineColourB != null) View.LINE_COLOUR_B[0] = lineColourB[0];

        if (meshlineColourR != null) View.MESHLINE_COLOUR_R[0] = meshlineColourR[0];
        if (meshlineColourG != null) View.MESHLINE_COLOUR_G[0] = meshlineColourG[0];
        if (meshlineColourB != null) View.MESHLINE_COLOUR_B[0] = meshlineColourB[0];

        if (condlineColourR != null) View.CONDLINE_COLOUR_R[0] = condlineColourR[0];
        if (condlineColourG != null) View.CONDLINE_COLOUR_G[0] = condlineColourG[0];
        if (condlineColourB != null) View.CONDLINE_COLOUR_B[0] = condlineColourB[0];

        if (condlineHiddenColourR != null) View.CONDLINE_HIDDEN_COLOUR_R[0] = condlineHiddenColourR[0];
        if (condlineHiddenColourG != null) View.CONDLINE_HIDDEN_COLOUR_G[0] = condlineHiddenColourG[0];
        if (condlineHiddenColourB != null) View.CONDLINE_HIDDEN_COLOUR_B[0] = condlineHiddenColourB[0];

        if (condlineShownColourR != null) View.CONDLINE_SHOWN_COLOUR_R[0] = condlineShownColourR[0];
        if (condlineShownColourG != null) View.CONDLINE_SHOWN_COLOUR_G[0] = condlineShownColourG[0];
        if (condlineShownColourB != null) View.CONDLINE_SHOWN_COLOUR_B[0] = condlineShownColourB[0];

        if (backgroundColourR != null) View.BACKGROUND_COLOUR_R[0] = backgroundColourR[0];
        if (backgroundColourG != null) View.BACKGROUND_COLOUR_G[0] = backgroundColourG[0];
        if (backgroundColourB != null) View.BACKGROUND_COLOUR_B[0] = backgroundColourB[0];

        if (light1ColourR != null) View.LIGHT1_COLOUR_R[0] = light1ColourR[0];
        if (light1ColourG != null) View.LIGHT1_COLOUR_G[0] = light1ColourG[0];
        if (light1ColourB != null) View.LIGHT1_COLOUR_B[0] = light1ColourB[0];

        if (light1SpecularColourR != null) View.LIGHT1_SPECULAR_COLOUR_R[0] = light1SpecularColourR[0];
        if (light1SpecularColourG != null) View.LIGHT1_SPECULAR_COLOUR_G[0] = light1SpecularColourG[0];
        if (light1SpecularColourB != null) View.LIGHT1_SPECULAR_COLOUR_B[0] = light1SpecularColourB[0];

        if (light2ColourR != null) View.LIGHT2_COLOUR_R[0] = light2ColourR[0];
        if (light2ColourG != null) View.LIGHT2_COLOUR_G[0] = light2ColourG[0];
        if (light2ColourB != null) View.LIGHT2_COLOUR_B[0] = light2ColourB[0];

        if (light2SpecularColourR != null) View.LIGHT2_SPECULAR_COLOUR_R[0] = light2SpecularColourR[0];
        if (light2SpecularColourG != null) View.LIGHT2_SPECULAR_COLOUR_G[0] = light2SpecularColourG[0];
        if (light2SpecularColourB != null) View.LIGHT2_SPECULAR_COLOUR_B[0] = light2SpecularColourB[0];

        if (light3ColourR != null) View.LIGHT3_COLOUR_R[0] = light3ColourR[0];
        if (light3ColourG != null) View.LIGHT3_COLOUR_G[0] = light3ColourG[0];
        if (light3ColourB != null) View.LIGHT3_COLOUR_B[0] = light3ColourB[0];

        if (light3SpecularColourR != null) View.LIGHT3_SPECULAR_COLOUR_R[0] = light3SpecularColourR[0];
        if (light3SpecularColourG != null) View.LIGHT3_SPECULAR_COLOUR_G[0] = light3SpecularColourG[0];
        if (light3SpecularColourB != null) View.LIGHT3_SPECULAR_COLOUR_B[0] = light3SpecularColourB[0];

        if (light4ColourR != null) View.LIGHT4_COLOUR_R[0] = light4ColourR[0];
        if (light4ColourG != null) View.LIGHT4_COLOUR_G[0] = light4ColourG[0];
        if (light4ColourB != null) View.LIGHT4_COLOUR_B[0] = light4ColourB[0];

        if (light4SpecularColourR != null) View.LIGHT4_SPECULAR_COLOUR_R[0] = light4SpecularColourR[0];
        if (light4SpecularColourG != null) View.LIGHT4_SPECULAR_COLOUR_G[0] = light4SpecularColourG[0];
        if (light4SpecularColourB != null) View.LIGHT4_SPECULAR_COLOUR_B[0] = light4SpecularColourB[0];

        if (manipulatorSelectedColourR != null) View.MANIPULATOR_SELECTED_COLOUR_R[0] = manipulatorSelectedColourR[0];
        if (manipulatorSelectedColourG != null) View.MANIPULATOR_SELECTED_COLOUR_G[0] = manipulatorSelectedColourG[0];
        if (manipulatorSelectedColourB != null) View.MANIPULATOR_SELECTED_COLOUR_B[0] = manipulatorSelectedColourB[0];

        if (manipulatorInnerCircleColourR != null) View.MANIPULATOR_INNERCIRCLE_COLOUR_R[0] = manipulatorInnerCircleColourR[0];
        if (manipulatorInnerCircleColourG != null) View.MANIPULATOR_INNERCIRCLE_COLOUR_G[0] = manipulatorInnerCircleColourG[0];
        if (manipulatorInnerCircleColourB != null) View.MANIPULATOR_INNERCIRCLE_COLOUR_B[0] = manipulatorInnerCircleColourB[0];

        if (manipulatorOuterCircleColourR != null) View.MANIPULATOR_OUTERCIRCLE_COLOUR_R[0] = manipulatorOuterCircleColourR[0];
        if (manipulatorOuterCircleColourG != null) View.MANIPULATOR_OUTERCIRCLE_COLOUR_G[0] = manipulatorOuterCircleColourG[0];
        if (manipulatorOuterCircleColourB != null) View.MANIPULATOR_OUTERCIRCLE_COLOUR_B[0] = manipulatorOuterCircleColourB[0];

        if (manipulatorXAxisColourR != null) View.MANIPULATOR_X_AXIS_COLOUR_R[0] = manipulatorXAxisColourR[0];
        if (manipulatorXAxisColourG != null) View.MANIPULATOR_X_AXIS_COLOUR_G[0] = manipulatorXAxisColourG[0];
        if (manipulatorXAxisColourB != null) View.MANIPULATOR_X_AXIS_COLOUR_B[0] = manipulatorXAxisColourB[0];

        if (manipulatorYAxisColourR != null) View.MANIPULATOR_Y_AXIS_COLOUR_R[0] = manipulatorYAxisColourR[0];
        if (manipulatorYAxisColourG != null) View.MANIPULATOR_Y_AXIS_COLOUR_G[0] = manipulatorYAxisColourG[0];
        if (manipulatorYAxisColourB != null) View.MANIPULATOR_Y_AXIS_COLOUR_B[0] = manipulatorYAxisColourB[0];

        if (manipulatorZAxisColourR != null) View.MANIPULATOR_Z_AXIS_COLOUR_R[0] = manipulatorZAxisColourR[0];
        if (manipulatorZAxisColourG != null) View.MANIPULATOR_Z_AXIS_COLOUR_G[0] = manipulatorZAxisColourG[0];
        if (manipulatorZAxisColourB != null) View.MANIPULATOR_Z_AXIS_COLOUR_B[0] = manipulatorZAxisColourB[0];

        if (addObjectColourR != null) View.ADD_OBJECT_COLOUR_R[0] = addObjectColourR[0];
        if (addObjectColourG != null) View.ADD_OBJECT_COLOUR_G[0] = addObjectColourG[0];
        if (addObjectColourB != null) View.ADD_OBJECT_COLOUR_B[0] = addObjectColourB[0];

        if (originColourR != null) View.ORIGIN_COLOUR_R[0] = originColourR[0];
        if (originColourG != null) View.ORIGIN_COLOUR_G[0] = originColourG[0];
        if (originColourB != null) View.ORIGIN_COLOUR_B[0] = originColourB[0];

        if (grid10ColourR != null) View.GRID10_COLOUR_R[0] = grid10ColourR[0];
        if (grid10ColourG != null) View.GRID10_COLOUR_G[0] = grid10ColourG[0];
        if (grid10ColourB != null) View.GRID10_COLOUR_B[0] = grid10ColourB[0];

        if (gridColourR != null) View.GRID_COLOUR_R[0] = gridColourR[0];
        if (gridColourG != null) View.GRID_COLOUR_G[0] = gridColourG[0];
        if (gridColourB != null) View.GRID_COLOUR_B[0] = gridColourB[0];

        if (rubberBandColourR != null) View.RUBBER_BAND_COLOUR_R[0] = rubberBandColourR[0];
        if (rubberBandColourG != null) View.RUBBER_BAND_COLOUR_G[0] = rubberBandColourG[0];
        if (rubberBandColourB != null) View.RUBBER_BAND_COLOUR_B[0] = rubberBandColourB[0];

        if (textColourR != null) View.TEXT_COLOUR_R[0] = textColourR[0];
        if (textColourG != null) View.TEXT_COLOUR_G[0] = textColourG[0];
        if (textColourB != null) View.TEXT_COLOUR_B[0] = textColourB[0];

        if (xAxisColourR != null) View.X_AXIS_COLOUR_R[0] = xAxisColourR[0];
        if (xAxisColourG != null) View.X_AXIS_COLOUR_G[0] = xAxisColourG[0];
        if (xAxisColourB != null) View.X_AXIS_COLOUR_B[0] = xAxisColourB[0];

        if (yAxisColourR != null) View.Y_AXIS_COLOUR_R[0] = yAxisColourR[0];
        if (yAxisColourG != null) View.Y_AXIS_COLOUR_G[0] = yAxisColourG[0];
        if (yAxisColourB != null) View.Y_AXIS_COLOUR_B[0] = yAxisColourB[0];

        if (zAxisColourR != null) View.Z_AXIS_COLOUR_R[0] = zAxisColourR[0];
        if (zAxisColourG != null) View.Z_AXIS_COLOUR_G[0] = zAxisColourG[0];
        if (zAxisColourB != null) View.Z_AXIS_COLOUR_B[0] = zAxisColourB[0];

        if (primitiveBackgroundColourR != null) View.PRIMITIVE_BACKGROUND_COLOUR_R[0] = primitiveBackgroundColourR[0];
        if (primitiveBackgroundColourG != null) View.PRIMITIVE_BACKGROUND_COLOUR_G[0] = primitiveBackgroundColourG[0];
        if (primitiveBackgroundColourB != null) View.PRIMITIVE_BACKGROUND_COLOUR_B[0] = primitiveBackgroundColourB[0];

        if (primitiveSignFGColourR != null) View.PRIMITIVE_SIGN_FG_COLOUR_R[0] = primitiveSignFGColourR[0];
        if (primitiveSignFGColourG != null) View.PRIMITIVE_SIGN_FG_COLOUR_G[0] = primitiveSignFGColourG[0];
        if (primitiveSignFGColourB != null) View.PRIMITIVE_SIGN_FG_COLOUR_B[0] = primitiveSignFGColourB[0];

        if (primitiveSignBGColourR != null) View.PRIMITIVE_SIGN_BG_COLOUR_R[0] = primitiveSignBGColourR[0];
        if (primitiveSignBGColourG != null) View.PRIMITIVE_SIGN_BG_COLOUR_G[0] = primitiveSignBGColourG[0];
        if (primitiveSignBGColourB != null) View.PRIMITIVE_SIGN_BG_COLOUR_B[0] = primitiveSignBGColourB[0];

        if (primitivePlusAndMinusColourR != null) View.PRIMITIVE_PLUS_N_MINUS_COLOUR_R[0] = primitivePlusAndMinusColourR[0];
        if (primitivePlusAndMinusColourG != null) View.PRIMITIVE_PLUS_N_MINUS_COLOUR_G[0] = primitivePlusAndMinusColourG[0];
        if (primitivePlusAndMinusColourB != null) View.PRIMITIVE_PLUS_N_MINUS_COLOUR_B[0] = primitivePlusAndMinusColourB[0];

        if (primitiveSelectedCellColourR != null) View.PRIMITIVE_SELECTED_CELL_COLOUR_R[0] = primitiveSelectedCellColourR[0];
        if (primitiveSelectedCellColourG != null) View.PRIMITIVE_SELECTED_CELL_COLOUR_G[0] = primitiveSelectedCellColourG[0];
        if (primitiveSelectedCellColourB != null) View.PRIMITIVE_SELECTED_CELL_COLOUR_B[0] = primitiveSelectedCellColourB[0];

        if (primitiveFocusedCellColourR != null) View.PRIMITIVE_FOCUSED_CELL_COLOUR_R[0] = primitiveFocusedCellColourR[0];
        if (primitiveFocusedCellColourG != null) View.PRIMITIVE_FOCUSED_CELL_COLOUR_G[0] = primitiveFocusedCellColourG[0];
        if (primitiveFocusedCellColourB != null) View.PRIMITIVE_FOCUSED_CELL_COLOUR_B[0] = primitiveFocusedCellColourB[0];

        if (primitiveNormalCellColourR != null) View.PRIMITIVE_NORMAL_CELL_COLOUR_R[0] = primitiveNormalCellColourR[0];
        if (primitiveNormalCellColourG != null) View.PRIMITIVE_NORMAL_CELL_COLOUR_G[0] = primitiveNormalCellColourG[0];
        if (primitiveNormalCellColourB != null) View.PRIMITIVE_NORMAL_CELL_COLOUR_B[0] = primitiveNormalCellColourB[0];

        if (primitiveCell1ColourR != null) View.PRIMITIVE_CELL_1_COLOUR_R[0] = primitiveCell1ColourR[0];
        if (primitiveCell1ColourG != null) View.PRIMITIVE_CELL_1_COLOUR_G[0] = primitiveCell1ColourG[0];
        if (primitiveCell1ColourB != null) View.PRIMITIVE_CELL_1_COLOUR_B[0] = primitiveCell1ColourB[0];

        if (primitiveCell2ColourR != null) View.PRIMITIVE_CELL_2_COLOUR_R[0] = primitiveCell2ColourR[0];
        if (primitiveCell2ColourG != null) View.PRIMITIVE_CELL_2_COLOUR_G[0] = primitiveCell2ColourG[0];
        if (primitiveCell2ColourB != null) View.PRIMITIVE_CELL_2_COLOUR_B[0] = primitiveCell2ColourB[0];

        if (primitiveCategoryCell1ColourR != null) View.PRIMITIVE_CATEGORYCELL_1_COLOUR_R[0] = primitiveCategoryCell1ColourR[0];
        if (primitiveCategoryCell1ColourG != null) View.PRIMITIVE_CATEGORYCELL_1_COLOUR_G[0] = primitiveCategoryCell1ColourG[0];
        if (primitiveCategoryCell1ColourB != null) View.PRIMITIVE_CATEGORYCELL_1_COLOUR_B[0] = primitiveCategoryCell1ColourB[0];

        if (primitiveCategoryCell2ColourR != null) View.PRIMITIVE_CATEGORYCELL_2_COLOUR_R[0] = primitiveCategoryCell2ColourR[0];
        if (primitiveCategoryCell2ColourG != null) View.PRIMITIVE_CATEGORYCELL_2_COLOUR_G[0] = primitiveCategoryCell2ColourG[0];
        if (primitiveCategoryCell2ColourB != null) View.PRIMITIVE_CATEGORYCELL_2_COLOUR_B[0] = primitiveCategoryCell2ColourB[0];

        if (primitiveEdgeColourR != null) View.PRIMITIVE_EDGE_COLOUR_R[0] = primitiveEdgeColourR[0];
        if (primitiveEdgeColourG != null) View.PRIMITIVE_EDGE_COLOUR_G[0] = primitiveEdgeColourG[0];
        if (primitiveEdgeColourB != null) View.PRIMITIVE_EDGE_COLOUR_B[0] = primitiveEdgeColourB[0];

        if (primitiveCondlineColourR != null) View.PRIMITIVE_CONDLINE_COLOUR_R[0] = primitiveCondlineColourR[0];
        if (primitiveCondlineColourG != null) View.PRIMITIVE_CONDLINE_COLOUR_G[0] = primitiveCondlineColourG[0];
        if (primitiveCondlineColourB != null) View.PRIMITIVE_CONDLINE_COLOUR_B[0] = primitiveCondlineColourB[0];

        if (cursor1ColourR != null) View.CURSOR1_COLOUR_R[0] = cursor1ColourR[0];
        if (cursor1ColourG != null) View.CURSOR1_COLOUR_G[0] = cursor1ColourG[0];
        if (cursor1ColourB != null) View.CURSOR1_COLOUR_B[0] = cursor1ColourB[0];

        if (cursor2ColourR != null) View.CURSOR2_COLOUR_R[0] = cursor2ColourR[0];
        if (cursor2ColourG != null) View.CURSOR2_COLOUR_G[0] = cursor2ColourG[0];
        if (cursor2ColourB != null) View.CURSOR2_COLOUR_B[0] = cursor2ColourB[0];

        if (lineBoxFontR != null) {
            Colour.lineBoxFont[0] = SWTResourceManager.getColor(lineBoxFontR[0], lineBoxFontG[0], lineBoxFontB[0]);
        }

        if (lineColourAttrFontR != null) {
            Colour.lineColourAttrFont[0] = SWTResourceManager.getColor(lineColourAttrFontR[0], lineColourAttrFontG[0], lineColourAttrFontB[0]);
        }

        if (lineCommentFontR != null) {
            Colour.lineCommentFont[0] = SWTResourceManager.getColor(lineCommentFontR[0], lineCommentFontG[0], lineCommentFontB[0]);
        }

        if (lineErrorUnderlineR != null) {
            Colour.lineErrorUnderline[0] = SWTResourceManager.getColor(lineErrorUnderlineR[0], lineErrorUnderlineG[0], lineErrorUnderlineB[0]);
        }

        if (lineHighlightBackgroundR != null) {
            Colour.lineHighlightBackground[0] = SWTResourceManager.getColor(lineHighlightBackgroundR[0], lineHighlightBackgroundG[0], lineHighlightBackgroundB[0]);
        }

        if (lineHighlightSelectedBackgroundR != null) {
            Colour.lineHighlightSelectedBackground[0] = SWTResourceManager.getColor(lineHighlightSelectedBackgroundR[0], lineHighlightSelectedBackgroundG[0], lineHighlightSelectedBackgroundB[0]);
        }

        if (lineHintUnderlineR != null) {
            Colour.lineHintUnderline[0] = SWTResourceManager.getColor(lineHintUnderlineR[0], lineHintUnderlineG[0], lineHintUnderlineB[0]);
        }

        if (linePrimaryFontR != null) {
            Colour.linePrimaryFont[0] = SWTResourceManager.getColor(linePrimaryFontR[0], linePrimaryFontG[0], linePrimaryFontB[0]);
        }

        if (lineQuadFontR != null) {
            Colour.lineQuadFont[0] = SWTResourceManager.getColor(lineQuadFontR[0], lineQuadFontG[0], lineQuadFontB[0]);
        }

        if (lineSecondaryFontR != null) {
            Colour.lineSecondaryFont[0] = SWTResourceManager.getColor(lineSecondaryFontR[0], lineSecondaryFontG[0], lineSecondaryFontB[0]);
        }

        if (lineWarningUnderlineR != null) {
            Colour.lineWarningUnderline[0] = SWTResourceManager.getColor(lineWarningUnderlineR[0], lineWarningUnderlineG[0], lineWarningUnderlineB[0]);
        }

        if (textBackgroundR != null) {
            Colour.textBackground[0] = SWTResourceManager.getColor(textBackgroundR[0], textBackgroundG[0], textBackgroundB[0]);
        }

        if (textForegroundR != null) {
            Colour.textForeground[0] = SWTResourceManager.getColor(textForegroundR[0], textForegroundG[0], textForegroundB[0]);
        }

        if (textForegroundHiddenR != null) {
            Colour.textForegroundHidden[0] = SWTResourceManager.getColor(textForegroundHiddenR[0], textForegroundHiddenG[0], textForegroundHiddenB[0]);
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

    public double getCoplanarityAngleWarning() {
        return coplanarityAngleWarning;
    }

    public void setCoplanarityAngleWarning(double coplanarityAngleWarning) {
        this.coplanarityAngleWarning = coplanarityAngleWarning;
    }

    public double getCoplanarityAngleError() {
        return coplanarityAngleError;
    }

    public void setCoplanarityAngleError(double coplanarityAngleError) {
        this.coplanarityAngleError = coplanarityAngleError;
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
