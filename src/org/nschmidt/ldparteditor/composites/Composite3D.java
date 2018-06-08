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
package org.nschmidt.ldparteditor.composites;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.wb.swt.SWTResourceManager;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.composites.compositetab.CompositeTab;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.GData2;
import org.nschmidt.ldparteditor.data.GData3;
import org.nschmidt.ldparteditor.data.GData4;
import org.nschmidt.ldparteditor.data.GData5;
import org.nschmidt.ldparteditor.data.MemorySnapshot;
import org.nschmidt.ldparteditor.data.ParsingResult;
import org.nschmidt.ldparteditor.data.Primitive;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.dialogs.snapshot.SnapshotDialog;
import org.nschmidt.ldparteditor.dialogs.value.ValueDialog;
import org.nschmidt.ldparteditor.dnd.MyDummyTransfer2;
import org.nschmidt.ldparteditor.enums.OpenInWhat;
import org.nschmidt.ldparteditor.enums.Perspective;
import org.nschmidt.ldparteditor.enums.Task;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.Cocoa;
import org.nschmidt.ldparteditor.helpers.Manipulator;
import org.nschmidt.ldparteditor.helpers.WidgetSelectionHelper;
import org.nschmidt.ldparteditor.helpers.composite3d.Composite3DModifier;
import org.nschmidt.ldparteditor.helpers.composite3d.GuiStatusManager;
import org.nschmidt.ldparteditor.helpers.composite3d.MouseActions;
import org.nschmidt.ldparteditor.helpers.composite3d.PerspectiveCalculator;
import org.nschmidt.ldparteditor.helpers.composite3d.ViewIdleManager;
import org.nschmidt.ldparteditor.helpers.math.HashBiMap;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeTreeMap;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer20;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer33;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.resources.ResourceManager;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.shells.editortext.EditorTextWindow;
import org.nschmidt.ldparteditor.state.KeyStateManager;
import org.nschmidt.ldparteditor.text.DatParser;
import org.nschmidt.ldparteditor.vertexwindow.VertexWindow;
import org.nschmidt.ldparteditor.widgets.listeners.Win32MouseWheelFilter;
import org.nschmidt.ldparteditor.workbench.Composite3DState;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * This is the composite for a 3D OpenGL perspective. This object can be divided
 * into two.
 *
 * @author nils
 *
 */
public class Composite3D extends ScalableComposite {

    // Context Menus
    /** The menu of this composite */
    private final Menu menu;
    /** The "View Angles"-Menu */
    private final Menu mnu_viewAngles;
    /** The "Render Mode"-Menu */
    public final Menu mnu_renderMode;
    /** The "Line Mode"-Menu */
    private final Menu mnu_lineMode;
    /** The "Synchronise..."-Menu */
    public final Menu mnu_syncronise;
    /** The "Manipulator"-Menu */
    private final Menu mnu_Manipulator;

    /** The cursor position in the 3D space [LDU] */
    private final Vector4f cursor3D = new Vector4f(0f, 0f, 0f, 1f);
    private final Vector4f cursorSnapped3D = new Vector4f(0f, 0f, 0f, 1f);
    private final BigDecimal[] cursorSnapped3Dprecise = new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO };
    private final Vector4f cursorPosition = new Vector4f(0f, 0f, 0f, 1f);

    private final Vector2f mousePosition = new Vector2f(0f, 0f);
    private final Vector2f oldMousePosition = new Vector2f(0f, 0f);

    /** The cursor position of the selection rectangle to start */
    private final Vector4f selectionStart = new Vector4f(0f, 0f, 0f, 1f);

    private final Vector4f selectionWidth = new Vector4f(0.0001f, 0.0001f, 0.0001f, 1f);
    private final Vector4f selectionHeight = new Vector4f(0.0001f, 0.0001f, 0.0001f, 1f);

    private boolean doingSelection;
    private boolean warpedSelection = false;
    private boolean negDeterminant;

    private Primitive draggedPrimitive = null;

    /** Resolution of the viewport at n% zoom */
    private float viewport_pixel_per_ldu;
    /** The translation matrix of the view */
    private final Matrix4f viewport_translation = new Matrix4f();
    /** The view zoom level */
    private volatile float zoom;

    private boolean hasMouse = true;

    private final Vector4f screenXY = new Vector4f(0, 0, 0, 1);

    private final Set<Vertex> tmpHiddenVertices = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());

    public Vector4f getScreenXY() {
        return screenXY;
    }

    /**
     * {@code true} if the view represents standard XY, XZ or YZ order and a
     * scale can be displayed.
     */
    private boolean classicPerspective;
    /** {@code true} if the origin is shown. */
    private boolean originShown;
    /** {@code true} if the anaglyph 3D mode is activated. */
    private boolean anaglyph3d;
    /** {@code true} if the grid is shown. */
    private boolean gridShown;
    /** The perspective value */
    private Perspective viewport_perspective;
    /** The rotation matrix of the view */
    private Matrix4f viewport_rotation = new Matrix4f();
    /** The transformation matrix of the view */
    private final Matrix4f viewport_matrix = new Matrix4f();
    /** The inverse transformation matrix of the view */
    private Matrix4f viewport_matrix_inv = new Matrix4f();

    /** The generator of the viewport space */
    private final Vector4f[] viewport_generator = new Vector4f[3];
    /** The origin axis coordinates of the viewport */
    private final Vector3f[] viewport_origin_axis = new Vector3f[] { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
    /** The viewport z-Near value */
    private double zNear = 1000000f;
    /** The viewport z-Far value */
    private double zFar = 1000001f;
    /**
     * The grid information (upper left corner, grid x-direction, grid
     * y-direction, cell count)
     */
    private final Vector4f[] grid_data = new Vector4f[] { new Vector4f(), new Vector4f(), new Vector4f(), new Vector4f(), new Vector4f(), new Vector4f(), new Vector4f(), new Vector4f() };

    /** the {@linkplain GLCanvas} */
    final GLCanvas canvas;
    final GLCapabilities capabilities;

    // Several helper classes
    /** The {@linkplain PerspectiveCalculator} instance */
    private final PerspectiveCalculator perspective = new PerspectiveCalculator(this);
    /** The {@linkplain Composite3DModifier} instance */
    private final Composite3DModifier c3d_modifier = new Composite3DModifier(this);
    /** The {@linkplain OpenGLRenderer} instance */
    private final OpenGLRenderer openGL = WorkbenchManager.getUserSettingState().getOpenGLVersion() == 20 ?  new OpenGLRenderer20(this) : new OpenGLRenderer33(this);
    /** The {@linkplain MouseActions} instance */
    private final MouseActions mouse = new MouseActions(this);
    /** Information about pressed keys */
    private final KeyStateManager keyboard = new KeyStateManager(this);

    private final MenuItem locked;

    private final Manipulator manipulator = new Manipulator();

    private boolean lightOn;
    private boolean meshLines;
    private boolean subMeshLines;
    private boolean showingVertices;
    private boolean showingHiddenVertices;
    private boolean showingCondlineControlPoints;
    private boolean showingLogo;
    private boolean blackEdges;

    private boolean syncManipulator;
    private boolean syncTranslation;
    private boolean syncZoom;

    private boolean showingAxis;
    private boolean showingLabels;

    private boolean smoothShading;

    private volatile int lineMode;

    private volatile int renderMode = 0;

    private volatile DatFile lockableDatFileReference;
    private boolean datFileLockedOnDisplay;

    private boolean drawingSolidMaterials;

    /** The grid size */
    private float grid_scale = 1f;

    public final MenuItem[] mntmNoBFC = new MenuItem[1];
    public final MenuItem[] mntmRandomColours = new MenuItem[1];
    final MenuItem[] mntmBFCFrontBack = new MenuItem[1];
    final MenuItem[] mntmBFCBack = new MenuItem[1];
    final MenuItem[] mntmBFCReal = new MenuItem[1];
    final MenuItem[] mntmBFCTextured = new MenuItem[1];
    public final MenuItem[] mntmCondlineMode = new MenuItem[1];
    public final MenuItem[] mntmCoplanarityHeatmapMode = new MenuItem[1];
    public final MenuItem[] mntmWireframeMode = new MenuItem[1];
    final MenuItem[] mntmAnaglyph = new MenuItem[1];
    final MenuItem[] mntmAxis = new MenuItem[1];
    final MenuItem[] mntmAlwaysBlack = new MenuItem[1];
    final MenuItem[] mntmHideAll = new MenuItem[1];
    final MenuItem[] mntmStdLines = new MenuItem[1];
    final MenuItem[] mntmShowAll = new MenuItem[1];
    final MenuItem[] mntmStudLogo = new MenuItem[1];
    final MenuItem[] mntmSmoothShading = new MenuItem[1];
    final MenuItem[] mntmControlPointVertices = new MenuItem[1];
    final MenuItem[] mntmHiddenVertices = new MenuItem[1];
    final MenuItem[] mntmVertices = new MenuItem[1];
    final MenuItem[] mntmSubMeshLines = new MenuItem[1];
    final MenuItem[] mntmMeshLines = new MenuItem[1];
    final MenuItem[] mntmSwitchLights = new MenuItem[1];
    final MenuItem[] mntmShowOrigin = new MenuItem[1];
    final MenuItem[] mntmTwoThirds = new MenuItem[1];
    final MenuItem[] mntmBottom = new MenuItem[1];
    final MenuItem[] mntmTop = new MenuItem[1];
    final MenuItem[] mntmRight = new MenuItem[1];
    final MenuItem[] mntmLeft = new MenuItem[1];
    final MenuItem[] mntmBack = new MenuItem[1];
    final MenuItem[] mntmFront = new MenuItem[1];
    final MenuItem[] mntmShowGrid = new MenuItem[1];
    final MenuItem[] mntmShowScale = new MenuItem[1];
    final MenuItem[] mntmLabel = new MenuItem[1];
    final MenuItem[] mntmRealPreview = new MenuItem[1];

    final MenuItem[] mntmSyncTranslate = new MenuItem[1];
    final MenuItem[] mntmSyncManipulator = new MenuItem[1];
    final MenuItem[] mntmSyncZoom = new MenuItem[1];

    public Composite3D(Composite parentCompositeContainer, boolean syncManipulator, boolean syncTranslation, boolean syncZoom) {
        this(parentCompositeContainer);
        setSyncManipulator(syncManipulator);
        setSyncTranslation(syncTranslation);
        setSyncZoom(syncZoom);
        mntmSyncManipulator[0].setSelection(syncManipulator);
        mntmSyncTranslate[0].setSelection(syncTranslation);
        mntmSyncZoom[0].setSelection(syncZoom);
    }

    public Composite3D(Composite parentCompositeContainer, DatFile df) {
        this(parentCompositeContainer);
        setSyncManipulator(false);
        setSyncTranslation(false);
        setSyncZoom(false);

        Editor3DWindow.renders.remove(openGL);
        Editor3DWindow.canvasList.remove(canvas);
        this.menu.dispose();
        setLockableDatFileReference(df);
    }

    /**
     * Creates a new 3D Composite in a {@link CompositeContainer}
     *
     * @param parentCompositeContainer
     */
    public Composite3D(Composite parentCompositeContainer) {
        super(parentCompositeContainer, I18n.I18N_NON_BIDIRECT() | SWT.H_SCROLL | SWT.V_SCROLL);
        final ScrollBar sb1 = super.getVerticalBar();
        sb1.setMinimum(-1);
        sb1.setMaximum(1);
        sb1.setSelection(0);
        sb1.setVisible(false);
        final ScrollBar sb2 = super.getHorizontalBar();
        sb2.setVisible(false);
        sb2.setMinimum(-1);
        sb2.setMaximum(1);
        sb2.setSelection(0);

        this.setBackgroundMode(SWT.INHERIT_FORCE);
        this.setClassicPerspective(true);
        this.setOriginShown(true);
        this.setGridShown(true);
        setShowingAxis(true);
        setShowingLabels(true);
        this.setLightOn(true);
        this.setMeshLines(true);
        this.setSubMeshLines(false);
        this.setShowingVertices(true);
        this.setShowingHiddenVertices(false);
        this.setShowingCondlineControlPoints(false);
        this.setAnaglyph3d(false);
        this.setBlackEdges(false);
        this.setRenderMode(0);
        this.setLockableDatFileReference(Project.getFileToEdit());
        this.setDatFileLockedOnDisplay(false);
        this.viewport_perspective = Perspective.FRONT;
        this.zoom = 0.00001f;
        Matrix4f.setIdentity(this.viewport_rotation);
        Matrix4f.setIdentity(this.viewport_translation);
        viewport_generator[0] = new Vector4f(1.0f, 0, 0, 1.0f);
        viewport_generator[1] = new Vector4f(0, 1.0f, 0, 1.0f);
        viewport_generator[2] = new Vector4f(0, 0, 1.0f, 1.0f);
        this.viewport_pixel_per_ldu = this.zoom * View.PIXEL_PER_LDU;
        this.addControlListener(new ControlListener() {
            @Override
            public void controlResized(ControlEvent e) {
                c3d_modifier.moveSashOnResize();
            }

            @Override
            public void controlMoved(ControlEvent consumed) {
            }
        });

        this.setLayout(new FillLayout());
        GLData data = new GLData();
        data.doubleBuffer = true;
        data.depthSize = 24;
        data.alphaSize = 8;
        data.blueSize = 8;
        data.redSize = 8;
        data.greenSize = 8;
        data.stencilSize = 8;
        if (WorkbenchManager.getUserSettingState().isAntiAliasing()) {
            data.sampleBuffers = 1;
            data.samples = 4;
        }
        canvas = new GLCanvas(this, I18n.I18N_NON_BIDIRECT(), data);
        canvas.setCurrent();
        if (WorkbenchManager.getUserSettingState().getOpenGLVersion() == 20) {
            capabilities = GL.createCapabilities();
        } else {
            capabilities = GL.createCapabilities(true);
        }
        canvas.setCursor(new Cursor(Display.getCurrent(), SWT.CURSOR_CROSS));
        canvas.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

        Editor3DWindow.renders.add(openGL);
        Editor3DWindow.canvasList.add(canvas);

        this.setBackgroundMode(SWT.INHERIT_FORCE);

        this.menu = new Menu(this);

        MenuItem mntmViewAngles = new MenuItem(menu, SWT.CASCADE);
        mntmViewAngles.setText(I18n.E3D_ViewingAngles);

        MenuItem mntmManipulator = new MenuItem(menu, SWT.CASCADE);
        mntmManipulator.setText(I18n.E3D_ModifyManipulator);

        MenuItem mntmViewActions = new MenuItem(menu, SWT.CASCADE);
        mntmViewActions.setText(I18n.E3D_ViewActions);

        MenuItem mntmRenderModes = new MenuItem(menu, SWT.CASCADE);
        mntmRenderModes.setText(I18n.C3D_RenderMode);

        MenuItem mntmSynchronise = new MenuItem(menu, SWT.CASCADE);
        mntmSynchronise.setText(I18n.E3D_Sync);

        @SuppressWarnings("unused")
        final MenuItem mntmSeparator3 = new MenuItem(menu, SWT.SEPARATOR);

        final MenuItem mntmCloseDat = new MenuItem(menu, SWT.NONE);
        mntmCloseDat.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Project.removeOpenedFile(lockableDatFileReference);
                if (!Editor3DWindow.getWindow().closeDatfile(lockableDatFileReference)) {
                    Project.addOpenedFile(lockableDatFileReference);
                    Project.setFileToEdit(lockableDatFileReference);
                    Editor3DWindow.getWindow().updateTree_unsavedEntries();
                }
            }
        });
        mntmCloseDat.setText(I18n.E3D_Close);
        mntmCloseDat.setSelection(false);

        final MenuItem mntmLockedDat = new MenuItem(menu, SWT.CHECK);
        locked = mntmLockedDat;
        mntmLockedDat.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!lockableDatFileReference.equals(View.DUMMY_DATFILE)) {
                    c3d_modifier.switchLockedDat(mntmLockedDat.getSelection());
                }
            }
        });
        mntmLockedDat.setText(I18n.C3D_LockFile);
        mntmLockedDat.setSelection(false);

        final MenuItem mntmOpenInTextEditor = new MenuItem(menu, SWT.NONE);
        mntmOpenInTextEditor.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                openInTextEditor();
            }
        });
        mntmOpenInTextEditor.setText(I18n.C3D_OpenInText);
        mntmOpenInTextEditor.setSelection(false);

        final MenuItem mntmSelectionInTextEditor = new MenuItem(menu, SWT.NONE);
        mntmSelectionInTextEditor.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                showSelectionInTextEditor(lockableDatFileReference, true);
            }
        });
        mntmSelectionInTextEditor.setText(I18n.C3D_ShowInText);
        mntmSelectionInTextEditor.setSelection(false);

        final MenuItem mntmJoinInTextEditor = new MenuItem(menu, SWT.NONE);
        mntmJoinInTextEditor.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                joinSelectionInTextEditor(lockableDatFileReference);
            }
        });
        mntmJoinInTextEditor.setText(I18n.C3D_JoinSelection);
        mntmJoinInTextEditor.setSelection(false);

        if (NLogger.DEBUG) {
            @SuppressWarnings("unused")
            final MenuItem mntmSeparator = new MenuItem(menu, SWT.SEPARATOR);

            final MenuItem mntmOpenSnapshot = new MenuItem(menu, SWT.NONE);
            mntmOpenSnapshot.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    DatFile df = lockableDatFileReference;
                    if (df.equals(View.DUMMY_DATFILE)) return;
                    MemorySnapshot[] retVal = new MemorySnapshot[1];
                    if (new SnapshotDialog(getShell(), df.getVertexManager().getSnapshots(), retVal).open() == IDialogConstants.OK_ID) {
                        if (retVal[0] != null) {
                            for (EditorTextWindow w : Project.getOpenTextWindows()) {
                                w.closeTabWithDatfile(df);
                            }
                            df.getVertexManager().loadSnapshot(retVal[0]);
                        }
                    }
                }
            });
            mntmOpenSnapshot.setText("Open Snapshot (DEBUG ONLY)"); //$NON-NLS-1$
            mntmOpenSnapshot.setSelection(false);
        }

        /* TODO I need another solution for clipping planes
        @SuppressWarnings("unused")
        final MenuItem mntmSeparator = new MenuItem(menu, SWT.SEPARATOR);

        MenuItem mntmMaxClip = new MenuItem(menu, I18n.I18N_RTL());
        mntmMaxClip.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                new ValueDialog(getShell(), I18n.EDITOR3D_NearClippingHint, I18n.UNIT_CurrentUnit()) {
                    @Override
                    public void initializeSpinner() {
                        this.spn_Value[0].setMinimum(new BigDecimal("0.1")); //$NON-NLS-1$
                        this.spn_Value[0].setMaximum(new BigDecimal(zFar - 0.0001));
                        this.spn_Value[0].setValue(new BigDecimal(zNear / 1000.0));
                    }

                    @Override
                    public void applyValue() {
                        zNear = this.spn_Value[0].getValue().doubleValue() * 1000.0;
                        perspective.initializeViewportPerspective();
                    }
                }.open();
            }
        });
        mntmMaxClip.setText(I18n.EDITOR3D_NearClipping);

        MenuItem mntmMinClip = new MenuItem(menu, I18n.I18N_RTL());
        mntmMinClip.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                new ValueDialog(getShell(), I18n.EDITOR3D_FarClippingHint, I18n.UNIT_CurrentUnit()) {
                    @Override
                    public void initializeSpinner() {
                        this.spn_Value[0].setMinimum(new BigDecimal(zNear + 0.0001));
                        this.spn_Value[0].setMaximum(new BigDecimal("1000")); //$NON-NLS-1$
                        this.spn_Value[0].setValue(new BigDecimal(zFar / 1000.0));
                    }

                    @Override
                    public void applyValue() {
                        zFar = this.spn_Value[0].getValue().doubleValue() * 1000.0;
                        perspective.initializeViewportPerspective();
                    }
                }.open();
            }
        });
        mntmMinClip.setText(I18n.EDITOR3D_FarClipping);
         */

        @SuppressWarnings("unused")
        final MenuItem mntmSeparatorCP = new MenuItem(menu, SWT.SEPARATOR);

        MenuItem mntmCut = new MenuItem(menu, I18n.I18N_RTL());
        mntmCut.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (lockableDatFileReference.equals(View.DUMMY_DATFILE)) return;
                lockableDatFileReference.getVertexManager().addSnapshot();
                lockableDatFileReference.getVertexManager().copy();
                lockableDatFileReference.getVertexManager().delete(false, true);
            }
        });
        mntmCut.setText(I18n.COPYNPASTE_Cut);
        mntmCut.setImage(ResourceManager.getImage("icon16_edit-cut.png")); //$NON-NLS-1$
        MenuItem mntmCopy = new MenuItem(menu, I18n.I18N_RTL());
        mntmCopy.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (lockableDatFileReference.equals(View.DUMMY_DATFILE)) return;
                lockableDatFileReference.getVertexManager().addSnapshot();
                lockableDatFileReference.getVertexManager().copy();
            }
        });
        mntmCopy.setText(I18n.COPYNPASTE_Copy);
        mntmCopy.setImage(ResourceManager.getImage("icon16_edit-copy.png")); //$NON-NLS-1$
        MenuItem mntmPaste = new MenuItem(menu, I18n.I18N_RTL());
        mntmPaste.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (lockableDatFileReference.equals(View.DUMMY_DATFILE)) return;
                lockableDatFileReference.getVertexManager().addSnapshot();
                lockableDatFileReference.getVertexManager().paste(Editor3DWindow.getWindow().loadSelectorSettings());
                if (WorkbenchManager.getUserSettingState().isDisableMAD3D()) {
                    Editor3DWindow.getWindow().setMovingAdjacentData(false);
                    GuiStatusManager.updateStatus();
                }
            }
        });
        mntmPaste.setText(I18n.COPYNPASTE_Paste);
        mntmPaste.setImage(ResourceManager.getImage("icon16_edit-paste.png")); //$NON-NLS-1$
        MenuItem mntmDelete = new MenuItem(menu, I18n.I18N_RTL());
        mntmDelete.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (lockableDatFileReference.equals(View.DUMMY_DATFILE)) return;
                lockableDatFileReference.getVertexManager().addSnapshot();
                lockableDatFileReference.getVertexManager().delete(Editor3DWindow.getWindow().isMovingAdjacentData(), true);
            }
        });
        mntmDelete.setText(I18n.COPYNPASTE_Delete);
        mntmDelete.setImage(ResourceManager.getImage("icon16_delete.png")); //$NON-NLS-1$

        @SuppressWarnings("unused")
        final MenuItem mntmSeparator2 = new MenuItem(menu, SWT.SEPARATOR);

        MenuItem mntmGridSize = new MenuItem(menu, I18n.I18N_RTL());
        mntmGridSize.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                new ValueDialog(getShell(), I18n.C3D_SetGridSize, I18n.UNIT_CurrentUnit()) {
                    @Override
                    public void initializeSpinner() {
                        this.spn_Value[0].setMinimum(new BigDecimal("0")); //$NON-NLS-1$
                        this.spn_Value[0].setMaximum(new BigDecimal("1000")); //$NON-NLS-1$
                        this.spn_Value[0].setValue(new BigDecimal(grid_scale * 50.0));
                    }

                    @Override
                    public void applyValue() {
                        grid_scale = (float) (this.spn_Value[0].getValue().doubleValue() / 50.0);
                        if (grid_scale < 0.1f)
                            grid_scale = 0.1f;
                        perspective.calculateOriginData();
                    }
                }.open();
            }
        });
        mntmGridSize.setText(I18n.C3D_GridSize);

        {
            // MARK CMenu Viewing Angles
            mnu_viewAngles = new Menu(mntmViewAngles);
            mntmViewAngles.setMenu(mnu_viewAngles);

            final MenuItem mntmFront = new MenuItem(mnu_viewAngles, SWT.CHECK);
            this.mntmFront[0] = mntmFront;
            mntmFront.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    WidgetSelectionHelper.unselectAllChildButtons(mnu_viewAngles);
                    ((MenuItem) e.widget).setSelection(true);
                    perspective.setPerspective(Perspective.FRONT);
                }
            });
            mntmFront.setImage(ResourceManager.getImage("icon16_front.png")); //$NON-NLS-1$
            mntmFront.setText(I18n.PERSPECTIVE_FRONT);
            mntmFront.setSelection(true);

            final MenuItem mntmBack = new MenuItem(mnu_viewAngles, SWT.CHECK);
            this.mntmBack[0] = mntmBack;
            mntmBack.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    WidgetSelectionHelper.unselectAllChildButtons(mnu_viewAngles);
                    ((MenuItem) e.widget).setSelection(true);
                    perspective.setPerspective(Perspective.BACK);
                }
            });
            mntmBack.setImage(ResourceManager.getImage("icon16_back.png")); //$NON-NLS-1$
            mntmBack.setText(I18n.PERSPECTIVE_BACK);

            final MenuItem mntmLeft = new MenuItem(mnu_viewAngles, SWT.CHECK);
            this.mntmLeft[0] = mntmLeft;
            mntmLeft.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    WidgetSelectionHelper.unselectAllChildButtons(mnu_viewAngles);
                    ((MenuItem) e.widget).setSelection(true);
                    perspective.setPerspective(Perspective.LEFT);
                }
            });
            mntmLeft.setImage(ResourceManager.getImage("icon16_left.png")); //$NON-NLS-1$
            mntmLeft.setText(I18n.PERSPECTIVE_LEFT);

            final MenuItem mntmRight = new MenuItem(mnu_viewAngles, SWT.CHECK);
            this.mntmRight[0] = mntmRight;
            mntmRight.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    WidgetSelectionHelper.unselectAllChildButtons(mnu_viewAngles);
                    ((MenuItem) e.widget).setSelection(true);
                    perspective.setPerspective(Perspective.RIGHT);
                }
            });
            mntmRight.setImage(ResourceManager.getImage("icon16_right.png")); //$NON-NLS-1$
            mntmRight.setText(I18n.PERSPECTIVE_RIGHT);

            final MenuItem mntmTop = new MenuItem(mnu_viewAngles, SWT.CHECK);
            this.mntmTop[0] = mntmTop;
            mntmTop.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    WidgetSelectionHelper.unselectAllChildButtons(mnu_viewAngles);
                    ((MenuItem) e.widget).setSelection(true);
                    perspective.setPerspective(Perspective.TOP);
                }
            });
            mntmTop.setImage(ResourceManager.getImage("icon16_top.png")); //$NON-NLS-1$
            mntmTop.setText(I18n.PERSPECTIVE_TOP);

            final MenuItem mntmBottom = new MenuItem(mnu_viewAngles, SWT.CHECK);
            this.mntmBottom[0] = mntmBottom;
            mntmBottom.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    WidgetSelectionHelper.unselectAllChildButtons(mnu_viewAngles);
                    ((MenuItem) e.widget).setSelection(true);
                    perspective.setPerspective(Perspective.BOTTOM);
                }
            });
            mntmBottom.setImage(ResourceManager.getImage("icon16_bottom.png")); //$NON-NLS-1$
            mntmBottom.setText(I18n.PERSPECTIVE_BOTTOM);

            @SuppressWarnings("unused")
            final MenuItem mntmViewSeparator = new MenuItem(mnu_viewAngles, SWT.SEPARATOR);

            final MenuItem mntmTwoThirds = new MenuItem(mnu_viewAngles, SWT.NONE);
            this.mntmTwoThirds[0] = mntmTwoThirds;
            mntmTwoThirds.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    WidgetSelectionHelper.unselectAllChildButtons(mnu_viewAngles);
                    perspective.setPerspective(Perspective.TWO_THIRDS);
                }
            });
            mntmTwoThirds.setImage(ResourceManager.getImage("icon16_twoThirds.png")); //$NON-NLS-1$
            mntmTwoThirds.setText(I18n.PERSPECTIVE_TwoThirds);
        }

        {
            // MARK CMenu Manipulator
            mnu_Manipulator = new Menu(mntmManipulator);
            mntmManipulator.setMenu(mnu_Manipulator);
            {
                MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                btn_Mani.setText(I18n.E3D_ManipulatorToOrigin);
                btn_Mani.setImage(ResourceManager.getImage("icon16_toOrigin.png")); //$NON-NLS-1$
                btn_Mani.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Editor3DWindow.getWindow().mntm_Manipulator_0_toOrigin();
                    }
                });
            }
            {
                MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                btn_Mani.setText(I18n.E3D_ManipulatorToWorld);
                btn_Mani.setImage(ResourceManager.getImage("icon16_toWorld.png")); //$NON-NLS-1$
                btn_Mani.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Editor3DWindow.getWindow().mntm_Manipulator_XIII_toWorld();
                    }
                });
            }
            {
                MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                btn_Mani.setText(I18n.E3D_AdjustRotationCenter);
                btn_Mani.setImage(ResourceManager.getImage("icon16_adjustrotationcenter.png")); //$NON-NLS-1$
                btn_Mani.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Editor3DWindow.getWindow().mntm_Manipulator_XIV_adjustRotationCenter();
                    }
                });
            }
            @SuppressWarnings("unused")
            final MenuItem mntmSeparator1 = new MenuItem(mnu_Manipulator, SWT.SEPARATOR);
            {
                MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                btn_Mani.setText(I18n.E3D_ReverseX);
                btn_Mani.setImage(ResourceManager.getImage("icon16_Xinv.png")); //$NON-NLS-1$
                btn_Mani.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Editor3DWindow.getWindow().mntm_Manipulator_X_XReverse();
                    }
                });
            }
            {
                MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                btn_Mani.setText(I18n.E3D_ReverseY);
                btn_Mani.setImage(ResourceManager.getImage("icon16_Yinv.png")); //$NON-NLS-1$
                btn_Mani.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Editor3DWindow.getWindow().mntm_Manipulator_XI_YReverse();
                    }
                });
            }
            {
                MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                btn_Mani.setText(I18n.E3D_ReverseZ);
                btn_Mani.setImage(ResourceManager.getImage("icon16_Zinv.png")); //$NON-NLS-1$
                btn_Mani.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Editor3DWindow.getWindow().mntm_Manipulator_XII_ZReverse();
                    }
                });
            }
            @SuppressWarnings("unused")
            final MenuItem mntmSeparator20 = new MenuItem(mnu_Manipulator, SWT.SEPARATOR);
            {
                MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                btn_Mani.setText(I18n.E3D_SwapXY);
                btn_Mani.setImage(ResourceManager.getImage("icon16_XswapY.png")); //$NON-NLS-1$
                btn_Mani.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Editor3DWindow.getWindow().mntm_Manipulator_SwitchXY();
                    }
                });
            }
            {
                MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                btn_Mani.setText(I18n.E3D_SwapXZ);
                btn_Mani.setImage(ResourceManager.getImage("icon16_XswapZ.png")); //$NON-NLS-1$
                btn_Mani.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Editor3DWindow.getWindow().mntm_Manipulator_SwitchXZ();
                    }
                });
            }
            {
                MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                btn_Mani.setText(I18n.E3D_SwapYZ);
                btn_Mani.setImage(ResourceManager.getImage("icon16_YswapZ.png")); //$NON-NLS-1$
                btn_Mani.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Editor3DWindow.getWindow().mntm_Manipulator_SwitchYZ();
                    }
                });
            }
            @SuppressWarnings("unused")
            final MenuItem mntmSeparator30 = new MenuItem(mnu_Manipulator, SWT.SEPARATOR);
            {
                MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                btn_Mani.setText(I18n.E3D_CamToManipulator);
                btn_Mani.setImage(ResourceManager.getImage("icon16_cameratomanipulator.png")); //$NON-NLS-1$
                btn_Mani.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Editor3DWindow.getWindow().mntm_Manipulator_1_cameraToPos();
                    }
                });
            }
            {
                MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                btn_Mani.setText(I18n.E3D_ManipulatorToAvg);
                btn_Mani.setImage(ResourceManager.getImage("icon16_toavg.png")); //$NON-NLS-1$
                KeyStateManager.addKeyText(btn_Mani, I18n.E3D_ManipulatorToAvg, Task.MOVE_TO_AVG);
                btn_Mani.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Editor3DWindow.getWindow().mntm_Manipulator_2_toAverage();
                    }
                });
            }
            @SuppressWarnings("unused")
            final MenuItem mntmSeparator31 = new MenuItem(mnu_Manipulator, SWT.SEPARATOR);
            {
                MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                btn_Mani.setText(I18n.E3D_ManipulatorToSubfile);
                btn_Mani.setImage(ResourceManager.getImage("icon16_tosubfile.png")); //$NON-NLS-1$
                btn_Mani.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Editor3DWindow.getWindow().mntm_Manipulator_3_toSubfile();
                    }
                });
            }
            {
                MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                btn_Mani.setText(Cocoa.replaceCtrlByCmd(I18n.E3D_SubfileToManipulator));
                btn_Mani.setImage(ResourceManager.getImage("icon16_tosubfile.png")); //$NON-NLS-1$
                btn_Mani.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Editor3DWindow.getWindow().mntm_Manipulator_32_subfileTo(Cocoa.checkCtrlOrCmdPressed(e.stateMask));
                    }
                });
            }
            @SuppressWarnings("unused")
            final MenuItem mntmSeparator32 = new MenuItem(mnu_Manipulator, SWT.SEPARATOR);
            {
                MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                btn_Mani.setText(I18n.E3D_ManipulatorToVertex);
                btn_Mani.setImage(ResourceManager.getImage("icon16_tonearestvertex.png")); //$NON-NLS-1$
                btn_Mani.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Editor3DWindow.getWindow().mntm_Manipulator_4_toVertex();
                    }
                });
            }
            {
                MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                btn_Mani.setText(I18n.E3D_ManipulatorToEdge);
                btn_Mani.setImage(ResourceManager.getImage("icon16_tonearestedge.png")); //$NON-NLS-1$
                btn_Mani.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Editor3DWindow.getWindow().mntm_Manipulator_5_toEdge();
                    }
                });
            }
            {
                MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                btn_Mani.setText(I18n.E3D_ManipulatorToFace);
                btn_Mani.setImage(ResourceManager.getImage("icon16_tonearestface.png")); //$NON-NLS-1$
                btn_Mani.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Editor3DWindow.getWindow().mntm_Manipulator_6_toSurface();
                    }
                });
            }
            @SuppressWarnings("unused")
            final MenuItem mntmSeparator4 = new MenuItem(mnu_Manipulator, SWT.SEPARATOR);
            {
                MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                btn_Mani.setText(I18n.E3D_ManipulatorToVertexN);
                btn_Mani.setImage(ResourceManager.getImage("icon16_tonearestvertexN.png")); //$NON-NLS-1$
                btn_Mani.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Editor3DWindow.getWindow().mntm_Manipulator_7_toVertexNormal();
                    }
                });
            }
            {
                MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                btn_Mani.setText(I18n.E3D_ManipulatorToVertexP);
                btn_Mani.setImage(ResourceManager.getImage("icon16_tonearestvertexN2.png")); //$NON-NLS-1$
                btn_Mani.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Editor3DWindow.getWindow().mntm_Manipulator_XV_toVertexPosition();
                    }
                });
            }
            {
                MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                btn_Mani.setText(I18n.E3D_ManipulatorToEdgeN);
                btn_Mani.setImage(ResourceManager.getImage("icon16_tonearestedgeN.png")); //$NON-NLS-1$
                btn_Mani.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Editor3DWindow.getWindow().mntm_Manipulator_8_toEdgeNormal();
                    }
                });
            }
            {
                MenuItem btn_Mani = new MenuItem(mnu_Manipulator, SWT.PUSH);
                btn_Mani.setText(I18n.E3D_ManipulatorToFaceN);
                btn_Mani.setImage(ResourceManager.getImage("icon16_tonearestfaceN.png")); //$NON-NLS-1$
                btn_Mani.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Editor3DWindow.getWindow().mntm_Manipulator_9_toSurfaceNormal();
                    }
                });
            }
        }

        {
            // MARK CMenu View Actions
            final Menu mnu_viewActions = new Menu(mntmViewAngles);
            mntmViewActions.setMenu(mnu_viewActions);

            final MenuItem mntmShowOrigin = new MenuItem(mnu_viewActions, SWT.CHECK);
            this.mntmShowOrigin[0] = mntmShowOrigin;
            mntmShowOrigin.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    c3d_modifier.showOrigin(mntmShowOrigin.getSelection());
                }
            });
            mntmShowOrigin.setText(I18n.E3D_Origin);
            mntmShowOrigin.setSelection(true);

            final MenuItem mntmSwitchLights = new MenuItem(mnu_viewActions, SWT.CHECK);
            this.mntmSwitchLights[0] = mntmSwitchLights;
            mntmSwitchLights.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    c3d_modifier.switchLigths(mntmSwitchLights.getSelection());
                }
            });
            mntmSwitchLights.setText(I18n.C3D_Lights);
            mntmSwitchLights.setSelection(true);

            final MenuItem mntmMeshLines = new MenuItem(mnu_viewActions, SWT.CHECK);
            this.mntmMeshLines[0] = mntmMeshLines;
            mntmMeshLines.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    c3d_modifier.switchMeshLines(mntmMeshLines.getSelection());
                }
            });
            mntmMeshLines.setText(I18n.C3D_MeshLines);
            mntmMeshLines.setSelection(true);

            final MenuItem mntmSubMeshLines = new MenuItem(mnu_viewActions, SWT.CHECK);
            this.mntmSubMeshLines[0] = mntmSubMeshLines;
            mntmSubMeshLines.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    c3d_modifier.switchSubMeshLines(mntmSubMeshLines.getSelection());
                }
            });
            mntmSubMeshLines.setText(I18n.C3D_SubfileMeshLines);
            mntmSubMeshLines.setSelection(false);

            final MenuItem mntmVertices = new MenuItem(mnu_viewActions, SWT.CHECK);
            this.mntmVertices[0] = mntmVertices;
            mntmVertices.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    c3d_modifier.switchVertices(mntmVertices.getSelection());
                }
            });
            mntmVertices.setText(I18n.C3D_Vertices);
            mntmVertices.setSelection(true);

            final MenuItem mntmHiddenVertices = new MenuItem(mnu_viewActions, SWT.CHECK);
            this.mntmHiddenVertices[0] = mntmHiddenVertices;
            mntmHiddenVertices.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    c3d_modifier.switchHiddenVertices(mntmHiddenVertices.getSelection());
                }
            });
            mntmHiddenVertices.setText(I18n.C3D_HiddenVertices);
            mntmHiddenVertices.setSelection(false);

            final MenuItem mntmControlPointVertices = new MenuItem(mnu_viewActions, SWT.CHECK);
            this.mntmControlPointVertices[0] = mntmControlPointVertices;
            mntmControlPointVertices.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    c3d_modifier.switchCondlineControlPoints(mntmControlPointVertices.getSelection());
                }
            });
            mntmControlPointVertices.setText(I18n.C3D_CondlineVertices);

            if (WorkbenchManager.getUserSettingState().getOpenGLVersion() >= 33) {
                final MenuItem mntmSmoothShading = new MenuItem(mnu_viewActions, SWT.CHECK);
                this.mntmSmoothShading[0] = mntmSmoothShading;
                mntmSmoothShading.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        c3d_modifier.switchSmoothShading(mntmSmoothShading.getSelection());
                    }
                });
                mntmSmoothShading.setText(I18n.C3D_SmoothShading);
                mntmSmoothShading.setSelection(false);
            }

            final MenuItem mntmStudLogo = new MenuItem(mnu_viewActions, SWT.CHECK);
            this.mntmStudLogo[0] = mntmStudLogo;
            mntmStudLogo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    c3d_modifier.switchShowingLogo(mntmStudLogo.getSelection());
                }
            });
            mntmStudLogo.setText(I18n.C3D_StudLogo);
            mntmStudLogo.setSelection(false);

            final MenuItem mntmLDrawLines = new MenuItem(mnu_viewActions, SWT.CASCADE);
            mntmLDrawLines.setText(I18n.C3D_LDrawLines);

            final MenuItem mntmShowGrid = new MenuItem(mnu_viewActions, SWT.CHECK);
            this.mntmShowGrid[0] = mntmShowGrid;
            mntmShowGrid.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    c3d_modifier.showGrid(mntmShowGrid.getSelection());
                }
            });
            KeyStateManager.addKeyText(mntmShowGrid, I18n.E3D_Grid, Task.SHOW_GRID);
            mntmShowGrid.setSelection(true);

            final MenuItem mntmShowScale = new MenuItem(mnu_viewActions, SWT.CHECK);
            this.mntmShowScale[0] = mntmShowScale;
            mntmShowScale.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    c3d_modifier.showScale(mntmShowScale.getSelection());
                    getCanvas().forceFocus();
                }
            });
            KeyStateManager.addKeyText(mntmShowScale, I18n.E3D_Ruler, Task.SHOW_RULER);

            final MenuItem mntmAxis = new MenuItem(mnu_viewActions, SWT.CHECK);
            this.mntmAxis[0] = mntmAxis;
            mntmAxis.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    c3d_modifier.switchAxis(mntmAxis.getSelection());
                }
            });
            mntmAxis.setText(I18n.C3D_XYZAxis);
            mntmAxis.setSelection(true);

            final MenuItem mntmLabel = new MenuItem(mnu_viewActions, SWT.CHECK);
            this.mntmLabel[0] = mntmLabel;
            mntmLabel.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    c3d_modifier.switchLabel(mntmLabel.getSelection());
                }
            });
            mntmLabel.setText(I18n.C3D_PerspectiveLabel);
            mntmLabel.setSelection(true);

            final MenuItem mntmAnaglyph = new MenuItem(mnu_viewActions, SWT.CHECK);
            this.mntmAnaglyph[0] = mntmAnaglyph;
            mntmAnaglyph.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    c3d_modifier.switchAnaglyph3D(mntmAnaglyph.getSelection());
                }
            });
            mntmAnaglyph.setText(I18n.C3D_Anaglyph3D);
            mntmAnaglyph.setSelection(false);

            @SuppressWarnings("unused")
            final MenuItem mntmViewSeparator = new MenuItem(mnu_viewActions, SWT.SEPARATOR);

            MenuItem mntmSplitHorizontally = new MenuItem(mnu_viewActions, SWT.NONE);
            mntmSplitHorizontally.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    c3d_modifier.splitViewHorizontally();
                }
            });
            mntmSplitHorizontally.setText(I18n.E3D_SplitHorizontally);

            MenuItem mntmSplitVertically = new MenuItem(mnu_viewActions, SWT.NONE);
            mntmSplitVertically.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    c3d_modifier.splitViewVertically();
                }
            });
            mntmSplitVertically.setText(I18n.E3D_SplitVertically);

            @SuppressWarnings("unused")
            final MenuItem mntmViewSeparator2 = new MenuItem(mnu_viewActions, SWT.SEPARATOR);

            MenuItem mntmRotateView = new MenuItem(mnu_viewActions, SWT.NONE);
            mntmRotateView.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    c3d_modifier.spinView();
                }
            });
            mntmRotateView.setText(I18n.E3D_RotateClockwise);

            @SuppressWarnings("unused")
            final MenuItem mntmViewSeparator3 = new MenuItem(mnu_viewActions, SWT.SEPARATOR);

            MenuItem mntmClose = new MenuItem(mnu_viewActions, SWT.NONE);
            mntmClose.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    c3d_modifier.closeView();
                }
            });
            mntmClose.setText(I18n.E3D_CloseView);

            {
                // MARK CMenu LineMode
                final MenuItem[] mntmAlwaysBlack = new MenuItem[1];
                mnu_lineMode = new Menu(mntmLDrawLines);
                mntmLDrawLines.setMenu(mnu_lineMode);

                final MenuItem mntmRealPreview = new MenuItem(mnu_lineMode, SWT.CHECK);
                this.mntmRealPreview[0] = mntmRealPreview;
                mntmRealPreview.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        WidgetSelectionHelper.unselectAllChildButtons(mnu_lineMode);
                        ((MenuItem) e.widget).setSelection(true);
                        mntmAlwaysBlack[0].setSelection(isBlackEdges());
                        c3d_modifier.setLineMode(0);
                    }
                });
                mntmRealPreview.setText(I18n.C3D_RealPreview);
                mntmRealPreview.setSelection(true);

                final MenuItem mntmShowAll = new MenuItem(mnu_lineMode, SWT.CHECK);
                this.mntmShowAll[0] = mntmShowAll;
                mntmShowAll.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        WidgetSelectionHelper.unselectAllChildButtons(mnu_lineMode);
                        ((MenuItem) e.widget).setSelection(true);
                        mntmAlwaysBlack[0].setSelection(isBlackEdges());
                        c3d_modifier.setLineMode(1);
                    }
                });
                mntmShowAll.setText(I18n.C3D_ShowAll);

                final MenuItem mntmStdLines = new MenuItem(mnu_lineMode, SWT.CHECK);
                this.mntmStdLines[0] = mntmStdLines;
                mntmStdLines.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        WidgetSelectionHelper.unselectAllChildButtons(mnu_lineMode);
                        ((MenuItem) e.widget).setSelection(true);
                        mntmAlwaysBlack[0].setSelection(isBlackEdges());
                        c3d_modifier.setLineMode(2);
                    }
                });
                mntmStdLines.setText(I18n.C3D_ShowEdges);

                final MenuItem mntmHideAll = new MenuItem(mnu_lineMode, SWT.CHECK);
                this.mntmHideAll[0] = mntmHideAll;
                mntmHideAll.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        WidgetSelectionHelper.unselectAllChildButtons(mnu_lineMode);
                        ((MenuItem) e.widget).setSelection(true);
                        mntmAlwaysBlack[0].setSelection(isBlackEdges());
                        c3d_modifier.setLineMode(4);
                    }
                });
                mntmHideAll.setText(I18n.C3D_HideAll);


                @SuppressWarnings("unused")
                final MenuItem mntmSeperator = new MenuItem(mnu_lineMode, SWT.SEPARATOR);

                mntmAlwaysBlack[0] = new MenuItem(mnu_lineMode, SWT.CHECK);
                mntmAlwaysBlack[0].addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        setBlackEdges(mntmAlwaysBlack[0].getSelection());
                    }
                });
                mntmAlwaysBlack[0].setText(I18n.C3D_UseAlwaysBlackLines);
                this.mntmAlwaysBlack[0] = mntmAlwaysBlack[0];
            }

        }

        {
            // MARK CMenu RenderMode
            mnu_renderMode = new Menu(mntmRenderModes);
            mntmRenderModes.setMenu(mnu_renderMode);

            final MenuItem mntmNoBFC = new MenuItem(mnu_renderMode, SWT.CHECK);
            this.mntmNoBFC[0] = mntmNoBFC;
            mntmNoBFC.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    WidgetSelectionHelper.unselectAllChildButtons(mnu_renderMode);
                    ((MenuItem) e.widget).setSelection(true);
                    c3d_modifier.setRenderMode(0);
                    getRenderer().disposeAllTextures();
                }
            });
            mntmNoBFC.setText(I18n.C3D_NoBackfaceCulling);
            mntmNoBFC.setSelection(true);

            final MenuItem mntmRandomColours = new MenuItem(mnu_renderMode, SWT.CHECK);
            this.mntmRandomColours[0] = mntmRandomColours;
            mntmRandomColours.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    WidgetSelectionHelper.unselectAllChildButtons(mnu_renderMode);
                    ((MenuItem) e.widget).setSelection(true);
                    c3d_modifier.setRenderMode(1);
                    getRenderer().disposeAllTextures();
                }
            });
            mntmRandomColours.setText(I18n.C3D_RandomColours);

            final MenuItem mntmBFCFrontBack = new MenuItem(mnu_renderMode, SWT.CHECK);
            this.mntmBFCFrontBack[0] = mntmBFCFrontBack;
            mntmBFCFrontBack.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    WidgetSelectionHelper.unselectAllChildButtons(mnu_renderMode);
                    ((MenuItem) e.widget).setSelection(true);
                    c3d_modifier.setRenderMode(2);
                    getRenderer().disposeAllTextures();
                }
            });
            mntmBFCFrontBack.setText(I18n.C3D_GreenRed);

            final MenuItem mntmBFCBack = new MenuItem(mnu_renderMode, SWT.CHECK);
            this.mntmBFCBack[0] = mntmBFCBack;
            mntmBFCBack.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    WidgetSelectionHelper.unselectAllChildButtons(mnu_renderMode);
                    ((MenuItem) e.widget).setSelection(true);
                    c3d_modifier.setRenderMode(3);
                    getRenderer().disposeAllTextures();
                }
            });
            mntmBFCBack.setText(I18n.C3D_RedBackfaces);

            final MenuItem mntmBFCReal = new MenuItem(mnu_renderMode, SWT.CHECK);
            this.mntmBFCReal[0] = mntmBFCReal;
            mntmBFCReal.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    WidgetSelectionHelper.unselectAllChildButtons(mnu_renderMode);
                    ((MenuItem) e.widget).setSelection(true);
                    c3d_modifier.setRenderMode(4);
                    getRenderer().disposeAllTextures();
                }
            });
            mntmBFCReal.setText(I18n.C3D_RealBackfaceCulling);

            final MenuItem mntmBFCTextured = new MenuItem(mnu_renderMode, SWT.CHECK);
            this.mntmBFCTextured[0] = mntmBFCTextured;
            mntmBFCTextured.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {

                    if (!WorkbenchManager.getUserSettingState().isBfcCertificationRequiredForLDrawMode()) {
                        MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
                        messageBox.setText(I18n.DIALOG_Info);
                        messageBox.setMessage(I18n.C3D_PreviewNote);
                        messageBox.open();
                        WorkbenchManager.getUserSettingState().setBfcCertificationRequiredForLDrawMode(true);
                    }

                    WidgetSelectionHelper.unselectAllChildButtons(mnu_renderMode);
                    ((MenuItem) e.widget).setSelection(true);
                    c3d_modifier.setRenderMode(5);
                }
            });
            mntmBFCTextured.setText(I18n.C3D_LDrawStandard);

            {
                @SuppressWarnings("unused")
                final MenuItem mntmSeparator4 = new MenuItem(mnu_renderMode, SWT.SEPARATOR);
            }

            final MenuItem mntmCondlineMode = new MenuItem(mnu_renderMode, SWT.CHECK);
            this.mntmCondlineMode[0] = mntmCondlineMode;
            mntmCondlineMode.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    WidgetSelectionHelper.unselectAllChildButtons(mnu_renderMode);
                    ((MenuItem) e.widget).setSelection(true);
                    c3d_modifier.setRenderMode(6);
                    getRenderer().disposeAllTextures();
                }
            });
            mntmCondlineMode.setText(I18n.C3D_CondlineMode);

            final MenuItem mntmCoplanarityHeatmapMode = new MenuItem(mnu_renderMode, SWT.CHECK);
            this.mntmCoplanarityHeatmapMode[0] = mntmCoplanarityHeatmapMode;
            mntmCoplanarityHeatmapMode.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    WidgetSelectionHelper.unselectAllChildButtons(mnu_renderMode);
                    ((MenuItem) e.widget).setSelection(true);
                    c3d_modifier.setRenderMode(7);
                    getRenderer().disposeAllTextures();
                }
            });
            mntmCoplanarityHeatmapMode.setText(I18n.C3D_CoplanarityMode);

            final MenuItem mntmWireframeMode = new MenuItem(mnu_renderMode, SWT.CHECK);
            this.mntmWireframeMode[0] = mntmWireframeMode;
            mntmWireframeMode.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    WidgetSelectionHelper.unselectAllChildButtons(mnu_renderMode);
                    ((MenuItem) e.widget).setSelection(true);
                    c3d_modifier.setRenderMode(-1);
                    getRenderer().disposeAllTextures();
                }
            });
            mntmWireframeMode.setText(I18n.C3D_Wireframe);
        }

        {
            // MARK CMenu Synchronise
            mnu_syncronise = new Menu(mntmSynchronise);
            mntmSynchronise.setMenu(mnu_syncronise);

            final MenuItem mntmSyncroniseManipulator = new MenuItem(mnu_syncronise, SWT.CHECK);
            this.mntmSyncManipulator[0] = mntmSyncroniseManipulator;
            mntmSyncroniseManipulator.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    setSyncManipulator(mntmSyncManipulator[0].getSelection());
                    for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                        Composite3D c3d = renderer.getC3D();
                        if (lockableDatFileReference.equals(c3d.getLockableDatFileReference())) {
                            c3d.setSyncManipulator(isSyncManipulator());
                        }
                    }
                }
            });
            mntmSyncroniseManipulator.setText(I18n.E3D_SyncManipulator);

            final MenuItem mntmSyncTranslation = new MenuItem(mnu_syncronise, SWT.CHECK);
            this.mntmSyncTranslate[0] = mntmSyncTranslation;
            mntmSyncTranslation.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    setSyncTranslation(mntmSyncTranslate[0].getSelection());
                    for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                        Composite3D c3d = renderer.getC3D();
                        if (lockableDatFileReference.equals(c3d.getLockableDatFileReference())) {
                            c3d.setSyncTranslation(isSyncTranslation());
                        }
                    }
                }
            });
            mntmSyncTranslation.setText(I18n.E3D_SyncTranslation);

            final MenuItem mntmSyncroniseZoom = new MenuItem(mnu_syncronise, SWT.CHECK);
            this.mntmSyncZoom[0] = mntmSyncroniseZoom;
            mntmSyncroniseZoom.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    setSyncZoom(mntmSyncZoom[0].getSelection());
                    for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                        Composite3D c3d = renderer.getC3D();
                        if (lockableDatFileReference.equals(c3d.getLockableDatFileReference())) {
                            c3d.setSyncZoom(isSyncZoom());
                        }
                    }
                }
            });
            mntmSyncroniseZoom.setText(I18n.E3D_SyncZoom);
        }

        GL.setCapabilities(capabilities);
        canvas.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                canvas.getCursor().dispose();
            }
        });
        // MARK Resize
        canvas.addListener(SWT.Resize, new Listener() {
            @Override
            public void handleEvent(Event event) {
                canvas.setCurrent();
                GL.setCapabilities(capabilities);
                perspective.initializeViewportPerspective();
                ViewIdleManager.pause[0].compareAndSet(false, true);
                VertexWindow.placeVertexWindow();
            }
        });

        canvas.addListener(SWT.MouseDown, new Listener() {
            @Override
            // MARK MouseDown
            public void handleEvent(Event event) {
                mouse.mouseDown(event);
            }
        });

        canvas.addListener(SWT.MouseMove, new Listener() {
            @Override
            // MARK MouseMove
            public void handleEvent(Event event) {
                mouse.mouseMove(event);
            }
        });

        canvas.addListener(SWT.MouseUp, new Listener() {
            @Override
            // MARK MouseUp
            public void handleEvent(Event event) {
                mouse.mouseUp(event);
            }
        });

        canvas.addListener(SWT.MouseEnter, new Listener() {
            @Override
            // MARK MouseEnter
            public void handleEvent(Event event) {
                setHasMouse(true);
            }
        });

        canvas.addListener(SWT.MouseExit, new Listener() {
            @Override
            // MARK MouseExit
            public void handleEvent(Event event) {
                setHasMouse(false);
            }
        });

        canvas.addListener(SWT.MouseVerticalWheel, new Listener() {
            @Override
            // MARK MouseVerticalWheel
            public void handleEvent(Event event) {
                Project.setFileToEdit(lockableDatFileReference);
                final Composite3D c3d = getComposite3D();
                canvas.forceFocus();
                lockableDatFileReference.setLastSelectedComposite(c3d);
                sb1.setSelection(0);
                sb2.setSelection(0);
                if (event.count < 0)
                    perspective.zoomIn();
                else
                    perspective.zoomOut();

                ViewIdleManager.refresh(c3d.getCanvas(), c3d.getRenderer());
            }
        });

        new Win32MouseWheelFilter(canvas.getDisplay());

        canvas.addListener(SWT.MouseDoubleClick, new Listener() {
            @Override
            // MARK MouseDoubleClick
            public void handleEvent(Event event) {
                Project.setFileToEdit(lockableDatFileReference);
                mouse.mouseDoubleClick(event);
            }
        });

        canvas.addListener(SWT.KeyDown, new Listener() {
            @Override
            // MARK KeyDown
            public void handleEvent(Event event) {
                final Composite3D c3d = getComposite3D();
                ViewIdleManager.refresh(c3d.getCanvas(), c3d.getRenderer());
                keyboard.setStates(event.keyCode, SWT.KeyDown, event);
            }
        });

        canvas.addListener(SWT.KeyUp, new Listener() {
            @Override
            // MARK KeyUp
            public void handleEvent(Event event) {
                final Composite3D c3d = getComposite3D();
                ViewIdleManager.refresh(c3d.getCanvas(), c3d.getRenderer());
                keyboard.setStates(event.keyCode, SWT.KeyUp, event);
            }
        });

        canvas.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                Project.setFileToEdit(lockableDatFileReference);
            }
        });

        canvas.addMenuDetectListener(new MenuDetectListener() {

            @Override
            public void menuDetected(MenuDetectEvent e) {
                if (e.detail != SWT.MENU_KEYBOARD) {
                    return;
                }
                java.awt.Point b = java.awt.MouseInfo.getPointerInfo().getLocation();
                final int x = (int) b.getX();
                final int y = (int) b.getY();

                Menu menu = getMenu();
                if (!menu.isDisposed()) {
                    menu.setLocation(x, y);
                    menu.setVisible(true);
                }

            }
        });

        Transfer[] types = new Transfer[] { MyDummyTransfer2.getInstance(), FileTransfer.getInstance() };
        int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;
        DropTarget target = new DropTarget(this, operations);
        target.setTransfer(types);
        target.addDropListener(new DropTargetAdapter() {

            volatile AtomicBoolean newMouseMove = new AtomicBoolean(true);

            @Override
            public void dragEnter(DropTargetEvent event) {
                final org.nschmidt.ldparteditor.data.Primitive p = Editor3DWindow.getWindow().getCompositePrimitive().getSelectedPrimitive();
                if (p == null || p.isCategory()) return;
                setDraggedPrimitive(p);
            }

            @Override
            public void dragOver(DropTargetEvent event) {
                final org.nschmidt.ldparteditor.data.Primitive p = Editor3DWindow.getWindow().getCompositePrimitive().getSelectedPrimitive();
                if (p == null || p.isCategory()) return;
                setDraggedPrimitive(p);
                final Event ev = new Event();
                ev.x = event.x - toDisplay(1, 1).x;
                ev.y = event.y - toDisplay(1, 1).y;
                ev.stateMask = ev.stateMask;
                if (newMouseMove.compareAndSet(true, false)) Display.getCurrent().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mouse.mouseMove(ev);
                            newMouseMove.set(true);
                        } catch (SWTException swtEx) { /* consumed */ }
                    }
                });
            }

            @Override
            public void dragLeave(DropTargetEvent event) {
                setDraggedPrimitive(null);
            }

            @Override
            public void drop(DropTargetEvent event) {
                String fileList[] = null;
                FileTransfer ft = FileTransfer.getInstance();
                if (ft.isSupportedType(event.currentDataType)) {
                    fileList = (String[]) event.data;
                    if (fileList != null) {
                        for (String f : fileList) {
                            NLogger.debug(getClass(), f);
                            if (f.toLowerCase(Locale.ENGLISH).endsWith(".dat")) { //$NON-NLS-1$
                                final File fileToOpen = new File(f);
                                if (!fileToOpen.exists() || fileToOpen.isDirectory()) continue;
                                DatFile df = Editor3DWindow.getWindow().openDatFile(getShell(), OpenInWhat.EDITOR_3D, f, true);
                                if (df != null) {
                                    boolean tabSync = WorkbenchManager.getUserSettingState().isSyncingTabs();
                                    WorkbenchManager.getUserSettingState().setSyncingTabs(false);
                                    Editor3DWindow.getWindow().addRecentFile(df);
                                    final File f2 = new File(df.getNewName());
                                    if (f2.getParentFile() != null) {
                                        Project.setLastVisitedPath(f2.getParentFile().getAbsolutePath());
                                    }
                                    for (EditorTextWindow w : Project.getOpenTextWindows()) {
                                        for (CTabItem t : w.getTabFolder().getItems()) {
                                            if (df.equals(((CompositeTab) t).getState().getFileNameObj())) {
                                                w.closeTabWithDatfile(df);
                                                WorkbenchManager.getUserSettingState().setSyncingTabs(tabSync);
                                                return;
                                            }
                                        }
                                    }
                                    WorkbenchManager.getUserSettingState().setSyncingTabs(tabSync);
                                }
                                break;
                            }
                        }
                    }
                    return;
                }
                NLogger.debug(getClass(), "Primitive dropped at: {0} | {1}", event.x, event.y); //$NON-NLS-1$
                final Editor3DWindow window = Editor3DWindow.getWindow();
                final org.nschmidt.ldparteditor.data.Primitive p = window.getCompositePrimitive().getSelectedPrimitive();
                final DatFile datfile = getLockableDatFileReference();
                if (p == null || p.isCategory() || datfile.isReadOnly()) return;
                NLogger.debug(getClass(), "Primitive: {0}", p); //$NON-NLS-1$
                String ref = p.getName();
                final BigDecimal[] cur = getCursorSnapped3Dprecise();
                final GColour col16 = View.getLDConfigColour(16);
                Set<String> alreadyParsed = new HashSet<String>();
                alreadyParsed.add(datfile.getShortName());
                ArrayList<ParsingResult> subfileLine = DatParser
                        .parseLine(
                                "1 16 " + MathHelper.bigDecimalToString(cur[0]) + " " + MathHelper.bigDecimalToString(cur[1]) + " " + MathHelper.bigDecimalToString(cur[2]) + " 1 0 0 0 1 0 0 0 1 " + ref, -1, 0, col16.getR(), col16.getG(), col16.getB(), 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, datfile, false, alreadyParsed, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                GData1 primitiveLine = (GData1) subfileLine.get(0).getGraphicalData();
                if (primitiveLine != null) {
                    datfile.addToTailOrInsertAfterCursor(primitiveLine);
                    datfile.getVertexManager().setModified(true, true);
                    if (!Project.getUnsavedFiles().contains(datfile)) {
                        Project.addUnsavedFile(datfile);
                        Editor3DWindow.getWindow().updateTree_unsavedEntries();
                    }
                    datfile.getVertexManager().validateState();
                }
            }
        });

        openGL.init();

        perspective.setPerspective(Perspective.FRONT);

    }

    private void openInTextEditor() {
        DatFile df = lockableDatFileReference;
        if (df.equals(View.DUMMY_DATFILE)) return;
        for (EditorTextWindow w : Project.getOpenTextWindows()) {
            for (CTabItem t : w.getTabFolder().getItems()) {
                if (df.equals(((CompositeTab) t).getState().getFileNameObj())) {
                    w.getTabFolder().setSelection(t);
                    ((CompositeTab) t).getControl().getShell().forceActive();
                    if (w.isSeperateWindow()) {
                        w.open();
                    }
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
    }

    @Override
    public SashForm getSashForm() {
        return ((ScalableComposite) this.getParent()).getSashForm();
    }

    @Override
    public Composite3D getComposite3D() {
        return this;
    }

    @Override
    public CompositeContainer getCompositeContainer() {
        return ((ScalableComposite) this.getParent()).getCompositeContainer();
    }

    @Override
    public Menu getMenu() {
        return menu;
    }

    /**
     * @return the view zoom level exponent
     */
    public float getZoom() {
        return zoom;
    }

    /**
     * Set the view zoom level exponent
     *
     * @param zoom
     *            value between -10.0 and 10.0
     */
    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    /**
     * @return {@code true} if the view represents standard +/- XY, XZ or YZ
     *         order
     */
    public boolean isClassicPerspective() {
        return classicPerspective;
    }

    /**
     * Set to {@code true} if the view represents standard +/- XY, XZ or YZ
     * order.
     *
     * @param isClassicPerspective
     *            value to set.
     */
    public void setClassicPerspective(boolean isClassicPerspective) {
        this.classicPerspective = isClassicPerspective;
    }

    /**
     * @return The translation matrix of the view
     */
    public Matrix4f getTranslation() {
        return viewport_translation;
    }

    /**
     * @return The rotation matrix of the view
     */
    public Matrix4f getRotation() {
        return viewport_rotation;
    }

    /**
     * @return The generator array of the view
     */
    public Vector4f[] getGenerator() {
        return viewport_generator;
    }

    /**
     * @return the {@linkplain Perspective}-Index of the viewport
     */
    public Perspective getPerspectiveIndex() {
        return viewport_perspective;
    }

    /**
     * Sets the {@linkplain Perspective}-Index of the viewport
     *
     * @param perspective
     *            the {@linkplain Perspective}-Index
     */
    public void setPerspectiveIndex(Perspective perspective) {
        this.viewport_perspective = perspective;
    }

    /**
     * @return the origin-axis of the viewport
     */
    public Vector3f[] getViewportOriginAxis() {
        return viewport_origin_axis;
    }

    /**
     * @return The transformation matrix of the viewport which was last drawn
     */
    public Matrix4f getViewport() {
        return viewport_matrix;
    }

    public Matrix4f getViewport_Inverse() {
        return viewport_matrix_inv;
    }

    /**
     * Sets the transformation matrix of the viewport
     *
     * @param matrix
     *            the matrix to set.
     */
    public void setViewport(Matrix4f matrix) {
        GData.CACHE_viewByProjection.clear();
        viewport_matrix.load(matrix);
        viewport_matrix_inv = (Matrix4f) matrix.invert();
    }

    /**
     * @return the viewport z-Near value
     */
    public double getzNear() {
        return zNear;
    }

    /**
     * @param zNear
     *            the viewport z-Near value to set
     */
    public void setzNear(double zNear) {
        this.zNear = zNear;
    }

    /**
     * @return the viewport z-Far value
     */
    public double getzFar() {
        return zFar;
    }

    /**
     * @param zFar
     *            the viewport z-Far value to set
     */
    public void setzFar(double zFar) {
        this.zFar = zFar;
    }

    /**
     * @return the resolution of the viewport in pixel per LDU
     */
    public float getViewportPixelPerLDU() {
        return viewport_pixel_per_ldu;
    }

    /**
     * @param set
     *            the resolution of the viewport in pixel per LDU
     */
    public void setViewportPixelPerLDU(float viewport_pixel_per_ldu) {
        this.viewport_pixel_per_ldu = viewport_pixel_per_ldu;
    }

    /**
     * @return the {@linkplain PerspectiveCalculator}
     */
    public PerspectiveCalculator getPerspectiveCalculator() {
        return perspective;
    }

    /**
     * @return the {@linkplain Composite3DModifier}
     */
    public Composite3DModifier getModifier() {
        return c3d_modifier;
    }

    /**
     * @return the {@linkplain KeyStateManager}
     */
    public KeyStateManager getKeys() {
        return keyboard;
    }

    /**
     * Returns the cascade drop down menu for the different view angles.
     *
     * @return the receiver's menu
     */
    public Menu getViewAnglesMenu() {
        return mnu_viewAngles;
    }

    /**
     * @return the {@linkplain OpenGLRenderer}
     */
    public OpenGLRenderer getRenderer() {
        return openGL;
    }

    /**
     * @return the {@linkplain MouseActions}
     */
    public MouseActions getMouse() {
        return mouse;
    }

    /**
     * @return the cursor in the 3D space
     */
    public Vector4f getCursor3D() {
        return cursor3D;
    }

    /**
     * @return the snapped cursor in the 3D space
     */
    public Vector4f getCursorSnapped3D() {
        return cursorSnapped3D;
    }

    /**
     * @return the snapped cursor in the 3D space
     */
    public BigDecimal[] getCursorSnapped3Dprecise() {
        return cursorSnapped3Dprecise;
    }

    public Vector4f getSelectionStart() {
        return selectionStart;
    }

    public Manipulator getManipulator() {
        return manipulator;
    }

    /**
     * @return the canvas
     */
    public GLCanvas getCanvas() {
        return canvas;
    }

    public GLCapabilities getCapabilities() {
        return capabilities;
    }

    /**
     * @return the grid vector array (upper left corner, grid x-direction, grid
     *         y-direction, cell count)
     */
    public Vector4f[] getGrid() {
        return grid_data;
    }

    /**
     * @return {@code true} if the origin is shown.
     */
    public boolean isOriginShown() {
        return originShown;
    }

    /**
     * Set to {@code true} if the origin is shown.
     *
     * @param originShown
     *            value to set.
     */
    public void setOriginShown(boolean originShown) {
        this.originShown = originShown;
    }

    /**
     * @return {@code true} if the grid is shown.
     */
    public boolean isGridShown() {
        return gridShown;
    }

    /**
     * Set to {@code true} if the grid is shown.
     *
     * @param gridShown
     *            value to set.
     */
    public void setGridShown(boolean gridShown) {
        this.gridShown = gridShown;
    }

    public int getRenderMode() {
        return renderMode;
    }

    public void setRenderMode(int renderMode) {
        this.renderMode = renderMode;
    }

    public int getLineMode() {
        return lineMode;
    }

    public void setLineMode(int lineMode) {
        this.lineMode = lineMode;
    }

    public boolean isLightOn() {
        return lightOn;
    }

    public void setLightOn(boolean lightOn) {
        this.lightOn = lightOn;
    }

    public boolean isMeshLines() {
        return meshLines;
    }

    public void setMeshLines(boolean meshLines) {
        this.meshLines = meshLines;
    }

    public boolean isSubMeshLines() {
        return subMeshLines;
    }

    public void setSubMeshLines(boolean subMeshLines) {
        this.subMeshLines = subMeshLines;
    }

    public boolean isShowingVertices() {
        return showingVertices;
    }

    public void setShowingVertices(boolean showingVertices) {
        this.showingVertices = showingVertices;
    }

    public boolean isShowingHiddenVertices() {
        return showingHiddenVertices;
    }

    public void setShowingHiddenVertices(boolean showingHiddenVertices) {
        this.showingHiddenVertices = showingHiddenVertices;
    }

    /**
     * @return The grid size as a scaling factor
     */
    public float getGrid_scale() {
        return grid_scale;
    }

    /**
     * Sets the grid size scaling factor
     */
    public void setGrid_scale(float scale) {
        this.grid_scale = scale;
    }

    public DatFile getLockableDatFileReference() {
        return lockableDatFileReference;
    }

    public void setLockableDatFileReference(DatFile datFile) {
        if (locked != null && !locked.isDisposed()) this.datFileLockedOnDisplay = locked.getSelection();
        Editor3DWindow.getWindow().saveState(this.lockableDatFileReference, this);
        this.lockableDatFileReference = datFile;
        Editor3DWindow.getWindow().loadState(datFile, this);
    }

    public boolean isDatFileLockedOnDisplay() {
        return datFileLockedOnDisplay;
    }

    public void setDatFileLockedOnDisplay(boolean datFileLockedDisplay) {
        this.datFileLockedOnDisplay = datFileLockedDisplay;
    }

    public boolean isDoingSelection() {
        return doingSelection;
    }

    public void setDoingSelection(boolean doingSelection) {
        this.doingSelection = doingSelection;
    }

    public boolean isAnaglyph3d() {
        return anaglyph3d;
    }

    public void setAnaglyph3d(boolean anaglyph3d) {
        this.anaglyph3d = anaglyph3d;
    }

    public Vector4f getCursorPosition() {
        return cursorPosition;
    }

    public Vector2f getMousePosition() {
        return mousePosition;
    }

    public Vector2f getOldMousePosition() {
        return oldMousePosition;
    }

    public Vector4f getSelectionWidth() {
        return selectionWidth;
    }

    public Vector4f getSelectionHeight() {
        return selectionHeight;
    }

    public boolean isDrawingSolidMaterials() {
        return drawingSolidMaterials;
    }

    public void setDrawingSolidMaterials(boolean drawingSolidMaterials) {
        this.drawingSolidMaterials = drawingSolidMaterials;
    }

    public boolean hasNegDeterminant() {
        return negDeterminant;
    }

    public void setNegDeterminant(boolean negDeterminant) {
        this.negDeterminant = negDeterminant;
    }

    public void setShowingLogo(boolean showingLogo) {
        this.showingLogo = showingLogo;
    }

    public boolean isShowingLogo() {
        return this.showingLogo;
    }

    public void setCursorSnapped3Dprecise(BigDecimal x, BigDecimal y, BigDecimal z) {
        cursorSnapped3Dprecise[0] = x;
        cursorSnapped3Dprecise[1] = y;
        cursorSnapped3Dprecise[2] = z;
    }

    public void unlinkData() {
        lockableDatFileReference = View.DUMMY_DATFILE;
        datFileLockedOnDisplay = false;
    }

    public VertexManager getVertexManager() {
        return lockableDatFileReference.getVertexManager();
    }

    public boolean isBlackEdges() {
        return blackEdges;
    }

    public void setBlackEdges(boolean blackEdges) {
        this.blackEdges = blackEdges;
    }

    public Primitive getDraggedPrimitive() {
        return draggedPrimitive;
    }

    public void setDraggedPrimitive(Primitive draggedPrimitive) {
        this.draggedPrimitive = draggedPrimitive;
    }

    public boolean isShowingAxis() {
        return showingAxis;
    }

    public void setShowingAxis(boolean showingAxis) {
        this.showingAxis = showingAxis;
    }

    public boolean isShowingLabels() {
        return showingLabels;
    }

    public void setShowingLabels(boolean showingLabels) {
        this.showingLabels = showingLabels;
    }

    public void setSmoothShading(boolean smoothShading) {
        this.smoothShading = smoothShading;
    }

    public boolean isSmoothShading() {
        return smoothShading;
    }

    public MenuItem getMntmNoBFC() {
        return mntmNoBFC[0];
    }

    public MenuItem getMntmRandomColours() {
        return mntmRandomColours[0];
    }

    public MenuItem getMntmBFCFrontBack() {
        return mntmBFCFrontBack[0];
    }

    public MenuItem getMntmBFCBack() {
        return mntmBFCBack[0];
    }

    public MenuItem getMntmBFCReal() {
        return mntmBFCReal[0];
    }

    public MenuItem getMntmBFCTextured() {
        return mntmBFCTextured[0];
    }

    public MenuItem getMntmCondlineMode() {
        return mntmCondlineMode[0];
    }

    public MenuItem getMntmCoplanarityHeatmapMode() {
        return mntmCoplanarityHeatmapMode[0];
    }

    public MenuItem getMntmWireframeMode() {
        return mntmWireframeMode[0];
    }

    public MenuItem getMntmAnaglyph() {
        return mntmAnaglyph[0];
    }

    public MenuItem getMntmAxis() {
        return mntmAxis[0];
    }

    public MenuItem getMntmAlwaysBlack() {
        return mntmAlwaysBlack[0];
    }

    public MenuItem getMntmHideAll() {
        return mntmHideAll[0];
    }

    public MenuItem getMntmStdLines() {
        return mntmStdLines[0];
    }

    public MenuItem getMntmShowAll() {
        return mntmShowAll[0];
    }

    public MenuItem getMntmStudLogo() {
        return mntmStudLogo[0];
    }

    public MenuItem getMntmSmoothShading() {
        return mntmSmoothShading[0];
    }

    public MenuItem getMntmControlPointVertices() {
        return mntmControlPointVertices[0];
    }

    public MenuItem getMntmHiddenVertices() {
        return mntmHiddenVertices[0];
    }

    public MenuItem getMntmVertices() {
        return mntmVertices[0];
    }

    public MenuItem getMntmSubMeshLines() {
        return mntmSubMeshLines[0];
    }

    public MenuItem getMntmMeshLines() {
        return mntmMeshLines[0];
    }

    public MenuItem getMntmSwitchLights() {
        return mntmSwitchLights[0];
    }

    public MenuItem getMntmShowOrigin() {
        return mntmShowOrigin[0];
    }

    public MenuItem getMntmTwoThirds() {
        return mntmTwoThirds[0];
    }

    public MenuItem getMntmBottom() {
        return mntmBottom[0];
    }

    public MenuItem getMntmTop() {
        return mntmTop[0];
    }

    public MenuItem getMntmRight() {
        return mntmRight[0];
    }

    public MenuItem getMntmLeft() {
        return mntmLeft[0];
    }

    public MenuItem getMntmBack() {
        return mntmBack[0];
    }

    public MenuItem getMntmFront() {
        return mntmFront[0];
    }

    public MenuItem getMntmShowGrid() {
        return mntmShowGrid[0];
    }

    public MenuItem getMntmShowScale() {
        return mntmShowScale[0];
    }

    public MenuItem getMntmLabel() {
        return mntmLabel[0];
    }

    public MenuItem getMntmRealPreview() {
        return mntmRealPreview[0];
    }

    public boolean isWarpedSelection() {
        return warpedSelection;
    }

    public void setWarpedSelection(boolean warpedSelection) {
        this.warpedSelection = warpedSelection;
    }

    public boolean isShowingCondlineControlPoints() {
        return showingCondlineControlPoints;
    }

    public void setShowingCondlineControlPoints(boolean showingCondlineControlPoints) {
        this.showingCondlineControlPoints = showingCondlineControlPoints;
    }

    public boolean isSyncManipulator() {
        return syncManipulator;
    }

    public void setSyncManipulator(boolean syncManipulator) {
        this.syncManipulator = syncManipulator;
        mntmSyncManipulator[0].setSelection(syncManipulator);
    }

    public boolean isSyncTranslation() {
        return syncTranslation;
    }

    public void setSyncTranslation(boolean syncTranslation) {
        this.syncTranslation = syncTranslation;
        mntmSyncTranslate[0].setSelection(syncTranslation);
    }

    public boolean isSyncZoom() {
        return syncZoom;
    }

    public void setSyncZoom(boolean syncZoom) {
        this.syncZoom = syncZoom;
        mntmSyncZoom[0].setSelection(syncZoom);
    }

    public Set<Vertex> getTmpHiddenVertices() {
        return tmpHiddenVertices;
    }

    public void joinSelectionInTextEditor(final DatFile df) {
        if (df.equals(View.DUMMY_DATFILE) || df.isReadOnly()) return;
        final VertexManager vm = df.getVertexManager();
        if (!vm.getSelectedData().isEmpty()) {
            Display.getCurrent().asyncExec(new Runnable() {
                @Override
                public void run() {

                    showSelectionInTextEditor(df, false);

                    boolean isOpen = false;
                    CompositeTab ct = null;
                    final CompositeTab ct2;
                    for (EditorTextWindow w : Project.getOpenTextWindows()) {
                        for (final CTabItem t : w.getTabFolder().getItems()) {
                            ct = (CompositeTab) t;
                            if (df.equals(ct.getState().getFileNameObj())) {
                                isOpen = true;
                                break;
                            }
                        }
                        if (isOpen) {
                            break;
                        }
                    }

                    if (!isOpen) {
                        openInTextEditor();
                        for (EditorTextWindow w : Project.getOpenTextWindows()) {
                            for (final CTabItem t : w.getTabFolder().getItems()) {
                                ct = (CompositeTab) t;
                                if (df.equals(ct.getState().getFileNameObj())) {
                                    isOpen = true;
                                    break;
                                }
                            }
                            if (isOpen) {
                                break;
                            }
                        }
                    }

                    ct2 = ct;

                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            int minLine = Integer.MAX_VALUE;
                            final HashBiMap<Integer, GData> dpl = df.getDrawPerLine_NOCLONE();
                            for (GData g : vm.getSelectedData()) {
                                if (dpl.containsValue(g)) {
                                    int line = dpl.getKey(g);
                                    if (line < minLine) {
                                        minLine = line;
                                    }
                                }
                            }
                            if (minLine != Integer.MAX_VALUE) {
                                ct2.getTextComposite().setTopIndex(minLine - 1);
                                ct2.getTextComposite().setCaretOffset(ct2.getTextComposite().getOffsetAtLine(minLine - 1));
                                ct2.getTextComposite().redraw();
                            }

                            vm.getSelectedVertices().clear();
                            vm.copy();

                            vm.delete(false, true);


                            if (minLine != Integer.MAX_VALUE) {
                                ct2.getTextComposite().setTopIndex(minLine - 1);
                                ct2.getTextComposite().setCaretOffset(ct2.getTextComposite().getOffsetAtLine(minLine - 1));
                                ct2.getTextComposite().redraw();
                            }

                            vm.pasteToJoin(dpl.getValue(Math.max(minLine - 1, 1)));
                        }
                    });
                }
            });
        }
    }

    public static void showSelectionInTextEditor(final DatFile df, final boolean setTopIndex) {
        if (df.equals(View.DUMMY_DATFILE)) return;
        if (!df.getVertexManager().getSelectedData().isEmpty() || !df.getVertexManager().getSelectedVertices().isEmpty()) {
            for (EditorTextWindow w : Project.getOpenTextWindows()) {
                for (final CTabItem t : w.getTabFolder().getItems()) {
                    if (df.equals(((CompositeTab) t).getState().getFileNameObj())) {

                        w.getTabFolder().setSelection(t);
                        ((CompositeTab) t).getControl().getShell().forceActive();

                        Display.getDefault().asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                final VertexManager vm = df.getVertexManager();
                                if (vm.getSelectedData().size() > 0 || vm.getSelectedVertices().size() > 0) {

                                    final int oldIndex = ((CompositeTab) t).getTextComposite().getTopIndex() + 1;
                                    final int lastSetIndex = ((CompositeTab) t).getState().getOldLineIndex();
                                    final ArrayList<Integer> indices = new ArrayList<Integer>();
                                    final HashSet<GData> selection = new HashSet<GData>();
                                    Integer index;

                                    selection.addAll(vm.getSelectedData());
                                    selection.addAll(vm.getSelectedSubfiles());

                                    for (Vertex v : vm.getSelectedVertices()) {
                                        selection.addAll(vm.getLinkedVertexMetaCommands(v));
                                    }

                                    for (GData g : selection) {
                                        index = df.getDrawPerLine_NOCLONE().getKey(g);
                                        if (index != null) {
                                            indices.add(index);
                                        }
                                    }

                                    if (!indices.isEmpty()) {

                                        Collections.sort(indices);

                                        index = indices.get(0);
                                        for (int i : indices) {
                                            if (i > oldIndex) {
                                                index = i;
                                                break;
                                            }
                                        }

                                        if (index == lastSetIndex) {
                                            index = indices.get(0);
                                        }

                                        if (setTopIndex) {
                                            ((CompositeTab) t).getState().setOldLineIndex(index);
                                            ((CompositeTab) t).getTextComposite().setTopIndex(index - 1);
                                        }
                                    }
                                }
                                ((CompositeTab) t).getTextComposite().redraw();
                            }
                        });
                        if (w.isSeperateWindow()) {
                            w.open();
                        }
                        return;
                    }
                }
            }

            final Integer index2;
            final VertexManager vm = df.getVertexManager();
            if (vm.getSelectedData().size() > 0) {
                Integer index = df.getDrawPerLine_NOCLONE().getKey(vm.getSelectedData().iterator().next());
                if (index == null) {
                    if (vm.getSelectedSubfiles().size() > 0) {
                        index = df.getDrawPerLine_NOCLONE().getKey(vm.getSelectedSubfiles().iterator().next());
                        if (index == null) {
                            return;
                        }
                    } else {
                        return;
                    }
                }
                index2 = index;
            } else {
                return;
            }

            final HashSet<Integer> selectedIndicies = new HashSet<Integer>();
            final TreeSet<Vertex> selectedVertices = new TreeSet<Vertex>();
            selectedVertices.addAll(vm.getSelectedVertices());
            for (GData gd : vm.getSelectedData()) {
                final Integer i = df.getDrawPerLine_NOCLONE().getKey(gd);
                if (i != null && gd.type() > 0 && gd.type() < 6) {
                    selectedIndicies.add(i);
                }
            }

            // Project.getParsedFiles().add(df); IS NECESSARY HERE
            Project.getParsedFiles().add(df);
            Project.addOpenedFile(df);
            final EditorTextWindow win;
            if (!Project.getOpenTextWindows().isEmpty() && !Project.getOpenTextWindows().iterator().next().isSeperateWindow()) {
                win = Project.getOpenTextWindows().iterator().next();
                win.openNewDatFileTab(df, true);
            } else {
                win = new EditorTextWindow();
                win.run(df, false);
                win.open();
            }

            for (final CTabItem t : win.getTabFolder().getItems()) {
                if (df.equals(((CompositeTab) t).getState().getFileNameObj())) {
                    win.getTabFolder().setSelection(t);
                    ((CompositeTab) t).getControl().getShell().forceActive();
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {

                            if (setTopIndex) {
                                ((CompositeTab) t).getTextComposite().setTopIndex(index2 - 1);
                            }

                            for (Integer i : selectedIndicies) {
                                final GData gs = df.getDrawPerLine_NOCLONE().getValue(i);
                                if (gs != null && gs.type() > 0 && gs.type() < 6) {
                                    switch (gs.type()) {
                                    case 1:
                                        vm.addSubfileToSelection((GData1) gs);
                                        break;
                                    case 2:
                                        vm.getSelectedLines().add((GData2) gs);
                                        break;
                                    case 3:
                                        vm.getSelectedTriangles().add((GData3) gs);
                                        break;
                                    case 4:
                                        vm.getSelectedQuads().add((GData4) gs);
                                        break;
                                    case 5:
                                        vm.getSelectedCondlines().add((GData5) gs);
                                        break;
                                    default:
                                        break;
                                    }
                                    vm.getSelectedData().add(gs);
                                }
                            }
                            vm.getSelectedVertices().addAll(selectedVertices);
                            ((CompositeTab) t).getTextComposite().redraw();
                        }
                    });
                    return;
                }
            }
        }
    }

    public Composite3DViewState exportState() {
        Composite3DViewState state = new Composite3DViewState();

        state.getHideShowState().putAll(lockableDatFileReference.getVertexManager().backupHideShowState());
        state.getSelection().putAll(lockableDatFileReference.getVertexManager().backupSelectedDataState(new HashMap<String, ArrayList<Boolean>>()));
        state.getHiddenVertices().addAll(lockableDatFileReference.getVertexManager().getHiddenVertices());
        state.getSelectedVertices().addAll(lockableDatFileReference.getVertexManager().getSelectedVertices());

        state.getManipulator().copyState(manipulator);
        state.setViewportPixelPerLDU(viewport_pixel_per_ldu);
        state.setZoom(zoom);
        state.setNegDeterminant(negDeterminant);
        state.setZoom_exponent(getPerspectiveCalculator().getZoom_exponent());
        state.setOffset(getPerspectiveCalculator().getOffset());

        state.getViewport_translation().load(viewport_translation);
        state.getViewport_rotation().load(viewport_rotation);
        state.getViewport_matrix().load(viewport_matrix);
        state.getViewport_matrix_inv().load(viewport_matrix_inv);

        for (int i = 0; i < 3; i++) {
            state.getViewport_generator()[i] = viewport_generator[i];
        }
        for (int i = 0; i < 4; i++) {
            state.getViewport_origin_axis()[i] = viewport_origin_axis[i];
        }

        state.setzFar(zFar);
        state.setzNear(zNear);

        // FIXME !Save state here for C3D
        Editor3DWindow.getWindow().fillC3DState(state.STATE, this);

        return state;
    }

    public void importState(Composite3DViewState state) {
        WidgetSelectionHelper.unselectAllChildButtons(mnu_renderMode);

        // FIXME !Load state here for C3D
        loadState(state.STATE);
        manipulator.copyState(state.getManipulator());

        viewport_pixel_per_ldu = state.getViewportPixelPerLDU();
        zoom = state.getZoom();
        negDeterminant = state.hasNegDeterminant();
        getPerspectiveCalculator().setZoom_exponent(state.getZoom_exponent());
        getPerspectiveCalculator().setOffset(state.getOffset());

        viewport_translation.load(state.getViewport_translation());
        viewport_rotation.load(state.getViewport_rotation());
        viewport_matrix.load(state.getViewport_matrix());
        viewport_matrix_inv.load(state.getViewport_matrix_inv());

        for (int i = 0; i < 3; i++) {
            viewport_generator[i] = state.getViewport_generator()[i];
        }
        for (int i = 0; i < 4; i++) {
            viewport_origin_axis[i] = state.getViewport_origin_axis()[i];
        }

        zFar = state.getzFar();
        zNear = state.getzNear();

        getPerspectiveCalculator().initializeViewportPerspective();

        lockableDatFileReference.getVertexManager().restoreHideShowState(state.getHideShowState());
        lockableDatFileReference.getVertexManager().restoreSelectedDataState(state.getSelection());
        lockableDatFileReference.getVertexManager().reSelectSubFiles();
        lockableDatFileReference.getVertexManager().getHiddenVertices().addAll(state.getHiddenVertices());
        lockableDatFileReference.getVertexManager().getSelectedVertices().addAll(state.getSelectedVertices());


        switch (renderMode) {
        case -1: // Wireframe
            mntmWireframeMode[0].setSelection(true);
            break;
        case 0: // No BFC
            mntmNoBFC[0].setSelection(true);
            break;
        case 1: // Random Colours
            mntmRandomColours[0].setSelection(true);
            break;
        case 2: // Front-Backface BFC
            mntmBFCFrontBack[0].setSelection(true);
            break;
        case 3: // Backface only BFC
            mntmBFCBack[0].setSelection(true);
            break;
        case 4: // Real BFC
            mntmBFCReal[0].setSelection(true);
            break;
        case 5: // Real BFC with texture mapping
            mntmRealPreview[0].setSelection(true);
            break;
        case 6: // Special mode for "Add condlines"
            mntmCondlineMode[0].setSelection(true);
            break;
        case 7: // Special mode for coplanar quads
            mntmCoplanarityHeatmapMode[0].setSelection(true);
            break;
        default:
            break;
        }
    }

    public void loadState(Composite3DState state) {
        final int renderMode = state.getRenderMode();
        final int lineMode = state.getLineMode();
        final Perspective perspective = state.getPerspective();
        setGrid_scale(state.getGridScale());
        getPerspectiveCalculator().setPerspective(perspective);
        setOriginShown(state.isShowOrigin());
        setShowingLabels(state.isShowLabel());
        setSmoothShading(state.isSmooth());
        setGridShown(state.isShowGrid());
        setLightOn(state.isLights());
        setMeshLines(state.isMeshlines());
        setSubMeshLines(state.isSubfileMeshlines());
        setShowingVertices(state.isVertices());
        setShowingHiddenVertices(state.isHiddenVertices());
        setShowingLogo(state.isStudLogo());
        setLineMode(lineMode);
        setBlackEdges(state.isAlwaysBlackLines());
        setShowingAxis(state.isShowAxis());
        setAnaglyph3d(state.isAnaglyph3d());
        setRenderMode(renderMode);
        setShowingCondlineControlPoints(state.isCondlineControlPoints());

        setSyncManipulator(state.isSyncManipulator());
        setSyncTranslation(state.isSyncTranslation());
        setSyncZoom(state.isSyncZoom());

        getMntmFront().setSelection(perspective == Perspective.FRONT);
        getMntmBack().setSelection(perspective == Perspective.BACK);
        getMntmLeft().setSelection(perspective == Perspective.LEFT);
        getMntmRight().setSelection(perspective == Perspective.RIGHT);
        getMntmTop().setSelection(perspective == Perspective.TOP);
        getMntmBottom().setSelection(perspective == Perspective.BOTTOM);
        getMntmRealPreview().setSelection(lineMode == 0);
        getMntmShowAll().setSelection(lineMode == 1);
        getMntmStdLines().setSelection(lineMode == 2);
        getMntmHideAll().setSelection(lineMode == 4);
        getMntmAlwaysBlack().setSelection(state.isAlwaysBlackLines());
        getMntmShowOrigin().setSelection(state.isShowOrigin());
        getMntmLabel().setSelection(state.isShowLabel());
        getMntmShowGrid().setSelection(state.isShowGrid());
        getMntmShowScale().setSelection(state.hasScales());
        getMntmSwitchLights().setSelection(state.isLights());
        getMntmMeshLines().setSelection(state.isMeshlines());
        getMntmSubMeshLines().setSelection(state.isSubfileMeshlines());
        getMntmVertices().setSelection(state.isVertices());
        getMntmHiddenVertices().setSelection(state.isHiddenVertices());
        getMntmControlPointVertices().setSelection(state.isCondlineControlPoints());
        getMntmStudLogo().setSelection(state.isStudLogo());
        getMntmAxis().setSelection(state.isShowAxis());
        getMntmAnaglyph().setSelection(state.isAnaglyph3d());
        getMntmNoBFC().setSelection(renderMode == 0);
        getMntmRandomColours().setSelection(renderMode == 1);
        getMntmBFCFrontBack().setSelection(renderMode == 2);
        getMntmBFCBack().setSelection(renderMode == 3);
        getMntmBFCReal().setSelection(renderMode == 4);
        getMntmBFCTextured().setSelection(renderMode == 5);
        getMntmCondlineMode().setSelection(renderMode == 6);
        getMntmCoplanarityHeatmapMode().setSelection(renderMode == 7);
        getMntmWireframeMode().setSelection(renderMode == -1);

        if (getMntmSmoothShading() != null) {
            getMntmSmoothShading().setSelection(state.isSmooth());
        }
    }

    public boolean hasMouse() {
        return hasMouse;
    }

    public void setHasMouse(boolean hasMouse) {
        this.hasMouse = hasMouse;
    }
}
