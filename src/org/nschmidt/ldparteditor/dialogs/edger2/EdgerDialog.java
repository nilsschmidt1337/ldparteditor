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
package org.nschmidt.ldparteditor.dialogs.edger2;

import static org.nschmidt.ldparteditor.helpers.WidgetUtility.WidgetUtil;

import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.helpers.composite3d.Edger2Settings;

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
public class EdgerDialog extends EdgerDesign {

    /**
     * Create the dialog.
     *
     * @param parentShell
     * @param es
     */
    public EdgerDialog(Shell parentShell, Edger2Settings es) {
        super(parentShell, es);
    }

    @Override
    public int open() {
        super.create();
        // MARK All final listeners will be configured here..
        spnAcPtr[0].addValueChangeListener(spn -> es.setAc(spn.getValue()));
        spnAfPtr[0].addValueChangeListener(spn -> es.setAf(spn.getValue()));
        spnAePtr[0].addValueChangeListener(spn -> es.setAe(spn.getValue()));
        spnVequPtr[0].addValueChangeListener(spn -> es.setEqualDistance(spn.getValue()));
        WidgetUtil(cmbBPtr[0]).addSelectionListener(e -> es.setExtendedRange(cmbBPtr[0].getSelectionIndex() == 1));
        WidgetUtil(cmbCPtr[0]).addSelectionListener(e -> es.setCondlineOnQuads(cmbCPtr[0].getSelectionIndex() == 1));
        WidgetUtil(cmbUPtr[0]).addSelectionListener(e -> es.setUnmatchedMode(cmbUPtr[0].getSelectionIndex()));
        WidgetUtil(cmbScopePtr[0]).addSelectionListener(e -> es.setScope(cmbScopePtr[0].getSelectionIndex()));
        WidgetUtil(btnVerbosePtr[0]).addSelectionListener(e -> es.setVerbose(btnVerbosePtr[0].getSelection()));
        return super.open();
    }
}
