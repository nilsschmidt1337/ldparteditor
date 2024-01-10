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
package org.nschmidt.ldparteditor.enumtype;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.SWTResourceManager;
import org.nschmidt.ldparteditor.text.FontMetrics;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * This class contains constant fonts and font information
 */
public enum Font {
    INSTANCE;

    /** The standard system font (copy) */
    public static final org.eclipse.swt.graphics.Font SYSTEM = SWTResourceManager.getFont(Display.getDefault().getSystemFont().getFontData()[0].getName(), Display.getDefault().getSystemFont()
            .getFontData()[0].getHeight(), SWT.NORMAL);
    /** The system font with the height of 8 */
    public static final org.eclipse.swt.graphics.Font SMALL = SWTResourceManager.getFont(Display.getDefault().getSystemFont().getFontData()[0].getName(), 8, SWT.NORMAL);
    /** The standard monospaced font for terminals */
    public static final org.eclipse.swt.graphics.Font MONOSPACE_1 = JFaceResources.getFont(JFaceResources.TEXT_FONT);
    /** The width of the standard monospaced font (in pixels) */
    private static final int MONOSPACE_1_WIDTH = FontMetrics.getStringWidth(" ", Font.MONOSPACE_1); //$NON-NLS-1$
    /** The standard monospaced font for terminals */
    public static final org.eclipse.swt.graphics.Font MONOSPACE_2 = SWTResourceManager.getFont(MONOSPACE_1.getFontData()[0].getName(), (int) (MONOSPACE_1.getFontData()[0].getHeight() * 1.5), SWT.NORMAL);
    /** The width of the standard monospaced font (in pixels) */
    private static final int MONOSPACE_2_WIDTH = FontMetrics.getStringWidth(" ", Font.MONOSPACE_2); //$NON-NLS-1$
    /** The standard monospaced font for terminals */
    public static final org.eclipse.swt.graphics.Font MONOSPACE_3 = SWTResourceManager.getFont(MONOSPACE_1.getFontData()[0].getName(), MONOSPACE_1.getFontData()[0].getHeight() * 2, SWT.NORMAL);
    /** The width of the standard monospaced font (in pixels) */
    private static final int MONOSPACE_3_WIDTH = FontMetrics.getStringWidth(" ", Font.MONOSPACE_3); //$NON-NLS-1$
    /** The em size of the standard font (in pixels) */
    public static final int STANDARD_EM_SIZE = FontMetrics.getStringWidth("M", Font.SYSTEM); //$NON-NLS-1$
    /** The width of the current monospaced font (in pixels) */
    public static int width() {
        final int textSize = WorkbenchManager.getUserSettingState().getTextSize();
        switch (textSize) {
        case 0:
            return Font.MONOSPACE_1_WIDTH;
        case 1:
            return Font.MONOSPACE_2_WIDTH;
        case 2:
            return Font.MONOSPACE_3_WIDTH;
        default:
            return Font.MONOSPACE_1_WIDTH;
        }
    }
    /** The monospaced font for terminals */
    public static org.eclipse.swt.graphics.Font monospaced() {
        final int textSize = WorkbenchManager.getUserSettingState().getTextSize();
        switch (textSize) {
        case 0:
            return Font.MONOSPACE_1;
        case 1:
            return Font.MONOSPACE_2;
        case 2:
            return Font.MONOSPACE_3;
        default:
            return Font.MONOSPACE_1;
        }
    }
}
