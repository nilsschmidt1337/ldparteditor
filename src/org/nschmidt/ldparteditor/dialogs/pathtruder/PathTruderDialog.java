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
package org.nschmidt.ldparteditor.dialogs.pathtruder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.helpers.composite3d.PathTruderSettings;

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
public class PathTruderDialog extends PathTruderDesign {

    /**
     * Create the dialog.
     *
     * @param parentShell
     * @param es
     */
    public PathTruderDialog(Shell parentShell, PathTruderSettings ps) {
        super(parentShell, ps);
    }

    @Override
    public int open() {
        super.create();

        // MARK All final listeners will be configured here..

        spn_centerCurve[0].addValueChangeListener(spn -> ps.setTransitionCurveCenter(spn.getValue()));
        spn_lineThreshold[0].addValueChangeListener(spn -> ps.setPathAngleForLine(spn.getValue()));
        spn_rotationAngle[0].addValueChangeListener(spn -> ps.setRotation(spn.getValue()));
        spn_transCurve[0].addValueChangeListener(spn -> ps.setTransitionCurveControl(spn.getValue()));
        spn_transitions[0].addValueChangeListener(spn -> ps.setTransitionCount(spn.getValue()));
        spn_maxPathSegmentLength[0].addValueChangeListener(spn -> ps.setMaxPathSegmentLength(spn.getValue()));
        cmb_bfcInvert[0].addListener(SWT.Selection, event -> ps.setInverted(cmb_bfcInvert[0].getSelectionIndex() == 1));
        cmb_shapeCompensation[0].addListener(SWT.Selection, event -> ps.setCompensation(cmb_shapeCompensation[0].getSelectionIndex() == 1));
        return super.open();
    }

}
