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

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.helpers.WidgetSelectionListener;
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
        spn_ac[0].addValueChangeListener(spn -> es.setAc(spn.getValue()));
        spn_af[0].addValueChangeListener(spn -> es.setAf(spn.getValue()));
        spn_ae[0].addValueChangeListener(spn -> es.setAe(spn.getValue()));
        spn_vequ[0].addValueChangeListener(spn -> es.setEqualDistance(spn.getValue()));
        WidgetUtil(cmb_b[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                es.setExtendedRange(cmb_b[0].getSelectionIndex() == 1);
            }
        });
        WidgetUtil(cmb_c[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                es.setCondlineOnQuads(cmb_c[0].getSelectionIndex() == 1);
            }
        });
        WidgetUtil(cmb_u[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                es.setUnmatchedMode(cmb_u[0].getSelectionIndex());
            }
        });
        WidgetUtil(cmb_scope[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                es.setScope(cmb_scope[0].getSelectionIndex());
            }
        });
        WidgetUtil(btn_verbose[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                es.setVerbose(btn_verbose[0].getSelection());
            }
        });
        return super.open();
    }
}
