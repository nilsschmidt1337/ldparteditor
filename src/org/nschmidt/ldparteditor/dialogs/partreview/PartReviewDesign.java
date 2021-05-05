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
package org.nschmidt.ldparteditor.dialogs.partreview;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.nschmidt.ldparteditor.i18n.I18n;

class PartReviewDesign extends Dialog {

    static String fileName = ""; //$NON-NLS-1$

    private final boolean alreadyReviewing;

    protected PartReviewDesign(Shell parentShell, boolean alreadyReviewing) {
        super(parentShell);
        this.alreadyReviewing = alreadyReviewing;
    }

    private Button[] btnOkPtr = new Button[1];
    private Button[] btnCancelPtr = new Button[1];
    private final Text[] txtFilePtr = new Text[1];

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

        if (alreadyReviewing) {
            Label lblInfo = new Label(cmpContainer, SWT.NONE);
            lblInfo.setText(I18n.E3D_PART_REVIEW_ALREADY);
        } else {
            Label lblPartName = new Label(cmpContainer, SWT.NONE);
            lblPartName.setText(I18n.E3D_PART_REVIEW_ENTER_PART_NAME);

            Text txtFile2 = new Text(cmpContainer, SWT.NONE);
            this.txtFilePtr[0] = txtFile2;
            GridData gd = new GridData();
            gd.grabExcessHorizontalSpace = true;
            gd.horizontalAlignment = SWT.FILL;
            txtFile2.setLayoutData(gd);

            this.txtFilePtr[0].addModifyListener(e -> fileName = txtFilePtr[0].getText());

            Label lblInfo = new Label(cmpContainer, SWT.NONE);
            lblInfo.setText(I18n.E3D_PART_REVIEW_INFO);
        }

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
        if (alreadyReviewing) {
            btnOkPtr[0] = createButton(parent, IDialogConstants.OK_ID, I18n.DIALOG_YES, true);
            btnCancelPtr[0] = createButton(parent, IDialogConstants.CANCEL_ID, I18n.DIALOG_NO, false);
        } else {
            btnOkPtr[0] = createButton(parent, IDialogConstants.OK_ID, I18n.DIALOG_OK, true);
            btnCancelPtr[0] = createButton(parent, IDialogConstants.CANCEL_ID, I18n.DIALOG_CANCEL, false);
        }
    }
}
