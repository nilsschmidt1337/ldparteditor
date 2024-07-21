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
package org.nschmidt.ldparteditor.dialog.partreview;

import java.text.MessageFormat;

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
import org.nschmidt.ldparteditor.dialog.ThemedDialog;
import org.nschmidt.ldparteditor.enumtype.MyLanguage;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.widget.IntegerSpinner;
import org.nschmidt.ldparteditor.widget.NButton;
import org.nschmidt.ldparteditor.workbench.Theming;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

class PartReviewDesign extends ThemedDialog {

    static String fileName = ""; //$NON-NLS-1$
    static String projectPath = ""; //$NON-NLS-1$

    private final boolean alreadyReviewing;

    protected PartReviewDesign(Shell parentShell, boolean alreadyReviewing) {
        super(parentShell);
        this.alreadyReviewing = alreadyReviewing;
    }

    private Button[] btnOkPtr = new Button[1];
    private Button[] btnCancelPtr = new Button[1];
    final Text[] txtFilePtr = new Text[1];
    final IntegerSpinner[] spnViewCountPtr = new IntegerSpinner[1];
    final NButton[] btnStoreLocallyPtr = new NButton[1];
    final NButton[] btnVerbosePtr = new NButton[1];

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
            Label lblInfo = Theming.label(cmpContainer, SWT.NONE);
            lblInfo.setText(I18n.PARTREVIEW_ALREADY);
        } else {
            Label lblPartName = Theming.label(cmpContainer, SWT.NONE);
            lblPartName.setText(I18n.PARTREVIEW_ENTER_PART_NAME);

            Text txtFile2 = Theming.text(cmpContainer, SWT.NONE);
            this.txtFilePtr[0] = txtFile2;
            GridData gd = new GridData();
            gd.grabExcessHorizontalSpace = true;
            gd.horizontalAlignment = SWT.FILL;
            txtFile2.setLayoutData(gd);
            txtFile2.setTextLimit(64);

            Label lblInfo = Theming.label(cmpContainer, SWT.NONE);
            lblInfo.setText(I18n.PARTREVIEW_INFO);

            NButton btnStoreLocally = new NButton(cmpContainer, SWT.CHECK);
            this.btnStoreLocallyPtr[0] = btnStoreLocally;
            btnStoreLocally.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            Object[] messageArguments = {""}; //$NON-NLS-1$
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.getLocale());
            formatter.applyPattern(I18n.PARTREVIEW_STORE_LOCATION);
            btnStoreLocally.setText(formatter.format(messageArguments));
            btnStoreLocally.setSelection(WorkbenchManager.getUserSettingState().isPartReviewStoreLocalFiles());

            Label lblNumberOf3dViews = Theming.label(cmpContainer, SWT.NONE);
            lblNumberOf3dViews.setText(I18n.PARTREVIEW_NUMBER_OF_3D_VIEWS);

            IntegerSpinner spnViewCount = new IntegerSpinner(cmpContainer, SWT.NONE);
            this.spnViewCountPtr[0] = spnViewCount;
            spnViewCount.setMinimum(1);
            spnViewCount.setMaximum(4);
            spnViewCount.setValue(WorkbenchManager.getUserSettingState().getPartReview3dViewCount());

            NButton btnVerbose = new NButton(cmpContainer, SWT.CHECK);
            this.btnVerbosePtr[0] = btnVerbose;
            btnVerbose.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            btnVerbose.setText(I18n.PARTREVIEW_VERBOSE);
            btnVerbose.setSelection(WorkbenchManager.getUserSettingState().isVerbosePartReview());
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
