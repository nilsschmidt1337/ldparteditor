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

import static org.nschmidt.ldparteditor.helpers.WidgetUtility.WidgetUtil;

import java.util.Set;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.composites.ToolItem;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.enums.ManipulatorScope;
import org.nschmidt.ldparteditor.enums.WorkingMode;
import org.nschmidt.ldparteditor.helpers.WidgetSelectionHelper;
import org.nschmidt.ldparteditor.helpers.WidgetSelectionListener;
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
            setScaleFactors(new Vertex(v.X, v.Y, v.Z));
        }
        if (clipboardVertices.size() == 1) {
            p = clipboardVertices.iterator().next();
            c = new Vertex(p.X, p.Y, p.Z);
        } else if (transformationMode == ManipulatorScope.LOCAL && manipulatorPosition != null) {
            pivot = new Vertex(m.X, m.Y, m.Z);
        }
    }

    @Override
    public int open() {
        super.create();
        // MARK All final listeners will be configured here..
        WidgetUtil(btn_Local[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btn_Local[0].getParent());
                btn_Local[0].setSelection(true);
                transformationMode = ManipulatorScope.LOCAL;
                Editor3DWindow.getWindow().setWorkingAction(WorkingMode.MOVE);
            }
        });
        WidgetUtil(btn_Global[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btn_Global[0].getParent());
                btn_Global[0].setSelection(true);
                transformationMode = ManipulatorScope.GLOBAL;
                Editor3DWindow.getWindow().setWorkingAction(WorkingMode.MOVE_GLOBAL);
            }
        });
        WidgetUtil(cb_Xaxis[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                x = cb_Xaxis[0].getSelection();
            }
        });
        WidgetUtil(cb_Yaxis[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                y = cb_Yaxis[0].getSelection();
            }
        });
        WidgetUtil(cb_Zaxis[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                z = cb_Zaxis[0].getSelection();
            }
        });
        spn_X[0].addValueChangeListener(spn -> setScaleFactors(new Vertex(spn_X[0].getValue(), spn_Y[0].getValue(), spn_Z[0].getValue())));
        spn_Y[0].addValueChangeListener(spn -> setScaleFactors(new Vertex(spn_X[0].getValue(), spn_Y[0].getValue(), spn_Z[0].getValue())));
        spn_Z[0].addValueChangeListener(spn -> setScaleFactors(new Vertex(spn_X[0].getValue(), spn_Y[0].getValue(), spn_Z[0].getValue())));
        spn_pX[0].addValueChangeListener(spn -> setPivot(new Vertex(spn_pX[0].getValue(), spn_pY[0].getValue(), spn_pZ[0].getValue())));
        spn_pY[0].addValueChangeListener(spn -> setPivot(new Vertex(spn_pX[0].getValue(), spn_pY[0].getValue(), spn_pZ[0].getValue())));
        spn_pZ[0].addValueChangeListener(spn -> setPivot(new Vertex(spn_pX[0].getValue(), spn_pY[0].getValue(), spn_pZ[0].getValue())));
        WidgetUtil(btn_PivotManipulator[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                spn_pX[0].setValue(m.X);
                spn_pY[0].setValue(m.Y);
                spn_pZ[0].setValue(m.Z);
            }
        });
        WidgetUtil(btn_PivotClipboard[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                spn_pX[0].setValue(c.X);
                spn_pY[0].setValue(c.Y);
                spn_pZ[0].setValue(c.Z);
            }
        });
        WidgetUtil(btn_Copy[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                creatingCopy = true;
                setReturnCode(OK);
                close();
            }
        });
        return super.open();
    }

    public static boolean isCreatingCopy() {
        return creatingCopy;
    }

    public static boolean isZ() {
        return z;
    }

    public static void setZ(boolean z) {
        ScaleDialog.z = z;
    }

    public static boolean isY() {
        return y;
    }

    public static void setY(boolean y) {
        ScaleDialog.y = y;
    }

    public static boolean isX() {
        return x;
    }

    public static void setX(boolean x) {
        ScaleDialog.x = x;
    }

    public static Vertex getScaleFactors() {
        return scaleFactors;
    }

    public static void setScaleFactors(Vertex scaleFactors) {
        ScaleDialog.scaleFactors = scaleFactors;
    }

    public static Vertex getPivot() {
        return pivot;
    }

    public static void setPivot(Vertex pivot) {
        ScaleDialog.pivot = pivot;
    }

    public static ManipulatorScope getTransformationMode() {
        return transformationMode;
    }
}
