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
package org.nschmidt.ldparteditor.dialog.infographic;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.resource.ResourceManager;

public class InfographicDialog extends InfographicDesign {

    public static final String CHEAT_SHEET_PDF = "ldparteditor_cheat_sheet.pdf"; //$NON-NLS-1$

    /**
     * Create the dialog.
     *
     * @param parentShell
     */
    public InfographicDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    public int open() {
        super.create();
        this.getButton(IDialogConstants.OK_ID).setText(I18n.INFOGRAPHIC_HELP_SAVE_AS_PDF);
        getShell().setImage(ResourceManager.getImage("imgDuke2.png")); //$NON-NLS-1$
        return super.open();
    }
}
