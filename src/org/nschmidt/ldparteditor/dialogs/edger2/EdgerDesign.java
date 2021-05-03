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

import static org.nschmidt.ldparteditor.helpers.WidgetUtility.widgetUtil;

import java.math.BigDecimal;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
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
import org.nschmidt.ldparteditor.widgets.NButton;

/**
 * The edger2 dialog
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 *
 * @author nils
 *
 */
class EdgerDesign extends TrayDialog {

    final Edger2Settings es;

    // Use final only for subclass/listener references!
    final BigDecimalSpinner[] spnVequPtr = new BigDecimalSpinner[1];

    final BigDecimalSpinner[] spnAfPtr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnAcPtr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnAePtr = new BigDecimalSpinner[1];
    final Combo[] cmbBPtr = new Combo[1];
    final Combo[] cmbCPtr = new Combo[1];
    final Combo[] cmbUPtr = new Combo[1];
    final Combo[] cmbScopePtr = new Combo[1];
    final NButton[] btnVerbosePtr = new NButton[1];

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
        Composite cmpContainer = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = (GridLayout) cmpContainer.getLayout();
        gridLayout.verticalSpacing = 10;
        gridLayout.horizontalSpacing = 10;

        Label lblSpecify = new Label(cmpContainer, SWT.NONE);
        lblSpecify.setText(I18n.EDGER_TITLE);

        Label lblSeparator = new Label(cmpContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
        lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label lblCoordsPrec = new Label(cmpContainer, SWT.NONE);
        lblCoordsPrec.setText(I18n.EDGER_PRECISION);

        BigDecimalSpinner spnVequ = new BigDecimalSpinner(cmpContainer, SWT.NONE);
        this.spnVequPtr[0] = spnVequ;
        spnVequ.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spnVequ.setMaximum(BigDecimal.ONE);
        spnVequ.setMinimum(new BigDecimal(0));
        spnVequ.setValue(es.getEqualDistance());

        Label lblUse180deg = new Label(cmpContainer, SWT.NONE);
        lblUse180deg.setText(I18n.EDGER_RANGE);

        Combo cmbB = new Combo(cmpContainer, SWT.READ_ONLY);
        this.cmbBPtr[0] = cmbB;
        widgetUtil(cmbB).setItems(I18n.EDGER_0_TO_90, I18n.EDGER_0_TO_180);
        cmbB.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        cmbB.setText(es.isExtendedRange() ? cmbB.getItem(1) : cmbB.getItem(0));
        cmbB.select(es.isExtendedRange() ? 1 : 0);

        Combo cmbC = new Combo(cmpContainer, SWT.READ_ONLY);
        this.cmbCPtr[0] = cmbC;
        widgetUtil(cmbC).setItems(I18n.EDGER_CONDLINE_ON_QUAD_OFF, I18n.EDGER_CONDLINE_ON_QUAD_ON);
        cmbC.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        cmbC.setText(es.isCondlineOnQuads() ? cmbC.getItem(1) : cmbC.getItem(0));
        cmbC.select(es.isCondlineOnQuads() ? 1 : 0);

        Label lblAf = new Label(cmpContainer, SWT.NONE);
        lblAf.setText(I18n.EDGER_FLAT_MAX_ANGLE);

        BigDecimalSpinner spnAf = new BigDecimalSpinner(cmpContainer, SWT.NONE);
        this.spnAfPtr[0] = spnAf;
        spnAf.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spnAf.setMaximum(new BigDecimal(180));
        spnAf.setMinimum(new BigDecimal(0));
        spnAf.setValue(es.getAf());

        Label lblAc = new Label(cmpContainer, SWT.NONE);
        lblAc.setText(I18n.EDGER_CONDLINE_MAX_ANGLE);

        BigDecimalSpinner spnAc = new BigDecimalSpinner(cmpContainer, SWT.NONE);
        this.spnAcPtr[0] = spnAc;
        spnAc.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spnAc.setMaximum(new BigDecimal(180));
        spnAc.setMinimum(new BigDecimal(0));
        spnAc.setValue(es.getAc());

        Label lblAe = new Label(cmpContainer, SWT.NONE);
        lblAe.setText(I18n.EDGER_EDGE_MAX_ANGLE);

        BigDecimalSpinner spnAe = new BigDecimalSpinner(cmpContainer, SWT.NONE);
        this.spnAePtr[0] = spnAe;
        spnAe.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spnAe.setMaximum(new BigDecimal(180));
        spnAe.setMinimum(new BigDecimal(0));
        spnAe.setValue(es.getAe());

        Combo cmbU = new Combo(cmpContainer, SWT.READ_ONLY);
        this.cmbUPtr[0] = cmbU;
        widgetUtil(cmbU).setItems(I18n.EDGER_INCLUDE_UNMATCHED, I18n.EDGER_EXCLUDE_UNMATCHED, I18n.EDGER_UNMATCHED_ONLY);
        cmbU.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        cmbU.setText(cmbU.getItem(es.getUnmatchedMode()));
        cmbU.select(es.getUnmatchedMode());

        Combo cmbScope = new Combo(cmpContainer, SWT.READ_ONLY);
        this.cmbScopePtr[0] = cmbScope;
        widgetUtil(cmbScope).setItems(I18n.EDGER_SCOPE_FILE_SUBFILES, I18n.EDGER_SCOPE_FILE, I18n.EDGER_SCOPE_SELECTION);
        cmbScope.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        cmbScope.setText(cmbScope.getItem(es.getScope()));
        cmbScope.select(es.getScope());

        NButton btnVerbose = new NButton(cmpContainer, SWT.CHECK);
        this.btnVerbosePtr[0] = btnVerbose;
        btnVerbose.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnVerbose.setText(I18n.EDGER_VERBOSE);
        btnVerbose.setSelection(es.isVerbose());

        Label lbl1 = new Label(cmpContainer, SWT.NONE);
        lbl1.setText(I18n.EDGER_CONDITION_1);
        Label lbl2 = new Label(cmpContainer, SWT.NONE);
        lbl2.setText(I18n.EDGER_CONDITION_2);
        Label lbl3 = new Label(cmpContainer, SWT.NONE);
        lbl3.setText(I18n.EDGER_CONDITION_3);
        Label lbl4 = new Label(cmpContainer, SWT.NONE);
        lbl4.setText(I18n.EDGER_CONDITION_4);

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
