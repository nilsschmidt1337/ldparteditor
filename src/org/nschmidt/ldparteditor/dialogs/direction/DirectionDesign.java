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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.nschmidt.ldparteditor.widgets.NButton;
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

    final NButton[] btn_Local = new NButton[1];
    final NButton[] btn_Global = new NButton[1];

    final NButton[] btn_mX = new NButton[1];
    final NButton[] btn_mY = new NButton[1];
    final NButton[] btn_mZ = new NButton[1];


    final BigDecimalSpinner[] spn_X = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_Y = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_Z = new BigDecimalSpinner[1];

    final BigDecimalSpinner[] spn_Rho = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_Theta = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_Phi = new BigDecimalSpinner[1];

    private final String NUMBER_FORMAT = View.NUMBER_FORMAT8F;

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
        Composite cmp_container = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = (GridLayout) cmp_container.getLayout();
        gridLayout.verticalSpacing = 5;
        gridLayout.horizontalSpacing = 10;

        Label lbl_specify = new Label(cmp_container, SWT.NONE);
        lbl_specify.setText("Direction vector:"); //$NON-NLS-1$ FIXME !i18n!

        Label lbl_separator = new Label(cmp_container, SWT.SEPARATOR | SWT.HORIZONTAL);
        lbl_separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        ToolItem toolItem_TransformationModes = new ToolItem(cmp_container, SWT.NONE, true);
        {
            NButton btn_Local = new NButton(toolItem_TransformationModes, SWT.TOGGLE);
            this.btn_Local[0] = btn_Local;
            btn_Local.setToolTipText(I18n.E3D_LOCAL);
            if (transformationMode == ManipulatorScope.LOCAL) {
                btn_Local.setSelection(true);
            }
            btn_Local.setImage(ResourceManager.getImage("icon16_local.png")); //$NON-NLS-1$
        }
        {
            NButton btn_Global = new NButton(toolItem_TransformationModes, SWT.TOGGLE);
            this.btn_Global[0] = btn_Global;
            btn_Global.setToolTipText(I18n.E3D_GLOBAL);
            if (transformationMode == ManipulatorScope.GLOBAL) {
                btn_Global.setSelection(true);
            }
            btn_Global.setImage(ResourceManager.getImage("icon16_global.png")); //$NON-NLS-1$
        }

        Label lbl_CartesianCoords = new Label(cmp_container, I18n.rightToLeftStyle());
        lbl_CartesianCoords.setText("Cartesian coordinates:"); //$NON-NLS-1$ FIXME !i18n!

        {
            Composite cmp_txt = new Composite(cmp_container, SWT.NONE);
            cmp_txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmp_txt.setLayout(new GridLayout(6, true));

            Label lbl_Xaxis = new Label(cmp_txt, I18n.rightToLeftStyle());
            lbl_Xaxis.setText("X:"); //$NON-NLS-1$ FIXME !i18n!

            BigDecimalSpinner spn_X = new BigDecimalSpinner(cmp_txt, SWT.NONE, NUMBER_FORMAT);
            this.spn_X[0] = spn_X;
            spn_X.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
            spn_X.setMaximum(new BigDecimal(1000000));
            spn_X.setMinimum(new BigDecimal(-1000000));
            spn_X.setValue(new BigDecimal(cart[0]));
        }

        {
            Composite cmp_txt = new Composite(cmp_container, SWT.NONE);
            cmp_txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmp_txt.setLayout(new GridLayout(6, true));

            Label lbl_Yaxis = new Label(cmp_txt, I18n.rightToLeftStyle());
            lbl_Yaxis.setText("Y:"); //$NON-NLS-1$ FIXME !i18n!

            BigDecimalSpinner spn_Y = new BigDecimalSpinner(cmp_txt, SWT.NONE, NUMBER_FORMAT);
            this.spn_Y[0] = spn_Y;
            spn_Y.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
            spn_Y.setMaximum(new BigDecimal(1000000));
            spn_Y.setMinimum(new BigDecimal(-1000000));
            spn_Y.setValue(new BigDecimal(cart[1]));
        }

        {
            Composite cmp_txt = new Composite(cmp_container, SWT.NONE);
            cmp_txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmp_txt.setLayout(new GridLayout(6, true));

            Label lbl_Zaxis = new Label(cmp_txt, I18n.rightToLeftStyle());
            lbl_Zaxis.setText("Z:"); //$NON-NLS-1$ FIXME !i18n!

            BigDecimalSpinner spn_Z = new BigDecimalSpinner(cmp_txt, SWT.NONE, NUMBER_FORMAT);
            this.spn_Z[0] = spn_Z;
            spn_Z.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
            spn_Z.setMaximum(new BigDecimal(1000000));
            spn_Z.setMinimum(new BigDecimal(-1000000));
            spn_Z.setValue(new BigDecimal(cart[2]));
        }

        Label lbl_SphericalCoords = new Label(cmp_container, I18n.rightToLeftStyle());
        lbl_SphericalCoords.setText("Spherical coordinates (as defined by ISO 80000-2:2009):"); //$NON-NLS-1$ FIXME !i18n!

        {
            Composite cmp_txt = new Composite(cmp_container, SWT.NONE);
            cmp_txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmp_txt.setLayout(new GridLayout(6, true));

            Label lbl_Xaxis = new Label(cmp_txt, I18n.rightToLeftStyle());
            lbl_Xaxis.setText("r:"); //$NON-NLS-1$ FIXME !i18n!

            BigDecimalSpinner spn_Rho = new BigDecimalSpinner(cmp_txt, SWT.NONE, NUMBER_FORMAT);
            this.spn_Rho[0] = spn_Rho;
            spn_Rho.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
            spn_Rho.setMaximum(new BigDecimal(1000000));
            spn_Rho.setMinimum(new BigDecimal(-1000000));
            spn_Rho.setValue(new BigDecimal(sphe[0]));
        }

        {
            Composite cmp_txt = new Composite(cmp_container, SWT.NONE);
            cmp_txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmp_txt.setLayout(new GridLayout(6, true));

            Label lbl_Yaxis = new Label(cmp_txt, I18n.rightToLeftStyle());
            lbl_Yaxis.setText("θ:"); //$NON-NLS-1$ FIXME !i18n!

            BigDecimalSpinner spn_Y = new BigDecimalSpinner(cmp_txt, SWT.NONE, NUMBER_FORMAT);
            this.spn_Theta[0] = spn_Y;
            spn_Y.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
            spn_Y.setMaximum(new BigDecimal(1000000));
            spn_Y.setMinimum(new BigDecimal(-1000000));
            spn_Y.setValue(new BigDecimal(sphe[1]));
        }

        {
            Composite cmp_txt = new Composite(cmp_container, SWT.NONE);
            cmp_txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmp_txt.setLayout(new GridLayout(6, true));

            Label lbl_Zaxis = new Label(cmp_txt, I18n.rightToLeftStyle());
            lbl_Zaxis.setText("φ:"); //$NON-NLS-1$ FIXME !i18n!

            BigDecimalSpinner spn_Z = new BigDecimalSpinner(cmp_txt, SWT.NONE, NUMBER_FORMAT);
            this.spn_Phi[0] = spn_Z;
            spn_Z.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
            spn_Z.setMaximum(new BigDecimal(1000000));
            spn_Z.setMinimum(new BigDecimal(-1000000));
            spn_Z.setValue(new BigDecimal(sphe[2]));
        }

        {
            Composite cmp_txt = new Composite(cmp_container, SWT.NONE);
            cmp_txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmp_txt.setLayout(new GridLayout(1, true));
            NButton btn_Clipboard = new NButton(cmp_txt, SWT.NONE);
            this.btn_mX[0] = btn_Clipboard;
            btn_Clipboard.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            btn_Clipboard.setImage(ResourceManager.getImage("icon8_local.png")); //$NON-NLS-1$
            btn_Clipboard.setText("Use X axis from manipulator"); //$NON-NLS-1$ FIXME !i18n!
        }

        {
            Composite cmp_txt = new Composite(cmp_container, SWT.NONE);
            cmp_txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmp_txt.setLayout(new GridLayout(1, true));
            NButton btn_Clipboard = new NButton(cmp_txt, SWT.NONE);
            this.btn_mY[0] = btn_Clipboard;
            btn_Clipboard.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            btn_Clipboard.setImage(ResourceManager.getImage("icon8_local.png")); //$NON-NLS-1$
            btn_Clipboard.setText("Use Y axis from manipulator"); //$NON-NLS-1$ FIXME !i18n!
        }

        {
            Composite cmp_txt = new Composite(cmp_container, SWT.NONE);
            cmp_txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmp_txt.setLayout(new GridLayout(1, true));
            NButton btn_Clipboard = new NButton(cmp_txt, SWT.NONE);
            this.btn_mZ[0] = btn_Clipboard;
            btn_Clipboard.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            btn_Clipboard.setImage(ResourceManager.getImage("icon8_local.png")); //$NON-NLS-1$
            btn_Clipboard.setText("Use Z axis from manipulator"); //$NON-NLS-1$ FIXME !i18n!
        }

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
        createButton(parent, IDialogConstants.OK_ID, I18n.DIALOG_OK, false);
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
