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

import java.math.BigDecimal;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
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
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.composites.CompositeContainer;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.DatType;
import org.nschmidt.ldparteditor.enums.Colour;
import org.nschmidt.ldparteditor.enums.Font;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.widgets.BigDecimalSpinner;
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
    final BigDecimalSpinner[] spn_minor = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_size = new BigDecimalSpinner[1];
    
    final IntegerSpinner[] spn_divisions = new IntegerSpinner[1];
    final IntegerSpinner[] spn_segments = new IntegerSpinner[1];
    
    final StyledText[] txt_data = new StyledText[1];  
    final StyledText[] txt_data2 = new StyledText[1];   
    
    final Combo[] cmb_type = new Combo[1];
    final Combo[] cmb_divisions = new Combo[1];
    final Combo[] cmb_segments = new Combo[1];
    final Combo[] cmb_torusType = new Combo[1];
    final Combo[] cmb_winding = new Combo[1];
    
    final Button[] btn_saveAs = new Button[1];
    final Button[] btn_ok = new Button[1];
    final Button[] btn_cancel = new Button[1];
    
    final Label[] lbl_torusType = new Label[1];
    final Label[] lbl_size = new Label[1];
    final Label[] lbl_major = new Label[1];
    final Label[] lbl_minor = new Label[1];
    final Label[] lbl_standard = new Label[1];
    
    DatFile df;
    Composite3D c3d;
    
    PrimGen2Design(Shell parentShell) {
        super(parentShell);
    }

    /**
     * Create contents of the dialog.
     *
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite cmp_container = (Composite) super.createDialogArea(parent);
        cmp_container.setLayout(new GridLayout(9, true));
        GridLayout gridLayout = (GridLayout) cmp_container.getLayout();
        gridLayout.verticalSpacing = 10;
        gridLayout.horizontalSpacing = 10;

        Label lbl_specify = new Label(cmp_container, SWT.NONE);
        lbl_specify.setText("Primitive Generator 2.X (Different Conditional Line Control Points)"); //$NON-NLS-1$
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
        this.lbl_torusType[0] = lbl_torusType;
        lbl_torusType.setText("Torus Type"); //$NON-NLS-1$
        lbl_torusType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
        lbl_torusType.setEnabled(false);
        
        {
            Label lbl_dummy = new Label(cmp_container, SWT.NONE);
            lbl_dummy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));        
        }
        
        Combo cmb_type = new Combo(cmp_container, SWT.READ_ONLY);
        this.cmb_type[0] = cmb_type;        
        cmb_type.setItems(new String[]{"Circle", "Ring", "Cone", "Torus", "Cylinder", "Disc", "Disc Negative", "Chord"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        cmb_type.setText("Circle"); //$NON-NLS-1$
        cmb_type.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
        
        Combo cmb_divisions = new Combo(cmp_container, SWT.READ_ONLY);
        this.cmb_divisions[0] = cmb_divisions;        
        cmb_divisions.setItems(new String[]{"8", "16", "48", "Custom"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        cmb_divisions.setText("16"); //$NON-NLS-1$
        cmb_divisions.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        
        Combo cmb_segments = new Combo(cmp_container, SWT.READ_ONLY);
        this.cmb_segments[0] = cmb_segments;        
        cmb_segments.setItems(new String[]{"1-4", "2-4", "3-4", "4-4", "Custom"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        cmb_segments.setText("1-4"); //$NON-NLS-1$
        cmb_segments.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        
        Combo cmb_torusType = new Combo(cmp_container, SWT.READ_ONLY);
        this.cmb_torusType[0] = cmb_torusType;        
        cmb_torusType.setItems(new String[]{"Inside", "Outside", "Tube"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        cmb_torusType.setText("Outside"); //$NON-NLS-1$
        cmb_torusType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
        cmb_torusType.setEnabled(false);
        
        {
            Label lbl_dummy = new Label(cmp_container, SWT.NONE);
            lbl_dummy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));        
        }
   
        Label lbl_size = new Label(cmp_container, SWT.NONE);
        this.lbl_size[0] = lbl_size;
        lbl_size.setText("Size (radius)"); //$NON-NLS-1$        
        lbl_size.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
        lbl_size.setEnabled(false);
        
        IntegerSpinner spn_divisions = new IntegerSpinner(cmp_container, SWT.NONE);
        this.spn_divisions[0] = spn_divisions;
        spn_divisions.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spn_divisions.setMaximum(360);
        spn_divisions.setMinimum(1);
        spn_divisions.setValue(16);
        
        IntegerSpinner spn_segments = new IntegerSpinner(cmp_container, SWT.NONE);
        this.spn_segments[0] = spn_segments;
        spn_segments.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spn_segments.setMaximum(360);
        spn_segments.setMinimum(1);
        spn_segments.setValue(4);
        
        Label lbl_major = new Label(cmp_container, SWT.NONE);
        this.lbl_major[0] = lbl_major;
        lbl_major.setText("Major"); //$NON-NLS-1$
        lbl_major.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        lbl_major.setEnabled(false);
        
        Label lbl_minor = new Label(cmp_container, SWT.NONE);
        this.lbl_minor[0] = lbl_minor;
        lbl_minor.setText("Minor"); //$NON-NLS-1$
        lbl_minor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        lbl_minor.setEnabled(false);
    
        {
            Label lbl_dummy = new Label(cmp_container, SWT.NONE);
            lbl_dummy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));        
        }
        
        BigDecimalSpinner spn_size = new BigDecimalSpinner(cmp_container, SWT.NONE, View.NUMBER_FORMAT0F);
        this.spn_size[0] = spn_size;
        spn_size.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
        spn_size.setMaximum(new BigDecimal(1000));
        spn_size.setMinimum(new BigDecimal("0.0001")); //$NON-NLS-1$
        spn_size.setValue(BigDecimal.ONE);        
        spn_size.setEnabled(false);
        spn_size.setSmallIncrement(BigDecimal.ONE);
        spn_size.setLargeIncrement(BigDecimal.ONE);
        
        Label lbl_winding = new Label(cmp_container, SWT.NONE);
        lbl_winding.setText("Winding"); //$NON-NLS-1$
        lbl_winding.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
    
        Combo cmb_winding = new Combo(cmp_container, SWT.READ_ONLY);
        this.cmb_winding[0] = cmb_winding;        
        cmb_winding.setItems(new String[]{"CCW", "CW"}); //$NON-NLS-1$ //$NON-NLS-2$
        cmb_winding.setText("CCW"); //$NON-NLS-1$
        cmb_winding.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        IntegerSpinner spn_major = new IntegerSpinner(cmp_container, SWT.NONE);
        this.spn_major[0] = spn_major;
        spn_major.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spn_major.setMaximum(1000);
        spn_major.setMinimum(1);
        spn_major.setValue(1000);
        spn_major.setEnabled(false);
        
        BigDecimalSpinner spn_minor = new BigDecimalSpinner(cmp_container, SWT.NONE, View.NUMBER_FORMAT0F);
        this.spn_minor[0] = spn_minor;
        spn_minor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spn_minor.setMaximum(new BigDecimal(1000));
        spn_minor.setMinimum(BigDecimal.ONE);
        spn_minor.setValue(new BigDecimal(1000));
        spn_minor.setEnabled(false);
        spn_minor.setSmallIncrement(BigDecimal.ONE);
        spn_minor.setLargeIncrement(BigDecimal.ONE);
        
        {
            Label lbl_dummy = new Label(cmp_container, SWT.NONE);
            lbl_dummy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));        
        }
   
        {
            Label lbl_dummy = new Label(cmp_container, SWT.NONE);
            lbl_dummy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 6, 1));        
        }
   
        Button btn_saveAs = new Button(cmp_container, SWT.NONE);
        this.btn_saveAs[0] = btn_saveAs;
        btn_saveAs.setText("Save As..."); //$NON-NLS-1$
        btn_saveAs.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
        
        {
            Label lbl_dummy = new Label(cmp_container, SWT.NONE);
            lbl_dummy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));        
        }
        
        SashForm sashForm = new SashForm(cmp_container, SWT.NONE);
        sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 8, 30));
        
        df = new DatFile("...", "Temporary Primitive", true, DatType.PRIMITIVE); //$NON-NLS-1$ //$NON-NLS-2$
        Project.getUnsavedFiles().add(df);
        
        CompositeContainer cmp_Container = new CompositeContainer(sashForm, df);
        c3d = cmp_Container.getComposite3D();
        
        StyledText txt_data = new StyledText(sashForm, SWT.V_SCROLL | SWT.H_SCROLL);
        this.txt_data[0] = txt_data;
        txt_data.setText(""); //$NON-NLS-1$        
        txt_data.setBackground(Colour.text_background[0]);
        txt_data.setForeground(Colour.text_foreground[0]);
        txt_data.setFont(Font.MONOSPACE);
        txt_data.setLineSpacing(0);
        
        if (NLogger.DEBUG) {
            StyledText txt_data2 = new StyledText(sashForm, SWT.V_SCROLL | SWT.H_SCROLL);
            this.txt_data2[0] = txt_data2;
            txt_data2.setText(""); //$NON-NLS-1$        
            txt_data2.setBackground(Colour.text_background[0]);
            txt_data2.setForeground(Colour.text_foreground[0]);
            txt_data2.setFont(Font.MONOSPACE);
            txt_data2.setLineSpacing(0);
        }
        
        {
            Label lbl_dummy = new Label(cmp_container, SWT.NONE);
            lbl_dummy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));        
        }
        
        Label lbl_standard = new Label(cmp_container, SWT.NONE);
        this.lbl_standard[0] = lbl_standard;
        lbl_standard.setText("NON-STANDARD"); //$NON-NLS-1$
        lbl_standard.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 8, 1));
        
        cmp_container.layout();
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
        this.btn_ok[0] = createButton(parent, IDialogConstants.OK_ID, I18n.DIALOG_OK, true);
        this.btn_cancel[0] = createButton(parent, IDialogConstants.CANCEL_ID, I18n.DIALOG_Cancel, false);
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return super.getInitialSize();
    }
}
