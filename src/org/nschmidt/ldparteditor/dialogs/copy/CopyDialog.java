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
package org.nschmidt.ldparteditor.dialogs.copy;

import static org.nschmidt.ldparteditor.helpers.WidgetUtility.WidgetUtil;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.helpers.WidgetSelectionListener;

/**
 *
 * <p>
 * Note: This class should be instantiated, it defines all listeners and part of
 * the business logic. It overrides the {@code open()} method to invoke the
 * listener definitions ;)
 *
 * @author nils
 *
 */
public class CopyDialog extends CopyDesign {

    /**
     * Create the dialog.
     *
     * @param parentShell
     * @param es
     */
    public CopyDialog(Shell parentShell, String fileName) {
        super(parentShell, fileName);
    }

    @Override
    public int open() {
        super.create();
        // MARK All final listeners will be configured here..
        WidgetUtil(btn_1[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setReturnCode(IDialogConstants.OK_ID);
                close();
            }
        });
        WidgetUtil(btn_2[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setReturnCode(IDialogConstants.YES_ID);
                close();
            }
        });
        WidgetUtil(btn_3[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setReturnCode(IDialogConstants.NO_ID);
                close();
            }
        });
        return super.open();
    }
}
