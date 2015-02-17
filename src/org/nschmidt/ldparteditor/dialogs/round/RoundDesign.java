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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.widgets.IntegerSpinner;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * The rounding precision dialog
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 *
 * @author nils
 *
 */
class RoundDesign extends Dialog {

    // Use final only for subclass/listener references!
    final Button[] btn_ok = new Button[1];
    final IntegerSpinner[] spn_coords = new IntegerSpinner[1];
    final IntegerSpinner[] spn_matrix = new IntegerSpinner[1];

    RoundDesign(Shell parentShell) {
        super(parentShell);
    }

    /**
     * Create contents of the dialog.
     *
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite cmp_container = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = (GridLayout) cmp_container.getLayout();
        gridLayout.verticalSpacing = 10;
        gridLayout.horizontalSpacing = 10;

        Label lbl_specify = new Label(cmp_container, SWT.NONE);
        lbl_specify.setText("Specify the Rounding Precision"); //$NON-NLS-1$ I18N Needs translation!

        Label lbl_separator = new Label(cmp_container, SWT.SEPARATOR | SWT.HORIZONTAL);
        lbl_separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label lbl_coordsPrec = new Label(cmp_container, SWT.NONE);
        lbl_coordsPrec.setText("Coordinate Precision"); //$NON-NLS-1$ I18N Needs translation!

        IntegerSpinner spn_coords = new IntegerSpinner(cmp_container, SWT.NONE);
        this.spn_coords[0] = spn_coords;
        spn_coords.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spn_coords.setMaximum(4);
        spn_coords.setMinimum(0);
        spn_coords.setValue(WorkbenchManager.getUserSettingState().getCoordsPrecision());

        Label lbl_matrixPrec = new Label(cmp_container, SWT.NONE);
        lbl_matrixPrec.setText("Transformation Matrix Precision"); //$NON-NLS-1$ I18N Needs translation!

        IntegerSpinner spn_matrix = new IntegerSpinner(cmp_container, SWT.NONE);
        this.spn_matrix[0] = spn_matrix;
        spn_matrix.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spn_matrix.setMaximum(5);
        spn_matrix.setMinimum(0);
        spn_matrix.setValue(WorkbenchManager.getUserSettingState().getTransMatrixPrecision());

        Label lbl_unit = new Label(cmp_container, SWT.NONE);
        lbl_unit.setText("(in Decimal Places)"); //$NON-NLS-1$ I18N Needs translation!

        cmp_container.pack();
        return cmp_container;
    }

    /**
     * Create contents of the button bar.
     *
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        btn_ok[0] = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return super.getInitialSize();
    }

}
