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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.nschmidt.ldparteditor.enums.Colour;
import org.nschmidt.ldparteditor.enums.Font;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.resources.ResourceManager;
import org.nschmidt.ldparteditor.widgets.NButton;
import org.nschmidt.ldparteditor.widgets.Tree;
import org.nschmidt.ldparteditor.widgets.TreeColumn;
import org.nschmidt.ldparteditor.widgets.TreeItem;

/**
 * The DAT file tab design for the text editor window
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 *
 * @author nils
 *
 */
class CompositeTabDesign extends CTabItem {

    /** The menu of this composite */
    private final Menu[] menu = new Menu[1];

    final MenuItem[] mntm_HideSelection = new MenuItem[1];
    final MenuItem[] mntm_ShowSelection = new MenuItem[1];
    final MenuItem[] mntm_ShowAll = new MenuItem[1];
    final MenuItem[] mntm_DrawSelection = new MenuItem[1];
    final MenuItem[] mntm_DrawUntilSelection = new MenuItem[1];

    final MenuItem[] mntm_Copy = new MenuItem[1];
    final MenuItem[] mntm_Cut = new MenuItem[1];
    final MenuItem[] mntm_Delete = new MenuItem[1];
    final MenuItem[] mntm_Paste = new MenuItem[1];

    final MenuItem[] mntm_QuickFix = new MenuItem[1];
    final MenuItem[] mntm_QuickFixSame = new MenuItem[1];
    final NButton[] btn_QuickFix = new NButton[1];
    final NButton[] btn_QuickFixSame = new NButton[1];

    final MenuItem[] mntm_Inspect = new MenuItem[1];
    final MenuItem[] mntm_InspectSame = new MenuItem[1];
    final NButton[] btn_Inspect = new NButton[1];
    final NButton[] btn_InspectSame = new NButton[1];

    final Canvas[] canvas_lineNumberArea = new Canvas[1];
    final StyledText[] compositeText = new StyledText[1];
    final Label[] lbl_ProblemCount = new Label[1];
    final Composite[] compositeContainer = new Composite[1];
    final CTabFolder[] tabFolder_partInformation = new CTabFolder[1];
    final SashForm[] sashForm = new SashForm[1];

    final TreeItem[] treeItem_Hints = new TreeItem[1];
    final TreeItem[] treeItem_Warnings = new TreeItem[1];
    final TreeItem[] treeItem_Errors = new TreeItem[1];
    final TreeItem[] treeItem_Duplicates = new TreeItem[1];

    final Tree[] tree_Problems = new Tree[1];

    public CompositeTabDesign(CTabFolder tabFolder, int style) {
        super(tabFolder, style);
        createContents(tabFolder);
    }

    public CompositeTabDesign(CTabFolder tabFolder, int style, int index) {
        super(tabFolder, style, index);
        createContents(tabFolder);
    }

    private final void createContents(CTabFolder tabFolder) {
        this.setText(I18n.COMPOSITETAB_NEW_FILE);
        {
            Composite cmpTextArea = new Composite(tabFolder, SWT.NONE);
            cmpTextArea.setLayout(new FillLayout(SWT.HORIZONTAL));
            this.setControl(cmpTextArea);

            SashForm sashForm = new SashForm(cmpTextArea, SWT.VERTICAL);
            this.sashForm[0] = sashForm;

            Composite composite = new Composite(sashForm, SWT.NONE);
            compositeContainer[0] = composite;
            composite.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
            GridLayout glComposite = new GridLayout(3, false);
            glComposite.marginBottom = 0;
            glComposite.marginHeight = 0;
            glComposite.marginLeft = 0;
            glComposite.marginRight = 0;
            glComposite.marginTop = 0;
            glComposite.marginWidth = 0;
            composite.setLayout(glComposite);

            Canvas canvasLineNumberArea = new Canvas(composite, SWT.NONE);
            this.canvas_lineNumberArea[0] = canvasLineNumberArea;
            canvasLineNumberArea.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
            canvasLineNumberArea.setCursor(new Cursor(Display.getCurrent(), SWT.CURSOR_HAND));
            this.canvas_lineNumberArea[0].addDisposeListener(new DisposeListener() {
                @Override
                public void widgetDisposed(DisposeEvent e) {
                    canvas_lineNumberArea[0].getCursor().dispose();
                }
            });

            GridData gdCanvasLineNumberArea = new GridData(SWT.LEFT, SWT.FILL, false, true);
            canvasLineNumberArea.setLayoutData(gdCanvasLineNumberArea);

            StyledText compositeText = new StyledText(composite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
            this.compositeText[0] = compositeText;
            compositeText.getVerticalBar().setPageIncrement(100);
            GridData gdCompositeText = new GridData(SWT.FILL, SWT.FILL, true, true);
            gdCompositeText.minimumHeight = 50;
            gdCompositeText.minimumWidth = 50;
            compositeText.setLayoutData(gdCompositeText);

            compositeText.setBackground(Colour.text_background[0]);
            compositeText.setForeground(Colour.text_foreground[0]);
            compositeText.setFont(Font.MONOSPACE);
            compositeText.setLineSpacing(0);

            Canvas canvasTextmarks = new Canvas(composite, SWT.NONE);
            canvasTextmarks.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));

            GridData gdCanvasTextmarks = new GridData(SWT.RIGHT, SWT.FILL, false, true);
            gdCanvasTextmarks.minimumWidth = 5;
            gdCanvasTextmarks.widthHint = 5;
            canvasTextmarks.setLayoutData(gdCanvasTextmarks);

            {
                Composite cmpInfoArea = new Composite(sashForm, SWT.NONE);
                cmpInfoArea.setLayout(new FillLayout(SWT.HORIZONTAL));

                CTabFolder tabFolderPartInformation = new CTabFolder(cmpInfoArea, SWT.BORDER);
                this.tabFolder_partInformation[0] = tabFolderPartInformation;
                tabFolderPartInformation.setSingle(true);
                tabFolderPartInformation.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));

                CTabItem tbtmPartProblems = new CTabItem(tabFolderPartInformation, SWT.CLOSE);
                tbtmPartProblems.setImage(null);
                tbtmPartProblems.setText(I18n.COMPOSITETAB_PROBLEMS);
                tabFolderPartInformation.setSelection(tbtmPartProblems);
                Composite cmpPartProblems = new Composite(tabFolderPartInformation, SWT.NONE);
                tbtmPartProblems.setControl(cmpPartProblems);
                cmpPartProblems.setLayout(new GridLayout(5, false));

                Label lblProblemCount = new Label(cmpPartProblems, SWT.NONE);
                this.lbl_ProblemCount[0] = lblProblemCount;
                lblProblemCount.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 5, 1));
                lblProblemCount.setText("0 " + I18n.EDITORTEXT_ERRORS + ", 0 " + I18n.EDITORTEXT_WARNINGS + ", 0 " + I18n.EDITORTEXT_OTHERS + ", 0 " + I18n.EDITORTEXT_DUPLICATES); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

                Tree tree = new Tree(cmpPartProblems, SWT.BORDER | SWT.MULTI, 128);

                tree_Problems[0] = tree;
                tree.setLinesVisible(true);
                tree.setHeaderVisible(true);
                tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 5, 1));

                TreeColumn trclmnDescription = new TreeColumn(tree, SWT.NONE);
                trclmnDescription.setWidth(598);
                trclmnDescription.setText(I18n.COMPOSITETAB_DESCRIPTION);

                TreeColumn trclmnLocation = new TreeColumn(tree, SWT.NONE);
                trclmnLocation.setWidth(100);
                trclmnLocation.setText(I18n.COMPOSITETAB_LOCATION);

                TreeColumn trclmnType = new TreeColumn(tree, SWT.NONE);
                trclmnType.setWidth(100);
                trclmnType.setText(I18n.COMPOSITETAB_TYPE);

                TreeItem trtmHints = new TreeItem(tree);
                treeItem_Hints[0] = trtmHints;
                trtmHints.setImage(ResourceManager.getImage("icon16_info.png")); //$NON-NLS-1$
                trtmHints.setText(new String[] { I18n.COMPOSITETAB_HINTS, "", "" }); //$NON-NLS-1$ //$NON-NLS-2$

                TreeItem trtmWarnings = new TreeItem(tree);
                treeItem_Warnings[0] = trtmWarnings;
                trtmWarnings.setImage(ResourceManager.getImage("icon16_warning.png")); //$NON-NLS-1$
                trtmWarnings.setText(new String[] { I18n.COMPOSITETAB_WARNINGS, "", "" }); //$NON-NLS-1$ //$NON-NLS-2$

                TreeItem trtmErrors = new TreeItem(tree);
                treeItem_Errors[0] = trtmErrors;
                trtmErrors.setImage(ResourceManager.getImage("icon16_error.png")); //$NON-NLS-1$
                trtmErrors.setText(new String[] { I18n.COMPOSITETAB_ERRORS, "", "" }); //$NON-NLS-1$ //$NON-NLS-2$

                TreeItem trtmDuplicates = new TreeItem(tree);
                treeItem_Duplicates[0] = trtmDuplicates;
                trtmDuplicates.setImage(ResourceManager.getImage("icon16_duplicate.png")); //$NON-NLS-1$
                trtmDuplicates.setText(new String[] { I18n.COMPOSITETAB_DUPLICATES, "", "" }); //$NON-NLS-1$ //$NON-NLS-2$

                Menu menu = new Menu(tabFolder);
                tree.setMenu(menu);

                MenuItem mntmQuickFix = new MenuItem(menu, I18n.noBiDirectionalTextStyle());
                mntm_QuickFix[0] = mntmQuickFix;
                mntmQuickFix.setEnabled(true);
                mntmQuickFix.setText(I18n.COMPOSITETAB_QUICK_FIX);

                MenuItem mntmQuickFixSame = new MenuItem(menu, I18n.noBiDirectionalTextStyle());
                mntm_QuickFixSame[0] = mntmQuickFixSame;
                mntmQuickFixSame.setEnabled(true);
                mntmQuickFixSame.setText(I18n.COMPOSITETAB_QUICK_FIX_SIMILAR);

                MenuItem mntmInspect = new MenuItem(menu, I18n.noBiDirectionalTextStyle());
                mntm_Inspect[0] = mntmInspect;
                mntmInspect.setEnabled(true);
                mntmInspect.setText(I18n.COMPOSITETAB_INSPECT);

                MenuItem mntmInspectSame = new MenuItem(menu, I18n.noBiDirectionalTextStyle());
                mntm_InspectSame[0] = mntmInspectSame;
                mntmInspectSame.setEnabled(true);
                mntmInspectSame.setText(I18n.COMPOSITETAB_INSPECT_SIMILAR);

                tree.build();

                Label lblSeparator = new Label(cmpPartProblems, SWT.NONE);
                lblSeparator.setText(" "); //$NON-NLS-1$

                NButton btnInspect = new NButton(cmpPartProblems, SWT.NONE);
                this.btn_Inspect[0] = btnInspect;
                btnInspect.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
                btnInspect.setEnabled(false);
                btnInspect.setImage(ResourceManager.getImage("icon16_inspect.png")); //$NON-NLS-1$
                btnInspect.setText(I18n.COMPOSITETAB_INSPECT);

                NButton btnInspectSame = new NButton(cmpPartProblems, SWT.NONE);
                this.btn_InspectSame[0] = btnInspectSame;
                btnInspectSame.setEnabled(false);
                btnInspectSame.setImage(ResourceManager.getImage("icon16_inspect.png")); //$NON-NLS-1$
                btnInspectSame.setText(I18n.COMPOSITETAB_INSPECT_SIMILAR);

                NButton btnQuickFix = new NButton(cmpPartProblems, SWT.NONE);
                this.btn_QuickFix[0] = btnQuickFix;
                btnQuickFix.setEnabled(false);
                btnQuickFix.setImage(ResourceManager.getImage("icon16_info.png")); //$NON-NLS-1$
                btnQuickFix.setText(I18n.COMPOSITETAB_QUICK_FIX);

                NButton btnQuickFixSame = new NButton(cmpPartProblems, SWT.NONE);
                this.btn_QuickFixSame[0] = btnQuickFixSame;
                btnQuickFixSame.setEnabled(false);
                btnQuickFixSame.setImage(ResourceManager.getImage("icon16_info.png")); //$NON-NLS-1$
                btnQuickFixSame.setText(I18n.COMPOSITETAB_QUICK_FIX_SIMILAR);
            }
            composite.pack();
            sashForm.setWeights(new int[] { 2, 1 } );

            this.menu[0] = new Menu(compositeText);
            compositeText.setMenu(this.menu[0]);

            MenuItem mntmHide = new MenuItem(menu[0], I18n.rightToLeftStyle());
            mntmHide.setText(I18n.EDITORTEXT_HIDE);
            mntmHide.setImage(ResourceManager.getImage("icon16_hide.png")); //$NON-NLS-1$
            mntm_HideSelection[0] = mntmHide;

            MenuItem mntmShowSelection = new MenuItem(menu[0], I18n.rightToLeftStyle());
            mntmShowSelection.setText(I18n.EDITORTEXT_SHOW);
            mntmShowSelection.setImage(ResourceManager.getImage("icon16_unhide.png")); //$NON-NLS-1$
            mntm_ShowSelection[0] = mntmShowSelection;

            MenuItem mntmShowAll = new MenuItem(menu[0], I18n.rightToLeftStyle());
            mntmShowAll.setText(I18n.E3D_SHOW_ALL);
            mntm_ShowAll[0] = mntmShowAll;

            new MenuItem(menu[0], SWT.SEPARATOR);

            MenuItem mntmDrawSelection = new MenuItem(menu[0], I18n.rightToLeftStyle());
            mntmDrawSelection.setText(I18n.EDITORTEXT_DRAW_SELECTION);
            mntm_DrawSelection[0] = mntmDrawSelection;

            MenuItem mntmDrawUntilSelection = new MenuItem(menu[0], I18n.rightToLeftStyle());
            mntmDrawUntilSelection.setText(I18n.EDITORTEXT_DRAW_UNTIL_SELECTION);
            mntm_DrawUntilSelection[0] = mntmDrawUntilSelection;

            new MenuItem(menu[0], SWT.SEPARATOR);

            MenuItem mntmCut = new MenuItem(menu[0], I18n.rightToLeftStyle());
            mntmCut.setText(I18n.COPYNPASTE_CUT);
            mntmCut.setImage(ResourceManager.getImage("icon16_edit-cut.png")); //$NON-NLS-1$
            mntm_Cut[0] = mntmCut;
            MenuItem mntmCopy = new MenuItem(menu[0], I18n.rightToLeftStyle());
            mntmCopy.setText(I18n.COPYNPASTE_COPY);
            mntmCopy.setImage(ResourceManager.getImage("icon16_edit-copy.png")); //$NON-NLS-1$
            mntm_Copy[0] = mntmCopy;
            MenuItem mntmPaste = new MenuItem(menu[0], I18n.rightToLeftStyle());
            mntmPaste.setText(I18n.COPYNPASTE_PASTE);
            mntmPaste.setImage(ResourceManager.getImage("icon16_edit-paste.png")); //$NON-NLS-1$
            mntm_Paste[0] = mntmPaste;
            MenuItem mntmDelete = new MenuItem(menu[0], I18n.rightToLeftStyle());
            mntmDelete.setText(I18n.COPYNPASTE_DELETE);
            mntmDelete.setImage(ResourceManager.getImage("icon16_delete.png")); //$NON-NLS-1$
            mntm_Delete[0] = mntmDelete;
        }
    }
}
