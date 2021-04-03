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

import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.nschmidt.ldparteditor.i18n.I18n;

/**
 * Provides useful keyboard functions
 *
 * @author nils
 *
 */
public enum KeyBoardHelper {
    INSTANCE;

    /**
     * Returns the string representation of the triggered key from the
     * {@link SWT.KeyUp} or {@link SWT.KeyDown} event.
     *
     * @param event
     *            The event to evaluate
     * @return The string representation of the triggered key
     */
    // TODO Needs improvements!
    public static String getKeyString(Event event) {
        int accelerator = SWTKeySupport.convertEventToUnmodifiedAccelerator(event);
        KeyStroke keyStroke = SWTKeySupport.convertAcceleratorToKeyStroke(accelerator);
        KeySequence sequence = KeySequence.getInstance(keyStroke);
        if (Cocoa.isCocoa) {
            return sequence.toString()
                    .replace("CTRL+", " " + I18n.KEYBOARD_CTRL.toLowerCase() + " ") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    .replace("SHIFT+", "\u21E7") //$NON-NLS-1$ //$NON-NLS-2$
                    .replace("ALT+", "\u2325") //$NON-NLS-1$ //$NON-NLS-2$
                    .replace("COMMAND+", I18n.KEYBOARD_COMMAND) //$NON-NLS-1$
                    .replace("BS", "\u232B"); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            return sequence.toString()
                    .replace("CTRL", I18n.KEYBOARD_CTRL) //$NON-NLS-1$
                    .replace("SHIFT", I18n.KEYBOARD_SHIFT) //$NON-NLS-1$
                    .replace("ALT", I18n.KEYBOARD_ALT) //$NON-NLS-1$
                    .replace("DEL", I18n.KEYBOARD_DEL) //$NON-NLS-1$
                    .replace("BS", I18n.KEYBOARD_BACKSPACE); //$NON-NLS-1$
        }
    }

}
