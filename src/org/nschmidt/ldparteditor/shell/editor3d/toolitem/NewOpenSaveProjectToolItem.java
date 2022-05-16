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
package org.nschmidt.ldparteditor.shell.editor3d.toolitem;

import static org.nschmidt.ldparteditor.helper.WidgetUtility.widgetUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.nschmidt.ldparteditor.composite.ToolItem;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.LibraryManager;
import org.nschmidt.ldparteditor.enumtype.Task;
import org.nschmidt.ldparteditor.helper.Cocoa;
import org.nschmidt.ldparteditor.helper.WidgetSelectionListener;
import org.nschmidt.ldparteditor.helper.compositetext.ProjectActions;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.resource.ResourceManager;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.state.KeyStateManager;
import org.nschmidt.ldparteditor.widget.NButton;
import org.nschmidt.ldparteditor.widget.TreeItem;

public class NewOpenSaveProjectToolItem extends ToolItem {

    private static final NButton[] btnNewPtr = new NButton[1];
    private static final NButton[] btnOpenPtr = new NButton[1];
    private static final NButton[] btnSavePtr = new NButton[1];
    private static final NButton[] btnSaveAllPtr = new NButton[1];

    private static final List<String> recentItems = new ArrayList<>();

    public NewOpenSaveProjectToolItem(Composite parent, int style, boolean isHorizontal) {
        super(parent, style, isHorizontal);
        createWidgets(this);
        addListeners();
    }

    public static List<String> getRecentItems() {
        return recentItems;
    }

    private static void createWidgets(NewOpenSaveProjectToolItem newOpenSaveProjectToolItem) {
        NButton btnNew = new NButton(newOpenSaveProjectToolItem, Cocoa.getStyle());
        btnNewPtr[0] = btnNew;
        btnNew.setToolTipText(I18n.E3D_NEW);
        btnNew.setImage(ResourceManager.getImage("icon16_document-new.png")); //$NON-NLS-1$

        NButton btnOpen = new NButton(newOpenSaveProjectToolItem, Cocoa.getStyle());
        btnOpenPtr[0] = btnOpen;
        btnOpen.setToolTipText(I18n.E3D_OPEN);
        btnOpen.setImage(ResourceManager.getImage("icon16_document-open.png")); //$NON-NLS-1$

        NButton btnSave = new NButton(newOpenSaveProjectToolItem, Cocoa.getStyle());
        btnSavePtr[0] = btnSave;
        KeyStateManager.addTooltipText(btnSave, I18n.E3D_SAVE, Task.SAVE);
        btnSave.setImage(ResourceManager.getImage("icon16_document-save.png")); //$NON-NLS-1$

        NButton btnSaveAll = new NButton(newOpenSaveProjectToolItem, Cocoa.getStyle());
        btnSaveAllPtr[0] = btnSaveAll;
        btnSaveAll.setToolTipText(I18n.E3D_SAVE_ALL);
        btnSaveAll.setImage(ResourceManager.getImage("icon16_document-saveall.png")); //$NON-NLS-1$
    }

    private static void addListeners() {
        final Editor3DWindow win = Editor3DWindow.getWindow();
        widgetUtil(btnNewPtr[0]).addSelectionListener(e -> {
            if (ProjectActions.createNewProject(Editor3DWindow.getWindow(), false)) {
                addRecentFile(Project.getProjectPath());
            }
            win.regainFocus();
        });
        widgetUtil(btnOpenPtr[0]).addSelectionListener(e -> {
            if (ProjectActions.openProject(null)) {
                addRecentFile(Project.getProjectPath());
                Project.setLastVisitedPath(Project.getProjectPath());
                Project.create(false);
                win.getProject().setData(Project.getProjectPath());
                win.resetSearch();
                LibraryManager.readProjectPartsParent(win.getProjectParts());
                LibraryManager.readProjectParts(win.getProjectParts());
                LibraryManager.readProjectSubparts(win.getProjectSubparts());
                LibraryManager.readProjectPrimitives(win.getProjectPrimitives());
                LibraryManager.readProjectHiResPrimitives(win.getProjectPrimitives48());
                LibraryManager.readProjectLowResPrimitives(win.getProjectPrimitives8());
                win.getOfficialParts().setData(null);
                win.getSearchText().setText(" "); //$NON-NLS-1$
                win.getSearchText().setText(""); //$NON-NLS-1$
                win.updateTreeUnsavedEntries();
            }
            win.regainFocus();
        });
        widgetUtil(btnSavePtr[0]).addSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (win.getPartsTree().getSelectionCount() == 1) {
                    if (win.getPartsTree().getSelection()[0].getData() instanceof DatFile df) {
                        if (!df.isReadOnly() && Project.getUnsavedFiles().contains(df)) {
                            if (df.save()) {
                                addRecentFile(df);
                                win.updateTreeUnsavedEntries();
                            } else {
                                MessageBox messageBoxError = new MessageBox(win.getShell(), SWT.ICON_ERROR | SWT.OK);
                                messageBoxError.setText(I18n.DIALOG_ERROR);
                                messageBoxError.setMessage(I18n.DIALOG_CANT_SAVE_FILE);
                                messageBoxError.open();
                                win.updateTreeUnsavedEntries();
                            }
                        }
                    } else if (win.getPartsTree().getSelection()[0].getData() instanceof ArrayList<?>) {
                        NLogger.debug(getClass(), "Saving all files from this group"); //$NON-NLS-1$
                        @SuppressWarnings("unchecked")
                        List<DatFile> dfs = (List<DatFile>) win.getPartsTree().getSelection()[0].getData();
                        for (DatFile df : dfs) {
                            if (!df.isReadOnly() && Project.getUnsavedFiles().contains(df)) {
                                if (df.save()) {
                                    addRecentFile(df);
                                    Project.removeUnsavedFile(df);
                                    win.updateTreeUnsavedEntries();
                                } else {
                                    MessageBox messageBoxError = new MessageBox(win.getShell(), SWT.ICON_ERROR | SWT.OK);
                                    messageBoxError.setText(I18n.DIALOG_ERROR);
                                    messageBoxError.setMessage(I18n.DIALOG_CANT_SAVE_FILE);
                                    messageBoxError.open();
                                    win.updateTreeUnsavedEntries();
                                }
                            }
                        }
                    } else if (win.getPartsTree().getSelection()[0].getData() instanceof String) {
                        if (win.getPartsTree().getSelection()[0].equals(win.getProject())) {
                            NLogger.debug(getClass(), "Save the project..."); //$NON-NLS-1$
                            if (Project.isDefaultProject() && ProjectActions.createNewProject(Editor3DWindow.getWindow(), true)) {
                                Project.setLastVisitedPath(Project.getProjectPath());
                            }

                            iterateOverItems(win.getProjectParts());
                            iterateOverItems(win.getProjectSubparts());
                            iterateOverItems(win.getProjectPrimitives());
                            iterateOverItems(win.getProjectPrimitives48());
                            iterateOverItems(win.getProjectPrimitives8());
                        } else if (win.getPartsTree().getSelection()[0].equals(win.getUnofficial())) {
                            iterateOverItems(win.getUnofficialParts());
                            iterateOverItems(win.getUnofficialSubparts());
                            iterateOverItems(win.getUnofficialPrimitives());
                            iterateOverItems(win.getUnofficialPrimitives48());
                            iterateOverItems(win.getUnofficialPrimitives8());
                        }
                        NLogger.debug(getClass(), "Saving all files from this group to {0}", win.getPartsTree().getSelection()[0].getData()); //$NON-NLS-1$
                    }
                } else {
                    NLogger.debug(getClass(), "Save the project..."); //$NON-NLS-1$
                    if (Project.isDefaultProject() && ProjectActions.createNewProject(Editor3DWindow.getWindow(), true)) {
                        Project.setLastVisitedPath(Project.getProjectPath());
                    }
                }
                win.regainFocus();
            }

            private void iterateOverItems(TreeItem ti) {
                @SuppressWarnings("unchecked")
                List<DatFile> dfs = (List<DatFile>) ti.getData();
                for (DatFile df : dfs) {
                    if (!df.isReadOnly() && Project.getUnsavedFiles().contains(df)) {
                        if (df.save()) {
                            addRecentFile(df);
                            Project.removeUnsavedFile(df);
                            win.updateTreeUnsavedEntries();
                        } else {
                            MessageBox messageBoxError = new MessageBox(win.getShell(), SWT.ICON_ERROR | SWT.OK);
                            messageBoxError.setText(I18n.DIALOG_ERROR);
                            messageBoxError.setMessage(I18n.DIALOG_CANT_SAVE_FILE);
                            messageBoxError.open();
                            win.updateTreeUnsavedEntries();
                        }
                    }
                }
            }
        });
        widgetUtil(btnSaveAllPtr[0]).addSelectionListener(e -> {
            Set<DatFile> dfs = new HashSet<>(Project.getUnsavedFiles());
            for (DatFile df : dfs) {
                if (!df.isReadOnly()) {
                    if (df.save()) {
                        addRecentFile(df);
                        Project.removeUnsavedFile(df);
                    } else {
                        MessageBox messageBoxError = new MessageBox(win.getShell(), SWT.ICON_ERROR | SWT.OK);
                        messageBoxError.setText(I18n.DIALOG_ERROR);
                        messageBoxError.setMessage(I18n.DIALOG_CANT_SAVE_FILE);
                        messageBoxError.open();
                    }
                }
            }
            if (Project.isDefaultProject() && ProjectActions.createNewProject(win, true)) {
                addRecentFile(Project.getProjectPath());
            }
            win.updateTreeUnsavedEntries();
            win.regainFocus();
        });
    }

    private static void addRecentFile(String projectPath) {
        // PrimGen2 uses a temporary "..." projectPath
        if (!"...".equals(projectPath)) { //$NON-NLS-1$
            boolean removedAnItem = true;
            while (removedAnItem) {
                removedAnItem = true;
                final int index = recentItems.indexOf(projectPath);
                if (index > -1) {
                    recentItems.remove(index);
                } else if (recentItems.size() > 20) {
                    recentItems.remove(0);
                } else {
                    removedAnItem = false;
                }
            }
            
            recentItems.add(projectPath);
        }
    }

    public static void addRecentFile(DatFile dat) {
        addRecentFile(new File(dat.getNewName()).getAbsolutePath());
    }
}
