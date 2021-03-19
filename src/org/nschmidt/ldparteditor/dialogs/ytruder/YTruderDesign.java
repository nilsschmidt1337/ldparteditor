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
package org.nschmidt.ldparteditor.dialogs.ytruder;

import java.math.BigDecimal;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.helpers.composite3d.YTruderSettings;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.widgets.BigDecimalSpinner;
import org.nschmidt.ldparteditor.widgets.NButton;

/**
 * The YTruder dialog
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 *
 * @author nils
 *
 */
class YTruderDesign extends Dialog {


    final YTruderSettings ys;

    // Use final only for subclass/listener references!
    final BigDecimalSpinner[] spn_value = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_condlineAngleThreshold = new BigDecimalSpinner[1];
    final Combo[] cmb_axis = new Combo[1];
    final NButton[] btn_TranslateByDistance = new NButton[1];
    final NButton[] btn_SymmetryAcrossPlane = new NButton[1];
    final NButton[] btn_ProjectionOnPlane = new NButton[1];
    final NButton[] btn_ExtrudeRadially = new NButton[1];

    YTruderDesign(Shell parentShell, YTruderSettings ys) {
        super(parentShell);
        this.ys = ys;
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

        Label lbl_title = new Label(cmp_container, SWT.NONE);
        lbl_title.setText(I18n.YTRUDER_Title);

        Label lbl_separator = new Label(cmp_container, SWT.SEPARATOR | SWT.HORIZONTAL);
        lbl_separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label lbl_description = new Label(cmp_container, SWT.NONE);
        lbl_description.setText(I18n.YTRUDER_Description);

        {
            NButton btn_TranslateByDistance = new NButton(cmp_container, SWT.RADIO);
            this.btn_TranslateByDistance[0] = btn_TranslateByDistance;
            btn_TranslateByDistance.setText(I18n.YTRUDER_TranslationByDistance);
            btn_TranslateByDistance.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            btn_TranslateByDistance.setSelection(ys.getMode() == 1);
        }
        {
            NButton btn_SymmetryAcrossPlane = new NButton(cmp_container, SWT.RADIO);
            this.btn_SymmetryAcrossPlane[0] = btn_SymmetryAcrossPlane;
            btn_SymmetryAcrossPlane.setText(I18n.YTRUDER_SymmetryAcrossPlane);
            btn_SymmetryAcrossPlane.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            btn_SymmetryAcrossPlane.setSelection(ys.getMode() == 2);
        }
        {
            NButton btn_ProjectionOnPlane = new NButton(cmp_container, SWT.RADIO);
            this.btn_ProjectionOnPlane[0] = btn_ProjectionOnPlane;
            btn_ProjectionOnPlane.setText(I18n.YTRUDER_ProjectionOnPlane);
            btn_ProjectionOnPlane.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            btn_ProjectionOnPlane.setSelection(ys.getMode() == 3);
        }
        {
            NButton btn_ExtrudeRadially = new NButton(cmp_container, SWT.RADIO);
            this.btn_ExtrudeRadially[0] = btn_ExtrudeRadially;
            btn_ExtrudeRadially.setText(I18n.YTRUDER_ExtrudeRadially);
            btn_ExtrudeRadially.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            btn_ExtrudeRadially.setSelection(ys.getMode() == 4);
        }

        Label lbl_lineThreshold = new Label(cmp_container, SWT.NONE);
        lbl_lineThreshold.setText(I18n.YTRUDER_Value);

        BigDecimalSpinner spn_value = new BigDecimalSpinner(cmp_container, SWT.NONE);
        this.spn_value[0] = spn_value;
        spn_value.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spn_value.setMaximum(new BigDecimal(999999));
        spn_value.setMinimum(new BigDecimal(-999999));
        spn_value.setValue(new BigDecimal(ys.getDistance()));

        Label lbl_rotationAngle = new Label(cmp_container, SWT.NONE);
        lbl_rotationAngle.setText(I18n.YTRUDER_CondlineAngle);

        BigDecimalSpinner spn_rotationAngle = new BigDecimalSpinner(cmp_container, SWT.NONE);
        this.spn_condlineAngleThreshold[0] = spn_rotationAngle;
        spn_rotationAngle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spn_rotationAngle.setMaximum(new BigDecimal(180));
        spn_rotationAngle.setMinimum(new BigDecimal(0));
        spn_rotationAngle.setValue(new BigDecimal(ys.getCondlineAngleThreshold()));

        Label lbl_af = new Label(cmp_container, SWT.NONE);
        lbl_af.setText(I18n.YTRUDER_Axis);

        Combo cmb_axis = new Combo(cmp_container, SWT.READ_ONLY);
        this.cmb_axis[0] = cmb_axis;
        cmb_axis.setItems(new String[] {I18n.YTRUDER_X, I18n.YTRUDER_Y, I18n.YTRUDER_Z});
        cmb_axis.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        cmb_axis.select(ys.getAxis());

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
        createButton(parent, IDialogConstants.OK_ID, I18n.DIALOG_OK, true);
        createButton(parent, IDialogConstants.CANCEL_ID, I18n.DIALOG_Cancel, false);
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return super.getInitialSize();
    }

}
