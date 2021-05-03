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

import static org.nschmidt.ldparteditor.helpers.WidgetUtility.widgetUtil;

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
import org.nschmidt.ldparteditor.helpers.composite3d.YTruderSettings;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.widgets.BigDecimalSpinner;
import org.nschmidt.ldparteditor.widgets.NButton;

/**
 * The YTruder dialog
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 *
 * @author nils
 *
 */
class YTruderDesign extends Dialog {


    final YTruderSettings ys;

    // Use final only for subclass/listener references!
    final BigDecimalSpinner[] spnValuePtr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnCondlineAngleThresholdPtr = new BigDecimalSpinner[1];
    final Combo[] cmbAxisPtr = new Combo[1];
    final NButton[] btnTranslateByDistancePtr = new NButton[1];
    final NButton[] btnSymmetryAcrossPlanePtr = new NButton[1];
    final NButton[] btnProjectionOnPlanePtr = new NButton[1];
    final NButton[] btnExtrudeRadiallyPtr = new NButton[1];

    YTruderDesign(Shell parentShell, YTruderSettings ys) {
        super(parentShell);
        this.ys = ys;
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

        Label lblTitle = new Label(cmpContainer, SWT.NONE);
        lblTitle.setText(I18n.YTRUDER_TITLE);

        Label lblSeparator = new Label(cmpContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
        lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label lblDescription = new Label(cmpContainer, SWT.NONE);
        lblDescription.setText(I18n.YTRUDER_DESCRIPTION);

        {
            NButton btnTranslateByDistance = new NButton(cmpContainer, SWT.RADIO);
            this.btnTranslateByDistancePtr[0] = btnTranslateByDistance;
            btnTranslateByDistance.setText(I18n.YTRUDER_TRANSLATION_BY_DISTANCE);
            btnTranslateByDistance.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            btnTranslateByDistance.setSelection(ys.getMode() == 1);
        }
        {
            NButton btnSymmetryAcrossPlane = new NButton(cmpContainer, SWT.RADIO);
            this.btnSymmetryAcrossPlanePtr[0] = btnSymmetryAcrossPlane;
            btnSymmetryAcrossPlane.setText(I18n.YTRUDER_SYMMETRY_ACROSS_PLANE);
            btnSymmetryAcrossPlane.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            btnSymmetryAcrossPlane.setSelection(ys.getMode() == 2);
        }
        {
            NButton btnProjectionOnPlane = new NButton(cmpContainer, SWT.RADIO);
            this.btnProjectionOnPlanePtr[0] = btnProjectionOnPlane;
            btnProjectionOnPlane.setText(I18n.YTRUDER_PROJECTION_ON_PLANE);
            btnProjectionOnPlane.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            btnProjectionOnPlane.setSelection(ys.getMode() == 3);
        }
        {
            NButton btnExtrudeRadially = new NButton(cmpContainer, SWT.RADIO);
            this.btnExtrudeRadiallyPtr[0] = btnExtrudeRadially;
            btnExtrudeRadially.setText(I18n.YTRUDER_EXTRUDE_RADIALLY);
            btnExtrudeRadially.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            btnExtrudeRadially.setSelection(ys.getMode() == 4);
        }

        Label lblLineThreshold = new Label(cmpContainer, SWT.NONE);
        lblLineThreshold.setText(I18n.YTRUDER_VALUE);

        BigDecimalSpinner spnValue = new BigDecimalSpinner(cmpContainer, SWT.NONE);
        this.spnValuePtr[0] = spnValue;
        spnValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spnValue.setMaximum(new BigDecimal(999999));
        spnValue.setMinimum(new BigDecimal(-999999));
        spnValue.setValue(new BigDecimal(ys.getDistance()));

        Label lblRotationAngle = new Label(cmpContainer, SWT.NONE);
        lblRotationAngle.setText(I18n.YTRUDER_CONDLINE_ANGLE);

        BigDecimalSpinner spnRotationAngle = new BigDecimalSpinner(cmpContainer, SWT.NONE);
        this.spnCondlineAngleThresholdPtr[0] = spnRotationAngle;
        spnRotationAngle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spnRotationAngle.setMaximum(new BigDecimal(180));
        spnRotationAngle.setMinimum(new BigDecimal(0));
        spnRotationAngle.setValue(new BigDecimal(ys.getCondlineAngleThreshold()));

        Label lblAf = new Label(cmpContainer, SWT.NONE);
        lblAf.setText(I18n.YTRUDER_AXIS);

        Combo cmbAxis = new Combo(cmpContainer, SWT.READ_ONLY);
        this.cmbAxisPtr[0] = cmbAxis;
        widgetUtil(cmbAxis).setItems(I18n.YTRUDER_X, I18n.YTRUDER_Y, I18n.YTRUDER_Z);
        cmbAxis.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        cmbAxis.select(ys.getAxis());

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

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return super.getInitialSize();
    }

}
