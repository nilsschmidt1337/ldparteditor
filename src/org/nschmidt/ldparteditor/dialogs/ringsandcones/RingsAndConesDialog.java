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
package org.nschmidt.ldparteditor.dialogs.ringsandcones;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.helpers.composite3d.RingsAndConesSettings;

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
public class RingsAndConesDialog extends RingsAndConesDesign {

    /**
     * Create the dialog.
     *
     * @param parentShell
     */
    public RingsAndConesDialog(Shell parentShell, RingsAndConesSettings rs) {
        super(parentShell, rs);
    }

    @Override
    public int open() {
        super.create();
        // MARK All final listeners will be configured here..
        spn_height[0].addValueChangeListener(spn -> rs.setHeight(spn_height[0].getValue()));
        spn_radi1[0].addValueChangeListener(spn -> rs.setRadius1(spn_radi1[0].getValue()));
        spn_radi2[0].addValueChangeListener(spn -> rs.setRadius2(spn_radi2[0].getValue()));
        cmb_angle[0].addListener(SWT.Selection, event -> {
            int si = cmb_angle[0].getSelectionIndex();
            rs.setAngle(si);
            if (!rs.isUsingHiRes()) {
                boolean hiRes = true;
                int tc = 0;
                for (int i = 0; i <= si; i++) {
                    if (tc == 2) {
                        hiRes = false;
                        tc = 0;
                        continue;
                    } else {
                        hiRes = true;
                    }
                    tc++;
                }
                if (hiRes) {
                    int index = cmb_shape[0].getSelectionIndex() + 2;
                    cmb_shape[0].select(index);
                    rs.setUsingHiRes(true);
                    rs.setUsingCones(index == 3);
                    rs.setUsingHiRes(index > 1);
                }
            }
        });
        cmb_existingOnly[0].addListener(SWT.Selection, event -> rs.setUsingExistingPrimitives(cmb_existingOnly[0].getSelectionIndex() == 0));
        cmb_createWhat[0].addListener(SWT.Selection, event -> rs.setCreatingNothingOnNoSolution(cmb_createWhat[0].getSelectionIndex() == 0));
        this.cmb_shape[0].addListener(SWT.Selection, event -> {
            rs.setUsingCones(cmb_shape[0].getSelectionIndex() == 1 || cmb_shape[0].getSelectionIndex() == 3);
            rs.setUsingHiRes(cmb_shape[0].getSelectionIndex() > 1);
            int si = cmb_angle[0].getSelectionIndex();
            if (!rs.isUsingHiRes()) {
                boolean hiRes = true;
                int tc = 0;
                for (int i = 0; i <= si; i++) {
                    if (tc == 2) {
                        hiRes = false;
                        tc = 0;
                        continue;
                    } else {
                        hiRes = true;
                    }
                    tc++;
                }
                if (hiRes) {
                    cmb_angle[0].select(47);
                    rs.setAngle(47);
                }
            }
        });
        return super.open();
    }
}
