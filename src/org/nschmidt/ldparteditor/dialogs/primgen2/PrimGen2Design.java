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
package org.nschmidt.ldparteditor.dialogs.primgen2;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.enums.Colour;
import org.nschmidt.ldparteditor.enums.Font;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.widgets.IntegerSpinner;

/**
 * The PrimGen2 dialog
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 *
 * @author nils
 *
 */
class PrimGen2Design extends Dialog {
    
    // FIXME Needs implementation (GUI)!

    // Use final only for subclass/listener references!
    final IntegerSpinner[] spn_major = new IntegerSpinner[1];
    final IntegerSpinner[] spn_minor = new IntegerSpinner[1];
    
    
    PrimGen2Design(Shell parentShell) {
        super(parentShell);
        // setShellStyle(SWT.RESIZE);
    }

    /**
     * Create contents of the dialog.
     *
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite cmp_container = (Composite) super.createDialogArea(parent);
        cmp_container.setLayout(new GridLayout(8, true));
        GridLayout gridLayout = (GridLayout) cmp_container.getLayout();
        gridLayout.verticalSpacing = 10;
        gridLayout.horizontalSpacing = 10;

        Label lbl_specify = new Label(cmp_container, SWT.NONE);
        lbl_specify.setText("Primitive Generator 2"); //$NON-NLS-1$
        lbl_specify.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 8, 1));
        
        Label lbl_separator = new Label(cmp_container, SWT.SEPARATOR | SWT.HORIZONTAL);
        lbl_separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 8, 1));

        Label lbl_type = new Label(cmp_container, SWT.NONE);
        lbl_type.setText("Type"); //$NON-NLS-1$
        lbl_type.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
        
        Label lbl_divisions = new Label(cmp_container, SWT.NONE);
        lbl_divisions.setText("Divisions"); //$NON-NLS-1$
        lbl_divisions.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        
        Label lbl_segments = new Label(cmp_container, SWT.NONE);
        lbl_segments.setText("Segments"); //$NON-NLS-1$
        lbl_segments.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        
        Label lbl_torusType = new Label(cmp_container, SWT.NONE);
        lbl_torusType.setText("Torus Type"); //$NON-NLS-1$
        lbl_torusType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

        {
            Label lbl_dummy = new Label(cmp_container, SWT.NONE);
            lbl_dummy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));        
        }
        
        Combo cmb_type = new Combo(cmp_container, SWT.NONE);
        cmb_type.setText("Circle"); //$NON-NLS-1$
        cmb_type.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
        
        Combo cmb_divisions = new Combo(cmp_container, SWT.NONE);
        cmb_divisions.setText("16"); //$NON-NLS-1$
        cmb_divisions.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        
        Combo cmb_segments = new Combo(cmp_container, SWT.NONE);
        cmb_segments.setText("1-4"); //$NON-NLS-1$
        cmb_segments.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        
        Combo cmb_torusType = new Combo(cmp_container, SWT.NONE);
        cmb_torusType.setText("Inside"); //$NON-NLS-1$
        cmb_torusType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
        cmb_torusType.setEnabled(false);
        
        {
            Label lbl_dummy = new Label(cmp_container, SWT.NONE);
            lbl_dummy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));        
        }
   
        Label lbl_size = new Label(cmp_container, SWT.NONE);
        lbl_size.setText("Size (radius)"); //$NON-NLS-1$
        lbl_size.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
    
        IntegerSpinner spn_divisions = new IntegerSpinner(cmp_container, SWT.NONE);
        spn_divisions.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spn_divisions.setMaximum(360);
        spn_divisions.setMinimum(1);
        spn_divisions.setValue(16);
        
        IntegerSpinner spn_segments = new IntegerSpinner(cmp_container, SWT.NONE);
        spn_segments.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spn_segments.setMaximum(360);
        spn_segments.setMinimum(1);
        spn_segments.setValue(4);
        
        Label lbl_major = new Label(cmp_container, SWT.NONE);
        lbl_major.setText("Major"); //$NON-NLS-1$
        lbl_major.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        
        Label lbl_minor = new Label(cmp_container, SWT.NONE);
        lbl_minor.setText("Minor"); //$NON-NLS-1$
        lbl_minor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
    
        {
            Label lbl_dummy = new Label(cmp_container, SWT.NONE);
            lbl_dummy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));        
        }
        
        IntegerSpinner spn_size = new IntegerSpinner(cmp_container, SWT.NONE);
        spn_size.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
        spn_size.setMaximum(360);
        spn_size.setMinimum(1);
        spn_size.setValue(1);
        spn_size.setEnabled(false);
        
        Label lbl_winding = new Label(cmp_container, SWT.NONE);
        lbl_winding.setText("Winding"); //$NON-NLS-1$
        lbl_winding.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
    
        Combo cmb_winding = new Combo(cmp_container, SWT.NONE);
        cmb_winding.setText("CCW"); //$NON-NLS-1$
        cmb_winding.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        IntegerSpinner spn_major = new IntegerSpinner(cmp_container, SWT.NONE);
        this.spn_major[0] = spn_major;
        spn_major.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spn_major.setMaximum(1000);
        spn_major.setMinimum(1);
        spn_major.setValue(1000);
        spn_major.setEnabled(false);
        
        IntegerSpinner spn_minor = new IntegerSpinner(cmp_container, SWT.NONE);
        this.spn_minor[0] = spn_minor;
        spn_minor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spn_minor.setMaximum(1000);
        spn_minor.setMinimum(1);
        spn_minor.setValue(1000);
        spn_minor.setEnabled(false);
        
        {
            Label lbl_dummy = new Label(cmp_container, SWT.NONE);
            lbl_dummy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));        
        }
   
        Button btn_view = new Button(cmp_container, SWT.NONE);
        btn_view.setText("View"); //$NON-NLS-1$
        btn_view.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
        
        {
            Label lbl_dummy = new Label(cmp_container, SWT.NONE);
            lbl_dummy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));        
        }
   
        Button btn_saveAs = new Button(cmp_container, SWT.NONE);
        btn_saveAs.setText("Save As..."); //$NON-NLS-1$
        btn_saveAs.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
        
        StyledText txt_data = new StyledText(cmp_container, SWT.V_SCROLL | SWT.H_SCROLL);
        txt_data.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 10, 30));
        txt_data.setText(""); //$NON-NLS-1$        
        txt_data.setBackground(Colour.text_background[0]);
        txt_data.setForeground(Colour.text_foreground[0]);
        txt_data.setFont(Font.MONOSPACE);
        txt_data.setLineSpacing(0);
        
        Label lbl_standard = new Label(cmp_container, SWT.NONE);
        lbl_standard.setText("STANDARD"); //$NON-NLS-1$
        lbl_standard.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 8, 1));
        
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
