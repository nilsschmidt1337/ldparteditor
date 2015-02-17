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
package org.nschmidt.ldparteditor.helpers;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.nschmidt.ldparteditor.composites.ToolItem;

/**
 * Provides useful methods for the selection of widgets
 *
 * @author nils
 *
 */
public enum WidgetSelectionHelper {
    INSTANCE;

    /**
     * Sets the selection attribute of all children buttons to false.
     *
     * @param toolItem
     *            the {@linkplain ToolItem} which contains the buttons.
     */
    public static void unselectAllChildButtons(ToolItem toolItem) {
        Control[] childs = toolItem.getChildren();
        int number_Of_Childs = childs.length;
        for (int i = 1; i < number_Of_Childs; i++) {
            Button b = (Button) childs[i];
            b.setSelection(false);
        }
    }

    /**
     * Sets the selection attribute of all children buttons to false.
     *
     * @param menuItem
     *            the {@linkplain Menu} which contains the items.
     */
    public static void unselectAllChildButtons(Menu menu) {
        MenuItem[] childs = menu.getItems();
        int number_Of_Childs = childs.length;
        for (int i = 0; i < number_Of_Childs; i++) {
            MenuItem it = childs[i];
            it.setSelection(false);
        }
    }

}
