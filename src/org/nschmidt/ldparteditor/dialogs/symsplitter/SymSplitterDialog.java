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
package org.nschmidt.ldparteditor.dialogs.symsplitter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.helpers.composite3d.SymSplitterSettings;

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
public class SymSplitterDialog extends SymSplitterDesign {

    /**
     * Create the dialog.
     *
     * @param parentShell
     */
    public SymSplitterDialog(Shell parentShell, SymSplitterSettings rs) {
        super(parentShell, rs);
    }

    @Override
    public int open() {
        super.create();
        // MARK All final listeners will be configured here..
        spnOffsetPtr[0].addValueChangeListener(spn -> ss.setOffset(spn.getValue()));
        cmbScopePtr[0].addListener(SWT.Selection, event -> ss.setScope(cmbScopePtr[0].getSelectionIndex()));
        cmbSplitPlanePtr[0].addListener(SWT.Selection, event -> ss.setSplitPlane(cmbSplitPlanePtr[0].getSelectionIndex()));
        cmbHidePtr[0].addListener(SWT.Selection, event -> ss.setHideLevel(cmbHidePtr[0].getSelectionIndex()));
        cmbColourisePtr[0].addListener(SWT.Selection, event -> ss.setColourise(cmbColourisePtr[0].getSelectionIndex() == 1));
        cmbCutAcrossPtr[0].addListener(SWT.Selection, event -> ss.setCutAcross(cmbCutAcrossPtr[0].getSelectionIndex() == 1));
        cmbValidatePtr[0].addListener(SWT.Selection, event -> ss.setValidate(cmbValidatePtr[0].getSelectionIndex() == 1));
        spnPrecisionPtr[0].addValueChangeListener(spn -> ss.setPrecision(spn.getValue()));
        return super.open();
    }
}
