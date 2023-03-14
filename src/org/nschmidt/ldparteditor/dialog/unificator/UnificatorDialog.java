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
package org.nschmidt.ldparteditor.dialog.unificator;

import static org.nschmidt.ldparteditor.helper.WidgetUtility.widgetUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.helper.composite3d.UnificatorSettings;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 *
 * <p>
 * Note: This class should be instantiated, it defines all listeners and part of
 * the business logic. It overrides the {@code open()} method to invoke the
 * listener definitions ;)
 */
public class UnificatorDialog extends UnificatorDesign {

    /**
     * Create the dialog.
     *
     * @param parentShell
     */
    public UnificatorDialog(Shell parentShell, UnificatorSettings us) {
        super(parentShell, us);
    }

    @Override
    public int open() {
        super.create();
        // MARK All final listeners will be configured here..
        spnVertexThresholdPtr[0].addValueChangeListener(spn -> us.setVertexThreshold(spn.getValue()));
        cmbScopePtr[0].addListener(SWT.Selection, event -> us.setScope(cmbScopePtr[0].getSelectionIndex()));
        cmbWhatToUnifyPtr[0].addListener(SWT.Selection, event -> us.setSnapOn(cmbWhatToUnifyPtr[0].getSelectionIndex()));
        spnSubfileThresholdPtr[0].addValueChangeListener(spn -> us.setSubvertexThreshold(spn.getValue()));
        widgetUtil(btnVerbosePtr[0]).addSelectionListener(e -> WorkbenchManager.getUserSettingState().setVerboseUnificator(btnVerbosePtr[0].getSelection()));
        return super.open();
    }

}
