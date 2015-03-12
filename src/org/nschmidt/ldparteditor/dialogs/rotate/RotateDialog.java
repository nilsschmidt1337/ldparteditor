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
package org.nschmidt.ldparteditor.dialogs.rotate;

import java.util.Set;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.data.Vertex;
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
public class RotateDialog extends RotateDesign {

    private static Vertex angles = new Vertex(0f, 0f, 0f);
    private static Vertex pivot = new Vertex(0f, 0f, 0f);
    private static boolean x = true;
    private static boolean y = false;
    private static boolean z = false;

    /**
     * Create the dialog.
     *
     * @param parentShell
     */
    public RotateDialog(Shell parentShell, Vertex v, Set<Vertex> clipboardVertices) {
        super(parentShell, v, clipboardVertices);
        x = true;
        y = false;
        z = false;
        if (v == null) {
            setAngles(new Vertex(0f, 0f, 0f));
        } else {
            setAngles(new Vertex(v.X, v.Y, v.Z));
        }
        if (clipboardVertices.size() == 1) {
            p = clipboardVertices.iterator().next();
        }
    }

    @Override
    public int open() {
        super.create();
        // MARK All final listeners will be configured here..
        rb_Xaxis[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (rb_Xaxis[0].getSelection()) {
                    x = true;
                    y = false;
                    z = false;
                }
            }
        });
        rb_Yaxis[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (rb_Yaxis[0].getSelection()) {
                    x = false;
                    y = true;
                    z = false;
                }
            }
        });
        rb_Zaxis[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (rb_Zaxis[0].getSelection()) {
                    x = false;
                    y = false;
                    z = true;
                }
            }
        });
        spn_X[0].addValueChangeListener(new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
                setAngles(new Vertex(spn_X[0].getValue(), spn_Y[0].getValue(), spn_Z[0].getValue()));
                rb_Xaxis[0].setSelection(true);
                rb_Yaxis[0].setSelection(false);
                rb_Zaxis[0].setSelection(false);
                x = true;
                y = false;
                z = false;
            }
        });
        spn_Y[0].addValueChangeListener(new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
                setAngles(new Vertex(spn_X[0].getValue(), spn_Y[0].getValue(), spn_Z[0].getValue()));
                rb_Xaxis[0].setSelection(false);
                rb_Yaxis[0].setSelection(true);
                rb_Zaxis[0].setSelection(false);
                x = false;
                y = true;
                z = false;
            }
        });
        spn_Z[0].addValueChangeListener(new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
                setAngles(new Vertex(spn_X[0].getValue(), spn_Y[0].getValue(), spn_Z[0].getValue()));
                rb_Xaxis[0].setSelection(false);
                rb_Yaxis[0].setSelection(false);
                rb_Zaxis[0].setSelection(true);
                x = false;
                y = false;
                z = true;
            }
        });
        spn_pX[0].addValueChangeListener(new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
                setPivot(new Vertex(spn_pX[0].getValue(), spn_pY[0].getValue(), spn_pZ[0].getValue()));
            }
        });
        spn_pY[0].addValueChangeListener(new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
                setPivot(new Vertex(spn_pX[0].getValue(), spn_pY[0].getValue(), spn_pZ[0].getValue()));
            }
        });
        spn_pZ[0].addValueChangeListener(new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
                setPivot(new Vertex(spn_pX[0].getValue(), spn_pY[0].getValue(), spn_pZ[0].getValue()));
            }
        });
        return super.open();
    }

    public static boolean isZ() {
        return z;
    }

    public static void setZ(boolean z) {
        RotateDialog.z = z;
    }

    public static boolean isY() {
        return y;
    }

    public static void setY(boolean y) {
        RotateDialog.y = y;
    }

    public static boolean isX() {
        return x;
    }

    public static void setX(boolean x) {
        RotateDialog.x = x;
    }

    public static Vertex getAngles() {
        return angles;
    }

    public static void setAngles(Vertex angles) {
        RotateDialog.angles = angles;
    }

    public static Vertex getPivot() {
        return pivot;
    }

    public static void setPivot(Vertex pivot) {
        RotateDialog.pivot = pivot;
    }
}
