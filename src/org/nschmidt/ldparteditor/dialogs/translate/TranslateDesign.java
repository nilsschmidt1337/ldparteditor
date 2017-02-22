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
package org.nschmidt.ldparteditor.dialogs.translate;

import java.math.BigDecimal;

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
import org.nschmidt.ldparteditor.enums.ManipulatorScope;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.resources.ResourceManager;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.widgets.BigDecimalSpinner;

/**
 * The translate dialog
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 *
 * @author nils
 *
 */
class TranslateDesign extends Dialog {

    static ManipulatorScope transformationMode = ManipulatorScope.LOCAL;

    final Button[] btn_Local = new Button[1];
    final Button[] btn_Global = new Button[1];

    final Button[] cb_Xaxis = new Button[1];
    final Button[] cb_Yaxis = new Button[1];
    final Button[] cb_Zaxis = new Button[1];
    final BigDecimalSpinner[] spn_X = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_Y = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_Z = new BigDecimalSpinner[1];

    final Button[] btn_ToManipulatorPosition = new Button[1];

    private final String NUMBER_FORMAT = View.NUMBER_FORMAT8F;

    // Use final only for subclass/listener references!

    TranslateDesign(Shell parentShell, ManipulatorScope scope) {
        super(parentShell);
        transformationMode = scope;
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
        lbl_specify.setText(I18n.TRANSLATE_Title);

        Label lbl_separator = new Label(cmp_container, SWT.SEPARATOR | SWT.HORIZONTAL);
        lbl_separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        if (Editor3DWindow.getWindow().isMovingAdjacentData()) {
            Label lbl_adjacencyWarning = new Label(cmp_container, SWT.NONE);
            lbl_adjacencyWarning.setText(I18n.E3D_AdjacentWarningStatus);
            lbl_adjacencyWarning.setToolTipText(I18n.E3D_AdjacentWarningDialog);
            lbl_adjacencyWarning.setForeground(SWTResourceManager.getColor(SWT.COLOR_INFO_FOREGROUND));
            lbl_adjacencyWarning.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
        }

        ToolItem toolItem_TransformationModes = new ToolItem(cmp_container, SWT.NONE, true);
        {
            Button btn_Local = new Button(toolItem_TransformationModes, SWT.TOGGLE);
            this.btn_Local[0] = btn_Local;
            btn_Local.setToolTipText(I18n.E3D_Local);
            if (transformationMode == ManipulatorScope.LOCAL) btn_Local.setSelection(true);
            btn_Local.setImage(ResourceManager.getImage("icon16_local.png")); //$NON-NLS-1$
        }
        {
            Button btn_Global = new Button(toolItem_TransformationModes, SWT.TOGGLE);
            this.btn_Global[0] = btn_Global;
            btn_Global.setToolTipText(I18n.E3D_Global);
            if (transformationMode == ManipulatorScope.GLOBAL) btn_Global.setSelection(true);
            btn_Global.setImage(ResourceManager.getImage("icon16_global.png")); //$NON-NLS-1$
        }

        {
            Composite cmp_txt = new Composite(cmp_container, SWT.NONE);
            cmp_txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmp_txt.setLayout(new GridLayout(6, true));

            Button cb_Xaxis = new Button(cmp_txt, SWT.CHECK);
            this.cb_Xaxis[0] = cb_Xaxis;
            cb_Xaxis.setText(I18n.TRANSLATE_X);
            cb_Xaxis.setSelection(true);

            BigDecimalSpinner spn_X = new BigDecimalSpinner(cmp_txt, SWT.NONE, NUMBER_FORMAT);
            this.spn_X[0] = spn_X;
            spn_X.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
            spn_X.setMaximum(new BigDecimal(1000000));
            spn_X.setMinimum(new BigDecimal(-1000000));
            spn_X.setValue(BigDecimal.ZERO);
        }

        {
            Composite cmp_txt = new Composite(cmp_container, SWT.NONE);
            cmp_txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmp_txt.setLayout(new GridLayout(6, true));

            Button cb_Yaxis = new Button(cmp_txt, SWT.CHECK);
            this.cb_Yaxis[0] = cb_Yaxis;
            cb_Yaxis.setText(I18n.TRANSLATE_Y);
            cb_Yaxis.setSelection(true);

            BigDecimalSpinner spn_Y = new BigDecimalSpinner(cmp_txt, SWT.NONE, NUMBER_FORMAT);
            this.spn_Y[0] = spn_Y;
            spn_Y.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
            spn_Y.setMaximum(new BigDecimal(1000000));
            spn_Y.setMinimum(new BigDecimal(-1000000));
            spn_Y.setValue(BigDecimal.ZERO);
        }

        {
            Composite cmp_txt = new Composite(cmp_container, SWT.NONE);
            cmp_txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmp_txt.setLayout(new GridLayout(6, true));

            Button cb_Zaxis = new Button(cmp_txt, SWT.CHECK);
            this.cb_Zaxis[0] = cb_Zaxis;
            cb_Zaxis.setText(I18n.TRANSLATE_Z);
            cb_Zaxis.setSelection(true);

            BigDecimalSpinner spn_Z = new BigDecimalSpinner(cmp_txt, SWT.NONE, NUMBER_FORMAT);
            this.spn_Z[0] = spn_Z;
            spn_Z.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
            spn_Z.setMaximum(new BigDecimal(1000000));
            spn_Z.setMinimum(new BigDecimal(-1000000));
            spn_Z.setValue(BigDecimal.ZERO);
        }

        {
            Button btn_ToManipulatorPosition = new Button(cmp_container, SWT.NONE);
            this.btn_ToManipulatorPosition[0] = btn_ToManipulatorPosition;
            btn_ToManipulatorPosition.setText(I18n.TRANSLATE_ToManipulatorPosition);
            btn_ToManipulatorPosition.setImage(ResourceManager.getImage("icon16_local.png")); //$NON-NLS-1$
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
