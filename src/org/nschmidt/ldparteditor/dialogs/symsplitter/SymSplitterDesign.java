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
package org.nschmidt.ldparteditor.dialogs.symsplitter;

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
import org.nschmidt.ldparteditor.helpers.composite3d.SymSplitterSettings;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.widgets.BigDecimalSpinner;

/**
 * The symsplitter dialog
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 *
 * @author nils
 *
 */
class SymSplitterDesign extends Dialog {

    final SymSplitterSettings ss;
    final BigDecimalSpinner[] spn_offset = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_precision = new BigDecimalSpinner[1];
    final Combo[] cmb_scope = new Combo[1];

    final Combo[] cmb_splitPlane = new Combo[1];
    final Combo[] cmb_hide = new Combo[1];
    final Combo[] cmb_colourise = new Combo[1];
    final Combo[] cmb_validate = new Combo[1];
    final Combo[] cmb_cutAcross = new Combo[1];

    // Use final only for subclass/listener references!

    SymSplitterDesign(Shell parentShell, SymSplitterSettings ss) {
        super(parentShell);
        this.ss = ss;
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
        lbl_specify.setText("SymSplitter [Arbitrary Precision]"); //$NON-NLS-1$ I18N Needs translation!

        Label lbl_separator = new Label(cmp_container, SWT.SEPARATOR | SWT.HORIZONTAL);
        lbl_separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label lbl_hint = new Label(cmp_container, SWT.NONE);
        lbl_hint.setText("\nPlease note that no inlining option is provided within SymSplitter and TEXMAP is not supported yet.\nSymSplitter restructures the file content and deletes nothing, unless the threshold is not zero.\n\n\nPlane offset (0, default):"); //$NON-NLS-1$ I18N Needs translation!

        BigDecimalSpinner spn_offset = new BigDecimalSpinner(cmp_container, SWT.NONE);
        this.spn_offset [0] = spn_offset;
        spn_offset.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spn_offset.setMaximum(new BigDecimal(100000000));
        spn_offset.setMinimum(new BigDecimal(-100000000));
        spn_offset.setValue(ss.getOffset());

        Label lbl_precision = new Label(cmp_container, SWT.NONE);
        lbl_precision.setText("Vertex unification threshold to the plane (0, default):"); //$NON-NLS-1$ I18N Needs translation!

        BigDecimalSpinner spn_precision = new BigDecimalSpinner(cmp_container, SWT.NONE);
        this.spn_precision [0] = spn_precision;
        spn_precision.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spn_precision.setMaximum(new BigDecimal(1000));
        spn_precision.setMinimum(new BigDecimal(0));
        spn_precision.setValue(ss.getPrecision());

        Label lbl_splitPlane = new Label(cmp_container, SWT.NONE);
        lbl_splitPlane.setText("Splitting Plane (+z would be split by the plane z=0):"); //$NON-NLS-1$ I18N Needs translation!

        {
            Combo cmb_splitPlane = new Combo(cmp_container, SWT.READ_ONLY);
            this.cmb_splitPlane[0] = cmb_splitPlane;
            cmb_splitPlane.setItems(new String[] {"+z", "+y", "+x", "-z", "-y", "-x"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ I18N Needs translation!
            cmb_splitPlane.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmb_splitPlane.setText(cmb_splitPlane.getItem(ss.getSplitPlane()));
            cmb_splitPlane.select(ss.getSplitPlane());
        }

        Label lbl_hide = new Label(cmp_container, SWT.NONE);
        lbl_hide.setText("Select what to show:"); //$NON-NLS-1$ I18N Needs translation!
        {
            Combo cmb_hide = new Combo(cmp_container, SWT.READ_ONLY);
            this.cmb_hide[0] = cmb_hide;
            cmb_hide.setItems(new String[] {"Show all.", "Show middle.", "Show what is in front of the plane.", "Show what is behind the plane."}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ I18N Needs translation!
            cmb_hide.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmb_hide.setText(cmb_hide.getItem(ss.getHideLevel()));
            cmb_hide.select(ss.getHideLevel());
        }

        Label lbl_dummy = new Label(cmp_container, SWT.NONE);
        lbl_dummy.setText(""); //$NON-NLS-1$

        {
            Combo cmb_validate = new Combo(cmp_container, SWT.READ_ONLY);
            this.cmb_validate[0] = cmb_validate;
            cmb_validate.setItems(new String[] {"No validation.", "Validates the middle section. Read the manual for more information."}); //$NON-NLS-1$ //$NON-NLS-2$ I18N Needs translation!
            cmb_validate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmb_validate.setText(cmb_validate.getItem(ss.isValidate() ? 1 : 0));
            cmb_validate.select(ss.isValidate() ? 1 : 0);
        }
        {
            Combo cmb_cutAcross = new Combo(cmp_container, SWT.READ_ONLY);
            this.cmb_cutAcross[0] = cmb_cutAcross;
            cmb_cutAcross.setItems(new String[] {"Do not cut.", "Cut across the plane."}); //$NON-NLS-1$ //$NON-NLS-2$ I18N Needs translation!
            cmb_cutAcross.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmb_cutAcross.setText(cmb_cutAcross.getItem(ss.isCutAcross() ? 1 : 0));
            cmb_cutAcross.select(ss.isCutAcross() ? 1 : 0);
        }
        {
            Combo cmb_colourise = new Combo(cmp_container, SWT.READ_ONLY);
            this.cmb_colourise[0] = cmb_colourise;
            cmb_colourise.setItems(new String[] {"No colour modifications.", "Colourises the result. Read the manual for more information."}); //$NON-NLS-1$ //$NON-NLS-2$ I18N Needs translation!
            cmb_colourise.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmb_colourise.setText(cmb_colourise.getItem(ss.isColourise() ? 1 : 0));
            cmb_colourise.select(ss.isColourise() ? 1 : 0);
        }
        Combo cmb_scope = new Combo(cmp_container, SWT.READ_ONLY);
        this.cmb_scope[0] = cmb_scope;
        cmb_scope.setItems(new String[] {"Scope: File", "Scope: Selection"}); //$NON-NLS-1$ //$NON-NLS-2$ I18N Needs translation!
        cmb_scope.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        cmb_scope.setText(cmb_scope.getItem(0));
        cmb_scope.select(0);
        cmb_scope.setEnabled(false);

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
