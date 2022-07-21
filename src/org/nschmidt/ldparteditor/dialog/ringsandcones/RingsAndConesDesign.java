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
package org.nschmidt.ldparteditor.dialog.ringsandcones;

import static org.nschmidt.ldparteditor.helper.WidgetUtility.widgetUtil;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.enumtype.MyLanguage;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.helper.composite3d.RingsAndConesSettings;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.widget.BigDecimalSpinner;

/**
 * The rings and cones dialog
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 */
class RingsAndConesDesign extends Dialog {

    final RingsAndConesSettings rs;

    final Combo[] cmbCreateWhatPtr = new Combo[1];
    final Combo[] cmbExistingOnlyPtr = new Combo[1];
    final Combo[] cmbAnglePtr = new Combo[1];
    final BigDecimalSpinner[] spnHeightPtr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnRadi1Ptr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnRadi2Ptr = new BigDecimalSpinner[1];
    final Combo[] cmbShapePtr = new Combo[1];

    // Use final only for subclass/listener references!

    RingsAndConesDesign(Shell parentShell, RingsAndConesSettings rs) {
        super(parentShell);
        this.rs = rs;
    }

    /**
     * Create contents of the dialog.
     *
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {

        final java.text.DecimalFormat numberFormat4f = new java.text.DecimalFormat(View.NUMBER_FORMAT4F, new DecimalFormatSymbols(MyLanguage.getLocale()));

        Composite cmpContainer = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = (GridLayout) cmpContainer.getLayout();
        gridLayout.verticalSpacing = 10;
        gridLayout.horizontalSpacing = 10;

        Label lblSpecify = new Label(cmpContainer, SWT.NONE);
        lblSpecify.setText(I18n.RCONES_TITLE);

        Label lblSeparator = new Label(cmpContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
        lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        {
            Label lbl = new Label(cmpContainer, SWT.NONE);
            lbl.setText(I18n.RCONES_HINT);
        }
        {
            Label lbl = new Label(cmpContainer, SWT.NONE);
            lbl.setText(I18n.RCONES_SHAPE);
        }
        {
            Combo cmb = new Combo(cmpContainer, SWT.READ_ONLY);
            this.cmbShapePtr[0] = cmb;
            widgetUtil(cmb).setItems(I18n.RCONES_RING, I18n.RCONES_CONE, I18n.RCONES_RING_48, I18n.RCONES_CONE_48);
            cmb.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmb.select((rs.isUsingCones() ? 1 : 0) + (rs.isUsingHiRes() ? 2 : 0));
        }
        {
            Label lbl = new Label(cmpContainer, SWT.NONE);
            lbl.setText(I18n.RCONES_RADIUS_1);
        }
        {
            BigDecimalSpinner spn = new BigDecimalSpinner(cmpContainer, SWT.NONE);
            this.spnRadi1Ptr [0] = spn;
            spn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            spn.setMaximum(new BigDecimal(10000));
            spn.setMinimum(BigDecimal.ZERO);
            spn.setValue(rs.getRadius1());
        }
        {
            Label lbl = new Label(cmpContainer, SWT.NONE);
            lbl.setText(I18n.RCONES_RADIUS_2);
        }
        {
            BigDecimalSpinner spn = new BigDecimalSpinner(cmpContainer, SWT.NONE);
            this.spnRadi2Ptr[0] = spn;
            spn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            spn.setMaximum(new BigDecimal(10000));
            spn.setMinimum(BigDecimal.ZERO);
            spn.setValue(rs.getRadius2());
        }

        {
            Label lbl = new Label(cmpContainer, SWT.NONE);
            lbl.setText(I18n.RCONES_HEIGHT);
        }
        {
            BigDecimalSpinner spn = new BigDecimalSpinner(cmpContainer, SWT.NONE);
            this.spnHeightPtr[0] = spn;
            spn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            spn.setMaximum(new BigDecimal(10000));
            spn.setMinimum(new BigDecimal("0.0001")); //$NON-NLS-1$
            spn.setValue(rs.getHeight());
        }

        {
            Label lbl = new Label(cmpContainer, SWT.NONE);
            lbl.setText(I18n.RCONES_ANGLE);
        }
        {
            Combo cmb = new Combo(cmpContainer, SWT.READ_ONLY);
            this.cmbAnglePtr[0] = cmb;
            widgetUtil(cmb).setItems(
                    I18n.RCONES_ANGLE_01,
                    I18n.RCONES_ANGLE_02,
                    I18n.RCONES_ANGLE_03,
                    I18n.RCONES_ANGLE_04,
                    I18n.RCONES_ANGLE_05,
                    I18n.RCONES_ANGLE_06,
                    I18n.RCONES_ANGLE_07,
                    I18n.RCONES_ANGLE_08,
                    I18n.RCONES_ANGLE_09,
                    I18n.RCONES_ANGLE_10,
                    I18n.RCONES_ANGLE_11,
                    I18n.RCONES_ANGLE_12,
                    I18n.RCONES_ANGLE_13,
                    I18n.RCONES_ANGLE_14,
                    I18n.RCONES_ANGLE_15,
                    I18n.RCONES_ANGLE_16,
                    I18n.RCONES_ANGLE_17,
                    I18n.RCONES_ANGLE_18,
                    I18n.RCONES_ANGLE_19,
                    I18n.RCONES_ANGLE_20,
                    I18n.RCONES_ANGLE_21,
                    I18n.RCONES_ANGLE_22,
                    I18n.RCONES_ANGLE_23,
                    I18n.RCONES_ANGLE_24,
                    I18n.RCONES_ANGLE_25,
                    I18n.RCONES_ANGLE_26,
                    I18n.RCONES_ANGLE_27,
                    I18n.RCONES_ANGLE_28,
                    I18n.RCONES_ANGLE_29,
                    I18n.RCONES_ANGLE_30,
                    I18n.RCONES_ANGLE_31,
                    I18n.RCONES_ANGLE_32,
                    I18n.RCONES_ANGLE_33,
                    I18n.RCONES_ANGLE_34,
                    I18n.RCONES_ANGLE_35,
                    I18n.RCONES_ANGLE_36,
                    I18n.RCONES_ANGLE_37,
                    I18n.RCONES_ANGLE_38,
                    I18n.RCONES_ANGLE_39,
                    I18n.RCONES_ANGLE_40,
                    I18n.RCONES_ANGLE_41,
                    I18n.RCONES_ANGLE_42,
                    I18n.RCONES_ANGLE_43,
                    I18n.RCONES_ANGLE_44,
                    I18n.RCONES_ANGLE_45,
                    I18n.RCONES_ANGLE_46,
                    I18n.RCONES_ANGLE_47,
                    I18n.RCONES_ANGLE_48);
            {
                rs.getAngles().clear();
                int i = 0;
                for (String it : cmb.getItems()) {
                    cmb.setItem(i, it.replace('.', numberFormat4f.getDecimalFormatSymbols().getDecimalSeparator()));
                    rs.getAngles().add(it);
                    i++;
                }
            }
            cmb.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmb.select(rs.getAngle());
        }
        {
            Combo cmb = new Combo(cmpContainer, SWT.READ_ONLY);
            this.cmbExistingOnlyPtr[0] = cmb;
            widgetUtil(cmb).setItems(I18n.RCONES_PRIMS_1, I18n.RCONES_PRIMS_2);
            cmb.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmb.select(rs.isUsingExistingPrimitives() ? 0 : 1);
        }
        {
            Combo cmb = new Combo(cmpContainer, SWT.READ_ONLY);
            this.cmbCreateWhatPtr[0] = cmb;
            widgetUtil(cmb).setItems(I18n.RCONES_CREATE_1, I18n.RCONES_CREATE_2);
            cmb.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmb.select(rs.isCreatingNothingOnNoSolution() ? 0 : 1);
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
        createButton(parent, IDialogConstants.OK_ID, I18n.DIALOG_OK, true);
        createButton(parent, IDialogConstants.CANCEL_ID, I18n.DIALOG_CANCEL, false);
    }
}
