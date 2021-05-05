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
package org.nschmidt.ldparteditor.dialogs.rectifier;

import static org.nschmidt.ldparteditor.helpers.WidgetUtility.widgetUtil;

import java.math.BigDecimal;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.helpers.composite3d.RectifierSettings;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.widgets.BigDecimalSpinner;
import org.nschmidt.ldparteditor.widgets.NButton;

/**
 * The rectifier dialog
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 *
 * @author nils
 *
 */
class RectifierDesign extends Dialog {

    final RectifierSettings rs;
    final BigDecimalSpinner[] spnAnglePtr = new BigDecimalSpinner[1];
    final Combo[] cmbScopePtr = new Combo[1];
    final NButton[] btnVerbosePtr = new NButton[1];

    final Combo[] cmbColourisePtr = new Combo[1];
    final Combo[] cmbNoQuadConversationPtr = new Combo[1];
    final Combo[] cmbNoRectConversationOnAdjacentCondlinesPtr = new Combo[1];
    final Combo[] cmbNoBorderedQuadToRectConversationPtr = new Combo[1];

    // Use final only for subclass/listener references!

    RectifierDesign(Shell parentShell, RectifierSettings rs) {
        super(parentShell);
        this.rs = rs;
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

        Label lblSpecify = new Label(cmpContainer, SWT.NONE);
        lblSpecify.setText(I18n.RECTIFIER_TITLE);

        Label lblSeparator = new Label(cmpContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
        lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label lblAngle = new Label(cmpContainer, SWT.NONE);
        lblAngle.setText(I18n.RECTIFIER_MAX_ANGLE);

        BigDecimalSpinner spnAngle = new BigDecimalSpinner(cmpContainer, SWT.NONE);
        this.spnAnglePtr [0] = spnAngle;
        spnAngle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spnAngle.setMaximum(new BigDecimal(90));
        spnAngle.setMinimum(new BigDecimal(0));
        spnAngle.setValue(rs.getMaximumAngle());

        {
            Combo cmbColourise = new Combo(cmpContainer, SWT.READ_ONLY);
            this.cmbColourisePtr[0] = cmbColourise;
            widgetUtil(cmbColourise).setItems(I18n.RECTIFIER_COLOUR_1, I18n.RECTIFIER_COLOUR_2);
            cmbColourise.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmbColourise.setText(cmbColourise.getItem(rs.isColourise() ? 1 : 0));
            cmbColourise.select(rs.isColourise() ? 1 : 0);
        }
        {
            Combo cmbNoQuadConversation = new Combo(cmpContainer, SWT.READ_ONLY);
            this.cmbNoQuadConversationPtr[0] = cmbNoQuadConversation;
            widgetUtil(cmbNoQuadConversation).setItems(I18n.RECTIFIER_TRI_QUADS_1, I18n.RECTIFIER_TRI_QUADS_2);
            cmbNoQuadConversation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmbNoQuadConversation.setText(cmbNoQuadConversation.getItem(rs.isNoQuadConversation() ? 1 : 0));
            cmbNoQuadConversation.select(rs.isNoQuadConversation() ? 1 : 0);
        }
        {
            Combo cmbNoBorderedQuadToRectConversation = new Combo(cmpContainer, SWT.READ_ONLY);
            this.cmbNoBorderedQuadToRectConversationPtr[0] = cmbNoBorderedQuadToRectConversation;
            widgetUtil(cmbNoBorderedQuadToRectConversation).setItems(I18n.RECTIFIER_RECT_1, I18n.RECTIFIER_RECT_2);
            cmbNoBorderedQuadToRectConversation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmbNoBorderedQuadToRectConversation.setText(cmbNoBorderedQuadToRectConversation.getItem(rs.isNoBorderedQuadToRectConversation() ? 1 : 0));
            cmbNoBorderedQuadToRectConversation.select(rs.isNoBorderedQuadToRectConversation() ? 1 : 0);
        }
        {
            Combo cmbNoRectConversationOnAdjacentCondlines = new Combo(cmpContainer, SWT.READ_ONLY);
            this.cmbNoRectConversationOnAdjacentCondlinesPtr[0] = cmbNoRectConversationOnAdjacentCondlines;
            widgetUtil(cmbNoRectConversationOnAdjacentCondlines).setItems(I18n.RECTIFIER_RECT_3, I18n.RECTIFIER_RECT_4);
            cmbNoRectConversationOnAdjacentCondlines.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            cmbNoRectConversationOnAdjacentCondlines.setText(cmbNoRectConversationOnAdjacentCondlines.getItem(rs.isNoRectConversationOnAdjacentCondlines() ? 1 : 0));
            cmbNoRectConversationOnAdjacentCondlines.select(rs.isNoRectConversationOnAdjacentCondlines() ? 1 : 0);
        }
        Combo cmbScope = new Combo(cmpContainer, SWT.READ_ONLY);
        this.cmbScopePtr[0] = cmbScope;
        widgetUtil(cmbScope).setItems(I18n.RECTIFIER_SCOPE_FILE, I18n.RECTIFIER_SCOPE_SELECTION);
        cmbScope.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        cmbScope.setText(cmbScope.getItem(rs.getScope()));
        cmbScope.select(rs.getScope());

        NButton btnVerbose = new NButton(cmpContainer, SWT.CHECK);
        this.btnVerbosePtr[0] = btnVerbose;
        btnVerbose.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnVerbose.setText(I18n.RECTIFIER_VERBOSE);
        btnVerbose.setSelection(rs.isVerbose());

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
