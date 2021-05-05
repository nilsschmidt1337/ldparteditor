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
package org.nschmidt.ldparteditor.dialogs.direction;

import java.math.BigDecimal;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.composites.ToolItem;
import org.nschmidt.ldparteditor.enums.ManipulatorScope;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.resources.ResourceManager;
import org.nschmidt.ldparteditor.widgets.BigDecimalSpinner;
import org.nschmidt.ldparteditor.widgets.NButton;

/**
 * The coordinates dialog
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 *
 * @author nils
 *
 */
class DirectionDesign extends Dialog {

    static ManipulatorScope transformationMode = ManipulatorScope.GLOBAL;

    final NButton[] btnLocalPtr = new NButton[1];
    final NButton[] btnGlobalPtr = new NButton[1];

    final NButton[] btnMXPtr = new NButton[1];
    final NButton[] btnMYPtr = new NButton[1];
    final NButton[] btnMZPtr = new NButton[1];


    final BigDecimalSpinner[] spnXPtr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnYPtr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnZPtr = new BigDecimalSpinner[1];

    final BigDecimalSpinner[] spnRhoPtr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnThetaPtr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnPhiPtr = new BigDecimalSpinner[1];

    private static final String NUMBER_FORMAT8F = View.NUMBER_FORMAT8F;

    // Use final only for subclass/listener references!

    static double[] cart = new double[]{0.0, 0.0, 0.0};
    static double[] sphe = new double[]{0.0, 0.0, 0.0};


    DirectionDesign(Shell parentShell) {
        super(parentShell);
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
        gridLayout.verticalSpacing = 5;
        gridLayout.horizontalSpacing = 10;

        Label lblSpecify = new Label(cmpContainer, SWT.NONE);
        lblSpecify.setText("Direction vector:"); //$NON-NLS-1$ FIXME !i18n!

        Label lblSeparator = new Label(cmpContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
        lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        ToolItem toolItemTransformationModes = new ToolItem(cmpContainer, SWT.NONE, true);
        {
            NButton btnLocal = new NButton(toolItemTransformationModes, SWT.TOGGLE);
            this.btnLocalPtr[0] = btnLocal;
            btnLocal.setToolTipText(I18n.E3D_LOCAL);
            if (transformationMode == ManipulatorScope.LOCAL) {
                btnLocal.setSelection(true);
            }
            btnLocal.setImage(ResourceManager.getImage("icon16_local.png")); //$NON-NLS-1$
        }
        {
            NButton btnGlobal = new NButton(toolItemTransformationModes, SWT.TOGGLE);
            this.btnGlobalPtr[0] = btnGlobal;
            btnGlobal.setToolTipText(I18n.E3D_GLOBAL);
            if (transformationMode == ManipulatorScope.GLOBAL) {
                btnGlobal.setSelection(true);
            }
            btnGlobal.setImage(ResourceManager.getImage("icon16_global.png")); //$NON-NLS-1$
        }

        Label lblCartesianCoords = new Label(cmpContainer, I18n.rightToLeftStyle());
        lblCartesianCoords.setText("Cartesian coordinates:"); //$NON-NLS-1$ FIXME !i18n!

        {
            Composite cmpTxt = new Composite(cmpContainer, SWT.NONE);
            cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpTxt.setLayout(new GridLayout(6, true));

            Label lblXaxis = new Label(cmpTxt, I18n.rightToLeftStyle());
            lblXaxis.setText("X:"); //$NON-NLS-1$ FIXME !i18n!

            BigDecimalSpinner spnX = new BigDecimalSpinner(cmpTxt, SWT.NONE, NUMBER_FORMAT8F);
            this.spnXPtr[0] = spnX;
            spnX.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
            spnX.setMaximum(new BigDecimal(1000000));
            spnX.setMinimum(new BigDecimal(-1000000));
            spnX.setValue(new BigDecimal(cart[0]));
        }

        {
            Composite cmpTxt = new Composite(cmpContainer, SWT.NONE);
            cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpTxt.setLayout(new GridLayout(6, true));

            Label lblYaxis = new Label(cmpTxt, I18n.rightToLeftStyle());
            lblYaxis.setText("Y:"); //$NON-NLS-1$ FIXME !i18n!

            BigDecimalSpinner spnY = new BigDecimalSpinner(cmpTxt, SWT.NONE, NUMBER_FORMAT8F);
            this.spnYPtr[0] = spnY;
            spnY.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
            spnY.setMaximum(new BigDecimal(1000000));
            spnY.setMinimum(new BigDecimal(-1000000));
            spnY.setValue(new BigDecimal(cart[1]));
        }

        {
            Composite cmpTxt = new Composite(cmpContainer, SWT.NONE);
            cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpTxt.setLayout(new GridLayout(6, true));

            Label lblZaxis = new Label(cmpTxt, I18n.rightToLeftStyle());
            lblZaxis.setText("Z:"); //$NON-NLS-1$ FIXME !i18n!

            BigDecimalSpinner spnZ = new BigDecimalSpinner(cmpTxt, SWT.NONE, NUMBER_FORMAT8F);
            this.spnZPtr[0] = spnZ;
            spnZ.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
            spnZ.setMaximum(new BigDecimal(1000000));
            spnZ.setMinimum(new BigDecimal(-1000000));
            spnZ.setValue(new BigDecimal(cart[2]));
        }

        Label lblSphericalCoords = new Label(cmpContainer, I18n.rightToLeftStyle());
        lblSphericalCoords.setText("Spherical coordinates (as defined by ISO 80000-2:2009):"); //$NON-NLS-1$ FIXME !i18n!

        {
            Composite cmpTxt = new Composite(cmpContainer, SWT.NONE);
            cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpTxt.setLayout(new GridLayout(6, true));

            Label lblXaxis = new Label(cmpTxt, I18n.rightToLeftStyle());
            lblXaxis.setText("r:"); //$NON-NLS-1$ FIXME !i18n!

            BigDecimalSpinner spnRho = new BigDecimalSpinner(cmpTxt, SWT.NONE, NUMBER_FORMAT8F);
            this.spnRhoPtr[0] = spnRho;
            spnRho.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
            spnRho.setMaximum(new BigDecimal(1000000));
            spnRho.setMinimum(new BigDecimal(-1000000));
            spnRho.setValue(new BigDecimal(sphe[0]));
        }

        {
            Composite cmpTxt = new Composite(cmpContainer, SWT.NONE);
            cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpTxt.setLayout(new GridLayout(6, true));

            Label lblYaxis = new Label(cmpTxt, I18n.rightToLeftStyle());
            lblYaxis.setText("θ:"); //$NON-NLS-1$ FIXME !i18n!

            BigDecimalSpinner spnY = new BigDecimalSpinner(cmpTxt, SWT.NONE, NUMBER_FORMAT8F);
            this.spnThetaPtr[0] = spnY;
            spnY.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
            spnY.setMaximum(new BigDecimal(1000000));
            spnY.setMinimum(new BigDecimal(-1000000));
            spnY.setValue(new BigDecimal(sphe[1]));
        }

        {
            Composite cmpTxt = new Composite(cmpContainer, SWT.NONE);
            cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpTxt.setLayout(new GridLayout(6, true));

            Label lblZaxis = new Label(cmpTxt, I18n.rightToLeftStyle());
            lblZaxis.setText("φ:"); //$NON-NLS-1$ FIXME !i18n!

            BigDecimalSpinner spnZ = new BigDecimalSpinner(cmpTxt, SWT.NONE, NUMBER_FORMAT8F);
            this.spnPhiPtr[0] = spnZ;
            spnZ.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
            spnZ.setMaximum(new BigDecimal(1000000));
            spnZ.setMinimum(new BigDecimal(-1000000));
            spnZ.setValue(new BigDecimal(sphe[2]));
        }

        {
            Composite cmpTxt = new Composite(cmpContainer, SWT.NONE);
            cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpTxt.setLayout(new GridLayout(1, true));
            NButton btnClipboard = new NButton(cmpTxt, SWT.NONE);
            this.btnMXPtr[0] = btnClipboard;
            btnClipboard.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            btnClipboard.setImage(ResourceManager.getImage("icon8_local.png")); //$NON-NLS-1$
            btnClipboard.setText("Use X axis from manipulator"); //$NON-NLS-1$ FIXME !i18n!
        }

        {
            Composite cmpTxt = new Composite(cmpContainer, SWT.NONE);
            cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpTxt.setLayout(new GridLayout(1, true));
            NButton btnClipboard = new NButton(cmpTxt, SWT.NONE);
            this.btnMYPtr[0] = btnClipboard;
            btnClipboard.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            btnClipboard.setImage(ResourceManager.getImage("icon8_local.png")); //$NON-NLS-1$
            btnClipboard.setText("Use Y axis from manipulator"); //$NON-NLS-1$ FIXME !i18n!
        }

        {
            Composite cmpTxt = new Composite(cmpContainer, SWT.NONE);
            cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpTxt.setLayout(new GridLayout(1, true));
            NButton btnClipboard = new NButton(cmpTxt, SWT.NONE);
            this.btnMZPtr[0] = btnClipboard;
            btnClipboard.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            btnClipboard.setImage(ResourceManager.getImage("icon8_local.png")); //$NON-NLS-1$
            btnClipboard.setText("Use Z axis from manipulator"); //$NON-NLS-1$ FIXME !i18n!
        }

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
        createButton(parent, IDialogConstants.OK_ID, I18n.DIALOG_OK, false);
        createButton(parent, IDialogConstants.CANCEL_ID, I18n.DIALOG_CANCEL, false);
    }
}
