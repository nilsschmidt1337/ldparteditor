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
package org.nschmidt.ldparteditor.shell.editortext;

import static org.nschmidt.ldparteditor.helper.WidgetUtility.widgetUtil;

import java.io.File;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CTabFolder2Listener;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.composite.Composite3D;
import org.nschmidt.ldparteditor.composite.CompositeContainer;
import org.nschmidt.ldparteditor.composite.compositetab.CompositeTab;
import org.nschmidt.ldparteditor.composite.compositetab.CompositeTabFolder;
import org.nschmidt.ldparteditor.composite.compositetab.CompositeTabState;
import org.nschmidt.ldparteditor.data.ColourChanger;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.QuadMerger;
import org.nschmidt.ldparteditor.data.QuadSplitter;
import org.nschmidt.ldparteditor.data.Rounder;
import org.nschmidt.ldparteditor.data.Unrectifier;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.dialog.colour.ColourDialog;
import org.nschmidt.ldparteditor.dialog.round.RoundDialog;
import org.nschmidt.ldparteditor.dialog.sort.SortDialog;
import org.nschmidt.ldparteditor.dnd.TextTabDragAndDropTransfer;
import org.nschmidt.ldparteditor.dnd.TextTabDragAndDropType;
import org.nschmidt.ldparteditor.enumtype.LDConfig;
import org.nschmidt.ldparteditor.enumtype.MyLanguage;
import org.nschmidt.ldparteditor.enumtype.OpenInWhat;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.helper.Cocoa;
import org.nschmidt.ldparteditor.helper.ShellHelper;
import org.nschmidt.ldparteditor.helper.Version;
import org.nschmidt.ldparteditor.helper.compositetext.Annotator;
import org.nschmidt.ldparteditor.helper.compositetext.AnnotatorTexmap;
import org.nschmidt.ldparteditor.helper.compositetext.BFCswapper;
import org.nschmidt.ldparteditor.helper.compositetext.Inliner;
import org.nschmidt.ldparteditor.helper.compositetext.SubfileCompiler;
import org.nschmidt.ldparteditor.helper.compositetext.Text2SelectionConverter;
import org.nschmidt.ldparteditor.helper.compositetext.VertexMarker;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.resource.ResourceManager;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.NewOpenSaveProjectToolItem;
import org.nschmidt.ldparteditor.shell.searchnreplace.SearchWindow;
import org.nschmidt.ldparteditor.workbench.EditorTextWindowState;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * The text editor window
 * <p>
 * Note: This class should be instantiated, it defines all listeners and part of
 * the business logic.
 *
 * @author nils
 *
 */
public class EditorTextWindow extends EditorTextDesign {

    private static CompositeTab draggedTabOrigin;
    private static CompositeTab draggedTabTarget;
    private static CompositeTabFolder dragFolderTarget;
    private static CompositeTabFolder dragFolderOrigin;

    /** The window state of this window */
    private EditorTextWindowState editorTextWindowState;

    public static void createTemporaryWindow(final DatFile df) {
        new EditorTextWindow().run(df, true);
    }
    
    public static EditorTextWindow createNewWindowIfRequired(final DatFile df) {
        for (EditorTextWindow w : Project.getOpenTextWindows()) {
            final CompositeTabFolder cTabFolder = w.getTabFolder();
            for (CTabItem t : cTabFolder.getItems()) {
                if (df.equals(((CompositeTab) t).getState().getFileNameObj())) {
                    cTabFolder.setSelection(t);
                    ((CompositeTab) t).getControl().getShell().forceActive();
                    
                    // Don't create a tab for already opened files
                    return w;
                }
            }
        }
        
        final EditorTextWindow result = new EditorTextWindow();
        result.run(df, false);
        return result;
    }
    
    /**
     * Create the application window.
     */
    private EditorTextWindow() {
        super();
        super.setEditorTextWindow(this);
    }

    /**
     * Create the application window.
     */
    public EditorTextWindow(Composite parent, ApplicationWindow win) {
        super(parent, win);
        super.setEditorTextWindow(win);
    }

    /**
     * Run a fresh instance of this window
     */
    private void run(DatFile fileToOpen, boolean closeASAP) {
        if (!isSeperateWindow()) {
            return;
        }
        Project.getOpenTextWindows().add(this);
        // Load the window state data
        this.editorTextWindowState = WorkbenchManager.getEditorTextWindowState();
        // Creating the window to get the shell
        this.create();
        final Shell sh = this.getShell();
        sh.setText(Version.getApplicationName() + " " + Version.getVersion()); //$NON-NLS-1$
        sh.setImage(ResourceManager.getImage("imgDuke2.png")); //$NON-NLS-1$
        sh.setMinimumSize(640, 480);
        sh.setBounds(this.editorTextWindowState.getWindowState().getSizeAndPosition());
        if (this.editorTextWindowState.getWindowState().isCentered()) {
            ShellHelper.centerShellOnPrimaryScreen(sh);
        }
        // Maximize has to be called asynchronously
        sh.getDisplay().asyncExec(() -> {
            try {
                if (!sh.isDisposed()) {
                    sh.setMaximized(editorTextWindowState.getWindowState().isMaximized());
                    sh.forceActive();
                }
            } catch (SWTException consumed) {
                NLogger.debug(EditorTextWindow.class, consumed);
            }
        });
        // The window reference has to be added to the tab folder
        tabFolderPtr[0].setWindow(editorTextWindow);
        // and the tab for the file has to be created.
        {
            CompositeTab tbtmnewItem = new CompositeTab(tabFolderPtr[0], SWT.CLOSE);
            tbtmnewItem.setFolderAndWindow(tabFolderPtr[0], editorTextWindow);
            tbtmnewItem.getState().setFileNameObj(fileToOpen);
            tbtmnewItem.parseForErrorAndHints();
            tabFolderPtr[0].setSelection(tbtmnewItem);
        }

        // MARK All final listeners will be configured here..
        registerEvents();

        this.open();
        if (closeASAP) {
            closeTabWithDatfile(Project.getFileToEdit());
        }
    }

    /**
     * The Shell-Close-Event
     */
    @Override
    protected void handleShellCloseEvent() {

        // Unsaved changes dialog only for files which are out of the scope of the current project..

        Set<DatFile> unsavedFiles = new HashSet<>(Project.getUnsavedFiles());
        {
            @SuppressWarnings("unchecked")
            List<DatFile> ta = (List<DatFile>) Editor3DWindow.getWindow().getProjectParts().getData();
            for (DatFile ti : ta) {
                unsavedFiles.remove(ti);
            }
        }
        {
            @SuppressWarnings("unchecked")
            List<DatFile> ta = (List<DatFile>) Editor3DWindow.getWindow().getProjectSubparts().getData();
            for (DatFile ti : ta) {
                unsavedFiles.remove(ti);
            }
        }
        {
            @SuppressWarnings("unchecked")
            List<DatFile> ta = (List<DatFile>) Editor3DWindow.getWindow().getProjectPrimitives().getData();
            for (DatFile ti : ta) {
                unsavedFiles.remove(ti);
            }
        }
        {
            @SuppressWarnings("unchecked")
            List<DatFile> ta = (List<DatFile>) Editor3DWindow.getWindow().getProjectPrimitives48().getData();
            for (DatFile ti : ta) {
                unsavedFiles.remove(ti);
            }
        }
        {
            @SuppressWarnings("unchecked")
            List<DatFile> ta = (List<DatFile>) Editor3DWindow.getWindow().getProjectPrimitives8().getData();
            for (DatFile ti : ta) {
                unsavedFiles.remove(ti);
            }
        }
        {
            @SuppressWarnings("unchecked")
            List<DatFile> ta = (List<DatFile>) Editor3DWindow.getWindow().getUnofficialParts().getData();
            for (DatFile ti : ta) {
                unsavedFiles.remove(ti);
            }
        }
        {
            @SuppressWarnings("unchecked")
            List<DatFile> ta = (List<DatFile>) Editor3DWindow.getWindow().getUnofficialSubparts().getData();
            for (DatFile ti : ta) {
                unsavedFiles.remove(ti);
            }
        }
        {
            @SuppressWarnings("unchecked")
            List<DatFile> ta = (List<DatFile>) Editor3DWindow.getWindow().getUnofficialPrimitives().getData();
            for (DatFile ti : ta) {
                unsavedFiles.remove(ti);
            }
        }
        {
            @SuppressWarnings("unchecked")
            List<DatFile> ta = (List<DatFile>) Editor3DWindow.getWindow().getUnofficialPrimitives48().getData();
            for (DatFile ti : ta) {
                unsavedFiles.remove(ti);
            }
        }
        {
            @SuppressWarnings("unchecked")
            List<DatFile> ta = (List<DatFile>) Editor3DWindow.getWindow().getUnofficialPrimitives8().getData();
            for (DatFile ti : ta) {
                unsavedFiles.remove(ti);
            }
        }
        {
            @SuppressWarnings("unchecked")
            List<DatFile> ta = (List<DatFile>) Editor3DWindow.getWindow().getOfficialParts().getData();
            for (DatFile ti : ta) {
                unsavedFiles.remove(ti);
            }
        }
        {
            @SuppressWarnings("unchecked")
            List<DatFile> ta = (List<DatFile>) Editor3DWindow.getWindow().getOfficialSubparts().getData();
            for (DatFile ti : ta) {
                unsavedFiles.remove(ti);
            }
        }
        {
            @SuppressWarnings("unchecked")
            List<DatFile> ta = (List<DatFile>) Editor3DWindow.getWindow().getOfficialPrimitives().getData();
            for (DatFile ti : ta) {
                unsavedFiles.remove(ti);
            }
        }
        {
            @SuppressWarnings("unchecked")
            List<DatFile> ta = (List<DatFile>) Editor3DWindow.getWindow().getOfficialPrimitives48().getData();
            for (DatFile ti : ta) {
                unsavedFiles.remove(ti);
            }
        }
        {
            @SuppressWarnings("unchecked")
            List<DatFile> ta = (List<DatFile>) Editor3DWindow.getWindow().getOfficialPrimitives8().getData();
            for (DatFile ti : ta) {
                unsavedFiles.remove(ti);
            }
        }

        Set<DatFile> myFiles = new HashSet<>();
        for (CTabItem tab : tabFolderPtr[0].getItems()) {
            CompositeTab cTab = (CompositeTab) tab;
            myFiles.add(cTab.getState().getFileNameObj());
        }

        unsavedFiles.retainAll(myFiles);

        for (DatFile df : unsavedFiles) {
            final String text = df.getText();
            if (df != null && !text.equals(df.getOriginalText()) || df.isVirtual() && !text.trim().isEmpty() && !text.equals(WorkbenchManager.getDefaultFileHeader())) {
                MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.CANCEL | SWT.NO);
                messageBox.setText(I18n.DIALOG_UNSAVED_CHANGES_TITLE);

                Object[] messageArguments = {df.getShortName()};
                MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                formatter.setLocale(MyLanguage.getLocale());
                formatter.applyPattern(I18n.DIALOG_UNSAVED_CHANGES);
                messageBox.setMessage(formatter.format(messageArguments));

                int result = messageBox.open();

                if (result == SWT.NO) {
                    // Skip file
                    Project.removeUnsavedFile(df);
                    Project.removeOpenedFile(df);
                    if (df.equals(Project.getFileToEdit())) {
                        Project.setFileToEdit(View.DUMMY_DATFILE);
                        for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                            if (df.equals(renderer.getC3D().getLockableDatFileReference())) {
                                renderer.getC3D().setLockableDatFileReference(View.DUMMY_DATFILE);
                            }
                        }
                    }
                } else if (result == SWT.YES) {
                    if (df.save()) {
                        NewOpenSaveProjectToolItem.addRecentFile(df);
                        ((CompositeTab) tabFolderPtr[0].getSelection()).getTextComposite().setText(df.getText());
                    } else {
                        MessageBox messageBoxError = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                        messageBoxError.setText(I18n.DIALOG_ERROR);
                        messageBoxError.setMessage(I18n.DIALOG_CANT_SAVE_FILE);
                        messageBoxError.open();
                        Editor3DWindow.getWindow().updateTreeUnsavedEntries();
                        return;
                    }
                } else if (result == SWT.CANCEL) {
                    Editor3DWindow.getWindow().updateTreeUnsavedEntries();
                    return;
                }
            }
        }
        if (isSeperateWindow()) {
            Project.getOpenTextWindows().remove(this);
        }
        
        Editor3DWindow.getWindow().updateTreeUnsavedEntries();
        
        // Save the workbench
        EditorTextWindowState stateText = WorkbenchManager.getEditorTextWindowState();
        stateText.getWindowState().setCentered(false);
        stateText.getWindowState().setMaximized(getShell().getMaximized());
        stateText.getWindowState().setSizeAndPosition(getShell().getBounds());
        setReturnCode(CANCEL);
        close();
    }

    /**
     * @return The tab folder
     */
    public CompositeTabFolder getTabFolder() {
        return this.tabFolderPtr[0];
    }

    /**
     * @return The serializable window state of the Editor3DWindow
     */
    public EditorTextWindowState getEditorTextWindowState() {
        return this.editorTextWindowState;
    }

    /**
     * @param editorTextWindowState
     *            The serializable window state of the EditorTextWindow
     */
    public void setEditorTextWindowState(EditorTextWindowState editorTextWindowState) {
        this.editorTextWindowState = editorTextWindowState;
    }

    public void closeTabWithDatfile(DatFile e) {
        CTabItem[] items =  tabFolderPtr[0].getItems().clone();
        for (CTabItem item : items) {
            CompositeTab cTab = (CompositeTab) item;
            if (cTab.getState().getFileNameObj().equals(e)) {
                item.dispose();
            }
        }
        if (isSeperateWindow() && tabFolderPtr[0].getItemCount() == 0) {
            Project.getOpenTextWindows().remove(this);
            close();
        }
    }

    public void closeAllTabs() {
        CTabItem[] items =  tabFolderPtr[0].getItems().clone();
        for (CTabItem item : items) {
            item.dispose();
        }
    }

    public void updateTabWithDatfile(DatFile e) {
        CTabItem[] items =  tabFolderPtr[0].getItems().clone();
        for (CTabItem item : items) {
            CompositeTab cTab = (CompositeTab) item;
            CompositeTabState state = cTab.getState();
            if (state.getFileNameObj().equals(e)) {
                state.setFileNameObj(e);
            }
        }
    }

    public void openNewDatFileTab(DatFile df, boolean updateLastVisited) {
        NewOpenSaveProjectToolItem.addRecentFile(df);
        final File f = new File(df.getNewName());
        if (f.getParentFile() != null && updateLastVisited) {
            Project.setLastVisitedPath(f.getParentFile().getAbsolutePath());
        }
        
        for (EditorTextWindow w : Project.getOpenTextWindows()) {
            final CompositeTabFolder cTabFolder = w.getTabFolder();
            for (CTabItem t : cTabFolder.getItems()) {
                if (df.equals(((CompositeTab) t).getState().getFileNameObj())) {
                    cTabFolder.setSelection(t);
                    ((CompositeTab) t).getControl().getShell().forceActive();
                    
                    // Don't create a tab for already opened files
                    return;
                }
            }
        }
        
        CompositeTab tbtmnewItem = new CompositeTab(tabFolderPtr[0], SWT.CLOSE);
        tbtmnewItem.setFolderAndWindow(tabFolderPtr[0], editorTextWindow);
        tbtmnewItem.getState().setFileNameObj(df);
        tabFolderPtr[0].setSelection(tbtmnewItem);
        tbtmnewItem.parseForErrorAndHints();
        tbtmnewItem.getTextComposite().redraw();
    }

    public void registerEvents() {
        {
            DropTarget dt = new DropTarget(tabFolderPtr[0].getParent().getParent(), DND.DROP_DEFAULT | DND.DROP_MOVE );
            widgetUtil(dt).setTransfer(FileTransfer.getInstance());
            dt.addDropListener(new DropTargetAdapter() {
                @Override
                public void drop(DropTargetEvent event) {
                    String[] fileList = null;
                    FileTransfer ft = FileTransfer.getInstance();
                    if (ft.isSupportedType(event.currentDataType)) {
                        fileList = (String[]) event.data;
                        if (fileList != null) {
                            for (String f : fileList) {
                                NLogger.debug(getClass(), f);
                                if (f.toLowerCase(Locale.ENGLISH).endsWith(".dat")) { //$NON-NLS-1$
                                    final File fileToOpen = new File(f);
                                    if (!fileToOpen.exists() || fileToOpen.isDirectory()) continue;
                                    if (WorkbenchManager.getUserSettingState().isSyncingTabs()) {
                                        DatFile df = Editor3DWindow.getWindow().openDatFile(OpenInWhat.EDITOR_3D, f, true);
                                        if (df != null) {
                                            NewOpenSaveProjectToolItem.addRecentFile(df);
                                            final File f2 = new File(df.getNewName());
                                            if (f2.getParentFile() != null) {
                                                Project.setLastVisitedPath(f2.getParentFile().getAbsolutePath());
                                            }
                                            Editor3DWindow.getWindow().openDatFile(df, OpenInWhat.EDITOR_TEXT, tabFolderPtr[0].getWindow());
                                        }
                                    } else {
                                        DatFile df = Editor3DWindow.getWindow().openDatFile(OpenInWhat.EDITOR_TEXT, f, true);
                                        if (df != null) {
                                            for (EditorTextWindow w : Project.getOpenTextWindows()) {
                                                for (CTabItem t : w.getTabFolder().getItems()) {
                                                    if (df.equals(((CompositeTab) t).getState().getFileNameObj())) {
                                                        w.getTabFolder().setSelection(t);
                                                        ((CompositeTab) t).getControl().getShell().forceActive();
                                                        if (w.isSeperateWindow()) {
                                                            w.open();
                                                        }
                                                        df.getVertexManager().setUpdated(true);
                                                    }
                                                }
                                            }
                                            df.getVertexManager().addSnapshot();
                                        }
                                    }
                                    Editor3DWindow.getWindow().updateTreeUnsavedEntries();
                                    break;
                                }
                            }
                        }
                    }
                }
            });
        }
        widgetUtil(tabFolderPtr[0]).addSelectionListener(e -> {
            CompositeTab ct = (CompositeTab) e.item;
            if (WorkbenchManager.getUserSettingState().isSyncingTabs() && ct != null) {
                CompositeTabState state = ct.getState();
                if (state != null) {
                    DatFile df = state.getFileNameObj();
                    if (df != null) {
                        if (Editor3DWindow.getNoSyncDeadlock().compareAndSet(false, true)) {
                            Editor3DWindow.getWindow().selectTabWithDatFile(df);
                            Editor3DWindow.getNoSyncDeadlock().set(false);
                        }
                        NLogger.debug(EditorTextWindow.class, "Old DatFile name {0}", df.getOldName()); //$NON-NLS-1$
                    }
                }
            }
        });

        if (btnShowLeftPtr[0] != null) widgetUtil(btnShowLeftPtr[0]).addSelectionListener(e -> {
            final SashForm sf = Editor3DWindow.getWindow().getSplitSashForm();
            int[] w = sf.getWeights();
            if (w[1] * 9 > w[0]) {
                sf.setWeights(95, 5);
            } else {
                sf.setWeights(Editor3DWindow.sashWeight1, Editor3DWindow.sashWeight2);
            }
        });

        if (btnShowRightPtr[0] != null) widgetUtil(btnShowRightPtr[0]).addSelectionListener(e -> {
            final SashForm sf = Editor3DWindow.getWindow().getSplitSashForm();
            int[] w = sf.getWeights();
            if (w[0] * 9 > w[1]) {
                sf.setWeights(5, 95);
            } else {
                sf.setWeights(Editor3DWindow.sashWeight1, Editor3DWindow.sashWeight2);
            }
        });

        if (btnSameWidthPtr[0] != null) widgetUtil(btnSameWidthPtr[0]).addSelectionListener(e -> Editor3DWindow.getWindow().getSplitSashForm().setWeights(50, 50));

        widgetUtil(btnNewPtr[0]).addSelectionListener(e -> {
            final boolean isSyncTabs = WorkbenchManager.getUserSettingState().isSyncingTabs();
            DatFile df;
            if (isSyncTabs) {
                df = Editor3DWindow.getWindow().createNewDatFile(btnNewPtr[0].getShell(), OpenInWhat.EDITOR_3D);
            } else {
                df = Editor3DWindow.getWindow().createNewDatFile(btnNewPtr[0].getShell(), OpenInWhat.EDITOR_TEXT);
            }
            if (df != null && isSyncTabs) {
                Editor3DWindow.getWindow().openDatFile(df, OpenInWhat.EDITOR_TEXT, editorTextWindow);
                final File f = new File(df.getNewName());
                if (f.getParentFile() != null) {
                    Project.setLastVisitedPath(f.getParentFile().getAbsolutePath());
                }
            }
        });
        widgetUtil(btnOpenPtr[0]).addSelectionListener(e -> {

            FileDialog fd = new FileDialog(btnOpenPtr[0].getShell(), SWT.MULTI);
            fd.setText(I18n.E3D_OPEN_DAT_FILE);

            fd.setFilterPath(Project.getLastVisitedPath());

            String[] filterExt = { "*.dat", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
            fd.setFilterExtensions(filterExt);
            String[] filterNames = {I18n.E3D_LDRAW_SOURCE_FILE, I18n.E3D_ALL_FILES};
            fd.setFilterNames(filterNames);

            String selected = fd.open();
            if (selected == null) {
                return;
            }

            for (String fileName : fd.getFileNames()) {
                final String filePath = fd.getFilterPath() + File.separator + fileName;
                if (WorkbenchManager.getUserSettingState().isSyncingTabs()) {
                    DatFile df1 = Editor3DWindow.getWindow().openDatFile(OpenInWhat.EDITOR_3D, filePath, true);
                    if (df1 != null) {
                        openNewDatFileTab(df1, true);
                    }
                } else {
                    DatFile df2 = Editor3DWindow.getWindow().openDatFile(OpenInWhat.EDITOR_TEXT, filePath, true);
                    if (df2 != null) {
                        for (EditorTextWindow w : Project.getOpenTextWindows()) {
                            for (CTabItem t : w.getTabFolder().getItems()) {
                                if (df2.equals(((CompositeTab) t).getState().getFileNameObj())) {
                                    w.getTabFolder().setSelection(t);
                                    ((CompositeTab) t).getControl().getShell().forceActive();
                                    if (w.isSeperateWindow()) {
                                        w.open();
                                    }
                                    df2.getVertexManager().setUpdated(true);
                                }
                            }
                        }
                        df2.getVertexManager().addSnapshot();
                    }
                }
            }
            Editor3DWindow.getWindow().cleanupClosedData();
            Editor3DWindow.getWindow().updateTreeUnsavedEntries();
        });
        widgetUtil(btnSavePtr[0]).addSelectionListener(e -> {
            final CompositeTab ct = (CompositeTab) tabFolderPtr[0].getSelection();
            if (ct != null) {
                CompositeTabState state = ct.getState();
                DatFile df = state.getFileNameObj();
                NewOpenSaveProjectToolItem.addRecentFile(df);
                final Point selection = ct.getTextComposite().getSelection();
                final int x = selection.x;
                final int y = selection.y;
                if (!df.isReadOnly() && Project.getUnsavedFiles().contains(df)) {
                    if (df.save()) {
                        NewOpenSaveProjectToolItem.addRecentFile(df);
                        Editor3DWindow.getWindow().updateTreeUnsavedEntries();
                        ((CompositeTab) tabFolderPtr[0].getSelection()).getTextComposite().setText(state.getFileNameObj().getText());
                    } else {
                        MessageBox messageBoxError = new MessageBox(btnSavePtr[0].getShell(), SWT.ICON_ERROR | SWT.OK);
                        messageBoxError.setText(I18n.DIALOG_ERROR);
                        messageBoxError.setMessage(I18n.DIALOG_CANT_SAVE_FILE);
                    }
                }
                ct.getTextComposite().setSelection(x, y);
                ct.getTextComposite().forceFocus();
            }
        });
        widgetUtil(btnSaveAsPtr[0]).addSelectionListener(e -> {
            if (tabFolderPtr[0].getSelection() != null) {
                saveAs(((CompositeTab) tabFolderPtr[0].getSelection()).getState().getFileNameObj(), null, null);
            }
        });
        widgetUtil(btnCutPtr[0]).addSelectionListener(e -> tabFolderPtr[0].cut());
        widgetUtil(btnCopyPtr[0]).addSelectionListener(e -> tabFolderPtr[0].copy());
        widgetUtil(btnPastePtr[0]).addSelectionListener(e -> tabFolderPtr[0].paste());
        widgetUtil(btnDeletePtr[0]).addSelectionListener(e -> tabFolderPtr[0].delete());

        widgetUtil(btnUndoPtr[0]).addSelectionListener(e -> {
            CompositeTab selection = (CompositeTab) tabFolderPtr[0].getSelection();
            if (selection != null && !selection.getState().getFileNameObj().isReadOnly()) {
                if (!selection.getState().getFileNameObj().getVertexManager().isUpdated()){
                    return;
                }
                selection.getState().getFileNameObj().undo(true);
            }
        });

        widgetUtil(btnRedoPtr[0]).addSelectionListener(e -> {
            CompositeTab selection = (CompositeTab) tabFolderPtr[0].getSelection();
            if (selection != null && !selection.getState().getFileNameObj().isReadOnly()) {
                if (!selection.getState().getFileNameObj().getVertexManager().isUpdated()){
                    return;
                }
                selection.getState().getFileNameObj().redo(true);
            }
        });

        if (NLogger.debugging) {
            widgetUtil(btnAddHistoryPtr[0]).addSelectionListener(e -> {
                CompositeTab selection = (CompositeTab) tabFolderPtr[0].getSelection();
                if (selection != null && !selection.getState().getFileNameObj().isReadOnly()) {
                    if (!selection.getState().getFileNameObj().getVertexManager().isUpdated()){
                        return;
                    }
                    selection.getState().getFileNameObj().addHistory();
                }
            });
        }

        widgetUtil(btnSyncEditPtr[0]).addSelectionListener(e -> {
            CompositeTab selection = (CompositeTab) tabFolderPtr[0].getSelection();
            if (selection != null && !selection.getState().getFileNameObj().isReadOnly()) {
                if (!selection.getState().getFileNameObj().getVertexManager().isUpdated()){
                    return;
                }
                final StyledText st = selection.getTextComposite();
                final CompositeTabState state = selection.getState();
                final DatFile df = state.getFileNameObj();
                if (!state.isReplacingVertex()) {
                    VertexMarker.markTheVertex(state, st, df);
                    if (state.getWindow() == Editor3DWindow.getWindow()) {
                        Editor3DWindow.getStatusLabel().setText(I18n.EDITORTEXT_SYNC_EDIT);
                        Editor3DWindow.getStatusLabel().setSize(Editor3DWindow.getStatusLabel().computeSize(SWT.DEFAULT, SWT.DEFAULT));
                        Editor3DWindow.getStatusLabel().update();
                    } else {
                        state.getWindow().setStatus(I18n.EDITORTEXT_SYNC_EDIT);
                    }
                } else {
                    state.setReplacingVertex(false);
                    df.getVertexManager().setVertexToReplace(null);
                    st.redraw(0, 0, st.getBounds().width, st.getBounds().height, true);
                    if (state.getWindow() == Editor3DWindow.getWindow()) {
                        Editor3DWindow.getStatusLabel().setText(I18n.EDITORTEXT_SYNC_EDIT_DEACTIVATED);
                        Editor3DWindow.getStatusLabel().setSize(Editor3DWindow.getStatusLabel().computeSize(SWT.DEFAULT, SWT.DEFAULT));
                        Editor3DWindow.getStatusLabel().update();
                    } else {
                        state.getWindow().setStatus(I18n.EDITORTEXT_SYNC_EDIT_DEACTIVATED);
                    }
                }
                st.forceFocus();
            }
        });

        widgetUtil(btnInlinePtr[0]).addSelectionListener(e -> {
            CompositeTab selection = (CompositeTab) tabFolderPtr[0].getSelection();
            if (selection != null && !selection.getState().getFileNameObj().isReadOnly()) {
                if (!selection.getState().getFileNameObj().getVertexManager().isUpdated()){
                    return;
                }
                NLogger.debug(getClass(), "Inlining.."); //$NON-NLS-1$
                final StyledText st = selection.getTextComposite();
                int s1 = st.getSelectionRange().x;
                int s2 = s1 + st.getSelectionRange().y;
                int fromLine = s1 > -1 ? st.getLineAtOffset(s1) : s1 * -1;
                int toLine = s2 > -1 ? st.getLineAtOffset(s2) : s2 * -1;
                fromLine++;
                toLine++;
                Inliner.withSubfileReference = false;
                Inliner.recursively = false;
                Inliner.noComment = false;
                NLogger.debug(getClass(), "From line {0}", fromLine); //$NON-NLS-1$
                NLogger.debug(getClass(), "To   line {0}", toLine); //$NON-NLS-1$
                Inliner.inline(st, fromLine, toLine, selection.getState().getFileNameObj());
                st.forceFocus();
            }
        });

        widgetUtil(btnInlineDeepPtr[0]).addSelectionListener(e -> {
            CompositeTab selection = (CompositeTab) tabFolderPtr[0].getSelection();
            if (selection != null && !selection.getState().getFileNameObj().isReadOnly()) {
                if (!selection.getState().getFileNameObj().getVertexManager().isUpdated()){
                    return;
                }
                NLogger.debug(getClass(), "Inlining (deep).."); //$NON-NLS-1$
                final StyledText st = selection.getTextComposite();
                int s1 = st.getSelectionRange().x;
                int s2 = s1 + st.getSelectionRange().y;
                int fromLine = s1 > -1 ? st.getLineAtOffset(s1) : s1 * -1;
                int toLine = s2 > -1 ? st.getLineAtOffset(s2) : s2 * -1;
                fromLine++;
                toLine++;
                Inliner.withSubfileReference = false;
                Inliner.recursively = true;
                Inliner.noComment = false;
                NLogger.debug(getClass(), "From line {0}", fromLine); //$NON-NLS-1$
                NLogger.debug(getClass(), "To   line {0}", toLine); //$NON-NLS-1$
                Inliner.inline(st, fromLine, toLine, selection.getState().getFileNameObj());
                st.forceFocus();
            }
        });

        widgetUtil(btnAnnotatePtr[0]).addSelectionListener(e -> {
            CompositeTab selection = (CompositeTab) tabFolderPtr[0].getSelection();
            if (selection != null && !selection.getState().getFileNameObj().isReadOnly()) {
                if (!selection.getState().getFileNameObj().getVertexManager().isUpdated()){
                    return;
                }
                NLogger.debug(getClass(), "Toggle Comment.."); //$NON-NLS-1$
                final StyledText st = selection.getTextComposite();
                int s1 = st.getSelectionRange().x;
                int s2 = s1 + st.getSelectionRange().y;
                int fromLine = s1 > -1 ? st.getLineAtOffset(s1) : s1 * -1;
                int toLine = s2 > -1 ? st.getLineAtOffset(s2) : s2 * -1;
                if (fromLine != toLine && s2 == st.getOffsetAtLine(toLine)) {
                    toLine -= 1;
                }
                fromLine++;
                toLine++;
                NLogger.debug(getClass(), "From line {0}", fromLine); //$NON-NLS-1$
                NLogger.debug(getClass(), "To   line {0}", toLine); //$NON-NLS-1$
                Annotator.annotate(fromLine, toLine, selection.getState().getFileNameObj());
                st.forceFocus();
            }
        });

        widgetUtil(btnTexmapPtr[0]).addSelectionListener(e -> {
            CompositeTab selection = (CompositeTab) tabFolderPtr[0].getSelection();
            if (selection != null && !selection.getState().getFileNameObj().isReadOnly()) {
                if (!selection.getState().getFileNameObj().getVertexManager().isUpdated()){
                    return;
                }
                NLogger.debug(getClass(), "Toggle Texmap.."); //$NON-NLS-1$
                final StyledText st = selection.getTextComposite();
                int s1 = st.getSelectionRange().x;
                int s2 = s1 + st.getSelectionRange().y;
                int fromLine = s1 > -1 ? st.getLineAtOffset(s1) : s1 * -1;
                int toLine = s2 > -1 ? st.getLineAtOffset(s2) : s2 * -1;
                if (fromLine != toLine && s2 == st.getOffsetAtLine(toLine)) {
                    toLine -= 1;
                }
                fromLine++;
                toLine++;
                NLogger.debug(getClass(), "From line {0}", fromLine); //$NON-NLS-1$
                NLogger.debug(getClass(), "To   line {0}", toLine); //$NON-NLS-1$
                AnnotatorTexmap.annotate(fromLine, toLine, selection.getState().getFileNameObj());
                st.forceFocus();
            }
        });

        widgetUtil(btnShowSelectionIn3DPtr[0]).addSelectionListener(e -> {
            CompositeTab selection = (CompositeTab) tabFolderPtr[0].getSelection();
            if (selection != null) {
                if (!selection.getState().getFileNameObj().getVertexManager().isUpdated()){
                    return;
                }
                NLogger.debug(getClass(), "Selecting.."); //$NON-NLS-1$
                final StyledText st = selection.getTextComposite();
                int s1 = st.getSelectionRange().x;
                int s2 = s1 + st.getSelectionRange().y;
                int fromLine = s1 > -1 ? st.getLineAtOffset(s1) : s1 * -1;
                int toLine = s2 > -1 ? st.getLineAtOffset(s2) : s2 * -1;
                fromLine++;
                toLine++;
                NLogger.debug(getClass(), "From line {0}", fromLine); //$NON-NLS-1$
                NLogger.debug(getClass(), "To   line {0}", toLine); //$NON-NLS-1$
                Text2SelectionConverter.convert(fromLine, toLine, selection.getState().getFileNameObj());
                selection.getState().getFileNameObj().addHistory();
                st.redraw(0, 0, st.getBounds().width, st.getBounds().height, true);
                st.forceFocus();
            }
        });

        widgetUtil(btnOpenIn3DPtr[0]).addSelectionListener(e -> {
            CompositeTab selection = (CompositeTab) tabFolderPtr[0].getSelection();
            if (selection != null) {
                if (!selection.getState().getFileNameObj().getVertexManager().isUpdated()){
                    return;
                }
                DatFile df = selection.getState().getFileNameObj();
                openIn3D(df);
            }
        });

        widgetUtil(btnFindAndReplacePtr[0]).addSelectionListener(e -> {
            CompositeTab selection = (CompositeTab) tabFolderPtr[0].getSelection();
            if (selection != null) {
                SearchWindow win = Editor3DWindow.getWindow().getSearchWindow();
                if (win != null) {
                    win.close();
                }
                Editor3DWindow.getWindow().setSearchWindow(new SearchWindow(btnFindAndReplacePtr[0].getShell()));
                Editor3DWindow.getWindow().getSearchWindow().run();
                Editor3DWindow.getWindow().getSearchWindow().setTextComposite(selection);
                Editor3DWindow.getWindow().getSearchWindow().setScopeToAll();
            }
        });

        widgetUtil(btnSortPtr[0]).addSelectionListener(e -> {
            CompositeTab selection = (CompositeTab) tabFolderPtr[0].getSelection();
            if (selection != null && !selection.getState().getFileNameObj().isReadOnly()) {
                if (!selection.getState().getFileNameObj().getVertexManager().isUpdated()){
                    return;
                }
                NLogger.debug(getClass(), "Open sorting dialog.."); //$NON-NLS-1$
                final StyledText st = selection.getTextComposite();
                int s1 = st.getSelectionRange().x;
                int s2 = s1 + st.getSelectionRange().y;
                int fromLine = s1 > -1 ? st.getLineAtOffset(s1) : s1 * -1;
                int toLine = s2 > -1 ? st.getLineAtOffset(s2) : s2 * -1;
                fromLine++;
                toLine++;
                NLogger.debug(getClass(), "From line {0}", fromLine); //$NON-NLS-1$
                NLogger.debug(getClass(), "To   line {0}", toLine); //$NON-NLS-1$
                final SortDialog sd = new SortDialog(btnSortPtr[0].getShell(), fromLine, toLine, selection.getState().getFileNameObj());
                sd.open();
                st.forceFocus();
            }
        });

        widgetUtil(btnSplitQuadPtr[0]).addSelectionListener(e -> {
            CompositeTab selection = (CompositeTab) tabFolderPtr[0].getSelection();
            if (selection != null && !selection.getState().getFileNameObj().isReadOnly()) {
                if (!selection.getState().getFileNameObj().getVertexManager().isUpdated()){
                    return;
                }
                NLogger.debug(getClass(), "Split quads into triangles.."); //$NON-NLS-1$
                final StyledText st = selection.getTextComposite();
                int s1 = st.getSelectionRange().x;
                int s2 = s1 + st.getSelectionRange().y;
                int fromLine = s1 > -1 ? st.getLineAtOffset(s1) : s1 * -1;
                int toLine = s2 > -1 ? st.getLineAtOffset(s2) : s2 * -1;
                fromLine++;
                toLine++;
                NLogger.debug(getClass(), "From line {0}", fromLine); //$NON-NLS-1$
                NLogger.debug(getClass(), "To   line {0}", toLine); //$NON-NLS-1$
                QuadSplitter.splitQuadsIntoTriangles(st, fromLine, toLine, selection.getState().getFileNameObj());
                st.forceFocus();
            }
        });

        widgetUtil(btnMergeQuadPtr[0]).addSelectionListener(e -> {
            CompositeTab selection = (CompositeTab) tabFolderPtr[0].getSelection();
            if (selection != null && !selection.getState().getFileNameObj().isReadOnly()) {
                if (!selection.getState().getFileNameObj().getVertexManager().isUpdated()){
                    return;
                }
                NLogger.debug(getClass(), "Merge triangles into quad.."); //$NON-NLS-1$
                final StyledText st = selection.getTextComposite();
                int s1 = st.getSelectionRange().x;
                int s2 = s1 + st.getSelectionRange().y;
                int fromLine = s1 > -1 ? st.getLineAtOffset(s1) : s1 * -1;
                int toLine = s2 > -1 ? st.getLineAtOffset(s2) : s2 * -1;
                fromLine++;
                toLine++;
                NLogger.debug(getClass(), "From line {0}", fromLine); //$NON-NLS-1$
                NLogger.debug(getClass(), "To   line {0}", toLine); //$NON-NLS-1$
                QuadMerger.mergeTrianglesIntoQuad(fromLine, toLine, selection.getState().getFileNameObj());
                st.forceFocus();
            }
        });

        widgetUtil(btnUnrectifyPtr[0]).addSelectionListener(e -> {
            CompositeTab selection = (CompositeTab) tabFolderPtr[0].getSelection();
            if (selection != null && !selection.getState().getFileNameObj().isReadOnly()) {
                if (!selection.getState().getFileNameObj().getVertexManager().isUpdated()){
                    return;
                }
                NLogger.debug(getClass(), "Unrectify.."); //$NON-NLS-1$
                final StyledText st = selection.getTextComposite();
                final boolean splitQuadsAndRectPrimitives = !Cocoa.checkCtrlOrCmdPressed(e.stateMask);
                Unrectifier.splitAllIntoTriangles(st, selection.getState().getFileNameObj(), splitQuadsAndRectPrimitives);
                st.forceFocus();
            }
        });

        widgetUtil(btnPalettePtr[0]).addSelectionListener(e -> {
            final GColour[] gColour2 = new GColour[1];
            new ColourDialog(btnPalettePtr[0].getShell(), gColour2, true).run();
            if (gColour2[0] != null) {
                int num = gColour2[0].getColourNumber();
                if (!LDConfig.hasColour(num)) {
                    num = -1;
                }

                CompositeTab selection = (CompositeTab) tabFolderPtr[0].getSelection();
                if (selection != null) {
                    DatFile df = selection.getState().getFileNameObj();
                    if (!df.isReadOnly() && df.getVertexManager().isUpdated()) {
                        NLogger.debug(getClass(), "Change colours..."); //$NON-NLS-1$
                        final StyledText st = selection.getTextComposite();
                        int s1 = st.getSelectionRange().x;
                        int s2 = s1 + st.getSelectionRange().y;
                        int fromLine = s1 > -1 ? st.getLineAtOffset(s1) : s1 * -1;
                        int toLine = s2 > -1 ? st.getLineAtOffset(s2) : s2 * -1;
                        fromLine++;
                        toLine++;
                        NLogger.debug(getClass(), "From line {0}", fromLine); //$NON-NLS-1$
                        NLogger.debug(getClass(), "To   line {0}", toLine); //$NON-NLS-1$
                        ColourChanger.changeColour(fromLine, toLine, df, num, gColour2[0].getR(), gColour2[0].getG(), gColour2[0].getB(), gColour2[0].getA());
                        st.forceFocus();
                    }
                }
            }
        });

        widgetUtil(btnInlineLinkedPtr[0]).addSelectionListener(e -> {
            CompositeTab selection = (CompositeTab) tabFolderPtr[0].getSelection();
            if (selection != null && !selection.getState().getFileNameObj().isReadOnly()) {
                if (!selection.getState().getFileNameObj().getVertexManager().isUpdated()){
                    return;
                }
                NLogger.debug(getClass(), "Inlining.."); //$NON-NLS-1$
                final StyledText st = selection.getTextComposite();
                int s1 = st.getSelectionRange().x;
                int s2 = s1 + st.getSelectionRange().y;
                int fromLine = s1 > -1 ? st.getLineAtOffset(s1) : s1 * -1;
                int toLine = s2 > -1 ? st.getLineAtOffset(s2) : s2 * -1;
                fromLine++;
                toLine++;
                Inliner.withSubfileReference = true;
                Inliner.recursively = false;
                Inliner.noComment = false;
                NLogger.debug(getClass(), "From line {0}", fromLine); //$NON-NLS-1$
                NLogger.debug(getClass(), "To   line {0}", toLine); //$NON-NLS-1$
                Inliner.inline(st, fromLine, toLine, selection.getState().getFileNameObj());
                st.forceFocus();
            }
        });
        widgetUtil(btnBFCswapPtr[0]).addSelectionListener(e -> {

            CompositeTab selection = (CompositeTab) tabFolderPtr[0].getSelection();
            if (selection != null) {
                if (!selection.getState().getFileNameObj().getVertexManager().isUpdated()){
                    return;
                }
                NLogger.debug(getClass(), "Inlining.."); //$NON-NLS-1$
                final StyledText st = selection.getTextComposite();
                int s1 = st.getSelectionRange().x;
                int s2 = s1 + st.getSelectionRange().y;
                int fromLine = s1 > -1 ? st.getLineAtOffset(s1) : s1 * -1;
                int toLine = s2 > -1 ? st.getLineAtOffset(s2) : s2 * -1;
                fromLine++;
                toLine++;
                Inliner.withSubfileReference = true;
                Inliner.recursively = false;
                Inliner.noComment = false;
                NLogger.debug(getClass(), "From line {0}", fromLine); //$NON-NLS-1$
                NLogger.debug(getClass(), "To   line {0}", toLine); //$NON-NLS-1$
                BFCswapper.swap(fromLine, toLine, selection.getState().getFileNameObj());
                st.forceFocus();
            }
        });
        widgetUtil(btnCompileSubfilePtr[0]).addSelectionListener(e -> {
            CompositeTab selection = (CompositeTab) tabFolderPtr[0].getSelection();
            if (selection != null) {
                if (!selection.getState().getFileNameObj().getVertexManager().isUpdated()){
                    return;
                }
                SubfileCompiler.compile(selection.getState().getFileNameObj(), false, false);
                final StyledText st = selection.getTextComposite();
                st.forceFocus();
            }
        });
        widgetUtil(btnRoundSelectionPtr[0]).addSelectionListener(e -> {
            CompositeTab selection = (CompositeTab) tabFolderPtr[0].getSelection();
            if (selection != null && !selection.getState().getFileNameObj().isReadOnly()) {
                if (!selection.getState().getFileNameObj().getVertexManager().isUpdated()){
                    return;
                }
                if (Cocoa.checkCtrlOrCmdPressed(e.stateMask) && new RoundDialog(btnRoundSelectionPtr[0].getShell()).open() == IDialogConstants.CANCEL_ID) {
                    return;
                }
                NLogger.debug(getClass(), "Rounding.."); //$NON-NLS-1$
                final StyledText st = selection.getTextComposite();
                int s1 = st.getSelectionRange().x;
                int s2 = s1 + st.getSelectionRange().y;
                int fromLine = s1 > -1 ? st.getLineAtOffset(s1) : s1 * -1;
                int toLine = s2 > -1 ? st.getLineAtOffset(s2) : s2 * -1;
                fromLine++;
                toLine++;
                NLogger.debug(getClass(), "From line {0}", fromLine); //$NON-NLS-1$
                NLogger.debug(getClass(), "To   line {0}", toLine); //$NON-NLS-1$
                Rounder.round(selection.getState(), fromLine, toLine, selection.getState().getFileNameObj());
                st.forceFocus();
            }
        });

        widgetUtil(btnShowErrorsPtr[0]).addSelectionListener(e -> {
            CompositeTab selection = (CompositeTab) tabFolderPtr[0].getSelection();
            if (selection != null) {
                selection.toggleErrorTabVisibility();
            }
        });

        widgetUtil(btnHidePtr[0]).addSelectionListener(e -> {
            CompositeTab selectedTab = (CompositeTab) tabFolderPtr[0].getSelection();
            if (selectedTab != null) {
                selectedTab.hideSelection();
            }
        });

        widgetUtil(btnShowPtr[0]).addSelectionListener(e -> {
            CompositeTab selectedTab = (CompositeTab) tabFolderPtr[0].getSelection();
            if (selectedTab != null) {
                selectedTab.showSelection();
            }
        });

        tabFolderPtr[0].addCTabFolder2Listener(new CTabFolder2Listener() {

            @Override
            public void showList(CTabFolderEvent event) {
                // Implementation is not required.
            }

            @Override
            public void restore(CTabFolderEvent event) {
                // Implementation is not required.
            }

            @Override
            public void minimize(CTabFolderEvent event) {
                // Implementation is not required.
            }

            @Override
            public void maximize(CTabFolderEvent event) {
                // Implementation is not required.
            }

            @Override
            public void close(CTabFolderEvent event) {
                CompositeTab ct = (CompositeTab) event.item;
                final DatFile df = ct.getState().getFileNameObj();
                ct.getTextComposite().getParent().getParent().getParent().dispose();
                ct.dispose();
                SearchWindow sw = Editor3DWindow.getWindow().getSearchWindow();
                if (sw != null) {
                    sw.setTextComposite(null);
                    sw.setScopeToAll();
                }
                if (!WorkbenchManager.getUserSettingState().hasSeparateTextWindow()) {
                    Editor3DWindow.getWindow().closeTabWithDatFile(df);
                }
            }
        });
        tabFolderPtr[0].addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                EditorTextWindow.draggedTabOrigin = (CompositeTab) tabFolderPtr[0].getItem(new Point(e.x, e.y));
                EditorTextWindow.dragFolderOrigin = tabFolderPtr[0];
            }
        });
        widgetUtil(tabFolderPtr[0]).addSelectionListener(e -> ((CompositeTab) tabFolderPtr[0].getSelection()).getTextComposite().forceFocus());
        int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;

        final DragSource source = new DragSource(tabFolderPtr[0], operations);
        widgetUtil(source).setTransfer(TextTabDragAndDropTransfer.getInstance());
        source.addDragListener(new DragSourceListener() {
            @Override
            public void dragStart(DragSourceEvent event) {
                event.doit = EditorTextWindow.draggedTabOrigin != null;
            }

            @Override
            public void dragSetData(DragSourceEvent event) {
                event.data = new TextTabDragAndDropType();
            }

            @Override
            public void dragFinished(DragSourceEvent event) {
                // Implementation is not required.
            }
        });

        DropTarget target = new DropTarget(tabFolderPtr[0], operations);
        widgetUtil(target).setTransfer(TextTabDragAndDropTransfer.getInstance(), FileTransfer.getInstance());
        target.addDropListener(new DropTargetAdapter() {
            @Override
            public void dragOver(DropTargetEvent event) {
                Point dpos = tabFolderPtr[0].toDisplay(1, 1);
                Point pos = new Point(event.x - dpos.x, event.y - dpos.y);
                CompositeTab item = (CompositeTab) tabFolderPtr[0].getItem(pos);
                EditorTextWindow.draggedTabTarget = item;
                EditorTextWindow.dragFolderTarget = tabFolderPtr[0];
                if (item != null) {
                    tabFolderPtr[0].setSelection(item);
                }
            }

            @Override
            public void drop(DropTargetEvent event) {
                String[] fileList = null;
                FileTransfer ft = FileTransfer.getInstance();
                if (ft.isSupportedType(event.currentDataType)) {
                    fileList = (String[]) event.data;
                    if (fileList != null) {
                        for (String f : fileList) {
                            NLogger.debug(getClass(), f);
                            if (f.toLowerCase(Locale.ENGLISH).endsWith(".dat")) { //$NON-NLS-1$
                                final File fileToOpen = new File(f);
                                if (!fileToOpen.exists() || fileToOpen.isDirectory()) continue;

                                if (WorkbenchManager.getUserSettingState().isSyncingTabs()) {
                                    DatFile df = Editor3DWindow.getWindow().openDatFile(OpenInWhat.EDITOR_3D, f, true);
                                    if (df != null) {
                                        NewOpenSaveProjectToolItem.addRecentFile(df);
                                        final File f2 = new File(df.getNewName());
                                        if (f2.getParentFile() != null) {
                                            Project.setLastVisitedPath(f2.getParentFile().getAbsolutePath());
                                        }
                                        Editor3DWindow.getWindow().openDatFile(df, OpenInWhat.EDITOR_TEXT, tabFolderPtr[0].getWindow());
                                    }
                                } else {
                                    DatFile df = Editor3DWindow.getWindow().openDatFile(OpenInWhat.EDITOR_TEXT, f, true);
                                    if (df != null) {
                                        for (EditorTextWindow w : Project.getOpenTextWindows()) {
                                            for (CTabItem t : w.getTabFolder().getItems()) {
                                                if (df.equals(((CompositeTab) t).getState().getFileNameObj())) {
                                                    w.getTabFolder().setSelection(t);
                                                    ((CompositeTab) t).getControl().getShell().forceActive();
                                                    if (w.isSeperateWindow()) {
                                                        w.open();
                                                    }
                                                    df.getVertexManager().setUpdated(true);
                                                }
                                            }
                                        }
                                        df.getVertexManager().addSnapshot();
                                    }
                                }
                                Editor3DWindow.getWindow().updateTreeUnsavedEntries();
                                return;
                            }
                        }
                    }
                }
                if (EditorTextWindow.draggedTabOrigin != null && !EditorTextWindow.draggedTabOrigin.equals(EditorTextWindow.draggedTabTarget)) {
                    if (!EditorTextWindow.draggedTabOrigin.getState().getFileNameObj().getVertexManager().isUpdated()) return;
                    if (EditorTextWindow.draggedTabTarget != null && !EditorTextWindow.draggedTabTarget.getState().getFileNameObj().getVertexManager().isUpdated()) return;
                    int index = 0;
                    if (EditorTextWindow.draggedTabTarget != null) {
                        for (CTabItem t : EditorTextWindow.dragFolderTarget.getItems()) {
                            CompositeTab tab = (CompositeTab) t;
                            if (tab.equals(EditorTextWindow.draggedTabTarget))
                                break;
                            index++;
                        }
                        if (index != 0)
                            index++;
                        if (EditorTextWindow.dragFolderOrigin.equals(EditorTextWindow.dragFolderTarget)) {
                            int index2 = 0;
                            for (CTabItem t : EditorTextWindow.dragFolderOrigin.getItems()) {
                                CompositeTab tab = (CompositeTab) t;
                                if (tab.equals(EditorTextWindow.draggedTabOrigin))
                                    break;
                                index2++;
                            }
                            if (index2 >= index && index > 0) {
                                index--;
                            }
                        }
                    }

                    EditorTextWindow.dragFolderTarget.setSelection(EditorTextWindow.draggedTabOrigin.moveToFolder(EditorTextWindow.dragFolderTarget, index));
                    ((CompositeTab) tabFolderPtr[0].getSelection()).getTextComposite().forceFocus();
                }
            }
        });
    }

    public boolean saveAs(DatFile dfToSave, String name, String filePath) {
        FileDialog fd = new FileDialog(btnSaveAsPtr[0].getShell(), SWT.SAVE);
        fd.setText(I18n.E3D_SAVE_DAT_FILE_AS);
        fd.setOverwrite(true);

        {
            File f = new File(dfToSave.getNewName()).getParentFile();
            if (f != null && f.exists()) {
                fd.setFilterPath(f.getAbsolutePath());
            } else {
                fd.setFilterPath(Project.getLastVisitedPath());
            }
        }

        if (name != null) {
            fd.setFileName(name);
        }

        String[] filterExt = { "*.dat", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
        fd.setFilterExtensions(filterExt);
        String[] filterNames = {I18n.E3D_LDRAW_SOURCE_FILE, I18n.E3D_ALL_FILES};
        fd.setFilterNames(filterNames);

        while (true) {
            try {
                String selected;
                if (filePath == null) {
                    selected = fd.open();
                } else {
                    selected = filePath;
                }
                if (selected != null) {

                    if (Editor3DWindow.getWindow().isFileNameAllocated(selected, new DatFile(selected), true)) {
                        MessageBox messageBox = new MessageBox(btnSaveAsPtr[0].getShell(), SWT.ICON_ERROR | SWT.RETRY | SWT.CANCEL);
                        messageBox.setText(I18n.DIALOG_ALREADY_ALLOCATED_NAME_TITLE);
                        messageBox.setMessage(I18n.DIALOG_ALREADY_ALLOCATED_NAME);

                        int result = messageBox.open();

                        if (result == SWT.CANCEL) {
                            break;
                        } else if (result == SWT.RETRY) {
                            filePath = null;
                            continue;
                        }
                    }

                    SearchWindow sw = Editor3DWindow.getWindow().getSearchWindow();
                    if (sw != null) {
                        sw.setTextComposite(null);
                        sw.setScopeToAll();
                    }

                    dfToSave.saveAs(selected);

                    if (WorkbenchManager.getUserSettingState().isSyncingTabs()) {
                        DatFile df = Editor3DWindow.getWindow().openDatFile(OpenInWhat.EDITOR_3D, selected, false);
                        if (df != null) {
                            NewOpenSaveProjectToolItem.addRecentFile(df);
                            final File f = new File(df.getNewName());
                            if (f.getParentFile() != null) {
                                Project.setLastVisitedPath(f.getParentFile().getAbsolutePath());
                            }
                            Editor3DWindow.getWindow().openDatFile(df, OpenInWhat.EDITOR_TEXT, editorTextWindow);
                        }
                    } else {
                        DatFile df = Editor3DWindow.getWindow().openDatFile(OpenInWhat.EDITOR_TEXT, selected, false);
                        if (df != null) {
                            for (EditorTextWindow w : Project.getOpenTextWindows()) {
                                for (CTabItem t : w.getTabFolder().getItems()) {
                                    if (df.equals(((CompositeTab) t).getState().getFileNameObj())) {
                                        w.getTabFolder().setSelection(t);
                                        ((CompositeTab) t).getControl().getShell().forceActive();
                                        if (w.isSeperateWindow()) {
                                            w.open();
                                        }
                                        df.getVertexManager().setUpdated(true);
                                    }
                                }
                            }
                            df.getVertexManager().addSnapshot();
                        }
                    }
                    Editor3DWindow.getWindow().cleanupClosedData();
                    Editor3DWindow.getWindow().updateTreeUnsavedEntries();
                    return true;
                }
            } catch (Exception ex) {
                NLogger.error(getClass(), ex);
            }
            break;
        }
        return false;
    }

    public static void openIn3D(DatFile df) {
        List<OpenGLRenderer> renders = Editor3DWindow.getRenders();

        if (renders.isEmpty()) {

            if ("%EMPTY%".equals(Editor3DWindow.getSashForm().getChildren()[1].getData())) { //$NON-NLS-1$
                int[] mainSashWeights = Editor3DWindow.getSashForm().getWeights();
                Editor3DWindow.getSashForm().getChildren()[1].dispose();
                CompositeContainer cmpContainer = new CompositeContainer(Editor3DWindow.getSashForm(), false);
                cmpContainer.moveBelow(Editor3DWindow.getSashForm().getChildren()[0]);
                df.parseForData(true);
                Project.setFileToEdit(df);
                cmpContainer.getComposite3D().setLockableDatFileReference(df);
                df.getVertexManager().addSnapshot();
                Editor3DWindow.getSashForm().getParent().layout();
                Editor3DWindow.getSashForm().setWeights(mainSashWeights);
            }

        } else {

            boolean canUpdate = false;

            for (OpenGLRenderer renderer : renders) {
                Composite3D c3d = renderer.getC3D();
                if (!c3d.isDatFileLockedOnDisplay()) {
                    canUpdate = true;
                    break;
                }
            }

            if (!canUpdate) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    c3d.getModifier().switchLockedDat(false);
                }
            }

            final VertexManager vm = df.getVertexManager();
            if (vm.isModified()) {
                df.setText(df.getText());
            }
            df.parseForData(true);

            Project.setFileToEdit(df);
            for (OpenGLRenderer renderer : renders) {
                Composite3D c3d = renderer.getC3D();
                if (!c3d.isDatFileLockedOnDisplay()) {
                    boolean hasState = Editor3DWindow.getWindow().hasState(df, c3d);
                    c3d.setLockableDatFileReference(df);
                    if (!hasState) c3d.getModifier().zoomToFit();
                }
            }

            df.getVertexManager().addSnapshot();

            if (!canUpdate) {
                for (OpenGLRenderer renderer : renders) {
                    Composite3D c3d = renderer.getC3D();
                    c3d.getModifier().switchLockedDat(true);
                }
            }
        }
        Editor3DWindow.getWindow().updateTreeSelectedDatFile(df);
    }
}
