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
package org.nschmidt.ldparteditor.dialogs.setcoordinates;

import static org.nschmidt.ldparteditor.helpers.WidgetUtility.WidgetUtil;

import java.math.BigDecimal;

import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.composites.ToolItem;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.enums.ManipulatorScope;
import org.nschmidt.ldparteditor.enums.WorkingMode;
import org.nschmidt.ldparteditor.helpers.Manipulator;
import org.nschmidt.ldparteditor.helpers.WidgetSelectionHelper;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
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
public class CoordinatesDialog extends CoordinatesDesign {

    private static ManipulatorScope transformationMode = ManipulatorScope.GLOBAL;

    private static Vector3d start = null;
    private static Vector3d end = null;

    private static Vertex vertex = new Vertex(0f, 0f, 0f);
    private static boolean x = false;
    private static boolean y = false;
    private static boolean z = false;
    private static boolean creatingCopy = false;

    private final Manipulator mani;

    /**
     * Create the dialog.
     *
     * @param parentShell
     */
    public CoordinatesDialog(Shell parentShell, Vertex v, Vertex manipulatorPosition, Manipulator mani) {
        super(parentShell, v, manipulatorPosition);
        x = false;
        y = false;
        z = false;
        creatingCopy = false;
        if (v == null) {
            vertex = new Vertex(0f, 0f, 0f);
        } else {
            vertex = new Vertex(v.xp, v.yp, v.zp);
        }
        this.mani = mani;
        if (transformationMode == ManipulatorScope.LOCAL) {
            vertex = globalToLocal(vertex);
        }
    }

    @Override
    public int open() {
        super.create();
        // MARK All final listeners will be configured here..
        WidgetUtil(btnLocalPtr[0]).addSelectionListener(e -> {
            WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btnLocalPtr[0].getParent());
            btnLocalPtr[0].setSelection(true);
            if (transformationMode != ManipulatorScope.LOCAL) {
                transformationMode = ManipulatorScope.LOCAL;
                Editor3DWindow.getWindow().setWorkingAction(WorkingMode.MOVE);
                vertex = globalToLocal(vertex);
            }
            updateXYZ();
        });
        WidgetUtil(btnGlobalPtr[0]).addSelectionListener(e -> {
            WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btnGlobalPtr[0].getParent());
            btnGlobalPtr[0].setSelection(true);
            if (transformationMode != ManipulatorScope.GLOBAL) {
                transformationMode = ManipulatorScope.GLOBAL;
                Editor3DWindow.getWindow().setWorkingAction(WorkingMode.MOVE_GLOBAL);
                vertex = localToGlobal(vertex);
            }
            updateXYZ();
        });
        WidgetUtil(cbXaxisPtr[0]).addSelectionListener(e -> x = cbXaxisPtr[0].getSelection());
        WidgetUtil(cbYaxisPtr[0]).addSelectionListener(e -> y = cbYaxisPtr[0].getSelection());
        WidgetUtil(cbZaxisPtr[0]).addSelectionListener(e -> z = cbZaxisPtr[0].getSelection());
        spnXPtr[0].addValueChangeListener(spn -> {
            vertex = new Vertex(spnXPtr[0].getValue(), spnYPtr[0].getValue(), spnZPtr[0].getValue());
            cbXaxisPtr[0].setSelection(true);
            x = true;
        });
        spnYPtr[0].addValueChangeListener(spn -> {
            vertex = new Vertex(spnXPtr[0].getValue(), spnYPtr[0].getValue(), spnZPtr[0].getValue());
            cbYaxisPtr[0].setSelection(true);
            y = true;
        });
        spnZPtr[0].addValueChangeListener(spn -> {
            vertex = new Vertex(spnXPtr[0].getValue(), spnYPtr[0].getValue(), spnZPtr[0].getValue());
            cbZaxisPtr[0].setSelection(true);
            z = true;
        });
        WidgetUtil(btnManipulatorPtr[0]).addSelectionListener(e -> {
            if (transformationMode == ManipulatorScope.GLOBAL) {
                vertex = new Vertex(m.xp, m.yp, m.zp);
            } else {
                vertex = globalToLocal(new Vertex(m.xp, m.yp, m.zp));
            }
            updateXYZ();
        });
        WidgetUtil(btnClipboardPtr[0]).addSelectionListener(e -> {
            if (transformationMode == ManipulatorScope.GLOBAL) {
                vertex = new Vertex(c.xp, c.yp, c.zp);
            } else {
                vertex = globalToLocal(new Vertex(c.xp, c.yp, c.zp));
            }
            updateXYZ();
        });
        WidgetUtil(btnCopyPtr[0]).addSelectionListener(e -> {
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

    public static void setZ(boolean z) {
        CoordinatesDialog.z = z;
    }

    public static boolean isY() {
        return y;
    }

    public static void setY(boolean y) {
        CoordinatesDialog.y = y;
    }

    public static boolean isX() {
        return x;
    }

    public static void setX(boolean x) {
        CoordinatesDialog.x = x;
    }

    public static Vertex getVertex() {
        return vertex;
    }

    public static void setVertex(Vertex vertex) {
        CoordinatesDialog.vertex = vertex;
    }

    public static Vector3d getStart() {
        return start;
    }

    public static void setStart(Vector3d start) {
        CoordinatesDialog.start = start;
    }

    public static Vector3d getEnd() {
        return end;
    }

    public static void setEnd(Vector3d end) {
        CoordinatesDialog.end = end;
    }

    public static ManipulatorScope getTransformationMode() {
        return transformationMode;
    }

    private Vertex globalToLocal(Vertex vert) {
        BigDecimal[] pos = mani.getAccuratePosition();
        return new Vertex(mani.getAccurateRotation().transform(Vector3d.sub(new Vector3d(vert), new Vector3d(pos[0], pos[1], pos[2]))));
    }

    private Vertex localToGlobal(Vertex vert) {
        BigDecimal[] pos = mani.getAccuratePosition();
        return new Vertex(Vector3d.add(mani.getAccurateRotation().invert().transform(new Vector3d(vert)), new Vector3d(pos[0], pos[1], pos[2])));
    }

    private void updateXYZ() {
        Vector3d v2 = new Vector3d(vertex.xp, vertex.yp, vertex.zp);
        spnXPtr[0].setValue(v2.x);
        spnYPtr[0].setValue(v2.y);
        spnZPtr[0].setValue(v2.z);
    }
}
