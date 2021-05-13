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

import static org.nschmidt.ldparteditor.helper.WidgetUtility.widgetUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CTabFolder2Listener;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.lwjgl.opengl.GL;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.csg.CSG;
import org.nschmidt.ldparteditor.composite.Composite3D;
import org.nschmidt.ldparteditor.composite.CompositeContainer;
import org.nschmidt.ldparteditor.composite.CompositeScale;
import org.nschmidt.ldparteditor.composite.ToolItem;
import org.nschmidt.ldparteditor.composite.ToolSeparator;
import org.nschmidt.ldparteditor.composite.compositetab.CompositeTab;
import org.nschmidt.ldparteditor.composite.compositetab.CompositeTabFolder;
import org.nschmidt.ldparteditor.composite.primitive.CompositePrimitive;
import org.nschmidt.ldparteditor.data.BFC;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.DatType;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GData0;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.GDataBFC;
import org.nschmidt.ldparteditor.data.GDataCSG;
import org.nschmidt.ldparteditor.data.GDataPNG;
import org.nschmidt.ldparteditor.data.GraphicalDataTools;
import org.nschmidt.ldparteditor.data.LibraryManager;
import org.nschmidt.ldparteditor.data.Matrix;
import org.nschmidt.ldparteditor.data.Primitive;
import org.nschmidt.ldparteditor.data.ProtractorHelper;
import org.nschmidt.ldparteditor.data.ReferenceParser;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.dialog.colour.ColourDialog;
import org.nschmidt.ldparteditor.dialog.copy.CopyDialog;
import org.nschmidt.ldparteditor.dialog.newproject.NewProjectDialog;
import org.nschmidt.ldparteditor.enumtype.GL20Primitives;
import org.nschmidt.ldparteditor.enumtype.IconSize;
import org.nschmidt.ldparteditor.enumtype.LDConfig;
import org.nschmidt.ldparteditor.enumtype.ManipulatorAxisMode;
import org.nschmidt.ldparteditor.enumtype.ManipulatorScope;
import org.nschmidt.ldparteditor.enumtype.MouseButton;
import org.nschmidt.ldparteditor.enumtype.MyLanguage;
import org.nschmidt.ldparteditor.enumtype.ObjectMode;
import org.nschmidt.ldparteditor.enumtype.OpenInWhat;
import org.nschmidt.ldparteditor.enumtype.Perspective;
import org.nschmidt.ldparteditor.enumtype.SnapSize;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.enumtype.WorkingMode;
import org.nschmidt.ldparteditor.helper.Cocoa;
import org.nschmidt.ldparteditor.helper.LDPartEditorException;
import org.nschmidt.ldparteditor.helper.Manipulator;
import org.nschmidt.ldparteditor.helper.ShellHelper;
import org.nschmidt.ldparteditor.helper.SphereGL20;
import org.nschmidt.ldparteditor.helper.Version;
import org.nschmidt.ldparteditor.helper.WidgetSelectionHelper;
import org.nschmidt.ldparteditor.helper.WidgetSelectionListener;
import org.nschmidt.ldparteditor.helper.composite3d.GuiStatusManager;
import org.nschmidt.ldparteditor.helper.composite3d.SelectorSettings;
import org.nschmidt.ldparteditor.helper.composite3d.TreeData;
import org.nschmidt.ldparteditor.helper.composite3d.ViewIdleManager;
import org.nschmidt.ldparteditor.helper.compositetext.ProjectActions;
import org.nschmidt.ldparteditor.helper.compositetext.SubfileCompiler;
import org.nschmidt.ldparteditor.helper.math.MathHelper;
import org.nschmidt.ldparteditor.helper.math.MatrixOperations;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.resource.ResourceManager;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.AddToolItem;
import org.nschmidt.ldparteditor.shell.editortext.EditorTextWindow;
import org.nschmidt.ldparteditor.shell.searchnreplace.SearchWindow;
import org.nschmidt.ldparteditor.text.LDParsingException;
import org.nschmidt.ldparteditor.text.References;
import org.nschmidt.ldparteditor.text.UTF8BufferedReader;
import org.nschmidt.ldparteditor.vertexwindow.VertexWindow;
import org.nschmidt.ldparteditor.widget.DecimalValueChangeAdapter;
import org.nschmidt.ldparteditor.widget.NButton;
import org.nschmidt.ldparteditor.widget.TreeItem;
import org.nschmidt.ldparteditor.win32openwith.TryToOpen;
import org.nschmidt.ldparteditor.workbench.Composite3DState;
import org.nschmidt.ldparteditor.workbench.Editor3DWindowState;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * The 3D editor window
 * <p>
 * Note: This class should be instantiated once, it defines all listeners and
 * part of the business logic.
 *
 * @author nils
 *
 */
public class Editor3DWindow extends Editor3DDesign {

    /** The reference to this window */
    private static Editor3DWindow window;

    /** The search window */
    private SearchWindow searchWindow;

    /** The vertex window */
    private final VertexWindow vertexWindow = new VertexWindow();

    public static final List<GLCanvas> canvasList = new ArrayList<>();
    public static final List<OpenGLRenderer> renders = new ArrayList<>();

    public static int sashWeight1 = 50;
    public static int sashWeight2 = 50;

    private static final AtomicBoolean alive = new AtomicBoolean(true);
    private static final AtomicBoolean no_sync_deadlock = new AtomicBoolean(false);

    private boolean movingAdjacentData = WorkbenchManager.getUserSettingState().isMovingAdjacentData();
    private boolean noTransparentSelection = false;
    private boolean bfcToggle = false;
    private boolean insertingAtCursorPosition = false;
    private boolean reviewingAPart = false;
    private ObjectMode workingType = ObjectMode.VERTICES;
    private WorkingMode workingAction = WorkingMode.SELECT;
    private ManipulatorAxisMode workingLayer = ManipulatorAxisMode.NONE;

    private GColour lastUsedColour = LDConfig.getColour16();

    private ManipulatorScope transformationMode = ManipulatorScope.LOCAL;

    private SnapSize snapSize = SnapSize.MEDIUM;

    private boolean updatingPngPictureTab;
    private int pngPictureUpdateCounter = 0;

    private boolean updatingSelectionTab = true;

    private List<String> recentItems = new ArrayList<>();

    private Map<DatFile, Map<Composite3D, org.nschmidt.ldparteditor.composite.Composite3DViewState>> c3dStates = new HashMap<>();

    /**
     * Create the application window.
     */
    public Editor3DWindow() {
        super();
        final int[] i = new int[1];
        final int[] j = new int[1];
        final GLCanvas[] first1 = ViewIdleManager.firstCanvas;
        final OpenGLRenderer[] first2 = ViewIdleManager.firstRender;
        final int[] intervall = new int[] { 10 };
        Display.getCurrent().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (ViewIdleManager.pause[0].get()) {
                    ViewIdleManager.pause[0].set(false);
                    intervall[0] = 500;
                } else {
                    final int cs = canvasList.size();
                    if (i[0] < cs && cs > 0) {
                        GLCanvas canvas;
                        if (!canvasList.get(i[0]).equals(first1[0])) {
                            canvas = first1[0];
                            if (canvas != null && !canvas.isDisposed()) {
                                first2[0].drawScene();
                                first1[0] = null;
                                first2[0] = null;
                            }
                        }
                        canvas = canvasList.get(i[0]);
                        if (!canvas.isDisposed()) {
                            boolean stdMode = ViewIdleManager.renderLDrawStandard[0].get();
                            // FIXME Needs workaround since SWT upgrade to 4.5!
                            if (renders.get(i[0]).getC3D().getRenderMode() != 5 || cs == 1 || stdMode) {
                                renders.get(i[0]).drawScene();
                                if (stdMode) {
                                    j[0]++;
                                }
                            }
                        } else {
                            canvasList.remove(i[0]);
                            renders.remove(i[0]);
                        }
                        i[0]++;
                    } else {
                        i[0] = 0;
                        if (j[0] > cs) {
                            j[0] = 0;
                            ViewIdleManager.renderLDrawStandard[0].set(false);
                        }
                    }
                }
                Display.getCurrent().timerExec(intervall[0], this);
                intervall[0] = 10;
            }
        });
    }

    /**
     * Run a fresh instance of this window
     */
    public void run() {
        window = this;
        // Load colours
        WorkbenchManager.getUserSettingState().loadColours();
        LDConfig.overrideColour16();
        // Load recent files
        recentItems = WorkbenchManager.getUserSettingState().getRecentItems();
        if (recentItems == null) recentItems = new ArrayList<>();
        // Adjust the last visited path according to what was last opened (and exists on the harddrive)
        {
            final int rc = recentItems.size() - 1;
            boolean foundPath = false;
            for (int i = rc; i > -1; i--) {
                final String path = recentItems.get(i);
                final File f = new File(path);
                if (f.exists()) {
                    if (f.isFile() && f.getParentFile() != null) {
                        Project.setLastVisitedPath(f.getParentFile().getAbsolutePath());
                        foundPath = true;
                        break;
                    } else if (f.isDirectory()) {
                        Project.setLastVisitedPath(path);
                        foundPath = true;
                        break;
                    }
                }
            }
            if (!foundPath) {
                final File f = new File(WorkbenchManager.getUserSettingState().getAuthoringFolderPath());
                if (f.exists() && f.isDirectory()) {
                    Project.setLastVisitedPath(WorkbenchManager.getUserSettingState().getAuthoringFolderPath());
                }
            }
        }
        // Closing this window causes the whole application to quit
        this.setBlockOnOpen(true);
        // Creating the window to get the shell
        this.create();
        final Shell sh = this.getShell();
        sh.setText(Version.getApplicationName() + " " + Version.getVersion() + " (" + WorkbenchManager.getUserSettingState().getOpenGLVersionString() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        sh.setImage(ResourceManager.getImage("imgDuke2.png")); //$NON-NLS-1$
        sh.setMinimumSize(640, 480);
        sh.setBounds(WorkbenchManager.getEditor3DWindowState().getWindowState().getSizeAndPosition());
        if (WorkbenchManager.getEditor3DWindowState().getWindowState().isCentered()) {
            ShellHelper.centerShellOnPrimaryScreen(sh);
        }
        // Maximize / tab creation on text editor has to be called asynchronously
        sh.getDisplay().asyncExec(() -> {
            sh.setMaximized(WorkbenchManager.getEditor3DWindowState().getWindowState().isMaximized());

            if (WorkbenchManager.getUserSettingState().getTextWinArr() != TEXT_3D_SEPARATE) {
                for (EditorTextWindow w : Project.getOpenTextWindows()) {
                    if (!w.isSeperateWindow()) {
                        Project.getParsedFiles().add(Project.getFileToEdit());
                        Project.addOpenedFile(Project.getFileToEdit());
                        {
                            CompositeTab tbtmnewItem = new CompositeTab(w.getTabFolder(), SWT.CLOSE);
                            tbtmnewItem.setFolderAndWindow(w.getTabFolder(), Editor3DWindow.getWindow());
                            tbtmnewItem.getState().setFileNameObj(Project.getFileToEdit());
                            w.getTabFolder().setSelection(tbtmnewItem);
                            tbtmnewItem.parseForErrorAndHints();
                            tbtmnewItem.getTextComposite().redraw();
                        }
                        break;
                    }
                }
            }
        });
        // Set the snapping
        Manipulator.setSnap(
                WorkbenchManager.getUserSettingState().getMediumMoveSnap(),
                WorkbenchManager.getUserSettingState().getMediumRotateSnap(),
                WorkbenchManager.getUserSettingState().getMediumScaleSnap()
                );
        // MARK All final listeners will be configured here..
        NLogger.writeVersion();
        sh.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent consumed) {
                // Implementation is not required.
            }

            @Override
            public void focusGained(FocusEvent e) {
                regainFocus();
                VertexWindow.placeVertexWindow();
            }
        });
        sh.addShellListener(new ShellListener() {
            @Override
            public void shellIconified(ShellEvent consumed) {
                // Implementation is not required.
            }

            @Override
            public void shellDeiconified(ShellEvent consumed) {
                // Implementation is not required.
            }

            @Override
            public void shellDeactivated(ShellEvent e) {
                Display.getDefault().timerExec(1000, () -> {
                    if (Display.getDefault().getActiveShell() == null) {
                        vertexWindow.close();
                    }
                });
            }

            @Override
            public void shellClosed(ShellEvent consumed) {
                // Implementation is not required.
            }

            @Override
            public void shellActivated(ShellEvent e) {
                VertexWindow.placeVertexWindow();
            }
        });
        sh.addListener(SWT.Move, event ->
            VertexWindow.placeVertexWindow()
        );
        sh.addListener(SWT.Resize, event ->
            VertexWindow.placeVertexWindow()
        );
        widgetUtil(tabFolderSettingsPtr[0]).addSelectionListener(e ->  regainFocus());

        WidgetSelectionListener sa = e -> {
            int[] w = leftSash[0].getWeights();
            if (w[0] == 800 && w[1] == 100 && w[2] == 100) {
                leftSash[0].setWeights(new int[]{33, 33, 33});
            } else {
                leftSash[0].setWeights(new int[]{80, 10, 10});
            }
        };

        widgetUtil(btnShowUpper1Ptr[0]).addSelectionListener(sa);
        widgetUtil(btnShowUpper2Ptr[0]).addSelectionListener(sa);
        widgetUtil(btnShowUpper3Ptr[0]).addSelectionListener(sa);

        widgetUtil(btnShowMiddlePtr[0]).addSelectionListener(e -> {
            int[] w = leftSash[0].getWeights();
            if (w[0] == 100 && w[1] == 800 && w[2] == 100) {
                leftSash[0].setWeights(new int[]{33, 33, 33});
            } else {
                leftSash[0].setWeights(new int[]{10, 80, 10});
            }
        });

        widgetUtil(btnSameHeightPtr[0]).addSelectionListener(e ->
            leftSash[0].setWeights(new int[]{33, 33, 33})
        );

        widgetUtil(btnShowLowerPtr[0]).addSelectionListener(e -> {
            int[] w = leftSash[0].getWeights();
            if (w[0] == 100 && w[1] == 100 && w[2] == 800) {
                leftSash[0].setWeights(new int[]{33, 33, 33});
            } else {
                leftSash[0].setWeights(new int[]{10, 10, 80});
            }
        });

        if (btnShowLeftPtr[0] != null) widgetUtil(btnShowLeftPtr[0]).addSelectionListener(e -> {
            final SashForm sf = splitSash[0];
            int[] w = sf.getWeights();
            if (w[1] * 9 > w[0]) {
                sf.setWeights(new int[]{95, 5});
            } else {
                sf.setWeights(new int[]{Editor3DWindow.sashWeight1, Editor3DWindow.sashWeight2});
            }
        });

        if (btnShowRightPtr[0] != null) widgetUtil(btnShowRightPtr[0]).addSelectionListener(e -> {
            final SashForm sf = splitSash[0];
            int[] w = sf.getWeights();
            if (w[0] * 9 > w[1]) {
                sf.setWeights(new int[]{5, 95});
            } else {
                sf.setWeights(new int[]{Editor3DWindow.sashWeight1, Editor3DWindow.sashWeight2});
            }
        });

        if (btnSameWidthPtr[0] != null) widgetUtil(btnSameWidthPtr[0]).addSelectionListener(e -> splitSash[0].setWeights(new int[]{50, 50}));

        if (splitSash[0] != null) splitSash[0].getChildren()[0].addControlListener(new ControlListener() {
            @Override
            public void controlResized(ControlEvent e) {
                int[] w = splitSash[0].getWeights();
                if (w[0] * 9 > w[1] && w[1] * 9 > w[0]) {
                    Editor3DWindow.sashWeight1 = w[0];
                    Editor3DWindow.sashWeight2 = w[1];
                }
            }
            @Override
            public void controlMoved(ControlEvent e) {
                // Implementation is not required.
            }
        });


        widgetUtil(btnSyncPtr[0]).addSelectionListener(e -> {

            resetSearch();
            int[][] stats = new int[15][3];
            stats[0] = LibraryManager.syncProjectElements(treeItemProjectPtr[0]);
            stats[5] = LibraryManager.syncUnofficialParts(treeItemUnofficialPartsPtr[0]);
            stats[6] = LibraryManager.syncUnofficialSubparts(treeItemUnofficialSubpartsPtr[0]);
            stats[7] = LibraryManager.syncUnofficialPrimitives(treeItemUnofficialPrimitivesPtr[0]);
            stats[8] = LibraryManager.syncUnofficialHiResPrimitives(treeItemUnofficialPrimitives48Ptr[0]);
            stats[9] = LibraryManager.syncUnofficialLowResPrimitives(treeItemUnofficialPrimitives8Ptr[0]);
            stats[10] = LibraryManager.syncOfficialParts(treeItemOfficialPartsPtr[0]);
            stats[11] = LibraryManager.syncOfficialSubparts(treeItemOfficialSubpartsPtr[0]);
            stats[12] = LibraryManager.syncOfficialPrimitives(treeItemOfficialPrimitivesPtr[0]);
            stats[13] = LibraryManager.syncOfficialHiResPrimitives(treeItemOfficialPrimitives48Ptr[0]);
            stats[14] = LibraryManager.syncOfficialLowResPrimitives(treeItemOfficialPrimitives8Ptr[0]);

            int additions = 0;
            int deletions = 0;
            int conflicts = 0;
            for (int[] folderStat : stats) {
                additions += folderStat[0];
                deletions += folderStat[1];
                conflicts += folderStat[2];
            }

            txtSearchPtr[0].setText(" "); //$NON-NLS-1$
            txtSearchPtr[0].setText(""); //$NON-NLS-1$

            Set<DatFile> dfs = new HashSet<>();
            for (OpenGLRenderer renderer : renders) {
                dfs.add(renderer.getC3D().getLockableDatFileReference());
            }
            for (EditorTextWindow w1 : Project.getOpenTextWindows()) {
                for (CTabItem t1 : w1.getTabFolder().getItems()) {
                    DatFile txtDat1 = ((CompositeTab) t1).getState().getFileNameObj();
                    if (txtDat1 != null) {
                        dfs.add(txtDat1);
                    }
                }
            }
            for (DatFile df : dfs) {
                SubfileCompiler.compile(df, false, false);
            }
            for (EditorTextWindow w2 : Project.getOpenTextWindows()) {
                for (CTabItem t2 : w2.getTabFolder().getItems()) {
                    DatFile txtDat2 = ((CompositeTab) t2).getState().getFileNameObj();
                    if (txtDat2 != null) {
                        ((CompositeTab) t2).parseForErrorAndHints();
                        ((CompositeTab) t2).getTextComposite().redraw();

                        ((CompositeTab) t2).getState().getTab().setText(((CompositeTab) t2).getState().getFilenameWithStar());
                    }
                }
            }

            updateTreeUnsavedEntries();
            treeParts[0].getTree().showItem(treeParts[0].getTree().getItem(0));

            MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
            messageBox.setText(I18n.DIALOG_SYNC_TITLE);

            Object[] messageArguments = {additions, deletions, conflicts};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.getLocale());
            formatter.applyPattern(I18n.DIALOG_SYNC);
            messageBox.setMessage(formatter.format(messageArguments));

            messageBox.open();
            regainFocus();
        });

        widgetUtil(btnLastOpenPtr[0]).addSelectionListener(e -> {

            Menu lastOpenedMenu = new Menu(treeParts[0].getTree());
            btnLastOpenPtr[0].setMenu(lastOpenedMenu);

            final int size = recentItems.size() - 1;
            for (int i = size; i > -1; i--) {
                final String path = recentItems.get(i);
                File f = new File(path);
                if (f.exists() && f.canRead()) {
                    if (f.isFile()) {
                        MenuItem mntmItem1 = new MenuItem(lastOpenedMenu, I18n.noBiDirectionalTextStyle());
                        mntmItem1.setEnabled(true);
                        mntmItem1.setText(path);
                        widgetUtil(mntmItem1).addSelectionListener(e11 -> {
                            File f11 = new File(path);
                            if (f11.exists() && f11.isFile() && f11.canRead()) {
                                DatFile df = openDatFile(OpenInWhat.EDITOR_3D, path, false);
                                if (df != null && !df.equals(View.DUMMY_DATFILE) && WorkbenchManager.getUserSettingState().isSyncingTabs()) {
                                    boolean fileIsOpenInTextEditor = false;
                                    for (EditorTextWindow w : Project.getOpenTextWindows()) {
                                        for (CTabItem t : w.getTabFolder().getItems()) {
                                            if (df.equals(((CompositeTab) t).getState().getFileNameObj())) {
                                                fileIsOpenInTextEditor = true;
                                            }
                                            if (fileIsOpenInTextEditor) break;
                                        }
                                        if (fileIsOpenInTextEditor) break;
                                    }
                                    if (Project.getOpenTextWindows().isEmpty() || fileIsOpenInTextEditor) {
                                        openDatFile(df, OpenInWhat.EDITOR_TEXT, null);
                                    } else {
                                        Project.getOpenTextWindows().iterator().next().openNewDatFileTab(df, true);
                                    }
                                }
                                cleanupClosedData();
                                regainFocus();
                            }
                        });
                    } else if (f.isDirectory()) {
                        MenuItem mntmItem2 = new MenuItem(lastOpenedMenu, I18n.noBiDirectionalTextStyle());
                        mntmItem2.setEnabled(true);

                        Object[] messageArguments = {path};
                        MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                        formatter.setLocale(MyLanguage.getLocale());
                        formatter.applyPattern(I18n.E3D_LAST_PROJECT);

                        mntmItem2.setText(formatter.format(messageArguments));
                        widgetUtil(mntmItem2).addSelectionListener(e12 -> {
                            File f12 = new File(path);
                            if (f12.exists() && f12.isDirectory() && f12.canRead() && ProjectActions.openProject(path)) {
                                Project.create(false);
                                treeItemProjectPtr[0].setData(Project.getProjectPath());
                                resetSearch();
                                LibraryManager.readProjectPartsParent(treeItemProjectPartsPtr[0]);
                                LibraryManager.readProjectParts(treeItemProjectPartsPtr[0]);
                                LibraryManager.readProjectSubparts(treeItemProjectSubpartsPtr[0]);
                                LibraryManager.readProjectPrimitives(treeItemProjectPrimitivesPtr[0]);
                                LibraryManager.readProjectHiResPrimitives(treeItemProjectPrimitives48Ptr[0]);
                                LibraryManager.readProjectLowResPrimitives(treeItemProjectPrimitives8Ptr[0]);
                                treeItemOfficialPartsPtr[0].setData(null);
                                txtSearchPtr[0].setText(" "); //$NON-NLS-1$
                                txtSearchPtr[0].setText(""); //$NON-NLS-1$
                                updateTreeUnsavedEntries();
                            }
                            regainFocus();
                        });
                    }
                }
            }

            java.awt.Point b = java.awt.MouseInfo.getPointerInfo().getLocation();
            final int x = (int) b.getX();
            final int y = (int) b.getY();

            lastOpenedMenu.setLocation(x, y);
            lastOpenedMenu.setVisible(true);
            regainFocus();
        });
        widgetUtil(btnNewPtr[0]).addSelectionListener(e -> {
            if (ProjectActions.createNewProject(Editor3DWindow.getWindow(), false)) {
                addRecentFile(Project.getProjectPath());
            }
            regainFocus();
        });
        widgetUtil(btnOpenPtr[0]).addSelectionListener(e -> {
            if (ProjectActions.openProject(null)) {
                addRecentFile(Project.getProjectPath());
                Project.setLastVisitedPath(Project.getProjectPath());
                Project.create(false);
                treeItemProjectPtr[0].setData(Project.getProjectPath());
                resetSearch();
                LibraryManager.readProjectPartsParent(treeItemProjectPartsPtr[0]);
                LibraryManager.readProjectParts(treeItemProjectPartsPtr[0]);
                LibraryManager.readProjectSubparts(treeItemProjectSubpartsPtr[0]);
                LibraryManager.readProjectPrimitives(treeItemProjectPrimitivesPtr[0]);
                LibraryManager.readProjectHiResPrimitives(treeItemProjectPrimitives48Ptr[0]);
                LibraryManager.readProjectLowResPrimitives(treeItemProjectPrimitives8Ptr[0]);
                treeItemOfficialPartsPtr[0].setData(null);
                txtSearchPtr[0].setText(" "); //$NON-NLS-1$
                txtSearchPtr[0].setText(""); //$NON-NLS-1$
                updateTreeUnsavedEntries();
            }
            regainFocus();
        });
        widgetUtil(btnSavePtr[0]).addSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (treeParts[0].getSelectionCount() == 1) {
                    if (treeParts[0].getSelection()[0].getData() instanceof DatFile) {
                        DatFile df = (DatFile) treeParts[0].getSelection()[0].getData();
                        if (!df.isReadOnly() && Project.getUnsavedFiles().contains(df)) {
                            if (df.save()) {
                                Editor3DWindow.getWindow().addRecentFile(df);
                                Editor3DWindow.getWindow().updateTreeUnsavedEntries();
                            } else {
                                MessageBox messageBoxError = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                                messageBoxError.setText(I18n.DIALOG_ERROR);
                                messageBoxError.setMessage(I18n.DIALOG_CANT_SAVE_FILE);
                                messageBoxError.open();
                                Editor3DWindow.getWindow().updateTreeUnsavedEntries();
                            }
                        }
                    } else if (treeParts[0].getSelection()[0].getData() instanceof ArrayList<?>) {
                        NLogger.debug(getClass(), "Saving all files from this group"); //$NON-NLS-1$
                        {
                            @SuppressWarnings("unchecked")
                            List<DatFile> dfs = (List<DatFile>) treeParts[0].getSelection()[0].getData();
                            for (DatFile df : dfs) {
                                if (!df.isReadOnly() && Project.getUnsavedFiles().contains(df)) {
                                    if (df.save()) {
                                        Editor3DWindow.getWindow().addRecentFile(df);
                                        Project.removeUnsavedFile(df);
                                        Editor3DWindow.getWindow().updateTreeUnsavedEntries();
                                    } else {
                                        MessageBox messageBoxError = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                                        messageBoxError.setText(I18n.DIALOG_ERROR);
                                        messageBoxError.setMessage(I18n.DIALOG_CANT_SAVE_FILE);
                                        messageBoxError.open();
                                        Editor3DWindow.getWindow().updateTreeUnsavedEntries();
                                    }
                                }
                            }
                        }
                    } else if (treeParts[0].getSelection()[0].getData() instanceof String) {
                        if (treeParts[0].getSelection()[0].equals(treeItemProjectPtr[0])) {
                            NLogger.debug(getClass(), "Save the project..."); //$NON-NLS-1$
                            if (Project.isDefaultProject() && ProjectActions.createNewProject(Editor3DWindow.getWindow(), true)) {
                                Project.setLastVisitedPath(Project.getProjectPath());
                            }
                            iterateOverItems(treeItemProjectPartsPtr[0]);
                            iterateOverItems(treeItemProjectSubpartsPtr[0]);
                            iterateOverItems(treeItemProjectPrimitivesPtr[0]);
                            iterateOverItems(treeItemProjectPrimitives48Ptr[0]);
                            iterateOverItems(treeItemProjectPrimitives8Ptr[0]);
                        } else if (treeParts[0].getSelection()[0].equals(treeItemUnofficialPtr[0])) {
                            iterateOverItems(treeItemUnofficialPartsPtr[0]);
                            iterateOverItems(treeItemUnofficialSubpartsPtr[0]);
                            iterateOverItems(treeItemUnofficialPrimitivesPtr[0]);
                            iterateOverItems(treeItemUnofficialPrimitives48Ptr[0]);
                            iterateOverItems(treeItemUnofficialPrimitives8Ptr[0]);
                        }
                        NLogger.debug(getClass(), "Saving all files from this group to {0}", treeParts[0].getSelection()[0].getData()); //$NON-NLS-1$
                    }
                } else {
                    NLogger.debug(getClass(), "Save the project..."); //$NON-NLS-1$
                    if (Project.isDefaultProject() && ProjectActions.createNewProject(Editor3DWindow.getWindow(), true)) {
                        Project.setLastVisitedPath(Project.getProjectPath());
                    }
                }
                regainFocus();
            }

            private void iterateOverItems(TreeItem ti) {
                {
                    @SuppressWarnings("unchecked")
                    List<DatFile> dfs = (List<DatFile>) ti.getData();
                    for (DatFile df : dfs) {
                        if (!df.isReadOnly() && Project.getUnsavedFiles().contains(df)) {
                            if (df.save()) {
                                Editor3DWindow.getWindow().addRecentFile(df);
                                Project.removeUnsavedFile(df);
                                Editor3DWindow.getWindow().updateTreeUnsavedEntries();
                            } else {
                                MessageBox messageBoxError = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                                messageBoxError.setText(I18n.DIALOG_ERROR);
                                messageBoxError.setMessage(I18n.DIALOG_CANT_SAVE_FILE);
                                messageBoxError.open();
                                Editor3DWindow.getWindow().updateTreeUnsavedEntries();
                            }
                        }
                    }
                }
            }
        });
        widgetUtil(btnSaveAllPtr[0]).addSelectionListener(e -> {
            Set<DatFile> dfs = new HashSet<>(Project.getUnsavedFiles());
            for (DatFile df : dfs) {
                if (!df.isReadOnly()) {
                    if (df.save()) {
                        Editor3DWindow.getWindow().addRecentFile(df);
                        Project.removeUnsavedFile(df);
                    } else {
                        MessageBox messageBoxError = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                        messageBoxError.setText(I18n.DIALOG_ERROR);
                        messageBoxError.setMessage(I18n.DIALOG_CANT_SAVE_FILE);
                        messageBoxError.open();
                    }
                }
            }
            if (Project.isDefaultProject() && ProjectActions.createNewProject(getWindow(), true)) {
                addRecentFile(Project.getProjectPath());
            }
            Editor3DWindow.getWindow().updateTreeUnsavedEntries();
            regainFocus();
        });

        widgetUtil(btnNewDatPtr[0]).addSelectionListener(e -> {
            DatFile dat = createNewDatFile(getShell(), OpenInWhat.EDITOR_TEXT_AND_3D);
            if (dat != null) {
                addRecentFile(dat);
                final File f = new File(dat.getNewName());
                if (f.getParentFile() != null) {
                    Project.setLastVisitedPath(f.getParentFile().getAbsolutePath());
                }
            }
            regainFocus();
        });

        widgetUtil(btnOpenDatPtr[0]).addSelectionListener(e -> {
            boolean tabSync = WorkbenchManager.getUserSettingState().isSyncingTabs();
            WorkbenchManager.getUserSettingState().setSyncingTabs(false);

            FileDialog fd = new FileDialog(btnOpenPtr[0].getShell(), SWT.MULTI);
            fd.setText(I18n.E3D_OPEN_DAT_FILE);

            fd.setFilterPath(Project.getLastVisitedPath());

            String[] filterExt = { "*.dat", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
            fd.setFilterExtensions(filterExt);
            String[] filterNames = {I18n.E3D_LDRAW_SOURCE_FILE, I18n.E3D_ALL_FILES};
            fd.setFilterNames(filterNames);

            String selected = fd.open();
            if (selected == null) {
                return;
            }

            for (String fileName : fd.getFileNames()) {
                final String filePath = fd.getFilterPath() + File.separator + fileName;
                DatFile dat = openDatFile(OpenInWhat.EDITOR_3D, filePath, true);
                if (dat != null) {
                    addRecentFile(dat);
                    final File f = new File(dat.getNewName());
                    if (f.getParentFile() != null) {
                        Project.setLastVisitedPath(f.getParentFile().getAbsolutePath());
                    }
                    boolean fileIsOpenInTextEditor = false;
                    for (EditorTextWindow w : Project.getOpenTextWindows()) {
                        for (CTabItem t : w.getTabFolder().getItems()) {
                            if (dat.equals(((CompositeTab) t).getState().getFileNameObj())) {
                                fileIsOpenInTextEditor = true;
                            }
                            if (fileIsOpenInTextEditor) break;
                        }
                        if (fileIsOpenInTextEditor) break;
                    }
                    if (Project.getOpenTextWindows().isEmpty() || fileIsOpenInTextEditor) {
                        openDatFile(dat, OpenInWhat.EDITOR_TEXT, null);
                    } else {
                        Project.getOpenTextWindows().iterator().next().openNewDatFileTab(dat, true);
                    }
                    Project.setFileToEdit(dat);
                }
            }

            updateTabs();
            WorkbenchManager.getUserSettingState().setSyncingTabs(tabSync);
            regainFocus();
        });

        widgetUtil(btnSaveDatPtr[0]).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null && !Project.getFileToEdit().equals(View.DUMMY_DATFILE)) {
                final DatFile df = Project.getFileToEdit();
                Editor3DWindow.getWindow().addRecentFile(df);
                if (!df.isReadOnly() && Project.getUnsavedFiles().contains(df)) {
                    if (df.save()) {
                        Editor3DWindow.getWindow().addRecentFile(df);
                        Editor3DWindow.getWindow().updateTreeUnsavedEntries();
                    } else {
                        MessageBox messageBoxError = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                        messageBoxError.setText(I18n.DIALOG_ERROR);
                        messageBoxError.setMessage(I18n.DIALOG_CANT_SAVE_FILE);
                    }
                }
            }
            regainFocus();
        });

        widgetUtil(btnSaveAsDatPtr[0]).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null && !Project.getFileToEdit().equals(View.DUMMY_DATFILE)) {
                final DatFile df2 = Project.getFileToEdit();

                FileDialog fd = new FileDialog(sh, SWT.SAVE);
                fd.setText(I18n.E3D_SAVE_DAT_FILE_AS);
                fd.setOverwrite(true);

                {
                    File f1 = new File(df2.getNewName()).getParentFile();
                    if (f1.exists()) {
                        fd.setFilterPath(f1.getAbsolutePath());
                    } else {
                        fd.setFilterPath(Project.getLastVisitedPath());
                    }
                }

                String[] filterExt = { "*.dat", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
                fd.setFilterExtensions(filterExt);
                String[] filterNames = {I18n.E3D_LDRAW_SOURCE_FILE, I18n.E3D_ALL_FILES};
                fd.setFilterNames(filterNames);

                while (true) {
                    try {
                        String selected = fd.open();
                        if (selected != null) {

                            if (Editor3DWindow.getWindow().isFileNameAllocated(selected, new DatFile(selected), true)) {
                                MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.RETRY | SWT.CANCEL);
                                messageBox.setText(I18n.DIALOG_ALREADY_ALLOCATED_NAME_TITLE);
                                messageBox.setMessage(I18n.DIALOG_ALREADY_ALLOCATED_NAME);

                                int result = messageBox.open();

                                if (result == SWT.CANCEL) {
                                    break;
                                } else if (result == SWT.RETRY) {
                                    continue;
                                }
                            }

                            df2.saveAs(selected);

                            DatFile df = Editor3DWindow.getWindow().openDatFile(OpenInWhat.EDITOR_3D, selected, false);
                            if (df != null) {
                                Editor3DWindow.getWindow().addRecentFile(df);
                                final File f2 = new File(df.getNewName());
                                if (f2.getParentFile() != null) {
                                    Project.setLastVisitedPath(f2.getParentFile().getAbsolutePath());
                                }
                            }
                        }
                    } catch (Exception ex) {
                        NLogger.error(getClass(), ex);
                    }
                    break;
                }

            }
            regainFocus();
        });

        widgetUtil(btnUndoPtr[0]).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null) {
                Project.getFileToEdit().getVertexManager().addSnapshot();
                Project.getFileToEdit().undo(false);
            }
            regainFocus();
        });

        widgetUtil(btnRedoPtr[0]).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null) {
                Project.getFileToEdit().getVertexManager().addSnapshot();
                Project.getFileToEdit().redo(false);
            }
            regainFocus();
        });

        if (NLogger.debugging) {
            widgetUtil(btnAddHistoryPtr[0]).addSelectionListener(e -> {
                if (Project.getFileToEdit() != null) {
                    Project.getFileToEdit().getVertexManager().addSnapshot();
                    Project.getFileToEdit().addHistory();
                }
            });
        }

        widgetUtil(btnSelectPtr[0]).addSelectionListener(e -> {
            clickBtnTest(btnSelectPtr[0]);
            workingAction = WorkingMode.SELECT;
            AddToolItem.disableAddAction();
            regainFocus();
        });
        widgetUtil(btnMovePtr[0]).addSelectionListener(e -> {
            clickBtnTest(btnMovePtr[0]);
            workingAction = WorkingMode.MOVE;
            AddToolItem.disableAddAction();
            regainFocus();
        });
        widgetUtil(btnRotatePtr[0]).addSelectionListener(e -> {
            clickBtnTest(btnRotatePtr[0]);
            workingAction = WorkingMode.ROTATE;
            AddToolItem.disableAddAction();
            regainFocus();
        });
        widgetUtil(btnScalePtr[0]).addSelectionListener(e -> {
            clickBtnTest(btnScalePtr[0]);
            workingAction = WorkingMode.SCALE;
            AddToolItem.disableAddAction();
            regainFocus();
        });
        widgetUtil(btnCombinedPtr[0]).addSelectionListener(e -> {
            clickBtnTest(btnCombinedPtr[0]);
            workingAction = WorkingMode.COMBINED;
            AddToolItem.disableAddAction();
            regainFocus();
        });

        widgetUtil(btnLocalPtr[0]).addSelectionListener(e -> {
            clickBtnTest(btnLocalPtr[0]);
            transformationMode = ManipulatorScope.LOCAL;
            regainFocus();
        });
        widgetUtil(btnGlobalPtr[0]).addSelectionListener(e -> {
            clickBtnTest(btnGlobalPtr[0]);
            transformationMode = ManipulatorScope.GLOBAL;
            regainFocus();
        });

        widgetUtil(btnVerticesPtr[0]).addSelectionListener(e -> {
            clickBtnTest(btnVerticesPtr[0]);
            setWorkingType(ObjectMode.VERTICES);
            if ((e.stateMask & SWT.ALT) == SWT.ALT && Project.getFileToEdit() != null && !Project.getFileToEdit().getVertexManager().getSelectedVertices().isEmpty()) {
                final VertexManager vm = Project.getFileToEdit().getVertexManager();
                vm.getSelectedVertices().clear();
                if (Cocoa.checkCtrlOrCmdPressed(e.stateMask)) {
                    vm.reSelectSubFiles();
                } else {
                    vm.getSelectedData().removeAll(vm.getSelectedSubfiles());
                    vm.getSelectedSubfiles().clear();
                }
                vm.setModified(true, true);
            }
            regainFocus();
        });
        widgetUtil(btnTrisNQuadsPtr[0]).addSelectionListener(e -> {
            clickBtnTest(btnTrisNQuadsPtr[0]);
            setWorkingType(ObjectMode.FACES);
            if ((e.stateMask & SWT.ALT) == SWT.ALT && Project.getFileToEdit() != null && (!Project.getFileToEdit().getVertexManager().getSelectedQuads().isEmpty() || !Project.getFileToEdit().getVertexManager().getSelectedTriangles().isEmpty())) {
                final VertexManager vm = Project.getFileToEdit().getVertexManager();
                vm.getSelectedData().removeAll(vm.getSelectedTriangles());
                vm.getSelectedData().removeAll(vm.getSelectedQuads());
                vm.getSelectedTriangles().clear();
                vm.getSelectedQuads().clear();
                if (Cocoa.checkCtrlOrCmdPressed(e.stateMask)) {
                    vm.reSelectSubFiles();
                } else {
                    vm.getSelectedData().removeAll(vm.getSelectedSubfiles());
                    vm.getSelectedSubfiles().clear();
                }
                vm.setModified(true, true);
            }
            regainFocus();
        });
        widgetUtil(btnLinesPtr[0]).addSelectionListener(e -> {
            clickBtnTest(btnLinesPtr[0]);
            setWorkingType(ObjectMode.LINES);
            if ((e.stateMask & SWT.ALT) == SWT.ALT && Project.getFileToEdit() != null && (!Project.getFileToEdit().getVertexManager().getSelectedLines().isEmpty() || !Project.getFileToEdit().getVertexManager().getSelectedCondlines().isEmpty())) {
                final VertexManager vm = Project.getFileToEdit().getVertexManager();
                vm.getSelectedData().removeAll(vm.getSelectedCondlines());
                vm.getSelectedData().removeAll(vm.getSelectedLines());
                vm.getSelectedCondlines().clear();
                vm.getSelectedLines().clear();
                if (Cocoa.checkCtrlOrCmdPressed(e.stateMask)) {
                    vm.reSelectSubFiles();
                } else {
                    vm.getSelectedData().removeAll(vm.getSelectedSubfiles());
                    vm.getSelectedSubfiles().clear();
                }
                vm.setModified(true, true);
            }
            regainFocus();
        });
        widgetUtil(btnSubfilesPtr[0]).addSelectionListener(e -> {
            clickBtnTest(btnSubfilesPtr[0]);
            setWorkingType(ObjectMode.SUBFILES);
            if ((e.stateMask & SWT.ALT) == SWT.ALT && !Cocoa.checkCtrlOrCmdPressed(e.stateMask) && Project.getFileToEdit() != null && !Project.getFileToEdit().getVertexManager().getSelectedSubfiles().isEmpty()) {
                final VertexManager vm = Project.getFileToEdit().getVertexManager();
                final List<GData1> subfiles = new ArrayList<>();
                subfiles.addAll(vm.getSelectedSubfiles());
                for (GData1 g1 : subfiles) {
                    vm.removeSubfileFromSelection(g1);
                }
                vm.getSelectedSubfiles().clear();
                vm.setModified(true, true);
            }
            regainFocus();
        });
        widgetUtil(btnMoveAdjacentDataPtr[0]).addSelectionListener(e -> {
            clickSingleBtn(btnMoveAdjacentDataPtr[0]);
            setMovingAdjacentData(btnMoveAdjacentDataPtr[0].getSelection());
            GuiStatusManager.updateStatus();
            regainFocus();
        });
        if (btnToggleLinesOpenGLPtr[0] != null) widgetUtil(btnToggleLinesOpenGLPtr[0]).addSelectionListener(e -> {
            if (btnToggleLinesOpenGLPtr[0].getSelection()) {
                View.edgeThreshold = 5e6f;
            } else {
                View.edgeThreshold = 5e-6f;
            }
            regainFocus();
        });
        widgetUtil(btnLineSize0Ptr[0]).addSelectionListener(e -> {
            setLineSize(GL20Primitives.SPHERE0, GL20Primitives.SPHERE_INV0, 0f, 0f, 0.01f, btnLineSize0Ptr[0]);
            regainFocus();
        });
        widgetUtil(btnLineSize1Ptr[0]).addSelectionListener(e -> {
            setLineSize(GL20Primitives.SPHERE1, GL20Primitives.SPHERE_INV1, 25f, .025f, 1f, btnLineSize1Ptr[0]);
            regainFocus();
        });
        widgetUtil(btnLineSize2Ptr[0]).addSelectionListener(e -> {
            setLineSize(GL20Primitives.SPHERE2, GL20Primitives.SPHERE_INV2, 50f, .050f, 2f, btnLineSize2Ptr[0]);
            regainFocus();
        });
        widgetUtil(btnLineSize3Ptr[0]).addSelectionListener(e -> {
            setLineSize(GL20Primitives.SPHERE3, GL20Primitives.SPHERE_INV3, 100f, .100f, 3f, btnLineSize3Ptr[0]);
            regainFocus();
        });
        widgetUtil(btnLineSize4Ptr[0]).addSelectionListener(e -> {
            setLineSize(GL20Primitives.SPHERE4, GL20Primitives.SPHERE_INV4, 200f, .200f, 4f, btnLineSize4Ptr[0]);
            regainFocus();
        });
        widgetUtil(btnCloseViewPtr[0]).addSelectionListener(e -> {
            Composite3D c3d = getCurrentCoposite3d();
            if (c3d != null) {
                c3d.getModifier().closeView();
            }
            regainFocus();
        });
        widgetUtil(btnRenderModeNoBackfaceCullingPtr[0]).addSelectionListener(e -> {
            Composite3D c3d = getCurrentCoposite3d();
            if (c3d != null) {
                c3d.setRenderMode(0);
                c3d.setRenderModeOnContextMenu(0);
            }
            regainFocus();
        });
        widgetUtil(btnRenderModeRandomColoursPtr[0]).addSelectionListener(e -> {
            Composite3D c3d = getCurrentCoposite3d();
            if (c3d != null) {
                c3d.setRenderMode(1);
                c3d.setRenderModeOnContextMenu(1);
            }
            regainFocus();
        });
        widgetUtil(btnRenderModeGreenRedPtr[0]).addSelectionListener(e -> {
            Composite3D c3d = getCurrentCoposite3d();
            if (c3d != null) {
                c3d.setRenderMode(2);
                c3d.setRenderModeOnContextMenu(2);
            }
            regainFocus();
        });
        widgetUtil(btnRenderModeRedBackfacesPtr[0]).addSelectionListener(e -> {
            Composite3D c3d = getCurrentCoposite3d();
            if (c3d != null) {
                c3d.setRenderMode(3);
                c3d.setRenderModeOnContextMenu(3);
            }
            regainFocus();
        });
        widgetUtil(btnRenderModeRealBackfaceCullingPtr[0]).addSelectionListener(e -> {
            Composite3D c3d = getCurrentCoposite3d();
            if (c3d != null) {
                c3d.setRenderMode(4);
                c3d.setRenderModeOnContextMenu(4);
            }
            regainFocus();
        });
        widgetUtil(btnRenderModeLDrawStandardPtr[0]).addSelectionListener(e -> {
            Composite3D c3d = getCurrentCoposite3d();
            if (c3d != null) {
                c3d.setRenderMode(5);
                c3d.setRenderModeOnContextMenu(5);
            }
            regainFocus();
        });
        widgetUtil(btnRenderModeCondlineModePtr[0]).addSelectionListener(e -> {
            Composite3D c3d = getCurrentCoposite3d();
            if (c3d != null) {
                c3d.setRenderMode(6);
                c3d.setRenderModeOnContextMenu(6);
            }
            regainFocus();
        });
        widgetUtil(btnRenderModeCoplanarityModePtr[0]).addSelectionListener(e -> {
            Composite3D c3d = getCurrentCoposite3d();
            if (c3d != null) {
                c3d.setRenderMode(7);
                c3d.setRenderModeOnContextMenu(7);
            }
            regainFocus();
        });
        widgetUtil(btnRenderModeWireframePtr[0]).addSelectionListener(e -> {
            Composite3D c3d = getCurrentCoposite3d();
            if (c3d != null) {
                c3d.setRenderMode(-1);
                c3d.setRenderModeOnContextMenu(-1);
            }
            regainFocus();
        });

        widgetUtil(btnPipettePtr[0]).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null) {
                VertexManager vm = Project.getFileToEdit().getVertexManager();
                vm.addSnapshot();
                final GColour gColour2;
                {
                    GColour gColour3 = vm.getRandomSelectedColour(lastUsedColour);
                    if (gColour3.getColourNumber() == 16) {
                        gColour2 = LDConfig.getColour16();
                    } else {
                        gColour2 = gColour3;
                    }
                    lastUsedColour = gColour2;
                }
                setLastUsedColour(gColour2);
                btnLastUsedColourPtr[0].clearPaintListeners();
                btnLastUsedColourPtr[0].clearSelectionListeners();
                final int imgSize = IconSize.getImageSizeFromIconSize();
                final Color col = SWTResourceManager.getColor((int) (gColour2.getR() * 255f), (int) (gColour2.getG() * 255f), (int) (gColour2.getB() * 255f));
                final Point size = btnLastUsedColourPtr[0].computeSize(SWT.DEFAULT, SWT.DEFAULT);
                final int x = Math.round(size.x / 5f);
                final int y = Math.round(size.y / 5f);
                final int w = Math.round(size.x * (3f / 5f));
                final int h = Math.round(size.y * (3f / 5f));
                int num = gColour2.getColourNumber();
                btnLastUsedColourPtr[0].addPaintListener(e1 -> {
                    e1.gc.setBackground(col);
                    e1.gc.fillRectangle(x, y, w, h);
                    if (gColour2.getA() >= .99f) {
                        e1.gc.drawImage(ResourceManager.getImage("icon16_transparent.png"), 0, 0, imgSize, imgSize, x, y, w, h); //$NON-NLS-1$
                    } else {
                        e1.gc.drawImage(ResourceManager.getImage("icon16_halftrans.png"), 0, 0, imgSize, imgSize, x, y, w, h); //$NON-NLS-1$
                    }
                });
                widgetUtil(btnLastUsedColourPtr[0]).addSelectionListener(e1 -> {
                    if (Project.getFileToEdit() != null) {
                        int num1 = gColour2.getColourNumber();
                        if (!LDConfig.hasColour(num1)) {
                            num1 = -1;
                        }
                        Project.getFileToEdit().getVertexManager().addSnapshot();
                        Project.getFileToEdit().getVertexManager().colourChangeSelection(num1, gColour2.getR(), gColour2.getG(), gColour2.getB(), gColour2.getA(), true);
                    }
                    regainFocus();
                });
                if (num != -1) {

                    Object[] messageArguments1 = {num, LDConfig.getColourName(num)};
                    MessageFormat formatter1 = new MessageFormat(""); //$NON-NLS-1$
                    formatter1.setLocale(MyLanguage.getLocale());
                    formatter1.applyPattern(I18n.EDITORTEXT_COLOUR_1);

                    btnLastUsedColourPtr[0].setToolTipText(formatter1.format(messageArguments1));
                } else {
                    StringBuilder colourBuilder = new StringBuilder();
                    colourBuilder.append("0x2"); //$NON-NLS-1$
                    colourBuilder.append(MathHelper.toHex((int) (255f * gColour2.getR())).toUpperCase());
                    colourBuilder.append(MathHelper.toHex((int) (255f * gColour2.getG())).toUpperCase());
                    colourBuilder.append(MathHelper.toHex((int) (255f * gColour2.getB())).toUpperCase());

                    Object[] messageArguments2 = {colourBuilder.toString()};
                    MessageFormat formatter2 = new MessageFormat(""); //$NON-NLS-1$
                    formatter2.setLocale(MyLanguage.getLocale());
                    formatter2.applyPattern(I18n.EDITORTEXT_COLOUR_2);

                    btnLastUsedColourPtr[0].setToolTipText(formatter2.format(messageArguments2));
                }
                btnLastUsedColourPtr[0].redraw();
            }
            regainFocus();
        });

        widgetUtil(btnDecolourPtr[0]).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null && !Project.getFileToEdit().isReadOnly()) {
                VertexManager vm = Project.getFileToEdit().getVertexManager();
                vm.addSnapshot();
                vm.selectAll(new SelectorSettings(), true);
                GDataCSG.clearSelection(Project.getFileToEdit());
                GColour c = LDConfig.getColour16();
                vm.colourChangeSelection(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), false);
                vm.getSelectedData().removeAll(vm.getTriangles().keySet());
                vm.getSelectedData().removeAll(vm.getQuads().keySet());
                vm.getSelectedData().removeAll(vm.getSelectedSubfiles());
                vm.getSelectedSubfiles().clear();
                vm.getSelectedTriangles().removeAll(vm.getTriangles().keySet());
                vm.getSelectedQuads().removeAll(vm.getQuads().keySet());
                c = LDConfig.getColour(24);
                vm.colourChangeSelection(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), true);
            }
        });

        initPaletteEvent();

        widgetUtil(btnCoarsePtr[0]).addSelectionListener(e -> {
            BigDecimal m = WorkbenchManager.getUserSettingState().getCoarseMoveSnap();
            BigDecimal r = WorkbenchManager.getUserSettingState().getCoarseRotateSnap();
            BigDecimal s = WorkbenchManager.getUserSettingState().getCoarseScaleSnap();
            snapSize = SnapSize.COARSE;
            spnMovePtr[0].setValue(m);
            spnRotatePtr[0].setValue(r);
            spnScalePtr[0].setValue(s);
            Manipulator.setSnap(m, r, s);
            regainFocus();
        });

        widgetUtil(btnMediumPtr[0]).addSelectionListener(e -> {
            BigDecimal m = WorkbenchManager.getUserSettingState().getMediumMoveSnap();
            BigDecimal r = WorkbenchManager.getUserSettingState().getMediumRotateSnap();
            BigDecimal s = WorkbenchManager.getUserSettingState().getMediumScaleSnap();
            snapSize = SnapSize.MEDIUM;
            spnMovePtr[0].setValue(m);
            spnRotatePtr[0].setValue(r);
            spnScalePtr[0].setValue(s);
            Manipulator.setSnap(m, r, s);
            regainFocus();
        });

        widgetUtil(btnFinePtr[0]).addSelectionListener(e -> {
            BigDecimal m = WorkbenchManager.getUserSettingState().getFineMoveSnap();
            BigDecimal r = WorkbenchManager.getUserSettingState().getFineRotateSnap();
            BigDecimal s = WorkbenchManager.getUserSettingState().getFineScaleSnap();
            snapSize = SnapSize.FINE;
            spnMovePtr[0].setValue(m);
            spnRotatePtr[0].setValue(r);
            spnScalePtr[0].setValue(s);
            Manipulator.setSnap(m, r, s);
            regainFocus();
        });

        btnCoarsePtr[0].addListener(SWT.MouseDown, event -> {
            if (event.button == MouseButton.RIGHT) {

                try {
                    if (btnCoarsePtr[0].getMenu() != null) {
                        btnCoarsePtr[0].getMenu().dispose();
                    }
                } catch (Exception ex) {}

                Menu gridMenu = new Menu(btnCoarsePtr[0]);
                btnCoarsePtr[0].setMenu(gridMenu);
                mnuCoarseMenuPtr[0] = gridMenu;

                MenuItem mntmGridCoarseDefault = new MenuItem(gridMenu, I18n.noBiDirectionalTextStyle());
                mntmGridCoarseDefaultPtr[0] = mntmGridCoarseDefault;
                mntmGridCoarseDefault.setEnabled(true);
                mntmGridCoarseDefault.setText(I18n.E3D_GRID_COARSE_DEFAULT);

                widgetUtil(mntmGridCoarseDefaultPtr[0]).addSelectionListener(e -> {
                    WorkbenchManager.getUserSettingState().setCoarseMoveSnap(new BigDecimal("1")); //$NON-NLS-1$
                    WorkbenchManager.getUserSettingState().setCoarseRotateSnap(new BigDecimal("90")); //$NON-NLS-1$
                    WorkbenchManager.getUserSettingState().setCoarseScaleSnap(new BigDecimal("2")); //$NON-NLS-1$
                    BigDecimal m = WorkbenchManager.getUserSettingState().getCoarseMoveSnap();
                    BigDecimal r = WorkbenchManager.getUserSettingState().getCoarseRotateSnap();
                    BigDecimal s = WorkbenchManager.getUserSettingState().getCoarseScaleSnap();
                    snapSize = SnapSize.COARSE;
                    spnMovePtr[0].setValue(m);
                    spnRotatePtr[0].setValue(r);
                    spnScalePtr[0].setValue(s);
                    Manipulator.setSnap(m, r, s);
                    btnCoarsePtr[0].setSelection(true);
                    btnMediumPtr[0].setSelection(false);
                    btnFinePtr[0].setSelection(false);
                    regainFocus();
                });

                java.awt.Point b = java.awt.MouseInfo.getPointerInfo().getLocation();
                final int x = (int) b.getX();
                final int y = (int) b.getY();

                Menu menu = mnuCoarseMenuPtr[0];
                menu.setLocation(x, y);
                menu.setVisible(true);
                regainFocus();
            }
        });

        btnMediumPtr[0].addListener(SWT.MouseDown, event -> {
            if (event.button == MouseButton.RIGHT) {

                try {
                    if (btnMediumPtr[0].getMenu() != null) {
                        btnMediumPtr[0].getMenu().dispose();
                    }
                } catch (Exception ex) {}

                Menu gridMenu = new Menu(btnMediumPtr[0]);
                btnMediumPtr[0].setMenu(gridMenu);
                mnuMediumMenuPtr[0] = gridMenu;

                MenuItem mntmGridMediumDefault = new MenuItem(gridMenu, I18n.noBiDirectionalTextStyle());
                mntmGridMediumDefaultPtr[0] = mntmGridMediumDefault;
                mntmGridMediumDefault.setEnabled(true);
                mntmGridMediumDefault.setText(I18n.E3D_GRID_MEDIUM_DEFAULT);

                widgetUtil(mntmGridMediumDefaultPtr[0]).addSelectionListener(e -> {
                    WorkbenchManager.getUserSettingState().setMediumMoveSnap(new BigDecimal("0.01")); //$NON-NLS-1$
                    WorkbenchManager.getUserSettingState().setMediumRotateSnap(new BigDecimal("11.25")); //$NON-NLS-1$
                    WorkbenchManager.getUserSettingState().setMediumScaleSnap(new BigDecimal("1.1")); //$NON-NLS-1$
                    BigDecimal m = WorkbenchManager.getUserSettingState().getMediumMoveSnap();
                    BigDecimal r = WorkbenchManager.getUserSettingState().getMediumRotateSnap();
                    BigDecimal s = WorkbenchManager.getUserSettingState().getMediumScaleSnap();
                    snapSize = SnapSize.MEDIUM;
                    spnMovePtr[0].setValue(m);
                    spnRotatePtr[0].setValue(r);
                    spnScalePtr[0].setValue(s);
                    Manipulator.setSnap(m, r, s);
                    btnCoarsePtr[0].setSelection(false);
                    btnMediumPtr[0].setSelection(true);
                    btnFinePtr[0].setSelection(false);
                    regainFocus();
                });

                java.awt.Point b = java.awt.MouseInfo.getPointerInfo().getLocation();
                final int x = (int) b.getX();
                final int y = (int) b.getY();

                Menu menu = mnuMediumMenuPtr[0];
                menu.setLocation(x, y);
                menu.setVisible(true);
                regainFocus();
            }
        });

        btnFinePtr[0].addListener(SWT.MouseDown, event -> {
            if (event.button == MouseButton.RIGHT) {

                try {
                    if (btnFinePtr[0].getMenu() != null) {
                        btnFinePtr[0].getMenu().dispose();
                    }
                } catch (Exception ex) {}

                Menu gridMenu = new Menu(btnFinePtr[0]);
                btnFinePtr[0].setMenu(gridMenu);
                mnuFineMenuPtr[0] = gridMenu;

                MenuItem mntmGridFineDefault = new MenuItem(gridMenu, I18n.noBiDirectionalTextStyle());
                mntmGridFineDefaultPtr[0] = mntmGridFineDefault;
                mntmGridFineDefault.setEnabled(true);
                mntmGridFineDefault.setText(I18n.E3D_GRID_FINE_DEFAULT);

                widgetUtil(mntmGridFineDefaultPtr[0]).addSelectionListener(e -> {
                    WorkbenchManager.getUserSettingState().setFineMoveSnap(new BigDecimal("0.0001")); //$NON-NLS-1$
                    WorkbenchManager.getUserSettingState().setFineRotateSnap(BigDecimal.ONE);
                    WorkbenchManager.getUserSettingState().setFineScaleSnap(new BigDecimal("1.001")); //$NON-NLS-1$
                    BigDecimal m = WorkbenchManager.getUserSettingState().getFineMoveSnap();
                    BigDecimal r = WorkbenchManager.getUserSettingState().getFineRotateSnap();
                    BigDecimal s = WorkbenchManager.getUserSettingState().getFineScaleSnap();
                    snapSize = SnapSize.FINE;
                    spnMovePtr[0].setValue(m);
                    spnRotatePtr[0].setValue(r);
                    spnScalePtr[0].setValue(s);
                    Manipulator.setSnap(m, r, s);
                    btnCoarsePtr[0].setSelection(false);
                    btnMediumPtr[0].setSelection(false);
                    btnFinePtr[0].setSelection(true);
                    regainFocus();
                });

                java.awt.Point b = java.awt.MouseInfo.getPointerInfo().getLocation();
                final int x = (int) b.getX();
                final int y = (int) b.getY();

                Menu menu = mnuFineMenuPtr[0];
                menu.setLocation(x, y);
                menu.setVisible(true);
                regainFocus();
            }
        });

        spnMovePtr[0].addValueChangeListener(spn -> {
            BigDecimal m;
            BigDecimal r;
            BigDecimal s;
            m = spn.getValue();
            switch (snapSize) {
            case FINE:
                WorkbenchManager.getUserSettingState().setFineMoveSnap(m);
                r = WorkbenchManager.getUserSettingState().getFineRotateSnap();
                s = WorkbenchManager.getUserSettingState().getFineScaleSnap();
                break;
            case MEDIUM:
                WorkbenchManager.getUserSettingState().setMediumMoveSnap(m);
                r = WorkbenchManager.getUserSettingState().getMediumRotateSnap();
                s = WorkbenchManager.getUserSettingState().getMediumScaleSnap();
                break;
            case COARSE:
            default:
                WorkbenchManager.getUserSettingState().setCoarseMoveSnap(m);
                r = WorkbenchManager.getUserSettingState().getCoarseRotateSnap();
                s = WorkbenchManager.getUserSettingState().getCoarseScaleSnap();
                break;
            }
            Manipulator.setSnap(m, r, s);
        });

        spnRotatePtr[0].addValueChangeListener(spn -> {
            BigDecimal m;
            BigDecimal r;
            BigDecimal s;
            r = spn.getValue();
            switch (snapSize) {
            case FINE:
                m = WorkbenchManager.getUserSettingState().getFineMoveSnap();
                WorkbenchManager.getUserSettingState().setFineRotateSnap(r);
                s = WorkbenchManager.getUserSettingState().getFineScaleSnap();
                break;
            case MEDIUM:
                m = WorkbenchManager.getUserSettingState().getMediumMoveSnap();
                WorkbenchManager.getUserSettingState().setMediumRotateSnap(r);
                s = WorkbenchManager.getUserSettingState().getMediumScaleSnap();
                break;
            case COARSE:
            default:
                m = WorkbenchManager.getUserSettingState().getCoarseMoveSnap();
                WorkbenchManager.getUserSettingState().setCoarseRotateSnap(r);
                s = WorkbenchManager.getUserSettingState().getCoarseScaleSnap();
                break;
            }
            Manipulator.setSnap(m, r, s);
        });

        spnScalePtr[0].addValueChangeListener(spn -> {
            BigDecimal m;
            BigDecimal r;
            BigDecimal s;
            s = spn.getValue();
            switch (snapSize) {
            case FINE:
                m = WorkbenchManager.getUserSettingState().getFineMoveSnap();
                r = WorkbenchManager.getUserSettingState().getFineRotateSnap();
                WorkbenchManager.getUserSettingState().setFineScaleSnap(s);
                break;
            case MEDIUM:
                m = WorkbenchManager.getUserSettingState().getMediumMoveSnap();
                r = WorkbenchManager.getUserSettingState().getMediumRotateSnap();
                WorkbenchManager.getUserSettingState().setMediumScaleSnap(s);
                break;
            case COARSE:
            default:
                m = WorkbenchManager.getUserSettingState().getCoarseMoveSnap();
                r = WorkbenchManager.getUserSettingState().getCoarseRotateSnap();
                WorkbenchManager.getUserSettingState().setCoarseScaleSnap(s);
                break;
            }
            Manipulator.setSnap(m, r, s);
        });

        spnScaleInitialPtr[0].addValueChangeListener(spn -> Manipulator.setInitialScale(spn.getValue()));

        widgetUtil(btnPreviousSelectionPtr[0]).addSelectionListener(e -> {
            updatingSelectionTab = true;
            NLogger.debug(getClass(), "Previous Selection..."); //$NON-NLS-1$
            final DatFile df = Project.getFileToEdit();
            if (df != null && !df.isReadOnly()) {
                final VertexManager vm = df.getVertexManager();
                vm.addSnapshot();
                final int count = vm.getSelectedData().size();
                if (count > 0) {
                    boolean breakIt = false;
                    boolean firstRun = true;
                    while (true) {
                        int index = vm.getSelectedItemIndex();
                        index--;
                        if (index < 0) {
                            index = count - 1;
                            if (!firstRun) breakIt = true;
                        }
                        if (index > count - 1) index = count - 1;
                        firstRun = false;
                        vm.setSelectedItemIndex(index);
                        final GData gdata = (GData) vm.getSelectedData().toArray()[index];

                        if (vm.isNotInSubfileAndLinetype1to5(gdata)) {
                            vm.setSelectedLine(gdata);
                            disableSelectionTab();
                            updatingSelectionTab = true;
                            switch (gdata.type()) {
                            case 1:
                            case 5:
                            case 4:
                                spnSelectionX4Ptr[0].setEnabled(true);
                                spnSelectionY4Ptr[0].setEnabled(true);
                                spnSelectionZ4Ptr[0].setEnabled(true);
                            case 3:
                                spnSelectionAnglePtr[0].setEnabled(gdata.type() == 3 && !((org.nschmidt.ldparteditor.data.GData3) gdata).isTriangle);
                                spnSelectionX3Ptr[0].setEnabled(true);
                                spnSelectionY3Ptr[0].setEnabled(true);
                                spnSelectionZ3Ptr[0].setEnabled(true);
                            case 2:
                                spnSelectionLengthPtr[0].setEnabled(
                                        gdata.type() == 2 && !((org.nschmidt.ldparteditor.data.GData2) gdata).isLine
                                        || gdata.type() == 3 && !((org.nschmidt.ldparteditor.data.GData3) gdata).isTriangle);
                                spnSelectionX1Ptr[0].setEnabled(true);
                                spnSelectionY1Ptr[0].setEnabled(true);
                                spnSelectionZ1Ptr[0].setEnabled(true);
                                spnSelectionX2Ptr[0].setEnabled(true);
                                spnSelectionY2Ptr[0].setEnabled(true);
                                spnSelectionZ2Ptr[0].setEnabled(true);

                                txtLinePtr[0].setText(gdata.toString());
                                breakIt = true;
                                btnMoveAdjacentData2Ptr[0].setEnabled(true);
                                switch (gdata.type()) {
                                case 5:
                                    BigDecimal[] g5 = GraphicalDataTools.getPreciseCoordinates(gdata);
                                    spnSelectionX1Ptr[0].setValue(g5[0]);
                                    spnSelectionY1Ptr[0].setValue(g5[1]);
                                    spnSelectionZ1Ptr[0].setValue(g5[2]);
                                    spnSelectionX2Ptr[0].setValue(g5[3]);
                                    spnSelectionY2Ptr[0].setValue(g5[4]);
                                    spnSelectionZ2Ptr[0].setValue(g5[5]);
                                    spnSelectionX3Ptr[0].setValue(g5[6]);
                                    spnSelectionY3Ptr[0].setValue(g5[7]);
                                    spnSelectionZ3Ptr[0].setValue(g5[8]);
                                    spnSelectionX4Ptr[0].setValue(g5[9]);
                                    spnSelectionY4Ptr[0].setValue(g5[10]);
                                    spnSelectionZ4Ptr[0].setValue(g5[11]);
                                    break;
                                case 4:
                                    BigDecimal[] g4 = GraphicalDataTools.getPreciseCoordinates(gdata);
                                    spnSelectionX1Ptr[0].setValue(g4[0]);
                                    spnSelectionY1Ptr[0].setValue(g4[1]);
                                    spnSelectionZ1Ptr[0].setValue(g4[2]);
                                    spnSelectionX2Ptr[0].setValue(g4[3]);
                                    spnSelectionY2Ptr[0].setValue(g4[4]);
                                    spnSelectionZ2Ptr[0].setValue(g4[5]);
                                    spnSelectionX3Ptr[0].setValue(g4[6]);
                                    spnSelectionY3Ptr[0].setValue(g4[7]);
                                    spnSelectionZ3Ptr[0].setValue(g4[8]);
                                    spnSelectionX4Ptr[0].setValue(g4[9]);
                                    spnSelectionY4Ptr[0].setValue(g4[10]);
                                    spnSelectionZ4Ptr[0].setValue(g4[11]);
                                    break;
                                case 3:
                                    BigDecimal[] g3 = GraphicalDataTools.getPreciseCoordinates(gdata);
                                    if (spnSelectionAnglePtr[0].isEnabled()) {
                                        spnSelectionAnglePtr[0].setValue(new BigDecimal(((org.nschmidt.ldparteditor.data.GData3) gdata).getProtractorAngle()));
                                    } else {
                                        spnSelectionAnglePtr[0].setValue(BigDecimal.ZERO);
                                    }
                                    if (spnSelectionLengthPtr[0].isEnabled()) {
                                        spnSelectionLengthPtr[0].setValue(((org.nschmidt.ldparteditor.data.GData3) gdata).getProtractorLength());
                                    } else {
                                        spnSelectionLengthPtr[0].setValue(BigDecimal.ONE);
                                    }
                                    spnSelectionX1Ptr[0].setValue(g3[0]);
                                    spnSelectionY1Ptr[0].setValue(g3[1]);
                                    spnSelectionZ1Ptr[0].setValue(g3[2]);
                                    spnSelectionX2Ptr[0].setValue(g3[3]);
                                    spnSelectionY2Ptr[0].setValue(g3[4]);
                                    spnSelectionZ2Ptr[0].setValue(g3[5]);
                                    spnSelectionX3Ptr[0].setValue(g3[6]);
                                    spnSelectionY3Ptr[0].setValue(g3[7]);
                                    spnSelectionZ3Ptr[0].setValue(g3[8]);
                                    break;
                                case 2:
                                    BigDecimal[] g2 = GraphicalDataTools.getPreciseCoordinates(gdata);
                                    if (spnSelectionLengthPtr[0].isEnabled()) {
                                        spnSelectionLengthPtr[0].setValue(((org.nschmidt.ldparteditor.data.GData2) gdata).getLength());
                                    } else {
                                        spnSelectionLengthPtr[0].setValue(BigDecimal.ONE);
                                    }
                                    spnSelectionX1Ptr[0].setValue(g2[0]);
                                    spnSelectionY1Ptr[0].setValue(g2[1]);
                                    spnSelectionZ1Ptr[0].setValue(g2[2]);
                                    spnSelectionX2Ptr[0].setValue(g2[3]);
                                    spnSelectionY2Ptr[0].setValue(g2[4]);
                                    spnSelectionZ2Ptr[0].setValue(g2[5]);
                                    break;
                                case 1:
                                    vm.getSelectedVertices().clear();
                                    btnMoveAdjacentData2Ptr[0].setSelection(false);
                                    btnMoveAdjacentData2Ptr[0].setEnabled(false);
                                    GData1 g1 = (GData1) gdata;
                                    spnSelectionX1Ptr[0].setValue(g1.getAccurateProductMatrix().m30);
                                    spnSelectionY1Ptr[0].setValue(g1.getAccurateProductMatrix().m31);
                                    spnSelectionZ1Ptr[0].setValue(g1.getAccurateProductMatrix().m32);
                                    spnSelectionX2Ptr[0].setValue(g1.getAccurateProductMatrix().m00);
                                    spnSelectionY2Ptr[0].setValue(g1.getAccurateProductMatrix().m01);
                                    spnSelectionZ2Ptr[0].setValue(g1.getAccurateProductMatrix().m02);
                                    spnSelectionX3Ptr[0].setValue(g1.getAccurateProductMatrix().m10);
                                    spnSelectionY3Ptr[0].setValue(g1.getAccurateProductMatrix().m11);
                                    spnSelectionZ3Ptr[0].setValue(g1.getAccurateProductMatrix().m12);
                                    spnSelectionX4Ptr[0].setValue(g1.getAccurateProductMatrix().m20);
                                    spnSelectionY4Ptr[0].setValue(g1.getAccurateProductMatrix().m21);
                                    spnSelectionZ4Ptr[0].setValue(g1.getAccurateProductMatrix().m22);
                                    break;
                                default:
                                    disableSelectionTab();
                                    updatingSelectionTab = true;
                                    break;
                                }

                                lblSelectionAnglePtr[0].setText((gdata.type() != 1 ? I18n.E3D_PROTRACTOR_ANGLE : "") + " {" + spnSelectionAnglePtr[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                lblSelectionLengthPtr[0].setText((gdata.type() != 1 ? I18n.E3D_PROTRACTOR_LENGTH : "") + " {" + spnSelectionLengthPtr[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                lblSelectionX1Ptr[0].setText((gdata.type() != 1 ? I18n.E3D_POSITION_X1 : "") + " {" + spnSelectionX1Ptr[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                lblSelectionY1Ptr[0].setText((gdata.type() != 1 ? I18n.E3D_POSITION_Y1 : "") + " {" + spnSelectionY1Ptr[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                lblSelectionZ1Ptr[0].setText((gdata.type() != 1 ? I18n.E3D_POSITION_Z1 : "") + " {" + spnSelectionZ1Ptr[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                lblSelectionX2Ptr[0].setText((gdata.type() != 1 ? I18n.E3D_POSITION_X2 : "") + " {" + spnSelectionX2Ptr[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                lblSelectionY2Ptr[0].setText((gdata.type() != 1 ? I18n.E3D_POSITION_Y2 : "") + " {" + spnSelectionY2Ptr[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                lblSelectionZ2Ptr[0].setText((gdata.type() != 1 ? I18n.E3D_POSITION_Z2 : "") + " {" + spnSelectionZ2Ptr[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                lblSelectionX3Ptr[0].setText((gdata.type() != 1 ? I18n.E3D_POSITION_X3 : "") + " {" + spnSelectionX3Ptr[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                lblSelectionY3Ptr[0].setText((gdata.type() != 1 ? I18n.E3D_POSITION_Y3 : "") + " {" + spnSelectionY3Ptr[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                lblSelectionZ3Ptr[0].setText((gdata.type() != 1 ? I18n.E3D_POSITION_Z3 : "") + " {" + spnSelectionZ3Ptr[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                lblSelectionX4Ptr[0].setText((gdata.type() != 1 ? I18n.E3D_POSITION_X4 : "") + " {" + spnSelectionX4Ptr[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                lblSelectionY4Ptr[0].setText((gdata.type() != 1 ? I18n.E3D_POSITION_Y4 : "") + " {" + spnSelectionY4Ptr[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                lblSelectionZ4Ptr[0].setText((gdata.type() != 1 ? I18n.E3D_POSITION_Z4 : "") + " {" + spnSelectionZ4Ptr[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

                                lblSelectionX1Ptr[0].getParent().layout();
                                updatingSelectionTab = false;

                                break;
                            default:
                                disableSelectionTab();
                                break;
                            }
                        } else {
                            disableSelectionTab();
                        }
                        if (breakIt) break;
                    }
                } else {
                    disableSelectionTab();
                }
            } else {
                disableSelectionTab();
            }
            updatingSelectionTab = false;
            regainFocus();
        });
        widgetUtil(btnNextSelectionPtr[0]).addSelectionListener(e -> {
            updatingSelectionTab = true;
            NLogger.debug(getClass(), "Next Selection..."); //$NON-NLS-1$
            final DatFile df = Project.getFileToEdit();
            if (df != null && !df.isReadOnly()) {
                final VertexManager vm = df.getVertexManager();
                vm.addSnapshot();
                final int count = vm.getSelectedData().size();
                if (count > 0) {
                    boolean breakIt = false;
                    boolean firstRun = true;
                    while (true) {
                        int index = vm.getSelectedItemIndex();
                        index++;
                        if (index >= count) {
                            index = 0;
                            if (!firstRun) breakIt = true;
                        }
                        firstRun = false;
                        vm.setSelectedItemIndex(index);
                        final GData gdata = (GData) vm.getSelectedData().toArray()[index];

                        if (vm.isNotInSubfileAndLinetype1to5(gdata)) {
                            vm.setSelectedLine(gdata);
                            disableSelectionTab();
                            updatingSelectionTab = true;
                            switch (gdata.type()) {
                            case 1:
                            case 5:
                            case 4:
                                spnSelectionX4Ptr[0].setEnabled(true);
                                spnSelectionY4Ptr[0].setEnabled(true);
                                spnSelectionZ4Ptr[0].setEnabled(true);
                            case 3:
                                spnSelectionAnglePtr[0].setEnabled(gdata.type() == 3 && !((org.nschmidt.ldparteditor.data.GData3) gdata).isTriangle);
                                spnSelectionX3Ptr[0].setEnabled(true);
                                spnSelectionY3Ptr[0].setEnabled(true);
                                spnSelectionZ3Ptr[0].setEnabled(true);
                            case 2:
                                spnSelectionLengthPtr[0].setEnabled(
                                        gdata.type() == 2 && !((org.nschmidt.ldparteditor.data.GData2) gdata).isLine
                                        || gdata.type() == 3 && !((org.nschmidt.ldparteditor.data.GData3) gdata).isTriangle);
                                spnSelectionX1Ptr[0].setEnabled(true);
                                spnSelectionY1Ptr[0].setEnabled(true);
                                spnSelectionZ1Ptr[0].setEnabled(true);
                                spnSelectionX2Ptr[0].setEnabled(true);
                                spnSelectionY2Ptr[0].setEnabled(true);
                                spnSelectionZ2Ptr[0].setEnabled(true);

                                txtLinePtr[0].setText(gdata.toString());
                                breakIt = true;
                                btnMoveAdjacentData2Ptr[0].setEnabled(true);
                                switch (gdata.type()) {
                                case 5:
                                    BigDecimal[] g5 = GraphicalDataTools.getPreciseCoordinates(gdata);
                                    spnSelectionX1Ptr[0].setValue(g5[0]);
                                    spnSelectionY1Ptr[0].setValue(g5[1]);
                                    spnSelectionZ1Ptr[0].setValue(g5[2]);
                                    spnSelectionX2Ptr[0].setValue(g5[3]);
                                    spnSelectionY2Ptr[0].setValue(g5[4]);
                                    spnSelectionZ2Ptr[0].setValue(g5[5]);
                                    spnSelectionX3Ptr[0].setValue(g5[6]);
                                    spnSelectionY3Ptr[0].setValue(g5[7]);
                                    spnSelectionZ3Ptr[0].setValue(g5[8]);
                                    spnSelectionX4Ptr[0].setValue(g5[9]);
                                    spnSelectionY4Ptr[0].setValue(g5[10]);
                                    spnSelectionZ4Ptr[0].setValue(g5[11]);
                                    break;
                                case 4:
                                    BigDecimal[] g4 = GraphicalDataTools.getPreciseCoordinates(gdata);
                                    spnSelectionX1Ptr[0].setValue(g4[0]);
                                    spnSelectionY1Ptr[0].setValue(g4[1]);
                                    spnSelectionZ1Ptr[0].setValue(g4[2]);
                                    spnSelectionX2Ptr[0].setValue(g4[3]);
                                    spnSelectionY2Ptr[0].setValue(g4[4]);
                                    spnSelectionZ2Ptr[0].setValue(g4[5]);
                                    spnSelectionX3Ptr[0].setValue(g4[6]);
                                    spnSelectionY3Ptr[0].setValue(g4[7]);
                                    spnSelectionZ3Ptr[0].setValue(g4[8]);
                                    spnSelectionX4Ptr[0].setValue(g4[9]);
                                    spnSelectionY4Ptr[0].setValue(g4[10]);
                                    spnSelectionZ4Ptr[0].setValue(g4[11]);
                                    break;
                                case 3:
                                    BigDecimal[] g3 = GraphicalDataTools.getPreciseCoordinates(gdata);
                                    if (spnSelectionAnglePtr[0].isEnabled()) {
                                        spnSelectionAnglePtr[0].setValue(new BigDecimal(((org.nschmidt.ldparteditor.data.GData3) gdata).getProtractorAngle()));
                                    } else {
                                        spnSelectionAnglePtr[0].setValue(BigDecimal.ZERO);
                                    }
                                    if (spnSelectionLengthPtr[0].isEnabled()) {
                                        spnSelectionLengthPtr[0].setValue(((org.nschmidt.ldparteditor.data.GData3) gdata).getProtractorLength());
                                    } else {
                                        spnSelectionLengthPtr[0].setValue(BigDecimal.ONE);
                                    }
                                    spnSelectionX1Ptr[0].setValue(g3[0]);
                                    spnSelectionY1Ptr[0].setValue(g3[1]);
                                    spnSelectionZ1Ptr[0].setValue(g3[2]);
                                    spnSelectionX2Ptr[0].setValue(g3[3]);
                                    spnSelectionY2Ptr[0].setValue(g3[4]);
                                    spnSelectionZ2Ptr[0].setValue(g3[5]);
                                    spnSelectionX3Ptr[0].setValue(g3[6]);
                                    spnSelectionY3Ptr[0].setValue(g3[7]);
                                    spnSelectionZ3Ptr[0].setValue(g3[8]);
                                    break;
                                case 2:
                                    BigDecimal[] g2 = GraphicalDataTools.getPreciseCoordinates(gdata);
                                    if (spnSelectionLengthPtr[0].isEnabled()) {
                                        spnSelectionLengthPtr[0].setValue(((org.nschmidt.ldparteditor.data.GData2) gdata).getLength());
                                    } else {
                                        spnSelectionLengthPtr[0].setValue(BigDecimal.ONE);
                                    }
                                    spnSelectionX1Ptr[0].setValue(g2[0]);
                                    spnSelectionY1Ptr[0].setValue(g2[1]);
                                    spnSelectionZ1Ptr[0].setValue(g2[2]);
                                    spnSelectionX2Ptr[0].setValue(g2[3]);
                                    spnSelectionY2Ptr[0].setValue(g2[4]);
                                    spnSelectionZ2Ptr[0].setValue(g2[5]);
                                    break;
                                case 1:
                                    vm.getSelectedVertices().clear();
                                    btnMoveAdjacentData2Ptr[0].setSelection(false);
                                    btnMoveAdjacentData2Ptr[0].setEnabled(false);
                                    GData1 g1 = (GData1) gdata;
                                    spnSelectionX1Ptr[0].setValue(g1.getAccurateProductMatrix().m30);
                                    spnSelectionY1Ptr[0].setValue(g1.getAccurateProductMatrix().m31);
                                    spnSelectionZ1Ptr[0].setValue(g1.getAccurateProductMatrix().m32);
                                    spnSelectionX2Ptr[0].setValue(g1.getAccurateProductMatrix().m00);
                                    spnSelectionY2Ptr[0].setValue(g1.getAccurateProductMatrix().m01);
                                    spnSelectionZ2Ptr[0].setValue(g1.getAccurateProductMatrix().m02);
                                    spnSelectionX3Ptr[0].setValue(g1.getAccurateProductMatrix().m10);
                                    spnSelectionY3Ptr[0].setValue(g1.getAccurateProductMatrix().m11);
                                    spnSelectionZ3Ptr[0].setValue(g1.getAccurateProductMatrix().m12);
                                    spnSelectionX4Ptr[0].setValue(g1.getAccurateProductMatrix().m20);
                                    spnSelectionY4Ptr[0].setValue(g1.getAccurateProductMatrix().m21);
                                    spnSelectionZ4Ptr[0].setValue(g1.getAccurateProductMatrix().m22);
                                    break;
                                default:
                                    disableSelectionTab();
                                    updatingSelectionTab = true;
                                    break;
                                }

                                lblSelectionAnglePtr[0].setText((gdata.type() != 1 ? I18n.E3D_PROTRACTOR_ANGLE : "") + " {" + spnSelectionAnglePtr[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                lblSelectionLengthPtr[0].setText((gdata.type() != 1 ? I18n.E3D_PROTRACTOR_LENGTH : "") + " {" + spnSelectionLengthPtr[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                lblSelectionX1Ptr[0].setText((gdata.type() != 1 ? I18n.E3D_POSITION_X1 : "X  :") + " {" + spnSelectionX1Ptr[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                lblSelectionY1Ptr[0].setText((gdata.type() != 1 ? I18n.E3D_POSITION_Y1 : "Y  :") + " {" + spnSelectionY1Ptr[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                lblSelectionZ1Ptr[0].setText((gdata.type() != 1 ? I18n.E3D_POSITION_Z1 : "Z  :") + " {" + spnSelectionZ1Ptr[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                lblSelectionX2Ptr[0].setText((gdata.type() != 1 ? I18n.E3D_POSITION_X2 : "M00:") + " {" + spnSelectionX2Ptr[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                lblSelectionY2Ptr[0].setText((gdata.type() != 1 ? I18n.E3D_POSITION_Y2 : "M01:") + " {" + spnSelectionY2Ptr[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                lblSelectionZ2Ptr[0].setText((gdata.type() != 1 ? I18n.E3D_POSITION_Z2 : "M02:") + " {" + spnSelectionZ2Ptr[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                lblSelectionX3Ptr[0].setText((gdata.type() != 1 ? I18n.E3D_POSITION_X3 : "M10:") + " {" + spnSelectionX3Ptr[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                lblSelectionY3Ptr[0].setText((gdata.type() != 1 ? I18n.E3D_POSITION_Y3 : "M11:") + " {" + spnSelectionY3Ptr[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                lblSelectionZ3Ptr[0].setText((gdata.type() != 1 ? I18n.E3D_POSITION_Z3 : "M12:") + " {" + spnSelectionZ3Ptr[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                lblSelectionX4Ptr[0].setText((gdata.type() != 1 ? I18n.E3D_POSITION_X4 : "M20:") + " {" + spnSelectionX4Ptr[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                lblSelectionY4Ptr[0].setText((gdata.type() != 1 ? I18n.E3D_POSITION_Y4 : "M21:") + " {" + spnSelectionY4Ptr[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                lblSelectionZ4Ptr[0].setText((gdata.type() != 1 ? I18n.E3D_POSITION_Z4 : "M22:") + " {" + spnSelectionZ4Ptr[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

                                lblSelectionX1Ptr[0].getParent().layout();
                                break;
                            default:
                                disableSelectionTab();
                                break;
                            }
                        } else {
                            disableSelectionTab();
                        }
                        if (breakIt) break;
                    }
                } else {
                    disableSelectionTab();
                }
            } else {
                disableSelectionTab();
            }
            updatingSelectionTab = false;
            regainFocus();
        });

        final DecimalValueChangeAdapter va = spn -> {
            if (updatingSelectionTab || Project.getFileToEdit() == null) return;
            Project.getFileToEdit().getVertexManager().addSnapshot();
            final GData newLine = Project.getFileToEdit().getVertexManager().updateSelectedLine(
                    spnSelectionX1Ptr[0].getValue(), spnSelectionY1Ptr[0].getValue(), spnSelectionZ1Ptr[0].getValue(),
                    spnSelectionX2Ptr[0].getValue(), spnSelectionY2Ptr[0].getValue(), spnSelectionZ2Ptr[0].getValue(),
                    spnSelectionX3Ptr[0].getValue(), spnSelectionY3Ptr[0].getValue(), spnSelectionZ3Ptr[0].getValue(),
                    spnSelectionX4Ptr[0].getValue(), spnSelectionY4Ptr[0].getValue(), spnSelectionZ4Ptr[0].getValue(),
                    btnMoveAdjacentData2Ptr[0].getSelection()
                    );
            if (newLine == null) {
                disableSelectionTab();
            } else {

                if (newLine.type() == 2 && !((org.nschmidt.ldparteditor.data.GData2) newLine).isLine) {
                    updatingSelectionTab = true;
                    spnSelectionLengthPtr[0].setValue(((org.nschmidt.ldparteditor.data.GData2) newLine).getLength());
                    updatingSelectionTab = false;
                }
                if (newLine.type() == 3 && !((org.nschmidt.ldparteditor.data.GData3) newLine).isTriangle) {
                    updatingSelectionTab = true;
                    spnSelectionAnglePtr[0].setValue(new BigDecimal(((org.nschmidt.ldparteditor.data.GData3) newLine).getProtractorAngle()));
                    spnSelectionLengthPtr[0].setValue(((org.nschmidt.ldparteditor.data.GData3) newLine).getProtractorLength());
                    updatingSelectionTab = false;
                }

                txtLinePtr[0].setText(newLine.toString());
            }
        };

        spnSelectionAnglePtr[0].addValueChangeListener(spn -> {
            if (updatingSelectionTab || Project.getFileToEdit() == null) return;
            VertexManager vm = Project.getFileToEdit().getVertexManager();
            vm.addSnapshot();

            org.nschmidt.ldparteditor.data.GData3 tri = null;
            if (vm.getSelectedTriangles().isEmpty()) {
                disableSelectionTab();
                return;
            }
            tri = vm.getSelectedTriangles().iterator().next();
            if (tri.isTriangle) {
                disableSelectionTab();
                return;
            }

            BigDecimal[] c = ProtractorHelper.changeAngle(spn.getValue().doubleValue(), tri);
            updatingSelectionTab = true;
            spnSelectionX3Ptr[0].setValue(c[0]);
            spnSelectionY3Ptr[0].setValue(c[1]);
            spnSelectionZ3Ptr[0].setValue(c[2]);
            updatingSelectionTab = false;

            final GData newLine = Project.getFileToEdit().getVertexManager().updateSelectedLine(
                    spnSelectionX1Ptr[0].getValue(), spnSelectionY1Ptr[0].getValue(), spnSelectionZ1Ptr[0].getValue(),
                    spnSelectionX2Ptr[0].getValue(), spnSelectionY2Ptr[0].getValue(), spnSelectionZ2Ptr[0].getValue(),
                    c[0], c[1], c[2],
                    spnSelectionX4Ptr[0].getValue(), spnSelectionY4Ptr[0].getValue(), spnSelectionZ4Ptr[0].getValue(),
                    btnMoveAdjacentData2Ptr[0].getSelection()
                    );
            if (newLine == null) {
                disableSelectionTab();
            } else {
                txtLinePtr[0].setText(newLine.toString());
            }
        });

        spnSelectionLengthPtr[0].addValueChangeListener(spn -> {
            if (updatingSelectionTab || Project.getFileToEdit() == null) return;
            VertexManager vm = Project.getFileToEdit().getVertexManager();
            vm.addSnapshot();

            if (!vm.getSelectedLines().isEmpty()) {
                org.nschmidt.ldparteditor.data.GData2 line = null;
                line = vm.getSelectedLines().iterator().next();
                if (line.isLine) {
                    disableSelectionTab();
                    return;
                }

                BigDecimal[] c1 = ProtractorHelper.changeLength(spn.getValue(), line);
                updatingSelectionTab = true;
                spnSelectionX2Ptr[0].setValue(c1[0]);
                spnSelectionY2Ptr[0].setValue(c1[1]);
                spnSelectionZ2Ptr[0].setValue(c1[2]);
                updatingSelectionTab = false;

                final GData newLine1 = Project.getFileToEdit().getVertexManager().updateSelectedLine(
                        spnSelectionX1Ptr[0].getValue(), spnSelectionY1Ptr[0].getValue(), spnSelectionZ1Ptr[0].getValue(),
                        c1[0], c1[1], c1[2],
                        spnSelectionX3Ptr[0].getValue(), spnSelectionY3Ptr[0].getValue(), spnSelectionZ3Ptr[0].getValue(),
                        spnSelectionX4Ptr[0].getValue(), spnSelectionY4Ptr[0].getValue(), spnSelectionZ4Ptr[0].getValue(),
                        btnMoveAdjacentData2Ptr[0].getSelection()
                        );
                if (newLine1 == null) {
                    disableSelectionTab();
                } else {
                    txtLinePtr[0].setText(newLine1.toString());
                }

            } else {

                org.nschmidt.ldparteditor.data.GData3 tri = null;
                if (vm.getSelectedTriangles().isEmpty()) {
                    disableSelectionTab();
                    return;
                }
                tri = vm.getSelectedTriangles().iterator().next();
                if (tri.isTriangle) {
                    disableSelectionTab();
                    return;
                }

                BigDecimal[] c2 = ProtractorHelper.changeLength(spn.getValue(), tri);
                updatingSelectionTab = true;
                spnSelectionX3Ptr[0].setValue(c2[0]);
                spnSelectionY3Ptr[0].setValue(c2[1]);
                spnSelectionZ3Ptr[0].setValue(c2[2]);
                updatingSelectionTab = false;

                final GData newLine2 = Project.getFileToEdit().getVertexManager().updateSelectedLine(
                        spnSelectionX1Ptr[0].getValue(), spnSelectionY1Ptr[0].getValue(), spnSelectionZ1Ptr[0].getValue(),
                        spnSelectionX2Ptr[0].getValue(), spnSelectionY2Ptr[0].getValue(), spnSelectionZ2Ptr[0].getValue(),
                        c2[0], c2[1], c2[2],
                        spnSelectionX4Ptr[0].getValue(), spnSelectionY4Ptr[0].getValue(), spnSelectionZ4Ptr[0].getValue(),
                        btnMoveAdjacentData2Ptr[0].getSelection()
                        );
                if (newLine2 == null) {
                    disableSelectionTab();
                } else {
                    txtLinePtr[0].setText(newLine2.toString());
                }
            }
        });

        spnSelectionX1Ptr[0].addValueChangeListener(va);
        spnSelectionY1Ptr[0].addValueChangeListener(va);
        spnSelectionZ1Ptr[0].addValueChangeListener(va);
        spnSelectionX2Ptr[0].addValueChangeListener(va);
        spnSelectionY2Ptr[0].addValueChangeListener(va);
        spnSelectionZ2Ptr[0].addValueChangeListener(va);
        spnSelectionX3Ptr[0].addValueChangeListener(va);
        spnSelectionY3Ptr[0].addValueChangeListener(va);
        spnSelectionZ3Ptr[0].addValueChangeListener(va);
        spnSelectionX4Ptr[0].addValueChangeListener(va);
        spnSelectionY4Ptr[0].addValueChangeListener(va);
        spnSelectionZ4Ptr[0].addValueChangeListener(va);

        widgetUtil(btnMoveAdjacentData2Ptr[0]).addSelectionListener(e -> regainFocus());
        widgetUtil(btnOpenInTextEditorPtr[0]).addSelectionListener(e -> actionOpenInTextEditor());
        widgetUtil(btnOpenIn3DEditorPtr[0]).addSelectionListener(e -> actionOpenIn3DEditor());
        widgetUtil(btnRevertPtr[0]).addSelectionListener(e -> actionRevert());
        widgetUtil(btnClosePtr[0]).addSelectionListener(e -> actionClose());
        widgetUtil(btnRenamePtr[0]).addSelectionListener(e -> actionRename());
        widgetUtil(btnCopyToUnofficialPtr[0]).addSelectionListener(e -> actionCopyToUnofficial());

        treeParts[0].addMenuDetectListener(new MenuDetectListener() {
            @Override
            public void menuDetected(MenuDetectEvent e) {

                NLogger.debug(getClass(), "Showing context menu."); //$NON-NLS-1$

                try {
                    if (treeParts[0].getTree().getMenu() != null) {
                        treeParts[0].getTree().getMenu().dispose();
                    }
                } catch (Exception ex) {}

                final boolean enabled = treeParts[0].getSelectionCount() > 0  && treeParts[0].getSelection()[0] != null && treeParts[0].getSelection()[0].getData() instanceof DatFile;
                final boolean writable = enabled && !((DatFile) treeParts[0].getSelection()[0].getData()).isReadOnly();
                final boolean isNotUnofficial = enabled && treeParts[0].getSelection()[0].getParentItem().getParentItem() != null && !treeParts[0].getSelection()[0].getParentItem().getParentItem().equals(treeItemUnofficialPtr[0]);

                Menu treeMenu = new Menu(treeParts[0].getTree());
                treeParts[0].getTree().setMenu(treeMenu);
                mnuTreeMenuPtr[0] = treeMenu;

                MenuItem mntmOpenIn3DEditor = new MenuItem(treeMenu, I18n.noBiDirectionalTextStyle());
                mntmOpenIn3DEditorPtr[0] = mntmOpenIn3DEditor;
                mntmOpenIn3DEditor.setEnabled(enabled);
                mntmOpenIn3DEditor.setText(I18n.E3D_OPEN_IN_3D_EDITOR);

                MenuItem mntmOpenInTextEditor = new MenuItem(treeMenu, I18n.noBiDirectionalTextStyle());
                mntmOpenInTextEditorPtr[0] = mntmOpenInTextEditor;
                mntmOpenInTextEditor.setEnabled(enabled);
                mntmOpenInTextEditor.setText(I18n.E3D_OPEN_IN_TEXT_EDITOR);

                new MenuItem(treeMenu, I18n.noBiDirectionalTextStyle() | SWT.SEPARATOR);

                MenuItem mntmClose = new MenuItem(treeMenu, I18n.noBiDirectionalTextStyle());
                mntmClosePtr[0] = mntmClose;
                mntmClose.setEnabled(enabled);
                mntmClose.setText(I18n.E3D_CLOSE);

                new MenuItem(treeMenu, I18n.noBiDirectionalTextStyle() | SWT.SEPARATOR);

                MenuItem mntmRename = new MenuItem(treeMenu, I18n.noBiDirectionalTextStyle());
                mntmRenamePtr[0] = mntmRename;
                mntmRename.setEnabled(enabled && writable);
                mntmRename.setText(I18n.E3D_RENAME_MOVE);

                MenuItem mntmRevert = new MenuItem(treeMenu, I18n.noBiDirectionalTextStyle());
                mntmRevertPtr[0] = mntmRevert;
                mntmRevert.setEnabled(enabled && writable);
                mntmRevert.setText(I18n.E3D_REVERT_ALL_CHANGES);

                new MenuItem(treeMenu, I18n.noBiDirectionalTextStyle() | SWT.SEPARATOR);

                MenuItem mntmCopyToUnofficial = new MenuItem(treeMenu, I18n.noBiDirectionalTextStyle());
                mntmCopyToUnofficialPtr[0] = mntmCopyToUnofficial;
                mntmCopyToUnofficial.setEnabled(enabled && isNotUnofficial);
                mntmCopyToUnofficial.setText(I18n.E3D_COPY_TO_UNOFFICIAL_LIBRARY);

                widgetUtil(mntmOpenInTextEditorPtr[0]).addSelectionListener(e1 -> actionOpenInTextEditor());
                widgetUtil(mntmOpenIn3DEditorPtr[0]).addSelectionListener(e1 -> actionOpenIn3DEditor());
                widgetUtil(mntmRevertPtr[0]).addSelectionListener(e1 -> actionRevert());
                widgetUtil(mntmClosePtr[0]).addSelectionListener(e1 -> actionClose());
                widgetUtil(mntmRenamePtr[0]).addSelectionListener(e1 -> actionRename());
                widgetUtil(mntmCopyToUnofficialPtr[0]).addSelectionListener(e1 -> actionCopyToUnofficial());

                java.awt.Point b = java.awt.MouseInfo.getPointerInfo().getLocation();
                final int x = (int) b.getX();
                final int y = (int) b.getY();

                Menu menu = mnuTreeMenuPtr[0];
                menu.setLocation(x, y);
                menu.setVisible(true);
                regainFocus();
            }
        });

        treeParts[0].addSelectionListener(e -> {
            final boolean enabled = treeParts[0].getSelectionCount() > 0  && treeParts[0].getSelection()[0] != null && treeParts[0].getSelection()[0].getData() instanceof DatFile;
            final boolean writable = enabled && !((DatFile) treeParts[0].getSelection()[0].getData()).isReadOnly();
            final boolean isNotUnofficial = enabled && treeParts[0].getSelection()[0].getParentItem().getParentItem() != null && !treeParts[0].getSelection()[0].getParentItem().getParentItem().equals(treeItemUnofficialPtr[0]);
            btnOpenIn3DEditorPtr[0].setEnabled(enabled);
            btnOpenInTextEditorPtr[0].setEnabled(enabled);
            btnClosePtr[0].setEnabled(enabled);
            btnRenamePtr[0].setEnabled(enabled && writable);
            btnRevertPtr[0].setEnabled(enabled && writable);
            btnCopyToUnofficialPtr[0].setEnabled(enabled && isNotUnofficial);
        });

        treeParts[0].addListener(SWT.MouseDoubleClick, event -> {
            if (treeParts[0].getSelectionCount() == 1 && treeParts[0].getSelection()[0] != null) {
                treeParts[0].getSelection()[0].setVisible(!treeParts[0].getSelection()[0].isVisible());
                TreeItem sel = treeParts[0].getSelection()[0];
                sh.getDisplay().asyncExec(treeParts[0]::build);
                treeParts[0].redraw();
                treeParts[0].update();
                treeParts[0].getTree().select(treeParts[0].getMapInv().get(sel));
            }
            regainFocus();
        });
        txtSearchPtr[0].addModifyListener(e -> search(txtSearchPtr[0].getText()));
        widgetUtil(btnResetSearchPtr[0]).addSelectionListener(e -> {
            txtSearchPtr[0].setText(""); //$NON-NLS-1$
            txtSearchPtr[0].setFocus();
        });
        txtPrimitiveSearchPtr[0].addModifyListener(e -> {
            if (getCompositePrimitive().stopDraw()) {
                txtPrimitiveSearchPtr[0].setText(""); //$NON-NLS-1$
                return;
            } else {
                getCompositePrimitive().disableRefresh();
            }
            getCompositePrimitive().collapseAll();
            List<Primitive> prims = getCompositePrimitive().getPrimitives();
            final String crit = txtPrimitiveSearchPtr[0].getText();
            if (crit.trim().isEmpty()) {
                getCompositePrimitive().setSearchResults(new ArrayList<>());
                Matrix4f.setIdentity(getCompositePrimitive().getTranslation());
                getCompositePrimitive().getOpenGL().drawScene(-1, -1);
                return;
            }
            String criteria = ".*" + crit + ".*"; //$NON-NLS-1$ //$NON-NLS-2$
            try {
                "DUMMY".matches(criteria); //$NON-NLS-1$
            } catch (PatternSyntaxException pe) {
                getCompositePrimitive().setSearchResults(new ArrayList<>());
                Matrix4f.setIdentity(getCompositePrimitive().getTranslation());
                getCompositePrimitive().getOpenGL().drawScene(-1, -1);
                return;
            }
            final Pattern pattern = Pattern.compile(criteria);
            List<Primitive> results = new ArrayList<>();
            for (Primitive p : prims) {
                p.search(pattern, results);
            }
            if (results.isEmpty()) {
                results.add(null);
            }
            getCompositePrimitive().setSearchResults(results);
            Matrix4f.setIdentity(getCompositePrimitive().getTranslation());
            getCompositePrimitive().getOpenGL().drawScene(-1, -1);
        });
        widgetUtil(btnResetPrimitiveSearchPtr[0]).addSelectionListener(e -> {
            txtPrimitiveSearchPtr[0].setText(""); //$NON-NLS-1$
            txtPrimitiveSearchPtr[0].setFocus();
        });
        widgetUtil(btnZoomInPrimitivesPtr[0]).addSelectionListener(e -> {
            getCompositePrimitive().zoomIn();
            getCompositePrimitive().getOpenGL().drawScene(-1, -1);
        });
        widgetUtil(btnZoomOutPrimitivesPtr[0]).addSelectionListener(e -> {
            getCompositePrimitive().zoomOut();
            getCompositePrimitive().getOpenGL().drawScene(-1, -1);
        });
        widgetUtil(btnHidePtr[0]).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null && !Project.getFileToEdit().getVertexManager().getSelectedData().isEmpty()) {
                Project.getFileToEdit().getVertexManager().addSnapshot();
                Project.getFileToEdit().getVertexManager().hideSelection();
                for (EditorTextWindow w : Project.getOpenTextWindows()) {
                    for (CTabItem t : w.getTabFolder().getItems()) {
                        if (Project.getFileToEdit().equals(((CompositeTab) t).getState().getFileNameObj())) {
                            StyledText st = ((CompositeTab) t).getTextComposite();
                            st.redraw(0, 0, st.getBounds().width, st.getBounds().height, true);
                        }
                    }
                }
                Project.getFileToEdit().addHistory();
            }
            regainFocus();
        });
        widgetUtil(btnShowAllPtr[0]).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null) {
                Project.getFileToEdit().getVertexManager().addSnapshot();
                Project.getFileToEdit().getVertexManager().showAll();
                for (EditorTextWindow w : Project.getOpenTextWindows()) {
                    for (CTabItem t : w.getTabFolder().getItems()) {
                        if (Project.getFileToEdit().equals(((CompositeTab) t).getState().getFileNameObj())) {
                            StyledText st = ((CompositeTab) t).getTextComposite();
                            st.redraw(0, 0, st.getBounds().width, st.getBounds().height, true);
                        }
                    }
                }
                Project.getFileToEdit().addHistory();
            }
            regainFocus();
        });
        widgetUtil(btnNoTransparentSelectionPtr[0]).addSelectionListener(e -> {
            setNoTransparentSelection(btnNoTransparentSelectionPtr[0].getSelection());
            regainFocus();
        });
        widgetUtil(btnBFCTogglePtr[0]).addSelectionListener(e -> {
            setBfcToggle(btnBFCTogglePtr[0].getSelection());
            regainFocus();
        });
        widgetUtil(btnInsertAtCursorPositionPtr[0]).addSelectionListener(e -> {
            setInsertingAtCursorPosition(btnInsertAtCursorPositionPtr[0].getSelection());
            regainFocus();
        });

        if (btnManipulatorToOriginPtr[0] != null) widgetUtil(btnManipulatorToOriginPtr[0]).addSelectionListener(e -> mntmManipulatorToOrigin());
        if (btnManipulatorToWorldPtr[0] != null) widgetUtil(btnManipulatorToWorldPtr[0]).addSelectionListener(e -> mntmManipulatorToWorld());
        if (btnManipulatorXReversePtr[0] != null) widgetUtil(btnManipulatorXReversePtr[0]).addSelectionListener(e -> mntmManipulatorXReverse());
        if (btnManipulatorYReversePtr[0] != null) widgetUtil(btnManipulatorYReversePtr[0]).addSelectionListener(e -> mntmManipulatorYReverse());
        if (btnManipulatorZReversePtr[0] != null) widgetUtil(btnManipulatorZReversePtr[0]).addSelectionListener(e -> mntmManipulatorZReverse());
        if (btnManipulatorSwitchXYPtr[0] != null) widgetUtil(btnManipulatorSwitchXYPtr[0]).addSelectionListener(e -> mntmManipulatorSwitchXY());
        if (btnManipulatorSwitchXZPtr[0] != null) widgetUtil(btnManipulatorSwitchXZPtr[0]).addSelectionListener(e -> mntmManipulatorSwitchXZ());
        if (btnManipulatorSwitchYZPtr[0] != null) widgetUtil(btnManipulatorSwitchYZPtr[0]).addSelectionListener(e -> mntmManipulatorSwitchYZ());
        if (btnManipulatorCameraToPosPtr[0] != null) widgetUtil(btnManipulatorCameraToPosPtr[0]).addSelectionListener(e -> mntmManipulatorCameraToPos());
        if (btnManipulatorToAveragePtr[0] != null) widgetUtil(btnManipulatorToAveragePtr[0]).addSelectionListener(e -> mntmManipulatorToAverage());
        if (btnManipulatorToSubfilePtr[0] != null) widgetUtil(btnManipulatorToSubfilePtr[0]).addSelectionListener(e -> mntmManipulatorToSubfile());
        if (btnManipulatorSubfileToPtr[0] != null) widgetUtil(btnManipulatorSubfileToPtr[0]).addSelectionListener(e -> mntmManipulatorSubfileTo(Cocoa.checkCtrlOrCmdPressed(e.stateMask)));
        if (btnManipulatorToVertexPtr[0] != null) widgetUtil(btnManipulatorToVertexPtr[0]).addSelectionListener(e -> mntmManipulatorToVertex());
        if (btnManipulatorToEdgePtr[0] != null) widgetUtil(btnManipulatorToEdgePtr[0]).addSelectionListener(e -> mntmManipulatorToEdge());
        if (btnManipulatorToSurfacePtr[0] != null) widgetUtil(btnManipulatorToSurfacePtr[0]).addSelectionListener(e -> mntmManipulatorToSurface());
        if (btnManipulatorToVertexNormalPtr[0] != null) widgetUtil(btnManipulatorToVertexNormalPtr[0]).addSelectionListener(e -> mntmManipulatorToVertexNormal());
        if (btnManipulatorToEdgeNormalPtr[0] != null) widgetUtil(btnManipulatorToEdgeNormalPtr[0]).addSelectionListener(e -> mntmManipulatorToEdgeNormal());
        if (btnManipulatorToSurfaceNormalPtr[0] != null) widgetUtil(btnManipulatorToSurfaceNormalPtr[0]).addSelectionListener(e -> mntmManipulatorToSurfaceNormal());
        if (btnManipulatorAdjustRotationCenterPtr[0] != null) widgetUtil(btnManipulatorAdjustRotationCenterPtr[0]).addSelectionListener(e -> mntmManipulatorAdjustRotationCenter());
        if (btnManipulatorToVertexPositionPtr[0] != null) widgetUtil(btnManipulatorToVertexPositionPtr[0]).addSelectionListener(e -> mntmManipulatorToVertexPosition());
        if (mntmManipulatorToOriginPtr[0] != null) widgetUtil(mntmManipulatorToOriginPtr[0]).addSelectionListener(e -> mntmManipulatorToOrigin());
        if (mntmManipulatorToWorldPtr[0] != null) widgetUtil(mntmManipulatorToWorldPtr[0]).addSelectionListener(e -> mntmManipulatorToWorld());
        if (mntmManipulatorXReversePtr[0] != null) widgetUtil(mntmManipulatorXReversePtr[0]).addSelectionListener(e -> mntmManipulatorXReverse());
        if (mntmManipulatorYReversePtr[0] != null) widgetUtil(mntmManipulatorYReversePtr[0]).addSelectionListener(e -> mntmManipulatorYReverse());
        if (mntmManipulatorZReversePtr[0] != null) widgetUtil(mntmManipulatorZReversePtr[0]).addSelectionListener(e -> mntmManipulatorZReverse());
        if (mntmManipulatorSwitchXYPtr[0] != null) widgetUtil(mntmManipulatorSwitchXYPtr[0]).addSelectionListener(e -> mntmManipulatorSwitchXY());
        if (mntmManipulatorSwitchXZPtr[0] != null) widgetUtil(mntmManipulatorSwitchXZPtr[0]).addSelectionListener(e -> mntmManipulatorSwitchXZ());
        if (mntmManipulatorSwitchYZPtr[0] != null) widgetUtil(mntmManipulatorSwitchYZPtr[0]).addSelectionListener(e -> mntmManipulatorSwitchYZ());
        if (mntmManipulatorCameraToPosPtr[0] != null) widgetUtil(mntmManipulatorCameraToPosPtr[0]).addSelectionListener(e -> mntmManipulatorCameraToPos());
        if (mntmManipulatorToAveragePtr[0] != null) widgetUtil(mntmManipulatorToAveragePtr[0]).addSelectionListener(e -> mntmManipulatorToAverage());
        if (mntmManipulatorToSubfilePtr[0] != null) widgetUtil(mntmManipulatorToSubfilePtr[0]).addSelectionListener(e -> mntmManipulatorToSubfile());
        if (mntmManipulatorSubfileToPtr[0] != null) widgetUtil(mntmManipulatorSubfileToPtr[0]).addSelectionListener(e -> mntmManipulatorSubfileTo(Cocoa.checkCtrlOrCmdPressed(e.stateMask)));
        if (mntmManipulatorToVertexPtr[0] != null) widgetUtil(mntmManipulatorToVertexPtr[0]).addSelectionListener(e -> mntmManipulatorToVertex());
        if (mntmManipulatorToEdgePtr[0] != null) widgetUtil(mntmManipulatorToEdgePtr[0]).addSelectionListener(e -> mntmManipulatorToEdge());
        if (mntmManipulatorToSurfacePtr[0] != null) widgetUtil(mntmManipulatorToSurfacePtr[0]).addSelectionListener(e -> mntmManipulatorToSurface());
        if (mntmManipulatorToVertexNormalPtr[0] != null) widgetUtil(mntmManipulatorToVertexNormalPtr[0]).addSelectionListener(e -> mntmManipulatorToVertexNormal());
        if (mntmManipulatorToEdgeNormalPtr[0] != null) widgetUtil(mntmManipulatorToEdgeNormalPtr[0]).addSelectionListener(e -> mntmManipulatorToEdgeNormal());
        if (mntmManipulatorToSurfaceNormalPtr[0] != null) widgetUtil(mntmManipulatorToSurfaceNormalPtr[0]).addSelectionListener(e -> mntmManipulatorToSurfaceNormal());
        if (mntmManipulatorAdjustRotationCenterPtr[0] != null) widgetUtil(mntmManipulatorAdjustRotationCenterPtr[0]).addSelectionListener(e -> mntmManipulatorAdjustRotationCenter());
        if (mntmManipulatorToVertexPositionPtr[0] != null) widgetUtil(mntmManipulatorToVertexPositionPtr[0]).addSelectionListener(e -> mntmManipulatorToVertexPosition());

        // MARK Background PNG
        widgetUtil(btnPngFocusPtr[0]).addSelectionListener(e -> {
            Composite3D c3d = null;
            for (OpenGLRenderer renderer : renders) {
                c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                    c3d = c3d.getLockableDatFileReference().getLastSelectedComposite();
                    if (c3d == null) {
                        c3d = renderer.getC3D();
                    }
                    break;
                }
            }

            if (c3d == null) {
                regainFocus();
                return;
            }

            c3d.setClassicPerspective(false);
            WidgetSelectionHelper.unselectAllChildButtons(c3d.getViewAnglesMenu());

            VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
            GDataPNG png = vm.getSelectedBgPicture();
            if (png == null) {
                if (c3d.getLockableDatFileReference().hasNoBackgroundPictures()) {
                    vm.addBackgroundPicture("", new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new Vertex(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE), Project.getProjectPath() + File.separator + ".png");  //$NON-NLS-1$ //$NON-NLS-2$
                    vm.setModified(true, true);
                }
                vm.setSelectedBgPicture(c3d.getLockableDatFileReference().getBackgroundPicture(0));
                png = vm.getSelectedBgPicture();
                updateBgPictureTab();
            }


            Matrix4f tMatrix = new Matrix4f();
            tMatrix.setIdentity();
            tMatrix = tMatrix.scale(new Vector3f(png.scale.x, png.scale.y, png.scale.z));

            Matrix4f dMatrix = new Matrix4f();
            dMatrix.setIdentity();

            Matrix4f.rotate((float) (png.angleB.doubleValue() / 180.0 * Math.PI), new Vector3f(1f, 0f, 0f), dMatrix, dMatrix);
            Matrix4f.rotate((float) (png.angleA.doubleValue() / 180.0 * Math.PI), new Vector3f(0f, 1f, 0f), dMatrix, dMatrix);

            Matrix4f.mul(dMatrix, tMatrix, tMatrix);

            Vector4f vx = Matrix4f.transform(dMatrix, new Vector4f(png.offset.x, 0f, 0f, 1f), null);
            Vector4f vy = Matrix4f.transform(dMatrix, new Vector4f(0f, png.offset.y, 0f, 1f), null);
            Vector4f vz = Matrix4f.transform(dMatrix, new Vector4f(0f, 0f, png.offset.z, 1f), null);

            Matrix4f transMatrix = new Matrix4f();
            transMatrix.setIdentity();
            transMatrix.m30 = -vx.x;
            transMatrix.m31 = -vx.y;
            transMatrix.m32 = -vx.z;
            transMatrix.m30 -= vy.x;
            transMatrix.m31 -= vy.y;
            transMatrix.m32 -= vy.z;
            transMatrix.m30 -= vz.x;
            transMatrix.m31 -= vz.y;
            transMatrix.m32 -= vz.z;

            Matrix4f rotMatrixD = new Matrix4f();
            rotMatrixD.setIdentity();

            Matrix4f.rotate((float) (png.angleB.doubleValue() / 180.0 * Math.PI), new Vector3f(1f, 0f, 0f), rotMatrixD, rotMatrixD);
            Matrix4f.rotate((float) (png.angleA.doubleValue() / 180.0 * Math.PI), new Vector3f(0f, 1f, 0f), rotMatrixD, rotMatrixD);

            rotMatrixD = rotMatrixD.scale(new Vector3f(-1f, 1f, -1f));
            rotMatrixD.invert();


            c3d.getRotation().load(rotMatrixD);
            c3d.getTranslation().load(transMatrix);
            c3d.getPerspectiveCalculator().calculateOriginData();

            vm.setSelectedBgPicture(c3d.getLockableDatFileReference().getBackgroundPicture(vm.getSelectedBgPictureIndex()));
            regainFocus();
        });
        widgetUtil(btnPngImagePtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : renders) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !Project.getFileToEdit().isReadOnly()) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    GDataPNG png = vm.getSelectedBgPicture();
                    if (updatingPngPictureTab) return;
                    if (png == null) {
                        if (c3d.getLockableDatFileReference().hasNoBackgroundPictures()) {
                            for (OpenGLRenderer renderer21 : renders) {
                                renderer21.disposeAllTextures();
                            }
                            vm.addBackgroundPicture("", new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new Vertex(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE), Project.getProjectPath() + File.separator + ".png");  //$NON-NLS-1$ //$NON-NLS-2$
                            vm.setModified(true, true);
                        }
                        vm.setSelectedBgPicture(c3d.getLockableDatFileReference().getBackgroundPicture(0));
                        png = vm.getSelectedBgPicture();
                        updateBgPictureTab();
                    }

                    FileDialog fd = new FileDialog(getShell(), SWT.SAVE);
                    fd.setText(I18n.E3D_OPEN_PNG_IMAGE);
                    try {
                        File f = new File(png.texturePath);
                        fd.setFilterPath(f.getParent());
                        fd.setFileName(f.getName());
                    } catch (Exception ex) {

                    }

                    String[] filterExt = { "*.png", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
                    fd.setFilterExtensions(filterExt);
                    String[] filterNames = { I18n.E3D_PORTABLE_NETWORK_GRAPHICS, I18n.E3D_ALL_FILES};
                    fd.setFilterNames(filterNames);
                    String texturePath = fd.open();

                    if (texturePath != null) {

                        String newText = png.getString(png.offset, png.angleA, png.angleB, png.angleC, png.scale, texturePath);

                        GDataPNG newPngPicture = new GDataPNG(newText, png.offset, png.angleA, png.angleB, png.angleC, png.scale, texturePath, View.DUMMY_REFERENCE);
                        replaceBgPicture(png, newPngPicture, c3d.getLockableDatFileReference());

                        pngPictureUpdateCounter++;
                        if (pngPictureUpdateCounter > 3) {
                            for (OpenGLRenderer renderer22 : renders) {
                                renderer22.disposeOldTextures();
                            }
                            pngPictureUpdateCounter = 0;
                        }

                        vm.setModified(true, true);
                        vm.setSelectedBgPicture(c3d.getLockableDatFileReference().getBackgroundPicture(vm.getSelectedBgPictureIndex()));
                    }

                    return;
                }
            }
        });
        widgetUtil(btnPngNextPtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : renders) {
                Composite3D c3d = renderer.getC3D();
                DatFile df = c3d.getLockableDatFileReference();
                if (df.equals(Project.getFileToEdit()) && !Project.getFileToEdit().isReadOnly()) {
                    VertexManager vm = df.getVertexManager();
                    GDataPNG sp = vm.getSelectedBgPicture();
                    boolean noBgPictures = df.hasNoBackgroundPictures();
                    vm.setSelectedBgPictureIndex(vm.getSelectedBgPictureIndex() + 1);
                    boolean indexOutOfBounds = vm.getSelectedBgPictureIndex() >= df.getBackgroundPictureCount();
                    boolean noRealData = df.getDrawPerLineNoClone().getKey(sp) == null;
                    if (noBgPictures) {
                        for (OpenGLRenderer renderer2 : renders) {
                            renderer2.disposeAllTextures();
                        }
                        vm.addBackgroundPicture("", new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new Vertex(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE), Project.getProjectPath() + File.separator + ".png");  //$NON-NLS-1$ //$NON-NLS-2$
                        vm.setModified(true, true);
                        vm.setSelectedBgPicture(c3d.getLockableDatFileReference().getBackgroundPicture(0));
                    } else {
                        if (indexOutOfBounds) vm.setSelectedBgPictureIndex(0);
                        if (noRealData) {
                            vm.setSelectedBgPictureIndex(0);
                            vm.setSelectedBgPicture(df.getBackgroundPicture(0));
                        } else {
                            vm.setSelectedBgPicture(df.getBackgroundPicture(vm.getSelectedBgPictureIndex()));
                        }
                    }
                    updateBgPictureTab();
                    break;
                }
            }
            regainFocus();
        });
        widgetUtil(btnPngPreviousPtr[0]).addSelectionListener(e -> {
            for (OpenGLRenderer renderer : renders) {
                Composite3D c3d = renderer.getC3D();
                DatFile df = c3d.getLockableDatFileReference();
                if (df.equals(Project.getFileToEdit()) && !Project.getFileToEdit().isReadOnly()) {
                    VertexManager vm = df.getVertexManager();
                    GDataPNG sp = vm.getSelectedBgPicture();
                    boolean noBgPictures = df.hasNoBackgroundPictures();
                    vm.setSelectedBgPictureIndex(vm.getSelectedBgPictureIndex() - 1);
                    boolean indexOutOfBounds = vm.getSelectedBgPictureIndex() < 0;
                    boolean noRealData = df.getDrawPerLineNoClone().getKey(sp) == null;
                    if (noBgPictures) {
                        for (OpenGLRenderer renderer2 : renders) {
                            renderer2.disposeAllTextures();
                        }
                        vm.addBackgroundPicture("", new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new Vertex(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE), Project.getProjectPath() + File.separator + ".png");  //$NON-NLS-1$ //$NON-NLS-2$
                        vm.setModified(true, true);
                        vm.setSelectedBgPicture(c3d.getLockableDatFileReference().getBackgroundPicture(0));
                    } else {
                        if (indexOutOfBounds) vm.setSelectedBgPictureIndex(df.getBackgroundPictureCount() - 1);
                        if (noRealData) {
                            vm.setSelectedBgPictureIndex(0);
                            vm.setSelectedBgPicture(df.getBackgroundPicture(0));
                        } else {
                            vm.setSelectedBgPicture(df.getBackgroundPicture(vm.getSelectedBgPictureIndex()));
                        }
                    }
                    updateBgPictureTab();
                    break;
                }
            }
            regainFocus();
        });
        spnPngA1Ptr[0].addValueChangeListener(spn -> {
            for (OpenGLRenderer renderer : renders) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !Project.getFileToEdit().isReadOnly()) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    GDataPNG png = vm.getSelectedBgPicture();
                    if (updatingPngPictureTab) return;
                    if (png == null) {
                        if (c3d.getLockableDatFileReference().hasNoBackgroundPictures()) {
                            for (OpenGLRenderer renderer21 : renders) {
                                renderer21.disposeAllTextures();
                            }
                            vm.addBackgroundPicture("", new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new Vertex(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE), Project.getProjectPath() + File.separator + ".png");  //$NON-NLS-1$ //$NON-NLS-2$
                            vm.setModified(true, true);
                        }
                        vm.setSelectedBgPicture(c3d.getLockableDatFileReference().getBackgroundPicture(0));
                        png = vm.getSelectedBgPicture();
                        updateBgPictureTab();
                    }



                    String newText = png.getString(png.offset, spn.getValue(), png.angleB, png.angleC, png.scale, png.texturePath);

                    GDataPNG newPngPicture = new GDataPNG(newText, png.offset, spn.getValue(), png.angleB, png.angleC, png.scale, png.texturePath, View.DUMMY_REFERENCE);
                    replaceBgPicture(png, newPngPicture, c3d.getLockableDatFileReference());

                    pngPictureUpdateCounter++;
                    if (pngPictureUpdateCounter > 3) {
                        for (OpenGLRenderer renderer22 : renders) {
                            renderer22.disposeOldTextures();
                        }
                        pngPictureUpdateCounter = 0;
                    }

                    vm.setModified(true, true);
                    vm.setSelectedBgPicture(c3d.getLockableDatFileReference().getBackgroundPicture(vm.getSelectedBgPictureIndex()));
                    return;
                }
            }
        });
        spnPngA2Ptr[0].addValueChangeListener(spn -> {
            for (OpenGLRenderer renderer : renders) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !Project.getFileToEdit().isReadOnly()) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    GDataPNG png = vm.getSelectedBgPicture();
                    if (updatingPngPictureTab) return;
                    if (png == null) {
                        if (c3d.getLockableDatFileReference().hasNoBackgroundPictures()) {
                            for (OpenGLRenderer renderer21 : renders) {
                                renderer21.disposeAllTextures();
                            }
                            vm.addBackgroundPicture("", new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new Vertex(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE), Project.getProjectPath() + File.separator + ".png");  //$NON-NLS-1$ //$NON-NLS-2$
                            vm.setModified(true, true);
                        }
                        vm.setSelectedBgPicture(c3d.getLockableDatFileReference().getBackgroundPicture(0));
                        png = vm.getSelectedBgPicture();
                        updateBgPictureTab();
                    }

                    String newText = png.getString(png.offset, png.angleA, spn.getValue(), png.angleC, png.scale, png.texturePath);

                    GDataPNG newPngPicture = new GDataPNG(newText, png.offset, png.angleA, spn.getValue(), png.angleC, png.scale, png.texturePath, View.DUMMY_REFERENCE);
                    replaceBgPicture(png, newPngPicture, c3d.getLockableDatFileReference());

                    pngPictureUpdateCounter++;
                    if (pngPictureUpdateCounter > 3) {
                        for (OpenGLRenderer renderer22 : renders) {
                            renderer22.disposeOldTextures();
                        }
                        pngPictureUpdateCounter = 0;
                    }

                    vm.setModified(true, true);
                    vm.setSelectedBgPicture(c3d.getLockableDatFileReference().getBackgroundPicture(vm.getSelectedBgPictureIndex()));
                    return;
                }
            }
        });
        spnPngA3Ptr[0].addValueChangeListener(spn -> {
            for (OpenGLRenderer renderer : renders) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !Project.getFileToEdit().isReadOnly()) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    GDataPNG png = vm.getSelectedBgPicture();
                    if (updatingPngPictureTab) return;
                    if (png == null) {
                        if (c3d.getLockableDatFileReference().hasNoBackgroundPictures()) {
                            for (OpenGLRenderer renderer21 : renders) {
                                renderer21.disposeAllTextures();
                            }
                            vm.addBackgroundPicture("", new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new Vertex(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE), Project.getProjectPath() + File.separator + ".png");  //$NON-NLS-1$ //$NON-NLS-2$
                            vm.setModified(true, true);
                        }
                        vm.setSelectedBgPicture(c3d.getLockableDatFileReference().getBackgroundPicture(0));
                        png = vm.getSelectedBgPicture();
                        updateBgPictureTab();
                    }

                    String newText = png.getString(png.offset, png.angleA, png.angleB, spn.getValue(), png.scale, png.texturePath);

                    GDataPNG newPngPicture = new GDataPNG(newText, png.offset, png.angleA, png.angleB, spn.getValue(), png.scale, png.texturePath, View.DUMMY_REFERENCE);
                    replaceBgPicture(png, newPngPicture, c3d.getLockableDatFileReference());

                    pngPictureUpdateCounter++;
                    if (pngPictureUpdateCounter > 3) {
                        for (OpenGLRenderer renderer22 : renders) {
                            renderer22.disposeOldTextures();
                        }
                        pngPictureUpdateCounter = 0;
                    }

                    vm.setModified(true, true);
                    vm.setSelectedBgPicture(c3d.getLockableDatFileReference().getBackgroundPicture(vm.getSelectedBgPictureIndex()));
                    return;
                }
            }
        });
        spnPngSXPtr[0].addValueChangeListener(spn -> {
            for (OpenGLRenderer renderer : renders) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !Project.getFileToEdit().isReadOnly()) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    GDataPNG png = vm.getSelectedBgPicture();
                    if (updatingPngPictureTab) return;
                    if (png == null) {
                        if (c3d.getLockableDatFileReference().hasNoBackgroundPictures()) {
                            for (OpenGLRenderer renderer21 : renders) {
                                renderer21.disposeAllTextures();
                            }
                            vm.addBackgroundPicture("", new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new Vertex(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE), Project.getProjectPath() + File.separator + ".png");  //$NON-NLS-1$ //$NON-NLS-2$
                            vm.setModified(true, true);
                        }
                        vm.setSelectedBgPicture(c3d.getLockableDatFileReference().getBackgroundPicture(0));
                        png = vm.getSelectedBgPicture();
                        updateBgPictureTab();
                    }

                    Vertex newScale = new Vertex(spn.getValue(), png.scale.yp, png.scale.zp);
                    String newText = png.getString(png.offset, png.angleA, png.angleB, png.angleC, newScale, png.texturePath);

                    GDataPNG newPngPicture = new GDataPNG(newText, png.offset, png.angleA, png.angleB, png.angleC, newScale, png.texturePath, View.DUMMY_REFERENCE);
                    replaceBgPicture(png, newPngPicture, c3d.getLockableDatFileReference());

                    pngPictureUpdateCounter++;
                    if (pngPictureUpdateCounter > 3) {
                        for (OpenGLRenderer renderer22 : renders) {
                            renderer22.disposeOldTextures();
                        }
                        pngPictureUpdateCounter = 0;
                    }

                    vm.setModified(true, true);
                    vm.setSelectedBgPicture(c3d.getLockableDatFileReference().getBackgroundPicture(vm.getSelectedBgPictureIndex()));
                    return;
                }
            }
        });
        spnPngSYPtr[0].addValueChangeListener(spn -> {
            for (OpenGLRenderer renderer : renders) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !Project.getFileToEdit().isReadOnly()) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    GDataPNG png = vm.getSelectedBgPicture();
                    if (updatingPngPictureTab) return;
                    if (png == null) {
                        if (c3d.getLockableDatFileReference().hasNoBackgroundPictures()) {
                            for (OpenGLRenderer renderer2 : renders) {
                                renderer2.disposeAllTextures();
                            }
                            vm.addBackgroundPicture("", new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new Vertex(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE), Project.getProjectPath() + File.separator + ".png");  //$NON-NLS-1$ //$NON-NLS-2$
                            vm.setModified(true, true);
                        }
                        vm.setSelectedBgPicture(c3d.getLockableDatFileReference().getBackgroundPicture(0));
                        png = vm.getSelectedBgPicture();
                        updateBgPictureTab();
                    }

                    Vertex newScale = new Vertex(png.scale.xp, spn.getValue(), png.scale.zp);
                    String newText = png.getString(png.offset, png.angleA, png.angleB, png.angleC, newScale, png.texturePath);

                    GDataPNG newPngPicture = new GDataPNG(newText, png.offset, png.angleA, png.angleB, png.angleC, newScale, png.texturePath, View.DUMMY_REFERENCE);
                    replaceBgPicture(png, newPngPicture, c3d.getLockableDatFileReference());

                    vm.setModified(true, true);
                    vm.setSelectedBgPicture(c3d.getLockableDatFileReference().getBackgroundPicture(vm.getSelectedBgPictureIndex()));
                    return;
                }
            }
        });
        spnPngXPtr[0].addValueChangeListener(spn -> {
            for (OpenGLRenderer renderer : renders) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !Project.getFileToEdit().isReadOnly()) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    GDataPNG png = vm.getSelectedBgPicture();
                    if (updatingPngPictureTab) return;
                    if (png == null) {
                        if (c3d.getLockableDatFileReference().hasNoBackgroundPictures()) {
                            for (OpenGLRenderer renderer21 : renders) {
                                renderer21.disposeAllTextures();
                            }
                            vm.addBackgroundPicture("", new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new Vertex(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE), Project.getProjectPath() + File.separator + ".png");  //$NON-NLS-1$ //$NON-NLS-2$
                            vm.setModified(true, true);
                        }
                        vm.setSelectedBgPicture(c3d.getLockableDatFileReference().getBackgroundPicture(0));
                        png = vm.getSelectedBgPicture();
                        updateBgPictureTab();
                    }


                    Vertex newOffset = new Vertex(spn.getValue(), png.offset.yp, png.offset.zp);
                    String newText = png.getString(newOffset, png.angleA, png.angleB, png.angleC, png.scale, png.texturePath);

                    GDataPNG newPngPicture = new GDataPNG(newText, newOffset, png.angleA, png.angleB, png.angleC, png.scale, png.texturePath, View.DUMMY_REFERENCE);
                    replaceBgPicture(png, newPngPicture, c3d.getLockableDatFileReference());

                    pngPictureUpdateCounter++;
                    if (pngPictureUpdateCounter > 3) {
                        for (OpenGLRenderer renderer22 : renders) {
                            renderer22.disposeOldTextures();
                        }
                        pngPictureUpdateCounter = 0;
                    }

                    vm.setModified(true, true);
                    vm.setSelectedBgPicture(c3d.getLockableDatFileReference().getBackgroundPicture(vm.getSelectedBgPictureIndex()));
                    return;
                }
            }
        });
        spnPngYPtr[0].addValueChangeListener(spn -> {
            for (OpenGLRenderer renderer : renders) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !Project.getFileToEdit().isReadOnly()) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    GDataPNG png = vm.getSelectedBgPicture();
                    if (updatingPngPictureTab) return;
                    if (png == null) {
                        if (c3d.getLockableDatFileReference().hasNoBackgroundPictures()) {
                            for (OpenGLRenderer renderer21 : renders) {
                                renderer21.disposeAllTextures();
                            }
                            vm.addBackgroundPicture("", new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new Vertex(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE), Project.getProjectPath() + File.separator + ".png");  //$NON-NLS-1$ //$NON-NLS-2$
                            vm.setModified(true, true);
                        }
                        vm.setSelectedBgPicture(c3d.getLockableDatFileReference().getBackgroundPicture(0));
                        png = vm.getSelectedBgPicture();
                        updateBgPictureTab();
                    }

                    Vertex newOffset = new Vertex(png.offset.xp, spn.getValue(), png.offset.zp);
                    String newText = png.getString(newOffset, png.angleA, png.angleB, png.angleC, png.scale, png.texturePath);

                    GDataPNG newPngPicture = new GDataPNG(newText, newOffset, png.angleA, png.angleB, png.angleC, png.scale, png.texturePath, View.DUMMY_REFERENCE);
                    replaceBgPicture(png, newPngPicture, c3d.getLockableDatFileReference());

                    pngPictureUpdateCounter++;
                    if (pngPictureUpdateCounter > 3) {
                        for (OpenGLRenderer renderer22 : renders) {
                            renderer22.disposeOldTextures();
                        }
                        pngPictureUpdateCounter = 0;
                    }

                    vm.setModified(true, true);
                    vm.setSelectedBgPicture(c3d.getLockableDatFileReference().getBackgroundPicture(vm.getSelectedBgPictureIndex()));
                    return;
                }
            }
        });
        spnPngZPtr[0].addValueChangeListener(spn -> {
            for (OpenGLRenderer renderer : renders) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !Project.getFileToEdit().isReadOnly()) {
                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                    GDataPNG png = vm.getSelectedBgPicture();
                    if (updatingPngPictureTab) return;
                    if (png == null) {
                        if (c3d.getLockableDatFileReference().hasNoBackgroundPictures()) {
                            for (OpenGLRenderer renderer21 : renders) {
                                renderer21.disposeAllTextures();
                            }
                            vm.addBackgroundPicture("", new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new Vertex(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE), Project.getProjectPath() + File.separator + ".png");  //$NON-NLS-1$ //$NON-NLS-2$
                            vm.setModified(true, true);
                        }
                        vm.setSelectedBgPicture(c3d.getLockableDatFileReference().getBackgroundPicture(0));
                        png = vm.getSelectedBgPicture();
                        updateBgPictureTab();
                    }

                    Vertex newOffset = new Vertex(png.offset.xp, png.offset.yp, spn.getValue());
                    String newText = png.getString(newOffset, png.angleA, png.angleB, png.angleC, png.scale, png.texturePath);

                    GDataPNG newPngPicture = new GDataPNG(newText, newOffset, png.angleA, png.angleB, png.angleC, png.scale, png.texturePath, View.DUMMY_REFERENCE);
                    replaceBgPicture(png, newPngPicture, c3d.getLockableDatFileReference());

                    pngPictureUpdateCounter++;
                    if (pngPictureUpdateCounter > 3) {
                        for (OpenGLRenderer renderer22 : renders) {
                            renderer22.disposeOldTextures();
                        }
                        pngPictureUpdateCounter = 0;
                    }

                    vm.setModified(true, true);
                    vm.setSelectedBgPicture(c3d.getLockableDatFileReference().getBackgroundPicture(vm.getSelectedBgPictureIndex()));
                    return;
                }
            }
        });

        Project.createDefault();
        treeItemProjectPtr[0].setData(Project.getProjectPath());
        treeItemOfficialPtr[0].setData(WorkbenchManager.getUserSettingState().getLdrawFolderPath());
        treeItemUnofficialPtr[0].setData(WorkbenchManager.getUserSettingState().getUnofficialFolderPath());

        try {
            new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, false, new IRunnableWithProgress() {
                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask(I18n.E3D_LOADING_LIBRARY, IProgressMonitor.UNKNOWN);
                    Display.getDefault().asyncExec(() -> {
                        LibraryManager.readUnofficialParts(treeItemUnofficialPartsPtr[0]);
                        LibraryManager.readUnofficialSubparts(treeItemUnofficialSubpartsPtr[0]);
                        LibraryManager.readUnofficialPrimitives(treeItemUnofficialPrimitivesPtr[0]);
                        LibraryManager.readUnofficialHiResPrimitives(treeItemUnofficialPrimitives48Ptr[0]);
                        LibraryManager.readUnofficialLowResPrimitives(treeItemUnofficialPrimitives8Ptr[0]);
                        LibraryManager.readOfficialParts(treeItemOfficialPartsPtr[0]);
                        LibraryManager.readOfficialSubparts(treeItemOfficialSubpartsPtr[0]);
                        LibraryManager.readOfficialPrimitives(treeItemOfficialPrimitivesPtr[0]);
                        LibraryManager.readOfficialHiResPrimitives(treeItemOfficialPrimitives48Ptr[0]);
                        LibraryManager.readOfficialLowResPrimitives(treeItemOfficialPrimitives8Ptr[0]);
                    });
                }
            });
        } catch (InvocationTargetException consumed) {
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new LDPartEditorException(ie);
        }

        tabFolderOpenDatFilesPtr[0].getItem(0).setData(View.DUMMY_DATFILE);
        tabFolderOpenDatFilesPtr[0].getItem(1).setData(Project.getFileToEdit());
        Project.addOpenedFile(Project.getFileToEdit());

        tabFolderOpenDatFilesPtr[0].addCTabFolder2Listener(new CTabFolder2Listener() {

            @Override
            public void showList(CTabFolderEvent event) {
                // Implementation is not required.
            }

            @Override
            public void restore(CTabFolderEvent event) {
                // Implementation is not required.
            }

            @Override
            public void minimize(CTabFolderEvent event) {
                // Implementation is not required.
            }

            @Override
            public void maximize(CTabFolderEvent event) {
                // Implementation is not required.
            }

            @Override
            public void close(CTabFolderEvent event) {
                DatFile df = null;
                if (event.item != null && (df = (DatFile) event.item.getData()) != null) {
                    if (df.equals(View.DUMMY_DATFILE)) {
                        event.doit = false;
                    } else {
                        event.item.dispose();
                        Project.removeOpenedFile(df);
                        if (!closeDatfile(df)) {
                            Project.addOpenedFile(df);
                            updateTabs();
                        }
                        Editor3DWindow.getWindow().getShell().forceFocus();
                        regainFocus();
                    }
                }
            }
        });

        widgetUtil(tabFolderOpenDatFilesPtr[0]).addSelectionListener(e -> {

            if (Editor3DWindow.getNoSyncDeadlock().compareAndSet(false, true)) {
                if (tabFolderOpenDatFilesPtr[0].getData() != null) {
                    Editor3DWindow.getNoSyncDeadlock().set(false);
                    return;
                }
                DatFile df = null;
                if (tabFolderOpenDatFilesPtr[0].getSelection() != null && (df = (DatFile) tabFolderOpenDatFilesPtr[0].getSelection().getData()) != null) {

                    openFileIn3DEditor(df);
                    if (!df.equals(View.DUMMY_DATFILE) && WorkbenchManager.getUserSettingState().isSyncingTabs()) {
                        boolean fileIsOpenInTextEditor = false;
                        for (EditorTextWindow w1 : Project.getOpenTextWindows()) {
                            for (CTabItem t1 : w1.getTabFolder().getItems()) {
                                if (df.equals(((CompositeTab) t1).getState().getFileNameObj())) {
                                    fileIsOpenInTextEditor = true;
                                }
                                if (fileIsOpenInTextEditor) break;
                            }
                            if (fileIsOpenInTextEditor) break;
                        }
                        if (fileIsOpenInTextEditor) {
                            for (EditorTextWindow w2 : Project.getOpenTextWindows()) {
                                for (final CTabItem t2 : w2.getTabFolder().getItems()) {
                                    if (df.equals(((CompositeTab) t2).getState().getFileNameObj())) {
                                        w2.getTabFolder().setSelection(t2);
                                        ((CompositeTab) t2).getControl().getShell().forceActive();
                                        if (w2.isSeperateWindow()) {
                                            w2.open();
                                        }
                                    }
                                }
                            }
                        } else if (Project.getOpenTextWindows().isEmpty()) {
                            openDatFile(df, OpenInWhat.EDITOR_TEXT, null);
                        } else {
                            Project.getOpenTextWindows().iterator().next().openNewDatFileTab(df, false);
                        }
                    }

                    cleanupClosedData();
                    regainFocus();
                }
                Editor3DWindow.getNoSyncDeadlock().set(false);
            }
        });

        widgetUtil(btnSyncTabsPtr[0]).addSelectionListener(e -> WorkbenchManager.getUserSettingState().setSyncingTabs(btnSyncTabsPtr[0].getSelection()));

        txtSearchPtr[0].setText(" "); //$NON-NLS-1$
        txtSearchPtr[0].setText(""); //$NON-NLS-1$

        Project.getFileToEdit().setLastSelectedComposite(Editor3DWindow.renders.get(0).getC3D());
        new EditorTextWindow().run(Project.getFileToEdit(), true);

        updateBgPictureTab();
        Project.getFileToEdit().addHistory();

        // Parse a file here if it should be opened
        if (TryToOpen.getFileToOpen() != null) {
            final DatFile fileToOpen = TryToOpen.getDatFileToOpen();
            Project.addOpenedFile(fileToOpen);
            Project.setFileToEdit(fileToOpen);
            updateTabs();
            tabFolderOpenDatFilesPtr[0].setSelection(2);
            Project.getFileToEdit().parseForData(true);
            Project.getFileToEdit().setLastSelectedComposite(Editor3DWindow.renders.get(0).getC3D());
            for (OpenGLRenderer renderer : renders) {
                renderer.getC3D().setLockableDatFileReference(Project.getFileToEdit());
            }
        }

        this.open();

        // Dispose all resources (never delete this!)
        CSG.executorService.shutdown();
        cmpPrimitivesPtr[0].getOpenGL().dispose();
        ResourceManager.dispose();
        SWTResourceManager.dispose();
        // Dispose the display (never delete this, too!)
        try {
            Display.getCurrent().dispose();
        } catch (SWTException consumed) {
            // Display can already be disposed.
        }
    }

    public Composite3D getCurrentCoposite3d() {
        Composite3D c3d = null;
        if (Project.getFileToEdit() != null
                && Project.getFileToEdit().getLastSelectedComposite() != null
                && !Project.getFileToEdit().getLastSelectedComposite().isDisposed()) {
            c3d = Project.getFileToEdit().getLastSelectedComposite();
        } else if (DatFile.getLastHoveredComposite() != null
                && !DatFile.getLastHoveredComposite().isDisposed()) {
            c3d = DatFile.getLastHoveredComposite();
        } else if (!renders.isEmpty()){
            OpenGLRenderer renderer = renders.get(0);
            c3d = renderer.getC3D();
        }
        if (c3d != null && c3d.isDisposed()) {
            c3d = null;
        }
        return c3d;
    }

    private void addRecentFile(String projectPath) {
        // PrimGen2 uses a temporary "..." projectPath
        if (!"...".equals(projectPath)) { //$NON-NLS-1$
            final int index = recentItems.indexOf(projectPath);
            if (index > -1) {
                recentItems.remove(index);
            } else if (recentItems.size() > 20) {
                recentItems.remove(0);
            }
            recentItems.add(projectPath);
        }
    }

    public void addRecentFile(DatFile dat) {
        addRecentFile(dat.getNewName());
    }

    private void replaceBgPicture(GDataPNG selectedBgPicture, GDataPNG newBgPicture, DatFile linkedDatFile) {
        if (linkedDatFile.getDrawPerLineNoClone().getKey(selectedBgPicture) == null) return;
        GData before = selectedBgPicture.getBefore();
        GData next = selectedBgPicture.getNext();
        int index = linkedDatFile.getDrawPerLineNoClone().getKey(selectedBgPicture);
        selectedBgPicture.setGoingToBeReplaced(true);
        linkedDatFile.getVertexManager().remove(selectedBgPicture);
        linkedDatFile.getDrawPerLineNoClone().put(index, newBgPicture);
        before.setNext(newBgPicture);
        newBgPicture.setNext(next);
        linkedDatFile.getVertexManager().setSelectedBgPicture(newBgPicture);
        updateBgPictureTab();
    }

    public void toggleInsertAtCursor() {
        setInsertingAtCursorPosition(!isInsertingAtCursorPosition());
        btnInsertAtCursorPositionPtr[0].setSelection(isInsertingAtCursorPosition());
        clickSingleBtn(btnInsertAtCursorPositionPtr[0]);
    }

    public void toggleMoveAdjacentData() {
        setMovingAdjacentData(!isMovingAdjacentData());
        btnMoveAdjacentDataPtr[0].setSelection(isMovingAdjacentData());
        clickSingleBtn(btnMoveAdjacentDataPtr[0]);
    }

    public void setObjMode(int type) {
        switch (type) {
        case 0:
            btnVerticesPtr[0].setSelection(true);
            setWorkingType(ObjectMode.VERTICES);
            clickSingleBtn(btnVerticesPtr[0]);
            break;
        case 1:
            btnTrisNQuadsPtr[0].setSelection(true);
            setWorkingType(ObjectMode.FACES);
            clickSingleBtn(btnTrisNQuadsPtr[0]);
            break;
        case 2:
            btnLinesPtr[0].setSelection(true);
            setWorkingType(ObjectMode.LINES);
            clickSingleBtn(btnLinesPtr[0]);
            break;
        case 3:
            btnSubfilesPtr[0].setSelection(true);
            setWorkingType(ObjectMode.SUBFILES);
            clickSingleBtn(btnSubfilesPtr[0]);
            break;
        default:
            break;
        }
    }

    /**
     * The Shell-Close-Event
     */
    @Override
    protected void handleShellCloseEvent() {
        Set<DatFile> unsavedFiles = new HashSet<>(Project.getUnsavedFiles());
        for (DatFile df : unsavedFiles) {
            final String text = df.getText();
            if ((!text.equals(df.getOriginalText()) || df.isVirtual() && !text.trim().isEmpty()) && !text.equals(WorkbenchManager.getDefaultFileHeader())) {
                MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.CANCEL | SWT.NO);
                messageBox.setText(I18n.DIALOG_UNSAVED_CHANGES_TITLE);

                Object[] messageArguments = {df.getShortName()};
                MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                formatter.setLocale(MyLanguage.getLocale());
                formatter.applyPattern(I18n.DIALOG_UNSAVED_CHANGES);
                messageBox.setMessage(formatter.format(messageArguments));

                int result = messageBox.open();

                if (result == SWT.NO) {
                    // Remove file from tree
                    updateTreeRemoveEntry(df);
                } else if (result == SWT.YES) {
                    if (df.save()) {
                        Editor3DWindow.getWindow().addRecentFile(df);
                    } else {
                        MessageBox messageBoxError = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                        messageBoxError.setText(I18n.DIALOG_ERROR);
                        messageBoxError.setMessage(I18n.DIALOG_CANT_SAVE_FILE);
                        messageBoxError.open();
                        cleanupClosedData();
                        updateTreeUnsavedEntries();
                        return;
                    }
                } else {
                    cleanupClosedData();
                    updateTreeUnsavedEntries();
                    return;
                }
            }
        }
        Set<EditorTextWindow> ow = new HashSet<>(Project.getOpenTextWindows());
        for (EditorTextWindow w : ow) {
            if (w.isSeperateWindow()) {
                w.getShell().close();
            } else {
                w.closeAllTabs();
            }
        }

        // NEVER DELETE THIS!
        final int s = renders.size();
        for (int i = 0; i < s; i++) {
            try {
                GLCanvas canvas = canvasList.get(i);
                OpenGLRenderer renderer = renders.get(i);
                if (!canvas.isCurrent()) {
                    canvas.setCurrent();
                    GL.setCapabilities(renderer.getC3D().getCapabilities());
                }
                renderer.dispose();
            } catch (SWTException swtEx) {
                NLogger.error(Editor3DWindow.class, swtEx);
            }
        }
        // All "history threads" needs to know that the main window was closed
        alive.set(false);

        final Editor3DWindowState winState = WorkbenchManager.getEditor3DWindowState();

        // Don't save the 3D view config when doing a part review
        if (!isReviewingAPart()) {
            winState.setThreeDwindowConfig(getC3DStates());
        }

        if (editorSashForm[0] != null) {
            winState.setEditorSashWeights(editorSashForm[0].getWeights());
        }
        winState.setLeftSashWeights(((SashForm) getSashForm().getChildren()[0]).getWeights());
        winState.setLeftSashWidth(getSashForm().getWeights());
        winState.setPrimitiveZoom(cmpPrimitivesPtr[0].getZoom());
        winState.setPrimitiveZoomExponent(cmpPrimitivesPtr[0].getZoomExponent());
        winState.setPrimitiveViewport(cmpPrimitivesPtr[0].getViewport2());

        WorkbenchManager.getPrimitiveCache().setPrimitiveCache(CompositePrimitive.getCache());
        WorkbenchManager.getPrimitiveCache().setPrimitiveFileCache(CompositePrimitive.getFileCache());

        WorkbenchManager.getUserSettingState().setRecentItems(getRecentItems());
        // Save the workbench
        WorkbenchManager.saveWorkbench(WorkbenchManager.SETTINGS_GZ);
        setReturnCode(CANCEL);
        close();
    }

    private void saveComposite3DStates(Control c, List<Composite3DState> c3dStates, String parentPath, String path) {
        Composite3DState st = new Composite3DState();
        st.setParentPath(parentPath);
        st.setPath(path);
        if (c instanceof CompositeContainer) {
            NLogger.debug(getClass(), "{0}C", path); //$NON-NLS-1$
            final Composite3D c3d = ((CompositeContainer) c).getComposite3D();
            st.setSash(false);
            st.setScales(c3d.getParent() instanceof CompositeScale);
            st.setVertical(false);
            st.setWeights(null);
            fillC3DState(st, c3d);
        } else if (c instanceof SashForm) {
            NLogger.debug(getClass(), path);
            SashForm s = (SashForm) c;
            st.setSash(true);
            st.setVertical((s.getStyle() & SWT.VERTICAL) != 0);
            st.setWeights(s.getWeights());
            Control c1 = s.getChildren()[0];
            Control c2 = s.getChildren()[1];
            saveComposite3DStates(c1, c3dStates, path, path + "s1|"); //$NON-NLS-1$
            saveComposite3DStates(c2, c3dStates, path, path + "s2|"); //$NON-NLS-1$
        } else {
            return;
        }
        c3dStates.add(st);
    }

    public void fillC3DState(Composite3DState st, Composite3D c3d) {
        st.setPerspective(c3d.isClassicPerspective() ? c3d.getPerspectiveIndex() : Perspective.TWO_THIRDS);
        st.setRenderMode(c3d.getRenderMode());
        st.setShowLabel(c3d.isShowingLabels());
        st.setShowAxis(c3d.isShowingAxis());
        st.setShowGrid(c3d.isGridShown());
        st.setShowOrigin(c3d.isOriginShown());
        st.setLights(c3d.isLightOn());
        st.setSmooth(c3d.isSmoothShading());
        st.setMeshlines(c3d.isMeshLines());
        st.setSubfileMeshlines(c3d.isSubMeshLines());
        st.setVertices(c3d.isShowingVertices());
        st.setCondlineControlPoints(c3d.isShowingCondlineControlPoints());
        st.setHiddenVertices(c3d.isShowingHiddenVertices());
        st.setStudLogo(c3d.isShowingLogo());
        st.setLineMode(c3d.getLineMode());
        st.setAlwaysBlackLines(c3d.isBlackEdges());
        st.setAnaglyph3d(c3d.isAnaglyph3d());
        st.setGridScale(c3d.getGridScale());
        st.setSyncManipulator(c3d.isSyncManipulator());
        st.setSyncTranslation(c3d.isSyncTranslation());
        st.setSyncZoom(c3d.isSyncZoom());
    }

    /**
     * @return The current Editor3DWindow instance
     */
    public static Editor3DWindow getWindow() {
        return Editor3DWindow.window;
    }

    /**
     * Updates the tree for new unsaved entries
     */
    public void updateTreeUnsavedEntries() {
        List<TreeItem> categories = new ArrayList<>();
        categories.add(this.treeItemProjectPartsPtr[0]);
        categories.add(this.treeItemProjectSubpartsPtr[0]);
        categories.add(this.treeItemProjectPrimitivesPtr[0]);
        categories.add(this.treeItemProjectPrimitives48Ptr[0]);
        categories.add(this.treeItemProjectPrimitives8Ptr[0]);
        categories.add(this.treeItemUnofficialPartsPtr[0]);
        categories.add(this.treeItemUnofficialSubpartsPtr[0]);
        categories.add(this.treeItemUnofficialPrimitivesPtr[0]);
        categories.add(this.treeItemUnofficialPrimitives48Ptr[0]);
        categories.add(this.treeItemUnofficialPrimitives8Ptr[0]);
        int counter = 0;
        for (TreeItem item : categories) {
            counter++;
            List<TreeItem> datFileTreeItems = item.getItems();
            for (TreeItem df : datFileTreeItems) {
                DatFile d = (DatFile) df.getData();
                StringBuilder nameSb = new StringBuilder(new File(d.getNewName()).getName());
                final String d2 = d.getDescription();
                if (counter < 6 && (!d.getNewName().startsWith(Project.getProjectPath()) || !d.getNewName().replace(Project.getProjectPath() + File.separator, "").contains(File.separator))) { //$NON-NLS-1$
                    nameSb.insert(0, "(!) "); //$NON-NLS-1$
                }

                if (NLogger.debugging) {
                    DatType t = d.getType();
                    if (t == DatType.PART) {
                        nameSb.append(" PART"); //$NON-NLS-1$
                    } else if (t == DatType.SUBPART) {
                        nameSb.append(" SUBPART"); //$NON-NLS-1$
                    } else if (t == DatType.PRIMITIVE) {
                        nameSb.append(" PRIMITIVE"); //$NON-NLS-1$
                    } else if (t == DatType.PRIMITIVE48) {
                        nameSb.append(" PRIMITIVE48"); //$NON-NLS-1$
                    } else if (t == DatType.PRIMITIVE8) {
                        nameSb.append(" PRIMITIVE8"); //$NON-NLS-1$
                    }
                }

                if (d2 != null)
                    nameSb.append(d2);
                if (Project.getUnsavedFiles().contains(d)) {
                    df.setText("* " + nameSb.toString()); //$NON-NLS-1$
                } else {
                    df.setText(nameSb.toString());
                }
            }
        }
        this.treeItemUnsavedPtr[0].removeAll();
        Set<DatFile> unsaved = Project.getUnsavedFiles();
        for (DatFile df : unsaved) {
            TreeItem ti = new TreeItem(this.treeItemUnsavedPtr[0]);
            StringBuilder nameSb = new StringBuilder(new File(df.getNewName()).getName());
            final String d = df.getDescription();
            if (d != null)
                nameSb.append(d);
            ti.setText(nameSb.toString());
            ti.setData(df);
        }

        this.treeParts[0].build();
        this.treeParts[0].redraw();
        updateTabs();
    }

    /**
     * Updates the tree for new unsaved entries
     */
    public void updateTreeSelectedDatFile(DatFile sdf) {
        List<TreeItem> categories = new ArrayList<>();
        categories.add(this.treeItemProjectPartsPtr[0]);
        categories.add(this.treeItemProjectSubpartsPtr[0]);
        categories.add(this.treeItemProjectPrimitivesPtr[0]);
        categories.add(this.treeItemProjectPrimitives48Ptr[0]);
        categories.add(this.treeItemProjectPrimitives8Ptr[0]);
        categories.add(this.treeItemUnofficialPartsPtr[0]);
        categories.add(this.treeItemUnofficialSubpartsPtr[0]);
        categories.add(this.treeItemUnofficialPrimitivesPtr[0]);
        categories.add(this.treeItemUnofficialPrimitives48Ptr[0]);
        categories.add(this.treeItemUnofficialPrimitives8Ptr[0]);
        categories.add(this.treeItemOfficialPartsPtr[0]);
        categories.add(this.treeItemOfficialSubpartsPtr[0]);
        categories.add(this.treeItemOfficialPrimitivesPtr[0]);
        categories.add(this.treeItemOfficialPrimitives48Ptr[0]);
        categories.add(this.treeItemOfficialPrimitives8Ptr[0]);
        for (TreeItem item : categories) {
            List<TreeItem> datFileTreeItems = item.getItems();
            for (TreeItem df : datFileTreeItems) {
                DatFile d = (DatFile) df.getData();
                if (d.equals(sdf)) {
                    item.setVisible(true);
                    item.getParentItem().setVisible(true);
                    this.treeParts[0].build();
                    this.treeParts[0].setSelection(df);
                    this.treeParts[0].redraw();
                    updateTabs();
                    return;
                }
            }
        }
        updateTabs();
    }


    /**
     * Updates the tree for renamed entries
     */
    @SuppressWarnings("unchecked")
    private void updateTreeRenamedEntries() {
        Map<String, TreeItem> categories = new HashMap<>();
        Map<String, DatType> types = new HashMap<>();

        List<String> validPrefixes = new ArrayList<>();

        {
            String s = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "PARTS" + File.separator + "S" + File.separator; //$NON-NLS-1$ //$NON-NLS-2$
            validPrefixes.add(s);
            categories.put(s, this.treeItemUnofficialSubpartsPtr[0]);
            types.put(s, DatType.SUBPART);
        }
        {
            String s = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "parts" + File.separator + "s" + File.separator; //$NON-NLS-1$ //$NON-NLS-2$
            validPrefixes.add(s);
            categories.put(s, this.treeItemUnofficialSubpartsPtr[0]);
            types.put(s, DatType.SUBPART);
        }
        {
            String s = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "PARTS" + File.separator; //$NON-NLS-1$
            validPrefixes.add(s);
            categories.put(s, this.treeItemUnofficialPartsPtr[0]);
            types.put(s, DatType.PART);
        }
        {
            String s = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "parts" + File.separator; //$NON-NLS-1$
            validPrefixes.add(s);
            categories.put(s,this.treeItemUnofficialPartsPtr[0]);
            types.put(s, DatType.PART);
        }
        {
            String s = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "P" + File.separator + "48" + File.separator; //$NON-NLS-1$ //$NON-NLS-2$
            validPrefixes.add(s);
            categories.put(s, this.treeItemUnofficialPrimitives48Ptr[0]);
            types.put(s, DatType.PRIMITIVE48);
        }
        {
            String s = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "p" + File.separator + "48" + File.separator; //$NON-NLS-1$ //$NON-NLS-2$
            validPrefixes.add(s);
            categories.put(s, this.treeItemUnofficialPrimitives48Ptr[0]);
            types.put(s, DatType.PRIMITIVE48);
        }
        {
            String s = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "P" + File.separator + "8" + File.separator; //$NON-NLS-1$ //$NON-NLS-2$
            validPrefixes.add(s);
            categories.put(s, this.treeItemUnofficialPrimitives8Ptr[0]);
            types.put(s, DatType.PRIMITIVE8);
        }
        {
            String s = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "p" + File.separator + "8" + File.separator; //$NON-NLS-1$ //$NON-NLS-2$
            validPrefixes.add(s);
            categories.put(s, this.treeItemUnofficialPrimitives8Ptr[0]);
            types.put(s, DatType.PRIMITIVE8);
        }
        {
            String s = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "P" + File.separator; //$NON-NLS-1$
            validPrefixes.add(s);
            categories.put(s, this.treeItemUnofficialPrimitivesPtr[0]);
            types.put(s, DatType.PRIMITIVE);
        }
        {
            String s = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "p" + File.separator; //$NON-NLS-1$
            validPrefixes.add(s);
            categories.put(s, this.treeItemUnofficialPrimitivesPtr[0]);
            types.put(s, DatType.PRIMITIVE);
        }
        {
            String s = Project.getProjectPath() + File.separator + "PARTS" + File.separator + "S" + File.separator; //$NON-NLS-1$ //$NON-NLS-2$
            validPrefixes.add(s);
            categories.put(s, this.treeItemProjectSubpartsPtr[0]);
            types.put(s, DatType.SUBPART);
        }
        {
            String s = Project.getProjectPath() + File.separator + "parts" + File.separator + "s" + File.separator; //$NON-NLS-1$ //$NON-NLS-2$
            validPrefixes.add(s);
            categories.put(s, this.treeItemProjectSubpartsPtr[0]);
            types.put(s, DatType.SUBPART);
        }
        {
            String s = Project.getProjectPath() + File.separator + "PARTS" + File.separator; //$NON-NLS-1$
            validPrefixes.add(s);
            categories.put(s, this.treeItemProjectPartsPtr[0]);
            types.put(s, DatType.PART);
        }
        {
            String s = Project.getProjectPath() + File.separator + "parts" + File.separator; //$NON-NLS-1$
            validPrefixes.add(s);
            categories.put(s, this.treeItemProjectPartsPtr[0]);
            types.put(s, DatType.PART);
        }
        {
            String s = Project.getProjectPath() + File.separator + "P" + File.separator + "48" + File.separator; //$NON-NLS-1$ //$NON-NLS-2$
            validPrefixes.add(s);
            categories.put(s, this.treeItemProjectPrimitives48Ptr[0]);
            types.put(s, DatType.PRIMITIVE48);
        }
        {
            String s = Project.getProjectPath() + File.separator + "p" + File.separator + "48" + File.separator; //$NON-NLS-1$ //$NON-NLS-2$
            validPrefixes.add(s);
            categories.put(s, this.treeItemProjectPrimitives48Ptr[0]);
            types.put(s, DatType.PRIMITIVE48);
        }
        {
            String s = Project.getProjectPath() + File.separator + "P" + File.separator + "8" + File.separator; //$NON-NLS-1$ //$NON-NLS-2$
            validPrefixes.add(s);
            categories.put(s, this.treeItemProjectPrimitives8Ptr[0]);
            types.put(s, DatType.PRIMITIVE8);
        }
        {
            String s = Project.getProjectPath() + File.separator + "p" + File.separator + "8" + File.separator; //$NON-NLS-1$ //$NON-NLS-2$
            validPrefixes.add(s);
            categories.put(s, this.treeItemProjectPrimitives8Ptr[0]);
            types.put(s, DatType.PRIMITIVE8);
        }
        {
            String s = Project.getProjectPath() + File.separator + "P" + File.separator; //$NON-NLS-1$
            validPrefixes.add(s);
            categories.put(s, this.treeItemProjectPrimitivesPtr[0]);
            types.put(s, DatType.PRIMITIVE);
        }
        {
            String s = Project.getProjectPath() + File.separator + "p" + File.separator; //$NON-NLS-1$
            validPrefixes.add(s);
            categories.put(s, this.treeItemProjectPrimitivesPtr[0]);
            types.put(s, DatType.PRIMITIVE);
        }

        Collections.sort(validPrefixes, new Comp());

        for (String prefix : validPrefixes) {
            TreeItem item = categories.get(prefix);
            List<DatFile> dats = (List<DatFile>) item.getData();
            List<TreeItem> datFileTreeItems = item.getItems();
            Set<TreeItem> itemsToRemove = new HashSet<>();
            for (TreeItem df : datFileTreeItems) {
                DatFile d = (DatFile) df.getData();
                String newName = d.getNewName();

                String validPrefix = null;
                for (String p2 : validPrefixes) {
                    if (newName.startsWith(p2)) {
                        validPrefix = p2;
                        break;
                    }
                }
                if (validPrefix != null) {
                    TreeItem item2 = categories.get(validPrefix);
                    if (!item2.equals(item)) {
                        itemsToRemove.add(df);
                        dats.remove(d);
                        ((List<DatFile>) item2.getData()).add(d);
                        TreeItem nt = new TreeItem(item2);
                        nt.setText(df.getText());
                        d.setType(types.get(validPrefix));
                        nt.setData(d);
                    }

                }
            }
            datFileTreeItems.removeAll(itemsToRemove);
        }


        this.treeParts[0].build();
        this.treeParts[0].redraw();
        updateTabs();
    }

    private class Comp implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            if (o1.length() < o2.length()) {
                return 1;
            } else if (o1.length() > o2.length()) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    /**
     * Removes an item from the tree,<br><br>
     * If it is open in a {@linkplain Composite3D}, this composite will be linked with a dummy file
     * If it is open in a {@linkplain CompositeTab}, this composite will be closed
     *
     */
    public void updateTreeRemoveEntry(DatFile e) {
        List<TreeItem> categories = new ArrayList<>();
        categories.add(this.treeItemProjectPartsPtr[0]);
        categories.add(this.treeItemProjectSubpartsPtr[0]);
        categories.add(this.treeItemProjectPrimitivesPtr[0]);
        categories.add(this.treeItemProjectPrimitives8Ptr[0]);
        categories.add(this.treeItemProjectPrimitives48Ptr[0]);
        categories.add(this.treeItemUnofficialPartsPtr[0]);
        categories.add(this.treeItemUnofficialSubpartsPtr[0]);
        categories.add(this.treeItemUnofficialPrimitivesPtr[0]);
        categories.add(this.treeItemUnofficialPrimitives8Ptr[0]);
        categories.add(this.treeItemUnofficialPrimitives48Ptr[0]);
        int counter = 0;
        for (TreeItem item : categories) {
            counter++;
            List<TreeItem> datFileTreeItems = new ArrayList<>(item.getItems());
            for (TreeItem df : datFileTreeItems) {
                DatFile d = (DatFile) df.getData();
                if (e.equals(d)) {
                    item.getItems().remove(df);
                } else {
                    StringBuilder nameSb = new StringBuilder(new File(d.getNewName()).getName());
                    final String d2 = d.getDescription();
                    if (counter < 6 && (!d.getNewName().startsWith(Project.getProjectPath()) || !d.getNewName().replace(Project.getProjectPath() + File.separator, "").contains(File.separator))) { //$NON-NLS-1$
                        nameSb.insert(0, "(!) "); //$NON-NLS-1$
                    }
                    if (d2 != null)
                        nameSb.append(d2);
                    if (Project.getUnsavedFiles().contains(d)) {
                        df.setText("* " + nameSb.toString()); //$NON-NLS-1$
                    } else {
                        df.setText(nameSb.toString());
                    }
                }
            }
        }
        this.treeItemUnsavedPtr[0].removeAll();
        Project.removeUnsavedFile(e);
        for (OpenGLRenderer renderer : renders) {
            Composite3D c3d = renderer.getC3D();
            if (c3d.getLockableDatFileReference().equals(e)) {
                c3d.unlinkData();
            }
        }
        Set<EditorTextWindow> windows = new HashSet<>(Project.getOpenTextWindows());
        for (EditorTextWindow win : windows) {
            win.closeTabWithDatfile(e);
        }

        Set<DatFile> unsaved = Project.getUnsavedFiles();
        for (DatFile df : unsaved) {
            TreeItem ti = new TreeItem(this.treeItemUnsavedPtr[0]);
            StringBuilder nameSb = new StringBuilder(new File(df.getNewName()).getName());
            final String d = df.getDescription();
            if (d != null)
                nameSb.append(d);
            ti.setText(nameSb.toString());
            ti.setData(df);
        }

        TreeItem[] folders = new TreeItem[10];
        folders[0] = treeItemProjectPartsPtr[0];
        folders[1] = treeItemProjectPrimitivesPtr[0];
        folders[2] = treeItemProjectPrimitives8Ptr[0];
        folders[3] = treeItemProjectPrimitives48Ptr[0];
        folders[4] = treeItemProjectSubpartsPtr[0];

        folders[5] = treeItemUnofficialPartsPtr[0];
        folders[6] = treeItemUnofficialPrimitivesPtr[0];
        folders[7] = treeItemUnofficialPrimitives8Ptr[0];
        folders[8] = treeItemUnofficialPrimitives48Ptr[0];
        folders[9] = treeItemUnofficialSubpartsPtr[0];

        for (TreeItem folder : folders) {
            @SuppressWarnings("unchecked")
            List<DatFile> cachedReferences =(List<DatFile>) folder.getData();
            if (cachedReferences != null) {
                cachedReferences.remove(e);
            }
        }

        this.treeParts[0].build();
        this.treeParts[0].redraw();
        updateTabs();
    }

    private synchronized void updateTabs() {

        boolean isSelected = false;
        if (tabFolderOpenDatFilesPtr[0].getData() != null) {
            return;
        }
        tabFolderOpenDatFilesPtr[0].setData(true);
        for (CTabItem c : tabFolderOpenDatFilesPtr[0].getItems()) {
            c.dispose();
        }

        {
            CTabItem tItem = new CTabItem(tabFolderOpenDatFilesPtr[0], SWT.NONE);
            tItem.setText(I18n.E3D_NO_FILE_SELECTED);
            tItem.setData(View.DUMMY_DATFILE);
        }

        for (Iterator<DatFile> iterator = Project.getOpenedFiles().iterator(); iterator.hasNext();) {
            DatFile df3 = iterator.next();
            if (View.DUMMY_DATFILE.equals(df3)) {
                iterator.remove();
            }
        }

        for (DatFile df2 : Project.getOpenedFiles()) {
            CTabItem tItem = new CTabItem(tabFolderOpenDatFilesPtr[0], SWT.NONE);
            tItem.setText(df2.getShortName() + (Project.getUnsavedFiles().contains(df2) ? "*" : "")); //$NON-NLS-1$ //$NON-NLS-2$
            tItem.setData(df2);
            if (df2.equals(Project.getFileToEdit())) {
                tabFolderOpenDatFilesPtr[0].setSelection(tItem);
                isSelected = true;
            }
        }
        if (!isSelected) {
            tabFolderOpenDatFilesPtr[0].setSelection(0);
            Project.setFileToEdit(View.DUMMY_DATFILE);
        }
        tabFolderOpenDatFilesPtr[0].setData(null);
        tabFolderOpenDatFilesPtr[0].layout();
        tabFolderOpenDatFilesPtr[0].redraw();
    }

    // Helper functions
    private void clickBtnTest(NButton btn) {
        WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btn.getParent());
        btn.setSelection(true);
    }

    private void clickSingleBtn(NButton btn) {
        boolean state = btn.getSelection();
        WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btn.getParent());
        btn.setSelection(state);
    }

    public TreeItem getProject() {
        return treeItemProjectPtr[0];
    }

    public TreeItem getProjectParts() {
        return treeItemProjectPartsPtr[0];
    }

    public TreeItem getProjectPrimitives() {
        return treeItemProjectPrimitivesPtr[0];
    }

    public TreeItem getProjectPrimitives48() {
        return treeItemProjectPrimitives48Ptr[0];
    }

    public TreeItem getProjectPrimitives8() {
        return treeItemProjectPrimitives8Ptr[0];
    }

    public TreeItem getProjectSubparts() {
        return treeItemProjectSubpartsPtr[0];
    }

    public TreeItem getUnofficialParts() {
        return treeItemUnofficialPartsPtr[0];
    }

    public TreeItem getUnofficialPrimitives() {
        return treeItemUnofficialPrimitivesPtr[0];
    }

    public TreeItem getUnofficialPrimitives48() {
        return treeItemUnofficialPrimitives48Ptr[0];
    }

    public TreeItem getUnofficialPrimitives8() {
        return treeItemUnofficialPrimitives8Ptr[0];
    }

    public TreeItem getUnofficialSubparts() {
        return treeItemUnofficialSubpartsPtr[0];
    }

    public TreeItem getOfficialParts() {
        return treeItemOfficialPartsPtr[0];
    }

    public TreeItem getOfficialPrimitives() {
        return treeItemOfficialPrimitivesPtr[0];
    }

    public TreeItem getOfficialPrimitives48() {
        return treeItemOfficialPrimitives48Ptr[0];
    }

    public TreeItem getOfficialPrimitives8() {
        return treeItemOfficialPrimitives8Ptr[0];
    }

    public TreeItem getOfficialSubparts() {
        return treeItemOfficialSubpartsPtr[0];
    }

    public TreeItem getUnsaved() {
        return treeItemUnsavedPtr[0];
    }

    public ObjectMode getWorkingType() {
        return workingType;
    }

    public void setWorkingType(ObjectMode workingMode) {
        this.workingType = workingMode;
    }

    public boolean isMovingAdjacentData() {
        return movingAdjacentData;
    }

    public void setMovingAdjacentData(boolean movingAdjacentData) {
        btnMoveAdjacentDataPtr[0].setSelection(movingAdjacentData);
        this.movingAdjacentData = movingAdjacentData;
        WorkbenchManager.getUserSettingState().setMovingAdjacentData(movingAdjacentData);
    }

    public WorkingMode getWorkingAction() {
        return workingAction;
    }

    public void setWorkingAction(WorkingMode workingAction) {
        this.workingAction = workingAction;
        switch (workingAction) {
        case COMBINED:
            clickBtnTest(btnCombinedPtr[0]);
            break;
        case MOVE:
            clickBtnTest(btnMovePtr[0]);
            break;
        case ROTATE:
            clickBtnTest(btnRotatePtr[0]);
            break;
        case SCALE:
            clickBtnTest(btnScalePtr[0]);
            break;
        case SELECT:
            clickBtnTest(btnSelectPtr[0]);
            break;
        default:
            break;
        }
    }

    public ManipulatorScope getTransformationMode() {
        return transformationMode;
    }

    public boolean hasNoTransparentSelection() {
        return noTransparentSelection;
    }

    public void setNoTransparentSelection(boolean noTransparentSelection) {
        this.noTransparentSelection = noTransparentSelection;
    }

    public boolean hasBfcToggle() {
        return bfcToggle;
    }

    public void setBfcToggle(boolean bfcToggle) {
        this.bfcToggle = bfcToggle;
    }

    public boolean isInsertingAtCursorPosition() {
        return insertingAtCursorPosition;
    }

    public void setInsertingAtCursorPosition(boolean insertAtCursor) {
        this.insertingAtCursorPosition = insertAtCursor;
    }

    public boolean isReviewingAPart() {
        return reviewingAPart;
    }

    @SuppressWarnings("unchecked")
    public void setReviewingAPart(boolean reviewingAPart, Set<DatFile> partsForReview) {
        if (reviewingAPart) {
            NButton btnEndPartReview = new NButton(cmpSyncAndReviewPtr[0], SWT.NONE);
            btnEndPartReview.setText(I18n.E3D_END_PART_REVIEW);
            btnEndPartReview.setData(partsForReview);
            widgetUtil(btnEndPartReview).addSelectionListener(e -> setReviewingAPart(false, null));
        } else {
            // Close all review files
            partsForReview = (Set<DatFile>) cmpSyncAndReviewPtr[0].getChildren()[1].getData();
            for (DatFile df : partsForReview) {
                Project.removeOpenedFile(df);
                closeDatfile(df);
            }
            // Hide "End Review" button
            cmpSyncAndReviewPtr[0].getChildren()[1].dispose();
            // Reset project
            Project.setFileToEdit(View.DUMMY_DATFILE);
            Project.setProjectPath(new File(Project.DEFAULT_PROJECT_PATH).getAbsolutePath());
            getShell().setText(Version.getApplicationName() + " " + Version.getVersion() + " (" + WorkbenchManager.getUserSettingState().getOpenGLVersionString() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            getShell().update();
            treeItemProjectPtr[0].setText(I18n.PROJECT_NEW_PROJECT);
            treeItemProjectPtr[0].setData(Project.getProjectPath());
            treeItemProjectPtr[0].getParent().build();
            treeItemProjectPtr[0].getParent().redraw();
            treeItemProjectPtr[0].getParent().update();
            // Restore the viewport
            closeAllComposite3D();
            reloadC3DStates(WorkbenchManager.getEditor3DWindowState().getThreeDwindowConfig());
        }
        cmpSyncAndReviewPtr[0].getParent().layout(true);

        this.reviewingAPart = reviewingAPart;
    }

    public ManipulatorAxisMode getWorkingLayer() {
        return workingLayer;
    }

    public void setWorkingLayer(ManipulatorAxisMode workingLayer) {
        this.workingLayer = workingLayer;
    }

    public GColour getLastUsedColour() {
        return lastUsedColour;
    }

    public void setLastUsedColour(GColour lastUsedColour) {
        this.lastUsedColour = lastUsedColour;
    }

    public void setLastUsedColour2(GColour lastUsedColour) {
        final int imgSize = IconSize.getImageSizeFromIconSize();
        final GColour[] gColour2 = new GColour[] { lastUsedColour };
        int num = gColour2[0].getColourNumber();
        if (LDConfig.hasColour(num)) {
            gColour2[0] = LDConfig.getColour(num);
        } else {
            num = -1;
        }
        Editor3DWindow.getWindow().setLastUsedColour(gColour2[0]);
        btnLastUsedColourPtr[0].clearPaintListeners();
        btnLastUsedColourPtr[0].clearSelectionListeners();
        final Color col = SWTResourceManager.getColor((int) (gColour2[0].getR() * 255f), (int) (gColour2[0].getG() * 255f), (int) (gColour2[0].getB() * 255f));
        final Point size = btnLastUsedColourPtr[0].computeSize(SWT.DEFAULT, SWT.DEFAULT);
        final int x = size.x / 4;
        final int y = size.y / 4;
        final int w = size.x / 2;
        final int h = size.y / 2;
        btnLastUsedColourPtr[0].addPaintListener(e -> {
            e.gc.setBackground(col);
            e.gc.fillRectangle(x, y, w, h);
            if (gColour2[0].getA() >= .99f) {
                e.gc.drawImage(ResourceManager.getImage("icon16_transparent.png"), 0, 0, imgSize, imgSize, x, y, w, h); //$NON-NLS-1$
            } else if (gColour2[0].getA() == 0f) {
                e.gc.drawImage(ResourceManager.getImage("icon16_randomColours.png"), 0, 0, imgSize, imgSize, x, y, w, h); //$NON-NLS-1$
            } else {
                e.gc.drawImage(ResourceManager.getImage("icon16_halftrans.png"), 0, 0, imgSize, imgSize, x, y, w, h); //$NON-NLS-1$
            }
        });
        widgetUtil(btnLastUsedColourPtr[0]).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null) {
                Editor3DWindow.getWindow().setLastUsedColour(gColour2[0]);
                int num1 = gColour2[0].getColourNumber();
                if (!LDConfig.hasColour(num1)) {
                    num1 = -1;
                }
                Project.getFileToEdit().getVertexManager().colourChangeSelection(num1, gColour2[0].getR(), gColour2[0].getG(), gColour2[0].getB(), gColour2[0].getA(), true);
            }
        });
        if (num != -1) {

            Object[] messageArguments = {num, LDConfig.getColourName(num)};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.getLocale());
            formatter.applyPattern(I18n.EDITORTEXT_COLOUR_1);

            btnLastUsedColourPtr[0].setToolTipText(formatter.format(messageArguments));
        } else {
            StringBuilder colourBuilder = new StringBuilder();
            colourBuilder.append("0x2"); //$NON-NLS-1$
            colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getR())).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getG())).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getB())).toUpperCase());

            Object[] messageArguments = {colourBuilder.toString()};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.getLocale());
            formatter.applyPattern(I18n.EDITORTEXT_COLOUR_2);

            btnLastUsedColourPtr[0].setToolTipText(formatter.format(messageArguments));
            if (gColour2[0].getA() == 0f) btnLastUsedColourPtr[0].setToolTipText(I18n.COLOURDIALOG_RANDOM_COLOURS);
        }
        btnLastUsedColourPtr[0].redraw();
    }

    public void cleanupClosedData() {
        Set<DatFile> openFiles = new HashSet<>(Project.getUnsavedFiles());
        for (OpenGLRenderer renderer : renders) {
            Composite3D c3d = renderer.getC3D();
            openFiles.add(c3d.getLockableDatFileReference());
        }
        for (EditorTextWindow w : Project.getOpenTextWindows()) {
            for (CTabItem t : w.getTabFolder().getItems()) {
                openFiles.add(((CompositeTab) t).getState().getFileNameObj());
            }
        }
        Set<DatFile> deadFiles = new HashSet<>(Project.getParsedFiles());
        deadFiles.removeAll(openFiles);
        if (!deadFiles.isEmpty()) {
            GData.CACHE_viewByProjection.clear();
            GData.parsedLines.clear();
            GData.CACHE_parsedFilesSource.clear();
        }
        for (DatFile datFile : deadFiles) {
            datFile.disposeData();
        }
    }

    public String getSearchCriteria() {
        return txtSearchPtr[0].getText();
    }

    public void resetSearch() {
        search(""); //$NON-NLS-1$
    }

    public void search(final String word) {

        this.getShell().getDisplay().asyncExec(new Runnable() {
            @SuppressWarnings("unchecked")
            @Override
            public void run() {

                String criteria = ".*" + word + ".*"; //$NON-NLS-1$ //$NON-NLS-2$

                TreeItem[] folders = new TreeItem[15];
                folders[0] = treeItemOfficialPartsPtr[0];
                folders[1] = treeItemOfficialPrimitivesPtr[0];
                folders[2] = treeItemOfficialPrimitives8Ptr[0];
                folders[3] = treeItemOfficialPrimitives48Ptr[0];
                folders[4] = treeItemOfficialSubpartsPtr[0];

                folders[5] = treeItemUnofficialPartsPtr[0];
                folders[6] = treeItemUnofficialPrimitivesPtr[0];
                folders[7] = treeItemUnofficialPrimitives8Ptr[0];
                folders[8] = treeItemUnofficialPrimitives48Ptr[0];
                folders[9] = treeItemUnofficialSubpartsPtr[0];

                folders[10] = treeItemProjectPartsPtr[0];
                folders[11] = treeItemProjectPrimitivesPtr[0];
                folders[12] = treeItemProjectPrimitives8Ptr[0];
                folders[13] = treeItemProjectPrimitives48Ptr[0];
                folders[14] = treeItemProjectSubpartsPtr[0];

                if (folders[0].getData() == null) {
                    for (TreeItem folder : folders) {
                        folder.setData(new ArrayList<>());
                        for (TreeItem part : folder.getItems()) {
                            ((List<DatFile>) folder.getData()).add((DatFile) part.getData());
                        }
                    }
                }

                try {
                    "42".matches(criteria); //$NON-NLS-1$
                } catch (Exception ex) {
                    criteria = ".*"; //$NON-NLS-1$
                }

                final Pattern pattern = Pattern.compile(criteria);
                for (int i = 0; i < 15; i++) {
                    TreeItem folder = folders[i];
                    folder.removeAll();
                    for (DatFile part : (List<DatFile>) folder.getData()) {
                        StringBuilder nameSb = new StringBuilder(new File(part.getNewName()).getName());
                        if (i > 9 && (!part.getNewName().startsWith(Project.getProjectPath()) || !part.getNewName().replace(Project.getProjectPath() + File.separator, "").contains(File.separator))) { //$NON-NLS-1$
                            nameSb.insert(0, "(!) "); //$NON-NLS-1$
                        }
                        final String d = part.getDescription();
                        if (d != null)
                            nameSb.append(d);
                        String name = nameSb.toString();
                        TreeItem finding = new TreeItem(folder);
                        // Save the path
                        finding.setData(part);
                        // Set the filename
                        if (Project.getUnsavedFiles().contains(part) || !part.getOldName().equals(part.getNewName())) {
                            // Insert asterisk if the file was
                            // modified
                            finding.setText("* " + name); //$NON-NLS-1$
                        } else {
                            finding.setText(name);
                        }
                        finding.setShown(!(d != null && d.startsWith(" - ~Moved to")) && pattern.matcher(name).matches()); //$NON-NLS-1$
                    }
                }
                folders[0].getParent().build();
                folders[0].getParent().redraw();
                folders[0].getParent().update();
            }
        });
    }

    public void closeAllComposite3D() {
        canvasList.clear();
        List<OpenGLRenderer> renders2 = new ArrayList<>(renders);
        for (OpenGLRenderer renderer : renders2) {
            Composite3D c3d = renderer.getC3D();
            c3d.getModifier().closeView();
        }
        renders.clear();
    }

    public TreeData getDatFileTreeData(DatFile df) {
        TreeData result = new TreeData();
        List<TreeItem> categories = new ArrayList<>();
        categories.add(this.treeItemProjectPartsPtr[0]);
        categories.add(this.treeItemProjectSubpartsPtr[0]);
        categories.add(this.treeItemProjectPrimitivesPtr[0]);
        categories.add(this.treeItemProjectPrimitives48Ptr[0]);
        categories.add(this.treeItemProjectPrimitives8Ptr[0]);
        categories.add(this.treeItemUnofficialPartsPtr[0]);
        categories.add(this.treeItemUnofficialSubpartsPtr[0]);
        categories.add(this.treeItemUnofficialPrimitivesPtr[0]);
        categories.add(this.treeItemUnofficialPrimitives48Ptr[0]);
        categories.add(this.treeItemUnofficialPrimitives8Ptr[0]);
        categories.add(this.treeItemOfficialPartsPtr[0]);
        categories.add(this.treeItemOfficialSubpartsPtr[0]);
        categories.add(this.treeItemOfficialPrimitivesPtr[0]);
        categories.add(this.treeItemOfficialPrimitives48Ptr[0]);
        categories.add(this.treeItemOfficialPrimitives8Ptr[0]);
        categories.add(this.treeItemUnsavedPtr[0]);
        for (TreeItem item : categories) {
            List<TreeItem> datFileTreeItems = item.getItems();
            for (TreeItem ti : datFileTreeItems) {
                DatFile d = (DatFile) ti.getData();
                if (df.equals(d)) {
                    result.setLocation(ti);
                } else if (d.getShortName().equals(df.getShortName())) {
                    result.getLocationsWithSameShortFilenames().add(ti);
                }
            }
        }
        return result;
    }

    /**
     * Updates the background picture tab
     */
    public void updateBgPictureTab() {
        for (OpenGLRenderer renderer : renders) {
            Composite3D c3d = renderer.getC3D();
            if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                GDataPNG png = vm.getSelectedBgPicture();
                if (png == null) {

                    updatingPngPictureTab = true;
                    txtPngPathPtr[0].setText("---"); //$NON-NLS-1$
                    txtPngPathPtr[0].setToolTipText("---"); //$NON-NLS-1$

                    spnPngXPtr[0].setValue(BigDecimal.ZERO);
                    spnPngYPtr[0].setValue(BigDecimal.ZERO);
                    spnPngZPtr[0].setValue(BigDecimal.ZERO);

                    spnPngA1Ptr[0].setValue(BigDecimal.ZERO);
                    spnPngA2Ptr[0].setValue(BigDecimal.ZERO);
                    spnPngA3Ptr[0].setValue(BigDecimal.ZERO);

                    spnPngSXPtr[0].setValue(BigDecimal.ONE);
                    spnPngSYPtr[0].setValue(BigDecimal.ONE);

                    txtPngPathPtr[0].setEnabled(false);
                    btnPngFocusPtr[0].setEnabled(false);
                    btnPngImagePtr[0].setEnabled(false);
                    spnPngXPtr[0].setEnabled(false);
                    spnPngYPtr[0].setEnabled(false);
                    spnPngZPtr[0].setEnabled(false);

                    spnPngA1Ptr[0].setEnabled(false);
                    spnPngA2Ptr[0].setEnabled(false);
                    spnPngA3Ptr[0].setEnabled(false);

                    spnPngSXPtr[0].setEnabled(false);
                    spnPngSYPtr[0].setEnabled(false);

                    spnPngA1Ptr[0].getParent().update();
                    updatingPngPictureTab = false;
                    return;
                }

                updatingPngPictureTab = true;

                txtPngPathPtr[0].setEnabled(true);
                btnPngFocusPtr[0].setEnabled(true);
                btnPngImagePtr[0].setEnabled(true);
                spnPngXPtr[0].setEnabled(true);
                spnPngYPtr[0].setEnabled(true);
                spnPngZPtr[0].setEnabled(true);

                spnPngA1Ptr[0].setEnabled(true);
                spnPngA2Ptr[0].setEnabled(true);
                spnPngA3Ptr[0].setEnabled(true);

                spnPngSXPtr[0].setEnabled(true);
                spnPngSYPtr[0].setEnabled(true);

                txtPngPathPtr[0].setText(png.texturePath);
                txtPngPathPtr[0].setToolTipText(png.texturePath);

                spnPngXPtr[0].setValue(png.offset.xp);
                spnPngYPtr[0].setValue(png.offset.yp);
                spnPngZPtr[0].setValue(png.offset.zp);

                spnPngA1Ptr[0].setValue(png.angleA);
                spnPngA2Ptr[0].setValue(png.angleB);
                spnPngA3Ptr[0].setValue(png.angleC);

                spnPngSXPtr[0].setValue(png.scale.xp);
                spnPngSYPtr[0].setValue(png.scale.yp);

                spnPngA1Ptr[0].getParent().update();
                updatingPngPictureTab = false;
                return;
            }
        }
    }

    public DatFile createNewDatFile(Shell sh, OpenInWhat where) {

        FileDialog fd = new FileDialog(sh, SWT.SAVE);
        fd.setText(I18n.E3D_CREATE_NEW_DAT);

        fd.setFilterPath(Project.getLastVisitedPath());

        String[] filterExt = { "*.dat", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
        fd.setFilterExtensions(filterExt);
        String[] filterNames = { I18n.E3D_LDRAW_SOURCE_FILE, I18n.E3D_ALL_FILES };
        fd.setFilterNames(filterNames);

        while (true) {
            String selected = fd.open();
            System.out.println(selected);

            if (selected != null) {

                // Check if its already created

                DatFile df = new DatFile(selected);

                if (isFileNameAllocated(selected, df, true)) {
                    MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.RETRY | SWT.CANCEL);
                    messageBox.setText(I18n.DIALOG_ALREADY_ALLOCATED_NAME_TITLE);
                    messageBox.setMessage(I18n.DIALOG_ALREADY_ALLOCATED_NAME);

                    int result = messageBox.open();

                    if (result == SWT.CANCEL) {
                        break;
                    }
                } else {

                    String typeSuffix = ""; //$NON-NLS-1$
                    String folderPrefix = ""; //$NON-NLS-1$
                    String subfilePrefix = ""; //$NON-NLS-1$
                    String path = new File(selected).getParent();
                    TreeItem parent = this.treeItemProjectPartsPtr[0];

                    if (path.endsWith(File.separator + "S") || path.endsWith(File.separator + "s")) { //$NON-NLS-1$ //$NON-NLS-2$
                        typeSuffix = "Unofficial_Subpart"; //$NON-NLS-1$
                        folderPrefix = "s\\"; //$NON-NLS-1$
                        subfilePrefix = "~"; //$NON-NLS-1$
                        parent = this.treeItemProjectSubpartsPtr[0];
                    } else if (path.endsWith(File.separator + "P" + File.separator + "48") || path.endsWith(File.separator + "p" + File.separator + "48")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                        typeSuffix = "Unofficial_48_Primitive"; //$NON-NLS-1$
                        folderPrefix = "48\\"; //$NON-NLS-1$
                        parent = this.treeItemProjectPrimitives48Ptr[0];
                    } else if (path.endsWith(File.separator + "P" + File.separator + "8") || path.endsWith(File.separator + "p" + File.separator + "8")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                        typeSuffix = "Unofficial_8_Primitive"; //$NON-NLS-1$
                        folderPrefix = "8\\"; //$NON-NLS-1$
                        parent = this.treeItemProjectPrimitives8Ptr[0];
                    } else if (path.endsWith(File.separator + "P") || path.endsWith(File.separator + "p")) { //$NON-NLS-1$ //$NON-NLS-2$
                        typeSuffix = "Unofficial_Primitive"; //$NON-NLS-1$
                        parent = this.treeItemProjectPrimitivesPtr[0];
                    }

                    df.addToTail(new GData0("0 " + subfilePrefix, View.DUMMY_REFERENCE)); //$NON-NLS-1$
                    df.addToTail(new GData0("0 Name: " + folderPrefix + new File(selected).getName(), View.DUMMY_REFERENCE)); //$NON-NLS-1$
                    String ldrawName = WorkbenchManager.getUserSettingState().getLdrawUserName();
                    if (ldrawName == null || ldrawName.isEmpty()) {
                        df.addToTail(new GData0("0 Author: " + WorkbenchManager.getUserSettingState().getRealUserName(), View.DUMMY_REFERENCE)); //$NON-NLS-1$
                    } else {
                        df.addToTail(new GData0("0 Author: " + WorkbenchManager.getUserSettingState().getRealUserName() + " [" + WorkbenchManager.getUserSettingState().getLdrawUserName() + "]", View.DUMMY_REFERENCE)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    }
                    df.addToTail(new GData0("0 !LDRAW_ORG " + typeSuffix, View.DUMMY_REFERENCE)); //$NON-NLS-1$
                    String license = WorkbenchManager.getUserSettingState().getLicense();
                    if (license == null || license.isEmpty()) {
                        df.addToTail(new GData0("0 !LICENSE Redistributable under CCAL version 2.0 : see CAreadme.txt", View.DUMMY_REFERENCE)); //$NON-NLS-1$
                    } else {
                        df.addToTail(new GData0(license, View.DUMMY_REFERENCE));
                    }
                    df.addToTail(new GData0("", View.DUMMY_REFERENCE)); //$NON-NLS-1$
                    df.addToTail(new GDataBFC(BFC.CCW_CLIP, View.DUMMY_REFERENCE));
                    df.addToTail(new GData0("", View.DUMMY_REFERENCE)); //$NON-NLS-1$
                    df.addToTail(new GData0("", View.DUMMY_REFERENCE)); //$NON-NLS-1$

                    df.getVertexManager().setModified(true, true);

                    TreeItem ti = new TreeItem(parent);
                    StringBuilder nameSb = new StringBuilder(new File(df.getNewName()).getName());
                    nameSb.append(I18n.E3D_NEW_FILE);
                    ti.setText(nameSb.toString());
                    ti.setData(df);

                    @SuppressWarnings("unchecked")
                    List<DatFile> cachedReferences = (List<DatFile>) this.treeItemProjectPartsPtr[0].getData();
                    cachedReferences.add(df);

                    Project.addUnsavedFile(df);
                    updateTreeRenamedEntries();
                    updateTreeUnsavedEntries();
                    updateTreeSelectedDatFile(df);

                    openDatFile(df, where, null);
                    return df;
                }
            } else {
                break;
            }
        }
        return null;
    }

    public DatFile openDatFile(OpenInWhat where, String filePath, boolean canRevert) {

        if (filePath != null) {
            NLogger.debug(Editor3DWindow.class, filePath);

            // Check if its already created

            DatType type = DatType.PART;

            DatFile df = new DatFile(filePath);
            DatFile original = isFileNameAllocated2(filePath);

            if (original == null) {

                // Type Check and Description Parsing!!
                StringBuilder titleSb = new StringBuilder();
                File f = new File(filePath);
                try (UTF8BufferedReader reader = new UTF8BufferedReader(f.getAbsolutePath())) {
                    String title = reader.readLine();
                    if (title != null) {
                        title = title.trim();
                        if (title.length() > 0) {
                            titleSb.append(" -"); //$NON-NLS-1$
                            titleSb.append(title.substring(1));
                        }
                    }
                    while (true) {
                        String typ = reader.readLine();
                        if (typ != null) {
                            typ = typ.trim();
                            if (!typ.startsWith("0")) { //$NON-NLS-1$
                                break;
                            } else {
                                int i1 = typ.indexOf("!LDRAW_ORG"); //$NON-NLS-1$
                                if (i1 > -1) {
                                    int i2;
                                    i2 = typ.indexOf("Subpart"); //$NON-NLS-1$
                                    if (i2 > -1 && i1 < i2) {
                                        type = DatType.SUBPART;
                                        break;
                                    }
                                    i2 = typ.indexOf("Part"); //$NON-NLS-1$
                                    if (i2 > -1 && i1 < i2) {
                                        type = DatType.PART;
                                        break;
                                    }
                                    i2 = typ.indexOf("48_Primitive"); //$NON-NLS-1$
                                    if (i2 > -1 && i1 < i2) {
                                        type = DatType.PRIMITIVE48;
                                        break;
                                    }
                                    i2 = typ.indexOf("8_Primitive"); //$NON-NLS-1$
                                    if (i2 > -1 && i1 < i2) {
                                        type = DatType.PRIMITIVE8;
                                        break;
                                    }
                                    i2 = typ.indexOf("Primitive"); //$NON-NLS-1$
                                    if (i2 > -1 && i1 < i2) {
                                        type = DatType.PRIMITIVE;
                                        break;
                                    }
                                }
                            }
                        } else {
                            break;
                        }
                    }
                } catch (LDParsingException | FileNotFoundException ex) {
                    NLogger.error(Editor3DWindow.class, ex);
                }

                df = new DatFile(filePath, titleSb.toString(), false, type);
                df.setProjectFile(df.getNewName().startsWith(Project.getProjectPath()));

            } else {

                // FIXME Needs code cleanup!!
                df = original;

                if (canRevert && Project.getUnsavedFiles().contains(df) && Editor3DWindow.getWindow().revert(df)) {
                    updateTreeUnsavedEntries();
                    boolean foundTab = false;
                    for (EditorTextWindow win : Project.getOpenTextWindows()) {
                        for (CTabItem ci : win.getTabFolder().getItems()) {
                            CompositeTab ct = (CompositeTab) ci;
                            if (df.equals(ct.getState().getFileNameObj())) {
                                foundTab = true;
                                break;
                            }
                        }
                        if (foundTab) {
                            break;
                        }
                    }
                    if (foundTab && OpenInWhat.EDITOR_3D != where) {
                        return null;
                    }
                }

                df.setProjectFile(df.getNewName().startsWith(Project.getProjectPath()));
                if (original.isProjectFile()) {
                    openDatFile(df, where, null);
                    return df;
                }
                {
                    @SuppressWarnings("unchecked")
                    List<DatFile> cachedReferences = (List<DatFile>) this.treeItemProjectPartsPtr[0].getData();
                    if (cachedReferences.contains(df)) {
                        openDatFile(df, where, null);
                        return df;
                    }
                }
                {
                    @SuppressWarnings("unchecked")
                    List<DatFile> cachedReferences = (List<DatFile>) this.treeItemProjectSubpartsPtr[0].getData();
                    if (cachedReferences.contains(df)) {
                        openDatFile(df, where, null);
                        return df;
                    }
                }
                {
                    @SuppressWarnings("unchecked")
                    List<DatFile> cachedReferences = (List<DatFile>) this.treeItemProjectPrimitivesPtr[0].getData();
                    if (cachedReferences.contains(df)) {
                        openDatFile(df, where, null);
                        return df;
                    }
                }
                {
                    @SuppressWarnings("unchecked")
                    List<DatFile> cachedReferences = (List<DatFile>) this.treeItemProjectPrimitives48Ptr[0].getData();
                    if (cachedReferences.contains(df)) {
                        openDatFile(df, where, null);
                        return df;
                    }
                }
                {
                    @SuppressWarnings("unchecked")
                    List<DatFile> cachedReferences = (List<DatFile>) this.treeItemProjectPrimitives8Ptr[0].getData();
                    if (cachedReferences.contains(df)) {
                        openDatFile(df, where, null);
                        return df;
                    }
                }
                type = original.getType();
                df = original;
            }

            TreeItem ti;
            switch (type) {
            case PART:
            {
                @SuppressWarnings("unchecked")
                List<DatFile> cachedReferences = (List<DatFile>) this.treeItemProjectPartsPtr[0].getData();
                if (cachedReferences != null) cachedReferences.add(df);
            }
            ti = new TreeItem(this.treeItemProjectPartsPtr[0]);
            break;
            case SUBPART:
            {
                @SuppressWarnings("unchecked")
                List<DatFile> cachedReferences = (List<DatFile>) this.treeItemProjectSubpartsPtr[0].getData();
                if (cachedReferences != null) cachedReferences.add(df);
            }
            ti = new TreeItem(this.treeItemProjectSubpartsPtr[0]);
            break;
            case PRIMITIVE:
            {
                @SuppressWarnings("unchecked")
                List<DatFile> cachedReferences = (List<DatFile>) this.treeItemProjectPrimitivesPtr[0].getData();
                if (cachedReferences != null) cachedReferences.add(df);
            }
            ti = new TreeItem(this.treeItemProjectPrimitivesPtr[0]);
            break;
            case PRIMITIVE48:
            {
                @SuppressWarnings("unchecked")
                List<DatFile> cachedReferences = (List<DatFile>) this.treeItemProjectPrimitives48Ptr[0].getData();
                if (cachedReferences != null) cachedReferences.add(df);
            }
            ti = new TreeItem(this.treeItemProjectPrimitives48Ptr[0]);
            break;
            case PRIMITIVE8:
            {
                @SuppressWarnings("unchecked")
                List<DatFile> cachedReferences = (List<DatFile>) this.treeItemProjectPrimitives8Ptr[0].getData();
                if (cachedReferences != null) cachedReferences.add(df);
            }
            ti = new TreeItem(this.treeItemProjectPrimitives8Ptr[0]);
            break;
            default:
            {
                @SuppressWarnings("unchecked")
                List<DatFile> cachedReferences = (List<DatFile>) this.treeItemProjectPartsPtr[0].getData();
                if (cachedReferences != null) cachedReferences.add(df);
            }
            ti = new TreeItem(this.treeItemProjectPartsPtr[0]);
            break;
            }

            StringBuilder nameSb = new StringBuilder(new File(df.getNewName()).getName());

            nameSb.append(I18n.E3D_NEW_FILE);

            ti.setText(nameSb.toString());
            ti.setData(df);

            if (canRevert && Project.getUnsavedFiles().contains(df) && Editor3DWindow.getWindow().revert(df)) {
                boolean foundTab = false;
                for (EditorTextWindow win : Project.getOpenTextWindows()) {
                    for (CTabItem ci : win.getTabFolder().getItems()) {
                        CompositeTab ct = (CompositeTab) ci;
                        if (df.equals(ct.getState().getFileNameObj())) {
                            foundTab = true;
                            break;
                        }
                    }
                    if (foundTab) {
                        break;
                    }
                }
                if (foundTab && OpenInWhat.EDITOR_3D != where) {
                    updateTreeUnsavedEntries();
                    return null;
                }
            }

            updateTreeUnsavedEntries();

            openDatFile(df, where, null);
            return df;
        } else {
            NLogger.error(Editor3DWindow.class, new IllegalArgumentException("No filename to open was specified.")); //$NON-NLS-1$
        }

        return null;
    }

    public boolean openDatFile(DatFile df, OpenInWhat where, ApplicationWindow tWin) {
        if (where == OpenInWhat.EDITOR_3D || where == OpenInWhat.EDITOR_TEXT_AND_3D) {
            if (renders.isEmpty()) {
                if ("%EMPTY%".equals(Editor3DWindow.getSashForm().getChildren()[1].getData())) { //$NON-NLS-1$
                    int[] mainSashWeights = Editor3DWindow.getSashForm().getWeights();
                    Editor3DWindow.getSashForm().getChildren()[1].dispose();
                    CompositeContainer cmpContainer = new CompositeContainer(Editor3DWindow.getSashForm(), false);
                    cmpContainer.moveBelow(Editor3DWindow.getSashForm().getChildren()[0]);
                    df.parseForData(true);
                    Project.setFileToEdit(df);
                    boolean hasState = hasState(df, cmpContainer.getComposite3D());
                    cmpContainer.getComposite3D().setLockableDatFileReference(df);
                    if (!hasState) cmpContainer.getComposite3D().getModifier().zoomToFit();
                    Editor3DWindow.getSashForm().getParent().layout();
                    Editor3DWindow.getSashForm().setWeights(mainSashWeights);
                }
            } else {
                boolean canUpdate = false;
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (!c3d.isDatFileLockedOnDisplay()) {
                        canUpdate = true;
                        break;
                    }
                }
                if (!canUpdate) {
                    for (OpenGLRenderer renderer : renders) {
                        Composite3D c3d = renderer.getC3D();
                        c3d.getModifier().switchLockedDat(false);
                    }
                }
                final VertexManager vm = df.getVertexManager();
                if (vm.isModified()) {
                    df.setText(df.getText());
                }
                df.parseForData(true);
                Project.setFileToEdit(df);
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (!c3d.isDatFileLockedOnDisplay()) {
                        boolean hasState = hasState(df, c3d);
                        c3d.setLockableDatFileReference(df);
                        if (!hasState) c3d.getModifier().zoomToFit();
                    }
                }
                if (!canUpdate) {
                    for (OpenGLRenderer renderer : renders) {
                        Composite3D c3d = renderer.getC3D();
                        c3d.getModifier().switchLockedDat(true);
                    }
                }
            }
            updateTreeSelectedDatFile(df);
        }

        if (where == OpenInWhat.EDITOR_TEXT || where == OpenInWhat.EDITOR_TEXT_AND_3D) {

            for (EditorTextWindow w : Project.getOpenTextWindows()) {
                final CompositeTabFolder cTabFolder = w.getTabFolder();
                for (CTabItem t : cTabFolder.getItems()) {
                    if (df.equals(((CompositeTab) t).getState().getFileNameObj())) {
                        if (Project.getUnsavedFiles().contains(df)) {
                            cTabFolder.setSelection(t);
                            ((CompositeTab) t).getControl().getShell().forceActive();
                        } else {
                            CompositeTab tbtmnewItem = new CompositeTab(cTabFolder, SWT.CLOSE);
                            tbtmnewItem.setFolderAndWindow(cTabFolder, w);
                            tbtmnewItem.getState().setFileNameObj(View.DUMMY_DATFILE);
                            w.closeTabWithDatfile(df);
                            tbtmnewItem.getState().setFileNameObj(df);
                            cTabFolder.setSelection(tbtmnewItem);
                            tbtmnewItem.getControl().getShell().forceActive();
                            if (w.isSeperateWindow()) {
                                w.open();
                            }
                            Project.getParsedFiles().add(df);
                            Project.addOpenedFile(df);
                            tbtmnewItem.parseForErrorAndHints();
                            tbtmnewItem.getTextComposite().redraw();
                            return true;
                        }
                        if (w.isSeperateWindow()) {
                            w.open();
                        }
                        return w == tWin;
                    }
                }
            }

            if (tWin == null) {
                EditorTextWindow w;
                // Project.getParsedFiles().add(df); IS NECESSARY HERE
                Project.getParsedFiles().add(df);
                Project.addOpenedFile(df);
                if (!Project.getOpenTextWindows().isEmpty() && !(w = Project.getOpenTextWindows().iterator().next()).isSeperateWindow()) {
                    w.openNewDatFileTab(df, true);
                } else {
                    new EditorTextWindow().run(df, false);
                }
            }
        }
        return false;
    }

    public void disableSelectionTab() {
        if (Thread.currentThread() == Display.getDefault().getThread()) {
            updatingSelectionTab = true;
            txtLinePtr[0].setText(""); //$NON-NLS-1$
            btnMoveAdjacentData2Ptr[0].setSelection(false);
            spnSelectionAnglePtr[0].setEnabled(false);
            spnSelectionLengthPtr[0].setEnabled(false);
            spnSelectionX1Ptr[0].setEnabled(false);
            spnSelectionY1Ptr[0].setEnabled(false);
            spnSelectionZ1Ptr[0].setEnabled(false);
            spnSelectionX2Ptr[0].setEnabled(false);
            spnSelectionY2Ptr[0].setEnabled(false);
            spnSelectionZ2Ptr[0].setEnabled(false);
            spnSelectionX3Ptr[0].setEnabled(false);
            spnSelectionY3Ptr[0].setEnabled(false);
            spnSelectionZ3Ptr[0].setEnabled(false);
            spnSelectionX4Ptr[0].setEnabled(false);
            spnSelectionY4Ptr[0].setEnabled(false);
            spnSelectionZ4Ptr[0].setEnabled(false);
            spnSelectionAnglePtr[0].setValue(BigDecimal.ZERO);
            spnSelectionLengthPtr[0].setValue(BigDecimal.ONE);
            spnSelectionX1Ptr[0].setValue(BigDecimal.ZERO);
            spnSelectionY1Ptr[0].setValue(BigDecimal.ZERO);
            spnSelectionZ1Ptr[0].setValue(BigDecimal.ZERO);
            spnSelectionX2Ptr[0].setValue(BigDecimal.ZERO);
            spnSelectionY2Ptr[0].setValue(BigDecimal.ZERO);
            spnSelectionZ2Ptr[0].setValue(BigDecimal.ZERO);
            spnSelectionX3Ptr[0].setValue(BigDecimal.ZERO);
            spnSelectionY3Ptr[0].setValue(BigDecimal.ZERO);
            spnSelectionZ3Ptr[0].setValue(BigDecimal.ZERO);
            spnSelectionX4Ptr[0].setValue(BigDecimal.ZERO);
            spnSelectionY4Ptr[0].setValue(BigDecimal.ZERO);
            spnSelectionZ4Ptr[0].setValue(BigDecimal.ZERO);
            lblSelectionAnglePtr[0].setText(I18n.E3D_PROTRACTOR_ANGLE);
            lblSelectionLengthPtr[0].setText(I18n.E3D_PROTRACTOR_LENGTH);
            lblSelectionX1Ptr[0].setText(I18n.E3D_POSITION_X1);
            lblSelectionY1Ptr[0].setText(I18n.E3D_POSITION_Y1);
            lblSelectionZ1Ptr[0].setText(I18n.E3D_POSITION_Z1);
            lblSelectionX2Ptr[0].setText(I18n.E3D_POSITION_X2);
            lblSelectionY2Ptr[0].setText(I18n.E3D_POSITION_Y2);
            lblSelectionZ2Ptr[0].setText(I18n.E3D_POSITION_Z2);
            lblSelectionX3Ptr[0].setText(I18n.E3D_POSITION_X3);
            lblSelectionY3Ptr[0].setText(I18n.E3D_POSITION_Y3);
            lblSelectionZ3Ptr[0].setText(I18n.E3D_POSITION_Z3);
            lblSelectionX4Ptr[0].setText(I18n.E3D_POSITION_X4);
            lblSelectionY4Ptr[0].setText(I18n.E3D_POSITION_Y4);
            lblSelectionZ4Ptr[0].setText(I18n.E3D_POSITION_Z4);
            lblSelectionZ4Ptr[0].getParent().layout();
            updatingSelectionTab = false;
        } else {
            NLogger.error(getClass(), new SWTException(SWT.ERROR_THREAD_INVALID_ACCESS, "A wrong thread tries to access the GUI!")); //$NON-NLS-1$
            Display.getDefault().asyncExec(() -> {
                try {
                    updatingSelectionTab = true;
                    txtLinePtr[0].setText(""); //$NON-NLS-1$
                    btnMoveAdjacentData2Ptr[0].setSelection(false);
                    spnSelectionAnglePtr[0].setEnabled(false);
                    spnSelectionLengthPtr[0].setEnabled(false);
                    spnSelectionX1Ptr[0].setEnabled(false);
                    spnSelectionY1Ptr[0].setEnabled(false);
                    spnSelectionZ1Ptr[0].setEnabled(false);
                    spnSelectionX2Ptr[0].setEnabled(false);
                    spnSelectionY2Ptr[0].setEnabled(false);
                    spnSelectionZ2Ptr[0].setEnabled(false);
                    spnSelectionX3Ptr[0].setEnabled(false);
                    spnSelectionY3Ptr[0].setEnabled(false);
                    spnSelectionZ3Ptr[0].setEnabled(false);
                    spnSelectionX4Ptr[0].setEnabled(false);
                    spnSelectionY4Ptr[0].setEnabled(false);
                    spnSelectionZ4Ptr[0].setEnabled(false);
                    spnSelectionX1Ptr[0].setValue(BigDecimal.ZERO);
                    spnSelectionY1Ptr[0].setValue(BigDecimal.ZERO);
                    spnSelectionZ1Ptr[0].setValue(BigDecimal.ZERO);
                    spnSelectionX2Ptr[0].setValue(BigDecimal.ZERO);
                    spnSelectionY2Ptr[0].setValue(BigDecimal.ZERO);
                    spnSelectionZ2Ptr[0].setValue(BigDecimal.ZERO);
                    spnSelectionX3Ptr[0].setValue(BigDecimal.ZERO);
                    spnSelectionY3Ptr[0].setValue(BigDecimal.ZERO);
                    spnSelectionZ3Ptr[0].setValue(BigDecimal.ZERO);
                    spnSelectionX4Ptr[0].setValue(BigDecimal.ZERO);
                    spnSelectionY4Ptr[0].setValue(BigDecimal.ZERO);
                    spnSelectionZ4Ptr[0].setValue(BigDecimal.ZERO);
                    lblSelectionAnglePtr[0].setText(I18n.E3D_PROTRACTOR_ANGLE);
                    lblSelectionLengthPtr[0].setText(I18n.E3D_PROTRACTOR_LENGTH);
                    lblSelectionX1Ptr[0].setText(I18n.E3D_POSITION_X1);
                    lblSelectionY1Ptr[0].setText(I18n.E3D_POSITION_Y1);
                    lblSelectionZ1Ptr[0].setText(I18n.E3D_POSITION_Z1);
                    lblSelectionX2Ptr[0].setText(I18n.E3D_POSITION_X2);
                    lblSelectionY2Ptr[0].setText(I18n.E3D_POSITION_Y2);
                    lblSelectionZ2Ptr[0].setText(I18n.E3D_POSITION_Z2);
                    lblSelectionX3Ptr[0].setText(I18n.E3D_POSITION_X3);
                    lblSelectionY3Ptr[0].setText(I18n.E3D_POSITION_Y3);
                    lblSelectionZ3Ptr[0].setText(I18n.E3D_POSITION_Z3);
                    lblSelectionX4Ptr[0].setText(I18n.E3D_POSITION_X4);
                    lblSelectionY4Ptr[0].setText(I18n.E3D_POSITION_Y4);
                    lblSelectionZ4Ptr[0].setText(I18n.E3D_POSITION_Z4);
                    lblSelectionZ4Ptr[0].getParent().layout();
                    updatingSelectionTab = false;
                } catch (Exception ex) {
                    NLogger.error(getClass(), ex);
                }
            });
        }
    }

    public static List<OpenGLRenderer> getRenders() {
        return renders;
    }

    public SearchWindow getSearchWindow() {
        return searchWindow;
    }

    public void setSearchWindow(SearchWindow searchWindow) {
        this.searchWindow = searchWindow;
    }

    public boolean isFileNameAllocated(String dir, DatFile df, boolean createNew) {

        TreeItem[] folders = new TreeItem[15];
        folders[0] = treeItemOfficialPartsPtr[0];
        folders[1] = treeItemOfficialPrimitivesPtr[0];
        folders[2] = treeItemOfficialPrimitives8Ptr[0];
        folders[3] = treeItemOfficialPrimitives48Ptr[0];
        folders[4] = treeItemOfficialSubpartsPtr[0];

        folders[5] = treeItemUnofficialPartsPtr[0];
        folders[6] = treeItemUnofficialPrimitivesPtr[0];
        folders[7] = treeItemUnofficialPrimitives8Ptr[0];
        folders[8] = treeItemUnofficialPrimitives48Ptr[0];
        folders[9] = treeItemUnofficialSubpartsPtr[0];

        folders[10] = treeItemProjectPartsPtr[0];
        folders[11] = treeItemProjectPrimitivesPtr[0];
        folders[12] = treeItemProjectPrimitives8Ptr[0];
        folders[13] = treeItemProjectPrimitives48Ptr[0];
        folders[14] = treeItemProjectSubpartsPtr[0];

        for (TreeItem folder : folders) {
            @SuppressWarnings("unchecked")
            List<DatFile> cachedReferences =(List<DatFile>) folder.getData();
            for (DatFile d : cachedReferences) {
                if ((createNew || !df.equals(d)) && (dir.equals(d.getOldName()) || dir.equals(d.getNewName()))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return null if the file is not allocated
     */
    private DatFile isFileNameAllocated2(String dir) {

        TreeItem[] folders = new TreeItem[15];
        folders[0] = treeItemOfficialPartsPtr[0];
        folders[1] = treeItemOfficialPrimitivesPtr[0];
        folders[2] = treeItemOfficialPrimitives8Ptr[0];
        folders[3] = treeItemOfficialPrimitives48Ptr[0];
        folders[4] = treeItemOfficialSubpartsPtr[0];

        folders[5] = treeItemUnofficialPartsPtr[0];
        folders[6] = treeItemUnofficialPrimitivesPtr[0];
        folders[7] = treeItemUnofficialPrimitives8Ptr[0];
        folders[8] = treeItemUnofficialPrimitives48Ptr[0];
        folders[9] = treeItemUnofficialSubpartsPtr[0];

        folders[10] = treeItemProjectPartsPtr[0];
        folders[11] = treeItemProjectPrimitivesPtr[0];
        folders[12] = treeItemProjectPrimitives8Ptr[0];
        folders[13] = treeItemProjectPrimitives48Ptr[0];
        folders[14] = treeItemProjectSubpartsPtr[0];

        for (TreeItem folder : folders) {
            @SuppressWarnings("unchecked")
            List<DatFile> cachedReferences =(List<DatFile>) folder.getData();
            // Null-check is only required when a file is opened on program start
            if (cachedReferences == null) {
                continue;
            }
            for (DatFile d : cachedReferences) {
                if (dir.equals(d.getOldName()) || dir.equals(d.getNewName())) {
                    return d;
                }
            }
        }
        return null;
    }

    public void updatePrimitiveLabel(Primitive p) {
        if (lblSelectedPrimitiveItemPtr[0] == null) return;
        if (p == null) {
            lblSelectedPrimitiveItemPtr[0].setText(I18n.E3D_NO_PRIMITIVE_SELECTED);
        } else {
            lblSelectedPrimitiveItemPtr[0].setText(p.toString());
        }
        lblSelectedPrimitiveItemPtr[0].getParent().layout();
    }

    public CompositePrimitive getCompositePrimitive() {
        return cmpPrimitivesPtr[0];
    }

    public static AtomicBoolean getAlive() {
        return alive;
    }

    public List<String> getRecentItems() {
        return recentItems;
    }

    private void setLineSize(SphereGL20 sp, SphereGL20 spInv, float lineWidth1000, float lineWidth, float lineWidthGL, NButton btn) {
        final boolean useLegacyGL = WorkbenchManager.getUserSettingState().getOpenGLVersion() == 20;
        View.lineWidth1000 = lineWidth1000;
        View.lineWidth = lineWidth;
        View.lineWidthGL = lineWidthGL;
        if (useLegacyGL) {
            GL20Primitives.sphere = sp;
            GL20Primitives.sphereInv = spInv;
            compileAll(false);
        }
        clickSingleBtn(btn);
    }

    public void compileAll(boolean forceParsing) {
        Set<DatFile> dfs = new HashSet<>();
        for (OpenGLRenderer renderer : renders) {
            dfs.add(renderer.getC3D().getLockableDatFileReference());
        }
        for (DatFile df : dfs) {
            df.getVertexManager().addSnapshot();
            SubfileCompiler.compile(df, false, forceParsing);
        }
    }

    public void initAllRenderers() {
        for (OpenGLRenderer renderer : renders) {
            final Composite3D c3d = renderer.getC3D();
            final GLCanvas canvas = c3d.getCanvas();
            if (!canvas.isCurrent()) {
                canvas.setCurrent();
                GL.setCapabilities(c3d.getCapabilities());
            }
            renderer.init();
        }
        final CompositePrimitive cp = getCompositePrimitive();
        final GLCanvas canvas = cp.getCanvas();
        if (!canvas.isCurrent()) {
            canvas.setCurrent();
            GL.setCapabilities(cp.getCapabilities());
        }
        cp.getOpenGL().init();
    }

    public void regainFocus() {
        for (OpenGLRenderer r : renders) {
            if (r.getC3D().getLockableDatFileReference().equals(Project.getFileToEdit()) && !r.getC3D().isDisposed() && !r.getC3D().getCanvas().isDisposed()) {
                r.getC3D().getCanvas().setFocus();
                return;
            }
        }

        for (OpenGLRenderer r : renders) {
            if (!r.getC3D().isDisposed() && !r.getC3D().getCanvas().isDisposed()) {
                r.getC3D().getCanvas().setFocus();
                return;
            }
        }
    }

    public void mntmManipulatorToOrigin() {
        if (Project.getFileToEdit() != null) {
            for (OpenGLRenderer renderer : renders) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                    c3d.getManipulator().positionToOrigin();
                }
            }
        }
        regainFocus();
    }

    public void mntmManipulatorToWorld() {
        if (Project.getFileToEdit() != null) {
            for (OpenGLRenderer renderer : renders) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                    Vector4f t = new Vector4f(c3d.getManipulator().getPosition());
                    BigDecimal[] tP = c3d.getManipulator().getAccuratePosition();
                    c3d.getManipulator().reset();
                    c3d.getManipulator().getPosition().set(t);
                    c3d.getManipulator().setAccuratePosition(tP[0], tP[1], tP[2]);
                }
            }
        }
        regainFocus();
    }

    public void mntmManipulatorXReverse() {
        for (OpenGLRenderer renderer : renders) {
            Composite3D c3d = renderer.getC3D();
            if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                Vector4f.sub(new Vector4f(0f, 0f, 0f, 2f), c3d.getManipulator().getXaxis(), c3d.getManipulator().getXaxis());
                BigDecimal[] a = c3d.getManipulator().getAccurateXaxis();
                c3d.getManipulator().setAccurateXaxis(a[0].negate(), a[1].negate(), a[2].negate());
            }
        }
        regainFocus();
    }

    public void mntmManipulatorYReverse() {
        for (OpenGLRenderer renderer : renders) {
            Composite3D c3d = renderer.getC3D();
            if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                Vector4f.sub(new Vector4f(0f, 0f, 0f, 2f), c3d.getManipulator().getYaxis(), c3d.getManipulator().getYaxis());
                BigDecimal[] a = c3d.getManipulator().getAccurateYaxis();
                c3d.getManipulator().setAccurateYaxis(a[0].negate(), a[1].negate(), a[2].negate());
            }
        }
        regainFocus();
    }

    public void mntmManipulatorZReverse() {
        for (OpenGLRenderer renderer : renders) {
            Composite3D c3d = renderer.getC3D();
            if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                Vector4f.sub(new Vector4f(0f, 0f, 0f, 2f), c3d.getManipulator().getZaxis(), c3d.getManipulator().getZaxis());
                BigDecimal[] a = c3d.getManipulator().getAccurateZaxis();
                c3d.getManipulator().setAccurateZaxis(a[0].negate(), a[1].negate(), a[2].negate());
            }
        }
        regainFocus();
    }

    public void mntmManipulatorCameraToPos() {
        for (OpenGLRenderer renderer : renders) {
            Composite3D c3d = renderer.getC3D();
            Vector4f pos = c3d.getManipulator().getPosition();
            Vector4f a1 = c3d.getManipulator().getXaxis();
            Vector4f a2 = c3d.getManipulator().getYaxis();
            Vector4f a3 = c3d.getManipulator().getZaxis();
            if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                c3d.setClassicPerspective(false);
                WidgetSelectionHelper.unselectAllChildButtons(c3d.getViewAnglesMenu());
                Matrix4f rot = new Matrix4f();
                Matrix4f.setIdentity(rot);
                rot.m00 = a1.x;
                rot.m10 = a1.y;
                rot.m20 = a1.z;
                rot.m01 = a2.x;
                rot.m11 = a2.y;
                rot.m21 = a2.z;
                rot.m02 = a3.x;
                rot.m12 = a3.y;
                rot.m22 = a3.z;
                c3d.getRotation().load(rot);
                Matrix4f trans = new Matrix4f();
                Matrix4f.setIdentity(trans);
                trans.translate(new Vector3f(-pos.x, -pos.y, -pos.z));
                c3d.getTranslation().load(trans);
                c3d.getPerspectiveCalculator().calculateOriginData();
            }
        }
        regainFocus();
    }

    public void mntmManipulatorToAverage() {
        if (Project.getFileToEdit() != null) {
            Vector4f avg = Project.getFileToEdit().getVertexManager().getSelectionCenter();
            for (OpenGLRenderer renderer : renders) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                    c3d.getManipulator().getPosition().set(avg.x, avg.y, avg.z, 1f);
                    c3d.getManipulator().setAccuratePosition(new BigDecimal(avg.x / 1000f), new BigDecimal(avg.y / 1000f), new BigDecimal(avg.z / 1000f));
                }
            }
        }
        regainFocus();
    }

    public void mntmManipulatorToSubfile() {
        if (Project.getFileToEdit() != null) {
            Set<GData1> subfiles = Project.getFileToEdit().getVertexManager().getSelectedSubfiles();
            if (!subfiles.isEmpty()) {
                final GData1 subfile = subfiles.iterator().next();
                Manipulator origin = null;
                for (OpenGLRenderer renderer : renders) {
                    final Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        Matrix4f m = subfile.getProductMatrix();
                        Matrix mP = subfile.getAccurateProductMatrix();
                        MatrixOperations.moveManipulatorToSubfileOrCSGMatrix(c3d, mP, m);
                        origin = c3d.getManipulator();
                        break;
                    }
                }
                if (origin != null) {
                    for (OpenGLRenderer renderer : renders) {
                        renderer.getC3D().getManipulator().copyState(origin);
                    }
                }
            }
        }
        regainFocus();
    }

    public void mntmManipulatorSubfileTo(boolean resetScale) {
        if (Project.getFileToEdit() != null) {
            VertexManager vm = Project.getFileToEdit().getVertexManager();
            Set<GData1> subfiles = vm.getSelectedSubfiles();
            if (!subfiles.isEmpty()) {
                GData1 subfile = null;
                for (GData1 g1 : subfiles) {
                    if (vm.getLineLinkedToVertices().containsKey(g1)) {
                        subfile = g1;
                        break;
                    }
                }
                if (subfile == null) {
                    return;
                }
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        vm.addSnapshot();
                        vm.backupHideShowState();
                        Manipulator ma = c3d.getManipulator();
                        if (resetScale) {
                            vm.transformSubfile(subfile, ma.getAccurateMatrix(), true, true);
                        } else {
                            vm.transformSubfile(subfile, Matrix.mul(
                                    ma.getAccurateMatrix(),
                                    MatrixOperations.removeRotationAndTranslation(subfile.getAccurateProductMatrix())), true, true);
                        }
                        break;
                    }
                }
            }
        }
        regainFocus();
    }

    public void mntmManipulatorToVertex() {
        for (OpenGLRenderer renderer : renders) {
            Composite3D c3d = renderer.getC3D();
            if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                float minDist = Float.MAX_VALUE;
                Vector4f next = new Vector4f(c3d.getManipulator().getPosition());
                Vector4f min = new Vector4f(c3d.getManipulator().getPosition());
                VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                Set<Vertex> vertices;
                if (vm.getSelectedVertices().isEmpty()) {
                    vertices = vm.getVertices();
                } else {
                    vertices = vm.getSelectedVertices();
                }
                Vertex minVertex = new Vertex(0f, 0f, 0f);
                for (Vertex vertex : vertices) {
                    Vector4f sub = Vector4f.sub(next, vertex.toVector4f(), null);
                    float d2 = sub.lengthSquared();
                    if (d2 < minDist) {
                        minVertex = vertex;
                        minDist = d2;
                        min = vertex.toVector4f();
                    }
                }
                c3d.getManipulator().getPosition().set(min.x, min.y, min.z, 1f);
                c3d.getManipulator().setAccuratePosition(minVertex.xp, minVertex.yp, minVertex.zp);
            }
        }
        regainFocus();
    }

    public void mntmManipulatorToEdge() {
        for (OpenGLRenderer renderer : renders) {
            Composite3D c3d = renderer.getC3D();
            if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                Vector4f min;
                VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                final Vertex[] result = vm.getMinimalDistanceVertexToLines(new Vertex(c3d.getManipulator().getPosition()));
                min = result[0].toVector4f();
                c3d.getManipulator().getPosition().set(min.x, min.y, min.z, 1f);
                c3d.getManipulator().setAccuratePosition(new BigDecimal(min.x / 1000f), new BigDecimal(min.y / 1000f), new BigDecimal(min.z / 1000f));
                if (result[1] != null) {
                    min = result[1].toVector4f();
                    if (min.lengthSquared() > 0) {
                        min.normalise();
                        Vector4f n = min;

                        float tx = 1f;
                        float ty;
                        float tz;

                        if (n.x <= 0f) {
                            tx = -1;
                        }

                        if (Math.abs(Vector3f.cross(new Vector3f(n.x, n.y, n.z), new Vector3f(0f, 0f, tx), null).length()) > .00001f) {
                            tz = tx;
                            tx = 0f;
                            ty = 0f;
                        } else if (Math.abs(Vector3f.cross(new Vector3f(n.x, n.y, n.z), new Vector3f(tx, 0f, 0f), null).length()) > .00001f) {
                            ty = 0f;
                            tz = 0f;
                        } else {
                            regainFocus();
                            return;
                        }

                        Vector3f cross = (Vector3f) Vector3f.cross(new Vector3f(n.x, n.y, n.z), new Vector3f(tx, ty, tz), null).normalise();
                        c3d.getManipulator().getZaxis().set(n.x, n.y, n.z, 1f);
                        c3d.getManipulator().getXaxis().set(cross.x, cross.y, cross.z, 1f);
                        Vector4f zaxis = c3d.getManipulator().getZaxis();
                        Vector4f xaxis = c3d.getManipulator().getXaxis();
                        cross = Vector3f.cross(new Vector3f(xaxis.x, xaxis.y, xaxis.z), new Vector3f(zaxis.x, zaxis.y, zaxis.z), null);
                        c3d.getManipulator().getYaxis().set(cross.x, cross.y, cross.z, 1f);

                        c3d.getManipulator().setAccurateXaxis(new BigDecimal(c3d.getManipulator().getXaxis().x), new BigDecimal(c3d.getManipulator().getXaxis().y),
                                new BigDecimal(c3d.getManipulator().getXaxis().z));
                        c3d.getManipulator().setAccurateYaxis(new BigDecimal(c3d.getManipulator().getYaxis().x), new BigDecimal(c3d.getManipulator().getYaxis().y),
                                new BigDecimal(c3d.getManipulator().getYaxis().z));
                        c3d.getManipulator().setAccurateZaxis(new BigDecimal(c3d.getManipulator().getZaxis().x), new BigDecimal(c3d.getManipulator().getZaxis().y),
                                new BigDecimal(c3d.getManipulator().getZaxis().z));
                    }
                }
            }
        }
        regainFocus();
    }

    public void mntmManipulatorToSurface() {
        for (OpenGLRenderer renderer : renders) {
            Composite3D c3d = renderer.getC3D();
            if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                Vector4f min;
                VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                min = vm.getMinimalDistanceVertexToSurfaces(new Vertex(c3d.getManipulator().getPosition())).toVector4f();
                c3d.getManipulator().getPosition().set(min.x, min.y, min.z, 1f);
                c3d.getManipulator().setAccuratePosition(new BigDecimal(min.x / 1000f), new BigDecimal(min.y / 1000f), new BigDecimal(min.z / 1000f));
            }
        }
        regainFocus();
    }

    public void mntmManipulatorToVertexNormal() {
        for (OpenGLRenderer renderer : renders) {
            Composite3D c3d = renderer.getC3D();
            if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                float minDist = Float.MAX_VALUE;
                Vector4f next = new Vector4f(c3d.getManipulator().getPosition());
                Vertex min = null;
                VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                Set<Vertex> vertices;
                if (vm.getSelectedVertices().isEmpty()) {
                    vertices = vm.getVertices();
                } else {
                    vertices = vm.getSelectedVertices();
                }
                for (Vertex vertex : vertices) {
                    Vector4f sub = Vector4f.sub(next, vertex.toVector4f(), null);
                    float d2 = sub.lengthSquared();
                    if (d2 < minDist) {
                        minDist = d2;
                        min = vertex;
                    }
                }
                vm = c3d.getLockableDatFileReference().getVertexManager();
                Vector4f n = vm.getVertexNormal(min);

                float tx = 1f;
                float ty;
                float tz;

                if (n.x <= 0f) {
                    tx = -1;
                }

                if (Math.abs(Vector3f.cross(new Vector3f(n.x, n.y, n.z), new Vector3f(0f, 0f, tx), null).length()) > .00001f) {
                    tz = tx;
                    tx = 0f;
                    ty = 0f;
                } else if (Math.abs(Vector3f.cross(new Vector3f(n.x, n.y, n.z), new Vector3f(tx, 0f, 0f), null).length()) > .00001f) {
                    ty = 0f;
                    tz = 0f;
                } else {
                    regainFocus();
                    return;
                }

                Vector3f cross = (Vector3f) Vector3f.cross(new Vector3f(n.x, n.y, n.z), new Vector3f(tx, ty, tz), null).normalise();
                c3d.getManipulator().getZaxis().set(n.x, n.y, n.z, 1f);
                c3d.getManipulator().getXaxis().set(cross.x, cross.y, cross.z, 1f);
                Vector4f zaxis = c3d.getManipulator().getZaxis();
                Vector4f xaxis = c3d.getManipulator().getXaxis();
                cross = Vector3f.cross(new Vector3f(xaxis.x, xaxis.y, xaxis.z), new Vector3f(zaxis.x, zaxis.y, zaxis.z), null);
                c3d.getManipulator().getYaxis().set(cross.x, cross.y, cross.z, 1f);

                c3d.getManipulator().setAccurateXaxis(new BigDecimal(c3d.getManipulator().getXaxis().x), new BigDecimal(c3d.getManipulator().getXaxis().y),
                        new BigDecimal(c3d.getManipulator().getXaxis().z));
                c3d.getManipulator().setAccurateYaxis(new BigDecimal(c3d.getManipulator().getYaxis().x), new BigDecimal(c3d.getManipulator().getYaxis().y),
                        new BigDecimal(c3d.getManipulator().getYaxis().z));
                c3d.getManipulator().setAccurateZaxis(new BigDecimal(c3d.getManipulator().getZaxis().x), new BigDecimal(c3d.getManipulator().getZaxis().y),
                        new BigDecimal(c3d.getManipulator().getZaxis().z));
            }
        }
        regainFocus();
    }

    public void mntmManipulatorToEdgeNormal() {
        for (OpenGLRenderer renderer : renders) {
            Composite3D c3d = renderer.getC3D();
            if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                Vector4f n = vm.getMinimalDistanceEdgeNormal(new Vertex(c3d.getManipulator().getPosition()));

                float tx = 1f;
                float ty;
                float tz;

                if (n.x <= 0f) {
                    tx = -1;
                }

                if (Math.abs(Vector3f.cross(new Vector3f(n.x, n.y, n.z), new Vector3f(0f, 0f, tx), null).length()) > .00001f) {
                    tz = tx;
                    tx = 0f;
                    ty = 0f;
                } else if (Math.abs(Vector3f.cross(new Vector3f(n.x, n.y, n.z), new Vector3f(tx, 0f, 0f), null).length()) > .00001f) {
                    ty = 0f;
                    tz = 0f;
                } else {
                    regainFocus();
                    return;
                }

                Vector3f cross = (Vector3f) Vector3f.cross(new Vector3f(n.x, n.y, n.z), new Vector3f(tx, ty, tz), null).normalise();
                c3d.getManipulator().getZaxis().set(n.x, n.y, n.z, 1f);
                c3d.getManipulator().getXaxis().set(cross.x, cross.y, cross.z, 1f);
                Vector4f zaxis = c3d.getManipulator().getZaxis();
                Vector4f xaxis = c3d.getManipulator().getXaxis();
                cross = Vector3f.cross(new Vector3f(xaxis.x, xaxis.y, xaxis.z), new Vector3f(zaxis.x, zaxis.y, zaxis.z), null);
                c3d.getManipulator().getYaxis().set(cross.x, cross.y, cross.z, 1f);

                c3d.getManipulator().setAccurateXaxis(new BigDecimal(c3d.getManipulator().getXaxis().x), new BigDecimal(c3d.getManipulator().getXaxis().y),
                        new BigDecimal(c3d.getManipulator().getXaxis().z));
                c3d.getManipulator().setAccurateYaxis(new BigDecimal(c3d.getManipulator().getYaxis().x), new BigDecimal(c3d.getManipulator().getYaxis().y),
                        new BigDecimal(c3d.getManipulator().getYaxis().z));
                c3d.getManipulator().setAccurateZaxis(new BigDecimal(c3d.getManipulator().getZaxis().x), new BigDecimal(c3d.getManipulator().getZaxis().y),
                        new BigDecimal(c3d.getManipulator().getZaxis().z));
            }
        }
        regainFocus();
    }

    public void mntmManipulatorToSurfaceNormal() {
        for (OpenGLRenderer renderer : renders) {
            Composite3D c3d = renderer.getC3D();
            if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                Vector4f n = vm.getMinimalDistanceSurfaceNormal(new Vertex(c3d.getManipulator().getPosition()));

                float tx = 1f;
                float ty;
                float tz;

                if (n.x <= 0f) {
                    tx = -1;
                }

                if (Math.abs(Vector3f.cross(new Vector3f(n.x, n.y, n.z), new Vector3f(0f, 0f, tx), null).length()) > .00001f) {
                    tz = tx;
                    tx = 0f;
                    ty = 0f;
                } else if (Math.abs(Vector3f.cross(new Vector3f(n.x, n.y, n.z), new Vector3f(tx, 0f, 0f), null).length()) > .00001f) {
                    ty = 0f;
                    tz = 0f;
                } else {
                    regainFocus();
                    return;
                }

                Vector3f cross = (Vector3f) Vector3f.cross(new Vector3f(n.x, n.y, n.z), new Vector3f(tx, ty, tz), null).normalise();
                c3d.getManipulator().getZaxis().set(n.x, n.y, n.z, 1f);
                c3d.getManipulator().getXaxis().set(cross.x, cross.y, cross.z, 1f);
                Vector4f zaxis = c3d.getManipulator().getZaxis();
                Vector4f xaxis = c3d.getManipulator().getXaxis();
                cross = Vector3f.cross(new Vector3f(xaxis.x, xaxis.y, xaxis.z), new Vector3f(zaxis.x, zaxis.y, zaxis.z), null);
                c3d.getManipulator().getYaxis().set(cross.x, cross.y, cross.z, 1f);

                c3d.getManipulator().setAccurateXaxis(new BigDecimal(c3d.getManipulator().getXaxis().x), new BigDecimal(c3d.getManipulator().getXaxis().y),
                        new BigDecimal(c3d.getManipulator().getXaxis().z));
                c3d.getManipulator().setAccurateYaxis(new BigDecimal(c3d.getManipulator().getYaxis().x), new BigDecimal(c3d.getManipulator().getYaxis().y),
                        new BigDecimal(c3d.getManipulator().getYaxis().z));
                c3d.getManipulator().setAccurateZaxis(new BigDecimal(c3d.getManipulator().getZaxis().x), new BigDecimal(c3d.getManipulator().getZaxis().y),
                        new BigDecimal(c3d.getManipulator().getZaxis().z));
            }
        }
        regainFocus();
    }

    public void mntmManipulatorAdjustRotationCenter() {
        for (OpenGLRenderer renderer : renders) {
            Composite3D c3d = renderer.getC3D();
            if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                vm.adjustRotationCenter(c3d, null);
            }
        }
        regainFocus();
    }

    public void mntmManipulatorToVertexPosition() {
        for (OpenGLRenderer renderer : renders) {
            Composite3D c3d = renderer.getC3D();
            if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                final VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                float minDist = Float.MAX_VALUE;
                Vector4f next = new Vector4f(c3d.getManipulator().getPosition());
                Vertex min = null;
                Set<Vertex> vertices;
                if (vm.getSelectedVertices().isEmpty()) {
                    vertices = vm.getVertices();
                } else {
                    vertices = vm.getSelectedVertices();
                }
                for (Vertex vertex : vertices) {
                    Vector4f sub = Vector4f.sub(next, vertex.toVector4f(), null);
                    float d2 = sub.lengthSquared();
                    if (d2 < minDist) {
                        minDist = d2;
                        min = vertex;
                    }
                }
                final Vector4f n;
                if (min == null || min.x == 0f && min.y == 0f && min.z == 0f) {
                    n = new Vector4f(0f, 0f, 1f, 0f);
                } else {
                    n = new Vector4f(min.x, min.y, min.z, 0f);
                }
                n.normalise();
                n.setW(1f);

                float tx = -1f;
                float ty = 0f;
                float tz = 0f;

                if (n.x <= 0f) {
                    tx = 1;
                }

                if (Math.abs(Vector3f.cross(new Vector3f(n.x, n.y, n.z), new Vector3f(0f, 0f, tx), null).length()) > .00001f) {
                    tz = tx;
                    tx = 0f;
                    ty = 0f;
                } else if (Math.abs(Vector3f.cross(new Vector3f(n.x, n.y, n.z), new Vector3f(tx, 0f, 0f), null).length()) > .00001f) {
                    // it is ty = 0
                    // and   tz = 0
                } else {
                    regainFocus();
                    return;
                }

                Vector3f cross = (Vector3f) Vector3f.cross(new Vector3f(n.x, n.y, n.z), new Vector3f(tx, ty, tz), null).normalise();
                c3d.getManipulator().getZaxis().set(n.x, n.y, n.z, 1f);
                c3d.getManipulator().getYaxis().set(cross.x, cross.y, cross.z, 1f);
                Vector4f zaxis = c3d.getManipulator().getZaxis();
                Vector4f yaxis = c3d.getManipulator().getYaxis();
                cross = Vector3f.cross(new Vector3f(yaxis.x, yaxis.y, yaxis.z), new Vector3f(zaxis.x, zaxis.y, zaxis.z), null);
                c3d.getManipulator().getXaxis().set(cross.x, cross.y, cross.z, 1f);

                c3d.getManipulator().setAccurateXaxis(BigDecimal.valueOf(c3d.getManipulator().getXaxis().x), BigDecimal.valueOf(c3d.getManipulator().getXaxis().y),
                        BigDecimal.valueOf(c3d.getManipulator().getXaxis().z));
                c3d.getManipulator().setAccurateYaxis(BigDecimal.valueOf(c3d.getManipulator().getYaxis().x), BigDecimal.valueOf(c3d.getManipulator().getYaxis().y),
                        BigDecimal.valueOf(c3d.getManipulator().getYaxis().z));
                c3d.getManipulator().setAccurateZaxis(BigDecimal.valueOf(c3d.getManipulator().getZaxis().x), BigDecimal.valueOf(c3d.getManipulator().getZaxis().y),
                        BigDecimal.valueOf(c3d.getManipulator().getZaxis().z));
            }
        }
        regainFocus();
    }

    public void mntmManipulatorSwitchYZ() {
        for (OpenGLRenderer renderer : renders) {
            Composite3D c3d = renderer.getC3D();
            if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                Vector4f temp = new Vector4f(c3d.getManipulator().getZaxis());
                c3d.getManipulator().getZaxis().set(c3d.getManipulator().getYaxis());
                c3d.getManipulator().getYaxis().set(temp);
                BigDecimal[] a = c3d.getManipulator().getAccurateYaxis().clone();
                BigDecimal[] b = c3d.getManipulator().getAccurateZaxis().clone();
                c3d.getManipulator().setAccurateYaxis(b[0], b[1], b[2]);
                c3d.getManipulator().setAccurateZaxis(a[0], a[1], a[2]);
            }
        }
        regainFocus();
    }

    public void mntmManipulatorSwitchXZ() {
        for (OpenGLRenderer renderer : renders) {
            Composite3D c3d = renderer.getC3D();
            if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                Vector4f temp = new Vector4f(c3d.getManipulator().getXaxis());
                c3d.getManipulator().getXaxis().set(c3d.getManipulator().getZaxis());
                c3d.getManipulator().getZaxis().set(temp);
                BigDecimal[] a = c3d.getManipulator().getAccurateXaxis().clone();
                BigDecimal[] b = c3d.getManipulator().getAccurateZaxis().clone();
                c3d.getManipulator().setAccurateXaxis(b[0], b[1], b[2]);
                c3d.getManipulator().setAccurateZaxis(a[0], a[1], a[2]);
            }
        }
        regainFocus();
    }

    public void mntmManipulatorSwitchXY() {
        for (OpenGLRenderer renderer : renders) {
            Composite3D c3d = renderer.getC3D();
            if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                Vector4f temp = new Vector4f(c3d.getManipulator().getXaxis());
                c3d.getManipulator().getXaxis().set(c3d.getManipulator().getYaxis());
                c3d.getManipulator().getYaxis().set(temp);
                BigDecimal[] a = c3d.getManipulator().getAccurateXaxis().clone();
                BigDecimal[] b = c3d.getManipulator().getAccurateYaxis().clone();
                c3d.getManipulator().setAccurateXaxis(b[0], b[1], b[2]);
                c3d.getManipulator().setAccurateYaxis(a[0], a[1], a[2]);
            }
        }
        regainFocus();
    }

    public boolean closeDatfile(DatFile df) {
        boolean result2 = false;
        if (Project.getUnsavedFiles().contains(df) && !df.isReadOnly() && !df.isFromPartReview()) {
            MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.CANCEL | SWT.NO);
            messageBox.setText(I18n.DIALOG_UNSAVED_CHANGES_TITLE);

            Object[] messageArguments = {df.getShortName()};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.getLocale());
            formatter.applyPattern(I18n.DIALOG_UNSAVED_CHANGES);
            messageBox.setMessage(formatter.format(messageArguments));

            int result = messageBox.open();

            if (result == SWT.NO) {
                result2 = true;
            } else if (result == SWT.YES) {
                if (df.save()) {
                    Editor3DWindow.getWindow().addRecentFile(df);
                    result2 = true;
                } else {
                    MessageBox messageBoxError = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                    messageBoxError.setText(I18n.DIALOG_ERROR);
                    messageBoxError.setMessage(I18n.DIALOG_CANT_SAVE_FILE);
                    messageBoxError.open();
                    cleanupClosedData();
                    updateTreeUnsavedEntries();
                    regainFocus();
                    return false;
                }
            } else {
                cleanupClosedData();
                updateTreeUnsavedEntries();
                regainFocus();
                return false;
            }
        } else {
            result2 = true;
        }
        updateTreeRemoveEntry(df);
        cleanupClosedData();
        regainFocus();
        return result2;
    }

    private void openFileIn3DEditor(final DatFile df) {
        if (renders.isEmpty()) {

            if ("%EMPTY%".equals(Editor3DWindow.getSashForm().getChildren()[1].getData())) { //$NON-NLS-1$
                int[] mainSashWeights = Editor3DWindow.getSashForm().getWeights();
                Editor3DWindow.getSashForm().getChildren()[1].dispose();
                CompositeContainer cmpContainer = new CompositeContainer(Editor3DWindow.getSashForm(), false);
                cmpContainer.moveBelow(Editor3DWindow.getSashForm().getChildren()[0]);
                addRecentFile(df);
                df.parseForData(true);
                Project.setFileToEdit(df);
                cmpContainer.getComposite3D().setLockableDatFileReference(df);
                df.getVertexManager().addSnapshot();
                Editor3DWindow.getSashForm().getParent().layout();
                Editor3DWindow.getSashForm().setWeights(mainSashWeights);
            }

        } else {

            boolean canUpdate = false;

            for (OpenGLRenderer renderer : renders) {
                Composite3D c3d = renderer.getC3D();
                if (!c3d.isDatFileLockedOnDisplay()) {
                    canUpdate = true;
                    break;
                }
            }

            if (!canUpdate) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    c3d.getModifier().switchLockedDat(false);
                }
            }

            final VertexManager vm = df.getVertexManager();
            if (vm.isModified()) {
                df.setText(df.getText());
            }
            addRecentFile(df);
            df.parseForData(true);

            Project.setFileToEdit(df);
            for (OpenGLRenderer renderer : renders) {
                Composite3D c3d = renderer.getC3D();
                if (!c3d.isDatFileLockedOnDisplay()) {
                    boolean hasState = hasState(df, c3d);
                    c3d.setLockableDatFileReference(df);
                    if (!hasState) c3d.getModifier().zoomToFit();
                }
            }

            df.getVertexManager().addSnapshot();

            if (!canUpdate) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    c3d.getModifier().switchLockedDat(true);
                }
            }
        }
    }

    public void selectTabWithDatFile(DatFile df) {
        for (CTabItem ti : tabFolderOpenDatFilesPtr[0].getItems()) {
            if (df.equals(ti.getData())) {
                tabFolderOpenDatFilesPtr[0].setSelection(ti);
                openFileIn3DEditor(df);
                cleanupClosedData();
                regainFocus();
                break;
            }
        }
    }

    public void closeTabWithDatFile(DatFile df) {
        if (View.DUMMY_DATFILE.equals(df) || df == null) {
            return;
        }
        for (CTabItem ti : tabFolderOpenDatFilesPtr[0].getItems()) {
            if (df.equals(ti.getData())) {
                ti.dispose();
                Project.removeOpenedFile(df);
                if (!closeDatfile(df)) {
                    Project.addOpenedFile(df);
                    updateTabs();
                }
                Editor3DWindow.getWindow().getShell().forceFocus();
                regainFocus();
                break;
            }
        }
    }

    public static AtomicBoolean getNoSyncDeadlock() {
        return no_sync_deadlock;
    }

    private boolean revert(DatFile df) {
        if (df.isReadOnly() || !Project.getUnsavedFiles().contains(df) || df.isVirtual() && df.getText().trim().isEmpty()) {
            regainFocus();
            return false;
        }
        df.getVertexManager().addSnapshot();

        MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
        messageBox.setText(I18n.DIALOG_REVERT_TITLE);

        Object[] messageArguments = {df.getShortName(), df.getLastSavedOpened()};
        MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
        formatter.setLocale(MyLanguage.getLocale());
        formatter.applyPattern(I18n.DIALOG_REVERT);
        messageBox.setMessage(formatter.format(messageArguments));

        int result = messageBox.open();

        if (result == SWT.NO) {
            regainFocus();
            return false;
        }


        boolean canUpdate = false;

        for (OpenGLRenderer renderer : renders) {
            Composite3D c3d = renderer.getC3D();
            if (c3d.getLockableDatFileReference().equals(df)) {
                canUpdate = true;
                break;
            }
        }

        EditorTextWindow tmpW = null;
        CTabItem tmpT = null;
        for (EditorTextWindow w : Project.getOpenTextWindows()) {
            for (CTabItem t : w.getTabFolder().getItems()) {
                if (df.equals(((CompositeTab) t).getState().getFileNameObj())) {
                    canUpdate = true;
                    tmpW = w;
                    tmpT = t;
                    break;
                }
            }
        }

        df.setText(df.getOriginalText());
        df.setOldName(df.getNewName());
        if (!df.isVirtual()) {
            Project.removeUnsavedFile(df);
            updateTreeUnsavedEntries();
        }

        if (canUpdate) {

            df.parseForData(true);
            df.getVertexManager().setModified(true, true);

            if (tmpW != null) {
                tmpW.getTabFolder().setSelection(tmpT);
                ((CompositeTab) tmpT).getControl().getShell().forceActive();
                if (tmpW.isSeperateWindow()) {
                    tmpW.open();
                }
                ((CompositeTab) tmpT).getTextComposite().forceFocus();
            }
        }
        return true;
    }

    public void saveState(final DatFile df, final Composite3D c3d) {
        if (df == null || c3d == null) {
            return;
        }
        {
            Map<Composite3D, org.nschmidt.ldparteditor.composite.Composite3DViewState> states = new HashMap<>();
            if (c3dStates.containsKey(df)) {
                states = c3dStates.get(df);
            } else {
                c3dStates.put(df, states);
            }
            states.remove(c3d);
            states.put(c3d, c3d.exportState());
        }

        // Cleanup old states
        {
            Set<DatFile> allFiles = new HashSet<>();
            for (CTabItem ci : tabFolderOpenDatFilesPtr[0].getItems()) {
                allFiles.add((DatFile) ci.getData());
            }

            Set<DatFile> cachedStates = new HashSet<>();
            cachedStates.addAll(c3dStates.keySet());
            for (DatFile d : cachedStates) {
                if (!allFiles.contains(d)) {
                    c3dStates.remove(d);
                }
            }
        }
    }

    public void loadState(final DatFile df, final Composite3D c3d) {
        if (df == null || c3d == null) {
            return;
        }
        if (c3dStates.containsKey(df))  {
            Map<Composite3D, org.nschmidt.ldparteditor.composite.Composite3DViewState> states = c3dStates.get(df);
            if (states.containsKey(c3d)) {
                c3d.importState(states.get(c3d));
            }
        }
    }

    public boolean hasState(final DatFile df, final Composite3D c3d) {
        return c3dStates.containsKey(df) && c3dStates.get(df).containsKey(c3d);
    }

    private void actionOpenInTextEditor() {
        if (treeParts[0].getSelectionCount() == 1 && treeParts[0].getSelection()[0] != null && treeParts[0].getSelection()[0].getData() instanceof DatFile) {
            DatFile df = (DatFile) treeParts[0].getSelection()[0].getData();
            for (EditorTextWindow w : Project.getOpenTextWindows()) {
                for (CTabItem t : w.getTabFolder().getItems()) {
                    if (df.equals(((CompositeTab) t).getState().getFileNameObj())) {
                        w.getTabFolder().setSelection(t);
                        ((CompositeTab) t).getControl().getShell().forceActive();
                        if (w.isSeperateWindow()) {
                            w.open();
                        }
                        df.getVertexManager().setUpdated(true);
                        return;
                    }
                }
            }

            EditorTextWindow w = null;
            for (EditorTextWindow w2 : Project.getOpenTextWindows()) {
                if (w2.getTabFolder().getItems().length == 0) {
                    w = w2;
                    break;
                }
            }

            // Project.getParsedFiles().add(df); IS NECESSARY HERE
            Project.getParsedFiles().add(df);
            Project.addOpenedFile(df);
            if (!Project.getOpenTextWindows().isEmpty() && (w != null || !(w = Project.getOpenTextWindows().iterator().next()).isSeperateWindow())) {
                w.openNewDatFileTab(df, true);
            } else {
                new EditorTextWindow().run(df, false);
            }
            df.getVertexManager().addSnapshot();
        }
        cleanupClosedData();
        updateTabs();
    }

    private void actionOpenIn3DEditor() {
        if (treeParts[0].getSelectionCount() == 1 && treeParts[0].getSelection()[0] != null && treeParts[0].getSelection()[0].getData() instanceof DatFile) {
            DatFile df = (DatFile) treeParts[0].getSelection()[0].getData();
            openFileIn3DEditor(df);
            updateTreeUnsavedEntries();
            cleanupClosedData();
            regainFocus();
        }
    }

    private void actionRevert() {
        if (treeParts[0].getSelectionCount() == 1 && treeParts[0].getSelection()[0] != null && treeParts[0].getSelection()[0].getData() instanceof DatFile) {
            DatFile df = (DatFile) treeParts[0].getSelection()[0].getData();
            revert(df);
        }
        regainFocus();
    }

    private void actionClose() {
        if (treeParts[0].getSelectionCount() == 1 && treeParts[0].getSelection()[0] != null && treeParts[0].getSelection()[0].getData() instanceof DatFile) {
            DatFile df = (DatFile) treeParts[0].getSelection()[0].getData();
            Project.removeOpenedFile(df);
            if (!closeDatfile(df)) {
                Project.addOpenedFile(df);
                updateTabs();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void actionRename() {
        if (treeParts[0].getSelectionCount() == 1 && treeParts[0].getSelection()[0] != null && treeParts[0].getSelection()[0].getData() instanceof DatFile) {
            DatFile df = (DatFile) treeParts[0].getSelection()[0].getData();
            if (df.isReadOnly()) {
                regainFocus();
                return;
            }
            df.getVertexManager().addSnapshot();

            FileDialog dlg = new FileDialog(Editor3DWindow.getWindow().getShell(), SWT.SAVE);

            File tmp = new File(df.getNewName());
            dlg.setFilterPath(tmp.getAbsolutePath().substring(0, Math.max(0, tmp.getAbsolutePath().length() - tmp.getName().length())));
            dlg.setFileName(tmp.getName());
            dlg.setFilterExtensions(new String[]{"*.dat"}); //$NON-NLS-1$
            dlg.setOverwrite(true);

            // Change the title bar text
            dlg.setText(I18n.DIALOG_RENAME_OR_MOVE);

            // Calling open() will open and run the dialog.
            // It will return the selected file, or
            // null if user cancels
            String newPath = dlg.open();
            if (newPath != null) {

                while (isFileNameAllocated(newPath, df, false)) {
                    MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.RETRY | SWT.CANCEL);
                    messageBox.setText(I18n.DIALOG_ALREADY_ALLOCATED_NAME_TITLE);
                    messageBox.setMessage(I18n.DIALOG_ALREADY_ALLOCATED_NAME);

                    int result = messageBox.open();

                    if (result == SWT.CANCEL) {
                        regainFocus();
                        return;
                    }
                    newPath = dlg.open();
                    if (newPath == null) {
                        regainFocus();
                        return;
                    }
                }


                if (df.isProjectFile() && !newPath.startsWith(Project.getProjectPath())) {

                    MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
                    messageBox.setText(I18n.DIALOG_NO_PROJECT_LOCATION_TITLE);

                    Object[] messageArguments = {new File(newPath).getName()};
                    MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                    formatter.setLocale(MyLanguage.getLocale());
                    formatter.applyPattern(I18n.DIALOG_NO_PROJECT_LOCATION);
                    messageBox.setMessage(formatter.format(messageArguments));

                    int result = messageBox.open();

                    if (result == SWT.NO) {
                        regainFocus();
                        return;
                    }
                }

                df.setNewName(newPath);
                if (!df.getOldName().equals(df.getNewName())) {
                    if (!Project.getUnsavedFiles().contains(df)) {
                        df.parseForData(true);
                        df.getVertexManager().setModified(true, true);
                        Project.getUnsavedFiles().add(df);
                    }
                } else {
                    if (df.getText().equals(df.getOriginalText()) && df.getOldName().equals(df.getNewName())) {
                        Project.removeUnsavedFile(df);
                    }
                }

                df.setProjectFile(df.getNewName().startsWith(Project.getProjectPath()));

                final File f = new File(df.getNewName());
                if (f.getParentFile() != null) {
                    Project.setLastVisitedPath(f.getParentFile().getAbsolutePath());
                }

                Set<EditorTextWindow> windows = new HashSet<>(Project.getOpenTextWindows());
                for (EditorTextWindow win : windows) {
                    win.updateTabWithDatfile(df);
                }
                updateTreeRenamedEntries();
                updateTreeUnsavedEntries();
            }
        } else if (treeParts[0].getSelectionCount() == 1 && treeParts[0].getSelection()[0] != null && treeParts[0].getSelection()[0].equals(treeItemProjectPtr[0])) {
            if (Project.isDefaultProject()) {
                if (ProjectActions.createNewProject(Editor3DWindow.getWindow(), true)) {
                    Project.setLastVisitedPath(Project.getProjectPath());
                }
            } else {
                int result = new NewProjectDialog(true).open();
                if (result == IDialogConstants.OK_ID && !Project.getTempProjectPath().equals(Project.getProjectPath())) {
                    try {
                        while (new File(Project.getTempProjectPath()).isDirectory()) {
                            MessageBox messageBoxError = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.YES | SWT.CANCEL | SWT.NO);
                            messageBoxError.setText(I18n.PROJECT_PROJECT_OVERWRITE_TITLE);
                            messageBoxError.setMessage(I18n.PROJECT_PROJECT_OVERWRITE);
                            int result2 = messageBoxError.open();
                            if (result2 == SWT.CANCEL) {
                                regainFocus();
                                return;
                            } else if (result2 == SWT.YES) {
                                break;
                            } else {
                                result = new NewProjectDialog(true).open();
                                if (result == IDialogConstants.CANCEL_ID) {
                                    regainFocus();
                                    return;
                                }
                            }
                        }
                        Project.copyFolder(new File(Project.getProjectPath()), new File(Project.getTempProjectPath()));
                        Project.deleteFolder(new File(Project.getProjectPath()));
                        // Linked project parts need a new path, because they were copied to a new directory
                        String defaultPrefix = new File(Project.getProjectPath()).getAbsolutePath() + File.separator;
                        String projectPrefix = new File(Project.getTempProjectPath()).getAbsolutePath() + File.separator;
                        Editor3DWindow.getWindow().getProjectParts().getParentItem().setData(Project.getTempProjectPath());
                        Set<DatFile> projectFiles = new HashSet<>();
                        projectFiles.addAll((List<DatFile>) Editor3DWindow.getWindow().getProjectParts().getData());
                        projectFiles.addAll((List<DatFile>) Editor3DWindow.getWindow().getProjectSubparts().getData());
                        projectFiles.addAll((List<DatFile>) Editor3DWindow.getWindow().getProjectPrimitives().getData());
                        projectFiles.addAll((List<DatFile>) Editor3DWindow.getWindow().getProjectPrimitives48().getData());
                        for (DatFile df : projectFiles) {
                            df.getVertexManager().addSnapshot();
                            boolean isUnsaved = Project.getUnsavedFiles().contains(df);
                            boolean isParsed = Project.getParsedFiles().contains(df);
                            Project.getParsedFiles().remove(df);
                            Project.getUnsavedFiles().remove(df);
                            String newName = df.getNewName();
                            String oldName = df.getOldName();
                            df.updateLastModified();
                            if (!newName.startsWith(projectPrefix) && newName.startsWith(defaultPrefix)) {
                                df.setNewName(projectPrefix + newName.substring(defaultPrefix.length()));
                            }
                            if (!oldName.startsWith(projectPrefix) && oldName.startsWith(defaultPrefix)) {
                                df.setOldName(projectPrefix + oldName.substring(defaultPrefix.length()));
                            }
                            df.setProjectFile(df.getNewName().startsWith(Project.getProjectPath()));
                            if (isUnsaved) Project.addUnsavedFile(df);
                            if (isParsed) Project.getParsedFiles().add(df);
                            Project.addOpenedFile(df);
                        }
                        Project.setProjectName(Project.getTempProjectName());
                        Project.setProjectPath(Project.getTempProjectPath());
                        Editor3DWindow.getWindow().getProjectParts().getParentItem().setText(Project.getProjectName());
                        updateTreeUnsavedEntries();
                        Project.updateEditor();
                        Editor3DWindow.getWindow().getShell().update();
                        Project.setLastVisitedPath(Project.getProjectPath());
                    } catch (IOException ioe) {
                        NLogger.error(getClass(), ioe);
                    }
                }
            }
        }
        regainFocus();
    }

    @SuppressWarnings("unchecked")
    private void actionCopyToUnofficial() {
        if (treeParts[0].getSelectionCount() == 1 && treeParts[0].getSelection()[0] != null && treeParts[0].getSelection()[0].getData() instanceof DatFile) {
            DatFile df = (DatFile) treeParts[0].getSelection()[0].getData();
            TreeItem p = treeParts[0].getSelection()[0].getParentItem();
            String targetPathU;
            String targetPathL;
            String targetPathDirU;
            String targetPathDirL;
            TreeItem targetTreeItem;
            boolean projectIsFileOrigin = false;
            if (treeItemProjectPartsPtr[0].equals(p)) {
                targetPathU = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "PARTS"; //$NON-NLS-1$
                targetPathL = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "parts"; //$NON-NLS-1$
                targetTreeItem = treeItemUnofficialPartsPtr[0];
                projectIsFileOrigin = true;
            } else if (treeItemProjectPrimitivesPtr[0].equals(p)) {
                targetPathU = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "P"; //$NON-NLS-1$
                targetPathL = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "p"; //$NON-NLS-1$
                targetTreeItem = treeItemUnofficialPrimitivesPtr[0];
                projectIsFileOrigin = true;
            } else if (treeItemProjectPrimitives48Ptr[0].equals(p)) {
                targetPathU = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "P" + File.separator + "48"; //$NON-NLS-1$ //$NON-NLS-2$
                targetPathL = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "p" + File.separator + "48"; //$NON-NLS-1$ //$NON-NLS-2$
                targetTreeItem = treeItemUnofficialPrimitives48Ptr[0];
                projectIsFileOrigin = true;
            } else if (treeItemProjectPrimitives8Ptr[0].equals(p)) {
                targetPathU = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "P" + File.separator + "8"; //$NON-NLS-1$ //$NON-NLS-2$
                targetPathL = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "p" + File.separator + "8"; //$NON-NLS-1$ //$NON-NLS-2$
                targetTreeItem = treeItemUnofficialPrimitives8Ptr[0];
                projectIsFileOrigin = true;
            } else if (treeItemProjectSubpartsPtr[0].equals(p)) {
                targetPathU = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "PARTS"+ File.separator + "S"; //$NON-NLS-1$ //$NON-NLS-2$
                targetPathL = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "parts"+ File.separator + "s"; //$NON-NLS-1$ //$NON-NLS-2$
                targetTreeItem = treeItemUnofficialSubpartsPtr[0];
                projectIsFileOrigin = true;
            } else if (treeItemOfficialPartsPtr[0].equals(p)) {
                targetPathU = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "PARTS"; //$NON-NLS-1$
                targetPathL = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "parts"; //$NON-NLS-1$
                targetTreeItem = treeItemUnofficialPartsPtr[0];
            } else if (treeItemOfficialPrimitivesPtr[0].equals(p)) {
                targetPathU = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "P"; //$NON-NLS-1$
                targetPathL = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "p"; //$NON-NLS-1$
                targetTreeItem = treeItemUnofficialPrimitivesPtr[0];
            } else if (treeItemOfficialPrimitives48Ptr[0].equals(p)) {
                targetPathU = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "P" + File.separator + "48"; //$NON-NLS-1$ //$NON-NLS-2$
                targetPathL = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "p" + File.separator + "48"; //$NON-NLS-1$ //$NON-NLS-2$
                targetTreeItem = treeItemUnofficialPrimitives48Ptr[0];
            } else if (treeItemOfficialPrimitives8Ptr[0].equals(p)) {
                targetPathU = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "P" + File.separator + "8"; //$NON-NLS-1$ //$NON-NLS-2$
                targetPathL = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "p" + File.separator + "8"; //$NON-NLS-1$ //$NON-NLS-2$
                targetTreeItem = treeItemUnofficialPrimitives8Ptr[0];
            } else if (treeItemOfficialSubpartsPtr[0].equals(p)) {
                targetPathU = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "PARTS"+ File.separator + "S"; //$NON-NLS-1$ //$NON-NLS-2$
                targetPathL = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "parts"+ File.separator + "s"; //$NON-NLS-1$ //$NON-NLS-2$
                targetTreeItem = treeItemUnofficialSubpartsPtr[0];
            } else {
                regainFocus();
                return;
            }

            targetPathDirL = targetPathL;
            targetPathDirU = targetPathU;

            final String newName = new File(df.getNewName()).getName();
            targetPathU = targetPathU + File.separator + newName;
            targetPathL = targetPathL + File.separator + newName;

            DatFile fileToOverwriteU = new DatFile(targetPathU);
            DatFile fileToOverwriteL = new DatFile(targetPathL);

            DatFile targetFile = null;

            TreeItem[] folders = new TreeItem[5];
            folders[0] = treeItemUnofficialPartsPtr[0];
            folders[1] = treeItemUnofficialPrimitivesPtr[0];
            folders[2] = treeItemUnofficialPrimitives48Ptr[0];
            folders[3] = treeItemUnofficialPrimitives8Ptr[0];
            folders[4] = treeItemUnofficialSubpartsPtr[0];

            for (TreeItem folder : folders) {
                List<DatFile> cachedReferences = (List<DatFile>) folder.getData();
                for (DatFile d : cachedReferences) {
                    if (fileToOverwriteU.equals(d) || fileToOverwriteL.equals(d)) {
                        targetFile = d;
                        break;
                    }
                }
            }

            if (new File(targetPathU).exists() || new File(targetPathL).exists() || targetFile != null) {
                MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
                messageBox.setText(I18n.DIALOG_REPLACE_TITLE);

                Object[] messageArguments = {newName};
                MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                formatter.setLocale(MyLanguage.getLocale());
                formatter.applyPattern(I18n.DIALOG_REPLACE);
                messageBox.setMessage(formatter.format(messageArguments));

                int result = messageBox.open();

                if (result == SWT.CANCEL) {
                    regainFocus();
                    return;
                }
            }

            List<List<DatFile>> refResult = null;

            if (new File(targetPathDirL).exists() || new File(targetPathDirU).exists()) {
                if (targetFile == null) {

                    int result = new CopyDialog(getShell(), new File(df.getNewName()).getName()).open();


                    switch (result) {
                    case IDialogConstants.OK_ID:
                        // Copy File Only
                        break;
                    case IDialogConstants.NO_ID:
                        // Copy File and required and related
                        if (projectIsFileOrigin) {
                            refResult = ReferenceParser.checkForReferences(df, References.REQUIRED_AND_RELATED, treeItemProjectPtr[0]);
                        } else {
                            refResult = ReferenceParser.checkForReferences(df, References.REQUIRED_AND_RELATED, treeItemOfficialPtr[0]);
                        }
                        break;
                    case IDialogConstants.YES_ID:
                        // Copy File and required
                        if (projectIsFileOrigin) {
                            refResult = ReferenceParser.checkForReferences(df, References.REQUIRED, treeItemProjectPtr[0]);
                        } else {
                            refResult = ReferenceParser.checkForReferences(df, References.REQUIRED, treeItemOfficialPtr[0]);
                        }
                        break;
                    default:
                        regainFocus();
                        return;
                    }
                    DatFile newDatFile = new DatFile(new File(targetPathDirL).exists() ? targetPathL : targetPathU);
                    // Text exchange includes description exchange
                    newDatFile.setText(df.getText());
                    newDatFile.saveForced();
                    newDatFile.setType(df.getType());
                    ((List<DatFile>) targetTreeItem.getData()).add(newDatFile);
                    TreeItem ti = new TreeItem(targetTreeItem);
                    ti.setText(new File(df.getNewName()).getName());
                    ti.setData(newDatFile);
                } else if (targetFile.equals(df)) { // This can only happen if the user opens the unofficial parts folder as a project
                    MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                    messageBox.setText(I18n.DIALOG_ALREADY_ALLOCATED_NAME_TITLE);
                    messageBox.setMessage(I18n.DIALOG_ALREADY_ALLOCATED_NAME);
                    messageBox.open();
                    regainFocus();
                    return;
                } else {

                    int result = new CopyDialog(getShell(), new File(df.getNewName()).getName()).open();
                    switch (result) {
                    case IDialogConstants.OK_ID:
                        // Copy File Only
                        break;
                    case IDialogConstants.NO_ID:
                        // Copy File and required and related
                        if (projectIsFileOrigin) {
                            refResult = ReferenceParser.checkForReferences(df, References.REQUIRED_AND_RELATED, treeItemProjectPtr[0]);
                        } else {
                            refResult = ReferenceParser.checkForReferences(df, References.REQUIRED_AND_RELATED, treeItemOfficialPtr[0]);
                        }
                        break;
                    case IDialogConstants.YES_ID:
                        // Copy File and required
                        if (projectIsFileOrigin) {
                            refResult = ReferenceParser.checkForReferences(df, References.REQUIRED, treeItemProjectPtr[0]);
                        } else {
                            refResult = ReferenceParser.checkForReferences(df, References.REQUIRED, treeItemOfficialPtr[0]);
                        }
                        break;
                    default:
                        regainFocus();
                        return;
                    }

                    targetFile.disposeData();
                    updateTreeRemoveEntry(targetFile);
                    DatFile newDatFile = new DatFile(new File(targetPathDirL).exists() ? targetPathL : targetPathU);
                    newDatFile.setText(df.getText());
                    newDatFile.saveForced();
                    ((List<DatFile>) targetTreeItem.getData()).add(newDatFile);
                    TreeItem ti = new TreeItem(targetTreeItem);
                    ti.setText(new File(df.getNewName()).getName());
                    ti.setData(newDatFile);
                }

                if (refResult != null) {
                    // Remove old data
                    for(int i = 0; i < 5; i++) {
                        List<DatFile> toRemove = refResult.get(i);
                        for (DatFile datToRemove : toRemove) {
                            datToRemove.disposeData();
                            updateTreeRemoveEntry(datToRemove);
                        }
                    }
                    // Create new data
                    TreeItem[] targetTrees = new TreeItem[]{treeItemUnofficialPartsPtr[0], treeItemUnofficialSubpartsPtr[0], treeItemUnofficialPrimitivesPtr[0], treeItemUnofficialPrimitives48Ptr[0], treeItemUnofficialPrimitives8Ptr[0]};
                    for(int i = 5; i < 10; i++) {
                        List<DatFile> toCreate = refResult.get(i);
                        for (DatFile datToCreate : toCreate) {
                            DatFile newDatFile = new DatFile(datToCreate.getOldName());
                            String source = datToCreate.getTextDirect();
                            newDatFile.setText(source);
                            newDatFile.setOriginalText(source);
                            newDatFile.saveForced();
                            newDatFile.setType(datToCreate.getType());
                            ((List<DatFile>) targetTrees[i - 5].getData()).add(newDatFile);
                            TreeItem ti = new TreeItem(targetTrees[i - 5]);
                            ti.setText(new File(datToCreate.getOldName()).getName());
                            ti.setData(newDatFile);
                        }
                    }

                }

                updateTreeUnsavedEntries();
            }
        }
        regainFocus();
    }

    private void reloadColours() {
        for (Control ctrl : toolItemColourBar.getChildren()) {
            if (!(ctrl instanceof ToolSeparator)) ctrl.dispose();
        }

        List<GColour> colours = WorkbenchManager.getUserSettingState().getUserPalette();

        final int size = colours.size();
        for (int i = 0; i < size; i++) {
            addColorButton(toolItemColourBar, colours.get(i), i);
        }

        {
            NButton btnPalette = new NButton(toolItemColourBar, SWT.NONE);
            this.btnPalettePtr[0] = btnPalette;
            btnPalette.setToolTipText(I18n.E3D_MORE);
            btnPalette.setImage(ResourceManager.getImage("icon16_colours.png")); //$NON-NLS-1$
            initPaletteEvent();
        }

        toolItemColourBar.getParent().layout();
        toolItemColourBar.layout();
        toolItemColourBar.redraw();
    }

    public static void reloadAllColours() {
        for (EditorTextWindow w : Project.getOpenTextWindows()) {
            w.reloadColours();
        }
        Editor3DWindow.getWindow().reloadColours();
    }

    private void initPaletteEvent() {
        widgetUtil(btnPalettePtr[0]).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null) {
                final GColour[] gColour2 = new GColour[1];
                new ColourDialog(getShell(), gColour2, true).run();
                if (gColour2[0] != null) {
                    setLastUsedColour(gColour2[0]);
                    int num = gColour2[0].getColourNumber();
                    if (!LDConfig.hasColour(num)) {
                        num = -1;
                    }
                    Project.getFileToEdit().getVertexManager().addSnapshot();
                    Project.getFileToEdit().getVertexManager().colourChangeSelection(num, gColour2[0].getR(), gColour2[0].getG(), gColour2[0].getB(), gColour2[0].getA(), true);

                    btnLastUsedColourPtr[0].clearPaintListeners();
                    btnLastUsedColourPtr[0].clearSelectionListeners();
                    final Color col = SWTResourceManager.getColor((int) (gColour2[0].getR() * 255f), (int) (gColour2[0].getG() * 255f), (int) (gColour2[0].getB() * 255f));
                    final Point size = btnLastUsedColourPtr[0].computeSize(SWT.DEFAULT, SWT.DEFAULT);
                    final int x = Math.round(size.x / 5f);
                    final int y = Math.round(size.y / 5f);
                    final int w = Math.round(size.x * (3f / 5f));
                    final int h = Math.round(size.y * (3f / 5f));
                    final int imgSize = IconSize.getImageSizeFromIconSize();
                    btnLastUsedColourPtr[0].addPaintListener(e1 -> {
                        e1.gc.setBackground(col);
                        e1.gc.fillRectangle(x, y, w, h);
                        if (gColour2[0].getA() >= .99f) {
                            e1.gc.drawImage(ResourceManager.getImage("icon16_transparent.png"), 0, 0, imgSize, imgSize, x, y, w, h); //$NON-NLS-1$
                        } else if (gColour2[0].getA() == 0f) {
                            e1.gc.drawImage(ResourceManager.getImage("icon16_randomColours.png"), 0, 0, imgSize, imgSize, x, y, w, h); //$NON-NLS-1$
                        } else {
                            e1.gc.drawImage(ResourceManager.getImage("icon16_halftrans.png"), 0, 0, imgSize, imgSize, x, y, w, h); //$NON-NLS-1$
                        }
                    });
                    widgetUtil(btnLastUsedColourPtr[0]).addSelectionListener(e1 -> {
                        if (Project.getFileToEdit() != null) {
                            int num1 = gColour2[0].getColourNumber();
                            if (!LDConfig.hasColour(num1)) {
                                num1 = -1;
                            }
                            Project.getFileToEdit().getVertexManager().addSnapshot();
                            Project.getFileToEdit().getVertexManager().colourChangeSelection(num1, gColour2[0].getR(), gColour2[0].getG(), gColour2[0].getB(), gColour2[0].getA(), true);
                        }
                        regainFocus();
                    });
                    if (num != -1) {

                        Object[] messageArguments1 = {num, LDConfig.getColourName(num)};
                        MessageFormat formatter1 = new MessageFormat(""); //$NON-NLS-1$
                        formatter1.setLocale(MyLanguage.getLocale());
                        formatter1.applyPattern(I18n.EDITORTEXT_COLOUR_1);

                        btnLastUsedColourPtr[0].setToolTipText(formatter1.format(messageArguments1));
                    } else {
                        StringBuilder colourBuilder = new StringBuilder();
                        colourBuilder.append("0x2"); //$NON-NLS-1$
                        colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getR())).toUpperCase());
                        colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getG())).toUpperCase());
                        colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getB())).toUpperCase());

                        Object[] messageArguments2 = {colourBuilder.toString()};
                        MessageFormat formatter2 = new MessageFormat(""); //$NON-NLS-1$
                        formatter2.setLocale(MyLanguage.getLocale());
                        formatter2.applyPattern(I18n.EDITORTEXT_COLOUR_2);

                        btnLastUsedColourPtr[0].setToolTipText(formatter2.format(messageArguments2));
                        if (gColour2[0].getA() == 0f) btnLastUsedColourPtr[0].setToolTipText(I18n.COLOURDIALOG_RANDOM_COLOURS);
                    }
                    btnLastUsedColourPtr[0].redraw();
                }
            }
            regainFocus();
        });
    }

    public void updateInitialScale(BigDecimal initialScale, BigDecimal scaleFacor, boolean setDefaults) {
        if (BigDecimal.ZERO.compareTo(spnScaleInitialPtr[0].getValue()) != 0) {
            if (setDefaults) {
                switch (snapSize) {
                case FINE:
                    WorkbenchManager.getUserSettingState().setFineScaleSnap(new BigDecimal("1.001")); //$NON-NLS-1$
                    scaleFacor = WorkbenchManager.getUserSettingState().getFineScaleSnap();
                    break;
                case MEDIUM:
                    WorkbenchManager.getUserSettingState().setMediumScaleSnap(new BigDecimal("1.1")); //$NON-NLS-1$
                    scaleFacor = WorkbenchManager.getUserSettingState().getMediumScaleSnap();
                    break;
                case COARSE:
                    WorkbenchManager.getUserSettingState().setCoarseScaleSnap(new BigDecimal("2")); //$NON-NLS-1$
                    scaleFacor = WorkbenchManager.getUserSettingState().getCoarseScaleSnap();
                    break;
                default:
                    break;
                }
            }

            spnScaleInitialPtr[0].setValue(initialScale);
            spnScalePtr[0].setValue(scaleFacor);
        }
    }

    public List<Composite3DState> getC3DStates() {
        // Traverse the sash forms to store the 3D configuration
        final List<Composite3DState> result = new ArrayList<>();
        Control c = getSashForm().getChildren()[1];
        if (c != null) {
            if (c instanceof SashForm|| c instanceof CompositeContainer) {
                // c instanceof CompositeContainer: Simple case, since its only one 3D view open -> No recursion!
                saveComposite3DStates(c, result, "", "|"); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                // There is no 3D window open at the moment
            }
        } else {
            // There is no 3D window open at the moment
        }
        return result;
    }

    public VertexWindow getVertexWindow() {
        return vertexWindow;
    }

    public static SashForm getSashForm() {
        return sashForm;
    }

    public static Label getStatusLabel() {
        return (Label) Editor3DDesign.status.getChildren()[0];
    }

    public SashForm getEditorSashForm() {
        return editorSashForm[0];
    }
}
