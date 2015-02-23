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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.helpers.composite3d.PathTruderSettings;
import org.nschmidt.ldparteditor.widgets.BigDecimalSpinner;
import org.nschmidt.ldparteditor.widgets.IntegerSpinner;
import org.nschmidt.ldparteditor.widgets.ValueChangeAdapter;

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

        spn_centerCurve[0].addValueChangeListener(new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
                ps.setTransitionCurveCenter(spn.getValue());
            }
        });
        spn_lineThreshold[0].addValueChangeListener(new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
                ps.setPathAngleForLine(spn.getValue());
            }
        });
        spn_rotationAngle[0].addValueChangeListener(new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
                ps.setRotation(spn.getValue());
            }
        });
        spn_transCurve[0].addValueChangeListener(new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
                ps.setTransitionCurveControl(spn.getValue());
            }
        });
        spn_transitions[0].addValueChangeListener(new ValueChangeAdapter() {
            @Override
            public void valueChanged(IntegerSpinner spn) {
                ps.setTransitionCount(spn.getValue());
            }
        });
        spn_maxPathSegmentLength[0].addValueChangeListener(new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
                ps.setMaxPathSegmentLength(spn.getValue());
            }
        });
        cmb_bfcInvert[0].addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                ps.setInverted(cmb_bfcInvert[0].getSelectionIndex() == 1);
            }
        });
        cmb_shapeCompensation[0].addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                ps.setCompensation(cmb_shapeCompensation[0].getSelectionIndex() == 1);
            }
        });

        //        spn_ac[0].addValueChangeListener(new ValueChangeAdapter() {
        //            @Override
        //            public void valueChanged(BigDecimalSpinner spn) {
        //                ps.setAc(spn.getValue());
        //            }
        //        });
        //        spn_af[0].addValueChangeListener(new ValueChangeAdapter() {
        //            @Override
        //            public void valueChanged(BigDecimalSpinner spn) {
        //                ps.setAf(spn.getValue());
        //            }
        //        });
        //        spn_ae[0].addValueChangeListener(new ValueChangeAdapter() {
        //            @Override
        //            public void valueChanged(BigDecimalSpinner spn) {
        //                ps.setAe(spn.getValue());
        //            }
        //        });
        //        spn_vequ[0].addValueChangeListener(new ValueChangeAdapter() {
        //            @Override
        //            public void valueChanged(BigDecimalSpinner spn) {
        //                ps.setEqualDistance(spn.getValue());
        //            }
        //        });
        //        cmb_b[0].addListener(SWT.Selection, new Listener() {
        //            @Override
        //            public void handleEvent(Event event) {
        //                ps.setExtendedRange(cmb_b[0].getSelectionIndex() == 1);
        //            }
        //        });
        //        cmb_u[0].addListener(SWT.Selection, new Listener() {
        //            @Override
        //            public void handleEvent(Event event) {
        //                ps.setUnmatchedMode(cmb_u[0].getSelectionIndex());
        //            }
        //        });
        //        cmb_scope[0].addListener(SWT.Selection, new Listener() {
        //            @Override
        //            public void handleEvent(Event event) {
        //                ps.setScope(cmb_scope[0].getSelectionIndex());
        //            }
        //        });
        return super.open();
    }

}
