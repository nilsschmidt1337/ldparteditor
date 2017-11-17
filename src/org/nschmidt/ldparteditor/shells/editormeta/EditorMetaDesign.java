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
package org.nschmidt.ldparteditor.shells.editormeta;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.text.LDParsingException;
import org.nschmidt.ldparteditor.text.StringHelper;
import org.nschmidt.ldparteditor.text.UTF8BufferedReader;
import org.nschmidt.ldparteditor.widgets.NButton;
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

    final NButton[] btn_Create = new NButton[1];

    final Text[] ev_description_txt = new Text[1];
    final NButton[] ev_description_btn = new NButton[1];
    final Text[] ev_name_txt = new Text[1];
    final Text[] ev_author_realName_txt = new Text[1];
    final Text[] ev_author_userName_txt = new Text[1];
    final NButton[] ev_type_unofficial_btn = new NButton[1];
    final Combo[] ev_type_type_cmb = new Combo[1];
    final NButton[] ev_type_update_btn = new NButton[1];
    final Text[] ev_type_update_txt  = new Text[1];
    final Combo[] ev_license_cmb = new Combo[1];
    final Text[] ev_help_txt = new Text[1];
    final Combo[] ev_bfcHeader_cmb = new Combo[1];
    final Combo[] ev_category_cmb = new Combo[1];
    final Text[] ev_keywords_txt = new Text[1];
    final Text[] ev_cmdline_txt = new Text[1];
    final Text[] ev_history11_txt = new Text[1];
    final Text[] ev_history12_txt = new Text[1];
    final Text[] ev_history13_txt = new Text[1];
    final Text[] ev_history21_txt = new Text[1];
    final Text[] ev_history22_txt = new Text[1];
    final Text[] ev_history23_txt = new Text[1];
    final NButton[] ev_comment_btn = new NButton[1];
    final Text[] ev_comment_txt  = new Text[1];
    final Combo[] ev_bfc_cmb = new Combo[1];
    final Combo[] ev_texmapPlanar_cmb = new Combo[1];
    final Text[] ev_texmapPlanar1_txt  = new Text[1];
    final Text[] ev_texmapPlanar2_txt  = new Text[1];
    final Text[] ev_texmapPlanar3_txt  = new Text[1];
    final Text[] ev_texmapPlanar4_txt  = new Text[1];
    final Text[] ev_texmapPlanar5_txt  = new Text[1];
    final Text[] ev_texmapPlanar6_txt  = new Text[1];
    final Text[] ev_texmapPlanar7_txt  = new Text[1];
    final Text[] ev_texmapPlanar8_txt  = new Text[1];
    final Text[] ev_texmapPlanar9_txt  = new Text[1];
    final Text[] ev_texmapPlanar10_txt  = new Text[1];
    final NButton[] ev_texmapPlanar_btn = new NButton[1];
    final Combo[] ev_texmapCyli_cmb = new Combo[1];
    final Text[] ev_texmapCyli1_txt  = new Text[1];
    final Text[] ev_texmapCyli2_txt  = new Text[1];
    final Text[] ev_texmapCyli3_txt  = new Text[1];
    final Text[] ev_texmapCyli4_txt  = new Text[1];
    final Text[] ev_texmapCyli5_txt  = new Text[1];
    final Text[] ev_texmapCyli6_txt  = new Text[1];
    final Text[] ev_texmapCyli7_txt  = new Text[1];
    final Text[] ev_texmapCyli8_txt  = new Text[1];
    final Text[] ev_texmapCyli9_txt  = new Text[1];
    final Text[] ev_texmapCyli10_txt  = new Text[1];
    final Text[] ev_texmapCyli11_txt  = new Text[1];
    final NButton[] ev_texmapCyli_btn = new NButton[1];
    final Combo[] ev_texmapSphere_cmb = new Combo[1];
    final Text[] ev_texmapSphere1_txt  = new Text[1];
    final Text[] ev_texmapSphere2_txt  = new Text[1];
    final Text[] ev_texmapSphere3_txt  = new Text[1];
    final Text[] ev_texmapSphere4_txt  = new Text[1];
    final Text[] ev_texmapSphere5_txt  = new Text[1];
    final Text[] ev_texmapSphere6_txt  = new Text[1];
    final Text[] ev_texmapSphere7_txt  = new Text[1];
    final Text[] ev_texmapSphere8_txt  = new Text[1];
    final Text[] ev_texmapSphere9_txt  = new Text[1];
    final Text[] ev_texmapSphere10_txt  = new Text[1];
    final Text[] ev_texmapSphere11_txt  = new Text[1];
    final Text[] ev_texmapSphere12_txt  = new Text[1];
    final NButton[] ev_texmapSphere_btn = new NButton[1];
    final NButton[] ev_texmapFallback_btn = new NButton[1];
    final Text[] ev_texmapMeta_txt  = new Text[1];
    final NButton[] ev_texmapEnd_btn = new NButton[1];
    final Text[] ev_todo_txt  = new Text[1];
    final Text[] ev_vertex1_txt  = new Text[1];
    final Text[] ev_vertex2_txt  = new Text[1];
    final Text[] ev_vertex3_txt  = new Text[1];
    final Combo[] ev_csgAction_cmb = new Combo[1];
    final Text[] ev_csgAction1_txt  = new Text[1];
    final Text[] ev_csgAction2_txt  = new Text[1];
    final Text[] ev_csgAction3_txt  = new Text[1];
    final Combo[] ev_csgBody_cmb = new Combo[1];
    final Text[] ev_csgBody1_txt  = new Text[1];
    final Text[] ev_csgBody2_txt  = new Text[1];
    final Text[] ev_csgBody3_txt  = new Text[1];
    final Text[] ev_csgBody4_txt  = new Text[1];
    final Text[] ev_csgBody5_txt  = new Text[1];
    final Text[] ev_csgBody6_txt  = new Text[1];
    final Text[] ev_csgBody7_txt  = new Text[1];
    final Text[] ev_csgBody8_txt  = new Text[1];
    final Text[] ev_csgBody9_txt  = new Text[1];
    final Text[] ev_csgBody10_txt  = new Text[1];
    final Text[] ev_csgBody11_txt  = new Text[1];
    final Text[] ev_csgBody12_txt  = new Text[1];
    final Text[] ev_csgBody13_txt  = new Text[1];
    final Text[] ev_csgBody14_txt  = new Text[1];
    final Text[] ev_csgTrans1_txt  = new Text[1];
    final Text[] ev_csgTrans2_txt  = new Text[1];
    final Text[] ev_csgTrans3_txt  = new Text[1];
    final Text[] ev_csgTrans4_txt  = new Text[1];
    final Text[] ev_csgTrans5_txt  = new Text[1];
    final Text[] ev_csgTrans6_txt  = new Text[1];
    final Text[] ev_csgTrans7_txt  = new Text[1];
    final Text[] ev_csgTrans8_txt  = new Text[1];
    final Text[] ev_csgTrans9_txt  = new Text[1];
    final Text[] ev_csgTrans10_txt  = new Text[1];
    final Text[] ev_csgTrans11_txt  = new Text[1];
    final Text[] ev_csgTrans12_txt  = new Text[1];
    final Text[] ev_csgTrans13_txt  = new Text[1];
    final Text[] ev_csgTrans14_txt  = new Text[1];
    final Text[] ev_csgTrans15_txt  = new Text[1];
    final Text[] ev_csgEx1_txt = new Text[1];
    final Text[] ev_csgEx2_txt = new Text[1];
    final Text[] ev_csgEx3_txt = new Text[1];
    final Text[] ev_csgEx4_txt = new Text[1];
    final Text[] ev_csgEx5_txt = new Text[1];
    final Text[] ev_csgEx6_txt = new Text[1];
    final Text[] ev_csgEx7_txt = new Text[1];
    final Text[] ev_csgCompile_txt  = new Text[1];
    final Text[] ev_csgQuality_txt  = new Text[1];
    final Text[] ev_csgEpsilon_txt  = new Text[1];
    final Text[] ev_csgTJunctionEpsilon_txt  = new Text[1];
    final Text[] ev_csgEdgeCollapseEpsilon_txt  = new Text[1];
    final Text[] ev_png1_txt  = new Text[1];
    final Text[] ev_png2_txt  = new Text[1];
    final Text[] ev_png3_txt  = new Text[1];
    final Text[] ev_png4_txt  = new Text[1];
    final Text[] ev_png5_txt  = new Text[1];
    final Text[] ev_png6_txt  = new Text[1];
    final Text[] ev_png7_txt  = new Text[1];
    final Text[] ev_png8_txt  = new Text[1];
    final Text[] ev_png9_txt  = new Text[1];
    final NButton[] ev_png_btn = new NButton[1];

    final Label[] lbl_lineToInsert  = new Label[1];

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

            final CTabItem tItem = new CTabItem(tabFolder_Settings, SWT.NONE);
            tItem.setText(I18n.META_LDrawHeader);
            {
                final ScrolledComposite cmp_scroll = new ScrolledComposite(tabFolder_Settings, SWT.H_SCROLL | SWT.V_SCROLL);
                tItem.setControl(cmp_scroll);

                Composite cmp_metaArea = new Composite(cmp_scroll, SWT.NONE);
                cmp_scroll.setContent(cmp_metaArea);
                cmp_scroll.setExpandHorizontal(true);
                cmp_scroll.setExpandVertical(true);
                cmp_scroll.setMinSize(600, 800);

                GridData gdm = new GridData();
                gdm.grabExcessHorizontalSpace = true;
                gdm.grabExcessVerticalSpace = true;
                cmp_scroll.setLayoutData(gdm);

                cmp_metaArea.setLayout(new GridLayout(1, false));

                {
                    Composite grp_meta = cmp_metaArea;
                    grp_meta.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                    grp_meta.setLayout(new GridLayout(1, false));

                    {
                        Composite cmp_description = new Composite(grp_meta, SWT.NONE);
                        cmp_description.setLayout(new GridLayout(3, false));

                        Label lbl_description = new Label(cmp_description, SWT.NONE);
                        lbl_description.setText("Description:"); //$NON-NLS-1$

                        Text txt_description = new Text(cmp_description, SWT.SEARCH);
                        txt_description.setMessage(I18n.META_Description);
                        ev_description_txt[0] = txt_description;

                        NButton btn_needsWork = new NButton(cmp_description, SWT.TOGGLE);
                        btn_needsWork.setText("(Needs Work)"); //$NON-NLS-1$
                        ev_description_btn[0] = btn_needsWork;
                    }

                    {
                        Composite cmp_name = new Composite(grp_meta, SWT.NONE);
                        cmp_name.setLayout(new GridLayout(2, false));

                        Label lbl_name = new Label(cmp_name, SWT.NONE);
                        lbl_name.setText("0 Name: "); //$NON-NLS-1$

                        Text txt_name = new Text(cmp_name, SWT.SEARCH);
                        txt_name.setMessage(I18n.META_Filename);
                        ev_name_txt[0] = txt_name;
                    }

                    {
                        Composite cmp_author = new Composite(grp_meta, SWT.NONE);
                        cmp_author.setLayout(new GridLayout(5, false));

                        Label lbl_author = new Label(cmp_author, SWT.NONE);
                        lbl_author.setText("0 Author: "); //$NON-NLS-1$

                        Text txt_realName = new Text(cmp_author, SWT.SEARCH);
                        txt_realName.setMessage(I18n.META_Author);
                        ev_author_realName_txt[0] = txt_realName;
                        if (userSettings.getRealUserName() != null) {
                            txt_realName.setText(userSettings.getRealUserName());
                        }

                        Label lbl_author2 = new Label(cmp_author, SWT.NONE);
                        lbl_author2.setText("["); //$NON-NLS-1$

                        Text txt_userName = new Text(cmp_author, SWT.SEARCH);
                        txt_userName.setMessage(I18n.META_Username);
                        ev_author_userName_txt[0] = txt_userName;

                        if (userSettings.getLdrawUserName() != null) {
                            txt_userName.setText(userSettings.getLdrawUserName());
                        }

                        Label lbl_author3 = new Label(cmp_author, SWT.NONE);
                        lbl_author3.setText("]"); //$NON-NLS-1$
                    }

                    {
                        Composite cmp_type = new Composite(grp_meta, SWT.NONE);
                        cmp_type.setLayout(new GridLayout(5, false));

                        Label lbl_type = new Label(cmp_type, SWT.NONE);
                        lbl_type.setText("0 !LDRAW_ORG "); //$NON-NLS-1$

                        NButton btn_unofficial = new NButton(cmp_type, SWT.TOGGLE);
                        btn_unofficial.setText("Unofficial"); //$NON-NLS-1$
                        ev_type_unofficial_btn[0] = btn_unofficial;

                        Combo cmb_type = new Combo(cmp_type, SWT.NONE);
                        cmb_type.setItems(new String[] { "Part", "Subpart", "Primitive", "8_Primitive", "48_Primitive", "Shortcut", "Part Alias", "Part Physical_Colour",  "Part Physical_Colour Alias", "Shortcut Alias", "Shortcut Physical_Colour",  "Shortcut Physical_Colour Alias"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$
                        cmb_type.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                        cmb_type.setText("Part"); //$NON-NLS-1$
                        cmb_type.select(0);
                        ev_type_type_cmb[0] = cmb_type;

                        NButton btn_update = new NButton(cmp_type, SWT.TOGGLE);
                        btn_update.setText("UPDATE"); //$NON-NLS-1$
                        ev_type_update_btn[0] = btn_update;

                        Text txt_update = new Text(cmp_type, SWT.SEARCH);
                        txt_update.setMessage(I18n.META_YearRelease);
                        txt_update.setEnabled(false);
                        ev_type_update_txt[0] = txt_update;
                    }

                    {
                        Composite cmp_license = new Composite(grp_meta, SWT.NONE);
                        cmp_license.setLayout(new GridLayout(2, false));

                        Label lbl_license = new Label(cmp_license, SWT.NONE);
                        lbl_license.setText("0 !LICENSE "); //$NON-NLS-1$

                        Combo cmb_license = new Combo(cmp_license, SWT.NONE);
                        cmb_license.setItems(new String[] { "Redistributable under CCAL version 2.0 : see CAreadme.txt", "Not redistributable : see NonCAreadme.txt" }); //$NON-NLS-1$ //$NON-NLS-2$
                        cmb_license.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                        cmb_license.setText(userSettings.getLicense() != null ? userSettings.getLicense() : "Redistributable under CCAL version 2.0 : see CAreadme.txt"); //$NON-NLS-1$
                        cmb_license.select(0);
                        ev_license_cmb[0] = cmb_license;
                    }

                    {
                        Composite cmp_help = new Composite(grp_meta, SWT.NONE);
                        cmp_help.setLayout(new GridLayout(2, false));

                        Label lbl_help = new Label(cmp_help, SWT.NONE);
                        lbl_help.setText("0 !HELP "); //$NON-NLS-1$

                        Text txt_help = new Text(cmp_help, SWT.SEARCH);
                        txt_help.setMessage(I18n.META_Help);
                        ev_help_txt[0] = txt_help;
                    }

                    {
                        Composite cmp_bfc = new Composite(grp_meta, SWT.NONE);
                        cmp_bfc.setLayout(new GridLayout(2, false));

                        Label lbl_bfc = new Label(cmp_bfc, SWT.NONE);
                        lbl_bfc.setText("0 BFC "); //$NON-NLS-1$

                        Combo cmb_bfc = new Combo(cmp_bfc, SWT.NONE);
                        cmb_bfc.setItems(new String[] { "NOCERTIFY", "CERTIFY CW", "CERTIFY CCW"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        cmb_bfc.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                        cmb_bfc.setText("NOCERTIFY"); //$NON-NLS-1$
                        cmb_bfc.select(0);
                        ev_bfcHeader_cmb[0] = cmb_bfc;
                    }

                    {
                        Composite cmp_category = new Composite(grp_meta, SWT.NONE);
                        cmp_category.setLayout(new GridLayout(2, false));

                        Label lbl_category = new Label(cmp_category, SWT.NONE);
                        lbl_category.setText("0 !CATEGORY "); //$NON-NLS-1$

                        Combo cmb_category = new Combo(cmp_category, SWT.NONE);
                        ev_category_cmb[0] = cmb_category;
                        File categoryFile = new File("categories.txt"); //$NON-NLS-1$
                        if (categoryFile.exists() && categoryFile.isFile()) {
                            UTF8BufferedReader reader = null;
                            try {
                                ArrayList<String> categories = new ArrayList<String>();
                                categories.add(""); //$NON-NLS-1$
                                reader = new UTF8BufferedReader(categoryFile.getAbsolutePath());
                                String line ;
                                while ((line = reader.readLine()) != null) {
                                    line = line.trim();
                                    if (StringHelper.isNotBlank(line)) {
                                        categories.add(line);
                                    }
                                }
                                ev_category_cmb[0].setItems(categories.toArray(new String[categories.size()]));
                            } catch (LDParsingException e) {
                                setDefaultCategories();
                            } catch (FileNotFoundException e) {
                                setDefaultCategories();
                            } catch (UnsupportedEncodingException e) {
                                setDefaultCategories();
                            } finally {
                                try {
                                    if (reader != null)
                                        reader.close();
                                } catch (LDParsingException e1) {
                                }
                            }
                        } else {
                            setDefaultCategories();
                        }

                        cmb_category.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                        cmb_category.setText(""); //$NON-NLS-1$
                        cmb_category.select(0);
                    }

                    {
                        Composite cmp_keywords = new Composite(grp_meta, SWT.NONE);
                        cmp_keywords.setLayout(new GridLayout(3, false));

                        Label lbl_keywords = new Label(cmp_keywords, SWT.NONE);
                        lbl_keywords.setText("0 !KEYWORDS "); //$NON-NLS-1$

                        Text txt_keywords = new Text(cmp_keywords, SWT.SEARCH);
                        txt_keywords.setMessage(I18n.META_Keywords1);
                        ev_keywords_txt[0] = txt_keywords;

                        Label lbl_keywords2 = new Label(cmp_keywords, SWT.NONE);
                        lbl_keywords2.setText(I18n.META_Keywords2);
                    }

                    {
                        Composite cmp_cmdline = new Composite(grp_meta, SWT.NONE);
                        cmp_cmdline.setLayout(new GridLayout(3, false));

                        Label lbl_cmdline = new Label(cmp_cmdline, SWT.NONE);
                        lbl_cmdline.setText("0 !CMDLINE "); //$NON-NLS-1$

                        Text txt_cmdline = new Text(cmp_cmdline, SWT.SEARCH);
                        txt_cmdline.setMessage(I18n.META_CommandLine);
                        ev_cmdline_txt[0] = txt_cmdline;
                    }

                    {
                        Composite cmp_history1 = new Composite(grp_meta, SWT.NONE);
                        cmp_history1.setLayout(new GridLayout(6, false));

                        Label lbl_history11 = new Label(cmp_history1, SWT.NONE);
                        lbl_history11.setText("0 !HISTORY "); //$NON-NLS-1$

                        Text txt_history11 = new Text(cmp_history1, SWT.SEARCH);
                        txt_history11.setMessage(I18n.META_History1);
                        ev_history11_txt[0] = txt_history11;

                        Label lbl_history12 = new Label(cmp_history1, SWT.NONE);
                        lbl_history12.setText(" ["); //$NON-NLS-1$

                        Text txt_history12 = new Text(cmp_history1, SWT.SEARCH);
                        txt_history12.setMessage(I18n.META_History2);
                        ev_history12_txt[0] = txt_history12;

                        Label lbl_history13 = new Label(cmp_history1, SWT.NONE);
                        lbl_history13.setText("] "); //$NON-NLS-1$

                        Text txt_history13 = new Text(cmp_history1, SWT.SEARCH);
                        txt_history13.setMessage(I18n.META_History4);
                        ev_history13_txt[0] = txt_history13;
                    }

                    {
                        Composite cmp_history2 = new Composite(grp_meta, SWT.NONE);
                        cmp_history2.setLayout(new GridLayout(6, false));

                        Label lbl_history21 = new Label(cmp_history2, SWT.NONE);
                        lbl_history21.setText("or 0 !HISTORY "); //$NON-NLS-1$

                        Text txt_history21 = new Text(cmp_history2, SWT.SEARCH);
                        txt_history21.setMessage(I18n.META_History1);
                        ev_history21_txt[0] = txt_history21;

                        Label lbl_history22 = new Label(cmp_history2, SWT.NONE);
                        lbl_history22.setText(" {"); //$NON-NLS-1$

                        Text txt_history22 = new Text(cmp_history2, SWT.SEARCH);
                        txt_history22.setMessage(I18n.META_History3);
                        ev_history22_txt[0] = txt_history22;

                        Label lbl_history23 = new Label(cmp_history2, SWT.NONE);
                        lbl_history23.setText("} "); //$NON-NLS-1$

                        Text txt_history23 = new Text(cmp_history2, SWT.SEARCH);
                        txt_history23.setMessage(I18n.META_History4);
                        ev_history23_txt[0] = txt_history23;
                    }

                    {
                        Composite cmp_comment = new Composite(grp_meta, SWT.NONE);
                        cmp_comment.setLayout(new GridLayout(5, false));

                        Label lbl_type = new Label(cmp_comment, SWT.NONE);
                        lbl_type.setText("0 // "); //$NON-NLS-1$

                        NButton btn_needsWork2 = new NButton(cmp_comment, SWT.TOGGLE);
                        btn_needsWork2.setText("Needs work:"); //$NON-NLS-1$
                        ev_comment_btn[0] = btn_needsWork2;

                        Text txt_comment = new Text(cmp_comment, SWT.SEARCH);
                        txt_comment.setMessage(I18n.META_Comment);
                        ev_comment_txt[0] = txt_comment;
                    }
                }
            }

            final CTabItem tItem2 = new CTabItem(tabFolder_Settings, SWT.NONE);
            tItem2.setText(I18n.META_BackFaceCulling);
            {

                final ScrolledComposite cmp_scroll = new ScrolledComposite(tabFolder_Settings, SWT.H_SCROLL | SWT.V_SCROLL);
                tItem2.setControl(cmp_scroll);

                Composite cmp_metaArea = new Composite(cmp_scroll, SWT.NONE);
                cmp_scroll.setContent(cmp_metaArea);
                cmp_scroll.setExpandHorizontal(true);
                cmp_scroll.setExpandVertical(true);
                cmp_scroll.setMinSize(600, 800);

                GridData gdm = new GridData();
                gdm.grabExcessHorizontalSpace = true;
                // gdm.grabExcessVerticalSpace = true;
                cmp_scroll.setLayoutData(gdm);

                cmp_metaArea.setLayout(new GridLayout(1, false));

                {
                    Composite grp_meta = cmp_metaArea;
                    grp_meta.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                    grp_meta.setLayout(new GridLayout(1, false));

                    Composite cmp_bfc = new Composite(grp_meta, SWT.NONE);
                    cmp_bfc.setLayout(new GridLayout(2, false));

                    Label lbl_bfc = new Label(cmp_bfc, SWT.NONE);
                    lbl_bfc.setText("0 BFC "); //$NON-NLS-1$

                    Combo cmb_bfc = new Combo(cmp_bfc, SWT.NONE);
                    cmb_bfc.setItems(new String[] { "INVERTNEXT", "NOCLIP", "CW", "CCW", "CLIP", "CLIP CW", "CLIP CCW"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
                    cmb_bfc.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                    cmb_bfc.setText("INVERTNEXT"); //$NON-NLS-1$
                    cmb_bfc.select(0);
                    ev_bfc_cmb[0] = cmb_bfc;
                }
            }

            final CTabItem tItem3 = new CTabItem(tabFolder_Settings, SWT.NONE);
            tItem3.setText(I18n.META_TextureMapping);
            {

                final ScrolledComposite cmp_scroll = new ScrolledComposite(tabFolder_Settings, SWT.H_SCROLL | SWT.V_SCROLL);
                tItem3.setControl(cmp_scroll);

                Composite cmp_metaArea = new Composite(cmp_scroll, SWT.NONE);
                cmp_scroll.setContent(cmp_metaArea);
                cmp_scroll.setExpandHorizontal(true);
                cmp_scroll.setExpandVertical(true);
                cmp_scroll.setMinSize(600, 800);

                GridData gdm = new GridData();
                gdm.grabExcessHorizontalSpace = true;
                // gdm.grabExcessVerticalSpace = true;
                cmp_scroll.setLayoutData(gdm);

                cmp_metaArea.setLayout(new GridLayout(1, false));

                {
                    Composite grp_meta = cmp_metaArea;
                    grp_meta.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                    grp_meta.setLayout(new GridLayout(1, false));
                    {
                        Composite cmp_texmap = new Composite(grp_meta, SWT.NONE);
                        cmp_texmap.setLayout(new GridLayout(14, false));

                        Label lbl_texmap = new Label(cmp_texmap, SWT.NONE);
                        lbl_texmap.setText("0 !TEXMAP "); //$NON-NLS-1$

                        Combo cmb_texmap = new Combo(cmp_texmap, SWT.NONE);
                        cmb_texmap.setItems(new String[] { "START", "NEXT" }); //$NON-NLS-1$ //$NON-NLS-2$
                        cmb_texmap.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                        cmb_texmap.setText("START"); //$NON-NLS-1$
                        cmb_texmap.select(0);
                        ev_texmapPlanar_cmb[0] = cmb_texmap;

                        Label lbl_planar = new Label(cmp_texmap, SWT.NONE);
                        lbl_planar.setText(" PLANAR "); //$NON-NLS-1$
                        {
                            Text txt_planar = new Text(cmp_texmap, SWT.SEARCH);
                            txt_planar.setMessage(I18n.META_TextureX1);
                            ev_texmapPlanar1_txt[0] = txt_planar;
                        }
                        {
                            Text txt_planar = new Text(cmp_texmap, SWT.SEARCH);
                            txt_planar.setMessage(I18n.META_TextureY1);
                            ev_texmapPlanar2_txt[0] = txt_planar;
                        }
                        {
                            Text txt_planar = new Text(cmp_texmap, SWT.SEARCH);
                            txt_planar.setMessage(I18n.META_TextureZ1);
                            ev_texmapPlanar3_txt[0] = txt_planar;
                        }
                        {
                            Text txt_planar = new Text(cmp_texmap, SWT.SEARCH);
                            txt_planar.setMessage(I18n.META_TextureX2);
                            ev_texmapPlanar4_txt[0] = txt_planar;
                        }
                        {
                            Text txt_planar = new Text(cmp_texmap, SWT.SEARCH);
                            txt_planar.setMessage(I18n.META_TextureY2);
                            ev_texmapPlanar5_txt[0] = txt_planar;
                        }
                        {
                            Text txt_planar = new Text(cmp_texmap, SWT.SEARCH);
                            txt_planar.setMessage(I18n.META_TextureZ2);
                            ev_texmapPlanar6_txt[0] = txt_planar;
                        }
                        {
                            Text txt_planar = new Text(cmp_texmap, SWT.SEARCH);
                            txt_planar.setMessage(I18n.META_TextureX3);
                            ev_texmapPlanar7_txt[0] = txt_planar;
                        }
                        {
                            Text txt_planar = new Text(cmp_texmap, SWT.SEARCH);
                            txt_planar.setMessage(I18n.META_TextureY3);
                            ev_texmapPlanar8_txt[0] = txt_planar;
                        }
                        {
                            Text txt_planar = new Text(cmp_texmap, SWT.SEARCH);
                            txt_planar.setMessage(I18n.META_TextureZ3);
                            ev_texmapPlanar9_txt[0] = txt_planar;
                        }
                        {
                            Text txt_png = new Text(cmp_texmap, SWT.SEARCH);
                            txt_png.setMessage(I18n.META_TexturePNG);
                            ev_texmapPlanar10_txt[0] = txt_png;
                        }
                        {
                            NButton btn_browse = new NButton(cmp_texmap, SWT.NONE);
                            btn_browse.setText(I18n.DIALOG_Browse);
                            ev_texmapPlanar_btn[0] = btn_browse;
                        }
                    }
                    {
                        Composite cmp_texmap = new Composite(grp_meta, SWT.NONE);
                        cmp_texmap.setLayout(new GridLayout(15, false));

                        Label lbl_texmap = new Label(cmp_texmap, SWT.NONE);
                        lbl_texmap.setText("0 !TEXMAP "); //$NON-NLS-1$

                        Combo cmb_texmap = new Combo(cmp_texmap, SWT.NONE);
                        cmb_texmap.setItems(new String[] { "START", "NEXT" }); //$NON-NLS-1$ //$NON-NLS-2$
                        cmb_texmap.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                        cmb_texmap.setText("START"); //$NON-NLS-1$
                        cmb_texmap.select(0);
                        ev_texmapCyli_cmb[0] = cmb_texmap;

                        Label lbl_cylindrical = new Label(cmp_texmap, SWT.NONE);
                        lbl_cylindrical.setText(" CYLINDRICAL "); //$NON-NLS-1$
                        {
                            Text txt_cylindrical = new Text(cmp_texmap, SWT.SEARCH);
                            txt_cylindrical.setMessage(I18n.META_TextureX1);
                            txt_cylindrical.setToolTipText(I18n.META_CylinderBottomCenter);
                            ev_texmapCyli1_txt[0] = txt_cylindrical;
                        }
                        {
                            Text txt_cylindrical = new Text(cmp_texmap, SWT.SEARCH);
                            txt_cylindrical.setMessage(I18n.META_TextureY1);
                            txt_cylindrical.setToolTipText(I18n.META_CylinderBottomCenter);
                            ev_texmapCyli2_txt[0] = txt_cylindrical;
                        }
                        {
                            Text txt_cylindrical = new Text(cmp_texmap, SWT.SEARCH);
                            txt_cylindrical.setMessage(I18n.META_TextureZ1);
                            txt_cylindrical.setToolTipText(I18n.META_CylinderBottomCenter);
                            ev_texmapCyli3_txt[0] = txt_cylindrical;
                        }
                        {
                            Text txt_cylindrical = new Text(cmp_texmap, SWT.SEARCH);
                            txt_cylindrical.setMessage(I18n.META_TextureX2);
                            txt_cylindrical.setToolTipText(I18n.META_CylinderTopCenter);
                            ev_texmapCyli4_txt[0] = txt_cylindrical;
                        }
                        {
                            Text txt_cylindrical = new Text(cmp_texmap, SWT.SEARCH);
                            txt_cylindrical.setMessage(I18n.META_TextureY2);
                            txt_cylindrical.setToolTipText(I18n.META_CylinderTopCenter);
                            ev_texmapCyli5_txt[0] = txt_cylindrical;
                        }
                        {
                            Text txt_cylindrical = new Text(cmp_texmap, SWT.SEARCH);
                            txt_cylindrical.setMessage(I18n.META_TextureZ2);
                            txt_cylindrical.setToolTipText(I18n.META_CylinderTopCenter);
                            ev_texmapCyli6_txt[0] = txt_cylindrical;
                        }
                        {
                            Text txt_cylindrical = new Text(cmp_texmap, SWT.SEARCH);
                            txt_cylindrical.setMessage(I18n.META_TextureX3);
                            txt_cylindrical.setToolTipText(I18n.META_TextureBottomCenter);
                            ev_texmapCyli7_txt[0] = txt_cylindrical;
                        }
                        {
                            Text txt_cylindrical = new Text(cmp_texmap, SWT.SEARCH);
                            txt_cylindrical.setMessage(I18n.META_TextureY3);
                            txt_cylindrical.setToolTipText(I18n.META_TextureBottomCenter);
                            ev_texmapCyli8_txt[0] = txt_cylindrical;
                        }
                        {
                            Text txt_cylindrical = new Text(cmp_texmap, SWT.SEARCH);
                            txt_cylindrical.setMessage(I18n.META_TextureZ3);
                            txt_cylindrical.setToolTipText(I18n.META_TextureBottomCenter);
                            ev_texmapCyli9_txt[0] = txt_cylindrical;
                        }
                        {
                            Text txt_cylindrical = new Text(cmp_texmap, SWT.SEARCH);
                            txt_cylindrical.setMessage(I18n.META_TextureAngle1);
                            ev_texmapCyli10_txt[0] = txt_cylindrical;
                        }
                        {
                            Text txt_png = new Text(cmp_texmap, SWT.SEARCH);
                            txt_png.setMessage(I18n.META_TexturePNG);
                            ev_texmapCyli11_txt[0] = txt_png;
                        }
                        {
                            NButton btn_browse = new NButton(cmp_texmap, SWT.NONE);
                            btn_browse.setText(I18n.DIALOG_Browse);
                            ev_texmapCyli_btn[0] = btn_browse;
                        }
                    }
                    {
                        Composite cmp_texmap = new Composite(grp_meta, SWT.NONE);
                        cmp_texmap.setLayout(new GridLayout(16, false));

                        Label lbl_texmap = new Label(cmp_texmap, SWT.NONE);
                        lbl_texmap.setText("0 !TEXMAP "); //$NON-NLS-1$

                        Combo cmb_texmap = new Combo(cmp_texmap, SWT.NONE);
                        cmb_texmap.setItems(new String[] { "START", "NEXT" }); //$NON-NLS-1$ //$NON-NLS-2$
                        cmb_texmap.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                        cmb_texmap.setText("START"); //$NON-NLS-1$
                        cmb_texmap.select(0);
                        ev_texmapSphere_cmb[0] = cmb_texmap;

                        Label lbl_spherical = new Label(cmp_texmap, SWT.NONE);
                        lbl_spherical.setText(" SPHERICAL "); //$NON-NLS-1$
                        {
                            Text txt_spherical = new Text(cmp_texmap, SWT.SEARCH);
                            txt_spherical.setMessage(I18n.META_TextureX1);
                            txt_spherical.setToolTipText(I18n.META_TextureSphereCenter);
                            ev_texmapSphere1_txt[0] = txt_spherical;
                        }
                        {
                            Text txt_spherical = new Text(cmp_texmap, SWT.SEARCH);
                            txt_spherical.setMessage(I18n.META_TextureY1);
                            txt_spherical.setToolTipText(I18n.META_TextureSphereCenter);
                            ev_texmapSphere2_txt[0] = txt_spherical;
                        }
                        {
                            Text txt_spherical = new Text(cmp_texmap, SWT.SEARCH);
                            txt_spherical.setMessage(I18n.META_TextureZ1);
                            txt_spherical.setToolTipText(I18n.META_TextureSphereCenter);
                            ev_texmapSphere3_txt[0] = txt_spherical;
                        }
                        {
                            Text txt_spherical = new Text(cmp_texmap, SWT.SEARCH);
                            txt_spherical.setMessage(I18n.META_TextureX2);
                            txt_spherical.setToolTipText(I18n.META_TextureCenter);
                            ev_texmapSphere4_txt[0] = txt_spherical;
                        }
                        {
                            Text txt_spherical = new Text(cmp_texmap, SWT.SEARCH);
                            txt_spherical.setMessage(I18n.META_TextureY2);
                            txt_spherical.setToolTipText(I18n.META_TextureCenter);
                            ev_texmapSphere5_txt[0] = txt_spherical;
                        }
                        {
                            Text txt_spherical = new Text(cmp_texmap, SWT.SEARCH);
                            txt_spherical.setMessage(I18n.META_TextureZ2);
                            txt_spherical.setToolTipText(I18n.META_TextureCenter);
                            ev_texmapSphere6_txt[0] = txt_spherical;
                        }
                        {
                            Text txt_spherical = new Text(cmp_texmap, SWT.SEARCH);
                            txt_spherical.setMessage(I18n.META_TextureX3);
                            txt_spherical.setToolTipText(I18n.META_TextureTopCenter);
                            ev_texmapSphere7_txt[0] = txt_spherical;
                        }
                        {
                            Text txt_spherical = new Text(cmp_texmap, SWT.SEARCH);
                            txt_spherical.setMessage(I18n.META_TextureY3);
                            txt_spherical.setToolTipText(I18n.META_TextureTopCenter);
                            ev_texmapSphere8_txt[0] = txt_spherical;
                        }
                        {
                            Text txt_spherical = new Text(cmp_texmap, SWT.SEARCH);
                            txt_spherical.setMessage(I18n.META_TextureZ3);
                            txt_spherical.setToolTipText(I18n.META_TextureTopCenter);
                            ev_texmapSphere9_txt[0] = txt_spherical;
                        }
                        {
                            Text txt_spherical = new Text(cmp_texmap, SWT.SEARCH);
                            txt_spherical.setMessage(I18n.META_TextureAngle1);
                            ev_texmapSphere10_txt[0] = txt_spherical;
                        }
                        {
                            Text txt_spherical = new Text(cmp_texmap, SWT.SEARCH);
                            txt_spherical.setMessage(I18n.META_TextureAngle2);
                            ev_texmapSphere11_txt[0] = txt_spherical;
                        }
                        {
                            Text txt_png = new Text(cmp_texmap, SWT.SEARCH);
                            txt_png.setMessage(I18n.META_TexturePNG);
                            ev_texmapSphere12_txt[0] = txt_png;
                        }
                        {
                            NButton btn_browse = new NButton(cmp_texmap, SWT.NONE);
                            btn_browse.setText(I18n.DIALOG_Browse);
                            ev_texmapSphere_btn[0] = btn_browse;
                        }
                    }
                    {
                        NButton btn_texmap = new NButton(grp_meta, SWT.NONE);
                        btn_texmap.setText("0 !TEXMAP FALLBACK"); //$NON-NLS-1$
                        ev_texmapFallback_btn[0] = btn_texmap;
                    }
                    {
                        Composite cmp_texmap = new Composite(grp_meta, SWT.NONE);
                        cmp_texmap.setLayout(new GridLayout(2, false));
                        Label lbl_texmap = new Label(cmp_texmap, SWT.NONE);
                        lbl_texmap.setText("0 !: "); //$NON-NLS-1$
                        {
                            Text txt_meta = new Text(cmp_texmap, SWT.SEARCH);
                            txt_meta.setMessage(I18n.META_TextureGeom1);
                            txt_meta.setToolTipText(I18n.META_TextureGeom2);
                            ev_texmapMeta_txt[0] = txt_meta;
                        }
                    }
                    {
                        NButton btn_texmap = new NButton(grp_meta, SWT.NONE);
                        btn_texmap.setText("0 !TEXMAP END"); //$NON-NLS-1$
                        ev_texmapEnd_btn[0] = btn_texmap;
                    }
                }
            }

            final CTabItem tItem4 = new CTabItem(tabFolder_Settings, SWT.NONE);
            tItem4.setText(I18n.META_LPE);
            {

                final ScrolledComposite cmp_scroll = new ScrolledComposite(tabFolder_Settings, SWT.H_SCROLL | SWT.V_SCROLL);
                tItem4.setControl(cmp_scroll);

                Composite cmp_metaArea = new Composite(cmp_scroll, SWT.NONE);
                cmp_scroll.setContent(cmp_metaArea);
                cmp_scroll.setExpandHorizontal(true);
                cmp_scroll.setExpandVertical(true);
                cmp_scroll.setMinSize(600, 800);

                GridData gdm = new GridData();
                gdm.grabExcessHorizontalSpace = true;
                // gdm.grabExcessVerticalSpace = true;
                cmp_scroll.setLayoutData(gdm);

                cmp_metaArea.setLayout(new GridLayout(1, false));

                {
                    Composite grp_meta = cmp_metaArea;
                    grp_meta.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                    grp_meta.setLayout(new GridLayout(1, false));

                    {
                        Composite cmp_lpe = new Composite(grp_meta, SWT.NONE);
                        cmp_lpe.setLayout(new GridLayout(2, false));
                        Label lbl_todo = new Label(cmp_lpe, SWT.NONE);
                        lbl_todo.setText("0 !LPE TODO "); //$NON-NLS-1$
                        {
                            Text txt_todo = new Text(cmp_lpe, SWT.SEARCH);
                            txt_todo.setMessage(I18n.META_Todo);
                            ev_todo_txt[0] = txt_todo;
                        }
                    }

                    {
                        Composite cmp_lpe = new Composite(grp_meta, SWT.NONE);
                        cmp_lpe.setLayout(new GridLayout(4, false));
                        Label lbl_vertex = new Label(cmp_lpe, SWT.NONE);
                        lbl_vertex.setText("0 !LPE VERTEX "); //$NON-NLS-1$
                        {
                            Text txt_x = new Text(cmp_lpe, SWT.SEARCH);
                            txt_x.setMessage(I18n.META_VertexX);
                            txt_x.setToolTipText(I18n.META_DecimalMark);
                            ev_vertex1_txt[0] = txt_x;
                        }
                        {
                            Text txt_y = new Text(cmp_lpe, SWT.SEARCH);
                            txt_y.setMessage(I18n.META_VertexY);
                            txt_y.setToolTipText(I18n.META_DecimalMark);
                            ev_vertex2_txt[0] = txt_y;
                        }
                        {
                            Text txt_z = new Text(cmp_lpe, SWT.SEARCH);
                            txt_z.setMessage(I18n.META_VertexZ);
                            txt_z.setToolTipText(I18n.META_DecimalMark);
                            ev_vertex3_txt[0] = txt_z;
                        }
                    }

                    {
                        Composite cmp_lpe = new Composite(grp_meta, SWT.NONE);
                        cmp_lpe.setLayout(new GridLayout(5, false));
                        Label lbl_vertex = new Label(cmp_lpe, SWT.NONE);
                        lbl_vertex.setText("0 !LPE CSG_"); //$NON-NLS-1$
                        {
                            Combo cmb_csg = new Combo(cmp_lpe, SWT.NONE);
                            cmb_csg.setItems(new String[] { "UNION ", "DIFFERENCE ", "INTERSECTION "}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            cmb_csg.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                            cmb_csg.setText("UNION "); //$NON-NLS-1$
                            cmb_csg.select(0);
                            ev_csgAction_cmb[0] = cmb_csg;
                        }
                        {
                            Text txt_csgid1 = new Text(cmp_lpe, SWT.SEARCH);
                            txt_csgid1.setMessage(I18n.META_CSGSource1);
                            ev_csgAction1_txt[0] = txt_csgid1;
                        }
                        {
                            Text txt_csgid2 = new Text(cmp_lpe, SWT.SEARCH);
                            txt_csgid2.setMessage(I18n.META_CSGSource2);
                            ev_csgAction2_txt[0] = txt_csgid2;
                        }
                        {
                            Text txt_csgid3 = new Text(cmp_lpe, SWT.SEARCH);
                            txt_csgid3.setMessage(I18n.META_CSGTarget1);
                            ev_csgAction3_txt[0] = txt_csgid3;
                        }
                    }
                    {
                        Composite cmp_lpe = new Composite(grp_meta, SWT.NONE);
                        cmp_lpe.setLayout(new GridLayout(16, false));
                        Label lbl_vertex = new Label(cmp_lpe, SWT.NONE);
                        lbl_vertex.setText("0 !LPE CSG_"); //$NON-NLS-1$
                        {
                            Combo cmb_csg = new Combo(cmp_lpe, SWT.NONE);
                            cmb_csg.setItems(new String[] { "CUBOID ", "ELLIPSOID ", "QUAD ", "CYLINDER ", "CONE ", "CIRCLE ", "MESH ", "EXTRUDE "}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
                            cmb_csg.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                            cmb_csg.setText("CUBOID "); //$NON-NLS-1$
                            cmb_csg.select(0);
                            ev_csgBody_cmb[0] = cmb_csg;
                        }
                        {
                            Text txt_csgid1 = new Text(cmp_lpe, SWT.SEARCH);
                            txt_csgid1.setMessage(I18n.META_CSGUnique);
                            txt_csgid1.setToolTipText(I18n.META_CSGUniqueHint);
                            ev_csgBody1_txt[0] = txt_csgid1;
                        }
                        {
                            Text txt_csgid2 = new Text(cmp_lpe, SWT.SEARCH);
                            txt_csgid2.setMessage(I18n.META_Colour);
                            txt_csgid2.setToolTipText(I18n.META_ColourHint);
                            ev_csgBody2_txt[0] = txt_csgid2;

                        }
                        {
                            Text txt_x = new Text(cmp_lpe, SWT.SEARCH);
                            txt_x.setMessage(I18n.META_VertexX);
                            txt_x.setToolTipText(I18n.META_DecimalMark);
                            ev_csgBody3_txt[0] = txt_x;
                        }
                        {
                            Text txt_y = new Text(cmp_lpe, SWT.SEARCH);
                            txt_y.setMessage(I18n.META_VertexY);
                            txt_y.setToolTipText(I18n.META_DecimalMark);
                            ev_csgBody4_txt[0] = txt_y;
                        }
                        {
                            Text txt_z = new Text(cmp_lpe, SWT.SEARCH);
                            txt_z.setMessage(I18n.META_VertexZ);
                            txt_z.setToolTipText(I18n.META_DecimalMark);
                            ev_csgBody5_txt[0] = txt_z;
                        }

                        {
                            Text txt_m = new Text(cmp_lpe, SWT.SEARCH);
                            txt_m.setMessage(I18n.META_M00);
                            txt_m.setToolTipText(I18n.META_TransMatrix);
                            ev_csgBody6_txt[0] = txt_m;
                        }
                        {
                            Text txt_m = new Text(cmp_lpe, SWT.SEARCH);
                            txt_m.setMessage(I18n.META_M01);
                            txt_m.setToolTipText(I18n.META_TransMatrix);
                            ev_csgBody7_txt[0] = txt_m;
                        }
                        {
                            Text txt_m = new Text(cmp_lpe, SWT.SEARCH);
                            txt_m.setMessage(I18n.META_M02);
                            txt_m.setToolTipText(I18n.META_TransMatrix);
                            ev_csgBody8_txt[0] = txt_m;
                        }

                        {
                            Text txt_m = new Text(cmp_lpe, SWT.SEARCH);
                            txt_m.setMessage(I18n.META_M10);
                            txt_m.setToolTipText(I18n.META_TransMatrix);
                            ev_csgBody9_txt[0] = txt_m;
                        }
                        {
                            Text txt_m = new Text(cmp_lpe, SWT.SEARCH);
                            txt_m.setMessage(I18n.META_M11);
                            txt_m.setToolTipText(I18n.META_TransMatrix);
                            ev_csgBody10_txt[0] = txt_m;
                        }
                        {
                            Text txt_m = new Text(cmp_lpe, SWT.SEARCH);
                            txt_m.setMessage(I18n.META_M12);
                            txt_m.setToolTipText(I18n.META_TransMatrix);
                            ev_csgBody11_txt[0] = txt_m;
                        }

                        {
                            Text txt_m = new Text(cmp_lpe, SWT.SEARCH);
                            txt_m.setMessage(I18n.META_M20);
                            txt_m.setToolTipText(I18n.META_TransMatrix);
                            ev_csgBody12_txt[0] = txt_m;
                        }
                        {
                            Text txt_m = new Text(cmp_lpe, SWT.SEARCH);
                            txt_m.setMessage(I18n.META_M21);
                            txt_m.setToolTipText(I18n.META_TransMatrix);
                            ev_csgBody13_txt[0] = txt_m;
                        }
                        {
                            Text txt_m = new Text(cmp_lpe, SWT.SEARCH);
                            txt_m.setMessage(I18n.META_M22);
                            txt_m.setToolTipText(I18n.META_TransMatrix);
                            ev_csgBody14_txt[0] = txt_m;
                        }
                    }
                    {
                        Composite cmp_lpe = new Composite(grp_meta, SWT.NONE);
                        cmp_lpe.setLayout(new GridLayout(16, false));
                        Label lbl_vertex = new Label(cmp_lpe, SWT.NONE);
                        lbl_vertex.setText("0 !LPE CSG_TRANSFORM "); //$NON-NLS-1$
                        {
                            Text txt_csgid1 = new Text(cmp_lpe, SWT.SEARCH);
                            txt_csgid1.setMessage(I18n.META_CSGSource1);
                            txt_csgid1.setToolTipText(I18n.META_CSGUniqueHint);
                            ev_csgTrans1_txt[0] = txt_csgid1;
                        }
                        {
                            Text txt_csgid2 = new Text(cmp_lpe, SWT.SEARCH);
                            txt_csgid2.setMessage(I18n.META_CSGTarget1);
                            txt_csgid2.setToolTipText(I18n.META_CSGUniqueHint);
                            ev_csgTrans2_txt[0] = txt_csgid2;

                        }
                        {
                            Text txt_csgcol = new Text(cmp_lpe, SWT.SEARCH);
                            txt_csgcol.setMessage(I18n.META_Colour);
                            txt_csgcol.setToolTipText(I18n.META_ColourHint);
                            ev_csgTrans3_txt[0] = txt_csgcol;

                        }
                        {
                            Text txt_x = new Text(cmp_lpe, SWT.SEARCH);
                            txt_x.setMessage(I18n.META_VertexX);
                            txt_x.setToolTipText(I18n.META_DecimalMark);
                            ev_csgTrans4_txt[0] = txt_x;
                        }
                        {
                            Text txt_y = new Text(cmp_lpe, SWT.SEARCH);
                            txt_y.setMessage(I18n.META_VertexY);
                            txt_y.setToolTipText(I18n.META_DecimalMark);
                            ev_csgTrans5_txt[0] = txt_y;
                        }
                        {
                            Text txt_z = new Text(cmp_lpe, SWT.SEARCH);
                            txt_z.setMessage(I18n.META_VertexZ);
                            txt_z.setToolTipText(I18n.META_DecimalMark);
                            ev_csgTrans6_txt[0] = txt_z;
                        }

                        {
                            Text txt_m = new Text(cmp_lpe, SWT.SEARCH);
                            txt_m.setMessage(I18n.META_M00);
                            txt_m.setToolTipText(I18n.META_TransMatrix);
                            ev_csgTrans7_txt[0] = txt_m;
                        }
                        {
                            Text txt_m = new Text(cmp_lpe, SWT.SEARCH);
                            txt_m.setMessage(I18n.META_M01);
                            txt_m.setToolTipText(I18n.META_TransMatrix);
                            ev_csgTrans8_txt[0] = txt_m;
                        }
                        {
                            Text txt_m = new Text(cmp_lpe, SWT.SEARCH);
                            txt_m.setMessage(I18n.META_M02);
                            txt_m.setToolTipText(I18n.META_TransMatrix);
                            ev_csgTrans9_txt[0] = txt_m;
                        }

                        {
                            Text txt_m = new Text(cmp_lpe, SWT.SEARCH);
                            txt_m.setMessage(I18n.META_M10);
                            txt_m.setToolTipText(I18n.META_TransMatrix);
                            ev_csgTrans10_txt[0] = txt_m;
                        }
                        {
                            Text txt_m = new Text(cmp_lpe, SWT.SEARCH);
                            txt_m.setMessage(I18n.META_M11);
                            txt_m.setToolTipText(I18n.META_TransMatrix);
                            ev_csgTrans11_txt[0] = txt_m;
                        }
                        {
                            Text txt_m = new Text(cmp_lpe, SWT.SEARCH);
                            txt_m.setMessage(I18n.META_M12);
                            txt_m.setToolTipText(I18n.META_TransMatrix);
                            ev_csgTrans12_txt[0] = txt_m;
                        }

                        {
                            Text txt_m = new Text(cmp_lpe, SWT.SEARCH);
                            txt_m.setMessage(I18n.META_M20);
                            txt_m.setToolTipText(I18n.META_TransMatrix);
                            ev_csgTrans13_txt[0] = txt_m;
                        }
                        {
                            Text txt_m = new Text(cmp_lpe, SWT.SEARCH);
                            txt_m.setMessage(I18n.META_M21);
                            txt_m.setToolTipText(I18n.META_TransMatrix);
                            ev_csgTrans14_txt[0] = txt_m;
                        }
                        {
                            Text txt_m = new Text(cmp_lpe, SWT.SEARCH);
                            txt_m.setMessage(I18n.META_M22);
                            txt_m.setToolTipText(I18n.META_TransMatrix);
                            ev_csgTrans15_txt[0] = txt_m;
                        }
                    }

                    {
                        Composite cmp_lpe = new Composite(grp_meta, SWT.NONE);
                        cmp_lpe.setLayout(new GridLayout(16, false));
                        Label lbl_vertex = new Label(cmp_lpe, SWT.NONE);
                        lbl_vertex.setText("0 !LPE CSG_EXT_CFG "); //$NON-NLS-1$
                        {
                            Text txt_csgid1 = new Text(cmp_lpe, SWT.SEARCH);
                            txt_csgid1.setMessage(I18n.META_CSGExtrude1a);
                            txt_csgid1.setToolTipText(I18n.META_CSGExtrude1b);
                            ev_csgEx1_txt[0] = txt_csgid1;
                        }
                        {
                            Text txt_csgid2 = new Text(cmp_lpe, SWT.SEARCH);
                            txt_csgid2.setMessage(I18n.META_CSGExtrude2a);
                            txt_csgid2.setToolTipText(I18n.META_CSGExtrude2b);
                            ev_csgEx2_txt[0] = txt_csgid2;

                        }
                        {
                            Text txt_csgcol = new Text(cmp_lpe, SWT.SEARCH);
                            txt_csgcol.setMessage(I18n.META_CSGExtrude3a);
                            txt_csgcol.setToolTipText(I18n.META_CSGExtrude3b);
                            ev_csgEx3_txt[0] = txt_csgcol;

                        }
                        {
                            Text txt_x = new Text(cmp_lpe, SWT.SEARCH);
                            txt_x.setMessage(I18n.META_CSGExtrude4a);
                            txt_x.setToolTipText(I18n.META_CSGExtrude4b);
                            ev_csgEx4_txt[0] = txt_x;
                        }
                        {
                            Text txt_y = new Text(cmp_lpe, SWT.SEARCH);
                            txt_y.setMessage(I18n.META_CSGExtrude5a);
                            txt_y.setToolTipText(I18n.META_CSGExtrude5b);
                            ev_csgEx5_txt[0] = txt_y;
                        }
                        {
                            Text txt_z = new Text(cmp_lpe, SWT.SEARCH);
                            txt_z.setMessage(I18n.META_CSGExtrude6a);
                            txt_z.setToolTipText(I18n.META_CSGExtrude6b);
                            ev_csgEx6_txt[0] = txt_z;
                        }

                        {
                            Text txt_m = new Text(cmp_lpe, SWT.SEARCH);
                            txt_m.setMessage(I18n.META_CSGExtrude7a);
                            txt_m.setToolTipText(I18n.META_CSGExtrude7b);
                            ev_csgEx7_txt[0] = txt_m;
                        }
                    }

                    {
                        Composite cmp_lpe = new Composite(grp_meta, SWT.NONE);
                        cmp_lpe.setLayout(new GridLayout(2, false));
                        Label lbl_vertex = new Label(cmp_lpe, SWT.NONE);
                        lbl_vertex.setText("0 !LPE CSG_COMPILE"); //$NON-NLS-1$
                        {
                            Text txt_csgid1 = new Text(cmp_lpe, SWT.SEARCH);
                            txt_csgid1.setMessage(I18n.META_CSGSource3);
                            txt_csgid1.setToolTipText(I18n.META_CSGCompile);
                            ev_csgCompile_txt[0] = txt_csgid1;
                        }
                    }

                    {
                        Composite cmp_lpe = new Composite(grp_meta, SWT.NONE);
                        cmp_lpe.setLayout(new GridLayout(2, false));
                        Label lbl_vertex = new Label(cmp_lpe, SWT.NONE);
                        lbl_vertex.setText("0 !LPE CSG_QUALITY"); //$NON-NLS-1$
                        {
                            Text txt_csgQuality = new Text(cmp_lpe, SWT.SEARCH);
                            txt_csgQuality.setMessage(I18n.META_Quality);
                            ev_csgQuality_txt[0] = txt_csgQuality;
                        }
                    }

                    {
                        Composite cmp_lpe = new Composite(grp_meta, SWT.NONE);
                        cmp_lpe.setLayout(new GridLayout(2, false));
                        Label lbl_vertex = new Label(cmp_lpe, SWT.NONE);
                        lbl_vertex.setText("0 !LPE CSG_EPSILON"); //$NON-NLS-1$
                        {
                            Text txt_csgEpsilon = new Text(cmp_lpe, SWT.SEARCH);
                            txt_csgEpsilon.setMessage(I18n.META_CSGEpsilon1);
                            txt_csgEpsilon.setToolTipText(I18n.META_CSGEpsilon2);
                            ev_csgEpsilon_txt[0] = txt_csgEpsilon;
                        }
                    }

                    {
                        Composite cmp_lpe = new Composite(grp_meta, SWT.NONE);
                        cmp_lpe.setLayout(new GridLayout(2, false));
                        Label lbl_vertex = new Label(cmp_lpe, SWT.NONE);
                        lbl_vertex.setText("0 !LPE CSG_TJUNCTION_EPSILON"); //$NON-NLS-1$
                        {
                            Text txt_csgTJunctionEpsilon = new Text(cmp_lpe, SWT.SEARCH);
                            txt_csgTJunctionEpsilon.setMessage(I18n.META_CSGJunctionEpsilon1);
                            txt_csgTJunctionEpsilon.setToolTipText(I18n.META_CSGJunctionEpsilon2);
                            ev_csgTJunctionEpsilon_txt[0] = txt_csgTJunctionEpsilon;
                        }
                    }

                    {
                        Composite cmp_lpe = new Composite(grp_meta, SWT.NONE);
                        cmp_lpe.setLayout(new GridLayout(2, false));
                        Label lbl_vertex = new Label(cmp_lpe, SWT.NONE);
                        lbl_vertex.setText("0 !LPE CSG_EDGE_COLLAPSE_EPSILON"); //$NON-NLS-1$
                        {
                            Text txt_csgEdgeCollapseEpsilon = new Text(cmp_lpe, SWT.SEARCH);
                            txt_csgEdgeCollapseEpsilon.setMessage(I18n.META_CSGCollapse1);
                            txt_csgEdgeCollapseEpsilon.setToolTipText(I18n.META_CSGCollapse2);
                            ev_csgEdgeCollapseEpsilon_txt[0] = txt_csgEdgeCollapseEpsilon;
                        }
                    }

                    {
                        Composite cmp_lpe = new Composite(grp_meta, SWT.NONE);
                        cmp_lpe.setLayout(new GridLayout(11, false));
                        Label lbl_png = new Label(cmp_lpe, SWT.NONE);
                        lbl_png.setText("0 !LPE PNG "); //$NON-NLS-1$
                        {
                            Text txt_x = new Text(cmp_lpe, SWT.SEARCH);
                            txt_x.setMessage(I18n.META_VertexX);
                            txt_x.setToolTipText(I18n.META_DecimalMark);
                            ev_png1_txt[0] = txt_x;
                        }
                        {
                            Text txt_y = new Text(cmp_lpe, SWT.SEARCH);
                            txt_y.setMessage(I18n.META_VertexY);
                            txt_y.setToolTipText(I18n.META_DecimalMark);
                            ev_png2_txt[0] = txt_y;
                        }
                        {
                            Text txt_z = new Text(cmp_lpe, SWT.SEARCH);
                            txt_z.setMessage(I18n.META_VertexZ);
                            txt_z.setToolTipText(I18n.META_DecimalMark);
                            ev_png3_txt[0] = txt_z;
                        }

                        {
                            Text txt_x = new Text(cmp_lpe, SWT.SEARCH);
                            txt_x.setMessage(I18n.META_RotationX);
                            txt_x.setToolTipText(I18n.META_DecimalMark);
                            ev_png4_txt[0] = txt_x;
                        }
                        {
                            Text txt_y = new Text(cmp_lpe, SWT.SEARCH);
                            txt_y.setMessage(I18n.META_RotationY);
                            txt_y.setToolTipText(I18n.META_DecimalMark);
                            ev_png5_txt[0] = txt_y;
                        }
                        {
                            Text txt_z = new Text(cmp_lpe, SWT.SEARCH);
                            txt_z.setMessage(I18n.META_RotationZ);
                            txt_z.setToolTipText(I18n.META_DecimalMark);
                            ev_png6_txt[0] = txt_z;
                        }

                        {
                            Text txt_x = new Text(cmp_lpe, SWT.SEARCH);
                            txt_x.setMessage(I18n.META_ScaleX);
                            txt_x.setToolTipText(I18n.META_DecimalMark);
                            ev_png7_txt[0] = txt_x;
                        }
                        {
                            Text txt_y = new Text(cmp_lpe, SWT.SEARCH);
                            txt_y.setMessage(I18n.META_ScaleY);
                            txt_y.setToolTipText(I18n.META_DecimalMark);
                            ev_png8_txt[0] = txt_y;
                        }
                        {
                            Text txt_png = new Text(cmp_lpe, SWT.SEARCH);
                            txt_png.setMessage(I18n.META_TexturePNG);
                            ev_png9_txt[0] = txt_png;
                        }
                        {
                            NButton btn_browse = new NButton(cmp_lpe, SWT.NONE);
                            btn_browse.setText(I18n.DIALOG_Browse);
                            ev_png_btn[0] = btn_browse;
                        }
                    }
                }
            }

        }

        Label lbl_OnlyFor3D = new Label(container, SWT.NONE);
        lbl_OnlyFor3D.setText(I18n.META_NewLineNote);

        Label lbl_Preview = new Label(container, SWT.BORDER);
        lbl_Preview.setText("0 BFC CERTIFY CCW"); //$NON-NLS-1$
        lbl_lineToInsert[0] = lbl_Preview;
        GridData gdl = new GridData();
        gdl.horizontalAlignment = SWT.CENTER;
        lbl_Preview.setLayoutData(gdl);


        NButton btn_Create = new NButton(container, SWT.NONE);
        btn_Create.setText(I18n.DIALOG_CreateMetaCommand);
        GridData gdt = new GridData();
        gdt.horizontalAlignment = SWT.RIGHT;
        btn_Create.setLayoutData(gdt);
        this.btn_Create[0] = btn_Create;

        return container;
    }

    private void setDefaultCategories() {
        ev_category_cmb[0].setItems(new String[] {
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
                "Znap", //$NON-NLS-1$
        });
    }

    /**
     * Return the initial size of the window.
     */
    @Override
    protected Point getInitialSize() {
        return super.getInitialSize();
    }
}
