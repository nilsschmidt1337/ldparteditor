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
package org.nschmidt.ldparteditor.dialogs.translate;

import static org.nschmidt.ldparteditor.helpers.WidgetUtility.WidgetUtil;

import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.composites.ToolItem;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.enums.ManipulatorScope;
import org.nschmidt.ldparteditor.enums.WorkingMode;
import org.nschmidt.ldparteditor.helpers.WidgetSelectionHelper;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;

/**
 *
 * <p>
 * Note: This class should be instantiated, it defines all listeners and part of
 * the business logic. It overrides the {@code open()} method to invoke the
 * listener definitions ;)
 *
 * @author nils
 *
 */
public class TranslateDialog extends TranslateDesign {

    private static Vertex manipulator = new Vertex(0f, 0f, 0f);
    private static Vertex offset = new Vertex(0f, 0f, 0f);
    private static boolean x = true;
    private static boolean y = true;
    private static boolean z = true;
    private static boolean creatingCopy = false;
    private static int iterations = 1;

    /**
     * Create the dialog.
     *
     * @param parentShell
     */
    public TranslateDialog(Shell parentShell, Vertex manipulatorPosition, ManipulatorScope scope) {
        super(parentShell, scope);
        x = true;
        y = true;
        z = true;
        creatingCopy = false;
        manipulator = manipulatorPosition;
    }

    @Override
    public int open() {
        super.create();
        // MARK All final listeners will be configured here..
        WidgetUtil(btn_Local[0]).addSelectionListener(e -> {
            WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btn_Local[0].getParent());
            btn_Local[0].setSelection(true);
            transformationMode = ManipulatorScope.LOCAL;
            Editor3DWindow.getWindow().setWorkingAction(WorkingMode.MOVE);
        });
        WidgetUtil(btn_Global[0]).addSelectionListener(e -> {
            WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btn_Global[0].getParent());
            btn_Global[0].setSelection(true);
            transformationMode = ManipulatorScope.GLOBAL;
            Editor3DWindow.getWindow().setWorkingAction(WorkingMode.MOVE_GLOBAL);
        });
        WidgetUtil(btn_ToManipulatorPosition[0]).addSelectionListener(e -> {
            WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btn_Global[0].getParent());
            btn_Global[0].setSelection(true);
            transformationMode = ManipulatorScope.GLOBAL;
            Editor3DWindow.getWindow().setWorkingAction(WorkingMode.MOVE_GLOBAL);
            spn_X[0].setValue(manipulator.X);
            spn_Y[0].setValue(manipulator.Y);
            spn_Z[0].setValue(manipulator.Z);
        });
        WidgetUtil(btn_ToManipulatorPositionInverted[0]).addSelectionListener(e -> {
            WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btn_Global[0].getParent());
            btn_Global[0].setSelection(true);
            transformationMode = ManipulatorScope.GLOBAL;
            Editor3DWindow.getWindow().setWorkingAction(WorkingMode.MOVE_GLOBAL);
            spn_X[0].setValue(manipulator.X.negate());
            spn_Y[0].setValue(manipulator.Y.negate());
            spn_Z[0].setValue(manipulator.Z.negate());
        });

        WidgetUtil(cb_Xaxis[0]).addSelectionListener(e -> x = cb_Xaxis[0].getSelection());
        WidgetUtil(cb_Yaxis[0]).addSelectionListener(e -> y = cb_Yaxis[0].getSelection());
        WidgetUtil(cb_Zaxis[0]).addSelectionListener(e -> z = cb_Zaxis[0].getSelection());
        spn_X[0].addValueChangeListener(spn -> setOffset(new Vertex(spn_X[0].getValue(), spn_Y[0].getValue(), spn_Z[0].getValue())));
        spn_Y[0].addValueChangeListener(spn -> setOffset(new Vertex(spn_X[0].getValue(), spn_Y[0].getValue(), spn_Z[0].getValue())));
        spn_Z[0].addValueChangeListener(spn -> setOffset(new Vertex(spn_X[0].getValue(), spn_Y[0].getValue(), spn_Z[0].getValue())));
        WidgetUtil(btn_Copy[0]).addSelectionListener(e -> {
            creatingCopy = true;
            setReturnCode(OK);
            close();
        });
        spn_Iterations[0].addValueChangeListener(spn -> iterations = spn.getValue());
        return super.open();
    }

    public static boolean isCreatingCopy() {
        return creatingCopy;
    }

    public static int getAndResetIterations() {
        int result = iterations;
        iterations = 1;
        return result;
    }

    public static boolean isZ() {
        return z;
    }

    public static void setZ(boolean z) {
        TranslateDialog.z = z;
    }

    public static boolean isY() {
        return y;
    }

    public static void setY(boolean y) {
        TranslateDialog.y = y;
    }

    public static boolean isX() {
        return x;
    }

    public static void setX(boolean x) {
        TranslateDialog.x = x;
    }

    public static Vertex getOffset() {
        return offset;
    }

    public static void setOffset(Vertex offset) {
        TranslateDialog.offset = offset;
    }

    public static ManipulatorScope getTransformationMode() {
        return transformationMode;
    }
}
