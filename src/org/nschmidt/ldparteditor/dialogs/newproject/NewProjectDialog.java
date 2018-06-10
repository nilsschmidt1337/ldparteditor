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

import static org.nschmidt.ldparteditor.helpers.WidgetUtility.WidgetUtil;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.nschmidt.ldparteditor.helpers.FileHelper;
import org.nschmidt.ldparteditor.helpers.WidgetSelectionListener;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * @author nils
 *
 */
public class NewProjectDialog extends NewProjectDesign {

    private String projectName = ""; //$NON-NLS-1$
    private String projectPath = WorkbenchManager.getUserSettingState().getAuthoringFolderPath();

    /**
     * Create the dialog.
     *
     * @param saveAs
     *            {@code true} if the dialog should be displayed as
     *            "Save As..."-dialog.
     */
    public NewProjectDialog(boolean saveAs) {
        super(Editor3DWindow.getWindow().getShell(), saveAs);
    }

    @Override
    public int open() {
        super.create();
        // MARK All final listeners will be configured here..
        txt_projectName[0].addListener(SWT.Modify, e -> {
            String txt = txt_projectName[0].getText().toLowerCase();
            btn_ok[0].setEnabled(FileHelper.isFilenameValid(txt) && !txt.equals("48") && !txt.equalsIgnoreCase("parts") && !txt.equalsIgnoreCase("s") && !txt.equalsIgnoreCase("p")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            projectName = txt_projectName[0].getText();
        });
        final boolean[] firstchoose = new boolean[1];
        firstchoose[0] = true;
        WidgetUtil(btn_browseProjectPath[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dlg = new DirectoryDialog(getShell());
                String authorFolder = WorkbenchManager.getUserSettingState().getAuthoringFolderPath();

                // Set the initial filter path according
                // to the authoring folder if the project path is Project.PROJECT,
                // else choose the parent folder of the last opened project.
                if (firstchoose[0] && Project.getProjectPath().equals(new File(Project.DEFAULT_PROJECT_PATH).getAbsolutePath())) {
                    dlg.setFilterPath(authorFolder);
                    firstchoose[0] = false;
                } else {
                    dlg.setFilterPath(projectPath);
                }

                // Change the title bar text
                dlg.setText(I18n.PROJECT_DefineProjectLocation);

                // Customizable message displayed in the dialog
                dlg.setMessage(I18n.DIALOG_DirectorySelect);

                // Calling open() will open and run the dialog.
                // It will return the selected directory, or
                // null if user cancels
                String dir = dlg.open();
                if (dir != null) {
                    String lcdir = dir.toLowerCase();
                    if (lcdir.startsWith(authorFolder.toLowerCase()) && !lcdir.endsWith(File.separator + "parts") //$NON-NLS-1$
                            && !lcdir.endsWith(File.separator + "s") //$NON-NLS-1$
                            && !lcdir.endsWith(File.separator + "p") //$NON-NLS-1$
                            && !dir.endsWith(File.separator + "48")) { //$NON-NLS-1$
                        // Set the text box to the new selection
                        txt_projectPath[0].setText(dir.substring(authorFolder.length()));
                        projectPath = authorFolder + txt_projectPath[0].getText();
                    } // do nothing otherwise
                } else {
                    txt_projectPath[0].setText(""); //$NON-NLS-1$
                    projectPath = authorFolder;
                }
            }
        });
        WidgetUtil(btn_ok[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Project.setTempProjectName(projectName);
                Project.setTempProjectPath(projectPath + File.separator + projectName);
            }
        });
        return super.open();
    }
}
