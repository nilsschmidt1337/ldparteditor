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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
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
import org.nschmidt.ldparteditor.composites.primitive.CompositePrimitive;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.dialogs.colour.ColourDialog;
import org.nschmidt.ldparteditor.enums.Perspective;
import org.nschmidt.ldparteditor.enums.Task;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.ShellHelper;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.resources.ResourceManager;
import org.nschmidt.ldparteditor.state.KeyStateManager;
import org.nschmidt.ldparteditor.text.StringHelper;
import org.nschmidt.ldparteditor.text.UTF8BufferedReader;
import org.nschmidt.ldparteditor.widgets.BigDecimalSpinner;
import org.nschmidt.ldparteditor.widgets.Tree;
import org.nschmidt.ldparteditor.widgets.TreeItem;
import org.nschmidt.ldparteditor.workbench.Composite3DState;
import org.nschmidt.ldparteditor.workbench.Editor3DWindowState;
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

    /** The menu of the manipulator */
    private Menu mnu_Manipulator;
    /** The menu of the tools */
    private Menu mnu_Tools;
    /** The menu of merge, split and other functions */
    private Menu mnu_Merge;
    /** The menu of the select features */
    private Menu mnu_Select;

    final Menu[] mnu_treeMenu = new Menu[1];

    Action menuItem_Open = ShellHelper.DUMMY_ACTION;
    Action menuItem_Exit = ShellHelper.DUMMY_ACTION;
    Action toolItem_Save = ShellHelper.DUMMY_ACTION;
    final MenuManager[] mnu_File = new MenuManager[1];
    private Composite toolBar;
    private static Composite status;
    private static final int iconSize = WorkbenchManager.getUserSettingState().getIconSize();
    final Button[] btn_Sync = new Button[1];
    final Button[] btn_LastOpen = new Button[1];
    final Button[] btn_New = new Button[1];
    final Button[] btn_Open = new Button[1];
    final Button[] btn_Save = new Button[1];
    final Button[] btn_SaveAll = new Button[1];
    final Button[] btn_Select = new Button[1];
    final Button[] btn_Move = new Button[1];
    final Button[] btn_Rotate = new Button[1];
    final Button[] btn_Scale = new Button[1];
    final Button[] btn_Combined = new Button[1];

    final Button[] btn_Local = new Button[1];
    final Button[] btn_Global = new Button[1];

    final Button[] btn_Undo = new Button[1];
    final Button[] btn_AddHistory = new Button[1];
    final Button[] btn_Redo = new Button[1];

    final MenuItem[] btn_Manipulator_0_toOrigin = new MenuItem[1];
    final MenuItem[] btn_Manipulator_X_XReverse = new MenuItem[1];
    final MenuItem[] btn_Manipulator_XI_YReverse = new MenuItem[1];
    final MenuItem[] btn_Manipulator_XII_ZReverse = new MenuItem[1];
    final MenuItem[] btn_Manipulator_XIII_toWorld = new MenuItem[1];
    final MenuItem[] btn_Manipulator_1_cameraToPos = new MenuItem[1];
    final MenuItem[] btn_Manipulator_2_toAverage = new MenuItem[1];
    final MenuItem[] btn_Manipulator_3_toSubfile = new MenuItem[1];
    final MenuItem[] btn_Manipulator_32_subfileTo = new MenuItem[1];
    final MenuItem[] btn_Manipulator_4_toVertex = new MenuItem[1];
    final MenuItem[] btn_Manipulator_5_toEdge = new MenuItem[1];
    final MenuItem[] btn_Manipulator_6_toSurface = new MenuItem[1];
    final MenuItem[] btn_Manipulator_7_toVertexNormal = new MenuItem[1];
    final MenuItem[] btn_Manipulator_8_toEdgeNormal = new MenuItem[1];
    final MenuItem[] btn_Manipulator_9_toSurfaceNormal = new MenuItem[1];
    final MenuItem[] btn_Manipulator_XIV_adjustRotationCenter = new MenuItem[1];
    final MenuItem[] btn_Manipulator_SwitchXY = new MenuItem[1];
    final MenuItem[] btn_Manipulator_SwitchXZ = new MenuItem[1];
    final MenuItem[] btn_Manipulator_SwitchYZ = new MenuItem[1];

    final Button[] btn_LastUsedColour = new Button[1];
    final Button[] btn_Pipette = new Button[1];
    final Button[] btn_Palette = new Button[1];

    final Button[] btn_MoveAdjacentData = new Button[1];
    final Button[] btn_CompileSubfile = new Button[1];
    final Button[] btn_SplitQuad = new Button[1];
    final Button[] btn_RoundSelection = new Button[1];
    final Button[] btn_BFCswap = new Button[1];
    final Button[] btn_Vertices = new Button[1];
    final Button[] btn_TrisNQuads = new Button[1];
    final Button[] btn_Lines = new Button[1];
    final Button[] btn_Subfiles = new Button[1];
    final Button[] btn_AddComment = new Button[1];
    final Button[] btn_AddVertex = new Button[1];
    final Button[] btn_AddPrimitive = new Button[1];
    final Button[] btn_AddLine = new Button[1];
    final Button[] btn_AddTriangle = new Button[1];
    final Button[] btn_AddQuad = new Button[1];
    final Button[] btn_AddCondline = new Button[1];

    final MenuItem[] mntm_OpenIn3DEditor = new MenuItem[1];
    final MenuItem[] mntm_OpenInTextEditor = new MenuItem[1];
    final MenuItem[] mntm_Rename = new MenuItem[1];
    final MenuItem[] mntm_Revert = new MenuItem[1];
    final MenuItem[] mntm_Delete = new MenuItem[1];
    final MenuItem[] mntm_CopyToUnofficial = new MenuItem[1];

    final Button[] btn_Select2 = new Button[1];

    final MenuItem[] mntm_SelectAll = new MenuItem[1];
    final MenuItem[] mntm_SelectAllWithColours = new MenuItem[1];
    final MenuItem[] mntm_SelectAllVisible = new MenuItem[1];
    final MenuItem[] mntm_SelectAllVisibleWithColours = new MenuItem[1];
    final MenuItem[] mntm_SelectNone = new MenuItem[1];
    final MenuItem[] mntm_SelectInverse = new MenuItem[1];
    final MenuItem[] mntm_SelectEverything = new MenuItem[1];
    final MenuItem[] mntm_SelectConnected = new MenuItem[1];
    final MenuItem[] mntm_SelectTouching = new MenuItem[1];
    final MenuItem[] mntm_WithSameOrientation = new MenuItem[1];
    final MenuItem[] mntm_WithAccuracy = new MenuItem[1];
    final MenuItem[] mntm_WithWholeSubfiles = new MenuItem[1];
    final MenuItem[] mntm_WithSameColour = new MenuItem[1];
    final MenuItem[] mntm_WithHiddenData = new MenuItem[1];
    final MenuItem[] mntm_ExceptSubfiles = new MenuItem[1];
    final MenuItem[] mntm_StopAtEdges = new MenuItem[1];
    final MenuItem[] mntm_SelectIsolatedVertices = new MenuItem[1];

    final MenuItem[] mntm_Edger2 = new MenuItem[1];
    final MenuItem[] mntm_Txt2Dat = new MenuItem[1];
    final MenuItem[] mntm_Rectifier = new MenuItem[1];
    final MenuItem[] mntm_Isecalc = new MenuItem[1];
    final MenuItem[] mntm_SlicerPro = new MenuItem[1];
    final MenuItem[] mntm_Intersector = new MenuItem[1];
    final MenuItem[] mntm_Lines2Pattern = new MenuItem[1];
    final MenuItem[] mntm_PathTruder = new MenuItem[1];
    final MenuItem[] mntm_SymSplitter = new MenuItem[1];
    final MenuItem[] mntm_Unificator = new MenuItem[1];
    final MenuItem[] mntm_RingsAndCones = new MenuItem[1];

    final MenuItem[] mntm_ResetSettingsOnRestart = new MenuItem[1];
    final MenuItem[] mntm_SelectAnotherLDConfig = new MenuItem[1];
    final MenuItem[] mntm_UploadLogs = new MenuItem[1];
    final MenuItem[] mntm_SyncWithTextEditor = new MenuItem[1];
    final MenuItem[] mntm_SyncLpeInline = new MenuItem[1];

    final MenuItem[] mntm_Flip = new MenuItem[1];
    final MenuItem[] mntm_SubdivideCatmullClark = new MenuItem[1];
    final MenuItem[] mntm_SubdivideLoop = new MenuItem[1];
    final MenuItem[] mntm_Split = new MenuItem[1];
    final MenuItem[] mntm_SplitNTimes = new MenuItem[1];

    final MenuItem[] mntm_MergeToAverage = new MenuItem[1];
    final MenuItem[] mntm_MergeToLastSelected = new MenuItem[1];
    final MenuItem[] mntm_MergeToNearestVertex = new MenuItem[1];
    final MenuItem[] mntm_MergeToNearestEdge = new MenuItem[1];
    final MenuItem[] mntm_MergeToNearestFace = new MenuItem[1];

    final MenuItem[] mntm_setXYZ = new MenuItem[1];
    final MenuItem[] mntm_Translate = new MenuItem[1];
    final MenuItem[] mntm_Rotate = new MenuItem[1];
    final MenuItem[] mntm_Scale = new MenuItem[1];

    final Text[] txt_Search = new Text[1];
    final Button[] btn_ResetSearch = new Button[1];
    final Composite[] tabFolder_Settings = new Composite[1];

    final Button[] btn_Hide = new Button[1];
    final Button[] btn_ShowAll = new Button[1];
    final Button[] btn_NoTransparentSelection = new Button[1];
    final Button[] btn_BFCToggle = new Button[1];

    final Button[] btn_Delete = new Button[1];
    final Button[] btn_Copy = new Button[1];
    final Button[] btn_Cut = new Button[1];
    final Button[] btn_Paste = new Button[1];

    final Button[] btn_Coarse = new Button[1];
    final Button[] btn_Medium = new Button[1];
    final Button[] btn_Fine = new Button[1];

    final BigDecimalSpinner[] spn_Move = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_Rotate = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_Scale = new BigDecimalSpinner[1];

    final TreeItem[] treeItem_Project = new TreeItem[1];
    final TreeItem[] treeItem_Unsaved = new TreeItem[1];
    final TreeItem[] treeItem_Unofficial = new TreeItem[1];
    final TreeItem[] treeItem_Official = new TreeItem[1];

    final TreeItem[] treeItem_ProjectParts = new TreeItem[1];
    final TreeItem[] treeItem_ProjectSubparts = new TreeItem[1];
    final TreeItem[] treeItem_ProjectPrimitives = new TreeItem[1];
    final TreeItem[] treeItem_ProjectPrimitives8 = new TreeItem[1];
    final TreeItem[] treeItem_ProjectPrimitives48 = new TreeItem[1];
    final TreeItem[] treeItem_UnofficialParts = new TreeItem[1];
    final TreeItem[] treeItem_UnofficialSubparts = new TreeItem[1];
    final TreeItem[] treeItem_UnofficialPrimitives = new TreeItem[1];
    final TreeItem[] treeItem_UnofficialPrimitives8 = new TreeItem[1];
    final TreeItem[] treeItem_UnofficialPrimitives48 = new TreeItem[1];
    final TreeItem[] treeItem_OfficialParts = new TreeItem[1];
    final TreeItem[] treeItem_OfficialSubparts = new TreeItem[1];
    final TreeItem[] treeItem_OfficialPrimitives = new TreeItem[1];
    final TreeItem[] treeItem_OfficialPrimitives8 = new TreeItem[1];
    final TreeItem[] treeItem_OfficialPrimitives48 = new TreeItem[1];
    final Tree[] treeParts = new Tree[1];

    final Button[] btn_PngPrevious = new Button[1];
    final Button[] btn_PngFocus = new Button[1];
    final Button[] btn_PngNext = new Button[1];
    final Button[] btn_PngImage = new Button[1];
    final Text[] txt_PngPath = new Text[1];
    final BigDecimalSpinner[] spn_PngX = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_PngY = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_PngZ = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_PngA1 = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_PngA2 = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_PngA3 = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_PngSX = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_PngSY = new BigDecimalSpinner[1];

    final Button[] btn_lineSize1 = new Button[1];
    final Button[] btn_lineSize2 = new Button[1];
    final Button[] btn_lineSize3 = new Button[1];
    final Button[] btn_lineSize4 = new Button[1];

    final Button[] btn_NewDat = new Button[1];
    final Button[] btn_OpenDat = new Button[1];

    final Button[] btn_PreviousSelection = new Button[1];
    final Button[] btn_NextSelection = new Button[1];
    final Text[] txt_Line = new Text[1];
    final Button[] btn_MoveAdjacentData2 = new Button[1];
    final BigDecimalSpinner[] spn_SelectionX1 = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_SelectionY1 = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_SelectionZ1 = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_SelectionX2 = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_SelectionY2 = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_SelectionZ2 = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_SelectionX3 = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_SelectionY3 = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_SelectionZ3 = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_SelectionX4 = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_SelectionY4 = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_SelectionZ4 = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_SelectionX5 = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_SelectionY5 = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_SelectionZ5 = new BigDecimalSpinner[1];
    final Label[] lbl_SelectionX1 = new Label[1];
    final Label[] lbl_SelectionY1 = new Label[1];
    final Label[] lbl_SelectionZ1 = new Label[1];
    final Label[] lbl_SelectionX2 = new Label[1];
    final Label[] lbl_SelectionY2 = new Label[1];
    final Label[] lbl_SelectionZ2 = new Label[1];
    final Label[] lbl_SelectionX3 = new Label[1];
    final Label[] lbl_SelectionY3 = new Label[1];
    final Label[] lbl_SelectionZ3 = new Label[1];
    final Label[] lbl_SelectionX4 = new Label[1];
    final Label[] lbl_SelectionY4 = new Label[1];
    final Label[] lbl_SelectionZ4 = new Label[1];
    final Label[] lbl_SelectionX5 = new Label[1];
    final Label[] lbl_SelectionY5 = new Label[1];
    final Label[] lbl_SelectionZ5 = new Label[1];
    final Label[] lbl_selectedPrimitiveItem = new Label[1];

    final CompositePrimitive[] cmp_Primitives = new CompositePrimitive[1];
    final Text[] txt_primitiveSearch = new Text[1];
    final Button[] btn_resetPrimitiveSearch = new Button[1];

    final MenuItem[] mntm_STriangles = new MenuItem[1];
    final MenuItem[] mntm_SQuads = new MenuItem[1];
    final MenuItem[] mntm_SCLines = new MenuItem[1];
    final MenuItem[] mntm_SVertices = new MenuItem[1];
    final MenuItem[] mntm_SLines = new MenuItem[1];


    final MenuItem[] mntm_IconSize1 = new MenuItem[1];
    final MenuItem[] mntm_IconSize2 = new MenuItem[1];
    final MenuItem[] mntm_IconSize3 = new MenuItem[1];
    final MenuItem[] mntm_IconSize4 = new MenuItem[1];
    final MenuItem[] mntm_IconSize5 = new MenuItem[1];
    final MenuItem[] mntm_IconSize6 = new MenuItem[1];

    private static SashForm sashForm;

    /**
     * Create the application window.
     */
    public Editor3DDesign() {
        super(null);
        addToolBar(SWT.FLAT | SWT.WRAP);
        addMenuBar();
    }

    /**
     * Create contents of the application window.
     *
     * @param parent
     */
    @Override
    protected Control createContents(Composite parent) {
        final Editor3DWindowState windowState = WorkbenchManager.getEditor3DWindowState();
        setStatus(I18n.EDITOR3D_ReadyStatus);
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new BorderLayout(0, 0));
        toolBar = new Composite(container, SWT.NONE);
        toolBar.setLayoutData(BorderLayout.NORTH);
        RowLayout rl_toolBar = new RowLayout(SWT.HORIZONTAL);
        rl_toolBar.center = true;
        toolBar.setLayout(rl_toolBar);
        {
            ToolItem toolItem_Sync = new ToolItem(toolBar, SWT.NONE);
            {
                Button btn_Sync = new Button(toolItem_Sync, SWT.NONE);
                this.btn_Sync[0] = btn_Sync;
                btn_Sync.setToolTipText("Synchronise Folders / Editor Content"); //$NON-NLS-1$ I18N Needs translation!
                btn_Sync.setImage(ResourceManager.getImage("icon16_sync.png")); //$NON-NLS-1$
            }
            {
                Button btn_Snapshot = new Button(toolItem_Sync, SWT.NONE);
                this.btn_LastOpen[0] = btn_Snapshot;
                btn_Snapshot.setToolTipText("Last opened Files/Projects");//$NON-NLS-1$ I18N Needs translation!
                btn_Snapshot.setImage(ResourceManager.getImage("icon16_snapshot.png")); //$NON-NLS-1$
            }
        }
        {
            ToolItem toolItem_NewOpenSave = new ToolItem(toolBar, SWT.NONE);
            {
                Button btn_New = new Button(toolItem_NewOpenSave, SWT.NONE);
                this.btn_New[0] = btn_New;
                btn_New.setToolTipText(I18n.EDITOR3D_New);
                btn_New.setImage(ResourceManager.getImage("icon16_document-new.png")); //$NON-NLS-1$
            }
            {
                Button btn_Open = new Button(toolItem_NewOpenSave, SWT.NONE);
                this.btn_Open[0] = btn_Open;
                btn_Open.setToolTipText(I18n.EDITOR3D_Open);
                btn_Open.setImage(ResourceManager.getImage("icon16_document-open.png")); //$NON-NLS-1$
            }
            {
                Button btn_Save = new Button(toolItem_NewOpenSave, SWT.NONE);
                this.btn_Save[0] = btn_Save;
                KeyStateManager.addTooltipText(btn_Save, I18n.EDITOR3D_Save, Task.SAVE);
                btn_Save.setImage(ResourceManager.getImage("icon16_document-save.png")); //$NON-NLS-1$
            }
            {
                Button btn_SaveAll = new Button(toolItem_NewOpenSave, SWT.NONE);
                this.btn_SaveAll[0] = btn_SaveAll;
                btn_SaveAll.setToolTipText(I18n.EDITOR3D_SaveAll);
                btn_SaveAll.setImage(ResourceManager.getImage("icon16_document-saveall.png")); //$NON-NLS-1$
            }
        }
        {
            ToolItem toolItem_NewOpenDAT = new ToolItem(toolBar, SWT.NONE);
            {
                Button btn_NewDat = new Button(toolItem_NewOpenDAT, SWT.NONE);
                this.btn_NewDat[0] = btn_NewDat;
                btn_NewDat.setToolTipText(I18n.EDITOR3D_NewDat);
                btn_NewDat.setImage(ResourceManager.getImage("icon16_document-newdat.png")); //$NON-NLS-1$
            }
            {
                Button btn_OpenDAT = new Button(toolItem_NewOpenDAT, SWT.NONE);
                this.btn_OpenDat[0] = btn_OpenDAT;
                btn_OpenDAT.setToolTipText(I18n.EDITOR3D_OpenDat);
                btn_OpenDAT.setImage(ResourceManager.getImage("icon16_document-opendat.png")); //$NON-NLS-1$
            }
        }
        {
            ToolItem toolItem_HideUnhide = new ToolItem(toolBar, SWT.NONE);
            {
                Button btn_Hide = new Button(toolItem_HideUnhide, SWT.NONE);
                this.btn_Hide[0] = btn_Hide;
                btn_Hide.setToolTipText("Hide"); // I18N Needs translation //$NON-NLS-1$
                btn_Hide.setImage(ResourceManager.getImage("icon16_hide.png")); //$NON-NLS-1$
            }
            {
                Button btn_Unhide = new Button(toolItem_HideUnhide, SWT.NONE);
                this.btn_ShowAll[0] = btn_Unhide;
                btn_Unhide.setToolTipText("Show All"); // I18N Needs translation //$NON-NLS-1$
                btn_Unhide.setImage(ResourceManager.getImage("icon16_unhide.png")); //$NON-NLS-1$
            }
        }
        {
            ToolItem toolItem_MiscToggle = new ToolItem(toolBar, SWT.NONE);
            {
                Button btn_AdjacentMove = new Button(toolItem_MiscToggle, SWT.TOGGLE);
                this.btn_MoveAdjacentData[0] = btn_AdjacentMove;
                btn_AdjacentMove.setToolTipText("Move Adjacent Data"); //$NON-NLS-1$ I18N
                btn_AdjacentMove.setImage(ResourceManager.getImage("icon16_adjacentmove.png")); //$NON-NLS-1$
                btn_AdjacentMove.setSelection(false);
            }
            {
                Button btn_TransSelection = new Button(toolItem_MiscToggle, SWT.TOGGLE);
                this.btn_NoTransparentSelection[0] = btn_TransSelection;
                btn_TransSelection.setToolTipText("Toggle Selection through transparent Objects"); // I18N Needs translation //$NON-NLS-1$
                btn_TransSelection.setImage(ResourceManager.getImage("icon16_notrans.png")); //$NON-NLS-1$
            }
            {
                Button btn_BFCToggle = new Button(toolItem_MiscToggle, SWT.TOGGLE);
                this.btn_BFCToggle[0] = btn_BFCToggle;
                btn_BFCToggle.setToolTipText("Toggle BFC Winding (for new surfaces)"); // I18N Needs translation //$NON-NLS-1$
                btn_BFCToggle.setImage(ResourceManager.getImage("icon16_bfc.png")); //$NON-NLS-1$
            }
        }
        ToolItem toolItem_UndoRedo = new ToolItem(toolBar, SWT.NONE);
        {
            Button btn_Undo = new Button(toolItem_UndoRedo, SWT.NONE);
            this.btn_Undo[0] = btn_Undo;
            btn_Undo.setImage(ResourceManager.getImage("icon16_undo.png")); //$NON-NLS-1$
            KeyStateManager.addTooltipText(btn_Undo, I18n.EDITOR3D_Undo, Task.UNDO);
        }
        if (NLogger.DEBUG) {
            Button btn_Snapshot = new Button(toolItem_UndoRedo, SWT.NONE);
            this.btn_AddHistory[0] = btn_Snapshot;
            btn_Snapshot.setImage(ResourceManager.getImage("icon16_snapshot.png")); //$NON-NLS-1$
            btn_Snapshot.setToolTipText(I18n.EDITOR3D_Snapshot);
        }
        {
            Button btn_Redo = new Button(toolItem_UndoRedo, SWT.NONE);
            this.btn_Redo[0] = btn_Redo;
            btn_Redo.setImage(ResourceManager.getImage("icon16_redo.png")); //$NON-NLS-1$
            KeyStateManager.addTooltipText(btn_Redo, I18n.EDITOR3D_Redo, Task.REDO);
        }
        {
            ToolItem toolItem_Transformations = new ToolItem(toolBar, SWT.NONE);
            {
                Button btn_Select = new Button(toolItem_Transformations, SWT.TOGGLE);
                this.btn_Select[0] = btn_Select;
                KeyStateManager.addTooltipText(btn_Select, I18n.EDITOR3D_Select, Task.MODE_SELECT);
                btn_Select.setSelection(true);
                btn_Select.setImage(ResourceManager.getImage("icon16_select.png")); //$NON-NLS-1$
            }
            {
                Button btn_Move = new Button(toolItem_Transformations, SWT.TOGGLE);
                this.btn_Move[0] = btn_Move;
                KeyStateManager.addTooltipText(btn_Move, I18n.EDITOR3D_Move, Task.MODE_MOVE);
                btn_Move.setImage(ResourceManager.getImage("icon16_move.png")); //$NON-NLS-1$
            }
            {
                Button btn_Rotate = new Button(toolItem_Transformations, SWT.TOGGLE);
                this.btn_Rotate[0] = btn_Rotate;
                KeyStateManager.addTooltipText(btn_Rotate, I18n.EDITOR3D_Rotate, Task.MODE_ROTATE);
                btn_Rotate.setImage(ResourceManager.getImage("icon16_rotate.png")); //$NON-NLS-1$
            }
            {
                Button btn_Scale = new Button(toolItem_Transformations, SWT.TOGGLE);
                this.btn_Scale[0] = btn_Scale;
                KeyStateManager.addTooltipText(btn_Scale, I18n.EDITOR3D_Scale, Task.MODE_SCALE);
                btn_Scale.setImage(ResourceManager.getImage("icon16_scale.png")); //$NON-NLS-1$
            }
            {
                Button btn_Combined = new Button(toolItem_Transformations, SWT.TOGGLE);
                this.btn_Combined[0] = btn_Combined;
                KeyStateManager.addTooltipText(btn_Combined, "Combined Mode", Task.MODE_COMBINED); // I18N Needs translation! //$NON-NLS-1$
                btn_Combined.setImage(ResourceManager.getImage("icon16_combined.png")); //$NON-NLS-1$
            }
        }
        {
            ToolItem toolItem_TransformationModes = new ToolItem(toolBar, SWT.NONE);
            {
                Button btn_Local = new Button(toolItem_TransformationModes, SWT.TOGGLE);
                this.btn_Local[0] = btn_Local;
                btn_Local.setToolTipText("Local"); //$NON-NLS-1$ I18N
                btn_Local.setSelection(true);
                btn_Local.setImage(ResourceManager.getImage("icon16_local.png")); //$NON-NLS-1$
            }
            {
                Button btn_Global = new Button(toolItem_TransformationModes, SWT.TOGGLE);
                this.btn_Global[0] = btn_Global;
                btn_Global.setToolTipText("Global"); //$NON-NLS-1$ I18N
                btn_Global.setImage(ResourceManager.getImage("icon16_global.png")); //$NON-NLS-1$
            }
            {
                final Button btn_ManipulatorActions = new Button(toolItem_TransformationModes, SWT.ARROW | SWT.DOWN);
                btn_ManipulatorActions.setToolTipText("Modify the Manipulator"); //$NON-NLS-1$ I18N
                this.mnu_Manipulator = new Menu(this.getShell(), SWT.POP_UP);
                btn_ManipulatorActions.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Point loc = btn_ManipulatorActions.getLocation();
                        Rectangle rect = btn_ManipulatorActions.getBounds();
                        Point mLoc = new Point(loc.x - 1, loc.y + rect.height);
                        mnu_Manipulator.setLocation(getShell().getDisplay().map(btn_ManipulatorActions.getParent(), null, mLoc));
                        mnu_Manipulator.setVisible(true);
                    }
                });
                {
                    {
                        MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                        this.btn_Manipulator_0_toOrigin[0] = btn_Mani;
                        btn_Mani.setText("Move the Manipulator to Origin"); //$NON-NLS-1$ I18N
                        btn_Mani.setImage(ResourceManager.getImage("icon16_toOrigin.png")); //$NON-NLS-1$
                    }
                    {
                        MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                        this.btn_Manipulator_XIII_toWorld[0] = btn_Mani;
                        btn_Mani.setText("Orientate the Manipulator to World"); //$NON-NLS-1$ I18N
                        btn_Mani.setImage(ResourceManager.getImage("icon16_toWorld.png")); //$NON-NLS-1$
                    }
                    {
                        MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                        this.btn_Manipulator_XIV_adjustRotationCenter[0] = btn_Mani;
                        btn_Mani.setText("Adjust the Rotation Center"); //$NON-NLS-1$ I18N
                        btn_Mani.setImage(ResourceManager.getImage("icon16_adjustrotationcenter.png")); //$NON-NLS-1$
                    }
                    @SuppressWarnings("unused")
                    final MenuItem mntmSeparator1 = new MenuItem(mnu_Manipulator, SWT.SEPARATOR);
                    {
                        MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                        this.btn_Manipulator_X_XReverse[0] = btn_Mani;
                        btn_Mani.setText("Reverse the Manipulator X Axis"); //$NON-NLS-1$ I18N
                        btn_Mani.setImage(ResourceManager.getImage("icon16_Xinv.png")); //$NON-NLS-1$
                    }
                    {
                        MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                        this.btn_Manipulator_XI_YReverse[0] = btn_Mani;
                        btn_Mani.setText("Reverse the Manipulator Y Axis"); //$NON-NLS-1$ I18N
                        btn_Mani.setImage(ResourceManager.getImage("icon16_Yinv.png")); //$NON-NLS-1$
                    }
                    {
                        MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                        this.btn_Manipulator_XII_ZReverse[0] = btn_Mani;
                        btn_Mani.setText("Reverse the Manipulator Z Axis"); //$NON-NLS-1$ I18N
                        btn_Mani.setImage(ResourceManager.getImage("icon16_Zinv.png")); //$NON-NLS-1$
                    }
                    @SuppressWarnings("unused")
                    final MenuItem mntmSeparator2 = new MenuItem(mnu_Manipulator, SWT.SEPARATOR);
                    {
                        MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                        this.btn_Manipulator_SwitchXY[0] = btn_Mani;
                        btn_Mani.setText("Swap the Manipulator X/Y Axes"); //$NON-NLS-1$ I18N
                        btn_Mani.setImage(ResourceManager.getImage("icon16_XswapY.png")); //$NON-NLS-1$
                    }
                    {
                        MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                        this.btn_Manipulator_SwitchXZ[0] = btn_Mani;
                        btn_Mani.setText("Swap the Manipulator X/Z Axes"); //$NON-NLS-1$ I18N
                        btn_Mani.setImage(ResourceManager.getImage("icon16_XswapZ.png")); //$NON-NLS-1$
                    }
                    {
                        MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                        this.btn_Manipulator_SwitchYZ[0] = btn_Mani;
                        btn_Mani.setText("Swap the Manipulator Y/Z Axes"); //$NON-NLS-1$ I18N
                        btn_Mani.setImage(ResourceManager.getImage("icon16_YswapZ.png")); //$NON-NLS-1$
                    }
                    @SuppressWarnings("unused")
                    final MenuItem mntmSeparator3 = new MenuItem(mnu_Manipulator, SWT.SEPARATOR);
                    {
                        MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                        this.btn_Manipulator_1_cameraToPos[0] = btn_Mani;
                        btn_Mani.setText("Move the Camera to the Manipulator Position"); //$NON-NLS-1$ I18N
                        btn_Mani.setImage(ResourceManager.getImage("icon16_cameratomanipulator.png")); //$NON-NLS-1$
                    }
                    {
                        MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                        this.btn_Manipulator_2_toAverage[0] = btn_Mani;
                        btn_Mani.setText("Move the Manipulator to the Average Point of the Selection"); //$NON-NLS-1$ I18N
                        btn_Mani.setImage(ResourceManager.getImage("icon16_toavg.png")); //$NON-NLS-1$
                    }
                    @SuppressWarnings("unused")
                    final MenuItem mntmSeparator31 = new MenuItem(mnu_Manipulator, SWT.SEPARATOR);
                    {
                        MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                        this.btn_Manipulator_3_toSubfile[0] = btn_Mani;
                        btn_Mani.setText("Move the Manipulator to the Origin from a selected Subfile"); //$NON-NLS-1$ I18N
                        btn_Mani.setImage(ResourceManager.getImage("icon16_tosubfile.png")); //$NON-NLS-1$
                    }
                    {
                        MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                        this.btn_Manipulator_32_subfileTo[0] = btn_Mani;
                        btn_Mani.setText("Move a selected Subfile to the Manipulator"); //$NON-NLS-1$ I18N
                        btn_Mani.setImage(ResourceManager.getImage("icon16_tosubfile.png")); //$NON-NLS-1$
                    }
                    @SuppressWarnings("unused")
                    final MenuItem mntmSeparator32 = new MenuItem(mnu_Manipulator, SWT.SEPARATOR);
                    {
                        MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                        this.btn_Manipulator_4_toVertex[0] = btn_Mani;
                        btn_Mani.setText("Move the Manipulator to the Nearest Vertex"); //$NON-NLS-1$ I18N
                        btn_Mani.setImage(ResourceManager.getImage("icon16_tonearestvertex.png")); //$NON-NLS-1$
                    }
                    {
                        MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                        this.btn_Manipulator_5_toEdge[0] = btn_Mani;
                        btn_Mani.setText("Move the Manipulator to the Nearest Edge"); //$NON-NLS-1$ I18N
                        btn_Mani.setImage(ResourceManager.getImage("icon16_tonearestedge.png")); //$NON-NLS-1$
                    }
                    {
                        MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                        this.btn_Manipulator_6_toSurface[0] = btn_Mani;
                        btn_Mani.setText("Move the Manipulator to the Nearest Face"); //$NON-NLS-1$ I18N
                        btn_Mani.setImage(ResourceManager.getImage("icon16_tonearestface.png")); //$NON-NLS-1$
                    }
                    @SuppressWarnings("unused")
                    final MenuItem mntmSeparator4 = new MenuItem(mnu_Manipulator, SWT.SEPARATOR);
                    {
                        MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                        this.btn_Manipulator_7_toVertexNormal[0] = btn_Mani;
                        btn_Mani.setText("Adjust the Manipulator to the Nearest Vertex Normal"); //$NON-NLS-1$ I18N
                        btn_Mani.setImage(ResourceManager.getImage("icon16_tonearestvertexN.png")); //$NON-NLS-1$
                    }
                    {
                        MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                        this.btn_Manipulator_8_toEdgeNormal[0] = btn_Mani;
                        btn_Mani.setText("Adjust the Manipulator to the Nearest Edge Normal"); //$NON-NLS-1$ I18N
                        btn_Mani.setImage(ResourceManager.getImage("icon16_tonearestedgeN.png")); //$NON-NLS-1$
                    }
                    {
                        MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                        this.btn_Manipulator_9_toSurfaceNormal[0] = btn_Mani;
                        btn_Mani.setText("Adjust the Manipulator to the Nearest Face Normal"); //$NON-NLS-1$ I18N
                        btn_Mani.setImage(ResourceManager.getImage("icon16_tonearestfaceN.png")); //$NON-NLS-1$
                    }
                }
            }
        }
        {
            ToolItem toolItem_MiscClick = new ToolItem(toolBar, SWT.NONE);
            {
                Button btn_BFCswap = new Button(toolItem_MiscClick, SWT.NONE);
                this.btn_BFCswap[0] = btn_BFCswap;
                btn_BFCswap.setToolTipText("Swap BFC Winding"); //$NON-NLS-1$ I18N
                btn_BFCswap.setImage(ResourceManager.getImage("icon16_bfcSwap.png")); //$NON-NLS-1$
            }
            {
                Button btn_CompileSubfile = new Button(toolItem_MiscClick, SWT.NONE);
                this.btn_CompileSubfile[0] = btn_CompileSubfile;
                btn_CompileSubfile.setToolTipText("Compile Subfile Data"); //$NON-NLS-1$ I18N
                btn_CompileSubfile.setImage(ResourceManager.getImage("icon16_subcompile.png")); //$NON-NLS-1$
            }
            {
                Button btn_SplitQuad = new Button(toolItem_MiscClick, SWT.NONE);
                this.btn_SplitQuad[0] = btn_SplitQuad;
                btn_SplitQuad.setImage(ResourceManager.getImage("icon16_quadToTri.png")); //$NON-NLS-1$
                btn_SplitQuad.setToolTipText("Split Quad into Triangles"); //$NON-NLS-1$ I18N Needs translation!
            }
            {
                Button btn_RoundSelection = new Button(toolItem_MiscClick, SWT.NONE);
                this.btn_RoundSelection[0] = btn_RoundSelection;
                btn_RoundSelection.setToolTipText("Round"); //$NON-NLS-1$ I18N
                btn_RoundSelection.setImage(ResourceManager.getImage("icon16_round.png")); //$NON-NLS-1$
            }
            {
                final Button btn_Select = new Button(toolItem_MiscClick, SWT.PUSH);
                this.btn_Select2[0] = btn_Select;
                btn_Select.setToolTipText("Select…"); //$NON-NLS-1$ I18N
                btn_Select.setText("Select…"); //$NON-NLS-1$ I18N
                this.mnu_Select = new Menu(this.getShell(), SWT.POP_UP);
                btn_Select.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        showSelectMenu();
                    }
                });
                {
                    {
                        MenuItem mntm_SelectAll = new MenuItem(mnu_Select, SWT.PUSH);
                        this.mntm_SelectAll[0] = mntm_SelectAll;
                        KeyStateManager.addKeyText(mntm_SelectAll, "…All.", Task.SELECT_ALL); //$NON-NLS-1$ I18N
                    }
                    {
                        MenuItem mntm_SelectNone = new MenuItem(mnu_Select, SWT.PUSH);
                        this.mntm_SelectNone[0] = mntm_SelectNone;
                        KeyStateManager.addKeyText(mntm_SelectNone, "…None.", Task.SELECT_NONE); //$NON-NLS-1$ I18N
                    }
                    @SuppressWarnings("unused")
                    final MenuItem mntmSeparator0 = new MenuItem(mnu_Select, SWT.SEPARATOR);
                    {
                        MenuItem mntm_SelectInverse = new MenuItem(mnu_Select, SWT.PUSH);
                        this.mntm_SelectInverse[0] = mntm_SelectInverse;
                        mntm_SelectInverse.setText("…Inverse (Shown)."); //$NON-NLS-1$ I18N
                    }
                    @SuppressWarnings("unused")
                    final MenuItem mntmSeparator1 = new MenuItem(mnu_Select, SWT.SEPARATOR);
                    {
                        MenuItem mntm_SelectAllVisible = new MenuItem(mnu_Select, SWT.PUSH);
                        this.mntm_SelectAllVisible[0] = mntm_SelectAllVisible;
                        mntm_SelectAllVisible.setText("…All Shown."); //$NON-NLS-1$ I18N
                    }
                    {
                        MenuItem mntm_SelectAllWithColours = new MenuItem(mnu_Select, SWT.PUSH);
                        this.mntm_SelectAllWithColours[0] = mntm_SelectAllWithColours;
                        KeyStateManager.addKeyText(mntm_SelectAllWithColours, "…All with Same Colours.", Task.SELECT_ALL_WITH_SAME_COLOURS); //$NON-NLS-1$ I18N
                    }
                    {
                        MenuItem mntm_SelectAllVisibleWithColours = new MenuItem(mnu_Select, SWT.PUSH);
                        this.mntm_SelectAllVisibleWithColours[0] = mntm_SelectAllVisibleWithColours;
                        mntm_SelectAllVisibleWithColours.setText("…All Shown with Same Colours."); //$NON-NLS-1$ I18N
                    }
                    @SuppressWarnings("unused")
                    final MenuItem mntmSeparator2 = new MenuItem(mnu_Select, SWT.SEPARATOR);
                    {
                        MenuItem mntm_SelectEverything = new MenuItem(mnu_Select, SWT.PUSH);
                        this.mntm_SelectEverything[0] = mntm_SelectEverything;
                        mntm_SelectEverything.setText("…Everything…"); //$NON-NLS-1$ I18N
                        mntm_SelectEverything.setEnabled(false);
                    }
                    {
                        MenuItem mntm_SelectConnected = new MenuItem(mnu_Select, SWT.PUSH);
                        this.mntm_SelectConnected[0] = mntm_SelectConnected;
                        KeyStateManager.addKeyText(mntm_SelectConnected, "…Connected…", Task.SELECT_CONNECTED); //$NON-NLS-1$ I18N
                    }
                    {
                        MenuItem mntm_SelectTouching = new MenuItem(mnu_Select, SWT.PUSH);
                        this.mntm_SelectTouching[0] = mntm_SelectTouching;
                        KeyStateManager.addKeyText(mntm_SelectTouching, "…Touching…", Task.SELECT_TOUCHING); //$NON-NLS-1$ I18N
                    }
                    {
                        MenuItem mntm_WithSameColour = new MenuItem(mnu_Select, SWT.CHECK);
                        this.mntm_WithSameColour[0] = mntm_WithSameColour;
                        KeyStateManager.addKeyText(mntm_WithSameColour, "…  with Same Colour.", Task.SELECT_OPTION_WITH_SAME_COLOURS); //$NON-NLS-1$ I18N
                    }
                    {
                        MenuItem mntm_WithSameOrientation = new MenuItem(mnu_Select, SWT.CHECK);
                        this.mntm_WithSameOrientation[0] = mntm_WithSameOrientation;
                        mntm_WithSameOrientation.setText("…  with Same Orientation¹."); //$NON-NLS-1$ I18N
                    }
                    {
                        MenuItem mntm_WithAccuracy = new MenuItem(mnu_Select, SWT.CHECK);
                        this.mntm_WithAccuracy[0] = mntm_WithAccuracy;
                        mntm_WithAccuracy.setText("…  with Specified Accuracy¹ ²."); //$NON-NLS-1$ I18N
                    }
                    {
                        MenuItem mntm_WithHiddenData = new MenuItem(mnu_Select, SWT.CHECK);
                        this.mntm_WithHiddenData[0] = mntm_WithHiddenData;
                        mntm_WithHiddenData.setText("…  what is Hidden."); //$NON-NLS-1$ I18N
                        mntm_WithHiddenData.setImage(ResourceManager.getImage("icon16_hide.png")); //$NON-NLS-1$
                    }
                    {
                        MenuItem mntm_ExceptSubfiles = new MenuItem(mnu_Select, SWT.CHECK);
                        this.mntm_ExceptSubfiles[0] = mntm_ExceptSubfiles;
                        mntm_ExceptSubfiles.setText("…  except Subfile Content."); //$NON-NLS-1$ I18N
                    }
                    {
                        MenuItem mntm_WithWholeSubfiles = new MenuItem(mnu_Select, SWT.CHECK);
                        this.mntm_WithWholeSubfiles[0] = mntm_WithWholeSubfiles;
                        mntm_WithWholeSubfiles.setText("…  with Whole Subfile Selection."); //$NON-NLS-1$ I18N
                    }
                    {
                        MenuItem mntm_StopAtEdges = new MenuItem(mnu_Select, SWT.CHECK);
                        this.mntm_StopAtEdges[0] = mntm_StopAtEdges;
                        mntm_StopAtEdges.setText("…  and Stop Selection at Edges²."); //$NON-NLS-1$ I18N
                    }
                    @SuppressWarnings("unused")
                    final MenuItem mntmSeparator3 = new MenuItem(mnu_Select, SWT.SEPARATOR);
                    {
                        MenuItem mntm_SelectIsolatedVertices = new MenuItem(mnu_Select, SWT.PUSH);
                        this.mntm_SelectIsolatedVertices[0] = mntm_SelectIsolatedVertices;
                        mntm_SelectIsolatedVertices.setText("…Isolated Vertices."); //$NON-NLS-1$ I18N
                    }
                    @SuppressWarnings("unused")
                    final MenuItem mntmSeparator4 = new MenuItem(mnu_Select, SWT.SEPARATOR);
                    {
                        MenuItem mntm_SVertices = new MenuItem(mnu_Select, SWT.CHECK);
                        this.mntm_SVertices[0] = mntm_SVertices;
                        mntm_SVertices.setText("Vertices."); //$NON-NLS-1$ I18N
                        mntm_SVertices.setSelection(true);
                    }
                    {
                        MenuItem mntm_SLines = new MenuItem(mnu_Select, SWT.CHECK);
                        this.mntm_SLines[0] = mntm_SLines;
                        mntm_SLines.setText("Lines."); //$NON-NLS-1$ I18N
                        mntm_SLines.setSelection(true);
                    }
                    {
                        MenuItem mntm_STriangles = new MenuItem(mnu_Select, SWT.CHECK);
                        this.mntm_STriangles[0] = mntm_STriangles;
                        mntm_STriangles.setText("Triangles."); //$NON-NLS-1$ I18N
                        mntm_STriangles.setSelection(true);
                    }
                    {
                        MenuItem mntm_SQuads = new MenuItem(mnu_Select, SWT.CHECK);
                        this.mntm_SQuads[0] = mntm_SQuads;
                        mntm_SQuads.setText("Quads."); //$NON-NLS-1$ I18N
                        mntm_SQuads.setSelection(true);
                    }
                    {
                        MenuItem mntm_SCLines = new MenuItem(mnu_Select, SWT.CHECK);
                        this.mntm_SCLines[0] = mntm_SCLines;
                        mntm_SCLines.setText("Conditional Lines."); //$NON-NLS-1$ I18N
                        mntm_SCLines.setSelection(true);
                    }
                    @SuppressWarnings("unused")
                    final MenuItem mntmSeparator5 = new MenuItem(mnu_Select, SWT.SEPARATOR);
                    {
                        MenuItem mntm_needsThreshold = new MenuItem(mnu_Select, SWT.PUSH);
                        mntm_needsThreshold.setText("¹ needs a threshold."); //$NON-NLS-1$ I18N
                        mntm_needsThreshold.setEnabled(false);
                    }
                    {
                        MenuItem mntm_noEffect = new MenuItem(mnu_Select, SWT.PUSH);
                        mntm_noEffect.setText("² has no effect on 'Select Everything'."); //$NON-NLS-1$ I18N
                        mntm_noEffect.setEnabled(false);
                    }
                }
            }
            {
                final Button btn_MergeNSplit = new Button(toolItem_MiscClick, SWT.PUSH);
                btn_MergeNSplit.setToolTipText("Merge/Split…"); //$NON-NLS-1$ I18N
                btn_MergeNSplit.setText("Merge/Split…"); //$NON-NLS-1$ I18N
                this.mnu_Merge = new Menu(this.getShell(), SWT.POP_UP);
                btn_MergeNSplit.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Point loc = btn_MergeNSplit.getLocation();
                        Rectangle rect = btn_MergeNSplit.getBounds();
                        Point mLoc = new Point(loc.x - 1, loc.y + rect.height);
                        mnu_Merge.setLocation(getShell().getDisplay().map(btn_MergeNSplit.getParent(), null, mLoc));
                        mnu_Merge.setVisible(true);
                    }
                });
                {
                    MenuItem mntm_Flip = new MenuItem(mnu_Merge, SWT.PUSH);
                    this.mntm_Flip[0] = mntm_Flip;
                    mntm_Flip.setText("Flip / Rotate Vertices"); //$NON-NLS-1$ I18N
                }
                @SuppressWarnings("unused")
                final MenuItem mntmSeparator2 = new MenuItem(mnu_Merge, SWT.SEPARATOR);
                {
                    MenuItem mntm_Split = new MenuItem(mnu_Merge, SWT.PUSH);
                    this.mntm_Split[0] = mntm_Split;
                    KeyStateManager.addKeyText(mntm_Split, "Split", Task.SPLIT); //$NON-NLS-1$ I18N
                }
                {
                    MenuItem mntm_SplitNTimes = new MenuItem(mnu_Merge, SWT.PUSH);
                    this.mntm_SplitNTimes[0] = mntm_SplitNTimes;
                    mntm_SplitNTimes.setText("Split N-Times"); //$NON-NLS-1$ I18N
                }
                @SuppressWarnings("unused")
                final MenuItem mntmSeparator3 = new MenuItem(mnu_Merge, SWT.SEPARATOR);
                {
                    MenuItem mntm_mergeTo = new MenuItem(mnu_Merge, SWT.PUSH);
                    this.mntm_MergeToAverage[0] = mntm_mergeTo;
                    KeyStateManager.addKeyText(mntm_mergeTo, "Merge To Average", Task.MERGE_TO_AVERAGE); //$NON-NLS-1$ I18N
                }
                {
                    MenuItem mntm_mergeTo = new MenuItem(mnu_Merge, SWT.PUSH);
                    this.mntm_MergeToLastSelected[0] = mntm_mergeTo;
                    KeyStateManager.addKeyText(mntm_mergeTo, "Merge To Last Selected Vertex", Task.MERGE_TO_LAST); //$NON-NLS-1$ I18N
                }
                {
                    MenuItem mntm_mergeTo = new MenuItem(mnu_Merge, SWT.PUSH);
                    this.mntm_MergeToNearestVertex[0] = mntm_mergeTo;
                    mntm_mergeTo.setText("Merge To Nearest Vertex"); //$NON-NLS-1$ I18N
                }
                {
                    MenuItem mntm_mergeTo = new MenuItem(mnu_Merge, SWT.PUSH);
                    this.mntm_MergeToNearestEdge[0] = mntm_mergeTo;
                    mntm_mergeTo.setText("Merge To Nearest Edge"); //$NON-NLS-1$ I18N
                }
                {
                    MenuItem mntm_mergeTo = new MenuItem(mnu_Merge, SWT.PUSH);
                    this.mntm_MergeToNearestFace[0] = mntm_mergeTo;
                    mntm_mergeTo.setText("Merge To Nearest Face"); //$NON-NLS-1$ I18N
                }
                @SuppressWarnings("unused")
                final MenuItem mntmSeparator4 = new MenuItem(mnu_Merge, SWT.SEPARATOR);
                {
                    MenuItem mntm_setXYZ = new MenuItem(mnu_Merge, SWT.PUSH);
                    this.mntm_setXYZ[0] = mntm_setXYZ;
                    mntm_setXYZ.setText("Set X/Y/Z (for Selection)"); //$NON-NLS-1$ I18N
                }
                {
                    MenuItem mntm_Translate = new MenuItem(mnu_Merge, SWT.PUSH);
                    this.mntm_Translate[0] = mntm_Translate;
                    mntm_Translate.setText("Translate Selection"); //$NON-NLS-1$ I18N
                }
                {
                    MenuItem mntm_Rotate = new MenuItem(mnu_Merge, SWT.PUSH);
                    this.mntm_Rotate[0] = mntm_Rotate;
                    mntm_Rotate.setText("Rotate Selection"); //$NON-NLS-1$ I18N
                }
                {
                    MenuItem mntm_Scale = new MenuItem(mnu_Merge, SWT.PUSH);
                    this.mntm_Scale[0] = mntm_Scale;
                    mntm_Scale.setText("Scale Selection"); //$NON-NLS-1$ I18N
                }
                @SuppressWarnings("unused")
                final MenuItem mntmSeparator1 = new MenuItem(mnu_Merge, SWT.SEPARATOR);
                {
                    MenuItem mntm_SubdivideCatmullClark = new MenuItem(mnu_Merge, SWT.PUSH);
                    this.mntm_SubdivideCatmullClark[0] = mntm_SubdivideCatmullClark;
                    mntm_SubdivideCatmullClark.setText("Subdivide (Catmull-Clark)"); //$NON-NLS-1$ I18N
                }
                {
                    MenuItem mntm_SubdivideLoop = new MenuItem(mnu_Merge, SWT.PUSH);
                    this.mntm_SubdivideLoop[0] = mntm_SubdivideLoop;
                    mntm_SubdivideLoop.setText("Subdivide (Loop)"); //$NON-NLS-1$ I18N
                }
            }
            {
                final Button btn_ToolsActions = new Button(toolItem_MiscClick, SWT.ARROW | SWT.DOWN);
                btn_ToolsActions.setToolTipText("Tools and Options"); //$NON-NLS-1$ I18N
                this.mnu_Tools = new Menu(this.getShell(), SWT.POP_UP);
                btn_ToolsActions.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Point loc = btn_ToolsActions.getLocation();
                        Rectangle rect = btn_ToolsActions.getBounds();
                        Point mLoc = new Point(loc.x - 1, loc.y + rect.height);
                        mnu_Tools.setLocation(getShell().getDisplay().map(btn_ToolsActions.getParent(), null, mLoc));
                        mnu_Tools.setVisible(true);
                    }
                });
                {
                    {
                        MenuItem mntm_Edger2 = new MenuItem(mnu_Tools, SWT.PUSH);
                        this.mntm_Edger2[0] = mntm_Edger2;
                        mntm_Edger2.setText("Edger2"); //$NON-NLS-1$ I18N
                    }
                    {
                        MenuItem mntm_Edger2 = new MenuItem(mnu_Tools, SWT.PUSH);
                        this.mntm_Txt2Dat[0] = mntm_Edger2;
                        mntm_Edger2.setText("Txt2Dat"); //$NON-NLS-1$ I18N
                    }
                    {
                        MenuItem mntm_Rectifier = new MenuItem(mnu_Tools, SWT.PUSH);
                        this.mntm_Rectifier[0] = mntm_Rectifier;
                        mntm_Rectifier.setText("Rectifier"); //$NON-NLS-1$ I18N
                    }
                    {
                        MenuItem mntm_Isecalc = new MenuItem(mnu_Tools, SWT.PUSH);
                        this.mntm_Isecalc[0] = mntm_Isecalc;
                        mntm_Isecalc.setText("Isecalc"); //$NON-NLS-1$ I18N
                    }
                    {
                        MenuItem mntm_SlicerPro = new MenuItem(mnu_Tools, SWT.PUSH);
                        this.mntm_SlicerPro[0] = mntm_SlicerPro;
                        mntm_SlicerPro.setText("SlicerPro"); //$NON-NLS-1$ I18N
                    }
                    {
                        MenuItem mntm_Intersector = new MenuItem(mnu_Tools, SWT.PUSH);
                        this.mntm_Intersector[0] = mntm_Intersector;
                        mntm_Intersector.setText("Intersector"); //$NON-NLS-1$ I18N
                    }
                    {
                        MenuItem mntm_Lines2Pattern = new MenuItem(mnu_Tools, SWT.PUSH);
                        this.mntm_Lines2Pattern[0] = mntm_Lines2Pattern;
                        mntm_Lines2Pattern.setText("Lines2Pattern"); //$NON-NLS-1$ I18N
                    }
                    {
                        MenuItem mntm_PathTruder = new MenuItem(mnu_Tools, SWT.PUSH);
                        this.mntm_PathTruder[0] = mntm_PathTruder;
                        mntm_PathTruder.setText("PathTruder"); //$NON-NLS-1$ I18N
                    }
                    {
                        MenuItem mntm_SymSplitter = new MenuItem(mnu_Tools, SWT.PUSH);
                        this.mntm_SymSplitter[0] = mntm_SymSplitter;
                        mntm_SymSplitter.setText("SymSplitter"); //$NON-NLS-1$ I18N
                    }
                    {
                        MenuItem mntm_Unificator = new MenuItem(mnu_Tools, SWT.PUSH);
                        this.mntm_Unificator[0] = mntm_Unificator;
                        mntm_Unificator.setText("Unificator"); //$NON-NLS-1$ I18N
                    }
                    {
                        MenuItem mntm_RingsAndCones = new MenuItem(mnu_Tools, SWT.PUSH);
                        this.mntm_RingsAndCones[0] = mntm_RingsAndCones;
                        mntm_RingsAndCones.setText("Rings and Cones"); //$NON-NLS-1$ I18N
                    }
                    @SuppressWarnings("unused")
                    final MenuItem mntmSeparator1 = new MenuItem(mnu_Tools, SWT.SEPARATOR);
                    {
                        MenuItem mntm_ResetSettingsOnRestart = new MenuItem(mnu_Tools, SWT.PUSH);
                        this.mntm_ResetSettingsOnRestart[0] = mntm_ResetSettingsOnRestart;
                        mntm_ResetSettingsOnRestart.setText("Reset All Settings On Restart"); //$NON-NLS-1$ I18N
                    }
                    {
                        MenuItem mntm_SelectAnotherLDConfig = new MenuItem(mnu_Tools, SWT.PUSH);
                        this.mntm_SelectAnotherLDConfig[0] = mntm_SelectAnotherLDConfig;
                        mntm_SelectAnotherLDConfig.setText("Select LDConfig.ldr"); //$NON-NLS-1$ I18N
                    }
                    {
                        final MenuItem mntm_SetIconSize = new MenuItem(mnu_Tools, SWT.CASCADE);
                        mntm_SetIconSize.setText("Set Icon Size"); //$NON-NLS-1$ I18N
                        final Menu mnu_IconSize = new Menu(mntm_SetIconSize);
                        mntm_SetIconSize.setMenu(mnu_IconSize);
                        final int iconSize = Editor3DWindow.getIconsize();
                        {
                            MenuItem mntm_IconSize = new MenuItem(mnu_IconSize, SWT.RADIO);
                            this.mntm_IconSize1[0] = mntm_IconSize;
                            mntm_IconSize.setText("Icon Size 1"); //$NON-NLS-1$ I18N
                            mntm_IconSize.setSelection(iconSize == 0);
                        }
                        {
                            MenuItem mntm_IconSize = new MenuItem(mnu_IconSize, SWT.RADIO);
                            this.mntm_IconSize2[0] = mntm_IconSize;
                            mntm_IconSize.setText("Icon Size 2"); //$NON-NLS-1$ I18N
                            mntm_IconSize.setSelection(iconSize == 1);
                        }
                        {
                            MenuItem mntm_IconSize = new MenuItem(mnu_IconSize, SWT.RADIO);
                            this.mntm_IconSize3[0] = mntm_IconSize;
                            mntm_IconSize.setText("Icon Size 3"); //$NON-NLS-1$ I18N
                            mntm_IconSize.setSelection(iconSize == 2);
                        }
                        {
                            MenuItem mntm_IconSize = new MenuItem(mnu_IconSize, SWT.RADIO);
                            this.mntm_IconSize4[0] = mntm_IconSize;
                            mntm_IconSize.setText("Icon Size 4"); //$NON-NLS-1$ I18N
                            mntm_IconSize.setSelection(iconSize == 3);
                        }
                        {
                            MenuItem mntm_IconSize = new MenuItem(mnu_IconSize, SWT.RADIO);
                            this.mntm_IconSize5[0] = mntm_IconSize;
                            mntm_IconSize.setText("Icon Size 5"); //$NON-NLS-1$ I18N
                            mntm_IconSize.setSelection(iconSize == 4);
                        }
                        {
                            MenuItem mntm_IconSize = new MenuItem(mnu_IconSize, SWT.RADIO);
                            this.mntm_IconSize6[0] = mntm_IconSize;
                            mntm_IconSize.setText("Icon Size 6"); //$NON-NLS-1$ I18N
                            mntm_IconSize.setSelection(iconSize == 5);
                        }
                        @SuppressWarnings("unused")
                        final MenuItem mntmSeparator2 = new MenuItem(mnu_IconSize, SWT.SEPARATOR);
                        {
                            MenuItem mntm_IconSize = new MenuItem(mnu_IconSize, SWT.PUSH);
                            mntm_IconSize.setText("(Requires restart)"); //$NON-NLS-1$ I18N
                            mntm_IconSize.setEnabled(false);
                        }
                    }
                    @SuppressWarnings("unused")
                    final MenuItem mntmSeparator2 = new MenuItem(mnu_Tools, SWT.SEPARATOR);
                    {
                        MenuItem mntm_UploadErrorLog = new MenuItem(mnu_Tools, SWT.PUSH);
                        this.mntm_UploadLogs[0] = mntm_UploadErrorLog;
                        mntm_UploadErrorLog.setText("Upload Error Logs"); //$NON-NLS-1$ I18N
                    }
                    @SuppressWarnings("unused")
                    final MenuItem mntmSeparator3 = new MenuItem(mnu_Tools, SWT.SEPARATOR);
                    {
                        MenuItem mntm_SyncWithTextEditor = new MenuItem(mnu_Tools, SWT.CHECK);
                        mntm_SyncWithTextEditor.setSelection(WorkbenchManager.getUserSettingState().getSyncWithTextEditor().get());
                        this.mntm_SyncWithTextEditor[0] = mntm_SyncWithTextEditor;
                        mntm_SyncWithTextEditor.setText("Sync. 3D Editor with Text Editor"); //$NON-NLS-1$ I18N
                    }
                    {
                        MenuItem mntm_SyncLpeInline = new MenuItem(mnu_Tools, SWT.CHECK);
                        mntm_SyncLpeInline.setSelection(WorkbenchManager.getUserSettingState().getSyncWithLpeInline().get());
                        mntm_SyncLpeInline.setEnabled(WorkbenchManager.getUserSettingState().getSyncWithTextEditor().get());
                        this.mntm_SyncLpeInline[0] = mntm_SyncLpeInline;
                        mntm_SyncLpeInline.setText("+ Automatic !LPE INLINE Parsing (slow!!)"); //$NON-NLS-1$ I18N
                    }
                }
            }

        }
        {
            ToolItem toolItem_CCPD = new ToolItem(toolBar, SWT.NONE);
            {
                Button btn_Cut = new Button(toolItem_CCPD, SWT.NONE);
                this.btn_Cut[0] = btn_Cut;
                btn_Cut.setImage(ResourceManager.getImage("icon16_edit-cut.png")); //$NON-NLS-1$
                KeyStateManager.addTooltipText(btn_Cut, I18n.COPYNPASTE_Cut, Task.CUT);
            }
            {
                Button btn_Copy = new Button(toolItem_CCPD, SWT.NONE);
                this.btn_Copy[0] = btn_Copy;
                btn_Copy.setImage(ResourceManager.getImage("icon16_edit-copy.png")); //$NON-NLS-1$
                KeyStateManager.addTooltipText(btn_Copy, I18n.COPYNPASTE_Copy, Task.COPY);
            }
            {
                Button btn_Paste = new Button(toolItem_CCPD, SWT.NONE);
                this.btn_Paste[0] = btn_Paste;
                btn_Paste.setImage(ResourceManager.getImage("icon16_edit-paste.png")); //$NON-NLS-1$
                KeyStateManager.addTooltipText(btn_Paste, I18n.COPYNPASTE_Paste, Task.PASTE);
            }
            {
                Button btn_Delete = new Button(toolItem_CCPD, SWT.NONE);
                this.btn_Delete[0] = btn_Delete;
                btn_Delete.setImage(ResourceManager.getImage("icon16_delete.png")); //$NON-NLS-1$
                KeyStateManager.addTooltipText(btn_Delete, I18n.COPYNPASTE_Delete, Task.DELETE);
            }
        }
        {
            ToolItem toolItem_Mode = new ToolItem(toolBar, SWT.NONE);
            {
                Button btn_Vertices = new Button(toolItem_Mode, SWT.TOGGLE);
                this.btn_Vertices[0] = btn_Vertices;
                KeyStateManager.addTooltipText(btn_Vertices, I18n.EDITOR3D_ModeVertex, Task.OBJ_VERTEX);
                btn_Vertices.setSelection(true);
                btn_Vertices.setImage(ResourceManager.getImage("icon16_vertices.png")); //$NON-NLS-1$
            }
            {
                Button btn_TrisNQuads = new Button(toolItem_Mode, SWT.TOGGLE);
                this.btn_TrisNQuads[0] = btn_TrisNQuads;
                KeyStateManager.addTooltipText(btn_TrisNQuads, I18n.EDITOR3D_ModeSurface, Task.OBJ_FACE);
                btn_TrisNQuads.setImage(ResourceManager.getImage("icon16_trisNquads.png")); //$NON-NLS-1$
            }
            {
                Button btn_Lines = new Button(toolItem_Mode, SWT.TOGGLE);
                this.btn_Lines[0] = btn_Lines;
                KeyStateManager.addTooltipText(btn_Lines, I18n.EDITOR3D_ModeLine, Task.OBJ_LINE);
                btn_Lines.setImage(ResourceManager.getImage("icon16_lines.png")); //$NON-NLS-1$
            }
            {
                Button btn_Subfiles = new Button(toolItem_Mode, SWT.TOGGLE);
                this.btn_Subfiles[0] = btn_Subfiles;
                KeyStateManager.addTooltipText(btn_Subfiles, I18n.EDITOR3D_ModeSubpart, Task.OBJ_PRIMITIVE);
                btn_Subfiles.setImage(ResourceManager.getImage("icon16_primitives.png")); //$NON-NLS-1$
            }
        }
        {
            ToolItem toolItem_Add = new ToolItem(toolBar, SWT.NONE);

            Button btn_AddComment = new Button(toolItem_Add, SWT.NONE);
            this.btn_AddComment[0] = btn_AddComment;
            KeyStateManager.addTooltipText(btn_AddComment, I18n.EDITOR3D_AddComment, Task.ADD_COMMENTS);
            btn_AddComment.setImage(ResourceManager.getImage("icon16_addcomment.png")); //$NON-NLS-1$

            Button btn_AddVertex = new Button(toolItem_Add, SWT.TOGGLE);
            this.btn_AddVertex[0] = btn_AddVertex;
            KeyStateManager.addTooltipText(btn_AddVertex, I18n.EDITOR3D_AddVertex, Task.ADD_VERTEX);
            btn_AddVertex.setImage(ResourceManager.getImage("icon16_addvertex.png")); //$NON-NLS-1$

            Button btn_AddPrimitive = new Button(toolItem_Add, SWT.TOGGLE);
            this.btn_AddPrimitive[0] = btn_AddPrimitive;
            btn_AddPrimitive.setToolTipText(I18n.EDITOR3D_AddSubpart);
            btn_AddPrimitive.setImage(ResourceManager.getImage("icon16_addprimitive.png")); //$NON-NLS-1$

            Button btn_AddLine = new Button(toolItem_Add, SWT.TOGGLE);
            this.btn_AddLine[0] = btn_AddLine;
            KeyStateManager.addTooltipText(btn_AddLine, I18n.EDITOR3D_AddLine, Task.ADD_LINE);
            btn_AddLine.setImage(ResourceManager.getImage("icon16_addline.png")); //$NON-NLS-1$

            Button btn_AddTriangle = new Button(toolItem_Add, SWT.TOGGLE);
            this.btn_AddTriangle[0] = btn_AddTriangle;
            KeyStateManager.addTooltipText(btn_AddTriangle, I18n.EDITOR3D_AddTriangle, Task.ADD_TRIANGLE);
            btn_AddTriangle.setImage(ResourceManager.getImage("icon16_addtriangle.png")); //$NON-NLS-1$

            Button btn_AddQuad = new Button(toolItem_Add, SWT.TOGGLE);
            this.btn_AddQuad[0] = btn_AddQuad;
            KeyStateManager.addTooltipText(btn_AddQuad, I18n.EDITOR3D_AddQuad, Task.ADD_QUAD);
            btn_AddQuad.setImage(ResourceManager.getImage("icon16_addquad.png")); //$NON-NLS-1$

            Button btn_AddCondline = new Button(toolItem_Add, SWT.TOGGLE);
            this.btn_AddCondline[0] = btn_AddCondline;
            KeyStateManager.addTooltipText(btn_AddCondline, I18n.EDITOR3D_AddCondline, Task.ADD_CONDLINE);
            btn_AddCondline.setImage(ResourceManager.getImage("icon16_addcondline.png")); //$NON-NLS-1$
        }
        {
            ToolItem toolItem_Colours = new ToolItem(toolBar, SWT.NONE);
            List<GColour> colours = WorkbenchManager.getUserSettingState().getUserPalette();
            addColorButton(toolItem_Colours, colours.get(0), 0);
            addColorButton(toolItem_Colours, colours.get(1), 1);
            addColorButton(toolItem_Colours, colours.get(2), 2);
            addColorButton(toolItem_Colours, colours.get(3), 3);
            addColorButton(toolItem_Colours, colours.get(4), 4);
            addColorButton(toolItem_Colours, colours.get(5), 5);
            addColorButton(toolItem_Colours, colours.get(6), 6);
            addColorButton(toolItem_Colours, colours.get(7), 7);
            addColorButton(toolItem_Colours, colours.get(8), 8);
            addColorButton(toolItem_Colours, colours.get(9), 9);
            addColorButton(toolItem_Colours, colours.get(10), 10);
            addColorButton(toolItem_Colours, colours.get(11), 11);
            addColorButton(toolItem_Colours, colours.get(12), 12);
            addColorButton(toolItem_Colours, colours.get(13), 13);
            addColorButton(toolItem_Colours, colours.get(14), 14);
            addColorButton(toolItem_Colours, colours.get(15), 15);
            addColorButton(toolItem_Colours, colours.get(16), 16);
            {
                Button btn_Palette = new Button(toolItem_Colours, SWT.NONE);
                this.btn_Palette[0] = btn_Palette;
                btn_Palette.setToolTipText("More…"); //$NON-NLS-1$ I18N Needs translation!
                btn_Palette.setImage(ResourceManager.getImage("icon16_colours.png")); //$NON-NLS-1$
            }
        }
        {
            ToolItem toolItem_ColourFunctions = new ToolItem(toolBar, SWT.NONE);
            {
                Button btn_LastUsedColour = new Button(toolItem_ColourFunctions, SWT.NONE);
                this.btn_LastUsedColour[0] = btn_LastUsedColour;
                btn_LastUsedColour.setToolTipText("Colour [16]"); //$NON-NLS-1$ I18N
                btn_LastUsedColour.setImage(ResourceManager.getImage("icon16_fullTransparent.png")); //$NON-NLS-1$
                final Color col = SWTResourceManager.getColor(128, 128, 128);
                final Point size = btn_LastUsedColour.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                final int x = Math.round(size.x / 5f);
                final int y = Math.round(size.y / 5f);
                final int w = Math.round(size.x * (3f / 5f));
                final int h = Math.round(size.y * (3f / 5f));
                btn_LastUsedColour.addPaintListener(new PaintListener() {
                    @Override
                    public void paintControl(PaintEvent e) {
                        e.gc.setBackground(col);
                        e.gc.fillRectangle(x, y, w, h);
                    }
                });
                btn_LastUsedColour.addSelectionListener(new SelectionListener() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        if (Project.getFileToEdit() != null) {
                            Project.getFileToEdit().getVertexManager().colourChangeSelection(16, .5f, .5f, .5f, 1f);
                        }
                    }

                    @Override
                    public void widgetDefaultSelected(SelectionEvent e) {
                    }
                });
            }
            {
                Button btn_Pipette = new Button(toolItem_ColourFunctions, SWT.NONE);
                this.btn_Pipette[0] = btn_Pipette;
                btn_Pipette.setToolTipText(I18n.EDITOR3D_Pipette);
                btn_Pipette.setImage(ResourceManager.getImage("icon16_pipette.png")); //$NON-NLS-1$
            }
        }
        {
            ToolItem toolItem_LineThickness = new ToolItem(toolBar, SWT.NONE);
            {
                Button btn_lineSize1 = new Button(toolItem_LineThickness, SWT.TOGGLE);
                this.btn_lineSize1[0] = btn_lineSize1;
                btn_lineSize1.setToolTipText("Line Size 1"); //$NON-NLS-1$ I18N Needs translation!
                btn_lineSize1.setImage(ResourceManager.getImage("icon16_linesize1.png")); //$NON-NLS-1$
            }
            {
                Button btn_lineSize2 = new Button(toolItem_LineThickness, SWT.TOGGLE);
                this.btn_lineSize2[0] = btn_lineSize2;
                btn_lineSize2.setToolTipText("Line Size 2"); //$NON-NLS-1$ I18N Needs translation!
                btn_lineSize2.setImage(ResourceManager.getImage("icon16_linesize2.png")); //$NON-NLS-1$
            }
            {
                Button btn_lineSize3 = new Button(toolItem_LineThickness, SWT.TOGGLE);
                btn_lineSize3.setSelection(true);
                this.btn_lineSize3[0] = btn_lineSize3;
                btn_lineSize3.setToolTipText("Line Size 3"); //$NON-NLS-1$ I18N Needs translation!
                btn_lineSize3.setImage(ResourceManager.getImage("icon16_linesize3.png")); //$NON-NLS-1$
            }
            {
                Button btn_lineSize4 = new Button(toolItem_LineThickness, SWT.TOGGLE);
                this.btn_lineSize4[0] = btn_lineSize4;
                btn_lineSize4.setToolTipText("Line Size 4"); //$NON-NLS-1$ I18N Needs translation!
                btn_lineSize4.setImage(ResourceManager.getImage("icon16_linesize4.png")); //$NON-NLS-1$
            }
        }

        {
            Composite cmp_main_editor = new Composite(container, SWT.BORDER);
            cmp_main_editor.setLayoutData(BorderLayout.CENTER);
            cmp_main_editor.setLayout(new FillLayout(SWT.HORIZONTAL));
            {
                SashForm sashForm = new SashForm(cmp_main_editor, SWT.NONE);
                Editor3DDesign.setSashForm(sashForm);
                sashForm.setToolTipText(I18n.EDITOR3D_DragHint);
                {
                    SashForm sashForm2 = new SashForm(sashForm, SWT.VERTICAL);
                    Composite cmp_Container1 = new Composite(sashForm2, SWT.BORDER);
                    GridLayout gridLayout = new GridLayout(1, true);
                    cmp_Container1.setLayout(gridLayout);
                    {
                        CTabFolder tabFolder_Settings = new CTabFolder(cmp_Container1, SWT.BORDER);
                        this.tabFolder_Settings[0] = tabFolder_Settings;
                        tabFolder_Settings.setMRUVisible(true);
                        tabFolder_Settings.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
                        GridData gridData = new GridData();
                        gridData.horizontalAlignment = SWT.FILL;
                        gridData.minimumHeight = 200;
                        gridData.minimumWidth = 160;
                        gridData.heightHint = 200;

                        gridData.verticalAlignment = SWT.FILL;
                        gridData.grabExcessVerticalSpace = true;

                        gridData.grabExcessHorizontalSpace = true;
                        tabFolder_Settings.setLayoutData(gridData);
                        tabFolder_Settings.setSize(700, 500);

                        CTabItem tItem = new CTabItem(tabFolder_Settings, SWT.NONE);
                        tItem.setText("Snapping:"); //$NON-NLS-1$
                        {
                            final ScrolledComposite cmp_scroll = new ScrolledComposite(tabFolder_Settings, SWT.V_SCROLL | SWT.H_SCROLL);
                            Composite cmp_snappingArea = new Composite(cmp_scroll, SWT.NONE);
                            tItem.setControl(cmp_scroll);
                            cmp_scroll.setContent(cmp_snappingArea);
                            cmp_scroll.setExpandHorizontal(true);
                            cmp_scroll.setExpandVertical(true);

                            cmp_snappingArea.setLayout(new GridLayout(3, false));
                            ((GridLayout) cmp_snappingArea.getLayout()).verticalSpacing = 0;
                            ((GridLayout) cmp_snappingArea.getLayout()).marginHeight = 0;
                            ((GridLayout) cmp_snappingArea.getLayout()).marginWidth = 0;

                            {
                                Composite cmp_dummy = new Composite(cmp_snappingArea, SWT.NONE);
                                cmp_dummy.setLayout(new FillLayout(SWT.HORIZONTAL));

                                Button btnCoarse = new Button(cmp_dummy, SWT.NONE);
                                this.btn_Coarse[0] = btnCoarse;
                                btnCoarse.setImage(ResourceManager.getImage("icon8_coarse.png")); //$NON-NLS-1$
                                btnCoarse.setToolTipText("Coarse"); //$NON-NLS-1$ I18N Needs translation!


                                Button btnMedium = new Button(cmp_dummy, SWT.NONE);
                                this.btn_Medium[0] = btnMedium;
                                btnMedium.setImage(ResourceManager.getImage("icon8_medium.png")); //$NON-NLS-1$
                                btnMedium.setToolTipText("Medium"); //$NON-NLS-1$ I18N Needs translation!

                                Button btnFine = new Button(cmp_dummy, SWT.NONE);
                                this.btn_Fine[0] = btnFine;
                                btnFine.setImage(ResourceManager.getImage("icon8_fine.png")); //$NON-NLS-1$
                                btnFine.setToolTipText("Fine"); //$NON-NLS-1$ I18N Needs translation!

                                cmp_dummy.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 3, 1));
                            }

                            Label lblNewLabel = new Label(cmp_snappingArea, SWT.NONE);
                            lblNewLabel.setText("Move Snap [LDU]:"); //$NON-NLS-1$ I18N Needs translation!
                            lblNewLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));

                            BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_snappingArea, SWT.NONE);
                            this.spn_Move[0] = spinner;
                            spinner.setMaximum(new BigDecimal("100")); //$NON-NLS-1$
                            spinner.setMinimum(new BigDecimal("0.0001")); //$NON-NLS-1$
                            spinner.setValue(WorkbenchManager.getUserSettingState().getMedium_move_snap());
                            spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

                            Label lblNewLabel2 = new Label(cmp_snappingArea, SWT.NONE);
                            lblNewLabel2.setText("Rotate Snap [°]:"); //$NON-NLS-1$ I18N Needs translation!
                            lblNewLabel2.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));

                            BigDecimalSpinner spinner2 = new BigDecimalSpinner(cmp_snappingArea, SWT.NONE);
                            this.spn_Rotate[0] = spinner2;
                            spinner2.setMaximum(new BigDecimal("360.0")); //$NON-NLS-1$
                            spinner2.setMinimum(new BigDecimal("0.0001")); //$NON-NLS-1$
                            spinner2.setValue(WorkbenchManager.getUserSettingState().getMedium_rotate_snap());
                            spinner2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

                            Label lblNewLabel3 = new Label(cmp_snappingArea, SWT.NONE);
                            lblNewLabel3.setText("Scale Snap [%]:"); //$NON-NLS-1$ I18N Needs translation!
                            lblNewLabel3.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));

                            BigDecimalSpinner spinner3 = new BigDecimalSpinner(cmp_snappingArea, SWT.NONE);
                            this.spn_Scale[0] = spinner3;
                            spinner3.setMaximum(new BigDecimal("100.0")); //$NON-NLS-1$
                            spinner3.setMinimum(new BigDecimal("0.01")); //$NON-NLS-1$
                            spinner3.setValue(WorkbenchManager.getUserSettingState().getMedium_scale_snap());
                            spinner3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

                            cmp_scroll.setMinSize(cmp_snappingArea.computeSize(SWT.DEFAULT, SWT.DEFAULT));
                        }

                        CTabItem tItem2 = new CTabItem(tabFolder_Settings, SWT.NONE);
                        tItem2.setText("Selection:"); //$NON-NLS-1$

                        {
                            final ScrolledComposite cmp_scroll = new ScrolledComposite(tabFolder_Settings, SWT.V_SCROLL | SWT.H_SCROLL);
                            Composite cmp_bgArea = new Composite(cmp_scroll, SWT.NONE);
                            tItem2.setControl(cmp_scroll);
                            cmp_scroll.setContent(cmp_bgArea);
                            cmp_scroll.setExpandHorizontal(true);
                            cmp_scroll.setExpandVertical(true);

                            cmp_bgArea.setLayout(new GridLayout(3, false));
                            ((GridLayout) cmp_bgArea.getLayout()).verticalSpacing = 0;
                            ((GridLayout) cmp_bgArea.getLayout()).marginHeight = 0;
                            ((GridLayout) cmp_bgArea.getLayout()).marginWidth = 0;

                            {
                                Composite cmp_Dummy = new Composite(cmp_bgArea, SWT.NONE);
                                cmp_Dummy.setLayout(new FillLayout(SWT.HORIZONTAL));

                                Button btn_PreviousSelection = new Button(cmp_Dummy, SWT.NONE);
                                this.btn_PreviousSelection[0] = btn_PreviousSelection;
                                btn_PreviousSelection.setImage(ResourceManager.getImage("icon8_previous.png")); //$NON-NLS-1$
                                btn_PreviousSelection.setToolTipText("Previous Item"); //$NON-NLS-1$ I18N Needs translation!

                                Button btn_NextSelection = new Button(cmp_Dummy, SWT.NONE);
                                this.btn_NextSelection[0] = btn_NextSelection;
                                btn_NextSelection.setImage(ResourceManager.getImage("icon8_next.png")); //$NON-NLS-1$
                                btn_NextSelection.setToolTipText("Next Item"); //$NON-NLS-1$ I18N Needs translation!

                                cmp_Dummy.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 3, 1));
                            }

                            {
                                Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                                lbl_Label.setText("Line:"); //$NON-NLS-1$ I18N Needs translation!
                                lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Composite cmp_LineSetup = new Composite(cmp_bgArea, SWT.NONE);
                                cmp_LineSetup.setLayout(new GridLayout(1, false));

                                Text txt_Line = new Text(cmp_LineSetup, SWT.BORDER);
                                this.txt_Line[0] = txt_Line;
                                txt_Line.setEnabled(false);
                                txt_Line.setEditable(false);
                                txt_Line.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

                                Button btn_moveAdjacentData2 = new Button(cmp_LineSetup, SWT.TOGGLE);
                                this.btn_MoveAdjacentData2[0] = btn_moveAdjacentData2;
                                btn_moveAdjacentData2.setImage(ResourceManager.getImage("icon16_adjacentmove.png")); //$NON-NLS-1$
                                btn_moveAdjacentData2.setText("Move Adjacent Data"); //$NON-NLS-1$ I18N Needs translation!

                                cmp_LineSetup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lbl_Label = new Label(cmp_bgArea, SWT.SEPARATOR | SWT.HORIZONTAL);
                                lbl_Label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                                this.lbl_SelectionX1[0] = lbl_Label;
                                lbl_Label.setText(I18n.EDITOR3D_PositionX1);
                                lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                                this.spn_SelectionX1[0] = spinner;
                                spinner.setEnabled(false);
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                                this.lbl_SelectionY1[0] = lbl_Label;
                                lbl_Label.setText(I18n.EDITOR3D_PositionY1);
                                lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                                this.spn_SelectionY1[0] = spinner;
                                spinner.setEnabled(false);
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                                this.lbl_SelectionZ1[0] = lbl_Label;
                                lbl_Label.setText(I18n.EDITOR3D_PositionZ1);
                                lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                                this.spn_SelectionZ1[0] = spinner;
                                spinner.setEnabled(false);
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                                this.lbl_SelectionX2[0] = lbl_Label;
                                lbl_Label.setText(I18n.EDITOR3D_PositionX2);
                                lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                                this.spn_SelectionX2[0] = spinner;
                                spinner.setEnabled(false);
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                                this.lbl_SelectionY2[0] = lbl_Label;
                                lbl_Label.setText(I18n.EDITOR3D_PositionY2);
                                lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                                this.spn_SelectionY2[0] = spinner;
                                spinner.setEnabled(false);
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                                this.lbl_SelectionZ2[0] = lbl_Label;
                                lbl_Label.setText(I18n.EDITOR3D_PositionZ2);
                                lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                                this.spn_SelectionZ2[0] = spinner;
                                spinner.setEnabled(false);
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                                this.lbl_SelectionX3[0] = lbl_Label;
                                lbl_Label.setText(I18n.EDITOR3D_PositionX3);
                                lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                                this.spn_SelectionX3[0] = spinner;
                                spinner.setEnabled(false);
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                                this.lbl_SelectionY3[0] = lbl_Label;
                                lbl_Label.setText(I18n.EDITOR3D_PositionY3);
                                lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                                this.spn_SelectionY3[0] = spinner;
                                spinner.setEnabled(false);
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                                this.lbl_SelectionZ3[0] = lbl_Label;
                                lbl_Label.setText(I18n.EDITOR3D_PositionZ3);
                                lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                                this.spn_SelectionZ3[0] = spinner;
                                spinner.setEnabled(false);
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                                this.lbl_SelectionX4[0] = lbl_Label;
                                lbl_Label.setText(I18n.EDITOR3D_PositionX4);
                                lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                                this.spn_SelectionX4[0] = spinner;
                                spinner.setEnabled(false);
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                                this.lbl_SelectionY4[0] = lbl_Label;
                                lbl_Label.setText(I18n.EDITOR3D_PositionY4);
                                lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                                this.spn_SelectionY4[0] = spinner;
                                spinner.setEnabled(false);
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                                this.lbl_SelectionZ4[0] = lbl_Label;
                                lbl_Label.setText(I18n.EDITOR3D_PositionZ4);
                                lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                                this.spn_SelectionZ4[0] = spinner;
                                spinner.setEnabled(false);
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                                this.lbl_SelectionX5[0] = lbl_Label;
                                lbl_Label.setText(I18n.EDITOR3D_PositionX4);
                                lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                                this.spn_SelectionX5[0] = spinner;
                                spinner.setEnabled(false);
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                                this.lbl_SelectionY5[0] = lbl_Label;
                                lbl_Label.setText(I18n.EDITOR3D_PositionY4);
                                lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                                this.spn_SelectionY5[0] = spinner;
                                spinner.setEnabled(false);
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                                this.lbl_SelectionZ5[0] = lbl_Label;
                                lbl_Label.setText(I18n.EDITOR3D_PositionZ4);
                                lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                                this.spn_SelectionZ5[0] = spinner;
                                spinner.setEnabled(false);
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            cmp_scroll.setMinSize(cmp_bgArea.computeSize(SWT.DEFAULT, SWT.DEFAULT));
                        }

                        CTabItem tItem3 = new CTabItem(tabFolder_Settings, SWT.NONE);
                        tItem3.setText("Background Image:"); //$NON-NLS-1$

                        {
                            final ScrolledComposite cmp_scroll = new ScrolledComposite(tabFolder_Settings, SWT.V_SCROLL | SWT.H_SCROLL);
                            Composite cmp_bgArea = new Composite(cmp_scroll, SWT.NONE);
                            tItem3.setControl(cmp_scroll);
                            cmp_scroll.setContent(cmp_bgArea);
                            cmp_scroll.setExpandHorizontal(true);
                            cmp_scroll.setExpandVertical(true);

                            cmp_bgArea.setLayout(new GridLayout(3, false));
                            ((GridLayout) cmp_bgArea.getLayout()).verticalSpacing = 0;
                            ((GridLayout) cmp_bgArea.getLayout()).marginHeight = 0;
                            ((GridLayout) cmp_bgArea.getLayout()).marginWidth = 0;

                            {
                                Composite cmp_dummy = new Composite(cmp_bgArea, SWT.NONE);
                                cmp_dummy.setLayout(new FillLayout(SWT.HORIZONTAL));

                                Button btnPrevious = new Button(cmp_dummy, SWT.NONE);
                                btn_PngPrevious[0] = btnPrevious;
                                btnPrevious.setImage(ResourceManager.getImage("icon8_previous.png")); //$NON-NLS-1$
                                btnPrevious.setToolTipText("Previous"); //$NON-NLS-1$ I18N Needs translation!

                                Button btnFocusBG = new Button(cmp_dummy, SWT.NONE);
                                btn_PngFocus[0] = btnFocusBG;
                                btnFocusBG.setImage(ResourceManager.getImage("icon8_focus.png")); //$NON-NLS-1$
                                btnFocusBG.setToolTipText("Focus"); //$NON-NLS-1$ I18N Needs translation!

                                Button btnNext = new Button(cmp_dummy, SWT.NONE);
                                btn_PngNext[0] = btnNext;
                                btnNext.setImage(ResourceManager.getImage("icon8_next.png")); //$NON-NLS-1$
                                btnNext.setToolTipText("Next"); //$NON-NLS-1$ I18N Needs translation!

                                cmp_dummy.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 3, 1));
                            }

                            {
                                Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                                lbl_Label.setText("Image:"); //$NON-NLS-1$ I18N Needs translation!
                                lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Composite cmp_pathChooser1 = new Composite(cmp_bgArea, SWT.NONE);
                                cmp_pathChooser1.setLayout(new GridLayout(2, false));

                                Text txt_pngPath = new Text(cmp_pathChooser1, SWT.BORDER);
                                this.txt_PngPath[0] = txt_pngPath;
                                txt_pngPath.setEditable(false);
                                txt_pngPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

                                Button btn_BrowsePngPath = new Button(cmp_pathChooser1, SWT.NONE);
                                btn_PngImage[0] = btn_BrowsePngPath;
                                btn_BrowsePngPath.setText(I18n.DIALOG_Browse);

                                cmp_pathChooser1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                                lbl_Label.setText("Position X [LDU]:"); //$NON-NLS-1$ I18N Needs translation!
                                lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                                spn_PngX[0] = spinner;
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                                lbl_Label.setText("Position Y [LDU]:"); //$NON-NLS-1$ I18N Needs translation!
                                lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                                spn_PngY[0] = spinner;
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                                lbl_Label.setText("Position Z [LDU]:"); //$NON-NLS-1$ I18N Needs translation!
                                lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                                spn_PngZ[0] = spinner;
                                spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                                lbl_Label.setText("Angle Y [°]:"); //$NON-NLS-1$ I18N Needs translation!
                                lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                                spn_PngA1[0] = spinner;
                                spinner.setMaximum(new BigDecimal("360")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-360")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                                lbl_Label.setText("Angle X [°]:"); //$NON-NLS-1$ I18N Needs translation!
                                lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                                spn_PngA2[0] = spinner;
                                spinner.setMaximum(new BigDecimal("360")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-360")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                                lbl_Label.setText("Angle Z [°]:"); //$NON-NLS-1$ I18N Needs translation!
                                lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                                spn_PngA3[0] = spinner;
                                spinner.setMaximum(new BigDecimal("360")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-360")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                                lbl_Label.setText("Scale X:"); //$NON-NLS-1$ I18N Needs translation!
                                lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                                spn_PngSX[0] = spinner;
                                spinner.setMaximum(new BigDecimal("1E6")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E6")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            {
                                Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                                lbl_Label.setText("Scale Y:"); //$NON-NLS-1$ I18N Needs translation!
                                lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                                BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                                spn_PngSY[0] = spinner;
                                spinner.setMaximum(new BigDecimal("1E6")); //$NON-NLS-1$
                                spinner.setMinimum(new BigDecimal("-1E6")); //$NON-NLS-1$
                                spinner.setValue(new BigDecimal(0));
                                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                            }
                            cmp_scroll.setMinSize(cmp_bgArea.computeSize(SWT.DEFAULT, SWT.DEFAULT));
                        }

                        tabFolder_Settings.setSelection(tItem);
                    }
                    Composite cmp_Container2 = new Composite(sashForm2, SWT.BORDER);
                    GridLayout gridLayout3 = new GridLayout(1, true);
                    cmp_Container2.setLayout(gridLayout3);
                    {
                        Tree treeAllParts = new Tree(cmp_Container2, SWT.BORDER, 1); // 4096);
                        this.treeParts[0] = treeAllParts;
                        TreeItem treeItemProjectName = new TreeItem(treeAllParts, SWT.NONE);
                        this.treeItem_Project[0] = treeItemProjectName;
                        treeItemProjectName.setText(I18n.PROJECT_NewProject);
                        TreeItem treeItemProjectParts = new TreeItem(treeItemProjectName, SWT.NONE);
                        this.treeItem_ProjectParts[0] = treeItemProjectParts;
                        treeItemProjectParts.setText(I18n.PARTS_Parts);

                        TreeItem treeItemNewPart = new TreeItem(treeItemProjectParts, SWT.NONE);
                        treeItemNewPart.setData(Project.getFileToEdit());
                        Project.addUnsavedFile(Project.getFileToEdit());


                        if (NLogger.DEBUG) {
                            try {
                                UTF8BufferedReader reader = new UTF8BufferedReader("testsource.txt"); //$NON-NLS-1$
                                StringBuilder sb = new StringBuilder();
                                String s;
                                while ((s = reader.readLine()) != null) {
                                    sb.append(s);
                                    sb.append(StringHelper.getLineDelimiter());
                                }
                                Project.getFileToEdit().setText(sb.toString());
                            } catch (Exception e) {

                            }
                        } else {
                            Project.getFileToEdit().setText("0 "); //$NON-NLS-1$
                        }

                        TreeItem treeItemProjectSubparts = new TreeItem(treeItemProjectName, SWT.NONE);
                        this.treeItem_ProjectSubparts[0] = treeItemProjectSubparts;
                        treeItemProjectSubparts.setText(I18n.PARTS_Subparts);
                        TreeItem treeItemProjectPrimitives = new TreeItem(treeItemProjectName, SWT.NONE);
                        this.treeItem_ProjectPrimitives[0] = treeItemProjectPrimitives;
                        treeItemProjectPrimitives.setText(I18n.PARTS_Primitives);
                        TreeItem treeItemProjectHiResPrimitives = new TreeItem(treeItemProjectName, SWT.NONE);
                        this.treeItem_ProjectPrimitives48[0] = treeItemProjectHiResPrimitives;
                        treeItemProjectHiResPrimitives.setText(I18n.PARTS_HiResPrimitives);
                        TreeItem treeItemProjectLowResPrimitives = new TreeItem(treeItemProjectName, SWT.NONE);
                        this.treeItem_ProjectPrimitives8[0] = treeItemProjectLowResPrimitives;
                        treeItemProjectLowResPrimitives.setText(I18n.PARTS_LowResPrimitives);

                        TreeItem treeItemUnsaved = new TreeItem(treeAllParts, SWT.NONE);
                        this.treeItem_Unsaved[0] = treeItemUnsaved;
                        treeItemUnsaved.setText("Unsaved Files"); //$NON-NLS-1$ I18N
                        treeItemUnsaved.setVisible(false);
                        TreeItem treeItemNewPart2 = new TreeItem(treeItem_Unsaved[0], SWT.NONE);
                        treeItemNewPart2.setData(Project.getFileToEdit());
                        treeItemNewPart2.setText(Project.getFileToEdit().getShortName());

                        TreeItem treeItemUnofficial = new TreeItem(treeAllParts, SWT.NONE);
                        this.treeItem_Unofficial[0] = treeItemUnofficial;
                        treeItemUnofficial.setText(I18n.PROJECT_UnofficialLibReadWrite);
                        treeItemUnofficial.setVisible(false);
                        TreeItem treeItemUnofficialParts = new TreeItem(treeItemUnofficial, SWT.NONE);
                        this.treeItem_UnofficialParts[0] = treeItemUnofficialParts;
                        treeItemUnofficialParts.setText(I18n.PARTS_Parts);
                        treeItemUnofficialParts.setVisible(false);
                        TreeItem treeItemUnofficialSubparts = new TreeItem(treeItemUnofficial, SWT.NONE);
                        this.treeItem_UnofficialSubparts[0] = treeItemUnofficialSubparts;
                        treeItemUnofficialSubparts.setText(I18n.PARTS_Subparts);
                        treeItemUnofficialSubparts.setVisible(false);
                        TreeItem treeItemUnofficialPrimitives = new TreeItem(treeItemUnofficial, SWT.NONE);
                        this.treeItem_UnofficialPrimitives[0] = treeItemUnofficialPrimitives;
                        treeItemUnofficialPrimitives.setText(I18n.PARTS_Primitives);
                        treeItemUnofficialPrimitives.setVisible(false);
                        TreeItem treeItemUnofficialHiResPrimitives = new TreeItem(treeItemUnofficial, SWT.NONE);
                        this.treeItem_UnofficialPrimitives48[0] = treeItemUnofficialHiResPrimitives;
                        treeItemUnofficialHiResPrimitives.setText(I18n.PARTS_HiResPrimitives);
                        treeItemUnofficialHiResPrimitives.setVisible(false);
                        TreeItem treeItemUnofficialLowResPrimitives = new TreeItem(treeItemUnofficial, SWT.NONE);
                        this.treeItem_UnofficialPrimitives8[0] = treeItemUnofficialLowResPrimitives;
                        treeItemUnofficialLowResPrimitives.setText(I18n.PARTS_LowResPrimitives);
                        treeItemUnofficialLowResPrimitives.setVisible(false);

                        TreeItem treeItemOfficial = new TreeItem(treeAllParts, SWT.NONE);
                        this.treeItem_Official[0] = treeItemOfficial;
                        treeItemOfficial.setText(I18n.PROJECT_OfficialLibRead);
                        treeItemOfficial.setVisible(false);
                        TreeItem treeItemOfficialParts = new TreeItem(treeItemOfficial, SWT.NONE);
                        this.treeItem_OfficialParts[0] = treeItemOfficialParts;
                        treeItemOfficialParts.setText(I18n.PARTS_Parts);
                        treeItemOfficialParts.setVisible(false);
                        TreeItem treeItemOfficialSubparts = new TreeItem(treeItemOfficial, SWT.NONE);
                        this.treeItem_OfficialSubparts[0] = treeItemOfficialSubparts;
                        treeItemOfficialSubparts.setText(I18n.PARTS_Subparts);
                        treeItemOfficialSubparts.setVisible(false);
                        TreeItem treeItemOfficialPrimitives = new TreeItem(treeItemOfficial, SWT.NONE);
                        this.treeItem_OfficialPrimitives[0] = treeItemOfficialPrimitives;
                        treeItemOfficialPrimitives.setText(I18n.PARTS_Primitives);
                        treeItemOfficialPrimitives.setVisible(false);
                        TreeItem treeItemOfficialHiResPrimitives = new TreeItem(treeItemOfficial, SWT.NONE);
                        this.treeItem_OfficialPrimitives48[0] = treeItemOfficialHiResPrimitives;
                        treeItemOfficialHiResPrimitives.setText(I18n.PARTS_HiResPrimitives);
                        treeItemOfficialHiResPrimitives.setVisible(false);
                        TreeItem treeItemOfficialLowResPrimitives = new TreeItem(treeItemOfficial, SWT.NONE);
                        this.treeItem_OfficialPrimitives8[0] = treeItemOfficialLowResPrimitives;
                        treeItemOfficialLowResPrimitives.setText(I18n.PARTS_LowResPrimitives);
                        treeItemOfficialLowResPrimitives.setVisible(false);

                        GridData gridData = new GridData();
                        gridData.horizontalAlignment = SWT.FILL;
                        gridData.verticalAlignment = SWT.FILL;
                        gridData.grabExcessVerticalSpace = true;
                        gridData.grabExcessHorizontalSpace = true;
                        treeAllParts.setLayoutData(gridData);
                    }
                    {
                        Composite cmp_Search = new Composite(cmp_Container2, SWT.NONE);
                        GridData gridData = new GridData();
                        gridData.horizontalAlignment = SWT.FILL;
                        gridData.grabExcessHorizontalSpace = true;
                        cmp_Search.setLayoutData(gridData);
                        GridLayout gridLayout2 = new GridLayout(2, false);
                        cmp_Search.setLayout(gridLayout2);
                        Text txt_Search = new Text(cmp_Search, SWT.BORDER);
                        this.txt_Search[0] = txt_Search;
                        txt_Search.setMessage("Search"); //$NON-NLS-1$ I18N Needs translation!
                        GridData gridData2 = new GridData();
                        gridData2.horizontalAlignment = SWT.FILL;
                        gridData2.grabExcessHorizontalSpace = true;
                        txt_Search.setLayoutData(gridData2);
                        Button btn_ResetSearch = new Button(cmp_Search, SWT.NONE);
                        this.btn_ResetSearch[0] = btn_ResetSearch;
                        btn_ResetSearch.setText("Reset"); //$NON-NLS-1$ I18N Needs translation!
                    }

                    Composite cmp_Container4 = new Composite(sashForm2, SWT.BORDER);
                    GridLayout gridLayout4 = new GridLayout(1, true);
                    cmp_Container4.setLayout(gridLayout4);

                    CompositePrimitive cmp_Primitives = new CompositePrimitive(cmp_Container4);
                    this.cmp_Primitives[0] = cmp_Primitives;

                    Matrix4f[] primitiveViewport = windowState.getPrimitiveViewport();
                    if (primitiveViewport != null) {
                        cmp_Primitives.setViewport2(primitiveViewport);
                    }

                    cmp_Primitives.setLayout(new FillLayout());

                    GridData gd = new GridData();
                    gd.grabExcessHorizontalSpace = true;
                    gd.grabExcessVerticalSpace = true;
                    gd.horizontalAlignment = SWT.FILL;
                    gd.verticalAlignment = SWT.FILL;
                    cmp_Primitives.setLayoutData(gd);

                    cmp_Primitives.loadPrimitives();

                    Label lbl_selectedPrimitiveItem = new Label(cmp_Container4, SWT.NONE);
                    this.lbl_selectedPrimitiveItem[0] = lbl_selectedPrimitiveItem;
                    {
                        GridData gd2 = new GridData();
                        gd2.grabExcessHorizontalSpace = true;
                        gd2.horizontalAlignment = SWT.FILL;
                        lbl_selectedPrimitiveItem.setLayoutData(gd2);
                    }
                    lbl_selectedPrimitiveItem.setText("(no primitive selected)");//$NON-NLS-1$ I18N Needs translation!

                    {
                        Composite cmp_Search = new Composite(cmp_Container4, SWT.NONE);
                        GridData gridData = new GridData();
                        gridData.horizontalAlignment = SWT.FILL;
                        gridData.grabExcessHorizontalSpace = true;
                        cmp_Search.setLayoutData(gridData);
                        GridLayout gridLayout2 = new GridLayout(2, false);
                        cmp_Search.setLayout(gridLayout2);
                        Text txt_Search = new Text(cmp_Search, SWT.BORDER);
                        this.txt_primitiveSearch[0] = txt_Search;
                        txt_Search.setMessage("Search Primitives"); //$NON-NLS-1$ I18N Needs translation!
                        GridData gridData2 = new GridData();
                        gridData2.horizontalAlignment = SWT.FILL;
                        gridData2.grabExcessHorizontalSpace = true;
                        txt_Search.setLayoutData(gridData2);
                        Button btn_ResetSearch = new Button(cmp_Search, SWT.NONE);
                        this.btn_resetPrimitiveSearch[0] = btn_ResetSearch;
                        btn_ResetSearch.setText("Reset"); //$NON-NLS-1$ I18N Needs translation!
                    }

                    int[] weights = windowState.getLeftSashWeights();
                    if (weights != null) {
                        sashForm2.setWeights(weights);
                    }
                }

                ArrayList<Composite3DState> threeDconfig = windowState.getThreeDwindowConfig();
                if (threeDconfig == null) {
                    @SuppressWarnings("unused")
                    CompositeContainer cmp_Container = new CompositeContainer(sashForm, false);
                    // cmp_Container.getComposite3D().getMntmBottom().setSelection(true);
                    // cmp_Container.getComposite3D().getPerspectiveCalculator().setPerspective(Perspective.BACK);
                    // cmp_Container.getComposite3D().getModifier().splitViewHorizontally();
                } else {
                    final int configSize = threeDconfig.size();
                    if (configSize < 2) {
                        if (configSize == 1) {
                            Composite3DState state = threeDconfig.get(0);
                            createComposite3D(sashForm, null, state);
                        } else {
                            @SuppressWarnings("unused")
                            CompositeContainer cmp_Container = new CompositeContainer(sashForm, false);
                        }
                    } else {
                        // MARK Load the configuration of multiple 3D windows
                        Collections.sort(threeDconfig, new Comparator<Composite3DState>(
                                ) {
                            @Override
                            public int compare(Composite3DState o1, Composite3DState o2) {
                                final int cmp = Integer.compare(o1.getPath().length(), o2.getPath().length());
                                if (cmp == 0) {
                                    return o1.getPath().compareTo(o2.getPath());
                                }
                                return cmp;
                            }
                        });
                        CompositeContainer cmp_Container = new CompositeContainer(sashForm, false);

                        HashSet<String> splitCandidate = new HashSet<String>();
                        HashSet<String> splitAlready = new HashSet<String>();
                        HashMap<String, CompositeContainer> cmpMap = new HashMap<String, CompositeContainer>();
                        HashMap<String, Composite3DState> sMap = new HashMap<String, Composite3DState>();

                        splitCandidate.add("|"); //$NON-NLS-1$
                        splitAlready.add("|"); //$NON-NLS-1$
                        {
                            SashForm sf;
                            if (!threeDconfig.get(0).isVertical()) {
                                sf = cmp_Container.getComposite3D().getModifier().splitViewVertically();
                            } else {
                                sf = cmp_Container.getComposite3D().getModifier().splitViewHorizontally();
                            }
                            sf.setWeights(threeDconfig.get(0).getWeights());
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
                                    sf.setWeights(state2.getWeights());
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
        status = new Composite(container, SWT.NONE);
        status.setLayoutData(BorderLayout.SOUTH);
        RowLayout rl_statusBar = new RowLayout(SWT.HORIZONTAL);
        rl_statusBar.center = true;
        status.setLayout(rl_statusBar);
        {
            Label lbl_Status = new Label(status, SWT.NONE);
            lbl_Status.setText(""); //$NON-NLS-1$
        }
        return container;
    }

    private void createComposite3D(SashForm sashForm, CompositeContainer c, Composite3DState state) {
        // Load the configuration of one 3D window
        final Composite3D c3d;
        if (c == null) {
            final CompositeContainer cmp_Container = new CompositeContainer(sashForm, state.hasScales());
            c3d = cmp_Container.getComposite3D();
        } else {
            c3d = c.getComposite3D();
        }
        final int renderMode = state.getRenderMode();
        final int lineMode = state.getLineMode();
        final Perspective perspective = state.getPerspective();
        c3d.setGrid_scale(state.getGridScale());
        c3d.getPerspectiveCalculator().setPerspective(perspective);
        c3d.setOriginShown(state.isShowOrigin());
        c3d.setShowingLabels(state.isShowLabel());
        c3d.setGridShown(state.isShowGrid());
        c3d.setLightOn(state.isLights());
        c3d.setMeshLines(state.isMeshlines());
        c3d.setSubMeshLines(state.isSubfileMeshlines());
        c3d.setShowingVertices(state.isVertices());
        c3d.setShowingHiddenVertices(state.isHiddenVertices());
        c3d.setShowingLogo(state.isStudLogo());
        c3d.setLineMode(lineMode);
        c3d.setBlackEdges(state.isAlwaysBlackLines());
        c3d.setShowingAxis(state.isShowAxis());
        c3d.setAnaglyph3d(state.isAnaglyph3d());
        c3d.setRenderMode(renderMode);

        c3d.getMntmFront().setSelection(perspective == Perspective.FRONT);
        c3d.getMntmBack().setSelection(perspective == Perspective.BACK);
        c3d.getMntmLeft().setSelection(perspective == Perspective.LEFT);
        c3d.getMntmRight().setSelection(perspective == Perspective.RIGHT);
        c3d.getMntmTop().setSelection(perspective == Perspective.TOP);
        c3d.getMntmBottom().setSelection(perspective == Perspective.BOTTOM);
        c3d.getMntmRealPreview().setSelection(lineMode == 0);
        c3d.getMntmShowAll().setSelection(lineMode == 1);
        c3d.getMntmStdLines().setSelection(lineMode == 2);
        c3d.getMntmHideAll().setSelection(lineMode == 4);
        c3d.getMntmAlwaysBlack().setSelection(state.isAlwaysBlackLines());
        c3d.getMntmShowOrigin().setSelection(state.isShowOrigin());
        c3d.getMntmLabel().setSelection(state.isShowLabel());
        c3d.getMntmShowGrid().setSelection(state.isShowGrid());
        c3d.getMntmShowScale().setSelection(state.hasScales());
        c3d.getMntmSwitchLights().setSelection(state.isLights());
        c3d.getMntmMeshLines().setSelection(state.isMeshlines());
        c3d.getMntmSubMeshLines().setSelection(state.isSubfileMeshlines());
        c3d.getMntmVertices().setSelection(state.isVertices());
        c3d.getMntmHiddenVertices().setSelection(state.isHiddenVertices());
        c3d.getMntmStudLogo().setSelection(state.isStudLogo());
        c3d.getMntmAxis().setSelection(state.isShowAxis());
        c3d.getMntmAnaglyph().setSelection(state.isAnaglyph3d());
        c3d.getMntmNoBFC().setSelection(renderMode == 0);
        c3d.getMntmRandomColours().setSelection(renderMode == 1);
        c3d.getMntmBFCFrontBack().setSelection(renderMode == 2);
        c3d.getMntmBFCBack().setSelection(renderMode == 3);
        c3d.getMntmBFCReal().setSelection(renderMode == 4);
        c3d.getMntmBFCTextured().setSelection(renderMode == 5);
        c3d.getMntmCondlineMode().setSelection(renderMode == 6);
    }

    private void addColorButton(ToolItem toolItem_Colours, GColour gColour, final int index) {
        int cn = gColour.getColourNumber();
        if (cn != -1 && View.hasLDConfigColour(cn)) {
            gColour = View.getLDConfigColour(cn);
        }

        final int imgSize;
        switch (Editor3DWindow.getIconsize()) {
        case 0:
            imgSize = 16;
            break;
        case 1:
            imgSize = 24;
            break;
        case 2:
            imgSize = 32;
            break;
        case 3:
            imgSize = 48;
            break;
        case 4:
            imgSize = 64;
            break;
        case 5:
            imgSize = 72;
            break;
        default:
            imgSize = 16;
            break;
        }

        final GColour[] gColour2 = new GColour[] { gColour };
        final Color[] col = new Color[1];
        col[0] = SWTResourceManager.getColor((int) (gColour2[0].getR() * 255f), (int) (gColour2[0].getG() * 255f), (int) (gColour2[0].getB() * 255f));

        final Button btn_Col = new Button(toolItem_Colours, SWT.NONE);
        btn_Col.setData(gColour2);
        int num = gColour2[0].getColourNumber();
        if (!View.hasLDConfigColour(num)) {
            num = -1;
        }
        if (num != -1) {
            btn_Col.setToolTipText("Colour [" + num + "]: " + View.getLDConfigColourName(num)); //$NON-NLS-1$ //$NON-NLS-2$ I18N
        } else {
            StringBuilder colourBuilder = new StringBuilder();
            colourBuilder.append("0x2"); //$NON-NLS-1$
            colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getR())).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getG())).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getB())).toUpperCase());
            btn_Col.setToolTipText("Colour [" + colourBuilder.toString() + "]"); //$NON-NLS-1$ //$NON-NLS-2$ I18N
        }

        btn_Col.setImage(ResourceManager.getImage("icon16_fullTransparent.png")); //$NON-NLS-1$

        btn_Col.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if ((e.stateMask & SWT.CTRL) == SWT.CTRL) {
                    // Choose new colour
                    new ColourDialog(getShell(), gColour2).open();
                    WorkbenchManager.getUserSettingState().getUserPalette().set(index, gColour2[0]);
                    col[0] = SWTResourceManager.getColor((int) (gColour2[0].getR() * 255f), (int) (gColour2[0].getG() * 255f), (int) (gColour2[0].getB() * 255f));
                    int num = gColour2[0].getColourNumber();
                    if (View.hasLDConfigColour(num)) {
                        gColour2[0] = View.getLDConfigColour(num);
                    } else {
                        num = -1;
                    }
                    if (num != -1) {
                        btn_Col.setToolTipText("Colour [" + num + "]: " + View.getLDConfigColourName(num)); //$NON-NLS-1$ //$NON-NLS-2$ I18N
                    } else {
                        StringBuilder colourBuilder = new StringBuilder();
                        colourBuilder.append("0x2"); //$NON-NLS-1$
                        colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getR())).toUpperCase());
                        colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getG())).toUpperCase());
                        colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getB())).toUpperCase());
                        btn_Col.setToolTipText("Colour [" + colourBuilder.toString() + "]"); //$NON-NLS-1$ //$NON-NLS-2$ I18N
                    }
                } else {
                    int num = gColour2[0].getColourNumber();
                    if (View.hasLDConfigColour(num)) {
                        gColour2[0] = View.getLDConfigColour(num);
                    } else {
                        num = -1;
                    }
                    if (Project.getFileToEdit() != null) {
                        Project.getFileToEdit().getVertexManager().colourChangeSelection(num, gColour2[0].getR(), gColour2[0].getG(), gColour2[0].getB(), gColour2[0].getA());
                    }
                    Editor3DWindow.getWindow().setLastUsedColour(gColour2[0]);
                    btn_LastUsedColour[0].removeListener(SWT.Paint, btn_LastUsedColour[0].getListeners(SWT.Paint)[0]);
                    btn_LastUsedColour[0].removeListener(SWT.Selection, btn_LastUsedColour[0].getListeners(SWT.Selection)[0]);
                    final Color col = SWTResourceManager.getColor((int) (gColour2[0].getR() * 255f), (int) (gColour2[0].getG() * 255f), (int) (gColour2[0].getB() * 255f));
                    final Point size = btn_LastUsedColour[0].computeSize(SWT.DEFAULT, SWT.DEFAULT);
                    final int x = Math.round(size.x / 5f);
                    final int y = Math.round(size.y / 5f);
                    final int w = Math.round(size.x * (3f / 5f));
                    final int h = Math.round(size.y * (3f / 5f));
                    btn_LastUsedColour[0].addPaintListener(new PaintListener() {
                        @Override
                        public void paintControl(PaintEvent e) {
                            e.gc.setBackground(col);
                            e.gc.fillRectangle(x, y, w, h);
                            if (gColour2[0].getA() == 1f) {
                                e.gc.drawImage(ResourceManager.getImage("icon16_transparent.png"), 0, 0, imgSize, imgSize, x, y, w, h); //$NON-NLS-1$
                            } else {
                                e.gc.drawImage(ResourceManager.getImage("icon16_halftrans.png"), 0, 0, imgSize, imgSize, x, y, w, h); //$NON-NLS-1$
                            }
                        }
                    });
                    btn_LastUsedColour[0].addSelectionListener(new SelectionListener() {
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            if (Project.getFileToEdit() != null) {
                                Editor3DWindow.getWindow().setLastUsedColour(gColour2[0]);
                                int num = gColour2[0].getColourNumber();
                                if (View.hasLDConfigColour(num)) {
                                    gColour2[0] = View.getLDConfigColour(num);
                                } else {
                                    num = -1;
                                }
                                Project.getFileToEdit().getVertexManager().colourChangeSelection(num, gColour2[0].getR(), gColour2[0].getG(), gColour2[0].getB(), gColour2[0].getA());
                            }
                        }

                        @Override
                        public void widgetDefaultSelected(SelectionEvent e) {
                        }
                    });
                    if (num != -1) {
                        btn_LastUsedColour[0].setToolTipText("Colour [" + num + "]: " + View.getLDConfigColourName(num)); //$NON-NLS-1$ //$NON-NLS-2$ I18N
                    } else {
                        StringBuilder colourBuilder = new StringBuilder();
                        colourBuilder.append("0x2"); //$NON-NLS-1$
                        colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getR())).toUpperCase());
                        colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getG())).toUpperCase());
                        colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getB())).toUpperCase());
                        btn_LastUsedColour[0].setToolTipText("Colour [" + colourBuilder.toString() + "]"); //$NON-NLS-1$ //$NON-NLS-2$ I18N
                    }
                    btn_LastUsedColour[0].redraw();
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        final Point size = btn_Col.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        final int x = Math.round(size.x / 5f);
        final int y = Math.round(size.y / 5f);
        final int w = Math.round(size.x * (3f / 5f));
        final int h = Math.round(size.y * (3f / 5f));
        btn_Col.addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                e.gc.setBackground(col[0]);
                e.gc.fillRectangle(x, y, w, h);
                if (gColour2[0].getA() == 1f) {
                    e.gc.drawImage(ResourceManager.getImage("icon16_transparent.png"), 0, 0, imgSize, imgSize, x, y, w, h); //$NON-NLS-1$
                } else {
                    e.gc.drawImage(ResourceManager.getImage("icon16_halftrans.png"), 0, 0, imgSize, imgSize, x, y, w, h); //$NON-NLS-1$
                }
            }
        });
    }

    /**
     * Create the menu manager.
     *
     * @return the menu manager
     */
    @Override
    protected MenuManager createMenuManager() {
        MenuManager menuManager = new MenuManager("menu"); //$NON-NLS-1$
        // menuManager.setVisible(true);
        // {
        // MenuManager mnu_File = new MenuManager(I18n.EDITOR3D_File);
        // mnu_File.setVisible(true);
        // menuManager.add(mnu_File);
        // this.mnu_File[0] = mnu_File;
        // }
        return menuManager;
    }

    /**
     * Create the status line manager.
     *
     * @return the status line manager
     */
    @Override
    protected StatusLineManager createStatusLineManager() {
        StatusLineManager status = new StatusLineManager();
        return status;
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

    public static void setSashForm(SashForm sashForm) {
        Editor3DDesign.sashForm = sashForm;
    }

    void showSelectMenu() {
        Point loc = btn_Select2[0].getLocation();
        Rectangle rect = btn_Select2[0].getBounds();
        Point mLoc = new Point(loc.x - 1, loc.y + rect.height);
        mnu_Select.setLocation(getShell().getDisplay().map(btn_Select2[0].getParent(), null, mLoc));
        mnu_Select.setVisible(true);
    }

    public static int getIconsize() {
        return iconSize;
    }
}