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
package org.nschmidt.ldparteditor.shell.editor3d.toolitem;

import static org.nschmidt.ldparteditor.helper.WidgetUtility.widgetUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.nschmidt.ldparteditor.composite.Composite3D;
import org.nschmidt.ldparteditor.composite.ToolItem;
import org.nschmidt.ldparteditor.enumtype.Perspective;
import org.nschmidt.ldparteditor.enumtype.Task;
import org.nschmidt.ldparteditor.helper.Cocoa;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.resource.ResourceManager;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.state.KeyStateManager;
import org.nschmidt.ldparteditor.widget.NButton;

public class PerspectiveToolItem extends ToolItem {

    private static final NButton[] btnPerspectiveFrontPtr = new NButton[1];
    private static final NButton[] btnPerspectiveBackPtr = new NButton[1];
    private static final NButton[] btnPerspectiveTopPtr = new NButton[1];
    private static final NButton[] btnPerspectiveBottomPtr = new NButton[1];
    private static final NButton[] btnPerspectiveLeftPtr = new NButton[1];
    private static final NButton[] btnPerspectiveRightPtr = new NButton[1];
    private static final NButton[] btnPerspectiveTwoThirdsPtr = new NButton[1];
    private static final NButton[] btnPerspectiveLockPtr = new NButton[1];

    public PerspectiveToolItem(Composite parent, int style, boolean isHorizontal) {
        super(parent, style, isHorizontal);
        createWidgets(this);
        addListeners();
    }
    
    public static void refreshPerspectiveLockButton(Composite3D c3d) {
        if (c3d != null) {
            btnPerspectiveLockPtr[0].setSelection(c3d.getPerspectiveCalculator().hasRotationLock());
        }
    }

    private static void createWidgets(PerspectiveToolItem perspectiveToolItem) {
        NButton btnPerspectiveFront = new NButton(perspectiveToolItem, Cocoa.getStyle());
        btnPerspectiveFrontPtr[0] = btnPerspectiveFront;
        KeyStateManager.addTooltipText(btnPerspectiveFront, I18n.PERSPECTIVE_FRONT, Task.PERSPECTIVE_FRONT);
        btnPerspectiveFront.setImage(ResourceManager.getImage("icon16_front.png")); //$NON-NLS-1$

        NButton btnPerspectiveBack = new NButton(perspectiveToolItem, Cocoa.getStyle());
        btnPerspectiveBackPtr[0] = btnPerspectiveBack;
        KeyStateManager.addTooltipText(btnPerspectiveBack, I18n.PERSPECTIVE_BACK, Task.PERSPECTIVE_BACK);
        btnPerspectiveBack.setImage(ResourceManager.getImage("icon16_back.png")); //$NON-NLS-1$

        NButton btnPerspectiveLeft = new NButton(perspectiveToolItem, Cocoa.getStyle());
        btnPerspectiveLeftPtr[0] = btnPerspectiveLeft;
        KeyStateManager.addTooltipText(btnPerspectiveLeft, I18n.PERSPECTIVE_LEFT, Task.PERSPECTIVE_LEFT);
        btnPerspectiveLeft.setImage(ResourceManager.getImage("icon16_left.png")); //$NON-NLS-1$

        NButton btnPerspectiveRight = new NButton(perspectiveToolItem, Cocoa.getStyle());
        btnPerspectiveRightPtr[0] = btnPerspectiveRight;
        KeyStateManager.addTooltipText(btnPerspectiveRight, I18n.PERSPECTIVE_RIGHT, Task.PERSPECTIVE_RIGHT);
        btnPerspectiveRight.setImage(ResourceManager.getImage("icon16_right.png")); //$NON-NLS-1$

        NButton btnPerspectiveTop = new NButton(perspectiveToolItem, Cocoa.getStyle());
        btnPerspectiveTopPtr[0] = btnPerspectiveTop;
        KeyStateManager.addTooltipText(btnPerspectiveTop, I18n.PERSPECTIVE_TOP, Task.PERSPECTIVE_TOP);
        btnPerspectiveTop.setImage(ResourceManager.getImage("icon16_top.png")); //$NON-NLS-1$

        NButton btnPerspectiveBottom = new NButton(perspectiveToolItem, Cocoa.getStyle());
        btnPerspectiveBottomPtr[0] = btnPerspectiveBottom;
        KeyStateManager.addTooltipText(btnPerspectiveBottom, I18n.PERSPECTIVE_BOTTOM, Task.PERSPECTIVE_BOTTOM);
        btnPerspectiveBottom.setImage(ResourceManager.getImage("icon16_bottom.png")); //$NON-NLS-1$

        NButton btnPerspectiveTwoThirds = new NButton(perspectiveToolItem, Cocoa.getStyle());
        btnPerspectiveTwoThirdsPtr[0] = btnPerspectiveTwoThirds;
        KeyStateManager.addTooltipText(btnPerspectiveTwoThirds, I18n.PERSPECTIVE_TWO_THIRDS, Task.PERSPECTIVE_TWO_THIRDS);
        btnPerspectiveTwoThirds.setImage(ResourceManager.getImage("icon16_twoThirds.png")); //$NON-NLS-1$
        
        NButton btnPerspectiveLock = new NButton(perspectiveToolItem, Cocoa.getStyle() | SWT.TOGGLE);
        btnPerspectiveLockPtr[0] = btnPerspectiveLock;
        btnPerspectiveLock.setToolTipText(I18n.PERSPECTIVE_LOCK);
        btnPerspectiveLock.setImage(ResourceManager.getImage("icon16_lock.png")); //$NON-NLS-1$
    }

    private static void addListeners() {
        widgetUtil(btnPerspectiveFrontPtr[0]).addSelectionListener(e ->
            changePerspective(Perspective.FRONT)
        );
        widgetUtil(btnPerspectiveBackPtr[0]).addSelectionListener(e ->
            changePerspective(Perspective.BACK)
        );
        widgetUtil(btnPerspectiveLeftPtr[0]).addSelectionListener(e ->
            changePerspective(Perspective.LEFT)
        );
        widgetUtil(btnPerspectiveRightPtr[0]).addSelectionListener(e ->
            changePerspective(Perspective.RIGHT)
        );
        widgetUtil(btnPerspectiveTopPtr[0]).addSelectionListener(e ->
            changePerspective(Perspective.TOP)
        );
        widgetUtil(btnPerspectiveBottomPtr[0]).addSelectionListener(e ->
            changePerspective(Perspective.BOTTOM)
        );
        widgetUtil(btnPerspectiveTwoThirdsPtr[0]).addSelectionListener(e ->
            changePerspective(Perspective.TWO_THIRDS)
        );
        widgetUtil(btnPerspectiveLockPtr[0]).addSelectionListener(e -> {
            Composite3D c3d = getCurrentCoposite3d();
            if (c3d != null) {
                c3d.getPerspectiveCalculator().setRotationLock(btnPerspectiveLockPtr[0].getSelection());
            }
        });
    }

    private static void changePerspective(Perspective perspective) {
        Composite3D c3d = getCurrentCoposite3d();
        if (c3d != null) {
            c3d.getPerspectiveCalculator().setPerspective(perspective);
            c3d.setPerspectiveOnContextMenu(perspective);
        }
        
        regainFocus();
    }

    private static Composite3D getCurrentCoposite3d() {
        return Editor3DWindow.getWindow().getCurrentCoposite3d();
    }

    private static void regainFocus() {
        Editor3DWindow.getWindow().regainFocus();
    }
}
