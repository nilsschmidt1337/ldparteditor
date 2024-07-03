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
package org.nschmidt.ldparteditor.dialog.calibrate;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.data.VertexInfo;
import org.nschmidt.ldparteditor.enumtype.MyLanguage;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.helper.math.MathHelper;
import org.nschmidt.ldparteditor.helper.math.Vector3d;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.widget.BigDecimalSpinner;
import org.nschmidt.ldparteditor.widget.NButton;

/**
 * The BG image calibration dialog
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 */
class CalibrateDesign extends Dialog {

    private static final DecimalFormat DF2F = new java.text.DecimalFormat(View.NUMBER_FORMAT2F, new DecimalFormatSymbols(MyLanguage.getLocale()));
    private static final DecimalFormat DF4F = new java.text.DecimalFormat(View.NUMBER_FORMAT4F, new DecimalFormatSymbols(MyLanguage.getLocale()));

    final NButton[] btnLDUPtr = new NButton[1];
    final NButton[] btnMMPtr = new NButton[1];
    final NButton[] btnInchPtr = new NButton[1];
    final NButton[] btnStudPtr = new NButton[1];
    final BigDecimalSpinner[] spnLDUPtr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnMMPtr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnInchPtr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnStudPtr = new BigDecimalSpinner[1];

    private static final String NUMBER_FORMAT8F = View.NUMBER_FORMAT8F;
    private static final String NUMBER_FORMAT1F = View.NUMBER_FORMAT1F;

    // Use final only for subclass/listener references!

    private final Set<VertexInfo> vis;
    protected BigDecimal oldDistLDU = BigDecimal.ZERO;
    protected Vector3d start = new Vector3d();

    CalibrateDesign(Shell parentShell, Set<VertexInfo> vis) {
        super(parentShell);
        this.vis = vis;
    }

    /**
     * Create contents of the dialog.
     *
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {

        List<VertexInfo> visList = new ArrayList<>(vis);
        for (VertexInfo vi : visList) {
            if (vi.getPosition() == 0) start = new Vector3d(vi.getVertex());
        }

        Vector3d v1 = new Vector3d(visList.get(0).getVertex());
        Vector3d v2 = new Vector3d(visList.get(1).getVertex());
        BigDecimal distLDU = MathHelper.roundBigDecimalAlways(Vector3d.sub(v1, v2, null).length());
        BigDecimal distMM = distLDU.multiply(new BigDecimal("0.4")); //$NON-NLS-1$
        oldDistLDU = distLDU;
        Object[] messageArguments = {DF4F.format(distLDU), DF2F.format(distMM)};
        MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
        formatter.setLocale(MyLanguage.getLocale());
        formatter.applyPattern(I18n.CALIBRATE_LENGTH);

        Composite cmpContainer = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = (GridLayout) cmpContainer.getLayout();
        gridLayout.verticalSpacing = 5;
        gridLayout.horizontalSpacing = 10;

        Label lblOldLength = new Label(cmpContainer, SWT.NONE);
        lblOldLength.setText(formatter.format(messageArguments));

        Label lblSeparator = new Label(cmpContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
        lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        {
            Composite cmpTxt = new Composite(cmpContainer, SWT.NONE);
            cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpTxt.setLayout(new GridLayout(6, true));

            NButton cbLDU = new NButton(cmpTxt, SWT.RADIO);
            this.btnLDUPtr[0] = cbLDU;
            cbLDU.setText(I18n.UNITS_LDU);
            cbLDU.setSelection(true);

            BigDecimalSpinner spnLDU = new BigDecimalSpinner(cmpTxt, SWT.NONE, NUMBER_FORMAT8F);
            this.spnLDUPtr[0] = spnLDU;
            spnLDU.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
            spnLDU.setMaximum(new BigDecimal(1000000));
            spnLDU.setMinimum(new BigDecimal(-1000000));
            spnLDU.setValue(distLDU);
        }

        {
            Composite cmpTxt = new Composite(cmpContainer, SWT.NONE);
            cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpTxt.setLayout(new GridLayout(6, true));

            NButton cbMM = new NButton(cmpTxt, SWT.RADIO);
            this.btnMMPtr[0] = cbMM;
            cbMM.setText(I18n.UNITS_SECONDARY);
            cbMM.setSelection(false);

            BigDecimalSpinner spnMM = new BigDecimalSpinner(cmpTxt, SWT.NONE, NUMBER_FORMAT8F);
            this.spnMMPtr[0] = spnMM;
            spnMM.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
            spnMM.setMaximum(new BigDecimal(1000000));
            spnMM.setMinimum(new BigDecimal(-1000000));
        }

        {
            Composite cmpTxt = new Composite(cmpContainer, SWT.NONE);
            cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpTxt.setLayout(new GridLayout(6, true));

            NButton cbStud = new NButton(cmpTxt, SWT.RADIO);
            this.btnStudPtr[0] = cbStud;
            cbStud.setText(I18n.UNITS_TERTIARY);
            cbStud.setSelection(false);

            BigDecimalSpinner spnStud = new BigDecimalSpinner(cmpTxt, SWT.NONE, NUMBER_FORMAT1F);
            this.spnStudPtr[0] = spnStud;
            spnStud.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
            spnStud.setMaximum(new BigDecimal(1000000));
            spnStud.setMinimum(new BigDecimal(-1000000));
        }

        {
            Composite cmpTxt = new Composite(cmpContainer, SWT.NONE);
            cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpTxt.setLayout(new GridLayout(6, true));

            NButton cbInch = new NButton(cmpTxt, SWT.RADIO);
            this.btnInchPtr[0] = cbInch;
            cbInch.setText(I18n.UNITS_PRIMARY);
            cbInch.setSelection(false);

            BigDecimalSpinner spnInch = new BigDecimalSpinner(cmpTxt, SWT.NONE, NUMBER_FORMAT8F);
            this.spnInchPtr[0] = spnInch;
            spnInch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
            spnInch.setMaximum(new BigDecimal(1000000));
            spnInch.setMinimum(new BigDecimal(-1000000));
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
