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
package org.nschmidt.ldparteditor.dialogs.newproject;

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.widgets.NButton;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * @author nils
 *
 */
class NewProjectDesign extends Dialog {

    Text[] txtProjectPathPtr = new Text[1];
    Text[] txtProjectNamePtr = new Text[1];
    NButton[] btnBrowseProjectPathPtr = new NButton[1];
    Button[] btnOkPtr = new Button[1];
    private boolean saveAs = false;

    NewProjectDesign(Shell parentShell, boolean saveAs) {
        super(parentShell);
        this.saveAs = saveAs;
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

        Label lblNewProject = new Label(cmpContainer, SWT.NONE);
        if (saveAs) {
            lblNewProject.setText(I18n.PROJECT_SAVE_PROJECT);
        } else {
            lblNewProject.setText(I18n.PROJECT_CREATE_NEW_PROJECT);
        }

        Label lblSeparator = new Label(cmpContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
        lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label lblProjectLocation = new Label(cmpContainer, SWT.NONE);
        lblProjectLocation.setText(I18n.PROJECT_PROJECT_LOCATION);

        Composite cmpPathChooser1 = new Composite(cmpContainer, SWT.NONE);
        cmpPathChooser1.setLayout(new RowLayout(SWT.HORIZONTAL));

        Text txtLdrawPath = new Text(cmpPathChooser1, SWT.BORDER);
        this.txtProjectPathPtr[0] = txtLdrawPath;
        txtLdrawPath.setEditable(false);
        txtLdrawPath.setLayoutData(new RowData(294, SWT.DEFAULT));
        if (!saveAs || Project.getProjectPath().equals(new File(Project.DEFAULT_PROJECT_PATH).getAbsolutePath())) {
            txtLdrawPath.setText(""); //$NON-NLS-1$
        } else {
            String authorFolder = WorkbenchManager.getUserSettingState().getAuthoringFolderPath();
            txtLdrawPath.setText(new File(Project.getProjectPath()).getParent().substring(authorFolder.length()));
        }

        NButton btnBrowseLdrawPath = new NButton(cmpPathChooser1, SWT.NONE);
        this.btnBrowseProjectPathPtr[0] = btnBrowseLdrawPath;
        btnBrowseLdrawPath.setText(I18n.DIALOG_BROWSE);

        Label lblProjectName = new Label(cmpContainer, SWT.NONE);
        lblProjectName.setText(I18n.PROJECT_PROJECT_NAME);

        Composite cmpProjectName = new Composite(cmpContainer, SWT.NONE);
        cmpProjectName.setLayout(new RowLayout(SWT.HORIZONTAL));

        Text txtProjectName = new Text(cmpProjectName, SWT.BORDER);
        this.txtProjectNamePtr[0] = txtProjectName;
        txtProjectName.setLayoutData(new RowData(294, SWT.DEFAULT));

        if (!txtLdrawPath.getText().isEmpty() && saveAs) {
            txtProjectName.setText(Project.getProjectName());
        }

        return cmpContainer;
    }

    /**
     * Create contents of the button bar.
     *
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        btnOkPtr[0] = createButton(parent, IDialogConstants.OK_ID, I18n.DIALOG_OK, true);
        btnOkPtr[0].setEnabled(false);
        createButton(parent, IDialogConstants.CANCEL_ID, I18n.DIALOG_CANCEL, false);
    }
}
