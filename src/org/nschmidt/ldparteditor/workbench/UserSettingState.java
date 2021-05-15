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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.wb.swt.SWTResourceManager;
import org.nschmidt.ldparteditor.composite.ToolItemState;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.enumtype.Colour;
import org.nschmidt.ldparteditor.enumtype.LDConfig;
import org.nschmidt.ldparteditor.enumtype.Task;
import org.nschmidt.ldparteditor.enumtype.TextEditorColour;
import org.nschmidt.ldparteditor.enumtype.TextTask;
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
    private List<GColour> userPalette = new ArrayList<>();

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

    private List<String> recentItems = new ArrayList<>();

    private Locale locale = Locale.US;

    /** {@code true} if the user has got the information that BFC certification is mandatory for the LDraw Standard Preview Mode  */
    private boolean bfcCertificationRequiredForLDrawMode = false;

    private String[] key3DStrings = null;
    private String[] key3DKeys = null;
    private Task[] key3DTasks = null;

    private String[] keyTextStrings = null;
    private String[] keyTextKeys = null;
    private TextTask[] keyTextTasks = null;

    private List<ToolItemState> toolItemConfig3D = new ArrayList<>();

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

    //  I need the arrays to detect if the value was not initialised before.
    // Otherwise it would be just 0 on new fields.
    private float[] color16OverrideColour = null;
    private float[] bfcFrontColour = null;
    private float[] bfcBackColour = null;
    private float[] bfcUncertifiedColour = null;
    private float[] vertexColour = null;
    private float[] vertexSelectedColour = null;
    private float[] condlineSelectedColour = null;
    private float[] lineColour = null;
    private float[] meshlineColour = null;
    private float[] condlineHiddenColour = null;
    private float[] condlineShownColour = null;
    private float[] backgroundColour = null;
    private float[] light1Colour = null;
    private float[] light1SpecularColour = null;
    private float[] light2Colour = null;
    private float[] light2SpecularColour = null;
    private float[] light3Colour = null;
    private float[] light3SpecularColour = null;
    private float[] light4Colour = null;
    private float[] light4SpecularColour = null;
    private float[] manipulatorSelectedColour = null;
    private float[] manipulatorInnerCircleColour = null;
    private float[] manipulatorOuterCircleColour = null;
    private float[] manipulatorXAxisColour = null;
    private float[] manipulatorYAxisColour = null;
    private float[] manipulatorZAxisColour = null;
    private float[] addObjectColour = null;
    private float[] originColour = null;
    private float[] grid10Colour = null;
    private float[] gridColour = null;
    private float[] rubberBandColour = null;
    private float[] textColour = null;
    private float[] xAxisColour = null;
    private float[] yAxisColour = null;
    private float[] zAxisColour = null;
    private float[] primitiveBackgroundColour = null;
    private float[] primitiveSignFGColour = null;
    private float[] primitiveSignBGColour = null;
    private float[] primitivePlusAndMinusColour = null;
    private float[] primitiveSelectedCellColour = null;
    private float[] primitiveFocusedCellColour = null;
    private float[] primitiveNormalCellColour = null;
    private float[] primitiveCell1Colour = null;
    private float[] primitiveCell2Colour = null;
    private float[] primitiveCategoryCell1Colour = null;
    private float[] primitiveCategoryCell2Colour = null;
    private float[] primitiveEdgeColour = null;
    private float[] primitiveCondlineColour = null;
    private int[] lineBoxFontColour = null;
    private int[] lineColourAttrFontColour = null;
    private int[] lineCommentFontColour = null;
    private int[] lineErrorUnderlineColour = null;
    private int[] lineHighlightBackgroundColour = null;
    private int[] lineHighlightSelectedBackgroundColour = null;
    private int[] lineHintUnderlineColour = null;
    private int[] linePrimaryFontColour = null;
    private int[] lineQuadFontColour = null;
    private int[] lineSecondaryFontColour = null;
    private int[] lineWarningUnderlineColour = null;
    private int[] textBackgroundColour = null;
    private int[] textForegroundColour = null;
    private int[] textForegroundHiddenColour = null;
    private float[] cursor1ColourColour = null;
    private float[] cursor2ColourColour = null;

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
    public List<GColour> getUserPalette() {
        return userPalette;
    }

    /**
     * @param userPalette
     *            sets the colour palette (17 colours)
     */
    public void setUserPalette(List<GColour> userPalette) {
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

    public List<String> getRecentItems() {
        if (recentItems == null) recentItems = new ArrayList<>();
        return recentItems;
    }

    public void setRecentItems(List<String> recentItems) {
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

    public List<ToolItemState> getToolItemConfig3D() {
        return toolItemConfig3D;
    }

    public void setToolItemConfig3D(List<ToolItemState> toolItemConfig3D) {
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
        Map<String, Task> m1 = KeyStateManager.getTaskmap();
        Map<Task, String> m2 = KeyStateManager.getTaskKeymap();
        Map<String, TextTask> m3 = KeyStateManager.getTextTaskmap();
        Map<TextTask, String> m4 = KeyStateManager.getTextTaskKeymap();

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
            for (Entry<String, Task> entry : m1.entrySet()) {
                String k = entry.getKey();
                Task t = entry.getValue();
                String keyString = m2.get(t);
                key3DStrings[i] = keyString;
                key3DKeys[i] = k;
                key3DTasks[i] = t;
                i++;
            }
        }

        {
            int i = 0;
            for (Entry<String, TextTask> entry : m3.entrySet()) {
                String k = entry.getKey();
                TextTask t = entry.getValue();
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

        color16OverrideColour = new float[] {LDConfig.colour16overrideR,LDConfig.colour16overrideG,LDConfig.colour16overrideB};

        bfcFrontColour = new float[] {Colour.bfcFrontColourR,Colour.bfcFrontColourG,Colour.bfcFrontColourB};
        bfcBackColour = new float[] {Colour.bfcBackColourR,Colour.bfcBackColourG,Colour.bfcBackColourB};
        bfcUncertifiedColour = new float[] {Colour.bfcUncertifiedColourR,Colour.bfcUncertifiedColourG,Colour.bfcUncertifiedColourB};

        vertexColour = new float[] {Colour.vertexColourR,Colour.vertexColourG,Colour.vertexColourB};
        vertexSelectedColour = new float[] {Colour.vertexSelectedColourR,Colour.vertexSelectedColourG,Colour.vertexSelectedColourB};

        condlineSelectedColour = new float[] {Colour.condlineSelectedColourR,Colour.condlineSelectedColourG,Colour.condlineSelectedColourB};

        lineColour = new float[] {Colour.lineColourR,Colour.lineColourG,Colour.lineColourB};

        meshlineColour = new float[] {Colour.meshlineColourR,Colour.meshlineColourG,Colour.meshlineColourB};

        condlineHiddenColour = new float[] {Colour.condlineHiddenColourR,Colour.condlineHiddenColourG,Colour.condlineHiddenColourB};
        condlineShownColour = new float[] {Colour.condlineShownColourR,Colour.condlineShownColourG,Colour.condlineShownColourB};

        backgroundColour = new float[] {Colour.backgroundColourR,Colour.backgroundColourG,Colour.backgroundColourB};

        light1Colour = new float[] {Colour.light1ColourR,Colour.light1ColourG,Colour.light1ColourB};
        light1SpecularColour = new float[] {Colour.light1SpecularColourR,Colour.light1SpecularColourG,Colour.light1SpecularColourB};
        light2Colour = new float[] {Colour.light2ColourR,Colour.light2ColourG,Colour.light2ColourB};
        light2SpecularColour = new float[] {Colour.light2SpecularColourR,Colour.light2SpecularColourG,Colour.light2SpecularColourB};
        light3Colour = new float[] {Colour.light3ColourR,Colour.light3ColourG,Colour.light3ColourB};
        light3SpecularColour = new float[] {Colour.light3SpecularColourR,Colour.light3SpecularColourG,Colour.light3SpecularColourB};
        light4Colour = new float[] {Colour.light4ColourR,Colour.light4ColourG,Colour.light4ColourB};
        light4SpecularColour = new float[] {Colour.light4SpecularColourR,Colour.light4SpecularColourG,Colour.light4SpecularColourB};

        manipulatorSelectedColour = new float[] {Colour.manipulatorSelectedColourR,Colour.manipulatorSelectedColourG,Colour.manipulatorSelectedColourB};
        manipulatorInnerCircleColour = new float[] {Colour.manipulatorInnerCircleColourR,Colour.manipulatorInnerCircleColourG,Colour.manipulatorInnerCircleColourB};
        manipulatorOuterCircleColour = new float[] {Colour.manipulatorOuterCircleColourR,Colour.manipulatorOuterCircleColourG,Colour.manipulatorOuterCircleColourB};

        manipulatorXAxisColour = new float[] {Colour.manipulatorXAxisColourR,Colour.manipulatorXAxisColourG,Colour.manipulatorXAxisColourB};
        manipulatorYAxisColour = new float[] {Colour.manipulatorYAxisColourR,Colour.manipulatorYAxisColourG,Colour.manipulatorYAxisColourB};
        manipulatorZAxisColour = new float[] {Colour.manipulatorZAxisColourR,Colour.manipulatorZAxisColourG,Colour.manipulatorZAxisColourB};

        addObjectColour = new float[] {Colour.addObjectColourR,Colour.addObjectColourG,Colour.addObjectColourB};

        originColour = new float[] {Colour.originColourR,Colour.originColourG,Colour.originColourB};

        grid10Colour = new float[] {Colour.grid10ColourR,Colour.grid10ColourG,Colour.grid10ColourB};
        gridColour = new float[] {Colour.gridColourR,Colour.gridColourB,Colour.gridColourB};

        rubberBandColour = new float[] {Colour.rubberBandColourR,Colour.rubberBandColourG,Colour.rubberBandColourB};

        textColour = new float[] {Colour.textColourR,Colour.textColourG,Colour.textColourB};

        xAxisColour = new float[] {Colour.xAxisColourR,Colour.xAxisColourG,Colour.xAxisColourB};
        yAxisColour = new float[] {Colour.yAxisColourR,Colour.yAxisColourG,Colour.yAxisColourB};
        zAxisColour = new float[] {Colour.zAxisColourR,Colour.zAxisColourG,Colour.zAxisColourB};

        primitiveBackgroundColour = new float[] {Colour.primitiveBackgroundColourR,Colour.primitiveBackgroundColourG,Colour.primitiveBackgroundColourB};
        primitiveSignFGColour = new float[] {Colour.primitiveSignFgColourR,Colour.primitiveSignFgColourG,Colour.primitiveSignFgColourB};
        primitiveSignBGColour = new float[] {Colour.primitiveSignBgColourR,Colour.primitiveSignBgColourG,Colour.primitiveSignBgColourB};
        primitivePlusAndMinusColour = new float[] {Colour.primitivePlusNMinusColourR,Colour.primitivePlusNMinusColourG,Colour.primitivePlusNMinusColourB};

        primitiveSelectedCellColour = new float[] {Colour.primitiveSelectedCellColourR,Colour.primitiveSelectedCellColourG,Colour.primitiveSelectedCellColourB};
        primitiveFocusedCellColour = new float[] {Colour.primitiveFocusedCellColourR,Colour.primitiveFocusedCellColourG,Colour.primitiveFocusedCellColourB};
        primitiveNormalCellColour = new float[] {Colour.primitiveNormalCellColourR,Colour.primitiveNormalCellColourG,Colour.primitiveNormalCellColourB};

        primitiveCell1Colour = new float[] {Colour.primitiveCell1ColourR,Colour.primitiveCell1ColourG,Colour.primitiveCell1ColourB};
        primitiveCell2Colour = new float[] {Colour.primitiveCell2ColourR,Colour.primitiveCell2ColourG,Colour.primitiveCell2ColourB};

        primitiveCategoryCell1Colour = new float[] {Colour.primitiveCategoryCell1ColourR,Colour.primitiveCategoryCell1ColourG,Colour.primitiveCategoryCell1ColourB};
        primitiveCategoryCell2Colour = new float[] {Colour.primitiveCategoryCell2ColourR,Colour.primitiveCategoryCell2ColourG,Colour.primitiveCategoryCell2ColourB};

        primitiveEdgeColour = new float[] {Colour.primitiveEdgeColourR,Colour.primitiveEdgeColourG,Colour.primitiveEdgeColourB};
        primitiveCondlineColour = new float[] {Colour.primitiveCondlineColourR,Colour.primitiveCondlineColourG,Colour.primitiveCondlineColourB};

        lineBoxFontColour = new int[]{TextEditorColour.getLineBoxFont().getRed(),TextEditorColour.getLineBoxFont().getGreen(),TextEditorColour.getLineBoxFont().getBlue()};

        lineColourAttrFontColour = new int[]{TextEditorColour.getLineColourAttrFont().getRed(),TextEditorColour.getLineColourAttrFont().getGreen(),TextEditorColour.getLineColourAttrFont().getBlue()};

        lineCommentFontColour = new int[]{TextEditorColour.getLineCommentFont().getRed(),TextEditorColour.getLineCommentFont().getGreen(),TextEditorColour.getLineCommentFont().getBlue()};

        lineWarningUnderlineColour = new int[]{TextEditorColour.getLineWarningUnderline().getRed(),TextEditorColour.getLineWarningUnderline().getGreen(),TextEditorColour.getLineWarningUnderline().getBlue()};
        lineErrorUnderlineColour = new int[]{TextEditorColour.getLineErrorUnderline().getRed(),TextEditorColour.getLineErrorUnderline().getGreen(),TextEditorColour.getLineErrorUnderline().getBlue()};

        lineHighlightBackgroundColour = new int[]{TextEditorColour.getLineHighlightBackground().getRed(),TextEditorColour.getLineHighlightBackground().getGreen(),TextEditorColour.getLineHighlightBackground().getBlue()};
        lineHighlightSelectedBackgroundColour = new int[]{TextEditorColour.getLineHighlightSelectedBackground().getRed(),TextEditorColour.getLineHighlightSelectedBackground().getGreen(),TextEditorColour.getLineHighlightSelectedBackground().getBlue()};
        lineHintUnderlineColour = new int[]{TextEditorColour.getLineHintUnderline().getRed(),TextEditorColour.getLineHintUnderline().getGreen(),TextEditorColour.getLineHintUnderline().getBlue()};

        linePrimaryFontColour = new int[]{TextEditorColour.getLinePrimaryFont().getRed(),TextEditorColour.getLinePrimaryFont().getGreen(),TextEditorColour.getLinePrimaryFont().getBlue()};
        lineSecondaryFontColour = new int[]{TextEditorColour.getLineSecondaryFont().getRed(),TextEditorColour.getLineSecondaryFont().getGreen(),TextEditorColour.getLineSecondaryFont().getBlue()};

        lineQuadFontColour = new int[]{TextEditorColour.getLineQuadFont().getRed(),TextEditorColour.getLineQuadFont().getGreen(),TextEditorColour.getLineQuadFont().getBlue()};

        textBackgroundColour = new int[]{TextEditorColour.getTextBackground().getRed(),TextEditorColour.getTextBackground().getGreen(),TextEditorColour.getTextBackground().getBlue()};
        textForegroundColour = new int[]{TextEditorColour.getTextForeground().getRed(),TextEditorColour.getTextForeground().getGreen(),TextEditorColour.getTextForeground().getBlue()};

        textForegroundHiddenColour = new int[]{TextEditorColour.getTextForegroundHidden().getRed(),TextEditorColour.getTextForegroundHidden().getGreen(),TextEditorColour.getTextForegroundHidden().getBlue()};

        cursor1ColourColour = new float[] {Colour.cursor1ColourR,Colour.cursor1ColourG,Colour.cursor1ColourB};
        cursor2ColourColour = new float[] {Colour.cursor2ColourR,Colour.cursor2ColourG,Colour.cursor2ColourB};
    }

    @SuppressWarnings("java:S2696")
    public void loadColours() {

        if (color16OverrideColour != null) {
            LDConfig.colour16overrideR = color16OverrideColour[0];
            LDConfig.colour16overrideG = color16OverrideColour[1];
            LDConfig.colour16overrideB = color16OverrideColour[2];
        }

        if (bfcFrontColour != null) {
            Colour.bfcFrontColourR = bfcFrontColour[0];
            Colour.bfcFrontColourG = bfcFrontColour[1];
            Colour.bfcFrontColourB = bfcFrontColour[2];
        }

        if (bfcBackColour != null) {
            Colour.bfcBackColourR = bfcBackColour[0];
            Colour.bfcBackColourG = bfcBackColour[1];
            Colour.bfcBackColourB = bfcBackColour[2];
        }

        if (bfcUncertifiedColour != null) {
            Colour.bfcUncertifiedColourR = bfcUncertifiedColour[0];
            Colour.bfcUncertifiedColourG = bfcUncertifiedColour[1];
            Colour.bfcUncertifiedColourB = bfcUncertifiedColour[2];
        }

        if (vertexColour != null) {
            Colour.vertexColourR = vertexColour[0];
            Colour.vertexColourG = vertexColour[1];
            Colour.vertexColourB = vertexColour[2];
        }

        if (vertexSelectedColour != null) {
            Colour.vertexSelectedColourR = vertexSelectedColour[0];
            Colour.vertexSelectedColourG = vertexSelectedColour[1];
            Colour.vertexSelectedColourB = vertexSelectedColour[2];
        }

        if (condlineSelectedColour != null) {
            Colour.condlineSelectedColourR = condlineSelectedColour[0];
            Colour.condlineSelectedColourG = condlineSelectedColour[1];
            Colour.condlineSelectedColourB = condlineSelectedColour[2];
        }

        if (lineColour != null) {
            Colour.lineColourR = lineColour[0];
            Colour.lineColourG = lineColour[1];
            Colour.lineColourB = lineColour[2];
        }

        if (meshlineColour != null) {
            Colour.meshlineColourR = meshlineColour[0];
            Colour.meshlineColourG = meshlineColour[1];
            Colour.meshlineColourB = meshlineColour[2];
        }

        if (condlineHiddenColour != null) {
            Colour.condlineHiddenColourR = condlineHiddenColour[0];
            Colour.condlineHiddenColourG = condlineHiddenColour[1];
            Colour.condlineHiddenColourB = condlineHiddenColour[2];
        }

        if (condlineShownColour != null) {
            Colour.condlineShownColourR = condlineShownColour[0];
            Colour.condlineShownColourG = condlineShownColour[1];
            Colour.condlineShownColourB = condlineShownColour[2];
        }

        if (backgroundColour != null) {
            Colour.backgroundColourR = backgroundColour[0];
            Colour.backgroundColourG = backgroundColour[1];
            Colour.backgroundColourB = backgroundColour[2];
        }

        if (light1Colour != null) {
            Colour.light1ColourR = light1Colour[0];
            Colour.light1ColourG = light1Colour[1];
            Colour.light1ColourB = light1Colour[2];
        }

        if (light1SpecularColour != null) {
            Colour.light1SpecularColourR = light1SpecularColour[0];
            Colour.light1SpecularColourG = light1SpecularColour[1];
            Colour.light1SpecularColourB = light1SpecularColour[2];
        }

        if (light2Colour != null) {
            Colour.light2ColourR = light2Colour[0];
            Colour.light2ColourG = light2Colour[1];
            Colour.light2ColourB = light2Colour[2];
        }

        if (light2SpecularColour != null) {
            Colour.light2SpecularColourR = light2SpecularColour[0];
            Colour.light2SpecularColourG = light2SpecularColour[1];
            Colour.light2SpecularColourB = light2SpecularColour[2];
        }

        if (light3Colour != null) {
            Colour.light3ColourR = light3Colour[0];
            Colour.light3ColourG = light3Colour[1];
            Colour.light3ColourB = light3Colour[2];
        }

        if (light3SpecularColour != null) {
            Colour.light3SpecularColourR = light3SpecularColour[0];
            Colour.light3SpecularColourG = light3SpecularColour[1];
            Colour.light3SpecularColourB = light3SpecularColour[2];
        }

        if (light4Colour != null) {
            Colour.light4ColourR = light4Colour[0];
            Colour.light4ColourG = light4Colour[1];
            Colour.light4ColourB = light4Colour[2];
        }

        if (light4SpecularColour != null) {
            Colour.light4SpecularColourR = light4SpecularColour[0];
            Colour.light4SpecularColourG = light4SpecularColour[1];
            Colour.light4SpecularColourB = light4SpecularColour[2];
        }

        if (manipulatorSelectedColour != null) {
            Colour.manipulatorSelectedColourR = manipulatorSelectedColour[0];
            Colour.manipulatorSelectedColourG = manipulatorSelectedColour[1];
            Colour.manipulatorSelectedColourB = manipulatorSelectedColour[2];
        }

        if (manipulatorInnerCircleColour != null) {
            Colour.manipulatorInnerCircleColourR = manipulatorInnerCircleColour[0];
            Colour.manipulatorInnerCircleColourG = manipulatorInnerCircleColour[1];
            Colour.manipulatorInnerCircleColourB = manipulatorInnerCircleColour[2];
        }

        if (manipulatorOuterCircleColour != null) {
            Colour.manipulatorOuterCircleColourR = manipulatorOuterCircleColour[0];
            Colour.manipulatorOuterCircleColourG = manipulatorOuterCircleColour[1];
            Colour.manipulatorOuterCircleColourB = manipulatorOuterCircleColour[2];
        }

        if (manipulatorXAxisColour != null) {
            Colour.manipulatorXAxisColourR = manipulatorXAxisColour[0];
            Colour.manipulatorXAxisColourG = manipulatorXAxisColour[1];
            Colour.manipulatorXAxisColourB = manipulatorXAxisColour[2];
        }

        if (manipulatorYAxisColour != null) {
            Colour.manipulatorYAxisColourR = manipulatorYAxisColour[0];
            Colour.manipulatorYAxisColourG = manipulatorYAxisColour[1];
            Colour.manipulatorYAxisColourB = manipulatorYAxisColour[2];
        }

        if (manipulatorZAxisColour != null) {
            Colour.manipulatorZAxisColourR = manipulatorZAxisColour[0];
            Colour.manipulatorZAxisColourG = manipulatorZAxisColour[1];
            Colour.manipulatorZAxisColourB = manipulatorZAxisColour[2];
        }

        if (addObjectColour != null) {
            Colour.addObjectColourR = addObjectColour[0];
            Colour.addObjectColourG = addObjectColour[1];
            Colour.addObjectColourB = addObjectColour[2];
        }

        if (originColour != null) {
            Colour.originColourR = originColour[0];
            Colour.originColourG = originColour[1];
            Colour.originColourB = originColour[2];
        }

        if (grid10Colour != null) {
            Colour.grid10ColourR = grid10Colour[0];
            Colour.grid10ColourG = grid10Colour[1];
            Colour.grid10ColourB = grid10Colour[2];
        }

        if (gridColour != null) {
            Colour.gridColourR = gridColour[0];
            Colour.gridColourG = gridColour[1];
            Colour.gridColourB = gridColour[2];
        }

        if (rubberBandColour != null) {
            Colour.rubberBandColourR = rubberBandColour[0];
            Colour.rubberBandColourG = rubberBandColour[1];
            Colour.rubberBandColourB = rubberBandColour[2];
        }

        if (textColour != null) {
            Colour.textColourR = textColour[0];
            Colour.textColourG = textColour[1];
            Colour.textColourB = textColour[2];
        }

        if (xAxisColour != null) {
            Colour.xAxisColourR = xAxisColour[0];
            Colour.xAxisColourG = xAxisColour[1];
            Colour.xAxisColourB = xAxisColour[2];
        }

        if (yAxisColour != null) {
            Colour.yAxisColourR = yAxisColour[0];
            Colour.yAxisColourG = yAxisColour[1];
            Colour.yAxisColourB = yAxisColour[2];
        }

        if (zAxisColour != null) {
            Colour.zAxisColourR = zAxisColour[0];
            Colour.zAxisColourG = zAxisColour[1];
            Colour.zAxisColourB = zAxisColour[2];
        }

        if (primitiveBackgroundColour != null) {
            Colour.primitiveBackgroundColourR = primitiveBackgroundColour[0];
            Colour.primitiveBackgroundColourG = primitiveBackgroundColour[1];
            Colour.primitiveBackgroundColourB = primitiveBackgroundColour[2];
        }

        if (primitiveSignFGColour != null) {
            Colour.primitiveSignFgColourR = primitiveSignFGColour[0];
            Colour.primitiveSignFgColourG = primitiveSignFGColour[1];
            Colour.primitiveSignFgColourB = primitiveSignFGColour[2];
        }

        if (primitiveSignBGColour != null) {
            Colour.primitiveSignBgColourR = primitiveSignBGColour[0];
            Colour.primitiveSignBgColourG = primitiveSignBGColour[1];
            Colour.primitiveSignBgColourB = primitiveSignBGColour[2];
        }

        if (primitivePlusAndMinusColour != null) {
            Colour.primitivePlusNMinusColourR = primitivePlusAndMinusColour[0];
            Colour.primitivePlusNMinusColourG = primitivePlusAndMinusColour[1];
            Colour.primitivePlusNMinusColourB = primitivePlusAndMinusColour[2];
        }

        if (primitiveSelectedCellColour != null) {
            Colour.primitiveSelectedCellColourR = primitiveSelectedCellColour[0];
            Colour.primitiveSelectedCellColourG = primitiveSelectedCellColour[1];
            Colour.primitiveSelectedCellColourB = primitiveSelectedCellColour[2];
        }

        if (primitiveFocusedCellColour != null) {
            Colour.primitiveFocusedCellColourR = primitiveFocusedCellColour[0];
            Colour.primitiveFocusedCellColourG = primitiveFocusedCellColour[1];
            Colour.primitiveFocusedCellColourB = primitiveFocusedCellColour[2];
        }

        if (primitiveNormalCellColour != null) {
            Colour.primitiveNormalCellColourR = primitiveNormalCellColour[0];
            Colour.primitiveNormalCellColourG = primitiveNormalCellColour[1];
            Colour.primitiveNormalCellColourB = primitiveNormalCellColour[2];
        }

        if (primitiveCell1Colour != null) {
            Colour.primitiveCell1ColourR = primitiveCell1Colour[0];
            Colour.primitiveCell1ColourG = primitiveCell1Colour[1];
            Colour.primitiveCell1ColourB = primitiveCell1Colour[2];
        }

        if (primitiveCell2Colour != null) {
            Colour.primitiveCell2ColourR = primitiveCell2Colour[0];
            Colour.primitiveCell2ColourG = primitiveCell2Colour[1];
            Colour.primitiveCell2ColourB = primitiveCell2Colour[2];
        }

        if (primitiveCategoryCell1Colour != null) {
            Colour.primitiveCategoryCell1ColourR = primitiveCategoryCell1Colour[0];
            Colour.primitiveCategoryCell1ColourG = primitiveCategoryCell1Colour[1];
            Colour.primitiveCategoryCell1ColourB = primitiveCategoryCell1Colour[2];
        }

        if (primitiveCategoryCell2Colour != null) {
            Colour.primitiveCategoryCell2ColourR = primitiveCategoryCell2Colour[0];
            Colour.primitiveCategoryCell2ColourG = primitiveCategoryCell2Colour[1];
            Colour.primitiveCategoryCell2ColourB = primitiveCategoryCell2Colour[2];
        }

        if (primitiveEdgeColour != null) {
            Colour.primitiveEdgeColourR = primitiveEdgeColour[0];
            Colour.primitiveEdgeColourG = primitiveEdgeColour[1];
            Colour.primitiveEdgeColourB = primitiveEdgeColour[2];
        }

        if (primitiveCondlineColour != null) {
            Colour.primitiveCondlineColourR = primitiveCondlineColour[0];
            Colour.primitiveCondlineColourG = primitiveCondlineColour[1];
            Colour.primitiveCondlineColourB = primitiveCondlineColour[2];
        }

        if (cursor1ColourColour != null) {
            Colour.cursor1ColourR = cursor1ColourColour[0];
            Colour.cursor1ColourG = cursor1ColourColour[1];
            Colour.cursor1ColourB = cursor1ColourColour[2];
        }

        if (cursor2ColourColour != null) {
            Colour.cursor2ColourR = cursor2ColourColour[0];
            Colour.cursor2ColourG = cursor2ColourColour[1];
            Colour.cursor2ColourB = cursor2ColourColour[2];
        }

        if (lineBoxFontColour != null) {
            TextEditorColour.loadLineBoxFont(SWTResourceManager.getColor(lineBoxFontColour[0], lineBoxFontColour[1], lineBoxFontColour[2]));
        }

        if (lineColourAttrFontColour != null) {
            TextEditorColour.loadLineColourAttrFont(SWTResourceManager.getColor(lineColourAttrFontColour[0], lineColourAttrFontColour[1], lineColourAttrFontColour[2]));
        }

        if (lineCommentFontColour != null) {
            TextEditorColour.loadLineCommentFont(SWTResourceManager.getColor(lineCommentFontColour[0], lineCommentFontColour[1], lineCommentFontColour[2]));
        }

        if (lineErrorUnderlineColour != null) {
            TextEditorColour.loadLineErrorUnderline(SWTResourceManager.getColor(lineErrorUnderlineColour[0], lineErrorUnderlineColour[1], lineErrorUnderlineColour[2]));
        }

        if (lineHighlightBackgroundColour != null) {
            TextEditorColour.loadLineHighlightBackground(SWTResourceManager.getColor(lineHighlightBackgroundColour[0], lineHighlightBackgroundColour[1], lineHighlightBackgroundColour[2]));
        }

        if (lineHighlightSelectedBackgroundColour != null) {
            TextEditorColour.loadLineHighlightSelectedBackground(SWTResourceManager.getColor(lineHighlightSelectedBackgroundColour[0], lineHighlightSelectedBackgroundColour[1], lineHighlightSelectedBackgroundColour[2]));
        }

        if (lineHintUnderlineColour != null) {
            TextEditorColour.loadLineHintUnderline(SWTResourceManager.getColor(lineHintUnderlineColour[0], lineHintUnderlineColour[1], lineHintUnderlineColour[2]));
        }

        if (linePrimaryFontColour != null) {
            TextEditorColour.loadLinePrimaryFont(SWTResourceManager.getColor(linePrimaryFontColour[0], linePrimaryFontColour[1], linePrimaryFontColour[2]));
        }

        if (lineQuadFontColour != null) {
            TextEditorColour.loadLineQuadFont(SWTResourceManager.getColor(lineQuadFontColour[0], lineQuadFontColour[1], lineQuadFontColour[2]));
        }

        if (lineSecondaryFontColour != null) {
            TextEditorColour.loadLineSecondaryFont(SWTResourceManager.getColor(lineSecondaryFontColour[0], lineSecondaryFontColour[1], lineSecondaryFontColour[2]));
        }

        if (lineWarningUnderlineColour != null) {
            TextEditorColour.loadLineWarningUnderline(SWTResourceManager.getColor(lineWarningUnderlineColour[0], lineWarningUnderlineColour[1], lineWarningUnderlineColour[2]));
        }

        if (textBackgroundColour != null) {
            TextEditorColour.loadTextBackground(SWTResourceManager.getColor(textBackgroundColour[0], textBackgroundColour[1], textBackgroundColour[2]));
        }

        if (textForegroundColour != null) {
            TextEditorColour.loadTextForeground(SWTResourceManager.getColor(textForegroundColour[0], textForegroundColour[1], textForegroundColour[2]));
        }

        if (textForegroundHiddenColour != null) {
            TextEditorColour.loadTextForegroundHidden(SWTResourceManager.getColor(textForegroundHiddenColour[0], textForegroundHiddenColour[1], textForegroundHiddenColour[2]));
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
