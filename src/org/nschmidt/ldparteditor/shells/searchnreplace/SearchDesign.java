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
package org.nschmidt.ldparteditor.shells.searchnreplace;


import static org.nschmidt.ldparteditor.helpers.WidgetUtility.WidgetUtil;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.widgets.NButton;

class SearchDesign extends ApplicationWindow {

    final Point loc = new Point(0, 0);
    final NButton[] btn_find = new NButton[1];
    final NButton[] btn_replace = new NButton[1];
    final NButton[] btn_findAndReplace = new NButton[1];
    final NButton[] btn_replaceAll = new NButton[1];

    final NButton[] cb_caseSensitive = new NButton[1];
    final NButton[] rb_forward = new NButton[1];
    final NButton[] rb_all = new NButton[1];
    final NButton[] rb_selectedLines = new NButton[1];
    final NButton[] cb_incremental = new NButton[1];

    final Text[] txt_find = new Text[1];
    final Text[] txt_replace = new Text[1];


    SearchDesign(Shell txtEditorShell) {
        super(txtEditorShell);
    }

    /**
     * Create contents of the application window.
     *
     * @param parent
     */
    @Override
    protected Control createContents(Composite cmpContainer) {

        cmpContainer.setLayout(new GridLayout(1, true));
        GridLayout gridLayout = (GridLayout) cmpContainer.getLayout();
        gridLayout.verticalSpacing = 5;
        gridLayout.horizontalSpacing = 10;

        Label lblFind = new Label(cmpContainer, SWT.NONE);
        lblFind.setText(I18n.SEARCH_FIND);

        Text txtFind = new Text(cmpContainer, SWT.NONE);
        this.txt_find[0] = txtFind;
        txtFind.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));


        Label lblReplace = new Label(cmpContainer, SWT.NONE);
        lblReplace.setText(I18n.SEARCH_REPLACE_WITH);

        Text txtReplace = new Text(cmpContainer, SWT.NONE);
        this.txt_replace[0] = txtReplace;
        txtReplace.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Group grpDir = new Group(cmpContainer, SWT.NONE);
        grpDir.setText(I18n.SEARCH_DIRECTION);
        grpDir.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        grpDir.setLayout(new GridLayout(3, false));

        NButton rbPart = new NButton(grpDir, SWT.RADIO);
        this.rb_forward[0] = rbPart;
        rbPart.setText(I18n.SEARCH_FORWARD);
        rbPart.setSelection(true);

        NButton rbSubpart = new NButton(grpDir, SWT.RADIO);
        rbSubpart.setText(I18n.SEARCH_BACKWARD);

        grpDir.layout();


        Group grpScope = new Group(cmpContainer, SWT.NONE);
        grpScope.setText(I18n.SEARCH_SCOPE);
        grpScope.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        grpScope.setLayout(new GridLayout(3, false));

        NButton rbAll = new NButton(grpScope, SWT.RADIO);
        this.rb_all[0] = rbAll;
        rbAll.setText(I18n.SEARCH_ALL);
        rbAll.setSelection(true);

        NButton rbSelectedLines = new NButton(grpScope, SWT.RADIO);
        this.rb_selectedLines[0] = rbSelectedLines;
        rbSelectedLines.setText(I18n.SEARCH_SELECTED_LINES);

        grpScope.layout();

        Group grpLocation = new Group(cmpContainer, SWT.NONE);
        grpLocation.setText(I18n.SEARCH_OPTIONS);
        grpLocation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        grpLocation.setLayout(new GridLayout());

        NButton rbCurrentProject = new NButton(grpLocation, SWT.CHECK);
        this.cb_caseSensitive[0] = rbCurrentProject;
        rbCurrentProject.setText(I18n.SEARCH_CASE_SENSITIVE);

        NButton rbUnofficialLib = new NButton(grpLocation, SWT.CHECK);
        this.cb_incremental[0] = rbUnofficialLib;
        rbUnofficialLib.setText(I18n.SEARCH_INCREMENTAL);

        grpLocation.layout();

        Composite cmpBtnGrid = new Composite(cmpContainer, SWT.RIGHT_TO_LEFT);
        cmpBtnGrid.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, false, false, 1, 1));
        cmpBtnGrid.setLayout(new GridLayout(2, true));
        {
            NButton btnTmp = new NButton(cmpBtnGrid, SWT.NONE);
            this.btn_findAndReplace[0] = btnTmp;
            btnTmp.setEnabled(false);
            btnTmp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            btnTmp.setText(I18n.SEARCH_REPLACE_FIND);
        }
        {
            NButton btnTmp = new NButton(cmpBtnGrid, SWT.NONE);
            this.btn_find[0] = btnTmp;
            btnTmp.setEnabled(false);
            btnTmp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            btnTmp.setText(I18n.SEARCH_FIND_2);
        }
        {
            NButton btnTmp = new NButton(cmpBtnGrid, SWT.NONE);
            this.btn_replaceAll[0] = btnTmp;
            btnTmp.setEnabled(false);
            btnTmp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            btnTmp.setText(I18n.SEARCH_REPLACE_ALL);
        }
        {
            NButton btnTmp = new NButton(cmpBtnGrid, SWT.NONE);
            this.btn_replace[0] = btnTmp;
            btnTmp.setEnabled(false);
            btnTmp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            btnTmp.setText(I18n.SEARCH_REPLACE);
        }
        {
            NButton btnClose = new NButton(cmpBtnGrid, SWT.NONE);
            btnClose.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            btnClose.setText(I18n.SEARCH_CLOSE);

            WidgetUtil(btnClose).addSelectionListener(e -> getShell().close());
        }

        cmpContainer.pack();

        cmpContainer.setSize(cmpContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        loc.x = getParentShell().getLocation().x + getParentShell().getSize().x / 2 - getShell().getSize().x / 2;
        loc.y = getParentShell().getLocation().y + getParentShell().getSize().y / 2 - getShell().getSize().y / 2;

        return cmpContainer;
    }

    /**
     * Configure the shell.
     *
     * @param newShell
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
    }

    /**
     * Return the initial size of the window.
     */
    @Override
    protected Point getInitialSize() {
        return super.getInitialSize();
    }
}
