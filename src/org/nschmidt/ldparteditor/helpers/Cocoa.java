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

import java.text.MessageFormat;

import org.eclipse.swt.SWT;
import org.nschmidt.ldparteditor.enums.MyLanguage;

public enum Cocoa {
    INSTANCE;

    public static final boolean IS_COCOA = "cocoa".equals(SWT.getPlatform()); //$NON-NLS-1$

    public static int getStyle() {
        // TODO Inline after more testing on Mac OS X
        return SWT.NONE;
    }

    public static String replaceCtrlByCmd(String source) {
        Object[] messageArguments = {IS_COCOA ? "\u2318" : "Ctrl"}; //$NON-NLS-1$ //$NON-NLS-2$
        MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
        formatter.setLocale(MyLanguage.getLocale());
        formatter.applyPattern(source);
        return formatter.format(messageArguments);
    }

    public static boolean checkCtrlOrCmdPressed(int stateMask) {
        if (IS_COCOA) {
            return (stateMask & SWT.COMMAND) == SWT.COMMAND;
        } else {
            return (stateMask & SWT.CTRL) == SWT.CTRL;
        }
    }

}
