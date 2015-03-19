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


    final Combo[] cmb_createWhat = new Combo[1];;
    final Combo[] cmb_existingOnly = new Combo[1];
    final IntegerSpinner[] spn_amount = new IntegerSpinner[1];
    final Combo[] cmb_angle = new Combo[1];
    final BigDecimalSpinner[] spn_height = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_radi1 = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_radi2 = new BigDecimalSpinner[1];
    final Combo[] cmb_shape = new Combo[1];

    // Use final only for subclass/listener references!

    RingsAndConesDesign(Shell parentShell) {
        super(parentShell);
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
            lbl.setText("The algorithm may only find an approximation.\n" //$NON-NLS-1$
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
            spn.setMaximum(new BigDecimal(90));
            spn.setMinimum(new BigDecimal("0.0001")); //$NON-NLS-1$
            spn.setValue(new BigDecimal(1));
        }
        {
            Label lbl = new Label(cmp_container, SWT.NONE);
            lbl.setText("Radius 2 (LDU):"); //$NON-NLS-1$ I18N Needs translation!
        }
        {
            BigDecimalSpinner spn = new BigDecimalSpinner(cmp_container, SWT.NONE);
            this.spn_radi2[0] = spn;
            spn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            spn.setMaximum(new BigDecimal(1000000));
            spn.setMinimum(new BigDecimal("0.0001")); //$NON-NLS-1$
            spn.setValue(new BigDecimal(1));
        }

        {
            Label lbl = new Label(cmp_container, SWT.NONE);
            lbl.setText("Height (LDU):"); //$NON-NLS-1$ I18N Needs translation!
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
                    "Use only existing primitives.", //$NON-NLS-1$
            "Use all possible primitives."}); //$NON-NLS-1$
            cmb.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmb.setText(cmb.getItem(0));
            cmb.select(0);
        }

        {
            Label lbl = new Label(cmp_container, SWT.NONE);
            lbl.setText("Maximum Amount:"); //$NON-NLS-1$ I18N Needs translation!
        }
        {
            IntegerSpinner spn = new IntegerSpinner(cmp_container, SWT.NONE);
            this.spn_amount [0] = spn;
            spn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            spn.setMaximum(10);
            spn.setMinimum(1);
            spn.setValue(3);
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
