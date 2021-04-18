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
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
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
import org.nschmidt.ldparteditor.resources.ResourceManager;
import org.nschmidt.ldparteditor.widgets.BigDecimalSpinner;
import org.nschmidt.ldparteditor.widgets.IntegerSpinner;
import org.nschmidt.ldparteditor.widgets.NButton;

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

    // Use final only for subclass/listener references!

    /** The menu of this composite */
    private final Menu[] menu = new Menu[1];

    final MenuItem[] mntmCopyPtr = new MenuItem[1];
    final MenuItem[] mntmCutPtr = new MenuItem[1];
    final MenuItem[] mntmDeletePtr = new MenuItem[1];
    final MenuItem[] mntmPastePtr = new MenuItem[1];

    final IntegerSpinner[] spnMajorPtr = new IntegerSpinner[1];
    final BigDecimalSpinner[] spnMinorPtr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnSizePtr = new BigDecimalSpinner[1];

    final IntegerSpinner[] spnDivisionsPtr = new IntegerSpinner[1];
    final IntegerSpinner[] spnSegmentsPtr = new IntegerSpinner[1];

    final StyledText[] txtDataPtr = new StyledText[1];
    final StyledText[] txtData2Ptr = new StyledText[1];

    final Combo[] cmbTypePtr = new Combo[1];
    final Combo[] cmbDivisionsPtr = new Combo[1];
    final Combo[] cmbSegmentsPtr = new Combo[1];
    final Combo[] cmbTorusTypePtr = new Combo[1];
    final Combo[] cmbWindingPtr = new Combo[1];

    final NButton[] btnSaveAsPtr = new NButton[1];
    final Button[] btnOkPtr = new Button[1];
    final Button[] btnCancelPtr = new Button[1];

    final NButton[] btnTopPtr = new NButton[1];

    final Label[] lblTorusTypePtr = new Label[1];
    final Label[] lblSizePtr = new Label[1];
    final Label[] lblMajorPtr = new Label[1];
    final Label[] lblMinorPtr = new Label[1];
    final Label[] lblStandardPtr = new Label[1];

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
        Composite cmpContainer = (Composite) super.createDialogArea(parent);
        cmpContainer.setLayout(new GridLayout(9, true));
        GridLayout gridLayout = (GridLayout) cmpContainer.getLayout();
        gridLayout.verticalSpacing = 10;
        gridLayout.horizontalSpacing = 10;

        Label lblSpecify = new Label(cmpContainer, SWT.NONE);
        lblSpecify.setText(I18n.PRIMGEN_TITLE);
        lblSpecify.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 8, 1));

        Label lblSeparator = new Label(cmpContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
        lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 8, 1));

        Label lblType = new Label(cmpContainer, SWT.NONE);
        lblType.setText(I18n.PRIMGEN_TYPE);
        lblType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

        Label lblDivisions = new Label(cmpContainer, SWT.NONE);
        lblDivisions.setText(I18n.PRIMGEN_DIVISIONS);
        lblDivisions.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label lblSegments = new Label(cmpContainer, SWT.NONE);
        lblSegments.setText(I18n.PRIMGEN_SEGMENTS);
        lblSegments.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label lblTorusType = new Label(cmpContainer, SWT.NONE);
        this.lblTorusTypePtr[0] = lblTorusType;
        lblTorusType.setText(I18n.PRIMGEN_TORUS_TYPE);
        lblTorusType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
        lblTorusType.setEnabled(false);

        {
            Label lblDummy = new Label(cmpContainer, SWT.NONE);
            lblDummy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
        }

        Combo cmbType = new Combo(cmpContainer, SWT.READ_ONLY);
        this.cmbTypePtr[0] = cmbType;
        cmbType.setItems(I18n.PRIMGEN_CIRCLE, I18n.PRIMGEN_RING, I18n.PRIMGEN_CONE, I18n.PRIMGEN_TORUS, I18n.PRIMGEN_CYLINDER, I18n.PRIMGEN_DISC, I18n.PRIMGEN_DISC_NEGATIVE, I18n.PRIMGEN_CHORD);
        cmbType.setText(I18n.PRIMGEN_CIRCLE);
        cmbType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

        Combo cmbDivisions = new Combo(cmpContainer, SWT.READ_ONLY);
        this.cmbDivisionsPtr[0] = cmbDivisions;
        cmbDivisions.setItems(I18n.PRIMGEN_EIGHT, I18n.PRIMGEN_SIXTEEN, I18n.PRIMGEN_FOURTYEIGHT, I18n.PRIMGEN_CUSTOM);
        cmbDivisions.setText(I18n.PRIMGEN_SIXTEEN);
        cmbDivisions.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Combo cmbSegments = new Combo(cmpContainer, SWT.READ_ONLY);
        this.cmbSegmentsPtr[0] = cmbSegments;
        cmbSegments.setItems(I18n.PRIMGEN_QUARTER, I18n.PRIMGEN_HALF, I18n.PRIMGEN_THREE_QUARTER, I18n.PRIMGEN_WHOLE, I18n.PRIMGEN_CUSTOM);
        cmbSegments.setText(I18n.PRIMGEN_QUARTER);
        cmbSegments.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Combo cmbTorusType = new Combo(cmpContainer, SWT.READ_ONLY);
        this.cmbTorusTypePtr[0] = cmbTorusType;
        cmbTorusType.setItems(I18n.PRIMGEN_INSIDE, I18n.PRIMGEN_OUTSIDE, I18n.PRIMGEN_TUBE);
        cmbTorusType.setText(I18n.PRIMGEN_OUTSIDE);
        cmbTorusType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
        cmbTorusType.setEnabled(false);

        {
            Label lblDummy = new Label(cmpContainer, SWT.NONE);
            lblDummy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
        }

        Label lblSize = new Label(cmpContainer, SWT.NONE);
        this.lblSizePtr[0] = lblSize;
        lblSize.setText(I18n.PRIMGEN_SIZE);
        lblSize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
        lblSize.setEnabled(false);

        IntegerSpinner spnDivisions = new IntegerSpinner(cmpContainer, SWT.NONE);
        this.spnDivisionsPtr[0] = spnDivisions;
        spnDivisions.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spnDivisions.setMaximum(360);
        spnDivisions.setMinimum(1);
        spnDivisions.setValue(16);

        IntegerSpinner spnSegments = new IntegerSpinner(cmpContainer, SWT.NONE);
        this.spnSegmentsPtr[0] = spnSegments;
        spnSegments.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spnSegments.setMaximum(360);
        spnSegments.setMinimum(1);
        spnSegments.setValue(4);

        Label lblMajor = new Label(cmpContainer, SWT.NONE);
        this.lblMajorPtr[0] = lblMajor;
        lblMajor.setText(I18n.PRIMGEN_MAJOR);
        lblMajor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        lblMajor.setEnabled(false);

        Label lblMinor = new Label(cmpContainer, SWT.NONE);
        this.lblMinorPtr[0] = lblMinor;
        lblMinor.setText(I18n.PRIMGEN_MINOR);
        lblMinor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        lblMinor.setEnabled(false);

        {
            Label lblDummy = new Label(cmpContainer, SWT.NONE);
            lblDummy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
        }

        BigDecimalSpinner spnSize = new BigDecimalSpinner(cmpContainer, SWT.NONE, View.NUMBER_FORMAT0F);
        this.spnSizePtr[0] = spnSize;
        spnSize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
        spnSize.setMaximum(new BigDecimal(1000));
        spnSize.setMinimum(new BigDecimal("0.0001")); //$NON-NLS-1$
        spnSize.setValue(BigDecimal.ONE);
        spnSize.setEnabled(false);
        spnSize.setSmallIncrement(BigDecimal.ONE);
        spnSize.setLargeIncrement(BigDecimal.ONE);

        Label lblWinding = new Label(cmpContainer, SWT.NONE);
        lblWinding.setText(I18n.PRIMGEN_WINDING);
        lblWinding.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Combo cmbWinding = new Combo(cmpContainer, SWT.READ_ONLY);
        this.cmbWindingPtr[0] = cmbWinding;
        cmbWinding.setItems(I18n.PRIMGEN_CCW, I18n.PRIMGEN_CW);
        cmbWinding.setText(I18n.PRIMGEN_CCW);
        cmbWinding.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        IntegerSpinner spnMajor = new IntegerSpinner(cmpContainer, SWT.NONE);
        this.spnMajorPtr[0] = spnMajor;
        spnMajor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spnMajor.setMaximum(1000);
        spnMajor.setMinimum(1);
        spnMajor.setValue(1000);
        spnMajor.setEnabled(false);

        BigDecimalSpinner spnMinor = new BigDecimalSpinner(cmpContainer, SWT.NONE, View.NUMBER_FORMAT0F);
        this.spnMinorPtr[0] = spnMinor;
        spnMinor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        spnMinor.setMaximum(new BigDecimal(1000));
        spnMinor.setMinimum(BigDecimal.ONE);
        spnMinor.setValue(new BigDecimal(1000));
        spnMinor.setEnabled(false);
        spnMinor.setSmallIncrement(BigDecimal.ONE);
        spnMinor.setLargeIncrement(BigDecimal.ONE);

        {
            Label lblDummy = new Label(cmpContainer, SWT.NONE);
            lblDummy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
        }

        NButton btnTop = new NButton(cmpContainer, SWT.NONE);
        this.btnTopPtr[0] = btnTop;
        btnTop.setText(I18n.PERSPECTIVE_TOP);
        btnTop.setImage(ResourceManager.getImage("icon16_top.png")); //$NON-NLS-1$
        btnTop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        {
            Label lblDummy = new Label(cmpContainer, SWT.NONE);
            lblDummy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 6, 1));
        }

        NButton btnSaveAs = new NButton(cmpContainer, SWT.NONE);
        this.btnSaveAsPtr[0] = btnSaveAs;
        btnSaveAs.setText(I18n.PRIMGEN_SAVE_AS);
        btnSaveAs.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

        {
            Label lblDummy = new Label(cmpContainer, SWT.NONE);
            lblDummy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        }

        SashForm sashForm = new SashForm(cmpContainer, SWT.NONE);
        sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 10, 30));

        df = new DatFile("...", "Temporary Primitive", true, DatType.PRIMITIVE); //$NON-NLS-1$ //$NON-NLS-2$
        Project.getUnsavedFiles().add(df);

        CompositeContainer compositeContainer = new CompositeContainer(sashForm, df);
        c3d = compositeContainer.getComposite3D();

        StyledText txtData = new StyledText(sashForm, SWT.V_SCROLL | SWT.H_SCROLL);
        this.txtDataPtr[0] = txtData;
        txtData.setText(""); //$NON-NLS-1$
        txtData.setBackground(Colour.textBackground[0]);
        txtData.setForeground(Colour.textForeground[0]);
        txtData.setFont(Font.MONOSPACE);
        txtData.setLineSpacing(0);

        if (NLogger.debugging) {
            StyledText txtData2 = new StyledText(sashForm, SWT.V_SCROLL | SWT.H_SCROLL);
            this.txtData2Ptr[0] = txtData2;
            txtData2.setText(""); //$NON-NLS-1$
            txtData2.setBackground(Colour.textBackground[0]);
            txtData2.setForeground(Colour.textForeground[0]);
            txtData2.setFont(Font.MONOSPACE);
            txtData2.setLineSpacing(0);
        } else {
            sashForm.setWeights(new int[]{33, 66});
        }

        Label lblStandard = new Label(cmpContainer, SWT.NONE);
        this.lblStandardPtr[0] = lblStandard;
        lblStandard.setText(I18n.PRIMGEN_NON_STANDARD);
        lblStandard.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 8, 1));

        this.menu[0] = new Menu(txtData);
        txtData.setMenu(this.menu[0]);

        MenuItem mntmCut = new MenuItem(menu[0], I18n.rightToLeftStyle());
        mntmCut.setText(I18n.COPYNPASTE_CUT);
        mntmCut.setImage(ResourceManager.getImage("icon16_edit-cut.png")); //$NON-NLS-1$
        mntmCutPtr[0] = mntmCut;
        MenuItem mntmCopy = new MenuItem(menu[0], I18n.rightToLeftStyle());
        mntmCopy.setText(I18n.COPYNPASTE_COPY);
        mntmCopy.setImage(ResourceManager.getImage("icon16_edit-copy.png")); //$NON-NLS-1$
        mntmCopyPtr[0] = mntmCopy;
        MenuItem mntmPaste = new MenuItem(menu[0], I18n.rightToLeftStyle());
        mntmPaste.setText(I18n.COPYNPASTE_PASTE);
        mntmPaste.setImage(ResourceManager.getImage("icon16_edit-paste.png")); //$NON-NLS-1$
        mntmPastePtr[0] = mntmPaste;
        MenuItem mntmDelete = new MenuItem(menu[0], I18n.rightToLeftStyle());
        mntmDelete.setText(I18n.COPYNPASTE_DELETE);
        mntmDelete.setImage(ResourceManager.getImage("icon16_delete.png")); //$NON-NLS-1$
        mntmDeletePtr[0] = mntmDelete;

        cmpContainer.layout();
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
        this.btnOkPtr[0] = createButton(parent, IDialogConstants.OK_ID, I18n.DIALOG_OK, true);
        this.btnCancelPtr[0] = createButton(parent, IDialogConstants.CANCEL_ID, I18n.DIALOG_CANCEL, false);
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return super.getInitialSize();
    }
}
