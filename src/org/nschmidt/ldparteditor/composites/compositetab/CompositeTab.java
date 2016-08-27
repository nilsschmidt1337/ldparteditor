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
package org.nschmidt.ldparteditor.composites.compositetab;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Locale;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Listener;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GDataCSG;
import org.nschmidt.ldparteditor.data.Rounder;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.dialogs.round.RoundDialog;
import org.nschmidt.ldparteditor.enums.Colour;
import org.nschmidt.ldparteditor.enums.Font;
import org.nschmidt.ldparteditor.enums.OpenInWhat;
import org.nschmidt.ldparteditor.enums.TextTask;
import org.nschmidt.ldparteditor.helpers.composite3d.GuiManager;
import org.nschmidt.ldparteditor.helpers.composite3d.ViewIdleManager;
import org.nschmidt.ldparteditor.helpers.compositetext.Inliner;
import org.nschmidt.ldparteditor.helpers.compositetext.QuickFixer;
import org.nschmidt.ldparteditor.helpers.compositetext.VertexMarker;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.shells.editortext.EditorTextWindow;
import org.nschmidt.ldparteditor.shells.searchnreplace.SearchWindow;
import org.nschmidt.ldparteditor.state.KeyStateManager;
import org.nschmidt.ldparteditor.text.StringHelper;
import org.nschmidt.ldparteditor.text.SyntaxFormatter;
import org.nschmidt.ldparteditor.widgets.TreeItem;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * This is the tab composite which displays the text from a DAT file
 * <p>
 * Note: This class should be instantiated, it defines all listeners and part of
 * the business logic. <b>Unfortunately, this widget has to be disposed if it is
 * moved to another position / window. This leads to the fact that all data has
 * to be externalized within {@linkplain CompositeTabState} </b>
 *
 * @author nils
 *
 */
public class CompositeTab extends CompositeTabDesign {

    final SyntaxFormatter syntaxFormatter = new SyntaxFormatter(compositeText[0]);
    final int caretHeight = compositeText[0].getCaret().getSize().y;

    /** The state of this tab */
    private CompositeTabState state = new CompositeTabState();

    // We need no other private attributes anymore!

    /**
     * Create the tab
     *
     * @param parentFolder
     * @param style
     */
    public CompositeTab(CTabFolder parentFolder, int style) {
        super(parentFolder, style);
        initListeners();
    }

    /**
     * Create the tab on the specified index
     *
     * @param parentFolder
     * @param style
     * @param index
     */
    public CompositeTab(CTabFolder parentFolder, int style, int index) {
        super(parentFolder, style, index);
        initListeners();
    }

    private final void initListeners() {
        // MARK All final listeners will be configured here..
        // Each line should be formatted automagically on text change within a
        // single text line
        this.state.setTab(this);

        {
            DropTarget dt = new DropTarget(compositeText[0], DND.DROP_DEFAULT | DND.DROP_MOVE );
            dt.setTransfer(new Transfer[] { FileTransfer.getInstance() });
            dt.addDropListener(new DropTargetAdapter() {
                @Override
                public void drop(DropTargetEvent event) {
                    String fileList[] = null;
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
                                        DatFile df = Editor3DWindow.getWindow().openDatFile(state.window[0].getShell(), OpenInWhat.EDITOR_3D, f, true);
                                        if (df != null) {
                                            Editor3DWindow.getWindow().addRecentFile(df);
                                            final File f2 = new File(df.getNewName());
                                            if (f2.getParentFile() != null) {
                                                Project.setLastVisitedPath(f2.getParentFile().getAbsolutePath());
                                            }
                                            if (!Editor3DWindow.getWindow().openDatFile(df, OpenInWhat.EDITOR_TEXT, state.window[0])) {
                                                {
                                                    CompositeTab tbtmnewItem = new CompositeTab(state.folder[0], SWT.CLOSE);
                                                    tbtmnewItem.setFolderAndWindow(state.folder[0], state.window[0]);
                                                    tbtmnewItem.getState().setFileNameObj(df);
                                                    state.folder[0].setSelection(tbtmnewItem);
                                                    tbtmnewItem.parseForErrorAndHints();
                                                    tbtmnewItem.getTextComposite().redraw();
                                                }
                                            }
                                        }
                                    } else {
                                        DatFile df = Editor3DWindow.getWindow().openDatFile(state.window[0].getShell(), OpenInWhat.EDITOR_TEXT, f, true);
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
                                        }
                                        Editor3DWindow.getWindow().updateTree_unsavedEntries();
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            });
        }
        compositeText[0].addLineStyleListener(new LineStyleListener() {
            @Override
            public void lineGetStyle(final LineStyleEvent e) {
                // So the line will be formated with the syntax formatter from
                // the CompositeText.
                final DatFile df = state.getFileNameObj();
                final VertexManager vm = df.getVertexManager();
                final GData data = df.getDrawPerLine_NOCLONE().getValue(compositeText[0].getLineAtOffset(e.lineOffset) + 1);
                boolean isSelected = vm.isSyncWithTextEditor() && vm.getSelectedData().contains(data);
                isSelected = isSelected || vm.isSyncWithTextEditor() && GDataCSG.getSelection(df).contains(data);
                syntaxFormatter.format(e,
                        state.getToReplaceX(), state.getToReplaceY(), state.getToReplaceZ(),
                        state.getReplaceEpsilon(), state.isReplacingVertex(), isSelected, GData.CACHE_duplicates.containsKey(data),  df);
            }
        });
        final boolean[] isDelPressed = new boolean[] { false };
        compositeText[0].addVerifyKeyListener(new VerifyKeyListener() {

            @Override
            public void verifyKey(VerifyEvent event) {
                DatFile dat = state.getFileNameObj();
                final VertexManager vm = dat.getVertexManager();
                event.doit = vm.isUpdated();
                isDelPressed[0] = event.keyCode == SWT.DEL;

            }

        });
        compositeText[0].addVerifyListener(new VerifyListener() {

            @Override
            // only POSSIBLE approach to get this working, VerifyKey has NO use!
            public void verifyText(VerifyEvent event) {
                event.doit = true;
                state.setDoingPaste(event.text.length() > 1 && compositeText[0].isFocusControl() && state.getFileNameObj().getVertexManager().isUpdated());
                final DatFile dat = state.getFileNameObj();
                final VertexManager vm = dat.getVertexManager();
                if (vm.getVertexToReplace() != null) {
                    if (!vm.isModified() && state.isReplacingVertex()) {
                        // Replaced vertex manipulation check
                        NLogger.debug(getClass(), "Vertex Manipulation is ACTIVE"); //$NON-NLS-1$

                        event.start = compositeText[0].getSelection().x;
                        event.end = compositeText[0].getSelection().y;

                        final boolean doReplace = event.start != event.end;
                        boolean foundVertexMetacommand = false;

                        final boolean doDelete;
                        if (doReplace) {
                            NLogger.debug(getClass(), "Did a text replace!"); //$NON-NLS-1$
                            doDelete = false;
                        } else {
                            doDelete = event.text.isEmpty();
                            if (doDelete) {
                                NLogger.debug(getClass(), "Did a text deletion!"); //$NON-NLS-1$
                                if (isDelPressed[0])
                                    NLogger.debug(getClass(), "with DEL"); //$NON-NLS-1$

                            }
                        }

                        state.currentCaretPositionLine = compositeText[0].getLineAtOffset(event.start);
                        state.currentCaretPositionChar = compositeText[0].getCaretOffset() - compositeText[0].getOffsetAtLine(state.currentCaretPositionLine);
                        state.currentCaretTopIndex = compositeText[0].getTopIndex();

                        String oldLine = compositeText[0].getLine(state.currentCaretPositionLine);
                        String newLine = oldLine;
                        NLogger.debug(getClass(), "Old Line {0}", oldLine); //$NON-NLS-1$
                        NLogger.debug(getClass(), "Key Char {0}", event.character); //$NON-NLS-1$
                        NLogger.debug(getClass(), "State Mask {0}", event.stateMask); //$NON-NLS-1$
                        NLogger.debug(getClass(), "Key Code {0}", event.keyCode); //$NON-NLS-1$
                        NLogger.debug(getClass(), "Key Location {0}", event.keyLocation); //$NON-NLS-1$
                        NLogger.debug(getClass(), "Text [null]{0}", event.text); //$NON-NLS-1$
                        NLogger.debug(getClass(), "Start {0}", event.start); //$NON-NLS-1$
                        NLogger.debug(getClass(), "End {0}", event.end); //$NON-NLS-1$

                        if (event.text.indexOf(StringHelper.getLineDelimiter()) != -1) {
                            NLogger.debug(getClass(), "Return, because new text contains a line delimiter."); //$NON-NLS-1$
                            return;
                        }

                        GData dataInLine = dat.getDrawPerLine().getValue(state.currentCaretPositionLine + 1);
                        final int type = dataInLine.type();
                        String[] data_segments = oldLine.trim().split("\\s+"); //$NON-NLS-1$

                        Vertex vertexToReplace = null;
                        boolean foundValidVertex = false;
                        switch (type) {
                        case 0:
                            if (data_segments.length == 6 && "0".equals(data_segments[0]) && "!LPE".equals(data_segments[1]) && "VERTEX".equals(data_segments[2])) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                if (state.currentCaretPositionChar > oldLine.indexOf("VERTEX") + 6) { //$NON-NLS-1$
                                    Vertex[] verts = vm.getDeclaredVertices().get(dataInLine);
                                    if (verts != null) {
                                        vertexToReplace = verts[0];
                                        foundVertexMetacommand = true;
                                        foundValidVertex = true;
                                        NLogger.debug(getClass(), "Vertex I"); //$NON-NLS-1$
                                    }
                                }
                            }
                            break;
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                            int index2 = StringHelper.getIndexFromWhitespaces(oldLine, state.currentCaretPositionChar);
                            if (index2 > 1) {
                                if (type > 3 && index2 > 10) {
                                    if (type == 4)
                                        vertexToReplace = vm.getQuads().get(dataInLine)[3];
                                    else if (type == 5)
                                        vertexToReplace = vm.getCondlines().get(dataInLine)[3];
                                } else if (type > 2 && index2 > 7) {
                                    if (type == 3)
                                        vertexToReplace = vm.getTriangles().get(dataInLine)[2];
                                    else if (type == 4)
                                        vertexToReplace = vm.getQuads().get(dataInLine)[2];
                                    else if (type == 5)
                                        vertexToReplace = vm.getCondlines().get(dataInLine)[2];
                                } else if (index2 > 4) {
                                    if (type == 2)
                                        vertexToReplace = vm.getLines().get(dataInLine)[1];
                                    else if (type == 3)
                                        vertexToReplace = vm.getTriangles().get(dataInLine)[1];
                                    else if (type == 4)
                                        vertexToReplace = vm.getQuads().get(dataInLine)[1];
                                    else if (type == 5)
                                        vertexToReplace = vm.getCondlines().get(dataInLine)[1];
                                } else {
                                    if (type == 2)
                                        vertexToReplace = vm.getLines().get(dataInLine)[0];
                                    else if (type == 3)
                                        vertexToReplace = vm.getTriangles().get(dataInLine)[0];
                                    else if (type == 4)
                                        vertexToReplace = vm.getQuads().get(dataInLine)[0];
                                    else if (type == 5)
                                        vertexToReplace = vm.getCondlines().get(dataInLine)[0];
                                }
                            }
                            if (!(vm.getVertexToReplace() != null && vertexToReplace != null && vm.getVertexToReplace().equals(vertexToReplace))) {
                                index2 = StringHelper.getIndexFromWhitespaces(oldLine, state.currentCaretPositionChar - 1);
                                if (index2 > 1) {
                                    if (type > 3 && index2 > 10) {
                                        if (type == 4)
                                            vertexToReplace = vm.getQuads().get(dataInLine)[3];
                                        else if (type == 5)
                                            vertexToReplace = vm.getCondlines().get(dataInLine)[3];
                                    } else if (type > 2 && index2 > 7) {
                                        if (type == 3)
                                            vertexToReplace = vm.getTriangles().get(dataInLine)[2];
                                        else if (type == 4)
                                            vertexToReplace = vm.getQuads().get(dataInLine)[2];
                                        else if (type == 5)
                                            vertexToReplace = vm.getCondlines().get(dataInLine)[2];
                                    } else if (index2 > 4) {
                                        if (type == 2)
                                            vertexToReplace = vm.getLines().get(dataInLine)[1];
                                        else if (type == 3)
                                            vertexToReplace = vm.getTriangles().get(dataInLine)[1];
                                        else if (type == 4)
                                            vertexToReplace = vm.getQuads().get(dataInLine)[1];
                                        else if (type == 5)
                                            vertexToReplace = vm.getCondlines().get(dataInLine)[1];
                                    } else {
                                        if (type == 2)
                                            vertexToReplace = vm.getLines().get(dataInLine)[0];
                                        else if (type == 3)
                                            vertexToReplace = vm.getTriangles().get(dataInLine)[0];
                                        else if (type == 4)
                                            vertexToReplace = vm.getQuads().get(dataInLine)[0];
                                        else if (type == 5)
                                            vertexToReplace = vm.getCondlines().get(dataInLine)[0];
                                    }
                                }
                            }

                            if (!(vm.getVertexToReplace() != null && vertexToReplace != null && vm.getVertexToReplace().equals(vertexToReplace))) {
                                index2 = StringHelper.getIndexFromWhitespaces(oldLine, state.currentCaretPositionChar + 1);
                                if (index2 > 1) {
                                    if (type > 3 && index2 > 10) {
                                        if (type == 4)
                                            vertexToReplace = vm.getQuads().get(dataInLine)[3];
                                        else if (type == 5)
                                            vertexToReplace = vm.getCondlines().get(dataInLine)[3];
                                    } else if (type > 2 && index2 > 7) {
                                        if (type == 3)
                                            vertexToReplace = vm.getTriangles().get(dataInLine)[2];
                                        else if (type == 4)
                                            vertexToReplace = vm.getQuads().get(dataInLine)[2];
                                        else if (type == 5)
                                            vertexToReplace = vm.getCondlines().get(dataInLine)[2];
                                    } else if (index2 > 4) {
                                        if (type == 2)
                                            vertexToReplace = vm.getLines().get(dataInLine)[1];
                                        else if (type == 3)
                                            vertexToReplace = vm.getTriangles().get(dataInLine)[1];
                                        else if (type == 4)
                                            vertexToReplace = vm.getQuads().get(dataInLine)[1];
                                        else if (type == 5)
                                            vertexToReplace = vm.getCondlines().get(dataInLine)[1];
                                    } else {
                                        if (type == 2)
                                            vertexToReplace = vm.getLines().get(dataInLine)[0];
                                        else if (type == 3)
                                            vertexToReplace = vm.getTriangles().get(dataInLine)[0];
                                        else if (type == 4)
                                            vertexToReplace = vm.getQuads().get(dataInLine)[0];
                                        else if (type == 5)
                                            vertexToReplace = vm.getCondlines().get(dataInLine)[0];
                                    }
                                }
                            }
                        default:
                            break;
                        }

                        if (vm.getVertexToReplace() != null && vertexToReplace != null && vm.getVertexToReplace().equals(vertexToReplace)) {

                            NLogger.debug(getClass(), "VerifyEvent.Text {0}", event.text); //$NON-NLS-1$
                            if (doReplace) {
                                int off = compositeText[0].getOffsetAtLine(state.currentCaretPositionLine);
                                newLine = oldLine.substring(0, event.start - off) + event.text + oldLine.substring(event.end - off);
                            } else if (event.text.length() == 0 && state.currentCaretPositionChar > 0) {
                                if (!isDelPressed[0])
                                    state.currentCaretPositionChar--;
                                newLine = oldLine.substring(0, state.currentCaretPositionChar) + oldLine.substring(state.currentCaretPositionChar + 1);
                            } else if (oldLine.length() > state.currentCaretPositionChar) {
                                newLine = oldLine.substring(0, state.currentCaretPositionChar) + event.text + oldLine.substring(state.currentCaretPositionChar);
                            } else {
                                newLine = oldLine.substring(0, state.currentCaretPositionChar) + event.text + oldLine.substring(Math.min(state.currentCaretPositionChar, oldLine.length()));
                            }

                            NLogger.debug(getClass(), "New Line {0}", newLine); //$NON-NLS-1$

                            int off2 = 0;

                            {
                                int minLen = Math.min(oldLine.length(), newLine.length());
                                int i = 0;
                                for (; i < minLen; i++) {
                                    char oc = oldLine.charAt(i);
                                    char nc = newLine.charAt(i);
                                    if (oc == nc) {
                                        off2++;
                                    } else {
                                        if (newLine.length() < oldLine.length() && nc == ' ' && i > 0 && newLine.charAt(i - 1) == '.') {
                                            off2--;
                                        }
                                        off2 += event.text.length();
                                        break;
                                    }
                                }
                                if (oldLine.length() < newLine.length() & i == minLen) {
                                    off2 = newLine.length();
                                }
                            }

                            String[] new_data_segments = newLine.trim().split("\\s+"); //$NON-NLS-1$

                            // Parse new coordinates from new line
                            BigDecimal x = null;
                            BigDecimal y = null;
                            BigDecimal z = null;

                            try {
                                switch (type) {
                                case 0:
                                    if (new_data_segments.length == 6 && "0".equals(new_data_segments[0]) && "!LPE".equals(new_data_segments[1]) && "VERTEX".equals(new_data_segments[2])) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                        if (state.currentCaretPositionChar > newLine.indexOf("VERTEX") + 6) { //$NON-NLS-1$
                                            x = new BigDecimal(new_data_segments[3]);
                                            y = new BigDecimal(new_data_segments[4]);
                                            z = new BigDecimal(new_data_segments[5]);
                                            foundValidVertex = true;
                                            foundVertexMetacommand = true;
                                            NLogger.debug(getClass(), "Vertex II"); //$NON-NLS-1$
                                        }
                                    }
                                    break;
                                case 2:
                                case 3:
                                case 4:
                                case 5:
                                    int index2;
                                    index2 = StringHelper.getIndexFromWhitespaces(newLine, state.currentCaretPositionChar);
                                    if (index2 > 0) {
                                        if (type > 3 && index2 > 10) {
                                            x = new BigDecimal(new_data_segments[11]);
                                            y = new BigDecimal(new_data_segments[12]);
                                            z = new BigDecimal(new_data_segments[13]);
                                            foundValidVertex = true;
                                        } else if (type > 2 && index2 > 7) {
                                            x = new BigDecimal(new_data_segments[8]);
                                            y = new BigDecimal(new_data_segments[9]);
                                            z = new BigDecimal(new_data_segments[10]);
                                            foundValidVertex = true;
                                        } else if (index2 > 4) {
                                            x = new BigDecimal(new_data_segments[5]);
                                            y = new BigDecimal(new_data_segments[6]);
                                            z = new BigDecimal(new_data_segments[7]);
                                            foundValidVertex = true;
                                        } else {
                                            x = new BigDecimal(new_data_segments[2]);
                                            y = new BigDecimal(new_data_segments[3]);
                                            z = new BigDecimal(new_data_segments[4]);
                                            foundValidVertex = true;
                                        }
                                    }
                                default:
                                    break;
                                }
                            } catch (Exception nfe) {
                                return;
                            }
                            if (foundValidVertex && x != null && y != null && z != null) {
                                // Do this only, if the replacement can be done!
                                Vertex newVertex = new Vertex(x, y, z);
                                if (vm.changeVertexDirect(vertexToReplace, newVertex, !foundVertexMetacommand)) {
                                    vm.setVertexToReplace(newVertex);
                                    state.setToReplaceX(x);
                                    state.setToReplaceY(y);
                                    state.setToReplaceZ(z);
                                    event.doit = false;
                                    vm.setModified_NoSync();
                                    state.currentCaretPositionChar = off2;
                                    compositeText[0].setText(state.getFileNameObj().getText()); // This has always to be the last line here!

                                } else {
                                    foundValidVertex = false;
                                    try {
                                        switch (type) {
                                        case 0:
                                            if (new_data_segments.length == 6
                                            && "0".equals(new_data_segments[0]) && "!LPE".equals(new_data_segments[1]) && "VERTEX".equals(new_data_segments[2])) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                                if (state.currentCaretPositionChar > newLine.indexOf("VERTEX") + 6) { //$NON-NLS-1$
                                                    x = new BigDecimal(new_data_segments[3]);
                                                    y = new BigDecimal(new_data_segments[4]);
                                                    z = new BigDecimal(new_data_segments[5]);
                                                    foundValidVertex = true;
                                                    foundVertexMetacommand = true;
                                                    NLogger.debug(getClass(), "Vertex III"); //$NON-NLS-1$
                                                }
                                            }
                                            break;
                                        case 2:
                                        case 3:
                                        case 4:
                                        case 5:
                                            int index2;
                                            index2 = StringHelper.getIndexFromWhitespaces(newLine, state.currentCaretPositionChar + 1);
                                            if (index2 > 1) {
                                                if (type > 3 && index2 > 10) {
                                                    x = new BigDecimal(new_data_segments[11]);
                                                    y = new BigDecimal(new_data_segments[12]);
                                                    z = new BigDecimal(new_data_segments[13]);
                                                    foundValidVertex = true;
                                                } else if (type > 2 && index2 > 7) {
                                                    x = new BigDecimal(new_data_segments[8]);
                                                    y = new BigDecimal(new_data_segments[9]);
                                                    z = new BigDecimal(new_data_segments[10]);
                                                    foundValidVertex = true;
                                                } else if (index2 > 4) {
                                                    x = new BigDecimal(new_data_segments[5]);
                                                    y = new BigDecimal(new_data_segments[6]);
                                                    z = new BigDecimal(new_data_segments[7]);
                                                    foundValidVertex = true;
                                                } else {
                                                    x = new BigDecimal(new_data_segments[2]);
                                                    y = new BigDecimal(new_data_segments[3]);
                                                    z = new BigDecimal(new_data_segments[4]);
                                                    foundValidVertex = true;
                                                }
                                            }
                                        default:
                                            break;
                                        }
                                    } catch (Exception nfe) {
                                        return;
                                    }
                                    if (foundValidVertex && x != null && y != null && z != null) {
                                        // Do this only, if the replacement can
                                        // be done!
                                        newVertex = new Vertex(x, y, z);
                                        if (vm.changeVertexDirect(vertexToReplace, newVertex, !foundVertexMetacommand)) {
                                            vm.setVertexToReplace(newVertex);
                                            state.setToReplaceX(x);
                                            state.setToReplaceY(y);
                                            state.setToReplaceZ(z);
                                            event.doit = false;
                                            vm.setModified_NoSync();
                                            state.currentCaretPositionChar = off2;
                                            compositeText[0].setText(state.getFileNameObj().getText()); // This has always to be the last line here!
                                        } else {
                                            foundValidVertex = false;
                                            try {
                                                switch (type) {
                                                case 0:
                                                    if (new_data_segments.length == 6
                                                    && "0".equals(new_data_segments[0]) && "!LPE".equals(new_data_segments[1]) && "VERTEX".equals(new_data_segments[2])) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                                        if (state.currentCaretPositionChar > newLine.indexOf("VERTEX") + 5) { //$NON-NLS-1$
                                                            x = new BigDecimal(new_data_segments[3]);
                                                            y = new BigDecimal(new_data_segments[4]);
                                                            z = new BigDecimal(new_data_segments[5]);
                                                            foundValidVertex = true;
                                                            foundVertexMetacommand = true;
                                                            NLogger.debug(getClass(), "Vertex IV"); //$NON-NLS-1$
                                                        }
                                                    }
                                                    break;
                                                case 2:
                                                case 3:
                                                case 4:
                                                case 5:
                                                    int index2;
                                                    index2 = StringHelper.getIndexFromWhitespaces(newLine, state.currentCaretPositionChar - 1);
                                                    if (index2 > 1) {
                                                        if (type > 3 && index2 > 10) {
                                                            x = new BigDecimal(new_data_segments[11]);
                                                            y = new BigDecimal(new_data_segments[12]);
                                                            z = new BigDecimal(new_data_segments[13]);
                                                            foundValidVertex = true;
                                                        } else if (type > 2 && index2 > 7) {
                                                            x = new BigDecimal(new_data_segments[8]);
                                                            y = new BigDecimal(new_data_segments[9]);
                                                            z = new BigDecimal(new_data_segments[10]);
                                                            foundValidVertex = true;
                                                        } else if (index2 > 4) {
                                                            x = new BigDecimal(new_data_segments[5]);
                                                            y = new BigDecimal(new_data_segments[6]);
                                                            z = new BigDecimal(new_data_segments[7]);
                                                            foundValidVertex = true;
                                                        } else {
                                                            x = new BigDecimal(new_data_segments[2]);
                                                            y = new BigDecimal(new_data_segments[3]);
                                                            z = new BigDecimal(new_data_segments[4]);
                                                            foundValidVertex = true;
                                                        }
                                                    }
                                                default:
                                                    break;
                                                }
                                            } catch (Exception nfe) {
                                                return;
                                            }
                                            if (foundValidVertex && x != null && y != null && z != null) {
                                                // Do this only, if the
                                                // replacement can be done!
                                                newVertex = new Vertex(x, y, z);
                                                if (vm.changeVertexDirect(vertexToReplace, newVertex, !foundVertexMetacommand)) {
                                                    vm.setVertexToReplace(newVertex);
                                                    state.setToReplaceX(x);
                                                    state.setToReplaceY(y);
                                                    state.setToReplaceZ(z);
                                                    event.doit = false;
                                                    vm.setModified_NoSync();
                                                    state.currentCaretPositionChar = off2;
                                                    compositeText[0].setText(state.getFileNameObj().getText()); // This has always to be the last line here!
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });

        // The line number canvas should be updated when the text contents are
        // modified
        compositeText[0].addExtendedModifyListener(new ExtendedModifyListener() {
            private int old_line_count;

            @Override
            public void modifyText(final ExtendedModifyEvent event) {
                final DatFile dat = state.getFileNameObj();
                final VertexManager vm = dat.getVertexManager();

                vm.addSnapshot();

                ViewIdleManager.pause[0].compareAndSet(false, true);

                final String text = compositeText[0].getText();

                int new_line_count = compositeText[0].getLineCount();
                if (old_line_count != new_line_count) {
                    old_line_count = new_line_count;
                    int number_of_digits = (int) Math.log10(new_line_count);
                    // TODO DEBUG
                    ((GridData) canvas_lineNumberArea[0].getLayoutData()).widthHint = (number_of_digits + (NLogger.DEBUG ? 26 : 2)) * Font.MONOSPACE_WIDTH;
                    canvas_lineNumberArea[0].getParent().layout();
                }
                if (event.length == 0) {
                    // Text deleted
                    canvas_lineNumberArea[0].redraw();
                } else {
                    // Text inserted
                    if (state.isDoingPaste() && Editor3DWindow.getWindow().isMovingAdjacentData() && WorkbenchManager.getUserSettingState().isDisableMADtext()) {
                        Editor3DWindow.getWindow().setMovingAdjacentData(false);
                        GuiManager.updateStatus();
                    }
                }

                if (text.equals(dat.getOriginalText()) && dat.getOldName().equals(dat.getNewName())) {
                    if (!dat.isVirtual()) state.getTab().setText(state.filename);
                    // Do not remove virtual files from the unsaved file list
                    // (they are virtual, because they were not saved at all!)
                    if (Project.getUnsavedFiles().contains(dat) && !dat.isVirtual()) {
                        Project.removeUnsavedFile(dat);
                        Editor3DWindow.getWindow().updateTree_unsavedEntries();
                    }
                } else {
                    state.getTab().setText(state.getFilenameWithStar());
                    if (!Project.getUnsavedFiles().contains(dat)) {
                        Project.addUnsavedFile(dat);
                        Editor3DWindow.getWindow().updateTree_unsavedEntries();
                    }
                }
                dat.setText(text);
                final int off = event.start + event.length;
                final String insertedText = event.length == 0 ? "" : compositeText[0].getText(event.start, off - 1); //$NON-NLS-1$

                if (!state.isSync()) {
                    boolean doRedraw = !vm.getSelectedData().isEmpty();
                    if (compositeText[0].isFocusControl()) {
                        vm.clearSelection();
                    }
                    if (doRedraw) {
                        compositeText[0].redraw();
                    }
                }
                // Reset the caret position when a vertex was modified
                if (vm.getVertexToReplace() != null) {
                    if (vm.isModified()) {
                        try {
                            compositeText[0].setCaretOffset(compositeText[0].getOffsetAtLine(state.currentCaretPositionLine) + state.currentCaretPositionChar);
                        } catch (IllegalArgumentException iae) {
                            int diff = 1;
                            int trys = 0;
                            while (true) {
                                try {
                                    compositeText[0].setCaretOffset(compositeText[0].getOffsetAtLine(state.currentCaretPositionLine) + state.currentCaretPositionChar - diff);
                                    break;
                                } catch (IllegalArgumentException iae2) {
                                    trys++;
                                    diff++;
                                    if (trys > 10) {
                                        compositeText[0].setCaretOffset(0);
                                        break;
                                    }
                                }
                            }
                        }
                        compositeText[0].setTopIndex(state.currentCaretTopIndex);
                        NLogger.debug(getClass(), "Caret Reset"); //$NON-NLS-1$
                        vm.getSelectedVertices().clear();
                        vm.getSelectedVertices().add(vm.getVertexToReplace());
                    }
                }

                if (state.isSync()) {
                    state.getFileNameObj().parseForError(compositeText[0], event.start, off, event.length, insertedText, event.replacedText, treeItem_Hints[0], treeItem_Warnings[0],
                            treeItem_Errors[0], treeItem_Duplicates[0], false);
                    vm.setModified(false, true);
                } else {
                    if (!vm.isModified()) {
                        Display.getCurrent().syncExec(new Runnable() {
                            @Override
                            public void run() {
                                state.getFileNameObj().parseForErrorAndData(compositeText[0], event.start, off, event.length, insertedText, event.replacedText, treeItem_Hints[0], treeItem_Warnings[0],
                                        treeItem_Errors[0], treeItem_Duplicates[0]);
                            }
                        });
                    } else {
                        vm.setModified(false, true);
                        GData.CACHE_warningsAndErrors.clear();
                        state.getFileNameObj().parseForError(compositeText[0], event.start, off, event.length, insertedText, event.replacedText, treeItem_Hints[0], treeItem_Warnings[0],
                                treeItem_Errors[0], treeItem_Duplicates[0], true);
                    }
                    vm.setUpdated(true);
                }
                int errorCount = treeItem_Errors[0].getItems().size();
                int warningCount = treeItem_Warnings[0].getItems().size();
                int hintCount = treeItem_Hints[0].getItems().size();
                int duplicateCount = treeItem_Duplicates[0].getItems().size();
                String errors = errorCount == 1 ? I18n.EDITORTEXT_Error : I18n.EDITORTEXT_Errors;
                String warnings = warningCount == 1 ? I18n.EDITORTEXT_Warning : I18n.EDITORTEXT_Warnings;
                String hints = hintCount == 1 ? I18n.EDITORTEXT_Other : I18n.EDITORTEXT_Others;
                String duplicates = duplicateCount == 1 ? I18n.EDITORTEXT_Duplicate : I18n.EDITORTEXT_Duplicates;
                lbl_ProblemCount[0].setText(errorCount + " " + errors + ", " + warningCount + " " + warnings + ", " + hintCount + " " + hints + ", " + duplicateCount + " " + duplicates); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
                lbl_ProblemCount[0].getParent().layout();
            }
        });
        final CompositeTab me = this;
        compositeText[0].addListener(SWT.KeyDown, new Listener() {
            @Override
            // MARK KeyDown (Quick Fix)
            public void handleEvent(Event event) {

                final DatFile df = state.getFileNameObj();
                final VertexManager vm = df.getVertexManager();

                vm.addSnapshot();

                // NLogger.debug(getClass(),
                // KeyBoardHelper.getKeyString(event));

                final int keyCode = event.keyCode;
                final boolean ctrlPressed = (event.stateMask & SWT.CTRL) != 0;
                final boolean altPressed = (event.stateMask & SWT.ALT) != 0;
                final boolean shiftPressed = (event.stateMask & SWT.SHIFT) != 0;
                StringBuilder sb = new StringBuilder();
                sb.append(keyCode);
                sb.append(ctrlPressed ? "+Ctrl" : ""); //$NON-NLS-1$//$NON-NLS-2$
                sb.append(altPressed ? "+Alt" : ""); //$NON-NLS-1$//$NON-NLS-2$
                sb.append(shiftPressed ? "+Shift" : ""); //$NON-NLS-1$//$NON-NLS-2$
                TextTask task = KeyStateManager.getTextTaskmap().get(sb.toString());

                if (task != null) {
                    ViewIdleManager.pause[0].compareAndSet(false, true);

                    switch (task) {
                    case EDITORTEXT_REPLACE_VERTEX:
                        if (compositeText[0].getEditable()) {
                            if (!vm.isUpdated()) return;
                            VertexMarker.markTheVertex(state, compositeText[0], df);
                            if (state.isReplacingVertex()) {
                                if (state.window[0] == Editor3DWindow.getWindow()) {
                                    Editor3DWindow.getStatusLabel().setText(I18n.EDITORTEXT_SyncEdit);
                                    Editor3DWindow.getStatusLabel().setSize(Editor3DWindow.getStatusLabel().computeSize(SWT.DEFAULT, SWT.DEFAULT));
                                    Editor3DWindow.getStatusLabel().update();
                                } else {
                                    state.window[0].setStatus(I18n.EDITORTEXT_SyncEdit);
                                }
                            }
                        }
                        break;
                    case EDITORTEXT_ESC:
                        if (compositeText[0].getEditable()) {
                            if (!vm.isUpdated()) return;
                            state.setReplacingVertex(false);
                            vm.setVertexToReplace(null);
                            compositeText[0].redraw(0, 0, compositeText[0].getBounds().width, compositeText[0].getBounds().height, true);
                            if (state.window[0] == Editor3DWindow.getWindow()) {
                                Editor3DWindow.getStatusLabel().setText(I18n.EDITORTEXT_SyncEditDeactivated);
                                Editor3DWindow.getStatusLabel().setSize(Editor3DWindow.getStatusLabel().computeSize(SWT.DEFAULT, SWT.DEFAULT));
                                Editor3DWindow.getStatusLabel().update();
                            } else {
                                state.window[0].setStatus(I18n.EDITORTEXT_SyncEditDeactivated);
                            }
                        }
                        break;
                    case EDITORTEXT_QUICKFIX:
                        if (compositeText[0].getEditable()) {
                            if (!vm.isUpdated()) return;
                            HashSet<TreeItem> items = new HashSet<TreeItem>();
                            int offset = compositeText[0].getOffsetAtLine(Math.max(Math.min(state.currentLineIndex, compositeText[0].getLineCount() - 1), 0));
                            for (TreeItem t : treeItem_Hints[0].getItems()) {
                                if (!t.getText(0).isEmpty() && ((Integer) t.getData()).intValue() == offset) {
                                    NLogger.debug(getClass(), "Found hint at {0}", t.getText(1)); //$NON-NLS-1$
                                    items.add(t);
                                }
                            }
                            for (TreeItem t : treeItem_Warnings[0].getItems()) {
                                if (!t.getText(0).isEmpty() && ((Integer) t.getData()).intValue() == offset) {
                                    NLogger.debug(getClass(), "Found warning at {0}", t.getText(1)); //$NON-NLS-1$
                                    items.add(t);
                                }
                            }
                            for (TreeItem t : treeItem_Errors[0].getItems()) {
                                if (!t.getText(0).isEmpty() && ((Integer) t.getData()).intValue() == offset) {
                                    NLogger.debug(getClass(), "Found error at {0}", t.getText(1)); //$NON-NLS-1$
                                    items.add(t);
                                }
                            }
                            for (TreeItem t : treeItem_Duplicates[0].getItems()) {
                                if (!t.getText(0).isEmpty() && ((Integer) t.getData()).intValue() == offset) {
                                    NLogger.debug(getClass(), "Found duplicate at {0}", t.getText(1)); //$NON-NLS-1$
                                    items.add(t);
                                }
                            }

                            QuickFixer.fixTextIssues(compositeText[0], items, df);
                        }
                        break;
                    case EDITORTEXT_SELECTALL:
                        compositeText[0].setSelection(0, compositeText[0].getText().length());
                        break;
                    case EDITORTEXT_INLINE:
                    {
                        if (!vm.isUpdated()) return;
                        NLogger.debug(getClass(), "Inlining per action key.."); //$NON-NLS-1$
                        final StyledText st = compositeText[0];
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
                        Inliner.inline(st, fromLine, toLine, df);
                        st.forceFocus();
                        break;
                    }
                    case EDITORTEXT_ROUND:
                    {
                        if (!vm.isUpdated()) return;
                        if (new RoundDialog(compositeText[0].getShell()).open() == IDialogConstants.CANCEL_ID) return;
                        NLogger.debug(getClass(), "Rounding.."); //$NON-NLS-1$
                        final StyledText st = compositeText[0];
                        int s1 = st.getSelectionRange().x;
                        int s2 = s1 + st.getSelectionRange().y;
                        int fromLine = s1 > -1 ? st.getLineAtOffset(s1) : s1 * -1;
                        int toLine = s2 > -1 ? st.getLineAtOffset(s2) : s2 * -1;
                        fromLine++;
                        toLine++;
                        NLogger.debug(getClass(), "From line {0}", fromLine); //$NON-NLS-1$
                        NLogger.debug(getClass(), "To   line {0}", toLine); //$NON-NLS-1$
                        Rounder.round(state, st, fromLine, toLine, df);
                        st.forceFocus();
                        break;
                    }
                    case EDITORTEXT_REDO:
                    {
                        final Shell sh = compositeText[0].getDisplay().getActiveShell();
                        if (vm.isUpdated() && sh != null) df.redo(sh);
                    }
                    break;
                    case EDITORTEXT_UNDO:
                    {
                        final Shell sh = compositeText[0].getDisplay().getActiveShell();
                        if (vm.isUpdated() && sh != null) df.undo(sh);
                    }
                    break;
                    case EDITORTEXT_SAVE:
                        if (!df.isReadOnly()) {
                            final Shell sh = compositeText[0].getDisplay().getActiveShell();
                            if (df.save()) {
                                Editor3DWindow.getWindow().addRecentFile(df);
                                Project.removeUnsavedFile(df);
                                Editor3DWindow.getWindow().updateTree_unsavedEntries();
                            } else if (sh != null) {
                                MessageBox messageBoxError = new MessageBox(sh, SWT.ICON_ERROR | SWT.OK);
                                messageBoxError.setText(I18n.DIALOG_Error);
                                messageBoxError.setMessage(I18n.DIALOG_CantSaveFile);
                                messageBoxError.open();
                            }
                        }
                        break;
                    case EDITORTEXT_FIND:
                    {
                        final Shell sh = compositeText[0].getDisplay().getActiveShell();
                        if (!vm.isUpdated() || sh == null) return;
                        NLogger.debug(getClass(), "Find and Replace.."); //$NON-NLS-1$
                        SearchWindow win = Editor3DWindow.getWindow().getSearchWindow();
                        if (win != null) {
                            win.close();
                        }
                        Editor3DWindow.getWindow().setSearchWindow(new SearchWindow(sh));
                        Editor3DWindow.getWindow().getSearchWindow().run();
                        Editor3DWindow.getWindow().getSearchWindow().setTextComposite(me);
                        Editor3DWindow.getWindow().getSearchWindow().setScopeToAll();
                        break;
                    }
                    case EDITORTEXT_INSERT_REFERENCE:
                    {
                        if (!vm.isUpdated() || df.isReadOnly()) return;
                        NLogger.debug(getClass(), "Insert TYPE 1 reference line.."); //$NON-NLS-1$

                        final StyledText st = compositeText[0];
                        int s1 = st.getSelectionRange().x;
                        int s2 = s1 + st.getSelectionRange().y;
                        int fromLine = s1 > -1 ? st.getLineAtOffset(s1) : s1 * -1;
                        int toLine = s2 > -1 ? st.getLineAtOffset(s2) : s2 * -1;
                        if (fromLine != toLine) return;
                        String currentLine = st.getLine(fromLine);
                        fromLine++;
                        NLogger.debug(getClass(), "Line {0}", fromLine); //$NON-NLS-1$
                        NLogger.debug(getClass(), currentLine);

                        final boolean needNewLine = StringHelper.isNotBlank(currentLine);

                        final String referenceLine = (needNewLine ? StringHelper.getLineDelimiter() : "") + "1 16 0 0 0 1 0 0 0 1 0 0 0 1 "; //$NON-NLS-1$ //$NON-NLS-2$

                        int delta = 0;
                        s1 = compositeText[0].getOffsetAtLine(toLine);
                        if (needNewLine) {
                            compositeText[0].setSelection(s1 + currentLine.length());
                        } else {
                            if (!currentLine.isEmpty()) {
                                compositeText[0].setSelection(s1, s1 + currentLine.length());
                                delta = referenceLine.length();
                            }
                        }
                        compositeText[0].insert(referenceLine);
                        compositeText[0].setCaretOffset(compositeText[0].getCaretOffset() + referenceLine.length() - delta);
                        break;
                    }
                    default:
                        break;
                    }
                }
            }
        });
        compositeText[0].addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
            }

            @Override
            public void focusGained(FocusEvent e) {
                final VertexManager vm = state.getFileNameObj().getVertexManager();
                if (!vm.isSyncWithTextEditor()) {
                    if (vm.isModified() && !vm.isUpdated()) {
                        NLogger.debug(getClass(), "Text focused, reload"); //$NON-NLS-1$
                        try {
                            compositeText[0].setText(state.getFileNameObj().getText());
                        } catch (IllegalArgumentException iae) {
                            // Ignored on termination
                        }
                        vm.setUpdated(true);
                    } else if (!vm.isModified() && !vm.isUpdated()) {
                        vm.setUpdated(true);
                    }
                }
                SearchWindow sw = Editor3DWindow.getWindow().getSearchWindow();
                if (sw != null) {
                    sw.setTextComposite(state.getTab());
                    sw.setScopeToAll();
                }
            }
        });
        //
        compositeText[0].addCaretListener(new CaretListener() {
            @Override
            public void caretMoved(CaretEvent event) {
                ViewIdleManager.pause[0].compareAndSet(false, true);
                Point r = compositeText[0].getSelectionRange();
                state.setOldLineIndex(-1);
                if (!state.isSync()) {
                    DatFile df = state.getFileNameObj();
                    df.addHistory(compositeText[0].getText(), r.x, r.y, compositeText[0].getTopIndex());

                    final boolean checkDuplicates = df.updateDuplicatesErrors(compositeText[0], treeItem_Duplicates[0]);
                    final boolean checkDatHeader = df.updateDatHeaderHints(compositeText[0], treeItem_Hints[0]);
                    if (checkDuplicates || checkDatHeader) {
                        if (checkDuplicates) {
                            df.getDuplicate().pushDuplicateCheck(df.getDrawChainStart());
                        }
                        if (checkDatHeader) {
                            df.getDatHeader().pushDatHeaderCheck(df.getDrawChainStart());
                        }
                        int errorCount = treeItem_Errors[0].getItems().size();
                        int warningCount = treeItem_Warnings[0].getItems().size();
                        int hintCount = treeItem_Hints[0].getItems().size();
                        int duplicateCount = treeItem_Duplicates[0].getItems().size();
                        String errors = errorCount == 1 ? I18n.EDITORTEXT_Error : I18n.EDITORTEXT_Errors;
                        String warnings = warningCount == 1 ? I18n.EDITORTEXT_Warning : I18n.EDITORTEXT_Warnings;
                        String hints = hintCount == 1 ? I18n.EDITORTEXT_Other : I18n.EDITORTEXT_Others;
                        String duplicates = duplicateCount == 1 ? I18n.EDITORTEXT_Duplicate : I18n.EDITORTEXT_Duplicates;
                        lbl_ProblemCount[0].setText(errorCount + " " + errors + ", " + warningCount + " " + warnings + ", " + hintCount + " " + hints + ", " + duplicateCount + " " + duplicates); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
                        treeItem_Hints[0].getParent().build();
                        lbl_ProblemCount[0].getParent().layout();
                    }
                }
                try {
                    compositeText[0].setLineBackground(state.currentLineIndex, 1, compositeText[0].getBackground());
                } catch (Exception a) {
                }
                int caret_offset = event.caretOffset;
                state.currentLineIndex = compositeText[0].getLineAtOffset(caret_offset);
                if (compositeText[0].getSelectionCount() == 0) {
                    try {
                        compositeText[0].setLineBackground(state.currentLineIndex, 1, Colour.line_highlight_background[0]);
                    } catch (Exception a) {
                    }
                }
                if (state.window[0] == Editor3DWindow.getWindow()) {
                    try {
                        if (state.isReplacingVertex()) {
                            Editor3DWindow.getStatusLabel().setText(state.currentLineIndex + 1 + " : " + (caret_offset - compositeText[0].getOffsetAtLine(state.currentLineIndex) + 1) + "   " + I18n.EDITORTEXT_SyncEdit); //$NON-NLS-1$ //$NON-NLS-2$
                        } else {
                            Editor3DWindow.getStatusLabel().setText(state.currentLineIndex + 1 + " : " + (caret_offset - compositeText[0].getOffsetAtLine(state.currentLineIndex) + 1)); //$NON-NLS-1$
                        }
                        Editor3DWindow.getStatusLabel().setSize(Editor3DWindow.getStatusLabel().computeSize(SWT.DEFAULT, SWT.DEFAULT));
                        Editor3DWindow.getStatusLabel().update();
                    } catch (Exception a) {
                    }
                } else {
                    try {
                        if (state.isReplacingVertex()) {
                            state.window[0].setStatus(state.currentLineIndex + 1 + " : " + (caret_offset - compositeText[0].getOffsetAtLine(state.currentLineIndex) + 1) + "   " + I18n.EDITORTEXT_SyncEdit); //$NON-NLS-1$ //$NON-NLS-2$
                        } else {
                            state.window[0].setStatus(state.currentLineIndex + 1 + " : " + (caret_offset - compositeText[0].getOffsetAtLine(state.currentLineIndex) + 1)); //$NON-NLS-1$
                        }
                    } catch (Exception a) {
                    }
                }
                canvas_lineNumberArea[0].redraw();
            }
        });
        tabFolder_partInformation[0].addCTabFolder2Listener(new CTabFolder2Listener() {
            @Override
            public void showList(CTabFolderEvent event) {
            }

            @Override
            public void restore(CTabFolderEvent event) {
            }

            @Override
            public void minimize(CTabFolderEvent event) {
            }

            @Override
            public void maximize(CTabFolderEvent event) {
            }

            @Override
            public void close(CTabFolderEvent event) {
                ((CTabFolder) event.widget).setVisible(false);
                sashForm[0].setWeights(new int[] { compositeText[0].getSize().y, 1 });
                event.doit = false;
            }
        });
        final SelectionAdapter quickFix = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (compositeText[0].getEditable() && tree_Problems[0].getSelectionCount() > 0) {
                    final VertexManager vm = state.getFileNameObj().getVertexManager();
                    if (!vm.isUpdated()) return;
                    HashSet<TreeItem> items = new HashSet<TreeItem>();
                    for (TreeItem sort : tree_Problems[0].getSelection()) {
                        items.add(sort);
                    }
                    if (items.contains(treeItem_Hints[0])) {
                        NLogger.debug(getClass(), "+Quick fix all hints."); //$NON-NLS-1$
                        items.remove(treeItem_Hints[0]);
                        for (TreeItem sort : treeItem_Hints[0].getItems()) {
                            if (!items.contains(sort))
                                items.add(sort);
                        }
                    }
                    if (items.contains(treeItem_Errors[0])) {
                        NLogger.debug(getClass(), "+Quick fix all errors."); //$NON-NLS-1$
                        items.remove(treeItem_Errors[0]);
                        for (TreeItem sort : treeItem_Errors[0].getItems()) {
                            if (!items.contains(sort))
                                items.add(sort);
                        }
                    }
                    if (items.contains(treeItem_Warnings[0])) {
                        NLogger.debug(getClass(), "+Quick fix all warnings."); //$NON-NLS-1$
                        items.remove(treeItem_Warnings[0]);
                        for (TreeItem sort : treeItem_Warnings[0].getItems()) {
                            if (!items.contains(sort))
                                items.add(sort);
                        }
                    }
                    if (items.contains(treeItem_Duplicates[0])) {
                        NLogger.debug(getClass(), "+Quick fix all duplicates."); //$NON-NLS-1$
                        items.remove(treeItem_Duplicates[0]);
                        for (TreeItem sort : treeItem_Duplicates[0].getItems()) {
                            if (!items.contains(sort))
                                items.add(sort);
                        }
                    }

                    for (TreeItem issue : items) {
                        if (issue != null && issue.getData() != null) {
                            NLogger.debug(getClass(), "+Fix {0}", issue.getText(1)); //$NON-NLS-1$
                        }
                    }

                    QuickFixer.fixTextIssues(compositeText[0], items, getState().getFileNameObj());
                }
            }
        };
        final SelectionAdapter quickFixSame = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (compositeText[0].getEditable() && tree_Problems[0].getSelectionCount() > 0) {
                    final VertexManager vm = state.getFileNameObj().getVertexManager();
                    if (!vm.isUpdated()) return;
                    HashSet<TreeItem> items = new HashSet<TreeItem>();
                    HashSet<String> sorts = new HashSet<String>();
                    for (TreeItem sort : tree_Problems[0].getSelection()) {
                        if (sort == null) continue;
                        if (sort.equals(treeItem_Hints[0])) {
                            items.add(treeItem_Hints[0]);
                        } else if (sort.equals(treeItem_Errors[0])) {
                            items.add(treeItem_Errors[0]);
                        } else if (sort.equals(treeItem_Warnings[0])) {
                            items.add(treeItem_Warnings[0]);
                        } else if (sort.equals(treeItem_Duplicates[0])) {
                            items.add(treeItem_Duplicates[0]);
                        }
                        if (sort.getText(2).startsWith("[WFE]")) { //$NON-NLS-1$
                            if (!sorts.contains(sort.getText(2)))
                                sorts.add(sort.getText(2));
                        } else if (sort.getText(2).startsWith("[E01]")) { //$NON-NLS-1$
                            if (!sorts.contains(sort.getText(2)))
                                sorts.add(sort.getText(2));
                        } else {
                            if (!sorts.contains(sort.getText(0)))
                                sorts.add(sort.getText(0));
                        }

                    }
                    for (TreeItem sort : treeItem_Hints[0].getItems()) {
                        if (sorts.contains(sort.getText(0)) && !items.contains(sort))
                            items.add(sort);
                    }
                    for (TreeItem sort : treeItem_Errors[0].getItems()) {
                        if (sorts.contains(sort.getText(0)) && !items.contains(sort))
                            items.add(sort);
                    }
                    for (TreeItem sort : treeItem_Warnings[0].getItems()) {
                        if (sorts.contains(sort.getText(0)) || sorts.contains(sort.getText(2)) && !items.contains(sort))
                            items.add(sort);
                    }
                    for (TreeItem sort : treeItem_Duplicates[0].getItems()) {
                        if (sorts.contains(sort.getText(0)) || sorts.contains(sort.getText(2)) && !items.contains(sort))
                            items.add(sort);
                    }


                    if (items.contains(treeItem_Hints[0])) {
                        NLogger.debug(getClass(), "+Quick fix all hints."); //$NON-NLS-1$
                        items.remove(treeItem_Hints[0]);
                        for (TreeItem sort : treeItem_Hints[0].getItems()) {
                            if (!items.contains(sort))
                                items.add(sort);
                        }
                    }
                    if (items.contains(treeItem_Errors[0])) {
                        NLogger.debug(getClass(), "+Quick fix all errors."); //$NON-NLS-1$
                        items.remove(treeItem_Errors[0]);
                        for (TreeItem sort : treeItem_Errors[0].getItems()) {
                            if (!items.contains(sort))
                                items.add(sort);
                        }
                    }
                    if (items.contains(treeItem_Warnings[0])) {
                        NLogger.debug(getClass(), "+Quick fix all warnings."); //$NON-NLS-1$
                        items.remove(treeItem_Warnings[0]);
                        for (TreeItem sort : treeItem_Warnings[0].getItems()) {
                            if (!items.contains(sort))
                                items.add(sort);
                        }
                    }
                    if (items.contains(treeItem_Duplicates[0])) {
                        NLogger.debug(getClass(), "+Quick fix all duplicates."); //$NON-NLS-1$
                        items.remove(treeItem_Duplicates[0]);
                        for (TreeItem sort : treeItem_Duplicates[0].getItems()) {
                            if (!items.contains(sort))
                                items.add(sort);
                        }
                    }

                    for (TreeItem issue : items) {
                        if (issue.getData() != null) {
                            NLogger.debug(getClass(), "+Fix {0}", issue.getText(1)); //$NON-NLS-1$
                        }
                    }

                    QuickFixer.fixTextIssues(compositeText[0], items, getState().getFileNameObj());
                }
            }
        };
        mntm_QuickFix[0].addSelectionListener(quickFix);
        mntm_QuickFixSame[0].addSelectionListener(quickFixSame);
        btn_QuickFix[0].addSelectionListener(quickFix);
        btn_QuickFixSame[0].addSelectionListener(quickFixSame);
        tree_Problems[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                boolean enabled = tree_Problems[0].getSelectionCount() == 1 && tree_Problems[0].getSelection()[0] != null;
                btn_QuickFix[0].setEnabled(enabled);
                btn_QuickFixSame[0].setEnabled(enabled);
            }
        });
        tree_Problems[0].addListener(SWT.MouseDoubleClick, new Listener() {
            @Override
            public void handleEvent(Event e) {
                if (tree_Problems[0].getSelectionCount() == 1 && tree_Problems[0].getSelection()[0] != null) {
                    if (tree_Problems[0].getSelection()[0].getData() != null) {
                        int pos = (Integer) tree_Problems[0].getSelection()[0].getData();
                        if (pos < 0)
                            pos = 0;
                        compositeText[0].setSelection(pos);
                    }
                    if (tree_Problems[0].getSelection()[0].getParentItem() == null) {
                        tree_Problems[0].getSelection()[0].setVisible(!tree_Problems[0].getSelection()[0].isVisible());
                        TreeItem sel = tree_Problems[0].getSelection()[0];
                        Display.getCurrent().asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                tree_Problems[0].build();
                            }
                        });
                        tree_Problems[0].redraw();
                        tree_Problems[0].update();
                        tree_Problems[0].getTree().select(tree_Problems[0].getMapInv().get(sel));
                    }
                }
            }
        });
        compositeContainer[0].addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                ((GridData) compositeText[0].getLayoutData()).minimumHeight = compositeContainer[0].getBounds().height;
                if (!tabFolder_partInformation[0].getVisible()) {
                    sashForm[0].setWeights(new int[] { compositeText[0].getSize().y, 1 });
                }
                compositeContainer[0].layout();
            }
        });
        canvas_lineNumberArea[0].addMouseListener(new MouseListener() {

            @Override
            public void mouseUp(MouseEvent e) {}

            @Override
            public void mouseDown(MouseEvent e) {
                // TODO Auto-generated method stub
                int y_offset = -compositeText[0].getVerticalBar().getSelection() % caretHeight;
                int height = compositeContainer[0].getBounds().height;
                int start_line = compositeText[0].getVerticalBar().getSelection() / caretHeight + 1;
                int end_line = compositeText[0].getLineCount() - 1;

                NLogger.debug(getClass(), "Mouse down on Line Number Area"); //$NON-NLS-1$

                NLogger.debug(getClass(), "y_offset" + y_offset); //$NON-NLS-1$
                NLogger.debug(getClass(), "height" + height); //$NON-NLS-1$
                NLogger.debug(getClass(), "start_line" + start_line); //$NON-NLS-1$
                NLogger.debug(getClass(), "end_line" + end_line); //$NON-NLS-1$

                NLogger.debug(getClass(), "e.y" + e.y); //$NON-NLS-1$


                int line = (e.y - y_offset) / caretHeight + start_line;

                NLogger.debug(getClass(), "Line " + line); //$NON-NLS-1$
                line--;

                final int oldSelectionStart = compositeText[0].getSelection().x;
                final int oldSelectionEnd = compositeText[0].getSelection().y;

                if ((e.stateMask & SWT.CTRL) != 0) {
                    try {
                        int newstart = compositeText[0].getOffsetAtLine(line + 1);
                        int newend = compositeText[0].getOffsetAtLine(line);
                        newstart = Math.max(Math.max(oldSelectionStart, oldSelectionEnd), newstart);
                        newend = Math.min(Math.min(oldSelectionStart, oldSelectionEnd), newend);
                        compositeText[0].setSelection(newstart, newend);
                    } catch (IllegalArgumentException iae) {
                        try {
                            int newstart = compositeText[0].getText().length();
                            int newend = compositeText[0].getOffsetAtLine(line);
                            newstart = Math.max(Math.max(oldSelectionStart, oldSelectionEnd), newstart);
                            newend = Math.min(Math.min(oldSelectionStart, oldSelectionEnd), newend);
                            compositeText[0].setSelection(newstart, newend);
                        } catch (IllegalArgumentException consumed) {}
                    }
                } else {
                    try {
                        compositeText[0].setSelection(compositeText[0].getOffsetAtLine(line + 1), compositeText[0].getOffsetAtLine(line));
                    } catch (IllegalArgumentException iae) {
                        try {
                            compositeText[0].setSelection(compositeText[0].getText().length(), compositeText[0].getOffsetAtLine(line));
                        } catch (IllegalArgumentException consumed) {}
                    }
                }
                try {
                    if (compositeText[0].getSelectionText().endsWith(StringHelper.getLineDelimiter())) {
                        compositeText[0].setSelection(compositeText[0].getSelection().x, compositeText[0].getSelection().y - StringHelper.getLineDelimiter().length());
                    }
                } catch (IllegalArgumentException consumed) {}
            }

            @Override
            public void mouseDoubleClick(MouseEvent e) {}
        });
        canvas_lineNumberArea[0].addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                e.gc.setFont(Font.MONOSPACE);
                int y_offset = -compositeText[0].getVerticalBar().getSelection() % caretHeight;
                int height = compositeContainer[0].getBounds().height;
                int start_line = compositeText[0].getVerticalBar().getSelection() / caretHeight + 1;
                int end_line = compositeText[0].getLineCount() - 1;

                for (int y = y_offset; y < height; y += caretHeight) { // Font.MONOSPACE_HEIGHT) {

                    if (NLogger.DEBUG && Project.getFileToEdit() != null) {

                        // TODO DEBUG Emergency reference debugging

                        StringBuilder sb = new StringBuilder();
                        sb.append(start_line);
                        sb.append(" "); //$NON-NLS-1$
                        GData source = Project.getFileToEdit().getDrawPerLine().getValue(start_line);
                        if (source != null) {
                            if (source.getBefore() != null) {
                                sb.append(source.getBefore().hashCode());
                                sb.append(" -> "); //$NON-NLS-1$
                            } else {
                                sb.append("null"); //$NON-NLS-1$
                                sb.append(" -> "); //$NON-NLS-1$
                            }
                            sb.append(source.hashCode());
                            sb.append(" -> "); //$NON-NLS-1$
                            source = Project.getFileToEdit().getDrawPerLine().getValue(start_line).getNext();
                            if (source != null) {
                                sb.append(source.hashCode());
                            } else {
                                sb.append("null"); //$NON-NLS-1$
                            }
                        } else {
                            sb.append("null"); //$NON-NLS-1$
                        }
                        e.gc.drawText(sb.toString(), 0, y);
                    } else {
                        e.gc.drawText(Integer.toString(start_line), 0, y);
                    }

                    if (start_line > end_line) {
                        break;
                    }
                    start_line++;
                }

            }
        });
        compositeText[0].getVerticalBar().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!isDisposed()) {
                    canvas_lineNumberArea[0].redraw();
                    getDisplay().update();
                }
            }
        });
        mntm_Delete[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                state.folder[0].delete();
            }
        });
        mntm_Copy[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                state.folder[0].copy();
            }
        });
        mntm_Cut[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                state.folder[0].cut();
            }
        });
        mntm_Paste[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                state.folder[0].paste();
            }
        });
    }

    /**
     * Sets the current window of this tab
     * @param cTabFolder TODO
     * @param textEditorWindow
     *            the window to set.
     */
    public void setFolderAndWindow(CompositeTabFolder cTabFolder, ApplicationWindow textEditorWindow) {
        this.state.window[0] = textEditorWindow;
        this.state.folder[0] = cTabFolder;
    }

    /**
     * Shows or hides the error tab
     */
    public void toggleErrorTabVisibility() {
        setErrorTabVisibility(!tabFolder_partInformation[0].isVisible());
        compositeContainer[0].layout();
    }

    /**
     * Shows or hides the error tab
     *
     * @param isVisible
     *            {@code true} when the tab is shown
     */
    public void setErrorTabVisibility(boolean isVisible) {
        tabFolder_partInformation[0].setVisible(isVisible);
        if (isVisible) {
            sashForm[0].setWeights(new int[] { compositeContainer[0].getSize().y, compositeContainer[0].getSize().y / 2 });
        } else {
            sashForm[0].setWeights(new int[] { compositeContainer[0].getSize().y, 1 });
        }
    }

    /**
     * Restores the state of this tab (necessary if the tab was disposed before)
     *
     * @param state
     */
    public void restoreState(CompositeTabState state) {
        this.state = state;
        this.state.setTab(this);
    }

    /**
     * @return The state of this tab
     */
    public CompositeTabState getState() {
        return this.state;
    }

    /**
     * Moves the tab to a new index position within another window
     *
     * @param folder
     *            the new tab folder
     * @param index
     *            the new index
     * @return the new tab instance
     */
    public CompositeTab moveToFolder(CompositeTabFolder folder, int index) {

        if (this.state.getTab().getParent().equals(folder)) {
            int index2 = 0;
            for (CTabItem t : folder.getItems()) {
                if (((CompositeTab) t).getState().getFileNameObj().equals(state.getFileNameObj()))
                    break;
                index2++;
            }
            if (index == index2)
                return this.state.getTab();
        }
        if (index != 0) {
            if (!this.state.getTab().getParent().equals(folder))
                index--;
        }
        final CompositeTab ct = new CompositeTab(folder, SWT.CLOSE, index);
        ct.setText(this.state.getTab().getText());
        ct.getControl().dispose();
        ct.canvas_lineNumberArea[0] = this.canvas_lineNumberArea[0];
        ct.compositeText[0] = this.compositeText[0];
        ct.compositeContainer[0] = this.compositeContainer[0];
        ct.sashForm[0] = this.sashForm[0];
        ct.tabFolder_partInformation[0] = this.tabFolder_partInformation[0];
        ct.tree_Problems[0] = this.tree_Problems[0];
        ct.treeItem_Hints[0] = this.treeItem_Hints[0];
        ct.treeItem_Warnings[0] = this.treeItem_Warnings[0];
        ct.treeItem_Errors[0] = this.treeItem_Errors[0];
        ct.treeItem_Duplicates[0] = this.treeItem_Duplicates[0];
        ct.btn_QuickFix[0] = this.btn_QuickFix[0];
        ct.btn_QuickFixSame[0] = this.btn_QuickFixSame[0];
        ct.lbl_ProblemCount[0] = this.lbl_ProblemCount[0];
        try {
            ct.setControl(this.state.getTab().getControl());
        } catch (IllegalArgumentException e) {
            this.state.getTab().getControl().setParent(folder);
            ct.setControl(this.state.getTab().getControl());
        }
        this.state.getTab().dispose();
        ct.restoreState(state);
        ct.setFolderAndWindow(folder, folder.getWindow());
        return ct;
    }

    public StyledText getTextComposite() {
        return this.state.getTab().compositeText[0];
    }

    public void parseForErrorAndHints() {
        this.state.getFileNameObj().parseForError(getTextComposite(), 0, getTextComposite().getText().length(), getTextComposite().getText().length(), getTextComposite().getText(), getTextComposite().getText(), treeItem_Hints[0], treeItem_Warnings[0], treeItem_Errors[0], treeItem_Duplicates[0], true);
        int errorCount = treeItem_Errors[0].getItems().size();
        int warningCount = treeItem_Warnings[0].getItems().size();
        int hintCount = treeItem_Hints[0].getItems().size();
        int duplicateCount = treeItem_Duplicates[0].getItems().size();
        String errors = errorCount == 1 ? I18n.EDITORTEXT_Error : I18n.EDITORTEXT_Errors;
        String warnings = warningCount == 1 ? I18n.EDITORTEXT_Warning : I18n.EDITORTEXT_Warnings;
        String hints = hintCount == 1 ? I18n.EDITORTEXT_Other : I18n.EDITORTEXT_Others;
        String duplicates = duplicateCount == 1 ? I18n.EDITORTEXT_Duplicate : I18n.EDITORTEXT_Duplicates;
        lbl_ProblemCount[0].setText(errorCount + " " + errors + ", " + warningCount + " " + warnings + ", " + hintCount + " " + hints + ", " + duplicateCount + " " + duplicates); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
        lbl_ProblemCount[0].getParent().layout();
    }

    public void updateColours() {
        this.state.getTab().compositeText[0].setBackground(Colour.text_background[0]);
        this.state.getTab().compositeText[0].setForeground(Colour.text_foreground[0]);
        this.state.getTab().compositeText[0].redrawRange(0, this.state.getTab().compositeText[0].getText().length(), false);
    }

}
