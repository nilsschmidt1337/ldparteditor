package org.nschmidt.ldparteditor.shell.editor3d.toolitem;

import static org.nschmidt.ldparteditor.helper.WidgetUtility.widgetUtil;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.composite.Composite3D;
import org.nschmidt.ldparteditor.composite.ToolItem;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.Matrix;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.dialog.selectgdata.SelectionDialog;
import org.nschmidt.ldparteditor.enumtype.Task;
import org.nschmidt.ldparteditor.helper.Cocoa;
import org.nschmidt.ldparteditor.helper.Manipulator;
import org.nschmidt.ldparteditor.helper.WidgetSelectionHelper;
import org.nschmidt.ldparteditor.helper.math.MatrixOperations;
import org.nschmidt.ldparteditor.helper.math.Vector3d;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.resource.ResourceManager;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.state.KeyStateManager;
import org.nschmidt.ldparteditor.widget.NButton;


public class ManipulatorToolItem extends ToolItem {

    private static final NButton[] btnManipulatorToOriginPtr = new NButton[1];
    private static final NButton[] btnManipulatorToWorldPtr = new NButton[1];
    private static final NButton[] btnManipulatorCameraToPosPtr = new NButton[1];
    private static final NButton[] btnManipulatorToAveragePtr = new NButton[1];
    private static final NButton[] btnManipulatorToSubfilePtr = new NButton[1];
    private static final NButton[] btnManipulatorSubfileToPtr = new NButton[1];
    private static final NButton[] btnManipulatorToVertexPtr = new NButton[1];
    private static final NButton[] btnManipulatorToEdgePtr = new NButton[1];
    private static final NButton[] btnManipulatorToSurfacePtr = new NButton[1];
    private static final NButton[] btnManipulatorToVertexNormalPtr = new NButton[1];
    private static final NButton[] btnManipulatorToEdgeNormalPtr = new NButton[1];
    private static final NButton[] btnManipulatorToSurfaceNormalPtr = new NButton[1];
    private static final NButton[] btnManipulatorToVertexPositionPtr = new NButton[1];

    private static final MenuItem[] mntmManipulatorToOriginPtr = new MenuItem[1];
    private static final MenuItem[] mntmManipulatorToWorldPtr = new MenuItem[1];
    private static final MenuItem[] mntmManipulatorCameraToPosPtr = new MenuItem[1];
    private static final MenuItem[] mntmManipulatorToAveragePtr = new MenuItem[1];
    private static final MenuItem[] mntmManipulatorToSubfilePtr = new MenuItem[1];
    private static final MenuItem[] mntmManipulatorSubfileToPtr = new MenuItem[1];
    private static final MenuItem[] mntmManipulatorToVertexPtr = new MenuItem[1];
    private static final MenuItem[] mntmManipulatorToEdgePtr = new MenuItem[1];
    private static final MenuItem[] mntmManipulatorToSurfacePtr = new MenuItem[1];
    private static final MenuItem[] mntmManipulatorToVertexNormalPtr = new MenuItem[1];
    private static final MenuItem[] mntmManipulatorToEdgeNormalPtr = new MenuItem[1];
    private static final MenuItem[] mntmManipulatorToSurfaceNormalPtr = new MenuItem[1];
    private static final MenuItem[] mntmManipulatorAdjustRotationCenterPtr = new MenuItem[1];
    private static final MenuItem[] mntmManipulatorToVertexPositionPtr = new MenuItem[1];

    public static ToolItem create(Composite target, ToolItem toolItem, int style, boolean isHorizontal, boolean isDropDown) {
        if (isDropDown) {
            createMenuItemWidgets(target, toolItem, style, isHorizontal);
            addListeners();

            return null;
        } else {
            return new ManipulatorToolItem(target, style, isHorizontal);
        }
    }

    private ManipulatorToolItem(Composite parent, int style, boolean isHorizontal) {
        super(parent, style, isHorizontal);
        createButtonWidgets(this);
        addListeners();
    }

    private static void createButtonWidgets(ManipulatorToolItem manipulatorToolItem) {
        NButton btnManipulatorToOrigin = new NButton(manipulatorToolItem, Cocoa.getStyle());
        btnManipulatorToOriginPtr[0] = btnManipulatorToOrigin;
        btnManipulatorToOrigin.setToolTipText(I18n.E3D_MANIPULATOR_TO_ORIGIN);
        btnManipulatorToOrigin.setImage(ResourceManager.getImageInvertedInDarkMode("icon16_toOrigin.png")); //$NON-NLS-1$

        NButton btnManipulatorToWorld = new NButton(manipulatorToolItem, Cocoa.getStyle());
        btnManipulatorToWorldPtr[0] = btnManipulatorToWorld;
        btnManipulatorToWorld.setToolTipText(I18n.E3D_MANIPULATOR_TO_WORLD);
        btnManipulatorToWorld.setImage(ResourceManager.getImageInvertedInDarkMode("icon16_toWorld.png")); //$NON-NLS-1$

        NButton btnManipulatorCameraToPos = new NButton(manipulatorToolItem, Cocoa.getStyle());
        btnManipulatorCameraToPosPtr[0] = btnManipulatorCameraToPos;
        btnManipulatorCameraToPos.setToolTipText(I18n.E3D_CAM_TO_MANIPULATOR);
        btnManipulatorCameraToPos.setImage(ResourceManager.getImageInvertedInDarkMode("icon16_cameratomanipulator.png")); //$NON-NLS-1$

        NButton btnManipulatorToAverage = new NButton(manipulatorToolItem, Cocoa.getStyle());
        btnManipulatorToAveragePtr[0] = btnManipulatorToAverage;
        btnManipulatorToAverage.setToolTipText(I18n.E3D_MANIPULATOR_TO_AVG);
        btnManipulatorToAverage.setImage(ResourceManager.getImageInvertedInDarkMode("icon16_toavg.png")); //$NON-NLS-1$
        KeyStateManager.addTooltipText(btnManipulatorToAverage, I18n.E3D_MANIPULATOR_TO_AVG, Task.MOVE_TO_AVG);

        NButton btnManipulatorToSubfile = new NButton(manipulatorToolItem, Cocoa.getStyle());
        btnManipulatorToSubfilePtr[0] = btnManipulatorToSubfile;
        btnManipulatorToSubfile.setToolTipText(I18n.E3D_MANIPULATOR_TO_SUBFILE);
        btnManipulatorToSubfile.setImage(ResourceManager.getImageInvertedInDarkMode("icon16_tosubfile.png")); //$NON-NLS-1$

        NButton btnManipulatorSubfileTo = new NButton(manipulatorToolItem, Cocoa.getStyle());
        btnManipulatorSubfileToPtr[0] = btnManipulatorSubfileTo;
        btnManipulatorSubfileTo.setToolTipText(Cocoa.replaceCtrlByCmd(I18n.E3D_SUBFILE_TO_MANIPULATOR));
        btnManipulatorSubfileTo.setImage(ResourceManager.getImageInvertedInDarkMode("icon16_tosubfile2.png")); //$NON-NLS-1$

        NButton btnManipulatorToVertex = new NButton(manipulatorToolItem, Cocoa.getStyle());
        btnManipulatorToVertexPtr[0] = btnManipulatorToVertex;
        btnManipulatorToVertex.setToolTipText(I18n.E3D_MANIPULATOR_TO_VERTEX);
        btnManipulatorToVertex.setImage(ResourceManager.getImageInvertedInDarkMode("icon16_tonearestvertex.png")); //$NON-NLS-1$

        NButton btnManipulatorToEdge = new NButton(manipulatorToolItem, Cocoa.getStyle());
        btnManipulatorToEdgePtr[0] = btnManipulatorToEdge;
        btnManipulatorToEdge.setToolTipText(I18n.E3D_MANIPULATOR_TO_EDGE);
        btnManipulatorToEdge.setImage(ResourceManager.getImageInvertedInDarkMode("icon16_tonearestedge.png")); //$NON-NLS-1$

        NButton btnManipulatorToSurface = new NButton(manipulatorToolItem, Cocoa.getStyle());
        btnManipulatorToSurfacePtr[0] = btnManipulatorToSurface;
        btnManipulatorToSurface.setToolTipText(I18n.E3D_MANIPULATOR_TO_FACE);
        btnManipulatorToSurface.setImage(ResourceManager.getImageInvertedInDarkMode("icon16_tonearestface.png")); //$NON-NLS-1$

        NButton btnManipulatorToVertexNormal = new NButton(manipulatorToolItem, Cocoa.getStyle());
        btnManipulatorToVertexNormalPtr[0] = btnManipulatorToVertexNormal;
        btnManipulatorToVertexNormal.setToolTipText(I18n.E3D_MANIPULATOR_TO_VERTEX_N);
        btnManipulatorToVertexNormal.setImage(ResourceManager.getImageInvertedInDarkMode("icon16_tonearestvertexN.png")); //$NON-NLS-1$

        NButton btnManipulatorToVertexPosition = new NButton(manipulatorToolItem, Cocoa.getStyle());
        btnManipulatorToVertexPositionPtr[0] = btnManipulatorToVertexPosition;
        btnManipulatorToVertexPosition.setToolTipText(I18n.E3D_MANIPULATOR_TO_VERTEX_P);
        btnManipulatorToVertexPosition.setImage(ResourceManager.getImageInvertedInDarkMode("icon16_tonearestvertexN2.png")); //$NON-NLS-1$

        NButton btnManipulatorToEdgeNormal = new NButton(manipulatorToolItem, Cocoa.getStyle());
        btnManipulatorToEdgeNormalPtr[0] = btnManipulatorToEdgeNormal;
        btnManipulatorToEdgeNormal.setToolTipText(I18n.E3D_MANIPULATOR_TO_EDGE_N);
        btnManipulatorToEdgeNormal.setImage(ResourceManager.getImageInvertedInDarkMode("icon16_tonearestedgeN.png")); //$NON-NLS-1$

        NButton btnManipulatorToSurfaceNormal = new NButton(manipulatorToolItem, Cocoa.getStyle());
        btnManipulatorToSurfaceNormalPtr[0] = btnManipulatorToSurfaceNormal;
        btnManipulatorToSurfaceNormal.setToolTipText(I18n.E3D_MANIPULATOR_TO_FACE_N);
        btnManipulatorToSurfaceNormal.setImage(ResourceManager.getImageInvertedInDarkMode("icon16_tonearestfaceN.png")); //$NON-NLS-1$
    }

    private static void createMenuItemWidgets(Composite target, ToolItem toolItem, int style, boolean isHorizontal) {
        if (toolItem == null) {
            toolItem = new ToolItem(target, style, isHorizontal);
        }
        final NButton btnManipulatorActions = new NButton(toolItem, SWT.ARROW | SWT.DOWN);
        btnManipulatorActions.setToolTipText(I18n.E3D_MODIFY_MANIPULATOR);
        final Menu mnuManipulator = new Menu(Editor3DWindow.getWindow().getShell(), SWT.POP_UP);
        widgetUtil(btnManipulatorActions).addSelectionListener(e -> {
            Point loc = btnManipulatorActions.getLocation();
            Rectangle rect = btnManipulatorActions.getBounds();
            Point mLoc = new Point(loc.x - 1, loc.y + rect.height);
            mnuManipulator.setLocation(Editor3DWindow.getWindow().getShell().getDisplay().map(btnManipulatorActions.getParent(), null, mLoc));
            mnuManipulator.setVisible(true);
            Editor3DWindow.getWindow().regainFocus();
        });

        MenuItem mntmManipulatorToOrigin = new MenuItem(mnuManipulator, SWT.PUSH);
        mntmManipulatorToOriginPtr[0] = mntmManipulatorToOrigin;
        mntmManipulatorToOrigin.setText(I18n.E3D_MANIPULATOR_TO_ORIGIN);
        mntmManipulatorToOrigin.setImage(ResourceManager.getImage("icon16_toOrigin.png")); //$NON-NLS-1$

        MenuItem mntmManipulatorToWorld = new MenuItem(mnuManipulator, SWT.PUSH);
        mntmManipulatorToWorldPtr[0] = mntmManipulatorToWorld;
        mntmManipulatorToWorld.setText(I18n.E3D_MANIPULATOR_TO_WORLD);
        mntmManipulatorToWorld.setImage(ResourceManager.getImage("icon16_toWorld.png")); //$NON-NLS-1$

        MenuItem mntmManipulatorAdjustRotationCenter = new MenuItem(mnuManipulator, SWT.PUSH);
        mntmManipulatorAdjustRotationCenterPtr[0] = mntmManipulatorAdjustRotationCenter;
        mntmManipulatorAdjustRotationCenter.setText(I18n.E3D_ADJUST_ROTATION_CENTER);
        mntmManipulatorAdjustRotationCenter.setImage(ResourceManager.getImage("icon16_adjustrotationcenter.png")); //$NON-NLS-1$

        new MenuItem(mnuManipulator, SWT.SEPARATOR);

        MenuItem mntmManipulatorCameraToPos = new MenuItem(mnuManipulator, SWT.PUSH);
        mntmManipulatorCameraToPosPtr[0] = mntmManipulatorCameraToPos;
        mntmManipulatorCameraToPos.setText(I18n.E3D_CAM_TO_MANIPULATOR);
        mntmManipulatorCameraToPos.setImage(ResourceManager.getImage("icon16_cameratomanipulator.png")); //$NON-NLS-1$

        MenuItem mntmManipulatorToAverage = new MenuItem(mnuManipulator, SWT.PUSH);
        mntmManipulatorToAveragePtr[0] = mntmManipulatorToAverage;
        mntmManipulatorToAverage.setText(I18n.E3D_MANIPULATOR_TO_AVG);
        mntmManipulatorToAverage.setImage(ResourceManager.getImage("icon16_toavg.png")); //$NON-NLS-1$
        KeyStateManager.addKeyText(mntmManipulatorToAverage, I18n.E3D_MANIPULATOR_TO_AVG, Task.MOVE_TO_AVG);

        new MenuItem(mnuManipulator, SWT.SEPARATOR);

        MenuItem mntmManipulatorToSubfile = new MenuItem(mnuManipulator, SWT.PUSH);
        mntmManipulatorToSubfilePtr[0] = mntmManipulatorToSubfile;
        mntmManipulatorToSubfile.setText(I18n.E3D_MANIPULATOR_TO_SUBFILE);
        mntmManipulatorToSubfile.setImage(ResourceManager.getImage("icon16_tosubfile.png")); //$NON-NLS-1$

        MenuItem mntmManipulatorSubfileTo = new MenuItem(mnuManipulator, SWT.PUSH);
        mntmManipulatorSubfileToPtr[0] = mntmManipulatorSubfileTo;
        mntmManipulatorSubfileTo.setText(Cocoa.replaceCtrlByCmd(I18n.E3D_SUBFILE_TO_MANIPULATOR));
        mntmManipulatorSubfileTo.setImage(ResourceManager.getImage("icon16_tosubfile.png")); //$NON-NLS-1$

        new MenuItem(mnuManipulator, SWT.SEPARATOR);

        MenuItem mntmManipulatorToVertex = new MenuItem(mnuManipulator, SWT.PUSH);
        mntmManipulatorToVertexPtr[0] = mntmManipulatorToVertex;
        mntmManipulatorToVertex.setText(I18n.E3D_MANIPULATOR_TO_VERTEX);
        mntmManipulatorToVertex.setImage(ResourceManager.getImage("icon16_tonearestvertex.png")); //$NON-NLS-1$

        MenuItem mntmManipulatorToEdge = new MenuItem(mnuManipulator, SWT.PUSH);
        mntmManipulatorToEdgePtr[0] = mntmManipulatorToEdge;
        mntmManipulatorToEdge.setText(I18n.E3D_MANIPULATOR_TO_EDGE);
        mntmManipulatorToEdge.setImage(ResourceManager.getImage("icon16_tonearestedge.png")); //$NON-NLS-1$

        MenuItem mntmManipulatorToSurface = new MenuItem(mnuManipulator, SWT.PUSH);
        mntmManipulatorToSurfacePtr[0] = mntmManipulatorToSurface;
        mntmManipulatorToSurface.setText(I18n.E3D_MANIPULATOR_TO_FACE);
        mntmManipulatorToSurface.setImage(ResourceManager.getImage("icon16_tonearestface.png")); //$NON-NLS-1$

        new MenuItem(mnuManipulator, SWT.SEPARATOR);

        MenuItem mntmManipulatorToVertexNormal = new MenuItem(mnuManipulator, SWT.PUSH);
        mntmManipulatorToVertexNormalPtr[0] = mntmManipulatorToVertexNormal;
        mntmManipulatorToVertexNormal.setText(I18n.E3D_MANIPULATOR_TO_VERTEX_N);
        mntmManipulatorToVertexNormal.setImage(ResourceManager.getImage("icon16_tonearestvertexN.png")); //$NON-NLS-1$

        MenuItem mntmManipulatorToVertexPosition = new MenuItem(mnuManipulator, SWT.PUSH);
        mntmManipulatorToVertexPositionPtr[0] = mntmManipulatorToVertexPosition;
        mntmManipulatorToVertexPosition.setText(I18n.E3D_MANIPULATOR_TO_VERTEX_P);
        mntmManipulatorToVertexPosition.setImage(ResourceManager.getImage("icon16_tonearestvertexN2.png")); //$NON-NLS-1$

        MenuItem mntmManipulatorToEdgeNormal = new MenuItem(mnuManipulator, SWT.PUSH);
        mntmManipulatorToEdgeNormalPtr[0] = mntmManipulatorToEdgeNormal;
        mntmManipulatorToEdgeNormal.setText(I18n.E3D_MANIPULATOR_TO_EDGE_N);
        mntmManipulatorToEdgeNormal.setImage(ResourceManager.getImage("icon16_tonearestedgeN.png")); //$NON-NLS-1$

        MenuItem mntmManipulatorToSurfaceNormal = new MenuItem(mnuManipulator, SWT.PUSH);
        mntmManipulatorToSurfaceNormalPtr[0] = mntmManipulatorToSurfaceNormal;
        mntmManipulatorToSurfaceNormal.setText(I18n.E3D_MANIPULATOR_TO_FACE_N);
        mntmManipulatorToSurfaceNormal.setImage(ResourceManager.getImage("icon16_tonearestfaceN.png")); //$NON-NLS-1$
    }

    private static void addListeners() {
        if (btnManipulatorToOriginPtr[0] != null) widgetUtil(btnManipulatorToOriginPtr[0]).addSelectionListener(e -> mntmManipulatorToOrigin());
        if (btnManipulatorToWorldPtr[0] != null) widgetUtil(btnManipulatorToWorldPtr[0]).addSelectionListener(e -> mntmManipulatorToWorld());
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
        if (btnManipulatorToVertexPositionPtr[0] != null) widgetUtil(btnManipulatorToVertexPositionPtr[0]).addSelectionListener(e -> mntmManipulatorToVertexPosition());
        if (mntmManipulatorToOriginPtr[0] != null) widgetUtil(mntmManipulatorToOriginPtr[0]).addSelectionListener(e -> mntmManipulatorToOrigin());
        if (mntmManipulatorToWorldPtr[0] != null) widgetUtil(mntmManipulatorToWorldPtr[0]).addSelectionListener(e -> mntmManipulatorToWorld());
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
    }

    public static void mntmManipulatorToOrigin() {
        if (Project.getFileToEdit() != null) {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                    c3d.getManipulator().positionToOrigin();
                }
            }
        }
        regainFocus();
    }

    public static void mntmManipulatorToWorld() {
        if (Project.getFileToEdit() != null) {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
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

    public static void mntmManipulatorXReverse() {
        for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
            Composite3D c3d = renderer.getC3D();
            if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                Vector4f.sub(new Vector4f(0f, 0f, 0f, 2f), c3d.getManipulator().getXaxis(), c3d.getManipulator().getXaxis());
                BigDecimal[] a = c3d.getManipulator().getAccurateXaxis();
                c3d.getManipulator().setAccurateXaxis(a[0].negate(), a[1].negate(), a[2].negate());
            }
        }
        regainFocus();
    }

    public static void mntmManipulatorYReverse() {
        for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
            Composite3D c3d = renderer.getC3D();
            if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                Vector4f.sub(new Vector4f(0f, 0f, 0f, 2f), c3d.getManipulator().getYaxis(), c3d.getManipulator().getYaxis());
                BigDecimal[] a = c3d.getManipulator().getAccurateYaxis();
                c3d.getManipulator().setAccurateYaxis(a[0].negate(), a[1].negate(), a[2].negate());
            }
        }
        regainFocus();
    }

    public static void mntmManipulatorZReverse() {
        for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
            Composite3D c3d = renderer.getC3D();
            if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                Vector4f.sub(new Vector4f(0f, 0f, 0f, 2f), c3d.getManipulator().getZaxis(), c3d.getManipulator().getZaxis());
                BigDecimal[] a = c3d.getManipulator().getAccurateZaxis();
                c3d.getManipulator().setAccurateZaxis(a[0].negate(), a[1].negate(), a[2].negate());
            }
        }
        regainFocus();
    }

    public static void mntmManipulatorCameraToPos() {
        for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
            Composite3D c3d = renderer.getC3D();
            Vector4f pos = c3d.getManipulator().getPosition();
            Vector4f a1 = c3d.getManipulator().getXaxis();
            Vector4f a2 = c3d.getManipulator().getYaxis();
            Vector4f a3 = c3d.getManipulator().getZaxis();
            if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit()) && !c3d.getPerspectiveCalculator().hasRotationLock()) {
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

    @SuppressWarnings("java:S2111")
    public static void mntmManipulatorToAverage() {
        if (Project.getFileToEdit() != null) {
            Vector3d avg = Project.getFileToEdit().getVertexManager().getSelectionCenter();
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                    c3d.getManipulator().getPosition().set(avg.x.floatValue() * 1000f, avg.y.floatValue() * 1000f, avg.z.floatValue() * 1000f, 1f);
                    c3d.getManipulator().setAccuratePosition(avg.x, avg.y, avg.z);
                }
            }
        }
        regainFocus();
    }

    public static void mntmManipulatorToSubfile() {
        if (Project.getFileToEdit() != null) {
            Set<GData1> subfiles = Project.getFileToEdit().getVertexManager().getSelectedSubfiles();
            if (!subfiles.isEmpty()) {
                final GData1 subfile = subfiles.iterator().next();
                Manipulator origin = null;
                for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
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
                    for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                        renderer.getC3D().getManipulator().copyState(origin);
                    }
                }
            }
        }
        regainFocus();
    }

    public static void mntmManipulatorSubfileTo(boolean resetScale) {
        final DatFile df = Project.getFileToEdit();
        if (df != null) {
            final VertexManager vm = df.getVertexManager();
            Set<GData1> subfiles = vm.getSelectedSubfiles();
            if (!subfiles.isEmpty()) {
                final SelectionDialog<GData1> dialog = new SelectionDialog<>(Editor3DWindow.getWindow().getShell(), I18n.E3D_SELECT_REFERENCE_OBJECT_SUBFILE, "icon16_primitives.png"); //$NON-NLS-1$
                dialog.addData(subfiles.stream().filter(s -> vm.getLineLinkedToVertices().containsKey(s)).sorted(
                        (a,b) -> Integer.compare(df.getDrawPerLineNoClone().getKey(a), df.getDrawPerLineNoClone().getKey(b))
                ).toList());
                GData1 mainSubfile = null;
                final boolean moreThanOneSubfileSelected = subfiles.size() > 1;
                if (moreThanOneSubfileSelected) {
                    if (dialog.open() == IDialogConstants.OK_ID) {
                        mainSubfile = dialog.getSelection();
                    } else {
                        return;
                    }
                } else {
                    for (GData1 g1 : subfiles) {
                        if (vm.getLineLinkedToVertices().containsKey(g1)) {
                            mainSubfile = g1;
                            break;
                        }
                    }
                }

                if (mainSubfile == null) {
                    return;
                }

                final Composite3D lastSelectedComposite = df.getLastSelectedComposite();
                for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                    Composite3D c3d = renderer.getC3D();
                    if ((lastSelectedComposite != null && !lastSelectedComposite.isDisposed() && c3d.equals(lastSelectedComposite))
                    ||  ((lastSelectedComposite == null || lastSelectedComposite.isDisposed()) && df.equals(c3d.getLockableDatFileReference()))) {
                        vm.addSnapshot();
                        vm.backupHideShowState();
                        final Set<GData1> newSubfiles = new HashSet<>();
                        final Matrix ma = c3d.getManipulator().getAccurateRotation();
                        final BigDecimal[] pos = c3d.getManipulator().getAccuratePosition();
                        final Matrix mainMatrixWithTranslation = mainSubfile.getAccurateProductMatrix();
                        final Matrix mainMatrix = mainSubfile.getAccurateProductMatrix().translateGlobally(mainMatrixWithTranslation.m30.negate(), mainMatrixWithTranslation.m31.negate(), mainMatrixWithTranslation.m32.negate());
                        final Vector3d mainX = new Vector3d();
                        final Vector3d mainY = new Vector3d();
                        final Vector3d mainZ = new Vector3d();
                        new Vector3d(mainMatrix.m00, mainMatrix.m01, mainMatrix.m02).normalise(mainX);
                        new Vector3d(mainMatrix.m10, mainMatrix.m11, mainMatrix.m12).normalise(mainY);
                        new Vector3d(mainMatrix.m20, mainMatrix.m21, mainMatrix.m22).normalise(mainZ);
                        final Matrix mainBaseMatrix = new Matrix(
                                mainX.x, mainX.y, mainX.z, BigDecimal.ZERO,
                                mainY.x, mainY.y, mainY.z, BigDecimal.ZERO,
                                mainZ.x, mainZ.y, mainZ.z, BigDecimal.ZERO,
                                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE);
                        final Matrix mainBaseMatrixInverse = mainBaseMatrix.invert();
                        final Matrix mainInverse = mainMatrix.invert();
                        final Matrix inverse;
                        if (resetScale) {
                            inverse = mainInverse;
                        } else {
                            inverse = mainBaseMatrixInverse;
                        }

                        final Matrix mainM = Matrix.mul(inverse, mainMatrixWithTranslation);
                        final BigDecimal dX = mainM.m30.negate();
                        final BigDecimal dY = mainM.m31.negate();
                        final BigDecimal dZ = mainM.m32.negate();

                        // Transform every selected subfile relatively to the main subfile
                        for (GData1 subfile : new HashSet<>(subfiles)) {
                            if (subfile != mainSubfile && vm.getLineLinkedToVertices().containsKey(subfile)) {
                                final Matrix subfileMatrixWithTranslation = subfile.getAccurateProductMatrix();
                                final Matrix subM = Matrix.mul(inverse, subfileMatrixWithTranslation).translateGlobally(dX, dY, dZ);
                                final Matrix subMprojected = Matrix.mul(ma.transpose(), subM).translateGlobally(pos[0], pos[1], pos[2]);
                                vm.transformSubfile(subfile, subMprojected, true, false);
                                newSubfiles.addAll(vm.getSelectedSubfiles());
                            }
                        }

                        // Finally, transform the chosen subfile
                        final Matrix mainMprojected = Matrix.mul(ma.transpose(), mainM.translateGlobally(dX, dY, dZ)).translateGlobally(pos[0], pos[1], pos[2]);
                        vm.transformSubfile(mainSubfile, mainMprojected, true, true);
                        newSubfiles.addAll(vm.getSelectedSubfiles());
                        vm.getSelectedSubfiles().addAll(newSubfiles);
                        vm.getSelectedData().addAll(newSubfiles);
                        vm.reSelectSubFiles();
                        df.addHistory();
                        break;
                    }
                }
            }
        }
        regainFocus();
    }

    public static void mntmManipulatorToVertex() {
        for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
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

    @SuppressWarnings("java:S2111")
    public static void mntmManipulatorToEdge() {
        for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
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

    @SuppressWarnings("java:S2111")
    public static void mntmManipulatorToSurface() {
        for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
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

    @SuppressWarnings("java:S2111")
    public static void mntmManipulatorToVertexNormal() {
        for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
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

    @SuppressWarnings("java:S2111")
    public static void mntmManipulatorToEdgeNormal() {
        for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
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

    @SuppressWarnings("java:S2111")
    public static void mntmManipulatorToSurfaceNormal() {
        for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
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

    public static void mntmManipulatorAdjustRotationCenter() {
        for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
            Composite3D c3d = renderer.getC3D();
            if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();
                vm.adjustRotationCenter(c3d, null);
            }
        }
        regainFocus();
    }

    public static void mntmManipulatorToVertexPosition() {
        for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
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

    public static void mntmManipulatorSwitchYZ() {
        for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
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

    public static void mntmManipulatorSwitchXZ() {
        for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
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

    public static void mntmManipulatorSwitchXY() {
        for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
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

    private static void regainFocus() {
        Editor3DWindow.getWindow().regainFocus();
    }
}
