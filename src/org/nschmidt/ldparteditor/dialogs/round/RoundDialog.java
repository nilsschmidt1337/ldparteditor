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
package org.nschmidt.ldparteditor.dialogs.round;

import static org.nschmidt.ldparteditor.helpers.WidgetUtility.WidgetUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

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
public class RoundDialog extends RoundDesign {

    private boolean onX = WorkbenchManager.getUserSettingState().isRoundX();
    private boolean onY = WorkbenchManager.getUserSettingState().isRoundY();
    private boolean onZ = WorkbenchManager.getUserSettingState().isRoundZ();

    /**
     * Create the dialog.
     *
     * @param parentShell
     */
    public RoundDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    public int open() {
        super.create();
        // MARK All final listeners will be configured here..
        WidgetUtil(cb_Xaxis[0]).addSelectionListener(e -> onX = cb_Xaxis[0].getSelection());
        WidgetUtil(cb_Yaxis[0]).addSelectionListener(e -> onY = cb_Yaxis[0].getSelection());
        WidgetUtil(cb_Zaxis[0]).addSelectionListener(e -> onZ = cb_Zaxis[0].getSelection());
        btn_ok[0].addListener(SWT.Selection, event -> {
            WorkbenchManager.getUserSettingState().setCoordsPrecision(spn_coords[0].getValue());
            WorkbenchManager.getUserSettingState().setTransMatrixPrecision(spn_matrix[0].getValue());
            WorkbenchManager.getUserSettingState().setRoundX(onX);
            WorkbenchManager.getUserSettingState().setRoundY(onY);
            WorkbenchManager.getUserSettingState().setRoundZ(onZ);
        });
        return super.open();
    }
}
