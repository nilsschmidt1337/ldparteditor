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
package org.nschmidt.ldparteditor.dialogs.rotate;

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
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.resources.ResourceManager;
import org.nschmidt.ldparteditor.widgets.BigDecimalSpinner;

/**
 * The rotate dialog
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 *
 * @author nils
 *
 */
class RotateDesign extends Dialog {


    final Button[] rb_Xaxis = new Button[1];
    final Button[] rb_Yaxis = new Button[1];
    final Button[] rb_Zaxis = new Button[1];
    final BigDecimalSpinner[] spn_X = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_Y = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_Z = new BigDecimalSpinner[1];
    final Button[] btn_PivotClipboard = new Button[1];
    final Button[] btn_PivotManipulator = new Button[1];
    final BigDecimalSpinner[] spn_pX = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_pY = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_pZ = new BigDecimalSpinner[1];

    // Use final only for subclass/listener references!

    Vertex v = new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    Vertex p = new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    Vertex m = new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    Vertex c = new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    RotateDesign(Shell parentShell, Vertex v, Set<Vertex> clipboardVertices, Vertex manipulatorPosition) {
        super(parentShell);
        if (manipulatorPosition != null) {
            m = manipulatorPosition;
        }
        if (v == null) {
            this.v = new Vertex(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        } else {
            this.v = v;
        }
        if (clipboardVertices.size() == 1) {
            p = clipboardVertices.iterator().next();
            c = new Vertex(p.X, p.Y, p.Z);
        }
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
        lbl_specify.setText(I18n.ROTATE_Title);

        Label lbl_separator = new Label(cmp_container, SWT.SEPARATOR | SWT.HORIZONTAL);
        lbl_separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        {
            Composite cmp_txt = new Composite(cmp_container, SWT.NONE);
            cmp_txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmp_txt.setLayout(new GridLayout(6, true));

            Button cb_Xaxis = new Button(cmp_txt, SWT.RADIO);
            this.rb_Xaxis[0] = cb_Xaxis;
            cb_Xaxis.setText(I18n.ROTATE_X);
            cb_Xaxis.setSelection(true);

            BigDecimalSpinner spn_X = new BigDecimalSpinner(cmp_txt, SWT.NONE);
            this.spn_X[0] = spn_X;
            spn_X.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
            spn_X.setMaximum(new BigDecimal(1000000));
            spn_X.setMinimum(new BigDecimal(-1000000));
            spn_X.setValue(v.X);

            Button cb_Yaxis = new Button(cmp_txt, SWT.RADIO);
            this.rb_Yaxis[0] = cb_Yaxis;
            cb_Yaxis.setText(I18n.ROTATE_Y);

            BigDecimalSpinner spn_Y = new BigDecimalSpinner(cmp_txt, SWT.NONE);
            this.spn_Y[0] = spn_Y;
            spn_Y.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
            spn_Y.setMaximum(new BigDecimal(1000000));
            spn_Y.setMinimum(new BigDecimal(-1000000));
            spn_Y.setValue(v.Y);

            Button cb_Zaxis = new Button(cmp_txt, SWT.RADIO);
            this.rb_Zaxis[0] = cb_Zaxis;
            cb_Zaxis.setText(I18n.ROTATE_Z);

            BigDecimalSpinner spn_Z = new BigDecimalSpinner(cmp_txt, SWT.NONE);
            this.spn_Z[0] = spn_Z;
            spn_Z.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
            spn_Z.setMaximum(new BigDecimal(1000000));
            spn_Z.setMinimum(new BigDecimal(-1000000));
            spn_Z.setValue(v.Z);
        }

        Label lbl_Pivot = new Label(cmp_container, SWT.NONE);
        lbl_Pivot.setText(I18n.ROTATE_Pivot);

        {
            Composite cmp_txt = new Composite(cmp_container, SWT.NONE);
            cmp_txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmp_txt.setLayout(new GridLayout(1, true));
            Button btn_PivotManipulator = new Button(cmp_txt, SWT.NONE);
            this.btn_PivotManipulator[0] = btn_PivotManipulator;
            btn_PivotManipulator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            btn_PivotManipulator.setImage(ResourceManager.getImage("icon8_local.png")); //$NON-NLS-1$
            btn_PivotManipulator.setText(I18n.ROTATE_PivotManipulator);
        }
        {
            Composite cmp_txt = new Composite(cmp_container, SWT.NONE);
            cmp_txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmp_txt.setLayout(new GridLayout(1, true));
            Button btn_PivotClipboard = new Button(cmp_txt, SWT.NONE);
            this.btn_PivotClipboard[0] = btn_PivotClipboard;
            btn_PivotClipboard.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            btn_PivotClipboard.setImage(ResourceManager.getImage("icon8_edit-paste.png")); //$NON-NLS-1$
            btn_PivotClipboard.setText(I18n.ROTATE_PivotClipboard);
        }

        {
            Composite cmp_txt = new Composite(cmp_container, SWT.NONE);
            cmp_txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmp_txt.setLayout(new GridLayout(1, true));

            BigDecimalSpinner spn_pX = new BigDecimalSpinner(cmp_txt, SWT.NONE);
            this.spn_pX[0] = spn_pX;
            spn_pX.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            spn_pX.setMaximum(new BigDecimal(1000000));
            spn_pX.setMinimum(new BigDecimal(-1000000));
            spn_pX.setValue(p.X);
        }


        {
            Composite cmp_txt = new Composite(cmp_container, SWT.NONE);
            cmp_txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmp_txt.setLayout(new GridLayout(1, true));

            BigDecimalSpinner spn_pY = new BigDecimalSpinner(cmp_txt, SWT.NONE);
            this.spn_pY[0] = spn_pY;
            spn_pY.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            spn_pY.setMaximum(new BigDecimal(1000000));
            spn_pY.setMinimum(new BigDecimal(-1000000));
            spn_pY.setValue(p.Y);
        }

        {
            Composite cmp_txt = new Composite(cmp_container, SWT.NONE);
            cmp_txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmp_txt.setLayout(new GridLayout(1, true));

            BigDecimalSpinner spn_pZ = new BigDecimalSpinner(cmp_txt, SWT.NONE);
            this.spn_pZ[0] = spn_pZ;
            spn_pZ.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            spn_pZ.setMaximum(new BigDecimal(1000000));
            spn_pZ.setMinimum(new BigDecimal(-1000000));
            spn_pZ.setValue(p.Z);
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
