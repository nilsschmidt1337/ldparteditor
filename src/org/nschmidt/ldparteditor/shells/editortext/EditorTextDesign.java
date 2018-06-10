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

import static org.nschmidt.ldparteditor.helpers.WidgetUtility.WidgetUtil;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
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
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.enums.Task;
import org.nschmidt.ldparteditor.enums.TextTask;
import org.nschmidt.ldparteditor.enums.View;
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

    final NButton[] btn_showLeft = new NButton[1];
    final NButton[] btn_showRight = new NButton[1];
    final NButton[] btn_sameWidth = new NButton[1];

    final NButton[] btn_New = new NButton[1];
    final NButton[] btn_Open = new NButton[1];
    final NButton[] btn_Save = new NButton[1];
    final NButton[] btn_SaveAs = new NButton[1];

    final NButton[] btn_Palette = new NButton[1];

    final NButton[] btn_Hide = new NButton[1];
    final NButton[] btn_Show = new NButton[1];

    final NButton[] btn_Cut = new NButton[1];
    final NButton[] btn_Copy = new NButton[1];
    final NButton[] btn_Paste = new NButton[1];
    final NButton[] btn_Delete = new NButton[1];

    final NButton[] btn_Undo = new NButton[1];
    final NButton[] btn_AddHistory = new NButton[1];
    final NButton[] btn_Redo = new NButton[1];

    final NButton[] btn_ShowErrors = new NButton[1];
    final NButton[] btn_FindAndReplace = new NButton[1];
    final NButton[] btn_Sort = new NButton[1];
    final NButton[] btn_SplitQuad = new NButton[1];
    final NButton[] btn_MergeQuad = new NButton[1];
    final NButton[] btn_Unrectify = new NButton[1];
    final NButton[] btn_ShowSelectionIn3D = new NButton[1];
    final NButton[] btn_OpenIn3D = new NButton[1];
    final NButton[] btn_SyncEdit = new NButton[1];
    final NButton[] btn_Inline = new NButton[1];
    final NButton[] btn_InlineDeep = new NButton[1];
    final NButton[] btn_InlineLinked = new NButton[1];
    final NButton[] btn_BFCswap = new NButton[1];
    final NButton[] btn_CompileSubfile = new NButton[1];
    final NButton[] btn_RoundSelection = new NButton[1];
    final NButton[] btn_Texmap = new NButton[1];
    final NButton[] btn_Annotate = new NButton[1];

    private Composite toolBar;
    private ToolItem toolItem_ColourBar;
    /**
     * The reference to the underlying business logic (only for testing
     * purpose!)
     */
    ApplicationWindow editorTextWindow;
    CompositeTabFolder[] tabFolder = new CompositeTabFolder[1];

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
        StatusLineManager status = new StatusLineManager();
        return status;
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


    private void addColorButton(ToolItem toolItem_Colours, GColour gColour, final int index) {
        int cn = gColour.getColourNumber();
        if (cn != -1 && View.hasLDConfigColour(cn)) {
            gColour = View.getLDConfigColour(cn);
        }
        final int imgSize = IconSize.getImageSizeFromIconSize();
        final GColour[] gColour2 = new GColour[] { gColour };
        final Color[] col = new Color[1];
        col[0] = SWTResourceManager.getColor((int) (gColour2[0].getR() * 255f), (int) (gColour2[0].getG() * 255f), (int) (gColour2[0].getB() * 255f));

        final NButton btn_Col = new NButton(toolItem_Colours, Cocoa.getStyle());
        btn_Col.setData(gColour2);
        int num = gColour2[0].getColourNumber();
        if (!View.hasLDConfigColour(num)) {
            num = -1;
        }
        if (num != -1) {

            Object[] messageArguments = {num, View.getLDConfigColourName(num)};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.LOCALE);
            formatter.applyPattern(I18n.EDITORTEXT_Colour1 + Cocoa.replaceCtrlByCmd(I18n.E3D_ControlClickModify));

            btn_Col.setToolTipText(formatter.format(messageArguments));
        } else {

            StringBuilder colourBuilder = new StringBuilder();
            colourBuilder.append("0x2"); //$NON-NLS-1$
            colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getR())).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getG())).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getB())).toUpperCase());

            Object[] messageArguments = {colourBuilder.toString()};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.LOCALE);
            formatter.applyPattern(I18n.EDITORTEXT_Colour2 + Cocoa.replaceCtrlByCmd(I18n.E3D_ControlClickModify));

            btn_Col.setToolTipText(formatter.format(messageArguments));
        }

        btn_Col.setImage(ResourceManager.getImage("icon16_fullTransparent.png")); //$NON-NLS-1$

        WidgetUtil(btn_Col).addSelectionListener(e -> {
            if (Cocoa.checkCtrlOrCmdPressed(e.stateMask)) {
                // Choose new colour
                new ColourDialog(btn_Col.getShell(), gColour2, false).run();
                WorkbenchManager.getUserSettingState().getUserPalette().set(index, gColour2[0]);
                col[0] = SWTResourceManager.getColor((int) (gColour2[0].getR() * 255f), (int) (gColour2[0].getG() * 255f), (int) (gColour2[0].getB() * 255f));
                int num1 = gColour2[0].getColourNumber();
                if (View.hasLDConfigColour(num1)) {
                    gColour2[0] = View.getLDConfigColour(num1);
                } else {
                    num1 = -1;
                }
                if (num1 != -1) {
                    Object[] messageArguments1 = {num1, View.getLDConfigColourName(num1)};
                    MessageFormat formatter1 = new MessageFormat(""); //$NON-NLS-1$
                    formatter1.setLocale(MyLanguage.LOCALE);
                    formatter1.applyPattern(I18n.EDITORTEXT_Colour1 + Cocoa.replaceCtrlByCmd(I18n.E3D_ControlClickModify));

                    btn_Col.setToolTipText(formatter1.format(messageArguments1));
                } else {
                    StringBuilder colourBuilder = new StringBuilder();
                    colourBuilder.append("0x2"); //$NON-NLS-1$
                    colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getR())).toUpperCase());
                    colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getG())).toUpperCase());
                    colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getB())).toUpperCase());

                    Object[] messageArguments2 = {colourBuilder.toString()};
                    MessageFormat formatter2 = new MessageFormat(""); //$NON-NLS-1$
                    formatter2.setLocale(MyLanguage.LOCALE);
                    formatter2.applyPattern(I18n.EDITORTEXT_Colour2 + Cocoa.replaceCtrlByCmd(I18n.E3D_ControlClickModify));

                    btn_Col.setToolTipText(formatter2.format(messageArguments2));

                }
                Editor3DWindow.reloadAllColours();
            } else {
                int num2 = gColour2[0].getColourNumber();
                if (View.hasLDConfigColour(num2)) {
                    gColour2[0] = View.getLDConfigColour(num2);
                } else {
                    num2 = -1;
                }

                CompositeTab selection = (CompositeTab) tabFolder[0].getSelection();
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
        final Point size = btn_Col.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        final int x = Math.round(size.x / 5f);
        final int y = Math.round(size.y / 5f);
        final int w = Math.round(size.x * (3f / 5f));
        final int h = Math.round(size.y * (3f / 5f));
        btn_Col.addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                e.gc.setBackground(col[0]);
                e.gc.fillRectangle(x, y, w, h);
                if (gColour2[0].getA() == 1f) {
                    e.gc.drawImage(ResourceManager.getImage("icon16_transparent.png"), 0, 0, imgSize, imgSize, x, y, w, h); //$NON-NLS-1$
                } else {
                    e.gc.drawImage(ResourceManager.getImage("icon16_halftrans.png"), 0, 0, imgSize, imgSize, x, y, w, h); //$NON-NLS-1$
                }
            }
        });
    }

    public void reloadColours() {
        for (Control ctrl : toolItem_ColourBar.getChildren()) {
            if (!(ctrl instanceof ToolSeparator)) ctrl.dispose();
        }

        List<GColour> colours = WorkbenchManager.getUserSettingState().getUserPalette();

        final int size = colours.size();
        for (int i = 0; i < size; i++) {
            addColorButton(toolItem_ColourBar, colours.get(i), i);
        }

        {
            NButton btn_Palette = new NButton(toolItem_ColourBar, Cocoa.getStyle());
            this.btn_Palette[0] = btn_Palette;
            btn_Palette.setToolTipText(I18n.E3D_More);
            btn_Palette.setImage(ResourceManager.getImage("icon16_colours.png")); //$NON-NLS-1$
        }

        toolItem_ColourBar.getParent().layout();
        toolItem_ColourBar.layout();
        toolItem_ColourBar.redraw();
    }

    public Control build() {
        editorTextWindow.setStatus(I18n.E3D_ReadyStatus);
        Composite container = new Composite(parent, Cocoa.getStyle());
        container.setLayout(new BorderLayout(0, 0));
        toolBar = new Composite(container, Cocoa.getStyle());
        toolBar.setLayoutData(BorderLayout.NORTH);
        RowLayout rl_toolBar = new RowLayout(SWT.HORIZONTAL);
        rl_toolBar.center = true;
        toolBar.setLayout(rl_toolBar);

        {
            final int TEXT_3D_SEPARATE = 0;
            final int TEXT_LEFT_3D_RIGHT = 1;
            final UserSettingState userSettings = WorkbenchManager.getUserSettingState();
            if (userSettings.getTextWinArr() != TEXT_3D_SEPARATE) {
                ToolItem toolItem_SashResize = new ToolItem(toolBar, Cocoa.getStyle(), true);
                if (userSettings.getTextWinArr() == TEXT_LEFT_3D_RIGHT) {
                    NButton btn_showLeft = new NButton(toolItem_SashResize, Cocoa.getStyle());
                    this.btn_showLeft[0] = btn_showLeft;
                    btn_showLeft.setToolTipText(I18n.E3D_SashLeft);
                    btn_showLeft.setImage(ResourceManager.getImage("icon16_leftSash.png")); //$NON-NLS-1$
                } else {
                    NButton btn_sameWidth = new NButton(toolItem_SashResize, Cocoa.getStyle());
                    this.btn_sameWidth[0] = btn_sameWidth;
                    btn_sameWidth.setToolTipText(I18n.E3D_SashSameWidth);
                    btn_sameWidth.setImage(ResourceManager.getImage("icon16_sameWidth.png")); //$NON-NLS-1$
                    NButton btn_showRight = new NButton(toolItem_SashResize, Cocoa.getStyle());
                    this.btn_showRight[0] = btn_showRight;
                    btn_showRight.setToolTipText(I18n.E3D_SashRight);
                    btn_showRight.setImage(ResourceManager.getImage("icon16_rightSash.png")); //$NON-NLS-1$
                }
            }
        }

        ToolItem toolItem_NewOpenSave = new ToolItem(toolBar, Cocoa.getStyle(), true);
        {
            NButton btn_New = new NButton(toolItem_NewOpenSave, Cocoa.getStyle());
            this.btn_New[0] = btn_New;
            btn_New.setToolTipText(I18n.E3D_NewDat);
            btn_New.setImage(ResourceManager.getImage("icon16_document-newdat.png")); //$NON-NLS-1$
        }
        {
            NButton btn_Open = new NButton(toolItem_NewOpenSave, Cocoa.getStyle());
            this.btn_Open[0] = btn_Open;
            btn_Open.setToolTipText(I18n.E3D_OpenDat);
            btn_Open.setImage(ResourceManager.getImage("icon16_document-opendat.png")); //$NON-NLS-1$
        }
        {
            NButton btn_Save = new NButton(toolItem_NewOpenSave, Cocoa.getStyle());
            this.btn_Save[0] = btn_Save;
            KeyStateManager.addTooltipText(btn_Save, I18n.E3D_Save, Task.SAVE);
            btn_Save.setImage(ResourceManager.getImage("icon16_document-savedat.png")); //$NON-NLS-1$
        }
        {
            NButton btn_SaveAs = new NButton(toolItem_NewOpenSave, Cocoa.getStyle());
            this.btn_SaveAs[0] = btn_SaveAs;
            btn_SaveAs.setToolTipText(I18n.E3D_SaveAs);
            btn_SaveAs.setImage(ResourceManager.getImage("icon16_document-savedat.png")); //$NON-NLS-1$
            btn_SaveAs.setText("..."); //$NON-NLS-1$
        }
        ToolItem toolItem_HideUnhide = new ToolItem(toolBar, Cocoa.getStyle(), true);
        {
            NButton btn_Hide = new NButton(toolItem_HideUnhide, Cocoa.getStyle());
            this.btn_Hide[0] = btn_Hide;
            btn_Hide.setToolTipText(I18n.EDITORTEXT_Hide);
            btn_Hide.setImage(ResourceManager.getImage("icon16_hide.png")); //$NON-NLS-1$
        }
        {
            NButton btn_Show = new NButton(toolItem_HideUnhide, Cocoa.getStyle());
            this.btn_Show[0] = btn_Show;
            btn_Show.setToolTipText(I18n.EDITORTEXT_Show);
            btn_Show.setImage(ResourceManager.getImage("icon16_unhide.png")); //$NON-NLS-1$
        }
        ToolItem toolItem_UndoRedo = new ToolItem(toolBar, Cocoa.getStyle(), true);
        {
            NButton btn_Undo = new NButton(toolItem_UndoRedo, Cocoa.getStyle());
            this.btn_Undo[0] = btn_Undo;
            btn_Undo.setImage(ResourceManager.getImage("icon16_undo.png")); //$NON-NLS-1$
            KeyStateManager.addTooltipText(btn_Undo, I18n.E3D_Undo, TextTask.EDITORTEXT_UNDO);
        }
        if (NLogger.DEBUG) {
            NButton btn_Snapshot = new NButton(toolItem_UndoRedo, Cocoa.getStyle());
            this.btn_AddHistory[0] = btn_Snapshot;
            btn_Snapshot.setImage(ResourceManager.getImage("icon16_snapshot.png")); //$NON-NLS-1$
            btn_Snapshot.setToolTipText(I18n.E3D_Snapshot);
        }
        {
            NButton btn_Redo = new NButton(toolItem_UndoRedo, Cocoa.getStyle());
            this.btn_Redo[0] = btn_Redo;
            btn_Redo.setImage(ResourceManager.getImage("icon16_redo.png")); //$NON-NLS-1$
            KeyStateManager.addTooltipText(btn_Redo, I18n.E3D_Redo, TextTask.EDITORTEXT_REDO);
        }
        ToolItem toolItem_CCPD = new ToolItem(toolBar, Cocoa.getStyle(), true);
        {
            NButton btn_Cut = new NButton(toolItem_CCPD, Cocoa.getStyle());
            this.btn_Cut[0] = btn_Cut;
            btn_Cut.setImage(ResourceManager.getImage("icon16_edit-cut.png")); //$NON-NLS-1$
            KeyStateManager.addTooltipText(btn_Cut, I18n.COPYNPASTE_Cut, Task.CUT);
        }
        {
            NButton btn_Copy = new NButton(toolItem_CCPD, Cocoa.getStyle());
            this.btn_Copy[0] = btn_Copy;
            btn_Copy.setImage(ResourceManager.getImage("icon16_edit-copy.png")); //$NON-NLS-1$
            KeyStateManager.addTooltipText(btn_Copy, I18n.COPYNPASTE_Copy, Task.COPY);
        }
        {
            NButton btn_Paste = new NButton(toolItem_CCPD, Cocoa.getStyle());
            this.btn_Paste[0] = btn_Paste;
            btn_Paste.setImage(ResourceManager.getImage("icon16_edit-paste.png")); //$NON-NLS-1$
            KeyStateManager.addTooltipText(btn_Paste, I18n.COPYNPASTE_Paste, Task.PASTE);
        }
        {
            NButton btn_Delete = new NButton(toolItem_CCPD, Cocoa.getStyle());
            this.btn_Delete[0] = btn_Delete;
            btn_Delete.setImage(ResourceManager.getImage("icon16_delete.png")); //$NON-NLS-1$
            KeyStateManager.addTooltipText(btn_Delete, I18n.COPYNPASTE_Delete, Task.DELETE);
        }
        ToolItem toolItem_Debug = new ToolItem(toolBar, Cocoa.getStyle(), true);
        {
            NButton btn_ShowErrors = new NButton(toolItem_Debug, Cocoa.getStyle());
            this.btn_ShowErrors[0] = btn_ShowErrors;
            btn_ShowErrors.setImage(ResourceManager.getImage("icon16_error.png")); //$NON-NLS-1$
            btn_ShowErrors.setToolTipText(I18n.EDITORTEXT_ShowHideErrorTab);
        }
        {
            NButton btn_FindAndReplace = new NButton(toolItem_Debug, Cocoa.getStyle());
            this.btn_FindAndReplace[0] = btn_FindAndReplace;
            btn_FindAndReplace.setImage(ResourceManager.getImage("icon16_findReplace.png")); //$NON-NLS-1$
            KeyStateManager.addTooltipText(btn_FindAndReplace, I18n.EDITORTEXT_FindReplace, TextTask.EDITORTEXT_FIND);
        }
        {
            NButton btn_Sort = new NButton(toolItem_Debug, Cocoa.getStyle());
            this.btn_Sort[0] = btn_Sort;
            btn_Sort.setImage(ResourceManager.getImage("icon16_sort.png")); //$NON-NLS-1$
            btn_Sort.setToolTipText(I18n.EDITORTEXT_Sort);
        }
        {
            NButton btn_SplitQuad = new NButton(toolItem_Debug, Cocoa.getStyle());
            this.btn_SplitQuad[0] = btn_SplitQuad;
            btn_SplitQuad.setImage(ResourceManager.getImage("icon16_quadToTri.png")); //$NON-NLS-1$
            btn_SplitQuad.setToolTipText(I18n.EDITORTEXT_SplitQuad);
        }
        {
            NButton btn_MergeQuad = new NButton(toolItem_Debug, Cocoa.getStyle());
            this.btn_MergeQuad[0] = btn_MergeQuad;
            btn_MergeQuad.setImage(ResourceManager.getImage("icon16_triToquad.png")); //$NON-NLS-1$
            btn_MergeQuad.setToolTipText(I18n.EDITORTEXT_MergeQuad);
        }
        {
            NButton btn_ShowSelectionIn3D = new NButton(toolItem_Debug, Cocoa.getStyle());
            this.btn_ShowSelectionIn3D[0] = btn_ShowSelectionIn3D;
            btn_ShowSelectionIn3D.setImage(ResourceManager.getImage("icon16_text2selection.png")); //$NON-NLS-1$
            btn_ShowSelectionIn3D.setToolTipText(I18n.EDITORTEXT_ShowSelectionIn3D);
        }
        {
            NButton btn_OpenIn3D = new NButton(toolItem_Debug, Cocoa.getStyle());
            this.btn_OpenIn3D[0] = btn_OpenIn3D;
            btn_OpenIn3D.setImage(ResourceManager.getImage("icon16_openIn3D.png")); //$NON-NLS-1$
            btn_OpenIn3D.setToolTipText(I18n.E3D_OpenIn3DEditor);
        }
        {
            NButton btn_Unrectify = new NButton(toolItem_Debug, Cocoa.getStyle());
            this.btn_Unrectify[0] = btn_Unrectify;
            btn_Unrectify.setImage(ResourceManager.getImage("icon16_unrectify.png")); //$NON-NLS-1$
            btn_Unrectify.setToolTipText(Cocoa.replaceCtrlByCmd(I18n.EDITORTEXT_Unrectify));
        }
        {
            NButton btn_SyncEdit = new NButton(toolItem_Debug, Cocoa.getStyle());
            this.btn_SyncEdit[0] = btn_SyncEdit;
            btn_SyncEdit.setImage(ResourceManager.getImage("icon16_syncedit.png")); //$NON-NLS-1$
            KeyStateManager.addTooltipText(btn_SyncEdit, I18n.EDITORTEXT_SyncEditButton, TextTask.EDITORTEXT_REPLACE_VERTEX);
        }
        {
            NButton btn_Inline = new NButton(toolItem_Debug, Cocoa.getStyle());
            this.btn_Inline[0] = btn_Inline;
            btn_Inline.setImage(ResourceManager.getImage("icon16_inline.png")); //$NON-NLS-1$
            KeyStateManager.addTooltipText(btn_Inline, I18n.EDITORTEXT_Inline1, TextTask.EDITORTEXT_INLINE);
        }
        {
            NButton btn_InlineDeep = new NButton(toolItem_Debug, Cocoa.getStyle());
            this.btn_InlineDeep[0] = btn_InlineDeep;
            btn_InlineDeep.setImage(ResourceManager.getImage("icon16_inlinedeep.png")); //$NON-NLS-1$
            btn_InlineDeep.setToolTipText(I18n.EDITORTEXT_Inline2);
        }
        {
            NButton btn_InlineLinked = new NButton(toolItem_Debug, Cocoa.getStyle());
            this.btn_InlineLinked[0] = btn_InlineLinked;
            btn_InlineLinked.setImage(ResourceManager.getImage("icon16_inlinelinked.png")); //$NON-NLS-1$
            btn_InlineLinked.setToolTipText(I18n.EDITORTEXT_Inline3);
        }
        {
            NButton btn_BFCswap = new NButton(toolItem_Debug, Cocoa.getStyle());
            this.btn_BFCswap[0] = btn_BFCswap;
            btn_BFCswap.setToolTipText(I18n.E3D_SwapWinding);
            btn_BFCswap.setImage(ResourceManager.getImage("icon16_bfcSwap.png")); //$NON-NLS-1$
        }
        {
            NButton btn_CompileSubfile = new NButton(toolItem_Debug, Cocoa.getStyle());
            this.btn_CompileSubfile[0] = btn_CompileSubfile;
            btn_CompileSubfile.setToolTipText(I18n.EDITORTEXT_Compile);
            btn_CompileSubfile.setImage(ResourceManager.getImage("icon16_subcompile.png")); //$NON-NLS-1$
        }
        {
            NButton btn_RoundSelection = new NButton(toolItem_Debug, Cocoa.getStyle());
            this.btn_RoundSelection[0] = btn_RoundSelection;
            KeyStateManager.addTooltipText(btn_RoundSelection, I18n.EDITORTEXT_Round + Cocoa.replaceCtrlByCmd(I18n.E3D_ControlClickModify), TextTask.EDITORTEXT_ROUND);
            btn_RoundSelection.setImage(ResourceManager.getImage("icon16_round.png")); //$NON-NLS-1$
        }

        {
            NButton btn_Texmap = new NButton(toolItem_Debug, Cocoa.getStyle());
            this.btn_Texmap[0] = btn_Texmap;
            btn_Texmap.setToolTipText(I18n.EDITORTEXT_Texmap);
            btn_Texmap.setImage(ResourceManager.getImage("icon16_texmap.png")); //$NON-NLS-1$
        }

        {
            NButton btn_Annotate = new NButton(toolItem_Debug, Cocoa.getStyle());
            this.btn_Annotate[0] = btn_Annotate;
            btn_Annotate.setToolTipText(I18n.EDITORTEXT_Comment);
            btn_Annotate.setImage(ResourceManager.getImage("icon16_annotate.png")); //$NON-NLS-1$
        }

        {
            ToolItem toolItem_Colours = new ToolItem(toolBar, Cocoa.getStyle(), true);
            toolItem_ColourBar = toolItem_Colours;
            List<GColour> colours = WorkbenchManager.getUserSettingState().getUserPalette();

            final int size = colours.size();
            for (int i = 0; i < size; i++) {
                addColorButton(toolItem_Colours, colours.get(i), i);
            }

            {
                NButton btn_Palette = new NButton(toolItem_Colours, Cocoa.getStyle());
                this.btn_Palette[0] = btn_Palette;
                btn_Palette.setToolTipText(I18n.E3D_More);
                btn_Palette.setImage(ResourceManager.getImage("icon16_colours.png")); //$NON-NLS-1$
            }
        }

        {
            Composite cmp_text_editor = new Composite(container, SWT.BORDER);
            cmp_text_editor.setLayoutData(BorderLayout.CENTER);
            cmp_text_editor.setLayout(new FillLayout(SWT.HORIZONTAL));
            {
                CompositeTabFolder tabFolder = new CompositeTabFolder(cmp_text_editor, SWT.BORDER);
                this.tabFolder[0] = tabFolder;
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
