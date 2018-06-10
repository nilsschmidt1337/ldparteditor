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

    final Text[] M00 = new Text[1];
    final Text[] M01 = new Text[1];
    final Text[] M02 = new Text[1];
    final Text[] M03 = new Text[1];
    final Text[] M10 = new Text[1];
    final Text[] M11 = new Text[1];
    final Text[] M12 = new Text[1];
    final Text[] M13 = new Text[1];
    final Text[] M20 = new Text[1];
    final Text[] M21 = new Text[1];
    final Text[] M22 = new Text[1];
    final Text[] M23 = new Text[1];
    final Text[] M30 = new Text[1];
    final Text[] M31 = new Text[1];
    final Text[] M32 = new Text[1];
    final Text[] M33 = new Text[1];

    // Use final only for subclass/listener references!
    final VertexManager vm;
    final SlantingMatrixProjectorSettings mps;
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
        Composite cmp_container = (Composite) super.createDialogArea(parent);
        cmp_container.setLayout(new GridLayout(4, true));
        GridLayout gridLayout = (GridLayout) cmp_container.getLayout();
        gridLayout.verticalSpacing = 10;
        gridLayout.horizontalSpacing = 10;

        Label lbl_title = new Label(cmp_container, SWT.NONE);
        lbl_title.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
        lbl_title.setText(I18n.SLANT_Title);

        Label lbl_separator = new Label(cmp_container, SWT.SEPARATOR | SWT.HORIZONTAL);
        lbl_separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));

        Label lbl_info = new Label(cmp_container, SWT.NONE);
        lbl_info.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));

        switch (vm.getSlantingMatrixStatus()) {
        case NO_SELECTION_THREE_AXIS:
            lbl_info.setText(I18n.SLANT_NoSelectionThreeAxes);
            insertMatrix(cmp_container);
            break;
        case NO_SELECTION_TWO_AXIS:
            lbl_info.setText(I18n.SLANT_NoSelectionTwoAxes);
            insertMatrix(cmp_container);
            {
                NButton cb_setOrigin = new NButton(cmp_container, SWT.CHECK);
                cb_setOrigin.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
                cb_setOrigin.setText(I18n.SLANT_SetOrigin);
                cb_setOrigin.setSelection(mps.isMovingOriginToAxisCenter());

                WidgetUtil(cb_setOrigin).addSelectionListener(e -> {
                    mps.setMovingOriginToAxisCenter(!mps.isMovingOriginToAxisCenter());
                    updateMatrix();
                });
            }
            break;
        case SELECTION:
            lbl_info.setText(I18n.SLANT_MatrixReady);
            insertMatrix(cmp_container);
            {
                NButton cb_setOrigin = new NButton(cmp_container, SWT.CHECK);
                cb_setOrigin.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
                cb_setOrigin.setText(I18n.SLANT_SetOrigin);
                cb_setOrigin.setSelection(mps.isMovingOriginToAxisCenter());

                WidgetUtil(cb_setOrigin).addSelectionListener(e -> {
                    mps.setMovingOriginToAxisCenter(!mps.isMovingOriginToAxisCenter());
                    updateMatrix();
                });
            }
            {
                NButton cb_resetSubfileOrientation = new NButton(cmp_container, SWT.CHECK);
                cb_resetSubfileOrientation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
                cb_resetSubfileOrientation.setText(I18n.SLANT_ResetSubfileOrientation);
                cb_resetSubfileOrientation.setSelection(mps.isResettingSubfileTransformation());


                WidgetUtil(cb_resetSubfileOrientation).addSelectionListener(e -> mps.setResettingSubfileTransformation(!mps.isResettingSubfileTransformation()));
            }
            break;
        case INIT:
        default:
            lbl_info.setText(I18n.SLANT_HowTo);
            break;
        }

        cmp_container.pack();
        return cmp_container;
    }

    private void insertMatrix(Composite cmp_container) {

        final Matrix M = vm.getSlantingMatrix(mps.isMovingOriginToAxisCenter());

        insertMatrixCell(cmp_container, M.M00, M00);
        insertMatrixCell(cmp_container, M.M10, M10);
        insertMatrixCell(cmp_container, M.M20, M20);
        insertMatrixCell(cmp_container, M.M30, M30);
        insertMatrixCell(cmp_container, M.M01, M01);
        insertMatrixCell(cmp_container, M.M11, M11);
        insertMatrixCell(cmp_container, M.M21, M21);
        insertMatrixCell(cmp_container, M.M31, M31);
        insertMatrixCell(cmp_container, M.M02, M02);
        insertMatrixCell(cmp_container, M.M12, M12);
        insertMatrixCell(cmp_container, M.M22, M22);
        insertMatrixCell(cmp_container, M.M32, M32);
        insertMatrixCell(cmp_container, BigDecimal.ZERO, M03);
        insertMatrixCell(cmp_container, BigDecimal.ZERO, M13);
        insertMatrixCell(cmp_container, BigDecimal.ZERO, M23);
        insertMatrixCell(cmp_container, BigDecimal.ONE, M33);

        NButton btn_CopyMatrixToClipboard = new NButton(cmp_container, SWT.NONE);
        btn_CopyMatrixToClipboard.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
        btn_CopyMatrixToClipboard.setText(I18n.SLANT_CopyToClipboard);

        WidgetUtil(btn_CopyMatrixToClipboard).addSelectionListener(e -> {
            final Matrix M1 = vm.getSlantingMatrix(mps.isMovingOriginToAxisCenter());
            final StringBuilder cbString = new StringBuilder();
            cbString.append("1 16 "); //$NON-NLS-1$
            cbString.append(M1.toLDrawString());
            cbString.append(" "); //$NON-NLS-1$
            final String cbs = cbString.toString();
            Display display = Display.getCurrent();
            Clipboard clipboard = new Clipboard(display);
            clipboard.setContents(new Object[] { cbs }, new Transfer[] { TextTransfer.getInstance() });
            clipboard.dispose();
        });
    }

    private void insertMatrixCell(Composite cmp_container, BigDecimal val, Text[] textCmp) {
        Text txt_Cell = new Text(cmp_container, SWT.NONE);
        textCmp[0] = txt_Cell;
        txt_Cell.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        txt_Cell.setText(numberFormat.format(val));
        txt_Cell.setFont(Font.MONOSPACE);
        txt_Cell.setEditable(false);
    }

    void updateMatrix() {
        final Matrix M = vm.getSlantingMatrix(mps.isMovingOriginToAxisCenter());
        updateMatrixCell(M00, M.M00);
        updateMatrixCell(M01, M.M01);
        updateMatrixCell(M02, M.M02);
        updateMatrixCell(M03, M.M03);
        updateMatrixCell(M10, M.M10);
        updateMatrixCell(M11, M.M11);
        updateMatrixCell(M12, M.M12);
        updateMatrixCell(M13, M.M13);
        updateMatrixCell(M20, M.M20);
        updateMatrixCell(M21, M.M21);
        updateMatrixCell(M22, M.M22);
        updateMatrixCell(M23, M.M23);
        updateMatrixCell(M30, M.M30);
        updateMatrixCell(M31, M.M31);
        updateMatrixCell(M32, M.M32);
        updateMatrixCell(M33, M.M33);
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
