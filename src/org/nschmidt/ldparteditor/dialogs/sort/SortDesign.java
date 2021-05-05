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
package org.nschmidt.ldparteditor.dialogs.sort;

import static org.nschmidt.ldparteditor.helpers.WidgetUtility.widgetUtil;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.widgets.NButton;

/**
 * The sort dialog
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 *
 * @author nils
 *
 */
class SortDesign extends Dialog {


    final Button[] btnOkPtr = new Button[1];

    final NButton[] btnIgnoreStructurePtr = new NButton[1];

    final Combo[] cmbScopePtr = new Combo[1];
    final Combo[] cmbSortCriteriaPtr = new Combo[1];

    final int fromLine;
    final int toLine;
    final DatFile fileNameObj;

    int scope;
    int criteria;

    boolean destructive = false;

    // Use final only for subclass/listener references!

    public SortDesign(Shell parentShell, int fromLine, int toLine, DatFile fileNameObj) {
        super(parentShell);
        this.fromLine = fromLine;
        this.toLine = toLine;
        this.fileNameObj = fileNameObj;
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
        lblSpecify.setText(I18n.SORT_TITLE);

        Label lblSeparator = new Label(cmpContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
        lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Combo cmbScope = new Combo(cmpContainer, SWT.READ_ONLY);
        this.cmbScopePtr[0] = cmbScope;
        widgetUtil(cmbScope).setItems(I18n.SORT_SCOPE_FILE, I18n.SORT_SCOPE_SELECTION);
        cmbScope.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        if (fromLine == toLine) {
            cmbScope.select(0);
            scope = 0;
        } else {
            cmbScope.select(1);
            scope = 1;
        }

        Combo cmbSortCriteria = new Combo(cmpContainer, SWT.READ_ONLY);
        this.cmbSortCriteriaPtr[0] = cmbSortCriteria;
        widgetUtil(cmbSortCriteria).setItems(I18n.SORT_BY_COLOUR_ASC, I18n.SORT_BY_COLOUR_DESC, I18n.SORT_BY_TYPE_ASC, I18n.SORT_BY_TYPE_DESC, I18n.SORT_BY_TYPE_COLOUR_ASC, I18n.SORT_BY_TYPE_COLOUR_DESC);
        cmbSortCriteria.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        cmbSortCriteria.select(2);
        criteria = 2;

        NButton btnIgnoreStructure = new NButton(cmpContainer, SWT.CHECK);
        this.btnIgnoreStructurePtr[0] = btnIgnoreStructure;
        btnIgnoreStructure.setText(I18n.SORT_IGNORE_STRUCTURE);
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
        btnOkPtr[0] = createButton(parent, IDialogConstants.OK_ID, I18n.DIALOG_OK, true);
        createButton(parent, IDialogConstants.CANCEL_ID, I18n.DIALOG_CANCEL, false);
    }
}
