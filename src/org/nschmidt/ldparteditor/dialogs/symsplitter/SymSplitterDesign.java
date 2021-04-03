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
        lbl_specify.setText(I18n.SYMSPLITTER_TITLE);

        Label lbl_separator = new Label(cmp_container, SWT.SEPARATOR | SWT.HORIZONTAL);
        lbl_separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label lbl_hint = new Label(cmp_container, SWT.NONE);
        lbl_hint.setText(I18n.SYMSPLITTER_HINT);

        BigDecimalSpinner spn_offset = new BigDecimalSpinner(cmp_container, SWT.NONE);
        this.spn_offset [0] = spn_offset;
        spn_offset.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spn_offset.setMaximum(new BigDecimal(100000000));
        spn_offset.setMinimum(new BigDecimal(-100000000));
        spn_offset.setValue(ss.getOffset());

        Label lbl_precision = new Label(cmp_container, SWT.NONE);
        lbl_precision.setText(I18n.SYMSPLITTER_VERTEX_THRESHOLD);

        BigDecimalSpinner spn_precision = new BigDecimalSpinner(cmp_container, SWT.NONE);
        this.spn_precision [0] = spn_precision;
        spn_precision.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spn_precision.setMaximum(new BigDecimal(1000));
        spn_precision.setMinimum(new BigDecimal(0));
        spn_precision.setValue(ss.getPrecision());

        Label lbl_splitPlane = new Label(cmp_container, SWT.NONE);
        lbl_splitPlane.setText(I18n.SYMSPLITTER_SPLITTING_PLANE);

        {
            Combo cmb_splitPlane = new Combo(cmp_container, SWT.READ_ONLY);
            this.cmb_splitPlane[0] = cmb_splitPlane;
            cmb_splitPlane.setItems(new String[] {I18n.SYMSPLITTER_ZP, I18n.SYMSPLITTER_YP, I18n.SYMSPLITTER_XP, I18n.SYMSPLITTER_ZM, I18n.SYMSPLITTER_YM, I18n.SYMSPLITTER_XM});
            cmb_splitPlane.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmb_splitPlane.setText(cmb_splitPlane.getItem(ss.getSplitPlane()));
            cmb_splitPlane.select(ss.getSplitPlane());
        }

        Label lbl_hide = new Label(cmp_container, SWT.NONE);
        lbl_hide.setText(I18n.SYMSPLITTER_SELECT_WHAT);
        {
            Combo cmb_hide = new Combo(cmp_container, SWT.READ_ONLY);
            this.cmb_hide[0] = cmb_hide;
            cmb_hide.setItems(new String[] {I18n.SYMSPLITTER_SHOW_ALL, I18n.SYMSPLITTER_SHOW_MIDDLE, I18n.SYMSPLITTER_SHOW_FRONT, I18n.SYMSPLITTER_SHOW_BEHIND});
            cmb_hide.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmb_hide.setText(cmb_hide.getItem(ss.getHideLevel()));
            cmb_hide.select(ss.getHideLevel());
        }

        Label lbl_dummy = new Label(cmp_container, SWT.NONE);
        lbl_dummy.setText(""); //$NON-NLS-1$

        {
            Combo cmb_validate = new Combo(cmp_container, SWT.READ_ONLY);
            this.cmb_validate[0] = cmb_validate;
            cmb_validate.setItems(new String[] {I18n.SYMSPLITTER_NO_VALIDATION, I18n.SYMSPLITTER_VALIDATION});
            cmb_validate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmb_validate.setText(cmb_validate.getItem(ss.isValidate() ? 1 : 0));
            cmb_validate.select(ss.isValidate() ? 1 : 0);
        }
        {
            Combo cmb_cutAcross = new Combo(cmp_container, SWT.READ_ONLY);
            this.cmb_cutAcross[0] = cmb_cutAcross;
            cmb_cutAcross.setItems(new String[] {I18n.SYMSPLITTER_DO_NOT_CUT, I18n.SYMSPLITTER_CUT});
            cmb_cutAcross.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmb_cutAcross.setText(cmb_cutAcross.getItem(ss.isCutAcross() ? 1 : 0));
            cmb_cutAcross.select(ss.isCutAcross() ? 1 : 0);
        }
        {
            Combo cmb_colourise = new Combo(cmp_container, SWT.READ_ONLY);
            this.cmb_colourise[0] = cmb_colourise;
            cmb_colourise.setItems(new String[] {I18n.SYMSPLITTER_NOT_COLOURISE, I18n.SYMSPLITTER_COLOURISE});
            cmb_colourise.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmb_colourise.setText(cmb_colourise.getItem(ss.isColourise() ? 1 : 0));
            cmb_colourise.select(ss.isColourise() ? 1 : 0);
        }
        Combo cmb_scope = new Combo(cmp_container, SWT.READ_ONLY);
        this.cmb_scope[0] = cmb_scope;
        cmb_scope.setItems(new String[] {I18n.SYMSPLITTER_SCOPE_FILE, I18n.SYMSPLITTER_SCOPE_SELECTION});
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
        createButton(parent, IDialogConstants.CANCEL_ID, I18n.DIALOG_CANCEL, false);
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return super.getInitialSize();
    }

}
