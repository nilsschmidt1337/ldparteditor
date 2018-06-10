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
import org.eclipse.swt.graphics.Point;
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

    final boolean alreadyReviewing;

    protected PartReviewDesign(Shell parentShell, boolean alreadyReviewing) {
        super(parentShell);
        this.alreadyReviewing = alreadyReviewing;
    }

    Button[] btn_ok = new Button[1];
    Button[] btn_cancel = new Button[1];
    final Text[] txt_file = new Text[1];

    /**
     * Create contents of the dialog.
     *
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite cmp_Container = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = (GridLayout) cmp_Container.getLayout();
        gridLayout.verticalSpacing = 10;
        gridLayout.horizontalSpacing = 10;

        if (alreadyReviewing) {
            Label lbl_Info = new Label(cmp_Container, SWT.NONE);
            lbl_Info.setText(I18n.E3D_PartReviewAlready);
        } else {
            Label lbl_PartName = new Label(cmp_Container, SWT.NONE);
            lbl_PartName.setText(I18n.E3D_PartReviewEnterPartName);

            Text txt_file2 = new Text(cmp_Container, SWT.NONE);
            this.txt_file[0] = txt_file2;
            GridData gd = new GridData();
            gd.grabExcessHorizontalSpace = true;
            gd.horizontalAlignment = SWT.FILL;
            txt_file2.setLayoutData(gd);

            this.txt_file[0].addModifyListener(e -> fileName = txt_file[0].getText());

            Label lbl_Info = new Label(cmp_Container, SWT.NONE);
            lbl_Info.setText(I18n.E3D_PartReviewInfo);
        }

        cmp_Container.pack();
        return cmp_Container;
    }

    /**
     * Create contents of the button bar.
     *
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        if (alreadyReviewing) {
            btn_ok[0] = createButton(parent, IDialogConstants.OK_ID, I18n.DIALOG_Yes, true);
            btn_cancel[0] = createButton(parent, IDialogConstants.CANCEL_ID, I18n.DIALOG_No, false);
        } else {
            btn_ok[0] = createButton(parent, IDialogConstants.OK_ID, I18n.DIALOG_OK, true);
            btn_cancel[0] = createButton(parent, IDialogConstants.CANCEL_ID, I18n.DIALOG_Cancel, false);
        }
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return super.getInitialSize();
    }

}
