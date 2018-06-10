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

    static ManipulatorScope transformationMode = ManipulatorScope.GLOBAL;

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
            vertex = new Vertex(v.X, v.Y, v.Z);
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
        WidgetUtil(btn_Local[0]).addSelectionListener(e -> {
            WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btn_Local[0].getParent());
            btn_Local[0].setSelection(true);
            if (transformationMode != ManipulatorScope.LOCAL) {
                transformationMode = ManipulatorScope.LOCAL;
                Editor3DWindow.getWindow().setWorkingAction(WorkingMode.MOVE);
                vertex = globalToLocal(vertex);
            }
            updateXYZ();
        });
        WidgetUtil(btn_Global[0]).addSelectionListener(e -> {
            WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btn_Global[0].getParent());
            btn_Global[0].setSelection(true);
            if (transformationMode != ManipulatorScope.GLOBAL) {
                transformationMode = ManipulatorScope.GLOBAL;
                Editor3DWindow.getWindow().setWorkingAction(WorkingMode.MOVE_GLOBAL);
                vertex = localToGlobal(vertex);
            }
            updateXYZ();
        });
        WidgetUtil(cb_Xaxis[0]).addSelectionListener(e -> x = cb_Xaxis[0].getSelection());
        WidgetUtil(cb_Yaxis[0]).addSelectionListener(e -> y = cb_Yaxis[0].getSelection());
        WidgetUtil(cb_Zaxis[0]).addSelectionListener(e -> z = cb_Zaxis[0].getSelection());
        spn_X[0].addValueChangeListener(spn -> {
            vertex = new Vertex(spn_X[0].getValue(), spn_Y[0].getValue(), spn_Z[0].getValue());
            cb_Xaxis[0].setSelection(true);
            x = true;
        });
        spn_Y[0].addValueChangeListener(spn -> {
            vertex = new Vertex(spn_X[0].getValue(), spn_Y[0].getValue(), spn_Z[0].getValue());
            cb_Yaxis[0].setSelection(true);
            y = true;
        });
        spn_Z[0].addValueChangeListener(spn -> {
            vertex = new Vertex(spn_X[0].getValue(), spn_Y[0].getValue(), spn_Z[0].getValue());
            cb_Zaxis[0].setSelection(true);
            z = true;
        });
        WidgetUtil(btn_Manipulator[0]).addSelectionListener(e -> {
            if (transformationMode == ManipulatorScope.GLOBAL) {
                vertex = new Vertex(m.X, m.Y, m.Z);
            } else {
                vertex = globalToLocal(new Vertex(m.X, m.Y, m.Z));
            }
            updateXYZ();
        });
        WidgetUtil(btn_Clipboard[0]).addSelectionListener(e -> {
            if (transformationMode == ManipulatorScope.GLOBAL) {
                vertex = new Vertex(c.X, c.Y, c.Z);
            } else {
                vertex = globalToLocal(new Vertex(c.X, c.Y, c.Z));
            }
            updateXYZ();
        });
        WidgetUtil(btn_Copy[0]).addSelectionListener(e -> {
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
        Vector3d v2 = new Vector3d(vertex.X, vertex.Y, vertex.Z);
        spn_X[0].setValue(v2.X);
        spn_Y[0].setValue(v2.Y);
        spn_Z[0].setValue(v2.Z);
    }
}
