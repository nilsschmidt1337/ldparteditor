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
package org.nschmidt.ldparteditor.dialogs.slantingmatrixprojector;

import static org.nschmidt.ldparteditor.helpers.WidgetUtility.WidgetUtil;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.nschmidt.ldparteditor.data.Matrix;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.enums.Font;
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.composite3d.SlantingMatrixProjectorSettings;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.widgets.NButton;

/**
 * The SlantingMatrixProjector dialog
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 *
 * @author nils
 *
 */
class SlantingMatrixProjectorDesign extends Dialog {

    private final Text[] m00Ptr = new Text[1];
    private final Text[] m01Ptr = new Text[1];
    private final Text[] m02Ptr = new Text[1];
    private final Text[] m03Ptr = new Text[1];
    private final Text[] m10Ptr = new Text[1];
    private final Text[] m11Ptr = new Text[1];
    private final Text[] m12Ptr = new Text[1];
    private final Text[] m13Ptr = new Text[1];
    private final Text[] m20Ptr = new Text[1];
    private final Text[] m21Ptr = new Text[1];
    private final Text[] m22Ptr = new Text[1];
    private final Text[] m23Ptr = new Text[1];
    private final Text[] m30Ptr = new Text[1];
    private final Text[] m31Ptr = new Text[1];
    private final Text[] m32Ptr = new Text[1];
    private final Text[] m33Ptr = new Text[1];

    // Use final only for subclass/listener references!
    private final VertexManager vm;
    private final SlantingMatrixProjectorSettings mps;
    private java.text.DecimalFormat numberFormat = new java.text.DecimalFormat(View.NUMBER_FORMATL4F, new DecimalFormatSymbols(MyLanguage.locale));

    SlantingMatrixProjectorDesign(Shell parentShell, VertexManager vm, SlantingMatrixProjectorSettings mps) {
        super(parentShell);
        this.vm = vm;
        this.mps = mps;
    }

    /**
     * Create contents of the dialog.
     *
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite cmpContainer = (Composite) super.createDialogArea(parent);
        cmpContainer.setLayout(new GridLayout(4, true));
        GridLayout gridLayout = (GridLayout) cmpContainer.getLayout();
        gridLayout.verticalSpacing = 10;
        gridLayout.horizontalSpacing = 10;

        Label lblTitle = new Label(cmpContainer, SWT.NONE);
        lblTitle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
        lblTitle.setText(I18n.SLANT_TITLE);

        Label lblSeparator = new Label(cmpContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
        lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));

        Label lblInfo = new Label(cmpContainer, SWT.NONE);
        lblInfo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));

        switch (vm.getSlantingMatrixStatus()) {
        case NO_SELECTION_THREE_AXIS:
            lblInfo.setText(I18n.SLANT_NO_SELECTION_THREE_AXES);
            insertMatrix(cmpContainer);
            break;
        case NO_SELECTION_TWO_AXIS:
            lblInfo.setText(I18n.SLANT_NO_SELECTION_TWO_AXES);
            insertMatrix(cmpContainer);
            {
                NButton cbSetOrigin = new NButton(cmpContainer, SWT.CHECK);
                cbSetOrigin.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
                cbSetOrigin.setText(I18n.SLANT_SET_ORIGIN);
                cbSetOrigin.setSelection(mps.isMovingOriginToAxisCenter());

                WidgetUtil(cbSetOrigin).addSelectionListener(e -> {
                    mps.setMovingOriginToAxisCenter(!mps.isMovingOriginToAxisCenter());
                    updateMatrix();
                });
            }
            break;
        case SELECTION:
            lblInfo.setText(I18n.SLANT_MATRIX_READY);
            insertMatrix(cmpContainer);
            {
                NButton cbSetOrigin = new NButton(cmpContainer, SWT.CHECK);
                cbSetOrigin.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
                cbSetOrigin.setText(I18n.SLANT_SET_ORIGIN);
                cbSetOrigin.setSelection(mps.isMovingOriginToAxisCenter());

                WidgetUtil(cbSetOrigin).addSelectionListener(e -> {
                    mps.setMovingOriginToAxisCenter(!mps.isMovingOriginToAxisCenter());
                    updateMatrix();
                });
            }
            {
                NButton cbResetSubfileOrientation = new NButton(cmpContainer, SWT.CHECK);
                cbResetSubfileOrientation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
                cbResetSubfileOrientation.setText(I18n.SLANT_RESET_SUBFILE_ORIENTATION);
                cbResetSubfileOrientation.setSelection(mps.isResettingSubfileTransformation());


                WidgetUtil(cbResetSubfileOrientation).addSelectionListener(e -> mps.setResettingSubfileTransformation(!mps.isResettingSubfileTransformation()));
            }
            break;
        case INIT:
        default:
            lblInfo.setText(I18n.SLANT_HOW_TO);
            break;
        }

        cmpContainer.pack();
        return cmpContainer;
    }

    private void insertMatrix(Composite cmpContainer) {

        final Matrix m = vm.getSlantingMatrix(mps.isMovingOriginToAxisCenter());

        insertMatrixCell(cmpContainer, m.m00, m00Ptr);
        insertMatrixCell(cmpContainer, m.m10, m10Ptr);
        insertMatrixCell(cmpContainer, m.m20, m20Ptr);
        insertMatrixCell(cmpContainer, m.m30, m30Ptr);
        insertMatrixCell(cmpContainer, m.m01, m01Ptr);
        insertMatrixCell(cmpContainer, m.m11, m11Ptr);
        insertMatrixCell(cmpContainer, m.m21, m21Ptr);
        insertMatrixCell(cmpContainer, m.m31, m31Ptr);
        insertMatrixCell(cmpContainer, m.m02, m02Ptr);
        insertMatrixCell(cmpContainer, m.m12, m12Ptr);
        insertMatrixCell(cmpContainer, m.m22, m22Ptr);
        insertMatrixCell(cmpContainer, m.m32, m32Ptr);
        insertMatrixCell(cmpContainer, BigDecimal.ZERO, m03Ptr);
        insertMatrixCell(cmpContainer, BigDecimal.ZERO, m13Ptr);
        insertMatrixCell(cmpContainer, BigDecimal.ZERO, m23Ptr);
        insertMatrixCell(cmpContainer, BigDecimal.ONE, m33Ptr);

        NButton btnCopyMatrixToClipboard = new NButton(cmpContainer, SWT.NONE);
        btnCopyMatrixToClipboard.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
        btnCopyMatrixToClipboard.setText(I18n.SLANT_COPY_TO_CLIPBOARD);

        WidgetUtil(btnCopyMatrixToClipboard).addSelectionListener(e -> {
            final Matrix m1 = vm.getSlantingMatrix(mps.isMovingOriginToAxisCenter());
            final StringBuilder cbString = new StringBuilder();
            cbString.append("1 16 "); //$NON-NLS-1$
            cbString.append(m1.toLDrawString());
            cbString.append(" "); //$NON-NLS-1$
            final String cbs = cbString.toString();
            Display display = Display.getCurrent();
            Clipboard clipboard = new Clipboard(display);
            clipboard.setContents(new Object[] { cbs }, new Transfer[] { TextTransfer.getInstance() });
            clipboard.dispose();
        });
    }

    private void insertMatrixCell(Composite cmpContainer, BigDecimal val, Text[] textCmp) {
        Text txtCell = new Text(cmpContainer, SWT.NONE);
        textCmp[0] = txtCell;
        txtCell.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        txtCell.setText(numberFormat.format(val));
        txtCell.setFont(Font.MONOSPACE);
        txtCell.setEditable(false);
    }

    private void updateMatrix() {
        final Matrix m = vm.getSlantingMatrix(mps.isMovingOriginToAxisCenter());
        updateMatrixCell(m00Ptr, m.m00);
        updateMatrixCell(m01Ptr, m.m01);
        updateMatrixCell(m02Ptr, m.m02);
        updateMatrixCell(m03Ptr, m.m03);
        updateMatrixCell(m10Ptr, m.m10);
        updateMatrixCell(m11Ptr, m.m11);
        updateMatrixCell(m12Ptr, m.m12);
        updateMatrixCell(m13Ptr, m.m13);
        updateMatrixCell(m20Ptr, m.m20);
        updateMatrixCell(m21Ptr, m.m21);
        updateMatrixCell(m22Ptr, m.m22);
        updateMatrixCell(m23Ptr, m.m23);
        updateMatrixCell(m30Ptr, m.m30);
        updateMatrixCell(m31Ptr, m.m31);
        updateMatrixCell(m32Ptr, m.m32);
        updateMatrixCell(m33Ptr, m.m33);
    }

    private void updateMatrixCell(Text[] textCmp, BigDecimal val) {
        textCmp[0].setText(numberFormat.format(val));
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
