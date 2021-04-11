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
import java.text.Collator;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
import org.nschmidt.ldparteditor.resources.ResourceManager;
import org.nschmidt.ldparteditor.widgets.NButton;

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
    final Button[] btnOkPtr = new Button[1];
    final Combo[] cmbLocalePtr = new Combo[1];
    final Text[] txtLdrawPathPtr = new Text[1];
    final Text[] txtUnofficialPathPtr = new Text[1];
    final Text[] txtLdrawUserNamePtr = new Text[1];
    final Text[] txtRealNamePtr = new Text[1];
    final Text[] txtPartAuthoringPathPtr = new Text[1];
    final Combo[] cmbLicensePtr = new Combo[1];
    final Label[] lblFormStatusIconPtr = new Label[1];
    final Label[] lblFormStatusPtr = new Label[1];
    final NButton[] btnBrowseLdrawPathPtr = new NButton[1];
    final NButton[] btnBrowseUnofficialPathPtr = new NButton[1];
    final NButton[] btnBrowseAuthoringPathPtr = new NButton[1];

    final Map<String, Locale> localeMap = new HashMap<>();

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
        Composite cmpContainer = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = (GridLayout) cmpContainer.getLayout();
        gridLayout.verticalSpacing = 10;
        gridLayout.horizontalSpacing = 10;

        Label lblWelcome = new Label(cmpContainer, SWT.NONE);
        lblWelcome.setText("Welcome to LD Part Editor!"); //$NON-NLS-1$ NO_I18N!!

        Label lblSeparator = new Label(cmpContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
        lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label lblFirstPrompt = new Label(cmpContainer, SWT.NONE);
        lblFirstPrompt.setText("Please answer the following questions on the first start of this program:"); //$NON-NLS-1$ NO_I18N!!

        Label lblLocale = new Label(cmpContainer, SWT.NONE);
        lblLocale.setText("Choose your locale:"); //$NON-NLS-1$ NO_I18N!!

        Combo cmbLocale = new Combo(cmpContainer, SWT.READ_ONLY);
        this.cmbLocalePtr[0] = cmbLocale;

        String[] locales = new String[DateFormat.getAvailableLocales().length];
        Locale[] locs = DateFormat.getAvailableLocales();
        Arrays.sort(locs, (o1, o2) ->
            Collator.getInstance(Locale.ENGLISH).compare(o1.getDisplayName(Locale.ENGLISH), o2.getDisplayName(Locale.ENGLISH))
        );
        localeMap.clear();
        int englishIndex = 0;
        for (int i = 0; i < locales.length; i++) {
            locales[i] = locs[i].getDisplayName();
            localeMap.put(locales[i], locs[i]);
            if (locs[i].equals(Locale.US)) {
                englishIndex = i;
            }
        }

        cmbLocale.setItems(locales);
        cmbLocale.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        cmbLocale.select(englishIndex);

        Label lblLdrawFolderQuestion = new Label(cmpContainer, SWT.NONE);
        lblLdrawFolderQuestion.setText("Where is your LDraw folder located?"); //$NON-NLS-1$ NO_I18N!!

        Composite cmpPathChooser1 = new Composite(cmpContainer, SWT.NONE);
        cmpPathChooser1.setLayout(new RowLayout(SWT.HORIZONTAL));

        Text txtLdrawPath = new Text(cmpPathChooser1, SWT.BORDER);
        this.txtLdrawPathPtr[0] = txtLdrawPath;
        txtLdrawPath.setEditable(false);
        txtLdrawPath.setLayoutData(new RowData(294, SWT.DEFAULT));

        String ldrawDir = System.getenv("LDRAWDIR"); //$NON-NLS-1$
        if (ldrawDir != null) {
            txtLdrawPath.setText(ldrawDir);
        }

        NButton btnBrowseLdrawPath = new NButton(cmpPathChooser1, SWT.NONE);
        this.btnBrowseLdrawPathPtr[0] = btnBrowseLdrawPath;
        btnBrowseLdrawPath.setText("Browse..."); //$NON-NLS-1$ NO_I18N!!

        Label lblLdrawUserQuestion = new Label(cmpContainer, SWT.NONE);
        lblLdrawUserQuestion.setText("What is your LDraw user name?"); //$NON-NLS-1$ NO_I18N!!

        Text txtLdrawUserName = new Text(cmpContainer, SWT.BORDER);
        this.txtLdrawUserNamePtr[0] = txtLdrawUserName;
        txtLdrawUserName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label lblRealNameQuestion = new Label(cmpContainer, SWT.NONE);
        lblRealNameQuestion.setText("What is your real name?"); //$NON-NLS-1$ NO_I18N!!

        Text txtRealName = new Text(cmpContainer, SWT.BORDER);
        this.txtRealNamePtr[0] = txtRealName;
        txtRealName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label lblLicenseQuestion = new Label(cmpContainer, SWT.NONE);
        lblLicenseQuestion.setText("Under which license do you want to publish your work?"); //$NON-NLS-1$ NO_I18N!!

        Combo cmbLicense = new Combo(cmpContainer, SWT.NONE);
        this.cmbLicensePtr[0] = cmbLicense;
        cmbLicense.setItems(new String[] { "0 !LICENSE Redistributable under CCAL version 2.0 : see CAreadme.txt", "0 !LICENSE Not redistributable : see NonCAreadme.txt" }); //$NON-NLS-1$ //$NON-NLS-2$
        cmbLicense.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        cmbLicense.setText("0 !LICENSE Redistributable under CCAL version 2.0 : see CAreadme.txt"); //$NON-NLS-1$
        cmbLicense.select(0);

        Label lblAuthoringFolderQuestion = new Label(cmpContainer, SWT.NONE);
        lblAuthoringFolderQuestion.setText("Define the Part Authoring Folder Path:"); //$NON-NLS-1$ NO_I18N!!

        Composite cmpPathChooser2 = new Composite(cmpContainer, SWT.NONE);
        cmpPathChooser2.setLayout(new RowLayout(SWT.HORIZONTAL));

        Text txtPartAuthoringPath = new Text(cmpPathChooser2, SWT.BORDER);
        this.txtPartAuthoringPathPtr[0] = txtPartAuthoringPath;
        txtPartAuthoringPath.setEditable(false);
        txtPartAuthoringPath.setLayoutData(new RowData(294, SWT.DEFAULT));

        NButton btnBrowseAuthoringPath = new NButton(cmpPathChooser2, SWT.NONE);
        this.btnBrowseAuthoringPathPtr[0] = btnBrowseAuthoringPath;
        btnBrowseAuthoringPath.setText("Browse..."); //$NON-NLS-1$ NO_I18N!!

        Label lblUnofficialPathQuestion = new Label(cmpContainer, SWT.NONE);
        lblUnofficialPathQuestion.setText("Define the Folder Path for Unofficial Parts:"); //$NON-NLS-1$ NO_I18N!!

        Composite cmpPathChooser3 = new Composite(cmpContainer, SWT.NONE);
        cmpPathChooser3.setLayout(new RowLayout(SWT.HORIZONTAL));

        Text txtUnofficialPath = new Text(cmpPathChooser3, SWT.BORDER);
        this.txtUnofficialPathPtr[0] = txtUnofficialPath;
        txtUnofficialPath.setEditable(false);
        txtUnofficialPath.setLayoutData(new RowData(294, SWT.DEFAULT));

        if (ldrawDir != null) {
            txtUnofficialPath.setText(ldrawDir + File.separator + "Unofficial"); //$NON-NLS-1$
        }

        NButton btnBrowseUnofficialPath = new NButton(cmpPathChooser3, SWT.NONE);
        this.btnBrowseUnofficialPathPtr[0] = btnBrowseUnofficialPath;
        btnBrowseUnofficialPath.setText("Browse..."); //$NON-NLS-1$ NO_I18N!!

        Composite cmpFormStatus = new Composite(cmpContainer, SWT.NONE);
        cmpFormStatus.setLayout(new RowLayout(SWT.HORIZONTAL));

        Label lblFormStatusIcon = new Label(cmpFormStatus, SWT.NONE);
        this.lblFormStatusIconPtr[0] = lblFormStatusIcon;
        lblFormStatusIcon.setImage(ResourceManager.getImage("icon16_info.png", 16)); //$NON-NLS-1$

        Label lblFormStatus = new Label(cmpFormStatus, SWT.NONE);
        this.lblFormStatusPtr[0] = lblFormStatus;
        lblFormStatus.setText("Please complete the form."); //$NON-NLS-1$ NO_I18N!!

        return cmpContainer;
    }

    /**
     * Create contents of the button bar.
     *
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        btnOkPtr[0] = createButton(parent, IDialogConstants.OK_ID, "OK", true); //$NON-NLS-1$ NO_I18N!!
        createButton(parent, IDialogConstants.CANCEL_ID, "Cancel", false); //$NON-NLS-1$ NO_I18N!!
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return super.getInitialSize();
    }
}
