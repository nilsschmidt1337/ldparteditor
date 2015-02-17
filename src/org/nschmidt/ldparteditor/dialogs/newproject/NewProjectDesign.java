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
import org.eclipse.swt.graphics.Point;
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
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * @author nils
 *
 */
class NewProjectDesign extends Dialog {

    Text[] txt_projectPath = new Text[1];
    Text[] txt_projectName = new Text[1];
    Button[] btn_browseProjectPath = new Button[1];
    Button[] btn_ok = new Button[1];
    boolean saveAs = false;

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
        Composite cmp_Container = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = (GridLayout) cmp_Container.getLayout();
        gridLayout.verticalSpacing = 10;
        gridLayout.horizontalSpacing = 10;

        Label lbl_newProject = new Label(cmp_Container, SWT.NONE);
        if (saveAs) {
            lbl_newProject.setText(I18n.PROJECT_SaveProject);
        } else {
            lbl_newProject.setText(I18n.PROJECT_CreateNewProject);
        }

        Label lbl_separator = new Label(cmp_Container, SWT.SEPARATOR | SWT.HORIZONTAL);
        lbl_separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label lbl_projectLocation = new Label(cmp_Container, SWT.NONE);
        lbl_projectLocation.setText(I18n.PROJECT_ProjectLocation);

        Composite cmp_pathChooser1 = new Composite(cmp_Container, SWT.NONE);
        cmp_pathChooser1.setLayout(new RowLayout(SWT.HORIZONTAL));

        Text txt_ldrawPath = new Text(cmp_pathChooser1, SWT.BORDER);
        this.txt_projectPath[0] = txt_ldrawPath;
        txt_ldrawPath.setEditable(false);
        txt_ldrawPath.setLayoutData(new RowData(294, SWT.DEFAULT));
        if (!saveAs || Project.getProjectPath().equals(new File("project").getAbsolutePath())) { //$NON-NLS-1$
            txt_ldrawPath.setText(""); //$NON-NLS-1$
        } else {
            String authorFolder = WorkbenchManager.getUserSettingState().getAuthoringFolderPath();
            txt_ldrawPath.setText(new File(Project.getProjectPath()).getParent().substring(authorFolder.length()));
        }

        Button btn_BrowseLdrawPath = new Button(cmp_pathChooser1, SWT.NONE);
        this.btn_browseProjectPath[0] = btn_BrowseLdrawPath;
        btn_BrowseLdrawPath.setText(I18n.DIALOG_Browse);

        Label lbl_projectName = new Label(cmp_Container, SWT.NONE);
        lbl_projectName.setText(I18n.PROJECT_ProjectName);

        Composite cmp_projectName = new Composite(cmp_Container, SWT.NONE);
        cmp_projectName.setLayout(new RowLayout(SWT.HORIZONTAL));

        Text txt_projectName = new Text(cmp_projectName, SWT.BORDER);
        this.txt_projectName[0] = txt_projectName;
        txt_projectName.setLayoutData(new RowData(294, SWT.DEFAULT));

        if (!txt_ldrawPath.getText().isEmpty() && saveAs) {
            txt_projectName.setText(Project.getProjectName());
        }

        return cmp_Container;
    }

    /**
     * Create contents of the button bar.
     *
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        btn_ok[0] = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        btn_ok[0].setEnabled(false);
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
