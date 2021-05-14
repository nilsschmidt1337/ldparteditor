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
package org.nschmidt.ldparteditor.dialog.rotate;

import static org.nschmidt.ldparteditor.helper.WidgetUtility.widgetUtil;

import java.util.Set;

import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.composite.ToolItem;
import org.nschmidt.ldparteditor.data.Vertex;
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
 *
 * @author nils
 *
 */
public class RotateDialog extends RotateDesign {

    private static Vertex angles = new Vertex(0f, 0f, 0f);
    private static Vertex pivot = new Vertex(0f, 0f, 0f);
    private static boolean x = true;
    private static boolean y = false;
    private static boolean z = false;
    private static boolean creatingCopy = false;
    private static int iterations = 1;

    /**
     * Create the dialog.
     *
     * @param parentShell
     */
    public RotateDialog(Shell parentShell, Vertex v, Set<Vertex> clipboardVertices, Vertex manipulatorPosition, ManipulatorScope scope) {
        super(parentShell, v, clipboardVertices, manipulatorPosition, scope);
        x = true;
        y = false;
        z = false;
        creatingCopy = false;
        if (v == null) {
            setAngles(new Vertex(0f, 0f, 0f));
        } else {
            setAngles(new Vertex(v.xp, v.yp, v.zp));
        }
        if (clipboardVertices.size() == 1) {
            p = clipboardVertices.iterator().next();
            c = new Vertex(p.xp, p.yp, p.zp);
        } else if (transformationMode == ManipulatorScope.LOCAL && manipulatorPosition != null) {
            pivot = new Vertex(m.xp, m.yp, m.zp);
        }
    }

    @Override
    public int open() {
        super.create();
        // MARK All final listeners will be configured here..
        widgetUtil(btnLocalPtr[0]).addSelectionListener(e -> {
            WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btnLocalPtr[0].getParent());
            btnLocalPtr[0].setSelection(true);
            transformationMode = ManipulatorScope.LOCAL;
            TransformationModeToolItem.setWorkingAction(WorkingMode.MOVE);
        });
        widgetUtil(btnGlobalPtr[0]).addSelectionListener(e -> {
            WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btnGlobalPtr[0].getParent());
            btnGlobalPtr[0].setSelection(true);
            transformationMode = ManipulatorScope.GLOBAL;
            TransformationModeToolItem.setWorkingAction(WorkingMode.MOVE_GLOBAL);
        });
        widgetUtil(rbXaxisPtr[0]).addSelectionListener(e -> {
            if (rbXaxisPtr[0].getSelection()) {
                x = true;
                y = false;
                z = false;
            }
        });
        widgetUtil(rbYaxisPtr[0]).addSelectionListener(e -> {
            if (rbYaxisPtr[0].getSelection()) {
                x = false;
                y = true;
                z = false;
            }
        });
        widgetUtil(rbZaxisPtr[0]).addSelectionListener(e -> {
            if (rbZaxisPtr[0].getSelection()) {
                x = false;
                y = false;
                z = true;
            }
        });
        spnXPtr[0].addValueChangeListener(spn -> {
            setAngles(new Vertex(spnXPtr[0].getValue(), spnYPtr[0].getValue(), spnZPtr[0].getValue()));
            rbXaxisPtr[0].setSelection(true);
            rbYaxisPtr[0].setSelection(false);
            rbZaxisPtr[0].setSelection(false);
            x = true;
            y = false;
            z = false;
        });
        spnYPtr[0].addValueChangeListener(spn -> {
            setAngles(new Vertex(spnXPtr[0].getValue(), spnYPtr[0].getValue(), spnZPtr[0].getValue()));
            rbXaxisPtr[0].setSelection(false);
            rbYaxisPtr[0].setSelection(true);
            rbZaxisPtr[0].setSelection(false);
            x = false;
            y = true;
            z = false;
        });
        spnZPtr[0].addValueChangeListener(spn -> {
            setAngles(new Vertex(spnXPtr[0].getValue(), spnYPtr[0].getValue(), spnZPtr[0].getValue()));
            rbXaxisPtr[0].setSelection(false);
            rbYaxisPtr[0].setSelection(false);
            rbZaxisPtr[0].setSelection(true);
            x = false;
            y = false;
            z = true;
        });
        spnPXPtr[0].addValueChangeListener(spn -> setPivot(new Vertex(spnPXPtr[0].getValue(), spnPYPtr[0].getValue(), spnPZPtr[0].getValue())));
        spnPYPtr[0].addValueChangeListener(spn -> setPivot(new Vertex(spnPXPtr[0].getValue(), spnPYPtr[0].getValue(), spnPZPtr[0].getValue())));
        spnPZPtr[0].addValueChangeListener(spn -> setPivot(new Vertex(spnPXPtr[0].getValue(), spnPYPtr[0].getValue(), spnPZPtr[0].getValue())));
        widgetUtil(btnPivotManipulatorPtr[0]).addSelectionListener(e -> {
            spnPXPtr[0].setValue(m.xp);
            spnPYPtr[0].setValue(m.yp);
            spnPZPtr[0].setValue(m.zp);
        });
        widgetUtil(btnPivotClipboardPtr[0]).addSelectionListener(e -> {
            spnPXPtr[0].setValue(c.xp);
            spnPYPtr[0].setValue(c.yp);
            spnPZPtr[0].setValue(c.zp);
        });
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

    public static Vertex getAngles() {
        return angles;
    }

    private static void setAngles(Vertex angles) {
        RotateDialog.angles = angles;
    }

    public static Vertex getPivot() {
        return pivot;
    }

    private static void setPivot(Vertex pivot) {
        RotateDialog.pivot = pivot;
    }

    public static ManipulatorScope getTransformationMode() {
        return transformationMode;
    }
}
