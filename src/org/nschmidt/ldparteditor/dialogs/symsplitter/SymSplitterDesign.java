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
        Composite cmpContainer = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = (GridLayout) cmpContainer.getLayout();
        gridLayout.verticalSpacing = 10;
        gridLayout.horizontalSpacing = 10;

        Label lblSpecify = new Label(cmpContainer, SWT.NONE);
        lblSpecify.setText(I18n.SYMSPLITTER_TITLE);

        Label lblSeparator = new Label(cmpContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
        lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label lblHint = new Label(cmpContainer, SWT.NONE);
        lblHint.setText(I18n.SYMSPLITTER_HINT);

        BigDecimalSpinner spnOffset = new BigDecimalSpinner(cmpContainer, SWT.NONE);
        this.spn_offset [0] = spnOffset;
        spnOffset.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spnOffset.setMaximum(new BigDecimal(100000000));
        spnOffset.setMinimum(new BigDecimal(-100000000));
        spnOffset.setValue(ss.getOffset());

        Label lblPrecision = new Label(cmpContainer, SWT.NONE);
        lblPrecision.setText(I18n.SYMSPLITTER_VERTEX_THRESHOLD);

        BigDecimalSpinner spnPrecision = new BigDecimalSpinner(cmpContainer, SWT.NONE);
        this.spn_precision [0] = spnPrecision;
        spnPrecision.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spnPrecision.setMaximum(new BigDecimal(1000));
        spnPrecision.setMinimum(new BigDecimal(0));
        spnPrecision.setValue(ss.getPrecision());

        Label lblSplitPlane = new Label(cmpContainer, SWT.NONE);
        lblSplitPlane.setText(I18n.SYMSPLITTER_SPLITTING_PLANE);

        {
            Combo cmbSplitPlane = new Combo(cmpContainer, SWT.READ_ONLY);
            this.cmb_splitPlane[0] = cmbSplitPlane;
            cmbSplitPlane.setItems(new String[] {I18n.SYMSPLITTER_ZP, I18n.SYMSPLITTER_YP, I18n.SYMSPLITTER_XP, I18n.SYMSPLITTER_ZM, I18n.SYMSPLITTER_YM, I18n.SYMSPLITTER_XM});
            cmbSplitPlane.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmbSplitPlane.setText(cmbSplitPlane.getItem(ss.getSplitPlane()));
            cmbSplitPlane.select(ss.getSplitPlane());
        }

        Label lblHide = new Label(cmpContainer, SWT.NONE);
        lblHide.setText(I18n.SYMSPLITTER_SELECT_WHAT);
        {
            Combo cmbHide = new Combo(cmpContainer, SWT.READ_ONLY);
            this.cmb_hide[0] = cmbHide;
            cmbHide.setItems(new String[] {I18n.SYMSPLITTER_SHOW_ALL, I18n.SYMSPLITTER_SHOW_MIDDLE, I18n.SYMSPLITTER_SHOW_FRONT, I18n.SYMSPLITTER_SHOW_BEHIND});
            cmbHide.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmbHide.setText(cmbHide.getItem(ss.getHideLevel()));
            cmbHide.select(ss.getHideLevel());
        }

        Label lblDummy = new Label(cmpContainer, SWT.NONE);
        lblDummy.setText(""); //$NON-NLS-1$

        {
            Combo cmbValidate = new Combo(cmpContainer, SWT.READ_ONLY);
            this.cmb_validate[0] = cmbValidate;
            cmbValidate.setItems(new String[] {I18n.SYMSPLITTER_NO_VALIDATION, I18n.SYMSPLITTER_VALIDATION});
            cmbValidate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmbValidate.setText(cmbValidate.getItem(ss.isValidate() ? 1 : 0));
            cmbValidate.select(ss.isValidate() ? 1 : 0);
        }
        {
            Combo cmbCutAcross = new Combo(cmpContainer, SWT.READ_ONLY);
            this.cmb_cutAcross[0] = cmbCutAcross;
            cmbCutAcross.setItems(new String[] {I18n.SYMSPLITTER_DO_NOT_CUT, I18n.SYMSPLITTER_CUT});
            cmbCutAcross.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmbCutAcross.setText(cmbCutAcross.getItem(ss.isCutAcross() ? 1 : 0));
            cmbCutAcross.select(ss.isCutAcross() ? 1 : 0);
        }
        {
            Combo cmbColourise = new Combo(cmpContainer, SWT.READ_ONLY);
            this.cmb_colourise[0] = cmbColourise;
            cmbColourise.setItems(new String[] {I18n.SYMSPLITTER_NOT_COLOURISE, I18n.SYMSPLITTER_COLOURISE});
            cmbColourise.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmbColourise.setText(cmbColourise.getItem(ss.isColourise() ? 1 : 0));
            cmbColourise.select(ss.isColourise() ? 1 : 0);
        }
        Combo cmbScope = new Combo(cmpContainer, SWT.READ_ONLY);
        this.cmb_scope[0] = cmbScope;
        cmbScope.setItems(new String[] {I18n.SYMSPLITTER_SCOPE_FILE, I18n.SYMSPLITTER_SCOPE_SELECTION});
        cmbScope.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        cmbScope.setText(cmbScope.getItem(0));
        cmbScope.select(0);
        cmbScope.setEnabled(false);

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

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return super.getInitialSize();
    }

}
