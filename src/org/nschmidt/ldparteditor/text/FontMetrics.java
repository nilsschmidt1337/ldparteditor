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
package org.nschmidt.ldparteditor.text;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * This class provides useful functions for calculating font sizes
 *
 * @author nils
 *
 */
public enum FontMetrics {
    INSTANCE;

    /**
     * Calculates the average width of a string depending its font and length
     *
     * @param string
     *            the string to evaluate
     * @param font
     *            the font object reference
     * @return the average(!) width in pixels
     */
    public static int getStringWidth(String string, Font font) {
        Shell shell = new Shell(Display.getCurrent());
        Text text = new Text(shell, SWT.NONE);
        text.setFont(font);
        GC gc = new GC(text);
        org.eclipse.swt.graphics.FontMetrics fm = gc.getFontMetrics();
        int charWidth = (int) fm.getAverageCharacterWidth();
        gc.dispose();
        shell.dispose();
        return string.length() * charWidth;
    }

    /**
     * Calculates the height of a font
     *
     * @param font
     *            the font object reference
     * @return the font height
     */
    public static int getFontHeight(Font font) {
        Shell shell = new Shell(Display.getCurrent());
        Text text = new Text(shell, SWT.NONE);
        text.setFont(font);
        GC gc = new GC(text);
        org.eclipse.swt.graphics.FontMetrics fm = gc.getFontMetrics();
        int charHeight = fm.getHeight();
        gc.dispose();
        shell.dispose();
        return charHeight;
    }

}
