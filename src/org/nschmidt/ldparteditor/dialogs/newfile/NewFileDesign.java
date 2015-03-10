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
package org.nschmidt.ldparteditor.dialogs.newfile;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * The new file dialog
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 *
 * @author nils
 *
 */
class NewFileDesign extends Dialog {


    final Button[] btn_create = new Button[1];
    final Button[] btn_createElsewhere = new Button[1];
    final Button[] cb_insertHeader = new Button[1];
    final Button[] cb_openIn3D = new Button[1];
    final Button[] cb_openInText = new Button[1];
    final Button[] rb_alias = new Button[1];
    final Button[] rb_currentProject = new Button[1];
    final Button[] rb_hrprimitive = new Button[1];
    final Button[] rb_none = new Button[1];
    final Button[] rb_part = new Button[1];
    final Button[] rb_pattern = new Button[1];
    final Button[] rb_physicalColour = new Button[1];
    final Button[] rb_primitive = new Button[1];
    final Button[] rb_shortcut = new Button[1];
    final Button[] rb_sticker = new Button[1];
    final Button[] rb_stickerf = new Button[1];
    final Button[] rb_subpart = new Button[1];
    final Button[] rb_unofficialLib = new Button[1];
    final Text[] txt_chooseName = new Text[1];

    // Use final only for subclass/listener references!

    NewFileDesign(Shell parentShell) {
        super(parentShell);
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
        gridLayout.verticalSpacing = 5;
        gridLayout.horizontalSpacing = 10;

        Label lbl_specify = new Label(cmp_container, SWT.NONE);
        lbl_specify.setText("Create a new file:"); //$NON-NLS-1$ I18N Needs translation!

        Label lbl_separator = new Label(cmp_container, SWT.SEPARATOR | SWT.HORIZONTAL);
        lbl_separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label lbl_chooseName = new Label(cmp_container, SWT.NONE);
        lbl_chooseName.setText("Choose a name:"); //$NON-NLS-1$ I18N Needs translation!

        Composite cmp_txt = new Composite(cmp_container, SWT.NONE);
        cmp_txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        cmp_txt.setLayout(new GridLayout(2, false));

        Text txt_chooseName = new Text(cmp_txt, SWT.NONE);
        this.txt_chooseName[0] = txt_chooseName;
        txt_chooseName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        txt_chooseName.setToolTipText("A hint is needed!"); //$NON-NLS-1$

        Label lbl_datEnding = new Label(cmp_txt, SWT.NONE);
        lbl_datEnding.setText(".dat"); //$NON-NLS-1$

        Button cb_openIn3D = new Button(cmp_container, SWT.CHECK);
        this.cb_openIn3D[0] = cb_openIn3D;
        cb_openIn3D.setText("Open in 3D editor if possible"); //$NON-NLS-1$ I18N Needs translation!

        Button cb_openInText = new Button(cmp_container, SWT.CHECK);
        this.cb_openInText[0] = cb_openInText;
        cb_openInText.setText("Open in text editor"); //$NON-NLS-1$ I18N Needs translation!

        Button cb_insertHeader = new Button(cmp_container, SWT.CHECK);
        this.cb_insertHeader[0] = cb_insertHeader;
        cb_insertHeader.setText("Insert file header"); //$NON-NLS-1$ I18N Needs translation!

        Group grp_type = new Group(cmp_container, SWT.NONE);
        grp_type.setText("Create as"); //$NON-NLS-1$ I18N Needs translation!
        grp_type.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        grp_type.setLayout(new GridLayout(3, false));

        Button rb_part = new Button(grp_type, SWT.RADIO);
        this.rb_part[0] = rb_part;
        rb_part.setText("part"); //$NON-NLS-1$ I18N Needs translation!
        rb_part.setSelection(true);

        Button rb_subpart = new Button(grp_type, SWT.RADIO);
        this.rb_subpart[0] = rb_subpart;
        rb_subpart.setText("subpart"); //$NON-NLS-1$ I18N Needs translation!

        Button rb_primitive = new Button(grp_type, SWT.RADIO);
        this.rb_primitive[0] = rb_primitive;
        rb_primitive.setText("primitive"); //$NON-NLS-1$ I18N Needs translation!

        Button rb_hrprimitive = new Button(grp_type, SWT.RADIO);
        this.rb_hrprimitive[0] = rb_hrprimitive;
        rb_hrprimitive.setText("hi-res primitive"); //$NON-NLS-1$ I18N Needs translation!

        Button rb_shortcut = new Button(grp_type, SWT.RADIO);
        this.rb_shortcut[0] = rb_shortcut;
        rb_shortcut.setText("shortcut"); //$NON-NLS-1$ I18N Needs translation!

        grp_type.layout();

        Group grp_suffix = new Group(cmp_container, SWT.NONE);
        grp_suffix.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        grp_suffix.setLayout(new GridLayout(3, false));

        Button rb_none = new Button(grp_suffix, SWT.RADIO);
        this.rb_none[0] = rb_none;
        rb_none.setText("(none)"); //$NON-NLS-1$ I18N Needs translation!
        rb_none.setSelection(true);

        Button rb_alias = new Button(grp_suffix, SWT.RADIO);
        this.rb_alias[0] = rb_alias;
        rb_alias.setText("alias"); //$NON-NLS-1$ I18N Needs translation!

        Button rb_physicalColour = new Button(grp_suffix, SWT.RADIO);
        this.rb_physicalColour[0] = rb_physicalColour;
        rb_physicalColour.setText("physical colour"); //$NON-NLS-1$ I18N Needs translation!

        Button rb_pattern = new Button(grp_suffix, SWT.RADIO);
        this.rb_pattern[0] = rb_pattern;
        rb_pattern.setText("pattern"); //$NON-NLS-1$ I18N Needs translation!

        Button rb_sticker = new Button(grp_suffix, SWT.RADIO);
        this.rb_sticker[0] = rb_sticker;
        rb_sticker.setText("sticker"); //$NON-NLS-1$ I18N Needs translation!

        Button rb_stickerf = new Button(grp_suffix, SWT.RADIO);
        this.rb_stickerf[0] = rb_stickerf;
        rb_stickerf.setText("formed sticker"); //$NON-NLS-1$ I18N Needs translation!

        grp_suffix.layout();

        Group grp_location = new Group(cmp_container, SWT.NONE);
        grp_location.setText("located at"); //$NON-NLS-1$ I18N Needs translation!
        grp_location.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        grp_location.setLayout(new GridLayout());

        Button rb_currentProject = new Button(grp_location, SWT.RADIO);
        this.rb_currentProject[0] = rb_currentProject;
        rb_currentProject.setText("the current project"); //$NON-NLS-1$ I18N Needs translation!
        rb_currentProject.setSelection(true);

        Button rb_unofficialLib = new Button(grp_location, SWT.RADIO);
        this.rb_unofficialLib[0] = rb_unofficialLib;
        rb_unofficialLib.setText("the unofficial library"); //$NON-NLS-1$ I18N Needs translation!

        grp_location.layout();

        Button btn_create = new Button(cmp_container, SWT.NONE);
        this.btn_create[0] = btn_create;
        btn_create.setText("Create it"); //$NON-NLS-1$ I18N Needs translation!
        btn_create.setEnabled(false);

        Label lbl_or = new Label(cmp_container, SWT.NONE);
        lbl_or.setText("- OR -"); //$NON-NLS-1$ I18N Needs translation!

        Button btn_createElsewhere = new Button(cmp_container, SWT.NONE);
        this.btn_createElsewhere[0] = btn_createElsewhere;
        btn_createElsewhere.setText("Create an empty file somewhere else"); //$NON-NLS-1$ I18N Needs translation!

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
