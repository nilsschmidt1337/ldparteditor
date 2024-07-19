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
package org.nschmidt.ldparteditor.dialog.startup;

import static org.nschmidt.ldparteditor.helper.WidgetUtility.widgetUtil;

import java.io.File;
import java.util.Arrays;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.enumtype.MyLanguage;
import org.nschmidt.ldparteditor.helper.FileHelper;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.resource.ResourceManager;
import org.nschmidt.ldparteditor.workbench.UserSettingState;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * This first dialog - shown on startup - asks for mandatory information about
 * the environment.
 * <p>
 * Note: This class should be instantiated, it defines all listeners and part of
 * the business logic. It overrides the {@code open()} method to invoke the
 * listener definitions ;)
 */
public class StartupDialog extends StartupDesign {

    private static final String SELECT_A_DIRECTORY = "Select a Directory"; //$NON-NLS-1$ NO_I18N!!
    private static final String UNOFFICIAL = "Unofficial"; //$NON-NLS-1$

    private String partAuthoringPath = ""; //$NON-NLS-1$
    private String ldrawPath = ""; //$NON-NLS-1$

    private String unofficialPath = ""; //$NON-NLS-1$
    private String ldrawUserName = ""; //$NON-NLS-1$
    private String license = "0 !LICENSE Licensed under CC BY 4.0 : see CAreadme.txt"; //$NON-NLS-1$
    private String realName = ""; //$NON-NLS-1$
    private Locale locale = Locale.US;

    private boolean canReadFromLdrawPath = false;
    private boolean canReadAndWriteToPartAuthoringFolder = false;
    private boolean canReadFromUnofficialLibraryPath = false;

    /**
     * Create the dialog.
     *
     * @param parentShell
     */
    public StartupDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    public int open() {

        String ldrawDir = System.getenv("LDRAWDIR"); //$NON-NLS-1$
        if (ldrawDir != null) {
            setLdrawPath(ldrawDir);
            setUnofficialPath(ldrawDir + File.separator + UNOFFICIAL);
            canReadFromLdrawPath = FileHelper.canReadFromPath(ldrawPath);
            canReadFromUnofficialLibraryPath = FileHelper.canReadFromPath(unofficialPath);
        }

        super.create();
        btnOkPtr[0].setEnabled(false);
        // MARK All final listeners will be configured here..
        widgetUtil(btnBrowseLdrawPathPtr[0]).addSelectionListener(e -> {
            DirectoryDialog dlg = new DirectoryDialog(getShell());

            // Set the initial filter to null
            dlg.setFilterPath(null);

            // Change the title bar text
            dlg.setText("Define the LDraw™ Folder Path:"); //$NON-NLS-1$ NO_I18N!!

            // Customizable message displayed in the dialog
            dlg.setMessage(SELECT_A_DIRECTORY);

            // Calling open() will open and run the dialog.
            // It will return the selected directory, or
            // null if user cancels
            String dir = dlg.open();
            if (dir != null) {
                // Set the text box to the new selection
                txtLdrawPathPtr[0].setText(dir);
                ldrawPath = dir;
                canReadFromLdrawPath = FileHelper.canReadFromPath(ldrawPath);

                if (canReadFromLdrawPath && unofficialPath.isEmpty()) {
                    if (ldrawPath.endsWith(File.separator)) {
                        unofficialPath = ldrawPath + UNOFFICIAL;
                    } else {
                        unofficialPath = ldrawPath + File.separator + UNOFFICIAL;
                    }
                    if (FileHelper.canWriteToPath(ldrawPath)) {
                        try {
                            File unofficialFolder = new File(unofficialPath);
                            if (!unofficialFolder.exists()) {
                                unofficialFolder.mkdir();
                            }
                            canReadFromUnofficialLibraryPath = true;
                        } catch (SecurityException s) {
                            NLogger.error(getClass(), "Failed to create unofficial library folder."); //$NON-NLS-1$
                            unofficialPath = ""; //$NON-NLS-1$
                        }

                        txtUnofficialPathPtr[0].setText(unofficialPath);
                    }
                }

                updateOkButtonEnabledState();
            }
        });
        widgetUtil(btnBrowseAuthoringPathPtr[0]).addSelectionListener(e -> {
            DirectoryDialog dlg = new DirectoryDialog(getShell());

            // Set the initial filter to null
            dlg.setFilterPath(null);

            // Change the title bar text
            dlg.setText("Where is your parts authoring folder located?"); //$NON-NLS-1$ NO_I18N!!

            // Customizable message displayed in the dialog
            dlg.setMessage(SELECT_A_DIRECTORY);

            // Calling open() will open and run the dialog.
            // It will return the selected directory, or
            // null if user cancels
            String dir = dlg.open();
            if (dir != null) {
                // Set the text box to the new selection
                txtPartAuthoringPathPtr[0].setText(dir);
                partAuthoringPath = dir;
                canReadAndWriteToPartAuthoringFolder = FileHelper.canReadFromPath(partAuthoringPath) && FileHelper.canWriteToPath(partAuthoringPath);
                updateOkButtonEnabledState();
            }
        });
        widgetUtil(btnBrowseUnofficialPathPtr[0]).addSelectionListener(e -> {
            DirectoryDialog dlg = new DirectoryDialog(getShell());

            // Set the initial filter to null
            dlg.setFilterPath(null);

            // Change the title bar text
            dlg.setText("Where is your unofficial parts folder located?"); //$NON-NLS-1$ NO_I18N!!

            // Customizable message displayed in the dialog
            dlg.setMessage(SELECT_A_DIRECTORY);

            // Calling open() will open and run the dialog.
            // It will return the selected directory, or
            // null if user cancels
            String dir = dlg.open();
            if (dir != null) {
                // Set the text box to the new selection
                txtUnofficialPathPtr[0].setText(dir);
                unofficialPath = dir;
                canReadFromUnofficialLibraryPath = FileHelper.canReadFromPath(unofficialPath);
                updateOkButtonEnabledState();
            }
        });
        txtLdrawUserNamePtr[0].addListener(SWT.Modify, e -> {
            ldrawUserName = txtLdrawUserNamePtr[0].getText();
            updateOkButtonEnabledState();
        });
        txtRealNamePtr[0].addListener(SWT.Modify, e -> {
            realName = txtRealNamePtr[0].getText();
            updateOkButtonEnabledState();
        });
        cmbLicensePtr[0].addListener(SWT.Modify, e -> {
            license = cmbLicensePtr[0].getText();
            updateOkButtonEnabledState();
        });
        cmbLocalePtr[0].addListener(SWT.Modify, e -> {
            if (localeMap.containsKey(cmbLocalePtr[0].getText())) {
                locale = localeMap.get(cmbLocalePtr[0].getText());
            }
        });
        btnOkPtr[0].addListener(SWT.Selection, event -> {
            UserSettingState userSettingState = new UserSettingState();
            userSettingState.setAuthoringFolderPath(partAuthoringPath);
            userSettingState.setLdrawFolderPath(ldrawPath);
            userSettingState.setUnofficialFolderPath(unofficialPath);
            userSettingState.setLdrawUserName(ldrawUserName);
            userSettingState.setLicense(license);
            userSettingState.setRealUserName(realName);
            userSettingState.setUsingRelativePaths(false);
            userSettingState.setLocale(locale);
            MyLanguage.setLocale(locale);
            WorkbenchManager.setUserSettingState(userSettingState);
            WorkbenchManager.setThemeSettingState(new UserSettingState());
        });
        return super.open();
    }

    public void setLdrawPath(String ldrawPath) {
        this.ldrawPath = ldrawPath;
    }

    public void setUnofficialPath(String unofficialPath) {
        this.unofficialPath = unofficialPath;
    }

    private void updateOkButtonEnabledState() {

        lblFormStatusIconPtr[0].setImage(ResourceManager.getImage("icon16_warning.png", 16)); //$NON-NLS-1$

        Label labelForStatus = lblFormStatusPtr[0];

        if (txtLdrawUserNamePtr[0].isFocusControl() && !ldrawUserName.isEmpty()) {
            lblFormStatusPtr[0].setText("Enter your LDraw™ user name and then:"); //$NON-NLS-1$
            labelForStatus = lblCurrentActionPtr[0];
        } else if (txtRealNamePtr[0].isFocusControl() && !realName.isEmpty()) {
            lblFormStatusPtr[0].setText("Enter a real name and then:"); //$NON-NLS-1$
            labelForStatus = lblCurrentActionPtr[0];
        } else if (cmbLicensePtr[0].isFocusControl() && !license.isEmpty()
                && !Arrays.asList(cmbLicensePtr[0].getItems()).contains(license)) {
            lblFormStatusPtr[0].setText("Choose a license for your work and then:"); //$NON-NLS-1$
            labelForStatus = lblCurrentActionPtr[0];
        } else {
            lblFormStatusPtr[0].setText(""); //$NON-NLS-1$
            lblCurrentActionPtr[0].setText(""); //$NON-NLS-1$
        }

        if (!canReadFromLdrawPath) {
            labelForStatus.setText("Choose a LDraw™ library folder with read access."); //$NON-NLS-1$ NO_I18N!!
        } else if (ldrawUserName.isEmpty()) {
            labelForStatus.setText("Enter your LDraw™ user name."); //$NON-NLS-1$ NO_I18N!!
        } else if (realName.isEmpty()) {
            labelForStatus.setText("Enter a real name."); //$NON-NLS-1$ NO_I18N!!
        } else if (license.isEmpty()) {
            labelForStatus.setText("Choose a license for your work."); //$NON-NLS-1$ NO_I18N!!
        } else if (!canReadAndWriteToPartAuthoringFolder) {
            labelForStatus.setText("Choose a part authoring folder with read and write access."); //$NON-NLS-1$ NO_I18N!!
        } else if (!canReadFromUnofficialLibraryPath) {
            labelForStatus.setText("Choose an unofficial library folder with read access."); //$NON-NLS-1$ NO_I18N!!
        } else {
            lblFormStatusIconPtr[0].setImage(ResourceManager.getImage("icon16_info.png", 16)); //$NON-NLS-1$
            lblFormStatusPtr[0].setText("Setup is complete."); //$NON-NLS-1$
            lblCurrentActionPtr[0].setText("Press OK to continue."); //$NON-NLS-1$ NO_I18N!!
        }

        lblFormStatusPtr[0].getParent().getParent().layout();

        btnOkPtr[0].setEnabled(
                canReadFromLdrawPath
                && canReadAndWriteToPartAuthoringFolder
                && canReadFromUnofficialLibraryPath
                && !ldrawUserName.isEmpty()
                && !license.isEmpty()
                && !realName.isEmpty());
    }
}
