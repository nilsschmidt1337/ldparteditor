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
package org.nschmidt.ldparteditor.dialogs.colour;

import static org.nschmidt.ldparteditor.helpers.WidgetUtility.WidgetUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.WidgetSelectionListener;
import org.nschmidt.ldparteditor.i18n.I18n;

/**
 *
 * <p>
 * Note: This class should be instantiated, it defines all listeners and part of
 * the business logic. It overrides the {@code open()} method to invoke the
 * listener definitions ;)
 *
 * @author nils
 *
 */
public class ColourDialog extends ColourDesign {


    /**
     * Create the dialog.
     *
     * @param parentShell
     */
    public ColourDialog(Shell parentShell, final GColour[] refCol, final boolean randomColours) {
        super(parentShell, refCol, randomColours);
    }

    public void run() {
        this.setBlockOnOpen(true);
        this.setShellStyle(SWT.APPLICATION_MODAL | SWT.SHELL_TRIM ^ SWT.MIN);
        this.create();
        // MARK All final listeners will be configured here..
        WidgetUtil(btn_colourChoose[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ColorDialog dlg = new ColorDialog(getShell());
                // Change the title bar text
                dlg.setText(I18n.COLOURDIALOG_ChooseDirectColour);
                // Open the dialog and retrieve the selected color
                RGB rgb = dlg.open();
                if (rgb != null) {
                    refCol[0] = new GColour(-1, rgb.red / 255f, rgb.green / 255f, rgb.blue / 255f, 1f);
                    me.close();
                }
            }
        });
        WidgetUtil(btn_colourTable[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                new ColourTableDialog(getShell(), refCol).run();
                me.close();
            }
        });
        if (randomColours) WidgetUtil(btn_randomColours[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                refCol[0] = View.RANDOM_COLOUR;
                me.close();
            }
        });
        this.open();
    }
}
