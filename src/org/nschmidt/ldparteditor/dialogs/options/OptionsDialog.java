package org.nschmidt.ldparteditor.dialogs.options;

import static org.nschmidt.ldparteditor.helpers.WidgetUtility.widgetUtil;

import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.enums.LDConfig;
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.enums.Threshold;
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

        widgetUtil(btnOkPtr[0]).addSelectionListener(e -> {
            closingProcedure();
            me.close();
        });

        widgetUtil(btnAllowInvalidShapesPtr[0]).addSelectionListener(e -> WorkbenchManager.getUserSettingState().setAllowInvalidShapes(btnAllowInvalidShapesPtr[0].getSelection()));
        widgetUtil(btnTranslateViewByCursorPtr[0]).addSelectionListener(e -> WorkbenchManager.getUserSettingState().setTranslatingViewByCursor( btnTranslateViewByCursorPtr[0].getSelection()));
        widgetUtil(btnDisableMAD3DPtr[0]).addSelectionListener(e -> WorkbenchManager.getUserSettingState().setDisableMAD3D(btnDisableMAD3DPtr[0].getSelection()));
        widgetUtil(btnDisableMADtextPtr[0]).addSelectionListener(e -> WorkbenchManager.getUserSettingState().setDisableMADtext(btnDisableMADtextPtr[0].getSelection()));
        widgetUtil(btnInvertInvertWheelZoomDirectionPtr[0]).addSelectionListener(e -> WorkbenchManager.getUserSettingState().setInvertingWheelZoomDirection(btnInvertInvertWheelZoomDirectionPtr[0].getSelection()));

        widgetUtil(btnBrowseLdrawPathPtr[0]).addSelectionListener(e -> {
            DirectoryDialog dlg = new DirectoryDialog(getShell());

            // Set the initial filter to the last selected path
            dlg.setFilterPath(userSettingState.getLdrawFolderPath());

            // Change the title bar text
            dlg.setText(I18n.OPTIONS_LDRAW_FOLDER);

            // Customizable message displayed in the dialog
            dlg.setMessage(I18n.OPTIONS_DIRECTORY);

            // Calling open() will open and run the dialog.
            // It will return the selected directory, or
            // null if user cancels
            String dir = dlg.open();
            if (dir != null) {
                // Set the text box to the new selection
                txtLdrawPathPtr[0].setText(dir);
                String ldrawPath = dir;
                userSettingState.setLdrawFolderPath(ldrawPath);
            }
        });
        widgetUtil(btnBrowseAuthoringPathPtr[0]).addSelectionListener(e -> {
            DirectoryDialog dlg = new DirectoryDialog(getShell());

            // Set the initial filter to the last selected path
            dlg.setFilterPath(userSettingState.getAuthoringFolderPath());

            // Change the title bar text
            dlg.setText(I18n.OPTIONS_AUTHORING_WHERE);

            // Customizable message displayed in the dialog
            dlg.setMessage(I18n.OPTIONS_DIRECTORY);

            // Calling open() will open and run the dialog.
            // It will return the selected directory, or
            // null if user cancels
            String dir = dlg.open();
            if (dir != null) {
                // Set the text box to the new selection
                txtPartAuthoringPathPtr[0].setText(dir);
                String partAuthoringPath = dir;
                userSettingState.setAuthoringFolderPath(partAuthoringPath);
            }
        });
        widgetUtil(btnBrowseUnofficialPathPtr[0]).addSelectionListener(e -> {
            DirectoryDialog dlg = new DirectoryDialog(getShell());

            // Set the initial filter to the last selected path
            dlg.setFilterPath(userSettingState.getUnofficialFolderPath());

            // Change the title bar text
            dlg.setText(I18n.OPTIONS_UNOFFICIAL_WHERE);

            // Customizable message displayed in the dialog
            dlg.setMessage(I18n.OPTIONS_DIRECTORY);

            // Calling open() will open and run the dialog.
            // It will return the selected directory, or
            // null if user cancels
            String dir = dlg.open();
            if (dir != null) {
                // Set the text box to the new selection
                txtUnofficialPathPtr[0].setText(dir);
                String unofficialPath = dir;
                userSettingState.setUnofficialFolderPath(unofficialPath);
            }
        });
        txtLdrawUserNamePtr[0].addListener(SWT.Modify, e -> {
            String ldrawUserName = txtLdrawUserNamePtr[0].getText();
            userSettingState.setLdrawUserName(ldrawUserName);
        });
        txtRealNamePtr[0].addListener(SWT.Modify, e -> {
            String realName = txtRealNamePtr[0].getText();
            userSettingState.setRealUserName(realName);
        });
        cmbLicensePtr[0].addListener(SWT.Modify, e -> {
            String license = cmbLicensePtr[0].getText();
            userSettingState.setLicense(license);
        });
        cmbLocalePtr[0].addListener(SWT.Modify, e -> {
            if (localeMap.containsKey(cmbLocalePtr[0].getText())) {
                Locale locale = localeMap.get(cmbLocalePtr[0].getText());
                userSettingState.setLocale(locale);
                MyLanguage.setLocale(locale);
            }
        });
        cmbTextWinArrPtr[0].addListener(SWT.Modify, e -> {
            final int index = cmbTextWinArrPtr[0].getSelectionIndex();
            if (index != -1) {
                userSettingState.setTextWinArr(index);
            }
        });
        spnCoplanarityWarningPtr[0].addValueChangeListener(spn -> {
            final double angle = spnCoplanarityWarningPtr[0].getValue().doubleValue();
            Threshold.coplanarityAngleWarning = angle;
            userSettingState.setCoplanarityAngleWarning(angle);
        });
        spnCoplanarityErrorPtr[0].addValueChangeListener(spn -> {
            final double angle = spnCoplanarityErrorPtr[0].getValue().doubleValue();
            Threshold.coplanarityAngleError = angle;
            userSettingState.setCoplanarityAngleError(angle);
        });
        spnViewportScalePtr[0].addValueChangeListener(spn -> {
            final double scaleFactor = spnViewportScalePtr[0].getValue().doubleValue();
            userSettingState.setViewportScaleFactor(scaleFactor);
        });
        cmbMouseButtonLayoutPtr[0].addListener(SWT.Modify, e -> {
            final int index = cmbMouseButtonLayoutPtr[0].getSelectionIndex();
            if (index != -1) {
                userSettingState.setMouseButtonLayout(index);
            }
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
        LDConfig.overrideColour16();
        // Recompile
        Editor3DWindow.getWindow().compileAll(true);
        // Re-initialise the renderer
        Editor3DWindow.getWindow().initAllRenderers();
    }
}
