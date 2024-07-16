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
package org.nschmidt.ldparteditor.workbench;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wb.swt.SWTResourceManager;

public enum Theming {
    INSTANCE;

    public static Color getBgColor() {
        // return SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND);
        return SWTResourceManager.getColor(SWT.COLOR_BLACK);
    }

    public static Color getFgColor() {
        // return SWTResourceManager.getColor(SWT.COLOR_WIDGET_FOREGROUND);
        return SWTResourceManager.getColor(SWT.COLOR_WHITE);
    }

    public static Composite composite(Composite parent, int style) {
        final Composite result = new Composite(parent, style);
        setBgColor(result);
        return result;
    }

    public static Label label(Composite parent, int style) {
        final Label result = new Label(parent, style);
        setBgColor(result);
        setFgColor(result);
        return result;
    }

    public static CTabFolder cTabFolder(Composite parent, int style) {
        final CTabFolder result = new CTabFolder(parent, style);
        result.setSelectionForeground(getFgColor());
        setBgColor(result);
        setFgColor(result);
        return result;
    }

    private static void setBgColor(Control ctrl) {
        ctrl.setBackground(getBgColor());
    }

    private static void setFgColor(Control ctrl) {
        ctrl.setForeground(getFgColor());
    }
}
