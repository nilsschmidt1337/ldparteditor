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

import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.enums.Task;
import org.nschmidt.ldparteditor.enums.TextTask;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.resources.ResourceManager;
import org.nschmidt.ldparteditor.state.KeyStateManager;
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
        cmp_container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        Label lbl_DoubleClick = new Label(cmp_container, I18n.I18N_RTL());
        lbl_DoubleClick.setText(I18n.KEYBOARD_DoubleClick);

        final Tree tree = new Tree(cmp_container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL, Task.values().length + TextTask.values().length - 7);

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

        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_AddComment, Task.ADD_COMMENTS);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_AddVertex, Task.ADD_VERTEX);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_AddLine, Task.ADD_LINE);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_AddTriangle, Task.ADD_TRIANGLE);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_AddQuad, Task.ADD_QUAD);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_AddCondline, Task.ADD_CONDLINE);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_Cut, Task.CUT);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_Copy, Task.COPY);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_Paste, Task.PASTE);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_Delete, Task.DELETE);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_Esc1, Task.ESC);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_LMB, Task.LMB);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_MergeToAvg, Task.MERGE_TO_AVERAGE);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_MergeToLast, Task.MERGE_TO_LAST);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_MMB, Task.MMB);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_ModeCombined, Task.MODE_COMBINED);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_ModeMove, Task.MODE_MOVE);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_ModeRotate, Task.MODE_ROTATE);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_ModeScale, Task.MODE_SCALE);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_ModeSelect, Task.MODE_SELECT);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_ObjFace, Task.OBJ_FACE);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_ObjLine, Task.OBJ_LINE);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_ObjPrimitive, Task.OBJ_PRIMITIVE);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_ObjVertex, Task.OBJ_VERTEX);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_ResetView, Task.RESET_VIEW);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_RMB, Task.RMB);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_Save, Task.SAVE);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_SelectAll, Task.SELECT_ALL);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_SelectAllWithSameColours, Task.SELECT_ALL_WITH_SAME_COLOURS);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_SelectConnected, Task.SELECT_CONNECTED);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_SelectNone, Task.SELECT_NONE);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_SelectOptionWithSameColours, Task.SELECT_OPTION_WITH_SAME_COLOURS);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_SelectTouching, Task.SELECT_TOUCHING);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_ShowGrid, Task.SHOW_GRID);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_ShowRuler, Task.SHOW_RULER);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_Split, Task.SPLIT);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_Undo, Task.UNDO);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_Redo, Task.REDO);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_ZoomIn, Task.ZOOM_IN);
        registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_ZoomOut, Task.ZOOM_OUT);

        registerDoubleClickEvent(trtm_EditorText, I18n.KEYBOARD_Esc2, TextTask.EDITORTEXT_ESC);
        registerDoubleClickEvent(trtm_EditorText, I18n.KEYBOARD_Inline, TextTask.EDITORTEXT_INLINE);
        registerDoubleClickEvent(trtm_EditorText, I18n.KEYBOARD_QuickFix, TextTask.EDITORTEXT_QUICKFIX);
        registerDoubleClickEvent(trtm_EditorText, I18n.KEYBOARD_Redo, TextTask.EDITORTEXT_REDO);
        registerDoubleClickEvent(trtm_EditorText, I18n.KEYBOARD_ReplaceVertex, TextTask.EDITORTEXT_REPLACE_VERTEX);
        registerDoubleClickEvent(trtm_EditorText, I18n.KEYBOARD_Round, TextTask.EDITORTEXT_ROUND);
        registerDoubleClickEvent(trtm_EditorText, I18n.KEYBOARD_Save, TextTask.EDITORTEXT_SAVE);
        registerDoubleClickEvent(trtm_EditorText, I18n.KEYBOARD_SelectAll, TextTask.EDITORTEXT_SELECTALL);
        registerDoubleClickEvent(trtm_EditorText, I18n.KEYBOARD_Undo, TextTask.EDITORTEXT_UNDO);
        registerDoubleClickEvent(trtm_EditorText, I18n.KEYBOARD_FindReplace, TextTask.EDITORTEXT_FIND);

        TreeItem trtm_Temp = new TreeItem(tree, SWT.NONE);
        trtm_Temp.setText(new String[] { "", "" }); //$NON-NLS-1$ //$NON-NLS-2$
        trtm_Temp.setVisible(true);

        if (s1.size() != Task.values().length || s2.size() != TextTask.values().length) {
            throw new AssertionError("Not all shortkey items are covered by this dialog! Please fix it"); //$NON-NLS-1$
        }

        tree.build();

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                final TreeItem selection;
                if (tree.getSelectionCount() == 1 && (selection = tree.getSelection()[0]).getData() != null) {
                    KeyStateManager.tmp_keyString = null;
                    if (new KeyDialog(getShell()).open() == IDialogConstants.OK_ID && KeyStateManager.tmp_keyString != null) {
                        Object[] data = (Object[]) selection.getData();
                        if (data[0] == null && !KeyStateManager.hasTextTaskKey(KeyStateManager.tmp_mapKey)) {
                            KeyStateManager.changeKey((String) data[2], KeyStateManager.tmp_mapKey, KeyStateManager.tmp_keyString, (TextTask) data[1]);
                            selection.setText(new String[]{selection.getText(0), KeyStateManager.tmp_keyString});
                        }
                        if (data[1] == null && !KeyStateManager.hasTaskKey(KeyStateManager.tmp_mapKey)) {
                            KeyStateManager.changeKey((String) data[2], KeyStateManager.tmp_mapKey, KeyStateManager.tmp_keyString, (Task) data[0]);
                            selection.setText(new String[]{selection.getText(0), KeyStateManager.tmp_keyString});
                        }
                        tree.build();
                        tree.update();
                    }
                }
            }
        });
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

        String keyCombination = ""; //$NON-NLS-1$

        String key = null;

        if (t1 != null) {
            HashMap<Task, String> m = KeyStateManager.getTaskKeymap();
            keyCombination = m.get(t1);
            key = KeyStateManager.getMapKey(t1);
        } else if (t2 != null) {
            HashMap<TextTask, String> m = KeyStateManager.getTextTaskKeymap();
            keyCombination = m.get(t2);
            key = KeyStateManager.getMapKey(t2);
        }

        TreeItem trtm_newKey = new TreeItem(parent, SWT.PUSH);
        trtm_newKey.setText(new String[] { description, keyCombination });
        trtm_newKey.setVisible(true);
        trtm_newKey.setData(new Object[]{t1, t2, key});

    }


    /**
     * Create contents of the button bar.
     *
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, I18n.DIALOG_OK, true);
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return super.getInitialSize();
    }

}
