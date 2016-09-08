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

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.widgets.BigDecimalSpinner;
import org.nschmidt.ldparteditor.widgets.ValueChangeAdapter;

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

    private static Vector3d start = null;
    private static Vector3d end = null;

    private static Vertex vertex = new Vertex(0f, 0f, 0f);
    private static boolean x = false;
    private static boolean y = false;
    private static boolean z = false;

    /**
     * Create the dialog.
     *
     * @param parentShell
     */
    public CoordinatesDialog(Shell parentShell, Vertex v, Vertex manipulatorPosition) {
        super(parentShell, v, manipulatorPosition);
        x = false;
        y = false;
        z = false;
        if (v == null) {
            vertex = new Vertex(0f, 0f, 0f);
        } else {
            vertex = new Vertex(v.X, v.Y, v.Z);
        }
    }

    @Override
    public int open() {
        super.create();
        // MARK All final listeners will be configured here..
        cb_Xaxis[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                x = cb_Xaxis[0].getSelection();
            }
        });
        cb_Yaxis[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                y = cb_Yaxis[0].getSelection();
            }
        });
        cb_Zaxis[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                z = cb_Zaxis[0].getSelection();
            }
        });
        spn_X[0].addValueChangeListener(new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
                vertex = new Vertex(spn_X[0].getValue(), spn_Y[0].getValue(), spn_Z[0].getValue());
                cb_Xaxis[0].setSelection(true);
                x = true;
            }
        });
        spn_Y[0].addValueChangeListener(new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
                vertex = new Vertex(spn_X[0].getValue(), spn_Y[0].getValue(), spn_Z[0].getValue());
                cb_Yaxis[0].setSelection(true);
                y = true;
            }
        });
        spn_Z[0].addValueChangeListener(new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
                vertex = new Vertex(spn_X[0].getValue(), spn_Y[0].getValue(), spn_Z[0].getValue());
                cb_Zaxis[0].setSelection(true);
                z = true;
            }
        });
        btn_Manipulator[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                spn_X[0].setValue(m.X);
                spn_Y[0].setValue(m.Y);
                spn_Z[0].setValue(m.Z);
            }
        });
        btn_Clipboard[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                spn_X[0].setValue(c.X);
                spn_Y[0].setValue(c.Y);
                spn_Z[0].setValue(c.Z);
            }
        });
        return super.open();
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
}
