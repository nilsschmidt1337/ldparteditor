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
package org.nschmidt.ldparteditor.enums;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.SWTResourceManager;
import org.nschmidt.ldparteditor.text.FontMetrics;

/**
 * This class contains constant fonts
 *
 * @author nils
 *
 */
public enum Font {
    INSTANCE;

    /** The standard system fond (copy) */
    public static final org.eclipse.swt.graphics.Font SYSTEM = SWTResourceManager.getFont(Display.getCurrent().getSystemFont().getFontData()[0].getName(), Display.getCurrent().getSystemFont()
            .getFontData()[0].getHeight(), SWT.NORMAL);
    /** The system fond with the height of 8 */
    public static final org.eclipse.swt.graphics.Font SMALL = SWTResourceManager.getFont(Display.getCurrent().getSystemFont().getFontData()[0].getName(), 8, SWT.NORMAL);
    /** The standard monospaced fond for terminals */
    public static final org.eclipse.swt.graphics.Font MONOSPACE = JFaceResources.getFont(JFaceResources.TEXT_FONT);
    /** The height of the standard monospaced font (in pixels) */
    // public static final int MONOSPACE_HEIGHT =
    // FontMetrics.getFontHeight(Font.MONOSPACE);
    /** The width of the standard monospaced font (in pixels) */
    public static final int MONOSPACE_WIDTH = FontMetrics.getStringWidth(" ", Font.MONOSPACE); //$NON-NLS-1$
}
