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
import org.nschmidt.ldparteditor.helper.Cocoa;
import org.nschmidt.ldparteditor.helper.composite3d.GuiStatusManager;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.resource.ResourceManager;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.state.KeyStateManager;
import org.nschmidt.ldparteditor.widget.NButton;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

public class MiscToggleToolItem extends ToolItem {

    private static final NButton[] btnMoveAdjacentDataPtr = new NButton[1];
    private static final NButton[] btnNoTransparentSelectionPtr = new NButton[1];
    private static final NButton[] btnBFCTogglePtr = new NButton[1];

    private static boolean movingAdjacentData = WorkbenchManager.getUserSettingState().isMovingAdjacentData();
    private static boolean noTransparentSelection = false;
    private static boolean bfcToggle = false;

    public MiscToggleToolItem(Composite parent, int style, boolean isHorizontal) {
        super(parent, style, isHorizontal);
        createWidgets(this);
        addListeners();
    }

    public static void toggleMoveAdjacentData() {
        setMovingAdjacentData(!isMovingAdjacentData());
        btnMoveAdjacentDataPtr[0].setSelection(isMovingAdjacentData());
        clickSingleBtn(btnMoveAdjacentDataPtr[0]);
    }

    public static boolean isMovingAdjacentData() {
        return movingAdjacentData;
    }

    public static void setMovingAdjacentData(boolean movingAdjacentData) {
        btnMoveAdjacentDataPtr[0].setSelection(movingAdjacentData);
        MiscToggleToolItem.movingAdjacentData = movingAdjacentData;
        WorkbenchManager.getUserSettingState().setMovingAdjacentData(movingAdjacentData);
    }

    public static boolean hasNoTransparentSelection() {
        return noTransparentSelection;
    }

    public static void setNoTransparentSelection(boolean noTransparentSelection) {
        MiscToggleToolItem.noTransparentSelection = noTransparentSelection;
    }

    public static boolean hasBfcToggle() {
        return bfcToggle;
    }

    public static void setBfcToggle(boolean bfcToggle) {
        MiscToggleToolItem.bfcToggle = bfcToggle;
    }

    private static void createWidgets(MiscToggleToolItem miscToggleToolItem) {
        NButton btnAdjacentMove = new NButton(miscToggleToolItem, SWT.TOGGLE | Cocoa.getStyle());
        btnMoveAdjacentDataPtr[0] = btnAdjacentMove;
        KeyStateManager.addTooltipText(btnAdjacentMove, I18n.E3D_MOVE_ADJACENT_DATA, Task.MOVE_ADJACENT_DATA);
        btnAdjacentMove.setImage(ResourceManager.getImage("icon16_adjacentmove.png")); //$NON-NLS-1$
        btnAdjacentMove.setSelection(WorkbenchManager.getUserSettingState().isMovingAdjacentData());

        NButton btnTransSelection = new NButton(miscToggleToolItem, SWT.TOGGLE | Cocoa.getStyle());
        btnNoTransparentSelectionPtr[0] = btnTransSelection;
        btnTransSelection.setToolTipText(I18n.E3D_TOGGLE_TRANSPARENT);
        btnTransSelection.setImage(ResourceManager.getImage("icon16_notrans.png")); //$NON-NLS-1$

        NButton btnBFCToggle = new NButton(miscToggleToolItem, SWT.TOGGLE | Cocoa.getStyle());
        btnBFCTogglePtr[0] = btnBFCToggle;
        btnBFCToggle.setToolTipText(I18n.E3D_TOGGLE_BFC);
        btnBFCToggle.setImage(ResourceManager.getImage("icon16_bfc.png")); //$NON-NLS-1$
    }

    private static void addListeners() {
        widgetUtil(btnMoveAdjacentDataPtr[0]).addSelectionListener(e -> {
            clickSingleBtn(btnMoveAdjacentDataPtr[0]);
            setMovingAdjacentData(btnMoveAdjacentDataPtr[0].getSelection());
            GuiStatusManager.updateStatus();
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
    }

    private static void regainFocus() {
        Editor3DWindow.getWindow().regainFocus();
    }
}
