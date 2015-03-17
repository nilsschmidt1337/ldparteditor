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

    final BigDecimalSpinner[] spn_angle = new BigDecimalSpinner[1];
    final Combo[] cmb_scope = new Combo[1];

    final Combo[] cmb_colourise = new Combo[1];
    final Combo[] cmb_noQuadConversation = new Combo[1];
    final Combo[] cmb_noRectConversationOnAdjacentCondlines = new Combo[1];
    final Combo[] cmb_noBorderedQuadToRectConversation = new Combo[1];

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
            lbl.setText("Shape:"); //$NON-NLS-1$ I18N Needs translation!
        }

        {
            Label lbl = new Label(cmp_container, SWT.NONE);
            lbl.setText("Inner Radius:"); //$NON-NLS-1$ I18N Needs translation!
        }

        {
            Label lbl = new Label(cmp_container, SWT.NONE);
            lbl.setText("Outer Radius:"); //$NON-NLS-1$ I18N Needs translation!
        }

        {
            Label lbl = new Label(cmp_container, SWT.NONE);
            lbl.setText("Hight:"); //$NON-NLS-1$ I18N Needs translation!
        }

        {
            Label lbl = new Label(cmp_container, SWT.NONE);
            lbl.setText("Angle:"); //$NON-NLS-1$ I18N Needs translation!
        }

        {
            Label lbl = new Label(cmp_container, SWT.NONE);
            lbl.setText("Maximum Amount:"); //$NON-NLS-1$ I18N Needs translation!
        }

        {
            Label lbl = new Label(cmp_container, SWT.NONE);
            lbl.setText("Use Non-Existing Primitives:"); //$NON-NLS-1$ I18N Needs translation!
        }


        BigDecimalSpinner spn_angle = new BigDecimalSpinner(cmp_container, SWT.NONE);
        this.spn_angle [0] = spn_angle;
        spn_angle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spn_angle.setMaximum(new BigDecimal(90));
        spn_angle.setMinimum(new BigDecimal(0));
        spn_angle.setValue(new BigDecimal(1));

        {
            Combo cmb_colourise = new Combo(cmp_container, SWT.READ_ONLY);
            this.cmb_colourise[0] = cmb_colourise;
            cmb_colourise.setItems(new String[] {"No colour modifications.", "Converted triangles are colored in yellow, newly formed rect primitives are colored blue."}); //$NON-NLS-1$ //$NON-NLS-2$ I18N Needs translation!
            cmb_colourise.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmb_colourise.setText(cmb_colourise.getItem(1));
            cmb_colourise.select(1);
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
