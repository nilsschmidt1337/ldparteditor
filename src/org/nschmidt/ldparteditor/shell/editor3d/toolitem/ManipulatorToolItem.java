package org.nschmidt.ldparteditor.shell.editor3d.toolitem;

import static org.nschmidt.ldparteditor.helper.WidgetUtility.widgetUtil;

import java.math.BigDecimal;
import java.util.Set;

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
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.Matrix;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.enumtype.Task;
import org.nschmidt.ldparteditor.helper.Cocoa;
import org.nschmidt.ldparteditor.helper.Manipulator;
import org.nschmidt.ldparteditor.helper.WidgetSelectionHelper;
import org.nschmidt.ldparteditor.helper.math.MatrixOperations;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.resource.ResourceManager;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.state.KeyStateManager;
import org.nschmidt.ldparteditor.widget.NButton;

@SuppressWarnings("java:S2111")
public class ManipulatorToolItem extends ToolItem {

    private static final NButton[] btnManipulatorToOriginPtr = new NButton[1];
    private static final NButton[] btnManipulatorXReversePtr = new NButton[1];
    private static final NButton[] btnManipulatorYReversePtr = new NButton[1];
    private static final NButton[] btnManipulatorZReversePtr = new NButton[1];
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
    private static final NButton[] btnManipulatorAdjustRotationCenterPtr = new NButton[1];
    private static final NButton[] btnManipulatorToVertexPositionPtr = new NButton[1];
    private static final NButton[] btnManipulatorSwitchXYPtr = new NButton[1];
    private static final NButton[] btnManipulatorSwitchXZPtr = new NButton[1];
    private static final NButton[] btnManipulatorSwitchYZPtr = new NButton[1];

    private static final MenuItem[] mntmManipulatorToOriginPtr = new MenuItem[1];
    private static final MenuItem[] mntmManipulatorXReversePtr = new MenuItem[1];
    private static final MenuItem[] mntmManipulatorYReversePtr = new MenuItem[1];
    private static final MenuItem[] mntmManipulatorZReversePtr = new MenuItem[1];
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
    private static final MenuItem[] mntmManipulatorSwitchXYPtr = new MenuItem[1];
    private static final MenuItem[] mntmManipulatorSwitchXZPtr = new MenuItem[1];
    private static final MenuItem[] mntmManipulatorSwitchYZPtr = new MenuItem[1];

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
        btnManipulatorToOrigin.setImage(ResourceManager.getImage("icon16_toOrigin.png")); //$NON-NLS-1$

        NButton btnManipulatorToWorld = new NButton(manipulatorToolItem, Cocoa.getStyle());
        btnManipulatorToWorldPtr[0] = btnManipulatorToWorld;
        btnManipulatorToWorld.setToolTipText(I18n.E3D_MANIPULATOR_TO_WORLD);
        btnManipulatorToWorld.setImage(ResourceManager.getImage("icon16_toWorld.png")); //$NON-NLS-1$

        NButton btnManipulatorAdjustRotationCenter = new NButton(manipulatorToolItem, Cocoa.getStyle());
        btnManipulatorAdjustRotationCenterPtr[0] = btnManipulatorAdjustRotationCenter;
        btnManipulatorAdjustRotationCenter.setToolTipText(I18n.E3D_ADJUST_ROTATION_CENTER);
        btnManipulatorAdjustRotationCenter.setImage(ResourceManager.getImage("icon16_adjustrotationcenter.png")); //$NON-NLS-1$

        NButton btnManipulatorXReverse = new NButton(manipulatorToolItem, Cocoa.getStyle());
        btnManipulatorXReversePtr[0] = btnManipulatorXReverse;
        btnManipulatorXReverse.setToolTipText(I18n.E3D_REVERSE_X);
        btnManipulatorXReverse.setImage(ResourceManager.getImage("icon16_Xinv.png")); //$NON-NLS-1$

        NButton btnManipulatorYReverse = new NButton(manipulatorToolItem, Cocoa.getStyle());
        btnManipulatorYReversePtr[0] = btnManipulatorYReverse;
        btnManipulatorYReverse.setToolTipText(I18n.E3D_REVERSE_Y);
        btnManipulatorYReverse.setImage(ResourceManager.getImage("icon16_Yinv.png")); //$NON-NLS-1$

        NButton btnManipulatorZReverse = new NButton(manipulatorToolItem, Cocoa.getStyle());
        btnManipulatorZReversePtr[0] = btnManipulatorZReverse;
        btnManipulatorZReverse.setToolTipText(I18n.E3D_REVERSE_Z);
        btnManipulatorZReverse.setImage(ResourceManager.getImage("icon16_Zinv.png")); //$NON-NLS-1$

        NButton btnManipulatorSwitchXY = new NButton(manipulatorToolItem, Cocoa.getStyle());
        btnManipulatorSwitchXYPtr[0] = btnManipulatorSwitchXY;
        btnManipulatorSwitchXY.setToolTipText(I18n.E3D_SWAP_XY);
        btnManipulatorSwitchXY.setImage(ResourceManager.getImage("icon16_XswapY.png")); //$NON-NLS-1$

        NButton btnManipulatorSwitchXZ = new NButton(manipulatorToolItem, Cocoa.getStyle());
        btnManipulatorSwitchXZPtr[0] = btnManipulatorSwitchXZ;
        btnManipulatorSwitchXZ.setToolTipText(I18n.E3D_SWAP_XZ);
        btnManipulatorSwitchXZ.setImage(ResourceManager.getImage("icon16_XswapZ.png")); //$NON-NLS-1$

        NButton btnManipulatorSwitchYZ = new NButton(manipulatorToolItem, Cocoa.getStyle());
        btnManipulatorSwitchYZPtr[0] = btnManipulatorSwitchYZ;
        btnManipulatorSwitchYZ.setToolTipText(I18n.E3D_SWAP_YZ);
        btnManipulatorSwitchYZ.setImage(ResourceManager.getImage("icon16_YswapZ.png")); //$NON-NLS-1$

        NButton btnManipulatorCameraToPos = new NButton(manipulatorToolItem, Cocoa.getStyle());
        btnManipulatorCameraToPosPtr[0] = btnManipulatorCameraToPos;
        btnManipulatorCameraToPos.setToolTipText(I18n.E3D_CAM_TO_MANIPULATOR);
        btnManipulatorCameraToPos.setImage(ResourceManager.getImage("icon16_cameratomanipulator.png")); //$NON-NLS-1$

        NButton btnManipulatorToAverage = new NButton(manipulatorToolItem, Cocoa.getStyle());
        btnManipulatorToAveragePtr[0] = btnManipulatorToAverage;
        btnManipulatorToAverage.setToolTipText(I18n.E3D_MANIPULATOR_TO_AVG);
        btnManipulatorToAverage.setImage(ResourceManager.getImage("icon16_toavg.png")); //$NON-NLS-1$
        KeyStateManager.addTooltipText(btnManipulatorToAverage, I18n.E3D_MANIPULATOR_TO_AVG, Task.MOVE_TO_AVG);

        NButton btnManipulatorToSubfile = new NButton(manipulatorToolItem, Cocoa.getStyle());
        btnManipulatorToSubfilePtr[0] = btnManipulatorToSubfile;
        btnManipulatorToSubfile.setToolTipText(I18n.E3D_MANIPULATOR_TO_SUBFILE);
        btnManipulatorToSubfile.setImage(ResourceManager.getImage("icon16_tosubfile.png")); //$NON-NLS-1$

        NButton btnManipulatorSubfileTo = new NButton(manipulatorToolItem, Cocoa.getStyle());
        btnManipulatorSubfileToPtr[0] = btnManipulatorSubfileTo;
        btnManipulatorSubfileTo.setToolTipText(Cocoa.replaceCtrlByCmd(I18n.E3D_SUBFILE_TO_MANIPULATOR));
        btnManipulatorSubfileTo.setImage(ResourceManager.getImage("icon16_tosubfile2.png")); //$NON-NLS-1$

        NButton btnManipulatorToVertex = new NButton(manipulatorToolItem, Cocoa.getStyle());
        btnManipulatorToVertexPtr[0] = btnManipulatorToVertex;
        btnManipulatorToVertex.setToolTipText(I18n.E3D_MANIPULATOR_TO_VERTEX);
        btnManipulatorToVertex.setImage(ResourceManager.getImage("icon16_tonearestvertex.png")); //$NON-NLS-1$

        NButton btnManipulatorToEdge = new NButton(manipulatorToolItem, Cocoa.getStyle());
        btnManipulatorToEdgePtr[0] = btnManipulatorToEdge;
        btnManipulatorToEdge.setToolTipText(I18n.E3D_MANIPULATOR_TO_EDGE);
        btnManipulatorToEdge.setImage(ResourceManager.getImage("icon16_tonearestedge.png")); //$NON-NLS-1$

        NButton btnManipulatorToSurface = new NButton(manipulatorToolItem, Cocoa.getStyle());
        btnManipulatorToSurfacePtr[0] = btnManipulatorToSurface;
        btnManipulatorToSurface.setToolTipText(I18n.E3D_MANIPULATOR_TO_FACE);
        btnManipulatorToSurface.setImage(ResourceManager.getImage("icon16_tonearestface.png")); //$NON-NLS-1$

        NButton btnManipulatorToVertexNormal = new NButton(manipulatorToolItem, Cocoa.getStyle());
        btnManipulatorToVertexNormalPtr[0] = btnManipulatorToVertexNormal;
        btnManipulatorToVertexNormal.setToolTipText(I18n.E3D_MANIPULATOR_TO_VERTEX_N);
        btnManipulatorToVertexNormal.setImage(ResourceManager.getImage("icon16_tonearestvertexN.png")); //$NON-NLS-1$

        NButton btnManipulatorToVertexPosition = new NButton(manipulatorToolItem, Cocoa.getStyle());
        btnManipulatorToVertexPositionPtr[0] = btnManipulatorToVertexPosition;
        btnManipulatorToVertexPosition.setToolTipText(I18n.E3D_MANIPULATOR_TO_VERTEX_P);
        btnManipulatorToVertexPosition.setImage(ResourceManager.getImage("icon16_tonearestvertexN2.png")); //$NON-NLS-1$

        NButton btnManipulatorToEdgeNormal = new NButton(manipulatorToolItem, Cocoa.getStyle());
        btnManipulatorToEdgeNormalPtr[0] = btnManipulatorToEdgeNormal;
        btnManipulatorToEdgeNormal.setToolTipText(I18n.E3D_MANIPULATOR_TO_EDGE_N);
        btnManipulatorToEdgeNormal.setImage(ResourceManager.getImage("icon16_tonearestedgeN.png")); //$NON-NLS-1$

        NButton btnManipulatorToSurfaceNormal = new NButton(manipulatorToolItem, Cocoa.getStyle());
        btnManipulatorToSurfaceNormalPtr[0] = btnManipulatorToSurfaceNormal;
        btnManipulatorToSurfaceNormal.setToolTipText(I18n.E3D_MANIPULATOR_TO_FACE_N);
        btnManipulatorToSurfaceNormal.setImage(ResourceManager.getImage("icon16_tonearestfaceN.png")); //$NON-NLS-1$
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

        MenuItem mntmManipulatorXReverse = new MenuItem(mnuManipulator, SWT.PUSH);
        mntmManipulatorXReversePtr[0] = mntmManipulatorXReverse;
        mntmManipulatorXReverse.setText(I18n.E3D_REVERSE_X);
        mntmManipulatorXReverse.setImage(ResourceManager.getImage("icon16_Xinv.png")); //$NON-NLS-1$

        MenuItem mntmManipulatorYReverse = new MenuItem(mnuManipulator, SWT.PUSH);
        mntmManipulatorYReversePtr[0] = mntmManipulatorYReverse;
        mntmManipulatorYReverse.setText(I18n.E3D_REVERSE_Y);
        mntmManipulatorYReverse.setImage(ResourceManager.getImage("icon16_Yinv.png")); //$NON-NLS-1$

        MenuItem mntmManipulatorZReverse = new MenuItem(mnuManipulator, SWT.PUSH);
        mntmManipulatorZReversePtr[0] = mntmManipulatorZReverse;
        mntmManipulatorZReverse.setText(I18n.E3D_REVERSE_Z);
        mntmManipulatorZReverse.setImage(ResourceManager.getImage("icon16_Zinv.png")); //$NON-NLS-1$

        new MenuItem(mnuManipulator, SWT.SEPARATOR);

        MenuItem mntmManipulatorSwitchXY = new MenuItem(mnuManipulator, SWT.PUSH);
        mntmManipulatorSwitchXYPtr[0] = mntmManipulatorSwitchXY;
        mntmManipulatorSwitchXY.setText(I18n.E3D_SWAP_XY);
        mntmManipulatorSwitchXY.setImage(ResourceManager.getImage("icon16_XswapY.png")); //$NON-NLS-1$

        MenuItem mntmManipulatorSwitchXZ = new MenuItem(mnuManipulator, SWT.PUSH);
        mntmManipulatorSwitchXZPtr[0] = mntmManipulatorSwitchXZ;
        mntmManipulatorSwitchXZ.setText(I18n.E3D_SWAP_XZ);
        mntmManipulatorSwitchXZ.setImage(ResourceManager.getImage("icon16_XswapZ.png")); //$NON-NLS-1$

        MenuItem mntmManipulatorSwitchYZ = new MenuItem(mnuManipulator, SWT.PUSH);
        mntmManipulatorSwitchYZPtr[0] = mntmManipulatorSwitchYZ;
        mntmManipulatorSwitchYZ.setText(I18n.E3D_SWAP_YZ);
        mntmManipulatorSwitchYZ.setImage(ResourceManager.getImage("icon16_YswapZ.png")); //$NON-NLS-1$

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

    public static void mntmManipulatorToAverage() {
        if (Project.getFileToEdit() != null) {
            Vector4f avg = Project.getFileToEdit().getVertexManager().getSelectionCenter();
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d = renderer.getC3D();
                if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                    c3d.getManipulator().getPosition().set(avg.x, avg.y, avg.z, 1f);
                    c3d.getManipulator().setAccuratePosition(new BigDecimal(avg.x / 1000f), new BigDecimal(avg.y / 1000f), new BigDecimal(avg.z / 1000f));
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
                for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
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
