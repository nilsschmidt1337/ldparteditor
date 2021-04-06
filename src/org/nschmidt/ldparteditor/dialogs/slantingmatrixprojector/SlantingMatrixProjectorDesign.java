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

    private final Text[] M00 = new Text[1];
    private final Text[] M01 = new Text[1];
    private final Text[] M02 = new Text[1];
    private final Text[] M03 = new Text[1];
    private final Text[] M10 = new Text[1];
    private final Text[] M11 = new Text[1];
    private final Text[] M12 = new Text[1];
    private final Text[] M13 = new Text[1];
    private final Text[] M20 = new Text[1];
    private final Text[] M21 = new Text[1];
    private final Text[] M22 = new Text[1];
    private final Text[] M23 = new Text[1];
    private final Text[] M30 = new Text[1];
    private final Text[] M31 = new Text[1];
    private final Text[] M32 = new Text[1];
    private final Text[] M33 = new Text[1];

    // Use final only for subclass/listener references!
    private final VertexManager vm;
    private final SlantingMatrixProjectorSettings mps;
    private java.text.DecimalFormat numberFormat = new java.text.DecimalFormat(View.NUMBER_FORMATL4F, new DecimalFormatSymbols(MyLanguage.LOCALE));

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

        insertMatrixCell(cmpContainer, m.M00, M00);
        insertMatrixCell(cmpContainer, m.M10, M10);
        insertMatrixCell(cmpContainer, m.M20, M20);
        insertMatrixCell(cmpContainer, m.M30, M30);
        insertMatrixCell(cmpContainer, m.M01, M01);
        insertMatrixCell(cmpContainer, m.M11, M11);
        insertMatrixCell(cmpContainer, m.M21, M21);
        insertMatrixCell(cmpContainer, m.M31, M31);
        insertMatrixCell(cmpContainer, m.M02, M02);
        insertMatrixCell(cmpContainer, m.M12, M12);
        insertMatrixCell(cmpContainer, m.M22, M22);
        insertMatrixCell(cmpContainer, m.M32, M32);
        insertMatrixCell(cmpContainer, BigDecimal.ZERO, M03);
        insertMatrixCell(cmpContainer, BigDecimal.ZERO, M13);
        insertMatrixCell(cmpContainer, BigDecimal.ZERO, M23);
        insertMatrixCell(cmpContainer, BigDecimal.ONE, M33);

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
        updateMatrixCell(M00, m.M00);
        updateMatrixCell(M01, m.M01);
        updateMatrixCell(M02, m.M02);
        updateMatrixCell(M03, m.M03);
        updateMatrixCell(M10, m.M10);
        updateMatrixCell(M11, m.M11);
        updateMatrixCell(M12, m.M12);
        updateMatrixCell(M13, m.M13);
        updateMatrixCell(M20, m.M20);
        updateMatrixCell(M21, m.M21);
        updateMatrixCell(M22, m.M22);
        updateMatrixCell(M23, m.M23);
        updateMatrixCell(M30, m.M30);
        updateMatrixCell(M31, m.M31);
        updateMatrixCell(M32, m.M32);
        updateMatrixCell(M33, m.M33);
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
