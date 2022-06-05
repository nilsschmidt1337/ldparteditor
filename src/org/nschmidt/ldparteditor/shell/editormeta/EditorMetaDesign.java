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
package org.nschmidt.ldparteditor.shell.editormeta;

import static org.nschmidt.ldparteditor.helper.WidgetUtility.widgetUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.text.LDParsingException;
import org.nschmidt.ldparteditor.text.StringHelper;
import org.nschmidt.ldparteditor.text.UTF8BufferedReader;
import org.nschmidt.ldparteditor.widget.NButton;
import org.nschmidt.ldparteditor.win32appdata.AppData;
import org.nschmidt.ldparteditor.workbench.UserSettingState;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * The text editor window
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 *
 * @author nils
 *
 */
class EditorMetaDesign extends ApplicationWindow {

    final NButton[] btnCreatePtr = new NButton[1];

    final Text[] evDescriptionTxtPtr = new Text[1];
    final NButton[] evDescriptionBtnPtr = new NButton[1];
    final Text[] evNameTxtPtr = new Text[1];
    final Text[] evAuthorRealNameTxtPtr = new Text[1];
    final Text[] evAuthorUserNameTxtPtr = new Text[1];
    final NButton[] evTypeUnofficialBtnPtr = new NButton[1];
    final Combo[] evTypeTypeCmbPtr = new Combo[1];
    final NButton[] evTypeUpdateBtnPtr = new NButton[1];
    final Text[] evTypeUpdateTxtPtr = new Text[1];
    final Combo[] evLicenseCmbPtr = new Combo[1];
    final Text[] evHelpTxtPtr = new Text[1];
    final Combo[] evBfcHeaderCmbPtr = new Combo[1];
    final Combo[] evCategoryCmbPtr = new Combo[1];
    final Text[] evKeywordsTxtPtr = new Text[1];
    final Text[] evCmdlineTxtPtr = new Text[1];
    final Text[] evHistory11TxtPtr = new Text[1];
    final Text[] evHistory12TxtPtr = new Text[1];
    final Text[] evHistory13TxtPtr = new Text[1];
    final NButton[] evHistory13BtnPtr = new NButton[1];
    final Text[] evHistory21TxtPtr = new Text[1];
    final Text[] evHistory22TxtPtr = new Text[1];
    final Text[] evHistory23TxtPtr = new Text[1];
    final NButton[] evHistory23BtnPtr = new NButton[1];
    final NButton[] evCommentBtnPtr = new NButton[1];
    final Text[] evCommentTxtPtr = new Text[1];
    final Combo[] evBfcCmbPtr = new Combo[1];
    final Combo[] evTexmapPlanarCmbPtr = new Combo[1];
    final Text[] evTexmapPlanar1TxtPtr = new Text[1];
    final Text[] evTexmapPlanar2TxtPtr = new Text[1];
    final Text[] evTexmapPlanar3TxtPtr = new Text[1];
    final Text[] evTexmapPlanar4TxtPtr = new Text[1];
    final Text[] evTexmapPlanar5TxtPtr = new Text[1];
    final Text[] evTexmapPlanar6TxtPtr = new Text[1];
    final Text[] evTexmapPlanar7TxtPtr = new Text[1];
    final Text[] evTexmapPlanar8TxtPtr = new Text[1];
    final Text[] evTexmapPlanar9TxtPtr = new Text[1];
    final Text[] evTexmapPlanar10TxtPtr = new Text[1];
    final NButton[] evTexmapPlanarBtnPtr = new NButton[1];
    final Combo[] evTexmapCyliCmbPtr = new Combo[1];
    final Text[] evTexmapCyli1TxtPtr = new Text[1];
    final Text[] evTexmapCyli2TxtPtr = new Text[1];
    final Text[] evTexmapCyli3TxtPtr = new Text[1];
    final Text[] evTexmapCyli4TxtPtr = new Text[1];
    final Text[] evTexmapCyli5TxtPtr = new Text[1];
    final Text[] evTexmapCyli6TxtPtr = new Text[1];
    final Text[] evTexmapCyli7TxtPtr = new Text[1];
    final Text[] evTexmapCyli8TxtPtr = new Text[1];
    final Text[] evTexmapCyli9TxtPtr = new Text[1];
    final Text[] evTexmapCyli10TxtPtr = new Text[1];
    final Text[] evTexmapCyli11TxtPtr = new Text[1];
    final NButton[] evTexmapCyliBtnPtr = new NButton[1];
    final Combo[] evTexmapSphereCmbPtr = new Combo[1];
    final Text[] evTexmapSphere1TxtPtr = new Text[1];
    final Text[] evTexmapSphere2TxtPtr = new Text[1];
    final Text[] evTexmapSphere3TxtPtr = new Text[1];
    final Text[] evTexmapSphere4TxtPtr = new Text[1];
    final Text[] evTexmapSphere5TxtPtr = new Text[1];
    final Text[] evTexmapSphere6TxtPtr = new Text[1];
    final Text[] evTexmapSphere7TxtPtr = new Text[1];
    final Text[] evTexmapSphere8TxtPtr = new Text[1];
    final Text[] evTexmapSphere9TxtPtr = new Text[1];
    final Text[] evTexmapSphere10TxtPtr = new Text[1];
    final Text[] evTexmapSphere11TxtPtr = new Text[1];
    final Text[] evTexmapSphere12TxtPtr = new Text[1];
    final NButton[] evTexmapSphereBtnPtr = new NButton[1];
    final NButton[] evTexmapFallbackBtnPtr = new NButton[1];
    final Text[] evTexmapMetaTxtPtr = new Text[1];
    final NButton[] evTexmapEndBtnPtr = new NButton[1];
    final Text[] evTodoTxtPtr = new Text[1];
    final Text[] evVertex1TxtPtr = new Text[1];
    final Text[] evVertex2TxtPtr = new Text[1];
    final Text[] evVertex3TxtPtr = new Text[1];
    final Combo[] evCsgActionCmbPtr = new Combo[1];
    final Text[] evCsgAction1TxtPtr = new Text[1];
    final Text[] evCsgAction2TxtPtr = new Text[1];
    final Text[] evCsgAction3TxtPtr = new Text[1];
    final Combo[] evCsgBodyCmbPtr = new Combo[1];
    final Text[] evCsgBody1TxtPtr = new Text[1];
    final Text[] evCsgBody2TxtPtr = new Text[1];
    final Text[] evCsgBody3TxtPtr = new Text[1];
    final Text[] evCsgBody4TxtPtr = new Text[1];
    final Text[] evCsgBody5TxtPtr = new Text[1];
    final Text[] evCsgBody6TxtPtr = new Text[1];
    final Text[] evCsgBody7TxtPtr = new Text[1];
    final Text[] evCsgBody8TxtPtr = new Text[1];
    final Text[] evCsgBody9TxtPtr = new Text[1];
    final Text[] evCsgBody10TxtPtr = new Text[1];
    final Text[] evCsgBody11TxtPtr = new Text[1];
    final Text[] evCsgBody12TxtPtr = new Text[1];
    final Text[] evCsgBody13TxtPtr = new Text[1];
    final Text[] evCsgBody14TxtPtr = new Text[1];
    final Text[] evCsgTrans1TxtPtr = new Text[1];
    final Text[] evCsgTrans2TxtPtr = new Text[1];
    final Text[] evCsgTrans3TxtPtr = new Text[1];
    final Text[] evCsgTrans4TxtPtr = new Text[1];
    final Text[] evCsgTrans5TxtPtr = new Text[1];
    final Text[] evCsgTrans6TxtPtr = new Text[1];
    final Text[] evCsgTrans7TxtPtr = new Text[1];
    final Text[] evCsgTrans8TxtPtr = new Text[1];
    final Text[] evCsgTrans9TxtPtr = new Text[1];
    final Text[] evCsgTrans10TxtPtr = new Text[1];
    final Text[] evCsgTrans11TxtPtr = new Text[1];
    final Text[] evCsgTrans12TxtPtr = new Text[1];
    final Text[] evCsgTrans13TxtPtr = new Text[1];
    final Text[] evCsgTrans14TxtPtr = new Text[1];
    final Text[] evCsgTrans15TxtPtr = new Text[1];
    final Text[] evCsgEx1TxtPtr = new Text[1];
    final Text[] evCsgEx2TxtPtr = new Text[1];
    final Text[] evCsgEx3TxtPtr = new Text[1];
    final Text[] evCsgEx4TxtPtr = new Text[1];
    final Text[] evCsgEx5TxtPtr = new Text[1];
    final Text[] evCsgEx6TxtPtr = new Text[1];
    final Text[] evCsgEx7TxtPtr = new Text[1];
    final Text[] evCsgCompileTxtPtr = new Text[1];
    final Text[] evCsgQualityTxtPtr = new Text[1];
    final Text[] evCsgEpsilonTxtPtr = new Text[1];
    final Text[] evCsgTJunctionEpsilonTxtPtr = new Text[1];
    final Text[] evCsgEdgeCollapseEpsilonTxtPtr = new Text[1];
    final NButton[] evCsgDontOptimizeBtnPtr = new NButton[1];
    final Text[] evPng1TxtPtr = new Text[1];
    final Text[] evPng2TxtPtr = new Text[1];
    final Text[] evPng3TxtPtr = new Text[1];
    final Text[] evPng4TxtPtr = new Text[1];
    final Text[] evPng5TxtPtr = new Text[1];
    final Text[] evPng6TxtPtr = new Text[1];
    final Text[] evPng7TxtPtr = new Text[1];
    final Text[] evPng8TxtPtr = new Text[1];
    final Text[] evPng9TxtPtr = new Text[1];
    final NButton[] evPngBtnPtr = new NButton[1];

    final Label[] lblLineToInsertPtr  = new Label[1];

    EditorMetaDesign() {
        super(null);
    }

    /**
     * Create contents of the application window.
     *
     * @param parent
     */
    @Override
    protected Control createContents(Composite parent) {
        final UserSettingState userSettings = WorkbenchManager.getUserSettingState();
        setStatus(I18n.E3D_READY_STATUS);
        Composite container = new Composite(parent, SWT.BORDER);
        GridLayout gridLayout = new GridLayout(1, true);
        container.setLayout(gridLayout);
        {
            CTabFolder tabFolderSettings = new CTabFolder(container, SWT.BORDER);
            tabFolderSettings.setMRUVisible(true);
            tabFolderSettings.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
            GridData gridData = new GridData();
            gridData.horizontalAlignment = SWT.FILL;
            gridData.minimumHeight = 200;
            gridData.minimumWidth = 160;
            gridData.heightHint = 200;

            gridData.verticalAlignment = SWT.FILL;
            gridData.grabExcessVerticalSpace = true;

            gridData.grabExcessHorizontalSpace = true;
            tabFolderSettings.setLayoutData(gridData);
            tabFolderSettings.setSize(1024, 768);

            final CTabItem tItem = new CTabItem(tabFolderSettings, SWT.NONE);
            tItem.setText(I18n.META_LDRAW_HEADER);
            {
                final ScrolledComposite cmpScroll = new ScrolledComposite(tabFolderSettings, SWT.H_SCROLL | SWT.V_SCROLL);
                tItem.setControl(cmpScroll);

                Composite cmpMetaArea = new Composite(cmpScroll, SWT.NONE);
                cmpScroll.setContent(cmpMetaArea);
                cmpScroll.setExpandHorizontal(true);
                cmpScroll.setExpandVertical(true);
                cmpScroll.setMinSize(600, 800);

                GridData gdm = new GridData();
                gdm.grabExcessHorizontalSpace = true;
                gdm.grabExcessVerticalSpace = true;
                cmpScroll.setLayoutData(gdm);

                cmpMetaArea.setLayout(new GridLayout(1, false));

                {
                    Composite grpMeta = cmpMetaArea;
                    grpMeta.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                    grpMeta.setLayout(new GridLayout(1, false));

                    {
                        Composite cmpDescription = new Composite(grpMeta, SWT.NONE);
                        cmpDescription.setLayout(new GridLayout(3, false));

                        Label lblDescription = new Label(cmpDescription, SWT.NONE);
                        lblDescription.setText("Description:"); //$NON-NLS-1$

                        Text txtDescription = new Text(cmpDescription, SWT.SEARCH);
                        txtDescription.setMessage(I18n.META_DESCRIPTION);
                        evDescriptionTxtPtr[0] = txtDescription;

                        NButton btnNeedsWork = new NButton(cmpDescription, SWT.TOGGLE);
                        btnNeedsWork.setText("(Needs Work)"); //$NON-NLS-1$
                        evDescriptionBtnPtr[0] = btnNeedsWork;
                    }

                    {
                        Composite cmpName = new Composite(grpMeta, SWT.NONE);
                        cmpName.setLayout(new GridLayout(2, false));

                        Label lblName = new Label(cmpName, SWT.NONE);
                        lblName.setText("0 Name: "); //$NON-NLS-1$

                        Text txtName = new Text(cmpName, SWT.SEARCH);
                        txtName.setMessage(I18n.META_FILENAME);
                        evNameTxtPtr[0] = txtName;
                    }

                    {
                        Composite cmpAuthor = new Composite(grpMeta, SWT.NONE);
                        cmpAuthor.setLayout(new GridLayout(5, false));

                        Label lblAuthor = new Label(cmpAuthor, SWT.NONE);
                        lblAuthor.setText("0 Author: "); //$NON-NLS-1$

                        Text txtRealName = new Text(cmpAuthor, SWT.SEARCH);
                        txtRealName.setMessage(I18n.META_AUTHOR);
                        evAuthorRealNameTxtPtr[0] = txtRealName;
                        if (userSettings.getRealUserName() != null) {
                            txtRealName.setText(userSettings.getRealUserName());
                        }

                        Label lblAuthor2 = new Label(cmpAuthor, SWT.NONE);
                        lblAuthor2.setText("["); //$NON-NLS-1$

                        Text txtUserName = new Text(cmpAuthor, SWT.SEARCH);
                        txtUserName.setMessage(I18n.META_USERNAME);
                        evAuthorUserNameTxtPtr[0] = txtUserName;

                        if (userSettings.getLdrawUserName() != null) {
                            txtUserName.setText(userSettings.getLdrawUserName());
                        }

                        Label lblAuthor3 = new Label(cmpAuthor, SWT.NONE);
                        lblAuthor3.setText("]"); //$NON-NLS-1$
                    }

                    {
                        Composite cmpType = new Composite(grpMeta, SWT.NONE);
                        cmpType.setLayout(new GridLayout(5, false));

                        Label lblType = new Label(cmpType, SWT.NONE);
                        lblType.setText("0 !LDRAW_ORG "); //$NON-NLS-1$

                        NButton btnUnofficial = new NButton(cmpType, SWT.TOGGLE);
                        btnUnofficial.setText("Unofficial"); //$NON-NLS-1$
                        evTypeUnofficialBtnPtr[0] = btnUnofficial;

                        Combo cmbType = new Combo(cmpType, SWT.NONE);
                        widgetUtil(cmbType).setItems("Part", "Subpart", "Primitive", "8_Primitive", "48_Primitive", "Shortcut", "Part Alias", "Part Physical_Colour",  "Part Physical_Colour Alias", "Part Flexible_Section", "Shortcut Alias", "Shortcut Physical_Colour",  "Shortcut Physical_Colour Alias"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$
                        cmbType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                        cmbType.setText("Part"); //$NON-NLS-1$
                        cmbType.select(0);
                        evTypeTypeCmbPtr[0] = cmbType;

                        NButton btnUpdate = new NButton(cmpType, SWT.TOGGLE);
                        btnUpdate.setText("UPDATE"); //$NON-NLS-1$
                        evTypeUpdateBtnPtr[0] = btnUpdate;

                        Text txtUpdate = new Text(cmpType, SWT.SEARCH);
                        txtUpdate.setMessage(I18n.META_YEAR_RELEASE);
                        txtUpdate.setEnabled(false);
                        evTypeUpdateTxtPtr[0] = txtUpdate;
                    }

                    {
                        Composite cmpLicense = new Composite(grpMeta, SWT.NONE);
                        cmpLicense.setLayout(new GridLayout(2, false));

                        Label lblLicense = new Label(cmpLicense, SWT.NONE);
                        lblLicense.setText("0 !LICENSE "); //$NON-NLS-1$

                        Combo cmbLicense = new Combo(cmpLicense, SWT.NONE);
                        widgetUtil(cmbLicense).setItems("Licensed under CC BY 4.0 : see CAreadme.txt", "Licensed under CC BY 2.0 and CC BY 4.0 : see CAreadme.txt", "Redistributable under CCAL version 2.0 : see CAreadme.txt", "Not redistributable : see NonCAreadme.txt" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                        cmbLicense.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                        cmbLicense.setText(userSettings.getLicense() != null ? userSettings.getLicense() : "Licensed under CC BY 4.0 : see CAreadme.txt"); //$NON-NLS-1$
                        cmbLicense.select(0);
                        evLicenseCmbPtr[0] = cmbLicense;
                    }

                    {
                        Composite cmpHelp = new Composite(grpMeta, SWT.NONE);
                        cmpHelp.setLayout(new GridLayout(2, false));

                        Label lblHelp = new Label(cmpHelp, SWT.NONE);
                        lblHelp.setText("0 !HELP "); //$NON-NLS-1$

                        Text txtHelp = new Text(cmpHelp, SWT.SEARCH);
                        txtHelp.setMessage(I18n.META_HELP);
                        evHelpTxtPtr[0] = txtHelp;
                    }

                    {
                        Composite cmpBfc = new Composite(grpMeta, SWT.NONE);
                        cmpBfc.setLayout(new GridLayout(2, false));

                        Label lblBfc = new Label(cmpBfc, SWT.NONE);
                        lblBfc.setText("0 BFC "); //$NON-NLS-1$

                        Combo cmbBfc = new Combo(cmpBfc, SWT.NONE);
                        widgetUtil(cmbBfc).setItems("NOCERTIFY", "CERTIFY CW", "CERTIFY CCW"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        cmbBfc.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                        cmbBfc.setText("NOCERTIFY"); //$NON-NLS-1$
                        cmbBfc.select(0);
                        evBfcHeaderCmbPtr[0] = cmbBfc;
                    }

                    {
                        Composite cmpCategory = new Composite(grpMeta, SWT.NONE);
                        cmpCategory.setLayout(new GridLayout(2, false));

                        Label lblCategory = new Label(cmpCategory, SWT.NONE);
                        lblCategory.setText("0 !CATEGORY "); //$NON-NLS-1$

                        Combo cmbCategory = new Combo(cmpCategory, SWT.NONE);
                        evCategoryCmbPtr[0] = cmbCategory;
                        File categoryFile = new File(AppData.getPath() + "categories.txt"); //$NON-NLS-1$
                        if (!categoryFile.exists() || !categoryFile.isFile()) {
                            categoryFile = new File("categories.txt"); //$NON-NLS-1$
                        }
                        if (categoryFile.exists() && categoryFile.isFile()) {
                            try (UTF8BufferedReader reader = new UTF8BufferedReader(categoryFile.getAbsolutePath())) {
                                List<String> categories = new ArrayList<>();
                                categories.add(""); //$NON-NLS-1$
                                String line ;
                                while ((line = reader.readLine()) != null) {
                                    line = line.trim();
                                    if (StringHelper.isNotBlank(line)) {
                                        categories.add(line);
                                    }
                                }
                                evCategoryCmbPtr[0].setItems(categories.toArray(new String[categories.size()]));
                            } catch (LDParsingException | FileNotFoundException e) {
                                NLogger.error(EditorMetaDesign.class, e);
                                setDefaultCategories();
                            }
                        } else {
                            setDefaultCategories();
                        }

                        cmbCategory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                        cmbCategory.setText(""); //$NON-NLS-1$
                        cmbCategory.select(0);
                    }

                    {
                        Composite cmpKeywords = new Composite(grpMeta, SWT.NONE);
                        cmpKeywords.setLayout(new GridLayout(3, false));

                        Label lblKeywords = new Label(cmpKeywords, SWT.NONE);
                        lblKeywords.setText("0 !KEYWORDS "); //$NON-NLS-1$

                        Text txtKeywords = new Text(cmpKeywords, SWT.SEARCH);
                        txtKeywords.setMessage(I18n.META_KEYWORDS_1);
                        evKeywordsTxtPtr[0] = txtKeywords;

                        Label lblKeywords2 = new Label(cmpKeywords, SWT.NONE);
                        lblKeywords2.setText(I18n.META_KEYWORDS_2);
                    }

                    {
                        Composite cmpCmdline = new Composite(grpMeta, SWT.NONE);
                        cmpCmdline.setLayout(new GridLayout(3, false));

                        Label lblCmdline = new Label(cmpCmdline, SWT.NONE);
                        lblCmdline.setText("0 !CMDLINE "); //$NON-NLS-1$

                        Text txtCmdline = new Text(cmpCmdline, SWT.SEARCH);
                        txtCmdline.setMessage(I18n.META_COMMAND_LINE);
                        evCmdlineTxtPtr[0] = txtCmdline;
                    }

                    {
                        Composite cmpHistory1 = new Composite(grpMeta, SWT.NONE);
                        cmpHistory1.setLayout(new GridLayout(7, false));

                        Label lblHistory11 = new Label(cmpHistory1, SWT.NONE);
                        lblHistory11.setText("0 !HISTORY "); //$NON-NLS-1$

                        Text txtHistory11 = new Text(cmpHistory1, SWT.SEARCH);
                        txtHistory11.setMessage(I18n.META_HISTORY_1);
                        evHistory11TxtPtr[0] = txtHistory11;

                        Label lblHistory12 = new Label(cmpHistory1, SWT.NONE);
                        lblHistory12.setText(" ["); //$NON-NLS-1$

                        Text txtHistory12 = new Text(cmpHistory1, SWT.SEARCH);
                        txtHistory12.setMessage(I18n.META_HISTORY_2);
                        evHistory12TxtPtr[0] = txtHistory12;

                        Label lblHistory13 = new Label(cmpHistory1, SWT.NONE);
                        lblHistory13.setText("] "); //$NON-NLS-1$
                        
                        NButton btnHistory13 = new NButton(cmpHistory1, SWT.TOGGLE);
                        btnHistory13.setText("[4.0]"); //$NON-NLS-1$
                        btnHistory13.setSelection(true);
                        evHistory13BtnPtr[0] = btnHistory13;

                        Text txtHistory13 = new Text(cmpHistory1, SWT.SEARCH);
                        txtHistory13.setMessage(I18n.META_HISTORY_4);
                        evHistory13TxtPtr[0] = txtHistory13;
                    }

                    {
                        Composite cmpHistory2 = new Composite(grpMeta, SWT.NONE);
                        cmpHistory2.setLayout(new GridLayout(7, false));

                        Label lblHistory21 = new Label(cmpHistory2, SWT.NONE);
                        lblHistory21.setText("or 0 !HISTORY "); //$NON-NLS-1$

                        Text txtHistory21 = new Text(cmpHistory2, SWT.SEARCH);
                        txtHistory21.setMessage(I18n.META_HISTORY_1);
                        evHistory21TxtPtr[0] = txtHistory21;

                        Label lblHistory22 = new Label(cmpHistory2, SWT.NONE);
                        lblHistory22.setText(" {"); //$NON-NLS-1$

                        Text txtHistory22 = new Text(cmpHistory2, SWT.SEARCH);
                        txtHistory22.setMessage(I18n.META_HISTORY_3);
                        evHistory22TxtPtr[0] = txtHistory22;

                        Label lblHistory23 = new Label(cmpHistory2, SWT.NONE);
                        lblHistory23.setText("} "); //$NON-NLS-1$
                        
                        NButton btnHistory23 = new NButton(cmpHistory2, SWT.TOGGLE);
                        btnHistory23.setText("[4.0]"); //$NON-NLS-1$
                        btnHistory23.setSelection(true);
                        evHistory23BtnPtr[0] = btnHistory23;

                        Text txtHistory23 = new Text(cmpHistory2, SWT.SEARCH);
                        txtHistory23.setMessage(I18n.META_HISTORY_4);
                        evHistory23TxtPtr[0] = txtHistory23;
                    }

                    {
                        Composite cmpComment = new Composite(grpMeta, SWT.NONE);
                        cmpComment.setLayout(new GridLayout(5, false));

                        Label lblType = new Label(cmpComment, SWT.NONE);
                        lblType.setText("0 // "); //$NON-NLS-1$

                        NButton btnNeedsWork2 = new NButton(cmpComment, SWT.TOGGLE);
                        btnNeedsWork2.setText("Needs work:"); //$NON-NLS-1$
                        evCommentBtnPtr[0] = btnNeedsWork2;

                        Text txtComment = new Text(cmpComment, SWT.SEARCH);
                        txtComment.setMessage(I18n.META_COMMENT);
                        evCommentTxtPtr[0] = txtComment;
                    }
                }
            }

            final CTabItem tItem2 = new CTabItem(tabFolderSettings, SWT.NONE);
            tItem2.setText(I18n.META_BACK_FACE_CULLING);
            {

                final ScrolledComposite cmpScroll = new ScrolledComposite(tabFolderSettings, SWT.H_SCROLL | SWT.V_SCROLL);
                tItem2.setControl(cmpScroll);

                Composite cmpMetaArea = new Composite(cmpScroll, SWT.NONE);
                cmpScroll.setContent(cmpMetaArea);
                cmpScroll.setExpandHorizontal(true);
                cmpScroll.setExpandVertical(true);
                cmpScroll.setMinSize(600, 800);

                GridData gdm = new GridData();
                gdm.grabExcessHorizontalSpace = true;
                cmpScroll.setLayoutData(gdm);

                cmpMetaArea.setLayout(new GridLayout(1, false));

                {
                    Composite grpMeta = cmpMetaArea;
                    grpMeta.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                    grpMeta.setLayout(new GridLayout(1, false));

                    Composite cmpBfc = new Composite(grpMeta, SWT.NONE);
                    cmpBfc.setLayout(new GridLayout(2, false));

                    Label lblBfc = new Label(cmpBfc, SWT.NONE);
                    lblBfc.setText("0 BFC "); //$NON-NLS-1$

                    Combo cmbBfc = new Combo(cmpBfc, SWT.NONE);
                    widgetUtil(cmbBfc).setItems("INVERTNEXT", "NOCLIP", "CW", "CCW", "CLIP", "CLIP CW", "CLIP CCW"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
                    cmbBfc.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                    cmbBfc.setText("INVERTNEXT"); //$NON-NLS-1$
                    cmbBfc.select(0);
                    evBfcCmbPtr[0] = cmbBfc;
                }
            }

            final CTabItem tItem3 = new CTabItem(tabFolderSettings, SWT.NONE);
            tItem3.setText(I18n.META_TEXTURE_MAPPING);
            {

                final ScrolledComposite cmpScroll = new ScrolledComposite(tabFolderSettings, SWT.H_SCROLL | SWT.V_SCROLL);
                tItem3.setControl(cmpScroll);

                Composite cmpMetaArea = new Composite(cmpScroll, SWT.NONE);
                cmpScroll.setContent(cmpMetaArea);
                cmpScroll.setExpandHorizontal(true);
                cmpScroll.setExpandVertical(true);
                cmpScroll.setMinSize(600, 800);

                GridData gdm = new GridData();
                gdm.grabExcessHorizontalSpace = true;
                cmpScroll.setLayoutData(gdm);

                cmpMetaArea.setLayout(new GridLayout(1, false));

                {
                    Composite grpMeta = cmpMetaArea;
                    grpMeta.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                    grpMeta.setLayout(new GridLayout(1, false));
                    {
                        Composite cmpTexmap = new Composite(grpMeta, SWT.NONE);
                        cmpTexmap.setLayout(new GridLayout(14, false));

                        Label lblTexmap = new Label(cmpTexmap, SWT.NONE);
                        lblTexmap.setText("0 !TEXMAP "); //$NON-NLS-1$

                        Combo cmbTexmap = new Combo(cmpTexmap, SWT.NONE);
                        widgetUtil(cmbTexmap).setItems("START", "NEXT"); //$NON-NLS-1$ //$NON-NLS-2$
                        cmbTexmap.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                        cmbTexmap.setText("START"); //$NON-NLS-1$
                        cmbTexmap.select(0);
                        evTexmapPlanarCmbPtr[0] = cmbTexmap;

                        Label lblPlanar = new Label(cmpTexmap, SWT.NONE);
                        lblPlanar.setText(" PLANAR "); //$NON-NLS-1$
                        {
                            Text txtPlanar = new Text(cmpTexmap, SWT.SEARCH);
                            txtPlanar.setMessage(I18n.META_TEXTURE_X1);
                            evTexmapPlanar1TxtPtr[0] = txtPlanar;
                        }
                        {
                            Text txtPlanar = new Text(cmpTexmap, SWT.SEARCH);
                            txtPlanar.setMessage(I18n.META_TEXTURE_Y1);
                            evTexmapPlanar2TxtPtr[0] = txtPlanar;
                        }
                        {
                            Text txtPlanar = new Text(cmpTexmap, SWT.SEARCH);
                            txtPlanar.setMessage(I18n.META_TEXTURE_Z1);
                            evTexmapPlanar3TxtPtr[0] = txtPlanar;
                        }
                        {
                            Text txtPlanar = new Text(cmpTexmap, SWT.SEARCH);
                            txtPlanar.setMessage(I18n.META_TEXTURE_X2);
                            evTexmapPlanar4TxtPtr[0] = txtPlanar;
                        }
                        {
                            Text txtPlanar = new Text(cmpTexmap, SWT.SEARCH);
                            txtPlanar.setMessage(I18n.META_TEXTURE_Y2);
                            evTexmapPlanar5TxtPtr[0] = txtPlanar;
                        }
                        {
                            Text txtPlanar = new Text(cmpTexmap, SWT.SEARCH);
                            txtPlanar.setMessage(I18n.META_TEXTURE_Z2);
                            evTexmapPlanar6TxtPtr[0] = txtPlanar;
                        }
                        {
                            Text txtPlanar = new Text(cmpTexmap, SWT.SEARCH);
                            txtPlanar.setMessage(I18n.META_TEXTURE_X3);
                            evTexmapPlanar7TxtPtr[0] = txtPlanar;
                        }
                        {
                            Text txtPlanar = new Text(cmpTexmap, SWT.SEARCH);
                            txtPlanar.setMessage(I18n.META_TEXTURE_Y3);
                            evTexmapPlanar8TxtPtr[0] = txtPlanar;
                        }
                        {
                            Text txtPlanar = new Text(cmpTexmap, SWT.SEARCH);
                            txtPlanar.setMessage(I18n.META_TEXTURE_Z3);
                            evTexmapPlanar9TxtPtr[0] = txtPlanar;
                        }
                        {
                            Text txtPng = new Text(cmpTexmap, SWT.SEARCH);
                            txtPng.setMessage(I18n.META_TEXTURE_PNG);
                            evTexmapPlanar10TxtPtr[0] = txtPng;
                        }
                        {
                            NButton btnBrowse = new NButton(cmpTexmap, SWT.NONE);
                            btnBrowse.setText(I18n.DIALOG_BROWSE);
                            evTexmapPlanarBtnPtr[0] = btnBrowse;
                        }
                    }
                    {
                        Composite cmpTexmap = new Composite(grpMeta, SWT.NONE);
                        cmpTexmap.setLayout(new GridLayout(15, false));

                        Label lblTexmap = new Label(cmpTexmap, SWT.NONE);
                        lblTexmap.setText("0 !TEXMAP "); //$NON-NLS-1$

                        Combo cmbTexmap = new Combo(cmpTexmap, SWT.NONE);
                        widgetUtil(cmbTexmap).setItems("START", "NEXT"); //$NON-NLS-1$ //$NON-NLS-2$
                        cmbTexmap.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                        cmbTexmap.setText("START"); //$NON-NLS-1$
                        cmbTexmap.select(0);
                        evTexmapCyliCmbPtr[0] = cmbTexmap;

                        Label lblCylindrical = new Label(cmpTexmap, SWT.NONE);
                        lblCylindrical.setText(" CYLINDRICAL "); //$NON-NLS-1$
                        {
                            Text txtCylindrical = new Text(cmpTexmap, SWT.SEARCH);
                            txtCylindrical.setMessage(I18n.META_TEXTURE_X1);
                            txtCylindrical.setToolTipText(I18n.META_CYLINDER_BOTTOM_CENTER);
                            evTexmapCyli1TxtPtr[0] = txtCylindrical;
                        }
                        {
                            Text txtCylindrical = new Text(cmpTexmap, SWT.SEARCH);
                            txtCylindrical.setMessage(I18n.META_TEXTURE_Y1);
                            txtCylindrical.setToolTipText(I18n.META_CYLINDER_BOTTOM_CENTER);
                            evTexmapCyli2TxtPtr[0] = txtCylindrical;
                        }
                        {
                            Text txtCylindrical = new Text(cmpTexmap, SWT.SEARCH);
                            txtCylindrical.setMessage(I18n.META_TEXTURE_Z1);
                            txtCylindrical.setToolTipText(I18n.META_CYLINDER_BOTTOM_CENTER);
                            evTexmapCyli3TxtPtr[0] = txtCylindrical;
                        }
                        {
                            Text txtCylindrical = new Text(cmpTexmap, SWT.SEARCH);
                            txtCylindrical.setMessage(I18n.META_TEXTURE_X2);
                            txtCylindrical.setToolTipText(I18n.META_CYLINDER_TOP_CENTER);
                            evTexmapCyli4TxtPtr[0] = txtCylindrical;
                        }
                        {
                            Text txtCylindrical = new Text(cmpTexmap, SWT.SEARCH);
                            txtCylindrical.setMessage(I18n.META_TEXTURE_Y2);
                            txtCylindrical.setToolTipText(I18n.META_CYLINDER_TOP_CENTER);
                            evTexmapCyli5TxtPtr[0] = txtCylindrical;
                        }
                        {
                            Text txtCylindrical = new Text(cmpTexmap, SWT.SEARCH);
                            txtCylindrical.setMessage(I18n.META_TEXTURE_Z2);
                            txtCylindrical.setToolTipText(I18n.META_CYLINDER_TOP_CENTER);
                            evTexmapCyli6TxtPtr[0] = txtCylindrical;
                        }
                        {
                            Text txtCylindrical = new Text(cmpTexmap, SWT.SEARCH);
                            txtCylindrical.setMessage(I18n.META_TEXTURE_X3);
                            txtCylindrical.setToolTipText(I18n.META_TEXTURE_BOTTOM_CENTER);
                            evTexmapCyli7TxtPtr[0] = txtCylindrical;
                        }
                        {
                            Text txtCylindrical = new Text(cmpTexmap, SWT.SEARCH);
                            txtCylindrical.setMessage(I18n.META_TEXTURE_Y3);
                            txtCylindrical.setToolTipText(I18n.META_TEXTURE_BOTTOM_CENTER);
                            evTexmapCyli8TxtPtr[0] = txtCylindrical;
                        }
                        {
                            Text txtCylindrical = new Text(cmpTexmap, SWT.SEARCH);
                            txtCylindrical.setMessage(I18n.META_TEXTURE_Z3);
                            txtCylindrical.setToolTipText(I18n.META_TEXTURE_BOTTOM_CENTER);
                            evTexmapCyli9TxtPtr[0] = txtCylindrical;
                        }
                        {
                            Text txtCylindrical = new Text(cmpTexmap, SWT.SEARCH);
                            txtCylindrical.setMessage(I18n.META_TEXTURE_ANGLE_1);
                            evTexmapCyli10TxtPtr[0] = txtCylindrical;
                        }
                        {
                            Text txtPng = new Text(cmpTexmap, SWT.SEARCH);
                            txtPng.setMessage(I18n.META_TEXTURE_PNG);
                            evTexmapCyli11TxtPtr[0] = txtPng;
                        }
                        {
                            NButton btnBrowse = new NButton(cmpTexmap, SWT.NONE);
                            btnBrowse.setText(I18n.DIALOG_BROWSE);
                            evTexmapCyliBtnPtr[0] = btnBrowse;
                        }
                    }
                    {
                        Composite cmpTexmap = new Composite(grpMeta, SWT.NONE);
                        cmpTexmap.setLayout(new GridLayout(16, false));

                        Label lblTexmap = new Label(cmpTexmap, SWT.NONE);
                        lblTexmap.setText("0 !TEXMAP "); //$NON-NLS-1$

                        Combo cmbTexmap = new Combo(cmpTexmap, SWT.NONE);
                        widgetUtil(cmbTexmap).setItems("START", "NEXT"); //$NON-NLS-1$ //$NON-NLS-2$
                        cmbTexmap.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                        cmbTexmap.setText("START"); //$NON-NLS-1$
                        cmbTexmap.select(0);
                        evTexmapSphereCmbPtr[0] = cmbTexmap;

                        Label lblSpherical = new Label(cmpTexmap, SWT.NONE);
                        lblSpherical.setText(" SPHERICAL "); //$NON-NLS-1$
                        {
                            Text txtSpherical = new Text(cmpTexmap, SWT.SEARCH);
                            txtSpherical.setMessage(I18n.META_TEXTURE_X1);
                            txtSpherical.setToolTipText(I18n.META_TEXTURE_SPHERE_CENTER);
                            evTexmapSphere1TxtPtr[0] = txtSpherical;
                        }
                        {
                            Text txtSpherical = new Text(cmpTexmap, SWT.SEARCH);
                            txtSpherical.setMessage(I18n.META_TEXTURE_Y1);
                            txtSpherical.setToolTipText(I18n.META_TEXTURE_SPHERE_CENTER);
                            evTexmapSphere2TxtPtr[0] = txtSpherical;
                        }
                        {
                            Text txtSpherical = new Text(cmpTexmap, SWT.SEARCH);
                            txtSpherical.setMessage(I18n.META_TEXTURE_Z1);
                            txtSpherical.setToolTipText(I18n.META_TEXTURE_SPHERE_CENTER);
                            evTexmapSphere3TxtPtr[0] = txtSpherical;
                        }
                        {
                            Text txtSpherical = new Text(cmpTexmap, SWT.SEARCH);
                            txtSpherical.setMessage(I18n.META_TEXTURE_X2);
                            txtSpherical.setToolTipText(I18n.META_TEXTURE_CENTER);
                            evTexmapSphere4TxtPtr[0] = txtSpherical;
                        }
                        {
                            Text txtSpherical = new Text(cmpTexmap, SWT.SEARCH);
                            txtSpherical.setMessage(I18n.META_TEXTURE_Y2);
                            txtSpherical.setToolTipText(I18n.META_TEXTURE_CENTER);
                            evTexmapSphere5TxtPtr[0] = txtSpherical;
                        }
                        {
                            Text txtSpherical = new Text(cmpTexmap, SWT.SEARCH);
                            txtSpherical.setMessage(I18n.META_TEXTURE_Z2);
                            txtSpherical.setToolTipText(I18n.META_TEXTURE_CENTER);
                            evTexmapSphere6TxtPtr[0] = txtSpherical;
                        }
                        {
                            Text txtSpherical = new Text(cmpTexmap, SWT.SEARCH);
                            txtSpherical.setMessage(I18n.META_TEXTURE_X3);
                            txtSpherical.setToolTipText(I18n.META_TEXTURE_TOP_CENTER);
                            evTexmapSphere7TxtPtr[0] = txtSpherical;
                        }
                        {
                            Text txtSpherical = new Text(cmpTexmap, SWT.SEARCH);
                            txtSpherical.setMessage(I18n.META_TEXTURE_Y3);
                            txtSpherical.setToolTipText(I18n.META_TEXTURE_TOP_CENTER);
                            evTexmapSphere8TxtPtr[0] = txtSpherical;
                        }
                        {
                            Text txtSpherical = new Text(cmpTexmap, SWT.SEARCH);
                            txtSpherical.setMessage(I18n.META_TEXTURE_Z3);
                            txtSpherical.setToolTipText(I18n.META_TEXTURE_TOP_CENTER);
                            evTexmapSphere9TxtPtr[0] = txtSpherical;
                        }
                        {
                            Text txtSpherical = new Text(cmpTexmap, SWT.SEARCH);
                            txtSpherical.setMessage(I18n.META_TEXTURE_ANGLE_1);
                            evTexmapSphere10TxtPtr[0] = txtSpherical;
                        }
                        {
                            Text txtSpherical = new Text(cmpTexmap, SWT.SEARCH);
                            txtSpherical.setMessage(I18n.META_TEXTURE_ANGLE_2);
                            evTexmapSphere11TxtPtr[0] = txtSpherical;
                        }
                        {
                            Text txtPng = new Text(cmpTexmap, SWT.SEARCH);
                            txtPng.setMessage(I18n.META_TEXTURE_PNG);
                            evTexmapSphere12TxtPtr[0] = txtPng;
                        }
                        {
                            NButton btnBrowse = new NButton(cmpTexmap, SWT.NONE);
                            btnBrowse.setText(I18n.DIALOG_BROWSE);
                            evTexmapSphereBtnPtr[0] = btnBrowse;
                        }
                    }
                    {
                        NButton btnTexmap = new NButton(grpMeta, SWT.NONE);
                        btnTexmap.setText("0 !TEXMAP FALLBACK"); //$NON-NLS-1$
                        evTexmapFallbackBtnPtr[0] = btnTexmap;
                    }
                    {
                        Composite cmpTexmap = new Composite(grpMeta, SWT.NONE);
                        cmpTexmap.setLayout(new GridLayout(2, false));
                        Label lblTexmap = new Label(cmpTexmap, SWT.NONE);
                        lblTexmap.setText("0 !: "); //$NON-NLS-1$
                        {
                            Text txtMeta = new Text(cmpTexmap, SWT.SEARCH);
                            txtMeta.setMessage(I18n.META_TEXTURE_GEOM_1);
                            txtMeta.setToolTipText(I18n.META_TEXTURE_GEOM_2);
                            evTexmapMetaTxtPtr[0] = txtMeta;
                        }
                    }
                    {
                        NButton btnTexmap = new NButton(grpMeta, SWT.NONE);
                        btnTexmap.setText("0 !TEXMAP END"); //$NON-NLS-1$
                        evTexmapEndBtnPtr[0] = btnTexmap;
                    }
                }
            }

            final CTabItem tItem4 = new CTabItem(tabFolderSettings, SWT.NONE);
            tItem4.setText(I18n.META_LPE);
            {

                final ScrolledComposite cmpScroll = new ScrolledComposite(tabFolderSettings, SWT.H_SCROLL | SWT.V_SCROLL);
                tItem4.setControl(cmpScroll);

                Composite cmpMetaArea = new Composite(cmpScroll, SWT.NONE);
                cmpScroll.setContent(cmpMetaArea);
                cmpScroll.setExpandHorizontal(true);
                cmpScroll.setExpandVertical(true);
                cmpScroll.setMinSize(600, 800);

                GridData gdm = new GridData();
                gdm.grabExcessHorizontalSpace = true;
                cmpScroll.setLayoutData(gdm);

                cmpMetaArea.setLayout(new GridLayout(1, false));

                {
                    Composite grpMeta = cmpMetaArea;
                    grpMeta.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                    grpMeta.setLayout(new GridLayout(1, false));

                    {
                        Composite cmpLpe = new Composite(grpMeta, SWT.NONE);
                        cmpLpe.setLayout(new GridLayout(2, false));
                        Label lblTodo = new Label(cmpLpe, SWT.NONE);
                        lblTodo.setText("0 !LPE TODO "); //$NON-NLS-1$
                        {
                            Text txtTodo = new Text(cmpLpe, SWT.SEARCH);
                            txtTodo.setMessage(I18n.META_TODO);
                            evTodoTxtPtr[0] = txtTodo;
                        }
                    }

                    {
                        Composite cmpLpe = new Composite(grpMeta, SWT.NONE);
                        cmpLpe.setLayout(new GridLayout(4, false));
                        Label lblVertex = new Label(cmpLpe, SWT.NONE);
                        lblVertex.setText("0 !LPE VERTEX "); //$NON-NLS-1$
                        {
                            Text txtX = new Text(cmpLpe, SWT.SEARCH);
                            txtX.setMessage(I18n.META_VERTEX_X);
                            txtX.setToolTipText(I18n.META_DECIMAL_MARK);
                            evVertex1TxtPtr[0] = txtX;
                        }
                        {
                            Text txtY = new Text(cmpLpe, SWT.SEARCH);
                            txtY.setMessage(I18n.META_VERTEX_Y);
                            txtY.setToolTipText(I18n.META_DECIMAL_MARK);
                            evVertex2TxtPtr[0] = txtY;
                        }
                        {
                            Text txtZ = new Text(cmpLpe, SWT.SEARCH);
                            txtZ.setMessage(I18n.META_VERTEX_Z);
                            txtZ.setToolTipText(I18n.META_DECIMAL_MARK);
                            evVertex3TxtPtr[0] = txtZ;
                        }
                    }

                    {
                        Composite cmpLpe = new Composite(grpMeta, SWT.NONE);
                        cmpLpe.setLayout(new GridLayout(5, false));
                        Label lblVertex = new Label(cmpLpe, SWT.NONE);
                        lblVertex.setText("0 !LPE CSG_"); //$NON-NLS-1$
                        {
                            Combo cmbCsg = new Combo(cmpLpe, SWT.NONE);
                            widgetUtil(cmbCsg).setItems("UNION ", "DIFFERENCE ", "INTERSECTION "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            cmbCsg.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                            cmbCsg.setText("UNION "); //$NON-NLS-1$
                            cmbCsg.select(0);
                            evCsgActionCmbPtr[0] = cmbCsg;
                        }
                        {
                            Text txtCsgid1 = new Text(cmpLpe, SWT.SEARCH);
                            txtCsgid1.setMessage(I18n.META_CSG_SOURCE_1);
                            evCsgAction1TxtPtr[0] = txtCsgid1;
                        }
                        {
                            Text txtCsgid2 = new Text(cmpLpe, SWT.SEARCH);
                            txtCsgid2.setMessage(I18n.META_CSG_SOURCE_2);
                            evCsgAction2TxtPtr[0] = txtCsgid2;
                        }
                        {
                            Text txtCsgid3 = new Text(cmpLpe, SWT.SEARCH);
                            txtCsgid3.setMessage(I18n.META_CSG_TARGET_1);
                            evCsgAction3TxtPtr[0] = txtCsgid3;
                        }
                    }
                    {
                        Composite cmpLpe = new Composite(grpMeta, SWT.NONE);
                        cmpLpe.setLayout(new GridLayout(16, false));
                        Label lblVertex = new Label(cmpLpe, SWT.NONE);
                        lblVertex.setText("0 !LPE CSG_"); //$NON-NLS-1$
                        {
                            Combo cmbCsg = new Combo(cmpLpe, SWT.NONE);
                            widgetUtil(cmbCsg).setItems("CUBOID ", "ELLIPSOID ", "QUAD ", "CYLINDER ", "CONE ", "CIRCLE ", "MESH ", "EXTRUDE "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
                            cmbCsg.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                            cmbCsg.setText("CUBOID "); //$NON-NLS-1$
                            cmbCsg.select(0);
                            evCsgBodyCmbPtr[0] = cmbCsg;
                        }
                        {
                            Text txtCsgid1 = new Text(cmpLpe, SWT.SEARCH);
                            txtCsgid1.setMessage(I18n.META_CSG_UNIQUE);
                            txtCsgid1.setToolTipText(I18n.META_CSG_UNIQUE_HINT);
                            evCsgBody1TxtPtr[0] = txtCsgid1;
                        }
                        {
                            Text txtCsgid2 = new Text(cmpLpe, SWT.SEARCH);
                            txtCsgid2.setMessage(I18n.META_COLOUR);
                            txtCsgid2.setToolTipText(I18n.META_COLOUR_HINT);
                            evCsgBody2TxtPtr[0] = txtCsgid2;

                        }
                        {
                            Text txtX = new Text(cmpLpe, SWT.SEARCH);
                            txtX.setMessage(I18n.META_VERTEX_X);
                            txtX.setToolTipText(I18n.META_DECIMAL_MARK);
                            evCsgBody3TxtPtr[0] = txtX;
                        }
                        {
                            Text txtY = new Text(cmpLpe, SWT.SEARCH);
                            txtY.setMessage(I18n.META_VERTEX_Y);
                            txtY.setToolTipText(I18n.META_DECIMAL_MARK);
                            evCsgBody4TxtPtr[0] = txtY;
                        }
                        {
                            Text txtZ = new Text(cmpLpe, SWT.SEARCH);
                            txtZ.setMessage(I18n.META_VERTEX_Z);
                            txtZ.setToolTipText(I18n.META_DECIMAL_MARK);
                            evCsgBody5TxtPtr[0] = txtZ;
                        }

                        {
                            Text txtM = new Text(cmpLpe, SWT.SEARCH);
                            txtM.setMessage(I18n.META_M00);
                            txtM.setToolTipText(I18n.META_TRANS_MATRIX);
                            evCsgBody6TxtPtr[0] = txtM;
                        }
                        {
                            Text txtM = new Text(cmpLpe, SWT.SEARCH);
                            txtM.setMessage(I18n.META_M01);
                            txtM.setToolTipText(I18n.META_TRANS_MATRIX);
                            evCsgBody7TxtPtr[0] = txtM;
                        }
                        {
                            Text txtM = new Text(cmpLpe, SWT.SEARCH);
                            txtM.setMessage(I18n.META_M02);
                            txtM.setToolTipText(I18n.META_TRANS_MATRIX);
                            evCsgBody8TxtPtr[0] = txtM;
                        }

                        {
                            Text txtM = new Text(cmpLpe, SWT.SEARCH);
                            txtM.setMessage(I18n.META_M10);
                            txtM.setToolTipText(I18n.META_TRANS_MATRIX);
                            evCsgBody9TxtPtr[0] = txtM;
                        }
                        {
                            Text txtM = new Text(cmpLpe, SWT.SEARCH);
                            txtM.setMessage(I18n.META_M11);
                            txtM.setToolTipText(I18n.META_TRANS_MATRIX);
                            evCsgBody10TxtPtr[0] = txtM;
                        }
                        {
                            Text txtM = new Text(cmpLpe, SWT.SEARCH);
                            txtM.setMessage(I18n.META_M12);
                            txtM.setToolTipText(I18n.META_TRANS_MATRIX);
                            evCsgBody11TxtPtr[0] = txtM;
                        }

                        {
                            Text txtM = new Text(cmpLpe, SWT.SEARCH);
                            txtM.setMessage(I18n.META_M20);
                            txtM.setToolTipText(I18n.META_TRANS_MATRIX);
                            evCsgBody12TxtPtr[0] = txtM;
                        }
                        {
                            Text txtM = new Text(cmpLpe, SWT.SEARCH);
                            txtM.setMessage(I18n.META_M21);
                            txtM.setToolTipText(I18n.META_TRANS_MATRIX);
                            evCsgBody13TxtPtr[0] = txtM;
                        }
                        {
                            Text txtM = new Text(cmpLpe, SWT.SEARCH);
                            txtM.setMessage(I18n.META_M22);
                            txtM.setToolTipText(I18n.META_TRANS_MATRIX);
                            evCsgBody14TxtPtr[0] = txtM;
                        }
                    }
                    {
                        Composite cmpLpe = new Composite(grpMeta, SWT.NONE);
                        cmpLpe.setLayout(new GridLayout(16, false));
                        Label lblCsgTransform = new Label(cmpLpe, SWT.NONE);
                        lblCsgTransform.setText("0 !LPE CSG_TRANSFORM "); //$NON-NLS-1$
                        {
                            Text txtCsgid1 = new Text(cmpLpe, SWT.SEARCH);
                            txtCsgid1.setMessage(I18n.META_CSG_SOURCE_1);
                            txtCsgid1.setToolTipText(I18n.META_CSG_UNIQUE_HINT);
                            evCsgTrans1TxtPtr[0] = txtCsgid1;
                        }
                        {
                            Text txtCsgid2 = new Text(cmpLpe, SWT.SEARCH);
                            txtCsgid2.setMessage(I18n.META_CSG_TARGET_1);
                            txtCsgid2.setToolTipText(I18n.META_CSG_UNIQUE_HINT);
                            evCsgTrans2TxtPtr[0] = txtCsgid2;

                        }
                        {
                            Text txtCsgcol = new Text(cmpLpe, SWT.SEARCH);
                            txtCsgcol.setMessage(I18n.META_COLOUR);
                            txtCsgcol.setToolTipText(I18n.META_COLOUR_HINT);
                            evCsgTrans3TxtPtr[0] = txtCsgcol;
                        }
                        {
                            Text txtX = new Text(cmpLpe, SWT.SEARCH);
                            txtX.setMessage(I18n.META_VERTEX_X);
                            txtX.setToolTipText(I18n.META_DECIMAL_MARK);
                            evCsgTrans4TxtPtr[0] = txtX;
                        }
                        {
                            Text txtY = new Text(cmpLpe, SWT.SEARCH);
                            txtY.setMessage(I18n.META_VERTEX_Y);
                            txtY.setToolTipText(I18n.META_DECIMAL_MARK);
                            evCsgTrans5TxtPtr[0] = txtY;
                        }
                        {
                            Text txtZ = new Text(cmpLpe, SWT.SEARCH);
                            txtZ.setMessage(I18n.META_VERTEX_Z);
                            txtZ.setToolTipText(I18n.META_DECIMAL_MARK);
                            evCsgTrans6TxtPtr[0] = txtZ;
                        }

                        {
                            Text txtM = new Text(cmpLpe, SWT.SEARCH);
                            txtM.setMessage(I18n.META_M00);
                            txtM.setToolTipText(I18n.META_TRANS_MATRIX);
                            evCsgTrans7TxtPtr[0] = txtM;
                        }
                        {
                            Text txtM = new Text(cmpLpe, SWT.SEARCH);
                            txtM.setMessage(I18n.META_M01);
                            txtM.setToolTipText(I18n.META_TRANS_MATRIX);
                            evCsgTrans8TxtPtr[0] = txtM;
                        }
                        {
                            Text txtM = new Text(cmpLpe, SWT.SEARCH);
                            txtM.setMessage(I18n.META_M02);
                            txtM.setToolTipText(I18n.META_TRANS_MATRIX);
                            evCsgTrans9TxtPtr[0] = txtM;
                        }

                        {
                            Text txtM = new Text(cmpLpe, SWT.SEARCH);
                            txtM.setMessage(I18n.META_M10);
                            txtM.setToolTipText(I18n.META_TRANS_MATRIX);
                            evCsgTrans10TxtPtr[0] = txtM;
                        }
                        {
                            Text txtM = new Text(cmpLpe, SWT.SEARCH);
                            txtM.setMessage(I18n.META_M11);
                            txtM.setToolTipText(I18n.META_TRANS_MATRIX);
                            evCsgTrans11TxtPtr[0] = txtM;
                        }
                        {
                            Text txtM = new Text(cmpLpe, SWT.SEARCH);
                            txtM.setMessage(I18n.META_M12);
                            txtM.setToolTipText(I18n.META_TRANS_MATRIX);
                            evCsgTrans12TxtPtr[0] = txtM;
                        }

                        {
                            Text txtM = new Text(cmpLpe, SWT.SEARCH);
                            txtM.setMessage(I18n.META_M20);
                            txtM.setToolTipText(I18n.META_TRANS_MATRIX);
                            evCsgTrans13TxtPtr[0] = txtM;
                        }
                        {
                            Text txtM = new Text(cmpLpe, SWT.SEARCH);
                            txtM.setMessage(I18n.META_M21);
                            txtM.setToolTipText(I18n.META_TRANS_MATRIX);
                            evCsgTrans14TxtPtr[0] = txtM;
                        }
                        {
                            Text txtM = new Text(cmpLpe, SWT.SEARCH);
                            txtM.setMessage(I18n.META_M22);
                            txtM.setToolTipText(I18n.META_TRANS_MATRIX);
                            evCsgTrans15TxtPtr[0] = txtM;
                        }
                    }

                    {
                        Composite cmpLpe = new Composite(grpMeta, SWT.NONE);
                        cmpLpe.setLayout(new GridLayout(16, false));
                        Label lblVertex = new Label(cmpLpe, SWT.NONE);
                        lblVertex.setText("0 !LPE CSG_EXT_CFG "); //$NON-NLS-1$
                        {
                            Text txtCsgid1 = new Text(cmpLpe, SWT.SEARCH);
                            txtCsgid1.setMessage(I18n.META_CSG_EXTRUDE_1_A);
                            txtCsgid1.setToolTipText(I18n.META_CSG_EXTRUDE_1_B);
                            evCsgEx1TxtPtr[0] = txtCsgid1;
                        }
                        {
                            Text txtCsgid2 = new Text(cmpLpe, SWT.SEARCH);
                            txtCsgid2.setMessage(I18n.META_CSG_EXTRUDE_2_A);
                            txtCsgid2.setToolTipText(I18n.META_CSG_EXTRUDE_2_B);
                            evCsgEx2TxtPtr[0] = txtCsgid2;

                        }
                        {
                            Text txtCsgcol = new Text(cmpLpe, SWT.SEARCH);
                            txtCsgcol.setMessage(I18n.META_CSG_EXTRUDE_3_A);
                            txtCsgcol.setToolTipText(I18n.META_CSG_EXTRUDE_3_B);
                            evCsgEx3TxtPtr[0] = txtCsgcol;

                        }
                        {
                            Text txtX = new Text(cmpLpe, SWT.SEARCH);
                            txtX.setMessage(I18n.META_CSG_EXTRUDE_4_A);
                            txtX.setToolTipText(I18n.META_CSG_EXTRUDE_4_B);
                            evCsgEx4TxtPtr[0] = txtX;
                        }
                        {
                            Text txtY = new Text(cmpLpe, SWT.SEARCH);
                            txtY.setMessage(I18n.META_CSG_EXTRUDE_5_A);
                            txtY.setToolTipText(I18n.META_CSG_EXTRUDE_5_B);
                            evCsgEx5TxtPtr[0] = txtY;
                        }
                        {
                            Text txtZ = new Text(cmpLpe, SWT.SEARCH);
                            txtZ.setMessage(I18n.META_CSG_EXTRUDE_6_A);
                            txtZ.setToolTipText(I18n.META_CSG_EXTRUDE_6_B);
                            evCsgEx6TxtPtr[0] = txtZ;
                        }

                        {
                            Text txtM = new Text(cmpLpe, SWT.SEARCH);
                            txtM.setMessage(I18n.META_CSG_EXTRUDE_7_A);
                            txtM.setToolTipText(I18n.META_CSG_EXTRUDE_7_B);
                            evCsgEx7TxtPtr[0] = txtM;
                        }
                    }

                    {
                        Composite cmpLpe = new Composite(grpMeta, SWT.NONE);
                        cmpLpe.setLayout(new GridLayout(2, false));
                        Label lblVertex = new Label(cmpLpe, SWT.NONE);
                        lblVertex.setText("0 !LPE CSG_COMPILE"); //$NON-NLS-1$
                        {
                            Text txtCsgid1 = new Text(cmpLpe, SWT.SEARCH);
                            txtCsgid1.setMessage(I18n.META_CSG_SOURCE_3);
                            txtCsgid1.setToolTipText(I18n.META_CSG_COMPILE);
                            evCsgCompileTxtPtr[0] = txtCsgid1;
                        }
                    }

                    {
                        Composite cmpLpe = new Composite(grpMeta, SWT.NONE);
                        cmpLpe.setLayout(new GridLayout(2, false));
                        Label lblVertex = new Label(cmpLpe, SWT.NONE);
                        lblVertex.setText("0 !LPE CSG_QUALITY"); //$NON-NLS-1$
                        {
                            Text txtCsgQuality = new Text(cmpLpe, SWT.SEARCH);
                            txtCsgQuality.setMessage(I18n.META_QUALITY);
                            evCsgQualityTxtPtr[0] = txtCsgQuality;
                        }
                    }

                    {
                        Composite cmpLpe = new Composite(grpMeta, SWT.NONE);
                        cmpLpe.setLayout(new GridLayout(2, false));
                        Label lblVertex = new Label(cmpLpe, SWT.NONE);
                        lblVertex.setText("0 !LPE CSG_EPSILON"); //$NON-NLS-1$
                        {
                            Text txtCsgEpsilon = new Text(cmpLpe, SWT.SEARCH);
                            txtCsgEpsilon.setMessage(I18n.META_CSG_EPSILON_1);
                            txtCsgEpsilon.setToolTipText(I18n.META_CSG_EPSILON_2);
                            evCsgEpsilonTxtPtr[0] = txtCsgEpsilon;
                        }
                    }

                    {
                        Composite cmpLpe = new Composite(grpMeta, SWT.NONE);
                        cmpLpe.setLayout(new GridLayout(2, false));
                        Label lblVertex = new Label(cmpLpe, SWT.NONE);
                        lblVertex.setText("0 !LPE CSG_TJUNCTION_EPSILON"); //$NON-NLS-1$
                        {
                            Text txtCsgTJunctionEpsilon = new Text(cmpLpe, SWT.SEARCH);
                            txtCsgTJunctionEpsilon.setMessage(I18n.META_CSG_JUNCTION_EPSILON_1);
                            txtCsgTJunctionEpsilon.setToolTipText(I18n.META_CSG_JUNCTION_EPSILON_2);
                            evCsgTJunctionEpsilonTxtPtr[0] = txtCsgTJunctionEpsilon;
                        }
                    }

                    {
                        Composite cmpLpe = new Composite(grpMeta, SWT.NONE);
                        cmpLpe.setLayout(new GridLayout(2, false));
                        Label lblVertex = new Label(cmpLpe, SWT.NONE);
                        lblVertex.setText("0 !LPE CSG_EDGE_COLLAPSE_EPSILON"); //$NON-NLS-1$
                        {
                            Text txtCsgEdgeCollapseEpsilon = new Text(cmpLpe, SWT.SEARCH);
                            txtCsgEdgeCollapseEpsilon.setMessage(I18n.META_CSG_COLLAPSE_1);
                            txtCsgEdgeCollapseEpsilon.setToolTipText(I18n.META_CSG_COLLAPSE_2);
                            evCsgEdgeCollapseEpsilonTxtPtr[0] = txtCsgEdgeCollapseEpsilon;
                        }
                    }

                    {
                        NButton btnDontoptimize = new NButton(grpMeta, SWT.NONE);
                        btnDontoptimize.setText("0 !LPE CSG_DONT_OPTIMISE"); //$NON-NLS-1$
                        evCsgDontOptimizeBtnPtr[0] = btnDontoptimize;
                    }

                    {
                        Composite cmpLpe = new Composite(grpMeta, SWT.NONE);
                        cmpLpe.setLayout(new GridLayout(11, false));
                        Label lblPng = new Label(cmpLpe, SWT.NONE);
                        lblPng.setText("0 !LPE PNG "); //$NON-NLS-1$
                        {
                            Text txtX = new Text(cmpLpe, SWT.SEARCH);
                            txtX.setMessage(I18n.META_VERTEX_X);
                            txtX.setToolTipText(I18n.META_DECIMAL_MARK);
                            evPng1TxtPtr[0] = txtX;
                        }
                        {
                            Text txtY = new Text(cmpLpe, SWT.SEARCH);
                            txtY.setMessage(I18n.META_VERTEX_Y);
                            txtY.setToolTipText(I18n.META_DECIMAL_MARK);
                            evPng2TxtPtr[0] = txtY;
                        }
                        {
                            Text txtZ = new Text(cmpLpe, SWT.SEARCH);
                            txtZ.setMessage(I18n.META_VERTEX_Z);
                            txtZ.setToolTipText(I18n.META_DECIMAL_MARK);
                            evPng3TxtPtr[0] = txtZ;
                        }

                        {
                            Text txtX = new Text(cmpLpe, SWT.SEARCH);
                            txtX.setMessage(I18n.META_ROTATION_X);
                            txtX.setToolTipText(I18n.META_DECIMAL_MARK);
                            evPng4TxtPtr[0] = txtX;
                        }
                        {
                            Text txtY = new Text(cmpLpe, SWT.SEARCH);
                            txtY.setMessage(I18n.META_ROTATION_Y);
                            txtY.setToolTipText(I18n.META_DECIMAL_MARK);
                            evPng5TxtPtr[0] = txtY;
                        }
                        {
                            Text txtZ = new Text(cmpLpe, SWT.SEARCH);
                            txtZ.setMessage(I18n.META_ROTATION_Z);
                            txtZ.setToolTipText(I18n.META_DECIMAL_MARK);
                            evPng6TxtPtr[0] = txtZ;
                        }

                        {
                            Text txtX = new Text(cmpLpe, SWT.SEARCH);
                            txtX.setMessage(I18n.META_SCALE_X);
                            txtX.setToolTipText(I18n.META_DECIMAL_MARK);
                            evPng7TxtPtr[0] = txtX;
                        }
                        {
                            Text txtY = new Text(cmpLpe, SWT.SEARCH);
                            txtY.setMessage(I18n.META_SCALE_Y);
                            txtY.setToolTipText(I18n.META_DECIMAL_MARK);
                            evPng8TxtPtr[0] = txtY;
                        }
                        {
                            Text txtPng = new Text(cmpLpe, SWT.SEARCH);
                            txtPng.setMessage(I18n.META_TEXTURE_PNG);
                            evPng9TxtPtr[0] = txtPng;
                        }
                        {
                            NButton btnBrowse = new NButton(cmpLpe, SWT.NONE);
                            btnBrowse.setText(I18n.DIALOG_BROWSE);
                            evPngBtnPtr[0] = btnBrowse;
                        }
                    }
                }
            }

        }

        Label lblOnlyFor3D = new Label(container, SWT.NONE);
        lblOnlyFor3D.setText(I18n.META_NEW_LINE_NOTE);

        Label lblPreview = new Label(container, SWT.BORDER);
        lblPreview.setText("0 BFC CERTIFY CCW"); //$NON-NLS-1$
        lblLineToInsertPtr[0] = lblPreview;
        GridData gdl = new GridData();
        gdl.horizontalAlignment = SWT.CENTER;
        lblPreview.setLayoutData(gdl);


        NButton btnCreate = new NButton(container, SWT.NONE);
        btnCreate.setText(I18n.DIALOG_CREATE_META_COMMAND);
        GridData gdt = new GridData();
        gdt.horizontalAlignment = SWT.RIGHT;
        btnCreate.setLayoutData(gdt);
        this.btnCreatePtr[0] = btnCreate;

        return container;
    }

    private void setDefaultCategories() {
        widgetUtil(evCategoryCmbPtr[0]).setItems(
                "", //$NON-NLS-1$
                "Animal", //$NON-NLS-1$
                "Antenna", //$NON-NLS-1$
                "Arch", //$NON-NLS-1$
                "Arm", //$NON-NLS-1$
                "Bar", //$NON-NLS-1$
                "Baseplate", //$NON-NLS-1$
                "Belville", //$NON-NLS-1$
                "Boat", //$NON-NLS-1$
                "Bracket", //$NON-NLS-1$
                "Brick", //$NON-NLS-1$
                "Canvas", //$NON-NLS-1$
                "Car", //$NON-NLS-1$
                "Clikits", //$NON-NLS-1$
                "Cockpit", //$NON-NLS-1$
                "Cone", //$NON-NLS-1$
                "Container", //$NON-NLS-1$
                "Conveyor", //$NON-NLS-1$
                "Crane", //$NON-NLS-1$
                "Cylinder", //$NON-NLS-1$
                "Dish", //$NON-NLS-1$
                "Door", //$NON-NLS-1$
                "Electric", //$NON-NLS-1$
                "Exhaust", //$NON-NLS-1$
                "Fence", //$NON-NLS-1$
                "Figure", //$NON-NLS-1$
                "Figure Accessory", //$NON-NLS-1$
                "Flag", //$NON-NLS-1$
                "Forklift", //$NON-NLS-1$
                "Freestyle", //$NON-NLS-1$
                "Garage", //$NON-NLS-1$
                "Glass", //$NON-NLS-1$
                "Grab", //$NON-NLS-1$
                "Hinge", //$NON-NLS-1$
                "Homemaker", //$NON-NLS-1$
                "Hose", //$NON-NLS-1$
                "Ladder", //$NON-NLS-1$
                "Lever", //$NON-NLS-1$
                "Magnet", //$NON-NLS-1$
                "Minifig", //$NON-NLS-1$
                "Minifig Accessory", //$NON-NLS-1$
                "Minifig Footwear", //$NON-NLS-1$
                "Minifig Headwear", //$NON-NLS-1$
                "Minifig Hipwear", //$NON-NLS-1$
                "Minifig Neckwear", //$NON-NLS-1$
                "Monorail", //$NON-NLS-1$
                "Panel", //$NON-NLS-1$
                "Plane", //$NON-NLS-1$
                "Plant", //$NON-NLS-1$
                "Plate", //$NON-NLS-1$
                "Platform", //$NON-NLS-1$
                "Propellor", //$NON-NLS-1$
                "Rack", //$NON-NLS-1$
                "Roadsign", //$NON-NLS-1$
                "Rock", //$NON-NLS-1$
                "Scala", //$NON-NLS-1$
                "Screw", //$NON-NLS-1$
                "Sheet", //$NON-NLS-1$
                "Slope", //$NON-NLS-1$
                "Sphere", //$NON-NLS-1$
                "Staircase", //$NON-NLS-1$
                "Sticker", //$NON-NLS-1$
                "Support", //$NON-NLS-1$
                "Tail", //$NON-NLS-1$
                "Tap", //$NON-NLS-1$
                "Technic", //$NON-NLS-1$
                "Tile", //$NON-NLS-1$
                "Tipper", //$NON-NLS-1$
                "Tractor", //$NON-NLS-1$
                "Trailer", //$NON-NLS-1$
                "Train", //$NON-NLS-1$
                "Turntable", //$NON-NLS-1$
                "Tyre", //$NON-NLS-1$
                "Vehicle", //$NON-NLS-1$
                "Wedge", //$NON-NLS-1$
                "Wheel", //$NON-NLS-1$
                "Winch", //$NON-NLS-1$
                "Window", //$NON-NLS-1$
                "Windscreen", //$NON-NLS-1$
                "Wing", //$NON-NLS-1$
                "Znap" //$NON-NLS-1$
        );
    }
}
