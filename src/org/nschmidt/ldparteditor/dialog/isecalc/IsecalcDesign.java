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
package org.nschmidt.ldparteditor.dialog.isecalc;

import static org.nschmidt.ldparteditor.helper.WidgetUtility.widgetUtil;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.dialog.ThemedDialog;
import org.nschmidt.ldparteditor.helper.composite3d.IsecalcSettings;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.widget.NButton;
import org.nschmidt.ldparteditor.workbench.Theming;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * The rounding precision dialog
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 */
class IsecalcDesign extends ThemedDialog {

    final IsecalcSettings is;
    final Combo[] cmbScopePtr = new Combo[1];
    final NButton[] btnVerbosePtr = new NButton[1];

    // Use final only for subclass/listener references!

    IsecalcDesign(Shell parentShell, IsecalcSettings is) {
        super(parentShell);
        this.is = is;
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

        Label lblSpecify = Theming.label(cmpContainer, SWT.NONE);
        lblSpecify.setText(I18n.ISECALC_TITLE);

        Label lblSeparator = Theming.label(cmpContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
        lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label lblHint = Theming.label(cmpContainer, SWT.NONE);
        lblHint.setText(I18n.ISECALC_HINT);

        Combo cmbScope = Theming.combo(cmpContainer, SWT.READ_ONLY);
        this.cmbScopePtr[0] = cmbScope;
        widgetUtil(cmbScope).setItems(I18n.ISECALC_SCOPE_FILE, I18n.ISECALC_SCOPE_SELECTION);
        cmbScope.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        cmbScope.setText(cmbScope.getItem(is.getScope()));
        cmbScope.select(is.getScope());

        NButton btnVerbose = new NButton(cmpContainer, SWT.CHECK);
        this.btnVerbosePtr[0] = btnVerbose;
        btnVerbose.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnVerbose.setText(I18n.ISECALC_VERBOSE);
        btnVerbose.setSelection(WorkbenchManager.getUserSettingState().isVerboseIsecalc());

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
