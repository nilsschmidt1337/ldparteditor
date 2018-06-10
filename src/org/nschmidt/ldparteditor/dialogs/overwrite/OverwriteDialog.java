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
package org.nschmidt.ldparteditor.dialogs.overwrite;

import static org.nschmidt.ldparteditor.helpers.WidgetUtility.WidgetUtil;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;

/**
 * @author nils
 *
 */
public class OverwriteDialog extends OverwriteDesign {

    /**
     * Create the dialog.
     *
     * @param saveAs
     *            {@code true} if the dialog should be displayed as
     *            "Save As..."-dialog.
     */
    public OverwriteDialog(String fileName) {
        super(Editor3DWindow.getWindow().getShell(), fileName);
    }

    @Override
    public int open() {
        super.create();
        getShell().setText(I18n.DIALOG_ReplaceTitle);
        // MARK All final listeners will be configured here..
        WidgetUtil(getButton(IDialogConstants.YES_ID)).addSelectionListener(e -> {
            setReturnCode(IDialogConstants.YES_ID);
            close();
        });
        WidgetUtil(getButton(IDialogConstants.NO_ID)).addSelectionListener(e -> {
            setReturnCode(IDialogConstants.NO_ID);
            close();
        });
        WidgetUtil(getButton(IDialogConstants.SKIP_ID)).addSelectionListener(e -> {
            setReturnCode(IDialogConstants.SKIP_ID);
            close();
        });
        return super.open();
    }
}
