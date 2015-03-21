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
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.composite3d.RingsAndConesSettings;
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
        Composite cmp_container = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = (GridLayout) cmp_container.getLayout();
        gridLayout.verticalSpacing = 10;
        gridLayout.horizontalSpacing = 10;

        Label lbl_specify = new Label(cmp_container, SWT.NONE);
        lbl_specify.setText("Rings And Cones [Arbitrary Precision]"); //$NON-NLS-1$ I18N Needs translation!

        Label lbl_separator = new Label(cmp_container, SWT.SEPARATOR | SWT.HORIZONTAL);
        lbl_separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        {
            Label lbl = new Label(cmp_container, SWT.NONE);
            lbl.setText("The search may only find an approximation.\n" //$NON-NLS-1$
                    + "If you select a 4-4 disc in your model,\n" //$NON-NLS-1$
                    + "the tool will place the output at the disc's location."); //$NON-NLS-1$ I18N Needs translation!
        }
        {
            Label lbl = new Label(cmp_container, SWT.NONE);
            lbl.setText("Shape:"); //$NON-NLS-1$ I18N Needs translation!
        }
        {
            Combo cmb = new Combo(cmp_container, SWT.READ_ONLY);
            this.cmb_shape[0] = cmb;
            cmb.setItems(new String[] {"Ring.", "Cone."}); //$NON-NLS-1$ //$NON-NLS-2$ I18N Needs translation!
            cmb.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmb.setText(cmb.getItem(0));
            cmb.select(0);
        }
        {
            Label lbl = new Label(cmp_container, SWT.NONE);
            lbl.setText("Radius 1 (LDU):"); //$NON-NLS-1$ I18N Needs translation!
        }
        {
            BigDecimalSpinner spn = new BigDecimalSpinner(cmp_container, SWT.NONE);
            this.spn_radi1 [0] = spn;
            spn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            spn.setMaximum(new BigDecimal(10000));
            spn.setMinimum(new BigDecimal("0.0001")); //$NON-NLS-1$
            spn.setValue(rs.getRadius1());
        }
        {
            Label lbl = new Label(cmp_container, SWT.NONE);
            lbl.setText("Radius 2 (LDU):"); //$NON-NLS-1$ I18N Needs translation!
        }
        {
            BigDecimalSpinner spn = new BigDecimalSpinner(cmp_container, SWT.NONE);
            this.spn_radi2[0] = spn;
            spn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            spn.setMaximum(new BigDecimal(10000));
            spn.setMinimum(new BigDecimal("0.0002")); //$NON-NLS-1$
            spn.setValue(rs.getRadius2());
        }

        {
            Label lbl = new Label(cmp_container, SWT.NONE);
            lbl.setText("Height, for Cones (LDU):"); //$NON-NLS-1$ I18N Needs translation!
        }
        {
            BigDecimalSpinner spn = new BigDecimalSpinner(cmp_container, SWT.NONE);
            this.spn_height[0] = spn;
            spn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            spn.setMaximum(new BigDecimal(90));
            spn.setMinimum(new BigDecimal(0));
            spn.setValue(new BigDecimal(1));
        }

        {
            Label lbl = new Label(cmp_container, SWT.NONE);
            lbl.setText("Angle:"); //$NON-NLS-1$ I18N Needs translation!
        }
        {
            Combo cmb = new Combo(cmp_container, SWT.READ_ONLY);
            this.cmb_angle[0] = cmb;
            cmb.setItems(new String[] {
                    "1-48  (  7.5°)", //$NON-NLS-1$
                    "1-24  ( 15.0°)", //$NON-NLS-1$
                    "1-16  ( 22.5°)", //$NON-NLS-1$
                    "1-12  ( 30.0°)", //$NON-NLS-1$
                    "5-48  ( 37.5°)", //$NON-NLS-1$
                    "1-8   ( 45.0°)", //$NON-NLS-1$
                    "7-48  ( 52.5°)", //$NON-NLS-1$
                    "1-6   ( 60.0°)", //$NON-NLS-1$
                    "3-16  ( 67.5°)", //$NON-NLS-1$
                    "5-24  ( 75.0°)", //$NON-NLS-1$
                    "11-48 ( 82.5°)", //$NON-NLS-1$
                    "1-4   ( 90.0°)", //$NON-NLS-1$
                    "13-48 ( 97.5°)", //$NON-NLS-1$
                    "7-24  (105.0°)", //$NON-NLS-1$
                    "5-16  (112.5°)", //$NON-NLS-1$
                    "1-3   (120.0°)", //$NON-NLS-1$
                    "17-48 (127.5°)", //$NON-NLS-1$
                    "3-8   (135.0°)", //$NON-NLS-1$
                    "19-48 (142.5°)", //$NON-NLS-1$
                    "5-12  (150.0°)", //$NON-NLS-1$
                    "7-16  (157.5°)", //$NON-NLS-1$
                    "11-24 (165.0°)", //$NON-NLS-1$
                    "23-48 (172.5°)", //$NON-NLS-1$
                    "2-4   (180.0°)", //$NON-NLS-1$
                    "25-48 (187.5°)", //$NON-NLS-1$
                    "13-48 (195.0°)", //$NON-NLS-1$
                    "9-16  (202.5°)", //$NON-NLS-1$
                    "7-12  (210.0°)", //$NON-NLS-1$
                    "29-48 (217.5°)", //$NON-NLS-1$
                    "5-8   (225.0°)", //$NON-NLS-1$
                    "31-48 (232.5°)", //$NON-NLS-1$
                    "2-3   (240.0°)", //$NON-NLS-1$
                    "11-16 (247.5°)", //$NON-NLS-1$
                    "17-24 (255.0°)", //$NON-NLS-1$
                    "35-48 (262.5°)", //$NON-NLS-1$
                    "3-4   (270.0°)", //$NON-NLS-1$
                    "37-48 (277.5°)", //$NON-NLS-1$
                    "19-24 (285.0°)", //$NON-NLS-1$
                    "13-16 (292.5°)", //$NON-NLS-1$
                    "5-6   (300.0°)", //$NON-NLS-1$
                    "41-48 (307.5°)", //$NON-NLS-1$
                    "7-8   (315.0°)", //$NON-NLS-1$
                    "43-48 (322.5°)", //$NON-NLS-1$
                    "11-12 (330.0°)", //$NON-NLS-1$
                    "15-16 (337.5°)", //$NON-NLS-1$
                    "23-24 (345.0°)", //$NON-NLS-1$
                    "47-48 (352.5°)", //$NON-NLS-1$
                    "4-4   (360.0°)" //$NON-NLS-1$
            });
            {
                rs.getAngles().clear();
                int i = 0;
                for (String it : cmb.getItems()) {
                    cmb.setItem(i, it.replace('.', View.NUMBER_FORMAT4F.getDecimalFormatSymbols().getDecimalSeparator()));
                    rs.getAngles().add(it);
                    i++;
                }
            }
            cmb.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmb.setText(cmb.getItem(47));
            cmb.select(47);
        }
        {
            Combo cmb = new Combo(cmp_container, SWT.READ_ONLY);
            this.cmb_existingOnly[0] = cmb;
            cmb.setItems(new String[] {"Use only existing primitives.", "Use all possible primitives."}); //$NON-NLS-1$ //$NON-NLS-2$ I18N Needs translation!
            cmb.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmb.setText(cmb.getItem(0));
            cmb.select(0);
        }
        {
            Combo cmb = new Combo(cmp_container, SWT.READ_ONLY);
            this.cmb_createWhat[0] = cmb;
            cmb.setItems(new String[] {"Create nothing, if no solution was found.", "Create the shape for me."}); //$NON-NLS-1$ //$NON-NLS-2$ I18N Needs translation!
            cmb.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmb.setText(cmb.getItem(0));
            cmb.select(0);
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
