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
package org.nschmidt.ldparteditor.dialogs.copy;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.widgets.NButton;

/**
 *
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 *
 * @author nils
 *
 */
class CopyDesign extends Dialog {

    // Use final only for subclass/listener references!

    final NButton[] btn1Ptr = new NButton[1];
    final NButton[] btn2Ptr = new NButton[1];
    final NButton[] btn3Ptr = new NButton[1];

    private final String fileName;

    CopyDesign(Shell parentShell, String fileName) {
        super(parentShell);
        this.fileName = fileName;
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

        NButton btn1 = new NButton(cmpContainer, SWT.NONE);
        this.btn1Ptr[0] = btn1;
        btn1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        {
            Object[] messageArguments = {fileName};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.locale);
            formatter.applyPattern(I18n.DIALOG_COPY_FILE_ONLY);
            btn1.setText(formatter.format(messageArguments));
        }

        NButton btn2 = new NButton(cmpContainer, SWT.NONE);
        this.btn2Ptr[0] = btn2;
        btn2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        {
            Object[] messageArguments = {fileName};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.locale);
            formatter.applyPattern(I18n.DIALOG_COPY_FILE_AND_REQUIRED);
            btn2.setText(formatter.format(messageArguments));
        }

        NButton btn3 = new NButton(cmpContainer, SWT.NONE);
        this.btn3Ptr[0] = btn3;
        btn3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        {
            Object[] messageArguments = {fileName};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.locale);
            formatter.applyPattern(I18n.DIALOG_COPY_FILE_AND_REQUIRED_AND_RELATED);
            btn3.setText(formatter.format(messageArguments));
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
        createButton(parent, IDialogConstants.CANCEL_ID, I18n.DIALOG_CANCEL, true);
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return super.getInitialSize();
    }

}
