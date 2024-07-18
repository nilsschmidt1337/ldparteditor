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
package org.nschmidt.ldparteditor.dialog.options;

import static org.nschmidt.ldparteditor.helper.WidgetUtility.widgetUtil;

import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.composite.compositetab.CompositeTab;
import org.nschmidt.ldparteditor.enumtype.Colour;
import org.nschmidt.ldparteditor.enumtype.LDConfig;
import org.nschmidt.ldparteditor.enumtype.MyLanguage;
import org.nschmidt.ldparteditor.enumtype.Threshold;
import org.nschmidt.ldparteditor.helper.Version;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.resource.ResourceManager;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.shell.editortext.EditorTextWindow;
import org.nschmidt.ldparteditor.workbench.Theming;
import org.nschmidt.ldparteditor.workbench.UserSettingState;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

public class OptionsDialog extends OptionsDesign {

    public OptionsDialog(Shell parentShell) {
        super(parentShell);
    }

    @SuppressWarnings("java:S2696")
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
        widgetUtil(btnIncludeUnmatchedEdgesPtr[0]).addSelectionListener(e -> WorkbenchManager.getUserSettingState().setIncludeUnmatchedEdgesByDefault(btnIncludeUnmatchedEdgesPtr[0].getSelection()));
        widgetUtil(btnCancelViaMousePtr[0]).addSelectionListener(e -> WorkbenchManager.getUserSettingState().setCancelAddViaMouse(btnCancelViaMousePtr[0].getSelection()));
        widgetUtil(btnInvertInvertWheelZoomDirectionPtr[0]).addSelectionListener(e -> WorkbenchManager.getUserSettingState().setInvertingWheelZoomDirection(btnInvertInvertWheelZoomDirectionPtr[0].getSelection()));
        widgetUtil(btnShowAxisLabelsPtr[0]).addSelectionListener(e -> {
            WorkbenchManager.getUserSettingState().setShowingAxisLabels(btnShowAxisLabelsPtr[0].getSelection());

            if (WorkbenchManager.getUserSettingState().isShowingAxisLabels()) {
                Colour.textColourR = Colour.textColourAltR;
                Colour.textColourG = Colour.textColourAltG;
                Colour.textColourB = Colour.textColourAltB;
            } else {
                Colour.textColourR = Colour.textColourDefaultR;
                Colour.textColourG = Colour.textColourDefaultG;
                Colour.textColourB = Colour.textColourDefaultB;
            }
        });

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
        spnDataFileSizeLimitPtr[0].addValueChangeListener(spn -> {
            final int sizeLimitInKiloBytes = spnDataFileSizeLimitPtr[0].getValue();
            userSettingState.setDataFileSizeLimit(sizeLimitInKiloBytes);
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
        cmbThemePtr[0].addListener(SWT.Modify, e -> {
            // This needs to be done twice to have an effect on the UI.
            updateTheme();
            updateTheme();
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

    private void updateTheme() {
        final String themeName = cmbThemePtr[0].getText();
        for (Theming theme : Theming.values()) {
            if (theme.name().equals(themeName)) {

                Theming.setCurrentTheme(theme);
                WorkbenchManager.getUserSettingState().setTheming(theme);
                WorkbenchManager.getThemeSettingState().setShowingAxisLabels(WorkbenchManager.getUserSettingState().isShowingAxisLabels());
                theme.overrideColours();
                WorkbenchManager.getThemeSettingState().loadColourSettings();

                updateColours(trColourTreePtr[0]);
                for (EditorTextWindow w : Project.getOpenTextWindows()) {
                    for (CTabItem t : w.getTabFolder().getItems()) {
                        ((CompositeTab) t).updateColours();
                    }
                }

                trtmEditor3DPtr[0].getItems().clear();
                trtmEditorTextPtr[0].getItems().clear();
                if (theme == Theming.DEFAULT) {
                    buildColourTree(WorkbenchManager.getUserSettingState(), trtmEditor3DPtr[0], trtmEditorTextPtr[0]);
                } else {
                    buildColourTree(WorkbenchManager.getThemeSettingState(), trtmEditor3DPtr[0], trtmEditorTextPtr[0]);
                }

                trColourTreePtr[0].build();
                trColourTreePtr[0].update();
                break;
            }
        }
    }
}
