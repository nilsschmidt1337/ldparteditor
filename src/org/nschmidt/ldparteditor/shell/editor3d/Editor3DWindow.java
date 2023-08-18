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
import java.util.Arrays;
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
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Point;
import org.lwjgl.opengl.swt.GLCanvas;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.lwjgl.opengl.GL;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.csg.CSG;
import org.nschmidt.ldparteditor.composite.Composite3D;
import org.nschmidt.ldparteditor.composite.CompositeContainer;
import org.nschmidt.ldparteditor.composite.CompositeScale;
import org.nschmidt.ldparteditor.composite.compositetab.CompositeTab;
import org.nschmidt.ldparteditor.composite.compositetab.CompositeTabFolder;
import org.nschmidt.ldparteditor.composite.primitive.CompositePrimitive;
import org.nschmidt.ldparteditor.data.BFC;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.DatType;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GData0;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.GData2;
import org.nschmidt.ldparteditor.data.GData5;
import org.nschmidt.ldparteditor.data.GDataBFC;
import org.nschmidt.ldparteditor.data.GDataPNG;
import org.nschmidt.ldparteditor.data.GraphicalDataTools;
import org.nschmidt.ldparteditor.data.LibraryManager;
import org.nschmidt.ldparteditor.data.Primitive;
import org.nschmidt.ldparteditor.data.ProtractorHelper;
import org.nschmidt.ldparteditor.data.ReferenceParser;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.dialog.copy.CopyDialog;
import org.nschmidt.ldparteditor.dialog.newproject.NewProjectDialog;
import org.nschmidt.ldparteditor.dnd.OpenedFilesTabDragAndDropTransfer;
import org.nschmidt.ldparteditor.dnd.OpenedFilesTabDragAndDropType;
import org.nschmidt.ldparteditor.enumtype.LDConfig;
import org.nschmidt.ldparteditor.enumtype.ManipulatorAxisMode;
import org.nschmidt.ldparteditor.enumtype.MouseButton;
import org.nschmidt.ldparteditor.enumtype.MyLanguage;
import org.nschmidt.ldparteditor.enumtype.OpenInWhat;
import org.nschmidt.ldparteditor.enumtype.Perspective;
import org.nschmidt.ldparteditor.enumtype.SnapSize;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.helper.LDPartEditorException;
import org.nschmidt.ldparteditor.helper.Manipulator;
import org.nschmidt.ldparteditor.helper.ShellHelper;
import org.nschmidt.ldparteditor.helper.Version;
import org.nschmidt.ldparteditor.helper.WidgetSelectionHelper;
import org.nschmidt.ldparteditor.helper.WidgetSelectionListener;
import org.nschmidt.ldparteditor.helper.composite3d.TreeData;
import org.nschmidt.ldparteditor.helper.composite3d.ViewIdleManager;
import org.nschmidt.ldparteditor.helper.compositetext.ProjectActions;
import org.nschmidt.ldparteditor.helper.compositetext.SubfileCompiler;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.resource.ResourceManager;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.NewOpenSaveProjectToolItem;
import org.nschmidt.ldparteditor.shell.editortext.EditorTextWindow;
import org.nschmidt.ldparteditor.shell.searchnreplace.SearchWindow;
import org.nschmidt.ldparteditor.text.LDParsingException;
import org.nschmidt.ldparteditor.text.References;
import org.nschmidt.ldparteditor.text.UTF8BufferedReader;
import org.nschmidt.ldparteditor.vertexwindow.VertexWindow;
import org.nschmidt.ldparteditor.widget.DecimalValueChangeAdapter;
import org.nschmidt.ldparteditor.widget.NButton;
import org.nschmidt.ldparteditor.widget.Tree;
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
    
    public static final int TEXT_3D_SEPARATE = 0;
    public static final int TEXT_LEFT_3D_RIGHT = 1;
    public static final int TEXT_RIGHT_3D_LEFT = 2;
    public static final int TEXT_NEW_INSTANCE = 3;

    private static final AtomicBoolean alive = new AtomicBoolean(true);
    private static final AtomicBoolean no_sync_deadlock = new AtomicBoolean(false);

    private boolean reviewingAPart = false;
    private ManipulatorAxisMode workingLayer = ManipulatorAxisMode.NONE;

    private SnapSize snapSize = SnapSize.MEDIUM;

    private boolean updatingPngPictureTab;
    private int pngPictureUpdateCounter = 0;

    private boolean updatingSelectionTab = true;

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
    @SuppressWarnings("java:S2111")
    public void run() {
        window = this;
        // Load colours
        WorkbenchManager.getUserSettingState().loadColours();
        LDConfig.overrideColour16();
        // Load recent files
        List<String> recentFiles = WorkbenchManager.getUserSettingState().getRecentItems();
        NewOpenSaveProjectToolItem.getRecentItems().addAll(recentFiles);
        // Adjust the last visited path according to what was last opened (and exists on the harddrive)
        {
            final int rc = recentFiles.size() - 1;
            boolean foundPath = false;
            for (int i = rc; i > -1; i--) {
                final String path = recentFiles.get(i);
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

            if (!WorkbenchManager.getUserSettingState().hasSeparateTextWindow()) {
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
                leftSash[0].setWeights(33, 33, 33);
            } else {
                leftSash[0].setWeights(80, 10, 10);
            }
        };

        widgetUtil(btnShowUpper1Ptr[0]).addSelectionListener(sa);
        widgetUtil(btnShowUpper2Ptr[0]).addSelectionListener(sa);
        widgetUtil(btnShowUpper3Ptr[0]).addSelectionListener(sa);

        widgetUtil(btnShowMiddlePtr[0]).addSelectionListener(e -> {
            int[] w = leftSash[0].getWeights();
            if (w[0] == 100 && w[1] == 800 && w[2] == 100) {
                leftSash[0].setWeights(33, 33, 33);
            } else {
                leftSash[0].setWeights(10, 80, 10);
            }
        });

        widgetUtil(btnSameHeightPtr[0]).addSelectionListener(e ->
            leftSash[0].setWeights(33, 33, 33)
        );

        widgetUtil(btnShowLowerPtr[0]).addSelectionListener(e -> {
            int[] w = leftSash[0].getWeights();
            if (w[0] == 100 && w[1] == 100 && w[2] == 800) {
                leftSash[0].setWeights(33, 33, 33);
            } else {
                leftSash[0].setWeights(10, 10, 80);
            }
        });

        if (btnShowLeftPtr[0] != null) widgetUtil(btnShowLeftPtr[0]).addSelectionListener(e -> {
            final SashForm sf = splitSash[0];
            int[] w = sf.getWeights();
            if (w[1] * 9 > w[0]) {
                sf.setWeights(95, 5);
            } else {
                sf.setWeights(Editor3DWindow.sashWeight1, Editor3DWindow.sashWeight2);
            }
        });

        if (btnShowRightPtr[0] != null) widgetUtil(btnShowRightPtr[0]).addSelectionListener(e -> {
            final SashForm sf = splitSash[0];
            int[] w = sf.getWeights();
            if (w[0] * 9 > w[1]) {
                sf.setWeights(5, 95);
            } else {
                sf.setWeights(Editor3DWindow.sashWeight1, Editor3DWindow.sashWeight2);
            }
        });

        if (btnSameWidthPtr[0] != null) widgetUtil(btnSameWidthPtr[0]).addSelectionListener(e -> splitSash[0].setWeights(50, 50));

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
                } catch (Exception ex) {
                    NLogger.debug(Editor3DWindow.class, ex);
                }

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
                } catch (Exception ex) {
                    NLogger.debug(Editor3DWindow.class, ex);
                }

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
                } catch (Exception ex) {
                    NLogger.debug(Editor3DWindow.class, ex);
                }

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
                            case 1, 5, 4:
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
                                spnSelectionLengthMMPtr[0].setEnabled(spnSelectionLengthPtr[0].getEnabled());
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
                                    spnSelectionLengthMMPtr[0].setValue(spnSelectionLengthPtr[0].getValue().multiply(GData2.MILLIMETRE_PER_LDU));
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
                                    spnSelectionLengthMMPtr[0].setValue(spnSelectionLengthPtr[0].getValue().multiply(GData2.MILLIMETRE_PER_LDU));
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
                                lblSelectionLengthMMPtr[0].setText((gdata.type() != 1 ? I18n.E3D_PROTRACTOR_LENGTH_MM : "") + " {" + spnSelectionLengthMMPtr[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                
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
                            case 1, 4, 5:
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
                                spnSelectionLengthMMPtr[0].setEnabled(spnSelectionLengthPtr[0].getEnabled());
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
                                    spnSelectionLengthMMPtr[0].setValue(spnSelectionLengthPtr[0].getValue().multiply(GData2.MILLIMETRE_PER_LDU));
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
                                    spnSelectionLengthMMPtr[0].setValue(spnSelectionLengthPtr[0].getValue().multiply(GData2.MILLIMETRE_PER_LDU));
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
                                lblSelectionLengthMMPtr[0].setText((gdata.type() != 1 ? I18n.E3D_PROTRACTOR_LENGTH_MM : "") + " {" + spnSelectionLengthMMPtr[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                
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
                    spnSelectionLengthMMPtr[0].setValue(spnSelectionLengthPtr[0].getValue().multiply(GData2.MILLIMETRE_PER_LDU));
                    updatingSelectionTab = false;
                }
                if (newLine.type() == 3 && !((org.nschmidt.ldparteditor.data.GData3) newLine).isTriangle) {
                    updatingSelectionTab = true;
                    spnSelectionAnglePtr[0].setValue(new BigDecimal(((org.nschmidt.ldparteditor.data.GData3) newLine).getProtractorAngle()));
                    spnSelectionLengthPtr[0].setValue(((org.nschmidt.ldparteditor.data.GData3) newLine).getProtractorLength());
                    spnSelectionLengthMMPtr[0].setValue(spnSelectionLengthPtr[0].getValue().multiply(GData2.MILLIMETRE_PER_LDU));
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
        
        spnSelectionLengthMMPtr[0].addValueChangeListener(spn -> 
            spnSelectionLengthPtr[0].setValue(spnSelectionLengthMMPtr[0].getValue().divide(GData2.MILLIMETRE_PER_LDU))
        );

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
                spnSelectionLengthMMPtr[0].setValue(spn.getValue().multiply(GData2.MILLIMETRE_PER_LDU));
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
                spnSelectionLengthMMPtr[0].setValue(spn.getValue().multiply(GData2.MILLIMETRE_PER_LDU));
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
                } catch (Exception ex) {
                    NLogger.debug(Editor3DWindow.class, ex);
                }

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

                    FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
                    fd.setText(I18n.E3D_OPEN_PNG_IMAGE);
                    try {
                        File f = new File(png.texturePath);
                        fd.setFilterPath(f.getParent());
                        fd.setFileName(f.getName());
                    } catch (Exception ex) {
                        NLogger.debug(Editor3DWindow.class, ex);
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
        } catch (InvocationTargetException ite) {
            NLogger.error(Editor3DWindow.class, ite);
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
        
        int operations = DND.DROP_MOVE;
        final int[] sourceIndex = new int[] {0};
        final DragSource source = new DragSource(tabFolderOpenDatFilesPtr[0], operations);
        widgetUtil(source).setTransfer(OpenedFilesTabDragAndDropTransfer.getInstance());
        source.addDragListener(new DragSourceListener() {
            @Override
            public void dragStart(DragSourceEvent event) {
                final int selectionIndex = tabFolderOpenDatFilesPtr[0].getSelectionIndex();
                event.doit = selectionIndex > 0 && !View.DUMMY_DATFILE.equals(tabFolderOpenDatFilesPtr[0].getSelection().getData());
                if (event.doit) {
                    sourceIndex[0] = selectionIndex;
                }
            }

            @Override
            public void dragSetData(DragSourceEvent event) {
                event.data = new OpenedFilesTabDragAndDropType();
            }

            @Override
            public void dragFinished(DragSourceEvent event) {
                // Implementation is not required.
            }
        });

        DropTarget target = new DropTarget(tabFolderOpenDatFilesPtr[0], operations);
        widgetUtil(target).setTransfer(OpenedFilesTabDragAndDropTransfer.getInstance(), FileTransfer.getInstance());
        target.addDropListener(new DropTargetAdapter() {
            @Override
            public void dragOver(DropTargetEvent event) {
                Point dpos = tabFolderOpenDatFilesPtr[0].toDisplay(1, 1);
                Point pos = new Point(event.x - dpos.x, event.y - dpos.y);
                CTabItem item = tabFolderOpenDatFilesPtr[0].getItem(pos);
                if (item != null && !View.DUMMY_DATFILE.equals(item.getData())) {
                    tabFolderOpenDatFilesPtr[0].setSelection(item);
                }
            }

            @Override
            public void drop(DropTargetEvent event) {
                if (OpenedFilesTabDragAndDropTransfer.getInstance().isSupportedType(event.currentDataType)) {
                    final int startIndex = sourceIndex[0];
                    final int selectionIndex = tabFolderOpenDatFilesPtr[0].getSelectionIndex();
                    if (startIndex == selectionIndex) return;
                    final CTabItem[] items = tabFolderOpenDatFilesPtr[0].getItems();
                    final List<CTabItem> itemList = new ArrayList<>(Arrays.asList(items));

                    CTabItem temp = items[startIndex];
                    itemList.remove(startIndex);
                    itemList.add(selectionIndex, temp);

                    Editor3DWindow.getWindow().updateTabs(itemList.toArray(new CTabItem[0]));
                    NLogger.debug(getClass(), "Re-arranged 3D editor tab."); //$NON-NLS-1$
                }
            }
        });

        txtSearchPtr[0].setText(" "); //$NON-NLS-1$
        txtSearchPtr[0].setText(""); //$NON-NLS-1$

        Project.getFileToEdit().setLastSelectedComposite(Editor3DWindow.renders.get(0).getC3D());
        EditorTextWindow.createTemporaryWindow(Project.getFileToEdit());

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
        CSG.EXECUTOR_SERVICE.shutdown();
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
                        NewOpenSaveProjectToolItem.addRecentFile(df);
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

        WorkbenchManager.getUserSettingState().setRecentItems(NewOpenSaveProjectToolItem.getRecentItems());
        // Save the workbench
        WorkbenchManager.saveWorkbench(WorkbenchManager.SETTINGS_GZ);
        setReturnCode(CANCEL);
        close();
    }

    private void saveComposite3DStates(Control c, List<Composite3DState> c3dStates, String parentPath, String path) {
        Composite3DState st = new Composite3DState();
        st.setParentPath(parentPath);
        st.setPath(path);
        if (c instanceof CompositeContainer compositecontainer) {
            NLogger.debug(getClass(), "{0}C", path); //$NON-NLS-1$
            final Composite3D c3d = compositecontainer.getComposite3D();
            st.setSash(false);
            st.setScales(c3d.getParent() instanceof CompositeScale);
            st.setVertical(false);
            st.setWeights(null);
            fillC3DState(st, c3d);
        } else if (c instanceof SashForm sashform) {
            NLogger.debug(getClass(), path);
            st.setSash(true);
            st.setVertical((sashform.getStyle() & SWT.VERTICAL) != 0);
            st.setWeights(sashform.getWeights());
            Control c1 = sashform.getChildren()[0];
            Control c2 = sashform.getChildren()[1];
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
    
    public synchronized void updateTabs() {
        updateTabs(tabFolderOpenDatFilesPtr[0].getItems());
    }

    public synchronized void updateTabs(CTabItem[] items) {
        List<DatFile> oldOpenFiles = new ArrayList<>();
        for (CTabItem cti : items) {
            oldOpenFiles.add((DatFile) cti.getData());
        }
        
        
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

        // Copy list of opened files
        List<DatFile> openFiles = new ArrayList<>();
        openFiles.addAll(Project.getOpenedFiles());
        
        // Iterate in the old order, just to keep it
        for (DatFile df2 : oldOpenFiles) {
            if (!openFiles.contains(df2)) continue;
            openFiles.remove(df2);
            CTabItem tItem = new CTabItem(tabFolderOpenDatFilesPtr[0], SWT.NONE);
            tItem.setText(df2.getShortName() + (Project.getUnsavedFiles().contains(df2) ? "*" : "")); //$NON-NLS-1$ //$NON-NLS-2$
            tItem.setData(df2);
            if (df2.equals(Project.getFileToEdit())) {
                tabFolderOpenDatFilesPtr[0].setSelection(tItem);
                isSelected = true;
            }
        }
        
        for (DatFile df2 : openFiles) {
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

    public Tree getPartsTree() {
        return treeParts[0];
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

    public TreeItem getUnofficial() {
        return treeItemUnofficialPtr[0];
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

    public Text getSearchText() {
        return txtSearchPtr[0];
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
            NLogger.debug(Editor3DWindow.class, selected);

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
                        df.addToTail(new GData0("0 !LICENSE Licensed under CC BY 4.0 : see CAreadme.txt", View.DUMMY_REFERENCE)); //$NON-NLS-1$
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
            final DatType type;
            final DatFile df;
            final DatFile original = isFileNameAllocated2(filePath);

            if (original == null) {

                // Title Parsing and Type Check!!
                String title = parseTitle(filePath);
                type = parseFileType(filePath);

                df = new DatFile(filePath, title, false, type);
                df.setProjectFile(df.getNewName().startsWith(Project.getProjectPath()));

            } else {

                df = original;

                if (checkForRevert(canRevert, df)) {
                    return null;
                }

                df.setProjectFile(df.getNewName().startsWith(Project.getProjectPath()));
                if (original.isProjectFile()) {
                    openDatFile(df, where, null);
                    return df;
                }
                if (openCachedReference(this.treeItemProjectPartsPtr, df, where)) {
                    return df;
                }
                if (openCachedReference(this.treeItemProjectSubpartsPtr, df, where)) {
                    return df;
                }
                if (openCachedReference(this.treeItemProjectPrimitivesPtr, df, where)) {
                    return df;
                }
                if (openCachedReference(this.treeItemProjectPrimitives48Ptr, df, where)) {
                    return df;
                }
                if (openCachedReference(this.treeItemProjectPrimitives8Ptr, df, where)) {
                    return df;
                }
                type = original.getType();
            }

            TreeItem ti;
            switch (type) {
            case SUBPART:
                ti = rebuildTreeItem(df, this.treeItemProjectSubpartsPtr);
                break;
            case PRIMITIVE:
                ti = rebuildTreeItem(df, this.treeItemProjectPrimitivesPtr);
                break;
            case PRIMITIVE48:
                ti = rebuildTreeItem(df, this.treeItemProjectPrimitives48Ptr);
                break;
            case PRIMITIVE8:
                ti = rebuildTreeItem(df, this.treeItemProjectPrimitives8Ptr);
                break;
            case PART:
            default:
                ti = rebuildTreeItem(df, this.treeItemProjectPartsPtr);
                break;
            }

            StringBuilder nameSb = new StringBuilder(new File(df.getNewName()).getName());

            nameSb.append(I18n.E3D_NEW_FILE);

            ti.setText(nameSb.toString());
            ti.setData(df);

            if (checkForRevert(canRevert, df)) {
                return null;
            }

            updateTreeUnsavedEntries();

            openDatFile(df, where, null);
            return df;
        } else {
            NLogger.error(Editor3DWindow.class, new IllegalArgumentException("No filename to open was specified.")); //$NON-NLS-1$
        }

        return null;
    }

    private boolean checkForRevert(boolean canRevert, final DatFile df) {
        if (canRevert && Project.getUnsavedFiles().contains(df)) {
            revert(df);
            return true;
        }
        
        return false;
    }

    private boolean openCachedReference(TreeItem[] treeItemPtr, DatFile df, OpenInWhat where) {
        @SuppressWarnings("unchecked")
        List<DatFile> cachedReferences = (List<DatFile>) treeItemPtr[0].getData();
        if (cachedReferences.contains(df)) {
            openDatFile(df, where, null);
            return true;
        }
        
        return false;
    }

    private TreeItem rebuildTreeItem(final DatFile df, final TreeItem[] treeItemPtr) {
        TreeItem ti;
        {
            @SuppressWarnings("unchecked")
            List<DatFile> cachedReferences = (List<DatFile>) treeItemPtr[0].getData();
            if (cachedReferences != null) cachedReferences.add(df);
        }
        ti = new TreeItem(treeItemPtr[0]);
        return ti;
    }

    private DatType parseFileType(String filePath) {
        DatType type = DatType.PART;
        try (UTF8BufferedReader reader = new UTF8BufferedReader(new File(filePath).getAbsolutePath())) {
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
        return type;
    }

    private String parseTitle(String filePath) {
        final StringBuilder titleSb = new StringBuilder();
        try (UTF8BufferedReader reader = new UTF8BufferedReader(new File(filePath).getAbsolutePath())) {
            String title = reader.readLine();
            if (title != null) {
                title = title.trim();
                if (title.length() > 0) {
                    titleSb.append(" -"); //$NON-NLS-1$
                    titleSb.append(title.substring(1));
                }
            }
        } catch (LDParsingException | FileNotFoundException ex) {
            NLogger.error(Editor3DWindow.class, ex);
        }
        return titleSb.toString();
    }

    public void openDatFile(DatFile df, OpenInWhat where, ApplicationWindow tWin) {
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
                        }
                        if (w.isSeperateWindow()) {
                            w.open();
                        }
                        
                        // File is already open in a text editor!
                        return;
                    }
                }
            }

            EditorTextWindow w = null;
            // Project.getParsedFiles().add(df); IS NECESSARY HERE
            Project.getParsedFiles().add(df);
            Project.addOpenedFile(df);
            
            if (tWin == null) {
                if (!Project.getOpenTextWindows().isEmpty() && !(w = Project.getOpenTextWindows().iterator().next()).isSeperateWindow() || (w != null && WorkbenchManager.getUserSettingState().hasSingleTextWindow())) {
                    w.openNewDatFileTab(df, true);
                } else {
                    EditorTextWindow.createNewWindowIfRequired(df);
                }
            } else {
                if (WorkbenchManager.getUserSettingState().hasSingleTextWindow()) {
                    if (tWin instanceof Editor3DWindow && !Project.getOpenTextWindows().isEmpty() && !(w = Project.getOpenTextWindows().iterator().next()).isSeperateWindow() || w != null) {
                        w.openNewDatFileTab(df, true);
                    } else if (tWin instanceof EditorTextWindow wtxt) {
                        wtxt.openNewDatFileTab(df, true);
                    }
                } else {
                    EditorTextWindow.createNewWindowIfRequired(df);
                }
            }
        }
    }

    public void disableSelectionTab() {
        if (Thread.currentThread() == Display.getDefault().getThread()) {
            updatingSelectionTab = true;
            txtLinePtr[0].setText(""); //$NON-NLS-1$
            btnMoveAdjacentData2Ptr[0].setSelection(false);
            spnSelectionAnglePtr[0].setEnabled(false);
            spnSelectionLengthPtr[0].setEnabled(false);
            spnSelectionLengthMMPtr[0].setEnabled(false);
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
            spnSelectionLengthMMPtr[0].setValue(BigDecimal.ONE.multiply(GData2.MILLIMETRE_PER_LDU));
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
            lblSelectionLengthMMPtr[0].setText(I18n.E3D_PROTRACTOR_LENGTH_MM);
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
                    spnSelectionLengthMMPtr[0].setEnabled(false);
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
                    lblSelectionLengthMMPtr[0].setText(I18n.E3D_PROTRACTOR_LENGTH_MM);
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
    
    public void updateLineThickness() {
        Set<DatFile> dfs = new HashSet<>();
        for (OpenGLRenderer renderer : renders) {
            dfs.add(renderer.getC3D().getLockableDatFileReference());
        }
        
        for (DatFile df : dfs) {
            df.getVertexManager().addSnapshot();
            Set<GData2> lines = df.getVertexManager().getLines().keySet();
            for (GData2 line : lines) {
                line.updateLine();
            }
            
            Set<GData5> condlines = df.getVertexManager().getCondlines().keySet();
            for (GData5 condline : condlines) {
                condline.updateLine();
            }
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
                    NewOpenSaveProjectToolItem.addRecentFile(df);
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
                NewOpenSaveProjectToolItem.addRecentFile(df);
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
            NewOpenSaveProjectToolItem.addRecentFile(df);
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
    
    public void closeAllTabs() {
        for (CTabItem ti : tabFolderOpenDatFilesPtr[0].getItems()) {
            ti.dispose();
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
            
            Project.setFileToEdit(df);

            if (tmpW != null) {
                tmpW.getTabFolder().setSelection(tmpT);
                ((CompositeTab) tmpT).getControl().getShell().forceActive();
                if (tmpW.isSeperateWindow()) {
                    tmpW.open();
                }
                ((CompositeTab) tmpT).getTextComposite().forceFocus();
            }
            
            for (OpenGLRenderer renderer : renders) {
                Composite3D c3d = renderer.getC3D();
                if (!c3d.isDatFileLockedOnDisplay() || df.equals(c3d.getLockableDatFileReference())) {
                    c3d.setLockableDatFileReference(df);
                }
            }
            
            updateTabs();
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
        } else {
            // Don't lock the view when there is no state saved.
            c3d.getPerspectiveCalculator().setRotationLock(false);
        }
    }

    public boolean hasState(final DatFile df, final Composite3D c3d) {
        return c3dStates.containsKey(df) && c3dStates.get(df).containsKey(c3d);
    }

    private void actionOpenInTextEditor() {
        if (treeParts[0].getSelectionCount() == 1 && treeParts[0].getSelection()[0] != null && treeParts[0].getSelection()[0].getData() instanceof DatFile df) {
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
            if (!Project.getOpenTextWindows().isEmpty() && (w != null || !(w = Project.getOpenTextWindows().iterator().next()).isSeperateWindow()) || (w != null && WorkbenchManager.getUserSettingState().hasSingleTextWindow())) {
                w.openNewDatFileTab(df, true);
            } else {
                EditorTextWindow.createNewWindowIfRequired(df);
            }
            df.getVertexManager().addSnapshot();
        }
        cleanupClosedData();
        updateTabs();
    }

    private void actionOpenIn3DEditor() {
        if (treeParts[0].getSelectionCount() == 1 && treeParts[0].getSelection()[0] != null && treeParts[0].getSelection()[0].getData() instanceof DatFile df) {
            openFileIn3DEditor(df);
            updateTreeUnsavedEntries();
            cleanupClosedData();
            regainFocus();
        }
    }

    private void actionRevert() {
        if (treeParts[0].getSelectionCount() == 1 && treeParts[0].getSelection()[0] != null && treeParts[0].getSelection()[0].getData() instanceof DatFile df) {
            revert(df);
        }
        regainFocus();
    }

    private void actionClose() {
        if (treeParts[0].getSelectionCount() == 1 && treeParts[0].getSelection()[0] != null && treeParts[0].getSelection()[0].getData() instanceof DatFile df) {
            Project.removeOpenedFile(df);
            if (!closeDatfile(df)) {
                Project.addOpenedFile(df);
                updateTabs();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void actionRename() {
        if (treeParts[0].getSelectionCount() == 1 && treeParts[0].getSelection()[0] != null && treeParts[0].getSelection()[0].getData() instanceof DatFile df) {
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
        if (treeParts[0].getSelectionCount() == 1 && treeParts[0].getSelection()[0] != null && treeParts[0].getSelection()[0].getData() instanceof DatFile df) {
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
        if (c instanceof SashForm || c instanceof CompositeContainer) {
            // c instanceof CompositeContainer: Simple case, since its only one 3D view open -> No recursion!
            saveComposite3DStates(c, result, "", "|"); //$NON-NLS-1$ //$NON-NLS-2$
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
