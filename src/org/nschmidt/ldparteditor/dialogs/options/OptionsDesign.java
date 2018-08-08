package org.nschmidt.ldparteditor.dialogs.options;

import java.math.BigDecimal;
import java.text.Collator;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.nschmidt.ldparteditor.composites.compositetab.CompositeTab;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.dialogs.keys.KeyDialog;
import org.nschmidt.ldparteditor.enums.Colour;
import org.nschmidt.ldparteditor.enums.Task;
import org.nschmidt.ldparteditor.enums.TextTask;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.Cocoa;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.resources.ResourceManager;
import org.nschmidt.ldparteditor.shells.editortext.EditorTextWindow;
import org.nschmidt.ldparteditor.state.KeyStateManager;
import org.nschmidt.ldparteditor.widgets.BigDecimalSpinner;
import org.nschmidt.ldparteditor.widgets.NButton;
import org.nschmidt.ldparteditor.widgets.Tree;
import org.nschmidt.ldparteditor.widgets.TreeColumn;
import org.nschmidt.ldparteditor.widgets.TreeItem;
import org.nschmidt.ldparteditor.workbench.UserSettingState;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

class OptionsDesign extends ApplicationWindow {

    NButton[] btn_AllowInvalidShapes = new NButton[1];
    NButton[] btn_translateViewByCursor = new NButton[1];
    NButton[] btn_disableMAD3D = new NButton[1];
    NButton[] btn_disableMADtext = new NButton[1];
    NButton[] btn_OK = new NButton[1];

    final Combo[] cmb_textWinArr = new Combo[1];
    final Combo[] cmb_locale = new Combo[1];
    final Text[] txt_ldrawPath = new Text[1];
    final Text[] txt_unofficialPath = new Text[1];
    final Text[] txt_ldrawUserName = new Text[1];
    final Text[] txt_realName = new Text[1];
    final Text[] txt_partAuthoringPath = new Text[1];
    final Combo[] cmb_license = new Combo[1];
    final NButton[] btn_browseLdrawPath = new NButton[1];
    final NButton[] btn_browseUnofficialPath = new NButton[1];
    final NButton[] btn_browseAuthoringPath = new NButton[1];
    final BigDecimalSpinner[] spn_coplanarityWarning = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spn_coplanarityError = new BigDecimalSpinner[1];

    final HashMap<String, Locale> localeMap = new HashMap<String, Locale>();

    private HashSet<Task>  s1 = new HashSet<Task>();
    private HashSet<TextTask> s2 = new HashSet<TextTask>();

    private enum ColourType {
        SWT_COLOUR,
        OPENGL_COLOUR
    }

    {
        s1.add(Task.TRANSFORM_UP);
        s1.add(Task.TRANSFORM_RIGHT);
        s1.add(Task.TRANSFORM_DOWN);
        s1.add(Task.TRANSFORM_LEFT);
        s1.add(Task.TRANSFORM_UP_COPY);
        s1.add(Task.TRANSFORM_RIGHT_COPY);
        s1.add(Task.TRANSFORM_DOWN_COPY);
        s1.add(Task.TRANSFORM_LEFT_COPY);
        s1.add(Task.TRANSLATE_UP);
        s1.add(Task.TRANSLATE_RIGHT);
        s1.add(Task.TRANSLATE_DOWN);
        s1.add(Task.TRANSLATE_LEFT);
        s1.add(Task.COLOUR_NUMBER0);
        s1.add(Task.COLOUR_NUMBER1);
        s1.add(Task.COLOUR_NUMBER2);
        s1.add(Task.COLOUR_NUMBER3);
        s1.add(Task.COLOUR_NUMBER4);
        s1.add(Task.COLOUR_NUMBER5);
        s1.add(Task.COLOUR_NUMBER6);
        s1.add(Task.COLOUR_NUMBER7);
        s1.add(Task.COLOUR_NUMBER8);
        s1.add(Task.COLOUR_NUMBER9);
    }

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
        final Tree treeColours;
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

            CTabItem tItem0 = new CTabItem(tabFolder_Settings, SWT.NONE);
            tItem0.setText(I18n.OPTIONS_MiscOptions);

            {
                final UserSettingState userSettings = WorkbenchManager.getUserSettingState();

                final ScrolledComposite cmp_containerX = new ScrolledComposite(tabFolder_Settings, SWT.V_SCROLL | SWT.H_SCROLL);
                Composite cmp_container = new Composite(cmp_containerX, Cocoa.getStyle());
                tItem0.setControl(cmp_containerX);
                cmp_containerX.setContent(cmp_container);
                cmp_containerX.setExpandHorizontal(true);
                cmp_containerX.setExpandVertical(true);

                cmp_container.setLayout(new GridLayout());
                cmp_container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

                NButton btn_AllowInvalidShapes = new NButton(cmp_container, SWT.CHECK);
                this.btn_AllowInvalidShapes[0] = btn_AllowInvalidShapes;
                btn_AllowInvalidShapes.setText(I18n.OPTIONS_AllowInvalidShapes);
                btn_AllowInvalidShapes.setSelection(userSettings.isAllowInvalidShapes());

                NButton btn_translateViewByCursor = new NButton(cmp_container, SWT.CHECK);
                this.btn_translateViewByCursor[0] = btn_translateViewByCursor;
                btn_translateViewByCursor.setText(I18n.OPTIONS_TranslateByCursor);
                btn_translateViewByCursor.setSelection(userSettings.isTranslatingViewByCursor());

                NButton btn_disableMAD3D = new NButton(cmp_container, SWT.CHECK);
                this.btn_disableMAD3D[0] = btn_disableMAD3D;
                btn_disableMAD3D.setText(I18n.OPTIONS_MAD1);
                btn_disableMAD3D.setSelection(userSettings.isDisableMAD3D());

                NButton btn_disableMADtext = new NButton(cmp_container, SWT.CHECK);
                this.btn_disableMADtext[0] = btn_disableMADtext;
                btn_disableMADtext.setText(I18n.OPTIONS_MAD2);
                btn_disableMADtext.setSelection(userSettings.isDisableMADtext());

                {
                    Label lbl_separator = new Label(cmp_container, SWT.SEPARATOR | SWT.HORIZONTAL);
                    lbl_separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                }

                Label lbl_textWinArr = new Label(cmp_container, SWT.NONE);
                lbl_textWinArr.setText(I18n.OPTIONS_TextWindowArr);

                Combo cmb_textWinArr = new Combo(cmp_container, SWT.READ_ONLY);
                this.cmb_textWinArr[0] = cmb_textWinArr;
                cmb_textWinArr.setItems(new String[]{I18n.OPTIONS_TextWindowSeparate, I18n.OPTIONS_TextWindowLeft, I18n.OPTIONS_TextWindowRight});
                cmb_textWinArr.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                cmb_textWinArr.select(userSettings.getTextWinArr());

                {
                    Label lbl_separator = new Label(cmp_container, SWT.SEPARATOR | SWT.HORIZONTAL);
                    lbl_separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                }

                Label lbl_coplanarityWarning = new Label(cmp_container, SWT.NONE);
                lbl_coplanarityWarning.setText(I18n.OPTIONS_CoplanarityWarning);
                lbl_coplanarityWarning.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

                BigDecimalSpinner spn_coplanarityWarning = new BigDecimalSpinner(cmp_container, Cocoa.getStyle());
                this.spn_coplanarityWarning[0] = spn_coplanarityWarning;
                spn_coplanarityWarning.setMaximum(new BigDecimal("179.9999")); //$NON-NLS-1$
                spn_coplanarityWarning.setMinimum(new BigDecimal("0.01")); //$NON-NLS-1$
                spn_coplanarityWarning.setValue(new BigDecimal(Threshold.coplanarity_angle_warning));
                spn_coplanarityWarning.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

                Label lbl_coplanarityError = new Label(cmp_container, SWT.NONE);
                lbl_coplanarityError.setText(I18n.OPTIONS_CoplanarityError);
                lbl_coplanarityError.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

                BigDecimalSpinner spn_coplanarityError = new BigDecimalSpinner(cmp_container, Cocoa.getStyle());
                this.spn_coplanarityError[0] = spn_coplanarityError;
                spn_coplanarityError.setMaximum(new BigDecimal("179.9999")); //$NON-NLS-1$
                spn_coplanarityError.setMinimum(new BigDecimal("0.01")); //$NON-NLS-1$
                spn_coplanarityError.setValue(new BigDecimal(Threshold.coplanarity_angle_error));
                spn_coplanarityError.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

                {
                    Label lbl_separator = new Label(cmp_container, SWT.SEPARATOR | SWT.HORIZONTAL);
                    lbl_separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                }

                Label lbl_locale = new Label(cmp_container, SWT.NONE);
                lbl_locale.setText(I18n.OPTIONS_ChooseLocale);

                Combo cmb_locale = new Combo(cmp_container, SWT.READ_ONLY);
                this.cmb_locale[0] = cmb_locale;

                String[] locales = new String[DateFormat.getAvailableLocales().length];
                Locale[] locs = DateFormat.getAvailableLocales();
                final Locale l = userSettings.getLocale();
                Arrays.sort(locs, new Comparator<Locale>() {
                    @Override
                    public int compare(Locale o1, Locale o2) {
                        return Collator.getInstance(Locale.ENGLISH).compare(o1.getDisplayName(l), o2.getDisplayName(l));
                    }
                });
                localeMap.clear();
                int englishIndex = 0;
                for (int i = 0; i < locales.length; i++) {
                    locales[i] = locs[i].getDisplayName(l);
                    localeMap.put(locales[i], locs[i]);
                    if (locs[i].equals(l)) {
                        englishIndex = i;
                    }
                }

                cmb_locale.setItems(locales);
                cmb_locale.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                cmb_locale.select(englishIndex);

                Label lbl_ldrawFolderQuestion = new Label(cmp_container, SWT.NONE);
                lbl_ldrawFolderQuestion.setText(I18n.OPTIONS_LdrawFolder);

                Composite cmp_pathChooser1 = new Composite(cmp_container, SWT.NONE);
                cmp_pathChooser1.setLayout(new RowLayout(SWT.HORIZONTAL));

                Text txt_ldrawPath = new Text(cmp_pathChooser1, SWT.BORDER);
                this.txt_ldrawPath[0] = txt_ldrawPath;
                txt_ldrawPath.setEditable(false);
                txt_ldrawPath.setLayoutData(new RowData(294, SWT.DEFAULT));
                txt_ldrawPath.setText(userSettings.getLdrawFolderPath());

                NButton btn_BrowseLdrawPath = new NButton(cmp_pathChooser1, SWT.NONE);
                this.btn_browseLdrawPath[0] = btn_BrowseLdrawPath;
                btn_BrowseLdrawPath.setText(I18n.OPTIONS_Browse);

                Label lbl_ldrawUserQuestion = new Label(cmp_container, SWT.NONE);
                lbl_ldrawUserQuestion.setText(I18n.OPTIONS_LdrawName);

                Text txt_ldrawUserName = new Text(cmp_container, SWT.BORDER);
                this.txt_ldrawUserName[0] = txt_ldrawUserName;
                txt_ldrawUserName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                txt_ldrawUserName.setText(userSettings.getLdrawUserName());

                Label lbl_realNameQuestion = new Label(cmp_container, SWT.NONE);
                lbl_realNameQuestion.setText(I18n.OPTIONS_RealName);

                Text txt_realName = new Text(cmp_container, SWT.BORDER);
                this.txt_realName[0] = txt_realName;
                txt_realName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                txt_realName.setText(userSettings.getRealUserName());

                Label lbl_licenseQuestion = new Label(cmp_container, SWT.NONE);
                lbl_licenseQuestion.setText(I18n.OPTIONS_License);

                Combo cmb_license = new Combo(cmp_container, SWT.NONE);
                this.cmb_license[0] = cmb_license;
                cmb_license.setItems(new String[] { "0 !LICENSE Redistributable under CCAL version 2.0 : see CAreadme.txt", "0 !LICENSE Not redistributable : see NonCAreadme.txt" }); //$NON-NLS-1$ //$NON-NLS-2$
                cmb_license.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                cmb_license.setText(userSettings.getLicense());

                Label lbl_authoringFolderQuestion = new Label(cmp_container, SWT.NONE);
                lbl_authoringFolderQuestion.setText(I18n.OPTIONS_AuthoringFolder);

                Composite cmp_pathChooser2 = new Composite(cmp_container, SWT.NONE);
                cmp_pathChooser2.setLayout(new RowLayout(SWT.HORIZONTAL));

                Text txt_partAuthoringPath = new Text(cmp_pathChooser2, SWT.BORDER);
                this.txt_partAuthoringPath[0] = txt_partAuthoringPath;
                txt_partAuthoringPath.setEditable(false);
                txt_partAuthoringPath.setLayoutData(new RowData(294, SWT.DEFAULT));
                txt_partAuthoringPath.setText(userSettings.getAuthoringFolderPath());

                NButton btn_browseAuthoringPath = new NButton(cmp_pathChooser2, SWT.NONE);
                this.btn_browseAuthoringPath[0] = btn_browseAuthoringPath;
                btn_browseAuthoringPath.setText(I18n.OPTIONS_Browse);

                Label lbl_unofficialPathQuestion = new Label(cmp_container, SWT.NONE);
                lbl_unofficialPathQuestion.setText(I18n.OPTIONS_UnofficialFolder);

                Composite cmp_pathChooser3 = new Composite(cmp_container, SWT.NONE);
                cmp_pathChooser3.setLayout(new RowLayout(SWT.HORIZONTAL));

                Text txt_unofficialPath = new Text(cmp_pathChooser3, SWT.BORDER);
                this.txt_unofficialPath[0] = txt_unofficialPath;
                txt_unofficialPath.setEditable(false);
                txt_unofficialPath.setLayoutData(new RowData(294, SWT.DEFAULT));
                txt_unofficialPath.setText(userSettings.getUnofficialFolderPath());

                NButton btn_browseUnofficialPath = new NButton(cmp_pathChooser3, SWT.NONE);
                this.btn_browseUnofficialPath[0] = btn_browseUnofficialPath;
                btn_browseUnofficialPath.setText(I18n.OPTIONS_Browse);

                cmp_containerX.setMinSize(cmp_container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
            }

            final CTabItem tItem1 = new CTabItem(tabFolder_Settings, SWT.NONE);
            tItem1.setText(I18n.KEYBOARD_CustomiseShortkeys);
            {
                final Composite cmp_container = new Composite(tabFolder_Settings, SWT.NONE);
                tItem1.setControl(cmp_container);

                cmp_container.setLayout(new GridLayout());
                cmp_container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

                Label lbl_DoubleClick = new Label(cmp_container, I18n.I18N_RTL());
                lbl_DoubleClick.setText(I18n.KEYBOARD_DoubleClick);

                final Tree tree = new Tree(cmp_container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL, Task.values().length + TextTask.values().length - 24);

                tree.setLinesVisible(true);
                tree.setHeaderVisible(true);
                tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

                TreeColumn trclmn_Description = new TreeColumn(tree, SWT.NONE);
                trclmn_Description.setWidth(598);
                trclmn_Description.setText(I18n.KEYBOARD_Description);

                TreeColumn trclmn_Location = new TreeColumn(tree, SWT.NONE);
                trclmn_Location.setWidth(100);
                trclmn_Location.setText(I18n.KEYBOARD_Shortkey);

                TreeItem trtm_Editor3D = new TreeItem(tree, SWT.NONE);
                trtm_Editor3D.setImage(ResourceManager.getImage("icon16_primitives.png")); //$NON-NLS-1$
                trtm_Editor3D.setText(new String[] { I18n.KEYBOARD_Editor3D, "" }); //$NON-NLS-1$
                trtm_Editor3D.setVisible(true);

                TreeItem trtm_EditorText = new TreeItem(tree, SWT.NONE);
                trtm_EditorText.setImage(ResourceManager.getImage("icon16_annotate.png")); //$NON-NLS-1$
                trtm_EditorText.setText(new String[] { I18n.KEYBOARD_EditorText, "" }); //$NON-NLS-1$
                trtm_EditorText.setVisible(true);

                registerTask(trtm_Editor3D, I18n.KEYBOARD_LMB, Task.LMB, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_MMB, Task.MMB, !Cocoa.isCocoa);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_RMB, Task.RMB, !Cocoa.isCocoa);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_CloseView, Task.CLOSE_VIEW, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_ToggleInsertAtCursor, Task.INSERT_AT_CURSOR, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_AddComment, Task.ADD_COMMENTS, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_AddVertex, Task.ADD_VERTEX, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_AddLine, Task.ADD_LINE, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_AddTriangle, Task.ADD_TRIANGLE, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_AddQuad, Task.ADD_QUAD, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_AddCondline, Task.ADD_CONDLINE, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_AddDistance, Task.ADD_DISTANCE, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_AddProtractor, Task.ADD_PROTRACTOR, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_Cut, Task.CUT, false);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_Copy, Task.COPY, false);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_Paste, Task.PASTE, false);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_Delete, Task.DELETE, false);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_Esc1, Task.ESC, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_MergeToAvg, Task.MERGE_TO_AVERAGE, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_MergeToLast, Task.MERGE_TO_LAST, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_Split, Task.SPLIT, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_FlipRotate, Task.FLIP_ROTATE_VERTICES, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_ModeCombined, Task.MODE_COMBINED, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_ModeSelect, Task.MODE_SELECT, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_ModeMove, Task.MODE_MOVE, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_ModeRotate, Task.MODE_ROTATE, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_ModeScale, Task.MODE_SCALE, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_MoveToAvg, Task.MOVE_TO_AVG, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_MoveAdjacentData, Task.MOVE_ADJACENT_DATA, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_SwapWinding, Task.SWAP_WINDING, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_ObjVertex, Task.OBJ_VERTEX, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_ObjLine, Task.OBJ_LINE, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_ObjFace, Task.OBJ_FACE, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_ObjPrimitive, Task.OBJ_PRIMITIVE, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_AxisX, Task.MODE_X, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_AxisY, Task.MODE_Y, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_AxisZ, Task.MODE_Z, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_AxisXY, Task.MODE_XY, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_AxisXZ, Task.MODE_XZ, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_AxisYZ, Task.MODE_YZ, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_AxisXYZ, Task.MODE_XYZ, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_ResetView, Task.RESET_VIEW, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_Save, Task.SAVE, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_SelectAll, Task.SELECT_ALL, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_SelectAllWithSameColours, Task.SELECT_ALL_WITH_SAME_COLOURS, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_SelectConnected, Task.SELECT_CONNECTED, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_SelectNone, Task.SELECT_NONE, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_SelectOptionWithSameColours, Task.SELECT_OPTION_WITH_SAME_COLOURS, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_SelectTouching, Task.SELECT_TOUCHING, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_ShowGrid, Task.SHOW_GRID, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_ShowRuler, Task.SHOW_RULER, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_Undo, Task.UNDO, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_Redo, Task.REDO, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_ZoomIn, Task.ZOOM_IN, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_ZoomOut, Task.ZOOM_OUT, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_RendermodeNoBackfaceCulling, Task.RENDERMODE_NoBackfaceCulling, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_RendermodeRandomColours, Task.RENDERMODE_RandomColours, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_RendermodeGreenFrontfacesRedBackfaces, Task.RENDERMODE_GreenFrontfacesRedBackfaces, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_RendermodeRedBackfaces, Task.RENDERMODE_RedBackfaces, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_RendermodeRealBackfaceCulling, Task.RENDERMODE_RealBackfaceCulling, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_RendermodeLdrawStandard, Task.RENDERMODE_LDrawStandard, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_RendermodeCondlineMode, Task.RENDERMODE_SpecialCondline, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_RendermodeCoplanarityHeatmap, Task.RENDERMODE_CoplanarityHeatmap, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_RendermodeWireframe, Task.RENDERMODE_Wireframe, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_PerspectiveFront, Task.PERSPECTIVE_FRONT, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_PerspectiveBack, Task.PERSPECTIVE_BACK, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_PerspectiveLeft, Task.PERSPECTIVE_LEFT, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_PerspectiveRight, Task.PERSPECTIVE_RIGHT, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_PerspectiveTop, Task.PERSPECTIVE_TOP, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_PerspectiveBottom, Task.PERSPECTIVE_BOTTOM, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_PerspectiveTwoThirds, Task.PERSPECTIVE_TwoThirds, true);

                registerTask(trtm_EditorText, I18n.KEYBOARD_Esc2, TextTask.EDITORTEXT_ESC, true);
                registerTask(trtm_EditorText, I18n.KEYBOARD_Inline, TextTask.EDITORTEXT_INLINE, true);
                registerTask(trtm_EditorText, I18n.KEYBOARD_QuickFix, TextTask.EDITORTEXT_QUICKFIX, true);
                registerTask(trtm_EditorText, I18n.KEYBOARD_Redo, TextTask.EDITORTEXT_REDO, true);
                registerTask(trtm_EditorText, I18n.KEYBOARD_ReplaceVertex, TextTask.EDITORTEXT_REPLACE_VERTEX, true);
                registerTask(trtm_EditorText, I18n.KEYBOARD_Round, TextTask.EDITORTEXT_ROUND, true);
                registerTask(trtm_EditorText, I18n.KEYBOARD_Save, TextTask.EDITORTEXT_SAVE, true);
                registerTask(trtm_EditorText, I18n.KEYBOARD_SelectAll, TextTask.EDITORTEXT_SELECTALL, true);
                registerTask(trtm_EditorText, I18n.KEYBOARD_Undo, TextTask.EDITORTEXT_UNDO, true);
                registerTask(trtm_EditorText, I18n.KEYBOARD_FindReplace, TextTask.EDITORTEXT_FIND, true);
                registerTask(trtm_EditorText, I18n.KEYBOARD_InsertHistory, TextTask.EDITORTEXT_INSERT_HISTORY, true);
                registerTask(trtm_EditorText, I18n.KEYBOARD_InsertKeyword, TextTask.EDITORTEXT_INSERT_KEYWORD, true);
                registerTask(trtm_EditorText, I18n.KEYBOARD_InsertReference, TextTask.EDITORTEXT_INSERT_REFERENCE, true);
                registerTask(trtm_EditorText, I18n.KEYBOARD_MoveLineUp, TextTask.EDITORTEXT_LINE_UP, true);
                registerTask(trtm_EditorText, I18n.KEYBOARD_MoveLineDown, TextTask.EDITORTEXT_LINE_DOWN, true);

                if (s1.size() != Task.values().length || s2.size() != TextTask.values().length) {
                    throw new AssertionError("Not all shortkey items are covered by this dialog! Please fix it"); //$NON-NLS-1$
                }

                tree.build();

                tree.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseDoubleClick(MouseEvent e) {
                        final TreeItem selection;
                        if (tree.getSelectionCount() == 1 && (selection = tree.getSelection()[0]).getData() != null) {
                            KeyStateManager.tmp_keyString = null;
                            if (new KeyDialog(getShell()).open() == IDialogConstants.OK_ID && KeyStateManager.tmp_keyString != null) {
                                Object[] data = (Object[]) selection.getData();
                                if (data[0] == null && !KeyStateManager.hasTextTaskKey(KeyStateManager.tmp_mapKey)) {
                                    KeyStateManager.changeKey((String) data[2], KeyStateManager.tmp_mapKey, KeyStateManager.tmp_keyString, (TextTask) data[1]);
                                    selection.setText(new String[]{selection.getText(0), KeyStateManager.tmp_keyString});
                                }
                                if (data[1] == null && !KeyStateManager.hasTaskKey(KeyStateManager.tmp_mapKey)) {
                                    KeyStateManager.changeKey((String) data[2], KeyStateManager.tmp_mapKey, KeyStateManager.tmp_keyString, (Task) data[0]);
                                    selection.setText(new String[]{selection.getText(0), KeyStateManager.tmp_keyString});
                                }
                                tree.build();
                                tree.update();
                            }
                        }
                    }
                });
            }

            CTabItem tItem2 = new CTabItem(tabFolder_Settings, SWT.NONE);
            tItem2.setText(I18n.COLOUR_CustomiseColours);

            {
                final Composite cmp_container = new Composite(tabFolder_Settings, SWT.NONE);
                tItem2.setControl(cmp_container);

                cmp_container.setLayout(new GridLayout());
                cmp_container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

                Label lbl_DoubleClick = new Label(cmp_container, I18n.I18N_RTL());
                lbl_DoubleClick.setText(I18n.COLOUR_DoubleClick);

                final Tree tree = new Tree(cmp_container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL, 67);
                treeColours = tree;

                tree.setLinesVisible(true);
                tree.setHeaderVisible(true);
                tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

                TreeColumn trclmn_Description = new TreeColumn(tree, SWT.NONE);
                trclmn_Description.setWidth(598);
                trclmn_Description.setText(I18n.COLOUR_Description);

                TreeColumn trclmn_Colour = new TreeColumn(tree, SWT.NONE);
                trclmn_Colour.setWidth(100);
                trclmn_Colour.setText(I18n.COLOUR_Colour);

                TreeItem trtm_Editor3D = new TreeItem(tree, SWT.NONE);
                trtm_Editor3D.setImage(ResourceManager.getImage("icon16_primitives.png")); //$NON-NLS-1$
                trtm_Editor3D.setText(new String[] { I18n.KEYBOARD_Editor3D, "" }); //$NON-NLS-1$
                trtm_Editor3D.setVisible(true);

                TreeItem trtm_EditorText = new TreeItem(tree, SWT.NONE);
                trtm_EditorText.setImage(ResourceManager.getImage("icon16_annotate.png")); //$NON-NLS-1$
                trtm_EditorText.setText(new String[] { I18n.KEYBOARD_EditorText, "" }); //$NON-NLS-1$
                trtm_EditorText.setVisible(true);

                registerColour(trtm_Editor3D, I18n.COLOUR_OverrideColour16, ColourType.OPENGL_COLOUR, new Object[]{View.Color16_override_r, View.Color16_override_g, View.Color16_override_b});

                registerColour(trtm_Editor3D, I18n.COLOUR_Background3DColour, ColourType.OPENGL_COLOUR, new Object[]{View.background_Colour_r, View.background_Colour_g, View.background_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_BfcBackColour, ColourType.OPENGL_COLOUR, new Object[]{View.BFC_back__Colour_r, View.BFC_back__Colour_g, View.BFC_back__Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_BfcFrontColour, ColourType.OPENGL_COLOUR, new Object[]{View.BFC_front_Colour_r, View.BFC_front_Colour_g, View.BFC_front_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_BfcUncertified, ColourType.OPENGL_COLOUR, new Object[]{View.BFC_uncertified_Colour_r, View.BFC_uncertified_Colour_g, View.BFC_uncertified_Colour_b});

                registerColour(trtm_Editor3D, I18n.COLOUR_OriginColour, ColourType.OPENGL_COLOUR, new Object[]{View.origin_Colour_r, View.origin_Colour_g, View.origin_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_GridColour, ColourType.OPENGL_COLOUR, new Object[]{View.grid_Colour_r, View.grid_Colour_g, View.grid_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_Grid10Colour, ColourType.OPENGL_COLOUR, new Object[]{View.grid10_Colour_r, View.grid10_Colour_g, View.grid10_Colour_b});

                registerColour(trtm_Editor3D, I18n.COLOUR_Cursor1Colour, ColourType.OPENGL_COLOUR, new Object[]{View.cursor1_Colour_r, View.cursor1_Colour_g, View.cursor1_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_Cursor2Colour, ColourType.OPENGL_COLOUR, new Object[]{View.cursor2_Colour_r, View.cursor2_Colour_g, View.cursor2_Colour_b});

                registerColour(trtm_Editor3D, I18n.COLOUR_MeshLineColour, ColourType.OPENGL_COLOUR, new Object[]{View.meshline_Colour_r, View.meshline_Colour_g, View.meshline_Colour_b});

                registerColour(trtm_Editor3D, I18n.COLOUR_AddObjectColour, ColourType.OPENGL_COLOUR, new Object[]{View.add_Object_Colour_r, View.add_Object_Colour_g, View.add_Object_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_CondlineColour, ColourType.OPENGL_COLOUR, new Object[]{View.condline_Colour_r, View.condline_Colour_g, View.condline_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_ShownCondlineColour, ColourType.OPENGL_COLOUR, new Object[]{View.condline_shown_Colour_r, View.condline_shown_Colour_g, View.condline_shown_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_HiddenCondlineColour, ColourType.OPENGL_COLOUR, new Object[]{View.condline_hidden_Colour_r, View.condline_hidden_Colour_g, View.condline_hidden_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_Light1Colour, ColourType.OPENGL_COLOUR, new Object[]{View.light1_Colour_r, View.light1_Colour_g, View.light1_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_Light1SpecularColour, ColourType.OPENGL_COLOUR, new Object[]{View.light1_specular_Colour_r, View.light1_specular_Colour_g, View.light1_specular_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_Light2Colour, ColourType.OPENGL_COLOUR, new Object[]{View.light2_Colour_r, View.light2_Colour_g, View.light2_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_Light2SpecularColour, ColourType.OPENGL_COLOUR, new Object[]{View.light2_specular_Colour_r, View.light2_specular_Colour_g, View.light2_specular_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_Light3Colour, ColourType.OPENGL_COLOUR, new Object[]{View.light3_Colour_r, View.light3_Colour_g, View.light3_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_Light3SpecularColour, ColourType.OPENGL_COLOUR, new Object[]{View.light3_specular_Colour_r, View.light3_specular_Colour_g, View.light3_specular_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_Light4Colour, ColourType.OPENGL_COLOUR, new Object[]{View.light4_Colour_r, View.light4_Colour_g, View.light4_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_Light4SpecularColour, ColourType.OPENGL_COLOUR, new Object[]{View.light4_specular_Colour_r, View.light4_specular_Colour_g, View.light4_specular_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_LineColour, ColourType.OPENGL_COLOUR, new Object[]{View.line_Colour_r, View.line_Colour_g, View.line_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_ManipulatorInnerCircleColour, ColourType.OPENGL_COLOUR, new Object[]{View.manipulator_innerCircle_Colour_r, View.manipulator_innerCircle_Colour_g, View.manipulator_innerCircle_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_ManipulatorOuterCircleColour, ColourType.OPENGL_COLOUR, new Object[]{View.manipulator_outerCircle_Colour_r, View.manipulator_outerCircle_Colour_g, View.manipulator_outerCircle_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_ManipulatorXaxisColour, ColourType.OPENGL_COLOUR, new Object[]{View.manipulator_x_axis_Colour_r, View.manipulator_x_axis_Colour_g, View.manipulator_x_axis_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_ManipulatorYaxisColour, ColourType.OPENGL_COLOUR, new Object[]{View.manipulator_y_axis_Colour_r, View.manipulator_y_axis_Colour_g, View.manipulator_y_axis_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_ManipulatorZaxisColour, ColourType.OPENGL_COLOUR, new Object[]{View.manipulator_z_axis_Colour_r, View.manipulator_z_axis_Colour_g, View.manipulator_z_axis_Colour_b});

                registerColour(trtm_Editor3D, I18n.COLOUR_VertexColour, ColourType.OPENGL_COLOUR, new Object[]{View.vertex_Colour_r, View.vertex_Colour_g, View.vertex_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_SelectedVertexColour, ColourType.OPENGL_COLOUR, new Object[]{View.vertex_selected_Colour_r, View.vertex_selected_Colour_g, View.vertex_selected_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_SelectedCondlineColour, ColourType.OPENGL_COLOUR, new Object[]{View.condline_selected_Colour_r, View.condline_selected_Colour_g, View.condline_selected_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_SelectedManipulatorColour, ColourType.OPENGL_COLOUR, new Object[]{View.manipulator_selected_Colour_r, View.manipulator_selected_Colour_g, View.manipulator_selected_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_RubberBandColour, ColourType.OPENGL_COLOUR, new Object[]{View.rubberBand_Colour_r, View.rubberBand_Colour_g, View.rubberBand_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_TextColour, ColourType.OPENGL_COLOUR, new Object[]{View.text_Colour_r, View.text_Colour_g, View.text_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_XAxisColour, ColourType.OPENGL_COLOUR, new Object[]{View.x_axis_Colour_r, View.x_axis_Colour_g, View.x_axis_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_YAxisColour, ColourType.OPENGL_COLOUR, new Object[]{View.y_axis_Colour_r, View.y_axis_Colour_g, View.y_axis_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_ZAxisColour, ColourType.OPENGL_COLOUR, new Object[]{View.z_axis_Colour_r, View.z_axis_Colour_g, View.z_axis_Colour_b});

                registerColour(trtm_Editor3D, I18n.COLOUR_PrimitveBGColour, ColourType.OPENGL_COLOUR, new Object[]{View.primitive_background_Colour_r, View.primitive_background_Colour_g, View.primitive_background_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_PrimitveSignFGColour, ColourType.OPENGL_COLOUR, new Object[]{View.primitive_signFG_Colour_r, View.primitive_signFG_Colour_g, View.primitive_signFG_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_PrimitveSignBGColour, ColourType.OPENGL_COLOUR, new Object[]{View.primitive_signBG_Colour_r, View.primitive_signBG_Colour_g, View.primitive_signBG_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_PrimitvePlusMinusColour, ColourType.OPENGL_COLOUR, new Object[]{View.primitive_plusNminus_Colour_r, View.primitive_plusNminus_Colour_g, View.primitive_plusNminus_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_PrimitveSelectedCellColour, ColourType.OPENGL_COLOUR, new Object[]{View.primitive_selectedCell_Colour_r, View.primitive_selectedCell_Colour_g, View.primitive_selectedCell_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_PrimitveFocusedCellColour, ColourType.OPENGL_COLOUR, new Object[]{View.primitive_focusedCell_Colour_r, View.primitive_focusedCell_Colour_g, View.primitive_focusedCell_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_PrimitveNormalCellColour, ColourType.OPENGL_COLOUR, new Object[]{View.primitive_normalCell_Colour_r, View.primitive_normalCell_Colour_g, View.primitive_normalCell_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_PrimitveCell1Colour, ColourType.OPENGL_COLOUR, new Object[]{View.primitive_cell_1_Colour_r, View.primitive_cell_1_Colour_g, View.primitive_cell_1_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_PrimitveCell2Colour, ColourType.OPENGL_COLOUR, new Object[]{View.primitive_cell_2_Colour_r, View.primitive_cell_2_Colour_g, View.primitive_cell_2_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_PrimitveCategoryCell1Colour, ColourType.OPENGL_COLOUR, new Object[]{View.primitive_categoryCell_1_Colour_r, View.primitive_categoryCell_1_Colour_g, View.primitive_categoryCell_1_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_PrimitveCategoryCell2Colour, ColourType.OPENGL_COLOUR, new Object[]{View.primitive_categoryCell_2_Colour_r, View.primitive_categoryCell_2_Colour_g, View.primitive_categoryCell_2_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_PrimitveEdgeColour, ColourType.OPENGL_COLOUR, new Object[]{View.primitive_edge_Colour_r, View.primitive_edge_Colour_g, View.primitive_edge_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_PrimitveCondlineColour, ColourType.OPENGL_COLOUR, new Object[]{View.primitive_condline_Colour_r, View.primitive_condline_Colour_g, View.primitive_condline_Colour_b});

                registerColour(trtm_EditorText, I18n.COLOUR_TextEditorBGColour, ColourType.SWT_COLOUR, Colour.text_background);
                registerColour(trtm_EditorText, I18n.COLOUR_TextEditorFGColour, ColourType.SWT_COLOUR, Colour.text_foreground);
                registerColour(trtm_EditorText, I18n.COLOUR_TextEditorFGColourHidden, ColourType.SWT_COLOUR, Colour.text_foreground_hidden);
                registerColour(trtm_EditorText, I18n.COLOUR_TextEditorSelectedBGColour, ColourType.SWT_COLOUR, Colour.line_highlight_selected_background);
                registerColour(trtm_EditorText, I18n.COLOUR_TextEditorHighlightBGColour, ColourType.SWT_COLOUR, Colour.line_highlight_background);
                registerColour(trtm_EditorText, I18n.COLOUR_TextEditorCommentColour, ColourType.SWT_COLOUR, Colour.line_comment_font);
                registerColour(trtm_EditorText, I18n.COLOUR_TextEditorHintUnderlineColour, ColourType.SWT_COLOUR, Colour.line_hint_underline);
                registerColour(trtm_EditorText, I18n.COLOUR_TextEditorWarningUnderlineColour, ColourType.SWT_COLOUR, Colour.line_warning_underline);
                registerColour(trtm_EditorText, I18n.COLOUR_TextEditorErrorUnderlineColour, ColourType.SWT_COLOUR, Colour.line_error_underline);
                registerColour(trtm_EditorText, I18n.COLOUR_TextEditorPrimaryColour, ColourType.SWT_COLOUR, Colour.line_primary_font);
                registerColour(trtm_EditorText, I18n.COLOUR_TextEditorSecondaryColour, ColourType.SWT_COLOUR, Colour.line_secondary_font);
                registerColour(trtm_EditorText, I18n.COLOUR_TextEditorQuadColour, ColourType.SWT_COLOUR, Colour.line_quad_font);
                registerColour(trtm_EditorText, I18n.COLOUR_TextEditorColourAttrColour, ColourType.SWT_COLOUR, Colour.line_colourAttr_font);
                registerColour(trtm_EditorText, I18n.COLOUR_TextEditorBoxColour, ColourType.SWT_COLOUR, Colour.line_box_font);

                tree.build();

                tree.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseDoubleClick(MouseEvent e) {
                        final TreeItem selection;
                        if (tree.getSelectionCount() == 1 && (selection = tree.getSelection()[0]).getData() != null) {
                            ColorDialog dlg = new ColorDialog(getShell());
                            // Change the title bar text
                            dlg.setText(selection.getText(0));
                            dlg.setRGB(selection.getParent().getMapInv().get(selection).getBackground(1).getRGB());
                            // Open the dialog and retrieve the selected color
                            RGB rgb = dlg.open();
                            if (rgb != null) {
                                GColour refCol = new GColour(-1, rgb.red / 255f, rgb.green / 255f, rgb.blue / 255f, 1f);
                                tree.getMapInv().get(selection).setBackground(1, SWTResourceManager.getColor(rgb));
                                Object[] colourObj = (Object[]) selection.getData();
                                ColourType type = (ColourType) colourObj[0];
                                switch (type) {
                                case OPENGL_COLOUR:
                                    ((float[]) ((Object[]) colourObj[1])[0])[0] = refCol.getR();
                                    ((float[]) ((Object[]) colourObj[1])[1])[0] = refCol.getG();
                                    ((float[]) ((Object[]) colourObj[1])[2])[0] = refCol.getB();
                                    break;
                                case SWT_COLOUR:
                                    ((Color[]) colourObj[1])[0] = SWTResourceManager.getColor(rgb) ;
                                    break;
                                default:
                                    break;
                                }

                                for (EditorTextWindow w : Project.getOpenTextWindows()) {
                                    for (CTabItem t : w.getTabFolder().getItems()) {
                                        ((CompositeTab) t).updateColours();
                                    }
                                }
                                tree.build();
                                tree.update();
                            }
                        }
                    }
                });
            }
            tabFolder_Settings.setSelection(tItem0);
        }


        Composite cmp_Buttons = new Composite(container, SWT.NONE);

        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.minimumHeight = 200;
        gridData.minimumWidth = 160;
        gridData.verticalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        cmp_Buttons.setLayoutData(gridData);

        GridLayout gl = new GridLayout(2, true);
        cmp_Buttons.setLayout(gl);

        Composite spacer = new Composite(cmp_Buttons, SWT.NONE);

        GridData gridData2 = new GridData();
        gridData2.horizontalAlignment = SWT.FILL;
        gridData2.grabExcessHorizontalSpace = true;
        spacer.setLayoutData(gridData2);

        NButton btnOK = new NButton(cmp_Buttons, SWT.NONE);
        this.btn_OK[0] = btnOK;
        btnOK.setText(I18n.DIALOG_OK);

        GridData gridData3 = new GridData();
        gridData3.horizontalAlignment = SWT.FILL;
        gridData3.grabExcessHorizontalSpace = true;
        btnOK.setLayoutData(gridData3);

        getShell().addShellListener(new ShellAdapter() {
            @Override
            public void shellActivated(ShellEvent e) {
                updateColours(treeColours);
                treeColours.redraw();
            }
        });

        return container;
    }

    private void updateColours(Tree tree) {
        for(TreeItem ti : tree.getItems()) {
            updateColoursHelper(ti);
        }
    }

    private void updateColoursHelper(TreeItem ti) {
        org.eclipse.swt.widgets.TreeItem key = ti.getParent().getMapInv().get(ti);
        if (key != null && ti.getData() != null && ((Object[]) ti.getData()).length == 2) {
            Object[] colourObj = (Object[]) ti.getData();
            ColourType type = (ColourType) colourObj[0];
            switch (type) {
            case OPENGL_COLOUR:
                key.setBackground(1, SWTResourceManager.getColor(
                        (int) (255f * ((float[])((Object[]) colourObj[1])[0])[0]),
                        (int) (255f * ((float[])((Object[]) colourObj[1])[1])[0]),
                        (int) (255f * ((float[])((Object[]) colourObj[1])[2])[0])));
                break;
            case SWT_COLOUR:
                key.setBackground(1, ((Color[]) colourObj[1])[0]);
                break;
            default:
                break;
            }
        }
        for (TreeItem ti2 : ti.getItems()) {
            updateColoursHelper(ti2);
        }
    }

    private void registerTask(TreeItem parent, String description, Task t, boolean visibility) {
        s1.add(t);
        registerTask(parent, description, t, null, visibility);
    }

    private void registerTask(TreeItem parent, String description, TextTask t, boolean visibility) {
        s2.add(t);
        registerTask(parent, description, null, t, visibility);
    }

    private void registerTask(TreeItem parent, String description, Task t1, TextTask t2, boolean visibility) {

        String keyCombination = ""; //$NON-NLS-1$

        String key = null;

        if (t1 != null) {
            HashMap<Task, String> m = KeyStateManager.getTaskKeymap();
            keyCombination = m.get(t1);
            key = KeyStateManager.getMapKey(t1);
        } else if (t2 != null) {
            HashMap<TextTask, String> m = KeyStateManager.getTextTaskKeymap();
            keyCombination = m.get(t2);
            key = KeyStateManager.getMapKey(t2);
        }
        if (visibility) {
            TreeItem trtm_newKey = new TreeItem(parent, SWT.PUSH);
            trtm_newKey.setText(new String[] { description, keyCombination });
            trtm_newKey.setVisible(true);
            trtm_newKey.setData(new Object[]{t1, t2, key});
        }
    }

    private void registerColour(TreeItem parent, String description, ColourType type, Object[] colourObj) {
        TreeItem trtm_newKey = new TreeItem(parent, SWT.PUSH);
        trtm_newKey.setText(new String[] { description, "" }); //$NON-NLS-1$
        trtm_newKey.setVisible(true);
        trtm_newKey.setData(new Object[]{type, colourObj});
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return new Point(super.getInitialSize().x, (int) (super.getInitialSize().y * 2.6));
    }
}
