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
package org.nschmidt.ldparteditor.dialogs.setcoordinates;

import java.math.BigDecimal;

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
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.widgets.BigDecimalSpinner;

/**
 * The coordinates dialog
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 *
 * @author nils
 *
 */
class CoordinatesDesign extends Dialog {


    final Button[] cb_Xaxis = new Button[1];
    final Button[] cb_Yaxis = new Button[1];
    final Button[] cb_Zaxis = new Button[1];
    final BigDecimalSpinner[] spn_X = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_Y = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_Z = new BigDecimalSpinner[1];

    // Use final only for subclass/listener references!

    Vertex v = new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    CoordinatesDesign(Shell parentShell, Vertex v) {
        super(parentShell);
        if (v == null) {
            this.v = new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        } else {
            this.v = v;
        }
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
        gridLayout.verticalSpacing = 5;
        gridLayout.horizontalSpacing = 10;

        Label lbl_specify = new Label(cmp_container, SWT.NONE);
        lbl_specify.setText(I18n.COORDINATESDIALOG_SetXYZ);

        Label lbl_separator = new Label(cmp_container, SWT.SEPARATOR | SWT.HORIZONTAL);
        lbl_separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        {
            Composite cmp_txt = new Composite(cmp_container, SWT.NONE);
            cmp_txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmp_txt.setLayout(new GridLayout(6, true));

            Button cb_Xaxis = new Button(cmp_txt, SWT.CHECK);
            this.cb_Xaxis[0] = cb_Xaxis;
            cb_Xaxis.setText(I18n.COORDINATESDIALOG_X);
            cb_Xaxis.setSelection(true);

            BigDecimalSpinner spn_X = new BigDecimalSpinner(cmp_txt, SWT.NONE);
            this.spn_X[0] = spn_X;
            spn_X.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
            spn_X.setMaximum(new BigDecimal(1000000));
            spn_X.setMinimum(new BigDecimal(-1000000));
            spn_X.setValue(v.X);
        }

        {
            Composite cmp_txt = new Composite(cmp_container, SWT.NONE);
            cmp_txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmp_txt.setLayout(new GridLayout(6, true));

            Button cb_Yaxis = new Button(cmp_txt, SWT.CHECK);
            this.cb_Yaxis[0] = cb_Yaxis;
            cb_Yaxis.setText(I18n.COORDINATESDIALOG_Y);
            cb_Yaxis.setSelection(true);

            BigDecimalSpinner spn_Y = new BigDecimalSpinner(cmp_txt, SWT.NONE);
            this.spn_Y[0] = spn_Y;
            spn_Y.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
            spn_Y.setMaximum(new BigDecimal(1000000));
            spn_Y.setMinimum(new BigDecimal(-1000000));
            spn_Y.setValue(v.Y);
        }

        {
            Composite cmp_txt = new Composite(cmp_container, SWT.NONE);
            cmp_txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmp_txt.setLayout(new GridLayout(6, true));

            Button cb_Zaxis = new Button(cmp_txt, SWT.CHECK);
            this.cb_Zaxis[0] = cb_Zaxis;
            cb_Zaxis.setText(I18n.COORDINATESDIALOG_Z);
            cb_Zaxis.setSelection(true);

            BigDecimalSpinner spn_Z = new BigDecimalSpinner(cmp_txt, SWT.NONE);
            this.spn_Z[0] = spn_Z;
            spn_Z.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
            spn_Z.setMaximum(new BigDecimal(1000000));
            spn_Z.setMinimum(new BigDecimal(-1000000));
            spn_Z.setValue(v.Z);
        }

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
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false);
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
