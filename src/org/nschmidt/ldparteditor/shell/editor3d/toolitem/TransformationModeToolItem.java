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
import org.nschmidt.ldparteditor.composite.ToolItem;
import org.nschmidt.ldparteditor.enumtype.Task;
import org.nschmidt.ldparteditor.enumtype.WorkingMode;
import org.nschmidt.ldparteditor.helper.Cocoa;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.resource.ResourceManager;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.state.KeyStateManager;
import org.nschmidt.ldparteditor.widget.NButton;

public class TransformationModeToolItem extends ToolItem {

    private static final NButton[] btnSelectPtr = new NButton[1];
    private static final NButton[] btnMovePtr = new NButton[1];
    private static final NButton[] btnRotatePtr = new NButton[1];
    private static final NButton[] btnScalePtr = new NButton[1];
    private static final NButton[] btnCombinedPtr = new NButton[1];

    private static WorkingMode workingAction = WorkingMode.SELECT;

    public TransformationModeToolItem(Composite parent, int style, boolean isHorizontal) {
        super(parent, style, isHorizontal);
        createWidgets(this);
        addListeners();
    }

    public static WorkingMode getWorkingAction() {
        return workingAction;
    }

    public static void setWorkingAction(WorkingMode workingAction) {
        TransformationModeToolItem.workingAction = workingAction;
        switch (workingAction) {
        case COMBINED:
            clickRadioBtn(btnCombinedPtr[0]);
            break;
        case MOVE:
            clickRadioBtn(btnMovePtr[0]);
            break;
        case ROTATE:
            clickRadioBtn(btnRotatePtr[0]);
            break;
        case SCALE:
            clickRadioBtn(btnScalePtr[0]);
            break;
        case SELECT:
            clickRadioBtn(btnSelectPtr[0]);
            break;
        default:
            break;
        }
    }

    private static void createWidgets(TransformationModeToolItem transformationModeToolItem) {
        NButton btnSelect = new NButton(transformationModeToolItem, SWT.TOGGLE | Cocoa.getStyle());
        btnSelectPtr[0] = btnSelect;
        KeyStateManager.addTooltipText(btnSelect, I18n.E3D_SELECT, Task.MODE_SELECT);
        btnSelect.setSelection(true);
        btnSelect.setImage(ResourceManager.getImage("icon16_select.png")); //$NON-NLS-1$

        NButton btnMove = new NButton(transformationModeToolItem, SWT.TOGGLE | Cocoa.getStyle());
        btnMovePtr[0] = btnMove;
        KeyStateManager.addTooltipText(btnMove, I18n.E3D_MOVE, Task.MODE_MOVE);
        btnMove.setImage(ResourceManager.getImage("icon16_move.png")); //$NON-NLS-1$

        NButton btnRotate = new NButton(transformationModeToolItem, SWT.TOGGLE | Cocoa.getStyle());
        btnRotatePtr[0] = btnRotate;
        KeyStateManager.addTooltipText(btnRotate, I18n.E3D_ROTATE, Task.MODE_ROTATE);
        btnRotate.setImage(ResourceManager.getImage("icon16_rotate.png")); //$NON-NLS-1$

        NButton btnScale = new NButton(transformationModeToolItem, SWT.TOGGLE | Cocoa.getStyle());
        btnScalePtr[0] = btnScale;
        KeyStateManager.addTooltipText(btnScale, I18n.E3D_SCALE, Task.MODE_SCALE);
        btnScale.setImage(ResourceManager.getImage("icon16_scale.png")); //$NON-NLS-1$

        NButton btnCombined = new NButton(transformationModeToolItem, SWT.TOGGLE | Cocoa.getStyle());
        btnCombinedPtr[0] = btnCombined;
        KeyStateManager.addTooltipText(btnCombined, I18n.E3D_COMBINED, Task.MODE_COMBINED);
        btnCombined.setImage(ResourceManager.getImage("icon16_combined.png")); //$NON-NLS-1$
    }

    private static void addListeners() {
        widgetUtil(btnSelectPtr[0]).addSelectionListener(e -> switchWorkingAction(btnSelectPtr, WorkingMode.SELECT));
        widgetUtil(btnMovePtr[0]).addSelectionListener(e -> switchWorkingAction(btnMovePtr, WorkingMode.MOVE));
        widgetUtil(btnRotatePtr[0]).addSelectionListener(e -> switchWorkingAction(btnRotatePtr, WorkingMode.ROTATE));
        widgetUtil(btnScalePtr[0]).addSelectionListener(e -> switchWorkingAction(btnScalePtr, WorkingMode.SCALE));
        widgetUtil(btnCombinedPtr[0]).addSelectionListener(e -> switchWorkingAction(btnCombinedPtr, WorkingMode.COMBINED));
    }

    private static void switchWorkingAction(NButton[] ptr, WorkingMode workingMode) {
        clickRadioBtn(ptr[0]);
        workingAction = workingMode;
        AddToolItem.disableAddAction();
        regainFocus();
    }

    private static void regainFocus() {
        Editor3DWindow.getWindow().regainFocus();
    }
}
