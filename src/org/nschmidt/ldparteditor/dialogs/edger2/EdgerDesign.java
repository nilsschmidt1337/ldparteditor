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
package org.nschmidt.ldparteditor.dialogs.edger2;

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
import org.nschmidt.ldparteditor.helpers.composite3d.Edger2Settings;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.widgets.BigDecimalSpinner;

/**
 * The edger2 dialog
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 *
 * @author nils
 *
 */
class EdgerDesign extends Dialog {

    final Edger2Settings es;

    // Use final only for subclass/listener references!
    final BigDecimalSpinner[] spn_vequ = new BigDecimalSpinner[1];

    final BigDecimalSpinner[] spn_af = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_ac = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_ae = new BigDecimalSpinner[1];
    final Combo[] cmb_b = new Combo[1];
    final Combo[] cmb_u = new Combo[1];
    final Combo[] cmb_scope = new Combo[1];

    EdgerDesign(Shell parentShell, Edger2Settings es) {
        super(parentShell);
        this.es = es;
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
        lbl_specify.setText(I18n.EDGER_Title);

        Label lbl_separator = new Label(cmp_container, SWT.SEPARATOR | SWT.HORIZONTAL);
        lbl_separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label lbl_coordsPrec = new Label(cmp_container, SWT.NONE);
        lbl_coordsPrec.setText(I18n.EDGER_Precision);

        BigDecimalSpinner spn_vequ = new BigDecimalSpinner(cmp_container, SWT.NONE);
        this.spn_vequ[0] = spn_vequ;
        spn_vequ.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spn_vequ.setMaximum(BigDecimal.ONE);
        spn_vequ.setMinimum(new BigDecimal(0));
        spn_vequ.setValue(es.getEqualDistance());

        Label lbl_use180deg = new Label(cmp_container, SWT.NONE);
        lbl_use180deg.setText(I18n.EDGER_Range);

        Combo cmb_b = new Combo(cmp_container, SWT.READ_ONLY);
        this.cmb_b[0] = cmb_b;
        cmb_b.setItems(new String[] { I18n.EDGER_0to90, I18n.EDGER_0to180 });
        cmb_b.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        cmb_b.setText(es.isExtendedRange() ? cmb_b.getItem(1) : cmb_b.getItem(0));
        cmb_b.select(es.isExtendedRange() ? 1 : 0);

        Label lbl_af = new Label(cmp_container, SWT.NONE);
        lbl_af.setText(I18n.EDGER_FlatMaxAngle);

        BigDecimalSpinner spn_af = new BigDecimalSpinner(cmp_container, SWT.NONE);
        this.spn_af[0] = spn_af;
        spn_af.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spn_af.setMaximum(new BigDecimal(180));
        spn_af.setMinimum(new BigDecimal(0));
        spn_af.setValue(es.getAf());

        Label lbl_ac = new Label(cmp_container, SWT.NONE);
        lbl_ac.setText(I18n.EDGER_CondlineMaxAngle);

        BigDecimalSpinner spn_ac = new BigDecimalSpinner(cmp_container, SWT.NONE);
        this.spn_ac[0] = spn_ac;
        spn_ac.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spn_ac.setMaximum(new BigDecimal(180));
        spn_ac.setMinimum(new BigDecimal(0));
        spn_ac.setValue(es.getAc());

        Label lbl_ae = new Label(cmp_container, SWT.NONE);
        lbl_ae.setText(I18n.EDGER_EdgeMaxAngle);

        BigDecimalSpinner spn_ae = new BigDecimalSpinner(cmp_container, SWT.NONE);
        this.spn_ae[0] = spn_ae;
        spn_ae.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spn_ae.setMaximum(new BigDecimal(180));
        spn_ae.setMinimum(new BigDecimal(0));
        spn_ae.setValue(es.getAe());

        Combo cmb_u = new Combo(cmp_container, SWT.READ_ONLY);
        this.cmb_u[0] = cmb_u;
        cmb_u.setItems(new String[] { I18n.EDGER_IncludeUnmatched, I18n.EDGER_ExcludeUnmatched, I18n.EDGER_UnmatchedOnly});
        cmb_u.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        cmb_u.setText(cmb_u.getItem(es.getUnmatchedMode()));
        cmb_u.select(es.getUnmatchedMode());

        Combo cmb_scope = new Combo(cmp_container, SWT.READ_ONLY);
        this.cmb_scope[0] = cmb_scope;
        cmb_scope.setItems(new String[] {I18n.EDGER_ScopeFileSubfiles, I18n.EDGER_ScopeFile, I18n.EDGER_ScopeSelection});
        cmb_scope.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        cmb_scope.setText(cmb_scope.getItem(es.getScope()));
        cmb_scope.select(es.getScope());

        Label lbl_1 = new Label(cmp_container, SWT.NONE);
        lbl_1.setText(I18n.EDGER_Condition1);
        Label lbl_2 = new Label(cmp_container, SWT.NONE);
        lbl_2.setText(I18n.EDGER_Condition2);
        Label lbl_3 = new Label(cmp_container, SWT.NONE);
        lbl_3.setText(I18n.EDGER_Condition3);
        Label lbl_4 = new Label(cmp_container, SWT.NONE);
        lbl_4.setText(I18n.EDGER_Condition4);

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
