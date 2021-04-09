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
package org.nschmidt.ldparteditor.dialogs.scale;

import static org.nschmidt.ldparteditor.helpers.WidgetUtility.widgetUtil;

import java.util.Set;

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
public class ScaleDialog extends ScaleDesign {

    private static Vertex scaleFactors = new Vertex(1f, 1f, 1f);
    private static Vertex pivot = new Vertex(0f, 0f, 0f);
    private static boolean x = true;
    private static boolean y = true;
    private static boolean z = true;
    private static boolean creatingCopy = false;

    /**
     * Create the dialog.
     *
     * @param parentShell
     */
    public ScaleDialog(Shell parentShell, Vertex v, Set<Vertex> clipboardVertices, Vertex manipulatorPosition, ManipulatorScope scope) {
        super(parentShell, v, clipboardVertices, manipulatorPosition, scope);
        x = true;
        y = true;
        z = true;
        creatingCopy = false;
        if (v == null) {
            setScaleFactors(new Vertex(1f, 1f, 1f));
        } else {
            setScaleFactors(new Vertex(v.xp, v.yp, v.zp));
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
            Editor3DWindow.getWindow().setWorkingAction(WorkingMode.MOVE);
        });
        widgetUtil(btnGlobalPtr[0]).addSelectionListener(e -> {
            WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btnGlobalPtr[0].getParent());
            btnGlobalPtr[0].setSelection(true);
            transformationMode = ManipulatorScope.GLOBAL;
            Editor3DWindow.getWindow().setWorkingAction(WorkingMode.MOVE_GLOBAL);
        });
        widgetUtil(cbXaxisPtr[0]).addSelectionListener(e -> x = cbXaxisPtr[0].getSelection());
        widgetUtil(cbYaxisPtr[0]).addSelectionListener(e -> y = cbYaxisPtr[0].getSelection());
        widgetUtil(cbZaxisPtr[0]).addSelectionListener(e -> z = cbZaxisPtr[0].getSelection());
        spnXPtr[0].addValueChangeListener(spn -> setScaleFactors(new Vertex(spnXPtr[0].getValue(), spnYPtr[0].getValue(), spnZPtr[0].getValue())));
        spnYPtr[0].addValueChangeListener(spn -> setScaleFactors(new Vertex(spnXPtr[0].getValue(), spnYPtr[0].getValue(), spnZPtr[0].getValue())));
        spnZPtr[0].addValueChangeListener(spn -> setScaleFactors(new Vertex(spnXPtr[0].getValue(), spnYPtr[0].getValue(), spnZPtr[0].getValue())));
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
        return super.open();
    }

    public static boolean isCreatingCopy() {
        return creatingCopy;
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

    public static Vertex getScaleFactors() {
        return scaleFactors;
    }

    private static void setScaleFactors(Vertex scaleFactors) {
        ScaleDialog.scaleFactors = scaleFactors;
    }

    public static Vertex getPivot() {
        return pivot;
    }

    private static void setPivot(Vertex pivot) {
        ScaleDialog.pivot = pivot;
    }

    public static ManipulatorScope getTransformationMode() {
        return transformationMode;
    }
}
