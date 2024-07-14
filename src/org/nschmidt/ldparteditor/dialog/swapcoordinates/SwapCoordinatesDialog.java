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
package org.nschmidt.ldparteditor.dialog.swapcoordinates;

import static org.nschmidt.ldparteditor.helper.WidgetUtility.widgetUtil;

import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.composite.ToolItem;
import org.nschmidt.ldparteditor.enumtype.ManipulatorAxisMode;
import org.nschmidt.ldparteditor.enumtype.ManipulatorScope;
import org.nschmidt.ldparteditor.enumtype.WorkingMode;
import org.nschmidt.ldparteditor.helper.WidgetSelectionHelper;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.TransformationModeToolItem;

/**
 *
 * <p>
 * Note: This class should be instantiated, it defines all listeners and part of
 * the business logic. It overrides the {@code open()} method to invoke the
 * listener definitions ;)
 */
public class SwapCoordinatesDialog extends SwapCoordinatesDesign {

    private static ManipulatorAxisMode axisMode = ManipulatorAxisMode.XY;

    private static boolean creatingCopy = false;

    /**
     * Create the dialog.
     *
     * @param parentShell
     */
    public SwapCoordinatesDialog(Shell parentShell) {
        super(parentShell);
        creatingCopy = false;
        axisMode = ManipulatorAxisMode.XY;
    }

    @Override
    public int open() {
        super.create();
        // MARK All final listeners will be configured here..
        widgetUtil(btnLocalPtr[0]).addSelectionListener(e -> {
            WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btnLocalPtr[0].getParent());
            btnLocalPtr[0].setSelection(true);
            if (transformationMode != ManipulatorScope.LOCAL) {
                transformationMode = ManipulatorScope.LOCAL;
                TransformationModeToolItem.setWorkingAction(WorkingMode.MOVE);
            }
        });
        widgetUtil(btnGlobalPtr[0]).addSelectionListener(e -> {
            WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btnGlobalPtr[0].getParent());
            btnGlobalPtr[0].setSelection(true);
            if (transformationMode != ManipulatorScope.GLOBAL) {
                transformationMode = ManipulatorScope.GLOBAL;
                TransformationModeToolItem.setWorkingAction(WorkingMode.MOVE_GLOBAL);
            }
        });
        widgetUtil(btnSwapXYPtr[0]).addSelectionListener(e -> {
            if (!btnSwapXYPtr[0].getSelection()) return;
            btnSwapXYPtr[0].setSelection(true);
            btnSwapXZPtr[0].setSelection(false);
            btnSwapYZPtr[0].setSelection(false);
            axisMode = ManipulatorAxisMode.XY;
        });
        widgetUtil(btnSwapXZPtr[0]).addSelectionListener(e -> {
            if (!btnSwapXZPtr[0].getSelection()) return;
            btnSwapXYPtr[0].setSelection(false);
            btnSwapXZPtr[0].setSelection(true);
            btnSwapYZPtr[0].setSelection(false);
            axisMode = ManipulatorAxisMode.XZ;
        });
        widgetUtil(btnSwapYZPtr[0]).addSelectionListener(e -> {
            if (!btnSwapYZPtr[0].getSelection()) return;
            btnSwapXYPtr[0].setSelection(false);
            btnSwapXZPtr[0].setSelection(false);
            btnSwapYZPtr[0].setSelection(true);
            axisMode = ManipulatorAxisMode.YZ;
        });
        widgetUtil(btnCopyPtr[0]).addSelectionListener(e -> {
            creatingCopy = true;
            setReturnCode(OK);
            close();
        });
        return super.open();
    }

    public static boolean isCreatingCopy() {
        return creatingCopy;
    }

    public static ManipulatorScope getTransformationMode() {
        return transformationMode;
    }

    public static ManipulatorAxisMode getAxisMode() {
        return axisMode;
    }
}
