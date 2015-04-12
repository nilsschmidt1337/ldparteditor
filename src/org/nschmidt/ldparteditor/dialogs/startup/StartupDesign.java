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
package org.nschmidt.ldparteditor.dialogs.startup;

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.nschmidt.ldparteditor.i18n.I18n;

/**
 * This first dialog - shown on startup - asks for mandatory information about
 * the user.
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 *
 * @author nils
 *
 */
class StartupDesign extends Dialog {

    // Use final only for subclass/listener references!
    final Button[] btn_ok = new Button[1];
    final Text[] txt_ldrawPath = new Text[1];
    final Text[] txt_unofficialPath = new Text[1];
    final Text[] txt_ldrawUserName = new Text[1];
    final Text[] txt_realName = new Text[1];
    final Text[] txt_partAuthoringPath = new Text[1];
    final Combo[] cmb_license = new Combo[1];
    final Button[] btn_browseLdrawPath = new Button[1];
    final Button[] btn_browseUnofficialPath = new Button[1];
    final Button[] btn_browseAuthoringPath = new Button[1];

    StartupDesign(Shell parentShell) {
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
        gridLayout.verticalSpacing = 10;
        gridLayout.horizontalSpacing = 10;

        Label lbl_welcome = new Label(cmp_container, SWT.NONE);
        lbl_welcome.setText(I18n.STARTUP_WelcomeMessage);

        Label lbl_separator = new Label(cmp_container, SWT.SEPARATOR | SWT.HORIZONTAL);
        lbl_separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label lbl_firstPrompt = new Label(cmp_container, SWT.NONE);
        lbl_firstPrompt.setText(I18n.STARTUP_FirstPrompt);

        Label lbl_ldrawFolderQuestion = new Label(cmp_container, SWT.NONE);
        lbl_ldrawFolderQuestion.setText(I18n.STARTUP_LDrawFolderQuestion);

        Composite cmp_pathChooser1 = new Composite(cmp_container, SWT.NONE);
        cmp_pathChooser1.setLayout(new RowLayout(SWT.HORIZONTAL));

        Text txt_ldrawPath = new Text(cmp_pathChooser1, SWT.BORDER);
        this.txt_ldrawPath[0] = txt_ldrawPath;
        txt_ldrawPath.setEditable(false);
        txt_ldrawPath.setLayoutData(new RowData(294, SWT.DEFAULT));

        String ldrawDir = System.getenv("LDRAWDIR"); //$NON-NLS-1$
        if (ldrawDir != null) {
        	txt_ldrawPath.setText(ldrawDir);
        }

        Button btn_BrowseLdrawPath = new Button(cmp_pathChooser1, SWT.NONE);
        this.btn_browseLdrawPath[0] = btn_BrowseLdrawPath;
        btn_BrowseLdrawPath.setText(I18n.DIALOG_Browse);

        Label lbl_ldrawUserQuestion = new Label(cmp_container, SWT.NONE);
        lbl_ldrawUserQuestion.setText(I18n.STARTUP_LDrawUserQuestion);

        Text txt_ldrawUserName = new Text(cmp_container, SWT.BORDER);
        this.txt_ldrawUserName[0] = txt_ldrawUserName;
        txt_ldrawUserName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label lbl_realNameQuestion = new Label(cmp_container, SWT.NONE);
        lbl_realNameQuestion.setText(I18n.STARTUP_RealNameQuestion);

        Text txt_realName = new Text(cmp_container, SWT.BORDER);
        this.txt_realName[0] = txt_realName;
        txt_realName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label lbl_licenseQuestion = new Label(cmp_container, SWT.NONE);
        lbl_licenseQuestion.setText(I18n.STARTUP_LicenseQuestion);

        Combo cmb_license = new Combo(cmp_container, SWT.NONE);
        this.cmb_license[0] = cmb_license;
        cmb_license.setItems(new String[] { "0 !LICENSE Redistributable under CCAL version 2.0 : see CAreadme.txt", "0 !LICENSE Not redistributable : see NonCAreadme.txt" }); //$NON-NLS-1$ //$NON-NLS-2$
        cmb_license.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        cmb_license.setText("0 !LICENSE Redistributable under CCAL version 2.0 : see CAreadme.txt"); //$NON-NLS-1$
        cmb_license.select(0);

        Label lbl_authoringFolderQuestion = new Label(cmp_container, SWT.NONE);
        lbl_authoringFolderQuestion.setText(I18n.STARTUP_AuthoringFolderQuestion);

        Composite cmp_pathChooser2 = new Composite(cmp_container, SWT.NONE);
        cmp_pathChooser2.setLayout(new RowLayout(SWT.HORIZONTAL));

        Text txt_partAuthoringPath = new Text(cmp_pathChooser2, SWT.BORDER);
        this.txt_partAuthoringPath[0] = txt_partAuthoringPath;
        txt_partAuthoringPath.setEditable(false);
        txt_partAuthoringPath.setLayoutData(new RowData(294, SWT.DEFAULT));

        Button btn_browseAuthoringPath = new Button(cmp_pathChooser2, SWT.NONE);
        this.btn_browseAuthoringPath[0] = btn_browseAuthoringPath;
        btn_browseAuthoringPath.setText(I18n.DIALOG_Browse);

        Label lbl_unofficialPathQuestion = new Label(cmp_container, SWT.NONE);
        lbl_unofficialPathQuestion.setText(I18n.STARTUP_UnofficialPathQuestion);

        Composite cmp_pathChooser3 = new Composite(cmp_container, SWT.NONE);
        cmp_pathChooser3.setLayout(new RowLayout(SWT.HORIZONTAL));

        Text txt_unofficialPath = new Text(cmp_pathChooser3, SWT.BORDER);
        this.txt_unofficialPath[0] = txt_unofficialPath;
        txt_unofficialPath.setEditable(false);
        txt_unofficialPath.setLayoutData(new RowData(294, SWT.DEFAULT));

        if (ldrawDir != null) {
        	txt_unofficialPath.setText(ldrawDir + File.separator + "Unofficial");
        }

        Button btn_browseUnofficialPath = new Button(cmp_pathChooser3, SWT.NONE);
        this.btn_browseUnofficialPath[0] = btn_browseUnofficialPath;
        btn_browseUnofficialPath.setText(I18n.DIALOG_Browse);

        return cmp_container;
    }

    /**
     * Create contents of the button bar.
     *
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        btn_ok[0] = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
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
