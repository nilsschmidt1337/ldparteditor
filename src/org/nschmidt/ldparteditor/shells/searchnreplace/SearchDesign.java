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

public class SearchDesign extends ApplicationWindow {

    final Point loc = new Point(0, 0);
    final NButton[] btn_find = new NButton[1];
    final NButton[] btn_replace = new NButton[1];
    final NButton[] btn_findAndReplace = new NButton[1];
    final NButton[] btn_replaceAll = new NButton[1];

    final NButton[] cb_caseSensitive = new NButton[1];
    final NButton[] rb_forward = new NButton[1];
    final NButton[] rb_backward = new NButton[1];
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
    protected Control createContents(Composite cmp_container) {

        cmp_container.setLayout(new GridLayout(1, true));
        GridLayout gridLayout = (GridLayout) cmp_container.getLayout();
        gridLayout.verticalSpacing = 5;
        gridLayout.horizontalSpacing = 10;

        Label lbl_find = new Label(cmp_container, SWT.NONE);
        lbl_find.setText(I18n.SEARCH_Find);

        Text txt_find = new Text(cmp_container, SWT.NONE);
        this.txt_find[0] = txt_find;
        txt_find.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));


        Label lbl_replace = new Label(cmp_container, SWT.NONE);
        lbl_replace.setText(I18n.SEARCH_ReplaceWith);

        Text txt_replace = new Text(cmp_container, SWT.NONE);
        this.txt_replace[0] = txt_replace;
        txt_replace.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Group grp_dir = new Group(cmp_container, SWT.NONE);
        grp_dir.setText(I18n.SEARCH_Direction);
        grp_dir.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        grp_dir.setLayout(new GridLayout(3, false));

        NButton rb_part = new NButton(grp_dir, SWT.RADIO);
        this.rb_forward[0] = rb_part;
        rb_part.setText(I18n.SEARCH_Forward);
        rb_part.setSelection(true);

        NButton rb_subpart = new NButton(grp_dir, SWT.RADIO);
        this.rb_backward[0] = rb_subpart;
        rb_subpart.setText(I18n.SEARCH_Backward);

        grp_dir.layout();


        Group grp_scope = new Group(cmp_container, SWT.NONE);
        grp_scope.setText(I18n.SEARCH_Scope);
        grp_scope.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        grp_scope.setLayout(new GridLayout(3, false));

        NButton rb_all = new NButton(grp_scope, SWT.RADIO);
        this.rb_all[0] = rb_all;
        rb_all.setText(I18n.SEARCH_All);
        rb_all.setSelection(true);

        NButton rb_selectedLines = new NButton(grp_scope, SWT.RADIO);
        this.rb_selectedLines[0] = rb_selectedLines;
        rb_selectedLines.setText(I18n.SEARCH_SelectedLines);

        grp_scope.layout();

        Group grp_location = new Group(cmp_container, SWT.NONE);
        grp_location.setText(I18n.SEARCH_Options);
        grp_location.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        grp_location.setLayout(new GridLayout());

        NButton rb_currentProject = new NButton(grp_location, SWT.CHECK);
        this.cb_caseSensitive[0] = rb_currentProject;
        rb_currentProject.setText(I18n.SEARCH_CaseSensitive);

        NButton rb_unofficialLib = new NButton(grp_location, SWT.CHECK);
        this.cb_incremental[0] = rb_unofficialLib;
        rb_unofficialLib.setText(I18n.SEARCH_Incremental);

        grp_location.layout();

        Composite cmp_btnGrid = new Composite(cmp_container, SWT.RIGHT_TO_LEFT);
        cmp_btnGrid.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, false, false, 1, 1));
        cmp_btnGrid.setLayout(new GridLayout(2, true));
        {
            NButton btn_tmp = new NButton(cmp_btnGrid, SWT.NONE);
            this.btn_findAndReplace[0] = btn_tmp;
            btn_tmp.setEnabled(false);
            btn_tmp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            btn_tmp.setText(I18n.SEARCH_ReplaceFind);
        }
        {
            NButton btn_tmp = new NButton(cmp_btnGrid, SWT.NONE);
            this.btn_find[0] = btn_tmp;
            btn_tmp.setEnabled(false);
            btn_tmp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            btn_tmp.setText(I18n.SEARCH_Find2);
        }
        {
            NButton btn_tmp = new NButton(cmp_btnGrid, SWT.NONE);
            this.btn_replaceAll[0] = btn_tmp;
            btn_tmp.setEnabled(false);
            btn_tmp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            btn_tmp.setText(I18n.SEARCH_ReplaceAll);
        }
        {
            NButton btn_tmp = new NButton(cmp_btnGrid, SWT.NONE);
            this.btn_replace[0] = btn_tmp;
            btn_tmp.setEnabled(false);
            btn_tmp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            btn_tmp.setText(I18n.SEARCH_Replace);
        }
        {
            NButton btn_close = new NButton(cmp_btnGrid, SWT.NONE);
            btn_close.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            btn_close.setText(I18n.SEARCH_Close);

            WidgetUtil(btn_close).addXSelectionListener(e -> getShell().close());
        }

        cmp_container.pack();

        cmp_container.setSize(cmp_container.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        loc.x = getParentShell().getLocation().x + getParentShell().getSize().x / 2 - getShell().getSize().x / 2;
        loc.y = getParentShell().getLocation().y + getParentShell().getSize().y / 2 - getShell().getSize().y / 2;

        return cmp_container;
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
