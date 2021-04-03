package org.nschmidt.ldparteditor.dialogs.options;

import java.math.BigDecimal;
import java.text.Collator;
import java.text.DateFormat;
import java.util.Arrays;
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
import org.eclipse.swt.widgets.MessageBox;
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
    NButton[] btn_invertInvertWheelZoomDirection = new NButton[1];
    NButton[] btn_OK = new NButton[1];

    final Combo[] cmb_textWinArr = new Combo[1];
    final Combo[] cmb_locale = new Combo[1];
    final Combo[] cmb_mouseButtonLayout = new Combo[1];
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
    final BigDecimalSpinner[] spn_viewportScale = new BigDecimalSpinner[1];
    final HashMap<String, Locale> localeMap = new HashMap<>();

    private HashSet<Task>  s1 = new HashSet<>();
    private HashSet<TextTask> s2 = new HashSet<>();

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
        final UserSettingState userSettings = WorkbenchManager.getUserSettingState();
        final Tree treeColours;
        setStatus(I18n.E3D_READY_STATUS);
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
            tItem0.setText(I18n.OPTIONS_MISC_OPTIONS);

            {

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
                btn_AllowInvalidShapes.setText(I18n.OPTIONS_ALLOW_INVALID_SHAPES);
                btn_AllowInvalidShapes.setSelection(userSettings.isAllowInvalidShapes());

                NButton btn_translateViewByCursor = new NButton(cmp_container, SWT.CHECK);
                this.btn_translateViewByCursor[0] = btn_translateViewByCursor;
                btn_translateViewByCursor.setText(I18n.OPTIONS_TRANSLATE_BY_CURSOR);
                btn_translateViewByCursor.setSelection(userSettings.isTranslatingViewByCursor());

                NButton btn_disableMAD3D = new NButton(cmp_container, SWT.CHECK);
                this.btn_disableMAD3D[0] = btn_disableMAD3D;
                btn_disableMAD3D.setText(I18n.OPTIONS_MAD_1);
                btn_disableMAD3D.setSelection(userSettings.isDisableMAD3D());

                NButton btn_disableMADtext = new NButton(cmp_container, SWT.CHECK);
                this.btn_disableMADtext[0] = btn_disableMADtext;
                btn_disableMADtext.setText(I18n.OPTIONS_MAD_2);
                btn_disableMADtext.setSelection(userSettings.isDisableMADtext());

                {
                    Label lbl_separator = new Label(cmp_container, SWT.SEPARATOR | SWT.HORIZONTAL);
                    lbl_separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                }

                Label lbl_textWinArr = new Label(cmp_container, SWT.NONE);
                lbl_textWinArr.setText(I18n.OPTIONS_TEXT_WINDOW_ARR);

                Combo cmb_textWinArr = new Combo(cmp_container, SWT.READ_ONLY);
                this.cmb_textWinArr[0] = cmb_textWinArr;
                cmb_textWinArr.setItems(new String[]{I18n.OPTIONS_TEXT_WINDOW_SEPARATE, I18n.OPTIONS_TEXT_WINDOW_LEFT, I18n.OPTIONS_TEXT_WINDOW_RIGHT});
                cmb_textWinArr.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                cmb_textWinArr.select(userSettings.getTextWinArr());

                {
                    Label lbl_separator = new Label(cmp_container, SWT.SEPARATOR | SWT.HORIZONTAL);
                    lbl_separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                }

                Label lbl_coplanarityWarning = new Label(cmp_container, SWT.NONE);
                lbl_coplanarityWarning.setText(I18n.OPTIONS_COPLANARITY_WARNING);
                lbl_coplanarityWarning.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

                BigDecimalSpinner spn_coplanarityWarning = new BigDecimalSpinner(cmp_container, Cocoa.getStyle());
                this.spn_coplanarityWarning[0] = spn_coplanarityWarning;
                spn_coplanarityWarning.setMaximum(new BigDecimal("179.9999")); //$NON-NLS-1$
                spn_coplanarityWarning.setMinimum(new BigDecimal("0.01")); //$NON-NLS-1$
                spn_coplanarityWarning.setValue(new BigDecimal(Threshold.coplanarity_angle_warning));
                spn_coplanarityWarning.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

                Label lbl_coplanarityError = new Label(cmp_container, SWT.NONE);
                lbl_coplanarityError.setText(I18n.OPTIONS_COPLANARITY_ERROR);
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

                Label lbl_viewportScale = new Label(cmp_container, SWT.NONE);
                lbl_viewportScale.setText(I18n.OPTIONS_SCALE_FACTOR);
                lbl_viewportScale.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

                BigDecimalSpinner spn_viewportScale = new BigDecimalSpinner(cmp_container, Cocoa.getStyle());
                this.spn_viewportScale[0] = spn_viewportScale;
                spn_viewportScale.setMaximum(new BigDecimal("10")); //$NON-NLS-1$
                spn_viewportScale.setMinimum(new BigDecimal("0.1")); //$NON-NLS-1$
                spn_viewportScale.setValue(new BigDecimal(userSettings.getViewportScaleFactor()));
                spn_viewportScale.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

                {
                    Label lbl_separator = new Label(cmp_container, SWT.SEPARATOR | SWT.HORIZONTAL);
                    lbl_separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                }

                Label lbl_locale = new Label(cmp_container, SWT.NONE);
                lbl_locale.setText(I18n.OPTIONS_CHOOSE_LOCALE);

                Combo cmb_locale = new Combo(cmp_container, SWT.READ_ONLY);
                this.cmb_locale[0] = cmb_locale;

                String[] locales = new String[DateFormat.getAvailableLocales().length];
                Locale[] locs = DateFormat.getAvailableLocales();
                final Locale l = userSettings.getLocale();
                Arrays.sort(locs, (o1, o2) ->
                    Collator.getInstance(Locale.ENGLISH).compare(o1.getDisplayName(l), o2.getDisplayName(l))
                );
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
                lbl_ldrawFolderQuestion.setText(I18n.OPTIONS_LDRAW_FOLDER);

                Composite cmp_pathChooser1 = new Composite(cmp_container, SWT.NONE);
                cmp_pathChooser1.setLayout(new RowLayout(SWT.HORIZONTAL));

                Text txt_ldrawPath = new Text(cmp_pathChooser1, SWT.BORDER);
                this.txt_ldrawPath[0] = txt_ldrawPath;
                txt_ldrawPath.setEditable(false);
                txt_ldrawPath.setLayoutData(new RowData(294, SWT.DEFAULT));
                txt_ldrawPath.setText(userSettings.getLdrawFolderPath());

                NButton btn_BrowseLdrawPath = new NButton(cmp_pathChooser1, SWT.NONE);
                this.btn_browseLdrawPath[0] = btn_BrowseLdrawPath;
                btn_BrowseLdrawPath.setText(I18n.OPTIONS_BROWSE);

                Label lbl_ldrawUserQuestion = new Label(cmp_container, SWT.NONE);
                lbl_ldrawUserQuestion.setText(I18n.OPTIONS_LDRAW_NAME);

                Text txt_ldrawUserName = new Text(cmp_container, SWT.BORDER);
                this.txt_ldrawUserName[0] = txt_ldrawUserName;
                txt_ldrawUserName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                txt_ldrawUserName.setText(userSettings.getLdrawUserName());

                Label lbl_realNameQuestion = new Label(cmp_container, SWT.NONE);
                lbl_realNameQuestion.setText(I18n.OPTIONS_REAL_NAME);

                Text txt_realName = new Text(cmp_container, SWT.BORDER);
                this.txt_realName[0] = txt_realName;
                txt_realName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                txt_realName.setText(userSettings.getRealUserName());

                Label lbl_licenseQuestion = new Label(cmp_container, SWT.NONE);
                lbl_licenseQuestion.setText(I18n.OPTIONS_LICENSE);

                Combo cmb_license = new Combo(cmp_container, SWT.NONE);
                this.cmb_license[0] = cmb_license;
                cmb_license.setItems(new String[] { "0 !LICENSE Redistributable under CCAL version 2.0 : see CAreadme.txt", "0 !LICENSE Not redistributable : see NonCAreadme.txt" }); //$NON-NLS-1$ //$NON-NLS-2$
                cmb_license.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                cmb_license.setText(userSettings.getLicense());

                Label lbl_authoringFolderQuestion = new Label(cmp_container, SWT.NONE);
                lbl_authoringFolderQuestion.setText(I18n.OPTIONS_AUTHORING_FOLDER);

                Composite cmp_pathChooser2 = new Composite(cmp_container, SWT.NONE);
                cmp_pathChooser2.setLayout(new RowLayout(SWT.HORIZONTAL));

                Text txt_partAuthoringPath = new Text(cmp_pathChooser2, SWT.BORDER);
                this.txt_partAuthoringPath[0] = txt_partAuthoringPath;
                txt_partAuthoringPath.setEditable(false);
                txt_partAuthoringPath.setLayoutData(new RowData(294, SWT.DEFAULT));
                txt_partAuthoringPath.setText(userSettings.getAuthoringFolderPath());

                NButton btn_browseAuthoringPath = new NButton(cmp_pathChooser2, SWT.NONE);
                this.btn_browseAuthoringPath[0] = btn_browseAuthoringPath;
                btn_browseAuthoringPath.setText(I18n.OPTIONS_BROWSE);

                Label lbl_unofficialPathQuestion = new Label(cmp_container, SWT.NONE);
                lbl_unofficialPathQuestion.setText(I18n.OPTIONS_UNOFFICIAL_FOLDER);

                Composite cmp_pathChooser3 = new Composite(cmp_container, SWT.NONE);
                cmp_pathChooser3.setLayout(new RowLayout(SWT.HORIZONTAL));

                Text txt_unofficialPath = new Text(cmp_pathChooser3, SWT.BORDER);
                this.txt_unofficialPath[0] = txt_unofficialPath;
                txt_unofficialPath.setEditable(false);
                txt_unofficialPath.setLayoutData(new RowData(294, SWT.DEFAULT));
                txt_unofficialPath.setText(userSettings.getUnofficialFolderPath());

                NButton btn_browseUnofficialPath = new NButton(cmp_pathChooser3, SWT.NONE);
                this.btn_browseUnofficialPath[0] = btn_browseUnofficialPath;
                btn_browseUnofficialPath.setText(I18n.OPTIONS_BROWSE);

                cmp_containerX.setMinSize(cmp_container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
            }

            final CTabItem tItem1 = new CTabItem(tabFolder_Settings, SWT.NONE);
            tItem1.setText(I18n.KEYBOARD_CUSTOMISE_SHORTKEYS);
            {
                final Composite cmp_container = new Composite(tabFolder_Settings, SWT.NONE);
                tItem1.setControl(cmp_container);

                cmp_container.setLayout(new GridLayout());
                cmp_container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

                Label lbl_mouseButtonLayout = new Label(cmp_container, SWT.NONE);
                lbl_mouseButtonLayout.setText(I18n.KEYBOARD_MOUSE_BUTTON_LAYOUT_TITLE);

                Combo cmb_mouseButtonLayout = new Combo(cmp_container, SWT.READ_ONLY);
                this.cmb_mouseButtonLayout[0] = cmb_mouseButtonLayout;
                cmb_mouseButtonLayout.setItems(new String[]{I18n.KEYBOARD_MOUSE_BUTTON_LAYOUT_A, I18n.KEYBOARD_MOUSE_BUTTON_LAYOUT_B});
                cmb_mouseButtonLayout.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                cmb_mouseButtonLayout.select(userSettings.getMouseButtonLayout());

                NButton btn_invertInvertWheelZoomDirection = new NButton(cmp_container, SWT.CHECK);
                this.btn_invertInvertWheelZoomDirection[0] = btn_invertInvertWheelZoomDirection;
                btn_invertInvertWheelZoomDirection.setText(I18n.KEYBOARD_INVERT_WHEEL_ZOOM_DIRECTION);
                btn_invertInvertWheelZoomDirection.setSelection(userSettings.isInvertingWheelZoomDirection());

                {
                    Label lbl_separator = new Label(cmp_container, SWT.SEPARATOR | SWT.HORIZONTAL);
                    lbl_separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                }

                Label lbl_DoubleClick = new Label(cmp_container, I18n.rightToLeftStyle());
                lbl_DoubleClick.setText(I18n.KEYBOARD_DOUBLE_CLICK);

                final Tree tree = new Tree(cmp_container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL, Task.values().length + TextTask.values().length - 24);

                tree.setLinesVisible(true);
                tree.setHeaderVisible(true);
                tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

                TreeColumn trclmn_Description = new TreeColumn(tree, SWT.NONE);
                trclmn_Description.setWidth(598);
                trclmn_Description.setText(I18n.KEYBOARD_DESCRIPTION);

                TreeColumn trclmn_Location = new TreeColumn(tree, SWT.NONE);
                trclmn_Location.setWidth(100);
                trclmn_Location.setText(I18n.KEYBOARD_SHORTKEY);

                TreeItem trtm_Editor3D = new TreeItem(tree);
                trtm_Editor3D.setImage(ResourceManager.getImage("icon16_primitives.png")); //$NON-NLS-1$
                trtm_Editor3D.setText(new String[] { I18n.KEYBOARD_EDITOR_3D, "" }); //$NON-NLS-1$
                trtm_Editor3D.setVisible(true);

                TreeItem trtm_EditorText = new TreeItem(tree);
                trtm_EditorText.setImage(ResourceManager.getImage("icon16_annotate.png")); //$NON-NLS-1$
                trtm_EditorText.setText(new String[] { I18n.KEYBOARD_EDITOR_TEXT, "" }); //$NON-NLS-1$
                trtm_EditorText.setVisible(true);

                registerTask(trtm_Editor3D, I18n.KEYBOARD_LMB, Task.LMB, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_MMB, Task.MMB, !Cocoa.isCocoa);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_RMB, Task.RMB, !Cocoa.isCocoa);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_CLOSE_VIEW, Task.CLOSE_VIEW, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_TOGGLE_INSERT_AT_CURSOR, Task.INSERT_AT_CURSOR, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_ADD_COMMENT, Task.ADD_COMMENTS, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_ADD_VERTEX, Task.ADD_VERTEX, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_ADD_LINE, Task.ADD_LINE, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_ADD_TRIANGLE, Task.ADD_TRIANGLE, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_ADD_QUAD, Task.ADD_QUAD, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_ADD_CONDLINE, Task.ADD_CONDLINE, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_ADD_DISTANCE, Task.ADD_DISTANCE, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_ADD_PROTRACTOR, Task.ADD_PROTRACTOR, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_CUT, Task.CUT, false);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_COPY, Task.COPY, false);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_PASTE, Task.PASTE, false);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_DELETE, Task.DELETE, false);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_ESC_1, Task.ESC, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_MERGE_TO_AVG, Task.MERGE_TO_AVERAGE, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_MERGE_TO_LAST, Task.MERGE_TO_LAST, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_SPLIT, Task.SPLIT, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_FLIP_ROTATE, Task.FLIP_ROTATE_VERTICES, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_MODE_COMBINED, Task.MODE_COMBINED, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_MODE_SELECT, Task.MODE_SELECT, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_MODE_MOVE, Task.MODE_MOVE, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_MODE_ROTATE, Task.MODE_ROTATE, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_MODE_SCALE, Task.MODE_SCALE, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_MOVE_TO_AVG, Task.MOVE_TO_AVG, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_MOVE_ADJACENT_DATA, Task.MOVE_ADJACENT_DATA, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_SWAP_WINDING, Task.SWAP_WINDING, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_OBJ_VERTEX, Task.OBJ_VERTEX, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_OBJ_LINE, Task.OBJ_LINE, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_OBJ_FACE, Task.OBJ_FACE, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_OBJ_PRIMITIVE, Task.OBJ_PRIMITIVE, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_AXIS_X, Task.MODE_X, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_AXIS_Y, Task.MODE_Y, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_AXIS_Z, Task.MODE_Z, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_AXIS_XY, Task.MODE_XY, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_AXIS_XZ, Task.MODE_XZ, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_AXIS_YZ, Task.MODE_YZ, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_AXIS_XYZ, Task.MODE_XYZ, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_RESET_MANIPULATOR, Task.RESET_MANIPULATOR, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_RESET_VIEW, Task.RESET_VIEW, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_SAVE, Task.SAVE, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_SELECT_ALL, Task.SELECT_ALL, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_SELECT_ALL_WITH_SAME_COLOURS, Task.SELECT_ALL_WITH_SAME_COLOURS, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_SELECT_CONNECTED, Task.SELECT_CONNECTED, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_SELECT_NONE, Task.SELECT_NONE, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_SELECT_OPTION_WITH_SAME_COLOURS, Task.SELECT_OPTION_WITH_SAME_COLOURS, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_SELECT_TOUCHING, Task.SELECT_TOUCHING, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_SHOW_GRID, Task.SHOW_GRID, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_SHOW_RULER, Task.SHOW_RULER, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_UNDO, Task.UNDO, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_REDO, Task.REDO, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_ZOOM_IN, Task.ZOOM_IN, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_ZOOM_OUT, Task.ZOOM_OUT, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_RENDERMODE_NO_BACKFACE_CULLING, Task.RENDERMODE_NoBackfaceCulling, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_RENDERMODE_RANDOM_COLOURS, Task.RENDERMODE_RandomColours, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_RENDERMODE_GREEN_FRONTFACES_RED_BACKFACES, Task.RENDERMODE_GreenFrontfacesRedBackfaces, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_RENDERMODE_RED_BACKFACES, Task.RENDERMODE_RedBackfaces, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_RENDERMODE_REAL_BACKFACE_CULLING, Task.RENDERMODE_RealBackfaceCulling, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_RENDERMODE_LDRAW_STANDARD, Task.RENDERMODE_LDrawStandard, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_RENDERMODE_CONDLINE_MODE, Task.RENDERMODE_SpecialCondline, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_RENDERMODE_COPLANARITY_HEATMAP, Task.RENDERMODE_CoplanarityHeatmap, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_RENDERMODE_WIREFRAME, Task.RENDERMODE_Wireframe, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_PERSPECTIVE_FRONT, Task.PERSPECTIVE_FRONT, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_PERSPECTIVE_BACK, Task.PERSPECTIVE_BACK, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_PERSPECTIVE_LEFT, Task.PERSPECTIVE_LEFT, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_PERSPECTIVE_RIGHT, Task.PERSPECTIVE_RIGHT, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_PERSPECTIVE_TOP, Task.PERSPECTIVE_TOP, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_PERSPECTIVE_BOTTOM, Task.PERSPECTIVE_BOTTOM, true);
                registerTask(trtm_Editor3D, I18n.KEYBOARD_PERSPECTIVE_TWO_THIRDS, Task.PERSPECTIVE_TwoThirds, true);

                registerTask(trtm_EditorText, I18n.KEYBOARD_ESC_2, TextTask.EDITORTEXT_ESC, true);
                registerTask(trtm_EditorText, I18n.KEYBOARD_INLINE, TextTask.EDITORTEXT_INLINE, true);
                registerTask(trtm_EditorText, I18n.KEYBOARD_QUICK_FIX, TextTask.EDITORTEXT_QUICKFIX, true);
                registerTask(trtm_EditorText, I18n.KEYBOARD_REDO, TextTask.EDITORTEXT_REDO, true);
                registerTask(trtm_EditorText, I18n.KEYBOARD_REPLACE_VERTEX, TextTask.EDITORTEXT_REPLACE_VERTEX, true);
                registerTask(trtm_EditorText, I18n.KEYBOARD_ROUND, TextTask.EDITORTEXT_ROUND, true);
                registerTask(trtm_EditorText, I18n.KEYBOARD_SAVE, TextTask.EDITORTEXT_SAVE, true);
                registerTask(trtm_EditorText, I18n.KEYBOARD_SELECT_ALL, TextTask.EDITORTEXT_SELECTALL, true);
                registerTask(trtm_EditorText, I18n.KEYBOARD_UNDO, TextTask.EDITORTEXT_UNDO, true);
                registerTask(trtm_EditorText, I18n.KEYBOARD_FIND_REPLACE, TextTask.EDITORTEXT_FIND, true);
                registerTask(trtm_EditorText, I18n.KEYBOARD_INSERT_HISTORY, TextTask.EDITORTEXT_INSERT_HISTORY, true);
                registerTask(trtm_EditorText, I18n.KEYBOARD_INSERT_KEYWORD, TextTask.EDITORTEXT_INSERT_KEYWORD, true);
                registerTask(trtm_EditorText, I18n.KEYBOARD_INSERT_REFERENCE, TextTask.EDITORTEXT_INSERT_REFERENCE, true);
                registerTask(trtm_EditorText, I18n.KEYBOARD_MOVE_LINE_UP, TextTask.EDITORTEXT_LINE_UP, true);
                registerTask(trtm_EditorText, I18n.KEYBOARD_MOVE_LINE_DOWN, TextTask.EDITORTEXT_LINE_DOWN, true);

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
                                if (data[0] == null) {
                                    if (KeyStateManager.hasTextTaskKey(KeyStateManager.tmp_mapKey)) {
                                        showKeyAlreadyInUseWarning(selection.getParentItem(), KeyStateManager.tmp_keyString);
                                    } else {
                                        KeyStateManager.changeKey(KeyStateManager.tmp_mapKey, KeyStateManager.tmp_keyString, (TextTask) data[1]);
                                        selection.setText(new String[]{selection.getText(0), KeyStateManager.tmp_keyString});
                                    }
                                }
                                if (data[1] == null) {
                                    if (KeyStateManager.hasTaskKey(KeyStateManager.tmp_mapKey)) {
                                        showKeyAlreadyInUseWarning(selection.getParentItem(), KeyStateManager.tmp_keyString);
                                    } else {
                                        KeyStateManager.changeKey(KeyStateManager.tmp_mapKey, KeyStateManager.tmp_keyString, (Task) data[0]);
                                        selection.setText(new String[]{selection.getText(0), KeyStateManager.tmp_keyString});
                                    }
                                }
                                tree.build();
                                tree.update();
                            }
                        }
                    }
                });
            }

            CTabItem tItem2 = new CTabItem(tabFolder_Settings, SWT.NONE);
            tItem2.setText(I18n.COLOUR_CUSTOMISE_COLOURS);

            {
                final Composite cmp_container = new Composite(tabFolder_Settings, SWT.NONE);
                tItem2.setControl(cmp_container);

                cmp_container.setLayout(new GridLayout());
                cmp_container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

                Label lbl_DoubleClick = new Label(cmp_container, I18n.rightToLeftStyle());
                lbl_DoubleClick.setText(I18n.COLOUR_DOUBLE_CLICK);

                final Tree tree = new Tree(cmp_container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL, 67);
                treeColours = tree;

                tree.setLinesVisible(true);
                tree.setHeaderVisible(true);
                tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

                TreeColumn trclmn_Description = new TreeColumn(tree, SWT.NONE);
                trclmn_Description.setWidth(598);
                trclmn_Description.setText(I18n.COLOUR_DESCRIPTION);

                TreeColumn trclmn_Colour = new TreeColumn(tree, SWT.NONE);
                trclmn_Colour.setWidth(100);
                trclmn_Colour.setText(I18n.COLOUR_COLOUR);

                TreeItem trtm_Editor3D = new TreeItem(tree);
                trtm_Editor3D.setImage(ResourceManager.getImage("icon16_primitives.png")); //$NON-NLS-1$
                trtm_Editor3D.setText(new String[] { I18n.KEYBOARD_EDITOR_3D, "" }); //$NON-NLS-1$
                trtm_Editor3D.setVisible(true);

                TreeItem trtm_EditorText = new TreeItem(tree);
                trtm_EditorText.setImage(ResourceManager.getImage("icon16_annotate.png")); //$NON-NLS-1$
                trtm_EditorText.setText(new String[] { I18n.KEYBOARD_EDITOR_TEXT, "" }); //$NON-NLS-1$
                trtm_EditorText.setVisible(true);

                registerColour(trtm_Editor3D, I18n.COLOUR_OVERRIDE_COLOUR_16, ColourType.OPENGL_COLOUR, new Object[]{View.Color16_override_r, View.Color16_override_g, View.Color16_override_b});

                registerColour(trtm_Editor3D, I18n.COLOUR_BACKGROUND_3D_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.background_Colour_r, View.background_Colour_g, View.background_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_BFC_BACK_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.BFC_back__Colour_r, View.BFC_back__Colour_g, View.BFC_back__Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_BFC_FRONT_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.BFC_front_Colour_r, View.BFC_front_Colour_g, View.BFC_front_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_BFC_UNCERTIFIED, ColourType.OPENGL_COLOUR, new Object[]{View.BFC_uncertified_Colour_r, View.BFC_uncertified_Colour_g, View.BFC_uncertified_Colour_b});

                registerColour(trtm_Editor3D, I18n.COLOUR_ORIGIN_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.origin_Colour_r, View.origin_Colour_g, View.origin_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_GRID_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.grid_Colour_r, View.grid_Colour_g, View.grid_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_GRID_10_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.grid10_Colour_r, View.grid10_Colour_g, View.grid10_Colour_b});

                registerColour(trtm_Editor3D, I18n.COLOUR_CURSOR_1_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.cursor1_Colour_r, View.cursor1_Colour_g, View.cursor1_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_CURSOR_2_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.cursor2_Colour_r, View.cursor2_Colour_g, View.cursor2_Colour_b});

                registerColour(trtm_Editor3D, I18n.COLOUR_MESH_LINE_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.meshline_Colour_r, View.meshline_Colour_g, View.meshline_Colour_b});

                registerColour(trtm_Editor3D, I18n.COLOUR_ADD_OBJECT_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.add_Object_Colour_r, View.add_Object_Colour_g, View.add_Object_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_CONDLINE_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.condline_Colour_r, View.condline_Colour_g, View.condline_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_SHOWN_CONDLINE_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.condline_shown_Colour_r, View.condline_shown_Colour_g, View.condline_shown_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_HIDDEN_CONDLINE_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.condline_hidden_Colour_r, View.condline_hidden_Colour_g, View.condline_hidden_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_LIGHT_1_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.light1_Colour_r, View.light1_Colour_g, View.light1_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_LIGHT_1_SPECULAR_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.light1_specular_Colour_r, View.light1_specular_Colour_g, View.light1_specular_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_LIGHT_2_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.light2_Colour_r, View.light2_Colour_g, View.light2_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_LIGHT_2_SPECULAR_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.light2_specular_Colour_r, View.light2_specular_Colour_g, View.light2_specular_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_LIGHT_3_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.light3_Colour_r, View.light3_Colour_g, View.light3_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_LIGHT_3_SPECULAR_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.light3_specular_Colour_r, View.light3_specular_Colour_g, View.light3_specular_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_LIGHT_4_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.light4_Colour_r, View.light4_Colour_g, View.light4_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_LIGHT_4_SPECULAR_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.light4_specular_Colour_r, View.light4_specular_Colour_g, View.light4_specular_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_LINE_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.line_Colour_r, View.line_Colour_g, View.line_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_MANIPULATOR_INNER_CIRCLE_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.manipulator_innerCircle_Colour_r, View.manipulator_innerCircle_Colour_g, View.manipulator_innerCircle_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_MANIPULATOR_OUTER_CIRCLE_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.manipulator_outerCircle_Colour_r, View.manipulator_outerCircle_Colour_g, View.manipulator_outerCircle_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_MANIPULATOR_X_AXIS_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.manipulator_x_axis_Colour_r, View.manipulator_x_axis_Colour_g, View.manipulator_x_axis_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_MANIPULATOR_Y_AXIS_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.manipulator_y_axis_Colour_r, View.manipulator_y_axis_Colour_g, View.manipulator_y_axis_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_MANIPULATOR_Z_AXIS_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.manipulator_z_axis_Colour_r, View.manipulator_z_axis_Colour_g, View.manipulator_z_axis_Colour_b});

                registerColour(trtm_Editor3D, I18n.COLOUR_VERTEX_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.vertex_Colour_r, View.vertex_Colour_g, View.vertex_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_SELECTED_VERTEX_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.vertex_selected_Colour_r, View.vertex_selected_Colour_g, View.vertex_selected_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_SELECTED_CONDLINE_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.condline_selected_Colour_r, View.condline_selected_Colour_g, View.condline_selected_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_SELECTED_MANIPULATOR_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.manipulator_selected_Colour_r, View.manipulator_selected_Colour_g, View.manipulator_selected_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_RUBBERBAND_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.rubberBand_Colour_r, View.rubberBand_Colour_g, View.rubberBand_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_TEXT_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.text_Colour_r, View.text_Colour_g, View.text_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_X_AXIS_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.x_axis_Colour_r, View.x_axis_Colour_g, View.x_axis_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_Y_AXIS_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.y_axis_Colour_r, View.y_axis_Colour_g, View.y_axis_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_Z_AXIS_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.z_axis_Colour_r, View.z_axis_Colour_g, View.z_axis_Colour_b});

                registerColour(trtm_Editor3D, I18n.COLOUR_PRIMITVE_BG_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.primitive_background_Colour_r, View.primitive_background_Colour_g, View.primitive_background_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_PRIMITVE_SIGN_FG_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.primitive_signFG_Colour_r, View.primitive_signFG_Colour_g, View.primitive_signFG_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_PRIMITVE_SIGN_BG_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.primitive_signBG_Colour_r, View.primitive_signBG_Colour_g, View.primitive_signBG_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_PRIMITVE_PLUS_MINUS_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.primitive_plusNminus_Colour_r, View.primitive_plusNminus_Colour_g, View.primitive_plusNminus_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_PRIMITVE_SELECTED_CELL_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.primitive_selectedCell_Colour_r, View.primitive_selectedCell_Colour_g, View.primitive_selectedCell_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_PRIMITVE_FOCUSED_CELL_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.primitive_focusedCell_Colour_r, View.primitive_focusedCell_Colour_g, View.primitive_focusedCell_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_PRIMITVE_NORMAL_CELL_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.primitive_normalCell_Colour_r, View.primitive_normalCell_Colour_g, View.primitive_normalCell_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_PRIMITVE_CELL_1_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.primitive_cell_1_Colour_r, View.primitive_cell_1_Colour_g, View.primitive_cell_1_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_PRIMITVE_CELL_2_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.primitive_cell_2_Colour_r, View.primitive_cell_2_Colour_g, View.primitive_cell_2_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_PRIMITVE_CATEGORY_CELL_1_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.primitive_categoryCell_1_Colour_r, View.primitive_categoryCell_1_Colour_g, View.primitive_categoryCell_1_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_PRIMITVE_CATEGORY_CELL_2_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.primitive_categoryCell_2_Colour_r, View.primitive_categoryCell_2_Colour_g, View.primitive_categoryCell_2_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_PRIMITVE_EDGE_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.primitive_edge_Colour_r, View.primitive_edge_Colour_g, View.primitive_edge_Colour_b});
                registerColour(trtm_Editor3D, I18n.COLOUR_PRIMITVE_CONDLINE_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{View.primitive_condline_Colour_r, View.primitive_condline_Colour_g, View.primitive_condline_Colour_b});

                registerColour(trtm_EditorText, I18n.COLOUR_TEXT_EDITOR_BG_COLOUR, ColourType.SWT_COLOUR, Colour.text_background);
                registerColour(trtm_EditorText, I18n.COLOUR_TEXT_EDITOR_FG_COLOUR, ColourType.SWT_COLOUR, Colour.text_foreground);
                registerColour(trtm_EditorText, I18n.COLOUR_TEXT_EDITOR_FG_COLOUR_HIDDEN, ColourType.SWT_COLOUR, Colour.text_foreground_hidden);
                registerColour(trtm_EditorText, I18n.COLOUR_TEXT_EDITOR_SELECTED_BG_COLOUR, ColourType.SWT_COLOUR, Colour.line_highlight_selected_background);
                registerColour(trtm_EditorText, I18n.COLOUR_TEXT_EDITOR_HIGHLIGHT_BG_COLOUR, ColourType.SWT_COLOUR, Colour.line_highlight_background);
                registerColour(trtm_EditorText, I18n.COLOUR_TEXT_EDITOR_COMMENT_COLOUR, ColourType.SWT_COLOUR, Colour.line_comment_font);
                registerColour(trtm_EditorText, I18n.COLOUR_TEXT_EDITOR_HINT_UNDERLINE_COLOUR, ColourType.SWT_COLOUR, Colour.line_hint_underline);
                registerColour(trtm_EditorText, I18n.COLOUR_TEXT_EDITOR_WARNING_UNDERLINE_COLOUR, ColourType.SWT_COLOUR, Colour.line_warning_underline);
                registerColour(trtm_EditorText, I18n.COLOUR_TEXT_EDITOR_ERROR_UNDERLINE_COLOUR, ColourType.SWT_COLOUR, Colour.line_error_underline);
                registerColour(trtm_EditorText, I18n.COLOUR_TEXT_EDITOR_PRIMARY_COLOUR, ColourType.SWT_COLOUR, Colour.line_primary_font);
                registerColour(trtm_EditorText, I18n.COLOUR_TEXT_EDITOR_SECONDARY_COLOUR, ColourType.SWT_COLOUR, Colour.line_secondary_font);
                registerColour(trtm_EditorText, I18n.COLOUR_TEXT_EDITOR_QUAD_COLOUR, ColourType.SWT_COLOUR, Colour.line_quad_font);
                registerColour(trtm_EditorText, I18n.COLOUR_TEXT_EDITOR_COLOUR_ATTR_COLOUR, ColourType.SWT_COLOUR, Colour.line_colourAttr_font);
                registerColour(trtm_EditorText, I18n.COLOUR_TEXT_EDITOR_BOX_COLOUR, ColourType.SWT_COLOUR, Colour.line_box_font);

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

    private void showKeyAlreadyInUseWarning(TreeItem parentItem, String keyString) {
        String actionString = "advanced usage by LDPartEditor"; //$NON-NLS-1$ I18N Needs translation!
        final MessageBox messageBoxInfo = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
        final HashMap<String, String> reservedKeysMappedToAction = new HashMap<>();
        final String inputTheColourNumber = "colour number input";//$NON-NLS-1$ I18N Needs translation!

        for (int n = 0; n < 10; n++) {
            reservedKeysMappedToAction.put("NUMPAD_" + n, inputTheColourNumber); //$NON-NLS-1$ I18N Needs translation!
        }

        if (reservedKeysMappedToAction.containsKey(keyString)) {
            actionString = reservedKeysMappedToAction.get(keyString);
        } else {
            for (TreeItem item : parentItem.getItems()) {
                if (keyString.equals(item.getText(1))) {
                    actionString = item.getText(0);
                    break;
                }
            }
        }

        messageBoxInfo.setText(I18n.DIALOG_INFO);
        messageBoxInfo.setMessage("The key combination " + keyString + " is already in use for '" + actionString + "'."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ I18N Needs translation!
        messageBoxInfo.open();
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

        if (t1 != null) {
            HashMap<Task, String> m = KeyStateManager.getTaskKeymap();
            keyCombination = m.get(t1);
        } else if (t2 != null) {
            HashMap<TextTask, String> m = KeyStateManager.getTextTaskKeymap();
            keyCombination = m.get(t2);
        }
        if (visibility) {
            TreeItem trtm_newKey = new TreeItem(parent);
            trtm_newKey.setText(new String[] { description, keyCombination });
            trtm_newKey.setVisible(true);
            trtm_newKey.setData(new Object[]{t1, t2});
        }
    }

    private void registerColour(TreeItem parent, String description, ColourType type, Object[] colourObj) {
        TreeItem trtm_newKey = new TreeItem(parent);
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
