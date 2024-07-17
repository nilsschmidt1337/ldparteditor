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
package org.nschmidt.ldparteditor.dialog.txt2dat;

import static org.nschmidt.ldparteditor.helper.WidgetUtility.widgetUtil;

import java.math.BigDecimal;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.nschmidt.ldparteditor.dialog.ThemedDialog;
import org.nschmidt.ldparteditor.enumtype.Font;
import org.nschmidt.ldparteditor.helper.composite3d.Txt2DatSettings;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.widget.BigDecimalSpinner;
import org.nschmidt.ldparteditor.widget.NButton;
import org.nschmidt.ldparteditor.workbench.Theming;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * The rounding precision dialog
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 */
class Txt2DatDesign extends ThemedDialog {

    final Txt2DatSettings ts;

    // Use final only for subclass/listener references!
    final NButton[] btnChooseFontPtr = new NButton[1];
    final BigDecimalSpinner[] spnFlatnessPtr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnMarginPercentagePtr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnFontHeightPtr = new BigDecimalSpinner[1];
    final Text[] txtTextPtr = new Text[1];
    final Combo[] cmbModePtr = new Combo[1];
    final NButton[] btnVerbosePtr = new NButton[1];

    Txt2DatDesign(Shell parentShell, Txt2DatSettings ts) {
        super(parentShell);
        this.ts = ts;
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
        gridLayout.verticalSpacing = 10;
        gridLayout.horizontalSpacing = 10;

        Label lblSpecify = Theming.label(cmpContainer, SWT.NONE);
        lblSpecify.setText(I18n.TXT2DAT_TITLE);

        Label lblSeparator = Theming.label(cmpContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
        lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label lblCoordsPrec = Theming.label(cmpContainer, SWT.NONE);
        lblCoordsPrec.setText(I18n.TXT2DAT_FONT + " [" + (ts.getFontData() == null ? Font.SYSTEM.getFontData()[0].getName() : ts.getFontData().getName()) + "]"); //$NON-NLS-1$ //$NON-NLS-2$

        NButton spnVequ = new NButton(cmpContainer, SWT.NONE);
        this.btnChooseFontPtr[0] = spnVequ;
        spnVequ.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spnVequ.setText(I18n.TXT2DAT_SELECT);

        Label lblText = Theming.label(cmpContainer, SWT.NONE);
        lblText.setText(I18n.TXT2DAT_TEXT);

        Text txtText = Theming.text(cmpContainer, SWT.NONE);
        this.txtTextPtr[0] = txtText;
        if (ts.getFontData() != null) txtText.setFont(SWTResourceManager.getFont(ts.getFontData().getName(), ts.getFontData().getHeight(), ts.getFontData().getStyle()));
        txtText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 4));
        txtText.setText(ts.getText());

        Label lblMode = Theming.label(cmpContainer, SWT.NONE);
        lblMode.setText(I18n.TXT2DAT_MODE);

        Combo cmbMode = Theming.combo(cmpContainer, SWT.READ_ONLY);
        this.cmbModePtr[0] = cmbMode;
        widgetUtil(cmbMode).setItems(I18n.TXT2DAT_SINGLE_LETTERS, I18n.TXT2DAT_ONLY_CHARACTERS, I18n.TXT2DAT_SINGLE_BACKGROUND);
        cmbMode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        cmbMode.setText(cmbMode.getItem(ts.getMode()));
        cmbMode.select(ts.getMode());

        Label lblUse180deg = Theming.label(cmpContainer, SWT.NONE);
        lblUse180deg.setText(I18n.TXT2DAT_FLATNESS);

        BigDecimalSpinner spnFlatness = new BigDecimalSpinner(cmpContainer, SWT.NONE);
        this.spnFlatnessPtr[0] = spnFlatness;
        spnFlatness.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spnFlatness.setMaximum(new BigDecimal(1000));
        spnFlatness.setMinimum(new BigDecimal(0));
        spnFlatness.setValue(ts.getFlatness());

        Label lblAf = Theming.label(cmpContainer, SWT.NONE);
        lblAf.setText(I18n.TXT2DAT_MARGIN);

        BigDecimalSpinner spnMarginPercentage = new BigDecimalSpinner(cmpContainer, SWT.NONE);
        this.spnMarginPercentagePtr[0] = spnMarginPercentage;
        spnMarginPercentage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spnMarginPercentage.setMaximum(new BigDecimal(100));
        spnMarginPercentage.setMinimum(new BigDecimal(0));
        spnMarginPercentage.setValue(ts.getMarginPercentage());

        Label lblAc = Theming.label(cmpContainer, SWT.NONE);
        lblAc.setText(I18n.TXT2DAT_FONT_HEIGHT);

        BigDecimalSpinner spnFontHeight = new BigDecimalSpinner(cmpContainer, SWT.NONE);
        this.spnFontHeightPtr[0] = spnFontHeight;
        spnFontHeight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spnFontHeight.setMaximum(new BigDecimal(10000));
        spnFontHeight.setMinimum(new BigDecimal("0.0001")); //$NON-NLS-1$
        spnFontHeight.setValue(ts.getFontHeight());

        NButton btnVerbose = new NButton(cmpContainer, SWT.CHECK);
        this.btnVerbosePtr[0] = btnVerbose;
        btnVerbose.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnVerbose.setText(I18n.TXT2DAT_VERBOSE);
        btnVerbose.setSelection(WorkbenchManager.getUserSettingState().isVerboseTxt2Dat());

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
        createButton(parent, IDialogConstants.OK_ID, I18n.DIALOG_OK, true);
        createButton(parent, IDialogConstants.CANCEL_ID, I18n.DIALOG_CANCEL, false);
    }
}
