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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
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


    final Button[] btn_OK = new Button[1];

    final NButton[] btn_ignoreStructure = new NButton[1];

    final Combo[] cmb_scope = new Combo[1];
    final Combo[] cmb_sortCriteria = new Combo[1];

    final StyledText st;
    final int fromLine;
    final int toLine;
    final DatFile fileNameObj;

    int scope;
    int criteria;

    boolean destructive = false;

    // Use final only for subclass/listener references!

    public SortDesign(Shell parentShell, StyledText st, int fromLine, int toLine, DatFile fileNameObj) {
        super(parentShell);
        this.st = st;
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
        Composite cmp_container = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = (GridLayout) cmp_container.getLayout();
        gridLayout.verticalSpacing = 10;
        gridLayout.horizontalSpacing = 10;

        Label lbl_specify = new Label(cmp_container, SWT.NONE);
        lbl_specify.setText(I18n.SORT_Title);

        Label lbl_separator = new Label(cmp_container, SWT.SEPARATOR | SWT.HORIZONTAL);
        lbl_separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Combo cmb_scope = new Combo(cmp_container, SWT.READ_ONLY);
        this.cmb_scope[0] = cmb_scope;
        cmb_scope.setItems(new String[] {I18n.SORT_ScopeFile, I18n.SORT_ScopeSelection});
        cmb_scope.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        if (fromLine == toLine) {
            cmb_scope.select(0);
            scope = 0;
        } else {
            cmb_scope.select(1);
            scope = 1;
        }

        Combo cmb_sortCriteria = new Combo(cmp_container, SWT.READ_ONLY);
        this.cmb_sortCriteria[0] = cmb_sortCriteria;
        cmb_sortCriteria.setItems(new String[] {I18n.SORT_ByColourAsc, I18n.SORT_ByColourDesc, I18n.SORT_ByTypeAsc, I18n.SORT_ByTypeDesc, I18n.SORT_ByTypeColourAsc, I18n.SORT_ByTypeColourDesc});
        cmb_sortCriteria.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        cmb_sortCriteria.select(2);
        criteria = 2;

        NButton btn_ignoreStructure = new NButton(cmp_container, SWT.CHECK);
        this.btn_ignoreStructure[0] = btn_ignoreStructure;
        btn_ignoreStructure.setText(I18n.SORT_IgnoreStructure);
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
        btn_OK[0] = createButton(parent, IDialogConstants.OK_ID, I18n.DIALOG_OK, true);
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
