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
package org.nschmidt.ldparteditor.dialogs.ytruder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.helpers.composite3d.YTruderSettings;

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
public class YTruderDialog extends YTruderDesign {

    /**
     * Create the dialog.
     *
     * @param parentShell
     * @param ys
     */
    public YTruderDialog(Shell parentShell, YTruderSettings ys) {
        super(parentShell, ys);
    }

    @Override
    public int open() {
        super.create();

        // MARK All final listeners will be configured here..

        spn_value[0].addValueChangeListener(spn -> ys.setDistance(spn.getValue().doubleValue()));
        spn_condlineAngleThreshold[0].addValueChangeListener(spn -> ys.setCondlineAngleThreshold(spn.getValue().doubleValue()));
        cmb_axis[0].addListener(SWT.Selection, event -> ys.setAxis(cmb_axis[0].getSelectionIndex()));
        btn_TranslateByDistance[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ys.setMode(1);
            }
        });
        btn_SymmetryAcrossPlane[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ys.setMode(2);
            }
        });
        btn_ProjectionOnPlane[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ys.setMode(3);
            }
        });
        btn_ExtrudeRadially[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ys.setMode(4);
            }
        });
        return super.open();
    }

}
