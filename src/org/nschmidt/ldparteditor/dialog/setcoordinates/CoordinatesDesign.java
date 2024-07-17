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
package org.nschmidt.ldparteditor.dialog.setcoordinates;

import java.math.BigDecimal;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.nschmidt.ldparteditor.composite.ToolItem;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.dialog.ThemedDialog;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.resource.ResourceManager;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.MiscToggleToolItem;
import org.nschmidt.ldparteditor.widget.BigDecimalSpinner;
import org.nschmidt.ldparteditor.widget.NButton;
import org.nschmidt.ldparteditor.workbench.Theming;

/**
 * The coordinates dialog
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 */
class CoordinatesDesign extends ThemedDialog {

    final NButton[] btnLocalPtr = new NButton[1];
    final NButton[] btnGlobalPtr = new NButton[1];

    final Button[] btnCopyPtr = new Button[1];

    final NButton[] cbXaxisPtr = new NButton[1];
    final NButton[] cbYaxisPtr = new NButton[1];
    final NButton[] cbZaxisPtr = new NButton[1];
    final BigDecimalSpinner[] spnXPtr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnYPtr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnZPtr = new BigDecimalSpinner[1];
    final NButton[] btnClipboardPtr = new NButton[1];
    final NButton[] btnManipulatorPtr = new NButton[1];
    final NButton[] btnSetManipulatorPtr = new NButton[1];

    Vertex m = new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    Vertex c = new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

    private static final String NUMBER_FORMAT8F = View.NUMBER_FORMAT8F;

    // Use final only for subclass/listener references!

    private Vertex v = new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

    CoordinatesDesign(Shell parentShell, Vertex v, Vertex manipulatorPosition) {
        super(parentShell);
        if (manipulatorPosition != null) {
            m = manipulatorPosition;
        }
        if (v != null) {
            this.v = v;
            final Vertex clipboardVertex = VertexManager.getSingleVertexFromClipboard();
            if (clipboardVertex == null) {
                c = new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
            } else {
                c = new Vertex(clipboardVertex.xp, clipboardVertex.yp, clipboardVertex.zp);
            }

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

        Label lblSpecify = Theming.label(cmpContainer, SWT.NONE);
        lblSpecify.setText(I18n.COORDINATESDIALOG_SET_XYZ);

        Label lblSeparator = Theming.label(cmpContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
        lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        if (MiscToggleToolItem.isMovingAdjacentData()) {
            Label lblAdjacencyWarning = Theming.label(cmpContainer, SWT.NONE);
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
        }
        {
            NButton btnGlobal = new NButton(toolItemTransformationModes, SWT.TOGGLE);
            this.btnGlobalPtr[0] = btnGlobal;
            btnGlobal.setToolTipText(I18n.E3D_GLOBAL);
            btnGlobal.setSelection(true);
            btnGlobal.setImage(ResourceManager.getImage("icon16_global.png")); //$NON-NLS-1$
        }

        {
            Composite cmpTxt = Theming.composite(cmpContainer, SWT.NONE);
            cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpTxt.setLayout(new GridLayout(1, true));
            NButton btnManipulator = new NButton(cmpTxt, SWT.NONE);
            this.btnManipulatorPtr[0] = btnManipulator;
            btnManipulator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            btnManipulator.setImage(ResourceManager.getImage("icon8_local.png")); //$NON-NLS-1$
            btnManipulator.setText(I18n.COORDINATESDIALOG_MANIPULATOR);
        }
        {
            Composite cmpTxt = Theming.composite(cmpContainer, SWT.NONE);
            cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpTxt.setLayout(new GridLayout(1, true));
            NButton btnClipboard = new NButton(cmpTxt, SWT.NONE);
            this.btnClipboardPtr[0] = btnClipboard;
            btnClipboard.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            btnClipboard.setImage(ResourceManager.getImage("icon8_edit-paste.png")); //$NON-NLS-1$
            btnClipboard.setText(I18n.COORDINATESDIALOG_CLIPBOARD);
        }

        {
            Composite cmpTxt = Theming.composite(cmpContainer, SWT.NONE);
            cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpTxt.setLayout(new GridLayout(6, true));

            NButton cbXaxis = new NButton(cmpTxt, SWT.CHECK);
            this.cbXaxisPtr[0] = cbXaxis;
            cbXaxis.setText(I18n.COORDINATESDIALOG_X);
            cbXaxis.setSelection(false);

            BigDecimalSpinner spnX = new BigDecimalSpinner(cmpTxt, SWT.NONE, NUMBER_FORMAT8F);
            this.spnXPtr[0] = spnX;
            spnX.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
            spnX.setMaximum(new BigDecimal(1000000));
            spnX.setMinimum(new BigDecimal(-1000000));
            spnX.setValue(v.xp);
        }

        {
            Composite cmpTxt = Theming.composite(cmpContainer, SWT.NONE);
            cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpTxt.setLayout(new GridLayout(6, true));

            NButton cbYaxis = new NButton(cmpTxt, SWT.CHECK);
            this.cbYaxisPtr[0] = cbYaxis;
            cbYaxis.setText(I18n.COORDINATESDIALOG_Y);
            cbYaxis.setSelection(false);

            BigDecimalSpinner spnY = new BigDecimalSpinner(cmpTxt, SWT.NONE, NUMBER_FORMAT8F);
            this.spnYPtr[0] = spnY;
            spnY.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
            spnY.setMaximum(new BigDecimal(1000000));
            spnY.setMinimum(new BigDecimal(-1000000));
            spnY.setValue(v.yp);
        }

        {
            Composite cmpTxt = Theming.composite(cmpContainer, SWT.NONE);
            cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpTxt.setLayout(new GridLayout(6, true));

            NButton cbZaxis = new NButton(cmpTxt, SWT.CHECK);
            this.cbZaxisPtr[0] = cbZaxis;
            cbZaxis.setText(I18n.COORDINATESDIALOG_Z);
            cbZaxis.setSelection(false);

            BigDecimalSpinner spnZ = new BigDecimalSpinner(cmpTxt, SWT.NONE, NUMBER_FORMAT8F);
            this.spnZPtr[0] = spnZ;
            spnZ.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
            spnZ.setMaximum(new BigDecimal(1000000));
            spnZ.setMinimum(new BigDecimal(-1000000));
            spnZ.setValue(v.zp);
        }

        {
            Composite cmpTxt = Theming.composite(cmpContainer, SWT.NONE);
            cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpTxt.setLayout(new GridLayout(1, true));
            NButton btnSetManipulator = new NButton(cmpTxt, SWT.NONE);
            this.btnSetManipulatorPtr[0] = btnSetManipulator;
            btnSetManipulator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            btnSetManipulator.setImage(ResourceManager.getImage("icon8_local.png")); //$NON-NLS-1$
            btnSetManipulator.setText(I18n.COORDINATESDIALOG_SET_MANIPULATOR);
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
}
