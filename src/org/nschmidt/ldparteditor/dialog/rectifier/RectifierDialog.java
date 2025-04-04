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
package org.nschmidt.ldparteditor.dialog.rectifier;

import static org.nschmidt.ldparteditor.helper.WidgetUtility.widgetUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.helper.composite3d.RectifierSettings;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 *
 * <p>
 * Note: This class should be instantiated, it defines all listeners and part of
 * the business logic. It overrides the {@code open()} method to invoke the
 * listener definitions ;)
 */
public class RectifierDialog extends RectifierDesign {

    /**
     * Create the dialog.
     *
     * @param parentShell
     */
    public RectifierDialog(Shell parentShell, RectifierSettings rs) {
        super(parentShell, rs);
    }

    @Override
    public int open() {
        super.create();
        // MARK All final listeners will be configured here..
        spnAnglePtr[0].addValueChangeListener(spn -> rs.setMaximumAngle(spn.getValue()));
        cmbScopePtr[0].addListener(SWT.Selection, event -> rs.setScope(cmbScopePtr[0].getSelectionIndex()));
        cmbColourisePtr[0].addListener(SWT.Selection, event -> rs.setColourise(cmbColourisePtr[0].getSelectionIndex() == 1));
        cmbNoBorderedQuadToRectConversationPtr[0].addListener(SWT.Selection, event -> rs.setNoBorderedQuadToRectConversation(cmbNoBorderedQuadToRectConversationPtr[0].getSelectionIndex() == 1));
        cmbNoQuadConversationPtr[0].addListener(SWT.Selection, event -> rs.setNoQuadConversation(cmbNoQuadConversationPtr[0].getSelectionIndex() == 1));
        cmbNoRectConversationOnAdjacentCondlinesPtr[0].addListener(SWT.Selection, event -> rs.setNoRectConversationOnAdjacentCondlines(cmbNoRectConversationOnAdjacentCondlinesPtr[0].getSelectionIndex() == 1));
        cmbNoWarpedRectWithShearPtr[0].addListener(SWT.Selection, event -> rs.setNoDecimalsInRectPrims(cmbNoWarpedRectWithShearPtr[0].getSelectionIndex() == 1));
        widgetUtil(btnVerbosePtr[0]).addSelectionListener(e -> WorkbenchManager.getUserSettingState().setVerboseRectifier(btnVerbosePtr[0].getSelection()));
        return super.open();
    }
}
