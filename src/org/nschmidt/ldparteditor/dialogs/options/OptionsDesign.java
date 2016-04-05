package org.nschmidt.ldparteditor.dialogs.options;

import java.math.BigDecimal;
import java.text.MessageFormat;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.resources.ResourceManager;
import org.nschmidt.ldparteditor.widgets.BigDecimalSpinner;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

class OptionsDesign extends ApplicationWindow {

    public OptionsDesign(Shell parentShell) {
        super(parentShell);
        // TODO Auto-generated constructor stub
    }

    /**
     * Create contents of the application window.
     *
     * @param parent
     */
    @Override
    protected Control createContents(Composite parent) {
        setStatus(I18n.E3D_ReadyStatus);
        Composite container = new Composite(parent, SWT.BORDER);
        GridLayout gridLayout = new GridLayout(1, true);
        container.setLayout(gridLayout);
        {
            CTabFolder tabFolder_Settings = new CTabFolder(container, SWT.BORDER);
            tabFolder_Settings.setMRUVisible(true);
            tabFolder_Settings.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
            GridData gridData = new GridData();
            gridData.horizontalAlignment = SWT.FILL;
            gridData.minimumHeight = 200;
            gridData.minimumWidth = 160;
            gridData.heightHint = 200;

            gridData.verticalAlignment = SWT.FILL;
            gridData.grabExcessVerticalSpace = true;

            gridData.grabExcessHorizontalSpace = true;
            tabFolder_Settings.setLayoutData(gridData);
            tabFolder_Settings.setSize(1024, 768);

            CTabItem tItem = new CTabItem(tabFolder_Settings, SWT.NONE);
            tItem.setText("Snapping:"); //$NON-NLS-1$
            {
                final ScrolledComposite cmp_scroll = new ScrolledComposite(tabFolder_Settings, SWT.V_SCROLL | SWT.H_SCROLL);
                Composite cmp_snappingArea = new Composite(cmp_scroll, SWT.NONE);
                tItem.setControl(cmp_scroll);
                cmp_scroll.setContent(cmp_snappingArea);
                cmp_scroll.setExpandHorizontal(true);
                cmp_scroll.setExpandVertical(true);

                cmp_snappingArea.setLayout(new GridLayout(3, false));
                ((GridLayout) cmp_snappingArea.getLayout()).verticalSpacing = 0;
                ((GridLayout) cmp_snappingArea.getLayout()).marginHeight = 0;
                ((GridLayout) cmp_snappingArea.getLayout()).marginWidth = 0;

                /*
                {
                    Composite cmp_dummy = new Composite(cmp_snappingArea, SWT.NONE);
                    cmp_dummy.setLayout(new FillLayout(SWT.HORIZONTAL));

                    Button btnCoarse = new Button(cmp_dummy, SWT.RADIO);
                    btnCoarse.setImage(ResourceManager.getImage("icon8_coarse.png")); //$NON-NLS-1$
                    btnCoarse.setToolTipText(I18n.E3D_Coarse);

                    Button btnMedium = new Button(cmp_dummy, SWT.RADIO);
                    btnMedium.setSelection(true);
                    btnMedium.setImage(ResourceManager.getImage("icon8_medium.png")); //$NON-NLS-1$
                    btnMedium.setToolTipText(I18n.E3D_Medium);

                    Button btnFine = new Button(cmp_dummy, SWT.RADIO);
                    btnFine.setImage(ResourceManager.getImage("icon8_fine.png")); //$NON-NLS-1$
                    btnFine.setToolTipText(I18n.E3D_Fine);

                    cmp_dummy.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 3, 1));
                }

                {
                    Object[] messageArguments = {I18n.UNIT_CurrentUnit()};
                    MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                    formatter.setLocale(MyLanguage.LOCALE);
                    formatter.applyPattern(I18n.E3D_MoveSnap);

                    Label lblNewLabel = new Label(cmp_snappingArea, SWT.NONE);
                    lblNewLabel.setText(formatter.format(messageArguments));
                    lblNewLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));

                }

                BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_snappingArea, SWT.NONE);
                spinner.setMaximum(new BigDecimal("100")); //$NON-NLS-1$
                spinner.setMinimum(new BigDecimal("0.0001")); //$NON-NLS-1$
                spinner.setValue(WorkbenchManager.getUserSettingState().getMedium_move_snap());
                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

                Label lblNewLabel2 = new Label(cmp_snappingArea, SWT.NONE);
                lblNewLabel2.setText(I18n.E3D_RotateSnap);
                lblNewLabel2.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));

                BigDecimalSpinner spinner2 = new BigDecimalSpinner(cmp_snappingArea, SWT.NONE);
                spinner2.setMaximum(new BigDecimal("360.0")); //$NON-NLS-1$
                spinner2.setMinimum(new BigDecimal("0.0001")); //$NON-NLS-1$
                spinner2.setValue(WorkbenchManager.getUserSettingState().getMedium_rotate_snap());
                spinner2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

                Label lblNewLabel3 = new Label(cmp_snappingArea, SWT.NONE);
                lblNewLabel3.setText(I18n.E3D_ScaleSnap);
                lblNewLabel3.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));

                BigDecimalSpinner spinner3 = new BigDecimalSpinner(cmp_snappingArea, SWT.NONE);
                spinner3.setMaximum(new BigDecimal("100.0")); //$NON-NLS-1$
                spinner3.setMinimum(new BigDecimal("0.01")); //$NON-NLS-1$
                spinner3.setValue(WorkbenchManager.getUserSettingState().getMedium_scale_snap());
                spinner3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

                //*/
                cmp_scroll.setMinSize(cmp_snappingArea.computeSize(SWT.DEFAULT, SWT.DEFAULT));
            }

            CTabItem tItem2 = new CTabItem(tabFolder_Settings, SWT.NONE);
            tItem2.setText(I18n.E3D_Selection);

            {
                final ScrolledComposite cmp_scroll = new ScrolledComposite(tabFolder_Settings, SWT.V_SCROLL | SWT.H_SCROLL);
                Composite cmp_bgArea = new Composite(cmp_scroll, SWT.NONE);
                tItem2.setControl(cmp_scroll);
                cmp_scroll.setContent(cmp_bgArea);
                cmp_scroll.setExpandHorizontal(true);
                cmp_scroll.setExpandVertical(true);

                cmp_bgArea.setLayout(new GridLayout(3, false));
                ((GridLayout) cmp_bgArea.getLayout()).verticalSpacing = 0;
                ((GridLayout) cmp_bgArea.getLayout()).marginHeight = 0;
                ((GridLayout) cmp_bgArea.getLayout()).marginWidth = 0;

                {
                    Composite cmp_Dummy = new Composite(cmp_bgArea, SWT.NONE);
                    cmp_Dummy.setLayout(new FillLayout(SWT.HORIZONTAL));

                    Button btn_PreviousSelection = new Button(cmp_Dummy, SWT.NONE);
                    btn_PreviousSelection.setImage(ResourceManager.getImage("icon8_previous.png")); //$NON-NLS-1$
                    btn_PreviousSelection.setToolTipText(I18n.E3D_PreviousItem);

                    Button btn_NextSelection = new Button(cmp_Dummy, SWT.NONE);
                    btn_NextSelection.setImage(ResourceManager.getImage("icon8_next.png")); //$NON-NLS-1$
                    btn_NextSelection.setToolTipText(I18n.E3D_NextItem);

                    cmp_Dummy.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 3, 1));
                }

                {
                    Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                    lbl_Label.setText(I18n.E3D_TextLine);
                    lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                }
                {
                    Composite cmp_LineSetup = new Composite(cmp_bgArea, SWT.NONE);
                    cmp_LineSetup.setLayout(new GridLayout(1, false));

                    Text txt_Line = new Text(cmp_LineSetup, SWT.BORDER);
                    txt_Line.setEnabled(false);
                    txt_Line.setEditable(false);
                    txt_Line.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

                    Button btn_moveAdjacentData2 = new Button(cmp_LineSetup, SWT.TOGGLE);
                    btn_moveAdjacentData2.setImage(ResourceManager.getImage("icon16_adjacentmove.png")); //$NON-NLS-1$
                    btn_moveAdjacentData2.setText(I18n.E3D_MoveAdjacentData);

                    cmp_LineSetup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                }
                {
                    Label lbl_Label = new Label(cmp_bgArea, SWT.SEPARATOR | SWT.HORIZONTAL);
                    lbl_Label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                }
                {
                    Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                    lbl_Label.setText(I18n.E3D_PositionX1);
                    lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                    BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                    spinner.setEnabled(false);
                    spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                    spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                    spinner.setValue(new BigDecimal(0));
                    spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                }
                {
                    Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                    lbl_Label.setText(I18n.E3D_PositionY1);
                    lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                    BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                    spinner.setEnabled(false);
                    spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                    spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                    spinner.setValue(new BigDecimal(0));
                    spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                }
                {
                    Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                    lbl_Label.setText(I18n.E3D_PositionZ1);
                    lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                    BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                    spinner.setEnabled(false);
                    spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                    spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                    spinner.setValue(new BigDecimal(0));
                    spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                }
                {
                    Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                    lbl_Label.setText(I18n.E3D_PositionX2);
                    lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                    BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                    spinner.setEnabled(false);
                    spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                    spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                    spinner.setValue(new BigDecimal(0));
                    spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                }
                {
                    Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                    lbl_Label.setText(I18n.E3D_PositionY2);
                    lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                    BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                    spinner.setEnabled(false);
                    spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                    spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                    spinner.setValue(new BigDecimal(0));
                    spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                }
                {
                    Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                    lbl_Label.setText(I18n.E3D_PositionZ2);
                    lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                    BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                    spinner.setEnabled(false);
                    spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                    spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                    spinner.setValue(new BigDecimal(0));
                    spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                }
                {
                    Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                    lbl_Label.setText(I18n.E3D_PositionX3);
                    lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                    BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                    spinner.setEnabled(false);
                    spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                    spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                    spinner.setValue(new BigDecimal(0));
                    spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                }
                {
                    Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                    lbl_Label.setText(I18n.E3D_PositionY3);
                    lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                    BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                    spinner.setEnabled(false);
                    spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                    spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                    spinner.setValue(new BigDecimal(0));
                    spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                }
                {
                    Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                    lbl_Label.setText(I18n.E3D_PositionZ3);
                    lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                    BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                    spinner.setEnabled(false);
                    spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                    spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                    spinner.setValue(new BigDecimal(0));
                    spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                }
                {
                    Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                    lbl_Label.setText(I18n.E3D_PositionX4);
                    lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                    BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                    spinner.setEnabled(false);
                    spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                    spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                    spinner.setValue(new BigDecimal(0));
                    spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                }
                {
                    Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                    lbl_Label.setText(I18n.E3D_PositionY4);
                    lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                    BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                    spinner.setEnabled(false);
                    spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                    spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                    spinner.setValue(new BigDecimal(0));
                    spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                }
                {
                    Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                    lbl_Label.setText(I18n.E3D_PositionZ4);
                    lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                    BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                    spinner.setEnabled(false);
                    spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                    spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                    spinner.setValue(new BigDecimal(0));
                    spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                }
                cmp_scroll.setMinSize(cmp_bgArea.computeSize(SWT.DEFAULT, SWT.DEFAULT));
            }

            CTabItem tItem3 = new CTabItem(tabFolder_Settings, SWT.NONE);
            tItem3.setText(I18n.E3D_BackgroundImage);

            {
                final ScrolledComposite cmp_scroll = new ScrolledComposite(tabFolder_Settings, SWT.V_SCROLL | SWT.H_SCROLL);
                Composite cmp_bgArea = new Composite(cmp_scroll, SWT.NONE);
                tItem3.setControl(cmp_scroll);
                cmp_scroll.setContent(cmp_bgArea);
                cmp_scroll.setExpandHorizontal(true);
                cmp_scroll.setExpandVertical(true);

                cmp_bgArea.setLayout(new GridLayout(3, false));
                ((GridLayout) cmp_bgArea.getLayout()).verticalSpacing = 0;
                ((GridLayout) cmp_bgArea.getLayout()).marginHeight = 0;
                ((GridLayout) cmp_bgArea.getLayout()).marginWidth = 0;

                {
                    Composite cmp_dummy = new Composite(cmp_bgArea, SWT.NONE);
                    cmp_dummy.setLayout(new FillLayout(SWT.HORIZONTAL));

                    Button btnPrevious = new Button(cmp_dummy, SWT.NONE);
                    btnPrevious.setImage(ResourceManager.getImage("icon8_previous.png")); //$NON-NLS-1$
                    btnPrevious.setToolTipText(I18n.E3D_Previous);

                    Button btnFocusBG = new Button(cmp_dummy, SWT.NONE);
                    btnFocusBG.setImage(ResourceManager.getImage("icon8_focus.png")); //$NON-NLS-1$
                    btnFocusBG.setToolTipText(I18n.E3D_Focus);

                    Button btnNext = new Button(cmp_dummy, SWT.NONE);
                    btnNext.setImage(ResourceManager.getImage("icon8_next.png")); //$NON-NLS-1$
                    btnNext.setToolTipText(I18n.E3D_Next);

                    cmp_dummy.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 3, 1));
                }

                {
                    Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                    lbl_Label.setText(I18n.E3D_Image);
                    lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                }
                {
                    Composite cmp_pathChooser1 = new Composite(cmp_bgArea, SWT.NONE);
                    cmp_pathChooser1.setLayout(new GridLayout(2, false));

                    Text txt_pngPath = new Text(cmp_pathChooser1, SWT.BORDER);
                    txt_pngPath.setEditable(false);
                    txt_pngPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

                    Button btn_BrowsePngPath = new Button(cmp_pathChooser1, SWT.NONE);
                    btn_BrowsePngPath.setText(I18n.DIALOG_Browse);

                    cmp_pathChooser1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                }
                {
                    Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                    lbl_Label.setText(I18n.E3D_PositionX);
                    lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                    BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                    spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                    spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                    spinner.setValue(new BigDecimal(0));
                    spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                }
                {
                    Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                    lbl_Label.setText(I18n.E3D_PositionY);
                    lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                    BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                    spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                    spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                    spinner.setValue(new BigDecimal(0));
                    spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                }
                {
                    Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                    lbl_Label.setText(I18n.E3D_PositionZ);
                    lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                    BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                    spinner.setMaximum(new BigDecimal("1E10")); //$NON-NLS-1$
                    spinner.setMinimum(new BigDecimal("-1E10")); //$NON-NLS-1$
                    spinner.setValue(new BigDecimal(0));
                    spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                }
                {
                    Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                    lbl_Label.setText(I18n.E3D_AngleY);
                    lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                    BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                    spinner.setMaximum(new BigDecimal("360")); //$NON-NLS-1$
                    spinner.setMinimum(new BigDecimal("-360")); //$NON-NLS-1$
                    spinner.setValue(new BigDecimal(0));
                    spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                }
                {
                    Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                    lbl_Label.setText(I18n.E3D_AngleX);
                    lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                    BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                    spinner.setMaximum(new BigDecimal("360")); //$NON-NLS-1$
                    spinner.setMinimum(new BigDecimal("-360")); //$NON-NLS-1$
                    spinner.setValue(new BigDecimal(0));
                    spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                }
                {
                    Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                    lbl_Label.setText(I18n.E3D_AngleZ);
                    lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                    BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                    spinner.setMaximum(new BigDecimal("360")); //$NON-NLS-1$
                    spinner.setMinimum(new BigDecimal("-360")); //$NON-NLS-1$
                    spinner.setValue(new BigDecimal(0));
                    spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                }
                {
                    Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                    lbl_Label.setText(I18n.E3D_ScaleX);
                    lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                    BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                    spinner.setMaximum(new BigDecimal("1E6")); //$NON-NLS-1$
                    spinner.setMinimum(new BigDecimal("-1E6")); //$NON-NLS-1$
                    spinner.setValue(new BigDecimal(0));
                    spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                }
                {
                    Label lbl_Label = new Label(cmp_bgArea, SWT.NONE);
                    lbl_Label.setText(I18n.E3D_ScaleY);
                    lbl_Label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                    BigDecimalSpinner spinner = new BigDecimalSpinner(cmp_bgArea, SWT.NONE);
                    spinner.setMaximum(new BigDecimal("1E6")); //$NON-NLS-1$
                    spinner.setMinimum(new BigDecimal("-1E6")); //$NON-NLS-1$
                    spinner.setValue(new BigDecimal(0));
                    spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
                }
                cmp_scroll.setMinSize(cmp_bgArea.computeSize(SWT.DEFAULT, SWT.DEFAULT));
            }

            tabFolder_Settings.setSelection(tItem);
        }
        return container;
    }


    // FIXME OptionsDialog needs implementation!
}
