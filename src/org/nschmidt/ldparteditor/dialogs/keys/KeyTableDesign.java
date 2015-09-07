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
package org.nschmidt.ldparteditor.dialogs.keys;

import java.util.HashSet;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.enums.Task;
import org.nschmidt.ldparteditor.enums.TextTask;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.resources.ResourceManager;
import org.nschmidt.ldparteditor.widgets.Tree;
import org.nschmidt.ldparteditor.widgets.TreeColumn;
import org.nschmidt.ldparteditor.widgets.TreeItem;

/**
 * @author nils
 *
 */
class KeyTableDesign extends Dialog {

    protected KeyTableDesign(Shell parentShell) {
        super(parentShell);
    }


    private HashSet<Task>  s1 = new HashSet<Task>();
    private HashSet<TextTask> s2 = new HashSet<TextTask>();

    {
        s1.add(Task.COLOUR_NUMBER0);
        s1.add(Task.COLOUR_NUMBER1);
        s1.add(Task.COLOUR_NUMBER2);
        s1.add(Task.COLOUR_NUMBER3);
        s1.add(Task.COLOUR_NUMBER4);
        s1.add(Task.COLOUR_NUMBER5);
        s1.add(Task.COLOUR_NUMBER6);
        s1.add(Task.COLOUR_NUMBER7);
        s1.add(Task.COLOUR_NUMBER8);
        s1.add(Task.COLOUR_NUMBER9);
    }

    /**
     * Create contents of the dialog.
     *
     * @param parent
     */
    @Override
    protected Control createDialogArea(final Composite parent) {
        this.getShell().setText(I18n.KEYBOARD_CustomiseShortkeys);
        Composite cmp_container = parent;
        cmp_container.setLayout(new GridLayout());

        Tree tree = new Tree(cmp_container, SWT.BORDER | SWT.MULTI, Task.values().length + TextTask.values().length - 8);

        // tree_Problems[0] = tree;
        tree.setLinesVisible(true);
        tree.setHeaderVisible(true);
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        TreeColumn trclmn_Description = new TreeColumn(tree, SWT.NONE);
        trclmn_Description.setWidth(598);
        trclmn_Description.setText(I18n.KEYBOARD_Description);

        TreeColumn trclmn_Location = new TreeColumn(tree, SWT.NONE);
        trclmn_Location.setWidth(100);
        trclmn_Location.setText(I18n.KEYBOARD_Shortkey);

        TreeItem trtm_Editor3D = new TreeItem(tree, SWT.NONE);
        // treeItem_Hints[0] = trtm_Hints;
        trtm_Editor3D.setImage(ResourceManager.getImage("icon16_primitives.png")); //$NON-NLS-1$
        trtm_Editor3D.setText(new String[] { I18n.KEYBOARD_Editor3D, "" }); //$NON-NLS-1$
        trtm_Editor3D.setVisible(true);

        TreeItem trtm_EditorText = new TreeItem(tree, SWT.NONE);
        // treeItem_Warnings[0] = trtm_Warnings;
        trtm_EditorText.setImage(ResourceManager.getImage("icon16_annotate.png")); //$NON-NLS-1$
        trtm_EditorText.setText(new String[] { I18n.KEYBOARD_EditorText, "" }); //$NON-NLS-1$
        trtm_EditorText.setVisible(true);

        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.ADD_COMMENTS); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.ADD_CONDLINE); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.ADD_LINE); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.ADD_QUAD); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.ADD_TRIANGLE); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.ADD_VERTEX); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.COPY); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.CUT); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.DELETE); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.ESC); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.LMB); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.MERGE_TO_AVERAGE); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.MERGE_TO_LAST); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.MMB); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.MODE_COMBINED); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.MODE_MOVE); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.MODE_ROTATE); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.MODE_SCALE); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.MODE_SELECT); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.OBJ_FACE); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.OBJ_LINE); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.OBJ_PRIMITIVE); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.OBJ_VERTEX); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.PASTE); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.REDO); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.RESET_VIEW); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.RMB); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.SAVE); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.SELECT_ALL); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.SELECT_ALL_WITH_SAME_COLOURS); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.SELECT_CONNECTED); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.SELECT_NONE); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.SELECT_OPTION_WITH_SAME_COLOURS); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.SELECT_TOUCHING); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.SHOW_GRID); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.SHOW_RULER); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.SPLIT); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.UNDO); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.ZOOM_IN); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_Editor3D, "Test", Task.ZOOM_OUT); //$NON-NLS-1$

        registerDoubleClickEvent(trtm_EditorText, "Test", TextTask.EDITORTEXT_ESC); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_EditorText, "Test", TextTask.EDITORTEXT_INLINE); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_EditorText, "Test", TextTask.EDITORTEXT_QUICKFIX); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_EditorText, "Test", TextTask.EDITORTEXT_REDO); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_EditorText, "Test", TextTask.EDITORTEXT_REPLACE_VERTEX); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_EditorText, "Test", TextTask.EDITORTEXT_ROUND); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_EditorText, "Test", TextTask.EDITORTEXT_SAVE); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_EditorText, "Test", TextTask.EDITORTEXT_SELECTALL); //$NON-NLS-1$
        registerDoubleClickEvent(trtm_EditorText, "Test2", TextTask.EDITORTEXT_UNDO); //$NON-NLS-1$

        if (s1.size() != Task.values().length || s2.size() != TextTask.values().length) {
            throw new AssertionError("Not all shortkey items are covered by this dialog! Please fix it"); //$NON-NLS-1$
        }

        tree.build();
        return cmp_container;
    }

    private void registerDoubleClickEvent(TreeItem parent, String description, Task t) {
        s1.add(t);
        registerDoubleClickEvent(parent, description, t, null);
    }

    private void registerDoubleClickEvent(TreeItem parent, String description, TextTask t) {
        s2.add(t);
        registerDoubleClickEvent(parent, description, null, t);
    }

    private void registerDoubleClickEvent(TreeItem parent, String description, Task t1, TextTask t2) {
        TreeItem trtm_newKey = new TreeItem(parent, SWT.PUSH);
        trtm_newKey.setText(new String[] { description, "" }); //$NON-NLS-1$
        trtm_newKey.setVisible(true);
    }


    /**
     * Create contents of the button bar.
     *
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, I18n.DIALOG_OK, true);
        createButton(parent, IDialogConstants.CANCEL_ID, I18n.DIALOG_Cancel, false);
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return super.getInitialSize();
    }

}
