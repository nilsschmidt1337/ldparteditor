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
package org.nschmidt.ldparteditor.dialog.calibrate;

import static org.nschmidt.ldparteditor.helper.WidgetUtility.widgetUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.data.VertexInfo;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.enumtype.Threshold;
import org.nschmidt.ldparteditor.i18n.I18n;

/**
 *
 * <p>
 * Note: This class should be instantiated, it defines all listeners and part of
 * the business logic. It overrides the {@code open()} method to invoke the
 * listener definitions ;)
 */
public class CalibrateDialog extends CalibrateDesign {

    private boolean ldu = true;
    private boolean mm = false;
    private boolean inch = false;
    private boolean stud = false;

    private boolean update = false;
    private final VertexManager vm;

    /**
     * Create the dialog.
     */
    public CalibrateDialog(Shell parentShell, VertexManager vm, Set<VertexInfo> vis) {
        super(parentShell, vis);
        this.vm = vm;
        ldu = false;
        mm = false;
        inch = false;
    }

    @Override
    public int open() {
        super.create();
        getShell().setText(I18n.CALIBRATE_BG_IMAGE);
        // MARK All final listeners will be configured here..
        widgetUtil(btnLDUPtr[0]).addSelectionListener(e -> {
            if (update) return;
            uncheckAllUnits();
            ldu = true;
            btnLDUPtr[0].setSelection(true);
        });
        widgetUtil(btnMMPtr[0]).addSelectionListener(e -> {
            if (update) return;
            uncheckAllUnits();
            mm = true;
            btnMMPtr[0].setSelection(true);
        });
        widgetUtil(btnInchPtr[0]).addSelectionListener(e -> {
            if (update) return;
            uncheckAllUnits();
            inch = true;
            btnInchPtr[0].setSelection(true);
        });
        widgetUtil(btnStudPtr[0]).addSelectionListener(e -> {
            if (update) return;
            uncheckAllUnits();
            stud = true;
            btnStudPtr[0].setSelection(true);
        });
        spnLDUPtr[0].addValueChangeListener(spn -> {
            if (update) return;
            uncheckAllUnits();
            update = true;
            spnInchPtr[0].setValue(spn.getValue().multiply(new BigDecimal(I18n.UNITS_FACTOR_LDU_TO_INCH), Threshold.MC));
            spnMMPtr[0].setValue(spn.getValue().multiply(new BigDecimal(I18n.UNITS_FACTOR_LDU_TO_MM), Threshold.MC));
            spnStudPtr[0].setValue(spnLDUPtr[0].getValue().multiply(new BigDecimal(I18n.UNITS_FACTOR_LDU_TO_STUD), Threshold.MC).setScale(1, RoundingMode.HALF_UP));
            update = false;
            btnLDUPtr[0].setSelection(true);
            ldu = true;
        });
        spnMMPtr[0].addValueChangeListener(spn -> {
            if (update) return;
            uncheckAllUnits();
            update = true;
            spnLDUPtr[0].setValue(spn.getValue().multiply(new BigDecimal(I18n.UNITS_FACTOR_MM_TO_LDU), Threshold.MC));
            spnInchPtr[0].setValue(spn.getValue().multiply(new BigDecimal(I18n.UNITS_FACTOR_MM_TO_INCH), Threshold.MC));
            spnStudPtr[0].setValue(spnLDUPtr[0].getValue().multiply(new BigDecimal(I18n.UNITS_FACTOR_LDU_TO_STUD), Threshold.MC).setScale(1, RoundingMode.HALF_UP));
            update = false;
            btnMMPtr[0].setSelection(true);
            mm = true;
        });
        spnInchPtr[0].addValueChangeListener(spn -> {
            if (update) return;
            uncheckAllUnits();
            update = true;
            spnLDUPtr[0].setValue(spn.getValue().multiply(new BigDecimal(I18n.UNITS_FACTOR_INCH_TO_LDU), Threshold.MC));
            spnMMPtr[0].setValue(spn.getValue().multiply(new BigDecimal(I18n.UNITS_FACTOR_INCH_TO_MM), Threshold.MC));
            spnStudPtr[0].setValue(spnLDUPtr[0].getValue().multiply(new BigDecimal(I18n.UNITS_FACTOR_LDU_TO_STUD), Threshold.MC).setScale(1, RoundingMode.HALF_UP));
            update = false;
            btnInchPtr[0].setSelection(true);
            inch = true;
        });
        spnStudPtr[0].addValueChangeListener(spn -> {
            if (update) return;
            uncheckAllUnits();
            update = true;
            spnLDUPtr[0].setValue(spn.getValue().multiply(new BigDecimal(I18n.UNITS_FACTOR_STUD_TO_LDU), Threshold.MC));
            spnInchPtr[0].setValue(spnLDUPtr[0].getValue().multiply(new BigDecimal(I18n.UNITS_FACTOR_LDU_TO_INCH), Threshold.MC));
            spnMMPtr[0].setValue(spnLDUPtr[0].getValue().multiply(new BigDecimal(I18n.UNITS_FACTOR_LDU_TO_MM), Threshold.MC));
            update = false;
            btnStudPtr[0].setSelection(true);
            stud = true;
        });

        final BigDecimal initialValue = spnLDUPtr[0].getValue();
        spnLDUPtr[0].setValue(BigDecimal.ZERO);
        spnLDUPtr[0].setValue(initialValue);
        return super.open();
    }

    private void uncheckAllUnits() {
        update = true;
        btnLDUPtr[0].setSelection(false);
        btnMMPtr[0].setSelection(false);
        btnInchPtr[0].setSelection(false);
        btnStudPtr[0].setSelection(false);
        ldu = false;
        mm = false;
        inch = false;
        stud = false;
        update = false;
    }

    public void performCalibration() {
        // TODO Auto-generated method stub

    }
}
