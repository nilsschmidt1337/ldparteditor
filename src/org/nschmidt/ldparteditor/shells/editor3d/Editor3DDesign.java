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
package org.nschmidt.ldparteditor.shells.editor3d;

import static org.nschmidt.ldparteditor.helpers.WidgetUtility.widgetUtil;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.lwjgl.util.vector.Matrix4f;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.composites.CompositeContainer;
import org.nschmidt.ldparteditor.composites.ToolItem;
import org.nschmidt.ldparteditor.composites.ToolItemDrawLocation;
import org.nschmidt.ldparteditor.composites.ToolItemDrawMode;
import org.nschmidt.ldparteditor.composites.ToolItemState;
import org.nschmidt.ldparteditor.composites.primitive.CompositePrimitive;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.dialogs.colour.ColourDialog;
import org.nschmidt.ldparteditor.enums.IconSize;
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.enums.OpenInWhat;
import org.nschmidt.ldparteditor.enums.Task;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.Cocoa;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.resources.ResourceManager;
import org.nschmidt.ldparteditor.shells.editortext.EditorTextWindow;
import org.nschmidt.ldparteditor.state.KeyStateManager;
import org.nschmidt.ldparteditor.text.LDParsingException;
import org.nschmidt.ldparteditor.text.StringHelper;
import org.nschmidt.ldparteditor.text.UTF8BufferedReader;
import org.nschmidt.ldparteditor.widgets.BigDecimalSpinner;
import org.nschmidt.ldparteditor.widgets.IntegerSpinner;
import org.nschmidt.ldparteditor.widgets.NButton;
import org.nschmidt.ldparteditor.widgets.Tree;
import org.nschmidt.ldparteditor.widgets.TreeItem;
import org.nschmidt.ldparteditor.workbench.Composite3DState;
import org.nschmidt.ldparteditor.workbench.Editor3DWindowState;
import org.nschmidt.ldparteditor.workbench.UserSettingState;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

import swing2swt.layout.BorderLayout;

/**
 * The 3D editor window
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 *
 * @author nils
 *
 */
class Editor3DDesign extends ApplicationWindow {

    /** The menu of the select features */
    private Menu mnuSelect;

    final Menu[] mnuTreeMenuPtr = new Menu[1];

    final Menu[] mnuCoarseMenuPtr = new Menu[1];
    final Menu[] mnuMediumMenuPtr = new Menu[1];
    final Menu[] mnuFineMenuPtr = new Menu[1];

    final CTabFolder[] tabFolderOpenDatFilesPtr = new CTabFolder[1];
    final NButton[] btnSyncTabsPtr = new NButton[1];
    final Composite[] cmpSyncAndReviewPtr = new Composite[1];

    private Composite cmpNorth;
    private Composite cmpEast;
    private Composite cmpWest;
    private static Composite status;

    final NButton[] btnSyncPtr = new NButton[1];
    final NButton[] btnLastOpenPtr = new NButton[1];
    final NButton[] btnNewPtr = new NButton[1];
    final NButton[] btnOpenPtr = new NButton[1];
    final NButton[] btnSavePtr = new NButton[1];
    final NButton[] btnSaveAllPtr = new NButton[1];
    final NButton[] btnSelectPtr = new NButton[1];
    final NButton[] btnMovePtr = new NButton[1];
    final NButton[] btnRotatePtr = new NButton[1];
    final NButton[] btnScalePtr = new NButton[1];
    final NButton[] btnCombinedPtr = new NButton[1];

    final NButton[] btnLocalPtr = new NButton[1];
    final NButton[] btnGlobalPtr = new NButton[1];

    final NButton[] btnUndoPtr = new NButton[1];
    final NButton[] btnAddHistoryPtr = new NButton[1];
    final NButton[] btnRedoPtr = new NButton[1];

    final NButton[] btnManipulatorToOriginPtr = new NButton[1];
    final NButton[] btnManipulatorXReversePtr = new NButton[1];
    final NButton[] btnManipulatorYReversePtr = new NButton[1];
    final NButton[] btnManipulatorZReversePtr = new NButton[1];
    final NButton[] btnManipulatorToWorldPtr = new NButton[1];
    final NButton[] btnManipulatorCameraToPosPtr = new NButton[1];
    final NButton[] btnManipulatorToAveragePtr = new NButton[1];
    final NButton[] btnManipulatorToSubfilePtr = new NButton[1];
    final NButton[] btnManipulatorSubfileToPtr = new NButton[1];
    final NButton[] btnManipulatorToVertexPtr = new NButton[1];
    final NButton[] btnManipulatorToEdgePtr = new NButton[1];
    final NButton[] btnManipulatorToSurfacePtr = new NButton[1];
    final NButton[] btnManipulatorToVertexNormalPtr = new NButton[1];
    final NButton[] btnManipulatorToEdgeNormalPtr = new NButton[1];
    final NButton[] btnManipulatorToSurfaceNormalPtr = new NButton[1];
    final NButton[] btnManipulatorAdjustRotationCenterPtr = new NButton[1];
    final NButton[] btnManipulatorToVertexPositionPtr = new NButton[1];
    final NButton[] btnManipulatorSwitchXYPtr = new NButton[1];
    final NButton[] btnManipulatorSwitchXZPtr = new NButton[1];
    final NButton[] btnManipulatorSwitchYZPtr = new NButton[1];

    final MenuItem[] mntmManipulatorToOriginPtr = new MenuItem[1];
    final MenuItem[] mntmManipulatorXReversePtr = new MenuItem[1];
    final MenuItem[] mntmManipulatorYReversePtr = new MenuItem[1];
    final MenuItem[] mntmManipulatorZReversePtr = new MenuItem[1];
    final MenuItem[] mntmManipulatorToWorldPtr = new MenuItem[1];
    final MenuItem[] mntmManipulatorCameraToPosPtr = new MenuItem[1];
    final MenuItem[] mntmManipulatorToAveragePtr = new MenuItem[1];
    final MenuItem[] mntmManipulatorToSubfilePtr = new MenuItem[1];
    final MenuItem[] mntmManipulatorSubfileToPtr = new MenuItem[1];
    final MenuItem[] mntmManipulatorToVertexPtr = new MenuItem[1];
    final MenuItem[] mntmManipulatorToEdgePtr = new MenuItem[1];
    final MenuItem[] mntmManipulatorToSurfacePtr = new MenuItem[1];
    final MenuItem[] mntmManipulatorToVertexNormalPtr = new MenuItem[1];
    final MenuItem[] mntmManipulatorToEdgeNormalPtr = new MenuItem[1];
    final MenuItem[] mntmManipulatorToSurfaceNormalPtr = new MenuItem[1];
    final MenuItem[] mntmManipulatorAdjustRotationCenterPtr = new MenuItem[1];
    final MenuItem[] mntmManipulatorToVertexPositionPtr = new MenuItem[1];
    final MenuItem[] mntmManipulatorSwitchXYPtr = new MenuItem[1];
    final MenuItem[] mntmManipulatorSwitchXZPtr = new MenuItem[1];
    final MenuItem[] mntmManipulatorSwitchYZPtr = new MenuItem[1];

    final NButton[] btnLastUsedColourPtr = new NButton[1];
    final NButton[] btnPipettePtr = new NButton[1];
    final NButton[] btnDecolourPtr = new NButton[1];
    final NButton[] btnPalettePtr = new NButton[1];

    final NButton[] btnMoveAdjacentDataPtr = new NButton[1];
    final NButton[] btnCompileSubfilePtr = new NButton[1];
    final NButton[] btnSplitQuadPtr = new NButton[1];
    final NButton[] btnMergeQuadPtr = new NButton[1];
    final NButton[] btnLineIntersectionPtr = new NButton[1];
    final NButton[] btnCondlineToLinePtr = new NButton[1];
    final NButton[] btnLineToCondlinePtr = new NButton[1];
    final NButton[] btnMoveOnLinePtr = new NButton[1];
    final NButton[] btnRoundSelectionPtr = new NButton[1];
    final NButton[] btnShowSelectionInTextEditorPtr = new NButton[1];
    final NButton[] btnBFCswapPtr = new NButton[1];
    final NButton[] btnVerticesPtr = new NButton[1];
    final NButton[] btnTrisNQuadsPtr = new NButton[1];
    final NButton[] btnLinesPtr = new NButton[1];
    final NButton[] btnSubfilesPtr = new NButton[1];
    final NButton[] btnInsertAtCursorPositionPtr = new NButton[1];
    final NButton[] btnAddCommentPtr = new NButton[1];
    final NButton[] btnAddVertexPtr = new NButton[1];
    final NButton[] btnAddPrimitivePtr = new NButton[1];
    final NButton[] btnAddLinePtr = new NButton[1];
    final NButton[] btnAddTrianglePtr = new NButton[1];
    final NButton[] btnAddQuadPtr = new NButton[1];
    final NButton[] btnAddCondlinePtr = new NButton[1];
    final NButton[] btnAddDistancePtr = new NButton[1];
    final NButton[] btnAddProtractorPtr = new NButton[1];

    final MenuItem[] mntmGridCoarseDefaultPtr = new MenuItem[1];
    final MenuItem[] mntmGridMediumDefaultPtr = new MenuItem[1];
    final MenuItem[] mntmGridFineDefaultPtr = new MenuItem[1];

    final MenuItem[] mntmOpenIn3DEditorPtr = new MenuItem[1];
    final MenuItem[] mntmOpenInTextEditorPtr = new MenuItem[1];
    final MenuItem[] mntmRenamePtr = new MenuItem[1];
    final MenuItem[] mntmRevertPtr = new MenuItem[1];
    final MenuItem[] mntmClosePtr = new MenuItem[1];
    final MenuItem[] mntmCopyToUnofficialPtr = new MenuItem[1];

    final NButton[] btnOpenIn3DEditorPtr = new NButton[1];
    final NButton[] btnOpenInTextEditorPtr = new NButton[1];
    final NButton[] btnRenamePtr = new NButton[1];
    final NButton[] btnRevertPtr = new NButton[1];
    final NButton[] btnClosePtr = new NButton[1];
    final NButton[] btnCopyToUnofficialPtr = new NButton[1];

    private final NButton[] btnSelect2Ptr = new NButton[1];

    final MenuItem[] mntmSelectAllPtr = new MenuItem[1];
    final MenuItem[] mntmSelectAllWithColoursPtr = new MenuItem[1];
    final MenuItem[] mntmSelectAllVisiblePtr = new MenuItem[1];
    final MenuItem[] mntmSelectAllVisibleWithColoursPtr = new MenuItem[1];
    final MenuItem[] mntmSelectNonePtr = new MenuItem[1];
    final MenuItem[] mntmSelectInversePtr = new MenuItem[1];
    final MenuItem[] mntmSelectEverythingPtr = new MenuItem[1];
    final MenuItem[] mntmSelectConnectedPtr = new MenuItem[1];
    final MenuItem[] mntmSelectTouchingPtr = new MenuItem[1];
    final MenuItem[] mntmWithSameOrientationPtr = new MenuItem[1];
    final MenuItem[] mntmWithAccuracyPtr = new MenuItem[1];
    final MenuItem[] mntmWithAdjacencyPtr = new MenuItem[1];
    final MenuItem[] mntmWithWholeSubfilesPtr = new MenuItem[1];
    final MenuItem[] mntmWithSameColourPtr = new MenuItem[1];
    final MenuItem[] mntmWithSameTypePtr = new MenuItem[1];
    final MenuItem[] mntmWithHiddenDataPtr = new MenuItem[1];
    final MenuItem[] mntmExceptSubfilesPtr = new MenuItem[1];
    final MenuItem[] mntmStopAtEdgesPtr = new MenuItem[1];
    final MenuItem[] mntmSelectIsolatedVerticesPtr = new MenuItem[1];
    final MenuItem[] mntmSelectSingleVertexPtr = new MenuItem[1];

    final MenuItem[] mntmPartReviewPtr = new MenuItem[1];
    final MenuItem[] mntmEdger2Ptr = new MenuItem[1];
    final MenuItem[] mntmTxt2DatPtr = new MenuItem[1];
    final MenuItem[] mntmPrimGen2Ptr = new MenuItem[1];
    final MenuItem[] mntmRectifierPtr = new MenuItem[1];
    final MenuItem[] mntmIsecalcPtr = new MenuItem[1];
    final MenuItem[] mntmSlicerProPtr = new MenuItem[1];
    final MenuItem[] mntmIntersectorPtr = new MenuItem[1];
    final MenuItem[] mntmSlantingMatrixProjectorPtr = new MenuItem[1];
    final MenuItem[] mntmLines2PatternPtr = new MenuItem[1];
    final MenuItem[] mntmPathTruderPtr = new MenuItem[1];
    final MenuItem[] mntmYTruderPtr = new MenuItem[1];
    final MenuItem[] mntmSymSplitterPtr = new MenuItem[1];
    final MenuItem[] mntmUnificatorPtr = new MenuItem[1];
    final MenuItem[] mntmRingsAndConesPtr = new MenuItem[1];
    final MenuItem[] mntmTJunctionFinderPtr = new MenuItem[1];
    final MenuItem[] mntmMeshReducerPtr = new MenuItem[1];

    final MenuItem[] mntmOptionsPtr = new MenuItem[1];
    final MenuItem[] mntmUserConfigLoadPtr = new MenuItem[1];
    final MenuItem[] mntmUserConfigSavePtr = new MenuItem[1];
    final MenuItem[] mntmResetSettingsOnRestartPtr = new MenuItem[1];
    final MenuItem[] mntmSelectAnotherLDConfigPtr = new MenuItem[1];
    final MenuItem[] mntmDownloadLDConfigPtr = new MenuItem[1];
    final MenuItem[] mntmDownloadCategoriesPtr = new MenuItem[1];

    final MenuItem[] mntmLoadPalettePtr = new MenuItem[1];
    final MenuItem[] mntmResetPalettePtr = new MenuItem[1];
    final MenuItem[] mntmSavePalettePtr = new MenuItem[1];
    final MenuItem[] mntmSetPaletteSizePtr = new MenuItem[1];

    final MenuItem[] mntmUploadLogsPtr = new MenuItem[1];
    final MenuItem[] mntmAntiAliasingPtr = new MenuItem[1];
    final MenuItem[] mntmOpenGL33EnginePtr = new MenuItem[1];
    final MenuItem[] mntmVulkanEnginePtr = new MenuItem[1];
    final MenuItem[] mntmSyncLpeInlinePtr = new MenuItem[1];

    final MenuItem[] mntmFlipPtr = new MenuItem[1];
    final MenuItem[] mntmSmoothPtr = new MenuItem[1];
    final MenuItem[] mntmSubdivideCatmullClarkPtr = new MenuItem[1];
    final MenuItem[] mntmSubdivideLoopPtr = new MenuItem[1];
    final MenuItem[] mntmSplitPtr = new MenuItem[1];
    final MenuItem[] mntmSplitNTimesPtr = new MenuItem[1];

    final MenuItem[] mntmMergeToAveragePtr = new MenuItem[1];
    final MenuItem[] mntmMergeToLastSelectedPtr = new MenuItem[1];
    final MenuItem[] mntmMergeToNearestVertexPtr = new MenuItem[1];
    final MenuItem[] mntmMergeToNearestEdgePtr = new MenuItem[1];
    final MenuItem[] mntmMergeToNearestEdgeSplitPtr = new MenuItem[1];
    final MenuItem[] mntmMergeToNearestFacePtr = new MenuItem[1];

    final MenuItem[] mntmMergeToNearestFaceDirPtr = new MenuItem[1];

    final MenuItem[] mntmSetXYZPtr = new MenuItem[1];
    final MenuItem[] mntmTranslatePtr = new MenuItem[1];
    final MenuItem[] mntmRotatePtr = new MenuItem[1];
    final MenuItem[] mntmScalePtr = new MenuItem[1];

    final Text[] txtSearchPtr = new Text[1];
    final NButton[] btnResetSearchPtr = new NButton[1];
    final CTabFolder[] tabFolderSettingsPtr = new CTabFolder[1];

    final NButton[] btnShowUpper1Ptr = new NButton[1];
    final NButton[] btnShowUpper2Ptr = new NButton[1];
    final NButton[] btnShowUpper3Ptr = new NButton[1];
    final NButton[] btnShowMiddlePtr = new NButton[1];
    final NButton[] btnShowLowerPtr = new NButton[1];
    final NButton[] btnSameHeightPtr = new NButton[1];
    final NButton[] btnShowLeftPtr = new NButton[1];
    final NButton[] btnShowRightPtr = new NButton[1];
    final NButton[] btnSameWidthPtr = new NButton[1];

    final NButton[] btnHidePtr = new NButton[1];
    final NButton[] btnShowAllPtr = new NButton[1];
    final NButton[] btnNoTransparentSelectionPtr = new NButton[1];
    final NButton[] btnBFCTogglePtr = new NButton[1];

    final NButton[] btnDeletePtr = new NButton[1];
    final NButton[] btnCopyPtr = new NButton[1];
    final NButton[] btnCutPtr = new NButton[1];
    final NButton[] btnPastePtr = new NButton[1];

    final NButton[] btnCoarsePtr = new NButton[1];
    final NButton[] btnMediumPtr = new NButton[1];
    final NButton[] btnFinePtr = new NButton[1];

    final BigDecimalSpinner[] spnMovePtr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnRotatePtr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnScalePtr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnScaleInitialPtr = new BigDecimalSpinner[1];

    final TreeItem[] treeItemProjectPtr = new TreeItem[1];
    final TreeItem[] treeItemUnsavedPtr = new TreeItem[1];
    final TreeItem[] treeItemUnofficialPtr = new TreeItem[1];
    final TreeItem[] treeItemOfficialPtr = new TreeItem[1];

    final TreeItem[] treeItemProjectPartsPtr = new TreeItem[1];
    final TreeItem[] treeItemProjectSubpartsPtr = new TreeItem[1];
    final TreeItem[] treeItemProjectPrimitivesPtr = new TreeItem[1];
    final TreeItem[] treeItemProjectPrimitives8Ptr = new TreeItem[1];
    final TreeItem[] treeItemProjectPrimitives48Ptr = new TreeItem[1];
    final TreeItem[] treeItemUnofficialPartsPtr = new TreeItem[1];
    final TreeItem[] treeItemUnofficialSubpartsPtr = new TreeItem[1];
    final TreeItem[] treeItemUnofficialPrimitivesPtr = new TreeItem[1];
    final TreeItem[] treeItemUnofficialPrimitives8Ptr = new TreeItem[1];
    final TreeItem[] treeItemUnofficialPrimitives48Ptr = new TreeItem[1];
    final TreeItem[] treeItemOfficialPartsPtr = new TreeItem[1];
    final TreeItem[] treeItemOfficialSubpartsPtr = new TreeItem[1];
    final TreeItem[] treeItemOfficialPrimitivesPtr = new TreeItem[1];
    final TreeItem[] treeItemOfficialPrimitives8Ptr = new TreeItem[1];
    final TreeItem[] treeItemOfficialPrimitives48Ptr = new TreeItem[1];
    final Tree[] treeParts = new Tree[1];

    final NButton[] btnPngPreviousPtr = new NButton[1];
    final NButton[] btnPngFocusPtr = new NButton[1];
    final NButton[] btnPngNextPtr = new NButton[1];
    final NButton[] btnPngImagePtr = new NButton[1];
    final Text[] txtPngPathPtr = new Text[1];
    final BigDecimalSpinner[] spnPngXPtr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnPngYPtr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnPngZPtr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnPngA1Ptr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnPngA2Ptr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnPngA3Ptr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnPngSXPtr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnPngSYPtr = new BigDecimalSpinner[1];

    final NButton[] btnCloseViewPtr = new NButton[1];

    final NButton[] btnToggleLinesOpenGLPtr = new NButton[1];
    final NButton[] btnLineSize0Ptr = new NButton[1];
    final NButton[] btnLineSize1Ptr = new NButton[1];
    final NButton[] btnLineSize2Ptr = new NButton[1];
    final NButton[] btnLineSize3Ptr = new NButton[1];
    final NButton[] btnLineSize4Ptr = new NButton[1];

    final NButton[] btnPerspectiveFrontPtr = new NButton[1];
    final NButton[] btnPerspectiveBackPtr = new NButton[1];
    final NButton[] btnPerspectiveTopPtr = new NButton[1];
    final NButton[] btnPerspectiveBottomPtr = new NButton[1];
    final NButton[] btnPerspectiveLeftPtr = new NButton[1];
    final NButton[] btnPerspectiveRightPtr = new NButton[1];
    final NButton[] btnPerspectiveTwoThirdsPtr = new NButton[1];

    final NButton[] btnRenderModeNoBackfaceCullingPtr = new NButton[1];
    final NButton[] btnRenderModeRandomColoursPtr = new NButton[1];
    final NButton[] btnRenderModeGreenRedPtr = new NButton[1];
    final NButton[] btnRenderModeRedBackfacesPtr = new NButton[1];
    final NButton[] btnRenderModeRealBackfaceCullingPtr = new NButton[1];
    final NButton[] btnRenderModeLDrawStandardPtr = new NButton[1];
    final NButton[] btnRenderModeCoplanarityModePtr = new NButton[1];
    final NButton[] btnRenderModeCondlineModePtr = new NButton[1];
    final NButton[] btnRenderModeWireframePtr = new NButton[1];

    final NButton[] btnNewDatPtr = new NButton[1];
    final NButton[] btnOpenDatPtr = new NButton[1];
    final NButton[] btnSaveDatPtr = new NButton[1];
    final NButton[] btnSaveAsDatPtr = new NButton[1];

    final NButton[] btnPreviousSelectionPtr = new NButton[1];
    final NButton[] btnNextSelectionPtr = new NButton[1];
    final Text[] txtLinePtr = new Text[1];
    final NButton[] btnMoveAdjacentData2Ptr = new NButton[1];

    final BigDecimalSpinner[] spnSelectionAnglePtr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnSelectionLengthPtr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnSelectionX1Ptr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnSelectionY1Ptr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnSelectionZ1Ptr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnSelectionX2Ptr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnSelectionY2Ptr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnSelectionZ2Ptr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnSelectionX3Ptr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnSelectionY3Ptr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnSelectionZ3Ptr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnSelectionX4Ptr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnSelectionY4Ptr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnSelectionZ4Ptr = new BigDecimalSpinner[1];
    final Label[] lblSelectionAnglePtr = new Label[1];
    final Label[] lblSelectionLengthPtr = new Label[1];
    final Label[] lblSelectionX1Ptr = new Label[1];
    final Label[] lblSelectionY1Ptr = new Label[1];
    final Label[] lblSelectionZ1Ptr = new Label[1];
    final Label[] lblSelectionX2Ptr = new Label[1];
    final Label[] lblSelectionY2Ptr = new Label[1];
    final Label[] lblSelectionZ2Ptr = new Label[1];
    final Label[] lblSelectionX3Ptr = new Label[1];
    final Label[] lblSelectionY3Ptr = new Label[1];
    final Label[] lblSelectionZ3Ptr = new Label[1];
    final Label[] lblSelectionX4Ptr = new Label[1];
    final Label[] lblSelectionY4Ptr = new Label[1];
    final Label[] lblSelectionZ4Ptr = new Label[1];
    final Label[] lblSelectedPrimitiveItemPtr = new Label[1];

    final CompositePrimitive[] cmpPrimitivesPtr = new CompositePrimitive[1];
    final Text[] txtPrimitiveSearchPtr = new Text[1];
    final NButton[] btnResetPrimitiveSearchPtr = new NButton[1];

    final NButton[] btnZoomOutPrimitivesPtr = new NButton[1];
    final NButton[] btnZoomInPrimitivesPtr = new NButton[1];

    final MenuItem[] mntmSAllTypesPtr = new MenuItem[1];
    final MenuItem[] mntmSTrianglesPtr = new MenuItem[1];
    final MenuItem[] mntmSQuadsPtr = new MenuItem[1];
    final MenuItem[] mntmSCLinesPtr = new MenuItem[1];
    final MenuItem[] mntmSVerticesPtr = new MenuItem[1];
    final MenuItem[] mntmSLinesPtr = new MenuItem[1];
    final MenuItem[] mntmSNothingPtr = new MenuItem[1];


    final MenuItem[] mntmIconSize1Ptr = new MenuItem[1];
    final MenuItem[] mntmIconSize2Ptr = new MenuItem[1];
    final MenuItem[] mntmIconSize3Ptr = new MenuItem[1];
    final MenuItem[] mntmIconSize4Ptr = new MenuItem[1];
    final MenuItem[] mntmIconSize5Ptr = new MenuItem[1];
    final MenuItem[] mntmIconSize6Ptr = new MenuItem[1];

    ToolItem toolItemColourBar;

    private static SashForm sashForm;
    final SashForm[] editorSashForm = new SashForm[]{null};
    final SashForm[] leftSash = new SashForm[1];
    final SashForm[] splitSash = new SashForm[1];

    static final int TEXT_3D_SEPARATE = 0;
    private static final int TEXT_LEFT_3D_RIGHT = 1;
    private static final int TEXT_RIGHT_3D_LEFT = 2;

    /**
     * Create the application window.
     */
    public Editor3DDesign() {
        super(null);
        addToolBar(SWT.FLAT | SWT.WRAP);
    }

    /**
     * Create contents of the application window.
     *
     * @param parent
     */
    @Override
    protected Control createContents(Composite parent) {
        final Editor3DWindowState windowState = WorkbenchManager.getEditor3DWindowState();
        final UserSettingState userSettings = WorkbenchManager.getUserSettingState();

        userSettings.setRoundX(true);
        userSettings.setRoundY(true);
        userSettings.setRoundZ(true);

        setStatus(I18n.E3D_READY_STATUS);
        Composite containerTop = new Composite(parent, Cocoa.getStyle());
        containerTop.setLayout(new BorderLayout(0, 0));

        if (userSettings.getTextWinArr() != TEXT_3D_SEPARATE) {
            containerTop = new SashForm(containerTop, SWT.BORDER);
            splitSash[0] = (SashForm) containerTop;
            if (userSettings.getTextWinArr() == TEXT_LEFT_3D_RIGHT) {
                Composite containerTextEditor = new Composite(containerTop, Cocoa.getStyle());
                containerTextEditor.setLayout(new BorderLayout(0, 0));
                EditorTextWindow tWin = new EditorTextWindow(containerTextEditor, this);
                tWin.build();
                tWin.getTabFolder().setWindow(this);
                tWin.registerEvents();
                Project.getOpenTextWindows().add(tWin);
            }
        }

        Composite container = new Composite(containerTop, Cocoa.getStyle());
        container.setLayout(new BorderLayout(0, 0));
        {
            cmpNorth = new Composite(container, Cocoa.getStyle());
            cmpNorth.setLayoutData(BorderLayout.NORTH);
            RowLayout rlRoolBar = new RowLayout(SWT.HORIZONTAL);
            rlRoolBar.center = true;
            cmpNorth.setLayout(rlRoolBar);
        }
        {
            cmpEast = new Composite(container, Cocoa.getStyle());
            cmpEast.setLayoutData(BorderLayout.EAST);
            RowLayout rlRoolBar = new RowLayout(SWT.VERTICAL);
            rlRoolBar.center = true;
            cmpEast.setLayout(rlRoolBar);
        }
        {
            cmpWest = new Composite(container, Cocoa.getStyle());
            cmpWest.setLayoutData(BorderLayout.WEST);
            RowLayout rlRoolBar = new RowLayout(SWT.VERTICAL);
            rlRoolBar.center = true;
            cmpWest.setLayout(rlRoolBar);
        }

        if (userSettings.getTextWinArr() != TEXT_3D_SEPARATE) {
            ToolItem toolItemSashResize = new ToolItem(cmpNorth, Cocoa.getStyle(), true);
            if (userSettings.getTextWinArr() == TEXT_RIGHT_3D_LEFT) {
                NButton btnShowLeft = new NButton(toolItemSashResize, Cocoa.getStyle());
                this.btnShowLeftPtr[0] = btnShowLeft;
                btnShowLeft.setToolTipText(I18n.E3D_SASH_LEFT);
                btnShowLeft.setImage(ResourceManager.getImage("icon16_leftSash.png")); //$NON-NLS-1$
            } else {
                NButton btnSameWidth = new NButton(toolItemSashResize, Cocoa.getStyle());
                this.btnSameWidthPtr[0] = btnSameWidth;
                btnSameWidth.setToolTipText(I18n.E3D_SASH_SAME_WIDTH);
                btnSameWidth.setImage(ResourceManager.getImage("icon16_sameWidth.png")); //$NON-NLS-1$
                NButton btnShowRight = new NButton(toolItemSashResize, Cocoa.getStyle());
                this.btnShowRightPtr[0] = btnShowRight;
                btnShowRight.setToolTipText(I18n.E3D_SASH_RIGHT);
                btnShowRight.setImage(ResourceManager.getImage("icon16_rightSash.png")); //$NON-NLS-1$
            }
        }

        HashSet<String> missingItemsToCreate = new HashSet<>();
        missingItemsToCreate.add("SYNC_AND_RECENT_FILES"); //$NON-NLS-1$
        missingItemsToCreate.add("PROJECT_MANAGEMENT"); //$NON-NLS-1$
        missingItemsToCreate.add("OPEN_SAVE_DATFILE"); //$NON-NLS-1$
        missingItemsToCreate.add("SHOW_AND_HIDE"); //$NON-NLS-1$
        missingItemsToCreate.add("MISC_TOGGLE"); //$NON-NLS-1$
        missingItemsToCreate.add("UNDO_REDO"); //$NON-NLS-1$
        missingItemsToCreate.add("TRANSFORMATION_MODE"); //$NON-NLS-1$
        missingItemsToCreate.add("MANIPULATOR_MODE"); //$NON-NLS-1$
        missingItemsToCreate.add("MANIPULATOR_ACTIONS"); //$NON-NLS-1$
        missingItemsToCreate.add("MISC_CLICK"); //$NON-NLS-1$
        missingItemsToCreate.add("CUT_COPY_PASTE_DELETE"); //$NON-NLS-1$
        missingItemsToCreate.add("OBJECT_MODE"); //$NON-NLS-1$
        missingItemsToCreate.add("INSERT_AT_CURSOR"); //$NON-NLS-1$
        missingItemsToCreate.add("ADD_SOMETHING"); //$NON-NLS-1$
        missingItemsToCreate.add("COLOUR_BAR"); //$NON-NLS-1$
        missingItemsToCreate.add("LINE_THICKNESS"); //$NON-NLS-1$
        missingItemsToCreate.add("CLOSE_VIEW"); //$NON-NLS-1$
        missingItemsToCreate.add("CHANGE_PERSPECTIVE"); //$NON-NLS-1$
        missingItemsToCreate.add("RENDER_MODE"); //$NON-NLS-1$

        ToolItem lastToolItem = null;
        for (ToolItemState s : userSettings.getToolItemConfig3D()) {
            String obj = s.getKey();
            if (obj.equals("SYNC_AND_RECENT_FILES")) { //$NON-NLS-1$
                lastToolItem = createToolItemSync(s.getDrawLocation(), s.getDrawMode()); // SYNC_AND_RECENT_FILES
                missingItemsToCreate.remove(obj);
            }
            if (obj.equals("PROJECT_MANAGEMENT")) { //$NON-NLS-1$
                lastToolItem = createToolItemNewOpenSave(s.getDrawLocation(), s.getDrawMode()); // PROJECT_MANAGEMENT
                missingItemsToCreate.remove(obj);
            }
            if (obj.equals("OPEN_SAVE_DATFILE")) { //$NON-NLS-1$
                lastToolItem = createToolItemNewOpenDat(s.getDrawLocation(), s.getDrawMode()); // OPEN_SAVE_DATFILE
                missingItemsToCreate.remove(obj);
            }
            if (obj.equals("SHOW_AND_HIDE")) { //$NON-NLS-1$
                lastToolItem = createToolItemHideUnhide(s.getDrawLocation(), s.getDrawMode()); // SHOW_AND_HIDE
                missingItemsToCreate.remove(obj);
            }
            if (obj.equals("MISC_TOGGLE")) { //$NON-NLS-1$
                lastToolItem = createToolItemMiscToggle(s.getDrawLocation(), s.getDrawMode()); // MISC_TOGGLE
                missingItemsToCreate.remove(obj);
            }
            if (obj.equals("UNDO_REDO")) { //$NON-NLS-1$
                lastToolItem = createToolItemUndoRedo(s.getDrawLocation(), s.getDrawMode()); // UNDO_REDO
                missingItemsToCreate.remove(obj);
            }
            if (obj.equals("TRANSFORMATION_MODE")) { //$NON-NLS-1$
                lastToolItem = createToolItemTransformationMode(s.getDrawLocation(), s.getDrawMode()); // TRANSFORMATION_MODE
                missingItemsToCreate.remove(obj);
            }
            if (obj.equals("MANIPULATOR_MODE")) { //$NON-NLS-1$
                lastToolItem = createToolItemManipulatorMode(s.getDrawLocation(), s.getDrawMode()); // MANIPULATOR_MODE
                missingItemsToCreate.remove(obj);
            }
            if (obj.equals("MANIPULATOR_ACTIONS")) { //$NON-NLS-1$
                lastToolItem = createToolItemManipulatorActions(s.getDrawLocation(), s.getDrawMode(), lastToolItem); // MANIPULATOR_ACTIONS
                missingItemsToCreate.remove(obj);
            }
            if (obj.equals("MISC_CLICK")) { //$NON-NLS-1$
                lastToolItem = createToolItemMiscClick(); // MISC_CLICK
                missingItemsToCreate.remove(obj);
            }
            if (obj.equals("CUT_COPY_PASTE_DELETE")) { //$NON-NLS-1$
                lastToolItem = createToolItemCCPD(s.getDrawLocation(), s.getDrawMode()); // CUT_COPY_PASTE_DELETE
                missingItemsToCreate.remove(obj);
            }
            if (obj.equals("OBJECT_MODE")) { //$NON-NLS-1$
                lastToolItem = createToolItemMode(s.getDrawLocation(), s.getDrawMode()); // OBJECT_MODE
                missingItemsToCreate.remove(obj);
            }
            if (obj.equals("INSERT_AT_CURSOR")) { //$NON-NLS-1$
                lastToolItem = createToolItemInsertAtCursorPosition(s.getDrawLocation(), s.getDrawMode()); // INSERT_AT_CURSOR
                missingItemsToCreate.remove(obj);
            }
            if (obj.equals("ADD_SOMETHING")) { //$NON-NLS-1$
                lastToolItem = createToolItemAdd(s.getDrawLocation(), s.getDrawMode()); // ADD_SOMETHING
                missingItemsToCreate.remove(obj);
            }
            if (obj.equals("COLOUR_BAR")) { //$NON-NLS-1$
                lastToolItem = createToolItemColours(s.getDrawLocation(), s.getDrawMode()); // COLOUR_BAR
                missingItemsToCreate.remove(obj);
            }
            if (obj.equals("LINE_THICKNESS")) { //$NON-NLS-1$
                lastToolItem = createToolItemLineThickness(s.getDrawLocation(), s.getDrawMode()); // LINE_THICKNESS
                missingItemsToCreate.remove(obj);
            }
            if (obj.equals("CLOSE_VIEW")) { //$NON-NLS-1$
                lastToolItem = createToolItemCloseView(s.getDrawLocation(), s.getDrawMode()); // LINE_THICKNESS
                missingItemsToCreate.remove(obj);
            }
            if (obj.equals("CHANGE_PERSPECTIVE")) { //$NON-NLS-1$
                lastToolItem = createToolItemPerspective(s.getDrawLocation(), s.getDrawMode()); // LINE_THICKNESS
                missingItemsToCreate.remove(obj);
            }
            if (obj.equals("RENDER_MODE")) { //$NON-NLS-1$
                lastToolItem = createToolItemRenderMode(s.getDrawLocation(), s.getDrawMode()); // LINE_THICKNESS
                missingItemsToCreate.remove(obj);
            }
        }

        if (missingItemsToCreate.contains("SYNC_AND_RECENT_FILES")) lastToolItem = createToolItemSync(ToolItemDrawLocation.WEST, ToolItemDrawMode.VERTICAL); // SYNC_AND_RECENT_FILES //$NON-NLS-1$
        if (missingItemsToCreate.contains("PROJECT_MANAGEMENT")) lastToolItem = createToolItemNewOpenSave(ToolItemDrawLocation.WEST, ToolItemDrawMode.VERTICAL); // PROJECT_MANAGEMENT //$NON-NLS-1$
        if (missingItemsToCreate.contains("CHANGE_PERSPECTIVE")) lastToolItem = createToolItemPerspective(ToolItemDrawLocation.WEST, ToolItemDrawMode.VERTICAL); // CHANGE_PERSPECTIVE //$NON-NLS-1$
        if (missingItemsToCreate.contains("RENDER_MODE")) lastToolItem = createToolItemRenderMode(ToolItemDrawLocation.WEST, ToolItemDrawMode.VERTICAL); // RENDER_MODE //$NON-NLS-1$
        if (missingItemsToCreate.contains("MANIPULATOR_ACTIONS")) lastToolItem = createToolItemManipulatorActions(ToolItemDrawLocation.WEST, ToolItemDrawMode.VERTICAL, lastToolItem); // MANIPULATOR_ACTIONS //$NON-NLS-1$
        if (missingItemsToCreate.contains("MANIPULATOR_MODE")) lastToolItem = createToolItemManipulatorMode(ToolItemDrawLocation.WEST, ToolItemDrawMode.VERTICAL); // MANIPULATOR_MODE //$NON-NLS-1$
        if (missingItemsToCreate.contains("MISC_TOGGLE")) lastToolItem = createToolItemMiscToggle(ToolItemDrawLocation.NORTH, ToolItemDrawMode.HORIZONTAL); // MISC_TOGGLE //$NON-NLS-1$
        if (missingItemsToCreate.contains("OPEN_SAVE_DATFILE")) lastToolItem = createToolItemNewOpenDat(ToolItemDrawLocation.NORTH, ToolItemDrawMode.HORIZONTAL); // OPEN_SAVE_DATFILE //$NON-NLS-1$
        if (missingItemsToCreate.contains("SHOW_AND_HIDE")) lastToolItem = createToolItemHideUnhide(ToolItemDrawLocation.NORTH, ToolItemDrawMode.HORIZONTAL); // SHOW_AND_HIDE //$NON-NLS-1$
        if (missingItemsToCreate.contains("UNDO_REDO")) lastToolItem = createToolItemUndoRedo(ToolItemDrawLocation.NORTH, ToolItemDrawMode.HORIZONTAL); // UNDO_REDO //$NON-NLS-1$
        if (missingItemsToCreate.contains("TRANSFORMATION_MODE")) lastToolItem = createToolItemTransformationMode(ToolItemDrawLocation.NORTH, ToolItemDrawMode.HORIZONTAL); // TRANSFORMATION_MODE //$NON-NLS-1$
        if (missingItemsToCreate.contains("OBJECT_MODE")) lastToolItem = createToolItemMode(ToolItemDrawLocation.NORTH, ToolItemDrawMode.HORIZONTAL); // OBJECT_MODE //$NON-NLS-1$
        if (missingItemsToCreate.contains("CUT_COPY_PASTE_DELETE")) lastToolItem = createToolItemCCPD(ToolItemDrawLocation.NORTH, ToolItemDrawMode.HORIZONTAL); // CUT_COPY_PASTE_DELETE //$NON-NLS-1$
        if (missingItemsToCreate.contains("MISC_CLICK")) lastToolItem = createToolItemMiscClick(); // MISC_CLICK //$NON-NLS-1$
        if (missingItemsToCreate.contains("INSERT_AT_CURSOR")) lastToolItem = createToolItemInsertAtCursorPosition(ToolItemDrawLocation.NORTH, ToolItemDrawMode.HORIZONTAL); // INSERT_AT_CURSOR //$NON-NLS-1$
        if (missingItemsToCreate.contains("ADD_SOMETHING")) lastToolItem = createToolItemAdd(ToolItemDrawLocation.NORTH, ToolItemDrawMode.HORIZONTAL); // ADD_SOMETHING //$NON-NLS-1$
        if (missingItemsToCreate.contains("CLOSE_VIEW")) lastToolItem = createToolItemCloseView(ToolItemDrawLocation.EAST, ToolItemDrawMode.VERTICAL); // CLOSE_VIEW //$NON-NLS-1$
        if (missingItemsToCreate.contains("COLOUR_BAR")) lastToolItem = createToolItemColours(ToolItemDrawLocation.EAST, ToolItemDrawMode.VERTICAL); // COLOUR_BAR //$NON-NLS-1$
        if (missingItemsToCreate.contains("LINE_THICKNESS")) lastToolItem = createToolItemLineThickness(ToolItemDrawLocation.EAST, ToolItemDrawMode.VERTICAL); // LINE_THICKNESS //$NON-NLS-1$

        {
            Composite cmpMainEditor = new Composite(container, SWT.BORDER);
            cmpMainEditor.setLayoutData(BorderLayout.CENTER);
            cmpMainEditor.setLayout(new GridLayout(2, false));
            {
                Composite cmpSyncAndReview = new Composite(cmpMainEditor, SWT.NONE);
                cmpSyncAndReview.setLayout(new GridLayout(2, false));
                this.cmpSyncAndReviewPtr[0] = cmpSyncAndReview;
                {
                    NButton btnSyncTabs = new NButton(cmpSyncAndReview, SWT.TOGGLE);
                    this.btnSyncTabsPtr[0] = btnSyncTabs;
                    btnSyncTabs.setToolTipText(I18n.E3D_SYNC_3D_EDITOR);
                    btnSyncTabs.setImage(ResourceManager.getImage("icon16_sync3D.png")); //$NON-NLS-1$

                    btnSyncTabs.setSelection(userSettings.isSyncingTabs());
                }
                {
                    GridData gridDataX = new GridData();
                    gridDataX.horizontalIndent = 10;
                    cmpSyncAndReview.setLayoutData(gridDataX);
                }

                CTabFolder tabFolderOpenDatFiles = new CTabFolder(cmpMainEditor, SWT.CLOSE);
                this.tabFolderOpenDatFilesPtr[0] = tabFolderOpenDatFiles;
                tabFolderOpenDatFiles.setMRUVisible(true);
                tabFolderOpenDatFiles.setSelectionBackground(new Color[]{Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND), Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND)}, new int[]{100}, true);

                {
                    GridData gridDataX = new GridData();

                    gridDataX.horizontalAlignment = SWT.FILL;
                    gridDataX.minimumHeight = 10;
                    gridDataX.minimumWidth = 160;
                    gridDataX.heightHint = tabFolderOpenDatFiles.getTabHeight() / 3;

                    gridDataX.verticalAlignment = SWT.FILL;

                    gridDataX.grabExcessHorizontalSpace = true;
                    tabFolderOpenDatFiles.setLayoutData(gridDataX);
                }

                {
                    CTabItem tItem = new CTabItem(tabFolderOpenDatFiles, Cocoa.getStyle());
                    tItem.setText(I18n.E3D_NO_FILE_SELECTED);
                }
                {
                    CTabItem tItem = new CTabItem(tabFolderOpenDatFiles, Cocoa.getStyle());
                    tItem.setText("new.dat*"); //$NON-NLS-1$
                }

                tabFolderOpenDatFiles.setSelection(1);

                SashForm sashForm = new SashForm(cmpMainEditor, Cocoa.getStyle());

                {
                    GridData gridDataX = new GridData();

                    gridDataX.horizontalSpan = 2;

                    gridDataX.horizontalAlignment = SWT.FILL;
                    gridDataX.minimumHeight = 200;
                    gridDataX.minimumWidth = 160;

                    gridDataX.verticalAlignment = SWT.FILL;
                    gridDataX.grabExcessVerticalSpace = true;

                    gridDataX.grabExcessHorizontalSpace = true;
                    sashForm.setLayoutData(gridDataX);
                }

                Editor3DDesign.setSashForm(sashForm);
                sashForm.setToolTipText(I18n.E3D_DRAG_HINT);
                {
                    SashForm sashForm2 = new SashForm(sashForm, SWT.VERTICAL);
                    this.leftSash[0] = sashForm2;
                    Composite cmpContainer1 = new Composite(sashForm2, SWT.BORDER);
                    GridLayout gridLayout = new GridLayout(1, true);
                    cmpContainer1.setLayout(gridLayout);
                    {
                        CTabFolder tabFolderSettings = new CTabFolder(cmpContainer1, SWT.BORDER);
                        this.tabFolderSettingsPtr[0] = tabFolderSettings;
                        tabFolderSettings.setMRUVisible(true);
                        tabFolderSettings.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
                        GridData gridData = new GridData();
                        gridData.horizontalAlignment = SWT.FILL;
                        gridData.minimumHeight = 200;
                        gridData.minimumWidth = 160;
                        gridData.heightHint = 200;

                        gridData.verticalAlignment = SWT.FILL;
                        gridData.grabExcessVerticalSpace = true;

                        gridData.grabExcessHorizontalSpace = true;
                        tabFolderSettings.setLayoutData(gridData);
                        tabFolderSettings.setSize(700, 500);

                        CTabItem tItem = new CTabItem(tabFolderSettings, Cocoa.getStyle());
                        tItem.setText("(1)"); //$NON-NLS-1$
                        tItem.setToolTipText(I18n.E3D_SNAPPING);
                        {
                            final ScrolledComposite cmpScroll = new ScrolledComposite(tabFolderSettings, SWT.V_SCROLL | SWT.H_SCROLL);
                            Composite cmpSnappingArea = new Composite(cmpScroll, Cocoa.getStyle());
                            tItem.setControl(cmpScroll);
                            cmpScroll.setContent(cmpSnappingArea);
                            cmpScroll.setExpandHorizontal(true);
                            cmpScroll.setExpandVertical(true);

                            cmpSnappingArea.setLayout(new GridLayout(3, false));
                            ((GridLayout) cmpSnappingArea.getLayout()).verticalSpacing = 0;
                            ((GridLayout) cmpSnappingArea.getLayout()).marginHeight = 0;
                            ((GridLayout) cmpSnappingArea.getLayout()).marginWidth = 0;

                            {
                                Label lblTabHeader = new Label(cmpSnappingArea, Cocoa.getStyle());
                                lblTabHeader.setText(I18n.E3D_SNAPPING);
                                lblTabHeader.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
                                NButton btnShowUpper = new NButton(cmpSnappingArea, Cocoa.getStyle());
                                this.btnShowUpper1Ptr[0] = btnShowUpper;
                                btnShowUpper.setImage(ResourceManager.getImage("icon16_upper.png")); //$NON-NLS-1$
                                btnShowUpper.setToolTipText(I18n.E3D_SASH_UPPER);
                                btnShowUpper.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 2, 1));
                            }

                            {
                                Composite cmpDummy = new Composite(cmpSnappingArea, Cocoa.getStyle());
                                cmpDummy.setLayout(new FillLayout(SWT.HORIZONTAL));

                                NButton btnCoarse = new NButton(cmpDummy, SWT.RADIO);
                                this.btnCoarsePtr[0] = btnCoarse;
                                btnCoarse.setImage(ResourceManager.getImage("icon8_coarse.png")); //$NON-NLS-1$
                                btnCoarse.setToolTipText(I18n.E3D_COARSE);

                                NButton btnMedium = new NButton(cmpDummy, SWT.RADIO);
                                this.btnMediumPtr[0] = btnMedium;
                                btnMedium.setSelection(true);
                                btnMedium.setImage(ResourceManager.getImage("icon8_medium.png")); //$NON-NLS-1$
                                btnMedium.setToolTipText(I18n.E3D_MEDIUM);

                                NButton btnFine = new NButton(cmpDummy, SWT.RADIO);
                                this.btnFinePtr[0] = btnFine;
                                btnFine.setImage(ResourceManager.getImage("icon8_fine.png")); //$NON-NLS-1$
                                btnFine.setToolTipText(I18n.E3D_FINE);

                                cmpDummy.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 3, 1));
                            }

                            {
                                Object[] messageArguments = {I18n.getCurrentUnit()};
                                MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                                formatter.setLocale(MyLanguage.locale);
                                formatter.applyPattern(I18n.E3D_MOVE_SNAP);

                                Label lblNewLabel = new Label(cmpSnappingArea, Cocoa.getStyle());
                                lblNewLabel.setText(formatter.format(messageArguments));
                                lblNewLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));

                            }

                            BigDecimalSpinner spinner = new BigDecimalSpinner(cmpSnappingArea, Cocoa.getStyle(), View.NUMBER_FORMAT8F);
                            this.spnMovePtr[0] = spinner;
                            spinner.setMaximum(new BigDecimal("100")); //$NON-NLS-1$
                            spinner.setMinimum(new BigDecimal("0.0001")); //$NON-NLS-1$
                            spinner.setValue(userSettings.getMediumMoveSnap());
                            spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

                            Label lblNewLabel2 = new Label(cmpSnappingArea, Cocoa.getStyle());
                            lblNewLabel2.setText(I18n.E3D_ROTATE_SNAP);
                            lblNewLabel2.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));

                            BigDecimalSpinner spinner2 = new BigDecimalSpinner(cmpSnappingArea, Cocoa.getStyle(), View.NUMBER_FORMAT8F);
                            this.spnRotatePtr[0] = spinner2;
                            spinner2.setMaximum(new BigDecimal("360.0")); //$NON-NLS-1$
                            spinner2.setMinimum(new BigDecimal("0.0001")); //$NON-NLS-1$
                            spinner2.setValue(userSettings.getMediumRotateSnap());
                            spinner2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

                            Label lblNewLabel3 = new Label(cmpSnappingArea, Cocoa.getStyle());
                            lblNewLabel3.setText(I18n.E3D_SCALE_SNAP);
                            lblNewLabel3.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));

                            BigDecimalSpinner spinner3 = new BigDecimalSpinner(cmpSnappingArea, Cocoa.getStyle(), View.NUMBER_FORMAT8F);
                            this.spnScalePtr[0] = spinner3;
                            spinner3.setMaximum(new BigDecimal("100.0")); //$NON-NLS-1$
                            spinner3.setMinimum(new BigDecimal("0.01")); //$NON-NLS-1$
                            spinner3.setValue(userSettings.getMediumScaleSnap());
                            spinner3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));


                            {
                                Object[] messageArguments = {I18n.getCurrentUnit()};
                                MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                                formatter.setLocale(MyLanguage.locale);
                                formatter.applyPattern(I18n.E3D_SCALE_INITIAL);

                                Label lblNewLabel31 = new Label(cmpSnappingArea, Cocoa.getStyle());
                                lblNewLabel31.setText(formatter.format(messageArguments));
                                lblNewLabel31.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                            }

                            BigDecimalSpinner spinner4 = new BigDecimalSpinner(cmpSnappingArea, Cocoa.getStyle(), View.NUMBER_FORMAT8F);
                            this.spnScaleInitialPtr[0] = spinner4;
                            spinner4.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                            spinner4.setMinimum(BigDecimal.ZERO);
                            spinner4.setValue(BigDecimal.ZERO);
                            spinner4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

                            Label lblSpacer1 = new Label(cmpSnappingArea, Cocoa.getStyle());
                            lblSpacer1.setText(" "); //$NON-NLS-1$
                            lblSpacer1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));

                            Label separator = new Label(cmpSnappingArea, SWT.SEPARATOR | SWT.HORIZONTAL);
                            separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

                            Label lblSpacer2 = new Label(cmpSnappingArea, Cocoa.getStyle());
                            lblSpacer2.setText(" "); //$NON-NLS-1$
                            lblSpacer2.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));

                            Label lblNewLabel4 = new Label(cmpSnappingArea, Cocoa.getStyle());
                            lblNewLabel4.setText(I18n.UNITS_NAME_LDU + " [" + I18n.UNITS_LDU + "]"); //$NON-NLS-1$ //$NON-NLS-2$
                            lblNewLabel4.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));

                            final BigDecimalSpinner spinnerLDU = new BigDecimalSpinner(cmpSnappingArea, Cocoa.getStyle(), View.NUMBER_FORMAT8F);
                            spinnerLDU.setMaximum(new BigDecimal("9999.99999999")); //$NON-NLS-1$
                            spinnerLDU.setMinimum(new BigDecimal("-9999.99999999")); //$NON-NLS-1$
                            spinnerLDU.setValue(BigDecimal.ZERO);
                            spinnerLDU.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

                            Label lblNewLabel5 = new Label(cmpSnappingArea, Cocoa.getStyle());
                            lblNewLabel5.setText(I18n.UNITS_NAME_SECONDARY + " [" + I18n.UNITS_SECONDARY + "]"); //$NON-NLS-1$ //$NON-NLS-2$
                            lblNewLabel5.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));

                            final BigDecimalSpinner spinnerMM = new BigDecimalSpinner(cmpSnappingArea, Cocoa.getStyle(), View.NUMBER_FORMAT8F);
                            spinnerMM.setMaximum(new BigDecimal("9999.99999999")); //$NON-NLS-1$
                            spinnerMM.setMinimum(new BigDecimal("-9999.99999999")); //$NON-NLS-1$
                            spinnerMM.setValue(BigDecimal.ZERO);
                            spinnerMM.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

                            Label lblNewLabel6 = new Label(cmpSnappingArea, Cocoa.getStyle());
                            lblNewLabel6.setText(I18n.UNITS_NAME_TERTIARY + " [" + I18n.UNITS_TERTIARY + "]"); //$NON-NLS-1$ //$NON-NLS-2$
                            lblNewLabel6.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));

                            final BigDecimalSpinner spinnerStud = new BigDecimalSpinner(cmpSnappingArea, Cocoa.getStyle(), View.NUMBER_FORMAT1F);
                            spinnerStud.setMaximum(new BigDecimal("9999.99999999")); //$NON-NLS-1$
                            spinnerStud.setMinimum(new BigDecimal("-9999.99999999")); //$NON-NLS-1$
                            spinnerStud.setValue(BigDecimal.ZERO);
                            spinnerStud.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

                            Label lblNewLabel7 = new Label(cmpSnappingArea, Cocoa.getStyle());
                            lblNewLabel7.setText(I18n.UNITS_NAME_PRIMARY + " [" + I18n.UNITS_PRIMARY + "]"); //$NON-NLS-1$ //$NON-NLS-2$
                            lblNewLabel7.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));

                            final BigDecimalSpinner spinnerInch = new BigDecimalSpinner(cmpSnappingArea, Cocoa.getStyle(), View.NUMBER_FORMAT8F);
                            spinnerInch.setMaximum(new BigDecimal("9999.99999999")); //$NON-NLS-1$
                            spinnerInch.setMinimum(new BigDecimal("-9999.99999999")); //$NON-NLS-1$
                            spinnerInch.setValue(BigDecimal.ZERO);
                            spinnerInch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

                            final AtomicBoolean change = new AtomicBoolean();

                            spinnerLDU.addValueChangeListener(spn -> {
                                if (change.get()) return;
                                change.set(true);
                                spinnerInch.setValue(spn.getValue().multiply(new BigDecimal(I18n.UNITS_FACTOR_PRIMARY), Threshold.MC));
                                spinnerMM.setValue(spn.getValue().multiply(new BigDecimal(I18n.UNITS_FACTOR_SECONDARY), Threshold.MC));
                                spinnerStud.setValue(spinnerLDU.getValue().multiply(new BigDecimal(I18n.UNITS_FACTOR_TERTIARY), Threshold.MC).setScale(1, RoundingMode.HALF_UP));
                                change.set(false);
                            });

                            spinnerInch.addValueChangeListener(spn -> {
                                if (change.get()) return;
                                change.set(true);
                                spinnerLDU.setValue(spn.getValue().divide(new BigDecimal(I18n.UNITS_FACTOR_PRIMARY), Threshold.MC));
                                spinnerMM.setValue(spinnerLDU.getValue().multiply(new BigDecimal(I18n.UNITS_FACTOR_SECONDARY), Threshold.MC));
                                spinnerStud.setValue(spinnerLDU.getValue().multiply(new BigDecimal(I18n.UNITS_FACTOR_TERTIARY), Threshold.MC).setScale(1, RoundingMode.HALF_UP));
                                change.set(false);
                            });

                            spinnerMM.addValueChangeListener(spn -> {
                                if (change.get()) return;
                                change.set(true);
                                spinnerLDU.setValue(spn.getValue().divide(new BigDecimal(I18n.UNITS_FACTOR_SECONDARY), Threshold.MC));
                                spinnerInch.setValue(spinnerLDU.getValue().multiply(new BigDecimal(I18n.UNITS_FACTOR_PRIMARY), Threshold.MC));
                                spinnerStud.setValue(spinnerLDU.getValue().multiply(new BigDecimal(I18n.UNITS_FACTOR_TERTIARY), Threshold.MC).setScale(1, RoundingMode.HALF_UP));
                                change.set(false);
                            });

                            spinnerStud.addValueChangeListener(spn -> {
                                if (change.get()) return;
                                change.set(true);
                                spinnerLDU.setValue(spn.getValue().divide(new BigDecimal(I18n.UNITS_FACTOR_TERTIARY), Threshold.MC));
                                spinnerInch.setValue(spinnerLDU.getValue().multiply(new BigDecimal(I18n.UNITS_FACTOR_PRIMARY), Threshold.MC));
                                spinnerMM.setValue(spinnerLDU.getValue().multiply(new BigDecimal(I18n.UNITS_FACTOR_SECONDARY), Threshold.MC));
                                change.set(false);
                            });

                            Label separator2 = new Label(cmpSnappingArea, SWT.SEPARATOR | SWT.HORIZONTAL);
                            separator2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

                            Label lblSpacer3 = new Label(cmpSnappingArea, Cocoa.getStyle());
                            lblSpacer3.setText(" "); //$NON-NLS-1$
                            lblSpacer3.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));

                            Label lblNewLabel8 = new Label(cmpSnappingArea, Cocoa.getStyle());
                            lblNewLabel8.setText(I18n.E3D_THRESH_FOR_ADDING_ELEMENTS_3D);
                            lblNewLabel8.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));

                            final BigDecimalSpinner spinnerT3D = new BigDecimalSpinner(cmpSnappingArea, Cocoa.getStyle(), View.NUMBER_FORMAT8F);
                            spinnerT3D.setMaximum(BigDecimal.ONE);
                            spinnerT3D.setMinimum(BigDecimal.ZERO);
                            spinnerT3D.setValue(userSettings.getFuzziness3D());
                            spinnerT3D.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

                            spinnerT3D.addValueChangeListener(spn -> WorkbenchManager.getUserSettingState().setFuzziness3D(spn.getValue()));

                            Label lblNewLabel9 = new Label(cmpSnappingArea, Cocoa.getStyle());
                            lblNewLabel9.setText(I18n.E3D_THRESH_FOR_ADDING_ELEMENTS_2D);
                            lblNewLabel9.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));

                            final IntegerSpinner spinnerT2D = new IntegerSpinner(cmpSnappingArea, Cocoa.getStyle());
                            spinnerT2D.setMaximum(9999);
                            spinnerT2D.setMinimum(1);
                            spinnerT2D.setValue(userSettings.getFuzziness2D());
                            spinnerT2D.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

                            spinnerT2D.addValueChangeListener(spn -> WorkbenchManager.getUserSettingState().setFuzziness2D(spn.getValue()));

                            Label lblSpacer4 = new Label(cmpSnappingArea, Cocoa.getStyle());
                            lblSpacer4.setText(" "); //$NON-NLS-1$
                            lblSpacer4.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));

                            cmpScroll.setMinSize(cmpSnappingArea.computeSize(SWT.DEFAULT, SWT.DEFAULT));
                        }

                        CTabItem tItem2 = new CTabItem(tabFolderSettings, Cocoa.getStyle());
                        tItem2.setText("(2)"); //$NON-NLS-1$
                        tItem2.setToolTipText(I18n.E3D_SELECTION);
                        {
                            final ScrolledComposite cmpScroll = new ScrolledComposite(tabFolderSettings, SWT.V_SCROLL | SWT.H_SCROLL);
                            Composite cmpSelArea = new Composite(cmpScroll, Cocoa.getStyle());
                            tItem2.setControl(cmpScroll);
                            cmpScroll.setContent(cmpSelArea);
                            cmpScroll.setExpandHorizontal(true);
                            cmpScroll.setExpandVertical(true);

                            cmpSelArea.setLayout(new GridLayout(3, false));
                            ((GridLayout) cmpSelArea.getLayout()).verticalSpacing = 0;
                            ((GridLayout) cmpSelArea.getLayout()).marginHeight = 0;
                            ((GridLayout) cmpSelArea.getLayout()).marginWidth = 0;

                            {
                                Label lblTabHeader = new Label(cmpSelArea, Cocoa.getStyle());
                                lblTabHeader.setText(I18n.E3D_SELECTION);
                                lblTabHeader.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
                                NButton btnShowUpper = new NButton(cmpSelArea, Cocoa.getStyle());
                                this.btnShowUpper2Ptr[0] = btnShowUpper;
                                btnShowUpper.setImage(ResourceManager.getImage("icon16_upper.png")); //$NON-NLS-1$
                                btnShowUpper.setToolTipText(I18n.E3D_SASH_UPPER);
                                btnShowUpper.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 2, 1));
                            }

                            {
                                Composite cmpDummy = new Composite(cmpSelArea, Cocoa.getStyle());
                                cmpDummy.setLayout(new FillLayout(SWT.HORIZONTAL));

                                NButton btnPreviousSelection = new NButton(cmpDummy, Cocoa.getStyle());
                                this.btnPreviousSelectionPtr[0] = btnPreviousSelection;
                                btnPreviousSelection.setImage(ResourceManager.getImage("icon8_previous.png")); //$NON-NLS-1$
                                btnPreviousSelection.setToolTipText(I18n.E3D_PREVIOUS_ITEM);

                                NButton btnNextSelection = new NButton(cmpDummy, Cocoa.getStyle());
                                this.btnNextSelectionPtr[0] = btnNextSelection;
                                btnNextSelection.setImage(ResourceManager.getImage("icon8_next.png")); //$NON-NLS-1$
                                btnNextSelection.setToolTipText(I18n.E3D_NEXT_ITEM);

                                cmpDummy.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 3, 1));
                            }

                            {
                                Composite cmpLineSetup = new Composite(cmpSelArea, Cocoa.getStyle());
                                cmpLineSetup.setLayout(new GridLayout(1, false));

                                Text txtLine = new Text(cmpLineSetup, SWT.BORDER);
                                this.txtLinePtr[0] = txtLine;
                                txtLine.setEnabled(false);
                                txtLine.setEditable(false);
                                txtLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

                                NButton btnMoveAdjacentData2 = new NButton(cmpLineSetup, SWT.TOGGLE);
                                this.btnMoveAdjacentData2Ptr[0] = btnMoveAdjacentData2;
                                btnMoveAdjacentData2.setImage(ResourceManager.getImage("icon16_adjacentmove.png")); //$NON-NLS-1$
                                btnMoveAdjacentData2.setText(I18n.E3D_MOVE_ADJACENT_DATA);

                                cmpLineSetup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lblLabel = new Label(cmpSelArea, SWT.SEPARATOR | SWT.HORIZONTAL);
                                lblLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lblLabel = new Label(cmpSelArea, Cocoa.getStyle());
                                this.lblSelectionAnglePtr[0] = lblLabel;
                                lblLabel.setText(I18n.E3D_PROTRACTOR_ANGLE);
                                lblLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmpSelArea, Cocoa.getStyle());
                                this.spnSelectionAnglePtr[0] = spinner;
                                spinner.setEnabled(false);
                                spinner.setMaximum(new BigDecimal("179.9999")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("0.0001")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lblLabel = new Label(cmpSelArea, Cocoa.getStyle());
                                this.lblSelectionLengthPtr[0] = lblLabel;
                                lblLabel.setText(I18n.E3D_PROTRACTOR_LENGTH);
                                lblLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmpSelArea, Cocoa.getStyle());
                                this.spnSelectionLengthPtr[0] = spinner;
                                spinner.setEnabled(false);
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("0.00000001")); //$NON-NLS-1$
                                spinner.setValue(BigDecimal.ONE);
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lblLabel = new Label(cmpSelArea, Cocoa.getStyle());
                                this.lblSelectionX1Ptr[0] = lblLabel;
                                lblLabel.setText(I18n.E3D_POSITION_X1);
                                lblLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmpSelArea, Cocoa.getStyle(), View.NUMBER_FORMAT8F);
                                this.spnSelectionX1Ptr[0] = spinner;
                                spinner.setEnabled(false);
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lblLabel = new Label(cmpSelArea, Cocoa.getStyle());
                                this.lblSelectionY1Ptr[0] = lblLabel;
                                lblLabel.setText(I18n.E3D_POSITION_Y1);
                                lblLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmpSelArea, Cocoa.getStyle(), View.NUMBER_FORMAT8F);
                                this.spnSelectionY1Ptr[0] = spinner;
                                spinner.setEnabled(false);
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lblLabel = new Label(cmpSelArea, Cocoa.getStyle());
                                this.lblSelectionZ1Ptr[0] = lblLabel;
                                lblLabel.setText(I18n.E3D_POSITION_Z1);
                                lblLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmpSelArea, Cocoa.getStyle(), View.NUMBER_FORMAT8F);
                                this.spnSelectionZ1Ptr[0] = spinner;
                                spinner.setEnabled(false);
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lblLabel = new Label(cmpSelArea, Cocoa.getStyle());
                                this.lblSelectionX2Ptr[0] = lblLabel;
                                lblLabel.setText(I18n.E3D_POSITION_X2);
                                lblLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmpSelArea, Cocoa.getStyle(), View.NUMBER_FORMAT8F);
                                this.spnSelectionX2Ptr[0] = spinner;
                                spinner.setEnabled(false);
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lblLabel = new Label(cmpSelArea, Cocoa.getStyle());
                                this.lblSelectionY2Ptr[0] = lblLabel;
                                lblLabel.setText(I18n.E3D_POSITION_Y2);
                                lblLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmpSelArea, Cocoa.getStyle(), View.NUMBER_FORMAT8F);
                                this.spnSelectionY2Ptr[0] = spinner;
                                spinner.setEnabled(false);
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lblLabel = new Label(cmpSelArea, Cocoa.getStyle());
                                this.lblSelectionZ2Ptr[0] = lblLabel;
                                lblLabel.setText(I18n.E3D_POSITION_Z2);
                                lblLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmpSelArea, Cocoa.getStyle(), View.NUMBER_FORMAT8F);
                                this.spnSelectionZ2Ptr[0] = spinner;
                                spinner.setEnabled(false);
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lblLabel = new Label(cmpSelArea, Cocoa.getStyle());
                                this.lblSelectionX3Ptr[0] = lblLabel;
                                lblLabel.setText(I18n.E3D_POSITION_X3);
                                lblLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmpSelArea, Cocoa.getStyle(), View.NUMBER_FORMAT8F);
                                this.spnSelectionX3Ptr[0] = spinner;
                                spinner.setEnabled(false);
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lblLabel = new Label(cmpSelArea, Cocoa.getStyle());
                                this.lblSelectionY3Ptr[0] = lblLabel;
                                lblLabel.setText(I18n.E3D_POSITION_Y3);
                                lblLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmpSelArea, Cocoa.getStyle(), View.NUMBER_FORMAT8F);
                                this.spnSelectionY3Ptr[0] = spinner;
                                spinner.setEnabled(false);
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lblLabel = new Label(cmpSelArea, Cocoa.getStyle());
                                this.lblSelectionZ3Ptr[0] = lblLabel;
                                lblLabel.setText(I18n.E3D_POSITION_Z3);
                                lblLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmpSelArea, Cocoa.getStyle(), View.NUMBER_FORMAT8F);
                                this.spnSelectionZ3Ptr[0] = spinner;
                                spinner.setEnabled(false);
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lblLabel = new Label(cmpSelArea, Cocoa.getStyle());
                                this.lblSelectionX4Ptr[0] = lblLabel;
                                lblLabel.setText(I18n.E3D_POSITION_X4);
                                lblLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmpSelArea, Cocoa.getStyle(), View.NUMBER_FORMAT8F);
                                this.spnSelectionX4Ptr[0] = spinner;
                                spinner.setEnabled(false);
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lblLabel = new Label(cmpSelArea, Cocoa.getStyle());
                                this.lblSelectionY4Ptr[0] = lblLabel;
                                lblLabel.setText(I18n.E3D_POSITION_Y4);
                                lblLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmpSelArea, Cocoa.getStyle(), View.NUMBER_FORMAT8F);
                                this.spnSelectionY4Ptr[0] = spinner;
                                spinner.setEnabled(false);
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lblLabel = new Label(cmpSelArea, Cocoa.getStyle());
                                this.lblSelectionZ4Ptr[0] = lblLabel;
                                lblLabel.setText(I18n.E3D_POSITION_Z4);
                                lblLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmpSelArea, Cocoa.getStyle(), View.NUMBER_FORMAT8F);
                                this.spnSelectionZ4Ptr[0] = spinner;
                                spinner.setEnabled(false);
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            cmpScroll.setMinSize(cmpSelArea.computeSize(SWT.DEFAULT, SWT.DEFAULT));
                        }

                        CTabItem tItem3 = new CTabItem(tabFolderSettings, Cocoa.getStyle());
                        tItem3.setText("(3)"); //$NON-NLS-1$
                        tItem3.setToolTipText(I18n.E3D_BACKGROUND_IMAGE);

                        {
                            final ScrolledComposite cmpScroll = new ScrolledComposite(tabFolderSettings, SWT.V_SCROLL | SWT.H_SCROLL);
                            Composite cmpBgArea = new Composite(cmpScroll, Cocoa.getStyle());
                            tItem3.setControl(cmpScroll);
                            cmpScroll.setContent(cmpBgArea);
                            cmpScroll.setExpandHorizontal(true);
                            cmpScroll.setExpandVertical(true);

                            cmpBgArea.setLayout(new GridLayout(3, false));
                            ((GridLayout) cmpBgArea.getLayout()).verticalSpacing = 0;
                            ((GridLayout) cmpBgArea.getLayout()).marginHeight = 0;
                            ((GridLayout) cmpBgArea.getLayout()).marginWidth = 0;

                            {
                                Label lblTabHeader = new Label(cmpBgArea, Cocoa.getStyle());
                                lblTabHeader.setText(I18n.E3D_BACKGROUND_IMAGE);
                                lblTabHeader.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
                                NButton btnShowUpper = new NButton(cmpBgArea, Cocoa.getStyle());
                                this.btnShowUpper3Ptr[0] = btnShowUpper;
                                btnShowUpper.setImage(ResourceManager.getImage("icon16_upper.png")); //$NON-NLS-1$
                                btnShowUpper.setToolTipText(I18n.E3D_SASH_UPPER);
                                btnShowUpper.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 2, 1));
                            }

                            {
                                Composite cmpDummy = new Composite(cmpBgArea, Cocoa.getStyle());
                                cmpDummy.setLayout(new FillLayout(SWT.HORIZONTAL));

                                NButton btnPrevious = new NButton(cmpDummy, Cocoa.getStyle());
                                btnPngPreviousPtr[0] = btnPrevious;
                                btnPrevious.setImage(ResourceManager.getImage("icon8_previous.png")); //$NON-NLS-1$
                                btnPrevious.setToolTipText(I18n.E3D_PREVIOUS);

                                NButton btnFocusBG = new NButton(cmpDummy, Cocoa.getStyle());
                                btnPngFocusPtr[0] = btnFocusBG;
                                btnFocusBG.setImage(ResourceManager.getImage("icon8_focus.png")); //$NON-NLS-1$
                                btnFocusBG.setToolTipText(I18n.E3D_FOCUS);

                                NButton btnNext = new NButton(cmpDummy, Cocoa.getStyle());
                                btnPngNextPtr[0] = btnNext;
                                btnNext.setImage(ResourceManager.getImage("icon8_next.png")); //$NON-NLS-1$
                                btnNext.setToolTipText(I18n.E3D_NEXT);

                                cmpDummy.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 3, 1));
                            }

                            {
                                Label lblLabel = new Label(cmpBgArea, Cocoa.getStyle());
                                lblLabel.setText(I18n.E3D_IMAGE);
                                lblLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Composite cmpPathChooser1 = new Composite(cmpBgArea, Cocoa.getStyle());
                                cmpPathChooser1.setLayout(new GridLayout(2, false));

                                Text txtPngPath = new Text(cmpPathChooser1, SWT.BORDER);
                                this.txtPngPathPtr[0] = txtPngPath;
                                txtPngPath.setEditable(false);
                                txtPngPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

                                NButton btnBrowsePngPath = new NButton(cmpPathChooser1, Cocoa.getStyle());
                                btnPngImagePtr[0] = btnBrowsePngPath;
                                btnBrowsePngPath.setText(I18n.DIALOG_BROWSE);

                                cmpPathChooser1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lblLabel = new Label(cmpBgArea, Cocoa.getStyle());
                                lblLabel.setText(I18n.E3D_POSITION_X);
                                lblLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmpBgArea, Cocoa.getStyle());
                                spnPngXPtr[0] = spinner;
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lblLabel = new Label(cmpBgArea, Cocoa.getStyle());
                                lblLabel.setText(I18n.E3D_POSITION_Y);
                                lblLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmpBgArea, Cocoa.getStyle());
                                spnPngYPtr[0] = spinner;
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lblLabel = new Label(cmpBgArea, Cocoa.getStyle());
                                lblLabel.setText(I18n.E3D_POSITION_Z);
                                lblLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmpBgArea, Cocoa.getStyle());
                                spnPngZPtr[0] = spinner;
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lblLabel = new Label(cmpBgArea, Cocoa.getStyle());
                                lblLabel.setText(I18n.E3D_ANGLE_Y);
                                lblLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmpBgArea, Cocoa.getStyle());
                                spnPngA1Ptr[0] = spinner;
                                spinner.setMaximum(new BigDecimal("360")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-360")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lblLabel = new Label(cmpBgArea, Cocoa.getStyle());
                                lblLabel.setText(I18n.E3D_ANGLE_X);
                                lblLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmpBgArea, Cocoa.getStyle());
                                spnPngA2Ptr[0] = spinner;
                                spinner.setMaximum(new BigDecimal("360")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-360")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lblLabel = new Label(cmpBgArea, Cocoa.getStyle());
                                lblLabel.setText(I18n.E3D_ANGLE_Z);
                                lblLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmpBgArea, Cocoa.getStyle());
                                spnPngA3Ptr[0] = spinner;
                                spinner.setMaximum(new BigDecimal("360")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-360")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lblLabel = new Label(cmpBgArea, Cocoa.getStyle());
                                lblLabel.setText(I18n.E3D_SCALE_X);
                                lblLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmpBgArea, Cocoa.getStyle());
                                spnPngSXPtr[0] = spinner;
                                spinner.setMaximum(new BigDecimal("1E6")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E6")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lblLabel = new Label(cmpBgArea, Cocoa.getStyle());
                                lblLabel.setText(I18n.E3D_SCALE_Y);
                                lblLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmpBgArea, Cocoa.getStyle());
                                spnPngSYPtr[0] = spinner;
                                spinner.setMaximum(new BigDecimal("1E6")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E6")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            cmpScroll.setMinSize(cmpBgArea.computeSize(SWT.DEFAULT, SWT.DEFAULT));
                        }

                        tabFolderSettings.setSelection(tItem);
                    }
                    Composite cmpContainer2 = new Composite(sashForm2, SWT.BORDER);
                    GridLayout gridLayout3 = new GridLayout(2, false);
                    cmpContainer2.setLayout(gridLayout3);
                    {
                        Tree treeAllParts = new Tree(cmpContainer2, SWT.BORDER, 1);
                        this.treeParts[0] = treeAllParts;
                        TreeItem treeItemProjectName = new TreeItem(treeAllParts);
                        this.treeItemProjectPtr[0] = treeItemProjectName;
                        treeItemProjectName.setText(I18n.PROJECT_NEW_PROJECT);
                        TreeItem treeItemProjectParts = new TreeItem(treeItemProjectName);
                        this.treeItemProjectPartsPtr[0] = treeItemProjectParts;
                        treeItemProjectParts.setText(I18n.PARTS_PARTS);

                        TreeItem treeItemNewPart = new TreeItem(treeItemProjectParts);
                        treeItemNewPart.setData(Project.getFileToEdit());
                        Project.addUnsavedFile(Project.getFileToEdit());


                        if (NLogger.debugging) {
                            UTF8BufferedReader reader = null;
                            try {
                                reader = new UTF8BufferedReader("testsource.txt"); //$NON-NLS-1$
                                StringBuilder sb = new StringBuilder();
                                String s;
                                while ((s = reader.readLine()) != null) {
                                    sb.append(s);
                                    sb.append(StringHelper.getLineDelimiter());
                                }
                                Project.getFileToEdit().setText(sb.toString());
                            } catch (LDParsingException e) {
                            } catch (FileNotFoundException e) {
                            } catch (UnsupportedEncodingException e) {
                            } finally {
                                try {
                                    if (reader != null)
                                        reader.close();
                                } catch (LDParsingException e1) {
                                }
                            }
                        } else {
                            Project.getFileToEdit().setText(WorkbenchManager.getDefaultFileHeader());
                        }

                        TreeItem treeItemProjectSubparts = new TreeItem(treeItemProjectName);
                        this.treeItemProjectSubpartsPtr[0] = treeItemProjectSubparts;
                        treeItemProjectSubparts.setText(I18n.PARTS_SUBPARTS);
                        TreeItem treeItemProjectPrimitives = new TreeItem(treeItemProjectName);
                        this.treeItemProjectPrimitivesPtr[0] = treeItemProjectPrimitives;
                        treeItemProjectPrimitives.setText(I18n.PARTS_PRIMITIVES);
                        TreeItem treeItemProjectHiResPrimitives = new TreeItem(treeItemProjectName);
                        this.treeItemProjectPrimitives48Ptr[0] = treeItemProjectHiResPrimitives;
                        treeItemProjectHiResPrimitives.setText(I18n.PARTS_HI_RES_PRIMITIVES);
                        TreeItem treeItemProjectLowResPrimitives = new TreeItem(treeItemProjectName);
                        this.treeItemProjectPrimitives8Ptr[0] = treeItemProjectLowResPrimitives;
                        treeItemProjectLowResPrimitives.setText(I18n.PARTS_LOW_RES_PRIMITIVES);

                        TreeItem treeItemUnsaved = new TreeItem(treeAllParts);
                        this.treeItemUnsavedPtr[0] = treeItemUnsaved;
                        treeItemUnsaved.setText(I18n.E3D_UNSAVED_FILES);
                        treeItemUnsaved.setVisible(false);
                        TreeItem treeItemNewPart2 = new TreeItem(treeItemUnsavedPtr[0]);
                        treeItemNewPart2.setData(Project.getFileToEdit());
                        treeItemNewPart2.setText(Project.getFileToEdit().getShortName());

                        TreeItem treeItemUnofficial = new TreeItem(treeAllParts);
                        this.treeItemUnofficialPtr[0] = treeItemUnofficial;
                        treeItemUnofficial.setText(I18n.PROJECT_UNOFFICIAL_LIB_READ_WRITE);
                        treeItemUnofficial.setVisible(false);
                        TreeItem treeItemUnofficialParts = new TreeItem(treeItemUnofficial);
                        this.treeItemUnofficialPartsPtr[0] = treeItemUnofficialParts;
                        treeItemUnofficialParts.setText(I18n.PARTS_PARTS);
                        treeItemUnofficialParts.setVisible(false);
                        TreeItem treeItemUnofficialSubparts = new TreeItem(treeItemUnofficial);
                        this.treeItemUnofficialSubpartsPtr[0] = treeItemUnofficialSubparts;
                        treeItemUnofficialSubparts.setText(I18n.PARTS_SUBPARTS);
                        treeItemUnofficialSubparts.setVisible(false);
                        TreeItem treeItemUnofficialPrimitives = new TreeItem(treeItemUnofficial);
                        this.treeItemUnofficialPrimitivesPtr[0] = treeItemUnofficialPrimitives;
                        treeItemUnofficialPrimitives.setText(I18n.PARTS_PRIMITIVES);
                        treeItemUnofficialPrimitives.setVisible(false);
                        TreeItem treeItemUnofficialHiResPrimitives = new TreeItem(treeItemUnofficial);
                        this.treeItemUnofficialPrimitives48Ptr[0] = treeItemUnofficialHiResPrimitives;
                        treeItemUnofficialHiResPrimitives.setText(I18n.PARTS_HI_RES_PRIMITIVES);
                        treeItemUnofficialHiResPrimitives.setVisible(false);
                        TreeItem treeItemUnofficialLowResPrimitives = new TreeItem(treeItemUnofficial);
                        this.treeItemUnofficialPrimitives8Ptr[0] = treeItemUnofficialLowResPrimitives;
                        treeItemUnofficialLowResPrimitives.setText(I18n.PARTS_LOW_RES_PRIMITIVES);
                        treeItemUnofficialLowResPrimitives.setVisible(false);

                        TreeItem treeItemOfficial = new TreeItem(treeAllParts);
                        this.treeItemOfficialPtr[0] = treeItemOfficial;
                        treeItemOfficial.setText(I18n.PROJECT_OFFICIAL_LIB_READ);
                        treeItemOfficial.setVisible(false);
                        TreeItem treeItemOfficialParts = new TreeItem(treeItemOfficial);
                        this.treeItemOfficialPartsPtr[0] = treeItemOfficialParts;
                        treeItemOfficialParts.setText(I18n.PARTS_PARTS);
                        treeItemOfficialParts.setVisible(false);
                        TreeItem treeItemOfficialSubparts = new TreeItem(treeItemOfficial);
                        this.treeItemOfficialSubpartsPtr[0] = treeItemOfficialSubparts;
                        treeItemOfficialSubparts.setText(I18n.PARTS_SUBPARTS);
                        treeItemOfficialSubparts.setVisible(false);
                        TreeItem treeItemOfficialPrimitives = new TreeItem(treeItemOfficial);
                        this.treeItemOfficialPrimitivesPtr[0] = treeItemOfficialPrimitives;
                        treeItemOfficialPrimitives.setText(I18n.PARTS_PRIMITIVES);
                        treeItemOfficialPrimitives.setVisible(false);
                        TreeItem treeItemOfficialHiResPrimitives = new TreeItem(treeItemOfficial);
                        this.treeItemOfficialPrimitives48Ptr[0] = treeItemOfficialHiResPrimitives;
                        treeItemOfficialHiResPrimitives.setText(I18n.PARTS_HI_RES_PRIMITIVES);
                        treeItemOfficialHiResPrimitives.setVisible(false);
                        TreeItem treeItemOfficialLowResPrimitives = new TreeItem(treeItemOfficial);
                        this.treeItemOfficialPrimitives8Ptr[0] = treeItemOfficialLowResPrimitives;
                        treeItemOfficialLowResPrimitives.setText(I18n.PARTS_LOW_RES_PRIMITIVES);
                        treeItemOfficialLowResPrimitives.setVisible(false);

                        GridData gridData = new GridData();
                        gridData.horizontalAlignment = SWT.FILL;
                        gridData.verticalAlignment = SWT.FILL;
                        gridData.grabExcessVerticalSpace = true;
                        gridData.grabExcessHorizontalSpace = true;
                        gridData.verticalSpan = 8;
                        treeAllParts.setLayoutData(gridData);
                    }
                    {
                        NButton btnShowMiddle = new NButton(cmpContainer2, SWT.RIGHT_TO_LEFT | Cocoa.getStyle());
                        this.btnShowMiddlePtr[0] = btnShowMiddle;
                        btnShowMiddle.setImage(ResourceManager.getImage("icon16_middle.png")); //$NON-NLS-1$
                        btnShowMiddle.setToolTipText(I18n.E3D_SASH_MIDDLE);
                    }
                    {
                        NButton btnSameHeight = new NButton(cmpContainer2, SWT.RIGHT_TO_LEFT | Cocoa.getStyle());
                        this.btnSameHeightPtr[0] = btnSameHeight;
                        btnSameHeight.setImage(ResourceManager.getImage("icon16_sameHeight.png")); //$NON-NLS-1$
                        btnSameHeight.setToolTipText(I18n.E3D_SASH_SAME_HEIGHT);
                    }
                    {
                        NButton btnOpenIn3DEditor = new NButton(cmpContainer2, Cocoa.getStyle());
                        this.btnOpenIn3DEditorPtr[0] = btnOpenIn3DEditor;
                        btnOpenIn3DEditor.setImage(ResourceManager.getImage("icon16_openIn3D.png")); //$NON-NLS-1$
                        btnOpenIn3DEditor.setToolTipText(I18n.E3D_OPEN_IN_3D_EDITOR);
                        btnOpenIn3DEditor.setLayoutData(new GridData());
                        btnOpenIn3DEditor.setEnabled(false);
                    }
                    {
                        NButton btnOpenInTextEditor = new NButton(cmpContainer2, Cocoa.getStyle());
                        this.btnOpenInTextEditorPtr[0] = btnOpenInTextEditor;
                        btnOpenInTextEditor.setImage(ResourceManager.getImage("icon16_openInText.png")); //$NON-NLS-1$
                        btnOpenInTextEditor.setToolTipText(I18n.E3D_OPEN_IN_TEXT_EDITOR);
                        btnOpenInTextEditor.setLayoutData(new GridData());
                        btnOpenInTextEditor.setEnabled(false);
                    }
                    {
                        NButton btnRename = new NButton(cmpContainer2, Cocoa.getStyle());
                        this.btnRenamePtr[0] = btnRename;
                        btnRename.setImage(ResourceManager.getImage("icon16_rename.png")); //$NON-NLS-1$
                        btnRename.setToolTipText(I18n.E3D_RENAME_MOVE);
                        btnRename.setLayoutData(new GridData());
                        btnRename.setEnabled(false);
                    }
                    {
                        NButton btnRevert = new NButton(cmpContainer2, Cocoa.getStyle());
                        this.btnRevertPtr[0] = btnRevert;
                        btnRevert.setImage(ResourceManager.getImage("icon16_revert.png")); //$NON-NLS-1$
                        btnRevert.setToolTipText(I18n.E3D_REVERT_ALL_CHANGES);
                        btnRevert.setLayoutData(new GridData());
                        btnRevert.setEnabled(false);
                    }
                    {
                        NButton btnCopyToUnofficial = new NButton(cmpContainer2, Cocoa.getStyle());
                        this.btnCopyToUnofficialPtr[0] = btnCopyToUnofficial;
                        btnCopyToUnofficial.setImage(ResourceManager.getImage("icon16_copyToUnofficial.png")); //$NON-NLS-1$
                        btnCopyToUnofficial.setToolTipText(I18n.E3D_COPY_TO_UNOFFICIAL_LIBRARY);
                        btnCopyToUnofficial.setLayoutData(new GridData());
                        btnCopyToUnofficial.setEnabled(false);
                    }
                    {
                        NButton btnClose = new NButton(cmpContainer2, Cocoa.getStyle());
                        this.btnClosePtr[0] = btnClose;
                        btnClose.setImage(ResourceManager.getImage("icon16_close.png")); //$NON-NLS-1$
                        btnClose.setToolTipText(I18n.E3D_CLOSE);
                        btnClose.setLayoutData(new GridData());
                        btnClose.setEnabled(false);
                    }
                    {
                        Composite cmpSearch = new Composite(cmpContainer2, Cocoa.getStyle());
                        GridData gridData = new GridData();
                        gridData.horizontalAlignment = SWT.FILL;
                        gridData.grabExcessHorizontalSpace = true;
                        gridData.horizontalSpan = 2;
                        cmpSearch.setLayoutData(gridData);
                        GridLayout gridLayout2 = new GridLayout(4, false);
                        cmpSearch.setLayout(gridLayout2);
                        Text txtSearch = new Text(cmpSearch, SWT.BORDER);
                        this.txtSearchPtr[0] = txtSearch;
                        txtSearch.setMessage(I18n.E3D_SEARCH);
                        GridData gridData2 = new GridData();
                        gridData2.horizontalAlignment = SWT.FILL;
                        gridData2.grabExcessHorizontalSpace = true;
                        txtSearch.setLayoutData(gridData2);
                        NButton btnResetSearch = new NButton(cmpSearch, Cocoa.getStyle());
                        this.btnResetSearchPtr[0] = btnResetSearch;
                        btnResetSearch.setText(I18n.E3D_RESET);
                    }

                    Composite cmpContainer4 = new Composite(sashForm2, SWT.BORDER);
                    GridLayout gridLayout4 = new GridLayout(1, true);
                    cmpContainer4.setLayout(gridLayout4);

                    CompositePrimitive cmpPrimitives = new CompositePrimitive(cmpContainer4);
                    this.cmpPrimitivesPtr[0] = cmpPrimitives;

                    Matrix4f[] primitiveViewport = windowState.getPrimitiveViewport();
                    if (primitiveViewport != null) {
                        cmpPrimitives.setViewport2(primitiveViewport);
                    }

                    cmpPrimitives.setLayout(new FillLayout());

                    GridData gd = new GridData();
                    gd.grabExcessHorizontalSpace = true;
                    gd.grabExcessVerticalSpace = true;
                    gd.horizontalAlignment = SWT.FILL;
                    gd.verticalAlignment = SWT.FILL;
                    cmpPrimitives.setLayoutData(gd);

                    cmpPrimitives.loadPrimitives();

                    Label lblSelectedPrimitiveItem = new Label(cmpContainer4, Cocoa.getStyle());
                    this.lblSelectedPrimitiveItemPtr[0] = lblSelectedPrimitiveItem;
                    {
                        GridData gd2 = new GridData();
                        gd2.grabExcessHorizontalSpace = true;
                        gd2.horizontalAlignment = SWT.FILL;
                        lblSelectedPrimitiveItem.setLayoutData(gd2);
                    }
                    lblSelectedPrimitiveItem.setText(I18n.E3D_NO_PRIMITIVE_LOADED);

                    {
                        Composite cmpSearch = new Composite(cmpContainer4, Cocoa.getStyle());
                        GridData gridData = new GridData();
                        gridData.horizontalAlignment = SWT.FILL;
                        gridData.grabExcessHorizontalSpace = true;
                        cmpSearch.setLayoutData(gridData);
                        GridLayout gridLayout2 = new GridLayout(5, false);
                        cmpSearch.setLayout(gridLayout2);
                        Text txtSearch = new Text(cmpSearch, SWT.BORDER);
                        this.txtPrimitiveSearchPtr[0] = txtSearch;
                        txtSearch.setMessage(I18n.E3D_SEARCH_PRIMITIVES);
                        GridData gridData2 = new GridData();
                        gridData2.horizontalAlignment = SWT.FILL;
                        gridData2.grabExcessHorizontalSpace = true;
                        txtSearch.setLayoutData(gridData2);
                        NButton btnResetSearch = new NButton(cmpSearch, Cocoa.getStyle());
                        this.btnResetPrimitiveSearchPtr[0] = btnResetSearch;
                        btnResetSearch.setText(I18n.E3D_RESET);
                        NButton btnZoomOutPrimitives = new NButton(cmpSearch, Cocoa.getStyle());
                        this.btnZoomOutPrimitivesPtr[0] = btnZoomOutPrimitives;
                        btnZoomOutPrimitives.setText("-"); //$NON-NLS-1$
                        NButton btnZoomInPrimitives = new NButton(cmpSearch, Cocoa.getStyle());
                        this.btnZoomInPrimitivesPtr[0] = btnZoomInPrimitives;
                        btnZoomInPrimitives.setText("+"); //$NON-NLS-1$
                        NButton btnShowLower = new NButton(cmpSearch, SWT.RIGHT_TO_LEFT | Cocoa.getStyle());
                        this.btnShowLowerPtr[0] = btnShowLower;
                        btnShowLower.setImage(ResourceManager.getImage("icon16_lower.png")); //$NON-NLS-1$
                        btnShowLower.setToolTipText(I18n.E3D_SASH_LOWER);
                    }

                    int[] weights = windowState.getLeftSashWeights();
                    if (weights != null) {
                        sashForm2.setWeights(weights);
                    }
                }

                ArrayList<Composite3DState> threeDconfig = windowState.getThreeDwindowConfig();
                if (threeDconfig == null) {
                    new CompositeContainer(sashForm, false);
                } else {
                    final int configSize = threeDconfig.size();
                    if (configSize < 2) {
                        if (configSize == 1) {
                            Composite3DState state = threeDconfig.get(0);
                            createComposite3D(sashForm, null, state);
                        } else {
                            new CompositeContainer(sashForm, false);
                        }
                    } else {
                        // MARK Load the configuration of multiple 3D windows
                        applyC3DStatesOnStartup(threeDconfig);
                    }
                }

                int width = windowState.getWindowState().getSizeAndPosition().width;
                int[] sashSize = windowState.getLeftSashWidth();
                if (sashSize == null) {
                    sashForm.setWeights(new int[] { width / 3, width - width / 3 });
                } else {
                    try {
                        sashForm.setWeights(sashSize);
                    } catch (IllegalArgumentException iae) {
                        sashForm.setWeights(new int[] { width / 3, width - width / 3 });
                    }
                }
            }
        }
        status = new Composite(container, Cocoa.getStyle());
        status.setLayoutData(BorderLayout.SOUTH);
        RowLayout rlStatusBar = new RowLayout(SWT.HORIZONTAL);
        rlStatusBar.center = true;
        status.setLayout(rlStatusBar);
        {
            Label lblStatus = new Label(status, Cocoa.getStyle());
            lblStatus.setText(""); //$NON-NLS-1$
        }

        if (userSettings.getTextWinArr() == TEXT_RIGHT_3D_LEFT) {
            Composite containerTextEditor = new Composite(containerTop, Cocoa.getStyle());
            containerTextEditor.setLayout(new BorderLayout(0, 0));
            EditorTextWindow tWin = new EditorTextWindow(containerTextEditor, this);
            tWin.build();
            tWin.getTabFolder().setWindow(this);
            tWin.registerEvents();
            Project.getOpenTextWindows().add(tWin);
        }

        if (containerTop instanceof SashForm) {
            this.editorSashForm[0] = (SashForm) containerTop;
            int width = windowState.getWindowState().getSizeAndPosition().width;
            int[] sashSize = windowState.getEditorSashWeights();
            if (sashSize == null) {
                ((SashForm) containerTop).setWeights(new int[] { width / 2, width / 2 });
            } else {
                try {
                    ((SashForm) containerTop).setWeights(sashSize);
                    Editor3DWindow.sashWeight1 = sashSize[0];
                    Editor3DWindow.sashWeight2 = sashSize[1];
                } catch (IllegalArgumentException iae) {
                    ((SashForm) containerTop).setWeights(new int[] { width / 2, width / 2 });
                }
            }
        }
        return container;
    }

    private ToolItem createToolItemCloseView(ToolItemDrawLocation location, ToolItemDrawMode mode) {
        final Composite target;
        switch (location) {
        case NORTH:
        default:
            target = cmpNorth;
            break;
        case EAST:
            target = cmpEast;
            break;
        case WEST:
            target = cmpWest;
            break;
        }
        ToolItem toolItemCloseView = new ToolItem(target, Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL);
        {
            NButton btnCloseView = new NButton(toolItemCloseView, Cocoa.getStyle());
            this.btnCloseViewPtr[0] = btnCloseView;
            KeyStateManager.addTooltipText(btnCloseView, I18n.E3D_CLOSE_VIEW, Task.CLOSE_VIEW);
            btnCloseView.setImage(ResourceManager.getImage("icon16_closeview.png")); //$NON-NLS-1$
        }
        return toolItemCloseView;
    }

    private ToolItem createToolItemPerspective(ToolItemDrawLocation location, ToolItemDrawMode mode) {
        final Composite target;
        switch (location) {
        case NORTH:
        default:
            target = cmpNorth;
            break;
        case EAST:
            target = cmpEast;
            break;
        case WEST:
            target = cmpWest;
            break;
        }
        ToolItem toolItemViewPerspective = new ToolItem(target, Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL);
        {
            NButton btnPerspectiveFront = new NButton(toolItemViewPerspective, Cocoa.getStyle());
            this.btnPerspectiveFrontPtr[0] = btnPerspectiveFront;
            KeyStateManager.addTooltipText(btnPerspectiveFront, I18n.PERSPECTIVE_FRONT, Task.PERSPECTIVE_FRONT);
            btnPerspectiveFront.setImage(ResourceManager.getImage("icon16_front.png")); //$NON-NLS-1$
        }
        {
            NButton btnPerspective = new NButton(toolItemViewPerspective, Cocoa.getStyle());
            this.btnPerspectiveBackPtr[0] = btnPerspective;
            KeyStateManager.addTooltipText(btnPerspective, I18n.PERSPECTIVE_BACK, Task.PERSPECTIVE_BACK);
            btnPerspective.setImage(ResourceManager.getImage("icon16_back.png")); //$NON-NLS-1$
        }
        {
            NButton btnPerspective = new NButton(toolItemViewPerspective, Cocoa.getStyle());
            this.btnPerspectiveLeftPtr[0] = btnPerspective;
            KeyStateManager.addTooltipText(btnPerspective, I18n.PERSPECTIVE_LEFT, Task.PERSPECTIVE_LEFT);
            btnPerspective.setImage(ResourceManager.getImage("icon16_left.png")); //$NON-NLS-1$
        }
        {
            NButton btnPerspective = new NButton(toolItemViewPerspective, Cocoa.getStyle());
            this.btnPerspectiveRightPtr[0] = btnPerspective;
            KeyStateManager.addTooltipText(btnPerspective, I18n.PERSPECTIVE_RIGHT, Task.PERSPECTIVE_RIGHT);
            btnPerspective.setImage(ResourceManager.getImage("icon16_right.png")); //$NON-NLS-1$
        }
        {
            NButton btnPerspective = new NButton(toolItemViewPerspective, Cocoa.getStyle());
            this.btnPerspectiveTopPtr[0] = btnPerspective;
            KeyStateManager.addTooltipText(btnPerspective, I18n.PERSPECTIVE_TOP, Task.PERSPECTIVE_TOP);
            btnPerspective.setImage(ResourceManager.getImage("icon16_top.png")); //$NON-NLS-1$
        }
        {
            NButton btnPerspective = new NButton(toolItemViewPerspective, Cocoa.getStyle());
            this.btnPerspectiveBottomPtr[0] = btnPerspective;
            KeyStateManager.addTooltipText(btnPerspective, I18n.PERSPECTIVE_BOTTOM, Task.PERSPECTIVE_BOTTOM);
            btnPerspective.setImage(ResourceManager.getImage("icon16_bottom.png")); //$NON-NLS-1$
        }
        {
            NButton btnPerspective = new NButton(toolItemViewPerspective, Cocoa.getStyle());
            this.btnPerspectiveTwoThirdsPtr[0] = btnPerspective;
            KeyStateManager.addTooltipText(btnPerspective, I18n.PERSPECTIVE_TWO_THIRDS, Task.PERSPECTIVE_TWO_THIRDS);
            btnPerspective.setImage(ResourceManager.getImage("icon16_twoThirds.png")); //$NON-NLS-1$
        }
        return toolItemViewPerspective;
    }

    private ToolItem createToolItemRenderMode(ToolItemDrawLocation location, ToolItemDrawMode mode) {
        final Composite target;
        switch (location) {
        case NORTH:
        default:
            target = cmpNorth;
            break;
        case EAST:
            target = cmpEast;
            break;
        case WEST:
            target = cmpWest;
            break;
        }
        ToolItem toolItemRenderMode = new ToolItem(target, Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL);
        {
            NButton btnRenderModeNoBackfaceCulling = new NButton(toolItemRenderMode, Cocoa.getStyle());
            this.btnRenderModeNoBackfaceCullingPtr[0] = btnRenderModeNoBackfaceCulling;
            KeyStateManager.addTooltipText(btnRenderModeNoBackfaceCulling, I18n.C3D_NO_BACKFACE_CULLING, Task.RENDERMODE_NO_BACKFACE_CULLING);
            btnRenderModeNoBackfaceCulling.setImage(ResourceManager.getImage("icon16_noBfc.png")); //$NON-NLS-1$
        }
        {
            NButton btnRenderModeRandomColours = new NButton(toolItemRenderMode, Cocoa.getStyle());
            this.btnRenderModeRandomColoursPtr[0] = btnRenderModeRandomColours;
            KeyStateManager.addTooltipText(btnRenderModeRandomColours, I18n.C3D_RANDOM_COLOURS, Task.RENDERMODE_RANDOM_COLOURS);
            btnRenderModeRandomColours.setImage(ResourceManager.getImage("icon16_randomColour.png")); //$NON-NLS-1$
        }
        {
            NButton btnRenderModeGreenRed = new NButton(toolItemRenderMode, Cocoa.getStyle());
            this.btnRenderModeGreenRedPtr[0] = btnRenderModeGreenRed;
            KeyStateManager.addTooltipText(btnRenderModeGreenRed, I18n.C3D_GREEN_RED, Task.RENDERMODE_GREEN_FRONTFACES_RED_BACKFACES);
            btnRenderModeGreenRed.setImage(ResourceManager.getImage("icon16_greenFrontRedBack.png")); //$NON-NLS-1$
        }
        {
            NButton btnRenderModeRedBackfaces = new NButton(toolItemRenderMode, Cocoa.getStyle());
            this.btnRenderModeRedBackfacesPtr[0] = btnRenderModeRedBackfaces;
            KeyStateManager.addTooltipText(btnRenderModeRedBackfaces, I18n.C3D_RED_BACKFACES, Task.RENDERMODE_RED_BACKFACES);
            btnRenderModeRedBackfaces.setImage(ResourceManager.getImage("icon16_redBackfaces.png")); //$NON-NLS-1$
        }
        {
            NButton btnRenderModeRealBackfaceCulling = new NButton(toolItemRenderMode, Cocoa.getStyle());
            this.btnRenderModeRealBackfaceCullingPtr[0] = btnRenderModeRealBackfaceCulling;
            KeyStateManager.addTooltipText(btnRenderModeRealBackfaceCulling, I18n.C3D_REAL_BACKFACE_CULLING, Task.RENDERMODE_REAL_BACKFACE_CULLING);
            btnRenderModeRealBackfaceCulling.setImage(ResourceManager.getImage("icon16_realBfc.png")); //$NON-NLS-1$
        }
        {
            NButton btnRenderModeLDrawStandard = new NButton(toolItemRenderMode, Cocoa.getStyle());
            this.btnRenderModeLDrawStandardPtr[0] = btnRenderModeLDrawStandard;
            KeyStateManager.addTooltipText(btnRenderModeLDrawStandard, I18n.C3D_LDRAW_STANDARD, Task.RENDERMODE_LDRAW_STANDARD);
            btnRenderModeLDrawStandard.setImage(ResourceManager.getImage("icon16_ldrawStandard.png")); //$NON-NLS-1$
        }
        {
            NButton btnRenderModeCondlineMode = new NButton(toolItemRenderMode, Cocoa.getStyle());
            this.btnRenderModeCondlineModePtr[0] = btnRenderModeCondlineMode;
            KeyStateManager.addTooltipText(btnRenderModeCondlineMode, I18n.C3D_CONDLINE_MODE, Task.RENDERMODE_SPECIAL_CONDLINE);
            btnRenderModeCondlineMode.setImage(ResourceManager.getImage("icon16_specialCondline.png")); //$NON-NLS-1$
        }
        {
            NButton btnRenderModeCoplanarityMode = new NButton(toolItemRenderMode, Cocoa.getStyle());
            this.btnRenderModeCoplanarityModePtr[0] = btnRenderModeCoplanarityMode;
            KeyStateManager.addTooltipText(btnRenderModeCoplanarityMode, I18n.C3D_COPLANARITY_MODE, Task.RENDERMODE_COPLANARITY_HEATMAP);
            btnRenderModeCoplanarityMode.setImage(ResourceManager.getImage("icon16_coplanarityHeatmap.png")); //$NON-NLS-1$
        }
        {
            NButton btnRenderModeWireframe = new NButton(toolItemRenderMode, Cocoa.getStyle());
            this.btnRenderModeWireframePtr[0] = btnRenderModeWireframe;
            KeyStateManager.addTooltipText(btnRenderModeWireframe, I18n.C3D_WIREFRAME, Task.RENDERMODE_WIREFRAME);
            btnRenderModeWireframe.setImage(ResourceManager.getImage("icon16_wireframe.png")); //$NON-NLS-1$
        }
        return toolItemRenderMode;
    }

    private ToolItem createToolItemLineThickness(ToolItemDrawLocation location, ToolItemDrawMode mode) {
        final Composite target;
        switch (location) {
        case NORTH:
        default:
            target = cmpNorth;
            break;
        case EAST:
            target = cmpEast;
            break;
        case WEST:
            target = cmpWest;
            break;
        }
        ToolItem toolItemLineThickness = new ToolItem(target, Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL);
        {
            NButton btnLineSize0 = new NButton(toolItemLineThickness, SWT.TOGGLE | Cocoa.getStyle());
            this.btnLineSize0Ptr[0] = btnLineSize0;
            btnLineSize0.setToolTipText(I18n.E3D_LINE_SIZE_0);
            btnLineSize0.setImage(ResourceManager.getImage("icon16_linesize0.png")); //$NON-NLS-1$
        }
        {
            NButton btnLineSize1 = new NButton(toolItemLineThickness, SWT.TOGGLE | Cocoa.getStyle());
            this.btnLineSize1Ptr[0] = btnLineSize1;
            btnLineSize1.setToolTipText(I18n.E3D_LINE_SIZE_1);
            btnLineSize1.setImage(ResourceManager.getImage("icon16_linesize1.png")); //$NON-NLS-1$
        }
        {
            NButton btnLineSize2 = new NButton(toolItemLineThickness, SWT.TOGGLE | Cocoa.getStyle());
            this.btnLineSize2Ptr[0] = btnLineSize2;
            btnLineSize2.setToolTipText(I18n.E3D_LINE_SIZE_2);
            btnLineSize2.setImage(ResourceManager.getImage("icon16_linesize2.png")); //$NON-NLS-1$
        }
        {
            NButton btnLineSize3 = new NButton(toolItemLineThickness, SWT.TOGGLE | Cocoa.getStyle());
            btnLineSize3.setSelection(true);
            this.btnLineSize3Ptr[0] = btnLineSize3;
            btnLineSize3.setToolTipText(I18n.E3D_LINE_SIZE_3);
            btnLineSize3.setImage(ResourceManager.getImage("icon16_linesize3.png")); //$NON-NLS-1$
        }
        {
            NButton btnLineSize4 = new NButton(toolItemLineThickness, SWT.TOGGLE | Cocoa.getStyle());
            this.btnLineSize4Ptr[0] = btnLineSize4;
            btnLineSize4.setToolTipText(I18n.E3D_LINE_SIZE_4);
            btnLineSize4.setImage(ResourceManager.getImage("icon16_linesize4.png")); //$NON-NLS-1$
        }
        if (WorkbenchManager.getUserSettingState().getOpenGLVersion() == 20) {
            NButton btnToggleLinesOpenGL = new NButton(target, SWT.TOGGLE | Cocoa.getStyle());
            this.btnToggleLinesOpenGLPtr[0] = btnToggleLinesOpenGL;
            btnToggleLinesOpenGL.setToolTipText(I18n.E3D_LINE_OPENGL);
            btnToggleLinesOpenGL.setImage(ResourceManager.getImage("icon16_gllines.png")); //$NON-NLS-1$
        }
        return toolItemLineThickness;
    }

    private ToolItem createToolItemColours(ToolItemDrawLocation location, ToolItemDrawMode mode) {
        final Composite target;
        switch (location) {
        case NORTH:
        default:
            target = cmpNorth;
            break;
        case EAST:
            target = cmpEast;
            break;
        case WEST:
            target = cmpWest;
            break;
        }
        ToolItem toolItemColours = new ToolItem(target, Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL);
        toolItemColourBar = toolItemColours;
        List<GColour> colours = WorkbenchManager.getUserSettingState().getUserPalette();

        {
            final int size = colours.size();
            for (int i = 0; i < size; i++) {
                addColorButton(toolItemColours, colours.get(i), i);
            }
        }

        {
            NButton btnPalette = new NButton(toolItemColours, Cocoa.getStyle());
            this.btnPalettePtr[0] = btnPalette;
            btnPalette.setToolTipText(I18n.E3D_MORE);
            btnPalette.setImage(ResourceManager.getImage("icon16_colours.png")); //$NON-NLS-1$
        }

        ToolItem toolItemColourFunctions = new ToolItem(target, Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL);
        {
            final int imgSize = IconSize.getImageSizeFromIconSize();
            NButton btnLastUsedColour = new NButton(toolItemColourFunctions, Cocoa.getStyle());
            this.btnLastUsedColourPtr[0] = btnLastUsedColour;
            btnLastUsedColour.setToolTipText(I18n.E3D_COLOUR_16);
            btnLastUsedColour.setImage(ResourceManager.getImage("icon16_fullTransparent.png")); //$NON-NLS-1$

            final GColour col16 = View.getLDConfigColour(16);
            final Color col = SWTResourceManager.getColor((int) (255f * col16.getR()), (int) (255f * col16.getG()), (int) (255f * col16.getB()));
            final Point size = btnLastUsedColour.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            final int x = Math.round(size.x / 5f);
            final int y = Math.round(size.y / 5f);
            final int w = Math.round(size.x * (3f / 5f));
            final int h = Math.round(size.y * (3f / 5f));
            btnLastUsedColour.addPaintListener(e -> {
                e.gc.setBackground(col);
                e.gc.fillRectangle(x, y, w, h);
                e.gc.drawImage(ResourceManager.getImage("icon16_transparent.png"), 0, 0, imgSize, imgSize, x, y, w, h); //$NON-NLS-1$
            });
            widgetUtil(btnLastUsedColour).addSelectionListener(e -> {
                if (Project.getFileToEdit() != null) {
                    Project.getFileToEdit().getVertexManager().addSnapshot();
                    GColour col1 = View.getLDConfigColour(16);
                    Project.getFileToEdit().getVertexManager().colourChangeSelection(16, col1.getR(), col1.getG(), col1.getB(), 1f, true);
                }
                Editor3DWindow.getWindow().regainFocus();
            });
        }
        {
            NButton btnPipette = new NButton(toolItemColourFunctions, Cocoa.getStyle());
            this.btnPipettePtr[0] = btnPipette;
            btnPipette.setToolTipText(I18n.E3D_PIPETTE);
            btnPipette.setImage(ResourceManager.getImage("icon16_pipette.png")); //$NON-NLS-1$
        }
        {
            NButton btnDecolour = new NButton(toolItemColourFunctions, Cocoa.getStyle());
            this.btnDecolourPtr[0] = btnDecolour;
            btnDecolour.setToolTipText(I18n.E3D_DECOLOUR);
            btnDecolour.setImage(ResourceManager.getImage("icon16_uncolour.png")); //$NON-NLS-1$
        }
        return toolItemColourFunctions;
    }

    private ToolItem createToolItemAdd(ToolItemDrawLocation location, ToolItemDrawMode mode) {
        final Composite target;
        switch (location) {
        case NORTH:
        default:
            target = cmpNorth;
            break;
        case EAST:
            target = cmpEast;
            break;
        case WEST:
            target = cmpWest;
            break;
        }
        ToolItem toolItemAdd = new ToolItem(target, Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL);

        NButton btnAddComment = new NButton(toolItemAdd, Cocoa.getStyle());
        this.btnAddCommentPtr[0] = btnAddComment;
        KeyStateManager.addTooltipText(btnAddComment, I18n.E3D_ADD_COMMENT, Task.ADD_COMMENTS);
        btnAddComment.setImage(ResourceManager.getImage("icon16_addcomment.png")); //$NON-NLS-1$

        NButton btnAddVertex = new NButton(toolItemAdd, SWT.TOGGLE | Cocoa.getStyle());
        this.btnAddVertexPtr[0] = btnAddVertex;
        KeyStateManager.addTooltipText(btnAddVertex, I18n.E3D_ADD_VERTEX, Task.ADD_VERTEX);
        btnAddVertex.setImage(ResourceManager.getImage("icon16_addvertex.png")); //$NON-NLS-1$

        NButton btnAddPrimitive = new NButton(toolItemAdd, SWT.TOGGLE | Cocoa.getStyle());
        this.btnAddPrimitivePtr[0] = btnAddPrimitive;
        btnAddPrimitive.setToolTipText(I18n.E3D_ADD_SUBPART);
        btnAddPrimitive.setImage(ResourceManager.getImage("icon16_addprimitive.png")); //$NON-NLS-1$

        NButton btnAddLine = new NButton(toolItemAdd, SWT.TOGGLE | Cocoa.getStyle());
        this.btnAddLinePtr[0] = btnAddLine;
        KeyStateManager.addTooltipText(btnAddLine, I18n.E3D_ADD_LINE, Task.ADD_LINE);
        btnAddLine.setImage(ResourceManager.getImage("icon16_addline.png")); //$NON-NLS-1$

        NButton btnAddTriangle = new NButton(toolItemAdd, SWT.TOGGLE | Cocoa.getStyle());
        this.btnAddTrianglePtr[0] = btnAddTriangle;
        KeyStateManager.addTooltipText(btnAddTriangle, I18n.E3D_ADD_TRIANGLE, Task.ADD_TRIANGLE);
        btnAddTriangle.setImage(ResourceManager.getImage("icon16_addtriangle.png")); //$NON-NLS-1$

        NButton btnAddQuad = new NButton(toolItemAdd, SWT.TOGGLE | Cocoa.getStyle());
        this.btnAddQuadPtr[0] = btnAddQuad;
        KeyStateManager.addTooltipText(btnAddQuad, I18n.E3D_ADD_QUAD, Task.ADD_QUAD);
        btnAddQuad.setImage(ResourceManager.getImage("icon16_addquad.png")); //$NON-NLS-1$

        NButton btnAddCondline = new NButton(toolItemAdd, SWT.TOGGLE | Cocoa.getStyle());
        this.btnAddCondlinePtr[0] = btnAddCondline;
        KeyStateManager.addTooltipText(btnAddCondline, I18n.E3D_ADD_CONDLINE, Task.ADD_CONDLINE);
        btnAddCondline.setImage(ResourceManager.getImage("icon16_addcondline.png")); //$NON-NLS-1$

        NButton btnAddDistance = new NButton(toolItemAdd, SWT.TOGGLE | Cocoa.getStyle());
        this.btnAddDistancePtr[0] = btnAddDistance;
        KeyStateManager.addTooltipText(btnAddDistance, I18n.E3D_ADD_DISTANCE, Task.ADD_DISTANCE);
        btnAddDistance.setImage(ResourceManager.getImage("icon16_adddistance.png")); //$NON-NLS-1$

        NButton btnAddProtractor = new NButton(toolItemAdd, SWT.TOGGLE | Cocoa.getStyle());
        this.btnAddProtractorPtr[0] = btnAddProtractor;
        KeyStateManager.addTooltipText(btnAddProtractor, I18n.E3D_ADD_PROTRACTOR, Task.ADD_PROTRACTOR);
        btnAddProtractor.setImage(ResourceManager.getImage("icon16_addprotractor.png")); //$NON-NLS-1$

        return toolItemAdd;
    }

    private ToolItem createToolItemInsertAtCursorPosition(ToolItemDrawLocation location, ToolItemDrawMode mode) {
        final Composite target;
        switch (location) {
        case NORTH:
        default:
            target = cmpNorth;
            break;
        case EAST:
            target = cmpEast;
            break;
        case WEST:
            target = cmpWest;
            break;
        }
        ToolItem toolItemInsertAtCursorPosition = new ToolItem(target, Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL);
        NButton btnInsertAtCursorPosition = new NButton(toolItemInsertAtCursorPosition, SWT.TOGGLE | Cocoa.getStyle());
        this.btnInsertAtCursorPositionPtr[0] = btnInsertAtCursorPosition;
        KeyStateManager.addTooltipText(btnInsertAtCursorPosition, I18n.E3D_INSERT_AT_CURSOR_POSITION, Task.INSERT_AT_CURSOR);
        btnInsertAtCursorPosition.setImage(ResourceManager.getImage("icon16_insertAtCursor.png")); //$NON-NLS-1$
        return toolItemInsertAtCursorPosition;
    }

    private ToolItem createToolItemMode(ToolItemDrawLocation location, ToolItemDrawMode mode) {
        final Composite target;
        switch (location) {
        case NORTH:
        default:
            target = cmpNorth;
            break;
        case EAST:
            target = cmpEast;
            break;
        case WEST:
            target = cmpWest;
            break;
        }
        ToolItem toolItemMode = new ToolItem(target, Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL);
        {
            NButton btnVertices = new NButton(toolItemMode, SWT.TOGGLE | Cocoa.getStyle());
            this.btnVerticesPtr[0] = btnVertices;
            KeyStateManager.addTooltipText(btnVertices, I18n.E3D_MODE_VERTEX + I18n.E3D_ALT_TO_DESELECT, Task.OBJ_VERTEX);
            btnVertices.setSelection(true);
            btnVertices.setImage(ResourceManager.getImage("icon16_vertices.png")); //$NON-NLS-1$
        }
        {
            NButton btnTrisNQuads = new NButton(toolItemMode, SWT.TOGGLE | Cocoa.getStyle());
            this.btnTrisNQuadsPtr[0] = btnTrisNQuads;
            KeyStateManager.addTooltipText(btnTrisNQuads, I18n.E3D_MODE_SURFACE + I18n.E3D_ALT_TO_DESELECT, Task.OBJ_FACE);
            btnTrisNQuads.setImage(ResourceManager.getImage("icon16_trisNquads.png")); //$NON-NLS-1$
        }
        {
            NButton btnLines = new NButton(toolItemMode, SWT.TOGGLE | Cocoa.getStyle());
            this.btnLinesPtr[0] = btnLines;
            KeyStateManager.addTooltipText(btnLines, I18n.E3D_MODE_LINE + I18n.E3D_ALT_TO_DESELECT, Task.OBJ_LINE);
            btnLines.setImage(ResourceManager.getImage("icon16_lines.png")); //$NON-NLS-1$
        }
        {
            NButton btnSubfiles = new NButton(toolItemMode, SWT.TOGGLE | Cocoa.getStyle());
            this.btnSubfilesPtr[0] = btnSubfiles;
            KeyStateManager.addTooltipText(btnSubfiles, I18n.E3D_MODE_SUBPART + I18n.E3D_ALT_TO_DESELECT, Task.OBJ_PRIMITIVE);
            btnSubfiles.setImage(ResourceManager.getImage("icon16_primitives.png")); //$NON-NLS-1$
        }
        return toolItemMode;
    }

    private ToolItem createToolItemCCPD(ToolItemDrawLocation location, ToolItemDrawMode mode) {
        final Composite target;
        switch (location) {
        case NORTH:
        default:
            target = cmpNorth;
            break;
        case EAST:
            target = cmpEast;
            break;
        case WEST:
            target = cmpWest;
            break;
        }
        ToolItem toolItemCCPD = new ToolItem(target, Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL);
        {
            NButton btnCut = new NButton(toolItemCCPD, Cocoa.getStyle());
            this.btnCutPtr[0] = btnCut;
            btnCut.setImage(ResourceManager.getImage("icon16_edit-cut.png")); //$NON-NLS-1$
            KeyStateManager.addTooltipText(btnCut, I18n.COPYNPASTE_CUT, Task.CUT);
        }
        {
            NButton btnCopy = new NButton(toolItemCCPD, Cocoa.getStyle());
            this.btnCopyPtr[0] = btnCopy;
            btnCopy.setImage(ResourceManager.getImage("icon16_edit-copy.png")); //$NON-NLS-1$
            KeyStateManager.addTooltipText(btnCopy, I18n.COPYNPASTE_COPY, Task.COPY);
        }
        {
            NButton btnPaste = new NButton(toolItemCCPD, Cocoa.getStyle());
            this.btnPastePtr[0] = btnPaste;
            btnPaste.setImage(ResourceManager.getImage("icon16_edit-paste.png")); //$NON-NLS-1$
            KeyStateManager.addTooltipText(btnPaste, I18n.COPYNPASTE_PASTE, Task.PASTE);
        }
        {
            NButton btnDelete = new NButton(toolItemCCPD, Cocoa.getStyle());
            this.btnDeletePtr[0] = btnDelete;
            btnDelete.setImage(ResourceManager.getImage("icon16_delete.png")); //$NON-NLS-1$
            KeyStateManager.addTooltipText(btnDelete, I18n.COPYNPASTE_DELETE, Task.DELETE);
        }
        return toolItemCCPD;
    }

    private ToolItem createToolItemMiscClick() {
        ToolItem toolItemMiscClick = new ToolItem(cmpNorth, Cocoa.getStyle(), true);
        {
            NButton btnShowSelectionInTextEditor = new NButton(toolItemMiscClick, Cocoa.getStyle());
            this.btnShowSelectionInTextEditorPtr[0] = btnShowSelectionInTextEditor;
            btnShowSelectionInTextEditor.setToolTipText(I18n.C3D_SHOW_IN_TEXT);
            btnShowSelectionInTextEditor.setImage(ResourceManager.getImage("icon16_selection2text.png")); //$NON-NLS-1$
        }
        {
            NButton btnBFCswap = new NButton(toolItemMiscClick, Cocoa.getStyle());
            this.btnBFCswapPtr[0] = btnBFCswap;
            KeyStateManager.addTooltipText(btnBFCswap, I18n.E3D_SWAP_WINDING, Task.SWAP_WINDING);
            btnBFCswap.setImage(ResourceManager.getImage("icon16_bfcSwap.png")); //$NON-NLS-1$
        }
        {
            NButton btnCompileSubfile = new NButton(toolItemMiscClick, Cocoa.getStyle());
            this.btnCompileSubfilePtr[0] = btnCompileSubfile;
            btnCompileSubfile.setToolTipText(I18n.E3D_COMPILE_SUBFILE_DATA);
            btnCompileSubfile.setImage(ResourceManager.getImage("icon16_subcompile.png")); //$NON-NLS-1$
        }
        {
            NButton btnSplitQuad = new NButton(toolItemMiscClick, Cocoa.getStyle());
            this.btnSplitQuadPtr[0] = btnSplitQuad;
            btnSplitQuad.setImage(ResourceManager.getImage("icon16_quadToTri.png")); //$NON-NLS-1$
            btnSplitQuad.setToolTipText(I18n.E3D_SPLIT_QUAD);
        }
        {
            NButton btnMergeQuad = new NButton(toolItemMiscClick, Cocoa.getStyle());
            this.btnMergeQuadPtr[0] = btnMergeQuad;
            btnMergeQuad.setImage(ResourceManager.getImage("icon16_triToquad.png")); //$NON-NLS-1$
            btnMergeQuad.setToolTipText(I18n.EDITORTEXT_MERGE_QUAD);
        }
        {
            NButton btnLineIntersection = new NButton(toolItemMiscClick, Cocoa.getStyle());
            this.btnLineIntersectionPtr[0] = btnLineIntersection;
            btnLineIntersection.setImage(ResourceManager.getImage("icon16_lineintersect.png")); //$NON-NLS-1$
            btnLineIntersection.setToolTipText(I18n.E3D_LINE_INTERSECTION);
        }
        {
            NButton btnCondlineToLine = new NButton(toolItemMiscClick, Cocoa.getStyle());
            this.btnCondlineToLinePtr[0] = btnCondlineToLine;
            // FIXME Needs icon!
            btnCondlineToLine.setText("C2L"); //$NON-NLS-1$
            btnCondlineToLine.setToolTipText(I18n.E3D_CONDLINE_TO_LINE);
        }
        {
            NButton btnLineToCondline = new NButton(toolItemMiscClick, Cocoa.getStyle());
            this.btnLineToCondlinePtr[0] = btnLineToCondline;
            // FIXME Needs icon!
            btnLineToCondline.setText("L2C"); //$NON-NLS-1$
            btnLineToCondline.setToolTipText(I18n.E3D_LINE_TO_CONDLINE);
        }
        {
            NButton btnMoveOnLine = new NButton(toolItemMiscClick, Cocoa.getStyle());
            this.btnMoveOnLinePtr[0] = btnMoveOnLine;
            // FIXME Needs icon!
            btnMoveOnLine.setText("ML"); //$NON-NLS-1$
            btnMoveOnLine.setToolTipText(I18n.E3D_MOVE_ON_LINE);
        }
        {
            NButton btnRoundSelection = new NButton(toolItemMiscClick, Cocoa.getStyle());
            this.btnRoundSelectionPtr[0] = btnRoundSelection;
            btnRoundSelection.setToolTipText(I18n.E3D_ROUND + Cocoa.replaceCtrlByCmd(I18n.E3D_CONTROL_CLICK_MODIFY));
            btnRoundSelection.setImage(ResourceManager.getImage("icon16_round.png")); //$NON-NLS-1$
        }
        {
            final NButton btnSelect = new NButton(toolItemMiscClick, SWT.PUSH | Cocoa.getStyle());
            this.btnSelect2Ptr[0] = btnSelect;
            btnSelect.setToolTipText(I18n.E3D_ADVANCED_SELECT);
            btnSelect.setText(I18n.E3D_ADVANCED_SELECT);
            this.mnuSelect = new Menu(this.getShell(), SWT.POP_UP);
            widgetUtil(btnSelect).addSelectionListener(e -> {
                showSelectMenu();
                Editor3DWindow.getWindow().regainFocus();
            });
            {
                {
                    MenuItem mntmSelectAll = new MenuItem(mnuSelect, SWT.PUSH);
                    this.mntmSelectAllPtr[0] = mntmSelectAll;
                    KeyStateManager.addKeyText(mntmSelectAll, I18n.E3D_ALL, Task.SELECT_ALL);
                }
                {
                    MenuItem mntmSelectNone = new MenuItem(mnuSelect, SWT.PUSH);
                    this.mntmSelectNonePtr[0] = mntmSelectNone;
                    KeyStateManager.addKeyText(mntmSelectNone, I18n.E3D_NONE, Task.SELECT_NONE);
                }
                new MenuItem(mnuSelect, SWT.SEPARATOR);
                {
                    MenuItem mntmSelectInverse = new MenuItem(mnuSelect, SWT.PUSH);
                    this.mntmSelectInversePtr[0] = mntmSelectInverse;
                    mntmSelectInverse.setText(I18n.E3D_INVERSE);
                }
                new MenuItem(mnuSelect, SWT.SEPARATOR);
                {
                    MenuItem mntmSelectAllVisible = new MenuItem(mnuSelect, SWT.PUSH);
                    this.mntmSelectAllVisiblePtr[0] = mntmSelectAllVisible;
                    mntmSelectAllVisible.setText(I18n.E3D_ALL_SHOWN);
                }
                {
                    MenuItem mntmSelectAllWithColours = new MenuItem(mnuSelect, SWT.PUSH);
                    this.mntmSelectAllWithColoursPtr[0] = mntmSelectAllWithColours;
                    KeyStateManager.addKeyText(mntmSelectAllWithColours, I18n.E3D_ALL_SAME_COLOURS, Task.SELECT_ALL_WITH_SAME_COLOURS);
                }
                {
                    MenuItem mntmSelectAllVisibleWithColours = new MenuItem(mnuSelect, SWT.PUSH);
                    this.mntmSelectAllVisibleWithColoursPtr[0] = mntmSelectAllVisibleWithColours;
                    mntmSelectAllVisibleWithColours.setText(I18n.E3D_ALL_SAME_COLOURS_SHOWN);
                }
                new MenuItem(mnuSelect, SWT.SEPARATOR);
                {
                    MenuItem mntmSelectEverything = new MenuItem(mnuSelect, SWT.PUSH);
                    this.mntmSelectEverythingPtr[0] = mntmSelectEverything;
                    mntmSelectEverything.setText(I18n.E3D_EVERYTHING);
                    mntmSelectEverything.setEnabled(false);
                }
                {
                    MenuItem mntmSelectConnected = new MenuItem(mnuSelect, SWT.PUSH);
                    this.mntmSelectConnectedPtr[0] = mntmSelectConnected;
                    KeyStateManager.addKeyText(mntmSelectConnected, I18n.E3D_CONNECTED, Task.SELECT_CONNECTED);
                }
                {
                    MenuItem mntmSelectTouching = new MenuItem(mnuSelect, SWT.PUSH);
                    this.mntmSelectTouchingPtr[0] = mntmSelectTouching;
                    KeyStateManager.addKeyText(mntmSelectTouching, I18n.E3D_TOUCHING, Task.SELECT_TOUCHING);
                }
                {
                    MenuItem mntmWithSameColour = new MenuItem(mnuSelect, SWT.CHECK);
                    this.mntmWithSameColourPtr[0] = mntmWithSameColour;
                    KeyStateManager.addKeyText(mntmWithSameColour, I18n.E3D_WITH_SAME_COLOUR, Task.SELECT_OPTION_WITH_SAME_COLOURS);
                }
                {
                    MenuItem mntmWithSameType = new MenuItem(mnuSelect, SWT.CHECK);
                    this.mntmWithSameTypePtr[0] = mntmWithSameType;
                    mntmWithSameType.setText(I18n.E3D_WITH_SAME_TYPE);
                }
                {
                    MenuItem mntmWithSameOrientation = new MenuItem(mnuSelect, SWT.CHECK);
                    this.mntmWithSameOrientationPtr[0] = mntmWithSameOrientation;
                    mntmWithSameOrientation.setText(I18n.E3D_WITH_SAME_ORIENTATION);
                }
                {
                    MenuItem mntmWithAccuracy = new MenuItem(mnuSelect, SWT.CHECK);
                    this.mntmWithAccuracyPtr[0] = mntmWithAccuracy;
                    mntmWithAccuracy.setText(I18n.E3D_WITH_ACCURACY);
                }
                {
                    MenuItem mntmWithAdjacency = new MenuItem(mnuSelect, SWT.CHECK);
                    this.mntmWithAdjacencyPtr[0] = mntmWithAdjacency;
                    mntmWithAdjacency.setText(I18n.E3D_WITH_ADJACENCY);
                }
                {
                    MenuItem mntmWithHiddenData = new MenuItem(mnuSelect, SWT.CHECK);
                    this.mntmWithHiddenDataPtr[0] = mntmWithHiddenData;
                    mntmWithHiddenData.setText(I18n.E3D_WHAT_IS_HIDDEN);
                    mntmWithHiddenData.setImage(ResourceManager.getImage("icon16_hide.png")); //$NON-NLS-1$
                }
                {
                    MenuItem mntmExceptSubfiles = new MenuItem(mnuSelect, SWT.CHECK);
                    this.mntmExceptSubfilesPtr[0] = mntmExceptSubfiles;
                    mntmExceptSubfiles.setText(I18n.E3D_EXCEPT_SUBFILE);
                }
                {
                    MenuItem mntmWithWholeSubfiles = new MenuItem(mnuSelect, SWT.CHECK);
                    this.mntmWithWholeSubfilesPtr[0] = mntmWithWholeSubfiles;
                    mntmWithWholeSubfiles.setText(I18n.E3D_WITH_WHOLE_SUBFILE_SELECTION);
                }
                {
                    MenuItem mntmStopAtEdges = new MenuItem(mnuSelect, SWT.CHECK);
                    this.mntmStopAtEdgesPtr[0] = mntmStopAtEdges;
                    mntmStopAtEdges.setText(I18n.E3D_STOP_SELECTION_AT_EDGES);
                }
                new MenuItem(mnuSelect, SWT.SEPARATOR);
                {
                    MenuItem mntmSelectSingleVertex = new MenuItem(mnuSelect, SWT.PUSH);
                    this.mntmSelectSingleVertexPtr[0] = mntmSelectSingleVertex;
                    mntmSelectSingleVertex.setText(I18n.E3D_SELECT_VERTEX);
                }
                {
                    MenuItem mntmSelectIsolatedVertices = new MenuItem(mnuSelect, SWT.PUSH);
                    this.mntmSelectIsolatedVerticesPtr[0] = mntmSelectIsolatedVertices;
                    mntmSelectIsolatedVertices.setText(I18n.E3D_ISOLATED_VERTICES);
                }
                new MenuItem(mnuSelect, SWT.SEPARATOR);
                {
                    MenuItem mntmSVertices = new MenuItem(mnuSelect, SWT.CHECK);
                    this.mntmSVerticesPtr[0] = mntmSVertices;
                    mntmSVertices.setText(I18n.E3D_VERTICES);
                    mntmSVertices.setSelection(true);
                }
                {
                    MenuItem mntmSLines = new MenuItem(mnuSelect, SWT.CHECK);
                    this.mntmSLinesPtr[0] = mntmSLines;
                    mntmSLines.setText(I18n.E3D_LINES);
                    mntmSLines.setSelection(true);
                }
                {
                    MenuItem mntmSTriangles = new MenuItem(mnuSelect, SWT.CHECK);
                    this.mntmSTrianglesPtr[0] = mntmSTriangles;
                    mntmSTriangles.setText(I18n.E3D_TRIANGLES);
                    mntmSTriangles.setSelection(true);
                }
                {
                    MenuItem mntmSQuads = new MenuItem(mnuSelect, SWT.CHECK);
                    this.mntmSQuadsPtr[0] = mntmSQuads;
                    mntmSQuads.setText(I18n.E3D_QUADS);
                    mntmSQuads.setSelection(true);
                }
                {
                    MenuItem mntmSCLines = new MenuItem(mnuSelect, SWT.CHECK);
                    this.mntmSCLinesPtr[0] = mntmSCLines;
                    mntmSCLines.setText(I18n.E3D_CONDLINES);
                    mntmSCLines.setSelection(true);
                }
                new MenuItem(mnuSelect, SWT.SEPARATOR);
                {
                    MenuItem mntmSAllTypes = new MenuItem(mnuSelect, SWT.PUSH);
                    this.mntmSAllTypesPtr[0] = mntmSAllTypes;
                    mntmSAllTypes.setText(I18n.E3D_ALL_TYPES);
                }
                {
                    MenuItem mntmSNothing = new MenuItem(mnuSelect, SWT.PUSH);
                    this.mntmSNothingPtr[0] = mntmSNothing;
                    mntmSNothing.setText(I18n.E3D_NOTHING);
                }
                new MenuItem(mnuSelect, SWT.SEPARATOR);
                {
                    MenuItem mntmNeedsThreshold = new MenuItem(mnuSelect, SWT.PUSH);
                    mntmNeedsThreshold.setText(I18n.E3D_NEEDS_A_THRESHOLD);
                    mntmNeedsThreshold.setEnabled(false);
                }
                {
                    MenuItem mntmNoEffect = new MenuItem(mnuSelect, SWT.PUSH);
                    mntmNoEffect.setText(I18n.E3D_NO_EFFECT_SELECT_EVERYTHING);
                    mntmNoEffect.setEnabled(false);
                }
            }
        }
        {
            final NButton btnMergeNSplit = new NButton(toolItemMiscClick, SWT.PUSH | Cocoa.getStyle());
            btnMergeNSplit.setToolTipText(I18n.E3D_MERGE_SPLIT);
            btnMergeNSplit.setText(I18n.E3D_MERGE_SPLIT);
            final Menu mnuMerge = new Menu(this.getShell(), SWT.POP_UP);
            widgetUtil(btnMergeNSplit).addSelectionListener(e -> {
                Point loc = btnMergeNSplit.getLocation();
                Rectangle rect = btnMergeNSplit.getBounds();
                Point mLoc = new Point(loc.x - 1, loc.y + rect.height);
                mnuMerge.setLocation(getShell().getDisplay().map(btnMergeNSplit.getParent(), null, mLoc));
                mnuMerge.setVisible(true);
                Editor3DWindow.getWindow().regainFocus();
            });
            {
                MenuItem mntmFlip = new MenuItem(mnuMerge, SWT.PUSH);
                this.mntmFlipPtr[0] = mntmFlip;
                mntmFlip.setText(I18n.E3D_FLIP_ROTATE);
                KeyStateManager.addKeyText(mntmFlip, I18n.E3D_FLIP_ROTATE, Task.FLIP_ROTATE_VERTICES);
            }
            {
                MenuItem mntmSmooth = new MenuItem(mnuMerge, SWT.PUSH);
                this.mntmSmoothPtr[0] = mntmSmooth;
                mntmSmooth.setText(I18n.E3D_SMOOTH);
            }
            new MenuItem(mnuMerge, SWT.SEPARATOR);
            {
                MenuItem mntmSplit = new MenuItem(mnuMerge, SWT.PUSH);
                this.mntmSplitPtr[0] = mntmSplit;
                KeyStateManager.addKeyText(mntmSplit, I18n.E3D_SPLIT, Task.SPLIT);
            }
            {
                MenuItem mntmSplitNTimes = new MenuItem(mnuMerge, SWT.PUSH);
                this.mntmSplitNTimesPtr[0] = mntmSplitNTimes;
                mntmSplitNTimes.setText(I18n.E3D_SPLIT_N_TIMES);
            }
            new MenuItem(mnuMerge, SWT.SEPARATOR);
            {
                MenuItem mntmMergeTo = new MenuItem(mnuMerge, SWT.PUSH);
                this.mntmMergeToAveragePtr[0] = mntmMergeTo;
                KeyStateManager.addKeyText(mntmMergeTo, I18n.E3D_MERGE_TO_AVG, Task.MERGE_TO_AVERAGE);
            }
            {
                MenuItem mntmMergeTo = new MenuItem(mnuMerge, SWT.PUSH);
                this.mntmMergeToLastSelectedPtr[0] = mntmMergeTo;
                KeyStateManager.addKeyText(mntmMergeTo, I18n.E3D_MERGE_TO_LAST_SELECTED, Task.MERGE_TO_LAST);
            }
            {
                MenuItem mntmMergeTo = new MenuItem(mnuMerge, SWT.PUSH);
                this.mntmMergeToNearestVertexPtr[0] = mntmMergeTo;
                mntmMergeTo.setText(I18n.E3D_MERGE_TO_NEAREST_VERTEX);
            }
            {
                MenuItem mntmMergeTo = new MenuItem(mnuMerge, SWT.PUSH);
                this.mntmMergeToNearestEdgePtr[0] = mntmMergeTo;
                mntmMergeTo.setText(I18n.E3D_MERGE_TO_NEAREST_EDGE);
            }
            {
                MenuItem mntmMergeTo = new MenuItem(mnuMerge, SWT.PUSH);
                this.mntmMergeToNearestEdgeSplitPtr[0] = mntmMergeTo;
                mntmMergeTo.setText(I18n.E3D_MERGE_TO_NEAREST_EDGE_SPLIT);
            }
            {
                MenuItem mntmMergeTo = new MenuItem(mnuMerge, SWT.PUSH);
                this.mntmMergeToNearestFacePtr[0] = mntmMergeTo;
                mntmMergeTo.setText(I18n.E3D_MERGE_TO_NEAREST_FACE);
            }
            new MenuItem(mnuMerge, SWT.SEPARATOR);
            {
                MenuItem mntmMergeTo = new MenuItem(mnuMerge, SWT.PUSH);
                this.mntmMergeToNearestFaceDirPtr[0] = mntmMergeTo;
                mntmMergeTo.setText(I18n.E3D_MERGE_TO_NEAREST_FACE_DIR);
            }
            new MenuItem(mnuMerge, SWT.SEPARATOR);
            {
                MenuItem mntmSetXYZ = new MenuItem(mnuMerge, SWT.PUSH);
                this.mntmSetXYZPtr[0] = mntmSetXYZ;
                mntmSetXYZ.setText(I18n.E3D_SET_XYZ);
            }
            {
                MenuItem mntmTranslate = new MenuItem(mnuMerge, SWT.PUSH);
                this.mntmTranslatePtr[0] = mntmTranslate;
                mntmTranslate.setText(I18n.E3D_TRANSLATE_SELECTION);
            }
            {
                MenuItem mntmRotate = new MenuItem(mnuMerge, SWT.PUSH);
                this.mntmRotatePtr[0] = mntmRotate;
                mntmRotate.setText(I18n.E3D_ROTATE_SELECTION);
            }
            {
                MenuItem mntmScale = new MenuItem(mnuMerge, SWT.PUSH);
                this.mntmScalePtr[0] = mntmScale;
                mntmScale.setText(I18n.E3D_SCALE_SELECTION);
            }
            new MenuItem(mnuMerge, SWT.SEPARATOR);
            {
                MenuItem mntmSubdivideCatmullClark = new MenuItem(mnuMerge, SWT.PUSH);
                this.mntmSubdivideCatmullClarkPtr[0] = mntmSubdivideCatmullClark;
                mntmSubdivideCatmullClark.setText(I18n.E3D_SUBDIVIDE_CATMULL_CLARK);
            }
            {
                MenuItem mntmSubdivideLoop = new MenuItem(mnuMerge, SWT.PUSH);
                this.mntmSubdivideLoopPtr[0] = mntmSubdivideLoop;
                mntmSubdivideLoop.setText(I18n.E3D_SUBDIVIDE_LOOP);
            }
        }
        {
            final NButton btnToolsActions = new NButton(toolItemMiscClick, SWT.PUSH | Cocoa.getStyle());
            btnToolsActions.setText(I18n.E3D_TOOLS);
            btnToolsActions.setToolTipText(I18n.E3D_TOOLS_OPTIONS);
            final Menu mnuTools = new Menu(this.getShell(), SWT.POP_UP);
            widgetUtil(btnToolsActions).addSelectionListener(e -> {
                Point loc = btnToolsActions.getLocation();
                Rectangle rect = btnToolsActions.getBounds();
                Point mLoc = new Point(loc.x - 1, loc.y + rect.height);
                mnuTools.setLocation(getShell().getDisplay().map(btnToolsActions.getParent(), null, mLoc));
                mnuTools.setVisible(true);
                Editor3DWindow.getWindow().regainFocus();
            });
            {
                {
                    MenuItem mntmPartReview = new MenuItem(mnuTools, SWT.PUSH);
                    this.mntmPartReviewPtr[0] = mntmPartReview;
                    mntmPartReview.setText(I18n.E3D_PART_REVIEW);
                }
                {
                    MenuItem mntmEdger2 = new MenuItem(mnuTools, SWT.PUSH);
                    this.mntmEdger2Ptr[0] = mntmEdger2;
                    mntmEdger2.setText(I18n.E3D_EDGER_2);
                }
                {
                    MenuItem mntmPrimGen2 = new MenuItem(mnuTools, SWT.PUSH);
                    this.mntmPrimGen2Ptr[0] = mntmPrimGen2;
                    mntmPrimGen2.setText(I18n.E3D_PRIMGEN2);
                }
                {
                    MenuItem mntmEdger2 = new MenuItem(mnuTools, SWT.PUSH);
                    this.mntmTxt2DatPtr[0] = mntmEdger2;
                    mntmEdger2.setText(I18n.E3D_TXT_2_DAT);
                }
                {
                    MenuItem mntmRectifier = new MenuItem(mnuTools, SWT.PUSH);
                    this.mntmRectifierPtr[0] = mntmRectifier;
                    mntmRectifier.setText(I18n.E3D_RECTIFIER);
                }
                {
                    MenuItem mntmIsecalc = new MenuItem(mnuTools, SWT.PUSH);
                    this.mntmIsecalcPtr[0] = mntmIsecalc;
                    mntmIsecalc.setText(I18n.E3D_ISECALC);
                }
                {
                    MenuItem mntmSlicerPro = new MenuItem(mnuTools, SWT.PUSH);
                    this.mntmSlicerProPtr[0] = mntmSlicerPro;
                    mntmSlicerPro.setText(I18n.E3D_SLICER_PRO);
                }
                {
                    MenuItem mntmIntersector = new MenuItem(mnuTools, SWT.PUSH);
                    this.mntmIntersectorPtr[0] = mntmIntersector;
                    mntmIntersector.setText(I18n.E3D_INTERSECTOR);
                }
                {
                    MenuItem mntmMatrixCalculator = new MenuItem(mnuTools, SWT.PUSH);
                    this.mntmSlantingMatrixProjectorPtr[0] = mntmMatrixCalculator;
                    mntmMatrixCalculator.setText(I18n.E3D_SLANTING_MATRIX_PROJECTOR);
                }
                {
                    MenuItem mntmLines2Pattern = new MenuItem(mnuTools, SWT.PUSH);
                    this.mntmLines2PatternPtr[0] = mntmLines2Pattern;
                    mntmLines2Pattern.setText(I18n.E3D_LINES_2_PATTERN);
                }
                {
                    MenuItem mntmPathTruder = new MenuItem(mnuTools, SWT.PUSH);
                    this.mntmPathTruderPtr[0] = mntmPathTruder;
                    mntmPathTruder.setText(I18n.E3D_PATH_TRUDER);
                }
                {
                    MenuItem mntmYTruder = new MenuItem(mnuTools, SWT.PUSH);
                    this.mntmYTruderPtr[0] = mntmYTruder;
                    mntmYTruder.setText(I18n.E3D_YTRUDER);
                }
                {
                    MenuItem mntmSymSplitter = new MenuItem(mnuTools, SWT.PUSH);
                    this.mntmSymSplitterPtr[0] = mntmSymSplitter;
                    mntmSymSplitter.setText(I18n.E3D_SYM_SPLITTER);
                }
                {
                    MenuItem mntmUnificator = new MenuItem(mnuTools, SWT.PUSH);
                    this.mntmUnificatorPtr[0] = mntmUnificator;
                    mntmUnificator.setText(I18n.E3D_UNIFICATOR);
                }
                {
                    MenuItem mntmRingsAndCones = new MenuItem(mnuTools, SWT.PUSH);
                    this.mntmRingsAndConesPtr[0] = mntmRingsAndCones;
                    mntmRingsAndCones.setText(I18n.E3D_RINGS_AND_CONES);
                }
                {
                    MenuItem mntmTJunctionFinder = new MenuItem(mnuTools, SWT.PUSH);
                    this.mntmTJunctionFinderPtr[0] = mntmTJunctionFinder;
                    mntmTJunctionFinder.setText(I18n.E3D_T_JUNCTION);
                }
                {
                    MenuItem mntmMeshReducer = new MenuItem(mnuTools, SWT.PUSH);
                    this.mntmMeshReducerPtr[0] = mntmMeshReducer;
                    mntmMeshReducer.setText(I18n.E3D_MESH_REDUCE);
                }
                new MenuItem(mnuTools, SWT.SEPARATOR);
                {
                    final MenuItem mntmLibFeatures = new MenuItem(mnuTools, SWT.CASCADE);
                    mntmLibFeatures.setText(I18n.E3D_LIBRARY_FEATURES);
                    final Menu mnuLibFeatures = new Menu(mntmLibFeatures);
                    mntmLibFeatures.setMenu(mnuLibFeatures);
                    {
                        MenuItem mntmSelectAnotherLDConfig = new MenuItem(mnuLibFeatures, SWT.PUSH);
                        this.mntmSelectAnotherLDConfigPtr[0] = mntmSelectAnotherLDConfig;
                        mntmSelectAnotherLDConfig.setText(I18n.E3D_SELECT_LDCONFIG);
                    }
                    {
                        MenuItem mntmDownloadLDConfig = new MenuItem(mnuLibFeatures, SWT.PUSH);
                        this.mntmDownloadLDConfigPtr[0] = mntmDownloadLDConfig;
                        mntmDownloadLDConfig.setText(I18n.E3D_DOWNLOAD_LD_CONFIG);
                    }
                    {
                        MenuItem mntmDownloadCategories = new MenuItem(mnuLibFeatures, SWT.PUSH);
                        this.mntmDownloadCategoriesPtr[0] = mntmDownloadCategories;
                        mntmDownloadCategories.setText(I18n.E3D_DOWNLOAD_CATEGORIES);
                    }
                }
                {
                    MenuItem mntmOptions = new MenuItem(mnuTools, SWT.PUSH);
                    this.mntmOptionsPtr[0] = mntmOptions;
                    mntmOptions.setText(I18n.E3D_OPTIONS);
                }
                {
                    MenuItem mntmUserConfigSave = new MenuItem(mnuTools, SWT.PUSH);
                    this.mntmUserConfigSavePtr[0] = mntmUserConfigSave;
                    mntmUserConfigSave.setText(I18n.E3D_USER_CONFIG_SAVE);
                }
                {
                    MenuItem mntmUserConfigLoad = new MenuItem(mnuTools, SWT.PUSH);
                    this.mntmUserConfigLoadPtr[0] = mntmUserConfigLoad;
                    mntmUserConfigLoad.setText(I18n.E3D_USER_CONFIG_LOAD);
                }
                {
                    MenuItem mntmResetSettingsOnRestart = new MenuItem(mnuTools, SWT.PUSH);
                    this.mntmResetSettingsOnRestartPtr[0] = mntmResetSettingsOnRestart;
                    mntmResetSettingsOnRestart.setText(I18n.E3D_RESET_ALL);
                }
                {
                    final MenuItem mntmPalette = new MenuItem(mnuTools, SWT.CASCADE);
                    mntmPalette.setText(I18n.E3D_PALETTE);
                    final Menu mnuPalette = new Menu(mntmPalette);
                    mntmPalette.setMenu(mnuPalette);
                    {
                        MenuItem mntmSavePalette = new MenuItem(mnuPalette, SWT.PUSH);
                        this.mntmSavePalettePtr[0] = mntmSavePalette;
                        mntmSavePalette.setText(I18n.E3D_PALETTE_SAVE);
                    }
                    {
                        MenuItem mntmLoadPalette = new MenuItem(mnuPalette, SWT.PUSH);
                        this.mntmLoadPalettePtr[0] = mntmLoadPalette;
                        mntmLoadPalette.setText(I18n.E3D_PALETTE_LOAD);
                    }
                    {
                        MenuItem mntmSetPaletteSize = new MenuItem(mnuPalette, SWT.PUSH);
                        this.mntmSetPaletteSizePtr[0] = mntmSetPaletteSize;
                        mntmSetPaletteSize.setText(I18n.E3D_PALETTE_SET_SIZE);
                    }
                    {
                        MenuItem mntmResetPalette = new MenuItem(mnuPalette, SWT.PUSH);
                        this.mntmResetPalettePtr[0] = mntmResetPalette;
                        mntmResetPalette.setText(I18n.E3D_PALETTE_RESET);
                    }
                }
                {
                    final MenuItem mntmSetIconSize = new MenuItem(mnuTools, SWT.CASCADE);
                    mntmSetIconSize.setText(I18n.E3D_SET_ICON_SIZE);
                    final Menu mnuIconSize = new Menu(mntmSetIconSize);
                    mntmSetIconSize.setMenu(mnuIconSize);
                    final int iconSize = IconSize.getIconsize();
                    {
                        MenuItem mntmIconSize = new MenuItem(mnuIconSize, SWT.RADIO);
                        this.mntmIconSize1Ptr[0] = mntmIconSize;
                        mntmIconSize.setText(I18n.E3D_ICON_SIZE_1);
                        mntmIconSize.setSelection(iconSize == -1);
                    }
                    {
                        MenuItem mntmIconSize = new MenuItem(mnuIconSize, SWT.RADIO);
                        this.mntmIconSize2Ptr[0] = mntmIconSize;
                        mntmIconSize.setText(I18n.E3D_ICON_SIZE_2);
                        mntmIconSize.setSelection(iconSize == 0);
                    }
                    {
                        MenuItem mntmIconSize = new MenuItem(mnuIconSize, SWT.RADIO);
                        this.mntmIconSize3Ptr[0] = mntmIconSize;
                        mntmIconSize.setText(I18n.E3D_ICON_SIZE_3);
                        mntmIconSize.setSelection(iconSize == 1);
                    }
                    {
                        MenuItem mntmIconSize = new MenuItem(mnuIconSize, SWT.RADIO);
                        this.mntmIconSize4Ptr[0] = mntmIconSize;
                        mntmIconSize.setText(I18n.E3D_ICON_SIZE_4);
                        mntmIconSize.setSelection(iconSize == 2);
                    }
                    {
                        MenuItem mntmIconSize = new MenuItem(mnuIconSize, SWT.RADIO);
                        this.mntmIconSize5Ptr[0] = mntmIconSize;
                        mntmIconSize.setText(I18n.E3D_ICON_SIZE_5);
                        mntmIconSize.setSelection(iconSize == 3);
                    }
                    {
                        MenuItem mntmIconSize = new MenuItem(mnuIconSize, SWT.RADIO);
                        this.mntmIconSize6Ptr[0] = mntmIconSize;
                        mntmIconSize.setText(I18n.E3D_ICON_SIZE_6);
                        mntmIconSize.setSelection(iconSize >= 4);
                    }
                    new MenuItem(mnuIconSize, SWT.SEPARATOR);
                    {
                        MenuItem mntmIconSize = new MenuItem(mnuIconSize, SWT.PUSH);
                        mntmIconSize.setText(I18n.E3D_REQUIRES_RESTART);
                        mntmIconSize.setEnabled(false);
                    }
                }
                new MenuItem(mnuTools, SWT.SEPARATOR);
                {
                    MenuItem mntmUploadErrorLog = new MenuItem(mnuTools, SWT.PUSH);
                    this.mntmUploadLogsPtr[0] = mntmUploadErrorLog;
                    mntmUploadErrorLog.setText(I18n.E3D_UPLOAD_ERROR_LOGS);
                }
                new MenuItem(mnuTools, SWT.SEPARATOR);
                {
                    MenuItem mntmAntiAliasing = new MenuItem(mnuTools, SWT.CHECK);
                    mntmAntiAliasing.setSelection(WorkbenchManager.getUserSettingState().isAntiAliasing());
                    this.mntmAntiAliasingPtr[0] = mntmAntiAliasing;
                    mntmAntiAliasing.setText(I18n.E3D_ANTI_ALIASING);
                }
                {
                    MenuItem mntmOpenGL33Engine = new MenuItem(mnuTools, SWT.CHECK);
                    mntmOpenGL33Engine.setSelection(WorkbenchManager.getUserSettingState().isOpenGL33Engine());
                    this.mntmOpenGL33EnginePtr[0] = mntmOpenGL33Engine;
                    mntmOpenGL33Engine.setText(I18n.E3D_NEW_ENGINE);
                }
                if (NLogger.debugging) {
                    MenuItem mntmVulkanEngine = new MenuItem(mnuTools, SWT.CHECK);
                    mntmVulkanEngine.setSelection(WorkbenchManager.getUserSettingState().isVulkanEngine());
                    this.mntmVulkanEnginePtr[0] = mntmVulkanEngine;
                    mntmVulkanEngine.setText(I18n.E3D_VULKAN_ENGINE);
                }
                new MenuItem(mnuTools, SWT.SEPARATOR);
                {
                    MenuItem mntmSyncLpeInline = new MenuItem(mnuTools, SWT.CHECK);
                    mntmSyncLpeInline.setSelection(WorkbenchManager.getUserSettingState().getSyncWithLpeInline().get());
                    this.mntmSyncLpeInlinePtr[0] = mntmSyncLpeInline;
                    mntmSyncLpeInline.setText(I18n.E3D_PARSE_INLINE);
                }
            }
        }
        return toolItemMiscClick;
    }

    private ToolItem createToolItemManipulatorActions(ToolItemDrawLocation location, ToolItemDrawMode mode, ToolItem toolItem) {
        final Composite target;
        switch (location) {
        case NORTH:
        default:
            target = cmpNorth;
            break;
        case EAST:
            target = cmpEast;
            break;
        case WEST:
            target = cmpWest;
            break;
        }
        if (mode == ToolItemDrawMode.DROP_DOWN) {
            if (toolItem == null) {
                toolItem = new ToolItem(target, Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL);;
            }
            final NButton btnManipulatorActions = new NButton(toolItem, SWT.ARROW | SWT.DOWN);
            btnManipulatorActions.setToolTipText(I18n.E3D_MODIFY_MANIPULATOR);
            final Menu mnuManipulator = new Menu(this.getShell(), SWT.POP_UP);
            widgetUtil(btnManipulatorActions).addSelectionListener(e -> {
                Point loc = btnManipulatorActions.getLocation();
                Rectangle rect = btnManipulatorActions.getBounds();
                Point mLoc = new Point(loc.x - 1, loc.y + rect.height);
                mnuManipulator.setLocation(getShell().getDisplay().map(btnManipulatorActions.getParent(), null, mLoc));
                mnuManipulator.setVisible(true);
                Editor3DWindow.getWindow().regainFocus();
            });
            {
                {
                    MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                    this.mntmManipulatorToOriginPtr[0] = btnMani;
                    btnMani.setText(I18n.E3D_MANIPULATOR_TO_ORIGIN);
                    btnMani.setImage(ResourceManager.getImage("icon16_toOrigin.png")); //$NON-NLS-1$
                }
                {
                    MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                    this.mntmManipulatorToWorldPtr[0] = btnMani;
                    btnMani.setText(I18n.E3D_MANIPULATOR_TO_WORLD);
                    btnMani.setImage(ResourceManager.getImage("icon16_toWorld.png")); //$NON-NLS-1$
                }
                {
                    MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                    this.mntmManipulatorAdjustRotationCenterPtr[0] = btnMani;
                    btnMani.setText(I18n.E3D_ADJUST_ROTATION_CENTER);
                    btnMani.setImage(ResourceManager.getImage("icon16_adjustrotationcenter.png")); //$NON-NLS-1$
                }
                new MenuItem(mnuManipulator, SWT.SEPARATOR);
                {
                    MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                    this.mntmManipulatorXReversePtr[0] = btnMani;
                    btnMani.setText(I18n.E3D_REVERSE_X);
                    btnMani.setImage(ResourceManager.getImage("icon16_Xinv.png")); //$NON-NLS-1$
                }
                {
                    MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                    this.mntmManipulatorYReversePtr[0] = btnMani;
                    btnMani.setText(I18n.E3D_REVERSE_Y);
                    btnMani.setImage(ResourceManager.getImage("icon16_Yinv.png")); //$NON-NLS-1$
                }
                {
                    MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                    this.mntmManipulatorZReversePtr[0] = btnMani;
                    btnMani.setText(I18n.E3D_REVERSE_Z);
                    btnMani.setImage(ResourceManager.getImage("icon16_Zinv.png")); //$NON-NLS-1$
                }
                new MenuItem(mnuManipulator, SWT.SEPARATOR);
                {
                    MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                    this.mntmManipulatorSwitchXYPtr[0] = btnMani;
                    btnMani.setText(I18n.E3D_SWAP_XY);
                    btnMani.setImage(ResourceManager.getImage("icon16_XswapY.png")); //$NON-NLS-1$
                }
                {
                    MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                    this.mntmManipulatorSwitchXZPtr[0] = btnMani;
                    btnMani.setText(I18n.E3D_SWAP_XZ);
                    btnMani.setImage(ResourceManager.getImage("icon16_XswapZ.png")); //$NON-NLS-1$
                }
                {
                    MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                    this.mntmManipulatorSwitchYZPtr[0] = btnMani;
                    btnMani.setText(I18n.E3D_SWAP_YZ);
                    btnMani.setImage(ResourceManager.getImage("icon16_YswapZ.png")); //$NON-NLS-1$
                }
                new MenuItem(mnuManipulator, SWT.SEPARATOR);
                {
                    MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                    this.mntmManipulatorCameraToPosPtr[0] = btnMani;
                    btnMani.setText(I18n.E3D_CAM_TO_MANIPULATOR);
                    btnMani.setImage(ResourceManager.getImage("icon16_cameratomanipulator.png")); //$NON-NLS-1$
                }
                {
                    MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                    this.mntmManipulatorToAveragePtr[0] = btnMani;
                    btnMani.setText(I18n.E3D_MANIPULATOR_TO_AVG);
                    btnMani.setImage(ResourceManager.getImage("icon16_toavg.png")); //$NON-NLS-1$
                    KeyStateManager.addKeyText(btnMani, I18n.E3D_MANIPULATOR_TO_AVG, Task.MOVE_TO_AVG);
                }
                new MenuItem(mnuManipulator, SWT.SEPARATOR);
                {
                    MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                    this.mntmManipulatorToSubfilePtr[0] = btnMani;
                    btnMani.setText(I18n.E3D_MANIPULATOR_TO_SUBFILE);
                    btnMani.setImage(ResourceManager.getImage("icon16_tosubfile.png")); //$NON-NLS-1$
                }
                {
                    MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                    this.mntmManipulatorSubfileToPtr[0] = btnMani;
                    btnMani.setText(Cocoa.replaceCtrlByCmd(I18n.E3D_SUBFILE_TO_MANIPULATOR));
                    btnMani.setImage(ResourceManager.getImage("icon16_tosubfile.png")); //$NON-NLS-1$
                }
                new MenuItem(mnuManipulator, SWT.SEPARATOR);
                {
                    MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                    this.mntmManipulatorToVertexPtr[0] = btnMani;
                    btnMani.setText(I18n.E3D_MANIPULATOR_TO_VERTEX);
                    btnMani.setImage(ResourceManager.getImage("icon16_tonearestvertex.png")); //$NON-NLS-1$
                }
                {
                    MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                    this.mntmManipulatorToEdgePtr[0] = btnMani;
                    btnMani.setText(I18n.E3D_MANIPULATOR_TO_EDGE);
                    btnMani.setImage(ResourceManager.getImage("icon16_tonearestedge.png")); //$NON-NLS-1$
                }
                {
                    MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                    this.mntmManipulatorToSurfacePtr[0] = btnMani;
                    btnMani.setText(I18n.E3D_MANIPULATOR_TO_FACE);
                    btnMani.setImage(ResourceManager.getImage("icon16_tonearestface.png")); //$NON-NLS-1$
                }
                new MenuItem(mnuManipulator, SWT.SEPARATOR);
                {
                    MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                    this.mntmManipulatorToVertexNormalPtr[0] = btnMani;
                    btnMani.setText(I18n.E3D_MANIPULATOR_TO_VERTEX_N);
                    btnMani.setImage(ResourceManager.getImage("icon16_tonearestvertexN.png")); //$NON-NLS-1$
                }
                {
                    MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                    this.mntmManipulatorToVertexPositionPtr[0] = btnMani;
                    btnMani.setText(I18n.E3D_MANIPULATOR_TO_VERTEX_P);
                    btnMani.setImage(ResourceManager.getImage("icon16_tonearestvertexN2.png")); //$NON-NLS-1$
                }
                {
                    MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                    this.mntmManipulatorToEdgeNormalPtr[0] = btnMani;
                    btnMani.setText(I18n.E3D_MANIPULATOR_TO_EDGE_N);
                    btnMani.setImage(ResourceManager.getImage("icon16_tonearestedgeN.png")); //$NON-NLS-1$
                }
                {
                    MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                    this.mntmManipulatorToSurfaceNormalPtr[0] = btnMani;
                    btnMani.setText(I18n.E3D_MANIPULATOR_TO_FACE_N);
                    btnMani.setImage(ResourceManager.getImage("icon16_tonearestfaceN.png")); //$NON-NLS-1$
                }
            }
            return null;
        } else {
            ToolItem toolItemManipulatorActions = new ToolItem(target, Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL);
            {
                NButton btnMani = new NButton(toolItemManipulatorActions, Cocoa.getStyle());
                this.btnManipulatorToOriginPtr[0] = btnMani;
                btnMani.setToolTipText(I18n.E3D_MANIPULATOR_TO_ORIGIN);
                btnMani.setImage(ResourceManager.getImage("icon16_toOrigin.png")); //$NON-NLS-1$
            }
            {
                NButton btnMani = new NButton(toolItemManipulatorActions, Cocoa.getStyle());
                this.btnManipulatorToWorldPtr[0] = btnMani;
                btnMani.setToolTipText(I18n.E3D_MANIPULATOR_TO_WORLD);
                btnMani.setImage(ResourceManager.getImage("icon16_toWorld.png")); //$NON-NLS-1$
            }
            {
                NButton btnMani = new NButton(toolItemManipulatorActions, Cocoa.getStyle());
                this.btnManipulatorAdjustRotationCenterPtr[0] = btnMani;
                btnMani.setToolTipText(I18n.E3D_ADJUST_ROTATION_CENTER);
                btnMani.setImage(ResourceManager.getImage("icon16_adjustrotationcenter.png")); //$NON-NLS-1$
            }
            {
                NButton btnMani = new NButton(toolItemManipulatorActions, Cocoa.getStyle());
                this.btnManipulatorXReversePtr[0] = btnMani;
                btnMani.setToolTipText(I18n.E3D_REVERSE_X);
                btnMani.setImage(ResourceManager.getImage("icon16_Xinv.png")); //$NON-NLS-1$
            }
            {
                NButton btnMani = new NButton(toolItemManipulatorActions, Cocoa.getStyle());
                this.btnManipulatorYReversePtr[0] = btnMani;
                btnMani.setToolTipText(I18n.E3D_REVERSE_Y);
                btnMani.setImage(ResourceManager.getImage("icon16_Yinv.png")); //$NON-NLS-1$
            }
            {
                NButton btnMani = new NButton(toolItemManipulatorActions, Cocoa.getStyle());
                this.btnManipulatorZReversePtr[0] = btnMani;
                btnMani.setToolTipText(I18n.E3D_REVERSE_Z);
                btnMani.setImage(ResourceManager.getImage("icon16_Zinv.png")); //$NON-NLS-1$
            }
            {
                NButton btnMani = new NButton(toolItemManipulatorActions, Cocoa.getStyle());
                this.btnManipulatorSwitchXYPtr[0] = btnMani;
                btnMani.setToolTipText(I18n.E3D_SWAP_XY);
                btnMani.setImage(ResourceManager.getImage("icon16_XswapY.png")); //$NON-NLS-1$
            }
            {
                NButton btnMani = new NButton(toolItemManipulatorActions, Cocoa.getStyle());
                this.btnManipulatorSwitchXZPtr[0] = btnMani;
                btnMani.setToolTipText(I18n.E3D_SWAP_XZ);
                btnMani.setImage(ResourceManager.getImage("icon16_XswapZ.png")); //$NON-NLS-1$
            }
            {
                NButton btnMani = new NButton(toolItemManipulatorActions, Cocoa.getStyle());
                this.btnManipulatorSwitchYZPtr[0] = btnMani;
                btnMani.setToolTipText(I18n.E3D_SWAP_YZ);
                btnMani.setImage(ResourceManager.getImage("icon16_YswapZ.png")); //$NON-NLS-1$
            }
            {
                NButton btnMani = new NButton(toolItemManipulatorActions, Cocoa.getStyle());
                this.btnManipulatorCameraToPosPtr[0] = btnMani;
                btnMani.setToolTipText(I18n.E3D_CAM_TO_MANIPULATOR);
                btnMani.setImage(ResourceManager.getImage("icon16_cameratomanipulator.png")); //$NON-NLS-1$
            }
            {
                NButton btnMani = new NButton(toolItemManipulatorActions, Cocoa.getStyle());
                this.btnManipulatorToAveragePtr[0] = btnMani;
                btnMani.setToolTipText(I18n.E3D_MANIPULATOR_TO_AVG);
                btnMani.setImage(ResourceManager.getImage("icon16_toavg.png")); //$NON-NLS-1$
                KeyStateManager.addTooltipText(btnMani, I18n.E3D_MANIPULATOR_TO_AVG, Task.MOVE_TO_AVG);

            }
            {
                NButton btnMani = new NButton(toolItemManipulatorActions, Cocoa.getStyle());
                this.btnManipulatorToSubfilePtr[0] = btnMani;
                btnMani.setToolTipText(I18n.E3D_MANIPULATOR_TO_SUBFILE);
                btnMani.setImage(ResourceManager.getImage("icon16_tosubfile.png")); //$NON-NLS-1$
            }
            {
                NButton btnMani = new NButton(toolItemManipulatorActions, Cocoa.getStyle());
                this.btnManipulatorSubfileToPtr[0] = btnMani;
                btnMani.setToolTipText(Cocoa.replaceCtrlByCmd(I18n.E3D_SUBFILE_TO_MANIPULATOR));
                btnMani.setImage(ResourceManager.getImage("icon16_tosubfile2.png")); //$NON-NLS-1$
            }
            {
                NButton btnMani = new NButton(toolItemManipulatorActions, Cocoa.getStyle());
                this.btnManipulatorToVertexPtr[0] = btnMani;
                btnMani.setToolTipText(I18n.E3D_MANIPULATOR_TO_VERTEX);
                btnMani.setImage(ResourceManager.getImage("icon16_tonearestvertex.png")); //$NON-NLS-1$
            }
            {
                NButton btnMani = new NButton(toolItemManipulatorActions, Cocoa.getStyle());
                this.btnManipulatorToEdgePtr[0] = btnMani;
                btnMani.setToolTipText(I18n.E3D_MANIPULATOR_TO_EDGE);
                btnMani.setImage(ResourceManager.getImage("icon16_tonearestedge.png")); //$NON-NLS-1$
            }
            {
                NButton btnMani = new NButton(toolItemManipulatorActions, Cocoa.getStyle());
                this.btnManipulatorToSurfacePtr[0] = btnMani;
                btnMani.setToolTipText(I18n.E3D_MANIPULATOR_TO_FACE);
                btnMani.setImage(ResourceManager.getImage("icon16_tonearestface.png")); //$NON-NLS-1$
            }
            {
                NButton btnMani = new NButton(toolItemManipulatorActions, Cocoa.getStyle());
                this.btnManipulatorToVertexNormalPtr[0] = btnMani;
                btnMani.setToolTipText(I18n.E3D_MANIPULATOR_TO_VERTEX_N);
                btnMani.setImage(ResourceManager.getImage("icon16_tonearestvertexN.png")); //$NON-NLS-1$
            }
            {
                NButton btnMani = new NButton(toolItemManipulatorActions, Cocoa.getStyle());
                this.btnManipulatorToVertexPositionPtr[0] = btnMani;
                btnMani.setToolTipText(I18n.E3D_MANIPULATOR_TO_VERTEX_P);
                btnMani.setImage(ResourceManager.getImage("icon16_tonearestvertexN2.png")); //$NON-NLS-1$
            }
            {
                NButton btnMani = new NButton(toolItemManipulatorActions, Cocoa.getStyle());
                this.btnManipulatorToEdgeNormalPtr[0] = btnMani;
                btnMani.setToolTipText(I18n.E3D_MANIPULATOR_TO_EDGE_N);
                btnMani.setImage(ResourceManager.getImage("icon16_tonearestedgeN.png")); //$NON-NLS-1$
            }
            {
                NButton btnMani = new NButton(toolItemManipulatorActions, Cocoa.getStyle());
                this.btnManipulatorToSurfaceNormalPtr[0] = btnMani;
                btnMani.setToolTipText(I18n.E3D_MANIPULATOR_TO_FACE_N);
                btnMani.setImage(ResourceManager.getImage("icon16_tonearestfaceN.png")); //$NON-NLS-1$
            }

            return null;
        }
    }

    private ToolItem createToolItemManipulatorMode(ToolItemDrawLocation location, ToolItemDrawMode mode) {
        final Composite target;
        switch (location) {
        case NORTH:
        default:
            target = cmpNorth;
            break;
        case EAST:
            target = cmpEast;
            break;
        case WEST:
            target = cmpWest;
            break;
        }
        ToolItem toolItemTransformationModes = new ToolItem(target, Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL);
        {
            NButton btnLocal = new NButton(toolItemTransformationModes, SWT.TOGGLE | Cocoa.getStyle());
            this.btnLocalPtr[0] = btnLocal;
            btnLocal.setToolTipText(I18n.E3D_LOCAL);
            btnLocal.setSelection(true);
            btnLocal.setImage(ResourceManager.getImage("icon16_local.png")); //$NON-NLS-1$
        }
        {
            NButton btnGlobal = new NButton(toolItemTransformationModes, SWT.TOGGLE | Cocoa.getStyle());
            this.btnGlobalPtr[0] = btnGlobal;
            btnGlobal.setToolTipText(I18n.E3D_GLOBAL);
            btnGlobal.setImage(ResourceManager.getImage("icon16_global.png")); //$NON-NLS-1$
        }
        return toolItemTransformationModes;
    }

    private ToolItem createToolItemTransformationMode(ToolItemDrawLocation location, ToolItemDrawMode mode) {
        final Composite target;
        switch (location) {
        case NORTH:
        default:
            target = cmpNorth;
            break;
        case EAST:
            target = cmpEast;
            break;
        case WEST:
            target = cmpWest;
            break;
        }
        ToolItem toolItemTransformationMode = new ToolItem(target, Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL);
        {
            NButton btnSelect = new NButton(toolItemTransformationMode, SWT.TOGGLE | Cocoa.getStyle());
            this.btnSelectPtr[0] = btnSelect;
            KeyStateManager.addTooltipText(btnSelect, I18n.E3D_SELECT, Task.MODE_SELECT);
            btnSelect.setSelection(true);
            btnSelect.setImage(ResourceManager.getImage("icon16_select.png")); //$NON-NLS-1$
        }
        {
            NButton btnMove = new NButton(toolItemTransformationMode, SWT.TOGGLE | Cocoa.getStyle());
            this.btnMovePtr[0] = btnMove;
            KeyStateManager.addTooltipText(btnMove, I18n.E3D_MOVE, Task.MODE_MOVE);
            btnMove.setImage(ResourceManager.getImage("icon16_move.png")); //$NON-NLS-1$
        }
        {
            NButton btnRotate = new NButton(toolItemTransformationMode, SWT.TOGGLE | Cocoa.getStyle());
            this.btnRotatePtr[0] = btnRotate;
            KeyStateManager.addTooltipText(btnRotate, I18n.E3D_ROTATE, Task.MODE_ROTATE);
            btnRotate.setImage(ResourceManager.getImage("icon16_rotate.png")); //$NON-NLS-1$
        }
        {
            NButton btnScale = new NButton(toolItemTransformationMode, SWT.TOGGLE | Cocoa.getStyle());
            this.btnScalePtr[0] = btnScale;
            KeyStateManager.addTooltipText(btnScale, I18n.E3D_SCALE, Task.MODE_SCALE);
            btnScale.setImage(ResourceManager.getImage("icon16_scale.png")); //$NON-NLS-1$
        }
        {
            NButton btnCombined = new NButton(toolItemTransformationMode, SWT.TOGGLE | Cocoa.getStyle());
            this.btnCombinedPtr[0] = btnCombined;
            KeyStateManager.addTooltipText(btnCombined, I18n.E3D_COMBINED, Task.MODE_COMBINED);
            btnCombined.setImage(ResourceManager.getImage("icon16_combined.png")); //$NON-NLS-1$
        }
        return toolItemTransformationMode;
    }

    private ToolItem createToolItemUndoRedo(ToolItemDrawLocation location, ToolItemDrawMode mode) {
        final Composite target;
        switch (location) {
        case NORTH:
        default:
            target = cmpNorth;
            break;
        case EAST:
            target = cmpEast;
            break;
        case WEST:
            target = cmpWest;
            break;
        }
        ToolItem toolItemUndoRedo = new ToolItem(target, Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL);
        {
            NButton btnUndo = new NButton(toolItemUndoRedo, Cocoa.getStyle());
            this.btnUndoPtr[0] = btnUndo;
            btnUndo.setImage(ResourceManager.getImage("icon16_undo.png")); //$NON-NLS-1$
            KeyStateManager.addTooltipText(btnUndo, I18n.E3D_UNDO, Task.UNDO);
        }
        if (NLogger.debugging) {
            NButton btnSnapshot = new NButton(toolItemUndoRedo, Cocoa.getStyle());
            this.btnAddHistoryPtr[0] = btnSnapshot;
            btnSnapshot.setImage(ResourceManager.getImage("icon16_snapshot.png")); //$NON-NLS-1$
            btnSnapshot.setToolTipText(I18n.E3D_SNAPSHOT);
        }
        {
            NButton btnRedo = new NButton(toolItemUndoRedo, Cocoa.getStyle());
            this.btnRedoPtr[0] = btnRedo;
            btnRedo.setImage(ResourceManager.getImage("icon16_redo.png")); //$NON-NLS-1$
            KeyStateManager.addTooltipText(btnRedo, I18n.E3D_REDO, Task.REDO);
        }
        return toolItemUndoRedo;
    }

    private ToolItem createToolItemMiscToggle(ToolItemDrawLocation location, ToolItemDrawMode mode) {
        final Composite target;
        switch (location) {
        case NORTH:
        default:
            target = cmpNorth;
            break;
        case EAST:
            target = cmpEast;
            break;
        case WEST:
            target = cmpWest;
            break;
        }
        ToolItem toolItemMiscToggle = new ToolItem(target, Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL);
        {
            NButton btnAdjacentMove = new NButton(toolItemMiscToggle, SWT.TOGGLE | Cocoa.getStyle());
            this.btnMoveAdjacentDataPtr[0] = btnAdjacentMove;
            KeyStateManager.addTooltipText(btnAdjacentMove, I18n.E3D_MOVE_ADJACENT_DATA, Task.MOVE_ADJACENT_DATA);
            btnAdjacentMove.setImage(ResourceManager.getImage("icon16_adjacentmove.png")); //$NON-NLS-1$
            btnAdjacentMove.setSelection(WorkbenchManager.getUserSettingState().isMovingAdjacentData());
        }
        {
            NButton btnTransSelection = new NButton(toolItemMiscToggle, SWT.TOGGLE | Cocoa.getStyle());
            this.btnNoTransparentSelectionPtr[0] = btnTransSelection;
            btnTransSelection.setToolTipText(I18n.E3D_TOGGLE_TRANSPARENT);
            btnTransSelection.setImage(ResourceManager.getImage("icon16_notrans.png")); //$NON-NLS-1$
        }
        {
            NButton btnBFCToggle = new NButton(toolItemMiscToggle, SWT.TOGGLE | Cocoa.getStyle());
            this.btnBFCTogglePtr[0] = btnBFCToggle;
            btnBFCToggle.setToolTipText(I18n.E3D_TOGGLE_BFC);
            btnBFCToggle.setImage(ResourceManager.getImage("icon16_bfc.png")); //$NON-NLS-1$
        }
        return toolItemMiscToggle;
    }

    private ToolItem createToolItemHideUnhide(ToolItemDrawLocation location, ToolItemDrawMode mode) {
        final Composite target;
        switch (location) {
        case NORTH:
        default:
            target = cmpNorth;
            break;
        case EAST:
            target = cmpEast;
            break;
        case WEST:
            target = cmpWest;
            break;
        }
        ToolItem toolItemHideUnhide = new ToolItem(target, Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL);
        {
            NButton btnHide = new NButton(toolItemHideUnhide, Cocoa.getStyle());
            this.btnHidePtr[0] = btnHide;
            btnHide.setToolTipText(I18n.E3D_HIDE);
            btnHide.setImage(ResourceManager.getImage("icon16_hide.png")); //$NON-NLS-1$
        }
        {
            NButton btnUnhide = new NButton(toolItemHideUnhide, Cocoa.getStyle());
            this.btnShowAllPtr[0] = btnUnhide;
            btnUnhide.setToolTipText(I18n.E3D_SHOW_ALL);
            btnUnhide.setImage(ResourceManager.getImage("icon16_unhide.png")); //$NON-NLS-1$
        }
        return toolItemHideUnhide;
    }

    private ToolItem createToolItemNewOpenDat(ToolItemDrawLocation location, ToolItemDrawMode mode) {
        final Composite target;
        switch (location) {
        case NORTH:
        default:
            target = cmpNorth;
            break;
        case EAST:
            target = cmpEast;
            break;
        case WEST:
            target = cmpWest;
            break;
        }
        ToolItem toolItemNewOpenDAT = new ToolItem(target, Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL);
        {
            NButton btnNewDat = new NButton(toolItemNewOpenDAT, Cocoa.getStyle());
            this.btnNewDatPtr[0] = btnNewDat;
            btnNewDat.setToolTipText(I18n.E3D_NEW_DAT);
            btnNewDat.setImage(ResourceManager.getImage("icon16_document-newdat.png")); //$NON-NLS-1$
        }
        {
            NButton btnOpenDAT = new NButton(toolItemNewOpenDAT, Cocoa.getStyle());
            this.btnOpenDatPtr[0] = btnOpenDAT;
            btnOpenDAT.setToolTipText(I18n.E3D_OPEN_DAT);
            btnOpenDAT.setImage(ResourceManager.getImage("icon16_document-opendat.png")); //$NON-NLS-1$
        }
        {
            NButton btnSnapshot = new NButton(toolItemNewOpenDAT, Cocoa.getStyle());
            this.btnLastOpenPtr[0] = btnSnapshot;
            btnSnapshot.setToolTipText(I18n.E3D_LAST_OPENED);
            btnSnapshot.setImage(ResourceManager.getImage("icon16_snapshot.png")); //$NON-NLS-1$
        }
        {
            NButton btnSaveDAT = new NButton(toolItemNewOpenDAT, Cocoa.getStyle());
            this.btnSaveDatPtr[0] = btnSaveDAT;
            KeyStateManager.addTooltipText(btnSaveDAT, I18n.E3D_SAVE, Task.SAVE);
            btnSaveDAT.setImage(ResourceManager.getImage("icon16_document-savedat.png")); //$NON-NLS-1$
        }
        {
            NButton btnSaveAsDAT = new NButton(toolItemNewOpenDAT, Cocoa.getStyle());
            this.btnSaveAsDatPtr[0] = btnSaveAsDAT;
            btnSaveAsDAT.setToolTipText(I18n.E3D_SAVE_AS);
            btnSaveAsDAT.setImage(ResourceManager.getImage("icon16_document-savedat.png")); //$NON-NLS-1$
            btnSaveAsDAT.setText("..."); //$NON-NLS-1$
        }
        return toolItemNewOpenDAT;
    }

    private ToolItem createToolItemNewOpenSave(ToolItemDrawLocation location, ToolItemDrawMode mode) {
        final Composite target;
        switch (location) {
        case NORTH:
        default:
            target = cmpNorth;
            break;
        case EAST:
            target = cmpEast;
            break;
        case WEST:
            target = cmpWest;
            break;
        }
        ToolItem toolItemNewOpenSave = new ToolItem(target, Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL);
        {
            NButton btnNew = new NButton(toolItemNewOpenSave, Cocoa.getStyle());
            this.btnNewPtr[0] = btnNew;
            btnNew.setToolTipText(I18n.E3D_NEW);
            btnNew.setImage(ResourceManager.getImage("icon16_document-new.png")); //$NON-NLS-1$
        }
        {
            NButton btnOpen = new NButton(toolItemNewOpenSave, Cocoa.getStyle());
            this.btnOpenPtr[0] = btnOpen;
            btnOpen.setToolTipText(I18n.E3D_OPEN);
            btnOpen.setImage(ResourceManager.getImage("icon16_document-open.png")); //$NON-NLS-1$
        }
        {
            NButton btnSave = new NButton(toolItemNewOpenSave, Cocoa.getStyle());
            this.btnSavePtr[0] = btnSave;
            KeyStateManager.addTooltipText(btnSave, I18n.E3D_SAVE, Task.SAVE);
            btnSave.setImage(ResourceManager.getImage("icon16_document-save.png")); //$NON-NLS-1$
        }
        {
            NButton btnSaveAll = new NButton(toolItemNewOpenSave, Cocoa.getStyle());
            this.btnSaveAllPtr[0] = btnSaveAll;
            btnSaveAll.setToolTipText(I18n.E3D_SAVE_ALL);
            btnSaveAll.setImage(ResourceManager.getImage("icon16_document-saveall.png")); //$NON-NLS-1$
        }
        return toolItemNewOpenSave;
    }

    private ToolItem createToolItemSync(ToolItemDrawLocation location, ToolItemDrawMode mode) {
        final Composite target;
        switch (location) {
        case NORTH:
        default:
            target = cmpNorth;
            break;
        case EAST:
            target = cmpEast;
            break;
        case WEST:
            target = cmpWest;
            break;
        }
        ToolItem toolItemSync = new ToolItem(target, Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL);
        {
            NButton btnSync = new NButton(toolItemSync, Cocoa.getStyle());
            this.btnSyncPtr[0] = btnSync;
            btnSync.setToolTipText(I18n.E3D_SYNC_FOLDERS);
            btnSync.setImage(ResourceManager.getImage("icon16_sync.png")); //$NON-NLS-1$
        }
        return toolItemSync;
    }

    private void createComposite3D(SashForm sashForm, CompositeContainer c, Composite3DState state) {
        // Load the configuration of one 3D window
        final Composite3D c3d;
        if (c == null) {
            final CompositeContainer cmpContainer = new CompositeContainer(sashForm, state.hasScales());
            c3d = cmpContainer.getComposite3D();
        } else {
            state.setScales(false);
            c3d = c.getComposite3D();
        }

        c3d.loadState(state);
    }

    void addColorButton(ToolItem toolItemColours, GColour gColour, final int index) {
        int cn = gColour.getColourNumber();
        if (cn != -1 && View.hasLDConfigColour(cn)) {
            gColour = View.getLDConfigColour(cn);
        }

        final int imgSize = IconSize.getImageSizeFromIconSize();
        final GColour[] gColour2 = new GColour[] { gColour };
        final Color[] col = new Color[1];
        col[0] = SWTResourceManager.getColor((int) (gColour2[0].getR() * 255f), (int) (gColour2[0].getG() * 255f), (int) (gColour2[0].getB() * 255f));

        final NButton btnCol = new NButton(toolItemColours, Cocoa.getStyle());
        btnCol.setData(gColour2);
        int num = gColour2[0].getColourNumber();
        if (!View.hasLDConfigColour(num)) {
            num = -1;
        }
        if (num != -1) {

            Object[] messageArguments = {num, View.getLDConfigColourName(num)};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.locale);
            formatter.applyPattern(I18n.EDITORTEXT_COLOUR_1 + Cocoa.replaceCtrlByCmd(I18n.E3D_CONTROL_CLICK_MODIFY));

            btnCol.setToolTipText(formatter.format(messageArguments));
        } else {
            StringBuilder colourBuilder = new StringBuilder();
            colourBuilder.append("0x2"); //$NON-NLS-1$
            colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getR())).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getG())).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getB())).toUpperCase());

            Object[] messageArguments = {colourBuilder.toString()};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.locale);
            formatter.applyPattern(I18n.EDITORTEXT_COLOUR_2 + Cocoa.replaceCtrlByCmd(I18n.E3D_CONTROL_CLICK_MODIFY));

            btnCol.setToolTipText(formatter.format(messageArguments));
        }

        btnCol.setImage(ResourceManager.getImage("icon16_fullTransparent.png")); //$NON-NLS-1$

        widgetUtil(btnCol).addSelectionListener(e -> {
            if (Cocoa.checkCtrlOrCmdPressed(e.stateMask)) {
                // Choose new colour
                new ColourDialog(getShell(), gColour2, false).run();
                WorkbenchManager.getUserSettingState().getUserPalette().set(index, gColour2[0]);
                col[0] = SWTResourceManager.getColor((int) (gColour2[0].getR() * 255f), (int) (gColour2[0].getG() * 255f), (int) (gColour2[0].getB() * 255f));
                int num2 = gColour2[0].getColourNumber();
                if (View.hasLDConfigColour(num2)) {
                    gColour2[0] = View.getLDConfigColour(num2);
                } else {
                    num2 = -1;
                }
                if (num2 != -1) {

                    Object[] messageArguments1 = {num2, View.getLDConfigColourName(num2)};
                    MessageFormat formatter1 = new MessageFormat(""); //$NON-NLS-1$
                    formatter1.setLocale(MyLanguage.locale);
                    formatter1.applyPattern(I18n.EDITORTEXT_COLOUR_1 + Cocoa.replaceCtrlByCmd(I18n.E3D_CONTROL_CLICK_MODIFY));

                    btnCol.setToolTipText(formatter1.format(messageArguments1));
                } else {
                    StringBuilder colourBuilder1 = new StringBuilder();
                    colourBuilder1.append("0x2"); //$NON-NLS-1$
                    colourBuilder1.append(MathHelper.toHex((int) (255f * gColour2[0].getR())).toUpperCase());
                    colourBuilder1.append(MathHelper.toHex((int) (255f * gColour2[0].getG())).toUpperCase());
                    colourBuilder1.append(MathHelper.toHex((int) (255f * gColour2[0].getB())).toUpperCase());

                    Object[] messageArguments2 = {colourBuilder1.toString()};
                    MessageFormat formatter2 = new MessageFormat(""); //$NON-NLS-1$
                    formatter2.setLocale(MyLanguage.locale);
                    formatter2.applyPattern(I18n.EDITORTEXT_COLOUR_2 + Cocoa.replaceCtrlByCmd(I18n.E3D_CONTROL_CLICK_MODIFY));

                    btnCol.setToolTipText(formatter2.format(messageArguments2));
                }
                Editor3DWindow.reloadAllColours();
            } else {
                int num3 = gColour2[0].getColourNumber();
                if (View.hasLDConfigColour(num3)) {
                    gColour2[0] = View.getLDConfigColour(num3);
                } else {
                    num3 = -1;
                }
                if (Project.getFileToEdit() != null) {
                    Project.getFileToEdit().getVertexManager().addSnapshot();
                    Project.getFileToEdit().getVertexManager().colourChangeSelection(num3, gColour2[0].getR(), gColour2[0].getG(), gColour2[0].getB(), gColour2[0].getA(), true);
                }
                Editor3DWindow.getWindow().setLastUsedColour(gColour2[0]);
                btnLastUsedColourPtr[0].clearPaintListeners();
                btnLastUsedColourPtr[0].clearSelectionListeners();
                final Color col1 = SWTResourceManager.getColor((int) (gColour2[0].getR() * 255f), (int) (gColour2[0].getG() * 255f), (int) (gColour2[0].getB() * 255f));
                final Point size = btnLastUsedColourPtr[0].computeSize(SWT.DEFAULT, SWT.DEFAULT);
                final int x = Math.round(size.x / 5f);
                final int y = Math.round(size.y / 5f);
                final int w = Math.round(size.x * (3f / 5f));
                final int h = Math.round(size.y * (3f / 5f));
                btnLastUsedColourPtr[0].addPaintListener(e1 -> {
                    e1.gc.setBackground(col1);
                    e1.gc.fillRectangle(x, y, w, h);
                    if (gColour2[0].getA() == 1f) {
                        e1.gc.drawImage(ResourceManager.getImage("icon16_transparent.png"), 0, 0, imgSize, imgSize, x, y, w, h); //$NON-NLS-1$
                    } else {
                        e1.gc.drawImage(ResourceManager.getImage("icon16_halftrans.png"), 0, 0, imgSize, imgSize, x, y, w, h); //$NON-NLS-1$
                    }
                });
                widgetUtil(btnLastUsedColourPtr[0]).addSelectionListener(e1 -> {
                    if (Project.getFileToEdit() != null) {
                        Editor3DWindow.getWindow().setLastUsedColour(gColour2[0]);
                        int num1 = gColour2[0].getColourNumber();
                        if (View.hasLDConfigColour(num1)) {
                            gColour2[0] = View.getLDConfigColour(num1);
                        } else {
                            num1 = -1;
                        }
                        Project.getFileToEdit().getVertexManager().addSnapshot();
                        Project.getFileToEdit().getVertexManager().colourChangeSelection(num1, gColour2[0].getR(), gColour2[0].getG(), gColour2[0].getB(), gColour2[0].getA(), true);
                    }
                    Editor3DWindow.getWindow().regainFocus();
                });
                if (num3 != -1) {
                    Object[] messageArguments3 = {num3, View.getLDConfigColourName(num3)};
                    MessageFormat formatter3 = new MessageFormat(""); //$NON-NLS-1$
                    formatter3.setLocale(MyLanguage.locale);
                    formatter3.applyPattern(I18n.EDITORTEXT_COLOUR_1);

                    btnLastUsedColourPtr[0].setToolTipText(formatter3.format(messageArguments3));
                } else {
                    StringBuilder colourBuilder2 = new StringBuilder();
                    colourBuilder2.append("0x2"); //$NON-NLS-1$
                    colourBuilder2.append(MathHelper.toHex((int) (255f * gColour2[0].getR())).toUpperCase());
                    colourBuilder2.append(MathHelper.toHex((int) (255f * gColour2[0].getG())).toUpperCase());
                    colourBuilder2.append(MathHelper.toHex((int) (255f * gColour2[0].getB())).toUpperCase());

                    Object[] messageArguments4 = {colourBuilder2.toString()};
                    MessageFormat formatter4 = new MessageFormat(""); //$NON-NLS-1$
                    formatter4.setLocale(MyLanguage.locale);
                    formatter4.applyPattern(I18n.EDITORTEXT_COLOUR_2);

                    btnLastUsedColourPtr[0].setToolTipText(formatter4.format(messageArguments4));
                }
                btnLastUsedColourPtr[0].redraw();
            }
            Editor3DWindow.getWindow().regainFocus();
        });
        final Point size = btnCol.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        final int x = Math.round(size.x / 5f);
        final int y = Math.round(size.y / 5f);
        final int w = Math.round(size.x * (3f / 5f));
        final int h = Math.round(size.y * (3f / 5f));
        btnCol.addPaintListener(e -> {
            e.gc.setBackground(col[0]);
            e.gc.fillRectangle(x, y, w, h);
            if (gColour2[0].getA() == 1f) {
                e.gc.drawImage(ResourceManager.getImage("icon16_transparent.png"), 0, 0, imgSize, imgSize, x, y, w, h); //$NON-NLS-1$
            } else {
                e.gc.drawImage(ResourceManager.getImage("icon16_halftrans.png"), 0, 0, imgSize, imgSize, x, y, w, h); //$NON-NLS-1$
            }
        });
    }

    /**
     * Create the status line manager.
     *
     * @return the status line manager
     */
    @Override
    protected StatusLineManager createStatusLineManager() {
        return new StatusLineManager();
    }

    /**
     * Configure the shell.
     *
     * @param newShell
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
    }

    /**
     * Return the initial size of the window.
     */
    @Override
    protected Point getInitialSize() {
        return new Point(916, 578);
    }

    public static Label getStatusLabel() {
        return (Label) Editor3DDesign.status.getChildren()[0];
    }

    public static SashForm getSashForm() {
        return sashForm;
    }

    public SashForm getSplitSashForm() {
        return splitSash[0];
    }

    private static void setSashForm(SashForm sashForm) {
        Editor3DDesign.sashForm = sashForm;
    }

    void showSelectMenu() {
        Point loc = btnSelect2Ptr[0].getLocation();
        Rectangle rect = btnSelect2Ptr[0].getBounds();
        Point mLoc = new Point(loc.x - 1, loc.y + rect.height);
        mnuSelect.setLocation(getShell().getDisplay().map(btnSelect2Ptr[0].getParent(), null, mLoc));
        mnuSelect.setVisible(true);
    }

    void reloadC3DStates(ArrayList<Composite3DState> threeDconfig) {

        // TODO Needs implementation!

        if (!threeDconfig.isEmpty()) {
            Collections.sort(threeDconfig, (o1, o2) -> {
                final int cmp = Integer.compare(o1.getPath().length(), o2.getPath().length());
                if (cmp == 0) {
                    return o1.getPath().compareTo(o2.getPath());
                }
                return cmp;
            });

            Editor3DWindow.getWindow().openDatFile(View.DUMMY_DATFILE, OpenInWhat.EDITOR_3D, null);

            if (threeDconfig.size() == 1) {
                Editor3DWindow.getRenders().get(0).getC3D().loadState(threeDconfig.get(0));
            } else {
                HashSet<String> splitCandidate = new HashSet<>();
                HashSet<String> splitAlready = new HashSet<>();
                HashMap<String, CompositeContainer> cmpMap = new HashMap<>();
                HashMap<String, Composite3DState> sMap = new HashMap<>();

                splitCandidate.add("|"); //$NON-NLS-1$
                splitAlready.add("|"); //$NON-NLS-1$
                {
                    Composite3D root = Editor3DWindow.getRenders().get(0).getC3D();
                    SashForm sf;
                    if (!threeDconfig.get(0).isVertical()) {
                        sf = root.getModifier().splitViewVertically();
                    } else {
                        sf = root.getModifier().splitViewHorizontally();
                    }
                    try {
                        sf.setWeights(threeDconfig.get(0).getWeights());
                    } catch (IllegalArgumentException iae) {
                        NLogger.error(getClass(), iae);
                    }
                    cmpMap.put("|s1|", (CompositeContainer) sf.getChildren()[0]); //$NON-NLS-1$
                    cmpMap.put("|s2|", (CompositeContainer) sf.getChildren()[1]); //$NON-NLS-1$
                }
                for (Composite3DState state : threeDconfig) {
                    String path = state.getPath();
                    String parentPath = state.getParentPath();
                    if (state.isSash()) {
                        sMap.put(path, state);
                    }
                    if (!splitAlready.contains(path) && !splitCandidate.contains(path)) {
                        splitCandidate.add(path);
                    }
                    if (splitCandidate.contains(parentPath) && !splitAlready.contains(parentPath) && cmpMap.containsKey(parentPath) && sMap.containsKey(parentPath)) {
                        {
                            Composite3DState state2 = sMap.get(parentPath);
                            CompositeContainer c = cmpMap.get(parentPath);
                            SashForm sf;
                            if (!state2.isVertical()) {
                                sf = c.getComposite3D().getModifier().splitViewVertically();
                            } else {
                                sf = c.getComposite3D().getModifier().splitViewHorizontally();
                            }
                            try {
                                sf.setWeights(state2.getWeights());
                            } catch (IllegalArgumentException iae) {
                                NLogger.error(getClass(), iae);
                            }
                            cmpMap.remove(parentPath);
                            cmpMap.put(parentPath + "s1|", (CompositeContainer) sf.getChildren()[0]); //$NON-NLS-1$
                            cmpMap.put(parentPath + "s2|", (CompositeContainer) sf.getChildren()[1]); //$NON-NLS-1$
                        }
                        splitAlready.add(parentPath);
                    }
                }

                for (Composite3DState state : threeDconfig) {
                    String path = state.getPath();
                    if (cmpMap.containsKey(path)) {
                        createComposite3D(null, cmpMap.get(path), state);
                    }
                }
            }
        }
    }

    private void applyC3DStatesOnStartup(ArrayList<Composite3DState> threeDconfig) {

        Collections.sort(threeDconfig, (o1, o2) -> {
            final int cmp = Integer.compare(o1.getPath().length(), o2.getPath().length());
            if (cmp == 0) {
                return o1.getPath().compareTo(o2.getPath());
            }
            return cmp;
        });
        CompositeContainer cmpContainer = new CompositeContainer(sashForm, false);
        cmpContainer.moveBelow(sashForm.getChildren()[0]);

        HashSet<String> splitCandidate = new HashSet<>();
        HashSet<String> splitAlready = new HashSet<>();
        HashMap<String, CompositeContainer> cmpMap = new HashMap<>();
        HashMap<String, Composite3DState> sMap = new HashMap<>();

        splitCandidate.add("|"); //$NON-NLS-1$
        splitAlready.add("|"); //$NON-NLS-1$
        {
            SashForm sf;
            if (!threeDconfig.get(0).isVertical()) {
                sf = cmpContainer.getComposite3D().getModifier().splitViewVertically();
            } else {
                sf = cmpContainer.getComposite3D().getModifier().splitViewHorizontally();
            }
            try {
                sf.setWeights(threeDconfig.get(0).getWeights());
            } catch (IllegalArgumentException iae) {
                NLogger.error(getClass(), iae);
            }
            cmpMap.put("|s1|", (CompositeContainer) sf.getChildren()[0]); //$NON-NLS-1$
            cmpMap.put("|s2|", (CompositeContainer) sf.getChildren()[1]); //$NON-NLS-1$
        }
        for (Composite3DState state : threeDconfig) {
            String path = state.getPath();
            String parentPath = state.getParentPath();
            if (state.isSash()) {
                sMap.put(path, state);
            }
            if (!splitAlready.contains(path) && !splitCandidate.contains(path)) {
                splitCandidate.add(path);
            }
            if (splitCandidate.contains(parentPath) && !splitAlready.contains(parentPath) && cmpMap.containsKey(parentPath) && sMap.containsKey(parentPath)) {
                {
                    Composite3DState state2 = sMap.get(parentPath);
                    CompositeContainer c = cmpMap.get(parentPath);
                    SashForm sf;
                    if (!state2.isVertical()) {
                        sf = c.getComposite3D().getModifier().splitViewVertically();
                    } else {
                        sf = c.getComposite3D().getModifier().splitViewHorizontally();
                    }
                    try {
                        sf.setWeights(state2.getWeights());
                    } catch (IllegalArgumentException iae) {
                        NLogger.error(getClass(), iae);
                    }
                    cmpMap.remove(parentPath);
                    cmpMap.put(parentPath + "s1|", (CompositeContainer) sf.getChildren()[0]); //$NON-NLS-1$
                    cmpMap.put(parentPath + "s2|", (CompositeContainer) sf.getChildren()[1]); //$NON-NLS-1$
                }
                splitAlready.add(parentPath);
            }
        }

        for (Composite3DState state : threeDconfig) {
            String path = state.getPath();
            if (cmpMap.containsKey(path)) {
                createComposite3D(null, cmpMap.get(path), state);
            }
        }
    }
}