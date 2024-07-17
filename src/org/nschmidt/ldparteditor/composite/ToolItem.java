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
package org.nschmidt.ldparteditor.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.nschmidt.ldparteditor.helper.WidgetSelectionHelper;
import org.nschmidt.ldparteditor.widget.NButton;
import org.nschmidt.ldparteditor.workbench.Theming;

/**
 * This is a custom toolbar item
 */
public class ToolItem extends Composite {

    public ToolItem(Composite parent, int style, boolean isHorizontal) {
        super(parent, style);
        setBackground(Theming.getBgColor());
        if (isHorizontal) {
            this.setLayout(new RowLayout(SWT.HORIZONTAL));
            new ToolSeparator(this, SWT.NONE, isHorizontal);
        } else {
            this.setLayout(new RowLayout(SWT.VERTICAL));
            new ToolSeparator(this, SWT.NONE, isHorizontal);
        }
    }

    protected static void clickSingleBtn(NButton btn) {
        boolean state = btn.getSelection();
        WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btn.getParent());
        btn.setSelection(state);
    }

    protected static void clickRadioBtn(NButton btn) {
        WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btn.getParent());
        btn.setSelection(true);
    }
}
