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
package org.nschmidt.ldparteditor.dialogs.newfile;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;

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
public class NewFileDialog extends NewFileDesign {

    final boolean[] lock = new boolean[]{false};

    /**
     * Create the dialog.
     *
     * @param parentShell
     */
    public NewFileDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    public int open() {
        super.create();
        // MARK All final listeners will be configured here..
        cb_insertHeader[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (lock[0]) return;
                lock[0] = true;
                cb_openIn3D[0].setSelection(cb_insertHeader[0].getSelection());
                lock[0] = false;
            }
        });
        cb_openIn3D[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (lock[0]) return;
                lock[0] = true;
                cb_openInText[0].setSelection(cb_openIn3D[0].getSelection());
                lock[0] = false;
            }
        });


        rb_shortcut[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (lock[0]) return;
                lock[0] = true;
                txt_chooseName[0].setToolTipText("The shortcut filename starts with the filename taken from the main type 1 line located within the file.\nA valid shortcut filename ends usally with c## \n where ## is a two digit number containing leading zeros (e.g. '01').\n\nPatterned shortcuts, such as 71396p01c01.dat, contain an additional p## identifier.\n\nHowever, the file can be numbered using the official LEGO number, if known.\n\nShortcuts can be of physical colour or an alias to another file.\n"); //$NON-NLS-1$ I18N
                lock[0] = false;
            }
        });

        return super.open();
    }

}
