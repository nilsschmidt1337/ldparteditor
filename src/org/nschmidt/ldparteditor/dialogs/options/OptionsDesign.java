package org.nschmidt.ldparteditor.dialogs.options;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
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
import org.nschmidt.ldparteditor.dialogs.keys.KeyDialog;
import org.nschmidt.ldparteditor.enums.Task;
import org.nschmidt.ldparteditor.enums.TextTask;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.resources.ResourceManager;
import org.nschmidt.ldparteditor.state.KeyStateManager;
import org.nschmidt.ldparteditor.widgets.BigDecimalSpinner;
import org.nschmidt.ldparteditor.widgets.Tree;
import org.nschmidt.ldparteditor.widgets.TreeColumn;
import org.nschmidt.ldparteditor.widgets.TreeItem;

class OptionsDesign extends ApplicationWindow {

    private HashSet<Task>  s1 = new HashSet<Task>();
    private HashSet<TextTask> s2 = new HashSet<TextTask>();

    {
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
            tItem.setText(I18n.KEYBOARD_CustomiseShortkeys);
            {
                final Composite cmp_container = new Composite(tabFolder_Settings, SWT.NONE);
                tItem.setControl(cmp_container);

                cmp_container.setLayout(new GridLayout());
                cmp_container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

                Label lbl_DoubleClick = new Label(cmp_container, I18n.I18N_RTL());
                lbl_DoubleClick.setText(I18n.KEYBOARD_DoubleClick);

                final Tree tree = new Tree(cmp_container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL, Task.values().length + TextTask.values().length - 11);

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

                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_ToggleInsertAtCursor, Task.INSERT_AT_CURSOR, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_AddComment, Task.ADD_COMMENTS, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_AddVertex, Task.ADD_VERTEX, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_AddLine, Task.ADD_LINE, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_AddTriangle, Task.ADD_TRIANGLE, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_AddQuad, Task.ADD_QUAD, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_AddCondline, Task.ADD_CONDLINE, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_Cut, Task.CUT, false);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_Copy, Task.COPY, false);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_Paste, Task.PASTE, false);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_Delete, Task.DELETE, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_Esc1, Task.ESC, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_LMB, Task.LMB, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_MergeToAvg, Task.MERGE_TO_AVERAGE, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_MergeToLast, Task.MERGE_TO_LAST, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_MMB, Task.MMB, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_ModeCombined, Task.MODE_COMBINED, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_ModeMove, Task.MODE_MOVE, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_ModeRotate, Task.MODE_ROTATE, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_ModeScale, Task.MODE_SCALE, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_ModeSelect, Task.MODE_SELECT, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_ObjFace, Task.OBJ_FACE, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_ObjLine, Task.OBJ_LINE, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_ObjPrimitive, Task.OBJ_PRIMITIVE, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_ObjVertex, Task.OBJ_VERTEX, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_ResetView, Task.RESET_VIEW, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_RMB, Task.RMB, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_Save, Task.SAVE, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_SelectAll, Task.SELECT_ALL, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_SelectAllWithSameColours, Task.SELECT_ALL_WITH_SAME_COLOURS, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_SelectConnected, Task.SELECT_CONNECTED, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_SelectNone, Task.SELECT_NONE, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_SelectOptionWithSameColours, Task.SELECT_OPTION_WITH_SAME_COLOURS, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_SelectTouching, Task.SELECT_TOUCHING, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_ShowGrid, Task.SHOW_GRID, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_ShowRuler, Task.SHOW_RULER, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_Split, Task.SPLIT, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_Undo, Task.UNDO, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_Redo, Task.REDO, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_ZoomIn, Task.ZOOM_IN, true);
                registerDoubleClickEvent(trtm_Editor3D, I18n.KEYBOARD_ZoomOut, Task.ZOOM_OUT, true);

                registerDoubleClickEvent(trtm_EditorText, I18n.KEYBOARD_Esc2, TextTask.EDITORTEXT_ESC, true);
                registerDoubleClickEvent(trtm_EditorText, I18n.KEYBOARD_Inline, TextTask.EDITORTEXT_INLINE, true);
                registerDoubleClickEvent(trtm_EditorText, I18n.KEYBOARD_QuickFix, TextTask.EDITORTEXT_QUICKFIX, true);
                registerDoubleClickEvent(trtm_EditorText, I18n.KEYBOARD_Redo, TextTask.EDITORTEXT_REDO, true);
                registerDoubleClickEvent(trtm_EditorText, I18n.KEYBOARD_ReplaceVertex, TextTask.EDITORTEXT_REPLACE_VERTEX, true);
                registerDoubleClickEvent(trtm_EditorText, I18n.KEYBOARD_Round, TextTask.EDITORTEXT_ROUND, true);
                registerDoubleClickEvent(trtm_EditorText, I18n.KEYBOARD_Save, TextTask.EDITORTEXT_SAVE, true);
                registerDoubleClickEvent(trtm_EditorText, I18n.KEYBOARD_SelectAll, TextTask.EDITORTEXT_SELECTALL, true);
                registerDoubleClickEvent(trtm_EditorText, I18n.KEYBOARD_Undo, TextTask.EDITORTEXT_UNDO, true);
                registerDoubleClickEvent(trtm_EditorText, I18n.KEYBOARD_FindReplace, TextTask.EDITORTEXT_FIND, true);
                registerDoubleClickEvent(trtm_EditorText, I18n.KEYBOARD_InsertReference, TextTask.EDITORTEXT_INSERT_REFERENCE, true);

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

        Button btnOK = new Button(cmp_Buttons, SWT.NONE);
        btnOK.setText(I18n.DIALOG_OK);

        GridData gridData3 = new GridData();
        gridData3.horizontalAlignment = SWT.FILL;
        gridData3.grabExcessHorizontalSpace = true;
        btnOK.setLayoutData(gridData3);

        return container;
    }

    private void registerDoubleClickEvent(TreeItem parent, String description, Task t, boolean visibility) {
        s1.add(t);
        registerDoubleClickEvent(parent, description, t, null, visibility);
    }

    private void registerDoubleClickEvent(TreeItem parent, String description, TextTask t, boolean visibility) {
        s2.add(t);
        registerDoubleClickEvent(parent, description, null, t, visibility);
    }

    private void registerDoubleClickEvent(TreeItem parent, String description, Task t1, TextTask t2, boolean visibility) {

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

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return new Point(super.getInitialSize().x, super.getInitialSize().y * 2);
    }

    // FIXME OptionsDialog needs implementation!
}
