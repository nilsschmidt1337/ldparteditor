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
package org.nschmidt.ldparteditor.dialogs.txt2dat;

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
import org.eclipse.swt.widgets.Text;
import org.nschmidt.ldparteditor.helpers.composite3d.Txt2DatSettings;
import org.nschmidt.ldparteditor.widgets.BigDecimalSpinner;

/**
 * The rounding precision dialog
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 *
 * @author nils
 *
 */
class Txt2DatDesign extends Dialog {

    final Txt2DatSettings ts;

    // Use final only for subclass/listener references!
    final Button[] btn_chooseFont = new Button[1];
    final BigDecimalSpinner[] spn_flatness = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_interpolateFlatness = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_fontHeight = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_deltaAngle = new BigDecimalSpinner[1];
    final Text[] txt_text = new Text[1];

    Txt2DatDesign(Shell parentShell, Txt2DatSettings ts) {
        super(parentShell);
        this.ts = ts;
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
        lbl_specify.setText("Txt2Dat [Normal Precision, max. 4000 Triangles per Letter]"); //$NON-NLS-1$ I18N Needs translation!

        Label lbl_separator = new Label(cmp_container, SWT.SEPARATOR | SWT.HORIZONTAL);
        lbl_separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label lbl_coordsPrec = new Label(cmp_container, SWT.NONE);
        lbl_coordsPrec.setText("Font:"); //$NON-NLS-1$ I18N Needs translation!

        Button spn_vequ = new Button(cmp_container, SWT.NONE);
        this.btn_chooseFont[0] = spn_vequ;
        spn_vequ.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spn_vequ.setText("Select"); //$NON-NLS-1$ I18N

        Label lbl_text = new Label(cmp_container, SWT.NONE);
        lbl_text.setText("Text:"); //$NON-NLS-1$ I18N Needs translation!

        Text txt_text = new Text(cmp_container, SWT.NONE);
        this.txt_text[0] = txt_text;
        txt_text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        txt_text.setText(ts.getText());

        Label lbl_use180deg = new Label(cmp_container, SWT.NONE);
        lbl_use180deg.setText("Flatness\n(the maximum distance that the line segments used to\napproximate the curved segments are allowed to deviate\nfrom any point on the original curve):"); //$NON-NLS-1$ I18N Needs translation!

        BigDecimalSpinner spn_flatness = new BigDecimalSpinner(cmp_container, SWT.NONE);
        this.spn_flatness[0] = spn_flatness;
        spn_flatness.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spn_flatness.setMaximum(new BigDecimal(1000));
        spn_flatness.setMinimum(new BigDecimal(0));
        spn_flatness.setValue(ts.getFlatness());

        Label lbl_af = new Label(cmp_container, SWT.NONE);
        lbl_af.setText("Interpolate Flatness:\n(the maximum distance that is used for interpolation)"); //$NON-NLS-1$ I18N Needs translation!

        BigDecimalSpinner spn_interpolateFlatness = new BigDecimalSpinner(cmp_container, SWT.NONE);
        this.spn_interpolateFlatness[0] = spn_interpolateFlatness;
        spn_interpolateFlatness.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spn_interpolateFlatness.setMaximum(new BigDecimal(1000));
        spn_interpolateFlatness.setMinimum(new BigDecimal(0));
        spn_interpolateFlatness.setValue(ts.getInterpolateFlatness());

        Label lbl_ac = new Label(cmp_container, SWT.NONE);
        lbl_ac.setText("Font Height [LDU]:"); //$NON-NLS-1$ I18N Needs translation!

        BigDecimalSpinner spn_fontHeight = new BigDecimalSpinner(cmp_container, SWT.NONE);
        this.spn_fontHeight[0] = spn_fontHeight;
        spn_fontHeight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spn_fontHeight.setMaximum(new BigDecimal(10000));
        spn_fontHeight.setMinimum(new BigDecimal("0.0001"));//$NON-NLS-1$
        spn_fontHeight.setValue(ts.getFontHeight());

        Label lbl_ae = new Label(cmp_container, SWT.NONE);
        lbl_ae.setText("Min. Angle Between Line Segments [°]:"); //$NON-NLS-1$ I18N Needs translation!
        BigDecimalSpinner spn_deltaAngle = new BigDecimalSpinner(cmp_container, SWT.NONE);
        this.spn_deltaAngle[0] = spn_deltaAngle;
        spn_deltaAngle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spn_deltaAngle.setMaximum(new BigDecimal(30));
        spn_deltaAngle.setMinimum(new BigDecimal(0));
        spn_deltaAngle.setValue(ts.getDeltaAngle());

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
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
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
