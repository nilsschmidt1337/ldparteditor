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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
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
        spn_offset[0].addValueChangeListener(spn -> ss.setOffset(spn.getValue()));
        cmb_scope[0].addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                ss.setScope(cmb_scope[0].getSelectionIndex());
            }
        });
        cmb_splitPlane[0].addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                ss.setSplitPlane(cmb_splitPlane[0].getSelectionIndex());
            }
        });
        cmb_hide[0].addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                ss.setHideLevel(cmb_hide[0].getSelectionIndex());
            }
        });
        cmb_colourise[0].addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                ss.setColourise(cmb_colourise[0].getSelectionIndex() == 1);
            }
        });
        cmb_cutAcross[0].addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                ss.setCutAcross(cmb_cutAcross[0].getSelectionIndex() == 1);
            }
        });
        cmb_validate[0].addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                ss.setValidate(cmb_validate[0].getSelectionIndex() == 1);
            }
        });
        spn_precision[0].addValueChangeListener(spn -> ss.setPrecision(spn.getValue()));
        return super.open();
    }

}
