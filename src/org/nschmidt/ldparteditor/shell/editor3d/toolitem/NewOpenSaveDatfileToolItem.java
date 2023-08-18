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
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.composite.ToolItem;
import org.nschmidt.ldparteditor.composite.compositetab.CompositeTab;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.LibraryManager;
import org.nschmidt.ldparteditor.enumtype.MyLanguage;
import org.nschmidt.ldparteditor.enumtype.OpenInWhat;
import org.nschmidt.ldparteditor.enumtype.Task;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.helper.Cocoa;
import org.nschmidt.ldparteditor.helper.compositetext.ProjectActions;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.resource.ResourceManager;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.shell.editortext.EditorTextWindow;
import org.nschmidt.ldparteditor.state.KeyStateManager;
import org.nschmidt.ldparteditor.text.Stl2Dat;
import org.nschmidt.ldparteditor.text.UTF8PrintWriter;
import org.nschmidt.ldparteditor.widget.NButton;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

public class NewOpenSaveDatfileToolItem extends ToolItem {

    private final NButton[] btnNewDatPtr = new NButton[1];
    private final NButton[] btnOpenDatPtr = new NButton[1];
    private final NButton[] btnLastOpenPtr = new NButton[1];
    private final NButton[] btnSaveDatPtr = new NButton[1];
    private final NButton[] btnSaveAsDatPtr = new NButton[1];

    public NewOpenSaveDatfileToolItem(Composite parent, int style, boolean isHorizontal) {
        super(parent, style, isHorizontal);
        createWidgets(this);
        addListeners();
    }

    private void createWidgets(NewOpenSaveDatfileToolItem newOpenSaveDatfileToolItem) {
        NButton btnNewDat = new NButton(newOpenSaveDatfileToolItem, Cocoa.getStyle());
        this.btnNewDatPtr[0] = btnNewDat;
        btnNewDat.setToolTipText(I18n.E3D_NEW_DAT);
        btnNewDat.setImage(ResourceManager.getImage("icon16_document-newdat.png")); //$NON-NLS-1$

        NButton btnOpenDAT = new NButton(newOpenSaveDatfileToolItem, Cocoa.getStyle());
        this.btnOpenDatPtr[0] = btnOpenDAT;
        btnOpenDAT.setToolTipText(I18n.E3D_OPEN_DAT);
        btnOpenDAT.setImage(ResourceManager.getImage("icon16_document-opendat.png")); //$NON-NLS-1$

        NButton btnSnapshot = new NButton(newOpenSaveDatfileToolItem, Cocoa.getStyle());
        this.btnLastOpenPtr[0] = btnSnapshot;
        btnSnapshot.setToolTipText(I18n.E3D_LAST_OPENED);
        btnSnapshot.setImage(ResourceManager.getImage("icon16_snapshot.png")); //$NON-NLS-1$

        NButton btnSaveDAT = new NButton(newOpenSaveDatfileToolItem, Cocoa.getStyle());
        this.btnSaveDatPtr[0] = btnSaveDAT;
        KeyStateManager.addTooltipText(btnSaveDAT, I18n.E3D_SAVE, Task.SAVE);
        btnSaveDAT.setImage(ResourceManager.getImage("icon16_document-savedat.png")); //$NON-NLS-1$

        NButton btnSaveAsDAT = new NButton(newOpenSaveDatfileToolItem, Cocoa.getStyle());
        this.btnSaveAsDatPtr[0] = btnSaveAsDAT;
        btnSaveAsDAT.setToolTipText(I18n.E3D_SAVE_AS);
        btnSaveAsDAT.setImage(ResourceManager.getImage("icon16_document-savedat.png")); //$NON-NLS-1$
        btnSaveAsDAT.setText("..."); //$NON-NLS-1$
    }

    private void addListeners() {
        final Editor3DWindow win = Editor3DWindow.getWindow();
        widgetUtil(btnLastOpenPtr[0]).addSelectionListener(e -> {

            Menu lastOpenedMenu = new Menu(win.getPartsTree().getTree());
            btnLastOpenPtr[0].setMenu(lastOpenedMenu);

            List<String> recentItems = NewOpenSaveProjectToolItem.getRecentItems();
            final int size = recentItems.size() - 1;
            for (int i = size; i > -1; i--) {
                final String path = recentItems.get(i);
                File f = new File(path);
                if (f.exists() && f.canRead()) {
                    if (f.isFile()) {
                        MenuItem mntmItem1 = new MenuItem(lastOpenedMenu, I18n.noBiDirectionalTextStyle());
                        mntmItem1.setEnabled(true);
                        mntmItem1.setText(path);
                        widgetUtil(mntmItem1).addSelectionListener(e11 -> {
                            File f11 = new File(path);
                            if (f11.exists() && f11.isFile() && f11.canRead()) {
                                DatFile df = win.openDatFile(OpenInWhat.EDITOR_3D, path, false);
                                if (df != null && !df.equals(View.DUMMY_DATFILE) && WorkbenchManager.getUserSettingState().isSyncingTabs()) {
                                    boolean fileIsOpenInTextEditor = false;
                                    for (EditorTextWindow w : Project.getOpenTextWindows()) {
                                        for (CTabItem t : w.getTabFolder().getItems()) {
                                            if (df.equals(((CompositeTab) t).getState().getFileNameObj())) {
                                                fileIsOpenInTextEditor = true;
                                            }
                                            if (fileIsOpenInTextEditor) break;
                                        }
                                        if (fileIsOpenInTextEditor) break;
                                    }
                                    if (Project.getOpenTextWindows().isEmpty() || fileIsOpenInTextEditor) {
                                        win.openDatFile(df, OpenInWhat.EDITOR_TEXT, null);
                                    } else {
                                        Project.getOpenTextWindows().iterator().next().openNewDatFileTab(df, true);
                                    }
                                }
                                win.cleanupClosedData();
                                win.regainFocus();
                            }
                        });
                    } else if (f.isDirectory()) {
                        MenuItem mntmItem2 = new MenuItem(lastOpenedMenu, I18n.noBiDirectionalTextStyle());
                        mntmItem2.setEnabled(true);

                        Object[] messageArguments = {path};
                        MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                        formatter.setLocale(MyLanguage.getLocale());
                        formatter.applyPattern(I18n.E3D_LAST_PROJECT);

                        mntmItem2.setText(formatter.format(messageArguments));
                        widgetUtil(mntmItem2).addSelectionListener(e12 -> {
                            File f12 = new File(path);
                            if (f12.exists() && f12.isDirectory() && f12.canRead() && ProjectActions.openProject(path)) {
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
                            }
                            
                            Project.setFileToEdit(View.DUMMY_DATFILE);
                            win.updateTreeUnsavedEntries();
                            win.regainFocus();
                        });
                    }
                }
            }

            java.awt.Point b = java.awt.MouseInfo.getPointerInfo().getLocation();
            final int x = (int) b.getX();
            final int y = (int) b.getY();

            lastOpenedMenu.setLocation(x, y);
            lastOpenedMenu.setVisible(true);
            win.regainFocus();
        });

        widgetUtil(btnNewDatPtr[0]).addSelectionListener(e -> {
            DatFile dat = win.createNewDatFile(getShell(), OpenInWhat.EDITOR_TEXT_AND_3D);
            if (dat != null) {
                NewOpenSaveProjectToolItem.addRecentFile(dat);
                final File f = new File(dat.getNewName());
                if (f.getParentFile() != null) {
                    Project.setLastVisitedPath(f.getParentFile().getAbsolutePath());
                }
            }
            win.regainFocus();
        });

        widgetUtil(btnOpenDatPtr[0]).addSelectionListener(e -> {
            open(btnOpenDatPtr[0].getShell(), null);
        });

        widgetUtil(btnSaveDatPtr[0]).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null && !Project.getFileToEdit().equals(View.DUMMY_DATFILE)) {
                final DatFile df = Project.getFileToEdit();
                NewOpenSaveProjectToolItem.addRecentFile(df);
                if (!df.isReadOnly() && Project.getUnsavedFiles().contains(df)) {
                    if (df.save()) {
                        NewOpenSaveProjectToolItem.addRecentFile(df);
                        Editor3DWindow.getWindow().updateTreeUnsavedEntries();
                    } else {
                        MessageBox messageBoxError = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                        messageBoxError.setText(I18n.DIALOG_ERROR);
                        messageBoxError.setMessage(I18n.DIALOG_CANT_SAVE_FILE);
                    }
                }
            }
            win.regainFocus();
        });

        widgetUtil(btnSaveAsDatPtr[0]).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null && !Project.getFileToEdit().equals(View.DUMMY_DATFILE)) {
                final DatFile df2 = Project.getFileToEdit();

                FileDialog fd = new FileDialog(win.getShell(), SWT.SAVE);
                fd.setText(I18n.E3D_SAVE_DAT_FILE_AS);
                fd.setOverwrite(true);

                File parentDirectory = new File(df2.getNewName()).getParentFile();
                if (parentDirectory.exists()) {
                    fd.setFilterPath(parentDirectory.getAbsolutePath());
                } else {
                    fd.setFilterPath(Project.getLastVisitedPath());
                }

                String[] filterExt = { "*.dat", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
                fd.setFilterExtensions(filterExt);
                String[] filterNames = {I18n.E3D_LDRAW_SOURCE_FILE, I18n.E3D_ALL_FILES};
                fd.setFilterNames(filterNames);

                while (true) {
                    try {
                        String selected = fd.open();
                        if (selected != null) {

                            if (Editor3DWindow.getWindow().isFileNameAllocated(selected, new DatFile(selected), true)) {
                                MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.RETRY | SWT.CANCEL);
                                messageBox.setText(I18n.DIALOG_ALREADY_ALLOCATED_NAME_TITLE);
                                messageBox.setMessage(I18n.DIALOG_ALREADY_ALLOCATED_NAME);

                                int result = messageBox.open();

                                if (result == SWT.CANCEL) {
                                    break;
                                } else if (result == SWT.RETRY) {
                                    continue;
                                }
                            }

                            df2.saveAs(selected);

                            DatFile df = Editor3DWindow.getWindow().openDatFile(OpenInWhat.EDITOR_TEXT_AND_3D, selected, false);
                            if (df != null) {
                                NewOpenSaveProjectToolItem.addRecentFile(df);
                                final File f2 = new File(df.getNewName());
                                if (f2.getParentFile() != null) {
                                    Project.setLastVisitedPath(f2.getParentFile().getAbsolutePath());
                                }
                            }
                        }
                    } catch (Exception ex) {
                        NLogger.error(getClass(), ex);
                    }
                    break;
                }

            }
            win.regainFocus();
        });
    }
    
    public static void open(final Shell sh, EditorTextWindow twin) {
        final Editor3DWindow win = Editor3DWindow.getWindow();
        boolean tabSync = WorkbenchManager.getUserSettingState().isSyncingTabs();
        WorkbenchManager.getUserSettingState().setSyncingTabs(false);

        FileDialog fd = new FileDialog(sh, SWT.MULTI);
        fd.setText(I18n.E3D_OPEN_DAT_FILE);

        fd.setFilterPath(Project.getLastVisitedPath());

        String[] filterExt = { "*.dat", "*.stl", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        fd.setFilterExtensions(filterExt);
        String[] filterNames = {I18n.E3D_LDRAW_SOURCE_FILE, I18n.E3D_STL_FILE, I18n.E3D_ALL_FILES};
        fd.setFilterNames(filterNames);

        String selected = fd.open();
        if (selected == null) {
            WorkbenchManager.getUserSettingState().setSyncingTabs(tabSync);
            return;
        }

        for (String fileName : fd.getFileNames()) {
            final String absoluteFilePath;
            if (fileName.toLowerCase().endsWith(".stl")) { //$NON-NLS-1$
                NLogger.debug(NewOpenSaveDatfileToolItem.class, "Convert stl-file to dat-file..."); //$NON-NLS-1$
                String stlFilePath = new File(fd.getFilterPath() + File.separator + fileName).getAbsolutePath();
                absoluteFilePath = new File(fd.getFilterPath() + File.separator + fileName).getAbsolutePath() + ".dat"; //$NON-NLS-1$
                try (UTF8PrintWriter r = new UTF8PrintWriter(absoluteFilePath)) {
                    r.println(Stl2Dat.convertStlToDatFile(stlFilePath, WorkbenchManager.getUserSettingState()));
                    r.flush();
                } catch (IOException ioe) {
                    NLogger.debug(NewOpenSaveDatfileToolItem.class, ioe);
                }
            } else {
                absoluteFilePath = new File(fd.getFilterPath() + File.separator + fileName).getAbsolutePath();
            }
            
            DatFile dat = win.openDatFile(OpenInWhat.EDITOR_3D, absoluteFilePath, true);
            if (dat != null) {
                NewOpenSaveProjectToolItem.addRecentFile(dat);
                final File f = new File(dat.getNewName());
                if (f.getParentFile() != null) {
                    Project.setLastVisitedPath(f.getParentFile().getAbsolutePath());
                }
                boolean fileIsOpenInTextEditor = false;
                for (EditorTextWindow w : Project.getOpenTextWindows()) {
                    for (CTabItem t : w.getTabFolder().getItems()) {
                        if (dat.equals(((CompositeTab) t).getState().getFileNameObj())) {
                            fileIsOpenInTextEditor = true;
                        }
                        if (fileIsOpenInTextEditor) break;
                    }
                    if (fileIsOpenInTextEditor) break;
                }
                if (Project.getOpenTextWindows().isEmpty() || fileIsOpenInTextEditor) {
                    win.openDatFile(dat, OpenInWhat.EDITOR_TEXT, null);
                } else {
                    if (twin == null) {
                        twin = Project.getOpenTextWindows().iterator().next();
                    }
                    
                    twin.openNewDatFileTab(dat, true);
                }
                Project.setFileToEdit(dat);
            }
        }

        win.updateTabs();
        WorkbenchManager.getUserSettingState().setSyncingTabs(tabSync);
        win.regainFocus();
    }
}
