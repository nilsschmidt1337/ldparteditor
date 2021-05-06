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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.helpers.KeyBoardHelper;
import org.nschmidt.ldparteditor.state.KeyStateManager;

public class KeyDialog extends KeyDesign {

    public KeyDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    public int open() {
        super.create();
        this.dialogArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                onKeyPressed(e);
            }
        });

        return super.open();
    }

    private void onKeyPressed(KeyEvent e) {
        final int stateMask = e.stateMask;
        final int keyCode = e.keyCode;
        final boolean ctrlPressed = (stateMask & SWT.CTRL) != 0;
        final boolean altPressed = (stateMask & SWT.ALT) != 0;
        final boolean shiftPressed = (stateMask & SWT.SHIFT) != 0;
        final boolean cmdPressed = (stateMask & SWT.COMMAND) != 0;

        final StringBuilder sb = new StringBuilder();
        sb.append(keyCode);
        sb.append(ctrlPressed ? "+Ctrl" : ""); //$NON-NLS-1$//$NON-NLS-2$
        sb.append(altPressed ? "+Alt" : ""); //$NON-NLS-1$//$NON-NLS-2$
        sb.append(shiftPressed ? "+Shift" : ""); //$NON-NLS-1$//$NON-NLS-2$
        sb.append(cmdPressed ? "+Cmd" : ""); //$NON-NLS-1$//$NON-NLS-2$
        KeyStateManager.tmpMapKey = sb.toString();

        final Event event = new Event();
        event.keyCode = keyCode;
        if (ctrlPressed) event.stateMask = event.stateMask | SWT.CTRL;
        if (altPressed) event.stateMask = event.stateMask | SWT.ALT;
        if (shiftPressed) event.stateMask = event.stateMask | SWT.SHIFT;
        if (cmdPressed) event.stateMask = event.stateMask | SWT.COMMAND;
        lblPressKeyPtr[0].setText(KeyBoardHelper.getKeyString(event));
        lblPressKeyPtr[0].update();
        KeyStateManager.tmpKeyCode = keyCode;
        KeyStateManager.tmpStateMask = stateMask;
        KeyStateManager.tmpKeyString = lblPressKeyPtr[0].getText();
    }
}
