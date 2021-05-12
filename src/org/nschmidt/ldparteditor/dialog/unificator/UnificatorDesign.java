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
package org.nschmidt.ldparteditor.dialog.unificator;

import static org.nschmidt.ldparteditor.helper.WidgetUtility.widgetUtil;

import java.math.BigDecimal;

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
import org.nschmidt.ldparteditor.helper.composite3d.UnificatorSettings;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.widget.BigDecimalSpinner;

/**
 * The unificator dialog
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 *
 * @author nils
 *
 */
class UnificatorDesign extends Dialog {

    final UnificatorSettings us;
    final BigDecimalSpinner[] spnVertexThresholdPtr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnSubfileThresholdPtr = new BigDecimalSpinner[1];

    final Combo[] cmbWhatToUnifyPtr = new Combo[1];
    final Combo[] cmbScopePtr = new Combo[1];

    // Use final only for subclass/listener references!

    UnificatorDesign(Shell parentShell, UnificatorSettings us) {
        super(parentShell);
        this.us = us;
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
        lblSpecify.setText(I18n.UNIFICATOR_TITLE);

        Label lblSeparator = new Label(cmpContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
        lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label lblHint = new Label(cmpContainer, SWT.NONE);
        lblHint.setText(I18n.UNIFICATOR_VERTEX_UNIFIATION);

        BigDecimalSpinner spnVertexThreshold = new BigDecimalSpinner(cmpContainer, SWT.NONE);
        this.spnVertexThresholdPtr [0] = spnVertexThreshold;
        spnVertexThreshold.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spnVertexThreshold.setMaximum(new BigDecimal(1000));
        spnVertexThreshold.setMinimum(new BigDecimal(0));
        spnVertexThreshold.setValue(us.getVertexThreshold());

        Label lblPrecision = new Label(cmpContainer, SWT.NONE);
        lblPrecision.setText(I18n.UNIFICATOR_VERTEX_SNAP);

        BigDecimalSpinner spnSubfileThreshold = new BigDecimalSpinner(cmpContainer, SWT.NONE);
        this.spnSubfileThresholdPtr [0] = spnSubfileThreshold;
        spnSubfileThreshold.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spnSubfileThreshold.setMaximum(new BigDecimal(1000));
        spnSubfileThreshold.setMinimum(new BigDecimal(0));
        spnSubfileThreshold.setValue(us.getSubvertexThreshold());

        Label lblSplitPlane = new Label(cmpContainer, SWT.NONE);
        lblSplitPlane.setText(I18n.UNIFICATOR_SNAP_ON);

        {
            Combo cmbSplitPlane = new Combo(cmpContainer, SWT.READ_ONLY);
            this.cmbWhatToUnifyPtr[0] = cmbSplitPlane;
            widgetUtil(cmbSplitPlane).setItems(I18n.UNIFICATOR_VERTICES, I18n.UNIFICATOR_SUBPART_VERTICES, I18n.UNIFICATOR_VERTICES_SUBPART_VERTICES);
            cmbSplitPlane.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmbSplitPlane.setText(cmbSplitPlane.getItem(us.getSnapOn()));
            cmbSplitPlane.select(us.getSnapOn());
        }

        Combo cmbScope = new Combo(cmpContainer, SWT.READ_ONLY);
        this.cmbScopePtr[0] = cmbScope;
        widgetUtil(cmbScope).setItems(I18n.UNIFICATOR_SCOPE_FILE, I18n.UNIFICATOR_SCOPE_SELECTION);
        cmbScope.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        cmbScope.setText(cmbScope.getItem(us.getScope()));
        cmbScope.select(us.getScope());

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
