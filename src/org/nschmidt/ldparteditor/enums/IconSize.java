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

import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

public enum IconSize {
    INSTANCE;

    private static final int iconSize = WorkbenchManager.getUserSettingState().getIconSize();

    public static int getIconsize() {
        return iconSize;
    }

    public static int getImageSizeFromIconSize() {
        final int imgSize;
        switch (IconSize.getIconsize()) {
        case -1:
            imgSize = 12;
            break;
        case 0:
            imgSize = 16;
            break;
        case 1:
            imgSize = 20;
            break;
        case 2:
            imgSize = 24;
            break;
        case 3:
            imgSize = 28;
            break;
        case 4:
            imgSize = 32;
            break;
        case 5:
            imgSize = 32;
            break;
        default:
            imgSize = 16;
            break;
        }
        return imgSize;
    }
}
