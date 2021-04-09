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
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.widgets.IntegerSpinner;
import org.nschmidt.ldparteditor.widgets.NButton;
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
    final Button[] btnOkPtr = new Button[1];
    final NButton[] cbXaxisPtr = new NButton[1];
    final NButton[] cbYaxisPtr = new NButton[1];
    final NButton[] cbZaxisPtr = new NButton[1];
    final IntegerSpinner[] spnCoordsPtr = new IntegerSpinner[1];
    final IntegerSpinner[] spnMatrixPtr = new IntegerSpinner[1];

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
        Composite cmpContainer = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = (GridLayout) cmpContainer.getLayout();
        gridLayout.verticalSpacing = 10;
        gridLayout.horizontalSpacing = 10;

        Label lblSpecify = new Label(cmpContainer, SWT.NONE);
        lblSpecify.setText(I18n.ROUND_TITLE);

        Label lblSeparator = new Label(cmpContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
        lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        {
            Composite cmpCoords = new Composite(cmpContainer, SWT.NONE);
            cmpCoords.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpCoords.setLayout(new GridLayout(3, true));

            NButton cbXaxis = new NButton(cmpCoords, SWT.CHECK);
            this.cbXaxisPtr[0] = cbXaxis;
            cbXaxis.setText(I18n.ROUND_X);
            cbXaxis.setSelection(WorkbenchManager.getUserSettingState().isRoundX());

            NButton cbYaxis = new NButton(cmpCoords, SWT.CHECK);
            this.cbYaxisPtr[0] = cbYaxis;
            cbYaxis.setText(I18n.ROUND_Y);
            cbYaxis.setSelection(WorkbenchManager.getUserSettingState().isRoundY());

            NButton cbZaxis = new NButton(cmpCoords, SWT.CHECK);
            this.cbZaxisPtr[0] = cbZaxis;
            cbZaxis.setText(I18n.ROUND_Z);
            cbZaxis.setSelection(WorkbenchManager.getUserSettingState().isRoundZ());
        }

        Label lblCoordsPrec = new Label(cmpContainer, SWT.NONE);
        lblCoordsPrec.setText(I18n.ROUND_COORD_PRECISION);

        IntegerSpinner spnCoords = new IntegerSpinner(cmpContainer, SWT.NONE);
        this.spnCoordsPtr[0] = spnCoords;
        spnCoords.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spnCoords.setMaximum(9);
        spnCoords.setMinimum(0);
        spnCoords.setValue(WorkbenchManager.getUserSettingState().getCoordsPrecision());

        Label lblMatrixPrec = new Label(cmpContainer, SWT.NONE);
        lblMatrixPrec.setText(I18n.ROUND_MATRIX_PRECISION);

        IntegerSpinner spnMatrix = new IntegerSpinner(cmpContainer, SWT.NONE);
        this.spnMatrixPtr[0] = spnMatrix;
        spnMatrix.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spnMatrix.setMaximum(9);
        spnMatrix.setMinimum(0);
        spnMatrix.setValue(WorkbenchManager.getUserSettingState().getTransMatrixPrecision());

        Label lblUnit = new Label(cmpContainer, SWT.NONE);
        lblUnit.setText(I18n.ROUND_IN_DEC_PLACES);

        cmpContainer.pack();
        return cmpContainer;
    }

    /**
     * Create contents of the button bar.
     *
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        btnOkPtr[0] = createButton(parent, IDialogConstants.OK_ID, I18n.DIALOG_OK, true);
        createButton(parent, IDialogConstants.CANCEL_ID, I18n.DIALOG_CANCEL, false);
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return super.getInitialSize();
    }

}
