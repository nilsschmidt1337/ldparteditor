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
package org.nschmidt.ldparteditor.dialog.options;

import static org.nschmidt.ldparteditor.helper.WidgetUtility.widgetUtil;

import java.math.BigDecimal;
import java.text.Collator;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
import org.nschmidt.ldparteditor.composite.compositetab.CompositeTab;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.dialog.key.KeyDialog;
import org.nschmidt.ldparteditor.enumtype.Colour;
import org.nschmidt.ldparteditor.enumtype.LDConfig;
import org.nschmidt.ldparteditor.enumtype.MyLanguage;
import org.nschmidt.ldparteditor.enumtype.Task;
import org.nschmidt.ldparteditor.enumtype.TextEditorColour;
import org.nschmidt.ldparteditor.enumtype.TextTask;
import org.nschmidt.ldparteditor.enumtype.Threshold;
import org.nschmidt.ldparteditor.helper.Cocoa;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.resource.ResourceManager;
import org.nschmidt.ldparteditor.shell.editortext.EditorTextWindow;
import org.nschmidt.ldparteditor.state.KeyStateManager;
import org.nschmidt.ldparteditor.widget.BigDecimalSpinner;
import org.nschmidt.ldparteditor.widget.IntegerSpinner;
import org.nschmidt.ldparteditor.widget.NButton;
import org.nschmidt.ldparteditor.widget.Tree;
import org.nschmidt.ldparteditor.widget.TreeColumn;
import org.nschmidt.ldparteditor.widget.TreeItem;
import org.nschmidt.ldparteditor.workbench.UserSettingState;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

@SuppressWarnings("java:S2111")
class OptionsDesign extends ApplicationWindow {

    NButton[] btnAllowInvalidShapesPtr = new NButton[1];
    NButton[] btnTranslateViewByCursorPtr = new NButton[1];
    NButton[] btnDisableMAD3DPtr = new NButton[1];
    NButton[] btnDisableMADtextPtr = new NButton[1];
    NButton[] btnInvertInvertWheelZoomDirectionPtr = new NButton[1];
    NButton[] btnOkPtr = new NButton[1];

    final Combo[] cmbTextWinArrPtr = new Combo[1];
    final Combo[] cmbLocalePtr = new Combo[1];
    final Combo[] cmbMouseButtonLayoutPtr = new Combo[1];
    final Text[] txtLdrawPathPtr = new Text[1];
    final Text[] txtUnofficialPathPtr = new Text[1];
    final Text[] txtLdrawUserNamePtr = new Text[1];
    final Text[] txtRealNamePtr = new Text[1];
    final Text[] txtPartAuthoringPathPtr = new Text[1];
    final Combo[] cmbLicensePtr = new Combo[1];
    final NButton[] btnBrowseLdrawPathPtr = new NButton[1];
    final NButton[] btnBrowseUnofficialPathPtr = new NButton[1];
    final NButton[] btnBrowseAuthoringPathPtr = new NButton[1];
    final BigDecimalSpinner[] spnCoplanarityWarningPtr = new BigDecimalSpinner[1];
    final BigDecimalSpinner[] spnCoplanarityErrorPtr = new BigDecimalSpinner[1];
    final IntegerSpinner[] spnDataFileSizeLimitPtr = new IntegerSpinner[1];
    final BigDecimalSpinner[] spnViewportScalePtr = new BigDecimalSpinner[1];
    final Map<String, Locale> localeMap = new HashMap<>();

    private Set<Task> s1 = new HashSet<>();
    private Set<TextTask> s2 = new HashSet<>();

    private enum ColourType {
        SWT_COLOUR,
        OPENGL_COLOUR
    }

    public OptionsDesign(Shell parentShell) {
        super(parentShell);

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

    /**
     * Create contents of the application window.
     *
     * @param parent
     */
    @Override
    @SuppressWarnings("java:S2696")
    protected Control createContents(Composite parent) {
        final UserSettingState userSettings = WorkbenchManager.getUserSettingState();
        final Tree treeColours;
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

            CTabItem tItem0 = new CTabItem(tabFolderSettings, SWT.NONE);
            tItem0.setText(I18n.OPTIONS_MISC_OPTIONS);

            {

                final ScrolledComposite cmpContainerX = new ScrolledComposite(tabFolderSettings, SWT.V_SCROLL | SWT.H_SCROLL);
                Composite cmpContainer = new Composite(cmpContainerX, Cocoa.getStyle());
                tItem0.setControl(cmpContainerX);
                cmpContainerX.setContent(cmpContainer);
                cmpContainerX.setExpandHorizontal(true);
                cmpContainerX.setExpandVertical(true);

                cmpContainer.setLayout(new GridLayout());
                cmpContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

                NButton btnAllowInvalidShapes = new NButton(cmpContainer, SWT.CHECK);
                this.btnAllowInvalidShapesPtr[0] = btnAllowInvalidShapes;
                btnAllowInvalidShapes.setText(I18n.OPTIONS_ALLOW_INVALID_SHAPES);
                btnAllowInvalidShapes.setSelection(userSettings.isAllowInvalidShapes());

                NButton btnTranslateViewByCursor = new NButton(cmpContainer, SWT.CHECK);
                this.btnTranslateViewByCursorPtr[0] = btnTranslateViewByCursor;
                btnTranslateViewByCursor.setText(I18n.OPTIONS_TRANSLATE_BY_CURSOR);
                btnTranslateViewByCursor.setSelection(userSettings.isTranslatingViewByCursor());

                NButton btnDisableMAD3D = new NButton(cmpContainer, SWT.CHECK);
                this.btnDisableMAD3DPtr[0] = btnDisableMAD3D;
                btnDisableMAD3D.setText(I18n.OPTIONS_MAD_1);
                btnDisableMAD3D.setSelection(userSettings.isDisableMAD3D());

                NButton btnDisableMADtext = new NButton(cmpContainer, SWT.CHECK);
                this.btnDisableMADtextPtr[0] = btnDisableMADtext;
                btnDisableMADtext.setText(I18n.OPTIONS_MAD_2);
                btnDisableMADtext.setSelection(userSettings.isDisableMADtext());

                {
                    Label lblSeparator = new Label(cmpContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
                    lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                }

                Label lblTextWinArr = new Label(cmpContainer, SWT.NONE);
                lblTextWinArr.setText(I18n.OPTIONS_TEXT_WINDOW_ARR);

                Combo cmbTextWinArr = new Combo(cmpContainer, SWT.READ_ONLY);
                this.cmbTextWinArrPtr[0] = cmbTextWinArr;
                widgetUtil(cmbTextWinArr).setItems(I18n.OPTIONS_TEXT_WINDOW_SEPARATE, I18n.OPTIONS_TEXT_WINDOW_LEFT, I18n.OPTIONS_TEXT_WINDOW_RIGHT, I18n.OPTIONS_TEXT_WINDOW_SINGLE);
                cmbTextWinArr.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                cmbTextWinArr.select(userSettings.getTextWinArr());

                {
                    Label lblSeparator = new Label(cmpContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
                    lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                }

                Label lblCoplanarityWarning = new Label(cmpContainer, SWT.NONE);
                lblCoplanarityWarning.setText(I18n.OPTIONS_COPLANARITY_WARNING);
                lblCoplanarityWarning.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

                BigDecimalSpinner spnCoplanarityWarning = new BigDecimalSpinner(cmpContainer, Cocoa.getStyle());
                this.spnCoplanarityWarningPtr[0] = spnCoplanarityWarning;
                spnCoplanarityWarning.setMaximum(new BigDecimal("179.9999")); //$NON-NLS-1$
                spnCoplanarityWarning.setMinimum(new BigDecimal("0.01")); //$NON-NLS-1$
                spnCoplanarityWarning.setValue(new BigDecimal(Threshold.coplanarityAngleWarning));
                spnCoplanarityWarning.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

                Label lblCoplanarityError = new Label(cmpContainer, SWT.NONE);
                lblCoplanarityError.setText(I18n.OPTIONS_COPLANARITY_ERROR);
                lblCoplanarityError.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

                BigDecimalSpinner spnCoplanarityError = new BigDecimalSpinner(cmpContainer, Cocoa.getStyle());
                this.spnCoplanarityErrorPtr[0] = spnCoplanarityError;
                spnCoplanarityError.setMaximum(new BigDecimal("179.9999")); //$NON-NLS-1$
                spnCoplanarityError.setMinimum(new BigDecimal("0.01")); //$NON-NLS-1$
                spnCoplanarityError.setValue(new BigDecimal(Threshold.coplanarityAngleError));
                spnCoplanarityError.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

                {
                    Label lblSeparator = new Label(cmpContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
                    lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                }
                
                Label lblDataFileSizeLimit = new Label(cmpContainer, SWT.NONE);
                lblDataFileSizeLimit.setText(I18n.OPTIONS_DATA_FILE_SIZE_LIMIT);
                lblDataFileSizeLimit.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                
                IntegerSpinner spnDataFileSizeLimit = new IntegerSpinner(cmpContainer, Cocoa.getStyle());
                this.spnDataFileSizeLimitPtr[0] = spnDataFileSizeLimit;
                spnDataFileSizeLimit.setMaximum(1024_000);
                spnDataFileSizeLimit.setMinimum(45);
                spnDataFileSizeLimit.setValue(userSettings.getDataFileSizeLimit());
                spnDataFileSizeLimit.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                
                {
                    Label lblSeparator = new Label(cmpContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
                    lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                }

                Label lblViewportScale = new Label(cmpContainer, SWT.NONE);
                lblViewportScale.setText(I18n.OPTIONS_SCALE_FACTOR);
                lblViewportScale.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

                BigDecimalSpinner spnViewportScale = new BigDecimalSpinner(cmpContainer, Cocoa.getStyle());
                this.spnViewportScalePtr[0] = spnViewportScale;
                spnViewportScale.setMaximum(new BigDecimal("10")); //$NON-NLS-1$
                spnViewportScale.setMinimum(new BigDecimal("0.1")); //$NON-NLS-1$
                spnViewportScale.setValue(new BigDecimal(userSettings.getViewportScaleFactor()));
                spnViewportScale.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

                {
                    Label lblSeparator = new Label(cmpContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
                    lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                }

                Label lblLocale = new Label(cmpContainer, SWT.NONE);
                lblLocale.setText(I18n.OPTIONS_CHOOSE_LOCALE);

                Combo cmbLocale = new Combo(cmpContainer, SWT.READ_ONLY);
                this.cmbLocalePtr[0] = cmbLocale;

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

                cmbLocale.setItems(locales);
                cmbLocale.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                cmbLocale.select(englishIndex);

                Label lblLdrawFolderQuestion = new Label(cmpContainer, SWT.NONE);
                lblLdrawFolderQuestion.setText(I18n.OPTIONS_LDRAW_FOLDER);

                Composite cmpPathChooser1 = new Composite(cmpContainer, SWT.NONE);
                cmpPathChooser1.setLayout(new RowLayout(SWT.HORIZONTAL));

                Text txtLdrawPath = new Text(cmpPathChooser1, SWT.BORDER);
                this.txtLdrawPathPtr[0] = txtLdrawPath;
                txtLdrawPath.setEditable(false);
                txtLdrawPath.setLayoutData(new RowData(294, SWT.DEFAULT));
                txtLdrawPath.setText(userSettings.getLdrawFolderPath());

                NButton btnBrowseLdrawPath = new NButton(cmpPathChooser1, SWT.NONE);
                this.btnBrowseLdrawPathPtr[0] = btnBrowseLdrawPath;
                btnBrowseLdrawPath.setText(I18n.OPTIONS_BROWSE);

                Label lblLdrawUserQuestion = new Label(cmpContainer, SWT.NONE);
                lblLdrawUserQuestion.setText(I18n.OPTIONS_LDRAW_NAME);

                Text txtLdrawUserName = new Text(cmpContainer, SWT.BORDER);
                this.txtLdrawUserNamePtr[0] = txtLdrawUserName;
                txtLdrawUserName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                txtLdrawUserName.setText(userSettings.getLdrawUserName());

                Label lblRealNameQuestion = new Label(cmpContainer, SWT.NONE);
                lblRealNameQuestion.setText(I18n.OPTIONS_REAL_NAME);

                Text txtRealName = new Text(cmpContainer, SWT.BORDER);
                this.txtRealNamePtr[0] = txtRealName;
                txtRealName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                txtRealName.setText(userSettings.getRealUserName());

                Label lblLicenseQuestion = new Label(cmpContainer, SWT.NONE);
                lblLicenseQuestion.setText(I18n.OPTIONS_LICENSE);

                Combo cmbLicense = new Combo(cmpContainer, SWT.NONE);
                this.cmbLicensePtr[0] = cmbLicense;
                widgetUtil(cmbLicense).setItems("0 !LICENSE Licensed under CC BY 4.0 : see CAreadme.txt", "0 !LICENSE Licensed under CC BY 2.0 and CC BY 4.0 : see CAreadme.txt", "0 !LICENSE Redistributable under CCAL version 2.0 : see CAreadme.txt", "0 !LICENSE Not redistributable : see NonCAreadme.txt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                cmbLicense.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                cmbLicense.setText(userSettings.getLicense());

                Label lblAuthoringFolderQuestion = new Label(cmpContainer, SWT.NONE);
                lblAuthoringFolderQuestion.setText(I18n.OPTIONS_AUTHORING_FOLDER);

                Composite cmpPathChooser2 = new Composite(cmpContainer, SWT.NONE);
                cmpPathChooser2.setLayout(new RowLayout(SWT.HORIZONTAL));

                Text txtPartAuthoringPath = new Text(cmpPathChooser2, SWT.BORDER);
                this.txtPartAuthoringPathPtr[0] = txtPartAuthoringPath;
                txtPartAuthoringPath.setEditable(false);
                txtPartAuthoringPath.setLayoutData(new RowData(294, SWT.DEFAULT));
                txtPartAuthoringPath.setText(userSettings.getAuthoringFolderPath());

                NButton btnBrowseAuthoringPath = new NButton(cmpPathChooser2, SWT.NONE);
                this.btnBrowseAuthoringPathPtr[0] = btnBrowseAuthoringPath;
                btnBrowseAuthoringPath.setText(I18n.OPTIONS_BROWSE);

                Label lblUnofficialPathQuestion = new Label(cmpContainer, SWT.NONE);
                lblUnofficialPathQuestion.setText(I18n.OPTIONS_UNOFFICIAL_FOLDER);

                Composite cmpPathChooser3 = new Composite(cmpContainer, SWT.NONE);
                cmpPathChooser3.setLayout(new RowLayout(SWT.HORIZONTAL));

                Text txtUnofficialPath = new Text(cmpPathChooser3, SWT.BORDER);
                this.txtUnofficialPathPtr[0] = txtUnofficialPath;
                txtUnofficialPath.setEditable(false);
                txtUnofficialPath.setLayoutData(new RowData(294, SWT.DEFAULT));
                txtUnofficialPath.setText(userSettings.getUnofficialFolderPath());

                NButton btnBrowseUnofficialPath = new NButton(cmpPathChooser3, SWT.NONE);
                this.btnBrowseUnofficialPathPtr[0] = btnBrowseUnofficialPath;
                btnBrowseUnofficialPath.setText(I18n.OPTIONS_BROWSE);

                cmpContainerX.setMinSize(cmpContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
            }

            final CTabItem tItem1 = new CTabItem(tabFolderSettings, SWT.NONE);
            tItem1.setText(I18n.KEYBOARD_CUSTOMISE_SHORTKEYS);
            {
                final Composite cmpContainer = new Composite(tabFolderSettings, SWT.NONE);
                tItem1.setControl(cmpContainer);

                cmpContainer.setLayout(new GridLayout());
                cmpContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

                Label lblMouseButtonLayout = new Label(cmpContainer, SWT.NONE);
                lblMouseButtonLayout.setText(I18n.KEYBOARD_MOUSE_BUTTON_LAYOUT_TITLE);

                Combo cmbMouseButtonLayout = new Combo(cmpContainer, SWT.READ_ONLY);
                this.cmbMouseButtonLayoutPtr[0] = cmbMouseButtonLayout;
                widgetUtil(cmbMouseButtonLayout).setItems(I18n.KEYBOARD_MOUSE_BUTTON_LAYOUT_A, I18n.KEYBOARD_MOUSE_BUTTON_LAYOUT_B);
                cmbMouseButtonLayout.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                cmbMouseButtonLayout.select(userSettings.getMouseButtonLayout());

                NButton btnInvertInvertWheelZoomDirection = new NButton(cmpContainer, SWT.CHECK);
                this.btnInvertInvertWheelZoomDirectionPtr[0] = btnInvertInvertWheelZoomDirection;
                btnInvertInvertWheelZoomDirection.setText(I18n.KEYBOARD_INVERT_WHEEL_ZOOM_DIRECTION);
                btnInvertInvertWheelZoomDirection.setSelection(userSettings.isInvertingWheelZoomDirection());

                {
                    Label lblSeparator = new Label(cmpContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
                    lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                }

                Label lblDoubleClick = new Label(cmpContainer, I18n.rightToLeftStyle());
                lblDoubleClick.setText(I18n.KEYBOARD_DOUBLE_CLICK);

                final Tree tree = new Tree(cmpContainer, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL, Task.values().length + TextTask.values().length - 24);

                tree.setLinesVisible(true);
                tree.setHeaderVisible(true);
                tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

                TreeColumn trclmnDescription = new TreeColumn(tree, SWT.NONE);
                trclmnDescription.setWidth(598);
                trclmnDescription.setText(I18n.KEYBOARD_DESCRIPTION);

                TreeColumn trclmnLocation = new TreeColumn(tree, SWT.NONE);
                trclmnLocation.setWidth(100);
                trclmnLocation.setText(I18n.KEYBOARD_SHORTKEY);

                TreeItem trtmEditor3D = new TreeItem(tree);
                trtmEditor3D.setImage(ResourceManager.getImage("icon16_primitives.png")); //$NON-NLS-1$
                trtmEditor3D.setText(new String[] { I18n.KEYBOARD_EDITOR_3D, "" }); //$NON-NLS-1$
                trtmEditor3D.setVisible(true);

                TreeItem trtmEditorText = new TreeItem(tree);
                trtmEditorText.setImage(ResourceManager.getImage("icon16_annotate.png")); //$NON-NLS-1$
                trtmEditorText.setText(new String[] { I18n.KEYBOARD_EDITOR_TEXT, "" }); //$NON-NLS-1$
                trtmEditorText.setVisible(true);

                registerTask(trtmEditor3D, I18n.KEYBOARD_LMB, Task.LMB, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_MMB, Task.MMB, !Cocoa.IS_COCOA);
                registerTask(trtmEditor3D, I18n.KEYBOARD_RMB, Task.RMB, !Cocoa.IS_COCOA);
                registerTask(trtmEditor3D, I18n.KEYBOARD_CLOSE_VIEW, Task.CLOSE_VIEW, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_TOGGLE_INSERT_AT_CURSOR, Task.INSERT_AT_CURSOR, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_ADD_COMMENT, Task.ADD_COMMENTS, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_ADD_VERTEX, Task.ADD_VERTEX, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_ADD_LINE, Task.ADD_LINE, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_ADD_TRIANGLE, Task.ADD_TRIANGLE, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_ADD_QUAD, Task.ADD_QUAD, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_ADD_CONDLINE, Task.ADD_CONDLINE, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_ADD_DISTANCE, Task.ADD_DISTANCE, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_ADD_PROTRACTOR, Task.ADD_PROTRACTOR, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_CUT, Task.CUT, false);
                registerTask(trtmEditor3D, I18n.KEYBOARD_COPY, Task.COPY, false);
                registerTask(trtmEditor3D, I18n.KEYBOARD_PASTE, Task.PASTE, false);
                registerTask(trtmEditor3D, I18n.KEYBOARD_DELETE, Task.DELETE, false);
                registerTask(trtmEditor3D, I18n.KEYBOARD_ESC_1, Task.ESC, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_MERGE_TO_AVG, Task.MERGE_TO_AVERAGE, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_MERGE_TO_LAST, Task.MERGE_TO_LAST, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_SPLIT, Task.SPLIT, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_FLIP_ROTATE, Task.FLIP_ROTATE_VERTICES, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_MODE_COMBINED, Task.MODE_COMBINED, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_MODE_SELECT, Task.MODE_SELECT, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_MODE_MOVE, Task.MODE_MOVE, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_MODE_ROTATE, Task.MODE_ROTATE, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_MODE_SCALE, Task.MODE_SCALE, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_MOVE_TO_AVG, Task.MOVE_TO_AVG, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_MOVE_ADJACENT_DATA, Task.MOVE_ADJACENT_DATA, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_SWAP_WINDING, Task.SWAP_WINDING, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_CONDLINE_TO_LINE, Task.CONDLINE_TO_LINE, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_LINE_TO_CONDLINE, Task.LINE_TO_CONDLINE, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_OBJ_VERTEX, Task.OBJ_VERTEX, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_OBJ_LINE, Task.OBJ_LINE, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_OBJ_FACE, Task.OBJ_FACE, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_OBJ_PRIMITIVE, Task.OBJ_PRIMITIVE, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_AXIS_X, Task.MODE_X, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_AXIS_Y, Task.MODE_Y, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_AXIS_Z, Task.MODE_Z, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_AXIS_XY, Task.MODE_XY, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_AXIS_XZ, Task.MODE_XZ, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_AXIS_YZ, Task.MODE_YZ, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_AXIS_XYZ, Task.MODE_XYZ, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_RESET_MANIPULATOR, Task.RESET_MANIPULATOR, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_RESET_VIEW, Task.RESET_VIEW, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_SAVE, Task.SAVE, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_SELECT_ALL, Task.SELECT_ALL, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_SELECT_ALL_WITH_SAME_COLOURS, Task.SELECT_ALL_WITH_SAME_COLOURS, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_SELECT_CONNECTED, Task.SELECT_CONNECTED, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_SELECT_NONE, Task.SELECT_NONE, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_SELECT_OPTION_WITH_SAME_COLOURS, Task.SELECT_OPTION_WITH_SAME_COLOURS, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_SELECT_TOUCHING, Task.SELECT_TOUCHING, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_SHOW_GRID, Task.SHOW_GRID, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_SHOW_RULER, Task.SHOW_RULER, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_UNDO, Task.UNDO, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_REDO, Task.REDO, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_ZOOM_IN, Task.ZOOM_IN, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_ZOOM_OUT, Task.ZOOM_OUT, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_RENDERMODE_NO_BACKFACE_CULLING, Task.RENDERMODE_NO_BACKFACE_CULLING, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_RENDERMODE_RANDOM_COLOURS, Task.RENDERMODE_RANDOM_COLOURS, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_RENDERMODE_GREEN_FRONTFACES_RED_BACKFACES, Task.RENDERMODE_GREEN_FRONTFACES_RED_BACKFACES, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_RENDERMODE_RED_BACKFACES, Task.RENDERMODE_RED_BACKFACES, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_RENDERMODE_REAL_BACKFACE_CULLING, Task.RENDERMODE_REAL_BACKFACE_CULLING, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_RENDERMODE_LDRAW_STANDARD, Task.RENDERMODE_LDRAW_STANDARD, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_RENDERMODE_CONDLINE_MODE, Task.RENDERMODE_SPECIAL_CONDLINE, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_RENDERMODE_COPLANARITY_HEATMAP, Task.RENDERMODE_COPLANARITY_HEATMAP, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_RENDERMODE_WIREFRAME, Task.RENDERMODE_WIREFRAME, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_PERSPECTIVE_FRONT, Task.PERSPECTIVE_FRONT, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_PERSPECTIVE_BACK, Task.PERSPECTIVE_BACK, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_PERSPECTIVE_LEFT, Task.PERSPECTIVE_LEFT, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_PERSPECTIVE_RIGHT, Task.PERSPECTIVE_RIGHT, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_PERSPECTIVE_TOP, Task.PERSPECTIVE_TOP, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_PERSPECTIVE_BOTTOM, Task.PERSPECTIVE_BOTTOM, true);
                registerTask(trtmEditor3D, I18n.KEYBOARD_PERSPECTIVE_TWO_THIRDS, Task.PERSPECTIVE_TWO_THIRDS, true);

                registerTask(trtmEditorText, I18n.KEYBOARD_ESC_2, TextTask.EDITORTEXT_ESC, true);
                registerTask(trtmEditorText, I18n.KEYBOARD_INLINE, TextTask.EDITORTEXT_INLINE, true);
                registerTask(trtmEditorText, I18n.KEYBOARD_QUICK_FIX, TextTask.EDITORTEXT_QUICKFIX, true);
                registerTask(trtmEditorText, I18n.KEYBOARD_REDO, TextTask.EDITORTEXT_REDO, true);
                registerTask(trtmEditorText, I18n.KEYBOARD_REPLACE_VERTEX, TextTask.EDITORTEXT_REPLACE_VERTEX, true);
                registerTask(trtmEditorText, I18n.KEYBOARD_ROUND, TextTask.EDITORTEXT_ROUND, true);
                registerTask(trtmEditorText, I18n.KEYBOARD_SAVE, TextTask.EDITORTEXT_SAVE, true);
                registerTask(trtmEditorText, I18n.KEYBOARD_SELECT_ALL, TextTask.EDITORTEXT_SELECTALL, true);
                registerTask(trtmEditorText, I18n.KEYBOARD_UNDO, TextTask.EDITORTEXT_UNDO, true);
                registerTask(trtmEditorText, I18n.KEYBOARD_FIND_REPLACE, TextTask.EDITORTEXT_FIND, true);
                registerTask(trtmEditorText, I18n.KEYBOARD_INSERT_HISTORY, TextTask.EDITORTEXT_INSERT_HISTORY, true);
                registerTask(trtmEditorText, I18n.KEYBOARD_INSERT_KEYWORD, TextTask.EDITORTEXT_INSERT_KEYWORD, true);
                registerTask(trtmEditorText, I18n.KEYBOARD_INSERT_REFERENCE, TextTask.EDITORTEXT_INSERT_REFERENCE, true);
                registerTask(trtmEditorText, I18n.KEYBOARD_MOVE_LINE_UP, TextTask.EDITORTEXT_LINE_UP, true);
                registerTask(trtmEditorText, I18n.KEYBOARD_MOVE_LINE_DOWN, TextTask.EDITORTEXT_LINE_DOWN, true);

                if (s1.size() != Task.values().length || s2.size() != TextTask.values().length) {
                    throw new AssertionError("Not all shortkey items are covered by this dialog! Please fix it"); //$NON-NLS-1$
                }

                tree.build();

                tree.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseDoubleClick(MouseEvent e) {
                        final TreeItem selection;
                        if (tree.getSelectionCount() == 1 && (selection = tree.getSelection()[0]).getData() != null) {
                            KeyStateManager.tmpKeyString = null;
                            if (new KeyDialog(getShell()).open() == IDialogConstants.OK_ID && KeyStateManager.tmpKeyString != null) {
                                Object[] data = (Object[]) selection.getData();
                                if (data[0] == null) {
                                    if (KeyStateManager.hasTextTaskKey(KeyStateManager.tmpMapKey)) {
                                        showKeyAlreadyInUseWarning(selection.getParentItem(), KeyStateManager.tmpKeyString);
                                    } else {
                                        KeyStateManager.changeKey(KeyStateManager.tmpMapKey, KeyStateManager.tmpKeyString, (TextTask) data[1]);
                                        selection.setText(new String[]{selection.getText(0), KeyStateManager.tmpKeyString});
                                    }
                                }
                                if (data[1] == null) {
                                    if (KeyStateManager.hasTaskKey(KeyStateManager.tmpMapKey)) {
                                        showKeyAlreadyInUseWarning(selection.getParentItem(), KeyStateManager.tmpKeyString);
                                    } else {
                                        KeyStateManager.changeKey(KeyStateManager.tmpMapKey, KeyStateManager.tmpKeyString, (Task) data[0]);
                                        selection.setText(new String[]{selection.getText(0), KeyStateManager.tmpKeyString});
                                    }
                                }
                                tree.build();
                                tree.update();
                            }
                        }
                    }
                });
            }

            CTabItem tItem2 = new CTabItem(tabFolderSettings, SWT.NONE);
            tItem2.setText(I18n.COLOUR_CUSTOMISE_COLOURS);

            {
                final Composite cmpContainer = new Composite(tabFolderSettings, SWT.NONE);
                tItem2.setControl(cmpContainer);

                cmpContainer.setLayout(new GridLayout());
                cmpContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

                Label lblDoubleClick = new Label(cmpContainer, I18n.rightToLeftStyle());
                lblDoubleClick.setText(I18n.COLOUR_DOUBLE_CLICK);

                final Tree tree = new Tree(cmpContainer, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL, 67);
                treeColours = tree;

                tree.setLinesVisible(true);
                tree.setHeaderVisible(true);
                tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

                TreeColumn trclmnDescription = new TreeColumn(tree, SWT.NONE);
                trclmnDescription.setWidth(598);
                trclmnDescription.setText(I18n.COLOUR_DESCRIPTION);

                TreeColumn trclmnColour = new TreeColumn(tree, SWT.NONE);
                trclmnColour.setWidth(100);
                trclmnColour.setText(I18n.COLOUR_COLOUR);

                TreeItem trtmEditor3D = new TreeItem(tree);
                trtmEditor3D.setImage(ResourceManager.getImage("icon16_primitives.png")); //$NON-NLS-1$
                trtmEditor3D.setText(new String[] { I18n.KEYBOARD_EDITOR_3D, "" }); //$NON-NLS-1$
                trtmEditor3D.setVisible(true);

                TreeItem trtmEditorText = new TreeItem(tree);
                trtmEditorText.setImage(ResourceManager.getImage("icon16_annotate.png")); //$NON-NLS-1$
                trtmEditorText.setText(new String[] { I18n.KEYBOARD_EDITOR_TEXT, "" }); //$NON-NLS-1$
                trtmEditorText.setVisible(true);

                registerColour(trtmEditor3D, I18n.COLOUR_OVERRIDE_COLOUR_16, ColourType.OPENGL_COLOUR, new Object[]{LDConfig.colour16overrideR, LDConfig.colour16overrideG, LDConfig.colour16overrideB}, (r, g, b) -> { LDConfig.colour16overrideR = r; LDConfig.colour16overrideG = g; LDConfig.colour16overrideB = b;});

                registerColour(trtmEditor3D, I18n.COLOUR_BACKGROUND_3D_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.backgroundColourR, Colour.backgroundColourG, Colour.backgroundColourB}, (r, g, b) -> { Colour.backgroundColourR = r; Colour.backgroundColourG = g; Colour.backgroundColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_BFC_BACK_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.bfcBackColourR, Colour.bfcBackColourG, Colour.bfcBackColourB}, (r, g, b) -> { Colour.bfcBackColourR = r; Colour.bfcBackColourG = g; Colour.bfcBackColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_BFC_FRONT_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.bfcFrontColourR, Colour.bfcFrontColourG, Colour.bfcFrontColourB}, (r, g, b) -> { Colour.bfcFrontColourR = r; Colour.bfcFrontColourG = g; Colour.bfcFrontColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_BFC_UNCERTIFIED, ColourType.OPENGL_COLOUR, new Object[]{Colour.bfcUncertifiedColourR, Colour.bfcUncertifiedColourG, Colour.bfcUncertifiedColourB}, (r, g, b) -> { Colour.bfcUncertifiedColourR = r; Colour.bfcUncertifiedColourG = g; Colour.bfcUncertifiedColourB = b;});

                registerColour(trtmEditor3D, I18n.COLOUR_ORIGIN_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.originColourR, Colour.originColourG, Colour.originColourB}, (r, g, b) -> { Colour.originColourR = r; Colour.originColourG = g; Colour.originColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_GRID_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.gridColourR, Colour.gridColourG, Colour.gridColourB}, (r, g, b) -> { Colour.gridColourR = r; Colour.gridColourG = g; Colour.gridColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_GRID_10_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.grid10ColourR, Colour.grid10ColourG, Colour.grid10ColourB}, (r, g, b) -> { Colour.grid10ColourR = r; Colour.grid10ColourG = g; Colour.grid10ColourB = b;});

                registerColour(trtmEditor3D, I18n.COLOUR_CURSOR_1_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.cursor1ColourR, Colour.cursor1ColourG, Colour.cursor1ColourB}, (r, g, b) -> { Colour.cursor1ColourR = r; Colour.cursor1ColourG = g; Colour.cursor1ColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_CURSOR_2_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.cursor2ColourR, Colour.cursor2ColourG, Colour.cursor2ColourB}, (r, g, b) -> { Colour.cursor2ColourR = r; Colour.cursor2ColourG = g; Colour.cursor2ColourB = b;});

                registerColour(trtmEditor3D, I18n.COLOUR_MESH_LINE_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.meshlineColourR, Colour.meshlineColourG, Colour.meshlineColourB}, (r, g, b) -> { Colour.meshlineColourR = r; Colour.meshlineColourG = g; Colour.meshlineColourB = b;});

                registerColour(trtmEditor3D, I18n.COLOUR_ADD_OBJECT_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.addObjectColourR, Colour.addObjectColourG, Colour.addObjectColourB}, (r, g, b) -> { Colour.addObjectColourR = r; Colour.addObjectColourG = g; Colour.addObjectColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_SHOWN_CONDLINE_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.condlineShownColourR, Colour.condlineShownColourG, Colour.condlineShownColourB}, (r, g, b) -> { Colour.condlineShownColourR = r; Colour.condlineShownColourG = g; Colour.condlineShownColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_HIDDEN_CONDLINE_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.condlineHiddenColourR, Colour.condlineHiddenColourG, Colour.condlineHiddenColourB}, (r, g, b) -> { Colour.condlineHiddenColourR = r; Colour.condlineHiddenColourG = g; Colour.condlineHiddenColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_LIGHT_1_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.light1ColourR, Colour.light1ColourG, Colour.light1ColourB}, (r, g, b) -> { Colour.light1ColourR = r; Colour.light1ColourG = g; Colour.light1ColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_LIGHT_1_SPECULAR_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.light1SpecularColourR, Colour.light1SpecularColourG, Colour.light1SpecularColourB}, (r, g, b) -> { Colour.light1SpecularColourR = r; Colour.light1SpecularColourG = g; Colour.light1SpecularColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_LIGHT_2_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.light2ColourR, Colour.light2ColourG, Colour.light2ColourB}, (r, g, b) -> { Colour.light2ColourR = r; Colour.light2ColourG = g; Colour.light2ColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_LIGHT_2_SPECULAR_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.light2SpecularColourR, Colour.light2SpecularColourG, Colour.light2SpecularColourB}, (r, g, b) -> { Colour.light2SpecularColourR = r; Colour.light2SpecularColourG = g; Colour.light2SpecularColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_LIGHT_3_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.light3ColourR, Colour.light3ColourG, Colour.light3ColourB}, (r, g, b) -> { Colour.light3ColourR = r; Colour.light3ColourG = g; Colour.light3ColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_LIGHT_3_SPECULAR_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.light3SpecularColourR, Colour.light3SpecularColourG, Colour.light3SpecularColourB}, (r, g, b) -> { Colour.light3SpecularColourR = r; Colour.light3SpecularColourG = g; Colour.light3SpecularColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_LIGHT_4_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.light4ColourR, Colour.light4ColourG, Colour.light4ColourB}, (r, g, b) -> { Colour.light4ColourR = r; Colour.light4ColourG = g; Colour.light4ColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_LIGHT_4_SPECULAR_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.light4SpecularColourR, Colour.light4SpecularColourG, Colour.light4SpecularColourB}, (r, g, b) -> { Colour.light4SpecularColourR = r; Colour.light4SpecularColourG = g; Colour.light4SpecularColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_LINE_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.lineColourR, Colour.lineColourG, Colour.lineColourB}, (r, g, b) -> { Colour.lineColourR = r; Colour.lineColourG = g; Colour.lineColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_MANIPULATOR_INNER_CIRCLE_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.manipulatorInnerCircleColourR, Colour.manipulatorInnerCircleColourG, Colour.manipulatorInnerCircleColourB}, (r, g, b) -> { Colour.manipulatorInnerCircleColourR = r; Colour.manipulatorInnerCircleColourG = g; Colour.manipulatorInnerCircleColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_MANIPULATOR_OUTER_CIRCLE_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.manipulatorOuterCircleColourR, Colour.manipulatorOuterCircleColourG, Colour.manipulatorOuterCircleColourB}, (r, g, b) -> { Colour.manipulatorOuterCircleColourR = r; Colour.manipulatorOuterCircleColourG = g; Colour.manipulatorOuterCircleColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_MANIPULATOR_X_AXIS_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.manipulatorXAxisColourR, Colour.manipulatorXAxisColourG, Colour.manipulatorXAxisColourB}, (r, g, b) -> { Colour.manipulatorXAxisColourR = r; Colour.manipulatorXAxisColourG = g; Colour.manipulatorXAxisColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_MANIPULATOR_Y_AXIS_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.manipulatorYAxisColourR, Colour.manipulatorYAxisColourG, Colour.manipulatorYAxisColourB}, (r, g, b) -> { Colour.manipulatorYAxisColourR = r; Colour.manipulatorYAxisColourG = g; Colour.manipulatorYAxisColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_MANIPULATOR_Z_AXIS_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.manipulatorZAxisColourR, Colour.manipulatorZAxisColourG, Colour.manipulatorZAxisColourB}, (r, g, b) -> { Colour.manipulatorZAxisColourR = r; Colour.manipulatorZAxisColourG = g; Colour.manipulatorZAxisColourB = b;});

                registerColour(trtmEditor3D, I18n.COLOUR_VERTEX_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.vertexColourR, Colour.vertexColourG, Colour.vertexColourB}, (r, g, b) -> { Colour.vertexColourR = r; Colour.vertexColourG = g; Colour.vertexColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_SELECTED_VERTEX_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.vertexSelectedColourR, Colour.vertexSelectedColourG, Colour.vertexSelectedColourB}, (r, g, b) -> { Colour.vertexSelectedColourR = r; Colour.vertexSelectedColourG = g; Colour.vertexSelectedColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_SELECTED_CONDLINE_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.condlineSelectedColourR, Colour.condlineSelectedColourG, Colour.condlineSelectedColourB}, (r, g, b) -> { Colour.condlineSelectedColourR = r; Colour.condlineSelectedColourG = g; Colour.condlineSelectedColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_SELECTED_MANIPULATOR_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.manipulatorSelectedColourR, Colour.manipulatorSelectedColourG, Colour.manipulatorSelectedColourB}, (r, g, b) -> { Colour.manipulatorSelectedColourR = r; Colour.manipulatorSelectedColourG = g; Colour.manipulatorSelectedColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_RUBBERBAND_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.rubberBandColourR, Colour.rubberBandColourG, Colour.rubberBandColourB}, (r, g, b) -> { Colour.rubberBandColourR = r; Colour.rubberBandColourG = g; Colour.rubberBandColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_TEXT_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.textColourR, Colour.textColourG, Colour.textColourB}, (r, g, b) -> { Colour.textColourR = r; Colour.textColourG = g; Colour.textColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_X_AXIS_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.xAxisColourR, Colour.xAxisColourG, Colour.xAxisColourB}, (r, g, b) -> { Colour.xAxisColourR = r; Colour.xAxisColourG = g; Colour.xAxisColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_Y_AXIS_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.yAxisColourR, Colour.yAxisColourG, Colour.yAxisColourB}, (r, g, b) -> { Colour.yAxisColourR = r; Colour.yAxisColourG = g; Colour.yAxisColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_Z_AXIS_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.zAxisColourR, Colour.zAxisColourG, Colour.zAxisColourB}, (r, g, b) -> { Colour.zAxisColourR = r; Colour.zAxisColourG = g; Colour.zAxisColourB = b;});

                registerColour(trtmEditor3D, I18n.COLOUR_PRIMITVE_BG_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.primitiveBackgroundColourR, Colour.primitiveBackgroundColourG, Colour.primitiveBackgroundColourB}, (r, g, b) -> { Colour.primitiveBackgroundColourR = r; Colour.primitiveBackgroundColourG = g; Colour.primitiveBackgroundColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_PRIMITVE_SIGN_FG_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.primitiveSignFgColourR, Colour.primitiveSignFgColourG, Colour.primitiveSignFgColourB}, (r, g, b) -> { Colour.primitiveSignFgColourR = r; Colour.primitiveSignFgColourG = g; Colour.primitiveSignFgColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_PRIMITVE_SIGN_BG_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.primitiveSignBgColourR, Colour.primitiveSignBgColourG, Colour.primitiveSignBgColourB}, (r, g, b) -> { Colour.primitiveSignBgColourR = r; Colour.primitiveSignBgColourG = g; Colour.primitiveSignBgColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_PRIMITVE_PLUS_MINUS_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.primitivePlusNMinusColourR, Colour.primitivePlusNMinusColourG, Colour.primitivePlusNMinusColourB}, (r, g, b) -> { Colour.primitivePlusNMinusColourR = r; Colour.primitivePlusNMinusColourG = g; Colour.primitivePlusNMinusColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_PRIMITVE_SELECTED_CELL_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.primitiveSelectedCellColourR, Colour.primitiveSelectedCellColourG, Colour.primitiveSelectedCellColourB}, (r, g, b) -> { Colour.primitiveSelectedCellColourR = r; Colour.primitiveSelectedCellColourG = g; Colour.primitiveSelectedCellColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_PRIMITVE_FOCUSED_CELL_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.primitiveFocusedCellColourR, Colour.primitiveFocusedCellColourG, Colour.primitiveFocusedCellColourB}, (r, g, b) -> { Colour.primitiveFocusedCellColourR = r; Colour.primitiveFocusedCellColourG = g; Colour.primitiveFocusedCellColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_PRIMITVE_NORMAL_CELL_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.primitiveNormalCellColourR, Colour.primitiveNormalCellColourG, Colour.primitiveNormalCellColourB}, (r, g, b) -> { Colour.primitiveNormalCellColourR = r; Colour.primitiveNormalCellColourG = g; Colour.primitiveNormalCellColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_PRIMITVE_CELL_1_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.primitiveCell1ColourR, Colour.primitiveCell1ColourG, Colour.primitiveCell1ColourB}, (r, g, b) -> { Colour.primitiveCell1ColourR = r; Colour.primitiveCell1ColourG = g; Colour.primitiveCell1ColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_PRIMITVE_CELL_2_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.primitiveCell2ColourR, Colour.primitiveCell2ColourG, Colour.primitiveCell2ColourB}, (r, g, b) -> { Colour.primitiveCell2ColourR = r; Colour.primitiveCell2ColourG = g; Colour.primitiveCell2ColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_PRIMITVE_CATEGORY_CELL_1_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.primitiveCategoryCell1ColourR, Colour.primitiveCategoryCell1ColourG, Colour.primitiveCategoryCell1ColourB}, (r, g, b) -> { Colour.primitiveCategoryCell1ColourR = r; Colour.primitiveCategoryCell1ColourG = g; Colour.primitiveCategoryCell1ColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_PRIMITVE_CATEGORY_CELL_2_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.primitiveCategoryCell2ColourR, Colour.primitiveCategoryCell2ColourG, Colour.primitiveCategoryCell2ColourB}, (r, g, b) -> { Colour.primitiveCategoryCell2ColourR = r; Colour.primitiveCategoryCell2ColourG = g; Colour.primitiveCategoryCell2ColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_PRIMITVE_EDGE_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.primitiveEdgeColourR, Colour.primitiveEdgeColourG, Colour.primitiveEdgeColourB}, (r, g, b) -> { Colour.primitiveEdgeColourR = r; Colour.primitiveEdgeColourG = g; Colour.primitiveEdgeColourB = b;});
                registerColour(trtmEditor3D, I18n.COLOUR_PRIMITVE_CONDLINE_COLOUR, ColourType.OPENGL_COLOUR, new Object[]{Colour.primitiveCondlineColourR, Colour.primitiveCondlineColourG, Colour.primitiveCondlineColourB}, (r, g, b) -> { Colour.primitiveCondlineColourR = r; Colour.primitiveCondlineColourG = g; Colour.primitiveCondlineColourB = b;});

                registerColour(trtmEditorText, I18n.COLOUR_TEXT_EDITOR_BG_COLOUR, ColourType.SWT_COLOUR, TextEditorColour.getTextBackground(), (r,g,b) -> TextEditorColour.loadTextBackground(SWTResourceManager.getColor(r.intValue(), g.intValue(), b.intValue())));
                registerColour(trtmEditorText, I18n.COLOUR_TEXT_EDITOR_FG_COLOUR, ColourType.SWT_COLOUR, TextEditorColour.getTextForeground(), (r,g,b) -> TextEditorColour.loadTextForeground(SWTResourceManager.getColor(r.intValue(), g.intValue(), b.intValue())));
                registerColour(trtmEditorText, I18n.COLOUR_TEXT_EDITOR_FG_COLOUR_HIDDEN, ColourType.SWT_COLOUR, TextEditorColour.getTextForegroundHidden(), (r,g,b) -> TextEditorColour.loadTextForegroundHidden(SWTResourceManager.getColor(r.intValue(), g.intValue(), b.intValue())));
                registerColour(trtmEditorText, I18n.COLOUR_TEXT_EDITOR_SELECTED_BG_COLOUR, ColourType.SWT_COLOUR, TextEditorColour.getLineHighlightSelectedBackground(), (r,g,b) -> TextEditorColour.loadLineHighlightSelectedBackground(SWTResourceManager.getColor(r.intValue(), g.intValue(), b.intValue())));
                registerColour(trtmEditorText, I18n.COLOUR_TEXT_EDITOR_HIGHLIGHT_BG_COLOUR, ColourType.SWT_COLOUR, TextEditorColour.getLineHighlightBackground(), (r,g,b) -> TextEditorColour.loadLineHighlightBackground(SWTResourceManager.getColor(r.intValue(), g.intValue(), b.intValue())));
                registerColour(trtmEditorText, I18n.COLOUR_TEXT_EDITOR_COMMENT_COLOUR, ColourType.SWT_COLOUR, TextEditorColour.getLineCommentFont(), (r,g,b) -> TextEditorColour.loadLineCommentFont(SWTResourceManager.getColor(r.intValue(), g.intValue(), b.intValue())));
                registerColour(trtmEditorText, I18n.COLOUR_TEXT_EDITOR_HINT_UNDERLINE_COLOUR, ColourType.SWT_COLOUR, TextEditorColour.getLineHintUnderline(), (r,g,b) -> TextEditorColour.loadLineHintUnderline(SWTResourceManager.getColor(r.intValue(), g.intValue(), b.intValue())));
                registerColour(trtmEditorText, I18n.COLOUR_TEXT_EDITOR_WARNING_UNDERLINE_COLOUR, ColourType.SWT_COLOUR, TextEditorColour.getLineWarningUnderline(), (r,g,b) -> TextEditorColour.loadLineWarningUnderline(SWTResourceManager.getColor(r.intValue(), g.intValue(), b.intValue())));
                registerColour(trtmEditorText, I18n.COLOUR_TEXT_EDITOR_ERROR_UNDERLINE_COLOUR, ColourType.SWT_COLOUR, TextEditorColour.getLineErrorUnderline(), (r,g,b) -> TextEditorColour.loadLineErrorUnderline(SWTResourceManager.getColor(r.intValue(), g.intValue(), b.intValue())));
                registerColour(trtmEditorText, I18n.COLOUR_TEXT_EDITOR_PRIMARY_COLOUR, ColourType.SWT_COLOUR, TextEditorColour.getLinePrimaryFont(), (r,g,b) -> TextEditorColour.loadLinePrimaryFont(SWTResourceManager.getColor(r.intValue(), g.intValue(), b.intValue())));
                registerColour(trtmEditorText, I18n.COLOUR_TEXT_EDITOR_SECONDARY_COLOUR, ColourType.SWT_COLOUR, TextEditorColour.getLineSecondaryFont(), (r,g,b) -> TextEditorColour.loadLineSecondaryFont(SWTResourceManager.getColor(r.intValue(), g.intValue(), b.intValue())));
                registerColour(trtmEditorText, I18n.COLOUR_TEXT_EDITOR_QUAD_COLOUR, ColourType.SWT_COLOUR, TextEditorColour.getLineQuadFont(), (r,g,b) -> TextEditorColour.loadLineQuadFont(SWTResourceManager.getColor(r.intValue(), g.intValue(), b.intValue())));
                registerColour(trtmEditorText, I18n.COLOUR_TEXT_EDITOR_COLOUR_ATTR_COLOUR, ColourType.SWT_COLOUR, TextEditorColour.getLineColourAttrFont(), (r,g,b) -> TextEditorColour.loadLineColourAttrFont(SWTResourceManager.getColor(r.intValue(), g.intValue(), b.intValue())));
                registerColour(trtmEditorText, I18n.COLOUR_TEXT_EDITOR_BOX_COLOUR, ColourType.SWT_COLOUR, TextEditorColour.getLineBoxFont(), (r,g,b) -> TextEditorColour.loadLineBoxFont(SWTResourceManager.getColor(r.intValue(), g.intValue(), b.intValue())));

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
                                @SuppressWarnings("unchecked")
                                TriConsumer<Float, Float, Float> consumer = (TriConsumer<Float, Float, Float>) colourObj[2];
                                switch (type) {
                                case OPENGL_COLOUR:
                                    consumer.consume(refCol.getR(), refCol.getG(), refCol.getB());
                                    break;
                                case SWT_COLOUR:
                                    consumer.consume((float) rgb.red, (float) rgb.green, (float) rgb.blue);
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
            tabFolderSettings.setSelection(tItem0);
        }


        Composite cmpButtons = new Composite(container, SWT.NONE);

        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.minimumHeight = 200;
        gridData.minimumWidth = 160;
        gridData.verticalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        cmpButtons.setLayoutData(gridData);

        GridLayout gl = new GridLayout(2, true);
        cmpButtons.setLayout(gl);

        Composite spacer = new Composite(cmpButtons, SWT.NONE);

        GridData gridData2 = new GridData();
        gridData2.horizontalAlignment = SWT.FILL;
        gridData2.grabExcessHorizontalSpace = true;
        spacer.setLayoutData(gridData2);

        NButton btnOK = new NButton(cmpButtons, SWT.NONE);
        this.btnOkPtr[0] = btnOK;
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
        String actionString = I18n.OPTIONS_ADVANCED_USAGE;
        final MessageBox messageBoxInfo = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
        final Map<String, String> reservedKeysMappedToAction = new HashMap<>();
        final String inputTheColourNumber = I18n.OPTIONS_COLOUR_NUMBER_INPUT;

        for (int n = 0; n < 10; n++) {
            reservedKeysMappedToAction.put("NUMPAD_" + n, inputTheColourNumber); //$NON-NLS-1$
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
        Object[] messageArguments = {keyString, actionString};
        MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
        formatter.setLocale(MyLanguage.getLocale());
        formatter.applyPattern(I18n.OPTIONS_KEY_COMBO_IN_USE);
        messageBoxInfo.setMessage(formatter.format(messageArguments));
        messageBoxInfo.open();
    }

    private void updateColours(Tree tree) {
        for(TreeItem ti : tree.getItems()) {
            updateColoursHelper(ti);
        }
    }

    private void updateColoursHelper(TreeItem ti) {
        org.eclipse.swt.widgets.TreeItem key = ti.getParent().getMapInv().get(ti);
        if (key != null && ti.getData() != null && ((Object[]) ti.getData()).length == 3) {
            Object[] colourObj = (Object[]) ti.getData();
            ColourType type = (ColourType) colourObj[0];
            switch (type) {
            case OPENGL_COLOUR:
                key.setBackground(1, SWTResourceManager.getColor(
                        (int) (255f * ((float)((Object[]) colourObj[1])[0])),
                        (int) (255f * ((float)((Object[]) colourObj[1])[1])),
                        (int) (255f * ((float)((Object[]) colourObj[1])[2]))));
                break;
            case SWT_COLOUR:
                key.setBackground(1, ((Color) colourObj[1]));
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
            Map<Task, String> m = KeyStateManager.getTaskKeymap();
            keyCombination = m.get(t1);
        } else if (t2 != null) {
            Map<TextTask, String> m = KeyStateManager.getTextTaskKeymap();
            keyCombination = m.get(t2);
        }
        if (visibility) {
            TreeItem trtmNewKey = new TreeItem(parent);
            trtmNewKey.setText(new String[] { description, keyCombination });
            trtmNewKey.setVisible(true);
            trtmNewKey.setData(new Object[]{t1, t2});
        }
    }

    private void registerColour(TreeItem parent, String description, ColourType type, Object colourObj, TriConsumer<Float, Float, Float> consumer) {
        TreeItem trtmNewKey = new TreeItem(parent);
        trtmNewKey.setText(new String[] { description, "" }); //$NON-NLS-1$
        trtmNewKey.setVisible(true);
        trtmNewKey.setData(new Object[]{type, colourObj, consumer});
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return new Point(super.getInitialSize().x, (int) (super.getInitialSize().y * 2.6));
    }
}
