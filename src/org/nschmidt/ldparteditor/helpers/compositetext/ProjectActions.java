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
package org.nschmidt.ldparteditor.helpers.compositetext;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.dialogs.newproject.NewProjectDialog;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.shells.editortext.EditorTextWindow;
import org.nschmidt.ldparteditor.widgets.TreeItem;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * Helper class for all project regarding actions (Create/Open/Save).
 *
 * @author nils
 *
 */
public enum ProjectActions {
    INSTANCE;

    /**
     * Creates a new project
     * @param createOnlyDefault
     */
    public static void createNewProject(Editor3DWindow win, boolean createOnlyDefault) {
        if (askForUnsavedChanges(win, createOnlyDefault, true) && !createOnlyDefault && new NewProjectDialog(false).open() == IDialogConstants.OK_ID) {
            while (new File(Project.getTempProjectPath()).isDirectory()) {
                MessageBox messageBoxError = new MessageBox(win.getShell(), SWT.ICON_ERROR | SWT.YES | SWT.CANCEL | SWT.NO);
                messageBoxError.setText(I18n.PROJECT_ProjectOverwriteTitle);
                messageBoxError.setMessage(I18n.PROJECT_ProjectOverwrite);
                int result2 = messageBoxError.open();
                if (result2 == SWT.YES) {
                    break;
                } else if (result2 == SWT.NO){
                    if (new NewProjectDialog(false).open() != IDialogConstants.OK_ID) return;
                } else {
                    return;
                }
            }
            String newProjectPath = Project.getTempProjectPath() + File.separator;
            if (!newProjectPath.startsWith(WorkbenchManager.getUserSettingState().getAuthoringFolderPath() + File.separator) && newProjectPath.startsWith(WorkbenchManager.getUserSettingState().getLdrawFolderPath() + File.separator) || newProjectPath.startsWith(WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator)) {
                MessageBox messageBox = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_ERROR | SWT.OK);
                messageBox.setText(I18n.DIALOG_AlreadyAllocatedNameTitle);
                messageBox.setMessage(I18n.DIALOG_AlreadyAllocatedName);
                messageBox.open();
                return;
            }
            Project.setProjectName(Project.getTempProjectName());
            Project.setProjectPath(Project.getTempProjectPath());
            Project.create(true);
        }
    }

    private static boolean askForUnsavedChanges(Editor3DWindow win, boolean createOnlyDefault, boolean ignoreNonProjectFiles) {
        boolean unsavedProjectFiles = false;
        if (!createOnlyDefault) {
            Set<DatFile> unsavedFiles = new HashSet<DatFile>(Project.getUnsavedFiles());
            for (DatFile df : unsavedFiles) {
                if (ignoreNonProjectFiles && !df.isProjectFile()) continue;
                if (!df.getText().trim().equals("")) { //$NON-NLS-1$
                    MessageBox messageBox = new MessageBox(win.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.CANCEL | SWT.NO);
                    messageBox.setText(I18n.DIALOG_UnsavedChangesTitle);

                    Object[] messageArguments = {df.getShortName()};
                    MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                    formatter.setLocale(View.LOCALE);
                    formatter.applyPattern(I18n.DIALOG_UnsavedChanges);
                    messageBox.setMessage(formatter.format(messageArguments));

                    int result = messageBox.open();

                    if (result == SWT.NO) {
                        // Remove file from tree
                        Editor3DWindow.getWindow().updateTree_removeEntry(df);
                    } else if (result == SWT.YES) {
                        if (df.save()) {
                            Editor3DWindow.getWindow().addRecentFile(df);
                            Editor3DWindow.getWindow().updateTree_unsavedEntries();
                        } else {
                            MessageBox messageBoxError = new MessageBox(win.getShell(), SWT.ICON_ERROR | SWT.OK);
                            messageBoxError.setText(I18n.DIALOG_Error);
                            messageBoxError.setMessage(I18n.DIALOG_CantSaveFile);
                            messageBoxError.open();
                            Editor3DWindow.getWindow().updateTree_unsavedEntries();
                            return false;
                        }
                    } else return false;
                }
            }
            if (!Project.getOpenTextWindows().isEmpty() || !Editor3DWindow.renders.isEmpty()) {
                MessageBox messageBoxOpenFiles = new MessageBox(win.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.CANCEL | SWT.NO);
                messageBoxOpenFiles.setText(I18n.DIALOG_KeepFilesOpenTitle);
                messageBoxOpenFiles.setMessage(I18n.DIALOG_KeepFilesOpen);

                int result = messageBoxOpenFiles.open();

                if (result == SWT.NO) {
                    Set<EditorTextWindow> ow = new HashSet<EditorTextWindow>(Project.getOpenTextWindows());
                    for (EditorTextWindow w : ow) {
                        w.getShell().close();
                    }
                    Editor3DWindow.getWindow().closeAllComposite3D();
                } else if (result == SWT.YES) {
                } else return false;
            }
            {
                ArrayList<TreeItem> ta = win.getProjectParts().getItems();
                for (TreeItem ti : ta) {
                    unsavedProjectFiles = unsavedProjectFiles || !((DatFile) ti.getData()).getText().trim().equals(""); //$NON-NLS-1$
                }
            }
            {
                ArrayList<TreeItem> ta = win.getProjectSubparts().getItems();
                for (TreeItem ti : ta) {
                    unsavedProjectFiles = unsavedProjectFiles || !((DatFile) ti.getData()).getText().trim().equals(""); //$NON-NLS-1$
                }
            }
            {
                ArrayList<TreeItem> ta = win.getProjectPrimitives().getItems();
                for (TreeItem ti : ta) {
                    unsavedProjectFiles = unsavedProjectFiles || !((DatFile) ti.getData()).getText().trim().equals(""); //$NON-NLS-1$
                }
            }
            {
                ArrayList<TreeItem> ta = win.getProjectPrimitives48().getItems();
                for (TreeItem ti : ta) {
                    unsavedProjectFiles = unsavedProjectFiles || !((DatFile) ti.getData()).getText().trim().equals(""); //$NON-NLS-1$
                }
            }
            {
                ArrayList<TreeItem> ta = win.getProjectPrimitives8().getItems();
                for (TreeItem ti : ta) {
                    unsavedProjectFiles = unsavedProjectFiles || !((DatFile) ti.getData()).getText().trim().equals(""); //$NON-NLS-1$
                }
            }
        }

        if (createOnlyDefault || unsavedProjectFiles && Project.isDefaultProject()) {
            // Save old project here, if the project contains at least one non-empty file
            boolean cancelIt = false;
            boolean secondRun = false;
            while (true) {
                int result = IDialogConstants.CANCEL_ID;
                if (secondRun) {
                    result = new NewProjectDialog(true).open();
                    if (result == IDialogConstants.CANCEL_ID) {
                        return false;
                    }
                    while (new File(Project.getTempProjectPath()).isDirectory()) {
                        MessageBox messageBoxError = new MessageBox(win.getShell(), SWT.ICON_ERROR | SWT.YES | SWT.CANCEL | SWT.NO);
                        messageBoxError.setText(I18n.PROJECT_ProjectOverwriteTitle);
                        messageBoxError.setMessage(I18n.PROJECT_ProjectOverwrite);
                        int result2 = messageBoxError.open();
                        if (result2 == SWT.NO) {
                            result = new NewProjectDialog(true).open();
                        } else if (result2 == SWT.YES) {
                            break;
                        } else {
                            cancelIt = true;
                            break;
                        }
                    }
                }
                if (result == IDialogConstants.OK_ID) {
                    NLogger.debug(ProjectActions.class, "Saving new project..."); //$NON-NLS-1$
                    String tmp1 = Project.getProjectName();
                    String tmp2 = Project.getProjectPath();
                    Project.setProjectName(Project.getTempProjectName());
                    Project.setProjectPath(Project.getTempProjectPath());
                    if (!Project.save()) {
                        MessageBox messageBoxError = new MessageBox(win.getShell(), SWT.ICON_ERROR | SWT.OK);
                        messageBoxError.setText(I18n.DIALOG_Error);
                        messageBoxError.setMessage(I18n.DIALOG_CantSaveProject);
                        messageBoxError.open();
                        Project.setProjectName(tmp1);
                        Project.setProjectPath(tmp2);
                        cancelIt = true;
                    } else {
                        Project.updateEditor();
                        Editor3DWindow.getWindow().getShell().update();
                        Editor3DWindow.getWindow().getProjectParts().getParent().build();
                        Editor3DWindow.getWindow().getProjectParts().getParent().redraw();
                        Editor3DWindow.getWindow().getProjectParts().getParent().update();
                    }
                    break;
                } else {
                    if (!cancelIt) {
                        secondRun = true;
                        MessageBox messageBox = new MessageBox(win.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.CANCEL | SWT.NO);
                        messageBox.setText(I18n.DIALOG_UnsavedChangesTitle);

                        Object[] messageArguments = {I18n.DIALOG_TheOldProject};
                        MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                        formatter.setLocale(View.LOCALE);
                        formatter.applyPattern(I18n.DIALOG_UnsavedChanges);
                        messageBox.setMessage(formatter.format(messageArguments));

                        if (!(createOnlyDefault && Project.isDefaultProject())) {
                            int result2 = messageBox.open();
                            if (result2 == SWT.YES) {
                            } else if (result2 == SWT.NO) {
                                break;
                            } else {
                                cancelIt = true;
                                break;
                            }
                        }
                    }
                }
            }
            if (cancelIt) return false;
        }
        return true;
    }

    /**
     * Opens a existing project
     * @param path
     */
    public static boolean openProject(String path) {

        if (askForUnsavedChanges(Editor3DWindow.getWindow(), false, true)) {

            DirectoryDialog dlg = new DirectoryDialog(Editor3DWindow.getWindow().getShell());
            String authorFolder = WorkbenchManager.getUserSettingState().getAuthoringFolderPath();

            // Set the initial filter path according
            // to the authoring folder if the project path is "project", else choose
            // the parent folder of the last opened project.
            if (Project.getProjectPath().equals("project")) { //$NON-NLS-1$
                dlg.setFilterPath(authorFolder);
            } else {
                dlg.setFilterPath(new File(Project.getProjectPath()).getParent());
            }

            // Change the title bar text
            dlg.setText(I18n.PROJECT_SelectProjectLocation);

            // Customizable message displayed in the dialog
            dlg.setMessage(I18n.DIALOG_DirectorySelect);

            // Calling open() will open and run the dialog.
            // It will return the selected directory, or
            // null if user cancels
            String dir = path == null ? dlg.open() : path;
            if (dir != null && dir.contains(authorFolder)) {
                Project.setProjectPath(dir);
                Project.setProjectName(new File(dir).getName());
                return true;
            }
        }
        return false;
    }
}
