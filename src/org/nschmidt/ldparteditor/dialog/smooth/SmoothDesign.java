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
package org.nschmidt.ldparteditor.dialog.smooth;

import java.math.BigDecimal;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.widget.BigDecimalSpinner;
import org.nschmidt.ldparteditor.widget.IntegerSpinner;
import org.nschmidt.ldparteditor.widget.NButton;

/**
 * The scale dialog
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 *
 * @author nils
 *
 */
class SmoothDesign extends Dialog {


    final NButton[] cbXaxisPtr = new NButton[1];
    final NButton[] cbYaxisPtr = new NButton[1];
    final NButton[] cbZaxisPtr = new NButton[1];
    final IntegerSpinner[] spnPXPtr = new IntegerSpinner[1];
    final BigDecimalSpinner[] spnPYPtr = new BigDecimalSpinner[1];

    private static final String NUMBER_FORMAT = View.NUMBER_FORMAT4F;

    // Use final only for subclass/listener references!

    SmoothDesign(Shell parentShell) {
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
        gridLayout.verticalSpacing = 5;
        gridLayout.horizontalSpacing = 10;

        Label lblSpecify = new Label(cmpContainer, SWT.NONE);
        lblSpecify.setText(I18n.SMOOTH_TITLE);

        Label lblSeparator = new Label(cmpContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
        lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label lblPreview = new Label(cmpContainer, SWT.NONE);
        lblPreview.setText("This dialog supports a realtime preview of the new mesh."); //$NON-NLS-1$ FIXME !i18n!

        {
            Composite cmpTxt = new Composite(cmpContainer, SWT.NONE);
            cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpTxt.setLayout(new GridLayout(3, true));

            NButton cbXaxis = new NButton(cmpTxt, SWT.CHECK);
            this.cbXaxisPtr[0] = cbXaxis;
            cbXaxis.setText(I18n.COORDINATESDIALOG_X);
            cbXaxis.setSelection(true);

            NButton cbYaxis = new NButton(cmpTxt, SWT.CHECK);
            this.cbYaxisPtr[0] = cbYaxis;
            cbYaxis.setText(I18n.COORDINATESDIALOG_Y);
            cbYaxis.setSelection(true);

            NButton cbZaxis = new NButton(cmpTxt, SWT.CHECK);
            this.cbZaxisPtr[0] = cbZaxis;
            cbZaxis.setText(I18n.COORDINATESDIALOG_Z);
            cbZaxis.setSelection(true);
        }

        Label lblIter = new Label(cmpContainer, SWT.NONE);
        lblIter.setText("Iterations:"); //$NON-NLS-1$ FIXME !i18n!

        {
            Composite cmpTxt = new Composite(cmpContainer, SWT.NONE);
            cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpTxt.setLayout(new GridLayout(1, true));

            IntegerSpinner spnPX = new IntegerSpinner(cmpTxt, SWT.NONE);
            this.spnPXPtr[0] = spnPX;
            spnPX.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            spnPX.setMaximum(9999);
            spnPX.setMinimum(1);
            spnPX.setValue(1);
        }

        Label lblFactor = new Label(cmpContainer, SWT.NONE);
        lblFactor.setText("Factor:"); //$NON-NLS-1$ FIXME !i18n!

        {
            Composite cmpTxt = new Composite(cmpContainer, SWT.NONE);
            cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpTxt.setLayout(new GridLayout(1, true));

            BigDecimalSpinner spnPY = new BigDecimalSpinner(cmpTxt, SWT.NONE, NUMBER_FORMAT);
            this.spnPYPtr[0] = spnPY;
            spnPY.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            spnPY.setMaximum(new BigDecimal(1));
            spnPY.setMinimum(new BigDecimal(-1));
            spnPY.setValue(new BigDecimal(1));
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
        createButton(parent, IDialogConstants.OK_ID, I18n.DIALOG_OK, false);
        createButton(parent, IDialogConstants.CANCEL_ID, I18n.DIALOG_CANCEL, false);
    }
}
