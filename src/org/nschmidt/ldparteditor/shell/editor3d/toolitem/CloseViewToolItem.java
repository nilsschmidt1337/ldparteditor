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
package org.nschmidt.ldparteditor.shell.editor3d.toolitem;

import static org.nschmidt.ldparteditor.helper.WidgetUtility.widgetUtil;

import org.eclipse.swt.widgets.Composite;
import org.nschmidt.ldparteditor.composite.Composite3D;
import org.nschmidt.ldparteditor.composite.ToolItem;
import org.nschmidt.ldparteditor.enumtype.Task;
import org.nschmidt.ldparteditor.helper.Cocoa;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.resource.ResourceManager;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.state.KeyStateManager;
import org.nschmidt.ldparteditor.widget.NButton;

public class CloseViewToolItem extends ToolItem {

    public CloseViewToolItem(Composite parent, int style, boolean isHorizontal) {
        super(parent, style, isHorizontal);

        NButton btnCloseView = new NButton(this, Cocoa.getStyle());
        KeyStateManager.addTooltipText(btnCloseView, I18n.E3D_CLOSE_VIEW, Task.CLOSE_VIEW);
        btnCloseView.setImage(ResourceManager.getImage("icon16_closeview.png")); //$NON-NLS-1$

        widgetUtil(btnCloseView).addSelectionListener(e -> {
            Composite3D c3d = Editor3DWindow.getWindow().getCurrentCoposite3d();
            if (c3d != null) {
                c3d.getModifier().closeView();
            }

            Editor3DWindow.getWindow().regainFocus();
        });
    }
}
