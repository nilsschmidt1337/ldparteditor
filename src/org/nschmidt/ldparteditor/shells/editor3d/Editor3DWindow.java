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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
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
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.opengl.GLCanvas;
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
import org.lwjgl.opengl.GL;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.csg.CSG;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.composites.CompositeContainer;
import org.nschmidt.ldparteditor.composites.CompositeScale;
import org.nschmidt.ldparteditor.composites.ToolItem;
import org.nschmidt.ldparteditor.composites.ToolSeparator;
import org.nschmidt.ldparteditor.composites.compositetab.CompositeTab;
import org.nschmidt.ldparteditor.composites.compositetab.CompositeTabFolder;
import org.nschmidt.ldparteditor.composites.primitive.CompositePrimitive;
import org.nschmidt.ldparteditor.data.BFC;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.DatType;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GData0;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.GData2;
import org.nschmidt.ldparteditor.data.GDataBFC;
import org.nschmidt.ldparteditor.data.GDataCSG;
import org.nschmidt.ldparteditor.data.GDataPNG;
import org.nschmidt.ldparteditor.data.GraphicalDataTools;
import org.nschmidt.ldparteditor.data.LibraryManager;
import org.nschmidt.ldparteditor.data.Matrix;
import org.nschmidt.ldparteditor.data.ParsingResult;
import org.nschmidt.ldparteditor.data.Primitive;
import org.nschmidt.ldparteditor.data.ProtractorHelper;
import org.nschmidt.ldparteditor.data.ReferenceParser;
import org.nschmidt.ldparteditor.data.RingsAndCones;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.dialogs.colour.ColourDialog;
import org.nschmidt.ldparteditor.dialogs.copy.CopyDialog;
import org.nschmidt.ldparteditor.dialogs.direction.DirectionDialog;
import org.nschmidt.ldparteditor.dialogs.edger2.EdgerDialog;
import org.nschmidt.ldparteditor.dialogs.intersector.IntersectorDialog;
import org.nschmidt.ldparteditor.dialogs.isecalc.IsecalcDialog;
import org.nschmidt.ldparteditor.dialogs.lines2pattern.Lines2PatternDialog;
import org.nschmidt.ldparteditor.dialogs.logupload.LogUploadDialog;
import org.nschmidt.ldparteditor.dialogs.meshreducer.MeshReducerDialog;
import org.nschmidt.ldparteditor.dialogs.newproject.NewProjectDialog;
import org.nschmidt.ldparteditor.dialogs.options.OptionsDialog;
import org.nschmidt.ldparteditor.dialogs.partreview.PartReviewDialog;
import org.nschmidt.ldparteditor.dialogs.pathtruder.PathTruderDialog;
import org.nschmidt.ldparteditor.dialogs.primgen2.PrimGen2Dialog;
import org.nschmidt.ldparteditor.dialogs.rectifier.RectifierDialog;
import org.nschmidt.ldparteditor.dialogs.ringsandcones.RingsAndConesDialog;
import org.nschmidt.ldparteditor.dialogs.rotate.RotateDialog;
import org.nschmidt.ldparteditor.dialogs.round.RoundDialog;
import org.nschmidt.ldparteditor.dialogs.scale.ScaleDialog;
import org.nschmidt.ldparteditor.dialogs.selectvertex.VertexDialog;
import org.nschmidt.ldparteditor.dialogs.setcoordinates.CoordinatesDialog;
import org.nschmidt.ldparteditor.dialogs.slantingmatrixprojector.SlantingMatrixProjectorDialog;
import org.nschmidt.ldparteditor.dialogs.slicerpro.SlicerProDialog;
import org.nschmidt.ldparteditor.dialogs.smooth.SmoothDialog;
import org.nschmidt.ldparteditor.dialogs.symsplitter.SymSplitterDialog;
import org.nschmidt.ldparteditor.dialogs.tjunction.TJunctionDialog;
import org.nschmidt.ldparteditor.dialogs.translate.TranslateDialog;
import org.nschmidt.ldparteditor.dialogs.txt2dat.Txt2DatDialog;
import org.nschmidt.ldparteditor.dialogs.unificator.UnificatorDialog;
import org.nschmidt.ldparteditor.dialogs.value.ValueDialog;
import org.nschmidt.ldparteditor.dialogs.value.ValueDialogInt;
import org.nschmidt.ldparteditor.dialogs.ytruder.YTruderDialog;
import org.nschmidt.ldparteditor.enums.GL20Primitives;
import org.nschmidt.ldparteditor.enums.IconSize;
import org.nschmidt.ldparteditor.enums.ManipulatorAxisMode;
import org.nschmidt.ldparteditor.enums.ManipulatorScope;
import org.nschmidt.ldparteditor.enums.MergeTo;
import org.nschmidt.ldparteditor.enums.MouseButton;
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.enums.ObjectMode;
import org.nschmidt.ldparteditor.enums.OpenInWhat;
import org.nschmidt.ldparteditor.enums.Perspective;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.enums.TransformationMode;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.enums.WorkingMode;
import org.nschmidt.ldparteditor.helpers.Cocoa;
import org.nschmidt.ldparteditor.helpers.FileHelper;
import org.nschmidt.ldparteditor.helpers.Manipulator;
import org.nschmidt.ldparteditor.helpers.ShellHelper;
import org.nschmidt.ldparteditor.helpers.SphereGL20;
import org.nschmidt.ldparteditor.helpers.Version;
import org.nschmidt.ldparteditor.helpers.WidgetSelectionHelper;
import org.nschmidt.ldparteditor.helpers.composite3d.Edger2Settings;
import org.nschmidt.ldparteditor.helpers.composite3d.GuiStatusManager;
import org.nschmidt.ldparteditor.helpers.composite3d.IntersectorSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.IsecalcSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.MeshReducerSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.PathTruderSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.RectifierSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.RingsAndConesSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.SelectorSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.SlantingMatrixProjectorSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.SlicerProSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.SymSplitterSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.TJunctionSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.TreeData;
import org.nschmidt.ldparteditor.helpers.composite3d.Txt2DatSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.UnificatorSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.ViewIdleManager;
import org.nschmidt.ldparteditor.helpers.composite3d.YTruderSettings;
import org.nschmidt.ldparteditor.helpers.compositetext.ProjectActions;
import org.nschmidt.ldparteditor.helpers.compositetext.SubfileCompiler;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.helpers.math.MatrixOperations;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.ldraworg.CategoriesUtils;
import org.nschmidt.ldparteditor.ldraworg.LDConfigUtils;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.resources.ResourceManager;
import org.nschmidt.ldparteditor.shells.editormeta.EditorMetaWindow;
import org.nschmidt.ldparteditor.shells.editortext.EditorTextWindow;
import org.nschmidt.ldparteditor.shells.searchnreplace.SearchWindow;
import org.nschmidt.ldparteditor.text.DatParser;
import org.nschmidt.ldparteditor.text.LDParsingException;
import org.nschmidt.ldparteditor.text.References;
import org.nschmidt.ldparteditor.text.StringHelper;
import org.nschmidt.ldparteditor.text.TextTriangulator;
import org.nschmidt.ldparteditor.text.UTF8BufferedReader;
import org.nschmidt.ldparteditor.text.UTF8PrintWriter;
import org.nschmidt.ldparteditor.vertexwindow.VertexWindow;
import org.nschmidt.ldparteditor.widgets.BigDecimalSpinner;
import org.nschmidt.ldparteditor.widgets.NButton;
import org.nschmidt.ldparteditor.widgets.TreeItem;
import org.nschmidt.ldparteditor.widgets.ValueChangeAdapter;
import org.nschmidt.ldparteditor.win32openWith.TryToOpen;
import org.nschmidt.ldparteditor.workbench.Composite3DState;
import org.nschmidt.ldparteditor.workbench.Editor3DWindowState;
import org.nschmidt.ldparteditor.workbench.UserSettingState;
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

    public static final ArrayList<GLCanvas> canvasList = new ArrayList<GLCanvas>();
    public static final ArrayList<OpenGLRenderer> renders = new ArrayList<OpenGLRenderer>();

    public static int sashWeight1 = 50;
    public static int sashWeight2 = 50;

    final private static AtomicBoolean alive = new AtomicBoolean(true);
    final private static AtomicBoolean no_sync_deadlock = new AtomicBoolean(false);

    private boolean addingSomething = false;
    private boolean addingVertices = false;
    private boolean addingLines = false;
    private boolean addingTriangles = false;
    private boolean addingQuads = false;
    private boolean addingCondlines = false;
    private boolean addingDistance = false;
    private boolean addingProtractor = false;
    private boolean addingSubfiles = false;
    private boolean movingAdjacentData = WorkbenchManager.getUserSettingState().isMovingAdjacentData();
    private boolean noTransparentSelection = false;
    private boolean bfcToggle = false;
    private boolean insertingAtCursorPosition = false;
    private boolean reviewingAPart = false;
    private ObjectMode workingType = ObjectMode.VERTICES;
    private WorkingMode workingAction = WorkingMode.SELECT;
    private ManipulatorAxisMode workingLayer = ManipulatorAxisMode.NONE;

    private GColour lastUsedColour = View.getLDConfigColour(16);

    private ManipulatorScope transformationMode = ManipulatorScope.LOCAL;

    private int snapSize = 1;

    private Txt2DatSettings ts = new Txt2DatSettings();
    private Edger2Settings es = new Edger2Settings();
    private RectifierSettings rs = new RectifierSettings();
    private IsecalcSettings is = new IsecalcSettings();
    private SlicerProSettings ss = new SlicerProSettings();
    private IntersectorSettings ins = new IntersectorSettings();
    private PathTruderSettings ps = new PathTruderSettings();
    private YTruderSettings ys = new YTruderSettings();
    private SymSplitterSettings sims = new SymSplitterSettings();
    private UnificatorSettings us = new UnificatorSettings();
    private RingsAndConesSettings ris = new RingsAndConesSettings();
    private SelectorSettings sels = new SelectorSettings();
    private TJunctionSettings tjs = new TJunctionSettings();
    private MeshReducerSettings ms = new MeshReducerSettings();
    private SlantingMatrixProjectorSettings mps = new SlantingMatrixProjectorSettings();

    private boolean updatingPngPictureTab;
    private int pngPictureUpdateCounter = 0;

    private final EditorMetaWindow metaWindow = new EditorMetaWindow();
    private boolean updatingSelectionTab = true;

    private ArrayList<String> recentItems = new ArrayList<String>();

    private HashMap<DatFile, HashMap<Composite3D, org.nschmidt.ldparteditor.composites.Composite3DViewState>> c3dStates = new HashMap<>();

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
        View.overrideColour16();
        // Load recent files
        recentItems = WorkbenchManager.getUserSettingState().getRecentItems();
        if (recentItems == null) recentItems = new ArrayList<String>();
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
        sh.getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
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
            }
        });
        // Set the snapping
        Manipulator.setSnap(
                WorkbenchManager.getUserSettingState().getMedium_move_snap(),
                WorkbenchManager.getUserSettingState().getMedium_rotate_snap(),
                WorkbenchManager.getUserSettingState().getMedium_scale_snap()
                );
        // MARK All final listeners will be configured here..
        NLogger.writeVersion();
        sh.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {
                if (vertexWindow.getShell() != null) {
                    vertexWindow.close();
                }
            }
            @Override
            public void focusGained(FocusEvent e) {
                regainFocus();
                VertexWindow.placeVertexWindow();
            }
        });
        sh.addListener(SWT.Move, (event) -> {
            VertexWindow.placeVertexWindow();
        });
        sh.addListener(SWT.Resize, (event) -> {
            VertexWindow.placeVertexWindow();
        });
        tabFolder_Settings[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                regainFocus();
            }
        });

        SelectionAdapter sa = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int[] w = leftSash[0].getWeights();
                if (w[0] == 800 && w[1] == 100 && w[2] == 100) {
                    leftSash[0].setWeights(new int[]{33, 33, 33});
                } else {
                    leftSash[0].setWeights(new int[]{80, 10, 10});
                }
            }
        };

        btn_showUpper1[0].addSelectionListener(sa);
        btn_showUpper2[0].addSelectionListener(sa);
        btn_showUpper3[0].addSelectionListener(sa);

        btn_showMiddle[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int[] w = leftSash[0].getWeights();
                if (w[0] == 100 && w[1] == 800 && w[2] == 100) {
                    leftSash[0].setWeights(new int[]{33, 33, 33});
                } else {
                    leftSash[0].setWeights(new int[]{10, 80, 10});
                }
            }
        });

        btn_sameHeight[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                leftSash[0].setWeights(new int[]{33, 33, 33});
            }
        });

        btn_showLower[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int[] w = leftSash[0].getWeights();
                if (w[0] == 100 && w[1] == 100 && w[2] == 800) {
                    leftSash[0].setWeights(new int[]{33, 33, 33});
                } else {
                    leftSash[0].setWeights(new int[]{10, 10, 80});
                }
            }
        });

        if (btn_showLeft[0] != null) btn_showLeft[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                final SashForm sf = splitSash[0];
                int[] w = sf.getWeights();
                if (w[1] * 9 > w[0]) {
                    sf.setWeights(new int[]{95, 5});
                } else {
                    sf.setWeights(new int[]{Editor3DWindow.sashWeight1, Editor3DWindow.sashWeight2});
                }
            }
        });

        if (btn_showRight[0] != null) btn_showRight[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                final SashForm sf = splitSash[0];
                int[] w = sf.getWeights();
                if (w[0] * 9 > w[1]) {
                    sf.setWeights(new int[]{5, 95});
                } else {
                    sf.setWeights(new int[]{Editor3DWindow.sashWeight1, Editor3DWindow.sashWeight2});
                }
            }
        });

        if (btn_sameWidth[0] != null) btn_sameWidth[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                splitSash[0].setWeights(new int[]{50, 50});
            }
        });

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
            public void controlMoved(ControlEvent e) {}
        });


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
                treeParts[0].getTree().showItem(treeParts[0].getTree().getItem(0));

                MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
                messageBox.setText(I18n.DIALOG_SyncTitle);

                Object[] messageArguments = {additions, deletions, conflicts};
                MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                formatter.setLocale(MyLanguage.LOCALE);
                formatter.applyPattern(I18n.DIALOG_Sync);
                messageBox.setMessage(formatter.format(messageArguments));

                messageBox.open();
                regainFocus();
            }
        });

        btn_LastOpen[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                Menu lastOpenedMenu = new Menu(treeParts[0].getTree());
                btn_LastOpen[0].setMenu(lastOpenedMenu);

                final int size = recentItems.size() - 1;
                for (int i = size; i > -1; i--) {
                    final String path = recentItems.get(i);
                    File f = new File(path);
                    if (f.exists() && f.canRead()) {
                        if (f.isFile()) {
                            MenuItem mntmItem = new MenuItem(lastOpenedMenu, I18n.I18N_NON_BIDIRECT());
                            mntmItem.setEnabled(true);
                            mntmItem.setText(path);
                            mntmItem.addSelectionListener(new SelectionAdapter() {
                                @Override
                                public void widgetSelected(SelectionEvent e) {
                                    File f = new File(path);
                                    if (f.exists() && f.isFile() && f.canRead()) {
                                        DatFile df = openDatFile(getShell(), OpenInWhat.EDITOR_3D, path, false);
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
                                }
                            });
                        } else if (f.isDirectory()) {
                            MenuItem mntmItem = new MenuItem(lastOpenedMenu, I18n.I18N_NON_BIDIRECT());
                            mntmItem.setEnabled(true);

                            Object[] messageArguments = {path};
                            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                            formatter.setLocale(MyLanguage.LOCALE);
                            formatter.applyPattern(I18n.E3D_LastProject);

                            mntmItem.setText(formatter.format(messageArguments));
                            mntmItem.addSelectionListener(new SelectionAdapter() {
                                @Override
                                public void widgetSelected(SelectionEvent e) {
                                    File f = new File(path);
                                    if (f.exists() && f.isDirectory() && f.canRead() && ProjectActions.openProject(path)) {
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
                                    regainFocus();
                                }
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
            }
        });
        btn_New[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (ProjectActions.createNewProject(Editor3DWindow.getWindow(), false)) {
                    addRecentFile(Project.getProjectPath());
                }
                regainFocus();
            }
        });
        btn_Open[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (ProjectActions.openProject(null)) {
                    addRecentFile(Project.getProjectPath());
                    Project.setLastVisitedPath(Project.getProjectPath());
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
                regainFocus();
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
                                Editor3DWindow.getWindow().addRecentFile(df);
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
                                        Editor3DWindow.getWindow().addRecentFile(df);
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
                                if (ProjectActions.createNewProject(Editor3DWindow.getWindow(), true)) {
                                    Project.setLastVisitedPath(Project.getProjectPath());
                                }
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
                        NLogger.debug(getClass(), "Saving all files from this group to {0}", treeParts[0].getSelection()[0].getData()); //$NON-NLS-1$
                    }
                } else {
                    NLogger.debug(getClass(), "Save the project..."); //$NON-NLS-1$
                    if (Project.isDefaultProject()) {
                        if (ProjectActions.createNewProject(Editor3DWindow.getWindow(), true)) {
                            Project.setLastVisitedPath(Project.getProjectPath());
                        }
                    }
                }
                regainFocus();
            }

            private void iterateOverItems(TreeItem ti) {
                {
                    @SuppressWarnings("unchecked")
                    ArrayList<DatFile> dfs = (ArrayList<DatFile>) ti.getData();
                    for (DatFile df : dfs) {
                        if (!df.isReadOnly() && Project.getUnsavedFiles().contains(df)) {
                            if (df.save()) {
                                Editor3DWindow.getWindow().addRecentFile(df);
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
                            Editor3DWindow.getWindow().addRecentFile(df);
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
                    if (ProjectActions.createNewProject(getWindow(), true)) {
                        addRecentFile(Project.getProjectPath());
                    }
                }
                Editor3DWindow.getWindow().updateTree_unsavedEntries();
                regainFocus();
            }
        });

        btn_NewDat[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DatFile dat = createNewDatFile(getShell(), OpenInWhat.EDITOR_TEXT_AND_3D);
                if (dat != null) {
                    addRecentFile(dat);
                    final File f = new File(dat.getNewName());
                    if (f.getParentFile() != null) {
                        Project.setLastVisitedPath(f.getParentFile().getAbsolutePath());
                    }
                }
                regainFocus();
            }
        });

        btn_OpenDat[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean tabSync = WorkbenchManager.getUserSettingState().isSyncingTabs();
                WorkbenchManager.getUserSettingState().setSyncingTabs(false);
                DatFile dat = openDatFile(getShell(), OpenInWhat.EDITOR_3D, null, true);
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
                    updateTabs();
                }
                WorkbenchManager.getUserSettingState().setSyncingTabs(tabSync);
                regainFocus();
            }
        });

        btn_SaveDat[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null && !Project.getFileToEdit().equals(View.DUMMY_DATFILE)) {
                    final DatFile df = Project.getFileToEdit();
                    Editor3DWindow.getWindow().addRecentFile(df);
                    if (!df.isReadOnly() && Project.getUnsavedFiles().contains(df)) {
                        if (df.save()) {
                            Editor3DWindow.getWindow().addRecentFile(df);
                            Editor3DWindow.getWindow().updateTree_unsavedEntries();
                        } else {
                            MessageBox messageBoxError = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                            messageBoxError.setText(I18n.DIALOG_Error);
                            messageBoxError.setMessage(I18n.DIALOG_CantSaveFile);
                        }
                    }
                }
                regainFocus();
            }
        });

        btn_SaveAsDat[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null && !Project.getFileToEdit().equals(View.DUMMY_DATFILE)) {
                    final DatFile df2 = Project.getFileToEdit();

                    FileDialog fd = new FileDialog(sh, SWT.SAVE);
                    fd.setText(I18n.E3D_SaveDatFileAs);
                    fd.setOverwrite(true);

                    {
                        File f = new File(df2.getNewName()).getParentFile();
                        if (f.exists()) {
                            fd.setFilterPath(f.getAbsolutePath());
                        } else {
                            fd.setFilterPath(Project.getLastVisitedPath());
                        }
                    }

                    String[] filterExt = { "*.dat", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
                    fd.setFilterExtensions(filterExt);
                    String[] filterNames = {I18n.E3D_LDrawSourceFile, I18n.E3D_AllFiles};
                    fd.setFilterNames(filterNames);

                    while (true) {
                        try {
                            String selected = fd.open();
                            if (selected != null) {

                                if (Editor3DWindow.getWindow().isFileNameAllocated(selected, new DatFile(selected), true)) {
                                    MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.RETRY | SWT.CANCEL);
                                    messageBox.setText(I18n.DIALOG_AlreadyAllocatedNameTitle);
                                    messageBox.setMessage(I18n.DIALOG_AlreadyAllocatedName);

                                    int result = messageBox.open();

                                    if (result == SWT.CANCEL) {
                                        break;
                                    } else if (result == SWT.RETRY) {
                                        continue;
                                    }
                                }

                                df2.saveAs(selected);

                                DatFile df = Editor3DWindow.getWindow().openDatFile(getShell(), OpenInWhat.EDITOR_3D, selected, false);
                                if (df != null) {
                                    Editor3DWindow.getWindow().addRecentFile(df);
                                    final File f = new File(df.getNewName());
                                    if (f.getParentFile() != null) {
                                        Project.setLastVisitedPath(f.getParentFile().getAbsolutePath());
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
            }
        });

        btn_Undo[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null) {
                    Project.getFileToEdit().getVertexManager().addSnapshot();
                    Project.getFileToEdit().undo(null, false);
                }
                regainFocus();
            }
        });

        btn_Redo[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null) {
                    Project.getFileToEdit().getVertexManager().addSnapshot();
                    Project.getFileToEdit().redo(null, false);
                }
                regainFocus();
            }
        });

        if (NLogger.DEBUG) {
            btn_AddHistory[0].addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (Project.getFileToEdit() != null) {
                        Project.getFileToEdit().getVertexManager().addSnapshot();
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
                disableAddAction();
                regainFocus();
            }
        });
        btn_Move[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                clickBtnTest(btn_Move[0]);
                workingAction = WorkingMode.MOVE;
                disableAddAction();
                regainFocus();
            }
        });
        btn_Rotate[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                clickBtnTest(btn_Rotate[0]);
                workingAction = WorkingMode.ROTATE;
                disableAddAction();
                regainFocus();
            }
        });
        btn_Scale[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                clickBtnTest(btn_Scale[0]);
                workingAction = WorkingMode.SCALE;
                disableAddAction();
                regainFocus();
            }
        });
        btn_Combined[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                clickBtnTest(btn_Combined[0]);
                workingAction = WorkingMode.COMBINED;
                disableAddAction();
                regainFocus();
            }
        });

        btn_Local[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                clickBtnTest(btn_Local[0]);
                transformationMode = ManipulatorScope.LOCAL;
                regainFocus();
            }
        });
        btn_Global[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                clickBtnTest(btn_Global[0]);
                transformationMode = ManipulatorScope.GLOBAL;
                regainFocus();
            }
        });

        btn_Vertices[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                clickBtnTest(btn_Vertices[0]);
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
            }
        });
        btn_TrisNQuads[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                clickBtnTest(btn_TrisNQuads[0]);
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
            }
        });
        btn_Lines[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                clickBtnTest(btn_Lines[0]);
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
            }
        });
        btn_Subfiles[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                clickBtnTest(btn_Subfiles[0]);
                setWorkingType(ObjectMode.SUBFILES);
                if ((e.stateMask & SWT.ALT) == SWT.ALT && !Cocoa.checkCtrlOrCmdPressed(e.stateMask) && Project.getFileToEdit() != null && !Project.getFileToEdit().getVertexManager().getSelectedSubfiles().isEmpty()) {
                    final VertexManager vm = Project.getFileToEdit().getVertexManager();
                    final ArrayList<GData1> subfiles = new ArrayList<GData1>();
                    subfiles.addAll(vm.getSelectedSubfiles());
                    for (GData1 g1 : subfiles) {
                        vm.removeSubfileFromSelection(g1);
                    }
                    vm.getSelectedSubfiles().clear();
                    vm.setModified(true, true);
                }
                regainFocus();
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
                regainFocus();
            }
        });
        btn_AddPrimitive[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                resetAddState();
                setAddingSubfiles(btn_AddPrimitive[0].getSelection());

                clickSingleBtn(btn_AddPrimitive[0]);

                if (Project.getFileToEdit() != null) {
                    final boolean readOnly = Project.getFileToEdit().isReadOnly();
                    final VertexManager vm = Project.getFileToEdit().getVertexManager();

                    if (vm.getSelectedData().size() > 0 || vm.getSelectedVertices().size() > 0) {

                        final boolean insertSubfileFromSelection;
                        final boolean cutTheSelection;

                        {
                            MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
                            messageBox.setText(I18n.E3D_SubfileFromSelection);
                            messageBox.setMessage(I18n.E3D_SubfileFromSelectionQuestion);
                            int result = messageBox.open();
                            insertSubfileFromSelection = result == SWT.YES;
                            if (result != SWT.NO && result != SWT.YES) {
                                resetAddState();
                                btn_AddPrimitive[0].setSelection(false);
                                setAddingSubfiles(false);
                                addingSomething = false;
                                regainFocus();
                                return;
                            }
                        }

                        if (insertSubfileFromSelection) {
                            if (!readOnly) {
                                MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
                                messageBox.setText(I18n.E3D_SubfileFromSelection);
                                messageBox.setMessage(I18n.E3D_SubfileFromSelectionQuestionCut);
                                int result = messageBox.open();
                                cutTheSelection = result == SWT.YES;
                                if (result != SWT.NO && result != SWT.YES) {
                                    resetAddState();
                                    btn_AddPrimitive[0].setSelection(false);
                                    setAddingSubfiles(false);
                                    addingSomething = false;
                                    regainFocus();
                                    return;
                                }
                            } else {
                                cutTheSelection = false;
                            }

                            vm.addSnapshot();
                            vm.copy();
                            vm.extendClipboardContent(cutTheSelection);

                            FileDialog fd = new FileDialog(sh, SWT.SAVE);
                            fd.setText(I18n.E3D_SaveDatFileAs);

                            {
                                File f = new File(Project.getFileToEdit().getNewName()).getParentFile();
                                if (f.exists()) {
                                    fd.setFilterPath(f.getAbsolutePath());
                                } else {
                                    fd.setFilterPath(Project.getLastVisitedPath());
                                }
                            }

                            String[] filterExt = { "*.dat", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
                            fd.setFilterExtensions(filterExt);
                            String[] filterNames = {I18n.E3D_LDrawSourceFile, I18n.E3D_AllFiles};
                            fd.setFilterNames(filterNames);

                            while (true) {
                                try {
                                    String selected = fd.open();
                                    if (selected != null) {

                                        if (Editor3DWindow.getWindow().isFileNameAllocated(selected, new DatFile(selected), true)) {
                                            MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.RETRY | SWT.CANCEL);
                                            messageBox.setText(I18n.DIALOG_AlreadyAllocatedNameTitle);
                                            messageBox.setMessage(I18n.DIALOG_AlreadyAllocatedName);

                                            int result = messageBox.open();

                                            if (result == SWT.CANCEL) {
                                                break;
                                            } else if (result == SWT.RETRY) {
                                                continue;
                                            }
                                        }

                                        SearchWindow sw = Editor3DWindow.getWindow().getSearchWindow();
                                        if (sw != null) {
                                            sw.setTextComposite(null);
                                            sw.setScopeToAll();
                                        }


                                        boolean hasIOerror = false;
                                        UTF8PrintWriter r = null;
                                        try {

                                            String typeSuffix = ""; //$NON-NLS-1$
                                            String folderPrefix = ""; //$NON-NLS-1$
                                            String subfilePrefix = ""; //$NON-NLS-1$
                                            String path = new File(selected).getParent();

                                            if (path.endsWith(File.separator + "S") || path.endsWith(File.separator + "s")) { //$NON-NLS-1$ //$NON-NLS-2$
                                                typeSuffix = "Unofficial_Subpart"; //$NON-NLS-1$
                                                folderPrefix = "s\\"; //$NON-NLS-1$
                                                subfilePrefix = "~"; //$NON-NLS-1$
                                            } else if (path.endsWith(File.separator + "P" + File.separator + "48") || path.endsWith(File.separator + "p" + File.separator + "48")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                                typeSuffix = "Unofficial_48_Primitive"; //$NON-NLS-1$
                                                folderPrefix = "48\\"; //$NON-NLS-1$
                                            } else if (path.endsWith(File.separator + "P" + File.separator + "8") || path.endsWith(File.separator + "p" + File.separator + "8")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                                typeSuffix = "Unofficial_8_Primitive"; //$NON-NLS-1$
                                                folderPrefix = "8\\"; //$NON-NLS-1$
                                            } else if (path.endsWith(File.separator + "P") || path.endsWith(File.separator + "p")) { //$NON-NLS-1$ //$NON-NLS-2$
                                                typeSuffix = "Unofficial_Primitive"; //$NON-NLS-1$
                                            }

                                            r = new UTF8PrintWriter(selected);
                                            r.println("0 " + subfilePrefix); //$NON-NLS-1$
                                            r.println("0 Name: " + folderPrefix + new File(selected).getName()); //$NON-NLS-1$
                                            String ldrawName = WorkbenchManager.getUserSettingState().getLdrawUserName();
                                            if (ldrawName == null || ldrawName.isEmpty()) {
                                                r.println("0 Author: " + WorkbenchManager.getUserSettingState().getRealUserName()); //$NON-NLS-1$
                                            } else {
                                                r.println("0 Author: " + WorkbenchManager.getUserSettingState().getRealUserName() + " [" + WorkbenchManager.getUserSettingState().getLdrawUserName() + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                            }
                                            r.println("0 !LDRAW_ORG " + typeSuffix); //$NON-NLS-1$
                                            String license = WorkbenchManager.getUserSettingState().getLicense();
                                            if (license == null || license.isEmpty()) {
                                                r.println("0 !LICENSE Redistributable under CCAL version 2.0 : see CAreadme.txt"); //$NON-NLS-1$
                                            } else {
                                                r.println(license);
                                            }
                                            r.println(""); //$NON-NLS-1$

                                            {
                                                byte bfc_type = BFC.NOCERTIFY;
                                                GData g = Project.getFileToEdit().getDrawChainStart();
                                                while ((g = g.getNext()) != null) {
                                                    if (g.type() == 6) {
                                                        byte bfc = ((GDataBFC) g).getType();
                                                        switch (bfc) {
                                                        case BFC.CCW_CLIP:
                                                            bfc_type = bfc;
                                                            r.println("0 BFC CERTIFY CCW"); //$NON-NLS-1$
                                                            break;
                                                        case BFC.CW_CLIP:
                                                            bfc_type = bfc;
                                                            r.println("0 BFC CERTIFY CW"); //$NON-NLS-1$
                                                            break;
                                                        }
                                                        if (bfc_type != BFC.NOCERTIFY) break;
                                                    }
                                                }
                                                if (bfc_type == BFC.NOCERTIFY) {
                                                    r.println("0 BFC NOCERTIFY"); //$NON-NLS-1$
                                                }
                                            }
                                            r.println(""); //$NON-NLS-1$
                                            r.println(vm.getClipboardText());
                                            r.flush();
                                            r.close();
                                        } catch (Exception ex) {
                                            hasIOerror = true;
                                        } finally {
                                            if (r != null) {
                                                r.close();
                                            }
                                        }

                                        if (hasIOerror) {
                                            MessageBox messageBoxError = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                                            messageBoxError.setText(I18n.DIALOG_Error);
                                            messageBoxError.setMessage(I18n.DIALOG_CantSaveFile);
                                            messageBoxError.open();
                                        } else {

                                            if (cutTheSelection) {
                                                // Insert a reference to the subfile in the old file
                                                Set<String> alreadyParsed = new HashSet<String>();
                                                alreadyParsed.add(Project.getFileToEdit().getShortName());
                                                final GColour col16 = View.getLDConfigColour(16);
                                                ArrayList<ParsingResult> subfileLine = DatParser
                                                        .parseLine(
                                                                "1 16 0 0 0 1 0 0 0 1 0 0 0 1 s\\" + new File(selected).getName(), -1, 0, col16.getR(), col16.getG(), col16.getB(), 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, Project.getFileToEdit(), false, alreadyParsed, false); //$NON-NLS-1$
                                                GData1 gd1 = (GData1) subfileLine.get(0).getGraphicalData();
                                                if (gd1 != null) {
                                                    if (isInsertingAtCursorPosition()) {
                                                        Project.getFileToEdit().insertAfterCursor(gd1);
                                                    } else {
                                                        Set<GData> sd = vm.getSelectedData();
                                                        GData g = Project.getFileToEdit().getDrawChainStart();
                                                        GData whereToInsert = null;
                                                        while ((g = g.getNext()) != null) {
                                                            if (sd.contains(g)) {
                                                                whereToInsert = g.getBefore();
                                                                break;
                                                            }
                                                        }
                                                        if (whereToInsert == null) {
                                                            whereToInsert = Project.getFileToEdit().getDrawChainTail();
                                                        }
                                                        Project.getFileToEdit().insertAfter(whereToInsert, gd1);
                                                    }
                                                }
                                                vm.delete(false, true);
                                            }

                                            DatFile df = Editor3DWindow.getWindow().openDatFile(getShell(), OpenInWhat.EDITOR_TEXT_AND_3D, selected, false);
                                            if (df != null) {
                                                addRecentFile(df);
                                                final File f = new File(df.getNewName());
                                                if (f.getParentFile() != null) {
                                                    Project.setLastVisitedPath(f.getParentFile().getAbsolutePath());
                                                }
                                            }
                                        }
                                        updateTree_unsavedEntries();
                                    }
                                } catch (Exception ex) {
                                    NLogger.error(getClass(), ex);
                                }
                                break;
                            }
                            resetAddState();
                            btn_AddPrimitive[0].setSelection(false);
                            setAddingSubfiles(false);
                            addingSomething = false;
                            regainFocus();
                            return;
                        }
                    }
                }
                setAddingSomething(isAddingSubfiles());
                regainFocus();
            }
        });
        btn_AddLine[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                resetAddState();
                setAddingLines(btn_AddLine[0].getSelection());
                setAddingSomething(isAddingLines());
                clickSingleBtn(btn_AddLine[0]);
                regainFocus();
            }
        });
        btn_AddTriangle[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                resetAddState();
                setAddingTriangles(btn_AddTriangle[0].getSelection());
                setAddingSomething(isAddingTriangles());
                clickSingleBtn(btn_AddTriangle[0]);
                regainFocus();
            }
        });
        btn_AddQuad[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                resetAddState();
                setAddingQuads(btn_AddQuad[0].getSelection());
                setAddingSomething(isAddingQuads());
                clickSingleBtn(btn_AddQuad[0]);
                regainFocus();
            }
        });
        btn_AddCondline[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                resetAddState();
                setAddingCondlines(btn_AddCondline[0].getSelection());
                setAddingSomething(isAddingCondlines());
                clickSingleBtn(btn_AddCondline[0]);
                regainFocus();
            }
        });
        btn_AddDistance[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                resetAddState();
                setAddingDistance(btn_AddDistance[0].getSelection());
                setAddingSomething(isAddingDistance());
                clickSingleBtn(btn_AddDistance[0]);
                regainFocus();
            }
        });
        btn_AddProtractor[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                resetAddState();
                setAddingProtractor(btn_AddProtractor[0].getSelection());
                setAddingSomething(isAddingProtractor());
                clickSingleBtn(btn_AddProtractor[0]);
                regainFocus();
            }
        });
        btn_MoveAdjacentData[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                clickSingleBtn(btn_MoveAdjacentData[0]);
                setMovingAdjacentData(btn_MoveAdjacentData[0].getSelection());
                GuiStatusManager.updateStatus();
                regainFocus();
            }
        });
        btn_CompileSubfile[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null) {
                    Project.getFileToEdit().getVertexManager().addSnapshot();
                    SubfileCompiler.compile(Project.getFileToEdit(), false, false);
                }
                regainFocus();
            }
        });
        if (btn_ToggleLinesOpenGL[0] != null) btn_ToggleLinesOpenGL[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (btn_ToggleLinesOpenGL[0].getSelection()) {
                    View.edge_threshold = 5e6f;
                } else {
                    View.edge_threshold = 5e-6f;
                }
                regainFocus();
            }
        });
        btn_lineSize0[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setLineSize(GL20Primitives.SPHERE0, GL20Primitives.SPHERE_INV0, 0f, 0f, 0.01f, btn_lineSize0[0]);
                regainFocus();
            }
        });
        btn_lineSize1[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setLineSize(GL20Primitives.SPHERE1, GL20Primitives.SPHERE_INV1, 25f, .025f, 1f, btn_lineSize1[0]);
                regainFocus();
            }
        });
        btn_lineSize2[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setLineSize(GL20Primitives.SPHERE2, GL20Primitives.SPHERE_INV2, 50f, .050f, 2f, btn_lineSize2[0]);
                regainFocus();
            }
        });
        btn_lineSize3[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setLineSize(GL20Primitives.SPHERE3, GL20Primitives.SPHERE_INV3, 100f, .100f, 3f, btn_lineSize3[0]);
                regainFocus();
            }
        });
        btn_lineSize4[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setLineSize(GL20Primitives.SPHERE4, GL20Primitives.SPHERE_INV4, 200f, .200f, 4f, btn_lineSize4[0]);
                regainFocus();
            }
        });

        btn_ShowSelectionInTextEditor[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null) {
                    Composite3D.showSelectionInTextEditor(Project.getFileToEdit(), true);
                }
                regainFocus();
            }
        });

        btn_BFCswap[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null) {
                    Project.getFileToEdit().getVertexManager().addSnapshot();
                    Project.getFileToEdit().getVertexManager().backupHideShowState();
                    Project.getFileToEdit().getVertexManager().windingChangeSelection(true);
                }
                regainFocus();
            }
        });

        btn_RoundSelection[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null) {
                    if (Cocoa.checkCtrlOrCmdPressed(e.stateMask)) {
                        if (new RoundDialog(getShell()).open() == IDialogConstants.CANCEL_ID) return;
                    }
                    Project.getFileToEdit().getVertexManager().addSnapshot();
                    Project.getFileToEdit().getVertexManager().backupHideShowState();
                    UserSettingState userSettings = WorkbenchManager.getUserSettingState();
                    Project.getFileToEdit().getVertexManager()
                    .roundSelection(userSettings.getCoordsPrecision(), userSettings.getTransMatrixPrecision(), isMovingAdjacentData(), true, userSettings.isRoundX(), userSettings.isRoundY(), userSettings.isRoundZ());
                }
                regainFocus();
            }
        });

        btn_Pipette[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null) {
                    VertexManager vm = Project.getFileToEdit().getVertexManager();
                    vm.addSnapshot();
                    final GColour gColour2;
                    {
                        GColour gColour3 = vm.getRandomSelectedColour(lastUsedColour);
                        if (gColour3.getColourNumber() == 16) {
                            gColour2 = View.getLDConfigColour(16);
                        } else {
                            gColour2 = gColour3;
                        }
                        lastUsedColour = gColour2;
                    }
                    setLastUsedColour(gColour2);
                    btn_LastUsedColour[0].clearPaintListeners();
                    btn_LastUsedColour[0].clearSelectionListeners();
                    final int imgSize = IconSize.getImageSizeFromIconSize();
                    final Color col = SWTResourceManager.getColor((int) (gColour2.getR() * 255f), (int) (gColour2.getG() * 255f), (int) (gColour2.getB() * 255f));
                    final Point size = btn_LastUsedColour[0].computeSize(SWT.DEFAULT, SWT.DEFAULT);
                    final int x = Math.round(size.x / 5f);
                    final int y = Math.round(size.y / 5f);
                    final int w = Math.round(size.x * (3f / 5f));
                    final int h = Math.round(size.y * (3f / 5f));
                    int num = gColour2.getColourNumber();
                    btn_LastUsedColour[0].addPaintListener(new PaintListener() {
                        @Override
                        public void paintControl(PaintEvent e) {
                            e.gc.setBackground(col);
                            e.gc.fillRectangle(x, y, w, h);
                            if (gColour2.getA() >= .99f) {
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
                                int num = gColour2.getColourNumber();
                                if (!View.hasLDConfigColour(num)) {
                                    num = -1;
                                }
                                Project.getFileToEdit().getVertexManager().addSnapshot();
                                Project.getFileToEdit().getVertexManager().colourChangeSelection(num, gColour2.getR(), gColour2.getG(), gColour2.getB(), gColour2.getA(), true);
                            }
                            regainFocus();
                        }

                        @Override
                        public void widgetDefaultSelected(SelectionEvent e) {
                        }
                    });
                    if (num != -1) {

                        Object[] messageArguments = {num, View.getLDConfigColourName(num)};
                        MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                        formatter.setLocale(MyLanguage.LOCALE);
                        formatter.applyPattern(I18n.EDITORTEXT_Colour1);

                        btn_LastUsedColour[0].setToolTipText(formatter.format(messageArguments));
                    } else {
                        StringBuilder colourBuilder = new StringBuilder();
                        colourBuilder.append("0x2"); //$NON-NLS-1$
                        colourBuilder.append(MathHelper.toHex((int) (255f * gColour2.getR())).toUpperCase());
                        colourBuilder.append(MathHelper.toHex((int) (255f * gColour2.getG())).toUpperCase());
                        colourBuilder.append(MathHelper.toHex((int) (255f * gColour2.getB())).toUpperCase());

                        Object[] messageArguments = {colourBuilder.toString()};
                        MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                        formatter.setLocale(MyLanguage.LOCALE);
                        formatter.applyPattern(I18n.EDITORTEXT_Colour2);

                        btn_LastUsedColour[0].setToolTipText(formatter.format(messageArguments));
                    }
                    btn_LastUsedColour[0].redraw();
                }
                regainFocus();
            }
        });

        btn_Decolour[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null && !Project.getFileToEdit().isReadOnly()) {
                    VertexManager vm = Project.getFileToEdit().getVertexManager();
                    vm.addSnapshot();
                    vm.selectAll(new SelectorSettings(), true);
                    GDataCSG.clearSelection(Project.getFileToEdit());
                    GColour c = View.getLDConfigColour(16);
                    vm.colourChangeSelection(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), false);
                    vm.getSelectedData().removeAll(vm.getTriangles().keySet());
                    vm.getSelectedData().removeAll(vm.getQuads().keySet());
                    vm.getSelectedData().removeAll(vm.getSelectedSubfiles());
                    vm.getSelectedSubfiles().clear();
                    vm.getSelectedTriangles().removeAll(vm.getTriangles().keySet());
                    vm.getSelectedQuads().removeAll(vm.getQuads().keySet());
                    c = View.getLDConfigColour(24);
                    vm.colourChangeSelection(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), true);
                }
            }
        });

        initPaletteEvent();

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
                regainFocus();
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
                regainFocus();
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
                regainFocus();
            }
        });

        btn_Coarse[0].addListener(SWT.MouseDown, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (event.button == MouseButton.RIGHT) {

                    try {
                        if (btn_Coarse[0].getMenu() != null) {
                            btn_Coarse[0].getMenu().dispose();
                        }
                    } catch (Exception ex) {}

                    Menu gridMenu = new Menu(btn_Coarse[0]);
                    btn_Coarse[0].setMenu(gridMenu);
                    mnu_coarseMenu[0] = gridMenu;

                    MenuItem mntmGridCoarseDefault = new MenuItem(gridMenu, I18n.I18N_NON_BIDIRECT());
                    mntm_gridCoarseDefault[0] = mntmGridCoarseDefault;
                    mntmGridCoarseDefault.setEnabled(true);
                    mntmGridCoarseDefault.setText(I18n.E3D_GridCoarseDefault);

                    mntm_gridCoarseDefault[0].addSelectionListener(new SelectionAdapter() {
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            WorkbenchManager.getUserSettingState().setCoarse_move_snap(new BigDecimal("1")); //$NON-NLS-1$
                            WorkbenchManager.getUserSettingState().setCoarse_rotate_snap(new BigDecimal("90")); //$NON-NLS-1$
                            WorkbenchManager.getUserSettingState().setCoarse_scale_snap(new BigDecimal("2")); //$NON-NLS-1$
                            BigDecimal m = WorkbenchManager.getUserSettingState().getCoarse_move_snap();
                            BigDecimal r = WorkbenchManager.getUserSettingState().getCoarse_rotate_snap();
                            BigDecimal s = WorkbenchManager.getUserSettingState().getCoarse_scale_snap();
                            snapSize = 2;
                            spn_Move[0].setValue(m);
                            spn_Rotate[0].setValue(r);
                            spn_Scale[0].setValue(s);
                            Manipulator.setSnap(m, r, s);
                            btn_Coarse[0].setSelection(true);
                            btn_Medium[0].setSelection(false);
                            btn_Fine[0].setSelection(false);
                            regainFocus();
                        }
                    });

                    java.awt.Point b = java.awt.MouseInfo.getPointerInfo().getLocation();
                    final int x = (int) b.getX();
                    final int y = (int) b.getY();

                    Menu menu = mnu_coarseMenu[0];
                    menu.setLocation(x, y);
                    menu.setVisible(true);
                    regainFocus();
                }
            }
        });

        btn_Medium[0].addListener(SWT.MouseDown, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (event.button == MouseButton.RIGHT) {

                    try {
                        if (btn_Medium[0].getMenu() != null) {
                            btn_Medium[0].getMenu().dispose();
                        }
                    } catch (Exception ex) {}

                    Menu gridMenu = new Menu(btn_Medium[0]);
                    btn_Medium[0].setMenu(gridMenu);
                    mnu_mediumMenu[0] = gridMenu;

                    MenuItem mntmGridMediumDefault = new MenuItem(gridMenu, I18n.I18N_NON_BIDIRECT());
                    mntm_gridMediumDefault[0] = mntmGridMediumDefault;
                    mntmGridMediumDefault.setEnabled(true);
                    mntmGridMediumDefault.setText(I18n.E3D_GridMediumDefault);

                    mntm_gridMediumDefault[0].addSelectionListener(new SelectionAdapter() {
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            WorkbenchManager.getUserSettingState().setMedium_move_snap(new BigDecimal("0.01")); //$NON-NLS-1$
                            WorkbenchManager.getUserSettingState().setMedium_rotate_snap(new BigDecimal("11.25")); //$NON-NLS-1$
                            WorkbenchManager.getUserSettingState().setMedium_scale_snap(new BigDecimal("1.1")); //$NON-NLS-1$
                            BigDecimal m = WorkbenchManager.getUserSettingState().getMedium_move_snap();
                            BigDecimal r = WorkbenchManager.getUserSettingState().getMedium_rotate_snap();
                            BigDecimal s = WorkbenchManager.getUserSettingState().getMedium_scale_snap();
                            snapSize = 1;
                            spn_Move[0].setValue(m);
                            spn_Rotate[0].setValue(r);
                            spn_Scale[0].setValue(s);
                            Manipulator.setSnap(m, r, s);
                            btn_Coarse[0].setSelection(false);
                            btn_Medium[0].setSelection(true);
                            btn_Fine[0].setSelection(false);
                            regainFocus();
                        }
                    });

                    java.awt.Point b = java.awt.MouseInfo.getPointerInfo().getLocation();
                    final int x = (int) b.getX();
                    final int y = (int) b.getY();

                    Menu menu = mnu_mediumMenu[0];
                    menu.setLocation(x, y);
                    menu.setVisible(true);
                    regainFocus();
                }
            }
        });

        btn_Fine[0].addListener(SWT.MouseDown, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (event.button == MouseButton.RIGHT) {

                    try {
                        if (btn_Fine[0].getMenu() != null) {
                            btn_Fine[0].getMenu().dispose();
                        }
                    } catch (Exception ex) {}

                    Menu gridMenu = new Menu(btn_Fine[0]);
                    btn_Fine[0].setMenu(gridMenu);
                    mnu_fineMenu[0] = gridMenu;

                    MenuItem mntmGridFineDefault = new MenuItem(gridMenu, I18n.I18N_NON_BIDIRECT());
                    mntm_gridFineDefault[0] = mntmGridFineDefault;
                    mntmGridFineDefault.setEnabled(true);
                    mntmGridFineDefault.setText(I18n.E3D_GridFineDefault);

                    mntm_gridFineDefault[0].addSelectionListener(new SelectionAdapter() {
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            WorkbenchManager.getUserSettingState().setFine_move_snap(new BigDecimal("0.0001")); //$NON-NLS-1$
                            WorkbenchManager.getUserSettingState().setFine_rotate_snap(BigDecimal.ONE);
                            WorkbenchManager.getUserSettingState().setFine_scale_snap(new BigDecimal("1.001")); //$NON-NLS-1$
                            BigDecimal m = WorkbenchManager.getUserSettingState().getFine_move_snap();
                            BigDecimal r = WorkbenchManager.getUserSettingState().getFine_rotate_snap();
                            BigDecimal s = WorkbenchManager.getUserSettingState().getFine_scale_snap();
                            snapSize = 0;
                            spn_Move[0].setValue(m);
                            spn_Rotate[0].setValue(r);
                            spn_Scale[0].setValue(s);
                            Manipulator.setSnap(m, r, s);
                            btn_Coarse[0].setSelection(false);
                            btn_Medium[0].setSelection(false);
                            btn_Fine[0].setSelection(true);
                            regainFocus();
                        }
                    });

                    java.awt.Point b = java.awt.MouseInfo.getPointerInfo().getLocation();
                    final int x = (int) b.getX();
                    final int y = (int) b.getY();

                    Menu menu = mnu_fineMenu[0];
                    menu.setLocation(x, y);
                    menu.setVisible(true);
                    regainFocus();
                }
            }
        });

        btn_SplitQuad[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null && !Project.getFileToEdit().isReadOnly()) {
                    Project.getFileToEdit().getVertexManager().addSnapshot();
                    Project.getFileToEdit().getVertexManager().splitQuads(true);
                }
                regainFocus();
            }
        });

        btn_MergeQuad[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null && !Project.getFileToEdit().isReadOnly()) {
                    Project.getFileToEdit().getVertexManager().addSnapshot();
                    RectifierSettings rs = new RectifierSettings();
                    rs.setScope(1);
                    rs.setNoBorderedQuadToRectConversation(true);
                    Project.getFileToEdit().getVertexManager().rectify(rs, true, true);
                }
                regainFocus();
            }
        });

        btn_LineIntersection[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null && !Project.getFileToEdit().isReadOnly()) {
                    Project.getFileToEdit().getVertexManager().addSnapshot();
                    Project.getFileToEdit().getVertexManager().intersectionVerticesBetweenLines3D(true);
                }
                regainFocus();
            }
        });

        btn_CondlineToLine[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null && !Project.getFileToEdit().isReadOnly()) {
                    Project.getFileToEdit().getVertexManager().addSnapshot();
                    Project.getFileToEdit().getVertexManager().condlineToLine();
                }
                regainFocus();
            }
        });

        btn_LineToCondline[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null && !Project.getFileToEdit().isReadOnly()) {
                    Project.getFileToEdit().getVertexManager().addSnapshot();
                    Project.getFileToEdit().getVertexManager().lineToCondline();
                }
                regainFocus();
            }
        });

        btn_MoveOnLine[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
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

        spn_ScaleInitial[0].addValueChangeListener(new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
                Manipulator.setInitialScale(spn.getValue());
            }
        });

        btn_PreviousSelection[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
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
                                    spn_SelectionX4[0].setEnabled(true);
                                    spn_SelectionY4[0].setEnabled(true);
                                    spn_SelectionZ4[0].setEnabled(true);
                                case 3:
                                    spn_SelectionAngle[0].setEnabled(gdata.type() == 3 && !((org.nschmidt.ldparteditor.data.GData3) gdata).isTriangle);
                                    spn_SelectionX3[0].setEnabled(true);
                                    spn_SelectionY3[0].setEnabled(true);
                                    spn_SelectionZ3[0].setEnabled(true);
                                case 2:
                                    spn_SelectionLength[0].setEnabled(
                                            gdata.type() == 2 && !((org.nschmidt.ldparteditor.data.GData2) gdata).isLine
                                            || gdata.type() == 3 && !((org.nschmidt.ldparteditor.data.GData3) gdata).isTriangle);
                                    spn_SelectionX1[0].setEnabled(true);
                                    spn_SelectionY1[0].setEnabled(true);
                                    spn_SelectionZ1[0].setEnabled(true);
                                    spn_SelectionX2[0].setEnabled(true);
                                    spn_SelectionY2[0].setEnabled(true);
                                    spn_SelectionZ2[0].setEnabled(true);

                                    txt_Line[0].setText(gdata.toString());
                                    breakIt = true;
                                    btn_MoveAdjacentData2[0].setEnabled(true);
                                    switch (gdata.type()) {
                                    case 5:
                                        BigDecimal[] g5 = GraphicalDataTools.getPreciseCoordinates(gdata);
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
                                        BigDecimal[] g4 = GraphicalDataTools.getPreciseCoordinates(gdata);
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
                                        BigDecimal[] g3 = GraphicalDataTools.getPreciseCoordinates(gdata);
                                        if (spn_SelectionAngle[0].isEnabled()) {
                                            spn_SelectionAngle[0].setValue(new BigDecimal(((org.nschmidt.ldparteditor.data.GData3) gdata).getProtractorAngle()));
                                        } else {
                                            spn_SelectionAngle[0].setValue(BigDecimal.ZERO);
                                        }
                                        if (spn_SelectionLength[0].isEnabled()) {
                                            spn_SelectionLength[0].setValue(((org.nschmidt.ldparteditor.data.GData3) gdata).getProtractorLength());
                                        } else {
                                            spn_SelectionLength[0].setValue(BigDecimal.ONE);
                                        }
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
                                        BigDecimal[] g2 = GraphicalDataTools.getPreciseCoordinates(gdata);
                                        if (spn_SelectionLength[0].isEnabled()) {
                                            spn_SelectionLength[0].setValue(((org.nschmidt.ldparteditor.data.GData2) gdata).getLength());
                                        } else {
                                            spn_SelectionLength[0].setValue(BigDecimal.ONE);
                                        }
                                        spn_SelectionX1[0].setValue(g2[0]);
                                        spn_SelectionY1[0].setValue(g2[1]);
                                        spn_SelectionZ1[0].setValue(g2[2]);
                                        spn_SelectionX2[0].setValue(g2[3]);
                                        spn_SelectionY2[0].setValue(g2[4]);
                                        spn_SelectionZ2[0].setValue(g2[5]);
                                        break;
                                    case 1:
                                        vm.getSelectedVertices().clear();
                                        btn_MoveAdjacentData2[0].setSelection(false);
                                        btn_MoveAdjacentData2[0].setEnabled(false);
                                        GData1 g1 = (GData1) gdata;
                                        spn_SelectionX1[0].setValue(g1.getAccurateProductMatrix().M30);
                                        spn_SelectionY1[0].setValue(g1.getAccurateProductMatrix().M31);
                                        spn_SelectionZ1[0].setValue(g1.getAccurateProductMatrix().M32);
                                        spn_SelectionX2[0].setValue(g1.getAccurateProductMatrix().M00);
                                        spn_SelectionY2[0].setValue(g1.getAccurateProductMatrix().M01);
                                        spn_SelectionZ2[0].setValue(g1.getAccurateProductMatrix().M02);
                                        spn_SelectionX3[0].setValue(g1.getAccurateProductMatrix().M10);
                                        spn_SelectionY3[0].setValue(g1.getAccurateProductMatrix().M11);
                                        spn_SelectionZ3[0].setValue(g1.getAccurateProductMatrix().M12);
                                        spn_SelectionX4[0].setValue(g1.getAccurateProductMatrix().M20);
                                        spn_SelectionY4[0].setValue(g1.getAccurateProductMatrix().M21);
                                        spn_SelectionZ4[0].setValue(g1.getAccurateProductMatrix().M22);
                                        break;
                                    default:
                                        disableSelectionTab();
                                        updatingSelectionTab = true;
                                        break;
                                    }

                                    lbl_SelectionAngle[0].setText((gdata.type() != 1 ? I18n.E3D_ProtractorAngle : "") + " {" + spn_SelectionAngle[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    lbl_SelectionLength[0].setText((gdata.type() != 1 ? I18n.E3D_ProtractorLength : "") + " {" + spn_SelectionLength[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    lbl_SelectionX1[0].setText((gdata.type() != 1 ? I18n.E3D_PositionX1 : "") + " {" + spn_SelectionX1[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    lbl_SelectionY1[0].setText((gdata.type() != 1 ? I18n.E3D_PositionY1 : "") + " {" + spn_SelectionY1[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    lbl_SelectionZ1[0].setText((gdata.type() != 1 ? I18n.E3D_PositionZ1 : "") + " {" + spn_SelectionZ1[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    lbl_SelectionX2[0].setText((gdata.type() != 1 ? I18n.E3D_PositionX2 : "") + " {" + spn_SelectionX2[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    lbl_SelectionY2[0].setText((gdata.type() != 1 ? I18n.E3D_PositionY2 : "") + " {" + spn_SelectionY2[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    lbl_SelectionZ2[0].setText((gdata.type() != 1 ? I18n.E3D_PositionZ2 : "") + " {" + spn_SelectionZ2[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    lbl_SelectionX3[0].setText((gdata.type() != 1 ? I18n.E3D_PositionX3 : "") + " {" + spn_SelectionX3[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    lbl_SelectionY3[0].setText((gdata.type() != 1 ? I18n.E3D_PositionY3 : "") + " {" + spn_SelectionY3[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    lbl_SelectionZ3[0].setText((gdata.type() != 1 ? I18n.E3D_PositionZ3 : "") + " {" + spn_SelectionZ3[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    lbl_SelectionX4[0].setText((gdata.type() != 1 ? I18n.E3D_PositionX4 : "") + " {" + spn_SelectionX4[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    lbl_SelectionY4[0].setText((gdata.type() != 1 ? I18n.E3D_PositionY4 : "") + " {" + spn_SelectionY4[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    lbl_SelectionZ4[0].setText((gdata.type() != 1 ? I18n.E3D_PositionZ4 : "") + " {" + spn_SelectionZ4[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

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
                regainFocus();
            }
        });
        btn_NextSelection[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
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
                                    spn_SelectionX4[0].setEnabled(true);
                                    spn_SelectionY4[0].setEnabled(true);
                                    spn_SelectionZ4[0].setEnabled(true);
                                case 3:
                                    spn_SelectionAngle[0].setEnabled(gdata.type() == 3 && !((org.nschmidt.ldparteditor.data.GData3) gdata).isTriangle);
                                    spn_SelectionX3[0].setEnabled(true);
                                    spn_SelectionY3[0].setEnabled(true);
                                    spn_SelectionZ3[0].setEnabled(true);
                                case 2:
                                    spn_SelectionLength[0].setEnabled(
                                            gdata.type() == 2 && !((org.nschmidt.ldparteditor.data.GData2) gdata).isLine
                                            || gdata.type() == 3 && !((org.nschmidt.ldparteditor.data.GData3) gdata).isTriangle);
                                    spn_SelectionX1[0].setEnabled(true);
                                    spn_SelectionY1[0].setEnabled(true);
                                    spn_SelectionZ1[0].setEnabled(true);
                                    spn_SelectionX2[0].setEnabled(true);
                                    spn_SelectionY2[0].setEnabled(true);
                                    spn_SelectionZ2[0].setEnabled(true);

                                    txt_Line[0].setText(gdata.toString());
                                    breakIt = true;
                                    btn_MoveAdjacentData2[0].setEnabled(true);
                                    switch (gdata.type()) {
                                    case 5:
                                        BigDecimal[] g5 = GraphicalDataTools.getPreciseCoordinates(gdata);
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
                                        BigDecimal[] g4 = GraphicalDataTools.getPreciseCoordinates(gdata);
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
                                        BigDecimal[] g3 = GraphicalDataTools.getPreciseCoordinates(gdata);
                                        if (spn_SelectionAngle[0].isEnabled()) {
                                            spn_SelectionAngle[0].setValue(new BigDecimal(((org.nschmidt.ldparteditor.data.GData3) gdata).getProtractorAngle()));
                                        } else {
                                            spn_SelectionAngle[0].setValue(BigDecimal.ZERO);
                                        }
                                        if (spn_SelectionLength[0].isEnabled()) {
                                            spn_SelectionLength[0].setValue(((org.nschmidt.ldparteditor.data.GData3) gdata).getProtractorLength());
                                        } else {
                                            spn_SelectionLength[0].setValue(BigDecimal.ONE);
                                        }
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
                                        BigDecimal[] g2 = GraphicalDataTools.getPreciseCoordinates(gdata);
                                        if (spn_SelectionLength[0].isEnabled()) {
                                            spn_SelectionLength[0].setValue(((org.nschmidt.ldparteditor.data.GData2) gdata).getLength());
                                        } else {
                                            spn_SelectionLength[0].setValue(BigDecimal.ONE);
                                        }
                                        spn_SelectionX1[0].setValue(g2[0]);
                                        spn_SelectionY1[0].setValue(g2[1]);
                                        spn_SelectionZ1[0].setValue(g2[2]);
                                        spn_SelectionX2[0].setValue(g2[3]);
                                        spn_SelectionY2[0].setValue(g2[4]);
                                        spn_SelectionZ2[0].setValue(g2[5]);
                                        break;
                                    case 1:
                                        vm.getSelectedVertices().clear();
                                        btn_MoveAdjacentData2[0].setSelection(false);
                                        btn_MoveAdjacentData2[0].setEnabled(false);
                                        GData1 g1 = (GData1) gdata;
                                        spn_SelectionX1[0].setValue(g1.getAccurateProductMatrix().M30);
                                        spn_SelectionY1[0].setValue(g1.getAccurateProductMatrix().M31);
                                        spn_SelectionZ1[0].setValue(g1.getAccurateProductMatrix().M32);
                                        spn_SelectionX2[0].setValue(g1.getAccurateProductMatrix().M00);
                                        spn_SelectionY2[0].setValue(g1.getAccurateProductMatrix().M01);
                                        spn_SelectionZ2[0].setValue(g1.getAccurateProductMatrix().M02);
                                        spn_SelectionX3[0].setValue(g1.getAccurateProductMatrix().M10);
                                        spn_SelectionY3[0].setValue(g1.getAccurateProductMatrix().M11);
                                        spn_SelectionZ3[0].setValue(g1.getAccurateProductMatrix().M12);
                                        spn_SelectionX4[0].setValue(g1.getAccurateProductMatrix().M20);
                                        spn_SelectionY4[0].setValue(g1.getAccurateProductMatrix().M21);
                                        spn_SelectionZ4[0].setValue(g1.getAccurateProductMatrix().M22);
                                        break;
                                    default:
                                        disableSelectionTab();
                                        updatingSelectionTab = true;
                                        break;
                                    }

                                    lbl_SelectionAngle[0].setText((gdata.type() != 1 ? I18n.E3D_ProtractorAngle : "") + " {" + spn_SelectionAngle[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    lbl_SelectionLength[0].setText((gdata.type() != 1 ? I18n.E3D_ProtractorLength : "") + " {" + spn_SelectionLength[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    lbl_SelectionX1[0].setText((gdata.type() != 1 ? I18n.E3D_PositionX1 : "X  :") + " {" + spn_SelectionX1[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    lbl_SelectionY1[0].setText((gdata.type() != 1 ? I18n.E3D_PositionY1 : "Y  :") + " {" + spn_SelectionY1[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    lbl_SelectionZ1[0].setText((gdata.type() != 1 ? I18n.E3D_PositionZ1 : "Z  :") + " {" + spn_SelectionZ1[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    lbl_SelectionX2[0].setText((gdata.type() != 1 ? I18n.E3D_PositionX2 : "M00:") + " {" + spn_SelectionX2[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    lbl_SelectionY2[0].setText((gdata.type() != 1 ? I18n.E3D_PositionY2 : "M01:") + " {" + spn_SelectionY2[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    lbl_SelectionZ2[0].setText((gdata.type() != 1 ? I18n.E3D_PositionZ2 : "M02:") + " {" + spn_SelectionZ2[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    lbl_SelectionX3[0].setText((gdata.type() != 1 ? I18n.E3D_PositionX3 : "M10:") + " {" + spn_SelectionX3[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    lbl_SelectionY3[0].setText((gdata.type() != 1 ? I18n.E3D_PositionY3 : "M11:") + " {" + spn_SelectionY3[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    lbl_SelectionZ3[0].setText((gdata.type() != 1 ? I18n.E3D_PositionZ3 : "M12:") + " {" + spn_SelectionZ3[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    lbl_SelectionX4[0].setText((gdata.type() != 1 ? I18n.E3D_PositionX4 : "M20:") + " {" + spn_SelectionX4[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    lbl_SelectionY4[0].setText((gdata.type() != 1 ? I18n.E3D_PositionY4 : "M21:") + " {" + spn_SelectionY4[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    lbl_SelectionZ4[0].setText((gdata.type() != 1 ? I18n.E3D_PositionZ4 : "M22:") + " {" + spn_SelectionZ4[0].getStringValue() + "}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

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
                regainFocus();
            }
        });

        final ValueChangeAdapter va = new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
                if (updatingSelectionTab || Project.getFileToEdit() == null) return;
                Project.getFileToEdit().getVertexManager().addSnapshot();
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

                    if (newLine.type() == 2 && !((org.nschmidt.ldparteditor.data.GData2) newLine).isLine) {
                        updatingSelectionTab = true;
                        spn_SelectionLength[0].setValue(((org.nschmidt.ldparteditor.data.GData2) newLine).getLength());
                        updatingSelectionTab = false;
                    }
                    if (newLine.type() == 3 && !((org.nschmidt.ldparteditor.data.GData3) newLine).isTriangle) {
                        updatingSelectionTab = true;
                        spn_SelectionAngle[0].setValue(new BigDecimal(((org.nschmidt.ldparteditor.data.GData3) newLine).getProtractorAngle()));
                        spn_SelectionLength[0].setValue(((org.nschmidt.ldparteditor.data.GData3) newLine).getProtractorLength());
                        updatingSelectionTab = false;
                    }

                    txt_Line[0].setText(newLine.toString());
                }
            }
        };

        spn_SelectionAngle[0].addValueChangeListener(new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
                if (updatingSelectionTab || Project.getFileToEdit() == null) return;
                VertexManager vm = Project.getFileToEdit().getVertexManager();
                vm.addSnapshot();

                org.nschmidt.ldparteditor.data.GData3 tri = null;
                if (vm.getSelectedTriangles().size() == 0) {
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
                spn_SelectionX3[0].setValue(c[0]);
                spn_SelectionY3[0].setValue(c[1]);
                spn_SelectionZ3[0].setValue(c[2]);
                updatingSelectionTab = false;

                final GData newLine = Project.getFileToEdit().getVertexManager().updateSelectedLine(
                        spn_SelectionX1[0].getValue(), spn_SelectionY1[0].getValue(), spn_SelectionZ1[0].getValue(),
                        spn_SelectionX2[0].getValue(), spn_SelectionY2[0].getValue(), spn_SelectionZ2[0].getValue(),
                        c[0], c[1], c[2],
                        spn_SelectionX4[0].getValue(), spn_SelectionY4[0].getValue(), spn_SelectionZ4[0].getValue(),
                        btn_MoveAdjacentData2[0].getSelection()
                        );
                if (newLine == null) {
                    disableSelectionTab();
                } else {
                    txt_Line[0].setText(newLine.toString());
                }
            }
        });

        spn_SelectionLength[0].addValueChangeListener(new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
                if (updatingSelectionTab || Project.getFileToEdit() == null) return;
                VertexManager vm = Project.getFileToEdit().getVertexManager();
                vm.addSnapshot();

                if (vm.getSelectedLines().size() > 0) {
                    org.nschmidt.ldparteditor.data.GData2 line = null;
                    line = vm.getSelectedLines().iterator().next();
                    if (line.isLine) {
                        disableSelectionTab();
                        return;
                    }

                    BigDecimal[] c = ProtractorHelper.changeLength(spn.getValue(), line);
                    updatingSelectionTab = true;
                    spn_SelectionX2[0].setValue(c[0]);
                    spn_SelectionY2[0].setValue(c[1]);
                    spn_SelectionZ2[0].setValue(c[2]);
                    updatingSelectionTab = false;

                    final GData newLine = Project.getFileToEdit().getVertexManager().updateSelectedLine(
                            spn_SelectionX1[0].getValue(), spn_SelectionY1[0].getValue(), spn_SelectionZ1[0].getValue(),
                            c[0], c[1], c[2],
                            spn_SelectionX3[0].getValue(), spn_SelectionY3[0].getValue(), spn_SelectionZ3[0].getValue(),
                            spn_SelectionX4[0].getValue(), spn_SelectionY4[0].getValue(), spn_SelectionZ4[0].getValue(),
                            btn_MoveAdjacentData2[0].getSelection()
                            );
                    if (newLine == null) {
                        disableSelectionTab();
                    } else {
                        txt_Line[0].setText(newLine.toString());
                    }

                } else {

                    org.nschmidt.ldparteditor.data.GData3 tri = null;
                    if (vm.getSelectedTriangles().size() == 0) {
                        disableSelectionTab();
                        return;
                    }
                    tri = vm.getSelectedTriangles().iterator().next();
                    if (tri.isTriangle) {
                        disableSelectionTab();
                        return;
                    }

                    BigDecimal[] c = ProtractorHelper.changeLength(spn.getValue(), tri);
                    updatingSelectionTab = true;
                    spn_SelectionX3[0].setValue(c[0]);
                    spn_SelectionY3[0].setValue(c[1]);
                    spn_SelectionZ3[0].setValue(c[2]);
                    updatingSelectionTab = false;

                    final GData newLine = Project.getFileToEdit().getVertexManager().updateSelectedLine(
                            spn_SelectionX1[0].getValue(), spn_SelectionY1[0].getValue(), spn_SelectionZ1[0].getValue(),
                            spn_SelectionX2[0].getValue(), spn_SelectionY2[0].getValue(), spn_SelectionZ2[0].getValue(),
                            c[0], c[1], c[2],
                            spn_SelectionX4[0].getValue(), spn_SelectionY4[0].getValue(), spn_SelectionZ4[0].getValue(),
                            btn_MoveAdjacentData2[0].getSelection()
                            );
                    if (newLine == null) {
                        disableSelectionTab();
                    } else {
                        txt_Line[0].setText(newLine.toString());
                    }
                }
            }
        });

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

        btn_MoveAdjacentData2[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                regainFocus();
            }
        });

        btn_OpenInTextEditor[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                action_OpenInTextEditor();
            }
        });
        btn_OpenIn3DEditor[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                action_OpenIn3DEditor();
            }
        });
        btn_Revert[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                action_Revert();
            }
        });
        btn_Close[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                action_Close();
            }
        });
        btn_Rename[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                action_Rename();
            }
        });
        btn_CopyToUnofficial[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                action_CopyToUnofficial();
            }
        });

        treeParts[0].addMenuDetectListener(new MenuDetectListener() {
            @Override
            public void menuDetected(MenuDetectEvent e) {

                NLogger.debug(getClass(), "Showing context menu."); //$NON-NLS-1$

                try {
                    if (treeParts[0].getTree().getMenu() != null) {
                        treeParts[0].getTree().getMenu().dispose();
                    }
                } catch (Exception ex) {}

                final boolean enabled = treeParts[0].getSelectionCount() > 0  && treeParts[0].getSelection()[0] != null ? treeParts[0].getSelection()[0].getData() instanceof DatFile : false;
                final boolean writable = enabled ? !((DatFile) treeParts[0].getSelection()[0].getData()).isReadOnly() : false;
                final boolean isNotUnofficial = enabled && treeParts[0].getSelection()[0].getParentItem().getParentItem() != null ? !treeParts[0].getSelection()[0].getParentItem().getParentItem().equals(treeItem_Unofficial[0]) : false;

                Menu treeMenu = new Menu(treeParts[0].getTree());
                treeParts[0].getTree().setMenu(treeMenu);
                mnu_treeMenu[0] = treeMenu;

                MenuItem mntmOpenIn3DEditor = new MenuItem(treeMenu, I18n.I18N_NON_BIDIRECT());
                mntm_OpenIn3DEditor[0] = mntmOpenIn3DEditor;
                mntmOpenIn3DEditor.setEnabled(enabled);
                mntmOpenIn3DEditor.setText(I18n.E3D_OpenIn3DEditor);

                MenuItem mntmOpenInTextEditor = new MenuItem(treeMenu, I18n.I18N_NON_BIDIRECT());
                mntm_OpenInTextEditor[0] = mntmOpenInTextEditor;
                mntmOpenInTextEditor.setEnabled(enabled);
                mntmOpenInTextEditor.setText(I18n.E3D_OpenInTextEditor);

                @SuppressWarnings("unused")
                MenuItem mntm_Separator = new MenuItem(treeMenu, I18n.I18N_NON_BIDIRECT() | SWT.SEPARATOR);

                MenuItem mntmClose = new MenuItem(treeMenu, I18n.I18N_NON_BIDIRECT());
                mntm_Close[0] = mntmClose;
                mntmClose.setEnabled(enabled);
                mntmClose.setText(I18n.E3D_Close);

                @SuppressWarnings("unused")
                MenuItem mntm_Separator2 = new MenuItem(treeMenu, I18n.I18N_NON_BIDIRECT() | SWT.SEPARATOR);

                MenuItem mntmRename = new MenuItem(treeMenu, I18n.I18N_NON_BIDIRECT());
                mntm_Rename[0] = mntmRename;
                mntmRename.setEnabled(enabled && writable);
                mntmRename.setText(I18n.E3D_RenameMove);

                MenuItem mntmRevert = new MenuItem(treeMenu, I18n.I18N_NON_BIDIRECT());
                mntm_Revert[0] = mntmRevert;
                mntmRevert.setEnabled(enabled && writable);
                mntmRevert.setText(I18n.E3D_RevertAllChanges);

                @SuppressWarnings("unused")
                MenuItem mntm_Separator3 = new MenuItem(treeMenu, I18n.I18N_NON_BIDIRECT() | SWT.SEPARATOR);

                MenuItem mntmCopyToUnofficial = new MenuItem(treeMenu, I18n.I18N_NON_BIDIRECT());
                mntm_CopyToUnofficial[0] = mntmCopyToUnofficial;
                mntmCopyToUnofficial.setEnabled(enabled && isNotUnofficial);
                mntmCopyToUnofficial.setText(I18n.E3D_CopyToUnofficialLibrary);

                mntm_OpenInTextEditor[0].addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        action_OpenInTextEditor();
                    }
                });
                mntm_OpenIn3DEditor[0].addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        action_OpenIn3DEditor();
                    }
                });
                mntm_Revert[0].addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        action_Revert();
                    }
                });
                mntm_Close[0].addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        action_Close();
                    }
                });
                mntm_Rename[0].addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        action_Rename();
                    }
                });
                mntm_CopyToUnofficial[0].addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        action_CopyToUnofficial();
                    }
                });

                java.awt.Point b = java.awt.MouseInfo.getPointerInfo().getLocation();
                final int x = (int) b.getX();
                final int y = (int) b.getY();

                Menu menu = mnu_treeMenu[0];
                menu.setLocation(x, y);
                menu.setVisible(true);
                regainFocus();
            }
        });

        treeParts[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                final boolean enabled = treeParts[0].getSelectionCount() > 0  && treeParts[0].getSelection()[0] != null ? treeParts[0].getSelection()[0].getData() instanceof DatFile : false;
                final boolean writable = enabled ? !((DatFile) treeParts[0].getSelection()[0].getData()).isReadOnly() : false;
                final boolean isNotUnofficial = enabled && treeParts[0].getSelection()[0].getParentItem().getParentItem() != null ? !treeParts[0].getSelection()[0].getParentItem().getParentItem().equals(treeItem_Unofficial[0]) : false;
                btn_OpenIn3DEditor[0].setEnabled(enabled);
                btn_OpenInTextEditor[0].setEnabled(enabled);
                btn_Close[0].setEnabled(enabled);
                btn_Rename[0].setEnabled(enabled && writable);
                btn_Revert[0].setEnabled(enabled && writable);
                btn_CopyToUnofficial[0].setEnabled(enabled && isNotUnofficial);
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
                regainFocus();
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
                txt_Search[0].setFocus();
            }
        });
        txt_primitiveSearch[0].addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                if (getCompositePrimitive().stopDraw()) {
                    txt_primitiveSearch[0].setText(""); //$NON-NLS-1$
                    return;
                } else {
                    getCompositePrimitive().disableRefresh();
                }
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
                txt_primitiveSearch[0].setFocus();
            }
        });
        btn_zoomInPrimitives[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                getCompositePrimitive().zoomIn();
                getCompositePrimitive().getOpenGL().drawScene(-1, -1);
            }
        });
        btn_zoomOutPrimitives[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                getCompositePrimitive().zoomOut();
                getCompositePrimitive().getOpenGL().drawScene(-1, -1);
            }
        });
        btn_Hide[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null) {
                    if (Project.getFileToEdit().getVertexManager().getSelectedData().size() > 0) {
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
                }
                regainFocus();
            }
        });
        btn_ShowAll[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
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
            }
        });
        btn_NoTransparentSelection[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setNoTransparentSelection(btn_NoTransparentSelection[0].getSelection());
                regainFocus();
            }
        });
        btn_BFCToggle[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setBfcToggle(btn_BFCToggle[0].getSelection());
                regainFocus();
            }
        });
        btn_InsertAtCursorPosition[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setInsertingAtCursorPosition(btn_InsertAtCursorPosition[0].getSelection());
                regainFocus();
            }
        });

        btn_Delete[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null) {
                    Project.getFileToEdit().getVertexManager().addSnapshot();
                    Project.getFileToEdit().getVertexManager().delete(Editor3DWindow.getWindow().isMovingAdjacentData(), true);
                }
                regainFocus();
            }
        });
        btn_Copy[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null) {
                    Project.getFileToEdit().getVertexManager().addSnapshot();
                    Project.getFileToEdit().getVertexManager().copy();
                }
                regainFocus();
            }
        });
        btn_Cut[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null) {
                    Project.getFileToEdit().getVertexManager().addSnapshot();
                    Project.getFileToEdit().getVertexManager().copy();
                    Project.getFileToEdit().getVertexManager().delete(false, true);
                }
                regainFocus();
            }
        });
        btn_Paste[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null) {
                    Project.getFileToEdit().getVertexManager().addSnapshot();
                    Project.getFileToEdit().getVertexManager().paste(loadSelectorSettings());
                    if (WorkbenchManager.getUserSettingState().isDisableMAD3D()) {
                        setMovingAdjacentData(false);
                        GuiStatusManager.updateStatus();
                    }
                }
                regainFocus();
            }
        });

        if (btn_Manipulator_0_toOrigin[0] != null) btn_Manipulator_0_toOrigin[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_0_toOrigin();
            }
        });

        if (btn_Manipulator_XIII_toWorld[0] != null) btn_Manipulator_XIII_toWorld[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_XIII_toWorld();
            }
        });

        if (btn_Manipulator_X_XReverse[0] != null) btn_Manipulator_X_XReverse[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_X_XReverse();
            }
        });

        if (btn_Manipulator_XI_YReverse[0] != null) btn_Manipulator_XI_YReverse[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_XI_YReverse();
            }
        });

        if (btn_Manipulator_XII_ZReverse[0] != null) btn_Manipulator_XII_ZReverse[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_XII_ZReverse();
            }
        });

        if (btn_Manipulator_SwitchXY[0] != null) btn_Manipulator_SwitchXY[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_SwitchXY();
            }
        });

        if (btn_Manipulator_SwitchXZ[0] != null) btn_Manipulator_SwitchXZ[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_SwitchXZ();
            }
        });

        if (btn_Manipulator_SwitchYZ[0] != null) btn_Manipulator_SwitchYZ[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_SwitchYZ();
            }
        });

        if (btn_Manipulator_1_cameraToPos[0] != null) btn_Manipulator_1_cameraToPos[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_1_cameraToPos();
            }
        });
        if (btn_Manipulator_2_toAverage[0] != null) btn_Manipulator_2_toAverage[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_2_toAverage();
            }
        });

        if (btn_Manipulator_3_toSubfile[0] != null) btn_Manipulator_3_toSubfile[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_3_toSubfile();
            }
        });

        if (btn_Manipulator_32_subfileTo[0] != null) btn_Manipulator_32_subfileTo[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_32_subfileTo(Cocoa.checkCtrlOrCmdPressed(e.stateMask));
            }
        });

        if (btn_Manipulator_4_toVertex[0] != null) btn_Manipulator_4_toVertex[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_4_toVertex();
            }
        });

        if (btn_Manipulator_5_toEdge[0] != null) btn_Manipulator_5_toEdge[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_5_toEdge();
            }
        });

        if (btn_Manipulator_6_toSurface[0] != null) btn_Manipulator_6_toSurface[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_6_toSurface();
            }
        });

        if (btn_Manipulator_7_toVertexNormal[0] != null) btn_Manipulator_7_toVertexNormal[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_7_toVertexNormal();
            }
        });

        if (btn_Manipulator_8_toEdgeNormal[0] != null) btn_Manipulator_8_toEdgeNormal[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_8_toEdgeNormal();
            }
        });

        if (btn_Manipulator_9_toSurfaceNormal[0] != null) btn_Manipulator_9_toSurfaceNormal[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_9_toSurfaceNormal();
            }
        });

        if (btn_Manipulator_XIV_adjustRotationCenter[0] != null) btn_Manipulator_XIV_adjustRotationCenter[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_XIV_adjustRotationCenter();
            }
        });

        if (btn_Manipulator_XV_toVertexPosition[0] != null) btn_Manipulator_XV_toVertexPosition[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_XV_toVertexPosition();
            }
        });

        if (mntm_Manipulator_0_toOrigin[0] != null) mntm_Manipulator_0_toOrigin[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_0_toOrigin();
            }
        });

        if (mntm_Manipulator_XIII_toWorld[0] != null) mntm_Manipulator_XIII_toWorld[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_XIII_toWorld();
            }
        });

        if (mntm_Manipulator_X_XReverse[0] != null) mntm_Manipulator_X_XReverse[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_X_XReverse();
            }
        });

        if (mntm_Manipulator_XI_YReverse[0] != null) mntm_Manipulator_XI_YReverse[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_XI_YReverse();
            }
        });

        if (mntm_Manipulator_XII_ZReverse[0] != null) mntm_Manipulator_XII_ZReverse[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_XII_ZReverse();
            }
        });

        if (mntm_Manipulator_SwitchXY[0] != null) mntm_Manipulator_SwitchXY[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_SwitchXY();
            }
        });

        if (mntm_Manipulator_SwitchXZ[0] != null) mntm_Manipulator_SwitchXZ[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_SwitchXZ();
            }
        });

        if (mntm_Manipulator_SwitchYZ[0] != null) mntm_Manipulator_SwitchYZ[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_SwitchYZ();
            }
        });

        if (mntm_Manipulator_1_cameraToPos[0] != null) mntm_Manipulator_1_cameraToPos[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_1_cameraToPos();
            }
        });
        if (mntm_Manipulator_2_toAverage[0] != null) mntm_Manipulator_2_toAverage[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_2_toAverage();
            }
        });

        if (mntm_Manipulator_3_toSubfile[0] != null) mntm_Manipulator_3_toSubfile[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_3_toSubfile();
            }
        });

        if (mntm_Manipulator_32_subfileTo[0] != null) mntm_Manipulator_32_subfileTo[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_32_subfileTo(Cocoa.checkCtrlOrCmdPressed(e.stateMask));
            }
        });

        if (mntm_Manipulator_4_toVertex[0] != null) mntm_Manipulator_4_toVertex[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_4_toVertex();
            }
        });

        if (mntm_Manipulator_5_toEdge[0] != null) mntm_Manipulator_5_toEdge[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_5_toEdge();
            }
        });

        if (mntm_Manipulator_6_toSurface[0] != null) mntm_Manipulator_6_toSurface[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_6_toSurface();
            }
        });

        if (mntm_Manipulator_7_toVertexNormal[0] != null) mntm_Manipulator_7_toVertexNormal[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_7_toVertexNormal();
            }
        });

        if (mntm_Manipulator_8_toEdgeNormal[0] != null) mntm_Manipulator_8_toEdgeNormal[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_8_toEdgeNormal();
            }
        });

        if (mntm_Manipulator_9_toSurfaceNormal[0] != null) mntm_Manipulator_9_toSurfaceNormal[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_9_toSurfaceNormal();
            }
        });

        if (mntm_Manipulator_XIV_adjustRotationCenter[0] != null) mntm_Manipulator_XIV_adjustRotationCenter[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_XIV_adjustRotationCenter();
            }
        });

        if (mntm_Manipulator_XV_toVertexPosition[0] != null) mntm_Manipulator_XV_toVertexPosition[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mntm_Manipulator_XV_toVertexPosition();
            }
        });

        mntm_SelectAll[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
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
            }
        });
        mntm_SelectAllVisible[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
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
            }
        });
        mntm_SelectAllWithColours[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
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
            }
        });
        mntm_SelectAllVisibleWithColours[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
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
            }
        });
        mntm_SelectNone[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
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
            }
        });
        mntm_SelectInverse[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
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
                                mntm_WithSameType[0].getSelection() ||
                                mntm_WithSameOrientation[0].getSelection() ||
                                mntm_ExceptSubfiles[0].getSelection()
                                );
                        showSelectMenu();
                    }
                });
                regainFocus();
            }
        });

        mntm_WithSameType[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Display.getCurrent().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        mntm_SelectEverything[0].setEnabled(
                                mntm_WithHiddenData[0].getSelection() ||
                                mntm_WithSameColour[0].getSelection() ||
                                mntm_WithSameType[0].getSelection() ||
                                mntm_WithSameOrientation[0].getSelection() ||
                                mntm_ExceptSubfiles[0].getSelection()
                                );
                        showSelectMenu();
                    }
                });
                regainFocus();
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
                                mntm_WithSameType[0].getSelection() ||
                                mntm_WithSameOrientation[0].getSelection() ||
                                mntm_ExceptSubfiles[0].getSelection()
                                );
                        if (mntm_WithSameOrientation[0].getSelection()) {


                            new ValueDialog(getShell(), I18n.E3D_AngleDiff, I18n.E3D_ThreshInDeg) {

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
                regainFocus();
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
                                mntm_WithSameType[0].getSelection() ||
                                mntm_WithSameOrientation[0].getSelection() ||
                                mntm_ExceptSubfiles[0].getSelection()
                                );
                        if (mntm_WithAccuracy[0].getSelection()) {

                            new ValueDialog(getShell(), I18n.E3D_SetAccuracy, I18n.E3D_ThreshInLdu) {

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
                regainFocus();
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
                                mntm_WithSameType[0].getSelection() ||
                                mntm_WithSameOrientation[0].getSelection() ||
                                mntm_ExceptSubfiles[0].getSelection()
                                );
                        showSelectMenu();
                    }
                });
                regainFocus();
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
                regainFocus();
            }
        });
        mntm_WithAdjacency[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Display.getCurrent().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        showSelectMenu();
                    }
                });
                regainFocus();
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
                                mntm_WithSameType[0].getSelection() ||
                                mntm_WithSameOrientation[0].getSelection() ||
                                mntm_ExceptSubfiles[0].getSelection()
                                );
                        mntm_WithWholeSubfiles[0].setEnabled(!mntm_ExceptSubfiles[0].getSelection());
                        showSelectMenu();
                    }
                });
                regainFocus();
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
                regainFocus();
            }
        });
        mntm_SAllTypes[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Display.getCurrent().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        activateAllTypes();
                        showSelectMenu();
                    }
                });
                regainFocus();
            }
        });
        mntm_SNothing[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Display.getCurrent().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        mntm_SVertices[0].setSelection(false);
                        mntm_SLines[0].setSelection(false);
                        mntm_STriangles[0].setSelection(false);
                        mntm_SQuads[0].setSelection(false);
                        mntm_SCLines[0].setSelection(false);
                        showSelectMenu();
                    }
                });
                regainFocus();
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
                regainFocus();
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
                regainFocus();
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
                regainFocus();
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
                regainFocus();
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
                regainFocus();
            }
        });

        mntm_SelectEverything[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
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
            }
        });

        mntm_SelectConnected[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
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
            }
        });

        mntm_SelectTouching[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
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
                                vm.addSnapshot();
                                vm.selectIsolatedVertices();
                                vm.syncWithTextEditors(true);
                                regainFocus();
                                return;
                            }
                        }
                    }
                });
                regainFocus();
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
                            if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                                VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                                vm.addSnapshot();
                                vm.split(2);
                                regainFocus();
                                return;
                            }
                        }
                    }
                });
                regainFocus();
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
                            if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {

                                final int[] frac = new int[]{2};
                                if (new ValueDialogInt(getShell(), I18n.E3D_SplitEdges, I18n.E3D_NumberOfFractions) {

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
                                    vm.addSnapshot();
                                    vm.split(frac[0]);
                                    regainFocus();
                                    return;
                                }
                            }
                        }
                    }
                });
                regainFocus();
            }
        });


        mntm_Smooth[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Display.getCurrent().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        for (OpenGLRenderer renderer : renders) {
                            Composite3D c3d = renderer.getC3D();
                            if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                                OpenGLRenderer.getSmoothing().set(true);
                                if (new SmoothDialog(getShell()).open() == OK) {
                                    VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                                    vm.addSnapshot();
                                    vm.smooth(SmoothDialog.isX(), SmoothDialog.isY(), SmoothDialog.isZ(), SmoothDialog.getFactor(), SmoothDialog.getIterations());
                                    regainFocus();
                                }
                                OpenGLRenderer.getSmoothing().set(false);
                                return;
                            }
                        }
                    }
                });
                regainFocus();
            }
        });

        mntm_MergeToAverage[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
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
            }
        });
        mntm_MergeToLastSelected[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
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
            }
        });
        mntm_MergeToNearestVertex[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
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
            }
        });
        mntm_MergeToNearestEdge[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
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
            }
        });
        mntm_MergeToNearestEdgeSplit[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
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
            }
        });
        mntm_MergeToNearestFace[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
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
            }
        });

        mntm_MergeToNearestFaceDir[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        vm.addSnapshot();
                        final WorkingMode action = Editor3DWindow.getWindow().getWorkingAction();
                        if (DirectionDialog.getTransformationMode() == ManipulatorScope.GLOBAL) {
                            Editor3DWindow.getWindow().setWorkingAction(WorkingMode.MOVE_GLOBAL);
                        } else {
                            Editor3DWindow.getWindow().setWorkingAction(WorkingMode.MOVE);
                        }
                        vm.merge(MergeTo.NEAREST_FACE, true, true);
                        Editor3DWindow.getWindow().setWorkingAction(action);
                        regainFocus();
                        return;
                    }
                }
                regainFocus();
            }
        });

        mntm_SelectSingleVertex[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        final VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        final Set<Vertex> sv = vm.getSelectedVertices();
                        if (new VertexDialog(getShell()).open() == IDialogConstants.OK_ID) {
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
            }
        });

        mntm_setXYZ[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                final boolean noReset = !Cocoa.checkCtrlOrCmdPressed(e.stateMask);
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                        Vertex v = new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
                        final VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        final Set<Vertex> sv = vm.getSelectedVertices();
                        final boolean hasNoClipboardVertex;

                        if (VertexManager.getClipboard().size() == 1) {
                            GData vertex = VertexManager.getClipboard().get(0);
                            if (vertex.type() == 0) {
                                String line = vertex.toString();
                                line = line.replaceAll("\\s+", " ").trim(); //$NON-NLS-1$ //$NON-NLS-2$
                                String[] data_segments = line.split("\\s+"); //$NON-NLS-1$
                                if (line.startsWith("0 !LPE VERTEX ")) { //$NON-NLS-1$
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
                                        v = new Vertex(start);
                                        hasNoClipboardVertex = false;
                                    } else {
                                        hasNoClipboardVertex = true;
                                    }
                                } else {
                                    hasNoClipboardVertex = true;
                                }
                            } else {
                                hasNoClipboardVertex = true;
                            }
                        } else {
                            hasNoClipboardVertex = true;
                        }

                        if (hasNoClipboardVertex) {
                            if (sv.size() == 1 && vm.getSelectedData().size() == 0) {
                                v = sv.iterator().next();
                            } else {
                                v = new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
                                vm.selectObjectVertices();
                                CoordinatesDialog.setStart(null);
                                CoordinatesDialog.setEnd(null);
                            }
                        }
                        final Vertex mani = new Vertex(c3d.getManipulator().getAccuratePosition());
                        final WorkingMode action = Editor3DWindow.getWindow().getWorkingAction();
                        if (CoordinatesDialog.getTransformationMode() == ManipulatorScope.GLOBAL) {
                            Editor3DWindow.getWindow().setWorkingAction(WorkingMode.MOVE_GLOBAL);
                        } else {
                            Editor3DWindow.getWindow().setWorkingAction(WorkingMode.MOVE);
                        }
                        if (new CoordinatesDialog(getShell(), v, mani, c3d.getManipulator()).open() == IDialogConstants.OK_ID) {

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
                                TreeSet<Vertex> verts = new TreeSet<Vertex>();
                                verts.addAll(vm.getSelectedVertices());
                                vm.clearSelection();
                                for (Vertex v2 : verts) {
                                    final boolean a = CoordinatesDialog.isX();
                                    final boolean b = CoordinatesDialog.isY();
                                    final boolean c = CoordinatesDialog.isZ();
                                    vm.getSelectedVertices().add(v2);
                                    Vector3d delta = Vector3d.sub(CoordinatesDialog.getEnd(), CoordinatesDialog.getStart());
                                    boolean doMoveOnLine = false;
                                    BigDecimal s = BigDecimal.ZERO;
                                    Vector3d v1 = CoordinatesDialog.getStart();
                                    if (CoordinatesDialog.isX() && delta.X.compareTo(BigDecimal.ZERO) != 0) {
                                        doMoveOnLine = true;
                                        s = v2.X.subtract(CoordinatesDialog.getStart().X).divide(delta.X, Threshold.mc);
                                    } else if (CoordinatesDialog.isY() && delta.Y.compareTo(BigDecimal.ZERO) != 0) {
                                        doMoveOnLine = true;
                                        s = v2.Y.subtract(CoordinatesDialog.getStart().Y).divide(delta.Y, Threshold.mc);
                                    } else if (CoordinatesDialog.isZ() && delta.Z.compareTo(BigDecimal.ZERO) != 0) {
                                        doMoveOnLine = true;
                                        s = v2.Z.subtract(CoordinatesDialog.getStart().Z).divide(delta.Z, Threshold.mc);
                                    }
                                    if (doMoveOnLine) {
                                        CoordinatesDialog.setVertex(new Vertex(v1.X.add(delta.X.multiply(s)), v1.Y.add(delta.Y.multiply(s)), v1.Z.add(delta.Z.multiply(s))));
                                        CoordinatesDialog.setX(true);
                                        CoordinatesDialog.setY(true);
                                        CoordinatesDialog.setZ(true);
                                    }
                                    vm.setXyzOrTranslateOrTransform(CoordinatesDialog.getVertex(), null, TransformationMode.SET, CoordinatesDialog.isX(), CoordinatesDialog.isY(), CoordinatesDialog.isZ(), true, true, CoordinatesDialog.getTransformationMode());
                                    vm.clearSelection();
                                    CoordinatesDialog.setX(a);
                                    CoordinatesDialog.setY(b);
                                    CoordinatesDialog.setZ(c);
                                }
                            } else if (coordCount == 2 && CoordinatesDialog.getStart() != null) {
                                TreeSet<Vertex> verts = new TreeSet<Vertex>();
                                verts.addAll(vm.getSelectedVertices());
                                vm.clearSelection();
                                for (Vertex v2 : verts) {
                                    final boolean a = CoordinatesDialog.isX();
                                    final boolean b = CoordinatesDialog.isY();
                                    final boolean c = CoordinatesDialog.isZ();
                                    vm.getSelectedVertices().add(v2);
                                    Vector3d delta = Vector3d.sub(CoordinatesDialog.getEnd(), CoordinatesDialog.getStart());
                                    boolean doMoveOnLine = false;
                                    BigDecimal s = BigDecimal.ZERO;
                                    Vector3d v1 = CoordinatesDialog.getStart();
                                    if (CoordinatesDialog.isX() && delta.X.compareTo(BigDecimal.ZERO) != 0) {
                                        doMoveOnLine = true;
                                        s = v2.X.subtract(CoordinatesDialog.getStart().X).divide(delta.X, Threshold.mc);
                                    } else if (CoordinatesDialog.isY() && delta.Y.compareTo(BigDecimal.ZERO) != 0) {
                                        doMoveOnLine = true;
                                        s = v2.Y.subtract(CoordinatesDialog.getStart().Y).divide(delta.Y, Threshold.mc);
                                    } else if (CoordinatesDialog.isZ() && delta.Z.compareTo(BigDecimal.ZERO) != 0) {
                                        doMoveOnLine = true;
                                        s = v2.Z.subtract(CoordinatesDialog.getStart().Z).divide(delta.Z, Threshold.mc);
                                    }
                                    BigDecimal X = !CoordinatesDialog.isX() ? v1.X.add(delta.X.multiply(s)) : v2.X;
                                    BigDecimal Y = !CoordinatesDialog.isY() ? v1.Y.add(delta.Y.multiply(s)) : v2.Y;
                                    BigDecimal Z = !CoordinatesDialog.isZ() ? v1.Z.add(delta.Z.multiply(s)) : v2.Z;
                                    if (doMoveOnLine) {
                                        CoordinatesDialog.setVertex(new Vertex(X, Y, Z));
                                        CoordinatesDialog.setX(true);
                                        CoordinatesDialog.setY(true);
                                        CoordinatesDialog.setZ(true);
                                    }
                                    vm.setXyzOrTranslateOrTransform(CoordinatesDialog.getVertex(), null, TransformationMode.SET, CoordinatesDialog.isX(), CoordinatesDialog.isY(), CoordinatesDialog.isZ(), true, true, CoordinatesDialog.getTransformationMode());
                                    vm.clearSelection();
                                    CoordinatesDialog.setX(a);
                                    CoordinatesDialog.setY(b);
                                    CoordinatesDialog.setZ(c);
                                }
                            } else {
                                final boolean moveAdjacentData = isMovingAdjacentData();
                                if (CoordinatesDialog.isCreatingCopy()) {
                                    vm.copy();
                                    vm.paste(null);
                                    setMovingAdjacentData(false);
                                    vm.setXyzOrTranslateOrTransform(CoordinatesDialog.getVertex(), null, TransformationMode.SET, CoordinatesDialog.isX(), CoordinatesDialog.isY(), CoordinatesDialog.isZ(), false, true, CoordinatesDialog.getTransformationMode());
                                    setMovingAdjacentData(moveAdjacentData);
                                } else {
                                    vm.setXyzOrTranslateOrTransform(CoordinatesDialog.getVertex(), null, TransformationMode.SET, CoordinatesDialog.isX(), CoordinatesDialog.isY(), CoordinatesDialog.isZ(), isMovingAdjacentData() || vm.getSelectedData().size() == 0 || vm.getSelectedVertices().size() == 1, true, CoordinatesDialog.getTransformationMode());
                                }
                            }

                            if (noReset) {
                                CoordinatesDialog.setStart(null);
                                CoordinatesDialog.setEnd(null);
                            }
                        }
                        Editor3DWindow.getWindow().setWorkingAction(action);
                        regainFocus();
                        return;
                    }
                }
                regainFocus();
            }
        });

        mntm_Translate[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())
                            && c3d.equals(DatFile.getLastHoveredComposite())
                            && !c3d.getLockableDatFileReference().isReadOnly()) {
                        final WorkingMode action = Editor3DWindow.getWindow().getWorkingAction();
                        if (TranslateDialog.getTransformationMode() == ManipulatorScope.GLOBAL) {
                            Editor3DWindow.getWindow().setWorkingAction(WorkingMode.MOVE_GLOBAL);
                        } else {
                            Editor3DWindow.getWindow().setWorkingAction(WorkingMode.MOVE);
                        }
                        if (new TranslateDialog(getShell(), new Vertex(c3d.getManipulator().getAccuratePosition()), transformationMode).open() == IDialogConstants.OK_ID) {
                            c3d.getLockableDatFileReference().getVertexManager().addSnapshot();
                            final boolean moveAdjacentData = isMovingAdjacentData();
                            final int iterations = TranslateDialog.getAndResetIterations();
                            for (int i = 0; i < iterations; i++) {
                                if (TranslateDialog.isCreatingCopy()) {
                                    c3d.getLockableDatFileReference().getVertexManager().copy();
                                    c3d.getLockableDatFileReference().getVertexManager().paste(null);
                                    setMovingAdjacentData(false);
                                }
                                c3d.getLockableDatFileReference().getVertexManager().setXyzOrTranslateOrTransform(TranslateDialog.getOffset(), null, TransformationMode.TRANSLATE, TranslateDialog.isX(), TranslateDialog.isY(), TranslateDialog.isZ(), isMovingAdjacentData(), true, TranslateDialog.getTransformationMode());
                            }
                            if (TranslateDialog.isCreatingCopy()) {
                                setMovingAdjacentData(moveAdjacentData);
                            }
                        }
                        Editor3DWindow.getWindow().setWorkingAction(action);
                        regainFocus();
                        return;
                    }
                }
                regainFocus();
            }
        });

        mntm_Rotate[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
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
                                                start.setY(new BigDecimal(data_segments[4], Threshold.mc));
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
                        final Vertex mani = new Vertex(c3d.getManipulator().getAccuratePosition());
                        final WorkingMode action = Editor3DWindow.getWindow().getWorkingAction();
                        if (RotateDialog.getTransformationMode() == ManipulatorScope.GLOBAL) {
                            Editor3DWindow.getWindow().setWorkingAction(WorkingMode.MOVE_GLOBAL);
                        } else {
                            Editor3DWindow.getWindow().setWorkingAction(WorkingMode.MOVE);
                        }
                        if (new RotateDialog(getShell(), null, clipboard, mani, transformationMode).open() == IDialogConstants.OK_ID) {
                            c3d.getLockableDatFileReference().getVertexManager().addSnapshot();
                            final boolean moveAdjacentData = isMovingAdjacentData();
                            final int iterations = RotateDialog.getAndResetIterations();
                            for (int i = 0; i < iterations; i++) {
                                if (RotateDialog.isCreatingCopy()) {
                                    c3d.getLockableDatFileReference().getVertexManager().copy();
                                    c3d.getLockableDatFileReference().getVertexManager().paste(null);
                                    setMovingAdjacentData(false);
                                }
                                c3d.getLockableDatFileReference().getVertexManager().setXyzOrTranslateOrTransform(RotateDialog.getAngles(), RotateDialog.getPivot(), TransformationMode.ROTATE, RotateDialog.isX(), RotateDialog.isY(), RotateDialog.isZ(), isMovingAdjacentData(), true, RotateDialog.getTransformationMode());
                            }
                            if (RotateDialog.isCreatingCopy()) {
                                setMovingAdjacentData(moveAdjacentData);
                            }
                        }
                        Editor3DWindow.getWindow().setWorkingAction(action);
                        regainFocus();
                        return;
                    }
                }
                regainFocus();
            }
        });

        mntm_Scale[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
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
                                                start.setY(new BigDecimal(data_segments[4], Threshold.mc));
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
                        final Vertex mani = new Vertex(c3d.getManipulator().getAccuratePosition());
                        final WorkingMode action = Editor3DWindow.getWindow().getWorkingAction();
                        if (ScaleDialog.getTransformationMode() == ManipulatorScope.GLOBAL) {
                            Editor3DWindow.getWindow().setWorkingAction(WorkingMode.MOVE_GLOBAL);
                        } else {
                            Editor3DWindow.getWindow().setWorkingAction(WorkingMode.MOVE);
                        }
                        if (new ScaleDialog(getShell(), null, clipboard, mani, transformationMode).open() == IDialogConstants.OK_ID) {
                            c3d.getLockableDatFileReference().getVertexManager().addSnapshot();
                            updateInitialScale(BigDecimal.ZERO, BigDecimal.ZERO, true);
                            final boolean moveAdjacentData = isMovingAdjacentData();
                            if (ScaleDialog.isCreatingCopy()) {
                                c3d.getLockableDatFileReference().getVertexManager().copy();
                                c3d.getLockableDatFileReference().getVertexManager().paste(null);
                                setMovingAdjacentData(false);
                            }
                            c3d.getLockableDatFileReference().getVertexManager().setXyzOrTranslateOrTransform(ScaleDialog.getScaleFactors(), ScaleDialog.getPivot(), TransformationMode.SCALE, ScaleDialog.isX(), ScaleDialog.isY(), ScaleDialog.isZ(), isMovingAdjacentData(), true, ScaleDialog.getTransformationMode());
                            if (ScaleDialog.isCreatingCopy()) {
                                setMovingAdjacentData(moveAdjacentData);
                            }
                        }
                        Editor3DWindow.getWindow().setWorkingAction(action);
                        regainFocus();
                        return;
                    }
                }
                regainFocus();
            }
        });

        mntm_PartReview[0].addSelectionListener(new SelectionAdapter() {

            final Pattern WHITESPACE = Pattern.compile("\\s+"); //$NON-NLS-1$
            final Pattern pattern = Pattern.compile("\r?\n|\r"); //$NON-NLS-1$

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (new PartReviewDialog(getShell(), isReviewingAPart()).open() == IDialogConstants.OK_ID) {
                    if (isReviewingAPart()) {
                        setReviewingAPart(false, null);
                        return;
                    }

                    final Editor3DWindowState winState = WorkbenchManager.getEditor3DWindowState();
                    winState.setThreeDwindowConfig(getC3DStates());

                    try {
                        new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, false, new IRunnableWithProgress() {
                            @Override
                            public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                                monitor.beginTask(I18n.E3D_PartReview, IProgressMonitor.UNKNOWN);

                                String fileName = PartReviewDialog.getFileName().toLowerCase(Locale.ENGLISH);
                                if (!fileName.endsWith(".dat")) fileName = fileName + ".dat"; //$NON-NLS-1$ //$NON-NLS-2$
                                String oldFileName = fileName;
                                try {
                                    oldFileName = oldFileName.replaceAll("\\\\", File.separator); //$NON-NLS-1$
                                } catch (Exception ex) {
                                    // Workaround for windows OS / JVM BUG
                                    oldFileName = oldFileName.replace("\\", File.separator); //$NON-NLS-1$
                                }
                                try {
                                    fileName = fileName.replaceAll("\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
                                } catch (Exception ex) {
                                    // Workaround for windows OS / JVM BUG
                                    fileName = fileName.replace("\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
                                }

                                // Download first, then build the views

                                // http://www.ldraw.org/library/unofficial
                                monitor.beginTask(fileName, IProgressMonitor.UNKNOWN);
                                String source = FileHelper.downloadPartFile("parts/" + fileName, monitor); //$NON-NLS-1$
                                if (source == null) source = FileHelper.downloadPartFile("parts/s/" + fileName, monitor); //$NON-NLS-1$
                                if (source == null) source = FileHelper.downloadPartFile("p/" + fileName, monitor); //$NON-NLS-1$
                                if (source == null) source = FileHelper.downloadPartFile("p/8/" + fileName, monitor); //$NON-NLS-1$
                                if (source == null) source = FileHelper.downloadPartFile("p/48/" + fileName, monitor); //$NON-NLS-1$
                                if (source == null) {
                                    MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                                    messageBox.setText(I18n.DIALOG_Error);
                                    messageBox.setMessage(I18n.E3D_PartReviewError);
                                    messageBox.open();
                                    return;
                                }

                                HashSet<String> files = new HashSet<String>();
                                files.add(fileName);
                                ArrayList<String> list = buildFileList(source, new ArrayList<String>(), files, monitor);

                                final String fileName2 = fileName;
                                final String source2 = source;
                                final String oldFileName2 = oldFileName;
                                Display.getDefault().asyncExec(new Runnable() {
                                    @Override
                                    public void run() {

                                        String fileName = fileName2;
                                        String source = source2;

                                        closeAllComposite3D();
                                        for (EditorTextWindow txtwin : Project.getOpenTextWindows()) {
                                            if (txtwin.isSeperateWindow()) {
                                                txtwin.getShell().close();
                                            } else {
                                                txtwin.closeAllTabs();
                                            }
                                        }
                                        Project.setDefaultProject(true);
                                        Project.setProjectPath(new File(Project.DEFAULT_PROJECT_PATH).getAbsolutePath());
                                        getShell().setText(Version.getApplicationName() + " " + Version.getVersion() + " (" + WorkbenchManager.getUserSettingState().getOpenGLVersionString() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                        getShell().update();
                                        treeItem_Project[0].setText(fileName);
                                        treeItem_Project[0].setData(Project.getProjectPath());

                                        treeItem_ProjectParts[0].getItems().clear();
                                        treeItem_ProjectSubparts[0].getItems().clear();
                                        treeItem_ProjectPrimitives[0].getItems().clear();

                                        treeItem_OfficialParts[0].setData(null);

                                        list.add(0, new File(Project.DEFAULT_PROJECT_PATH).getAbsolutePath() + File.separator + oldFileName2);
                                        list.add(1, source);

                                        DatFile main = View.DUMMY_DATFILE;

                                        HashSet<DatFile> dfsToOpen = new HashSet<DatFile>();
                                        ArrayList<DatFile> tempFileList = new ArrayList<DatFile>();
                                        tempFileList.addAll(Project.getOpenedFiles());

                                        for (int i =  list.size() - 2; i >= 0; i -= 2) {
                                            DatFile df;
                                            TreeItem n;
                                            fileName = list.get(i);
                                            source = list.get(i + 1);
                                            df = DatFile.createDatFileForReview(fileName);
                                            monitor.beginTask(fileName, IProgressMonitor.UNKNOWN);
                                            Display.getCurrent().readAndDispatch();
                                            dfsToOpen.add(df);
                                            df.setText(source);
                                            // Add / remove from unsaved files is mandatory!
                                            Project.addUnsavedFile(df);
                                            df.parseForData(true);
                                            Project.removeUnsavedFile(df);
                                            Project.getParsedFiles().remove(df);
                                            if (source.contains("0 !LDRAW_ORG Unofficial_Subpart")) { //$NON-NLS-1$
                                                int ind = fileName.lastIndexOf(File.separator + "s" + File.separator); //$NON-NLS-1$
                                                if (ind >= 0) {
                                                    fileName = new StringBuilder(fileName).replace(ind, ind + File.separator.length() * 2 + 1, File.separator + "parts" + File.separator + "s" + File.separator).toString();  //$NON-NLS-1$ //$NON-NLS-2$
                                                }
                                                n = new TreeItem(treeItem_ProjectSubparts[0], SWT.NONE);
                                                df.setType(DatType.SUBPART);
                                            } else if (source.contains("0 !LDRAW_ORG Unofficial_Primitive")) { //$NON-NLS-1$
                                                int ind = fileName.lastIndexOf(File.separator);
                                                if (ind >= 0) {
                                                    fileName = new StringBuilder(fileName).replace(ind, ind + File.separator.length(), File.separator + "p" + File.separator).toString();  //$NON-NLS-1$
                                                }
                                                n = new TreeItem(treeItem_ProjectPrimitives[0], SWT.NONE);
                                                df.setType(DatType.PRIMITIVE);
                                            } else if (source.contains("0 !LDRAW_ORG Unofficial_48_Primitive")) { //$NON-NLS-1$
                                                int ind = fileName.lastIndexOf(File.separator + "48" + File.separator); //$NON-NLS-1$
                                                if (ind >= 0) {
                                                    fileName = new StringBuilder(fileName).replace(ind, ind + File.separator.length() * 2 + 2, File.separator + "p" + File.separator  + "48" + File.separator).toString();  //$NON-NLS-1$ //$NON-NLS-2$
                                                }
                                                n = new TreeItem(treeItem_ProjectPrimitives48[0], SWT.NONE);
                                                df.setType(DatType.PRIMITIVE48);
                                            } else if (source.contains("0 !LDRAW_ORG Unofficial_8_Primitive")) { //$NON-NLS-1$
                                                int ind = fileName.lastIndexOf(File.separator + "8" + File.separator); //$NON-NLS-1$
                                                if (ind >= 0) {
                                                    fileName = new StringBuilder(fileName).replace(ind, ind + File.separator.length() * 2 + 1, File.separator + "p" + File.separator  + "8" + File.separator).toString();  //$NON-NLS-1$ //$NON-NLS-2$
                                                }
                                                n = new TreeItem(treeItem_ProjectPrimitives8[0], SWT.NONE);
                                                df.setType(DatType.PRIMITIVE8);
                                            } else {
                                                int ind = fileName.lastIndexOf(File.separator);
                                                if (ind >= 0) {
                                                    fileName = new StringBuilder(fileName).replace(ind, ind + File.separator.length(), File.separator + "parts" + File.separator).toString();  //$NON-NLS-1$
                                                }
                                                n = new TreeItem(treeItem_ProjectParts[0], SWT.NONE);
                                                df.setType(DatType.PART);
                                            }

                                            df.setNewName(fileName);
                                            df.setOldName(fileName);

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

                                        resetSearch();

                                        treeItem_Project[0].getParent().build();
                                        treeItem_Project[0].getParent().redraw();
                                        treeItem_Project[0].getParent().update();

                                        Project.setFileToEdit(main);

                                        openDatFile(main, OpenInWhat.EDITOR_3D, null);
                                        renders.get(0).getC3D().getModifier().splitViewHorizontally();
                                        renders.get(0).getC3D().getModifier().splitViewVertically();
                                        renders.get(1).getC3D().getModifier().splitViewVertically();

                                        int state = 0;
                                        for (OpenGLRenderer renderer : getRenders()) {
                                            Composite3D c3d = renderer.getC3D();
                                            WidgetSelectionHelper.unselectAllChildButtons(c3d.mnu_renderMode);
                                            if (state == 0) {
                                                c3d.mntmNoBFC[0].setSelection(true);
                                                c3d.getMntmStudLogo().setSelection(true);
                                                c3d.getModifier().switchShowingLogo(true);
                                                c3d.getModifier().setRenderMode(0);
                                            }
                                            if (state == 1) {
                                                c3d.mntmRandomColours[0].setSelection(true);
                                                c3d.getModifier().setRenderMode(1);
                                            }
                                            if (state == 2) {
                                                c3d.mntmCondlineMode[0].setSelection(true);
                                                c3d.getModifier().setRenderMode(6);
                                            }
                                            if (state == 3) {
                                                c3d.mntmWireframeMode[0].setSelection(true);
                                                c3d.getModifier().setRenderMode(-1);
                                            }
                                            state++;
                                        }

                                        updateTree_unsavedEntries();

                                        EditorTextWindow txt;

                                        if (Project.getOpenTextWindows().isEmpty()) {
                                            txt = new EditorTextWindow();
                                            txt.run(main, false);
                                        } else {
                                            txt = Project.getOpenTextWindows().iterator().next();
                                        }

                                        for (DatFile df : dfsToOpen) {
                                            txt.openNewDatFileTab(df, false);
                                        }

                                        setReviewingAPart(true, dfsToOpen);
                                        regainFocus();
                                    }
                                });
                            }
                        });
                    } catch (InvocationTargetException consumed) {
                    } catch (InterruptedException consumed) {
                    }
                }
            }

            private ArrayList<String> buildFileList(String source, ArrayList<String> result, HashSet<String> files, final IProgressMonitor monitor) {
                String[] lines;

                lines = pattern.split(source, -1);

                for (String line : lines) {
                    line = line.trim();
                    if (line.startsWith("1 ")) { //$NON-NLS-1$
                        final String[] data_segments = WHITESPACE.split(line.trim());
                        if (data_segments.length > 14) {
                            StringBuilder sb = new StringBuilder();
                            for (int s = 14; s < data_segments.length - 1; s++) {
                                sb.append(data_segments[s]);
                                sb.append(" "); //$NON-NLS-1$
                            }
                            sb.append(data_segments[data_segments.length - 1]);
                            String fileName = sb.toString();
                            fileName = fileName.toLowerCase(Locale.ENGLISH);
                            try {
                                fileName = fileName.replaceAll("\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
                            } catch (Exception e) {
                                // Workaround for windows OS / JVM BUG
                                fileName = fileName.replace("\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
                            }

                            if (files.contains(fileName)) continue;
                            files.add(fileName);
                            monitor.subTask(I18n.E3D_PartReviewCheck + fileName);
                            String source2 = FileHelper.downloadPartFile("parts/" + fileName, monitor); //$NON-NLS-1$
                            if (source2 == null) source2 = FileHelper.downloadPartFile("parts/s/" + fileName, monitor); //$NON-NLS-1$
                            if (source2 == null) source2 = FileHelper.downloadPartFile("p/" + fileName, monitor); //$NON-NLS-1$

                            if (source2 != null) {

                                try {
                                    fileName = fileName.replaceAll("/", File.separator); //$NON-NLS-1$
                                } catch (Exception ex) {
                                    // Workaround for windows OS / JVM BUG
                                    fileName = fileName.replace("/", File.separator); //$NON-NLS-1$
                                }
                                try {
                                    fileName = fileName.replaceAll("\\\\", File.separator); //$NON-NLS-1$
                                } catch (Exception ex) {
                                    // Workaround for windows OS / JVM BUG
                                    fileName = fileName.replace("\\", File.separator); //$NON-NLS-1$
                                }

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

        mntm_Edger2[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        if (new EdgerDialog(getShell(), es).open() == IDialogConstants.OK_ID) {

                            final HashSet<GData2> oldLines = new HashSet<>();
                            final int oldCondlineCount = es.isVerbose() ? vm.getCondlines().size() : 0;
                            final int oldLineCount = es.isVerbose() ? vm.getLines().size() : 0;
                            if (es.isVerbose()) {
                                oldLines.addAll(vm.getLines().keySet());
                            }

                            vm.addSnapshot();
                            vm.addEdges(es);

                            if (es.isVerbose()) {
                                int unmatchedLines = 0;
                                for (GData2 g2 : vm.getLines().keySet()) {
                                    if (!oldLines.contains(g2) && g2.colourNumber == 4) {
                                        unmatchedLines += 1;
                                    }
                                }

                                MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
                                messageBox.setText(I18n.DIALOG_Info);

                                Object[] messageArguments = {
                                        vm.getCondlines().size() - oldCondlineCount,
                                        vm.getLines().size() - oldLineCount - unmatchedLines,
                                        unmatchedLines};
                                MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                                formatter.setLocale(MyLanguage.LOCALE);
                                formatter.applyPattern(I18n.EDGER_VerboseMsg);

                                messageBox.setMessage(formatter.format(messageArguments));
                                messageBox.open();
                            }
                        }
                        regainFocus();
                        return;
                    }
                }
                regainFocus();
            }
        });

        mntm_PrimGen2[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                new PrimGen2Dialog(getShell()).open();
                regainFocus();
            }
        });

        mntm_Rectifier[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        if (new RectifierDialog(getShell(), rs).open() == IDialogConstants.OK_ID) {
                            vm.addSnapshot();

                            final int[] result = vm.rectify(rs, true, true);

                            if (rs.isVerbose()) {
                                MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
                                messageBox.setText(I18n.DIALOG_Info);

                                Object[] messageArguments = {
                                        result[0],
                                        result[1],
                                        result[2],
                                        result[3]
                                };
                                MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                                formatter.setLocale(MyLanguage.LOCALE);
                                formatter.applyPattern(I18n.RECTIFIER_VerboseMsg);

                                messageBox.setMessage(formatter.format(messageArguments));
                                messageBox.open();
                            }
                        }
                        regainFocus();
                        return;
                    }
                }
                regainFocus();
            }
        });

        mntm_Isecalc[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        if (new IsecalcDialog(getShell(), is).open() == IDialogConstants.OK_ID) {
                            vm.addSnapshot();
                            vm.isecalc(is);
                        }
                        regainFocus();
                        return;
                    }
                }
                regainFocus();
            }
        });

        mntm_SlicerPro[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        if (new SlicerProDialog(getShell(), ss).open() == IDialogConstants.OK_ID) {
                            vm.addSnapshot();
                            vm.slicerpro(ss);
                        }
                        regainFocus();
                        return;
                    }
                }
                regainFocus();
            }
        });

        mntm_Intersector[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        if (new IntersectorDialog(getShell(), ins).open() == IDialogConstants.OK_ID) {
                            vm.addSnapshot();
                            vm.intersector(ins, true);
                        }
                        regainFocus();
                        return;
                    }
                }
                regainFocus();
            }
        });

        mntm_SlantingMatrixProjector[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        if (new SlantingMatrixProjectorDialog(getShell(), vm, mps).open() == IDialogConstants.OK_ID) {
                            vm.addSnapshot();
                            vm.projectWithSlantingMatrix(mps);
                        }
                        regainFocus();
                        return;
                    }
                }
                regainFocus();
            }
        });

        mntm_Lines2Pattern[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        if (new Lines2PatternDialog(getShell()).open() == IDialogConstants.OK_ID) {
                            vm.addSnapshot();
                            vm.lines2pattern();
                        }
                        regainFocus();
                        return;
                    }
                }
                regainFocus();
            }
        });

        mntm_PathTruder[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        if (new PathTruderDialog(getShell(), ps).open() == IDialogConstants.OK_ID) {
                            vm.addSnapshot();
                            vm.pathTruder(ps, true, null);
                        }
                        regainFocus();
                        return;
                    }
                }
                regainFocus();
            }
        });

        mntm_YTruder[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        if (new YTruderDialog(getShell(), ys).open() == IDialogConstants.OK_ID) {
                            vm.addSnapshot();
                            vm.yTruder(ys);
                        }
                        regainFocus();
                        return;
                    }
                }
                regainFocus();
            }
        });

        mntm_SymSplitter[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        if (new SymSplitterDialog(getShell(), sims).open() == IDialogConstants.OK_ID) {
                            vm.addSnapshot();
                            vm.symSplitter(sims);
                        }
                        regainFocus();
                        return;
                    }
                }
                regainFocus();
            }
        });

        mntm_Unificator[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                        if (new UnificatorDialog(getShell(), us).open() == IDialogConstants.OK_ID) {
                            vm.addSnapshot();
                            vm.unificator(us);
                        }
                        regainFocus();
                        return;
                    }
                }
                regainFocus();
            }
        });

        mntm_RingsAndCones[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getLockableDatFileReference().isReadOnly()) {
                        if (new RingsAndConesDialog(getShell(), ris).open() == IDialogConstants.OK_ID) {
                            c3d.getLockableDatFileReference().getVertexManager().addSnapshot();
                            RingsAndCones.solve(Editor3DWindow.getWindow().getShell(), c3d.getLockableDatFileReference(), cmp_Primitives[0].getPrimitives(), ris, true);
                        }
                        regainFocus();
                        return;
                    }
                }
                regainFocus();
            }
        });

        mntm_TJunctionFinder[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    DatFile df = c3d.getLockableDatFileReference();
                    if (df.equals(Project.getFileToEdit()) && !df.isReadOnly()) {
                        if (new TJunctionDialog(getShell(), tjs).open() == IDialogConstants.OK_ID) {
                            VertexManager vm = df.getVertexManager();
                            vm.addSnapshot();
                            vm.fixTjunctions(tjs);
                        }
                        regainFocus();
                        return;
                    }
                }
                regainFocus();
            }
        });

        mntm_MeshReducer[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    DatFile df = c3d.getLockableDatFileReference();
                    if (df.equals(Project.getFileToEdit()) && !df.isReadOnly()) {
                        if (new MeshReducerDialog(getShell(), ms).open() == IDialogConstants.OK_ID) {
                            VertexManager vm = df.getVertexManager();
                            vm.addSnapshot();
                            vm.meshReduce(0, ms);
                        }
                        regainFocus();
                        return;
                    }
                }
                regainFocus();
            }
        });

        mntm_Txt2Dat[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        DatFile df = c3d.getLockableDatFileReference();
                        if (df.isReadOnly()) {
                            regainFocus();
                            return;
                        }
                        VertexManager vm = df.getVertexManager();
                        vm.addSnapshot();
                        if (new Txt2DatDialog(getShell(), ts).open() == IDialogConstants.OK_ID && !ts.getText().trim().isEmpty()) {

                            java.awt.Font myFont;
                            float r = 0.1f;
                            float g = 0.1f;
                            float b = 0.1f;

                            if (ts.getFontData() == null) {
                                myFont = new java.awt.Font(org.nschmidt.ldparteditor.enums.Font.MONOSPACE.getFontData()[0].getName(), java.awt.Font.PLAIN, 32);
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
                                }
                                myFont = new java.awt.Font(fd.getName(), style, fd.getHeight());
                            }
                            GData anchorData = df.getDrawChainTail();
                            int lineNumber = df.getDrawPerLine_NOCLONE().getKey(anchorData);
                            Set<GData> triangleSet = TextTriangulator.triangulateText(myFont, r, g, b, ts.getText().trim(), ts.getFlatness().doubleValue(), ts.getInterpolateFlatness().doubleValue(), View.DUMMY_REFERENCE, df, ts.getFontHeight().intValue(), ts.getDeltaAngle().doubleValue());
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
                        }
                        regainFocus();
                        return;
                    }
                }
                regainFocus();
            }
        });

        // MARK Options

        mntm_UserConfigSave[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog fd = new FileDialog(getShell(), SWT.SAVE);
                fd.setText(I18n.E3D_UserConfigSelectSave);
                fd.setOverwrite(true);
                String[] filterExt = { "*.gz", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
                fd.setFilterExtensions(filterExt);
                String[] filterNames = {I18n.E3D_UserConfigFile, I18n.E3D_AllFiles};
                fd.setFilterNames(filterNames);

                String selected = fd.open();
                if (selected != null) {
                    final Editor3DWindowState winState = WorkbenchManager.getEditor3DWindowState();
                    if (!isReviewingAPart()) {
                        winState.setThreeDwindowConfig(getC3DStates());
                    }

                    if (editorSashForm[0] != null) {
                        winState.setEditorSashWeights(editorSashForm[0].getWeights());
                    }
                    winState.setLeftSashWeights(((SashForm) Editor3DDesign.getSashForm().getChildren()[0]).getWeights());
                    winState.setLeftSashWidth(Editor3DDesign.getSashForm().getWeights());
                    winState.setPrimitiveZoom(cmp_Primitives[0].getZoom());
                    winState.setPrimitiveZoomExponent(cmp_Primitives[0].getZoom_exponent());
                    winState.setPrimitiveViewport(cmp_Primitives[0].getViewport2());

                    WorkbenchManager.getPrimitiveCache().setPrimitiveCache(CompositePrimitive.getCache());
                    WorkbenchManager.getPrimitiveCache().setPrimitiveFileCache(CompositePrimitive.getFileCache());

                    WorkbenchManager.getUserSettingState().setRecentItems(getRecentItems());

                    if (!WorkbenchManager.saveWorkbench(selected)) {
                        MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                        messageBox.setText(I18n.DIALOG_Error);
                        messageBox.setMessage(I18n.E3D_UserConfigFailSave);
                        messageBox.open();
                    }
                }
            }
        });

        mntm_UserConfigLoad[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
                fd.setText(I18n.E3D_UserConfigSelectLoad);
                String[] filterExt = { "*.gz", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
                fd.setFilterExtensions(filterExt);
                String[] filterNames = {I18n.E3D_UserConfigFile, I18n.E3D_AllFiles};
                fd.setFilterNames(filterNames);

                String selected = fd.open();
                if (selected != null) {
                    boolean issueDetected = false;
                    try {
                        Files.delete(Paths.get(WorkbenchManager.CONFIG_GZ));
                        Files.copy(Paths.get(selected), Paths.get(WorkbenchManager.CONFIG_GZ));
                        issueDetected = !WorkbenchManager.loadWorkbench(WorkbenchManager.CONFIG_GZ);
                    } catch (IOException ioe) {
                        NLogger.error(getClass(), ioe);
                        issueDetected = true;
                    }
                    if (issueDetected) {
                        MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                        messageBox.setText(I18n.DIALOG_Error);
                        messageBox.setMessage(I18n.E3D_UserConfigFailLoad);
                        messageBox.open();
                        return;
                    }
                    WorkbenchManager.getUserSettingState().loadColours();
                    // Override colour 16
                    View.overrideColour16();
                    // Recompile
                    Editor3DWindow.getWindow().compileAll(true);
                    // Restore the viewport
                    closeAllComposite3D();
                    reloadC3DStates(WorkbenchManager.getEditor3DWindowState().getThreeDwindowConfig());
                    // Link renderes with dummy file
                    Project.setFileToEdit(View.DUMMY_DATFILE);
                    for (OpenGLRenderer renderer : renders) {
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
            }
        });

        mntm_ResetSettingsOnRestart[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
                messageBox.setText(I18n.DIALOG_Warning);
                messageBox.setMessage(I18n.E3D_DeleteConfig);
                int result = messageBox.open();
                if (result == SWT.CANCEL) {
                    regainFocus();
                    return;
                }
                WorkbenchManager.getUserSettingState().setResetOnStart(true);
                regainFocus();
            }
        });

        mntm_Options[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                OptionsDialog dialog = new OptionsDialog(getShell());
                dialog.run();
                regainFocus();
            }
        });

        mntm_SelectAnotherLDConfig[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog fd = new FileDialog(sh, SWT.OPEN);
                fd.setText(I18n.E3D_OpenLDConfig);
                fd.setFilterPath(WorkbenchManager.getUserSettingState().getLdrawFolderPath());

                String[] filterExt = { "*.ldr", "ldconfig.ldr", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                fd.setFilterExtensions(filterExt);
                String[] filterNames = { I18n.E3D_LDrawConfigurationFile1, I18n.E3D_LDrawConfigurationFile2, I18n.E3D_AllFiles };
                fd.setFilterNames(filterNames);

                String selected = fd.open();
                LDConfigUtils.reloadLDConfig(selected);
                regainFocus();
            }
        });

        mntm_DownloadLDConfig[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                LDConfigUtils.downloadLDConfig();
                regainFocus();
            }
        });

        mntm_DownloadCategories[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CategoriesUtils.downloadCategories();
                regainFocus();
            }
        });

        mntm_SavePalette[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                FileDialog dlg = new FileDialog(Editor3DWindow.getWindow().getShell(), SWT.SAVE);

                dlg.setFilterPath(Project.getLastVisitedPath());

                dlg.setFilterExtensions(new String[]{"*_pal.dat"}); //$NON-NLS-1$
                dlg.setOverwrite(true);

                // Change the title bar text
                dlg.setText(I18n.E3D_PaletteSave);

                // Calling open() will open and run the dialog.
                // It will return the selected file, or
                // null if user cancels
                String newPath = dlg.open();
                if (newPath != null) {
                    final File f = new File(newPath);
                    if (f.getParentFile() != null) {
                        Project.setLastVisitedPath(f.getParentFile().getAbsolutePath());
                    }

                    UTF8PrintWriter r = null;
                    try {
                        r = new UTF8PrintWriter(newPath);
                        int x = 0;
                        for (GColour col : WorkbenchManager.getUserSettingState().getUserPalette()) {
                            r.println("1 " + col + " " + x + " 0 0 1 0 0 0 1 0 0 0 1 rect.dat"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            x += 2;
                        }
                        r.flush();
                        r.close();
                    } catch (Exception ex) {
                        MessageBox messageBoxError = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                        messageBoxError.setText(I18n.DIALOG_Error);
                        messageBoxError.setMessage(I18n.DIALOG_CantSaveFile);
                        messageBoxError.open();
                    } finally {
                        if (r != null) {
                            r.close();
                        }
                    }

                }

                regainFocus();
            }
        });

        mntm_LoadPalette[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                FileDialog dlg = new FileDialog(Editor3DWindow.getWindow().getShell(), SWT.OPEN);

                dlg.setFilterPath(Project.getLastVisitedPath());

                dlg.setFilterExtensions(new String[]{"*_pal.dat"}); //$NON-NLS-1$
                dlg.setOverwrite(true);

                // Change the title bar text
                dlg.setText(I18n.E3D_PaletteLoad);

                // Calling open() will open and run the dialog.
                // It will return the selected file, or
                // null if user cancels
                String newPath = dlg.open();
                if (newPath != null) {
                    final File f = new File(newPath);
                    if (f.getParentFile() != null) {
                        Project.setLastVisitedPath(f.getParentFile().getAbsolutePath());
                    }

                    final Pattern WHITESPACE = Pattern.compile("\\s+"); //$NON-NLS-1$
                    ArrayList<GColour> pal = WorkbenchManager.getUserSettingState().getUserPalette();
                    ArrayList<GColour> newPal = new ArrayList<GColour>();


                    UTF8BufferedReader reader = null;
                    try {
                        reader = new UTF8BufferedReader(newPath);
                        String line;
                        while ((line = reader.readLine()) != null) {
                            final String[] data_segments = WHITESPACE.split(line.trim());
                            if (data_segments.length > 1) {
                                GColour c = DatParser.validateColour(data_segments[1], 0f, 0f, 0f, 1f);
                                if (c != null) {
                                    newPal.add(c.clone());
                                }
                            }
                        }
                        pal.clear();
                        pal.addAll(newPal);
                    } catch (LDParsingException ex) {
                    } catch (FileNotFoundException ex) {
                    } catch (UnsupportedEncodingException ex) {
                    } finally {
                        try {
                            if (reader != null)
                                reader.close();
                        } catch (LDParsingException ex2) {
                        }
                    }

                    reloadAllColours();

                }

                regainFocus();
            }
        });

        mntm_SetPaletteSize[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                final List<GColour> colours = WorkbenchManager.getUserSettingState().getUserPalette();

                final int[] frac = new int[]{2};
                if (new ValueDialogInt(getShell(), I18n.E3D_PaletteSetSize, "") { //$NON-NLS-1$

                    @Override
                    public void initializeSpinner() {
                        this.spn_Value[0].setMinimum(1);
                        this.spn_Value[0].setMaximum(100);
                        this.spn_Value[0].setValue(colours.size());
                    }

                    @Override
                    public void applyValue() {
                        frac[0] = this.spn_Value[0].getValue();
                    }
                }.open() == OK) {

                    final boolean reBuild = frac[0] != colours.size();

                    if (frac[0] > colours.size()) {
                        while (frac[0] > colours.size()) {
                            if (colours.size() < 17) {
                                if (colours.size() == 8) {
                                    colours.add(View.getLDConfigColour(72));
                                } else {
                                    colours.add(View.getLDConfigColour(colours.size()));
                                }
                            } else {
                                colours.add(View.getLDConfigColour(16));
                            }
                        }
                    } else {
                        while (frac[0] < colours.size()) {
                            colours.remove(colours.size() - 1);
                        }
                    }

                    if (reBuild) {
                        reloadAllColours();
                    }
                }

                regainFocus();
            }
        });

        mntm_ResetPalette[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                final List<GColour> colours = WorkbenchManager.getUserSettingState().getUserPalette();
                colours.clear();

                while (colours.size() < 17) {
                    if (colours.size() == 8) {
                        colours.add(View.getLDConfigColour(72));
                    } else {
                        colours.add(View.getLDConfigColour(colours.size()));
                    }
                }

                reloadAllColours();

                regainFocus();
            }
        });

        mntm_UploadLogs[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                String source = ""; //$NON-NLS-1$
                {
                    UTF8BufferedReader b1 = null, b2 = null;

                    StringBuilder code = new StringBuilder();

                    File l1 = new File(NLogger.ERROR_LOG);
                    File l2 = new File(NLogger.ERROR_LOG2);

                    if (l1.exists() || l2.exists()) {
                        try {
                            if (l1.exists()) {
                                b1 = new UTF8BufferedReader(NLogger.ERROR_LOG);
                                String line;
                                while ((line = b1.readLine()) != null) {
                                    code.append(line);
                                    code.append(StringHelper.getLineDelimiter());
                                }
                            }

                            if (l2.exists()) {
                                b2 = new UTF8BufferedReader(NLogger.ERROR_LOG2);
                                String line;
                                while ((line = b2.readLine()) != null) {
                                    code.append(line);
                                    code.append(StringHelper.getLineDelimiter());
                                }
                            }

                            source = code.toString();
                        } catch (Exception e1) {
                            if (b1 != null) {
                                try {
                                    b1.close();
                                } catch (Exception consumend) {}
                            }
                            if (b2 != null) {
                                try {
                                    b2.close();
                                } catch (Exception consumend) {}
                            }
                            MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                            messageBox.setText(I18n.DIALOG_Error);
                            messageBox.setMessage(I18n.E3D_LogUploadUnexpectedException);
                            messageBox.open();
                            regainFocus();
                            return;
                        } finally {
                            if (b1 != null) {
                                try {
                                    b1.close();
                                } catch (Exception consumend) {}
                            }
                            if (b2 != null) {
                                try {
                                    b2.close();
                                } catch (Exception consumend) {}
                            }
                        }
                    } else {
                        MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
                        messageBox.setText(I18n.DIALOG_Info);
                        messageBox.setMessage(I18n.E3D_LogUploadNoLogFiles);
                        messageBox.open();
                        regainFocus();
                        return;
                    }
                }

                LogUploadDialog dialog = new LogUploadDialog(getShell(), source);

                if (dialog.open() == IDialogConstants.OK_ID) {

                    UTF8BufferedReader b1 = null, b2 = null;

                    if (mntm_UploadLogs[0].getData() == null) {
                        mntm_UploadLogs[0].setData(0);
                    } else {
                        int uploadCount = (int) mntm_UploadLogs[0].getData();
                        uploadCount++;
                        if (uploadCount > 16) {
                            MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
                            messageBox.setText(I18n.DIALOG_Warning);
                            messageBox.setMessage(I18n.E3D_LogUploadLimit);
                            messageBox.open();
                            regainFocus();
                            return;
                        }
                        mntm_UploadLogs[0].setData(uploadCount);
                    }

                    try {
                        Thread.sleep(2000);
                        String url = "http://pastebin.com/api/api_post.php"; //$NON-NLS-1$
                        String charset = StandardCharsets.UTF_8.name();  // Or in Java 7 and later, use the constant: java.nio.charset.StandardCharsets.UTF_8.name()
                        String title = "[LDPartEditor " + I18n.VERSION_Version + "]"; //$NON-NLS-1$ //$NON-NLS-2$
                        String devKey = "79cf77977cd2d798dd02f07d93b01ddb"; //$NON-NLS-1$

                        StringBuilder code = new StringBuilder();

                        File l1 = new File(NLogger.ERROR_LOG);
                        File l2 = new File(NLogger.ERROR_LOG2);

                        if (l1.exists() || l2.exists()) {

                            if (l1.exists()) {
                                b1 = new UTF8BufferedReader(NLogger.ERROR_LOG);
                                String line;
                                while ((line = b1.readLine()) != null) {
                                    code.append(line);
                                    code.append(StringHelper.getLineDelimiter());
                                }
                            }

                            if (l2.exists()) {
                                b2 = new UTF8BufferedReader(NLogger.ERROR_LOG2);
                                String line;
                                while ((line = b2.readLine()) != null) {
                                    code.append(line);
                                    code.append(StringHelper.getLineDelimiter());
                                }
                            }

                            String query = String.format("api_option=paste&api_user_key=%s&api_paste_private=%s&api_paste_name=%s&api_dev_key=%s&api_paste_code=%s",  //$NON-NLS-1$
                                    URLEncoder.encode("4cc892c8052bd17d805a1a2907ee8014", charset), //$NON-NLS-1$
                                    URLEncoder.encode("0", charset),//$NON-NLS-1$
                                    URLEncoder.encode(title, charset),
                                    URLEncoder.encode(devKey, charset),
                                    URLEncoder.encode(code.toString(), charset)
                                    );

                            URLConnection connection = new URL(url).openConnection();
                            connection.setDoOutput(true); // Triggers POST.
                            connection.setRequestProperty("Accept-Charset", charset); //$NON-NLS-1$
                            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset); //$NON-NLS-1$ //$NON-NLS-2$

                            try (OutputStream output = connection.getOutputStream()) {
                                output.write(query.getBytes(charset));
                            }

                            BufferedReader response =new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8")); //$NON-NLS-1$
                            MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
                            messageBox.setText(I18n.DIALOG_Info);
                            Object[] messageArguments = {response.readLine()};
                            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                            formatter.setLocale(MyLanguage.LOCALE);
                            formatter.applyPattern(I18n.E3D_LogUploadSuccess);
                            messageBox.setMessage(formatter.format(messageArguments));
                            messageBox.open();
                        } else {
                            MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
                            messageBox.setText(I18n.DIALOG_Info);
                            messageBox.setMessage(I18n.E3D_LogUploadNoLogFiles);
                            messageBox.open();
                        }
                    } catch (Exception e1) {
                        MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                        messageBox.setText(I18n.DIALOG_Info);
                        messageBox.setMessage(I18n.E3D_LogUploadUnexpectedException);
                        messageBox.open();
                    } finally {
                        if (b1 != null) {
                            try {
                                b1.close();
                            } catch (Exception consumend) {}
                        }
                        if (b2 != null) {
                            try {
                                b2.close();
                            } catch (Exception consumend) {}
                        }
                    }
                }
                regainFocus();
            }
        });

        mntm_AntiAliasing[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                WorkbenchManager.getUserSettingState().setAntiAliasing(mntm_AntiAliasing[0].getSelection());
                regainFocus();
            }
        });

        mntm_OpenGL33Engine[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                WorkbenchManager.getUserSettingState().setOpenGL33Engine(mntm_OpenGL33Engine[0].getSelection());
                WorkbenchManager.getUserSettingState().setVulkanEngine(false);
                if (NLogger.DEBUG) {
                    mntm_VulkanEngine[0].setSelection(false);
                }
                regainFocus();
            }
        });

        if (NLogger.DEBUG) mntm_VulkanEngine[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                WorkbenchManager.getUserSettingState().setVulkanEngine(mntm_VulkanEngine[0].getSelection());
                WorkbenchManager.getUserSettingState().setOpenGL33Engine(false);
                mntm_OpenGL33Engine[0].setSelection(false);
                regainFocus();
            }
        });

        //        mntm_SyncWithTextEditor[0].addSelectionListener(new SelectionAdapter() {
        //            @Override
        //            public void widgetSelected(SelectionEvent e) {
        //                WorkbenchManager.getUserSettingState().getSyncWithTextEditor().set(mntm_SyncWithTextEditor[0].getSelection());
        //                mntm_SyncLpeInline[0].setEnabled(mntm_SyncWithTextEditor[0].getSelection());
        //                regainFocus();
        //            }
        //        });

        mntm_SyncLpeInline[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                WorkbenchManager.getUserSettingState().getSyncWithLpeInline().set(mntm_SyncLpeInline[0].getSelection());
                regainFocus();
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
                        vm.addSnapshot();
                        vm.flipSelection();
                        regainFocus();
                        return;
                    }
                }
                regainFocus();
            }
        });

        mntm_SubdivideCatmullClark[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
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
            }
        });

        mntm_SubdivideLoop[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (OpenGLRenderer renderer : renders) {
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
                        fd.setText(I18n.E3D_OpenPngImage);
                        try {
                            File f = new File(png.texturePath);
                            fd.setFilterPath(f.getParent());
                            fd.setFileName(f.getName());
                        } catch (Exception ex) {

                        }

                        String[] filterExt = { "*.png", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
                        fd.setFilterExtensions(filterExt);
                        String[] filterNames = { I18n.E3D_PortableNetworkGraphics, I18n.E3D_AllFiles};
                        fd.setFilterNames(filterNames);
                        String texturePath = fd.open();

                        if (texturePath != null) {

                            String newText = png.getString(png.offset, png.angleA, png.angleB, png.angleC, png.scale, texturePath);

                            GDataPNG newPngPicture = new GDataPNG(newText, png.offset, png.angleA, png.angleB, png.angleC, png.scale, texturePath, View.DUMMY_REFERENCE);
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
                        break;
                    }
                }
                regainFocus();
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
                        break;
                    }
                }
                regainFocus();
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

                        GDataPNG newPngPicture = new GDataPNG(newText, png.offset, spn.getValue(), png.angleB, png.angleC, png.scale, png.texturePath, View.DUMMY_REFERENCE);
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

                        GDataPNG newPngPicture = new GDataPNG(newText, png.offset, png.angleA, spn.getValue(), png.angleC, png.scale, png.texturePath, View.DUMMY_REFERENCE);
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

                        GDataPNG newPngPicture = new GDataPNG(newText, png.offset, png.angleA, png.angleB, spn.getValue(), png.scale, png.texturePath, View.DUMMY_REFERENCE);
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

                        GDataPNG newPngPicture = new GDataPNG(newText, png.offset, png.angleA, png.angleB, png.angleC, newScale, png.texturePath, View.DUMMY_REFERENCE);
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

                        GDataPNG newPngPicture = new GDataPNG(newText, png.offset, png.angleA, png.angleB, png.angleC, newScale, png.texturePath, View.DUMMY_REFERENCE);
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

                        GDataPNG newPngPicture = new GDataPNG(newText, newOffset, png.angleA, png.angleB, png.angleC, png.scale, png.texturePath, View.DUMMY_REFERENCE);
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

                        GDataPNG newPngPicture = new GDataPNG(newText, newOffset, png.angleA, png.angleB, png.angleC, png.scale, png.texturePath, View.DUMMY_REFERENCE);
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

                        GDataPNG newPngPicture = new GDataPNG(newText, newOffset, png.angleA, png.angleB, png.angleC, png.scale, png.texturePath, View.DUMMY_REFERENCE);
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
                WorkbenchManager.getUserSettingState().setIconSize(-1);
                regainFocus();
            }
        });
        mntm_IconSize2[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                WorkbenchManager.getUserSettingState().setIconSize(0);
                regainFocus();
            }
        });
        mntm_IconSize3[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                WorkbenchManager.getUserSettingState().setIconSize(1);
                regainFocus();
            }
        });
        mntm_IconSize4[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                WorkbenchManager.getUserSettingState().setIconSize(2);
                regainFocus();
            }
        });
        mntm_IconSize5[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                WorkbenchManager.getUserSettingState().setIconSize(3);
                regainFocus();
            }
        });
        mntm_IconSize6[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                WorkbenchManager.getUserSettingState().setIconSize(4);
                regainFocus();
            }
        });

        Project.createDefault();
        treeItem_Project[0].setData(Project.getProjectPath());
        treeItem_Official[0].setData(WorkbenchManager.getUserSettingState().getLdrawFolderPath());
        treeItem_Unofficial[0].setData(WorkbenchManager.getUserSettingState().getUnofficialFolderPath());

        try {
            new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, false, new IRunnableWithProgress() {
                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask(I18n.E3D_LoadingLibrary, IProgressMonitor.UNKNOWN);
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
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
                        }
                    });
                }
            });
        } catch (InvocationTargetException consumed) {
        } catch (InterruptedException consumed) {
        }

        tabFolder_OpenDatFiles[0].getItem(0).setData(View.DUMMY_DATFILE);
        tabFolder_OpenDatFiles[0].getItem(1).setData(Project.getFileToEdit());
        Project.addOpenedFile(Project.getFileToEdit());

        tabFolder_OpenDatFiles[0].addCTabFolder2Listener(new CTabFolder2Listener() {

            @Override
            public void showList(CTabFolderEvent event) {}

            @Override
            public void restore(CTabFolderEvent event) {}

            @Override
            public void minimize(CTabFolderEvent event) {}

            @Override
            public void maximize(CTabFolderEvent event) {}

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

        tabFolder_OpenDatFiles[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                if (Editor3DWindow.getNoSyncDeadlock().compareAndSet(false, true)) {
                    if (tabFolder_OpenDatFiles[0].getData() != null) {
                        Editor3DWindow.getNoSyncDeadlock().set(false);
                        return;
                    }
                    DatFile df = null;
                    if (tabFolder_OpenDatFiles[0].getSelection() != null && (df = (DatFile) tabFolder_OpenDatFiles[0].getSelection().getData()) != null) {

                        openFileIn3DEditor(df);
                        if (!df.equals(View.DUMMY_DATFILE) && WorkbenchManager.getUserSettingState().isSyncingTabs()) {
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
                            if (fileIsOpenInTextEditor) {
                                for (EditorTextWindow w : Project.getOpenTextWindows()) {
                                    for (final CTabItem t : w.getTabFolder().getItems()) {
                                        if (df.equals(((CompositeTab) t).getState().getFileNameObj())) {
                                            w.getTabFolder().setSelection(t);
                                            ((CompositeTab) t).getControl().getShell().forceActive();
                                            if (w.isSeperateWindow()) {
                                                w.open();
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
            }
        });

        btn_SyncTabs[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                WorkbenchManager.getUserSettingState().setSyncingTabs(btn_SyncTabs[0].getSelection());
            }
        });

        txt_Search[0].setText(" "); //$NON-NLS-1$
        txt_Search[0].setText(""); //$NON-NLS-1$

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
            tabFolder_OpenDatFiles[0].setSelection(2);
            Project.getFileToEdit().parseForData(true);
            Project.getFileToEdit().setLastSelectedComposite(Editor3DWindow.renders.get(0).getC3D());
            for (OpenGLRenderer renderer : renders) {
                renderer.getC3D().setLockableDatFileReference(Project.getFileToEdit());
            }
        }

        this.open();

        // Dispose all resources (never delete this!)
        CSG.executorService.shutdown();
        cmp_Primitives[0].getOpenGL().dispose();
        ResourceManager.dispose();
        SWTResourceManager.dispose();
        // Dispose the display (never delete this, too!)
        try {
            Display.getCurrent().dispose();
        } catch (SWTException consumed) {
            // Display can already be disposed.
        }
    }

    protected void addRecentFile(String projectPath) {
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
        setAddingDistance(false);
        setAddingProtractor(false);
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
            btn_AddDistance[0].setSelection(false);
            btn_AddProtractor[0].setSelection(false);
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
        case 6:
            setAddingDistance(!isAddingDistance());
            btn_AddDistance[0].setSelection(isAddingDistance());
            setAddingSomething(isAddingDistance());
            clickSingleBtn(btn_AddDistance[0]);
            break;
        case 7:
            setAddingProtractor(!isAddingProtractor());
            btn_AddProtractor[0].setSelection(isAddingProtractor());
            setAddingSomething(isAddingProtractor());
            clickSingleBtn(btn_AddProtractor[0]);
            break;
        }
    }

    public void toggleInsertAtCursor() {
        setInsertingAtCursorPosition(!isInsertingAtCursorPosition());
        btn_InsertAtCursorPosition[0].setSelection(isInsertingAtCursorPosition());
        clickSingleBtn(btn_InsertAtCursorPosition[0]);
    }

    public void toggleMoveAdjacentData() {
        setMovingAdjacentData(!isMovingAdjacentData());
        btn_MoveAdjacentData[0].setSelection(isMovingAdjacentData());
        clickSingleBtn(btn_MoveAdjacentData[0]);
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
     * The Shell-Close-Event
     */
    @Override
    protected void handleShellCloseEvent() {
        boolean unsavedProjectFiles = false;
        Set<DatFile> unsavedFiles = new HashSet<DatFile>(Project.getUnsavedFiles());
        for (DatFile df : unsavedFiles) {
            final String text = df.getText();
            if ((!text.equals(df.getOriginalText()) || df.isVirtual() && !text.trim().isEmpty()) && !text.equals(WorkbenchManager.getDefaultFileHeader())) {
                MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.CANCEL | SWT.NO);
                messageBox.setText(I18n.DIALOG_UnsavedChangesTitle);

                Object[] messageArguments = {df.getShortName()};
                MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                formatter.setLocale(MyLanguage.LOCALE);
                formatter.applyPattern(I18n.DIALOG_UnsavedChanges);
                messageBox.setMessage(formatter.format(messageArguments));

                int result = messageBox.open();

                if (result == SWT.NO) {
                    // Remove file from tree
                    updateTree_removeEntry(df);
                } else if (result == SWT.YES) {
                    if (df.save()) {
                        Editor3DWindow.getWindow().addRecentFile(df);
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
            if (w.isSeperateWindow()) {
                w.getShell().close();
            } else {
                w.closeAllTabs();
            }
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

        final boolean ENABLE_DEFAULT_PROJECT_SAVE = !(Math.random() + 1 > 0);
        if (unsavedProjectFiles && Project.isDefaultProject() && ENABLE_DEFAULT_PROJECT_SAVE) {
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
                    formatter.setLocale(MyLanguage.LOCALE);
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
        winState.setLeftSashWeights(((SashForm) Editor3DDesign.getSashForm().getChildren()[0]).getWeights());
        winState.setLeftSashWidth(Editor3DDesign.getSashForm().getWeights());
        winState.setPrimitiveZoom(cmp_Primitives[0].getZoom());
        winState.setPrimitiveZoomExponent(cmp_Primitives[0].getZoom_exponent());
        winState.setPrimitiveViewport(cmp_Primitives[0].getViewport2());

        WorkbenchManager.getPrimitiveCache().setPrimitiveCache(CompositePrimitive.getCache());
        WorkbenchManager.getPrimitiveCache().setPrimitiveFileCache(CompositePrimitive.getFileCache());

        WorkbenchManager.getUserSettingState().setRecentItems(getRecentItems());
        // Save the workbench
        WorkbenchManager.saveWorkbench(WorkbenchManager.CONFIG_GZ);
        setReturnCode(CANCEL);
        close();
    }

    private void saveComposite3DStates(Control c, ArrayList<Composite3DState> c3dStates, String parentPath, String path) {
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
        st.setGridScale(c3d.getGrid_scale());
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
        updateTabs();
    }

    /**
     * Updates the tree for new unsaved entries
     */
    public void updateTree_selectedDatFile(DatFile sdf) {
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
        for (TreeItem item : categories) {
            ArrayList<TreeItem> datFileTreeItems = item.getItems();
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
        if (tabFolder_OpenDatFiles[0].getData() != null) {
            return;
        }
        tabFolder_OpenDatFiles[0].setData(true);
        for (CTabItem c : tabFolder_OpenDatFiles[0].getItems()) {
            c.dispose();
        }

        {
            CTabItem tItem = new CTabItem(tabFolder_OpenDatFiles[0], SWT.NONE);
            tItem.setText(I18n.E3D_NoFileSelected);
            tItem.setData(View.DUMMY_DATFILE);
        }

        for (Iterator<DatFile> iterator = Project.getOpenedFiles().iterator(); iterator.hasNext();) {
            DatFile df3 = iterator.next();
            if (View.DUMMY_DATFILE.equals(df3)) {
                iterator.remove();
            }
        }

        for (DatFile df2 : Project.getOpenedFiles()) {
            CTabItem tItem = new CTabItem(tabFolder_OpenDatFiles[0], SWT.NONE);
            tItem.setText(df2.getShortName() + (Project.getUnsavedFiles().contains(df2) ? "*" : "")); //$NON-NLS-1$ //$NON-NLS-2$
            tItem.setData(df2);
            if (df2.equals(Project.getFileToEdit())) {
                tabFolder_OpenDatFiles[0].setSelection(tItem);
                isSelected = true;
            }
        }
        if (!isSelected) {
            tabFolder_OpenDatFiles[0].setSelection(0);
            Project.setFileToEdit(View.DUMMY_DATFILE);
        }
        tabFolder_OpenDatFiles[0].setData(null);
        tabFolder_OpenDatFiles[0].layout();
        tabFolder_OpenDatFiles[0].redraw();
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

    public boolean isAddingDistance() {
        return addingDistance;
    }

    public void setAddingDistance(boolean addingDistance) {
        this.addingDistance = addingDistance;
    }

    public boolean isAddingProtractor() {
        return addingProtractor;
    }

    public void setAddingProtractor(boolean addingProtractor) {
        this.addingProtractor = addingProtractor;
    }

    public void disableAddAction() {
        addingSomething = false;
        addingVertices = false;
        addingLines = false;
        addingTriangles = false;
        addingQuads = false;
        addingCondlines = false;
        addingSubfiles = false;
        addingDistance = false;
        addingProtractor = false;
        btn_AddVertex[0].setSelection(false);
        btn_AddLine[0].setSelection(false);
        btn_AddTriangle[0].setSelection(false);
        btn_AddQuad[0].setSelection(false);
        btn_AddCondline[0].setSelection(false);
        btn_AddDistance[0].setSelection(false);
        btn_AddProtractor[0].setSelection(false);
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
        WorkbenchManager.getUserSettingState().setMovingAdjacentData(movingAdjacentData);
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
            NButton btn_EndPartReview = new NButton(cmp_SyncAndReview[0], SWT.NONE);
            btn_EndPartReview.setText(I18n.E3D_EndPartReview);
            btn_EndPartReview.setData(partsForReview);
            btn_EndPartReview.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    setReviewingAPart(false, null);
                }
            });
        } else {
            // Close all review files
            partsForReview = (Set<DatFile>) cmp_SyncAndReview[0].getChildren()[1].getData();
            for (DatFile df : partsForReview) {
                Project.removeOpenedFile(df);
                closeDatfile(df);
            }
            // Hide "End Review" button
            cmp_SyncAndReview[0].getChildren()[1].dispose();
            // Reset project
            Project.setFileToEdit(View.DUMMY_DATFILE);
            Project.setProjectPath(new File(Project.DEFAULT_PROJECT_PATH).getAbsolutePath());
            getShell().setText(Version.getApplicationName() + " " + Version.getVersion() + " (" + WorkbenchManager.getUserSettingState().getOpenGLVersionString() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            getShell().update();
            treeItem_Project[0].setText(I18n.PROJECT_NewProject);
            treeItem_Project[0].setData(Project.getProjectPath());
            treeItem_Project[0].getParent().build();
            treeItem_Project[0].getParent().redraw();
            treeItem_Project[0].getParent().update();
            // Restore the viewport
            closeAllComposite3D();
            reloadC3DStates(WorkbenchManager.getEditor3DWindowState().getThreeDwindowConfig());
        }
        cmp_SyncAndReview[0].getParent().layout(true);

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
        if (View.hasLDConfigColour(num)) {
            gColour2[0] = View.getLDConfigColour(num);
        } else {
            num = -1;
        }
        Editor3DWindow.getWindow().setLastUsedColour(gColour2[0]);
        btn_LastUsedColour[0].clearPaintListeners();
        btn_LastUsedColour[0].clearSelectionListeners();
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
                if (gColour2[0].getA() >= .99f) {
                    e.gc.drawImage(ResourceManager.getImage("icon16_transparent.png"), 0, 0, imgSize, imgSize, x, y, w, h); //$NON-NLS-1$
                } else if (gColour2[0].getA() == 0f) {
                    e.gc.drawImage(ResourceManager.getImage("icon16_randomColours.png"), 0, 0, imgSize, imgSize, x, y, w, h); //$NON-NLS-1$
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
                    Project.getFileToEdit().getVertexManager().colourChangeSelection(num, gColour2[0].getR(), gColour2[0].getG(), gColour2[0].getB(), gColour2[0].getA(), true);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        if (num != -1) {

            Object[] messageArguments = {num, View.getLDConfigColourName(num)};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.LOCALE);
            formatter.applyPattern(I18n.EDITORTEXT_Colour1);

            btn_LastUsedColour[0].setToolTipText(formatter.format(messageArguments));
        } else {
            StringBuilder colourBuilder = new StringBuilder();
            colourBuilder.append("0x2"); //$NON-NLS-1$
            colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getR())).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getG())).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getB())).toUpperCase());

            Object[] messageArguments = {colourBuilder.toString()};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.LOCALE);
            formatter.applyPattern(I18n.EDITORTEXT_Colour2);

            btn_LastUsedColour[0].setToolTipText(formatter.format(messageArguments));
            if (gColour2[0].getA() == 0f) btn_LastUsedColour[0].setToolTipText(I18n.COLOURDIALOG_RandomColours);
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
        canvasList.clear();
        ArrayList<OpenGLRenderer> renders2 = new ArrayList<OpenGLRenderer>(renders);
        for (OpenGLRenderer renderer : renders2) {
            Composite3D c3d = renderer.getC3D();
            c3d.getModifier().closeView();
        }
        renders.clear();
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
        fd.setText(I18n.E3D_CreateNewDat);

        fd.setFilterPath(Project.getLastVisitedPath());

        String[] filterExt = { "*.dat", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
        fd.setFilterExtensions(filterExt);
        String[] filterNames = { I18n.E3D_LDrawSourceFile, I18n.E3D_AllFiles };
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

                    String typeSuffix = ""; //$NON-NLS-1$
                    String folderPrefix = ""; //$NON-NLS-1$
                    String subfilePrefix = ""; //$NON-NLS-1$
                    String path = new File(selected).getParent();
                    TreeItem parent = this.treeItem_ProjectParts[0];

                    if (path.endsWith(File.separator + "S") || path.endsWith(File.separator + "s")) { //$NON-NLS-1$ //$NON-NLS-2$
                        typeSuffix = "Unofficial_Subpart"; //$NON-NLS-1$
                        folderPrefix = "s\\"; //$NON-NLS-1$
                        subfilePrefix = "~"; //$NON-NLS-1$
                        parent = this.treeItem_ProjectSubparts[0];
                    } else if (path.endsWith(File.separator + "P" + File.separator + "48") || path.endsWith(File.separator + "p" + File.separator + "48")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                        typeSuffix = "Unofficial_48_Primitive"; //$NON-NLS-1$
                        folderPrefix = "48\\"; //$NON-NLS-1$
                        parent = this.treeItem_ProjectPrimitives48[0];
                    } else if (path.endsWith(File.separator + "P" + File.separator + "8") || path.endsWith(File.separator + "p" + File.separator + "8")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                        typeSuffix = "Unofficial_8_Primitive"; //$NON-NLS-1$
                        folderPrefix = "8\\"; //$NON-NLS-1$
                        parent = this.treeItem_ProjectPrimitives8[0];
                    } else if (path.endsWith(File.separator + "P") || path.endsWith(File.separator + "p")) { //$NON-NLS-1$ //$NON-NLS-2$
                        typeSuffix = "Unofficial_Primitive"; //$NON-NLS-1$
                        parent = this.treeItem_ProjectPrimitives[0];
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

                    TreeItem ti = new TreeItem(parent, SWT.NONE);
                    StringBuilder nameSb = new StringBuilder(new File(df.getNewName()).getName());
                    nameSb.append(I18n.E3D_NewFile);
                    ti.setText(nameSb.toString());
                    ti.setData(df);

                    @SuppressWarnings("unchecked")
                    ArrayList<DatFile> cachedReferences = (ArrayList<DatFile>) this.treeItem_ProjectParts[0].getData();
                    cachedReferences.add(df);

                    Project.addUnsavedFile(df);
                    updateTree_renamedEntries();
                    updateTree_unsavedEntries();
                    updateTree_selectedDatFile(df);

                    openDatFile(df, where, null);
                    return df;
                }
            } else {
                break;
            }
        }
        return null;
    }

    public DatFile openDatFile(Shell sh, OpenInWhat where, String filePath, boolean canRevert) {


        FileDialog fd = new FileDialog(sh, SWT.OPEN);
        fd.setText(I18n.E3D_OpenDatFile);

        fd.setFilterPath(Project.getLastVisitedPath());

        String[] filterExt = { "*.dat", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
        fd.setFilterExtensions(filterExt);
        String[] filterNames = {I18n.E3D_LDrawSourceFile, I18n.E3D_AllFiles};
        fd.setFilterNames(filterNames);

        String selected = filePath == null ? fd.open() : filePath;
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

                // FIXME Needs code cleanup!!
                df = original;

                if (canRevert && Project.getUnsavedFiles().contains(df)) {
                    if (Editor3DWindow.getWindow().revert(df)) {
                        updateTree_unsavedEntries();
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
                }

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
                if (cachedReferences != null) cachedReferences.add(df);
            }
            ti = new TreeItem(this.treeItem_ProjectParts[0], SWT.NONE);
            break;
            case SUBPART:
            {
                @SuppressWarnings("unchecked")
                ArrayList<DatFile> cachedReferences = (ArrayList<DatFile>) this.treeItem_ProjectSubparts[0].getData();
                if (cachedReferences != null) cachedReferences.add(df);
            }
            ti = new TreeItem(this.treeItem_ProjectSubparts[0], SWT.NONE);
            break;
            case PRIMITIVE:
            {
                @SuppressWarnings("unchecked")
                ArrayList<DatFile> cachedReferences = (ArrayList<DatFile>) this.treeItem_ProjectPrimitives[0].getData();
                if (cachedReferences != null) cachedReferences.add(df);
            }
            ti = new TreeItem(this.treeItem_ProjectPrimitives[0], SWT.NONE);
            break;
            case PRIMITIVE48:
            {
                @SuppressWarnings("unchecked")
                ArrayList<DatFile> cachedReferences = (ArrayList<DatFile>) this.treeItem_ProjectPrimitives48[0].getData();
                if (cachedReferences != null) cachedReferences.add(df);
            }
            ti = new TreeItem(this.treeItem_ProjectPrimitives48[0], SWT.NONE);
            break;
            case PRIMITIVE8:
            {
                @SuppressWarnings("unchecked")
                ArrayList<DatFile> cachedReferences = (ArrayList<DatFile>) this.treeItem_ProjectPrimitives8[0].getData();
                if (cachedReferences != null) cachedReferences.add(df);
            }
            ti = new TreeItem(this.treeItem_ProjectPrimitives8[0], SWT.NONE);
            break;
            default:
            {
                @SuppressWarnings("unchecked")
                ArrayList<DatFile> cachedReferences = (ArrayList<DatFile>) this.treeItem_ProjectParts[0].getData();
                if (cachedReferences != null) cachedReferences.add(df);
            }
            ti = new TreeItem(this.treeItem_ProjectParts[0], SWT.NONE);
            break;
            }

            StringBuilder nameSb = new StringBuilder(new File(df.getNewName()).getName());

            nameSb.append(I18n.E3D_NewFile);

            ti.setText(nameSb.toString());
            ti.setData(df);

            if (canRevert && Project.getUnsavedFiles().contains(df)) {
                if (Editor3DWindow.getWindow().revert(df)) {
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
                        updateTree_unsavedEntries();
                        return null;
                    }
                }
            }

            updateTree_unsavedEntries();

            openDatFile(df, where, null);
            return df;
        }
        return null;
    }

    public boolean openDatFile(DatFile df, OpenInWhat where, ApplicationWindow tWin) {
        if (where == OpenInWhat.EDITOR_3D || where == OpenInWhat.EDITOR_TEXT_AND_3D) {
            if (renders.isEmpty()) {
                if ("%EMPTY%".equals(Editor3DWindow.getSashForm().getChildren()[1].getData())) { //$NON-NLS-1$
                    int[] mainSashWeights = Editor3DWindow.getSashForm().getWeights();
                    Editor3DWindow.getSashForm().getChildren()[1].dispose();
                    CompositeContainer cmp_Container = new CompositeContainer(Editor3DWindow.getSashForm(), false);
                    cmp_Container.moveBelow(Editor3DWindow.getSashForm().getChildren()[0]);
                    df.parseForData(true);
                    Project.setFileToEdit(df);
                    boolean hasState = hasState(df, cmp_Container.getComposite3D());
                    cmp_Container.getComposite3D().setLockableDatFileReference(df);
                    if (!hasState) cmp_Container.getComposite3D().getModifier().zoomToFit();
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
            updateTree_selectedDatFile(df);
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
            txt_Line[0].setText(""); //$NON-NLS-1$
            btn_MoveAdjacentData2[0].setSelection(false);
            spn_SelectionAngle[0].setEnabled(false);
            spn_SelectionLength[0].setEnabled(false);
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
            spn_SelectionAngle[0].setValue(BigDecimal.ZERO);
            spn_SelectionLength[0].setValue(BigDecimal.ONE);
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
            lbl_SelectionAngle[0].setText(I18n.E3D_ProtractorAngle);
            lbl_SelectionLength[0].setText(I18n.E3D_ProtractorLength);
            lbl_SelectionX1[0].setText(I18n.E3D_PositionX1);
            lbl_SelectionY1[0].setText(I18n.E3D_PositionY1);
            lbl_SelectionZ1[0].setText(I18n.E3D_PositionZ1);
            lbl_SelectionX2[0].setText(I18n.E3D_PositionX2);
            lbl_SelectionY2[0].setText(I18n.E3D_PositionY2);
            lbl_SelectionZ2[0].setText(I18n.E3D_PositionZ2);
            lbl_SelectionX3[0].setText(I18n.E3D_PositionX3);
            lbl_SelectionY3[0].setText(I18n.E3D_PositionY3);
            lbl_SelectionZ3[0].setText(I18n.E3D_PositionZ3);
            lbl_SelectionX4[0].setText(I18n.E3D_PositionX4);
            lbl_SelectionY4[0].setText(I18n.E3D_PositionY4);
            lbl_SelectionZ4[0].setText(I18n.E3D_PositionZ4);
            lbl_SelectionZ4[0].getParent().layout();
            updatingSelectionTab = false;
        } else {
            NLogger.error(getClass(), new SWTException(SWT.ERROR_THREAD_INVALID_ACCESS, "A wrong thread tries to access the GUI!")); //$NON-NLS-1$
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    try {
                        updatingSelectionTab = true;
                        txt_Line[0].setText(""); //$NON-NLS-1$
                        btn_MoveAdjacentData2[0].setSelection(false);
                        spn_SelectionAngle[0].setEnabled(false);
                        spn_SelectionLength[0].setEnabled(false);
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
                        lbl_SelectionAngle[0].setText(I18n.E3D_ProtractorAngle);
                        lbl_SelectionLength[0].setText(I18n.E3D_ProtractorLength);
                        lbl_SelectionX1[0].setText(I18n.E3D_PositionX1);
                        lbl_SelectionY1[0].setText(I18n.E3D_PositionY1);
                        lbl_SelectionZ1[0].setText(I18n.E3D_PositionZ1);
                        lbl_SelectionX2[0].setText(I18n.E3D_PositionX2);
                        lbl_SelectionY2[0].setText(I18n.E3D_PositionY2);
                        lbl_SelectionZ2[0].setText(I18n.E3D_PositionZ2);
                        lbl_SelectionX3[0].setText(I18n.E3D_PositionX3);
                        lbl_SelectionY3[0].setText(I18n.E3D_PositionY3);
                        lbl_SelectionZ3[0].setText(I18n.E3D_PositionZ3);
                        lbl_SelectionX4[0].setText(I18n.E3D_PositionX4);
                        lbl_SelectionY4[0].setText(I18n.E3D_PositionY4);
                        lbl_SelectionZ4[0].setText(I18n.E3D_PositionZ4);
                        lbl_SelectionZ4[0].getParent().layout();
                        updatingSelectionTab = false;
                    } catch (Exception ex) {
                        NLogger.error(getClass(), ex);
                    }
                }
            });
        }
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
        sels.setType(mntm_WithSameType[0].getSelection());
        sels.setEdgeAdjacency(mntm_WithAdjacency[0].getSelection());
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

    public boolean isFileNameAllocated(String dir, DatFile df, boolean createNew) {

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

    /**
     * @return null if the file is not allocated
     */
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
        if (lbl_selectedPrimitiveItem[0] == null) return;
        if (p == null) {
            lbl_selectedPrimitiveItem[0].setText(I18n.E3D_NoPrimitiveSelected);
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

    public ArrayList<String> getRecentItems() {
        return recentItems;
    }

    private void setLineSize(SphereGL20 sp, SphereGL20 sp_inv, float line_width1000, float line_width, float line_width_gl, NButton btn) {
        final boolean useLegacyGL = WorkbenchManager.getUserSettingState().getOpenGLVersion() == 20;
        View.lineWidth1000[0] = line_width1000;
        View.lineWidth[0] = line_width;
        View.lineWidthGL[0] = line_width_gl;
        if (useLegacyGL) {
            GL20Primitives.SPHERE = sp;
            GL20Primitives.SPHERE_INV = sp_inv;
            compileAll(false);
        }
        clickSingleBtn(btn);
    }

    public void compileAll(boolean forceParsing) {
        Set<DatFile> dfs = new HashSet<DatFile>();
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
            if (r.getC3D().getLockableDatFileReference().equals(Project.getFileToEdit())) {
                r.getC3D().getCanvas().setFocus();
                return;
            }
        }
    }

    public void mntm_Manipulator_0_toOrigin() {
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

    public void mntm_Manipulator_XIII_toWorld() {
        if (Project.getFileToEdit() != null) {
            for (OpenGLRenderer renderer : renders) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                    Vector4f t = new Vector4f(c3d.getManipulator().getPosition());
                    BigDecimal[] T = c3d.getManipulator().getAccuratePosition();
                    c3d.getManipulator().reset();
                    c3d.getManipulator().getPosition().set(t);
                    c3d.getManipulator().setAccuratePosition(T[0], T[1], T[2]);
                }
            }
        }
        regainFocus();
    }

    public void mntm_Manipulator_X_XReverse() {
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

    public void mntm_Manipulator_XI_YReverse() {
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

    public void mntm_Manipulator_XII_ZReverse() {
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

    public void mntm_Manipulator_1_cameraToPos() {
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

    public void mntm_Manipulator_2_toAverage() {
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

    public void mntm_Manipulator_3_toSubfile() {
        if (Project.getFileToEdit() != null) {
            Set<GData1> subfiles = Project.getFileToEdit().getVertexManager().getSelectedSubfiles();
            if (!subfiles.isEmpty()) {
                final GData1 subfile = subfiles.iterator().next();
                Manipulator origin = null;
                for (OpenGLRenderer renderer : renders) {
                    final Composite3D c3d = renderer.getC3D();
                    if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                        Matrix4f m = subfile.getProductMatrix();
                        Matrix M = subfile.getAccurateProductMatrix();
                        MatrixOperations.moveManipulatorToSubfileOrCSGMatrix(c3d, M, m);
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

    public void mntm_Manipulator_32_subfileTo(boolean resetScale) {
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

    public void mntm_Manipulator_4_toVertex() {
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
        regainFocus();
    }

    public void mntm_Manipulator_5_toEdge() {
        for (OpenGLRenderer renderer : renders) {
            Composite3D c3d = renderer.getC3D();
            if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                Vector4f min = new Vector4f(c3d.getManipulator().getPosition());
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

    public void mntm_Manipulator_6_toSurface() {
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
        regainFocus();
    }

    public void mntm_Manipulator_7_toVertexNormal() {
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

    public void mntm_Manipulator_8_toEdgeNormal() {
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

    public void mntm_Manipulator_9_toSurfaceNormal() {
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

    public void mntm_Manipulator_XIV_adjustRotationCenter() {
        for (OpenGLRenderer renderer : renders) {
            Composite3D c3d = renderer.getC3D();
            if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                vm.adjustRotationCenter(c3d, null);
            }
        }
        regainFocus();
    }

    public void mntm_Manipulator_XV_toVertexPosition() {
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
                Vector4f n = new Vector4f(min.x, min.y, min.z, 0f);
                if (min.x == 0f && min.y == 0f && min.z == 0f) {
                    n = new Vector4f(0f, 0f, 1f, 0f);
                }
                n.normalise();
                n.setW(1f);

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

    public void mntm_Manipulator_SwitchYZ() {
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

    public void mntm_Manipulator_SwitchXZ() {
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

    public void mntm_Manipulator_SwitchXY() {
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
            messageBox.setText(I18n.DIALOG_UnsavedChangesTitle);

            Object[] messageArguments = {df.getShortName()};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.LOCALE);
            formatter.applyPattern(I18n.DIALOG_UnsavedChanges);
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
                    messageBoxError.setText(I18n.DIALOG_Error);
                    messageBoxError.setMessage(I18n.DIALOG_CantSaveFile);
                    messageBoxError.open();
                    cleanupClosedData();
                    updateTree_unsavedEntries();
                    regainFocus();
                    return false;
                }
            } else {
                cleanupClosedData();
                updateTree_unsavedEntries();
                regainFocus();
                return false;
            }
        } else {
            result2 = true;
        }
        updateTree_removeEntry(df);
        cleanupClosedData();
        regainFocus();
        return result2;
    }

    private void openFileIn3DEditor(final DatFile df) {
        if (renders.isEmpty()) {

            if ("%EMPTY%".equals(Editor3DWindow.getSashForm().getChildren()[1].getData())) { //$NON-NLS-1$
                int[] mainSashWeights = Editor3DWindow.getSashForm().getWeights();
                Editor3DWindow.getSashForm().getChildren()[1].dispose();
                CompositeContainer cmp_Container = new CompositeContainer(Editor3DWindow.getSashForm(), false);
                cmp_Container.moveBelow(Editor3DWindow.getSashForm().getChildren()[0]);
                addRecentFile(df);
                df.parseForData(true);
                Project.setFileToEdit(df);
                cmp_Container.getComposite3D().setLockableDatFileReference(df);
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
        for (CTabItem ti : tabFolder_OpenDatFiles[0].getItems()) {
            if (df.equals(ti.getData())) {
                tabFolder_OpenDatFiles[0].setSelection(ti);
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
        for (CTabItem ti : tabFolder_OpenDatFiles[0].getItems()) {
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

    public boolean revert(DatFile df) {
        if (df.isReadOnly() || !Project.getUnsavedFiles().contains(df) || df.isVirtual() && df.getText().trim().isEmpty()) {
            regainFocus();
            return false;
        }
        df.getVertexManager().addSnapshot();

        MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
        messageBox.setText(I18n.DIALOG_RevertTitle);

        Object[] messageArguments = {df.getShortName(), df.getLastSavedOpened()};
        MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
        formatter.setLocale(MyLanguage.LOCALE);
        formatter.applyPattern(I18n.DIALOG_Revert);
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
            updateTree_unsavedEntries();
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
            HashMap<Composite3D, org.nschmidt.ldparteditor.composites.Composite3DViewState> states = new HashMap<>();
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
            HashSet<DatFile> allFiles = new HashSet<DatFile>();
            for (CTabItem ci : tabFolder_OpenDatFiles[0].getItems()) {
                allFiles.add((DatFile) ci.getData());
            }

            HashSet<DatFile> cachedStates = new HashSet<DatFile>();
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
            HashMap<Composite3D, org.nschmidt.ldparteditor.composites.Composite3DViewState> states = c3dStates.get(df);
            if (states.containsKey(c3d)) {
                c3d.importState(states.get(c3d));
            }
        }
    }

    public boolean hasState(final DatFile df, final Composite3D c3d) {
        return c3dStates.containsKey(df) && c3dStates.get(df).containsKey(c3d);
    }

    private void action_OpenInTextEditor() {
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

    private void action_OpenIn3DEditor() {
        if (treeParts[0].getSelectionCount() == 1 && treeParts[0].getSelection()[0] != null && treeParts[0].getSelection()[0].getData() instanceof DatFile) {
            DatFile df = (DatFile) treeParts[0].getSelection()[0].getData();
            openFileIn3DEditor(df);
            updateTree_unsavedEntries();
            cleanupClosedData();
            regainFocus();
        }
    }

    private void action_Revert() {
        if (treeParts[0].getSelectionCount() == 1 && treeParts[0].getSelection()[0] != null && treeParts[0].getSelection()[0].getData() instanceof DatFile) {
            DatFile df = (DatFile) treeParts[0].getSelection()[0].getData();
            revert(df);
        }
        regainFocus();
    }

    private void action_Close() {
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
    private void action_Rename() {
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
                    messageBox.setText(I18n.DIALOG_NoProjectLocationTitle);

                    Object[] messageArguments = {new File(newPath).getName()};
                    MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                    formatter.setLocale(MyLanguage.LOCALE);
                    formatter.applyPattern(I18n.DIALOG_NoProjectLocation);
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

                HashSet<EditorTextWindow> windows = new HashSet<EditorTextWindow>(Project.getOpenTextWindows());
                for (EditorTextWindow win : windows) {
                    win.updateTabWithDatfile(df);
                }
                updateTree_renamedEntries();
                updateTree_unsavedEntries();
            }
        } else if (treeParts[0].getSelectionCount() == 1 && treeParts[0].getSelection()[0] != null && treeParts[0].getSelection()[0].equals(treeItem_Project[0])) {
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
                            messageBoxError.setText(I18n.PROJECT_ProjectOverwriteTitle);
                            messageBoxError.setMessage(I18n.PROJECT_ProjectOverwrite);
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
                        HashSet<DatFile> projectFiles = new HashSet<DatFile>();
                        projectFiles.addAll((ArrayList<DatFile>) Editor3DWindow.getWindow().getProjectParts().getData());
                        projectFiles.addAll((ArrayList<DatFile>) Editor3DWindow.getWindow().getProjectSubparts().getData());
                        projectFiles.addAll((ArrayList<DatFile>) Editor3DWindow.getWindow().getProjectPrimitives().getData());
                        projectFiles.addAll((ArrayList<DatFile>) Editor3DWindow.getWindow().getProjectPrimitives48().getData());
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
                        updateTree_unsavedEntries();
                        Project.updateEditor();
                        Editor3DWindow.getWindow().getShell().update();
                        Project.setLastVisitedPath(Project.getProjectPath());
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
            }
        }
        regainFocus();
    }

    @SuppressWarnings("unchecked")
    private void action_CopyToUnofficial() {
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
                regainFocus();
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
                formatter.setLocale(MyLanguage.LOCALE);
                formatter.applyPattern(I18n.DIALOG_Replace);
                messageBox.setMessage(formatter.format(messageArguments));

                int result = messageBox.open();

                if (result == SWT.CANCEL) {
                    regainFocus();
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
                        regainFocus();
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
                        regainFocus();
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
        }
        regainFocus();
    }

    void reloadColours() {
        for (Control ctrl : toolItem_ColourBar.getChildren()) {
            if (!(ctrl instanceof ToolSeparator)) ctrl.dispose();
        }

        List<GColour> colours = WorkbenchManager.getUserSettingState().getUserPalette();

        final int size = colours.size();
        for (int i = 0; i < size; i++) {
            addColorButton(toolItem_ColourBar, colours.get(i), i);
        }

        {
            NButton btn_Palette = new NButton(toolItem_ColourBar, SWT.NONE);
            this.btn_Palette[0] = btn_Palette;
            btn_Palette.setToolTipText(I18n.E3D_More);
            btn_Palette.setImage(ResourceManager.getImage("icon16_colours.png")); //$NON-NLS-1$
            initPaletteEvent();
        }

        toolItem_ColourBar.getParent().layout();
        toolItem_ColourBar.layout();
        toolItem_ColourBar.redraw();
    }

    public static void reloadAllColours() {
        for (EditorTextWindow w : Project.getOpenTextWindows()) {
            w.reloadColours();
        }
        Editor3DWindow.getWindow().reloadColours();
    }

    void initPaletteEvent() {
        btn_Palette[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (Project.getFileToEdit() != null) {
                    final GColour[] gColour2 = new GColour[1];
                    new ColourDialog(getShell(), gColour2, true).run();
                    if (gColour2[0] != null) {
                        setLastUsedColour(gColour2[0]);
                        int num = gColour2[0].getColourNumber();
                        if (!View.hasLDConfigColour(num)) {
                            num = -1;
                        }
                        Project.getFileToEdit().getVertexManager().addSnapshot();
                        Project.getFileToEdit().getVertexManager().colourChangeSelection(num, gColour2[0].getR(), gColour2[0].getG(), gColour2[0].getB(), gColour2[0].getA(), true);

                        btn_LastUsedColour[0].clearPaintListeners();
                        btn_LastUsedColour[0].clearSelectionListeners();
                        final Color col = SWTResourceManager.getColor((int) (gColour2[0].getR() * 255f), (int) (gColour2[0].getG() * 255f), (int) (gColour2[0].getB() * 255f));
                        final Point size = btn_LastUsedColour[0].computeSize(SWT.DEFAULT, SWT.DEFAULT);
                        final int x = Math.round(size.x / 5f);
                        final int y = Math.round(size.y / 5f);
                        final int w = Math.round(size.x * (3f / 5f));
                        final int h = Math.round(size.y * (3f / 5f));
                        final int imgSize = IconSize.getImageSizeFromIconSize();
                        btn_LastUsedColour[0].addPaintListener(new PaintListener() {
                            @Override
                            public void paintControl(PaintEvent e) {
                                e.gc.setBackground(col);
                                e.gc.fillRectangle(x, y, w, h);
                                if (gColour2[0].getA() >= .99f) {
                                    e.gc.drawImage(ResourceManager.getImage("icon16_transparent.png"), 0, 0, imgSize, imgSize, x, y, w, h); //$NON-NLS-1$
                                } else if (gColour2[0].getA() == 0f) {
                                    e.gc.drawImage(ResourceManager.getImage("icon16_randomColours.png"), 0, 0, imgSize, imgSize, x, y, w, h); //$NON-NLS-1$
                                } else {
                                    e.gc.drawImage(ResourceManager.getImage("icon16_halftrans.png"), 0, 0, imgSize, imgSize, x, y, w, h); //$NON-NLS-1$
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
                                    Project.getFileToEdit().getVertexManager().addSnapshot();
                                    Project.getFileToEdit().getVertexManager().colourChangeSelection(num, gColour2[0].getR(), gColour2[0].getG(), gColour2[0].getB(), gColour2[0].getA(), true);
                                }
                                regainFocus();
                            }

                            @Override
                            public void widgetDefaultSelected(SelectionEvent e) {
                            }
                        });
                        if (num != -1) {

                            Object[] messageArguments = {num, View.getLDConfigColourName(num)};
                            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                            formatter.setLocale(MyLanguage.LOCALE);
                            formatter.applyPattern(I18n.EDITORTEXT_Colour1);

                            btn_LastUsedColour[0].setToolTipText(formatter.format(messageArguments));
                        } else {
                            StringBuilder colourBuilder = new StringBuilder();
                            colourBuilder.append("0x2"); //$NON-NLS-1$
                            colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getR())).toUpperCase());
                            colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getG())).toUpperCase());
                            colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getB())).toUpperCase());

                            Object[] messageArguments = {colourBuilder.toString()};
                            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                            formatter.setLocale(MyLanguage.LOCALE);
                            formatter.applyPattern(I18n.EDITORTEXT_Colour2);

                            btn_LastUsedColour[0].setToolTipText(formatter.format(messageArguments));
                            if (gColour2[0].getA() == 0f) btn_LastUsedColour[0].setToolTipText(I18n.COLOURDIALOG_RandomColours);
                        }
                        btn_LastUsedColour[0].redraw();
                    }
                }
                regainFocus();
            }
        });
    }

    public void updateInitialScale(BigDecimal initialScale, BigDecimal scaleFacor, boolean setDefaults) {
        if (setDefaults) {
            switch (snapSize) {
            case 0:
                WorkbenchManager.getUserSettingState().setCoarse_scale_snap(new BigDecimal("2")); //$NON-NLS-1$
                scaleFacor = WorkbenchManager.getUserSettingState().getCoarse_scale_snap();
                break;
            case 1:
                WorkbenchManager.getUserSettingState().setCoarse_scale_snap(new BigDecimal("1.1")); //$NON-NLS-1$
                scaleFacor = WorkbenchManager.getUserSettingState().getCoarse_scale_snap();
                break;
            case 2:
                WorkbenchManager.getUserSettingState().setCoarse_scale_snap(new BigDecimal("1.001")); //$NON-NLS-1$
                scaleFacor = WorkbenchManager.getUserSettingState().getCoarse_scale_snap();
                break;
            default:
                break;
            }
        }
        spn_ScaleInitial[0].setValue(initialScale);
        spn_Scale[0].setValue(scaleFacor);
    }

    public void activateAllTypes() {
        mntm_SVertices[0].setSelection(true);
        mntm_SLines[0].setSelection(true);
        mntm_STriangles[0].setSelection(true);
        mntm_SQuads[0].setSelection(true);
        mntm_SCLines[0].setSelection(true);
    }

    public ArrayList<Composite3DState> getC3DStates() {
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
        return c3dStates;
    }

    public VertexWindow getVertexWindow() {
        return vertexWindow;
    }
}
