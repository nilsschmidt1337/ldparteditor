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
package org.nschmidt.ldparteditor.shell.editor3d;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.eclipse.swt.widgets.Text;
import org.lwjgl.util.vector.Matrix4f;
import org.nschmidt.ldparteditor.composite.Composite3D;
import org.nschmidt.ldparteditor.composite.CompositeContainer;
import org.nschmidt.ldparteditor.composite.ToolItem;
import org.nschmidt.ldparteditor.composite.ToolItemDrawLocation;
import org.nschmidt.ldparteditor.composite.ToolItemDrawMode;
import org.nschmidt.ldparteditor.composite.ToolItemState;
import org.nschmidt.ldparteditor.composite.primitive.CompositePrimitive;
import org.nschmidt.ldparteditor.enumtype.MyLanguage;
import org.nschmidt.ldparteditor.enumtype.OpenInWhat;
import org.nschmidt.ldparteditor.enumtype.Task;
import org.nschmidt.ldparteditor.enumtype.Threshold;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.helper.Cocoa;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.resource.ResourceManager;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.AddToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.ColourFunctionsToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.ColourToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.CopyPasteToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.HideUnhideToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.LineThicknessToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.ManipulatorToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.MiscToggleToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.MiscToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.PerspectiveToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.RenderModeToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.TransformationModeToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.UndoRedoToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.WorkingTypeToolItem;
import org.nschmidt.ldparteditor.shell.editortext.EditorTextWindow;
import org.nschmidt.ldparteditor.state.KeyStateManager;
import org.nschmidt.ldparteditor.text.LDParsingException;
import org.nschmidt.ldparteditor.text.StringHelper;
import org.nschmidt.ldparteditor.text.UTF8BufferedReader;
import org.nschmidt.ldparteditor.widget.BigDecimalSpinner;
import org.nschmidt.ldparteditor.widget.IntegerSpinner;
import org.nschmidt.ldparteditor.widget.NButton;
import org.nschmidt.ldparteditor.widget.Tree;
import org.nschmidt.ldparteditor.widget.TreeItem;
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
    protected static Composite status;

    final NButton[] btnSyncPtr = new NButton[1];
    final NButton[] btnLastOpenPtr = new NButton[1];
    final NButton[] btnNewPtr = new NButton[1];
    final NButton[] btnOpenPtr = new NButton[1];
    final NButton[] btnSavePtr = new NButton[1];
    final NButton[] btnSaveAllPtr = new NButton[1];

    final NButton[] btnLocalPtr = new NButton[1];
    final NButton[] btnGlobalPtr = new NButton[1];

    final NButton[] btnInsertAtCursorPositionPtr = new NButton[1];

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

    protected static SashForm sashForm;
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

        Set<String> missingItemsToCreate = new HashSet<>();
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
                lastToolItem = createToolItemWorkingType(s.getDrawLocation(), s.getDrawMode()); // OBJECT_MODE
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
        if (missingItemsToCreate.contains("OBJECT_MODE")) lastToolItem = createToolItemWorkingType(ToolItemDrawLocation.NORTH, ToolItemDrawMode.HORIZONTAL); // OBJECT_MODE //$NON-NLS-1$
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

                SashForm sashFormCmpMain = new SashForm(cmpMainEditor, Cocoa.getStyle());

                {
                    GridData gridDataX = new GridData();

                    gridDataX.horizontalSpan = 2;

                    gridDataX.horizontalAlignment = SWT.FILL;
                    gridDataX.minimumHeight = 200;
                    gridDataX.minimumWidth = 160;

                    gridDataX.verticalAlignment = SWT.FILL;
                    gridDataX.grabExcessVerticalSpace = true;

                    gridDataX.grabExcessHorizontalSpace = true;
                    sashFormCmpMain.setLayoutData(gridDataX);
                }

                Editor3DDesign.setSashForm(sashFormCmpMain);
                sashFormCmpMain.setToolTipText(I18n.E3D_DRAG_HINT);
                {
                    SashForm sashForm2 = new SashForm(sashFormCmpMain, SWT.VERTICAL);
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
                                formatter.setLocale(MyLanguage.getLocale());
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
                                formatter.setLocale(MyLanguage.getLocale());
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
                            try (UTF8BufferedReader reader = new UTF8BufferedReader("testsource.txt")) { //$NON-NLS-1$
                                StringBuilder sb = new StringBuilder();
                                String s;
                                while ((s = reader.readLine()) != null) {
                                    sb.append(s);
                                    sb.append(StringHelper.getLineDelimiter());
                                }
                                Project.getFileToEdit().setText(sb.toString());
                            } catch (LDParsingException | FileNotFoundException e) {
                                NLogger.error(Editor3DDesign.class, e);
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

                List<Composite3DState> threeDconfig = windowState.getThreeDwindowConfig();
                if (threeDconfig == null) {
                    new CompositeContainer(sashFormCmpMain, false);
                } else {
                    final int configSize = threeDconfig.size();
                    if (configSize < 2) {
                        if (configSize == 1) {
                            Composite3DState state = threeDconfig.get(0);
                            createComposite3D(sashFormCmpMain, null, state);
                        } else {
                            new CompositeContainer(sashFormCmpMain, false);
                        }
                    } else {
                        // MARK Load the configuration of multiple 3D windows
                        applyC3DStatesOnStartup(threeDconfig);
                    }
                }

                int width = windowState.getWindowState().getSizeAndPosition().width;
                int[] sashSize = windowState.getLeftSashWidth();
                if (sashSize == null) {
                    sashFormCmpMain.setWeights(new int[] { width / 3, width - width / 3 });
                } else {
                    try {
                        sashFormCmpMain.setWeights(sashSize);
                    } catch (IllegalArgumentException iae) {
                        sashFormCmpMain.setWeights(new int[] { width / 3, width - width / 3 });
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
        final Composite target = areaFromLocation(location);
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
        return new PerspectiveToolItem(areaFromLocation(location), Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL);
    }

    private ToolItem createToolItemRenderMode(ToolItemDrawLocation location, ToolItemDrawMode mode) {
        return new RenderModeToolItem(areaFromLocation(location), Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL);
    }

    private ToolItem createToolItemLineThickness(ToolItemDrawLocation location, ToolItemDrawMode mode) {
        return new LineThicknessToolItem(areaFromLocation(location), Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL);
    }

    private ToolItem createToolItemColours(ToolItemDrawLocation location, ToolItemDrawMode mode) {
        final Composite target = areaFromLocation(location);
        new ColourToolItem(target, Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL);
        return new ColourFunctionsToolItem(target, Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL);
    }

    private Composite areaFromLocation(ToolItemDrawLocation location) {
        final Composite target;
        switch (location) {
        case EAST:
            target = cmpEast;
            break;
        case WEST:
            target = cmpWest;
            break;
        case NORTH:
        default:
            target = cmpNorth;
            break;
        }
        return target;
    }

    private ToolItem createToolItemAdd(ToolItemDrawLocation location, ToolItemDrawMode mode) {
        return new AddToolItem(areaFromLocation(location), Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL);
    }

    private ToolItem createToolItemInsertAtCursorPosition(ToolItemDrawLocation location, ToolItemDrawMode mode) {
        final Composite target = areaFromLocation(location);
        ToolItem toolItemInsertAtCursorPosition = new ToolItem(target, Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL);
        NButton btnInsertAtCursorPosition = new NButton(toolItemInsertAtCursorPosition, SWT.TOGGLE | Cocoa.getStyle());
        this.btnInsertAtCursorPositionPtr[0] = btnInsertAtCursorPosition;
        KeyStateManager.addTooltipText(btnInsertAtCursorPosition, I18n.E3D_INSERT_AT_CURSOR_POSITION, Task.INSERT_AT_CURSOR);
        btnInsertAtCursorPosition.setImage(ResourceManager.getImage("icon16_insertAtCursor.png")); //$NON-NLS-1$
        return toolItemInsertAtCursorPosition;
    }

    private ToolItem createToolItemWorkingType(ToolItemDrawLocation location, ToolItemDrawMode mode) {
        return new WorkingTypeToolItem(areaFromLocation(location), Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL);
    }

    private ToolItem createToolItemCCPD(ToolItemDrawLocation location, ToolItemDrawMode mode) {
        return new CopyPasteToolItem(areaFromLocation(location), Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL);
    }

    private ToolItem createToolItemMiscClick() {
        return new MiscToolItem(cmpNorth, Cocoa.getStyle(), true);
    }

    private ToolItem createToolItemManipulatorActions(ToolItemDrawLocation location, ToolItemDrawMode mode, ToolItem toolItem) {
        return ManipulatorToolItem.create(areaFromLocation(location), toolItem, Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL, mode == ToolItemDrawMode.DROP_DOWN);
    }

    private ToolItem createToolItemManipulatorMode(ToolItemDrawLocation location, ToolItemDrawMode mode) {
        final Composite target = areaFromLocation(location);
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
        return new TransformationModeToolItem(areaFromLocation(location), Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL);
    }

    private ToolItem createToolItemUndoRedo(ToolItemDrawLocation location, ToolItemDrawMode mode) {
        return new UndoRedoToolItem(areaFromLocation(location), Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL);
    }

    private ToolItem createToolItemMiscToggle(ToolItemDrawLocation location, ToolItemDrawMode mode) {
        return new MiscToggleToolItem(areaFromLocation(location), Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL);
    }

    private ToolItem createToolItemHideUnhide(ToolItemDrawLocation location, ToolItemDrawMode mode) {
        return new HideUnhideToolItem(areaFromLocation(location), Cocoa.getStyle(), mode == ToolItemDrawMode.HORIZONTAL);
    }

    private ToolItem createToolItemNewOpenDat(ToolItemDrawLocation location, ToolItemDrawMode mode) {
        final Composite target = areaFromLocation(location);
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
        final Composite target = areaFromLocation(location);
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
        final Composite target = areaFromLocation(location);
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
     * Return the initial size of the window.
     */
    @Override
    protected Point getInitialSize() {
        return new Point(916, 578);
    }

    public SashForm getSplitSashForm() {
        return splitSash[0];
    }

    private static void setSashForm(SashForm sashForm) {
        Editor3DDesign.sashForm = sashForm;
    }

    public void reloadC3DStates(List<Composite3DState> threeDconfig) {
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
                Set<String> splitCandidate = new HashSet<>();
                Set<String> splitAlready = new HashSet<>();
                Map<String, CompositeContainer> cmpMap = new HashMap<>();
                Map<String, Composite3DState> sMap = new HashMap<>();

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

    private void applyC3DStatesOnStartup(List<Composite3DState> threeDconfig) {

        Collections.sort(threeDconfig, (o1, o2) -> {
            final int cmp = Integer.compare(o1.getPath().length(), o2.getPath().length());
            if (cmp == 0) {
                return o1.getPath().compareTo(o2.getPath());
            }
            return cmp;
        });
        CompositeContainer cmpContainer = new CompositeContainer(sashForm, false);
        cmpContainer.moveBelow(sashForm.getChildren()[0]);

        Set<String> splitCandidate = new HashSet<>();
        Set<String> splitAlready = new HashSet<>();
        Map<String, CompositeContainer> cmpMap = new HashMap<>();
        Map<String, Composite3DState> sMap = new HashMap<>();

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