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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.composites.CompositeContainer;
import org.nschmidt.ldparteditor.composites.CompositeScale;
import org.nschmidt.ldparteditor.composites.ToolItem;
import org.nschmidt.ldparteditor.composites.compositetab.CompositeTab;
import org.nschmidt.ldparteditor.composites.primitive.CompositePrimitive;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.DatType;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.GDataPNG;
import org.nschmidt.ldparteditor.data.LibraryManager;
import org.nschmidt.ldparteditor.data.Matrix;
import org.nschmidt.ldparteditor.data.Primitive;
import org.nschmidt.ldparteditor.data.ReferenceParser;
import org.nschmidt.ldparteditor.data.RingsAndCones;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.dialogs.colour.ColourDialog;
import org.nschmidt.ldparteditor.dialogs.copy.CopyDialog;
import org.nschmidt.ldparteditor.dialogs.edger2.EdgerDialog;
import org.nschmidt.ldparteditor.dialogs.intersector.IntersectorDialog;
import org.nschmidt.ldparteditor.dialogs.isecalc.IsecalcDialog;
import org.nschmidt.ldparteditor.dialogs.lines2pattern.Lines2PatternDialog;
import org.nschmidt.ldparteditor.dialogs.newproject.NewProjectDialog;
import org.nschmidt.ldparteditor.dialogs.pathtruder.PathTruderDialog;
import org.nschmidt.ldparteditor.dialogs.rectifier.RectifierDialog;
import org.nschmidt.ldparteditor.dialogs.ringsandcones.RingsAndConesDialog;
import org.nschmidt.ldparteditor.dialogs.rotate.RotateDialog;
import org.nschmidt.ldparteditor.dialogs.round.RoundDialog;
import org.nschmidt.ldparteditor.dialogs.scale.ScaleDialog;
import org.nschmidt.ldparteditor.dialogs.setcoordinates.CoordinatesDialog;
import org.nschmidt.ldparteditor.dialogs.slicerpro.SlicerProDialog;
import org.nschmidt.ldparteditor.dialogs.symsplitter.SymSplitterDialog;
import org.nschmidt.ldparteditor.dialogs.translate.TranslateDialog;
import org.nschmidt.ldparteditor.dialogs.txt2dat.Txt2DatDialog;
import org.nschmidt.ldparteditor.dialogs.unificator.UnificatorDialog;
import org.nschmidt.ldparteditor.dialogs.value.ValueDialog;
import org.nschmidt.ldparteditor.dialogs.value.ValueDialogInt;
import org.nschmidt.ldparteditor.enums.GLPrimitives;
import org.nschmidt.ldparteditor.enums.ManipulatorScope;
import org.nschmidt.ldparteditor.enums.MergeTo;
import org.nschmidt.ldparteditor.enums.MouseButton;
import org.nschmidt.ldparteditor.enums.ObjectMode;
import org.nschmidt.ldparteditor.enums.OpenInWhat;
import org.nschmidt.ldparteditor.enums.Perspective;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.enums.TransformationMode;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.enums.WorkingMode;
import org.nschmidt.ldparteditor.helpers.Manipulator;
import org.nschmidt.ldparteditor.helpers.ShellHelper;
import org.nschmidt.ldparteditor.helpers.Version;
import org.nschmidt.ldparteditor.helpers.WidgetSelectionHelper;
import org.nschmidt.ldparteditor.helpers.composite3d.Edger2Settings;
import org.nschmidt.ldparteditor.helpers.composite3d.IntersectorSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.IsecalcSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.PathTruderSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.RectifierSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.RingsAndConesSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.SelectorSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.SlicerProSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.SymSplitterSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.TreeData;
import org.nschmidt.ldparteditor.helpers.composite3d.Txt2DatSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.UnificatorSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.ViewIdleManager;
import org.nschmidt.ldparteditor.helpers.compositetext.ProjectActions;
import org.nschmidt.ldparteditor.helpers.compositetext.SubfileCompiler;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.main.LDPartEditor;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.resources.ResourceManager;
import org.nschmidt.ldparteditor.shells.editormeta.EditorMetaWindow;
import org.nschmidt.ldparteditor.shells.editortext.EditorTextWindow;
import org.nschmidt.ldparteditor.shells.searchnreplace.SearchWindow;
import org.nschmidt.ldparteditor.text.LDParsingException;
import org.nschmidt.ldparteditor.text.References;
import org.nschmidt.ldparteditor.text.TextTriangulator;
import org.nschmidt.ldparteditor.text.UTF8BufferedReader;
import org.nschmidt.ldparteditor.widgets.BigDecimalSpinner;
import org.nschmidt.ldparteditor.widgets.TreeItem;
import org.nschmidt.ldparteditor.widgets.ValueChangeAdapter;
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

    /** The window state of this window */
    private Editor3DWindowState editor3DWindowState;
    /** The reference to this window */
    private static Editor3DWindow window;

    /** The window state of this window */
    private SearchWindow searchWindow;

    public static final ArrayList<GLCanvas> canvasList = new ArrayList<GLCanvas>();
    public static final ArrayList<OpenGLRenderer> renders = new ArrayList<OpenGLRenderer>();

    final private static AtomicBoolean alive = new AtomicBoolean(true);

    private boolean addingSomething = false;
    private boolean addingVertices = false;
    private boolean addingLines = false;
    private boolean addingTriangles = false;
    private boolean addingQuads = false;
    private boolean addingCondlines = false;
    private boolean addingSubfiles = false;
    private boolean movingAdjacentData = false;
    private boolean noTransparentSelection = false;
    private boolean bfcToggle = false;
    private ObjectMode workingType = ObjectMode.VERTICES;
    private WorkingMode workingAction = WorkingMode.SELECT;

    private GColour lastUsedColour = new GColour(16, .5f, .5f, .5f, 1f);

    private ManipulatorScope transformationMode = ManipulatorScope.LOCAL;

    private int snapSize = 1;

    private Txt2DatSettings ts = new Txt2DatSettings();
    private Edger2Settings es = new Edger2Settings();
    private RectifierSettings rs = new RectifierSettings();
    private IsecalcSettings is = new IsecalcSettings();
    private SlicerProSettings ss = new SlicerProSettings();
    private IntersectorSettings ins = new IntersectorSettings();
    private PathTruderSettings ps = new PathTruderSettings();
    private SymSplitterSettings sims = new SymSplitterSettings();
    private UnificatorSettings us = new UnificatorSettings();
    private RingsAndConesSettings ris = new RingsAndConesSettings();
    private SelectorSettings sels = new SelectorSettings();

    private boolean updatingPngPictureTab;
    private int pngPictureUpdateCounter = 0;

    private final EditorMetaWindow metaWindow = new EditorMetaWindow();
    private boolean updatingSelectionTab = true;

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
        // Load the window state data
        editor3DWindowState = WorkbenchManager.getEditor3DWindowState();
        WorkbenchManager.setEditor3DWindow(this);
        // Closing this window causes the whole application to quit
        this.setBlockOnOpen(true);
        // Creating the window to get the shell
        this.create();
        final Shell sh = this.getShell();
        sh.setText(Version.getApplicationName());
        sh.setImage(ResourceManager.getImage("imgDuke2.png")); //$NON-NLS-1$
        sh.setMinimumSize(640, 480);
        sh.setBounds(this.editor3DWindowState.getWindowState().getSizeAndPosition());
        if (this.editor3DWindowState.getWindowState().isCentered()) {
            ShellHelper.centerShellOnPrimaryScreen(sh);
        }
        // Maximize has to be called asynchronously
        sh.getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                sh.setMaximized(editor3DWindowState.getWindowState().isMaximized());
            }
        });
        // MARK All final listeners will be configured here..
        // First, create all menu actions.
        createActions();
        btn_Sync[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                resetSearch();
                int[][] stats = new int[15][3];
                stats[0] = LibraryManager.syncProjectElements(treeItem_Project[0]);
                stats[5] = LibraryManager.syncUnofficialParts(treeItem_UnofficialParts[0]);
                stats[6] = LibraryManager.syncUnofficialSubparts(treeItem_UnofficialSubparts[0]);
                stats[7] = LibraryManager.syncUnofficialPrimitives(treeItem_UnofficialPrimitives[0]);
                stats[8] = LibraryManager.syncUnofficialHiResPrimitives(treeItem_UnofficialPrimitives48[0]);
                stats[9] = LibraryManager.syncUnofficialLowResPrimitives(treeItem_UnofficialPrimitives8[0]);
                stats[10] = LibraryManager.syncOfficialParts(treeItem_OfficialParts[0]);
                stats[11] = LibraryManager.syncOfficialSubparts(treeItem_OfficialSubparts[0]);
                stats[12] = LibraryManager.syncOfficialPrimitives(treeItem_OfficialPrimitives[0]);
                stats[13] = LibraryManager.syncOfficialHiResPrimitives(treeItem_OfficialPrimitives48[0]);
                stats[14] = LibraryManager.syncOfficialLowResPrimitives(treeItem_OfficialPrimitives8[0]);

                int additions = 0;
                int deletions = 0;
                int conflicts = 0;
                for (int[] is : stats) {
                    additions += is[0];
                    deletions += is[1];
                    conflicts += is[2];
                }

                txt_Search[0].setText(" "); //$NON-NLS-1$
                txt_Search[0].setText(""); //$NON-NLS-1$

                Set<DatFile> dfs = new HashSet<DatFile>();
                for (OpenGLRenderer renderer : renders) {
                    dfs.add(renderer.getC3D().getLockableDatFileReference());
                }
                for (EditorTextWindow w : Project.getOpenTextWindows()) {
                    for (CTabItem t : w.getTabFolder().getItems()) {
                        DatFile txtDat = ((CompositeTab) t).getState().getFileNameObj();
                        if (txtDat != null) {
                            dfs.add(txtDat);
                        }
                    }
                }
                for (DatFile df : dfs) {
                    SubfileCompiler.compile(df, false, false);
                }
                for (EditorTextWindow w : Project.getOpenTextWindows()) {
                    for (CTabItem t : w.getTabFolder().getItems()) {
                        DatFile txtDat = ((CompositeTab) t).getState().getFileNameObj();
                        if (txtDat != null) {
                            ((CompositeTab) t).parseForErrorAndHints();
                            ((CompositeTab) t).getTextComposite().redraw();

                            ((CompositeTab) t).getState().getTab().setText(((CompositeTab) t).getState().getFilenameWithStar());
                        }
                    }
                }

                updateTree_unsavedEntries();

                try {
                    new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, false, new IRunnableWithProgress() {
                        @Override
                        public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                            monitor.beginTask("Loading Primitives...", IProgressMonitor.UNKNOWN); //$NON-NLS-1$ I18N
                            Thread.sleep(1500);
                        }
                    });
                } catch (InvocationTargetException consumed) {
                } catch (InterruptedException consumed) {
                }

                cmp_Primitives[0].load(false);

                MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
                messageBox.setText(I18n.DIALOG_SyncTitle);

                Object[] messageArguments = {additions, deletions, conflicts};
                MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                formatter.setLocale(View.LOCALE);
                formatter.applyPattern(I18n.DIALOG_Sync);
                messageBox.setMessage(formatter.format(messageArguments));

                messageBox.open();
            }
        });
        btn_New[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ProjectActions.createNewProject(Editor3DWindow.getWindow(), false);
            }
        });
        btn_Open[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (ProjectActions.openProject()) {
                    Project.create(false);
                    treeItem_Project[0].setData(Project.getProjectPath());
                    resetSearch();
                    LibraryManager.readProjectPartsParent(treeItem_ProjectParts[0]);
                    LibraryManager.readProjectParts(treeItem_ProjectParts[0]);
                    LibraryManager.readProjectSubparts(treeItem_ProjectSubparts[0]);
                    LibraryManager.readProjectPrimitives(treeItem_ProjectPrimitives[0]);
                    LibraryManager.readProjectHiResPrimitives(treeItem_ProjectPrimitives48[0]);
                    LibraryManager.readProjectLowResPrimitives(treeItem_ProjectPrimitives8[0]);
                    treeItem_OfficialParts[0].setData(null);
                    txt_Search[0].setText(" "); //$NON-NLS-1$
                    txt_Search[0].setText(""); //$NON-NLS-1$
                    updateTree_unsavedEntries();
                }
            }
        });
        btn_Save[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (treeParts[0].getSelectionCount() == 1) {
                    if (treeParts[0].getSelection()[0].getData() instanceof DatFile) {
                        DatFile df = (DatFile) treeParts[0].getSelection()[0].getData();
                        if (!df.isReadOnly() && Project.getUnsavedFiles().contains(df)) {
                            if (df.save()) {
                                Editor3DWindow.getWindow().updateTree_unsavedEntries();
                            } else {
                                MessageBox messageBoxError = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                                messageBoxError.setText(I18n.DIALOG_Error);
                                messageBoxError.setMessage(I18n.DIALOG_CantSaveFile);
                                messageBoxError.open();
                                Editor3DWindow.getWindow().updateTree_unsavedEntries();
                            }
                        }
                    } else if (treeParts[0].getSelection()[0].getData() instanceof ArrayList<?>) {
                        NLogger.debug(getClass(), "Saving all files from this group"); //$NON-NLS-1$
                        {
                            @SuppressWarnings("unchecked")
                            ArrayList<DatFile> dfs = (ArrayList<DatFile>) treeParts[0].getSelection()[0].getData();
                            for (DatFile df : dfs) {
                                if (!df.isReadOnly() && Project.getUnsavedFiles().contains(df)) {
                                    if (df.save()) {
                                        Project.removeUnsavedFile(df);
                                        Editor3DWindow.getWindow().updateTree_unsavedEntries();
                                    } else {
                                        MessageBox messageBoxError = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                                        messageBoxError.setText(I18n.DIALOG_Error);
                                        messageBoxError.setMessage(I18n.DIALOG_CantSaveFile);
                                        messageBoxError.open();
                                        Editor3DWindow.getWindow().updateTree_unsavedEntries();
                                    }
                                }
                            }
                        }
                    } else if (treeParts[0].getSelection()[0].getData() instanceof String) {
                        if (treeParts[0].getSelection()[0].equals(treeItem_Project[0])) {
                            NLogger.debug(getClass(), "Save the project..."); //$NON-NLS-1$
                            if (Project.isDefaultProject()) {
                                ProjectActions.createNewProject(Editor3DWindow.getWindow(), true);
                            }
                            iterateOverItems(treeItem_ProjectParts[0]);
                            iterateOverItems(treeItem_ProjectSubparts[0]);
                            iterateOverItems(treeItem_ProjectPrimitives[0]);
                            iterateOverItems(treeItem_ProjectPrimitives48[0]);
                            iterateOverItems(treeItem_ProjectPrimitives8[0]);
                        } else if (treeParts[0].getSelection()[0].equals(treeItem_Unofficial[0])) {
                            iterateOverItems(treeItem_UnofficialParts[0]);
                            iterateOverItems(treeItem_UnofficialSubparts[0]);
                            iterateOverItems(treeItem_UnofficialPrimitives[0]);
                            iterateOverItems(treeItem_UnofficialPrimitives48[0]);
                            iterateOverItems(treeItem_UnofficialPrimitives8[0]);
                        }
                        NLogger.debug(getClass(), "Saving all files from this group to"); //$NON-NLS-1$
                        NLogger.debug(getClass(), (String) treeParts[0].getSelection()[0].getData());
                    }
                } else {
                    NLogger.debug(getClass(), "Save the project..."); //$NON-NLS-1$
                    if (Project.isDefaultProject()) {
                        ProjectActions.createNewProject(Editor3DWindow.getWindow(), true);
                    }
                }
            }

            private void iterateOverItems(TreeItem ti) {
                {
                    @SuppressWarnings("unchecked")
                    ArrayList<DatFile> dfs = (ArrayList<DatFile>) ti.getData();
                    for (DatFile df : dfs) {
                        if (!df.isReadOnly() && Project.getUnsavedFiles().contains(df)) {
                            if (df.save()) {
                                Project.removeUnsavedFile(df);
                                Editor3DWindow.getWindow().updateTree_unsavedEntries();
                            } else {
                                MessageBox messageBoxError = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                                messageBoxError.setText(I18n.DIALOG_Error);
                                messageBoxError.setMessage(I18n.DIALOG_CantSaveFile);
                                messageBoxError.open();
                                Editor3DWindow.getWindow().updateTree_unsavedEntries();
                            }
                        }
                    }
                }
            }
        });
        btn_SaveAll[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                HashSet<DatFile> dfs = new HashSet<DatFile>(Project.getUnsavedFiles());
                for (DatFile df : dfs) {
                    if (!df.isReadOnly()) {
                        if (df.save()) {
                            Project.removeUnsavedFile(df);
                        } else {
                            MessageBox messageBoxError = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                            messageBoxError.setText(I18n.DIALOG_Error);
                            messageBoxError.setMessage(I18n.DIALOG_CantSaveFile);
                            messageBoxError.open();
                        }
                    }
                }
                if (Project.isDefaultProject()) {
                    ProjectActions.createNewProject(getWindow(), true);
                }
                Editor3DWindow.getWindow().updateTree_unsavedEntries();
            }
        });

        btn_NewDat[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                createNewDatFile(getShell(), OpenInWhat.EDITOR_TEXT_AND_3D);
            }
        });

        btn_OpenDat[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                openDatFile(getShell(), OpenInWhat.EDITOR_TEXT_AND_3D);
            }
        });

        btn_Undo[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null) {
                    Project.getFileToEdit().undo();
                }
            }
        });

        btn_Redo[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null) {
                    Project.getFileToEdit().redo();
                }
            }
        });

        if (NLogger.DEBUG) {
            btn_AddHistory[0].addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (Project.getFileToEdit() != null) {
                        Project.getFileToEdit().addHistory();
                    }
                }
            });
        }

        btn_Select[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                clickBtnTest(btn_Select[0]);
                workingAction = WorkingMode.SELECT;
            }
        });
        btn_Move[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                clickBtnTest(btn_Move[0]);
                workingAction = WorkingMode.MOVE;
            }
        });
        btn_Rotate[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                clickBtnTest(btn_Rotate[0]);
                workingAction = WorkingMode.ROTATE;
            }
        });
        btn_Scale[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                clickBtnTest(btn_Scale[0]);
                workingAction = WorkingMode.SCALE;
            }
        });
        btn_Combined[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                clickBtnTest(btn_Combined[0]);
                workingAction = WorkingMode.COMBINED;
            }
        });

        btn_Local[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                clickBtnTest(btn_Local[0]);
                transformationMode = ManipulatorScope.LOCAL;
            }
        });
        btn_Global[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                clickBtnTest(btn_Global[0]);
                transformationMode = ManipulatorScope.GLOBAL;
            }
        });

        btn_Vertices[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                clickBtnTest(btn_Vertices[0]);
                setWorkingType(ObjectMode.VERTICES);
            }
        });
        btn_TrisNQuads[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                clickBtnTest(btn_TrisNQuads[0]);
                setWorkingType(ObjectMode.FACES);
            }
        });
        btn_Lines[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                clickBtnTest(btn_Lines[0]);
                setWorkingType(ObjectMode.LINES);
            }
        });
        btn_Subfiles[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null) {
                    clickBtnTest(btn_Subfiles[0]);
                    setWorkingType(ObjectMode.SUBFILES);
                }
            }
        });
        btn_AddComment[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!metaWindow.isOpened()) {
                    metaWindow.run();
                } else {
                    metaWindow.open();
                }
            }
        });
        btn_AddVertex[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                resetAddState();
                clickSingleBtn(btn_AddVertex[0]);
                setAddingVertices(btn_AddVertex[0].getSelection());
                setAddingSomething(isAddingVertices());
            }
        });
        btn_AddPrimitive[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                resetAddState();
                setAddingSubfiles(btn_AddPrimitive[0].getSelection());
                setAddingSomething(isAddingSubfiles());
                clickSingleBtn(btn_AddPrimitive[0]);
            }
        });
        btn_AddLine[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                resetAddState();
                setAddingLines(btn_AddLine[0].getSelection());
                setAddingSomething(isAddingLines());
                clickSingleBtn(btn_AddLine[0]);
            }
        });
        btn_AddTriangle[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                resetAddState();
                setAddingTriangles(btn_AddTriangle[0].getSelection());
                setAddingSomething(isAddingTriangles());
                clickSingleBtn(btn_AddTriangle[0]);
            }
        });
        btn_AddQuad[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                resetAddState();
                setAddingQuads(btn_AddQuad[0].getSelection());
                setAddingSomething(isAddingQuads());
                clickSingleBtn(btn_AddQuad[0]);
            }
        });
        btn_AddCondline[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                resetAddState();
                setAddingCondlines(btn_AddCondline[0].getSelection());
                setAddingSomething(isAddingCondlines());
                clickSingleBtn(btn_AddCondline[0]);
            }
        });
        btn_MoveAdjacentData[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                clickSingleBtn(btn_MoveAdjacentData[0]);
                setMovingAdjacentData(btn_MoveAdjacentData[0].getSelection());
            }
        });
        btn_CompileSubfile[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null) {
                    SubfileCompiler.compile(Project.getFileToEdit(), false, false);
                }
            }
        });
        btn_lineSize1[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                GLPrimitives.SPHERE = GLPrimitives.SPHERE1;
                GLPrimitives.SPHERE_INV = GLPrimitives.SPHERE_INV1;
                View.lineWidth1000[0] = 25f;
                View.lineWidth[0] = .025f;
                View.lineWidthGL[0] = .375f;
                Set<DatFile> dfs = new HashSet<DatFile>();
                for (OpenGLRenderer renderer : renders) {
                    dfs.add(renderer.getC3D().getLockableDatFileReference());
                }
                for (DatFile df : dfs) {
                    SubfileCompiler.compile(df, false, false);
                }
                clickSingleBtn(btn_lineSize1[0]);
            }
        });
        btn_lineSize2[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                GLPrimitives.SPHERE = GLPrimitives.SPHERE2;
                GLPrimitives.SPHERE_INV = GLPrimitives.SPHERE_INV2;
                View.lineWidth1000[0] = 50f;
                View.lineWidth[0] = .050f;
                View.lineWidthGL[0] = .75f;
                Set<DatFile> dfs = new HashSet<DatFile>();
                for (OpenGLRenderer renderer : renders) {
                    dfs.add(renderer.getC3D().getLockableDatFileReference());
                }
                for (DatFile df : dfs) {
                    SubfileCompiler.compile(df, false, false);
                }
                clickSingleBtn(btn_lineSize2[0]);
            }
        });
        btn_lineSize3[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                GLPrimitives.SPHERE = GLPrimitives.SPHERE3;
                GLPrimitives.SPHERE_INV = GLPrimitives.SPHERE_INV3;
                View.lineWidth1000[0] = 100f;
                View.lineWidth[0] = .100f;
                View.lineWidthGL[0] = 1.5f;
                Set<DatFile> dfs = new HashSet<DatFile>();
                for (OpenGLRenderer renderer : renders) {
                    dfs.add(renderer.getC3D().getLockableDatFileReference());
                }
                for (DatFile df : dfs) {
                    SubfileCompiler.compile(df, false, false);
                }
                clickSingleBtn(btn_lineSize3[0]);
            }
        });
        btn_lineSize4[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                GLPrimitives.SPHERE = GLPrimitives.SPHERE4;
                GLPrimitives.SPHERE_INV = GLPrimitives.SPHERE_INV4;
                View.lineWidth1000[0] = 200f;
                View.lineWidth[0] = .200f;
                View.lineWidthGL[0] = 3f;
                Set<DatFile> dfs = new HashSet<DatFile>();
                for (OpenGLRenderer renderer : renders) {
                    dfs.add(renderer.getC3D().getLockableDatFileReference());
                }
                for (DatFile df : dfs) {
                    SubfileCompiler.compile(df, false, false);
                }
                clickSingleBtn(btn_lineSize4[0]);
            }
        });
        btn_BFCswap[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null) {
                    Project.getFileToEdit().getVertexManager().windingChangeSelection();
                }
            }
        });

        btn_RoundSelection[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null) {
                    if ((e.stateMask & SWT.CTRL) == SWT.CTRL) {
                        if (new RoundDialog(getShell()).open() == IDialogConstants.CANCEL_ID) return;
                    }
                    Project.getFileToEdit().getVertexManager()
                    .roundSelection(WorkbenchManager.getUserSettingState().getCoordsPrecision(), WorkbenchManager.getUserSettingState().getTransMatrixPrecision(), isMovingAdjacentData(), true);
                }
            }
        });

        btn_Pipette[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null) {
                    VertexManager vm = Project.getFileToEdit().getVertexManager();
                    final GColour gColour2 = vm.getRandomSelectedColour(lastUsedColour);
                    setLastUsedColour(gColour2);
                    btn_LastUsedColour[0].removeListener(SWT.Paint, btn_LastUsedColour[0].getListeners(SWT.Paint)[0]);
                    btn_LastUsedColour[0].removeListener(SWT.Selection, btn_LastUsedColour[0].getListeners(SWT.Selection)[0]);
                    final Color col = SWTResourceManager.getColor((int) (gColour2.getR() * 255f), (int) (gColour2.getG() * 255f), (int) (gColour2.getB() * 255f));
                    final Point size = btn_LastUsedColour[0].computeSize(SWT.DEFAULT, SWT.DEFAULT);
                    final int x = size.x / 4;
                    final int y = size.y / 4;
                    final int w = size.x / 2;
                    final int h = size.y / 2;
                    int num = gColour2.getColourNumber();
                    btn_LastUsedColour[0].addPaintListener(new PaintListener() {
                        @Override
                        public void paintControl(PaintEvent e) {
                            e.gc.setBackground(col);
                            e.gc.fillRectangle(x, y, w, h);
                            if (gColour2.getA() == 1f) {
                                e.gc.drawImage(ResourceManager.getImage("icon16_transparent.png"), 0, 0, 16, 16, x, y, w, h); //$NON-NLS-1$
                            } else {
                                e.gc.drawImage(ResourceManager.getImage("icon16_halftrans.png"), 0, 0, 16, 16, x, y, w, h); //$NON-NLS-1$
                            }
                        }
                    });
                    btn_LastUsedColour[0].addSelectionListener(new SelectionListener() {
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            if (Project.getFileToEdit() != null) {
                                int num = gColour2.getColourNumber();
                                if (!View.hasLDConfigColour(num)) {
                                    num = -1;
                                }
                                Project.getFileToEdit().getVertexManager().colourChangeSelection(num, gColour2.getR(), gColour2.getG(), gColour2.getB(), gColour2.getA());
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
                        colourBuilder.append(MathHelper.toHex((int) (255f * gColour2.getR())).toUpperCase());
                        colourBuilder.append(MathHelper.toHex((int) (255f * gColour2.getG())).toUpperCase());
                        colourBuilder.append(MathHelper.toHex((int) (255f * gColour2.getB())).toUpperCase());
                        btn_LastUsedColour[0].setToolTipText("Colour [" + colourBuilder.toString() + "]"); //$NON-NLS-1$ //$NON-NLS-2$ I18N
                    }
                    btn_LastUsedColour[0].redraw();
                }
            }
        });

        btn_Palette[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null) {
                    final GColour[] gColour2 = new GColour[1];
                    new ColourDialog(getShell(), gColour2).open();
                    if (gColour2[0] != null) {
                        setLastUsedColour(gColour2[0]);
                        int num = gColour2[0].getColourNumber();
                        if (!View.hasLDConfigColour(num)) {
                            num = -1;
                        }
                        Project.getFileToEdit().getVertexManager().colourChangeSelection(num, gColour2[0].getR(), gColour2[0].getG(), gColour2[0].getB(), gColour2[0].getA());

                        btn_LastUsedColour[0].removeListener(SWT.Paint, btn_LastUsedColour[0].getListeners(SWT.Paint)[0]);
                        btn_LastUsedColour[0].removeListener(SWT.Selection, btn_LastUsedColour[0].getListeners(SWT.Selection)[0]);
                        final Color col = SWTResourceManager.getColor((int) (gColour2[0].getR() * 255f), (int) (gColour2[0].getG() * 255f), (int) (gColour2[0].getB() * 255f));
                        final Point size = btn_LastUsedColour[0].computeSize(SWT.DEFAULT, SWT.DEFAULT);
                        final int x = size.x / 4;
                        final int y = size.y / 4;
                        final int w = size.x / 2;
                        final int h = size.y / 2;
                        btn_LastUsedColour[0].addPaintListener(new PaintListener() {
                            @Override
                            public void paintControl(PaintEvent e) {
                                e.gc.setBackground(col);
                                e.gc.fillRectangle(x, y, w, h);
                                if (gColour2[0].getA() == 1f) {
                                    e.gc.drawImage(ResourceManager.getImage("icon16_transparent.png"), 0, 0, 16, 16, x, y, w, h); //$NON-NLS-1$
                                } else {
                                    e.gc.drawImage(ResourceManager.getImage("icon16_halftrans.png"), 0, 0, 16, 16, x, y, w, h); //$NON-NLS-1$
                                }
                            }
                        });
                        btn_LastUsedColour[0].addSelectionListener(new SelectionListener() {
                            @Override
                            public void widgetSelected(SelectionEvent e) {
                                if (Project.getFileToEdit() != null) {
                                    int num = gColour2[0].getColourNumber();
                                    if (!View.hasLDConfigColour(num)) {
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
            }
        });

        btn_Coarse[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                BigDecimal m = WorkbenchManager.getUserSettingState().getCoarse_move_snap();
                BigDecimal r = WorkbenchManager.getUserSettingState().getCoarse_rotate_snap();
                BigDecimal s = WorkbenchManager.getUserSettingState().getCoarse_scale_snap();
                snapSize = 2;
                spn_Move[0].setValue(m);
                spn_Rotate[0].setValue(r);
                spn_Scale[0].setValue(s);
                Manipulator.setSnap(m, r, s);
            }
        });

        btn_Medium[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                BigDecimal m = WorkbenchManager.getUserSettingState().getMedium_move_snap();
                BigDecimal r = WorkbenchManager.getUserSettingState().getMedium_rotate_snap();
                BigDecimal s = WorkbenchManager.getUserSettingState().getMedium_scale_snap();
                snapSize = 1;
                spn_Move[0].setValue(m);
                spn_Rotate[0].setValue(r);
                spn_Scale[0].setValue(s);
                Manipulator.setSnap(m, r, s);
            }
        });

        btn_Fine[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                BigDecimal m = WorkbenchManager.getUserSettingState().getFine_move_snap();
                BigDecimal r = WorkbenchManager.getUserSettingState().getFine_rotate_snap();
                BigDecimal s = WorkbenchManager.getUserSettingState().getFine_scale_snap();
                snapSize = 0;
                spn_Move[0].setValue(m);
                spn_Rotate[0].setValue(r);
                spn_Scale[0].setValue(s);
                Manipulator.setSnap(m, r, s);
            }
        });

        btn_SplitQuad[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null && !Project.getFileToEdit().isReadOnly()) {
                    Project.getFileToEdit().getVertexManager().splitQuads(true);
                }
            }
        });

        spn_Move[0].addValueChangeListener(new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
                BigDecimal m, r, s;
                m = spn.getValue();
                switch (snapSize) {
                case 0:
                    WorkbenchManager.getUserSettingState().setFine_move_snap(m);
                    r = WorkbenchManager.getUserSettingState().getFine_rotate_snap();
                    s = WorkbenchManager.getUserSettingState().getFine_scale_snap();
                    break;
                case 2:
                    WorkbenchManager.getUserSettingState().setCoarse_move_snap(m);
                    r = WorkbenchManager.getUserSettingState().getCoarse_rotate_snap();
                    s = WorkbenchManager.getUserSettingState().getCoarse_scale_snap();
                    break;
                default:
                    WorkbenchManager.getUserSettingState().setMedium_move_snap(m);
                    r = WorkbenchManager.getUserSettingState().getMedium_rotate_snap();
                    s = WorkbenchManager.getUserSettingState().getMedium_scale_snap();
                    break;
                }
                Manipulator.setSnap(m, r, s);
            }
        });

        spn_Rotate[0].addValueChangeListener(new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
                BigDecimal m, r, s;
                r = spn.getValue();
                switch (snapSize) {
                case 0:
                    m = WorkbenchManager.getUserSettingState().getFine_move_snap();
                    WorkbenchManager.getUserSettingState().setFine_rotate_snap(r);
                    s = WorkbenchManager.getUserSettingState().getFine_scale_snap();
                    break;
                case 2:
                    m = WorkbenchManager.getUserSettingState().getCoarse_move_snap();
                    WorkbenchManager.getUserSettingState().setCoarse_rotate_snap(r);
                    s = WorkbenchManager.getUserSettingState().getCoarse_scale_snap();
                    break;
                default:
                    m = WorkbenchManager.getUserSettingState().getMedium_move_snap();
                    WorkbenchManager.getUserSettingState().setMedium_rotate_snap(r);
                    s = WorkbenchManager.getUserSettingState().getMedium_scale_snap();
                    break;
                }
                Manipulator.setSnap(m, r, s);
            }
        });

        spn_Scale[0].addValueChangeListener(new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
                BigDecimal m, r, s;
                s = spn.getValue();
                switch (snapSize) {
                case 0:
                    m = WorkbenchManager.getUserSettingState().getFine_move_snap();
                    r = WorkbenchManager.getUserSettingState().getFine_rotate_snap();
                    WorkbenchManager.getUserSettingState().setFine_scale_snap(s);
                    break;
                case 2:
                    m = WorkbenchManager.getUserSettingState().getCoarse_move_snap();
                    r = WorkbenchManager.getUserSettingState().getCoarse_rotate_snap();
                    WorkbenchManager.getUserSettingState().setCoarse_scale_snap(s);
                    break;
                default:
                    m = WorkbenchManager.getUserSettingState().getMedium_move_snap();
                    r = WorkbenchManager.getUserSettingState().getMedium_rotate_snap();
                    WorkbenchManager.getUserSettingState().setMedium_scale_snap(s);
                    break;
                }
                Manipulator.setSnap(m, r, s);
            }
        });

        btn_PreviousSelection[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updatingSelectionTab = true;
                NLogger.debug(getClass(), "Previous Selection..."); //$NON-NLS-1$
                final DatFile df = Project.getFileToEdit();
                if (df != null) {
                    final VertexManager vm = df.getVertexManager();
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
                            firstRun = false;
                            vm.setSelectedItemIndex(index);
                            final GData gdata = (GData) vm.getSelectedData().toArray()[index];

                            if (vm.isNotInSubfileAndLinetype2to5(gdata)) {
                                vm.setSelectedLine(gdata);
                                disableSelectionTab();
                                updatingSelectionTab = true;
                                switch (gdata.type()) {
                                case 5:
                                case 4:
                                    spn_SelectionX4[0].setEnabled(true);
                                    spn_SelectionY4[0].setEnabled(true);
                                    spn_SelectionZ4[0].setEnabled(true);
                                case 3:
                                    spn_SelectionX3[0].setEnabled(true);
                                    spn_SelectionY3[0].setEnabled(true);
                                    spn_SelectionZ3[0].setEnabled(true);
                                case 2:
                                    spn_SelectionX1[0].setEnabled(true);
                                    spn_SelectionY1[0].setEnabled(true);
                                    spn_SelectionZ1[0].setEnabled(true);
                                    spn_SelectionX2[0].setEnabled(true);
                                    spn_SelectionY2[0].setEnabled(true);
                                    spn_SelectionZ2[0].setEnabled(true);

                                    txt_Line[0].setText(gdata.toString());
                                    breakIt = true;

                                    switch (gdata.type()) {
                                    case 5:
                                        BigDecimal[] g5 = vm.getPreciseCoordinates(gdata);
                                        spn_SelectionX1[0].setValue(g5[0]);
                                        spn_SelectionY1[0].setValue(g5[1]);
                                        spn_SelectionZ1[0].setValue(g5[2]);
                                        spn_SelectionX2[0].setValue(g5[3]);
                                        spn_SelectionY2[0].setValue(g5[4]);
                                        spn_SelectionZ2[0].setValue(g5[5]);
                                        spn_SelectionX3[0].setValue(g5[6]);
                                        spn_SelectionY3[0].setValue(g5[7]);
                                        spn_SelectionZ3[0].setValue(g5[8]);
                                        spn_SelectionX4[0].setValue(g5[9]);
                                        spn_SelectionY4[0].setValue(g5[10]);
                                        spn_SelectionZ4[0].setValue(g5[11]);
                                        break;
                                    case 4:
                                        BigDecimal[] g4 = vm.getPreciseCoordinates(gdata);
                                        spn_SelectionX1[0].setValue(g4[0]);
                                        spn_SelectionY1[0].setValue(g4[1]);
                                        spn_SelectionZ1[0].setValue(g4[2]);
                                        spn_SelectionX2[0].setValue(g4[3]);
                                        spn_SelectionY2[0].setValue(g4[4]);
                                        spn_SelectionZ2[0].setValue(g4[5]);
                                        spn_SelectionX3[0].setValue(g4[6]);
                                        spn_SelectionY3[0].setValue(g4[7]);
                                        spn_SelectionZ3[0].setValue(g4[8]);
                                        spn_SelectionX4[0].setValue(g4[9]);
                                        spn_SelectionY4[0].setValue(g4[10]);
                                        spn_SelectionZ4[0].setValue(g4[11]);
                                        break;
                                    case 3:
                                        BigDecimal[] g3 = vm.getPreciseCoordinates(gdata);
                                        spn_SelectionX1[0].setValue(g3[0]);
                                        spn_SelectionY1[0].setValue(g3[1]);
                                        spn_SelectionZ1[0].setValue(g3[2]);
                                        spn_SelectionX2[0].setValue(g3[3]);
                                        spn_SelectionY2[0].setValue(g3[4]);
                                        spn_SelectionZ2[0].setValue(g3[5]);
                                        spn_SelectionX3[0].setValue(g3[6]);
                                        spn_SelectionY3[0].setValue(g3[7]);
                                        spn_SelectionZ3[0].setValue(g3[8]);
                                        break;
                                    case 2:
                                        BigDecimal[] g2 = vm.getPreciseCoordinates(gdata);
                                        spn_SelectionX1[0].setValue(g2[0]);
                                        spn_SelectionY1[0].setValue(g2[1]);
                                        spn_SelectionZ1[0].setValue(g2[2]);
                                        spn_SelectionX2[0].setValue(g2[3]);
                                        spn_SelectionY2[0].setValue(g2[4]);
                                        spn_SelectionZ2[0].setValue(g2[5]);
                                        break;
                                    default:
                                        disableSelectionTab();
                                        updatingSelectionTab = true;
                                        break;
                                    }

                                    lbl_SelectionX1[0].setText(I18n.EDITOR3D_PositionX1 + " {" + spn_SelectionX1[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$
                                    lbl_SelectionY1[0].setText(I18n.EDITOR3D_PositionY1 + " {" + spn_SelectionY1[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$
                                    lbl_SelectionZ1[0].setText(I18n.EDITOR3D_PositionZ1 + " {" + spn_SelectionZ1[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$
                                    lbl_SelectionX2[0].setText(I18n.EDITOR3D_PositionX2 + " {" + spn_SelectionX2[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$
                                    lbl_SelectionY2[0].setText(I18n.EDITOR3D_PositionY2 + " {" + spn_SelectionY2[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$
                                    lbl_SelectionZ2[0].setText(I18n.EDITOR3D_PositionZ2 + " {" + spn_SelectionZ2[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$
                                    lbl_SelectionX3[0].setText(I18n.EDITOR3D_PositionX3 + " {" + spn_SelectionX3[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$
                                    lbl_SelectionY3[0].setText(I18n.EDITOR3D_PositionY3 + " {" + spn_SelectionY3[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$
                                    lbl_SelectionZ3[0].setText(I18n.EDITOR3D_PositionZ3 + " {" + spn_SelectionZ3[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$
                                    lbl_SelectionX4[0].setText(I18n.EDITOR3D_PositionX4 + " {" + spn_SelectionX4[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$
                                    lbl_SelectionY4[0].setText(I18n.EDITOR3D_PositionY4 + " {" + spn_SelectionY4[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$
                                    lbl_SelectionZ4[0].setText(I18n.EDITOR3D_PositionZ4 + " {" + spn_SelectionZ4[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$

                                    lbl_SelectionX1[0].getParent().layout();
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
            }
        });
        btn_NextSelection[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updatingSelectionTab = true;
                NLogger.debug(getClass(), "Next Selection..."); //$NON-NLS-1$
                final DatFile df = Project.getFileToEdit();
                if (df != null) {
                    final VertexManager vm = df.getVertexManager();
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

                            if (vm.isNotInSubfileAndLinetype2to5(gdata)) {
                                vm.setSelectedLine(gdata);
                                disableSelectionTab();
                                updatingSelectionTab = true;
                                switch (gdata.type()) {
                                case 5:
                                case 4:
                                    spn_SelectionX4[0].setEnabled(true);
                                    spn_SelectionY4[0].setEnabled(true);
                                    spn_SelectionZ4[0].setEnabled(true);
                                case 3:
                                    spn_SelectionX3[0].setEnabled(true);
                                    spn_SelectionY3[0].setEnabled(true);
                                    spn_SelectionZ3[0].setEnabled(true);
                                case 2:
                                    spn_SelectionX1[0].setEnabled(true);
                                    spn_SelectionY1[0].setEnabled(true);
                                    spn_SelectionZ1[0].setEnabled(true);
                                    spn_SelectionX2[0].setEnabled(true);
                                    spn_SelectionY2[0].setEnabled(true);
                                    spn_SelectionZ2[0].setEnabled(true);

                                    txt_Line[0].setText(gdata.toString());
                                    breakIt = true;

                                    switch (gdata.type()) {
                                    case 5:
                                        BigDecimal[] g5 = vm.getPreciseCoordinates(gdata);
                                        spn_SelectionX1[0].setValue(g5[0]);
                                        spn_SelectionY1[0].setValue(g5[1]);
                                        spn_SelectionZ1[0].setValue(g5[2]);
                                        spn_SelectionX2[0].setValue(g5[3]);
                                        spn_SelectionY2[0].setValue(g5[4]);
                                        spn_SelectionZ2[0].setValue(g5[5]);
                                        spn_SelectionX3[0].setValue(g5[6]);
                                        spn_SelectionY3[0].setValue(g5[7]);
                                        spn_SelectionZ3[0].setValue(g5[8]);
                                        spn_SelectionX4[0].setValue(g5[9]);
                                        spn_SelectionY4[0].setValue(g5[10]);
                                        spn_SelectionZ4[0].setValue(g5[11]);
                                        break;
                                    case 4:
                                        BigDecimal[] g4 = vm.getPreciseCoordinates(gdata);
                                        spn_SelectionX1[0].setValue(g4[0]);
                                        spn_SelectionY1[0].setValue(g4[1]);
                                        spn_SelectionZ1[0].setValue(g4[2]);
                                        spn_SelectionX2[0].setValue(g4[3]);
                                        spn_SelectionY2[0].setValue(g4[4]);
                                        spn_SelectionZ2[0].setValue(g4[5]);
                                        spn_SelectionX3[0].setValue(g4[6]);
                                        spn_SelectionY3[0].setValue(g4[7]);
                                        spn_SelectionZ3[0].setValue(g4[8]);
                                        spn_SelectionX4[0].setValue(g4[9]);
                                        spn_SelectionY4[0].setValue(g4[10]);
                                        spn_SelectionZ4[0].setValue(g4[11]);
                                        break;
                                    case 3:
                                        BigDecimal[] g3 = vm.getPreciseCoordinates(gdata);
                                        spn_SelectionX1[0].setValue(g3[0]);
                                        spn_SelectionY1[0].setValue(g3[1]);
                                        spn_SelectionZ1[0].setValue(g3[2]);
                                        spn_SelectionX2[0].setValue(g3[3]);
                                        spn_SelectionY2[0].setValue(g3[4]);
                                        spn_SelectionZ2[0].setValue(g3[5]);
                                        spn_SelectionX3[0].setValue(g3[6]);
                                        spn_SelectionY3[0].setValue(g3[7]);
                                        spn_SelectionZ3[0].setValue(g3[8]);
                                        break;
                                    case 2:
                                        BigDecimal[] g2 = vm.getPreciseCoordinates(gdata);
                                        spn_SelectionX1[0].setValue(g2[0]);
                                        spn_SelectionY1[0].setValue(g2[1]);
                                        spn_SelectionZ1[0].setValue(g2[2]);
                                        spn_SelectionX2[0].setValue(g2[3]);
                                        spn_SelectionY2[0].setValue(g2[4]);
                                        spn_SelectionZ2[0].setValue(g2[5]);
                                        break;
                                    default:
                                        disableSelectionTab();
                                        updatingSelectionTab = true;
                                        break;
                                    }

                                    lbl_SelectionX1[0].setText(I18n.EDITOR3D_PositionX1 + " {" + spn_SelectionX1[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$
                                    lbl_SelectionY1[0].setText(I18n.EDITOR3D_PositionY1 + " {" + spn_SelectionY1[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$
                                    lbl_SelectionZ1[0].setText(I18n.EDITOR3D_PositionZ1 + " {" + spn_SelectionZ1[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$
                                    lbl_SelectionX2[0].setText(I18n.EDITOR3D_PositionX2 + " {" + spn_SelectionX2[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$
                                    lbl_SelectionY2[0].setText(I18n.EDITOR3D_PositionY2 + " {" + spn_SelectionY2[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$
                                    lbl_SelectionZ2[0].setText(I18n.EDITOR3D_PositionZ2 + " {" + spn_SelectionZ2[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$
                                    lbl_SelectionX3[0].setText(I18n.EDITOR3D_PositionX3 + " {" + spn_SelectionX3[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$
                                    lbl_SelectionY3[0].setText(I18n.EDITOR3D_PositionY3 + " {" + spn_SelectionY3[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$
                                    lbl_SelectionZ3[0].setText(I18n.EDITOR3D_PositionZ3 + " {" + spn_SelectionZ3[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$
                                    lbl_SelectionX4[0].setText(I18n.EDITOR3D_PositionX4 + " {" + spn_SelectionX4[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$
                                    lbl_SelectionY4[0].setText(I18n.EDITOR3D_PositionY4 + " {" + spn_SelectionY4[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$
                                    lbl_SelectionZ4[0].setText(I18n.EDITOR3D_PositionZ4 + " {" + spn_SelectionZ4[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$

                                    lbl_SelectionX1[0].getParent().layout();
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
            }
        });

        final ValueChangeAdapter va = new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
                if (updatingSelectionTab) return;
                final GData newLine = Project.getFileToEdit().getVertexManager().updateSelectedLine(
                        spn_SelectionX1[0].getValue(), spn_SelectionY1[0].getValue(), spn_SelectionZ1[0].getValue(),
                        spn_SelectionX2[0].getValue(), spn_SelectionY2[0].getValue(), spn_SelectionZ2[0].getValue(),
                        spn_SelectionX3[0].getValue(), spn_SelectionY3[0].getValue(), spn_SelectionZ3[0].getValue(),
                        spn_SelectionX4[0].getValue(), spn_SelectionY4[0].getValue(), spn_SelectionZ4[0].getValue(),
                        btn_MoveAdjacentData2[0].getSelection()
                        );
                if (newLine == null) {
                    disableSelectionTab();
                } else {
                    txt_Line[0].setText(newLine.toString());
                }
            }
        };

        spn_SelectionX1[0].addValueChangeListener(va);
        spn_SelectionY1[0].addValueChangeListener(va);
        spn_SelectionZ1[0].addValueChangeListener(va);
        spn_SelectionX2[0].addValueChangeListener(va);
        spn_SelectionY2[0].addValueChangeListener(va);
        spn_SelectionZ2[0].addValueChangeListener(va);
        spn_SelectionX3[0].addValueChangeListener(va);
        spn_SelectionY3[0].addValueChangeListener(va);
        spn_SelectionZ3[0].addValueChangeListener(va);
        spn_SelectionX4[0].addValueChangeListener(va);
        spn_SelectionY4[0].addValueChangeListener(va);
        spn_SelectionZ4[0].addValueChangeListener(va);

        //        treeParts[0].addSelectionListener(new SelectionAdapter() {
        //            @Override
        //            public void widgetSelected(final SelectionEvent e) {
        //
        //            }
        //        });

        treeParts[0].addListener(SWT.MouseDown, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (event.button == MouseButton.RIGHT) {

                    NLogger.debug(getClass(), "Showing context menu."); //$NON-NLS-1$

                    try {
                        if (treeParts[0].getTree().getMenu() != null) {
                            treeParts[0].getTree().getMenu().dispose();
                        }
                    } catch (Exception ex) {}

                    Menu treeMenu = new Menu(treeParts[0].getTree());
                    treeParts[0].getTree().setMenu(treeMenu);
                    mnu_treeMenu[0] = treeMenu;

                    MenuItem mntmOpenIn3DEditor = new MenuItem(treeMenu, I18n.I18N_NON_BIDIRECT());
                    mntm_OpenIn3DEditor[0] = mntmOpenIn3DEditor;
                    mntmOpenIn3DEditor.setEnabled(true);
                    mntmOpenIn3DEditor.setText("Open In 3D Editor"); //$NON-NLS-1$ I18N Needs translation!

                    MenuItem mntmOpenInTextEditor = new MenuItem(treeMenu, I18n.I18N_NON_BIDIRECT());
                    mntm_OpenInTextEditor[0] = mntmOpenInTextEditor;
                    mntmOpenInTextEditor.setEnabled(true);
                    mntmOpenInTextEditor.setText("Open In Text Editor"); //$NON-NLS-1$ I18N Needs translation!

                    @SuppressWarnings("unused")
                    MenuItem mntm_Separator = new MenuItem(treeMenu, I18n.I18N_NON_BIDIRECT() | SWT.SEPARATOR);

                    MenuItem mntmRename = new MenuItem(treeMenu, I18n.I18N_NON_BIDIRECT());
                    mntm_Rename[0] = mntmRename;
                    mntmRename.setEnabled(true);
                    mntmRename.setText("Rename / Move"); //$NON-NLS-1$ I18N Needs translation!

                    MenuItem mntmRevert = new MenuItem(treeMenu, I18n.I18N_NON_BIDIRECT());
                    mntm_Revert[0] = mntmRevert;
                    mntmRevert.setEnabled(true);
                    mntmRevert.setText("Revert All Changes"); //$NON-NLS-1$ I18N Needs translation!

                    MenuItem mntmDelete = new MenuItem(treeMenu, I18n.I18N_NON_BIDIRECT());
                    mntm_Delete[0] = mntmDelete;
                    mntmDelete.setEnabled(true);
                    mntmDelete.setText("Delete"); //$NON-NLS-1$ I18N Needs translation!

                    @SuppressWarnings("unused")
                    MenuItem mntm_Separator2 = new MenuItem(treeMenu, I18n.I18N_NON_BIDIRECT() | SWT.SEPARATOR);

                    MenuItem mntmCopyToUnofficial = new MenuItem(treeMenu, I18n.I18N_NON_BIDIRECT());
                    mntm_CopyToUnofficial[0] = mntmCopyToUnofficial;
                    mntmCopyToUnofficial.setEnabled(true);
                    mntmCopyToUnofficial.setText("Copy To Unofficial Library"); //$NON-NLS-1$ I18N Needs translation!

                    mntm_OpenInTextEditor[0].addSelectionListener(new SelectionAdapter() {
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            if (treeParts[0].getSelectionCount() == 1 && treeParts[0].getSelection()[0] != null && treeParts[0].getSelection()[0].getData() instanceof DatFile) {
                                DatFile df = (DatFile) treeParts[0].getSelection()[0].getData();
                                for (EditorTextWindow w : Project.getOpenTextWindows()) {
                                    for (CTabItem t : w.getTabFolder().getItems()) {
                                        if (df.equals(((CompositeTab) t).getState().getFileNameObj())) {
                                            w.getTabFolder().setSelection(t);
                                            ((CompositeTab) t).getControl().getShell().forceActive();
                                            w.open();
                                            df.getVertexManager().setUpdated(true);
                                            return;
                                        }
                                    }
                                }
                                // Project.getParsedFiles().add(df); IS NECESSARY HERE
                                Project.getParsedFiles().add(df);
                                new EditorTextWindow().run(df);
                            } else {
                                MessageBox messageBoxError = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
                                messageBoxError.setText(I18n.DIALOG_UnavailableTitle);
                                messageBoxError.setMessage(I18n.DIALOG_Unavailable);
                                messageBoxError.open();
                            }
                            cleanupClosedData();
                        }
                    });
                    mntm_OpenIn3DEditor[0].addSelectionListener(new SelectionAdapter() {
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            if (treeParts[0].getSelectionCount() == 1 && treeParts[0].getSelection()[0] != null && treeParts[0].getSelection()[0].getData() instanceof DatFile) {

                                if (renders.isEmpty()) {

                                    if ("%EMPTY%".equals(Editor3DWindow.getSashForm().getChildren()[1].getData())) { //$NON-NLS-1$
                                        int[] mainSashWeights = Editor3DWindow.getSashForm().getWeights();
                                        Editor3DWindow.getSashForm().getChildren()[1].dispose();
                                        CompositeContainer cmp_Container = new CompositeContainer(Editor3DWindow.getSashForm(), false);
                                        cmp_Container.moveBelow(Editor3DWindow.getSashForm().getChildren()[0]);
                                        DatFile df = (DatFile) treeParts[0].getSelection()[0].getData();
                                        df.parseForData(true);
                                        Project.setFileToEdit(df);
                                        cmp_Container.getComposite3D().setLockableDatFileReference(df);
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

                                    if (canUpdate) {
                                        DatFile df = (DatFile) treeParts[0].getSelection()[0].getData();
                                        final VertexManager vm = df.getVertexManager();
                                        if (vm.isModified()) {
                                            df.setText(df.getText());
                                        }
                                        df.parseForData(true);

                                        Project.setFileToEdit(df);
                                        for (OpenGLRenderer renderer : renders) {
                                            Composite3D c3d = renderer.getC3D();
                                            if (!c3d.isDatFileLockedOnDisplay()) {
                                                c3d.setLockableDatFileReference(df);
                                                vm.zoomToFit(c3d);
                                            }
                                        }

                                    }
                                }
                            } else {
                                MessageBox messageBoxError = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
                                messageBoxError.setText(I18n.DIALOG_UnavailableTitle);
                                messageBoxError.setMessage(I18n.DIALOG_Unavailable);
                                messageBoxError.open();
                            }
                            cleanupClosedData();
                        }
                    });
                    mntm_Revert[0].addSelectionListener(new SelectionAdapter() {
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            if (treeParts[0].getSelectionCount() == 1 && treeParts[0].getSelection()[0] != null && treeParts[0].getSelection()[0].getData() instanceof DatFile) {
                                DatFile df = (DatFile) treeParts[0].getSelection()[0].getData();
                                if (df.isReadOnly() || !Project.getUnsavedFiles().contains(df) || df.isVirtual() && df.getText().trim().isEmpty()) return;

                                MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
                                messageBox.setText(I18n.DIALOG_RevertTitle);

                                Object[] messageArguments = {df.getShortName(), df.getLastSavedOpened()};
                                MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                                formatter.setLocale(View.LOCALE);
                                formatter.applyPattern(I18n.DIALOG_Revert);
                                messageBox.setMessage(formatter.format(messageArguments));

                                int result = messageBox.open();

                                if (result == SWT.NO) {
                                    return;
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
                                    updateTree_unsavedEntries();
                                }

                                if (canUpdate) {

                                    df.parseForData(true);
                                    df.getVertexManager().setModified(true, true);

                                    if (tmpW != null) {
                                        tmpW.getTabFolder().setSelection(tmpT);
                                        ((CompositeTab) tmpT).getControl().getShell().forceActive();
                                        tmpW.open();
                                        ((CompositeTab) tmpT).getTextComposite().forceFocus();
                                    }
                                }
                            } else {
                                MessageBox messageBoxError = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
                                messageBoxError.setText(I18n.DIALOG_UnavailableTitle);
                                messageBoxError.setMessage(I18n.DIALOG_Unavailable);
                                messageBoxError.open();
                            }
                        }
                    });
                    mntm_Delete[0].addSelectionListener(new SelectionAdapter() {
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            if (treeParts[0].getSelectionCount() == 1 && treeParts[0].getSelection()[0] != null && treeParts[0].getSelection()[0].getData() instanceof DatFile) {
                                DatFile df = (DatFile) treeParts[0].getSelection()[0].getData();
                                if (df.isReadOnly()) {
                                    if (treeParts[0].getSelection()[0].getParentItem().getParentItem() == treeItem_Project[0]) {
                                        updateTree_removeEntry(df);
                                        cleanupClosedData();
                                    }
                                    return;
                                }

                                MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
                                messageBox.setText(I18n.DIALOG_DeleteTitle);

                                Object[] messageArguments = {df.getShortName()};
                                MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                                formatter.setLocale(View.LOCALE);
                                formatter.applyPattern(I18n.DIALOG_Delete);
                                messageBox.setMessage(formatter.format(messageArguments));

                                int result = messageBox.open();

                                if (result == SWT.NO) {
                                    return;
                                }

                                updateTree_removeEntry(df);

                                try {
                                    File f = new File(df.getOldName());
                                    if (f.exists()) {
                                        f.delete();
                                    }
                                } catch (Exception ex) {}

                                cleanupClosedData();
                            } else {
                                MessageBox messageBoxError = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
                                messageBoxError.setText(I18n.DIALOG_UnavailableTitle);
                                messageBoxError.setMessage(I18n.DIALOG_Unavailable);
                                messageBoxError.open();
                            }
                        }
                    });
                    mntm_Rename[0].addSelectionListener(new SelectionAdapter() {
                        @SuppressWarnings("unchecked")
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            if (treeParts[0].getSelectionCount() == 1 && treeParts[0].getSelection()[0] != null && treeParts[0].getSelection()[0].getData() instanceof DatFile) {
                                DatFile df = (DatFile) treeParts[0].getSelection()[0].getData();
                                if (df.isReadOnly()) return;

                                FileDialog dlg = new FileDialog(Editor3DWindow.getWindow().getShell(), SWT.SAVE);

                                File tmp = new File(df.getNewName());
                                dlg.setFilterPath(tmp.getAbsolutePath().substring(0, tmp.getAbsolutePath().length() - tmp.getName().length()));
                                dlg.setFileName(tmp.getName());
                                dlg.setFilterExtensions(new String[]{"*.dat"}); //$NON-NLS-1$
                                dlg.setOverwrite(true);

                                // Change the title bar text
                                dlg.setText(I18n.DIALOG_RenameOrMove);

                                // Calling open() will open and run the dialog.
                                // It will return the selected file, or
                                // null if user cancels
                                String newPath = dlg.open();
                                if (newPath != null) {

                                    while (isFileNameAllocated(newPath, df, false)) {
                                        MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.RETRY | SWT.CANCEL);
                                        messageBox.setText(I18n.DIALOG_AlreadyAllocatedNameTitle);
                                        messageBox.setMessage(I18n.DIALOG_AlreadyAllocatedName);

                                        int result = messageBox.open();

                                        if (result == SWT.CANCEL) {
                                            return;
                                        }
                                        newPath = dlg.open();
                                        if (newPath == null) return;
                                    }


                                    if (df.isProjectFile() && !newPath.startsWith(Project.getProjectPath())) {

                                        MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
                                        messageBox.setText(I18n.DIALOG_NoProjectLocationTitle);

                                        Object[] messageArguments = {new File(newPath).getName()};
                                        MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                                        formatter.setLocale(View.LOCALE);
                                        formatter.applyPattern(I18n.DIALOG_NoProjectLocation);
                                        messageBox.setMessage(formatter.format(messageArguments));

                                        int result = messageBox.open();

                                        if (result == SWT.NO) {
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

                                    HashSet<EditorTextWindow> windows = new HashSet<EditorTextWindow>(Project.getOpenTextWindows());
                                    for (EditorTextWindow win : windows) {
                                        win.updateTabWithDatfile(df);
                                    }
                                    updateTree_renamedEntries();
                                    updateTree_unsavedEntries();
                                }
                            } else if (treeParts[0].getSelectionCount() == 1 && treeParts[0].getSelection()[0] != null && treeParts[0].getSelection()[0].equals(treeItem_Project[0])) {
                                if (Project.isDefaultProject()) {
                                    ProjectActions.createNewProject(Editor3DWindow.getWindow(), true);
                                } else {
                                    int result = new NewProjectDialog(true).open();
                                    if (result == IDialogConstants.OK_ID && !Project.getTempProjectPath().equals(Project.getProjectPath())) {
                                        try {
                                            while (new File(Project.getTempProjectPath()).isDirectory()) {
                                                MessageBox messageBoxError = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.YES | SWT.CANCEL | SWT.NO);
                                                messageBoxError.setText(I18n.PROJECT_ProjectOverwriteTitle);
                                                messageBoxError.setMessage(I18n.PROJECT_ProjectOverwrite);
                                                int result2 = messageBoxError.open();
                                                if (result2 == SWT.CANCEL) {
                                                    return;
                                                } else if (result2 == SWT.YES) {
                                                    break;
                                                } else {
                                                    result = new NewProjectDialog(true).open();
                                                    if (result == IDialogConstants.CANCEL_ID) {
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
                                            HashSet<DatFile> projectFiles = new HashSet<DatFile>();
                                            projectFiles.addAll((ArrayList<DatFile>) Editor3DWindow.getWindow().getProjectParts().getData());
                                            projectFiles.addAll((ArrayList<DatFile>) Editor3DWindow.getWindow().getProjectSubparts().getData());
                                            projectFiles.addAll((ArrayList<DatFile>) Editor3DWindow.getWindow().getProjectPrimitives().getData());
                                            projectFiles.addAll((ArrayList<DatFile>) Editor3DWindow.getWindow().getProjectPrimitives48().getData());
                                            for (DatFile df : projectFiles) {
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
                                            }
                                            Project.setProjectName(Project.getTempProjectName());
                                            Project.setProjectPath(Project.getTempProjectPath());
                                            Editor3DWindow.getWindow().getProjectParts().getParentItem().setText(Project.getProjectName());
                                            updateTree_unsavedEntries();
                                            Project.updateEditor();
                                            Editor3DWindow.getWindow().getShell().update();
                                        } catch (IOException e1) {
                                            // TODO Auto-generated catch block
                                            e1.printStackTrace();
                                        }
                                    }
                                }
                            } else {
                                MessageBox messageBoxError = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
                                messageBoxError.setText(I18n.DIALOG_UnavailableTitle);
                                messageBoxError.setMessage(I18n.DIALOG_Unavailable);
                                messageBoxError.open();
                            }
                        }
                    });
                    mntm_CopyToUnofficial[0] .addSelectionListener(new SelectionAdapter() {
                        @SuppressWarnings("unchecked")
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            if (treeParts[0].getSelectionCount() == 1 && treeParts[0].getSelection()[0] != null && treeParts[0].getSelection()[0].getData() instanceof DatFile) {
                                DatFile df = (DatFile) treeParts[0].getSelection()[0].getData();
                                TreeItem p = treeParts[0].getSelection()[0].getParentItem();
                                String targetPath_u;
                                String targetPath_l;
                                String targetPathDir_u;
                                String targetPathDir_l;
                                TreeItem targetTreeItem;
                                boolean projectIsFileOrigin = false;
                                if (treeItem_ProjectParts[0].equals(p)) {
                                    targetPath_u = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "PARTS"; //$NON-NLS-1$
                                    targetPath_l = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "parts"; //$NON-NLS-1$
                                    targetTreeItem = treeItem_UnofficialParts[0];
                                    projectIsFileOrigin = true;
                                } else if (treeItem_ProjectPrimitives[0].equals(p)) {
                                    targetPath_u = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "P"; //$NON-NLS-1$
                                    targetPath_l = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "p"; //$NON-NLS-1$
                                    targetTreeItem = treeItem_UnofficialPrimitives[0];
                                    projectIsFileOrigin = true;
                                } else if (treeItem_ProjectPrimitives48[0].equals(p)) {
                                    targetPath_u = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "P" + File.separator + "48"; //$NON-NLS-1$ //$NON-NLS-2$
                                    targetPath_l = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "p" + File.separator + "48"; //$NON-NLS-1$ //$NON-NLS-2$
                                    targetTreeItem = treeItem_UnofficialPrimitives48[0];
                                    projectIsFileOrigin = true;
                                } else if (treeItem_ProjectPrimitives8[0].equals(p)) {
                                    targetPath_u = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "P" + File.separator + "8"; //$NON-NLS-1$ //$NON-NLS-2$
                                    targetPath_l = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "p" + File.separator + "8"; //$NON-NLS-1$ //$NON-NLS-2$
                                    targetTreeItem = treeItem_UnofficialPrimitives8[0];
                                    projectIsFileOrigin = true;
                                } else if (treeItem_ProjectSubparts[0].equals(p)) {
                                    targetPath_u = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "PARTS"+ File.separator + "S"; //$NON-NLS-1$ //$NON-NLS-2$
                                    targetPath_l = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "parts"+ File.separator + "s"; //$NON-NLS-1$ //$NON-NLS-2$
                                    targetTreeItem = treeItem_UnofficialSubparts[0];
                                    projectIsFileOrigin = true;
                                } else if (treeItem_OfficialParts[0].equals(p)) {
                                    targetPath_u = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "PARTS"; //$NON-NLS-1$
                                    targetPath_l = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "parts"; //$NON-NLS-1$
                                    targetTreeItem = treeItem_UnofficialParts[0];
                                } else if (treeItem_OfficialPrimitives[0].equals(p)) {
                                    targetPath_u = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "P"; //$NON-NLS-1$
                                    targetPath_l = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "p"; //$NON-NLS-1$
                                    targetTreeItem = treeItem_UnofficialPrimitives[0];
                                } else if (treeItem_OfficialPrimitives48[0].equals(p)) {
                                    targetPath_u = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "P" + File.separator + "48"; //$NON-NLS-1$ //$NON-NLS-2$
                                    targetPath_l = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "p" + File.separator + "48"; //$NON-NLS-1$ //$NON-NLS-2$
                                    targetTreeItem = treeItem_UnofficialPrimitives48[0];
                                } else if (treeItem_OfficialPrimitives8[0].equals(p)) {
                                    targetPath_u = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "P" + File.separator + "8"; //$NON-NLS-1$ //$NON-NLS-2$
                                    targetPath_l = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "p" + File.separator + "8"; //$NON-NLS-1$ //$NON-NLS-2$
                                    targetTreeItem = treeItem_UnofficialPrimitives8[0];
                                } else if (treeItem_OfficialSubparts[0].equals(p)) {
                                    targetPath_u = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "PARTS"+ File.separator + "S"; //$NON-NLS-1$ //$NON-NLS-2$
                                    targetPath_l = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "parts"+ File.separator + "s"; //$NON-NLS-1$ //$NON-NLS-2$
                                    targetTreeItem = treeItem_UnofficialSubparts[0];
                                } else {
                                    MessageBox messageBoxError = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
                                    messageBoxError.setText(I18n.DIALOG_UnavailableTitle);
                                    messageBoxError.setMessage(I18n.DIALOG_Unavailable);
                                    messageBoxError.open();
                                    return;
                                }

                                targetPathDir_l = targetPath_l;
                                targetPathDir_u = targetPath_u;

                                final String newName = new File(df.getNewName()).getName();
                                targetPath_u = targetPath_u + File.separator + newName;
                                targetPath_l = targetPath_l + File.separator + newName;

                                DatFile fileToOverwrite_u = new DatFile(targetPath_u);
                                DatFile fileToOverwrite_l = new DatFile(targetPath_l);

                                DatFile targetFile = null;

                                TreeItem[] folders = new TreeItem[5];
                                folders[0] = treeItem_UnofficialParts[0];
                                folders[1] = treeItem_UnofficialPrimitives[0];
                                folders[2] = treeItem_UnofficialPrimitives48[0];
                                folders[3] = treeItem_UnofficialPrimitives8[0];
                                folders[4] = treeItem_UnofficialSubparts[0];

                                for (TreeItem folder : folders) {
                                    ArrayList<DatFile> cachedReferences =(ArrayList<DatFile>) folder.getData();
                                    for (DatFile d : cachedReferences) {
                                        if (fileToOverwrite_u.equals(d) || fileToOverwrite_l.equals(d)) {
                                            targetFile = d;
                                            break;
                                        }
                                    }
                                }

                                if (new File(targetPath_u).exists() || new File(targetPath_l).exists() || targetFile != null) {
                                    MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
                                    messageBox.setText(I18n.DIALOG_ReplaceTitle);

                                    Object[] messageArguments = {newName};
                                    MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                                    formatter.setLocale(View.LOCALE);
                                    formatter.applyPattern(I18n.DIALOG_Replace);
                                    messageBox.setMessage(formatter.format(messageArguments));

                                    int result = messageBox.open();

                                    if (result == SWT.CANCEL) {
                                        return;
                                    }
                                }

                                ArrayList<ArrayList<DatFile>> refResult = null;

                                if (new File(targetPathDir_l).exists() || new File(targetPathDir_u).exists()) {
                                    if (targetFile == null) {

                                        int result = new CopyDialog(getShell(), new File(df.getNewName()).getName()).open();


                                        switch (result) {
                                        case IDialogConstants.OK_ID:
                                            // Copy File Only
                                            break;
                                        case IDialogConstants.NO_ID:
                                            // Copy File and required and related
                                            if (projectIsFileOrigin) {
                                                refResult = ReferenceParser.checkForReferences(df, References.REQUIRED_AND_RELATED, treeItem_Project[0], treeItem_Unofficial[0], treeItem_Official[0]);
                                            } else {
                                                refResult = ReferenceParser.checkForReferences(df, References.REQUIRED_AND_RELATED, treeItem_Official[0], treeItem_Unofficial[0], treeItem_Project[0]);
                                            }
                                            break;
                                        case IDialogConstants.YES_ID:
                                            // Copy File and required
                                            if (projectIsFileOrigin) {
                                                refResult = ReferenceParser.checkForReferences(df, References.REQUIRED, treeItem_Project[0], treeItem_Unofficial[0], treeItem_Official[0]);
                                            } else {
                                                refResult = ReferenceParser.checkForReferences(df, References.REQUIRED, treeItem_Official[0], treeItem_Unofficial[0], treeItem_Project[0]);
                                            }
                                            break;
                                        default:
                                            return;
                                        }
                                        DatFile newDatFile = new DatFile(new File(targetPathDir_l).exists() ? targetPath_l : targetPath_u);
                                        // Text exchange includes description exchange
                                        newDatFile.setText(df.getText());
                                        newDatFile.saveForced();
                                        newDatFile.setType(df.getType());
                                        ((ArrayList<DatFile>) targetTreeItem.getData()).add(newDatFile);
                                        TreeItem ti = new TreeItem(targetTreeItem, SWT.NONE);
                                        ti.setText(new File(df.getNewName()).getName());
                                        ti.setData(newDatFile);
                                    } else if (targetFile.equals(df)) { // This can only happen if the user opens the unofficial parts folder as a project
                                        MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                                        messageBox.setText(I18n.DIALOG_AlreadyAllocatedNameTitle);
                                        messageBox.setMessage(I18n.DIALOG_AlreadyAllocatedName);
                                        messageBox.open();
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
                                                refResult = ReferenceParser.checkForReferences(df, References.REQUIRED_AND_RELATED, treeItem_Project[0], treeItem_Unofficial[0], treeItem_Official[0]);
                                            } else {
                                                refResult = ReferenceParser.checkForReferences(df, References.REQUIRED_AND_RELATED, treeItem_Official[0], treeItem_Unofficial[0], treeItem_Project[0]);
                                            }
                                            break;
                                        case IDialogConstants.YES_ID:
                                            // Copy File and required
                                            if (projectIsFileOrigin) {
                                                refResult = ReferenceParser.checkForReferences(df, References.REQUIRED, treeItem_Project[0], treeItem_Unofficial[0], treeItem_Official[0]);
                                            } else {
                                                refResult = ReferenceParser.checkForReferences(df, References.REQUIRED, treeItem_Official[0], treeItem_Unofficial[0], treeItem_Project[0]);
                                            }
                                            break;
                                        default:
                                            return;
                                        }

                                        targetFile.disposeData();
                                        updateTree_removeEntry(targetFile);
                                        DatFile newDatFile = new DatFile(new File(targetPathDir_l).exists() ? targetPath_l : targetPath_u);
                                        newDatFile.setText(df.getText());
                                        newDatFile.saveForced();
                                        ((ArrayList<DatFile>) targetTreeItem.getData()).add(newDatFile);
                                        TreeItem ti = new TreeItem(targetTreeItem, SWT.NONE);
                                        ti.setText(new File(df.getNewName()).getName());
                                        ti.setData(newDatFile);
                                    }

                                    if (refResult != null) {
                                        // Remove old data
                                        for(int i = 0; i < 5; i++) {
                                            ArrayList<DatFile> toRemove = refResult.get(i);
                                            for (DatFile datToRemove : toRemove) {
                                                datToRemove.disposeData();
                                                updateTree_removeEntry(datToRemove);
                                            }
                                        }
                                        // Create new data
                                        TreeItem[] targetTrees = new TreeItem[]{treeItem_UnofficialParts[0], treeItem_UnofficialSubparts[0], treeItem_UnofficialPrimitives[0], treeItem_UnofficialPrimitives48[0], treeItem_UnofficialPrimitives8[0]};
                                        for(int i = 5; i < 10; i++) {
                                            ArrayList<DatFile> toCreate = refResult.get(i);
                                            for (DatFile datToCreate : toCreate) {
                                                DatFile newDatFile = new DatFile(datToCreate.getOldName());
                                                String source = datToCreate.getTextDirect();
                                                newDatFile.setText(source);
                                                newDatFile.setOriginalText(source);
                                                newDatFile.saveForced();
                                                newDatFile.setType(datToCreate.getType());
                                                ((ArrayList<DatFile>) targetTrees[i - 5].getData()).add(newDatFile);
                                                TreeItem ti = new TreeItem(targetTrees[i - 5], SWT.NONE);
                                                ti.setText(new File(datToCreate.getOldName()).getName());
                                                ti.setData(newDatFile);
                                            }
                                        }

                                    }

                                    updateTree_unsavedEntries();
                                }
                            } else {
                                MessageBox messageBoxError = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
                                messageBoxError.setText(I18n.DIALOG_UnavailableTitle);
                                messageBoxError.setMessage(I18n.DIALOG_Unavailable);
                                messageBoxError.open();
                            }
                        }
                    });

                    java.awt.Point b = java.awt.MouseInfo.getPointerInfo().getLocation();
                    final int x = (int) b.getX();
                    final int y = (int) b.getY();

                    Menu menu = mnu_treeMenu[0];
                    menu.setLocation(x, y);
                    menu.setVisible(true);
                }
            }
        });

        treeParts[0].addListener(SWT.MouseDoubleClick, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (treeParts[0].getSelectionCount() == 1 && treeParts[0].getSelection()[0] != null) {
                    treeParts[0].getSelection()[0].setVisible(!treeParts[0].getSelection()[0].isVisible());
                    TreeItem sel = treeParts[0].getSelection()[0];
                    sh.getDisplay().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            treeParts[0].build();
                        }
                    });
                    treeParts[0].redraw();
                    treeParts[0].update();
                    treeParts[0].getTree().select(treeParts[0].getMapInv().get(sel));
                }
            }
        });
        txt_Search[0].addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                search(txt_Search[0].getText());
            }
        });
        btn_ResetSearch[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                txt_Search[0].setText(""); //$NON-NLS-1$
            }
        });
        txt_primitiveSearch[0].addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                getCompositePrimitive().collapseAll();
                ArrayList<Primitive> prims = getCompositePrimitive().getPrimitives();
                final String crit = txt_primitiveSearch[0].getText();
                if (crit.trim().isEmpty()) {
                    getCompositePrimitive().setSearchResults(new ArrayList<Primitive>());
                    Matrix4f.setIdentity(getCompositePrimitive().getTranslation());
                    getCompositePrimitive().getOpenGL().drawScene(-1, -1);
                    return;
                }
                String criteria = ".*" + crit + ".*"; //$NON-NLS-1$ //$NON-NLS-2$
                try {
                    "DUMMY".matches(criteria); //$NON-NLS-1$
                } catch (PatternSyntaxException pe) {
                    getCompositePrimitive().setSearchResults(new ArrayList<Primitive>());
                    Matrix4f.setIdentity(getCompositePrimitive().getTranslation());
                    getCompositePrimitive().getOpenGL().drawScene(-1, -1);
                    return;
                }
                final Pattern pattern = Pattern.compile(criteria);
                ArrayList<Primitive> results = new ArrayList<Primitive>();
                for (Primitive p : prims) {
                    p.search(pattern, results);
                }
                if (results.isEmpty()) {
                    results.add(null);
                }
                getCompositePrimitive().setSearchResults(results);
                Matrix4f.setIdentity(getCompositePrimitive().getTranslation());
                getCompositePrimitive().getOpenGL().drawScene(-1, -1);
            }
        });
        btn_resetPrimitiveSearch[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                txt_primitiveSearch[0].setText(""); //$NON-NLS-1$
            }
        });
        btn_Hide[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null) Project.getFileToEdit().getVertexManager().hideSelection();
            }
        });
        btn_ShowAll[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null) Project.getFileToEdit().getVertexManager().showAll();
            }
        });
        btn_NoTransparentSelection[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setNoTransparentSelection(btn_NoTransparentSelection[0].getSelection());
            }
        });
        btn_BFCToggle[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setBfcToggle(btn_BFCToggle[0].getSelection());
            }
        });

        btn_Delete[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null) Project.getFileToEdit().getVertexManager().delete(Editor3DWindow.getWindow().isMovingAdjacentData(), true);
            }
        });
        btn_Copy[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null) Project.getFileToEdit().getVertexManager().copy();
            }
        });
        btn_Cut[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null) {
                    Project.getFileToEdit().getVertexManager().copy();
                    Project.getFileToEdit().getVertexManager().delete(false, true);
                }
            }
        });
        btn_Paste[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null) {
                    Project.getFileToEdit().getVertexManager().paste();
                    setMovingAdjacentData(false);
                }
            }
        });

        btn_Manipulator_0_toOrigin[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null) {
                    for (OpenGLRenderer renderer : renders) {
                        Composite3D c3d = renderer.getC3D();
                        if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                            c3d.getManipulator().reset();
                        }
                    }
                }
            }
        });

        btn_Manipulator_XIII_toWorld[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null) {
                    for (OpenGLRenderer renderer : renders) {
                        Composite3D c3d = renderer.getC3D();
                        if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                            Vector4f t = new Vector4f(c3d.getManipulator().getPosition());
                            BigDecimal[] T = c3d.getManipulator().getAccuratePosition();
                            c3d.getManipulator().reset();
                            c3d.getManipulator().getPosition().set(t);
                            c3d.getManipulator().setAccuratePosition(T[0], T[1], T[2]);
                            ;
                        }
                    }
                }
            }
        });

        btn_Manipulator_X_XReverse[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        Vector4f.sub(new Vector4f(0f, 0f, 0f, 2f), c3d.getManipulator().getXaxis(), c3d.getManipulator().getXaxis());
                        BigDecimal[] a = c3d.getManipulator().getAccurateXaxis();
                        c3d.getManipulator().setAccurateXaxis(a[0].negate(), a[1].negate(), a[2].negate());
                    }
                }
            }
        });

        btn_Manipulator_XI_YReverse[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        Vector4f.sub(new Vector4f(0f, 0f, 0f, 2f), c3d.getManipulator().getYaxis(), c3d.getManipulator().getYaxis());
                        BigDecimal[] a = c3d.getManipulator().getAccurateYaxis();
                        c3d.getManipulator().setAccurateYaxis(a[0].negate(), a[1].negate(), a[2].negate());
                    }
                }
            }
        });

        btn_Manipulator_XII_ZReverse[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        Vector4f.sub(new Vector4f(0f, 0f, 0f, 2f), c3d.getManipulator().getZaxis(), c3d.getManipulator().getZaxis());
                        BigDecimal[] a = c3d.getManipulator().getAccurateZaxis();
                        c3d.getManipulator().setAccurateZaxis(a[0].negate(), a[1].negate(), a[2].negate());
                    }
                }
            }
        });

        btn_Manipulator_SwitchXY[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
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
            }
        });

        btn_Manipulator_SwitchXZ[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
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
            }
        });

        btn_Manipulator_SwitchYZ[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
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
            }
        });

        if (NLogger.DEBUG)
            btn_Manipulator_1_cameraToPos[0].addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    for (OpenGLRenderer renderer : renders) {
                        Composite3D c3d = renderer.getC3D();
                        Vector4f pos = c3d.getManipulator().getPosition();
                        Vector4f a1 = c3d.getManipulator().getXaxis();
                        Vector4f a2 = c3d.getManipulator().getYaxis();
                        Vector4f a3 = c3d.getManipulator().getZaxis();
                        if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
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
                }
            });
        btn_Manipulator_2_toAverage[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
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
            }
        });

        btn_Manipulator_3_toSubfile[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null) {
                    Set<GData1> subfiles = Project.getFileToEdit().getVertexManager().getSelectedSubfiles();
                    if (!subfiles.isEmpty()) {
                        GData1 subfile = null;
                        for (GData1 g1 : subfiles) {
                            subfile = g1;
                            break;
                        }
                        Matrix4f m = subfile.getProductMatrix();
                        Matrix M = subfile.getAccurateProductMatrix();
                        for (OpenGLRenderer renderer : renders) {
                            Composite3D c3d = renderer.getC3D();
                            if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                                c3d.getManipulator().getPosition().set(m.m30, m.m31, m.m32, 1f);
                                c3d.getManipulator().setAccuratePosition(M.M30, M.M31, M.M32);
                                Vector3f x = new Vector3f(m.m00, m.m01, m.m02);
                                x.normalise();
                                Vector3f y = new Vector3f(m.m10, m.m11, m.m12);
                                y.normalise();
                                Vector3f z = new Vector3f(m.m20, m.m21, m.m22);
                                z.normalise();
                                c3d.getManipulator().getXaxis().set(x.x, x.y, x.z, 1f);
                                c3d.getManipulator().getYaxis().set(y.x, y.y, y.z, 1f);
                                c3d.getManipulator().getZaxis().set(z.x, z.y, z.z, 1f);
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
            }
        });

        btn_Manipulator_32_subfileTo[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
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
                                Manipulator ma = c3d.getManipulator();
                                vm.transformSubfile(subfile, ma.getAccurateMatrix(), true, true);
                            }
                        }
                    }
                }
            }
        });

        btn_Manipulator_4_toVertex[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
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
                        c3d.getManipulator().setAccuratePosition(minVertex.X, minVertex.Y, minVertex.Z);
                    }
                }
            }
        });

        btn_Manipulator_5_toEdge[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        Vector4f min = new Vector4f(c3d.getManipulator().getPosition());
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        min = vm.getMinimalDistanceVertexToLines(new Vertex(c3d.getManipulator().getPosition())).toVector4f();
                        c3d.getManipulator().getPosition().set(min.x, min.y, min.z, 1f);
                        c3d.getManipulator().setAccuratePosition(new BigDecimal(min.x / 1000f), new BigDecimal(min.y / 1000f), new BigDecimal(min.z / 1000f));
                    }
                }
            }
        });

        btn_Manipulator_6_toSurface[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        Vector4f min = new Vector4f(c3d.getManipulator().getPosition());
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        min = vm.getMinimalDistanceVertexToSurfaces(new Vertex(c3d.getManipulator().getPosition())).toVector4f();
                        c3d.getManipulator().getPosition().set(min.x, min.y, min.z, 1f);
                        c3d.getManipulator().setAccuratePosition(new BigDecimal(min.x / 1000f), new BigDecimal(min.y / 1000f), new BigDecimal(min.z / 1000f));
                    }
                }
            }
        });

        btn_Manipulator_7_toVertexNormal[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
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
                        float ty = 0f;
                        float tz = 0f;

                        if (n.x <= 0f) {
                            tx = -1;
                        }

                        if (Math.abs(Vector3f.cross(new Vector3f(n.x, n.y, n.z), new Vector3f(0f, 0f, tx), null).length()) > .00001f) {
                            tz = tx;
                            tx = 0f;
                            ty = 0f;
                        } else if (Math.abs(Vector3f.cross(new Vector3f(n.x, n.y, n.z), new Vector3f(tx, 0f, 0f), null).length()) > .00001f) {
                            // ty = 0f;
                            // tz = 0f;
                        } else if (Math.abs(Vector3f.cross(new Vector3f(n.x, n.y, n.z), new Vector3f(0f, 0f, tx), null).length()) > .00001f) {
                            ty = tx;
                            tx = 0f;
                            tz = 0f;
                        } else {
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
        });

        btn_Manipulator_8_toEdgeNormal[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        Vector4f n = vm.getMinimalDistanceEdgeNormal(new Vertex(c3d.getManipulator().getPosition()));

                        float tx = 1f;
                        float ty = 0f;
                        float tz = 0f;

                        if (n.x <= 0f) {
                            tx = -1;
                        }

                        if (Math.abs(Vector3f.cross(new Vector3f(n.x, n.y, n.z), new Vector3f(0f, 0f, tx), null).length()) > .00001f) {
                            tz = tx;
                            tx = 0f;
                            ty = 0f;
                        } else if (Math.abs(Vector3f.cross(new Vector3f(n.x, n.y, n.z), new Vector3f(tx, 0f, 0f), null).length()) > .00001f) {
                            // ty = 0f;
                            // tz = 0f;
                        } else if (Math.abs(Vector3f.cross(new Vector3f(n.x, n.y, n.z), new Vector3f(0f, 0f, tx), null).length()) > .00001f) {
                            ty = tx;
                            tx = 0f;
                            tz = 0f;
                        } else {
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
        });

        btn_Manipulator_9_toSurfaceNormal[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        Vector4f n = vm.getMinimalDistanceSurfaceNormal(new Vertex(c3d.getManipulator().getPosition()));

                        float tx = 1f;
                        float ty = 0f;
                        float tz = 0f;

                        if (n.x <= 0f) {
                            tx = -1;
                        }

                        if (Math.abs(Vector3f.cross(new Vector3f(n.x, n.y, n.z), new Vector3f(0f, 0f, tx), null).length()) > .00001f) {
                            tz = tx;
                            tx = 0f;
                            ty = 0f;
                        } else if (Math.abs(Vector3f.cross(new Vector3f(n.x, n.y, n.z), new Vector3f(tx, 0f, 0f), null).length()) > .00001f) {
                            // ty = 0f;
                            // tz = 0f;
                        } else if (Math.abs(Vector3f.cross(new Vector3f(n.x, n.y, n.z), new Vector3f(0f, 0f, tx), null).length()) > .00001f) {
                            ty = tx;
                            tx = 0f;
                            tz = 0f;
                        } else {
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
        });

        btn_Manipulator_XIV_adjustRotationCenter[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        vm.adjustRotationCenter(c3d, null);
                    }
                }
            }
        });

        mntm_SelectAll[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        loadSelectorSettings();
                        vm.selectAll(sels, true);
                        vm.syncWithTextEditors(true);
                        return;
                    }
                }
            }
        });
        mntm_SelectAllVisible[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        loadSelectorSettings();
                        vm.selectAll(sels, false);
                        vm.syncWithTextEditors(true);
                        return;
                    }
                }
            }
        });
        mntm_SelectAllWithColours[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        loadSelectorSettings();
                        vm.selectAllWithSameColours(sels, true);
                        vm.syncWithTextEditors(true);
                        return;
                    }
                }
            }
        });
        mntm_SelectAllVisibleWithColours[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        loadSelectorSettings();
                        vm.selectAllWithSameColours(sels, false);
                        vm.syncWithTextEditors(true);
                        return;
                    }
                }
            }
        });
        mntm_SelectNone[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        vm.clearSelection();
                        vm.syncWithTextEditors(true);
                        return;
                    }
                }
            }
        });
        mntm_SelectInverse[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        loadSelectorSettings();
                        vm.selectInverse(sels);
                        vm.syncWithTextEditors(true);
                        return;
                    }
                }
            }
        });

        mntm_WithSameColour[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Display.getCurrent().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        mntm_SelectEverything[0].setEnabled(
                                mntm_WithHiddenData[0].getSelection() ||
                                mntm_WithSameColour[0].getSelection() ||
                                mntm_WithSameOrientation[0].getSelection() ||
                                mntm_ExceptSubfiles[0].getSelection()
                                );
                        showSelectMenu();
                    }
                });
            }
        });

        mntm_WithSameOrientation[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Display.getCurrent().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        mntm_SelectEverything[0].setEnabled(
                                mntm_WithHiddenData[0].getSelection() ||
                                mntm_WithSameColour[0].getSelection() ||
                                mntm_WithSameOrientation[0].getSelection() ||
                                mntm_ExceptSubfiles[0].getSelection()
                                );
                        if (mntm_WithSameOrientation[0].getSelection()) {


                            new ValueDialog(getShell(), "Set angular surface normal difference:", "Threshold in degree [°], range from -90 to 180.\nNegative values do not care about the surface winding,\nwhile positive do.") { //$NON-NLS-1$ //$NON-NLS-2$ I18N

                                @Override
                                public void initializeSpinner() {
                                    this.spn_Value[0].setMinimum(new BigDecimal("-90")); //$NON-NLS-1$
                                    this.spn_Value[0].setMaximum(new BigDecimal("180")); //$NON-NLS-1$
                                    this.spn_Value[0].setValue(sels.getAngle());
                                }

                                @Override
                                public void applyValue() {
                                    sels.setAngle(this.spn_Value[0].getValue());
                                }
                            }.open();
                        }
                        showSelectMenu();
                    }
                });
            }
        });

        mntm_WithAccuracy[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Display.getCurrent().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        mntm_SelectEverything[0].setEnabled(
                                mntm_WithHiddenData[0].getSelection() ||
                                mntm_WithSameColour[0].getSelection() ||
                                mntm_WithSameOrientation[0].getSelection() ||
                                mntm_ExceptSubfiles[0].getSelection()
                                );
                        if (mntm_WithAccuracy[0].getSelection()) {

                            new ValueDialog(getShell(), "Set accuracy:", "Threshold in LDU, range from 0 to 1000.\nControls the maximum distance between two points that the process will consider matching") { //$NON-NLS-1$ //$NON-NLS-2$ I18N

                                @Override
                                public void initializeSpinner() {
                                    this.spn_Value[0].setMinimum(new BigDecimal("0")); //$NON-NLS-1$
                                    this.spn_Value[0].setMaximum(new BigDecimal("1000")); //$NON-NLS-1$
                                    this.spn_Value[0].setValue(sels.getEqualDistance());
                                }

                                @Override
                                public void applyValue() {
                                    sels.setEqualDistance(this.spn_Value[0].getValue());
                                }
                            }.open();
                        }
                        showSelectMenu();
                    }
                });
            }
        });
        mntm_WithHiddenData[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Display.getCurrent().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        mntm_SelectEverything[0].setEnabled(
                                mntm_WithHiddenData[0].getSelection() ||
                                mntm_WithSameColour[0].getSelection() ||
                                mntm_WithSameOrientation[0].getSelection() ||
                                mntm_ExceptSubfiles[0].getSelection()
                                );
                        showSelectMenu();
                    }
                });
            }
        });
        mntm_WithWholeSubfiles[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Display.getCurrent().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        showSelectMenu();
                    }
                });
            }
        });
        mntm_ExceptSubfiles[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Display.getCurrent().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        mntm_SelectEverything[0].setEnabled(
                                mntm_WithHiddenData[0].getSelection() ||
                                mntm_WithSameColour[0].getSelection() ||
                                mntm_WithSameOrientation[0].getSelection() ||
                                mntm_ExceptSubfiles[0].getSelection()
                                );
                        mntm_WithWholeSubfiles[0].setEnabled(!mntm_ExceptSubfiles[0].getSelection());
                        showSelectMenu();
                    }
                });
            }
        });
        mntm_StopAtEdges[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Display.getCurrent().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        showSelectMenu();
                    }
                });
            }
        });
        mntm_STriangles[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Display.getCurrent().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        showSelectMenu();
                    }
                });
            }
        });
        mntm_SQuads[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Display.getCurrent().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        showSelectMenu();
                    }
                });
            }
        });
        mntm_SCLines[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Display.getCurrent().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        showSelectMenu();
                    }
                });
            }
        });
        mntm_SVertices[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Display.getCurrent().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        showSelectMenu();
                    }
                });
            }
        });
        mntm_SLines[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Display.getCurrent().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        showSelectMenu();
                    }
                });
            }
        });

        mntm_SelectEverything[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        sels.setScope(SelectorSettings.EVERYTHING);
                        loadSelectorSettings();
                        vm.selector(sels);
                        vm.syncWithTextEditors(true);
                    }
                }
            }
        });

        mntm_SelectConnected[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        sels.setScope(SelectorSettings.CONNECTED);
                        loadSelectorSettings();
                        vm.selector(sels);
                        vm.syncWithTextEditors(true);
                    }
                }
            }
        });

        mntm_SelectTouching[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        sels.setScope(SelectorSettings.TOUCHING);
                        loadSelectorSettings();
                        vm.selector(sels);
                        vm.syncWithTextEditors(true);
                    }
                }
            }
        });

        mntm_SelectIsolatedVertices[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Display.getCurrent().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        for (OpenGLRenderer renderer : renders) {
                            Composite3D c3d = renderer.getC3D();
                            if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                                VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                                vm.selectIsolatedVertices();
                                vm.syncWithTextEditors(true);
                            }
                        }
                    }
                });
            }
        });

        mntm_Split[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Display.getCurrent().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        for (OpenGLRenderer renderer : renders) {
                            Composite3D c3d = renderer.getC3D();
                            if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                                VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                                vm.split(2);
                            }
                        }
                    }
                });
            }
        });

        mntm_SplitNTimes[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Display.getCurrent().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        for (OpenGLRenderer renderer : renders) {
                            Composite3D c3d = renderer.getC3D();
                            if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {

                                final int[] frac = new int[]{2};
                                if (new ValueDialogInt(getShell(), "Split edges:", "(Number of resulting fractions)") { //$NON-NLS-1$ //$NON-NLS-2$ I18N

                                    @Override
                                    public void initializeSpinner() {
                                        this.spn_Value[0].setMinimum(2);
                                        this.spn_Value[0].setMaximum(1000);
                                        this.spn_Value[0].setValue(2);
                                    }

                                    @Override
                                    public void applyValue() {
                                        frac[0] = this.spn_Value[0].getValue();
                                    }
                                }.open() == OK) {

                                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();

                                    vm.split(frac[0]);
                                }
                            }
                        }
                    }
                });
            }
        });

        mntm_MergeToAverage[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        vm.merge(MergeTo.AVERAGE, true);
                        return;
                    }
                }
            }
        });
        mntm_MergeToLastSelected[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        vm.merge(MergeTo.LAST_SELECTED, true);
                        return;
                    }
                }
            }
        });
        mntm_MergeToNearestVertex[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        vm.merge(MergeTo.NEAREST_VERTEX, true);
                        return;
                    }
                }
            }
        });
        mntm_MergeToNearestEdge[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        vm.merge(MergeTo.NEAREST_EDGE, true);
                        return;
                    }
                }
            }
        });
        mntm_MergeToNearestFace[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        vm.merge(MergeTo.NEAREST_FACE, true);
                        return;
                    }
                }
            }
        });

        mntm_setXYZ[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        Vertex v = null;
                        final VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        final Set<Vertex> sv = vm.getSelectedVertices();
                        if (sv.size() == 1) {
                            v = sv.iterator().next();
                        }
                        if (new CoordinatesDialog(getShell(), v).open() == IDialogConstants.OK_ID) {
                            vm.setXyzOrTranslateOrTransform(CoordinatesDialog.getVertex(), null, TransformationMode.SET, CoordinatesDialog.isX(), CoordinatesDialog.isY(), CoordinatesDialog.isZ(), isMovingAdjacentData(), true);
                        }
                        return;
                    }
                }
            }
        });

        mntm_Translate[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        if (new TranslateDialog(getShell(), null).open() == IDialogConstants.OK_ID) {
                            c3d.getLockableDatFileReference().getVertexManager().setXyzOrTranslateOrTransform(TranslateDialog.getOffset(), null, TransformationMode.TRANSLATE, TranslateDialog.isX(), TranslateDialog.isY(), TranslateDialog.isZ(), isMovingAdjacentData(), true);
                        }
                        return;
                    }
                }
            }
        });

        mntm_Rotate[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        TreeSet<Vertex> clipboard = new TreeSet<Vertex>();
                        if (VertexManager.getClipboard().size() == 1) {
                            GData vertex = VertexManager.getClipboard().get(0);
                            if (vertex.type() == 0) {
                                String line = vertex.toString();
                                line = line.replaceAll("\\s+", " ").trim(); //$NON-NLS-1$ //$NON-NLS-2$
                                String[] data_segments = line.split("\\s+"); //$NON-NLS-1$
                                if (line.startsWith("0 !LPE")) { //$NON-NLS-1$
                                    if (line.startsWith("VERTEX ", 7)) { //$NON-NLS-1$
                                        Vector3d start = new Vector3d();
                                        boolean numberError = false;
                                        if (data_segments.length == 6) {
                                            try {
                                                start.setX(new BigDecimal(data_segments[3], Threshold.mc));
                                            } catch (NumberFormatException nfe) {
                                                numberError = true;
                                            }
                                            try {
                                                start.setY(new BigDecimal(data_segments[4], Threshold.mc));
                                            } catch (NumberFormatException nfe) {
                                                numberError = true;
                                            }
                                            try {
                                                start.setZ(new BigDecimal(data_segments[5], Threshold.mc));
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
                        }
                        if (new RotateDialog(getShell(), null, clipboard).open() == IDialogConstants.OK_ID) {
                            c3d.getLockableDatFileReference().getVertexManager().setXyzOrTranslateOrTransform(RotateDialog.getAngles(), RotateDialog.getPivot(), TransformationMode.ROTATE, RotateDialog.isX(), RotateDialog.isY(), TranslateDialog.isZ(), isMovingAdjacentData(), true);
                        }
                        return;
                    }
                }
            }
        });

        mntm_Scale[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        TreeSet<Vertex> clipboard = new TreeSet<Vertex>();
                        if (VertexManager.getClipboard().size() == 1) {
                            GData vertex = VertexManager.getClipboard().get(0);
                            if (vertex.type() == 0) {
                                String line = vertex.toString();
                                line = line.replaceAll("\\s+", " ").trim(); //$NON-NLS-1$ //$NON-NLS-2$
                                String[] data_segments = line.split("\\s+"); //$NON-NLS-1$
                                if (line.startsWith("0 !LPE")) { //$NON-NLS-1$
                                    if (line.startsWith("VERTEX ", 7)) { //$NON-NLS-1$
                                        Vector3d start = new Vector3d();
                                        boolean numberError = false;
                                        if (data_segments.length == 6) {
                                            try {
                                                start.setX(new BigDecimal(data_segments[3], Threshold.mc));
                                            } catch (NumberFormatException nfe) {
                                                numberError = true;
                                            }
                                            try {
                                                start.setY(new BigDecimal(data_segments[4], Threshold.mc));
                                            } catch (NumberFormatException nfe) {
                                                numberError = true;
                                            }
                                            try {
                                                start.setZ(new BigDecimal(data_segments[5], Threshold.mc));
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
                        }
                        if (new ScaleDialog(getShell(), null, clipboard).open() == IDialogConstants.OK_ID) {
                            c3d.getLockableDatFileReference().getVertexManager().setXyzOrTranslateOrTransform(ScaleDialog.getScaleFactors(), ScaleDialog.getPivot(), TransformationMode.SCALE, ScaleDialog.isX(), ScaleDialog.isY(), ScaleDialog.isZ(), isMovingAdjacentData(), true);
                        }
                        return;
                    }
                }
            }
        });



        mntm_Edger2[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        if (new EdgerDialog(getShell(), es).open() == IDialogConstants.OK_ID)
                            vm.addEdges(es);
                        return;

                    }
                }
            }
        });

        mntm_Rectifier[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        if (new RectifierDialog(getShell(), rs).open() == IDialogConstants.OK_ID)
                            vm.rectify(rs, true);
                        return;

                    }
                }
            }
        });

        mntm_Isecalc[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        if (new IsecalcDialog(getShell(), is).open() == IDialogConstants.OK_ID)
                            vm.isecalc(is);
                        return;

                    }
                }
            }
        });

        mntm_SlicerPro[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        if (new SlicerProDialog(getShell(), ss).open() == IDialogConstants.OK_ID)
                            vm.slicerpro(ss);
                        return;

                    }
                }
            }
        });

        mntm_Intersector[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        if (new IntersectorDialog(getShell(), ins).open() == IDialogConstants.OK_ID)
                            vm.intersector(ins, true);
                        return;

                    }
                }
            }
        });

        mntm_Lines2Pattern[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        if (new Lines2PatternDialog(getShell()).open() == IDialogConstants.OK_ID)
                            vm.lines2pattern();
                        return;
                    }
                }
            }
        });

        mntm_PathTruder[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        if (new PathTruderDialog(getShell(), ps).open() == IDialogConstants.OK_ID)
                            vm.pathTruder(ps);
                        return;
                    }
                }
            }
        });

        mntm_SymSplitter[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        if (new SymSplitterDialog(getShell(), sims).open() == IDialogConstants.OK_ID)
                            vm.symSplitter(sims);
                        return;
                    }
                }
            }
        });

        mntm_Unificator[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        if (new UnificatorDialog(getShell(), us).open() == IDialogConstants.OK_ID)
                            vm.unificator(us);
                        return;
                    }
                }
            }
        });

        mntm_RingsAndCones[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        if (new RingsAndConesDialog(getShell(), ris).open() == IDialogConstants.OK_ID) {
                            RingsAndCones.solve(Editor3DWindow.getWindow().getShell(), c3d.getLockableDatFileReference(), cmp_Primitives[0].getPrimitives(), ris, true);
                        }
                    }
                }
            }
        });

        mntm_Txt2Dat[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        DatFile df = c3d.getLockableDatFileReference();
                        if (df.isReadOnly()) return;
                        VertexManager vm = df.getVertexManager();
                        if (new Txt2DatDialog(getShell(), ts).open() == IDialogConstants.OK_ID && !ts.getText().trim().isEmpty()) {

                            java.awt.Font myFont;

                            if (ts.getFontData() == null) {
                                myFont = new java.awt.Font(org.nschmidt.ldparteditor.enums.Font.MONOSPACE.getFontData()[0].getName(), java.awt.Font.PLAIN, 32);
                            } else {
                                FontData fd = ts.getFontData();
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
                                }
                                myFont = new java.awt.Font(fd.getName(), style, fd.getHeight());
                            }
                            GData anchorData = df.getDrawChainTail();
                            int lineNumber = df.getDrawPerLine_NOCLONE().getKey(anchorData);
                            Set<GData> triangleSet = TextTriangulator.triangulateText(myFont, ts.getText().trim(), ts.getFlatness().doubleValue(), ts.getInterpolateFlatness().doubleValue(), View.DUMMY_REFERENCE, df, ts.getFontHeight().intValue(), ts.getDeltaAngle().doubleValue());
                            for (GData gda3 : triangleSet) {
                                lineNumber++;
                                df.getDrawPerLine_NOCLONE().put(lineNumber, gda3);
                                GData gdata = gda3;
                                anchorData.setNext(gda3);
                                anchorData = gdata;
                            }
                            anchorData.setNext(null);
                            df.setDrawChainTail(anchorData);
                            vm.setModified(true, true);
                            return;
                        }
                    }
                }
            }
        });

        // MARK Options

        mntm_ResetSettingsOnRestart[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
                messageBox.setText("Warning:"); //$NON-NLS-1$ I18N
                messageBox.setMessage("Are you sure to delete your configuration on the next start?"); //$NON-NLS-1$
                int result = messageBox.open();
                if (result == SWT.CANCEL) {
                    return;
                }
                WorkbenchManager.getUserSettingState().setResetOnStart(true);
            }
        });

        mntm_SelectAnotherLDConfig[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog fd = new FileDialog(sh, SWT.OPEN);
                fd.setText("Open LDraw Configuration File (LDConfig.ldr):"); //$NON-NLS-1$ I18N Needs translation!
                fd.setFilterPath(WorkbenchManager.getUserSettingState().getLdrawFolderPath());

                String[] filterExt = { "*.ldr", "LDConfig.ldr", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                fd.setFilterExtensions(filterExt);
                String[] filterNames = { "LDraw Configuration File (*.ldr)", "LDraw Configuration File (LDConfig.ldr)", "All Files" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ I18N Needs translation!
                fd.setFilterNames(filterNames);

                String selected = fd.open();
                System.out.println(selected);

                if (selected != null && View.loadLDConfig(selected)) {
                    GData.CACHE_warningsAndErrors.clear();
                    WorkbenchManager.getUserSettingState().setLdConfigPath(selected);
                    Set<DatFile> dfs = new HashSet<DatFile>();
                    for (OpenGLRenderer renderer : renders) {
                        dfs.add(renderer.getC3D().getLockableDatFileReference());
                    }
                    for (DatFile df : dfs) {
                        SubfileCompiler.compile(df, false, false);
                    }
                }

            }
        });

        mntm_SyncWithTextEditor[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                WorkbenchManager.getUserSettingState().getSyncWithTextEditor().set(mntm_SyncWithTextEditor[0].getSelection());
                mntm_SyncLpeInline[0].setEnabled(mntm_SyncWithTextEditor[0].getSelection());
            }
        });

        mntm_SyncLpeInline[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                WorkbenchManager.getUserSettingState().getSyncWithLpeInline().set(mntm_SyncLpeInline[0].getSelection());
            }
        });

        // MARK Merge, split...

        mntm_Flip[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        vm.flipSelection();
                        return;
                    }
                }
            }
        });

        mntm_SubdivideCatmullClark[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        vm.subdivideCatmullClark();
                        return;
                    }
                }
            }
        });

        mntm_SubdivideLoop[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        vm.subdivideLoop();
                        return;
                    }
                }
            }
        });

        // MARK Background PNG
        btn_PngFocus[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
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

                if (c3d == null) return;

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
                return;
            }
        });
        btn_PngImage[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
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

                        FileDialog fd = new FileDialog(getShell(), SWT.SAVE);
                        fd.setText("Open PNG Image"); //$NON-NLS-1$ I18N Needs translation!
                        try {
                            File f = new File(png.texturePath);
                            fd.setFilterPath(f.getParent());
                            fd.setFileName(f.getName());
                        } catch (Exception ex) {

                        }

                        String[] filterExt = { "*.png", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
                        fd.setFilterExtensions(filterExt);
                        String[] filterNames = { "Portable Network Graphics (*.png)", "All Files" }; //$NON-NLS-1$ //$NON-NLS-2$ I18N Needs translation!
                        fd.setFilterNames(filterNames);
                        String texturePath = fd.open();

                        if (texturePath != null) {

                            String newText = png.getString(png.offset, png.angleA, png.angleB, png.angleC, png.scale, texturePath);

                            GDataPNG newPngPicture = new GDataPNG(newText, png.offset, png.angleA, png.angleB, png.angleC, png.scale, texturePath);
                            replaceBgPicture(png, newPngPicture, c3d.getLockableDatFileReference());

                            pngPictureUpdateCounter++;
                            if (pngPictureUpdateCounter > 3) {
                                for (OpenGLRenderer renderer2 : renders) {
                                    renderer2.disposeOldTextures();
                                }
                                pngPictureUpdateCounter = 0;
                            }

                            vm.setModified(true, true);
                            vm.setSelectedBgPicture(c3d.getLockableDatFileReference().getBackgroundPicture(vm.getSelectedBgPictureIndex()));
                        }

                        return;
                    }
                }
            }
        });
        btn_PngNext[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    DatFile df = c3d.getLockableDatFileReference();
                    if (df.equals(Project.getFileToEdit()) && !Project.getFileToEdit().isReadOnly()) {
                        VertexManager vm = df.getVertexManager();
                        GDataPNG sp = vm.getSelectedBgPicture();
                        boolean noBgPictures = df.hasNoBackgroundPictures();
                        vm.setSelectedBgPictureIndex(vm.getSelectedBgPictureIndex() + 1);
                        boolean indexOutOfBounds = vm.getSelectedBgPictureIndex() >= df.getBackgroundPictureCount();
                        boolean noRealData = df.getDrawPerLine_NOCLONE().getKey(sp) == null;
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
                    }
                }
            }
        });
        btn_PngPrevious[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    DatFile df = c3d.getLockableDatFileReference();
                    if (df.equals(Project.getFileToEdit()) && !Project.getFileToEdit().isReadOnly()) {
                        VertexManager vm = df.getVertexManager();
                        GDataPNG sp = vm.getSelectedBgPicture();
                        boolean noBgPictures = df.hasNoBackgroundPictures();
                        vm.setSelectedBgPictureIndex(vm.getSelectedBgPictureIndex() - 1);
                        boolean indexOutOfBounds = vm.getSelectedBgPictureIndex() < 0;
                        boolean noRealData = df.getDrawPerLine_NOCLONE().getKey(sp) == null;
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
                    }
                }
            }
        });
        spn_PngA1[0].addValueChangeListener(new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
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



                        String newText = png.getString(png.offset, spn.getValue(), png.angleB, png.angleC, png.scale, png.texturePath);

                        GDataPNG newPngPicture = new GDataPNG(newText, png.offset, spn.getValue(), png.angleB, png.angleC, png.scale, png.texturePath);
                        replaceBgPicture(png, newPngPicture, c3d.getLockableDatFileReference());

                        pngPictureUpdateCounter++;
                        if (pngPictureUpdateCounter > 3) {
                            for (OpenGLRenderer renderer2 : renders) {
                                renderer2.disposeOldTextures();
                            }
                            pngPictureUpdateCounter = 0;
                        }

                        vm.setModified(true, true);
                        vm.setSelectedBgPicture(c3d.getLockableDatFileReference().getBackgroundPicture(vm.getSelectedBgPictureIndex()));
                        return;
                    }
                }
            }
        });
        spn_PngA2[0].addValueChangeListener(new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
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

                        String newText = png.getString(png.offset, png.angleA, spn.getValue(), png.angleC, png.scale, png.texturePath);

                        GDataPNG newPngPicture = new GDataPNG(newText, png.offset, png.angleA, spn.getValue(), png.angleC, png.scale, png.texturePath);
                        replaceBgPicture(png, newPngPicture, c3d.getLockableDatFileReference());

                        pngPictureUpdateCounter++;
                        if (pngPictureUpdateCounter > 3) {
                            for (OpenGLRenderer renderer2 : renders) {
                                renderer2.disposeOldTextures();
                            }
                            pngPictureUpdateCounter = 0;
                        }

                        vm.setModified(true, true);
                        vm.setSelectedBgPicture(c3d.getLockableDatFileReference().getBackgroundPicture(vm.getSelectedBgPictureIndex()));
                        return;
                    }
                }
            }
        });
        spn_PngA3[0].addValueChangeListener(new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
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

                        String newText = png.getString(png.offset, png.angleA, png.angleB, spn.getValue(), png.scale, png.texturePath);

                        GDataPNG newPngPicture = new GDataPNG(newText, png.offset, png.angleA, png.angleB, spn.getValue(), png.scale, png.texturePath);
                        replaceBgPicture(png, newPngPicture, c3d.getLockableDatFileReference());

                        pngPictureUpdateCounter++;
                        if (pngPictureUpdateCounter > 3) {
                            for (OpenGLRenderer renderer2 : renders) {
                                renderer2.disposeOldTextures();
                            }
                            pngPictureUpdateCounter = 0;
                        }

                        vm.setModified(true, true);
                        vm.setSelectedBgPicture(c3d.getLockableDatFileReference().getBackgroundPicture(vm.getSelectedBgPictureIndex()));
                        return;
                    }
                }
            }
        });
        spn_PngSX[0].addValueChangeListener(new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
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

                        Vertex newScale = new Vertex(spn.getValue(), png.scale.Y, png.scale.Z);
                        String newText = png.getString(png.offset, png.angleA, png.angleB, png.angleC, newScale, png.texturePath);

                        GDataPNG newPngPicture = new GDataPNG(newText, png.offset, png.angleA, png.angleB, png.angleC, newScale, png.texturePath);
                        replaceBgPicture(png, newPngPicture, c3d.getLockableDatFileReference());

                        pngPictureUpdateCounter++;
                        if (pngPictureUpdateCounter > 3) {
                            for (OpenGLRenderer renderer2 : renders) {
                                renderer2.disposeOldTextures();
                            }
                            pngPictureUpdateCounter = 0;
                        }

                        vm.setModified(true, true);
                        vm.setSelectedBgPicture(c3d.getLockableDatFileReference().getBackgroundPicture(vm.getSelectedBgPictureIndex()));
                        return;
                    }
                }
            }
        });
        spn_PngSY[0].addValueChangeListener(new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
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

                        Vertex newScale = new Vertex(png.scale.X, spn.getValue(), png.scale.Z);
                        String newText = png.getString(png.offset, png.angleA, png.angleB, png.angleC, newScale, png.texturePath);

                        GDataPNG newPngPicture = new GDataPNG(newText, png.offset, png.angleA, png.angleB, png.angleC, newScale, png.texturePath);
                        replaceBgPicture(png, newPngPicture, c3d.getLockableDatFileReference());

                        vm.setModified(true, true);
                        vm.setSelectedBgPicture(c3d.getLockableDatFileReference().getBackgroundPicture(vm.getSelectedBgPictureIndex()));
                        return;
                    }
                }
            }
        });
        spn_PngX[0].addValueChangeListener(new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
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


                        Vertex newOffset = new Vertex(spn.getValue(), png.offset.Y, png.offset.Z);
                        String newText = png.getString(newOffset, png.angleA, png.angleB, png.angleC, png.scale, png.texturePath);

                        GDataPNG newPngPicture = new GDataPNG(newText, newOffset, png.angleA, png.angleB, png.angleC, png.scale, png.texturePath);
                        replaceBgPicture(png, newPngPicture, c3d.getLockableDatFileReference());

                        pngPictureUpdateCounter++;
                        if (pngPictureUpdateCounter > 3) {
                            for (OpenGLRenderer renderer2 : renders) {
                                renderer2.disposeOldTextures();
                            }
                            pngPictureUpdateCounter = 0;
                        }

                        vm.setModified(true, true);
                        vm.setSelectedBgPicture(c3d.getLockableDatFileReference().getBackgroundPicture(vm.getSelectedBgPictureIndex()));
                        return;
                    }
                }
            }
        });
        spn_PngY[0].addValueChangeListener(new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
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

                        Vertex newOffset = new Vertex(png.offset.X, spn.getValue(), png.offset.Z);
                        String newText = png.getString(newOffset, png.angleA, png.angleB, png.angleC, png.scale, png.texturePath);

                        GDataPNG newPngPicture = new GDataPNG(newText, newOffset, png.angleA, png.angleB, png.angleC, png.scale, png.texturePath);
                        replaceBgPicture(png, newPngPicture, c3d.getLockableDatFileReference());

                        pngPictureUpdateCounter++;
                        if (pngPictureUpdateCounter > 3) {
                            for (OpenGLRenderer renderer2 : renders) {
                                renderer2.disposeOldTextures();
                            }
                            pngPictureUpdateCounter = 0;
                        }

                        vm.setModified(true, true);
                        vm.setSelectedBgPicture(c3d.getLockableDatFileReference().getBackgroundPicture(vm.getSelectedBgPictureIndex()));
                        return;
                    }
                }
            }
        });
        spn_PngZ[0].addValueChangeListener(new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
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

                        Vertex newOffset = new Vertex(png.offset.X, png.offset.Y, spn.getValue());
                        String newText = png.getString(newOffset, png.angleA, png.angleB, png.angleC, png.scale, png.texturePath);

                        GDataPNG newPngPicture = new GDataPNG(newText, newOffset, png.angleA, png.angleB, png.angleC, png.scale, png.texturePath);
                        replaceBgPicture(png, newPngPicture, c3d.getLockableDatFileReference());

                        pngPictureUpdateCounter++;
                        if (pngPictureUpdateCounter > 3) {
                            for (OpenGLRenderer renderer2 : renders) {
                                renderer2.disposeOldTextures();
                            }
                            pngPictureUpdateCounter = 0;
                        }

                        vm.setModified(true, true);
                        vm.setSelectedBgPicture(c3d.getLockableDatFileReference().getBackgroundPicture(vm.getSelectedBgPictureIndex()));
                        return;
                    }
                }
            }
        });

        mntm_IconSize1[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                WorkbenchManager.getUserSettingState().setIconSize(0);
            }
        });
        mntm_IconSize2[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                WorkbenchManager.getUserSettingState().setIconSize(1);
            }
        });
        mntm_IconSize3[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                WorkbenchManager.getUserSettingState().setIconSize(2);
            }
        });
        mntm_IconSize4[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                WorkbenchManager.getUserSettingState().setIconSize(3);
            }
        });
        mntm_IconSize5[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                WorkbenchManager.getUserSettingState().setIconSize(4);
            }
        });
        mntm_IconSize6[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                WorkbenchManager.getUserSettingState().setIconSize(5);
            }
        });

        Project.createDefault();
        treeItem_Project[0].setData(Project.getProjectPath());
        treeItem_Official[0].setData(WorkbenchManager.getUserSettingState().getLdrawFolderPath());
        treeItem_Unofficial[0].setData(WorkbenchManager.getUserSettingState().getUnofficialFolderPath());
        LibraryManager.readUnofficialParts(treeItem_UnofficialParts[0]);
        LibraryManager.readUnofficialSubparts(treeItem_UnofficialSubparts[0]);
        LibraryManager.readUnofficialPrimitives(treeItem_UnofficialPrimitives[0]);
        LibraryManager.readUnofficialHiResPrimitives(treeItem_UnofficialPrimitives48[0]);
        LibraryManager.readUnofficialLowResPrimitives(treeItem_UnofficialPrimitives8[0]);
        LibraryManager.readOfficialParts(treeItem_OfficialParts[0]);
        LibraryManager.readOfficialSubparts(treeItem_OfficialSubparts[0]);
        LibraryManager.readOfficialPrimitives(treeItem_OfficialPrimitives[0]);
        LibraryManager.readOfficialHiResPrimitives(treeItem_OfficialPrimitives48[0]);
        LibraryManager.readOfficialLowResPrimitives(treeItem_OfficialPrimitives8[0]);
        txt_Search[0].setText(" "); //$NON-NLS-1$
        txt_Search[0].setText(""); //$NON-NLS-1$

        Project.getFileToEdit().setLastSelectedComposite(Editor3DWindow.renders.get(0).getC3D());
        new EditorTextWindow().run(Project.getFileToEdit());

        updateBgPictureTab();
        Project.getFileToEdit().addHistory();
        this.open();
        // Dispose all resources (never delete this!)
        ResourceManager.dispose();
        SWTResourceManager.dispose();
        // Dispose the display (never delete this, too!)
        Display.getCurrent().dispose();
    }

    private void replaceBgPicture(GDataPNG selectedBgPicture, GDataPNG newBgPicture, DatFile linkedDatFile) {
        if (linkedDatFile.getDrawPerLine_NOCLONE().getKey(selectedBgPicture) == null) return;
        GData before = selectedBgPicture.getBefore();
        GData next = selectedBgPicture.getNext();
        int index = linkedDatFile.getDrawPerLine_NOCLONE().getKey(selectedBgPicture);
        selectedBgPicture.setGoingToBeReplaced(true);
        linkedDatFile.getVertexManager().remove(selectedBgPicture);
        linkedDatFile.getDrawPerLine_NOCLONE().put(index, newBgPicture);
        before.setNext(newBgPicture);
        newBgPicture.setNext(next);
        linkedDatFile.getVertexManager().setSelectedBgPicture(newBgPicture);
        updateBgPictureTab();
        return;
    }

    private void resetAddState() {
        setAddingSubfiles(false);
        setAddingVertices(false);
        setAddingLines(false);
        setAddingTriangles(false);
        setAddingQuads(false);
        setAddingCondlines(false);
        for (OpenGLRenderer renderer : renders) {
            Composite3D c3d = renderer.getC3D();
            DatFile df = c3d.getLockableDatFileReference();
            df.setObjVertex1(null);
            df.setObjVertex2(null);
            df.setObjVertex3(null);
            df.setObjVertex4(null);
            df.setNearestObjVertex1(null);
            df.setNearestObjVertex2(null);
        }
    }

    public void setAddState(int type) {
        if (isAddingSomething()) {
            resetAddState();
            btn_AddVertex[0].setSelection(false);
            btn_AddLine[0].setSelection(false);
            btn_AddTriangle[0].setSelection(false);
            btn_AddQuad[0].setSelection(false);
            btn_AddCondline[0].setSelection(false);
            btn_AddPrimitive[0].setSelection(false);
            setAddingSomething(false);
        }
        switch (type) {
        case 0:
            btn_AddComment[0].notifyListeners(SWT.Selection, new Event());
            break;
        case 1:
            setAddingVertices(!isAddingVertices());
            btn_AddVertex[0].setSelection(isAddingVertices());
            setAddingSomething(isAddingVertices());
            clickSingleBtn(btn_AddVertex[0]);
            break;
        case 2:
            setAddingLines(!isAddingLines());
            btn_AddLine[0].setSelection(isAddingLines());
            setAddingSomething(isAddingLines());
            clickSingleBtn(btn_AddLine[0]);
            break;
        case 3:
            setAddingTriangles(!isAddingTriangles());
            btn_AddTriangle[0].setSelection(isAddingTriangles());
            setAddingSomething(isAddingTriangles());
            clickSingleBtn(btn_AddTriangle[0]);
            break;
        case 4:
            setAddingQuads(!isAddingQuads());
            btn_AddQuad[0].setSelection(isAddingQuads());
            setAddingSomething(isAddingQuads());
            clickSingleBtn(btn_AddQuad[0]);
            break;
        case 5:
            setAddingCondlines(!isAddingCondlines());
            btn_AddCondline[0].setSelection(isAddingCondlines());
            setAddingSomething(isAddingCondlines());
            clickSingleBtn(btn_AddCondline[0]);
            break;
        }
    }

    public void setObjMode(int type) {
        switch (type) {
        case 0:
            btn_Vertices[0].setSelection(true);
            setWorkingType(ObjectMode.VERTICES);
            clickSingleBtn(btn_Vertices[0]);
            break;
        case 1:
            btn_TrisNQuads[0].setSelection(true);
            setWorkingType(ObjectMode.FACES);
            clickSingleBtn(btn_TrisNQuads[0]);
            break;
        case 2:
            btn_Lines[0].setSelection(true);
            setWorkingType(ObjectMode.LINES);
            clickSingleBtn(btn_Lines[0]);
            break;
        case 3:
            btn_Subfiles[0].setSelection(true);
            setWorkingType(ObjectMode.SUBFILES);
            clickSingleBtn(btn_Subfiles[0]);
            break;
        }
    }

    /**
     * Create the actions.
     */
    private void createActions() {
        // Create the actions
        // {
        // menuItem_Open = new Action(I18n.EDITOR3D_Open) {
        // @Override
        // public void run() {
        //
        // }
        // };
        // menuItem_Open.setAccelerator(SWT.CTRL | 'Z');
        // }
        // {
        // menuItem_Exit = new Action(I18n.EDITOR3D_Exit) {
        // @Override
        // public void run() {
        //
        // }
        // };
        // }
        // {
        // toolItem_Save = new Action(I18n.EDITOR3D_Save) {
        // };
        //            toolItem_Save.setImageDescriptor(ImageDescriptor.createFromImage(ResourceManager.getImage("icon32_document-save.png"))); //$NON-NLS-1$
        // toolItem_Save.setAccelerator(SWT.CTRL | 'S');
        // }
        // mnu_File[0].add(menuItem_Open);
        // mnu_File[0].add(toolItem_Save);
        // mnu_File[0].add(new Separator());
        // mnu_File[0].add(menuItem_Exit);
        // mnu_File[0].getParent().update(true);
    }

    /**
     * The Shell-Close-Event
     */
    @Override
    protected void handleShellCloseEvent() {
        boolean unsavedProjectFiles = false;
        Set<DatFile> unsavedFiles = new HashSet<DatFile>(Project.getUnsavedFiles());
        for (DatFile df : unsavedFiles) {
            if (!df.getText().equals(df.getOriginalText()) || df.isVirtual() && !df.getText().trim().isEmpty()) {
                MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.CANCEL | SWT.NO);
                messageBox.setText(I18n.DIALOG_UnsavedChangesTitle);

                Object[] messageArguments = {df.getShortName()};
                MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                formatter.setLocale(View.LOCALE);
                formatter.applyPattern(I18n.DIALOG_UnsavedChanges);
                messageBox.setMessage(formatter.format(messageArguments));

                int result = messageBox.open();

                if (result == SWT.NO) {
                    // Remove file from tree
                    updateTree_removeEntry(df);
                } else if (result == SWT.YES) {
                    if (df.save()) {
                    } else {
                        MessageBox messageBoxError = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                        messageBoxError.setText(I18n.DIALOG_Error);
                        messageBoxError.setMessage(I18n.DIALOG_CantSaveFile);
                        messageBoxError.open();
                        cleanupClosedData();
                        updateTree_unsavedEntries();
                        return;
                    }
                } else {
                    cleanupClosedData();
                    updateTree_unsavedEntries();
                    return;
                }
            }
        }
        Set<EditorTextWindow> ow = new HashSet<EditorTextWindow>(Project.getOpenTextWindows());
        for (EditorTextWindow w : ow) {
            w.getShell().close();
        }

        {
            ArrayList<TreeItem> ta = getProjectParts().getItems();
            for (TreeItem ti : ta) {
                unsavedProjectFiles = unsavedProjectFiles || !((DatFile) ti.getData()).getText().trim().equals("") || !Project.getUnsavedFiles().contains(ti.getData()); //$NON-NLS-1$
            }
        }
        {
            ArrayList<TreeItem> ta = getProjectSubparts().getItems();
            for (TreeItem ti : ta) {
                unsavedProjectFiles = unsavedProjectFiles || !((DatFile) ti.getData()).getText().trim().equals("") || !Project.getUnsavedFiles().contains(ti.getData()); ; //$NON-NLS-1$
            }
        }
        {
            ArrayList<TreeItem> ta = getProjectPrimitives().getItems();
            for (TreeItem ti : ta) {
                unsavedProjectFiles = unsavedProjectFiles || !((DatFile) ti.getData()).getText().trim().equals("") || !Project.getUnsavedFiles().contains(ti.getData()); ; //$NON-NLS-1$
            }
        }
        {
            ArrayList<TreeItem> ta = getProjectPrimitives48().getItems();
            for (TreeItem ti : ta) {
                unsavedProjectFiles = unsavedProjectFiles || !((DatFile) ti.getData()).getText().trim().equals("") || !Project.getUnsavedFiles().contains(ti.getData()); ; //$NON-NLS-1$
            }
        }
        {
            ArrayList<TreeItem> ta = getProjectPrimitives8().getItems();
            for (TreeItem ti : ta) {
                unsavedProjectFiles = unsavedProjectFiles || !((DatFile) ti.getData()).getText().trim().equals("") || !Project.getUnsavedFiles().contains(ti.getData()); ; //$NON-NLS-1$
            }
        }

        if (unsavedProjectFiles && Project.isDefaultProject()) {
            // Save new project here, if the project contains at least one non-empty file
            boolean cancelIt = false;
            boolean secondRun = false;
            while (true) {
                int result = IDialogConstants.CANCEL_ID;
                if (secondRun) result = new NewProjectDialog(true).open();
                if (result == IDialogConstants.OK_ID) {
                    while (new File(Project.getTempProjectPath()).isDirectory()) {
                        MessageBox messageBoxError = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.YES | SWT.CANCEL | SWT.NO);
                        messageBoxError.setText(I18n.PROJECT_ProjectOverwriteTitle);
                        messageBoxError.setMessage(I18n.PROJECT_ProjectOverwrite);
                        int result2 = messageBoxError.open();
                        if (result2 == SWT.NO) {
                            result = new NewProjectDialog(true).open();
                        } else if (result2 == SWT.YES) {
                            break;
                        } else {
                            cancelIt = true;
                            break;
                        }
                    }
                    if (!cancelIt) {
                        Project.setProjectName(Project.getTempProjectName());
                        Project.setProjectPath(Project.getTempProjectPath());
                        NLogger.debug(getClass(), "Saving new project..."); //$NON-NLS-1$
                        if (!Project.save()) {
                            MessageBox messageBoxError = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                            messageBoxError.setText(I18n.DIALOG_Error);
                            messageBoxError.setMessage(I18n.DIALOG_CantSaveProject);
                        }
                    }
                    break;
                } else {
                    secondRun = true;
                    MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.CANCEL | SWT.NO);
                    messageBox.setText(I18n.DIALOG_UnsavedChangesTitle);

                    Object[] messageArguments = {I18n.DIALOG_TheNewProject};
                    MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                    formatter.setLocale(View.LOCALE);
                    formatter.applyPattern(I18n.DIALOG_UnsavedChanges);
                    messageBox.setMessage(formatter.format(messageArguments));

                    int result2 = messageBox.open();
                    if (result2 == SWT.CANCEL) {
                        cancelIt = true;
                        break;
                    } else if (result2 == SWT.NO) {
                        break;
                    }
                }
            }
            if (cancelIt) {
                cleanupClosedData();
                updateTree_unsavedEntries();
                return;
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
                    try {
                        GLContext.useContext(canvas);
                    } catch (LWJGLException e) {
                        NLogger.error(Editor3DWindow.class, e);
                    }
                }
                renderer.dispose();
            } catch (SWTException swtEx) {
                NLogger.error(Editor3DWindow.class, swtEx);
            }
        }
        // All "history threads" needs to know that the main window was closed
        alive.set(false);

        final Editor3DWindowState winState = WorkbenchManager.getEditor3DWindowState();

        // Traverse the sash forms to store the 3D configuration
        final ArrayList<Composite3DState> c3dStates = new ArrayList<Composite3DState>();
        Control c = Editor3DDesign.getSashForm().getChildren()[1];
        if (c != null) {
            if (c instanceof SashForm|| c instanceof CompositeContainer) {
                // c instanceof CompositeContainer: Simple case, since its only one 3D view open -> No recursion!
                saveComposite3DStates(c, c3dStates, "", "|"); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                // There is no 3D window open at the moment
            }
        } else {
            // There is no 3D window open at the moment
        }
        winState.setThreeDwindowConfig(c3dStates);

        winState.setLeftSashWeights(((SashForm) Editor3DDesign.getSashForm().getChildren()[0]).getWeights());
        winState.setLeftSashWidth(Editor3DDesign.getSashForm().getWeights());
        winState.setPrimitiveZoom(cmp_Primitives[0].getZoom());
        winState.setPrimitiveZoomExponent(cmp_Primitives[0].getZoom_exponent());
        winState.setPrimitiveViewport(cmp_Primitives[0].getViewport2());
        // Save the workbench
        WorkbenchManager.saveWorkbench();
        setReturnCode(CANCEL);
        close();
    }

    private void saveComposite3DStates(Control c, ArrayList<Composite3DState> c3dStates, String parentPath, String path) {
        Composite3DState st = new Composite3DState();
        st.setParentPath(parentPath);
        st.setPath(path);
        if (c instanceof CompositeContainer) {
            NLogger.debug(getClass(), path + "C"); //$NON-NLS-1$
            final Composite3D c3d = ((CompositeContainer) c).getComposite3D();
            st.setSash(false);
            st.setScales(c3d.getParent() instanceof CompositeScale);
            st.setVertical(false);
            st.setWeights(null);
            st.setPerspective(c3d.isClassicPerspective() ? c3d.getPerspectiveIndex() : Perspective.TWO_THIRDS);
            st.setRenderMode(c3d.getRenderMode());
            st.setShowLabel(c3d.isShowingLabels());
            st.setShowAxis(c3d.isShowingAxis());
            st.setShowGrid(c3d.isGridShown());
            st.setShowOrigin(c3d.isOriginShown());
            st.setLights(c3d.isLightOn());
            st.setMeshlines(c3d.isMeshLines());
            st.setSubfileMeshlines(c3d.isSubMeshLines());
            st.setVertices(c3d.isShowingVertices());
            st.setHiddenVertices(c3d.isShowingHiddenVertices());
            st.setStudLogo(c3d.isShowingLogo());
            st.setLineMode(c3d.getLineMode());
            st.setAlwaysBlackLines(c3d.isBlackEdges());
            st.setAnaglyph3d(c3d.isAnaglyph3d());
            st.setGridScale(c3d.getGrid_scale());
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

    /**
     * @return The serializable window state of the Editor3DWindow
     */
    public Editor3DWindowState getEditor3DWindowState() {
        return this.editor3DWindowState;
    }

    /**
     * @param editor3DWindowState
     *            The serializable window state of the Editor3DWindow
     */
    public void setEditor3DWindowState(Editor3DWindowState editor3DWindowState) {
        this.editor3DWindowState = editor3DWindowState;
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
    public void updateTree_unsavedEntries() {
        ArrayList<TreeItem> categories = new ArrayList<TreeItem>();
        categories.add(this.treeItem_ProjectParts[0]);
        categories.add(this.treeItem_ProjectSubparts[0]);
        categories.add(this.treeItem_ProjectPrimitives[0]);
        categories.add(this.treeItem_ProjectPrimitives48[0]);
        categories.add(this.treeItem_ProjectPrimitives8[0]);
        categories.add(this.treeItem_UnofficialParts[0]);
        categories.add(this.treeItem_UnofficialSubparts[0]);
        categories.add(this.treeItem_UnofficialPrimitives[0]);
        categories.add(this.treeItem_UnofficialPrimitives48[0]);
        categories.add(this.treeItem_UnofficialPrimitives8[0]);
        int counter = 0;
        for (TreeItem item : categories) {
            counter++;
            ArrayList<TreeItem> datFileTreeItems = item.getItems();
            for (TreeItem df : datFileTreeItems) {
                DatFile d = (DatFile) df.getData();
                StringBuilder nameSb = new StringBuilder(new File(d.getNewName()).getName());
                final String d2 = d.getDescription();
                if (counter < 6 && (!d.getNewName().startsWith(Project.getProjectPath()) || !d.getNewName().replace(Project.getProjectPath() + File.separator, "").contains(File.separator))) { //$NON-NLS-1$
                    nameSb.insert(0, "(!) "); //$NON-NLS-1$
                }

                // MARK For Debug Only!
                //                DatType t = d.getType();
                //                if (t == DatType.PART) {
                //                    nameSb.append(" PART"); //$NON-NLS-1$
                //                } else if (t == DatType.SUBPART) {
                //                    nameSb.append(" SUBPART"); //$NON-NLS-1$
                //                } else if (t == DatType.PRIMITIVE) {
                //                    nameSb.append(" PRIMITIVE"); //$NON-NLS-1$
                //                } else if (t == DatType.PRIMITIVE48) {
                //                    nameSb.append(" PRIMITIVE48"); //$NON-NLS-1$
                //                } else if (t == DatType.PRIMITIVE8) {
                //                    nameSb.append(" PRIMITIVE8"); //$NON-NLS-1$
                //                }

                if (d2 != null)
                    nameSb.append(d2);
                if (Project.getUnsavedFiles().contains(d)) {
                    df.setText("* " + nameSb.toString()); //$NON-NLS-1$
                } else {
                    df.setText(nameSb.toString());
                }
            }
        }
        this.treeItem_Unsaved[0].removeAll();
        Set<DatFile> unsaved = Project.getUnsavedFiles();
        for (DatFile df : unsaved) {
            TreeItem ti = new TreeItem(this.treeItem_Unsaved[0], SWT.NONE);
            StringBuilder nameSb = new StringBuilder(new File(df.getNewName()).getName());
            final String d = df.getDescription();
            if (d != null)
                nameSb.append(d);
            ti.setText(nameSb.toString());
            ti.setData(df);
        }

        this.treeParts[0].build();
        this.treeParts[0].redraw();
    }


    /**
     * Updates the tree for renamed entries
     */
    @SuppressWarnings("unchecked")
    public void updateTree_renamedEntries() {
        HashMap<String, TreeItem> categories = new HashMap<String, TreeItem>();
        HashMap<String, DatType> types = new HashMap<String, DatType>();

        ArrayList<String> validPrefixes = new ArrayList<String>();

        {
            String s = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "PARTS" + File.separator + "S" + File.separator; //$NON-NLS-1$ //$NON-NLS-2$
            validPrefixes.add(s);
            categories.put(s, this.treeItem_UnofficialSubparts[0]);
            types.put(s, DatType.SUBPART);
        }
        {
            String s = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "parts" + File.separator + "s" + File.separator; //$NON-NLS-1$ //$NON-NLS-2$
            validPrefixes.add(s);
            categories.put(s, this.treeItem_UnofficialSubparts[0]);
            types.put(s, DatType.SUBPART);
        }
        {
            String s = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "PARTS" + File.separator; //$NON-NLS-1$
            validPrefixes.add(s);
            categories.put(s, this.treeItem_UnofficialParts[0]);
            types.put(s, DatType.PART);
        }
        {
            String s = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "parts" + File.separator; //$NON-NLS-1$
            validPrefixes.add(s);
            categories.put(s,this.treeItem_UnofficialParts[0]);
            types.put(s, DatType.PART);
        }
        {
            String s = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "P" + File.separator + "48" + File.separator; //$NON-NLS-1$ //$NON-NLS-2$
            validPrefixes.add(s);
            categories.put(s, this.treeItem_UnofficialPrimitives48[0]);
            types.put(s, DatType.PRIMITIVE48);
        }
        {
            String s = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "p" + File.separator + "48" + File.separator; //$NON-NLS-1$ //$NON-NLS-2$
            validPrefixes.add(s);
            categories.put(s, this.treeItem_UnofficialPrimitives48[0]);
            types.put(s, DatType.PRIMITIVE48);
        }
        {
            String s = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "P" + File.separator + "8" + File.separator; //$NON-NLS-1$ //$NON-NLS-2$
            validPrefixes.add(s);
            categories.put(s, this.treeItem_UnofficialPrimitives8[0]);
            types.put(s, DatType.PRIMITIVE8);
        }
        {
            String s = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "p" + File.separator + "8" + File.separator; //$NON-NLS-1$ //$NON-NLS-2$
            validPrefixes.add(s);
            categories.put(s, this.treeItem_UnofficialPrimitives8[0]);
            types.put(s, DatType.PRIMITIVE8);
        }
        {
            String s = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "P" + File.separator; //$NON-NLS-1$
            validPrefixes.add(s);
            categories.put(s, this.treeItem_UnofficialPrimitives[0]);
            types.put(s, DatType.PRIMITIVE);
        }
        {
            String s = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "p" + File.separator; //$NON-NLS-1$
            validPrefixes.add(s);
            categories.put(s, this.treeItem_UnofficialPrimitives[0]);
            types.put(s, DatType.PRIMITIVE);
        }
        {
            String s = Project.getProjectPath() + File.separator + "PARTS" + File.separator + "S" + File.separator; //$NON-NLS-1$ //$NON-NLS-2$
            validPrefixes.add(s);
            categories.put(s, this.treeItem_ProjectSubparts[0]);
            types.put(s, DatType.SUBPART);
        }
        {
            String s = Project.getProjectPath() + File.separator + "parts" + File.separator + "s" + File.separator; //$NON-NLS-1$ //$NON-NLS-2$
            validPrefixes.add(s);
            categories.put(s, this.treeItem_ProjectSubparts[0]);
            types.put(s, DatType.SUBPART);
        }
        {
            String s = Project.getProjectPath() + File.separator + "PARTS" + File.separator; //$NON-NLS-1$
            validPrefixes.add(s);
            categories.put(s, this.treeItem_ProjectParts[0]);
            types.put(s, DatType.PART);
        }
        {
            String s = Project.getProjectPath() + File.separator + "parts" + File.separator; //$NON-NLS-1$
            validPrefixes.add(s);
            categories.put(s, this.treeItem_ProjectParts[0]);
            types.put(s, DatType.PART);
        }
        {
            String s = Project.getProjectPath() + File.separator + "P" + File.separator + "48" + File.separator; //$NON-NLS-1$ //$NON-NLS-2$
            validPrefixes.add(s);
            categories.put(s, this.treeItem_ProjectPrimitives48[0]);
            types.put(s, DatType.PRIMITIVE48);
        }
        {
            String s = Project.getProjectPath() + File.separator + "p" + File.separator + "48" + File.separator; //$NON-NLS-1$ //$NON-NLS-2$
            validPrefixes.add(s);
            categories.put(s, this.treeItem_ProjectPrimitives48[0]);
            types.put(s, DatType.PRIMITIVE48);
        }
        {
            String s = Project.getProjectPath() + File.separator + "P" + File.separator + "8" + File.separator; //$NON-NLS-1$ //$NON-NLS-2$
            validPrefixes.add(s);
            categories.put(s, this.treeItem_ProjectPrimitives8[0]);
            types.put(s, DatType.PRIMITIVE8);
        }
        {
            String s = Project.getProjectPath() + File.separator + "p" + File.separator + "8" + File.separator; //$NON-NLS-1$ //$NON-NLS-2$
            validPrefixes.add(s);
            categories.put(s, this.treeItem_ProjectPrimitives8[0]);
            types.put(s, DatType.PRIMITIVE8);
        }
        {
            String s = Project.getProjectPath() + File.separator + "P" + File.separator; //$NON-NLS-1$
            validPrefixes.add(s);
            categories.put(s, this.treeItem_ProjectPrimitives[0]);
            types.put(s, DatType.PRIMITIVE);
        }
        {
            String s = Project.getProjectPath() + File.separator + "p" + File.separator; //$NON-NLS-1$
            validPrefixes.add(s);
            categories.put(s, this.treeItem_ProjectPrimitives[0]);
            types.put(s, DatType.PRIMITIVE);
        }

        Collections.sort(validPrefixes, new Comp());

        for (String prefix : validPrefixes) {
            TreeItem item = categories.get(prefix);
            ArrayList<DatFile> dats = (ArrayList<DatFile>) item.getData();
            ArrayList<TreeItem> datFileTreeItems = item.getItems();
            Set<TreeItem> itemsToRemove = new HashSet<TreeItem>();
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
                        ((ArrayList<DatFile>) item2.getData()).add(d);
                        TreeItem nt = new TreeItem(item2, SWT.NONE);
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
    public void updateTree_removeEntry(DatFile e) {
        ArrayList<TreeItem> categories = new ArrayList<TreeItem>();
        categories.add(this.treeItem_ProjectParts[0]);
        categories.add(this.treeItem_ProjectSubparts[0]);
        categories.add(this.treeItem_ProjectPrimitives[0]);
        categories.add(this.treeItem_ProjectPrimitives8[0]);
        categories.add(this.treeItem_ProjectPrimitives48[0]);
        categories.add(this.treeItem_UnofficialParts[0]);
        categories.add(this.treeItem_UnofficialSubparts[0]);
        categories.add(this.treeItem_UnofficialPrimitives[0]);
        categories.add(this.treeItem_UnofficialPrimitives8[0]);
        categories.add(this.treeItem_UnofficialPrimitives48[0]);
        int counter = 0;
        for (TreeItem item : categories) {
            counter++;
            ArrayList<TreeItem> datFileTreeItems = new ArrayList<TreeItem>(item.getItems());
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
        this.treeItem_Unsaved[0].removeAll();
        Project.removeUnsavedFile(e);
        for (OpenGLRenderer renderer : renders) {
            Composite3D c3d = renderer.getC3D();
            if (c3d.getLockableDatFileReference().equals(e)) {
                c3d.unlinkData();
            }
        }
        HashSet<EditorTextWindow> windows = new HashSet<EditorTextWindow>(Project.getOpenTextWindows());
        for (EditorTextWindow win : windows) {
            win.closeTabWithDatfile(e);
        }

        Set<DatFile> unsaved = Project.getUnsavedFiles();
        for (DatFile df : unsaved) {
            TreeItem ti = new TreeItem(this.treeItem_Unsaved[0], SWT.NONE);
            StringBuilder nameSb = new StringBuilder(new File(df.getNewName()).getName());
            final String d = df.getDescription();
            if (d != null)
                nameSb.append(d);
            ti.setText(nameSb.toString());
            ti.setData(df);
        }

        TreeItem[] folders = new TreeItem[10];
        folders[0] = treeItem_ProjectParts[0];
        folders[1] = treeItem_ProjectPrimitives[0];
        folders[2] = treeItem_ProjectPrimitives8[0];
        folders[3] = treeItem_ProjectPrimitives48[0];
        folders[4] = treeItem_ProjectSubparts[0];

        folders[5] = treeItem_UnofficialParts[0];
        folders[6] = treeItem_UnofficialPrimitives[0];
        folders[7] = treeItem_UnofficialPrimitives8[0];
        folders[8] = treeItem_UnofficialPrimitives48[0];
        folders[9] = treeItem_UnofficialSubparts[0];

        for (TreeItem folder : folders) {
            @SuppressWarnings("unchecked")
            ArrayList<DatFile> cachedReferences =(ArrayList<DatFile>) folder.getData();
            cachedReferences.remove(e);
        }

        this.treeParts[0].build();
        this.treeParts[0].redraw();
    }

    // Helper functions
    private void clickBtnTest(Button btn) {
        WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btn.getParent());
        btn.setSelection(true);
    }

    private void clickSingleBtn(Button btn) {
        boolean state = btn.getSelection();
        WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btn.getParent());
        btn.setSelection(state);
    }

    public boolean isAddingSomething() {
        return addingSomething;
    }

    public void setAddingSomething(boolean addingSomething) {
        this.addingSomething = addingSomething;
        for (OpenGLRenderer renderer : renders) {
            renderer.getC3D().getLockableDatFileReference().getVertexManager().clearSelection();
        }
    }

    public boolean isAddingVertices() {
        return addingVertices;
    }

    public void setAddingVertices(boolean addingVertices) {
        this.addingVertices = addingVertices;
    }

    public boolean isAddingLines() {
        return addingLines;
    }

    public void setAddingLines(boolean addingLines) {
        this.addingLines = addingLines;
    }

    public boolean isAddingTriangles() {
        return addingTriangles;
    }

    public void setAddingTriangles(boolean addingTriangles) {
        this.addingTriangles = addingTriangles;
    }

    public boolean isAddingQuads() {
        return addingQuads;
    }

    public void setAddingQuads(boolean addingQuads) {
        this.addingQuads = addingQuads;
    }

    public boolean isAddingCondlines() {
        return addingCondlines;
    }

    public void setAddingCondlines(boolean addingCondlines) {
        this.addingCondlines = addingCondlines;
    }

    public boolean isAddingSubfiles() {
        return addingSubfiles;
    }

    public void setAddingSubfiles(boolean addingSubfiles) {
        this.addingSubfiles = addingSubfiles;
    }

    public void disableAddAction() {
        addingSomething = false;
        addingVertices = false;
        addingLines = false;
        addingTriangles = false;
        addingQuads = false;
        addingCondlines = false;
        addingSubfiles = false;
        btn_AddVertex[0].setSelection(false);
        btn_AddLine[0].setSelection(false);
        btn_AddTriangle[0].setSelection(false);
        btn_AddQuad[0].setSelection(false);
        btn_AddCondline[0].setSelection(false);
        btn_AddPrimitive[0].setSelection(false);
    }

    public TreeItem getProjectParts() {
        return treeItem_ProjectParts[0];
    }

    public TreeItem getProjectPrimitives() {
        return treeItem_ProjectPrimitives[0];
    }

    public TreeItem getProjectPrimitives48() {
        return treeItem_ProjectPrimitives48[0];
    }

    public TreeItem getProjectPrimitives8() {
        return treeItem_ProjectPrimitives8[0];
    }

    public TreeItem getProjectSubparts() {
        return treeItem_ProjectSubparts[0];
    }

    public TreeItem getUnofficialParts() {
        return treeItem_UnofficialParts[0];
    }

    public TreeItem getUnofficialPrimitives() {
        return treeItem_UnofficialPrimitives[0];
    }

    public TreeItem getUnofficialPrimitives48() {
        return treeItem_UnofficialPrimitives48[0];
    }

    public TreeItem getUnofficialPrimitives8() {
        return treeItem_UnofficialPrimitives8[0];
    }

    public TreeItem getUnofficialSubparts() {
        return treeItem_UnofficialSubparts[0];
    }

    public TreeItem getOfficialParts() {
        return treeItem_OfficialParts[0];
    }

    public TreeItem getOfficialPrimitives() {
        return treeItem_OfficialPrimitives[0];
    }

    public TreeItem getOfficialPrimitives48() {
        return treeItem_OfficialPrimitives48[0];
    }

    public TreeItem getOfficialPrimitives8() {
        return treeItem_OfficialPrimitives8[0];
    }

    public TreeItem getOfficialSubparts() {
        return treeItem_OfficialSubparts[0];
    }

    public TreeItem getUnsaved() {
        return treeItem_Unsaved[0];
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
        btn_MoveAdjacentData[0].setSelection(movingAdjacentData);
        this.movingAdjacentData = movingAdjacentData;
    }

    public WorkingMode getWorkingAction() {
        return workingAction;
    }

    public void setWorkingAction(WorkingMode workingAction) {
        this.workingAction = workingAction;
        switch (workingAction) {
        case COMBINED:
            clickBtnTest(btn_Combined[0]);
            workingAction = WorkingMode.COMBINED;
            break;
        case MOVE:
            clickBtnTest(btn_Move[0]);
            workingAction = WorkingMode.MOVE;
            break;
        case ROTATE:
            clickBtnTest(btn_Rotate[0]);
            workingAction = WorkingMode.ROTATE;
            break;
        case SCALE:
            clickBtnTest(btn_Scale[0]);
            workingAction = WorkingMode.SCALE;
            break;
        case SELECT:
            clickBtnTest(btn_Select[0]);
            workingAction = WorkingMode.SELECT;
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

    public GColour getLastUsedColour() {
        return lastUsedColour;
    }

    public void setLastUsedColour(GColour lastUsedColour) {
        this.lastUsedColour = lastUsedColour;
    }

    public void setLastUsedColour2(GColour lastUsedColour) {
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
        final GColour[] gColour2 = new GColour[] { lastUsedColour };
        int num = gColour2[0].getColourNumber();
        if (View.hasLDConfigColour(num)) {
            gColour2[0] = View.getLDConfigColour(num);
        } else {
            num = -1;
        }
        Editor3DWindow.getWindow().setLastUsedColour(gColour2[0]);
        btn_LastUsedColour[0].removeListener(SWT.Paint, btn_LastUsedColour[0].getListeners(SWT.Paint)[0]);
        btn_LastUsedColour[0].removeListener(SWT.Selection, btn_LastUsedColour[0].getListeners(SWT.Selection)[0]);
        final Color col = SWTResourceManager.getColor((int) (gColour2[0].getR() * 255f), (int) (gColour2[0].getG() * 255f), (int) (gColour2[0].getB() * 255f));
        final Point size = btn_LastUsedColour[0].computeSize(SWT.DEFAULT, SWT.DEFAULT);
        final int x = size.x / 4;
        final int y = size.y / 4;
        final int w = size.x / 2;
        final int h = size.y / 2;
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
                    if (!View.hasLDConfigColour(num)) {
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

    public void cleanupClosedData() {
        Set<DatFile> openFiles = new HashSet<DatFile>(Project.getUnsavedFiles());
        for (OpenGLRenderer renderer : renders) {
            Composite3D c3d = renderer.getC3D();
            openFiles.add(c3d.getLockableDatFileReference());
        }
        for (EditorTextWindow w : Project.getOpenTextWindows()) {
            for (CTabItem t : w.getTabFolder().getItems()) {
                openFiles.add(((CompositeTab) t).getState().getFileNameObj());
            }
        }
        Set<DatFile> deadFiles = new HashSet<DatFile>(Project.getParsedFiles());
        deadFiles.removeAll(openFiles);
        if (!deadFiles.isEmpty()) {
            GData.CACHE_viewByProjection.clear();
            GData.parsedLines.clear();
            GData.CACHE_parsedFilesSource.clear();
        }
        for (DatFile datFile : deadFiles) {
            datFile.disposeData();
        }
        if (!deadFiles.isEmpty()) {
            // TODO Debug only System.gc();
        }
    }

    public String getSearchCriteria() {
        return txt_Search[0].getText();
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
                folders[0] = treeItem_OfficialParts[0];
                folders[1] = treeItem_OfficialPrimitives[0];
                folders[2] = treeItem_OfficialPrimitives8[0];
                folders[3] = treeItem_OfficialPrimitives48[0];
                folders[4] = treeItem_OfficialSubparts[0];

                folders[5] = treeItem_UnofficialParts[0];
                folders[6] = treeItem_UnofficialPrimitives[0];
                folders[7] = treeItem_UnofficialPrimitives8[0];
                folders[8] = treeItem_UnofficialPrimitives48[0];
                folders[9] = treeItem_UnofficialSubparts[0];

                folders[10] = treeItem_ProjectParts[0];
                folders[11] = treeItem_ProjectPrimitives[0];
                folders[12] = treeItem_ProjectPrimitives8[0];
                folders[13] = treeItem_ProjectPrimitives48[0];
                folders[14] = treeItem_ProjectSubparts[0];

                if (folders[0].getData() == null) {
                    for (TreeItem folder : folders) {
                        folder.setData(new ArrayList<DatFile>());
                        for (TreeItem part : folder.getItems()) {
                            ((ArrayList<DatFile>) folder.getData()).add((DatFile) part.getData());
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
                    for (DatFile part : (ArrayList<DatFile>) folder.getData()) {
                        StringBuilder nameSb = new StringBuilder(new File(part.getNewName()).getName());
                        if (i > 9 && (!part.getNewName().startsWith(Project.getProjectPath()) || !part.getNewName().replace(Project.getProjectPath() + File.separator, "").contains(File.separator))) { //$NON-NLS-1$
                            nameSb.insert(0, "(!) "); //$NON-NLS-1$
                        }
                        final String d = part.getDescription();
                        if (d != null)
                            nameSb.append(d);
                        String name = nameSb.toString();
                        TreeItem finding = new TreeItem(folder, SWT.NONE);
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
        ArrayList<OpenGLRenderer> renders2 = new ArrayList<OpenGLRenderer>(renders);
        for (OpenGLRenderer renderer : renders2) {
            Composite3D c3d = renderer.getC3D();
            c3d.getModifier().closeView();
        }
    }

    public TreeData getDatFileTreeData(DatFile df) {
        TreeData result = new TreeData();
        ArrayList<TreeItem> categories = new ArrayList<TreeItem>();
        categories.add(this.treeItem_ProjectParts[0]);
        categories.add(this.treeItem_ProjectSubparts[0]);
        categories.add(this.treeItem_ProjectPrimitives[0]);
        categories.add(this.treeItem_ProjectPrimitives48[0]);
        categories.add(this.treeItem_ProjectPrimitives8[0]);
        categories.add(this.treeItem_UnofficialParts[0]);
        categories.add(this.treeItem_UnofficialSubparts[0]);
        categories.add(this.treeItem_UnofficialPrimitives[0]);
        categories.add(this.treeItem_UnofficialPrimitives48[0]);
        categories.add(this.treeItem_UnofficialPrimitives8[0]);
        categories.add(this.treeItem_OfficialParts[0]);
        categories.add(this.treeItem_OfficialSubparts[0]);
        categories.add(this.treeItem_OfficialPrimitives[0]);
        categories.add(this.treeItem_OfficialPrimitives48[0]);
        categories.add(this.treeItem_OfficialPrimitives8[0]);
        categories.add(this.treeItem_Unsaved[0]);
        for (TreeItem item : categories) {
            ArrayList<TreeItem> datFileTreeItems = item.getItems();
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
                    txt_PngPath[0].setText("---"); //$NON-NLS-1$
                    txt_PngPath[0].setToolTipText("---"); //$NON-NLS-1$

                    spn_PngX[0].setValue(BigDecimal.ZERO);
                    spn_PngY[0].setValue(BigDecimal.ZERO);
                    spn_PngZ[0].setValue(BigDecimal.ZERO);

                    spn_PngA1[0].setValue(BigDecimal.ZERO);
                    spn_PngA2[0].setValue(BigDecimal.ZERO);
                    spn_PngA3[0].setValue(BigDecimal.ZERO);

                    spn_PngSX[0].setValue(BigDecimal.ONE);
                    spn_PngSY[0].setValue(BigDecimal.ONE);

                    txt_PngPath[0].setEnabled(false);
                    btn_PngFocus[0].setEnabled(false);
                    btn_PngImage[0].setEnabled(false);
                    spn_PngX[0].setEnabled(false);
                    spn_PngY[0].setEnabled(false);
                    spn_PngZ[0].setEnabled(false);

                    spn_PngA1[0].setEnabled(false);
                    spn_PngA2[0].setEnabled(false);
                    spn_PngA3[0].setEnabled(false);

                    spn_PngSX[0].setEnabled(false);
                    spn_PngSY[0].setEnabled(false);

                    spn_PngA1[0].getParent().update();
                    updatingPngPictureTab = false;
                    return;
                }

                updatingPngPictureTab = true;

                txt_PngPath[0].setEnabled(true);
                btn_PngFocus[0].setEnabled(true);
                btn_PngImage[0].setEnabled(true);
                spn_PngX[0].setEnabled(true);
                spn_PngY[0].setEnabled(true);
                spn_PngZ[0].setEnabled(true);

                spn_PngA1[0].setEnabled(true);
                spn_PngA2[0].setEnabled(true);
                spn_PngA3[0].setEnabled(true);

                spn_PngSX[0].setEnabled(true);
                spn_PngSY[0].setEnabled(true);

                txt_PngPath[0].setText(png.texturePath);
                txt_PngPath[0].setToolTipText(png.texturePath);

                spn_PngX[0].setValue(png.offset.X);
                spn_PngY[0].setValue(png.offset.Y);
                spn_PngZ[0].setValue(png.offset.Z);

                spn_PngA1[0].setValue(png.angleA);
                spn_PngA2[0].setValue(png.angleB);
                spn_PngA3[0].setValue(png.angleC);

                spn_PngSX[0].setValue(png.scale.X);
                spn_PngSY[0].setValue(png.scale.Y);

                spn_PngA1[0].getParent().update();
                updatingPngPictureTab = false;
                return;
            }
        }
    }

    public void unselectAddSubfile() {
        resetAddState();
        btn_AddPrimitive[0].setSelection(false);
        setAddingSubfiles(false);
        setAddingSomething(false);
    }

    public DatFile createNewDatFile(Shell sh, OpenInWhat where) {

        FileDialog fd = new FileDialog(sh, SWT.SAVE);
        fd.setText("Create a new *.dat file"); //$NON-NLS-1$ I18N Needs translation!

        if ("project".equals(Project.getProjectPath())) { //$NON-NLS-1$
            try {
                String path = LDPartEditor.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                String decodedPath = URLDecoder.decode(path, "UTF-8"); //$NON-NLS-1$
                decodedPath = decodedPath.substring(0, decodedPath.length() - 4);
                fd.setFilterPath(decodedPath + "project"); //$NON-NLS-1$
            } catch (Exception consumed) {
                fd.setFilterPath(Project.getProjectPath());
            }
        } else {
            fd.setFilterPath(Project.getProjectPath());
        }

        String[] filterExt = { "*.dat", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
        fd.setFilterExtensions(filterExt);
        String[] filterNames = { "LDraw Source File (*.dat)", "All Files" }; //$NON-NLS-1$ //$NON-NLS-2$ I18N Needs translation!
        fd.setFilterNames(filterNames);

        while (true) {
            String selected = fd.open();
            System.out.println(selected);

            if (selected != null) {

                // Check if its already created

                DatFile df = new DatFile(selected);

                if (isFileNameAllocated(selected, df, true)) {
                    MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.RETRY | SWT.CANCEL);
                    messageBox.setText(I18n.DIALOG_AlreadyAllocatedNameTitle);
                    messageBox.setMessage(I18n.DIALOG_AlreadyAllocatedName);

                    int result = messageBox.open();

                    if (result == SWT.CANCEL) {
                        break;
                    }
                } else {
                    TreeItem ti = new TreeItem(this.treeItem_ProjectParts[0], SWT.NONE);
                    StringBuilder nameSb = new StringBuilder(new File(df.getNewName()).getName());
                    nameSb.append("(new file)"); //$NON-NLS-1$ I18N
                    ti.setText(nameSb.toString());
                    ti.setData(df);

                    @SuppressWarnings("unchecked")
                    ArrayList<DatFile> cachedReferences = (ArrayList<DatFile>) this.treeItem_ProjectParts[0].getData();
                    cachedReferences.add(df);

                    Project.addUnsavedFile(df);
                    updateTree_renamedEntries();
                    updateTree_unsavedEntries();

                    openDatFile(df, where, null);
                    return df;
                }
            } else {
                break;
            }
        }
        return null;
    }

    public DatFile openDatFile(Shell sh, OpenInWhat where) {

        FileDialog fd = new FileDialog(sh, SWT.OPEN);
        fd.setText("Open *.dat file"); //$NON-NLS-1$ I18N Needs translation!

        if ("project".equals(Project.getProjectPath())) { //$NON-NLS-1$
            try {
                String path = LDPartEditor.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                String decodedPath = URLDecoder.decode(path, "UTF-8"); //$NON-NLS-1$
                decodedPath = decodedPath.substring(0, decodedPath.length() - 4);
                fd.setFilterPath(decodedPath + "project"); //$NON-NLS-1$
            } catch (Exception consumed) {
                fd.setFilterPath(Project.getProjectPath());
            }
        } else {
            fd.setFilterPath(Project.getProjectPath());
        }

        String[] filterExt = { "*.dat", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
        fd.setFilterExtensions(filterExt);
        String[] filterNames = { "LDraw Source File (*.dat)", "All Files" }; //$NON-NLS-1$ //$NON-NLS-2$ I18N Needs translation!
        fd.setFilterNames(filterNames);

        String selected = fd.open();
        System.out.println(selected);

        if (selected != null) {

            // Check if its already created

            DatType type = DatType.PART;

            DatFile df = new DatFile(selected);
            DatFile original = isFileNameAllocated2(selected, df);

            if (original == null) {

                // Type Check and Description Parsing!!
                StringBuilder titleSb = new StringBuilder();
                UTF8BufferedReader reader = null;
                File f = new File(selected);
                try {
                    reader = new UTF8BufferedReader(f.getAbsolutePath());
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

                df = new DatFile(selected, titleSb.toString(), false, type);
                df.setProjectFile(df.getNewName().startsWith(Project.getProjectPath()));

            } else {

                df.setProjectFile(df.getNewName().startsWith(Project.getProjectPath()));
                if (original.isProjectFile()) {
                    openDatFile(df, where, null);
                    return df;
                }
                {
                    @SuppressWarnings("unchecked")
                    ArrayList<DatFile> cachedReferences = (ArrayList<DatFile>) this.treeItem_ProjectParts[0].getData();
                    if (cachedReferences.contains(df)) {
                        openDatFile(df, where, null);
                        return df;
                    }
                }
                {
                    @SuppressWarnings("unchecked")
                    ArrayList<DatFile> cachedReferences = (ArrayList<DatFile>) this.treeItem_ProjectSubparts[0].getData();
                    if (cachedReferences.contains(df)) {
                        openDatFile(df, where, null);
                        return df;
                    }
                }
                {
                    @SuppressWarnings("unchecked")
                    ArrayList<DatFile> cachedReferences = (ArrayList<DatFile>) this.treeItem_ProjectPrimitives[0].getData();
                    if (cachedReferences.contains(df)) {
                        openDatFile(df, where, null);
                        return df;
                    }
                }
                {
                    @SuppressWarnings("unchecked")
                    ArrayList<DatFile> cachedReferences = (ArrayList<DatFile>) this.treeItem_ProjectPrimitives48[0].getData();
                    if (cachedReferences.contains(df)) {
                        openDatFile(df, where, null);
                        return df;
                    }
                }
                {
                    @SuppressWarnings("unchecked")
                    ArrayList<DatFile> cachedReferences = (ArrayList<DatFile>) this.treeItem_ProjectPrimitives8[0].getData();
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
                ArrayList<DatFile> cachedReferences = (ArrayList<DatFile>) this.treeItem_ProjectParts[0].getData();
                cachedReferences.add(df);
            }
            ti = new TreeItem(this.treeItem_ProjectParts[0], SWT.NONE);
            break;
            case SUBPART:
            {
                @SuppressWarnings("unchecked")
                ArrayList<DatFile> cachedReferences = (ArrayList<DatFile>) this.treeItem_ProjectSubparts[0].getData();
                cachedReferences.add(df);
            }
            ti = new TreeItem(this.treeItem_ProjectSubparts[0], SWT.NONE);
            break;
            case PRIMITIVE:
            {
                @SuppressWarnings("unchecked")
                ArrayList<DatFile> cachedReferences = (ArrayList<DatFile>) this.treeItem_ProjectPrimitives[0].getData();
                cachedReferences.add(df);
            }
            ti = new TreeItem(this.treeItem_ProjectPrimitives[0], SWT.NONE);
            break;
            case PRIMITIVE48:
            {
                @SuppressWarnings("unchecked")
                ArrayList<DatFile> cachedReferences = (ArrayList<DatFile>) this.treeItem_ProjectPrimitives48[0].getData();
                cachedReferences.add(df);
            }
            ti = new TreeItem(this.treeItem_ProjectPrimitives48[0], SWT.NONE);
            break;
            case PRIMITIVE8:
            {
                @SuppressWarnings("unchecked")
                ArrayList<DatFile> cachedReferences = (ArrayList<DatFile>) this.treeItem_ProjectPrimitives8[0].getData();
                cachedReferences.add(df);
            }
            ti = new TreeItem(this.treeItem_ProjectPrimitives8[0], SWT.NONE);
            break;
            default:
            {
                @SuppressWarnings("unchecked")
                ArrayList<DatFile> cachedReferences = (ArrayList<DatFile>) this.treeItem_ProjectParts[0].getData();
                cachedReferences.add(df);
            }
            ti = new TreeItem(this.treeItem_ProjectParts[0], SWT.NONE);
            break;
            }

            StringBuilder nameSb = new StringBuilder(new File(df.getNewName()).getName());

            nameSb.append("(new file)"); //$NON-NLS-1$ I18N

            ti.setText(nameSb.toString());
            ti.setData(df);

            updateTree_unsavedEntries();

            openDatFile(df, where, null);
            return df;
        }
        return null;
    }

    public boolean openDatFile(DatFile df, OpenInWhat where, EditorTextWindow tWin) {
        if (where == OpenInWhat.EDITOR_3D || where == OpenInWhat.EDITOR_TEXT_AND_3D) {
            if (renders.isEmpty()) {
                if ("%EMPTY%".equals(Editor3DWindow.getSashForm().getChildren()[1].getData())) { //$NON-NLS-1$
                    int[] mainSashWeights = Editor3DWindow.getSashForm().getWeights();
                    Editor3DWindow.getSashForm().getChildren()[1].dispose();
                    CompositeContainer cmp_Container = new CompositeContainer(Editor3DWindow.getSashForm(), false);
                    cmp_Container.moveBelow(Editor3DWindow.getSashForm().getChildren()[0]);
                    df.parseForData(true);
                    final VertexManager vm = df.getVertexManager();
                    Project.setFileToEdit(df);
                    cmp_Container.getComposite3D().setLockableDatFileReference(df);
                    vm.zoomToFit(cmp_Container.getComposite3D());
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
                if (canUpdate) {
                    final VertexManager vm = df.getVertexManager();
                    if (vm.isModified()) {
                        df.setText(df.getText());
                    }
                    df.parseForData(true);
                    Project.setFileToEdit(df);
                    for (OpenGLRenderer renderer : renders) {
                        Composite3D c3d = renderer.getC3D();
                        if (!c3d.isDatFileLockedOnDisplay()) {
                            c3d.setLockableDatFileReference(df);
                            vm.zoomToFit(c3d);
                        }
                    }
                }
            }
        }

        if (where == OpenInWhat.EDITOR_TEXT || where == OpenInWhat.EDITOR_TEXT_AND_3D) {
            for (EditorTextWindow w : Project.getOpenTextWindows()) {

                for (CTabItem t : w.getTabFolder().getItems()) {
                    if (df.equals(((CompositeTab) t).getState().getFileNameObj())) {
                        w.getTabFolder().setSelection(t);
                        ((CompositeTab) t).getControl().getShell().forceActive();
                        w.open();
                        return w == tWin;
                    }
                }
            }
            if (tWin == null) {
                // Project.getParsedFiles().add(df); IS NECESSARY HERE
                Project.getParsedFiles().add(df);
                new EditorTextWindow().run(df);
            }
        }
        return false;
    }

    public void disableSelectionTab() {
        updatingSelectionTab = true;
        txt_Line[0].setText(""); //$NON-NLS-1$
        spn_SelectionX1[0].setEnabled(false);
        spn_SelectionY1[0].setEnabled(false);
        spn_SelectionZ1[0].setEnabled(false);
        spn_SelectionX2[0].setEnabled(false);
        spn_SelectionY2[0].setEnabled(false);
        spn_SelectionZ2[0].setEnabled(false);
        spn_SelectionX3[0].setEnabled(false);
        spn_SelectionY3[0].setEnabled(false);
        spn_SelectionZ3[0].setEnabled(false);
        spn_SelectionX4[0].setEnabled(false);
        spn_SelectionY4[0].setEnabled(false);
        spn_SelectionZ4[0].setEnabled(false);
        spn_SelectionX1[0].setValue(BigDecimal.ZERO);
        spn_SelectionY1[0].setValue(BigDecimal.ZERO);
        spn_SelectionZ1[0].setValue(BigDecimal.ZERO);
        spn_SelectionX2[0].setValue(BigDecimal.ZERO);
        spn_SelectionY2[0].setValue(BigDecimal.ZERO);
        spn_SelectionZ2[0].setValue(BigDecimal.ZERO);
        spn_SelectionX3[0].setValue(BigDecimal.ZERO);
        spn_SelectionY3[0].setValue(BigDecimal.ZERO);
        spn_SelectionZ3[0].setValue(BigDecimal.ZERO);
        spn_SelectionX4[0].setValue(BigDecimal.ZERO);
        spn_SelectionY4[0].setValue(BigDecimal.ZERO);
        spn_SelectionZ4[0].setValue(BigDecimal.ZERO);
        lbl_SelectionX1[0].setText(I18n.EDITOR3D_PositionX1);
        lbl_SelectionY1[0].setText(I18n.EDITOR3D_PositionY1);
        lbl_SelectionZ1[0].setText(I18n.EDITOR3D_PositionZ1);
        lbl_SelectionX2[0].setText(I18n.EDITOR3D_PositionX2);
        lbl_SelectionY2[0].setText(I18n.EDITOR3D_PositionY2);
        lbl_SelectionZ2[0].setText(I18n.EDITOR3D_PositionZ2);
        lbl_SelectionX3[0].setText(I18n.EDITOR3D_PositionX3);
        lbl_SelectionY3[0].setText(I18n.EDITOR3D_PositionY3);
        lbl_SelectionZ3[0].setText(I18n.EDITOR3D_PositionZ3);
        lbl_SelectionX4[0].setText(I18n.EDITOR3D_PositionX4);
        lbl_SelectionY4[0].setText(I18n.EDITOR3D_PositionY4);
        lbl_SelectionZ4[0].setText(I18n.EDITOR3D_PositionZ4);
        updatingSelectionTab = false;
    }

    public static ArrayList<OpenGLRenderer> getRenders() {
        return renders;
    }

    public SearchWindow getSearchWindow() {
        return searchWindow;
    }

    public void setSearchWindow(SearchWindow searchWindow) {
        this.searchWindow = searchWindow;
    }


    public SelectorSettings loadSelectorSettings()  {
        sels.setColour(mntm_WithSameColour[0].getSelection());
        sels.setEdgeStop(mntm_StopAtEdges[0].getSelection());
        sels.setHidden(mntm_WithHiddenData[0].getSelection());
        sels.setNoSubfiles(mntm_ExceptSubfiles[0].getSelection());
        sels.setOrientation(mntm_WithSameOrientation[0].getSelection());
        sels.setDistance(mntm_WithAccuracy[0].getSelection());
        sels.setWholeSubfiles(mntm_WithWholeSubfiles[0].getSelection());
        sels.setVertices(mntm_SVertices[0].getSelection());
        sels.setLines(mntm_SLines[0].getSelection());
        sels.setTriangles(mntm_STriangles[0].getSelection());
        sels.setQuads(mntm_SQuads[0].getSelection());
        sels.setCondlines(mntm_SCLines[0].getSelection());
        return sels;
    }

    private boolean isFileNameAllocated(String dir, DatFile df, boolean createNew) {

        TreeItem[] folders = new TreeItem[15];
        folders[0] = treeItem_OfficialParts[0];
        folders[1] = treeItem_OfficialPrimitives[0];
        folders[2] = treeItem_OfficialPrimitives8[0];
        folders[3] = treeItem_OfficialPrimitives48[0];
        folders[4] = treeItem_OfficialSubparts[0];

        folders[5] = treeItem_UnofficialParts[0];
        folders[6] = treeItem_UnofficialPrimitives[0];
        folders[7] = treeItem_UnofficialPrimitives8[0];
        folders[8] = treeItem_UnofficialPrimitives48[0];
        folders[9] = treeItem_UnofficialSubparts[0];

        folders[10] = treeItem_ProjectParts[0];
        folders[11] = treeItem_ProjectPrimitives[0];
        folders[12] = treeItem_ProjectPrimitives8[0];
        folders[13] = treeItem_ProjectPrimitives48[0];
        folders[14] = treeItem_ProjectSubparts[0];

        for (TreeItem folder : folders) {
            @SuppressWarnings("unchecked")
            ArrayList<DatFile> cachedReferences =(ArrayList<DatFile>) folder.getData();
            for (DatFile d : cachedReferences) {
                if (createNew || !df.equals(d)) {
                    if (dir.equals(d.getOldName()) || dir.equals(d.getNewName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private DatFile isFileNameAllocated2(String dir, DatFile df) {

        TreeItem[] folders = new TreeItem[15];
        folders[0] = treeItem_OfficialParts[0];
        folders[1] = treeItem_OfficialPrimitives[0];
        folders[2] = treeItem_OfficialPrimitives8[0];
        folders[3] = treeItem_OfficialPrimitives48[0];
        folders[4] = treeItem_OfficialSubparts[0];

        folders[5] = treeItem_UnofficialParts[0];
        folders[6] = treeItem_UnofficialPrimitives[0];
        folders[7] = treeItem_UnofficialPrimitives8[0];
        folders[8] = treeItem_UnofficialPrimitives48[0];
        folders[9] = treeItem_UnofficialSubparts[0];

        folders[10] = treeItem_ProjectParts[0];
        folders[11] = treeItem_ProjectPrimitives[0];
        folders[12] = treeItem_ProjectPrimitives8[0];
        folders[13] = treeItem_ProjectPrimitives48[0];
        folders[14] = treeItem_ProjectSubparts[0];

        for (TreeItem folder : folders) {
            @SuppressWarnings("unchecked")
            ArrayList<DatFile> cachedReferences =(ArrayList<DatFile>) folder.getData();
            for (DatFile d : cachedReferences) {
                if (dir.equals(d.getOldName()) || dir.equals(d.getNewName())) {
                    return d;
                }
            }
        }
        return null;
    }

    public void updatePrimitiveLabel(Primitive p) {
        if (lbl_selectedPrimitiveItem[0] == null) return;
        if (p == null) {
            lbl_selectedPrimitiveItem[0].setText("(no primitive selected)"); //$NON-NLS-1$ I18N Needs translation!
        } else {
            lbl_selectedPrimitiveItem[0].setText(p.toString());
        }
        lbl_selectedPrimitiveItem[0].getParent().layout();
    }

    public CompositePrimitive getCompositePrimitive() {
        return cmp_Primitives[0];
    }

    public static AtomicBoolean getAlive() {
        return alive;
    }

    public MenuItem getMntmWithSameColour() {
        return mntm_WithSameColour[0];
    }
}
