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

import static org.nschmidt.ldparteditor.helpers.WidgetUtility.widgetUtil;

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
import org.nschmidt.ldparteditor.helpers.WidgetSelectionListener;
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

    private final SyntaxFormatter syntaxFormatter = new SyntaxFormatter(compositeTextPtr[0]);
    private final int caretHeight = compositeTextPtr[0].getCaret().getSize().y;

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
    private CompositeTab(CTabFolder parentFolder, int style, int index) {
        super(parentFolder, style, index);
        initListeners();
    }

    private final void initListeners() {
        // MARK All final listeners will be configured here..
        // Each line should be formatted automagically on text change within a
        // single text line
        this.state.setTab(this);

        {
            DropTarget dt = new DropTarget(compositeTextPtr[0], DND.DROP_DEFAULT | DND.DROP_MOVE );
            dt.setTransfer(new Transfer[] { MyDummyTransfer2.getInstance(), FileTransfer.getInstance() });
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
                                        }
                                        Editor3DWindow.getWindow().updateTreeUnsavedEntries();
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
        compositeTextPtr[0].addLineStyleListener(new LineStyleListener() {
            @Override
            public void lineGetStyle(final LineStyleEvent e) {
                // So the line will be formated with the syntax formatter from
                // the CompositeText.
                final DatFile df = state.getFileNameObj();
                final VertexManager vm = df.getVertexManager();
                final GData data = df.getDrawPerLineNoClone().getValue(compositeTextPtr[0].getLineAtOffset(e.lineOffset) + 1);
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
        compositeTextPtr[0].addVerifyKeyListener(new VerifyKeyListener() {

            @Override
            public void verifyKey(VerifyEvent event) {
                DatFile dat = state.getFileNameObj();
                final VertexManager vm = dat.getVertexManager();
                event.doit = vm.isUpdated();
                isDelPressed[0] = event.keyCode == SWT.DEL;

            }

        });
        compositeTextPtr[0].addVerifyListener(new VerifyListener() {

            @Override
            // only POSSIBLE approach to get this working, VerifyKey has NO use!
            public void verifyText(VerifyEvent event) {
                event.doit = true;
                state.setDoingPaste(event.text.length() > 1 && compositeTextPtr[0].isFocusControl() && state.getFileNameObj().getVertexManager().isUpdated());
                final DatFile dat = state.getFileNameObj();
                final VertexManager vm = dat.getVertexManager();
                if (vm.getVertexToReplace() != null) {
                    if (!vm.isModified() && state.isReplacingVertex()) {
                        // Replaced vertex manipulation check
                        NLogger.debug(getClass(), "Vertex Manipulation is ACTIVE"); //$NON-NLS-1$

                        event.start = compositeTextPtr[0].getSelection().x;
                        event.end = compositeTextPtr[0].getSelection().y;

                        final boolean doReplace = event.start != event.end;
                        boolean foundVertexMetacommand = false;

                        if (doReplace) {
                            NLogger.debug(getClass(), "Did a text replace!"); //$NON-NLS-1$
                        } else {
                            if (event.text.isEmpty()) {
                                NLogger.debug(getClass(), "Did a text deletion!"); //$NON-NLS-1$
                                if (isDelPressed[0])
                                    NLogger.debug(getClass(), "with DEL"); //$NON-NLS-1$

                            }
                        }

                        state.currentCaretPositionLine = compositeTextPtr[0].getLineAtOffset(event.start);
                        state.currentCaretPositionChar = compositeTextPtr[0].getCaretOffset() - compositeTextPtr[0].getOffsetAtLine(state.currentCaretPositionLine);
                        state.currentCaretTopIndex = compositeTextPtr[0].getTopIndex();

                        String oldLine = compositeTextPtr[0].getLine(state.currentCaretPositionLine);
                        String newLine;
                        NLogger.debug(getClass(), "Old Line {0}", oldLine); //$NON-NLS-1$
                        NLogger.debug(getClass(), "Key Char {0}", event.character); //$NON-NLS-1$
                        NLogger.debug(getClass(), "State Mask {0}", event.stateMask); //$NON-NLS-1$
                        NLogger.debug(getClass(), "Key Code {0}", event.keyCode); //$NON-NLS-1$
                        NLogger.debug(getClass(), "Key Location {0}", event.keyLocation); //$NON-NLS-1$
                        NLogger.debug(getClass(), "Text [null] {0}", event.text); //$NON-NLS-1$
                        NLogger.debug(getClass(), "Start {0}", event.start); //$NON-NLS-1$
                        NLogger.debug(getClass(), "End {0}", event.end); //$NON-NLS-1$

                        if (event.text.indexOf(StringHelper.getLineDelimiter()) != -1) {
                            NLogger.debug(getClass(), "Return, because new text contains a line delimiter."); //$NON-NLS-1$
                            return;
                        }

                        GData dataInLine = dat.getDrawPerLine().getValue(state.currentCaretPositionLine + 1);
                        final int type = dataInLine.type();
                        String[] dataSegments = oldLine.trim().split("\\s+"); //$NON-NLS-1$
                        final boolean isDistanceOrProtractor = type == 2 && !((GData2) dataInLine).isLine
                                || type == 3 && !((GData3) dataInLine).isTriangle;

                        Vertex vertexToReplace = null;
                        boolean foundValidVertex = false;
                        switch (type) {
                        case 0:
                            if (dataSegments.length == 6 && "0".equals(dataSegments[0]) && "!LPE".equals(dataSegments[1]) && "VERTEX".equals(dataSegments[2])) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
                                int off = compositeTextPtr[0].getOffsetAtLine(state.currentCaretPositionLine);
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

                            String[] newDataSegments = newLine.trim().split("\\s+"); //$NON-NLS-1$

                            // Parse new coordinates from new line
                            BigDecimal x = null;
                            BigDecimal y = null;
                            BigDecimal z = null;

                            try {
                                switch (type) {
                                case 0:
                                    if (newDataSegments.length == 6 && "0".equals(newDataSegments[0]) && "!LPE".equals(newDataSegments[1]) && "VERTEX".equals(newDataSegments[2])) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                        if (state.currentCaretPositionChar > newLine.indexOf("VERTEX") + 6) { //$NON-NLS-1$
                                            x = new BigDecimal(newDataSegments[3]);
                                            y = new BigDecimal(newDataSegments[4]);
                                            z = new BigDecimal(newDataSegments[5]);
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
                                            x = new BigDecimal(newDataSegments[11]);
                                            y = new BigDecimal(newDataSegments[12]);
                                            z = new BigDecimal(newDataSegments[13]);
                                            foundValidVertex = true;
                                        } else if (type > 2 && index2 > 7) {
                                            x = new BigDecimal(newDataSegments[8 + (isDistanceOrProtractor ? 2 : 0)]);
                                            y = new BigDecimal(newDataSegments[9 + (isDistanceOrProtractor ? 2 : 0)]);
                                            z = new BigDecimal(newDataSegments[10 + (isDistanceOrProtractor ? 2 : 0)]);
                                            foundValidVertex = true;
                                        } else if (index2 > 4) {
                                            x = new BigDecimal(newDataSegments[5 + (isDistanceOrProtractor ? 2 : 0)]);
                                            y = new BigDecimal(newDataSegments[6 + (isDistanceOrProtractor ? 2 : 0)]);
                                            z = new BigDecimal(newDataSegments[7 + (isDistanceOrProtractor ? 2 : 0)]);
                                            foundValidVertex = true;
                                        } else {
                                            x = new BigDecimal(newDataSegments[2 + (isDistanceOrProtractor ? 2 : 0)]);
                                            y = new BigDecimal(newDataSegments[3 + (isDistanceOrProtractor ? 2 : 0)]);
                                            z = new BigDecimal(newDataSegments[4 + (isDistanceOrProtractor ? 2 : 0)]);
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
                                    vm.setModifiedNoSync();
                                    state.currentCaretPositionChar = off2;
                                    compositeTextPtr[0].setText(state.getFileNameObj().getText()); // This has always to be the last line here!

                                } else {
                                    foundValidVertex = false;
                                    try {
                                        switch (type) {
                                        case 0:
                                            if (newDataSegments.length == 6
                                            && "0".equals(newDataSegments[0]) && "!LPE".equals(newDataSegments[1]) && "VERTEX".equals(newDataSegments[2])) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                                if (state.currentCaretPositionChar > newLine.indexOf("VERTEX") + 6) { //$NON-NLS-1$
                                                    x = new BigDecimal(newDataSegments[3]);
                                                    y = new BigDecimal(newDataSegments[4]);
                                                    z = new BigDecimal(newDataSegments[5]);
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
                                                    x = new BigDecimal(newDataSegments[11]);
                                                    y = new BigDecimal(newDataSegments[12]);
                                                    z = new BigDecimal(newDataSegments[13]);
                                                    foundValidVertex = true;
                                                } else if (type > 2 && index2 > 7) {
                                                    x = new BigDecimal(newDataSegments[8 + (isDistanceOrProtractor ? 2 : 0)]);
                                                    y = new BigDecimal(newDataSegments[9 + (isDistanceOrProtractor ? 2 : 0)]);
                                                    z = new BigDecimal(newDataSegments[10 + (isDistanceOrProtractor ? 2 : 0)]);
                                                    foundValidVertex = true;
                                                } else if (index2 > 4) {
                                                    x = new BigDecimal(newDataSegments[5 + (isDistanceOrProtractor ? 2 : 0)]);
                                                    y = new BigDecimal(newDataSegments[6 + (isDistanceOrProtractor ? 2 : 0)]);
                                                    z = new BigDecimal(newDataSegments[7 + (isDistanceOrProtractor ? 2 : 0)]);
                                                    foundValidVertex = true;
                                                } else {
                                                    x = new BigDecimal(newDataSegments[2 + (isDistanceOrProtractor ? 2 : 0)]);
                                                    y = new BigDecimal(newDataSegments[3 + (isDistanceOrProtractor ? 2 : 0)]);
                                                    z = new BigDecimal(newDataSegments[4 + (isDistanceOrProtractor ? 2 : 0)]);
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
                                            vm.setModifiedNoSync();
                                            state.currentCaretPositionChar = off2;
                                            compositeTextPtr[0].setText(state.getFileNameObj().getText()); // This has always to be the last line here!
                                        } else {
                                            foundValidVertex = false;
                                            try {
                                                switch (type) {
                                                case 0:
                                                    if (newDataSegments.length == 6
                                                    && "0".equals(newDataSegments[0]) && "!LPE".equals(newDataSegments[1]) && "VERTEX".equals(newDataSegments[2])) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                                        if (state.currentCaretPositionChar > newLine.indexOf("VERTEX") + 5) { //$NON-NLS-1$
                                                            x = new BigDecimal(newDataSegments[3]);
                                                            y = new BigDecimal(newDataSegments[4]);
                                                            z = new BigDecimal(newDataSegments[5]);
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
                                                            x = new BigDecimal(newDataSegments[11]);
                                                            y = new BigDecimal(newDataSegments[12]);
                                                            z = new BigDecimal(newDataSegments[13]);
                                                            foundValidVertex = true;
                                                        } else if (type > 2 && index2 > 7) {
                                                            x = new BigDecimal(newDataSegments[8 + (isDistanceOrProtractor ? 2 : 0)]);
                                                            y = new BigDecimal(newDataSegments[9 + (isDistanceOrProtractor ? 2 : 0)]);
                                                            z = new BigDecimal(newDataSegments[10 + (isDistanceOrProtractor ? 2 : 0)]);
                                                            foundValidVertex = true;
                                                        } else if (index2 > 4) {
                                                            x = new BigDecimal(newDataSegments[5 + (isDistanceOrProtractor ? 2 : 0)]);
                                                            y = new BigDecimal(newDataSegments[6 + (isDistanceOrProtractor ? 2 : 0)]);
                                                            z = new BigDecimal(newDataSegments[7 + (isDistanceOrProtractor ? 2 : 0)]);
                                                            foundValidVertex = true;
                                                        } else {
                                                            x = new BigDecimal(newDataSegments[2 + (isDistanceOrProtractor ? 2 : 0)]);
                                                            y = new BigDecimal(newDataSegments[3 + (isDistanceOrProtractor ? 2 : 0)]);
                                                            z = new BigDecimal(newDataSegments[4 + (isDistanceOrProtractor ? 2 : 0)]);
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
                                                    vm.setModifiedNoSync();
                                                    state.currentCaretPositionChar = off2;
                                                    compositeTextPtr[0].setText(state.getFileNameObj().getText()); // This has always to be the last line here!
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
        compositeTextPtr[0].addExtendedModifyListener(new ExtendedModifyListener() {
            private int oldLineCount;

            @Override
            public void modifyText(final ExtendedModifyEvent event) {
                final DatFile dat = state.getFileNameObj();
                final VertexManager vm = dat.getVertexManager();

                vm.addSnapshot();

                ViewIdleManager.pause[0].compareAndSet(false, true);

                final String text = compositeTextPtr[0].getText();

                int newLineCount = compositeTextPtr[0].getLineCount();
                if (oldLineCount != newLineCount) {
                    oldLineCount = newLineCount;
                    int numberOfDigits = (int) Math.log10(newLineCount);
                    ((GridData) canvasLineNumberAreaPtr[0].getLayoutData()).widthHint = (numberOfDigits + (NLogger.debugging ? 26 : 2)) * Font.MONOSPACE_WIDTH;
                    canvasLineNumberAreaPtr[0].getParent().layout();
                }
                if (event.length == 0) {
                    // Text deleted
                    canvasLineNumberAreaPtr[0].redraw();
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
                        Editor3DWindow.getWindow().updateTreeUnsavedEntries();
                    }
                } else {
                    state.getTab().setText(state.getFilenameWithStar());
                    if (!Project.getUnsavedFiles().contains(dat)) {
                        Project.addUnsavedFile(dat);
                        Editor3DWindow.getWindow().updateTreeUnsavedEntries();
                    }
                }
                dat.setText(text);
                final int off = event.start + event.length;

                if (!state.isSync()) {
                    boolean doRedraw = !vm.getSelectedData().isEmpty();
                    if (compositeTextPtr[0].isFocusControl()) {
                        vm.clearSelection();
                    }
                    if (doRedraw) {
                        compositeTextPtr[0].redraw();
                    }
                }
                // Reset the caret position when a vertex was modified
                if (vm.getVertexToReplace() != null) {
                    if (vm.isModified()) {
                        try {
                            compositeTextPtr[0].setCaretOffset(compositeTextPtr[0].getOffsetAtLine(state.currentCaretPositionLine) + state.currentCaretPositionChar);
                        } catch (IllegalArgumentException iae) {
                            int diff = 1;
                            int trys = 0;
                            while (true) {
                                try {
                                    compositeTextPtr[0].setCaretOffset(compositeTextPtr[0].getOffsetAtLine(state.currentCaretPositionLine) + state.currentCaretPositionChar - diff);
                                    break;
                                } catch (IllegalArgumentException iae2) {
                                    trys++;
                                    diff++;
                                    if (trys > 10) {
                                        compositeTextPtr[0].setCaretOffset(0);
                                        break;
                                    }
                                }
                            }
                        }
                        compositeTextPtr[0].setTopIndex(state.currentCaretTopIndex);
                        NLogger.debug(getClass(), "Caret Reset"); //$NON-NLS-1$
                        vm.getSelectedVertices().clear();
                        vm.getSelectedVertices().add(vm.getVertexToReplace());
                    }
                }

                if (state.isSync()) {
                    state.getFileNameObj().parseForError(compositeTextPtr[0], event.start, off, event.length, event.replacedText, treeItemHintsPtr[0], treeItemWarningsPtr[0], treeItemErrorsPtr[0],
                            treeItemDuplicatesPtr[0], lblProblemCountPtr[0], false);
                    vm.setModified(false, true);
                } else {
                    if (!vm.isModified()) {
                        Display.getCurrent().syncExec(new Runnable() {
                            @Override
                            public void run() {
                                state.getFileNameObj().parseForErrorAndData(compositeTextPtr[0], event.start, off, event.length, event.replacedText, treeItemHintsPtr[0], treeItemWarningsPtr[0], treeItemErrorsPtr[0],
                                        treeItemDuplicatesPtr[0], lblProblemCountPtr[0]);
                            }
                        });
                    } else {
                        vm.setModified(false, true);
                        GData.CACHE_warningsAndErrors.clear();
                        state.getFileNameObj().parseForError(compositeTextPtr[0], event.start, off, event.length, event.replacedText, treeItemHintsPtr[0], treeItemWarningsPtr[0], treeItemErrorsPtr[0],
                                treeItemDuplicatesPtr[0], lblProblemCountPtr[0], true);
                    }
                    vm.setUpdated(true);
                }
                int errorCount = treeItemErrorsPtr[0].getItems().size();
                int warningCount = treeItemWarningsPtr[0].getItems().size();
                int hintCount = treeItemHintsPtr[0].getItems().size();
                int duplicateCount = treeItemDuplicatesPtr[0].getItems().size();
                String errors = errorCount == 1 ? I18n.EDITORTEXT_ERROR : I18n.EDITORTEXT_ERRORS;
                String warnings = warningCount == 1 ? I18n.EDITORTEXT_WARNING : I18n.EDITORTEXT_WARNINGS;
                String hints = hintCount == 1 ? I18n.EDITORTEXT_OTHER : I18n.EDITORTEXT_OTHERS;
                String duplicates = duplicateCount == 1 ? I18n.EDITORTEXT_DUPLICATE : I18n.EDITORTEXT_DUPLICATES;
                lblProblemCountPtr[0].setText(errorCount + " " + errors + ", " + warningCount + " " + warnings + ", " + hintCount + " " + hints + ", " + duplicateCount + " " + duplicates); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
                lblProblemCountPtr[0].getParent().layout();
            }
        });
        final CompositeTab me = this;
        compositeTextPtr[0].addListener(SWT.KeyDown, event -> {

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
                    if (compositeTextPtr[0].getEditable()) {
                        if (!vm.isUpdated()) return;
                        VertexMarker.markTheVertex(state, compositeTextPtr[0], df);
                        if (state.isReplacingVertex()) {
                            if (state.window[0] == Editor3DWindow.getWindow()) {
                                Editor3DWindow.getStatusLabel().setText(I18n.EDITORTEXT_SYNC_EDIT);
                                Editor3DWindow.getStatusLabel().setSize(Editor3DWindow.getStatusLabel().computeSize(SWT.DEFAULT, SWT.DEFAULT));
                                Editor3DWindow.getStatusLabel().update();
                            } else {
                                state.window[0].setStatus(I18n.EDITORTEXT_SYNC_EDIT);
                            }
                        }
                    }
                    break;
                case EDITORTEXT_ESC:
                    if (compositeTextPtr[0].getEditable()) {
                        if (!vm.isUpdated()) return;
                        state.setReplacingVertex(false);
                        vm.setVertexToReplace(null);
                        compositeTextPtr[0].redraw(0, 0, compositeTextPtr[0].getBounds().width, compositeTextPtr[0].getBounds().height, true);
                        if (state.window[0] == Editor3DWindow.getWindow()) {
                            Editor3DWindow.getStatusLabel().setText(I18n.EDITORTEXT_SYNC_EDIT_DEACTIVATED);
                            Editor3DWindow.getStatusLabel().setSize(Editor3DWindow.getStatusLabel().computeSize(SWT.DEFAULT, SWT.DEFAULT));
                            Editor3DWindow.getStatusLabel().update();
                        } else {
                            state.window[0].setStatus(I18n.EDITORTEXT_SYNC_EDIT_DEACTIVATED);
                        }
                    }
                    break;
                case EDITORTEXT_QUICKFIX:
                    if (compositeTextPtr[0].getEditable()) {
                        if (!vm.isUpdated()) return;
                        HashSet<TreeItem> items = new HashSet<>();
                        int offset = compositeTextPtr[0].getOffsetAtLine(Math.max(Math.min(state.currentLineIndex, compositeTextPtr[0].getLineCount() - 1), 0));
                        for (TreeItem t1 : treeItemHintsPtr[0].getItems()) {
                            if (!t1.getText(0).isEmpty() && ((Integer) t1.getData()).intValue() == offset) {
                                NLogger.debug(getClass(), "Found hint at {0}", t1.getText(1)); //$NON-NLS-1$
                                items.add(t1);
                            }
                        }
                        for (TreeItem t2 : treeItemWarningsPtr[0].getItems()) {
                            if (!t2.getText(0).isEmpty() && ((Integer) t2.getData()).intValue() == offset) {
                                NLogger.debug(getClass(), "Found warning at {0}", t2.getText(1)); //$NON-NLS-1$
                                items.add(t2);
                            }
                        }
                        for (TreeItem t3 : treeItemErrorsPtr[0].getItems()) {
                            if (!t3.getText(0).isEmpty() && ((Integer) t3.getData()).intValue() == offset) {
                                NLogger.debug(getClass(), "Found error at {0}", t3.getText(1)); //$NON-NLS-1$
                                items.add(t3);
                            }
                        }
                        for (TreeItem t4 : treeItemDuplicatesPtr[0].getItems()) {
                            if (!t4.getText(0).isEmpty() && ((Integer) t4.getData()).intValue() == offset) {
                                NLogger.debug(getClass(), "Found duplicate at {0}", t4.getText(1)); //$NON-NLS-1$
                                items.add(t4);
                            }
                        }

                        QuickFixer.fixTextIssues(compositeTextPtr[0], items, df);
                    }
                    break;
                case EDITORTEXT_SELECTALL:
                    compositeTextPtr[0].setSelection(0, compositeTextPtr[0].getText().length());
                    break;
                case EDITORTEXT_INLINE:
                {
                    if (!vm.isUpdated()) return;
                    NLogger.debug(getClass(), "Inlining per action key.."); //$NON-NLS-1$
                    final StyledText st1 = compositeTextPtr[0];
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
                    if (new RoundDialog(compositeTextPtr[0].getShell()).open() == IDialogConstants.CANCEL_ID) return;
                    NLogger.debug(getClass(), "Rounding.."); //$NON-NLS-1$
                    final StyledText st2 = compositeTextPtr[0];
                    int s12 = st2.getSelectionRange().x;
                    int s22 = s12 + st2.getSelectionRange().y;
                    int fromLine2 = s12 > -1 ? st2.getLineAtOffset(s12) : s12 * -1;
                    int toLine2 = s22 > -1 ? st2.getLineAtOffset(s22) : s22 * -1;
                    fromLine2++;
                    toLine2++;
                    NLogger.debug(getClass(), "From line {0}", fromLine2); //$NON-NLS-1$
                    NLogger.debug(getClass(), "To   line {0}", toLine2); //$NON-NLS-1$
                    Rounder.round(state, fromLine2, toLine2, df);
                    st2.forceFocus();
                    break;
                }
                case EDITORTEXT_REDO:
                {
                    if (vm.isUpdated()) df.redo(true);
                }
                break;
                case EDITORTEXT_UNDO:
                {
                    if (vm.isUpdated()) df.undo(true);
                }
                break;
                case EDITORTEXT_SAVE:
                    if (!df.isReadOnly()) {
                        final Shell sh3 = compositeTextPtr[0].getDisplay().getActiveShell();
                        if (df.save()) {
                            Editor3DWindow.getWindow().addRecentFile(df);
                            Project.removeUnsavedFile(df);
                            Editor3DWindow.getWindow().updateTreeUnsavedEntries();
                        } else if (sh3 != null) {
                            MessageBox messageBoxError = new MessageBox(sh3, SWT.ICON_ERROR | SWT.OK);
                            messageBoxError.setText(I18n.DIALOG_ERROR);
                            messageBoxError.setMessage(I18n.DIALOG_CANT_SAVE_FILE);
                            messageBoxError.open();
                        }
                    }
                    break;
                case EDITORTEXT_FIND:
                {
                    final Shell sh4 = compositeTextPtr[0].getDisplay().getActiveShell();
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

                    final StyledText st3 = compositeTextPtr[0];
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
                    s13 = compositeTextPtr[0].getOffsetAtLine(toLine3);
                    if (needNewLine1) {
                        compositeTextPtr[0].setSelection(s13 + currentLine1.length());
                    } else {
                        if (!currentLine1.isEmpty()) {
                            compositeTextPtr[0].setSelection(s13, s13 + currentLine1.length());
                            delta1 = historyLine.length();
                        }
                    }
                    compositeTextPtr[0].insert(historyLine);
                    compositeTextPtr[0].setCaretOffset(compositeTextPtr[0].getCaretOffset() + historyLine.length() - delta1);
                    break;
                }
                case EDITORTEXT_INSERT_KEYWORD:
                {
                    if (!vm.isUpdated() || df.isReadOnly()) return;
                    NLogger.debug(getClass(), "Insert keyword line.."); //$NON-NLS-1$

                    final StyledText st4 = compositeTextPtr[0];
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
                    s14 = compositeTextPtr[0].getOffsetAtLine(toLine4);
                    if (needNewLine2) {
                        compositeTextPtr[0].setSelection(s14 + currentLine2.length());
                    } else {
                        if (!currentLine2.isEmpty()) {
                            compositeTextPtr[0].setSelection(s14, s14 + currentLine2.length());
                            delta2 = keywordLine.length();
                        }
                    }
                    compositeTextPtr[0].insert(keywordLine);
                    compositeTextPtr[0].setCaretOffset(compositeTextPtr[0].getCaretOffset() + keywordLine.length() - delta2);
                    break;
                }
                case EDITORTEXT_INSERT_REFERENCE:
                {
                    if (!vm.isUpdated() || df.isReadOnly()) return;
                    NLogger.debug(getClass(), "Insert TYPE 1 reference line.."); //$NON-NLS-1$

                    final StyledText st5 = compositeTextPtr[0];
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
                    s15 = compositeTextPtr[0].getOffsetAtLine(toLine5);
                    if (needNewLine3) {
                        compositeTextPtr[0].setSelection(s15 + currentLine3.length());
                    } else {
                        if (!currentLine3.isEmpty()) {
                            compositeTextPtr[0].setSelection(s15, s15 + currentLine3.length());
                            delta3 = referenceLine.length();
                        }
                    }
                    compositeTextPtr[0].insert(referenceLine);
                    compositeTextPtr[0].setCaretOffset(compositeTextPtr[0].getCaretOffset() + referenceLine.length() - delta3);
                    break;
                }
                case EDITORTEXT_LINE_UP:
                {
                    if (!vm.isUpdated() || df.isReadOnly()) return;
                    NLogger.debug(getClass(), "Move line up.."); //$NON-NLS-1$

                    final StyledText st6 = compositeTextPtr[0];
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

                    final StyledText st7 = compositeTextPtr[0];
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
        compositeTextPtr[0].addFocusListener(new FocusListener() {

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
                            compositeTextPtr[0].setText(state.getFileNameObj().getText());
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
                if (Editor3DWindow.getWindow().isAddingSomething()) {
                    NLogger.debug(getClass(), "Editor3DWindow.getWindow().disableAddAction()"); //$NON-NLS-1$
                    Editor3DWindow.getWindow().disableAddAction();
                }
            }
        });
        //
        compositeTextPtr[0].addCaretListener(new CaretListener() {
            @Override
            public void caretMoved(CaretEvent event) {
                ViewIdleManager.pause[0].compareAndSet(false, true);
                Point r = compositeTextPtr[0].getSelectionRange();
                state.setOldLineIndex(-1);
                if (!state.isSync()) {
                    DatFile df = state.getFileNameObj();
                    df.addHistory(compositeTextPtr[0].getText(), r.x, r.y, compositeTextPtr[0].getTopIndex());
                    if (df.updateDuplicatesErrors(compositeTextPtr[0], treeItemDuplicatesPtr[0])) {
                        df.getDuplicate().pushDuplicateCheck(df.getDrawChainStart());
                        int errorCount = treeItemErrorsPtr[0].getItems().size();
                        int warningCount = treeItemWarningsPtr[0].getItems().size();
                        int hintCount = treeItemHintsPtr[0].getItems().size();
                        int duplicateCount = treeItemDuplicatesPtr[0].getItems().size();
                        String errors = errorCount == 1 ? I18n.EDITORTEXT_ERROR : I18n.EDITORTEXT_ERRORS;
                        String warnings = warningCount == 1 ? I18n.EDITORTEXT_WARNING : I18n.EDITORTEXT_WARNINGS;
                        String hints = hintCount == 1 ? I18n.EDITORTEXT_OTHER : I18n.EDITORTEXT_OTHERS;
                        String duplicates = duplicateCount == 1 ? I18n.EDITORTEXT_DUPLICATE : I18n.EDITORTEXT_DUPLICATES;
                        lblProblemCountPtr[0].setText(errorCount + " " + errors + ", " + warningCount + " " + warnings + ", " + hintCount + " " + hints + ", " + duplicateCount + " " + duplicates); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
                        treeItemHintsPtr[0].getParent().build();
                        lblProblemCountPtr[0].getParent().layout();
                    }
                }
                try {
                    compositeTextPtr[0].setLineBackground(state.currentLineIndex, 1, compositeTextPtr[0].getBackground());
                } catch (Exception a) {
                }
                int caretOffset = event.caretOffset;
                state.currentLineIndex = compositeTextPtr[0].getLineAtOffset(caretOffset);
                if (compositeTextPtr[0].getSelectionCount() == 0) {
                    try {
                        compositeTextPtr[0].setLineBackground(state.currentLineIndex, 1, Colour.lineHighlightBackground[0]);
                    } catch (Exception a) {
                    }
                }
                if (state.window[0] == Editor3DWindow.getWindow()) {
                    try {
                        if (state.isReplacingVertex()) {
                            Editor3DWindow.getStatusLabel().setText(state.currentLineIndex + 1 + " : " + (caretOffset - compositeTextPtr[0].getOffsetAtLine(state.currentLineIndex) + 1) + "   " + I18n.EDITORTEXT_SYNC_EDIT); //$NON-NLS-1$ //$NON-NLS-2$
                        } else {
                            Editor3DWindow.getStatusLabel().setText(state.currentLineIndex + 1 + " : " + (caretOffset - compositeTextPtr[0].getOffsetAtLine(state.currentLineIndex) + 1)); //$NON-NLS-1$
                        }
                        Editor3DWindow.getStatusLabel().setSize(Editor3DWindow.getStatusLabel().computeSize(SWT.DEFAULT, SWT.DEFAULT));
                        Editor3DWindow.getStatusLabel().update();
                    } catch (Exception a) {
                    }
                } else {
                    try {
                        if (state.isReplacingVertex()) {
                            state.window[0].setStatus(state.currentLineIndex + 1 + " : " + (caretOffset - compositeTextPtr[0].getOffsetAtLine(state.currentLineIndex) + 1) + "   " + I18n.EDITORTEXT_SYNC_EDIT); //$NON-NLS-1$ //$NON-NLS-2$
                        } else {
                            state.window[0].setStatus(state.currentLineIndex + 1 + " : " + (caretOffset - compositeTextPtr[0].getOffsetAtLine(state.currentLineIndex) + 1)); //$NON-NLS-1$
                        }
                    } catch (Exception a) {
                    }
                }
                canvasLineNumberAreaPtr[0].redraw();
            }
        });

        compositeTextPtr[0].addWordMovementListener(new MovementListener() {
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
                    final int len = compositeTextPtr[0].getText().length() - 1;
                    while (c != ' ' && (ignoreLineBreak || c != '\n'  && c != '\r') && event.newOffset < len) {
                        event.newOffset++;
                        c = compositeTextPtr[0].getText().charAt(event.newOffset);
                    }
                    if (c != ' ' && compositeTextPtr[0].getLineAtOffset(event.offset) == compositeTextPtr[0].getLineCount() - 1) {
                        event.newOffset = Math.max(len + 1, event.newOffset + 1);
                    }
                    break;
                default:
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
                    if (!ignoreLineBreak && event.offset == compositeTextPtr[0].getOffsetAtLine(compositeTextPtr[0].getLineAtOffset(event.offset))) {
                        return;
                    }
                    char c = ignoreLineBreak || event.newOffset < 0 ? '#' : compositeTextPtr[0].getText().charAt(event.newOffset);
                    if (!ignoreLineBreak && event.newOffset > 0 && c == '\n'  || c == '\r') {
                        event.newOffset--;
                        c = '#';
                    }
                    while (c != ' ' && (ignoreLineBreak || c != '\n'  && c != '\r') && event.newOffset > 0) {
                        event.newOffset--;
                        c = compositeTextPtr[0].getText().charAt(event.newOffset);
                    }
                    event.newOffset++;
                    while (event.newOffset > 0 && c == '\n' || c == '\r') {
                        event.newOffset--;
                        c = compositeTextPtr[0].getText().charAt(event.newOffset);
                    }
                    break;
                default:
                    break;
                }
            }
        });
        tabFolderPartInformationPtr[0].addCTabFolder2Listener(new CTabFolder2Listener() {
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
                sashFormPtr[0].setWeights(new int[] { compositeTextPtr[0].getSize().y, 1 });
                event.doit = false;
            }
        });
        final WidgetSelectionListener quickFix = e -> {
            if (compositeTextPtr[0].getEditable() && treeProblemsPtr[0].getSelectionCount() > 0) {
                final VertexManager vm = state.getFileNameObj().getVertexManager();
                if (!vm.isUpdated()) return;
                HashSet<TreeItem> items = new HashSet<>();
                for (TreeItem sort1 : treeProblemsPtr[0].getSelection()) {
                    items.add(sort1);
                }
                if (items.contains(treeItemHintsPtr[0])) {
                    NLogger.debug(getClass(), "+Quick fix all hints."); //$NON-NLS-1$
                    items.remove(treeItemHintsPtr[0]);
                    for (TreeItem sort2 : treeItemHintsPtr[0].getItems()) {
                        if (!items.contains(sort2))
                            items.add(sort2);
                    }
                }
                if (items.contains(treeItemErrorsPtr[0])) {
                    NLogger.debug(getClass(), "+Quick fix all errors."); //$NON-NLS-1$
                    items.remove(treeItemErrorsPtr[0]);
                    for (TreeItem sort3 : treeItemErrorsPtr[0].getItems()) {
                        if (!items.contains(sort3))
                            items.add(sort3);
                    }
                }
                if (items.contains(treeItemWarningsPtr[0])) {
                    NLogger.debug(getClass(), "+Quick fix all warnings."); //$NON-NLS-1$
                    items.remove(treeItemWarningsPtr[0]);
                    for (TreeItem sort4 : treeItemWarningsPtr[0].getItems()) {
                        if (!items.contains(sort4))
                            items.add(sort4);
                    }
                }
                if (items.contains(treeItemDuplicatesPtr[0])) {
                    NLogger.debug(getClass(), "+Quick fix all duplicates."); //$NON-NLS-1$
                    items.remove(treeItemDuplicatesPtr[0]);
                    for (TreeItem sort5 : treeItemDuplicatesPtr[0].getItems()) {
                        if (!items.contains(sort5))
                            items.add(sort5);
                    }
                }

                for (TreeItem issue : items) {
                    if (issue != null && issue.getData() != null) {
                        NLogger.debug(getClass(), "+Fix {0}", issue.getText(1)); //$NON-NLS-1$
                    }
                }

                QuickFixer.fixTextIssues(compositeTextPtr[0], items, getState().getFileNameObj());
            }
        };
        final WidgetSelectionListener quickFixSame = e -> {
            if (compositeTextPtr[0].getEditable() && treeProblemsPtr[0].getSelectionCount() > 0) {
                final VertexManager vm = state.getFileNameObj().getVertexManager();
                if (!vm.isUpdated()) return;
                HashSet<TreeItem> items = new HashSet<>();
                HashSet<String> sorts = new HashSet<>();
                for (TreeItem sort1 : treeProblemsPtr[0].getSelection()) {
                    if (sort1 == null) continue;
                    if (sort1.equals(treeItemHintsPtr[0])) {
                        items.add(treeItemHintsPtr[0]);
                    } else if (sort1.equals(treeItemErrorsPtr[0])) {
                        items.add(treeItemErrorsPtr[0]);
                    } else if (sort1.equals(treeItemWarningsPtr[0])) {
                        items.add(treeItemWarningsPtr[0]);
                    } else if (sort1.equals(treeItemDuplicatesPtr[0])) {
                        items.add(treeItemDuplicatesPtr[0]);
                    }
                    if (sort1.getText(2).startsWith("[WFE]")) { //$NON-NLS-1$
                        if (!sorts.contains(sort1.getText(2)))
                            sorts.add(sort1.getText(2));
                    } else if (sort1.getText(2).startsWith("[E01]")) { //$NON-NLS-1$
                        if (!sorts.contains(sort1.getText(2)))
                            sorts.add(sort1.getText(2));
                    } else {
                        if (!sorts.contains(sort1.getText(0)))
                            sorts.add(sort1.getText(0));
                    }

                }
                for (TreeItem sort2 : treeItemHintsPtr[0].getItems()) {
                    if (sorts.contains(sort2.getText(0)) && !items.contains(sort2))
                        items.add(sort2);
                }
                for (TreeItem sort3 : treeItemErrorsPtr[0].getItems()) {
                    if (sorts.contains(sort3.getText(0)) && !items.contains(sort3))
                        items.add(sort3);
                }
                for (TreeItem sort4 : treeItemWarningsPtr[0].getItems()) {
                    if (sorts.contains(sort4.getText(0)) || sorts.contains(sort4.getText(2)) && !items.contains(sort4))
                        items.add(sort4);
                }
                for (TreeItem sort5 : treeItemDuplicatesPtr[0].getItems()) {
                    if (sorts.contains(sort5.getText(0)) || sorts.contains(sort5.getText(2)) && !items.contains(sort5))
                        items.add(sort5);
                }

                if (items.contains(treeItemHintsPtr[0])) {
                    NLogger.debug(getClass(), "+Quick fix all hints."); //$NON-NLS-1$
                    items.remove(treeItemHintsPtr[0]);
                    for (TreeItem sort6 : treeItemHintsPtr[0].getItems()) {
                        if (!items.contains(sort6))
                            items.add(sort6);
                    }
                }
                if (items.contains(treeItemErrorsPtr[0])) {
                    NLogger.debug(getClass(), "+Quick fix all errors."); //$NON-NLS-1$
                    items.remove(treeItemErrorsPtr[0]);
                    for (TreeItem sort7 : treeItemErrorsPtr[0].getItems()) {
                        if (!items.contains(sort7))
                            items.add(sort7);
                    }
                }
                if (items.contains(treeItemWarningsPtr[0])) {
                    NLogger.debug(getClass(), "+Quick fix all warnings."); //$NON-NLS-1$
                    items.remove(treeItemWarningsPtr[0]);
                    for (TreeItem sort8 : treeItemWarningsPtr[0].getItems()) {
                        if (!items.contains(sort8))
                            items.add(sort8);
                    }
                }
                if (items.contains(treeItemDuplicatesPtr[0])) {
                    NLogger.debug(getClass(), "+Quick fix all duplicates."); //$NON-NLS-1$
                    items.remove(treeItemDuplicatesPtr[0]);
                    for (TreeItem sort9 : treeItemDuplicatesPtr[0].getItems()) {
                        if (!items.contains(sort9))
                            items.add(sort9);
                    }
                }

                for (TreeItem issue : items) {
                    if (issue.getData() != null) {
                        NLogger.debug(getClass(), "+Fix {0}", issue.getText(1)); //$NON-NLS-1$
                    }
                }

                QuickFixer.fixTextIssues(compositeTextPtr[0], items, getState().getFileNameObj());
            }
        };
        final WidgetSelectionListener inspect = e -> {
            if (compositeTextPtr[0].getEditable() && treeProblemsPtr[0].getSelectionCount() > 0) {
                final VertexManager vm = state.getFileNameObj().getVertexManager();
                if (!vm.isUpdated()) return;
                HashSet<TreeItem> items = new HashSet<>();
                for (TreeItem sort1 : treeProblemsPtr[0].getSelection()) {
                    items.add(sort1);
                }
                if (items.contains(treeItemHintsPtr[0])) {
                    NLogger.debug(getClass(), "+Inspect all hints."); //$NON-NLS-1$
                    items.remove(treeItemHintsPtr[0]);
                    for (TreeItem sort2 : treeItemHintsPtr[0].getItems()) {
                        if (!items.contains(sort2))
                            items.add(sort2);
                    }
                }
                if (items.contains(treeItemErrorsPtr[0])) {
                    NLogger.debug(getClass(), "+Inspect all errors."); //$NON-NLS-1$
                    items.remove(treeItemErrorsPtr[0]);
                    for (TreeItem sort3 : treeItemErrorsPtr[0].getItems()) {
                        if (!items.contains(sort3))
                            items.add(sort3);
                    }
                }
                if (items.contains(treeItemWarningsPtr[0])) {
                    NLogger.debug(getClass(), "+Inspect all warnings."); //$NON-NLS-1$
                    items.remove(treeItemWarningsPtr[0]);
                    for (TreeItem sort4 : treeItemWarningsPtr[0].getItems()) {
                        if (!items.contains(sort4))
                            items.add(sort4);
                    }
                }
                if (items.contains(treeItemDuplicatesPtr[0])) {
                    NLogger.debug(getClass(), "+Inspect all duplicates."); //$NON-NLS-1$
                    items.remove(treeItemDuplicatesPtr[0]);
                    for (TreeItem sort5 : treeItemDuplicatesPtr[0].getItems()) {
                        if (!items.contains(sort5))
                            items.add(sort5);
                    }
                }

                for (TreeItem issue : items) {
                    if (issue != null && issue.getData() != null) {
                        NLogger.debug(getClass(), "+Inspect {0}", issue.getText(1)); //$NON-NLS-1$
                    }
                }

                Inspector.inspectTextIssues(compositeTextPtr[0], items, getState().getFileNameObj());
            }
        };
        final WidgetSelectionListener inspectSame = e -> {
            if (compositeTextPtr[0].getEditable() && treeProblemsPtr[0].getSelectionCount() > 0) {
                final VertexManager vm = state.getFileNameObj().getVertexManager();
                if (!vm.isUpdated()) return;
                HashSet<TreeItem> items = new HashSet<>();
                HashSet<String> sorts = new HashSet<>();
                for (TreeItem sort1 : treeProblemsPtr[0].getSelection()) {
                    if (sort1 == null) continue;
                    if (sort1.equals(treeItemHintsPtr[0])) {
                        items.add(treeItemHintsPtr[0]);
                    } else if (sort1.equals(treeItemErrorsPtr[0])) {
                        items.add(treeItemErrorsPtr[0]);
                    } else if (sort1.equals(treeItemWarningsPtr[0])) {
                        items.add(treeItemWarningsPtr[0]);
                    } else if (sort1.equals(treeItemDuplicatesPtr[0])) {
                        items.add(treeItemDuplicatesPtr[0]);
                    }
                    if (sort1.getText(2).startsWith("[WFE]")) { //$NON-NLS-1$
                        if (!sorts.contains(sort1.getText(2)))
                            sorts.add(sort1.getText(2));
                    } else if (sort1.getText(2).startsWith("[E01]")) { //$NON-NLS-1$
                        if (!sorts.contains(sort1.getText(2)))
                            sorts.add(sort1.getText(2));
                    } else {
                        if (!sorts.contains(sort1.getText(0)))
                            sorts.add(sort1.getText(0));
                    }

                }
                for (TreeItem sort2 : treeItemHintsPtr[0].getItems()) {
                    if (sorts.contains(sort2.getText(0)) && !items.contains(sort2))
                        items.add(sort2);
                }
                for (TreeItem sort3 : treeItemErrorsPtr[0].getItems()) {
                    if (sorts.contains(sort3.getText(0)) && !items.contains(sort3))
                        items.add(sort3);
                }
                for (TreeItem sort4 : treeItemWarningsPtr[0].getItems()) {
                    if (sorts.contains(sort4.getText(0)) || sorts.contains(sort4.getText(2)) && !items.contains(sort4))
                        items.add(sort4);
                }
                for (TreeItem sort5 : treeItemDuplicatesPtr[0].getItems()) {
                    if (sorts.contains(sort5.getText(0)) || sorts.contains(sort5.getText(2)) && !items.contains(sort5))
                        items.add(sort5);
                }

                if (items.contains(treeItemHintsPtr[0])) {
                    NLogger.debug(getClass(), "+Inspect all hints."); //$NON-NLS-1$
                    items.remove(treeItemHintsPtr[0]);
                    for (TreeItem sort6 : treeItemHintsPtr[0].getItems()) {
                        if (!items.contains(sort6))
                            items.add(sort6);
                    }
                }
                if (items.contains(treeItemErrorsPtr[0])) {
                    NLogger.debug(getClass(), "+Inspect all errors."); //$NON-NLS-1$
                    items.remove(treeItemErrorsPtr[0]);
                    for (TreeItem sort7 : treeItemErrorsPtr[0].getItems()) {
                        if (!items.contains(sort7))
                            items.add(sort7);
                    }
                }
                if (items.contains(treeItemWarningsPtr[0])) {
                    NLogger.debug(getClass(), "+Inspect all warnings."); //$NON-NLS-1$
                    items.remove(treeItemWarningsPtr[0]);
                    for (TreeItem sort8 : treeItemWarningsPtr[0].getItems()) {
                        if (!items.contains(sort8))
                            items.add(sort8);
                    }
                }
                if (items.contains(treeItemDuplicatesPtr[0])) {
                    NLogger.debug(getClass(), "+Inspect all duplicates."); //$NON-NLS-1$
                    items.remove(treeItemDuplicatesPtr[0]);
                    for (TreeItem sort9 : treeItemDuplicatesPtr[0].getItems()) {
                        if (!items.contains(sort9))
                            items.add(sort9);
                    }
                }

                for (TreeItem issue : items) {
                    if (issue.getData() != null) {
                        NLogger.debug(getClass(), "+Inspect {0}", issue.getText(1)); //$NON-NLS-1$
                    }
                }

                Inspector.inspectTextIssues(compositeTextPtr[0], items, getState().getFileNameObj());
            }
        };
        widgetUtil(mntmQuickFixPtr[0]).addSelectionListener(quickFix);
        widgetUtil(mntmQuickFixSamePtr[0]).addSelectionListener(quickFixSame);
        widgetUtil(btnQuickFixPtr[0]).addSelectionListener(quickFix);
        widgetUtil(btnQuickFixSamePtr[0]).addSelectionListener(quickFixSame);
        widgetUtil(mntmInspectPtr[0]).addSelectionListener(inspect);
        widgetUtil(mntmInspectSamePtr[0]).addSelectionListener(inspectSame);
        widgetUtil(btnInspectPtr[0]).addSelectionListener(inspect);
        widgetUtil( btnInspectSamePtr[0]).addSelectionListener(inspectSame);

        treeProblemsPtr[0].addSelectionListener(e -> {
            boolean enabled = treeProblemsPtr[0].getSelectionCount() == 1 && treeProblemsPtr[0].getSelection()[0] != null;
            btnInspectPtr[0].setEnabled(enabled);
            btnInspectSamePtr[0].setEnabled(enabled);
            btnQuickFixPtr[0].setEnabled(enabled);
            btnQuickFixSamePtr[0].setEnabled(enabled);
        });
        treeProblemsPtr[0].addListener(SWT.MouseDoubleClick, e -> {
            final TreeItem[] selection = treeProblemsPtr[0].getSelection();
            final TreeItem sel;
            if (selection.length == 1 && (sel = selection[0]) != null) {
                final Integer pos = (Integer) sel.getData();
                if (pos != null) {
                    compositeTextPtr[0].setSelection(Math.max(0, pos));
                }
                if (sel.getParentItem() == null) {
                    sel.setVisible(!sel.isVisible());
                    Display.getCurrent().asyncExec(() -> treeProblemsPtr[0].build());
                    treeProblemsPtr[0].redraw();
                    treeProblemsPtr[0].update();
                    treeProblemsPtr[0].getTree().select(treeProblemsPtr[0].getMapInv().get(sel));
                }
            }
        });
        compositeContainerPtr[0].addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                ((GridData) compositeTextPtr[0].getLayoutData()).minimumHeight = compositeContainerPtr[0].getBounds().height;
                if (!tabFolderPartInformationPtr[0].getVisible()) {
                    sashFormPtr[0].setWeights(new int[] { compositeTextPtr[0].getSize().y, 1 });
                }
                compositeContainerPtr[0].layout();
            }
        });
        canvasLineNumberAreaPtr[0].addMouseListener(new MouseListener() {

            @Override
            public void mouseUp(MouseEvent e) {}

            @Override
            public void mouseDown(MouseEvent e) {
                int yOffset = -compositeTextPtr[0].getVerticalBar().getSelection() % caretHeight;
                int height = compositeContainerPtr[0].getBounds().height;
                int startLine = compositeTextPtr[0].getVerticalBar().getSelection() / caretHeight + 1;
                int endLine = compositeTextPtr[0].getLineCount() - 1;

                NLogger.debug(getClass(), "Mouse down on Line Number Area"); //$NON-NLS-1$

                NLogger.debug(getClass(), "y_offset" + yOffset); //$NON-NLS-1$
                NLogger.debug(getClass(), "height" + height); //$NON-NLS-1$
                NLogger.debug(getClass(), "start_line" + startLine); //$NON-NLS-1$
                NLogger.debug(getClass(), "end_line" + endLine); //$NON-NLS-1$

                NLogger.debug(getClass(), "e.y" + e.y); //$NON-NLS-1$


                int line = (e.y - yOffset) / caretHeight + startLine;

                NLogger.debug(getClass(), "Line " + line); //$NON-NLS-1$
                line--;

                final int oldSelectionStart = compositeTextPtr[0].getSelection().x;
                final int oldSelectionEnd = compositeTextPtr[0].getSelection().y;

                if ((e.stateMask & SWT.CTRL) != 0) {
                    try {
                        int newstart = compositeTextPtr[0].getOffsetAtLine(line + 1);
                        int newend = compositeTextPtr[0].getOffsetAtLine(line);
                        newstart = Math.max(Math.max(oldSelectionStart, oldSelectionEnd), newstart);
                        newend = Math.min(Math.min(oldSelectionStart, oldSelectionEnd), newend);
                        compositeTextPtr[0].setSelection(newstart, newend);
                    } catch (IllegalArgumentException iae) {
                        try {
                            int newstart = compositeTextPtr[0].getText().length();
                            int newend = compositeTextPtr[0].getOffsetAtLine(line);
                            newstart = Math.max(Math.max(oldSelectionStart, oldSelectionEnd), newstart);
                            newend = Math.min(Math.min(oldSelectionStart, oldSelectionEnd), newend);
                            compositeTextPtr[0].setSelection(newstart, newend);
                        } catch (IllegalArgumentException consumed) {}
                    }
                } else {
                    try {
                        compositeTextPtr[0].setSelection(compositeTextPtr[0].getOffsetAtLine(line + 1), compositeTextPtr[0].getOffsetAtLine(line));
                    } catch (IllegalArgumentException iae) {
                        try {
                            compositeTextPtr[0].setSelection(compositeTextPtr[0].getText().length(), compositeTextPtr[0].getOffsetAtLine(line));
                        } catch (IllegalArgumentException consumed) {}
                    }
                }
                try {
                    if (compositeTextPtr[0].getSelectionText().endsWith(StringHelper.getLineDelimiter())) {
                        compositeTextPtr[0].setSelection(compositeTextPtr[0].getSelection().x, compositeTextPtr[0].getSelection().y - StringHelper.getLineDelimiter().length());
                    }
                } catch (IllegalArgumentException consumed) {}
            }

            @Override
            public void mouseDoubleClick(MouseEvent e) {}
        });
        canvasLineNumberAreaPtr[0].addPaintListener(e -> {
            e.gc.setFont(Font.MONOSPACE);
            int yOffset = -compositeTextPtr[0].getVerticalBar().getSelection() % caretHeight;
            int height = compositeContainerPtr[0].getBounds().height;
            int startLine = compositeTextPtr[0].getVerticalBar().getSelection() / caretHeight + 1;
            int endLine = compositeTextPtr[0].getLineCount() - 1;

            for (int y = yOffset; y < height; y += caretHeight) {

                if (NLogger.debugging && Project.getFileToEdit() != null) {

                    // Emergency reference debugging
                    StringBuilder sb = new StringBuilder();
                    sb.append(startLine);
                    sb.append(" "); //$NON-NLS-1$
                    GData source = Project.getFileToEdit().getDrawPerLine().getValue(startLine);
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
                        source = Project.getFileToEdit().getDrawPerLine().getValue(startLine).getNext();
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
                    e.gc.drawText(Integer.toString(startLine), 0, y);
                }

                if (startLine > endLine) {
                    break;
                }
                startLine++;
            }

        });
        widgetUtil(compositeTextPtr[0].getVerticalBar()).addSelectionListener(e -> {
            if (!isDisposed()) {
                canvasLineNumberAreaPtr[0].redraw();
                getDisplay().update();
            }
        });
        widgetUtil(mntmDeletePtr[0]).addSelectionListener(e -> state.folder[0].delete());
        widgetUtil(mntmCopyPtr[0]).addSelectionListener(e -> state.folder[0].copy());
        widgetUtil(mntmCutPtr[0]).addSelectionListener(e -> state.folder[0].cut());
        widgetUtil(mntmPastePtr[0]).addSelectionListener(e -> state.folder[0].paste());

        widgetUtil(mntmDrawSelectionPtr[0]).addSelectionListener(e -> {
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
        });

        widgetUtil(mntmDrawUntilSelectionPtr[0]).addSelectionListener(e -> {
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
        });

        widgetUtil(mntmHideSelectionPtr[0]).addSelectionListener(e -> hideSelection());
        widgetUtil(mntmShowSelectionPtr[0]).addSelectionListener(e -> showSelection());

        widgetUtil(mntmShowAllPtr[0]).addSelectionListener(e -> {
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
        setErrorTabVisibility(!tabFolderPartInformationPtr[0].isVisible());
        compositeContainerPtr[0].layout();
    }

    /**
     * Shows or hides the error tab
     *
     * @param isVisible
     *            {@code true} when the tab is shown
     */
    public void setErrorTabVisibility(boolean isVisible) {
        tabFolderPartInformationPtr[0].setVisible(isVisible);
        if (isVisible) {
            sashFormPtr[0].setWeights(new int[] { compositeContainerPtr[0].getSize().y, compositeContainerPtr[0].getSize().y / 2 });
        } else {
            sashFormPtr[0].setWeights(new int[] { compositeContainerPtr[0].getSize().y, 1 });
        }
    }

    /**
     * Restores the state of this tab (necessary if the tab was disposed before)
     *
     * @param state
     */
    private void restoreState(CompositeTabState state) {
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
        ct.canvasLineNumberAreaPtr[0] = this.canvasLineNumberAreaPtr[0];
        ct.compositeTextPtr[0] = this.compositeTextPtr[0];
        ct.compositeContainerPtr[0] = this.compositeContainerPtr[0];
        ct.sashFormPtr[0] = this.sashFormPtr[0];
        ct.tabFolderPartInformationPtr[0] = this.tabFolderPartInformationPtr[0];
        ct.treeProblemsPtr[0] = this.treeProblemsPtr[0];
        ct.treeItemHintsPtr[0] = this.treeItemHintsPtr[0];
        ct.treeItemWarningsPtr[0] = this.treeItemWarningsPtr[0];
        ct.treeItemErrorsPtr[0] = this.treeItemErrorsPtr[0];
        ct.treeItemDuplicatesPtr[0] = this.treeItemDuplicatesPtr[0];
        ct.btnInspectPtr[0] = this.btnInspectPtr[0];
        ct.btnInspectSamePtr[0] = this.btnInspectSamePtr[0];
        ct.btnQuickFixPtr[0] = this.btnQuickFixPtr[0];
        ct.btnQuickFixSamePtr[0] = this.btnQuickFixSamePtr[0];
        ct.lblProblemCountPtr[0] = this.lblProblemCountPtr[0];
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
        return this.state.getTab().compositeTextPtr[0];
    }

    public void parseForErrorAndHints() {
        this.state.getFileNameObj().parseForError(getTextComposite(), 0, getTextComposite().getText().length(), getTextComposite().getText().length(), getTextComposite().getText(), treeItemHintsPtr[0], treeItemWarningsPtr[0], treeItemErrorsPtr[0], treeItemDuplicatesPtr[0], lblProblemCountPtr[0], true);
        int errorCount = treeItemErrorsPtr[0].getItems().size();
        int warningCount = treeItemWarningsPtr[0].getItems().size();
        int hintCount = treeItemHintsPtr[0].getItems().size();
        int duplicateCount = treeItemDuplicatesPtr[0].getItems().size();
        String errors = errorCount == 1 ? I18n.EDITORTEXT_ERROR : I18n.EDITORTEXT_ERRORS;
        String warnings = warningCount == 1 ? I18n.EDITORTEXT_WARNING : I18n.EDITORTEXT_WARNINGS;
        String hints = hintCount == 1 ? I18n.EDITORTEXT_OTHER : I18n.EDITORTEXT_OTHERS;
        String duplicates = duplicateCount == 1 ? I18n.EDITORTEXT_DUPLICATE : I18n.EDITORTEXT_DUPLICATES;
        lblProblemCountPtr[0].setText(errorCount + " " + errors + ", " + warningCount + " " + warnings + ", " + hintCount + " " + hints + ", " + duplicateCount + " " + duplicates); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
        lblProblemCountPtr[0].getParent().layout();
    }

    public void updateColours() {
        this.state.getTab().compositeTextPtr[0].setBackground(Colour.textBackground[0]);
        this.state.getTab().compositeTextPtr[0].setForeground(Colour.textForeground[0]);
        this.state.getTab().compositeTextPtr[0].redrawRange(0, this.state.getTab().compositeTextPtr[0].getText().length(), false);
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
