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
package org.nschmidt.ldparteditor.dialogs.scale;

import java.math.BigDecimal;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.nschmidt.ldparteditor.composites.ToolItem;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.enums.ManipulatorScope;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.enums.WorkingMode;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.resources.ResourceManager;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.widgets.BigDecimalSpinner;
import org.nschmidt.ldparteditor.widgets.NButton;

/**
 * The scale dialog
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 *
 * @author nils
 *
 */
class ScaleDesign extends Dialog {

    static ManipulatorScope transformationMode = ManipulatorScope.LOCAL;

    final NButton[] btnLocalPtr = new NButton[1];
    final NButton[] btnGlobalPtr = new NButton[1];

    final Button[] btnCopyPtr = new Button[1];

    final NButton[] cbXaxisPtr = new NButton[1];
    final NButton[] cbYaxisPtr = new NButton[1];
    final NButton[] cbZaxisPtr = new NButton[1];
    final BigDecimalSpinner[] spnXPtr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnYPtr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnZPtr = new BigDecimalSpinner[1];
    final NButton[] btnPivotClipboardPtr = new NButton[1];
    final NButton[] btnPivotManipulatorPtr = new NButton[1];
    final BigDecimalSpinner[] spnPXPtr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnPYPtr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnPZPtr = new BigDecimalSpinner[1];

    private static final String NUMBER_FORMAT = View.NUMBER_FORMAT8F;

    // Use final only for subclass/listener references!

    private Vertex v = new Vertex(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE);
    Vertex p = new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    Vertex m = new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    Vertex c = new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

    ScaleDesign(Shell parentShell, Vertex v, Set<Vertex> clipboardVertices, Vertex manipulatorPosition, ManipulatorScope scope) {
        super(parentShell);
        transformationMode = scope;
        if (manipulatorPosition != null) {
            m = manipulatorPosition;
        }
        if (v == null) {
            this.v = new Vertex(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE);
        } else {
            this.v = v;
        }
        if (clipboardVertices.size() == 1) {
            p = clipboardVertices.iterator().next();
            c = new Vertex(p.xp, p.yp, p.zp);
        } else if (transformationMode == ManipulatorScope.LOCAL && manipulatorPosition != null) {
            p = new Vertex(m.xp, m.yp, m.zp);
        }
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
        lblSpecify.setText(I18n.SCALE_TITLE);

        Label lblSeparator = new Label(cmpContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
        lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        if (Editor3DWindow.getWindow().isMovingAdjacentData()) {
            Label lblAdjacencyWarning = new Label(cmpContainer, SWT.NONE);
            lblAdjacencyWarning.setText(I18n.E3D_ADJACENT_WARNING_STATUS);
            lblAdjacencyWarning.setToolTipText(I18n.E3D_ADJACENT_WARNING_DIALOG);
            lblAdjacencyWarning.setForeground(SWTResourceManager.getColor(SWT.COLOR_INFO_FOREGROUND));
            lblAdjacencyWarning.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
        }

        ToolItem toolItemTransformationModes = new ToolItem(cmpContainer, SWT.NONE, true);
        {
            NButton btnLocal = new NButton(toolItemTransformationModes, SWT.TOGGLE);
            this.btnLocalPtr[0] = btnLocal;
            btnLocal.setToolTipText(I18n.E3D_LOCAL);
            btnLocal.setImage(ResourceManager.getImage("icon16_local.png")); //$NON-NLS-1$
            if (transformationMode == ManipulatorScope.LOCAL) {
                btnLocal.setSelection(true);
                Editor3DWindow.getWindow().setWorkingAction(WorkingMode.MOVE);
            }
        }
        {
            NButton btnGlobal = new NButton(toolItemTransformationModes, SWT.TOGGLE);
            this.btnGlobalPtr[0] = btnGlobal;
            btnGlobal.setToolTipText(I18n.E3D_GLOBAL);
            btnGlobal.setImage(ResourceManager.getImage("icon16_global.png")); //$NON-NLS-1$
            if (transformationMode == ManipulatorScope.GLOBAL) {
                btnGlobal.setSelection(true);
                Editor3DWindow.getWindow().setWorkingAction(WorkingMode.MOVE_GLOBAL);
            }
        }

        {
            Composite cmpTxt = new Composite(cmpContainer, SWT.NONE);
            cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpTxt.setLayout(new GridLayout(6, true));

            NButton cbXaxis = new NButton(cmpTxt, SWT.CHECK);
            this.cbXaxisPtr[0] = cbXaxis;
            cbXaxis.setText(I18n.SCALE_X);
            cbXaxis.setSelection(true);

            BigDecimalSpinner spnX = new BigDecimalSpinner(cmpTxt, SWT.NONE, NUMBER_FORMAT);
            this.spnXPtr[0] = spnX;
            spnX.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
            spnX.setMaximum(new BigDecimal(1000000));
            spnX.setMinimum(new BigDecimal(-1000000));
            spnX.setValue(v.xp);
        }

        {
            Composite cmpTxt = new Composite(cmpContainer, SWT.NONE);
            cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpTxt.setLayout(new GridLayout(6, true));

            NButton cbYaxis = new NButton(cmpTxt, SWT.CHECK);
            this.cbYaxisPtr[0] = cbYaxis;
            cbYaxis.setText(I18n.SCALE_Y);
            cbYaxis.setSelection(true);

            BigDecimalSpinner spnY = new BigDecimalSpinner(cmpTxt, SWT.NONE, NUMBER_FORMAT);
            this.spnYPtr[0] = spnY;
            spnY.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
            spnY.setMaximum(new BigDecimal(1000000));
            spnY.setMinimum(new BigDecimal(-1000000));
            spnY.setValue(v.yp);
        }

        {
            Composite cmpTxt = new Composite(cmpContainer, SWT.NONE);
            cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpTxt.setLayout(new GridLayout(6, true));

            NButton cbZaxis = new NButton(cmpTxt, SWT.CHECK);
            this.cbZaxisPtr[0] = cbZaxis;
            cbZaxis.setText(I18n.SCALE_Z);
            cbZaxis.setSelection(true);

            BigDecimalSpinner spnZ = new BigDecimalSpinner(cmpTxt, SWT.NONE, NUMBER_FORMAT);
            this.spnZPtr[0] = spnZ;
            spnZ.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
            spnZ.setMaximum(new BigDecimal(1000000));
            spnZ.setMinimum(new BigDecimal(-1000000));
            spnZ.setValue(v.zp);
        }

        Label lblPivot = new Label(cmpContainer, SWT.NONE);
        lblPivot.setText(I18n.SCALE_PIVOT);

        {
            Composite cmpTxt = new Composite(cmpContainer, SWT.NONE);
            cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpTxt.setLayout(new GridLayout(1, true));
            NButton btnPivotManipulator = new NButton(cmpTxt, SWT.NONE);
            this.btnPivotManipulatorPtr[0] = btnPivotManipulator;
            btnPivotManipulator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            btnPivotManipulator.setImage(ResourceManager.getImage("icon8_local.png")); //$NON-NLS-1$
            btnPivotManipulator.setText(I18n.SCALE_PIVOT_MANIPULATOR);
        }
        {
            Composite cmpTxt = new Composite(cmpContainer, SWT.NONE);
            cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpTxt.setLayout(new GridLayout(1, true));
            NButton btnPivotClipboard = new NButton(cmpTxt, SWT.NONE);
            this.btnPivotClipboardPtr[0] = btnPivotClipboard;
            btnPivotClipboard.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            btnPivotClipboard.setImage(ResourceManager.getImage("icon8_edit-paste.png")); //$NON-NLS-1$
            btnPivotClipboard.setText(I18n.SCALE_PIVOT_CLIPBOARD);
        }

        {
            Composite cmpTxt = new Composite(cmpContainer, SWT.NONE);
            cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpTxt.setLayout(new GridLayout(1, true));

            BigDecimalSpinner spnPX = new BigDecimalSpinner(cmpTxt, SWT.NONE, NUMBER_FORMAT);
            this.spnPXPtr[0] = spnPX;
            spnPX.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            spnPX.setMaximum(new BigDecimal(1000000));
            spnPX.setMinimum(new BigDecimal(-1000000));
            spnPX.setValue(p.xp);
        }


        {
            Composite cmpTxt = new Composite(cmpContainer, SWT.NONE);
            cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpTxt.setLayout(new GridLayout(1, true));

            BigDecimalSpinner spnPY = new BigDecimalSpinner(cmpTxt, SWT.NONE, NUMBER_FORMAT);
            this.spnPYPtr[0] = spnPY;
            spnPY.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            spnPY.setMaximum(new BigDecimal(1000000));
            spnPY.setMinimum(new BigDecimal(-1000000));
            spnPY.setValue(p.yp);
        }

        {
            Composite cmpTxt = new Composite(cmpContainer, SWT.NONE);
            cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpTxt.setLayout(new GridLayout(1, true));

            BigDecimalSpinner spnPZ = new BigDecimalSpinner(cmpTxt, SWT.NONE, NUMBER_FORMAT);
            this.spnPZPtr[0] = spnPZ;
            spnPZ.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            spnPZ.setMaximum(new BigDecimal(1000000));
            spnPZ.setMinimum(new BigDecimal(-1000000));
            spnPZ.setValue(p.zp);
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
        btnCopyPtr[0] = createButton(parent, IDialogConstants.OK_ID, I18n.E3D_CREATE_TRANSFORMED_COPY, false);
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
