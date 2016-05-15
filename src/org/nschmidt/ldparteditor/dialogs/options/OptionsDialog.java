package org.nschmidt.ldparteditor.dialogs.options;

import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.helpers.Version;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.resources.ResourceManager;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.workbench.UserSettingState;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

public class OptionsDialog extends OptionsDesign {

    public OptionsDialog(Shell parentShell) {
        super(parentShell);
    }

    public void run() {
        final OptionsDialog me = this;
        this.setBlockOnOpen(true);
        this.setShellStyle(SWT.APPLICATION_MODAL | SWT.SHELL_TRIM ^ SWT.MIN);
        this.create();
        final Shell sh = this.getShell();
        sh.setText(Version.getApplicationName() + " " + Version.getVersion()); //$NON-NLS-1$
        sh.setImage(ResourceManager.getImage("imgDuke2.png")); //$NON-NLS-1$
        final UserSettingState userSettingState = WorkbenchManager.getUserSettingState();

        btn_OK[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Editor3DWindow.getWindow().compileAll();
                Editor3DWindow.getWindow().initAllRenderers();
                me.close();
            }
        });

        btn_AllowInvalidShapes[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                WorkbenchManager.getUserSettingState().setAllowInvalidShapes(btn_AllowInvalidShapes[0].getSelection());
            }
        });

        btn_browseLdrawPath[0].addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                DirectoryDialog dlg = new DirectoryDialog(getShell());

                // Set the initial filter to the last selected path
                dlg.setFilterPath(userSettingState.getLdrawFolderPath());

                // Change the title bar text
                dlg.setText(I18n.OPTIONS_LdrawFolder);

                // Customizable message displayed in the dialog
                dlg.setMessage(I18n.OPTIONS_Directory);

                // Calling open() will open and run the dialog.
                // It will return the selected directory, or
                // null if user cancels
                String dir = dlg.open();
                if (dir != null) {
                    // Set the text box to the new selection
                    txt_ldrawPath[0].setText(dir);
                    String ldrawPath = dir;
                    userSettingState.setLdrawFolderPath(ldrawPath);
                }
            }
        });
        btn_browseAuthoringPath[0].addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                DirectoryDialog dlg = new DirectoryDialog(getShell());

                // Set the initial filter to the last selected path
                dlg.setFilterPath(userSettingState.getAuthoringFolderPath());

                // Change the title bar text
                dlg.setText(I18n.OPTIONS_AuthoringWhere);

                // Customizable message displayed in the dialog
                dlg.setMessage(I18n.OPTIONS_Directory);

                // Calling open() will open and run the dialog.
                // It will return the selected directory, or
                // null if user cancels
                String dir = dlg.open();
                if (dir != null) {
                    // Set the text box to the new selection
                    txt_partAuthoringPath[0].setText(dir);
                    String partAuthoringPath = dir;
                    userSettingState.setAuthoringFolderPath(partAuthoringPath);
                }
            }
        });
        btn_browseUnofficialPath[0].addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                DirectoryDialog dlg = new DirectoryDialog(getShell());

                // Set the initial filter to the last selected path
                dlg.setFilterPath(userSettingState.getUnofficialFolderPath());

                // Change the title bar text
                dlg.setText(I18n.OPTIONS_UnofficialWhere);

                // Customizable message displayed in the dialog
                dlg.setMessage(I18n.OPTIONS_Directory);

                // Calling open() will open and run the dialog.
                // It will return the selected directory, or
                // null if user cancels
                String dir = dlg.open();
                if (dir != null) {
                    // Set the text box to the new selection
                    txt_unofficialPath[0].setText(dir);
                    String unofficialPath = dir;
                    userSettingState.setUnofficialFolderPath(unofficialPath);
                }
            }
        });
        txt_ldrawUserName[0].addListener(SWT.Modify, new Listener() {
            @Override
            public void handleEvent(Event e) {
                String ldrawUserName = txt_ldrawUserName[0].getText();
                userSettingState.setLdrawUserName(ldrawUserName);
            }
        });
        txt_realName[0].addListener(SWT.Modify, new Listener() {
            @Override
            public void handleEvent(Event e) {
                String realName = txt_realName[0].getText();
                userSettingState.setRealUserName(realName);
            }
        });
        cmb_license[0].addListener(SWT.Modify, new Listener() {
            @Override
            public void handleEvent(Event e) {
                String license = cmb_license[0].getText();
                userSettingState.setLicense(license);
            }
        });
        cmb_locale[0].addListener(SWT.Modify, new Listener() {
            @Override
            public void handleEvent(Event e) {
                if (localeMap.containsKey(cmb_locale[0].getText())) {
                    Locale locale = localeMap.get(cmb_locale[0].getText());
                    userSettingState.setLocale(locale);
                    MyLanguage.LOCALE = locale;
                }
            }
        });
        this.open();
    }

    @Override
    protected void handleShellCloseEvent() {
        Editor3DWindow.getWindow().compileAll();
        Editor3DWindow.getWindow().initAllRenderers();
        super.handleShellCloseEvent();
    }

    // FIXME OptionsDialog needs implementation!
}
