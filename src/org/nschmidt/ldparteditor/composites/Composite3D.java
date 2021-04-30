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

import static org.nschmidt.ldparteditor.helpers.WidgetUtility.widgetUtil;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;
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
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
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
import org.nschmidt.ldparteditor.dnd.PrimitiveDragAndDropTransfer;
import org.nschmidt.ldparteditor.enums.LDConfig;
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
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeSortedMap;
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
    private final Menu mnuComposite;
    /** The "View Angles"-Menu */
    private final Menu mnuViewAngles;
    /** The "Render Mode"-Menu */
    public final Menu mnuRenderMode;
    /** The "Line Mode"-Menu */
    private final Menu mnuLineMode;
    /** The "Synchronise..."-Menu */
    private final Menu mnuSyncronise;
    /** The "Manipulator"-Menu */
    private final Menu mnuManipulator;

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
    private float viewportPixelPerLDU;
    /** The translation matrix of the view */
    private final Matrix4f viewportTranslation = new Matrix4f();
    /** The view zoom level */
    private volatile float zoom;

    private boolean hasMouse = true;

    private final Vector4f screenXY = new Vector4f(0, 0, 0, 1);

    private final Set<Vertex> tmpHiddenVertices = Collections.newSetFromMap(new ThreadsafeSortedMap<>());

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
    private Perspective viewportPerspective;
    /** The rotation matrix of the view */
    private Matrix4f viewportRotation = new Matrix4f();
    /** The transformation matrix of the view */
    private final Matrix4f viewportMatrix = new Matrix4f();
    /** The inverse transformation matrix of the view */
    private Matrix4f viewportMatrixInv = new Matrix4f();

    /** The generator of the viewport space */
    private final Vector4f[] viewportGenerator = new Vector4f[3];
    /** The origin axis coordinates of the viewport */
    private final Vector3f[] viewportOriginAxis = new Vector3f[] { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
    /** The viewport z-Near value */
    private double zNear = 1000000f;
    /** The viewport z-Far value */
    private double zFar = 1000001f;
    /**
     * The grid information (upper left corner, grid x-direction, grid
     * y-direction, cell count)
     */
    private final Vector4f[] gridData = new Vector4f[] { new Vector4f(), new Vector4f(), new Vector4f(), new Vector4f(), new Vector4f(), new Vector4f(), new Vector4f(), new Vector4f() };

    /** the {@linkplain GLCanvas} */
    private final GLCanvas canvas;
    private final GLCapabilities capabilities;

    // Several helper classes
    /** The {@linkplain PerspectiveCalculator} instance */
    private final PerspectiveCalculator perspective = new PerspectiveCalculator(this);
    /** The {@linkplain Composite3DModifier} instance */
    private final Composite3DModifier c3dModifier = new Composite3DModifier(this);
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
    private float gridScale = 1f;

    public final MenuItem[] mntmNoBFCPtr = new MenuItem[1];
    public final MenuItem[] mntmRandomColoursPtr = new MenuItem[1];
    private final MenuItem[] mntmBFCFrontBackPtr = new MenuItem[1];
    private final MenuItem[] mntmBFCBackPtr = new MenuItem[1];
    private final MenuItem[] mntmBFCRealPtr = new MenuItem[1];
    private final MenuItem[] mntmBFCTexturedPtr = new MenuItem[1];
    public final MenuItem[] mntmCondlineModePtr = new MenuItem[1];
    private final MenuItem[] mntmCoplanarityHeatmapModePtr = new MenuItem[1];
    public final MenuItem[] mntmWireframeModePtr = new MenuItem[1];
    private final MenuItem[] mntmAnaglyphPtr = new MenuItem[1];
    private final MenuItem[] mntmAxisPtr = new MenuItem[1];
    private final MenuItem[] mntmAlwaysBlackPtr = new MenuItem[1];
    private final MenuItem[] mntmHideAllPtr = new MenuItem[1];
    private final MenuItem[] mntmStdLinesPtr = new MenuItem[1];
    private final MenuItem[] mntmShowAllPtr = new MenuItem[1];
    private final MenuItem[] mntmStudLogoPtr = new MenuItem[1];
    private final MenuItem[] mntmSmoothShadingPtr = new MenuItem[1];
    private final MenuItem[] mntmControlPointVerticesPtr = new MenuItem[1];
    private final MenuItem[] mntmHiddenVerticesPtr = new MenuItem[1];
    private final MenuItem[] mntmVerticesPtr = new MenuItem[1];
    private final MenuItem[] mntmSubMeshLinesPtr = new MenuItem[1];
    private final MenuItem[] mntmMeshLinesPtr = new MenuItem[1];
    private final MenuItem[] mntmSwitchLightsPtr = new MenuItem[1];
    private final MenuItem[] mntmShowOriginPtr = new MenuItem[1];
    private final MenuItem[] mntmTwoThirdsPtr = new MenuItem[1];
    private final MenuItem[] mntmBottomPtr = new MenuItem[1];
    private final MenuItem[] mntmTopPtr = new MenuItem[1];
    private final MenuItem[] mntmRightPtr = new MenuItem[1];
    private final MenuItem[] mntmLeftPtr = new MenuItem[1];
    private final MenuItem[] mntmBackPtr = new MenuItem[1];
    private final MenuItem[] mntmFrontPtr = new MenuItem[1];
    private final MenuItem[] mntmShowGridPtr = new MenuItem[1];
    private final MenuItem[] mntmShowScalePtr = new MenuItem[1];
    private final MenuItem[] mntmLabelPtr = new MenuItem[1];
    private final MenuItem[] mntmRealPreviewPtr = new MenuItem[1];

    private final MenuItem[] mntmSyncTranslatePtr = new MenuItem[1];
    private final MenuItem[] mntmSyncManipulatorPtr = new MenuItem[1];
    private final MenuItem[] mntmSyncZoomPtr = new MenuItem[1];

    Composite3D(Composite parentCompositeContainer, boolean syncManipulator, boolean syncTranslation, boolean syncZoom) {
        this(parentCompositeContainer);
        setSyncManipulator(syncManipulator);
        setSyncTranslation(syncTranslation);
        setSyncZoom(syncZoom);
        mntmSyncManipulatorPtr[0].setSelection(syncManipulator);
        mntmSyncTranslatePtr[0].setSelection(syncTranslation);
        mntmSyncZoomPtr[0].setSelection(syncZoom);
    }

    Composite3D(Composite parentCompositeContainer, DatFile df) {
        this(parentCompositeContainer);
        setSyncManipulator(false);
        setSyncTranslation(false);
        setSyncZoom(false);

        Editor3DWindow.renders.remove(openGL);
        Editor3DWindow.canvasList.remove(canvas);
        this.mnuComposite.dispose();
        setLockableDatFileReference(df);
    }

    /**
     * Creates a new 3D Composite in a {@link CompositeContainer}
     *
     * @param parentCompositeContainer
     */
    Composite3D(Composite parentCompositeContainer) {
        super(parentCompositeContainer, I18n.noBiDirectionalTextStyle() | SWT.H_SCROLL | SWT.V_SCROLL);
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
        this.viewportPerspective = Perspective.FRONT;
        this.zoom = 0.00001f;
        Matrix4f.setIdentity(this.viewportRotation);
        Matrix4f.setIdentity(this.viewportTranslation);
        viewportGenerator[0] = new Vector4f(1.0f, 0, 0, 1.0f);
        viewportGenerator[1] = new Vector4f(0, 1.0f, 0, 1.0f);
        viewportGenerator[2] = new Vector4f(0, 0, 1.0f, 1.0f);
        this.viewportPixelPerLDU = this.zoom * View.PIXEL_PER_LDU;
        this.addControlListener(new ControlListener() {
            @Override
            public void controlResized(ControlEvent e) {
                c3dModifier.moveSashOnResize();
            }

            @Override
            public void controlMoved(ControlEvent consumed) {
                // Implementation is not required.
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
        canvas = new GLCanvas(this, I18n.noBiDirectionalTextStyle(), data);
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

        this.mnuComposite = new Menu(this);

        MenuItem mntmViewAngles = new MenuItem(mnuComposite, SWT.CASCADE);
        mntmViewAngles.setText(I18n.E3D_VIEWING_ANGLES);

        MenuItem mntmManipulator = new MenuItem(mnuComposite, SWT.CASCADE);
        mntmManipulator.setText(I18n.E3D_MODIFY_MANIPULATOR);

        MenuItem mntmViewActions = new MenuItem(mnuComposite, SWT.CASCADE);
        mntmViewActions.setText(I18n.E3D_VIEW_ACTIONS);

        MenuItem mntmRenderModes = new MenuItem(mnuComposite, SWT.CASCADE);
        mntmRenderModes.setText(I18n.C3D_RENDER_MODE);

        MenuItem mntmSynchronise = new MenuItem(mnuComposite, SWT.CASCADE);
        mntmSynchronise.setText(I18n.E3D_SYNC);

        new MenuItem(mnuComposite, SWT.SEPARATOR);

        final MenuItem mntmCloseDat = new MenuItem(mnuComposite, SWT.NONE);
        widgetUtil(mntmCloseDat).addSelectionListener(e -> {
            Project.removeOpenedFile(lockableDatFileReference);
            if (!Editor3DWindow.getWindow().closeDatfile(lockableDatFileReference)) {
                Project.addOpenedFile(lockableDatFileReference);
                Project.setFileToEdit(lockableDatFileReference);
                Editor3DWindow.getWindow().updateTreeUnsavedEntries();
            }
        });
        mntmCloseDat.setText(I18n.E3D_CLOSE);
        mntmCloseDat.setSelection(false);

        final MenuItem mntmLockedDat = new MenuItem(mnuComposite, SWT.CHECK);
        locked = mntmLockedDat;
        widgetUtil(mntmLockedDat).addSelectionListener(e -> {
            if (!lockableDatFileReference.equals(View.DUMMY_DATFILE)) {
                c3dModifier.switchLockedDat(mntmLockedDat.getSelection());
            }
        });
        mntmLockedDat.setText(I18n.C3D_LOCK_FILE);
        mntmLockedDat.setSelection(false);

        final MenuItem mntmOpenInTextEditor = new MenuItem(mnuComposite, SWT.NONE);
        widgetUtil(mntmOpenInTextEditor).addSelectionListener(e -> openInTextEditor());
        mntmOpenInTextEditor.setText(I18n.C3D_OPEN_IN_TEXT);
        mntmOpenInTextEditor.setSelection(false);

        final MenuItem mntmSelectionInTextEditor = new MenuItem(mnuComposite, SWT.NONE);
        widgetUtil(mntmSelectionInTextEditor).addSelectionListener(e -> showSelectionInTextEditor(lockableDatFileReference, true));
        mntmSelectionInTextEditor.setText(I18n.C3D_SHOW_IN_TEXT);
        mntmSelectionInTextEditor.setSelection(false);

        final MenuItem mntmJoinInTextEditor = new MenuItem(mnuComposite, SWT.NONE);
        widgetUtil(mntmJoinInTextEditor).addSelectionListener(e -> joinSelectionInTextEditor(lockableDatFileReference));
        mntmJoinInTextEditor.setText(I18n.C3D_JOIN_SELECTION);
        mntmJoinInTextEditor.setSelection(false);

        if (NLogger.debugging) {
            new MenuItem(mnuComposite, SWT.SEPARATOR);

            final MenuItem mntmOpenSnapshot = new MenuItem(mnuComposite, SWT.NONE);
            widgetUtil(mntmOpenSnapshot).addSelectionListener(e -> {
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
            });
            mntmOpenSnapshot.setText("Open Snapshot (DEBUG ONLY)"); //$NON-NLS-1$
            mntmOpenSnapshot.setSelection(false);
        }

        new MenuItem(mnuComposite, SWT.SEPARATOR);

        MenuItem mntmCut = new MenuItem(mnuComposite, I18n.rightToLeftStyle());
        widgetUtil(mntmCut).addSelectionListener(e -> {
            if (lockableDatFileReference.equals(View.DUMMY_DATFILE)) return;
            lockableDatFileReference.getVertexManager().addSnapshot();
            lockableDatFileReference.getVertexManager().copy();
            lockableDatFileReference.getVertexManager().delete(false, true);
        });
        mntmCut.setText(I18n.COPYNPASTE_CUT);
        mntmCut.setImage(ResourceManager.getImage("icon16_edit-cut.png")); //$NON-NLS-1$
        MenuItem mntmCopy = new MenuItem(mnuComposite, I18n.rightToLeftStyle());
        widgetUtil(mntmCopy).addSelectionListener(e -> {
            if (lockableDatFileReference.equals(View.DUMMY_DATFILE)) return;
            lockableDatFileReference.getVertexManager().addSnapshot();
            lockableDatFileReference.getVertexManager().copy();
        });
        mntmCopy.setText(I18n.COPYNPASTE_COPY);
        mntmCopy.setImage(ResourceManager.getImage("icon16_edit-copy.png")); //$NON-NLS-1$
        MenuItem mntmPaste = new MenuItem(mnuComposite, I18n.rightToLeftStyle());
        widgetUtil(mntmPaste).addSelectionListener(e -> {
            if (lockableDatFileReference.equals(View.DUMMY_DATFILE)) return;
            lockableDatFileReference.getVertexManager().addSnapshot();
            lockableDatFileReference.getVertexManager().paste(Editor3DWindow.getWindow().loadSelectorSettings());
            if (WorkbenchManager.getUserSettingState().isDisableMAD3D()) {
                Editor3DWindow.getWindow().setMovingAdjacentData(false);
                GuiStatusManager.updateStatus();
            }
        });
        mntmPaste.setText(I18n.COPYNPASTE_PASTE);
        mntmPaste.setImage(ResourceManager.getImage("icon16_edit-paste.png")); //$NON-NLS-1$
        MenuItem mntmDelete = new MenuItem(mnuComposite, I18n.rightToLeftStyle());
        widgetUtil(mntmDelete).addSelectionListener(e -> {
            if (lockableDatFileReference.equals(View.DUMMY_DATFILE)) return;
            lockableDatFileReference.getVertexManager().addSnapshot();
            lockableDatFileReference.getVertexManager().delete(Editor3DWindow.getWindow().isMovingAdjacentData(), true);
        });
        mntmDelete.setText(I18n.COPYNPASTE_DELETE);
        mntmDelete.setImage(ResourceManager.getImage("icon16_delete.png")); //$NON-NLS-1$

        new MenuItem(mnuComposite, SWT.SEPARATOR);

        MenuItem mntmGridSize = new MenuItem(mnuComposite, I18n.rightToLeftStyle());
        widgetUtil(mntmGridSize).addSelectionListener(e -> new ValueDialog(getShell(), I18n.C3D_SET_GRID_SIZE, I18n.getCurrentUnit()) {
            @Override
            public void initializeSpinner() {
                this.spnValuePtr[0].setMinimum(new BigDecimal("0")); //$NON-NLS-1$
                this.spnValuePtr[0].setMaximum(new BigDecimal("1000")); //$NON-NLS-1$
                this.spnValuePtr[0].setValue(new BigDecimal(gridScale * 50.0));
            }

            @Override
            public void applyValue() {
                gridScale = (float) (this.spnValuePtr[0].getValue().doubleValue() / 50.0);
                if (gridScale < 0.1f)
                    gridScale = 0.1f;
                perspective.calculateOriginData();
            }
        }.open());
        mntmGridSize.setText(I18n.C3D_GRID_SIZE);

        {
            // MARK CMenu Viewing Angles
            mnuViewAngles = new Menu(mntmViewAngles);
            mntmViewAngles.setMenu(mnuViewAngles);

            final MenuItem mntmFront = new MenuItem(mnuViewAngles, SWT.CHECK);
            this.mntmFrontPtr[0] = mntmFront;
            widgetUtil(mntmFront).addSelectionListener(e -> {
                WidgetSelectionHelper.unselectAllChildButtons(mnuViewAngles);
                ((MenuItem) e.widget).setSelection(true);
                perspective.setPerspective(Perspective.FRONT);
            });
            mntmFront.setImage(ResourceManager.getImage("icon16_front.png")); //$NON-NLS-1$
            mntmFront.setText(I18n.PERSPECTIVE_FRONT);
            mntmFront.setSelection(true);

            final MenuItem mntmBack = new MenuItem(mnuViewAngles, SWT.CHECK);
            this.mntmBackPtr[0] = mntmBack;
            widgetUtil(mntmBack).addSelectionListener(e -> {
                WidgetSelectionHelper.unselectAllChildButtons(mnuViewAngles);
                ((MenuItem) e.widget).setSelection(true);
                perspective.setPerspective(Perspective.BACK);
            });
            mntmBack.setImage(ResourceManager.getImage("icon16_back.png")); //$NON-NLS-1$
            mntmBack.setText(I18n.PERSPECTIVE_BACK);

            final MenuItem mntmLeft = new MenuItem(mnuViewAngles, SWT.CHECK);
            this.mntmLeftPtr[0] = mntmLeft;
            widgetUtil(mntmLeft).addSelectionListener(e -> {
                WidgetSelectionHelper.unselectAllChildButtons(mnuViewAngles);
                ((MenuItem) e.widget).setSelection(true);
                perspective.setPerspective(Perspective.LEFT);
            });
            mntmLeft.setImage(ResourceManager.getImage("icon16_left.png")); //$NON-NLS-1$
            mntmLeft.setText(I18n.PERSPECTIVE_LEFT);

            final MenuItem mntmRight = new MenuItem(mnuViewAngles, SWT.CHECK);
            this.mntmRightPtr[0] = mntmRight;
            widgetUtil(mntmRight).addSelectionListener(e -> {
                WidgetSelectionHelper.unselectAllChildButtons(mnuViewAngles);
                ((MenuItem) e.widget).setSelection(true);
                perspective.setPerspective(Perspective.RIGHT);
            });
            mntmRight.setImage(ResourceManager.getImage("icon16_right.png")); //$NON-NLS-1$
            mntmRight.setText(I18n.PERSPECTIVE_RIGHT);

            final MenuItem mntmTop = new MenuItem(mnuViewAngles, SWT.CHECK);
            this.mntmTopPtr[0] = mntmTop;
            widgetUtil(mntmTop).addSelectionListener(e -> {
                WidgetSelectionHelper.unselectAllChildButtons(mnuViewAngles);
                ((MenuItem) e.widget).setSelection(true);
                perspective.setPerspective(Perspective.TOP);
            });
            mntmTop.setImage(ResourceManager.getImage("icon16_top.png")); //$NON-NLS-1$
            mntmTop.setText(I18n.PERSPECTIVE_TOP);

            final MenuItem mntmBottom = new MenuItem(mnuViewAngles, SWT.CHECK);
            this.mntmBottomPtr[0] = mntmBottom;
            widgetUtil(mntmBottom).addSelectionListener(e -> {
                WidgetSelectionHelper.unselectAllChildButtons(mnuViewAngles);
                ((MenuItem) e.widget).setSelection(true);
                perspective.setPerspective(Perspective.BOTTOM);
            });
            mntmBottom.setImage(ResourceManager.getImage("icon16_bottom.png")); //$NON-NLS-1$
            mntmBottom.setText(I18n.PERSPECTIVE_BOTTOM);

            new MenuItem(mnuViewAngles, SWT.SEPARATOR);

            final MenuItem mntmTwoThirds = new MenuItem(mnuViewAngles, SWT.NONE);
            this.mntmTwoThirdsPtr[0] = mntmTwoThirds;
            widgetUtil(mntmTwoThirds).addSelectionListener(e -> {
                WidgetSelectionHelper.unselectAllChildButtons(mnuViewAngles);
                perspective.setPerspective(Perspective.TWO_THIRDS);
            });
            mntmTwoThirds.setImage(ResourceManager.getImage("icon16_twoThirds.png")); //$NON-NLS-1$
            mntmTwoThirds.setText(I18n.PERSPECTIVE_TWO_THIRDS);
        }

        {
            // MARK CMenu Manipulator
            mnuManipulator = new Menu(mntmManipulator);
            mntmManipulator.setMenu(mnuManipulator);
            {
                MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                btnMani.setText(I18n.E3D_MANIPULATOR_TO_ORIGIN);
                btnMani.setImage(ResourceManager.getImage("icon16_toOrigin.png")); //$NON-NLS-1$
                widgetUtil(btnMani).addSelectionListener(e -> Editor3DWindow.getWindow().mntmManipulatorToOrigin());
            }
            {
                MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                btnMani.setText(I18n.E3D_MANIPULATOR_TO_WORLD);
                btnMani.setImage(ResourceManager.getImage("icon16_toWorld.png")); //$NON-NLS-1$
                widgetUtil(btnMani).addSelectionListener(e -> Editor3DWindow.getWindow().mntmManipulatorToWorld());
            }
            {
                MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                btnMani.setText(I18n.E3D_ADJUST_ROTATION_CENTER);
                btnMani.setImage(ResourceManager.getImage("icon16_adjustrotationcenter.png")); //$NON-NLS-1$
                widgetUtil(btnMani).addSelectionListener(e -> Editor3DWindow.getWindow().mntmManipulatorAdjustRotationCenter());
            }

            new MenuItem(mnuManipulator, SWT.SEPARATOR);

            {
                MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                btnMani.setText(I18n.E3D_REVERSE_X);
                btnMani.setImage(ResourceManager.getImage("icon16_Xinv.png")); //$NON-NLS-1$
                widgetUtil(btnMani).addSelectionListener(e -> Editor3DWindow.getWindow().mntmManipulatorXReverse());
            }
            {
                MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                btnMani.setText(I18n.E3D_REVERSE_Y);
                btnMani.setImage(ResourceManager.getImage("icon16_Yinv.png")); //$NON-NLS-1$
                widgetUtil(btnMani).addSelectionListener(e -> Editor3DWindow.getWindow().mntmManipulatorYReverse());
            }
            {
                MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                btnMani.setText(I18n.E3D_REVERSE_Z);
                btnMani.setImage(ResourceManager.getImage("icon16_Zinv.png")); //$NON-NLS-1$
                widgetUtil(btnMani).addSelectionListener(e -> Editor3DWindow.getWindow().mntmManipulatorZReverse());
            }

            new MenuItem(mnuManipulator, SWT.SEPARATOR);

            {
                MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                btnMani.setText(I18n.E3D_SWAP_XY);
                btnMani.setImage(ResourceManager.getImage("icon16_XswapY.png")); //$NON-NLS-1$
                widgetUtil(btnMani).addSelectionListener(e -> Editor3DWindow.getWindow().mntmManipulatorSwitchXY());
            }
            {
                MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                btnMani.setText(I18n.E3D_SWAP_XZ);
                btnMani.setImage(ResourceManager.getImage("icon16_XswapZ.png")); //$NON-NLS-1$
                widgetUtil(btnMani).addSelectionListener(e -> Editor3DWindow.getWindow().mntmManipulatorSwitchXZ());
            }
            {
                MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                btnMani.setText(I18n.E3D_SWAP_YZ);
                btnMani.setImage(ResourceManager.getImage("icon16_YswapZ.png")); //$NON-NLS-1$
                widgetUtil(btnMani).addSelectionListener(e -> Editor3DWindow.getWindow().mntmManipulatorSwitchYZ());
            }

            new MenuItem(mnuManipulator, SWT.SEPARATOR);

            {
                MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                btnMani.setText(I18n.E3D_CAM_TO_MANIPULATOR);
                btnMani.setImage(ResourceManager.getImage("icon16_cameratomanipulator.png")); //$NON-NLS-1$
                widgetUtil(btnMani).addSelectionListener(e -> Editor3DWindow.getWindow().mntmManipulatorCameraToPos());
            }
            {
                MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                btnMani.setText(I18n.E3D_MANIPULATOR_TO_AVG);
                btnMani.setImage(ResourceManager.getImage("icon16_toavg.png")); //$NON-NLS-1$
                KeyStateManager.addKeyText(btnMani, I18n.E3D_MANIPULATOR_TO_AVG, Task.MOVE_TO_AVG);
                widgetUtil(btnMani).addSelectionListener(e -> Editor3DWindow.getWindow().mntmManipulatorToAverage());
            }

            new MenuItem(mnuManipulator, SWT.SEPARATOR);

            {
                MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                btnMani.setText(I18n.E3D_MANIPULATOR_TO_SUBFILE);
                btnMani.setImage(ResourceManager.getImage("icon16_tosubfile.png")); //$NON-NLS-1$
                widgetUtil(btnMani).addSelectionListener(e -> Editor3DWindow.getWindow().mntmManipulatorToSubfile());
            }
            {
                MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                btnMani.setText(Cocoa.replaceCtrlByCmd(I18n.E3D_SUBFILE_TO_MANIPULATOR));
                btnMani.setImage(ResourceManager.getImage("icon16_tosubfile.png")); //$NON-NLS-1$
                widgetUtil(btnMani).addSelectionListener(e -> Editor3DWindow.getWindow().mntmManipulatorSubfileTo(Cocoa.checkCtrlOrCmdPressed(e.stateMask)));
            }

            new MenuItem(mnuManipulator, SWT.SEPARATOR);

            {
                MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                btnMani.setText(I18n.E3D_MANIPULATOR_TO_VERTEX);
                btnMani.setImage(ResourceManager.getImage("icon16_tonearestvertex.png")); //$NON-NLS-1$
                widgetUtil(btnMani).addSelectionListener(e -> Editor3DWindow.getWindow().mntmManipulatorToVertex());
            }
            {
                MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                btnMani.setText(I18n.E3D_MANIPULATOR_TO_EDGE);
                btnMani.setImage(ResourceManager.getImage("icon16_tonearestedge.png")); //$NON-NLS-1$
                widgetUtil(btnMani).addSelectionListener(e -> Editor3DWindow.getWindow().mntmManipulatorToEdge());
            }
            {
                MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                btnMani.setText(I18n.E3D_MANIPULATOR_TO_FACE);
                btnMani.setImage(ResourceManager.getImage("icon16_tonearestface.png")); //$NON-NLS-1$
                widgetUtil(btnMani).addSelectionListener(e -> Editor3DWindow.getWindow().mntmManipulatorToSurface());
            }

            new MenuItem(mnuManipulator, SWT.SEPARATOR);

            {
                MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                btnMani.setText(I18n.E3D_MANIPULATOR_TO_VERTEX_N);
                btnMani.setImage(ResourceManager.getImage("icon16_tonearestvertexN.png")); //$NON-NLS-1$
                widgetUtil(btnMani).addSelectionListener(e -> Editor3DWindow.getWindow().mntmManipulatorToVertexNormal());
            }
            {
                MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                btnMani.setText(I18n.E3D_MANIPULATOR_TO_VERTEX_P);
                btnMani.setImage(ResourceManager.getImage("icon16_tonearestvertexN2.png")); //$NON-NLS-1$
                widgetUtil(btnMani).addSelectionListener(e -> Editor3DWindow.getWindow().mntmManipulatorToVertexPosition());
            }
            {
                MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                btnMani.setText(I18n.E3D_MANIPULATOR_TO_EDGE_N);
                btnMani.setImage(ResourceManager.getImage("icon16_tonearestedgeN.png")); //$NON-NLS-1$
                widgetUtil(btnMani).addSelectionListener(e -> Editor3DWindow.getWindow().mntmManipulatorToEdgeNormal());
            }
            {
                MenuItem btnMani = new MenuItem(mnuManipulator, SWT.PUSH);
                btnMani.setText(I18n.E3D_MANIPULATOR_TO_FACE_N);
                btnMani.setImage(ResourceManager.getImage("icon16_tonearestfaceN.png")); //$NON-NLS-1$
                widgetUtil(btnMani).addSelectionListener(e -> Editor3DWindow.getWindow().mntmManipulatorToSurfaceNormal());
            }
        }

        {
            // MARK CMenu View Actions
            final Menu mnuViewActions = new Menu(mntmViewAngles);
            mntmViewActions.setMenu(mnuViewActions);

            final MenuItem mntmShowOrigin = new MenuItem(mnuViewActions, SWT.CHECK);
            this.mntmShowOriginPtr[0] = mntmShowOrigin;
            widgetUtil(mntmShowOrigin).addSelectionListener(e -> c3dModifier.showOrigin(mntmShowOrigin.getSelection()));
            mntmShowOrigin.setText(I18n.E3D_ORIGIN);
            mntmShowOrigin.setSelection(true);

            final MenuItem mntmSwitchLights = new MenuItem(mnuViewActions, SWT.CHECK);
            this.mntmSwitchLightsPtr[0] = mntmSwitchLights;
            widgetUtil(mntmSwitchLights).addSelectionListener(e -> c3dModifier.switchLigths(mntmSwitchLights.getSelection()));
            mntmSwitchLights.setText(I18n.C3D_LIGHTS);
            mntmSwitchLights.setSelection(true);

            final MenuItem mntmMeshLines = new MenuItem(mnuViewActions, SWT.CHECK);
            this.mntmMeshLinesPtr[0] = mntmMeshLines;
            widgetUtil(mntmMeshLines).addSelectionListener(e -> c3dModifier.switchMeshLines(mntmMeshLines.getSelection()));
            mntmMeshLines.setText(I18n.C3D_MESH_LINES);
            mntmMeshLines.setSelection(true);

            final MenuItem mntmSubMeshLines = new MenuItem(mnuViewActions, SWT.CHECK);
            this.mntmSubMeshLinesPtr[0] = mntmSubMeshLines;
            widgetUtil(mntmSubMeshLines).addSelectionListener(e -> c3dModifier.switchSubMeshLines(mntmSubMeshLines.getSelection()));
            mntmSubMeshLines.setText(I18n.C3D_SUBFILE_MESH_LINES);
            mntmSubMeshLines.setSelection(false);

            final MenuItem mntmVertices = new MenuItem(mnuViewActions, SWT.CHECK);
            this.mntmVerticesPtr[0] = mntmVertices;
            widgetUtil(mntmVertices).addSelectionListener(e -> c3dModifier.switchVertices(mntmVertices.getSelection()));
            mntmVertices.setText(I18n.C3D_VERTICES);
            mntmVertices.setSelection(true);

            final MenuItem mntmHiddenVertices = new MenuItem(mnuViewActions, SWT.CHECK);
            this.mntmHiddenVerticesPtr[0] = mntmHiddenVertices;
            widgetUtil(mntmHiddenVertices).addSelectionListener(e -> c3dModifier.switchHiddenVertices(mntmHiddenVertices.getSelection()));
            mntmHiddenVertices.setText(I18n.C3D_HIDDEN_VERTICES);
            mntmHiddenVertices.setSelection(false);

            final MenuItem mntmControlPointVertices = new MenuItem(mnuViewActions, SWT.CHECK);
            this.mntmControlPointVerticesPtr[0] = mntmControlPointVertices;
            widgetUtil(mntmControlPointVertices).addSelectionListener(e -> c3dModifier.switchCondlineControlPoints(mntmControlPointVertices.getSelection()));
            mntmControlPointVertices.setText(I18n.C3D_CONDLINE_VERTICES);

            if (WorkbenchManager.getUserSettingState().getOpenGLVersion() >= 33) {
                final MenuItem mntmSmoothShading = new MenuItem(mnuViewActions, SWT.CHECK);
                this.mntmSmoothShadingPtr[0] = mntmSmoothShading;
                widgetUtil(mntmSmoothShading).addSelectionListener(e -> c3dModifier.switchSmoothShading(mntmSmoothShading.getSelection()));
                mntmSmoothShading.setText(I18n.C3D_SMOOTH_SHADING);
                mntmSmoothShading.setSelection(false);
            }

            final MenuItem mntmStudLogo = new MenuItem(mnuViewActions, SWT.CHECK);
            this.mntmStudLogoPtr[0] = mntmStudLogo;
            widgetUtil(mntmStudLogo).addSelectionListener(e -> c3dModifier.switchShowingLogo(mntmStudLogo.getSelection()));
            mntmStudLogo.setText(I18n.C3D_STUD_LOGO);
            mntmStudLogo.setSelection(false);

            final MenuItem mntmLDrawLines = new MenuItem(mnuViewActions, SWT.CASCADE);
            mntmLDrawLines.setText(I18n.C3D_LDRAW_LINES);

            final MenuItem mntmShowGrid = new MenuItem(mnuViewActions, SWT.CHECK);
            this.mntmShowGridPtr[0] = mntmShowGrid;
            widgetUtil(mntmShowGrid).addSelectionListener(e -> c3dModifier.showGrid(mntmShowGrid.getSelection()));
            KeyStateManager.addKeyText(mntmShowGrid, I18n.E3D_GRID, Task.SHOW_GRID);
            mntmShowGrid.setSelection(true);

            final MenuItem mntmShowScale = new MenuItem(mnuViewActions, SWT.CHECK);
            this.mntmShowScalePtr[0] = mntmShowScale;
            widgetUtil(mntmShowScale).addSelectionListener(e -> {
                c3dModifier.showScale(mntmShowScale.getSelection());
                getCanvas().forceFocus();
            });
            KeyStateManager.addKeyText(mntmShowScale, I18n.E3D_RULER, Task.SHOW_RULER);

            final MenuItem mntmAxis = new MenuItem(mnuViewActions, SWT.CHECK);
            this.mntmAxisPtr[0] = mntmAxis;
            widgetUtil(mntmAxis).addSelectionListener(e -> c3dModifier.switchAxis(mntmAxis.getSelection()));
            mntmAxis.setText(I18n.C3D_XYZ_AXIS);
            mntmAxis.setSelection(true);

            final MenuItem mntmLabel = new MenuItem(mnuViewActions, SWT.CHECK);
            this.mntmLabelPtr[0] = mntmLabel;
            widgetUtil(mntmLabel).addSelectionListener(e -> c3dModifier.switchLabel(mntmLabel.getSelection()));
            mntmLabel.setText(I18n.C3D_PERSPECTIVE_LABEL);
            mntmLabel.setSelection(true);

            final MenuItem mntmAnaglyph = new MenuItem(mnuViewActions, SWT.CHECK);
            this.mntmAnaglyphPtr[0] = mntmAnaglyph;
            widgetUtil(mntmAnaglyph).addSelectionListener(e -> c3dModifier.switchAnaglyph3D(mntmAnaglyph.getSelection()));
            mntmAnaglyph.setText(I18n.C3D_ANAGLYPH_3D);
            mntmAnaglyph.setSelection(false);

            new MenuItem(mnuViewActions, SWT.SEPARATOR);

            MenuItem mntmSplitHorizontally = new MenuItem(mnuViewActions, SWT.NONE);
            widgetUtil(mntmSplitHorizontally).addSelectionListener(e -> c3dModifier.splitViewHorizontally());
            mntmSplitHorizontally.setText(I18n.E3D_SPLIT_HORIZONTALLY);

            MenuItem mntmSplitVertically = new MenuItem(mnuViewActions, SWT.NONE);
            widgetUtil(mntmSplitVertically).addSelectionListener(e -> c3dModifier.splitViewVertically());
            mntmSplitVertically.setText(I18n.E3D_SPLIT_VERTICALLY);

            new MenuItem(mnuViewActions, SWT.SEPARATOR);

            MenuItem mntmRotateView = new MenuItem(mnuViewActions, SWT.NONE);
            widgetUtil(mntmRotateView).addSelectionListener(e -> c3dModifier.spinView());
            mntmRotateView.setText(I18n.E3D_ROTATE_CLOCKWISE);

            new MenuItem(mnuViewActions, SWT.SEPARATOR);

            MenuItem mntmClose = new MenuItem(mnuViewActions, SWT.NONE);
            widgetUtil(mntmClose).addSelectionListener(e -> c3dModifier.closeView());
            mntmClose.setText(I18n.E3D_CLOSE_VIEW);

            {
                // MARK CMenu LineMode
                final MenuItem[] mntmAlwaysBlack = new MenuItem[1];
                mnuLineMode = new Menu(mntmLDrawLines);
                mntmLDrawLines.setMenu(mnuLineMode);

                final MenuItem mntmRealPreview = new MenuItem(mnuLineMode, SWT.CHECK);
                this.mntmRealPreviewPtr[0] = mntmRealPreview;
                widgetUtil(mntmRealPreview).addSelectionListener(e -> {
                    WidgetSelectionHelper.unselectAllChildButtons(mnuLineMode);
                    ((MenuItem) e.widget).setSelection(true);
                    mntmAlwaysBlack[0].setSelection(isBlackEdges());
                    c3dModifier.setLineMode(0);
                });
                mntmRealPreview.setText(I18n.C3D_REAL_PREVIEW);
                mntmRealPreview.setSelection(true);

                final MenuItem mntmShowAll = new MenuItem(mnuLineMode, SWT.CHECK);
                this.mntmShowAllPtr[0] = mntmShowAll;
                widgetUtil(mntmShowAll).addSelectionListener(e -> {
                    WidgetSelectionHelper.unselectAllChildButtons(mnuLineMode);
                    ((MenuItem) e.widget).setSelection(true);
                    mntmAlwaysBlack[0].setSelection(isBlackEdges());
                    c3dModifier.setLineMode(1);
                });
                mntmShowAll.setText(I18n.C3D_SHOW_ALL);

                final MenuItem mntmStdLines = new MenuItem(mnuLineMode, SWT.CHECK);
                this.mntmStdLinesPtr[0] = mntmStdLines;
                widgetUtil(mntmStdLines).addSelectionListener(e -> {
                    WidgetSelectionHelper.unselectAllChildButtons(mnuLineMode);
                    ((MenuItem) e.widget).setSelection(true);
                    mntmAlwaysBlack[0].setSelection(isBlackEdges());
                    c3dModifier.setLineMode(2);
                });
                mntmStdLines.setText(I18n.C3D_SHOW_EDGES);

                final MenuItem mntmHideAll = new MenuItem(mnuLineMode, SWT.CHECK);
                this.mntmHideAllPtr[0] = mntmHideAll;
                widgetUtil(mntmHideAll).addSelectionListener(e -> {
                    WidgetSelectionHelper.unselectAllChildButtons(mnuLineMode);
                    ((MenuItem) e.widget).setSelection(true);
                    mntmAlwaysBlack[0].setSelection(isBlackEdges());
                    c3dModifier.setLineMode(4);
                });
                mntmHideAll.setText(I18n.C3D_HIDE_ALL);


                new MenuItem(mnuLineMode, SWT.SEPARATOR);

                mntmAlwaysBlack[0] = new MenuItem(mnuLineMode, SWT.CHECK);
                widgetUtil(mntmAlwaysBlack[0]).addSelectionListener(e -> setBlackEdges(mntmAlwaysBlack[0].getSelection()));
                mntmAlwaysBlack[0].setText(I18n.C3D_USE_ALWAYS_BLACK_LINES);
                this.mntmAlwaysBlackPtr[0] = mntmAlwaysBlack[0];
            }

        }

        {
            // MARK CMenu RenderMode
            mnuRenderMode = new Menu(mntmRenderModes);
            mntmRenderModes.setMenu(mnuRenderMode);

            final MenuItem mntmNoBFC = new MenuItem(mnuRenderMode, SWT.CHECK);
            this.mntmNoBFCPtr[0] = mntmNoBFC;
            widgetUtil(mntmNoBFC).addSelectionListener(e -> {
                WidgetSelectionHelper.unselectAllChildButtons(mnuRenderMode);
                ((MenuItem) e.widget).setSelection(true);
                c3dModifier.setRenderMode(0);
                getRenderer().disposeAllTextures();
            });
            mntmNoBFC.setText(I18n.C3D_NO_BACKFACE_CULLING);
            mntmNoBFC.setSelection(true);

            final MenuItem mntmRandomColours = new MenuItem(mnuRenderMode, SWT.CHECK);
            this.mntmRandomColoursPtr[0] = mntmRandomColours;
            widgetUtil(mntmRandomColours).addSelectionListener(e -> {
                WidgetSelectionHelper.unselectAllChildButtons(mnuRenderMode);
                ((MenuItem) e.widget).setSelection(true);
                c3dModifier.setRenderMode(1);
                getRenderer().disposeAllTextures();
            });
            mntmRandomColours.setText(I18n.C3D_RANDOM_COLOURS);

            final MenuItem mntmBFCFrontBack = new MenuItem(mnuRenderMode, SWT.CHECK);
            this.mntmBFCFrontBackPtr[0] = mntmBFCFrontBack;
            widgetUtil(mntmBFCFrontBack).addSelectionListener(e -> {
                WidgetSelectionHelper.unselectAllChildButtons(mnuRenderMode);
                ((MenuItem) e.widget).setSelection(true);
                c3dModifier.setRenderMode(2);
                getRenderer().disposeAllTextures();
            });
            mntmBFCFrontBack.setText(I18n.C3D_GREEN_RED);

            final MenuItem mntmBFCBack = new MenuItem(mnuRenderMode, SWT.CHECK);
            this.mntmBFCBackPtr[0] = mntmBFCBack;
            widgetUtil(mntmBFCBack).addSelectionListener(e -> {
                WidgetSelectionHelper.unselectAllChildButtons(mnuRenderMode);
                ((MenuItem) e.widget).setSelection(true);
                c3dModifier.setRenderMode(3);
                getRenderer().disposeAllTextures();
            });
            mntmBFCBack.setText(I18n.C3D_RED_BACKFACES);

            final MenuItem mntmBFCReal = new MenuItem(mnuRenderMode, SWT.CHECK);
            this.mntmBFCRealPtr[0] = mntmBFCReal;
            widgetUtil(mntmBFCReal).addSelectionListener(e -> {
                WidgetSelectionHelper.unselectAllChildButtons(mnuRenderMode);
                ((MenuItem) e.widget).setSelection(true);
                c3dModifier.setRenderMode(4);
                getRenderer().disposeAllTextures();
            });
            mntmBFCReal.setText(I18n.C3D_REAL_BACKFACE_CULLING);

            final MenuItem mntmBFCTextured = new MenuItem(mnuRenderMode, SWT.CHECK);
            this.mntmBFCTexturedPtr[0] = mntmBFCTextured;
            widgetUtil(mntmBFCTextured).addSelectionListener(e -> {

                if (!WorkbenchManager.getUserSettingState().isBfcCertificationRequiredForLDrawMode()) {
                    MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
                    messageBox.setText(I18n.DIALOG_INFO);
                    messageBox.setMessage(I18n.C3D_PREVIEW_NOTE);
                    messageBox.open();
                    WorkbenchManager.getUserSettingState().setBfcCertificationRequiredForLDrawMode(true);
                }

                WidgetSelectionHelper.unselectAllChildButtons(mnuRenderMode);
                ((MenuItem) e.widget).setSelection(true);
                c3dModifier.setRenderMode(5);
            });
            mntmBFCTextured.setText(I18n.C3D_LDRAW_STANDARD);

            new MenuItem(mnuRenderMode, SWT.SEPARATOR);

            final MenuItem mntmCondlineMode = new MenuItem(mnuRenderMode, SWT.CHECK);
            this.mntmCondlineModePtr[0] = mntmCondlineMode;
            widgetUtil(mntmCondlineMode).addSelectionListener(e -> {
                WidgetSelectionHelper.unselectAllChildButtons(mnuRenderMode);
                ((MenuItem) e.widget).setSelection(true);
                c3dModifier.setRenderMode(6);
                getRenderer().disposeAllTextures();
            });
            mntmCondlineMode.setText(I18n.C3D_CONDLINE_MODE);

            final MenuItem mntmCoplanarityHeatmapMode = new MenuItem(mnuRenderMode, SWT.CHECK);
            this.mntmCoplanarityHeatmapModePtr[0] = mntmCoplanarityHeatmapMode;
            widgetUtil(mntmCoplanarityHeatmapMode).addSelectionListener(e -> {
                WidgetSelectionHelper.unselectAllChildButtons(mnuRenderMode);
                ((MenuItem) e.widget).setSelection(true);
                c3dModifier.setRenderMode(7);
                getRenderer().disposeAllTextures();
            });
            mntmCoplanarityHeatmapMode.setText(I18n.C3D_COPLANARITY_MODE);

            final MenuItem mntmWireframeMode = new MenuItem(mnuRenderMode, SWT.CHECK);
            this.mntmWireframeModePtr[0] = mntmWireframeMode;
            widgetUtil(mntmWireframeMode).addSelectionListener(e -> {
                WidgetSelectionHelper.unselectAllChildButtons(mnuRenderMode);
                ((MenuItem) e.widget).setSelection(true);
                c3dModifier.setRenderMode(-1);
                getRenderer().disposeAllTextures();
            });
            mntmWireframeMode.setText(I18n.C3D_WIREFRAME);
        }

        {
            // MARK CMenu Synchronise
            mnuSyncronise = new Menu(mntmSynchronise);
            mntmSynchronise.setMenu(mnuSyncronise);

            final MenuItem mntmSyncroniseManipulator = new MenuItem(mnuSyncronise, SWT.CHECK);
            this.mntmSyncManipulatorPtr[0] = mntmSyncroniseManipulator;
            widgetUtil(mntmSyncroniseManipulator).addSelectionListener(e -> {
                setSyncManipulator(mntmSyncManipulatorPtr[0].getSelection());
                for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                    Composite3D c3d = renderer.getC3D();
                    if (lockableDatFileReference.equals(c3d.getLockableDatFileReference())) {
                        c3d.setSyncManipulator(isSyncManipulator());
                    }
                }
            });
            mntmSyncroniseManipulator.setText(I18n.E3D_SYNC_MANIPULATOR);

            final MenuItem mntmSyncTranslation = new MenuItem(mnuSyncronise, SWT.CHECK);
            this.mntmSyncTranslatePtr[0] = mntmSyncTranslation;
            widgetUtil(mntmSyncTranslation).addSelectionListener(e -> {
                setSyncTranslation(mntmSyncTranslatePtr[0].getSelection());
                for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                    Composite3D c3d = renderer.getC3D();
                    if (lockableDatFileReference.equals(c3d.getLockableDatFileReference())) {
                        c3d.setSyncTranslation(isSyncTranslation());
                    }
                }
            });
            mntmSyncTranslation.setText(I18n.E3D_SYNC_TRANSLATION);

            final MenuItem mntmSyncroniseZoom = new MenuItem(mnuSyncronise, SWT.CHECK);
            this.mntmSyncZoomPtr[0] = mntmSyncroniseZoom;
            widgetUtil(mntmSyncroniseZoom).addSelectionListener(e -> {
                setSyncZoom(mntmSyncZoomPtr[0].getSelection());
                for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                    Composite3D c3d = renderer.getC3D();
                    if (lockableDatFileReference.equals(c3d.getLockableDatFileReference())) {
                        c3d.setSyncZoom(isSyncZoom());
                    }
                }
            });
            mntmSyncroniseZoom.setText(I18n.E3D_SYNC_ZOOM);
        }

        GL.setCapabilities(capabilities);
        canvas.addDisposeListener(e -> canvas.getCursor().dispose());

        // MARK Resize
        canvas.addListener(SWT.Resize, event -> {
            canvas.setCurrent();
            GL.setCapabilities(capabilities);
            perspective.initializeViewportPerspective();
            ViewIdleManager.pause[0].compareAndSet(false, true);
            VertexWindow.placeVertexWindow();
        });

        canvas.addListener(SWT.MouseDown, mouse::mouseDown);
        canvas.addListener(SWT.MouseMove, mouse::mouseMove);
        canvas.addListener(SWT.MouseUp, mouse::mouseUp);
        canvas.addListener(SWT.MouseEnter, event -> setHasMouse(true));
        canvas.addListener(SWT.MouseExit, event -> setHasMouse(false));

        canvas.addListener(SWT.MouseVerticalWheel, event -> {
            Project.setFileToEdit(lockableDatFileReference);
            final Composite3D c3d = getComposite3D();
            canvas.forceFocus();
            lockableDatFileReference.setLastSelectedComposite(c3d);
            sb1.setSelection(0);
            sb2.setSelection(0);
            if (event.count < 0 ^ WorkbenchManager.getUserSettingState().isInvertingWheelZoomDirection())
                perspective.zoomIn();
            else
                perspective.zoomOut();

            ViewIdleManager.refresh(c3d.getCanvas(), c3d.getRenderer());
        });

        new Win32MouseWheelFilter(canvas.getDisplay());

        canvas.addListener(SWT.MouseDoubleClick, event -> {
            Project.setFileToEdit(lockableDatFileReference);
            mouse.mouseDoubleClick(event);
        });

        canvas.addListener(SWT.KeyDown, event -> {
            final Composite3D c3d = getComposite3D();
            ViewIdleManager.refresh(c3d.getCanvas(), c3d.getRenderer());
            keyboard.setStates(event.keyCode, SWT.KeyDown, event);
        });

        canvas.addListener(SWT.KeyUp, event -> {
            final Composite3D c3d = getComposite3D();
            ViewIdleManager.refresh(c3d.getCanvas(), c3d.getRenderer());
            keyboard.setStates(event.keyCode, SWT.KeyUp, event);
        });

        canvas.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                Project.setFileToEdit(lockableDatFileReference);
            }
        });

        canvas.addMenuDetectListener(e -> {
            if (e.detail != SWT.MENU_KEYBOARD) {
                return;
            }
            java.awt.Point b = java.awt.MouseInfo.getPointerInfo().getLocation();
            final int x = (int) b.getX();
            final int y = (int) b.getY();

            Menu tmpMenu = getMenu();
            if (!tmpMenu.isDisposed()) {
                tmpMenu.setLocation(x, y);
                tmpMenu.setVisible(true);
            }
        });

        Transfer[] types = new Transfer[] { PrimitiveDragAndDropTransfer.getInstance(), FileTransfer.getInstance() };
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
                if (newMouseMove.compareAndSet(true, false)) Display.getCurrent().asyncExec(() -> {
                    try {
                        mouse.mouseMove(ev);
                        newMouseMove.set(true);
                    } catch (SWTException swtEx) { /* consumed */ }
                });
            }

            @Override
            public void dragLeave(DropTargetEvent event) {
                setDraggedPrimitive(null);
            }

            @Override
            public void drop(DropTargetEvent event) {
                String[] fileList = null;
                FileTransfer ft = FileTransfer.getInstance();
                if (ft.isSupportedType(event.currentDataType)) {
                    fileList = (String[]) event.data;
                    if (fileList != null) {
                        for (String f : fileList) {
                            NLogger.debug(getClass(), f);
                            if (f.toLowerCase(Locale.ENGLISH).endsWith(".dat")) { //$NON-NLS-1$
                                final File fileToOpen = new File(f);
                                if (fileToOpen.exists() && !fileToOpen.isDirectory()) {
                                    DatFile df = Editor3DWindow.getWindow().openDatFile(OpenInWhat.EDITOR_3D, f, true);
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
                    }
                } else {
                    NLogger.debug(getClass(), "Primitive dropped at: {0} | {1}", event.x, event.y); //$NON-NLS-1$
                    final Editor3DWindow window = Editor3DWindow.getWindow();
                    final org.nschmidt.ldparteditor.data.Primitive p = window.getCompositePrimitive().getSelectedPrimitive();
                    final DatFile datfile = getLockableDatFileReference();
                    if (p == null || p.isCategory() || datfile.isReadOnly()) return;
                    NLogger.debug(getClass(), "Primitive: {0}", p); //$NON-NLS-1$
                    String ref = p.getName();
                    final BigDecimal[] cur = getCursorSnapped3Dprecise();
                    final GColour col16 = LDConfig.getColour16();
                    Set<String> alreadyParsed = new HashSet<>();
                    alreadyParsed.add(datfile.getShortName());
                    List<ParsingResult> subfileLine = DatParser
                            .parseLine(
                                    "1 16 " + MathHelper.bigDecimalToString(cur[0]) + " " + MathHelper.bigDecimalToString(cur[1]) + " " + MathHelper.bigDecimalToString(cur[2]) + " 1 0 0 0 1 0 0 0 1 " + ref, -1, 0, col16.getR(), col16.getG(), col16.getB(), 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, datfile, false, alreadyParsed); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    GData1 primitiveLine = (GData1) subfileLine.get(0).getGraphicalData();
                    if (primitiveLine != null) {
                        datfile.addToTailOrInsertAfterCursor(primitiveLine);
                        datfile.getVertexManager().setModified(true, true);
                        if (!Project.getUnsavedFiles().contains(datfile)) {
                            Project.addUnsavedFile(datfile);
                            Editor3DWindow.getWindow().updateTreeUnsavedEntries();
                        }
                        datfile.getVertexManager().validateState();
                    }
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
        return mnuComposite;
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
        return viewportTranslation;
    }

    /**
     * @return The rotation matrix of the view
     */
    public Matrix4f getRotation() {
        return viewportRotation;
    }

    /**
     * @return The generator array of the view
     */
    public Vector4f[] getGenerator() {
        return viewportGenerator;
    }

    /**
     * @return the {@linkplain Perspective}-Index of the viewport
     */
    public Perspective getPerspectiveIndex() {
        return viewportPerspective;
    }

    /**
     * Sets the {@linkplain Perspective}-Index of the viewport
     *
     * @param perspective
     *            the {@linkplain Perspective}-Index
     */
    public void setPerspectiveIndex(Perspective perspective) {
        this.viewportPerspective = perspective;
    }

    /**
     * @return the origin-axis of the viewport
     */
    public Vector3f[] getViewportOriginAxis() {
        return viewportOriginAxis;
    }

    /**
     * @return The transformation matrix of the viewport which was last drawn
     */
    public Matrix4f getViewport() {
        return viewportMatrix;
    }

    public Matrix4f getViewportInverse() {
        return viewportMatrixInv;
    }

    /**
     * Sets the transformation matrix of the viewport
     *
     * @param matrix
     *            the matrix to set.
     */
    public void setViewport(Matrix4f matrix) {
        GData.CACHE_viewByProjection.clear();
        viewportMatrix.load(matrix);
        viewportMatrixInv = (Matrix4f) matrix.invert();
    }

    /**
     * @return the viewport z-Near value
     */
    public double getzNear() {
        return zNear;
    }

    /**
     * @return the viewport z-Far value
     */
    public double getzFar() {
        return zFar;
    }

    /**
     * @return the resolution of the viewport in pixel per LDU
     */
    public float getViewportPixelPerLDU() {
        return viewportPixelPerLDU;
    }

    /**
     * @param set
     *            the resolution of the viewport in pixel per LDU
     */
    public void setViewportPixelPerLDU(float viewportPixelPerLDU) {
        this.viewportPixelPerLDU = viewportPixelPerLDU;
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
        return c3dModifier;
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
        return mnuViewAngles;
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
        return gridData;
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
    public float getGridScale() {
        return gridScale;
    }

    /**
     * Sets the grid size scaling factor
     */
    public void setGridScale(float scale) {
        this.gridScale = scale;
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
        return mntmNoBFCPtr[0];
    }

    public MenuItem getMntmRandomColours() {
        return mntmRandomColoursPtr[0];
    }

    public MenuItem getMntmBFCFrontBack() {
        return mntmBFCFrontBackPtr[0];
    }

    public MenuItem getMntmBFCBack() {
        return mntmBFCBackPtr[0];
    }

    public MenuItem getMntmBFCReal() {
        return mntmBFCRealPtr[0];
    }

    public MenuItem getMntmBFCTextured() {
        return mntmBFCTexturedPtr[0];
    }

    public MenuItem getMntmCondlineMode() {
        return mntmCondlineModePtr[0];
    }

    public MenuItem getMntmCoplanarityHeatmapMode() {
        return mntmCoplanarityHeatmapModePtr[0];
    }

    public MenuItem getMntmWireframeMode() {
        return mntmWireframeModePtr[0];
    }

    public MenuItem getMntmAnaglyph() {
        return mntmAnaglyphPtr[0];
    }

    public MenuItem getMntmAxis() {
        return mntmAxisPtr[0];
    }

    public MenuItem getMntmAlwaysBlack() {
        return mntmAlwaysBlackPtr[0];
    }

    public MenuItem getMntmHideAll() {
        return mntmHideAllPtr[0];
    }

    public MenuItem getMntmStdLines() {
        return mntmStdLinesPtr[0];
    }

    public MenuItem getMntmShowAll() {
        return mntmShowAllPtr[0];
    }

    public MenuItem getMntmStudLogo() {
        return mntmStudLogoPtr[0];
    }

    public MenuItem getMntmSmoothShading() {
        return mntmSmoothShadingPtr[0];
    }

    public MenuItem getMntmControlPointVertices() {
        return mntmControlPointVerticesPtr[0];
    }

    public MenuItem getMntmHiddenVertices() {
        return mntmHiddenVerticesPtr[0];
    }

    public MenuItem getMntmVertices() {
        return mntmVerticesPtr[0];
    }

    public MenuItem getMntmSubMeshLines() {
        return mntmSubMeshLinesPtr[0];
    }

    public MenuItem getMntmMeshLines() {
        return mntmMeshLinesPtr[0];
    }

    public MenuItem getMntmSwitchLights() {
        return mntmSwitchLightsPtr[0];
    }

    public MenuItem getMntmShowOrigin() {
        return mntmShowOriginPtr[0];
    }

    public MenuItem getMntmTwoThirds() {
        return mntmTwoThirdsPtr[0];
    }

    public MenuItem getMntmBottom() {
        return mntmBottomPtr[0];
    }

    public MenuItem getMntmTop() {
        return mntmTopPtr[0];
    }

    public MenuItem getMntmRight() {
        return mntmRightPtr[0];
    }

    public MenuItem getMntmLeft() {
        return mntmLeftPtr[0];
    }

    public MenuItem getMntmBack() {
        return mntmBackPtr[0];
    }

    public MenuItem getMntmFront() {
        return mntmFrontPtr[0];
    }

    public MenuItem getMntmShowGrid() {
        return mntmShowGridPtr[0];
    }

    public MenuItem getMntmShowScale() {
        return mntmShowScalePtr[0];
    }

    public MenuItem getMntmLabel() {
        return mntmLabelPtr[0];
    }

    public MenuItem getMntmRealPreview() {
        return mntmRealPreviewPtr[0];
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
        mntmSyncManipulatorPtr[0].setSelection(syncManipulator);
    }

    public boolean isSyncTranslation() {
        return syncTranslation;
    }

    public void setSyncTranslation(boolean syncTranslation) {
        this.syncTranslation = syncTranslation;
        mntmSyncTranslatePtr[0].setSelection(syncTranslation);
    }

    public boolean isSyncZoom() {
        return syncZoom;
    }

    public void setSyncZoom(boolean syncZoom) {
        this.syncZoom = syncZoom;
        mntmSyncZoomPtr[0].setSelection(syncZoom);
    }

    public Set<Vertex> getTmpHiddenVertices() {
        return tmpHiddenVertices;
    }

    private void joinSelectionInTextEditor(final DatFile df) {
        if (df.equals(View.DUMMY_DATFILE) || df.isReadOnly()) return;
        final VertexManager vm = df.getVertexManager();
        if (!vm.getSelectedData().isEmpty()) {
            Display.getCurrent().asyncExec(() -> {

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

                Display.getDefault().asyncExec(() -> {
                    int minLine = Integer.MAX_VALUE;
                    final HashBiMap<Integer, GData> dpl = df.getDrawPerLineNoClone();
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
                });
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

                        Display.getDefault().asyncExec(() -> {
                            final VertexManager vm = df.getVertexManager();
                            if (!vm.getSelectedData().isEmpty() || !vm.getSelectedVertices().isEmpty()) {

                                final int oldIndex = ((CompositeTab) t).getTextComposite().getTopIndex() + 1;
                                final int lastSetIndex = ((CompositeTab) t).getState().getOldLineIndex();
                                final List<Integer> indices = new ArrayList<>();
                                final Set<GData> selection = new HashSet<>();
                                Integer index;

                                selection.addAll(vm.getSelectedData());
                                selection.addAll(vm.getSelectedSubfiles());

                                for (Vertex v : vm.getSelectedVertices()) {
                                    selection.addAll(vm.getLinkedVertexMetaCommands(v));
                                }

                                for (GData g : selection) {
                                    index = df.getDrawPerLineNoClone().getKey(g);
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
            if (!vm.getSelectedData().isEmpty()) {
                Integer index = df.getDrawPerLineNoClone().getKey(vm.getSelectedData().iterator().next());
                if (index == null) {
                    if (!vm.getSelectedSubfiles().isEmpty()) {
                        index = df.getDrawPerLineNoClone().getKey(vm.getSelectedSubfiles().iterator().next());
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

            final Set<Integer> selectedIndicies = new HashSet<>();
            final SortedSet<Vertex> selectedVertices = new TreeSet<>();
            selectedVertices.addAll(vm.getSelectedVertices());
            for (GData gd : vm.getSelectedData()) {
                final Integer i = df.getDrawPerLineNoClone().getKey(gd);
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
                    Display.getDefault().asyncExec(() -> {

                        if (setTopIndex) {
                            ((CompositeTab) t).getTextComposite().setTopIndex(index2 - 1);
                        }

                        for (Integer i : selectedIndicies) {
                            final GData gs = df.getDrawPerLineNoClone().getValue(i);
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
                    });
                    return;
                }
            }
        }
    }

    public Composite3DViewState exportState() {
        Composite3DViewState state = new Composite3DViewState();

        state.getHideShowState().putAll(lockableDatFileReference.getVertexManager().backupHideShowState());
        state.getSelection().putAll(lockableDatFileReference.getVertexManager().backupSelectedDataState(new HashMap<>()));
        state.getHiddenVertices().addAll(lockableDatFileReference.getVertexManager().getHiddenVertices());
        state.getSelectedVertices().addAll(lockableDatFileReference.getVertexManager().getSelectedVertices());

        state.getManipulator().copyState(manipulator);
        state.setViewportPixelPerLDU(viewportPixelPerLDU);
        state.setZoom(zoom);
        state.setNegDeterminant(negDeterminant);
        state.setZoomExponent(getPerspectiveCalculator().getZoomExponent());
        state.setOffset(getPerspectiveCalculator().getOffset());

        state.getViewportTranslation().load(viewportTranslation);
        state.getViewportRotation().load(viewportRotation);
        state.getViewportMatrix().load(viewportMatrix);
        state.getViewportMatrixInv().load(viewportMatrixInv);

        System.arraycopy(viewportGenerator, 0, state.getViewportGenerator(), 0, 3);
        System.arraycopy(viewportOriginAxis, 0, state.getViewportOriginAxis(), 0, 4);

        state.setzFar(zFar);
        state.setzNear(zNear);

        // FIXME !Save state here for C3D
        Editor3DWindow.getWindow().fillC3DState(state.state, this);

        return state;
    }

    public void importState(Composite3DViewState state) {
        WidgetSelectionHelper.unselectAllChildButtons(mnuRenderMode);

        // FIXME !Load state here for C3D
        loadState(state.state);
        manipulator.copyState(state.getManipulator());

        viewportPixelPerLDU = state.getViewportPixelPerLDU();
        zoom = state.getZoom();
        negDeterminant = state.hasNegDeterminant();
        getPerspectiveCalculator().setZoomExponent(state.getZoomExponent());
        getPerspectiveCalculator().setOffset(state.getOffset());

        viewportTranslation.load(state.getViewportTranslation());
        viewportRotation.load(state.getViewportRotation());
        viewportMatrix.load(state.getViewportMatrix());
        viewportMatrixInv.load(state.getViewportMatrixInv());

        System.arraycopy(state.getViewportGenerator(), 0, viewportGenerator, 0, 3);
        System.arraycopy(state.getViewportOriginAxis(), 0 , viewportOriginAxis, 0, 4);

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
            mntmWireframeModePtr[0].setSelection(true);
            break;
        case 0: // No BFC
            mntmNoBFCPtr[0].setSelection(true);
            break;
        case 1: // Random Colours
            mntmRandomColoursPtr[0].setSelection(true);
            break;
        case 2: // Front-Backface BFC
            mntmBFCFrontBackPtr[0].setSelection(true);
            break;
        case 3: // Backface only BFC
            mntmBFCBackPtr[0].setSelection(true);
            break;
        case 4: // Real BFC
            mntmBFCRealPtr[0].setSelection(true);
            break;
        case 5: // Real BFC with texture mapping
            mntmRealPreviewPtr[0].setSelection(true);
            break;
        case 6: // Special mode for "Add condlines"
            mntmCondlineModePtr[0].setSelection(true);
            break;
        case 7: // Special mode for coplanar quads
            mntmCoplanarityHeatmapModePtr[0].setSelection(true);
            break;
        default:
            break;
        }
    }

    public void loadState(Composite3DState state) {
        final int tmpRenderMode = state.getRenderMode();
        final int tmpLineMode = state.getLineMode();
        final Perspective tmpPerspective = state.getPerspective();
        setGridScale(state.getGridScale());
        getPerspectiveCalculator().setPerspective(tmpPerspective);
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
        setLineMode(tmpLineMode);
        setBlackEdges(state.isAlwaysBlackLines());
        setShowingAxis(state.isShowAxis());
        setAnaglyph3d(state.isAnaglyph3d());
        setRenderMode(tmpRenderMode);
        setShowingCondlineControlPoints(state.isCondlineControlPoints());

        setSyncManipulator(state.isSyncManipulator());
        setSyncTranslation(state.isSyncTranslation());
        setSyncZoom(state.isSyncZoom());

        setPerspectiveOnContextMenu(tmpPerspective);
        getMntmRealPreview().setSelection(tmpLineMode == 0);
        getMntmShowAll().setSelection(tmpLineMode == 1);
        getMntmStdLines().setSelection(tmpLineMode == 2);
        getMntmHideAll().setSelection(tmpLineMode == 4);
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
        setRenderModeOnContextMenu(tmpRenderMode);

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

    public void setPerspectiveOnContextMenu(Perspective perspective) {
        getMntmFront().setSelection(perspective == Perspective.FRONT);
        getMntmBack().setSelection(perspective == Perspective.BACK);
        getMntmLeft().setSelection(perspective == Perspective.LEFT);
        getMntmRight().setSelection(perspective == Perspective.RIGHT);
        getMntmTop().setSelection(perspective == Perspective.TOP);
        getMntmBottom().setSelection(perspective == Perspective.BOTTOM);
    }

    public void setRenderModeOnContextMenu(int renderMode) {
        getMntmNoBFC().setSelection(renderMode == 0);
        getMntmRandomColours().setSelection(renderMode == 1);
        getMntmBFCFrontBack().setSelection(renderMode == 2);
        getMntmBFCBack().setSelection(renderMode == 3);
        getMntmBFCReal().setSelection(renderMode == 4);
        getMntmBFCTextured().setSelection(renderMode == 5);
        getMntmCondlineMode().setSelection(renderMode == 6);
        getMntmCoplanarityHeatmapMode().setSelection(renderMode == 7);
        getMntmWireframeMode().setSelection(renderMode == -1);
    }

    @Override
    public Rectangle getBounds() {
        return super.getBounds();
    }

    public Rectangle getScaledBounds() {
        final double factor = WorkbenchManager.getUserSettingState().getViewportScaleFactor();
        final Rectangle bounds = super.getBounds();
        return new Rectangle(bounds.x, bounds.y, (int) (bounds.width * factor), (int) (bounds.height * factor));
    }
}
