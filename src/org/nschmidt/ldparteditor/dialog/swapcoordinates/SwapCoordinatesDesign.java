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
package org.nschmidt.ldparteditor.dialog.swapcoordinates;

import org.eclipse.jface.dialogs.Dialog;
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
import org.nschmidt.ldparteditor.enumtype.ManipulatorScope;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.resource.ResourceManager;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.MiscToggleToolItem;
import org.nschmidt.ldparteditor.widget.NButton;
import org.nschmidt.ldparteditor.workbench.Theming;

/**
 * The swap coordinates dialog
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 */
class SwapCoordinatesDesign extends Dialog {

    protected static ManipulatorScope transformationMode = ManipulatorScope.GLOBAL;

    final NButton[] btnLocalPtr = new NButton[1];
    final NButton[] btnGlobalPtr = new NButton[1];

    final Button[] btnCopyPtr = new Button[1];

    final NButton[] btnSwapXZPtr = new NButton[1];
    final NButton[] btnSwapXYPtr = new NButton[1];
    final NButton[] btnSwapYZPtr = new NButton[1];

    // Use final only for subclass/listener references!

    SwapCoordinatesDesign(Shell parentShell) {
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
        cmpContainer.setBackground(Theming.getBgColor());
        GridLayout gridLayout = (GridLayout) cmpContainer.getLayout();
        gridLayout.verticalSpacing = 5;
        gridLayout.horizontalSpacing = 10;

        Label lblSpecify = Theming.label(cmpContainer, SWT.NONE);
        lblSpecify.setText(I18n.SWAPCOORDINATES_SWAP_XYZ);

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
            if (transformationMode == ManipulatorScope.LOCAL) btnLocal.setSelection(true);
            btnLocal.setImage(ResourceManager.getImage("icon16_local.png")); //$NON-NLS-1$
        }
        {
            NButton btnGlobal = new NButton(toolItemTransformationModes, SWT.TOGGLE);
            this.btnGlobalPtr[0] = btnGlobal;
            btnGlobal.setToolTipText(I18n.E3D_GLOBAL);
            if (transformationMode == ManipulatorScope.GLOBAL) btnGlobal.setSelection(true);
            btnGlobal.setImage(ResourceManager.getImage("icon16_global.png")); //$NON-NLS-1$
        }

        {
            Composite cmpTxt = Theming.composite(cmpContainer, SWT.NONE);
            cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpTxt.setLayout(new GridLayout(1, true));
            NButton btnSwapXY = new NButton(cmpTxt, SWT.RADIO);
            this.btnSwapXYPtr[0] = btnSwapXY;
            btnSwapXY.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            btnSwapXY.setImage(ResourceManager.getImage("icon16_XswapY.png")); //$NON-NLS-1$
            btnSwapXY.setText(' ' + I18n.SWAPCOORDINATES_SWAP_ON_XY);
            btnSwapXY.setSelection(true);
        }
        {
            Composite cmpTxt = Theming.composite(cmpContainer, SWT.NONE);
            cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpTxt.setLayout(new GridLayout(1, true));
            NButton btnSwapXZ = new NButton(cmpTxt, SWT.RADIO);
            this.btnSwapXZPtr[0] = btnSwapXZ;
            btnSwapXZ.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            btnSwapXZ.setImage(ResourceManager.getImage("icon16_XswapZ.png")); //$NON-NLS-1$
            btnSwapXZ.setText(' ' + I18n.SWAPCOORDINATES_SWAP_ON_XZ);
        }

        {
            Composite cmpTxt = Theming.composite(cmpContainer, SWT.NONE);
            cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmpTxt.setLayout(new GridLayout(1, true));
            NButton btnSwapYZ = new NButton(cmpTxt, SWT.RADIO);
            this.btnSwapYZPtr[0] = btnSwapYZ;
            btnSwapYZ.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            btnSwapYZ.setImage(ResourceManager.getImage("icon16_YswapZ.png")); //$NON-NLS-1$
            btnSwapYZ.setText(' ' + I18n.SWAPCOORDINATES_SWAP_ON_YZ);
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

    @Override
    protected Control createButtonBar(Composite parent) {
        final Control btnBar = super.createButtonBar(parent);
        parent.setBackground(Theming.getBgColor());
        btnBar.setBackground(Theming.getBgColor());
        return btnBar;
    }

    @Override
    protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
        final Button btn = super.createButton(parent, id, label, defaultButton);
        btn.setBackground(Theming.getBgColor());
        btn.setForeground(Theming.getFgColor());
        return btn;
    }
}
