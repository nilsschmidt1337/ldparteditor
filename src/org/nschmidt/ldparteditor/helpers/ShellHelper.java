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

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

/**
 * A helper class for various shell actions (center on screen, ...)
 *
 * @author nils
 *
 */
public enum ShellHelper {
    INSTANCE;

    /**
     * Centers a shell on the primary screen
     *
     * @param sh
     *            The shell to center
     */
    public static final void centerShellOnPrimaryScreen(final Shell sh) {

        Monitor primary = Display.getCurrent().getPrimaryMonitor();
        Rectangle bounds = primary.getBounds();
        Rectangle rect = sh.getBounds();

        int x = bounds.x + (bounds.width - rect.width) / 2;
        int y = bounds.y + (bounds.height - rect.height) / 2;

        sh.setLocation(x, y);
    }


    /**
     * Calculates the absolute position from a Control on the Shell
     * @param cmp the Control
     * @return the absolute position of this Control on the Shell
     */
    public static final Point absolutePositionOnShell(Control cmp) {
        int absX = 0;
        int absY = 0;
        while (!(cmp instanceof Shell)) {
            if (cmp instanceof Control) {
                Control con = cmp;
                Point ownPos = con.getLocation();
                cmp = con.getParent();
                absX += ownPos.x;
                absY += ownPos.y;
            } else {
                break;
            }
        }
        if (cmp instanceof Shell) {
            Shell sh = (Shell) cmp;
            absX += sh.getLocation().x;
            absY += sh.getLocation().y;
        }
        return new Point(absX, absY);
    }
}
