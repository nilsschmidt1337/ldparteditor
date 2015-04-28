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
package org.nschmidt.ldparteditor.shells.editortext;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder2Listener;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.composites.compositetab.CompositeTab;
import org.nschmidt.ldparteditor.composites.compositetab.CompositeTabFolder;
import org.nschmidt.ldparteditor.composites.compositetab.CompositeTabState;
import org.nschmidt.ldparteditor.data.Beautifier;
import org.nschmidt.ldparteditor.data.ColourChanger;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.QuadSplitter;
import org.nschmidt.ldparteditor.data.Rounder;
import org.nschmidt.ldparteditor.data.Unrectifier;
import org.nschmidt.ldparteditor.dialogs.colour.ColourDialog;
import org.nschmidt.ldparteditor.dialogs.round.RoundDialog;
import org.nschmidt.ldparteditor.dialogs.sort.SortDialog;
import org.nschmidt.ldparteditor.dnd.MyDummyTransfer;
import org.nschmidt.ldparteditor.dnd.MyDummyType;
import org.nschmidt.ldparteditor.enums.OpenInWhat;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.ShellHelper;
import org.nschmidt.ldparteditor.helpers.Version;
import org.nschmidt.ldparteditor.helpers.compositetext.Annotator;
import org.nschmidt.ldparteditor.helpers.compositetext.AnnotatorTexmap;
import org.nschmidt.ldparteditor.helpers.compositetext.Inliner;
import org.nschmidt.ldparteditor.helpers.compositetext.SubfileCompiler;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.resources.ResourceManager;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.shells.searchnreplace.SearchWindow;
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

    /**
     * Create the application window.
     */
    public EditorTextWindow() {
        super();
        super.setEditorTextWindow(this);
    }

    /**
     * Run a fresh instance of this window
     */
    public void run(DatFile fileToOpen) {
        Project.getOpenTextWindows().add(this);
        // Load the window state data
        this.editorTextWindowState = WorkbenchManager.getEditorTextWindowState();
        // Creating the window to get the shell
        this.create();
        final Shell sh = this.getShell();
        sh.setText(Version.getApplicationName());
        sh.setImage(ResourceManager.getImage("imgDuke2.png")); //$NON-NLS-1$
        sh.setMinimumSize(640, 480);
        sh.setBounds(this.editorTextWindowState.getWindowState().getSizeAndPosition());
        if (this.editorTextWindowState.getWindowState().isCentered()) {
            ShellHelper.centerShellOnPrimaryScreen(sh);
        }
        // Maximize has to be called asynchronously
        sh.getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                sh.setMaximized(editorTextWindowState.getWindowState().isMaximized());
                sh.forceActive();
            }
        });
        // The window reference have to be added to the tab folder
        tabFolder[0].setWindow(this);
        // and the tab for the file has to be created.
        {
            CompositeTab tbtmnewItem = new CompositeTab(tabFolder[0], SWT.CLOSE);
            tbtmnewItem.setWindow(editorTextWindow);
            tbtmnewItem.getState().setFileNameObj(fileToOpen);
            tbtmnewItem.parseForErrorAndHints();
            tabFolder[0].setSelection(tbtmnewItem);
        }

        // MARK All final listeners will be configured here..
        // First, create all menu actions.
        createActions();
        tabFolder[0].addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                // TODO Implement loader here for the file data itself
                if (e.item != null)
                    NLogger.debug(EditorTextWindow.class, ((CompositeTab) e.item).getState().getFileNameObj().getOldName());
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent consumed) {
            }
        });
        btn_New[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DatFile df = Editor3DWindow.getWindow().createNewDatFile(getShell(), OpenInWhat.EDITOR_3D);
                if (df != null && !Editor3DWindow.getWindow().openDatFile(df, OpenInWhat.EDITOR_TEXT, editorTextWindow)) {
                    {
                        CompositeTab tbtmnewItem = new CompositeTab(tabFolder[0], SWT.CLOSE);
                        tbtmnewItem.setWindow(editorTextWindow);
                        tbtmnewItem.getState().setFileNameObj(df);
                        tabFolder[0].setSelection(tbtmnewItem);
                    }
                }
            }
        });
        btn_Open[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DatFile df = Editor3DWindow.getWindow().openDatFile(getShell(), OpenInWhat.EDITOR_3D, null);
                if (df != null) {
                    Editor3DWindow.getWindow().addRecentFile(df);
                    if (!Editor3DWindow.getWindow().openDatFile(df, OpenInWhat.EDITOR_TEXT, editorTextWindow)) {
                        {
                            CompositeTab tbtmnewItem = new CompositeTab(tabFolder[0], SWT.CLOSE);
                            tbtmnewItem.setWindow(editorTextWindow);
                            tbtmnewItem.getState().setFileNameObj(df);
                            tabFolder[0].setSelection(tbtmnewItem);
                        }
                    }
                }
            }
        });
        btn_Save[0].addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (tabFolder[0].getSelection() != null) {
                    CompositeTabState state = ((CompositeTab) tabFolder[0].getSelection()).getState();
                    DatFile df = state.getFileNameObj();
                    Editor3DWindow.getWindow().addRecentFile(df);
                    if (!df.isReadOnly() && Project.getUnsavedFiles().contains(df)) {
                        if (df.save()) {
                            Editor3DWindow.getWindow().addRecentFile(df);
                            Editor3DWindow.getWindow().updateTree_unsavedEntries();
                            ((CompositeTab) tabFolder[0].getSelection()).getTextComposite().setText(state.getFileNameObj().getText());
                        } else {
                            MessageBox messageBoxError = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                            messageBoxError.setText(I18n.DIALOG_Error);
                            messageBoxError.setMessage(I18n.DIALOG_CantSaveFile);
                        }
                    }
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        btn_Cut[0].addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CompositeTab selection = (CompositeTab) tabFolder[0].getSelection();
                if (selection != null && !selection.getState().getFileNameObj().isReadOnly()) {
                    if (!selection.getState().getFileNameObj().getVertexManager().isUpdated()){
                        selection.getTextComposite().copy();
                        return;
                    }
                    selection.getTextComposite().cut();
                    selection.getTextComposite().forceFocus();
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        btn_Copy[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CompositeTab selection = (CompositeTab) tabFolder[0].getSelection();
                if (selection != null) {
                    selection.getTextComposite().copy();
                    selection.getTextComposite().forceFocus();
                }
            }
        });
        btn_Paste[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CompositeTab selection = (CompositeTab) tabFolder[0].getSelection();
                if (selection != null && !selection.getState().getFileNameObj().isReadOnly()) {
                    if (!selection.getState().getFileNameObj().getVertexManager().isUpdated()){
                        return;
                    }
                    selection.getTextComposite().paste();
                    selection.getTextComposite().forceFocus();
                }
            }
        });
        btn_Delete[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CompositeTab selection = (CompositeTab) tabFolder[0].getSelection();
                if (selection != null && !selection.getState().getFileNameObj().isReadOnly()) {
                    if (!selection.getState().getFileNameObj().getVertexManager().isUpdated()){
                        return;
                    }
                    int x = selection.getTextComposite().getSelection().x;
                    selection.getTextComposite().insert(""); //$NON-NLS-1$
                    selection.getTextComposite().setSelection(new Point(x, x));
                    selection.getTextComposite().forceFocus();
                }
            }
        });

        btn_Undo[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CompositeTab selection = (CompositeTab) tabFolder[0].getSelection();
                if (selection != null && !selection.getState().getFileNameObj().isReadOnly()) {
                    if (!selection.getState().getFileNameObj().getVertexManager().isUpdated()){
                        return;
                    }
                    selection.getState().getFileNameObj().undo();
                }
            }
        });

        btn_Redo[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CompositeTab selection = (CompositeTab) tabFolder[0].getSelection();
                if (selection != null && !selection.getState().getFileNameObj().isReadOnly()) {
                    if (!selection.getState().getFileNameObj().getVertexManager().isUpdated()){
                        return;
                    }
                    selection.getState().getFileNameObj().redo();
                }
            }
        });

        if (NLogger.DEBUG) {
            btn_AddHistory[0].addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    CompositeTab selection = (CompositeTab) tabFolder[0].getSelection();
                    if (selection != null && !selection.getState().getFileNameObj().isReadOnly()) {
                        if (!selection.getState().getFileNameObj().getVertexManager().isUpdated()){
                            return;
                        }
                        selection.getState().getFileNameObj().addHistory();
                    }
                }
            });
        }

        btn_Inline[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CompositeTab selection = (CompositeTab) tabFolder[0].getSelection();
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
                    NLogger.debug(getClass(), "From line " + fromLine); //$NON-NLS-1$
                    NLogger.debug(getClass(), "To   line " + toLine); //$NON-NLS-1$
                    Inliner.inline(st, fromLine, toLine, selection.getState().getFileNameObj());
                    st.forceFocus();
                }
            }
        });

        btn_InlineDeep[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CompositeTab selection = (CompositeTab) tabFolder[0].getSelection();
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
                    NLogger.debug(getClass(), "From line " + fromLine); //$NON-NLS-1$
                    NLogger.debug(getClass(), "To   line " + toLine); //$NON-NLS-1$
                    Inliner.inline(st, fromLine, toLine, selection.getState().getFileNameObj());
                    st.forceFocus();
                }
            }
        });

        btn_Annotate[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CompositeTab selection = (CompositeTab) tabFolder[0].getSelection();
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
                    fromLine++;
                    toLine++;
                    NLogger.debug(getClass(), "From line " + fromLine); //$NON-NLS-1$
                    NLogger.debug(getClass(), "To   line " + toLine); //$NON-NLS-1$
                    Annotator.annotate(st, fromLine, toLine, selection.getState().getFileNameObj());
                    st.forceFocus();
                }
            }
        });

        btn_Texmap[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CompositeTab selection = (CompositeTab) tabFolder[0].getSelection();
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
                    fromLine++;
                    toLine++;
                    NLogger.debug(getClass(), "From line " + fromLine); //$NON-NLS-1$
                    NLogger.debug(getClass(), "To   line " + toLine); //$NON-NLS-1$
                    AnnotatorTexmap.annotate(st, fromLine, toLine, selection.getState().getFileNameObj());
                    st.forceFocus();
                }
            }
        });


        btn_FindAndReplace[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CompositeTab selection = (CompositeTab) tabFolder[0].getSelection();
                if (selection != null) {
                    SearchWindow win = Editor3DWindow.getWindow().getSearchWindow();
                    if (win != null) {
                        win.close();
                    }
                    Editor3DWindow.getWindow().setSearchWindow(new SearchWindow(getShell()));
                    Editor3DWindow.getWindow().getSearchWindow().run();
                    Editor3DWindow.getWindow().getSearchWindow().setTextComposite(selection);
                    Editor3DWindow.getWindow().getSearchWindow().setScopeToAll();
                }
            }
        });

        btn_Beautify[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CompositeTab selection = (CompositeTab) tabFolder[0].getSelection();
                if (selection != null && !selection.getState().getFileNameObj().isReadOnly()) {
                    if (!selection.getState().getFileNameObj().getVertexManager().isUpdated()){
                        return;
                    }
                    final StyledText st = selection.getTextComposite();
                    Beautifier.beautify(st, selection.getState().getFileNameObj());
                    st.forceFocus();
                }
            }
        });

        btn_Sort[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CompositeTab selection = (CompositeTab) tabFolder[0].getSelection();
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
                    NLogger.debug(getClass(), "From line " + fromLine); //$NON-NLS-1$
                    NLogger.debug(getClass(), "To   line " + toLine); //$NON-NLS-1$
                    final SortDialog sd = new SortDialog(getShell(), st, fromLine, toLine, selection.getState().getFileNameObj());
                    sd.open();
                    st.forceFocus();
                }
            }
        });

        btn_SplitQuad[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CompositeTab selection = (CompositeTab) tabFolder[0].getSelection();
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
                    NLogger.debug(getClass(), "From line " + fromLine); //$NON-NLS-1$
                    NLogger.debug(getClass(), "To   line " + toLine); //$NON-NLS-1$
                    QuadSplitter.splitQuadsIntoTriangles(st, fromLine, toLine, selection.getState().getFileNameObj());
                    st.forceFocus();
                }
            }
        });

        btn_Unrectify[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CompositeTab selection = (CompositeTab) tabFolder[0].getSelection();
                if (selection != null && !selection.getState().getFileNameObj().isReadOnly()) {
                    if (!selection.getState().getFileNameObj().getVertexManager().isUpdated()){
                        return;
                    }
                    NLogger.debug(getClass(), "Unrectify.."); //$NON-NLS-1$
                    final StyledText st = selection.getTextComposite();
                    Unrectifier.splitAllIntoTriangles(st, selection.getState().getFileNameObj());
                    st.forceFocus();
                }
            }
        });

        btn_Palette[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                final GColour[] gColour2 = new GColour[1];
                new ColourDialog(getShell(), gColour2).open();
                if (gColour2[0] != null) {
                    int num = gColour2[0].getColourNumber();
                    if (!View.hasLDConfigColour(num)) {
                        num = -1;
                    }

                    CompositeTab selection = (CompositeTab) tabFolder[0].getSelection();
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
                            NLogger.debug(getClass(), "From line " + fromLine); //$NON-NLS-1$
                            NLogger.debug(getClass(), "To   line " + toLine); //$NON-NLS-1$
                            ColourChanger.changeColour(st, fromLine, toLine, df, num, gColour2[0].getR(), gColour2[0].getG(), gColour2[0].getB());
                            st.forceFocus();
                        }
                    }
                }
            }
        });

        btn_InlineLinked[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CompositeTab selection = (CompositeTab) tabFolder[0].getSelection();
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
                    NLogger.debug(getClass(), "From line " + fromLine); //$NON-NLS-1$
                    NLogger.debug(getClass(), "To   line " + toLine); //$NON-NLS-1$
                    Inliner.inline(st, fromLine, toLine, selection.getState().getFileNameObj());
                    st.forceFocus();
                }
            }
        });
        btn_CompileSubfile[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CompositeTab selection = (CompositeTab) tabFolder[0].getSelection();
                if (selection != null) {
                    if (!selection.getState().getFileNameObj().getVertexManager().isUpdated()){
                        return;
                    }
                    SubfileCompiler.compile(selection.getState().getFileNameObj(), false, false);
                    final StyledText st = selection.getTextComposite();
                    st.forceFocus();
                }
            }
        });
        btn_RoundSelection[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CompositeTab selection = (CompositeTab) tabFolder[0].getSelection();
                if (selection != null && !selection.getState().getFileNameObj().isReadOnly()) {
                    if (!selection.getState().getFileNameObj().getVertexManager().isUpdated()){
                        return;
                    }
                    if ((e.stateMask & SWT.CTRL) == SWT.CTRL) {
                        if (new RoundDialog(getShell()).open() == IDialogConstants.CANCEL_ID) return;
                    }
                    NLogger.debug(getClass(), "Rounding.."); //$NON-NLS-1$
                    final StyledText st = selection.getTextComposite();
                    int s1 = st.getSelectionRange().x;
                    int s2 = s1 + st.getSelectionRange().y;
                    int fromLine = s1 > -1 ? st.getLineAtOffset(s1) : s1 * -1;
                    int toLine = s2 > -1 ? st.getLineAtOffset(s2) : s2 * -1;
                    fromLine++;
                    toLine++;
                    NLogger.debug(getClass(), "From line " + fromLine); //$NON-NLS-1$
                    NLogger.debug(getClass(), "To   line " + toLine); //$NON-NLS-1$
                    Rounder.round(selection.getState(), st, fromLine, toLine, selection.getState().getFileNameObj());
                    st.forceFocus();
                }
            }
        });

        btn_ShowErrors[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CompositeTab selection = (CompositeTab) tabFolder[0].getSelection();
                if (selection != null) {
                    selection.toggleErrorTabVisibility();
                }
            }
        });

        tabFolder[0].addCTabFolder2Listener(new CTabFolder2Listener() {

            @Override
            public void showList(CTabFolderEvent event) {}

            @Override
            public void restore(CTabFolderEvent event) {}

            @Override
            public void minimize(CTabFolderEvent event) {}

            @Override
            public void maximize(CTabFolderEvent event) {}

            @Override
            public void close(CTabFolderEvent event) {
                CompositeTab ct = (CompositeTab) event.item;
                ct.getTextComposite().getParent().getParent().getParent().dispose();
                ct.dispose();
                SearchWindow sw = Editor3DWindow.getWindow().getSearchWindow();
                if (sw != null) {
                    sw.setTextComposite(null);
                    sw.setScopeToAll();
                }
            }
        });
        tabFolder[0].addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                EditorTextWindow.draggedTabOrigin = (CompositeTab) tabFolder[0].getItem(new Point(e.x, e.y));
                EditorTextWindow.dragFolderOrigin = tabFolder[0];
            }
        });
        tabFolder[0].addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                ((CompositeTab) tabFolder[0].getSelection()).getTextComposite().forceFocus();
            }
        });
        Transfer[] types = new Transfer[] { MyDummyTransfer.getInstance() };
        int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;

        final DragSource source = new DragSource(tabFolder[0], operations);
        source.setTransfer(types);
        source.addDragListener(new DragSourceListener() {
            @Override
            public void dragStart(DragSourceEvent event) {
                event.doit = EditorTextWindow.draggedTabOrigin != null;
            }

            @Override
            public void dragSetData(DragSourceEvent event) {
                event.data = new MyDummyType();
            }

            @Override
            public void dragFinished(DragSourceEvent event) {

            }
        });

        DropTarget target = new DropTarget(tabFolder[0], operations);
        target.setTransfer(types);
        target.addDropListener(new DropTargetAdapter() {
            @Override
            public void dragOver(DropTargetEvent event) {
                Point dpos = tabFolder[0].toDisplay(1, 1);
                Point pos = new Point(event.x - dpos.x, event.y - dpos.y);
                CompositeTab item = (CompositeTab) tabFolder[0].getItem(pos);
                EditorTextWindow.draggedTabTarget = item;
                EditorTextWindow.dragFolderTarget = tabFolder[0];
                if (item != null) {
                    tabFolder[0].setSelection(item);
                }
            }

            @Override
            public void drop(DropTargetEvent event) {
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
                    ((CompositeTab) tabFolder[0].getSelection()).getTextComposite().forceFocus();
                }
            }
        });
        this.open();
    }

    /**
     * Create the actions.
     */
    private void createActions() {

    }

    /**
     * The Shell-Close-Event
     */
    @Override
    protected void handleShellCloseEvent() {

        // Unsaved changes dialog only for files which are out of the scope of the current project..

        Set<DatFile> unsavedFiles = new HashSet<DatFile>(Project.getUnsavedFiles());
        {
            @SuppressWarnings("unchecked")
            ArrayList<DatFile> ta = (ArrayList<DatFile>) Editor3DWindow.getWindow().getProjectParts().getData();
            for (DatFile ti : ta) {
                unsavedFiles.remove(ti);
            }
        }
        {
            @SuppressWarnings("unchecked")
            ArrayList<DatFile> ta = (ArrayList<DatFile>) Editor3DWindow.getWindow().getProjectSubparts().getData();
            for (DatFile ti : ta) {
                unsavedFiles.remove(ti);
            }
        }
        {
            @SuppressWarnings("unchecked")
            ArrayList<DatFile> ta = (ArrayList<DatFile>) Editor3DWindow.getWindow().getProjectPrimitives().getData();
            for (DatFile ti : ta) {
                unsavedFiles.remove(ti);
            }
        }
        {
            @SuppressWarnings("unchecked")
            ArrayList<DatFile> ta = (ArrayList<DatFile>) Editor3DWindow.getWindow().getProjectPrimitives48().getData();
            for (DatFile ti : ta) {
                unsavedFiles.remove(ti);
            }
        }
        {
            @SuppressWarnings("unchecked")
            ArrayList<DatFile> ta = (ArrayList<DatFile>) Editor3DWindow.getWindow().getProjectPrimitives8().getData();
            for (DatFile ti : ta) {
                unsavedFiles.remove(ti);
            }
        }
        {
            @SuppressWarnings("unchecked")
            ArrayList<DatFile> ta = (ArrayList<DatFile>) Editor3DWindow.getWindow().getUnofficialParts().getData();
            for (DatFile ti : ta) {
                unsavedFiles.remove(ti);
            }
        }
        {
            @SuppressWarnings("unchecked")
            ArrayList<DatFile> ta = (ArrayList<DatFile>) Editor3DWindow.getWindow().getUnofficialSubparts().getData();
            for (DatFile ti : ta) {
                unsavedFiles.remove(ti);
            }
        }
        {
            @SuppressWarnings("unchecked")
            ArrayList<DatFile> ta = (ArrayList<DatFile>) Editor3DWindow.getWindow().getUnofficialPrimitives().getData();
            for (DatFile ti : ta) {
                unsavedFiles.remove(ti);
            }
        }
        {
            @SuppressWarnings("unchecked")
            ArrayList<DatFile> ta = (ArrayList<DatFile>) Editor3DWindow.getWindow().getUnofficialPrimitives48().getData();
            for (DatFile ti : ta) {
                unsavedFiles.remove(ti);
            }
        }
        {
            @SuppressWarnings("unchecked")
            ArrayList<DatFile> ta = (ArrayList<DatFile>) Editor3DWindow.getWindow().getUnofficialPrimitives8().getData();
            for (DatFile ti : ta) {
                unsavedFiles.remove(ti);
            }
        }
        {
            @SuppressWarnings("unchecked")
            ArrayList<DatFile> ta = (ArrayList<DatFile>) Editor3DWindow.getWindow().getOfficialParts().getData();
            for (DatFile ti : ta) {
                unsavedFiles.remove(ti);
            }
        }
        {
            @SuppressWarnings("unchecked")
            ArrayList<DatFile> ta = (ArrayList<DatFile>) Editor3DWindow.getWindow().getOfficialSubparts().getData();
            for (DatFile ti : ta) {
                unsavedFiles.remove(ti);
            }
        }
        {
            @SuppressWarnings("unchecked")
            ArrayList<DatFile> ta = (ArrayList<DatFile>) Editor3DWindow.getWindow().getOfficialPrimitives().getData();
            for (DatFile ti : ta) {
                unsavedFiles.remove(ti);
            }
        }
        {
            @SuppressWarnings("unchecked")
            ArrayList<DatFile> ta = (ArrayList<DatFile>) Editor3DWindow.getWindow().getOfficialPrimitives48().getData();
            for (DatFile ti : ta) {
                unsavedFiles.remove(ti);
            }
        }
        {
            @SuppressWarnings("unchecked")
            ArrayList<DatFile> ta = (ArrayList<DatFile>) Editor3DWindow.getWindow().getOfficialPrimitives8().getData();
            for (DatFile ti : ta) {
                unsavedFiles.remove(ti);
            }
        }

        for (DatFile df : unsavedFiles) {
            if (!df.getText().equals(df.getOriginalText()) || df.isVirtual() && !df.getText().trim().isEmpty()) {
                MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.CANCEL | SWT.NO);
                messageBox.setText(I18n.DIALOG_UnsavedChangesTitle);

                Object[] messageArguments = {df.getShortName()};
                MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                formatter.setLocale(View.LOCALE);
                formatter.applyPattern(I18n.DIALOG_UnsavedChanges);
                messageBox.setMessage(formatter.format(messageArguments));

                int result = messageBox.open();

                if (result == SWT.NO) {
                    // Skip file
                    Project.removeUnsavedFile(df);
                } if (result == SWT.YES) {
                    if (df.save()) {
                        Editor3DWindow.getWindow().addRecentFile(df);
                        ((CompositeTab) tabFolder[0].getSelection()).getTextComposite().setText(df.getText());
                    } else {
                        MessageBox messageBoxError = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                        messageBoxError.setText(I18n.DIALOG_Error);
                        messageBoxError.setMessage(I18n.DIALOG_CantSaveFile);
                        messageBoxError.open();
                        return;
                    }
                } else if (result == SWT.CANCEL)
                    return;
            }
        }
        Project.getOpenTextWindows().remove(this);
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
        return this.tabFolder[0];
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
        CTabItem[] items =  tabFolder[0].getItems().clone();
        for (CTabItem item : items) {
            CompositeTab cTab = (CompositeTab) item;
            if (cTab.getState().getFileNameObj().equals(e)) {
                item.dispose();
            }
        }
        if (tabFolder[0].getItemCount() == 0) {
            Project.getOpenTextWindows().remove(this);
            close();
        }
    }

    public void updateTabWithDatfile(DatFile e) {
        CTabItem[] items =  tabFolder[0].getItems().clone();
        for (CTabItem item : items) {
            CompositeTab cTab = (CompositeTab) item;
            CompositeTabState state = cTab.getState();
            if (state.getFileNameObj().equals(e)) {
                state.setFileNameObj(e);
            }
        }
    }
}
