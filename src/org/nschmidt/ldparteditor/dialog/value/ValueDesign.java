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
package org.nschmidt.ldparteditor.dialog.value;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.dialog.ThemedDialog;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.widget.BigDecimalSpinner;
import org.nschmidt.ldparteditor.workbench.Theming;

class ValueDesign extends ThemedDialog {

    private Label[] lblUnitPtr = new Label[1];
    Button[] btnOkPtr = new Button[1];
    protected BigDecimalSpinner[] spnValuePtr = new BigDecimalSpinner[1];


    private final String unitText;
    final String shellText;

    ValueDesign(Shell parentShell, String shellText, String unitText) {
        super(parentShell);
        this.unitText = unitText;
        this.shellText = shellText;
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

        BigDecimalSpinner spnValue = new BigDecimalSpinner(cmpContainer, SWT.NONE);
        this.spnValuePtr[0] = spnValue;
        GridData gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        spnValue.setLayoutData(gd);


        Label lblUnit = Theming.label(cmpContainer, SWT.NONE);
        this.lblUnitPtr[0] = lblUnit;
        lblUnit.setText(unitText);

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
}
