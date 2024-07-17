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
package org.nschmidt.ldparteditor.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.workbench.Theming;

public abstract class ThemedDialog extends Dialog {

    protected ThemedDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected final Control createButtonBar(Composite parent) {
        final Control btnBar = super.createButtonBar(parent);
        parent.setBackground(Theming.getBgColor());
        btnBar.setBackground(Theming.getBgColor());
        return btnBar;
    }

    @Override
    protected final Button createButton(Composite parent, int id, String label, boolean defaultButton) {
        final Button btn = super.createButton(parent, id, label, defaultButton);
        btn.setBackground(Theming.getBgColor());
        btn.setForeground(Theming.getFgColor());
        return btn;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite cmpContainer = (Composite) super.createDialogArea(parent);
        cmpContainer.setBackground(Theming.getBgColor());
        return cmpContainer;
    }
}
