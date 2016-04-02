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
package org.nschmidt.ldparteditor.dialogs.ringsandcones;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;

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
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.composite3d.RingsAndConesSettings;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.widgets.BigDecimalSpinner;
import org.nschmidt.ldparteditor.widgets.IntegerSpinner;

/**
 * The rings and cones dialog
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 *
 * @author nils
 *
 */
class RingsAndConesDesign extends Dialog {

    final RingsAndConesSettings rs;

    final Combo[] cmb_createWhat = new Combo[1];;
    final Combo[] cmb_existingOnly = new Combo[1];
    final IntegerSpinner[] spn_amount = new IntegerSpinner[1];
    final Combo[] cmb_angle = new Combo[1];
    final BigDecimalSpinner[] spn_height = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_radi1 = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_radi2 = new BigDecimalSpinner[1];
    final Combo[] cmb_shape = new Combo[1];

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

        final java.text.DecimalFormat NUMBER_FORMAT4F = new java.text.DecimalFormat(View.NUMBER_FORMAT4F, new DecimalFormatSymbols(MyLanguage.LOCALE));

        Composite cmp_container = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = (GridLayout) cmp_container.getLayout();
        gridLayout.verticalSpacing = 10;
        gridLayout.horizontalSpacing = 10;

        Label lbl_specify = new Label(cmp_container, SWT.NONE);
        lbl_specify.setText(I18n.RCONES_Title);

        Label lbl_separator = new Label(cmp_container, SWT.SEPARATOR | SWT.HORIZONTAL);
        lbl_separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        {
            Label lbl = new Label(cmp_container, SWT.NONE);
            lbl.setText(I18n.RCONES_Hint);
        }
        {
            Label lbl = new Label(cmp_container, SWT.NONE);
            lbl.setText(I18n.RCONES_Shape);
        }
        {
            Combo cmb = new Combo(cmp_container, SWT.READ_ONLY);
            this.cmb_shape[0] = cmb;
            cmb.setItems(new String[] {I18n.RCONES_Ring, I18n.RCONES_Cone, I18n.RCONES_Ring48, I18n.RCONES_Cone48});
            cmb.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmb.select((rs.isUsingCones() ? 1 : 0) + (rs.isUsingHiRes() ? 2 : 0));
        }
        {
            Label lbl = new Label(cmp_container, SWT.NONE);
            lbl.setText(I18n.RCONES_Radius1);
        }
        {
            BigDecimalSpinner spn = new BigDecimalSpinner(cmp_container, SWT.NONE);
            this.spn_radi1 [0] = spn;
            spn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            spn.setMaximum(new BigDecimal(10000));
            spn.setMinimum(BigDecimal.ZERO);
            spn.setValue(rs.getRadius1());
        }
        {
            Label lbl = new Label(cmp_container, SWT.NONE);
            lbl.setText(I18n.RCONES_Radius2);
        }
        {
            BigDecimalSpinner spn = new BigDecimalSpinner(cmp_container, SWT.NONE);
            this.spn_radi2[0] = spn;
            spn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            spn.setMaximum(new BigDecimal(10000));
            spn.setMinimum(BigDecimal.ZERO);
            spn.setValue(rs.getRadius2());
        }

        {
            Label lbl = new Label(cmp_container, SWT.NONE);
            lbl.setText(I18n.RCONES_Height);
        }
        {
            BigDecimalSpinner spn = new BigDecimalSpinner(cmp_container, SWT.NONE);
            this.spn_height[0] = spn;
            spn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            spn.setMaximum(new BigDecimal(10000));
            spn.setMinimum(new BigDecimal("0.0001")); //$NON-NLS-1$
            spn.setValue(rs.getHeight());
        }

        {
            Label lbl = new Label(cmp_container, SWT.NONE);
            lbl.setText(I18n.RCONES_Angle);
        }
        {
            Combo cmb = new Combo(cmp_container, SWT.READ_ONLY);
            this.cmb_angle[0] = cmb;
            cmb.setItems(new String[] {
                    I18n.RCONES_Angle01,
                    I18n.RCONES_Angle02,
                    I18n.RCONES_Angle03,
                    I18n.RCONES_Angle04,
                    I18n.RCONES_Angle05,
                    I18n.RCONES_Angle06,
                    I18n.RCONES_Angle07,
                    I18n.RCONES_Angle08,
                    I18n.RCONES_Angle09,
                    I18n.RCONES_Angle10,
                    I18n.RCONES_Angle11,
                    I18n.RCONES_Angle12,
                    I18n.RCONES_Angle13,
                    I18n.RCONES_Angle14,
                    I18n.RCONES_Angle15,
                    I18n.RCONES_Angle16,
                    I18n.RCONES_Angle17,
                    I18n.RCONES_Angle18,
                    I18n.RCONES_Angle19,
                    I18n.RCONES_Angle20,
                    I18n.RCONES_Angle21,
                    I18n.RCONES_Angle22,
                    I18n.RCONES_Angle23,
                    I18n.RCONES_Angle24,
                    I18n.RCONES_Angle25,
                    I18n.RCONES_Angle26,
                    I18n.RCONES_Angle27,
                    I18n.RCONES_Angle28,
                    I18n.RCONES_Angle29,
                    I18n.RCONES_Angle30,
                    I18n.RCONES_Angle31,
                    I18n.RCONES_Angle32,
                    I18n.RCONES_Angle33,
                    I18n.RCONES_Angle34,
                    I18n.RCONES_Angle35,
                    I18n.RCONES_Angle36,
                    I18n.RCONES_Angle37,
                    I18n.RCONES_Angle38,
                    I18n.RCONES_Angle39,
                    I18n.RCONES_Angle40,
                    I18n.RCONES_Angle41,
                    I18n.RCONES_Angle42,
                    I18n.RCONES_Angle43,
                    I18n.RCONES_Angle44,
                    I18n.RCONES_Angle45,
                    I18n.RCONES_Angle46,
                    I18n.RCONES_Angle47,
                    I18n.RCONES_Angle48
            });
            {
                rs.getAngles().clear();
                int i = 0;
                for (String it : cmb.getItems()) {
                    cmb.setItem(i, it.replace('.', NUMBER_FORMAT4F.getDecimalFormatSymbols().getDecimalSeparator()));
                    rs.getAngles().add(it);
                    i++;
                }
            }
            cmb.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmb.select(rs.getAngle());
        }
        {
            Combo cmb = new Combo(cmp_container, SWT.READ_ONLY);
            this.cmb_existingOnly[0] = cmb;
            cmb.setItems(new String[] {I18n.RCONES_Prims1, I18n.RCONES_Prims2});
            cmb.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmb.select(rs.isUsingExistingPrimitives() ? 0 : 1);
        }
        {
            Combo cmb = new Combo(cmp_container, SWT.READ_ONLY);
            this.cmb_createWhat[0] = cmb;
            cmb.setItems(new String[] {I18n.RCONES_Create1, I18n.RCONES_Create2});
            cmb.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmb.select(rs.isCreatingNothingOnNoSolution() ? 0 : 1);
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
