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
    final Menu[] menu = new Menu[1];

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
        this.setText(I18n.COMPOSITETAB_NewFile);
        {
            Composite cmp_textArea = new Composite(tabFolder, SWT.NONE);
            cmp_textArea.setLayout(new FillLayout(SWT.HORIZONTAL));
            this.setControl(cmp_textArea);

            SashForm sashForm = new SashForm(cmp_textArea, SWT.VERTICAL);
            this.sashForm[0] = sashForm;

            Composite composite = new Composite(sashForm, SWT.NONE);
            compositeContainer[0] = composite;
            composite.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
            GridLayout gl_composite = new GridLayout(3, false);
            gl_composite.marginBottom = 0;
            gl_composite.marginHeight = 0;
            gl_composite.marginLeft = 0;
            gl_composite.marginRight = 0;
            gl_composite.marginTop = 0;
            gl_composite.marginWidth = 0;
            composite.setLayout(gl_composite);

            Canvas canvas_lineNumber_Area = new Canvas(composite, SWT.NONE);
            this.canvas_lineNumberArea[0] = canvas_lineNumber_Area;
            canvas_lineNumber_Area.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
            canvas_lineNumber_Area.setCursor(new Cursor(Display.getCurrent(), SWT.CURSOR_HAND));
            this.canvas_lineNumberArea[0].addDisposeListener(new DisposeListener() {
                @Override
                public void widgetDisposed(DisposeEvent e) {
                    canvas_lineNumberArea[0].getCursor().dispose();
                }
            });

            GridData gd_canvas_lineNumberArea = new GridData(SWT.LEFT, SWT.FILL, false, true);
            canvas_lineNumber_Area.setLayoutData(gd_canvas_lineNumberArea);

            StyledText compositeText = new StyledText(composite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
            this.compositeText[0] = compositeText;
            compositeText.getVerticalBar().setPageIncrement(100);
            GridData gd_compositeText = new GridData(SWT.FILL, SWT.FILL, true, true);
            gd_compositeText.minimumHeight = 50;
            gd_compositeText.minimumWidth = 50;
            compositeText.setLayoutData(gd_compositeText);

            compositeText.setBackground(Colour.text_background[0]);
            compositeText.setForeground(Colour.text_foreground[0]);
            compositeText.setFont(Font.MONOSPACE);
            compositeText.setLineSpacing(0);

            Canvas canvas_textmarks = new Canvas(composite, SWT.NONE);
            canvas_textmarks.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));

            GridData gd_canvas_textmarks = new GridData(SWT.RIGHT, SWT.FILL, false, true);
            gd_canvas_textmarks.minimumWidth = 5;
            gd_canvas_textmarks.widthHint = 5;
            canvas_textmarks.setLayoutData(gd_canvas_textmarks);

            {
                Composite cmp_InfoArea = new Composite(sashForm, SWT.NONE);
                cmp_InfoArea.setLayout(new FillLayout(SWT.HORIZONTAL));

                CTabFolder tabFolder_partInformation = new CTabFolder(cmp_InfoArea, SWT.BORDER);
                this.tabFolder_partInformation[0] = tabFolder_partInformation;
                tabFolder_partInformation.setSingle(true);
                tabFolder_partInformation.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));

                CTabItem tbtm_partProblems = new CTabItem(tabFolder_partInformation, SWT.CLOSE);
                tbtm_partProblems.setImage(null);
                tbtm_partProblems.setText(I18n.COMPOSITETAB_Problems);
                tabFolder_partInformation.setSelection(tbtm_partProblems);
                Composite cmp_partProblems = new Composite(tabFolder_partInformation, SWT.NONE);
                tbtm_partProblems.setControl(cmp_partProblems);
                cmp_partProblems.setLayout(new GridLayout(5, false));

                Label lbl_ProblemCount = new Label(cmp_partProblems, SWT.NONE);
                this.lbl_ProblemCount[0] = lbl_ProblemCount;
                lbl_ProblemCount.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 5, 1));
                lbl_ProblemCount.setText("0 " + I18n.EDITORTEXT_Errors + ", 0 " + I18n.EDITORTEXT_Warnings + ", 0 " + I18n.EDITORTEXT_Others + ", 0 " + I18n.EDITORTEXT_Duplicates); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

                Tree tree = new Tree(cmp_partProblems, SWT.BORDER | SWT.MULTI, 128);

                tree_Problems[0] = tree;
                tree.setLinesVisible(true);
                tree.setHeaderVisible(true);
                tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 5, 1));

                TreeColumn trclmn_Description = new TreeColumn(tree, SWT.NONE);
                trclmn_Description.setWidth(598);
                trclmn_Description.setText(I18n.COMPOSITETAB_Description);

                TreeColumn trclmn_Location = new TreeColumn(tree, SWT.NONE);
                trclmn_Location.setWidth(100);
                trclmn_Location.setText(I18n.COMPOSITETAB_Location);

                TreeColumn trclmn_Type = new TreeColumn(tree, SWT.NONE);
                trclmn_Type.setWidth(100);
                trclmn_Type.setText(I18n.COMPOSITETAB_Type);

                TreeItem trtm_Hints = new TreeItem(tree, SWT.NONE);
                treeItem_Hints[0] = trtm_Hints;
                trtm_Hints.setImage(ResourceManager.getImage("icon16_info.png")); //$NON-NLS-1$
                trtm_Hints.setText(new String[] { I18n.COMPOSITETAB_Hints, "", "" }); //$NON-NLS-1$ //$NON-NLS-2$

                TreeItem trtm_Warnings = new TreeItem(tree, SWT.NONE);
                treeItem_Warnings[0] = trtm_Warnings;
                trtm_Warnings.setImage(ResourceManager.getImage("icon16_warning.png")); //$NON-NLS-1$
                trtm_Warnings.setText(new String[] { I18n.COMPOSITETAB_Warnings, "", "" }); //$NON-NLS-1$ //$NON-NLS-2$

                TreeItem trtm_Errors = new TreeItem(tree, SWT.NONE);
                treeItem_Errors[0] = trtm_Errors;
                trtm_Errors.setImage(ResourceManager.getImage("icon16_error.png")); //$NON-NLS-1$
                trtm_Errors.setText(new String[] { I18n.COMPOSITETAB_Errors, "", "" }); //$NON-NLS-1$ //$NON-NLS-2$

                TreeItem trtm_Duplicates = new TreeItem(tree, SWT.NONE);
                treeItem_Duplicates[0] = trtm_Duplicates;
                trtm_Duplicates.setImage(ResourceManager.getImage("icon16_duplicate.png")); //$NON-NLS-1$
                trtm_Duplicates.setText(new String[] { I18n.COMPOSITETAB_Duplicates, "", "" }); //$NON-NLS-1$ //$NON-NLS-2$

                Menu menu = new Menu(tabFolder);
                tree.setMenu(menu);

                MenuItem mntmQuickFix = new MenuItem(menu, I18n.I18N_NON_BIDIRECT());
                mntm_QuickFix[0] = mntmQuickFix;
                mntmQuickFix.setEnabled(true);
                mntmQuickFix.setText(I18n.COMPOSITETAB_QuickFix);

                MenuItem mntmQuickFixSame = new MenuItem(menu, I18n.I18N_NON_BIDIRECT());
                mntm_QuickFixSame[0] = mntmQuickFixSame;
                mntmQuickFixSame.setEnabled(true);
                mntmQuickFixSame.setText(I18n.COMPOSITETAB_QuickFixSimilar);

                MenuItem mntmInspect = new MenuItem(menu, I18n.I18N_NON_BIDIRECT());
                mntm_Inspect[0] = mntmInspect;
                mntmInspect.setEnabled(true);
                mntmInspect.setText(I18n.COMPOSITETAB_Inspect);

                MenuItem mntmInspectSame = new MenuItem(menu, I18n.I18N_NON_BIDIRECT());
                mntm_InspectSame[0] = mntmInspectSame;
                mntmInspectSame.setEnabled(true);
                mntmInspectSame.setText(I18n.COMPOSITETAB_InspectSimilar);

                tree.build();

                Label lbl_Separator = new Label(cmp_partProblems, SWT.NONE);
                lbl_Separator.setText(" "); //$NON-NLS-1$

                NButton btn_Inspect = new NButton(cmp_partProblems, SWT.NONE);
                this.btn_Inspect[0] = btn_Inspect;
                btn_Inspect.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
                btn_Inspect.setEnabled(false);
                btn_Inspect.setImage(ResourceManager.getImage("icon16_inspect.png")); //$NON-NLS-1$
                btn_Inspect.setText(I18n.COMPOSITETAB_Inspect);

                NButton btn_InspectSame = new NButton(cmp_partProblems, SWT.NONE);
                this.btn_InspectSame[0] = btn_InspectSame;
                btn_InspectSame.setEnabled(false);
                btn_InspectSame.setImage(ResourceManager.getImage("icon16_inspect.png")); //$NON-NLS-1$
                btn_InspectSame.setText(I18n.COMPOSITETAB_InspectSimilar);

                NButton btn_QuickFix = new NButton(cmp_partProblems, SWT.NONE);
                this.btn_QuickFix[0] = btn_QuickFix;
                btn_QuickFix.setEnabled(false);
                btn_QuickFix.setImage(ResourceManager.getImage("icon16_info.png")); //$NON-NLS-1$
                btn_QuickFix.setText(I18n.COMPOSITETAB_QuickFix);

                NButton btn_QuickFixSame = new NButton(cmp_partProblems, SWT.NONE);
                this.btn_QuickFixSame[0] = btn_QuickFixSame;
                btn_QuickFixSame.setEnabled(false);
                btn_QuickFixSame.setImage(ResourceManager.getImage("icon16_info.png")); //$NON-NLS-1$
                btn_QuickFixSame.setText(I18n.COMPOSITETAB_QuickFixSimilar);
            }
            composite.pack();
            sashForm.setWeights(new int[] { 2, 1 } );

            this.menu[0] = new Menu(compositeText);
            compositeText.setMenu(this.menu[0]);

            MenuItem mntmHide = new MenuItem(menu[0], I18n.I18N_RTL());
            mntmHide.setText(I18n.EDITORTEXT_Hide);
            mntmHide.setImage(ResourceManager.getImage("icon16_hide.png")); //$NON-NLS-1$
            mntm_HideSelection[0] = mntmHide;

            MenuItem mntmShowSelection = new MenuItem(menu[0], I18n.I18N_RTL());
            mntmShowSelection.setText(I18n.EDITORTEXT_Show);
            mntmShowSelection.setImage(ResourceManager.getImage("icon16_unhide.png")); //$NON-NLS-1$
            mntm_ShowSelection[0] = mntmShowSelection;

            MenuItem mntmShowAll = new MenuItem(menu[0], I18n.I18N_RTL());
            mntmShowAll.setText(I18n.E3D_ShowAll);
            mntm_ShowAll[0] = mntmShowAll;

            @SuppressWarnings("unused")
            final MenuItem mntmSeparator0 = new MenuItem(menu[0], SWT.SEPARATOR);

            MenuItem mntmDrawSelection = new MenuItem(menu[0], I18n.I18N_RTL());
            mntmDrawSelection.setText(I18n.EDITORTEXT_DrawSelection);
            mntm_DrawSelection[0] = mntmDrawSelection;

            MenuItem mntmDrawUntilSelection = new MenuItem(menu[0], I18n.I18N_RTL());
            mntmDrawUntilSelection.setText(I18n.EDITORTEXT_DrawUntilSelection);
            mntm_DrawUntilSelection[0] = mntmDrawUntilSelection;

            @SuppressWarnings("unused")
            final MenuItem mntmSeparator1 = new MenuItem(menu[0], SWT.SEPARATOR);

            MenuItem mntmCut = new MenuItem(menu[0], I18n.I18N_RTL());
            mntmCut.setText(I18n.COPYNPASTE_Cut);
            mntmCut.setImage(ResourceManager.getImage("icon16_edit-cut.png")); //$NON-NLS-1$
            mntm_Cut[0] = mntmCut;
            MenuItem mntmCopy = new MenuItem(menu[0], I18n.I18N_RTL());
            mntmCopy.setText(I18n.COPYNPASTE_Copy);
            mntmCopy.setImage(ResourceManager.getImage("icon16_edit-copy.png")); //$NON-NLS-1$
            mntm_Copy[0] = mntmCopy;
            MenuItem mntmPaste = new MenuItem(menu[0], I18n.I18N_RTL());
            mntmPaste.setText(I18n.COPYNPASTE_Paste);
            mntmPaste.setImage(ResourceManager.getImage("icon16_edit-paste.png")); //$NON-NLS-1$
            mntm_Paste[0] = mntmPaste;
            MenuItem mntmDelete = new MenuItem(menu[0], I18n.I18N_RTL());
            mntmDelete.setText(I18n.COPYNPASTE_Delete);
            mntmDelete.setImage(ResourceManager.getImage("icon16_delete.png")); //$NON-NLS-1$
            mntm_Delete[0] = mntmDelete;
        }
    }
}
