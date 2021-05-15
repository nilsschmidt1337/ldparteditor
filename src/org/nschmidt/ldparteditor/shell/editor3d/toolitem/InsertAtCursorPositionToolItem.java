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
package org.nschmidt.ldparteditor.shell.editor3d.toolitem;

import static org.nschmidt.ldparteditor.helper.WidgetUtility.widgetUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.nschmidt.ldparteditor.composite.ToolItem;
import org.nschmidt.ldparteditor.enumtype.Task;
import org.nschmidt.ldparteditor.helper.Cocoa;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.resource.ResourceManager;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.state.KeyStateManager;
import org.nschmidt.ldparteditor.widget.NButton;

public class InsertAtCursorPositionToolItem extends ToolItem {

    private static final NButton[] btnInsertAtCursorPositionPtr = new NButton[1];

    private static boolean insertingAtCursorPosition = false;

    public InsertAtCursorPositionToolItem(Composite parent, int style, boolean isHorizontal) {
        super(parent, style, isHorizontal);
        createWidgets(this);
        addListeners();
    }

    public static boolean isInsertingAtCursorPosition() {
        return insertingAtCursorPosition;
    }

    public static void setInsertingAtCursorPosition(boolean insertAtCursor) {
        insertingAtCursorPosition = insertAtCursor;
    }

    public static void toggleInsertAtCursor() {
        setInsertingAtCursorPosition(!isInsertingAtCursorPosition());
        btnInsertAtCursorPositionPtr[0].setSelection(isInsertingAtCursorPosition());
        clickSingleBtn(btnInsertAtCursorPositionPtr[0]);
    }

    private static void createWidgets(InsertAtCursorPositionToolItem insertAtCursorPositionToolItem) {
        NButton btnInsertAtCursorPosition = new NButton(insertAtCursorPositionToolItem, SWT.TOGGLE | Cocoa.getStyle());
        btnInsertAtCursorPositionPtr[0] = btnInsertAtCursorPosition;
        KeyStateManager.addTooltipText(btnInsertAtCursorPosition, I18n.E3D_INSERT_AT_CURSOR_POSITION, Task.INSERT_AT_CURSOR);
        btnInsertAtCursorPosition.setImage(ResourceManager.getImage("icon16_insertAtCursor.png")); //$NON-NLS-1$
    }

    private static void addListeners() {
        widgetUtil(btnInsertAtCursorPositionPtr[0]).addSelectionListener(e -> {
            setInsertingAtCursorPosition(btnInsertAtCursorPositionPtr[0].getSelection());
            Editor3DWindow.getWindow().regainFocus();
        });
    }
}
