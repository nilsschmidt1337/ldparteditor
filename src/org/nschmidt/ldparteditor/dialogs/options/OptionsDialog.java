package org.nschmidt.ldparteditor.dialogs.options;

import static org.nschmidt.ldparteditor.helpers.WidgetUtility.WidgetUtil;

import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.Version;
import org.nschmidt.ldparteditor.helpers.WidgetSelectionListener;
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

        WidgetUtil(btn_OK[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                closingProcedure();
                me.close();
            }
        });

        WidgetUtil( btn_AllowInvalidShapes[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                WorkbenchManager.getUserSettingState().setAllowInvalidShapes(btn_AllowInvalidShapes[0].getSelection());
            }
        });

        WidgetUtil(btn_disableMAD3D[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                WorkbenchManager.getUserSettingState().setDisableMAD3D(btn_disableMAD3D[0].getSelection());
            }
        });

        WidgetUtil(btn_disableMADtext[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                WorkbenchManager.getUserSettingState().setDisableMADtext(btn_disableMADtext[0].getSelection());
            }
        });

        WidgetUtil(btn_browseLdrawPath[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
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
        WidgetUtil(btn_browseAuthoringPath[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
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
        WidgetUtil(btn_browseUnofficialPath[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
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
        txt_ldrawUserName[0].addListener(SWT.Modify, e -> {
            String ldrawUserName = txt_ldrawUserName[0].getText();
            userSettingState.setLdrawUserName(ldrawUserName);
        });
        txt_realName[0].addListener(SWT.Modify, e -> {
            String realName = txt_realName[0].getText();
            userSettingState.setRealUserName(realName);
        });
        cmb_license[0].addListener(SWT.Modify, e -> {
            String license = cmb_license[0].getText();
            userSettingState.setLicense(license);
        });
        cmb_locale[0].addListener(SWT.Modify, e -> {
            if (localeMap.containsKey(cmb_locale[0].getText())) {
                Locale locale = localeMap.get(cmb_locale[0].getText());
                userSettingState.setLocale(locale);
                MyLanguage.LOCALE = locale;
            }
        });
        cmb_textWinArr[0].addListener(SWT.Modify, e -> {
            final int index = cmb_textWinArr[0].getSelectionIndex();
            if (index != -1) {
                userSettingState.setTextWinArr(index);
            }
        });
        spn_coplanarityWarning[0].addValueChangeListener(spn -> {
            final double angle = spn_coplanarityWarning[0].getValue().doubleValue();
            Threshold.coplanarity_angle_warning = angle;
            userSettingState.setCoplanarity_angle_warning(angle);
        });
        spn_coplanarityError[0].addValueChangeListener(spn -> {
            final double angle = spn_coplanarityError[0].getValue().doubleValue();
            Threshold.coplanarity_angle_error = angle;
            userSettingState.setCoplanarity_angle_error(angle);
        });
        this.open();
    }

    @Override
    protected void handleShellCloseEvent() {
        closingProcedure();
        super.handleShellCloseEvent();
    }

    private void closingProcedure() {
        // Override colour 16
        View.overrideColour16();
        // Recompile
        Editor3DWindow.getWindow().compileAll(true);
        // Re-initialise the renderer
        Editor3DWindow.getWindow().initAllRenderers();
    }
}
