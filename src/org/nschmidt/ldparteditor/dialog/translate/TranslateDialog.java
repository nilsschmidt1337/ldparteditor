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
package org.nschmidt.ldparteditor.dialog.translate;

import static org.nschmidt.ldparteditor.helper.WidgetUtility.widgetUtil;

import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.composite.ToolItem;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.enumtype.ManipulatorScope;
import org.nschmidt.ldparteditor.enumtype.WorkingMode;
import org.nschmidt.ldparteditor.helper.WidgetSelectionHelper;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;

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
        widgetUtil(btnLocalPtr[0]).addSelectionListener(e -> {
            WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btnLocalPtr[0].getParent());
            btnLocalPtr[0].setSelection(true);
            transformationMode = ManipulatorScope.LOCAL;
            Editor3DWindow.getWindow().setWorkingAction(WorkingMode.MOVE);
        });
        widgetUtil(btnGlobalPtr[0]).addSelectionListener(e -> {
            WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btnGlobalPtr[0].getParent());
            btnGlobalPtr[0].setSelection(true);
            transformationMode = ManipulatorScope.GLOBAL;
            Editor3DWindow.getWindow().setWorkingAction(WorkingMode.MOVE_GLOBAL);
        });
        widgetUtil(btnToManipulatorPositionPtr[0]).addSelectionListener(e -> {
            WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btnGlobalPtr[0].getParent());
            btnGlobalPtr[0].setSelection(true);
            transformationMode = ManipulatorScope.GLOBAL;
            Editor3DWindow.getWindow().setWorkingAction(WorkingMode.MOVE_GLOBAL);
            spnXPtr[0].setValue(manipulator.xp);
            spnYPtr[0].setValue(manipulator.yp);
            spnZPtr[0].setValue(manipulator.zp);
        });
        widgetUtil(btnToManipulatorPositionInvertedPtr[0]).addSelectionListener(e -> {
            WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btnGlobalPtr[0].getParent());
            btnGlobalPtr[0].setSelection(true);
            transformationMode = ManipulatorScope.GLOBAL;
            Editor3DWindow.getWindow().setWorkingAction(WorkingMode.MOVE_GLOBAL);
            spnXPtr[0].setValue(manipulator.xp.negate());
            spnYPtr[0].setValue(manipulator.yp.negate());
            spnZPtr[0].setValue(manipulator.zp.negate());
        });

        widgetUtil(cbXaxisPtr[0]).addSelectionListener(e -> x = cbXaxisPtr[0].getSelection());
        widgetUtil(cbYaxisPtr[0]).addSelectionListener(e -> y = cbYaxisPtr[0].getSelection());
        widgetUtil(cbZaxisPtr[0]).addSelectionListener(e -> z = cbZaxisPtr[0].getSelection());
        spnXPtr[0].addValueChangeListener(spn -> setOffset(new Vertex(spnXPtr[0].getValue(), spnYPtr[0].getValue(), spnZPtr[0].getValue())));
        spnYPtr[0].addValueChangeListener(spn -> setOffset(new Vertex(spnXPtr[0].getValue(), spnYPtr[0].getValue(), spnZPtr[0].getValue())));
        spnZPtr[0].addValueChangeListener(spn -> setOffset(new Vertex(spnXPtr[0].getValue(), spnYPtr[0].getValue(), spnZPtr[0].getValue())));
        widgetUtil(btnCopyPtr[0]).addSelectionListener(e -> {
            creatingCopy = true;
            setReturnCode(OK);
            close();
        });
        spnIterationsPtr[0].addValueChangeListener(spn -> iterations = spn.getValue());
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

    public static boolean isY() {
        return y;
    }

    public static boolean isX() {
        return x;
    }

    public static Vertex getOffset() {
        return offset;
    }

    private static void setOffset(Vertex offset) {
        TranslateDialog.offset = offset;
    }

    public static ManipulatorScope getTransformationMode() {
        return transformationMode;
    }
}
