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

import static org.nschmidt.ldparteditor.helpers.WidgetUtility.widgetUtil;

import java.math.BigDecimal;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
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
    final BigDecimalSpinner[] spnMaxPathSegmentLengthPtr = new BigDecimalSpinner[1];

    final BigDecimalSpinner[] spnCenterCurvePtr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnLineThresholdPtr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnRotationAnglePtr = new BigDecimalSpinner[1];
    final IntegerSpinner[] spnTransitionsPtr = new IntegerSpinner[1];
    final BigDecimalSpinner[] spnTransCurvePtr = new BigDecimalSpinner[1];
    final Combo[] cmbShapeCompensationPtr = new Combo[1];
    final Combo[] cmbBfcInvertPtr = new Combo[1];

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
        Composite cmpContainer = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = (GridLayout) cmpContainer.getLayout();
        gridLayout.verticalSpacing = 10;
        gridLayout.horizontalSpacing = 10;

        Label lblSpecify = new Label(cmpContainer, SWT.NONE);
        lblSpecify.setText(I18n.PATHTRUDER_TITLE);

        Label lblSeparator = new Label(cmpContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
        lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label lblColourCodes = new Label(cmpContainer, SWT.NONE);
        lblColourCodes.setText(I18n.PATHTRUDER_COLOUR_CODES);

        Label lblUse180deg = new Label(cmpContainer, SWT.NONE);
        lblUse180deg.setText(I18n.PATHTRUDER_MAX_PATH_LENGTH);

        BigDecimalSpinner spnMaxPathSegmentLength = new BigDecimalSpinner(cmpContainer, SWT.NONE);
        this.spnMaxPathSegmentLengthPtr[0] = spnMaxPathSegmentLength;
        spnMaxPathSegmentLength.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spnMaxPathSegmentLength.setMaximum(new BigDecimal(100000));
        spnMaxPathSegmentLength.setMinimum(new BigDecimal("0.0001")); //$NON-NLS-1$
        spnMaxPathSegmentLength.setValue(ps.getMaxPathSegmentLength());

        Label lblTransitions = new Label(cmpContainer, SWT.NONE);
        lblTransitions.setText(I18n.PATHTRUDER_NUM_TRANSITIONS);

        IntegerSpinner spnTransitions = new IntegerSpinner(cmpContainer, SWT.NONE);
        this.spnTransitionsPtr[0] = spnTransitions;
        spnTransitions.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spnTransitions.setMaximum(1000000);
        spnTransitions.setMinimum(1);
        spnTransitions.setValue(ps.getTransitionCount());

        Label lblTransCurve = new Label(cmpContainer, SWT.NONE);
        lblTransCurve.setText(I18n.PATHTRUDER_CONTROL_CURVE);

        BigDecimalSpinner spnTransCurve = new BigDecimalSpinner(cmpContainer, SWT.NONE);
        this.spnTransCurvePtr[0] = spnTransCurve;
        spnTransCurve.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spnTransCurve.setMaximum(new BigDecimal(100));
        spnTransCurve.setMinimum(BigDecimal.ONE);
        spnTransCurve.setValue(ps.getTransitionCurveControl());

        Label lblCenterCurve = new Label(cmpContainer, SWT.NONE);
        lblCenterCurve.setText(I18n.PATHTRUDER_CONTROL_CURVE_CENTER);

        BigDecimalSpinner spnCenterCurve = new BigDecimalSpinner(cmpContainer, SWT.NONE);
        this.spnCenterCurvePtr[0] = spnCenterCurve;
        spnCenterCurve.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spnCenterCurve.setMaximum(BigDecimal.ONE);
        spnCenterCurve.setMinimum(new BigDecimal(0));
        spnCenterCurve.setValue(ps.getTransitionCurveCenter());

        Label lblLineThreshold = new Label(cmpContainer, SWT.NONE);
        lblLineThreshold.setText(I18n.PATHTRUDER_LINE_THRESH);

        BigDecimalSpinner spnLineThreshold = new BigDecimalSpinner(cmpContainer, SWT.NONE);
        this.spnLineThresholdPtr[0] = spnLineThreshold;
        spnLineThreshold.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spnLineThreshold.setMaximum(new BigDecimal(180));
        spnLineThreshold.setMinimum(new BigDecimal(-1));
        spnLineThreshold.setValue(ps.getPathAngleForLine());

        Label lblRotationAngle = new Label(cmpContainer, SWT.NONE);
        lblRotationAngle.setText(I18n.PATHTRUDER_ROT_ANGLE);

        BigDecimalSpinner spnRotationAngle = new BigDecimalSpinner(cmpContainer, SWT.NONE);
        this.spnRotationAnglePtr[0] = spnRotationAngle;
        spnRotationAngle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spnRotationAngle.setMaximum(new BigDecimal(1000000));
        spnRotationAngle.setMinimum(new BigDecimal(0));
        spnRotationAngle.setValue(ps.getRotation());

        Label lblAf = new Label(cmpContainer, SWT.NONE);
        lblAf.setText(I18n.PATHTRUDER_SHAPE_COMP);

        Combo cmbShapeCompensation = new Combo(cmpContainer, SWT.READ_ONLY);
        this.cmbShapeCompensationPtr[0] = cmbShapeCompensation;
        widgetUtil(cmbShapeCompensation).setItems(I18n.PATHTRUDER_SHAPE_COMP_1, I18n.PATHTRUDER_SHAPE_COMP_2);
        cmbShapeCompensation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        cmbShapeCompensation.setText(ps.isCompensation() ? cmbShapeCompensation.getItem(1) : cmbShapeCompensation.getItem(0));
        cmbShapeCompensation.select(ps.isCompensation() ? 1 : 0);

        Label lblBfcinvert = new Label(cmpContainer, SWT.NONE);
        lblBfcinvert.setText(I18n.PATHTRUDER_INVERT_SHAPE);

        Combo cmbBfcInvert = new Combo(cmpContainer, SWT.READ_ONLY);
        this.cmbBfcInvertPtr[0] = cmbBfcInvert;
        widgetUtil(cmbBfcInvert).setItems(I18n.PATHTRUDER_INVERT_SHAPE_1, I18n.PATHTRUDER_INVERT_SHAPE_2);
        cmbBfcInvert.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        cmbBfcInvert.setText(ps.isInverted() ? cmbBfcInvert.getItem(1) : cmbBfcInvert.getItem(0));
        cmbBfcInvert.select(ps.isInverted() ? 1 : 0);

        cmpContainer.pack();
        return cmpContainer;
    }

    /**
     * Create contents of the button bar.
     *
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, I18n.DIALOG_OK, true);
        createButton(parent, IDialogConstants.CANCEL_ID, I18n.DIALOG_CANCEL, false);
    }
}
