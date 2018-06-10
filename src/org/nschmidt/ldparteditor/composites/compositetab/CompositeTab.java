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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

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
import org.eclipse.swt.custom.MovementEvent;
import org.eclipse.swt.custom.MovementListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GData2;
import org.nschmidt.ldparteditor.data.GData3;
import org.nschmidt.ldparteditor.data.GDataCSG;
import org.nschmidt.ldparteditor.data.Rounder;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.data.VertexInfo;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.dialogs.round.RoundDialog;
import org.nschmidt.ldparteditor.dnd.MyDummyTransfer2;
import org.nschmidt.ldparteditor.enums.Colour;
import org.nschmidt.ldparteditor.enums.Font;
import org.nschmidt.ldparteditor.enums.OpenInWhat;
import org.nschmidt.ldparteditor.enums.TextTask;
import org.nschmidt.ldparteditor.helpers.composite3d.GuiStatusManager;
import org.nschmidt.ldparteditor.helpers.composite3d.SelectorSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.ViewIdleManager;
import org.nschmidt.ldparteditor.helpers.compositetext.Inliner;
import org.nschmidt.ldparteditor.helpers.compositetext.Inspector;
import org.nschmidt.ldparteditor.helpers.compositetext.QuickFixer;
import org.nschmidt.ldparteditor.helpers.compositetext.Text2SelectionConverter;
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
            dt.setTransfer(new Transfer[] { MyDummyTransfer2.getInstance(), FileTransfer.getInstance() });
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
                        return;
                    }

                    final DatFile datfile = state.getFileNameObj();
                    if (datfile.isReadOnly()) {
                        return;
                    }
                    NLogger.debug(getClass(), "Primitive dropped."); //$NON-NLS-1$
                    final Editor3DWindow window = Editor3DWindow.getWindow();
                    final org.nschmidt.ldparteditor.data.Primitive p = window.getCompositePrimitive().getSelectedPrimitive();
                    final StyledText st = getTextComposite();
                    if (p == null || p.isCategory() || datfile.isReadOnly()) return;
                    NLogger.debug(getClass(), "Primitive: {0}", p); //$NON-NLS-1$
                    String ref = p.getName();
                    final int lineNr = st.getLineAtOffset(st.getSelection().x);
                    final boolean lastLine = lineNr + 1 == st.getLineCount();
                    int start = st.getOffsetAtLine(lineNr);
                    if (lastLine) {
                        start = st.getText().length();
                        st.setSelection(start);
                        st.insert(StringHelper.getLineDelimiter() + "1 16 0 0 0 1 0 0 0 1 0 0 0 1 " + ref); //$NON-NLS-1$
                        st.setSelection(st.getText().length());
                    } else {
                        st.setSelection(start);
                        String newPrimitive = "1 16 0 0 0 1 0 0 0 1 0 0 0 1 " + ref + StringHelper.getLineDelimiter(); //$NON-NLS-1$
                        st.insert(newPrimitive);
                        st.setSelection(start + newPrimitive.length());
                    }
                    st.redraw(0, 0, st.getBounds().width, st.getBounds().height, true);
                    st.forceFocus();
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

                if (data != null && data.type() == 0) {
                    Set<VertexInfo> vis = vm.getLineLinkedToVertices().get(data);
                    if (vis != null) {
                        for (VertexInfo vi : vis) {
                            if (vm.getSelectedVertices().contains(vi.getVertex())) {
                                isSelected = true;
                                break;
                            }
                        }
                    }
                }

                isSelected = isSelected || vm.isSyncWithTextEditor() && GDataCSG.getSelection(df).contains(data);
                syntaxFormatter.format(e,
                        state.getToReplaceX(), state.getToReplaceY(), state.getToReplaceZ(),
                        state.getReplaceEpsilon(), state.isReplacingVertex(), isSelected, GData.CACHE_duplicates.containsKey(data), data == null || data.isVisible(),  df);
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
                        final boolean isDistanceOrProtractor = type == 2 && !((GData2) dataInLine).isLine
                                || type == 3 && !((GData3) dataInLine).isTriangle;

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
                            int index2 = StringHelper.getIndexFromWhitespaces(oldLine, state.currentCaretPositionChar) - (isDistanceOrProtractor ? 2 : 0);
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
                                index2 = StringHelper.getIndexFromWhitespaces(oldLine, state.currentCaretPositionChar - 1) - (isDistanceOrProtractor ? 2 : 0);
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
                                index2 = StringHelper.getIndexFromWhitespaces(oldLine, state.currentCaretPositionChar + 1) - (isDistanceOrProtractor ? 2 : 0);
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
                                newLine = oldLine.substring(0, Math.max(0, event.start - off)) + event.text + oldLine.substring(Math.max(0, event.end - off));
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
                                    index2 = StringHelper.getIndexFromWhitespaces(newLine, state.currentCaretPositionChar) - (isDistanceOrProtractor ? 2 : 0);
                                    if (index2 > 0) {
                                        if (type > 3 && index2 > 10) {
                                            x = new BigDecimal(new_data_segments[11]);
                                            y = new BigDecimal(new_data_segments[12]);
                                            z = new BigDecimal(new_data_segments[13]);
                                            foundValidVertex = true;
                                        } else if (type > 2 && index2 > 7) {
                                            x = new BigDecimal(new_data_segments[8 + (isDistanceOrProtractor ? 2 : 0)]);
                                            y = new BigDecimal(new_data_segments[9 + (isDistanceOrProtractor ? 2 : 0)]);
                                            z = new BigDecimal(new_data_segments[10 + (isDistanceOrProtractor ? 2 : 0)]);
                                            foundValidVertex = true;
                                        } else if (index2 > 4) {
                                            x = new BigDecimal(new_data_segments[5 + (isDistanceOrProtractor ? 2 : 0)]);
                                            y = new BigDecimal(new_data_segments[6 + (isDistanceOrProtractor ? 2 : 0)]);
                                            z = new BigDecimal(new_data_segments[7 + (isDistanceOrProtractor ? 2 : 0)]);
                                            foundValidVertex = true;
                                        } else {
                                            x = new BigDecimal(new_data_segments[2 + (isDistanceOrProtractor ? 2 : 0)]);
                                            y = new BigDecimal(new_data_segments[3 + (isDistanceOrProtractor ? 2 : 0)]);
                                            z = new BigDecimal(new_data_segments[4 + (isDistanceOrProtractor ? 2 : 0)]);
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
                                            index2 = StringHelper.getIndexFromWhitespaces(newLine, state.currentCaretPositionChar + 1) - (isDistanceOrProtractor ? 2 : 0);
                                            if (index2 > 1) {
                                                if (type > 3 && index2 > 10) {
                                                    x = new BigDecimal(new_data_segments[11]);
                                                    y = new BigDecimal(new_data_segments[12]);
                                                    z = new BigDecimal(new_data_segments[13]);
                                                    foundValidVertex = true;
                                                } else if (type > 2 && index2 > 7) {
                                                    x = new BigDecimal(new_data_segments[8 + (isDistanceOrProtractor ? 2 : 0)]);
                                                    y = new BigDecimal(new_data_segments[9 + (isDistanceOrProtractor ? 2 : 0)]);
                                                    z = new BigDecimal(new_data_segments[10 + (isDistanceOrProtractor ? 2 : 0)]);
                                                    foundValidVertex = true;
                                                } else if (index2 > 4) {
                                                    x = new BigDecimal(new_data_segments[5 + (isDistanceOrProtractor ? 2 : 0)]);
                                                    y = new BigDecimal(new_data_segments[6 + (isDistanceOrProtractor ? 2 : 0)]);
                                                    z = new BigDecimal(new_data_segments[7 + (isDistanceOrProtractor ? 2 : 0)]);
                                                    foundValidVertex = true;
                                                } else {
                                                    x = new BigDecimal(new_data_segments[2 + (isDistanceOrProtractor ? 2 : 0)]);
                                                    y = new BigDecimal(new_data_segments[3 + (isDistanceOrProtractor ? 2 : 0)]);
                                                    z = new BigDecimal(new_data_segments[4 + (isDistanceOrProtractor ? 2 : 0)]);
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
                                                    index2 = StringHelper.getIndexFromWhitespaces(newLine, state.currentCaretPositionChar - 1) - (isDistanceOrProtractor ? 2 : 0);
                                                    if (index2 > 1) {
                                                        if (type > 3 && index2 > 10) {
                                                            x = new BigDecimal(new_data_segments[11]);
                                                            y = new BigDecimal(new_data_segments[12]);
                                                            z = new BigDecimal(new_data_segments[13]);
                                                            foundValidVertex = true;
                                                        } else if (type > 2 && index2 > 7) {
                                                            x = new BigDecimal(new_data_segments[8 + (isDistanceOrProtractor ? 2 : 0)]);
                                                            y = new BigDecimal(new_data_segments[9 + (isDistanceOrProtractor ? 2 : 0)]);
                                                            z = new BigDecimal(new_data_segments[10 + (isDistanceOrProtractor ? 2 : 0)]);
                                                            foundValidVertex = true;
                                                        } else if (index2 > 4) {
                                                            x = new BigDecimal(new_data_segments[5 + (isDistanceOrProtractor ? 2 : 0)]);
                                                            y = new BigDecimal(new_data_segments[6 + (isDistanceOrProtractor ? 2 : 0)]);
                                                            z = new BigDecimal(new_data_segments[7 + (isDistanceOrProtractor ? 2 : 0)]);
                                                            foundValidVertex = true;
                                                        } else {
                                                            x = new BigDecimal(new_data_segments[2 + (isDistanceOrProtractor ? 2 : 0)]);
                                                            y = new BigDecimal(new_data_segments[3 + (isDistanceOrProtractor ? 2 : 0)]);
                                                            z = new BigDecimal(new_data_segments[4 + (isDistanceOrProtractor ? 2 : 0)]);
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
                        GuiStatusManager.updateStatus();
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
                            treeItem_Errors[0], treeItem_Duplicates[0], lbl_ProblemCount[0], false);
                    vm.setModified(false, true);
                } else {
                    if (!vm.isModified()) {
                        Display.getCurrent().syncExec(new Runnable() {
                            @Override
                            public void run() {
                                state.getFileNameObj().parseForErrorAndData(compositeText[0], event.start, off, event.length, insertedText, event.replacedText, treeItem_Hints[0], treeItem_Warnings[0],
                                        treeItem_Errors[0], treeItem_Duplicates[0], lbl_ProblemCount[0]);
                            }
                        });
                    } else {
                        vm.setModified(false, true);
                        GData.CACHE_warningsAndErrors.clear();
                        state.getFileNameObj().parseForError(compositeText[0], event.start, off, event.length, insertedText, event.replacedText, treeItem_Hints[0], treeItem_Warnings[0],
                                treeItem_Errors[0], treeItem_Duplicates[0], lbl_ProblemCount[0], true);
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
        compositeText[0].addListener(SWT.KeyDown, event -> {

            final DatFile df = state.getFileNameObj();
            final VertexManager vm = df.getVertexManager();

            vm.addSnapshot();

            final int keyCode = event.keyCode;
            final boolean ctrlPressed = (event.stateMask & SWT.CTRL) != 0;
            final boolean altPressed = (event.stateMask & SWT.ALT) != 0;
            final boolean shiftPressed = (event.stateMask & SWT.SHIFT) != 0;
            final boolean cmdPressed = (event.stateMask & SWT.COMMAND) != 0;
            StringBuilder sb = new StringBuilder();
            sb.append(keyCode);
            sb.append(ctrlPressed ? "+Ctrl" : ""); //$NON-NLS-1$//$NON-NLS-2$
            sb.append(altPressed ? "+Alt" : ""); //$NON-NLS-1$//$NON-NLS-2$
            sb.append(shiftPressed ? "+Shift" : ""); //$NON-NLS-1$//$NON-NLS-2$
            sb.append(cmdPressed ? "+Cmd" : ""); //$NON-NLS-1$//$NON-NLS-2$
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
                        for (TreeItem t1 : treeItem_Hints[0].getItems()) {
                            if (!t1.getText(0).isEmpty() && ((Integer) t1.getData()).intValue() == offset) {
                                NLogger.debug(getClass(), "Found hint at {0}", t1.getText(1)); //$NON-NLS-1$
                                items.add(t1);
                            }
                        }
                        for (TreeItem t2 : treeItem_Warnings[0].getItems()) {
                            if (!t2.getText(0).isEmpty() && ((Integer) t2.getData()).intValue() == offset) {
                                NLogger.debug(getClass(), "Found warning at {0}", t2.getText(1)); //$NON-NLS-1$
                                items.add(t2);
                            }
                        }
                        for (TreeItem t3 : treeItem_Errors[0].getItems()) {
                            if (!t3.getText(0).isEmpty() && ((Integer) t3.getData()).intValue() == offset) {
                                NLogger.debug(getClass(), "Found error at {0}", t3.getText(1)); //$NON-NLS-1$
                                items.add(t3);
                            }
                        }
                        for (TreeItem t4 : treeItem_Duplicates[0].getItems()) {
                            if (!t4.getText(0).isEmpty() && ((Integer) t4.getData()).intValue() == offset) {
                                NLogger.debug(getClass(), "Found duplicate at {0}", t4.getText(1)); //$NON-NLS-1$
                                items.add(t4);
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
                    final StyledText st1 = compositeText[0];
                    int s11 = st1.getSelectionRange().x;
                    int s21 = s11 + st1.getSelectionRange().y;
                    int fromLine1 = s11 > -1 ? st1.getLineAtOffset(s11) : s11 * -1;
                    int toLine1 = s21 > -1 ? st1.getLineAtOffset(s21) : s21 * -1;
                    fromLine1++;
                    toLine1++;
                    Inliner.withSubfileReference = false;
                    Inliner.recursively = false;
                    Inliner.noComment = false;
                    NLogger.debug(getClass(), "From line {0}", fromLine1); //$NON-NLS-1$
                    NLogger.debug(getClass(), "To   line {0}", toLine1); //$NON-NLS-1$
                    Inliner.inline(st1, fromLine1, toLine1, df);
                    st1.forceFocus();
                    break;
                }
                case EDITORTEXT_ROUND:
                {
                    if (!vm.isUpdated()) return;
                    if (new RoundDialog(compositeText[0].getShell()).open() == IDialogConstants.CANCEL_ID) return;
                    NLogger.debug(getClass(), "Rounding.."); //$NON-NLS-1$
                    final StyledText st2 = compositeText[0];
                    int s12 = st2.getSelectionRange().x;
                    int s22 = s12 + st2.getSelectionRange().y;
                    int fromLine2 = s12 > -1 ? st2.getLineAtOffset(s12) : s12 * -1;
                    int toLine2 = s22 > -1 ? st2.getLineAtOffset(s22) : s22 * -1;
                    fromLine2++;
                    toLine2++;
                    NLogger.debug(getClass(), "From line {0}", fromLine2); //$NON-NLS-1$
                    NLogger.debug(getClass(), "To   line {0}", toLine2); //$NON-NLS-1$
                    Rounder.round(state, st2, fromLine2, toLine2, df);
                    st2.forceFocus();
                    break;
                }
                case EDITORTEXT_REDO:
                {
                    final Shell sh1 = compositeText[0].getDisplay().getActiveShell();
                    if (vm.isUpdated() && sh1 != null) df.redo(sh1, true);
                }
                break;
                case EDITORTEXT_UNDO:
                {
                    final Shell sh2 = compositeText[0].getDisplay().getActiveShell();
                    if (vm.isUpdated() && sh2 != null) df.undo(sh2, true);
                }
                break;
                case EDITORTEXT_SAVE:
                    if (!df.isReadOnly()) {
                        final Shell sh3 = compositeText[0].getDisplay().getActiveShell();
                        if (df.save()) {
                            Editor3DWindow.getWindow().addRecentFile(df);
                            Project.removeUnsavedFile(df);
                            Editor3DWindow.getWindow().updateTree_unsavedEntries();
                        } else if (sh3 != null) {
                            MessageBox messageBoxError = new MessageBox(sh3, SWT.ICON_ERROR | SWT.OK);
                            messageBoxError.setText(I18n.DIALOG_Error);
                            messageBoxError.setMessage(I18n.DIALOG_CantSaveFile);
                            messageBoxError.open();
                        }
                    }
                    break;
                case EDITORTEXT_FIND:
                {
                    final Shell sh4 = compositeText[0].getDisplay().getActiveShell();
                    if (!vm.isUpdated() || sh4 == null) return;
                    NLogger.debug(getClass(), "Find and Replace.."); //$NON-NLS-1$
                    SearchWindow win = Editor3DWindow.getWindow().getSearchWindow();
                    if (win != null) {
                        win.close();
                    }
                    Editor3DWindow.getWindow().setSearchWindow(new SearchWindow(sh4));
                    Editor3DWindow.getWindow().getSearchWindow().run();
                    Editor3DWindow.getWindow().getSearchWindow().setTextComposite(me);
                    Editor3DWindow.getWindow().getSearchWindow().setScopeToAll();
                    break;
                }
                case EDITORTEXT_INSERT_HISTORY:
                {
                    if (!vm.isUpdated() || df.isReadOnly()) return;
                    NLogger.debug(getClass(), "Insert history line.."); //$NON-NLS-1$

                    final StyledText st3 = compositeText[0];
                    int s13 = st3.getSelectionRange().x;
                    int s23 = s13 + st3.getSelectionRange().y;
                    int fromLine3 = s13 > -1 ? st3.getLineAtOffset(s13) : s13 * -1;
                    int toLine3 = s23 > -1 ? st3.getLineAtOffset(s23) : s23 * -1;
                    if (fromLine3 != toLine3) return;
                    String currentLine1 = st3.getLine(fromLine3);
                    fromLine3++;
                    NLogger.debug(getClass(), "Line {0}", fromLine3); //$NON-NLS-1$
                    NLogger.debug(getClass(), currentLine1);

                    final boolean needNewLine1 = StringHelper.isNotBlank(currentLine1);

                    final String username;
                    if (WorkbenchManager.getUserSettingState().getLdrawUserName().trim().isEmpty()) {
                        username = " {" + WorkbenchManager.getUserSettingState().getRealUserName() + "} ";    //$NON-NLS-1$//$NON-NLS-2$
                    } else {
                        username = " [" + WorkbenchManager.getUserSettingState().getLdrawUserName() + "] ";    //$NON-NLS-1$//$NON-NLS-2$
                    }

                    final String historyLine = (needNewLine1 ? StringHelper.getLineDelimiter() : "") + "0 !HISTORY " + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + username; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

                    int delta1 = 0;
                    s13 = compositeText[0].getOffsetAtLine(toLine3);
                    if (needNewLine1) {
                        compositeText[0].setSelection(s13 + currentLine1.length());
                    } else {
                        if (!currentLine1.isEmpty()) {
                            compositeText[0].setSelection(s13, s13 + currentLine1.length());
                            delta1 = historyLine.length();
                        }
                    }
                    compositeText[0].insert(historyLine);
                    compositeText[0].setCaretOffset(compositeText[0].getCaretOffset() + historyLine.length() - delta1);
                    break;
                }
                case EDITORTEXT_INSERT_KEYWORD:
                {
                    if (!vm.isUpdated() || df.isReadOnly()) return;
                    NLogger.debug(getClass(), "Insert keyword line.."); //$NON-NLS-1$

                    final StyledText st4 = compositeText[0];
                    int s14 = st4.getSelectionRange().x;
                    int s24 = s14 + st4.getSelectionRange().y;
                    int fromLine4 = s14 > -1 ? st4.getLineAtOffset(s14) : s14 * -1;
                    int toLine4 = s24 > -1 ? st4.getLineAtOffset(s24) : s24 * -1;
                    if (fromLine4 != toLine4) return;
                    String currentLine2 = st4.getLine(fromLine4);
                    fromLine4++;
                    NLogger.debug(getClass(), "Line {0}", fromLine4); //$NON-NLS-1$
                    NLogger.debug(getClass(), currentLine2);

                    final boolean needNewLine2 = StringHelper.isNotBlank(currentLine2);

                    final String keywordLine = (needNewLine2 ? StringHelper.getLineDelimiter() : "") + "0 !KEYWORDS "; //$NON-NLS-1$ //$NON-NLS-2$

                    int delta2 = 0;
                    s14 = compositeText[0].getOffsetAtLine(toLine4);
                    if (needNewLine2) {
                        compositeText[0].setSelection(s14 + currentLine2.length());
                    } else {
                        if (!currentLine2.isEmpty()) {
                            compositeText[0].setSelection(s14, s14 + currentLine2.length());
                            delta2 = keywordLine.length();
                        }
                    }
                    compositeText[0].insert(keywordLine);
                    compositeText[0].setCaretOffset(compositeText[0].getCaretOffset() + keywordLine.length() - delta2);
                    break;
                }
                case EDITORTEXT_INSERT_REFERENCE:
                {
                    if (!vm.isUpdated() || df.isReadOnly()) return;
                    NLogger.debug(getClass(), "Insert TYPE 1 reference line.."); //$NON-NLS-1$

                    final StyledText st5 = compositeText[0];
                    int s15 = st5.getSelectionRange().x;
                    int s25 = s15 + st5.getSelectionRange().y;
                    int fromLine5 = s15 > -1 ? st5.getLineAtOffset(s15) : s15 * -1;
                    int toLine5 = s25 > -1 ? st5.getLineAtOffset(s25) : s25 * -1;
                    if (fromLine5 != toLine5) return;
                    String currentLine3 = st5.getLine(fromLine5);
                    fromLine5++;
                    NLogger.debug(getClass(), "Line {0}", fromLine5); //$NON-NLS-1$
                    NLogger.debug(getClass(), currentLine3);

                    final boolean needNewLine3 = StringHelper.isNotBlank(currentLine3);

                    final String referenceLine = (needNewLine3 ? StringHelper.getLineDelimiter() : "") + "1 16 0 0 0 1 0 0 0 1 0 0 0 1 "; //$NON-NLS-1$ //$NON-NLS-2$

                    int delta3 = 0;
                    s15 = compositeText[0].getOffsetAtLine(toLine5);
                    if (needNewLine3) {
                        compositeText[0].setSelection(s15 + currentLine3.length());
                    } else {
                        if (!currentLine3.isEmpty()) {
                            compositeText[0].setSelection(s15, s15 + currentLine3.length());
                            delta3 = referenceLine.length();
                        }
                    }
                    compositeText[0].insert(referenceLine);
                    compositeText[0].setCaretOffset(compositeText[0].getCaretOffset() + referenceLine.length() - delta3);
                    break;
                }
                case EDITORTEXT_LINE_UP:
                {
                    if (!vm.isUpdated() || df.isReadOnly()) return;
                    NLogger.debug(getClass(), "Move line up.."); //$NON-NLS-1$

                    final StyledText st6 = compositeText[0];
                    int s16 = st6.getSelectionRange().x;
                    int s26 = s16 + st6.getSelectionRange().y;
                    int fromLine6 = s16 > -1 ? st6.getLineAtOffset(s16) : s16 * -1;
                    int toLine6 = s26 > -1 ? st6.getLineAtOffset(s26) : s26 * -1;
                    fromLine6++;
                    toLine6++;
                    NLogger.debug(getClass(), "From line {0}", fromLine6); //$NON-NLS-1$
                    NLogger.debug(getClass(), "To   line {0}", toLine6); //$NON-NLS-1$

                    if (fromLine6 <= 1) {
                        return;
                    }

                    Clipboard clipboard1 = new Clipboard(Display.getCurrent());
                    String plainText1 = (String)clipboard1.getContents(TextTransfer.getInstance());
                    clipboard1.dispose();

                    int delta4 = st6.getLine(fromLine6 - 2).length();

                    st6.setSelection(st6.getOffsetAtLine(fromLine6 - 1), st6.getOffsetAtLine(toLine6 - 1) + st6.getLine(toLine6 - 1).length());
                    boolean doCutPaste1 = st6.getSelectionCount() > 0;
                    if (doCutPaste1) st6.cut();
                    st6.setSelection(st6.getOffsetAtLine(fromLine6 - 1) - StringHelper.getLineDelimiter().length(), st6.getOffsetAtLine(fromLine6 - 1));
                    st6.insert(""); //$NON-NLS-1$

                    st6.setSelection(st6.getOffsetAtLine(fromLine6 - 2));
                    if (doCutPaste1) st6.paste();
                    st6.insert(StringHelper.getLineDelimiter());
                    st6.setSelectionRange(s16 - delta4 - StringHelper.getLineDelimiter().length(), s26 - s16);

                    if (plainText1 != null) {
                        clipboard1 = new Clipboard(Display.getCurrent());
                        clipboard1.setContents(new Object[] { plainText1 }, new Transfer[] { TextTransfer.getInstance() });
                        clipboard1.dispose();
                    }

                    st6.redraw();
                    break;
                }
                case EDITORTEXT_LINE_DOWN:
                {
                    if (!vm.isUpdated() || df.isReadOnly()) return;
                    NLogger.debug(getClass(), "Move line down.."); //$NON-NLS-1$

                    final StyledText st7 = compositeText[0];
                    int s17 = st7.getSelectionRange().x;
                    int s27 = s17 + st7.getSelectionRange().y;
                    int fromLine7 = s17 > -1 ? st7.getLineAtOffset(s17) : s17 * -1;
                    int toLine7 = s27 > -1 ? st7.getLineAtOffset(s27) : s27 * -1;
                    fromLine7++;
                    toLine7++;
                    NLogger.debug(getClass(), "From line {0}", fromLine7); //$NON-NLS-1$
                    NLogger.debug(getClass(), "To   line {0}", toLine7); //$NON-NLS-1$

                    if (toLine7 >= st7.getLineCount()) {
                        return;
                    }

                    Clipboard clipboard2 = new Clipboard(Display.getCurrent());
                    String plainText2 = (String)clipboard2.getContents(TextTransfer.getInstance());
                    clipboard2.dispose();

                    int delta5 = st7.getLine(toLine7).length();

                    st7.setSelection(st7.getOffsetAtLine(fromLine7 - 1), st7.getOffsetAtLine(toLine7 - 1) + st7.getLine(toLine7 - 1).length());
                    boolean doCutPaste2 = st7.getSelectionCount() > 0;
                    if (doCutPaste2) st7.cut();
                    st7.setSelection(st7.getOffsetAtLine(fromLine7 - 1) - StringHelper.getLineDelimiter().length(), st7.getOffsetAtLine(fromLine7 - 1));
                    st7.insert(""); //$NON-NLS-1$

                    st7.setSelection(st7.getOffsetAtLine(fromLine7 - 1) + st7.getLine(fromLine7 - 1).length());
                    st7.insert(StringHelper.getLineDelimiter());
                    st7.setSelection(st7.getOffsetAtLine(fromLine7 - 1) + st7.getLine(fromLine7 - 1).length() + StringHelper.getLineDelimiter().length());
                    if (doCutPaste2) st7.paste();

                    st7.setSelectionRange(s17 + delta5 + StringHelper.getLineDelimiter().length(), s27 - s17);

                    if (plainText2 != null) {
                        clipboard2 = new Clipboard(Display.getCurrent());
                        clipboard2.setContents(new Object[] { plainText2 }, new Transfer[] { TextTransfer.getInstance() });
                        clipboard2.dispose();
                    }

                    break;
                }
                default:
                    break;
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
                    if (df.updateDuplicatesErrors(compositeText[0], treeItem_Duplicates[0])) {
                        df.getDuplicate().pushDuplicateCheck(df.getDrawChainStart());
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

        compositeText[0].addWordMovementListener(new MovementListener() {
            @Override
            public void getNextOffset (MovementEvent event) {
                boolean ignoreLineBreak = false;
                switch (event.movement) {
                /* This method is called:
                 *   word next (control right-arrow)
                 *   select word next (control shift right-arrow)
                 *   delete next word (control delete)
                 */
                case SWT.MOVEMENT_WORD:
                    ignoreLineBreak = true;

                    /* This method is called:
                     *   double click select word
                     *   double click drag select word
                     */
                case SWT.MOVEMENT_WORD_END:
                    event.newOffset = event.offset;
                    char c = '#';
                    final int len = compositeText[0].getText().length() - 1;
                    while (c != ' ' && (ignoreLineBreak || c != '\n'  && c != '\r') && event.newOffset < len) {
                        event.newOffset++;
                        c = compositeText[0].getText().charAt(event.newOffset);
                    }
                    if (c != ' ' && compositeText[0].getLineAtOffset(event.offset) == compositeText[0].getLineCount() - 1) {
                        event.newOffset = Math.max(len + 1, event.newOffset + 1);
                    }
                    break;
                }
            }

            @Override
            public void getPreviousOffset(MovementEvent event) {
                boolean ignoreLineBreak = false;
                event.newOffset = event.offset;
                switch (event.movement) {
                /* This method is called:
                 *   word previous (control right-arrow)
                 *   select word previous (control shift right-arrow)
                 *   delete previous word (control delete)
                 */
                case SWT.MOVEMENT_WORD:
                    event.newOffset--;
                    ignoreLineBreak = true;
                    /* This method is called:
                     *   double click select word
                     *   double click drag select word
                     */
                case SWT.MOVEMENT_WORD_START:
                    if (!ignoreLineBreak && event.offset == compositeText[0].getOffsetAtLine(compositeText[0].getLineAtOffset(event.offset))) {
                        return;
                    }
                    char c = ignoreLineBreak ? '#' : compositeText[0].getText().charAt(event.newOffset);
                    if (!ignoreLineBreak && event.newOffset > 0 && c == '\n'  || c == '\r') {
                        event.newOffset--;
                        c = '#';
                    }
                    while (c != ' ' && (ignoreLineBreak || c != '\n'  && c != '\r') && event.newOffset > 0) {
                        event.newOffset--;
                        c = compositeText[0].getText().charAt(event.newOffset);
                    }
                    event.newOffset++;
                    while (event.newOffset > 0 && c == '\n' || c == '\r') {
                        event.newOffset--;
                        c = compositeText[0].getText().charAt(event.newOffset);
                    }
                    break;
                }
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
        final SelectionAdapter inspect = new SelectionAdapter() {
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
                        NLogger.debug(getClass(), "+Inspect all hints."); //$NON-NLS-1$
                        items.remove(treeItem_Hints[0]);
                        for (TreeItem sort : treeItem_Hints[0].getItems()) {
                            if (!items.contains(sort))
                                items.add(sort);
                        }
                    }
                    if (items.contains(treeItem_Errors[0])) {
                        NLogger.debug(getClass(), "+Inspect all errors."); //$NON-NLS-1$
                        items.remove(treeItem_Errors[0]);
                        for (TreeItem sort : treeItem_Errors[0].getItems()) {
                            if (!items.contains(sort))
                                items.add(sort);
                        }
                    }
                    if (items.contains(treeItem_Warnings[0])) {
                        NLogger.debug(getClass(), "+Inspect all warnings."); //$NON-NLS-1$
                        items.remove(treeItem_Warnings[0]);
                        for (TreeItem sort : treeItem_Warnings[0].getItems()) {
                            if (!items.contains(sort))
                                items.add(sort);
                        }
                    }
                    if (items.contains(treeItem_Duplicates[0])) {
                        NLogger.debug(getClass(), "+Inspect all duplicates."); //$NON-NLS-1$
                        items.remove(treeItem_Duplicates[0]);
                        for (TreeItem sort : treeItem_Duplicates[0].getItems()) {
                            if (!items.contains(sort))
                                items.add(sort);
                        }
                    }

                    for (TreeItem issue : items) {
                        if (issue != null && issue.getData() != null) {
                            NLogger.debug(getClass(), "+Inspect {0}", issue.getText(1)); //$NON-NLS-1$
                        }
                    }

                    Inspector.inspectTextIssues(compositeText[0], items, getState().getFileNameObj());
                }
            }
        };
        final SelectionAdapter inspectSame = new SelectionAdapter() {
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
                        NLogger.debug(getClass(), "+Inspect all hints."); //$NON-NLS-1$
                        items.remove(treeItem_Hints[0]);
                        for (TreeItem sort : treeItem_Hints[0].getItems()) {
                            if (!items.contains(sort))
                                items.add(sort);
                        }
                    }
                    if (items.contains(treeItem_Errors[0])) {
                        NLogger.debug(getClass(), "+Inspect all errors."); //$NON-NLS-1$
                        items.remove(treeItem_Errors[0]);
                        for (TreeItem sort : treeItem_Errors[0].getItems()) {
                            if (!items.contains(sort))
                                items.add(sort);
                        }
                    }
                    if (items.contains(treeItem_Warnings[0])) {
                        NLogger.debug(getClass(), "+Inspect all warnings."); //$NON-NLS-1$
                        items.remove(treeItem_Warnings[0]);
                        for (TreeItem sort : treeItem_Warnings[0].getItems()) {
                            if (!items.contains(sort))
                                items.add(sort);
                        }
                    }
                    if (items.contains(treeItem_Duplicates[0])) {
                        NLogger.debug(getClass(), "+Inspect all duplicates."); //$NON-NLS-1$
                        items.remove(treeItem_Duplicates[0]);
                        for (TreeItem sort : treeItem_Duplicates[0].getItems()) {
                            if (!items.contains(sort))
                                items.add(sort);
                        }
                    }

                    for (TreeItem issue : items) {
                        if (issue.getData() != null) {
                            NLogger.debug(getClass(), "+Inspect {0}", issue.getText(1)); //$NON-NLS-1$
                        }
                    }

                    Inspector.inspectTextIssues(compositeText[0], items, getState().getFileNameObj());
                }
            }
        };
        mntm_QuickFix[0].addSelectionListener(quickFix);
        mntm_QuickFixSame[0].addSelectionListener(quickFixSame);
        btn_QuickFix[0].addSelectionListener(quickFix);
        btn_QuickFixSame[0].addSelectionListener(quickFixSame);
        mntm_Inspect[0].addSelectionListener(inspect);
        mntm_InspectSame[0].addSelectionListener(inspectSame);
        btn_Inspect[0].addSelectionListener(inspect);
        btn_InspectSame[0].addSelectionListener(inspectSame);

        tree_Problems[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                boolean enabled = tree_Problems[0].getSelectionCount() == 1 && tree_Problems[0].getSelection()[0] != null;
                btn_Inspect[0].setEnabled(enabled);
                btn_InspectSame[0].setEnabled(enabled);
                btn_QuickFix[0].setEnabled(enabled);
                btn_QuickFixSame[0].setEnabled(enabled);
            }
        });
        tree_Problems[0].addListener(SWT.MouseDoubleClick, e -> {
            final TreeItem[] selection = tree_Problems[0].getSelection();
            final TreeItem sel;
            if (selection.length == 1 && (sel = selection[0]) != null) {
                final Integer pos = (Integer) sel.getData();
                if (pos != null) {
                    compositeText[0].setSelection(Math.max(0, pos));
                }
                if (sel.getParentItem() == null) {
                    sel.setVisible(!sel.isVisible());
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

        mntm_DrawSelection[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!state.getFileNameObj().getVertexManager().isUpdated()){
                    return;
                }
                final DatFile df = state.getFileNameObj();
                final VertexManager vm = df.getVertexManager();
                vm.addSnapshot();
                vm.showAll();
                final StyledText st = getTextComposite();
                int s1 = st.getSelectionRange().x;
                int s2 = s1 + st.getSelectionRange().y;
                int fromLine = s1 > -1 ? st.getLineAtOffset(s1) : s1 * -1;
                int toLine = s2 > -1 ? st.getLineAtOffset(s2) : s2 * -1;
                fromLine++;
                toLine++;
                Text2SelectionConverter.convert(fromLine, toLine, df);
                vm.selectInverse(new SelectorSettings());
                vm.hideSelection();
                Text2SelectionConverter.convert(fromLine, toLine, df);
                df.addHistory();
                st.redraw(0, 0, st.getBounds().width, st.getBounds().height, true);
                st.forceFocus();
            }
        });

        mntm_DrawUntilSelection[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!state.getFileNameObj().getVertexManager().isUpdated()){
                    return;
                }
                final DatFile df = state.getFileNameObj();
                final VertexManager vm = df.getVertexManager();
                vm.addSnapshot();
                vm.showAll();
                final StyledText st = getTextComposite();
                int s1 = st.getSelectionRange().x;
                int fromLine = s1 > -1 ? st.getLineAtOffset(s1) : s1 * -1;
                int toLine = st.getLineCount();
                fromLine++;
                if (Math.min(fromLine, toLine) != toLine) {
                    Text2SelectionConverter.convert(Math.min(fromLine + 1, toLine), toLine, df);
                    vm.hideSelection();
                }
                Text2SelectionConverter.convert(1, fromLine, df);
                df.addHistory();
                st.redraw(0, 0, st.getBounds().width, st.getBounds().height, true);
                st.forceFocus();
            }
        });

        mntm_HideSelection[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                hideSelection();
            }
        });

        mntm_ShowSelection[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                showSelection();
            }
        });

        mntm_ShowAll[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!state.getFileNameObj().getVertexManager().isUpdated()){
                    return;
                }
                final DatFile df = state.getFileNameObj();
                final VertexManager vm = df.getVertexManager();
                final StyledText st = getTextComposite();
                vm.addSnapshot();
                vm.showAll();
                df.addHistory();
                st.redraw(0, 0, st.getBounds().width, st.getBounds().height, true);
                st.forceFocus();
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
        ct.btn_Inspect[0] = this.btn_Inspect[0];
        ct.btn_InspectSame[0] = this.btn_InspectSame[0];
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
        this.state.getFileNameObj().parseForError(getTextComposite(), 0, getTextComposite().getText().length(), getTextComposite().getText().length(), getTextComposite().getText(), getTextComposite().getText(), treeItem_Hints[0], treeItem_Warnings[0], treeItem_Errors[0], treeItem_Duplicates[0], lbl_ProblemCount[0], true);
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

    public void hideSelection() {
        if (!state.getFileNameObj().getVertexManager().isUpdated()){
            return;
        }
        final DatFile df = state.getFileNameObj();
        final VertexManager vm = df.getVertexManager();
        vm.addSnapshot();
        final StyledText st = getTextComposite();
        int s1 = st.getSelectionRange().x;
        int s2 = s1 + st.getSelectionRange().y;
        int fromLine = s1 > -1 ? st.getLineAtOffset(s1) : s1 * -1;
        int toLine = s2 > -1 ? st.getLineAtOffset(s2) : s2 * -1;
        fromLine++;
        toLine++;
        Text2SelectionConverter.convert(fromLine, toLine, df);
        vm.hideSelection();
        df.addHistory();
        st.redraw(0, 0, st.getBounds().width, st.getBounds().height, true);
        st.forceFocus();
    }

    public void showSelection() {
        if (!state.getFileNameObj().getVertexManager().isUpdated()){
            return;
        }
        final DatFile df = state.getFileNameObj();
        final VertexManager vm = df.getVertexManager();
        vm.addSnapshot();
        final StyledText st = getTextComposite();
        int s1 = st.getSelectionRange().x;
        int s2 = s1 + st.getSelectionRange().y;
        int fromLine = s1 > -1 ? st.getLineAtOffset(s1) : s1 * -1;
        int toLine = s2 > -1 ? st.getLineAtOffset(s2) : s2 * -1;
        fromLine++;
        toLine++;
        Text2SelectionConverter.convert(fromLine, toLine, df);
        vm.showSelection();
        df.addHistory();
        st.redraw(0, 0, st.getBounds().width, st.getBounds().height, true);
        st.forceFocus();
    }
}
