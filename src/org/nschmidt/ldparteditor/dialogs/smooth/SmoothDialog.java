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
package org.nschmidt.ldparteditor.dialogs.smooth;

import static org.nschmidt.ldparteditor.helpers.WidgetUtility.widgetUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.project.Project;

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
public class SmoothDialog extends SmoothDesign {

    private static boolean x = true;
    private static boolean y = true;
    private static boolean z = true;
    private static int iterations = 1;
    private static BigDecimal factor = BigDecimal.ONE;

    /**
     * Create the dialog.
     *
     * @param parentShell
     */
    public SmoothDialog(Shell parentShell) {
        super(parentShell);
        x = true;
        y = true;
        z = true;
        iterations = 1;
        factor = BigDecimal.ONE;
    }

    @Override
    public int open() {
        super.create();

        {
            final VertexManager vm = Project.getFileToEdit().getVertexManager();
            List<Vertex> selectedVerts = new ArrayList<>();
            selectedVerts.addAll(vm.getSelectedVertices());
            Project.getFileToEdit().getVertexManager().clearSelection();
            if (selectedVerts.isEmpty()) {
                selectedVerts.addAll(vm.getVertices());
            }
            vm.getSelectedVertices().addAll(selectedVerts);
        }

        // MARK All final listeners will be configured here..
        widgetUtil(cbXaxisPtr[0]).addSelectionListener(e -> x = cbXaxisPtr[0].getSelection());
        widgetUtil(cbYaxisPtr[0]).addSelectionListener(e -> y = cbYaxisPtr[0].getSelection());
        widgetUtil(cbZaxisPtr[0]).addSelectionListener(e -> z = cbZaxisPtr[0].getSelection());
        spnPXPtr[0].addValueChangeListener(spn -> iterations = spn.getValue());
        spnPYPtr[0].addValueChangeListener(spn -> factor = spn.getValue());
        return super.open();
    }

    public static boolean isZ() {
        return z;
    }

    public static void setZ(boolean z) {
        SmoothDialog.z = z;
    }

    public static boolean isY() {
        return y;
    }

    public static void setY(boolean y) {
        SmoothDialog.y = y;
    }

    public static boolean isX() {
        return x;
    }

    public static void setX(boolean x) {
        SmoothDialog.x = x;
    }

    public static int getIterations() {
        return iterations;
    }

    public static void setIterations(int iterations2) {
        iterations = iterations2;

    }

    public static BigDecimal getFactor() {
        return factor;
    }

    public static void setFactor(BigDecimal factor2) {
        factor = factor2;
    }
}
