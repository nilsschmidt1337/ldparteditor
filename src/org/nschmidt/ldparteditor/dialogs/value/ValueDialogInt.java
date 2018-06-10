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
package org.nschmidt.ldparteditor.dialogs.value;

import static org.nschmidt.ldparteditor.helpers.WidgetUtility.WidgetUtil;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.helpers.WidgetSelectionListener;

/**
 * @author nils
 *
 */
public abstract class ValueDialogInt extends ValueDesignInt {

    /**
     * Create the dialog.
     *
     */
    public ValueDialogInt(Shell shell, String shellText, String unitText) {
        super(shell, shellText, unitText);
    }

    @Override
    public int open() {
        super.create();
        getShell().setText(shellText);
        initializeSpinner();
        // MARK All final listeners will be configured here..
        WidgetUtil(btn_ok[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                applyValue();
            }
        });
        return super.open();
    }

    public abstract void initializeSpinner();
    public abstract void applyValue();

}
