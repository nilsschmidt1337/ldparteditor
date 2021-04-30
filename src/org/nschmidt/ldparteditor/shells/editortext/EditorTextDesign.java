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

import static org.nschmidt.ldparteditor.helpers.WidgetUtility.widgetUtil;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.nschmidt.ldparteditor.composites.ToolItem;
import org.nschmidt.ldparteditor.composites.ToolSeparator;
import org.nschmidt.ldparteditor.composites.compositetab.CompositeTab;
import org.nschmidt.ldparteditor.composites.compositetab.CompositeTabFolder;
import org.nschmidt.ldparteditor.data.ColourChanger;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.dialogs.colour.ColourDialog;
import org.nschmidt.ldparteditor.enums.IconSize;
import org.nschmidt.ldparteditor.enums.LDConfig;
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.enums.Task;
import org.nschmidt.ldparteditor.enums.TextTask;
import org.nschmidt.ldparteditor.helpers.Cocoa;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.resources.ResourceManager;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.state.KeyStateManager;
import org.nschmidt.ldparteditor.widgets.NButton;
import org.nschmidt.ldparteditor.workbench.UserSettingState;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

import swing2swt.layout.BorderLayout;

/**
 * The text editor window
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 *
 * @author nils
 *
 */
class EditorTextDesign extends ApplicationWindow {

    private Composite parent = null;

    final NButton[] btnShowLeftPtr = new NButton[1];
    final NButton[] btnShowRightPtr = new NButton[1];
    final NButton[] btnSameWidthPtr = new NButton[1];

    final NButton[] btnNewPtr = new NButton[1];
    final NButton[] btnOpenPtr = new NButton[1];
    final NButton[] btnSavePtr = new NButton[1];
    final NButton[] btnSaveAsPtr = new NButton[1];

    final NButton[] btnPalettePtr = new NButton[1];

    final NButton[] btnHidePtr = new NButton[1];
    final NButton[] btnShowPtr = new NButton[1];

    final NButton[] btnCutPtr = new NButton[1];
    final NButton[] btnCopyPtr = new NButton[1];
    final NButton[] btnPastePtr = new NButton[1];
    final NButton[] btnDeletePtr = new NButton[1];

    final NButton[] btnUndoPtr = new NButton[1];
    final NButton[] btnAddHistoryPtr = new NButton[1];
    final NButton[] btnRedoPtr = new NButton[1];

    final NButton[] btnShowErrorsPtr = new NButton[1];
    final NButton[] btnFindAndReplacePtr = new NButton[1];
    final NButton[] btnSortPtr = new NButton[1];
    final NButton[] btnSplitQuadPtr = new NButton[1];
    final NButton[] btnMergeQuadPtr = new NButton[1];
    final NButton[] btnUnrectifyPtr = new NButton[1];
    final NButton[] btnShowSelectionIn3DPtr = new NButton[1];
    final NButton[] btnOpenIn3DPtr = new NButton[1];
    final NButton[] btnSyncEditPtr = new NButton[1];
    final NButton[] btnInlinePtr = new NButton[1];
    final NButton[] btnInlineDeepPtr = new NButton[1];
    final NButton[] btnInlineLinkedPtr = new NButton[1];
    final NButton[] btnBFCswapPtr = new NButton[1];
    final NButton[] btnCompileSubfilePtr = new NButton[1];
    final NButton[] btnRoundSelectionPtr = new NButton[1];
    final NButton[] btnTexmapPtr = new NButton[1];
    final NButton[] btnAnnotatePtr = new NButton[1];

    private ToolItem toolItemColourBar;
    /**
     * The reference to the underlying business logic (only for testing
     * purpose!)
     */
    ApplicationWindow editorTextWindow;
    CompositeTabFolder[] tabFolderPtr = new CompositeTabFolder[1];

    EditorTextDesign() {
        super(null);
        this.editorTextWindow = this;
        addToolBar(SWT.FLAT | SWT.WRAP);
        addStatusLine();
    }

    public EditorTextDesign(Composite parent, ApplicationWindow win) {
        super(null);
        this.parent = parent;
        this.editorTextWindow = win;
    }

    /**
     * Create contents of the application window.
     *
     * @param parent
     */
    @Override
    protected Control createContents(Composite parent) {
        this.parent = parent;
        return build();
    }

    /**
     * Create the status line manager.
     *
     * @return the status line manager
     */
    @Override
    protected StatusLineManager createStatusLineManager() {
        return new StatusLineManager();
    }

    /**
     * Configure the shell.
     *
     * @param newShell
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
    }

    /**
     * Return the initial size of the window.
     */
    @Override
    protected Point getInitialSize() {
        return new Point(916, 578);
    }

    /**
     * Sets the reference to the business logic component of the text editor.
     *
     * @param editorTextWindow
     *            the window (business logic) of the text editor Note: I need
     *            this only for testing!
     */
    public void setEditorTextWindow(ApplicationWindow editorTextWindow) {
        this.editorTextWindow = editorTextWindow;
    }


    private void addColorButton(ToolItem toolItemColours, GColour gColour, final int index) {
        int cn = gColour.getColourNumber();
        if (cn != -1 && LDConfig.hasColour(cn)) {
            gColour = LDConfig.getColour(cn);
        }
        final int imgSize = IconSize.getImageSizeFromIconSize();
        final GColour[] gColour2 = new GColour[] { gColour };
        final Color[] col = new Color[1];
        col[0] = SWTResourceManager.getColor((int) (gColour2[0].getR() * 255f), (int) (gColour2[0].getG() * 255f), (int) (gColour2[0].getB() * 255f));

        final NButton btnCol = new NButton(toolItemColours, Cocoa.getStyle());
        btnCol.setData(gColour2);
        int num = gColour2[0].getColourNumber();
        if (!LDConfig.hasColour(num)) {
            num = -1;
        }
        if (num != -1) {

            Object[] messageArguments = {num, LDConfig.getColourName(num)};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.getLocale());
            formatter.applyPattern(I18n.EDITORTEXT_COLOUR_1 + Cocoa.replaceCtrlByCmd(I18n.E3D_CONTROL_CLICK_MODIFY));

            btnCol.setToolTipText(formatter.format(messageArguments));
        } else {

            StringBuilder colourBuilder = new StringBuilder();
            colourBuilder.append("0x2"); //$NON-NLS-1$
            colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getR())).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getG())).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getB())).toUpperCase());

            Object[] messageArguments = {colourBuilder.toString()};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.getLocale());
            formatter.applyPattern(I18n.EDITORTEXT_COLOUR_2 + Cocoa.replaceCtrlByCmd(I18n.E3D_CONTROL_CLICK_MODIFY));

            btnCol.setToolTipText(formatter.format(messageArguments));
        }

        btnCol.setImage(ResourceManager.getImage("icon16_fullTransparent.png")); //$NON-NLS-1$

        widgetUtil(btnCol).addSelectionListener(e -> {
            if (Cocoa.checkCtrlOrCmdPressed(e.stateMask)) {
                // Choose new colour
                new ColourDialog(btnCol.getShell(), gColour2, false).run();
                WorkbenchManager.getUserSettingState().getUserPalette().set(index, gColour2[0]);
                col[0] = SWTResourceManager.getColor((int) (gColour2[0].getR() * 255f), (int) (gColour2[0].getG() * 255f), (int) (gColour2[0].getB() * 255f));
                int num1 = gColour2[0].getColourNumber();
                if (LDConfig.hasColour(num1)) {
                    gColour2[0] = LDConfig.getColour(num1);
                } else {
                    num1 = -1;
                }
                if (num1 != -1) {
                    Object[] messageArguments1 = {num1, LDConfig.getColourName(num1)};
                    MessageFormat formatter1 = new MessageFormat(""); //$NON-NLS-1$
                    formatter1.setLocale(MyLanguage.getLocale());
                    formatter1.applyPattern(I18n.EDITORTEXT_COLOUR_1 + Cocoa.replaceCtrlByCmd(I18n.E3D_CONTROL_CLICK_MODIFY));

                    btnCol.setToolTipText(formatter1.format(messageArguments1));
                } else {
                    StringBuilder colourBuilder = new StringBuilder();
                    colourBuilder.append("0x2"); //$NON-NLS-1$
                    colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getR())).toUpperCase());
                    colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getG())).toUpperCase());
                    colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getB())).toUpperCase());

                    Object[] messageArguments2 = {colourBuilder.toString()};
                    MessageFormat formatter2 = new MessageFormat(""); //$NON-NLS-1$
                    formatter2.setLocale(MyLanguage.getLocale());
                    formatter2.applyPattern(I18n.EDITORTEXT_COLOUR_2 + Cocoa.replaceCtrlByCmd(I18n.E3D_CONTROL_CLICK_MODIFY));

                    btnCol.setToolTipText(formatter2.format(messageArguments2));

                }
                Editor3DWindow.reloadAllColours();
            } else {
                int num2 = gColour2[0].getColourNumber();
                if (LDConfig.hasColour(num2)) {
                    gColour2[0] = LDConfig.getColour(num2);
                } else {
                    num2 = -1;
                }

                CompositeTab selection = (CompositeTab) tabFolderPtr[0].getSelection();
                if (selection != null) {
                    DatFile df = selection.getState().getFileNameObj();
                    if (!df.isReadOnly()) {
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
                        ColourChanger.changeColour(fromLine, toLine, df, num2, gColour2[0].getR(), gColour2[0].getG(), gColour2[0].getB(), gColour2[0].getA());
                        st.forceFocus();
                    }
                }

            }
        });
        final Point size = btnCol.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        final int x = Math.round(size.x / 5f);
        final int y = Math.round(size.y / 5f);
        final int w = Math.round(size.x * (3f / 5f));
        final int h = Math.round(size.y * (3f / 5f));
        btnCol.addPaintListener(e -> {
            e.gc.setBackground(col[0]);
            e.gc.fillRectangle(x, y, w, h);
            if (gColour2[0].getA() == 1f) {
                e.gc.drawImage(ResourceManager.getImage("icon16_transparent.png"), 0, 0, imgSize, imgSize, x, y, w, h); //$NON-NLS-1$
            } else {
                e.gc.drawImage(ResourceManager.getImage("icon16_halftrans.png"), 0, 0, imgSize, imgSize, x, y, w, h); //$NON-NLS-1$
            }
        });
    }

    public void reloadColours() {
        for (Control ctrl : toolItemColourBar.getChildren()) {
            if (!(ctrl instanceof ToolSeparator)) ctrl.dispose();
        }

        List<GColour> colours = WorkbenchManager.getUserSettingState().getUserPalette();

        final int size = colours.size();
        for (int i = 0; i < size; i++) {
            addColorButton(toolItemColourBar, colours.get(i), i);
        }

        {
            NButton btnPalette = new NButton(toolItemColourBar, Cocoa.getStyle());
            this.btnPalettePtr[0] = btnPalette;
            btnPalette.setToolTipText(I18n.E3D_MORE);
            btnPalette.setImage(ResourceManager.getImage("icon16_colours.png")); //$NON-NLS-1$
        }

        toolItemColourBar.getParent().layout();
        toolItemColourBar.layout();
        toolItemColourBar.redraw();
    }

    public Control build() {
        editorTextWindow.setStatus(I18n.E3D_READY_STATUS);
        Composite container = new Composite(parent, Cocoa.getStyle());
        container.setLayout(new BorderLayout(0, 0));
        final Composite toolBar = new Composite(container, Cocoa.getStyle());
        toolBar.setLayoutData(BorderLayout.NORTH);
        RowLayout rlToolBar = new RowLayout(SWT.HORIZONTAL);
        rlToolBar.center = true;
        toolBar.setLayout(rlToolBar);

        {
            final int TEXT_3D_SEPARATE = 0;
            final int TEXT_LEFT_3D_RIGHT = 1;
            final UserSettingState userSettings = WorkbenchManager.getUserSettingState();
            if (userSettings.getTextWinArr() != TEXT_3D_SEPARATE) {
                ToolItem toolItemSashResize = new ToolItem(toolBar, Cocoa.getStyle(), true);
                if (userSettings.getTextWinArr() == TEXT_LEFT_3D_RIGHT) {
                    NButton btnShowLeft = new NButton(toolItemSashResize, Cocoa.getStyle());
                    this.btnShowLeftPtr[0] = btnShowLeft;
                    btnShowLeft.setToolTipText(I18n.E3D_SASH_LEFT);
                    btnShowLeft.setImage(ResourceManager.getImage("icon16_leftSash.png")); //$NON-NLS-1$
                } else {
                    NButton btnSameWidth = new NButton(toolItemSashResize, Cocoa.getStyle());
                    this.btnSameWidthPtr[0] = btnSameWidth;
                    btnSameWidth.setToolTipText(I18n.E3D_SASH_SAME_WIDTH);
                    btnSameWidth.setImage(ResourceManager.getImage("icon16_sameWidth.png")); //$NON-NLS-1$
                    NButton btnShowRight = new NButton(toolItemSashResize, Cocoa.getStyle());
                    this.btnShowRightPtr[0] = btnShowRight;
                    btnShowRight.setToolTipText(I18n.E3D_SASH_RIGHT);
                    btnShowRight.setImage(ResourceManager.getImage("icon16_rightSash.png")); //$NON-NLS-1$
                }
            }
        }

        ToolItem toolItemNewOpenSave = new ToolItem(toolBar, Cocoa.getStyle(), true);
        {
            NButton btnNew = new NButton(toolItemNewOpenSave, Cocoa.getStyle());
            this.btnNewPtr[0] = btnNew;
            btnNew.setToolTipText(I18n.E3D_NEW_DAT);
            btnNew.setImage(ResourceManager.getImage("icon16_document-newdat.png")); //$NON-NLS-1$
        }
        {
            NButton btnOpen = new NButton(toolItemNewOpenSave, Cocoa.getStyle());
            this.btnOpenPtr[0] = btnOpen;
            btnOpen.setToolTipText(I18n.E3D_OPEN_DAT);
            btnOpen.setImage(ResourceManager.getImage("icon16_document-opendat.png")); //$NON-NLS-1$
        }
        {
            NButton btnSave = new NButton(toolItemNewOpenSave, Cocoa.getStyle());
            this.btnSavePtr[0] = btnSave;
            KeyStateManager.addTooltipText(btnSave, I18n.E3D_SAVE, Task.SAVE);
            btnSave.setImage(ResourceManager.getImage("icon16_document-savedat.png")); //$NON-NLS-1$
        }
        {
            NButton btnSaveAs = new NButton(toolItemNewOpenSave, Cocoa.getStyle());
            this.btnSaveAsPtr[0] = btnSaveAs;
            btnSaveAs.setToolTipText(I18n.E3D_SAVE_AS);
            btnSaveAs.setImage(ResourceManager.getImage("icon16_document-savedat.png")); //$NON-NLS-1$
            btnSaveAs.setText("..."); //$NON-NLS-1$
        }
        ToolItem toolItemHideUnhide = new ToolItem(toolBar, Cocoa.getStyle(), true);
        {
            NButton btnHide = new NButton(toolItemHideUnhide, Cocoa.getStyle());
            this.btnHidePtr[0] = btnHide;
            btnHide.setToolTipText(I18n.EDITORTEXT_HIDE);
            btnHide.setImage(ResourceManager.getImage("icon16_hide.png")); //$NON-NLS-1$
        }
        {
            NButton btnShow = new NButton(toolItemHideUnhide, Cocoa.getStyle());
            this.btnShowPtr[0] = btnShow;
            btnShow.setToolTipText(I18n.EDITORTEXT_SHOW);
            btnShow.setImage(ResourceManager.getImage("icon16_unhide.png")); //$NON-NLS-1$
        }
        ToolItem toolItemUndoRedo = new ToolItem(toolBar, Cocoa.getStyle(), true);
        {
            NButton btnUndo = new NButton(toolItemUndoRedo, Cocoa.getStyle());
            this.btnUndoPtr[0] = btnUndo;
            btnUndo.setImage(ResourceManager.getImage("icon16_undo.png")); //$NON-NLS-1$
            KeyStateManager.addTooltipText(btnUndo, I18n.E3D_UNDO, TextTask.EDITORTEXT_UNDO);
        }
        if (NLogger.debugging) {
            NButton btnSnapshot = new NButton(toolItemUndoRedo, Cocoa.getStyle());
            this.btnAddHistoryPtr[0] = btnSnapshot;
            btnSnapshot.setImage(ResourceManager.getImage("icon16_snapshot.png")); //$NON-NLS-1$
            btnSnapshot.setToolTipText(I18n.E3D_SNAPSHOT);
        }
        {
            NButton btnRedo = new NButton(toolItemUndoRedo, Cocoa.getStyle());
            this.btnRedoPtr[0] = btnRedo;
            btnRedo.setImage(ResourceManager.getImage("icon16_redo.png")); //$NON-NLS-1$
            KeyStateManager.addTooltipText(btnRedo, I18n.E3D_REDO, TextTask.EDITORTEXT_REDO);
        }
        ToolItem toolItemCCPD = new ToolItem(toolBar, Cocoa.getStyle(), true);
        {
            NButton btnCut = new NButton(toolItemCCPD, Cocoa.getStyle());
            this.btnCutPtr[0] = btnCut;
            btnCut.setImage(ResourceManager.getImage("icon16_edit-cut.png")); //$NON-NLS-1$
            KeyStateManager.addTooltipText(btnCut, I18n.COPYNPASTE_CUT, Task.CUT);
        }
        {
            NButton btnCopy = new NButton(toolItemCCPD, Cocoa.getStyle());
            this.btnCopyPtr[0] = btnCopy;
            btnCopy.setImage(ResourceManager.getImage("icon16_edit-copy.png")); //$NON-NLS-1$
            KeyStateManager.addTooltipText(btnCopy, I18n.COPYNPASTE_COPY, Task.COPY);
        }
        {
            NButton btnPaste = new NButton(toolItemCCPD, Cocoa.getStyle());
            this.btnPastePtr[0] = btnPaste;
            btnPaste.setImage(ResourceManager.getImage("icon16_edit-paste.png")); //$NON-NLS-1$
            KeyStateManager.addTooltipText(btnPaste, I18n.COPYNPASTE_PASTE, Task.PASTE);
        }
        {
            NButton btnDelete = new NButton(toolItemCCPD, Cocoa.getStyle());
            this.btnDeletePtr[0] = btnDelete;
            btnDelete.setImage(ResourceManager.getImage("icon16_delete.png")); //$NON-NLS-1$
            KeyStateManager.addTooltipText(btnDelete, I18n.COPYNPASTE_DELETE, Task.DELETE);
        }
        ToolItem toolItemDebug = new ToolItem(toolBar, Cocoa.getStyle(), true);
        {
            NButton btnShowErrors = new NButton(toolItemDebug, Cocoa.getStyle());
            this.btnShowErrorsPtr[0] = btnShowErrors;
            btnShowErrors.setImage(ResourceManager.getImage("icon16_error.png")); //$NON-NLS-1$
            btnShowErrors.setToolTipText(I18n.EDITORTEXT_SHOW_HIDE_ERROR_TAB);
        }
        {
            NButton btnFindAndReplace = new NButton(toolItemDebug, Cocoa.getStyle());
            this.btnFindAndReplacePtr[0] = btnFindAndReplace;
            btnFindAndReplace.setImage(ResourceManager.getImage("icon16_findReplace.png")); //$NON-NLS-1$
            KeyStateManager.addTooltipText(btnFindAndReplace, I18n.EDITORTEXT_FIND_REPLACE, TextTask.EDITORTEXT_FIND);
        }
        {
            NButton btnSort = new NButton(toolItemDebug, Cocoa.getStyle());
            this.btnSortPtr[0] = btnSort;
            btnSort.setImage(ResourceManager.getImage("icon16_sort.png")); //$NON-NLS-1$
            btnSort.setToolTipText(I18n.EDITORTEXT_SORT);
        }
        {
            NButton btnSplitQuad = new NButton(toolItemDebug, Cocoa.getStyle());
            this.btnSplitQuadPtr[0] = btnSplitQuad;
            btnSplitQuad.setImage(ResourceManager.getImage("icon16_quadToTri.png")); //$NON-NLS-1$
            btnSplitQuad.setToolTipText(I18n.EDITORTEXT_SPLIT_QUAD);
        }
        {
            NButton btnMergeQuad = new NButton(toolItemDebug, Cocoa.getStyle());
            this.btnMergeQuadPtr[0] = btnMergeQuad;
            btnMergeQuad.setImage(ResourceManager.getImage("icon16_triToquad.png")); //$NON-NLS-1$
            btnMergeQuad.setToolTipText(I18n.EDITORTEXT_MERGE_QUAD);
        }
        {
            NButton btnShowSelectionIn3D = new NButton(toolItemDebug, Cocoa.getStyle());
            this.btnShowSelectionIn3DPtr[0] = btnShowSelectionIn3D;
            btnShowSelectionIn3D.setImage(ResourceManager.getImage("icon16_text2selection.png")); //$NON-NLS-1$
            btnShowSelectionIn3D.setToolTipText(I18n.EDITORTEXT_SHOW_SELECTION_IN_3D);
        }
        {
            NButton btnOpenIn3D = new NButton(toolItemDebug, Cocoa.getStyle());
            this.btnOpenIn3DPtr[0] = btnOpenIn3D;
            btnOpenIn3D.setImage(ResourceManager.getImage("icon16_openIn3D.png")); //$NON-NLS-1$
            btnOpenIn3D.setToolTipText(I18n.E3D_OPEN_IN_3D_EDITOR);
        }
        {
            NButton btnUnrectify = new NButton(toolItemDebug, Cocoa.getStyle());
            this.btnUnrectifyPtr[0] = btnUnrectify;
            btnUnrectify.setImage(ResourceManager.getImage("icon16_unrectify.png")); //$NON-NLS-1$
            btnUnrectify.setToolTipText(Cocoa.replaceCtrlByCmd(I18n.EDITORTEXT_UNRECTIFY));
        }
        {
            NButton btnSyncEdit = new NButton(toolItemDebug, Cocoa.getStyle());
            this.btnSyncEditPtr[0] = btnSyncEdit;
            btnSyncEdit.setImage(ResourceManager.getImage("icon16_syncedit.png")); //$NON-NLS-1$
            KeyStateManager.addTooltipText(btnSyncEdit, I18n.EDITORTEXT_SYNC_EDIT_BUTTON, TextTask.EDITORTEXT_REPLACE_VERTEX);
        }
        {
            NButton btnInline = new NButton(toolItemDebug, Cocoa.getStyle());
            this.btnInlinePtr[0] = btnInline;
            btnInline.setImage(ResourceManager.getImage("icon16_inline.png")); //$NON-NLS-1$
            KeyStateManager.addTooltipText(btnInline, I18n.EDITORTEXT_INLINE_1, TextTask.EDITORTEXT_INLINE);
        }
        {
            NButton btnInlineDeep = new NButton(toolItemDebug, Cocoa.getStyle());
            this.btnInlineDeepPtr[0] = btnInlineDeep;
            btnInlineDeep.setImage(ResourceManager.getImage("icon16_inlinedeep.png")); //$NON-NLS-1$
            btnInlineDeep.setToolTipText(I18n.EDITORTEXT_INLINE_2);
        }
        {
            NButton btnInlineLinked = new NButton(toolItemDebug, Cocoa.getStyle());
            this.btnInlineLinkedPtr[0] = btnInlineLinked;
            btnInlineLinked.setImage(ResourceManager.getImage("icon16_inlinelinked.png")); //$NON-NLS-1$
            btnInlineLinked.setToolTipText(I18n.EDITORTEXT_INLINE_3);
        }
        {
            NButton btnBFCswap = new NButton(toolItemDebug, Cocoa.getStyle());
            this.btnBFCswapPtr[0] = btnBFCswap;
            btnBFCswap.setToolTipText(I18n.E3D_SWAP_WINDING);
            btnBFCswap.setImage(ResourceManager.getImage("icon16_bfcSwap.png")); //$NON-NLS-1$
        }
        {
            NButton btnCompileSubfile = new NButton(toolItemDebug, Cocoa.getStyle());
            this.btnCompileSubfilePtr[0] = btnCompileSubfile;
            btnCompileSubfile.setToolTipText(I18n.EDITORTEXT_COMPILE);
            btnCompileSubfile.setImage(ResourceManager.getImage("icon16_subcompile.png")); //$NON-NLS-1$
        }
        {
            NButton btnRoundSelection = new NButton(toolItemDebug, Cocoa.getStyle());
            this.btnRoundSelectionPtr[0] = btnRoundSelection;
            KeyStateManager.addTooltipText(btnRoundSelection, I18n.EDITORTEXT_ROUND + Cocoa.replaceCtrlByCmd(I18n.E3D_CONTROL_CLICK_MODIFY), TextTask.EDITORTEXT_ROUND);
            btnRoundSelection.setImage(ResourceManager.getImage("icon16_round.png")); //$NON-NLS-1$
        }

        {
            NButton btnTexmap = new NButton(toolItemDebug, Cocoa.getStyle());
            this.btnTexmapPtr[0] = btnTexmap;
            btnTexmap.setToolTipText(I18n.EDITORTEXT_TEXMAP);
            btnTexmap.setImage(ResourceManager.getImage("icon16_texmap.png")); //$NON-NLS-1$
        }

        {
            NButton btnAnnotate = new NButton(toolItemDebug, Cocoa.getStyle());
            this.btnAnnotatePtr[0] = btnAnnotate;
            btnAnnotate.setToolTipText(I18n.EDITORTEXT_COMMENT);
            btnAnnotate.setImage(ResourceManager.getImage("icon16_annotate.png")); //$NON-NLS-1$
        }

        {
            ToolItem toolItemColours = new ToolItem(toolBar, Cocoa.getStyle(), true);
            toolItemColourBar = toolItemColours;
            List<GColour> colours = WorkbenchManager.getUserSettingState().getUserPalette();

            final int size = colours.size();
            for (int i = 0; i < size; i++) {
                addColorButton(toolItemColours, colours.get(i), i);
            }

            {
                NButton btnPalette = new NButton(toolItemColours, Cocoa.getStyle());
                this.btnPalettePtr[0] = btnPalette;
                btnPalette.setToolTipText(I18n.E3D_MORE);
                btnPalette.setImage(ResourceManager.getImage("icon16_colours.png")); //$NON-NLS-1$
            }
        }

        {
            Composite cmpTextEditor = new Composite(container, SWT.BORDER);
            cmpTextEditor.setLayoutData(BorderLayout.CENTER);
            cmpTextEditor.setLayout(new FillLayout(SWT.HORIZONTAL));
            {
                CompositeTabFolder tabFolder = new CompositeTabFolder(cmpTextEditor, SWT.BORDER);
                this.tabFolderPtr[0] = tabFolder;
                tabFolder.setMRUVisible(true);
                tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
                tabFolder.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            }
        }
        return container;
    }

    public boolean isSeperateWindow() {
        return this == editorTextWindow;
    }
}
