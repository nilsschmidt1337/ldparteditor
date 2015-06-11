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

import java.math.BigDecimal;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.helpers.composite3d.PathTruderSettings;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.widgets.BigDecimalSpinner;
import org.nschmidt.ldparteditor.widgets.IntegerSpinner;

/**
 * The PathTruder dialog
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 *
 * @author nils
 *
 */
class PathTruderDesign extends Dialog {

    final PathTruderSettings ps;

    // Use final only for subclass/listener references!
    final BigDecimalSpinner[] spn_maxPathSegmentLength = new BigDecimalSpinner[1];

    final BigDecimalSpinner[] spn_centerCurve = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_lineThreshold = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_rotationAngle = new BigDecimalSpinner[1];
    final IntegerSpinner[] spn_transitions = new IntegerSpinner[1];
    final BigDecimalSpinner[] spn_transCurve = new BigDecimalSpinner[1];
    final Combo[] cmb_shapeCompensation = new Combo[1];
    final Combo[] cmb_bfcInvert = new Combo[1];
    final Combo[] cmb_scope = new Combo[1];

    PathTruderDesign(Shell parentShell, PathTruderSettings ps) {
        super(parentShell);
        this.ps = ps;
    }

    /**
     * Create contents of the dialog.
     *
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite cmp_container = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = (GridLayout) cmp_container.getLayout();
        gridLayout.verticalSpacing = 10;
        gridLayout.horizontalSpacing = 10;

        Label lbl_specify = new Label(cmp_container, SWT.NONE);
        lbl_specify.setText("PathTruder [Double Precision, IEEE-754]\nResults are rounded to 6 decimal places."); //$NON-NLS-1$ I18N Needs translation!

        Label lbl_separator = new Label(cmp_container, SWT.SEPARATOR | SWT.HORIZONTAL);
        lbl_separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label lbl_colourCodes = new Label(cmp_container, SWT.NONE);
        lbl_colourCodes.setText("Colour codes:\n\n1\t= Path 1\n2\t= Path 2\n4\t= Shape 1 Direction Vector\n5\t= Shape 1\n12\t= Shape 2 Direction Vector [optional]\n13\t= Shape 2 [optional]\n7\t= Line Indicators [optional]\n0\t= Line Ending Normal Indicators [optional]\n"); //$NON-NLS-1$ I18N Needs translation!

        Label lbl_use180deg = new Label(cmp_container, SWT.NONE);
        lbl_use180deg.setText("Define maximum path segment length. Longer segments are split (100000, default):"); //$NON-NLS-1$ I18N Needs translation!

        BigDecimalSpinner spn_maxPathSegmentLength = new BigDecimalSpinner(cmp_container, SWT.NONE);
        this.spn_maxPathSegmentLength[0] = spn_maxPathSegmentLength;
        spn_maxPathSegmentLength.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spn_maxPathSegmentLength.setMaximum(new BigDecimal(100000));
        spn_maxPathSegmentLength.setMinimum(new BigDecimal("0.0001")); //$NON-NLS-1$
        spn_maxPathSegmentLength.setValue(ps.getMaxPathSegmentLength());

        Label lbl_transitions = new Label(cmp_container, SWT.NONE);
        lbl_transitions.setText("Sets the number of transitions from shape1 to shape2 and vice versa (1, default):"); //$NON-NLS-1$ I18N Needs translation!

        IntegerSpinner spn_transitions = new IntegerSpinner(cmp_container, SWT.NONE);
        this.spn_transitions[0] = spn_transitions;
        spn_transitions.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spn_transitions.setMaximum(1000000);
        spn_transitions.setMinimum(1);
        spn_transitions.setValue(ps.getTransitionCount());

        Label lbl_transCurve = new Label(cmp_container, SWT.NONE);
        lbl_transCurve.setText("Control transition curve, from 1 (linear) to 10 (s-shape, default) to 100 (step):"); //$NON-NLS-1$ I18N Needs translation!

        BigDecimalSpinner spn_transCurve = new BigDecimalSpinner(cmp_container, SWT.NONE);
        this.spn_transCurve[0] = spn_transCurve;
        spn_transCurve.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spn_transCurve.setMaximum(new BigDecimal(100));
        spn_transCurve.setMinimum(BigDecimal.ONE);
        spn_transCurve.setValue(ps.getTransitionCurveControl());

        Label lbl_centerCurve = new Label(cmp_container, SWT.NONE);
        lbl_centerCurve.setText("Control transition curve centering. 0 <= position <= 1. Default is 0.5, centered:"); //$NON-NLS-1$ I18N Needs translation!

        BigDecimalSpinner spn_centerCurve = new BigDecimalSpinner(cmp_container, SWT.NONE);
        this.spn_centerCurve[0] = spn_centerCurve;
        spn_centerCurve.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spn_centerCurve.setMaximum(BigDecimal.ONE);
        spn_centerCurve.setMinimum(new BigDecimal(0));
        spn_centerCurve.setValue(ps.getTransitionCurveCenter());

        Label lbl_lineThreshold = new Label(cmp_container, SWT.NONE);
        lbl_lineThreshold.setText("If path has a sharper angle, a line is created at the junction. Default is 180° (no line).\nUse a negative value to force line creation:"); //$NON-NLS-1$ I18N Needs translation!

        BigDecimalSpinner spn_lineThreshold = new BigDecimalSpinner(cmp_container, SWT.NONE);
        this.spn_lineThreshold[0] = spn_lineThreshold;
        spn_lineThreshold.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spn_lineThreshold.setMaximum(new BigDecimal(180));
        spn_lineThreshold.setMinimum(new BigDecimal(-1));
        spn_lineThreshold.setValue(ps.getPathAngleForLine());

        Label lbl_rotationAngle = new Label(cmp_container, SWT.NONE);
        lbl_rotationAngle.setText("Specify rotation of shape direction vector around path1 as it sweeps along the path. Default is 0°:"); //$NON-NLS-1$ I18N Needs translation!

        BigDecimalSpinner spn_rotationAngle = new BigDecimalSpinner(cmp_container, SWT.NONE);
        this.spn_rotationAngle[0] = spn_rotationAngle;
        spn_rotationAngle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spn_rotationAngle.setMaximum(new BigDecimal(1000000));
        spn_rotationAngle.setMinimum(new BigDecimal(0));
        spn_rotationAngle.setValue(ps.getRotation());

        Label lbl_af = new Label(cmp_container, SWT.NONE);
        lbl_af.setText("Compensation at sharp path angles:"); //$NON-NLS-1$ I18N Needs translation!

        Combo cmb_shapeCompensation = new Combo(cmp_container, SWT.READ_ONLY);
        this.cmb_shapeCompensation[0] = cmb_shapeCompensation;
        cmb_shapeCompensation.setItems(new String[] {"No compensation (default).", "The shape is elongated to compensate for flattening at sharp path angles." }); //$NON-NLS-1$ //$NON-NLS-2$
        cmb_shapeCompensation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        cmb_shapeCompensation.setText(ps.isCompensation() ? cmb_shapeCompensation.getItem(1) : cmb_shapeCompensation.getItem(0));
        cmb_shapeCompensation.select(ps.isCompensation() ? 1 : 0);

        Label lbl_bfcinvert = new Label(cmp_container, SWT.NONE);
        lbl_bfcinvert.setText("Invert shape winding:"); //$NON-NLS-1$ I18N Needs translation!

        Combo cmb_bfcInvert = new Combo(cmp_container, SWT.READ_ONLY);
        this.cmb_bfcInvert[0] = cmb_bfcInvert;
        cmb_bfcInvert.setItems(new String[] {"Use original winding (default).", "Invert winding." }); //$NON-NLS-1$ //$NON-NLS-2$
        cmb_bfcInvert.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        cmb_bfcInvert.setText(ps.isInverted() ? cmb_bfcInvert.getItem(1) : cmb_bfcInvert.getItem(0));
        cmb_bfcInvert.select(ps.isInverted() ? 1 : 0);

        cmp_container.pack();
        return cmp_container;
    }

    /**
     * Create contents of the button bar.
     *
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, I18n.DIALOG_OK, true);
        createButton(parent, IDialogConstants.CANCEL_ID, I18n.DIALOG_Cancel, false);
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return super.getInitialSize();
    }

}
