/* MIT - License

Copyright (c) 2012 - this year, Nils Schmidt

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission miscToolItemce shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package org.nschmidt.ldparteditor.shell.editor3d.toolitem;

import static org.nschmidt.ldparteditor.helper.WidgetUtility.widgetUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.nschmidt.ldparteditor.composite.Composite3D;
import org.nschmidt.ldparteditor.composite.ToolItem;
import org.nschmidt.ldparteditor.composite.compositetab.CompositeTab;
import org.nschmidt.ldparteditor.composite.primitive.CompositePrimitive;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.DatType;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GData2;
import org.nschmidt.ldparteditor.data.RingsAndCones;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.dialog.direction.DirectionDialog;
import org.nschmidt.ldparteditor.dialog.edger2.EdgerDialog;
import org.nschmidt.ldparteditor.dialog.intersector.IntersectorDialog;
import org.nschmidt.ldparteditor.dialog.isecalc.IsecalcDialog;
import org.nschmidt.ldparteditor.dialog.lines2pattern.Lines2PatternDialog;
import org.nschmidt.ldparteditor.dialog.logupload.LogDisplayDialog;
import org.nschmidt.ldparteditor.dialog.meshreducer.MeshReducerDialog;
import org.nschmidt.ldparteditor.dialog.options.OptionsDialog;
import org.nschmidt.ldparteditor.dialog.partreview.PartReviewDialog;
import org.nschmidt.ldparteditor.dialog.pathtruder.PathTruderDialog;
import org.nschmidt.ldparteditor.dialog.primgen2.PrimGen2Dialog;
import org.nschmidt.ldparteditor.dialog.rectifier.RectifierDialog;
import org.nschmidt.ldparteditor.dialog.ringsandcones.RingsAndConesDialog;
import org.nschmidt.ldparteditor.dialog.rotate.RotateDialog;
import org.nschmidt.ldparteditor.dialog.round.RoundDialog;
import org.nschmidt.ldparteditor.dialog.scale.ScaleDialog;
import org.nschmidt.ldparteditor.dialog.selectvertex.VertexDialog;
import org.nschmidt.ldparteditor.dialog.setcoordinates.CoordinatesDialog;
import org.nschmidt.ldparteditor.dialog.slantingmatrixprojector.SlantingMatrixProjectorDialog;
import org.nschmidt.ldparteditor.dialog.slicerpro.SlicerProDialog;
import org.nschmidt.ldparteditor.dialog.smooth.SmoothDialog;
import org.nschmidt.ldparteditor.dialog.symsplitter.SymSplitterDialog;
import org.nschmidt.ldparteditor.dialog.tjunction.TJunctionDialog;
import org.nschmidt.ldparteditor.dialog.translate.TranslateDialog;
import org.nschmidt.ldparteditor.dialog.txt2dat.Txt2DatDialog;
import org.nschmidt.ldparteditor.dialog.unificator.UnificatorDialog;
import org.nschmidt.ldparteditor.dialog.value.ValueDialog;
import org.nschmidt.ldparteditor.dialog.value.ValueDialogInt;
import org.nschmidt.ldparteditor.dialog.ytruder.YTruderDialog;
import org.nschmidt.ldparteditor.enumtype.IconSize;
import org.nschmidt.ldparteditor.enumtype.LDConfig;
import org.nschmidt.ldparteditor.enumtype.ManipulatorScope;
import org.nschmidt.ldparteditor.enumtype.MergeTo;
import org.nschmidt.ldparteditor.enumtype.MyLanguage;
import org.nschmidt.ldparteditor.enumtype.OpenInWhat;
import org.nschmidt.ldparteditor.enumtype.Perspective;
import org.nschmidt.ldparteditor.enumtype.Task;
import org.nschmidt.ldparteditor.enumtype.Threshold;
import org.nschmidt.ldparteditor.enumtype.TransformationMode;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.enumtype.WorkingMode;
import org.nschmidt.ldparteditor.helper.Cocoa;
import org.nschmidt.ldparteditor.helper.FileHelper;
import org.nschmidt.ldparteditor.helper.LDPartEditorException;
import org.nschmidt.ldparteditor.helper.Version;
import org.nschmidt.ldparteditor.helper.WidgetSelectionHelper;
import org.nschmidt.ldparteditor.helper.WidgetSelectionListener;
import org.nschmidt.ldparteditor.helper.composite3d.Edger2Settings;
import org.nschmidt.ldparteditor.helper.composite3d.GuiStatusManager;
import org.nschmidt.ldparteditor.helper.composite3d.IntersectorSettings;
import org.nschmidt.ldparteditor.helper.composite3d.IsecalcSettings;
import org.nschmidt.ldparteditor.helper.composite3d.MeshReducerSettings;
import org.nschmidt.ldparteditor.helper.composite3d.PathTruderSettings;
import org.nschmidt.ldparteditor.helper.composite3d.RectifierSettings;
import org.nschmidt.ldparteditor.helper.composite3d.RingsAndConesSettings;
import org.nschmidt.ldparteditor.helper.composite3d.SelectorSettings;
import org.nschmidt.ldparteditor.helper.composite3d.SlantingMatrixProjectorSettings;
import org.nschmidt.ldparteditor.helper.composite3d.SymSplitterSettings;
import org.nschmidt.ldparteditor.helper.composite3d.TJunctionSettings;
import org.nschmidt.ldparteditor.helper.composite3d.Txt2DatSettings;
import org.nschmidt.ldparteditor.helper.composite3d.UnificatorSettings;
import org.nschmidt.ldparteditor.helper.composite3d.YTruderSettings;
import org.nschmidt.ldparteditor.helper.compositetext.SubfileCompiler;
import org.nschmidt.ldparteditor.helper.math.Vector3d;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.ldraworg.CategoriesUtils;
import org.nschmidt.ldparteditor.ldraworg.LDConfigUtils;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.resource.ResourceManager;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.shell.editortext.EditorTextWindow;
import org.nschmidt.ldparteditor.state.KeyStateManager;
import org.nschmidt.ldparteditor.text.DatParser;
import org.nschmidt.ldparteditor.text.LDParsingException;
import org.nschmidt.ldparteditor.text.StringHelper;
import org.nschmidt.ldparteditor.text.TextTriangulator;
import org.nschmidt.ldparteditor.text.UTF8BufferedReader;
import org.nschmidt.ldparteditor.text.UTF8PrintWriter;
import org.nschmidt.ldparteditor.widget.NButton;
import org.nschmidt.ldparteditor.widget.TreeItem;
import org.nschmidt.ldparteditor.workbench.Editor3DWindowState;
import org.nschmidt.ldparteditor.workbench.UserSettingState;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

public class MiscToolItem extends ToolItem {

    /** The menu of the select features */
    private static Menu mnuSelect;

    private static final NButton[] btnShowSelectionInTextEditorPtr = new NButton[1];
    private static final NButton[] btnBFCswapPtr = new NButton[1];
    private static final NButton[] btnCompileSubfilePtr = new NButton[1];
    private static final NButton[] btnSplitQuadPtr = new NButton[1];
    private static final NButton[] btnMergeQuadPtr = new NButton[1];
    private static final NButton[] btnLineIntersectionPtr = new NButton[1];
    private static final NButton[] btnCondlineToLinePtr = new NButton[1];
    private static final NButton[] btnLineToCondlinePtr = new NButton[1];
    private static final NButton[] btnMoveOnLinePtr = new NButton[1];
    private static final NButton[] btnRoundSelectionPtr = new NButton[1];

    private static final NButton[] btnSelect2Ptr = new NButton[1];

    private static final MenuItem[] mntmSelectAllPtr = new MenuItem[1];
    private static final MenuItem[] mntmSelectAllWithColoursPtr = new MenuItem[1];
    private static final MenuItem[] mntmSelectAllVisiblePtr = new MenuItem[1];
    private static final MenuItem[] mntmSelectAllVisibleWithColoursPtr = new MenuItem[1];
    private static final MenuItem[] mntmSelectNonePtr = new MenuItem[1];
    private static final MenuItem[] mntmSelectInversePtr = new MenuItem[1];
    private static final MenuItem[] mntmSelectEverythingPtr = new MenuItem[1];
    private static final MenuItem[] mntmSelectConnectedPtr = new MenuItem[1];
    private static final MenuItem[] mntmSelectTouchingPtr = new MenuItem[1];
    private static final MenuItem[] mntmWithSameOrientationPtr = new MenuItem[1];
    private static final MenuItem[] mntmWithAccuracyPtr = new MenuItem[1];
    private static final MenuItem[] mntmWithAdjacencyPtr = new MenuItem[1];
    private static final MenuItem[] mntmWithWholeSubfilesPtr = new MenuItem[1];
    private static final MenuItem[] mntmWithSameColourPtr = new MenuItem[1];
    private static final MenuItem[] mntmWithSameTypePtr = new MenuItem[1];
    private static final MenuItem[] mntmWithHiddenDataPtr = new MenuItem[1];
    private static final MenuItem[] mntmExceptSubfilesPtr = new MenuItem[1];
    private static final MenuItem[] mntmStopAtEdgesPtr = new MenuItem[1];
    private static final MenuItem[] mntmSelectIsolatedVerticesPtr = new MenuItem[1];
    private static final MenuItem[] mntmSelectSingleVertexPtr = new MenuItem[1];

    private static final MenuItem[] mntmSAllTypesPtr = new MenuItem[1];
    private static final MenuItem[] mntmSTrianglesPtr = new MenuItem[1];
    private static final MenuItem[] mntmSQuadsPtr = new MenuItem[1];
    private static final MenuItem[] mntmSCLinesPtr = new MenuItem[1];
    private static final MenuItem[] mntmSVerticesPtr = new MenuItem[1];
    private static final MenuItem[] mntmSLinesPtr = new MenuItem[1];
    private static final MenuItem[] mntmSNothingPtr = new MenuItem[1];

    private static final MenuItem[] mntmSetXYZPtr = new MenuItem[1];
    private static final MenuItem[] mntmTranslatePtr = new MenuItem[1];
    private static final MenuItem[] mntmRotatePtr = new MenuItem[1];
    private static final MenuItem[] mntmScalePtr = new MenuItem[1];

    private static final MenuItem[] mntmSnapToGridPtr = new MenuItem[1];

    private static final MenuItem[] mntmFlipPtr = new MenuItem[1];
    private static final MenuItem[] mntmSmoothPtr = new MenuItem[1];
    private static final MenuItem[] mntmSubdivideCatmullClarkPtr = new MenuItem[1];
    private static final MenuItem[] mntmSubdivideLoopPtr = new MenuItem[1];
    private static final MenuItem[] mntmSplitPtr = new MenuItem[1];
    private static final MenuItem[] mntmSplitNTimesPtr = new MenuItem[1];

    private static final MenuItem[] mntmMergeToAveragePtr = new MenuItem[1];
    private static final MenuItem[] mntmMergeToLastSelectedPtr = new MenuItem[1];
    private static final MenuItem[] mntmMergeToNearestVertexPtr = new MenuItem[1];
    private static final MenuItem[] mntmMergeToNearestEdgePtr = new MenuItem[1];
    private static final MenuItem[] mntmMergeToNearestEdgeSplitPtr = new MenuItem[1];
    private static final MenuItem[] mntmMergeToNearestFacePtr = new MenuItem[1];
    private static final MenuItem[] mntmMergeToNearestFaceDirPtr = new MenuItem[1];

    private static final MenuItem[] mntmPartReviewPtr = new MenuItem[1];
    private static final MenuItem[] mntmEdger2Ptr = new MenuItem[1];
    private static final MenuItem[] mntmTxt2DatPtr = new MenuItem[1];
    private static final MenuItem[] mntmPrimGen2Ptr = new MenuItem[1];
    private static final MenuItem[] mntmRectifierPtr = new MenuItem[1];
    private static final MenuItem[] mntmIsecalcPtr = new MenuItem[1];
    private static final MenuItem[] mntmSlicerProPtr = new MenuItem[1];
    private static final MenuItem[] mntmIntersectorPtr = new MenuItem[1];
    private static final MenuItem[] mntmSlantingMatrixProjectorPtr = new MenuItem[1];
    private static final MenuItem[] mntmLines2PatternPtr = new MenuItem[1];
    private static final MenuItem[] mntmPathTruderPtr = new MenuItem[1];
    private static final MenuItem[] mntmYTruderPtr = new MenuItem[1];
    private static final MenuItem[] mntmSymSplitterPtr = new MenuItem[1];
    private static final MenuItem[] mntmUnificatorPtr = new MenuItem[1];
    private static final MenuItem[] mntmRingsAndConesPtr = new MenuItem[1];
    private static final MenuItem[] mntmTJunctionFinderPtr = new MenuItem[1];
    private static final MenuItem[] mntmMeshReducerPtr = new MenuItem[1];

    private static final MenuItem[] mntmOptionsPtr = new MenuItem[1];
    private static final MenuItem[] mntmUserConfigLoadPtr = new MenuItem[1];
    private static final MenuItem[] mntmUserConfigSavePtr = new MenuItem[1];
    private static final MenuItem[] mntmResetSettingsOnRestartPtr = new MenuItem[1];
    private static final MenuItem[] mntmSelectAnotherLDConfigPtr = new MenuItem[1];
    private static final MenuItem[] mntmDownloadLDConfigPtr = new MenuItem[1];
    private static final MenuItem[] mntmDownloadCategoriesPtr = new MenuItem[1];

    private static final MenuItem[] mntmLoadPalettePtr = new MenuItem[1];
    private static final MenuItem[] mntmResetPalettePtr = new MenuItem[1];
    private static final MenuItem[] mntmSavePalettePtr = new MenuItem[1];
    private static final MenuItem[] mntmSetPaletteSizePtr = new MenuItem[1];

    private static final MenuItem[] mntmShowLogsPtr = new MenuItem[1];
    private static final MenuItem[] mntmAntiAliasingPtr = new MenuItem[1];
    private static final MenuItem[] mntmOpenGL33EnginePtr = new MenuItem[1];
    private static final MenuItem[] mntmVulkanEnginePtr = new MenuItem[1];
    private static final MenuItem[] mntmSyncLpeInlinePtr = new MenuItem[1];

    private static final MenuItem[] mntmIconSize1Ptr = new MenuItem[1];
    private static final MenuItem[] mntmIconSize2Ptr = new MenuItem[1];
    private static final MenuItem[] mntmIconSize3Ptr = new MenuItem[1];
    private static final MenuItem[] mntmIconSize4Ptr = new MenuItem[1];
    private static final MenuItem[] mntmIconSize5Ptr = new MenuItem[1];
    private static final MenuItem[] mntmIconSize6Ptr = new MenuItem[1];

    private static Txt2DatSettings ts = new Txt2DatSettings();
    private static Edger2Settings es = new Edger2Settings();
    private static RectifierSettings rs = new RectifierSettings();
    private static IsecalcSettings is = new IsecalcSettings();
    private static IntersectorSettings ins = new IntersectorSettings();
    private static PathTruderSettings ps = new PathTruderSettings();
    private static YTruderSettings ys = new YTruderSettings();
    private static SymSplitterSettings sims = new SymSplitterSettings();
    private static UnificatorSettings us = new UnificatorSettings();
    private static RingsAndConesSettings ris = new RingsAndConesSettings();
    private static SelectorSettings sels = new SelectorSettings();
    private static TJunctionSettings tjs = new TJunctionSettings();
    private static MeshReducerSettings ms = new MeshReducerSettings();
    private static SlantingMatrixProjectorSettings mps = new SlantingMatrixProjectorSettings();

    public MiscToolItem(Composite parent, int style, boolean isHorizontal) {
        super(parent, style, isHorizontal);
        createWidgets(this);
        addListeners();
    }

    private static void createWidgets(MiscToolItem miscToolItem) {
        NButton btnShowSelectionInTextEditor = new NButton(miscToolItem, Cocoa.getStyle());
        MiscToolItem.btnShowSelectionInTextEditorPtr[0] = btnShowSelectionInTextEditor;
        btnShowSelectionInTextEditor.setToolTipText(I18n.C3D_SHOW_IN_TEXT);
        btnShowSelectionInTextEditor.setImage(ResourceManager.getImage("icon16_selection2text.png")); //$NON-NLS-1$

        NButton btnBFCswap = new NButton(miscToolItem, Cocoa.getStyle());
        MiscToolItem.btnBFCswapPtr[0] = btnBFCswap;
        KeyStateManager.addTooltipText(btnBFCswap, I18n.E3D_SWAP_WINDING, Task.SWAP_WINDING);
        btnBFCswap.setImage(ResourceManager.getImage("icon16_bfcSwap.png")); //$NON-NLS-1$

        NButton btnCompileSubfile = new NButton(miscToolItem, Cocoa.getStyle());
        MiscToolItem.btnCompileSubfilePtr[0] = btnCompileSubfile;
        btnCompileSubfile.setToolTipText(I18n.E3D_COMPILE_SUBFILE_DATA);
        btnCompileSubfile.setImage(ResourceManager.getImage("icon16_subcompile.png")); //$NON-NLS-1$

        NButton btnSplitQuad = new NButton(miscToolItem, Cocoa.getStyle());
        MiscToolItem.btnSplitQuadPtr[0] = btnSplitQuad;
        btnSplitQuad.setImage(ResourceManager.getImage("icon16_quadToTri.png")); //$NON-NLS-1$
        btnSplitQuad.setToolTipText(I18n.E3D_SPLIT_QUAD);

        NButton btnMergeQuad = new NButton(miscToolItem, Cocoa.getStyle());
        MiscToolItem.btnMergeQuadPtr[0] = btnMergeQuad;
        btnMergeQuad.setImage(ResourceManager.getImage("icon16_triToquad.png")); //$NON-NLS-1$
        btnMergeQuad.setToolTipText(I18n.EDITORTEXT_MERGE_QUAD);

        NButton btnLineIntersection = new NButton(miscToolItem, Cocoa.getStyle());
        MiscToolItem.btnLineIntersectionPtr[0] = btnLineIntersection;
        btnLineIntersection.setImage(ResourceManager.getImage("icon16_lineintersect.png")); //$NON-NLS-1$
        btnLineIntersection.setToolTipText(I18n.E3D_LINE_INTERSECTION + Cocoa.replaceCtrlByCmd(I18n.E3D_CONTROL_CLICK_SURFACE));

        NButton btnCondlineToLine = new NButton(miscToolItem, Cocoa.getStyle());
        MiscToolItem.btnCondlineToLinePtr[0] = btnCondlineToLine;
        KeyStateManager.addTooltipText(btnCondlineToLine, I18n.E3D_CONDLINE_TO_LINE, Task.CONDLINE_TO_LINE);
        // FIXME Needs icon!
        btnCondlineToLine.setText("C2L"); //$NON-NLS-1$
        btnCondlineToLine.setToolTipText(I18n.E3D_CONDLINE_TO_LINE);

        NButton btnLineToCondline = new NButton(miscToolItem, Cocoa.getStyle());
        MiscToolItem.btnLineToCondlinePtr[0] = btnLineToCondline;
        KeyStateManager.addTooltipText(btnLineToCondline, I18n.E3D_LINE_TO_CONDLINE, Task.LINE_TO_CONDLINE);
        // FIXME Needs icon!
        btnLineToCondline.setText("L2C"); //$NON-NLS-1$
        btnLineToCondline.setToolTipText(I18n.E3D_LINE_TO_CONDLINE);

        NButton btnMoveOnLine = new NButton(miscToolItem, Cocoa.getStyle());
        MiscToolItem.btnMoveOnLinePtr[0] = btnMoveOnLine;
        // FIXME Needs icon!
        btnMoveOnLine.setText("ML"); //$NON-NLS-1$
        btnMoveOnLine.setToolTipText(I18n.E3D_MOVE_ON_LINE);

        NButton btnRoundSelection = new NButton(miscToolItem, Cocoa.getStyle());
        MiscToolItem.btnRoundSelectionPtr[0] = btnRoundSelection;
        btnRoundSelection.setToolTipText(I18n.E3D_ROUND + Cocoa.replaceCtrlByCmd(I18n.E3D_CONTROL_CLICK_MODIFY));
        btnRoundSelection.setImage(ResourceManager.getImage("icon16_round.png")); //$NON-NLS-1$

        final NButton btnSelect = new NButton(miscToolItem, SWT.PUSH | Cocoa.getStyle());
        MiscToolItem.btnSelect2Ptr[0] = btnSelect;
        btnSelect.setToolTipText(I18n.E3D_ADVANCED_SELECT);
        btnSelect.setText(I18n.E3D_ADVANCED_SELECT);
        MiscToolItem.mnuSelect = new Menu(miscToolItem.getShell(), SWT.POP_UP);
        widgetUtil(btnSelect).addSelectionListener(e -> {
            showSelectMenu();
            Editor3DWindow.getWindow().regainFocus();
        });

        MenuItem mntmSelectAll = new MenuItem(mnuSelect, SWT.PUSH);
        MiscToolItem.mntmSelectAllPtr[0] = mntmSelectAll;
        KeyStateManager.addKeyText(mntmSelectAll, I18n.E3D_ALL, Task.SELECT_ALL);

        MenuItem mntmSelectNone = new MenuItem(mnuSelect, SWT.PUSH);
        MiscToolItem.mntmSelectNonePtr[0] = mntmSelectNone;
        KeyStateManager.addKeyText(mntmSelectNone, I18n.E3D_NONE, Task.SELECT_NONE);

        new MenuItem(mnuSelect, SWT.SEPARATOR);

        MenuItem mntmSelectInverse = new MenuItem(mnuSelect, SWT.PUSH);
        MiscToolItem.mntmSelectInversePtr[0] = mntmSelectInverse;
        mntmSelectInverse.setText(I18n.E3D_INVERSE);

        new MenuItem(mnuSelect, SWT.SEPARATOR);

        MenuItem mntmSelectAllVisible = new MenuItem(mnuSelect, SWT.PUSH);
        MiscToolItem.mntmSelectAllVisiblePtr[0] = mntmSelectAllVisible;
        mntmSelectAllVisible.setText(I18n.E3D_ALL_SHOWN);

        MenuItem mntmSelectAllWithColours = new MenuItem(mnuSelect, SWT.PUSH);
        MiscToolItem.mntmSelectAllWithColoursPtr[0] = mntmSelectAllWithColours;
        KeyStateManager.addKeyText(mntmSelectAllWithColours, I18n.E3D_ALL_SAME_COLOURS, Task.SELECT_ALL_WITH_SAME_COLOURS);

        MenuItem mntmSelectAllVisibleWithColours = new MenuItem(mnuSelect, SWT.PUSH);
        MiscToolItem.mntmSelectAllVisibleWithColoursPtr[0] = mntmSelectAllVisibleWithColours;
        mntmSelectAllVisibleWithColours.setText(I18n.E3D_ALL_SAME_COLOURS_SHOWN);

        new MenuItem(mnuSelect, SWT.SEPARATOR);

        MenuItem mntmSelectEverything = new MenuItem(mnuSelect, SWT.PUSH);
        MiscToolItem.mntmSelectEverythingPtr[0] = mntmSelectEverything;
        mntmSelectEverything.setText(I18n.E3D_EVERYTHING);
        mntmSelectEverything.setEnabled(false);

        MenuItem mntmSelectConnected = new MenuItem(mnuSelect, SWT.PUSH);
        MiscToolItem.mntmSelectConnectedPtr[0] = mntmSelectConnected;
        KeyStateManager.addKeyText(mntmSelectConnected, I18n.E3D_CONNECTED, Task.SELECT_CONNECTED);

        MenuItem mntmSelectTouching = new MenuItem(mnuSelect, SWT.PUSH);
        MiscToolItem.mntmSelectTouchingPtr[0] = mntmSelectTouching;
        KeyStateManager.addKeyText(mntmSelectTouching, I18n.E3D_TOUCHING, Task.SELECT_TOUCHING);

        MenuItem mntmWithSameColour = new MenuItem(mnuSelect, SWT.CHECK);
        MiscToolItem.mntmWithSameColourPtr[0] = mntmWithSameColour;
        KeyStateManager.addKeyText(mntmWithSameColour, I18n.E3D_WITH_SAME_COLOUR, Task.SELECT_OPTION_WITH_SAME_COLOURS);

        MenuItem mntmWithSameType = new MenuItem(mnuSelect, SWT.CHECK);
        MiscToolItem.mntmWithSameTypePtr[0] = mntmWithSameType;
        mntmWithSameType.setText(I18n.E3D_WITH_SAME_TYPE);

        MenuItem mntmWithSameOrientation = new MenuItem(mnuSelect, SWT.CHECK);
        MiscToolItem.mntmWithSameOrientationPtr[0] = mntmWithSameOrientation;
        mntmWithSameOrientation.setText(I18n.E3D_WITH_SAME_ORIENTATION);

        MenuItem mntmWithAccuracy = new MenuItem(mnuSelect, SWT.CHECK);
        MiscToolItem.mntmWithAccuracyPtr[0] = mntmWithAccuracy;
        mntmWithAccuracy.setText(I18n.E3D_WITH_ACCURACY);

        MenuItem mntmWithAdjacency = new MenuItem(mnuSelect, SWT.CHECK);
        MiscToolItem.mntmWithAdjacencyPtr[0] = mntmWithAdjacency;
        mntmWithAdjacency.setText(I18n.E3D_WITH_ADJACENCY);

        MenuItem mntmWithHiddenData = new MenuItem(mnuSelect, SWT.CHECK);
        MiscToolItem.mntmWithHiddenDataPtr[0] = mntmWithHiddenData;
        mntmWithHiddenData.setText(I18n.E3D_WHAT_IS_HIDDEN);
        mntmWithHiddenData.setImage(ResourceManager.getImage("icon16_hide.png")); //$NON-NLS-1$

        MenuItem mntmExceptSubfiles = new MenuItem(mnuSelect, SWT.CHECK);
        MiscToolItem.mntmExceptSubfilesPtr[0] = mntmExceptSubfiles;
        mntmExceptSubfiles.setText(I18n.E3D_EXCEPT_SUBFILE);

        MenuItem mntmWithWholeSubfiles = new MenuItem(mnuSelect, SWT.CHECK);
        MiscToolItem.mntmWithWholeSubfilesPtr[0] = mntmWithWholeSubfiles;
        mntmWithWholeSubfiles.setText(I18n.E3D_WITH_WHOLE_SUBFILE_SELECTION);

        MenuItem mntmStopAtEdges = new MenuItem(mnuSelect, SWT.CHECK);
        MiscToolItem.mntmStopAtEdgesPtr[0] = mntmStopAtEdges;
        mntmStopAtEdges.setText(I18n.E3D_STOP_SELECTION_AT_EDGES);

        new MenuItem(mnuSelect, SWT.SEPARATOR);

        MenuItem mntmSelectSingleVertex = new MenuItem(mnuSelect, SWT.PUSH);
        MiscToolItem.mntmSelectSingleVertexPtr[0] = mntmSelectSingleVertex;
        mntmSelectSingleVertex.setText(I18n.E3D_SELECT_VERTEX);

        MenuItem mntmSelectIsolatedVertices = new MenuItem(mnuSelect, SWT.PUSH);
        MiscToolItem.mntmSelectIsolatedVerticesPtr[0] = mntmSelectIsolatedVertices;
        mntmSelectIsolatedVertices.setText(I18n.E3D_ISOLATED_VERTICES);

        new MenuItem(mnuSelect, SWT.SEPARATOR);

        MenuItem mntmSVertices = new MenuItem(mnuSelect, SWT.CHECK);
        MiscToolItem.mntmSVerticesPtr[0] = mntmSVertices;
        mntmSVertices.setText(I18n.E3D_VERTICES);
        mntmSVertices.setSelection(true);

        MenuItem mntmSLines = new MenuItem(mnuSelect, SWT.CHECK);
        MiscToolItem.mntmSLinesPtr[0] = mntmSLines;
        mntmSLines.setText(I18n.E3D_LINES);
        mntmSLines.setSelection(true);

        MenuItem mntmSTriangles = new MenuItem(mnuSelect, SWT.CHECK);
        MiscToolItem.mntmSTrianglesPtr[0] = mntmSTriangles;
        mntmSTriangles.setText(I18n.E3D_TRIANGLES);
        mntmSTriangles.setSelection(true);

        MenuItem mntmSQuads = new MenuItem(mnuSelect, SWT.CHECK);
        MiscToolItem.mntmSQuadsPtr[0] = mntmSQuads;
        mntmSQuads.setText(I18n.E3D_QUADS);
        mntmSQuads.setSelection(true);

        MenuItem mntmSCLines = new MenuItem(mnuSelect, SWT.CHECK);
        MiscToolItem.mntmSCLinesPtr[0] = mntmSCLines;
        mntmSCLines.setText(I18n.E3D_CONDLINES);
        mntmSCLines.setSelection(true);

        new MenuItem(mnuSelect, SWT.SEPARATOR);

        MenuItem mntmSAllTypes = new MenuItem(mnuSelect, SWT.PUSH);
        MiscToolItem.mntmSAllTypesPtr[0] = mntmSAllTypes;
        mntmSAllTypes.setText(I18n.E3D_ALL_TYPES);

        MenuItem mntmSNothing = new MenuItem(mnuSelect, SWT.PUSH);
        MiscToolItem.mntmSNothingPtr[0] = mntmSNothing;
        mntmSNothing.setText(I18n.E3D_NOTHING);

        new MenuItem(mnuSelect, SWT.SEPARATOR);

        MenuItem mntmNeedsThreshold = new MenuItem(mnuSelect, SWT.PUSH);
        mntmNeedsThreshold.setText(I18n.E3D_NEEDS_A_THRESHOLD);
        mntmNeedsThreshold.setEnabled(false);

        MenuItem mntmNoEffect = new MenuItem(mnuSelect, SWT.PUSH);
        mntmNoEffect.setText(I18n.E3D_NO_EFFECT_SELECT_EVERYTHING);
        mntmNoEffect.setEnabled(false);

        final NButton btnMergeNSplit = new NButton(miscToolItem, SWT.PUSH | Cocoa.getStyle());
        btnMergeNSplit.setToolTipText(I18n.E3D_MERGE_SPLIT);
        btnMergeNSplit.setText(I18n.E3D_MERGE_SPLIT);
        final Menu mnuMerge = new Menu(miscToolItem.getShell(), SWT.POP_UP);
        widgetUtil(btnMergeNSplit).addSelectionListener(e -> {
            final Composite3D c3d = Editor3DWindow.getWindow().getCurrentCoposite3d();
            MiscToolItem.mntmSnapToGridPtr[0].setEnabled(c3d != null && c3d.isClassicPerspective());
            Point loc = btnMergeNSplit.getLocation();
            Rectangle rect = btnMergeNSplit.getBounds();
            Point mLoc = new Point(loc.x - 1, loc.y + rect.height);
            mnuMerge.setLocation(Editor3DWindow.getWindow().getShell().getDisplay().map(btnMergeNSplit.getParent(), null, mLoc));
            mnuMerge.setVisible(true);
            Editor3DWindow.getWindow().regainFocus();
        });

        MenuItem mntmFlip = new MenuItem(mnuMerge, SWT.PUSH);
        MiscToolItem.mntmFlipPtr[0] = mntmFlip;
        mntmFlip.setText(I18n.E3D_FLIP_ROTATE);
        KeyStateManager.addKeyText(mntmFlip, I18n.E3D_FLIP_ROTATE, Task.FLIP_ROTATE_VERTICES);

        MenuItem mntmSmooth = new MenuItem(mnuMerge, SWT.PUSH);
        MiscToolItem.mntmSmoothPtr[0] = mntmSmooth;
        mntmSmooth.setText(I18n.E3D_SMOOTH);

        new MenuItem(mnuMerge, SWT.SEPARATOR);

        MenuItem mntmSplit = new MenuItem(mnuMerge, SWT.PUSH);
        MiscToolItem.mntmSplitPtr[0] = mntmSplit;
        KeyStateManager.addKeyText(mntmSplit, I18n.E3D_SPLIT, Task.SPLIT);

        MenuItem mntmSplitNTimes = new MenuItem(mnuMerge, SWT.PUSH);
        MiscToolItem.mntmSplitNTimesPtr[0] = mntmSplitNTimes;
        mntmSplitNTimes.setText(I18n.E3D_SPLIT_N_TIMES);

        new MenuItem(mnuMerge, SWT.SEPARATOR);

        MenuItem mntmMergeToAverage = new MenuItem(mnuMerge, SWT.PUSH);
        MiscToolItem.mntmMergeToAveragePtr[0] = mntmMergeToAverage;
        KeyStateManager.addKeyText(mntmMergeToAverage, I18n.E3D_MERGE_TO_AVG, Task.MERGE_TO_AVERAGE);

        MenuItem mntmMergeToLastSelected = new MenuItem(mnuMerge, SWT.PUSH);
        MiscToolItem.mntmMergeToLastSelectedPtr[0] = mntmMergeToLastSelected;
        KeyStateManager.addKeyText(mntmMergeToLastSelected, I18n.E3D_MERGE_TO_LAST_SELECTED, Task.MERGE_TO_LAST);

        MenuItem mntmMergeToNearestVertex = new MenuItem(mnuMerge, SWT.PUSH);
        MiscToolItem.mntmMergeToNearestVertexPtr[0] = mntmMergeToNearestVertex;
        KeyStateManager.addKeyText(mntmMergeToNearestVertex, I18n.E3D_MERGE_TO_NEAREST_VERTEX, Task.MERGE_TO_NEAREST_VERTEX);

        MenuItem mntmMergeToNearestEdge = new MenuItem(mnuMerge, SWT.PUSH);
        MiscToolItem.mntmMergeToNearestEdgePtr[0] = mntmMergeToNearestEdge;
        mntmMergeToNearestEdge.setText(I18n.E3D_MERGE_TO_NEAREST_EDGE);

        MenuItem mntmMergeToNearestEdgeSplit = new MenuItem(mnuMerge, SWT.PUSH);
        MiscToolItem.mntmMergeToNearestEdgeSplitPtr[0] = mntmMergeToNearestEdgeSplit;
        mntmMergeToNearestEdgeSplit.setText(I18n.E3D_MERGE_TO_NEAREST_EDGE_SPLIT);

        MenuItem mntmMergeToNearestFace = new MenuItem(mnuMerge, SWT.PUSH);
        MiscToolItem.mntmMergeToNearestFacePtr[0] = mntmMergeToNearestFace;
        mntmMergeToNearestFace.setText(I18n.E3D_MERGE_TO_NEAREST_FACE);

        new MenuItem(mnuMerge, SWT.SEPARATOR);

        MenuItem mntmMergeToNearestFaceDir = new MenuItem(mnuMerge, SWT.PUSH);
        MiscToolItem.mntmMergeToNearestFaceDirPtr[0] = mntmMergeToNearestFaceDir;
        mntmMergeToNearestFaceDir.setText(I18n.E3D_MERGE_TO_NEAREST_FACE_DIR);

        new MenuItem(mnuMerge, SWT.SEPARATOR);

        MenuItem mntmSetXYZ = new MenuItem(mnuMerge, SWT.PUSH);
        MiscToolItem.mntmSetXYZPtr[0] = mntmSetXYZ;
        mntmSetXYZ.setText(I18n.E3D_SET_XYZ);

        MenuItem mntmTranslate = new MenuItem(mnuMerge, SWT.PUSH);
        MiscToolItem.mntmTranslatePtr[0] = mntmTranslate;
        mntmTranslate.setText(I18n.E3D_TRANSLATE_SELECTION);

        MenuItem mntmRotate = new MenuItem(mnuMerge, SWT.PUSH);
        MiscToolItem.mntmRotatePtr[0] = mntmRotate;
        mntmRotate.setText(I18n.E3D_ROTATE_SELECTION);

        MenuItem mntmScale = new MenuItem(mnuMerge, SWT.PUSH);
        MiscToolItem.mntmScalePtr[0] = mntmScale;
        mntmScale.setText(I18n.E3D_SCALE_SELECTION);

        new MenuItem(mnuMerge, SWT.SEPARATOR);

        MenuItem mntmSnapToGrid = new MenuItem(mnuMerge, SWT.PUSH);
        MiscToolItem.mntmSnapToGridPtr[0] = mntmSnapToGrid;
        mntmSnapToGrid.setText(I18n.E3D_SNAP_TO_GRID);

        new MenuItem(mnuMerge, SWT.SEPARATOR);

        MenuItem mntmSubdivideCatmullClark = new MenuItem(mnuMerge, SWT.PUSH);
        MiscToolItem.mntmSubdivideCatmullClarkPtr[0] = mntmSubdivideCatmullClark;
        mntmSubdivideCatmullClark.setText(I18n.E3D_SUBDIVIDE_CATMULL_CLARK);

        MenuItem mntmSubdivideLoop = new MenuItem(mnuMerge, SWT.PUSH);
        MiscToolItem.mntmSubdivideLoopPtr[0] = mntmSubdivideLoop;
        mntmSubdivideLoop.setText(I18n.E3D_SUBDIVIDE_LOOP);

        final NButton btnToolsActions = new NButton(miscToolItem, SWT.PUSH | Cocoa.getStyle());
        btnToolsActions.setText(I18n.E3D_TOOLS);
        btnToolsActions.setToolTipText(I18n.E3D_TOOLS_OPTIONS);
        final Menu mnuTools = new Menu(miscToolItem.getShell(), SWT.POP_UP);
        widgetUtil(btnToolsActions).addSelectionListener(e -> {
            Point loc = btnToolsActions.getLocation();
            Rectangle rect = btnToolsActions.getBounds();
            Point mLoc = new Point(loc.x - 1, loc.y + rect.height);
            mnuTools.setLocation(Editor3DWindow.getWindow().getShell().getDisplay().map(btnToolsActions.getParent(), null, mLoc));
            mnuTools.setVisible(true);
            Editor3DWindow.getWindow().regainFocus();
        });

        MenuItem mntmPartReview = new MenuItem(mnuTools, SWT.PUSH);
        MiscToolItem.mntmPartReviewPtr[0] = mntmPartReview;
        mntmPartReview.setText(I18n.PARTREVIEW_TITLE);

        MenuItem mntmEdger2 = new MenuItem(mnuTools, SWT.PUSH);
        MiscToolItem.mntmEdger2Ptr[0] = mntmEdger2;
        mntmEdger2.setText(I18n.E3D_EDGER_2);

        MenuItem mntmPrimGen2 = new MenuItem(mnuTools, SWT.PUSH);
        MiscToolItem.mntmPrimGen2Ptr[0] = mntmPrimGen2;
        mntmPrimGen2.setText(I18n.E3D_PRIMGEN2);

        MenuItem mntmTxt2Dat = new MenuItem(mnuTools, SWT.PUSH);
        MiscToolItem.mntmTxt2DatPtr[0] = mntmTxt2Dat;
        mntmTxt2Dat.setText(I18n.E3D_TXT_2_DAT);

        MenuItem mntmRectifier = new MenuItem(mnuTools, SWT.PUSH);
        MiscToolItem.mntmRectifierPtr[0] = mntmRectifier;
        mntmRectifier.setText(I18n.E3D_RECTIFIER);

        MenuItem mntmIsecalc = new MenuItem(mnuTools, SWT.PUSH);
        MiscToolItem.mntmIsecalcPtr[0] = mntmIsecalc;
        mntmIsecalc.setText(I18n.E3D_ISECALC);

        MenuItem mntmSlicerPro = new MenuItem(mnuTools, SWT.PUSH);
        MiscToolItem.mntmSlicerProPtr[0] = mntmSlicerPro;
        mntmSlicerPro.setText(I18n.E3D_SLICER_PRO);

        MenuItem mntmIntersector = new MenuItem(mnuTools, SWT.PUSH);
        MiscToolItem.mntmIntersectorPtr[0] = mntmIntersector;
        mntmIntersector.setText(I18n.E3D_INTERSECTOR);

        MenuItem mntmMatrixCalculator = new MenuItem(mnuTools, SWT.PUSH);
        MiscToolItem.mntmSlantingMatrixProjectorPtr[0] = mntmMatrixCalculator;
        mntmMatrixCalculator.setText(I18n.E3D_SLANTING_MATRIX_PROJECTOR);

        MenuItem mntmLines2Pattern = new MenuItem(mnuTools, SWT.PUSH);
        MiscToolItem.mntmLines2PatternPtr[0] = mntmLines2Pattern;
        mntmLines2Pattern.setText(I18n.E3D_LINES_2_PATTERN);

        MenuItem mntmPathTruder = new MenuItem(mnuTools, SWT.PUSH);
        MiscToolItem.mntmPathTruderPtr[0] = mntmPathTruder;
        mntmPathTruder.setText(I18n.E3D_PATH_TRUDER);

        MenuItem mntmYTruder = new MenuItem(mnuTools, SWT.PUSH);
        MiscToolItem.mntmYTruderPtr[0] = mntmYTruder;
        mntmYTruder.setText(I18n.E3D_YTRUDER);

        MenuItem mntmSymSplitter = new MenuItem(mnuTools, SWT.PUSH);
        MiscToolItem.mntmSymSplitterPtr[0] = mntmSymSplitter;
        mntmSymSplitter.setText(I18n.E3D_SYM_SPLITTER);

        MenuItem mntmUnificator = new MenuItem(mnuTools, SWT.PUSH);
        MiscToolItem.mntmUnificatorPtr[0] = mntmUnificator;
        mntmUnificator.setText(I18n.E3D_UNIFICATOR);

        MenuItem mntmRingsAndCones = new MenuItem(mnuTools, SWT.PUSH);
        MiscToolItem.mntmRingsAndConesPtr[0] = mntmRingsAndCones;
        mntmRingsAndCones.setText(I18n.E3D_RINGS_AND_CONES);

        MenuItem mntmTJunctionFinder = new MenuItem(mnuTools, SWT.PUSH);
        MiscToolItem.mntmTJunctionFinderPtr[0] = mntmTJunctionFinder;
        mntmTJunctionFinder.setText(I18n.E3D_T_JUNCTION);

        MenuItem mntmMeshReducer = new MenuItem(mnuTools, SWT.PUSH);
        MiscToolItem.mntmMeshReducerPtr[0] = mntmMeshReducer;
        mntmMeshReducer.setText(I18n.E3D_MESH_REDUCE);

        new MenuItem(mnuTools, SWT.SEPARATOR);

        final MenuItem mntmLibFeatures = new MenuItem(mnuTools, SWT.CASCADE);
        mntmLibFeatures.setText(I18n.E3D_LIBRARY_FEATURES);
        final Menu mnuLibFeatures = new Menu(mntmLibFeatures);
        mntmLibFeatures.setMenu(mnuLibFeatures);

        MenuItem mntmSelectAnotherLDConfig = new MenuItem(mnuLibFeatures, SWT.PUSH);
        MiscToolItem.mntmSelectAnotherLDConfigPtr[0] = mntmSelectAnotherLDConfig;
        mntmSelectAnotherLDConfig.setText(I18n.E3D_SELECT_LDCONFIG);

        MenuItem mntmDownloadLDConfig = new MenuItem(mnuLibFeatures, SWT.PUSH);
        MiscToolItem.mntmDownloadLDConfigPtr[0] = mntmDownloadLDConfig;
        mntmDownloadLDConfig.setText(I18n.E3D_DOWNLOAD_LD_CONFIG);

        MenuItem mntmDownloadCategories = new MenuItem(mnuLibFeatures, SWT.PUSH);
        MiscToolItem.mntmDownloadCategoriesPtr[0] = mntmDownloadCategories;
        mntmDownloadCategories.setText(I18n.E3D_DOWNLOAD_CATEGORIES);

        MenuItem mntmOptions = new MenuItem(mnuTools, SWT.PUSH);
        MiscToolItem.mntmOptionsPtr[0] = mntmOptions;
        mntmOptions.setText(I18n.E3D_OPTIONS);

        MenuItem mntmUserConfigSave = new MenuItem(mnuTools, SWT.PUSH);
        MiscToolItem.mntmUserConfigSavePtr[0] = mntmUserConfigSave;
        mntmUserConfigSave.setText(I18n.E3D_USER_CONFIG_SAVE);

        MenuItem mntmUserConfigLoad = new MenuItem(mnuTools, SWT.PUSH);
        MiscToolItem.mntmUserConfigLoadPtr[0] = mntmUserConfigLoad;
        mntmUserConfigLoad.setText(I18n.E3D_USER_CONFIG_LOAD);

        MenuItem mntmResetSettingsOnRestart = new MenuItem(mnuTools, SWT.PUSH);
        MiscToolItem.mntmResetSettingsOnRestartPtr[0] = mntmResetSettingsOnRestart;
        mntmResetSettingsOnRestart.setText(I18n.E3D_RESET_ALL);

        final MenuItem mntmPalette = new MenuItem(mnuTools, SWT.CASCADE);
        mntmPalette.setText(I18n.E3D_PALETTE);
        final Menu mnuPalette = new Menu(mntmPalette);
        mntmPalette.setMenu(mnuPalette);

        MenuItem mntmSavePalette = new MenuItem(mnuPalette, SWT.PUSH);
        MiscToolItem.mntmSavePalettePtr[0] = mntmSavePalette;
        mntmSavePalette.setText(I18n.E3D_PALETTE_SAVE);

        MenuItem mntmLoadPalette = new MenuItem(mnuPalette, SWT.PUSH);
        MiscToolItem.mntmLoadPalettePtr[0] = mntmLoadPalette;
        mntmLoadPalette.setText(I18n.E3D_PALETTE_LOAD);

        MenuItem mntmSetPaletteSize = new MenuItem(mnuPalette, SWT.PUSH);
        MiscToolItem.mntmSetPaletteSizePtr[0] = mntmSetPaletteSize;
        mntmSetPaletteSize.setText(I18n.E3D_PALETTE_SET_SIZE);

        MenuItem mntmResetPalette = new MenuItem(mnuPalette, SWT.PUSH);
        MiscToolItem.mntmResetPalettePtr[0] = mntmResetPalette;
        mntmResetPalette.setText(I18n.E3D_PALETTE_RESET);

        final MenuItem mntmSetIconSize = new MenuItem(mnuTools, SWT.CASCADE);
        mntmSetIconSize.setText(I18n.E3D_SET_ICON_SIZE);
        final Menu mnuIconSize = new Menu(mntmSetIconSize);
        mntmSetIconSize.setMenu(mnuIconSize);
        final int iconSize = IconSize.getIconsize();

        MenuItem mntmIconSize1 = new MenuItem(mnuIconSize, SWT.RADIO);
        MiscToolItem.mntmIconSize1Ptr[0] = mntmIconSize1;
        mntmIconSize1.setText(I18n.E3D_ICON_SIZE_1);
        mntmIconSize1.setSelection(iconSize == -1);

        MenuItem mntmIconSize2 = new MenuItem(mnuIconSize, SWT.RADIO);
        MiscToolItem.mntmIconSize2Ptr[0] = mntmIconSize2;
        mntmIconSize2.setText(I18n.E3D_ICON_SIZE_2);
        mntmIconSize2.setSelection(iconSize == 0);

        MenuItem mntmIconSize3 = new MenuItem(mnuIconSize, SWT.RADIO);
        MiscToolItem.mntmIconSize3Ptr[0] = mntmIconSize3;
        mntmIconSize3.setText(I18n.E3D_ICON_SIZE_3);
        mntmIconSize3.setSelection(iconSize == 1);

        MenuItem mntmIconSize4 = new MenuItem(mnuIconSize, SWT.RADIO);
        MiscToolItem.mntmIconSize4Ptr[0] = mntmIconSize4;
        mntmIconSize4.setText(I18n.E3D_ICON_SIZE_4);
        mntmIconSize4.setSelection(iconSize == 2);

        MenuItem mntmIconSize5 = new MenuItem(mnuIconSize, SWT.RADIO);
        MiscToolItem.mntmIconSize5Ptr[0] = mntmIconSize5;
        mntmIconSize5.setText(I18n.E3D_ICON_SIZE_5);
        mntmIconSize5.setSelection(iconSize == 3);

        MenuItem mntmIconSize6 = new MenuItem(mnuIconSize, SWT.RADIO);
        MiscToolItem.mntmIconSize6Ptr[0] = mntmIconSize6;
        mntmIconSize6.setText(I18n.E3D_ICON_SIZE_6);
        mntmIconSize6.setSelection(iconSize >= 4);

        new MenuItem(mnuIconSize, SWT.SEPARATOR);

        MenuItem mntmIconSizeRequiresRestart = new MenuItem(mnuIconSize, SWT.PUSH);
        mntmIconSizeRequiresRestart.setText(I18n.E3D_REQUIRES_RESTART);
        mntmIconSizeRequiresRestart.setEnabled(false);

        new MenuItem(mnuTools, SWT.SEPARATOR);

        MenuItem mntmShowErrorLog = new MenuItem(mnuTools, SWT.PUSH);
        MiscToolItem.mntmShowLogsPtr[0] = mntmShowErrorLog;
        mntmShowErrorLog.setText(I18n.E3D_SHOW_ERROR_LOGS);

        new MenuItem(mnuTools, SWT.SEPARATOR);

        MenuItem mntmAntiAliasing = new MenuItem(mnuTools, SWT.CHECK);
        mntmAntiAliasing.setSelection(WorkbenchManager.getUserSettingState().isAntiAliasing());
        MiscToolItem.mntmAntiAliasingPtr[0] = mntmAntiAliasing;
        mntmAntiAliasing.setText(I18n.E3D_ANTI_ALIASING);

        MenuItem mntmOpenGL33Engine = new MenuItem(mnuTools, SWT.CHECK);
        mntmOpenGL33Engine.setSelection(WorkbenchManager.getUserSettingState().isOpenGL33Engine());
        MiscToolItem.mntmOpenGL33EnginePtr[0] = mntmOpenGL33Engine;
        mntmOpenGL33Engine.setText(I18n.E3D_NEW_ENGINE);

        if (NLogger.debugging) {
            MenuItem mntmVulkanEngine = new MenuItem(mnuTools, SWT.CHECK);
            mntmVulkanEngine.setSelection(WorkbenchManager.getUserSettingState().isVulkanEngine());
            MiscToolItem.mntmVulkanEnginePtr[0] = mntmVulkanEngine;
            mntmVulkanEngine.setText(I18n.E3D_VULKAN_ENGINE);
        }
        new MenuItem(mnuTools, SWT.SEPARATOR);

        MenuItem mntmSyncLpeInline = new MenuItem(mnuTools, SWT.CHECK);
        mntmSyncLpeInline.setSelection(WorkbenchManager.getUserSettingState().getSyncWithLpeInline().get());
        MiscToolItem.mntmSyncLpeInlinePtr[0] = mntmSyncLpeInline;
        mntmSyncLpeInline.setText(I18n.E3D_PARSE_INLINE);
    }

    private static void addListeners() {

        widgetUtil(mntmSelectAllPtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    vm.addSnapshot();
                    loadSelectorSettings();
                    vm.selectAll(sels, true);
                    vm.syncWithTextEditors(true);
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });
        widgetUtil(mntmSelectAllVisiblePtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    vm.addSnapshot();
                    loadSelectorSettings();
                    vm.selectAll(sels, false);
                    vm.syncWithTextEditors(true);
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });
        widgetUtil(mntmSelectAllWithColoursPtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    vm.addSnapshot();
                    loadSelectorSettings();
                    vm.selectAllWithSameColours(sels, true);
                    vm.syncWithTextEditors(true);
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });
        widgetUtil(mntmSelectAllVisibleWithColoursPtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    vm.addSnapshot();
                    loadSelectorSettings();
                    vm.selectAllWithSameColours(sels, false);
                    vm.syncWithTextEditors(true);
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });
        widgetUtil(mntmSelectNonePtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    vm.addSnapshot();
                    vm.clearSelection();
                    vm.syncWithTextEditors(true);
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });
        widgetUtil(mntmSelectInversePtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    vm.addSnapshot();
                    loadSelectorSettings();
                    vm.selectInverse(sels);
                    vm.syncWithTextEditors(true);
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });

        widgetUtil(mntmWithSameColourPtr[0]).addSelectionListener(e -> {
            Display.getCurrent().asyncExec(() -> {
                mntmSelectEverythingPtr[0].setEnabled(
                        mntmWithHiddenDataPtr[0].getSelection() ||
                        mntmWithSameColourPtr[0].getSelection() ||
                        mntmWithSameTypePtr[0].getSelection() ||
                        mntmWithSameOrientationPtr[0].getSelection() ||
                        mntmExceptSubfilesPtr[0].getSelection()
                        );
                showSelectMenu();
            });
            regainFocus();
        });

        widgetUtil(mntmWithSameTypePtr[0]).addSelectionListener(e -> {
            Display.getCurrent().asyncExec(() -> {
                mntmSelectEverythingPtr[0].setEnabled(
                        mntmWithHiddenDataPtr[0].getSelection() ||
                        mntmWithSameColourPtr[0].getSelection() ||
                        mntmWithSameTypePtr[0].getSelection() ||
                        mntmWithSameOrientationPtr[0].getSelection() ||
                        mntmExceptSubfilesPtr[0].getSelection()
                        );
                showSelectMenu();
            });
            regainFocus();
        });

        widgetUtil(mntmWithSameOrientationPtr[0]).addSelectionListener(e -> {
            Display.getCurrent().asyncExec(() -> {
                mntmSelectEverythingPtr[0].setEnabled(
                        mntmWithHiddenDataPtr[0].getSelection() ||
                        mntmWithSameColourPtr[0].getSelection() ||
                        mntmWithSameTypePtr[0].getSelection() ||
                        mntmWithSameOrientationPtr[0].getSelection() ||
                        mntmExceptSubfilesPtr[0].getSelection()
                        );
                if (mntmWithSameOrientationPtr[0].getSelection()) {


                    new ValueDialog(Editor3DWindow.getWindow().getShell(), I18n.E3D_ANGLE_DIFF, I18n.E3D_THRESH_IN_DEG) {

                        @Override
                        public void initializeSpinner() {
                            this.spnValuePtr[0].setMinimum(new BigDecimal("-90")); //$NON-NLS-1$
                            this.spnValuePtr[0].setMaximum(new BigDecimal("180")); //$NON-NLS-1$
                            this.spnValuePtr[0].setValue(sels.getAngle());
                        }

                        @Override
                        public void applyValue() {
                            sels.setAngle(this.spnValuePtr[0].getValue());
                        }
                    }.open();
                }
                showSelectMenu();
            });
            regainFocus();
        });

        widgetUtil(mntmWithAccuracyPtr[0]).addSelectionListener(e -> {
            Display.getCurrent().asyncExec(() -> {
                mntmSelectEverythingPtr[0].setEnabled(
                        mntmWithHiddenDataPtr[0].getSelection() ||
                        mntmWithSameColourPtr[0].getSelection() ||
                        mntmWithSameTypePtr[0].getSelection() ||
                        mntmWithSameOrientationPtr[0].getSelection() ||
                        mntmExceptSubfilesPtr[0].getSelection()
                        );
                if (mntmWithAccuracyPtr[0].getSelection()) {

                    new ValueDialog(Editor3DWindow.getWindow().getShell(), I18n.E3D_SET_ACCURACY, I18n.E3D_THRESH_IN_LDU) {

                        @Override
                        public void initializeSpinner() {
                            this.spnValuePtr[0].setMinimum(new BigDecimal("0")); //$NON-NLS-1$
                            this.spnValuePtr[0].setMaximum(new BigDecimal("1000")); //$NON-NLS-1$
                            this.spnValuePtr[0].setValue(sels.getEqualDistance());
                        }

                        @Override
                        public void applyValue() {
                            sels.setEqualDistance(this.spnValuePtr[0].getValue());
                        }
                    }.open();
                }
                showSelectMenu();
            });
            regainFocus();
        });
        widgetUtil(mntmWithHiddenDataPtr[0]).addSelectionListener(e -> {
            Display.getCurrent().asyncExec(() -> {
                mntmSelectEverythingPtr[0].setEnabled(
                        mntmWithHiddenDataPtr[0].getSelection() ||
                        mntmWithSameColourPtr[0].getSelection() ||
                        mntmWithSameTypePtr[0].getSelection() ||
                        mntmWithSameOrientationPtr[0].getSelection() ||
                        mntmExceptSubfilesPtr[0].getSelection()
                        );
                showSelectMenu();
            });
            regainFocus();
        });
        widgetUtil(mntmWithWholeSubfilesPtr[0]).addSelectionListener(e -> {
            Display.getCurrent().asyncExec(MiscToolItem::showSelectMenu);
            regainFocus();
        });
        widgetUtil(mntmWithAdjacencyPtr[0]).addSelectionListener(e -> {
            Display.getCurrent().asyncExec(MiscToolItem::showSelectMenu);
            regainFocus();
        });
        widgetUtil(mntmExceptSubfilesPtr[0]).addSelectionListener(e -> {
            Display.getCurrent().asyncExec(() -> {
                mntmSelectEverythingPtr[0].setEnabled(
                        mntmWithHiddenDataPtr[0].getSelection() ||
                        mntmWithSameColourPtr[0].getSelection() ||
                        mntmWithSameTypePtr[0].getSelection() ||
                        mntmWithSameOrientationPtr[0].getSelection() ||
                        mntmExceptSubfilesPtr[0].getSelection()
                        );
                mntmWithWholeSubfilesPtr[0].setEnabled(!mntmExceptSubfilesPtr[0].getSelection());
                showSelectMenu();
            });
            regainFocus();
        });
        widgetUtil(mntmStopAtEdgesPtr[0]).addSelectionListener(e -> {
            Display.getCurrent().asyncExec(MiscToolItem::showSelectMenu);
            regainFocus();
        });
        widgetUtil(mntmSAllTypesPtr[0]).addSelectionListener(e -> {
            Display.getCurrent().asyncExec(() -> {
                activateAllTypes();
                showSelectMenu();
            });
            regainFocus();
        });
        widgetUtil(mntmSNothingPtr[0]).addSelectionListener(e -> {
            Display.getCurrent().asyncExec(() -> {
                mntmSVerticesPtr[0].setSelection(false);
                mntmSLinesPtr[0].setSelection(false);
                mntmSTrianglesPtr[0].setSelection(false);
                mntmSQuadsPtr[0].setSelection(false);
                mntmSCLinesPtr[0].setSelection(false);
                showSelectMenu();
            });
            regainFocus();
        });
        widgetUtil(mntmSTrianglesPtr[0]).addSelectionListener(e -> {
            Display.getCurrent().asyncExec(MiscToolItem::showSelectMenu);
            regainFocus();
        });
        widgetUtil(mntmSQuadsPtr[0]).addSelectionListener(e -> {
            Display.getCurrent().asyncExec(MiscToolItem::showSelectMenu);
            regainFocus();
        });
        widgetUtil(mntmSCLinesPtr[0]).addSelectionListener(e -> {
            Display.getCurrent().asyncExec(MiscToolItem::showSelectMenu);
            regainFocus();
        });
        widgetUtil(mntmSVerticesPtr[0]).addSelectionListener(e -> {
            Display.getCurrent().asyncExec(MiscToolItem::showSelectMenu);
            regainFocus();
        });
        widgetUtil(mntmSLinesPtr[0]).addSelectionListener(e -> {
            Display.getCurrent().asyncExec(MiscToolItem::showSelectMenu);
            regainFocus();
        });

        widgetUtil(mntmSelectEverythingPtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    vm.addSnapshot();
                    sels.setScope(SelectorSettings.EVERYTHING);
                    loadSelectorSettings();
                    vm.selector(sels);
                    vm.syncWithTextEditors(true);
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });

        widgetUtil(mntmSelectConnectedPtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    vm.addSnapshot();
                    sels.setScope(SelectorSettings.CONNECTED);
                    loadSelectorSettings();
                    vm.selector(sels);
                    vm.syncWithTextEditors(true);
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });

        widgetUtil(mntmSelectTouchingPtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    vm.addSnapshot();
                    sels.setScope(SelectorSettings.TOUCHING);
                    loadSelectorSettings();
                    vm.selector(sels);
                    vm.syncWithTextEditors(true);
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });

        widgetUtil(mntmSelectIsolatedVerticesPtr[0]).addSelectionListener(e -> {
            Display.getCurrent().asyncExec(() -> {
                for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        vm.addSnapshot();
                        vm.selectIsolatedVertices();
                        vm.syncWithTextEditors(true);
                        regainFocus();
                        return;
                    }
                }
            });
            regainFocus();
        });

        widgetUtil(mntmSplitPtr[0]).addSelectionListener(e -> {
            Display.getCurrent().asyncExec(() -> {
                for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        vm.addSnapshot();
                        vm.split(2);
                        regainFocus();
                        return;
                    }
                }
            });
            regainFocus();
        });

        widgetUtil(mntmSplitNTimesPtr[0]).addSelectionListener(e -> {
            Display.getCurrent().asyncExec(() -> {
                for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {

                        final int[] frac = new int[]{2};
                        if (new ValueDialogInt(Editor3DWindow.getWindow().getShell(), I18n.E3D_SPLIT_EDGES, I18n.E3D_NUMBER_OF_FRACTIONS) {

                            @Override
                            public void initializeSpinner() {
                                this.spnValuePtr[0].setMinimum(2);
                                this.spnValuePtr[0].setMaximum(1000);
                                this.spnValuePtr[0].setValue(2);
                            }

                            @Override
                            public void applyValue() {
                                frac[0] = this.spnValuePtr[0].getValue();
                            }
                        }.open() == Window.OK) {

                            VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                            vm.addSnapshot();
                            vm.split(frac[0]);
                            regainFocus();
                            return;
                        }
                    }
                }
            });
            regainFocus();
        });

        widgetUtil(mntmSmoothPtr[0]).addSelectionListener(e -> {
            Display.getCurrent().asyncExec(() -> {
                for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                        OpenGLRenderer.getSmoothing().set(true);
                        if (new SmoothDialog(Editor3DWindow.getWindow().getShell()).open() == Window.OK) {
                            VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                            vm.addSnapshot();
                            vm.smooth(SmoothDialog.isX(), SmoothDialog.isY(), SmoothDialog.isZ(), SmoothDialog.getFactor(), SmoothDialog.getIterations());
                            regainFocus();
                        }
                        OpenGLRenderer.getSmoothing().set(false);
                        return;
                    }
                }
            });
            regainFocus();
        });

        widgetUtil(mntmMergeToAveragePtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    vm.addSnapshot();
                    vm.merge(MergeTo.AVERAGE, true, false);
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });
        widgetUtil(mntmMergeToLastSelectedPtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    vm.addSnapshot();
                    vm.merge(MergeTo.LAST_SELECTED, true, false);
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });
        widgetUtil(mntmMergeToNearestVertexPtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    vm.addSnapshot();
                    vm.merge(MergeTo.NEAREST_VERTEX, true, false);
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });
        widgetUtil(mntmMergeToNearestEdgePtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    vm.addSnapshot();
                    vm.merge(MergeTo.NEAREST_EDGE, true, false);
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });
        widgetUtil(mntmMergeToNearestEdgeSplitPtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    vm.addSnapshot();
                    vm.merge(MergeTo.NEAREST_EDGE_SPLIT, true, false);
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });
        widgetUtil(mntmMergeToNearestFacePtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    vm.addSnapshot();
                    vm.merge(MergeTo.NEAREST_FACE, true, false);
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });

        widgetUtil(mntmMergeToNearestFaceDirPtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    vm.addSnapshot();
                    final WorkingMode action = TransformationModeToolItem.getWorkingAction();
                    if (DirectionDialog.getTransformationMode() == ManipulatorScope.GLOBAL) {
                        TransformationModeToolItem.setWorkingAction(WorkingMode.MOVE_GLOBAL);
                    } else {
                        TransformationModeToolItem.setWorkingAction(WorkingMode.MOVE);
                    }
                    vm.merge(MergeTo.NEAREST_FACE, true, true);
                    TransformationModeToolItem.setWorkingAction(action);
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });
        
        widgetUtil(mntmSnapToGridPtr[0]).addSelectionListener(e -> {
            final Composite3D c3d = Editor3DWindow.getWindow().getCurrentCoposite3d();
            if (c3d != null && c3d.isClassicPerspective()) {
                final boolean snapOnX;
                final boolean snapOnY;
                final boolean snapOnZ;
                final Perspective perspective = c3d.getPerspectiveIndex();
                switch (perspective) {
                case FRONT, BACK:
                    snapOnX = true;
                    snapOnY = true;
                    snapOnZ = false;
                    break;
                case TOP, BOTTOM:
                    snapOnX = true;
                    snapOnY = false;
                    snapOnZ = true;
                    break;
                case LEFT, RIGHT:
                    snapOnX = false;
                    snapOnY = true;
                    snapOnZ = true;
                    break;
                case TWO_THIRDS:
                default:
                    return;
                }
                
                NLogger.debug(MiscToggleToolItem.class, "Perspective :" + perspective); //$NON-NLS-1$
                NLogger.debug(MiscToggleToolItem.class, "Snap on X   :" + snapOnX); //$NON-NLS-1$
                NLogger.debug(MiscToggleToolItem.class, "Snap on Y   :" + snapOnY); //$NON-NLS-1$
                NLogger.debug(MiscToggleToolItem.class, "Snap on Z   :" + snapOnZ); //$NON-NLS-1$
                
                final VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                final float gridSize = c3d.getGridSize();
                final Set<Vertex> sv = vm.getSelectedVertices();
                
                NLogger.debug(MiscToggleToolItem.class, "Grid size   :" + gridSize + " LDU"); //$NON-NLS-1$ //$NON-NLS-2$
                NLogger.debug(MiscToggleToolItem.class, "Vertex count:" + sv.size()); //$NON-NLS-1$
                
                // FIXME Needs implementation!
            }
        });

        widgetUtil(mntmSelectSingleVertexPtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                    final VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    final Set<Vertex> sv = vm.getSelectedVertices();
                    if (new VertexDialog(Editor3DWindow.getWindow().getShell()).open() == IDialogConstants.OK_ID) {
                        Vertex v = VertexDialog.getVertex();
                        if (vm.getVertices().contains(v)) {
                            sv.add(v);
                            vm.syncWithTextEditors(true);
                        }
                    }
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });

        widgetUtil(mntmSetXYZPtr[0]).addSelectionListener(e -> {
            final boolean noReset = !Cocoa.checkCtrlOrCmdPressed(e.stateMask);
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                    Vertex v = new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
                    final VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    final Set<Vertex> sv = vm.getSelectedVertices();
                    if (sv.size() == 1 && vm.getSelectedData().isEmpty()) {
                        v = sv.iterator().next();
                    } else {
                        v = new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
                        vm.selectObjectVertices();
                        CoordinatesDialog.setStart(null);
                        CoordinatesDialog.setEnd(null);
                    }
                    final Vertex mani = new Vertex(c3d.getManipulator().getAccuratePosition());
                    final WorkingMode action = TransformationModeToolItem.getWorkingAction();
                    if (CoordinatesDialog.getTransformationMode() == ManipulatorScope.GLOBAL) {
                        TransformationModeToolItem.setWorkingAction(WorkingMode.MOVE_GLOBAL);
                    } else {
                        TransformationModeToolItem.setWorkingAction(WorkingMode.MOVE);
                    }
                    if (new CoordinatesDialog(Editor3DWindow.getWindow().getShell(), v, mani, c3d.getManipulator()).open() == IDialogConstants.OK_ID) {

                        if (CoordinatesDialog.getTransformationMode() == ManipulatorScope.LOCAL) {
                            BigDecimal[] pos = c3d.getManipulator().getAccuratePosition();
                            CoordinatesDialog.setVertex(
                                    new Vertex(
                                            Vector3d.add(
                                                    c3d.getManipulator().getAccurateRotation().invert().transform(new Vector3d(CoordinatesDialog.getVertex())),
                                                    new Vector3d(pos[0], pos[1], pos[2]))));
                        }

                        vm.addSnapshot();
                        int coordCount = 0;
                        coordCount += CoordinatesDialog.isX() ? 1 : 0;
                        coordCount += CoordinatesDialog.isY() ? 1 : 0;
                        coordCount += CoordinatesDialog.isZ() ? 1 : 0;
                        if (coordCount == 1 && CoordinatesDialog.getStart() != null) {
                            SortedSet<Vertex> verts1 = new TreeSet<>();
                            verts1.addAll(vm.getSelectedVertices());
                            vm.clearSelection();
                            for (Vertex v21 : verts1) {
                                final boolean a1 = CoordinatesDialog.isX();
                                final boolean b1 = CoordinatesDialog.isY();
                                final boolean c1 = CoordinatesDialog.isZ();
                                vm.getSelectedVertices().add(v21);
                                Vector3d delta1 = Vector3d.sub(CoordinatesDialog.getEnd(), CoordinatesDialog.getStart());
                                boolean doMoveOnLine1 = false;
                                BigDecimal s1 = BigDecimal.ZERO;
                                Vector3d v11 = CoordinatesDialog.getStart();
                                if (CoordinatesDialog.isX() && delta1.x.compareTo(BigDecimal.ZERO) != 0) {
                                    doMoveOnLine1 = true;
                                    s1 = v21.xp.subtract(CoordinatesDialog.getStart().x).divide(delta1.x, Threshold.MC);
                                } else if (CoordinatesDialog.isY() && delta1.y.compareTo(BigDecimal.ZERO) != 0) {
                                    doMoveOnLine1 = true;
                                    s1 = v21.yp.subtract(CoordinatesDialog.getStart().y).divide(delta1.y, Threshold.MC);
                                } else if (CoordinatesDialog.isZ() && delta1.z.compareTo(BigDecimal.ZERO) != 0) {
                                    doMoveOnLine1 = true;
                                    s1 = v21.zp.subtract(CoordinatesDialog.getStart().z).divide(delta1.z, Threshold.MC);
                                }
                                if (doMoveOnLine1) {
                                    CoordinatesDialog.setVertex(new Vertex(v11.x.add(delta1.x.multiply(s1)), v11.y.add(delta1.y.multiply(s1)), v11.z.add(delta1.z.multiply(s1))));
                                    CoordinatesDialog.setX(true);
                                    CoordinatesDialog.setY(true);
                                    CoordinatesDialog.setZ(true);
                                }
                                vm.setXyzOrTranslateOrTransform(CoordinatesDialog.getVertex(), null, TransformationMode.SET, CoordinatesDialog.isX(), CoordinatesDialog.isY(), CoordinatesDialog.isZ(), true, true, CoordinatesDialog.getTransformationMode());
                                vm.clearSelection();
                                CoordinatesDialog.setX(a1);
                                CoordinatesDialog.setY(b1);
                                CoordinatesDialog.setZ(c1);
                            }
                        } else if (coordCount == 2 && CoordinatesDialog.getStart() != null) {
                            SortedSet<Vertex> verts2 = new TreeSet<>();
                            verts2.addAll(vm.getSelectedVertices());
                            vm.clearSelection();
                            for (Vertex v22 : verts2) {
                                final boolean a2 = CoordinatesDialog.isX();
                                final boolean b2 = CoordinatesDialog.isY();
                                final boolean c2 = CoordinatesDialog.isZ();
                                vm.getSelectedVertices().add(v22);
                                Vector3d delta2 = Vector3d.sub(CoordinatesDialog.getEnd(), CoordinatesDialog.getStart());
                                boolean doMoveOnLine2 = false;
                                BigDecimal s2 = BigDecimal.ZERO;
                                Vector3d v12 = CoordinatesDialog.getStart();
                                if (CoordinatesDialog.isX() && delta2.x.compareTo(BigDecimal.ZERO) != 0) {
                                    doMoveOnLine2 = true;
                                    s2 = v22.xp.subtract(CoordinatesDialog.getStart().x).divide(delta2.x, Threshold.MC);
                                } else if (CoordinatesDialog.isY() && delta2.y.compareTo(BigDecimal.ZERO) != 0) {
                                    doMoveOnLine2 = true;
                                    s2 = v22.yp.subtract(CoordinatesDialog.getStart().y).divide(delta2.y, Threshold.MC);
                                } else if (CoordinatesDialog.isZ() && delta2.z.compareTo(BigDecimal.ZERO) != 0) {
                                    doMoveOnLine2 = true;
                                    s2 = v22.zp.subtract(CoordinatesDialog.getStart().z).divide(delta2.z, Threshold.MC);
                                }
                                BigDecimal xP = !CoordinatesDialog.isX() ? v12.x.add(delta2.x.multiply(s2)) : v22.xp;
                                BigDecimal yP = !CoordinatesDialog.isY() ? v12.y.add(delta2.y.multiply(s2)) : v22.yp;
                                BigDecimal zP = !CoordinatesDialog.isZ() ? v12.z.add(delta2.z.multiply(s2)) : v22.zp;
                                if (doMoveOnLine2) {
                                    CoordinatesDialog.setVertex(new Vertex(xP, yP, zP));
                                    CoordinatesDialog.setX(true);
                                    CoordinatesDialog.setY(true);
                                    CoordinatesDialog.setZ(true);
                                }
                                vm.setXyzOrTranslateOrTransform(CoordinatesDialog.getVertex(), null, TransformationMode.SET, CoordinatesDialog.isX(), CoordinatesDialog.isY(), CoordinatesDialog.isZ(), true, true, CoordinatesDialog.getTransformationMode());
                                vm.clearSelection();
                                CoordinatesDialog.setX(a2);
                                CoordinatesDialog.setY(b2);
                                CoordinatesDialog.setZ(c2);
                            }
                        } else {
                            final boolean moveAdjacentData = MiscToggleToolItem.isMovingAdjacentData();
                            if (CoordinatesDialog.isCreatingCopy()) {
                                vm.copy();
                                vm.paste(null);
                                MiscToggleToolItem.setMovingAdjacentData(false);
                                vm.setXyzOrTranslateOrTransform(CoordinatesDialog.getVertex(), null, TransformationMode.SET, CoordinatesDialog.isX(), CoordinatesDialog.isY(), CoordinatesDialog.isZ(), false, true, CoordinatesDialog.getTransformationMode());
                                MiscToggleToolItem.setMovingAdjacentData(moveAdjacentData);
                            } else {
                                vm.setXyzOrTranslateOrTransform(CoordinatesDialog.getVertex(), null, TransformationMode.SET, CoordinatesDialog.isX(), CoordinatesDialog.isY(), CoordinatesDialog.isZ(), MiscToggleToolItem.isMovingAdjacentData() || vm.getSelectedData().isEmpty() || vm.getSelectedVertices().size() == 1, true, CoordinatesDialog.getTransformationMode());
                            }
                        }

                        if (noReset) {
                            CoordinatesDialog.setStart(null);
                            CoordinatesDialog.setEnd(null);
                        }
                    }
                    TransformationModeToolItem.setWorkingAction(action);
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });

        widgetUtil(mntmTranslatePtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())
                        && c3d.equals(DatFile.getLastHoveredComposite())
                        && !c3d.getLockableDatFileReference().isReadOnly()) {
                    final WorkingMode action = TransformationModeToolItem.getWorkingAction();
                    if (TranslateDialog.getTransformationMode() == ManipulatorScope.GLOBAL) {
                        TransformationModeToolItem.setWorkingAction(WorkingMode.MOVE_GLOBAL);
                    } else {
                        TransformationModeToolItem.setWorkingAction(WorkingMode.MOVE);
                    }
                    if (new TranslateDialog(Editor3DWindow.getWindow().getShell(), new Vertex(c3d.getManipulator().getAccuratePosition()), ManipulatorScopeToolItem.getTransformationScope()).open() == IDialogConstants.OK_ID) {
                        c3d.getLockableDatFileReference().getVertexManager().addSnapshot();
                        final boolean moveAdjacentData = MiscToggleToolItem.isMovingAdjacentData();
                        final int iterations = TranslateDialog.getAndResetIterations();
                        for (int i = 0; i < iterations; i++) {
                            if (TranslateDialog.isCreatingCopy()) {
                                c3d.getLockableDatFileReference().getVertexManager().copy();
                                c3d.getLockableDatFileReference().getVertexManager().paste(null);
                                MiscToggleToolItem.setMovingAdjacentData(false);
                            }
                            c3d.getLockableDatFileReference().getVertexManager().setXyzOrTranslateOrTransform(TranslateDialog.getOffset(), null, TransformationMode.TRANSLATE, TranslateDialog.isX(), TranslateDialog.isY(), TranslateDialog.isZ(), MiscToggleToolItem.isMovingAdjacentData(), true, TranslateDialog.getTransformationMode());
                        }
                        if (TranslateDialog.isCreatingCopy()) {
                            MiscToggleToolItem.setMovingAdjacentData(moveAdjacentData);
                        }
                    }
                    TransformationModeToolItem.setWorkingAction(action);
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });

        widgetUtil(mntmRotatePtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                    SortedSet<Vertex> clipboard = new TreeSet<>();
                    if (VertexManager.getClipboard().size() == 1) {
                        GData vertex = VertexManager.getClipboard().get(0);
                        if (vertex.type() == 0) {
                            String line = vertex.toString();
                            line = line.replaceAll("\\s+", " ").trim(); //$NON-NLS-1$ //$NON-NLS-2$
                            String[] dataSegments = line.split("\\s+"); //$NON-NLS-1$
                            if (line.startsWith("0 !LPE") && line.startsWith("VERTEX ", 7)) { //$NON-NLS-1$ //$NON-NLS-2$
                                Vector3d start = new Vector3d();
                                boolean numberError = false;
                                if (dataSegments.length == 6) {
                                    try {
                                        start.setX(new BigDecimal(dataSegments[3], Threshold.MC));
                                        start.setY(new BigDecimal(dataSegments[4], Threshold.MC));
                                        start.setZ(new BigDecimal(dataSegments[5], Threshold.MC));
                                    } catch (NumberFormatException nfe) {
                                        numberError = true;
                                    }
                                } else {
                                    numberError = true;
                                }
                                if (!numberError) {
                                    clipboard.add(new Vertex(start));
                                }
                            }
                        }
                    }
                    final Vertex mani = new Vertex(c3d.getManipulator().getAccuratePosition());
                    final WorkingMode action = TransformationModeToolItem.getWorkingAction();
                    if (RotateDialog.getTransformationMode() == ManipulatorScope.GLOBAL) {
                        TransformationModeToolItem.setWorkingAction(WorkingMode.MOVE_GLOBAL);
                    } else {
                        TransformationModeToolItem.setWorkingAction(WorkingMode.MOVE);
                    }
                    if (new RotateDialog(Editor3DWindow.getWindow().getShell(), null, clipboard, mani, ManipulatorScopeToolItem.getTransformationScope()).open() == IDialogConstants.OK_ID) {
                        c3d.getLockableDatFileReference().getVertexManager().addSnapshot();
                        final boolean moveAdjacentData = MiscToggleToolItem.isMovingAdjacentData();
                        final int iterations = RotateDialog.getAndResetIterations();
                        for (int i = 0; i < iterations; i++) {
                            if (RotateDialog.isCreatingCopy()) {
                                c3d.getLockableDatFileReference().getVertexManager().copy();
                                c3d.getLockableDatFileReference().getVertexManager().paste(null);
                                MiscToggleToolItem.setMovingAdjacentData(false);
                            }
                            c3d.getLockableDatFileReference().getVertexManager().setXyzOrTranslateOrTransform(RotateDialog.getAngles(), RotateDialog.getPivot(), TransformationMode.ROTATE, RotateDialog.isX(), RotateDialog.isY(), RotateDialog.isZ(), MiscToggleToolItem.isMovingAdjacentData(), true, RotateDialog.getTransformationMode());
                        }
                        if (RotateDialog.isCreatingCopy()) {
                            MiscToggleToolItem.setMovingAdjacentData(moveAdjacentData);
                        }
                    }
                    TransformationModeToolItem.setWorkingAction(action);
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });

        widgetUtil(mntmScalePtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                    SortedSet<Vertex> clipboard = new TreeSet<>();
                    if (VertexManager.getClipboard().size() == 1) {
                        GData vertex = VertexManager.getClipboard().get(0);
                        if (vertex.type() == 0) {
                            String line = vertex.toString();
                            line = line.replaceAll("\\s+", " ").trim(); //$NON-NLS-1$ //$NON-NLS-2$
                            String[] dataSegments = line.split("\\s+"); //$NON-NLS-1$
                            if (line.startsWith("0 !LPE") && line.startsWith("VERTEX ", 7)) { //$NON-NLS-1$ //$NON-NLS-2$
                                Vector3d start = new Vector3d();
                                boolean numberError = false;
                                if (dataSegments.length == 6) {
                                    try {
                                        start.setX(new BigDecimal(dataSegments[3], Threshold.MC));
                                        start.setY(new BigDecimal(dataSegments[4], Threshold.MC));
                                        start.setZ(new BigDecimal(dataSegments[5], Threshold.MC));
                                    } catch (NumberFormatException nfe) {
                                        numberError = true;
                                    }
                                } else {
                                    numberError = true;
                                }
                                if (!numberError) {
                                    clipboard.add(new Vertex(start));
                                }
                            }
                        }
                    }
                    final Vertex mani = new Vertex(c3d.getManipulator().getAccuratePosition());
                    final WorkingMode action = TransformationModeToolItem.getWorkingAction();
                    if (ScaleDialog.getTransformationMode() == ManipulatorScope.GLOBAL) {
                        TransformationModeToolItem.setWorkingAction(WorkingMode.MOVE_GLOBAL);
                    } else {
                        TransformationModeToolItem.setWorkingAction(WorkingMode.MOVE);
                    }
                    if (new ScaleDialog(Editor3DWindow.getWindow().getShell(), null, clipboard, mani, ManipulatorScopeToolItem.getTransformationScope()).open() == IDialogConstants.OK_ID) {
                        c3d.getLockableDatFileReference().getVertexManager().addSnapshot();
                        Editor3DWindow.getWindow().updateInitialScale(BigDecimal.ZERO, BigDecimal.ZERO, true);
                        final boolean moveAdjacentData = MiscToggleToolItem.isMovingAdjacentData();
                        if (ScaleDialog.isCreatingCopy()) {
                            c3d.getLockableDatFileReference().getVertexManager().copy();
                            c3d.getLockableDatFileReference().getVertexManager().paste(null);
                            MiscToggleToolItem.setMovingAdjacentData(false);
                        }
                        c3d.getLockableDatFileReference().getVertexManager().setXyzOrTranslateOrTransform(ScaleDialog.getScaleFactors(), ScaleDialog.getPivot(), TransformationMode.SCALE, ScaleDialog.isX(), ScaleDialog.isY(), ScaleDialog.isZ(), MiscToggleToolItem.isMovingAdjacentData(), true, ScaleDialog.getTransformationMode());
                        if (ScaleDialog.isCreatingCopy()) {
                            MiscToggleToolItem.setMovingAdjacentData(moveAdjacentData);
                        }
                    }
                    TransformationModeToolItem.setWorkingAction(action);
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });

        widgetUtil(mntmPartReviewPtr[0]).addSelectionListener(new WidgetSelectionListener() {

            final Pattern whitespace = Pattern.compile("\\s+"); //$NON-NLS-1$
            final Pattern pattern = Pattern.compile("\r?\n|\r"); //$NON-NLS-1$

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (new PartReviewDialog(Editor3DWindow.getWindow().getShell(), Editor3DWindow.getWindow().isReviewingAPart()).open() == IDialogConstants.OK_ID) {
                    if (Editor3DWindow.getWindow().isReviewingAPart()) {
                        Editor3DWindow.getWindow().setReviewingAPart(false, null);
                        return;
                    }

                    final Editor3DWindowState winState = WorkbenchManager.getEditor3DWindowState();
                    winState.setThreeDwindowConfig(Editor3DWindow.getWindow().getC3DStates());

                    try {
                        new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, false, new IRunnableWithProgress() {
                            @Override
                            public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                                monitor.beginTask(I18n.PARTREVIEW_TITLE, IProgressMonitor.UNKNOWN);

                                String fileName = PartReviewDialog.getFileName().toLowerCase(Locale.ENGLISH);
                                if (!fileName.endsWith(".dat")) fileName = fileName + ".dat"; //$NON-NLS-1$ //$NON-NLS-2$
                                String oldFileName = fileName;
                                oldFileName = oldFileName.replace("\\", File.separator); //$NON-NLS-1$
                                fileName = fileName.replace("\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$

                                // Download first, then build the views

                                // https://library.ldraw.org/library/unofficial
                                monitor.beginTask(fileName, IProgressMonitor.UNKNOWN);
                                String source = FileHelper.downloadPartFile("parts/" + fileName, monitor); //$NON-NLS-1$
                                if (source == null) source = FileHelper.downloadPartFile("parts/s/" + fileName, monitor); //$NON-NLS-1$
                                if (source == null) source = FileHelper.downloadPartFile("p/" + fileName, monitor); //$NON-NLS-1$
                                if (source == null) source = FileHelper.downloadPartFile("p/8/" + fileName, monitor); //$NON-NLS-1$
                                if (source == null) source = FileHelper.downloadPartFile("p/48/" + fileName, monitor); //$NON-NLS-1$
                                if (source == null) {
                                    Display.getDefault().syncExec(() -> {
                                        MessageBox messageBox = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_ERROR | SWT.OK);
                                        messageBox.setText(I18n.DIALOG_ERROR);
                                        messageBox.setMessage(I18n.PARTREVIEW_ERROR);
                                        messageBox.open();
                                    });
                                    return;
                                }

                                Set<String> files = new HashSet<>();
                                files.add(fileName);
                                List<String> list = buildFileList(source, new ArrayList<>(), files, monitor);
                                
                                if (WorkbenchManager.getUserSettingState().isVerbosePartReview()) {
                                    String mainFileName = fileName;
                                    Object[] messageArguments = { fileName,
                                            files.stream().filter(n -> !mainFileName.equals(n)).sorted().collect(Collectors.joining(", ")) }; //$NON-NLS-1$
                                    MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                                    formatter.setLocale(MyLanguage.getLocale());
                                    formatter.applyPattern(I18n.PARTREVIEW_VERBOSE_MSG);
                                    Display.getDefault().syncExec(() -> {
                                        MessageBox messageBox = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_INFORMATION | SWT.OK);
                                        messageBox.setText(I18n.DIALOG_INFO);
                                        messageBox.setMessage(formatter.format(messageArguments));
                                        messageBox.open();
                                    });
                                }

                                final String fileName2 = fileName;
                                final String source2 = source;
                                final String oldFileName2 = oldFileName;
                                Display.getDefault().asyncExec(() -> {

                                    String fileName3 = fileName2;
                                    String source3 = source2;

                                    Editor3DWindow.getWindow().closeAllComposite3D();
                                    Set<EditorTextWindow> windows = new HashSet<>(Project.getOpenTextWindows());
                                    for (EditorTextWindow txtwin : windows) {
                                        if (txtwin.isSeperateWindow()) {
                                            txtwin.getShell().close();
                                        } else {
                                            txtwin.closeAllTabs();
                                        }
                                    }
                                    Project.setDefaultProject(true);
                                    Project.setProjectPath(new File(Project.DEFAULT_PROJECT_PATH).getAbsolutePath());
                                    Editor3DWindow.getWindow().getShell().setText(Version.getApplicationName() + " " + Version.getVersion() + " (" + WorkbenchManager.getUserSettingState().getOpenGLVersionString() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    Editor3DWindow.getWindow().getShell().update();
                                    Editor3DWindow.getWindow().getProject().setText(fileName3);
                                    Editor3DWindow.getWindow().getProject().setData(Project.getProjectPath());

                                    Editor3DWindow.getWindow().getProjectParts().getItems().clear();
                                    Editor3DWindow.getWindow().getProjectSubparts().getItems().clear();
                                    Editor3DWindow.getWindow().getProjectPrimitives().getItems().clear();

                                    Editor3DWindow.getWindow().getOfficialParts().setData(null);

                                    list.add(0, new File(Project.DEFAULT_PROJECT_PATH).getAbsolutePath() + File.separator + oldFileName2);
                                    list.add(1, source3);

                                    DatFile main = View.DUMMY_DATFILE;

                                    Set<DatFile> dfsToOpen = new HashSet<>();
                                    List<DatFile> tempFileList = new ArrayList<>();
                                    tempFileList.addAll(Project.getOpenedFiles());

                                    for (int i =  list.size() - 2; i >= 0; i -= 2) {
                                        DatFile df;
                                        TreeItem n;
                                        fileName3 = list.get(i);
                                        source3 = list.get(i + 1);
                                        df = DatFile.createDatFileForReview(fileName3);
                                        monitor.beginTask(fileName3, IProgressMonitor.UNKNOWN);
                                        Display.getCurrent().readAndDispatch();
                                        dfsToOpen.add(df);
                                        df.setText(source3);
                                        // Add / remove from unsaved files is mandatory!
                                        Project.addUnsavedFile(df);
                                        df.parseForData(true);
                                        Project.removeUnsavedFile(df);
                                        Project.getParsedFiles().remove(df);
                                        if (source3.contains("0 !LDRAW_ORG Unofficial_Subpart")) { //$NON-NLS-1$
                                            int ind = fileName3.lastIndexOf(File.separator + "s" + File.separator); //$NON-NLS-1$
                                            if (ind >= 0) {
                                                fileName3 = new StringBuilder(fileName3).replace(ind, ind + File.separator.length() * 2 + 1, File.separator + "parts" + File.separator + "s" + File.separator).toString();  //$NON-NLS-1$ //$NON-NLS-2$
                                            }
                                            n = new TreeItem(Editor3DWindow.getWindow().getProjectSubparts());
                                            df.setType(DatType.SUBPART);
                                        } else if (source3.contains("0 !LDRAW_ORG Unofficial_Primitive")) { //$NON-NLS-1$
                                            int ind = fileName3.lastIndexOf(File.separator);
                                            if (ind >= 0) {
                                                fileName3 = new StringBuilder(fileName3).replace(ind, ind + File.separator.length(), File.separator + "p" + File.separator).toString();  //$NON-NLS-1$
                                            }
                                            n = new TreeItem(Editor3DWindow.getWindow().getProjectPrimitives());
                                            df.setType(DatType.PRIMITIVE);
                                        } else if (source3.contains("0 !LDRAW_ORG Unofficial_48_Primitive")) { //$NON-NLS-1$
                                            int ind = fileName3.lastIndexOf(File.separator + "48" + File.separator); //$NON-NLS-1$
                                            if (ind >= 0) {
                                                fileName3 = new StringBuilder(fileName3).replace(ind, ind + File.separator.length() * 2 + 2, File.separator + "p" + File.separator  + "48" + File.separator).toString();  //$NON-NLS-1$ //$NON-NLS-2$
                                            }
                                            n = new TreeItem(Editor3DWindow.getWindow().getProjectPrimitives48());
                                            df.setType(DatType.PRIMITIVE48);
                                        } else if (source3.contains("0 !LDRAW_ORG Unofficial_8_Primitive")) { //$NON-NLS-1$
                                            int ind = fileName3.lastIndexOf(File.separator + "8" + File.separator); //$NON-NLS-1$
                                            if (ind >= 0) {
                                                fileName3 = new StringBuilder(fileName3).replace(ind, ind + File.separator.length() * 2 + 1, File.separator + "p" + File.separator  + "8" + File.separator).toString();  //$NON-NLS-1$ //$NON-NLS-2$
                                            }
                                            n = new TreeItem(Editor3DWindow.getWindow().getProjectPrimitives8());
                                            df.setType(DatType.PRIMITIVE8);
                                        } else {
                                            int ind = fileName3.lastIndexOf(File.separator);
                                            if (ind >= 0) {
                                                fileName3 = new StringBuilder(fileName3).replace(ind, ind + File.separator.length(), File.separator + "parts" + File.separator).toString();  //$NON-NLS-1$
                                            }
                                            n = new TreeItem(Editor3DWindow.getWindow().getProjectParts());
                                            df.setType(DatType.PART);
                                        }

                                        df.setNewName(fileName3);
                                        df.setOldName(fileName3);

                                        for (DatFile datFile : tempFileList) {
                                            if (df.equals(datFile)) {
                                                Project.getParsedFiles().remove(datFile);
                                                Project.removeUnsavedFile(datFile);
                                                datFile.disposeData();
                                                Project.getOpenedFiles().remove(datFile);
                                            }
                                        }

                                        Project.addUnsavedFile(df);
                                        Project.getParsedFiles().add(df);
                                        Project.addOpenedFile(df);

                                        n.setText(fileName2);
                                        n.setData(df);

                                        if (i == 0) {
                                            main = df;
                                        }
                                    }

                                    dfsToOpen.remove(main);

                                    Editor3DWindow.getWindow().resetSearch();

                                    Editor3DWindow.getWindow().getProject().getParent().build();
                                    Editor3DWindow.getWindow().getProject().getParent().redraw();
                                    Editor3DWindow.getWindow().getProject().getParent().update();

                                    Project.setFileToEdit(main);

                                    Editor3DWindow.getWindow().openDatFile(main, OpenInWhat.EDITOR_3D, null);
                                    Editor3DWindow.getRenders().get(0).getC3D().getModifier().splitViewHorizontally();
                                    Editor3DWindow.getRenders().get(0).getC3D().getModifier().splitViewVertically();
                                    Editor3DWindow.getRenders().get(1).getC3D().getModifier().splitViewVertically();

                                    int state = 0;
                                    for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                                        Composite3D c3d = renderer.getC3D();
                                        WidgetSelectionHelper.unselectAllChildButtons(c3d.mnuRenderMode);
                                        if (state == 0) {
                                            c3d.mntmNoBFCPtr[0].setSelection(true);
                                            c3d.getMntmStudLogo().setSelection(true);
                                            c3d.getModifier().switchShowingLogo(true);
                                            c3d.getModifier().setRenderMode(0);
                                        }
                                        if (state == 1) {
                                            c3d.mntmRandomColoursPtr[0].setSelection(true);
                                            c3d.getModifier().setRenderMode(1);
                                        }
                                        if (state == 2) {
                                            c3d.mntmCondlineModePtr[0].setSelection(true);
                                            c3d.getModifier().setRenderMode(6);
                                        }
                                        if (state == 3) {
                                            c3d.mntmWireframeModePtr[0].setSelection(true);
                                            c3d.getModifier().setRenderMode(-1);
                                        }
                                        state++;
                                    }

                                    Editor3DWindow.getWindow().updateTreeUnsavedEntries();

                                    EditorTextWindow txt;

                                    if (Project.getOpenTextWindows().isEmpty()) {
                                        txt = EditorTextWindow.createNewWindowIfRequired(main);
                                    } else {
                                        txt = Project.getOpenTextWindows().iterator().next();
                                    }

                                    for (DatFile df : dfsToOpen) {
                                        txt.openNewDatFileTab(df, false);
                                    }

                                    Editor3DWindow.getWindow().setReviewingAPart(true, dfsToOpen);
                                    regainFocus();
                                });
                            }
                        });
                    } catch (InvocationTargetException ex) {
                        NLogger.error(MiscToolItem.class, ex);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new LDPartEditorException(ie);
                    }
                }
            }

            private List<String> buildFileList(String source, List<String> result, Set<String> files, final IProgressMonitor monitor) {
                String[] lines;

                lines = pattern.split(source, -1);

                for (String line : lines) {
                    line = line.trim();
                    if (line.startsWith("1 ")) { //$NON-NLS-1$
                        final String[] dataSegments = whitespace.split(line.trim());
                        if (dataSegments.length > 14) {
                            StringBuilder sb = new StringBuilder();
                            for (int s = 14; s < dataSegments.length - 1; s++) {
                                sb.append(dataSegments[s]);
                                sb.append(" "); //$NON-NLS-1$
                            }
                            sb.append(dataSegments[dataSegments.length - 1]);
                            String fileName = sb.toString();
                            fileName = fileName.toLowerCase(Locale.ENGLISH);
                            fileName = fileName.replace("\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$

                            if (files.contains(fileName)) continue;
                            files.add(fileName);
                            monitor.subTask(I18n.PARTREVIEW_CHECK + fileName);
                            String source2 = FileHelper.downloadPartFile("parts/" + fileName, monitor); //$NON-NLS-1$
                            if (source2 == null) source2 = FileHelper.downloadPartFile("parts/s/" + fileName, monitor); //$NON-NLS-1$
                            if (source2 == null) source2 = FileHelper.downloadPartFile("p/" + fileName, monitor); //$NON-NLS-1$

                            if (source2 != null) {

                                fileName = fileName.replace("/", File.separator); //$NON-NLS-1$
                                fileName = fileName.replace("\\", File.separator); //$NON-NLS-1$

                                result.add(new File(Project.DEFAULT_PROJECT_PATH).getAbsolutePath() + File.separator + fileName);
                                result.add(source2);
                                buildFileList(source2, result, files, monitor);
                            }
                        }
                    }
                }
                return result;
            }
        });

        widgetUtil(btnSplitQuadPtr[0]).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null && !Project.getFileToEdit().isReadOnly()) {
                Project.getFileToEdit().getVertexManager().addSnapshot();
                Project.getFileToEdit().getVertexManager().splitQuads(true);
            }
            regainFocus();
        });

        widgetUtil(btnMergeQuadPtr[0]).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null && !Project.getFileToEdit().isReadOnly()) {
                Project.getFileToEdit().getVertexManager().addSnapshot();
                RectifierSettings rectifierSettings = new RectifierSettings();
                rectifierSettings.setScope(1);
                rectifierSettings.setNoBorderedQuadToRectConversation(true);
                Project.getFileToEdit().getVertexManager().rectify(rectifierSettings, true, true);
            }
            regainFocus();
        });

        widgetUtil(btnLineIntersectionPtr[0]).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null && !Project.getFileToEdit().isReadOnly()) {
                Project.getFileToEdit().getVertexManager().addSnapshot();
                if (Cocoa.checkCtrlOrCmdPressed(e.stateMask)) {
                    Project.getFileToEdit().getVertexManager().intersectionVerticesBetweenLinesAndSurfaces3D(true);
                } else {
                    Project.getFileToEdit().getVertexManager().intersectionVerticesBetweenLines3D(true);
                }
            }
            regainFocus();
        });

        widgetUtil(btnCondlineToLinePtr[0]).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null) {
                Project.getFileToEdit().getVertexManager().condlineToLine();
            }
            regainFocus();
        });

        widgetUtil(btnLineToCondlinePtr[0]).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null) {
                Project.getFileToEdit().getVertexManager().lineToCondline();
            }
            regainFocus();
        });

        widgetUtil(btnMoveOnLinePtr[0]).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null && !Project.getFileToEdit().isReadOnly()) {
                Project.getFileToEdit().getVertexManager().addSnapshot();
                Set<Vertex> verts = Project.getFileToEdit().getVertexManager().getSelectedVertices();
                CoordinatesDialog.setStart(null);
                CoordinatesDialog.setEnd(null);
                if (verts.size() == 2) {
                    Iterator<Vertex> it = verts.iterator();
                    CoordinatesDialog.setStart(new Vector3d(it.next()));
                    CoordinatesDialog.setEnd(new Vector3d(it.next()));
                }
            }
            regainFocus();
        });

        widgetUtil(btnCompileSubfilePtr[0]).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null) {
                Project.getFileToEdit().getVertexManager().addSnapshot();
                SubfileCompiler.compile(Project.getFileToEdit(), false, false);
            }
            regainFocus();
        });

        widgetUtil(btnShowSelectionInTextEditorPtr[0]).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null) {
                Composite3D.showSelectionInTextEditor(Project.getFileToEdit(), true);
            }
            regainFocus();
        });

        widgetUtil(btnBFCswapPtr[0]).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null) {
                Project.getFileToEdit().getVertexManager().addSnapshot();
                Project.getFileToEdit().getVertexManager().backupHideShowState();
                Project.getFileToEdit().getVertexManager().windingChangeSelection(true);
            }
            regainFocus();
        });

        widgetUtil(btnRoundSelectionPtr[0]).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null) {
                if (Cocoa.checkCtrlOrCmdPressed(e.stateMask) && new RoundDialog(Editor3DWindow.getWindow().getShell()).open() == IDialogConstants.CANCEL_ID) {
                    return;
                }
                Project.getFileToEdit().getVertexManager().addSnapshot();
                Project.getFileToEdit().getVertexManager().backupHideShowState();
                UserSettingState userSettings = WorkbenchManager.getUserSettingState();
                Project.getFileToEdit().getVertexManager()
                .roundSelection(userSettings.getCoordsPrecision(), userSettings.getTransMatrixPrecision(), MiscToggleToolItem.isMovingAdjacentData(), true, userSettings.isRoundX(), userSettings.isRoundY(), userSettings.isRoundZ());
            }
            regainFocus();
        });

        widgetUtil(mntmEdger2Ptr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    if (new EdgerDialog(Editor3DWindow.getWindow().getShell(), es).open() == IDialogConstants.OK_ID) {
                        final boolean verbose = WorkbenchManager.getUserSettingState().isVerboseEdger2();
                        final Set<GData2> oldLines = new HashSet<>();
                        final int oldCondlineCount = verbose ? vm.getCondlines().size() : 0;
                        final int oldLineCount = verbose ? vm.getLines().size() : 0;
                        if (verbose) {
                            oldLines.addAll(vm.getLines().keySet());
                        }

                        vm.addSnapshot();
                        vm.skipSyncTimer();
                        vm.addEdges(es);

                        if (verbose) {
                            int unmatchedLines = 0;
                            for (GData2 g2 : vm.getLines().keySet()) {
                                if (!oldLines.contains(g2) && g2.colourNumber == 4) {
                                    unmatchedLines += 1;
                                }
                            }

                            MessageBox messageBox = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_INFORMATION | SWT.OK);
                            messageBox.setText(I18n.DIALOG_INFO);

                            Object[] messageArguments = {
                                    vm.getCondlines().size() - oldCondlineCount,
                                    vm.getLines().size() - oldLineCount - unmatchedLines,
                                    unmatchedLines};
                            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                            formatter.setLocale(MyLanguage.getLocale());
                            formatter.applyPattern(I18n.EDGER_VERBOSE_MSG);

                            messageBox.setMessage(formatter.format(messageArguments));
                            messageBox.open();
                        }
                    }
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });

        widgetUtil(mntmPrimGen2Ptr[0]).addSelectionListener(e -> {
            new PrimGen2Dialog(Editor3DWindow.getWindow().getShell()).open();
            regainFocus();
        });

        widgetUtil(mntmRectifierPtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    if (new RectifierDialog(Editor3DWindow.getWindow().getShell(), rs).open() == IDialogConstants.OK_ID) {
                        vm.addSnapshot();
                        vm.skipSyncTimer();

                        final int[] result = vm.rectify(rs, true, true);

                        if (WorkbenchManager.getUserSettingState().isVerboseRectifier()) {
                            MessageBox messageBox = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_INFORMATION | SWT.OK);
                            messageBox.setText(I18n.DIALOG_INFO);

                            Object[] messageArguments = {
                                    result[0],
                                    result[1],
                                    result[2],
                                    result[3]
                            };
                            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                            formatter.setLocale(MyLanguage.getLocale());
                            formatter.applyPattern(I18n.RECTIFIER_VERBOSE_MSG);

                            messageBox.setMessage(formatter.format(messageArguments));
                            messageBox.open();
                        }
                    }
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });

        widgetUtil(mntmIsecalcPtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    if (new IsecalcDialog(Editor3DWindow.getWindow().getShell(), is).open() == IDialogConstants.OK_ID) {
                        vm.addSnapshot();
                        vm.skipSyncTimer();
                        final int createdLineCount = vm.isecalc(is);
                        if (WorkbenchManager.getUserSettingState().isVerboseIsecalc()) {
                            Object[] messageArguments = { createdLineCount };
                            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                            formatter.setLocale(MyLanguage.getLocale());
                            formatter.applyPattern(I18n.ISECALC_VERBOSE_MSG);
                            MessageBox messageBox = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_INFORMATION | SWT.OK);
                            messageBox.setText(I18n.DIALOG_INFO);
                            messageBox.setMessage(formatter.format(messageArguments));
                            messageBox.open();
                        }
                    }
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });

        widgetUtil(mntmSlicerProPtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    if (new SlicerProDialog(Editor3DWindow.getWindow().getShell()).open() == IDialogConstants.OK_ID) {
                        vm.addSnapshot();
                        vm.skipSyncTimer();
                        final int[] result = vm.slicerpro();
                        if (WorkbenchManager.getUserSettingState().isVerboseSlicerPro()) {
                            final int createdTriangleCount = result[0];
                            final int createdQuadCount = result[1];
                            final int deletedTriangleCount = result[2];
                            final int deletedQuadCount = result[3];
                            Object[] messageArguments = { createdTriangleCount, createdQuadCount, deletedTriangleCount, deletedQuadCount };
                            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                            formatter.setLocale(MyLanguage.getLocale());
                            formatter.applyPattern(I18n.SLICERPRO_VERBOSE_MSG);
                            MessageBox messageBox = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_INFORMATION | SWT.OK);
                            messageBox.setText(I18n.DIALOG_INFO);
                            messageBox.setMessage(formatter.format(messageArguments));
                            messageBox.open();
                        }
                    }
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });

        widgetUtil(mntmIntersectorPtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    if (new IntersectorDialog(Editor3DWindow.getWindow().getShell(), ins).open() == IDialogConstants.OK_ID) {
                        vm.addSnapshot();
                        vm.skipSyncTimer();
                        final int[] result = vm.intersector(ins, true);
                        if (WorkbenchManager.getUserSettingState().isVerboseIntersector()) {
                            final int createdLinesCount = result[0];
                            final int createdTriangleCount = result[1];
                            final int createdCondlineCount = result[2];
                            final int deletedLineCount = result[3];
                            final int deletedTriangleCount = result[4];
                            final int deletedQuadCount = result[5];
                            final int deletedCondlineCount = result[6];
                            Object[] messageArguments = { createdLinesCount, createdTriangleCount, createdCondlineCount, 
                                    deletedLineCount, deletedTriangleCount, deletedQuadCount, deletedCondlineCount };
                            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                            formatter.setLocale(MyLanguage.getLocale());
                            formatter.applyPattern(I18n.INTERSECTOR_VERBOSE_MSG);
                            MessageBox messageBox = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_INFORMATION | SWT.OK);
                            messageBox.setText(I18n.DIALOG_INFO);
                            messageBox.setMessage(formatter.format(messageArguments));
                            messageBox.open();
                        }
                    }
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });

        widgetUtil(mntmSlantingMatrixProjectorPtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    if (new SlantingMatrixProjectorDialog(Editor3DWindow.getWindow().getShell(), vm, mps).open() == IDialogConstants.OK_ID) {
                        vm.addSnapshot();
                        vm.skipSyncTimer();
                        final int[] result = vm.projectWithSlantingMatrix(mps);
                        if (WorkbenchManager.getUserSettingState().isVerboseSlantingMatrixProjector()) {
                            if (result.length == 0) {
                                MessageBox messageBox = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_INFORMATION | SWT.OK);
                                messageBox.setText(I18n.DIALOG_INFO);
                                messageBox.setMessage(I18n.SLANT_NO_SELECTION_RESULT);
                                messageBox.open();
                            } else {
                                final StringBuilder sbSelection = new StringBuilder();
                                final String matrix = vm.getSlantingMatrix(mps.isMovingOriginToAxisCenter()).toLDrawString();
                                GuiStatusManager.updateSelection(sbSelection, vm);
                                String selectionString = sbSelection.toString().trim();
                                if (selectionString.isEmpty()) {
                                    selectionString = I18n.SLANT_NO_SELECTION_NOTHING;
                                } else {
                                    selectionString += c3d.getLockableDatFileReference().getShortName();
                                }
                                
                                Object[] messageArguments = { matrix, selectionString, mps.isResettingSubfileTransformation(), mps.isMovingOriginToAxisCenter() };
                                MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                                formatter.setLocale(MyLanguage.getLocale());
                                formatter.applyPattern(I18n.SLANT_VERBOSE_MSG);
                                MessageBox messageBox = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_INFORMATION | SWT.OK);
                                messageBox.setText(I18n.DIALOG_INFO);
                                messageBox.setMessage(formatter.format(messageArguments));
                                messageBox.open();
                            }
                        }
                    }
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });

        widgetUtil(mntmLines2PatternPtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    if (new Lines2PatternDialog(Editor3DWindow.getWindow().getShell()).open() == IDialogConstants.OK_ID) {
                        vm.addSnapshot();
                        vm.skipSyncTimer();
                        final int oldTriangleCount = vm.getTriangles().size();
                        vm.lines2pattern();
                        if (WorkbenchManager.getUserSettingState().isVerboseLines2Pattern()) {
                            Object[] messageArguments = { vm.getTriangles().size() - oldTriangleCount };
                            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                            formatter.setLocale(MyLanguage.getLocale());
                            formatter.applyPattern(I18n.LINES_VERBOSE_MSG);
                            MessageBox messageBox = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_INFORMATION | SWT.OK);
                            messageBox.setText(I18n.DIALOG_INFO);
                            messageBox.setMessage(formatter.format(messageArguments));
                            messageBox.open();
                        }
                    }
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });

        widgetUtil(mntmPathTruderPtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    if (new PathTruderDialog(Editor3DWindow.getWindow().getShell(), ps).open() == IDialogConstants.OK_ID) {
                        vm.addSnapshot();
                        vm.skipSyncTimer();
                        final int oldLineCount = vm.getLines().size();
                        final int oldTriangleCount = vm.getTriangles().size();
                        final int oldQuadCount = vm.getQuads().size();
                        vm.pathTruder(ps, true, null);
                        if (WorkbenchManager.getUserSettingState().isVerbosePathTruder()) {
                            Object[] messageArguments = { vm.getLines().size() - oldLineCount, vm.getTriangles().size() - oldTriangleCount, vm.getQuads().size() - oldQuadCount };
                            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                            formatter.setLocale(MyLanguage.getLocale());
                            formatter.applyPattern(I18n.PATHTRUDER_VERBOSE_MSG);
                            MessageBox messageBox = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_INFORMATION | SWT.OK);
                            messageBox.setText(I18n.DIALOG_INFO);
                            messageBox.setMessage(formatter.format(messageArguments));
                            messageBox.open();
                        }
                    }
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });

        widgetUtil(mntmYTruderPtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    if (new YTruderDialog(Editor3DWindow.getWindow().getShell(), ys).open() == IDialogConstants.OK_ID) {
                        vm.addSnapshot();
                        vm.skipSyncTimer();
                        final int oldLineCount = vm.getLines().size();
                        final int oldTriangleCount = vm.getTriangles().size();
                        final int oldQuadCount = vm.getQuads().size();
                        vm.yTruder(ys);
                        if (WorkbenchManager.getUserSettingState().isVerboseYTruder()) {
                            Object[] messageArguments = { vm.getLines().size() - oldLineCount, vm.getTriangles().size() - oldTriangleCount, vm.getQuads().size() - oldQuadCount };
                            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                            formatter.setLocale(MyLanguage.getLocale());
                            formatter.applyPattern(I18n.YTRUDER_VERBOSE_MSG);
                            MessageBox messageBox = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_INFORMATION | SWT.OK);
                            messageBox.setText(I18n.DIALOG_INFO);
                            messageBox.setMessage(formatter.format(messageArguments));
                            messageBox.open();
                        }
                    }
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });

        widgetUtil(mntmSymSplitterPtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    if (new SymSplitterDialog(Editor3DWindow.getWindow().getShell(), sims).open() == IDialogConstants.OK_ID) {
                        vm.addSnapshot();
                        vm.skipSyncTimer();
                        final int oldVertexCount = vm.getVertices().size();
                        final int oldLineCount = vm.getLines().size();
                        final int oldTriangleCount = vm.getTriangles().size();
                        final int oldQuadCount = vm.getQuads().size();
                        final int oldCondlineCount = vm.getCondlines().size();
                        final int[] result = vm.symSplitter(sims);
                        if (WorkbenchManager.getUserSettingState().isVerboseSymSplitter()) {
                            Object[] messageArguments = { result[0], result[1], result[2], 
                                    Math.max(0, oldVertexCount - vm.getVertices().size()), 
                                    Math.max(0, oldLineCount - vm.getLines().size()), 
                                    Math.max(0, oldTriangleCount - vm.getTriangles().size()), 
                                    Math.max(0, oldQuadCount - vm.getQuads().size()), 
                                    Math.max(0, oldCondlineCount - vm.getCondlines().size()),
                                    Math.max(0, vm.getLines().size() - oldLineCount), 
                                    Math.max(0, vm.getTriangles().size() - oldTriangleCount), 
                                    Math.max(0, vm.getQuads().size() - oldQuadCount), 
                                    Math.max(0, vm.getCondlines().size() - oldCondlineCount) };
                            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                            formatter.setLocale(MyLanguage.getLocale());
                            formatter.applyPattern(I18n.SYMSPLITTER_VERBOSE_MSG);
                            MessageBox messageBox = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_INFORMATION | SWT.OK);
                            messageBox.setText(I18n.DIALOG_INFO);
                            messageBox.setMessage(formatter.format(messageArguments));
                            messageBox.open();
                        }
                    }
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });

        widgetUtil(mntmUnificatorPtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    if (new UnificatorDialog(Editor3DWindow.getWindow().getShell(), us).open() == IDialogConstants.OK_ID) {
                        vm.addSnapshot();
                        vm.skipSyncTimer();
                        final int oldVertexCount = vm.getVertices().size();
                        final int oldLineCount = vm.getLines().size();
                        final int oldTriangleCount = vm.getTriangles().size();
                        final int oldQuadCount = vm.getQuads().size();
                        final int oldCondlineCount = vm.getCondlines().size();
                        vm.unificator(us);
                        if (WorkbenchManager.getUserSettingState().isVerboseUnificator()) {
                            Object[] messageArguments = { oldVertexCount - vm.getVertices().size(), oldLineCount - vm.getLines().size(), oldTriangleCount - vm.getTriangles().size(), oldQuadCount - vm.getQuads().size(), oldCondlineCount - vm.getCondlines().size() };
                            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                            formatter.setLocale(MyLanguage.getLocale());
                            formatter.applyPattern(I18n.UNIFICATOR_VERBOSE_MSG);
                            MessageBox messageBox = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_INFORMATION | SWT.OK);
                            messageBox.setText(I18n.DIALOG_INFO);
                            messageBox.setMessage(formatter.format(messageArguments));
                            messageBox.open();
                        }
                    }
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });

        widgetUtil(mntmRingsAndConesPtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                    if (new RingsAndConesDialog(Editor3DWindow.getWindow().getShell(), ris).open() == IDialogConstants.OK_ID) {
                        c3d.getLockableDatFileReference().getVertexManager().addSnapshot();
                        c3d.getLockableDatFileReference().getVertexManager().skipSyncTimer();
                        RingsAndCones.solve(Editor3DWindow.getWindow().getShell(), c3d.getLockableDatFileReference(), Editor3DWindow.getWindow().getCompositePrimitive().getPrimitives(), ris, true);
                    }
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });

        widgetUtil(mntmTJunctionFinderPtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                DatFile df = c3d.getLockableDatFileReference();
                if (df.equals(Project.getFileToEdit()) && !df.isReadOnly()) {
                    if (new TJunctionDialog(Editor3DWindow.getWindow().getShell(), tjs).open() == IDialogConstants.OK_ID) {
                        VertexManager vm = df.getVertexManager();
                        vm.addSnapshot();
                        vm.skipSyncTimer();
                        vm.fixTjunctions(tjs);
                    }
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });

        widgetUtil(mntmMeshReducerPtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                DatFile df = c3d.getLockableDatFileReference();
                if (df.equals(Project.getFileToEdit()) && !df.isReadOnly()) {
                    if (new MeshReducerDialog(Editor3DWindow.getWindow().getShell(), ms).open() == IDialogConstants.OK_ID) {
                        VertexManager vm = df.getVertexManager();
                        vm.addSnapshot();
                        vm.skipSyncTimer();
                        vm.meshReduce(0, ms);
                    }
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });

        widgetUtil(mntmTxt2DatPtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                    DatFile df = c3d.getLockableDatFileReference();
                    if (df.isReadOnly()) {
                        regainFocus();
                        return;
                    }
                    VertexManager vm = df.getVertexManager();
                    vm.addSnapshot();
                    
                    
                    int dialogResult;
                    do {
                        ts.setReload(false);
                        dialogResult = new Txt2DatDialog(Editor3DWindow.getWindow().getShell(), ts).open();
                    } while (ts.isReload());
                    
                    if (dialogResult == IDialogConstants.OK_ID && !ts.getText().trim().isEmpty()) {
                        vm.skipSyncTimer();
                        java.awt.Font myFont;
                        float r = 0.1f;
                        float g = 0.1f;
                        float b = 0.1f;

                        if (ts.getFontData() == null) {
                            myFont = new java.awt.Font(org.nschmidt.ldparteditor.enumtype.Font.MONOSPACE.getFontData()[0].getName(), java.awt.Font.PLAIN, 32);
                        } else {
                            FontData fd = ts.getFontData();
                            RGB fontColour = ts.getRGB();
                            if (fontColour != null) {
                                r = fontColour.red / 255f;
                                g = fontColour.green / 255f;
                                b = fontColour.blue / 255f;
                            }

                            int style = 0;
                            final int c2 = SWT.BOLD | SWT.ITALIC;
                            switch (fd.getStyle()) {
                            case c2:
                                style = java.awt.Font.BOLD | java.awt.Font.ITALIC;
                                break;
                            case SWT.BOLD:
                                style = java.awt.Font.BOLD;
                                break;
                            case SWT.ITALIC:
                                style = java.awt.Font.ITALIC;
                                break;
                            case SWT.NORMAL:
                                style = java.awt.Font.PLAIN;
                                break;
                            default:
                                break;
                            }
                            myFont = new java.awt.Font(fd.getName(), style, fd.getHeight());
                        }
                        GData anchorData = df.getDrawChainTail();
                        int lineNumber = df.getDrawPerLineNoClone().getKey(anchorData);
                        Set<GData> triangleSet = TextTriangulator.triangulateText(myFont, r, g, b, ts.getText().trim(), ts.getFlatness().doubleValue(), ts.getMarginPercentage().doubleValue(), View.DUMMY_REFERENCE, df, ts.getFontHeight().intValue(), ts.getMode());
                        if (WorkbenchManager.getUserSettingState().isVerboseTxt2Dat()) {
                            Object[] messageArguments = { ts.getText().trim().length(), triangleSet.size()};
                            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                            formatter.setLocale(MyLanguage.getLocale());
                            formatter.applyPattern(I18n.TXT2DAT_VERBOSE_MSG);
                            MessageBox messageBox = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_INFORMATION | SWT.OK);
                            messageBox.setText(I18n.DIALOG_INFO);
                            messageBox.setMessage(formatter.format(messageArguments));
                            messageBox.open();
                        }
                        
                        for (GData gda3 : triangleSet) {
                            lineNumber++;
                            df.getDrawPerLineNoClone().put(lineNumber, gda3);
                            GData gdata = gda3;
                            anchorData.setNext(gda3);
                            anchorData = gdata;
                        }
                        anchorData.setNext(null);
                        df.setDrawChainTail(anchorData);
                        vm.setModified(true, true);
                    }
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });

        // MARK Options

        widgetUtil(mntmUserConfigSavePtr[0]).addSelectionListener(e -> {
            FileDialog fd = new FileDialog(Editor3DWindow.getWindow().getShell(), SWT.SAVE);
            fd.setText(I18n.E3D_USER_CONFIG_SELECT_SAVE);
            fd.setOverwrite(true);
            String[] filterExt = { "*.gz", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
            fd.setFilterExtensions(filterExt);
            String[] filterNames = {I18n.E3D_USER_CONFIG_FILE, I18n.E3D_ALL_FILES};
            fd.setFilterNames(filterNames);

            String selected = fd.open();
            if (selected != null) {
                final Editor3DWindowState winState = WorkbenchManager.getEditor3DWindowState();
                if (!Editor3DWindow.getWindow().isReviewingAPart()) {
                    winState.setThreeDwindowConfig(Editor3DWindow.getWindow().getC3DStates());
                }

                if (Editor3DWindow.getWindow().getEditorSashForm() != null) {
                    winState.setEditorSashWeights(Editor3DWindow.getWindow().getEditorSashForm().getWeights());
                }
                Editor3DWindow.getWindow();
                winState.setLeftSashWeights(((SashForm) Editor3DWindow.getSashForm().getChildren()[0]).getWeights());
                Editor3DWindow.getWindow();
                winState.setLeftSashWidth(Editor3DWindow.getSashForm().getWeights());
                winState.setPrimitiveZoom(Editor3DWindow.getWindow().getCompositePrimitive().getZoom());
                winState.setPrimitiveZoomExponent(Editor3DWindow.getWindow().getCompositePrimitive().getZoomExponent());
                winState.setPrimitiveViewport(Editor3DWindow.getWindow().getCompositePrimitive().getViewport2());

                WorkbenchManager.getPrimitiveCache().setPrimitiveCache(CompositePrimitive.getCache());
                WorkbenchManager.getPrimitiveCache().setPrimitiveFileCache(CompositePrimitive.getFileCache());

                WorkbenchManager.getUserSettingState().setRecentItems(NewOpenSaveProjectToolItem.getRecentItems());

                if (!WorkbenchManager.saveWorkbench(selected)) {
                    MessageBox messageBox = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_ERROR | SWT.OK);
                    messageBox.setText(I18n.DIALOG_ERROR);
                    messageBox.setMessage(I18n.E3D_USER_CONFIG_FAIL_SAVE);
                    messageBox.open();
                }
            }
        });

        widgetUtil(mntmUserConfigLoadPtr[0]).addSelectionListener(e -> {

            FileDialog fd = new FileDialog(Editor3DWindow.getWindow().getShell(), SWT.OPEN);
            fd.setText(I18n.E3D_USER_CONFIG_SELECT_LOAD);
            String[] filterExt = { "*.gz", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
            fd.setFilterExtensions(filterExt);
            String[] filterNames = {I18n.E3D_USER_CONFIG_FILE, I18n.E3D_ALL_FILES};
            fd.setFilterNames(filterNames);

            String selected = fd.open();
            if (selected != null) {
                boolean issueDetected = false;
                try {
                    Files.delete(Paths.get(WorkbenchManager.SETTINGS_GZ));
                    Files.copy(Paths.get(selected), Paths.get(WorkbenchManager.SETTINGS_GZ));
                    issueDetected = !WorkbenchManager.loadWorkbench(WorkbenchManager.SETTINGS_GZ);
                } catch (IOException ioe) {
                    NLogger.error(MiscToolItem.class, ioe);
                    issueDetected = true;
                }
                if (issueDetected) {
                    MessageBox messageBox = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_ERROR | SWT.OK);
                    messageBox.setText(I18n.DIALOG_ERROR);
                    messageBox.setMessage(I18n.E3D_USER_CONFIG_FAIL_LOAD);
                    messageBox.open();
                    return;
                }
                WorkbenchManager.getUserSettingState().loadColours();
                // Override colour 16
                LDConfig.overrideColour16();
                // Recompile
                Editor3DWindow.getWindow().compileAll(true);
                // Restore the viewport
                Editor3DWindow.getWindow().closeAllComposite3D();
                Editor3DWindow.getWindow().reloadC3DStates(WorkbenchManager.getEditor3DWindowState().getThreeDwindowConfig());
                // Link renderes with dummy file
                Project.setFileToEdit(View.DUMMY_DATFILE);
                for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                    renderer.getC3D().setLockableDatFileReference(View.DUMMY_DATFILE);
                }
                // Re-initialise the renderer
                Editor3DWindow.getWindow().initAllRenderers();
                // Update colours on text editor
                for (EditorTextWindow w : Project.getOpenTextWindows()) {
                    for (CTabItem t : w.getTabFolder().getItems()) {
                        ((CompositeTab) t).updateColours();
                    }
                }
            }
        });

        widgetUtil(mntmResetSettingsOnRestartPtr[0]).addSelectionListener(e -> {
            MessageBox messageBox = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
            messageBox.setText(I18n.DIALOG_WARNING);
            messageBox.setMessage(I18n.E3D_DELETE_CONFIG);
            int result = messageBox.open();
            if (result == SWT.CANCEL) {
                regainFocus();
                return;
            }
            WorkbenchManager.getUserSettingState().setResetOnStart(true);
            regainFocus();
        });

        widgetUtil(mntmOptionsPtr[0]).addSelectionListener(e -> {
            OptionsDialog dialog = new OptionsDialog(Editor3DWindow.getWindow().getShell());
            dialog.run();
            regainFocus();
        });

        widgetUtil(mntmSelectAnotherLDConfigPtr[0]).addSelectionListener(e -> {
            FileDialog fd = new FileDialog(Editor3DWindow.getWindow().getShell(), SWT.OPEN);
            fd.setText(I18n.E3D_OPEN_LDCONFIG);
            fd.setFilterPath(WorkbenchManager.getUserSettingState().getLdrawFolderPath());

            String[] filterExt = { "*.ldr", "ldconfig.ldr", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            fd.setFilterExtensions(filterExt);
            String[] filterNames = { I18n.E3D_LDRAW_CONFIGURATION_FILE_1, I18n.E3D_LDRAW_CONFIGURATION_FILE_2, I18n.E3D_ALL_FILES };
            fd.setFilterNames(filterNames);

            String selected = fd.open();
            LDConfigUtils.reloadLDConfig(selected);
            regainFocus();
        });

        widgetUtil(mntmDownloadLDConfigPtr[0]).addSelectionListener(e -> {
            LDConfigUtils.downloadLDConfig();
            regainFocus();
        });

        widgetUtil(mntmDownloadCategoriesPtr[0]).addSelectionListener(e -> {
            CategoriesUtils.downloadCategories();
            regainFocus();
        });

        widgetUtil(mntmSavePalettePtr[0]).addSelectionListener(e -> {

            FileDialog dlg = new FileDialog(Editor3DWindow.getWindow().getShell(), SWT.SAVE);

            dlg.setFilterPath(Project.getLastVisitedPath());

            dlg.setFilterExtensions(new String[]{"*_pal.dat"}); //$NON-NLS-1$
            dlg.setOverwrite(true);

            // Change the title bar text
            dlg.setText(I18n.E3D_PALETTE_SAVE);

            // Calling open() will open and run the dialog.
            // It will return the selected file, or
            // null if user cancels
            String newPath = dlg.open();
            if (newPath != null) {
                final File f = new File(newPath);
                if (f.getParentFile() != null) {
                    Project.setLastVisitedPath(f.getParentFile().getAbsolutePath());
                }

                try (UTF8PrintWriter r = new UTF8PrintWriter(newPath)) {
                    int x = 0;
                    for (GColour col : WorkbenchManager.getUserSettingState().getUserPalette()) {
                        r.println("1 " + col + " " + x + " 0 0 1 0 0 0 1 0 0 0 1 rect.dat"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        x += 2;
                    }
                    r.flush();
                } catch (Exception ex) {
                    MessageBox messageBoxError = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_ERROR | SWT.OK);
                    messageBoxError.setText(I18n.DIALOG_ERROR);
                    messageBoxError.setMessage(I18n.DIALOG_CANT_SAVE_FILE);
                    messageBoxError.open();
                }
            }

            regainFocus();
        });

        widgetUtil(mntmLoadPalettePtr[0]).addSelectionListener(e -> {

            FileDialog dlg = new FileDialog(Editor3DWindow.getWindow().getShell(), SWT.OPEN);

            dlg.setFilterPath(Project.getLastVisitedPath());

            dlg.setFilterExtensions(new String[]{"*_pal.dat"}); //$NON-NLS-1$
            dlg.setOverwrite(true);

            // Change the title bar text
            dlg.setText(I18n.E3D_PALETTE_LOAD);

            // Calling open() will open and run the dialog.
            // It will return the selected file, or
            // null if user cancels
            String newPath = dlg.open();
            if (newPath != null) {
                final File f = new File(newPath);
                if (f.getParentFile() != null) {
                    Project.setLastVisitedPath(f.getParentFile().getAbsolutePath());
                }

                final Pattern whitespace = Pattern.compile("\\s+"); //$NON-NLS-1$
                List<GColour> pal = WorkbenchManager.getUserSettingState().getUserPalette();
                List<GColour> newPal = new ArrayList<>();


                try (UTF8BufferedReader reader = new UTF8BufferedReader(newPath)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        final String[] dataSegments = whitespace.split(line.trim());
                        if (dataSegments.length > 1) {
                            GColour c = DatParser.validateColour(dataSegments[1], 0f, 0f, 0f, 1f);
                            if (c != null) {
                                newPal.add(c.createClone());
                            }
                        }
                    }
                    pal.clear();
                    pal.addAll(newPal);
                } catch (LDParsingException | FileNotFoundException ex) {
                    NLogger.error(Editor3DWindow.class, ex);
                }

                ColourToolItem.reloadAllColours();

            }

            regainFocus();
        });

        widgetUtil(mntmSetPaletteSizePtr[0]).addSelectionListener(e -> {

            final List<GColour> colours = WorkbenchManager.getUserSettingState().getUserPalette();

            final int[] frac = new int[]{2};
            if (new ValueDialogInt(Editor3DWindow.getWindow().getShell(), I18n.E3D_PALETTE_SET_SIZE, "") { //$NON-NLS-1$

                @Override
                public void initializeSpinner() {
                    this.spnValuePtr[0].setMinimum(1);
                    this.spnValuePtr[0].setMaximum(100);
                    this.spnValuePtr[0].setValue(colours.size());
                }

                @Override
                public void applyValue() {
                    frac[0] = this.spnValuePtr[0].getValue();
                }
            }.open() == Window.OK) {

                final boolean reBuild = frac[0] != colours.size();

                if (frac[0] > colours.size()) {
                    while (frac[0] > colours.size()) {
                        if (colours.size() < 17) {
                            if (colours.size() == 8) {
                                colours.add(LDConfig.getColour(72));
                            } else {
                                colours.add(LDConfig.getColour(colours.size()));
                            }
                        } else {
                            colours.add(LDConfig.getColour16());
                        }
                    }
                } else {
                    while (frac[0] < colours.size()) {
                        colours.remove(colours.size() - 1);
                    }
                }

                if (reBuild) {
                    ColourToolItem.reloadAllColours();
                }
            }

            regainFocus();
        });

        widgetUtil(mntmResetPalettePtr[0]).addSelectionListener(e -> {

            final List<GColour> colours = WorkbenchManager.getUserSettingState().getUserPalette();
            colours.clear();

            while (colours.size() < 17) {
                if (colours.size() == 8) {
                    colours.add(LDConfig.getColour(72));
                } else {
                    colours.add(LDConfig.getColour(colours.size()));
                }
            }

            ColourToolItem.reloadAllColours();

            regainFocus();
        });

        widgetUtil(mntmShowLogsPtr[0]).addSelectionListener(e -> {

            String source = ""; //$NON-NLS-1$

            StringBuilder code1 = new StringBuilder();

            File l11 = new File(NLogger.ERROR_LOG);
            File l21 = new File(NLogger.ERROR_LOG2);

            if (l11.exists() || l21.exists()) {
                try {
                    if (l11.exists()) {
                        try (UTF8BufferedReader b11 = new UTF8BufferedReader(NLogger.ERROR_LOG)) {
                            String line1;
                            while ((line1 = b11.readLine()) != null) {
                                code1.append(line1);
                                code1.append(StringHelper.getLineDelimiter());
                            }
                        }
                    }

                    if (l21.exists()) {
                        try (UTF8BufferedReader b21 = new UTF8BufferedReader(NLogger.ERROR_LOG2)) {
                            String line2;
                            while ((line2 = b21.readLine()) != null) {
                                code1.append(line2);
                                code1.append(StringHelper.getLineDelimiter());
                            }
                        }
                    }

                    source = code1.toString();
                } catch (Exception ex) {
                    NLogger.error(Editor3DWindow.class, ex);
                    MessageBox messageBox1 = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_ERROR | SWT.OK);
                    messageBox1.setText(I18n.DIALOG_ERROR);
                    messageBox1.setMessage(I18n.E3D_LOG_SHOW_UNEXPECTED_EXCEPTION);
                    messageBox1.open();
                    regainFocus();
                    return;
                }
            } else {
                MessageBox messageBox2 = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_INFORMATION | SWT.OK);
                messageBox2.setText(I18n.DIALOG_INFO);
                messageBox2.setMessage(I18n.E3D_LOG_SHOW_NO_LOG_FILES);
                messageBox2.open();
                regainFocus();
                return;
            }

            LogDisplayDialog dialog = new LogDisplayDialog(Editor3DWindow.getWindow().getShell(), source);
            dialog.open();
            regainFocus();
        });

        widgetUtil(mntmAntiAliasingPtr[0]).addSelectionListener(e -> {
            WorkbenchManager.getUserSettingState().setAntiAliasing(mntmAntiAliasingPtr[0].getSelection());
            regainFocus();
        });

        widgetUtil(mntmOpenGL33EnginePtr[0]).addSelectionListener(e -> {
            WorkbenchManager.getUserSettingState().setOpenGL33Engine(mntmOpenGL33EnginePtr[0].getSelection());
            WorkbenchManager.getUserSettingState().setVulkanEngine(false);
            if (NLogger.debugging) {
                mntmVulkanEnginePtr[0].setSelection(false);
            }
            regainFocus();
        });

        if (NLogger.debugging) widgetUtil(mntmVulkanEnginePtr[0]).addSelectionListener(e -> {
            WorkbenchManager.getUserSettingState().setVulkanEngine(mntmVulkanEnginePtr[0].getSelection());
            WorkbenchManager.getUserSettingState().setOpenGL33Engine(false);
            mntmOpenGL33EnginePtr[0].setSelection(false);
            regainFocus();
        });

        widgetUtil(mntmSyncLpeInlinePtr[0]).addSelectionListener(e -> {
            WorkbenchManager.getUserSettingState().getSyncWithLpeInline().set(mntmSyncLpeInlinePtr[0].getSelection());
            regainFocus();
        });

        // MARK Merge, split...

        widgetUtil(mntmFlipPtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    vm.addSnapshot();
                    vm.flipSelection();
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });

        widgetUtil(mntmSubdivideCatmullClarkPtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    vm.addSnapshot();
                    vm.subdivideCatmullClark(true);
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });

        widgetUtil(mntmSubdivideLoopPtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    vm.addSnapshot();
                    vm.subdivideLoop(true);
                    regainFocus();
                    return;
                }
            }
            regainFocus();
        });

        widgetUtil(mntmIconSize1Ptr[0]).addSelectionListener(e -> {
            WorkbenchManager.getUserSettingState().setIconSize(-1);
            regainFocus();
        });
        widgetUtil(mntmIconSize2Ptr[0]).addSelectionListener(e -> {
            WorkbenchManager.getUserSettingState().setIconSize(0);
            regainFocus();
        });
        widgetUtil(mntmIconSize3Ptr[0]).addSelectionListener(e -> {
            WorkbenchManager.getUserSettingState().setIconSize(1);
            regainFocus();
        });
        widgetUtil(mntmIconSize4Ptr[0]).addSelectionListener(e -> {
            WorkbenchManager.getUserSettingState().setIconSize(2);
            regainFocus();
        });
        widgetUtil(mntmIconSize5Ptr[0]).addSelectionListener(e -> {
            WorkbenchManager.getUserSettingState().setIconSize(3);
            regainFocus();
        });
        widgetUtil(mntmIconSize6Ptr[0]).addSelectionListener(e -> {
            WorkbenchManager.getUserSettingState().setIconSize(4);
            regainFocus();
        });

    }

    private static void regainFocus() {
        Editor3DWindow.getWindow().regainFocus();
    }

    public static SelectorSettings loadSelectorSettings()  {
        sels.setColour(mntmWithSameColourPtr[0].getSelection());
        sels.setType(mntmWithSameTypePtr[0].getSelection());
        sels.setEdgeAdjacency(mntmWithAdjacencyPtr[0].getSelection());
        sels.setEdgeStop(mntmStopAtEdgesPtr[0].getSelection());
        sels.setHidden(mntmWithHiddenDataPtr[0].getSelection());
        sels.setNoSubfiles(mntmExceptSubfilesPtr[0].getSelection());
        sels.setOrientation(mntmWithSameOrientationPtr[0].getSelection());
        sels.setDistance(mntmWithAccuracyPtr[0].getSelection());
        sels.setWholeSubfiles(mntmWithWholeSubfilesPtr[0].getSelection());
        sels.setVertices(mntmSVerticesPtr[0].getSelection());
        sels.setLines(mntmSLinesPtr[0].getSelection());
        sels.setTriangles(mntmSTrianglesPtr[0].getSelection());
        sels.setQuads(mntmSQuadsPtr[0].getSelection());
        sels.setCondlines(mntmSCLinesPtr[0].getSelection());
        return sels;
    }

    public static MenuItem getMntmWithSameColour() {
        return mntmWithSameColourPtr[0];
    }

    static void showSelectMenu() {
        Point loc = btnSelect2Ptr[0].getLocation();
        Rectangle rect = btnSelect2Ptr[0].getBounds();
        Point mLoc = new Point(loc.x - 1, loc.y + rect.height);
        mnuSelect.setLocation(Editor3DWindow.getWindow().getShell().getDisplay().map(btnSelect2Ptr[0].getParent(), null, mLoc));
        mnuSelect.setVisible(true);
    }

    public static void activateAllTypes() {
        mntmSVerticesPtr[0].setSelection(true);
        mntmSLinesPtr[0].setSelection(true);
        mntmSTrianglesPtr[0].setSelection(true);
        mntmSQuadsPtr[0].setSelection(true);
        mntmSCLinesPtr[0].setSelection(true);
    }
}
